package com.codacy.fauxpas

import com.codacy.fauxpas.FauxPasResult._

class FauxPasReportParser() {

  private val outputStartDelimiter = "{"
  private val outputEndDelimiter = "------"

  def parse(lines: Seq[String]): Seq[FauxPasResult] = {

    val unprefixedLines = lines.dropWhile(_.trim != outputStartDelimiter)
    val cleanLines = unprefixedLines.takeWhile(_.trim != outputEndDelimiter)
    val jsonString = cleanLines.mkString

    (for {
      json <- io.circe.parser.parse(jsonString)
      parsedOutput <- json.as[FauxPasOutput]
    } yield parsedOutput.diagnostics) match {
      case Right(results) => results
      case Left(error) =>
        System.err.println(s"Error while parsing output. Message: ${error.getMessage}")
        error.printStackTrace(System.err)
        Seq.empty
    }

  }
}
