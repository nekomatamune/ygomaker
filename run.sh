#!/usr/bin/env bash
./gradlew pack && java -jar ygomaker.jar "$@"