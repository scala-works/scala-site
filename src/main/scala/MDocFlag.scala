import works.scala.cmd.UnitFlag

object MDocFlag extends UnitFlag {

  override val name: String = "mdoc"

  override val shortKey: String = "m"

  override val description: String = "Pass this flag to enable mdoc processing."

}
