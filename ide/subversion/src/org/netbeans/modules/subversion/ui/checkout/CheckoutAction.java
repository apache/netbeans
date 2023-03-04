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
package org.netbeans.modules.subversion.ui.checkout;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.subversion.util.CheckoutCompleted;
import java.io.File;
import java.util.concurrent.Callable;
import org.netbeans.modules.subversion.RepositoryFile;
import org.netbeans.modules.subversion.Subversion;
import org.netbeans.modules.subversion.SvnModuleConfig;
import org.netbeans.modules.subversion.client.SvnProgressSupport;
import org.netbeans.modules.subversion.client.SvnClient;
import org.netbeans.modules.subversion.client.SvnClientExceptionHandler;
import org.netbeans.modules.subversion.ui.wizards.*;
import org.netbeans.modules.subversion.util.SvnUtils;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNUrl;

/**
 *
 * @author Tomas Stupka
 */
@ActionID(id = "org.netbeans.modules.subversion.ui.checkout.CheckoutAction", category = "Subversion")
@ActionRegistration(displayName = "#LBL_CheckoutAction_Name")
@ActionReferences({
   @ActionReference(path="Versioning/Subversion/Actions/Global", position=300 /*, separatorAfter=350*/)
})
public final class CheckoutAction implements ActionListener, HelpCtx.Provider {
           
    private static final String WORKING_COPY_FORMAT_PROP = "svnkit.wc.17"; //NOI18N

    public CheckoutAction() {
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.subversion.ui.checkout.CheckoutAction");
    }
          
    @Override
    public void actionPerformed(ActionEvent ae) {
        if(!Subversion.getInstance().checkClientAvailable()) {            
            return;
        }
        
        Utils.logVCSActionEvent("SVN");
        performCheckout(false);
    }
        
    public static File performCheckout (final boolean wait) {
        assert !wait || !EventQueue.isDispatchThread(); // cannot wait in AWT
        CheckoutWizard wizard = new CheckoutWizard();
        if (!wizard.show()) {
            return null;
        
        }
        final SVNUrl repository = wizard.getRepositoryRoot();
        final RepositoryFile[] repositoryFiles = wizard.getRepositoryFiles();
        final File workDir = wizard.getWorkdir();
        final boolean atWorkingDirLevel = wizard.isAtWorkingDirLevel();
        final boolean doExport = wizard.isExport();
        final boolean showCheckoutCompleted = SvnModuleConfig.getDefault().getShowCheckoutCompleted();
        final boolean old16Format = wizard.isOldFormatPreferred();
        Runnable run = new Runnable() {
            @Override
            public void run() {
                final String oldPreference = System.getProperty(WORKING_COPY_FORMAT_PROP);
                System.setProperty(WORKING_COPY_FORMAT_PROP, Boolean.toString(!old16Format));
                SvnClient client;
                try {
                    // this needs to be done in a background thread, otherwise the password won't be acquired from the keyring
                    client = Subversion.getInstance().getClient(repository);
                } catch (SVNClientException ex) {
                    SvnClientExceptionHandler.notifyException(ex, true, true); // should not happen
                    return;
                }
                Task t = performCheckout(repository, client, repositoryFiles, workDir, atWorkingDirLevel, doExport, showCheckoutCompleted);
                t.addTaskListener(new TaskListener() {
                    @Override
                    public void taskFinished (Task task) {
                        if (oldPreference == null) {
                            System.clearProperty(WORKING_COPY_FORMAT_PROP);
                        } else {
                            System.setProperty(WORKING_COPY_FORMAT_PROP, oldPreference);
                        }
                    }
                });
                if (wait) {
                    t.waitFinished();
                }
            }
        };
        if (wait) {
            run.run();
        } else {
            Subversion.getInstance().getRequestProcessor().post(run);
        }
        return wizard.getWorkdir();
    }
    
    public static RequestProcessor.Task performCheckout(
        final SVNUrl repository,
        final SvnClient client,
        final RepositoryFile[] repositoryFiles,
        final File workingDir,
        final boolean atWorkingDirLevel,
        final boolean doExport,
        final boolean showCheckoutCompleted)
    {
        SvnProgressSupport support = new SvnProgressSupport() {
            @Override
            public void perform() {
                try {
                    setDisplayName(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/checkout/Bundle").getString("LBL_Checkout_Progress"));
                    setCancellableDelegate(client);
                    client.addNotifyListener(this);
                    checkout(client, repository, repositoryFiles, workingDir, atWorkingDirLevel, doExport, this);
                } catch (SVNClientException ex) {
                    annotate(ex);
                    return;
                } finally {
                    Subversion.getInstance().versionedFilesChanged();
                    client.removeNotifyListener(this);
                }
                if(isCanceled()) {
                    return;
                }
                setDisplayName(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/checkout/Bundle").getString("LBL_ScanFolders_Progress"));
                if(showCheckoutCompleted) showCheckoutCompletet(repositoryFiles, workingDir, atWorkingDirLevel, doExport, this);
            }
        };
        return support.start(Subversion.getInstance().getRequestProcessor(repository), repository, java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/checkout/Bundle").getString("LBL_Checkout_Progress"));
    }

    public static void checkout(final SvnClient client,
                                final SVNUrl repository,
                                final RepositoryFile[] repositoryFiles,
                                final File workingDir,
                                final boolean atWorkingDirLevel,
                                final boolean doExport,
                                final SvnProgressSupport support)
    throws SVNClientException
    {
        final File[] destinations = new File[repositoryFiles.length];
        for (int i = 0; i < repositoryFiles.length; i++) {
            File destination;
            if(!atWorkingDirLevel) {
                destination = new File(workingDir.getAbsolutePath() +
                                       "/" +  // NOI18N
                                       repositoryFiles[i].getName()); // XXX what if the whole repository is seletcted
                destination = FileUtil.normalizeFile(destination);
                destination.mkdir();
            } else {
                destination = workingDir;
            }
            destinations[i] = destination;
        }
        SvnUtils.runWithoutIndexing(new Callable<Void>() {
            @Override
            public Void call () throws Exception {
                for (int i = 0; i < repositoryFiles.length; i++) {
                    File destination = destinations[i];
                    if(support!=null && support.isCanceled()) { 
                        return null;
                    }
                    if(doExport) {
                        client.doExport(repositoryFiles[i].getFileUrl(), destination, repositoryFiles[i].getRevision(), true);
                    } else {
                        client.checkout(repositoryFiles[i].getFileUrl(), destination, repositoryFiles[i].getRevision(), true);
                    }
                }
                return null;
            }
        }, destinations);
    }

    private static void showCheckoutCompletet(
        final RepositoryFile[] repositoryFiles,
        final File workingDir,
        final boolean atWorkingDirLevel,
        final boolean doExport,
        final SvnProgressSupport support)
    {
        String[] folders;
        if (atWorkingDirLevel) {
            folders = new String[1];
            folders[0] = "."; // NOI18N
        } else {
            folders = new String[repositoryFiles.length];
            for (int i = 0; i < repositoryFiles.length; i++) {
                if (support != null && support.isCanceled()) {
                    return;
                }
                if (repositoryFiles[i].isRepositoryRoot()) {
                    folders[i] = "."; // NOI18N
                } else {
                    folders[i] = repositoryFiles[i].getFileUrl().getLastPathSegment();
                }
            }
        }
        CheckoutCompleted cc = new CheckoutCompleted(workingDir, folders);
        if (support != null && support.isCanceled()) {
            return;
        }
        cc.scanForProjects(support, doExport ? CheckoutCompleted.Type.EXPORT : CheckoutCompleted.Type.CHECKOUT);
        return;
    }

}
