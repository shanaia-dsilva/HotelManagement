#!/bin/sh
# Maven Wrapper script
# Downloads Maven if needed, then runs it

set -e

MAVEN_VERSION="3.9.6"
MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-${MAVEN_VERSION}"
MAVEN_URL="https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/${MAVEN_VERSION}/apache-maven-${MAVEN_VERSION}-bin.tar.gz"

if [ ! -f "${MAVEN_HOME}/bin/mvn" ]; then
    echo "Downloading Maven ${MAVEN_VERSION}..."
    mkdir -p "${MAVEN_HOME}"
    curl -fsSL "${MAVEN_URL}" | tar xz -C "${MAVEN_HOME}" --strip-components=1
    echo "Maven ${MAVEN_VERSION} installed."
fi

exec "${MAVEN_HOME}/bin/mvn" "$@"
