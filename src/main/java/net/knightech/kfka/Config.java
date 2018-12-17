package net.knightech.kfka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

@Configuration
@EnableSwagger2
public class Config {

    @Bean
    public ObjectMapper objectMapper(){
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return objectMapper;
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.sky.csc.title.discovery.controller"))
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfo(
                "CSC Title Discovery API",
                "API specifications for the Title Discovery service in Sky's new Content Supply Chain\n" +
                        "    More details on the Title Discovery architecture can be found " +
                        "[here](https://github.com/sky-uk/csp-title-discovery-api/blob/master/README.md)",
                "version: 0.1.2",
                "Terms of service",
                new Contact("CSC - Planning and Scheduling Tide Team",
                        "https://github.com/sky-uk/csp-title-discovery-api",
                        "@sky-uk/tide-guys"),
                        "License of API", "API license URL", Collections.emptyList());
    }
}