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
