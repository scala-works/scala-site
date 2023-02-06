import java.io.File
import mdoc.MainSettings
final case class MdocProcessor(appConfig: AppConfig):

  def process(inDir: File, outDir: File): Int =
    val settings: MainSettings = mdoc
      .MainSettings()
      .withIn(inDir.toPath())
      .withOut(outDir.toPath())
      .withCleanTarget(true)
    mdoc.Main.process(settings)
