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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author ads
 *
 */
public class ContextNamesResponse extends DbgpResponse {
    static final String CONTEXT = "context"; // NOI18N
    private static final String NAME = "name"; // NOI18N
    private static final String ID = "id"; // NOI18N

    ContextNamesResponse(Node node) {
        super(node);
    }

    Collection<Context> getContexts() {
        List<Context> result = new LinkedList<>();
        NodeList list = getNode().getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            Node node = list.item(i);
            if (CONTEXT.equals(node.getNodeName())) {
                String name = getAttribute(node, NAME);
                String idString = getAttribute(node, ID);
                try {
                    int id = Integer.parseInt(idString);
                    result.add(new Context(name, id));
                } catch (NumberFormatException e) {
                    assert false;
                    continue;
                }
            }
        }
        return result;
    }

    @Override
    public void process(DebugSession session, DbgpCommand command) {
        if (!(command instanceof ContextNamesCommand)) {
            return;
        }
        ContextNamesCommand namesCommand = (ContextNamesCommand) command;
        int depth = namesCommand.getDepth();
        for (Context context : getContexts()) {
            ContextGetCommand getCommand = new ContextGetCommand(session.getTransactionId());
            getCommand.setContext(context);
            if (depth != -1) {
                getCommand.setDepth(depth);
            }
            session.sendCommandLater(getCommand);
        }
    }

    /**
     * This is holder class for context name and its id that is returned by
     * ContextNamesResponse.
     *
     * @author ads
     *
     */
    public static final class Context {
        private String myContext;
        private int myId;

        private Context(String contextName, int id) {
            myContext = contextName;
            myId = id;
        }

        public String getContext() {
            return myContext;
        }

        public int getId() {
            return myId;
        }

    }

}
