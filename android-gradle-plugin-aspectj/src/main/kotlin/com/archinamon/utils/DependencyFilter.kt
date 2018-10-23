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

/**
 * Jar/aar filter that able to search within gradle cache for asked package name in aar's manifests
 *
 * @author archinamon on 18/03/17.
 */

internal object DependencyFilter {

    private enum class Policy {
        INCLUDE,
        EXCLUDE
    }

    internal fun isExcludeFilterMatched(file: File?, filters: Collection<String>?): Boolean {
        return isFilterMatched(file, filters, Policy.EXCLUDE)
    }

    internal fun isIncludeFilterMatched(file: File?, filters: Collection<String>?): Boolean {
        return isFilterMatched(file, filters, Policy.INCLUDE)
    }

    private fun isFilterMatched(file: File?, filters: Collection<String>?, filterPolicy: Policy): Boolean {
        if (file === null) {
            return false
        }

        if (filters === null || filters.isEmpty()) {
            return filterPolicy === Policy.INCLUDE
        }

        val str = findPackageNameIfAar(file)
        return filters.any { isContained(str, it) }
    }

    private fun isContained(str: String?, filter: String): Boolean {
        if (str === null) {
            return false
        }

        return when {
            str.contains(filter) -> true
            filter.contains("/") -> str.contains(filter.replace("/", File.separator))
            filter.contains("\\") -> str.contains(filter.replace("\\", File.separator))
            else -> false
        }
    }
}