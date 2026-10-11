package com.svarupa.ui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Minimal JSON REST client for Vaadin views. Calls the same deployment over
 * localhost (UI and API share the container), carrying the session JWT.
 */
@Component
public class ApiClient {

  private final RestTemplate restTemplate = new RestTemplate();
  private final SessionContext session;
  private final String baseUrl;

  public ApiClient(SessionContext session, @Value("${server.port:8080}") int port) {
    this.session = session;
    this.baseUrl = "http://localhost:" + port;
  }

  public <T> T get(String path, Class<T> type) {
    return exchange(path, HttpMethod.GET, null, type);
  }

  public <T> T post(String path, Object body, Class<T> type) {
    return exchange(path, HttpMethod.POST, body, type);
  }

  public <T> T put(String path, Object body, Class<T> type) {
    return exchange(path, HttpMethod.PUT, body, type);
  }

  public void delete(String path) {
    exchange(path, HttpMethod.DELETE, null, Void.class);
  }

  private <T> T exchange(String path, HttpMethod method, Object body, Class<T> type) {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    if (session.isLoggedIn()) {
      headers.setBearerAuth(session.getAccessToken());
    }
    var entity = new HttpEntity<>(body, headers);
    return restTemplate.exchange(baseUrl + path, method, entity, type).getBody();
  }
}
