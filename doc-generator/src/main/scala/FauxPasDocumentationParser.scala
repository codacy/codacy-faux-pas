import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.{Document, Element}

case class RuleCategory(value: String)

case class RuleTypeElement(category: RuleCategory, rulesElements: List[Element])

case class Rule(id: String, name: String, description: String, category: RuleCategory)

class FauxPasDocumentationParser(document: Document) {
  def getRuleTypesElements(): List[RuleTypeElement] = {
    val rulesCategoriesSectionElement = document >> elementList(".tag-section")
    rulesCategoriesSectionElement.map(ruleTypeSectionElementToRuleType)
  }

  private def ruleTypeSectionElementToRuleType(ruleTypeElement: Element) = {
    val id = ruleTypeElement.attr("id")
    val rulesElements = ruleTypeElement >> elementList(".rule")
    RuleTypeElement(RuleCategory(id), rulesElements)
  }

  def getRules(ruleTypeElement: RuleTypeElement): List[Rule] = {
    ruleTypeElement.rulesElements.map(ruleFromElement(_, ruleTypeElement.category))
  }

  private def ruleFromElement(ruleElement: Element, ruleCategory: RuleCategory): Rule = {
    val ruleHeaderElement = ruleElement >> element("h3")
    val ruleDescriptionElement = ruleElement >> element(".description")

    val ruleId = ruleHeaderElement.attr("id").stripPrefix("rule-")
    val name = ruleHeaderElement.text.stripSuffix(ruleId)
    val description = ruleDescriptionElement.text

    Rule(ruleId, name, description, ruleCategory)
  }
}

object FauxPasDocumentationParser {
  def fromString(htmlContent: String): FauxPasDocumentationParser = {
    val htmlPage = JsoupBrowser()
    val document = htmlPage.parseString(htmlContent)
    new FauxPasDocumentationParser(document)
  }
}
