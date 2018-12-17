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
package org.netbeans.installer.products.weblogic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.panels.JdkLocationPanel;
import org.netbeans.installer.products.weblogic.wizard.panels.WebLogicPanel;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils.JavaInfo;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.applications.WebLogicUtils;
import org.netbeans.installer.utils.helper.Status;

/**
 *
 
 
 */
public class ConfigurationLogic extends ProductConfigurationLogic {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private List<WizardComponent> wizardComponents;

    // constructor //////////////////////////////////////////////////////////////////
    public ConfigurationLogic() throws InitializationException {
        wizardComponents = Wizard.loadWizardComponents(
                WIZARD_COMPONENTS_URI,
                getClass().getClassLoader());
    }

    // configuration logic implementation ///////////////////////////////////////////
    @Override
    public void install(final Progress progress)
            throws InstallationException {
        final File directory = getProduct().getInstallationLocation();
       
        final File domainsubdir = new File(getProperty(WebLogicPanel.DOMAIN_INSTALLATION_SUBDIR_PROPERTY));
        final String domainname = getProperty(WebLogicPanel.DOMAINNAME_PROPERTY);
        final String username = getProperty(WebLogicPanel.USERNAME_PROPERTY);        
        final String password = getProperty(WebLogicPanel.PASSWORD_PROPERTY);
        
        final File domaindir = new File(domainsubdir, domainname);

        final File javaHome =
                new File(getProperty(JdkLocationPanel.JDK_LOCATION_PROPERTY));
        JavaInfo info = JavaUtils.getInfo(javaHome);
        LogManager.log("Using the following JDK for WebLogic configuration : ");
        LogManager.log("... path    : "  + javaHome);
        LogManager.log("... version : "  + info.getVersion().toJdkStyle());
        LogManager.log("... vendor  : "  + info.getVendor());
        LogManager.log("... final   : "  + (!info.isNonFinal()));

        final FilesList list = getProduct().getInstalledFiles();
        
 /////////////////////////////////////////////////////////////////////////////       
        try {                                    
            progress.setDetail(getString("CL.install.running.jar")); //NOI18N
            File installFile = new File(directory, INSTALLER_FILENAME);
            WebLogicUtils.unpackServerFiles(directory, javaHome, installFile);
            FileUtils.deleteFile(installFile);
            addFiles(list, directory);
            progress.setPercentage(Progress.COMPLETE / 2);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.running.jar"), //NOI18N
                    e);
        }
        try {                                    
            progress.setDetail(getString("CL.install.create.domain")); // NOI18N
            WebLogicUtils.createDomain(directory, javaHome, domainsubdir, domainname, username, password);
            progress.setPercentage(Progress.COMPLETE * 3 / 4);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.create.domain"), // NOI18N
                    e);            
        }
        
        try {
            progress.setDetail(getString("CL.install.extra.files")); // NOI18N                        
            //TODO: do it more clever: check installed files after domain creation and add the difference only.
            addFiles(list, domaindir);    
            list.add(new File(directory, REGISTRY_XML));
            list.add(new File(directory, LOGS_DIR));
            progress.setPercentage(Progress.COMPLETE * 9 / 10);
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.extra.files"), // NOI18N
                    e);
        }                  
      
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
        try {
            progress.setDetail(getString("CL.install.ide.integration")); // NOI18N

            final List<Product> ides =
                    Registry.getInstance().getProducts("nb-base");
            List<Product> productsToIntegrate = new ArrayList<Product>();
            for (Product ide : ides) {
                if (ide.getStatus() == Status.INSTALLED) {
                    LogManager.log("... checking if " + getProduct().getDisplayName() + " can be integrated with " + ide.getDisplayName() + " at " + ide.getInstallationLocation());
                    final File location = ide.getInstallationLocation();
                    if (location != null && FileUtils.exists(location) && !FileUtils.isEmpty(location)) {
                        final Product bundledProduct = bundledRegistry.getProduct(ide.getUid(), ide.getVersion());
                        if (bundledProduct != null) {
                            //one of already installed IDEs is in the bundled registry as well - we need to integrate with it
                            productsToIntegrate.add(ide);
                            LogManager.log("... will be integrated since this produce is also bundled");
                        } else {
                            //check if this IDE is not integrated with any other WL instance - we need integrate with such IDE instance
                            try {
                                if(!isWebLogicRegistred(location)) {
                                    LogManager.log("... will be integrated since there it is not yet integrated with any instance or such an instance does not exist");
                                    productsToIntegrate.add(ide);
                                } else {
                                    LogManager.log("... will not be integrated since it is already integrated with another instance");
                                }
                            } catch (IOException e)  {
                                LogManager.log(e);
                            }
                        }
                    }
                }
            }

            for (Product productToIntegrate : productsToIntegrate) {
                final File location = productToIntegrate.getInstallationLocation();
                //registerJavaDB(location, new File(directory, "javadb"));
                LogManager.log("... integrate " + getProduct().getDisplayName() + " with " + productToIntegrate.getDisplayName() + " installed at " + location);
                if(!registerWebLogic(location, directory, domaindir, username, SystemUtils.isWindows() ? "\"\"" : "")) {
                    continue;
                }
                
                // if the IDE was installed in the same session as the
                // appserver, we should add its "product id" to the IDE
                if (productToIntegrate.hasStatusChanged()) {
                    NetBeansUtils.addPackId(
                            location,
                            PRODUCT_ID);

                }
            }
        } catch  (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.ide.integration"), // NOI18N
                    e);
        }

        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);                     
    }
    
   private void addFiles(FilesList list, File location) throws IOException {
        LogManager.log("...addFiles");
        if(FileUtils.exists(location)) {
            if(location.isDirectory()) {
                list.add(location);
                File [] files = location.listFiles();
                if(files!=null && files.length>0) {
                    for(File f: files) {
                        addFiles(list, f);
                    }
                }
            } else {
                LogManager.log("...Adding " + location.getAbsolutePath() + " to the list");
                list.add(location);
            }
        }
    }    
    
    private boolean isWebLogicRegistred(File nbLocation) throws IOException {
        return new File (nbLocation, "nb/config/J2EE/InstalledServers/Instances/weblogic_autoregistered_instance").exists();
    }
  
    private boolean registerWebLogic(File nbLocation, File wlLocation, File domaindir, String username, String password) throws IOException {
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
            "enterprise/modules/org-netbeans-modules-j2ee-weblogic9.jar"
        };
        for(String c : cp) {
            File f = new File(nbLocation, c);
            if(!FileUtils.exists(f)) {
                LogManager.log("... cannot find jar required for WebLogic integration: " + f);
                return false;
            }
        }
        String mainClass = "org.netbeans.modules.j2ee.weblogic9.registration.AutomaticRegistration";
        List <String> commands = new ArrayList <String> ();
        File nbCluster = new File(nbLocation, "nb");
        commands.add(javaExe.getAbsolutePath());
        commands.add("-cp");
        commands.add(StringUtils.asString(cp, File.pathSeparator));
        commands.add(mainClass);
        commands.add("--add");
        commands.add(nbCluster.getAbsolutePath());     
        commands.add(new File(wlLocation, "wlserver").getAbsolutePath());
        commands.add(domaindir.getAbsolutePath());
        commands.add(username);
        commands.add(password);
        
        return SystemUtils.executeCommand(nbLocation, commands.toArray(new String[]{})).getErrorCode() == 0;
    }
    private  boolean removeWebLogicIntegration(File nbLocation,  File wlLocation, File domaindir) throws IOException {
        LogManager.log("... ide location is " + nbLocation);      

        //TODO Tomcat will be unregistered as well!!! Fix here and in Tomcat logic
        //FileUtils.deleteFile(new File (nbLocation, "nb/config/J2EE/InstalledServers/.nbattrs"));                 

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
            "enterprise/modules/org-netbeans-modules-j2ee-weblogic9.jar",
            "enterprise/modules/org-netbeans-modules-weblogic-common.jar"               
        };
        for(String c : cp) {
            File f = new File(nbLocation, c);
            if(!FileUtils.exists(f)) {
                LogManager.log("... cannot find jar required for WebLogic integration: " + f);
                return false;
            }
        }
        String mainClass = "org.netbeans.modules.j2ee.weblogic9.registration.AutomaticRegistration";
        List <String> commands = new ArrayList <String> ();
        File nbCluster = new File(nbLocation, "nb");
        commands.add(javaExe.getAbsolutePath());
        commands.add("-cp");
        commands.add(StringUtils.asString(cp, File.pathSeparator));
        commands.add(mainClass);
        commands.add("--remove");
        commands.add(nbCluster.getAbsolutePath());     
        commands.add(new File(wlLocation, "wlserver").getAbsolutePath());
        commands.add(domaindir.getAbsolutePath());        
        
        boolean result = SystemUtils.executeCommand(nbLocation, commands.toArray(new String[]{})).getErrorCode() == 0;
        
//        FileUtils.deleteFile(new File (nbLocation, "nb/config/J2EE/InstalledServers/weblogic_autoregistered_instance"));
        return result;
    }
    
    

    public void uninstall(final Progress progress)
            throws UninstallationException {
        File directory = getProduct().getInstallationLocation();

        final File domainsubdir = new File(getProperty(WebLogicPanel.DOMAIN_INSTALLATION_SUBDIR_PROPERTY));
        final String domainname = getProperty(WebLogicPanel.DOMAINNAME_PROPERTY);           
        
        final File domaindir = new File(domainsubdir, domainname);
        

        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.uninstall.ide.integration")); // NOI18N

            final List<Product> ides =
                    Registry.getInstance().getProducts("nb-base");
            for (Product ide: ides) {
                if (ide.getStatus() == Status.INSTALLED) {
                    LogManager.log("... checking if " + ide.getDisplayName() + " is integrated with " + getProduct().getDisplayName() + " installed at " + directory);
                    final File nbLocation = ide.getInstallationLocation();
                   
                    if (nbLocation != null) {
                        removeWebLogicIntegration(nbLocation, directory, domaindir);
                    }
                }
            }
        } catch (IOException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.ide.integration"), // NOI18N
                    e);
        }
        
/////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.uninstall.stop.domain")); // NOI18N

            WebLogicUtils.stopDomain(directory, domaindir);
        } catch (IOException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.stop.domain"), // NOI18N
                    e);
        }
/////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }

    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }

    @Override
    public int getLogicPercentage () {
        return 75;
    }
    
    @Override
    public boolean allowModifyMode() {
        return false;
    }


    public boolean requireLegalArtifactSaving() {     
       return false;                                   
    }
    
///////////////////////////////////////////////////////////////////////////////////
//// Constants
    public static final String WIZARD_COMPONENTS_URI =
            "resource:" + // NOI18N
            "org/netbeans/installer/products/weblogic/wizard.xml"; // NOI18N

    public static final String LOGS_DIR = 
            "logs"; // NOI18N     
  
    public static final String REGISTRY_XML = 
            "registry.xml"; // NOI18N
    public static final String PRODUCT_ID =
            "WEBLOGIC"; // NOI18N
    private static final String INSTALLER_FILENAME = 
        ResourceUtils.getString(ConfigurationLogic.class, 
            "CL.wls.installer.file"); //NOI18N
}
