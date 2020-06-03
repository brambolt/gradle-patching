package com.brambolt.gradle.patching

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.slf4j.Logger
import spock.lang.Specification

import static org.slf4j.LoggerFactory.getLogger

class PatcherFileSpec extends Specification {

  @Rule TemporaryFolder testProjectDir = new TemporaryFolder()

  Logger log = getLogger(PatcherFileSpec.class)

  def "can apply to files"() {
    given:
    def sh = getFiles('standalone.sh', testProjectDir.newFolder('sh'))
    def xml = getFiles('standalone.xml', testProjectDir.newFolder('xml'))
    def patcher = new Patcher(log)
    when:
    sh.actual = patcher.applyOrThrow(sh.patch, sh.input, sh.actual)
    xml.actual = patcher.applyOrThrow(xml.patch, xml.input, xml.actual)
    then:
    null != sh.expected && sh.expected.exists() && !sh.expected.text.isEmpty()
    null != xml.expected && xml.expected.exists() && !xml.expected.text.isEmpty()
    sh.expected.text.join('\n') == sh.actual.text.join('\n')
    xml.expected.text.join('\n') == xml.actual.text.join('\n')
  }

  static Map<String, File> getFiles(String resourceName, File dir) {
    [
      actual:   new File(dir, 'a' + resourceName),
      expected: getFile("/expected/${resourceName}", new File(dir, 'x' + resourceName)),
      input   : getFile("/content/${resourceName}", new File(dir, 'i' + resourceName)),
      patch   : getFile("/patches/${resourceName}.diff", new File(dir, 'p' + resourceName))
    ]
  }

  static File getFile(String resourcePath, File file) {
    URL resource = PatcherFileSpec.class.getResource(resourcePath)
    file.text = new File(resource.file).text
    file
  }
}
