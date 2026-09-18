#!/usr/bin/env bash
set -euo pipefail

# Idempotent Cloud Agent bootstrap: JDK 25 (pom.java.version) plus a Maven package.
JAVA_25_HOME=/usr/lib/jvm/java-25-openjdk-amd64

if [[ ! -x "${JAVA_25_HOME}/bin/java" ]]; then
  sudo DEBIAN_FRONTEND=noninteractive apt-get update
  sudo DEBIAN_FRONTEND=noninteractive apt-get install -y --no-install-recommends openjdk-25-jdk
fi

sudo ln -sfn java-25-openjdk-amd64 /usr/lib/jvm/default-java
export JAVA_HOME="${JAVA_25_HOME}"
export PATH="${JAVA_HOME}/bin:${PATH}"

./mvnw -B -ntp clean package -Dmaven.test.skip=true
