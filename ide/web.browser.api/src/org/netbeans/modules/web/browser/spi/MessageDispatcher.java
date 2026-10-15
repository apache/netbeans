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
package org.netbeans.modules.web.browser.spi;

import java.util.ArrayList;
import java.util.List;

/**
 * Dispatcher of messages from a web-browser pane to features implemented
 * on top of the pane.
 *
 * @author Jan Stola
 */
public class MessageDispatcher {
    /** Listeners interested in messages from this dispatcher. */
    private final List<MessageListener> listeners = new ArrayList<MessageListener>();

    /**
     * Adds a listener to this message dispatcher.
     * 
     * @param listener listener interested in messages from this dispatcher.
     */
    public void addMessageListener(MessageListener listener) {
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Removes a listener from this message dispatcher.
     * 
     * @param listener listener to unregister.
     */
    public void removeMessageListener(MessageListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Dispatches a new message to all registered listeners.
     * 
     * @param featureId ID of a feature the message being dispatched is related to.
     * @param message message to dispatch.
     */
    protected void dispatchMessage(String featureId, String message) {
        MessageListener[] listenersCopy;
        synchronized(listeners) {
            listenersCopy = listeners.toArray(new MessageListener[0]);
        }
        for (MessageListener listener : listenersCopy) {
            listener.messageReceived(featureId, message);
        }
    }

    /**
     * Message listener.
     */
    public static interface MessageListener {
        /**
         * Invoked when a new message is being dispatched by the message dispatcher.
         * 
         * @param featureId ID of a feature the message being dispatched is related to.
         * @param message message to dispatch.
         */
        void messageReceived(String featureId, String message);
    }
    
}
