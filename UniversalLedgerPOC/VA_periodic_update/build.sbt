
lazy val root = (project in file("."))
  .settings(
    name := "VA_periodic_update",
    mainClass in (Compile, packageBin) := Some("com.ibm.VA_ledger.ledger.VA_ledger")

  )

//mainClass in (Compile, run) := Some("com.ibm.univledger.universal_ledger")

exportJars := true

libraryDependencies ++= Seq(
"org.apache.spark" %% "spark-core" % "2.4.0",
"org.apache.spark" %% "spark-sql" % "2.4.0",
"org.postgresql" % "postgresql" % "42.2.5"
  //"com.databricks" %% "spark-csv" % "1.5.0"
)

//// META-INF discarding
assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}