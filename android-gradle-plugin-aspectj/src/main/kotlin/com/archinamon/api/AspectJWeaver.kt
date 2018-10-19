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

package com.archinamon.api

import com.archinamon.utils.logBuildParametersAdapted
import com.archinamon.utils.logExtraAjcArgumentAlreadyExists
import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
import org.gradle.api.GradleException
import org.gradle.api.Project
import java.io.File
import java.util.*

internal class AspectJWeaver(val project: Project) {

    private val errorReminder = "Look into %s file for details"

    var compilationLogFile: String? = null
        internal set(name) {
            if (name?.length!! > 0) {
                field = project.buildDir.absolutePath + File.separator + name
            }
        }

    var transformLogFile: String? = null
        internal set(name) {
            if (name?.length!! > 0) {
                field = project.buildDir.absolutePath + File.separator + name
            }
        }

    var encoding: String? = null

    var weaveInfo: Boolean = false
    var debugInfo: Boolean = false
    var addSerialVUID: Boolean = false
    var noInlineAround: Boolean = false
    var ignoreErrors: Boolean = false

    var breakOnError: Boolean = false
    var experimental: Boolean = false

    var ajcArgs = LinkedHashSet<String>()

    var ajSources: MutableSet<File> = LinkedHashSet()
        internal set(ajSources) {
            ajSources.forEach { this.ajSources.add(it) }
        }

    var aspectPath: MutableSet<File> = LinkedHashSet()
    var inPath: MutableSet<File> = LinkedHashSet()
    var classPath: MutableSet<File> = LinkedHashSet()
    var bootClasspath: String? = null
    var sourceCompatibility: String? = null
    var targetCompatibility: String? = null
    var destinationDir: String? = null

    internal fun doWeave() {
        val log = prepareLogger()

        //http://www.eclipse.org/aspectj/doc/released/devguide/ajc-ref.html

        val args = mutableListOf(
            "-encoding", encoding,
            "-source", sourceCompatibility,
            "-target", targetCompatibility,
            "-d", destinationDir,
            "-bootclasspath", bootClasspath,
            "-classpath", classPath.joinToString(separator = File.pathSeparator)
        )

        if (ajSources.isNotEmpty()) {
            args + "-sourceroots" + ajSources.joinToString(separator = File.pathSeparator)
        }

        if (inPath.isNotEmpty()) {
            args + "-inpath" + inPath.joinToString(separator = File.pathSeparator)
        }

        if (aspectPath.isNotEmpty()) {
            args + "-aspectpath" + aspectPath.joinToString(separator = File.pathSeparator)
        }

        if (getLogFile().isNotBlank()) {
            args + "-log" + getLogFile()
        }

        if (debugInfo) {
            args + "-g"
        }

        if (weaveInfo) {
            args + "-showWeaveInfo"
        }

        if (addSerialVUID) {
            args + "-XaddSerialVersionUID"
        }

        if (noInlineAround) {
            args + "-XnoInline"
        }

        if (ignoreErrors) {
            args + "-proceedOnError" + "-noImportError"
        }

        if (experimental) {
            args + "-XhasMember" + "-Xjoinpoints:synchronization,arrayconstruction"
        }

        if (ajcArgs.isNotEmpty()) {
            ajcArgs.forEach { extra ->
                if (extra.startsWith('-') && args.contains(extra)) {
                    logExtraAjcArgumentAlreadyExists(extra)
                    log.writeText("[warning] Duplicate argument found while composing ajc config! Build may be corrupted.\n\n")
                }
                args + extra
            }
        }

        log.writeText("Full ajc build args: ${args.joinToString()}\n\n")
        logBuildParametersAdapted(args, log.name)

        val handler = MessageHandler(true)
        Main().run(args.toTypedArray(), handler)
        for (message in handler.getMessages(null, true)) {
            when (message.kind) {
                IMessage.ERROR -> {
                    log.writeText("[error]" + message?.message + "${message?.thrown}\n\n")
                    if (breakOnError) throw GradleException (errorReminder.format(getLogFile()))
                }
                IMessage.FAIL, IMessage.ABORT -> {
                    log.writeText("[error]" + message?.message + "${message?.thrown}\n\n")
                    throw GradleException (message.message)
                }
                IMessage.INFO, IMessage.DEBUG, IMessage.WARNING -> {
                    log.writeText("[warning]" + message?.message + "${message?.thrown}\n\n")
                    if (getLogFile().isNotBlank()) log.writeText("${errorReminder.format(getLogFile())}\n\n")
                }
            }
        }

        detectErrors()
    }

    private fun getLogFile(): String {
        return compilationLogFile ?: transformLogFile!!
    }

    private fun prepareLogger(): File {
        val lf = project.file(getLogFile())
        if (lf.exists()) {
            lf.delete()
        }

        return lf
    }

    private fun detectErrors() {
        val lf: File  = project.file(getLogFile())
        if (lf.exists()) {
            lf.readLines().reversed().forEach { line ->
                if (line.contains("[error]") && breakOnError) {
                    throw GradleException ("$line\n${errorReminder.format(getLogFile())}")
                }
            }
        }
    }

    private inline operator fun <reified E> MutableCollection<in E>.plus(elem: E): MutableCollection<in E> {
        this.add(elem)
        return this
    }
}