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

import com.brambolt.gradle.patching.Patcher
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction
import org.gradle.language.jvm.tasks.ProcessResources

import java.nio.charset.Charset

import static com.brambolt.gradle.SpecObjects.asFile
import static com.brambolt.gradle.patching.Patcher.DEFAULT_CHARSET
import static com.brambolt.gradle.patching.Patcher.DEFAULT_DIFF_EXTENSION

/**
 * <p>Applies the unified diff patches found at <code>patches</code> and
 * ending with the <code>diffExtension</code> (<code>.diff</code> by default)
 * to the content at the <code>content</code> path and writes the results
 * to the <code>destination</code> path.</p>
 *
 * <p>The patch path can identify a file, in which case the content path must
 * also identify a file and the <code>diffExtension</code> is ignored, or a
 * directory in which case the content path must also identify a directory.
 * If these paths identify files then the result will be a file with the same
 * name as the content file, placed in the destination directory.</p>

 * <p>If patches and content are directories then the task will look for all
 * patches with the <code>diffExtension</code> under the source directory, and
 * apply the patches to files with the identical relative paths under the content
 * directory.</p>
 *
 * <p>In other words, the patches must be named the same as the content
 * files, with the diff extension added. These match and produce the result:</p>
 * <pre>
 *     ${patches}/a/b/c/d/e.txt.diff
 *     ${content}/a/b/c/d/e.txt
 *     ${destination}/a/b/c/d/e.txt
 * </pre>
 *
 * <p>When the patch task is applied with the structure given in the
 * example above, the unified diff patch in <code>e.txt.diff</code>
 * is going to be applied to <code>e.txt</code>, and this generates the
 * file with the same name in the destination directory.</p>
 *
 * The diff utils library works on a line-by-line basis. The task therefore
 * has to split the input files on newlines and recombine after patch processing.
 * There is no attempt to produce exactly the same line endings. In particular,
 * the output files will include always newlines before EOF, irrespective of
 * whether the input data does or not.
 */
class ProcessPatches extends DefaultTask {

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
   * The content to apply the patch(es) to. Can be a string path, a file
   * object or a closure producing either.
   */
  Object content

  /**
   * The patch(es) to apply. Can be a string path, a file
   * object or a closure producting either.
   */
  Object patches

  /**
   * The patch to apply - syntactic sugar for using with single patches.
   * Use either <code>patch</code> or <code>patches</code>, it does not matter
   * which, if both are used then <code>patch</code> will be ignored.
   */
  Object patch

  /**
   * The directory (not file) to write the patched content into.
   */
  Object destination

  /**
   * Task configuration. If the <code>processResources</code> task is available
   * then its destination directory is set as the default destination directory.
   * @param closure The configuration closure
   * @return The configured task
   */
  @Override
  Task configure(Closure closure) {
    destination = findDefaultDestinationDir()
    super.configure(closure)
  }

  /**
   * Looks up the destination directory of the <code>processResources</code>
   * task, if available, or returns null.
   * @return The destination directory of the process-resource task, or null
   */
  File findDefaultDestinationDir() {
    try {
      Task t = project.tasks.withType(ProcessResources).named('processResources').getOrElse(null)
      (null != t) ? (t as ProcessResources).destinationDir : null
    } catch (Exception x) {
      project.logger.debug('Unable to look up default destination directory', x)
      null // Best we can do
    }
  }

  /**
   * Applies the patches to the content.
   */
  @TaskAction
  void apply() {
    if (null == patches)
      patches = patch
    File patchFile = asFile('patches', patches)
    File contentFile = asFile('content', content)
    File destinationDir = asFile('destination', destination)
    project.logger.info("Applying ${patchFile} to ${contentFile}.")
    List<String> errors = new Patcher(project.logger).apply(
      patchFile, contentFile, diffExtension, charset, destinationDir)
    if (!errors.isEmpty())
      throw new GradleException("""
Patching failed with errors:
\t${errors.join(System.getProperty('line.separator') + '\t')}
""")
  }
}


