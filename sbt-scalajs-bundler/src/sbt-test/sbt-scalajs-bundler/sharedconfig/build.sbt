
name := "sharedconfig"

enablePlugins(ScalaJSBundlerPlugin, ScalaJSJUnitPlugin)

scalaVersion := "2.11.12"

scalaJSUseMainModuleInitializer := true

libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "1.0.0"

npmDependencies in Compile += "leaflet" -> "0.7.7"

npmDevDependencies in Compile ++= Seq(
  "webpack-merge" -> "4.1.2",
  "file-loader" -> "6.2.0",
  "image-webpack-loader" -> "7.0.1",
  "css-loader" -> "5.0.1",
  "style-loader" -> "2.0.0",
  "url-loader" -> "4.1.0"
)

webpackConfigFile in fastOptJS := Some(baseDirectory.value / "dev.webpack.config.js")

// Use a different Webpack configuration file for production
webpackConfigFile in fullOptJS := Some(baseDirectory.value / "prod.webpack.config.js")

// Use the shared Webpack configuration file for reload workflow and for running the tests
webpackConfigFile in Test := Some(baseDirectory.value / "common.webpack.config.js")

// Execute the tests in browser-like environment
requireJsDomEnv in Test := true

webpackBundlingMode in fastOptJS := BundlingMode.LibraryAndApplication()

useYarn := true

// HtmlUnit does not support ECMAScript 2015
scalaJSLinkerConfig ~= { _.withESFeatures(_.withUseECMAScript2015(false)) }

// Check that a HTML can be loaded (and that its JavaScript can be executed) without errors
InputKey[Unit]("html") := {
  import complete.DefaultParsers._
  val page = (Space ~> StringBasic).parsed
  import com.gargoylesoftware.htmlunit.WebClient
  val client = new WebClient()
  try {
    client.getPage(s"file://${baseDirectory.value.absolutePath}/$page")
  } finally {
    client.close()
  }
}

TaskKey[Unit]("checkSize") := {
  val files = (webpack in (Compile, fullOptJS)).value
  val bundleFile = files
    .find(_.metadata.get(BundlerFileTypeAttr).exists(_ == BundlerFileType.ApplicationBundle))
    .get.data
  val artifactSize = IO.readBytes(bundleFile).length
  // Account for minor variance in size due to transitive dependency updates
  assert(artifactSize > 150000 && artifactSize < 170000)
}

ivyLoggingLevel in ThisBuild := UpdateLogging.Quiet
