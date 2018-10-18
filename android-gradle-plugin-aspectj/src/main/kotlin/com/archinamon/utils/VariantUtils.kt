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

package com.archinamon.utils

import com.android.build.gradle.BasePlugin
import com.android.build.gradle.internal.scope.VariantScope
import com.android.build.gradle.internal.variant.BaseVariantData
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.collections.SimpleFileCollection
import org.gradle.api.tasks.compile.JavaCompile
import java.io.File

fun getJavaTask(baseVariantData: BaseVariantData): JavaCompile? {
    if (baseVariantData.javacTask != null) {
        return baseVariantData.javacTask
    } else if (baseVariantData.javaCompilerTask != null) {
        return baseVariantData.javaCompilerTask as JavaCompile
    }
    return null
}

fun getAjSourceAndExcludeFromJavac(project: Project, variantData: BaseVariantData): FileCollection {
    val javaTask = getJavaTask(variantData)

    val flavors: List<String>? = variantData.variantConfiguration.productFlavors.map { flavor -> flavor.name }
    val srcSet = mutableListOf("main", variantData.variantConfiguration!!.buildType!!.name)
    flavors?.let { srcSet.addAll(it) }

    val srcDirs = srcSet.map { "src/$it/aspectj" }
    val aspects: FileCollection = SimpleFileCollection(srcDirs.map { project.file(it) })

    javaTask!!.exclude { treeElem ->
        treeElem.file in aspects.files
    }

    return aspects.filter(File::exists)
}

fun findAjSourcesForVariant(project: Project, variantName: String): MutableSet<File> {
    val possibleDirs: MutableSet<File> = mutableSetOf()
    if (project.file("src/main/aspectj").exists()) {
        possibleDirs.add(project.file("src/main/aspectj"))
    }

    val types = variantName.split("(?=\\p{Upper})".toRegex())
    val root = project.file("src").listFiles()

    root.forEach { file ->
        types.forEach { type ->
            if (file.name.contains(type.toLowerCase()) &&
                    file.list().any { it.contains("aspectj") }) {
                possibleDirs.add(File(file, "aspectj"))
            }
        }
    }

    return LinkedHashSet(possibleDirs)
}

fun getVariantDataList(plugin: BasePlugin): List<BaseVariantData> {
    return plugin.variantManager.variantScopes.map(VariantScope::getVariantData)
}

internal infix fun <E> MutableCollection<in E>.shl(elem: E): MutableCollection<in E> {
    this.add(elem)
    return this
}

internal infix fun <E> MutableCollection<in E>.from(elems: Collection<E>) {
    this.addAll(elems)
}