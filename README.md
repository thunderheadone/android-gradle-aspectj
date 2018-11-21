![Thunderhead Android Gradle AspectJ](https://www.thunderhead.com/uploads/2015/07/Thunderhead_LogoIcon_Aubergine.png "Thunderhead")

# Thunderhead Android Gradle Plugin for AspectJ

[![Kotlin](https://img.shields.io/badge/Kotlin-1.1.51-blue.svg)](http://kotlinlang.org) 
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0)
[![Download](https://api.bintray.com/packages/thunderheadone/Android/android-gradle-plugin-aspectj/images/download.svg) ](https://bintray.com/thunderheadone/Android/android-gradle-plugin-aspectj/_latestVersion) 

A Gradle plugin which adds the AspectJ toolchain to an Android build.
Write code with the AspectJ language in `.aj` files and/or using java annotations.

Supported Android Gradle Plugin Version: `3.0.1`

## Installation
1. Update your **top-level** `build.gradle` to include the plugin in the build.
+ Update the `buildscript` `repositories` to include jcenter if it is not present 
and add a `classpath` dependency on the plugin as shown in the following example:
``` groovy
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.0.1'
        classpath 'com.thunderhead.android:android-gradle-plugin-aspectj:4.0.1'
    }
}
```
+ Update the `allprojects` `repositories` closure to include `mavenCentral` as shown in the following example:
``` groovy
allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}
```
+ Example of the **top-level** `build.gradle` file after integration:
``` gradle
buildscript {
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.0.1'
        classpath 'com.thunderhead.android:android-gradle-plugin-aspectj:4.0.1'
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}
```
2. Update your **app-level** `build.gradle` to apply the plugin
+ `apply plugin: 'com.thunderhead.android.aspectj'`


Now you can write aspects using the annotation style or native AspectJ-lang AJ Syntax (even without IntelliJ IDEA Ultimate edition).

## Plugin Configuration Options

```groovy
aspectj {
    ajc '1.8.12' // default value

    // @see Ext plugin config
    includeAllJars false // default value
    includeJar 'design', 'support-v4', 'dagger' // default is empty
    excludeJar 'support-v7', 'joda' // default is empty
    extendClasspath true // default value

    includeAspectsFromJar 'my-aj-logger-lib', 'any-other-libs-with-aspects'  // default is empty
    ajcArgs << '-referenceInfo' << '-warn:deprecation'

    weaveInfo true // default value
    debugInfo false // default value
    addSerialVersionUID false // default value
    noInlineAround false // default value
    ignoreErrors false // default value
    
    breakOnError true // default value
    experimental false // default value
    buildTimeLog true // default value

    transformLogFile 'ajc-transform.log' // default value
    compilationLogFile 'ajc-compile.log' // default value
}
```
Note that you may not include all these options!

All the extension parameters have default values (all of them are described above, except for includeJar/Aspects/ajcArgs options)
and are not needed to be defined manually.

+ `ajc` Sets the aspectj runtime jar version manually (1.8.12 current).
+ `extendClasspath` Mutates the classpath with the aspectj-runtime itself if set to true(default).

+ `includeAllJars` Includes all available jar-files in the -inpath argument provided to the aspectj compiler.
+ `includeJar` Name filter to include any jar/aar which name or path satisfies the filter.
+ `excludeJar` Name filter to exclude any jar/aar which name or path satisfies the filter.
+ `includeAspectsFromJar` Name filter to include any jar/aar with compiled binary aspects you want to be woven into your project.
+ `ajcExtraArgs` Additional parameters to be sent to the aspectj compiler.

+ `weaveInfo` Prints info messages from the aspectj compiler when set to true(default).
+ `debugInfo` Prints debug info in aspect's bytecode when set to true(default false).
+ `addSerialVersionUID` Adds serialVersionUID field for Serializable-implemented aspect classes when set to true(default false).
+ `noInlineAround` Forces the aspectj compiler to inline `around` advice into the target methods when set to true(default false).
+ `ignoreErrors` Allows the compiler to continue if errors occur while processing the sources when set to true(default false).

+ `breakOnError` Prevents the build from continuing if the aspectj compiler fails or throws any errors if set to true(default).
+ `experimental` Enables experimental aspectj compiler options: `-XhasMember` and `-Xjoinpoints:synchronization,arrayconstruction`. 
More details in <a href="https://github.com/Archinamon/GradleAspectJ-Android/issues/18" target="_blank">Base Repository #18</a>.

+ `buildTimeLog` Appends a BuildTimeListener to the current module that prints the time spent for every task in the build flow.
 Logged in milliseconds.

+ `transformLogFile` Defines the name for the log file where all aspectj compiler info is written to. 
Writes out during the transform portion of the build.
+ `compilationLogFile` Defines the name for the log file where all aspectj compiler info is written to.
Writes out during the CompileTask portion of the build.

## Extended Plugin Configuration

Apply the extended plugin instead of the default plugin.
```groovy
apply plugin: 'com.thunderhead.android.aspectj-ext'
```

Use this plugin when using the `includeJar` and `includeAllJars` configurations or when working in a multidex application.
+ `InstantRun`(IR) must be switched off (The plugin detects IR status and fails the build if IR is switched on).

## Provider Plugin Configuration

Apply the provider plugin instead of the default plugin.
```groovy
apply plugin: 'com.thunderhead.android.aspectj-provides'
```

Use this plugin for cases when you need to extract aspect-sources into a separate module and include it on demand to the modules where you need it.
Therefore this plugin will save build-time due to bypassing the aspectj-transformers in provide-only modules.

You are not limited in the amount of provider modules, you can have as many as you need 
and then include them using `includeAspectsFromJar` parameter in the module which you want to augment.


## Test Plugin Configuration

Apply the test plugin instead of the default plugin.
```groovy
apply plugin: 'com.thunderhead.android.aspectj-test'
```

This plugin configuration inherits the `aspectj-ext` behavior with strictly excluding compile and transform tasks from non-test build variants.
In other words only instrumentation `androidTest` will work with this sub-plugin.
This won't affect `unitTest` variants.

## ProGuard

Correct tuning will depend on your own usage of aspect classes. 
If you declare inter-type injections you'll have to predict side-effects 
and define your annotations/interfaces which you inject into java classes/methods/etc. in your proguard config.

Basic rules you'll need to declare for your project:
```
-adaptclassstrings
-keepattributes InnerClasses, EnclosingMethod, Signature, *Annotation*

-keepnames @org.aspectj.lang.annotation.Aspect class * {
    ajc* <methods>;
}
```

If you face problems with lambda factories, you may need to explicitly suppress them. 
That could happen not just in aspect classes but in any arbitrary java-class if you're using Retrolambda.
To avoid this use this concrete rule:
```
-keep class *$Lambda* { <methods>; }
-keepclassmembernames public class * {
    *** lambda*(...);
}
```

## License

    Copyright 2015 Eduard "Archinamon" Matsukov.
    Copyright 2018 Thunderhead

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
