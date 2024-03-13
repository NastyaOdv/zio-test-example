package controllers

import models.Post
import models.daos.PostDAO
import zio.ZIO
import zio.http.{HttpApp, Method, Request, Response, Routes, Status, handler, int}
import zio.json._

object PostController {
  val routes: HttpApp[PostDAO] = {
    Routes(
      Method.POST / "post" -> handler { (req: Request) =>
        val response = for {
          postRequest <- req.body.asString.map(_.fromJson[Post]).mapError(_ => "Invalid JSON in request body").debug
          post <- ZIO.fromOption(postRequest.toOption).orElseFail("Invalid post data").debug
          createdPost <- ZIO.serviceWithZIO[PostDAO](_.create(post)).debug
          jsonResponse <- ZIO.from(Response.json(createdPost.toJson).status(Status.Ok)).debug
        } yield jsonResponse
        response.catchAll(error => ZIO.fail(Response.error(Status.BadRequest, s"Invalid request: $error")))
      },

      Method.GET / "post" / int("id") -> handler { (id: Int, _: Request) =>
        val response = for {
          post <- ZIO.serviceWithZIO[PostDAO](_.read(id)).debug
          jsonResponse <- ZIO.from(Response.json(post.toJson).status(Status.Ok)).debug
        } yield jsonResponse
        response.catchAll(error => ZIO.fail(Response.error(Status.NotFound, error.toString)))
      },

      Method.DELETE / "post" / int("id") -> handler { (postId: Int, _: Request) =>
        val response = for {
          _ <- ZIO.serviceWithZIO[PostDAO](_.delete(postId)).debug
          jsonResponse <- ZIO.from(Response.status(Status.NoContent)).debug
        } yield jsonResponse
        response.catchAll(error => ZIO.fail(Response.error(Status.BadRequest, s"Invalid request: $error")))
      }

    ).toHttpApp
  }

}
