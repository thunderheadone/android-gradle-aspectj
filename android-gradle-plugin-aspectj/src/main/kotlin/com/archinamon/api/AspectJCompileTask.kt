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

package com.archinamon.api

import com.archinamon.AndroidConfig
import com.archinamon.AspectJExtension
import com.archinamon.extensions.aspectJExtension
import com.archinamon.lang.kotlin.closureOf
import com.archinamon.plugin.ConfigScope
import com.archinamon.utils.*
import org.aspectj.util.FileUtil
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.ConventionTask
import org.gradle.api.internal.file.collections.SimpleFileCollection
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.compile.JavaCompile
import java.io.File
import java.util.*

// TODO: This task doesn't do incremental builds. Is that problem?
internal open class AspectJCompileTask : ConventionTask() {
    private var javaCompileDestinationDir: File? = null
    private var classpath: FileCollection? = null
    private var destinationDir: File? = null

    internal class Builder(val project: Project) {
        private lateinit var config: AspectJExtension
        private lateinit var javaCompiler: JavaCompile
        private lateinit var variantName: String
        private lateinit var taskName: String

        fun config(extension: AspectJExtension): Builder {
            this.config = extension
            return this
        }

        fun compiler(compiler: JavaCompile): Builder {
            this.javaCompiler = compiler
            return this
        }

        fun variant(name: String): Builder {
            this.variantName = name
            return this
        }

        fun name(name: String): Builder {
            this.taskName = name
            return this
        }

        fun buildAndAttach(android: AndroidConfig) {
            val options = mutableMapOf(
                    "overwrite" to true,
                    "dependsOn" to javaCompiler.name,
                    "group" to "build",
                    "description" to "Compile .aj source files into java .class with meta instructions",
                    "type" to AspectJCompileTask::class.java
            )

            val sources = findAjSourcesForVariant(project, variantName)
            val task = project.task(options, taskName, closureOf<AspectJCompileTask> task@ {
                destinationDir = obtainBuildDirectory(android)
                aspectJWeaver = AspectJWeaver(project)
                javaCompileDestinationDir = javaCompiler.destinationDir

                classpath = classpath()
                findCompiledAspectsInClasspath(this, project.aspectJExtension.includeAspectsFromJar)

                aspectJWeaver.apply {
                    val destination = this@task.destinationDir
                            ?: throw GradleException("Aspectj Compile Task Destination null.")

                    ajSources = sources

                    inPath shl destination shl javaCompiler.destinationDir

                    targetCompatibility = JavaVersion.VERSION_1_7.toString()
                    sourceCompatibility = JavaVersion.VERSION_1_7.toString()
                    destinationDir = destination.absolutePath
                    bootClasspath = android.getBootClasspath().joinToString(separator = File.pathSeparator)
                    encoding = javaCompiler.options.encoding

                    compilationLogFile = config.compilationLogFile
                    addSerialVUID = config.addSerialVersionUID
                    debugInfo = config.debugInfo
                    addSerialVUID = config.addSerialVersionUID
                    noInlineAround = config.noInlineAround
                    ignoreErrors = config.ignoreErrors
                    breakOnError = config.breakOnError
                    experimental = config.experimental
                    ajcArgs from config.ajcArgs
                }
            }) as AspectJCompileTask

            // uPhyca's fix
            // javaCompile.classpath does not contain exploded-aar/**/jars/*.jars till first run
            javaCompiler.doLast {
                task.classpath = classpath()
                findCompiledAspectsInClasspath(task, project.aspectJExtension.includeAspectsFromJar)
            }

            //apply behavior
            javaCompiler.finalizedBy(task)
        }

        private fun obtainBuildDirectory(android: AndroidConfig): File? {
            return if (android.scope == ConfigScope.PROVIDE) {
                javaCompiler.destinationDir
            } else {
                project.file("${project.buildDir}/aspectj/$variantName")
            }
        }

        private fun classpath(): FileCollection {
            return SimpleFileCollection(javaCompiler.classpath.files + javaCompiler.destinationDir)
        }

        private fun findCompiledAspectsInClasspath(task: AspectJCompileTask, aspectsFromJar: Collection<String>) {
            val classpath = task.classpath
            if (classpath != null) {
                val aspects: MutableSet<File> = mutableSetOf()

                classpath.forEach { file ->
                    if (aspectsFromJar.isNotEmpty() && DependencyFilter.isIncludeFilterMatched(file, aspectsFromJar)) {
                        logJarAspectAdded(file)
                        aspects shl file
                    }
                }

                if (aspects.isNotEmpty()) task.aspectJWeaver.aspectPath from aspects
            }
        }
    }

    lateinit var aspectJWeaver: AspectJWeaver

    @TaskAction
    fun compile() {
        logCompilationStart()

        destinationDir?.deleteRecursively()

        if (classpath == null) {
            throw GradleException("AspectJ compile task classpath null.")
        }
        aspectJWeaver.classPath = LinkedHashSet(classpath!!.files)
        aspectJWeaver.doWeave()

        /*
         * Java8 Enabled Apps only:
         *
         * Replacing the javac task output with the processed AJ output
         * so that the assemble tasks can include the AJ code.
         * This normally happens in the AspectJTransform.kt transform method,
         * however in java8 the desugar tool modifies the bytecode constant pool
         * resulting in an error.
         *
         * To avoid the error, perform the weave now during the compilation task,
         * and bypass the transformer.
         */
        if (sourceCompatibilityIsJavaEight(project)) {
            FileUtil.copyDir(destinationDir, javaCompileDestinationDir)
        }

        logCompilationFinish()
    }
}
