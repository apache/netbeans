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

package org.netbeans.installer.products.jdk;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.installer.product.components.ProductConfigurationLogic;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.StringUtils;
import static org.netbeans.installer.utils.StringUtils.BACK_SLASH;
import static org.netbeans.installer.utils.StringUtils.EMPTY_STRING;
import static org.netbeans.installer.utils.StringUtils.QUOTE;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.applications.JavaUtils;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.helper.ErrorLevel;
import org.netbeans.installer.utils.helper.ExecutionResults;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.NbiThread;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.WindowsNativeUtils;
import org.netbeans.installer.utils.system.launchers.LauncherResource;
import org.netbeans.installer.utils.system.windows.WindowsRegistry;
import static org.netbeans.installer.utils.system.windows.WindowsRegistry.HKLM;
import org.netbeans.installer.wizard.Wizard;
import org.netbeans.installer.wizard.components.WizardComponent;

/**
 *
 * @author Dmitry Lipin
 */
public class ConfigurationLogic extends ProductConfigurationLogic {
    // Instance
    private List<WizardComponent> wizardComponents;
    
    public ConfigurationLogic() throws InitializationException {
        wizardComponents = Wizard.loadWizardComponents(
                WIZARD_COMPONENTS_URI,
                getClass().getClassLoader());
    }
    
    public void install(
            final Progress progress) throws InstallationException {
        if(progress.isCanceled()) return;
        
        final File location = getProduct().getInstallationLocation();
        final File installer = new File(location, JDK_INSTALLER_FILE_NAME);
        
        try {
            String [] commands = null;
            ExecutionResults results = null;
            
            
            boolean jreInstallationFailed = false;
            boolean javadbInstallation = false;
            boolean restartIsNeeded = false;
            final CompositeProgress overallProgress = new CompositeProgress();
            overallProgress.synchronizeTo(progress);
            overallProgress.synchronizeDetails(true);
            
            if(SystemUtils.isWindows()) {
                final File jdk = JavaUtils.findJDKHome(getProduct().getVersion());
                final File jre = JavaUtils.findJreHome(getProduct().getVersion());
                if (jdk == null) {
                    final Progress jdkProgress = new Progress();
                    final Progress jreProgress = new Progress();
                    final Progress javadbProgress = new Progress();
                    final boolean isFullSilentInstaller = isJDK6U15orLater();
                    //TODO: JavaDB feature is turned off for 64-bit OS
                    final boolean javadbBundled =
                            getProduct().getVersion().newerOrEquals(Version.getVersion("1.6.0"));
                    final boolean jreAlreadyInstalled = (jre != null);
                    if(isFullSilentInstaller) {
                        LogManager.log("... perform full silent installation");
                        overallProgress.addChild(jdkProgress, progress.COMPLETE);
                    } else {
                        //before jdk6u15 there were separate jre and javadb installers
                        if(jreAlreadyInstalled) {
                            if(javadbBundled) {
                                overallProgress.addChild(jdkProgress,    progress.COMPLETE * 6 / 7);
                                overallProgress.addChild(javadbProgress, progress.COMPLETE * 1 / 7);
                            } else {
                                overallProgress.addChild(jdkProgress, progress.COMPLETE);
                            }
                        } else {
                            if(javadbBundled) {
                                overallProgress.addChild(jdkProgress, progress.COMPLETE * 4 / 7 );
                                overallProgress.addChild(jreProgress, progress.COMPLETE * 2 / 7);
                                overallProgress.addChild(javadbProgress, progress.COMPLETE * 1 / 7);
                            } else {
                                overallProgress.addChild(jdkProgress, progress.COMPLETE * 3 / 5 );
                                overallProgress.addChild(jreProgress, progress.COMPLETE * 2 / 5);
                            }
                        }
                    }

                    if(!isFullSilentInstaller) {
                        //before jdk6u15(isFullSilentInstaller == true) there were separate jre and javadb installers
                        if(!progress.isCanceled()) {
                            if(!jreAlreadyInstalled) {                                
                                final File jreInstaller = findJREWindowsInstaller();
                                if(jreInstaller!=null) {
                                    results = runJREInstallerWindows(jreInstaller, jreProgress);
                                    configureJREProductWindows(results);
                                }
                                if(results.getErrorCode() != 0) {
                                    jreInstallationFailed = true;                                    
                                }
                                if(results.getErrorCode() == 3010) {                                    
                                    jreInstallationFailed = false;
                                    LogManager.log("The system Restart is required to complete the configuration of JRE");
                                    getProduct().setProperty(RESTART_IS_REQUIRED_PROPERTY, "" + true);
                                }
                            } else {
                                LogManager.log("... jre " + getProduct().getVersion() +
                                        " is already installed, skipping its configuration");
                            }
                        }
                        if (!progress.isCanceled() && javadbBundled) {
                            final File javadbInstaller = findJavaDBWindowsInstaller();
                            if (javadbInstaller != null) {
                                javadbInstallation = true;
                                getProduct().setProperty(JAVADB_INSTALLER_LOCATION_PROPERTY, javadbInstaller.getAbsolutePath());
                                results = runJavaDBInstallerWindows(javadbInstaller, javadbProgress);
                                configureJavaDBProductWindows(results);
                            }
                        }
                    }

                    if(!progress.isCanceled()) {
                        results = runJDKInstallerWindows(location, installer, jdkProgress,
                                isFullSilentInstaller, jreAlreadyInstalled, javadbBundled);
                        if(results.getErrorCode()==0) {
                            getProduct().setProperty(JDK_INSTALLED_WINDOWS_PROPERTY,
                                    "" + true);
                        }
                        addUninsallationJVM(results, location);
                    }
                    if(isFullSilentInstaller) {
                        if (!jreAlreadyInstalled) {
                            configureJREProductWindows(results);
                        } else {
                              LogManager.log("... jre " + getProduct().getVersion() +
                                        " is already installed, skipping its configuration");
                        }
                        if(javadbBundled) {
                            configureJavaDBProductWindows(results);
                        }
                    }
                } else {
                    LogManager.log("... jdk " + getProduct().getVersion() +
                            " is already installed, skipping JDK and JRE configuration");
                }
            } else {
                final Progress jdkProgress = new Progress();
                overallProgress.addChild(jdkProgress,Progress.COMPLETE);
                if (JDK_INSTALLER_FILE_NAME.isEmpty()) {
                    results = new ExecutionResults(0, "", "");
                } else {
                    results = runJDKInstallerUnix(location, installer, jdkProgress);
                }
                addUninsallationJVM(results, location);
                try {
                    addFiles(getProduct().getInstalledFiles(),location);
                } catch (IOException e) {
                    LogManager.log("Cannot add installed JDK files", e);
                }
            }
            
            
            if(results.getErrorCode()!=0) {
                throw new InstallationException(
                        ResourceUtils.getString(ConfigurationLogic.class,
                         ERROR_JDK_INSTALL_SCRIPT_RETURN_NONZERO_KEY,
                        StringUtils.EMPTY_STRING + results.getErrorCode()));
            }
            if(jreInstallationFailed) {
                throw new InstallationException(
                        ResourceUtils.getString(ConfigurationLogic.class,
                        ERROR_JRE_INSTALL_SCRIPT_RETURN_NONZERO_KEY,
                        StringUtils.EMPTY_STRING + results.getErrorCode()));
            }
        }  finally {
            if (!JDK_INSTALLER_FILE_NAME.isEmpty()) {
                try {
                    FileUtils.deleteFile(installer);
                } catch (IOException e) {
                    LogManager.log("Cannot delete installer file "+ installer, e);
                }
            }
        }
        
        progress.setPercentage(Progress.COMPLETE);
    }
    private void addFiles(FilesList list, File location) throws IOException {
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
                list.add(location);
            }
        }
    }

    private ExecutionResults runJDKInstallerWindows(File location,
            File installer, Progress progress,
            final boolean isFullSilentInstaller,
            final boolean jreAlreadyInstalled,
            final boolean javadbBundled) throws InstallationException {
        progress.setDetail(PROGRESS_DETAIL_RUNNING_JDK_INSTALLER);
        final File logFile = getLog("jdk_install");
        LogManager.log("... JDK installation log file : " + logFile);
        String [] commands;

        if(installer.getAbsolutePath().endsWith(".exe")) {
            //for exe
            LogManager.log("Installing JDK with exe installer");
            String logPath = logFile.getAbsolutePath();
            String locationPath = location.getAbsolutePath();
            if (logPath.contains(" ")) {
                logPath = convertPathNamesToShort(logPath);
            }
            if (locationPath.contains(" ")) {
                locationPath = convertPathNamesToShort(locationPath);
            }
            if (isJDK11()) {                
                // JDK 11
                commands = new String [] {
                    installer.getAbsolutePath(),
                    "/s",
                    logFile != null ? "/L" : EMPTY_STRING,
                    logFile != null ? logPath : EMPTY_STRING,
                    "INSTALLDIR=" + locationPath
                };
            } else if (isJDK8()) {
                commands = new String [] {
                    installer.getAbsolutePath(),
                    "/s",
                    logFile != null ? "/lv" : EMPTY_STRING,
                    logFile != null ? logPath : EMPTY_STRING,
                    "INSTALLDIR=" + locationPath,
                    "REBOOT=ReallySuppress"            
                };
            } else {
                // JDK 1.7
                commands = new String [] {
                    installer.getAbsolutePath(),
                    "/s",
                    "/qn",
                    "/norestart",
                    logFile != null ? "/lv" : EMPTY_STRING,
                    logFile != null ? logPath : EMPTY_STRING,
                    "INSTALLDIR=" + locationPath,
                    "REBOOT=ReallySuppress"            
                };
            }
        } else {
             //for msi
            LogManager.log("Installing JDK with MSI installer");
            final String packageOption = "/i \"" + installer.getAbsolutePath() +"\" ";
            final String loggingOption = (logFile!=null) ?
                "/log \"" + logFile.getAbsolutePath()  +"\" " : EMPTY_STRING;
            final String installLocationOption = "/qn INSTALLDIR=\"" +  location.getAbsolutePath() + "\"";
            commands = new String [] {
                "CMD",
                "/C",
                "msiexec.exe " + packageOption + loggingOption + installLocationOption
            };

        }
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
                    ERROR_INSTALL_JDK_ERROR_KEY),e);
        } catch (IOException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_INSTALL_JDK_ERROR_KEY),e);
        }

             List<File> directories = new ArrayList<File>();
        directories.add(location);
        directories.add(tempDir);
        long maxDeltaSize = getJDKinstallationSize()  + getProduct().getDownloadSize();

        //after jdk6u15 jre and javaDB installation is included into silent jdk installation
        if(isFullSilentInstaller) {
            if(!jreAlreadyInstalled) {
                directories.add(getJREInstallationLocationWindows());
                maxDeltaSize += getJREinstallationSize();
            }
            if(javadbBundled) {
                directories.add(getJavaDBInstallationLocationWindows());
                maxDeltaSize += getJavaDBInstallationSize();
            }
        }

        ProgressThread progressThread = new ProgressThread(progress,
                directories.toArray(new File[directories.size()]),
                maxDeltaSize);
        try {
            progressThread.start();
            return SystemUtils.executeCommand(location, commands);
        } catch (IOException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_INSTALL_JDK_ERROR_KEY),e);
        } finally {
            progressThread.finish();
            progress.setPercentage(Progress.COMPLETE);
        }
    }
    
    
    private static String convertPathNamesToShort(String path){
        File pathConverter = new File(SystemUtils.getTempDirectory(), "pathConverter.cmd");
        String result = path;
        List <String> commands = new ArrayList <String> (); 
        commands.add("@echo off");
        commands.add("set JPATH=" + path);
        commands.add("for %%i in (\"%JPATH%\") do set JPATH=%%~fsi");
        commands.add("echo %JPATH%");
        try{
            FileUtils.writeStringList(pathConverter, commands);
            ExecutionResults res=SystemUtils.executeCommand(pathConverter.getAbsolutePath());        
            FileUtils.deleteFile(pathConverter);
            result = res.getStdOut().trim();
        } catch(IOException ioe) {
            LogManager.log(ErrorLevel.WARNING, 
                    "Failed to convert " + path + " to a path with short names only." +
                     "\n Exception is thrown " + ioe);
        }
        return result;
    }    
    
    private ExecutionResults runJDKInstallerUnix(File location, File installer, Progress progress) throws InstallationException {
        File yesFile = null;
        ExecutionResults results = null;
        try {
            progress.setDetail(PROGRESS_DETAIL_RUNNING_JDK_INSTALLER);
            yesFile = FileUtils.createTempFile();
            FileUtils.writeFile(yesFile, "yes" + SystemUtils.getLineSeparator());
            
            //no separate log file since we can write at the same
            //final File logFile = getLog("jdk_install");
            final File logFile = null;
            
            final String loggingOption = (logFile!=null) ?
                " > " + StringUtils.escapePath(logFile.getAbsolutePath()) + " 2>&1"  :
                EMPTY_STRING;
            
            SystemUtils.correctFilesPermissions(installer);
            
            // according to Mandy Chung and Marek Slama:
            // If NB bundles JDK 6u5, the NB installer has to be modified to add a
            // new -noregister option to disable JDK registration.
            // Otherwise, a browser will be popped up during NB+JDK install. 
            final String registerOption = getProduct().getVersion().
                    newerOrEquals(Version.getVersion("1.6.0_05")) 
                    && getProduct().getVersion().olderThan(Version.getVersion("1.7.0_06")) // we build our own .bin since 1.7.0.6
                    ? " " + NO_REGISTER_JDK_OPTION : 
                        StringUtils.EMPTY_STRING;
                
            String [] commands = new String [] {
                "/bin/sh", "-c",
                StringUtils.escapePath(installer.getAbsolutePath()) +
                        registerOption +
                        " < " + StringUtils.escapePath(yesFile.getAbsolutePath()) +
                        loggingOption
            };
            ProgressThread progressThread = new ProgressThread(progress,
                    new File [] {location}, getJDKinstallationSize());
            try {
                progressThread.start();
                results = SystemUtils.executeCommand(location, commands);
            } finally {
                progressThread.finish();
            }
            // unix JDK installers create extra level directory jdkxxx
            File [] jdkDirs = location.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return (pathname.isDirectory() &&
                            pathname.getName().startsWith("jdk"));
                }
            });
            
            try {
                for(File dir : jdkDirs) {
                    for(File f : dir.listFiles()) {
                        SystemUtils.executeCommand("mv", "-f", f.getPath(), location.getAbsolutePath());
                    }
                    FileUtils.deleteFile(dir);
                }
            }  catch (IOException e) {
                throw new InstallationException(
                        ResourceUtils.getString(ConfigurationLogic.class,
                        ERROR_INSTALL_CANNOT_MOVE_DATA_KEY),e);
            }
            
            
        } catch (IOException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_INSTALL_JDK_ERROR_KEY),e);
        } finally {
            if(yesFile!=null) {
                try {
                    FileUtils.deleteFile(yesFile);
                } catch (IOException e) {
                    LogManager.log(e);
                }
            }
            progress.setPercentage(progress.COMPLETE);
        }
        return results;
    }
    private boolean isJDK6U10orLater() {
        return getProduct().getVersion().newerOrEquals(Version.getVersion("1.6.0_10"));
    }
    private boolean isJDK6U15orLater() {
        return getProduct().getVersion().newerOrEquals(Version.getVersion("1.6.0_15"));
    }
    private boolean isJDK7() {
        return getProduct().getVersion().newerOrEquals(Version.getVersion("1.7.0"));
    }
    private boolean isJDK8() {
        return getProduct().getVersion().newerOrEquals(Version.getVersion("1.8.0"));
    }
    private boolean isJDK11() {
        return getProduct().getVersion().newerOrEquals(Version.getVersion("11.0.1"));
    }


    private void configureJREProductWindows(ExecutionResults results) {
        LogManager.log("... configuring JRE Product");
        addUninsallationJVM(results, JavaUtils.findJreHome(getProduct().getVersion()));
        if(results.getErrorCode()==0) {
               getProduct().setProperty(JRE_INSTALLED_WINDOWS_PROPERTY,
                    "" + true);
        }
    }
    private void configureJavaDBProductWindows(ExecutionResults results) {
        if(results.getErrorCode()==0) {
            LogManager.log("... configuring JavaDB Product...");
            getProduct().setProperty(JAVADB_INSTALLED_WINDOWS_PROPERTY,
                        "" + true);
        }
    }
            
    private ExecutionResults runJREInstallerWindows(File jreInstaller, Progress progress) throws InstallationException {
        progress.setDetail(PROGRESS_DETAIL_RUNNING_JRE_INSTALLER);
        final List <String> commands = new ArrayList<String> ();
        
        final File logFile = getLog("jre_install");
        
        if(jreInstaller.getName().equals(JRE_MSI_NAME)) {
            commands.add("msiexec.exe");
            commands.add("/qn");
            commands.add("/i");
            commands.add(jreInstaller.getPath());
            commands.add("IEXPLORER=1");
            commands.add("MOZILLA=1");
            if (logFile != null) {
                commands.add("/log");
                commands.add(logFile.getAbsolutePath());
                LogManager.log("... JRE installation log file : " + logFile);
            }
        } else {
           /* final String packageOption = "/i \"" + jreInstaller.getPath() +"\" ";
            final String loggingOption = (logFile!=null) ?
                "/log \"" + logFile.getAbsolutePath()  +"\" ":
                EMPTY_STRING;
            final String silentOption = "/qn /norestart";
            String [] commands = new String [] {
                "CMD",
                "/C",
                "msiexec.exe " + packageOption + loggingOption + installLocationOption
                //"msiexec.exe /i D:\\NBI\\FXSDK_bundle\\test msi\\fx2.0.msi /log \"D:\\NBI\\FXSDK_bundle\\test space\\log.log1\" /qn"
            };*/
            commands.add(jreInstaller.getPath());
            commands.add("/s");
            commands.add("/v");
            commands.add("/qn");
            commands.add("/norestart");
            final String loggingOption = (logFile!=null) ?
            "/log " + BACK_SLASH + QUOTE  + logFile.getAbsolutePath()  + BACK_SLASH + QUOTE +" " : EMPTY_STRING;
            commands.add(loggingOption);
        }
        
        final String [] command = commands.toArray(new String [] {});
        
        final File location = getJREInstallationLocationWindows();
        LogManager.log("... JRE installation location (default) : " + location);
        try {
            SystemUtils.setEnvironmentVariable("TEMP",
                    SystemUtils.getTempDirectory().getAbsolutePath(),
                    EnvironmentScope.PROCESS,
                    false);
            SystemUtils.setEnvironmentVariable("TMP",
                    SystemUtils.getTempDirectory().getAbsolutePath(),
                    EnvironmentScope.PROCESS,
                    false);
        }  catch (NativeException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_INSTALL_JRE_ERROR_KEY),e);
        }
        ProgressThread progressThread = new ProgressThread( progress,
                new File [] {location},
                getJREinstallationSize());
        try {
            progressThread.start();
            return SystemUtils.executeCommand(command);
        } catch (IOException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_INSTALL_JRE_ERROR_KEY),e);
        } finally {
            progressThread.finish();
            if(!jreInstaller.getName().equals(JRE_MSI_NAME)) {
                try {
                    FileUtils.deleteFile(jreInstaller);
                } catch (IOException e) {
                    LogManager.log("Cannot delete JRE installer file " + jreInstaller, e);                
                }
            }

            progress.setPercentage(progress.COMPLETE);
        }
    }

    private ExecutionResults runJavaDBInstallerWindows(File javadbInstaller, Progress progress) throws InstallationException {
        progress.setDetail(PROGRESS_DETAIL_RUNNING_JAVADB_INSTALLER);
        final String [] command ;
        
        final File logFile = getLog("javadb_install");
        
        if(logFile!=null) {
            command = new String [] {
                "msiexec.exe",
                "/qn",
                "/i",
                javadbInstaller.getPath(),
                "/log",
                logFile.getAbsolutePath()
            };
            LogManager.log("... JavaDB installation log file : " + logFile);
        } else {
            command = new String [] {
                "msiexec.exe",
                "/qn",
                "/i",
                javadbInstaller.getPath()
            };
        }
        
        
        final File location = getJavaDBInstallationLocationWindows();
                
        LogManager.log("... JavaDB installation location (default) : " + location);
        try {
            SystemUtils.setEnvironmentVariable("TEMP",
                    SystemUtils.getTempDirectory().getAbsolutePath(),
                    EnvironmentScope.PROCESS,
                    false);
            SystemUtils.setEnvironmentVariable("TMP",
                    SystemUtils.getTempDirectory().getAbsolutePath(),
                    EnvironmentScope.PROCESS,
                    false);
        }  catch (NativeException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_INSTALL_JAVADB_ERROR_KEY),e);
        }
        ProgressThread progressThread = new ProgressThread( progress,
                new File [] {location},
                getJavaDBInstallationSize());
        try {
            progressThread.start();
            return SystemUtils.executeCommand(command);
        } catch (IOException e) {
            throw new InstallationException(
                    ResourceUtils.getString(ConfigurationLogic.class,
                    ERROR_INSTALL_JAVADB_ERROR_KEY),e);
        } finally {
            progressThread.finish();
            progress.setPercentage(progress.COMPLETE);
        }
    }
    private void addUninsallationJVM(ExecutionResults results, File location) {
        if(results!=null && results.getErrorCode()==0 && location!=null) {
            SystemUtils.getNativeUtils().addUninstallerJVM(new LauncherResource(false, location));
        }
    }
    /** Find path to public JRE installer ie. jre.msi file WITHOUT file itself.
     * @return null if jre.msi file for given JRE version is not found
     */
    private File findJREWindowsInstaller() {
        if (isJDK6U10orLater()) {
            // for 6u14+ we additionally bundle public jre installer
            // for 6u10-6u13, jre.msi is located at the JDK installation directory
            final String jreFileName = getProduct().getVersion().newerOrEquals(Version.getVersion("1.6.0_14")) ?
                JRE_INSTALLER_FILE_NAME :
                JRE_MSI_NAME;
            
            File jreInstallerFile = new File(
                    getProduct().getInstallationLocation(), jreFileName);
            if (!jreInstallerFile.exists()) {
                LogManager.log("... JRE installer doesn`t exist : " + jreInstallerFile);
                return null;
            }
            return jreInstallerFile;
        }
        
        File baseImagesDir  = new File(parseString("$E{CommonProgramFiles}"),
                "Java\\Update\\Base Images");
        if (!baseImagesDir.exists()) {
            LogManager.log("... cannot find images dir : " + baseImagesDir);
            return null;
        }
        
        File [] files = baseImagesDir.listFiles();
        File jdkDirFile = null;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().startsWith(JDK_PATCH_DIRECTORY)) {
                LogManager.log("... using JDK dir : " + files[i]);
                jdkDirFile = files[i];
                break;
            }
        }
        if (jdkDirFile==null) {
            LogManager.log("... cannot find default JDK dir");
            return null;
        }
        if (!jdkDirFile.exists()) {
            LogManager.log("... default JDK directory does not exist : " + jdkDirFile);
            return null;
        }
        
        files = jdkDirFile.listFiles();
        File patchDirFile = null;
        
        for (int i = 0; i < files.length; i++) {
            LogManager.log("... investigating : " + files [i]);
            if (files[i].getName().startsWith("patch-" + JDK_DEFAULT_INSTALL_DIR)) {
                patchDirFile = files[i];
                LogManager.log("... using JDK patch dir : " + patchDirFile);
                break;
            }
        }
        if (patchDirFile==null) {
            LogManager.log("... cannot find default JDK patch dir");
            return null;
        }
        if (!patchDirFile.exists()) {
            LogManager.log("... default JDK patch directory does not exist : " + patchDirFile);
            return null;
        }
        File jreInstallerFile = new File(patchDirFile,
                JRE_MSI_NAME);
        if (!jreInstallerFile.exists()) {
            LogManager.log("... JRE installer doesn`t exist : " + jreInstallerFile);
            return null;
        }
        LogManager.log("... found JRE windows installer at " + jreInstallerFile.getPath());
        return jreInstallerFile;
    }
    
    /** Find path to JavaDB installer ie. javadb.msi file WITHOUT file itself.
     * @return null if javadb.msi file for given JRE version is not found
     */
    private File findJavaDBWindowsInstaller() {
        if (isJDK6U10orLater()) {
            // Starting with JDK6U10, javadb.msi is located at the JDK installation directory
            File javadbInstallerFile = new File(
                    getProduct().getInstallationLocation(), JAVADB_MSI_NAME);
            if (!javadbInstallerFile.exists()) {
                LogManager.log("... JavaDB installer doesn`t exist : " + javadbInstallerFile);
                return null;
            }
            return javadbInstallerFile;
        }

        File baseImagesDir  = new File(parseString("$E{CommonProgramFiles}"),
                "Java\\Update\\Base Images");
        if (!baseImagesDir.exists()) {
            LogManager.log("... cannot find images dir : " + baseImagesDir);
            return null;
        }
        
        File [] files = baseImagesDir.listFiles();
        File jdkDirFile = null;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().startsWith(JDK_PATCH_DIRECTORY)) {
                LogManager.log("... using JDK dir : " + files[i]);
                jdkDirFile = files[i];
                break;
            }
        }
        if (jdkDirFile==null) {
            LogManager.log("... cannot find default JDK dir");
            return null;
        }
        if (!jdkDirFile.exists()) {
            LogManager.log("... default JDK directory does not exist : " + jdkDirFile);
            return null;
        }
        
        files = jdkDirFile.listFiles();
        File patchDirFile = null;
        
        for (int i = 0; i < files.length; i++) {
            LogManager.log("... investigating : " + files [i]);
            if (files[i].getName().startsWith("patch-" + JDK_DEFAULT_INSTALL_DIR)) {
                patchDirFile = files[i];
                LogManager.log("... using JDK patch dir : " + patchDirFile);
                break;
            }
        }
        if (patchDirFile==null) {
            LogManager.log("... cannot find default JDK patch dir");
            return null;
        }
        if (!patchDirFile.exists()) {
            LogManager.log("... default JDK patch directory does not exist : " + patchDirFile);
            return null;
        }
        File javadbInstallerFile = new File(patchDirFile,
                JAVADB_MSI_NAME);
        if (!javadbInstallerFile.exists()) {
            LogManager.log("... JavaDB installer doesn`t exist : " + javadbInstallerFile);
            return null;
        }
        LogManager.log("... found JavaDB windows installer at " + javadbInstallerFile.getPath());
        return javadbInstallerFile;
    }

    private File getJREInstallationLocationWindows() {
        String suffix;
        if(isJDK7()) {
            suffix = "7";
        } else if (isJDK6U10orLater()) {
            suffix = "6";
        } else {
            suffix = getProduct().getVersion().toJdkStyle();
        }
        return new File(parseString("$E{ProgramFiles}"),
                "Java\\jre" + suffix);
    }
    private File getJavaDBInstallationLocationWindows() {
        return new File(parseString(SUN_JAVADB_DEFAULT_LOCATION));
    }
    private long getJREinstallationSize() {
        long minorVersion = getProduct().getVersion().getMinor();
        if(minorVersion == 5) {
            return 70000000L;
        } else if (minorVersion == 6) {
            return 90000000L;
        } else if (minorVersion == 7) {
            return 100000000L;
        } else {
            return 100000000L;
        }
    }
    private long getJavaDBInstallationSize() {
        return 30000000L;
    }
    private long getJDKinstallationSize() {
        final long size;
        if(getProduct().getVersion().getMinor()==5) {
            if(SystemUtils.isWindows()) {
                size = 140000000L ;
            } else if(SystemUtils.isLinux()){
                size = 150000000L ;
            } else if(SystemUtils.getCurrentPlatform().isCompatibleWith(Platform.SOLARIS_SPARC)) {
                size = 148000000L;
            } else if(SystemUtils.getCurrentPlatform().isCompatibleWith(Platform.SOLARIS_X86)) {
                size = 140000000L;
            } else {
                // who knows...
                size = 160000000L;
            }
        } else if(getProduct().getVersion().getMinor()==6) {
            if(SystemUtils.isWindows()) {
                size = 190000000L ;
            } else if(SystemUtils.isLinux()){
                size = 200000000L ;
            } else if(SystemUtils.getCurrentPlatform().isCompatibleWith(Platform.SOLARIS_SPARC)) {
                size = 178000000L;
            } else if(SystemUtils.getCurrentPlatform().isCompatibleWith(Platform.SOLARIS_X86)) {
                size = 170000000L;
            } else {
                // who knows...
                size = 180000000L;
            }
        } else if(getProduct().getVersion().getMinor()==7) {
            if(SystemUtils.isWindows()) {
                size = 257000000L ;
            } else if(SystemUtils.isLinux()){
                size = 235000000L ;
            } else if(SystemUtils.getCurrentPlatform().isCompatibleWith(Platform.SOLARIS_SPARC)) {
                size = 244000000L;
            } else if(SystemUtils.getCurrentPlatform().isCompatibleWith(Platform.SOLARIS_X86)) {
                size = 245000000L;
            } else {
                // who knows...
                size = 180000000L;
            }
        } else {
            // who knows...
            size = 200000000L;
        }
        return size;
    }
    private String getInstallationID(File location) throws NativeException {
        String id = null;
        WindowsNativeUtils utils = (WindowsNativeUtils)SystemUtils.getNativeUtils();
        WindowsRegistry reg = utils.getWindowsRegistry();
        String [] keyNames = reg.getSubKeyNames(HKLM, utils.UNINSTALL_KEY);
        for(String key : keyNames) {
            if(key.startsWith("{")) {//all IS-based JDK installations start with this string
                if(reg.valueExists(HKLM, utils.UNINSTALL_KEY + reg.SEPARATOR + key, "DisplayIcon") &&
                        reg.valueExists(HKLM, utils.UNINSTALL_KEY + reg.SEPARATOR + key,"UninstallString")) {
                    // this value is created by JDK/JRE installer
                    final String icon = reg.getStringValue(HKLM, utils.UNINSTALL_KEY + reg.SEPARATOR + key, "DisplayIcon");
                    if(icon.endsWith("\\bin\\javaws.exe") && icon.startsWith(location.getAbsolutePath())) {
                        String uninstallString = reg.getStringValue(HKLM, utils.UNINSTALL_KEY + reg.SEPARATOR + key,"UninstallString");
                        int index = uninstallString.indexOf("/I{");
                        if(index==-1) {
                            index = uninstallString.indexOf("/X{");
                        }
                        if(index!=-1) {
                            uninstallString = uninstallString.substring(index+2);
                            if(uninstallString.indexOf("}")!=-1) {
                                id = uninstallString.substring(0, uninstallString.indexOf("}") + 1);
                                break;
                            }
                        }
                    }
                }
            }
            
        }
        return id;
    }
    private String getJavaDBInstallationID(File location) throws NativeException {
        String id = null;
        WindowsNativeUtils utils = (WindowsNativeUtils)SystemUtils.getNativeUtils();
        WindowsRegistry reg = utils.getWindowsRegistry();
        String [] keyNames = reg.getSubKeyNames(HKLM, utils.UNINSTALL_KEY);
        for(String key : keyNames) {
            if(key.startsWith("{")) {//all IS-based JavaDB installations start with this string
                if(reg.valueExists(HKLM, utils.UNINSTALL_KEY + reg.SEPARATOR + key, "InstallSource") &&
                        reg.valueExists(HKLM, utils.UNINSTALL_KEY + reg.SEPARATOR + key,"UninstallString") &&
                        reg.valueExists(HKLM, utils.UNINSTALL_KEY + reg.SEPARATOR + key,"URLInfoAbout")) {
                    // this value is created by JavaDB installer
                    final String urlAbout = reg.getStringValue(HKLM, utils.UNINSTALL_KEY + reg.SEPARATOR + key, "URLInfoAbout");
                    final File source = new File(reg.getStringValue(HKLM, utils.UNINSTALL_KEY + reg.SEPARATOR + key, "InstallSource"));                    
                    if(source.equals(location) && urlAbout.equals("http://developers.sun.com/javadb/")) {
                        String uninstallString = reg.getStringValue(HKLM, utils.UNINSTALL_KEY + reg.SEPARATOR + key,"UninstallString");
                        int index = uninstallString.indexOf("/X{");
                        if(index!=-1) {
                            uninstallString = uninstallString.substring(index+2);
                            if(uninstallString.indexOf("}")!=-1) {
                                id = uninstallString.substring(0, uninstallString.indexOf("}") + 1);
                                break;
                            }
                        }
                    }
                }
            }
            
        }
        return id;
    }
    
    private ExecutionResults runJDKUninstallerWindows(Progress progress, File location) throws UninstallationException {
        ExecutionResults results = null;
        try{
            String id = getInstallationID(location);
            
            if(id!=null) {
                LogManager.log("... uninstall ID : " + id);
                final File logFile = getLog("jdk_uninstall");
                final String [] commands;
                if(logFile!=null) {
                    commands = new String [] {"msiexec.exe", "/qn", "/x", id, "/log", logFile.getAbsolutePath()};
                } else {
                    commands = new String [] {"msiexec.exe", "/qn", "/x", id};
                }
                progress.setDetail(PROGRESS_DETAIL_RUNNING_JDK_UNINSTALLER);
                
                ProgressThread progressThread = new ProgressThread(progress,
                        new File[] {location}, -1 * FileUtils.getSize(location));
                try {
                    progressThread.start();
                    return SystemUtils.executeCommand(commands);
                } catch (IOException e) {
                    throw new UninstallationException(
                            ResourceUtils.getString(ConfigurationLogic.class,
                            ERROR_UNINSTALL_JDK_ERROR_KEY),e);
                } finally {
                    progressThread.finish();
                }
            } else {
                LogManager.log("... cannot fing JDK in the uninstall section");
            }
            
        } catch (NativeException e) {
            throw new UninstallationException(ERROR_UNINSTALL_JDK_ERROR_KEY,e);
        } finally {
            progress.setPercentage(progress.COMPLETE);
        }
        return results;
    }
    
    private ExecutionResults runJREUninstallerWindows(Progress progress, File location) throws UninstallationException {
        ExecutionResults results = null;
        try{
            String id = getInstallationID(location);
            
            if(id!=null) {
                LogManager.log("... uninstall ID : " + id);
                final File logFile = getLog("jre_uninstall");
                final String [] commands;
                if(logFile!=null) {
                    commands = new String [] {"msiexec.exe", "/qn", "/x", id, "/log", logFile.getAbsolutePath()};
                } else {
                    commands = new String [] {"msiexec.exe", "/qn", "/x", id};
                }
                progress.setDetail(PROGRESS_DETAIL_RUNNING_JRE_UNINSTALLER);
                ProgressThread progressThread = new ProgressThread(progress,
                        new File[] {location}, -1 * FileUtils.getSize(location));
                try {
                    progressThread.start();
                    return SystemUtils.executeCommand(commands);
                } catch (IOException e) {
                    throw new UninstallationException(
                            ResourceUtils.getString(ConfigurationLogic.class,
                            ERROR_UNINSTALL_JRE_ERROR_KEY),e);
                } finally {
                    progressThread.finish();
                }
            } else {
                LogManager.log("... cannot fing JRE in the uninstall section");
            }
            
        } catch (NativeException e) {
            throw new UninstallationException(ERROR_UNINSTALL_JDK_ERROR_KEY,e);
        } finally {
            progress.setPercentage(progress.COMPLETE);
        }
        return results;
    }
    private ExecutionResults runJavaDBUninstallerWindows(Progress progress, File location) throws UninstallationException {
        ExecutionResults results = null;
        try{
            File msiSourceLocation = new File(getProduct().getProperty(JAVADB_INSTALLER_LOCATION_PROPERTY));
            String id = getJavaDBInstallationID(msiSourceLocation.getParentFile());
            
            if(id!=null) {
                LogManager.log("... uninstall ID : " + id);
                final File logFile = getLog("javadb_uninstall");
                final String [] commands;
                if(logFile!=null) {
                    commands = new String [] {"msiexec.exe", "/qn", "/x", id, "/log", logFile.getAbsolutePath()};
                } else {
                    commands = new String [] {"msiexec.exe", "/qn", "/x", id};
                }
                progress.setDetail(PROGRESS_DETAIL_RUNNING_JAVADB_UNINSTALLER);
                ProgressThread progressThread = new ProgressThread(progress,
                        new File[] {location}, -1 * FileUtils.getSize(location));
                try {
                    progressThread.start();
                    return SystemUtils.executeCommand(commands);
                } catch (IOException e) {
                    throw new UninstallationException(
                            ResourceUtils.getString(ConfigurationLogic.class,
                            ERROR_UNINSTALL_JAVADB_ERROR_KEY),e);
                } finally {
                    progressThread.finish();
                }
            } else {
                LogManager.log("... cannot fing JavaDB in the uninstall section");
            }
            
        } catch (NativeException e) {
            throw new UninstallationException(ERROR_UNINSTALL_JAVADB_ERROR_KEY,e);
        } finally {
            progress.setPercentage(progress.COMPLETE);
        }
        return results;
    }
    @Override
    public boolean registerInSystem() {
        return false;
    }
    
    private File getLog(String suffix) {
        File logFile = LogManager.getLogFile();
        File resultLogFile = null;
        
        if(logFile!=null) {
            String name = logFile.getName();            
            
            if(name.lastIndexOf(".")==-1) {
                name += "_";
                name += suffix;
                name += ".log";
            } else {
                String ext = name.substring(name.lastIndexOf("."));
                name = name.substring(0, name.lastIndexOf("."));
                name += "_";
                name += suffix;
                name += ext;
            }
            resultLogFile = new File(LogManager.getLogFile().getParentFile(),name);
        }
        return resultLogFile;
    }
    public void uninstall(
            final Progress progress) throws UninstallationException {
        final File location = getProduct().getInstallationLocation();
        ExecutionResults results = null;
        if(SystemUtils.isWindows()) {
            if("true".equals(getProduct().getProperty(JDK_INSTALLED_WINDOWS_PROPERTY))) {
                final CompositeProgress overallProgress = new CompositeProgress();
                overallProgress.synchronizeTo(progress);
                overallProgress.synchronizeDetails(true);
                
                final Progress jdkProgress = new Progress();
                final Progress jreProgress = new Progress();
                final Progress javadbProgress = new Progress();
                
                if("true".equals(getProduct().getProperty(JRE_INSTALLED_WINDOWS_PROPERTY))) {
                    if("true".equals(getProduct().getProperty(JAVADB_INSTALLED_WINDOWS_PROPERTY))) {
                        overallProgress.addChild(jdkProgress,    progress.COMPLETE * 4 / 7 );
                        overallProgress.addChild(jreProgress,    progress.COMPLETE * 2 / 7);
                        overallProgress.addChild(javadbProgress, progress.COMPLETE * 1 / 7);
                    } else {
                        overallProgress.addChild(jdkProgress, progress.COMPLETE * 3 / 5 );
                        overallProgress.addChild(jreProgress, progress.COMPLETE * 2 / 5);
                    }
                } else {
                    if("true".equals(getProduct().getProperty(JAVADB_INSTALLED_WINDOWS_PROPERTY))) {
                        overallProgress.addChild(jdkProgress,    progress.COMPLETE * 6 / 7 );                        
                        overallProgress.addChild(javadbProgress, progress.COMPLETE * 1 / 7);
                    } else {
                        overallProgress.addChild(jdkProgress, progress.COMPLETE);
                    }
                }
                
                results = runJDKUninstallerWindows(jdkProgress, location);                
                
                if(results!=null) {
                    if(results.getErrorCode()==0) {
                        if("true".equals(getProduct().getProperty(JRE_INSTALLED_WINDOWS_PROPERTY))) {
                            results = runJREUninstallerWindows(jreProgress, JavaUtils.findJreHome(getProduct().getVersion()));                            
                            if(results!=null && results.getErrorCode()!=0) {
                                throw new UninstallationException(
                                        ResourceUtils.getString(ConfigurationLogic.class,
                                        ERROR_JRE_UNINSTALL_SCRIPT_RETURN_NONZERO_KEY,
                                        StringUtils.EMPTY_STRING + results.getErrorCode()));
                            }
                        }
                        if ("true".equals(getProduct().getProperty(JAVADB_INSTALLED_WINDOWS_PROPERTY))) {
                            final File javadbLocation = new File(parseString(SUN_JAVADB_DEFAULT_LOCATION));
                            results = runJavaDBUninstallerWindows(javadbProgress, javadbLocation);
                            if (results != null && results.getErrorCode() != 0) {
                                throw new UninstallationException(
                                        ResourceUtils.getString(ConfigurationLogic.class,
                                        ERROR_JAVADB_UNINSTALL_SCRIPT_RETURN_NONZERO_KEY,
                                        StringUtils.EMPTY_STRING + results.getErrorCode()));
                            }
                        }                        
                    } else {
                        throw new UninstallationException(
                                ResourceUtils.getString(ConfigurationLogic.class,
                                ERROR_JDK_UNINSTALL_SCRIPT_RETURN_NONZERO_KEY,
                                StringUtils.EMPTY_STRING + results.getErrorCode()));
                    }
                    
                }
            }
        }
        progress.setPercentage(Progress.COMPLETE);
    }
    
    public List<WizardComponent> getWizardComponents() {
        return wizardComponents;
    }
    
    @Override
    public String getIcon() {
        if (SystemUtils.isWindows()) {
            return "bin/javaws.exe";
        } else {
            return null;
        }
    }
    @Override
    public int getLogicPercentage() {
        return JDK_INSTALLER_FILE_NAME.isEmpty() ? 10 : 90;
    }
    
    @Override
    public boolean allowModifyMode() {
        return false;
    }
    public RemovalMode getRemovalMode() {
        return RemovalMode.ALL;
    }
    
    @Override
    public String validateInstallation() {
        String message = super.validateInstallation();
        if(message==null) {
            File jdkLocation = getProduct().getInstallationLocation();
            if(JavaUtils.getInfo(jdkLocation)==null) {
                message = "There is no JDK at " + jdkLocation + " or the installation is corrupted";
            }
        }
        if(message!=null) {
            LogManager.log("JDK validation:");
            LogManager.log(message);
            getProduct().setStatus(Status.NOT_INSTALLED);
            getProduct().getParent().removeChild(getProduct());
        }
        return null;
    }
    class ProgressThread extends NbiThread {
        private File [] directories ;
        private long deltaSize = 0;
        private long initialSize = 0L;
        private Progress progress;
        private final Object LOCK = new Object();
        private boolean loop = false;
        
        public ProgressThread(Progress progress, File [] directories, final long maxDeltaSize) {
            LogManager.log("... new ProgressThread created");
            this.directories = directories;
            for(File directory : directories) {
                if(directory.exists()) {
                    initialSize += FileUtils.getSize(directory);
                }
            }
            this.deltaSize = maxDeltaSize;
            this.progress = progress;
            LogManager.log("... directories : " + StringUtils.asString(directories));
            LogManager.log("...   initial : " + initialSize);
            LogManager.log("...     delta : " + deltaSize);
        }
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
                        for(File directory : directories) {
                            if (directory.exists()) {
                                update = true;
                            }
                        }
                        if(update) {
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
            }  finally {
                synchronized (LOCK) {
                    LOCK.notify();
                }
            }
            progress.setPercentage(Progress.COMPLETE);
            LogManager.log("... progress thread finished");
        }
        public void finish() {
            if(!isRunning()) return;
            synchronized (LOCK) {
                loop = false;
            }
            synchronized (LOCK) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e){
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
            for(File directory : directories) {
                if(directory.exists()) {
                    size+=FileUtils.getSize(directory);
                }
            }
            //LogManager.log("... size : " + size);
            long d = progress.COMPLETE * (size - initialSize) / deltaSize;
            //LogManager.log(".... real progress : " + d);
            d = progress.getPercentage() + (d  - progress.getPercentage() + 1) / 2;
            //LogManager.log("... bound progress : " + d);
            d = (d<0) ? 0 : (d > progress.COMPLETE ? progress.COMPLETE : d);
            if(((int)d) > progress.getPercentage()) {
                //LogManager.log("..... set progress : " + d);
                progress.setPercentage(d);
            }
        }
    }
    @Override
    public Text getLicense() {
        return null;
    }
// Constants
    public static final String WIZARD_COMPONENTS_URI =
            "resource:" + // NOI18N
            "org/netbeans/installer/products/jdk/wizard.xml"; // NOI18N
    
    public static final String JDK_INSTALLED_WINDOWS_PROPERTY =
            "jdk.win.installed";//NOI18N
    public static final String JRE_INSTALLED_WINDOWS_PROPERTY =
            "jre.win.installed";//NOI18N
    public static final String JAVADB_INSTALLED_WINDOWS_PROPERTY = 
            "javadb.win.installed";//NOI18N
    public static final String JAVADB_INSTALLER_LOCATION_PROPERTY =
            "javadb.msi.location";//NOI18N
    public static final String JDK_INSTALLER_FILE_NAME =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.jdk.installer.file");
    public static final String JRE_INSTALLER_FILE_NAME =
            "{jre-installer-file}";
    public static final String CAB_INSTALLER_FILE_SJ =
            "sj170030.cab";
    public static final String CAB_INSTALLER_FILE_SS =
            "ss170030.cab";
    public static final String CAB_INSTALLER_FILE_ST =
            "st170030.cab";
    public static final String CAB_INSTALLER_FILE_SZ =
            "sz170030.cab";
    public static final String ERROR_JDK_INSTALL_SCRIPT_RETURN_NONZERO_KEY =
            "CL.error.jdk.installation.return.nonzero";//NOI18N
    public static final String ERROR_JDK_UNINSTALL_SCRIPT_RETURN_NONZERO_KEY =
            "CL.error.jdk.uninstallation.return.nonzero";//NOI18N
    public static final String ERROR_JRE_UNINSTALL_SCRIPT_RETURN_NONZERO_KEY =
            "CL.error.jre.uninstallation.return.nonzero";//NOI18N
    public static final String ERROR_JRE_INSTALL_SCRIPT_RETURN_NONZERO_KEY =
            "CL.error.jre.installation.return.nonzero";//NOI18N
    public static final String ERROR_JAVADB_INSTALL_SCRIPT_RETURN_NONZERO_KEY =
            "CL.error.javadb.installation.return.nonzero";//NOI18N
    public static final String ERROR_JAVADB_UNINSTALL_SCRIPT_RETURN_NONZERO_KEY =
            "CL.error.javadb.uninstallation.return.nonzero";//NOI18N
    public static final String ERROR_INSTALL_JDK_ERROR_KEY =
            "CL.error.install.jdk.exception";//NOI18N
    public static final String ERROR_UNINSTALL_JDK_ERROR_KEY =
            "CL.error.uninstall.jdk.exception";//NOI18N
    public static final String ERROR_INSTALL_JRE_ERROR_KEY =
            "CL.error.install.jre.exception";//NOI18N
    public static final String ERROR_INSTALL_JAVADB_ERROR_KEY =
            "CL.error.install.javadb.exception";//NOI18N    
    public static final String ERROR_UNINSTALL_JAVADB_ERROR_KEY =
            "CL.error.uninstall.javadb.exception";//NOI18N        
    public static final String ERROR_UNINSTALL_JRE_ERROR_KEY =
            "CL.error.uninstall.jre.exception";//NOI18N
    public static final String ERROR_INSTALL_CANNOT_MOVE_DATA_KEY =
            "CL.error.install.cannot.move.data";//NOI18N
    public static final String PROGRESS_DETAIL_RUNNING_JDK_INSTALLER =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.progress.detail.install.jdk");
    public static final String PROGRESS_DETAIL_RUNNING_JRE_INSTALLER =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.progress.detail.install.jre");
    public static final String PROGRESS_DETAIL_RUNNING_JAVADB_INSTALLER =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.progress.detail.install.javadb");
    public static final String PROGRESS_DETAIL_RUNNING_JDK_UNINSTALLER =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.progress.detail.uninstall.jdk");
    public static final String PROGRESS_DETAIL_RUNNING_JRE_UNINSTALLER =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.progress.detail.uninstall.jre");
    public static final String PROGRESS_DETAIL_RUNNING_JAVADB_UNINSTALLER =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.progress.detail.uninstall.javadb");
    
    
    public static final String JDK_PATCH_DIRECTORY =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.jdk.patch.directory");//NOI18N
    public static final String JDK_DEFAULT_INSTALL_DIR =
            ResourceUtils.getString(ConfigurationLogic.class,
            "CL.jdk.install.dir");//NOI18N
    public static final String JRE_MSI_NAME =
            "jre.msi";//NOI18N
    public static final String JAVADB_MSI_NAME =
            "javadb.msi";//NOI18N
    public static final String SUN_JAVADB_DEFAULT_LOCATION =
            "$E{ProgramFiles}\\Sun\\JavaDB";
    public static final String NO_REGISTER_JDK_OPTION =
            "-noregister";
    public static final String RESTART_IS_REQUIRED_PROPERTY =
            "restart.required";
}
