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

package org.netbeans.modules.cnd.actions;

import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;

/**
 * Implements Make Cleanaction
 */
public class MakeCleanAction extends MakeBaseAction {

    @Override
    public String getName () {
        return getString("BTN_Clean"); // NOI18N
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            performAction(activatedNodes[i], "clean"); // NOI18N
        }
    }

    /**
     *  Execute a single MakefileDataObject.
     *
     *  @param node A single MakefileDataNode(should have a {@link MakeExecSupport}
     */
    public static void execute(Node node) {
        (SystemAction.get(MakeCleanAction.class)).performAction(new Node[] {node});
    }

    @Override
    protected String iconResource() {
        return "org/netbeans/modules/cnd/resources/MakeCleanAction.gif"; // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx(MakeCleanAction.class); // FIXUP ???
    }
}
