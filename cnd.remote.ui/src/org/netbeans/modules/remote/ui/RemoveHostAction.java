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

import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.remote.ui.RemoveHostAction", category = "NativeRemote")
@ActionRegistration(displayName = "#RemoveHostMenuItem", lazy = false)
@ActionReference(path = "Remote/Host/Actions", name = "RemoveHostAction", position = 400)
public class RemoveHostAction extends SingleHostAction {

    @Override
    public String getName() {
        return NbBundle.getMessage(HostListRootNode.class, "RemoveHostMenuItem");
    }

    @Override
    public boolean isVisible(Node node) {
        return isRemote(node);
    }

    @Override
    protected void performAction(final ExecutionEnvironment env, Node node) {
        ServerRecord record = ServerList.get(env);
        String title = NbBundle.getMessage(HostNode.class, "RemoveHostCaption");
        String message = NbBundle.getMessage(HostNode.class, "RemoveHostQuestion", record.getDisplayName());

        if (JOptionPane.showConfirmDialog(WindowManager.getDefault().getMainWindow(),
                message, title, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            ToolsCacheManager cacheManager = ToolsCacheManager.createInstance(true);
            List<ServerRecord> hosts = new ArrayList<ServerRecord>(ServerList.getRecords());
            hosts.remove(record);
            ConnectionManager.getInstance().forget(env);
            cacheManager.setHosts(hosts);
            ServerRecord defaultRecord = ServerList.getDefaultRecord();
            if (defaultRecord.getExecutionEnvironment().equals(env)) {
                defaultRecord = ServerList.get(ExecutionEnvironmentFactory.getLocal());
            }
            cacheManager.setDefaultRecord(defaultRecord);
            cacheManager.applyChanges();
            ConnectionManager.getInstance().disconnect(env);
        }
    }
}
