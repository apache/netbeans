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
package org.netbeans.lib.chrome_devtools_protocol;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.chrome_devtools_protocol.json.Endpoint;


public class ChromeDevToolsClient implements Closeable {

    private static final Logger LOG = Logger.getLogger(ChromeDevToolsClient.class.getName());

    // node does not tollerate http2 upgrades
    private static final HttpClient client = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
    private static final Gson gson = new Gson();

    private final AtomicInteger idSupplier = new AtomicInteger();
    private final DebuggerDomain debuggerDomain;
    private final RuntimeDomain runtimeDomain;
    private final ConcurrentHashMap<Integer,CompletableFuture<JsonElement>> callbacks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Consumer<JsonElement>>> eventHandler = new ConcurrentHashMap<>();
    private final URI websocketUri;
    private WebSocket webSocket;

    public static Endpoint[] listEndpoints(String hostname, int port) throws IOException {
        try {
            URI jsonUri = new URI("http", null, hostname, port, "/json", null, null);
            HttpResponse<String> result = client.send(
                    HttpRequest.newBuilder(jsonUri).GET().build(),
                    HttpResponse.BodyHandlers.ofString()
            );
            if (result.statusCode() == 404) {
                jsonUri = new URI("http", null, hostname, port, "/json/list", null, null);
                result = client.send(
                        HttpRequest.newBuilder(jsonUri).GET().build(),
                        HttpResponse.BodyHandlers.ofString()
                );
            }
            if(result.statusCode() != 200) {
                throw new IOException("Failed to fetch endpoint list (http-status: " + result.statusCode() + ")" + result.body());
            }
            return gson.fromJson(result.body(), Endpoint[].class);
        } catch (InterruptedException | URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    public ChromeDevToolsClient(URI websocketUri) {
        this.websocketUri = websocketUri;
        debuggerDomain = new DebuggerDomain(this);
        runtimeDomain = new RuntimeDomain(this);
    }

    public void connect() {
        if(webSocket != null) {
            try {
                close();
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Failed to close previous connection", ex);
            }
        }
        try {
            webSocket = client.newWebSocketBuilder().buildAsync(websocketUri, new WebSocket.Listener() {
                StringBuilder sb = new StringBuilder();

                @Override
                public void onOpen(WebSocket webSocket) {
                    webSocket.request(1);
                }

                @Override
                public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                    return null;
                }

                @Override
                public void onError(WebSocket webSocket, Throwable error) {
                    callbacks.values().stream().forEach(c -> c.completeExceptionally(error));
                }

                @Override
                public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                    sb.append(data);
                    if(last) {
                        String result = sb.toString();
                        LOG.log(Level.FINE, "Received: {0}", result);
                        dispatchMethod(result);
                        sb.setLength(0);
                    }
                    webSocket.request(1);
                    return null;
                }

                private void dispatchMethod(String data) {
                    JsonElement jsonElement = gson.fromJson(data, JsonElement.class);
                    if(jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("id")) {
                        int id = jsonElement.getAsJsonObject().getAsJsonPrimitive("id").getAsInt();
                        CompletableFuture<JsonElement> callback = callbacks.remove(id);
                        if(callback != null) {
                            if(jsonElement.getAsJsonObject().has("error")) {
                                try {
                                    ErrorData ed = gson.fromJson(jsonElement.getAsJsonObject().get("error"), ErrorData.class);
                                    callback.completeExceptionally(new DebuggerException(ed.getCode(), ed.getMessage(), ed.getData()));
                                } catch (Exception ex) {
                                    throw new DebuggerException(jsonElement.getAsJsonObject().get("error").toString());
                                }
                            } else {
                                callback.complete(jsonElement.getAsJsonObject().get("result"));
                            }
                        }
                    } else if (jsonElement.isJsonObject() && jsonElement.getAsJsonObject().has("method")) {
                        List<Consumer<JsonElement>> handlers = eventHandler.getOrDefault(
                                jsonElement.getAsJsonObject().getAsJsonPrimitive("method").getAsString(),
                                Collections.emptyList());
                        for(Consumer<JsonElement> handler: handlers) {
                            handler.accept(jsonElement.getAsJsonObject().get("params"));
                        }
                    }
                }
            }).get();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch (ExecutionException ex) {
            if(ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ex.getCause();
            } else {
                throw new RuntimeException(ex.getCause());
            }
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (this.webSocket != null) {
                this.webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "").get();
                this.webSocket = null;
            }
            for (CompletableFuture<JsonElement> cf : callbacks.values()) {
                cf.completeExceptionally(new DebuggerException("Connection closed"));
            }
            callbacks.clear();
        } catch (InterruptedException ex) {
        } catch (ExecutionException ex) {
            if(ex.getCause() instanceof IOException) {
                throw (IOException) ex.getCause();
            } else if (ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ex.getCause();
            } else {
                throw new RuntimeException(ex.getCause());
            }
        }
    }

    public boolean connected() {
        return this.webSocket != null;
    }

    public RuntimeDomain getRuntime() {
        return runtimeDomain;
    }

    public DebuggerDomain getDebugger() {
        return debuggerDomain;
    }

    Gson getGson() {
        return gson;
    }

    int getId() {
        return idSupplier.getAndIncrement();
    }

    CompletionStage<JsonElement> methodCall(String method, Object parameters) {
        int id = getId();
        MethodCall methodCall = new MethodCall();
        methodCall.setId(id);
        methodCall.setMethod(method);
        if(parameters != null) {
            methodCall.setParams(parameters);
        }
        CompletableFuture<JsonElement> resultProvider = new CompletableFuture<>();
        callbacks.put(id, resultProvider);

        String jsonPayload = gson.toJson(methodCall);
        LOG.log(Level.FINE, "methodCall: {0}", jsonPayload);

        webSocket.sendText(jsonPayload, true)
                .whenComplete((ws, ex) -> {
                    if(ex != null) {
                        resultProvider.completeExceptionally(ex);
                        callbacks.remove(id);
                    }
                });
        return resultProvider;
    }

    void registerEventHandler(String event, Consumer<JsonElement> handler) {
        eventHandler.computeIfAbsent(event, (s) -> new CopyOnWriteArrayList<>())
                .add(handler);
    }

    void unregisterEventHandler(String event, Consumer<JsonElement> handler) {
        eventHandler.computeIfAbsent(event, (s) -> new CopyOnWriteArrayList<>())
                .remove(handler);
    }
}
