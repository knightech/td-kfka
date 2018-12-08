#!/usr/bin/env bash
chmod -R 777 test-scripts/kafka
docker cp test-scripts/kafka/connect.sh kiro-kafka:/opt/kafka/bin
docker cp test-scripts/kafka/sink kiro-kafka:/opt/kafka/bin
docker exec -d kiro-kafka /opt/kafka/bin/connect.sh