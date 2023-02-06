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
import works.scala.cmd.Cmd
import works.scala.cmd.Flag
import cats.effect.unsafe.implicits.global

object Main extends Cmd:

  override def flags: Seq[Flag[?]] = Seq(MDocFlag)

  val currentDirectory = new java.io.File(".").getCanonicalPath
  println(currentDirectory)

  val appConfig: AppConfig = AppConfig(currentDirectory + "/.site.conf")
  val mdocProcessor        = MdocProcessor(appConfig)
  val laikaProcessor       = LaikaProcessor(appConfig)

  override def command(args: Array[String]): Unit =
    // Set some files
    val inDir      = new File("./site")
    val shouldMdoc = MDocFlag.isPresent(args)
    val stageDir   =
      if shouldMdoc then new File("./site-mdoc")
      else inDir
    val outDir     = new File("./build")
    if shouldMdoc then mdocProcessor.process(inDir, stageDir)
    laikaProcessor.process(stageDir, outDir).as(()).unsafeRunSync()
