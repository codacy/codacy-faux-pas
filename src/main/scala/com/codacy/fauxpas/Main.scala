package com.codacy.fauxpas

import java.nio.file.Paths

object Main {
  val toolName = "faux-pas"

  def run(config: ParserConfig): Unit = {
    val stdin = scala.io.Source.fromInputStream(System.in)(config.encoding)
    val lines = stdin.getLines().to(LazyList)
    val pwd = Paths.get(System.getProperty("user.dir"))
    val jsonString = new Converter(toolName, new FauxPasReportParser).convert(lines, relativizeTo = pwd)
    println(jsonString)
  }

  def main(args: Array[String]): Unit = {
    val exitStatus = ParserConfig.withConfig(toolName, args) { config =>
      run(config)
    }
    sys.exit(exitStatus)
  }

}
