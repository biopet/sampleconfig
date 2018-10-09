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

package nl.biopet.tools.sampleconfig.casecontrol

import java.io.File

import htsjdk.samtools.SamReaderFactory
import nl.biopet.utils.tool.ToolCommand
import nl.biopet.tools.sampleconfig
import nl.biopet.tools.sampleconfig.SampleConfig
import nl.biopet.utils.io
import nl.biopet.utils.ngs.bam
import play.api.libs.json._

object CaseControl extends ToolCommand[Args] {
  def emptyArgs: Args = Args()
  def argsParser = new ArgsParser(this)

  case class IndexedBamFile(file: String, index: String)
  case class CaseControl(inputName: String,
                         inputFile: IndexedBamFile,
                         controlName: String,
                         controlFile: IndexedBamFile)
  case class CaseControls(caseControls: List[CaseControl])

  implicit val indexedBamFileReads: Reads[IndexedBamFile] =
    Json.reads[IndexedBamFile]
  implicit val indexedBamFileWrites: Writes[IndexedBamFile] =
    Json.writes[IndexedBamFile]

  implicit val caseControlReads: Reads[CaseControl] = Json.reads[CaseControl]
  implicit val caseControlWrites: Writes[CaseControl] = Json.writes[CaseControl]

  implicit val caseControlsReads: Reads[CaseControls] = Json.reads[CaseControls]
  implicit val caseControlsWrites: Writes[CaseControls] =
    Json.writes[CaseControls]

  def main(args: Array[String]): Unit = {
    val cmdArgs = cmdArrayToArgs(args)

    logger.info("Start")

    val config = sampleconfig.readSampleConfigs(cmdArgs.sampleConfigs)
    val fileMap = createFileMap(cmdArgs.inputFiles)

    val pairsJson =
      Json.toJson(createPairs(config, fileMap, cmdArgs.controlTag))
    val pairsJsonString = Json.stringify(pairsJson)

    cmdArgs.outputFile match {
      case Some(file) => io.writeLinesToFile(file, pairsJsonString :: Nil)
      case _          => println(pairsJsonString)
    }

    logger.info("Done")
  }

  /**
    * This will create a map of bamfiles including the index files
    * @param files bam files
    * @return
    */
  def createFileMap(files: List[File]): Map[String, IndexedBamFile] = {
    bam.sampleBamMap(files).map {
      case (k, v) =>
        val reader = SamReaderFactory.makeDefault().open(v)
        val index = (reader.hasIndex,
                     new File(v.getPath + ".bai"),
                     new File(v.getPath.stripSuffix(".bam") + ".bai")) match {
          case (true, i, _) if i.exists() => i
          case (true, _, i) if i.exists() => i
          case _ =>
            throw new IllegalStateException(
              s"Bam file '$v' does not have an index file")
        }
        reader.close()
        k -> IndexedBamFile(v.getPath, index.getPath)
    }
  }

  /**
    * This will create the control pairs
    * @param config SampleConfig
    * @param fileMap bam files
    * @param controlTag control tag, default "control"
    * @return
    */
  def createPairs(config: SampleConfig,
                  fileMap: Map[String, IndexedBamFile],
                  controlTag: String = "control"): CaseControls = {
    CaseControls(
      config.samples
        .filter { case (_, s) => s.values.contains(controlTag) }
        .flatMap {
          case (sampleName, sample) =>
            val inputBam = fileMap.getOrElse(
              sampleName,
              throw new IllegalStateException(
                s"Bam file for input sample '$sampleName' not found"))
            sample.values
              .get(controlTag)
              .toList
              .flatMap {
                case x: List[_] => x.map(_.toString)
                case x          => List(x.toString)
              }
              .map { controlName =>
                val controlBam = fileMap.getOrElse(
                  controlName,
                  throw new IllegalStateException(
                    s"Bam file for input sample '$controlName' not found"))
                CaseControl(sampleName, inputBam, controlName, controlBam)
              }
        }
        .toList)
  }

  def descriptionText: String =
    """
      | This tool will extract the case-control pairs from a sample config file.
      | It will read the headers of the bam files to confirm that samples do exist.
    """.stripMargin

  def manualText: String =
    """
      | Each that has a control need to have the control tag inside the config file.
      | The tool will automatically finds the combination of bam file needed, for this the readgroups should be setup correctly.
    """.stripMargin

  def exampleText: String =
    s"""
       |Default run, 2 bam files:
       |${SampleConfig.example("CaseControl",
                               "-i",
                               "<bam file>",
                               "-i",
                               "<bam file>",
                               "-s",
                               "<sample config file>",
                               "-o",
                               "<output file>")}
       |
       |
     """.stripMargin
}
