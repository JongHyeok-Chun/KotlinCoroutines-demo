package com.scarlet.coroutines.exceptions

import com.scarlet.util.completeStatus
import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import java.lang.RuntimeException

@ExperimentalCoroutinesApi
class LaunchSupervisorJobTest {

    /**
     * SupervisorJob - failing child does not affect the parent and its sibling.
     *
     * Location of SupervisorJob matters:
     *  - Warning: SupervisorJob only works when it is part of a failing
     *    coroutine's direct parent context!! <-- by kim
     */

    /**
     * Quiz: Who's child1's parent?
     */

    @Test
    fun `lecture note example - who's child1's parent`() = runTest {

        val parentJob = launch(SupervisorJob()) {
            launch {
                delay(100)
                throw IOException("failure")
            }.onCompletion("child1")

            launch { delay(200) }.onCompletion("child2")
        }.onCompletion("parent")

        parentJob.join()
    }

    @Test
    fun `SupervisorJob in failing child's parent context takes effect`() = runTest {
        val scope = CoroutineScope(SupervisorJob()) // Compare with Job()

        val child1 = scope.launch {
            delay(100)
            throw RuntimeException("oops")
        }.onCompletion("child1")

        val child2 = scope.launch { delay(200) }.onCompletion("child2")

        joinAll(child1, child2)
        scope.completeStatus()
    }

    @Test
    fun `SupervisorJob in parent context controls the lifetime of children`() = runTest {
        val scope = CoroutineScope(Job())
        val sharedJob = SupervisorJob()

        val child1 = scope.launch(sharedJob) {
            delay(100)
            throw RuntimeException("oops")
        }.onCompletion("child1")

        val child2 = scope.launch(sharedJob) {
            delay(200)
        }.onCompletion("child2")

        joinAll(child1, child2)
        sharedJob.completeStatus("sharedJob")
        scope.completeStatus("scope")
    }

    @Test
    fun `SupervisorJob in parent context controls only the lifetime of its own children`() = runTest {
        val scope = CoroutineScope(Job())
        val sharedJob = SupervisorJob()

        val child1 = scope.launch(sharedJob) {
            delay(100)
            throw RuntimeException("oops")
        }.onCompletion("child1")

        val child2 = scope.launch(sharedJob) {
            delay(200)
        }.onCompletion("child2")

        val child3 = scope.launch {
            delay(200)
        }.onCompletion("child3")

        val child4 = scope.launch {
            delay(200)
        }.onCompletion("child4")

        joinAll(child1, child2, child3, child4)
        sharedJob.completeStatus("sharedJob")
        scope.completeStatus()
    }

    @Test
    fun `SupervisorJob does not work when it is not part of the failing child's parent context`() = runTest {
        try {
            val scope = CoroutineScope(Job())
            try {

                val parentJob = scope.launch(SupervisorJob()) {
                    launch {
                        delay(100)
                        throw RuntimeException("oops")
                    }.onCompletion("child1")
                    launch {
                        delay(1000)
                    }.onCompletion("child2")
                }.onCompletion("parentJob")
                parentJob.join()

            } catch (ex: Exception) {
                log("Exception caught: $ex") // No use
            }
            scope.completeStatus()
        } catch (ex: Exception) {
            log("Outer: Exception caught: $ex") // No use
        }
    }

}
