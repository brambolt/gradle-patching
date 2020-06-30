
A Gradle plugin for creating and applying patch files using the Google `diffutils` library. 

Instructions for applying the plugin are at 

  https://plugins.gradle.org/plugin/com.brambolt.gradle.patching

The easiest way to use the plugin is to configure the `processPatches` task with 
directories to locate patches and the content to apply them to, for example:
```
plugins {
  id 'application'
  id 'com.brambolt.gradle.patching' version 'SNAPSHOT'
}

processPatches {
  content = "${project.projectDir}/src/vendor/resources"
  patches = "${project.projectDir}/src/main/diffs"
  destination = processResources.destinationDir
}
```
