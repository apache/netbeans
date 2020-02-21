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

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileView;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.api.ui.FileChooserBuilder.JFileChooserEx;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.remote.ui.OpenRemoteProjectAction", category = "NativeRemote")
@ActionRegistration(displayName = "#OpenRemoteProjectActionMenuItem", lazy = false)
@ActionReference(path = "Remote/Host/Actions", name = "OpenRemoteProjectAction", position = 250)
public class OpenRemoteProjectAction extends SingleHostAction {

    private static final RequestProcessor RP = new RequestProcessor("Opening remote project", 1); //NOI18N
    private static boolean isRunning = false;
    private static final Object lock = new Object();
    
    @Override
    public String getName() {
        return NbBundle.getMessage(HostListRootNode.class, "OpenRemoteProjectActionMenuItem");
    }

    @Override
    protected void performAction(final ExecutionEnvironment env, Node node) {
        setEnabled(false);
        final Runnable edtWorker = new Runnable() {
            @Override
            public void run() {
                openRemoteProject(env);
            }
        };
        final Runnable connectWrapper = new Runnable() {
            @Override
            public void run() {
                try {
                    ConnectionManager.getInstance().connectTo(env);
                    SwingUtilities.invokeLater(edtWorker);
                } catch (IOException ex) {                    
                    showErrorConnectionDialog(env, ex);
                } catch (CancellationException ex) {
                    // don't report CancellationException
                } finally {
                    synchronized (lock) {
                        isRunning = false;
                    }
                    setEnabled(true);
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
    
    private void showErrorConnectionDialog(final ExecutionEnvironment env, final IOException ex) throws HeadlessException, MissingResourceException {
        RemoteUtil.LOGGER.log(Level.INFO, "Error connecting " + env, ex);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(), 
                        NbBundle.getMessage(OpenRemoteProjectAction.class, "ErrorConnectingHost", env.getDisplayName(), ex.getMessage()));
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            SwingUtilities.invokeLater(r);
        }
    }    

    @Override
    protected boolean enable(ExecutionEnvironment env) {
        synchronized (lock) {
            return ! isRunning;
        }
    }
    
    @Override
    public boolean isVisible(Node node) {
        return isRemote(node);
    }
    
    private void openRemoteProject(final ExecutionEnvironment env) {            
        final String chooser_key = "open.remote.project";//NOI18N
        final String homeDir = RemoteFileChooserUtil.getCurrentChooserFile(chooser_key, env);
        final Callable<String> homeDirCallable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                return homeDir == null ? getRemoteProjectDir(env) : homeDir;
            }
        };             
        JFileChooserEx fileChooser = (JFileChooserEx) RemoteFileChooserUtil.createFileChooser(env,
            NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenProjectTitle", env.getDisplayName()),
            NbBundle.getMessage(OpenRemoteProjectAction.class, "OpenProjectButtonText"),
            JFileChooser.DIRECTORIES_ONLY, null, homeDirCallable, true);
        fileChooser.setFileView(new ProjectSelectionFileView(fileChooser));
        int ret = fileChooser.showOpenDialog(WindowManager.getDefault().getMainWindow());
        if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }
        FileObject remoteProjectFO = fileChooser.getSelectedFileObject();
        if (remoteProjectFO == null || !remoteProjectFO.isFolder()) {
            String msg = fileChooser.getSelectedFile() != null ? fileChooser.getSelectedFile().getPath() : null;
            if (msg != null) {
                JOptionPane.showMessageDialog(WindowManager.getDefault().getMainWindow(),
                    NbBundle.getMessage(OpenRemoteProjectAction.class, "InvalidFolder", msg));
            }
            return;
        }
        String currentChooserFile = remoteProjectFO.getParent() == null ? remoteProjectFO.getPath() : remoteProjectFO.getParent().getPath();
        RemoteFileChooserUtil.setCurrentChooserFile(chooser_key, currentChooserFile, env);
        Project project;
        try {
            project = ProjectManager.getDefault().findProject(remoteProjectFO);
            if (project != null) {
                OpenProjects.getDefault().open(new Project[] {project}, false, true);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private static String getRemoteProjectDir(ExecutionEnvironment env) {
        try {
            return HostInfoUtils.getHostInfo(env).getUserDir() + '/' + ProjectChooser.getProjectsFolder().getName();  //NOI18N
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
