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
package org.netbeans.modules.cnd.remote.actions.base;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.MissingResourceException;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ui.ServerListUI;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.actions.OpenRemoteProjectAction;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public abstract class RemoteActionPerformer implements ActionListener, DynamicMenuContent {
    protected RemoteOpenActionBase presenter;
    private JMenuItem lastPresenter;
    
    protected abstract void actionPerformedRemote(ExecutionEnvironment env, ActionEvent e);
    
    protected final Action findAction(String path, String id) {
        Lookup lookup = Lookups.forPath(path);  
        Lookup.Template<Action> template = new Lookup.Template<>(Action.class, id, null);
        Lookup.Item<Action> item = lookup.lookupItem(template);
        if (item != null) {
            return item.getInstance();
        }
        return null;
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        if (e != null && RemoteOpenActionBase.ACTIVATED_PSEUDO_ACTION_COMAND.equals(e.getActionCommand())) {
            presenter = (RemoteOpenActionBase) e.getSource();
            return;
        }
        if (e!= null && (e.getSource() instanceof JMenuItem)) {
            JMenuItem item = (JMenuItem) e.getSource();
            Object property = item.getClientProperty(RemoteOpenActionBase.ENV_KEY);
            if (property instanceof ExecutionEnvironment) {
                actionPerformedRemote((ExecutionEnvironment)property, e);
            } else {
                ServerListUI.showServerListDialog();
            }
            return;
        } else if (e!= null && (e.getSource() instanceof Lookup.Provider)) {
            ExecutionEnvironment env = ((Lookup.Provider) e.getSource()).getLookup().lookup(ExecutionEnvironment.class);
            if (env != null) {
                actionPerformedRemote(env, e);
                return;
            }
        }
        actionPerformedRemote(ServerList.getDefaultRecord().getExecutionEnvironment(), e);
    }

    @Override
    public JComponent[] getMenuPresenters() {
        lastPresenter = createSubMenu();
        return new JComponent[] { lastPresenter };
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] jcs) {
        lastPresenter = createSubMenu();
        return new JComponent[] { lastPresenter };
    }
    
    private JMenuItem createSubMenu() throws MissingResourceException {
        String label = presenter.getSubmenuTitle();
        JMenu subMenu = new JMenu(label);
        subMenu.setIcon(presenter.getIcon());
        for (ServerRecord record : ServerList.getRecords()) {
            if (record.isRemote()) {
                String text = presenter.getItemTitle(record.getDisplayName());
                JMenuItem item = new JMenuItem(text);
                item.putClientProperty(RemoteOpenActionBase.ENV_KEY, record.getExecutionEnvironment());
                item.addActionListener(this);
                subMenu.add(item);
            }
        }
        if (subMenu.getItemCount() > 0) {
            subMenu.add(new JSeparator());
        }
        JMenuItem item = new JMenuItem(NbBundle.getMessage(OpenRemoteProjectAction.class, "LBL_ManagePlatforms_Name")); // NOI18N
        item.putClientProperty(RemoteOpenActionBase.ENV_KEY, null);
        item.addActionListener(this);
        subMenu.add(item);
        return subMenu;
    }
    
}
