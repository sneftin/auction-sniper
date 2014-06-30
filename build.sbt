name := "AuctionSniper"

version := "1.0"

//libraryDependencies += "org.igniterealtime.smack" % "smack" % "3.2.1"
//libraryDependencies += "com.googlecode.windowlicker" % "windowlicker-swing" % "r268"
//libraryDependencies += "org.hamcrest" % "hamcrest-all" % "1.3" % "test"

libraryDependencies ++= Seq(
  "org.hamcrest" % "hamcrest-all" % "1.3",
  "com.googlecode.windowlicker" % "windowlicker-swing" % "r268",
  "org.igniterealtime.smack" % "smack" % "3.2.1",
  "org.igniterealtime.smack" % "smackx" % "3.2.1",
  "org.specs2" %% "specs2" % "2.3.7" % "test"
)

//  "ch.qos.logback" % "logback-classic" % "1.0.13",
