/*
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

package org.netbeans.installer.product.components;

import org.netbeans.installer.utils.cli.options.ForceUninstallOption;
import org.netbeans.installer.utils.cli.options.TargetOption;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.netbeans.installer.Installer;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.product.RegistryNode;
import org.netbeans.installer.product.dependencies.Conflict;
import org.netbeans.installer.product.dependencies.InstallAfter;
import org.netbeans.installer.product.dependencies.Requirement;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.helper.DependencyType;
import org.netbeans.installer.utils.helper.DetailedStatus;
import org.netbeans.installer.utils.helper.RemovalMode;
import org.netbeans.installer.utils.cli.*;
import org.netbeans.installer.utils.helper.Status;
import org.netbeans.installer.utils.FileProxy;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.XMLUtils;
import org.netbeans.installer.utils.exceptions.NativeException;
import org.netbeans.installer.utils.FileUtils;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.exceptions.XMLException;
import org.netbeans.installer.utils.helper.FileEntry;
import org.netbeans.installer.utils.helper.FilesList;
import org.netbeans.installer.utils.helper.NbiClassLoader;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.exceptions.FinalizationException;
import org.netbeans.installer.utils.exceptions.InitializationException;
import org.netbeans.installer.utils.exceptions.InstallationException;
import org.netbeans.installer.utils.exceptions.UninstallationException;
import org.netbeans.installer.utils.helper.Platform;
import org.netbeans.installer.utils.StringUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.exceptions.ParseException;
import org.netbeans.installer.utils.helper.ApplicationDescriptor;
import org.netbeans.installer.utils.helper.Version;
import org.netbeans.installer.utils.helper.ExtendedUri;
import org.netbeans.installer.utils.helper.Dependency;
import org.netbeans.installer.utils.helper.Text;
import org.netbeans.installer.utils.progress.CompositeProgress;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.system.UnixNativeUtils;
import org.netbeans.installer.wizard.components.WizardComponent;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author Kirill Sorokin
 */
public final class Product extends RegistryNode implements StatusInterface {
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private Version version;
    private List<Platform> supportedPlatforms;
    
    private Status initialStatus;
    private Status currentStatus;
    private boolean reinstallationForced;
    
    private List<ExtendedUri> logicUris;
    private List<ExtendedUri> dataUris;
    
    private List<String> features;
    
    private long requiredDiskSpace;
    
    private List<Dependency> dependencies;
    
    private NbiClassLoader classLoader;
    private ProductConfigurationLogic configurationLogic;
    
    private Throwable installationError;
    private List<Throwable> installationWarnings;
    
    private Throwable uninstallationError;
    private List<Throwable> uninstallationWarnings;
    
    private FilesList installedFiles;
    
    private InstallationPhase installationPhase;
    
    // constructor //////////////////////////////////////////////////////////////////
    public Product() {
        supportedPlatforms = new ArrayList<Platform>();
        logicUris          = new ArrayList<ExtendedUri>();
        dataUris           = new ArrayList<ExtendedUri>();
        dependencies       = new ArrayList<Dependency>();
    }
    
    // essential functionality //////////////////////////////////////////////////////
    public void install(final Progress progress) throws InstallationException {
        LogManager.logIndent("Start installation of " + getDisplayName() + "(" + getUid() + "/" + getVersion()+")");
        final CompositeProgress totalProgress = new CompositeProgress();
        final CompositeProgress unjarProgress = new CompositeProgress();
        final Progress          logicProgress = new Progress();
        
        // initialization phase ////////////////////////////////////////////////
        installationPhase = InstallationPhase.INITIALIZATION;
        
        // load the component's configuration logic (it should be already
        // there, but we need to be sure)
        try {
            getLogic();
        } catch (InitializationException e) {
            throw new InstallationException(
                    ResourceUtils.getString(Product.class,
                    ERROR_CANNOT_INITIALIZE_PRODUCT_KEY, getDisplayName()), e);
        }
        
        totalProgress.addChild(
                unjarProgress,
                Progress.COMPLETE - configurationLogic.getLogicPercentage());
        totalProgress.addChild(
                logicProgress,
                configurationLogic.getLogicPercentage());
        totalProgress.synchronizeTo(progress);
        totalProgress.synchronizeDetails(true);
        
        // check whether the installation location was set, if it's not we
        // cannot continue
        if (getInstallationLocation() == null) {
            throw new InstallationException(
                    ResourceUtils.getString(Product.class,
                    ERROR_INSTALLATION_LOCATION_NOT_SET_KEY, 
                    getDisplayName()));
        } else  if (getInstallationLocation().equals(
                new File(StringUtils.EMPTY_STRING))) {
            throw new InstallationException(
                    ResourceUtils.getString(Product.class,
                    ERROR_INSTALLATION_LOCATION_SET_EMPTY_KEY, 
                    getDisplayName()));
        }
        
        // initialize the local cache directory
        final File cache = getLocalCache();
        if (!cache.exists()) {
            if (!cache.mkdirs()) {
                throw new InstallationException(
                        ResourceUtils.getString(Product.class,
                        ERROR_CANNOT_CREATE_PRODUCT_CACHE_DIR_KEY, cache, getDisplayName()));
            }
        } else if (!cache.isDirectory()) {
            throw new InstallationException(
                    ResourceUtils.getString(Product.class,
                    ERROR_CACHE_NOT_DIRECTORY_KEY, cache, getDisplayName()));
        }
        
        // initialize the files list
        installedFiles = new FilesList();
        
        // check for cancel status
        if (progress.isCanceled()) return;
        
        // extraction phase /////////////////////////////////////////////////////////
        installationPhase = InstallationPhase.EXTRACTION;
        
        totalProgress.setTitle(StringUtils.format(
                MESSAGE_INSTALLATION_STRING, getDisplayName()));
        
        final File contentsDir = new File(getInstallationLocation(), "Contents");
        final File macosDir = new File(contentsDir, "MacOS");
        final File resourcesDir = new File(contentsDir, "Resources");
        final File infoplist = new File(contentsDir, "Info.plist");
        final File ds_store = new File(getInstallationLocation().getParentFile(), ".DS_Store");
        
        // if we're running on macos x and the configuraion logic tells us that the
        // product should be automatically wrapped, we first create the required
        // directories structure and then extract the product
        if (SystemUtils.isMacOS() && configurationLogic.wrapForMacOs()) {
            setProperty(APPLICATION_LOCATION_PROPERTY,
                    getInstallationLocation().getAbsolutePath());
            setInstallationLocation(new File(resourcesDir,
                    getInstallationLocation().getName().replaceAll("\\.app$",
                    StringUtils.EMPTY_STRING)));
            
            final UnixNativeUtils utils =
                    (UnixNativeUtils) SystemUtils.getNativeUtils();
            
            try {
                installedFiles.add(FileUtils.mkdirs(contentsDir));
                installedFiles.add(FileUtils.mkdirs(resourcesDir));
                installedFiles.add(FileUtils.mkdirs(macosDir));
                
                final String executableName = "executable"; //NOI18N
                
                installedFiles.add(utils.createSymLink(
                        new File(macosDir, executableName),
                        new File(getInstallationLocation(), configurationLogic.getExecutable())));
                
                final String iconName = "icon.icns"; //NOI18N
                
                if(configurationLogic.getIcon() != null) {
                    installedFiles.add(utils.createSymLink(
                        new File(resourcesDir, iconName),
                        new File(getInstallationLocation(), configurationLogic.getIcon())));
                }
                
                installedFiles.add(FileUtils.writeFile(infoplist, StringUtils.format(
                        INFO_PLIST_STUB,
                        getDisplayName(),
                        getVersion().toString(),
                        getVersion().toMinor(),
                        executableName,
                        iconName)));
                
                //Fix #172000: NetBeans folder stay at Applications after uninstallation		
                //installedFiles.add(ds_store);

            } catch (IOException e) {
                throw new InstallationException(
                        ResourceUtils.getString(Product.class,
                        ERROR_CANNOT_WRAP_FOR_MACOS_KEY), e);
            }
        }

        // check for cancel status
        if (progress.isCanceled()) return;
        
        if(dataUris.size()>0) {
        LogManager.log("... extracting files from the data archives");
        // extract each of the defined installation data files
        unjarProgress.setPercentage(Progress.COMPLETE % dataUris.size());
        unjarProgress.synchronizeDetails(true);
        for (ExtendedUri uri: dataUris) {
            final Progress currentProgress = new Progress();
            unjarProgress.addChild(
                    currentProgress,
                    Progress.COMPLETE / dataUris.size());
            
            // get the uri of the current data file
            final URI dataUri = uri.getLocal();
            if (dataUri == null) {
                throw new InstallationException(
                        ResourceUtils.getString(Product.class,
                        ERROR_DATA_NOT_CACHED_KEY, getDisplayName()));
            }
            
            // convert it to a file and do some additional checks
            final File dataFile = new File(uri.getLocal());
            if (!dataFile.exists()) {
                throw new InstallationException(
                        ResourceUtils.getString(Product.class,
                        ERROR_DATA_NOT_CACHED_KEY, getDisplayName()));
            }
            
            // exract it and add the files to the installed files list
            try {
                installedFiles.add(FileUtils.unjar(
                        dataFile,
                        getInstallationLocation(),
                        currentProgress));
            } catch (IOException e) {
                if (e.getMessage().equals("Not enough space")) {
                    throw new InstallationException(
                            ResourceUtils.getString(Product.class,
                            ERROR_NOT_ENOUGH_SPACE_KEY), e);
                }
                throw new InstallationException(
                        ResourceUtils.getString(Product.class,
                        ERROR_CANNOT_EXTRACT_DATA_KEY,
                        getDisplayName()),
                        e);
            } catch (XMLException e) {
                throw new InstallationException(
                        ResourceUtils.getString(Product.class,
                        ERROR_CANNOT_EXTRACT_DATA_KEY,
                        getDisplayName()),
                        e);
            }
            
            // finally remove the data file
            try {
                FileProxy.getInstance().deleteFile(uri);
            } catch (IOException e) {
                throw new InstallationException(
                        ResourceUtils.getString(Product.class,
                        ERROR_CANNOT_CLEAR_DATA_CACHE_KEY,
                        dataFile),
                        e);
            }
            // check for cancel status
            if (progress.isCanceled()) break;
        }
        } else {
            LogManager.log("... no data archives assigned to this product");
            unjarProgress.setPercentage(Progress.COMPLETE);
        }

        LogManager.log("... saving legal artifacts if required");
        // create legal/docs artifacts
        progress.setDetail(StringUtils.format(MESSAGE_LEGAL_ARTIFACTS_STRING));
        try {
            saveLegalArtifacts();
        } catch (IOException e) {
            addInstallationWarning(e);
        }
        
        // check for cancel status
        if (progress.isCanceled()) return;
        
        // custom configuration phase ///////////////////////////////////////////////
        installationPhase = InstallationPhase.CUSTOM_LOGIC;
        
        totalProgress.setTitle(StringUtils.format(
                MESSAGE_CONFIGURATION_STRING, getDisplayName()));
        
        // run custom configuration logic
        progress.setDetail(StringUtils.format(MESSAGE_RUN_LOGIC_STRING));
        
        LogManager.log("... running installation logic");
        configurationLogic.install(logicProgress);
        logicProgress.setPercentage(Progress.COMPLETE);
        progress.setDetail(StringUtils.EMPTY_STRING);
        
        // check for cancel status
        if (progress.isCanceled()) return;
        
        // finalization phase ///////////////////////////////////////////////////////
        installationPhase = InstallationPhase.FINALIZATION;
        LogManager.log("... register in system, create uninstaller, etc");
        // register the component in the system install manager
        if (configurationLogic.registerInSystem()) {
            try {
                progress.setDetail(StringUtils.format(MESSAGE_SYSTEM_REGISTRATION_STRING));
                installedFiles.add(SystemUtils.addComponentToSystemInstallManager(getApplicationDescriptor()));
            } catch (NativeException e) {
                throw new InstallationException(ResourceUtils.getString(Product.class,
                        ERROR_SYSTEM_INTEGRATION_FAILER_KEY,
                        getDisplayName()),e);
            } catch (IOException e) {
                throw new InstallationException(ResourceUtils.getString(Product.class,
                        ERROR_SYSTEM_INTEGRATION_FAILER_KEY,
                        getDisplayName()),e);
            }
        }
        LogManager.log("... save installation files list");
        // save the installed files list
        progress.setDetail(StringUtils.format(MESSAGE_SAVE_INSTALL_FILES_LIST_STRING));
        try {
            installedFiles.saveXmlGz(getInstalledFilesList());
        } catch (XMLException e) {
            throw new InstallationException(
                    ResourceUtils.getString(Product.class,
                    ERROR_CANNOT_SAVE_FILES_LIST_KEY), e);
        }
        
        installationPhase = InstallationPhase.COMPLETE;
        progress.setPercentage(Progress.COMPLETE);
        progress.setDetail(StringUtils.EMPTY_STRING);
        setStatus(Status.INSTALLED);
        LogManager.logUnindent("... finished installation of " + getDisplayName() + "(" + getUid() + "/" + getVersion()+")");
    }
    
    public void rollback(final Progress progress) throws UninstallationException {
        final CompositeProgress totalProgress = new CompositeProgress();
        final Progress          logicProgress = new Progress();
        final Progress          eraseProgress = new Progress();
        LogManager.logIndent("Start rollback of " + getDisplayName() + "(" + getUid() + "/" + getVersion()+")");
        // initialization ///////////////////////////////////////////////////////////
        
        // load the component's configuration logic (it should be already
        // there, but we need to be sure)
        try {
            getLogic();
        } catch (InitializationException e) {
            throw new UninstallationException(
                    ResourceUtils.getString(Product.class,
                    ERROR_CANNOT_INITIALIZE_PRODUCT_KEY, getDisplayName()), e);
        }
        
        int logicChunk = (int) (progress.getPercentage() * (
                (float) configurationLogic.getLogicPercentage() /
                (float) Progress.COMPLETE));
        int eraseChunk = (int) (progress.getPercentage() * (1. - (
                (float) configurationLogic.getLogicPercentage() /
                (float) Progress.COMPLETE)));
        
        totalProgress.setPercentage(Progress.COMPLETE - logicChunk - eraseChunk);
        totalProgress.addChild(logicProgress, logicChunk);
        totalProgress.addChild(eraseProgress, eraseChunk);
        totalProgress.synchronizeDetails(true);
        totalProgress.reverseSynchronizeTo(progress);
        
        // rollback /////////////////////////////////////////////////////////////////
        
        // the starting point is chosen depending on the stage at which the
        // installation process was canceled, or failed; note that we intentionally
        // fall through all these cases, as they should be executed exactly in this
        // order and the only unclear point is where to start
        switch (installationPhase) {
            case COMPLETE:
            case FINALIZATION:
                LogManager.log("... deleting installed files files");
                try {
                    FileUtils.deleteFile(getInstalledFilesList());
                } catch (IOException e) {
                    ErrorManager.notifyWarning(ResourceUtils.getString(Product.class,
                            ERROR_CANNOT_DELETE_FILES_LIST_KEY), e);
                }
                
                if (configurationLogic.registerInSystem()) {
                    LogManager.log("... removing system integration");
                    try {
                        SystemUtils.removeComponentFromSystemInstallManager(getApplicationDescriptor());
                    } catch (NativeException e) {
                        ErrorManager.notifyWarning(
                                ResourceUtils.getString(Product.class,
                                ERROR_CANNOT_REMOVE_FROM_SYSTEM_KEY, getDisplayName()), e);
                    }
                }
                
            case CUSTOM_LOGIC:
                LogManager.log("... running uninstallation logic");
                configurationLogic.uninstall(logicProgress);
                
            case EXTRACTION:
                logicProgress.setPercentage(Progress.COMPLETE);
                LogManager.log("... deleting installed files");
                // remove installation files
                int total   = installedFiles.getSize();
                int current = 0;
                
                for (FileEntry entry: installedFiles) {
                    current++;
                    
                    File file = entry.getFile();
                    
                    eraseProgress.setDetail(StringUtils.format(MESSAGE_DELETE_STRING, file));
                    eraseProgress.setPercentage(Progress.COMPLETE * current / total);
                    
                    try {
                        FileUtils.deleteFile(file);
                    } catch (IOException e) {
                        ErrorManager.notifyWarning(
                                ResourceUtils.getString(Product.class,
                                ERROR_CANNOT_DELETE_FILE_KEY), e);
                    }
                }
                
            case INITIALIZATION:
                eraseProgress.setPercentage(Progress.COMPLETE);
                // for initialization we don't need to do anything
                
            default:
                // default, nothing should be done here
        }
        setStatus(Status.NOT_INSTALLED);
        LogManager.logUnindent("... finished rollbacking of " + getDisplayName() + "(" + getUid() + "/" + getVersion()+")");
    }
    
    public void uninstall(final Progress progress) throws UninstallationException {
        LogManager.logIndent("Start uninstallation of " + getDisplayName() + "(" + getUid() + "/" + getVersion()+")");
        final CompositeProgress totalProgress = new CompositeProgress();
        final Progress logicProgress = new Progress();
        final Progress eraseProgress = new Progress();
        
        // initialization phase /////////////////////////////////////////////////////
        
        // load the component's configuration logic (it should be already
        // there, but we need to be sure)
        try {
            getLogic();
        } catch (InitializationException e) {
            throw new UninstallationException(
                    ResourceUtils.getString(Product.class,
                    ERROR_CANNOT_INITIALIZE_PRODUCT_KEY, getDisplayName()), e);
        }
        
        totalProgress.addChild(
                logicProgress,
                configurationLogic.getLogicPercentage());
        totalProgress.addChild(
                eraseProgress,
                Progress.COMPLETE - configurationLogic.getLogicPercentage());
        totalProgress.synchronizeTo(progress);
        totalProgress.synchronizeDetails(true);
        
        // load the installed files list
        try {
            installedFiles = new FilesList().loadXmlGz(getInstalledFilesList());
        } catch (XMLException e) {
            throw new UninstallationException(
                    ResourceUtils.getString(Product.class,
                    ERROR_CANNOT_GET_FILES_LIST_KEY), e);
        }
        
        // custom logic phase ///////////////////////////////////////////////////////
        progress.setTitle(StringUtils.format(
                MESSAGE_UNCONFIGURATION_STRING, getDisplayName()));
        
        // run custom unconfiguration logic
        LogManager.log("... running uninstallation logic");
        configurationLogic.uninstall(logicProgress);
        logicProgress.setPercentage(Progress.COMPLETE);
        progress.setDetail(StringUtils.EMPTY_STRING);
        
        // files deletion phase /////////////////////////////////////////////////////
        progress.setTitle(StringUtils.format(
                MESSAGE_UNINSTALLATION_STRING, getDisplayName()));
        
        // remove installation files
        LogManager.log("... removing installation files");
        if (configurationLogic.getRemovalMode() == RemovalMode.ALL) {
            try {
                File startPoint = getInstallationLocation();
                if(SystemUtils.isMacOS() && configurationLogic.wrapForMacOs()) {
                    startPoint = startPoint.
                            getParentFile().
                            getParentFile().
                            getParentFile();
                }
                FileUtils.deleteFile(startPoint, true, eraseProgress);
            } catch (IOException e) {
                addUninstallationWarning(new UninstallationException(
                        ResourceUtils.getString(Product.class,
                        ERROR_CANNOT_DELETE_FILE_KEY),
                        e));
            }
        } else {
            try {
                FileUtils.deleteFiles(installedFiles, eraseProgress);
            } catch (IOException e) {
                addUninstallationWarning(new UninstallationException(
                        ResourceUtils.getString(Product.class,
                        ERROR_CANNOT_DELETE_FILE_KEY),
                        e));
            }
        }
        LogManager.log("... removing the system integration");
        // remove the component from the native install manager
        if (configurationLogic.registerInSystem()) {
            try {
                SystemUtils.removeComponentFromSystemInstallManager(getApplicationDescriptor());
            } catch (NativeException e) {
                addUninstallationWarning(new UninstallationException(
                        ResourceUtils.getString(Product.class,
                        ERROR_CANNOT_REMOVE_FROM_SYSTEM_KEY,
                        getDisplayName()), e));
            }
        }
        LogManager.log("... removing installation files list");
        progress.setDetail(StringUtils.EMPTY_STRING);
        // remove the files list
        try {
            FileUtils.deleteFile(getInstalledFilesList());
        } catch (IOException e) {
            addUninstallationWarning(new UninstallationException(
                    ResourceUtils.getString(Product.class,
                    ERROR_CANNOT_DELETE_FILES_LIST_KEY), e));
        }
        
        progress.setPercentage(Progress.COMPLETE);
        setStatus(Status.NOT_INSTALLED);
        LogManager.logUnindent("...finished uninstallation of " + getDisplayName() + "(" + getUid() + "/" + getVersion()+")");
    }
    
    // configuration logic //////////////////////////////////////////////////////////
    public List<ExtendedUri> getLogicUris() {
        return logicUris;
    }
    
    public void downloadLogic(final Progress progress) throws DownloadException {
        final CompositeProgress overallProgress = new CompositeProgress();
        
        final int percentageChunk = Progress.COMPLETE / logicUris.size();
        final int percentageLeak  = Progress.COMPLETE % logicUris.size();
        
        overallProgress.setPercentage(percentageLeak);
        overallProgress.synchronizeTo(progress);
        overallProgress.synchronizeDetails(true);
        
        for (ExtendedUri uri: logicUris) {
            final Progress currentProgress = new Progress();
            overallProgress.addChild(currentProgress, percentageChunk);
            
            final File cache = FileProxy.getInstance().getFile(
                    uri.getRemote(),
                    currentProgress);
            uri.setLocal(cache.toURI());
        }
    }
    
    public boolean isLogicDownloaded() {
        for (ExtendedUri uri: logicUris) {
            if (uri.getLocal() == null) {
                return false;
            }
        }
        
        return true;
    }
    
    public ProductConfigurationLogic getLogic() throws InitializationException {
        if (configurationLogic != null) {
            return configurationLogic;
        }
        
        if (!isLogicDownloaded()) {
            throw new InitializationException(
                    ResourceUtils.getString(Product.class,
                    ERROR_LOGIC_NOT_YET_DOWNLOADED_KEY, getDisplayName()));
        }
        
        try {
            String classname = null;
            for (ExtendedUri uri: logicUris) {
                classname = FileUtils.getJarAttribute(
                        new File(uri.getLocal()),
                        MANIFEST_LOGIC_CLASS);
                
                if (classname != null) {
                    break;
                }
            }
            
            classLoader = new NbiClassLoader(logicUris);
            
            configurationLogic = (ProductConfigurationLogic) classLoader.
                    loadClass(classname).newInstance();
            configurationLogic.setProduct(this);
            
            return configurationLogic;
        } catch (IOException e) {
            throw new InitializationException(ResourceUtils.getString(Product.class,
                    ERROR_CANNOT_LOAD_LOGIC_KEY, getDisplayName()), e);
        } catch (ClassNotFoundException e) {
            throw new InitializationException(ResourceUtils.getString(Product.class,
                    ERROR_CANNOT_LOAD_LOGIC_KEY, getDisplayName()), e);
        } catch (InstantiationException e) {
            throw new InitializationException(ResourceUtils.getString(Product.class,
                    ERROR_CANNOT_LOAD_LOGIC_KEY, getDisplayName()), e);
        } catch (IllegalAccessException e) {
            throw new InitializationException(ResourceUtils.getString(Product.class,
                    ERROR_CANNOT_LOAD_LOGIC_KEY, getDisplayName()), e);
        } catch (NoClassDefFoundError e) {
            throw new InitializationException(ResourceUtils.getString(Product.class,
                    ERROR_CANNOT_LOAD_LOGIC_KEY, getDisplayName()), e);
        } catch (UnsupportedClassVersionError e) {
            throw new InitializationException(ResourceUtils.getString(Product.class,
                    ERROR_CANNOT_LOAD_LOGIC_KEY, getDisplayName()), e);
        } 
    }
    
    // installation data ////////////////////////////////////////////////////////////
    public List<ExtendedUri> getDataUris() {
        return dataUris;
    }
    
    public void downloadData(final Progress progress) throws DownloadException {
        final CompositeProgress overallProgress = new CompositeProgress();
        if (dataUris.size() > 0) {
            final int percentageChunk = Progress.COMPLETE / dataUris.size();
            final int percentageLeak = Progress.COMPLETE % dataUris.size();

            overallProgress.setPercentage(percentageLeak);
            overallProgress.synchronizeTo(progress);
            overallProgress.synchronizeDetails(true);

            for (ExtendedUri uri : dataUris) {
                final Progress currentProgress = new Progress();
                overallProgress.addChild(currentProgress, percentageChunk);

                final File cache = FileProxy.getInstance().getFile(
                        uri.getRemote(),
                        currentProgress);
                uri.setLocal(cache.toURI());
                if (progress.isCanceled()) return;
            }
        } else {
            overallProgress.setPercentage(Progress.COMPLETE);
            overallProgress.synchronizeTo(progress);
        }
    }
    
    public boolean isDataDownloaded() {
        for (ExtendedUri uri: dataUris) {
            if (uri.getLocal() == null) {
                return false;
            }
        }
        
        return true;
    }
    
    // wizard ///////////////////////////////////////////////////////////////////////
    public List<WizardComponent> getWizardComponents() {
        try {
            return getLogic().getWizardComponents();
        } catch (InitializationException e) {
            ErrorManager.notifyError(ResourceUtils.getString(
                    Product.class, ERROR_CANNOT_GET_WIZARD_COMPONENTS_KEY), e);
        }
        
        return null;
    }
    
    // status ///////////////////////////////////////////////////////////////////////
    public Status getStatus() {
        return currentStatus;
    }

    public void setStatus(final Status status) {
        if (initialStatus == null) {
            initialStatus = status;
        }

        currentStatus = status;

        if (initialStatus == Status.INSTALLED &&
                currentStatus == Status.TO_BE_INSTALLED) {
            reinstallationForced = true;
        }

    }
    
    public boolean hasStatusChanged() {
        return currentStatus != initialStatus;
    }
    
    public DetailedStatus getDetailedStatus() {
        if (getStatus() == Status.INSTALLED) {
            if (getUninstallationError() != null) {
                return DetailedStatus.FAILED_TO_UNINSTALL;
            }
            if ((hasStatusChanged() || reinstallationForced)
                    && (getInstallationWarnings() != null)) {
                return DetailedStatus.INSTALLED_WITH_WARNINGS;
            }
            if (hasStatusChanged() || reinstallationForced) {
                return DetailedStatus.INSTALLED_SUCCESSFULLY;
            }
        }
        
        if (getStatus() == Status.NOT_INSTALLED) {
            if (getInstallationError() != null) {
                return DetailedStatus.FAILED_TO_INSTALL;
            }
            if (hasStatusChanged() && (getUninstallationWarnings() != null)) {
                return DetailedStatus.UNINSTALLED_WITH_WARNINGS;
            }
            if (hasStatusChanged()) {
                return DetailedStatus.UNINSTALLED_SUCCESSFULLY;
            }
        }
        
        return null;
    }
    
    // dependencies /////////////////////////////////////////////////////////////////
    public List<Dependency> getDependencies() {
        return dependencies;
    }
    @Deprecated
    public List<Dependency> getDependencies(final DependencyType ... types) {
        Class [] classes = new Class[types.length];
        for(int i=0;i<types.length;i++) {
            classes[i] = toDependencyClass(types[i]);
        }
        return getDependencies(classes);
    }
    
    @Deprecated
    private Class <? extends Dependency> toDependencyClass(DependencyType type) {
        switch (type) {
            case REQUIREMENT:
                return Requirement.class;
            case CONFLICT :
                return Conflict.class;
            case INSTALL_AFTER:
                return InstallAfter.class;
            default :
                return null;
        }
    }
    
    public List<Dependency> getDependencies(Class ... dependencyClasses) {
        final List<Dependency> filtered = new ArrayList<Dependency>();
        
        for (Dependency dependency: dependencies) {
            for (Class clazz: dependencyClasses) {
                //if (clazz.isInstance(dependency)) {
                if (clazz.isInstance(dependency)) {
                    filtered.add(dependency);
                    break;
                }
            }
        }
        
        return filtered;
    }
    
    public boolean satisfies(final Dependency dependency) {
        return dependency.satisfies(this);
    }
    
    public List<Dependency> getDependencyByUid(String dependentUid) {
        final List<Dependency> filtered = new ArrayList<Dependency>();
        
        for (Dependency dependency: dependencies) {
            if (dependency.getUid().equals(dependentUid)) {
                filtered.add(dependency);
            }
        }
        
        return filtered;
    }
    
    // system requirements //////////////////////////////////////////////////////////
    public long getRequiredDiskSpace() {
        return requiredDiskSpace;
    }
    
    // install-time error/warnings //////////////////////////////////////////////////
    public Throwable getInstallationError() {
        return installationError;
    }
    
    public void setInstallationError(final Throwable error) {
        installationError = error;
    }
    
    public List<Throwable> getInstallationWarnings() {
        return installationWarnings;
    }
    
    public void addInstallationWarning(final Throwable warning) {
        if (installationWarnings == null) {
            installationWarnings = new ArrayList<Throwable>();
        }
        
        installationWarnings.add(warning);
    }
    
    // uninstall-time error/warnings ////////////////////////////////////////////////
    public Throwable getUninstallationError() {
        return uninstallationError;
    }
    
    public void setUninstallationError(final Throwable error) {
        uninstallationError = error;
    }
    
    public List<Throwable> getUninstallationWarnings() {
        return uninstallationWarnings;
    }
    
    public void addUninstallationWarning(final Throwable warning) {
        if (uninstallationWarnings == null) {
            uninstallationWarnings = new ArrayList<Throwable>();
        }
        
        uninstallationWarnings.add(warning);
    }
    
    // node <-> dom /////////////////////////////////////////////////////////////////
    protected String getTagName() {
        return PRODUCT_TAG_NAME;
    }
    
    protected Element saveToDom(final Element element) throws FinalizationException {
        super.saveToDom(element);
        
        final Document document = element.getOwnerDocument();
        
        element.setAttribute(VERSION_TAG_NAME, version.toString());
        element.setAttribute(PLATFORMS_TAG_NAME,
                StringUtils.asString(supportedPlatforms, StringUtils.SPACE));
        element.setAttribute(STATUS_TAG_NAME, currentStatus.toString());
        element.setAttribute(FEATURES_TAG_NAME,
                StringUtils.asString(features, StringUtils.SPACE));
        
        element.appendChild(XMLUtils.saveExtendedUrisList(
                logicUris,
                document.createElement(CONFIGURATION_LOGIC_TAG_NAME)));//NOI18N
        
        element.appendChild(XMLUtils.saveExtendedUrisList(
                dataUris,
                document.createElement(INSTALLATION_DATA_TAG_NAME)));//NOI18N
        
        final Element systemRequirementsElement =
                document.createElement(SYSTEM_REQUIREMENTS_TAG_NAME);//NOI18N
        
        final Element diskSpaceElement =
                document.createElement(DISK_SPACE_TAG_NAME);//NOI18N
        diskSpaceElement.setTextContent(Long.toString(requiredDiskSpace));
        systemRequirementsElement.appendChild(diskSpaceElement);
        
        element.appendChild(systemRequirementsElement);
        
        if (dependencies.size() > 0) {
            element.appendChild(XMLUtils.saveDependencies(
                    dependencies,
                    document.createElement(DEPENDENCIES_TAG_NAME)));//NOI18N
        }
        
        return element;
    }
    
    public Product loadFromDom(final Element element) throws InitializationException {
        
        super.loadFromDom(element);
        
        Element child;
        
        try {
            version =
                    Version.getVersion(element.getAttribute(VERSION_TAG_NAME));
            supportedPlatforms =
                    StringUtils.parsePlatforms(element.getAttribute(PLATFORMS_TAG_NAME));
            
            initialStatus =
                    StringUtils.parseStatus(element.getAttribute(STATUS_TAG_NAME));
            currentStatus =
                    initialStatus;
            
            features = StringUtils.asList(element.getAttribute(FEATURES_TAG_NAME),
                    StringUtils.SPACE);
            
            logicUris.addAll(XMLUtils.parseExtendedUrisList(XMLUtils.getChild(
                    element, CONFIGURATION_LOGIC_TAG_NAME)));
            
            dataUris.addAll(XMLUtils.parseExtendedUrisList(XMLUtils.getChild(
                    element, INSTALLATION_DATA_TAG_NAME)));
            
            requiredDiskSpace = Long.parseLong(XMLUtils.getChild(
                    element,
                    SYSTEM_REQUIREMENTS_TAG_NAME + "/" + DISK_SPACE_TAG_NAME).
                    getTextContent());
            
            child = XMLUtils.getChild(element, DEPENDENCIES_TAG_NAME);
            if (child != null) {
                dependencies.addAll(XMLUtils.parseDependencies(child));
            }
        } catch (ParseException e) {
            throw new InitializationException(ResourceUtils.getString(
                    Product.class,
                    ERROR_CANNOT_LOAD_PRODUCT_KEY,
                    getDisplayName()),
                    e);
        }
        
        return this;
    }
    
    // essential getters/setters ////////////////////////////////////////////////////
    public Version getVersion() {
        return version;
    }
    
    public List<Platform> getPlatforms() {
        return supportedPlatforms;
    }
    
    public List<String> getFeatures() {
        return features;
    }
    
    public File getInstallationLocation() {
        final String path = SystemUtils.resolveString(
                getProperty(INSTALLATION_LOCATION_PROPERTY),
                getClassLoader());
        
        return path == null ? null : new File(path);
    }
    
    public void setInstallationLocation(final File location) {
        setProperty(INSTALLATION_LOCATION_PROPERTY, location.getAbsolutePath());
    }
    
    public File getLocalCache() {
        return new File(
                Registry.getInstance().getLocalProductCache(),
                uid + File.separator + version);
    }
    
    public FilesList getInstalledFiles() {
        return installedFiles;
    }
    
    public File getInstalledFilesList() {
        return new File(
                getLocalCache(),
                INSTALLED_FILES_LIST_FILE_NAME);
    }
    
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    public long getDownloadSize() {
        long downloadSize = 0;
        
        for (ExtendedUri uri: logicUris) {
            downloadSize += uri.getSize();
        }
        for (ExtendedUri uri: dataUris) {
            downloadSize += uri.getSize();
        }
        
        return downloadSize;
    }
    
    private ApplicationDescriptor getApplicationDescriptor() {
        final String key = "nbi-" + uid + "-" + version;
        final String displayName = configurationLogic.getSystemDisplayName();
        final String icon;
        if (configurationLogic.getIcon() != null) {
            icon = new File(
                    getInstallationLocation(),
                    configurationLogic.getIcon()).getAbsolutePath();
        } else {
            icon = null;
        }
        
        String installLocation = getInstallationLocation().getAbsolutePath();
        if (SystemUtils.isMacOS() && configurationLogic.wrapForMacOs()) {
            final String applicationLocation = getProperty(APPLICATION_LOCATION_PROPERTY);
            
            if (applicationLocation != null) {
                installLocation = applicationLocation;
            }
        }
        
        final String[] modifyCommand = new String[] {
            TargetOption.TARGET_ARG, uid, version.toString()};
        
        final String[] uninstallCommand = new String[] {
            TargetOption.TARGET_ARG, uid, version.toString(), ForceUninstallOption.FORCE_UNINSTALL_ARG};
        
        if (configurationLogic.allowModifyMode()) {
            return new ApplicationDescriptor(
                    key,
                    displayName,
                    icon,
                    installLocation,
                    uninstallCommand,
                    modifyCommand,
                    configurationLogic.getAdditionalSystemIntegrationInfo());
        } else {
            return new ApplicationDescriptor(
                    key,
                    displayName,
                    icon,
                    installLocation,
                    uninstallCommand,
                    null,
                    configurationLogic.getAdditionalSystemIntegrationInfo());
        }
    }
    
    // miscellanea //////////////////////////////////////////////////////////////////
    public boolean isCompatibleWith(final Platform platform) {
        for (Platform compatiblePlatform: supportedPlatforms) {
            if (compatiblePlatform.isCompatibleWith(platform)) {
                return true;
            }
        }
        
        return false;
    }
    
    private void saveLegalArtifacts() throws IOException {
        if (!configurationLogic.requireLegalArtifactSaving()) {
            return;
        }

        final Text license = configurationLogic.getLicense();
        if (license != null) {
            final File file = new File(
                    getInstallationLocation(),
                    "LICENSE-" + uid + license.getContentType().getExtension());
            
            FileUtils.writeFile(file, license.getText());
            installedFiles.add(file);
        }
        
        final Map<String, Text> thirdPartyLicenses = configurationLogic.getThirdPartyLicenses();
        if (thirdPartyLicenses != null) {
            final File file = new File(
                    getInstallationLocation(),
                    "THIRDPARTYLICENSES-" + uid + ".txt");
            
            for (Map.Entry<String, Text> entry : thirdPartyLicenses.entrySet()) {
                FileUtils.appendFile(file,
                        "%% The following software may be included in this product: " + entry.getKey() + ";\n" +
                        "Use of any of this software is governed by the terms of the license below:\n\n");
                FileUtils.appendFile(file, entry.getValue().getText() + "\n\n");
            }
            
            installedFiles.add(file);
        }
        
        final Text thirdPartyLicense = configurationLogic.getThirdPartyLicense();
        if (thirdPartyLicense != null) {
            final File file = new File(
                    getInstallationLocation(),
                    "THIRDPARTYLICENSE-" + uid + thirdPartyLicense.getContentType().getExtension());
            
            FileUtils.writeFile(file, thirdPartyLicense.getText());
            installedFiles.add(file);
        }
        
        final Text releaseNotes = configurationLogic.getReleaseNotes();
        if (releaseNotes != null) {
            final File file = new File(
                    getInstallationLocation(),
                    "RELEASENOTES-" + uid + releaseNotes.getContentType().getExtension());
            
            FileUtils.writeFile(file, releaseNotes.getText());
            installedFiles.add(file);
        }
        
        final Text readme = configurationLogic.getReadme();
        if (readme != null) {
            final File file = new File(
                    getInstallationLocation(),
                    "README-" + uid + readme.getContentType().getExtension());
            
            FileUtils.writeFile(file, readme.getText());
            installedFiles.add(file);
        }
        
        final Text distributionReadme = configurationLogic.getDistributionReadme();
        if (distributionReadme != null) {
            final File file = new File(
                    getInstallationLocation(),
                    "DISTRIBUTION-" + uid + distributionReadme.getContentType().getExtension());
            
            FileUtils.writeFile(file, distributionReadme.getText());
            installedFiles.add(file);
        }
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    private static enum InstallationPhase {
        INITIALIZATION,
        EXTRACTION,
        CUSTOM_LOGIC,
        FINALIZATION,
        COMPLETE;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    public static final String INSTALLATION_LOCATION_PROPERTY =
            "installation.location"; // NOI18N
    
    public static final String INSTALLED_FILES_LIST_FILE_NAME =
            "installed-files.xml.gz"; // NOI18N
    
    public static final String MANIFEST_LOGIC_CLASS =
            "Configuration-Logic-Class"; // NOI18N
    
    public static final String INFO_PLIST_STUB = FileUtils.INFO_PLIST_STUB;
    
    private static final String APPLICATION_LOCATION_PROPERTY =
            "application.location";
    
    private static final String VERSION_TAG_NAME =
            "version";//NOI18N
    private static final String PLATFORMS_TAG_NAME =
            "platforms";//NOI18N
    private static final String STATUS_TAG_NAME =
            "status";//NOI18N
    private static final String FEATURES_TAG_NAME =
            "features";//NOI18N
    private static final String CONFIGURATION_LOGIC_TAG_NAME =
            "configuration-logic";//NOI18N
    private static final String INSTALLATION_DATA_TAG_NAME =
            "installation-data";//NOI18N
    private static final String DEPENDENCIES_TAG_NAME =
            "dependencies";//NOI18N
    private static final String PRODUCT_TAG_NAME =
            "product";//NOI18N
    private static final String SYSTEM_REQUIREMENTS_TAG_NAME =
            "system-requirements";//NOI18N
    private static final String DISK_SPACE_TAG_NAME =
            "disk-space";//NOI18N
    
    private static final String ERROR_CANNOT_INITIALIZE_PRODUCT_KEY =
            "P.error.cannot.initialize.product";//NOI18N
    private static final String ERROR_CANNOT_LOAD_LOGIC_KEY =
            "P.error.cannot.load.logic";//NOI18N
    private static final String ERROR_INSTALLATION_LOCATION_NOT_SET_KEY =
            "P.error.installdir.not.set";//NOI18N
    private static final String ERROR_INSTALLATION_LOCATION_SET_EMPTY_KEY = 
            "P.error.installdir.set.empty";//NOI18N
    private static final String ERROR_CANNOT_CREATE_PRODUCT_CACHE_DIR_KEY =
            "P.error.cannot.create.cache.dir";//NOI18N
    private static final String ERROR_CACHE_NOT_DIRECTORY_KEY =
            "P.error.local.cache.not.dir";//NOI18N
    private static final String ERROR_CANNOT_LOAD_PRODUCT_KEY =
            "P.error.cannot.load.product";//NOI18N
    private static final String ERROR_LOGIC_NOT_YET_DOWNLOADED_KEY =
            "P.error.logic.not.yet.downloaded";//NOI18N
    private static final String ERROR_DATA_NOT_CACHED_KEY =
            "P.error.installation.data.not.cached";//NOI18N
    private static final String ERROR_CANNOT_EXTRACT_DATA_KEY =
            "P.error.cannot.extract.data";//NOI18N
    private static final String ERROR_CANNOT_SAVE_FILES_LIST_KEY =
            "P.error.cannot.save.files.list";//NOI18N
    private static final String ERROR_CANNOT_GET_WIZARD_COMPONENTS_KEY =
            "P.error.cannot.get.wizard.components";//NOI18N
    private static final String ERROR_CANNOT_WRAP_FOR_MACOS_KEY =
            "P.error.cannot.wrap.for.macos";//NOI18N
    private static final String ERROR_CANNOT_GET_FILES_LIST_KEY =
            "P.error.cannot.get.files.list";//NOI18N
    private static final String ERROR_CANNOT_DELETE_FILES_LIST_KEY =
            "P.error.cannot.delete.files.list";//NOI18N
    private static final String ERROR_CANNOT_DELETE_FILE_KEY =
            "P.error.cannot.delete.file";//NOI18N
    private static final String ERROR_CANNOT_REMOVE_FROM_SYSTEM_KEY =
            "P.error.cannot.remove.from.system";//NOI18N
    private static final String ERROR_NOT_ENOUGH_SPACE_KEY =
            "P.error.not.enough.space";//NOI18N
    private static final String ERROR_CANNOT_CLEAR_DATA_CACHE_KEY =
            "P.error.cannot.clear.cache";//NOI18N
    private static final String ERROR_SYSTEM_INTEGRATION_FAILER_KEY =
            "P.error.system.integartion.failed";//NOI18N
    
    private static final String MESSAGE_INSTALLATION_STRING =
            ResourceUtils.getString(Product.class,
            "P.message.installation");//NOI18N
    private static final String MESSAGE_UNINSTALLATION_STRING =
            ResourceUtils.getString(Product.class,
            "P.message.uninstallation");//NOI18N
    private static final String MESSAGE_CONFIGURATION_STRING =
            ResourceUtils.getString(Product.class,
            "P.message.configuration");//NOI18N
    private static final String MESSAGE_UNCONFIGURATION_STRING =
            ResourceUtils.getString(Product.class,
            "P.message.unconfiguration");//NOI18N
    private static final String MESSAGE_LEGAL_ARTIFACTS_STRING =
            ResourceUtils.getString(Product.class,
            "P.message.legal.artifacts");//NOI18N
    private static final String MESSAGE_RUN_LOGIC_STRING =
            ResourceUtils.getString(Product.class,
            "P.message.run.logic");//NOI18N
    private static final String MESSAGE_SYSTEM_REGISTRATION_STRING =
            ResourceUtils.getString(Product.class,
            "P.message.system.registration");//NOI18N
    private static final String MESSAGE_SAVE_INSTALL_FILES_LIST_STRING =
            ResourceUtils.getString(Product.class,
            "P.message.save.installation.files.list");//NOI18N
    private static final String MESSAGE_DELETE_STRING =
            ResourceUtils.getString(Product.class,
            "P.message.delete");//NOI18N
}
