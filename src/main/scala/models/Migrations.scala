package models

import com.typesafe.config.Config
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import org.flywaydb.core.api.output.{CleanResult, MigrateResult}

/**
 * A class to manage database migrations using Flyway.
 *
 * @param config The Typesafe Config object containing database connection configuration.
 */
class Migrations(config: Config) {

  /**
   * Flyway configuration object.
   */
  val flyway: FluentConfiguration = Flyway
    .configure()
    .validateMigrationNaming(true)
    .connectRetries(3)
    .connectRetriesInterval(3)
    .dataSource(
      config.getString("url"),
      config.getString("user"),
      config.getString("password")
    )
    .locations("classpath:/db")

  /**
   * Run database migrations.
   *
   * @return The result of migration process.
   */
  def run: MigrateResult = flyway.load().migrate()

  /**
   * Drop all tables in the database (clean).
   *
   * @return The result of clean process.
   */
  def dropTables: CleanResult = flyway.cleanDisabled(false).load().clean()
}
