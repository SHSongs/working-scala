/** json 파일에서 친구들의 평균나이을 구해주는 스크립트 입니다.
  * 해당 파일은 fixture 폴더에 있습니다.
  * fixture의 파일 내용  친구 관련 json 외 동물이나 과일같은 json 파일도 있습니다. (혹은 csv, txt, sql등 다양한 파일이 있을 수도 있습니다)
  * 친구관련 json 파일만 읽어서 친구들의 평균나이을 구해주세요.
  */

import FriendsAges.SimpleReport.{FailGenerateReport, SuccessGenerateReport}
import ujson.Value.Value
import zio._

import java.io.Serializable

// ADT는 enum의 상위 개념입니다. https://blog.rockthejvm.com/algebraic-data-types/
// 에러 타입을 정의하였습니다.
abstract class SimpleError(message: String = "", cause: Throwable = null)
    extends Throwable(message, cause)
    with Product
    with Serializable
object SimpleError {
  final case class ReadFail(cause: Throwable)
      extends SimpleError(s"read fail: ", cause)

  final case class FriendPropertyNotFound(fileName: String)
      extends SimpleError(s"friend property not found in ${fileName}", null)
}

object FriendsAges extends ZIOAppDefault {

  // python처럼 쉽게 파일을 읽을 수 있는 라이브러리 https://github.com/com-lihaoyi/os-lib
  val path = os.pwd / "fixture"
  // map(x => x.last), list : 리스트의 마지막 요소 반환
  val fileNames = ZIO.attempt(os.list(path).map(_.last))

  private def extractsAgeValuesFromJson(json: Value) = {
    // 내 생각엔 json.obj.get("friends").map(el => el.get("age")) 하면 바로 추출할 수 있을 것 같았는데 생각처럼 안됐다.
    // Optional 같은 게 있는 것 같은데 공부 필요..
    json.obj.get("friends").map(_.arr.map(el2 => el2.obj("age").num.toInt)).get
  }


  private def filterJsonWithFriendProperty(json: Value, fileName: String): ZIO[Any, SimpleError, Value] =
    for {
      _ <-
        ZIO.unless(json.obj.contains("friends"))(
          ZIO.fail(SimpleError.FriendPropertyNotFound(fileName))
        )
    } yield json

  // python처럼 json을 쉽게 다룰 수 있는 라이브러리 https://www.lihaoyi.com/post/uJsonfastflexibleandintuitiveJSONforScala.html
  def readJson(name: String): ZIO[Any, SimpleError, Value] =
    for {
      _ <- Console.printLine(s"read ${name}").ignore
      json <- ZIO
        .attempt(ujson.read(os.read(path / s"$name")))
        .catchAll(cause => ZIO.fail(SimpleError.ReadFail(cause)))
      filtered <- filterJsonWithFriendProperty(json, name)
      _ <- Console.printLine(s"$json").ignore
    } yield filtered

  // ADT는 enum의 상위 개념입니다. https://blog.rockthejvm.com/algebraic-data-types/
  sealed trait SimpleReport extends Product with Serializable
  object SimpleReport {
    case class FailGenerateReport(cause: SimpleError) extends SimpleReport
    case class SuccessGenerateReport(message: String, json: Value)
        extends SimpleReport
  }

  // 프로그램 시작점
  override def run = for {
    // run! 출력
    _ <- Console.printLine("run!")

    // "fixture" 디렉토리에 있는 파일 리스트 출력
    // ArraySeq(hello.txt, hello_b.txt, wst.txt, zcxv.sql, zzzx3tfsru.txt)
    names <- fileNames
    _ <- Console.printLine(s"files ${names}")

    reports <- ZIO.foreach(names) { name =>
      (
        for {
          json <- readJson(name)
        } yield SuccessGenerateReport(s"뭔지는 모르겠지만 json 입니다. ${json}", json)
      ).catchAll(e => ZIO.succeed(FailGenerateReport(e)))
    }

    _ <- Console.printLine(s"-------------")
    _ <- Console.printLine(s"-------------")
    _ <- Console.printLine(s"-------------")
    _ <- Console.printLine(s"-------------")
    _ <- Console.printLine(s"-------------")
    _ <- Console.printLine(s"-------------")
    _ <- Console.printLine(s"-------------")
    _ <- Console.printLine(s"-------------")

    // 패턴 매칭은 정적 타입 언어에서 사용할 수 있는 강력한 도구입니다. https://docs.scala-lang.org/ko/tour/pattern-matching.html
    _ <- ZIO.foreachDiscard(reports) { report =>
      report match {
        case FailGenerateReport(error) =>
          Console.printLine(s"생성 실패한 리포트 입니다: ${error}")
        case SuccessGenerateReport(message, json) =>
          val ages = extractsAgeValuesFromJson(json)
          println(s"평균 나이 : ${ages.sum / ages.length}")
          Console.printLine(s"생성 성공한 리포트 입니다: ${message}")
      }
    }
  } yield ()
}

// 스칼라 문법도 아직 익숙하지 않은데.. ZIO까지 들어오니 굉장히 헷갈린다...
// ZIO[R, E, A]에 대한 공부가 더 필요할 것 같다.. 문서에서 대충 확인은 했는데 막상 코드로 부딪히니 제대로 파악이 덜 된 것 같드.
