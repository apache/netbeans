/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
