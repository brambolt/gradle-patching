package com.brambolt.gradle

import org.gradle.api.GradleException

/**
 * Utility class with functions to convert objects to intended property types.
 */
class SpecObjects {

  /**
   * Converts a spec object to a file, if possible. The spec can be a string
   * path, a file object or a closure that yields a path or a file.
   * @param propertyName The property name, only used for an exception message
   * @param spec The object to convert to a file
   * @return The file produced by converting the spec object; never null
   * @throws org.gradle.api.GradleException If the spec object can't be converted to a file
   */
  static File getFile(String propertyName, Object spec) {
    File file = getFile(spec)
    if (null == file)
      throw new GradleException(
        "Configure ${propertyName} property with ${propertyName} = <path, file or closure>")
    file
  }


  /**
   * Converts a spec object to a file, if possible. The spec can be a string
   * path, a file object or a closure that yields a path or a file. The function
   * returns null if it for some reason can't produce a file.
   * @param spec The object to convert to a file
   * @return The file produced by converting the spec object, or null
   */
  static File getFile(Object spec) {
    switch (spec) {
      case { it instanceof String || it instanceof GString }:
        return new File(spec as String)
      case { it instanceof File }:
        return spec as File
      case { it instanceof Closure }:
        return getFile((spec as Closure).call())
      default:
        return null
    }
  }
}