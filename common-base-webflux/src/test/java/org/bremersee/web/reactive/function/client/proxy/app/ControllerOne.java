/*
 * Copyright 2019 the original author or authors.
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

package org.bremersee.web.reactive.function.client.proxy.app;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Test controller one.
 *
 * @author Christian Bremer
 */
public interface ControllerOne {

  /**
   * The constant OK_RESPONSE.
   */
  String OK_RESPONSE = "OK";

  /**
   * Simple get mono.
   *
   * @return the mono
   */
  @GetMapping
  Mono<String> simpleGet();

  /**
   * Gets oks.
   *
   * @return the oks
   */
  @GetMapping(path = "/api/oks", produces = MediaType.APPLICATION_JSON_VALUE)
  Flux<Map<String, Object>> getOks();

  /**
   * Update ok mono.
   *
   * @param name the name
   * @param payload the payload
   * @return the mono
   */
  @PutMapping(path = "/api/oks/{name}",
      produces = MediaType.TEXT_PLAIN_VALUE,
      consumes = MediaType.TEXT_PLAIN_VALUE)
  Mono<String> updateOk(@PathVariable("name") String name,
      @RequestBody String payload);

  /**
   * Patch ok mono.
   *
   * @param name the name
   * @param suffix the suffix
   * @param payload the payload
   * @return the mono
   */
  @PatchMapping(path = "/api/oks/{name}",
      consumes = MediaType.TEXT_PLAIN_VALUE)
  Mono<Void> patchOk(
      @PathVariable("name") String name,
      @RequestParam(name = "suffix") String suffix,
      @RequestBody String payload);

  /**
   * Delete ok mono.
   *
   * @param name the name
   * @return the mono
   */
  @DeleteMapping(path = "/api/oks/{name}", produces = MediaType.APPLICATION_JSON_VALUE)
  Mono<Boolean> deleteOk(@PathVariable("name") String name);

}
