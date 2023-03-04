/**
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
package org.netbeans.installer.products.nb.all;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.product.filters.OrFilter;
import org.netbeans.installer.product.filters.ProductFilter;
import org.netbeans.installer.utils.*;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.applications.JavaUtils.JavaInfo;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.shortcut.FileShortcut;
import org.netbeans.installer.utils.system.shortcut.LocationType;
import org.netbeans.installer.utils.system.shortcut.Shortcut;
import org.netbeans.installer.utils.system.windows.WindowsRegistry;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.netbeans.installer.wizard.components.panels.JdkLocationPanel;

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

    @Override
    public void install(final Progress progress) throws InstallationException {
        final Product product = getProduct();
        final File installLocation = product.getInstallationLocation();
        final FilesList filesList = product.getInstalledFiles();
        final boolean hasNestedJre = product.getProperty(JdkLocationPanel.JRE_NESTED) != null;
        
        /////////////////////////////////////////////////////////////////////////////
        final File jdkHome = new File(
                product.getProperty(JdkLocationPanel.JDK_LOCATION_PROPERTY));
        try {
            progress.setDetail(getString("CL.install.jdk.home")); // NOI18N
            JavaInfo info = JavaUtils.getInfo(jdkHome);
            LogManager.log("Using the following JDK for NetBeans configuration : ");
            LogManager.log("... path    : "  + jdkHome);
            LogManager.log("... version : "  + info.getVersion().toJdkStyle());
            LogManager.log("... vendor  : "  + info.getVendor());
            LogManager.log("... arch    : "  + info.getArch());
            LogManager.log("... final   : "  + (!info.isNonFinal()));
            if (hasNestedJre) {
                NetBeansUtils.setJavaHome(installLocation, new File(installLocation, JavaUtils.JRE_NESTED_SUBDIR));
            } else {
                NetBeansUtils.setJavaHome(installLocation, jdkHome);
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.jdk.home"), // NOI18N
                    e);
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.netbeans.clusters")); // NOI18N
            for (String clusterName: CLUSTERS) {
                File lastModified = new File(new File(installLocation, clusterName), 
                                    NetBeansUtils.LAST_MODIFIED_MARKER);
                if(!FileUtils.exists(lastModified)) {
                    filesList.add(lastModified);
                }
                NetBeansUtils.addCluster(installLocation, clusterName);
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.netbeans.clusters"), // NOI18N
                    e);
        }

         // update the update_tracking files information //////////////////////////////
        for (String clusterName: CLUSTERS) {
            try {
                progress.setDetail(getString(
                        "CL.install.netbeans.update.tracking", // NOI18N
                        clusterName));

                NetBeansUtils.updateTrackingFilesInfo(installLocation, clusterName);
            } catch (IOException e) {
                throw new InstallationException(getString(
                        "CL.install.error.netbeans.update.tracking", // NOI18N
                        clusterName),
                        e);
            }
        }

        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.product.id")); // NOI18N

            filesList.add(NetBeansUtils.createProductId(installLocation));
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.product.id"), // NOI18N
                    e);
        }

        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.license.accepted")); // NOI18N
            filesList.add(
                    NetBeansUtils.createLicenseAcceptedMarker(installLocation, ""));

        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.license.accepted"), // NOI18N
                    e);
        }

        /////////////////////////////////////////////////////////////////////////////
        //try {
        //    progress.setDetail(getString("CL.install.irrelevant.files")); // NOI18N
        //
        //    SystemUtils.removeIrrelevantFiles(binSubdir);
        //    SystemUtils.removeIrrelevantFiles(etcSubdir);
        //    SystemUtils.removeIrrelevantFiles(platformCluster);
        //    SystemUtils.removeIrrelevantFiles(nbCluster);
        //    SystemUtils.removeIrrelevantFiles(ideCluster);
        //} catch (IOException e) {
        //    throw new InstallationException(
        //            getString("CL.install.error.irrelevant.files"), // NOI18N
        //            e);
        //}

        /////////////////////////////////////////////////////////////////////////////
        //try {
        //    progress.setDetail(getString("CL.install.files.permissions")); // NOI18N
        //
        //    SystemUtils.correctFilesPermissions(binSubdir);
        //    SystemUtils.correctFilesPermissions(etcSubdir);
        //    SystemUtils.correctFilesPermissions(platformCluster);
        //    SystemUtils.correctFilesPermissions(nbCluster);
        //    SystemUtils.correctFilesPermissions(ideCluster);
        //} catch (IOException e) {
        //    throw new InstallationException(
        //            getString("CL.install.error.files.permissions"), // NOI18N
        //            e);
        //}

        /////////////////////////////////////////////////////////////////////////////
        LogManager.logIndent(
                "creating the desktop shortcut for NetBeans IDE"); // NOI18N
        if (!SystemUtils.isMacOS()) {
            try {
                progress.setDetail(getString("CL.install.desktop")); // NOI18N

                if (SystemUtils.isCurrentUserAdmin()) {
                    LogManager.log(
                            "... current user is an administrator " + // NOI18N
                            "-- creating the shortcut for all users"); // NOI18N

                    SystemUtils.createShortcut(
                            getDesktopShortcut(installLocation),
                            LocationType.ALL_USERS_DESKTOP);

                    getProduct().setProperty(
                            DESKTOP_SHORTCUT_LOCATION_PROPERTY,
                            ALL_USERS_PROPERTY_VALUE);
                } else {
                    LogManager.log(
                            "... current user is an ordinary user " + // NOI18N
                            "-- creating the shortcut for the current " + // NOI18N
                            "user only"); // NOI18N

                    SystemUtils.createShortcut(
                            getDesktopShortcut(installLocation),
                            LocationType.CURRENT_USER_DESKTOP);

                    getProduct().setProperty(
                            DESKTOP_SHORTCUT_LOCATION_PROPERTY,
                            CURRENT_USER_PROPERTY_VALUE);
                }
            } catch (NativeException e) {
                LogManager.unindent();

                LogManager.log(
                        getString("CL.install.error.desktop"), // NOI18N
                        e);
            }
        } else {
            LogManager.log(
                    "... skipping this step as we're on Mac OS"); // NOI18N
        }
        LogManager.logUnindent(
                "... done"); // NOI18N

        /////////////////////////////////////////////////////////////////////////////
        LogManager.logIndent(
                "creating the start menu shortcut for NetBeans IDE"); // NOI18N
        try {
            progress.setDetail(getString("CL.install.start.menu")); // NOI18N

            if (SystemUtils.isCurrentUserAdmin()) {
                LogManager.log(
                        "... current user is an administrator " + // NOI18N
                        "-- creating the shortcut for all users"); // NOI18N

                SystemUtils.createShortcut(
                        getStartMenuShortcut(installLocation),
                        LocationType.ALL_USERS_START_MENU);

                getProduct().setProperty(
                        START_MENU_SHORTCUT_LOCATION_PROPERTY,
                        ALL_USERS_PROPERTY_VALUE);
            } else {
                LogManager.log(
                        "... current user is an ordinary user " + // NOI18N
                        "-- creating the shortcut for the current " + // NOI18N
                        "user only"); // NOI18N

                SystemUtils.createShortcut(
                        getStartMenuShortcut(installLocation),
                        LocationType.CURRENT_USER_START_MENU);

                getProduct().setProperty(
                        START_MENU_SHORTCUT_LOCATION_PROPERTY,
                        CURRENT_USER_PROPERTY_VALUE);
            }
        } catch (NativeException e) {
            LogManager.log(
                    getString("CL.install.error.start.menu"), // NOI18N
                    e);
        }
        LogManager.logUnindent(
                "... done"); // NOI18N

        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.install.netbeans.conf")); // NOI18N

            NetBeansUtils.updateNetBeansHome(installLocation);

        // final long xmx = NetBeansUtils.getJvmMemorySize(
        //         installLocation,
        //         NetBeansUtils.MEMORY_XMX);
        // if (xmx < REQUIRED_XMX_VALUE) {
        //     NetBeansUtils.setJvmMemorySize(
        //            installLocation,
        //             NetBeansUtils.MEMORY_XMX,
        //            REQUIRED_XMX_VALUE);
        // }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.netbeans.conf"), // NOI18N
                    e);
        }

        // register JavaDB if available
        File javadbLocation = null;
        boolean javadbRegistered = false;
        if(SystemUtils.isWindows()) {
            javadbLocation = new File(System.getenv("PROGRAMFILES"), "Sun\\JavaDB");
        }
        if (javadbLocation == null || ! javadbLocation.exists()) {
            if (JavaUtils.isJdk(jdkHome)) {
                javadbLocation = new File(jdkHome, "db");
            }
        }

        if(javadbLocation != null && javadbLocation.isDirectory()) {
            try {
                LogManager.log("... integrate " + getSystemDisplayName() + " with Java DB installed at " + javadbLocation);
                javadbRegistered = registerJavaDB(installLocation, javadbLocation);
                // Derby registration creates this file (see #234759)
                File lastModified = new File(installLocation, "nb/var/cache/lastmodified/all-checksum.txt");
                if (lastModified.exists()) {
                    File actual = lastModified;
                    do {
                        filesList.add(actual);
                        actual = actual.getParentFile();
                    } while (!actual.getName().equals("nb")); // NOI18N
                }
                if (! javadbRegistered) {
                    LogManager.log("... ... Java DB wasn't registred.");
                }
            } catch (IOException e) {
                LogManager.log("Cannot register JavaDB available at " + javadbLocation, e);
            }
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
        
        if (! javadbRegistered) {

            /////////////////////////////////////////////////////////////////////////////
            try {
                progress.setDetail(getString("CL.install.javadb.integration")); // NOI18N


                final List<Product> glassfishes =
                       Registry.getInstance().queryProducts(new OrFilter(
                        new ProductFilter("glassfish-mod-sun", Registry.getInstance().getTargetPlatform()),
                        new ProductFilter("glassfish-mod", Registry.getInstance().getTargetPlatform())));   

                Product productToIntegrate = null;
                for (Product glassfish : glassfishes) {
                    final Product bundledProduct = bundledRegistry.getProduct(
                            glassfish.getUid(), glassfish.getVersion());
                    if (glassfish.getStatus() == Status.INSTALLED && bundledProduct != null) {
                        final File location = glassfish.getInstallationLocation();
                        if (location != null && FileUtils.exists(location) && !FileUtils.isEmpty(location)) {
                            productToIntegrate = glassfish;
                            break;
                        }
                    }
                }
                if (productToIntegrate == null) {
                    for (Product glassfish : glassfishes) {
                        if (glassfish.getStatus() == Status.INSTALLED) {
                            final File location = glassfish.getInstallationLocation();
                            if (location != null && FileUtils.exists(location) && !FileUtils.isEmpty(location)) {
                                productToIntegrate = glassfish;
                                break;
                            }
                        }
                    }
                }
                if (productToIntegrate != null) {
                    final File location = productToIntegrate.getInstallationLocation();
                    LogManager.log("... integrate " + getSystemDisplayName() + " with Java DB installed at " + location);
                    boolean passed = registerJavaDB(installLocation, new File(location, "javadb"));
                    if (! passed) {
                        LogManager.log("... ... Java DB wasn't registred.");
                    }
                }
            } catch (IOException e) {
                throw new InstallationException(
                        getString("CL.install.error.javadb.integration"), // NOI18N
                        e);
            } finally {
                progress.setDetail(StringUtils.EMPTY_STRING); // NOI18N
            }
            /////////////////////////////////////////////////////////////////////////////
            
        }

        try {
            progress.setDetail(getString("CL.install.javafxsdk.integration")); // NOI18N

            final List<Product> javafxsdks =
                    Registry.getInstance().queryProducts(
                        new ProductFilter("javafxsdk", Registry.getInstance().getTargetPlatform()));

                  Product productToIntegrate = null;
            for (Product javafxsdk : javafxsdks) {
                final Product bundledProduct = bundledRegistry.getProduct(
                        javafxsdk.getUid(), javafxsdk.getVersion());
                if (javafxsdk.getStatus() == Status.INSTALLED && bundledProduct != null) {
                    final File fxsdkLocation = javafxsdk.getInstallationLocation();
                    final File fxrtLocation =
                            new File(SystemUtils.resolveString(javafxsdk.getProperty(JAVAFX_RUNTIME_INSTALLATION_LOCATION_PROPERTY)));
                    if (fxsdkLocation != null && fxrtLocation != null &&
                            FileUtils.exists(fxsdkLocation) && !FileUtils.isEmpty(fxsdkLocation) &&
                                FileUtils.exists(fxrtLocation) && !FileUtils.isEmpty(fxrtLocation)) {
                        productToIntegrate = javafxsdk;
                        break;
                    }
                }
            }
            if (productToIntegrate == null) {
                for (Product javafxsdk : javafxsdks) {
                    if (javafxsdk.getStatus() == Status.INSTALLED) {
                        final File fxsdkLocation = javafxsdk.getInstallationLocation();
                        final File fxrtLocation =
                                new File(SystemUtils.resolveString(javafxsdk.getProperty(JAVAFX_RUNTIME_INSTALLATION_LOCATION_PROPERTY)));
                        if (fxsdkLocation != null && fxrtLocation != null &&
                                FileUtils.exists(fxsdkLocation) && !FileUtils.isEmpty(fxsdkLocation) &&
                                FileUtils.exists(fxrtLocation) && !FileUtils.isEmpty(fxrtLocation)) {
                            productToIntegrate = javafxsdk;
                            break;
                        }
                    }
                }
            }
            if (productToIntegrate != null) {
                final File fxsdkLocation = productToIntegrate.getInstallationLocation();
                final File fxrtLocation =
                       new File(SystemUtils.resolveString(productToIntegrate.getProperty(JAVAFX_RUNTIME_INSTALLATION_LOCATION_PROPERTY)));
                LogManager.log("... integrate " + getSystemDisplayName() + " with " + productToIntegrate.getDisplayName() + 
                        " installed at " + fxsdkLocation + " and " + fxrtLocation);
                registerJavaFX(installLocation, fxsdkLocation, fxrtLocation);
            }
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.javafxsdk.integration"), // NOI18N
                    e);
        }  finally {
            progress.setDetail(StringUtils.EMPTY_STRING); // NOI18N
        }
        
        /////////////////////////////////////////////////////////////////////////////
        try {
            final List<Product> jdks = Registry.getInstance().getProducts("jdk");
            for (Product jdk : jdks) {
                // if the IDE was installed in the same session as the jdk, 
                // we should add jdk`s "product id" to the IDE
                if (jdk.getStatus().equals(Status.INSTALLED) && jdk.hasStatusChanged()) {
                    NetBeansUtils.addPackId(installLocation, JDK_PRODUCT_ID);
                    break;
                }
            }
        } catch  (IOException e) {
            LogManager.log("Cannot add jdk`s id to netbeans productid file", e);
        }

        try {
            //IDE Registartion files
            final File nbCluster = NetBeansUtils.getNbCluster(installLocation);
            filesList.add(new File(nbCluster,"servicetag/registration.xml"));
            filesList.add(new File(nbCluster,"servicetag/servicetag"));
            filesList.add(new File(nbCluster,"servicetag"));

            //core.properties file is required for usage statistics settings
            File coreProp = new File(nbCluster,NetBeansUtils.CORE_PROPERTIES);
            filesList.add(coreProp);
            filesList.add(coreProp.getParentFile());
            filesList.add(coreProp.getParentFile().getParentFile());
            filesList.add(coreProp.getParentFile().getParentFile().getParentFile());
            
            //GlassFish v3/Tomcat integration files
            filesList.add(new File (nbCluster, "config/GlassFishEE6WC/Instances/.nbattrs"));
            filesList.add(new File (nbCluster, "config/GlassFishEE6WC/Instances/glassfish_autoregistered_instance"));
            filesList.add(new File (nbCluster, "config/GlassFishEE6WC/Instances"));
            filesList.add(new File (nbCluster, "config/GlassFishEE6WC"));
            filesList.add(new File (nbCluster, "config/GlassFishEE6/Instances/.nbattrs"));
            filesList.add(new File (nbCluster, "config/GlassFishEE6/Instances/glassfish_autoregistered_instance"));
            filesList.add(new File (nbCluster, "config/GlassFishEE6/Instances"));
            filesList.add(new File (nbCluster, "config/GlassFishEE6"));
            filesList.add(new File (nbCluster, "config/J2EE/InstalledServers/.nbattrs"));
            filesList.add(new File (nbCluster, "config/J2EE/InstalledServers/tomcat_autoregistered_instance"));
            filesList.add(new File (nbCluster, "config/J2EE/InstalledServers"));
            filesList.add(new File (nbCluster, "config/J2EE"));
            filesList.add(new File (nbCluster, "config/JavaDB/registration_instance"));
            filesList.add(new File (nbCluster, "config/JavaDB/.nbattrs"));
            filesList.add(new File (nbCluster, "config/JavaDB"));

            //JavaFX integration files
            filesList.add(new File (nbCluster, "config/JavaFX/Instances/javafx_sdk_autoregistered_instance"));
            filesList.add(new File (nbCluster, "config/JavaFX/Instances/.nbattrs"));
            filesList.add(new File (nbCluster, "config/JavaFX/Instances"));
            filesList.add(new File (nbCluster, "config/JavaFX"));
        } catch (IOException e) {
            LogManager.log(e);
        }

        product.setProperty("installation.timestamp", new Long(System.currentTimeMillis()).toString());
        
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }

    private boolean registerJavaDB(File nbLocation, File javadbLocation) throws IOException {
        if(!FileUtils.exists(javadbLocation)) {
            LogManager.log("Requested to register JavaDB at " + javadbLocation + " but can't find it");
            return false;
        }
        File javaExe = JavaUtils.getExecutable(SystemUtils.getCurrentJavaHome());
        String [] cp = {
            "platform/core/core.jar",
            "platform/lib/boot.jar",
            "platform/lib/org-openide-modules.jar",
            "platform/core/org-openide-filesystems.jar",
            "platform/lib/org-openide-util.jar",
            "platform/lib/org-openide-util-lookup.jar",
            "ide/modules/org-netbeans-modules-derby.jar"
        };
        for(String c : cp) {
            File f = new File(nbLocation, c);
            if(!FileUtils.exists(f)) {
                LogManager.log("... cannot find jar required for JavaDB integration: " + f);
                return false;
            }
        }
        String mainClass = "org.netbeans.modules.derby.DerbyRegistration";
        List <String> commands = new ArrayList <String> ();
        commands.add(javaExe.getAbsolutePath());
        commands.add("-cp");
        commands.add(StringUtils.asString(cp, File.pathSeparator));
        commands.add(mainClass);
        commands.add(new File(nbLocation, "nb").getAbsolutePath());
        commands.add(javadbLocation.getAbsolutePath());
        return SystemUtils.executeCommand(nbLocation, commands.toArray(new String [] {})).getErrorCode() == 0;
    }

    private boolean registerJavaFX(File nbLocation, File sdkLocation, File reLocation) throws IOException {
        File javaExe = JavaUtils.getExecutable(new File(System.getProperty("java.home")));
        String [] cp = {
            "platform/core/core.jar",
            "platform/lib/boot.jar",
            "platform/lib/org-openide-modules.jar",
            "platform/core/org-openide-filesystems.jar",
            "platform/lib/org-openide-util.jar",
            "platform/lib/org-openide-util-lookup.jar",
            "javafx/modules/org-netbeans-modules-javafx2-platform.jar"
        };
        for(String c : cp) {
            File f = new File(nbLocation, c);
            if(!FileUtils.exists(f)) {
                LogManager.log("... cannot find jar required for JavaFX integration: " + f);
                return false;
            }
        }
        String mainClass = "org.netbeans.modules.javafx2.platform.registration.AutomaticRegistration";
        List <String> commands = new ArrayList <String> ();
        File nbCluster = new File(nbLocation, "nb");
        commands.add(javaExe.getAbsolutePath());
        commands.add("-cp");
        commands.add(StringUtils.asString(cp, File.pathSeparator));
        commands.add(mainClass);
        commands.add(nbCluster.getAbsolutePath());
        commands.add(sdkLocation.getAbsolutePath());
        commands.add(reLocation.getAbsolutePath());
        return SystemUtils.executeCommand(nbLocation, commands.toArray(new String[]{})).getErrorCode() == 0;
    }
    

    @Override
    public void uninstall(final Progress progress) throws UninstallationException {
        final Product product = getProduct();
        final File installLocation = product.getInstallationLocation();

        NetBeansUtils.warnNetbeansRunning(installLocation);
        /////////////////////////////////////////////////////////////////////////////
        try {
            progress.setDetail(getString("CL.uninstall.start.menu")); // NOI18N

            final String shortcutLocation =
                    getProduct().getProperty(START_MENU_SHORTCUT_LOCATION_PROPERTY);

            if ((shortcutLocation == null) ||
                    shortcutLocation.equals(CURRENT_USER_PROPERTY_VALUE)) {
                SystemUtils.removeShortcut(
                        getStartMenuShortcut(installLocation),
                        LocationType.CURRENT_USER_START_MENU,
                        true);
            } else {
                SystemUtils.removeShortcut(
                        getStartMenuShortcut(installLocation),
                        LocationType.ALL_USERS_START_MENU,
                        true);
            }
        } catch (NativeException e) {
            LogManager.log(
                    getString("CL.uninstall.error.start.menu"), // NOI18N
                    e);
        }

        /////////////////////////////////////////////////////////////////////////////
        if (!SystemUtils.isMacOS()) {
            try {
                progress.setDetail(getString("CL.uninstall.desktop")); // NOI18N

                final String shortcutLocation = getProduct().getProperty(
                        DESKTOP_SHORTCUT_LOCATION_PROPERTY);

                if ((shortcutLocation == null) ||
                        shortcutLocation.equals(CURRENT_USER_PROPERTY_VALUE)) {
                    SystemUtils.removeShortcut(
                            getDesktopShortcut(installLocation),
                            LocationType.CURRENT_USER_DESKTOP,
                            false);
                } else {
                    SystemUtils.removeShortcut(
                            getDesktopShortcut(installLocation),
                            LocationType.ALL_USERS_DESKTOP,
                            false);
                }
            } catch (NativeException e) {
                LogManager.log(
                        getString("CL.uninstall.error.desktop"), // NOI18N
                        e);
            }
        }
        
        if (SystemUtils.isWindows()) {
            checkAndDeleteWindowsRegistry(installLocation.getAbsolutePath());                        
        }
        
        product.setProperty("uninstallation.timestamp",
                new Long(System.currentTimeMillis()).toString());

        if (Boolean.getBoolean("remove.netbeans.userdir")) {
            try {
                progress.setDetail(getString("CL.uninstall.remove.userdir")); // NOI18N
                LogManager.logIndent("Removing NetBeans userdir... ");
                File userDir = NetBeansUtils.getNetBeansUserDirFile(installLocation);
                LogManager.log("... NetBeans userdir location : " + userDir);
                if (FileUtils.exists(userDir) && FileUtils.canWrite(userDir)) {
                    FileUtils.deleteFile(userDir, true);
                }
                LogManager.log("... NetBeans userdir totally removed");
            } catch (IOException e) {
                LogManager.log("Can`t remove NetBeans userdir", e);
            } finally {
                LogManager.unindent();
            }
            try {
                progress.setDetail(getString("CL.uninstall.remove.cachedir")); // NOI18N
                LogManager.logIndent("Removing NetBeans cachedir... ");
                File cacheDir = NetBeansUtils.getNetBeansCacheDirFile(installLocation);
                LogManager.log("... NetBeans cachedir location : " + cacheDir);
                if (FileUtils.exists(cacheDir) && FileUtils.canWrite(cacheDir)) {
                    FileUtils.deleteFile(cacheDir, true);
                }
                LogManager.log("... NetBeans cachedir totally removed");
            } catch (IOException e) {
                LogManager.log("Can`t remove NetBeans cachedir", e);
            } finally {
                LogManager.unindent();
            }
        }

        /////////////////////////////////////////////////////////////////////////////
        //remove cluster/update files
        try {
            progress.setDetail(getString("CL.uninstall.update.files")); // NOI18N
            for(String cluster : CLUSTERS) {
               File updateDir = new File(installLocation, cluster + File.separator + "update");
               if ( updateDir.exists()) {
                    FileUtils.deleteFile(updateDir, true);
               }
            }
        } catch (IOException e) {
            LogManager.log(
                    getString("CL.uninstall.error.update.files"), // NOI18N
                    e);
        }

        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }

    @Override
    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }

    @Override
    public String getSystemDisplayName() {
        return getString("CL.system.display.name");
    }

    @Override
    public boolean allowModifyMode() {
        return false;
    }

    @Override
    public boolean wrapForMacOs() {
        return true;
    }

    @Override
    public String getExecutable() {
        File jdkHome = new File(getProduct().getProperty(JdkLocationPanel.JDK_LOCATION_PROPERTY));
        JavaInfo javaInfo = JavaUtils.getInfo(jdkHome);
        
        if (SystemUtils.isWindows()) {
            String javaArch = "";
            if (javaInfo != null) {
                if (javaInfo.getArch().isEmpty()) {
                    javaInfo = JavaUtils.getInfo(jdkHome, true);
                    if (javaInfo != null) {
                        javaArch = javaInfo.getArch();
                    }
                } else {
                    javaArch = javaInfo.getArch();
                }
            }
            if (javaArch.endsWith("64")) {
                return EXECUTABLE_WINDOWS_64;
            } else {
                return EXECUTABLE_WINDOWS;
            }    
        } else {
            return EXECUTABLE_UNIX;
        }
    }

    @Override
    public String getIcon() {
        if (SystemUtils.isWindows()) {
            return ICON_WINDOWS;
        } else if (SystemUtils.isMacOS()) {
            return ICON_MACOSX;
        } else {
            return ICON_UNIX;
        }
    }

    @Override
    public Text getLicense() {
        return null;
    }
    // private //////////////////////////////////////////////////////////////////////
    private Shortcut getDesktopShortcut(final File directory) {
        return getShortcut(
                getStrings("CL.desktop.shortcut.name"), // NOI18N
                getStrings("CL.desktop.shortcut.description"), // NOI18N
                getString("CL.desktop.shortcut.path"), // NOI18N
                directory);
    }

    private Shortcut getStartMenuShortcut(final File directory) {
        if (SystemUtils.isMacOS()) {
            return getShortcut(
                    getStrings("CL.start.menu.shortcut.name.macosx"), // NOI18N
                    getStrings("CL.start.menu.shortcut.description"), // NOI18N
                    getString("CL.start.menu.shortcut.path"), // NOI18N
                    directory);
        } else {
            return getShortcut(
                    getStrings("CL.start.menu.shortcut.name"), // NOI18N
                    getStrings("CL.start.menu.shortcut.description"), // NOI18N
                    getString("CL.start.menu.shortcut.path"), // NOI18N
                    directory);
        }
    }

    private Shortcut getShortcut(
            final Map <Locale, String> names,
            final Map <Locale, String> descriptions,
            final String relativePath,
            final File location) {
        final File icon;
        final File executable;

        if (SystemUtils.isWindows()) {
            icon = new File(location, ICON_WINDOWS);
        } else if (SystemUtils.isMacOS()) {
            icon = new File(location, ICON_MACOSX);
        } else {
            icon = new File(location, ICON_UNIX);
        }

        executable = new File(location, getExecutable());
        final String name = names.get(new Locale(StringUtils.EMPTY_STRING));
        final FileShortcut shortcut = new FileShortcut(name, executable);
        shortcut.setNames(names);
        shortcut.setDescriptions(descriptions);
        shortcut.setCategories(SHORTCUT_CATEGORIES);
        shortcut.setFileName(SHORTCUT_FILENAME);
        shortcut.setIcon(icon);
        shortcut.setRelativePath(relativePath);
        shortcut.setWorkingDirectory(location);
        shortcut.setModifyPath(true);

        return shortcut;
    }
    
    private void checkAndDeleteWindowsRegistry(String installLocation) {
        final int HKEY = WindowsRegistry.HKEY_CLASSES_ROOT;
        final String KEY_ROOT = "Applications";
        final String KEY_ENDS[] = {"netbeans.exe\\shell\\open\\command",
            "netbeans64.exe\\shell\\open\\command"};

        final WindowsRegistry windowsRegistry = new WindowsRegistry();

        LogManager.log("Checking windows registry");

        for (String key : KEY_ENDS) {
            String fullKey = KEY_ROOT + WindowsRegistry.SEPARATOR + key;
            try {
                if (windowsRegistry.keyExists(HKEY, fullKey)) {
                    final String value = windowsRegistry.getStringValue(HKEY, fullKey, StringUtils.EMPTY_STRING);

                    String launcherPath = getLauncherPathFromRegistryValue(value);

                    File launcher = new File(launcherPath);
                    if (!launcher.exists() || launcherPath.startsWith(installLocation)) {
                        while (!fullKey.equals(KEY_ROOT)) {
                            windowsRegistry.deleteKey(HKEY, fullKey);
                            LogManager.log("... key deleted : " + fullKey);
                            fullKey = fullKey.substring(0, fullKey.lastIndexOf("\\"));
                        }                                                            
                    }
                }
            } catch (NativeException e) {
                LogManager.log(
                        getString("CL.uninstall.error.registry", "HKEY_CLASSES_ROOT" + WindowsRegistry.SEPARATOR + fullKey), // NOI18N
                        e);
            }
        }
    }
    
    private String getLauncherPathFromRegistryValue(String value) {
        String splittedRegistry[] = value.split("\"");
        if (splittedRegistry.length > 2) {
            return splittedRegistry[1];
        } else {
            return StringUtils.EMPTY_STRING;
        }
    }
    
    @Override
    public RemovalMode getRemovalMode() {
        if(Boolean.getBoolean("remove.netbeans.installdir")) {
            return RemovalMode.ALL;
        }
        return RemovalMode.LIST;
    }

    @Override
    public Map<String, Object> getAdditionalSystemIntegrationInfo() {
        Map<String, Object> map = super.getAdditionalSystemIntegrationInfo();
        if (SystemUtils.isWindows()) {
            //TODO: get localized readme if it is available and matches current locale
            String readme = new File(getProduct().getInstallationLocation(), "readme.html").getAbsolutePath();
            map.put("DisplayVersion", getString("CL.system.display.version"));
            map.put("Publisher",      getString("CL.system.publisher"));
            map.put("URLInfoAbout",   getString("CL.system.url.about"));
            map.put("URLUpdateInfo",  getString("CL.system.url.update"));
            map.put("HelpLink",       getString("CL.system.url.support"));
            map.put("Readme",         readme);
        }
        return map;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String WIZARD_COMPONENTS_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX + // NOI18N
            "org/netbeans/installer/products/nb/all/wizard.xml"; // NOI18N
    
    public static final String BIN_SUBDIR =
            "netbeans/bin"; // NOI18N
    public static final String ETC_SUBDIR =
            "netbeans/etc"; // NOI18N
    
    public static final String PLATFORM_CLUSTER =
            "{platform-cluster}"; // NOI18N
    public static final String NB_CLUSTER  =
            "{nb-cluster}"; // NOI18N
    public static final String IDE_CLUSTER =
            "{ide-cluster}"; // NOI18N
//    public static final String EXTRA_CLUSTER =
//            "{extra-cluster}"; // NOI18N
    public static final String [] CLUSTERS = new String [] {
        PLATFORM_CLUSTER,
        NB_CLUSTER,
        IDE_CLUSTER,
        /*EXTRA_CLUSTER*/};
    
    public static final String EXECUTABLE_WINDOWS =
            BIN_SUBDIR + "/netbeans.exe"; // NOI18N
    public static final String EXECUTABLE_WINDOWS_64 =
            BIN_SUBDIR + "/netbeans64.exe"; // NOI18N
    public static final String EXECUTABLE_UNIX =
            BIN_SUBDIR + "/netbeans"; // NOI18N
    
    public static final String ICON_WINDOWS =
            EXECUTABLE_WINDOWS;
    public static final String ICON_UNIX =
            "/netbeans/" + NB_CLUSTER + "/netbeans.png"; // NOI18N
    public static final String ICON_MACOSX =
            "/netbeans/" + NB_CLUSTER + "/netbeans.icns"; // NOI18N
    
    public static final String SHORTCUT_FILENAME =
            "Apache NetBeans-{display-version}.desktop"; // NOI18N
    public static final String[] SHORTCUT_CATEGORIES = new String[] {
        "Application",
        "Development", // NOI18N
        "Java",// NOI18N
        "IDE"// NOI18N
    };
    public static final String JDK_PRODUCT_ID =
            "JDK";//NOI18N
    public static final String GLASSFISH_JVM_OPTION_NAME =
            "-Dcom.sun.aas.installRoot"; // NOI18N
    
    public static final long REQUIRED_XMX_VALUE =
            192 * NetBeansUtils.M;
    
    private static final String DESKTOP_SHORTCUT_LOCATION_PROPERTY =
            "desktop.shortcut.location"; // NOI18N
    
    private static final String START_MENU_SHORTCUT_LOCATION_PROPERTY =
            "start.menu.shortcut.location"; // NOI18N
    
    private static final String ALL_USERS_PROPERTY_VALUE =
            "all.users"; // NOI18N
    
    private static final String CURRENT_USER_PROPERTY_VALUE =
            "current.user"; // NOI18N

    public static final String JAVAFX_RUNTIME_INSTALLATION_LOCATION_PROPERTY =
            "javafx.runtime.installation.location"; // NOI18N
}
