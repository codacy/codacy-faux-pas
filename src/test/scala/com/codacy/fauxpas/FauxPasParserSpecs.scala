package com.codacy.fauxpas

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import java.nio.file.{Path, Paths}

class FauxPasParserSpecs extends AnyWordSpec with Matchers {
  val pwd: Path = Paths.get("/src")

  val successResult = List(
    FauxPasResult(
      FauxPasResult.Extent(FauxPasResult.Extent.Data(0, 0), FauxPasResult.Extent.Data(0, 0)),
      None,
      Some(
        "Required compiler argument -DNDEBUG is not used. This argument disables the C standard library assertion macro (as defined in assert.h)."
      ),
      "ReleaseBuildCompilerArgs",
      9
    ),
    FauxPasResult(
      FauxPasResult.Extent(FauxPasResult.Extent.Data(0, 0), FauxPasResult.Extent.Data(0, 0)),
      None,
      Some(
        "Required compiler argument -DNS_BLOCK_ASSERTIONS is not used. This argument disables Foundation assertion macros."
      ),
      "ReleaseBuildCompilerArgs",
      9
    )
  )

  "ReportParser::parse" should {
    "have results for a clean and valid sample output" in {
      //ARRANGE
      val stream = getClass.getResourceAsStream("/clean_output.json")
      val lines = scala.io.Source.fromInputStream(stream).getLines.to(LazyList)

      val expectedResult = successResult

      //ACT
      val actualResult = new FauxPasReportParser().parse(lines)

      //ASSERT
      actualResult should contain theSameElementsAs expectedResult
    }

    "have results for a polluted (extra faux pas output) and valid sample output" in {
      //ARRANGE
      val stream = getClass.getResourceAsStream("/polluted_output.json")
      val lines = scala.io.Source.fromInputStream(stream).getLines.to(LazyList)

      val expectedResult = successResult

      //ACT
      val actualResult = new FauxPasReportParser().parse(lines)

      //ASSERT
      actualResult should contain theSameElementsAs expectedResult
    }

    "have no results for an invalid sample output" in {
      //ARRANGE
      val lines = List("this is not a faux pas report", "omelete du fromage", "baguette")

      //ACT
      val actualResult = new FauxPasReportParser().parse(lines)

      //ASSERT
      actualResult should have size 0
    }

    "have no results for a clean sample output with no issues found" in {
      //ARRANGE
      val lines = List("""{ "diagnostics": []}""")

      //ACT
      val actualResult = new FauxPasReportParser().parse(lines)

      //ASSERT
      actualResult should have size 0
    }
  }

}
