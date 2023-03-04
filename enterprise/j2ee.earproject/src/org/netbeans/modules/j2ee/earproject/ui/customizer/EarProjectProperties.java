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

package org.netbeans.modules.j2ee.earproject.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.javaee.project.api.ant.DeployOnSaveUtils;
import org.netbeans.modules.javaee.project.api.ui.utils.J2eePlatformUiSupport;
import org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.dd.api.application.Web;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.modules.j2ee.earproject.EarProject;
import org.netbeans.modules.j2ee.earproject.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.j2ee.earproject.ui.customizer.CustomizerRun.ApplicationUrisComboBoxModel;
import org.netbeans.modules.j2ee.earproject.util.EarProjectUtil;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.customizer.ClassPathListCellRenderer;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Helper class. Defines constants for properties. Knows the proper
 * place where to store the properties.
 *
 * @author Petr Hrebejk
 */
public final class EarProjectProperties {
    
    // Special properties of the project
    public static final String WEB_PROJECT_NAME = "web.project.name"; //NOI18N
    public static final String JAVA_PLATFORM = "platform.active"; //NOI18N
    public static final String J2EE_PLATFORM = "j2ee.platform"; //NOI18N
    public static final String J2EE_PLATFORM_CLASSPATH = "j2ee.platform.classpath"; //NOI18N
    
    // Properties stored in the PROJECT.PROPERTIES    
    /** root of external web module sources (full path), ".." if the sources are within project folder */
    public static final String SOURCE_ROOT = "source.root"; //NOI18N
    public static final String BUILD_FILE = "buildfile"; //NOI18N
    public static final String LIBRARIES_DIR = "lib.dir"; //NOI18N
    public static final String DIST_DIR = "dist.dir"; //NOI18N
    public static final String DIST_JAR = "dist.jar"; //NOI18N
    public static final String JAVAC_CLASSPATH = "javac.classpath"; //NOI18N
    public static final String DEBUG_CLASSPATH = "debug.classpath";     //NOI18N
    public static final String RUN_CLASSPATH = "run.classpath"; // NOI18N
    public static final String JAR_NAME = "jar.name"; //NOI18N
    public static final String JAR_COMPRESS = "jar.compress"; //NOI18N
    public static final String JAR_CONTENT_ADDITIONAL = "jar.content.additional"; //NOI18N
    
    public static final String APPLICATION_CLIENT = "app.client"; // NOI18N
    public static final String APPCLIENT_MAIN_CLASS = "main.class"; // NOI18N
    public static final String APPCLIENT_ARGS = "application.args"; // NOI18N
    public static final String APPCLIENT_JVM_OPTIONS = "j2ee.appclient.jvmoptions"; // NOI18N
    public static final String APPCLIENT_MAINCLASS_ARGS = "j2ee.appclient.mainclass.args"; // NOI18N
    
    public static final String LAUNCH_URL_RELATIVE = "client.urlPart"; //NOI18N
    public static final String DISPLAY_BROWSER = "display.browser"; //NOI18N
    public static final String J2EE_DEPLOY_ON_SAVE = "j2ee.deploy.on.save"; //NOI18N
    public static final String J2EE_COMPILE_ON_SAVE = "j2ee.compile.on.save"; //NOI18N
    public static final String CLIENT_MODULE_URI = "client.module.uri"; //NOI18N
    public static final String J2EE_SERVER_INSTANCE = J2EEProjectProperties.J2EE_SERVER_INSTANCE;
    public static final String J2EE_SERVER_TYPE = J2EEProjectProperties.J2EE_SERVER_TYPE;
    public static final String JAVAC_SOURCE = "javac.source"; //NOI18N
    public static final String JAVAC_DEBUG = "javac.debug"; //NOI18N
    public static final String JAVAC_DEPRECATION = "javac.deprecation"; //NOI18N
    public static final String JAVAC_TARGET = "javac.target"; //NOI18N
    public static final String META_INF = "meta.inf"; //NOI18N
    public static final String RESOURCE_DIR = "resource.dir"; //NOI18N
    public static final String WEB_DOCBASE_DIR = "web.docbase.dir"; //NOI18N
    public static final String BUILD_DIR = "build.dir"; //NOI18N
    public static final String BUILD_GENERATED_DIR = "build.generated.dir"; //NOI18N
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"; //NOI18N
    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir"; //NOI18N
    public static final String NO_DEPENDENCIES="no.dependencies"; //NOI18N
    
    public static final String JAVADOC_PRIVATE="javadoc.private"; //NOI18N
    public static final String JAVADOC_NO_TREE="javadoc.notree"; //NOI18N
    public static final String JAVADOC_USE="javadoc.use"; //NOI18N
    public static final String JAVADOC_NO_NAVBAR="javadoc.nonavbar"; //NOI18N
    public static final String JAVADOC_NO_INDEX="javadoc.noindex"; //NOI18N
    public static final String JAVADOC_SPLIT_INDEX="javadoc.splitindex"; //NOI18N
    public static final String JAVADOC_AUTHOR="javadoc.author"; //NOI18N
    public static final String JAVADOC_VERSION="javadoc.version"; //NOI18N
    public static final String JAVADOC_WINDOW_TITLE="javadoc.windowtitle"; //NOI18N
    public static final String JAVADOC_ENCODING="javadoc.encoding"; //NOI18N
    
    public static final String JAVADOC_PREVIEW="javadoc.preview"; //NOI18N
    
    public static final String COMPILE_JSPS = "compile.jsps"; //NOI18N
    
    public static final String CLIENT_NAME = "j2ee.clientName"; // NOI18N
    
    // Properties stored in the PRIVATE.PROPERTIES
    
    public static final String APPCLIENT_TOOL_RUNTIME = "j2ee.appclient.tool.runtime"; // NOI18N
    public static final String APPCLIENT_TOOL_MAINCLASS = "j2ee.appclient.tool.mainclass"; // NOI18N
    public static final String APPCLIENT_TOOL_JVMOPTS = "j2ee.appclient.tool.jvmoptions";  // NOI18N
    public static final String APPCLIENT_TOOL_ARGS = "j2ee.appclient.tool.args"; // NOI18N
    
    /**
     * "API" contract between Application Client and Glassfish plugin's
     * J2eePlatformImpl implementation.
     */
    private static final String J2EE_PLATFORM_APPCLIENT_ARGS = "j2ee.appclient.args"; // NOI18N
    
    static final String APPCLIENT_WA_COPY_CLIENT_JAR_FROM = "wa.copy.client.jar.from"; // NOI18N
    
    public static final String TAG_WEB_MODULE_LIBRARIES = "web-module-libraries"; // NOI18N
    public static final String TAG_WEB_MODULE__ADDITIONAL_LIBRARIES = "web-module-additional-libraries"; //NOI18N
    
    public static final String DEPLOY_ANT_PROPS_FILE = "deploy.ant.properties.file"; // NOI18N
    
    public static final String ANT_DEPLOY_BUILD_SCRIPT = "nbproject/ant-deploy.xml"; // NOI18N
    public static final String SELECTED_BROWSER = "selected.browser"; //NOI18N
    
    private static final Logger LOGGER = Logger.getLogger(EarProjectProperties.class.getName());
    
    // CustomizerLibraries
    Document SHARED_LIBRARIES_MODEL;
    DefaultListModel DEBUG_CLASSPATH_MODEL;
    DefaultListModel ENDORSED_CLASSPATH_MODEL;
    ListCellRenderer CLASS_PATH_LIST_RENDERER;
    
    // CustomizerJarContent
    Document ARCHIVE_NAME_MODEL;
    ButtonModel ARCHIVE_COMPRESS_MODEL;
    Document BUILD_CLASSES_EXCLUDES_MODEL;
    AdditionalContentTableModel EAR_CONTENT_ADDITIONAL_MODEL;
    TableCellRenderer CLASS_PATH_TABLE_RENDERER;
    
    // CustomizerRun
    ApplicationUrisComboBoxModel CLIENT_MODULE_MODEL; 
    ComboBoxModel J2EE_SERVER_INSTANCE_MODEL; 
    Document J2EE_PLATFORM_MODEL;
    ButtonModel DISPLAY_BROWSER_MODEL; 
    Document LAUNCH_URL_RELATIVE_MODEL;
    Document MAIN_CLASS_MODEL;
    Document ARUGMENTS_MODEL;
    Document VM_OPTIONS_MODEL;
    Document APPLICATION_CLIENT_MODEL;
    JToggleButton.ToggleButtonModel DEPLOY_ON_SAVE_MODEL;
    JToggleButton.ToggleButtonModel COMPILE_ON_SAVE_MODEL;
    BrowserUISupport.BrowserComboBoxModel BROWSERS_MODEL;
    
    // Private fields ----------------------------------------------------------
    
    private StoreGroup privateGroup; 
    private StoreGroup projectGroup;
    
    private final AntProjectHelper antProjectHelper;
    private final ReferenceHelper refHelper;
    private final UpdateHelper updateHelper;
    private final EarProject project;
    private final GeneratedFilesHelper genFilesHelper;
    private PropertyEvaluator evaluator;
    public ClassPathSupport cs;
    
    /** Utility field used by bound properties. */
    private final PropertyChangeSupport propertyChangeSupport =  new PropertyChangeSupport(this);

    EarProjectProperties(EarProject project, UpdateHelper updateHelper, 
            PropertyEvaluator evaluator, ReferenceHelper refHelper) {
        this.project = project;
        this.updateHelper = project.getUpdateHelper();
        this.antProjectHelper = updateHelper.getAntProjectHelper();
        this.refHelper = refHelper;
        this.genFilesHelper = project.getGeneratedFilesHelper();
        this.evaluator = evaluator;
        privateGroup = new StoreGroup();
        projectGroup = new StoreGroup();
        cs = project.getClassPathSupport();
        init();
    }

    private void init() {
        
        // CustomizerLibraries
        SHARED_LIBRARIES_MODEL = new PlainDocument(); 
        try {
            SHARED_LIBRARIES_MODEL.insertString(0, project.getAntProjectHelper().getLibrariesLocation(), null);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );                
        EditableProperties privateProperties = updateHelper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
        DEBUG_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator(projectProperties.get( ProjectProperties.RUN_CLASSPATH ), null ) );
        ENDORSED_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator(projectProperties.get( ProjectProperties.ENDORSED_CLASSPATH ), null ) );
        CLASS_PATH_LIST_RENDERER = ClassPathListCellRenderer.createClassPathListRenderer(evaluator, project.getProjectDirectory());

        // CustomizerJarContent
        ARCHIVE_COMPRESS_MODEL = projectGroup.createToggleButtonModel( evaluator, JAR_COMPRESS );
        ARCHIVE_NAME_MODEL = projectGroup.createStringDocument( evaluator, JAR_NAME );
        BUILD_CLASSES_EXCLUDES_MODEL = projectGroup.createStringDocument( evaluator, BUILD_CLASSES_EXCLUDES );
        EAR_CONTENT_ADDITIONAL_MODEL = AdditionalContentTableModel.createTableModel( cs.itemsIterator(projectProperties.get( JAR_CONTENT_ADDITIONAL ), TAG_WEB_MODULE__ADDITIONAL_LIBRARIES) );
        EAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel().addListDataListener(new ListDataListener() {
            public void intervalAdded(ListDataEvent e) {
                CLIENT_MODULE_MODEL.refresh(ClassPathUiSupport.getList( EAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel()));
            }

            public void intervalRemoved(ListDataEvent e) {
                CLIENT_MODULE_MODEL.refresh(ClassPathUiSupport.getList( EAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel()));
            }

            public void contentsChanged(ListDataEvent e) {
                CLIENT_MODULE_MODEL.refresh(ClassPathUiSupport.getList( EAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel()));
            }
        });
        CLASS_PATH_TABLE_RENDERER = ClassPathListCellRenderer.createClassPathTableRenderer(evaluator, project.getProjectDirectory());

        // CustomizerRun
        J2EE_PLATFORM_MODEL = projectGroup.createStringDocument(evaluator, J2EE_PLATFORM);
        LAUNCH_URL_RELATIVE_MODEL = projectGroup.createStringDocument(evaluator, LAUNCH_URL_RELATIVE);
        DISPLAY_BROWSER_MODEL = projectGroup.createToggleButtonModel(evaluator, DISPLAY_BROWSER);
        DEPLOY_ON_SAVE_MODEL = projectGroup.createToggleButtonModel(evaluator, J2EE_DEPLOY_ON_SAVE);
        COMPILE_ON_SAVE_MODEL = projectGroup.createToggleButtonModel(evaluator, J2EE_COMPILE_ON_SAVE);
        COMPILE_ON_SAVE_MODEL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!COMPILE_ON_SAVE_MODEL.isSelected()) {
                    DEPLOY_ON_SAVE_MODEL.setSelected(false);
                }
            }
        });
        DEPLOY_ON_SAVE_MODEL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (DEPLOY_ON_SAVE_MODEL.isSelected()) {
                    COMPILE_ON_SAVE_MODEL.setSelected(true);
                }
            }
        });
        
        J2EE_SERVER_INSTANCE_MODEL = J2eePlatformUiSupport.createPlatformComboBoxModel(
                privateProperties.getProperty( J2EE_SERVER_INSTANCE ),
                Profile.fromPropertiesString(projectProperties.getProperty(J2EE_PLATFORM)),
                J2eeModule.Type.EAR);
        MAIN_CLASS_MODEL = projectGroup.createStringDocument(evaluator, APPCLIENT_MAIN_CLASS);
        ARUGMENTS_MODEL = projectGroup.createStringDocument(evaluator, APPCLIENT_ARGS);
        VM_OPTIONS_MODEL = projectGroup.createStringDocument(evaluator, APPCLIENT_JVM_OPTIONS);
        APPLICATION_CLIENT_MODEL = projectGroup.createStringDocument(evaluator, APPLICATION_CLIENT);
        CLIENT_MODULE_MODEL = CustomizerRun.createApplicationUrisComboBoxModel(project);

        String selectedBrowser = evaluator.getProperty(SELECTED_BROWSER);
        BROWSERS_MODEL = BrowserUISupport.createBrowserModel(selectedBrowser, true);
    }

    private void saveLibrariesLocation() throws IOException, IllegalArgumentException {
        try {
            String str = SHARED_LIBRARIES_MODEL.getText(0, SHARED_LIBRARIES_MODEL.getLength()).trim();
            if (str.length() == 0) {
                str = null;
            }
            String old = project.getAntProjectHelper().getLibrariesLocation();
            if ((old == null && str == null) || (old != null && old.equals(str))) {
                //ignore, nothing changed..
            } else {
                project.getAntProjectHelper().setLibrariesLocation(str);
                ProjectManager.getDefault().saveProject(project);
            }
        } catch (BadLocationException x) {
            Exceptions.printStackTrace(x);
        }
    }
    
    private void storeProperties() throws IOException {
        // Store special properties
        
        // Modify the project dependencies properly        
        resolveProjectDependencies();
       
        // Encode all paths (this may change the project properties)
        String[] debug_cp = cs.encodeToStrings(ClassPathUiSupport.getList(DEBUG_CLASSPATH_MODEL), null );
        String[] additional_content = cs.encodeToStrings(ClassPathUiSupport.getList( EAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel()), TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
        String[] endorsed_cp = cs.encodeToStrings(ClassPathUiSupport.getList(ENDORSED_CLASSPATH_MODEL), null );

        // Store standard properties
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        EditableProperties privateProperties = updateHelper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
        
        // Standard store of the properties
        projectGroup.store( projectProperties );        
        privateGroup.store( privateProperties );

        // Save all paths
        projectProperties.setProperty( ProjectProperties.RUN_CLASSPATH, debug_cp );
        projectProperties.setProperty( JAR_CONTENT_ADDITIONAL, additional_content );
        projectProperties.setProperty( ProjectProperties.ENDORSED_CLASSPATH, endorsed_cp );
        
        // Set new server instance ID
        if (J2EE_SERVER_INSTANCE_MODEL.getSelectedItem() != null) {
            Profile profile = Profile.fromPropertiesString(project.evaluator().getProperty(J2EE_PLATFORM));
            String serverInstance = J2eePlatformUiSupport.getServerInstanceID(J2EE_SERVER_INSTANCE_MODEL.getSelectedItem());
            J2EEProjectProperties.updateServerProperties(projectProperties, privateProperties,
                    serverInstance,
                    null, null, new CallbackImpl(project), project,
                    profile, J2eeModule.Type.EAR);
            updateEarServerProperties(serverInstance, projectProperties, privateProperties);
        }
        
        CLIENT_MODULE_MODEL.storeSelectedItem(projectProperties);
        privateProperties.setProperty(SELECTED_BROWSER, BROWSERS_MODEL.getSelectedBrowserId());
        
        // Store the property changes into the project
        updateHelper.putProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties );
        updateHelper.putProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties );
        
        // compile on save listeners
        if (COMPILE_ON_SAVE_MODEL.isEnabled() && COMPILE_ON_SAVE_MODEL.isSelected()) {
            LOGGER.log(Level.FINE, "Starting listening on cos for {0}", project.getAppModule());
            Deployment.getDefault().enableCompileOnSaveSupport(project.getAppModule());
        } else {
            LOGGER.log(Level.FINE, "Stopping listening on cos for {0}", project.getAppModule());
            Deployment.getDefault().disableCompileOnSaveSupport(project.getAppModule());
        }        
    }

    public static void setServerInstance(final EarProject project, final UpdateHelper helper, final String serverInstanceID) {
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    Profile profile = Profile.fromPropertiesString(project.evaluator().getProperty(J2EE_PLATFORM));
                    J2EEProjectProperties.updateServerProperties(projectProps, privateProps, serverInstanceID,
                            null, null, new CallbackImpl(project), project,
                            profile, J2eeModule.Type.EAR);
                    updateEarServerProperties(serverInstanceID, projectProps, privateProps);
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                    ProjectManager.getDefault().saveProject(project);

                    setupDeploymentDescriptor(project);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });
    }
    
    
    private static void updateEarServerProperties(String newServInstID,
            EditableProperties projectProps, EditableProperties privateProps) {

        assert newServInstID != null : "Server isntance id to set can't be null"; // NOI18N

        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(newServInstID);
        if (j2eePlatform == null) {
            // remove J2eePlatform.TOOL_APP_CLIENT_RUNTIME classpath
            privateProps.remove(APPCLIENT_TOOL_RUNTIME);
            return;
        }
        Map<String, String> roots = J2EEProjectProperties.extractPlatformLibrariesRoot(j2eePlatform);
        // update j2ee.appclient.tool.runtime
        if (j2eePlatform.isToolSupported(J2eePlatform.TOOL_APP_CLIENT_RUNTIME)) {
            File[] wsClasspath = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_APP_CLIENT_RUNTIME);
            privateProps.setProperty(APPCLIENT_TOOL_RUNTIME, J2EEProjectProperties.toClasspathString(wsClasspath, roots));
        } else {
            privateProps.remove(APPCLIENT_TOOL_RUNTIME);
        }
        String mainClassArgs = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_MAIN_CLASS_ARGS);
        if (mainClassArgs != null && !mainClassArgs.equals("")) {
            projectProps.setProperty(APPCLIENT_MAINCLASS_ARGS, mainClassArgs);
            projectProps.remove(CLIENT_NAME);
        } else if ((mainClassArgs = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, CLIENT_NAME)) != null) {
            projectProps.setProperty(CLIENT_NAME, mainClassArgs);
            projectProps.remove(APPCLIENT_MAINCLASS_ARGS);
        } else {
            projectProps.remove(APPCLIENT_MAINCLASS_ARGS);
            projectProps.remove(CLIENT_NAME);
        }
        setAppClientPrivateProperties(j2eePlatform, newServInstID, privateProps);
    }

    /** <strong>Package private for unit test only</strong>. */
    static void updateContentDependency(EarProject project, List<ClassPathSupport.Item> oldContent, List<ClassPathSupport.Item> newContent,
            EditableProperties props) {
        Application app = project.getAppModule().getApplication();
        if (app == null) {
            return;
        }
        
        Set<ClassPathSupport.Item> deleted = new HashSet<ClassPathSupport.Item>(oldContent);
        deleted.removeAll(newContent);
        Set<ClassPathSupport.Item> added = new HashSet<ClassPathSupport.Item>(newContent);
        added.removeAll(oldContent);
        Set<ClassPathSupport.Item> needsUpdate = new HashSet<ClassPathSupport.Item>(newContent);
        needsUpdate.removeAll(added);
        
        boolean saveNeeded = false;
        // delete the old entries out of the application
        for (ClassPathSupport.Item item : deleted) {
            removeItemFromAppDD(project, app, item);
            saveNeeded = true;
        }
        // add the new stuff "back"
        for (ClassPathSupport.Item item : added) {
            addItemToAppDD(project, app,item);
            saveNeeded = true;
        }
        for (ClassPathSupport.Item item : needsUpdate) {
            ClassPathSupport.Item old = oldContent.get(oldContent.indexOf(item));
            boolean changed = old.getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT) == null ? 
                item.getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT) != null : 
                !old.getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT).equals(
                item.getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT));
            if (changed) {
                // #76008 - PATH_IN_DEPLOYMENT have changed; remove old one and save new one:
                removeItemFromAppDD(project, app, old);
                addItemToAppDD(project, app, item);
                saveNeeded = true;
            }
        }
        
        if (saveNeeded && EarProjectUtil.isDDWritable(project)) {
                try {
                    app.write(project.getAppModule().getDeploymentDescriptor());
                } catch (IOException ioe) {
                    Logger.getLogger("global").log(Level.INFO, ioe.getLocalizedMessage());
                }
        }
    }
    
    private static void removeItemFromAppDD(EarProject project, Application dd, ClassPathSupport.Item item) {
        String pathInEAR = getCompletePathInArchive(project, item);
        Module m = searchForModule(dd, pathInEAR);
        if (null != m) {
            dd.removeModule(m);
            if (item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT && !item.isBroken()) {
                AntArtifact aa = item.getArtifact();
                Project p = aa.getProject();
                J2eeModuleProvider jmp = p.getLookup().lookup(J2eeModuleProvider.class);
                if (null != jmp) {
                    J2eeModule jm = jmp.getJ2eeModule();
                    if (null != jm) {
                        project.getAppModule().removeModuleProvider(jmp, pathInEAR);
                    }
                }
            }
        }
    }
    
    private static Module searchForModule(Application dd, String path) {
        Module mods[] = dd.getModule();
        int len = 0;
        if (null != mods) {
            len = mods.length;
        }
        for (int i = 0; i < len; i++) {
            String val = mods[i].getEjb();
            if (null != val && val.equals(path)) {
                return mods[i];
            }
            val = mods[i].getConnector();
            if (null != val && val.equals(path)) {
                return mods[i];
            }
            val = mods[i].getJava();
            if (null != val && val.equals(path)) {
                return mods[i];
            }
            Web w = mods[i].getWeb();
            val = null;
            if ( null != w) {
                val = w.getWebUri();
            }
            if (null != val && val.equals(path)) {
                return mods[i];
            }
        }
        return null;
    }
    
    public static void addItemToAppDD(EarProject project, Application dd, ClassPathSupport.Item item) {
        if (item.isBroken()) {
            return;
        }
        String path = getCompletePathInArchive(project, item);
        Module mod = null;
        if (item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT) {
            mod = getModFromAntArtifact(project, item.getArtifact(), dd, path);
        } else if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
           mod = getModFromFile(item.getResolvedFile(), dd, path);
        }
        Module prevMod = searchForModule(dd, path);
        if (null == prevMod && null != mod) {
            dd.addModule(mod);
        }
    }
    
    
    private static Module getModFromAntArtifact(EarProject project, AntArtifact aa, Application dd, String path) {
        Project p = aa.getProject();
        Module mod = null;
        try {
            J2eeModuleProvider jmp = p.getLookup().lookup(J2eeModuleProvider.class);
            if (null != jmp) {
                String serverInstanceId = project.getServerInstanceID();
                if (serverInstanceId != null) {
                    jmp.setServerInstanceID(serverInstanceId);
                }
                J2eeModule jm = jmp.getJ2eeModule();
                if (null != jm) {
                    project.getAppModule().addModuleProvider(jmp,path);
                } else {
                    return null;
                }
                mod = (Module) dd.createBean(Application.MODULE);
                if (J2eeModule.Type.EJB.equals(jm.getType())) {
                    mod.setEjb(path); // NOI18N
                } else if (J2eeModule.Type.WAR.equals(jm.getType())) {
                    Web w = mod.newWeb(); // createBean("Web");
                    w.setWebUri(path);
                    FileObject tmp = aa.getScriptFile();
                    if (null != tmp) {
                        tmp = tmp.getParent().getFileObject("web/WEB-INF/web.xml"); // NOI18N
                    }
                    WebModule wm = null;
                    if (null != tmp) {
                        wm = WebModule.getWebModule(tmp);
                    }
                    String contextPath = null;
                    if (null != wm) {
                        contextPath = wm.getContextPath();
                    } 
                    if (contextPath == null) {
                        int endex = path.length() - 4;
                        if (endex < 1) {
                            endex = path.length();
                        }
                        contextPath = "/" + path.substring(0, endex); // NOI18N
                    }
                    w.setContextRoot(contextPath);
                    mod.setWeb(w);
                } else if (J2eeModule.Type.RAR.equals(jm.getType())) {
                    mod.setConnector(path);
                } else if (J2eeModule.Type.CAR.equals(jm.getType())) {
                    mod.setJava(path);
                }
            }
        }
        catch (ClassNotFoundException cnfe) {
            Exceptions.printStackTrace(cnfe);
        }
        return mod;
    }
    
    private static Module getModFromFile(File f, Application dd, String path) {
            JarFile jar = null;
            Module mod = null;
            try {
                jar= new JarFile(f);
                JarEntry ddf = jar.getJarEntry("META-INF/ejb-jar.xml"); // NOI18N
                if (null != ddf) {
                    mod = (Module) dd.createBean(Application.MODULE);
                    mod.setEjb(path);
                }
                ddf = jar.getJarEntry("META-INF/ra.xml"); // NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean(Application.MODULE);
                    mod.setConnector(path);                    
                } else if (null != ddf && null != mod) {
                    return null; // two timing jar file.
                }
                ddf = jar.getJarEntry("META-INF/application-client.xml"); //NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean(Application.MODULE);
                    mod.setJava(path);                    
                } else if (null != ddf && null != mod) {
                    return null; // two timing jar file.
                }
                ddf = jar.getJarEntry("WEB-INF/web.xml"); //NOI18N
                if (null != ddf && null == mod) {
                    mod = (Module) dd.createBean(Application.MODULE);
                    Web w = mod.newWeb(); 
                    w.setWebUri(path);
                        int endex = path.length() - 4;
                        if (endex < 1) {
                            endex = path.length();
                        }
                        w.setContextRoot("/"+path.substring(0,endex)); // NOI18N
                    mod.setWeb(w);
                } else if (null != ddf && null != mod) {
                    return null; // two timing jar file.
                }
                ddf = jar.getJarEntry("META-INF/application.xml"); //NOI18N
                if (null != ddf) {
                    return null;
                }
            } catch (ClassNotFoundException cnfe) {
                Logger.getLogger("global").log(Level.INFO, cnfe.getLocalizedMessage());
            } catch (IOException ioe) {
                Logger.getLogger("global").log(Level.INFO, ioe.getLocalizedMessage());
            } finally {
                try {
                    if (null != jar) {
                        jar.close();
                    }
                } catch (IOException ioe) {
                    // there is little that we can do about this.
                }
            }
            return mod;
        }
    
    public static List<ClassPathSupport.Item> getJarContentAdditional(final EarProject project) {
        EditableProperties ep = project.getAntProjectHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        return project.getClassPathSupport().itemsList(
                ep.get( JAR_CONTENT_ADDITIONAL ), TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
    }
    
    /**
     * Acquires modules form the earproject's metadata (properties files).
     */
    public static Map<String, J2eeModuleProvider> getModuleMap(EarProject project) {
        Map<String, J2eeModuleProvider> mods = new HashMap<String, J2eeModuleProvider>();
        for (ClassPathSupport.Item item : getJarContentAdditional(project)) {
            Project p;
            if (item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT && !item.isBroken()) {
                AntArtifact aa = item.getArtifact();
                p = aa.getProject();
            } else {
                continue;
            }
            J2eeModuleProvider jmp = p.getLookup().lookup(J2eeModuleProvider.class);
            if (null != jmp) {
                J2eeModule jm = jmp.getJ2eeModule();
                if (null != jm) {
                    String path = getCompletePathInArchive(project, item);
                    mods.put(path, jmp);
                }
            }
        }
        return mods; // project.getAppModule().setModules(mods);
    }


    public static void addJ2eeSubprojects(final EarProject project, final Project[] moduleProjects) {
        addRemoveJ2eeSubprojects(project, moduleProjects, true);
    }
    
    public static void removeJ2eeSubprojects(final EarProject project, final Project[] moduleProjects) {
        addRemoveJ2eeSubprojects(project, moduleProjects, false);
    }
    
    private static void addRemoveJ2eeSubprojects(final EarProject project, final Project[] moduleProjects, final boolean add) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                try {
                    EditableProperties ep = project.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    List<ClassPathSupport.Item> oldContent = project.getClassPathSupport().itemsList(
                            ep.get( JAR_CONTENT_ADDITIONAL ), TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                    List<ClassPathSupport.Item> l = new ArrayList<ClassPathSupport.Item>(oldContent);
                    List<String> referencesToBeDestroyed = new ArrayList<String>();
                    for (int i = 0; i < moduleProjects.length; i++) {
                        AntArtifact artifacts[] = AntArtifactQuery.findArtifactsByType(
                                moduleProjects[i],
                                EjbProjectConstants.ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE); //the artifact type is the some for both ejb and war projects
                        for (AntArtifact artifact : artifacts) {
                            ClassPathSupport.Item item = ClassPathSupport.Item.create(artifact, artifact.getArtifactLocations()[0], null);
                            item.setAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT, "/"); // NOI18N
                            if (add) {
                                l.add(item);
                            } else {
                                if (l.indexOf(item) != -1) {
                                    ClassPathSupport.Item existingItem = l.get(l.indexOf(item));
                                    l.remove(existingItem);
                                    if (isLastReference(CommonProjectUtils.getAntPropertyName(existingItem.getReference()), ep, JAR_CONTENT_ADDITIONAL)) {
                                        referencesToBeDestroyed.add(existingItem.getReference());
                                    }
                                }
                            }
                        }
                    }
                    String[] newValue = project.getClassPathSupport().encodeToStrings(l, TAG_WEB_MODULE__ADDITIONAL_LIBRARIES);
                    ep = project.getUpdateHelper().getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    ep.setProperty(JAR_CONTENT_ADDITIONAL, newValue);
                    updateContentDependency(project, oldContent, l, ep);
                    // put properties here so that updateClientModule can read them from project:
                    project.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                    updateClientModule(project, ep);
                    project.getUpdateHelper().putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                    for (String ref : referencesToBeDestroyed) {
                        project.getReferenceHelper().destroyReference(ref);
                    }
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });
    }

    /**
     * Check whether given property is referenced by other properties.
     * 
     * @param property property which presence it going to be tested
     * @param props properties
     * @param ignoreProperty a property to ignore
     */
    private static boolean isLastReference(String property, EditableProperties props, String ignoreProperty) {
        for (Map.Entry<String,String> entry : props.entrySet()) {
            if (ignoreProperty.equals(entry.getKey())) {
                continue;
            }
            if (entry.getValue().contains(property)) {
                return false;
            }
        }
        return true;
    }

    private static void updateClientModule(EarProject project, EditableProperties ep) {
        // using model here just to update currently selected client module / app client
        ApplicationUrisComboBoxModel m = new ApplicationUrisComboBoxModel(project);
        m.storeSelectedItem(ep);
    }
    /**
     * @see #getApplicationSubprojects(Object)
     */
    static List<Project> getApplicationSubprojects(EarProject p) {
        return getApplicationSubprojects(getJarContentAdditional(p), null);
    }

    /**
     * Acquires modules (in the form of projects) from "JAVA EE Modules" not from the deployment descriptor (application.xml).
     * <p>
     * The reason is that for JAVA EE 5 the deployment descriptor is not compulsory.
     * @param moduleType the type of module, see {@link J2eeModule.Type J2eeModule constants}.
     *                   If it is <code>null</code> then all modules are returned.
     * @return list of EAR project subprojects.
     */
    static List<Project> getApplicationSubprojects(List<ClassPathSupport.Item> items, J2eeModule.Type moduleType) {
        List<Project> projects = new ArrayList<Project>(items.size());
        for (ClassPathSupport.Item item : items) {
            if (item.getType() != ClassPathSupport.Item.TYPE_ARTIFACT || item.getArtifact() == null) {
                continue;
            }
            Project vcpiProject = item.getArtifact().getProject();
            J2eeModuleProvider jmp = vcpiProject.getLookup().lookup(J2eeModuleProvider.class);
            if (jmp == null) {
                continue;
            }
            if (moduleType == null) {
                projects.add(vcpiProject);
            } else if (moduleType.equals(jmp.getJ2eeModule().getType())) {
                projects.add(vcpiProject);
            }
        }
        return projects;
    }
    
    public static List getSortedSubprojectsList(EarProject project) {
        List<Project> subprojects = new ArrayList<Project>();
        addSubprojects( project, subprojects ); // Find the projects recursively
        String[] displayNames = new String[subprojects.size()];
         
        // Replace projects in the list with formated names
        for ( int i = 0; i < subprojects.size(); i++ ) {
            displayNames[i] = ProjectUtils.getInformation(subprojects.get(i)).getDisplayName();
        }

        Arrays.sort(displayNames, Collator.getInstance());
        return Arrays.asList(displayNames);
    }
    
    /** Gets all subprojects recursively
     */
    private static void addSubprojects( Project project, List<Project> result ) {
        SubprojectProvider spp = project.getLookup().lookup( SubprojectProvider.class );
        
        if ( spp == null ) {
            return;
        }
        
        for( Iterator<? extends Project> it = spp.getSubprojects().iterator(); it.hasNext(); ) {
            Project sp = (Project) it.next();
            if (ProjectUtils.hasSubprojectCycles(project, sp)) {
                Logger.getLogger("global").log(Level.WARNING, "There would be cyclic " + // NOI18N
                        "dependencies if the " + sp + " would be added. Skipping..."); // NOI18N
                continue;
            }
            if ( !result.contains( sp ) ) {
                result.add( sp );
            }
            addSubprojects( sp, result );            
        }
    }

    /**
     * Transforms all the Objects from GUI controls into String Ant properties
     * and stores them in the project.
     */
    public void store() {
        try {
            // Store properties
            Boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                public Boolean run() throws IOException {
                    saveLibrariesLocation();
                    storeProperties();
                    //Delete COS mark
                    if (!COMPILE_ON_SAVE_MODEL.isSelected()) {
                        DeployOnSaveUtils.performCleanup(project, evaluator, updateHelper, null, true); // NOI18N
                    }
                    setupDeploymentDescriptor(project);
                    return true;
                }
            });
            // and save the project
            if (result) {
                ProjectManager.getDefault().saveProject(project);
            }
        } catch (MutexException e) {
            Exceptions.printStackTrace((IOException) e.getException());
        } catch ( IOException ex ) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void setupDeploymentDescriptor(EarProject project) {
        if (project.getAppModule().getConfigSupport().isDescriptorRequired()) {
            project.getAppModule().getMetadataModel();
        }
    }

    private static void setAppClientPrivateProperties(final J2eePlatform j2eePlatform,
            final String serverInstanceID, final EditableProperties ep) {
        // XXX rather hotfix for #75518. Get rid of it with fixing or #75574
        if (!j2eePlatform.getSupportedTypes().contains(J2eeModule.Type.CAR)) {
            return;
        }
        String mainClass = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_MAIN_CLASS);
        if (mainClass != null) {
            ep.setProperty(APPCLIENT_TOOL_MAINCLASS, mainClass);
        }
        
        String jvmOpts = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_JVM_OPTS);
        if (jvmOpts != null) {
            ep.setProperty(APPCLIENT_TOOL_JVMOPTS, jvmOpts);
        }
        
        String args = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2EE_PLATFORM_APPCLIENT_ARGS);
        if (args != null) {
            ep.setProperty(APPCLIENT_TOOL_ARGS, args);
        }    else {
            ep.remove(APPCLIENT_TOOL_ARGS);
        }
        
        //WORKAROUND for --retrieve option in asadmin deploy command
        //works only for local domains
        //see also http://www.netbeans.org/issues/show_bug.cgi?id=82929
        File asRoot = j2eePlatform.getPlatformRoots()[0];
        InstanceProperties ip = InstanceProperties.getInstanceProperties(serverInstanceID);
        //check if we have AS
        if (ip != null) {
            // Pre-v3
            if (new File(asRoot, "lib/admin-cli.jar").exists()) {
                File exFile = new File(asRoot, "lib/javaee.jar"); // NOI18N
                if (exFile.exists()) {
                    // GF v1, v2
                    ep.setProperty(APPCLIENT_WA_COPY_CLIENT_JAR_FROM,
                            new File(ip.getProperty("LOCATION"), ip.getProperty("DOMAIN") + "/generated/xml/j2ee-apps").getAbsolutePath()); // NOI18N
                } else {
                    // SJSAS 8.x
                    ep.setProperty(APPCLIENT_WA_COPY_CLIENT_JAR_FROM,
                            new File(ip.getProperty("LOCATION"), ip.getProperty("DOMAIN") + "/applications/j2ee-apps").getAbsolutePath()); // NOI18N
                }
             } else {
                String copyProperty = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME,
                        J2eePlatform.TOOL_PROP_CLIENT_JAR_LOCATION);
                if (copyProperty != null) {
                    ep.setProperty(APPCLIENT_WA_COPY_CLIENT_JAR_FROM, copyProperty);
                } else {
                    ep.remove(APPCLIENT_WA_COPY_CLIENT_JAR_FROM);
                }
             }
        } else {
            ep.remove(APPCLIENT_WA_COPY_CLIENT_JAR_FROM);
        }
        
    }
    
    private void resolveProjectDependencies() {
            
        // Create a set of old and new artifacts.
        Set<ClassPathSupport.Item> oldArtifacts = new HashSet<ClassPathSupport.Item>();
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        oldArtifacts.addAll(cs.itemsList(projectProperties.get(RUN_CLASSPATH), null));
        oldArtifacts.addAll(cs.itemsList(projectProperties.get(JAR_CONTENT_ADDITIONAL), null));
        oldArtifacts.addAll(cs.itemsList(projectProperties.get(ProjectProperties.ENDORSED_CLASSPATH), null));

        Set<ClassPathSupport.Item> newArtifacts = new HashSet<ClassPathSupport.Item>();
        newArtifacts.addAll(ClassPathUiSupport.getList( DEBUG_CLASSPATH_MODEL));
        newArtifacts.addAll(ClassPathUiSupport.getList( EAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel()));
        newArtifacts.addAll(ClassPathUiSupport.getList( ENDORSED_CLASSPATH_MODEL));

        projectProperties = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        updateContentDependency(project,
            cs.itemsList(projectProperties.get(JAR_CONTENT_ADDITIONAL), TAG_WEB_MODULE__ADDITIONAL_LIBRARIES), 
            ClassPathUiSupport.getList( EAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel()),
            projectProperties);
        updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties);
        
        // Create set of removed artifacts and remove them
        Set<ClassPathSupport.Item> removed = new HashSet<ClassPathSupport.Item>( oldArtifacts );
        removed.removeAll( newArtifacts );
        Set<ClassPathSupport.Item> added = new HashSet<ClassPathSupport.Item>(newArtifacts);
        added.removeAll(oldArtifacts);
        
        // 1. first remove all project references. The method will modify
        // project property files, so it must be done separately
        for (ClassPathSupport.Item item : removed) {
            if ( item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT ||
                    item.getType() == ClassPathSupport.Item.TYPE_JAR ) {
                refHelper.destroyReference(item.getReference());
                if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
                    item.removeSourceAndJavadoc(updateHelper);
                }
            }
        }
        
        // 2. now read project.properties and modify rest
        EditableProperties ep = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );
        boolean changed = false;
        
        for (ClassPathSupport.Item item : removed) {
            if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                // remove helper property pointing to library jar if there is any
                String prop = item.getReference();
                prop = prop.substring(2, prop.length()-1);
                ep.remove(prop);
                changed = true;
            }
        }
        if (changed) {
            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
    }
    
    
    public static String getCompletePathInArchive(EarProject project, ClassPathSupport.Item item) {
        String full = "";
        if (item.getReference() == null) {
            switch (item.getType()) {
                case ClassPathSupport.Item.TYPE_ARTIFACT:
                    full = item.getArtifact().getArtifactLocations()[0].getPath();
                    break;
                case ClassPathSupport.Item.TYPE_JAR:
                    full = item.getResolvedFile().getPath();
                    break;
                case ClassPathSupport.Item.TYPE_LIBRARY:
                    full = item.getLibrary().getName();
                    break;
                default: assert false;
            }
        } else {
            full = project.evaluator().evaluate(item.getReference());
        }
        int lastSlash = full != null ? full.lastIndexOf('/') : -1; // NOI18N
        String trimmed = null;
        trimmed = (lastSlash != -1) ? full.substring(lastSlash+1) : full;
        String path = item.getAdditionalProperty(ClassPathSupportCallbackImpl.PATH_IN_DEPLOYMENT);
        return (null != path && path.length() > 1)
                ? path + '/' + trimmed : trimmed; // NOI18N
    }

    public EarProject getProject() {
        return project;
    }

    private static class CallbackImpl implements J2EEProjectProperties.Callback {

        private EarProject project;

        public CallbackImpl(EarProject project) {
            this.project = project;
        }

        @Override
        public void registerJ2eePlatformListener(J2eePlatform platform) {
            project.registerJ2eePlatformListener(platform);
        }

        @Override
        public void unregisterJ2eePlatformListener(J2eePlatform platform) {
            project.unregisterJ2eePlatformListener(platform);
        }

    }
    
}
