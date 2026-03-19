#!/bin/bash

mvn clean package -DskipTests
docker buildx build --platform linux/amd64 -t ghcr.io/nils-witt/webmap_backend:dev .
docker push ghcr.io/nils-witt/webmap_backend:dev

docker buildx build --platform linux/amd64 -t registry.home.nils-witt.de/webmap_backend:dev .
docker push registry.home.nils-witt.de/webmap_backend:dev