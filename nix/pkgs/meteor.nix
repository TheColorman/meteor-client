{
  inputs,
  stdenv,
  gradle_9,
  jdk21,
  makeWrapper,
  lib,
}:
let
  gradle = gradle_9;
  jdk = jdk21;
  baritone = inputs.baritone.packages.${stdenv.hostPlatform.system}.baritone-meteor;
  baritoneJar = "${baritone}/baritone-api-fabric-1.17.0+1.21.11.jar";
in
stdenv.mkDerivation (finalAttrs: {
  pname = "meteor-client";
  version = "2026-7-19";

  src = inputs.self;

  nativeBuildInputs = [
    gradle
    jdk
    makeWrapper
  ];

  outputs = [
    "out"
    "dev"
  ];

  mitmCache = gradle.fetchDeps {
    pkg = finalAttrs.finalPackage;
    data = ./deps.json;
  };

  __darwinAllowLocalNetworking = true;

  gradleFlags = [
    "-Dfile.encoding=utf-8"
    "-PbaritoneJar=${baritoneJar}"
  ];

  gradleBuildTask = "build";

  doCheck = true;

  installPhase = ''
    mkdir -p {$out,$dev}

    cp build/libs/*.jar $out/
    cp build/devlibs/*.jar $dev/
  '';

  meta.sourceProvenance = with lib.sourceTypes; [
    fromSource
    binaryBytecode
  ];
})
