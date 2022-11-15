PB.protocVersion := "-v3.11.4"

Compile / PB.targets := Seq(
  scalapb.gen(
    flatPackage = true,
    javaConversions = false,
    grpc = false,
  ) -> (Compile / sourceManaged).value / "protobuf"
)
