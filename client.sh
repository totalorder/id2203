#!/usr/bin/env bash
java -jar client/target/project17-client-1.0-SNAPSHOT-shaded.jar -b localhost:$1 -p $(( ( RANDOM % 64000 )  + 1025 ))
