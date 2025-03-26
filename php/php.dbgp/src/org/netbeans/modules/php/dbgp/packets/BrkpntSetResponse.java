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
package org.netbeans.modules.php.dbgp.packets;

import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.breakpoints.AbstractBreakpoint;
import org.netbeans.modules.php.dbgp.packets.BrkpntSetCommand.State;
import org.w3c.dom.Node;

/**
 * @author ads
 *
 */
public class BrkpntSetResponse extends DbgpResponse {
    private static final String STATE = "state"; // NOI18N
    private static final String ID = "id"; // NOI18N
    private static final String ERROR = "error"; // NOI18N
    private static final String MESSAGE = "message"; // NOI18N

    BrkpntSetResponse(Node node) {
        super(node);
    }

    public String getBreakpointId() {
        return getAttribute(getNode(), ID);
    }

    public State getState() {
        String state = getAttribute(getNode(), STATE);
        return State.forString(state);
    }

    @Override
    public void process(DebugSession session, DbgpCommand command) {
        if (!(command instanceof BrkpntSetCommand)) {
            return;
        }
        BrkpntSetCommand setCommand = (BrkpntSetCommand) command;
        AbstractBreakpoint breakpoint = setCommand.getBreakpoint();
        if (breakpoint == null) {
            // This is normal situation . It happens when breakpoint is fake and
            // set f.e. for as temporary ( for run to cursor command ).
            return;
        }

        Node error = getChild(getNode(), ERROR);
        if (error != null) {
            Node message = getChild(error, MESSAGE);
            if (message != null) {
                breakpoint.setInvalid(message.getTextContent());
                return;
            }
        }

        breakpoint.setBreakpointId(getBreakpointId());
        if (getState() == State.DISABLED) {
            breakpoint.disable();
        } else {
            breakpoint.enable();
        }
    }

}
