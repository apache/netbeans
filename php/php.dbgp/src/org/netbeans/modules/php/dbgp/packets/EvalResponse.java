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
package org.netbeans.modules.php.dbgp.packets;

import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.DebugSession.IDESessionBridge;
import org.netbeans.modules.php.dbgp.SessionManager;
import org.netbeans.modules.php.dbgp.models.WatchesModel;
import org.w3c.dom.Node;

/**
 * @author ads
 *
 */
public class EvalResponse extends DbgpResponse {
    EvalResponse(Node node) {
        super(node);
    }

    public boolean isSuccess() {
        return getBoolean(getNode(), PropertySetResponse.SUCCESS);
    }

    public Property getProperty() {
        Node node = getChild(getNode(), Property.PROPERTY);
        if (node == null) {
            return null;
        } else {
            return new Property(node);
        }
    }

    @Override
    public void process(DebugSession session, DbgpCommand command) {
        if (!(command instanceof EvalCommand)) {
            return;
        }
        EvalCommand eval = (EvalCommand) command;
        String expression = eval.getData();
        DebugSession currentSession = SessionManager.getInstance().getSession(session.getSessionId());
        if (currentSession == session) {
            // perform view update only if response appears in current session
            IDESessionBridge bridge = session.getBridge();
            if (bridge != null) {
                WatchesModel watchesModel = bridge.getWatchesModel();
                if (watchesModel != null) {
                    watchesModel.updateExpressionValue(expression, getProperty());
                }
            }
            eval.firePropertyChangeEvent(expression, getProperty());
        }
    }

}
