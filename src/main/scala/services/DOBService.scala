package services

import models.User
import zio.{ZIO, ZLayer}

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Calendar

class DOBService() {

  /**
   * Calculates the number of days until the next birthday for the given user.
   *
   * @param user The user for whom to calculate the days until the next birthday.
   * @return A ZIO effect containing the number of days until the next birthday.
   */
  def getDaysToBirth(user: User): ZIO[Any, Nothing, Long] = {
    val today = LocalDate.now()
    val cal = Calendar.getInstance()
    cal.setTime(user.dateOfBirth)
    val nextBirthday = LocalDate.of(today.getYear, cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
    val daysUntilBirthday = ChronoUnit.DAYS.between(today, nextBirthday)
    if (daysUntilBirthday >= 0) {
      ZIO.succeed(daysUntilBirthday)
    } else {
      val nextYearBirthday = LocalDate.of(today.getYear + 1, cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH))
      val daysUntilNextYearBirthday = ChronoUnit.DAYS.between(today, nextYearBirthday)
      ZIO.succeed(daysUntilNextYearBirthday)
    }
  }

}

object DOBService {
  val live: ZLayer[Any, Nothing, DOBService] = ZLayer.succeed(new DOBService())
}
