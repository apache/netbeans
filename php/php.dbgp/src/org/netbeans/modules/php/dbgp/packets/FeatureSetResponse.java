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
import org.netbeans.modules.php.dbgp.breakpoints.BreakpointModel;
import org.w3c.dom.Node;

/**
 * @author ads
 *
 */
public class FeatureSetResponse extends DbgpResponse {
    private static final String SUCCESS = "success"; // NOI18N
    private static final String FEATURE_NAME = "feature_name"; // NOI18N
    private static final String ERROR = "error"; // NOI18N

    FeatureSetResponse(Node node) {
        super(node);
    }

    public String getFeature() {
        return getAttribute(getNode(), FEATURE_NAME);
    }

    public boolean isSuccess() {
        return getBoolean(getNode(), SUCCESS);
    }

    @Override
    public void process(DebugSession session, DbgpCommand command) {
        if (command instanceof FeatureSetCommand) {
            String feature = ((FeatureSetCommand) command).getFeature();
            if (feature.equals(FeatureGetCommand.Feature.BREAKPOINT_DETAILS.toString())) {
                Node error = getChild(getNode(), ERROR);
                setSearchCurrentBreakpointById(session, error == null);
            }
        }
    }

    private void setSearchCurrentBreakpointById(DebugSession session, boolean value) {
        DebugSession.IDESessionBridge bridge = session.getBridge();
        if (bridge != null) {
            BreakpointModel breakpointModel = bridge.getBreakpointModel();
            if (breakpointModel != null) {
                breakpointModel.setSearchCurrentBreakpointById(value);
            }
        }
    }

}
