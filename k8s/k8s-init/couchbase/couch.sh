#!/usr/bin/env bash

echo '##################'
echo '## init cluster ##'
echo '##################'

/opt/couchbase/bin/couchbase-cli cluster-init -c localhost \
        --cluster-name=titles \
        --cluster-username=tds-owner \
        --cluster-password=tds-owner \
        --cluster-ramsize=318 \
        --cluster-index-ramsize=512 \
        --services=data,index,query

echo '###################'
echo '## create bucket ##'
echo '###################'

/opt/couchbase/bin/couchbase-cli bucket-create -c localhost:8091 \
       --bucket=tds-data \
       --bucket-type=couchbase \
       --bucket-ramsize=200 \
       --bucket-replica=1 \
       --bucket-priority=high \
       -u tds-owner -p tds-owner

echo '##########################'
echo '## create tds-full user ##'
echo '##########################'

/opt/couchbase/bin/couchbase-cli user-manage -c localhost:8091 -u tds-owner  -p tds-owner \
 --set --rbac-username tds-full --rbac-password tds-full \
 --rbac-name "tds-full" --roles data_reader[tds-data],data_writer[tds-data],query_manage_index[tds-data],query_select[tds-data],query_delete[tds-data] \
 --auth-domain local

sleep 5

echo '####################'
echo '## create indexes ##'
echo '####################'

curl --silent -L --max-redirs 3 --retry-connrefused --retry-delay 3 --max-time 30 \
 http://localhost:8093/query/service -u tds-owner:tds-owner -d "statement=CREATE PRIMARY INDEX \`#primary\` ON \`tds-data\`"

curl --silent -L --max-redirs 3 --retry-connrefused --retry-delay 3 --max-time 30 \
 http://localhost:8093/query/service -u tds-owner:tds-owner -d "statement=CREATE INDEX \`genre_index\` ON \`tds-data\`(\`genre\`)"

