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

package nl.biopet.tools.sampleconfig.readfromtsv

import java.io.{File, PrintWriter}

import nl.biopet.utils.Documentation.htmlTable
import nl.biopet.utils.conversions
import nl.biopet.utils.tool.ToolCommand

import scala.collection.mutable
import scala.io.Source

object ReadFromTsv extends ToolCommand[Args] {
  def emptyArgs: Args = Args()
  def argsParser = new ArgsParser(this)

  def main(args: Array[String]): Unit = {
    val cmdArgs = cmdArrayToArgs(args)

    require(cmdArgs.inputFiles.nonEmpty || cmdArgs.tagFiles.nonEmpty,
            "At least 1 input or tag file should be given")

    val configMap = stringFromInputs(cmdArgs.inputFiles, cmdArgs.tagFiles)
    cmdArgs.outputFile match {
      case Some(file)
          if file.getName.endsWith(".yml") || file.getName.endsWith(".yaml") =>
        conversions.mapToYamlFile(configMap, file)
      case Some(file) =>
        val writer = new PrintWriter(file)
        writer.println(conversions.mapToJson(configMap))
        writer.close()
      case _ => println(conversions.mapToJson(configMap))
    }
  }

  def mapFromFile(inputFile: File, tags: Boolean = false): Map[String, Any] = {
    val reader = Source.fromFile(inputFile)
    val lines = reader.getLines().toList.filter(!_.isEmpty)
    val header = lines.headOption
      .map(_.split("\t"))
      .getOrElse(throw new IllegalArgumentException(" file seems to be empty"))
    val sampleColumn = header.indexOf("sample")
    val libraryColumn = header.indexOf("library")
    if (sampleColumn == -1)
      throw new IllegalStateException(
        "Sample column does not exist in: " + inputFile)

    val sampleLibCache: mutable.Set[(String, Option[String])] = mutable.Set()

    val librariesValues: List[Map[String, Any]] = for (tsvLine <- lines.tail)
      yield {
        val values = tsvLine.split("\t")
        require(header.length == values.length,
                "Number of columns is not the same as the header")
        val sample = values(sampleColumn)
        val library =
          if (libraryColumn != -1) Some(values(libraryColumn)) else None

        if (sample.headOption.exists(_.isDigit) || library.exists(
              _.headOption.exists(_.isDigit)))
          throw new IllegalStateException(
            "Sample or library may not start with a number")
        if (sampleLibCache.contains((sample, library)))
          throw new IllegalStateException(
            s"Combination of $sample ${library.map("and " + _).getOrElse("")} is found multiple times")
        else sampleLibCache.add((sample, library))
        val valuesMap =
          (for (t <- 0 until values.size
                if !values(t).isEmpty && t != sampleColumn && t != libraryColumn)
            yield header(t) -> values(t)).toMap
        library match {
          case Some(lib) if tags =>
            Map(
              "samples" -> Map(sample -> Map(
                "libraries" -> Map(lib -> Map("tags" -> valuesMap)))))
          case Some(lib) =>
            Map(
              "samples" -> Map(
                sample -> Map("libraries" -> Map(lib -> valuesMap))))
          case _ if tags =>
            Map("samples" -> Map(sample -> Map("tags" -> valuesMap)))
          case _ => Map("samples" -> Map(sample -> valuesMap))
        }
      }
    librariesValues.foldLeft(Map[String, Any]())((acc, kv) =>
      conversions.mergeMaps(acc, kv))
  }

  def stringFromInputs(inputs: List[File],
                       tagsInputs: List[File]): Map[String, Any] = {
    val map =
      inputs
        .map(f => mapFromFile(f))
        .foldLeft(Map[String, Any]())((acc, kv) =>
          conversions.mergeMaps(acc, kv))
    val tags =
      tagsInputs
        .map(f => mapFromFile(f, tags = true))
        .foldLeft(Map[String, Any]())((acc, kv) =>
          conversions.mergeMaps(acc, kv))
    conversions.mergeMaps(map, tags)
  }

  def descriptionText: String =
    """
      |This tool enables a user to create a full sample sheet in JSON format or
      |YAML format, suitable for all Biopet Queue pipelines, from TSV file(s).
    """.stripMargin

  def manualText: String =
    """
      |A user provides a TAB separated file (TSV) with sample specific
      |properties which are parsed into JSON format by the tool.
      |For example, a user wants to add certain properties to the
      |description of a sample, such as the treatment a sample received.
      | Then a TSV file with an extra column called treatment is provided.
      |The resulting file will have the 'treatment' property in it as well.
      |The order of the columns is not relevant to the end result
      |
      |The tag files works the same only the value is prefixed in the key `tags`.
      |
    """.stripMargin

  def exampleText: String =
    s"""
       |
       |#### Sample definition
       |
       |To get the below example out of the tool one should provide 2 TSV files as follows:
       |
       |${htmlTable(List("sample", "library", "bam"),
                    List(
                      List("Sample_ID_1", "Lib_ID_1", "MyFirst.bam"),
                      List("Sample_ID_2", "Lib_ID_2", "MySecond.bam")
                    ))}
       |
       |The second TSV file can contain as much properties as you would like.
       |Possible option would be: gender, age and family.
       |Basically anything you want to pass to your pipeline is possible.
       |
       |${htmlTable(List("sample", "treatment"),
                    List(
                      List("Sample_ID_1", "heatshock"),
                      List("Sample_ID_2", "heatshock")
                    ))}
       |#### Example
       |
       |###### Yaml
       |
       |
       |    samples:
       |      Sample_ID_1:
       |        treatment: heatshock
       |       libraries:
       |         Lib_ID_1:
       |            bam: MyFirst.bam
       |     Sample_ID_2:
       |        treatment: heatshock
       |       libraries:
       |          Lib_ID_2:
       |            bam: MySecond.bam
       |
       |
       |###### Json
       |
       |
       |    {
       |      "samples" : {
       |        "Sample_ID_1" : {
       |          "treatment" : "heatshock",
       |          "libraries" : {
       |            "Lib_ID_1" : {
       |              "bam" : "MyFirst.bam"
       |            }
       |          }
       |        },
       |        "Sample_ID_2" : {
       |          "treatment" : "heatshock",
       |          "libraries" : {
       |            "Lib_ID_2" : {
       |              "bam" : "MySecond.bam"
       |            }
       |          }
       |        }
       |      }
       |    }
       |
       |
     """.stripMargin
}
