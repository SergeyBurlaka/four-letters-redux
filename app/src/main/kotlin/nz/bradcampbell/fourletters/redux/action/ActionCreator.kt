package nz.bradcampbell.fourletters.redux.action

import nz.bradcampbell.fourletters.R
import nz.bradcampbell.fourletters.data.Clock
import nz.bradcampbell.fourletters.data.WordRepository
import nz.bradcampbell.fourletters.redux.state.Page
import nz.bradcampbell.fourletters.redux.store.Store
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.Subscriptions
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
public class ActionCreator @Inject constructor(val store: Store,
                                               val wordRepository: WordRepository,
                                               val clock: Clock) {
    private var initGameSubscription: Subscription = Subscriptions.empty()
    private var checkWinSubscription: Subscription = Subscriptions.empty()

    public companion object {
        const val GAME_DURATION = 20000L
        const val TIME_BONUS = 5000L
        const val POINTS_PER_WIN = 1
    }

    public fun initiateGame() {
        store.dispatch(Action.Navigate(Page(R.layout.loading)))
        initGameSubscription = wordRepository.getRandomWord()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                store.dispatch(Action.InitGame(it, clock.millis() + GAME_DURATION))
                store.dispatch(Action.Navigate(Page(R.layout.game), false))
            }, {
                store.dispatch(Action.Back)
                store.dispatch(Action.LoadWordError)
            });
    }

    public fun playAgain() {
        store.dispatch(Action.Back)
        initiateGame()
    }

    public fun gameOver() {
        store.dispatch(Action.Navigate(Page(R.layout.lose), false))
    }

    public fun back() {
        initGameSubscription.unsubscribe()
        checkWinSubscription.unsubscribe()
        store.dispatch(Action.Back)
    }

    public fun dismissWordLoadError() {
        store.dispatch(Action.DismissLoadWordError)
    }

    public fun leftLetterPressed() {
        letterPressed(Action.LeftPressed)
    }

    public fun topLetterPressed() {
        letterPressed(Action.TopPressed)
    }

    public fun rightLetterPressed() {
        letterPressed(Action.RightPressed)
    }

    public fun bottomLetterPressed() {
        letterPressed(Action.BottomPressed)
    }

    private fun letterPressed(action: Action) {
        store.dispatch(action)
        checkWin()
    }

    private fun checkWin() {
        val gameState = store.state().gameState!!
        val answer = gameState.answer
        if (answer.size == 4) {
            val stringAnswer = answer.map { it.letter }.joinToString("")
            if (gameState.possibleAnswers.contains(stringAnswer)) {
                checkWinSubscription = wordRepository.getRandomWord()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        // Bonus time is TIME_BONUS, but the time bonus addition can never make the finish time be
                        // greater than GAME_DURATION from now
                        val oldFinishTime = store.state().gameState!!.finishTime
                        val timeRemaining = oldFinishTime - clock.millis();
                        val newFinishTime = clock.millis() + Math.min(timeRemaining + TIME_BONUS, GAME_DURATION)
                        val bonusTime = newFinishTime - oldFinishTime
                        store.dispatch(Action.NextGame(it, POINTS_PER_WIN, bonusTime))
                    }, {
                        store.dispatch(Action.Back)
                        store.dispatch(Action.LoadWordError)
                    })
            } else {
                store.dispatch(Action.ResetGame)
            }
        }
    }
}
