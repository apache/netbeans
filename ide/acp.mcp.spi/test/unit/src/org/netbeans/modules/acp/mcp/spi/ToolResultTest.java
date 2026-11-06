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
package org.netbeans.modules.acp.mcp.spi;

import java.util.Map;
import org.netbeans.junit.NbTestCase;

public class ToolResultTest extends NbTestCase {

    public ToolResultTest(String name) {
        super(name);
    }

    public void testOkNoArgs() {
        Map<String, Object> result = ToolResult.ok();
        assertEquals("ok", result.get("status"));
        assertEquals(1, result.size());
    }

    public void testOkOnePair() {
        Map<String, Object> result = ToolResult.ok("key", "value");
        assertEquals("ok", result.get("status"));
        assertEquals("value", result.get("key"));
        assertEquals(2, result.size());
    }

    public void testOkTwoPairs() {
        Map<String, Object> result = ToolResult.ok("a", 1, "b", 2);
        assertEquals("ok", result.get("status"));
        assertEquals(1, result.get("a"));
        assertEquals(2, result.get("b"));
        assertEquals(3, result.size());
    }

    public void testOkThreePairs() {
        Map<String, Object> result = ToolResult.ok("a", 1, "b", 2, "c", 3);
        assertEquals("ok", result.get("status"));
        assertEquals(1, result.get("a"));
        assertEquals(2, result.get("b"));
        assertEquals(3, result.get("c"));
        assertEquals(4, result.size());
    }

    public void testOkFourPairs() {
        Map<String, Object> result = ToolResult.ok("a", 1, "b", 2, "c", 3, "d", 4);
        assertEquals("ok", result.get("status"));
        assertEquals(4, result.get("d"));
        assertEquals(5, result.size());
    }

    public void testError() {
        Map<String, Object> result = ToolResult.error("something broke");
        assertEquals("error", result.get("status"));
        assertEquals("something broke", result.get("message"));
        assertEquals(2, result.size());
    }

    public void testOkResultIsMutable() {
        Map<String, Object> result = ToolResult.ok("a", 1);
        result.put("extra", "data");
        assertEquals("data", result.get("extra"));
    }

    public void testOkPreservesInsertionOrder() {
        Map<String, Object> result = ToolResult.ok("first", 1, "second", 2, "third", 3);
        Object[] keys = result.keySet().toArray();
        assertEquals("status", keys[0]);
        assertEquals("first", keys[1]);
        assertEquals("second", keys[2]);
        assertEquals("third", keys[3]);
    }
}
