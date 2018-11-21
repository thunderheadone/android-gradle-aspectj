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

@file:JvmName("JavaEightUtil")

package com.archinamon.utils

import com.archinamon.extensions.androidExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project

/**
 * @param [org.gradle.api.Project] The gradle project to check that the plugin is applied to.
 * @return [Boolean] - True if [com.android.build.gradle.TestedExtension.compileOptions] has sourceCompatibility set to [JavaVersion.VERSION_1_8] .
 */
fun sourceCompatibilityIsJavaEight(project: Project): Boolean = project.androidExtension.compileOptions.sourceCompatibility == JavaVersion.VERSION_1_8
