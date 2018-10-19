/*
 *    Copyright 2018 the original author or authors.
 *    Copyright 2018 Thunderhead
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.archinamon.api

import org.gradle.BuildListener
import org.gradle.BuildResult
import org.gradle.api.Task
import org.gradle.api.execution.TaskExecutionListener
import org.gradle.api.initialization.Settings
import org.gradle.api.invocation.Gradle
import org.gradle.api.tasks.TaskState

class BuildTimeListener: TaskExecutionListener, BuildListener {

    private var startTime: Long = 0L
    private var times = mutableListOf<Pair<Long, String>>()

    override fun buildStarted(gradle: Gradle) {}
    override fun settingsEvaluated(settings: Settings) {}
    override fun projectsLoaded(gradle: Gradle) {}
    override fun projectsEvaluated(gradle: Gradle) {}

    override fun buildFinished(result: BuildResult) {
        println("Task spend time:")
        times.filter { it.first > 50 }
            .forEach { println("%7sms\t%s".format(it.first, it.second)) }
    }

    override fun beforeExecute(task: Task) {
        startTime = System.currentTimeMillis()
    }

    override fun afterExecute(task: Task, state: TaskState) {
        val ms = System.currentTimeMillis() - startTime
        times.add(Pair(ms, task.path))
        task.project.logger.warn("${task.path} spend ${ms}ms")
    }
}