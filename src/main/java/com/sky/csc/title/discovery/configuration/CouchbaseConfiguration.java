package com.sky.csc.title.discovery.configuration;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.query.N1qlQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CouchbaseConfiguration {

    @Value("${couchbase.cluster.host}")
    private String host;

    @Value("${couchbase.cluster.title-bucket}")
    private String titleBucket;


    @Value("${couchbase.cluster.username}")
    private String username;

    @Value("${couchbase.cluster.password}")
    private String password;

    @Bean
    public Cluster couchbaseCluster() {

        return CouchbaseCluster
                    .create(host)
                    .authenticate(username, password);
    }

    @Bean(destroyMethod = "close")
    public Bucket titleBucket() {
        Bucket bucket = couchbaseCluster().openBucket(titleBucket);

        createIndexes(bucket);

        return bucket;
    }

    private void createIndexes(Bucket bucket) {


        bucket.query(N1qlQuery.simple(
                "CREATE PRIMARY INDEX ON `" + bucket.name() + "`;"
        ));
    }
}
