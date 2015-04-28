import sbt._

object MyBuild extends Build {

  lazy val root = Project(id = "mqttd_benchmark", base = file(".")) dependsOn (mqttcProject)
  lazy val mqttcProject = RootProject(uri("git://github.com/kumquatt/mqttc.git"))
}