/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
                    J2eeModuleProvider jmp = (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
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
        J2eeModuleProvider jmp =
                (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
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
        J2eeModuleProvider jmp =
                (J2eeModuleProvider)project.getLookup().lookup(J2eeModuleProvider.class);
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
