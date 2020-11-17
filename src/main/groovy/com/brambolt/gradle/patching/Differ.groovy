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

import difflib.DiffUtils
import difflib.Patch
import groovy.io.FileType
import org.gradle.api.GradleException
import org.slf4j.Logger

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path

import static com.brambolt.gradle.patching.Patcher.getDEFAULT_CHARSET
import static com.brambolt.gradle.patching.Patcher.getDEFAULT_DIFF_EXTENSION

import static Lines.getLines
import static Lines.writeLines

/**
 * @see com.brambolt.gradle.patching.tasks.CreatePatches
 */
class Differ {

  static final int DEFAULT_CONTEXT_SIZE = 2

  final int contextSize

  final Logger log

  Differ(Logger log) {
    this(DEFAULT_CONTEXT_SIZE, log)
  }

  Differ(int contextSize, Logger log) {
    this.contextSize = contextSize
    this.log = log
    if (0 > contextSize)
      throw new IllegalArgumentException('Negative context size')
    if (null == log)
      throw new IllegalArgumentException('Null logger')
  }

  /**
   * Applies the parameter patch(es) to the parameter file(s).
   * @param patch The patch file to apply
   * @param content The content to apply the patch to
   * @param destination The destination to write to
   */
  void apply(File patch, File content, File destination) {
    apply(patch, content, DEFAULT_DIFF_EXTENSION, DEFAULT_CHARSET, destination)
  }

  /**
   * Creates one or more patches from one or more file pairs.
   *
   * @param contentFileOrDir The original content
   * @param modifiedFileOrDir The modified content
   * @param diffExtension The diff file extension
   * @param charset The character set in use, defaults to UTF-8
   * @param destinatinDir The directory to write to
   * @return A list of errors, or an empty list if patching succeeded
   */
  List<String> apply(
    File contentFileOrDir, File modifiedFileOrDir, String diffExtension,
    Charset charset, File destinationDir) {
    if (null == contentFileOrDir)
      throw new GradleException('contentFileOrDir parameter is null')
    if (null == modifiedFileOrDir)
      throw new GradleException('modifiedFileOrDir parameter is null')
    if (!contentFileOrDir.exists() || !modifiedFileOrDir.exists())
      // No original or no modified content - nothing to do and no errors?
      return []
    if (!destinationDir.exists())
      destinationDir.mkdirs()
    switch (contentFileOrDir) {
      case { it.isDirectory() }:
        return applyDirectory(
          contentFileOrDir, modifiedFileOrDir, diffExtension, charset, destinationDir)
      case { it.isFile() }:
        File destinationFile = new File(destinationDir, modifiedFileOrDir.name)
        return applyFile(contentFileOrDir, modifiedFileOrDir, charset, destinationFile, [])
      default:
        throw new UnsupportedOperationException(
          "Not a file or a directory: ${contentFileOrDir}"
        )
    }
  }

  /**
   * Creates a directory of patches from directories of content and modifications.
   * @param contentDir A directory containing original content
   * @param modifiedDir A directory containing modifications
   * @param diffExtension The file extension that identifies the patch files
   * @param charset The character set in use, defaults to UTF-8
   * @param destinationDir The directory to write to
   * @return A list of errors, empty if patching succeeded
   */
  List<String> applyDirectory(
    File contentDir, File modifiedDir, String diffExtension, Charset charset,
    File destinationDir) {
    if (null == contentDir || !contentDir.isDirectory())
      throw new GradleException("Not a valid content directory: ${contentDir}")
    if (null == modifiedDir || !modifiedDir.isDirectory())
      throw new GradleException("Not a valid modifications directory: ${contentDir}")
    if (null == destinationDir || !destinationDir.isDirectory())
      throw new GradleException("Not a valid destination directory: ${destinationDir}")
    if (!diffExtension.startsWith('.'))
      throw new GradleException(
        "The diff file extension should start with a preceding '.' (dot), got instead: ${diffExtension}")
    log.info("Generating patches from ${contentDir} and ${modifiedDir}.")
    List<String> errors = []
    Path rootPath = contentDir.toPath()
    contentDir.eachFileRecurse(FileType.FILES) { File contentFile ->
      errors = visit(modifiedDir, rootPath, contentFile, diffExtension, charset, destinationDir, errors)
    }
    errors
  }

  private List<String> visit(
    File modifiedDir, Path rootContentPath, File contentFile, String diffExtension,
    Charset charset, File destinationDir, List<String> errors) {
    log.debug("Visiting ${contentFile}...")
    Path relDirPath = rootContentPath.relativize(contentFile.parentFile.toPath())
    File matchingModifiedFile = new File(
      modifiedDir, "${relDirPath.toString()}/${contentFile.name}")
    File matchingDestinationFile = new File(
      destinationDir, "${relDirPath.toString()}/${contentFile.name}${diffExtension}")
    matchingDestinationFile.parentFile.mkdirs()
    if (matchingModifiedFile.exists())
      errors = applyFile(
        matchingModifiedFile, contentFile, charset, matchingDestinationFile, errors)
      // Else no patch to generate, nothing to do
    errors
  }

  /**
   * Generates a patch file from a content file and a modified file.
   * @param modifiedFile A modified file
   * @param contentFile A content file
   * @param charset The character set in use, defaults to UTF-8
   * @param destinationFile The file to write the patch to
   * @param errors An error list to add to if patching fails
   * @return The parameter list of errors, unmodified if diffing succeeded
   */
  List<String> applyFile(
    File modifiedFile, File contentFile, Charset charset,
    File destinationFile, List<String> errors) {
    try {
      applyOrThrow(modifiedFile, contentFile, charset, destinationFile)
    } catch (Exception x) {
      errors.add(
        """Diffing exception:
    Content file:  ${contentFile.absolutePath}
    Modified file: ${modifiedFile.absolutePath}
    Exception:     ${x.getMessage()}
""")
    }
    errors
  }

  /**
   * Creates a patch file from a content file and a modified file.
   * @param modifiedFile A modified file
   * @param contentFile A content file
   * @param destinationFile The file to write the patch to
   * @return The destination file
   */
  File applyOrThrow(
    File modifiedFile, File contentFile, File destinationFile) {
    applyOrThrow(modifiedFile, contentFile, StandardCharsets.UTF_8, destinationFile)
  }

  /**
   * Creates a patch file from a content file and a modified file.
   * @param modifiedFile A modified file
   * @param contentFile A content file
   * @param charset The character set in use, defaults to UTF-8
   * @param destinationFile The output file to write to
   * @return The destination file
   */
  File applyOrThrow(
    File modifiedFile, File contentFile, Charset charset, File destinationFile) {
    if (null == modifiedFile | !modifiedFile.isFile())
      throw new GradleException("Not a file: ${modifiedFile}")
    if (null == contentFile || !contentFile.isFile())
      throw new GradleException("Not a file: ${contentFile}")
    log.info("Diffing ${contentFile} and ${modifiedFile} into ${destinationFile}.")
    List<String> contentLines = getLines(contentFile)
    List<String> modifiedLines = getLines(modifiedFile)
    Patch<String> patch = applyOrThrow(modifiedLines, contentLines)
    List<String> diff = applyOrThrow(
      contentFile, modifiedFile, contentLines, patch, contextSize)
    File written = writeLines(diff, destinationFile, charset)
    log.info("Diffed ${contentFile}\n\tand ${modifiedFile}\n\tinto ${written}.")
    written
  }

  Patch<String> applyOrThrow(
    List<String> modifiedLines, List<String> contentLines) {
    DiffUtils.diff(contentLines, modifiedLines)
  }

  List<String> applyOrThrow(
    File contentFile, File modifiedFile, List<String> contentLines,
    Patch<String> patch, int contextSize) {
    DiffUtils.generateUnifiedDiff(
      contentFile.getName(), modifiedFile.getName(), contentLines,
      patch, contextSize)
  }
}
