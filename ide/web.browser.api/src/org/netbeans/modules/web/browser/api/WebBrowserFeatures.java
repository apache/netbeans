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
package org.netbeans.modules.web.browser.api;

public final class WebBrowserFeatures {

    private boolean netBeansIntegrationEnabled;
    private boolean jsDebuggerEnabled;
    private boolean pageInspectorEnabled;
    private boolean networkMonitorEnabled;
    private boolean consoleLoggerEnabled;
    private boolean liveHTMLEnabled;

    public WebBrowserFeatures() {
        this(true, true, true, true, true, false);
    }

    public WebBrowserFeatures(boolean netBeansIntegrationEnabled, boolean jsDebuggerEnabled,
            boolean pageInspectorEnabled, boolean networkMonitorEnabled,
            boolean consoleLoggerEnabled, boolean liveHTMLEnabled) {
        this.netBeansIntegrationEnabled = netBeansIntegrationEnabled;
        this.jsDebuggerEnabled = jsDebuggerEnabled;
        this.pageInspectorEnabled = pageInspectorEnabled;
        this.networkMonitorEnabled = networkMonitorEnabled;
        this.consoleLoggerEnabled = consoleLoggerEnabled;
        this.liveHTMLEnabled = liveHTMLEnabled;
    }

    public boolean isNetBeansIntegrationEnabled() {
        return netBeansIntegrationEnabled;
    }

    public boolean isPageInspectorEnabled() {
        return pageInspectorEnabled;
    }

    public boolean isLiveHTMLEnabled() {
        return liveHTMLEnabled;
    }

    public boolean isNetworkMonitorEnabled() {
        return networkMonitorEnabled;
    }

    public boolean isConsoleLoggerEnabled() {
        return consoleLoggerEnabled;
    }

    public boolean isJsDebuggerEnabled() {
        return jsDebuggerEnabled;
    }
}
