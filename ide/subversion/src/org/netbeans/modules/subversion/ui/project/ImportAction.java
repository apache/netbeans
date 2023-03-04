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

package org.netbeans.modules.subversion.ui.project;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.subversion.Subversion;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.netbeans.api.project.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.subversion.FileInformation;
import org.netbeans.modules.subversion.FileStatusCache;
import org.netbeans.modules.subversion.ui.wizards.ImportWizard;
import org.netbeans.modules.subversion.util.Context;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Kuzel
 */
@ActionID(id = "org.netbeans.modules.subversion.ui.project.ImportAction", category = "Subversion")
@ActionRegistration(displayName = "#BK0006", popupText="#CTL_PopupMenuItem_Import", menuText="#BK0006")
@ActionReferences({
   @ActionReference(path="Versioning/Subversion/Actions/Unversioned", position=1)
})
public final class ImportAction implements ActionListener, HelpCtx.Provider {
    
    private static final Logger LOG = Logger.getLogger(ImportAction.class.getName());
    private final List<File> roots;

    public ImportAction (List<File> rootFiles) {
        this.roots = rootFiles;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.subversion.ui.project.ImportAction");
    }
    
    private boolean isEnabled() {
        
        if (roots.size() == 1) {
            if(!isCacheReady()) {
                LOG.log(Level.FINE, "Cache not ready yet"); //NOI18N
                return false;
            }
            FileStatusCache cache = Subversion.getInstance().getStatusCache();
            File dir = lookupImportDirectory(roots.iterator().next());
            if (dir != null && dir.isDirectory()) {
                FileInformation status = cache.getCachedStatus(dir);
                // mutually exclusive enablement logic with commit
                if (!SvnUtils.isManaged(dir) && (status == null || (status.getStatus() & FileInformation.STATUS_MANAGED) == 0)) {
                    // do not allow to import partial/nonatomic project, all must lie under imported common root
                    FileObject fo = FileUtil.toFileObject(dir);
                    Project p = FileOwnerQuery.getOwner(fo);
                    if (p == null) {
                        return true;
                    }
                    FileObject projectDir = p.getProjectDirectory();
                    boolean b = FileUtil.isParentOf(projectDir, fo) == false;
                    if(!b) {
                        notifyImportImpossible(NbBundle.getMessage(ImportAction.class, "MSG_NoSubproject"));            
                    } 
                    return b;
                } else {
                    LOG.log(Level.FINE, "Already versioned: {0} - {1}", new Object[] { dir, status }); //NOI18N
                }
            } else {
                LOG.log(Level.FINE, "Root not folder: {0}", dir); //NOI18N
            }
        } else if (roots.isEmpty()) {
            notifyImportImpossible(NbBundle.getMessage(ImportAction.class, "MSG_EmptySelection")); //NOI18N
        } else {
            notifyImportImpossible(NbBundle.getMessage(ImportAction.class, "MSG_TooManyRoots"));            
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        performAction();
    }

    protected void performAction() {
        
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }

        if(!isEnabled()) {
            return;
        }
          
        Utils.logVCSActionEvent("SVN");                                 

        assert roots.size() == 1; // ensured through isEnabled
        
        if (roots.size() == 1) {
            final File importDirectory = lookupImportDirectory(roots.iterator().next());
            if (importDirectory == null) {
                LOG.log(Level.FINE, "null dir to import: {0}", roots.iterator().next()); //NOI18N
            } else {
                LOG.log(Level.FINE, "Starting wizard: {0}", roots.iterator().next()); //NOI18N
                List<File> list = new ArrayList<File>(1);
                list.add(importDirectory);
                Context context = new Context(Context.getEmptyList(), list, Context.getEmptyList());
                ImportWizard wizard = new ImportWizard(context);
                wizard.show(); // wizard starts all neccessary task (import, commit)
            }
        } else {
            LOG.warning("too many roots in selection.");             // NOI18N
        }
    }

    private File lookupImportDirectory(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        Project project = null;
        if (fo.isFolder()) {
            try {
                project = ProjectManager.getDefault().findProject(fo);
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
            } catch (IllegalArgumentException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
        }
    
        File importDirectory = null;
        if (project != null) {
            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            if (groups.length == 1) {
                FileObject root = groups[0].getRootFolder();
                importDirectory = FileUtil.toFile(root);
            } else {
                importDirectory = FileUtil.toFile(project.getProjectDirectory());
            }
        } else if (file.isDirectory()) {
            importDirectory = file;
        }
        return importDirectory;
    }
    
    private void notifyImportImpossible(String msg) {
        LOG.log(Level.FINE, "Import impossible: {0}", msg);
        NotifyDescriptor nd =
            new NotifyDescriptor(
                msg,
                NbBundle.getMessage(ImportAction.class, "MSG_ImportNotAllowed"), // NOI18N
                NotifyDescriptor.DEFAULT_OPTION,
                NotifyDescriptor.WARNING_MESSAGE,
                new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(nd);
    }

    private boolean isCacheReady() {
        final DialogDescriptor dd = 
                new DialogDescriptor(
                        NbBundle.getMessage(ImportAction.class,"MSG_CacheNotReady"), // NOI18N
                        NbBundle.getMessage(ImportAction.class,"MSG_InitRunning"),   // NOI18N
                        true, 
                        new Object[]{
                            NbBundle.getMessage(ImportAction.class,"LBL_CancelAction")}, // NOI18N
                            null, 0, null, null);
        final Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        final FileStatusCache cache = Subversion.getInstance().getStatusCache();
        if(!cache.ready()) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    while(!cache.ready()) {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException ex) {
                            break;
                        }
                    }
                    dialog.setVisible(false);
                }
            });
            dialog.setVisible(!cache.ready());
            return cache.ready();
        }
        return true;
    }
}
