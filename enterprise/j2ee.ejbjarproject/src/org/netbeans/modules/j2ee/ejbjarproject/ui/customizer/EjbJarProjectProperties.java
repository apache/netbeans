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

package org.netbeans.modules.j2ee.ejbjarproject.ui.customizer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.modules.java.api.common.project.ui.customizer.SourceRootsUi;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
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
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
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
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.MutexException;
import org.openide.util.Mutex;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.javaee.project.api.ant.DeployOnSaveUtils;
import org.netbeans.modules.javaee.project.api.ui.utils.J2eePlatformUiSupport;
import org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.javaee.project.api.ant.ui.customizer.LicensePanelSupport;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProjectType;
import org.netbeans.modules.j2ee.ejbjarproject.Utils;
import org.netbeans.modules.j2ee.ejbjarproject.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.customizer.ClassPathListCellRenderer;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.spi.java.project.support.ui.IncludeExcludeVisualizer;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;


/** Helper class. Defines constants for properties. Knows the proper
 *  place where to store the properties.
 * 
 * @author Petr Hrebejk
 * @author Chris Webster
 * @author Andrei Badea
 */
public final class EjbJarProjectProperties {
    
    // Special properties of the project
    public static final String EJB_PROJECT_NAME = "j2ee.ejbjarproject.name"; // NOI18N
    public static final String JAVA_PLATFORM = "platform.active"; // NOI18N
    public static final String J2EE_PLATFORM = "j2ee.platform"; // NOI18N
    public static final String J2EE_DEPLOY_ON_SAVE = "j2ee.deploy.on.save"; // NOI18N
    public static final String J2EE_COMPILE_ON_SAVE = "j2ee.compile.on.save"; // NOI18N
    
    // Properties stored in the PROJECT.PROPERTIES    
    /** root of external web module sources (full path), ".." if the sources are within project folder */
    public static final String SOURCE_ROOT = "source.root"; // NOI18N
    public static final String SOURCE_ENCODING="source.encoding"; // NOI18N
    public static final String BUILD_FILE = "buildfile"; // NOI18N
    public static final String LIBRARIES_DIR = "lib.dir"; //NOI18N
    public static final String DIST_DIR = "dist.dir"; // NOI18N
    public static final String DIST_JAR = "dist.jar"; // NOI18N
    public static final String DIST_EAR_JAR = "dist.ear.jar"; //NOI18N
    public static final String DEBUG_CLASSPATH = "debug.classpath"; // NOI18N

    public static final String JAR_NAME = "jar.name"; // NOI18N
    public static final String JAR_COMPRESS = "jar.compress"; // NOI18N
    public static final String INCLUDE_JARS = "jars.in.ejbjar"; // NOI18N

    public static final String J2EE_SERVER_INSTANCE = J2EEProjectProperties.J2EE_SERVER_INSTANCE;
    public static final String J2EE_SERVER_TYPE = J2EEProjectProperties.J2EE_SERVER_TYPE;
    public static final String J2EE_PLATFORM_CLASSPATH = "j2ee.platform.classpath"; //NOI18N
    public static final String J2EE_PLATFORM_EMBEDDABLE_EJB_CLASSPATH = "j2ee.platform.embeddableejb.classpath"; //NOI18N
    public static final String JAVAC_SOURCE = "javac.source"; // NOI18N
    public static final String JAVAC_DEBUG = "javac.debug"; // NOI18N
    public static final String JAVAC_DEPRECATION = "javac.deprecation"; // NOI18N
    public static final String JAVAC_TARGET = "javac.target"; // NOI18N

    public static final String JAVAC_COMPILER_ARG = "javac.compilerargs";    //NOI18N    
    public static final String SRC_DIR = "src.dir"; // NOI18N
    public static final String TEST_SRC_DIR = "test.src.dir"; // NOI18N
    public static final String META_INF = "meta.inf"; // NOI18N
    public static final String RESOURCE_DIR = "resource.dir"; // NOI18N
    public static final String BUILD_DIR = "build.dir"; // NOI18N
    public static final String BUILD_GENERATED_DIR = "build.generated.dir"; // NOI18N
    public static final String BUILD_EAR_CLASSES_DIR = "build.ear.classes.dir"; // NOI18N
    public static final String BUILD_CLASSES_EXCLUDES = "build.classes.excludes"; // NOI18N
    
    public static final String BUILD_TEST_RESULTS_DIR = "build.test.results.dir"; // NOI18N
    public static final String DEBUG_TEST_CLASSPATH = "debug.test.classpath"; // NOI18N
    
    public static final String NO_DEPENDENCIES="no.dependencies"; //NOI18N    
    public static final String DIST_JAVADOC_DIR = "dist.javadoc.dir"; // NOI18N
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
    
    public static final String RUNMAIN_JVM_ARGS = "runmain.jvmargs"; // NOI18N
    
    public static final String META_INF_EXCLUDES="meta.inf.excludes"; // NOI18N
    
    // Properties stored in the PRIVATE.PROPERTIES
    public static final String JAVADOC_PREVIEW="javadoc.preview"; // NOI18N
    
    public static final String DEPLOY_ANT_PROPS_FILE = "deploy.ant.properties.file"; //NOI18N
    
    public static final String ANT_DEPLOY_BUILD_SCRIPT = "nbproject/ant-deploy.xml"; // NOI18N
    
    public static final String JAVA_SOURCE_BASED = "java.source.based";
    
    private static final Logger LOGGER = Logger.getLogger(EjbJarProjectProperties.class.getName());
    
    ClassPathSupport cs;    
    
    
    // SOURCE ROOTS
    // public static final String SOURCE_ROOTS = "__virtual_source_roots__";   //NOI18N
    // public static final String TEST_ROOTS = "__virtual_test_roots__";   //NOI18N
    
    // MODELS FOR VISUAL CONTROLS
    
    // CustomizerSources
    DefaultTableModel SOURCE_ROOTS_MODEL;
    DefaultTableModel TEST_ROOTS_MODEL;
    Document META_INF_MODEL;
    ComboBoxModel JAVAC_SOURCE_MODEL;
     
    // CustomizerLibraries
    ClassPathTableModel JAVAC_CLASSPATH_MODEL;
    DefaultListModel JAVAC_TEST_CLASSPATH_MODEL;
    DefaultListModel JAVAC_PROCESSORPATH_MODEL;
    
    //DefaultListModel RUN_CLASSPATH_MODEL;
    DefaultListModel RUN_TEST_CLASSPATH_MODEL;
    DefaultListModel ENDORSED_CLASSPATH_MODEL;
    ComboBoxModel PLATFORM_MODEL;
    ListCellRenderer CLASS_PATH_LIST_RENDERER;
    ListCellRenderer PLATFORM_LIST_RENDERER;
    ListCellRenderer JAVAC_SOURCE_RENDERER;
    TableCellRenderer CLASS_PATH_TABLE_ITEM_RENDERER;
    Document SHARED_LIBRARIES_MODEL;
    
    // CustomizerCompile
    ButtonModel JAVAC_DEPRECATION_MODEL; 
    ButtonModel JAVAC_DEBUG_MODEL;
    ButtonModel NO_DEPENDENCIES_MODEL;
    Document JAVAC_COMPILER_ARG_MODEL;
    ButtonModel ENABLE_ANNOTATION_PROCESSING_MODEL;
    ButtonModel ENABLE_ANNOTATION_PROCESSING_IN_EDITOR_MODEL;
    DefaultListModel ANNOTATION_PROCESSORS_MODEL;
    JToggleButton.ToggleButtonModel COMPILE_ON_SAVE_MODEL;
    
    // CustomizerCompileTest
                
    // CustomizerJar
    Document DIST_JAR_MODEL; 
    Document BUILD_CLASSES_EXCLUDES_MODEL; 
    ButtonModel JAR_COMPRESS_MODEL;
    ButtonModel INCLUDE_JARS_MODEL;
                
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
    ComboBoxModel J2EE_SERVER_INSTANCE_MODEL;
    ComboBoxModel J2EE_PLATFORM_MODEL;
    JToggleButton.ToggleButtonModel DEPLOY_ON_SAVE_MODEL;
    Document RUNMAIN_JVM_MODEL;

    //customizer license headers
    LicensePanelSupport LICENSE_SUPPORT;

    // CustomizerRunTest
    
    // Private fields ----------------------------------------------------------
    private EjbJarProject project;
    private PropertyEvaluator evaluator;
    private ReferenceHelper refHelper;
    private UpdateHelper updateHelper;
    
    private StoreGroup privateGroup; 
    private StoreGroup projectGroup;
    
    private Map<String,String> additionalProperties;
    
    private String includes, excludes;
    
    //Hotfix of the issue #70058 (copied from J2seProjectProperties)
    //Should be removed when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
    private static final Integer BOOLEAN_KIND_TF = 0;
    private static final Integer BOOLEAN_KIND_YN = 1;
    private static final Integer BOOLEAN_KIND_ED = 2;

    private final List<ActionListener> optionListeners = new CopyOnWriteArrayList<ActionListener>();

    EjbJarProject getProject() {
        return project;
    }

    /** Creates a new instance of EjbJarProjectProperties and initializes them */
    EjbJarProjectProperties(EjbJarProject project, UpdateHelper updateHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper ) {
        this.project = project;
        this.updateHelper = updateHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        
        cs = new ClassPathSupport( evaluator, refHelper, updateHelper.getAntProjectHelper(), 
                updateHelper, new ClassPathSupportCallbackImpl(project.getAntProjectHelper()));
        
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
        
        JAVAC_CLASSPATH_MODEL = ClassPathTableModel.createTableModel( cs.itemsIterator( projectProperties.get(ProjectProperties.JAVAC_CLASSPATH), ClassPathSupportCallbackImpl.ELEMENT_INCLUDED_LIBRARIES  ) );
        String processorPath = projectProperties.get(ProjectProperties.JAVAC_PROCESSORPATH);
        processorPath = processorPath == null ? "${javac.classpath}" : processorPath;
        JAVAC_PROCESSORPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(processorPath));
        JAVAC_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator( projectProperties.get(ProjectProperties.JAVAC_TEST_CLASSPATH), null  ) );
        RUN_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator( projectProperties.get(ProjectProperties.RUN_TEST_CLASSPATH), null  ) );
        ENDORSED_CLASSPATH_MODEL = ClassPathUiSupport.createListModel( cs.itemsIterator( projectProperties.get(ProjectProperties.ENDORSED_CLASSPATH), null  ) );
        PLATFORM_MODEL = PlatformUiSupport.createPlatformComboBoxModel (evaluator.getProperty(JAVA_PLATFORM));
        PLATFORM_LIST_RENDERER = PlatformUiSupport.createPlatformListCellRenderer();
        SpecificationVersion minimalSourceLevel = null;
        Profile profile = Profile.fromPropertiesString(evaluator.getProperty(J2EE_PLATFORM));
        if (profile != null && profile.isFullProfile()) {
            if (profile.isAtLeast(Profile.JAKARTA_EE_11_FULL)) {
                minimalSourceLevel = new SpecificationVersion("21");
            } else if (profile.isAtLeast(Profile.JAKARTA_EE_9_1_FULL)) {
                minimalSourceLevel = new SpecificationVersion("11");
            } else if (profile.isAtLeast(Profile.JAVA_EE_8_FULL)) {
                minimalSourceLevel = new SpecificationVersion("1.8");
            } else if (Profile.JAVA_EE_7_FULL.equals(profile)) {
                minimalSourceLevel = new SpecificationVersion("1.7");
            } else if (Profile.JAVA_EE_6_FULL.equals(profile)) {
                minimalSourceLevel = new SpecificationVersion("1.6");
            } else if (Profile.JAVA_EE_5.equals(profile)) {
                minimalSourceLevel = new SpecificationVersion("1.5");
            }
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
        
        // CustomizerJar
        DIST_JAR_MODEL = projectGroup.createStringDocument( evaluator, DIST_JAR );
        BUILD_CLASSES_EXCLUDES_MODEL = projectGroup.createStringDocument( evaluator, BUILD_CLASSES_EXCLUDES );
        JAR_COMPRESS_MODEL = projectGroup.createToggleButtonModel( evaluator, JAR_COMPRESS );
        INCLUDE_JARS_MODEL = projectGroup.createToggleButtonModel( evaluator, INCLUDE_JARS );
        
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
        J2EE_SERVER_INSTANCE_MODEL = J2eePlatformUiSupport.createPlatformComboBoxModel( 
            privateProperties.getProperty(J2EE_SERVER_INSTANCE),
            profile,
            J2eeModule.Type.EJB);
        J2EE_PLATFORM_MODEL = J2eePlatformUiSupport.createSpecVersionComboBoxModel(profile);
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
        
        RUNMAIN_JVM_MODEL = projectGroup.createStringDocument(evaluator, RUNMAIN_JVM_ARGS);

        LICENSE_SUPPORT = new LicensePanelSupport(evaluator, project.getAntProjectHelper(),
                projectProperties.get(LicensePanelSupport.LICENSE_PATH),
                projectProperties.get(LicensePanelSupport.LICENSE_NAME));

    }
    
    public void save() {
        try {
            saveLibrariesLocation();
            LICENSE_SUPPORT.saveLicenseFile();
            // Store properties 
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
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
        resolveProjectDependenciesNew();
        
        // Encode all paths (this may change the project properties)
        List<ClassPathSupport.Item> javaClasspathList = ClassPathUiSupport.getList(JAVAC_CLASSPATH_MODEL.getDefaultListModel());
        String[] javac_cp = cs.encodeToStrings( javaClasspathList, ClassPathSupportCallbackImpl.ELEMENT_INCLUDED_LIBRARIES  );
        String[] javac_test_cp = cs.encodeToStrings( ClassPathUiSupport.getList( JAVAC_TEST_CLASSPATH_MODEL ), null );
        String[] run_test_cp = cs.encodeToStrings( ClassPathUiSupport.getList( RUN_TEST_CLASSPATH_MODEL ), null );
        String[] endorsed_cp = cs.encodeToStrings( ClassPathUiSupport.getList( ENDORSED_CLASSPATH_MODEL ), null );
        String[] javac_pp = cs.encodeToStrings( ClassPathUiSupport.getList( JAVAC_PROCESSORPATH_MODEL ) );
                
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
        
        // Save all paths
        projectProperties.setProperty( ProjectProperties.JAVAC_CLASSPATH, javac_cp );
        projectProperties.setProperty( ProjectProperties.JAVAC_PROCESSORPATH, javac_pp );
        projectProperties.setProperty( ProjectProperties.JAVAC_TEST_CLASSPATH, javac_test_cp );
        projectProperties.setProperty( ProjectProperties.RUN_TEST_CLASSPATH, run_test_cp );
        projectProperties.setProperty( ProjectProperties.ENDORSED_CLASSPATH, endorsed_cp );
        
        //Handle platform selection and javac.source javac.target properties
        PlatformUiSupport.storePlatform (projectProperties, updateHelper, EjbJarProjectType.PROJECT_CONFIGURATION_NAMESPACE, PLATFORM_MODEL.getSelectedItem(), JAVAC_SOURCE_MODEL.getSelectedItem());
                
        // Handle other special cases
        if ( NO_DEPENDENCIES_MODEL.isSelected() ) { // NOI18N
            projectProperties.remove( NO_DEPENDENCIES ); // Remove the property completely if not set
        }
        Profile newJ2eeVersion = J2eePlatformUiSupport.getJavaEEProfile(J2EE_PLATFORM_MODEL.getSelectedItem());
        
        if (J2EE_SERVER_INSTANCE_MODEL.getSelectedItem() != null) {
            final String instanceId = J2eePlatformUiSupport.getServerInstanceID(J2EE_SERVER_INSTANCE_MODEL.getSelectedItem());
            J2EEProjectProperties.updateServerProperties(projectProperties, privateProperties, instanceId,
                    cs, javaClasspathList,
                    new CallbackImpl(project), project,
                    newJ2eeVersion, J2eeModule.Type.EJB);
        }
        
        // Update the deployment descriptor if upgrading from J2EE 1.3 to 1.4 and set the new J2EE spec version
        Profile oldJ2eeVersion = Profile.fromPropertiesString(projectProperties.getProperty(J2EE_PLATFORM));
        if (oldJ2eeVersion != null && newJ2eeVersion != null) {
            if (oldJ2eeVersion.equals(Profile.J2EE_13) && newJ2eeVersion.equals(Profile.J2EE_14)) {
                org.netbeans.modules.j2ee.api.ejbjar.EjbJar ejbJarModules[] = org.netbeans.modules.j2ee.api.ejbjar.EjbJar.getEjbJars(project);
                if (ejbJarModules.length > 0) {
                    FileObject ddFo = ejbJarModules[0].getDeploymentDescriptor();
                    if (ddFo != null) {
                        EjbJar ddRoot = DDProvider.getDefault().getDDRoot(ddFo);
                        if (ddRoot != null) {
                            ddRoot.setVersion(new BigDecimal(EjbJar.VERSION_2_1));
                            ddRoot.write(ddFo);
                        }
                    }
                }
            }
            
            // Set the new J2EE spec version 
            projectProperties.setProperty(J2EE_PLATFORM, newJ2eeVersion.toPropertiesString());
        }
        
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
            LOGGER.log(Level.FINE, "Starting listening on cos for {0}", project.getEjbModule());
            Deployment.getDefault().enableCompileOnSaveSupport(project.getEjbModule());
        } else {
            LOGGER.log(Level.FINE, "Stopping listening on cos for {0}", project.getEjbModule());
            Deployment.getDefault().disableCompileOnSaveSupport(project.getEjbModule());
        }
        
        String value = additionalProperties.get(SOURCE_ENCODING);
        if (value != null) {
            try {
                FileEncodingQuery.setDefaultEncoding(Charset.forName(value));
            } catch (UnsupportedCharsetException e) {
                //When the encoding is not supported by JVM do not set it as default
            }
        }
    }
    
    /** 
     * Finds out what are new and removed project dependencies and 
     * applies the info to the project
     */   
    private void resolveProjectDependenciesNew() {
            
        // Create a set of old and new artifacts.
        Set<ClassPathSupport.Item> oldArtifacts = new HashSet<ClassPathSupport.Item>();
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        oldArtifacts.addAll( cs.itemsList( projectProperties.get(ProjectProperties.JAVAC_CLASSPATH), ClassPathSupportCallbackImpl.ELEMENT_INCLUDED_LIBRARIES  ) );
        oldArtifacts.addAll( cs.itemsList( projectProperties.get( ProjectProperties.JAVAC_PROCESSORPATH ) ) );
        oldArtifacts.addAll( cs.itemsList( projectProperties.get(ProjectProperties.JAVAC_TEST_CLASSPATH), null  ) );
        oldArtifacts.addAll( cs.itemsList( projectProperties.get(ProjectProperties.RUN_TEST_CLASSPATH), null  ) );
        oldArtifacts.addAll( cs.itemsList( projectProperties.get(ProjectProperties.ENDORSED_CLASSPATH), null  ) );
                   
        Set<ClassPathSupport.Item> newArtifacts = new HashSet<ClassPathSupport.Item>();
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_CLASSPATH_MODEL.getDefaultListModel() ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_PROCESSORPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_TEST_CLASSPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( RUN_TEST_CLASSPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( ENDORSED_CLASSPATH_MODEL ) );
                
        // Create set of removed artifacts and remove them
        Set<ClassPathSupport.Item> removed = new HashSet<ClassPathSupport.Item>(oldArtifacts);
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
            File f = ((File)((Vector)data.elementAt(i)).elementAt(0));
            rootURLs[i] = Utils.getRootURL(f,null);
            rootLabels[i] = (String) ((Vector)data.elementAt(i)).elementAt(1);
        }
        roots.putRoots(rootURLs,rootLabels);
    }
    
    public static void setServerInstance(final EjbJarProject project, final AntProjectHelper helper, final String serverInstanceID) {
        ProjectManager.mutex().postWriteRequest(new Runnable() {
            @Override
            public void run() {
                try {
                    EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    EditableProperties privateProps = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                    Profile prof = Profile.fromPropertiesString(projectProps.getProperty(J2EE_PLATFORM));
                    J2EEProjectProperties.updateServerProperties(projectProps, privateProps, serverInstanceID,
                            null, null, new CallbackImpl(project), project, prof, J2eeModule.Type.EJB);

                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, projectProps);
                    helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateProps);
                    ProjectManager.getDefault().saveProject(project);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
        });
    }
    
    /* This is used by CustomizerWSServiceHost */
    void putAdditionalProperty(String propertyName, String propertyValue) {
        additionalProperties.put(propertyName, propertyValue);
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

        private EjbJarProject project;

        public CallbackImpl(EjbJarProject project) {
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
