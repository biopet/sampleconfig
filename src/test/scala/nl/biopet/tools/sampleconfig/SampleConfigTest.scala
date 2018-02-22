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

import java.io.{ByteArrayOutputStream, File}

import nl.biopet.utils.test.tools.ToolTest
import org.testng.annotations.Test

class SampleConfigTest extends ToolTest[Args] {
  def toolCommand: SampleConfig.type = SampleConfig
  @Test
  def testNoArgs(): Unit = {
    intercept[IllegalArgumentException] {
      SampleConfig.main(Array())
    }
  }

  @Test
  def testSamples(): Unit = {
    val baos = new ByteArrayOutputStream
    Console.withOut(baos) {
      SampleConfig.main(Array("-i", resourcePath("/samples.yml")))
    }
    val samples = new String(baos.toByteArray).split("\n").toList.sorted
    samples shouldBe List("sample1", "sample2")
  }

  @Test
  def testLibraries(): Unit = {
    val baos = new ByteArrayOutputStream
    Console.withOut(baos) {
      SampleConfig.main(
        Array("-i", resourcePath("/samples.yml"), "--sample", "sample1"))
    }
    val libraries = new String(baos.toByteArray).split("\n").toList.sorted
    libraries shouldBe List("lib1")
  }

  @Test
  def testReadgroups(): Unit = {
    val baos = new ByteArrayOutputStream
    Console.withOut(baos) {
      SampleConfig.main(
        Array("-i",
              resourcePath("/samples.yml"),
              "--sample",
              "sample1",
              "--library",
              "lib1"))
    }
    val readgroups = new String(baos.toByteArray).split("\n").toList.sorted
    readgroups shouldBe List("rg1")
  }

  @Test
  def testSampleConfig(): Unit = {
    val jsonOutput = File.createTempFile("test.", ".json")
    jsonOutput.deleteOnExit()
    val tsvOutput = File.createTempFile("test.", ".tsv")
    tsvOutput.deleteOnExit()

    SampleConfig.main(
      Array("-i",
            resourcePath("/samples.yml"),
            "--sample",
            "sample1",
            "--jsonOutput",
            jsonOutput.getAbsolutePath,
            "--tsvOutput",
            tsvOutput.getAbsolutePath))
  }

  @Test
  def testLibraryConfig(): Unit = {
    val jsonOutput = File.createTempFile("test.", ".json")
    jsonOutput.deleteOnExit()
    val tsvOutput = File.createTempFile("test.", ".tsv")
    tsvOutput.deleteOnExit()

    SampleConfig.main(
      Array(
        "-i",
        resourcePath("/samples.yml"),
        "--sample",
        "sample1",
        "--library",
        "lib1",
        "--jsonOutput",
        jsonOutput.getAbsolutePath,
        "--tsvOutput",
        tsvOutput.getAbsolutePath
      ))
  }

  @Test
  def testReadgroupConfig(): Unit = {
    val jsonOutput = File.createTempFile("test.", ".json")
    jsonOutput.deleteOnExit()
    val tsvOutput = File.createTempFile("test.", ".tsv")
    tsvOutput.deleteOnExit()

    SampleConfig.main(
      Array(
        "-i",
        resourcePath("/samples.yml"),
        "--sample",
        "sample1",
        "--library",
        "lib1",
        "--readgroup",
        "rg1",
        "--jsonOutput",
        jsonOutput.getAbsolutePath,
        "--tsvOutput",
        tsvOutput.getAbsolutePath
      ))
  }

  @Test
  def testNoTags(): Unit = {
    intercept[IllegalArgumentException] {
      SampleConfig.main(
        Array("-i", resourcePath("/samples.yml"), "--jsonOutput", "."))
    }.getMessage shouldBe "No tags found to write a file"

    intercept[IllegalArgumentException] {
      SampleConfig.main(
        Array("-i", resourcePath("/samples.yml"), "--tsvOutput", "."))
    }.getMessage shouldBe "No tags found to write a file"
  }

  @Test
  def testWrongSample(): Unit = {
    intercept[IllegalStateException] {
      SampleConfig.main(
        Array("-i", resourcePath("/samples.yml"), "--sample", "not_exist"))
    }.getMessage shouldBe "Sample 'not_exist' not found"
  }

  @Test
  def testWrongLibrary(): Unit = {
    intercept[IllegalStateException] {
      SampleConfig.main(
        Array("-i",
              resourcePath("/samples.yml"),
              "--sample",
              "sample1",
              "--library",
              "not_exist"))
    }.getMessage shouldBe "Library 'not_exist' in sample 'sample1' not found"
  }

  @Test
  def testWrongReadgroup(): Unit = {
    intercept[IllegalStateException] {
      SampleConfig.main(
        Array("-i",
              resourcePath("/samples.yml"),
              "--sample",
              "sample1",
              "--library",
              "lib1",
              "--readgroup",
              "not_exist",
              "--tsvOutput",
              "."))
    }.getMessage shouldBe "Readgroup 'not_exist' in library 'lib1' in sample 'sample1' not found"
  }
}
