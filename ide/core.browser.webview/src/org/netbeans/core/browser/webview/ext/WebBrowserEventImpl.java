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

package org.netbeans.core.browser.webview.ext;

import org.netbeans.core.browser.api.*;
import java.awt.AWTEvent;
import org.w3c.dom.Node;

/**
 * Browser event implementation.
 * 
 * @author S. Aubrecht
 */
class WebBrowserEventImpl extends WebBrowserEvent {

    private final int type;
    private final WebBrowser browser;
    private final Node node;
    private final AWTEvent event;
    private boolean cancelled = false;
    private final String url;

    public WebBrowserEventImpl( int type, WebBrowser browser, String url ) {
        this.type = type;
        this.browser = browser;
        this.url = url;
        this.event = null;
        this.node = null;
    }

    public WebBrowserEventImpl( int type, WebBrowser browser, AWTEvent event, Node node ) {
        this.type = type;
        this.browser = browser;
        this.event = event;
        this.node = node;
        this.url = null;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public WebBrowser getWebBrowser() {
        return browser;
    }

    @Override
    public String getURL() {
        return null != url ? url : browser.getURL();
    }

    @Override
    public AWTEvent getAWTEvent() {
        return event;
    }

    @Override
    public Node getNode() {
        return node;
    }

    @Override
    public void cancel() {
        cancelled = true;
    }

    boolean isCancelled() {
        return cancelled;
    }
}
