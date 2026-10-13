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
package org.netbeans.modules.acp.mcp;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ToolResult {

    private ToolResult() {
    }

    public static Map<String, Object> ok() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        return result;
    }

    public static Map<String, Object> ok(String key, Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put(key, value);
        return result;
    }

    public static Map<String, Object> ok(String k1, Object v1, String k2, Object v2) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put(k1, v1);
        result.put(k2, v2);
        return result;
    }

    public static Map<String, Object> ok(String k1, Object v1, String k2, Object v2,
            String k3, Object v3) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put(k1, v1);
        result.put(k2, v2);
        result.put(k3, v3);
        return result;
    }

    public static Map<String, Object> ok(String k1, Object v1, String k2, Object v2,
            String k3, Object v3, String k4, Object v4) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "ok");
        result.put(k1, v1);
        result.put(k2, v2);
        result.put(k3, v3);
        result.put(k4, v4);
        return result;
    }

    public static Map<String, Object> error(String message) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "error");
        result.put("message", message);
        return result;
    }
}
