import java.net.URL
import java.util.zip.GZIPInputStream

import better.files.Dsl._
import com.codacy.plugins.api.results.Pattern.Description
import com.codacy.plugins.api.results.Result.Level
import com.codacy.plugins.api.results.{Pattern, Tool}
import play.api.libs.json.Json

import scala.io.Source

object DocGenerator extends App {
  Generator.run()
}

object Generator {
  private val rulesDocumentationUrl = new URL("http://fauxpasapp.com/rules/")
  private val toolName = "Faux-Pas"
  // This tool version can be retrieved from http://fauxpasapp.com/releases/
  // At the time of this comment, the version was not updated for 3 years now so we decided not to fetch it dynamically
  private val toolVersion = "1.7.2"
  private val docsDir = pwd / "docs"
  private val descriptionDir = docsDir / "description"

  def run() = {
    val documentationHtmlContent = fetchRulesDocumentationHtml
    val fauxPasDocumentationParser = FauxPasDocumentationParser.fromString(documentationHtmlContent)

    val ruleTypesElements = fauxPasDocumentationParser.getRuleTypesElements()
    val rules = ruleTypesElements.flatMap(fauxPasDocumentationParser.getRules)

    val patternSpecification = rules.map(ruleToPatternSpecification)
    val patternDescriptions = rules.map(ruleToPatternDescription)

    val toolSpecification = Tool.Specification(Tool.Name(toolName), Some(Tool.Version(toolVersion)), patternSpecification.toSet)

    writePatternsJson(toolSpecification)
    writeDescriptionsJson(patternDescriptions, rules)
  }

  private def fetchRulesDocumentationHtml = {
    val connection = rulesDocumentationUrl.openConnection()
    connection.setRequestProperty("Accept-Encoding", "gzip")
    val gzippedInputStream = new GZIPInputStream(connection.getInputStream)
    Source.fromInputStream(gzippedInputStream).mkString
  }

  private def ruleToPatternSpecification(rule: Rule): Pattern.Specification = {
    // Since level will be sent in the report
    // we are using a default one here
    val level = Level.Info
    val category = patternCategoryFromRule(rule)
    Pattern.Specification(Pattern.Id(rule.id), level, category, None, Set.empty)
  }

  private def patternCategoryFromRule(rule: Rule) = {
    rule.category.value match {
      case "BestPractice" => Pattern.Category.BestPractice
      case "Resources" => Pattern.Category.ErrorProne
      case "Config" => Pattern.Category.ErrorProne
      case "Localization" => Pattern.Category.BestPractice
      case "APIUsage" => Pattern.Category.ErrorProne
      case "VCS" => Pattern.Category.ErrorProne
      case "Style" => Pattern.Category.CodeStyle
      case "Pedantic" => Pattern.Category.BestPractice
      case "Miscellaneous" => Pattern.Category.ErrorProne
    }
  }

  private def ruleToPatternDescription(rule: Rule): Description = {
    Description(
      patternId = Pattern.Id(rule.id),
      title = Pattern.Title(rule.name),
      description = Some(Pattern.DescriptionText(rule.description)),
      timeToFix = None,
      parameters = Set.empty
    )
  }

  private def writePatternsJson(toolSpecification: Tool.Specification): Unit = {
    mkdirs(docsDir)

    val specificationJsonString = Json.prettyPrint(Json.toJson(toolSpecification))

    val patternJsonFile = docsDir / "patterns.json"
    patternJsonFile.writeText(specificationJsonString + System.lineSeparator)
  }

  private def writeDescriptionsJson(patternDescriptions: Seq[Description], rules: List[Rule]): Unit = {
    rm(descriptionDir)
    mkdirs(descriptionDir)

    val descriptionJsonFile = descriptionDir / "description.json"
    val descriptionsJsonString = Json.prettyPrint(Json.toJson(patternDescriptions))
    descriptionJsonFile.writeText(descriptionsJsonString + System.lineSeparator)

    rules.foreach(rule => (descriptionDir / s"${rule.id}.md").writeText(rule.description + System.lineSeparator))
  }
}
