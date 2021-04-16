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
package org.netbeans.modules.cordova;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cordova.platforms.spi.BuildPerformer;
import org.netbeans.modules.cordova.platforms.spi.Device;
import org.netbeans.modules.cordova.platforms.api.PlatformManager;
import org.netbeans.modules.cordova.project.MobileConfigurationImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import static org.netbeans.modules.cordova.PropertyNames.*;
import org.netbeans.modules.cordova.platforms.api.ClientProjectUtilities;
import org.netbeans.modules.cordova.platforms.spi.MobilePlatform;
import org.netbeans.modules.cordova.wizard.CordovaProjectExtender;
import org.netbeans.modules.cordova.platforms.spi.SDK;
import org.netbeans.modules.cordova.platforms.api.WebKitDebuggingSupport;
import org.netbeans.modules.cordova.project.ConfigUtils;
import org.netbeans.modules.cordova.project.CordovaCustomizerPanel;
import org.netbeans.modules.cordova.project.CordovaBrowserFactory;
import org.netbeans.modules.cordova.updatetask.SourceConfig;
import org.netbeans.modules.web.browser.api.BrowserFamilyId;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.browser.spi.BrowserURLMapperImplementation;
import org.netbeans.modules.web.browser.spi.ProjectBrowserProvider;
import org.netbeans.modules.web.common.api.ServerURLMapping;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.execution.ExecutorTask;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jan Becicka
 */
@ServiceProvider(service = BuildPerformer.class)
public class CordovaPerformer implements BuildPerformer {
    public static final String NAME_BUILD_XML = "build.xml"; // NOI18N
    public static final String NAME_PLUGINS_PROPERTIES = "plugins.properties"; //NII18N
    public static final String NAME_CONFIG_XML = "config.xml"; // NOI18N
    public static final String PATH_BUILD_XML = "nbproject/" + NAME_BUILD_XML; // NOI18N
    public static final String PATH_PLUGINS_PROPERTIES = "nbproject/" + NAME_PLUGINS_PROPERTIES; // NOI18N
    public static final String PATH_EXTRA_ANT_JAR = "ant/extra/org-netbeans-modules-cordova-projectupdate.jar"; // NOI18N
    public static final String DEFAULT_ID_PREFIX = "com.coolappz"; // NOI18N
    public static final String DEFAULT_EMAIL = "info@com.coolappz"; // NOI18N
    public static final String DEFAULT_WWW = "http://www.coolappz.com";
    public static final String DEFAULT_VERSION = "1.0.0"; // NOI18N
    public static final String DEFAULT_DESCRIPTION = Bundle.DSC_Cordova();
    public static final String PROP_BUILD_SCRIPT_VERSION = "cordova_build_script_version"; // NOI18N
    public static final String PROP_PROVISIONING_PROFILE = "ios.provisioning.profile"; // NOI18N
    public static final String PROP_CERTIFICATE_NAME = "ios.certificate.name"; // NOI18N
    public static final String WWW_NB_TEMP = "www_nb_temp";
    public static final String WWW = "www";

    static final String GRUNT_CUSTOMIZER_IDENT = "Grunt";
    static final String GULP_CUSTOMIZER_IDENT = "Gulp";
    
    private final RequestProcessor RP = new RequestProcessor(CordovaPerformer.class.getName(), 10);

    //must be increased on each change in build.xml
    private final int BUILD_SCRIPT_VERSION = 52;
    
    public static CordovaPerformer getDefault() {
        return Lookup.getDefault().lookup(CordovaPerformer.class);
    }
    
    
    public Task createPlatforms(final Project project) {
        return perform("upgrade-to-cordova-project", project, false);      
    }
    
    @NbBundle.Messages({
        "LBL_InstallThroughItunes=Install application using iTunes and tap on it",
        "CTL_InstallAndRun=Install and Run",
        "DSC_Cordova=Cordova Application",
        "ERR_StartFileNotFound=Start file cannot be found.",
        "ERR_NO_Cordova=NetBeans cannot find cordova or git on your PATH. Please install cordova and git.\n" +
            "NetBeans might require restart for changes to take effect.\n",
        "ERR_NO_Provisioning=Provisioning Profile not found.\nPlease use XCode and install valid Provisioning Profile for your device.",
        "ERR_NOT_Cordova=Create Cordova Resources and rename site root to 'www'?",
        "CTL_SelectBuildTool=Select build tool",
        "# {0} - project name", 
        "# {1} - build file", 
        "# {2} - build tool name", 
        "MSG_SelectBuildTool=The project ''{0}'' contains ''{1}''.\nClick Ant to execute Ant targets for this project.\nClick ''{2}'' to assign IDE actions to ''{2}'' tasks.",
        "ERR_SiteRootNotDefined=Project Site Root Folder must be located in project directory"    
    })
    @Override
    public ExecutorTask perform(final String target, final Project project) {    
        return perform(target, project, true);
    }
    
    private ExecutorTask perform(final String target, final Project project, final boolean checkOtherScripts) {    
        if ((target.startsWith("build") || target.startsWith("sim"))
                && ClientProjectUtilities.getStartFile(project) == null) {
            DialogDisplayer.getDefault().notify(
                    new DialogDescriptor.Message(
                    Bundle.ERR_StartFileNotFound()));
            CustomizerProvider2 cust = project.getLookup().lookup(CustomizerProvider2.class);
            cust.showCustomizer("RUN", null);
            return null;
        }
        
        if (((target.startsWith("build") || target.startsWith("sim") || target.startsWith("rebuild")) // NOI18N
                && !CordovaPlatform.isCordovaProject(project))) { // NOI18N
            NotifyDescriptor desc = new NotifyDescriptor(
                    Bundle.ERR_NOT_Cordova(),
                    NbBundle.getMessage(CordovaCustomizerPanel.class, "CordovaPanel.createConfigs.text"),
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.QUESTION_MESSAGE,
                    null,
                    NotifyDescriptor.OK_OPTION);
            DialogDisplayer.getDefault().notify(desc);
            if (desc.getValue() != NotifyDescriptor.OK_OPTION) {
                return null;
            }
       }
        
        final FileObject siteRoot = ClientProjectUtilities.getSiteRoot(project);
        if (siteRoot == null || FileUtil.getRelativePath(project.getProjectDirectory(), siteRoot) == null) {
            DialogDisplayer.getDefault().notify(
                    new DialogDescriptor.Message(
                    Bundle.ERR_SiteRootNotDefined()));
            CustomizerProvider2 cust = project.getLookup().lookup(CustomizerProvider2.class);
            cust.showCustomizer("SOURCES", null);
            return null;
        }
        

        if (!CordovaPlatform.getDefault().isReady()) {
            throw new UnsupportedOperationException(Bundle.ERR_NO_Cordova());
        }
        
        ProjectBrowserProvider provider = project.getLookup().lookup(ProjectBrowserProvider.class);
        if (provider != null &&
                "ios_1".equals(provider.getActiveBrowser().getId()) && 
                (target.equals(BuildPerformer.RUN_IOS) || target.equals(BuildPerformer.BUILD_IOS)) &&
                PlatformManager.getPlatform(PlatformManager.IOS_TYPE) != null &&
                PlatformManager.getPlatform(PlatformManager.IOS_TYPE).getProvisioningProfilePath() == null
                ) {
            throw new IllegalStateException(Bundle.ERR_NO_Provisioning());
       }        

        
        final ExecutorTask runTarget[] = new ExecutorTask[1];
        Runnable run = new Runnable() {
            @Override
            public void run() {
                try {
                    FileObject siteRoot = ClientProjectUtilities.getSiteRoot(project);
                    final DataObject siteRootDOB = DataObject.find(siteRoot);
                    final boolean rename = target.startsWith("build") || target.startsWith("sim") || target.startsWith("rebuild") || target.startsWith("upgrade");
                    if (rename) {
                        if (!CordovaPlatform.isCordovaProject(project)) {
                            siteRootDOB.rename(WWW_NB_TEMP);
                        } else {
                            siteRootDOB.rename(WWW);
                        }
                    }
                    
                    if(checkOtherScripts && 
                       project.getProjectDirectory().getFileObject(PATH_BUILD_XML) == null && 
                       checkOtherBuildScripts(project)) 
                    {
                        return;
                    }
                    generateBuildScripts(project);
                    FileObject buildFo = project.getProjectDirectory().getFileObject(PATH_BUILD_XML);//NOI18N
                    
                    final Properties properties = properties(project);
                    runTarget[0] = ActionUtils.runTarget(buildFo, new String[]{target}, properties);
                    final int result = runTarget[0].result();

                    project.getProjectDirectory().refresh();
                    if (rename && !WWW.equals(siteRootDOB.getName())) {
                        FileObject www = project.getProjectDirectory().getFileObject(WWW);
                        if (www !=null) {
                            DataObject.find(www).delete();
                        }
                        siteRootDOB.rename(WWW);
                    }

                    if (target.equals(BuildPerformer.RUN_IOS) || (target.equals(BuildPerformer.RUN_ANDROID)) && isAndroidDebugSupported(project)) {
                        if (result == 0) {
                            ProjectBrowserProvider provider = project.getLookup().lookup(ProjectBrowserProvider.class);
                            if (provider != null) {
                                WebBrowser activeConfiguration = provider.getActiveBrowser();
                                MobileConfigurationImpl mobileConfig = MobileConfigurationImpl.create(project, activeConfiguration.getId());
                                Device device = mobileConfig.getDevice();
                                if (!device.isWebViewDebugSupported()) {
                                    return;
                                }
                                final FileObject startFile = ClientProjectUtilities.getStartFile(project);
                                
                                //#231037
                                URL u = ServerURLMapping.toServer(project, startFile);
                                activeConfiguration.toBrowserURL(project, startFile, u);
                                
                                BrowserURLMapperImplementation.BrowserURLMapper mapper = ((CordovaBrowserFactory) activeConfiguration.getHtmlBrowserFactory()).getMapper();
                                if (!device.isEmulator() && target.equals(BuildPerformer.RUN_IOS)) {
                                    DialogDescriptor dd = new DialogDescriptor(Bundle.LBL_InstallThroughItunes(), Bundle.CTL_InstallAndRun());
                                    if (DialogDisplayer.getDefault().notify(dd) != DialogDescriptor.OK_OPTION) {
                                        return;
                                    }
                                } else {
                                    try {
                                        Thread.sleep(target.equals(BuildPerformer.RUN_IOS)?2000:5000);
                                    } catch (InterruptedException ex) {
                                        Exceptions.printStackTrace(ex);
                                } 
                                }
                                WebKitDebuggingSupport.getDefault().startDebugging(device, 
                                        project, 
                                        Lookups.fixed(
                                            mapper, 
                                            ImageUtilities.loadImage("org/netbeans/modules/cordova/platforms/ios/ios" + (device.isEmulator()?"simulator16.png":"device16.png")), 
                                            getConfig(project).getId()), 
                                        false);
                            }
                        }
                   }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            private boolean isAndroidDebugSupported(Project project) {
                if (CordovaPlatform.getDefault().getVersion().getApiVersion().compareTo(new CordovaPlatform.Version.SubVersion("3.3.0")) >= 0) {
                    try {
                        FileObject manifestFile = project.getProjectDirectory().getFileObject("platforms/android/AndroidManifest.xml");
                        if (manifestFile == null) {
                            return false;
                        }
                        FileObject propertiesFile = project.getProjectDirectory().getFileObject("platforms/android/project.properties");
                        if (propertiesFile == null) {
                            return false;
                        }
                        
                        try (InputStream s=propertiesFile.getInputStream()) {
                            Properties props = new Properties();
                            props.load(s);
                            String target = props.getProperty("target"); //NOI18N
                            if (target == null || target.trim().compareTo("android-19") < 0) { //NOI18N
                                return false;
                            }
                        }
                        return true;
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
                return false;
            }
        };
        
        if (SwingUtilities.isEventDispatchThread()) {
            RP.post(run);
        } else {
            run.run();
        }
        return runTarget[0];
    }

    private Properties properties(Project p) {
        Properties props = new Properties();
        final CordovaPlatform cordovaPlatform = CordovaPlatform.getDefault();
//        props.put(PROP_CORDOVA_HOME, cordovaPlatform.getSdkLocation());//NOI18N
        props.put(PROP_CORDOVA_VERSION, cordovaPlatform.getVersion().toString());//NOI18N
        final FileObject siteRoot = ClientProjectUtilities.getSiteRoot(p);
        if (siteRoot != null) {
            final String siteRootRelative = FileUtil.getRelativePath(p.getProjectDirectory(), siteRoot);
                props.put(PROP_SITE_ROOT, siteRootRelative);
        } else {
            props.put(PROP_SITE_ROOT, WWW_NB_TEMP);
        }
        final FileObject startFile = ClientProjectUtilities.getStartFile(p);
        if (startFile!=null) {
            final String startFileRelative = FileUtil.getRelativePath(siteRoot, startFile);
            props.put(PROP_START_FILE, startFileRelative);
        }
        final File antTaskJar = InstalledFileLocator.getDefault().locate(
           PATH_EXTRA_ANT_JAR, 
           "org.netbeans.modules.cordova" , true); // NOI18N
        props.put(PROP_UPDATE_TASK_JAR, antTaskJar.getAbsolutePath());
        final String id = getConfig(p).getId();
        String activity = id.substring(id.lastIndexOf(".")+1, id.length()); // NOI18N
        props.put(PROP_ANDROID_PROJECT_ACTIVITY, activity);//NOI18N
        
        MobilePlatform iosPlatform = PlatformManager.getPlatform(PlatformManager.IOS_TYPE);
        if (iosPlatform != null) {
            final String provisioningProfilePath = iosPlatform.getProvisioningProfilePath();
            if (provisioningProfilePath != null) {
                props.put(PROP_PROVISIONING_PROFILE, provisioningProfilePath);
            }
            final String codeSignIdentity = iosPlatform.getCodeSignIdentity();

            if (codeSignIdentity != null) {
                props.put(PROP_CERTIFICATE_NAME, codeSignIdentity);
            }
        }

        //workaround for some strange behavior of ant execution in netbeans
        props.put(PROP_ENV_DISPLAY, ":0.0");//NOI18N
        final String sdkLocation = PlatformManager.getPlatform(PlatformManager.ANDROID_TYPE).getSdkLocation();
        if (sdkLocation!=null) {
            props.put(PROP_ANDROID_SDK_HOME, sdkLocation);
        }

        ProjectBrowserProvider provider = p.getLookup().lookup(ProjectBrowserProvider.class);
        if (provider != null && provider.getActiveBrowser().getBrowserFamily()==BrowserFamilyId.PHONEGAP) {
            WebBrowser activeConfiguration = provider.getActiveBrowser();
            MobileConfigurationImpl mobileConfig = MobileConfigurationImpl.create(p, activeConfiguration.getId());

            props.put(PROP_CONFIG, mobileConfig.getId());
            mobileConfig.getDevice().addProperties(props);
            if (mobileConfig.getId().equals("ios") && iosPlatform != null) { // NOI18N
                boolean sdkVerified = false;
                try {
                    for (SDK sdk:iosPlatform.getSDKs()) {
                        if (sdk.getIdentifier().equals(mobileConfig.getProperty("ios.build.sdk"))) {
                            sdkVerified = true;
                            break;
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                if (!sdkVerified) {
                    mobileConfig.putProperty("ios.build.sdk", iosPlatform.getPrefferedTarget().getIdentifier()); // NOI18N
                    mobileConfig.save();
                }
            }
        }

        return props;
    }

    private void generateBuildScripts(Project project) {
        try {
            CordovaProjectExtender.createMobileConfigs(project.getProjectDirectory());
            Preferences preferences = ProjectUtils.getPreferences(project, CordovaPlatform.class, true);
            int version = preferences.getInt(PROP_BUILD_SCRIPT_VERSION, 0);

            boolean fresh;
            if (version < BUILD_SCRIPT_VERSION) {
                fresh = createScript(project, NAME_BUILD_XML, PATH_BUILD_XML, true);//NOI18N
            } else {
                fresh = createScript(project, NAME_BUILD_XML, PATH_BUILD_XML, false);//NOI18N
            }
            if (fresh) {
                preferences.putInt(PROP_BUILD_SCRIPT_VERSION, BUILD_SCRIPT_VERSION);
                Map<String, String> map = new HashMap<String, String>();
                map.put("__PROJECT_NAME__", ProjectUtils.getInformation(project).getName());// NOI18N
                ConfigUtils.replaceToken(project.getProjectDirectory().getFileObject(PATH_BUILD_XML), map);
                createScript(project, "empty.properties", PATH_PLUGINS_PROPERTIES, false);
            }

            getConfig(project);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private boolean checkOtherBuildScripts(Project project) {
        return hasOtherBuildTool(project, "Gruntfile.js", "Grunt", () -> { project.getLookup().lookup(CustomizerProvider2.class).showCustomizer(GRUNT_CUSTOMIZER_IDENT, null); }) ||
               hasOtherBuildTool(project, "gulpfile.js",  "Gulp", () -> { project.getLookup().lookup(CustomizerProvider2.class).showCustomizer(GULP_CUSTOMIZER_IDENT, null); }) 
               ? true : false;            
    }
    
    private boolean hasOtherBuildTool(Project project, String toolfile, String toolName, Runnable r) {
        if(project.getProjectDirectory().getFileObject(toolfile) != null) {
            ProjectInformation info = ProjectUtils.getInformation(project);
            String name = info != null ? info.getDisplayName() : project.getProjectDirectory().getNameExt();
            JButton tool = new JButton(toolName);
            NotifyDescriptor desc = new NotifyDescriptor(
                Bundle.MSG_SelectBuildTool(name, toolfile, toolName),
                Bundle.CTL_SelectBuildTool(),
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                new Object[] {tool, new JButton("Ant"), NotifyDescriptor.CANCEL_OPTION},
                NotifyDescriptor.OK_OPTION);
            DialogDisplayer.getDefault().notify(desc);
            if(desc.getValue() == tool) {
                r.run();
                return true;
            } else if (desc.getValue() == NotifyDescriptor.CANCEL_OPTION) {
                return true;
            }
        }
        return false;
    }
    
    private static String getConfigPath(Project project) {
        final FileObject siteRoot = ClientProjectUtilities.getSiteRoot(project);
        String configPath = (siteRoot==null?WWW_NB_TEMP:siteRoot.getNameExt()) + "/" + NAME_CONFIG_XML; // NOI18N
        boolean configExists = project.getProjectDirectory().getFileObject(configPath) != null;

        if (CordovaPlatform.getDefault().getVersion().getApiVersion().compareTo(new CordovaPlatform.Version.SubVersion("3.4.0")) >= 0) {
            String newConfigPath = "/" + NAME_CONFIG_XML;
            boolean newConfigPathExists = project.getProjectDirectory().getFileObject(newConfigPath) !=null;
            if (newConfigPathExists) {
                return newConfigPath;
            } else {
                if (configExists) {
                    return configPath;
                } else {
                    return newConfigPath;
                }
            }
        } else {
            return configPath;
        }
    }
    
    public static SourceConfig getConfig(Project project)  {
        try {
            String configPath = getConfigPath(project);
            boolean fresh = createScript(project, NAME_CONFIG_XML, configPath, false);//NOI18N

            FileObject config = project.getProjectDirectory().getFileObject(configPath);
            SourceConfig conf = new SourceConfig(FileUtil.toFile(config));
            if (fresh) {
                final String appName = ProjectUtils.getInformation(project).getDisplayName().replaceAll(" ", "_").replaceAll("-", "_").replaceAll("\\.","_"); // NOI18N
                conf.setId(DEFAULT_ID_PREFIX + "." + appName); // NOI18N
                conf.setName(appName);
                conf.setDescription(DEFAULT_DESCRIPTION);
                conf.setAuthor(System.getProperty("user.name"));
                conf.setAuthorEmail(DEFAULT_EMAIL);
                conf.setAuthorHref(DEFAULT_WWW);
                conf.setVersion(DEFAULT_VERSION);
                conf.save();
            }
            return conf;
        } catch (IOException iOException) {
            throw new IllegalStateException(iOException);
        }
    }

    /**
     * 
     * @param project
     * @param source
     * @param target
     * @param overwrite
     * @return true if script was created. False if script was already there
     * @throws IOException 
     */
    public static boolean createScript(Project project, String source, String target, boolean overwrite) throws IOException {        
        FileObject build = null;
        if (!overwrite) {
            build = project.getProjectDirectory().getFileObject(target);
        }
        if (build == null) {
            build = FileUtil.createData(project.getProjectDirectory(), target);
            InputStream resourceAsStream = CordovaPerformer.class.getResourceAsStream(source);
            OutputStream outputStream = build.getOutputStream();
            try {
                FileUtil.copy(resourceAsStream, outputStream);
            } finally {
                outputStream.close();
                resourceAsStream.close();
            }
            return true;
        }
        return false;
    }
    
    private class CompoundTask extends Task {

        private final Task task1;
        private final Task task2;

        public CompoundTask(Task task1, Task task2) {
            this.task1 = task1;
            this.task2 = task2;
        }
        @Override
        public void waitFinished() {
            if (task1!=null) {
                task1.waitFinished();
            }
            if (task2!=null) {
                task2.waitFinished();
            }
        }
    }

}
