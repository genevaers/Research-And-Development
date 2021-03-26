
lazy val root = (project in file("."))
  .settings(
    name := "SAFRonSpark",
    mainClass in (Compile, packageBin) := Some("com.ibm.safr.core.SAFR"),
    scalaVersion := "2.11.8"
  )

exportJars := true

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "2.4.1",
  "org.apache.spark" %% "spark-sql" % "2.4.0",
  "org.codehaus.janino" % "janino" % "3.0.12",
//  "org.apache.cassandra" % "cassandra-all" % "3.7", //cassndra does not appear to be scala dependent
//  "com.datastax.spark" % "spark-cassandra-connector_2.10" % "1.6.0", //cassndra does not appear to be scala dependent
  "com.databricks" %% "spark-csv" % "1.5.0",
  "org.scalatest"     %% "scalatest"   % "3.0.3",
  "junit"             %  "junit"       % "4.12"
//  "com.aerospike" % "aerospike-spark" % "1.1.9" from "/Users/ktwitchell001/workspace/universal_ledger/SAFRonSpark/src/main/resources/aerospike-spark_2.11-1.1.9.jar",
//  "com.aerospike" %% "aerospike-spark" % "1.1.9",
//  "com.aerospike" % "aerospike-client" % "latest.integration"
)

// Original from POM
//libraryDependencies ++= Seq(
//  "org.apache.spark" %% "spark-core_2.10" % "1.6.2",
//  "org.apache.spark" %% "spark-sql_2.10" % "1.6.2",
//  "org.codehaus.janino" %% "janino" % "2.7.8",
//  "org.apache.cassandra" %% "cassandra-all" % "3.7",
//  "com.datastax.spark" %% "spark-cassandra-connector_2.10" % "1.6.0",
//  "com.databricks" %% "spark-csv_2.10" % "1.4.0"
//"org.scalatest"     %% "scalatest"   % "3.0.3" % Test withSources(),
//"junit"             %  "junit"       % "4.12"  % Test
//)