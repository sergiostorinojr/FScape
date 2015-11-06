import com.typesafe.sbt.packager.linux.LinuxPackageMapping

lazy val baseName     = "FScape"
lazy val baseNameL    = baseName.toLowerCase

lazy val authorName   = "Hanns Holger Rutz"
lazy val authorEMail  = "contact@sciss.de"

lazy val basicJavaOpts = Seq("-source", "1.6", "-target", "1.6")

lazy val commonSettings = Seq(
  name             := baseName,
  version          := "1.2.0-SNAPSHOT",
  organization     := "de.sciss",
  description      := "A standalone audio rendering software for time domain and spectral signal processing",
  homepage         := Some(url(s"https://github.com/Sciss/${name.value}")),
  licenses         := Seq("GPL v3+" -> url("http://www.gnu.org/licenses/gpl-3.0.txt")),
  scalaVersion     := "2.11.7",
  javacOptions    ++= basicJavaOpts ++ Seq("-encoding", "utf8"),
  javaOptions in (Compile, doc) := basicJavaOpts,  // doesn't eat `-encoding`
  mainClass in Compile := Some("de.sciss.fscape.FScape"),
  libraryDependencies ++= Seq(
    "de.sciss"    %  "weblaf"             % "1.28",
    "de.sciss"    %% "desktop-mac"        % "0.7.1",
    // "de.sciss" %% "audiowidgets-swing" % "1.6.2",
    "de.sciss"    %% "raphael-icons"      % "1.0.2",
    "de.sciss"    %% "fileutil"           % "1.1.1",
    "de.sciss"    %  "scisslib"           % "1.0.0",
    "de.sciss"    %  "netutil"            % "1.0.0",
    "org.pegdown" %  "pegdown"            % "1.6.0"
  )
)

// ---- bundling ----

lazy val assemblySettings = Seq(
  test            in assembly    := (),
  target          in assembly    := baseDirectory.value,
  assemblyJarName in assembly    := s"${name.value}.jar"
)

// ---- build info source generator ----

lazy val buildInfoSettings = Seq(
  buildInfoKeys := Seq(name, organization, version, scalaVersion, description,
    BuildInfoKey.map(homepage) { case (k, opt) => k -> opt.get },
    BuildInfoKey.map(licenses) { case (_, Seq((lic, _))) => "license" -> lic }
  ),
  buildInfoPackage := "de.sciss.fscape"
)

// ---- publishing ----

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    Some(if (isSnapshot.value)
      "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    else
      "Sonatype Releases"  at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
    )
  },
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  pomExtra := { val n = name.value
<scm>
  <url>git@github.com:Sciss/{n}.git</url>
  <connection>scm:git:git@github.com:Sciss/{n}.git</connection>
</scm>
<developers>
  <developer>
    <id>sciss</id>
    <name>Hanns Holger Rutz</name>
    <url>http://www.sciss.de</url>
  </developer>
</developers>
  }
)

//////////////// universal (directory) installer
lazy val pkgUniversalSettings = Seq(
  // NOTE: doesn't work on Windows, where we have to
  // provide manual file `SYSSON_config.txt` instead!
  javaOptions in Universal ++= Seq(
    // -J params will be added as jvm parameters
    "-J-Xmx1024m"
    // others will be added as app parameters
    // "-Dproperty=true",
  ),
  // Since our class path is very very long,
  // we use instead the wild-card, supported
  // by Java 6+. In the packaged script this
  // results in something like `java -cp "../lib/*" ...`.
  // NOTE: `in Universal` does not work. It therefore
  // also affects debian package building :-/
  // We need this settings for Windows.
  scriptClasspath /* in Universal */ := Seq("*")
)

//////////////// debian installer
lazy val pkgDebianSettings: Seq[sbt.Def.Setting[_]] = Seq(
  maintainer in Debian := s"$authorName <$authorEMail>",
  debianPackageDependencies in Debian += "java7-runtime",
  packageSummary in Debian := description.value,
  packageDescription in Debian :=
    """SysSon is a platform for the development and application
      | of sonification. It aims to be an integrative system that
      | serves different types of users, from domain scientists to
      | sonification researchers to composers and sound artists.
      | It therefore has an open nature capable of addressing different
      | usage scenarios.
      |""".stripMargin,
  // include all files in src/debian in the installed base directory
  linuxPackageMappings in Debian ++= {
    val n     = (name            in Debian).value.toLowerCase
    val dir   = (sourceDirectory in Debian).value / "debian"
    val f1    = (dir * "*").filter(_.isFile).get  // direct child files inside `debian` folder
    val f2    = ((dir / "doc") * "*").get
    //
    def readOnly(in: LinuxPackageMapping) =
      in.withUser ("root")
        .withGroup("root")
        .withPerms("0644")  // http://help.unc.edu/help/how-to-use-unix-and-linux-file-permissions/
    //
    val aux   = f1.map { fIn => packageMapping(fIn -> s"/usr/share/$n/${fIn.name}") }
    val doc   = f2.map { fIn => packageMapping(fIn -> s"/usr/share/doc/$n/${fIn.name}") }
    (aux ++ doc).map(readOnly)
  }
)

lazy val root = Project(id = baseName, base = file("."))
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging, DebianPlugin)
  .settings(commonSettings)
  .settings(buildInfoSettings)
  .settings(assemblySettings)
  .settings(pkgUniversalSettings)
  .settings(useNativeZip) // cf. https://github.com/sbt/sbt-native-packager/issues/334
  .settings(pkgDebianSettings)
  .settings(publishSettings)


