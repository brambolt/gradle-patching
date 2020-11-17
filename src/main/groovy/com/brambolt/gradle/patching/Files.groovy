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

import org.apache.tools.ant.taskdefs.Copy
import org.slf4j.Logger

/**
 * Utilities.
 */
class Files {

  static void copy(File file, File toFile, Logger log) {
    Copy copy = new Copy()
    copy.setFile(file)
    copy.setTofile(toFile)
    copy.execute()
    log.debug("Copied ${file} to ${toFile}.")
  }
}
