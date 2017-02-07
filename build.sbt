// import com.arpnetworking.sbt.typescript.Import.TypescriptKeys

name := """d2"""

version := "1.0-SNAPSHOT"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
resolvers += "google-sedis-fix" at "http://pk11-scratch.googlecode.com/svn/trunk"
resolvers += "Madoushi sbt-plugins" at "https://dl.bintray.com/madoushi/sbt-plugins/"
resolvers += Resolver.typesafeRepo("releases")
resolvers += Resolver.sbtPluginRepo("releases")
resolvers += Resolver.sonatypeRepo("releases")

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

val playVersion = "2.5.0"

libraryDependencies ++= Seq(
  ws,
  filters,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.14",

  // Redis
  "com.github.etaty" %% "rediscala" % "1.8.0",
  "com.typesafe.play.modules" %% "play-modules-redis" % playVersion,

  // Akka
  "com.typesafe.akka" %% "akka-slf4j" % "2.5-M1",

  // Test
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test"
)

routesGenerator := InjectedRoutesGenerator

scalacOptions in ThisBuild ++= Seq(
  "-feature",
  "-language:postfixOps",
  "-Xfuture",
  "-language:_",
  "-deprecation",
  "-unchecked"
)

assemblyJarName in assembly := "d2.jar"

mainClass in assembly := Some("play.core.server.ProdServerStart")

fullClasspath in assembly += Attributed.blank(PlayKeys.playPackageAssets.value)

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case PathList("META-INF", xs@_*) => MergeStrategy.last
  case PathList("org", "apache", "commons", "logging", xs@_*) => MergeStrategy.last
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

