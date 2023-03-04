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
package org.netbeans.modules.web.webkit.debugging.api.page;

import org.json.simple.JSONObject;
import org.netbeans.modules.web.webkit.debugging.TransportHelper;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;

/**
 * Java wrapper of the Page domain of WebKit Remote Debugging Protocol.
 * 
 * @author Jan Stola
 */
public class Page {
    /** Transport used by this instance. */
    private final TransportHelper transport;
    /** Determines if Page domain notifications are enabled. */
    private boolean enabled;
    /** Callback for Page event notifications. */
    private final Callback callback;
    /** Number of clients interested in Page domain notifications. */
    private int numberOfClients = 0;

    /**
     * Creates a new wrapper for the Page domain of WebKit Remote Debugging Protocol.
     * 
     * @param transport transport to use.
     * @param webKit WebKit remote debugging API wrapper to use.
     */
    public Page(TransportHelper transport, WebKitDebugging webKit) {
        this.transport = transport;
        this.callback = new Callback();
        this.transport.addListener(callback);
    }

    /**
     * Enables Page domain notifications.
     */
    public void enable() {
        numberOfClients++;
        if (!enabled) {
            enabled = true;
            transport.sendBlockingCommand(new Command("Page.enable")); // NOI18N
        }
    }

    /**
     * Disables Page domain notifications.
     */
    public void disable() {
        assert numberOfClients > 0;
        numberOfClients--;
        if (numberOfClients == 0) {
            transport.sendCommand(new Command("Page.disable")); // NOI18N
            enabled = false;
        }
    }

    /**
     * Reloads the page optionally ignoring the cache.
     * 
     * @param ignoreCache if true then the browser cache is ignored
     * (as if the user pressed Shift+refresh).
     * @param scriptToEvaluateOnLoad if non-null then the script will
     * be injected into all frames of the inspected page after reload.
     */
    public void reload(boolean ignoreCache, String scriptToEvaluateOnLoad) {
        reload(ignoreCache, scriptToEvaluateOnLoad, null);
    }

    /**
     * Reloads the page optionally ignoring the cache.
     * 
     * @param ignoreCache if true then the browser cache is ignored
     * (as if the user pressed Shift+refresh).
     * @param scriptToEvaluateOnLoad if non-null then the script will
     * be injected into all frames of the inspected page after reload.
     * @param scriptPreprocessor script body that should evaluate to function
     * that will preprocess all the scripts before their compilation.
     */
    public void reload(boolean ignoreCache, String scriptToEvaluateOnLoad, String scriptPreprocessor) {
        JSONObject pars = new JSONObject();
        pars.put("ignoreCache", ignoreCache); // NOI18N
        if (scriptToEvaluateOnLoad != null) {
            pars.put("scriptToEvaluateOnLoad", scriptToEvaluateOnLoad); // NOI18N
        }
        if (scriptPreprocessor != null) {
            pars.put("scriptPreprocessor", scriptPreprocessor); // NOI18N
        }
        transport.sendBlockingCommand(new Command("Page.reload", pars)); // NOI18N
    }

    /**
     * Navigates the page to the given URL.
     * 
     * @param url URL to navigate to.
     */
    public void navigate(String url) {
        JSONObject urlPar = new JSONObject();
        urlPar.put("url", url); // NOI18N
        transport.sendBlockingCommand(new Command("Page.navigate", urlPar)); // NOI18N
    }

    /**
     * Determines whether Page domain notifications are enabled.
     * 
     * @return {@code true} when Page domain notifications are enabled,
     * returns {@code false} otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Callback for Page domain notifications.
     */
    private static class Callback implements ResponseCallback {

        @Override
        public void handleResponse(Response response) {
//            if ("Page.loadEventFired".equals(response.getMethod())) {
//            } else if ("Page.domContentEventFired".equals(response.getMethod())) {
//            }
        }
        
    }
    
}
