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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.remote.ui.NewRemoteProjectAction", category = "NativeRemote")
@ActionRegistration(displayName = "#NewProjectConnectMenuItem", lazy = false)
@ActionReference(path = "Remote/Host/Actions", name = "NewRemoteProjectAction", position = 260)
public class NewRemoteProjectAction extends SingleHostAction {
    
    @Override
    public String getName() {
        return NbBundle.getMessage(HostListRootNode.class, "NewProjectConnectMenuItem");
    }

    @Override
    protected boolean enable(ExecutionEnvironment env) {
        return true;
    }

    @Override
    public boolean isVisible(Node node) {
        ExecutionEnvironment env = node.getLookup().lookup(ExecutionEnvironment.class);        
        return env != null && env.isRemote();
    }

    @Override
    protected void performAction(final ExecutionEnvironment env, Node node) {
        ActionListener performer = Lookups.forPath("CND/Toobar/Services/NewRemoteProject").lookup(ActionListener.class); //NOI18N
        if (performer != null) {
            performer.actionPerformed(new ActionEvent(node, 0, null));
        } else {
            RemoteUtil.LOGGER.info("Can not find NewRemoteProject action"); //NOI18N
        }
    }
}
