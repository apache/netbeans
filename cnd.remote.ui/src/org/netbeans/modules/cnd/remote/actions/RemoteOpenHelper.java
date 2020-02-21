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
package org.netbeans.modules.cnd.remote.actions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileView;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 */
public class RemoteOpenHelper {

    private final ExecutionEnvironment env;
    private static final Logger LOGGER = Logger.getLogger("cnd.remote.logger"); //NOI18N
    private static boolean isRunning = false;
    private static final Object lock = new Object();
    private static final RequestProcessor RP = new RequestProcessor("Opening remote project", 1); //NOI18N
    private static final Map<ExecutionEnvironment, String> lastUsedDirs = new HashMap<>();

    private RemoteOpenHelper(ExecutionEnvironment env) {
        this.env = env;
    }

    public static void openFile(ExecutionEnvironment env) {
        new RemoteOpenHelper(env).openFile();
    }

    public static void openProject(ExecutionEnvironment env) {
        new RemoteOpenHelper(env).openProject();
    }

    private void openFile() {
        openImpl(getOpenRemoteFileWorker(env));
    }

    private void openProject() {
        openImpl(getOpenRemoteProjectWorker(env));
    }

    private void openImpl(final Runnable worker) {
        final Runnable connectWrapper = new Runnable() {
            @Override
            public void run() {
                try {
                    ConnectionManager.getInstance().connectTo(env);
                    worker.run();
                } catch (final IOException ex) {
                    LOGGER.log(Level.INFO, "Error connecting " + env, ex);
                    DialogDisplayer.getDefault().notify(new DialogDescriptor.Message(NbBundle.getMessage(OpenRemoteProjectAction.class, "ErrorConnectingHost", env.getDisplayName(), ex.getMessage()), DialogDescriptor.ERROR_MESSAGE));
                } catch (CancellationException ex) {
                    // don't report CancellationException
                } finally {
                    synchronized (lock) {
                        isRunning = false;
                    }
                }
            }
        };
        synchronized (lock) {
            if (!isRunning) {
                isRunning = true;
                RP.post(connectWrapper);
            }
        }
    }

    private static Callable<String> getCurrentDirectory(final ExecutionEnvironment env) {
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                if (Boolean.getBoolean("netbeans.openfile.197063")) {
                    // Prefer to open from parent of active editor, if any.
                    TopComponent activated = TopComponent.getRegistry().getActivated();
                    if (activated != null && WindowManager.getDefault().isOpenedEditorTopComponent(activated)) {
                        DataObject d = activated.getLookup().lookup(DataObject.class);
                        if (d != null) {
                            FileObject primaryFile = d.getPrimaryFile();
                            if (primaryFile != null && primaryFile.isValid()) {
                                try {
                                    if (primaryFile.getFileSystem().equals(FileSystemProvider.getFileSystem(env))) {
                                        FileObject parent = primaryFile.getParent();
                                        if (parent != null && parent.isValid()) {
                                            return parent.getPath();
                                        }
                                    }
                                } catch (FileStateInvalidException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                    }
                }
                String homeDir = lastUsedDirs.get(env);
                if (homeDir == null) {
                    homeDir = getRemoteHomeDir(env);
                }
                return homeDir;
            }
        };

    }

    /**
     * Returns Runnable which should be ran in non-UI thread
     * @param env
     * @return 
     */
    private Runnable getOpenRemoteFileWorker(final ExecutionEnvironment env) {
        Runnable worker = new Runnable() {
            @Override
            public void run() {
                final Callable<String> homeDir = getCurrentDirectory(env);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        final JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(env,
                                NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenFileTitle"),
                                NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenFileButtonText"),
                                JFileChooser.FILES_ONLY, null, homeDir, true);
                        int ret = fileChooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
                        if (ret == JFileChooser.CANCEL_OPTION) {
                            return;
                        }
                        Runnable worker = new Runnable() {
                            @Override
                            public void run() {
                                FileObject fo = FileSystemProvider.fileToFileObject(fileChooser.getSelectedFile());
                                if (fo == null) {
                                    return;
                                }
                                FileObject parent = fo.getParent();
                                if (parent != null) {
                                    lastUsedDirs.put(env, parent.getPath());
                                }
                                if (!fo.isValid() || !fo.isData()) {
                                    return;
                                }
                                try {
                                    DataObject dob = DataObject.find(fo);
                                    EditorCookie ec = dob.getCookie(EditorCookie.class);
                                    if (ec != null) {
                                        ec.open();
                                    }
                                } catch (DataObjectNotFoundException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        };
                        RP.post(worker);
                    }
                });

            }
        };
        return worker;

    }

    /**
     * Returns Runnable which should be ran in non-UI thread
     * @param env
     * @return 
     */
    private Runnable getOpenRemoteProjectWorker(final ExecutionEnvironment env) {
        Runnable worker = new Runnable() {
            @Override
            public void run() {
                final String home = lastUsedDirs.get(env);
                final Callable<String> homeDirCallable = home != null ? new Callable<String>() {

                    @Override
                    public String call() throws Exception {
                        return home;
                    }
                } : getRemoteProjectDirCallable(env);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        final JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(env,
                                NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenProjectTitle"),
                                NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenProjectButtonText"),
                                JFileChooser.DIRECTORIES_ONLY, null, homeDirCallable, true);
                        fileChooser.setFileView(new ProjectSelectionFileView(fileChooser));
                        fileChooser.setMultiSelectionEnabled(true);
                        int ret = fileChooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
                        if (ret == JFileChooser.CANCEL_OPTION) {
                            return;
                        }
                        Runnable worker = new Runnable() {
                            @Override
                            public void run() {
                                File[] files = fileChooser.getSelectedFiles();
                                if (files == null || files.length == 0) {
                                    return;
                                }
                                FileObject parent = null;
                                List<Project> projects = new ArrayList<>(files.length);
                                for (File file : files) {
                                    FileObject remoteProjectFO = FileSystemProvider.fileToFileObject(file);
                                    if (remoteProjectFO == null) {
                                        continue;
                                    }
                                    if (parent == null) {
                                        parent = remoteProjectFO.getParent();
                                        if (parent != null) {
                                            lastUsedDirs.put(env, parent.getPath());
                                        }
                                    }
                                    if (!remoteProjectFO.isValid() || !remoteProjectFO.isFolder()) {
                                        continue;
                                    }
                                    try {
                                        Project project = ProjectManager.getDefault().findProject(remoteProjectFO);
                                        if (project != null) {
                                            projects.add(project);
                                        }
                                    } catch (IOException ex) {
                                        StatusDisplayer.getDefault().setStatusText(ex.getLocalizedMessage());
                                        ex.printStackTrace(System.err);
                                    } catch (IllegalArgumentException ex) {
                                        Exceptions.printStackTrace(ex);
                                    }
                                }
                                OpenProjects.getDefault().open(projects.toArray(new Project[projects.size()]), false, true);
                            }
                        };
                        RP.post(worker);
                    }
                });

            }
        };
        return worker;
    }

    
    private static Callable<String> getRemoteProjectDirCallable(final ExecutionEnvironment env) {
        return new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    return HostInfoUtils.getHostInfo(env).getUserDir() + '/' + ProjectChooser.getProjectsFolder().getName() + '/';  //NOI18N
                } catch (IOException ex) {
                    ex.printStackTrace(System.err); // it doesn't make sense to disturb user
                } catch (CancellationException ex) {
                    // don't report CancellationException
                }
                return null;
            }
        };

    } 

    private static String getRemoteProjectDir(ExecutionEnvironment env) {
        try {
            return HostInfoUtils.getHostInfo(env).getUserDir() + '/' + ProjectChooser.getProjectsFolder().getName() + '/';  //NOI18N
        } catch (IOException ex) {
            ex.printStackTrace(System.err); // it doesn't make sense to disturb user
        } catch (CancellationException ex) {
            // don't report CancellationException
        }
        return null;
    }

    private static String getRemoteHomeDir(ExecutionEnvironment env) {
        try {
            return HostInfoUtils.getHostInfo(env).getUserDir() + '/';  //NOI18N
        } catch (IOException ex) {
            ex.printStackTrace(System.err); // it doesn't make sense to disturb user
        } catch (CancellationException ex) {
            // don't report CancellationException
        }
        return null;
    }

    private static final class ProjectSelectionFileView extends FileView implements Runnable {

        private final JFileChooser chooser;
        private final Map<File, Icon> knownProjectIcons = new HashMap<>();
        private final RequestProcessor.Task task = new RequestProcessor("ProjectIconFileView").create(this);//NOI18N
        private File lookingForIcon;

        public ProjectSelectionFileView(JFileChooser chooser) {
            this.chooser = chooser;
        }

        @Override
        public Icon getIcon(File f) {
            if (f.isDirectory() && // #173958: do not call ProjectManager.isProject now, could block
                    !f.toString().matches("/[^/]+") && // Unix: /net, /proc, etc. //NOI18N
                    f.getParentFile() != null) { // do not consider drive roots
                synchronized (this) {
                    Icon icon = knownProjectIcons.get(f);
                    if (icon != null) {
                        return icon;
                    } else if (lookingForIcon == null) {
                        lookingForIcon = f;
                        task.schedule(20);
                        // Only calculate one at a time.
                        // When the view refreshes, the next unknown icon
                        // should trigger the task to be reloaded.
                    }
                }
            }
            return chooser.getFileSystemView().getSystemIcon(f);
        }

        @Override
        public void run() {
            String path = lookingForIcon.getAbsolutePath();
            String project = path + "/nbproject"; // NOI18N
            File projectDir = chooser.getFileSystemView().createFileObject(project);
            Icon icon = chooser.getFileSystemView().getSystemIcon(lookingForIcon);
            if (projectDir.exists() && projectDir.isDirectory() && projectDir.canRead()) {
                String projectXml = path + "/nbproject/project.xml"; // NOI18N
                File projectFile = chooser.getFileSystemView().createFileObject(projectXml);
                if (projectFile.exists()) {
                    String conf = path + "/nbproject/configurations.xml"; // NOI18N
                    File configuration = chooser.getFileSystemView().createFileObject(conf);
                    if (configuration.exists()) {
                        icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/makeproject/resources/makeProject.gif", true); // NOI18N
                    }
                }
            }
            synchronized (this) {
                knownProjectIcons.put(lookingForIcon, icon);
                lookingForIcon = null;
            }
            chooser.repaint();
        }
    }
}
