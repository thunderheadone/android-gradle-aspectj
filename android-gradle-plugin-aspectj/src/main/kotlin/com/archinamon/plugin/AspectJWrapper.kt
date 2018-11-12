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

package com.archinamon.plugin

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.TestedExtension
import com.archinamon.AndroidConfig
import com.archinamon.AspectJExtension
import com.archinamon.api.transform.*
import org.gradle.api.Plugin
import org.gradle.api.Project
import javax.inject.Inject

internal sealed class AspectJWrapper(private val scope: ConfigScope): Plugin<Project> {

    internal class Standard @Inject constructor(): AspectJWrapper(ConfigScope.STANDARD) {
        override fun getTransformer(project: Project): AspectJTransform = StandardTransformer(project)
    }

    internal class Provides @Inject constructor(): AspectJWrapper(ConfigScope.PROVIDE) {
        override fun getTransformer(project: Project): AspectJTransform = ProvidesTransformer(project)
    }

    internal class Extended @Inject constructor(): AspectJWrapper(ConfigScope.EXTEND) {
        override fun getTransformer(project: Project): AspectJTransform = ExtendedTransformer(project)
    }

    internal class Test @Inject constructor(): AspectJWrapper(ConfigScope.TEST) {
        override fun getTransformer(project: Project): AspectJTransform = TestsTransformer(project)
    }

    override fun apply(project: Project) {
        val config = AndroidConfig(project, scope)
        val settings = project.extensions.create("aspectj", AspectJExtension::class.java)

        configProject(project, config, settings)

        val module: TestedExtension
        val transformer: AspectJTransform
        if (config.isLibraryPlugin) {
            transformer = LibraryTransformer(project)
            module = project.extensions.getByType(LibraryExtension::class.java)
        } else {
            transformer = getTransformer(project)
            module = project.extensions.getByType(AppExtension::class.java)
        }

        transformer.withConfig(config).prepareProject()
//        module.registerTransform(transformer)
    }

    internal abstract fun getTransformer(project: Project): AspectJTransform
}