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

package com.brambolt.gradle.patching.tasks

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static com.brambolt.gradle.patching.PatcherFileSpec.getFiles

class ProcessPatchesSpec2 extends Specification {

  @Rule TemporaryFolder testProjectDir = new TemporaryFolder()

  def 'can apply'() {
    given:
    def task = ProjectBuilder.builder().build()
      .task(type: ProcessPatches, 'patch') as ProcessPatches
    def sh = getFiles('standalone.sh', testProjectDir.newFolder('sh'))
    when:
    task.content = sh.input
    task.patch = sh.patch
    task.destination = testProjectDir.newFolder('out')
    task.apply()
    then:
    new File(task.destination as File, sh.input.name).text == sh.expected.text
  }
}
