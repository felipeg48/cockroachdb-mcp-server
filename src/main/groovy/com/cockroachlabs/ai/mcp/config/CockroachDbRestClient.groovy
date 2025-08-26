package com.cockroachlabs.ai.mcp.config

import org.springframework.boot.ssl.SslBundle
import org.springframework.boot.ssl.SslBundles
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

import javax.net.ssl.SSLContext
import java.net.http.HttpClient
import java.time.Duration

@Configuration
class CockroachDbRestClient {

    private final SslBundles sslBundles

    CockroachDbRestClient(SslBundles sslBundles) {
        this.sslBundles = sslBundles
    }

    @Bean
    RestTemplate restTemplate() {
        SslBundle sslBundle = sslBundles.getBundle("cockroachdb")
        SSLContext sslContext = sslBundle.createSslContext()

        HttpClient jdkHttpClient = HttpClient.newBuilder()
                .sslContext(sslContext)
                .connectTimeout(Duration.ofMillis(100000))
                .version(HttpClient.Version.HTTP_2)
                .build()

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(jdkHttpClient)
        requestFactory.setReadTimeout(Duration.ofMillis(15000))

        new RestTemplate(requestFactory)
    }
}
