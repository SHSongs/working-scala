import domain.{Product, Shop}
import youtube.{ShortContainer, Youtube, GeneralVideoContainer}
import zio.{Scope, ZIO, ZIOAppArgs, ZIOAppDefault}

object Main2 extends ZIOAppDefault {

  private val shop = Shop("상점1")
  private val product = Product("상품1", shop)


  override def run = for {
    _ <- zio.Console.printLine("안녕하세요")

    short1 = ShortContainer("i am short1", 100, "thumbnail1")
    short2 = ShortContainer("i am short2", 123, "thumbnail2")
    general = GeneralVideoContainer("iam general 1", 123, "thumbnail3", "hwibaski")
    youtube = Youtube(List(short1, short2, general))

    shortSumViewCount = youtube.containers.map {
      case ShortContainer(_, viewCount, _) => viewCount
      case _ => 0
    }.sum

    shortContainerCount = youtube.containers.collect{ case short: ShortContainer => short}
    shortCount =shortContainerCount.length

    _ <- zio.Console.printLine(s"총 조회수는 ${shortSumViewCount / shortCount} 입니다")

  } yield ()
}
