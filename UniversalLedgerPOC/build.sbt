import sbt.Keys.licenses

lazy val commonSettings = Seq(
  organization := "com.ibm.univledger",

  version := "0.1.0-SNAPSHOT",

  scalaVersion := "2.11.8"

)

//name := "universal_ledger"

lazy val root = Project(id="universal_ledger", base = file("."))

lazy val play = (project in file("play"))
  .settings(
    commonSettings,
    // other settings
  )

lazy val SAFRonSpark = (project in file("SAFRonSpark"))
  .settings(
    commonSettings,
    // other settings
  )


lazy val VA_periodic_update = (project in file("VA_periodic_update"))
  .settings(
    commonSettings,
    // other settings
  )
