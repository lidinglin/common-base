/*
 * Copyright 2020 the original author or authors.
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

package org.bremersee.security.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;
import org.bremersee.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

/**
 * The anauthenticated user context caller test.
 *
 * @author Christian Bremer
 */
class UnauthenticatedUserContextCallerTest {

  private static final UserContext expected = UserContext.newInstance();

  /**
   * Sets up.
   */
  @BeforeEach
  void setUp() {
    SecurityContextHolder.clearContext();
    Authentication authentication = Mockito.mock(Authentication.class);
    when(authentication.isAuthenticated()).thenReturn(false);
    when(authentication.getName()).thenReturn("guest");
    when(authentication.getAuthorities()).then(invocation -> Collections.emptyList());
    SecurityContextHolder.setContext(new SecurityContextImpl(authentication));
  }

  /**
   * Call with required user context.
   */
  @Test
  void callWithRequiredUserContext() {
    UserContextCaller caller = new UserContextCaller(
        UserContextCaller.EMPTY_GROUPS_SUPPLIER,
        UserContextCaller.FORBIDDEN_SUPPLIER);
    assertThrows(ServiceException.class, () -> caller
        .callWithRequiredUserContext(userContext -> serviceMethod(userContext, new Object())));
  }

  /**
   * Call with optional user context.
   */
  @Test
  void callWithOptionalUserContext() {
    UserContextCaller caller = new UserContextCaller();
    Optional<UserContext> optActual = caller
        .callWithOptionalUserContext(userContext -> optServiceMethod(userContext, new Object()));
    assertNotNull(optActual);
    assertTrue(optActual.isPresent());
    assertFalse(optActual.get().isUserIdPresent());
    assertEquals(expected, optActual.get());
  }

  /**
   * Response with required user context.
   */
  @Test
  void responseWithRequiredUserContext() {
    UserContextCaller caller = new UserContextCaller();
    assertThrows(ServiceException.class, () -> caller
        .responseWithRequiredUserContext(userContext -> serviceMethod(userContext, new Object())));
  }

  /**
   * Response with optional user context.
   */
  @Test
  void responseWithOptionalUserContext() {
    UserContextCaller caller = new UserContextCaller(auth -> Collections.emptySet());
    ResponseEntity<UserContext> response = caller
        .responseWithOptionalUserContext(userContext -> serviceMethod(userContext, new Object()));
    assertNotNull(response);
    UserContext actual = response.getBody();
    assertNotNull(actual);
    assertFalse(actual.isUserIdPresent());
    assertEquals(expected, actual);
  }

  /**
   * Response with optional user context and expect not found.
   */
  @Test
  void responseWithOptionalUserContextAndExpectNotFound() {
    UserContextCaller caller = new UserContextCaller();
    ResponseEntity<?> response = caller
        .responseWithOptionalUserContext(this::nullServiceMethod);
    assertNotNull(response);
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
  }

  private UserContext serviceMethod(UserContext userContext, Object arg) {
    assertNotNull(arg);
    return userContext;
  }

  private Optional<UserContext> optServiceMethod(UserContext userContext, Object arg) {
    assertNotNull(arg);
    return Optional.of(userContext);
  }

  private Object nullServiceMethod(UserContext userContext) {
    assertNotNull(userContext);
    return null;
  }

}