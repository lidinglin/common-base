/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bremersee.security.authentication;

import java.io.IOException;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import org.bremersee.exception.PasswordFlowAuthenticationException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

/**
 * @author Christian Bremer
 */
public class PasswordFlowAccessTokenRetriever
    implements AccessTokenRetriever<MultiValueMap<String, String>, String> {

  private final ErrorHandler errorHandler = new ErrorHandler();

  private final HttpHeaders headers = new HttpHeaders();

  private final RestTemplateBuilder restTemplateBuilder;

  private final String tokenEndpoint;

  @SuppressWarnings("WeakerAccess")
  public PasswordFlowAccessTokenRetriever(
      final RestTemplateBuilder restTemplateBuilder,
      final String tokenEndpoint) {
    this.restTemplateBuilder = restTemplateBuilder;
    this.tokenEndpoint = tokenEndpoint;
    this.headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
  }

  @Override
  public String retrieveAccessToken(final MultiValueMap<String, String> body) {
    final RestTemplate restTemplate = restTemplateBuilder.build();
    restTemplate.setErrorHandler(errorHandler);
    final HttpEntity<?> request = new HttpEntity<>(body, headers);
    final String response = restTemplate.exchange(
        tokenEndpoint,
        HttpMethod.POST,
        request,
        String.class)
        .getBody();
    final JSONObject json = (JSONObject) JSONValue.parse(response);
    final String accessToken = json.getAsString("access_token");
    if (StringUtils.hasText(accessToken)) {
      return accessToken;
    }
    throw new PasswordFlowAuthenticationException(HttpStatus.UNAUTHORIZED,
        "There is no access token in the response: " + accessToken);
  }

  private static class ErrorHandler extends DefaultResponseErrorHandler {

    @Override
    protected void handleError(ClientHttpResponse response, HttpStatus statusCode)
        throws IOException {
      final String statusText = response.getStatusText();
      throw new PasswordFlowAuthenticationException(statusCode, statusText);
    }
  }

}
