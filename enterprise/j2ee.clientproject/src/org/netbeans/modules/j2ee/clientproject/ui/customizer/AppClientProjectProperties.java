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

package org.netbeans.modules.j2ee.clientproject.ui.customizer;

import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.j2ee.clientproject.AppClientProjectType;
import org.netbeans.modules.j2ee.clientproject.Utils;
import org.netbeans.modules.j2ee.clientproject.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.javaee.project.api.ui.utils.J2eePlatformUiSupport;
import org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.customizer.SourceRootsUi;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.javaee.project.api.ant.ui.customizer.LicensePanelSupport;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.customizer.ClassPathListCellRenderer;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.spi.java.project.support.ui.IncludeExcludeVisualizer;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.modules.SpecificationVersion;
import org.openide.util.*;

/**
 * @author Petr Hrebejk
 */
public final class AppClientProjectProperties {
    
    //Hotfix of the issue #70058
    //Should be removed when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
    private static final Integer BOOLEAN_KIND_TF = 0;
    private static final Integer BOOLEAN_KIND_YN = 1;
    private static final Integer BOOLEAN_KIND_ED = 2;
    private Integer javacDebugBooleanKind;
    private Integer javadocPreviewBooleanKind;
    
    // Special properties of the project
    public static final String CAR_PROJECT_NAME = "j2ee.clientproject.name"; // NOI18N
    public static final String JAVA_PLATFORM = "platform.active"; // NOI18N
    public static final String J2EE_PLATFORM = "j2ee.platform"; // NOI18N
    
    public static final String SOURCE_ROOT = "source.root"; // NOI18N
    public static final String SOURCE_ENCODING="source.encoding"; // NOI18N
    public static final String BUILD_FILE = "buildfile"; // NOI18N
    public static final String META_INF = "meta.inf"; // NOI18N
    public static final String SRC_DIR = "src.dir"; // NOI18N
    public static final String LIBRARIES_DIR = "lib.dir"; //NOI18N
    public static final String RESOURCE_DIR = "resource.dir"; // NOI18N
    public static final String DIST_EAR_JAR = "dist.ear.jar"; //NOI18N
    public static final String JAR_NAME = "jar.name"; // NOI18N
    public static final String BUILD_EAR_CLASSES_DIR = "build.ear.classes.dir"; // NOI18N
    public static final String J2EE_SERVER_INSTANCE = J2EEProjectProperties.J2EE_SERVER_INSTANCE;
    public static final String J2EE_SERVER_TYPE = J2EEProjectProperties.J2EE_SERVER_TYPE;
    public static final String J2EE_PLATFORM_CLASSPATH = "j2ee.platform.classpath"; //NOI18N
    
    // Properties stored in the PROJECT.PROPERTIES    
    /** root of external web module sources (full path), ".." if the sources are within project folder */
    public static final String DIST_DIR = "dist.dir"; // NOI18N
    public static final String DIST_JAR = "dist.jar"; // NOI18N
    public static final String RUN_JVM_ARGS = "run.jvmargs"; // NOI18N
    public static final String RUN_WORK_DIR = "work.dir"; // NOI18N
    public static final String DEBUG_CLASSPATH = "debug.classpath"; // NOI18N
    public static final String JAR_COMPRESS = "jar.compress"; // NOI18N
    public static final String MAIN_CLASS = "main.class"; // NOI18N
    public static final String JAVAC_SOURCE = "javac.source"; // NOI18N
    public static final String JAVAC_TARGET = "javac.target"; // NOI18N
    public static final String JAVAC_DEBUG = "javac.debug"; // NOI18N
    public static final String JAVAC_DEPRECATION = "javac.deprecation"; // NOI18N
    public static final String JAVAC_COMPILER_ARG = "javac.compilerargs";    //NOI18N
    public static final String BUILD_DIR = "build.dir"; // NOI18N
    public static final String BUILD_TEST_RESULTS_DIR = "build.test.results.dir"; // NOI18N
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"; // NOI18N
    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir"; // NOI18N
    public static final String NO_DEPENDENCIES="no.dependencies"; // NOI18N
    public static final String DEBUG_TEST_CLASSPATH = "debug.test.classpath"; // NOI18N
    
    
    public static final String JAVADOC_PRIVATE="javadoc.private"; // NOI18N
    public static final String JAVADOC_NO_TREE="javadoc.notree"; // NOI18N
    public static final String JAVADOC_USE="javadoc.use"; // NOI18N
    public static final String JAVADOC_NO_NAVBAR="javadoc.nonavbar"; // NOI18N
    public static final String JAVADOC_NO_INDEX="javadoc.noindex"; // NOI18N
    public static final String JAVADOC_SPLIT_INDEX="javadoc.splitindex"; // NOI18N
    public static final String JAVADOC_AUTHOR="javadoc.author"; // NOI18N
    public static final String JAVADOC_VERSION="javadoc.version"; // NOI18N
    public static final String JAVADOC_WINDOW_TITLE="javadoc.windowtitle"; // NOI18N
    public static final String JAVADOC_ENCODING="javadoc.encoding"; // NOI18N
    public static final String JAVADOC_ADDITIONALPARAM="javadoc.additionalparam"; // NOI18N
    
    public static final String CLIENT_NAME = "j2ee.clientName"; // NOI18N
    
    // Properties stored in the PRIVATE.PROPERTIES
    public static final String APPLICATION_ARGS = "application.args"; // NOI18N
    public static final String JAVADOC_PREVIEW="javadoc.preview"; // NOI18N
    public static final String DEPLOY_ANT_PROPS_FILE = "deploy.ant.properties.file"; //NOI18N
    
    public static final String ANT_DEPLOY_BUILD_SCRIPT = "nbproject/ant-deploy.xml"; // NOI18N

    public static final String APPCLIENT_MAINCLASS_ARGS = "j2ee.appclient.mainclass.args"; // NOI18N
    
    public static final String APPCLIENT_TOOL_RUNTIME = "j2ee.appclient.tool.runtime"; // NOI18N
    public static final String APPCLIENT_TOOL_MAINCLASS = "j2ee.appclient.tool.mainclass"; // NOI18N
    public static final String APPCLIENT_TOOL_JVMOPTS = "j2ee.appclient.tool.jvmoptions";  // NOI18N
    public static final String APPCLIENT_TOOL_ARGS = "j2ee.appclient.tool.args"; // NOI18N
    
    public static final String APPCLIENT_TOOL_CLIENT_JAR = "wa.copy.client.jar.from"; // NOI18N
    
    /**
     * "API" contract between Application Client and Glassfish plugin's
     * J2eePlatformImpl implementation.
     */
    public static final String J2EE_PLATFORM_APPCLIENT_ARGS = "j2ee.appclient.args"; // NOI18N
    
    ClassPathSupport cs;
    
    
    // SOURCE ROOTS
    // public static final String SOURCE_ROOTS = "__virtual_source_roots__";   //NOI18N
    // public static final String TEST_ROOTS = "__virtual_test_roots__"; // NOI18N
                        
    // MODELS FOR VISUAL CONTROLS
    
    // CustomizerSources
    DefaultTableModel SOURCE_ROOTS_MODEL;
    DefaultTableModel TEST_ROOTS_MODEL;
    Document META_INF_MODEL;
    ComboBoxModel JAVAC_SOURCE_MODEL;
     
    // CustomizerLibraries
    ClassPathTableModel JAVAC_CLASSPATH_MODEL;
    DefaultListModel JAVAC_TEST_CLASSPATH_MODEL;
    DefaultListModel RUN_CLASSPATH_MODEL;
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
    ButtonModel ENABLE_ANNOTATION_PROCESSING_MODEL;
    ButtonModel ENABLE_ANNOTATION_PROCESSING_IN_EDITOR_MODEL;
    DefaultListModel ANNOTATION_PROCESSORS_MODEL;
    
    // CustomizerCompileTest
                
    // CustomizerJar
    Document DIST_JAR_MODEL; 
    Document BUILD_CLASSES_EXCLUDES_MODEL; 
    ButtonModel JAR_COMPRESS_MODEL;
                
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
    Document MAIN_CLASS_MODEL;
    Document APPLICATION_ARGS_MODEL;
    Document RUN_JVM_ARGS_MODEL;
    Document RUN_WORK_DIR_MODEL;
    
    ComboBoxModel J2EE_SERVER_INSTANCE_MODEL;
    ComboBoxModel J2EE_PLATFORM_MODEL;
    
    //customizer license headers
    LicensePanelSupport LICENSE_SUPPORT;

    // CustomizerRunTest

    // Private fields ----------------------------------------------------------    
    private final AppClientProject project;
    private final UpdateHelper updateHelper;
    private final PropertyEvaluator evaluator;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFileHelper;
    
    private final StoreGroup privateGroup; 
    private final StoreGroup projectGroup;
    
    private Map<String,String> additionalProperties;
    
    private String includes, excludes;

    public static final String JAVA_SOURCE_BASED = "java.source.based";

    
    private final List<ActionListener> optionListeners = new CopyOnWriteArrayList<ActionListener>();

    AppClientProject getProject() {
        return project;
    }
    
    /** Creates a new instance of J2SEUIProperties and initializes them */
    AppClientProjectProperties( AppClientProject project, UpdateHelper updateHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper, GeneratedFilesHelper genFileHelper ) {
        this.project = project;
        this.updateHelper  = updateHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        this.genFileHelper = genFileHelper;
        this.cs = new ClassPathSupport(evaluator, refHelper, updateHelper.getAntProjectHelper(), updateHelper,
                new ClassPathSupportCallbackImpl(updateHelper.getAntProjectHelper()));
                
        privateGroup = new StoreGroup();
        projectGroup = new StoreGroup();
        
        additionalProperties = new HashMap<String,String>();
        
        init(); // Load known properties        
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
        META_INF_MODEL = projectGroup.createStringDocument( evaluator, META_INF );
                
        // CustomizerLibraries
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );                
        EditableProperties privateProperties = updateHelper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
        
        JAVAC_CLASSPATH_MODEL = ClassPathTableModel.createTableModel( cs.itemsIterator(projectProperties.get( ProjectProperties.JAVAC_CLASSPATH ), ClassPathSupportCallbackImpl.ELEMENT_INCLUDED_LIBRARIES ) );
        String processorPath = projectProperties.get(ProjectProperties.JAVAC_PROCESSORPATH);
        processorPath = processorPath == null ? "${javac.classpath}" : processorPath;
        JAVAC_PROCESSORPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(processorPath));
        JAVAC_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator(projectProperties.get( ProjectProperties.JAVAC_TEST_CLASSPATH ), null ) );
        RUN_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator(projectProperties.get( ProjectProperties.RUN_CLASSPATH ), null ) );
        RUN_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator(projectProperties.get( ProjectProperties.RUN_TEST_CLASSPATH ), null ) );
        ENDORSED_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator(projectProperties.get( ProjectProperties.ENDORSED_CLASSPATH ), null ) );
        PLATFORM_MODEL = PlatformUiSupport.createPlatformComboBoxModel (evaluator.getProperty(JAVA_PLATFORM));
        PLATFORM_LIST_RENDERER = PlatformUiSupport.createPlatformListCellRenderer();
        SpecificationVersion minimalSourceLevel = null;
        Profile profile = Profile.fromPropertiesString(evaluator.getProperty(J2EE_PLATFORM));
        switch (profile) {
            case JAKARTA_EE_11_FULL:
                minimalSourceLevel = new SpecificationVersion("21");
                break;
            case JAKARTA_EE_9_1_FULL:
            case JAKARTA_EE_10_FULL:
                minimalSourceLevel = new SpecificationVersion("11");
                break;
            case JAKARTA_EE_8_FULL:
            case JAVA_EE_8_FULL:
            case JAKARTA_EE_9_FULL:
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
        JAVAC_SOURCE_MODEL = PlatformUiSupport.createSourceLevelComboBoxModel(PLATFORM_MODEL, evaluator.getProperty(JAVAC_SOURCE), evaluator.getProperty(JAVAC_TARGET), minimalSourceLevel);
        JAVAC_SOURCE_RENDERER = PlatformUiSupport.createSourceLevelListCellRenderer ();
        SHARED_LIBRARIES_MODEL = new PlainDocument(); 
        try {
            SHARED_LIBRARIES_MODEL.insertString(0, project.getAntProjectHelper().getLibrariesLocation(), null);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
                
        // CustomizerCompile
        JAVAC_DEPRECATION_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVAC_DEPRECATION );
                
        //Hotfix of the issue #70058
        //Should use the StoreGroup when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
        Integer[] kind = new Integer[1];
        JAVAC_DEBUG_MODEL = createToggleButtonModel( evaluator, JAVAC_DEBUG, kind);
        javacDebugBooleanKind = kind[0];
        
        NO_DEPENDENCIES_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, NO_DEPENDENCIES );
        ENABLE_ANNOTATION_PROCESSING_MODEL =projectGroup.createToggleButtonModel(evaluator, ProjectProperties.ANNOTATION_PROCESSING_ENABLED);
        ENABLE_ANNOTATION_PROCESSING_IN_EDITOR_MODEL = projectGroup.createToggleButtonModel(evaluator, ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR);
        String annotationProcessors = projectProperties.get(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST);
        if (annotationProcessors == null)
            annotationProcessors = ""; //NOI18N
        ANNOTATION_PROCESSORS_MODEL = ClassPathUiSupport.createListModel(
                (annotationProcessors.length() > 0 ? Arrays.asList(annotationProcessors.split(",")) : Collections.emptyList()).iterator()); //NOI18N
        JAVAC_COMPILER_ARG_MODEL = projectGroup.createStringDocument( evaluator, JAVAC_COMPILER_ARG );

        // CustomizerJar
        DIST_JAR_MODEL = projectGroup.createStringDocument( evaluator, DIST_JAR );
        BUILD_CLASSES_EXCLUDES_MODEL = projectGroup.createStringDocument( evaluator, BUILD_CLASSES_EXCLUDES );
        JAR_COMPRESS_MODEL = projectGroup.createToggleButtonModel( evaluator, JAR_COMPRESS );
        
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
        //Hotfix of the issue #70058
        //Should use the StoreGroup when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel        
        JAVADOC_PREVIEW_MODEL = createToggleButtonModel ( evaluator, JAVADOC_PREVIEW, kind);
        javadocPreviewBooleanKind = kind[0];
        
        JAVADOC_ADDITIONALPARAM_MODEL = projectGroup.createStringDocument( evaluator, JAVADOC_ADDITIONALPARAM );
        // CustomizerRun
        MAIN_CLASS_MODEL = projectGroup.createStringDocument( evaluator, MAIN_CLASS ); 
        APPLICATION_ARGS_MODEL = privateGroup.createStringDocument( evaluator, APPLICATION_ARGS );
        RUN_JVM_ARGS_MODEL = projectGroup.createStringDocument( evaluator, RUN_JVM_ARGS );
        RUN_WORK_DIR_MODEL = privateGroup.createStringDocument( evaluator, RUN_WORK_DIR );

        J2EE_SERVER_INSTANCE_MODEL = J2eePlatformUiSupport.createPlatformComboBoxModel(
                privateProperties.getProperty(J2EE_SERVER_INSTANCE),
                profile,
                J2eeModule.Type.CAR);
        J2EE_PLATFORM_MODEL = J2eePlatformUiSupport.createSpecVersionComboBoxModel(profile);
        
        LICENSE_SUPPORT = new LicensePanelSupport(evaluator, project.getAntProjectHelper(),
                projectProperties.get(LicensePanelSupport.LICENSE_PATH),
                projectProperties.get(LicensePanelSupport.LICENSE_NAME));
    }
    
    public void save() {
        try {                        
            LICENSE_SUPPORT.saveLicenseFile();
            // Store properties 
            Boolean result = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Boolean>() {
                @Override
                public Boolean run() throws IOException {
                    storeProperties();
                    return Boolean.TRUE;
                }
            });
            // and save the project
            if (result == Boolean.TRUE) {
                project.setProjectPropertiesSave(true);
                try {
                    ProjectManager.getDefault().saveProject(project);
                } finally {
                    project.setProjectPropertiesSave(false);
                }
            }
        } 
        catch (MutexException e) {
            Exceptions.printStackTrace((IOException) e.getException());
        }
        catch ( IOException ex ) {
            Exceptions.printStackTrace(ex);
        }
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
        List<ClassPathSupport.Item> javaClasspathList = ClassPathUiSupport.getList(JAVAC_CLASSPATH_MODEL.getDefaultListModel());
        String[] javac_cp = cs.encodeToStrings( javaClasspathList, ClassPathSupportCallbackImpl.ELEMENT_INCLUDED_LIBRARIES  );
        String[] javac_pp = cs.encodeToStrings( ClassPathUiSupport.getList( JAVAC_PROCESSORPATH_MODEL ) );
        String[] javac_test_cp = cs.encodeToStrings( ClassPathUiSupport.getList( JAVAC_TEST_CLASSPATH_MODEL ), null );
        String[] run_test_cp = cs.encodeToStrings( ClassPathUiSupport.getList( RUN_TEST_CLASSPATH_MODEL ), null );
        String[] run_cp = cs.encodeToStrings( ClassPathUiSupport.getList( RUN_CLASSPATH_MODEL ), null );
        String[] endorsed_cp = cs.encodeToStrings( ClassPathUiSupport.getList( ENDORSED_CLASSPATH_MODEL ), null );
                
        // Store source roots
        storeRoots( project.getSourceRoots(), SOURCE_ROOTS_MODEL );
        storeRoots( project.getTestSourceRoots(), TEST_ROOTS_MODEL );
                
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

        //Hotfix of the issue #70058
        //Should use the StoreGroup when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
        //Save javac.debug
        privateProperties.setProperty(JAVAC_DEBUG, encodeBoolean (JAVAC_DEBUG_MODEL.isSelected(), javacDebugBooleanKind));
                
        //Hotfix of the issue #70058
        //Should use the StoreGroup when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
        //Save javadoc.preview
        privateProperties.setProperty(JAVADOC_PREVIEW, encodeBoolean (JAVADOC_PREVIEW_MODEL.isSelected(), javadocPreviewBooleanKind));
                
        // Save all paths
        projectProperties.setProperty( ProjectProperties.JAVAC_CLASSPATH, javac_cp );
        projectProperties.setProperty( ProjectProperties.JAVAC_PROCESSORPATH, javac_pp );
        projectProperties.setProperty( ProjectProperties.JAVAC_TEST_CLASSPATH, javac_test_cp );
        projectProperties.setProperty( ProjectProperties.RUN_CLASSPATH, run_cp );
        projectProperties.setProperty( ProjectProperties.RUN_TEST_CLASSPATH, run_test_cp );
        projectProperties.setProperty( ProjectProperties.ENDORSED_CLASSPATH, endorsed_cp );
        
        //Handle platform selection and javac.source javac.target properties
        PlatformUiSupport.storePlatform (projectProperties, updateHelper, AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, PLATFORM_MODEL.getSelectedItem(), JAVAC_SOURCE_MODEL.getSelectedItem());
                                
        // Handle other special cases
        if ( NO_DEPENDENCIES_MODEL.isSelected() ) { // NOI18N
            projectProperties.remove( NO_DEPENDENCIES ); // Remove the property completely if not set
        }

        if ( getDocumentText( RUN_WORK_DIR_MODEL ).trim().equals( "" ) ) { // NOI18N
            privateProperties.remove( RUN_WORK_DIR ); // Remove the property completely if not set
        }
        
        if (getDocumentText(MAIN_CLASS_MODEL).trim().equals("")) { // NOI18N
            projectProperties.remove(MAIN_CLASS); // Remove the property completely if not set
        }

        if (J2EE_SERVER_INSTANCE_MODEL.getSelectedItem() != null) {
            final String instanceId = J2eePlatformUiSupport.getServerInstanceID(J2EE_SERVER_INSTANCE_MODEL.getSelectedItem());
            Profile profile = Profile.fromPropertiesString(evaluator.getProperty(J2EE_PLATFORM));
            J2EEProjectProperties.updateServerProperties(projectProperties, privateProperties, instanceId,
                    cs, javaClasspathList,
                    new CallbackImpl(project), project,
                    profile, J2eeModule.Type.CAR);
            generateExtraServerProperty(privateProperties);
            updateAppClientServerProperties(instanceId, projectProperties, privateProperties);
        }

        projectProperties.putAll(additionalProperties);

        projectProperties.put(ProjectProperties.INCLUDES, includes);
        projectProperties.put(ProjectProperties.EXCLUDES, excludes);
        
        StringBuilder sb = new StringBuilder();
        for (Enumeration<String> elements = ANNOTATION_PROCESSORS_MODEL.elements(); elements.hasMoreElements();) {
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
        
        String value = additionalProperties.get(SOURCE_ENCODING);
        if (value != null) {
            try {
                FileEncodingQuery.setDefaultEncoding(Charset.forName(value));
            } catch (UnsupportedCharsetException e) {
                //When the encoding is not supported by JVM do not set it as default
            }
        }
        
    }
  
    private static String getDocumentText( Document document ) {
        try {
            return document.getText( 0, document.getLength() );
        }
        catch( BadLocationException e ) {
            return ""; // NOI18N
        }
    }
    
    /** Finds out what are new and removed project dependencies and 
     * applies the info to the project
     */
    private void resolveProjectDependencies() {
            
        // Create a set of old and new artifacts.
        Set<ClassPathSupport.Item> oldArtifacts = new HashSet<ClassPathSupport.Item>();
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        oldArtifacts.addAll( cs.itemsList(projectProperties.get( ProjectProperties.JAVAC_CLASSPATH ), ClassPathSupportCallbackImpl.ELEMENT_INCLUDED_LIBRARIES ) );
        oldArtifacts.addAll( cs.itemsList( projectProperties.get( ProjectProperties.JAVAC_PROCESSORPATH ) ) );
        oldArtifacts.addAll( cs.itemsList(projectProperties.get( ProjectProperties.JAVAC_TEST_CLASSPATH ), null ) );
        oldArtifacts.addAll( cs.itemsList(projectProperties.get( ProjectProperties.RUN_CLASSPATH ), null ) );
        oldArtifacts.addAll( cs.itemsList(projectProperties.get( ProjectProperties.RUN_TEST_CLASSPATH ), null ) );
        oldArtifacts.addAll( cs.itemsList(projectProperties.get( ProjectProperties.ENDORSED_CLASSPATH ), null ) );
                   
        Set<ClassPathSupport.Item> newArtifacts = new HashSet<ClassPathSupport.Item>();
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_CLASSPATH_MODEL.getDefaultListModel() ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_PROCESSORPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_TEST_CLASSPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( RUN_CLASSPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( RUN_TEST_CLASSPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( ENDORSED_CLASSPATH_MODEL ) );
                
        // Create set of removed artifacts and remove them
        Set<ClassPathSupport.Item> removed = new HashSet<ClassPathSupport.Item>( oldArtifacts );
        removed.removeAll( newArtifacts );
        Set<ClassPathSupport.Item> added = new HashSet<ClassPathSupport.Item>(newArtifacts);
        added.removeAll(oldArtifacts);
        
        // 1. first remove all project references. The method will modify
        // project property files, so it must be done separately
        for (Iterator<ClassPathSupport.Item> it = removed.iterator(); it.hasNext(); ) {
            ClassPathSupport.Item item = it.next();
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
        
        for (Iterator<ClassPathSupport.Item> it = removed.iterator(); it.hasNext(); ) {
            ClassPathSupport.Item item = it.next();
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
            File f = (File) ((Vector)data.elementAt(i)).elementAt(0);
            rootURLs[i] = Utils.getRootURL(f,null);            
            rootLabels[i] = (String) ((Vector)data.elementAt(i)).elementAt(1);
        }
        roots.putRoots(rootURLs,rootLabels);
    }
    
    /* This is used by CustomizerWSServiceHost */
    public void putAdditionalProperty(String propertyName, String propertyValue) {
        additionalProperties.put(propertyName, propertyValue);
    }
    
    private static boolean showModifiedMessage (String title) {
        String message = NbBundle.getMessage(AppClientProjectProperties.class,"TXT_Regenerate");
        JButton regenerateButton = new JButton (NbBundle.getMessage(AppClientProjectProperties.class,"CTL_RegenerateButton"));
        regenerateButton.setDefaultCapable(true);
        regenerateButton.getAccessibleContext().setAccessibleDescription (NbBundle.getMessage(AppClientProjectProperties.class,"AD_RegenerateButton"));
        NotifyDescriptor d = new NotifyDescriptor.Message (message, NotifyDescriptor.WARNING_MESSAGE);
        d.setTitle(title);
        d.setOptionType(NotifyDescriptor.OK_CANCEL_OPTION);
        d.setOptions(new Object[] {regenerateButton, NotifyDescriptor.CANCEL_OPTION});        
        return DialogDisplayer.getDefault().notify(d) == regenerateButton;
    }
    
    //Hotfix of the issue #70058
    //Should be removed when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
    private static String encodeBoolean (boolean value, Integer kind) {
        if ( Objects.equals(kind, BOOLEAN_KIND_ED) ) {
            return value ? "on" : "off"; // NOI18N
        }
        else if ( Objects.equals(kind, BOOLEAN_KIND_YN) ) { // NOI18N
            return value ? "yes" : "no"; // NOI18N
        }
        else {
            return value ? "true" : "false"; // NOI18N
        }
    }
    
    //Hotfix of the issue #70058
    //Should be removed when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
    private static JToggleButton.ToggleButtonModel createToggleButtonModel (final PropertyEvaluator evaluator, final String propName, Integer[] kind) {
        assert evaluator != null && propName != null && kind != null && kind.length == 1;
        String value = evaluator.getProperty( propName );
        boolean isSelected = false;
        if (value == null) {
            isSelected = true;
        }
        else {
           String lowercaseValue = value.toLowerCase();
           if ( lowercaseValue.equals( "yes" ) || lowercaseValue.equals( "no" ) ) { // NOI18N
               kind[0] = BOOLEAN_KIND_YN;
           }
           else if ( lowercaseValue.equals( "on" ) || lowercaseValue.equals( "off" ) ) { // NOI18N
               kind[0] = BOOLEAN_KIND_ED;
           }
           else {
               kind[0] = BOOLEAN_KIND_TF;
           }

           if ( lowercaseValue.equals( "true") || // NOI18N
                lowercaseValue.equals( "yes") ||  // NOI18N
                lowercaseValue.equals( "on") ) {  // NOI18N
               isSelected = true;                   
           } 
        }
        JToggleButton.ToggleButtonModel bm = new JToggleButton.ToggleButtonModel();
        bm.setSelected(isSelected );
        return bm;
    }
    
    public static void setServerInstance(final AppClientProject project, final AntProjectHelper helper, final String serverInstanceID) {
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    Profile profile = Profile.fromPropertiesString(project.evaluator().getProperty(J2EE_PLATFORM));
                    J2EEProjectProperties.updateServerProperties(projectProps, privateProps, serverInstanceID,
                            null, null, new CallbackImpl(project), project,
                            profile, J2eeModule.Type.CAR);
                    generateExtraServerProperty(privateProps);
                    updateAppClientServerProperties(serverInstanceID, projectProps, privateProps);
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });
    }

    public static void generateExtraServerProperty(EditableProperties props) {
        // TODO: this should come from server instead of hacking it here.
        // Good enough for now though.
        String val = props.getProperty("j2ee.appclient.tool.jvmoptions");
        if (val != null && val.endsWith(",client=jar=")) {
            props.setProperty("j2ee.appclient.tool.jvmoptions.class", val.substring(0, val.length()-4)+"class=");
        }
    }

    private static void updateAppClientServerProperties(String newServInstID,
            EditableProperties projectProps, EditableProperties privateProps) {

        assert newServInstID != null : "Server isntance id to set can't be null"; // NOI18N

        J2eePlatform j2eePlatform = Deployment.getDefault().getJ2eePlatform(newServInstID);
        if (j2eePlatform == null) {
            privateProps.remove(AppClientProjectProperties.APPCLIENT_TOOL_CLIENT_JAR);
            return;
        }

        // XXX this seems to be used in runtime only so, not part of sharable server
        // set j2ee.appclient environment
        File[] accrt = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_APP_CLIENT_RUNTIME);
        Map<String, String> roots = J2EEProjectProperties.extractPlatformLibrariesRoot(j2eePlatform);
        privateProps.setProperty(APPCLIENT_TOOL_RUNTIME, J2EEProjectProperties.toClasspathString(accrt, roots));

        String jvmOpts = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_JVM_OPTS);
        if (jvmOpts != null) {
            privateProps.setProperty(APPCLIENT_TOOL_JVMOPTS, jvmOpts);
        }
        String acMain = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_MAIN_CLASS);
        if (acMain != null) {
            privateProps.setProperty(APPCLIENT_TOOL_MAINCLASS, acMain);
        }
        String args = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2EE_PLATFORM_APPCLIENT_ARGS);
        if (args != null) {
            privateProps.setProperty(AppClientProjectProperties.APPCLIENT_TOOL_ARGS, args);
        }  else {
            privateProps.remove(AppClientProjectProperties.APPCLIENT_TOOL_ARGS);
        }

        String mainClassArgs = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_MAIN_CLASS_ARGS);
        if (mainClassArgs != null && !mainClassArgs.equals("")) {
            if (projectProps.getProperty(CLIENT_NAME) != null) {
                projectProps.remove(CLIENT_NAME);
            }
            projectProps.put(APPCLIENT_MAINCLASS_ARGS, mainClassArgs);
        } else if ((j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, CLIENT_NAME)) != null) {
            if (projectProps.getProperty(APPCLIENT_MAINCLASS_ARGS) != null) {
                projectProps.remove(APPCLIENT_MAINCLASS_ARGS);
            }
            projectProps.put(CLIENT_NAME, mainClassArgs);
        }

        // WORKAROUND for --retrieve option in asadmin deploy command
        // works only for local domains
        // see also http://www.netbeans.org/issues/show_bug.cgi?id=82929
        String copyProperty = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_CLIENT_JAR_LOCATION);
        if (copyProperty != null) {
            privateProps.setProperty(AppClientProjectProperties.APPCLIENT_TOOL_CLIENT_JAR, copyProperty);
        } else {
            privateProps.remove(AppClientProjectProperties.APPCLIENT_TOOL_CLIENT_JAR);
        }

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
     
    private static class CallbackImpl implements J2EEProjectProperties.Callback {

        private AppClientProject project;

        public CallbackImpl(AppClientProject project) {
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
