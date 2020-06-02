package com.brambolt.gradle

import com.brambolt.gradle.patching.tasks.ProcessPatches
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.language.jvm.tasks.ProcessResources

/**
 * <p>A Gradle plug-in for working with patch files.</p>
 *
 * <p>The plugin creates a <code>processPatches</code> task when applied.</p>
 *
 * <p>If a <code>processResources</code> task exists then the process-patches
 * task is made a dependency of process-resources, so patches are processed
 * before general resource processing takes place.</p>
 *
 * <p>The <code>processResources</code> is created and included in the dependency
 * chain but is not configured with content or patches. If executed it will
 * do nothing. To configure the task, a configuration closure similar to this
 * should be used:</p>
 *
 * <pre>
 *   processResources {
 *     content = "${project.projectDir}/src/main/content"
 *     patches = "${project.projectDir}/src/main/patches"
 *     destination = "${project.buildDir}/resources/main"
 *   }
 * </pre>
 *
 * @see com.brambolt.gradle.patching.tasks.ProcessPatches
 */
class PatchingPlugin implements Plugin<Project> {

  /**
   * Applies the plug-in to the parameter project.
   * @param project The project to apply the plug-in to
   */
  void apply(Project project) {
    project.logger.debug("Applying ${getClass().getCanonicalName()}.")
    Task processPatches = createProcessPatchesTask(project)
    configureDefaultTaskDependencies(project, processPatches)
  }

  protected Task createProcessPatchesTask(Project project) {
    project.task(
      // Apply an empty configuration closure to set defaults, if any:
      [type: ProcessPatches], 'processPatches') {}

  }

  protected void configureDefaultTaskDependencies(Project project, Task processPatches) {
    try {
      project.tasks.withType(ProcessResources).named('processResources') {
        Task t -> t.dependsOn(processPatches)
      }
    } catch (Exception x) {
      project.logger.debug('Unable to add task dependency', x)
    }
  }
}
