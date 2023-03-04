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

import org.netbeans.modules.php.dbgp.packets.BrkpntSetCommand.State;

/**
 * @author ads
 *
 */
public class BrkpntUpdateCommand extends DbgpCommand {
    public static final String UPDATE = "breakpoint_update"; // NOI18N
    static final String ID_ARG = "-d "; // NOI18N
    private static final String STATE_ARG = "-s "; // NOI18N
    private String myId;
    private State state;

    public BrkpntUpdateCommand(String transactionId, String brkpntId) {
        super(UPDATE, transactionId);
        myId = brkpntId;
    }

    public String getBreakpointId() {
        return myId;
    }

    public void setState(State state) {
        this.state = state;
    }

    @Override
    protected String getArguments() {
        StringBuilder builder = new StringBuilder();
        builder.append(ID_ARG);
        builder.append(getBreakpointId());
        builder.append(SPACE);
        builder.append(STATE_ARG);
        builder.append(state.toString());
        return builder.toString();
    }

    @Override
    public boolean wantAcknowledgment() {
        return true;
    }

}
