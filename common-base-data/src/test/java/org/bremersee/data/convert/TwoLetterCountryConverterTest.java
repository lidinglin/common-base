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

package org.bremersee.data.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.bremersee.common.model.TwoLetterCountryCode;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * The two letter country converter test.
 *
 * @author Christian Bremer
 */
class TwoLetterCountryConverterTest {

  private static TwoLetterCountryCodeReadConverter readConverter;

  private static TwoLetterCountryCodeWriteConverter writeConverter;

  /**
   * Sets up.
   */
  @BeforeAll
  static void setUp() {
    readConverter = new TwoLetterCountryCodeReadConverter();
    writeConverter = new TwoLetterCountryCodeWriteConverter();
  }

  /**
   * Convert.
   */
  @Test
  void convert() {
    TwoLetterCountryCode code = TwoLetterCountryCode.BE;
    String actual = writeConverter.convert(code);
    assertNotNull(actual);
    assertEquals(code.toString(), actual);
    assertEquals(code, readConverter.convert(actual));
  }
}