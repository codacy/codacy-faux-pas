package com.codacy.fauxpas
import java.nio.file.{Path, Paths}

import com.codacy.plugins.api.results
import com.codacy.analysis.core.model.{FullLocation, Issue}
import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

//the file field may come empty, hence the Optional type
case class FauxPasResult(
    extent: FauxPasResult.Extent,
    file: Option[String],
    info: String,
    ruleShortName: String,
    severity: Int
)
case class FauxPasOutput(diagnostics: Seq[FauxPasResult])

object FauxPasResult {

  case class Extent(start: FauxPasResult.Extent.Data, end: FauxPasResult.Extent.Data)

  object Extent {
    case class Data(line: Int, utf16Column: Int)
  }

  implicit val fauxPasExtentDataDecoder: Decoder[FauxPasResult.Extent.Data] = deriveDecoder
  implicit val fauxPasExtentDecoder: Decoder[FauxPasResult.Extent] = deriveDecoder
  implicit val fauxPasResultDecoder: Decoder[FauxPasResult] = deriveDecoder
  implicit val fauxPasOutputDecoder: Decoder[FauxPasOutput] = deriveDecoder

  private val toolPrefix = "FauxPas_"

  def withPrefix(patternId: String) = s"$toolPrefix$patternId"

  def toIssue(result: FauxPasResult, relativizeTo: Path): Option[Issue] = {
    result.file.map(
      file =>
        Issue(
          results.Pattern.Id(withPrefix(result.ruleShortName)),
          relativizeTo.relativize(Paths.get(file)),
          Issue.Message(result.info),
          FauxPasResult.convertLevel(result.severity),
          FauxPasResult.convertCategory(result.ruleShortName),
          FullLocation(result.extent.start.line, result.extent.start.utf16Column)
      )
    )
  }

  private def convertLevel(level: Int): results.Result.Level.Value = level match {
    case 1 | 2 => results.Result.Level.Info
    case 3 | 4 | 5 => results.Result.Level.Warn
    case _ => results.Result.Level.Err
  }

  private def convertCategory(checkName: String): Option[results.Pattern.Category] = {
    //Always converts to none because the category used in the product will be the one in patterns.json
    //since this tool does not have any updated for the last 3 years, we assume that the patterns.json file will have updated docs in all cases
    None
  }
}
