/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.debugger.jpda.projectsui;

import org.netbeans.modules.debugger.jpda.projectsui.MainProjectManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Martin Entlicher
 */
public class SourcesTabs extends JTabbedPane {

    private PropertyChangeListener  mainProjectListener;
    private DebuggerManagerListener debuggerListener;

    public SourcesTabs() {
        initComponent();
        mainProjectListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                Project p = MainProjectManager.getDefault().getMainProject();
                setProjectTitle(p);
            }
        };
        MainProjectManager.getDefault().addPropertyChangeListener(
                WeakListeners.propertyChange(mainProjectListener, MainProjectManager.getDefault()));

        debuggerListener = new DebuggerManagerAdapter() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if (DebuggerManager.PROP_CURRENT_SESSION.equals(propertyName)) {
                    Session s = DebuggerManager.getDebuggerManager().getCurrentSession();
                    setSessionTitle(s);
                }
            }
        };
        DebuggerManager.getDebuggerManager().addDebuggerListener(
                WeakListeners.create(DebuggerManagerListener.class, debuggerListener,
                                             DebuggerManager.getDebuggerManager()));
        setSessionTitle(DebuggerManager.getDebuggerManager().getCurrentSession());
    }

    private void initComponent() {
        // Will have two tabbs - one with the current sources, one for remote connection
        addTab(NbBundle.getMessage(SourcesTabs.class, "LBL_SourcesTabbs.Current"), new JLabel("Current"));
        addTab(NbBundle.getMessage(SourcesTabs.class, "LBL_SourcesTabbs.Remote"), new JLabel("Remote"));

    }

    private void setProjectTitle(Project p) {
        if (p == null) {
            setTitleAt(0, NbBundle.getMessage(SourcesTabs.class, "LBL_SourcesTabbs.Current"));
        } else {
            ProjectInformation pi = ProjectUtils.getInformation(p);
            setTitleAt(0, pi.getDisplayName());
        }
    }

    private void setSessionTitle(Session s) {
        if (s != null) {
            //getTabComponentAt(1).setVisible(false);
            setTitleAt(0, s.getName());
            setEnabledAt(1, false);
        } else {
            setProjectTitle(MainProjectManager.getDefault().getMainProject());
            setEnabledAt(1, true);
        }
    }

}
