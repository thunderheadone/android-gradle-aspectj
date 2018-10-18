/*
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

package com.archinamon.api.transform

import org.gradle.api.Project

/**
 * Simple transformers declarations and common data
 *
 * @author archinamon on 07.10.17.
 */

internal const val TRANSFORM_NAME = "aspectj"
const val AJRUNTIME = "aspectjrt"
const val SLICER_DETECTED_ERROR = "Running with InstantRun slicer when weaver extended not allowed!"

enum class BuildPolicy {

    SIMPLE,
    COMPLEX,
    LIBRARY
}

internal class StandardTransformer(project: Project): AspectJTransform(project, BuildPolicy.SIMPLE)
internal class ExtendedTransformer(project: Project): AspectJTransform(project, BuildPolicy.COMPLEX)
internal class TestsTransformer(project: Project): AspectJTransform(project, BuildPolicy.COMPLEX)