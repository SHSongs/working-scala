import domain.{Product, Shop}
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object Main2 extends ZIOAppDefault {

  private val shop = Shop("상점1")
  private val product = Product("상품1", shop)

  product.shop.name

  override def run = for {
    _ <- zio.Console.printLine("안녕하세요")
    _ <- zio.Console.printLine(product.shop.name)
  } yield ()
}
