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

package com.archinamon.utils

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.CompileOptions
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.PluginContainer
import org.junit.Test


class JavaEightUtilTest {
    private val mockCompileOptions = mock<CompileOptions>()
    private val mockAppExtension = mock<AppExtension> {
        on { compileOptions } doReturn mockCompileOptions
    }
    private val mockLibraryExtension = mock<LibraryExtension> {
        on { compileOptions } doReturn mockCompileOptions
    }
    private val mockExtensions = mock<ExtensionContainer> {
        on { getByType(any<Class<AppExtension>>()) } doReturn mockAppExtension
        on { getByType(any<Class<LibraryExtension>>()) } doReturn mockLibraryExtension
    }
    private val mockPlugins = mock<PluginContainer> {
        on { hasPlugin(AppPlugin::class.java) } doReturn true
        on { hasPlugin(LibraryPlugin::class.java) } doReturn true
    }
    private val mockProject = mock<Project> {
        on { plugins } doReturn mockPlugins
        on { extensions } doReturn mockExtensions
    }

    @Test
    fun `test check for java8 source compatibility`() {
        // Test AppPlugin
        whenever(mockCompileOptions.sourceCompatibility).thenReturn(JavaVersion.VERSION_1_8)
        var result = sourceCompatibilityIsJavaEight(mockProject)
        assertThat(result).isTrue()

        whenever(mockCompileOptions.sourceCompatibility).thenReturn(JavaVersion.VERSION_1_7)
        result = sourceCompatibilityIsJavaEight(mockProject)
        assertThat(result).isFalse()

        // Test LibraryPlugin
        whenever(mockPlugins.hasPlugin(AppPlugin::class.java)).thenReturn(false)

        whenever(mockCompileOptions.sourceCompatibility).thenReturn(JavaVersion.VERSION_1_8)
        result = sourceCompatibilityIsJavaEight(mockProject)
        assertThat(result).isTrue()

        whenever(mockCompileOptions.sourceCompatibility).thenReturn(JavaVersion.VERSION_1_7)
        result = sourceCompatibilityIsJavaEight(mockProject)
        assertThat(result).isFalse()
    }
}