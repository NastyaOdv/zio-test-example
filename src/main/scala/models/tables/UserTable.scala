package models.tables

import models.User
import slick.jdbc.H2Profile.api._
import slick.lifted.Tag

import java.text.SimpleDateFormat
import java.util.Date

class UserTable(tag: Tag) extends Table[User](tag, "User") {
  implicit val dateColumnType: BaseColumnType[Date] = MappedColumnType.base[Date, String](
    date => new SimpleDateFormat("yyyy-MM-dd").format(date),
    millis => new SimpleDateFormat("yyyy-MM-dd").parse(millis)
  )

  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def name = column[String]("name")

  def email = column[String]("email")

  def dateOfBirth = column[Date]("dateOfBirth")

  def * = (id.?,name, email, dateOfBirth) <> ((User.apply _).tupled, User.unapply)
}

object UserTable {
  val users = TableQuery[UserTable]
}
