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

package org.netbeans.modules.web.project.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.*;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.web.project.ProjectWebModule;

import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.web.spi.webmodule.WebModuleExtender;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;

import org.openide.filesystems.FileObject;
import org.openide.util.MutexException;
import org.openide.util.Mutex;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.javaee.project.api.ant.DeployOnSaveUtils;
import org.netbeans.modules.javaee.project.api.ui.utils.J2eePlatformUiSupport;
import org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.customizer.SourceRootsUi;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.javaee.project.api.ant.ui.customizer.LicensePanelSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.customizer.ClassPathListCellRenderer;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.modules.web.api.webmodule.WebFrameworks;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.browser.api.BrowserUISupport;
import org.netbeans.modules.web.project.UpdateProjectImpl;
import org.netbeans.modules.web.project.Utils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.web.project.WebProjectType;
import org.netbeans.modules.web.project.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.web.spi.webmodule.WebFrameworkProvider;
import org.netbeans.spi.java.project.support.ui.IncludeExcludeVisualizer;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;

/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 *
 * @author Petr Hrebejk, Radko Najman
 */
public final class WebProjectProperties {

    // Special properties of the project
    public static final String WEB_PROJECT_NAME = "web.project.name"; //NOI18N
    public static final String JAVA_PLATFORM = "platform.active"; //NOI18N
    public static final String J2EE_PLATFORM = "j2ee.platform"; //NOI18N

    // Properties stored in the PROJECT.PROPERTIES
    /** root of external web module sources (full path), ".." if the sources are within project folder */
    public static final String SOURCE_ROOT = "source.root"; //NOI18N
    public static final String SOURCE_ENCODING="source.encoding"; // NOI18N
    public static final String BUILD_FILE = "buildfile"; //NOI18N
    public static final String LIBRARIES_DIR = "lib.dir"; //NOI18N
    public static final String DIST_DIR = "dist.dir"; //NOI18N
    public static final String DIST_WAR = "dist.war"; //NOI18N
    public static final String DIST_WAR_EAR = "dist.ear.war"; //NOI18N
    public static final String DEBUG_CLASSPATH = "debug.classpath";     //NOI18N
    public static final String JSPCOMPILATION_CLASSPATH = "jspcompilation.classpath";     //NOI18N

    public static final String WAR_NAME = "war.name"; //NOI18N
    public static final String WAR_EAR_NAME = "war.ear.name"; //NOI18N
    public static final String WAR_COMPRESS = "jar.compress"; //NOI18N
    public static final String WAR_CONTENT_ADDITIONAL = "war.content.additional"; //NOI18N

    public static final String LAUNCH_URL_RELATIVE = "client.urlPart"; //NOI18N
    public static final String DISPLAY_BROWSER = "display.browser"; //NOI18N
    public static final String J2EE_DEPLOY_ON_SAVE = "j2ee.deploy.on.save"; //NOI18N
    public static final String J2EE_COMPILE_ON_SAVE = "j2ee.compile.on.save"; //NOI18N
    public static final String J2EE_COPY_STATIC_FILES_ON_SAVE = "j2ee.copy.static.files.on.save"; //NOI18N
    public static final String CONTEXT_PATH = "context.path"; //NOI18N
    public static final String J2EE_SERVER_INSTANCE = J2EEProjectProperties.J2EE_SERVER_INSTANCE;
    public static final String J2EE_SERVER_CHECK = "j2ee.server.check"; //NOI18N
    public static final String J2EE_SERVER_TYPE = J2EEProjectProperties.J2EE_SERVER_TYPE;
    public static final String J2EE_PLATFORM_CLASSPATH = "j2ee.platform.classpath"; //NOI18N
    public static final String J2EE_PLATFORM_EMBEDDABLE_EJB_CLASSPATH = "j2ee.platform.embeddableejb.classpath"; //NOI18N
    public static final String JAVAC_SOURCE = "javac.source"; //NOI18N
    public static final String JAVAC_DEBUG = "javac.debug"; //NOI18N
    public static final String JAVAC_DEPRECATION = "javac.deprecation"; //NOI18N
    public static final String JAVAC_COMPILER_ARG = "javac.compilerargs";    //NOI18N
    public static final String JAVAC_TARGET = "javac.target"; //NOI18N
    public static final String SRC_DIR = "src.dir"; //NOI18N
    public static final String TEST_SRC_DIR = "test.src.dir"; //NOI18N
    public static final String CONF_DIR = "conf.dir"; //NOI18N
    public static final String PERSISTENCE_XML_DIR = "persistence.xml.dir"; //NOI18N
    public static final String WEB_DOCBASE_DIR = "web.docbase.dir"; //NOI18N
    public static final String RESOURCE_DIR = "resource.dir"; //NOI18N
    public static final String WEBINF_DIR = "webinf.dir"; //NOI18N
    public static final String BUILD_DIR = "build.dir"; //NOI18N
    public static final String BUILD_WEB_DIR = "build.web.dir"; //NOI18N
    public static final String BUILD_GENERATED_DIR = "build.generated.dir"; //NOI18N
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"; //NOI18N
    public static final String BUILD_WEB_EXCLUDES = "build.web.excludes"; //NOI18N
    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir"; //NOI18N
    public static final String NO_DEPENDENCIES="no.dependencies"; //NOI18N
    public static final String RUNMAIN_JVM_ARGS = "runmain.jvmargs"; //NOI18N

    public static final String BUILD_TEST_RESULTS_DIR = "build.test.results.dir"; // NOI18N
    public static final String DEBUG_TEST_CLASSPATH = "debug.test.classpath"; // NOI18N

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
    public static final String JAVADOC_ADDITIONALPARAM="javadoc.additionalparam"; // NOI18N

    public static final String COMPILE_JSPS = "compile.jsps"; //NOI18N

    public static final String TAG_WEB_MODULE_LIBRARIES = "web-module-libraries"; // NOI18N
    public static final String TAG_WEB_MODULE__ADDITIONAL_LIBRARIES = "web-module-additional-libraries"; //NOI18N

    // Properties stored in the PRIVATE.PROPERTIES
    public static final String APPLICATION_ARGS = "application.args"; // NOI18N
    public static final String JAVADOC_PREVIEW="javadoc.preview"; // NOI18N

    public static final String WS_DEBUG_CLASSPATHS = "ws.debug.classpaths";     //NOI18N
    public static final String WS_WEB_DOCBASE_DIRS = "ws.web.docbase.dirs"; //NOI18N

    public static final String DEPLOY_ANT_PROPS_FILE = "deploy.ant.properties.file"; //NOI18N

    public static final String ANT_DEPLOY_BUILD_SCRIPT = "nbproject/ant-deploy.xml"; // NOI18N
    public static final String SELECTED_BROWSER = "selected.browser"; //NOI18N

    //Files excluded from WAR:
    public static final String DIST_ARCHIVE_EXCLUDES = "dist.archive.excludes";   //NOI18N

    private static Logger LOGGER = Logger.getLogger(WebProjectProperties.class.getName());
    private static RequestProcessor RP = new RequestProcessor("WebProjectProperties", 5);

    public ClassPathSupport cs;

    //list of frameworks to add to the application
    private List newExtenders;

    //list of changed frameworks
    private List<WebModuleExtender> existingExtenders;

    // MODELS FOR VISUAL CONTROLS

    // CustomizerSources
    DefaultTableModel SOURCE_ROOTS_MODEL;
    DefaultTableModel TEST_ROOTS_MODEL;
    Document WEB_DOCBASE_DIR_MODEL;
    Document WEBINF_DIR_MODEL;
    ComboBoxModel JAVAC_SOURCE_MODEL;

    // CustomizerLibraries
    ClassPathTableModel JAVAC_CLASSPATH_MODEL;
    DefaultListModel JAVAC_TEST_CLASSPATH_MODEL;
    DefaultListModel RUN_TEST_CLASSPATH_MODEL;
    DefaultListModel ENDORSED_CLASSPATH_MODEL;
    ComboBoxModel PLATFORM_MODEL;
    ListCellRenderer CLASS_PATH_LIST_RENDERER;
    ListCellRenderer PLATFORM_LIST_RENDERER;
    ListCellRenderer JAVAC_SOURCE_RENDERER;
    TableCellRenderer CLASS_PATH_TABLE_ITEM_RENDERER;
    Document SHARED_LIBRARIES_MODEL;
    DefaultListModel JAVAC_PROCESSORPATH_MODEL;

    // CustomizerCompile
    ButtonModel JAVAC_DEPRECATION_MODEL;
    ButtonModel JAVAC_DEBUG_MODEL;
    ButtonModel NO_DEPENDENCIES_MODEL;
    Document JAVAC_COMPILER_ARG_MODEL;
    ButtonModel COMPILE_JSP_MODEL;
    ButtonModel ENABLE_ANNOTATION_PROCESSING_MODEL;
    ButtonModel ENABLE_ANNOTATION_PROCESSING_IN_EDITOR_MODEL;
    DefaultListModel ANNOTATION_PROCESSORS_MODEL;
    JToggleButton.ToggleButtonModel COMPILE_ON_SAVE_MODEL;
    JToggleButton.ToggleButtonModel COPY_STATIC_RESOURCE_ON_SAVE_MODEL;

    // CustomizerWar
    Document WAR_NAME_MODEL;
    Document BUILD_CLASSES_EXCLUDES_MODEL;
    ButtonModel WAR_COMPRESS_MODEL;
    WarIncludesTableModel WAR_CONTENT_ADDITIONAL_MODEL;

    // CustomizerJavadoc
    ButtonModel JAVADOC_PRIVATE_MODEL;
    ButtonModel JAVADOC_NO_TREE_MODEL;
    ButtonModel JAVADOC_USE_MODEL;
    ButtonModel JAVADOC_NO_NAVBAR_MODEL;
    ButtonModel JAVADOC_NO_INDEX_MODEL;
    ButtonModel JAVADOC_SPLIT_INDEX_MODEL;
    ButtonModel JAVADOC_AUTHOR_MODEL;
    ButtonModel JAVADOC_VERSION_MODEL;
    Document JAVADOC_WINDOW_TITLE_MODEL;
    ButtonModel JAVADOC_PREVIEW_MODEL;
    Document JAVADOC_ADDITIONALPARAM_MODEL;

    // CustomizerRun
    Document J2EE_PLATFORM_MODEL;
    Document CONTEXT_PATH_MODEL;
    Document LAUNCH_URL_RELATIVE_MODEL;
    ButtonModel DISPLAY_BROWSER_MODEL;
    JToggleButton.ToggleButtonModel DEPLOY_ON_SAVE_MODEL;
    ComboBoxModel J2EE_SERVER_INSTANCE_MODEL;
    BrowserUISupport.BrowserComboBoxModel BROWSERS_MODEL;
    Document RUNMAIN_JVM_MODEL;

    //customizer license headers
    LicensePanelSupport LICENSE_SUPPORT;

    // for ui logging added frameworks
    private List<String> addedFrameworkNames;
    private List<WebFrameworkProvider> currentFrameworks;

    // Private fields ----------------------------------------------------------
    private WebProject project;
    private ReferenceHelper refHelper;
    private UpdateHelper updateHelper;
    private PropertyEvaluator evaluator;

    private StoreGroup privateGroup;
    private StoreGroup projectGroup;

    private Map<String,String> additionalProperties;

    private static boolean needsUpdate = false;

    private static String serverId;
    private static String cp;

    public static final String JAVA_SOURCE_BASED= "java.source.based";

    private String includes, excludes;

    private static String logServInstID = null;

    Task loadingFrameworksTask = null;

    //Hotfix of the issue #70058 (copied from J2seProjectProperties)
    //Should be removed when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
    private static final Integer BOOLEAN_KIND_TF = 0;
    private static final Integer BOOLEAN_KIND_YN = 1;
    private static final Integer BOOLEAN_KIND_ED = 2;

    private final List<ActionListener> optionListeners = new CopyOnWriteArrayList<ActionListener>();

    WebProjectProperties(WebProject project, UpdateHelper updateHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper) {
        this.project = project;
        this.updateHelper = updateHelper;

        //this is called from updatehelper when user confirms the project update
        project.getUpdateImplementation().setProjectUpdateListener(new UpdateProjectImpl.ProjectUpdateListener() {
            public void projectUpdated() {
                needsUpdate = true;
            }
        });

        this.evaluator = evaluator;
        this.refHelper = refHelper;

        this.cs = new ClassPathSupport( evaluator, refHelper,
                updateHelper.getAntProjectHelper(), updateHelper,
                new ClassPathSupportCallbackImpl(updateHelper.getAntProjectHelper()));

        privateGroup = new StoreGroup();
        projectGroup = new StoreGroup();

        additionalProperties = new HashMap<String,String>();

        init(); // Load known properties
    }

    WebProject getProject() {
        return project;
    }

    /** Initializes the visual models
     */
    private void init() {

        CLASS_PATH_LIST_RENDERER = ClassPathListCellRenderer.createClassPathListRenderer(evaluator, project.getProjectDirectory());
        CLASS_PATH_TABLE_ITEM_RENDERER = ClassPathListCellRenderer.createClassPathTableRenderer(evaluator, project.getProjectDirectory());

        // CustomizerSources
        SOURCE_ROOTS_MODEL = SourceRootsUi.createModel( project.getSourceRoots() );
        TEST_ROOTS_MODEL = SourceRootsUi.createModel( project.getTestSourceRoots() );
        includes = evaluator.getProperty(ProjectProperties.INCLUDES);
        if (includes == null) {
            includes = "**"; // NOI18N
        }
        excludes = evaluator.getProperty(ProjectProperties.EXCLUDES);
        if (excludes == null) {
            excludes = ""; // NOI18N
        }
        WEB_DOCBASE_DIR_MODEL = projectGroup.createStringDocument( evaluator, WEB_DOCBASE_DIR );
        WEBINF_DIR_MODEL = projectGroup.createStringDocument( evaluator, WEBINF_DIR );

        // CustomizerLibraries
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );
        EditableProperties privateProperties = updateHelper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );

        JAVAC_CLASSPATH_MODEL = ClassPathTableModel.createTableModel( cs.itemsIterator( (String)projectProperties.get( ProjectProperties.JAVAC_CLASSPATH ), ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES) );
        String processorPath = projectProperties.get(ProjectProperties.JAVAC_PROCESSORPATH);
        processorPath = processorPath == null ? "${javac.classpath}" : processorPath;
        JAVAC_PROCESSORPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(processorPath));
        JAVAC_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator( (String)projectProperties.get( ProjectProperties.JAVAC_TEST_CLASSPATH ), null ) );
        RUN_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator( (String)projectProperties.get( ProjectProperties.RUN_TEST_CLASSPATH ), null ) );
        ENDORSED_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator( (String)projectProperties.get( ProjectProperties.ENDORSED_CLASSPATH ), null ) );
        PLATFORM_MODEL = PlatformUiSupport.createPlatformComboBoxModel (evaluator.getProperty(JAVA_PLATFORM));
        PLATFORM_LIST_RENDERER = PlatformUiSupport.createPlatformListCellRenderer();
        SpecificationVersion minimalSourceLevel = null;
        Profile profile = Profile.fromPropertiesString(evaluator.getProperty(J2EE_PLATFORM));
        switch (profile) {
            case JAKARTA_EE_11_FULL:
                minimalSourceLevel = new SpecificationVersion("21");
                break;
            case JAKARTA_EE_10_FULL:
            case JAKARTA_EE_9_1_FULL:
                minimalSourceLevel = new SpecificationVersion("11");
                break;
            case JAKARTA_EE_9_FULL:
            case JAKARTA_EE_8_FULL:
            case JAVA_EE_8_FULL:
                minimalSourceLevel = new SpecificationVersion("1.8");
                break;
            case JAVA_EE_7_FULL:
                minimalSourceLevel = new SpecificationVersion("1.7");
                break;
            case JAVA_EE_6_FULL:
                minimalSourceLevel = new SpecificationVersion("1.6");
                break;
            case JAVA_EE_5:
                minimalSourceLevel = new SpecificationVersion("1.5");
                break;
            default:
                break;
        }
        JAVAC_SOURCE_MODEL = PlatformUiSupport.createSourceLevelComboBoxModel (PLATFORM_MODEL, evaluator.getProperty(JAVAC_SOURCE), evaluator.getProperty(JAVAC_TARGET), minimalSourceLevel);
        JAVAC_SOURCE_RENDERER = PlatformUiSupport.createSourceLevelListCellRenderer ();
        SHARED_LIBRARIES_MODEL = new PlainDocument();
        try {
            SHARED_LIBRARIES_MODEL.insertString(0, project.getAntProjectHelper().getLibrariesLocation(), null);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        // CustomizerCompile
        JAVAC_DEPRECATION_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVAC_DEPRECATION );
        JAVAC_DEBUG_MODEL = privateGroup.createToggleButtonModel( evaluator, JAVAC_DEBUG );
        NO_DEPENDENCIES_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, NO_DEPENDENCIES );
        ENABLE_ANNOTATION_PROCESSING_MODEL =projectGroup.createToggleButtonModel(evaluator, ProjectProperties.ANNOTATION_PROCESSING_ENABLED);
        ENABLE_ANNOTATION_PROCESSING_IN_EDITOR_MODEL = projectGroup.createToggleButtonModel(evaluator, ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR);
        String annotationProcessors = projectProperties.get(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST);
        if (annotationProcessors == null)
            annotationProcessors = ""; //NOI18N
        ANNOTATION_PROCESSORS_MODEL = ClassPathUiSupport.createListModel(
                (annotationProcessors.length() > 0 ? Arrays.asList(annotationProcessors.split(",")) : Collections.emptyList()).iterator()); //NOI18N
        JAVAC_COMPILER_ARG_MODEL = projectGroup.createStringDocument( evaluator, JAVAC_COMPILER_ARG );
        COMPILE_JSP_MODEL = projectGroup.createToggleButtonModel( evaluator, COMPILE_JSPS );

        // CustomizerWar
        WAR_NAME_MODEL = projectGroup.createStringDocument( evaluator, WAR_NAME );
        BUILD_CLASSES_EXCLUDES_MODEL = projectGroup.createStringDocument( evaluator, BUILD_CLASSES_EXCLUDES );
        WAR_COMPRESS_MODEL = projectGroup.createToggleButtonModel( evaluator, WAR_COMPRESS );
        WAR_CONTENT_ADDITIONAL_MODEL = WarIncludesTableModel.createTableModel( cs.itemsIterator( (String)projectProperties.get( WAR_CONTENT_ADDITIONAL ), ClassPathSupportCallbackImpl.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES));

        // CustomizerJavadoc
        JAVADOC_PRIVATE_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_PRIVATE );
        JAVADOC_NO_TREE_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, JAVADOC_NO_TREE );
        JAVADOC_USE_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_USE );
        JAVADOC_NO_NAVBAR_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, JAVADOC_NO_NAVBAR );
        JAVADOC_NO_INDEX_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, JAVADOC_NO_INDEX );
        JAVADOC_SPLIT_INDEX_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_SPLIT_INDEX );
        JAVADOC_AUTHOR_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_AUTHOR );
        JAVADOC_VERSION_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_VERSION );
        JAVADOC_WINDOW_TITLE_MODEL = projectGroup.createStringDocument( evaluator, JAVADOC_WINDOW_TITLE );
        JAVADOC_PREVIEW_MODEL = privateGroup.createToggleButtonModel( evaluator, JAVADOC_PREVIEW );
        JAVADOC_ADDITIONALPARAM_MODEL = projectGroup.createStringDocument( evaluator, JAVADOC_ADDITIONALPARAM );

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
        COPY_STATIC_RESOURCE_ON_SAVE_MODEL = projectGroup.createToggleButtonModel(evaluator, J2EE_COPY_STATIC_FILES_ON_SAVE);
        COPY_STATIC_RESOURCE_ON_SAVE_MODEL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!COPY_STATIC_RESOURCE_ON_SAVE_MODEL.isSelected()) {
                    DEPLOY_ON_SAVE_MODEL.setSelected(false);
                }
            }
        });
        DEPLOY_ON_SAVE_MODEL.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (DEPLOY_ON_SAVE_MODEL.isSelected()) {
                    COMPILE_ON_SAVE_MODEL.setSelected(true);
                    COPY_STATIC_RESOURCE_ON_SAVE_MODEL.setSelected(true);
                }
            }
        });

        J2EE_SERVER_INSTANCE_MODEL = J2eePlatformUiSupport.createPlatformComboBoxModel(
                privateProperties.getProperty( J2EE_SERVER_INSTANCE ),
                Profile.fromPropertiesString(projectProperties.getProperty(J2EE_PLATFORM)),
                J2eeModule.Type.WAR);
        RUNMAIN_JVM_MODEL = projectGroup.createStringDocument(evaluator, RUNMAIN_JVM_ARGS);
        try {
            CONTEXT_PATH_MODEL = new PlainDocument();
            CONTEXT_PATH_MODEL.remove(0, CONTEXT_PATH_MODEL.getLength());
            ProjectWebModule wm = (ProjectWebModule) project.getLookup().lookup(ProjectWebModule.class);
            String contextPath = wm.getContextPath();
            if (contextPath != null) {
                CONTEXT_PATH_MODEL.insertString(0, contextPath, null);
            }
        } catch (BadLocationException exc) {
            //ignore
        }
        String selectedBrowser = evaluator.getProperty(SELECTED_BROWSER);
        BROWSERS_MODEL = BrowserUISupport.createBrowserModel(selectedBrowser, true);
        loadingFrameworksTask = RP.post(new Runnable() {
                public void run() {
                    loadCurrentFrameworks();
                }
            });

        LICENSE_SUPPORT = new LicensePanelSupport(evaluator, project.getAntProjectHelper(),
                projectProperties.get(LicensePanelSupport.LICENSE_PATH),
                projectProperties.get(LicensePanelSupport.LICENSE_NAME));

    }

    // #148786 - load frameworks in background thread
    private void loadCurrentFrameworks() {
        List frameworks = WebFrameworks.getFrameworks();
        WebModule webModule = project.getAPIWebModule();
        List<WebFrameworkProvider> list = new LinkedList<WebFrameworkProvider>();
        if (frameworks != null & webModule != null) {
            for (int i = 0; i < frameworks.size(); i++) {
                WebFrameworkProvider framework = (WebFrameworkProvider) frameworks.get(i);
                if (framework.isInWebModule(webModule)) {
                    list.add(framework);
                }
            }
        }
        currentFrameworks = list;
    }

    Task getLoadingFrameworksTask() {
        return loadingFrameworksTask;
    }

    List<WebFrameworkProvider> getCurrentFrameworks() {
        return currentFrameworks;
    }

    public void save() {
        try {
            saveLibrariesLocation();
            LICENSE_SUPPORT.saveLicenseFile();
            // Store properties
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    storeProperties();
                    return null;
                }
            });
            // and save the project
            project.setProjectPropertiesSave(true);
            try {
                ProjectManager.getDefault().saveProject(project);
            } finally {
                project.setProjectPropertiesSave(false);
            }

            if (COMPILE_JSP_MODEL.isSelected()) {
                WebProject.makeSureProjectHasJspCompilationLibraries(refHelper);
            }

            Profile j2eeProfile = project.getAPIWebModule().getJ2eeProfile();
            FileObject webInf = project.getAPIWebModule().getWebInf();
            FileObject ddFo = project.getAPIWebModule().getDeploymentDescriptor();
            if (ddFo == null && shouldCreateWebXml() && webInf != null) {
                DDHelper.createWebXml(j2eeProfile, webInf);
            }

            // handle new and existing extenders
            handleExtenders(newExtenders, existingExtenders);

            // ui logging of the added frameworks
            if ((addedFrameworkNames != null) && (addedFrameworkNames.size() > 0)) {
                Utils.logUI(NbBundle.getBundle(WebProjectProperties.class),"UI_WEB_PROJECT_FRAMEWORK_ADDED", // NOI18N
                        addedFrameworkNames.toArray());
            }

            // usage logging of target server and currently active frameworks
            String serverName = ""; // NOI18N
            try {
                if (logServInstID != null) {
                    serverName = Deployment.getDefault().getServerInstance(logServInstID).getServerDisplayName();
                }
            }
            catch(InstanceRemovedException ier) {
                // ignore
            }

            if (loadingFrameworksTask != null && loadingFrameworksTask.isFinished()) {
                StringBuffer sb = new StringBuffer(50);
                if (currentFrameworks != null && currentFrameworks.size() > 0) {
                    for (int i = 0; i < currentFrameworks.size(); i++) {
                        if (sb.length() > 0) {
                            sb.append("|"); // NOI18N
                        }
                        sb.append(currentFrameworks.get(i).getName());
                    }
                }
                if (addedFrameworkNames != null && addedFrameworkNames.size() > 0) {
                    for (int i = 0; i < addedFrameworkNames.size(); i++) {
                        if (sb.length() > 0) {
                            sb.append("|"); // NOI18N
                        }
                        sb.append(addedFrameworkNames.get(i));
                    }
                }
                Utils.logUsage(WebProjectProperties.class, "USG_PROJECT_CONFIG_WEB", new Object[] { serverName, sb }); // NOI18N
            }

            //prevent deadlock reported in the issue #54643
            //cp and serverId values are read in setNewContextPathValue() method which is called from storeProperties() before this code
            //it is easier to preset them instead of reading them here again
            if (cp != null) {
                ProjectWebModule wm = (ProjectWebModule) project.getLookup().lookup(ProjectWebModule.class);
                String oldCP = wm.getContextPath(serverId);
                if (!cp.equals(oldCP))
                    wm.setContextPath(serverId, cp);
            }

            //Delete COS mark
            if (!COMPILE_ON_SAVE_MODEL.isSelected()) {
                DeployOnSaveUtils.performCleanup(project, evaluator, updateHelper, "build.classes.dir", false); // NOI18N
            }
        }
        catch (MutexException e) {
            Exceptions.printStackTrace((IOException) e.getException());
        }
        catch ( IOException ex ) {
            Exceptions.printStackTrace(ex);
        }

    }

    private boolean shouldCreateWebXml() {
        J2eeModuleProvider provider = getProject().getLookup().lookup(J2eeModuleProvider.class);
        if (provider != null) {
            if (provider.getConfigSupport().isDescriptorRequired()) {
                return true;
            }
        }

        boolean res = false;
        if (addedFrameworkNames != null) {
            for (String fName : addedFrameworkNames) {
                for (WebFrameworkProvider wfp : WebFrameworks.getFrameworks()) {
                    if (wfp.getName().equals(fName)) {
                        res |= wfp.requiresWebXml();
                        break;
                    }
                }
            }
        }
        return res;
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
        destroyRemovedDependencies();

        // Store source roots
        storeRoots( project.getSourceRoots(), SOURCE_ROOTS_MODEL );
        storeRoots( project.getTestSourceRoots(), TEST_ROOTS_MODEL );

        //test whether user wants to update his project to newest version
        if(needsUpdate) {
            //remove servlet24 and jsp20 libraries (they are not used in 4.1)
            ClassPathTableModel cptm = getJavaClassPathModel();

            ArrayList<ClassPathSupport.Item> cpItemsToRemove = new ArrayList<ClassPathSupport.Item>();
            for(int i = 0; i < cptm.getRowCount(); i++) {
                Object item = cptm.getValueAt(i,0);
                if (item instanceof ClassPathSupport.Item) {
                    ClassPathSupport.Item cpti = (ClassPathSupport.Item)item;
                    String propertyName = cpti.getReference();
                    if(propertyName != null) {
                        String libname = propertyName.substring("${libs.".length());
                        if(libname.indexOf(".classpath}") != -1) libname = libname.substring(0, libname.indexOf(".classpath}"));

                        if("servlet24".equals(libname) || "jsp20".equals(libname)) { //NOI18N
                            cpItemsToRemove.add(cpti);
                        }
                    }
                }
            }

            //remove selected libraries
            Iterator<ClassPathSupport.Item> remove = cpItemsToRemove.iterator();
            while(remove.hasNext()) {
                ClassPathSupport.Item cpti = remove.next();
                cptm.getDefaultListModel().removeElement(cpti);
            }

            //commented out, one more check follows
            //needsUpdate = false;
        }

        // Encode all paths (this may change the project properties)
        List<ClassPathSupport.Item> javaClasspathList = ClassPathUiSupport.getList(JAVAC_CLASSPATH_MODEL.getDefaultListModel());
        String[] javac_cp = cs.encodeToStrings(javaClasspathList, ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES  );
        String[] javac_test_cp = cs.encodeToStrings( ClassPathUiSupport.getList( JAVAC_TEST_CLASSPATH_MODEL ), null );
        String[] run_test_cp = cs.encodeToStrings( ClassPathUiSupport.getList( RUN_TEST_CLASSPATH_MODEL ), null );
        String[] war_includes = cs.encodeToStrings( ClassPathUiSupport.getList( WAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel() ), ClassPathSupportCallbackImpl.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES  );
        String[] endorsed_cp = cs.encodeToStrings( ClassPathUiSupport.getList( ENDORSED_CLASSPATH_MODEL ), null );
        String[] javac_pp = cs.encodeToStrings( ClassPathUiSupport.getList( JAVAC_PROCESSORPATH_MODEL ) );

        // Store standard properties
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );
        EditableProperties privateProperties = updateHelper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );

        // Assure inegrity which can't shound not be assured in UI
        if ( !JAVADOC_NO_INDEX_MODEL.isSelected() ) {
            JAVADOC_SPLIT_INDEX_MODEL.setSelected( false ); // Can't split non existing index
        }

        // Standard store of the properties
        projectGroup.store( projectProperties );
        privateGroup.store( privateProperties );

        LICENSE_SUPPORT.updateProperties(projectProperties);

        //test whether user wants to update his project to newest version
        if(needsUpdate) {
            //add items for test classpath (they are not used in 4.1)
            javac_test_cp = new String[] {
                "${javac.classpath}:", // NOI18N
                "${build.classes.dir}:", // NOI18N
                "${libs.junit.classpath}:", // NOI18N
                "${libs.junit_4.classpath}", // NOI18N
            };
            run_test_cp = new String[] {
                "${javac.test.classpath}:", // NOI18N
                "${build.test.classes.dir}", // NOI18N
            };
            projectProperties.setProperty(DEBUG_TEST_CLASSPATH, new String[] {
                "${run.test.classpath}", // NOI18N
            });

            needsUpdate = false;
        }

        // Save all paths
        projectProperties.setProperty( ProjectProperties.JAVAC_CLASSPATH, javac_cp );
        projectProperties.setProperty( ProjectProperties.JAVAC_PROCESSORPATH, javac_pp );
        projectProperties.setProperty( ProjectProperties.JAVAC_TEST_CLASSPATH, javac_test_cp );
        projectProperties.setProperty( ProjectProperties.RUN_TEST_CLASSPATH, run_test_cp );
        projectProperties.setProperty( ProjectProperties.ENDORSED_CLASSPATH, endorsed_cp );

        projectProperties.setProperty( WAR_CONTENT_ADDITIONAL, war_includes );

        //Handle platform selection and javac.source javac.target properties
        PlatformUiSupport.storePlatform (projectProperties, updateHelper, WebProjectType.PROJECT_CONFIGURATION_NAMESPACE, PLATFORM_MODEL.getSelectedItem(), JAVAC_SOURCE_MODEL.getSelectedItem());

        // Handle other special cases
        if ( NO_DEPENDENCIES_MODEL.isSelected() ) { // NOI18N
            projectProperties.remove( NO_DEPENDENCIES ); // Remove the property completely if not set
        }

        if (J2EE_SERVER_INSTANCE_MODEL.getSelectedItem() != null) {
            final String instanceId = J2eePlatformUiSupport.getServerInstanceID(J2EE_SERVER_INSTANCE_MODEL.getSelectedItem());
            J2EEProjectProperties.updateServerProperties(projectProperties, privateProperties, instanceId,
                    cs, javaClasspathList,
                    new CallbackImpl(project), project,
                    project.getAPIWebModule().getJ2eeProfile(), J2eeModule.Type.WAR);
        }

        // Set new context path
        try {
            String clsPth = CONTEXT_PATH_MODEL.getText(0, CONTEXT_PATH_MODEL.getLength());
            if (clsPth == null) {
                clsPth = "/" + PropertyUtils.getUsablePropertyName(project.getName()); //NOI18N
            } else if (!isCorrectCP(clsPth)) {
                if (clsPth.startsWith("/")) //NOI18N
                    clsPth = clsPth.substring(1);
                clsPth = "/" + PropertyUtils.getUsablePropertyName(clsPth); //NOI18N
            }
            setNewContextPathValue(clsPth, project, projectProperties, privateProperties);
        } catch (BadLocationException exc) {
            //ignore
        }

        privateProperties.setProperty(SELECTED_BROWSER, BROWSERS_MODEL.getSelectedBrowserId());

        projectProperties.putAll(additionalProperties);

        projectProperties.put(ProjectProperties.INCLUDES, includes);
        projectProperties.put(ProjectProperties.EXCLUDES, excludes);

        StringBuilder sb = new StringBuilder();
        for (Enumeration elements = ANNOTATION_PROCESSORS_MODEL.elements(); elements.hasMoreElements();) {
            sb.append(elements.nextElement());
            if (elements.hasMoreElements())
                sb.append(',');
        }
        if (sb.length() > 0) {
            projectProperties.put(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, encodeBoolean(false, BOOLEAN_KIND_TF));
            projectProperties.put(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, sb.toString());
        } else {
            projectProperties.put(ProjectProperties.ANNOTATION_PROCESSING_RUN_ALL_PROCESSORS, encodeBoolean(true, BOOLEAN_KIND_TF));
            projectProperties.put(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST, ""); // NOI18N
        }

        // Store the property changes into the project
        updateHelper.putProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProperties );
        updateHelper.putProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProperties );

        // compile on save listeners
        if (DEPLOY_ON_SAVE_MODEL.isEnabled() && DEPLOY_ON_SAVE_MODEL.isSelected()) {
            LOGGER.log(Level.FINE, "Starting listening on cos for {0}", project.getWebModule());
            Deployment.getDefault().enableCompileOnSaveSupport(project.getWebModule());
        } else {
            LOGGER.log(Level.FINE, "Stopping listening on cos for {0}", project.getWebModule());
            Deployment.getDefault().disableCompileOnSaveSupport(project.getWebModule());
        }

        String value = (String)additionalProperties.get(SOURCE_ENCODING);
        if (value != null) {
            try {
                FileEncodingQuery.setDefaultEncoding(Charset.forName(value));
            } catch (UnsupportedCharsetException e) {
                //When the encoding is not supported by JVM do not set it as default
            }
        }
    }

    private static boolean isCorrectCP(String contextPath) {
        if (contextPath.length() == 0) {
            return true;
        } else if (!contextPath.startsWith("/")) { //NOI18N
	    return false;
        } else if (contextPath.endsWith("/")) {     //NOI18N
            return false;
        } else if (contextPath.indexOf("//") >= 0) { //NOI18N
	    return false;
        } else if (contextPath.indexOf(' ') >= 0) {  //NOI18N
	    return false;
        }
	return true;
    }

    /** XXX to be deleted when introduced in AntPropertyHeleper API
     */
    static boolean isAntProperty (String string) {
        return string != null && string.startsWith( "${" ) && string.endsWith( "}" ); //NOI18N
    }

    /** Finds out what are new and removed project dependencies and
     * applyes the info to the project
     */
    private void destroyRemovedDependencies() {

        // Create a set of old and new artifacts.
        Set<ClassPathSupport.Item> oldArtifacts = new HashSet<ClassPathSupport.Item>();
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );
        oldArtifacts.addAll( cs.itemsList( (String)projectProperties.get( ProjectProperties.JAVAC_CLASSPATH ), ClassPathSupportCallbackImpl.TAG_WEB_MODULE_LIBRARIES ) );
        oldArtifacts.addAll( cs.itemsList( projectProperties.get( ProjectProperties.JAVAC_PROCESSORPATH ) ) );
        oldArtifacts.addAll( cs.itemsList( (String)projectProperties.get( ProjectProperties.JAVAC_TEST_CLASSPATH ), null ) );
        oldArtifacts.addAll( cs.itemsList( (String)projectProperties.get( ProjectProperties.RUN_TEST_CLASSPATH ), null ) );
        oldArtifacts.addAll( cs.itemsList( (String)projectProperties.get( WAR_CONTENT_ADDITIONAL ), ClassPathSupportCallbackImpl.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES ) );

        Set<ClassPathSupport.Item> newArtifacts = new HashSet<ClassPathSupport.Item>();
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_CLASSPATH_MODEL.getDefaultListModel() ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_PROCESSORPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_TEST_CLASSPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( RUN_TEST_CLASSPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( WAR_CONTENT_ADDITIONAL_MODEL.getDefaultListModel() ) );

        // Create set of removed artifacts and remove them
        Set<ClassPathSupport.Item> removed = new HashSet<ClassPathSupport.Item>( oldArtifacts );
        removed.removeAll( newArtifacts );
        Set<ClassPathSupport.Item> added = new HashSet<ClassPathSupport.Item>(newArtifacts);
        added.removeAll(oldArtifacts);

        // 1. first remove all project references. The method will modify
        // project property files, so it must be done separately
        for( ClassPathSupport.Item item : removed) {
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

        for( ClassPathSupport.Item item : removed) {
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

    private void storeRoots( SourceRoots roots, DefaultTableModel tableModel ) throws MalformedURLException {
        Vector data = tableModel.getDataVector();
        URL[] rootURLs = new URL[data.size()];
        String []rootLabels = new String[data.size()];
        for (int i=0; i<data.size();i++) {
            File f = ((File)((Vector)data.elementAt(i)).elementAt(0));
            rootURLs[i] = Utils.getRootURL(f,null);
            rootLabels[i] = (String) ((Vector)data.elementAt(i)).elementAt(1);
        }
        roots.putRoots(rootURLs,rootLabels);
    }

    public void store() {
        save();
    }

    public static void setServerInstanceInner(final WebProject project, final UpdateHelper helper, final String serverInstanceID) throws IOException {
        final AtomicReference<IOException> exRef = new AtomicReference<>();
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            @Override
            public void run() {
                EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                J2EEProjectProperties.updateServerProperties(projectProps, privateProps, serverInstanceID,
                        null, null, new CallbackImpl(project), project,
                        project.getAPIWebModule().getJ2eeProfile(), J2eeModule.Type.WAR);
                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
                helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                try {
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException ex) {
                    exRef.set(ex);
                }

            }
        });
        if (exRef.get() != null) {
            throw exRef.get();
        }
    }

    public static void setServerInstance(final WebProject project, final UpdateHelper helper, final String serverInstanceID) {
        try {
            setServerInstanceInner(project, helper, serverInstanceID);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /* This is used by CustomizerWSServiceHost */
    void putAdditionalProperty(String propertyName, String propertyValue) {
        additionalProperties.put(propertyName, propertyValue);
    }

    private static void setNewContextPathValue(String contextPath, Project project, EditableProperties projectProps, EditableProperties privateProps) {
        if (contextPath == null)
            return;

        cp = contextPath;
        serverId = privateProps.getProperty(J2EE_SERVER_INSTANCE);
    }

    //Hotfix of the issue #70058 (copied from J2SEProjectProperties)
    //Should be removed when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
    private static String encodeBoolean (boolean value, Integer kind) {
        if ( BOOLEAN_KIND_ED.equals(kind) ) {
            return value ? "on" : "off"; // NOI18N
        }
        else if ( BOOLEAN_KIND_YN.equals(kind) ) { // NOI18N
            return value ? "yes" : "no";
        }
        else {
            return value ? "true" : "false"; // NOI18N
        }
    }

    public ClassPathTableModel getJavaClassPathModel() {
        return JAVAC_CLASSPATH_MODEL;
    }

    public void setNewExtenders(List extenders) {
        newExtenders = extenders;
    }

    public void setExistingExtenders(List<WebModuleExtender> extenders) {
        existingExtenders = extenders;
    }

    public void setNewFrameworksNames(List<String> names) {
        addedFrameworkNames = names;
    }

    void loadIncludesExcludes(IncludeExcludeVisualizer v) {
        Set<File> roots = new HashSet<File>();
        for (DefaultTableModel model : new DefaultTableModel[] {SOURCE_ROOTS_MODEL, TEST_ROOTS_MODEL}) {
            for (Object row : model.getDataVector()) {
                File d = (File) ((Vector) row).elementAt(0);
                if (d.isDirectory()) {
                    roots.add(d);
                }
            }
        }
        try {
            String webDocRoot = WEB_DOCBASE_DIR_MODEL.getText(0, WEB_DOCBASE_DIR_MODEL.getLength());
            File d = project.getAntProjectHelper().resolveFile(webDocRoot);
            if (d.isDirectory()) {
                roots.add(d);
            }
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
        v.setRoots(roots.toArray(new File[0]));
        v.setIncludePattern(includes);
        v.setExcludePattern(excludes);
    }

    void storeIncludesExcludes(IncludeExcludeVisualizer v) {
        includes = v.getIncludePattern();
        excludes = v.getExcludePattern();
    }

    @NonNull
    Iterable<? extends ActionListener> getOptionListeners() {
        return optionListeners;
    }

    void addOptionListener(@NonNull final ActionListener al) {
        Parameters.notNull("al", al);   //NOI18N
        optionListeners.add(al);
    }

    void removeOptionListener(@NonNull final ActionListener al) {
        Parameters.notNull("al", al);   //NOI18N
        optionListeners.remove(al);
    }

    @Messages({
        "WebProjectProperties.label.adding.project.frameworks=Adding project frameworks",
        "WebProjectProperties.label.saving.project.frameworks=Saving project frameworks"
    })
    private void handleExtenders(final List newExtenders, final List<WebModuleExtender> existingExtenders) {
        if (newExtenders != null && !newExtenders.isEmpty()) {
            // in case that new extenders should be included
            RP.post(new Runnable() {
                @Override
                public void run() {
                    // it mostly results into lenghty opperation, show progress dialog
                    ProgressUtils.showProgressDialogAndRun(new Runnable() {
                        @Override
                        public void run() {
                            // include newly added extenders into webmodule
                            for (int i = 0; i < newExtenders.size(); i++) {
                                ((WebModuleExtender) newExtenders.get(i)).extend(project.getAPIWebModule());
                            }

                            // save all already included extenders
                            saveExistingExtenders(existingExtenders);

                            newExtenders.clear();
                            project.resetTemplates();
                        }
                    }, Bundle.WebProjectProperties_label_adding_project_frameworks());
                }
            });
        } else if (existingExtenders != null && !existingExtenders.isEmpty()) {
            // in case that webModule contains some extenders which should be saved
            RP.post(new Runnable() {

                @Override
                public void run() {
                    final FutureTask<Void> future = new FutureTask<Void>(new Callable<Void>() {
                        @Override
                        public Void call() throws Exception {
                            // save all already included extenders
                            saveExistingExtenders(existingExtenders);
                            project.resetTemplates();
                            return null;
                        }
                    });
                    try {
                        // start the extenders saving task
                        RP.post(future);
                        // When the task doesn't finish shortly, run it with progress dialog to inform user
                        // that lenghty opperation is happening. BTW, initial waiting time is used to prevent
                        // dialogs flickering.
                        future.get(300, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (ExecutionException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (TimeoutException ex) {
                        // End of the 300ms period, continue in processing but display progress dialog
                        ProgressUtils.showProgressDialogAndRun(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    // Wait for finishing of the future
                                    future.get();
                                } catch (InterruptedException ex) {
                                    Exceptions.printStackTrace(ex);
                                } catch (ExecutionException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }, Bundle.WebProjectProperties_label_saving_project_frameworks());
                    }
                }
            });
        }
    }

    private void saveExistingExtenders(List<WebModuleExtender> existingExtenders) {
        if (existingExtenders != null) {
            for (WebModuleExtender webModuleExtender : existingExtenders) {
                if (webModuleExtender instanceof WebModuleExtender.Savable) {
                    ((WebModuleExtender.Savable) webModuleExtender).save(project.getAPIWebModule());
                }
            }
        }
    }

    private static class CallbackImpl implements J2EEProjectProperties.Callback {

        private WebProject project;

        public CallbackImpl(WebProject project) {
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
