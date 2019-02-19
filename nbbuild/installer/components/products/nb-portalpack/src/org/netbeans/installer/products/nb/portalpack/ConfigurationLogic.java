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

package org.netbeans.installer.products.nb.portalpack;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;

/**
 *
 */
public class ConfigurationLogic extends ProductConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    private static final String ENTERPRISE_CLUSTER =
            "{enterprise-cluster}"; // NOI18N
    private static final String THIRDPARTYLICENSE_RESOURCE =
            "org/netbeans/installer/products/nb/portalpack/THIRDPARTYLICENSE.txt";
    public static final String WIZARD_COMPONENTS_URI =
            "resource:" + // NOI18N
            "org/netbeans/installer/products/nb/portalpack/wizard.xml"; // NOI18N
    private static final String NB_JAVAEE_UID= "nb-javaee";
    private List<WizardComponent> wizardComponents;
    
    
    public ConfigurationLogic() throws InitializationException {
        wizardComponents = Wizard.loadWizardComponents(
                WIZARD_COMPONENTS_URI,
                getClass().getClassLoader());
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    
    public void install(Progress progress) throws InstallationException {
        // get the list of suitable glassfish installations
        final List<Dependency> dependencies =
                getProduct().getDependencyByUid(NB_JAVAEE_UID);
        final List<Product> sources =
                Registry.getInstance().getProducts(dependencies.get(0));
        // resolve the dependency
        dependencies.get(0).setVersionResolved(sources.get(0).getVersion());
        
        // pick the first one and integrate with it
        final File nbLocation = sources.get(0).getInstallationLocation();
        final File entCluster = new File(nbLocation,ENTERPRISE_CLUSTER);
        
        try {
            List <File> before = FileUtils.listFiles(entCluster).toList();
            NetBeansUtils.runUpdater(nbLocation);
            List <File> after = FileUtils.listFiles(entCluster).toList();
            FilesList installedFiles = getProduct().getInstalledFiles();
            for(File f : after) {
                if(!before.contains(f)) {
                    LogManager.log("... file was created during Portal Pack installation : " + f);
                    installedFiles.add(f);
                }
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("cl.error.running.updater"),//NOI18N
                    e);
        }
    }
    public void uninstall(Progress progress) throws UninstallationException {
        //remove data created by updater
    }
    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }
    public boolean registerInSystem() {
        return false;
    }
    @Override
    public RemovalMode getRemovalMode() {
        return RemovalMode.LIST;
    }
    @Override
    public Text getThirdPartyLicense() {
        final String text = parseString("$R{" + THIRDPARTYLICENSE_RESOURCE + ";utf-8}");
        return new Text(text, Text.ContentType.PLAIN_TEXT);
    }
}
