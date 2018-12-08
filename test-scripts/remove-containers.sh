#!/usr/bin/env bash

echo '####################################################################'
echo '##              stop and remove docker containers                 ##'
echo '####################################################################'

docker-compose stop;
docker rm -f $(docker ps -a -q -f name="kiro-zk|kiro-kafka|kiro-couch");

