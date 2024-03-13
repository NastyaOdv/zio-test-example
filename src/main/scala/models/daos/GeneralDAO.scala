package models.daos

import slick.dbio.DBIO
import slick.interop.zio.DatabaseProvider
import zio.{Task, ZIO, ZLayer}
import slick.interop.zio.syntax._

trait GeneralDAO[A] {
  def create(a: A): Task[A]
  def read(id: Long): Task[Option[A]]
  def update(a: A): Task[Int]
  def delete(id: Long): Task[Int]
  def runDBIO[R](action: DBIO[R], databaseProvider: ZLayer[Any, Throwable, DatabaseProvider]): Task[R] = ZIO.fromDBIO(action).provideLayer(databaseProvider)
}
