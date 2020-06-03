package com.brambolt.gradle.patching

import org.slf4j.Logger
import spock.lang.Specification

import static org.slf4j.LoggerFactory.getLogger

class PatcherLineSpec extends Specification {

  Logger log = getLogger(PatcherFileSpec.class)

  def "can apply to lines"() {
    given:
    def sh = getLineMap('standalone.sh')
    def xml = getLineMap('standalone.xml')
    def patcher = new Patcher(log)
    when:
    sh.actual = patcher.applyOrThrow(sh.patch, sh.input)
    xml.actual = patcher.applyOrThrow(xml.patch, xml.input)
    then:
    null != sh.expected && !sh.expected.isEmpty()
    null != xml.expected && !xml.expected.isEmpty()
    sh.expected.join('\n') == sh.actual.join('\n')
    xml.expected.join('\n') == xml.actual.join('\n')
  }

  Map<String, List<String>> getLineMap(String resourceName) {
    [
      expected: getLines("/expected/${resourceName}"),
      input   : getLines("/content/${resourceName}"),
      patch   : getLines("/patches/${resourceName}.diff")
    ]
  }

  List<String> getLines(String resourcePath) {
    String scanned = new Scanner(getClass().getResourceAsStream(resourcePath))
      .useDelimiter('\\Z')
      .next()
    List<String> lines = scanned.split('\n')
    if (scanned.endsWith('\n')) // Sad
      lines.add('')
    lines
  }
}
