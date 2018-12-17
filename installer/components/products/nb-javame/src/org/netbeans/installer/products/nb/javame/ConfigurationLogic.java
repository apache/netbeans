/**
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
