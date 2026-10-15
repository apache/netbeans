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

package org.netbeans.modules.testng.ui;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;

/**
 *
 * @author Marian Petras
 */
final class CallstackFrameNode extends org.netbeans.modules.gsf.testrunner.ui.api.CallstackFrameNode {

    /**
     * Creates a node for a call stack frame.
     * @param  frameInfo  string specifying the call stack frame
     */
    CallstackFrameNode(String frameInfo, String displayName) {
        super(frameInfo, displayName);
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        Action preferred = getPreferredAction();
        if (preferred != null){
            actions.add(preferred);
        }
        return actions.toArray(new Action[0]);
    }

    @Override
    public Action getPreferredAction() {
        return new JumpAction(this, frameInfo);
    }
}
