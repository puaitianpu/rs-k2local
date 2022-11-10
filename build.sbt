ThisBuild / name := "k2local"
ThisBuild / organization := "com.tp"
ThisBuild / scalaVersion := "2.13.10"

lazy val root = project
  .in(file("."))
  .settings(name := (ThisBuild / name).value)
  .settings(version := (ThisBuild / version).value)
  .settings(coverageSettings) // scoverage 的 Scala 代码覆盖率支持
  .enablePlugins(JavaAppPackaging) // 启用 Java 应用程序原型插件，包含以下特性: 1. 默认应用程序映射(无 fat jar) 2. 可执行的 bash/bat 脚本
  .enablePlugins(UniversalPlugin) // 启用通用插件
  .aggregate(core, test)

lazy val core = project
  .in(file("core"))
  .settings(name := "core")
  .settings(scalacOptions ++= commonScalacOptions)
  .settings(addKinkProjector)
  .enablePlugins(JavaAppPackaging) // 启用 Java 应用程序原型插件，包含以下特性: 1. 默认应用程序映射(无 fat jar) 2. 可执行的 bash/bat 脚本
  .enablePlugins(UniversalPlugin) // 启用通用插件
  .enablePlugins(JavaAgent) // 启用 Java 代理插件
  .settings(packagerSettings)
// .settings(wartRemoverSettings)

lazy val test = project
  .in(file("test"))
  .dependsOn(core)

val akkaVersion         = "2.7.0"
val akkaHttpVersion     = "10.4.0"
val alpakkaKafkaVersion = "4.0.0"
val catsVersion         = "2.8.0"
val prometheusVersion   = "0.16.0"
val slickVersion        = "3.4.1"
val scalaCsvVersion     = "1.3.10"

libraryDependencies ++= Seq(
  "com.typesafe.akka"    %% "akka-actor-typed"    % akkaVersion,
  "com.typesafe.akka"    %% "akka-stream"         % akkaVersion,
  "com.typesafe.akka"    %% "akka-http"           % akkaHttpVersion,
  "com.typesafe.akka"    %% "akka-stream-kafka"   % alpakkaKafkaVersion,
  "org.typelevel"        %% "cats-core"           % catsVersion,
  "org.typelevel"        %% "cats-free"           % catsVersion,
  "io.prometheus"        % "simpleclient"         % prometheusVersion,
  "io.prometheus"        % "simpleclient_hotspot" % prometheusVersion,
  "io.prometheus"        % "simpleclient_common"  % prometheusVersion,
  "com.typesafe.slick"   %% "slick"               % slickVersion,
  "org.slf4j"            % "slf4j-nop"            % "1.7.26",
  "com.typesafe.slick"   %% "slick-hikaricp"      % slickVersion,
  "com.github.tototoshi" %% "scala-csv"           % scalaCsvVersion
)

lazy val wartRemoverSettings = Seq(
  wartremoverWarnings ++= Warts.allBut(Wart.Equals)
)

// 使用 scalapb/scalapb.proto 或 google/protobuf/*.proto 的定制功能
lazy val scalapbDependencies = libraryDependencies += "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"

// jetty-alpn-agent 是一个JVM代理，通过使用正确的JVM版本的Jetty alpn-boot(或 npn-boot) JAR文件转换相关的 Java 类，使 TLS ALPN(或 NPN) 扩展支持 Java 7和8
lazy val jettyAlpnAgent = javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.10" % "runtime;test"

// 使类型 lambdas (类型投影) 更容易编写的编译器插件
lazy val addKinkProjector = addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full)

lazy val commonScalacOptions = Seq(
  "-deprecation", // Emit warning and location for usages of deprecated APIs(对已废弃的API的使用发出警告和定位)
  "-encoding",
  "UTF-8",
  "-feature", // Emit warning and location for usages of features that should be imported explicitly(对于应该明确导入的特征的使用，发出警告和位置)
  "-unchecked",       // Enable additional warnings where generated code depends on assumptions(在生成的代码依赖于假设的情况下启用额外的警告)
  "-Ywarn-dead-code", // Warn when dead code is identified(Warn when dead code is identified)
  // "-Xfuture",         // Turn on future language features(开启未来的语言功能)
  // "-Xlint",                   // Enable specific warnings (see `scalac -Xlint:help`)(Enable specific warnings (see `scalac -Xlint:help`))
  // "-Yno-adapted-args",        // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver(不要调整参数列表（通过插入（）或创建一个元组）来匹配接收器)
  // "-Ywarn-inaccessible", // Warn about inaccessible types in method signatures(对方法签名中不可访问的类型发出警告)
  // "-Ywarn-infer-any",        // Warn when a type argument is inferred to be `Any`(当一个类型的参数被推断为 "任何 "时发出警告)
  // "-Ywarn-nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'(当非空值的`def f()'覆盖空值的`def f'时发出警告)
  // "-Ywarn-nullary-unit",      // Warn when nullary methods return Unit(当空洞的方法返回单位时发出警告)
  // "-Ywarn-numeric-widen",     // Warn when numerics are widened(当数字被加宽时发出警告)
  // "-Ywarn-unused",            // Warn when local and private vals, vars, defs, and types are unused(当本地和私有vals、vars、defs和type未使用时发出警告)
  // "-Ywarn-unused-import",     // Warn when imports are unused(当导入未被使用时发出警告)
  // "-Ywarn-value-discard",     // Warn when non-Unit expression results are unused(当非单位表达式的结果未被使用时发出警告)
  "-language:postfixOps",
  "-language:higherKinds",
  "-language:existentials"
)

lazy val coverageSettings = Seq(
  coverageEnabled := true,
  coverageFailOnMinimum := true,
  coverageMinimumStmtTotal := 90,
  coverageMinimumBranchTotal := 90,
  coverageMinimumStmtPerPackage := 90,
  coverageMinimumBranchPerPackage := 85,
  coverageMinimumStmtPerFile := 85,
  coverageMinimumBranchPerFile := 80,
  coverageOutputCobertura := false,
  coverageOutputXML := false
)

// 用于抑制警告的Scala编译器插件(已过时了)
/*lazy val silencerSettings = libraryDependencies ++= Seq(
  compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.12" cross CrossVersion.full),
  "com.github.ghik" % "silencer-lib" % "1.7.12" % Provided cross CrossVersion.full
)*/

addCommandAlias("validate", ";clean;protobuf:protobufGenerate;coverage;test;coverageReport")
addCommandAlias("package", ";core/universal:packageZipTarball")
addCommandAlias("stage", ";core/stage")
addCommandAlias("build", ";validate;coverageOff;package")
addCommandAlias("run", ";core/run")
addCommandAlias("pkg", ";core/universal:packageZipTarball")

ThisBuild / Compile / doc / sources := List()
ThisBuild / packageDoc / publishArtifact := false
ThisBuild / packageSrc / publishArtifact := true

lazy val packagerSettings = Seq(Universal / mappings += baseDirectory.value / "deploy/app.sh" -> "app.sh") ++
  Option(System.getProperty("conf")).toList.map { conf =>
    Universal / mappings ++= {
      (baseDirectory.value / "deploy" / conf * "*").get.map { f =>
        f -> s"config/${f.name}"
      }
    }
  }
