#!/usr/bin/env bash

echo '####################################################################'
echo '##     init kafka, zookeeper and couchbase deployments in k8s     ##'
echo '####################################################################'

## wait until couchbase service comes online
until $(curl --output /dev/null --silent --head --fail http://localhost:30037); do
    printf '.'
    sleep 1
done

chmod -R 777 k8s-init/couchbase
couchpod=$(kubectl --namespace=td get pod -l  app=couchbase-rc-pod -o go-template --template '{{range .items}}{{.metadata.name}}{{end}}');
kubectl --namespace=td cp ./k8s-init/couchbase/couch.sh $couchpod:/opt/couchbase/bin/;
kubectl --namespace=td exec $couchpod /opt/couchbase/bin/couch.sh;

## wait until kafka service comes online
until (nc -z localhost 30038); do
    printf '.'
    sleep 1
done

chmod -R 777 k8s-init/kafka
kafpod=$(kubectl --namespace=td get pod -l name=kafka -o go-template --template '{{range .items}}{{.metadata.name}}{{end}}');
kubectl --namespace=td cp ./k8s-init/kafka/connect.sh $kafpod:/opt/kafka/bin/
kubectl --namespace=td cp ./k8s-init/kafka/sink $kafpod:/opt/kafka/bin/
kubectl --namespace=td exec $kafpod /opt/kafka/bin/connect.sh;


