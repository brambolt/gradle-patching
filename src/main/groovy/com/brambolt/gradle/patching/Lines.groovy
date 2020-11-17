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

import java.nio.charset.Charset

/**
 * Utilities.
 */
class Lines {

  /**
   * Returns the lines of the file at the input path.
   * @param inputPath The input path to read from
   * @return The lines of the input file
   * @throws IOException If unable to read the file
   */
  static List<String> getLines(String inputPath) {
    getLines(new File(inputPath))
  }

  /**
   * Returns the lines of the parameter file.
   * @param inputFile The input file to read from
   * @return The lines of the input file
   * @throws IOException If unable to read the file
   */
  static List<String> getLines(File inputFile) {
    List<String> result = new ArrayList<>()
    inputFile.eachLine { String line -> result.add(line) }
    result
  }

  static File writeLines(List<String> patchedLines, String outputPath, Charset charset) {
    writeLines(patchedLines, new File(outputPath), charset)
  }

  static File writeLines(List<String> patchedLines, File outputFile, Charset charset) {
    outputFile.withWriter(charset.name()) { writer ->
      patchedLines.each { String line -> writer.writeLine(line) }
    }
    outputFile
  }
}
