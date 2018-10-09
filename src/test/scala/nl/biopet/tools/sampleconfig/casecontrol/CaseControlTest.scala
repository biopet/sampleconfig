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

import nl.biopet.utils.conversions
import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test
import play.api.libs.json.Json

class CaseControlTest extends ToolTest[Args] {

  def toolCommand: CaseControl.type = CaseControl

  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      CaseControl.main(Array())
    }
  }

  @Test
  def testDefault(): Unit = {
    val output = File.createTempFile("test.", ".json")
    output.deleteOnExit()

    noException should be thrownBy CaseControl.main(
      Array("-i",
            resourcePath("/fake_chrQ1000simreads.bam"),
            "-i",
            resourcePath("/paired01.bam"),
            "-s",
            resourcePath("/casecontrol.yml"),
            "-o",
            output.toString))
  }

  @Test
  def testDefault2(): Unit = {
    val output = File.createTempFile("test.", ".json")
    output.deleteOnExit()

    noException should be thrownBy CaseControl.main(
      Array("-i",
            resourcePath("/fake_chrQ1000simreads.bam"),
            "-i",
            resourcePath("/paired02.bam"),
            "-s",
            resourcePath("/casecontrol.yml"),
            "-o",
            output.toString))
  }

}
