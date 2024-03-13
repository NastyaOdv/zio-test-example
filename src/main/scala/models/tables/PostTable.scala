package models.tables

import models.Post
import slick.jdbc.H2Profile.api._
import slick.lifted.Tag

class PostTable(tag: Tag) extends Table[Post](tag, "Post") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

  def userId = column[Long]("userId")

  def title = column[String]("title")

  // Foreign key relationship with User table
  def author = foreignKey("author_fk", userId, UserTable.users)(_.id)

  // Projection
  def * = (id.?, userId, title) <> ((Post.apply _).tupled, Post.unapply)

}

object PostTable {
  val posts: TableQuery[PostTable] = TableQuery[PostTable]
}
