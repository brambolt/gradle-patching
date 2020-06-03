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
