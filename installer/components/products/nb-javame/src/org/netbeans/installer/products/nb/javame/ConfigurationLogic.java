/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
 */
package org.netbeans.installer.products.nb.javame;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.NbClusterConfigurationLogic;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.components.panels.netbeans.NbWelcomePanel;

/**
 *
 */
public class ConfigurationLogic extends NbClusterConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String MOBILITY_CLUSTER =
            "{mobility-cluster}"; // NOI18N
    private static final String ENTERPRISE_CLUSTER =
            "{enterprise-cluster}"; // NOI18N
    private static final String ID =
            "MOB"; // NOI18N
    private static final String[] MOBILITY_END_2_END = {
        "config/Modules/org-netbeans-modules-mobility-end2end.xml",
        "config/Modules/org-netbeans-modules-mobility-jsr172.xml",
        "config/Modules/org-netbeans-modules-mobility-end2end-kit.xml",
        "modules/org-netbeans-modules-mobility-end2end.jar",
        "modules/org-netbeans-modules-mobility-jsr172.jar",
        "modules/org-netbeans-modules-mobility-end2end-kit.jar",
        "update_tracking/org-netbeans-modules-mobility-end2end.xml",
        "update_tracking/org-netbeans-modules-mobility-jsr172.xml",
        "update_tracking/org-netbeans-modules-mobility-end2end-kit.xml"
    };
    private static final String MOBILITY_END_2_END_KIT =
            "org-netbeans-modules-mobility-end2end-kit";
    private static final String NB_JAVAEE_UID = "nb-javaee"; //NOI18N
    private static final String END2END_CANT_REMOVE_TEXT = ResourceUtils.getString(
            ConfigurationLogic.class, "error.cannot.remove.end2end");
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    public ConfigurationLogic() throws InitializationException {
        super(new String[]{
            MOBILITY_CLUSTER}, ID);
    }

    @Override
    public void install(final Progress progress) throws InstallationException {
        super.install(progress);

        // HACK : remove mobility end-2-end if installed by mobility pack installer
        // and there is no enterpise cluster in the netbeans distribution
        File installationLocation = getProduct().getInstallationLocation();

        boolean removeEnd2End = false;

        if(NbWelcomePanel.BundleType.JAVAME.toString().equals(
                System.getProperty(NbWelcomePanel.WELCOME_PAGE_TYPE_PROPERTY))) {
            // Mobility Pack Installer
            removeEnd2End = true;
        }

        if (installationLocation != null) {
            if (removeEnd2End) {
                // check if pack is install in NetBeans with already installed enterprise4 cluster
                File entCluster = new File(installationLocation, ENTERPRISE_CLUSTER);
                if (!entCluster.exists() || FileUtils.isEmpty(entCluster)) {
                    for (String file : MOBILITY_END_2_END) {
                        File del = new File(installationLocation,
                                MOBILITY_CLUSTER + File.separator + file);
                        try {
                            FileUtils.deleteFile(del);
                        } catch (IOException e) {
                            throw new InstallationException(
                                    StringUtils.format(END2END_CANT_REMOVE_TEXT, del), e);
                        }
                    }
                }
            } else {
                // full installer
                // http://www.netbeans.org/issues/show_bug.cgi?id=123636
                // End2End modules must be disabled together with J2EE cluster
                // If enterprise will not be installed in the same session - disable end2end kit.
                // If it is already installed - do nothing
                List<Product> toInstall = Registry.getInstance().getProductsToInstall();
                Product eeProduct = null;
                for (Product p : toInstall) {
                    if (p.getUid().equals(NB_JAVAEE_UID)) {
                        eeProduct = p;
                        break;
                    }
                }
                if (eeProduct == null) {
                    //not selected to be installed, search for already installed instances of the same version
                    List<Dependency> dependencies =
                            getProduct().getDependencyByUid(BASE_IDE_UID);
                    List<Product> sources =
                            Registry.getInstance().getProducts(dependencies.get(0));
                    final Product nbProduct = sources.get(0);
                    for (Product p : Registry.getInstance().getInavoidableDependents(nbProduct)) {
                        if (p.getUid().equals(NB_JAVAEE_UID) && p.getStatus().equals(Status.INSTALLED)) {
                            eeProduct = p;
                            break;
                        }
                    }
                    if (eeProduct == null) {
                        //not installed and will not be installed in this session, disable end2end kit
                        LogManager.log("No enterprise features, disabling end2end kit");                        
                        NetBeansUtils.setModuleStatus(installationLocation,
                                MOBILITY_CLUSTER, 
                                MOBILITY_END_2_END_KIT, 
                                false);
                    } else {
                        LogManager.log("Enterprise is installed together with mobility, do nothing with end2end kit");
                    }
                } else {
                    LogManager.log("Enterprise would be installed together with mobility, do nothing with end2end kit");
                }
            }
        }
    }
}
