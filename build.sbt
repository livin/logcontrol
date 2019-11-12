name := """logcontrol"""
organization := "ru.livesystems"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.2"

libraryDependencies += guice

libraryDependencies ++= Seq(
  "commons-io" % "commons-io" % "2.0.1",
  "org.webjars" % "bootstrap" % "3.3.7-1",
  "org.webjars" % "jquery-throttle-debounce-plugin" % "1.1",
  "org.webjars.npm" % "vue" % "2.4.2",
  "org.mockito" % "mockito-core" % "2.8.47"
)

sources in doc in Compile := List()

herokuAppName in Compile := "shrouded-refuge-84614"

