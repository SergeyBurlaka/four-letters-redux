package nz.bradcampbell.fourletters.tests

import nz.bradcampbell.fourletters.R
import nz.bradcampbell.fourletters.redux.action.Action
import nz.bradcampbell.fourletters.redux.state.State
import nz.bradcampbell.fourletters.redux.state.Page
import nz.bradcampbell.fourletters.redux.state.PaginationState
import nz.bradcampbell.fourletters.redux.reducer.PaginateReducer
import org.junit.Test
import kotlin.test.assertEquals

class PaginateReducerTests {

    var menuPage = Page(R.layout.menu)
    var gamePage = Page(R.layout.game)
    var losePage = Page(R.layout.lose)

    @Test public fun testInitialStateIsCorrect() {
        val appState = State()
        assertEquals(appState.paginationState.currentPage, menuPage)
        assertEquals(appState.paginationState.history, emptyList())
    }

    @Test public fun testNavigateWithEmptyHistory() {
        val paginateReducer = PaginateReducer()
        var appState = State()
        val action = Action.Navigate(gamePage)
        appState = paginateReducer.call(action, appState)
        assertEquals(appState.paginationState.currentPage, gamePage)
        assertEquals(appState.paginationState.history, listOf(menuPage))
    }

    @Test public fun testNavigateWithExistingItemInHistory() {
        val paginateReducer = PaginateReducer()
        var appState = State(PaginationState(currentPage = gamePage, history = listOf(menuPage)))
        val action = Action.Navigate(losePage)
        appState = paginateReducer.call(action, appState)
        assertEquals(appState.paginationState.currentPage, losePage)
        assertEquals(appState.paginationState.history, listOf(menuPage, gamePage))
    }

    @Test public fun testNavigateWithoutAddingToBackStack() {
        val paginateReducer = PaginateReducer()
        var appState = State()
        val action = Action.Navigate(gamePage, false)
        appState = paginateReducer.call(action, appState)
        assertEquals(appState.paginationState.currentPage, gamePage)
        assertEquals(appState.paginationState.history, emptyList())
    }

    @Test public fun testNavigateWithoutAddingToBackStackWithOneItemInHistory() {
        val paginateReducer = PaginateReducer()
        var appState = State(PaginationState(currentPage = gamePage, history = listOf(menuPage)))
        val action = Action.Navigate(losePage, false)
        appState = paginateReducer.call(action, appState)
        assertEquals(appState.paginationState.currentPage, losePage)
        assertEquals(appState.paginationState.history, listOf(menuPage))
    }

    @Test public fun testBackWithOneItemInHistory() {
        val paginateReducer = PaginateReducer()
        var appState = State(PaginationState(currentPage = gamePage, history = listOf(menuPage)))
        val action = Action.Back
        appState = paginateReducer.call(action, appState)
        assertEquals(appState.paginationState.currentPage, menuPage)
        assertEquals(appState.paginationState.history, emptyList())
    }

    @Test public fun testBackWithTwoItemsInHistory() {
        val paginateReducer = PaginateReducer()
        var appState = State(PaginationState(currentPage = losePage, history = listOf(menuPage, gamePage)))
        val action = Action.Back
        appState = paginateReducer.call(action, appState)
        assertEquals(appState.paginationState.currentPage, gamePage)
        assertEquals(appState.paginationState.history, listOf(menuPage))
    }
}