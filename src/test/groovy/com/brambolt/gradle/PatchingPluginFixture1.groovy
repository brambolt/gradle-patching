package com.brambolt.gradle

import org.junit.rules.TemporaryFolder

import static com.brambolt.gradle.testkit.Builds.createBuildFile

class PatchingPluginFixture1 {

  static applyFalse(TemporaryFolder testProjectDir) {
    createBuildFile('build-no-apply.gradle', """
plugins {
  id 'com.brambolt.gradle.patching' apply false
}
""", testProjectDir)
  }

  static applyOnly(TemporaryFolder testProjectDir) {
    createBuildFile('build-apply-only.gradle', """
plugins {
  id 'com.brambolt.gradle.patching' 
}
""", testProjectDir)
  }

  static ithDirectories(TemporaryFolder testProjectDir) {
    createBuildFile('build-with-directories.gradle', """
plugins {
  id 'com.brambolt.gradle.patching'
}

processPatches {
  content = '${new File(testProjectDir.root, 'content').absolutePath}'
  patches = '${new File(testProjectDir.root, 'patches').absolutePath}'
  destination = '${new File(testProjectDir.root, 'actual').absolutePath}'}
""", testProjectDir)
  }

  static withPatch(TemporaryFolder testProjectDir) {
    createBuildFile('build-with-patch.gradle', """
plugins {
  id 'com.brambolt.gradle.patching'
}

processPatches {
  content = '${testProjectDir.root.absolutePath}/file.txt'
  patches = '${testProjectDir.root.absolutePath}/file.patch'
  destination = '${testProjectDir.root.absolutePath}'
}
""",testProjectDir)
  }

  static File contentFile(TemporaryFolder testProjectDir) {
    File file = testProjectDir.newFile('file.txt')
    file.text = """Just
a
text
file.
"""
    file
  }

  static File patchFile(TemporaryFolder testProjectDir) {
    File file = testProjectDir.newFile('file.patch')
    file.text = """--- file.txt    2020-05-26 16:09:53.886129195 +0200
+++ patched.txt 2020-05-26 16:10:09.070128232 +0200
@@ -1,4 +1,5 @@
 Just
 a
+patched
 text
 file.
"""
    file
  }

  static File patchedFile(TemporaryFolder testProjectDir) {
    File file = testProjectDir.newFile('patched.txt')
    file.text = """Just
a
patched
text
file.
"""
  file
  }
}
