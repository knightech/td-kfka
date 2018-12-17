package net.knightech.kfka;

import com.couchbase.client.java.Bucket;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestConfig {

    /**
     * Mock the database as this is eagerly loaded during startup
     */
    @MockBean
    Bucket bucket;

    @Bean(destroyMethod = "close")
    public Bucket titleBucket() {
        return bucket;
    }

}
