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

import java.io.Writer;
import java.util.List;
import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.modules.cnd.builds.MakeExecSupport;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.windows.InputOutput;

/**
 * Implements Make action
 */
public class MakeAction extends MakeBaseAction {

    @Override
    public String getName () {
        return getString("BTN_Execute"); // NOI18N
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            performAction(activatedNodes[i], ""); // NOI18N
        }
    }

    /**
     *  Execute a single MakefileDataObject.
     *
     *  @param node A single MakefileDataNode(should have a {@link MakeExecSupport}
     */
    public static void execute(Node node) {
        (SystemAction.get(MakeAction.class)).performAction(node, ""); // NOI18N
    }

    /**
     *  Execute a single MakefileDataObject.
     *
     *  @param node A single MakefileDataNode(should have a {@link MakeExecSupport}
     */
    public static void execute(Node node, String target) {
        (SystemAction.get(MakeAction.class)).performAction(node, target);
    }

    /**
     *  Execute a single MakefileDataObject.
     *
     *  @param node A single MakefileDataNode(should have a {@link MakeExecSupport}
     */
    public static Future<Integer> execute(Node node, String target, ExecutionListener listener, Writer outputListener,
                               Project project, List<String> additionalEnvironment, InputOutput inputOutput) {
        return (SystemAction.get(MakeAction.class)).performAction(node, target, listener, outputListener, project, additionalEnvironment, inputOutput);
    }

    @Override
    protected String iconResource() {
        return "org/netbeans/modules/cnd/resources/MakeAction.gif"; // NOI18N
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public HelpCtx getHelpCtx () {
        return new HelpCtx(MakeAction.class); // FIXUP ???
    }
}
