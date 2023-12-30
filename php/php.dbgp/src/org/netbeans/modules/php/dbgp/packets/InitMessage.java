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

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.DebuggerOptions;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.breakpoints.AbstractBreakpoint;
import org.netbeans.modules.php.dbgp.breakpoints.Utils;
import org.netbeans.modules.php.dbgp.packets.FeatureGetCommand.Feature;
import org.w3c.dom.Node;

/**
 * @author ads
 *
 */
public class InitMessage extends DbgpMessage {

    private static final String IDEKEY = "idekey"; // NOI18N
    private static final String FILE = "fileuri"; // NOI18N

    InitMessage(Node node) {
        super(node);
    }

    public String getSessionId() {
        // accessor to idekey attribute
        return getAttribute(getNode(), IDEKEY);
    }

    public String getFileUri() {
        return getAttribute(getNode(), FILE);
    }

    @Override
    public void process(DebugSession session, DbgpCommand command) {
        setId(session);
        setShowHidden(session);
        setBreakpointResolution(session);
        setMaxDepth(session);
        setMaxChildren(session);
        setMaxDataSize(session);
        setBreakpointDetails(session);
        setBreakpoints(session);
        negotiateOutputStream(session);
        negotiateRequestedUrls(session);
        final String transactionId = session.getTransactionId();
        DbgpCommand startCommand = DebuggerOptions.getGlobalInstance().isDebuggerStoppedAtTheFirstLine()
                ? new StepIntoCommand(transactionId)
                : new RunCommand(transactionId);
        session.sendCommandLater(startCommand);
    }

    private void setMaxDataSize(DebugSession session) {
        int optionsMaxData = DebuggerOptions.getGlobalInstance().getMaxData();
        FeatureSetCommand setCommand = new FeatureSetCommand(session.getTransactionId());
        setCommand.setFeature(Feature.MAX_DATA);
        setCommand.setValue(optionsMaxData + "");
        DbgpResponse response = session.sendSynchronCommand(setCommand);
        assert response instanceof FeatureSetResponse;
        DbgpMessage.setMaxDataSize(optionsMaxData);
    }

    private void setShowHidden(DebugSession session) {
        setFeature(session, Feature.SHOW_HIDDEN, "1"); //NOI18N
    }

    private void setBreakpointDetails(DebugSession session) {
        setFeature(session, Feature.BREAKPOINT_DETAILS, "1"); //NOI18N
    }

    private void setBreakpointResolution(DebugSession session) {
        if (DebuggerOptions.getGlobalInstance().resolveBreakpoints()) {
            setFeature(session, Feature.RESOLVED_BREAKPOINTS, "1"); // NOI18N
        }
    }

    private void setMaxDepth(DebugSession session) {
        setFeature(session, Feature.MAX_DEPTH, String.valueOf(DebuggerOptions.getGlobalInstance().getMaxStructuresDepth()));
    }

    private void setMaxChildren(DebugSession session) {
        setFeature(session, Feature.MAX_CHILDREN, String.valueOf(DebuggerOptions.getGlobalInstance().getMaxChildren()));
    }

    private void setFeature(DebugSession session, Feature feature, String value) {
        FeatureSetCommand setCommand = new FeatureSetCommand(session.getTransactionId());
        setCommand.setFeature(feature);
        setCommand.setValue(value);
        DbgpResponse response = session.sendSynchronCommand(setCommand);
        assert response instanceof FeatureSetResponse : response;
    }

    private void negotiateOutputStream(DebugSession session) {
        if (DebuggerOptions.getGlobalInstance().showDebuggerConsole()) {
            StreamCommand streamCommand = new StreamCommand(DbgpStream.StreamType.STDOUT, session.getTransactionId());
            streamCommand.setOperation(StreamCommand.Operation.COPY);
            session.sendCommandLater(streamCommand);
        }
    }

    private void negotiateRequestedUrls(DebugSession session) {
        if (DebuggerOptions.getGlobalInstance().showRequestedUrls()) {
            session.sendCommandLater(new RequestedUrlEvalCommand(session.getTransactionId()));
        }
    }

    private void setBreakpoints(DebugSession session) {
        SessionId id = session.getSessionId();
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (Breakpoint breakpoint : breakpoints) {
            if (!(breakpoint instanceof AbstractBreakpoint) ) {
                continue;
            }
            //do not set a breakpoint at debug start if it is not enabled
            if (!breakpoint.isEnabled()) {
                continue;
            }
            AbstractBreakpoint brkpnt = (AbstractBreakpoint) breakpoint;
            BrkpntSetCommand command = Utils.getCommand(session, id, brkpnt);
            if (command == null) {
                continue;
            }
            session.sendCommandLater(command);
        }

    }

    private void setId(DebugSession session) {
        session.initConnection(this);
    }

}
