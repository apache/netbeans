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

import java.io.IOException;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.dlight.api.terminal.TerminalSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.remote.ui.OpenTerminalAction", category = "NativeRemote")
@ActionRegistration(displayName = "#OpenTerminalMenuItem", lazy = false)
@ActionReference(path = "Remote/Host/Actions", name = "OpenTerminalAction", position = 700)
public class OpenTerminalAction extends SingleHostAction {
    private JMenu remotePopupMenu;
    private JMenuItem localPopupMenu;
    
    private static final RequestProcessor RP = new RequestProcessor("OpenTerminalAction", 1); // NOI18N

    @Override
    public String getName() {
        return NbBundle.getMessage(HostListRootNode.class, "OpenTerminalMenuItem"); // NOI18N
    }

    @Override
    protected void performAction(final ExecutionEnvironment env, Node node) {
        Node[] activatedNodes = getActivatedNodes();
        if (activatedNodes != null && activatedNodes.length == 1 && !isRemote(activatedNodes[0])) {
            SystemAction.get(AddHome.class).performAction(env, node);
        }        
    }

    @Override
    public boolean isVisible(Node node) {
        return true;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        createSubMenu();
        JMenuItem out = localPopupMenu;
        Node[] activatedNodes = getActivatedNodes();
        if (activatedNodes != null && activatedNodes.length == 1 && isRemote(activatedNodes[0])) {
            out = remotePopupMenu;
        }
        return out;
    }

    private void createSubMenu() {
        if (remotePopupMenu == null) {
            remotePopupMenu = new JMenu(getName());
            remotePopupMenu.add(SystemAction.get(AddHome.class).getPopupPresenter());
            remotePopupMenu.add(SystemAction.get(AddMirror.class).getPopupPresenter());
//            remotePopupMenu.add(SystemAction.get(AddRoot.class).getPopupPresenter());
//            remotePopupMenu.add(SystemAction.get(AddOther.class).getPopupPresenter());
        }
        if (localPopupMenu == null) {
            localPopupMenu = super.getPopupPresenter();
        }
    }

    private enum PLACE {

        ROOT("OpenRoot"),// NOI18N
        HOME("OpenHome"),// NOI18N
        PROJECTS("OpenProjects"),// NOI18N
        OTHER("OpenOtherFolder"), // NOI18N
        MIRROR("OpenMirror");// NOI18N
        private final String name;

        PLACE(String nameKey) {
            this.name = NbBundle.getMessage(OpenTerminalAction.class, nameKey);
        }

        private String getName() {
            return name;
        }
    }

    private static abstract class AddPlace extends SingleHostAction {

        private final PLACE place;

        private AddPlace(PLACE place) {
            this.place = place;
            putProperty("noIconInMenu", Boolean.TRUE);// NOI18N
        }

        protected abstract String getPath(ExecutionEnvironment env);

        @Override
        protected void performAction(final ExecutionEnvironment env, Node node) {
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(OpenTerminalAction.class,
                    "OpenTerminalAction.opening")); // NOI18N

            Runnable runnable = new Runnable() {

                @Override
                public void run() {
                    if (!ConnectionManager.getInstance().connect(env)) {
                        return;
                    }

                    final String path = getPath(env);

                    if (path == null || path.isEmpty()) {
                        String msg = NbBundle.getMessage(OpenTerminalAction.class, "NoRemotePath", path); // NOI18N
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                        return;
                    }

                    Runnable openTask = new Runnable() {

                        @Override
                        public void run() {
                            TerminalSupport.openTerminal(env.getDisplayName(), env, path);
                        }
                    };

                    SwingUtilities.invokeLater(openTask);
                }
            };

            RP.post(runnable);
        }

        @Override
        public String getName() {
            return place.getName();
        }
    }

    private static final class AddRoot extends AddPlace {

        public AddRoot() {
            super(PLACE.ROOT);
        }

        @Override
        protected String getPath(ExecutionEnvironment env) {
            return "/"; // NOI18N
        }
    }

    private static final class AddHome extends AddPlace {

        public AddHome() {
            super(PLACE.HOME);
        }

        @Override
        protected String getPath(ExecutionEnvironment env) {
            return getHomeDir(env);
        }
    }

    private static String getHomeDir(ExecutionEnvironment env) {
        if (HostInfoUtils.isHostInfoAvailable(env)) {
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                if (hostInfo != null) {
                    String userDir;
                    if (env.isLocal()) {
                        userDir = hostInfo.getUserDirFile().getAbsolutePath();
                    } else {
                        userDir = hostInfo.getUserDir();
                    }
                    return userDir;
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (CancellationException ex) {
                // don't report CancellationException
            }
        }
        return null;
    }
    

    private static final class AddMirror extends AddPlace {

        public AddMirror() {
            super(PLACE.MIRROR);
        }

        @Override
        protected String getPath(ExecutionEnvironment env) {
            String remoteSyncRoot = RemotePathMap.getRemoteSyncRoot(env);
            return remoteSyncRoot;
        }
    }
         
}
