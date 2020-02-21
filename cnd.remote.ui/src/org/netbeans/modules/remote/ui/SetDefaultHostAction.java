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

import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.remote.ui.SetDefaultHostAction", category = "NativeRemote")
@ActionRegistration(displayName = "#SetDefaultMenuItem", lazy = false)
@ActionReference(path = "Remote/Host/Actions", name = "SetDefaultHostAction", position = 300)
public class SetDefaultHostAction extends SingleHostAction {

    @Override
    public String getName() {
        return NbBundle.getMessage(HostListRootNode.class, "SetDefaultMenuItem");
    }

    @Override
    protected boolean enable(ExecutionEnvironment env) {
        return ! ServerList.getDefaultRecord().getExecutionEnvironment().equals(env);
    }

    @Override
    protected boolean asynchronous() {
        return true;
    }

    @Override
    protected void performAction(ExecutionEnvironment env, Node node) {
        ServerList.setDefaultRecord(ServerList.get(env));
    }
}
