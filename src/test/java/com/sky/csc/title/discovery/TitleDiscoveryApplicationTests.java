package com.sky.csc.title.discovery;

import io.prometheus.client.CollectorRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TitleDiscoveryTestConfig.class)
public class TitleDiscoveryApplicationTests {

	static {
		//HACK Avoids duplicate metrics registration in case of Spring Boot dev-tools restarts
		CollectorRegistry.defaultRegistry.clear();
	}

	@Test
	public void contextLoads() {
	}

}
