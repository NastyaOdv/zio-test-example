import controllers.{PostController, UserController}
import models.daos.{PostDAO, UserDAO}
import slick.interop.zio.DatabaseProvider
import slick.jdbc.JdbcProfile
import zio.http._
import zio.{ZIO, ZIOAppDefault, ZLayer}
import com.typesafe.config.ConfigFactory
import models.Migrations
import services.DOBService

object Main extends ZIOAppDefault {
  // Load database configuration from the application.conf file
  private val config = ConfigFactory.load().getConfig("database")

  private val databaseProvider: ZLayer[Any, Throwable, DatabaseProvider] =
    (ZLayer.succeed(config) ++ ZLayer.succeed[JdbcProfile](
      slick.jdbc.H2Profile
    )) >>> DatabaseProvider.fromConfig()


  override val run: ZIO[Any, Throwable, Unit] =
    for {
      _ <- ZIO.succeed(new Migrations(config).run) // Execute migrations
      _ <- Server.serve(
        UserController.routes ++
          PostController.routes
      ).provide(
        databaseProvider,
        UserDAO.live(databaseProvider),
        PostDAO.live(databaseProvider),
        DOBService.live,
        Server.default
      )
    } yield ()

}
