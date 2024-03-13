package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

import java.text.SimpleDateFormat
import java.util.Date

case class User(id: Option[Long], name: String, email: String, dateOfBirth: Date)

object User {
  implicit val localDateDecoder: JsonDecoder[Date] =
    JsonDecoder[String].map(str => new SimpleDateFormat("yyyy-MM-dd").parse(str))


  implicit val localDateEncoder: JsonEncoder[Date] =
    JsonEncoder[String].contramap(date => new SimpleDateFormat("yyyy-MM-dd").format(date))

  implicit val decoder: JsonDecoder[User] = DeriveJsonDecoder.gen[User]
  implicit val encoder: JsonEncoder[User] = DeriveJsonEncoder.gen[User]
}
