package com.scarlet.coroutines.advanced

import com.scarlet.util.log
import com.scarlet.util.onCompletion
import kotlinx.coroutines.*

@DelicateCoroutinesApi
object Dependency_Between_Jobs {

    @JvmStatic
    fun main(args: Array<String>) = runBlocking<Unit> {

        // coroutine starts when start() or join() called
        val job = launch(start = CoroutineStart.LAZY) {
            delay(100)
            log("Pong")
        }

        launch {
            log("Ping")
            job.join()
            log("Ping")
        }
    }
}

object Jobs_Forms_Hierarchy {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val parentJob = launch {
            log("I am parent")

            launch {
                log("\t\tI am a child of the parentJob")
                delay(1000)
            }.invokeOnCompletion { log("\t\tchild completes") }

            launch { // To check whether already finished child counted as children
                log("\t\tI am another child of the parentJob")
                delay(500)
            }.invokeOnCompletion { log("\t\tanother child completes") }

        }.apply{
            invokeOnCompletion { log("parentJob completes") }
        }

        launch {
            log("I’m a sibling of the parentJob, not its child")
            delay(1000)
        }.invokeOnCompletion { log("sibling completes") }

        delay(300)
        log("The parentJob has ${parentJob.children.count()} children")

        delay(500)
        log("The parentJob has ${parentJob.children.count()} children")
    }
}

object In_Hierarchy_Parent_Waits_Until_All_Children_Finished {

    /**
     * Parental responsibilities:
     *
     * A parent coroutine always waits for completion of all its children.
     * A parent does not have to explicitly track all the children it launches,
     * and it does not have to use `Job.join` to wait for them at the end:
     */

    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        // launch a coroutine to process some kind of incoming request
        val parent = launch {
            repeat(3) { i -> // launch a few children jobs
                launch { // try Dispatchers.Default
                    delay((i + 1) * 200L) // variable delay 200ms, 400ms, 600ms
                    log("\t\tChild Coroutine $i is done")
                }
            }
            log("parent: I'm done, but will wait until all my children completes")
            // No need to join here
        }.onCompletion("parent: now, I am completed")

        parent.join() // wait for completion of the request, including all its children
        log("Done")
    }
}

object In_Hierarchy_Parent_Waits_Until_All_Children_Finished_Other_Demo {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val parentJob = launch {
            log("I’m the parent")
        }.onCompletion("Finally, parent finished ...")

        launch(parentJob) {
            log("\t\tI’m a child")
            delay(1000)
        }.onCompletion("\t\tChild finished after 1000")

        log("The Parent job has ${parentJob.children.count()} child right after child launch")
        log("is Parent active right after child launch? ${parentJob.isActive}")

        delay(500)
        log("is Parent still active at 500? ${parentJob.isActive}")

        parentJob.join()
        log("is Parent still active after joined? ${parentJob.isActive}")
    }
}


