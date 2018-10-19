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

package com.archinamon

open class AspectJExtension {

    open var ajc = "1.8.12"

    open var includeAllJars = false
    open var includeJar = mutableSetOf<String>()
    open var excludeJar = mutableSetOf<String>()
    open var extendClasspath = true

    open var includeAspectsFromJar = mutableSetOf<String>()
    open var ajcArgs = mutableSetOf<String>()

    open var weaveInfo = true
    open var debugInfo = false

    open var addSerialVersionUID = false
    open var noInlineAround = false

    open var ignoreErrors = false
    open var breakOnError = true

    open var experimental = false
    open var buildTimeLog = true

    open var transformLogFile = "ajc-transform.log"
    open var compilationLogFile = "ajc-compile.log"

    fun ajcArgs(vararg args: String): AspectJExtension {
        ajcArgs.addAll(args)
        return this
    }

    fun includeJar(vararg filters: String): AspectJExtension {
        includeJar.addAll(filters)
        return this
    }

    fun excludeJar(vararg filters: String): AspectJExtension {
        excludeJar.addAll(filters)
        return this
    }

    fun includeAspectsFromJar(vararg filters: String): AspectJExtension {
        includeAspectsFromJar.addAll(filters)
        return this
    }
}
