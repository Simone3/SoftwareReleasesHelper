package com.utils.releaseshelper.connector.jenkins;

import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import com.utils.releaseshelper.model.config.JenkinsConfig;
import com.utils.releaseshelper.model.dto.CrumbResponse;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.SneakyThrows;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * An implementation of the Jenkins connector based on REST APIs invocations
 */
public class JenkinsConnectorReal implements JenkinsConnector {

	private final WebClient webClient;
	
	@SneakyThrows
	public JenkinsConnectorReal(JenkinsConfig jenkinsConfig) {
		
		boolean insecureHttps = jenkinsConfig.isInsecureHttps();
		int timeoutMilliseconds = jenkinsConfig.getTimeoutMilliseconds();
		
		HttpClient httpClient = HttpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMilliseconds)
			.responseTimeout(Duration.ofMillis(timeoutMilliseconds))
			.doOnConnected(conn -> conn
				.addHandlerLast(new ReadTimeoutHandler(timeoutMilliseconds, TimeUnit.MILLISECONDS))
				.addHandlerLast(new WriteTimeoutHandler(timeoutMilliseconds, TimeUnit.MILLISECONDS)));
		
		if(insecureHttps) {

			SslContext sslContext = SslContextBuilder
				.forClient()
				.trustManager(InsecureTrustManagerFactory.INSTANCE)
				.build();
			
			httpClient = httpClient.secure(t -> t.sslContext(sslContext));
		}
		
		this.webClient = WebClient.builder()
			.clientConnector(new ReactorClientHttpConnector(httpClient))
			.build();
	}
	
	@Override
	public String getCrumb(String crumbUrl, String username, String password) {
		
		CrumbResponse response = webClient
			.get()
			.uri(crumbUrl)
			.headers(headers -> headers.setBasicAuth(username, password))
			.retrieve()
			.onStatus(status -> !status.is2xxSuccessful(), this::onErrorHttpStatus)
			.bodyToMono(CrumbResponse.class)
			.block();
		
		if(response == null || StringUtils.isBlank(response.getCrumb())) {
			
			throw new IllegalStateException("No Jenkins crumb received");
		}
		
		return response.getCrumb();
	}

	@Override
	public void startBuild(String buildUrl, String username, String password, String crumb, Map<String, String> parameters) {
		
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		for(Entry<String, String> parameter: parameters.entrySet()) {
			
			queryParams.add(parameter.getKey(), parameter.getValue());
		}
		
		webClient
			.post()
			.uri(buildUrl, uriBuilder -> uriBuilder
				.queryParams(queryParams)
				.build()
			)
			.headers(headers -> headers.setBasicAuth(username, password))
			.headers(headers -> headers.add("Jenkins-Crumb", crumb))
			.retrieve()
			.onStatus(status -> !status.is2xxSuccessful(), this::onErrorHttpStatus)
			.bodyToMono(CrumbResponse.class)
			.block();
	}

	private Mono<? extends Throwable> onErrorHttpStatus(ClientResponse httpResponse) {
		
		return httpResponse
			.bodyToMono(String.class)
			.map(httpResponseString -> new IllegalStateException("Error status: " + httpResponse.statusCode() + " - Body: " + httpResponseString));
	}
}
