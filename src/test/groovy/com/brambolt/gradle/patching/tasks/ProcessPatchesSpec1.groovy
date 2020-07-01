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
import spock.lang.Specification

import static com.brambolt.gradle.SpecObjects.asFile

class ProcessPatchesSpec1 extends Specification {

  def 'can define task with patch path'() {
    given:
    def task = ProjectBuilder.builder().build().task(type: ProcessPatches, 'patch')
    when:
    task.patch = "${System.getProperty('java.tmp.dir')}/file.patch"
    then:
    asFile('patch', task.patch) instanceof File
  }

  def 'can define task with patch file'() {
    given:
    def task = ProjectBuilder.builder().build().task(type: ProcessPatches, 'patch')
    when:
    task.patch = new File("${System.getProperty('java.tmp.dir')}/file.patch")
    then:
    asFile('patch', task.patch) == task.patch
  }

  def 'can define task with patch closure'() {
    given:
    def task = ProjectBuilder.builder().build().task(type: ProcessPatches, 'patch')
    when:
    task.patch = { "${System.getProperty('java.tmp.dir')}/file.patch" }
    then:
    asFile('patch', task.patch) instanceof File
  }
}
