package com.codacy.fauxpas

import com.codacy.analysis.core.model.{IssuesAnalysis, ToolResults}
import com.codacy.analysis.core.model.IssuesAnalysis.FileResults
import com.codacy.analysis.core.serializer.IssuesReportSerializer
import java.nio.file.{Path, Paths}

class Converter(toolName: String, reportParser: FauxPasReportParser) {

  def convert(lines: Seq[String], relativizeTo: Path): String = {
    val parsed = reportParser.parse(lines)

    val grouped = parsed
      .groupBy(_.file)
      .view
      .collect {
        case (Some(path), res) => //only get issues associated with a file
          FileResults(
            relativizeTo.relativize(Paths.get(path)),
            res.view.flatMap(FauxPasResult.toIssue(_, relativizeTo)).to(Set)
          )
      }
      .to(Set)

    val toolResults = ToolResults(toolName, IssuesAnalysis.Success(grouped))
    IssuesReportSerializer.toJsonString(Set(toolResults))
  }

}
