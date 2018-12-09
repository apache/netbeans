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

package org.netbeans.installer.products.tomcat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;

/**
 *
 
 
 */
public class ConfigurationLogic extends ProductConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private List<WizardComponent> wizardComponents;

    public ConfigurationLogic() throws InitializationException {
        wizardComponents = Wizard.loadWizardComponents(
                WIZARD_COMPONENTS_URI,
                getClass().getClassLoader());
    }

    public void install(
            final Progress progress) throws InstallationException {
        final File location = getProduct().getInstallationLocation();
        
        /////////////////////////////////////////////////////////////////////////////
        //try {
        //    progress.setDetail(getString("CL.install.irrelevant.files")); // NOI18N
        //    
        //    SystemUtils.removeIrrelevantFiles(location);
        //} catch (IOException e) {
        //    throw new InstallationException(
        //            getString("CL.install.error.irrelevant.files"), // NOI18N
        //            e);
        //}

        /////////////////////////////////////////////////////////////////////////////
//        try {
//            progress.setDetail(getString("CL.install.files.permissions")); // NOI18N
//            
//            SystemUtils.correctFilesPermissions(location);
//        } catch (IOException e) {
//            throw new InstallationException(
//                    getString("CL.install.error.files.permissions"), // NOI18N
//                    e);
//        }
        
        //get bundled registry to perform further runtime integration
        //http://wiki.netbeans.org/NetBeansInstallerIDEAndRuntimesIntegration
        Registry bundledRegistry = new Registry();
        try {
            final String bundledRegistryUri = System.getProperty(
                    Registry.BUNDLED_PRODUCT_REGISTRY_URI_PROPERTY);

            bundledRegistry.loadProductRegistry(
                    (bundledRegistryUri != null) ? bundledRegistryUri : Registry.DEFAULT_BUNDLED_PRODUCT_REGISTRY_URI);
        } catch (InitializationException e) {
            LogManager.log("Cannot load bundled registry", e);
        }

        /////////////////////////////////////////////////////////////////////////////
        // Reference: http://wiki.netbeans.org/wiki/view/TomcatAutoRegistration
        try {
            progress.setDetail(getString("CL.install.ide.integration")); // NOI18N

            final List<Product> ides =
                    Registry.getInstance().getProducts("nb-base");
            List<Product> productsToIntegrate = new ArrayList<Product>();
            for (Product ide : ides) {
                if (ide.getStatus() == Status.INSTALLED) {
                    LogManager.log("... checking if " + getProduct().getDisplayName() + " can be integrated with " + ide.getDisplayName() + " at " + ide.getInstallationLocation());
                    final File ideLocation = ide.getInstallationLocation();
                    if (ideLocation != null && FileUtils.exists(ideLocation) && !FileUtils.isEmpty(ideLocation)) {
                        final Product bundledProduct = bundledRegistry.getProduct(ide.getUid(), ide.getVersion());
                        if (bundledProduct != null) {
                            //one of already installed IDEs is in the bundled registry as well - we need to integrate with it
                            productsToIntegrate.add(ide);
                            LogManager.log("... will be integrated since this produce is also bundled");
                        } else {
                            //check if this IDE is not integrated with any other GF instance - we need integrate with such IDE instance
                            try {
                                if(!isTomcatRegistred(location)) {
                                    LogManager.log("... will be integrated since there it is not yet integrated with any instance or such an instance does not exist");
                                    productsToIntegrate.add(ide);
                                } else {
                                    LogManager.log("... will not be integrated since it is already integrated with another instance");
                                }
                            } catch (IOException e) {
                                LogManager.log(e);
                            }
                        }
                    }
                }
            }

            for (Product productToIntegrate : productsToIntegrate) {
                final File ideLocation = productToIntegrate.getInstallationLocation();
                LogManager.log("... integrate " + getProduct().getDisplayName() + " with " + productToIntegrate.getDisplayName() + " installed at " + ideLocation);
                /////////////////////////////////////////////////////////////////////////////
                // Reference: http://wiki.netbeans.org/wiki/view/TomcatAutoRegistration
                if(!registerTomcat(ideLocation, location)) {
                    continue;
                }
                
                // if the IDE was installed in the same session as the
                // appserver, we should add its "product id" to the IDE
                if (productToIntegrate.hasStatusChanged()) {
                    NetBeansUtils.addPackId(
                            ideLocation,
                            PRODUCT_ID);
                }
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.ide.integration"), // NOI18N
                    e);
        }

        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }

    private boolean isTomcatRegistred(File nbLocation) throws IOException {
        return new File (nbLocation, "nb/config/J2EE/InstalledServers/tomcat_autoregistered_instance").exists();
    }
    
    private boolean registerTomcat(File nbLocation, File tomcatLocation) throws IOException {
        File javaExe = JavaUtils.getExecutable(new File(System.getProperty("java.home")));
        String [] cp = {
            "platform/core/core.jar",
            "platform/core/core-base.jar",
            "platform/lib/boot.jar",
            "platform/lib/org-openide-modules.jar",
            "platform/core/org-openide-filesystems.jar",
            "platform/lib/org-openide-util.jar",
            "platform/lib/org-openide-util-lookup.jar",
            "platform/lib/org-openide-util-ui.jar",
            "enterprise/modules/org-netbeans-modules-j2eeapis.jar",
            "enterprise/modules/org-netbeans-modules-j2eeserver.jar",
            "enterprise/modules/org-netbeans-modules-tomcat5.jar"
        };
        for(String c : cp) {
            File f = new File(nbLocation, c);
            if(!FileUtils.exists(f)) {
                LogManager.log("... cannot find jar required for Tomcat integration: " + f);
                return false;
            }
        }
        String mainClass = "org.netbeans.modules.tomcat5.registration.AutomaticRegistration";
        List <String> commands = new ArrayList <String> ();
        File nbCluster = new File(nbLocation, "nb");
        commands.add(javaExe.getAbsolutePath());
        commands.add("-cp");
        commands.add(StringUtils.asString(cp, File.pathSeparator));
        commands.add(mainClass);
        commands.add("--add");
        commands.add(nbCluster.getAbsolutePath());
        commands.add(tomcatLocation.getAbsolutePath());
        
        return SystemUtils.executeCommand(nbLocation, commands.toArray(new String[]{})).getErrorCode() == 0;
    }
    
    private void removeTomcatIntegration(File nbLocation, File tomcatLocation) throws IOException {        
        /*
        final String value = NetBeansUtils.getJvmOption(
                nbLocation,
                JVM_OPTION_AUTOREGISTER_HOME_NAME);
        LogManager.log("... ide integrated with: " + value);
        if ((value != null)
                && (value.equals(tomcatLocation.getAbsolutePath()))) {
            LogManager.log("... removing integration");
            NetBeansUtils.removeJvmOption(
                    nbLocation,
                    JVM_OPTION_AUTOREGISTER_HOME_NAME);
            NetBeansUtils.removeJvmOption(
                    nbLocation,
                    JVM_OPTION_AUTOREGISTER_TOKEN_NAME);
        }*/
        FileUtils.deleteFile(new File (nbLocation, "nb/config/J2EE/InstalledServers/tomcat_autoregistered_instance"));
        FileUtils.deleteFile(new File (nbLocation, "nb/config/J2EE/InstalledServers/.nbattrs"));
    }

    public void uninstall(
            final Progress progress) throws UninstallationException {
        final File location = getProduct().getInstallationLocation();

        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.uninstall.ide.integration")); // NOI18N

            final List<Product> ides =
                    Registry.getInstance().getProducts("nb-base");
            for (Product ide: ides) {
                if (ide.getStatus() == Status.INSTALLED) {
                    LogManager.log("... checking if " + ide.getDisplayName() + " is integrated with " + getProduct().getDisplayName() + " installed at " + location);
                    final File nbLocation = ide.getInstallationLocation();
                    
                    if (nbLocation != null) {
                        LogManager.log("... ide location is " + nbLocation);
                        removeTomcatIntegration(nbLocation, location);
                    } else {
                        LogManager.log("... ide location is null");
                    }
                }
            }
        } catch (IOException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.ide.integration"), // NOI18N
                    e);
        }

        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }

    public List<WizardComponent> getWizardComponents(
            ) {
        return wizardComponents;
    }

    @Override
    public String getIcon() {
        if (SystemUtils.isWindows()) {
            return "bin/tomcat7.exe";
        } else {
            return null;
        }
    }

    public boolean requireLegalArtifactSaving() {     
       return false;                                   
    }    

    @Override
    public boolean allowModifyMode() {
        return false;
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String WIZARD_COMPONENTS_URI =
            "resource:" + // NOI18N
            "org/netbeans/installer/products/tomcat/wizard.xml"; // NOI18N
    public static final String PRODUCT_ID =
            "TOMCAT"; // NOI18N
}
