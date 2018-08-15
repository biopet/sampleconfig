/*
 * Copyright (c) 2014 Biopet
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

import java.io.File

import nl.biopet.utils.conversions
import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test
import play.api.libs.json.Json

class ReadFromTsvTest extends ToolTest[Args] {

  def toolCommand: ReadFromTsv.type = ReadFromTsv

  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      ReadFromTsv.main(Array())
    }.getMessage shouldBe "requirement failed: At least 1 input or tag file should be given"
  }

  @Test
  def testCorrectSampleTsv(): Unit = {
    val tsv = resourcePath("/sample.tsv")
    val output = File.createTempFile("testCorrectSampleTsv", ".json")
    output.deleteOnExit()

    noException should be thrownBy ReadFromTsv.main(
      Array("-i", tsv, "-o", output.toString))
  }

  @Test
  def testNoSampleColumn(): Unit = {
    val tsv = resourcePath("/no_sample.tsv")
    val output = File.createTempFile("testNoSampleColumn", ".json")
    output.deleteOnExit()
    val thrown = the[IllegalStateException] thrownBy ReadFromTsv.main(
      Array("-i", tsv, "-o", output.toString))
    thrown.getMessage should equal("Sample column does not exist in: " + tsv)
  }

  @Test
  def testNumberInLibs(): Unit = {
    val tsv = resourcePath("/number.tsv")
    val output = File.createTempFile("testNumberInLibs", ".json")
    output.deleteOnExit()
    val thrown = the[IllegalStateException] thrownBy ReadFromTsv.main(
      Array("-i", tsv, "-o", output.toString))
    thrown.getMessage should equal(
      "Sample or library may not start with a number")
  }

  @Test
  def testSampleIDs(): Unit = {
    val tsv = resourcePath("/same.tsv")
    val output = File.createTempFile("testSampleIDs", ".json")
    output.deleteOnExit()
    val thrown = the[IllegalStateException] thrownBy ReadFromTsv.main(
      Array("-i", tsv, "-o", output.toString))
    thrown.getMessage should equal(
      "Combination of Sample_ID_1 and Lib_ID_1 is found multiple times")

  }

  @Test
  def testJson(): Unit = {
    val tsv = new File(resourcePath("/sample.tsv"))
    val json = ReadFromTsv.stringFromInputs(List(tsv), Nil)

    conversions.mapToJson(json) shouldBe Json.parse(
      """|{
                                                      |  "samples" : {
                                                      |    "Sample_ID_1" : {
                                                      |      "libraries" : {
                                                      |        "Lib_ID_1" : {
                                                      |          "bam" : "MyFirst.bam"
                                                      |        }
                                                      |      }
                                                      |    },
                                                      |    "Sample_ID_2" : {
                                                      |      "libraries" : {
                                                      |        "Lib_ID_2" : {
                                                      |          "bam" : "MySecond.bam"
                                                      |        }
                                                      |      }
                                                      |    }
                                                      |  }
                                                      |}""".stripMargin)
  }

}
