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
