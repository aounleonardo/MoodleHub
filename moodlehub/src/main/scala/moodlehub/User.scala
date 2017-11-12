package moodlehub

import scala.util.{Failure, Success}
import play.api.libs.json.{JsArray, JsValue}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class User(token: Token, moodleHubPath: Path = Path("/tmp/test")) {

  var enrolledCourses: Array[Course] = _
  val path = moodleHubPath

  FileManager.createDirectory(path)
  val siteInfo: Future[JsValue] = Client.getSiteInfo(token)

  siteInfo.onComplete {
    case Success(s) => processUserInfo(s)
    case Failure(e) => throw e
  }
  private def processUserInfo(value: JsValue): Unit = {
    val username = value("username").as[String]
    val userid = value("userid").as[Int]
    val userPath = path add username
    FileManager.createDirectory(userPath)


    val coursesInfo: Future[JsValue] = Client.getUsersCourses(userid)(token)

    coursesInfo.onComplete {
      case Success(s) => enrolledCourses = processCoursesInfo(s.as[Array[JsValue]], userPath)
      case Failure(e) => throw e
    }
  }



  private def processCoursesInfo(courses: Array[JsValue], userPath: Path): Array[Course] = {
    courses.map { course =>
      val shortname = course("shortname").as[String]
      val fullname = course("fullname").as[String]
      val courseId = course("id").as[Int]

      Course(s"${shortname}_$fullname", courseId)(token, userPath)
    }
  }
}

object User {
  def apply(token: Token = Token("6aca2ab143095b1e8498c6e8c3364898")): User = new User(token)
}
