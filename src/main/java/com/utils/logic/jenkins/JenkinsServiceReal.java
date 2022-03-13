package com.utils.logic.jenkins;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import com.utils.model.jenkins.CrumbResponse;
import com.utils.model.jenkins.JenkinsData;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.SneakyThrows;
import reactor.netty.http.client.HttpClient;

class JenkinsServiceReal implements JenkinsService {

	private final WebClient webClient;
	
	@SneakyThrows
	public JenkinsServiceReal(JenkinsData jenkinsData) {
		
		boolean insecureHttps = jenkinsData.isInsecureHttps();
		int timeoutMilliseconds = jenkinsData.getTimeoutMilliseconds();
		
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
		for(var parameter: parameters.entrySet()) {
			
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
			.bodyToMono(CrumbResponse.class)
			.block();
	}
}
