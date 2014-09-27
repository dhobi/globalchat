import com.typesafe.sbt.SbtStartScript

name := "GlobalChat"

version := "0.0.1"

resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

organization := "net.liftweb"

scalaVersion := "2.10.2"

resolvers ++= Seq("snapshots"     at "http://oss.sonatype.org/content/repositories/snapshots",
                "releases"        at "http://oss.sonatype.org/content/repositories/releases"
                )

seq(webSettings :_*)

unmanagedResourceDirectories in Test <+= (baseDirectory) { _ / "src/main/webapp" }

scalacOptions ++= Seq("-deprecation", "-unchecked")

libraryDependencies ++= {
  val liftVersion = "2.5.1"
  Seq(
    "net.liftweb"       %% "lift-webkit"        % liftVersion        % "compile",
    "net.liftmodules"   %% "lift-jquery-module_2.5" % "2.4",
    "org.eclipse.jetty" % "jetty-webapp" % "9.0.0.M0" % "container", // For Jetty 9
    "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,test" artifacts Artifact("javax.servlet", "jar", "jar"),
    "javax.servlet" % "servlet-api" % "2.5" % "provided->default",
    "ch.qos.logback"    % "logback-classic"     % "1.0.6",
    "org.specs2"        %% "specs2"             % "1.14"            % "test",
    "org.eclipse.jetty" % "jetty-server" % "9.0.0.M0" % "compile->default",
    "org.eclipse.jetty" % "jetty-servlet" % "9.0.0.M0" % "compile->default"
  )
}

seq(SbtStartScript.startScriptForClassesSettings: _*)

