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

package org.netbeans.modules.j2ee.common.ui;

import java.awt.Dialog;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.ServerLibraryDependency;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Support for managing broken/missing server libraries.
 *
 * PLEASE NOTE! This is just a temporary solution. BrokenReferencesSupport from
 * the java project support currently does not allow to plug in a check for missing
 * servers. Once BrokenReferencesSupport will support it, this class should be 
 * removed.
 */
public final class BrokenServerLibrarySupport {

    public static String OFFER_LIBRARY_DEPLOYMENT = "offerLibraryDeployment";

    private static final Logger LOGGER = Logger.getLogger(BrokenServerLibrarySupport.class.getName());

    /** Last time in ms when the Broken References alert was shown. */
    private static long brokenAlertLastTime = 0;
    
    /** Is Broken References alert shown now? */
    private static boolean brokenAlertShown = false;

    /** Timeout within which request to show alert will be ignored. */
    private static int BROKEN_ALERT_TIMEOUT = 1000;
    
    private BrokenServerLibrarySupport() {}

    public static boolean isBroken(Project project) {
        return !getMissingServerLibraries(project).isEmpty() || !getDeployableServerLibraries(project).isEmpty();
    }

    public static void fixServerLibraries(final Project project, final Runnable post) {
        if (!getMissingServerLibraries(project).isEmpty()) {
            // FIXME
            LOGGER.log(Level.WARNING, "Missing libraries - FIX THIS USECASE");
            return;
        }

        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    fixServerLibraries(project, post);
                }
            });
            return;
        }

        String title = NbBundle.getMessage(BrokenServerLibrarySupport.class, "LBL_Resolve_Broken_Server_Library_Title");
        String msg = NbBundle.getMessage(BrokenServerLibrarySupport.class, "MSG_Resolve_Broken_Server_Library_Fix_Message");

        NotifyDescriptor d = new NotifyDescriptor.Confirmation(msg, title, NotifyDescriptor.YES_NO_OPTION);
        if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.YES_OPTION) {
            RequestProcessor.getDefault().post(new Runnable(){

                @Override
                public void run() {
                    J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
                    String serverInstanceID = jmp.getServerInstanceID();
                    ServerInstance inst = Deployment.getDefault().getServerInstance(serverInstanceID);
                    try {
                        ServerInstance.LibraryManager manager = inst.getLibraryManager();
                        if (manager != null) {
                            try {
                                manager.deployLibraries(getDeployableServerLibraries(project));
                            } catch (ConfigurationException ex) {
                                // just log it for now server log will report something
                                LOGGER.log(Level.INFO, null, ex);
                            }
                        }
                    } catch(InstanceRemovedException ex) {
                        LOGGER.log(Level.FINE, null, ex);
                    } finally {
                        if (post != null) {
                            post.run();
                        }
                    }
                }

            });
        }
    }

    public static Set<ServerLibraryDependency> getMissingServerLibraries(Project project) {
        J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
        Set<ServerLibraryDependency> deps = getDependencies(jmp);

        String serverInstanceID = jmp.getServerInstanceID();
        if (serverInstanceID != null) {
            ServerInstance inst = Deployment.getDefault().getServerInstance(serverInstanceID);
            try {
                ServerInstance.LibraryManager manager = inst.getLibraryManager();
                if (manager != null) {
                    return manager.getMissingDependencies(deps);
                }
            } catch(InstanceRemovedException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }
        return Collections.emptySet();
    }

    public static Set<ServerLibraryDependency> getDeployableServerLibraries(Project project) {
        J2eeModuleProvider jmp = project.getLookup().lookup(J2eeModuleProvider.class);
        Set<ServerLibraryDependency> deps = getDependencies(jmp);

        String serverInstanceID = jmp.getServerInstanceID();
        if (serverInstanceID != null) {
            ServerInstance inst = Deployment.getDefault().getServerInstance(serverInstanceID);
            try {
            ServerInstance.LibraryManager manager = inst.getLibraryManager();
                if (manager != null) {
                    return manager.getDeployableDependencies(deps);
                }
            } catch(InstanceRemovedException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }
        return Collections.emptySet();
    }

    private static Set<ServerLibraryDependency> getDependencies(J2eeModuleProvider jmp) {
        Set<ServerLibraryDependency> deps = null;
        try {
            return jmp.getConfigSupport().getLibraries();
        } catch (ConfigurationException e) {
            return Collections.emptySet();
        }
    }

    public static synchronized void fixOrShowAlert(Project project, Runnable postFix) {
        Preferences prefs = ProjectUtils.getPreferences(project, ProjectUtils.class, true);
        boolean offerLibraryDeployment = prefs.getBoolean(
                BrokenServerLibrarySupport.OFFER_LIBRARY_DEPLOYMENT, false);
        if (offerLibraryDeployment) {
            prefs.remove(BrokenServerLibrarySupport.OFFER_LIBRARY_DEPLOYMENT);
            fixServerLibraries(project, postFix);
        } else {
            showAlert();
        }
    }

    /**
     * Show alert message box informing user that a project has missing
     * server. This method can be safely called from any thread, e.g. during
     * the project opening, and it will take care about showing message box only
     * once for several subsequent calls during a timeout.
     * The alert box has also "show this warning again" check box.
     */
    private static void showAlert() {
        // Do not show alert if it is already shown or if it was shown
        // in last BROKEN_ALERT_TIMEOUT milliseconds or if user do not wish it.
        if (brokenAlertShown
            || brokenAlertLastTime+BROKEN_ALERT_TIMEOUT > System.currentTimeMillis() 
            || !J2EEUISettings.getDefault().isShowAgainBrokenServerLibsAlert()) {
                return;
        }
        brokenAlertShown = true;
        SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        BrokenServerLibraryAlertPanel alert = new BrokenServerLibraryAlertPanel();
                        JButton close = new JButton(
                                NbBundle.getMessage(BrokenServerLibrarySupport.class, "LBL_BrokenServerLibrariesCustomizer_Close"));
                        close.getAccessibleContext().setAccessibleDescription(
                                NbBundle.getMessage(BrokenServerLibrarySupport.class, "ACSD_BrokenServerLibrariesCustomizer_Close"));
                        DialogDescriptor dd = new DialogDescriptor(
                                alert,
                                NbBundle.getMessage(BrokenServerAlertPanel.class, "MSG_Broken_Server_Libraries_Title"),
                                true,
                                new Object[] {close},
                                close,
                                DialogDescriptor.DEFAULT_ALIGN,
                                null,
                                null);
                        dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);
                        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
                        dlg.setVisible(true);
                    } finally {
                        synchronized (BrokenServerLibrarySupport.class) {
                            brokenAlertLastTime = System.currentTimeMillis();
                            brokenAlertShown = false;
                        }
                    }
                }
            });
    }
}
