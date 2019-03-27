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

package org.netbeans.installer.products.portletcontainer;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.utils.applications.GlassFishUtils;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.product.dependencies.Requirement;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.Dependency;
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
    public static final String WIZARD_COMPONENTS_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX +
            "org/netbeans/installer/products/portletcontainer/wizard.xml"; // NOI18N
    
    private static final String PC_INSTALLER =
            "portlet-container-configurator.jar"; // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private List<WizardComponent> wizardComponents;
    
    public ConfigurationLogic() throws InitializationException {
        wizardComponents = Wizard.loadWizardComponents(
                WIZARD_COMPONENTS_URI,
                getClass().getClassLoader());
    }
    
    public void install(Progress progress) throws InstallationException {
        final File pcLocation = getProduct().getInstallationLocation();
        
        // get the list of suitable glassfish installations
        final List<Dependency> dependencies =
                getProduct().getDependencies(Requirement.class);
        final List<Product> sources =
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final File asLocation = sources.get(0).getInstallationLocation();
        final File pcInstaller = new File(pcLocation, PC_INSTALLER);
        
        // resolve the dependency
        dependencies.get(0).setVersionResolved(sources.get(0).getVersion());
        /*
        // stop the default domain //////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.stop.as")); // NOI18N
            
            GlassFishUtils.stopDefaultDomain(asLocation);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.stop.as"), // NOI18N
                    e);
        }
        
        // run the pc installer ////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.portletcontainer.installer")); // NOI18N
            
            File javaExecutable = JavaUtils.getExecutable(
                    GlassFishUtils.getJavaHome(asLocation));
            
            SystemUtils.executeCommand(asLocation,
                    javaExecutable.getAbsolutePath(),
                    "-jar",
                    pcInstaller.getAbsolutePath(),
                    asLocation.getAbsolutePath(),
                    asLocation.getAbsolutePath() + File.separator +
                    "domains" +  File.separator +
                    GlassFishUtils.DEFAULT_DOMAIN);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.portletcontainer.installer"), // NOI18N
                    e);
        }
        */
        try {
            progress.setDetail(getString("CL.install.portletcontainer.installer")); // NOI18N
	    final File targetFile = new File(asLocation,
                            "lib" + File.separator + "addons" + File.separator + PC_INSTALLER);
            FileUtils.copyFile(pcInstaller, targetFile);
            getProduct().getInstalledFiles().add(targetFile);
	} catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.portletcontainer.installer"), // NOI18N
                    e);
        }
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public void uninstall(final Progress progress) throws UninstallationException {
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }
    
    @Override
    public boolean registerInSystem() {
        return false;
    }
    @Override
    public RemovalMode getRemovalMode() {
        return RemovalMode.LIST;
    }
    
    @Override
    public Text getLicense() {
       return null;
    }    
}
