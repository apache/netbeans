/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Andrei Badea
 */
public class ParseResultTest extends NbTestCase {

    public ParseResultTest(String testName) {
        super(testName);
    }

    public void testGetChecksType() {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("foo", "fooValue");
        values.put("bar", null);
        ParseResult result = new ParseResult(values);
        assertEquals("fooValue", result.get("foo", String.class));
        assertEquals("fooValue", result.get("foo", CharSequence.class));
        try {
            result.get("foo", Integer.class);
            fail();
        } catch (IllegalStateException e) {}
        assertNull(result.get("bar", String.class));
        assertNull(result.get("bar", Integer.class));
    }
}
