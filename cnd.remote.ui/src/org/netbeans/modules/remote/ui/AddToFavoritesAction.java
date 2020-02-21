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

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.remote.mapper.RemotePathMap;
import org.netbeans.modules.favorites.api.Favorites;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.api.ui.FileChooserBuilder.JFileChooserEx;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.remote.ui.AddToFavoritesAction", category = "NativeRemote")
@ActionRegistration(displayName = "#AddToFavoritesMenuItem", lazy = false)
@ActionReference(path = "Remote/Host/Actions", name = "AddToFavoritesAction", position = 600)
public class AddToFavoritesAction extends SingleHostAction {

    private static final String FILE_CHOOSER_KEY = "remote.add.favorite"; //NOI18N
    private JMenu popupMenu;

    @Override
    public String getName() {
        return NbBundle.getMessage(HostListRootNode.class, "AddToFavoritesMenuItem"); // NOI18N
    }

    @Override
    protected void performAction(final ExecutionEnvironment env, Node node) {
        FileSystem fs = FileSystemProvider.getFileSystem(env);
        FileObject fo = fs.getRoot();
        if (!Favorites.getDefault().isInFavorites(fo)) {
            try {
                Favorites.getDefault().add(fo);
            } catch (NullPointerException ex) {
            } catch (DataObjectNotFoundException ex) {
            }
        }
    }

    @Override
    public boolean isVisible(Node node) {
        TopComponent favoritesComponent = getFavorites();
        return favoritesComponent != null && isRemote(node);
    }

    private static TopComponent getFavorites() {
        TopComponent favoritesComponent = WindowManager.getDefault().findTopComponent("favorites"); // NOI18N
        return favoritesComponent;
    }

    @Override
    public JMenuItem getPopupPresenter() {
        createSubMenu();
        return popupMenu;
    }

    private void createSubMenu() {
        if (popupMenu == null) {
            popupMenu = new JMenu(getName());
            popupMenu.add(SystemAction.get(AddHome.class).getPopupPresenter());
//            popupMenu.add(SystemAction.get(AddProjects.class).getPopupPresenter());
            popupMenu.add(SystemAction.get(AddMirror.class).getPopupPresenter());
            popupMenu.add(SystemAction.get(AddRoot.class).getPopupPresenter());
            popupMenu.add(SystemAction.get(AddOther.class).getPopupPresenter());
        }
    }

    private enum PLACE {

        ROOT("AddRoot"),// NOI18N
        HOME("AddHome"),// NOI18N
        PROJECTS("AddProjects"),// NOI18N
        OTHER("AddOtherFolder"), // NOI18N
        MIRROR("AddMirror");// NOI18N
        private final String name;

        PLACE(String nameKey) {
            this.name = NbBundle.getMessage(AddToFavoritesAction.class, nameKey);
        }

        private String getName() {
            return name;
        }
    }

    private static abstract class AddPlace extends SingleHostAction {

        protected static final RequestProcessor RP = new RequestProcessor("AddToFavoritesAction", 1); // NOI18N
        private final PLACE place;

        private AddPlace(PLACE place) {
            this.place = place;
            putProperty("noIconInMenu", Boolean.TRUE);// NOI18N
        }

        protected abstract FileObject getRoot(ExecutionEnvironment env, FileSystem fs);

        protected abstract String getPath(ExecutionEnvironment env);

        @Override
        protected void performAction(final ExecutionEnvironment env, Node node) {
            final TopComponent favorites = getFavorites();
            if (favorites != null) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (!ConnectionManager.getInstance().connect(env)) {
                            return;
                        }
                        FileSystem fs = FileSystemProvider.getFileSystem(env);
                        final FileObject fo = getRoot(env, fs);
                        if (fo != null) {
                            Runnable openFavorites = new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Favorites.getDefault().selectWithAddition(fo);
                                    } catch (DataObjectNotFoundException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                            };
                            SwingUtilities.invokeLater(openFavorites);
                        } else {
                            String path = getPath(env);
                            if (path != null) {
                                String msg;
                                if (!ConnectionManager.getInstance().isConnectedTo(env)) {
                                    msg = NbBundle.getMessage(AddToFavoritesAction.class, "NotConnected", path, env.getDisplayName());
                                } else {
                                    msg = NbBundle.getMessage(AddToFavoritesAction.class, "NoRemotePath", path);
                                }
                                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg));
                            }
                        }
                    }
                };
                RP.post(runnable);
            }
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
        protected FileObject getRoot(ExecutionEnvironment env, FileSystem fs) {
            return fs.getRoot();
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
        protected FileObject getRoot(ExecutionEnvironment env, FileSystem fs) {
            String path = getPath(env);
            return path == null ? null : fs.findResource(path);
        }

        @Override
        protected String getPath(ExecutionEnvironment env) {
            try {
                HostInfo hostInfo = HostInfoUtils.getHostInfo(env);
                if (hostInfo != null) {
                    String userDir = hostInfo.getUserDir();
                    return userDir;
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            } catch (CancellationException ex) {
                // don't report CancellationException
            }
            return null;
        }
    }

    private static final class AddProjects extends AddPlace {

        public AddProjects() {
            super(PLACE.PROJECTS);
        }

        @Override
        protected FileObject getRoot(ExecutionEnvironment env, FileSystem fs) {
            return fs.getRoot();
        }

        @Override
        protected String getPath(ExecutionEnvironment env) {
            return "/"; // NOI18N
        }
    }

    private static final class AddMirror extends AddPlace {

        public AddMirror() {
            super(PLACE.MIRROR);
        }

        @Override
        protected FileObject getRoot(ExecutionEnvironment env, FileSystem fs) {
            String path = getPath(env);
            return path == null ? null : fs.findResource(path);
        }

        @Override
        protected String getPath(ExecutionEnvironment env) {
            String remoteSyncRoot = RemotePathMap.getRemoteSyncRoot(env);
            return remoteSyncRoot;
        }
    }

    private static final class AddOther extends AddPlace {

        private final Frame mainWindow;

        public AddOther() {
            super(PLACE.OTHER);
            mainWindow = WindowManager.getDefault().getMainWindow();
        }

        @Override
        protected FileObject getRoot(ExecutionEnvironment env, FileSystem fs) {
            throw new IllegalArgumentException(NbBundle.getMessage(AddToFavoritesAction.class, "AddToFavoritesAction.AddOther.DoNotInvoke.getRoot.directly"));//NOI18N
        }

        @Override
        protected String getPath(ExecutionEnvironment env) {
            return null;
        }

        @Override
        protected void performAction(final ExecutionEnvironment env, Node node) {
            final TopComponent favorites = getFavorites();
            if (favorites != null) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (!ConnectionManager.getInstance().connect(env)) {
                            return;
                        }
//Non UI Thread:
                        final String dir = RemoteFileChooserUtil.getCurrentChooserFile(FILE_CHOOSER_KEY, env);
                        final Callable<String> homeDirCallable =  new Callable<String>() {
                            @Override
                            public String call() throws Exception {
                                return dir == null ? getHomeDir(env) : dir;
                            }
                        };

                        Runnable openFavorites = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    FileObject rootFO = null;
                                    //UI thread:
                                    String title = NbBundle.getMessage(AddToFavoritesAction.class, "SelectFolder");
                                    String btn = NbBundle.getMessage(AddToFavoritesAction.class, "AddText");
                                    JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(
                                            env,
                                            title,
                                            btn,
                                            JFileChooser.DIRECTORIES_ONLY, null, homeDirCallable, true);
                                    int ret = fileChooser.showOpenDialog(mainWindow);
                                    if (ret == JFileChooser.CANCEL_OPTION) {
                                        rootFO = null;
                                    } else {
                                        if (fileChooser instanceof JFileChooserEx) {
                                            rootFO = ((JFileChooserEx) fileChooser).getSelectedFileObject();
                                        } else {
                                            File selectedFile = fileChooser.getSelectedFile();
                                            if (selectedFile != null) {
                                                rootFO = FileUtil.toFileObject(selectedFile);
                                            }
                                        }
                                        if (rootFO == null || !rootFO.isFolder()) {
                                            String msg = fileChooser.getSelectedFile() != null ? fileChooser.getSelectedFile().getPath() : null;
                                            if (msg != null) {
                                                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                                                        NbBundle.getMessage(OpenRemoteProjectAction.class, "InvalidFolder", msg));
                                            }
                                        } else {
                                            String lastPath = rootFO.getParent() == null ? rootFO.getPath() : rootFO.getParent().getPath();
                                            RemoteFileChooserUtil.setCurrentChooserFile(FILE_CHOOSER_KEY, lastPath, env);
                                        }
                                    }
                                    if (rootFO != null) {
                                        Favorites.getDefault().selectWithAddition(rootFO);
                                    }
                                } catch (DataObjectNotFoundException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        };
                        SwingUtilities.invokeLater(openFavorites);

                    }
                };
                RP.post(runnable);
            }
        }
    }

    static FileObject getRemoteFileObject(final ExecutionEnvironment env, String title, String btn, Frame mainWindow) {
        //Non UI Thread:
        final String curDir = RemoteFileChooserUtil.getCurrentChooserFile(FILE_CHOOSER_KEY, env);
        final Callable<String> homeDirCallable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return curDir == null ? getHomeDir(env) : curDir;
            }
        };      

        
        //UI thread:
        JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(
                env,
                title,
                btn,
                JFileChooser.DIRECTORIES_ONLY, null, homeDirCallable, true);
        int ret = fileChooser.showOpenDialog(mainWindow);
        if (ret == JFileChooser.CANCEL_OPTION) {
            return null;
        }
        FileObject fo = null;
        if (fileChooser instanceof JFileChooserEx) {
            fo = ((JFileChooserEx) fileChooser).getSelectedFileObject();
        } else {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null) {
                fo = FileUtil.toFileObject(selectedFile);
            }
        }
        if (fo == null || !fo.isFolder()) {
            String msg = fileChooser.getSelectedFile() != null ? fileChooser.getSelectedFile().getPath() : null;
            if (msg != null) {
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                        NbBundle.getMessage(OpenRemoteProjectAction.class, "InvalidFolder", msg));
            }
            return null;
        }
        String lastPath = fo.getParent() == null ? fo.getPath() : fo.getParent().getPath();
        RemoteFileChooserUtil.setCurrentChooserFile(FILE_CHOOSER_KEY, lastPath, env);
        return fo;
    }

    private static String getHomeDir(ExecutionEnvironment env) {
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
        return null;
    }
}
