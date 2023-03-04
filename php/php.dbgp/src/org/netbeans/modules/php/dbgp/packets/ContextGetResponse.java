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

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.SessionManager;
import org.netbeans.modules.php.dbgp.models.VariablesModel.ContextNode;
import org.netbeans.modules.php.dbgp.packets.ContextNamesResponse.Context;
import org.w3c.dom.Node;

/**
 * @author ads
 *
 */
public class ContextGetResponse extends DbgpResponse {

    public ContextGetResponse(Node node) {
        super(node);
    }

    public int getContextId() {
        String id = getAttribute(getNode(), ContextNamesResponse.CONTEXT);
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public List<Property> getProperties() {
        List<Node> nodes = getChildren(getNode(), Property.PROPERTY);
        List<Property> result = new ArrayList<>(nodes.size());
        for (Node node : nodes) {
            result.add(new Property(node));
        }
        return result;
    }

    @Override
    public void process(DebugSession session, DbgpCommand command) {
        if (!(command instanceof ContextGetCommand)) {
            return;
        }
        ContextGetCommand getCommand = (ContextGetCommand) command;
        Context ctx = getCommand.getContext();
        ContextNode node = new ContextNode(ctx, getProperties());
        DebugSession currentSession = SessionManager.getInstance().getSession(session.getSessionId());
        if (currentSession == session) {
            session.getBridge().getVariablesModel().updateContext(node);
        }
    }

}
