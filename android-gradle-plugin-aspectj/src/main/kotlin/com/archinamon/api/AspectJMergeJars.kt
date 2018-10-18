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

package com.archinamon.api

import com.android.build.api.transform.Format
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.builder.packaging.JarMerger
import com.android.builder.packaging.ZipEntryFilter
import com.android.utils.FileUtils
import com.archinamon.api.transform.AspectJTransform
import java.io.File

/**
 * Merging all jars and aars in project with dependencies
 * This runs when AspectJ augments jar's/aar's bytecode
 *
 * @author archinamon on 13/03/17.
 */
internal class AspectJMergeJars {

    private val target = com.archinamon.api.transform.TRANSFORM_NAME

    internal fun doMerge(transform: AspectJTransform, context: TransformInvocation, resultDir: File) {
        if (resultDir.listFiles().isNotEmpty()) {
            val jarFile = context.outputProvider.getContentLocation(target, transform.outputTypes, transform.scopes, Format.JAR)
            FileUtils.mkdirs(jarFile.parentFile)
            FileUtils.deleteIfExists(jarFile)

            val jarMerger = JarMerger(jarFile.toPath(), ZipEntryFilter.CLASSES_ONLY)
            try {
                jarMerger.addDirectory(resultDir.toPath())
            } catch (e: Exception) {
                throw TransformException(e)
            } finally {
                jarMerger.close()
            }
        }

        FileUtils.deletePath(resultDir)
    }
}