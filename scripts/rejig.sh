#!/bin/sh

killall java -9
rm -rf ../build/nodes 
cd ../ && ./gradlew deployNodes
