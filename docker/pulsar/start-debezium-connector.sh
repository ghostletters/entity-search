#!/bin/sh
set -xe

bin/pulsar-admin sources create --source-config-file conf/debezium-postgres-source-config.yaml
