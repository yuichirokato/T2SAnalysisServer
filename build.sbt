name := "T2SAnalysisServer"

version := "1.0"

scalaVersion := "2.11.4"

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  Seq(
    "io.spray" %% "spray-can" % sprayV,
    "io.spray" %% "spray-routing" % sprayV,
    "io.spray" %% "spray-testkit" % sprayV % "test",
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV % "test",
    "org.specs2" %% "specs2-core" % "2.3.11" % "test",
    "mysql" % "mysql-connector-java" % "5.1.25",
    "net.liftweb" % "lift-json_2.11" % "3.0-M2",
    "ch.qos.logback" % "logback-classic" % "1.1.2",
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe.slick" %% "slick" % "2.1.0-M2",
    "org.atilika.kuromoji" % "kuromoji" % "0.7.7"
  )
}

resolvers ++= Seq(
  "Spray repository" at "http://repo.spray.io",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Atilika Open Source repository" at "http://www.atilika.org/nexus/content/repositories/atilika"
)