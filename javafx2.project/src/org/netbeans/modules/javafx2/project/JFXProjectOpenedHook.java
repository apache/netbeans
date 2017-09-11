/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.project;

import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.SwingUtilities;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.spi.project.ProjectConfigurationProvider;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.awt.Notification;
import org.openide.awt.NotificationDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.windows.WindowManager;

/**
 *
 * @author Tomas Zezula
 * @author Petr Somol
 * @author Anton Chechel
 */
@ProjectServiceProvider(service=ProjectOpenedHook.class, projectType={"org-netbeans-modules-java-j2seproject"}) // NOI18N
public final class JFXProjectOpenedHook extends ProjectOpenedHook {

    private static final Logger LOGGER = Logger.getLogger("javafx"); // NOI18N
    private final Project prj;
    private final J2SEPropertyEvaluator eval;
    private ConfigChangeListener chl = null;
    private static final Map<String, List<Notification>> projectNotifications = new HashMap<String, List<Notification>>();

    public JFXProjectOpenedHook(final Lookup lkp) {
        Parameters.notNull("lkp", lkp); //NOI18N
        this.prj = lkp.lookup(Project.class);
        Parameters.notNull("prj", prj); //NOI18N
        this.eval = lkp.lookup(J2SEPropertyEvaluator.class);
        Parameters.notNull("eval", eval);   //NOI18N
    }

    @Override
    protected synchronized void projectOpened() {
        if(!isFXProject(eval)) {
            final JFXPlatformUpdater updater = prj.getLookup().lookup(JFXPlatformUpdater.class);            
            // replace Default_JavaFX_Platform by default Java Platform (since NB7.4)
            final Runnable runUpdateJFXPlatform = updater != null ? new Runnable() {
                @Override
                public void run() {
                    updater.updateFXPlatform();
                }
            } : null;
            if(runUpdateJFXPlatform != null) {
                switchBusy();
                runUpdateJFXPlatform.run();
                switchDefault();
            }
        } else {
            JFXProjectGenerator.logUsage(JFXProjectGenerator.Action.OPEN);

            //hotfix for Bug 214819 - Completion list is corrupted after IDE upgrade 
            //http://netbeans.org/bugzilla/show_bug.cgi?id=214819
            Preferences prefs = ProjectUtils.getPreferences(prj, Project.class, false);
            prefs.put("issue214819_fx_enabled", "true"); //NOI18N

            ProjectConfigurationProvider<?> pcp = prj.getLookup().lookup(ProjectConfigurationProvider.class);
            assert pcp != null;
            LOGGER.log(Level.INFO, "FX PCP: " + pcp.toString());
            chl = new ConfigChangeListener(prj);
            pcp.addPropertyChangeListener(chl);

            final JFXPlatformUpdater updater = prj.getLookup().lookup(JFXPlatformUpdater.class);
            
            // replace Default_JavaFX_Platform by default Java Platform (since NB7.4)
            final Runnable runUpdateJFXPlatform = updater != null ? new Runnable() {
                @Override
                public void run() {
                    updater.updateFXPlatform();
                }
            } : null;
            
            // and update FX build script file jfx-impl.xml if it is not in expected state
            // #204765
            // and create Default RunAs Configurations
            // #204760
            final Runnable runUpdateJFXImpl;
            runUpdateJFXImpl = isEnabledJFXUpdate() ? new Runnable() {
                 @Override
                 public void run() {
                     updateDefaultConfigs();
                     FileObject readmeFO = updateJfxImpl();
                     if(readmeFO != null && isEnabledJFXUpdateNotification()) {
                         final String headerTemplate = NbBundle.getMessage(JFXProjectUtils.class, "TXT_UPDATED_README_FILE_CONTENT_HEADER"); //NOI18N
                         final String header = MessageFormat.format(headerTemplate, new Object[] {ProjectUtils.getInformation(prj).getDisplayName()});
                         final String content = NbBundle.getMessage(JFXProjectUtils.class, "TXT_UPDATED_NOTIFICATION_CONTENT"); //NOI18N
                         Notification noteUpdate = NotificationDisplayer.getDefault().notify(
                                 header, 
                                 ImageUtilities.loadImageIcon("org/netbeans/modules/javafx2/project/ui/resources/jfx_project.png", true), //NOI18N
                                 content, 
                                 null, 
                                 NotificationDisplayer.Priority.LOW, 
                                 NotificationDisplayer.Category.INFO);
                         addNotification(prj, noteUpdate);
                     }
                 }
            } : null;

            if(runUpdateJFXPlatform != null || runUpdateJFXImpl != null) {
                switchBusy();
                if(runUpdateJFXPlatform != null) {
                    runUpdateJFXPlatform.run();
                }
                if(runUpdateJFXImpl != null) {
                    runUpdateJFXImpl.run();
                }
                switchDefault();
           }

        }
    }
    
    public static void addNotification(@NonNull final Project project, @NonNull final Notification notification) {
        synchronized(projectNotifications) {
            final String path = project.getProjectDirectory().getPath();
            List<Notification> notifications = projectNotifications.get(path);
            if(notifications == null) {
                notifications = new ArrayList<Notification>();
                projectNotifications.put(path, notifications);
            }
            notifications.add(notification);
        }
    }

    @Override
    protected void projectClosed() {
        if(isFXProject(eval)) {
            JFXProjectGenerator.logUsage(JFXProjectGenerator.Action.CLOSE);
            if(chl != null) {
                ProjectConfigurationProvider<?> pcp = prj.getLookup().lookup(ProjectConfigurationProvider.class);
                assert pcp != null;
                pcp.removePropertyChangeListener(chl);
                chl = null;
            }
        }
        final String path = prj.getProjectDirectory().getPath();
        List<Notification> notifications;
        synchronized(projectNotifications) {
            notifications = projectNotifications.get(path);
            projectNotifications.remove(path);
        }
        if(notifications != null) {
            for(Notification n : notifications) {
                n.clear();
            }
        }
        final JFXPlatformUpdater updater = prj.getLookup().lookup(JFXPlatformUpdater.class);
        if(updater != null) {
            updater.resetUpdated();
        }
    }

    private boolean isEnabledJFXUpdate() {
        final PropertyEvaluator evaluator = eval.evaluator();
        if(evaluator != null) {
            return !JFXProjectProperties.isTrue(evaluator.getProperty(JFXProjectProperties.JAVAFX_DISABLE_AUTOUPDATE));
        } else {
            LOGGER.log(Level.WARNING, "PropertyEvaluator instantiation failed, disabling jfx-impl.xml auto-update."); // NOI18N
        }
        return false;
    }

    private boolean isEnabledJFXUpdateNotification() {
        final PropertyEvaluator evaluator = eval.evaluator();
        if(evaluator != null) {
            return !JFXProjectProperties.isTrue(evaluator.getProperty(JFXProjectProperties.JAVAFX_DISABLE_AUTOUPDATE_NOTIFICATION));
        } else {
            LOGGER.log(Level.WARNING, "PropertyEvaluator instantiation failed, disabling jfx-impl.xml auto-update notification."); // NOI18N
        }
        return false;
    }

    private FileObject updateJfxImpl() {
        // this operation must be finished before any user
        // action on this project involving Run, Build, Debug, etc.
        FileObject readmeFO = null;
        try {
            final AntBuildExtender extender = prj.getLookup().lookup(AntBuildExtender.class);
            if (extender == null) {
                LOGGER.log(
                    Level.WARNING,
                    "The project {0} ({1}) does not support AntBuildExtender.",     //NOI18N
                    new Object[] {
                        ProjectUtils.getInformation(prj).getDisplayName(),
                        FileUtil.getFileDisplayName(prj.getProjectDirectory())
                    });
            } else {
                // update jfx-impl.xml on project open only if build-impl extension version is compatible
                if (extender.getExtension(JFXProjectUtils.getCurrentExtensionName()) != null) {
                    readmeFO = JFXProjectUtils.updateJfxImpl(prj);
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Can't update JavaFX specific build script jfx-impl.xml: {0}", ex); // NOI18N
        }
        return readmeFO;
    }
    
    private boolean updateDefaultConfigs() {
        // this operation must be finished before any user
        // action on this project involving Run, Build, Debug, etc.
        boolean updated = false;
        final PropertyEvaluator evaluator = eval.evaluator();
        if(evaluator != null) {
            try {
                updated |= JFXProjectUtils.updateDefaultRunAsConfigFile(prj.getProjectDirectory(), JFXProjectProperties.RunAsType.ASWEBSTART, false);
                updated |= JFXProjectUtils.updateDefaultRunAsConfigFile(prj.getProjectDirectory(), JFXProjectProperties.RunAsType.INBROWSER, 
                        !JFXProjectProperties.isNonEmpty(evaluator.getProperty(JFXProjectProperties.RUN_IN_BROWSER)) ||
                        !JFXProjectProperties.isNonEmpty(evaluator.getProperty(JFXProjectProperties.RUN_IN_BROWSER_PATH)));
            } catch (Exception ex) {
                LOGGER.log(Level.WARNING, "Can't update JavaFX specific RunAs configuration files: {0}", ex); // NOI18N
            }
        } else {
            LOGGER.log(Level.WARNING, "PropertyEvaluator instantiation failed, disabling jfx-impl.xml auto-update."); // NOI18N
        }
        return updated;
    }

    private void switchBusy() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                WindowManager.getDefault().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
        });
    }

    private void switchDefault() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                WindowManager.getDefault().getMainWindow().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });
    }

    private static final class ConfigChangeListener implements PropertyChangeListener {
        private final Project prj;
        public ConfigChangeListener(Project p) {
            this.prj = p;
        }
        @Override public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getPropertyName().equals(ProjectConfigurationProvider.PROP_CONFIGURATION_ACTIVE)) {
                LOGGER.log(Level.FINE, "FX config change: " + evt.toString()); // NOI18N
                final Lookup look = prj.getLookup();
                JFXProjectProperties props = JFXProjectProperties.getInstanceIfExists(look);
                if(props == null || props.hasPreloaderInAnyConfig()) {
                    boolean propsDidNotExist = props == null;
                    JFXProjectProperties.cleanup(look);
                    props = JFXProjectProperties.getInstance(look);
                    props.updatePreloaderDependencies();
                    if(propsDidNotExist) {
                        JFXProjectProperties.cleanup(look);
                    }
                }
            }
        }
    }

    private static boolean isFXProject(@NonNull final J2SEPropertyEvaluator eval) {
        if (eval == null) {
            return false;
        }
        //Don't use JFXProjectProperties.isTrue to prevent JFXProjectProperties from being loaded
        //JFXProjectProperties.JAVAFX_ENABLED is inlined by compliler
        return isTrue(eval.evaluator().getProperty(JFXProjectProperties.JAVAFX_ENABLED));
    }

    private static boolean isTrue(@NullAllowed final String value) {
        return  value != null && (
           "true".equalsIgnoreCase(value) ||    //NOI18N
           "yes".equalsIgnoreCase(value) ||     //NOI18N
           "on".equalsIgnoreCase(value));       //NOI18N
    }
}
