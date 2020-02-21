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

package org.netbeans.modules.cnd.testrunner.ui;

import javax.swing.Action;
import org.netbeans.modules.gsf.testrunner.ui.api.CallstackFrameNode;
import org.netbeans.modules.gsf.testrunner.ui.api.DiffViewAction;
import org.netbeans.modules.gsf.testrunner.api.Trouble.ComparisonFailure;
import org.openide.util.actions.SystemAction;

/**
 *
 */
public final class CndCallstackFrameNode extends CallstackFrameNode {
    private final String displayName;

    public CndCallstackFrameNode(String frameInfo, String displayName) {
        super(frameInfo, displayName);
        // Keep our own copy since the parent will assign frameInfo to displayName
        // if none is provided
        this.displayName = displayName;
    }

    /**
     */
    @Override
    public Action getPreferredAction() {
        // If it's a diff failure line, the default action is to diff it!
        if (displayName != null) {
            ComparisonFailure failure = CndUnitHandlerFactory.getComparisonFailure(displayName);
            if (failure != null) {
                return new DiffViewAction(failure);
            }
        }
        
        return new JumpToCallStackAction(this, frameInfo);
    }
    
    public SystemAction[] getActions(boolean context) {
        return new SystemAction[0];
    }
}
