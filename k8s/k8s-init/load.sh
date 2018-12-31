#!/usr/bin/env bash

until $(curl --output /dev/null --silent --head --fail http://localhost:30036/health); do
    printf '.'
    sleep 1
done

echo
echo '###########################'
echo '##     load and obtain   ##'
echo '###########################'


curl -X GET "http://localhost:30036/title-discovery/load?items=1000&offers=2&terms=2&genre=int-test" -H "accept: application/json;charset=UTF-8"

curl -X GET "http://localhost:30036/title-discovery/titles?genre=int-test" -H "accept: application/json;charset=UTF-8"
