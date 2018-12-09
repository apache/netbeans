/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
