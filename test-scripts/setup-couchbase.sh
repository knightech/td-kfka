#!/usr/bin/env bash
chmod -R 777 test-scripts/couchbase
docker cp test-scripts/couchbase/couch.sh kiro-couch:/opt/couchbase/bin
docker exec kiro-couch /opt/couchbase/bin/couch.sh

