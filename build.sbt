name := """job-scheduler"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.2"

libraryDependencies ++= Seq(
  cache,
  ws,
  "com.typesafe"        %  "config"        % "1.2.1",
  "joda-time"           %  "joda-time"     % "2.3",
  "com.typesafe.akka"   %% "akka-camel"    % "2.3.7",
  "org.apache.camel"    %  "camel-quartz"  % "2.14.0",
  "org.json4s"          %% "json4s-jackson"   % "3.2.9",
  "com.github.seratch"  %% "awscala"       % "0.4.+",
  "com.amazonaws"       % "aws-java-sdk"  % "1.8.9",
  "ch.qos.logback"      % "logback-classic"   % "1.0.13",
  "org.scalatest"       %% "scalatest"    % "2.1.6" % "test",
  "junit"               % "junit"         % "4.11" % "test",
  "com.novocode"        % "junit-interface"   % "0.10" % "test"
)
