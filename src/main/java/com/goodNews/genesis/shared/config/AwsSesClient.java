package com.goodNews.genesis.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
public class AwsSesClient {

	@Value("${aws_access_key_id}")
	private String key;

	@Value("${aws_secret_access_key}")
	private String secretKey;

	@Value("${aws_region}")
	private String region;

	@Bean
	public SesClient sesClient() {
		return SesClient.builder()
				.region(Region.of(region))
				.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(key, secretKey)))
				.httpClient(UrlConnectionHttpClient.create())
				.build();
	}
}
