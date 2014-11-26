//JsEngineKeys.engineType := JsEngineKeys.EngineType.Node

RjsKeys.generateSourceMaps := false

pipelineStages := Seq(rjs)

excludeFilter in rjs := GlobFilter("*.jsx")

libraryDependencies ++= Seq(
  "org.webjars"    % "bootstrap"       % "3.2.0",
  "org.webjars"    % "jquery"          % "2.1.0-2",
  "org.webjars"    % "requirejs-text"  % "2.0.10-1",
  "org.webjars"    % "underscorejs"    % "1.6.0-1",
  "org.webjars"    % "highcharts"      % "4.0.3",
  "org.webjars"    % "react"           % "0.11.2",
  "org.webjars"    % "react-bootstrap" % "0.12.0",
  "org.webjars"    % "json3"           % "3.3.2"
)

RjsKeys.mainModule := "main"

RjsKeys.modules ++= Seq(
  WebJs.JS.Object("name" -> "main")
)
