name := """d2"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

// routesGenerator := InjectedRoutesGenerator

libraryDependencies ++= Seq(
  ws,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

scalacOptions in ThisBuild ++= Seq(
  "-feature",
  "-language:postfixOps",
  "-Xfuture",
  "-language:_",
  "-deprecation",
  "-unchecked"
)