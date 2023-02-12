/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javaee.wildfly.util;

import java.util.Optional;
import org.junit.Test;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;

public class WildflyDefaultValueExtractorTest {

    @Test
    public void testNullInput() {
        Optional<String> resolve = WildflyDefaultValueExtractor.extract(null);
        assertFalse(resolve.isPresent());
    }

    @Test
    public void testEmptyInput() {
        Optional<String> resolve = WildflyDefaultValueExtractor.extract("");
        assertFalse(resolve.isPresent());
    }

    @Test
    public void testRandomInput() {
        Optional<String> resolve = WildflyDefaultValueExtractor.extract("key=3344;value=12");
        assertFalse(resolve.isPresent());
    }

    @Test
    public void testVariableOnly() {
        Optional<String> resolve = WildflyDefaultValueExtractor.extract("${jboss.management.http.port}");
        assertFalse(resolve.isPresent());
    }

    @Test
    public void testVariableWithDefault() {
        Optional<String> resolve = WildflyDefaultValueExtractor.extract("${jboss.management.http.port:3455}");
        assertEquals("3455", resolve.get());
    }

}
