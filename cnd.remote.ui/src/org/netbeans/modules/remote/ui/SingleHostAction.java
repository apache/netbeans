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
package org.netbeans.modules.remote.ui;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 */
public abstract class SingleHostAction extends NodeAction {

    public SingleHostAction() {
    }

    /** @param env not null */
    protected boolean enable(ExecutionEnvironment env) {
        return true;
    }

    protected abstract void performAction(ExecutionEnvironment env, Node node);

//    protected final ExecutionEnvironment getEnv(Node[] activatedNodes) {
//        if (activatedNodes.length == 1) {
//            return activatedNodes[0].getLookup().lookup(ExecutionEnvironment.class);
//        }
//        return null;
//    }

    public boolean isVisible(Node node) {
        return true;
    }

    protected boolean isRemote(Node node) {
        ExecutionEnvironment env = node.getLookup().lookup(ExecutionEnvironment.class);
        return env != null && env.isRemote();
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            ExecutionEnvironment env = activatedNodes[0].getLookup().lookup(ExecutionEnvironment.class);
            if (env != null) {
                return enable(env);
            }
        }
        return false;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if (activatedNodes.length == 1) {
            Node node = activatedNodes[0];
            ExecutionEnvironment env = node.getLookup().lookup(ExecutionEnvironment.class);
            if (env != null) {
                performAction(env, node);
            }
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
