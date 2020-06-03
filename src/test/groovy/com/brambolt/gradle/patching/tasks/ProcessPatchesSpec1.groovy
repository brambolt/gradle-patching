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
