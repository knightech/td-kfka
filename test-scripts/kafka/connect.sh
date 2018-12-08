#!/usr/bin/env bash

echo '##########################################################'
echo '## connect to kafka container and execute kafka-connect ##'
echo '##########################################################'

env CLASSPATH=/opt/kafka/bin/sink/kafka-connect-couchbase-3.4.0.jar connect-standalone.sh /opt/kafka/config/connect-standalone.properties /opt/kafka/bin/sink/kiro-couchbase-sink.properties