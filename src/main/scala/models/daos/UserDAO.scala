package models.daos

import models.{Post, User}
import models.tables.PostTable.posts
import models.tables.UserTable.users
import slick.interop.zio.DatabaseProvider
import slick.jdbc.H2Profile.api._
import zio.{Task, ZLayer}

import java.util.Date
import scala.concurrent.ExecutionContext.Implicits.global

class UserDAO(databaseProvider: ZLayer[Any, Throwable, DatabaseProvider]) extends GeneralDAO[User]{
  /**
   * Creates a new user in the database.
   *
   * @param newUser The user to be created.
   * @return A ZIO effect representing the creation of the user.
   */
  def create(newUser: User): Task[User] = {
    val createUserAction = users returning users.map(_.id) into ((user, id) => user.copy(id = Some(id))) += newUser
    runDBIO(createUserAction, databaseProvider)
  }

  /**
   * Retrieves a user from the database based on their ID.
   *
   * @param id The ID of the user to retrieve.
   * @return A ZIO effect representing the retrieval of the user, wrapped in an Option.
   */
  def read(id: Long): Task[Option[User]] = {
    val getUserAction = users.filter(_.id === id).result.headOption
    runDBIO(getUserAction, databaseProvider)
  }

  /**
   * Updates an existing user in the database.
   *
   * @param user The user with updated information.
   * @return A ZIO effect representing the update operation, returning the number of affected rows.
   */
  def update(user: User): Task[Int] = {
    val updateUserAction = users.filter(_.id === user.id.get).update(user)
    runDBIO(updateUserAction, databaseProvider)
  }

  /**
   * Retrieves a user along with their associated posts from the database.
   *
   * @param userId The ID of the user for whom posts are to be retrieved.
   * @return A ZIO effect representing the retrieval of the user and associated posts.
   */
  def getUser2Post(userId: Long): Task[(User, Seq[Post])] = {
    val query = for {
      (user, post) <- users join posts on (_.id === _.userId) filter (_._1.id === userId)
    } yield (user, post)

    runDBIO(query.result, databaseProvider).map { result =>
      val groupedResults = result.groupBy(_._1).map { case (user, userPosts) =>
        (user, userPosts.map(_._2))
      }
      groupedResults.headOption.getOrElse((User(None, "", "", new Date()), Seq.empty[Post]))
    }
  }

  /**
   * Deletes a user from the database based on their ID along with associated posts.
   *
   * @param userId The ID of the user to delete.
   * @return A ZIO effect representing the deletion operation, returning the number of affected rows.
   */
  def delete(userId: Long): Task[Int] = {
    val deleteAction = for {
      _ <- posts.filter(_.userId === userId).delete
      usersDeleted <- users.filter(_.id === userId).delete
    } yield usersDeleted

    runDBIO(deleteAction.transactionally, databaseProvider)
  }

  /**
   * Deletes all posts and users from the database.
   *
   * @return A ZIO Task representing the number of users deleted.
   */
  def deleteAll():Task[Int] = {
    val deleteAllAction = for {
      _ <- posts.delete
      usersDeleted <- users.delete
    } yield usersDeleted
    runDBIO(deleteAllAction, databaseProvider)
  }

}
object UserDAO {
  def live(dp: ZLayer[Any, Throwable, DatabaseProvider]): ZLayer[DatabaseProvider, Throwable, UserDAO] =
    ZLayer.succeed(new UserDAO(dp))
}