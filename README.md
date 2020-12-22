
A Gradle plugin for creating and applying patch files using Google's 
`diffutils` library. 

Instructions for applying the plugin are at 

  https://plugins.gradle.org/plugin/com.brambolt.gradle.patching

A repository declaration for Bintray is also needed, in a snippet like this in 
the project's `settings.gradle` (where `bramboltVersion` is the version in use):

```
pluginManagement {
  repositories {
    gradlePluginPortal()
    maven { url 'https://dl.bintray.com/brambolt/public' }
  }
  plugins {
    id 'com.brambolt.gradle.patching' version bramboltVersion
  }
}
```

The easiest way to use the plugin is to configure the `processPatches` task with 
directories to locate patches and the content to apply them to, for example:
```
plugins {
  id 'application'
  id 'com.brambolt.gradle.patching' version 'SNAPSHOT'
}

processPatches {
  content = "${projectDir}/src/vendor/resources"
  patches = "${projectDir}/src/main/diffs"
  destination = processResources.destinationDir
}
```

This will apply the patches in `src/main/diffs` to the content in 
`src/vendor/resources` and write the output to 
`processResources.destinationDir`, which is usually 
`${buildDir}/resources/main`. 

The plugin can also generate the patch files:
```
plugins {
  id 'application'
  id 'com.brambolt.gradle.patching' version 'SNAPSHOT'
}

createPatches {
  content = "${projectDir}/src/vendor/resources"
  modified = "${buildDir}/modified"
  destination = "${buildDir}/patches"
}
```

This generates the patch files to convert the content in `src/vendor/resources`
into the modified content in `${buildDir}/modified`. 

The plugin adds the `createPatches` and `processPatches` tasks to the project 
when it is applied. It adds a dependency from the `processResources` task on 
the `processPatches` task if the `processResources` task is available. 
The `createPatches` task must be explicitly added to a dependency chain or can 
be (and usually is) invoked directly, to repair the patches.

The following simple workflow can be useful when new versions of third party 
resources frequently become available, but local modifications must also be 
maintained onsite:

1. Start by creating a local project and apply the patching plugin
2. Copy the third party resources into `${buildDir}/modified`
3. Edit the copied resources until all local changes have been applied
4. Use the `createPatches` task to generate the diff files in `${buildDir}/patches`
5. When satisfied, move the patches to `src/main/diffs`

Don't include the `createPatches` task in any dependency chains. For this 
workflow, it is only invoked manually as described above. Then, assuming that 
the `java` or `application` plugins are used,  

6. Configure `processPatches` as a dependency of `processResources`
7. Set `processPatches.content` to point to the third party sources

The patches will now be processed during every build. If desired, it is 
simple and easy to replace the file system copy of the third party sources with 
a Maven dependency, unzip that archive into the build directory, and use that 
location for `processPatches.content`. 

The third party resources can now be updated as required and as long as the 
patches continue to be valid the build will succeed. When a third party update 
invalidates a patch, the following steps repair the build:

8. Remove the invalid patches from `src/main/diffs`
9. Run the `processPatches` task on the remaining diffs
10. Reapply the changes from the removed patches by hand
11. Use the `createPatches` task to recreate the removed patches
12. Insert the recreated patches back into `src/main/diffs`

There are many other ways to deal with this problem - checking the third party 
resources into source control and merging is probably the most prominent one. 
If the third party update process tends to break every patch then the source 
control approach works well, but if the breaks are rare then this build-based 
approach may be a good substitute. 