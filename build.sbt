ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.7"

lazy val root = (project in file("."))
  .settings(
    name := "real-time-task-processor",
    libraryDependencies ++= Seq(
    "io.monix" %% "monix" % "3.4.1",
    "io.circe" %% "circe-core" % "0.14.1",
    "io.circe" %% "circe-generic" % "0.14.1",
    "io.circe" %% "circe-parser" % "0.14.1",
    "io.udash" %% "udash-rest" % "0.9.0",
    "com.softwaremill.sttp.client3" %% "core" % "3.8.13",
    "org.scalatest" %% "scalatest" % "3.2.15" % Test
    )

  )
