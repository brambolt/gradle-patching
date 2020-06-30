/**
 * Copyright 2017-2020 Brambolt ehf.
 */

package com.brambolt.gradle.patching

import difflib.DiffUtils
import difflib.Patch
import groovy.io.FileType
import org.slf4j.Logger

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.Path
import org.apache.tools.ant.taskdefs.Copy
import org.gradle.api.GradleException

/**
 * @see com.brambolt.gradle.patching.tasks.ProcessPatches
 */
class Patcher {

  /**
   * The default character set, UTF-8.
   */
  static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8

  /**
   * The patching logic looks for files with the <code>.diff</code>
   * extension by default.
   */
  static final String DEFAULT_DIFF_EXTENSION = '.diff'

  Logger log

  Patcher(Logger log) {
    if (null == log)
      throw new IllegalArgumentException('Null logger')
    this.log = log
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
   * Applies one or more patches to one or more files.
   * @param patchOrPatches A patch file or directory containing patch files
   * @param fileOrFiles A content file or directory containing content files
   * @param diffExtension The file extension that identifies the patch files
   * @param charset The character set in use, defaults to UTF-8
   * @param destinatinDir The directory to write to
   * @return A list of errors, or an empty list if patching succeeded
   */
  List<String> apply(
    File patchOrPatches, File fileOrFiles, String diffExtension,
    Charset charset, File destinationDir) {
    if (null == patchOrPatches)
      throw new GradleException('patchOrPatches parameter is null')
    if (null == fileOrFiles)
      throw new GradleException('fileOrFiles parameter is null')
    if (!patchOrPatches.exists() || !fileOrFiles.exists())
    // No patches or no content - nothing to do and no errors?
      return []
    if (!destinationDir.exists())
      destinationDir.mkdirs()
    switch (patchOrPatches) {
      case { it.isDirectory() }:
        return applyDirectory(
          patchOrPatches, fileOrFiles, diffExtension, charset, destinationDir)
      case { it.isFile() }:
        File destinationFile = new File(destinationDir, fileOrFiles.name)
        return applyFile(patchOrPatches, fileOrFiles, charset, destinationFile, [])
      default:
        throw new UnsupportedOperationException(
          "Not a file or a directory: ${patchOrPatches}"
        )
    }
  }

  /**
   * Applies a directory of patches to a directory of content.
   * @param patchDir A directory containing patch files
   * @param contentDir A content directory
   * @param diffExtension The file extension that identifies the patch files
   * @param charset The character set in use, defaults to UTF-8
   * @param destinationDir The directory to write to
   * @return A list of errors, empty if patching succeeded
   */
  List<String> applyDirectory(
    File patchDir, File contentDir, String diffExtension, Charset charset,
    File destinationDir) {
    if (null == patchDir || !patchDir.isDirectory())
      throw new GradleException("Not a valid patch directory: ${patchDir}")
    if (null == contentDir || !contentDir.isDirectory())
      throw new GradleException(
        "Patch directory ${patchDir} can't be applied to content at ${contentDir}")
    if (null == destinationDir || !destinationDir.isDirectory())
      throw new GradleException("Not a valid destination directory: ${destinationDir}")
    if (!diffExtension.startsWith('.'))
      throw new GradleException(
        "The diff file extension should start with a preceding '.' (dot), got instead: ${diffExtension}")
    log.info("Patching content in ${contentDir} with patches in ${patchDir}.")
    List<String> errors = []
    Path rootPath = contentDir.toPath()
    contentDir.eachFileRecurse(FileType.FILES) { File contentFile ->
      errors = visit(patchDir, rootPath, contentFile, diffExtension, charset, destinationDir, errors)
    }
    errors
  }

  private List<String> visit(
    File patchDir, Path rootPath,File contentFile, String diffExtension,
    Charset charset, File destinationDir, List<String> errors) {
    log.debug("Visiting ${contentFile}...")
    Path relDirPath = rootPath.relativize(contentFile.parentFile.toPath())
    File matchingPatchFile = new File(
      // The diff extension must start with the dot character:
      patchDir, "${relDirPath.toString()}/${contentFile.name}${diffExtension}")
    File matchingDestinationFile = new File(
      destinationDir, "${relDirPath.toString()}/${contentFile.name}")
    matchingDestinationFile.parentFile.mkdirs()
    if (matchingPatchFile.exists())
      errors = applyFile(
        matchingPatchFile, contentFile, charset, matchingDestinationFile, errors)
    else copy(contentFile, matchingDestinationFile)
    errors
  }

  /**
   * Applies a patch file to a content file.
   * @param patchFile A patch file
   * @param contentFile A content file
   * @param charset The character set in use, defaults to UTF-8
   * @param destinationFile The file to write to
   * @param errors An error list to add to if patching fails
   * @return The parameter list of errors, unmodified if patching succeeded
   */
  List<String> applyFile(
    File patchFile, File contentFile, Charset charset,
    File destinationFile, List<String> errors) {
    try {
      applyOrThrow(patchFile, contentFile, charset, destinationFile)
    } catch (Exception x) {
      errors.add(
        """Patching exception:
    Patch file:   ${patchFile.absolutePath}
    Content file: ${contentFile.absolutePath}
    Exception:    ${x.getMessage()}
""")
    }
    errors
  }

  /**
   * Applies a patch file to a content file.
   * @param patchFile A patch file
   * @param contentFile A content file
   * @param destinationFile The file to write to
   * @return The destination file
   */
  File applyOrThrow(
    File patchFile, File contentFile, File destinationFile) {
    applyOrThrow(patchFile, contentFile, StandardCharsets.UTF_8, destinationFile)
  }

  /**
   * Applies a patch file to a content file and produces a destination file.
   * @param patchFile A patch file
   * @param contentFile A content file
   * @param charset The character set in use, defaults to UTF-8
   * @param destinationFile The output file to write to
   * @return The destination file
   */
  File applyOrThrow(
    File patchFile, File contentFile, Charset charset, File destinationFile) {
    if (null == patchFile | !patchFile.isFile())
      throw new GradleException("Not a file: ${patchFile}")
    if (null == contentFile || !contentFile.isFile())
      throw new GradleException(
        "Patch file ${patchFile} can't be applied to ${contentFile}")
    log.info("Patching ${contentFile} with ${patchFile} into ${destinationFile}.")
    List<String> patchLines = getLines(patchFile)
    List<String> contentLines = getLines(contentFile)
    List<String> patchedLines = applyOrThrow(patchLines, contentLines)
    File written = writeLines(patchedLines, destinationFile, charset)
    log.info("Patched ${contentFile}\n\twith ${patchFile}\n\tinto ${written}.")
    written
  }

  List<String> applyOrThrow(
    List<String> patchLines, List<String> contentLines) {
    Patch<String> patch = DiffUtils.parseUnifiedDiff(patchLines)
    patch.applyTo(contentLines)
  }

  /**
   * Returns the lines of the file at the input path.
   * @param inputPath The input path to read from
   * @return The lines of the input file
   * @throws IOException If unable to read the file
   */
  protected List<String> getLines(String inputPath) {
    getLines(new File(inputPath))
  }

  /**
   * Returns the lines of the parameter file.
   * @param inputFile The input file to read from
   * @return The lines of the input file
   * @throws IOException If unable to read the file
   */
  protected List<String> getLines(File inputFile) {
    List<String> result = new ArrayList<>()
    inputFile.eachLine { String line -> result.add(line) }
    result
  }

  protected File writeLines(List<String> patchedLines, String outputPath, Charset charset) {
    writeLines(patchedLines, new File(outputPath), charset)
  }

  protected File writeLines(List<String> patchedLines, File outputFile, Charset charset) {
    outputFile.withWriter(charset.name()) { writer ->
      patchedLines.each { String line -> writer.writeLine(line) }
    }
    outputFile
  }

  protected void copy(File file, File toFile) {
    Copy copy = new Copy()
    copy.setFile(file)
    copy.setTofile(toFile)
    copy.execute()
    log.debug("Copied ${file} to ${toFile}.")
  }
}
