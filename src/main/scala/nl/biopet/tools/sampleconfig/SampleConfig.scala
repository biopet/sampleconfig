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

import nl.biopet.tools.sampleconfig.casecontrol.CaseControl
import nl.biopet.tools.sampleconfig.cromwellarrays.CromwellArrays
import nl.biopet.tools.sampleconfig.extracttsv.ExtractTsv
import nl.biopet.tools.sampleconfig.readfromtsv.ReadFromTsv
import nl.biopet.utils.conversions
import nl.biopet.utils.tool.ToolCommand
import nl.biopet.utils.tool.multi.MultiToolCommand

object SampleConfig extends MultiToolCommand {

  def subTools: Map[String, List[ToolCommand[_]]] =
    Map("Tools" -> List(ExtractTsv, ReadFromTsv, CromwellArrays, CaseControl))

  def descriptionText: String = extendedDescriptionText

  def manualText: String = extendedManualText

  def exampleText: String = extendedExampleText
}

class SampleConfig(config: Map[String, Any]) {
  lazy val samples: Map[String, Sample] = config.get("samples") match {
    case Some(s) =>
      conversions.any2map(s).map {
        case (name, c) => name -> new Sample(name, conversions.any2map(c))
      }
    case _ => Map()
  }

  lazy val values: Map[String, Any] = config.filter {
    case (k, _) => k != "samples"
  }
}
