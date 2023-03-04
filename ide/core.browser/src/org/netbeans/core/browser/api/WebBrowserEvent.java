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

package org.netbeans.core.browser.api;

import java.awt.AWTEvent;
import org.w3c.dom.Node;

/**
 *
 * @author S. Aubrecht
 */
public abstract class WebBrowserEvent {

    /**
     * Browser is about to load a new URL.
     */
    public static final int WBE_LOADING_STARTING = 1;

    /**
     * Browser started loading a new URL.
     */
    public static final int WBE_LOADING_STARTED = 2;

    /**
     * Browser finished loading
     */
    public static final int WBE_LOADING_ENDED = 3;

    /**
     * Mouse event in browser component.
     */
    public static final int WBE_MOUSE_EVENT = 4;

    /**
     * Key event in browser component.
     */
    public static final int WBE_KEY_EVENT = 5;



    /**
     * @return Event type.
     */
    public abstract int getType();

    /**
     * @return Browser component the event originated from.
     */
    public abstract WebBrowser getWebBrowser();

    /**
     * @return URL associated with the event or null.
     */
    public abstract String getURL();

    /**
     * @return AWT event (MouseEvent or KeyEvent) or null.
     */
    public abstract AWTEvent getAWTEvent();

    /**
     * @return Document associated with the event (WBE_MOUSE_EVENT and WBE_KEY_EVENT only) or null.
     */
    public abstract Node getNode();

    /**
     * Invoke this method to abort loading of URL when event type is WBE_LOADING_STARTING.
     * Has no effect for other event types.
     */
    public abstract void cancel();
}
