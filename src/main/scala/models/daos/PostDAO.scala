package models.daos

import models.Post
import models.tables.PostTable.posts
import slick.interop.zio.DatabaseProvider
import slick.jdbc.H2Profile.api._
import zio.{Task, ZLayer}

class PostDAO(databaseProvider: ZLayer[Any, Throwable, DatabaseProvider]) extends GeneralDAO[Post]{
  /**
   * Creates a new post in the database.
   *
   * @param newPost The post to be created.
   * @return A ZIO effect representing the creation of the post.
   */
  def create(newPost: Post): Task[Post] = {
    val createUserAction = posts returning posts.map(_.id) into ((user, id) => user.copy(id = Some(id))) += newPost
    runDBIO(createUserAction, databaseProvider)
  }

  /**
   * Reads a post from the database based on its ID.
   *
   * @param id The ID of the post to retrieve.
   * @return A ZIO effect representing the retrieval of the post, wrapped in an Option.
   */

  def read(id: Long): Task[Option[Post]] = {
    val getUserAction = posts.filter(_.id === id).result.headOption
    runDBIO(getUserAction, databaseProvider)
  }

  /**
   * Updates an existing post in the database.
   *
   * @param post The post with updated information.
   * @return A ZIO effect representing the update operation, returning the number of affected rows.
   */
  def update(post: Post): Task[Int] = {
    val updateUserAction = posts.filter(_.id === post.id.get).update(post)
    runDBIO(updateUserAction, databaseProvider)
  }

  /**
   * Deletes a post from the database based on its ID.
   *
   * @param id The ID of the post to delete.
   * @return A ZIO effect representing the deletion operation, returning the number of affected rows.
   */
  def delete(id: Long): Task[Int] = {
    val deleteAction =  posts.filter(_.id === id).delete
    runDBIO(deleteAction, databaseProvider)
  }

  /**
   * Deletes all posts from the database.
   *
   * @return A ZIO Task representing the number of posts deleted.
   */
  def deleteAll():Task[Int] = {
    val deleteAllAction = posts.delete
    runDBIO(deleteAllAction, databaseProvider)
  }

}

object PostDAO {
  def live(dp: ZLayer[Any, Throwable, DatabaseProvider]): ZLayer[DatabaseProvider, Throwable, PostDAO] =
    ZLayer.succeed(new PostDAO(dp))
}