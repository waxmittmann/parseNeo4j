name := "neo4jBase"

version := "0.1"

scalaVersion := "2.12.6" // For driver neo4j
//scalaVersion := "2.11.8" // For embedded neo4j

val catsVersion = "1.1.0"
val catsEffectVersion = "1.0.0-RC2"
val scalacheckVersion = "1.13.4" // todo: update
val specsVersion = "4.0.0" // todo: update
val neo4jBase = "3.3.3"
val neo4jDriver = "1.5.1"

// Todo: Factor out dependencies
libraryDependencies ++= Seq(
  // Cats
  "org.typelevel" %% "cats-core" % catsVersion,
  "org.typelevel" %% "cats-effect" % catsEffectVersion,

  // Neo4j
  "org.neo4j" % "neo4j" % neo4jBase,
  "org.neo4j" % "neo4j-common" % neo4jBase,
  "org.neo4j" % "neo4j-cypher" % neo4jBase,
  "org.neo4j" % "neo4j-bolt" % neo4jBase,
  "org.neo4j.driver" % "neo4j-java-driver" % neo4jDriver,

  // PureConfig
  "com.github.pureconfig" %% "pureconfig" % "0.9.1",

  // Test
  "org.specs2" %% "specs2-core" % specsVersion % "test",
  "org.scalacheck" %% "scalacheck" % scalacheckVersion % "test",

  // Pretty print
  "com.lihaoyi" %% "pprint" % "0.5.3"
)

// For macro annotations
addCompilerPlugin("org.scalamacros" %% "paradise" % "2.1.0" cross CrossVersion.full)

/*
//lazy val root = Project(id = "main", base = file(".")) // aggregate experiment dependsOn experiment
//lazy val experiment = Project(id = "experiment", base = file("experiment")) dependsOn root //aggregate root

lazy val global = project
//  .in(file("."))
  .aggregate(
    root,
    experiment
  )

//lazy val root = project //Project(id = "main", base = file(".")) // aggregate experiment dependsOn experiment
lazy val root = project.in(file(".")) //Project(id = "main", base = file(".")) // aggregate experiment dependsOn experiment

lazy val experiment = Project(id = "experiment", base = file("experiment")) dependsOn root //aggregate root

//lazy val common = project
//
//lazy val multi1 = project
//  .dependsOn(
//    common
//  )
*/