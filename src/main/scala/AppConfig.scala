import scala.io.Source
import scala.util.*
import laika.config.*
import scala.util.Using.Manager.Resource
import java.io.File

final case class AppConfig(configPath: String):

  lazy val hocon: Config =
    new File(configPath).createNewFile()
    Using.resource(Source.fromFile(configPath)) { buff =>
      ConfigParser
        .parse(buff.mkString)
        .resolve()
        .getOrElse(AppConfig.emptyConfig)
    }

object AppConfig:
  val emptyConfig: Config = ConfigBuilder.empty.build
