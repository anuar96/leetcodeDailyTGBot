ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"


val circeVersion = "0.14.1"

lazy val root = (project in file("."))
  .settings(
    libraryDependencies += "io.github.apimorphism" %% "telegramium-core" % "7.63.0",
    libraryDependencies += "io.github.apimorphism" %% "telegramium-high" % "7.63.0",
    libraryDependencies += "com.softwaremill.sttp.tapir" %% "tapir-sttp-client" % "1.2.4",
    libraryDependencies += "io.circe" %% "circe-optics" % circeVersion,
    libraryDependencies ++= Seq(
      "eu.timepit" %% "refined" % "0.10.1",
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion),
    name := "leetcodeDailyTGBot"
  )
