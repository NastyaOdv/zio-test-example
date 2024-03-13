ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"

lazy val root = (project in file("."))
  .settings(
    name := "zio-test-example"
  ).settings(
  Test / parallelExecution := false
)
libraryDependencies ++= Seq(
  "dev.zio"            %% "zio"                 % "2.0.13",
  "dev.zio"            %% "zio-http"            % "3.0.0-RC3",
  "dev.zio"            %% "zio-json"            % "0.5.0",
  "dev.zio"            %% "zio-config"          % "3.0.7",
  "dev.zio"            %% "zio-config-typesafe" % "3.0.7",
  "dev.zio"            %% "zio-config-magnolia" % "3.0.7",
  "dev.zio"            %% "zio-test"            % "2.0.20" % Test,
  "dev.zio"            %% "zio-test-sbt"        % "2.0.20" % Test,
  "dev.zio"            %% "zio-test-magnolia"   % "2.0.20" % Test,
  "org.scalatest"      %% "scalatest"           % "3.2.15" % Test,

  "dev.zio"            %% "zio-config-magnolia" % "3.0.7",

  "io.scalac"          %% "zio-slick-interop"   % "0.6.0",
  "com.typesafe.slick" %% "slick"               % "3.4.1",

  "com.h2database"     % "h2"                   % "2.1.214",

  "com.github.blemale" %% "scaffeine"           % "5.2.1",
  "org.flywaydb"       % "flyway-core"          % "9.16.0",
  "joda-time"          % "joda-time"            % "2.12.7"
)
testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")