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

package org.netbeans.modules.cnd.remote.projectui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.remote.actions.base.RemoteOpenActionBase;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.cnd.remote.projectui.actions.ConnectionStatusAction", category = "Project")
@ActionRegistration(iconInMenu = true, displayName = "#ConnectionStatusAction.submenu.title", lazy = false)
@ActionReference(path = "Toolbars/Remote", position = 450)
public class ConnectionStatusAction  extends AbstractAction implements Presenter.Toolbar {

    private JButton lastToolbarPresenter;
    private ActionListener performer;

    public ConnectionStatusAction() {
        super(NbBundle.getMessage(ConnectionStatusAction.class, "ConnectionStatusAction.submenu.title")); // NOI18N
        putValue("iconBase","org/netbeans/modules/cnd/remote/projectui/resources/disconnected.png"); //NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        initPerformer();
        performer.actionPerformed(e);
    }
    

    @Override
    public JButton getToolbarPresenter() {
        lastToolbarPresenter = new JButton();
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

    private void initPerformer() {
        assert SwingUtilities.isEventDispatchThread();
        if (performer == null) {
            performer = Lookups.forPath("CND/Toobar/Services/ConnectionStatus").lookup(ActionListener.class); // NOI18N
            performer.actionPerformed(new ActionEvent(ConnectionStatusAction.this, 0, 
                    RemoteOpenActionBase.ACTIVATED_PSEUDO_ACTION_COMAND));
        }
    }
}
