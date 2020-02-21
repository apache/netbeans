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
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ui.ServerListUI;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public abstract class RemoteOpenActionBase extends AbstractAction implements DynamicMenuContent, Presenter.Toolbar, PropertyChangeListener {

    static final String ENV_KEY = "org.netbeans.modules.cnd.remote.actions.ENV"; // NOI18N
    public static final String ACTIVATED_PSEUDO_ACTION_COMAND = "performerActivated"; // NOI18N
    
    private JButton lastToolbarPresenter;
    private boolean isEnabledToolbarAction = true;
    private ActionListener peformer;
    
    public RemoteOpenActionBase(String name) {
        super(name);
        ServerList.addPropertyChangeListener(this);
        updateToolTip();
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ServerList.PROP_DEFAULT_RECORD.equals(evt.getPropertyName())) {
            defaultRemoteHostChanged();
        }
    }

    protected void defaultRemoteHostChanged() {
        SwingUtilities.invokeLater(() -> updateToolTip());
    }

    protected void updateToolTip() {
    }

    @Override
    public JComponent[] getMenuPresenters() {
        initPerformer();
        return ((DynamicMenuContent)getPerformer()).getMenuPresenters();
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] jcs) {
        initPerformer();
        return ((DynamicMenuContent)getPerformer()).synchMenuPresenters(jcs);
    }
    
    @Override
    public JButton getToolbarPresenter() {
        lastToolbarPresenter = new JButton() {

            @Override
            public void setEnabled(boolean b) {
                super.setEnabled(isEnabledToolbarAction);
            }

            @Override
            public boolean isEnabled() {
                return isEnabledToolbarAction;
            }
        };
        lastToolbarPresenter.addHierarchyListener(new HierarchyListener() {

            @Override
            public void hierarchyChanged(HierarchyEvent e) {
                if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                    if (e.getChanged().isShowing()){
                        initPerformer();
                    }
                }
            }
        });
        Actions.connect(lastToolbarPresenter, this);
        return lastToolbarPresenter;
    }

    private ActionListener getPerformer() {
        if (peformer == null) {
            initPerformer();
        }
        return peformer;
    }

    private void initPerformer() {
        assert SwingUtilities.isEventDispatchThread();
        if (peformer == null) {
            peformer = Lookups.forPath(getPerformerID()).lookup(ActionListener.class);
            peformer.actionPerformed(new ActionEvent(RemoteOpenActionBase.this, 0, ACTIVATED_PSEUDO_ACTION_COMAND));
        }
    }
    
    @Override
    public final void setEnabled(boolean enabled) {
        isEnabledToolbarAction = enabled;
        if (lastToolbarPresenter != null) {
            lastToolbarPresenter.setEnabled(enabled);
        }
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JMenuItem) {
            JMenuItem item = (JMenuItem) e.getSource();
            Object env = item.getClientProperty(ENV_KEY);
            if (env == null) {
                ServerListUI.showServerListDialog();
            } else {
                getPerformer().actionPerformed(e);
            }
        } else {
            getPerformer().actionPerformed(e);
        }
    }

    protected abstract Icon getIcon();
    protected abstract String getPerformerID();
    protected abstract String getSubmenuTitle();
    protected abstract String getItemTitle(String record);
}
