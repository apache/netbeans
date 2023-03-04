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

package org.netbeans.modules.javaee.project.api.ant;

import java.awt.Image;
import java.net.URL;
import java.text.MessageFormat;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Petr Hejl
 * @since 1.28
 */
public final class DeployOnSaveUtils {

    private static final String COS_MARK = ".netbeans_automatic_build"; // NOI18N

    private static final String DEPLOY_ON_SAVE_DISABLED_BADGE_PATH = "org/netbeans/modules/javaee/project/ui/resources/compileOnSaveDisabledBadge.gif"; // NOI18N

    private static final Image DEPLOY_ON_SAVE_DISABLED_BADGE;

    static {
        URL errorBadgeIconURL = DeployOnSaveUtils.class.getClassLoader().getResource(
                DEPLOY_ON_SAVE_DISABLED_BADGE_PATH);
        String compileOnSaveDisabledTP = "<img src=\"" + errorBadgeIconURL
                + "\">&nbsp;" + NbBundle.getMessage(DeployOnSaveUtils.class, "TP_DeployOnSaveDisabled");
        DEPLOY_ON_SAVE_DISABLED_BADGE = ImageUtilities.assignToolTipToImage(
                ImageUtilities.loadImage(DEPLOY_ON_SAVE_DISABLED_BADGE_PATH), compileOnSaveDisabledTP);
    }

    private DeployOnSaveUtils() {
        super();
    }
    /**
     *
     * @param icon
     * @return
     * @since 1.34
     */
    public static Image badgeDisabledDeployOnSave(Image icon) {
        return ImageUtilities.mergeImages(icon, DEPLOY_ON_SAVE_DISABLED_BADGE, 8, 0);
    }

    /**
     *
     * @return <code>true</code> if it is safe to perform build
     * @since 1.33
     */
    public static boolean containsIdeArtifacts(PropertyEvaluator evaluator, UpdateHelper updateHelper,
            String classesPropertyName) {

        FileObject mark = null;
        String propertyValue = (classesPropertyName != null)
                ? evaluator.getProperty(classesPropertyName)
                : null;

        if (propertyValue != null) {
            FileObject buildClasses = updateHelper.getAntProjectHelper().resolveFileObject(propertyValue);
            if (buildClasses != null) {
                mark = buildClasses.getFileObject(COS_MARK);
            }
        }

        return mark != null;
    }

    public static boolean showBuildActionWarning(Project project, CustomizerPresenter presenter) {
        String text = NbBundle.getMessage(DeployOnSaveUtils.class, "LBL_ProjectBuiltAutomatically");
        String projectProperties = NbBundle.getMessage(DeployOnSaveUtils.class, "BTN_ProjectProperties");
        String cleanAndBuild = NbBundle.getMessage(DeployOnSaveUtils.class, "BTN_CleanAndBuild");
        String ok = NbBundle.getMessage(DeployOnSaveUtils.class, "BTN_OK");
        String titleFormat = NbBundle.getMessage(DeployOnSaveUtils.class, "TITLE_BuildProjectWarning");
        String title = MessageFormat.format(titleFormat, ProjectUtils.getInformation(project).getDisplayName());
        DialogDescriptor dd = new DialogDescriptor(text,
                title,
                true,
                new Object[]{projectProperties, cleanAndBuild, ok},
                ok,
                DialogDescriptor.DEFAULT_ALIGN,
                null,
                null);

        dd.setMessageType(NotifyDescriptor.WARNING_MESSAGE);

        Object result = DialogDisplayer.getDefault().notify(dd);

        if (result == projectProperties) {
            if (presenter != null) {
                presenter.showCustomizer("Run"); // NOI18N
            }
            return false;
        }

        if (result == cleanAndBuild) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Disbles the compile on save.
     *
     * @param project
     * @param evaluator
     * @param updateHelper
     * @param classesPropertyName
     * @since 1.30
     */
    public static void performCleanup(final Project project, PropertyEvaluator evaluator,
            UpdateHelper updateHelper, String classesPropertyName, boolean forceCleanup) {

        // Delete COS mark
        FileObject mark = null;
        String propertyValue = (classesPropertyName != null)
                ? evaluator.getProperty(classesPropertyName)
                : null;

        if (propertyValue != null) {
            FileObject buildClasses = updateHelper.getAntProjectHelper().resolveFileObject(propertyValue);
            if (buildClasses != null) {
                mark = buildClasses.getFileObject(COS_MARK);
            }
        }

        if (mark != null || forceCleanup) {
            final ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
            assert ap != null;
            Mutex.EVENT.writeAccess(new Runnable() {
                @Override public void run() {
                    ap.invokeAction(ActionProvider.COMMAND_CLEAN, Lookups.fixed(project));
                }
            });
        }
    }

    public interface CustomizerPresenter {
        void showCustomizer(String category);
    }

    public static String isDeployOnSaveSupported(String serverInstanceID) {
        boolean deployOnSaveEnabled = false;
        try {
            deployOnSaveEnabled = Deployment.getDefault().getServerInstance(serverInstanceID)
                    .isDeployOnSaveSupported();
        } catch (InstanceRemovedException ex) {
            // false
        }
        return Boolean.toString(deployOnSaveEnabled);
    }
    
}
