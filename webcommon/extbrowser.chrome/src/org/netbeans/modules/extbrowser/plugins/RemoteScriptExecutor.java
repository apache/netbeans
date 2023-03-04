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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.netbeans.modules.extbrowser.chrome.ChromeBrowserImpl;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.browser.spi.MessageDispatcher;
import org.netbeans.modules.web.browser.spi.MessageDispatcher.MessageListener;
import org.netbeans.modules.web.browser.spi.ScriptExecutor;
import org.openide.util.Lookup;

/**
 * Script executor for an external browser.
 *
 * @author Jan Stola
 */
public class RemoteScriptExecutor implements ScriptExecutor {
    /** Logger for this class. */
    private static final Logger LOG = Logger.getLogger(RemoteScriptExecutor.class.getName());
    // Message attributes
    private static final String MESSAGE_TYPE = "message"; // NOI18N
    private static final String MESSAGE_EVAL = "eval"; // NOI18N
    private static final String MESSAGE_ID = "id"; // NOI18N
    private static final String MESSAGE_SCRIPT = "script"; // NOI18N
    private static final String MESSAGE_STATUS = "status"; // NOI18N
    private static final String MESSAGE_STATUS_OK = "ok"; // NOI18N
    private static final String MESSAGE_RESULT = "result"; // NOI18N
    /** ID of the last message sent to the browser plugin. */
    private int lastIDSent = 0;
    /** ID of the last message received from the browser plugin. */
    private int lastIDReceived = 0;
    /** Lock guarding access to the modifiable state of the executor. */
    private final Object LOCK = new Object();
    /** Results of the executed scripts. It maps message ID to the result. */
    private Map<Integer,Object> results = new HashMap<Integer,Object>();
    /** Web-browser pane of this executor. */
    private ChromeBrowserImpl browserImpl;
    /** Determines whether the executor was initialized. */
    private boolean initialized;
    /** Determines whether the executor is active (i.e. ready to use). */
    private boolean active;

    /**
     * Creates a new {@code RemoteScriptExecutor}.
     * 
     * @param browserImpl web-browser pane of the executor. 
     */
    public RemoteScriptExecutor(ChromeBrowserImpl browserImpl) {
        this.browserImpl = browserImpl;
    }

    @Override
    public Object execute(String script) {
        synchronized (LOCK) {
            if (!active) {
                return ERROR_RESULT;
            }
            if (!initialized) {
                initialize();
            }
            int id = ++lastIDSent;
            JSONObject message = new JSONObject();
            message.put(MESSAGE_TYPE, MESSAGE_EVAL);
            message.put(MESSAGE_ID, id);
            message.put(MESSAGE_SCRIPT, script);
            ExternalBrowserPlugin.getInstance().sendMessage(
                    message.toJSONString(),
                    browserImpl,
                    PageInspector.MESSAGE_DISPATCHER_FEATURE_ID);
            try {
                do {
                    LOCK.wait();
                } while (!results.containsKey(id));
            } catch (InterruptedException iex) {
                LOG.log(Level.INFO, null, iex);
            }
            return results.remove(id);
        }
    }

    /**
     * Initializes the executor.
     */
    private void initialize() {
        Lookup lookup = browserImpl.getLookup();
        MessageDispatcher dispatcher = lookup.lookup(MessageDispatcher.class);
        if (dispatcher != null) {
            dispatcher.addMessageListener(new MessageListener() {
                @Override
                public void messageReceived(String featureId, String message) {
                    if (PageInspector.MESSAGE_DISPATCHER_FEATURE_ID.equals(featureId)) {
                        if (message == null) {
                            deactivate();
                        } else {
                            RemoteScriptExecutor.this.messageReceived(message);
                        }
                    }
                }
            });
        } else {
            LOG.log(Level.INFO, "No MessageDispatcher found in ExtBrowserImpl.getLookup()!"); // NOI18N
        }
        initialized = true;
    }

    /**
     * Deactivates this executor. All pending results are set to {@code ERROR_RESULT}.
     */
    private void deactivate() {
        synchronized (LOCK) {
            if (lastIDReceived < lastIDSent) {
                int fromID = lastIDReceived+1;
                LOG.log(Level.INFO, "Executor disposed before responses with IDs {0} to {1} were received!", // NOI18N
                        new Object[]{fromID, lastIDSent});
                for (int i=fromID; i<=lastIDSent; i++) {
                    results.put(i, ERROR_RESULT);
                }
                lastIDReceived = lastIDSent;
            }
            active = false;
            LOCK.notifyAll();
        }
    }

    /**
     * Activates the executor.
     */
    void activate() {
        synchronized (LOCK) {
            active = true;
        }
    }

    /**
     * Called when a message for this executor is received.
     * 
     * @param messageTxt message for this executor.
     */
    void messageReceived(String messageTxt) {
        try {
            JSONObject message = (JSONObject)JSONValue.parseWithException(messageTxt);
            Object type = message.get(MESSAGE_TYPE);
            if (MESSAGE_EVAL.equals(type)) {
                int id = ((Number)message.get(MESSAGE_ID)).intValue();
                synchronized (LOCK) {
                    for (int i=lastIDReceived+1; i<id; i++) {
                        LOG.log(Level.INFO, "Haven''t received result of execution of script with ID {0}.", i); // NOI18N
                        results.put(i, ERROR_RESULT);
                    }
                    Object status = message.get(MESSAGE_STATUS);
                    Object result = message.get(MESSAGE_RESULT);
                    if (MESSAGE_STATUS_OK.equals(status)) {
                        results.put(id, result);
                    } else {
                        LOG.log(Level.INFO, "Message with id {0} wasn''t executed successfuly: {1}", // NOI18N
                                new Object[]{id, result});
                        results.put(id, ERROR_RESULT);
                    }
                    lastIDReceived = id;
                    LOCK.notifyAll();
                }
            }
        } catch (ParseException ex) {
            LOG.log(Level.INFO, "Ignoring message that is not in JSON format: {0}", messageTxt); // NOI18N
        }        
    }

}
