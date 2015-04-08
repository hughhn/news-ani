name := """news-ani"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "org.webjars"    %%   "webjars-play"          % "2.3.0-2",
  "org.webjars"    %    "bootstrap"             % "3.1.1-2",
  "org.webjars"    %    "bootswatch-cerulean"   % "3.3.1+2",
  "org.webjars"    %    "html5shiv"             % "3.7.0",
  "org.webjars"    %    "respond"               % "1.4.2"
)


fork in run := true