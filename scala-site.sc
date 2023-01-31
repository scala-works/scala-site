//> using scala "3.2.2"
//> using lib "org.scalameta::mdoc:2.3.7"
//> using lib "org.planet42::laika-core:0.19.0"
//> using lib "org.planet42::laika-io:0.19.0"
import laika.api.*
import laika.format.*
import laika.io.implicits.*
import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting
import cats.effect.IO
import laika.helium.Helium
import cats.effect.unsafe.implicits.global
import laika.io.model.*
import laika.ast.Path.*
import laika.config.LaikaKeys
import laika.helium.config.*
import java.nio.file.Files
import mdoc.MainSettings
import java.nio.file.Paths
import java.io.File
import cats.effect.kernel.Resource
import laika.config.*
import scala.io.Source

// Quick and dirty example of reading a config file
val hocon = Source.fromFile(".site.conf").mkString
val emptyConfig = ConfigBuilder.empty.build
val config = ConfigParser.parse(hocon).resolve().getOrElse(emptyConfig)

// Parse mdoc
val settings: MainSettings = mdoc
  .MainSettings()
  .withIn(Paths.get("site"))
  .withOut(Paths.get("site-mdoc"))
mdoc.Main.process(settings)

// Get laika set up

val theme = Helium.defaults.site
  .mainNavigation(depth = 3)
  .site
  .topNavigationBar(homeLink =
    IconLink
      .internal(Root / "index.md", HeliumIcon.home, text = config.getOpt[String]("site.title").getOrElse(None)),
  )
  .build

val transformer =
  Transformer
    .from(Markdown)
    .to(HTML)
    .using(
      GitHubFlavor,
      SyntaxHighlighting,
    )
    .withConfigValue(LaikaKeys.titleDocuments.inputName, "index")
    .sequential[IO]
    .withTheme(theme)
    .build

// Ensure our output directory exists
Files.createDirectories(Paths.get("build"))

val html =
  transformer.use {
    _.fromDirectory("site-mdoc")
      .toDirectory("build")
      .transform
  }

html.unsafeRunSync()
