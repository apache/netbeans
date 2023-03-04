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
package org.netbeans.modules.web.webkit.debugging.api.network;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.LiveHTML;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.console.ConsoleMessage;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;

/**
 * Java wrapper of the Network domain of WebKit Remote Debugging Protocol.
 * 
 * @author David Konecny, Jan Stola
 */
public class Network {
    private final TransportHelper transport;
    private boolean enabled;
    private final Callback callback;
    private final WebKitDebugging webKit;
    private int numberOfClients = 0;
    private boolean inLiveHTMLMode = false;
    private final List<Listener> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, Request> activeRequests = new HashMap<>();
    private final Map<String, WebSocketRequest> activeWebSocketRequests = new HashMap<>();
    
    public Network(TransportHelper transport, WebKitDebugging webKit) {
        this.transport = transport;
        this.callback = new Callback();
        this.transport.addListener(callback);
        this.webKit = webKit;
    }

    public void enable() {
        numberOfClients++;
        if (!enabled) {
            enabled = true;
            transport.sendBlockingCommand(new Command("Network.enable")); // NOI18N
        }
        inLiveHTMLMode = webKit.getDebugger().isInLiveHTMLMode();
    }

    public void disable() {
        assert numberOfClients > 0;
        numberOfClients--;
        if (numberOfClients == 0) {
            transport.sendCommand(new Command("Network.disable")); // NOI18N
            enabled = false;
        }
    }

    private String getResponseBody(String requestId) {
        JSONObject params = new JSONObject();
        params.put("requestId", requestId); // NOI18N
        Response response = transport.sendBlockingCommand(new Command("Network.getResponseBody", params)); // NOI18N
        if (response != null) {
            JSONObject result = response.getResult();
            if (result != null) {
                // XXX: check here base64Encoded property and decode it if necessary
                return String.valueOf(result.get("body")); // NOI18N
            }
        }
        return null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    private void recordDataEvent(long timeStamp, String id, String request, String mime) {
        assert inLiveHTMLMode;
        // TODO: fetch request here as well
        String response = getResponseBody(id);
        LiveHTML.getDefault().storeDataEvent(transport.getConnectionURL(), timeStamp, response, request, mime);
    }

    private void requestReceived(JSONObject params) {
        Request req = new Request(this, params);
        activeRequests.put(req.getRequestId(), req);
        fireNetworkRequest(req);
    }

    private void responseReceived(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId")); // NOI18N
        assert requestId != null;
        Request req = activeRequests.get(requestId);
        if (req == null) {
            // ignore this. I noticed that WebKit protocol sometimes sends
            // duplicate messages. for example POST on a Java REST service
            // results into OPTIONS message first followed by POST itself.
            // webkit protocol send OPTIONS and gets response and data and
            // a message that loading is finished (ie. Network.loadingFinished) yet (sometimes?)
            // another Network.responseReceived followed by Network.loadingFinished
            // can be received for the same requestId
            return;
        }
        req.setResponse(params);
    }

    private void requestFailed(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId")); // NOI18N
        assert requestId != null;
        Request req = activeRequests.remove(requestId);
        if (req == null) {
            // see comment in responseReceived()
            return;
        }
        req.setFailed(params);
    }

    private void dataReceived(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId")); // NOI18N
        assert requestId != null;
        Request req = activeRequests.get(requestId);
        if (req == null) {
            // see comment in responseReceived()
            return;
        }
        req.dataLoadingStarted();
    }

    private void responseFinished(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId")); // NOI18N
        assert requestId != null;
        Request req = activeRequests.remove(requestId);
        if (req == null) {
            // see comment in responseReceived()
            return;
        }
        req.requestCompleted();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    private void fireNetworkRequest(Request req) {
        for (Listener l : listeners) {
            l.networkRequest(req);
        }
    }

    private void webSocketCreated(JSONObject params) {
        WebSocketRequest req = new WebSocketRequest(params);
        activeWebSocketRequests.put(req.getRequestId(), req);
        fireWebSocketNetworkRequest(req);
    }

    private void webSocketHandshakeRequest(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId")); // NOI18N
        assert requestId != null;
        WebSocketRequest req = activeWebSocketRequests.get(requestId);
        if (req == null) {
            return;
        }
        req.setHandshakeRequest(params);
    }

    private void webSocketHandshakeResponse(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId")); // NOI18N
        assert requestId != null;
        WebSocketRequest req = activeWebSocketRequests.get(requestId);
        if (req == null) {
            return;
        }
        req.setHandshakeResponse(params);
    }

    private void webSocketFrameSent(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId")); // NOI18N
        assert requestId != null;
        WebSocketRequest req = activeWebSocketRequests.get(requestId);
        if (req == null) {
            return;
        }
        req.addFrame(Direction.SEND, params);
    }

    private void webSocketFrameReceived(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId")); // NOI18N
        assert requestId != null;
        WebSocketRequest req = activeWebSocketRequests.get(requestId);
        if (req == null) {
            return;
        }
        req.addFrame(Direction.RECEIVED, params);
    }

    private void webSocketFrameError(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId")); // NOI18N
        assert requestId != null;
        WebSocketRequest req = activeWebSocketRequests.get(requestId);
        if (req == null) {
            return;
        }
        req.setFrameError(params);
    }

    private void webSocketClosed(JSONObject params) {
        String requestId = String.valueOf(params.get("requestId")); // NOI18N
        assert requestId != null;
        WebSocketRequest req = activeWebSocketRequests.remove(requestId);
        if (req == null) {
            return;
        }
        req.close();
    }

    private void fireWebSocketNetworkRequest(WebSocketRequest req) {
        for (Listener l : listeners) {
            l.webSocketRequest(req);
        }
    }

    /**
     * Listener for events about network events.
     */
    public static interface Listener {

        /**
         * New network request was created.
         * 
         * @param request new network request.
         */
        void networkRequest(Request request);

        /**
         * New WebSocket request was created.
         * 
         * @param request new web-socket request.
         */
        void webSocketRequest(WebSocketRequest request);

    }

    public static final class Request {

        public static final String PROP_RESPONSE = "Network.Request.Response"; // NOI18N
        public static final String PROP_RESPONSE_DATA = "Network.Request.Response.Data"; // NOI18N

        private final JSONObject request;
        private final JSONObject initiator;
        private final String requestId;
        private String responseType;
        private JSONObject response;
        private final PropertyChangeSupport support = new PropertyChangeSupport(this);
        private boolean hasData = false;
        private boolean dataReady = false;
        private final Network network;
        private boolean failed = false;
        private final String documentUrl;

        private Request(Network network, JSONObject params) {
            this.request = (JSONObject)params.get("request"); // NOI18N
            this.initiator = (JSONObject)params.get("initiator"); // NOI18N
            this.requestId = String.valueOf(params.get("requestId")); // NOI18N
            this.network = network;
            this.documentUrl = (String)params.get("documentURL"); // NOI18N
        }

        public String getInitiatorType() {
            return (String)getInitiator().get("type"); // NOI18N
        }

        public String getDocumentUrl() {
            return documentUrl;
        }

        public String getResponseType() {
            return responseType;
        }

        public JSONObject getInitiator() {
            return initiator;
        }

        public JSONObject getRequest() {
            return request;
        }

        private String getRequestId() {
            return requestId;
        }

        public JSONObject getResponse() {
            return response;
        }

        public int getResponseCode() {
            if (response != null) {
                Number statusCode = (Number)response.get("status"); // NOI18N
                if (statusCode != null) {
                    return statusCode.intValue();
                }
            }
            return -1;
        }

        private void setResponse(JSONObject response) {
            this.response = (JSONObject)response.get("response"); // NOI18N
            this.responseType = String.valueOf(response.get("type")); // NOI18N
            support.firePropertyChange(PROP_RESPONSE, null, null);
        }

        public String getResponseData() {
            if (!dataReady) {
                return null;
            }
            return network.getResponseBody(getRequestId());
        }

        private void dataLoadingStarted() {
            hasData = true;
        }

        private void requestCompleted() {
            if (hasData) {
                dataReady = true;
                support.firePropertyChange(PROP_RESPONSE_DATA, null, null);
            }
        }
        
        public boolean hasData() {
            return hasData;
        }

        public List<ConsoleMessage.StackFrame> getInitiatorCallStack() {
            JSONArray stack = (JSONArray)getInitiator().get("stackTrace"); // NOI18N
            if (stack == null) {
                JSONObject stackObj = (JSONObject) getInitiator().get("stack"); // NOI18N
                if (stackObj != null) {
                    stack = (JSONArray) stackObj.get("callFrames");             // NOI18N
                }
            }
            if (stack != null && stack.size() > 0) {
                List<ConsoleMessage.StackFrame> stackTrace = new ArrayList<>();
                for (Object o : stack) {
                    JSONObject json = (JSONObject)o;
                    stackTrace.add(new ConsoleMessage.StackFrame(json));
                }
                return stackTrace;
            }
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            support.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            support.removePropertyChangeListener(l);
        }

        private void setFailed(JSONObject params) {
            // there is "errorText" in params but it is always empty so I ignore it for now
            failed = true;
            support.firePropertyChange(PROP_RESPONSE, null, null);
        }

        public boolean isFailed() {
            return failed;
        }

    }

    public static final class WebSocketRequest {

        public static final String PROP_HANDSHAKE_REQUEST = "Network.WebSocketRequest.Handshake.Request"; // NOI18N
        public static final String PROP_HANDSHAKE_RESPONSE = "Network.WebSocketRequest.Handshake.Response"; // NOI18N
        public static final String PROP_FRAMES = "Network.WebSocketRequest.Frame"; // NOI18N
        public static final String PROP_CLOSED = "Network.WebSocketRequest.Closed"; // NOI18N

        private final String requestId;
        private final String url;
        private JSONObject handshakeRequest;
        private JSONObject handshakeResponse;
        private final PropertyChangeSupport support = new PropertyChangeSupport(this);
        private final List<WebSocketFrame> frames = new ArrayList<>();
        private boolean closed = false;
        private String errorMessage = null;

        private WebSocketRequest(JSONObject params) {
            this.requestId = String.valueOf(params.get("requestId")); // NOI18N
            this.url = String.valueOf(params.get("url")); // NOI18N
        }

        private String getRequestId() {
            return requestId;
        }

        public String getURL() {
            return url;
        }

        public JSONObject getHandshakeRequest() {
            return handshakeRequest;
        }

        private void setHandshakeRequest(JSONObject params) {
            this.handshakeRequest = (JSONObject)params.get("request"); // NOI18N
            support.firePropertyChange(PROP_HANDSHAKE_REQUEST, null, null);
        }
        public JSONObject getHandshakeResponse() {
            return handshakeResponse;
        }

        private void setHandshakeResponse(JSONObject params) {
            this.handshakeResponse = (JSONObject)params.get("response"); // NOI18N
            support.firePropertyChange(PROP_HANDSHAKE_RESPONSE, null, null);
        }

        private void setFrameError(JSONObject params) {
            this.errorMessage = (String)params.get("errorMessage"); // NOI18N
            support.firePropertyChange(PROP_FRAMES, null, null);
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        private void addFrame(Direction direction, JSONObject params) {
            JSONObject response = (JSONObject)params.get("response"); // NOI18N
            Number timestamp = (Number)params.get("timestamp"); // NOI18N
            Number opcode = (Number)response.get("opcode"); // NOI18N
            WebSocketFrame frame = new WebSocketFrame(new Date(
                    timestamp.longValue()), direction, response, opcode.intValue());
            frames.add(frame);
            support.firePropertyChange(PROP_FRAMES, null, null);
        }

        public List<WebSocketFrame> getFrames() {
            return new ArrayList<>(frames);
        }

        public boolean isClosed() {
            return closed;
        }

        private void close() {
            this.closed = true;
            support.firePropertyChange(PROP_CLOSED, null, null);
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
            support.addPropertyChangeListener(l);
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
            support.addPropertyChangeListener(l);
        }
    }

    public enum Direction {
        SEND,
        RECEIVED,
    }
    
    public static final class WebSocketFrame {

        private final Direction direction;
        private final JSONObject data;
        private final Date timestamp;
        private final int opcode;

        private WebSocketFrame(Date timestamp, Direction direction, JSONObject data, int opcode) {
            this.timestamp = timestamp;
            this.direction = direction;
            this.data = data;
            this.opcode = opcode;
        }

        public String getPayload() {
            return String.valueOf(data.get("payloadData")); // NOI18N
        }

        public Direction getDirection() {
            return direction;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public int getOpcode() {
            return opcode;
        }

    }

    private class Callback implements ResponseCallback {

        @Override
        public void handleResponse(Response response) {
            String method = response.getMethod();
            JSONObject params = response.getParams();
            if ("Network.requestWillBeSent".equals(method)) { // NOI18N
                requestReceived(params);
            } else if ("Network.responseReceived".equals(method)) { // NOI18N
                responseReceived(params);

                // LiveHTML support:
                if (inLiveHTMLMode && 
                        "XHR".equals(params.get("type"))) { // NOI18N
                    final long timeStamp = System.currentTimeMillis();
                    final String id = (String)params.get("requestId"); // NOI18N
                    final String request = (String)((JSONObject)params.get("response")).get("url"); // NOI18N
                    final String mime = (String)((JSONObject)params.get("response")).get("mimeType"); // NOI18N
                    transport.getRequestProcessor().post(new Runnable() {
                        @Override
                        public void run() {
                            recordDataEvent(timeStamp, id, request, mime);
                        }
                    });
                }
            } else if ("Network.loadingFailed".equals(method)) { // NOI18N
                requestFailed(params);
                

// TODO: XXX: handle requestServedFromMemoryCache here as well


            } else if ("Network.dataReceived".equals(method)) { // NOI18N
                dataReceived(params);
            } else if ("Network.loadingFinished".equals(method)) { // NOI18N
                responseFinished(params);
            } else if ("Network.webSocketCreated".equals(method)) { // NOI18N
                webSocketCreated(params);
            } else if ("Network.webSocketWillSendHandshakeRequest".equals(method)) { // NOI18N
                webSocketHandshakeRequest(params);
            } else if ("Network.webSocketHandshakeResponseReceived".equals(method)) { // NOI18N
                webSocketHandshakeResponse(params);
            } else if ("Network.webSocketFrameSent".equals(method)) { // NOI18N
                webSocketFrameSent(params);
            } else if ("Network.webSocketFrameReceived".equals(method)) { // NOI18N
                webSocketFrameReceived(params);
            } else if ("Network.webSocketFrameError".equals(method)) { // NOI18N
                webSocketFrameError(params);
            } else if ("Network.webSocketClosed".equals(method)) { // NOI18N
                webSocketClosed(params);
            }
        }

    }
    
    
}
