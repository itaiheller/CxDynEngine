package com.checkmarx.engine.rest;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.assertj.core.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.google.common.base.MoreObjects;

/**
 * {@link ClientHttpRequestInterceptor} to apply Cx authentication cookies.
 * 
 * @author randy@checkmarx.com
 *
 */
public class CxCookieAuthInterceptor implements ClientHttpRequestInterceptor {
	
	private static final Logger log = LoggerFactory.getLogger(CxCookieAuthInterceptor.class);

	//private static final String COOKIE_HEADER = "Cookie";
	private static final String CX_CSRF_COOKIE = "CXCSRFToken";
	private static final String CX_SESSION_COOKIE = "cxCookie";
	
	private String csrfToken;
	private String cxSession;
	private final StringBuilder cookies = new StringBuilder();


	public CxCookieAuthInterceptor() {
		log.info("CxCookieAuthInterceptor.ctor(): {}", this);
	}

	public CxCookieAuthInterceptor(String cxSession, String csrfToken) {
		this.cxSession = Preconditions.checkNotNullOrEmpty(cxSession).toString();
		this.csrfToken = csrfToken;
		buildAuthCookies();
		log.info("CxCookieAuthInterceptor.ctor(): {}", this);
	}
	
	private void buildAuthCookies() {
		log.trace("buildAuthCookies()");
		
		cookies.setLength(0);
		cookies.append(String.format("%1$s=%2$s", CX_SESSION_COOKIE, cxSession));
		if (StringUtils.isNotBlank(csrfToken)) {
			cookies.append("; ");
			cookies.append(String.format("%1$s=%2$s", CX_CSRF_COOKIE, csrfToken));
		}
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		log.trace("intercept()");
		
		if (cookies.length() > 0) {
			//HttpClient automatically adds the cookies, just add the CSRF token
			//request.getHeaders().add(COOKIE_HEADER, cookies.toString());
			request.getHeaders().add(CX_CSRF_COOKIE, csrfToken);
		}
		
		final ClientHttpResponse response =  execution.execute(request, body);
	
		return checkCookies(response);
	}
	
	private ClientHttpResponse checkCookies(ClientHttpResponse response) {
		log.trace("checkCookies()");
		
		final List<String> headers = response.getHeaders().get("Set-Cookie");
		if (headers == null) {
			return response;
		}
		
		log.trace("Response has cookies: count={}", headers.size());
		List<HttpCookie> setCookies = Lists.newArrayList();
		for (String header : headers) {
			setCookies.addAll(HttpCookie.parse(header));
		}
		
		boolean isSet = false;
		for (HttpCookie cookie : setCookies) {
			log.trace("{} : {}", cookie.getName(), cookie.getValue());
			
			final String name = cookie.getName(); 
			if (name.equals(CX_SESSION_COOKIE)) {
				isSet = true;
				this.cxSession = cookie.getValue();
			} else if (name.equals(CX_CSRF_COOKIE)) {
				this.csrfToken = cookie.getValue();
			}
		}
		if (isSet) {
			buildAuthCookies();
		}
		return response;
	}

	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
				.add("cxSession", StringUtils.right(cxSession, 4))
				.add("csrfToken", StringUtils.right(csrfToken, 4))
				.toString();
	}

}
