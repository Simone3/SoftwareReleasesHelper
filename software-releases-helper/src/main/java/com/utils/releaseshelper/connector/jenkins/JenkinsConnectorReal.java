package com.utils.releaseshelper.connector.jenkins;

import java.time.Duration;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseCookie;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.utils.releaseshelper.model.domain.JenkinsConfig;
import com.utils.releaseshelper.model.dto.rest.CrumbResponse;
import com.utils.releaseshelper.model.error.BusinessException;
import com.utils.releaseshelper.model.logic.JenkinsCrumbData;
import com.utils.releaseshelper.model.misc.KeyValuePair;

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
	public JenkinsCrumbData getCrumb(String crumbUrl, String username, String password) {
		
		JenkinsCrumbData crumbData = new JenkinsCrumbData();
		
		CrumbResponse crumbResponseBody = webClient
			.get()
			.uri(crumbUrl)
			.headers(headers -> headers.setBasicAuth(username, password))
			.exchangeToMono(response -> {
				
				if(response.statusCode().is2xxSuccessful()) {
					
					crumbData.setCookies(getResponseCookies(response));
					return response.bodyToMono(CrumbResponse.class);
				}
				else {
					
					return response.createException().flatMap(Mono::error);
				}
			})
			.onErrorMap(WebClientResponseException.class, e -> new IllegalStateException("Error status: " + e.getStatusCode() + " - Body: " + e.getResponseBodyAsString()))
			.block();
		
		if(crumbResponseBody == null || StringUtils.isBlank(crumbResponseBody.getCrumb())) {
			
			throw new BusinessException("No Jenkins crumb received");
		}
		
		crumbData.setCrumb(crumbResponseBody.getCrumb());
		
		return crumbData;
	}

	@Override
	public void startBuild(String buildUrl, String username, String password, JenkinsCrumbData crumbData, List<KeyValuePair> parameters) {
		
		MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
		for(KeyValuePair parameter: parameters) {
			
			queryParams.add(parameter.getKey(), parameter.getValue());
		}
		
		webClient
			.post()
			.uri(buildUrl, uriBuilder -> uriBuilder
				.queryParams(queryParams)
				.build()
			)
			.headers(headers -> headers.setBasicAuth(username, password))
			.headers(headers -> {
				if(crumbData != null) {
					headers.add("Jenkins-Crumb", crumbData.getCrumb());
				}
			})
			.cookies(cookies -> {
				if(crumbData != null) {
					cookies.addAll(crumbData.getCookies());
				}
			})
			.retrieve()
			.onStatus(status -> !status.is2xxSuccessful(), httpResponse ->
				httpResponse
					.bodyToMono(String.class)
					.map(httpResponseString -> new IllegalStateException("Error status: " + httpResponse.statusCode() + " - Body: " + httpResponseString)))
			.bodyToMono(CrumbResponse.class)
			.block();
	}
	
	private MultiValueMap<String, String> getResponseCookies(ClientResponse response) {
		
		MultiValueMap<String, String> cookies = new LinkedMultiValueMap<>();
		
		for(Entry<String, List<ResponseCookie>> entry: response.cookies().entrySet()) {
			
			if(!StringUtils.isBlank(entry.getKey()) && !CollectionUtils.isEmpty(entry.getValue())) {
				
				cookies.add(entry.getKey(), entry.getValue().get(0).getName());
			}
		}
		
		return cookies;
	}
}
