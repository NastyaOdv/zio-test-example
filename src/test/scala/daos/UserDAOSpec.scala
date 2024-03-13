package daos

import helper.TestHelper
import models.User
import models.daos.UserDAO
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import zio._
import zio.prelude.data.Optional.AllValuesAreNullable
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

import java.util.Date


object UserDAOSpec extends TestHelper {

  def test1: Spec[UserDAO, Throwable] = suite("test1")(test("create user returned user ") {
    val user = new User(None, "test", "testTest@gmail.com", new Date(2002, 2, 2))
    for {
      user <- ZIO.serviceWithZIO[UserDAO](_.create(user))
    } yield assert(user.nonEmpty)(equalTo(true))
  })

  def test2: Spec[UserDAO, Nothing] = suite("test2")(test("create user with same email") {
    val user = new User(None, "test", "testTest@gmail.com", new Date(2002, 2, 2))
    val createTask = ZIO.serviceWithZIO[UserDAO](_.create(user))
    val value = for {
      user <- createTask
    } yield user
    assertZIO(value.exit)(fails(isSubtype[JdbcSQLIntegrityConstraintViolationException](anything)))
  })

  def test3: Spec[UserDAO, Throwable] = suite("test3")(test("read user with right id") {
    val user = new User(None, "test", "testForTest@gmail.com", new Date(2002, 2, 2))
    for {
      createdUser <- ZIO.serviceWithZIO[UserDAO](_.create(user))
      readUser <- ZIO.serviceWithZIO[UserDAO](_.read(createdUser.id.get))
    } yield assert(readUser.get.email)(equalTo(user.email))
  })

  def test4: Spec[UserDAO, Throwable] = suite("test4")(test("read user with wrong id") {
    val value = for {
      readUser <- ZIO.serviceWithZIO[UserDAO](_.read(1000))
    } yield readUser
    assertZIO(value)(isNone)
  })

  def spec: Spec[Any, Throwable] = suite("UserDAO")(
    test1,
    test2,
    test3
  ).provideLayerShared(databaseProvider >>> UserDAO.live(databaseProvider)) @@ beforeAll(ZIO.from(migrations.run))  @@ sequential @@afterAll(ZIO.succeed(migrations.dropTables))
}
