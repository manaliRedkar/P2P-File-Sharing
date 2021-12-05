#!/bin/sh
# mvn package builds the jar file (jar is a java executable)
# java -jar <jarname> will run the executable
projectroot=$(git rev-parse --show-toplevel) # Cool git cheat to find project root
mvn package -f $projectroot && java -jar $projectroot/target/p2p-file-sharing-1.0.0.jar