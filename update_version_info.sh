#!/usr/bin/env bash
set -euo pipefail

# Read and validate environment variable 'github.ref_name'
ref_name="$1"
if [ -z "$ref_name" ]; then
  echo "Error: environment variable 'github.ref_name' is not set" >&2
  exit 1
fi

# Determine VERSION:
# - If ref_name starts with 'v', strip the leading 'v'
# - Otherwise use ref_name as-is
if [[ "$ref_name" == v*.*.* ]]; then
  VERSION="${ref_name#v}"
  sed -i  "s#^(?:[\s\S]*?)(<version>[^<]*<\/version>)#<version>${VERSION}</version>#g" pom.xml
  sed -i "s#^application.version=.*#application.version=${VERSION}#g" src/main/resources/application.properties
else
  VERSION="$ref_name"
  sed -i "s#^application.version=.*#application.version=${VERSION}#g" src/main/resources/application.properties
fi
