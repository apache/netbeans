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
package org.netbeans.modules.web.webkit.debugging.api.console;

import java.util.EventListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.api.debugger.CallFrame;
import org.netbeans.modules.web.webkit.debugging.api.debugger.RemoteObject;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;

/**
 * JavaScript Console.
 * See Console section of WebKit Remote Debugging Protocol for more details.
 */
public final class Console {
    
    private static final Logger LOGGER = Logger.getLogger(Console.class.getName());
    
    private final TransportHelper transport;
    private final WebKitDebugging webKit;
    private boolean enabled;
    private final Callback callback;
    private int numberOfClients = 0;
    private final List<Console.Listener> listeners = new CopyOnWriteArrayList<Console.Listener>();
    private final InputCallback input = new InputCallback();
    
    public Console(TransportHelper transport, WebKitDebugging webKit) {
        this.transport = transport;
        this.webKit = webKit;
        this.callback = new Callback();
        this.transport.addListener(callback);
    }

    public void enable() {
        numberOfClients++;
        if (!enabled) {
            enabled = true;
            transport.sendBlockingCommand(new Command("Console.enable"));
        }
    }

    public void disable() {
        assert numberOfClients > 0;
        numberOfClients--;
        if (numberOfClients == 0) {
            transport.sendCommand(new Command("Console.disable"));
            enabled = false;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
    
    public void clearMessages() {
        transport.sendCommand(new Command("Console.clearMessages"));
    }
    
    /* hidden command
    public void setMonitoringXHREnabled(boolean monitoringXHREnabled) {
        JSONObject params = new JSONObject();
        params.put("enabled", Boolean.valueOf(monitoringXHREnabled));
        transport.sendBlockingCommand(new Command("Console.setMonitoringXHREnabled", params));
    }
    */
    
    public void addInspectedHeapObject(int heapObjectId) {
        JSONObject params = new JSONObject();
        params.put("heapObjectId", Integer.valueOf(heapObjectId));
        transport.sendBlockingCommand(new Command("Console.addInspectedHeapObject", params));
    }
    
    /**
     * Add a listener for console messages.
     * @param l a state change listener
     */
    public void addListener(Console.Listener l) {
        listeners.add(l);
    }
    
    /**
     * Remove a listener for console messages.
     * @param l a state change listener
     */
    public void removeListener(Console.Listener l) {
        listeners.remove(l);
    }
    
    private class Callback implements ResponseCallback {

        @Override
        public void handleResponse(final Response response) {
            String method = response.getMethod();
            if (method == null || !method.startsWith("Console")) {
                return;
            }
            if ("Console.messageAdded".equals(method)) {
                JSONObject msg = ((JSONObject)response.getParams().get("message"));
                final ConsoleMessage cm = new ConsoleMessage(msg);
                transport.getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        notifyConsoleMessage(cm);
                    }
                });
            } else if ("Console.messageRepeatCountUpdated".equals(method)) {
                final int count = ((Number) response.getParams().get("count")).intValue();
                transport.getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        notifyConsoleMessageRepeatCountUpdated(count);
                    }
                });
            } else if ("Console.messagesCleared".equals(method)) {
                transport.getRequestProcessor().post(new Runnable() {
                    @Override
                    public void run() {
                        notifyConsoleMessagesCleared();
                    }
                });
            } else {
                LOGGER.warning("Unknown console event: method = "+method);
            }
        }
        
    }
    
    private void notifyConsoleMessage(ConsoleMessage msg) {
        for (Console.Listener l : listeners ) {
            l.messageAdded(msg);
        }
    }
    
    private void notifyConsoleMessagesCleared() {
        for (Console.Listener l : listeners ) {
            l.messagesCleared();
        }
    }
    
    private void notifyConsoleMessageRepeatCountUpdated(int count) {
        for (Console.Listener l : listeners ) {
            l.messageRepeatCountUpdated(count);
        }
    }

    public void reset() {
        listeners.clear();
    }
    
    /**
     * Get the console input callback.
     * Use this to provide an input to the console.
     * @return the input callback.
     */
    public InputCallback getInput() {
        return input;
    }
    
    /**
     * Console listener.
     */
    public interface Listener extends EventListener {
        
        /**
         * A new console message was added.
         */
        void messageAdded(ConsoleMessage message);
        
        /**
         * Console messages are cleared.
         * This happens either upon <code>clearMessages</code> command or after page navigation.
         */
        void messagesCleared();
        
        /**
         * Called when subsequent message(s) are equal to the previous one(s).
         * @param count new repeat count value.
         */
        void messageRepeatCountUpdated(int count);
        
    }
    
    /**
     * Use this to provide an input to the console.
     */
    public final class InputCallback {
        
        /**
         * A line added to a console input.
         * @param line The line without the ending newline character.
         */
        public void line(String line) {
            RemoteObject result;
            CallFrame frame = webKit.getDebugger().getCurrentCallFrame();
            if (frame != null) {
                result = frame.evaluate(line);
            } else {
                result = webKit.getRuntime().evaluate(line);
            }
            if (result != null) {
                ConsoleMessage msg = new RemoteObjectMessage(webKit, result);
                notifyConsoleMessage(msg);
            }
        }

    }
    
}
