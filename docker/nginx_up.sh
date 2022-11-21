#!/usr/bin/env bash
set -ex
docker-compose up --force-recreate --remove-orphans -d nginx