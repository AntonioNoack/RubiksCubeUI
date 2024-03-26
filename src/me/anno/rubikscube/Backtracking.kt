package me.anno.rubikscube

import kotlin.math.max

// todo solve the given cube via backtracing
// todo encode the current state, so we can create a hash from it, and check for identities
object Backtracking {
    data class Task<State>(val state: State, val score: Double, val origin: Task<State>?)

    fun <State> solve(
        getScore: (State) -> Double,
        getNextStates: (State) -> Collection<State>,
        isTarget: (Task<State>) -> Boolean,
        startState: State, maxNumSteps: Int
    ): List<State>? {
        val startScore = getScore(startState)
        val startTask = Task(startState, startScore, null)
        if (isTarget(startTask)) {
            return listOf(startState)
        }
        var maxSteps = maxNumSteps
        val nextBestTasks = ArrayList<Task<State>>(64)
        val uniqueStates = HashSet<State>()
        nextBestTasks.add(startTask)
        while (nextBestTasks.isNotEmpty() && maxSteps-- > 0) {
            val next = nextBestTasks.removeLast()
            val newStates = getNextStates(next.state)
            for (state in newStates) {
                if (uniqueStates.add(state)) {
                    val score = getScore(state)
                    val task = Task(state, score, next)
                    if (isTarget(task)) {
                        return backtrack(next, state)
                    }
                    val idx = nextBestTasks.binarySearch { task.score.compareTo(it.score) }
                    val ii = max(-1 - idx, 0)
                    nextBestTasks.add(ii, task)
                }
            }
        }
        return null
    }

    fun <State> backtrack(
        next: Task<State>, state: State,
    ): List<State> {
        val result = ArrayList<State>()
        result.add(state)
        var currState = next
        while (true) {
            result.add(currState.state)
            currState = currState.origin ?: break
        }
        result.reverse()
        return result
    }
}