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

package com.archinamon

import org.gradle.api.GradleException
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

/**
 * TODO: Add description
 *
 * @author archinamon on 11/04/17.
 */
class AndroidPluginTest {

    @Test
    fun detectAppPlugin() {
        val project = ProjectBuilder.builder().build()
        project.apply(mapOf(Pair("plugin", "com.android.application")))
        project.apply(mapOf(Pair("plugin", "com.thunderhead.android.aspectj")))
    }

    @Test
    fun detectAppExtPlugin() {
        val project = ProjectBuilder.builder().build()
        project.apply(mapOf(Pair("plugin", "com.android.application")))
        project.apply(mapOf(Pair("plugin", "com.thunderhead.android.aspectj-ext")))
    }

    @Test
    fun detectTestPlugin() {
        val project = ProjectBuilder.builder().build()
        project.apply(mapOf(Pair("plugin", "com.android.application")))
        project.apply(mapOf(Pair("plugin", "com.thunderhead.android.aspectj-test")))
    }

    @Test
    fun detectLibPlugin() {
        val project = ProjectBuilder.builder().build()
        project.apply(mapOf(Pair("plugin", "com.android.library")))
        project.apply(mapOf(Pair("plugin", "com.thunderhead.android.aspectj")))
    }

    @Test
    fun detectLibExtPlugin() {
        val project = ProjectBuilder.builder().build()
        project.apply(mapOf(Pair("plugin", "com.android.library")))
        project.apply(mapOf(Pair("plugin", "com.thunderhead.android.aspectj-ext")))
    }

    @Test
    fun detectLibTestPlugin() {
        val project = ProjectBuilder.builder().build()
        project.apply(mapOf(Pair("plugin", "com.android.library")))
        project.apply(mapOf(Pair("plugin", "com.thunderhead.android.aspectj-test")))
    }

    @Test(expected = GradleException::class)
    fun failsWithoutAndroidPlugin() {
        val project = ProjectBuilder.builder().build()
        project.apply(mapOf(Pair("plugin", "com.thunderhead.android.aspectj")))
    }

    @Test(expected = GradleException::class)
    fun failsExtWithoutAndroidPlugin() {
        val project = ProjectBuilder.builder().build()
        project.apply(mapOf(Pair("plugin", "com.thunderhead.android.aspectj-ext")))
    }

    @Test(expected = GradleException::class)
    fun failsTestWithoutAndroidPlugin() {
        val project = ProjectBuilder.builder().build()
        project.apply(mapOf(Pair("plugin", "com.thunderhead.android.aspectj-test")))
    }
}