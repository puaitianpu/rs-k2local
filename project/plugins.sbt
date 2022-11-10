// Coursier是Scala应用程序和工件管理器。它可以安装Scala应用程序并设置你的Scala开发环境。它还可以从网上下载和缓存工件。
// sbt-scoverage是一个sbt插件，为使用scoverage的Scala代码覆盖提供支持
// sbt-native-packager 是一个sbt原生打包器
// sbt-wartremover 灵活的 Scala 代码检查工具
// sbt-javaagent Java 代理插件

//addSbtPlugin("io.get-coursier"   % "sbt-coursier"        % "2.0.12")

addSbtPlugin("org.scoverage"     % "sbt-scoverage"       % "2.0.6")
addSbtPlugin("com.github.sbt"    % "sbt-native-packager" % "1.9.11")
addSbtPlugin("org.wartremover"   % "sbt-wartremover"     % "3.0.6")
addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent"       % "0.1.5")
addSbtPlugin("com.thesamet"      % "sbt-protoc"          % "1.0.3")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.11.11"
