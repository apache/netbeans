/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
