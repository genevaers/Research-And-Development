name := "play"

libraryDependencies ++= Seq(
 ws,  // resolved through the sbt plugin below
 guice, // resolved through the sbt plugin below
 "org.scalatestplus.play" %% "scalatestplus-play" % "4.0.1" % Test,
  "com.h2database" % "h2" % "1.4.197",
  "com.aerospike" % "aerospike-client" % "latest.integration"
)

enablePlugins(PlayScala)
//enablePlugins(PlayJava)  //Added for Aerospike code which has been commented out.

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-Xfatal-warnings"
)

