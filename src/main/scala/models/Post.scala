package models

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class Post(id: Option[Long], author: Long, title: String)

object Post {
  implicit val decoder: JsonDecoder[Post] = DeriveJsonDecoder.gen[Post]
  implicit val encoder: JsonEncoder[Post] = DeriveJsonEncoder.gen[Post]
}
