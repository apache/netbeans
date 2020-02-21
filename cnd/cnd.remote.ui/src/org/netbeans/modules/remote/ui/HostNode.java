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

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public final class HostNode extends AbstractNode implements ConnectionListener, PropertyChangeListener {

    private final ExecutionEnvironment env;

    @SuppressWarnings("LeakingThisInConstructor") // it's ok to just add this as a listener
    public HostNode(ExecutionEnvironment execEnv) {
        super(createChildren(execEnv), Lookups.singleton(execEnv));
        this.env = execEnv;
        ConnectionManager.getInstance().addConnectionListener(WeakListeners.create(ConnectionListener.class, this, ConnectionManager.getInstance()));
        ServerList.addPropertyChangeListener(WeakListeners.propertyChange(this, null));
        addRecordListener();
    }
    
    private void addRecordListener() {
        ServerRecord record = ServerList.get(env);
        record.addPropertyChangeListener(WeakListeners.propertyChange(this, null));
    }

    private static Children createChildren(ExecutionEnvironment execEnv) {
        final Collection<? extends HostNodesProvider> providers = Lookup.getDefault().lookupAll(HostNodesProvider.class);
        if (providers.isEmpty()) {
            return Children.LEAF;
        } else {
            return Children.create(new HostSubnodeChildren(execEnv, providers), true);
        }
    }

    @Override
    public void connected(ExecutionEnvironment env) {
        fireIconChange();
    }

    @Override
    public void disconnected(ExecutionEnvironment env) {
        fireIconChange();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ServerList.PROP_DEFAULT_RECORD)
                || evt.getPropertyName().equals(ServerRecord.DISPLAY_NAME_CHANGED)) {
                refresh();
        }
        if (evt.getPropertyName().equals(ServerRecord.PROP_STATE_CHANGED)) {
                fireIconChange();
        }
//        if (evt.getPropertyName().equals(ServerList.PROP_RECORD_LIST)) {
//            List<RemoteServerRecord> oldItems = (List<RemoteServerRecord>) evt.getOldValue();            
//            List<RemoteServerRecord> newItems = (List<RemoteServerRecord>) evt.getNewValue();
//            if (contains(newItems, env) && !contains(oldItems, env)) {
//                addRecordListener();
//            }
//        }
    }
    
//    private static boolean contains(List<? extends ServerRecord> list, ExecutionEnvironment env) {
//        if (list != null) {
//            for (ServerRecord record : list) {
//                if (env.equals(record.getExecutionEnvironment())) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

    private void refresh() {
        fireDisplayNameChange("", getDisplayName()); // to make Node refresh
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public Image getIcon(int type) {
        if (isConnected()) {
            if (isOnline(env)) {
                return ImageUtilities.loadImage("org/netbeans/modules/remote/ui/connected_host.png"); //NOI18N
            } else {
                return ImageUtilities.loadImage("org/netbeans/modules/remote/ui/not_set_up_host.png"); //NOI18N
            }
        } else {
            return ImageUtilities.loadImage("org/netbeans/modules/remote/ui/disconnected_host.png"); //NOI18N
        }
    }
    
    static boolean isOnline(ExecutionEnvironment execEnv) {
        ServerRecord record = ServerList.get(execEnv);
        return record != null && record.isOnline();
    }

    private boolean isConnected() {
        return ConnectionManager.getInstance().isConnectedTo(env);
    }

    @Override
    public String getName() {
        return ExecutionEnvironmentFactory.toUniqueID(env);
    }

    @Override
    public String getDisplayName() {
        return RemoteUtil.getDisplayName(env);
    }

    @Override
    public String getHtmlDisplayName() {
        String displayName = RemoteUtil.getDisplayName(env);
        ServerRecord defRec = ServerList.getDefaultRecord();
        if (defRec != null && defRec.getExecutionEnvironment().equals(env)) {
            displayName = "<b>" + displayName + "</b>"; // NOI18N
        }
        return displayName;
    }


    @Override
    public Action[] getActions(boolean context) {
        List<Action> list = new ArrayList<>();
        for (Action action : Utilities.actionsForPath("Remote/Host/Actions")) { // NOI18N
            if (!(action instanceof SingleHostAction) || ((SingleHostAction) action).isVisible(this)) {
                list.add(action);
            }
        }
        return list.toArray(new Action[list.size()]);
    }

    @Override
    public Action getPreferredAction() {
        if (env.isLocal()) {
            return super.getPreferredAction();
        } else {
            return SharedClassObject.findObject(HostPropertiesAction.class, true);
        }
    }


    public ExecutionEnvironment getExecutionEnvironment() {
        return getLookup().lookup(ExecutionEnvironment.class);
    }

    private static class HostSubnodeChildren extends ChildFactory<HostNodesProvider> {

        private final Collection<HostNodesProvider> providers;
        private final ExecutionEnvironment execEnv;

        public HostSubnodeChildren(ExecutionEnvironment execEnv, Collection<? extends HostNodesProvider> providers) {
            this.execEnv = execEnv;
            this.providers = new ArrayList<>(providers.size());
            for (HostNodesProvider provider : providers) {
                if (provider.isApplicable(execEnv)) {
                    this.providers.add(provider);
                }
            }
        }

        @Override
        protected boolean createKeys(List<HostNodesProvider> toPopulate) {
            toPopulate.addAll(providers);
            return true;
        }

        @Override
        protected Node createNodeForKey(HostNodesProvider key) {
            return key.createNode(execEnv);
        }
    }
}
