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

import java.io.File

import nl.biopet.utils.tool.{AbstractOptParser, ToolCommand}

class ArgsParser(toolCommand: ToolCommand[Args])
    extends AbstractOptParser[Args](toolCommand) {
  opt[File]('i', "inputFiles") unbounded () valueName "<file>" action {
    (x, c) =>
      c.copy(inputFiles = x :: c.inputFiles)
  } text
    """Input must be a tsv file, first line is seen as header and must at least have a 'sample' column,
      | 'library' column is optional, multiple files can be specified by using multiple flags.""".stripMargin
  opt[File]('t', "tagFiles") unbounded () valueName "<file>" action { (x, c) =>
    c.copy(tagFiles = x :: c.tagFiles)
  } text "This works the same as for a normal input file. Difference is that it placed in a sub key 'tags' in the config file"
  opt[File]('o', "outputFile") valueName "<file>" action { (x, c) =>
    c.copy(outputFile = Some(x))
  } text """
           |When the extension is .yml or .yaml the output is in yaml format, otherwise it is in json.
           |When no file is given the output goes to stdout as yaml.
         """.stripMargin
}
