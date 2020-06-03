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

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * Jump to action for call stack lines.
 *
 */
final class JumpToCallStackAction extends AbstractAction {

    /** */
    private final Node node;
    /** */
    private final String callstackFrameInfo;

    private int line;

    /** Creates a new instance of JumpAction */
    JumpToCallStackAction(Node node, String callstackFrameInfo) {
        this(node, callstackFrameInfo, -1);
    }

    /**
     * @param node
     * @param callstackFrameInfo a line in call stack representing the location 
     * to jump
     * @param line the line where to jump, if <code>line == -1</code>, then 
     * the line will be computed from the given <code>callstackFrameInfo</code>.
     */
    JumpToCallStackAction(Node node, String callstackFrameInfo, int line) {
        this.node = node;
        this.callstackFrameInfo = callstackFrameInfo;
        this.line = line;
    }


    @Override
    public Object getValue(String key) {
        if (NAME.equals(key)) {
            return NbBundle.getMessage(JumpToCallStackAction.class, "LBL_GoToSource");
        }
        return super.getValue(key);
    }

    /**
     * If the <code>callstackFrameInfo</code> is not <code>null</code>,
     * tries to jump to the callstack frame source code. Otherwise does nothing.
     */
    public void actionPerformed(ActionEvent e) {
        if (callstackFrameInfo == null) {
            return;
        }
        
        OutputUtils.openCallstackFrame(node, callstackFrameInfo, line);
    }

}
