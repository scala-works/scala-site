import scala.io.Source
import scala.util.*
import laika.config.*

// Laike has a SiteConfig, so this should be refactored
final case class SiteConfig(configPath: String):

  // This needs to be a Try/Either
  lazy val hocon: Config =
    Using.resource(Source.fromFile(configPath)) { buff =>
      ConfigParser
        .parse(buff.mkString)
        .resolve()
        .getOrElse(SiteConfig.emptyConfig)
    }

object SiteConfig:
  val emptyConfig: Config = ConfigBuilder.empty.build
