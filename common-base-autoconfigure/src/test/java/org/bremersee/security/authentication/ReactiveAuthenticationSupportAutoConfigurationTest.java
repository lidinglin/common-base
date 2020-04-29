/*
 * Copyright 2019-2020 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.bremersee.security.SecurityProperties;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

/**
 * The reactive authentication support auto configuration test.
 *
 * @author Christian Bremer
 */
class ReactiveAuthenticationSupportAutoConfigurationTest {

  private static ReactiveAuthenticationSupportAutoConfiguration configuration;

  /**
   * Init.
   */
  @BeforeAll
  static void init() {
    SecurityProperties properties = new SecurityProperties();
    configuration = new ReactiveAuthenticationSupportAutoConfiguration(properties);
    configuration.init();
  }

  /**
   * Json path reactive jwt converter.
   */
  @Test
  void jsonPathReactiveJwtConverter() {
    assertNotNull(configuration.jsonPathReactiveJwtConverter());
  }

  /**
   * Web client access token retriever.
   */
  @Test
  void webClientAccessTokenRetriever() {
    assertNotNull(configuration.webClientAccessTokenRetriever());
  }

  /**
   * Password flow reactive authentication manager.
   */
  @Test
  void passwordFlowReactiveAuthenticationManager() {
    assertNotNull(configuration.passwordFlowReactiveAuthenticationManager(
        jwtDecoder(),
        new JsonPathReactiveJwtConverter(),
        new WebClientAccessTokenRetriever()));
  }

  private static ObjectProvider<ReactiveJwtDecoder> jwtDecoder() {
    ReactiveJwtDecoder value = mock(ReactiveJwtDecoder.class);
    //noinspection unchecked
    ObjectProvider<ReactiveJwtDecoder> provider = mock(ObjectProvider.class);
    when(provider.getIfAvailable()).thenReturn(value);
    return provider;
  }
}