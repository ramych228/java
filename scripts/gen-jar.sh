#!/bin/bash

SRC_PATH="../java-solutions"
ARTIFACTS_PATH="../../shared/artifacts"
LIB_PATH="../../shared/lib"

OUT_DIR="out"

mkdir -p $OUT_DIR

javac --module-path "$ARTIFACTS_PATH;$LIB_PATH" \
      META-INF/module-info.java \
      $SRC_PATH/info/kgeorgiy/ja/amirov/implementor/*.java \
      -d $OUT_DIR

JAR_OUT_DIR="./"
mkdir -p $JAR_OUT_DIR

jar --create --file=$JAR_OUT_DIR/implementor.jar \
    --manifest=META-INF/MANIFEST.MF \
    -C $OUT_DIR .

echo "Jar file created at $JAR_OUT_DIR/implementor.jar"

sleep 10