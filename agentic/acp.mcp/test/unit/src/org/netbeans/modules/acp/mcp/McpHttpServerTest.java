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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.acp.mcp.spi.McpToolProvider;
import org.netbeans.modules.acp.mcp.spi.McpToolRegistry;

public class McpHttpServerTest extends NbTestCase {
    private static final ObjectMapper JSON = new ObjectMapper();
    private McpHttpServer server;
    private String token;

    public McpHttpServerTest(String name) {
        super(name);
    }

    public static class DefaultTool implements McpToolProvider {
        @Override
        public List<McpToolDefinition> getTools() {
            return List.of(new McpToolDefinition("default_tool", "default description", Map.of("type", "object")),
                    new McpToolDefinition("error_tool", "error", Map.of("type", "object")),
                    new McpToolDefinition("throw_tool", "throws", Map.of("type", "object")));
        }

        @Override
        public Map<String, Object> executeTool(String name, Map<String, Object> args) throws Exception {
            return switch (name) {
                case "error_tool" -> Map.of("status", "error", "text", "tool failed");
                case "throw_tool" -> throw new Exception("provider failed");
                default -> Map.of("text", "value=" + args.get("value"));
            };
        }
    }

    public static class OptionalTool implements McpToolProvider {
        @Override
        public List<McpToolDefinition> getTools() {
            return List.of(new McpToolDefinition("optional_tool", "optional", Map.of("type", "object")));
        }

        @Override
        public Map<String, Object> executeTool(String name, Map<String, Object> args) {
            return Map.of("text", "optional result");
        }

        @Override
        public boolean isEnabledByDefault() {
            return false;
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(DefaultTool.class, OptionalTool.class);
        McpToolRegistry registry = McpToolRegistry.getInstance();
        registry.refresh();
        server = new McpHttpServer(registry);
        server.start();
        token = server.createSessionToken(null);
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            server.stop();
            MockServices.setServices();
            McpToolRegistry.getInstance().refresh();
        } finally {
            super.tearDown();
        }
    }

    public void testSessionToolPoliciesAndIsolation() throws Exception {
        String empty = server.createSessionToken(Set.of());
        String optional = server.createSessionToken(Set.of("optional_tool"));
        assertFalse(token.equals(empty));
        JsonNode defaults = rpc(token, "tools/list", null).path("result").path("tools");
        assertEquals(3, defaults.size());
        assertEquals("default_tool", defaults.get(0).path("name").asText());
        assertEquals("default description", defaults.get(0).path("description").asText());
        assertEquals("object", defaults.get(0).path("inputSchema").path("type").asText());
        assertEquals(0, rpc(empty, "tools/list", null).path("result").path("tools").size());
        assertEquals("optional_tool", rpc(optional, "tools/list", null).path("result").path("tools").get(0).path("name").asText());
        assertEquals(-32602, rpc(token, "tools/call", "{\"name\":\"optional_tool\"}").path("error").path("code").asInt());
        assertEquals(-32602, rpc(empty, "tools/call", "{\"name\":\"default_tool\"}").path("error").path("code").asInt());
        assertEquals("optional result", rpc(optional, "tools/call", "{\"name\":\"optional_tool\"}")
                .path("result").path("content").get(0).path("text").asText());
    }

    public void testRequestValidation() throws Exception {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"}";
        assertEquals(405, request("GET", Map.of(), "").status());
        assertEquals(401, request("POST", Map.of("Content-Type", "application/json"), body).status());
        assertEquals(401, request("POST", Map.of("Authorization", "Basic invalid", "Content-Type", "application/json"), body).status());
        assertEquals(401, request("POST", headers("Bearer invalid", "application/json"), body).status());
        assertEquals(415, request("POST", headers("Bearer " + token, "text/plain"), body).status());
        assertEquals(415, request("POST", Map.of("Authorization", "Bearer " + token), body).status());
        assertEquals(403, request("POST", Map.of("Authorization", "Bearer " + token, "Content-Type", "application/json", "Host", "evil.example"), body).status());
        assertEquals(403, request("POST", Map.of("Authorization", "Bearer " + token, "Content-Type", "application/json", "Origin", "https://evil.example"), body).status());
        assertEquals(200, request("POST", Map.of("Authorization", "Bearer " + token, "Content-Type", "application/json; charset=utf-8", "Origin", "http://localhost:8080"), body).status());
    }

    public void testTokenRevocationAndRestart() throws Exception {
        String other = server.createSessionToken(Set.of("optional_tool"));
        server.removeSessionToken(token);
        assertEquals(401, request("POST", headers("Bearer " + token, "application/json"), "{}").status());
        assertEquals(200, request("POST", headers("Bearer " + other, "application/json"), "{}").status());
        server.stop();
        assertFalse(server.isRunning());
        assertEquals(0, server.getPort());
        server.start();
        assertTrue(server.isRunning());
        assertEquals(401, request("POST", headers("Bearer " + other, "application/json"), "{}").status());
    }

    public void testInitializePingAndNotification() throws Exception {
        JsonNode response = rpc(token, "initialize", null);
        assertEquals("2.0", response.path("jsonrpc").asText());
        assertEquals(7, response.path("id").asInt());
        assertEquals("2025-03-26", response.path("result").path("protocolVersion").asText());
        assertEquals("netbeans-ide", response.path("result").path("serverInfo").path("name").asText());
        assertFalse(response.path("result").path("capabilities").path("tools").path("listChanged").asBoolean(true));
        assertTrue(rpc(token, "ping", null).path("result").isObject());
        Response notification = request("POST", headers("Bearer " + token, "application/json"),
                "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}");
        assertEquals(202, notification.status());
        assertEquals("", notification.body());
    }

    public void testNotificationsAndBatches() throws Exception {
        Map<String, String> auth = headers("Bearer " + token, "application/json");
        for (String method : List.of("notifications/cancelled", "unknown")) {
            Response response = request("POST", auth,
                    "{\"jsonrpc\":\"2.0\",\"method\":\"" + method + "\"}");
            assertEquals(202, response.status());
            assertEquals("", response.body());
        }
        Response notifications = request("POST", auth,
                "[{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"},"
                + "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/cancelled\"}]");
        assertEquals(202, notifications.status());
        assertEquals("", notifications.body());

        Response mixed = request("POST", auth,
                "[{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"},"
                + "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"ping\"},"
                + "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"unknown\"},"
                + "42,{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"ping\"}]");
        assertEquals(200, mixed.status());
        JsonNode results = JSON.readTree(mixed.body());
        assertEquals(4, results.size());
        assertEquals(1, results.get(0).path("id").asInt());
        assertEquals(-32601, results.get(1).path("error").path("code").asInt());
        assertEquals(-32600, results.get(2).path("error").path("code").asInt());
        assertEquals(3, results.get(3).path("id").asInt());

        assertEquals(-32600, json("[]").path("error").path("code").asInt());
        assertEquals(-32600, json("42").path("error").path("code").asInt());
        assertTrue(json("{\"jsonrpc\":\"2.0\",\"id\":null,\"method\":\"ping\"}")
                .has("result"));
        Response clientResponse = request("POST", auth,
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{}}");
        assertEquals(202, clientResponse.status());
        assertEquals("", clientResponse.body());
    }

    public void testJsonRpcErrors() throws Exception {
        Response malformed = request("POST", headers("Bearer " + token, "application/json"), "{");
        assertEquals(200, malformed.status());
        assertEquals(-32700, JSON.readTree(malformed.body()).path("error").path("code").asInt());
        assertEquals(-32600, json("{\"jsonrpc\":\"2.0\",\"id\":7}").path("error").path("code").asInt());
        assertEquals(-32601, rpc(token, "unknown", null).path("error").path("code").asInt());
        assertEquals(-32602, rpc(token, "tools/call", "{}").path("error").path("code").asInt());
    }

    public void testToolResultsAndArguments() throws Exception {
        JsonNode success = rpc(token, "tools/call", "{\"name\":\"default_tool\",\"arguments\":{\"value\":\"hello\"}}").path("result");
        assertFalse(success.path("isError").asBoolean(true));
        assertEquals("text", success.path("content").get(0).path("type").asText());
        assertEquals("value=hello", success.path("content").get(0).path("text").asText());
        JsonNode failure = rpc(token, "tools/call", "{\"name\":\"error_tool\"}").path("result");
        assertTrue(failure.path("isError").asBoolean());
        assertEquals("tool failed", failure.path("content").get(0).path("text").asText());
        JsonNode exception = rpc(token, "tools/call", "{\"name\":\"throw_tool\"}").path("result");
        assertTrue(exception.path("isError").asBoolean());
        assertTrue(exception.path("content").get(0).path("text").asText().contains("provider failed"));
    }

    private JsonNode rpc(String bearer, String method, String params) throws Exception {
        String request = "{\"jsonrpc\":\"2.0\",\"id\":7,\"method\":\"" + method + "\""
                + (params == null ? "" : ",\"params\":" + params) + "}";
        Response response = request("POST", headers("Bearer " + bearer, "application/json"), request);
        assertEquals(200, response.status());
        return JSON.readTree(response.body());
    }

    private JsonNode json(String body) throws Exception {
        return JSON.readTree(request("POST", headers("Bearer " + token, "application/json"), body).body());
    }

    private static Map<String, String> headers(String authorization, String contentType) {
        return Map.of("Authorization", authorization, "Content-Type", contentType);
    }

    private record Response(int status, String body) { }

    private Response request(String method, Map<String, String> headers, String body) throws Exception {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        StringBuilder head = new StringBuilder(method + " /mcp HTTP/1.1\r\n");
        if (!headers.containsKey("Host")) {
            head.append("Host: 127.0.0.1:").append(server.getPort()).append("\r\n");
        }
        headers.forEach((key, value) -> head.append(key).append(": ").append(value).append("\r\n"));
        head.append("Connection: close\r\nContent-Length: ").append(bytes.length).append("\r\n\r\n");
        try (Socket socket = new Socket("127.0.0.1", server.getPort())) {
            socket.setSoTimeout(5000);
            socket.getOutputStream().write(head.toString().getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().write(bytes);
            socket.getOutputStream().flush();
            ByteArrayOutputStream response = new ByteArrayOutputStream();
            socket.getInputStream().transferTo(response);
            String raw = response.toString(StandardCharsets.UTF_8);
            int bodyStart = raw.indexOf("\r\n\r\n");
            assertTrue("Invalid HTTP response: " + raw, bodyStart >= 0);
            int status = Integer.parseInt(raw.substring(raw.indexOf(' ') + 1, raw.indexOf(' ') + 4));
            return new Response(status, raw.substring(bodyStart + 4));
        }
    }
}
