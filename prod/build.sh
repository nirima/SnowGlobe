#!/bin/bash

#DIR = directory of script
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

rm $DIR/*.war
rm $DIR/*.jar

ls

M2_REPO=~/.m2/repository
VERSION=0.0.1

BINARY_JAR=$DIR/../snowglobe/server/snowglobe-server-exe/target/snowglobe-server-exe-$VERSION-jar-with-dependencies.jar
  
echo Directory = $DIR
echo Binary = $BINARY_JAR

cp $BINARY_JAR $DIR/snowglobe.jar

# --> docker build -f Dockerfile -t snowglobe .
