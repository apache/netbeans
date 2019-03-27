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

package org.netbeans.installer.products.openesb;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.installer.utils.applications.GlassFishUtils;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.product.dependencies.Requirement;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.RemovalMode;
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
            "resource:" + // NOI18N
            "org/netbeans/installer/products/openesb/wizard.xml"; // NOI18N
    
    private static final String JBI_INSTALLER =
            "jbi_components_installer.jar"; // NOI18N

    private static final String JBI_CORE_INSTALLER =
            "jbi-core-installer.jar"; // NOI18N
    
    private static final String ADDON_ID = 
            "jbi_components"; // NOI18N
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private List<WizardComponent> wizardComponents;
    
    public ConfigurationLogic() throws InitializationException {
        wizardComponents = Wizard.loadWizardComponents(
                WIZARD_COMPONENTS_URI,
                getClass().getClassLoader());
    }
    
    public void install(Progress progress) throws InstallationException {
        final File openesbLocation = getProduct().getInstallationLocation();
        
        // get the list of suitable glassfish installations
        final List<Dependency> dependencies = 
                getProduct().getDependencies(Requirement.class);
        
        final List<Product> sources = 
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final File glassfishLocation = sources.get(0).getInstallationLocation();
        final File jbiInstaller = new File(openesbLocation, JBI_INSTALLER);
        
        // resolve the dependency
        dependencies.get(0).setVersionResolved(sources.get(0).getVersion());
        
        // stop the default domain //////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.stop.as")); // NOI18N
            
            GlassFishUtils.stopDefaultDomain(glassfishLocation);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.stop.as"), // NOI18N
                    e);
        }
        
        // http://www.netbeans.org/issues/show_bug.cgi?id=125358
        // run the jbi core installer first - temporary solution        
        /*
        final File jbiCoreInstallerTemp = new File(openesbLocation, JBI_CORE_INSTALLER);
        final File jbiCoreInstaller = new File(glassfishLocation, JBI_CORE_INSTALLER);
        
        try 
        {            
            FileUtils.moveFile(jbiCoreInstallerTemp, jbiCoreInstaller);
            progress.setDetail(getString("CL.install.jbi.core.installer")); // NOI18N
            final File java = GlassFishUtils.getJavaHome(glassfishLocation);
            SystemUtils.executeCommand(glassfishLocation,
                    JavaUtils.getExecutable(java).getPath(),
                    "-jar",
                    jbiCoreInstaller.getAbsolutePath(),
                    glassfishLocation.getAbsolutePath(),
                    "install");
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.jbi.core.installer"), // NOI18N
                    e);
        } finally {
            try {
                FileUtils.deleteFile(jbiCoreInstaller);
            } catch (IOException e) {
                LogManager.log(e);
            }
        }
        */
        // run the openesb installer ////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.openesb.installer")); // NOI18N
            
            final File asadmin = GlassFishUtils.getAsadmin(glassfishLocation);
            
            SystemUtils.executeCommand(
                    asadmin.getParentFile(),
                    asadmin.getAbsolutePath(),
                    GlassFishUtils.INSTALL_ADDON_COMMAND,
                    jbiInstaller.getAbsolutePath());
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.openesb.installer"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public void uninstall(Progress progress) throws UninstallationException {
        // get the list of suitable glassfish installations
        final List<Dependency> dependencies = 
                getProduct().getDependencies(Requirement.class);
        final List<Product> sources = 
                Registry.getInstance().getProducts(dependencies.get(0));
        
        // pick the first one and integrate with it
        final File glassfishLocation = sources.get(0).getInstallationLocation();
        
        // stop the default domain //////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.uninstall.stop.as")); // NOI18N
            
            GlassFishUtils.stopDefaultDomain(glassfishLocation);
        } catch (IOException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.stop.as"), // NOI18N
                    e);
        }

        // run the openesb uninstaller //////////////////////////////////////////////
        try {
            progress.setDetail(
                    getString("CL.uninstall.openesb.installer")); // NOI18N
            
            final File asadmin = GlassFishUtils.getAsadmin(glassfishLocation);
            
            SystemUtils.executeCommand(
                    asadmin.getParentFile(),
                    asadmin.getAbsolutePath(),
                    GlassFishUtils.UNINSTALL_ADDON_COMMAND,
                    ADDON_ID);
        } catch (IOException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.openesb.installer"), // NOI18N
                    e);
        }
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
}
