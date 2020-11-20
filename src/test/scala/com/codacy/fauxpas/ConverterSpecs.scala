package com.codacy.fauxpas

import java.nio.file.Paths

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class ConverterSpecs extends AnyWordSpec with Matchers {

  val fauxPasResults = List(
    FauxPasResult(
      FauxPasResult.Extent(FauxPasResult.Extent.Data(0, 0), FauxPasResult.Extent.Data(0, 0)),
      Some("/path/to/somewhere/where/an/issue/is/present"),
      "Required compiler argument -DNDEBUG is not used. This argument disables the C standard library assertion macro (as defined in assert.h).",
      "ReleaseBuildCompilerArgs",
      9
    ),
    FauxPasResult(
      FauxPasResult.Extent(FauxPasResult.Extent.Data(0, 0), FauxPasResult.Extent.Data(0, 0)),
      Some("/path/to/somewhere/where/an/issue/is/present"),
      "Required compiler argument -DNS_BLOCK_ASSERTIONS is not used. This argument disables Foundation assertion macros.",
      "ReleaseBuildCompilerArgs",
      9
    )
  )

  val expectedResults = {
    val stream = getClass.getResourceAsStream("/codacy_results.json")
    scala.io.Source.fromInputStream(stream).getLines.mkString
  }

  "Converter::convert" should {

    "convert faux pas parsed results into codacy results" in {

      val mockedReportParser = new FauxPasReportParser {
        override def parse(lines: Seq[String]): Seq[FauxPasResult] = fauxPasResults
      }

      val actualResults =
        new Converter("faux-pas", mockedReportParser).convert(Seq.empty, Paths.get("/path/to/somewhere/"))

      actualResults should contain theSameElementsAs expectedResults
    }

    "discard faux pas parsed results that do not contain a file field" in {

      val mockedReportParser = new FauxPasReportParser {
        override def parse(lines: Seq[String]): Seq[FauxPasResult] =
          fauxPasResults ++ fauxPasResults.map(_.copy(file = None))
      }

      val actualResults =
        new Converter("faux-pas", mockedReportParser).convert(Seq.empty, Paths.get("/path/to/somewhere/"))

      actualResults should contain theSameElementsAs expectedResults

    }

  }

}
