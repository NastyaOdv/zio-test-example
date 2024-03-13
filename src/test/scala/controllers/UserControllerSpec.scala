package controllers

import helper.TestHelper
import models.daos.UserDAO
import models.User
import services.DOBService
import slick.interop.zio.DatabaseProvider
import zio.http.{Body, Path, Request, Status, URL}
import zio.json.DecoderOps
import zio.test.Assertion._
import zio.test.TestAspect.{afterAll, beforeAll, sequential}
import zio.test._
import zio.{ZIO, ZLayer}

import java.util.Date

object UserControllerSpec extends TestHelper{

  def test1: Spec[UserDAO with DOBService, Serializable] = suite("suite 1")(test("create user returned user ") {
    val request = Request.post(URL(Path("/user")),
      Body.fromString("""{"name": "John Doe", "email": "john.doe2@example.com", "dateOfBirth":"2002-02-02"}"""))
    for {
      response <- UserController.routes.runZIO(request)
      userRes <- response.body.asString.map(_.fromJson[User]).debug
      user <- ZIO.fromOption(userRes.toOption)
    } yield assert(response.status)(equalTo(Status.Ok)) &&
      assert(user.email)(equalTo("john.doe2@example.com"))
  })

  def test2: Spec[UserDAO with DOBService, Nothing] = suite("suite 2")(test("wrong request") {
    val request = Request.post(URL(Path("/user")),
      Body.fromString("""{"firstName": "John Doe", "email": "john.doe2@example.com", "dateOfBirth":"2002-02-02"}"""))
    for {
      response <- UserController.routes.runZIO(request)
    } yield assert(response.status)(equalTo(Status.BadRequest)) &&
      assert(response.headers.get("warning").get)(equalTo("400 ZIO HTTP Invalid request: Invalid user data"))
  })

  def test3: Spec[UserDAO with DOBService, Serializable] = suite("suite 3")(test("2get request user by id") {
    for {
      createdUser <- ZIO.serviceWithZIO[UserDAO](_.create(User(None, "john", "john@example.com", new Date(1999, 1, 1))))
      response <- UserController.routes.runZIO(Request.get(URL(Path(s"/user/${createdUser.id.get}"))))
      userRes <- response.body.asString.map(_.fromJson[User]).debug
      user <- ZIO.fromOption(userRes.toOption)
    } yield assert(response.status)(equalTo(Status.Ok)) &&
      assert(user.email)(equalTo("john@example.com"))
  })

  val testLayer: ZLayer[Any, Throwable, DatabaseProvider with UserDAO with DOBService] =
    ZLayer.make[DatabaseProvider with UserDAO with DOBService](databaseProvider, UserDAO.live(databaseProvider), DOBService.live)

  def spec: Spec[TestEnvironment, Serializable] = suite("UserController")(
    test1,
    test2,
    test3
  ).provideLayerShared(testLayer) @@ beforeAll(ZIO.from(migrations.run)) @@ sequential @@afterAll(ZIO.succeed(migrations.dropTables))

}
