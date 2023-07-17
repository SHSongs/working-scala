package youtube

case class Youtube(containers: List[Container])

sealed trait Container {
  val viewCount: Int
  val thumbnail: String
  val title: String
}
case class ShortContainer(title: String, viewCount: Int, thumbnail: String) extends Container
case class GeneralVideoContainer(title: String, viewCount: Int, thumbnail: String, creator: String) extends Container
