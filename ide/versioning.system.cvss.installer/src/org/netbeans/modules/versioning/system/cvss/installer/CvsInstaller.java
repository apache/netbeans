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
package org.netbeans.modules.versioning.system.cvss.installer;

import java.awt.Dialog;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.OperationSupport;
import org.netbeans.api.autoupdate.OperationSupport.Restarter;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.netbeans.modules.versioning.system.cvss.installer.util.Utils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author ondra
 */
@VersioningSystem.Registration(
    displayName="#CTL_CvsInstaller_DisplayName",
    menuLabel="",
    actionsCategory="CVS",
    metadataFolderNames="CVS")
public final class CvsInstaller extends VersioningSystem {
    
    public static final Logger LOG = Logger.getLogger(CvsInstaller.class.getName());
    private final Set<File> unversionedParents = Collections.synchronizedSet(new HashSet<File>(20));
    public static final String FILENAME_CVS = "CVS"; //NOI18N
    public static final String FILENAME_CVS_REPOSITORY = FILENAME_CVS + "/Repository"; // NOI18N
    public static final String FILENAME_CVS_ENTRIES = FILENAME_CVS + "/Entries"; // NOI18N
    private static final RequestProcessor RP = new RequestProcessor("CVS Installer", 1, false, false); //NOI18N
    private final RequestProcessor.Task task;
    private final AsyncTask installTask;
    private static final String CVS_CODENAME = "org.netbeans.modules.versioning.system.cvss"; //NOI18N
    private static final String INSTALLER_CODENAME = "org.netbeans.modules.versioning.system.cvss.installer"; //NOI18N
    
    public CvsInstaller () {
        installTask = new AsyncTask();
        task = RP.create(installTask);
    }
    
    @Override
    public File getTopmostManagedAncestor (File file) {
        if (CvsInstallerModuleConfig.getInstance().isCvsInstalled()) {
            // no need for this module, will be uninstalled
            task.schedule(0);
            return null;
        }
        if (CvsInstallerModuleConfig.getInstance().isIgnored()) {
            return null;
        }
        return getTopmostManagedAncestor (file, false);
    }
        
    public File getTopmostManagedAncestor (File file, boolean internal) {
        long t = System.currentTimeMillis();
        LOG.log(Level.FINE, "getTopmostManagedParent {0}", new Object[] { file });
        if(unversionedParents.contains(file)) {
            LOG.fine(" cached as unversioned");
            return null;
        }
        if (Utils.isPartOfCVSMetadata(file)) {
            LOG.fine(" part of metaddata");
            for (;file != null; file = file.getParentFile()) {
                if (file.getName().equals(FILENAME_CVS) && (file.isDirectory() || !file.exists())) {
                    file = file.getParentFile();
                    LOG.log(Level.FINE, " will use parent {0}", new Object[] { file });
                    break;
                }
            }
        }

        Set<File> done = new HashSet<File>();
        File topmost = null;
        for (; file != null; file = file.getParentFile()) {
            if(unversionedParents.contains(file)) {
                LOG.log(Level.FINE, " already known as unversioned {0}", new Object[] { file });
                break;
            }
            if (VersioningSupport.isExcluded(file)) break;
            if (Utils.containsMetadata(file)) {
                LOG.log(Level.FINE, " found managed parent {0}", new Object[] { file });
                topmost = file;
                done.clear();   // all folders added before must be removed, they ARE in fact managed by CVS
            } else {
                LOG.log(Level.FINE, " found unversioned {0}", new Object[] { file });
                if(file.exists()) { // could be created later ...
                    done.add(file);
                }
            }
        }
        if(done.size() > 0) {
            LOG.log(Level.FINE, " storing unversioned");
            unversionedParents.addAll(done);
        }
        if(LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, " getTopmostManagedParent returns {0} after {1} millis", new Object[] { topmost, System.currentTimeMillis() - t });
        }
        if (internal) {
            return topmost;
        } else {
            if (topmost != null) {
                // now we have a cvs checkout and the download dialog has not been displayed or we're running with a fresh userdir
                task.schedule(0);
            }
            return null;
        }
    }

    private boolean isCvsInstalled () {
        assert !EventQueue.isDispatchThread();
        for (UpdateUnit u : UpdateManager.getDefault().getUpdateUnits()) {
            if (CVS_CODENAME.equals(u.getCodeName())) {
                return u.getInstalled() != null;
            }
        }
        return false;
    }
    
    private static boolean downloadWindowDisplayed;
    private class AsyncTask implements Runnable {

        private PropertyChangeListener listener;

        @Override
        public void run () {
            // cvs might have been installed manually, check that
            if (CvsInstallerModuleConfig.getInstance().isCvsInstalled() || isCvsInstalled()) {
                doFinish();
                return;
            }
            if (downloadWindowDisplayed || CvsInstallerModuleConfig.getInstance().isIgnored()) {
                return;
            }
            // cvs not yet installed, attach a listener to open projects and wait for a cvs project to be opened
            OpenProjects projs = OpenProjects.getDefault();
            if (listener == null) {
                projs.addPropertyChangeListener(WeakListeners.propertyChange(listener = new PropertyChangeListener() {
                    @Override
                    public void propertyChange (PropertyChangeEvent evt) {
                        if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
                            task.schedule(0);
                        }
                    }
                }, projs));
            }
            Project[] projects;
            try {
                projects = projs.openProjects().get();
            } catch (Exception ex) {
                LOG.log(Level.INFO, null, ex);
                projects = projs.getOpenProjects();
            }
            for (Project p : projects) {
                FileObject fo = p.getProjectDirectory();
                File f = FileUtil.toFile(fo);
                // is the project directory under CVS control?
                if (f != null && getTopmostManagedAncestor(f, true) != null) {
                    processCvsProject(p);
                    break;
                }
            }
        }

        private void doFinish () {
            CvsInstallerModuleConfig.getInstance().setCvsInstalled(true);
            if (listener != null) {
                OpenProjects.getDefault().removePropertyChangeListener(listener);
                listener = null;
            }
            assert !EventQueue.isDispatchThread();
            for (UpdateUnit u : UpdateManager.getDefault().getUpdateUnits()) {
                if (INSTALLER_CODENAME.equals(u.getCodeName()) && u.getInstalled() != null && !u.isPending()) {
                    OperationContainer<OperationSupport> container = OperationContainer.createForUninstall();
                    if (container.canBeAdded(u, u.getInstalled())) {
                        container.add(u, u.getInstalled());
                        try {
                            LOG.log(Level.INFO, "doFinish: uninstalling"); //NOI18N
                            OperationSupport support = container.getSupport();
                            Restarter restarter = support.doOperation(null);
                            LOG.log(Level.INFO, "doFinish: uninstalled"); //NOI18N
                            if (restarter != null) {
                                LOG.log(Level.INFO, "doFinish: restart scheduled"); //NOI18N
                                support.doRestartLater(restarter);
                            }
                        } catch (OperationException ex) {
                            LOG.log(Level.INFO, null, ex);
                        }
                    }
                }
            }
        }

        private void processCvsProject (Project p) {
            JButton installButton = new JButton();
            Mnemonics.setLocalizedText(installButton, NbBundle.getMessage(CvsInstallDialog.class, "CvsInstallDialog.downloadButton.text")); //NOI18N
            CvsInstallDialog panel = new CvsInstallDialog();
            DialogDescriptor dd = new DialogDescriptor(panel, NbBundle.getMessage(CvsInstallDialog.class, "CvsInstallDialog.title"), //NOI18N
                    true, new Object[] { installButton, DialogDescriptor.CANCEL_OPTION }, installButton, DialogDescriptor.DEFAULT_ALIGN, null, null);
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
            dialog.setVisible(true);
            // no more dialog in the future
            downloadWindowDisplayed = true;
            if (listener != null) {
                OpenProjects.getDefault().removePropertyChangeListener(listener);
                listener = null;
            }
            // handle result
            if (panel.cbDoNotAsk.isSelected()) {
                CvsInstallerModuleConfig.getInstance().setIgnored(true);
            }
            if (dd.getValue() == installButton) {
                // install CVS support
                if (installCvsSupport()) {
                    // and uninstall me
                    doFinish();
                } else {
                    // huh? probably a connection issue? user canceled the download or did not accept the license?
                    CvsInstallerModuleConfig.getInstance().setIgnored(false);
                    DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(NbBundle.getMessage(CvsInstaller.class, "CvsInstaller.installFailed"), //NOI18N
                            NotifyDescriptor.ERROR_MESSAGE));
                }
            }
        }

        private boolean installCvsSupport () {
            assert !EventQueue.isDispatchThread();
            for (UpdateUnit u : UpdateManager.getDefault().getUpdateUnits()) {
                if (CVS_CODENAME.equals(u.getCodeName()) && u.getInstalled() == null) {
                    if (u.getAvailableUpdates().size() > 0) {
                        UpdateElement element = u.getAvailableUpdates().get(0);
                        OperationContainer<InstallSupport> container = OperationContainer.createForInstall();
                        if (container.canBeAdded(u, element)) {
                            container.add(u, element);
                            LOG.log(Level.INFO, "installCvsSupport: installing CVS"); //NOI18N
                            return PluginManager.openInstallWizard(container);
                        }
                    }
                }
            }
            return false;
        }
        
    }
}
