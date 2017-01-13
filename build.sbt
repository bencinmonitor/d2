name := """d2"""

version := "1.0-SNAPSHOT"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "google-sedis-fix" at "http://pk11-scratch.googlecode.com/svn/trunk"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

// routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  ws,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14",
  "com.typesafe.play.modules" %% "play-modules-redis" % "2.5.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test"
)


scalacOptions in ThisBuild ++= Seq(
  "-feature",
  "-language:postfixOps",
  "-Xfuture",
  "-language:_",
  "-deprecation",
  "-unchecked"
)