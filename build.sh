#!/bin/bash

# Directories
SRC_DIR="src"
#RESOURCES_DIR="resources"
LIB_DIR="lib"
BUILD_DIR="build"

# create build directory
mkdir -p "$BUILD_DIR"

# classpath (include all jars in lib directory)
CP=$(find "$LIB_DIR" -name "*.jar" | paste -sd ":" -)

# compile Java files
find "$SRC_DIR" -name "*.java" > sources.txt
javac -d "$BUILD_DIR" -cp "$CP" @sources.txt
rm -f sources.txt

# copy resources to buildpath (the Icons)
# cp -r "$RESOURCES_DIR"/* "$BUILD_DIR"/

# run the main class with java -cp (adjust package path accordingly)
MAIN_CLASS="SergeantStubby"
java -cp "$BUILD_DIR:$CP" "$MAIN_CLASS"

