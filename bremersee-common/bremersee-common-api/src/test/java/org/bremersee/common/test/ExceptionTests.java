/*
 * Copyright 2016 the original author or authors.
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

package org.bremersee.common.test;

import lombok.extern.slf4j.Slf4j;
import org.bremersee.common.exception.ExceptionRegistry;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Christian Bremer
 */
@Slf4j
public class ExceptionTests {

    @Test
    public void testExceptionRegistry() {
        log.info("Testing exception registry ...");
        boolean exists = ExceptionRegistry.existsByHttpStatusCode(404);
        Assert.assertEquals(true, exists);
        log.info("Testing exception registry ... DONE!");
    }
}