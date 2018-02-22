/*
 * Copyright (c) 2018 Biopet
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package nl.biopet.tools.sampleconfig

import nl.biopet.utils.tool.ToolCommand
import nl.biopet.utils.conversions

object SampleConfig extends ToolCommand[Args] {
  def emptyArgs = Args()
  def argsParser = new ArgsParser(this)

  def main(args: Array[String]): Unit = {
    val cmdArgs = cmdArrayToArgs(args)

    logger.info("Start")

    val config = cmdArgs.inputFiles
      .map(conversions.yamlFileToMap)
      .foldLeft(Map[String, Any]())(conversions.mergeMaps(_, _))

    //TODO: Add config
    (cmdArgs.mode, cmdArgs.sample, cmdArgs.library, cmdArgs.readgroup) match {
      case ("samples", _, _, _) => getSamples(config).foreach(println)
      case ("libraries", Some(sample), _, _) =>
        getLibraries(config, sample).foreach(println)
      case ("libraries", _, _, _) =>
        throw new IllegalArgumentException(
          "libraries does require a sample name")
      case ("readgroups", Some(sample), Some(library), _) =>
        getReadgroups(config, sample, library).foreach(println)
      case ("readgroups", _, _, _) =>
        throw new IllegalArgumentException(
          "readgroups does require a sample name and library name")
      case (m, _, _, _) =>
        throw new IllegalArgumentException(s"Mode '$m' does not exist")
    }

    logger.info("Done")
  }

  private def getSamplesConfig(config: Map[String, Any]): Map[String, Any] = {
    conversions.any2map(
      config.getOrElse("samples",
                       throw new IllegalStateException("No samples key found")))
  }

  private def getSampleConfig(config: Map[String, Any],
                              sample: String): Map[String, Any] = {
    conversions.any2map(
      getSamplesConfig(config).getOrElse(
        sample,
        throw new IllegalStateException(s"Sample '$sample' not found")))
  }

  private def getLibrariesConfig(config: Map[String, Any],
                                 sample: String): Map[String, Any] = {
    conversions.any2map(
      getSampleConfig(config, sample).getOrElse("libraries", Map()))
  }

  private def getlibraryConfig(config: Map[String, Any],
                               sample: String,
                               library: String): Map[String, Any] = {
    conversions.any2map(
      getLibrariesConfig(config, sample).getOrElse(
        library,
        throw new IllegalStateException(
          s"Library '$library' in sample '$sample' not found")))
  }

  private def getReadgroupsConfig(config: Map[String, Any],
                                  sample: String,
                                  library: String): Map[String, Any] = {
    conversions.any2map(
      getlibraryConfig(config, sample, library).getOrElse("readgroups", Map()))
  }

  private def getReadgroupConfig(config: Map[String, Any],
                                 sample: String,
                                 library: String,
                                 readgroup: String): Map[String, Any] = {
    conversions.any2map(
      getReadgroupsConfig(config, sample, library).getOrElse(
        readgroup,
        throw new IllegalStateException(
          s"Readgroup '$readgroup' in library '$library' in sample '$sample' not found")))
  }

  def getSamples(config: Map[String, Any]): List[String] = {
    getSamplesConfig(config).keys.toList
  }

  def getLibraries(config: Map[String, Any], sample: String): List[String] = {
    getLibrariesConfig(config, sample).keys.toList
  }

  def getReadgroups(config: Map[String, Any],
                    sample: String,
                    library: String): List[String] = {
    getReadgroupsConfig(config, sample, library).keys.toList
  }

  def descriptionText: String =
    """
      |
    """.stripMargin

  def manualText: String =
    """
      |
    """.stripMargin

  def exampleText: String =
    """
      |
    """.stripMargin
}
