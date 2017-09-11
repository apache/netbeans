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
            listenersCopy = listeners.toArray(new MessageListener[listeners.size()]);
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
