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

package org.netbeans.modules.ide.ergonomics.fod;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.autoupdate.ui.api.PluginManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Jirka Rechtacek
 */
public class ModulesInstaller {
    
    private Collection<UpdateElement> modules4install;
    private RequestProcessor.Task installTask = null;
    private OperationContainer<InstallSupport> installContainer;
    private ProgressHandle downloadHandle;
    private ProgressHandle verifyHandle;
    private ProgressHandle installHandle;
    private final FindComponentModules finder;
    private final ProgressMonitor progressMonitor;
    
    public ModulesInstaller (Collection<UpdateElement> modules, FindComponentModules find) {
        this(modules, find, null);
    }
    
    public ModulesInstaller (Collection<UpdateElement> modules, FindComponentModules find, ProgressMonitor progressMonitor) {
        if (modules == null || modules.isEmpty ()) {
            throw new IllegalArgumentException ("Cannot construct InstallerMissingModules with null or empty Collection " + modules);
        }
        modules4install = modules;
        finder = find;
        if (progressMonitor != null) {
            this.progressMonitor = progressMonitor;
        } else {
            this.progressMonitor = ProgressMonitor.DEV_NULL_PROGRESS_MONITOR;
        }
    }
    
    static boolean success = false;
    public static boolean installModules (
        ProgressMonitor monitor, FeatureInfo info, Collection<UpdateElement> alreadyOffered, Set<FeatureInfo.ExtraModuleInfo> filter
    ) {
        assert ! SwingUtilities.isEventDispatchThread () : "Cannot run in EQ!";
        
        FindComponentModules findModules = new FindComponentModules(info, filter);
        Collection<UpdateElement> toInstall = findModules.getModulesForInstall();
        toInstall.removeAll(alreadyOffered);
        Collection<UpdateElement> toEnable = findModules.getModulesForEnable();
        if (toInstall != null && !toInstall.isEmpty()) {
            ModulesInstaller installer = new ModulesInstaller(toInstall, findModules, monitor);
            installer.getInstallTask ().schedule (10);
            installer.getInstallTask ().waitFinished();
            findModules = new FindComponentModules(info, filter);
            success = findModules.getModulesForInstall ().isEmpty ();
        } else if (toEnable != null && !toEnable.isEmpty()) {
            ModulesActivator enabler = new ModulesActivator(toEnable, findModules, monitor);
            enabler.getEnableTask ().schedule (100);
            enabler.getEnableTask ().waitFinished();
            success = true;
        }
        
        if (success) {
            FoDLayersProvider.getInstance().refreshForce();
        }
        
        return success;
    }

    public void assignDownloadHandle (ProgressHandle handle) {
        this.downloadHandle = handle;
    }
    
    public void assignVerifyHandle (ProgressHandle handle) {
        this.verifyHandle = handle;
    }
    
    public void assignInstallHandle (ProgressHandle handle) {
        this.installHandle = handle;
    }
    
    public RequestProcessor.Task getInstallTask () {
        if (installTask == null) {
            installTask = createInstallTask ();
        }
        return installTask;
    }
    
    private RequestProcessor.Task createInstallTask () {
        assert installTask == null || installTask.isFinished () : "The Install Task cannot be started nor scheduled.";
        installTask = FeatureManager.getInstance().create(new InstallOrActivateTask(this));
        return installTask;
    }
    
    final void installMissingModules () {
        try {
            doInstallMissingModules ();
        } catch (Exception x) {
            JButton tryAgain = new JButton ();
            tryAgain.addActionListener(new ActionListener () {
                public void actionPerformed (ActionEvent e) {
                    if (installContainer != null) {
                        try {
                            installContainer.getSupport ().doCancel ();
                        } catch (Exception ex) {
                            Logger.getLogger (ModulesInstaller.class.getName ()).
                                    log (Level.INFO, ex.getLocalizedMessage (), ex);
                        }
                    }
                    RequestProcessor.Task task = getInstallTask ();
                    if (task != null) {
                        task.schedule (10);
                    }
                }
            });
            Logger.getLogger (ModulesInstaller.class.getName ()).log (Level.INFO, x.getLocalizedMessage (), x);
            tryAgain.setEnabled (getInstallTask () != null);
            Mnemonics.setLocalizedText (tryAgain, getBundle ("InstallerMissingModules_TryAgainButton"));
            NotifyDescriptor nd = new NotifyDescriptor (
                    getErrorNotifyPanel (x),
                    getBundle ("InstallerMissingModules_ErrorPanel_Title"),
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.ERROR_MESSAGE,
                    new Object [] { tryAgain, NotifyDescriptor.OK_OPTION },
                    NotifyDescriptor.OK_OPTION
                    );
            DialogDisplayer.getDefault ().notifyLater (nd);
        } finally {
            FoDLayersProvider.getInstance().refreshForce();
        }
    }
    
    private JComponent getErrorNotifyPanel (Exception x) {
        JTextArea area = new JTextArea ();
        area.setWrapStyleWord (true);
        area.setLineWrap (true);
        area.setEditable (false);
        area.setRows (15);
        area.setColumns (40);
        area.setOpaque (false);
        area.setText (getBundle ("InstallerMissingModules_ErrorPanel", x.getLocalizedMessage (), x));
        return area;
    }

    private void doInstallMissingModules () throws OperationException {
        assert ! SwingUtilities.isEventDispatchThread () : "Cannot be called in EQ.";
        installContainer = null;
        for (UpdateElement module : modules4install) {
            if (installContainer == null) {
                boolean isNewOne = module.getUpdateUnit ().getInstalled () == null;
                if (isNewOne) {
                    installContainer = OperationContainer.createForInstall ();
                } else {
                    installContainer = OperationContainer.createForUpdate ();
                }
            }
            if (installContainer.canBeAdded (module.getUpdateUnit (), module)) {
                installContainer.add (module);
            }
        }
        if (installContainer.listAll ().isEmpty ()) {
            return ;
        }
        assert installContainer.listInvalid ().isEmpty () :
            "No invalid Update Elements " + installContainer.listInvalid ();
        if (! installContainer.listInvalid ().isEmpty ()) {
            throw new IllegalArgumentException ("Some are invalid for install: " + installContainer.listInvalid ());
        }
        boolean ok = PluginManager.openInstallWizard(installContainer);
        if (!ok) {
            StringBuilder sb = new StringBuilder();
            String sep = "";
            for (UpdateElement el : modules4install) {
                sb.append(sep);
                sb.append(el.getDisplayName());
                sep = ", ";
            }
            progressMonitor.onError(
                getBundle("InstallerMissingModules_Cancelled", sb) // NOI18N
            );
        }
    }
    
    public static String presentUpdateElements (Collection<UpdateElement> elems) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        Set<String> used = new HashSet<String>();
        
        for (UpdateElement el : elems) {
            if (!used.add(el.getCategory())) {
                continue;
            }
            sb.append(sep);
            sb.append(el.getCategory());
            if (sb.length() > 30) {
                sb.append("..."); // NOI18N
                break;
            }
            sep = ", "; // NOI18N
        }
        return sb.toString();
    }

    private void waitToModuleLoaded () {
        assert ! SwingUtilities.isEventDispatchThread () : "Cannot be called in EQ.";
        for (UpdateElement m : modules4install) {
           while (!m.isEnabled()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
           }
        }
        
    }
    
    private static String getBundle (String key, Object... params) {
        return NbBundle.getMessage (ModulesInstaller.class, key, params);
    }
    
}
