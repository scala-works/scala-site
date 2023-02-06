import works.scala.cmd.UnitFlag
import works.scala.cmd.BooleanFlag

object MDocFlag extends BooleanFlag:

  override val name: String = "mdoc"

  override val shortKey: String = "m"

  override val description: String = "Pass this flag to enable mdoc processing."
