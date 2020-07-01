/*
 * Copyright 2017-2020 Brambolt ehf.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.brambolt.gradle

import groovy.io.FileType
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.nio.file.Path

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

import static com.brambolt.gradle.PatchingPluginFixture1.applyFalse
import static com.brambolt.gradle.PatchingPluginFixture1.applyOnly
import static com.brambolt.gradle.PatchingPluginFixture1.contentFile
import static com.brambolt.gradle.PatchingPluginFixture1.patchFile
import static com.brambolt.gradle.PatchingPluginFixture1.patchedFile
import static com.brambolt.gradle.PatchingPluginFixture1.withDirectories
import static com.brambolt.gradle.PatchingPluginFixture1.withPatch
import static com.brambolt.gradle.testkit.Builds.runTask

class PatchingPluginSpec1 extends Specification {

  @Rule TemporaryFolder testProjectDir = new TemporaryFolder()

  Map buildFiles

  def setup() {
    buildFiles = [
      applyFalse: applyFalse(testProjectDir),
      applyOnly: applyOnly(testProjectDir),
      withDirectories: withDirectories(testProjectDir),
      withPatch: withPatch(testProjectDir)
    ]
  }

  def 'can include plugin without applying'() {
    when:
    def result = runTask(testProjectDir.root,
      '-b', buildFiles.applyFalse.name as String,
      'tasks', '--debug', '--stacktrace')
    then:
    result.task(":tasks").outcome == SUCCESS
  }

  def 'can apply plugin'() {
    when:
    def result = runTask(testProjectDir.root,
      '-b', buildFiles.applyOnly.name as String, 'tasks')
    then:
    result.task(":tasks").outcome == SUCCESS
  }

  def 'can execute patch task on a pair of files'() {
    given:
    File contentFile = contentFile(testProjectDir) // Creates the file
    patchFile(testProjectDir) // Creates...
    File patchedFile = patchedFile(testProjectDir) // Creates...
    def contentBeforePatching = contentFile.text
    def contentExpected = patchedFile.text
    when:
    def result = runTask(testProjectDir.root,
      '-b', buildFiles.withPatch.name as String, 'processPatches')
    def contentAfterPatching = contentFile.text
    then:
    // Check that the task execution succeeded according to Gradle:
    result.task(':processPatches').outcome == SUCCESS
    // Check that the text file now matches the expectation:
    contentAfterPatching == contentExpected
    // Double-check that we actually did something:
    contentBeforePatching != contentExpected
  }

  def 'can execute patching task on empty directory hierarchies'() {
    given:
    File contentDir = testProjectDir.newFolder('content')
    File expectedDir = testProjectDir.newFolder('expected')
    testProjectDir.newFolder('patches')
    when:
    def result = runTask(testProjectDir.root,
      '-b', buildFiles.withDirectories.name as String, 'processPatches')
    then:
    result.task(':processPatches').outcome == SUCCESS
    Path contentRootPath = contentDir.toPath()
    contentDir.eachFileRecurse(FileType.FILES) { File contentFile ->
      Path relative = contentRootPath.relativize(contentFile.toPath())
      File expectedFile = new File(expectedDir, relative.toString())
      contentFile.text == expectedFile.text
    }
  }
}