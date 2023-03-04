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
package org.netbeans.modules.web.webkit.debugging.api;

import org.netbeans.api.debugger.Session;
import org.netbeans.modules.web.webkit.debugging.spi.BrowserConsoleLoggerFactory;
import org.netbeans.modules.web.webkit.debugging.spi.JavaScriptDebuggerFactory;
import org.netbeans.modules.web.webkit.debugging.spi.NetworkMonitorFactory;
import org.openide.util.Lookup;

/**
 * Manager for UI integration of webkit protocol into NetBeans.
 */
public final class WebKitUIManager {

    private static final WebKitUIManager DEFAULT = new WebKitUIManager();

    private JavaScriptDebuggerFactory debuggerFactory;
    private BrowserConsoleLoggerFactory browserLoggerFactory;
    private NetworkMonitorFactory networkMonitorFactory;

    private WebKitUIManager() {
    }

    public static WebKitUIManager getDefault() {
        return DEFAULT;
    }

    private synchronized JavaScriptDebuggerFactory getDebuggerFactory() {
        if (debuggerFactory == null) {
            debuggerFactory = Lookup.getDefault().lookup(JavaScriptDebuggerFactory.class);
        }
        return debuggerFactory;
    }

    public Session createDebuggingSession(WebKitDebugging webkit, Lookup projectContext) {
        JavaScriptDebuggerFactory fact = getDebuggerFactory();
        if (fact == null) {
            return null;
        }
        return fact.createDebuggingSession(webkit, projectContext);
    }

    public void stopDebuggingSession(Session session) {
        JavaScriptDebuggerFactory fact = getDebuggerFactory();
        if (fact == null) {
            return;
        }
        fact.stopDebuggingSession(session);
    }

    private synchronized BrowserConsoleLoggerFactory getBrowserLoggerFactory() {
        if (browserLoggerFactory == null) {
            browserLoggerFactory = Lookup.getDefault().lookup(BrowserConsoleLoggerFactory.class);
        }
        return browserLoggerFactory;
    }

    public Lookup createBrowserConsoleLogger(WebKitDebugging webkit, Lookup projectContext) {
        BrowserConsoleLoggerFactory fact = getBrowserLoggerFactory();
        if (fact == null) {
            return null;
        }
        return fact.createBrowserConsoleLogger(webkit, projectContext);
    }

    public void stopBrowserConsoleLogger(Lookup session) {
        BrowserConsoleLoggerFactory fact = getBrowserLoggerFactory();
        if (fact == null) {
            return;
        }
        fact.stopBrowserConsoleLogger(session);
    }

    private synchronized NetworkMonitorFactory getNetworkMonitorFactory() {
        if (networkMonitorFactory == null) {
            networkMonitorFactory = Lookup.getDefault().lookup(NetworkMonitorFactory.class);
        }
        return networkMonitorFactory;
    }

    public Lookup createNetworkMonitor(WebKitDebugging webkit, Lookup projectContext) {
        NetworkMonitorFactory fact = getNetworkMonitorFactory();
        if (fact == null) {
            return null;
        }
        return fact.createNetworkMonitor(webkit, projectContext);
    }

    public void stopNetworkMonitor(Lookup session) {
        NetworkMonitorFactory fact = getNetworkMonitorFactory();
        if (fact == null) {
            return;
        }
        fact.stopNetworkMonitor(session);
    }
}
