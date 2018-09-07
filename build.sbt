organization := "com.github.biopet"
organizationName := "Biopet"

startYear := Some(2018)

name := "SampleConfig"
biopetUrlName := "sampleconfig"

biopetIsTool := true

mainClass in assembly := Some(
  s"nl.biopet.tools.${name.value.toLowerCase()}.${name.value}")

developers += Developer(id = "ffinfo",
                        name = "Peter van 't Hof",
                        email = "pjrvanthof@gmail.com",
                        url = url("https://github.com/ffinfo"))

scalaVersion := "2.11.12"

libraryDependencies += "com.github.biopet" %% "tool-utils" % "0.6"
libraryDependencies += "com.github.biopet" %% "tool-test-utils" % "0.3"

biocondaTestCommands := Seq(
  "biopet-sampleconfig ExtractTsv --version",
  "biopet-sampleconfig ExtractTsv --help",
  "biopet-sampleconfig ReadFromTsv --version",
  "biopet-sampleconfig ReadFromTsv --help",
  "biopet-sampleconfig CromwellArrays --version",
  "biopet-sampleconfig CromwellArrays --help"
)
