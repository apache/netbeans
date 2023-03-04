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
import org.netbeans.modules.php.dbgp.SessionManager;
import org.w3c.dom.Node;

/**
 * @author ads
 *
 */
public class PropertySetResponse extends DbgpResponse {
    static final String SUCCESS = "success"; // NOI18N

    PropertySetResponse(Node node) {
        super(node);
    }

    @Override
    public void process(DebugSession session, DbgpCommand command) {
        if (!(command instanceof PropertySetCommand)) {
            return;
        }
        PropertySetCommand setCommand = (PropertySetCommand) command;
        String fullName = setCommand.getName();

        /*
         * Retrieve value of property for update it in view.
         * As alternative : request context names and as consequence update all
         * local view.
         */
        PropertyGetCommand getCommand = new PropertyGetCommand(session.getTransactionId());
        getCommand.setName(fullName);
        getCommand.setContext(setCommand.getContext());
        session.sendSynchronCommand(getCommand);
        if (!isSusccess()) {
            /*
             *  TODO :
             *  Report to user about error in setting value
             */
        } else {
            DebugSession currentSession = SessionManager.getInstance().getSession(session.getSessionId());
            if (currentSession == session) {
                StackGetResponse.updateWatchView(session);
            }
        }
    }

    public boolean isSusccess() {
        return getBoolean(getNode(), SUCCESS);
    }

}
