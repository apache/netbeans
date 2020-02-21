/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
