/*
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

package org.netbeans.installer.utils.system;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.netbeans.installer.Installer;
import org.netbeans.installer.utils.EngineUtils;
import org.netbeans.installer.utils.helper.EnvironmentScope;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.system.shortcut.Shortcut;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.helper.ApplicationDescriptor;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.system.launchers.Launcher;
import org.netbeans.installer.utils.system.launchers.LauncherResource;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.cleaner.OnExitCleanerHandler;
import org.netbeans.installer.utils.system.cleaner.JavaOnExitCleanerHandler;
import org.netbeans.installer.utils.system.cleaner.SystemPropertyOnExitCleanerHandler;
import org.netbeans.installer.utils.system.launchers.LauncherProperties;
import org.netbeans.installer.utils.system.shortcut.LocationType;

/**
 *
 * @author Kirill Sorokin
 */
public abstract class NativeUtils {
    /////////////////////////////////////////////////////////////////////////////////
    // Static    
    protected static boolean nativeLibraryLoaded;
    private static HashSet<File> forbiddenDeletingFiles = new HashSet<File>();
    private static List <LauncherResource> uninstallerJVMs = new ArrayList <LauncherResource> ();
    private static Platform currentPlatform;    
    public static final String NATIVE_RESOURCE_SUFFIX = "native/"; // NOI18N
    public static final String NATIVE_JNILIB_RESOURCE_SUFFIX =
            NATIVE_RESOURCE_SUFFIX +
            "jnilib/"; // NOI18N
    public static final String NATIVE_LAUNCHER_RESOURCE_SUFFIX =
            NATIVE_RESOURCE_SUFFIX +
            "launcher/"; // NOI18N
    public static final String NATIVE_CLEANER_RESOURCE_SUFFIX =
            NATIVE_RESOURCE_SUFFIX +
            "cleaner/"; // NOI18N
    private static OnExitCleanerHandler cleanerHandler;
    
    protected abstract Platform getPlatform();
    
    public final Platform getCurrentPlatform() {
        if (currentPlatform == null) {
            currentPlatform = getPlatform();
        }        
        return currentPlatform;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    
    // constructor //////////////////////////////////////////////////////////////////
    protected NativeUtils() {
        // does nothing
    }
    
    // abstract /////////////////////////////////////////////////////////////////////
    public abstract boolean isSystem64Bit();
    
    public abstract boolean isCurrentUserAdmin() throws NativeException;
    
    public abstract File getDefaultApplicationsLocation() throws NativeException;
    
    public abstract long getFreeSpace(File file) throws NativeException;
    
    public abstract boolean isPathValid(String path);
    
    public abstract boolean isUNCPath(String path);
    
    public File getRoot(final File file) {
        File parent = file;
        while (parent.getParentFile() != null) {
            parent = parent.getParentFile();
        }
        
        return parent;
    }
    
    public abstract File getShortcutLocation(Shortcut shortcut, LocationType locationType) throws NativeException;
    
    public abstract File createShortcut(Shortcut shortcut, LocationType locationType) throws NativeException;
    
    public abstract void removeShortcut(Shortcut shortcut, LocationType locationType, boolean deleteEmptyParents) throws NativeException;
    
    public void addUninstallerJVM(LauncherResource jvm) {
        if(jvm!=null && !uninstallerJVMs.contains(jvm)) {
        uninstallerJVMs.add(jvm);
        }
    }
    protected Launcher createUninstaller(ApplicationDescriptor descriptor, boolean uninstall, Progress progress) throws IOException {
        LogManager.log("creating uninstaller...");
        
        final File engine = new File(descriptor.getInstallPath(),
                "uninstall.jar");
        try {
            EngineUtils.cacheEngine(engine, new Progress());
            
            final LauncherProperties props = new LauncherProperties();
            
            props.addJVM(new LauncherResource(false, SystemUtils.getCurrentJavaHome()));
            for(LauncherResource jvm : uninstallerJVMs) {
                props.addJVM(jvm);
            }
            props.addJar(new LauncherResource(true, engine));
            props.setJvmArguments(new String[]{
                "-Xmx256m",
                "-Xms64m",
                "-D" + Installer.LOCAL_DIRECTORY_PATH_PROPERTY +
                        "=" + Installer.getInstance().getLocalDirectory()});
            props.setMainClass(EngineUtils.getEngineMainClass().getName());
            
            if (uninstall) {
                props.setAppArguments(descriptor.getUninstallCommand());
                props.setOutput(
                        new File(descriptor.getInstallPath(), "uninstall"),
                        true);
            } else {
                props.setAppArguments(descriptor.getModifyCommand());
                props.setOutput(
                        new File(descriptor.getInstallPath(), "modify-install"),
                        true);
            }
            return SystemUtils.createLauncher(props, progress);
        } finally {
            FileUtils.deleteFile(engine);
        }
    }

    public abstract boolean openBrowser(URI uri);
    
    public abstract boolean isBrowseSupported();

    public abstract FilesList addComponentToSystemInstallManager(ApplicationDescriptor descriptor) throws NativeException;
    
    public abstract void removeComponentFromSystemInstallManager(ApplicationDescriptor descriptor) throws NativeException;
    
    public abstract String getEnvironmentVariable(String name, EnvironmentScope scope, boolean expand) throws NativeException;
    
    public abstract void setEnvironmentVariable(String name, String value, EnvironmentScope scope, boolean expand) throws NativeException;
    
    public abstract List<File> findIrrelevantFiles(File parent) throws IOException;
    
    public abstract List<File> findExecutableFiles(File parent) throws IOException;
    
    public abstract void correctFilesPermissions(File parent) throws IOException;
    
    public abstract void setPermissions(
            final File file,
            final int mode,
            final int change) throws IOException;
    
    public abstract int getPermissions(
            final File file) throws IOException;
    
    public boolean checkFileAccess(File file, boolean isReadNotModify) throws NativeException {
        return true;
    }
    public final void addDeleteOnExitFile(File file) {        
        getDeleteOnExitHandler().addDeleteOnExitFile(file);
    }
    
    public final void removeDeleteOnExitFile(File file) {
        getDeleteOnExitHandler().removeDeleteOnExitFile(file);
    }
    protected OnExitCleanerHandler newDeleteOnExitCleanerHandler() {
        return new JavaOnExitCleanerHandler();
    }
    protected OnExitCleanerHandler getDeleteOnExitHandler() {
        if(cleanerHandler == null) {
            if(SystemPropertyOnExitCleanerHandler.isSet()) {
                cleanerHandler = new SystemPropertyOnExitCleanerHandler();
            } else {
                cleanerHandler = newDeleteOnExitCleanerHandler();
                Runtime.getRuntime().addShutdownHook(cleanerHandler);
            }            
        }   
        return cleanerHandler;
    }
    
    /**
     * Return roots for specified files. If it is used with no arguments, 
     * returns all available roots.
     * But when files are not specified, on Unix systems there is
     * a possibility of installer freeze because of "df -k" command -
     * when unavailable nfs filesystem is mounted. 
     * 
     * @param files - files for which we want get roots
     * @return List of roots
     * @throws IOException 
     */
    public abstract List<File> getFileSystemRoots(String... files) throws IOException;
    
    // protected ////////////////////////////////////////////////////////////////////
    protected void loadNativeLibrary(String path) throws NativeException {
        LogManager.logIndent("loading jni library");
        LogManager.log("library resource path: " + path);
        try {
            if (path != null) {
                final File tempDir = SystemUtils.getTempDirectory();
                if(!tempDir.exists() && !tempDir.mkdirs()) {
                    throw new NativeException("Cannot create temporary directory " + tempDir.getAbsolutePath());
                }
                File file = null;
                try {
                    file = FileUtils.createTempFile(tempDir);
                } catch (IOException e) {
                    throw new NativeException("Cannot create temporary file for native library at " + tempDir.getAbsolutePath(), e);
                }

                InputStream input = getClass().getClassLoader().getResourceAsStream(path);
                if (input == null) {
                    throw new NativeException("Cannot find native library at resource " + path);//NOI18N
                }
                try {
                    LogManager.log("library file path: " + file.getAbsolutePath());
                    FileUtils.writeFile(file, input);
                } catch (IOException e) {
                    file.delete();
                    throw new NativeException("Cannot write native library (" + path + ") to temporary file " + file.getAbsolutePath(), e);
                } finally {
                    try {
                        input.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }
                try {
                    System.load(file.getAbsolutePath());
                    LogManager.log("... successfully loaded the library");
                    nativeLibraryLoaded = true;
                    addDeleteOnExitFile(file);
                } catch (UnsatisfiedLinkError e) {
                    LogManager.log("... failed loading the library", e);
                    final String message = e.getMessage();
                    //special handling for #163022
                    if (message != null && message.contains("failed to map segment from shared object")) {
                        throw new NativeException("Could not load library from temporary directory which is located on the filesystem mounted with \"noexec\" option:\n" +
                                tempDir.getAbsolutePath() + 
                                "\n\nTry to use other temporary directory.", e);
                    } else {
                        throw new NativeException("Cannot load native library from path: " + path, e);
                    }
                }
            }
        } finally {
            LogManager.unindent();
        }

    }
    
    protected void initializeForbiddenFiles(String ... filepaths) {
        for (String path : filepaths) {
            if(path!=null) {
                File file = new File(path);
                if(file.exists() && !forbiddenDeletingFiles.contains(file)) {
                    forbiddenDeletingFiles.add(file);
                }
            }
        }
    }
    
    public boolean isDeletingAllowed(File file) {
        return !(forbiddenDeletingFiles.contains(file));
    }
    
    public static  final int FA_MODE_SET = 1;
    public static  final int FA_MODE_ADD = 2;
    public static  final int FA_MODE_REMOVE = 4;
}
