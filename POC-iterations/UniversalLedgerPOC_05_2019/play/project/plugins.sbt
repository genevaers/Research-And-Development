// this plugin is also copied at the Universal Ledger level, so sbt works correctly there too.

// added for File Upload Template
resolvers += Resolver.jcenterRepo
resolvers += Resolver.typesafeRepo("snapshots")

// Added for original project template
// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.7.0")

