import zio.ZIOAppDefault

object Main extends ZIOAppDefault {
  override def run = for {
    _ <- zio.Console.printLine("안녕하세요")

    shorts1 = ShortsContainer(Video("https://...", "T1", 3000, 20))
    shorts2 = ShortsContainer(Video("https://...", "T2", 3500, 5))
    shorts3 = ShortsContainer(Video("https://...", "T3", 2000, 10))
    nomal1 = NormalContainer(Video("https://...", "T4", 2000, 10))
    youtube = Youtube(List(shorts1, shorts2, shorts3, nomal1))

    // map을 쓸때는 모든 경우의 수를 입력해줘야 함
    shortsList = youtube.containers.map(
      Container => Container match {
        case ShortsContainer(vData) => vData.viewCount
      }
    )

    sumCount = shortsList.sum
    shortsCount = shortsList.count(s => true)
    avgCount = sumCount / shortsCount

    _ <- zio.Console.printLine(youtube)
    _ <- zio.Console.printLine(s"youtube 총 조회수는 $sumCount 입니다")
    _ <- zio.Console.printLine(s"youtube 평균 조회수는 $avgCount 입니다")

  } yield ()
}

sealed  class Container
case class Video(videoUri: String, title: String, viewCount: Long, sTime: Long)
case class NormalContainer(vData: Video) extends Container
case class ShortsContainer(vData: Video) extends Container
case class Youtube(containers: List[Container])

