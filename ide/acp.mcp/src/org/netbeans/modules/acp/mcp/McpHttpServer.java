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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class McpHttpServer {

    private static final Logger LOG = Logger.getLogger(McpHttpServer.class.getName());
    private static final String MCP_PROTOCOL_VERSION = "2025-03-26";
    private static final String SERVER_NAME = "netbeans-ide";
    private static final int TOKEN_BYTES = 32;
    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://127.0.0.1", "http://localhost");

    private final McpToolRegistry registry;
    private final ObjectMapper mapper;
    private final SecureRandom secureRandom = new SecureRandom();
    private final ConcurrentHashMap<String, Set<String>> sessionTokens = new ConcurrentHashMap<>();
    private HttpServer server;
    private ExecutorService executor;
    private int port;

    public McpHttpServer(McpToolRegistry registry) {
        this.registry = registry;
        this.mapper = new ObjectMapper();
    }

    public String createSessionToken(Set<String> enabledTools) {
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        sessionTokens.put(token, enabledTools != null ? enabledTools : Set.of());
        LOG.log(Level.INFO, "MCP session created, tools: {0}",
                enabledTools != null ? enabledTools : "all");
        return token;
    }

    public void removeSessionToken(String token) {
        if (token != null) {
            sessionTokens.remove(token);
        }
    }

    public synchronized void start() throws IOException {
        if (server != null) {
            return;
        }
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/mcp", new McpHandler());
        executor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r, "MCP-Server-Worker");
            t.setDaemon(true);
            return t;
        });
        server.setExecutor(executor);
        server.start();
        port = server.getAddress().getPort();
        LOG.log(Level.INFO, "MCP server started on 127.0.0.1:{0}", port);
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(1);
            server = null;
            if (executor != null) {
                executor.shutdown();
                try {
                    if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                        executor.shutdownNow();
                    }
                } catch (InterruptedException ex) {
                    executor.shutdownNow();
                    Thread.currentThread().interrupt();
                }
                executor = null;
            }
            port = 0;
            sessionTokens.clear();
            LOG.info("MCP server stopped");
        }
    }

    public synchronized int getPort() {
        return port;
    }

    public synchronized String getUrl() {
        return "http://127.0.0.1:" + port + "/mcp";
    }

    public synchronized boolean isRunning() {
        return server != null;
    }

    private class McpHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendError(exchange, 405, "Method not allowed");
                return;
            }
            Set<String> sessionFilter = validateRequest(exchange);
            if (sessionFilter == null) {
                return;
            }
            byte[] body;
            try (InputStream is = exchange.getRequestBody()) {
                body = is.readAllBytes();
            }
            String requestStr = new String(body, StandardCharsets.UTF_8);
            LOG.log(Level.FINE, "MCP request: {0}", requestStr);

            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> request = mapper.readValue(requestStr, Map.class);
                Map<String, Object> response = handleJsonRpc(request, sessionFilter);
                if (response == null) {
                    exchange.sendResponseHeaders(204, -1);
                    exchange.close();
                    return;
                }
                sendJson(exchange, 200, response);
            } catch (JsonProcessingException ex) {
                sendJson(exchange, 200, errorResponse(null, -32700, "Parse error"));
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "MCP handler error", ex);
                sendJson(exchange, 200, errorResponse(null, -32603, "Internal error"));
            }
        }

        /**
         * @return the session's enabled tools set, or {@code null} if the request was rejected
         */
        private Set<String> validateRequest(HttpExchange exchange) throws IOException {
            String auth = exchange.getRequestHeaders().getFirst("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                sendError(exchange, 401, "Unauthorized");
                return null;
            }
            String token = auth.substring("Bearer ".length());
            Set<String> filter = sessionTokens.get(token);
            if (filter == null) {
                sendError(exchange, 401, "Unauthorized");
                return null;
            }
            String host = exchange.getRequestHeaders().getFirst("Host");
            if (host != null) {
                String hostOnly = host.contains(":") ? host.substring(0, host.indexOf(':')) : host;
                if (!"127.0.0.1".equals(hostOnly) && !"localhost".equals(hostOnly)) {
                    LOG.log(Level.WARNING, "MCP request rejected: unexpected Host header: {0}", host);
                    sendError(exchange, 403, "Forbidden");
                    return null;
                }
            }
            String origin = exchange.getRequestHeaders().getFirst("Origin");
            if (origin != null) {
                boolean allowed = ALLOWED_ORIGINS.stream()
                        .anyMatch(o -> origin.equals(o) || origin.startsWith(o + ":"));
                if (!allowed) {
                    LOG.log(Level.WARNING, "MCP request rejected: unexpected Origin header: {0}", origin);
                    sendError(exchange, 403, "Forbidden");
                    return null;
                }
            }
            String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
            if (contentType == null || !contentType.startsWith("application/json")) {
                sendError(exchange, 415, "Unsupported Media Type");
                return null;
            }
            return filter;
        }

        private Map<String, Object> handleJsonRpc(Map<String, Object> request,
                Set<String> sessionFilter) throws Exception {
            String method = (String) request.get("method");
            Object id = request.get("id");
            @SuppressWarnings("unchecked")
            Map<String, Object> params = (Map<String, Object>) request.getOrDefault("params", Map.of());

            if (method == null) {
                return errorResponse(id, -32600, "Invalid request: missing method");
            }

            LOG.log(Level.FINE, "MCP method: {0}, id: {1}", new Object[]{method, id});

            return switch (method) {
                case "initialize" -> handleInitialize(id);
                case "notifications/initialized" -> null;
                case "tools/list" -> handleToolsList(id, sessionFilter);
                case "tools/call" -> handleToolsCall(id, params, sessionFilter);
                case "ping" -> successResponse(id, Map.of());
                default -> errorResponse(id, -32601, "Method not found: " + method);
            };
        }

        private Map<String, Object> handleInitialize(Object id) {
            Map<String, Object> capabilities = new LinkedHashMap<>();
            capabilities.put("tools", Map.of("listChanged", false));

            Map<String, Object> serverInfo = new LinkedHashMap<>();
            serverInfo.put("name", SERVER_NAME);
            serverInfo.put("version", "1.0.0");

            Map<String, Object> result = new LinkedHashMap<>();
            result.put("protocolVersion", MCP_PROTOCOL_VERSION);
            result.put("capabilities", capabilities);
            result.put("serverInfo", serverInfo);

            return successResponse(id, result);
        }

        private Map<String, Object> handleToolsList(Object id, Set<String> filter) {
            List<McpToolProvider.McpToolDefinition> tools = filter.isEmpty()
                    ? registry.getAllTools() : registry.getAllAvailableTools();
            List<Map<String, Object>> toolList = new ArrayList<>();
            for (McpToolProvider.McpToolDefinition tool : tools) {
                if (!filter.isEmpty() && !filter.contains(tool.name())) {
                    continue;
                }
                Map<String, Object> toolMap = new LinkedHashMap<>();
                toolMap.put("name", tool.name());
                toolMap.put("description", tool.description());
                toolMap.put("inputSchema", tool.inputSchema());
                toolList.add(toolMap);
            }
            return successResponse(id, Map.of("tools", toolList));
        }

        private Map<String, Object> handleToolsCall(Object id, Map<String, Object> params,
                Set<String> filter) {
            String name = (String) params.get("name");
            if (name == null) {
                return errorResponse(id, -32602, "Invalid params: missing tool name");
            }
            if (!filter.isEmpty() && !filter.contains(name)) {
                return errorResponse(id, -32602, "Tool not enabled: " + name);
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", Map.of());
            try {
                Map<String, Object> toolResult = registry.executeTool(name, arguments);
                List<Map<String, Object>> content = new ArrayList<>();
                Object text = toolResult.get("text");
                if (text != null) {
                    content.add(Map.of("type", "text", "text", text.toString()));
                } else {
                    content.add(Map.of("type", "text", "text", mapper.writeValueAsString(toolResult)));
                }
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("content", content);
                result.put("isError", "error".equals(toolResult.get("status")));
                return successResponse(id, result);
            } catch (IllegalArgumentException ex) {
                return errorResponse(id, -32602, ex.getMessage());
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Tool execution failed: " + name, ex);
                List<Map<String, Object>> content = List.of(
                        Map.of("type", "text", "text",
                                "Error (" + ex.getClass().getSimpleName() + "): " + ex.getMessage()));
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("content", content);
                result.put("isError", true);
                return successResponse(id, result);
            }
        }

        private Map<String, Object> successResponse(Object id, Map<String, Object> result) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("jsonrpc", "2.0");
            response.put("id", id);
            response.put("result", result);
            return response;
        }

        private Map<String, Object> errorResponse(Object id, int code, String message) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("jsonrpc", "2.0");
            response.put("id", id);
            response.put("error", Map.of("code", code, "message", message));
            return response;
        }

        private void sendJson(HttpExchange exchange, int statusCode, Map<String, Object> data) throws IOException {
            byte[] responseBytes = mapper.writeValueAsBytes(data);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }

        private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
            byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(statusCode, responseBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        }
    }
}
