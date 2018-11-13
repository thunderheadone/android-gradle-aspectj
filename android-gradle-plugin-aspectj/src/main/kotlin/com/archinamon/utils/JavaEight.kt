@file:JvmName("JavaEightUtil")

package com.archinamon.utils

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Project

fun checkJavaEight(project: Project): Boolean = when {
    project.plugins.hasPlugin(AppPlugin::class.java) -> {
        project.extensions.getByType(AppExtension::class.java).compileOptions.sourceCompatibility == JavaVersion.VERSION_1_8
    }
    project.plugins.hasPlugin(LibraryPlugin::class.java) -> {
        project.extensions.getByType(LibraryExtension::class.java).compileOptions.sourceCompatibility == JavaVersion.VERSION_1_8
    }
    else -> {
        false
    }
}
