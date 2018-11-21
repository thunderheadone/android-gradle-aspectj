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

package com.archinamon

import com.android.build.gradle.*
import com.archinamon.plugin.ConfigScope
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File

private const val ASPECTJ_PLUGIN = "com.archinamon.aspectj"
const val RETROLAMBDA = "me.tatarka.retrolambda"
const val MISDEFINITION = "Illegal definition: $ASPECTJ_PLUGIN should be defined after $RETROLAMBDA plugin"

private const val TAG = "AJC:"
private const val PLUGIN_EXCEPTION = "$TAG You must apply the Android plugin or the Android library plugin"

internal class AndroidConfig(val project: Project, val scope: ConfigScope) {

    val extAndroid: BaseExtension
    val isLibraryPlugin: Boolean
    val plugin: BasePlugin

    init {
        when {
            project.plugins.hasPlugin(AppPlugin::class.java) -> {
                extAndroid = project.extensions.getByType(AppExtension::class.java)
                plugin = project.plugins.getPlugin(AppPlugin::class.java)
                isLibraryPlugin = false
            }

            project.plugins.hasPlugin(LibraryPlugin::class.java) -> {
                extAndroid = project.extensions.getByType(LibraryExtension::class.java)
                plugin = project.plugins.getPlugin(LibraryPlugin::class.java)
                isLibraryPlugin = true
            }

            project.plugins.hasPlugin(TestPlugin::class.java) -> {
                extAndroid = project.extensions.getByType(TestExtension::class.java)
                plugin = project.plugins.getPlugin(TestPlugin::class.java)
                isLibraryPlugin = false
            }

            else -> {
                isLibraryPlugin = false
                throw GradleException(PLUGIN_EXCEPTION)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun getBootClasspath(): List<File> {
        return extAndroid.bootClasspath ?: plugin::class.java.getMethod("getRuntimeJarList").invoke(plugin) as List<File>
    }
}