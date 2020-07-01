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

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.brambolt.gradle.PatchingPluginFixture1.withDirectories
import static com.brambolt.gradle.testkit.Builds.runTask
import static com.brambolt.gradle.testkit.Fixtures.createDirectoryFixture
import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

class PatchingPluginSpec2 extends Specification {

  @Rule TemporaryFolder testProjectDir = new TemporaryFolder()

  Map buildFiles

  Map fixtures

  def setup() {
    buildFiles = [ withDirectories: withDirectories(testProjectDir) ]
    fixtures = [ fixture2: createDirectoryFixture('fixture2.zip', testProjectDir) ]
  }

  def 'can execute patching task on matching directory hierarchies'() {
    given:
    File actualDir = new File(fixtures.fixture2 as File, 'actual')
    File expectedDir = new File(fixtures.fixture2 as File, 'expected')
    def relpaths = [
      sh: 'bin/standalone.sh',
      xml: 'standalone/configuration/standalone.xml'
    ]
    def actual = [
      sh: new File(actualDir, relpaths.sh),
      xml: new File(actualDir, relpaths.xml)
    ]
    def expected = [
      sh: new File(expectedDir, relpaths.sh),
      xml: new File(expectedDir, relpaths.xml)
    ]
    when:
    def buildResult = runTask(testProjectDir.root,
      '-b', buildFiles.withDirectories.name as String, 'processPatches')
    then:
    buildResult.task(':processPatches').outcome == SUCCESS
    // Trim, because after patching a newline precedes EOF:
    actual.sh.text.trim() == expected.sh.text.trim()
    actual.xml.text.trim() == expected.xml.text.trim()
  }
}