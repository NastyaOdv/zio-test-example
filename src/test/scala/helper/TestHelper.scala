package helper

import com.typesafe.config.{Config, ConfigFactory}
import models.Migrations
import slick.interop.zio.DatabaseProvider
import slick.jdbc.JdbcProfile
import zio.ZLayer
import zio.test.ZIOSpecDefault

trait TestHelper extends ZIOSpecDefault{

  val config: Config = ConfigFactory.load("application.test").getConfig("database")

  implicit val databaseProvider: ZLayer[Any, Throwable, DatabaseProvider] =
    (ZLayer.succeed(config) ++ ZLayer.succeed[JdbcProfile](
      slick.jdbc.H2Profile
    )) >>> DatabaseProvider.fromConfig()

  val migrations: Migrations = new Migrations(config)

}
