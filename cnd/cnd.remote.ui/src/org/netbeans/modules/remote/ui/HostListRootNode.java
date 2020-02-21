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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.core.ide.ServicesTabNodeRegistration;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import org.netbeans.modules.cnd.remote.ui.setup.CreateHostWizardIterator;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.RemoteStatistics;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.NodeAction;

/**
 * Root node for remote hosts list in Services tab
 */
public final class HostListRootNode extends AbstractNode {

    public static final String NODE_NAME = "remote"; // NOI18N
    private static final String ICON_BASE = "org/netbeans/modules/remote/ui/servers.png"; // NOI18N

    private final Action[] actions;

    @ServicesTabNodeRegistration(name=NODE_NAME, displayName="#LBL_HostRootNode", iconResource=ICON_BASE, position=800)
    public static HostListRootNode getDefault() {
        return new HostListRootNode();
    }

    private HostListRootNode() {
        super(Children.create(new HostChildren(), true));
        setName(NODE_NAME);
        setDisplayName(NbBundle.getMessage(HostListRootNode.class, "LBL_HostRootNode"));
        setIconBaseWithExtension(ICON_BASE);
        if (RemoteStatistics.COLLECT_STATISTICS) {
            actions = new Action[] { new AddHostAction(), new TraficStatisticsAction()};
        } else {
            actions = new Action[]{ new AddHostAction() };
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        return actions;
    }

    private static class HostChildren extends ChildFactory<ExecutionEnvironment> implements PropertyChangeListener, Runnable {

        private final RequestProcessor.Task refreshTask;

        public HostChildren() {
            ServerList.addPropertyChangeListener(WeakListeners.propertyChange(this, null));
            refreshTask = new RequestProcessor("Refreshing Host List", 1).create(this); // NOI18N
        }

        @Override
        protected boolean createKeys(List<ExecutionEnvironment> toPopulate) {
            for (ExecutionEnvironment env : ServerList.getEnvironments()) {
                //if (env.isRemote()) {
                    toPopulate.add(env);
                //}
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(ExecutionEnvironment key) {
            final HostNode node = new HostNode(key);
            return node;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            refreshTask.schedule(0);
        }

        @Override
        public void run() {
            this.refresh(true);
        }
    }

    //@ActionID(id = "org.netbeans.modules.remote.ui.TrafficStatisticsAction", category = "NativeRemote")
    //@ActionRegistration(displayName = "TrafficStatisticsMenuItem")
    //@ActionReference(path = "Remote/Host/Actions", name = "TrafficStatisticsAction", position = 99998)
    private static class TraficStatisticsAction extends NodeAction {

        public TraficStatisticsAction() {
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(HostListRootNode.class, "TrafficStatisticsMenuItem");
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }        

        @Override
        protected boolean enable(Node[] activatedNodes) {
            return true;
        }
        
        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        protected void performAction(Node[] activatedNodes) {
            final NotifyDescriptor.InputLine notifyDescriptor = new NotifyDescriptor.InputLine("Log file:", "Turn Remote Statistics On"); // NOI18N
            DialogDisplayer.getDefault().notify(notifyDescriptor);
            if (notifyDescriptor.getValue() == NotifyDescriptor.OK_OPTION) {    
                final String inputText = notifyDescriptor.getInputText();
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        RemoteStatistics.startTest(inputText, null, 0, 10000);
                    }
                });
            }
        }
    }

    @ActionID(id = "AddNewHostAction", category = "NativeRemote")
    @ActionRegistration(displayName = "#AddNewHostAction")
    public static class AddHostAction extends AbstractAction implements Runnable {
        private static final RequestProcessor RP = new RequestProcessor("AddHostAction", 1); // NOI18N

        public AddHostAction() {
            super(NbBundle.getMessage(HostListRootNode.class, "AddHostMenuItem"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setEnabled(false);
            RP.post(this);
        }

        @Override
        public void run() {
            if (SwingUtilities.isEventDispatchThread()) {
                setEnabled(true);
            } else {
                try {
                    work();
                } finally {
                    SwingUtilities.invokeLater(this);
                }
            }
        }

        private void work() {
            ToolsCacheManager cacheManager = ToolsCacheManager.createInstance(true);
            ServerRecord newServerRecord = CreateHostWizardIterator.invokeMe(cacheManager);
            if (newServerRecord != null) {
                List<ServerRecord> hosts = new ArrayList<ServerRecord>(ServerList.getRecords());
                if (!hosts.contains(newServerRecord)) {
                    hosts.add(newServerRecord);
                    cacheManager.setHosts(hosts);
                    cacheManager.setDefaultRecord(ServerList.getDefaultRecord());
                    cacheManager.applyChanges();
                }
            }
        }
    }
}
