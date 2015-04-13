name := """news-ani"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  "Apache Snapshots" at "http://repository.apache.org/snapshots/")

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "org.webjars"    %%   "webjars-play"          % "2.3.0-2",
  "org.webjars"    %    "bootstrap"             % "3.3.4",
  "org.webjars"    %    "bootswatch-cerulean"   % "3.3.1+2",
  "org.webjars"    %    "html5shiv"             % "3.7.2",
  "org.webjars"    %    "respond"               % "1.4.2",
  "ws.securesocial" %% "securesocial" % "master-SNAPSHOT",
  "org.twitter4j"% "twitter4j-core"% "4.0.3",
  "com.rometools" % "rome" % "1.5.0"
)


fork in run := true