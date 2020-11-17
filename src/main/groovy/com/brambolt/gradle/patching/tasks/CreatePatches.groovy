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

import com.brambolt.gradle.patching.Differ
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction

import java.nio.charset.Charset

import static com.brambolt.gradle.SpecObjects.asFile
import static com.brambolt.gradle.patching.Patcher.DEFAULT_CHARSET
import static com.brambolt.gradle.patching.Patcher.DEFAULT_DIFF_EXTENSION

/**
 */
class CreatePatches extends DefaultTask {

  /**
   * The extension identifying the patch files. Set this property to a
   * different value like <code>.patch</code> for example, to instruct
   * the patching logic to look for files with an extension other than
   * <code>.patch</code>.
   */
  String diffExtension = DEFAULT_DIFF_EXTENSION

  /**
   * The character set in use, UTF-8 by default.
   */
  Charset charset = DEFAULT_CHARSET

  /**
   * The original content.
   */
  Object content

  /**
   * The modified content.
   */
  Object modified

  /**
   * The directory (not file) to write the generated patches into.
   */
  Object destination

  /**
   * Task configuration.
   *
   * @param closure The configuration closure
   * @return The configured task
   */
  @Override
  Task configure(Closure closure) {
    destination = findDefaultDestinationDir()
    super.configure(closure)
  }

  /**
   * The default destination directory is <code>${buildDir}/patches</code>.
   * @return The default destination directory
   */
  File findDefaultDestinationDir() {
    new File(project.buildDir, 'patches')
  }

  /**
   * Creates the patches.
   */
  @TaskAction
  void apply() {
    File contentDir = asFile('content', content)
    File modifiedDir = asFile('modified', modified)
    File destinationDir = asFile('destination', destination)
    project.logger.info("Generating from ${contentDir} and ${modifiedDir} to ${destinationDir}.")
    List<String> errors = new Differ(project.logger).apply(
      contentDir, modifiedDir, diffExtension, charset, destinationDir)
    if (!errors.isEmpty())
      throw new GradleException("""
Diffing failed with errors:
\t${errors.join(System.getProperty('line.separator') + '\t')}
""")
  }
}


