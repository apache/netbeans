/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
