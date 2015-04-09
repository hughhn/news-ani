name := """news-ani"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

resolvers += Resolver.sonatypeRepo("snapshots")

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
  "ws.securesocial" %% "securesocial" % "master-SNAPSHOT"
)


fork in run := true