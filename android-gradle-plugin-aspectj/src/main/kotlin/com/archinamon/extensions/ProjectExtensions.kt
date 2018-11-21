/*
 *    Copyright 2015 Eduard "Archinamon" Matsukov.
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

package com.archinamon.extensions

import com.android.build.gradle.*
import com.archinamon.AspectJExtension
import org.gradle.api.GradleException
import org.gradle.api.Project

val Project.androidExtension: TestedExtension
    get() = when {
        plugins.hasPlugin(AppPlugin::class.java) -> {
            extensions.getByType(AppExtension::class.java)
        }
        plugins.hasPlugin(LibraryPlugin::class.java) -> {
            extensions.getByType(LibraryExtension::class.java)
        }
        else -> {
            throw GradleException("""Invalid Android Project Type.
                |Expected:
                |${AppPlugin::class.java.name}
                |or
                |${LibraryPlugin::class.java.name}""".trimMargin())
        }
    }

val Project.aspectJExtension: AspectJExtension
    get() = extensions.getByType(AspectJExtension::class.java)
