scalaVersion := "2.12.4"
libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core"      % "1.0.1",
  "io.monix"      %% "monix"          % "3.0.0-M3",
  "io.lettuce"    % "lettuce-core"    % "5.0.1.RELEASE", // Redis client
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.275",
  "org.scalatest" %% "scalatest"      % "3.0.4" % Test
)
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.4")

enablePlugins(PlayScala)
routesImport += "controllers.Bindables._"

scalafmtOnCompile := true
