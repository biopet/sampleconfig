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

package nl.biopet.tools

import java.io.File

import nl.biopet.utils.conversions

package object sampleconfig {
  def readSampleConfigs(files: List[File]): SampleConfig = {
    new SampleConfig(
      files
        .map(conversions.yamlFileToMap)
        .foldLeft(Map[String, Any]())(conversions.mergeMaps(_, _)))
  }

  class Sample(name: String, config: Map[String, Any]) {
    lazy val libraries: Map[String, Library] = config.get("libraries") match {
      case Some(s) =>
        conversions.any2map(s).map {
          case (n, c) => n -> new Library(n, conversions.any2map(c))
        }
      case _ => Map()
    }

    lazy val values: Map[String, Any] = config.filter {
      case (k, _) => k != "libraries"
    }
  }

  class Library(name: String, config: Map[String, Any]) {
    lazy val readgroups: Map[String, Readgroup] =
      config.get("readgroups") match {
        case Some(s) =>
          conversions.any2map(s).map {
            case (n, c) => n -> new Readgroup(n, conversions.any2map(c))
          }
        case _ => Map()
      }

    lazy val values: Map[String, Any] = config.filter {
      case (k, _) => k != "readgroups"
    }

  }

  class Readgroup(name: String, config: Map[String, Any]) {
    lazy val values: Map[String, Any] = config
  }

}
