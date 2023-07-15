import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object Main2 extends ZIOAppDefault {
  override def run = for {
    _ <- zio.Console.printLine("안녕하세요")
  } yield ()
}
