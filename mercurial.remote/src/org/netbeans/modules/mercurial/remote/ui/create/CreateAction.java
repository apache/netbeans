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
package org.netbeans.modules.mercurial.remote.ui.create;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.mercurial.remote.FileInformation;
import org.netbeans.modules.mercurial.remote.FileStatusCache;
import org.netbeans.modules.mercurial.remote.HgException;
import org.netbeans.modules.mercurial.remote.HgProgressSupport;
import org.netbeans.modules.mercurial.remote.Mercurial;
import org.netbeans.modules.mercurial.remote.OutputLogger;
import org.netbeans.modules.mercurial.remote.util.HgCommand;
import org.netbeans.modules.mercurial.remote.util.HgUtils;
import org.netbeans.modules.remotefs.versioning.api.VCSFileProxySupport;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
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
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

/**
 * Create action for mercurial: 
 * hg init - create a new repository in the given directory
 * 
 * 
 */
@ActionID(id = "org.netbeans.modules.mercurial.remote.ui.create.CreateAction", category = "MercurialRemote")
@ActionRegistration(displayName = "#CTL_MenuItem_Create", popupText="#CTL_PopupMenuItem_Create", menuText="#CTL_MenuItem_Create")
@ActionReferences({
   @ActionReference(path="Versioning/MercurialRemote/Actions/Unversioned", position=2)
})
@Messages({"CTL_MenuItem_Create=I&nitialize Repository...", //NOI18N
    "CTL_PopupMenuItem_Create=Initialize &Mercurial Repository..."}) //NOI18N
public class CreateAction implements ActionListener, HelpCtx.Provider {
    private final VCSFileProxy[] rootFiles;

    public CreateAction (List<VCSFileProxy> rootFiles) {
        this.rootFiles = rootFiles.toArray(new VCSFileProxy[rootFiles.size()]);
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.mercurial.remote.ui.create.CreateAction");
    }
    
    private boolean isEnabled() {
        // If it is not a mercurial managed repository enable action
        if ( rootFiles == null || rootFiles.length == 0) {
            notifyImportImpossible(NbBundle.getMessage(CreateAction.class, "MSG_WrongSelection"));            
            return false;
        }
        
        VCSFileProxy root = Mercurial.getInstance().getRepositoryRoot(rootFiles[0]);
        if (root == null) {
            return true;
        } else {
            notifyImportImpossible(NbBundle.getMessage(CreateAction.class, "MSG_AlreadyVersioned"));            
            return false;
        }
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
                Mercurial.LOG.log(Level.SEVERE, "Unable to get common parent of {0} and {1} ", // NOI18N
                        new Object[] {f1.getPath(), files[i].getPath()});
                // XXX not sure wat to do at this point
            } else {
                f1 = f;
            }
        }
        return f1;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(!isEnabled()) {
            return;
        }
        performCreate();
    }
    
    private void performCreate () {
        HgUtils.runIfHgAvailable(rootFiles[0], new Runnable() {
            @Override
            public void run () {
                final Mercurial hg = Mercurial.getInstance();
                final VCSFileProxy rootToManage = selectRootToManage();
                if (rootToManage == null) {
                    return;
                }

                RequestProcessor rp = hg.getRequestProcessor(rootToManage);
                HgProgressSupport supportCreate = new HgProgressSupport() {
                    @Override
                    public void perform() {

                        Utils.logVCSActionEvent("HG"); //NOI18N
                        try {
                            OutputLogger logger = getLogger();
                            logger.outputInRed(
                                    NbBundle.getMessage(CreateAction.class, "MSG_CREATE_TITLE")); // NOI18N
                            logger.outputInRed(
                                    NbBundle.getMessage(CreateAction.class, "MSG_CREATE_TITLE_SEP")); // NOI18N
                            logger.output(
                                    NbBundle.getMessage(CreateAction.class,
                                    "MSG_CREATE_INIT", rootToManage)); // NOI18N
                            HgCommand.doCreate(rootToManage, logger);
                            hg.versionedFilesChanged();
                            hg.refreshAllAnnotations();
                        } catch (HgException.HgCommandCanceledException ex) {
                            // canceled by user, do nothing
                        } catch (HgException ex) {
                            HgUtils.notifyException(ex);
                        } finally {
                            Mercurial.getInstance().clearAncestorCaches();
                            VersioningSupport.versionedRootsChanged();
                        }
                    }
                };
                supportCreate.start(rp, rootToManage,
                        org.openide.util.NbBundle.getMessage(CreateAction.class, "MSG_Create_Progress")); // NOI18N

                HgProgressSupport supportAdd = new HgProgressSupport() {
                    @Override
                    public void perform() {
                        OutputLogger logger = getLogger();
                        try {
                            VCSFileProxy[] repositoryFiles;
                            FileStatusCache cache = hg.getFileStatusCache();
                            Calendar start = Calendar.getInstance();
                            cache.refreshAllRoots(Collections.singletonMap(rootToManage, Collections.singleton(rootToManage)));
                            Calendar end = Calendar.getInstance();
                            Mercurial.LOG.log(Level.FINE, "cache refresh took {0} millisecs", end.getTimeInMillis() - start.getTimeInMillis()); // NOI18N
                            repositoryFiles = cache.listFiles(new VCSFileProxy[] {rootToManage}, FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY);
                            logger.output(
                                    NbBundle.getMessage(CreateAction.class,
                                    "MSG_CREATE_ADD", repositoryFiles.length)); // NOI18N
                            if (repositoryFiles.length < OutputLogger.MAX_LINES_TO_PRINT) {
                                for (VCSFileProxy f : repositoryFiles) {
                                    logger.output("\t" + f.getPath());  //NOI18N
                                }
                            }
                            HgUtils.createIgnored(rootToManage);
                            logger.output(""); // NOI18N
                            logger.outputInRed(NbBundle.getMessage(CreateAction.class, "MSG_CREATE_DONE_WARNING")); // NOI18N
                        } finally {
                            logger.outputInRed(NbBundle.getMessage(CreateAction.class, "MSG_CREATE_DONE")); // NOI18N
                            logger.output(""); // NOI18N
                        }
                    }
                };
                supportAdd.start(rp, rootToManage,
                        org.openide.util.NbBundle.getMessage(CreateAction.class, "MSG_Create_Add_Progress")); // NOI18N
            }
        });
    }

    private VCSFileProxy selectRootToManage () {
        VCSFileProxy rootPath = getSuggestedRoot();

        final CreatePanel panel = new CreatePanel(rootPath);
        panel.lblMessage.setVisible(false);
        final DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(CreateAction.class, "LBL_Create_Panel_Label"), //NOI18N
                true, DialogDescriptor.OK_CANCEL_OPTION, DialogDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(CreatePanel.class), null);
        dd.setValid(false);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);

        final RequestProcessor.Task validateTask = Mercurial.getInstance().getRequestProcessor().create(new Runnable() {
            @Override
            public void run() {
                String validatedPath = panel.tfRootPath.getText();
                String errorMessage = null;
                boolean valid = !validatedPath.trim().isEmpty();
                VCSFileProxy dir = VCSFileProxySupport.getResource(rootFiles[0], validatedPath);
                // must be an existing directory
                if (!dir.isDirectory()) {
                    errorMessage = NbBundle.getMessage(CreateAction.class, "LBL_Create_Panel_Error_Directory"); //NOI18N
                    if (Mercurial.LOG.isLoggable(Level.FINE) && dir.exists()) {
                        Mercurial.LOG.fine("CreateAction.selectRootToManage.validateTask: selected a file: " + dir); //NOI18N
                    }
                    valid = false;
                }
                if (valid) {
                    if (Thread.interrupted()) {
                        return;
                    }
                    // children can't be versioned
                    VCSFileProxy[] children = dir.listFiles();
                    if (children != null) {
                        for (VCSFileProxy f : children) {
                            VCSFileProxy repoRoot = null;
                            if (f.isDirectory() && (repoRoot = Mercurial.getInstance().getRepositoryRoot(f)) != null) {
                                valid = false;
                                if (Mercurial.LOG.isLoggable(Level.FINE) && dir.exists()) {
                                    Mercurial.LOG.fine("CreateAction.selectRootToManage.validateTask: file is versioned: " + f + ", root: " + repoRoot); //NOI18N
                                }
                                errorMessage = NbBundle.getMessage(CreateAction.class, "LBL_Create_Panel_Error_Versioned"); //NOI18N
                                break;
                            }
                        }
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
                            errorMessage = NbBundle.getMessage(CreateAction.class, "LBL_Create_Panel_Warning_Under_Project"); //NOI18N
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
                rootPath = VCSFileProxySupport.getResource(rootFiles[0], panel.tfRootPath.getText());
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
    private VCSFileProxy getSuggestedRoot () {
        if (rootFiles == null || rootFiles.length == 0) {
            return null;
        }

        final Project proj = VCSFileProxySupport.getProject(rootFiles);
        final VCSFileProxy projFile = org.netbeans.modules.mercurial.remote.versioning.util.Utils.getProjectFile(proj);

        VCSFileProxy root = null;
        root = getCommonAncestor(rootFiles);
        if (Mercurial.LOG.isLoggable(Level.FINER)) {
            Mercurial.LOG.finer("CreateAction.getSuggestedRoot: common root for " + Arrays.asList(rootFiles) + ": " + root); //NOI18N
        }

        if (projFile != null) {
            root = getCommonAncestor(root, projFile);
            if (Mercurial.LOG.isLoggable(Level.FINER)) {
                Mercurial.LOG.finer("CreateAction.getSuggestedRoot: root with project at " + projFile + ": " + root); //NOI18N
            }
        }
        return root;
    }
    
    private void notifyImportImpossible(String msg) {
        NotifyDescriptor nd =
            new NotifyDescriptor(
                msg,
                NbBundle.getMessage(CreateAction.class, "MSG_ImportNotAllowed"), // NOI18N
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(nd);
    }
}
