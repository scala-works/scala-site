import laika.api.*
import laika.io.implicits.*
import laika.format.*
import laika.helium.*
import laika.format.*
import cats.effect.*
import laika.markdown.github.GitHubFlavor
import laika.config.*
import laika.helium.config.*
import laika.ast.Path.Root
import laika.parse.code.SyntaxHighlighting
import laika.io.api.TreeTransformer
import java.io.File
import laika.io.model.FilePath
import laika.theme.ThemeProvider
import laika.io.model.RenderedTreeRoot
import java.nio.file.Files

final case class LaikaProcessor(appConfig: AppConfig):

  val theme: ThemeProvider = Helium.defaults.site
    .mainNavigation(depth = 3)
    .site
    .topNavigationBar(homeLink =
      IconLink
        .internal(
          Root / "index.md",
          HeliumIcon.home,
          text = appConfig.hocon.getOpt[String]("site.title").getOrElse(None),
        ),
    )
    .build

  val transformer: Resource[IO, TreeTransformer[IO]] =
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

  def process(inDir: File, outDir: File): IO[RenderedTreeRoot[cats.effect.IO]] =
    // Ensure our output directory exists
    Files.createDirectories(outDir.toPath())
    transformer.use {
      _.fromDirectory(FilePath.fromJavaFile(inDir))
        .toDirectory(FilePath.fromJavaFile(outDir))
        .transform
    }
