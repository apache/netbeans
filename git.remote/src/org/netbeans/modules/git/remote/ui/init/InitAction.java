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
package org.netbeans.modules.git.remote.ui.init;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.git.remote.cli.GitException;
import org.netbeans.modules.git.remote.Git;
import org.netbeans.modules.git.remote.client.GitClient;
import org.netbeans.modules.git.remote.client.GitClientExceptionHandler;
import org.netbeans.modules.git.remote.client.GitProgressSupport;
import org.netbeans.modules.git.remote.ui.actions.ContextHolder;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Init action for git: 
 * git init - init a new repository in the given directory
 * 
 */
@ActionID(id = "org.netbeans.modules.git.remote.ui.init.InitAction", category = "GitRemote")
@ActionRegistration(displayName = "#LBL_InitAction_Name", popupText="#LBL_InitAction.popupName", menuText="#LBL_InitAction_Name")
@ActionReferences({
   @ActionReference(path="Versioning/GitRemote/Actions/Unversioned", position=301)
})
@NbBundle.Messages({
    "LBL_InitAction.popupName=Initialize &Git Repository...",
    "LBL_InitAction_Name=I&nitialize Repository..."
})
public class InitAction implements ActionListener, HelpCtx.Provider {

    private static final Logger LOG = Logger.getLogger(InitAction.class.getName());
    private final VCSContext ctx;

    public InitAction(ContextHolder ctx) {
        this.ctx = ctx.getContext();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.git.remote.ui.init.InitAction");
            }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(!isEnabled()) {
            return;
        }
        Git.getInstance().getRequestProcessor().post(new Runnable() {
            @Override
            public void run() {
                Utils.logVCSActionEvent("Git"); //NOI18N
                performInit(ctx);
            }
        });
    }

    private void performInit (VCSContext context) {
        final VCSFileProxy rootToManage = selectRootToManage(context);
        if (rootToManage == null) {
            return;
        }

        RequestProcessor rp = Git.getInstance().getRequestProcessor(rootToManage);
        GitProgressSupport support = new GitProgressSupport() {
            @Override
            public void perform() {
                try {
                    output(NbBundle.getMessage(InitAction.class, "MSG_INIT", rootToManage)); // NOI18N
                    GitClient client = getClient();
                    client.init(getProgressMonitor());
                    Git.getInstance().getFileStatusCache().refreshAllRoots(rootToManage);
                    Git.getInstance().versionedFilesChanged();                       
                } catch (GitException ex) {
                    GitClientExceptionHandler.notifyException(ex, true);
                } finally {
                    Git.getInstance().clearAncestorCaches();
                    VersioningSupport.versionedRootsChanged();
                }
            }            
        };
        support.start(rp, rootToManage, NbBundle.getMessage(InitAction.class, "MSG_Init_Progress")); // NOI18N
    }

    private VCSFileProxy selectRootToManage (VCSContext context) {
        VCSFileProxy rootPath = getSuggestedRoot(context);
        final VCSFileProxy root = rootPath;

        final InitPanel panel = new InitPanel(root);
        panel.lblMessage.setVisible(false);
        final DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(InitAction.class, "LBL_Init_Panel_Label"), //NOI18N
                true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(InitPanel.class), null);
        dd.setValid(false);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        final RequestProcessor.Task validateTask = Git.getInstance().getRequestProcessor().create(new Runnable() {
            @Override
            public void run() {
                String validatedPath = panel.tfRootPath.getText();
                String errorMessage = null;
                boolean valid = !validatedPath.trim().isEmpty();
                VCSFileProxy dir = VCSFileProxySupport.getResource(root, validatedPath);
                // must be an existing directory
                if (!dir.isDirectory()) {
                    errorMessage = NbBundle.getMessage(InitAction.class, "LBL_Init_Panel_Error_Directory"); //NOI18N
                    if (LOG.isLoggable(Level.FINE) && dir.exists()) {
                        LOG.log(Level.FINE, "InitAction.selectRootToManage.validateTask: selected a file: {0}", dir); //NOI18N
                    }
                    valid = false;
                }
                if (valid) {
                    if (Thread.interrupted()) {
                        return;
                    }
                    if (dir.equals(Git.getInstance().getRepositoryRoot(dir))) {
                        valid = false;
                        errorMessage = NbBundle.getMessage(InitAction.class, "LBL_Init_Panel_Error_Versioned"); //NOI18N
                    }
                }
                if (Thread.interrupted()) {
                    return;
                }
                if (valid) {
                    // warning message (validation does not fail) for directories under a project
                    FileObject fo = dir.normalizeFile().toFileObject();
                    Project p = FileOwnerQuery.getOwner(fo);
                    if (p != null) {
                        FileObject projectDir = p.getProjectDirectory();
                        if (FileUtil.isParentOf(projectDir, fo)) {
                            errorMessage = NbBundle.getMessage(InitAction.class, "LBL_Init_Panel_Warning_Under_Project"); //NOI18N
                        }
                    }
                }
                if (Thread.interrupted()) {
                    return;
                }
                dd.setValid(valid);
                if (errorMessage != null) {
                    panel.lblMessage.setText(errorMessage);
                    panel.lblMessage.setForeground(javax.swing.UIManager.getDefaults().getColor(valid ? "nb.warningForeground" : "nb.errorForeground")); //NOI18N
                }
                panel.lblMessage.setVisible(errorMessage != null);
                panel.invalidate();
            }
        });

        panel.tfRootPath.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                validate();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                validate();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                validate();
            }
            private void validate () {
                validateTask.cancel();
                dd.setValid(false);
                validateTask.schedule(300);
            }
        });
        panel.tfRootPath.setText(rootPath == null ? "" : rootPath.getPath()); //NOI18N
        do {
            dialog.setVisible(true);
            if (dd.getValue() == DialogDescriptor.OK_OPTION) {
                rootPath = VCSFileProxySupport.getResource(root, panel.tfRootPath.getText());
                validateTask.run();
            } else {
                rootPath = null;
            }
        } while (!dd.isValid() && dd.getValue() == DialogDescriptor.OK_OPTION);

        return rootPath;
    }

    /**
     * Returns a common ancestor for all context rootfiles
     * If these belong to a project, returns a common ancestor of all rootfiles and the project folder
     * @return
     */
    private VCSFileProxy getSuggestedRoot (VCSContext context) {
        final VCSFileProxy [] files = context.getRootFiles().toArray(new VCSFileProxy[context.getRootFiles().size()]);
        if (files == null || files.length == 0) {
            return null;
        }

        final Project proj = VCSFileProxySupport.getProject(files);
        final VCSFileProxy projFile = VCSFileProxySupport.getProjectFile(proj);

        VCSFileProxy root = null;
        root = getCommonAncestor(files);
        if (LOG.isLoggable(Level.FINER)) {
            LOG.log(Level.FINER, "InitAction.getSuggestedRoot: common root for {0}: {1}", new Object[]{context.getRootFiles(), root}); //NOI18N
        }

        if (projFile != null) {
            root = getCommonAncestor(root, projFile);
            if (LOG.isLoggable(Level.FINER)) {
                LOG.log(Level.FINER, "InitAction.getSuggestedRoot: root with project at {0}: {1}", new Object[]{projFile, root}); //NOI18N
            }
        }
        return root;
    }
    

    private VCSFileProxy getCommonAncestor(VCSFileProxy firstFile, VCSFileProxy secondFile) {
        if (firstFile.equals(secondFile)) {
            return firstFile;
        }

        VCSFileProxy tempFirstFile = firstFile;
        while (tempFirstFile != null) {
            VCSFileProxy tempSecondFile = secondFile;
            while (tempSecondFile != null) {
                if (tempFirstFile.equals(tempSecondFile)) {
                    return tempSecondFile;
                }
                tempSecondFile = tempSecondFile.getParentFile();
}
            tempFirstFile = tempFirstFile.getParentFile();
        }
        return null;
    }

    private VCSFileProxy getCommonAncestor(VCSFileProxy[] files) {
        VCSFileProxy f1 = files[0];

        for (int i = 1; i < files.length; i++) {
            VCSFileProxy f = getCommonAncestor(f1, files[i]);
            if (f == null) {
                LOG.log(Level.SEVERE, "Unable to get common parent of {0} and {1} ", // NOI18N
                        new Object[] {f1.getPath(), files[i].getPath()});
                // XXX not sure wat to do at this point
            } else {
                f1 = f;
            }
        }
        return f1;
    }

    private boolean isEnabled() {
        Set<VCSFileProxy> rootFiles = ctx.getRootFiles();
        if (rootFiles.isEmpty()) {
            return false;
        }
        VCSFileProxy root = rootFiles.iterator().next();
        if (!VCSFileProxySupport.isConnectedFileSystem(VCSFileProxySupport.getFileSystem(root))) {
            notifyImportImpossible(NbBundle.getMessage(InitAction.class, "MSG_FileSystemDisconnected")); // NOI18N
            return false;
        }
        boolean ret = !GitUtils.isFromGitRepository(ctx);
        if(!ret) {
            notifyImportImpossible(NbBundle.getMessage(InitAction.class, "MSG_AlreadyVersioned")); // NOI18N
        }
        return ret;
    }

    private void notifyImportImpossible(String msg) {
        NotifyDescriptor nd =
            new NotifyDescriptor(
                msg,
                NbBundle.getMessage(InitAction.class, "MSG_ImportNotAllowed"), // NOI18N
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(nd);
    }    
    
}
