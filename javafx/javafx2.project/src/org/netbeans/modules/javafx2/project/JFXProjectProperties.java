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
package org.netbeans.modules.javafx2.project;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils;
import org.netbeans.modules.javafx2.project.ui.JFXApplicationPanel;
import org.netbeans.modules.javafx2.project.ui.JFXPackagingPanel;
import org.netbeans.modules.javafx2.project.ui.JSEDeploymentPanel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

public final class JFXProjectProperties {

    private static final Logger LOG = Logger.getLogger(JFXProjectProperties.class.getName());
    private static final Color INVALID_CELL_CONTENT_COLOR = Color.RED;
    
    public static final String JAVAFX_ENABLED = "javafx.enabled"; // NOI18N
    public static final String JAVAFX_PRELOADER = "javafx.preloader"; // NOI18N
    public static final String JAVAFX_SWING = "javafx.swing"; // NOI18N
    public static final String JAVAFX_DISABLE_AUTOUPDATE = "javafx.disable.autoupdate"; // NOI18N
    public static final String JAVAFX_DISABLE_AUTOUPDATE_NOTIFICATION = "javafx.disable.autoupdate.notification"; // NOI18N
    public static final String JAVAFX_DISABLE_CONCURRENT_RUNS = "javafx.disable.concurrent.runs"; // NOI18N
    public static final String JAVAFX_ENABLE_CONCURRENT_EXTERNAL_RUNS = "javafx.enable.concurrent.external.runs"; // NOI18N
    public static final String JAVAFX_ENDORSED_ANT_CLASSPATH = "endorsed.javafx.ant.classpath"; // NOI18N
    public static final String PLATFORM_ACTIVE = "platform.active"; // NOI18N
    public static final String PLATFORM_ANT_NAME = "platform.ant.name";    //NOI18N
    
    /** The standard extension for FXML source files. */
    public static final String FXML_EXTENSION = "fxml"; // NOI18N    
    
    // copies of private J2SE properties
    public static final String SOURCE_ENCODING = "source.encoding"; // NOI18N
    public static final String JAVADOC_PRIVATE = "javadoc.private"; // NOI18N
    public static final String JAVADOC_NO_TREE = "javadoc.notree"; // NOI18N
    public static final String JAVADOC_USE = "javadoc.use"; // NOI18N
    public static final String JAVADOC_NO_NAVBAR = "javadoc.nonavbar"; // NOI18N
    public static final String JAVADOC_NO_INDEX = "javadoc.noindex"; // NOI18N
    public static final String JAVADOC_SPLIT_INDEX = "javadoc.splitindex"; // NOI18N
    public static final String JAVADOC_AUTHOR = "javadoc.author"; // NOI18N
    public static final String JAVADOC_VERSION = "javadoc.version"; // NOI18N
    public static final String JAVADOC_WINDOW_TITLE = "javadoc.windowtitle"; // NOI18N
    public static final String JAVADOC_ENCODING = "javadoc.encoding"; // NOI18N
    public static final String JAVADOC_ADDITIONALPARAM = "javadoc.additionalparam"; // NOI18N
    public static final String BUILD_SCRIPT = "buildfile"; //NOI18N
    public static final String DIST_JAR = "dist.jar"; // NOI18N

    // Packaging properties
    public static final String JAVAFX_BINARY_ENCODE_CSS = "javafx.binarycss"; // NOI18N
    public static final String JAVAFX_DEPLOY_INCLUDEDT = "javafx.deploy.includeDT"; // NOI18N
    public static final String JAVAFX_DEPLOY_EMBEDJNLP = "javafx.deploy.embedJNLP"; // NOI18N
    public static final String JAVAFX_REBASE_LIBS = "javafx.rebase.libs"; // NOI18N
    
    // FX config properties (Run panel), replicated from ProjectProperties
    public static final String MAIN_CLASS = "javafx.main.class"; // NOI18N
    //public static final String APPLICATION_ARGS = JFXProjectConfigurations.APPLICATION_ARGS;
    //public static final String APP_PARAM_PREFIX = JFXProjectConfigurations.APP_PARAM_PREFIX;
    //public static final String APP_PARAM_SUFFIXES[] = JFXProjectConfigurations.APP_PARAM_SUFFIXES;
    public static final String RUN_JVM_ARGS = ProjectProperties.RUN_JVM_ARGS;
    public static final String FALLBACK_CLASS = "javafx.fallback.class"; // NOI18N
    public static final String SIGNED_JAR = "dist.signed.jar"; // NOI18N
    
    public static final String PRELOADER_ENABLED = "javafx.preloader.enabled"; // NOI18N
    public static final String PRELOADER_TYPE = "javafx.preloader.type"; // NOI18N
    public static final String PRELOADER_PROJECT = "javafx.preloader.project.path"; // NOI18N
    public static final String PRELOADER_CLASS = "javafx.preloader.class"; // NOI18N
    public static final String PRELOADER_JAR_FILENAME = "javafx.preloader.jar.filename"; // NOI18N
    public static final String PRELOADER_JAR_PATH = "javafx.preloader.jar.path"; // NOI18N
    
    public static final String RUN_WORK_DIR = ProjectProperties.RUN_WORK_DIR; // NOI18N
    public static final String RUN_APP_WIDTH = "javafx.run.width"; // NOI18N
    public static final String RUN_APP_HEIGHT = "javafx.run.height"; // NOI18N
    public static final String RUN_IN_HTMLTEMPLATE = "javafx.run.htmltemplate"; // NOI18N
    public static final String RUN_IN_HTMLTEMPLATE_PROCESSED = "javafx.run.htmltemplate.processed"; // NOI18N
    public static final String RUN_IN_BROWSER = "javafx.run.inbrowser"; // NOI18N
    public static final String RUN_IN_BROWSER_PATH = "javafx.run.inbrowser.path"; // NOI18N
    public static final String RUN_IN_BROWSER_ARGUMENTS = "javafx.run.inbrowser.arguments"; // NOI18N
    public static final String RUN_IN_BROWSER_UNDEFINED = "undefined"; // NOI18N
    public static final String RUN_AS = "javafx.run.as"; // NOI18N

    public static final String DEFAULT_APP_WIDTH = "800"; // NOI18N
    public static final String DEFAULT_APP_HEIGHT = "600"; // NOI18N

    // Deployment properties
    public static final String UPDATE_MODE_BACKGROUND = "javafx.deploy.backgroundupdate"; // NOI18N
    public static final String ALLOW_OFFLINE = "javafx.deploy.allowoffline"; // NOI18N
    public static final String INSTALL_PERMANENTLY = "javafx.deploy.installpermanently"; // NOI18N
    public static final String ADD_DESKTOP_SHORTCUT = "javafx.deploy.adddesktopshortcut"; // NOI18N
    public static final String ADD_STARTMENU_SHORTCUT = "javafx.deploy.addstartmenushortcut"; // NOI18N
    public static final String ICON_FILE = "javafx.deploy.icon"; // NOI18N
    public static final String NATIVE_ICON_FILE = "javafx.deploy.icon.native"; // NOI18N
    public static final String SPLASH_IMAGE_FILE = "javafx.deploy.splash"; // NOI18N
    public static final String PERMISSIONS_ELEVATED = "javafx.deploy.permissionselevated"; // NOI18N
    public static final String DISABLE_PROXY = "javafx.deploy.disable.proxy"; // NOI18N
    public static final String REQUEST_RT = "javafx.deploy.request.runtime"; // NOI18N

    // Deployment - signing
    public static final String JAVAFX_SIGNING_ENABLED = "javafx.signing.enabled"; //NOI18N
    public static final String JAVAFX_SIGNING_TYPE = "javafx.signing.type"; //NOI18N
    public static final String JAVAFX_SIGNING_KEYSTORE = "javafx.signing.keystore"; //NOI18N
    public static final String JAVAFX_SIGNING_KEYSTORE_PASSWORD = "javafx.signing.keystore.password"; //NOI18N
    public static final String JAVAFX_SIGNING_KEY = "javafx.signing.keyalias"; //NOI18N
    public static final String JAVAFX_SIGNING_KEY_PASSWORD = "javafx.signing.keyalias.password"; //NOI18N
    public static final String JAVAFX_SIGNING_BLOB = "javafx.signing.blob"; //NOI18N
    
    // Deployment - native packaging
    public static final String NATIVE_BUNDLING_ENABLED = "native.bundling.enabled"; //NOI18N
    public static final String NATIVE_BUNDLING_TYPE = "native.bundling.type"; //NOI18N
    //public static final String JAVASE_NATIVE_BUNDLING_ENABLED = "native.bundling.enabled"; //NOI18N

    //Deloyment - copylibs
    public static final String COPYLIBS_EXCLUDES = "copylibs.excludes"; //NOI18N

    // Deployment - common and SE specific
    public static final String RUN_CP = "run.classpath";    //NOI18N
    public static final String BUILD_CLASSES = "build.classes.dir"; //NOI18N
    public static final String JAVASE_KEEP_JFXRT_ON_CLASSPATH = "keep.javafx.runtime.on.classpath"; //NOI18N
    
    // Deployment - libraries download mode
    public static final String DOWNLOAD_MODE_LAZY_JARS = "download.mode.lazy.jars";   //NOI18N
    private static final String DOWNLOAD_MODE_LAZY_JAR = "download.mode.lazy.jar."; //NOI18N
    private static final String DOWNLOAD_MODE_LAZY_FORMAT = DOWNLOAD_MODE_LAZY_JAR +"%s"; //NOI18N
    
    // Deployment - callbacks
    public static final String JAVASCRIPT_CALLBACK_PREFIX = "javafx.jscallback."; // NOI18N
    
    // Application
    public static final String IMPLEMENTATION_VERSION = "javafx.application.implementation.version"; // NOI18N
    public static final String IMPLEMENTATION_VERSION_DEFAULT = "1.0"; // NOI18N
    
    // folders and files
    public static final String PROJECT_CONFIGS_DIR = JFXProjectConfigurations.PROJECT_CONFIGS_DIR;
    public static final String PROJECT_PRIVATE_CONFIGS_DIR = JFXProjectConfigurations.PROJECT_PRIVATE_CONFIGS_DIR;
    public static final String PROPERTIES_FILE_EXT = JFXProjectConfigurations.PROPERTIES_FILE_EXT;
    public static final String CONFIG_PROPERTIES_FILE = JFXProjectConfigurations.CONFIG_PROPERTIES_FILE;
    public static final String DEFAULT_CONFIG = NbBundle.getBundle("org.netbeans.modules.javafx2.project.ui.Bundle").getString("JFXConfigurationProvider.default.label"); // NOI18N
    public static final String DEFAULT_CONFIG_STANDALONE = NbBundle.getBundle("org.netbeans.modules.javafx2.project.ui.Bundle").getString("JFXConfigurationProvider.standalone.label"); // NOI18N
    public static final String DEFAULT_CONFIG_WEBSTART = NbBundle.getBundle("org.netbeans.modules.javafx2.project.ui.Bundle").getString("JFXConfigurationProvider.webstart.label"); // NOI18N
    public static final String DEFAULT_CONFIG_BROWSER = NbBundle.getBundle("org.netbeans.modules.javafx2.project.ui.Bundle").getString("JFXConfigurationProvider.browser.label"); // NOI18N

    // explicit manifest entries (see #231951, #234231, http://docs.oracle.com/javase/7/docs/technotes/guides/jweb/no_redeploy.html)
    public static final String MANIFEST_CUSTOM_CODEBASE = "manifest.custom.codebase"; // NOI18N
    public static final String MANIFEST_CUSTOM_PERMISSIONS = "manifest.custom.permissions"; // NOI18N
    public static final String PLATFORM_RUNTIME = "platform.runtime";           //NOI18N
    
    // FX RT artifact reference to be kept at compile classpath
    private static final String JFX_EXTENSION_CPREF = "${javafx.classpath.extension}";  //NOI18N

    private StoreGroup fxPropGroup = new StoreGroup();
    
    // Packaging
    JToggleButton.ToggleButtonModel binaryEncodeCSS;
    public JToggleButton.ToggleButtonModel getBinaryEncodeCSSModel() {
        return binaryEncodeCSS;
    }

    private JFXPackagingPanel packagingPanel = null;
    public JFXPackagingPanel getPackagingPanel() {
        if(packagingPanel == null) {
            packagingPanel = new JFXPackagingPanel(this);
        }
        return packagingPanel;
    }

    private JFXApplicationPanel applicationPanel = null;
    public JFXApplicationPanel getApplicationPanel() {
        if(applicationPanel == null) {
            applicationPanel = new JFXApplicationPanel(this);
        }
        return applicationPanel;
    }

    private JSEDeploymentPanel seDeploymentPanel = null;
    public JSEDeploymentPanel getSEDeploymentPanel() {
        if(seDeploymentPanel == null) {
            seDeploymentPanel = new JSEDeploymentPanel(this);
        }
        return seDeploymentPanel;
    }

    // CustomizerRun
    private JFXConfigs CONFIGS = null;
    public JFXConfigs getConfigs() {
        return CONFIGS;
    }

    private Map<String,String> browserPaths = null;

    public Map<String, String> getBrowserPaths() {
        return browserPaths;
    }
    public void resetBrowserPaths() {
        this.browserPaths = new HashMap<String, String>();
    }
    public void setBrowserPaths(Map<String, String> browserPaths) {
        this.browserPaths = browserPaths;
    }

    // CustomizerRun - Preloader source type
    public enum PreloaderSourceType {
        NONE("none"), // NOI18N
        PROJECT("project"), // NOI18N
        JAR("jar"); // NOI18N
        private final String propertyValue;
        PreloaderSourceType(String propertyValue) {
            this.propertyValue = propertyValue;
        }
        public String getString() {
            return propertyValue;
        }
    }
    
    PreloaderClassComboBoxModel preloaderClassModel;
    public PreloaderClassComboBoxModel getPreloaderClassModel() {
        return preloaderClassModel;
    }

    // CustomizerRun - Run type
    public enum RunAsType {
        STANDALONE("standalone", DEFAULT_CONFIG_STANDALONE), // NOI18N
        ASWEBSTART("webstart", DEFAULT_CONFIG_WEBSTART), // NOI18N
        INBROWSER("embedded", DEFAULT_CONFIG_BROWSER); // NOI18N
        private final String propertyValue;
        private final String defaultConfig;
        RunAsType(String propertyValue, String defaultConfig) {
            this.propertyValue = propertyValue;
            this.defaultConfig = defaultConfig;
        }
        public String getString() {
            return propertyValue;
        }
        public String getDefaultConfig() {
            return defaultConfig;
        }
    }
    JToggleButton.ToggleButtonModel runStandalone;
    JToggleButton.ToggleButtonModel runAsWebStart;
    JToggleButton.ToggleButtonModel runInBrowser;
    
    // Deployment
    JToggleButton.ToggleButtonModel allowOfflineModel;
    public JToggleButton.ToggleButtonModel getAllowOfflineModel() {
        return allowOfflineModel;
    }
    JToggleButton.ToggleButtonModel backgroundUpdateCheck;
    public JToggleButton.ToggleButtonModel getBackgroundUpdateCheckModel() {
        return backgroundUpdateCheck;
    }
    JToggleButton.ToggleButtonModel installPermanently;
    public JToggleButton.ToggleButtonModel getInstallPermanentlyModel() {
        return installPermanently;
    }
    JToggleButton.ToggleButtonModel addDesktopShortcut;
    public JToggleButton.ToggleButtonModel getAddDesktopShortcutModel() {
        return addDesktopShortcut;
    }
    JToggleButton.ToggleButtonModel addStartMenuShortcut;
    public JToggleButton.ToggleButtonModel getAddStartMenuShortcutModel() {
        return addStartMenuShortcut;
    }
    JToggleButton.ToggleButtonModel disableProxy;
    public JToggleButton.ToggleButtonModel getDisableProxyModel() {
        return disableProxy;
    }

    String wsIconPath;
    String splashImagePath;
    String nativeIconPath;
    public String getWSIconPath() {
        return wsIconPath;
    }
    public void setWSIconPath(String path) {
        this.wsIconPath = path;
    }
    public String getSplashImagePath() {
        return splashImagePath;
    }
    public void setSplashImagePath(String path) {
        this.splashImagePath = path;
    }
    public String getNativeIconPath() {
        return nativeIconPath;
    }
    public void setNativeIconPath(String path) {
        this.nativeIconPath = path;
    }
    
    // Deployment - Signing
    public enum SigningType {
        NOSIGN("notsigned"), // NOI18N
        SELF("self"), // NOI18N
        KEY("key"); // NOI18N
        private final String propertyValue;
        SigningType(String propertyValue) {
            this.propertyValue = propertyValue;
        }
        public String getString() {
            return propertyValue;
        }
    }
    boolean signingEnabled;
    boolean signingBlob;
    SigningType signingType;
    String signingKeyStore;
    String signingKeyAlias;
    boolean permissionsElevated;
    char [] signingKeyStorePassword;
    char [] signingKeyPassword;

    public boolean getSigningEnabled() {
        return signingEnabled;
    }
    public void setSigningEnabled(boolean enabled) {
        this.signingEnabled = enabled;
    }
    public boolean getBLOBSigningEnabled() {
        return signingBlob;
    }
    public void setBLOBSigningEnabled(boolean enabled) {
        this.signingBlob = enabled;
    }
    public boolean getPermissionsElevated() {
        return permissionsElevated;
    }
    public void setPermissionsElevated(boolean enabled) {
        this.permissionsElevated = enabled;
    }
    public SigningType getSigningType() {
        return signingType;
    }
    public void setSigningType(SigningType type) {
        this.signingType = type;
    }
    public String getSigningKeyStore() {
        return signingKeyStore;
    }
    public String getSigningKeyAlias() {
        return signingKeyAlias;
    }
    public char[] getSigningKeyStorePassword() {
        return signingKeyStorePassword;
    }
    public char[] getSigningKeyPassword() {
        return signingKeyPassword;
    }
    public void setSigningKeyAlias(String signingKeyAlias) {
        this.signingKeyAlias = signingKeyAlias;
    }
    public void setSigningKeyPassword(char[] signingKeyPassword) {
        this.signingKeyPassword = signingKeyPassword;
    }
    public void setSigningKeyStore(String signingKeyStore) {
        this.signingKeyStore = signingKeyStore;
    }
    public void setSigningKeyStorePassword(char[] signingKeyStorePassword) {
        this.signingKeyStorePassword = signingKeyStorePassword;
    }
    
    // Deployment - Native Packaging (JDK 7u6+)
    public enum BundlingType {
        NONE("none", OS.ALL, "None"), // NOI18N
        ALL("all", OS.ALL, "All Artifacts"), // NOI18N
        IMAGE("image", OS.ALL, "Image Only"), // NOI18N
        INSTALLER("installer", OS.ALL, "All Installers"), // NOI18N
        DEB("deb", OS.LINUX, "DEB Package"), // NOI18N
        RPM("rpm", OS.LINUX, "RPM Package"), // NOI18N
        DMG("dmg", OS.MAC, "DMG Image"), // NOI18N
        EXE("exe", OS.WIN, "EXE Installer"), // NOI18N
        MSI("msi", OS.WIN, "MSI Installer"); // NOI18N
        private final String propertyValue;
        private final String description;
        private final OS extent;
        public enum OS {ALL, WIN, MAC, LINUX, NONE}
        BundlingType(String propertyValue, OS os, String desc) {
            this.propertyValue = propertyValue;
            this.extent = os;
            this.description = desc;
        }
        public String getValue() {
            return propertyValue;
        }
        public OS getExtent() {
            return extent;
        }
        @Override
        public String toString() {
            return description;
        }
    }

    boolean nativeBundlingEnabled;
    public boolean getNativeBundlingEnabled() {
        return nativeBundlingEnabled;
    }
    public void setNativeBundlingEnabled(boolean enabled) {
        this.nativeBundlingEnabled = enabled;
    }
    
    // in SE project - keep jfxrt.jar on classpath
    boolean keepJFXRTonCP;
    public boolean getKeepJFXRTonCP() {
        return keepJFXRTonCP;
    }
    public void setKeepJFXRTonCP(boolean enabled) {
        this.keepJFXRTonCP = enabled;
    }

    // Deployment - Libraries Download Mode
    List<? extends File> runtimeCP;
    List<? extends File> lazyJars;
    boolean lazyJarsChanged;
    public List<? extends File> getRuntimeCP() {
        return runtimeCP;
    }
    public List<? extends File> getLazyJars() {
        return lazyJars;
    }
    public void setLazyJars(List<? extends File> newLazyJars) {
        this.lazyJars = newLazyJars;
    }
    public boolean getLazyJarsChanged() {
        return lazyJarsChanged;
    }
    public void setLazyJarsChanged(boolean changed) {
        this.lazyJarsChanged = changed;
    }
    
    // Deployment - JavaScript Callbacks
    Map<String,String> jsCallbacks;
    boolean jsCallbacksChanged;
    public Map<String,String> getJSCallbacks() {
        return jsCallbacks;
    }
    public void setJSCallbacks(Map<String,String> newCallbacks) {
        jsCallbacks = newCallbacks;
    }
    public boolean getJSCallbacksChanged() {
        return jsCallbacksChanged;
    }
    public void setJSCallbacksChanged(boolean changed) {
        jsCallbacksChanged = changed;
    }
    
    // Deployment - requested RT
    String requestedRT;
    public String getRequestedRT() {
        return requestedRT;
    }
    public void setRequestedRT(String rt) {
        this.requestedRT = rt;
    }
    
    // Application
    String implVersion;
    public String getImplementationVersion() {
        return implVersion;
    }
    public void setImplementationVersion(String implVer) {
        implVersion = implVer;
    }
        
    // Project related references
    private J2SEPropertyEvaluator j2sePropEval;
    private PropertyEvaluator evaluator;
    private Project project;

    public Project getProject() {
        return project;
    }
    public PropertyEvaluator getEvaluator() {
        return evaluator;
    }
    
    /** Keeps singleton instance of JFXProjectProperties for any fx project for which property customizer is opened at once */
    private static Map<String, JFXProjectProperties> propInstance = new HashMap<String, JFXProjectProperties>();

    /** Keeps set of category markers used to identify validity of JFXProjectProperties instance */
    private Set<String> instanceMarkers = new TreeSet<String>();
    
    public void markInstance(@NonNull String marker) {
        instanceMarkers.add(marker);
    }
    
    public boolean isInstanceMarked(@NonNull String marker) {
        return instanceMarkers.contains(marker);
    }
    
    /** Factory method */
    public static JFXProjectProperties getInstance(Lookup context) {
        Project proj = context.lookup(Project.class);
        String projDir = proj.getProjectDirectory().getPath();
        JFXProjectProperties prop = propInstance.get(projDir);
        if(prop == null) {
            prop = new JFXProjectProperties(context);
            propInstance.put(projDir, prop);
        }
        return prop;
    }

    /** Factory method 
     * This is to prevent reuse of the same instance after the properties dialog
     * has been cancelled. Called by each FX category provider at the time
     * when properties dialog is opened, it checks/stores category-specific marker strings. 
     * Previous existence of marker string indicates that properties dialog had been opened
     * before and ended by Cancel, otherwise this instance would not exist (OK would
     * cause properties to be saved and the instance deleted by a call to JFXProjectProperties.cleanup()).
     * (Note that this is a workaround to avoid adding listener to properties dialog close event.)
     * 
     * @param category marker string to indicate which category provider is calling this
     * @return instance of JFXProjectProperties shared among category panels in the current Project Properties dialog only
     * 
     * @deprecated handle cleanup using ProjectCustomizer.Category.setCloseListener instead
     */
    @Deprecated
    public static JFXProjectProperties getInstancePerSession(Lookup context, String category) {
        Project proj = context.lookup(Project.class);
        String projDir = proj.getProjectDirectory().getPath();
        JFXProjectProperties prop = propInstance.get(projDir);
        if(prop != null) {
            if(prop.isInstanceMarked(category)) {
                // category marked before - create new instance to avoid reuse after Cancel
                prop = null;
            } else {
                prop.markInstance(category);
            }
        }
        if(prop == null) {
            prop = new JFXProjectProperties(context);
            propInstance.put(projDir, prop);
            prop.markInstance(category);
        }
        return prop;
    }
    
    /** Getter method */
    public static JFXProjectProperties getInstanceIfExists(Project proj) {
        assert proj != null;
        String projDir = proj.getProjectDirectory().getPath();
        JFXProjectProperties prop = propInstance.get(projDir);
        if(prop != null) {
            return prop;
        }
        return null;
    }

    /** Getter method */
    public static JFXProjectProperties getInstanceIfExists(Lookup context) {
        Project proj = context.lookup(Project.class);
        return getInstanceIfExists(proj);
    }

    public static void cleanup(Lookup context) {
        Project proj = context.lookup(Project.class);
        String projDir = proj.getProjectDirectory().getPath();
        propInstance.remove(projDir);
    }

    /** Keeps singleton instance of a set of preloader artifact dependencies for any fx project */
    private static Map<String, Set<PreloaderArtifact>> prelArtifacts = new HashMap<String, Set<PreloaderArtifact>>();
    
    /** Factory method */
    private static Set<PreloaderArtifact> getPreloaderArtifacts(@NonNull Project proj) {
        String projDir = proj.getProjectDirectory().getPath();
        Set<PreloaderArtifact> prels = prelArtifacts.get(projDir);
        if(prels == null) {
            prels = new HashSet<PreloaderArtifact>();
            prelArtifacts.put(projDir, prels);
        }
        return prels;
    }
    
    public String getFXRunTimeJar() {
        assert evaluator != null;
        String active = evaluator.getProperty(PLATFORM_ACTIVE);
        JavaPlatform platform = JavaFXPlatformUtils.findJavaPlatform(active);
        if(platform != null) {
            return JavaFXPlatformUtils.getJavaFXRuntimeJar(platform);
        }
        return null;
    }
    
    /** Creates a new instance of JFXProjectProperties */
    private JFXProjectProperties(Lookup context) {
        
        //defaultInstance = provider.getJFXProjectProperties();
        project = context.lookup(Project.class);
        
        if (project != null) {
            j2sePropEval = project.getLookup().lookup(J2SEPropertyEvaluator.class);
            evaluator = j2sePropEval.evaluator();
            
            // Packaging
            binaryEncodeCSS = fxPropGroup.createToggleButtonModel(evaluator, JAVAFX_BINARY_ENCODE_CSS); // set true by default in JFXProjectGenerator

            // Deployment
            allowOfflineModel = fxPropGroup.createToggleButtonModel(evaluator, ALLOW_OFFLINE); // set true by default in JFXProjectGenerator            
            backgroundUpdateCheck = fxPropGroup.createToggleButtonModel(evaluator, UPDATE_MODE_BACKGROUND); // set true by default in JFXProjectGenerator
            installPermanently = fxPropGroup.createToggleButtonModel(evaluator, INSTALL_PERMANENTLY);
            addDesktopShortcut = fxPropGroup.createToggleButtonModel(evaluator, ADD_DESKTOP_SHORTCUT);
            addStartMenuShortcut = fxPropGroup.createToggleButtonModel(evaluator, ADD_STARTMENU_SHORTCUT);
            disableProxy = fxPropGroup.createToggleButtonModel(evaluator, DISABLE_PROXY);
            
            // CustomizerRun
            CONFIGS = new JFXConfigs();
            CONFIGS.read();
            initPreloaderArtifacts(project, CONFIGS);
            CONFIGS.setActive(evaluator.getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG));
            preloaderClassModel = new PreloaderClassComboBoxModel();

            initVersion(evaluator);
            initIcons(evaluator);
            initSigning(evaluator);
            initNativeBundling(evaluator);
            initResources(evaluator, project, CONFIGS);
            initJSCallbacks(evaluator);
            initRest(evaluator);
        }
    }
    
    public static boolean isTrue(final String value) {
        return value != null &&
                (value.equalsIgnoreCase("true") ||  //NOI18N
                 value.equalsIgnoreCase("yes") ||   //NOI18N
                 value.equalsIgnoreCase("on"));     //NOI18N
    }

    public static boolean isNonEmpty(String s) {
        return s != null && !s.isEmpty();
    }
            
    public static boolean isEqual(final String s1, final String s2) {
        return (s1 == null && s2 == null) ||
                (s1 != null && s2 != null && s1.equals(s2));
    }                                   

    public static boolean isEqualIgnoreCase(final String s1, final String s2) {
        return (s1 == null && s2 == null) ||
                (s1 != null && s2 != null && s1.equalsIgnoreCase(s2));
    }                                   

    public static boolean isEqualText(final String s1, final String s2) {
        return ((s1 == null || s1.isEmpty()) && (s2 == null || s2.isEmpty())) ||
                (s1 != null && s2 != null && s1.equals(s2));
    }                                   
    
    /**
     * Used to display named and unnamed parameters in table. The point
     * here is to keep consistency by forbidding invalid parameters, i.e.,
     * named parameters with value but without name.
     */
    public static class PropertiesTableModel extends AbstractTableModel {
        
        private List<Map<String,String>> properties;
        private List<Map<String,String>> defaultProperties;
        private String propSuffixes[];
        private String columnNames[];
        
        public PropertiesTableModel(List<Map<String,String>> props, List<Map<String,String>> defaultProps, String sfxs[], String clmns[]) {
            if (sfxs.length < clmns.length) {
                throw new IllegalArgumentException();
            }
            properties = props;
            defaultProperties = defaultProps;
            propSuffixes = sfxs;
            columnNames = clmns;
        }
        
        public boolean isValid() {
            assert properties != null;
            for(Map<String,String> map : properties) {
                String left = map.get(propSuffixes[0]);
                if(left == null || left.trim().isEmpty()) {
                    for(int c=1; c<columnNames.length; c++) {
                        String right = map.get(propSuffixes[c]);
                        if(right != null && !right.isEmpty()) {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        
        public boolean hasDefaultProperties() {
            return defaultProperties != null;
        }
        
        public boolean isRowEmpty(int index) {
            if(!properties.isEmpty() && index < properties.size()) {
                Map<String,String> last = properties.get(index);
                for(int c=0; c<columnNames.length; c++) {
                    String value = last.get(propSuffixes[c]);
                    if(value != null && !value.isEmpty()) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }

        public boolean isLastRowEmpty() {
            return isRowEmpty(properties.size()-1);
        }
        
        @Override
        public int getRowCount() {
            return properties.size();
        }

        @Override
        public int getColumnCount() {
            return columnNames.length;
        }

        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
        
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            if(columnIndex>0) {
                String left = properties.get(rowIndex).get(propSuffixes[columnIndex-1]);
                if(left == null || left.isEmpty()) {
                    return false;
                }
            }
            return true;
        }
        
        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            properties.get(rowIndex).put(propSuffixes[columnIndex], (String) aValue);
            fireTableCellUpdated(rowIndex, columnIndex);
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return properties.get(rowIndex).get(propSuffixes[columnIndex]);
        }
        
        /**
         * restore defaults if defaultProperties exist, otherwise clean
         */
        public void reset() {
            if(defaultProperties != null) {
                properties.clear();
                properties.addAll(defaultProperties);
            } else {
                properties.clear();
            }
            fireTableDataChanged();
        }
        
        /**
         * Indicates whether it makes sense to enable the Default/Clean
         * button, i.e., whether current table contents differ from the default
         * @return true is a call to reset() would modify data
         */
        public boolean isResettable() {
            if(hasDefaultProperties()) {
                return !areEqual(properties, defaultProperties);
            }
            return !properties.isEmpty();
        }
        
        private boolean areEqual(List<Map<String,String>> list1, List<Map<String,String>> list2) {
            String s1 = getAsString(list1);
            String s2 = getAsString(list2);
            if(isEqualText(s1, s2)) {
                return true;
            }
            return false;
        }
        
        private String getAsString(List<Map<String,String>> list) {
            if(list != null) {
                List<String> l = new LinkedList<String>();
                for(Map<String, String> entry : list) {
                    StringBuilder sb = new StringBuilder();
                    for(int i = 0; i < columnNames.length; i++) {
                        sb.append(propSuffixes[i]);
                        sb.append(entry.get(propSuffixes[i]));
                    }
                    l.add(sb.toString());
                }
                Collections.sort(l);
                return l.toString();
            }
            return null;
        }
        
        public void addRow() {
            Map<String,String> emptyMap = new HashMap<String,String>();
            for (String  suffix : propSuffixes) {
                emptyMap.put(suffix, "");
            }
            properties.add(emptyMap);
            fireTableDataChanged();
        }
        
        public void removeRow(int index) {
            properties.remove(index);
            fireTableDataChanged();
        }
        
        private int getEmptyRow() {
            for(int i = 0; i < properties.size(); i++) {
                if(isRowEmpty(i)) {
                    return i;
                }
            }
            return -1;
        }
        
        public void removeEmptyRows() {
            boolean removed = false;
            while(true) {
                int i = getEmptyRow();
                if(i == -1) {
                    if(removed) {
                        fireTableDataChanged();
                    }
                    return;
                }
                removeRow(i);
                removed = true;
            }
        }

    }

    /**
     * May not be necessary but improves user experience - updates OK button state
     * immediately while editing text in table, i.e., not only after cell editing
     * has finished.
     */
    public static class PropertyCellEditor extends DefaultCellEditor implements CellEditorListener {

        private DocumentListener listener = null;
        private Document document = null;

        public PropertyCellEditor() {
            super(new JTextField());
        }

        public void registerCellEditorListener() {
            addCellEditorListener(this);
        }
        
        public void unregisterCellEditorListener() {
            removeCellEditorListener(this);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            if(document != null) {
                document.removeDocumentListener(listener);
            }
            JTextField editor = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
            document = editor.getDocument();
            listener = new CellEditorDocumentListener(table.getModel(), row, column);
            document.addDocumentListener(listener);
            return editor;
        }

        @Override
        public void editingStopped(ChangeEvent e) {
            removeDocumentListenerReference();
        }

        @Override
        public void editingCanceled(ChangeEvent e) {
            removeDocumentListenerReference();
        }

        private void removeDocumentListenerReference() {
            if(document != null) {
                document.removeDocumentListener(listener);
                document = null;
            }
            listener = null;
        }

        private class CellEditorDocumentListener implements DocumentListener {
            private TableModel model;
            private int row;
            private int column;

            private CellEditorDocumentListener(TableModel model, int row, int column) {
                this.model = model;
                this.row = row;
                this.column = column;
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                update(e);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                update(e);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                update(e);
            }

            private void update(DocumentEvent e) {
                Document d = e.getDocument();
                try {
                    model.setValueAt(d.getText(0, d.getLength()), row, column);
                } catch (BadLocationException ex) {
                    // can be ignored
                }
            }
        }
    }

    /**
     * Content in invalid cells is rendered in emphasized color.
     */
    public static class PropertyCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object o, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, o, isSelected, hasFocus, row, column);
            if(!table.getModel().isCellEditable(row, column)) {
                cell.setForeground(INVALID_CELL_CONTENT_COLOR);
            } else {
                cell.setForeground(UIManager.getColor("textText")); //NOI18N
            }
            return cell;
        }
    }
    
    
    private FileObject getSrcRoot(@NonNull Project project)
    {
        FileObject srcRoot = null;
        for (SourceGroup sg : ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (!isTest(sg.getRootFolder(),project)) {
                srcRoot = sg.getRootFolder();
                break;
            }
        }
        return srcRoot;
    }

    private void initPreloaderArtifacts(@NonNull Project project, @NonNull JFXConfigs configs) {
        Set<PreloaderArtifact> prels = getPreloaderArtifacts(project);
        prels.clear();
        try {
            prels.addAll(getPreloaderArtifactsFromConfigs(configs));
        } catch (IOException ex) {
            // can be ignored
        }
    }
    
    public boolean hasPreloaderInAnyConfig() {
        return hasPreloaderInAnyConfig(CONFIGS);
    }
    
    private boolean hasPreloaderInAnyConfig(@NonNull JFXConfigs configs) {
        if(configs != null) {
            for(String config : configs.getConfigNames()) {
                if(isTrue( configs.getProperty(config, PRELOADER_ENABLED))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private PreloaderArtifact getPreloaderArtifactFromConfig(@NonNull JFXConfigs configs, @NonNull String config, boolean transparent) throws IOException {       
        // check records on any type of preloader from config
        if(configs.hasConfig(config)) {
            
            PreloaderArtifact preloader = null;
            if(!isTrue( transparent ? configs.getPropertyTransparent(config, PRELOADER_ENABLED) : configs.getProperty(config, PRELOADER_ENABLED))) {
                return null;
            }
            String prelTypeString = transparent ? configs.getPropertyTransparent(config, PRELOADER_TYPE) : configs.getProperty(config, PRELOADER_TYPE);
            
            String prelProjDir = transparent ? configs.getPropertyTransparent(config, PRELOADER_PROJECT) : configs.getProperty(config, PRELOADER_PROJECT);
            if (prelProjDir != null && isEqualIgnoreCase(prelTypeString, PreloaderSourceType.PROJECT.getString())) {
                FileObject thisProjDir = project.getProjectDirectory();
                FileObject fo = JFXProjectUtils.getFileObject(thisProjDir, prelProjDir);
                File prelProjDirF = (fo == null) ? null : FileUtil.toFile(fo);                
                if( isTrue(transparent ? configs.getPropertyTransparent(config, PRELOADER_ENABLED) : configs.getProperty(config, PRELOADER_ENABLED)) && prelProjDirF != null && prelProjDirF.exists() ) {
                    FileObject srcRoot = getSrcRoot(getProject());
                    if(srcRoot != null) {
                        prelProjDirF = FileUtil.normalizeFile(prelProjDirF);
                        FileObject prelProjFO = FileUtil.toFileObject(prelProjDirF);
                        final Project proj = ProjectManager.getDefault().findProject(prelProjFO);

                        AntArtifact[] artifacts = proj != null ? AntArtifactQuery.findArtifactsByType(proj, JavaProjectConstants.ARTIFACT_TYPE_JAR) : new AntArtifact[0];
                        List<URI> allURI = new ArrayList<URI>();
                        for(AntArtifact artifact : artifacts) {
                            allURI.addAll(Arrays.asList(artifact.getArtifactLocations()));
                        }
                        if(!allURI.isEmpty()) {
                            URI[] arrayURI = allURI.toArray(new URI[0]);
                            preloader = new PreloaderProjectArtifact(artifacts, arrayURI, srcRoot, ClassPath.COMPILE, prelProjDirF.getAbsolutePath());
                        }
                    }
                }
            }
            if(preloader == null) {
                String prelJar = transparent ? configs.getPropertyTransparent(config, PRELOADER_JAR_PATH) : configs.getProperty(config, PRELOADER_JAR_PATH);
                if(prelJar != null && isEqualIgnoreCase(prelTypeString, PreloaderSourceType.JAR.getString())) {
                    FileObject thisProjDir = project.getProjectDirectory();
                    FileObject fo = JFXProjectUtils.getFileObject(thisProjDir, prelJar);
                    File prelJarF = (fo == null) ? null : FileUtil.toFile(fo);                
                    if( prelJarF != null && prelJarF.exists() ) {
                        FileObject srcRoot = getSrcRoot(getProject());
                        if(srcRoot != null) {
                            URL[] urls = new URL[1];
                            urls[0] = FileUtil.urlForArchiveOrDir(prelJarF);
                            FileObject[] fos = new FileObject[1];
                            fos[0] = FileUtil.toFileObject(prelJarF);
                            preloader = new PreloaderJarArtifact(urls, fos, srcRoot, ClassPath.COMPILE, urls[0].toString());
                        }
                    }
                }
            }
            return preloader;
        }
        return null;
    }
    
    private Set<PreloaderArtifact> getPreloaderArtifactsFromConfigs(@NonNull JFXConfigs configs) throws IOException {       
        Set<PreloaderArtifact> preloaderArtifacts = new HashSet<PreloaderArtifact>();
        // check records on all preloaders from all configurations
        for(String config : configs.getConfigNames()) {
            PreloaderArtifact preloader = getPreloaderArtifactFromConfig(configs, config, false);
            if(preloader != null) {
                preloaderArtifacts.add(preloader);
            }
        }
        return preloaderArtifacts;
    }

    public void updatePreloaderDependencies() {
        try {
            updatePreloaderDependencies(CONFIGS);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void updatePreloaderDependencies(@NonNull final JFXConfigs configs) throws IOException {
        // depeding on the currently (de)selected preloaders update project dependencies,
        // i.e., remove disabled/deleted preloader project dependencies and add enabled/added preloader project dependencies
        Set<PreloaderArtifact> preloaderArtifacts = getPreloaderArtifacts(getProject());
        for(PreloaderArtifact artifact : preloaderArtifacts) {
            artifact.setValid(false);
        }
        final PreloaderArtifact preloaderActive = getPreloaderArtifactFromConfig(configs, configs.getActive(), true);
        // collect all dependencies from any configuration
        for(final String config : configs.getConfigNames()) {
            final PreloaderArtifact preloader = getPreloaderArtifactFromConfig(configs, config, false);
            if(preloader != null) {
                boolean updated = false;
                for(PreloaderArtifact a : preloaderArtifacts) {
                    if(a.equals(preloader)) {
                        a.setValid(true);
                        updated = true;
                    }
                }
                if(!updated) {
                    preloader.setValid(true);
                    preloaderArtifacts.add(preloader);
                }
            }
        }
        // remove all dependencies not-specified in active configuration (and add the active one)
        Set<PreloaderArtifact> toRemove = new HashSet<PreloaderArtifact>();
        for(final PreloaderArtifact artifact : preloaderArtifacts) {
            if(preloaderActive == null || !preloaderActive.equals(artifact)) {
                toRemove.add(artifact);
            }
        }
        if(preloaderActive != null || !toRemove.isEmpty()) {
            final Set<PreloaderArtifact> toRemoveFinal = Collections.unmodifiableSet(toRemove);
            ProjectManager.mutex().postWriteRequest(new Runnable() {
                @Override
                public void run() {
                    if(preloaderActive != null) {
                        try {
                            preloaderActive.addDependency();
                        } catch(IOException e) {
                            LOG.log(Level.SEVERE, "Preloader dependency addition failed."); // NOI18N
                        }
                    }
                    try {
                        for(final PreloaderArtifact artifact : toRemoveFinal) {
                            artifact.removeDependency();
                        }
                    } catch(IOException e) {
                        LOG.log(Level.SEVERE, "Preloader dependency removal failed."); // NOI18N
                    }
                }
            });
        }
        // remove from preloaderArtifacts those not more present in any config
        toRemove.clear();
        for(final PreloaderArtifact artifact : preloaderArtifacts) {
            if(!artifact.isValid()) {
                toRemove.add(artifact);
            }
        }
        
        preloaderArtifacts.removeAll(toRemove);
    }
    
    private static boolean isTest(final @NonNull FileObject root, final @NonNull Project project) {
        assert root != null;
        assert project != null;
        final ClassPath cp = ClassPath.getClassPath(root, ClassPath.COMPILE);
        for (ClassPath.Entry entry : cp.entries()) {
            final FileObject[] srcRoots = SourceForBinaryQuery.findSourceRoots(entry.getURL()).getRoots();
            for (FileObject srcRoot : srcRoots) {
                if (project.equals(FileOwnerQuery.getOwner(srcRoot))) {
                    return true;
                }
            }
        }
        return false;
    }

    private void storeRest(@NonNull EditableProperties editableProps, @NonNull EditableProperties privProps) {
        // create extended manifest attribute properties if not existing
        if(!editableProps.containsKey(MANIFEST_CUSTOM_CODEBASE) && !privProps.containsKey(MANIFEST_CUSTOM_CODEBASE)) {
            editableProps.setProperty(MANIFEST_CUSTOM_CODEBASE, "*"); // NOI18N
            editableProps.setComment(MANIFEST_CUSTOM_CODEBASE, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_manifest_custom_codebase")}, false); // NOI18N
        }
        if(!editableProps.containsKey(MANIFEST_CUSTOM_PERMISSIONS) && !privProps.containsKey(MANIFEST_CUSTOM_PERMISSIONS)) {
            editableProps.setProperty(MANIFEST_CUSTOM_PERMISSIONS, ""); // NOI18N
            editableProps.setComment(MANIFEST_CUSTOM_PERMISSIONS, new String[]{"# " + NbBundle.getMessage(JFXProjectUtils.class, "COMMENT_manifest_custom_permissions")}, false); // NOI18N
        }
        // store implementation version
        setOrRemove(editableProps, IMPLEMENTATION_VERSION, implVersion);
        // store signing info
        editableProps.setProperty(JAVAFX_SIGNING_ENABLED, signingEnabled ? "true" : "false"); //NOI18N
        editableProps.setProperty(JAVAFX_SIGNING_BLOB, signingBlob ? "true" : "false"); //NOI18N
        editableProps.setProperty(JAVAFX_SIGNING_TYPE, signingType.getString());
        setOrRemove(editableProps, JAVAFX_SIGNING_KEY, signingKeyAlias);
        setOrRemove(editableProps, JAVAFX_SIGNING_KEYSTORE, signingKeyStore);
        editableProps.setProperty(PERMISSIONS_ELEVATED, permissionsElevated ? "true" : "false"); //NOI18N
        setOrRemove(privProps, JAVAFX_SIGNING_KEYSTORE_PASSWORD, signingKeyStorePassword);
        setOrRemove(privProps, JAVAFX_SIGNING_KEY_PASSWORD, signingKeyPassword);        
        // store native bundling info
        editableProps.setProperty(NATIVE_BUNDLING_ENABLED, nativeBundlingEnabled ? "true" : "false"); //NOI18N
        // store icons
        setOrRemove(editableProps, ICON_FILE, wsIconPath);
        setOrRemove(editableProps, SPLASH_IMAGE_FILE, splashImagePath);
        setOrRemove(editableProps, NATIVE_ICON_FILE, nativeIconPath);
        // store requested RT
        setOrRemove(editableProps, REQUEST_RT, requestedRT);
        // store resources
        storeResources(editableProps);
        // store JavaScript callbacks
        storeJSCallbacks(editableProps);
    }

    private void setOrRemove(EditableProperties props, String name, char [] value) {
        setOrRemove(props, name, value != null ? new String(value) : null);
    }

    private void setOrRemove(@NonNull EditableProperties props, @NonNull String name, String value) {
        if (value != null) {
            props.setProperty(name, value);
        } else {
            props.remove(name);
        }
    }
        
    public void store() throws IOException {
        String fxEnabled = evaluator.getProperty(JFXProjectProperties.JAVAFX_ENABLED);
        if(isTrue(fxEnabled)) {
            storeFX();
        } else {
            storeSE();
        }
    }
    
    private void storeFX() throws IOException {
        updatePreloaderDependencies(CONFIGS);
        CONFIGS.storeActive();
        final EditableProperties ep = new EditableProperties(true);
        final FileObject projPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        final EditableProperties pep = new EditableProperties(true);
        final FileObject privPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PRIVATE_PROPERTIES_PATH);        
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    final InputStream is = projPropsFO.getInputStream();
                    final InputStream pis = privPropsFO.getInputStream();
                    try {
                        ep.load(is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                    try {
                        pep.load(pis);
                    } finally {
                        if (pis != null) {
                            pis.close();
                        }
                    }
                    
                    fxPropGroup.store(ep);
                    storeRest(ep, pep);
                    CONFIGS.store(ep, pep);
                    updatePreloaderComment(ep);
                    //JFXProjectUtils.updateClassPathExtensionProperties(ep);
                    logProps(ep);

                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        lock = projPropsFO.lock();
                        os = projPropsFO.getOutputStream(lock);
                        ep.store(os);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        if (os != null) {
                            os.close();
                        }
                    }
                    try {
                        lock = privPropsFO.lock();
                        os = privPropsFO.getOutputStream(lock);
                        pep.store(os);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        if (os != null) {
                            os.close();
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException mux) {
            throw (IOException) mux.getException();
        }
    }

    private void storeSE() throws IOException {
        final EditableProperties ep = new EditableProperties(true);
        final FileObject projPropsFO = project.getProjectDirectory().getFileObject(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        try {
            final InputStream is = projPropsFO.getInputStream();
            ProjectManager.mutex().readAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    try {
                        ep.load(is);
                    } finally {
                        if (is != null) {
                            is.close();
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException mux) {
            throw (IOException) mux.getException();
        }
        setOrRemove(ep, JAVASE_KEEP_JFXRT_ON_CLASSPATH, keepJFXRTonCP ? "true" : null); //NOI18N
        //Copylibs excludes for J2SE Project with JFX extension
        String copyLibsExcludes = ep.getProperty(COPYLIBS_EXCLUDES);
        if (keepJFXRTonCP) {
            if (copyLibsExcludes == null || copyLibsExcludes.isEmpty()) {
                copyLibsExcludes = JFX_EXTENSION_CPREF;
            } else if (copyLibsExcludes.indexOf(JFX_EXTENSION_CPREF)<0){
                copyLibsExcludes = copyLibsExcludes + ':' + JFX_EXTENSION_CPREF;
            }
            setOrRemove(ep, COPYLIBS_EXCLUDES, copyLibsExcludes);
        } else {
            if (copyLibsExcludes != null) {
                copyLibsExcludes = JFXProjectUtils.removeFromPath(copyLibsExcludes,JFX_EXTENSION_CPREF);
                if (copyLibsExcludes.isEmpty()) {
                    copyLibsExcludes = null;
                }
            }
            setOrRemove(ep, COPYLIBS_EXCLUDES, copyLibsExcludes);
        }
        //JFXProjectUtils.updateClassPathExtensionProperties(ep);
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    OutputStream os = null;
                    FileLock lock = null;
                    try {
                        lock = projPropsFO.lock();
                        os = projPropsFO.getOutputStream(lock);
                        ep.store(os);
                    } finally {
                        if (lock != null) {
                            lock.releaseLock();
                        }
                        if (os != null) {
                            os.close();
                        }
                    }
                    return null;
                }
            });
        } catch (MutexException mux) {
            throw (IOException) mux.getException();
        }
    }
    
    private void updatePreloaderComment(EditableProperties ep) {
        if(isTrue(ep.get(JFXProjectProperties.PRELOADER_ENABLED))) {
            ep.setComment(JFXProjectProperties.PRELOADER_ENABLED, new String[]{"# " + NbBundle.getMessage(JFXProjectProperties.class, "COMMENT_use_preloader")}, false); // NOI18N    
        } else {
            ep.setComment(JFXProjectProperties.PRELOADER_ENABLED, new String[]{"# " + NbBundle.getMessage(JFXProjectProperties.class, "COMMENT_dontuse_preloader")}, false); // NOI18N    
        }
    }

    private void initVersion(PropertyEvaluator eval) {
        implVersion = eval.getProperty(IMPLEMENTATION_VERSION);
        if(implVersion == null) {
            implVersion = IMPLEMENTATION_VERSION_DEFAULT;
        }
    }
    
    private void initIcons(PropertyEvaluator eval) {
        wsIconPath = eval.getProperty(ICON_FILE);
        splashImagePath = eval.getProperty(SPLASH_IMAGE_FILE);
        nativeIconPath = eval.getProperty(NATIVE_ICON_FILE);
    }
    
    private void initSigning(PropertyEvaluator eval) {
        String enabled = eval.getProperty(JAVAFX_SIGNING_ENABLED);
        String blob = eval.getProperty(JAVAFX_SIGNING_BLOB);
        String signedProp = eval.getProperty(JAVAFX_SIGNING_TYPE);
        signingEnabled = isTrue(enabled);
        signingBlob = isTrue(blob);
        if(signedProp == null) {
            signingType = SigningType.NOSIGN;
        } else {
            if(signedProp.equalsIgnoreCase(SigningType.SELF.getString())) {
                signingType = SigningType.SELF;
            } else {
                if(signedProp.equalsIgnoreCase(SigningType.KEY.getString())) {
                    signingType = SigningType.KEY;
                } else {
                    signingType = SigningType.NOSIGN;
                }
            }
        }
        signingKeyStore = eval.getProperty(JAVAFX_SIGNING_KEYSTORE);
        //if (signingKeyStore == null) signingKeyStore = "";
        signingKeyAlias = eval.getProperty(JAVAFX_SIGNING_KEY);
        //if (signingKeyAlias == null) signingKeyAlias = "";
        if (eval.getProperty(JAVAFX_SIGNING_KEYSTORE_PASSWORD) != null) {
            signingKeyStorePassword = eval.getProperty(JAVAFX_SIGNING_KEYSTORE_PASSWORD).toCharArray();
        }
        if (eval.getProperty(JAVAFX_SIGNING_KEY_PASSWORD) != null) {
            signingKeyPassword = eval.getProperty(JAVAFX_SIGNING_KEY_PASSWORD).toCharArray();
        }
        permissionsElevated = isTrue(eval.getProperty(PERMISSIONS_ELEVATED));
    }
    
    private void initNativeBundling(PropertyEvaluator eval) {
        String fxEnabled = evaluator.getProperty(JFXProjectProperties.JAVAFX_ENABLED);
        if(isTrue(fxEnabled)) {
            String enabled = eval.getProperty(NATIVE_BUNDLING_ENABLED);
            nativeBundlingEnabled = isTrue(enabled);
        }
    }
    
    private void initRest(PropertyEvaluator eval) {
        requestedRT = eval.getProperty(REQUEST_RT);
        keepJFXRTonCP = isTrue(eval.getProperty(JAVASE_KEEP_JFXRT_ON_CLASSPATH));
    }
    
    private boolean isParentOf(File parent, File child) {
        if(parent == null || child == null) {
            return false;
        }
        if(!parent.exists() || !child.exists()) {
            return false;
        }
        FileObject parentFO = FileUtil.toFileObject(parent);
        FileObject childFO = FileUtil.toFileObject(child);
        return FileUtil.isParentOf(parentFO, childFO);
    }

    private void initResources (final PropertyEvaluator eval, final Project prj, final JFXConfigs configs) {
        final String lz = eval.getProperty(DOWNLOAD_MODE_LAZY_JARS); //old way, when changed rewritten to new
        final String rcp = eval.getProperty(RUN_CP);        
        final String bc = eval.getProperty(BUILD_CLASSES);
        final String plat = eval.getProperty(PLATFORM_ACTIVE);
        JavaPlatform platform = JavaFXPlatformUtils.findJavaPlatform(plat);
        final File prjDir = FileUtil.toFile(prj.getProjectDirectory());
        final File bcDir = bc == null ? null : PropertyUtils.resolveFile(prjDir, bc);
        final List<File> lazyFileList = new ArrayList<File>();
        String[] paths;
        if (lz != null) {
            paths = PropertyUtils.tokenizePath(lz);            
            for (String p : paths) {
                lazyFileList.add(PropertyUtils.resolveFile(prjDir, p));
            }
        }
        paths = rcp != null ? PropertyUtils.tokenizePath(rcp) : new String[0];
        String mainJar = eval.getProperty(DIST_JAR);
        final File mainFile = mainJar != null ? PropertyUtils.resolveFile(prjDir, mainJar) : null;
        List<FileObject> preloaders = new ArrayList<FileObject>();
        try {
            for(PreloaderArtifact pa : getPreloaderArtifactsFromConfigs(configs)) {
                preloaders.addAll(Arrays.asList(pa.getFileObjects()));
            }
        } catch (IOException ex) {
            // no need to react
        }

        Collection<FileObject> platfF = platform != null ? platform.getInstallFolders() : null;
        final List<File> resFileList = new ArrayList<File>(paths.length);
        for (String p : paths) {
            if (p.startsWith("${") && p.endsWith("}")) {    //NOI18N
                continue;
            }
            final File f = PropertyUtils.resolveFile(prjDir, p);
            if (!f.exists()) {
                continue;
            }
            if (mainFile != null && f.equals(mainFile)) {
                continue;
            }
            if(platfF != null) {
                boolean cont = false;
                for(FileObject fo : platfF) {
                    if(isParentOf(FileUtil.toFile(fo), f)) {
                        cont = true;
                    }
                }
                if(cont) {
                    continue;
                }
            }
            boolean isPrel = false;
            for(FileObject prelfo : preloaders) {
                File prelf = FileUtil.toFile(prelfo);
                if(prelf != null && prelf.equals(f)) {
                    isPrel = true;
                    continue;
                }
            }
            if (!isPrel && (bc == null || !bcDir.equals(f)) ) {
                resFileList.add(f);
                if (isTrue(eval.getProperty(String.format(DOWNLOAD_MODE_LAZY_FORMAT, f.getName())))) {
                    lazyFileList.add(f);
                }
            }
        }
        lazyJars = lazyFileList;
        runtimeCP = resFileList;
        lazyJarsChanged = false;
    }
    
    private void storeResources(final EditableProperties props) {
        if (lazyJarsChanged) {
            //Remove old way if exists
            props.remove(DOWNLOAD_MODE_LAZY_JARS);
            final Iterator<Map.Entry<String,String>> it = props.entrySet().iterator();
            while (it.hasNext()) {
                if (it.next().getKey().startsWith(DOWNLOAD_MODE_LAZY_JAR)) {
                    it.remove();
                }
            }
            for (File lazyJar : lazyJars) {
                props.setProperty(String.format(DOWNLOAD_MODE_LAZY_FORMAT, lazyJar.getName()), "true");  //NOI18N
            }
        }
    }

    private void initJSCallbacks (final PropertyEvaluator eval) {
        String platformName = eval.getProperty(PLATFORM_ACTIVE);
        Map<String,List<String>/*|null*/> callbacks = JFXProjectUtils.getJSCallbacks(platformName);
        Map<String,String/*|null*/> result = new LinkedHashMap<String,String/*|null*/>();
        for(Map.Entry<String,List<String>/*|null*/> entry : callbacks.entrySet()) {
            String v = eval.getProperty(JFXProjectProperties.JAVASCRIPT_CALLBACK_PREFIX + entry.getKey());
            if(v != null && !v.isEmpty()) {
                result.put(entry.getKey(), v);
            }
        }
        jsCallbacks = result;
        jsCallbacksChanged = false;
    }
    
    private void storeJSCallbacks(final EditableProperties props) {
        if (jsCallbacksChanged && jsCallbacks != null) {
            for (Map.Entry<String,String> entry : jsCallbacks.entrySet()) {
                if(entry.getValue() != null && !entry.getValue().isEmpty()) {
                    props.setProperty(JAVASCRIPT_CALLBACK_PREFIX + entry.getKey(), entry.getValue());  //NOI18N
                } else {
                    props.remove(JAVASCRIPT_CALLBACK_PREFIX + entry.getKey());
                }
            }
        }
    }

    public class PreloaderClassComboBoxModel extends DefaultComboBoxModel {
        
        private volatile boolean filling = false;
        private ChangeListener changeListener = null;
              
        public PreloaderClassComboBoxModel() {
            fillNoPreloaderAvailable();
        }
        
        public void addChangeListener (ChangeListener l) {
            changeListener = l;
        }

        public void removeChangeListener (ChangeListener l) {
            changeListener = null;
        }

        public final void fillNoPreloaderAvailable() {
            removeAllElements();
            addElement(NbBundle.getMessage(JFXProjectProperties.class, "MSG_ComboNoPreloaderClassAvailable"));  // NOI18N
        }
        
        public void fillFromProject(final Project project, final String select, final JFXConfigs configs, final String activeConfig) {
            final Collection<? extends FileObject> roots = JFXProjectUtils.getClassPathMap(project).keySet();
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    if(!filling) {
                        filling = true;
                        removeAllElements();
                        if(project == null) {
                            addElement(NbBundle.getMessage(JFXProjectProperties.class, "MSG_ComboNoPreloaderClassAvailable"));  // NOI18N
                            return;
                        }
                        final Set<String> appClassNames = JFXProjectUtils.getAppClassNames(roots, "javafx.application.Preloader"); //NOI18N
                        if(appClassNames.isEmpty()) {
                            addElement(NbBundle.getMessage(JFXProjectProperties.class, "MSG_ComboNoPreloaderClassAvailable"));  // NOI18N
                        } else {
                            addElements(appClassNames);
                            if(select != null) {
                                setSelectedItem(select);
                            }
                            String verify = (String)getSelectedItem();
                            if(!isEqual(configs.getPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_CLASS), verify)) {
                                configs.setPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_CLASS, verify);
                            }
                        }
                        if (changeListener != null) {
                            changeListener.stateChanged (appClassNames.isEmpty() ? null : new ChangeEvent (this));
                        }
                        filling = false;
                    }
                }
            });            
        }

        public void fillFromJAR(final FileObject jarFile, final JFXProjectProperties fxProps, final String select, final JFXConfigs configs, final String activeConfig) {
            RequestProcessor.getDefault().post(new Runnable() {
                @Override
                public void run() {
                    if(!filling) {
                        filling = true;
                        removeAllElements();
                        if(jarFile == null) {
                            addElement(NbBundle.getMessage(JFXProjectProperties.class, "MSG_ComboNoPreloaderClassAvailable"));  // NOI18N
                            return;
                        }
                        final Set<String> appClassNames = JFXProjectUtils.getAppClassNamesInJar(jarFile, "javafx.application.Preloader", fxProps.getFXRunTimeJar()); //NOI18N    
                        appClassNames.remove("com.javafx.main.Main"); // NOI18N
                        appClassNames.remove("com.javafx.main.NoJavaFXFallback"); // NOI18N
                        if(appClassNames.isEmpty()) {
                            addElement(NbBundle.getMessage(JFXProjectProperties.class, "MSG_ComboNoPreloaderClassAvailable"));  // NOI18N
                        } else {
                            addElements(appClassNames);
                            if(select != null) {
                                setSelectedItem(select);
                            }
                            String verify = (String)getSelectedItem();
                            if(!isEqual(configs.getPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_CLASS), verify)) {
                                configs.setPropertyTransparent(activeConfig, JFXProjectProperties.PRELOADER_CLASS, verify);
                            }
                        }
                        if (changeListener != null) {
                            changeListener.stateChanged (appClassNames.isEmpty() ? null : new ChangeEvent (this));
                        }
                        filling = false;
                    }
                }
            });            
        }

        private void addElements(Set<String> elems) {
            for (String elem : elems) {
                addElement(elem);
            }
        }
        
    }
    
    /**
     * Each preloader specified in project configurations needs
     * to be added/removed to/from project dependencies whenever
     * configurations change (see Run category in Project Properties
     * dialog). 
     * List of preoader artifacts is thus needed to keep track which
     * project dependencies are preloader related.
     */
    abstract class PreloaderArtifact {
        
        /**
         * Dependency validity tag
         */
        private boolean valid;
        
        /**
         * Add {@code this} to dependencies of project if it is not there yet
         * @return true if preloader artifact has been added, false if it was already there
         */
        abstract boolean addDependency() throws IOException, UnsupportedOperationException;
        
        /**
         * Remove {@code this} from dependencies of project if it is there
         * @return true if preloader artifact has been removed, false if it was not among project dependencies
         */
        abstract boolean removeDependency() throws IOException, UnsupportedOperationException;
        
        /**
         * Returns array of files represented by this PreloaderArtifact
         * @return array of FileObjects of files represented by this object
         */
        abstract FileObject[] getFileObjects();
        
        /**
         * Set the validity tag for {@code this} artifact
         * @param valid true for dependencies to be kept, false for dependencies to be removed
         */
        void setValid(boolean valid) {
            this.valid = valid;
        }
        
        /**
         * Get the validity tag for {@code this} artifact
         * @return valid true for dependencies to be kept, false for dependencies to be removed
         */
        boolean isValid() {
            return valid;
        }
    }
    
    class PreloaderProjectArtifact extends PreloaderArtifact {

        private final String ID;
        private final AntArtifact[] artifacts;
        private final URI[] artifactElements;
        private final FileObject projectArtifact;
        private final String classPathType;
                
        PreloaderProjectArtifact(final @NonNull AntArtifact[] artifacts, final @NonNull URI[] artifactElements,
            final @NonNull FileObject projectArtifact, final @NonNull String classPathType, final @NonNull String ID) {
            this.artifacts = artifacts;
            this.artifactElements = artifactElements;
            this.projectArtifact = projectArtifact;
            this.classPathType = classPathType;
            this.ID = ID;
        }
        
        @Override
        public boolean addDependency() throws IOException, UnsupportedOperationException {
            return ProjectClassPathModifier.addAntArtifacts(artifacts, artifactElements, projectArtifact, classPathType);
        }

        @Override
        public boolean removeDependency()  throws IOException, UnsupportedOperationException {
            return ProjectClassPathModifier.removeAntArtifacts(artifacts, artifactElements, projectArtifact, classPathType);
        }

        @Override
        public boolean equals(Object that){
            if ( this == that ) return true;
            if ( !(that instanceof PreloaderProjectArtifact) ) return false;
            PreloaderProjectArtifact concrete = (PreloaderProjectArtifact)that;
            return ID.equals(concrete.ID);
        }

        @Override
        final FileObject[] getFileObjects() {
            List<FileObject> l = new ArrayList<FileObject>();
            for(AntArtifact a : artifacts) {
                l.addAll(Arrays.asList(a.getArtifactFiles()));
            }
            return l.toArray(new FileObject[0]);
        }
    }

    class PreloaderJarArtifact extends PreloaderArtifact {

        private final String ID;
        private final URL[] classPathRoots;
        private final FileObject[] fileObjects;
        private final FileObject projectArtifact;
        private final String classPathType;
                
        PreloaderJarArtifact(final @NonNull URL[] classPathRoots, final @NonNull FileObject[] fileObjects, final @NonNull FileObject projectArtifact, 
                final @NonNull String classPathType, final @NonNull String ID) {
            this.classPathRoots = classPathRoots;
            this.fileObjects = fileObjects;
            this.projectArtifact = projectArtifact;
            this.classPathType = classPathType;
            this.ID = ID;
        }
        
        @Override
        public boolean addDependency() throws IOException, UnsupportedOperationException {
            return ProjectClassPathModifier.addRoots(classPathRoots, projectArtifact, classPathType);
        }

        @Override
        public boolean removeDependency()  throws IOException, UnsupportedOperationException {
            return ProjectClassPathModifier.removeRoots(classPathRoots, projectArtifact, classPathType);
        }
        
        @Override
        public boolean equals(Object that){
            if ( this == that ) return true;
            if ( !(that instanceof PreloaderJarArtifact) ) return false;
            PreloaderJarArtifact concrete = (PreloaderJarArtifact)that;
            return ID.equals(concrete.ID);
        }

        @Override
        final FileObject[] getFileObjects() {
            return fileObjects;
        }
    }

    /**
     * Project configurations maintenance class
     * 
     * Getter/Setter naming conventions:
     * "Property" in method name -> method deals with single properties in configuration given by parameter config
     * "Default" in method name -> method deals with properties in default configuration
     * "Active" in method name -> method deals with properties in currently chosen configuration
     * "Transparent" in method name -> method deals with property in configuration fiven by parameter config if
     *     exists, or with property in default configuration otherwise. This is to provide simple access to
     *     union of default and non-default properties that are to be presented to users in non-default configurations
     * "Param" in method name -> metod deals with properties representing sets of application parameters
     */
    public class JFXConfigs extends JFXProjectConfigurations {

        // property groups
        private String PRELOADER_GROUP_NAME = "preloader"; // NOI18N
        private List<String> PRELOADER_PROPERTIES = Arrays.asList(new String[] {
            JFXProjectProperties.PRELOADER_ENABLED, JFXProjectProperties.PRELOADER_TYPE, JFXProjectProperties.PRELOADER_PROJECT, 
            JFXProjectProperties.PRELOADER_JAR_PATH, JFXProjectProperties.PRELOADER_JAR_FILENAME, JFXProjectProperties.PRELOADER_CLASS});

        private String BROWSER_GROUP_NAME = "browser"; // NOI18N
        private List<String> BROWSER_PROPERTIES = Arrays.asList(new String[] {
            JFXProjectProperties.RUN_IN_BROWSER, JFXProjectProperties.RUN_IN_BROWSER_PATH, JFXProjectProperties.RUN_IN_BROWSER_ARGUMENTS});
        
        public final List<String> getPreloaderProperties() {
            return Collections.unmodifiableList(PRELOADER_PROPERTIES);
        }
        
        public final List<String> getBrowserProperties() {
            return Collections.unmodifiableList(BROWSER_PROPERTIES);
        }

        JFXConfigs() {
            super(project.getProjectDirectory());
            registerProjectProperties(new String[] {
                ProjectProperties.MAIN_CLASS, MAIN_CLASS, /*APPLICATION_ARGS,*/ RUN_JVM_ARGS, 
                PRELOADER_ENABLED, PRELOADER_TYPE, PRELOADER_PROJECT, PRELOADER_JAR_PATH, PRELOADER_JAR_FILENAME, PRELOADER_CLASS, 
                RUN_WORK_DIR, RUN_APP_WIDTH, RUN_APP_HEIGHT, RUN_IN_HTMLTEMPLATE, RUN_IN_BROWSER, RUN_IN_BROWSER_PATH, RUN_AS});
            registerPrivateProperties(new String[] {
                RUN_WORK_DIR, RUN_IN_HTMLTEMPLATE, RUN_IN_BROWSER, RUN_IN_BROWSER_PATH, RUN_IN_BROWSER_ARGUMENTS, RUN_AS});
            registerStaticProperties(new String[] {
                RUN_AS});
            
            Map<String, String> substituteMissing = new HashMap<String, String>();
            substituteMissing.put(RUN_APP_WIDTH, DEFAULT_APP_WIDTH);
            substituteMissing.put(RUN_APP_HEIGHT, DEFAULT_APP_HEIGHT);
            registerDefaultsIfMissing(substituteMissing);
            
            registerCleanEmptyProjectProperties(new String[] {
                MAIN_CLASS, RUN_JVM_ARGS, 
                PRELOADER_ENABLED, PRELOADER_TYPE, PRELOADER_PROJECT, PRELOADER_JAR_PATH, PRELOADER_JAR_FILENAME, PRELOADER_CLASS, 
                RUN_APP_WIDTH, RUN_APP_HEIGHT});
            registerCleanEmptyPrivateProperties(new String[] {
                RUN_WORK_DIR, RUN_IN_HTMLTEMPLATE, RUN_IN_BROWSER, RUN_IN_BROWSER_PATH});
            
            defineGroup(PRELOADER_GROUP_NAME, getPreloaderProperties());
            defineGroup(BROWSER_GROUP_NAME, getBrowserProperties());
        }
        
    }
    
    static void logProps(EditableProperties ep) {
        LOG.log(Level.INFO, PRELOADER_ENABLED + " = " + (ep.get(PRELOADER_ENABLED)==null ? "null" : ep.get(PRELOADER_ENABLED)));
        LOG.log(Level.INFO, PRELOADER_TYPE + " = " + (ep.get(PRELOADER_TYPE)==null ? "null" : ep.get(PRELOADER_TYPE)));
        LOG.log(Level.INFO, PRELOADER_PROJECT + " = " + (ep.get(PRELOADER_PROJECT)==null ? "null" : ep.get(PRELOADER_PROJECT)));
        LOG.log(Level.INFO, PRELOADER_CLASS + " = " + (ep.get(PRELOADER_CLASS)==null ? "null" : ep.get(PRELOADER_CLASS)));
        LOG.log(Level.INFO, PRELOADER_JAR_FILENAME + " = " + (ep.get(PRELOADER_JAR_FILENAME)==null ? "null" : ep.get(PRELOADER_JAR_FILENAME)));
        LOG.log(Level.INFO, PRELOADER_JAR_PATH + " = " + (ep.get(PRELOADER_JAR_PATH)==null ? "null" : ep.get(PRELOADER_JAR_PATH)));
    }
}
