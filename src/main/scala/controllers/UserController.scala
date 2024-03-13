package controllers

import models.User
import models.daos.UserDAO
import services.DOBService
import zio.ZIO
import zio.http.{HttpApp, Method, Request, Response, Routes, Status, handler, int}
import zio.json._

object UserController {
  val routes: HttpApp[UserDAO with DOBService] = {
    Routes(
      Method.POST / "user" -> handler { (req: Request) =>
        val response = for {
          userRequest <- req.body.asString.map(_.fromJson[User]).mapError(_ => "Invalid JSON in request body")
          user <- ZIO.fromOption(userRequest.toOption).orElseFail("Invalid user data")
          createdUser <- ZIO.serviceWithZIO[UserDAO](_.create(user)).debug
          jsonResponse <- ZIO.from(Response.json(createdUser.toJson).status(Status.Ok)).debug
        } yield jsonResponse
        response.catchAll(error => ZIO.fail(Response.error(Status.BadRequest, s"Invalid request: $error")))
      },

      Method.GET / "user" / int("id") -> handler { (id: Int, _: Request) =>
        val response = for {
          user <- ZIO.serviceWithZIO[UserDAO](_.read(id)).debug
          jsonResponse <- ZIO.from(Response.json(user.toJson).status(Status.Ok)).debug
        } yield jsonResponse
        response.catchAll(error => ZIO.fail(Response.error(Status.BadRequest, s"Invalid request: $error")))
      },

      Method.GET / "user" / int("id") / "posts" -> handler { (id: Int, _: Request) =>
        val response = for {
          userAndPosts <- ZIO.serviceWithZIO[UserDAO](_.getUser2Post(id)).debug
          jsonResponse <- userAndPosts match {
            case (user, posts) if user.id.isDefined => ZIO.from(Response.json((user, posts).toJson).status(Status.Ok)).debug
            case (user, _) if user.id.isEmpty => ZIO.from(Response.status(Status.NoContent))
          }
        } yield jsonResponse
        response.catchAll(error => ZIO.fail(Response.error(Status.BadRequest, s"Invalid request: $error")))
      },

      Method.DELETE / "user" / int("id") -> handler { (userId: Int, _: Request) =>
        val response = for {
          _ <- ZIO.serviceWithZIO[UserDAO](_.delete(userId)).debug
          jsonResponse <- ZIO.from(Response.status(Status.NoContent)).debug
        } yield jsonResponse

        response.catchAll(error => ZIO.fail(Response.error(Status.BadRequest, s"Invalid request: $error")))
      },

      Method.PUT / "user" / int("id") -> handler { (userId: Int, req: Request) =>
        val response = for {
          userRequest <- req.body.asString.map(_.fromJson[User]).mapError(_ => "Invalid JSON in request body").debug
          user <- ZIO.fromOption(userRequest.toOption).orElseFail("Invalid user data").debug
          updatedUser <- ZIO.serviceWithZIO[UserDAO](_.update(user.copy(id = Option(userId.toLong)))).debug
          jsonResponse <- updatedUser match {
            case 0 => ZIO.from(Response.status(Status.NoContent))
            case _ => ZIO.from(Response.json(user.copy(id = Option(userId.toLong)).toJson).status(Status.Ok)).debug
          }
        } yield jsonResponse
        response.catchAll(error => ZIO.fail(Response.error(Status.BadRequest, s"Invalid request: $error")))
      },

      Method.GET / "user" / int("id") / "days" -> handler { (id: Int, _: Request) =>
        val response = for {
          user <- ZIO.serviceWithZIO[UserDAO](_.read(id.toLong)).debug
          days <- if (user.nonEmpty) {
            ZIO.serviceWithZIO[DOBService](_.getDaysToBirth(user.get)).debug
          } else {
            ZIO.succeed(None)
          }
          jsonResponse <- ZIO.from(
            days match {
              case Some(days) => Response.json(days.toString).status(Status.Ok)
              case None => Response.status(Status.NoContent)
            }
          ).debug
        } yield jsonResponse
        response.catchAll(error => ZIO.fail(Response.error(Status.BadRequest, s"Invalid request: $error")))
      },

    ).toHttpApp
  }

}
