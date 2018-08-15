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

package nl.biopet.tools.sampleconfig.cromwellarrays

import nl.biopet.utils.tool.ToolCommand
import nl.biopet.tools.sampleconfig._
import nl.biopet.utils.conversions

object CromwellArrays extends ToolCommand[Args] {
  def emptyArgs = Args()
  def argsParser = new ArgsParser(this)

  def main(args: Array[String]): Unit = {
    val cmdArgs = cmdArrayToArgs(args)

    logger.info("Start")

    val config = readSampleConfigs(cmdArgs.inputFiles)

    val biowdlSampleConfig = config.samples.map {
      case (sampleId, sample) =>
        val libraries = sample.libraries.map {
          case (libraryId, library) =>
            val readgroups = library.readgroups.map {
              case (readgroupId, readgroup) =>
                readgroup.values + ("id" -> readgroupId)
            }.toList
            library.values + ("id" -> libraryId) + ("readgroups" -> readgroups)
        }.toList
        sample.values + ("id" -> sampleId) + ("libraries" -> libraries)
    }.toList
    val json = conversions.mapToJson(Map("samples" -> biowdlSampleConfig))
    println(json)

    logger.info("Done")
  }

  def descriptionText: String =
    """
      |This tool will convert the sample configs to a array based format that can be used inside wdl pipelines.
      |This tool is only to support biowdl pipelines.
      |
    """.stripMargin

  def manualText: String =
    """
      |For this tool to work the sample yml / json should look like this:
      |```
      |samples:
      |  sampleName:
      |    key1: value1
      |    libraries:
      |      libraryName:
      |        key1: value1
      |        readgroups:
      |          readgroupName:
      |            key1: value1
      |            key2: value2
      |```
    """.stripMargin

  def exampleText: String =
    s"""
      |Default run, output to stdout:
      |${SampleConfig.example("CromwellArrays", "-i", "<input config>")}
      |
      |Default run, output to file:
      |${SampleConfig.example("CromwellArrays",
                              "-i",
                              "<input config>",
                              "-o",
                              "<output file>")}
      |
      |Multiple configs:
      |${SampleConfig.example("CromwellArrays",
                              "-i",
                              "<input config>",
                              "-i",
                              "<input config>")}
      |
    """.stripMargin
}
