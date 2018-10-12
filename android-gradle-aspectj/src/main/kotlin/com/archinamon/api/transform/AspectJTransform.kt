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
 *
 *    Note: This is a new license header as previous contributor did
 *    not provide a boilerplate header and copyright.
 */

package com.archinamon.api.transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformInvocationBuilder
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.build.gradle.internal.pipeline.TransformTask
import com.android.build.gradle.internal.variant.BaseVariantData
import com.android.utils.FileUtils
import com.archinamon.AndroidConfig
import com.archinamon.api.AspectJMergeJars
import com.archinamon.api.AspectJWeaver
import com.archinamon.plugin.ConfigScope
import com.archinamon.utils.*
import com.archinamon.utils.DependencyFilter.isExcludeFilterMatched
import com.archinamon.utils.DependencyFilter.isIncludeFilterMatched
import com.google.common.collect.Sets
import org.aspectj.util.FileUtil
import org.gradle.api.GradleException
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import java.io.File

internal abstract class AspectJTransform(val project: Project, private val policy: BuildPolicy): Transform() {

    private lateinit var config: AndroidConfig

    private val aspectJWeaver: AspectJWeaver = AspectJWeaver(project)
    private val aspectJMerger: AspectJMergeJars = AspectJMergeJars()

    fun withConfig(config: AndroidConfig): AspectJTransform {
        this.config = config
        return this
    }

    open fun prepareProject(): AspectJTransform {
        project.afterEvaluate {
            getVariantDataList(config.plugin).forEach(this::setupVariant)

            with(config.aspectj()) {
                aspectJWeaver.weaveInfo = weaveInfo
                aspectJWeaver.debugInfo = debugInfo
                aspectJWeaver.addSerialVUID = addSerialVersionUID
                aspectJWeaver.noInlineAround = noInlineAround
                aspectJWeaver.ignoreErrors = ignoreErrors
                aspectJWeaver.transformLogFile = transformLogFile
                aspectJWeaver.breakOnError = breakOnError
                aspectJWeaver.experimental = experimental
                aspectJWeaver.ajcArgs from ajcArgs
            }
        }

        return this
    }

    private fun <T: BaseVariantData> setupVariant(variantData: T) {
        if (variantData.scope.instantRunBuildContext.isInInstantRunMode) {
            if (modeComplex()) {
                throw GradleException(SLICER_DETECTED_ERROR)
            }
        }

        val javaTask = getJavaTask(variantData)
        getAjSourceAndExcludeFromJavac(project, variantData)
        aspectJWeaver.encoding = javaTask!!.options.encoding
        aspectJWeaver.sourceCompatibility = JavaVersion.VERSION_1_7.toString()
        aspectJWeaver.targetCompatibility = JavaVersion.VERSION_1_7.toString()
    }

    /* External API */

    override fun getName(): String {
        return TRANSFORM_NAME
    }

    override fun getInputTypes(): Set<QualifiedContent.ContentType> {
        return Sets.immutableEnumSet(QualifiedContent.DefaultContentType.CLASSES)
    }

    override fun getOutputTypes(): Set<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return if (modeComplex()) TransformManager.SCOPE_FULL_PROJECT else Sets.immutableEnumSet(QualifiedContent.Scope.PROJECT)
    }

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> {
        return if (modeComplex()) super.getReferencedScopes() else TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return false
    }

    @Suppress("OverridingDeprecatedMember")
    override fun transform(context: Context, inputs: Collection<TransformInput>, referencedInputs: Collection<TransformInput>, outputProvider: TransformOutputProvider, isIncremental: Boolean) {
        this.transform(TransformInvocationBuilder(context)
            .addInputs(inputs)
            .addReferencedInputs(referencedInputs)
            .addOutputProvider(outputProvider)
            .setIncrementalMode(isIncremental).build())
    }

    override fun transform(transformInvocation: TransformInvocation) {
        // bypassing transformer for non-test variant data in ConfigScope.TEST
        if (!verifyBypassInTestScope(transformInvocation.context)) {
            // TODO: THX-35249 this bypass does not adhere to the "Transformer Contract"
            // Each transform MUST write out the input it was given.
            // IE. fileA -> Transform -> FileA
            logBypassTransformation()
            return
        }

        val outputProvider = transformInvocation.outputProvider
        val includeJars = config.aspectj().includeJar
        val excludeJars = config.aspectj().excludeJar
        val includeAspects = config.aspectj().includeAspectsFromJar

        if (!transformInvocation.isIncremental) {
            outputProvider.deleteAll()
        }

        val outputDir = outputProvider.getContentLocation(TRANSFORM_NAME, outputTypes, scopes, Format.DIRECTORY)
        if (outputDir.isDirectory) FileUtils.deleteDirectoryContents(outputDir)
        FileUtils.mkdirs(outputDir)

        aspectJWeaver.destinationDir = outputDir.absolutePath
        aspectJWeaver.bootClasspath = config.getBootClasspath().joinToString(separator = File.pathSeparator)

        // clear weaver input, so each transformation can have its own configuration
        // (e.g. different build types / variants)
        // thanks to @philippkumar
        aspectJWeaver.inPath.clear()
        aspectJWeaver.aspectPath.clear()

        logAugmentationStart()

        // attaching source classes compiled by compile${variantName}AspectJ task
        includeCompiledAspects(transformInvocation, outputDir)
        val inputs = if (modeComplex()) transformInvocation.inputs else transformInvocation.referencedInputs

        var hasAj = false
        inputs.forEach proceedInputs@ { input ->
            if (input.directoryInputs.isEmpty() && input.jarInputs.isEmpty())
                return@proceedInputs //if no inputs so nothing to proceed

            input.directoryInputs.forEach { dir ->
                // NOTE: The java doc for `name` is quoted as being unreliable.
                // TODO: THX-35245 Is there another handle to check for AJ runtime being present?
                hasAj = hasAj || dir.name.contains(AJRUNTIME)
                aspectJWeaver.inPath shl dir.file
                aspectJWeaver.classPath shl dir.file

            }
            input.jarInputs.forEach { jar ->
                // NOTE: The java doc for `name` is quoted as being unreliable.
                // TODO: THX-35245 Is there another handle to check for AJ runtime being present?
                hasAj = hasAj || jar.name.contains(AJRUNTIME)

                aspectJWeaver.classPath shl jar.file

                if (modeComplex()) {
                    val includeAllJars = config.aspectj().includeAllJars
                    val includeFilterMatched = includeJars.isNotEmpty() && isIncludeFilterMatched(jar.file, includeJars)
                    val excludeFilterMatched = excludeJars.isNotEmpty() && isExcludeFilterMatched(jar.file, excludeJars)

                    if (excludeFilterMatched) {
                        logJarInpathRemoved(jar)
                    }

                    if (!excludeFilterMatched && (includeAllJars || includeFilterMatched)) {
                        logJarInpathAdded(jar)
                        aspectJWeaver.inPath shl jar.file
                    } else {
                        copyJar(outputProvider, jar)
                    }
                } else {
                    if (includeJars.isNotEmpty() || excludeJars.isNotEmpty()) logIgnoreInpathJars()
                }

                val includeAspectsFilterMatched = includeAspects.isNotEmpty() && isIncludeFilterMatched(jar.file, includeAspects)
                if (includeAspectsFilterMatched) {
                    logJarAspectAdded(jar)
                    aspectJWeaver.aspectPath shl jar.file
                }
            }
        }

        val hasAjRt = hasAj || aspectJWeaver.classPath.any { it.name.contains(AJRUNTIME); }

        if (hasAjRt) {
            logWeaverBuildPolicy(policy)
            aspectJWeaver.doWeave()

            if (modeComplex()) {
                aspectJMerger.doMerge(this, transformInvocation, outputDir)
            }

            logAugmentationFinish()
        } else {
            logEnvInvalid()
            logNoAugmentation()
            // TODO: should we write out the files before throwing exception?
            // TODO: consider adding flag to continue build without throwing fatal exception
            throw GradleException("""Thunderhead AspectJ plugin configured, AspectJ runtime not on classpath.
                | Try clearing gradle cache and build directory.
                | Check IDE is not in offline mode.""".trimMargin())
        }
    }

    private fun modeComplex(): Boolean {
        return policy == BuildPolicy.COMPLEX
    }

    /* Internal */

    private fun verifyBypassInTestScope(ctx: Context): Boolean {
        val variant = (ctx as TransformTask).variantName

        return when (config.scope) {
            ConfigScope.TEST -> variant.contains("androidtest", true)
            else -> true
        }
    }

    private fun includeCompiledAspects(transformInvocation: TransformInvocation, outputDir: File) {
        val compiledAj = project.file("${project.buildDir}/aspectj/${(transformInvocation.context as TransformTask).variantName}")
        if (compiledAj.exists()) {
            aspectJWeaver.aspectPath shl compiledAj

            //copy compiled .class files to output directory
            FileUtil.copyDir(compiledAj, outputDir)
        }
    }

    private fun copyJar(outputProvider: TransformOutputProvider, jarInput: JarInput?): Boolean {
        if (jarInput === null) {
            return false
        }

        var jarName = jarInput.name
        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length - 4)
        }

        val dest: File = outputProvider.getContentLocation(jarName, jarInput.contentTypes, jarInput.scopes, Format.JAR)

        FileUtil.copyFile(jarInput.file, dest)

        return true
    }
}
