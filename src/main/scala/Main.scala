import laika.api.*
import laika.format.*
import laika.io.implicits.*
import laika.markdown.github.GitHubFlavor
import laika.parse.code.SyntaxHighlighting
import cats.effect.IO
import laika.helium.Helium
import laika.io.model.*
import laika.ast.Path.*
import laika.config.LaikaKeys
import laika.helium.config.*
import java.nio.file.Files
import mdoc.MainSettings
import java.nio.file.Paths
import java.nio.file.Path
import java.io.File
import cats.effect.kernel.Resource
import laika.config.*
import scala.io.Source
import cats.effect.*

object Main extends IOApp.Simple:
  val currentDirectory = new java.io.File(".").getCanonicalPath
  println(currentDirectory)

  val siteConfig: SiteConfig = SiteConfig(currentDirectory + "/.site.conf")


  // Get laika set up

  val theme = Helium.defaults.site
    .mainNavigation(depth = 3)
    .site
    .topNavigationBar(homeLink =
      IconLink
        .internal(
          Root / "index.md",
          HeliumIcon.home,
          text = siteConfig.hocon.getOpt[String]("site.title").getOrElse(None),
        ),
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

  // Set some files
  val inFile   = new File("./site")
  val mdocFile = new File("./site-mdoc")
  val outFile  = new File("./build")

  // Parse mdoc
  val settings: MainSettings = mdoc
    .MainSettings()
    .withIn(inFile.toPath())
    .withOut(mdocFile.toPath())
    .withCleanTarget(true)
  // Seems to be an issue here when using scala-cli package
  mdoc.Main.process(settings)

  // Ensure our output directory exists
  Files.createDirectories(outFile.toPath())

  val html =
    transformer.use {
      _.fromDirectory(FilePath.fromJavaFile(mdocFile))
        .toDirectory(FilePath.fromJavaFile(outFile))
        .transform
    }

  val run: IO[Unit] = html.as(())
