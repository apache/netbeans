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
package org.netbeans.modules.extbrowser.plugins;

import org.json.simple.JSONObject;
import org.netbeans.modules.extbrowser.chrome.ChromeBrowserImpl;
import org.netbeans.modules.web.browser.spi.PageInspectionHandle;

/**
 * {@code PageInspectionHandle} for external browser.
 *
 * @author Jan Stola
 */
public class PageInspectionHandleImpl implements PageInspectionHandle {
    /** Name of the attribute holding the message type. */
    private static final String MESSAGE_TYPE = "message"; // NOI18N
    /** Message type used by the page inspection handle. */
    private static final String PAGE_INSPECTION_PROPERTY_CHANGE = "pageInspectionPropertyChange"; // NOI18N
    /** Name of the attribute holding the name of the modified property. */
    private static final String PROPERTY_NAME = "propertyName"; // NOI18N
    /** Name of the attribute holding the value of the modified property. */
    private static final String PROPERTY_VALUE = "propertyValue"; // NOI18N
    /** Name of the selection mode property. */
    private static final String SELECTION_MODE = "selectionMode"; // NOI18N
    /** Name of the synchronize selection property. */
    private static final String SYNCHRONIZE_SELECTION = "synchronizeSelection"; // NOI18N
    /** Web-browser pane this handle belongs to. */
    private ChromeBrowserImpl browserImpl;

    /**
     * Creates a new {@code PageInspectionHandleImpl}.
     *
     * @param browserImpl web-browser pane this handle belongs to.
     */
    public PageInspectionHandleImpl(ChromeBrowserImpl browserImpl) {
        this.browserImpl = browserImpl;
    }

    @Override
    public void setSelectionMode(boolean selectionMode) {
        JSONObject message = new JSONObject();
        message.put(MESSAGE_TYPE, PAGE_INSPECTION_PROPERTY_CHANGE);
        message.put(PROPERTY_NAME, SELECTION_MODE);
        message.put(PROPERTY_VALUE, selectionMode);
        sendMessage(message.toJSONString());
    }

    @Override
    public void setSynchronizeSelection(boolean synchronizeSelection) {
        JSONObject message = new JSONObject();
        message.put(MESSAGE_TYPE, PAGE_INSPECTION_PROPERTY_CHANGE);
        message.put(PROPERTY_NAME, SYNCHRONIZE_SELECTION);
        message.put(PROPERTY_VALUE, synchronizeSelection);
        sendMessage(message.toJSONString());
    }

    /**
     * Sends a message to the inspected web-browser pane.
     *
     * @param message message to send.
     */
    private void sendMessage(String message) {
        ExternalBrowserPlugin.getInstance().sendMessage(message, browserImpl, ExternalBrowserPlugin.FEATURE_ROS);
    }

}
