package net.knightech.kfka;

import com.couchbase.client.java.Bucket;
import com.fasterxml.jackson.databind.JsonNode;
import com.palantir.docker.compose.DockerComposeRule;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import net.knightech.kfka.service.MyTopology;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


/**
 *
 * End-to-end test of Kafka Streams and Kafka Connect to Couchbase with dependencies provided
 * by Docker.
 *
 * This test is excluded in the test task of the Gradle build due to its dependency on
 * Docker.
 *
 * It is also Ignored as it has a dependency on the workspace to have Docker running.
 *
 */
@Ignore
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class DockerComposeIntegrationTest {


    private static final Logger log = LoggerFactory.getLogger(DockerComposeIntegrationTest.class);
    private static final String TITLE_BUCKET_BEAN_NAME = "titleBucket";
    private static final String TITLE_TOPOLOGY_BEAN_NAME = "titleTopology";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ApplicationContext appContext;

    public void initiateShutdown(int returnCode) {
        SpringApplication.exit(appContext, () -> returnCode);
    }

    @ClassRule
    public static DockerComposeRule docker = DockerComposeRule.builder()
            .file("docker-compose.yml")
            .waitingForService("kafka", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("zookeeper", HealthChecks.toHaveAllPortsOpen())
            .waitingForService("couchbase-server", HealthChecks.toRespond2xxOverHttp(8091,
                    dockerPort -> "http://" + dockerPort.getIp() + ":" + dockerPort.getExternalPort()))
            .build();

    @BeforeClass
    public static void initialize() throws Exception {

        runScript("sh test-scripts/setup-couchbase.sh");
        runScript("sh test-scripts/run-kafka-connect.sh");

    }

    @After
    public void tearDown() throws Exception {

        Bucket titleBucket = (Bucket) appContext.getBean(TITLE_BUCKET_BEAN_NAME);
        titleBucket.close(1000, TimeUnit.SECONDS);

        MyTopology titleTopology = (MyTopology) appContext.getBean(TITLE_TOPOLOGY_BEAN_NAME);
        titleTopology.closeStream();

        runScript("sh test-scripts/remove-containers.sh");
    }

    @Test
    public void getItemsByGenreActionReturnsListOfGenresByAction() throws InterruptedException {

        // arrange
        ResponseEntity<String> populate = restTemplate.getForEntity(
                "/title-discovery/load?items=3&offers=2&terms=2&genre=Action",
                String.class);

        assertThat(populate.getStatusCode()).isEqualTo(HttpStatus.OK);

        int counter = 0;
        int numberToTry = 3;

        //wait
        while (counter < numberToTry) {

            ResponseEntity<List<JsonNode>> itemList = restTemplate.exchange(
                    "/title-discovery/titles?genre=Action",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<JsonNode>>() {
                    });


            assertThat(itemList.getStatusCode()).isEqualTo(HttpStatus.OK);

            // wait until all items have been processed
            if (itemList.getBody() != null && itemList.getBody().isEmpty()) {

                TimeUnit.SECONDS.sleep(15);
                counter++;


            } else {

                //assert
                assertThat(itemList.getBody()).isNotNull();
                assertThat(itemList.getBody().size()).isEqualTo(3);
                break;
            }
        }
    }

    /**
     * Excecutes the command passed in the parameter
     *
     * @param command the command to execute
     * @throws IOException thrown should any error occur during processing
     */
    public static void runScript(String command) throws IOException {

        CommandLine oCmdLine = CommandLine.parse(command);
        DefaultExecutor oDefaultExecutor = new DefaultExecutor();
        oDefaultExecutor.setExitValue(0);
        log.debug("successfully executed: %s with response code %d%n", command, oDefaultExecutor.execute(oCmdLine));

    }
}


