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

package com.archinamon.utils

import java.io.File
import javax.xml.bind.JAXBContext
import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement

internal fun findPackageNameIfAar(input: File): String {
    if (!input.absolutePath.contains("build-cache")) return input.absolutePath
    if (!input.exists()) return "[empty]"

    var f: File? = input

    do {
        f = f?.parentFile
    } while (f?.isDirectory!! && !f.listFiles().any(::findManifest))

    val manifest = f.listFiles().find(::findManifest)
    if (manifest != null) {
        val xml = readXml(manifest, Manifest::class.java)
        return xml.libPackage
    }

    return input.name
}

private fun findManifest(f: File): Boolean {
    return f.name.equals("androidmanifest.xml", true)
}

private inline fun <reified T> readXml(file: File, clazz: Class<T>): T {
    val jc = JAXBContext.newInstance(clazz)
    val unmarshaller = jc.createUnmarshaller()
    val data = unmarshaller.unmarshal(file) ?: error("Marshalling failed. Get null object")
    return data as T
}

@XmlRootElement(name = "manifest")
@XmlAccessorType(XmlAccessType.FIELD)
internal class Manifest {

    @XmlAttribute(name = "package")
    lateinit var libPackage: String
}