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
package org.netbeans.installer.products.mysql;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.components.Product;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.products.mysql.wizard.panels.MySQLPanel;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;
import static org.netbeans.installer.utils.StringUtils.QUOTE;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StreamUtils;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.NetBeansUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.helper.NbiThread;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.system.NativeUtils;
import org.netbeans.installer.utils.system.UnixNativeUtils.FileAccessMode;
import org.netbeans.installer.utils.system.WindowsNativeUtils;
import org.netbeans.installer.utils.system.shortcut.FileShortcut;
import org.netbeans.installer.utils.system.shortcut.LocationType;
import org.netbeans.installer.utils.system.windows.WindowsRegistry;
import static org.netbeans.installer.utils.system.windows.WindowsRegistry.*;

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
    public void install(
            final Progress progress) throws InstallationException {
        LogManager.log("Starting MySQL installation...");
        if (SystemUtils.isWindows()) {
            installWindows(progress);
        } else {
            installUnix(progress);
        }

        Registry bundledRegistry = new Registry();
        try {
            final String bundledRegistryUri = System.getProperty(
                    Registry.BUNDLED_PRODUCT_REGISTRY_URI_PROPERTY);

            bundledRegistry.loadProductRegistry(
                    (bundledRegistryUri != null) ? bundledRegistryUri : Registry.DEFAULT_BUNDLED_PRODUCT_REGISTRY_URI);
        } catch (InitializationException e) {
            LogManager.log("Cannot load bundled registry", e);
        }


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
                            //check if this IDE is not integrated with any other MySQL instance - we need integrate with such IDE instance
                            try {
                                String path = NetBeansUtils.getJvmOption(ideLocation, MYSQL_START_COMMAND_PROPERTY);
                                if (path == null || !FileUtils.exists(new File(path))) {
                                    LogManager.log("... will be integrated since there it is not yet integrated with any instance or such an instance does not exist");
                                    productsToIntegrate.add(ide);
                                } else {
                                    LogManager.log("... will not be integrated since it is already integrated with another instance at " + path);
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
                if (SystemUtils.isWindows()) {
                    File netLocation = new File(SystemUtils.getEnvironmentVariable("SYSTEMROOT") + File.separator + "system32" + File.separator + "net.exe");
                    NetBeansUtils.setJvmOption(
                            ideLocation, MYSQL_START_COMMAND_PROPERTY, 
                            netLocation.getAbsolutePath(), true);
                    NetBeansUtils.setJvmOption(
                            ideLocation, MYSQL_STOP_COMMAND_PROPERTY, 
                            netLocation.getAbsolutePath(), true);
                    NetBeansUtils.setJvmOption(
                            ideLocation, MYSQL_START_ARGS_PROPERTY,
                            StringUtils.asString(new String[]{"start", MYSQL_SERVICE_NAME}, StringUtils.SPACE), true);
                    NetBeansUtils.setJvmOption(
                            ideLocation, MYSQL_STOP_ARGS_PROPERTY, 
                            StringUtils.asString(new String[]{"stop", MYSQL_SERVICE_NAME}, StringUtils.SPACE), true);
                    NetBeansUtils.setJvmOption(
                            ideLocation, MYSQL_PORT_PROPERTY, 
                            getProperty(MySQLPanel.PORT_PROPERTY));
                } else {
                    File daemon = new File(getProduct().getInstallationLocation(),
                                MYSQL_SERVER_DAEMON_FILE_UNIX);
                    if (!SystemUtils.isCurrentUserAdmin()) {
                        NetBeansUtils.setJvmOption(
                                ideLocation, MYSQL_START_COMMAND_PROPERTY, daemon.getAbsolutePath(), true);
                        NetBeansUtils.setJvmOption(
                                ideLocation, MYSQL_STOP_COMMAND_PROPERTY, daemon.getAbsolutePath(), true);
                        NetBeansUtils.setJvmOption(
                                ideLocation, MYSQL_START_ARGS_PROPERTY, "start", true);
                        NetBeansUtils.setJvmOption(
                                ideLocation, MYSQL_STOP_ARGS_PROPERTY, "stop", true);
                        NetBeansUtils.setJvmOption(
                                ideLocation, MYSQL_PORT_PROPERTY,
                                getProperty(MySQLPanel.PORT_PROPERTY));
                    } else {
                        File gksu = null;
                        for(String s: POSSIBLE_GKSU_LOCATIONS) {
                            File f = new File(s);
                            if(FileUtils.exists(f)) {
                                gksu = f;
                                break;
                            }
                        }
                        if(gksu==null) {
                            //search in PATH
                            for(String s : StringUtils.asList(SystemUtils.getEnvironmentVariable("PATH"), File.pathSeparator)) {
                                if(s!=null && !s.equals(StringUtils.EMPTY_STRING)) {
                                    File f = new File(s, "gksu");
                                    if(FileUtils.exists(f)) {
                                        gksu = f;
                                        break;
                                    }
                                }
                            }
                        }
                        if (gksu != null) {
                            NetBeansUtils.setJvmOption(
                                    ideLocation, MYSQL_START_COMMAND_PROPERTY, gksu.getAbsolutePath(), true);
                            NetBeansUtils.setJvmOption(
                                    ideLocation, MYSQL_STOP_COMMAND_PROPERTY, gksu.getAbsolutePath(), true);
                            NetBeansUtils.setJvmOption(
                                    ideLocation, MYSQL_START_ARGS_PROPERTY,
                                    StringUtils.asString(new String[]{daemon.getAbsolutePath(), "start"}, StringUtils.SPACE), true);
                            NetBeansUtils.setJvmOption(
                                    ideLocation, MYSQL_STOP_ARGS_PROPERTY,
                                    StringUtils.asString(new String[]{daemon.getAbsolutePath(), "stop"}, StringUtils.SPACE), true);
                            NetBeansUtils.setJvmOption(
                                    ideLocation, MYSQL_PORT_PROPERTY,
                                    getProperty(MySQLPanel.PORT_PROPERTY));
                        } else {
                            LogManager.log("... gksu not available on the system, skipping MySQL integration");
                        }
                    }
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
        } catch (NativeException e) {
            throw new InstallationException(
                    getString("CL.install.error.ide.integration"), // NOI18N
                    e);
        }


        try {
            ClassLoader cl = getClass().getClassLoader();
            FileUtils.writeFile(new File(getProduct().getInstallationLocation(), NBGFMYSQL_LICENSE),
                    ResourceUtils.getResource(LEGAL_RESOURCE_PREFIX + NBGFMYSQL_LICENSE,
                    cl));
            FileUtils.writeFile(new File(getProduct().getInstallationLocation(), NBGFMYSQL_THIRDPARTY_README),
                    ResourceUtils.getResource(LEGAL_RESOURCE_PREFIX + NBGFMYSQL_THIRDPARTY_README,
                    cl));
        } catch (IOException e) {
            throw new InstallationException(
                    getString("CL.install.error.legal.creation"), // NOI18N
                    e);
        }

        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }

    private void installWindows(Progress progress) throws InstallationException {
        final File location = getProduct().getInstallationLocation();
        final File installer = new File(location, MYSQL_MSI_FILE_NAME);
        if (progress.isCanceled()) {
            return;
        }


        try {
            ExecutionResults results = null;

            final CompositeProgress overallProgress = new CompositeProgress();
            overallProgress.synchronizeTo(progress);
            overallProgress.synchronizeDetails(true);

            final Progress msiProgress = new Progress();
            final Progress configurationProgress = new Progress();

            overallProgress.addChild(msiProgress, (Progress.COMPLETE * 2) / 5);
            overallProgress.addChild(configurationProgress, (Progress.COMPLETE * 3) / 5);
            results = runMsiInstallerWindows(location, installer, msiProgress);
            if (results.getErrorCode() == 0) {
                getProduct().setProperty(MYSQL_INSTALLED_WINDOWS_PROPERTY,
                        "" + true);
                results = runInstanceConfigurationWizard(location, configurationProgress);
                switch (results.getErrorCode()) {
                    case 0:// All OK; 

                        break;
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                        throw new InstallationException(
                                ResourceUtils.getString(ConfigurationLogic.class,
                                ERROR_CONFIGURE_INSTANCE_MYSQL_ERROR_KEY_CODE_PREFIX + results.getErrorCode()));
                    default:
                        // General error? 
                        throw new InstallationException(
                                ResourceUtils.getString(ConfigurationLogic.class,
                                ERROR_CONFIGURE_INSTANCE_MYSQL_ERROR_KEY));
                }
                SystemUtils.sleep(3000);//wait for 3 seconds so that mysql really starts
                if(Boolean.parseBoolean(getProperty(MySQLPanel.MODIFY_SECURITY_PROPERTY))) {
                    fixSecuritySettingsWindows(location);
                }
            //createWindowsShortcuts(location);

            }


            if (results.getErrorCode() != 0) {
                throw new InstallationException(
                        ResourceUtils.getString(ConfigurationLogic.class,
                        ERROR_MYSQL_INSTALL_SCRIPT_RETURN_NONZERO_KEY,
                        StringUtils.EMPTY_STRING + results.getErrorCode()));
            }
        } finally {
            try {
                FileUtils.deleteFile(installer);
            } catch (IOException e) {
                LogManager.log("Cannot delete installer file " + installer, e);

            }
        }
    }

    private void fixSecuritySettingsWindows(File location) throws InstallationException {
        if (!Boolean.parseBoolean(getProperty(MySQLPanel.ANONYMOUS_ACCOUNT_PROPERTY))) {
            query(location, REMOVE_ANONYMOUS_QUERY);
        }
        query(location, REMOVE_REMOTE_ROOT_QUERY);
        query(location, FLUSH_PRIVILEGES_QUERY);
    }

    private void query(File location, String query) {
        final File exe = new File(location, MYSQL_EXE);


        try {
            LogManager.log("... query : " + query);
            List<String> commands = new ArrayList<String>();
            commands.add(exe.getAbsolutePath());
            commands.add("--defaults-file=" + new File(location, TARGET_CONFIGURATION_FILE));
            commands.add("--user=root");
            if (!getProperty(MySQLPanel.PASSWORD_PROPERTY).equals(StringUtils.EMPTY_STRING)) {
                commands.add("--password=" + getProperty(MySQLPanel.PASSWORD_PROPERTY));
            }
            commands.add("--connect_timeout=3");
            commands.add("-v");
            ProcessBuilder pb = new ProcessBuilder(commands).directory(location).redirectErrorStream(true);
            LogManager.log("... starting process : " + StringUtils.asString(commands, " "));
            Process p = pb.start();
            LogManager.log("... started, write query to stdin");
            p.getOutputStream().write(query.getBytes());
            p.getOutputStream().flush();
            p.getOutputStream().close();
            LogManager.log("... wait for termination");
            try {
                p.waitFor();
            } catch (InterruptedException e) {
                LogManager.log(e);
            }
            LogManager.logIndent("... query output: ");
            LogManager.log(StreamUtils.readStream(p.getInputStream()));
            LogManager.logUnindent("... query errorcode: " + p.exitValue());
            p.destroy();
        } catch (IOException e) {
            LogManager.log(e);
        }
    }

    private void installUnix(Progress progress) throws InstallationException {
        progress.setDetail(PROGRESS_DETAIL_RUNNING_MYSQL_INSTANCE_CONFIGURATION);
        final File location = getProduct().getInstallationLocation();
        final File installScript = new File(location, "configure-mysql.sh");
        try {
            InputStream is = ResourceUtils.getResource(INSTALL_SCRIPT_UNIX,
                    getClass().getClassLoader());
            FileUtils.writeFile(installScript, is);
            SystemUtils.setPermissions(installScript, FileAccessMode.EU, NativeUtils.FA_MODE_ADD);
            is.close();
            List<String> commandsList = new ArrayList<String>();
            commandsList.add(installScript.getAbsolutePath());
            commandsList.add(SystemUtils.isCurrentUserAdmin() ? "1" : "0"); // is root            
            commandsList.add("small"); // small system type
            commandsList.add(getProperty(MySQLPanel.PASSWORD_PROPERTY)); // password            
            if(!Boolean.parseBoolean(getProperty(MySQLPanel.NETWORK_PROPERTY))) {
                SystemUtils.setEnvironmentVariable("SKIP_NETWORKING", "true", EnvironmentScope.PROCESS, false);
            } else {
                SystemUtils.setEnvironmentVariable("PORT_NUMBER", getProperty(MySQLPanel.PORT_PROPERTY), EnvironmentScope.PROCESS, false);
            }
            if(!Boolean.parseBoolean(getProperty(MySQLPanel.ANONYMOUS_ACCOUNT_PROPERTY))) {
                SystemUtils.setEnvironmentVariable("REMOVE_ANONYMOUS", "true", EnvironmentScope.PROCESS, false);
            }
            if(Boolean.parseBoolean(getProperty(MySQLPanel.MODIFY_SECURITY_PROPERTY))) {
                SystemUtils.setEnvironmentVariable("MODIFY_SECURITY", "true", EnvironmentScope.PROCESS, false);
            }
            SystemUtils.executeCommand(location, commandsList.toArray(new String[0]));
        } catch (NativeException e) {
            throw new InstallationException(ERROR_INSTALL_MYSQL_ERROR_KEY, e);
        } catch (IOException e) {
            throw new InstallationException(ERROR_INSTALL_MYSQL_ERROR_KEY, e);
        } finally {
            try {
                FileUtils.deleteFile(installScript);
            } catch (IOException e) {
                LogManager.log(e);
            }
        }
    }

    private void createWindowsShortcuts(File location) throws InstallationException {
        // start MySQL server
        File icon = new File(location, "icons\\mysqlStart.ico");
        File executable = new File(location, "bin\\mysqld-nt.exe");
        FileShortcut shortcut = new FileShortcut(START_MYSQL_SHORTCUT_NAME, executable);

        shortcut.setRelativePath(MYSQL_START_MENU_GROUP);
        shortcut.setWorkingDirectory(location);
        shortcut.setModifyPath(true);
        List<String> args = new ArrayList<String>();
        args.add("--defaults-file=" + new File(location, TARGET_CONFIGURATION_FILE).getAbsolutePath());
        args.add("--console");
        shortcut.setIcon(icon);
        shortcut.setArguments(args);
        try {
            SystemUtils.createShortcut(shortcut, LocationType.ALL_USERS_START_MENU);
        } catch (NativeException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_CANNOT_CREATE_SHORTCUT_KEY),
                    e);
        }

        // stop MySQL server
        icon = new File(location, "icons\\mysqlStop.ico");
        executable = new File(location, "bin\\mysqladmin.exe");
        shortcut = new FileShortcut(STOP_MYSQL_SHORTCUT_NAME, executable);
        shortcut.setRelativePath(MYSQL_START_MENU_GROUP);
        shortcut.setWorkingDirectory(location);
        shortcut.setModifyPath(true);
        args = new ArrayList<String>();
        args.add("--u");
        args.add("root");
        args.add("shutdown");

        shortcut.setIcon(icon);
        shortcut.setArguments(args);
        try {
            SystemUtils.createShortcut(shortcut, LocationType.ALL_USERS_START_MENU);
        } catch (NativeException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_CANNOT_CREATE_SHORTCUT_KEY),
                    e);
        }
    }

    private long getInstanceConfigurationSpace() {
        return 40000000L;
    }

    private ExecutionResults runInstanceConfigurationWizard(File installationLocation, Progress progress) throws InstallationException {
        final File configInstanceFile = new File(installationLocation, INSTANCE_CONFIGURATION_FILE);

        progress.setDetail(PROGRESS_DETAIL_RUNNING_MYSQL_INSTANCE_CONFIGURATION);
        final File tempDir;
        try {
            tempDir = FileUtils.createTempFile(
                    SystemUtils.getTempDirectory(), true, true);
            SystemUtils.setEnvironmentVariable("TEMP",
                    tempDir.getAbsolutePath(),
                    EnvironmentScope.PROCESS,
                    false);
            SystemUtils.setEnvironmentVariable("TMP",
                    tempDir.getAbsolutePath(),
                    EnvironmentScope.PROCESS,
                    false);
            LogManager.log("... tempdir : " + tempDir);
        } catch (NativeException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_CONFIGURE_INSTANCE_MYSQL_ERROR_KEY), e);
        } catch (IOException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_CONFIGURE_INSTANCE_MYSQL_ERROR_KEY), e);
        }
        final File logFile = getLog("config");

        LogManager.log("... MySQL configuration log file : " + logFile);
        //String version =
        //        getProduct().getVersion().getMajor() + StringUtils.DOT +
        //        getProduct().getVersion().getMinor() + StringUtils.DOT +
        //        getProduct().getVersion().getMicro();
        String version = MYSQL_INSTANCE_VERSION;
        List<String> commandsList = new ArrayList<String>();

        commandsList.add(configInstanceFile.getAbsolutePath());
        commandsList.add("-i");                             // -i  (install instance)        

        commandsList.add("-q");                             // -q  (be quiet)

        commandsList.add("-p" + installationLocation);      // -p<path of installation> (no \bin)

        commandsList.add("-v" + version);                    // -v<version>

        if (logFile != null) {
            // -lfilename  (write log file)
            commandsList.add("-l" + logFile.getAbsolutePath());
        }

        // When launched manually, these can also be submitted
        // -t<.cnf template filename>
        // -c<.cnf filename>
        final File template = new File(installationLocation, TEMPLATE_CONFIGURATION_FILE);
        final File targetConfigFile = new File(installationLocation, TARGET_CONFIGURATION_FILE);
        commandsList.add("-t" + template);
        commandsList.add("-c" + targetConfigFile);
        commandsList.add("-n" + PRODUCT_NAME);              // -n<product name>
        // Use the following option to define the parameters for the config file generation.
        //
        // ServiceName=$
        // AddBinToPath={yes | no}
        // ServerType={DEVELOPMENT | SERVER | DEDICATED}
        // DatabaseType={MIXED | INNODB | MYISAM}
        // ConnectionUsage={DSS | OLTP}
        // ConnectionCount=#
        // SkipNetworking={yes | no}
        // Port=#
        // StrictMode={yes | no}
        // Charset=$
        // RootPassword=$
        // RootCurrentPassword=$

        commandsList.add("ServiceName=" + MYSQL_SERVICE_NAME);
        commandsList.add("AddBinToPath=no");
        commandsList.add("ServerType=DEVELOPMENT");
        commandsList.add("DatabaseType=MIXED");
        commandsList.add("ConnectionUsage=DSS");
        commandsList.add("Charset=utf8");

        if (Boolean.parseBoolean(getProperty(MySQLPanel.NETWORK_PROPERTY))) {
            commandsList.add("SkipNetworking=no");
            commandsList.add("Port=" + getProperty(MySQLPanel.PORT_PROPERTY));
        } else {
            commandsList.add("SkipNetworking=yes");
        }

        if (Boolean.parseBoolean(getProperty(MySQLPanel.MODIFY_SECURITY_PROPERTY))) {
            commandsList.add("RootPassword=" + getProperty(MySQLPanel.PASSWORD_PROPERTY));
        }


        String[] commands = commandsList.toArray(new String[0]);

        ProgressThread progressThread = new ProgressThread(progress,
                new File[]{installationLocation, tempDir},
                getInstanceConfigurationSpace());
        try {
            progressThread.start();
            return SystemUtils.executeCommand(installationLocation, commands);
        } catch (IOException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_INSTALL_MYSQL_ERROR_KEY), e);
        } finally {
            progressThread.finish();
            progress.setPercentage(Progress.COMPLETE);
        }
    }

    private File getLog(String suffix) {
        File logFile = LogManager.getLogFile();
        File resultLogFile = null;

        if (logFile != null) {
            String name = logFile.getName();

            if (name.lastIndexOf(".") == -1) {
                name += "_mysql_" + suffix + ".log";
            } else {
                String ext = name.substring(name.lastIndexOf("."));
                name = name.substring(0, name.lastIndexOf("."));
                name += "_mysql_" + suffix + ext;
            }
            resultLogFile = new File(LogManager.getLogFile().getParentFile(), name);
        }
        return resultLogFile;
    }

    private ExecutionResults runMsiInstallerWindows(File location, File installer, Progress progress) throws InstallationException {
        progress.setDetail(PROGRESS_DETAIL_RUNNING_MYSQL_INSTALLER);
        final File tempDir;
        try {
            tempDir = FileUtils.createTempFile(
                    SystemUtils.getTempDirectory(), true, true);
            SystemUtils.setEnvironmentVariable("TEMP",
                    tempDir.getAbsolutePath(),
                    EnvironmentScope.PROCESS,
                    false);
            SystemUtils.setEnvironmentVariable("TMP",
                    tempDir.getAbsolutePath(),
                    EnvironmentScope.PROCESS,
                    false);
            LogManager.log("... tempdir : " + tempDir);
        } catch (NativeException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_INSTALL_MYSQL_ERROR_KEY), e);
        } catch (IOException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_INSTALL_MYSQL_ERROR_KEY), e);
        }
        final File logFile = getLog("install");

        LogManager.log("... MySQL installation log file : " + logFile);

        final String installLocationOption = "INSTALLDIR=" + QUOTE + location + QUOTE;
        installLocationOption.replaceAll("[ ]+", " ").trim();
        List<String> commandsList = new ArrayList<String>();
        commandsList.add("msiexec.exe");
        commandsList.add("/qn");

        commandsList.addAll(Arrays.asList(installLocationOption.split(" ")));

        commandsList.add("/i");
        commandsList.add(installer.getAbsolutePath());

        if (logFile != null) {
            commandsList.add("/log");
            commandsList.add(logFile.getAbsolutePath());
        }
        String[] commands = commandsList.toArray(new String[0]);

        ProgressThread progressThread = new ProgressThread(progress,
                new File[]{location, tempDir},
                getProduct().getRequiredDiskSpace() - getProduct().getDownloadSize());
        try {
            progressThread.start();
            return SystemUtils.executeCommand(location, commands);
        } catch (IOException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_INSTALL_MYSQL_ERROR_KEY), e);
        } finally {
            progressThread.finish();
            progress.setPercentage(Progress.COMPLETE);
        }
    }

    private String getInstallationID(File location) throws NativeException {
        String id = null;
        WindowsNativeUtils utils = (WindowsNativeUtils) SystemUtils.getNativeUtils();
        WindowsRegistry reg = utils.getWindowsRegistry();
        String[] keyNames = reg.getSubKeyNames(HKLM, utils.UNINSTALL_KEY);
        for (String key : keyNames) {
            if (key.startsWith("{")) {//all IS-based installations start with this string

                String publisher = reg.valueExists(HKLM, utils.UNINSTALL_KEY + reg.SEPARATOR + key, "Publisher") ? reg.getStringValue(HKLM, utils.UNINSTALL_KEY + reg.SEPARATOR + key, "Publisher")
                        : null;
                String installSource = reg.valueExists(HKLM, utils.UNINSTALL_KEY + reg.SEPARATOR + key, "InstallSource") ? reg.getStringValue(HKLM, utils.UNINSTALL_KEY + reg.SEPARATOR + key, "InstallSource") : null;

                if (publisher != null && publisher.equals("MySQL AB") &&
                        installSource != null && new File(installSource).equals(location)) {
                    // this value is created by JDK installer
                    String uninstallString = reg.getStringValue(HKLM, utils.UNINSTALL_KEY + reg.SEPARATOR + key, "UninstallString");
                    int index = uninstallString.indexOf("/I{");
                    if (index != -1) {
                        uninstallString = uninstallString.substring(index + 2);
                        if (uninstallString.indexOf("}") != -1) {
                            id = uninstallString.substring(0, uninstallString.indexOf("}") + 1);
                            break;
                        }
                    }
                }
            }

        }
        return id;
    }

    public void uninstall(
            final Progress progress)
            throws UninstallationException {
        File location = getProduct().getInstallationLocation();
        if (SystemUtils.isWindows()) {
            uninstallWindows(progress, location);
        } else {
            uninstallUnix(progress, location);
        }
        
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
                        boolean integrated = false;
                        
                        if(SystemUtils.isWindows()) {
                            final String value = NetBeansUtils.getJvmOption(
                                nbLocation, MYSQL_START_ARGS_PROPERTY);
                            LogManager.log("... ide integrated with (start args): " + value);                        
                            integrated = value!=null && value.contains(MYSQL_SERVICE_NAME);
                        } else {                            
                            if(SystemUtils.isCurrentUserAdmin()) {
                                final String value = NetBeansUtils.getJvmOption(
                                nbLocation, MYSQL_START_COMMAND_PROPERTY);
                                LogManager.log("... ide integrated with: " + value);
                                integrated = value!=null && FileUtils.exists(new File(value));
                            } else {
                                final String value = NetBeansUtils.getJvmOption(
                                nbLocation, MYSQL_START_ARGS_PROPERTY);
                                LogManager.log("... ide integrated with (start args): " + value);
                                if(value!=null) {
                                    List <String> args = StringUtils.asList(value, StringUtils.SPACE);
                                    if(args.size()==2) {
                                        integrated = FileUtils.exists(new File(args.get(0)));
                                    }
                                }                                
                            }
                        }
                        if (integrated) {
			    LogManager.log("... removing integration");
                            NetBeansUtils.removeJvmOption(
                                    nbLocation,
                                    MYSQL_START_COMMAND_PROPERTY);
                            NetBeansUtils.removeJvmOption(
                                    nbLocation,
                                    MYSQL_STOP_COMMAND_PROPERTY);
                            NetBeansUtils.removeJvmOption(
                                    nbLocation,
                                    MYSQL_START_ARGS_PROPERTY);
                            NetBeansUtils.removeJvmOption(
                                    nbLocation,
                                    MYSQL_STOP_ARGS_PROPERTY);
                            NetBeansUtils.removeJvmOption(
                                    nbLocation,
                                    MYSQL_PORT_PROPERTY);
                        }
                    } else {
                        LogManager.log("... ide location is null");
                    }
                }
            }
        } catch (IOException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.ide.integration"), // NOI18N
                    e);           
        } catch (NativeException e) {
            throw new UninstallationException(
                    getString("CL.uninstall.error.ide.integration"), // NOI18N
                    e);
        }

        
        try {
            FileUtils.deleteFile(new File(location, "data"), true);
        } catch (IOException e) {
            LogManager.log(e);
        }
        /////////////////////////////////////////////////////////////////////////////
        progress.setPercentage(Progress.COMPLETE);
    }

    private void uninstallWindows(Progress progress, File location) throws UninstallationException {
        try {
            String id = getInstallationID(location);
            if (id != null) {
                LogManager.log("... uninstall ID : " + id);
                final File logFile = getLog("uninstall");
                final String[] commands;
                if (logFile != null) {
                    commands = new String[]{"msiexec.exe", "/qn", "/x", id, "/log", logFile.getAbsolutePath()};
                } else {
                    commands = new String[]{"msiexec.exe", "/qn", "/x", id};
                }
                progress.setDetail(PROGRESS_DETAIL_RUNNING_MYSQL_UNINSTALLER);

                ProgressThread progressThread = new ProgressThread(progress,
                        new File[]{location}, -1 * FileUtils.getSize(location));
                try {
                    progressThread.start();
                    ExecutionResults results = SystemUtils.executeCommand(commands);
                    if (results.getErrorCode() != 0) {
                        throw new UninstallationException(
                                ResourceUtils.getString(ConfigurationLogic.class,
                                ERROR_MYSQL_UNINSTALL_SCRIPT_RETURN_NONZERO_KEY,
                                StringUtils.EMPTY_STRING + results.getErrorCode()));
                    }
                } catch (IOException e) {
                    throw new UninstallationException(
                            ResourceUtils.getString(ConfigurationLogic.class,
                            ERROR_UNINSTALL_MYSQL_ERROR_KEY), e);
                } finally {
                    progressThread.finish();
                }

            }
        } catch (NativeException e) {
            throw new UninstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_UNINSTALL_MYSQL_ERROR_KEY), e);

        } finally {
            progress.setPercentage(progress.COMPLETE);
        }
    }

    private void uninstallUnix(Progress progress, File location) throws UninstallationException {

        final File uninstallScript = new File(location, "uninstall-mysql.sh");
        try {
            InputStream is = ResourceUtils.getResource(UNINSTALL_SCRIPT_UNIX,
                    getClass().getClassLoader());
            FileUtils.writeFile(uninstallScript, is);
            SystemUtils.setPermissions(uninstallScript, FileAccessMode.EU, NativeUtils.FA_MODE_ADD);
            is.close();
            List<String> commandsList = new ArrayList<String>();
            commandsList.add(uninstallScript.getAbsolutePath());
            commandsList.add(getProperty(MySQLPanel.PASSWORD_PROPERTY));
            ExecutionResults results = SystemUtils.executeCommand(location, commandsList.toArray(new String[0]));
            if (results.getStdErr().contains("Check that mysqld is running")) {
                LogManager.log("MySQL server is not running");
            } else if (results.getErrorCode() != 0) {
                throw new UninstallationException(
                        ResourceUtils.getString(ConfigurationLogic.class,
                        ERROR_MYSQL_UNINSTALL_SCRIPT_RETURN_NONZERO_KEY,
                        StringUtils.EMPTY_STRING + results.getErrorCode()));
            }
        } catch (IOException e) {
            throw new UninstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_UNINSTALL_MYSQL_ERROR_KEY), e);
        } finally {
            progress.setPercentage(progress.COMPLETE);
            try {
                FileUtils.deleteFile(uninstallScript);
            } catch (IOException e) {
                LogManager.log(e);
            }
        }
    }

    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }

    @Override
    public int getLogicPercentage() {
        return 90;
    }

    @Override
    public boolean registerInSystem() {
        return !SystemUtils.isWindows();
    }

    @Override
    public boolean allowModifyMode() {
        return false;
    }

    @Override
    public RemovalMode getRemovalMode() {
        return RemovalMode.ALL;
    }

    class ProgressThread extends NbiThread {

        private File[] directories;
        private long deltaSize = 0;
        private long initialSize = 0L;
        private Progress progress;
        private final Object LOCK = new Object();
        private boolean loop = false;

        public ProgressThread(Progress progress, File[] directories, final long maxDeltaSize) {
            LogManager.log("... new ProgressThread created");
            this.directories = directories;
            for (File directory : directories) {
                if (directory.exists()) {
                    initialSize += FileUtils.getSize(directory);
                }
            }
            this.deltaSize = maxDeltaSize;
            this.progress = progress;
            LogManager.log("... directories : " + StringUtils.asString(directories));
            LogManager.log("...   initial : " + initialSize);
            LogManager.log("...     delta : " + deltaSize);
        }

        @Override
        public void run() {
            LogManager.log("... progress thread started");
            long sleepTime = 1000L;
            try {
                synchronized (LOCK) {
                    loop = true;
                }
                while (isRunning()) {
                    try {
                        boolean update = false;
                        for (File directory : directories) {
                            if (directory.exists()) {
                                update = true;
                            }
                        }
                        if (update) {
                            updateProgressBar();
                        }
                        Thread.currentThread().sleep(sleepTime);
                    } catch (InterruptedException ex) {
                        LogManager.log(ex);
                        break;
                    } catch (Exception ex) {
                        LogManager.log(ex);
                        break;
                    }
                }
            } finally {
                synchronized (LOCK) {
                    LOCK.notify();
                }
            }
            progress.setPercentage(Progress.COMPLETE);
            LogManager.log("... progress thread finished");
        }

        public void finish() {
            if (!isRunning()) {
                return;
            }
            synchronized (LOCK) {
                loop = false;
            }
            synchronized (LOCK) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    LogManager.log(e);
                }
            }
        }

        private boolean isRunning() {
            boolean result;
            synchronized (LOCK) {
                result = loop;
            }
            return result;
        }

        private void updateProgressBar() {
            //LogManager.log("... get directory size");
            long size = 0;
            for (File directory : directories) {
                if (directory.exists()) {
                    size += FileUtils.getSize(directory);
                }
            }
            //LogManager.log("... size : " + size);
            long d = progress.COMPLETE * (size - initialSize) / deltaSize;
            //LogManager.log(".... real progress : " + d);
            d = progress.getPercentage() + (d - progress.getPercentage() + 1) / 2;
            //LogManager.log("... bound progress : " + d);
            d = (d < 0) ? 0 : (d > progress.COMPLETE ? progress.COMPLETE : d);
            if (((int) d) > progress.getPercentage()) {
                //LogManager.log("..... set progress : " + d);
                progress.setPercentage(d);
            }
        }
    }
/////////////////////////////////////////////////////////////////////////////////
// Constants
    public static final String WIZARD_COMPONENTS_URI =
            FileProxy.RESOURCE_SCHEME_PREFIX +
            "org/netbeans/installer/products/mysql/wizard.xml"; // NOI18N
    public static final String MYSQL_MSI_FILE_NAME =
            "{mysql-msi-installer-name}";//NOI18N
    public static final String MYSQL_INSTANCE_VERSION =
            "{mysql-instance-version}";//NOI18N
    public static final String INSTANCE_CONFIGURATION_FILE =
            "bin/MySQLInstanceConfig.exe";
    public static final String MYSQL_INSTALLED_WINDOWS_PROPERTY =
            "mysql.windows.installed";//NOI18N
    public static final String ERROR_MYSQL_INSTALL_SCRIPT_RETURN_NONZERO_KEY =
            "CL.error.installation.return.nonzero";//NOI18N
    public static final String PROGRESS_DETAIL_RUNNING_MYSQL_INSTALLER =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.progress.detail.install.mysql");
    public static final String PROGRESS_DETAIL_RUNNING_MYSQL_UNINSTALLER =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.progress.detail.uninstall.mysql");
    public static final String PROGRESS_DETAIL_RUNNING_MYSQL_INSTANCE_CONFIGURATION =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.progress.detail.configure.mysql");
    public static final String ERROR_INSTALL_MYSQL_ERROR_KEY =
            "CL.error.install.mysql.exception";
    public static final String ERROR_UNINSTALL_MYSQL_ERROR_KEY =
            "CL.error.uninstall.mysql.exception";
    public static final String ERROR_MYSQL_UNINSTALL_SCRIPT_RETURN_NONZERO_KEY =
            "CL.error.uninstall.mysql.non.zero";
    public static final String ERROR_CONFIGURE_INSTANCE_MYSQL_ERROR_KEY =
            "CL.error.configure.instance.exception";
    public static final String ERROR_CONFIGURE_INSTANCE_MYSQL_ERROR_KEY_CODE_PREFIX =
            "CL.error.configuration.code.";//NOI18N
    public static final String PRODUCT_NAME =
            "MySQL Server 5.0";//NOI18N
    public static final String TARGET_CONFIGURATION_FILE =
            "my.ini";//NOI18N
    public static final String TEMPLATE_CONFIGURATION_FILE =
            "my-template.ini";//NOI18N
    public static final String MYSQL_SERVICE_NAME =
            "MySQL50";
    private static final String START_MENU_SHORTCUT_LOCATION_PROPERTY =
            "start.menu.shortcut.location"; // NOI18N
    public static final String START_MYSQL_SHORTCUT_NAME =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.shortcuts.start.mysql");
    public static final String STOP_MYSQL_SHORTCUT_NAME =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.shortcuts.stop.mysql");
    public static final String MYSQL_START_MENU_GROUP =
            "MySQL\\MySQL Server 5.0";
    public static final String ERROR_CANNOT_CREATE_SHORTCUT_KEY =
            "CL.error.shortcut.create";
    public static final String PRODUCT_ID =
            "MYSQL";
    public static final String INSTALL_SCRIPT_UNIX =
            "org/netbeans/installer/products/mysql/scripts/install.sh";
    public static final String UNINSTALL_SCRIPT_UNIX =
            "org/netbeans/installer/products/mysql/scripts/uninstall.sh";
    final public static String REMOVE_ANONYMOUS_QUERY =
            "DELETE FROM mysql.user WHERE User='';";
    final public static String REMOVE_REMOTE_ROOT_QUERY =
            "DELETE FROM mysql.user WHERE User='root' AND Host!='localhost';";
    final public static String FLUSH_PRIVILEGES_QUERY =
            "FLUSH PRIVILEGES;";
    final public static String MYSQL_EXE = SystemUtils.isWindows() ? "bin/mysql.exe" : "bin/mysql";
    public static final String LEGAL_RESOURCE_PREFIX =
            "org/netbeans/installer/products/mysql/";
    public static final String NBGFMYSQL_LICENSE =
            "NB_GF_MySQL.txt";//NOI18N
    public static final String NBGFMYSQL_THIRDPARTY_README =
            "NB_GF_MySQL_Bundle_Thirdparty_license_readme.txt";
    public static final String MYSQL_START_COMMAND_PROPERTY =
            "-Dcom.sun.mysql.startcommand";
    public static final String MYSQL_START_ARGS_PROPERTY =
            "-Dcom.sun.mysql.startargs";
    public static final String MYSQL_STOP_COMMAND_PROPERTY =
            "-Dcom.sun.mysql.stopcommand";
    public static final String MYSQL_STOP_ARGS_PROPERTY =
            "-Dcom.sun.mysql.stopargs";
    public static final String MYSQL_PORT_PROPERTY =
            "-Dcom.sun.mysql.port";
    public static final String MYSQL_SERVER_DAEMON_FILE_UNIX =
            "support-files/mysql.server";
    public static final String[] POSSIBLE_GKSU_LOCATIONS = {
        "/usr/bin/gksu",
        "/usr/sbin/gksu",
        "/bin/gksu",
        "/sbin/gksu"
    };
    
}
