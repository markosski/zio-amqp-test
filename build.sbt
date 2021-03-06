ThisBuild / organization := "example"
ThisBuild / scalaVersion := "2.13.4"
ThisBuild / scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-language:higherKinds",
  "-language:postfixOps",
  "-feature",
  // "-Xfatal-warnings",
)
ThisBuild / version := "0.0.1-SNAPSHOT"

fork in run := true
cancelable in Global := true

val fatJarSettings = Seq(
  test in assembly := {},
  artifact in (Compile, assembly) := {
    val art = (artifact in (Compile, assembly)).value
    art.withClassifier(Some("assembly"))
  },
  assemblyMergeStrategy in assembly := {
    case PathList("reference.conf") => MergeStrategy.concat // configs
    case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
   },
  addArtifact(artifact in (Compile, assembly), assembly)
)

lazy val amqp = (project in file("./amqp"))
  .settings(
    resolvers += Resolver.jcenterRepo,
    Global / cancelable := true,
    name := "amqp",
    libraryDependencies ++= Seq(
      "nl.vroste" %% "zio-amqp" % "0.1.3" 
    ) 
  )
  .settings(fatJarSettings: _*)
  .settings(mainClass in assembly := Some("amqp.HelloWorld"))

lazy val root = (project in file("."))
   .aggregate(amqp)