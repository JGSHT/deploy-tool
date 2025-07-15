package com.huyiyu.deploy.config;

import com.huyiyu.deploy.http.ArtifactoryExchange;
import com.huyiyu.deploy.property.DeployProperties;
import com.huyiyu.deploy.property.DeployProperties.Flyway;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(DeployProperties.class)
public class HttpConfig {


    @Bean
    public RestClient.Builder restClientBuilder() {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder().requestFactory(requestFactory);
    }

    @Bean
    public ArtifactoryExchange artifactoryExchange(DeployProperties deployProperties,
                                                   RestClient.Builder builder) {
        Flyway flyway = deployProperties.getFlyway();
        String s = flyway.getRepoUsername() + ":" + flyway.getRepoPassword();
        String cert = Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
        builder
                .baseUrl(flyway.getRepoUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + cert);
        return HttpServiceProxyFactory.builderFor(RestClientAdapter.create(builder.build()))
                .build().createClient(ArtifactoryExchange.class);
    }


}
