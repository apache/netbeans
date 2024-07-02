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

package org.netbeans.installer;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeSet;
//import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.product.Registry;
import org.netbeans.installer.utils.DateUtils;
import org.netbeans.installer.utils.EngineUtils;
import org.netbeans.installer.utils.SystemUtils;
import org.netbeans.installer.utils.ErrorManager;
import org.netbeans.installer.utils.LogManager;
import org.netbeans.installer.utils.ResourceUtils;
import org.netbeans.installer.utils.UiUtils;
import org.netbeans.installer.utils.cli.options.*;
import org.netbeans.installer.utils.cli.CLIHandler;
import org.netbeans.installer.utils.helper.EngineResources;
import org.netbeans.installer.utils.helper.FinishHandler;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.wizard.Wizard;

/**
 * The main class of the NBI engine. It represents the installer and provides
 * methods to start the installation/maintenance process as well as to
 * finish/cancel/break the installation.
 *
 * @author Kirill Sorokin
 */
public class Installer implements FinishHandler {
    /////////////////////////////////////////////////////////////////////////////////
    // Main
    /**
     * The main method. It creates an instance of {@link Installer} and calls
     * the {@link #start()} method, passing in the command line arguments.
     *
     * @param arguments The command line arguments
     */
    public static void main(String[] arguments) {
        new Installer(arguments).start();
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Static
    private static Installer instance;
    
    /**
     * Returns an instance of <code>Installer</code>. If the instance does not
     * exist - it is created.
     *
     * @return An instance of <code>Installer</code>
     */
    public static synchronized Installer getInstance() {
        if (instance == null) {
            instance = new Installer(new String[0]);
        }
        
        return instance;
    }
    
    /////////////////////////////////////////////////////////////////////////////////
    // Instance
    private File localDirectory = null;
    
    // Constructor //////////////////////////////////////////////////////////////////
    /**
     * The only private constructor - we need to hide the default one as
     * <code>Installer is a singleton.
     *
     * @param arguments Command-line parameters.
     */
    private Installer(String[] arguments) {
        LogManager.logEntry("initializing the installer engine"); // NOI18N
        
        initializeErrorHandler();
        dumpSystemInfo();
        EngineUtils.checkEngine();
        initializePlatform();
        
        instance = this;
        parseArguments(arguments);
        loadEngineProperties();
        initializeLocalDirectory();
        
        // once we have set the local directory (and therefore devised the log
        // file path) we can start logging safely
        initializeLogManager();
//        initializeDownloadManager();
        initializeRegistry();
        initializeWizard();
        createLockFile();
        
        LogManager.logExit("... finished initializing the engine"); // NOI18N
    }
    
    // Life cycle control methods ///////////////////////////////////////////////////
    /**
     * Starts the installer.
     */
    public void start() {
        LogManager.logEntry("starting the installer"); // NOI18N
        
        Wizard.getInstance().open();
        
        LogManager.logExit("... finished starting the installer"); // NOI18N
    }
    
    /**
     * Cancels the installation. This method cancels the changes that were possibly
     * made to the components registry and exits with the cancel error code.
     *
     * This method is not logged.
     *
     * @see #finish()
     * @see #criticalExit()
     */
    public void cancel() {
        exitNormally(CANCEL_ERRORCODE);
    }
    
    /**
     * Finishes the installation. This method finalizes the changes made to the
     * components registry and exits with a normal error code.
     *
     * This method is not logged.
     *
     * @see #cancel()
     * @see #criticalExit()
     */
    public void finish() {
        int exitCode = NORMAL_ERRORCODE;
        final Object prop = System.getProperties().get(EXIT_CODE_PROPERTY);
        if (prop instanceof Integer) {
            try {
                exitCode = ((Integer)prop).intValue();
            } catch (NumberFormatException e) {
                LogManager.log("... cannot parse exit code : " + prop, e);
            }
        }
        exitNormally(exitCode);
    }
    
    /**
     * Critically exists. No changes will be made to the components registry - it
     * will remain at the same state it was at the moment this method was called.
     *
     * This method is not logged.
     *
     * @see #cancel()
     * @see #finish()
     */
    public void criticalExit() {
        LogManager.stop();
        // exit immediately, as the system is apparently in a crashed state
        exitImmediately(CRITICAL_ERRORCODE);
    }
    
    public File getLocalDirectory() {
        if(localDirectory==null) {
            initializeLocalDirectory();
        }
        return localDirectory;
    }
    
    private void initializeErrorHandler() {
        // initialize the error manager
        LogManager.logEntry("... initializing ErrorHandler");
        ErrorManager.setFinishHandler(this);
        ErrorManager.setExceptionHandler(new ErrorManager.ExceptionHandler());
        
        // attach a handler for uncaught exceptions in the main thread
        Thread.currentThread().setUncaughtExceptionHandler(
                ErrorManager.getExceptionHandler());
        LogManager.logExit("... end of ErrorHandler initialization"); // NOI18N
    }
    
    private void initializePlatform() {
        // check whether we can safely execute on the current platform, exit in panic otherwise
        if (SystemUtils.getCurrentPlatform() == null) {
            ErrorManager.notifyCritical(ResourceUtils.getString(
                    Installer.class,
                    ERROR_UNSUPPORTED_PLATFORM_KEY));
        }
    }
    
    private void initializeLogManager() {        
        final String logFileProp = System.getProperty(LogManager.LOG_FILE_PROPERTY);
        final File logFile = logFileProp == null ? 
                    new File(getLocalDirectory(), LOG_FILE_NAME) : 
                    new File(logFileProp).getAbsoluteFile();
        LogManager.setLogFile(logFile);
        LogManager.start();
    }
    
//    private void initializeDownloadManager() {
//        LogManager.logEntry("... initializing DownloadManager");
//        final DownloadManager downloadManager = DownloadManager.getInstance();
//        downloadManager.setLocalDirectory(getLocalDirectory());
//        downloadManager.setFinishHandler(this);
//        downloadManager.init();
//        LogManager.logExit("... end of initializing DownloadManager");
//    }
    
    private void initializeRegistry() {
        LogManager.logEntry("... initializing Registry");
        final Registry registry = Registry.getInstance();
        registry.setLocalDirectory(getLocalDirectory());
        registry.setFinishHandler(this);
        LogManager.logExit("... end of initializing ErrorHandler");
    }
    
    private void initializeWizard() {
        LogManager.logEntry("... initializing Wizard");
        final Wizard wizard = Wizard.getInstance();
        wizard.setFinishHandler(this);
        wizard.getContext().put(Registry.getInstance());
        LogManager.logExit("... end of initializing Wizard");
    }
    
    // private //////////////////////////////////////////////////////////////////////
    private void exitNormally(int errorCode) {
        Wizard.getInstance().close();
//        DownloadManager.getInstance().terminate();
        LogManager.stop();
        
        exitImmediately(errorCode);
    }
    
    private void exitImmediately(int errorCode) {
        if (Boolean.getBoolean(DONT_USE_SYSTEM_EXIT_PROPERTY) &&
                (errorCode != CRITICAL_ERRORCODE)) {
            System.getProperties().put(
                    EXIT_CODE_PROPERTY,
                    Integer.valueOf(errorCode));
        } else {
            System.exit(errorCode);
        }
    }
    
    private void dumpSystemInfo() {
        // output all the possible target system information -- this may help to
        // devise the configuration differences in case of errors        
        LogManager.logEntry("dumping target system information"); // NOI18N
        
        LogManager.logIndent("system properties:"); // NOI18N
        
        for (Object key: new TreeSet<Object>(System.getProperties().keySet())) {
            LogManager.log(key.toString() + " => " +  // NOI18N
                    System.getProperties().get(key).toString());
        }
        
        LogManager.unindent();
        
        LogManager.logExit("... end of target system information"); // NOI18N
    }
    
    private void loadEngineProperties() {
        LogManager.logEntry("loading engine properties"); // NOI18N
        
        try {
            LogManager.logIndent("loading engine properties");
            
            ResourceBundle bundle = ResourceBundle.getBundle(
                    EngineResources.ENGINE_PROPERTIES_BUNDLE);
            Enumeration <String> keys = bundle.getKeys();
            
            while (keys.hasMoreElements()) {
                final String key = keys.nextElement();
                final String value = bundle.getString(key);
                LogManager.log("loading " + key + " => " + value); // NOI18N
		final String currentValue = System.getProperty(key);
		if(currentValue!=null) {
                    LogManager.log("... already defined, using existing value: " + currentValue); // NOI18N
                } else {
                    System.setProperty(key,value);
                }
                
            }
        } catch (MissingResourceException e) {
            LogManager.log("... no engine properties file, skip loading engine properties");
        }
        
        LogManager.unindent();
        
        LogManager.logExit("... finished loading engine properties"); // NOI18N
    }
    
    /**
     * Parses the command line arguments passed to the installer. All unknown
     * arguments are ignored.
     *
     * @param arguments The command line arguments
     */
    private void parseArguments(String[] arguments) {
        LogManager.logEntry("parsing command-line arguments"); // NOI18N
        
        new CLIHandler(arguments).proceed();
        
        // validate arguments ///////////////////////////////////////////////////////
	/*
          Disabled since 28.02.2008 by Dmitry Lipin:
          I don`t see any reason for having that restiction at this moment:
          I can succesfully install/create bundle just with empty state file similar to
          Registry.DEFAULT_STATE_FILE_STUB_URI or even just have <state/> as the file contents
	*/
        /*
        if (UiMode.getCurrentUiMode() != UiMode.DEFAULT_MODE) {
            if (System.getProperty(
                    Registry.SOURCE_STATE_FILE_PATH_PROPERTY) == null) {
                UiMode.setCurrentUiMode(UiMode.DEFAULT_MODE);
                ErrorManager.notifyWarning(ResourceUtils.getString(
                        Installer.class,
                        WARNING_SILENT_WITHOUT_STATE_KEY,
                         SilentOption.SILENT_ARG,
                         StateOption.STATE_ARG));
            }
        }
        */
        LogManager.logExit(
                "... finished parsing command line arguments"); // NOI18N
    }
    
    private void initializeLocalDirectory() {
        if(localDirectory!=null) {
            return;
        }
        LogManager.logIndent("initializing the local directory"); // NOI18N

        if (System.getProperty(LOCAL_DIRECTORY_PATH_PROPERTY) != null) {
            String path = System.getProperty(LOCAL_DIRECTORY_PATH_PROPERTY);
            LogManager.log("... local directory path (initial) : " + path);
            path = SystemUtils.resolveString(path);
            LogManager.log("... local directory path (resolved): " + path);
            localDirectory = new File(path).getAbsoluteFile();
        } else {
            LogManager.log("... custom local directory was " + // NOI18N
                        "not specified, using the default"); // NOI18N

            localDirectory =
                        new File(DEFAULT_LOCAL_DIRECTORY_PATH).getAbsoluteFile();
            System.setProperty(
                    LOCAL_DIRECTORY_PATH_PROPERTY,
                    localDirectory.getAbsolutePath());
            
        }
        
        
        LogManager.log("... local directory: " + localDirectory); // NOI18N
        
        if (!localDirectory.exists()) {
            if (!localDirectory.mkdirs()) {
                ErrorManager.notifyCritical(ResourceUtils.getString(
                        Installer.class,
                        ERROR_CANNOT_CREATE_LOCAL_DIR_KEY,
                        localDirectory));
            }
        } else if (localDirectory.isFile()) {
            ErrorManager.notifyCritical(ResourceUtils.getString(
                    Installer.class,
                    ERROR_LOCAL_DIR_IS_FILE_KEY,
                    localDirectory));
        } else if (!localDirectory.canRead()) {
            ErrorManager.notifyCritical(ResourceUtils.getString(
                    Installer.class,
                    ERROR_NO_READ_PERMISSIONS_FOR_LOCAL_DIR_KEY,
                    localDirectory));
        } else if (!localDirectory.canWrite()) {
            ErrorManager.notifyCritical(ResourceUtils.getString(
                    Installer.class,
                    ERROR_NO_WRITE_PERMISSIONS_FOR_LOCAL_DIR_KEY,
                    localDirectory));
        }
        
        LogManager.logUnindent(
                "... finished initializing local directory"); // NOI18N
    }
    
    private void createLockFile()  {
        LogManager.logIndent("creating lock file"); // NOI18N
        
        if (System.getProperty(IGNORE_LOCK_FILE_PROPERTY) == null) {
            final File lock = new File(getLocalDirectory(), LOCK_FILE_NAME);
            
            if (lock.exists()) {
                LogManager.log("... lock file already exists at " + lock); // NOI18N
                LogManager.log("... to skip lock file check run installer with " + IgnoreLockOption.IGNORE_LOCK_ARG + " argument"); // NOI18N
                
                final String dialogTitle = ResourceUtils.getString(
                        Installer.class,
                        LOCK_FILE_EXISTS_DIALOG_TITLE_KEY);
                final String dialogText = ResourceUtils.getString(
                        Installer.class,
                        LOCK_FILE_EXISTS_DIALOG_TEXT_KEY,
                        lock.getAbsolutePath());
                if(!UiUtils.showYesNoDialog(dialogTitle, dialogText)) {
                    cancel();
                }
            } else {
                try {
                    lock.createNewFile();
                } catch (IOException e) {
                    ErrorManager.notifyCritical(ResourceUtils.getString(
                            Installer.class,
                            ERROR_CANNOT_CREATE_LOCK_FILE_KEY), e);
                }
                
                LogManager.log("... created lock file: " + lock); // NOI18N
            }
            
            lock.deleteOnExit();
        } else {
            LogManager.log("... running with " + // NOI18N
                    IgnoreLockOption.IGNORE_LOCK_ARG + ", skipping this step"); // NOI18N
        }
        
        LogManager.logUnindent("finished creating lock file"); // NOI18N
    }
    
    @Deprecated
    public File cacheInstallerEngine(Progress progress) throws IOException {
        return EngineUtils.cacheEngine(progress);
    }
    /////////////////////////////////////////////////////////////////////////////////
    // Constants
    
    // errorcodes ///////////////////////////////////////////////////////////////////
    /** Errorcode to be used at normal exit */
    public static final int NORMAL_ERRORCODE =
            0;
    
    /** Errorcode to be used when the installer is canceled */
    public static final int CANCEL_ERRORCODE =
            1;
    
    /** Errorcode to be used when the installer exits because of a critical error */
    public static final int CRITICAL_ERRORCODE =
            255;
    
    // lock file ////////////////////////////////////////////////////////////////////
    public static final String LOCK_FILE_NAME =
            ".nbilock"; // NOI18N
    
    public static final String IGNORE_LOCK_FILE_PROPERTY =
            "nbi.ignore.lock.file"; // NOI18N
    
    // local working directory //////////////////////////////////////////////////////
    public static final String DEFAULT_LOCAL_DIRECTORY_PATH =
            System.getProperty("user.home") + File.separator + ".nbi";
    
    public static final String LOCAL_DIRECTORY_PATH_PROPERTY =
            "nbi.local.directory.path"; // NOI18N
    
    // miscellaneous ////////////////////////////////////////////////////////////////
    public static final String DONT_USE_SYSTEM_EXIT_PROPERTY =
            "nbi.dont.use.system.exit"; // NOI18N
    
    public static final String EXIT_CODE_PROPERTY =
            "nbi.exit.code"; // NOI18N
    
    public static final String BUNDLE_PROPERTIES_FILE_PROPERTY =
            "nbi.bundle.properties.file";//NOI18N
    
    public static final String LOG_FILE_NAME =
            "log/" + DateUtils.getTimestamp() + ".log";
    
    // resource bundle keys /////////////////////////////////////////////////////////
    private static final String ERROR_UNSUPPORTED_PLATFORM_KEY =
            "I.error.unsupported.platform"; // NOI18N
    
    private static final String WARNING_SILENT_WITHOUT_STATE_KEY =
            "I.warning.silent.without.state"; // NOI18N
    
    private static final String ERROR_CANNOT_CREATE_LOCAL_DIR_KEY =
            "I.error.cannot.create.local.dir"; // NOI18N
    
    private static final String ERROR_LOCAL_DIR_IS_FILE_KEY =
            "I.error.local.dir.is.file"; // NOI18N
    
    private static final String ERROR_NO_READ_PERMISSIONS_FOR_LOCAL_DIR_KEY =
            "I.error.no.read.permissions.for.local.dir"; // NOI18N
    
    private static final String ERROR_NO_WRITE_PERMISSIONS_FOR_LOCAL_DIR_KEY =
            "I.error.no.write.permissions.for.local.dir"; // NOI18N
    
    private static final String LOCK_FILE_EXISTS_DIALOG_TITLE_KEY =
            "I.lock.file.exists.dialog.title"; // NOI18N
    
    private static final String LOCK_FILE_EXISTS_DIALOG_TEXT_KEY =
            "I.lock.file.exists.dialog.text"; // NOI18N
    
    private static final String ERROR_CANNOT_CREATE_LOCK_FILE_KEY =
            "I.error.cannot.create.lock.file"; // NOI18N
    
}
