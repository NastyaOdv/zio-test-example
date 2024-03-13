package services

import models.User
import org.joda.time.DateTime
import zio.ZIO
import zio.test.Assertion.equalTo
import zio.test.{Spec, ZIOSpecDefault, assert}


object DOBServiceSpec extends ZIOSpecDefault {
  val nowDate: DateTime = DateTime.now()

  def test1: Spec[DOBService, Nothing] = suite("test1")(test("calculates days to birthday correctly for upcoming birthday") {
    val expectedDays = 1
    val dob = new DateTime(2002, nowDate.monthOfYear().get(), nowDate.dayOfMonth().get(), 0, 0).plusDays(expectedDays)
    val user = User(Some(1L), "name", "email", dob.toDate)
    for {
      daysToBirth <- ZIO.serviceWithZIO[DOBService](_.getDaysToBirth(user))
    } yield assert(daysToBirth)(equalTo(expectedDays.toLong))
  })

  def test2: Spec[DOBService, Nothing] = suite("test2")(test("calculates days to birthday for " +
    "next year's birthday if already passed") {
    val expectedDays = 12
    val dob = new DateTime(2002, nowDate.monthOfYear().get(), nowDate.dayOfMonth().get(), 0, 0).minusDays(expectedDays)
    val user = User(Some(1L), "name", "email", dob.toDate)
    val days = (if (nowDate.year().isLeap) 365 else 366) - expectedDays
    for {
      daysToBirth <- ZIO.serviceWithZIO[DOBService](_.getDaysToBirth(user))
    } yield assert(daysToBirth)(equalTo(days.toLong))
  })

  def test3: Spec[DOBService, Nothing] = suite("test3")(test("returns 0 for birthday today") {
    val dob = new DateTime(2002, nowDate.monthOfYear().get(), nowDate.dayOfMonth().get(), 0, 0)
    val user = User(Some(1L), "name", "email", dob.toDate)
    for {
      daysToBirth <- ZIO.serviceWithZIO[DOBService](_.getDaysToBirth(user))
    } yield assert(daysToBirth)(equalTo(0L))
  })

  def spec = suite("UserDAO")(
    test1,
    test2,
    test3
  ).provideLayerShared(DOBService.live)

}
