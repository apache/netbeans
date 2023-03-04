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

package org.netbeans.modules.profiler.v2;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.SwingUtilities;
import org.netbeans.modules.profiler.LoadedSnapshot;
import org.netbeans.modules.profiler.ResultsManager;
import org.netbeans.modules.profiler.SnapshotsListener;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.netbeans.modules.profiler.v2.impl.SnapshotsWindowHelper;
import org.netbeans.modules.profiler.v2.impl.SnapshotsWindowUI;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Jiri Sedlacek
 */
 public final class SnapshotsWindow {
    
    // --- Singleton -----------------------------------------------------------
    
    private static SnapshotsWindow INSTANCE;
    
    private final SnapshotsListener snapshotsListener;
    
    public static synchronized SnapshotsWindow instance() {
        if (INSTANCE == null) INSTANCE = new SnapshotsWindow();
        return INSTANCE;
    }
    
    private SnapshotsWindow() {
        snapshotsListener = Lookup.getDefault().lookup(SnapshotsWindowHelper.class);
        
        TopComponent.getRegistry().addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (TopComponent.Registry.PROP_TC_CLOSED.equals(evt.getPropertyName()))
                    if (ui != null && evt.getNewValue() == ui) ui = null;
            }
        });
    }
    
    // --- API -----------------------------------------------------------------
    
    public void showStandalone() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SnapshotsWindowUI ui = getUI(true);
                ui.open();
                ui.requestActive();
            }
        });
    }
    
    public void sessionOpened(final ProfilerSession session) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SnapshotsWindowUI ui = getUI(false);
                if (ui == null && ProfilerIDESettings.getInstance().getSnapshotWindowOpenPolicy() == ProfilerIDESettings.SNAPSHOT_WINDOW_OPEN_PROFILER) {
                    ui = getUI(true);
                    ui.setProject(session.getProject());
                    ui.open();
                } else if (ui != null) ui.setProject(session.getProject());
            }
        });
    }
    
    public void sessionActivated(final ProfilerSession session) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SnapshotsWindowUI ui = getUI(false);
                if (ui == null && ProfilerIDESettings.getInstance().getSnapshotWindowOpenPolicy() == ProfilerIDESettings.SNAPSHOT_WINDOW_SHOW_PROFILER) {
                    ui = getUI(true);
                    ui.setProject(session.getProject());
                    ui.open();
                } else if (ui != null) ui.setProject(session.getProject());
            }
        });
    }
    
    public void sessionDeactivated(final ProfilerSession session) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SnapshotsWindowUI ui = getUI(false);
                if (ui != null) {
                    if (ProfilerIDESettings.getInstance().getSnapshotWindowClosePolicy() == ProfilerIDESettings.SNAPSHOT_WINDOW_HIDE_PROFILER)
                        ui.close();
                    ui.resetProject(session.getProject());
                }
            }
        });
    }
    
    public void sessionClosed(final ProfilerSession session) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SnapshotsWindowUI ui = getUI(false);
                if (ui != null) {
                    if (ProfilerIDESettings.getInstance().getSnapshotWindowClosePolicy() == ProfilerIDESettings.SNAPSHOT_WINDOW_CLOSE_PROFILER)
                        ui.close();
                    ui.resetProject(session.getProject());
                }
            }
        });
    }
    
    public void snapshotSaved(final LoadedSnapshot snapshot) {
        assert !SwingUtilities.isEventDispatchThread();
        
        int policy = ProfilerIDESettings.getInstance().getSnapshotWindowOpenPolicy();
        if ((policy == ProfilerIDESettings.SNAPSHOT_WINDOW_OPEN_FIRST &&
             ResultsManager.getDefault().getSnapshotsCountFor(snapshot.getProject()) == 1) ||
             policy == ProfilerIDESettings.SNAPSHOT_WINDOW_OPEN_EACH) {
            final Lookup.Provider project = snapshot.getProject();
            ProfilerSession session = ProfilerSession.currentSession();
            if (session != null && Objects.equals(session.getProject(), project))
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        SnapshotsWindowUI ui = getUI(false);
                        if (ui == null) {
                            ui = getUI(true);
                            ui.setProject(project);
                            ui.open();
                        }
                    }
                });
        }
    }
    
    public void refreshFolder(final FileObject folder, final boolean fullRefresh) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SnapshotsWindowUI ui = getUI(false);
                if (ui != null) ui.refreshFolder(folder, fullRefresh);
            }
        });
    }
    
    
    // --- UI ------------------------------------------------------------------
    
    private SnapshotsWindowUI ui;
    
    private SnapshotsWindowUI getUI(boolean create) {
        if (ui == null) {
            WindowManager wm = WindowManager.getDefault();
            
            for (TopComponent tc : TopComponent.getRegistry().getOpened())
                if (tc.getClientProperty(SnapshotsWindowUI.ID) != null)
                    ui = (SnapshotsWindowUI)tc;
            
            if (ui == null && create)
                ui = (SnapshotsWindowUI)wm.findTopComponent(SnapshotsWindowUI.ID);
            
            if (ui == null && create)
                ui = new SnapshotsWindowUI();
        }
        
        return ui;
    }
    
}
