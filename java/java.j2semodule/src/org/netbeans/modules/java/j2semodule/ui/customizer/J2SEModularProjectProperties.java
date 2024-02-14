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

package org.netbeans.modules.java.j2semodule.ui.customizer;

import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.swing.ButtonModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.java.api.common.ModuleRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.project.ui.ClassPathUiSupport;
import org.netbeans.modules.java.api.common.ui.PlatformFilter;
import org.netbeans.modules.java.api.common.project.ui.customizer.ClassPathListCellRenderer;
import org.netbeans.modules.java.api.common.project.ui.customizer.SourceRootsUi;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.modules.java.j2semodule.J2SEModularProject;
import org.netbeans.modules.java.j2semodule.J2SEModularProjectUtil;
import org.netbeans.spi.java.project.support.ui.IncludeExcludeVisualizer;
import org.netbeans.spi.java.project.support.ui.SharableLibrariesUtils;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.support.ant.ui.StoreGroup;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.*;
import org.openide.util.lookup.Lookups;
import static org.netbeans.modules.java.api.common.project.ProjectProperties.*;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.modules.SpecificationVersion;

/**
 * @author Petr Hrebejk
 */
public class J2SEModularProjectProperties {
    
    //Hotfix of the issue #70058
    //Should be removed when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
    private static final Integer BOOLEAN_KIND_TF = 0;
    private static final Integer BOOLEAN_KIND_YN = 1;
    private static final Integer BOOLEAN_KIND_ED = 2;
    private static final String COS_MARK = ".netbeans_automatic_build";     //NOI18N
    private static final Logger LOG = Logger.getLogger(J2SEModularProjectProperties.class.getName());
    private Integer javacDebugBooleanKind;
    private Integer doJarBooleanKind;
    private Integer javadocPreviewBooleanKind;
    private Integer doJLinkKind;
    private Integer jLinkStripKind;
    private Integer jLinkLauncherKind;
    
    // Special properties of the project
    public static final String J2SE_PROJECT_NAME = "j2se.project.name"; // NOI18N
    // Properties stored in the PROJECT.PROPERTIES
    public static final String APPLICATION_TITLE ="application.title"; // NOI18N
    public static final String APPLICATION_VENDOR ="application.vendor"; // NOI18N
    public static final String APPLICATION_DESC ="application.desc"; // NOI18N
    public static final String APPLICATION_HOMEPAGE ="application.homepage"; // NOI18N
    //Disables copying of dependencies to dist folder
    public static final String MKDIST_DISABLED = "mkdist.disabled"; //NOI18N
    //Runtime platform
    public static final String PLATFORM_RUNTIME = "platform.runtime";   //NOI18N
    //Force javac fork
    public static final String JAVAC_EXTERNAL_VM = "javac.external.vm";   //NOI18N

    //Name of ant platform name property
    public static final String PROP_PLATFORM_ANT_NAME = "platform.ant.name";    //NOI18N

    public static final PlatformFilter MODULAR_PLATFORM_FILTER = (jp) -> {
        final Specification spec = jp.getSpecification();
        return CommonProjectUtils.J2SE_PLATFORM_TYPE.contentEquals(spec.getName()) &&
                J2SEModularProjectUtil.MIN_SOURCE_LEVEL.compareTo(spec.getVersion()) <= 0;
    };

    private static final String[] CONFIG_AWARE_PROPERTIES = {
        ProjectProperties.MAIN_CLASS,
        ProjectProperties.APPLICATION_ARGS,
        ProjectProperties.RUN_JVM_ARGS,
        ProjectProperties.RUN_WORK_DIR,
        PLATFORM_RUNTIME
    };
    
    ClassPathSupport cs;
    
    // SOURCE ROOTS
    // public static final String SOURCE_ROOTS = "__virtual_source_roots__";   //NOI18N
    // public static final String TEST_ROOTS = "__virtual_test_roots__"; // NOI18N
                        
    // MODELS FOR VISUAL CONTROLS
    
    // CustomizerSources
    DefaultTableModel MODULE_ROOTS_MODEL;
    DefaultTableModel TEST_MODULE_ROOTS_MODEL;
    ComboBoxModel JAVAC_SOURCE_MODEL;
    ComboBoxModel JAVAC_PROFILE_MODEL;
     
    // CustomizerLibraries
    DefaultListModel JAVAC_MODULEPATH_MODEL;
    DefaultListModel JAVAC_CLASSPATH_MODEL;
    DefaultListModel JAVAC_PROCESSORMODULEPATH_MODEL;
    DefaultListModel JAVAC_PROCESSORPATH_MODEL;
    DefaultListModel JAVAC_TEST_MODULEPATH_MODEL;
    DefaultListModel JAVAC_TEST_CLASSPATH_MODEL;
    DefaultListModel RUN_MODULEPATH_MODEL;
    DefaultListModel RUN_CLASSPATH_MODEL;
    DefaultListModel RUN_TEST_MODULEPATH_MODEL;
    DefaultListModel RUN_TEST_CLASSPATH_MODEL;
    DefaultListModel ENDORSED_CLASSPATH_MODEL;
    ComboBoxModel PLATFORM_MODEL;
    ListCellRenderer CLASS_PATH_LIST_RENDERER;
    ListCellRenderer PLATFORM_LIST_RENDERER;
    ListCellRenderer JAVAC_SOURCE_RENDERER;
    ListCellRenderer JAVAC_PROFILE_RENDERER;
    Document SHARED_LIBRARIES_MODEL;
    
    // CustomizerCompile
    ButtonModel JAVAC_DEPRECATION_MODEL; 
    ButtonModel JAVAC_DEBUG_MODEL;
    ButtonModel DO_DEPEND_MODEL;
    ButtonModel COMPILE_ON_SAVE_MODEL;
    ButtonModel NO_DEPENDENCIES_MODEL;
    ButtonModel ENABLE_ANNOTATION_PROCESSING_MODEL;
    ButtonModel ENABLE_ANNOTATION_PROCESSING_IN_EDITOR_MODEL;
    ButtonModel JAVAC_EXTERNAL_VM_MODEL;
    DefaultListModel ANNOTATION_PROCESSORS_MODEL;
    DefaultTableModel PROCESSOR_OPTIONS_MODEL;
    Document JAVAC_COMPILER_ARG_MODEL;
    
    // CustomizerCompileTest
                
    // CustomizerJar
    Document BUILD_CLASSES_EXCLUDES_MODEL; 
    ButtonModel JAR_COMPRESS_MODEL;
    ButtonModel DO_JAR_MODEL;
    ButtonModel JLINK_MODEL;
    ButtonModel JLINK_STRIP_MODEL;
    ButtonModel JLINK_LAUNCHER_MODEL;
    Document    JLINK_LAUNCHER_NAME_MODEL;
                
    // CustomizerJavadoc
    ButtonModel JAVADOC_PRIVATE_MODEL;
    ButtonModel JAVADOC_NO_TREE_MODEL;
    ButtonModel JAVADOC_USE_MODEL;
    ButtonModel JAVADOC_NO_NAVBAR_MODEL; 
    ButtonModel JAVADOC_NO_INDEX_MODEL; 
    ButtonModel JAVADOC_SPLIT_INDEX_MODEL; 
    ButtonModel JAVADOC_HTML5_MODEL; 
    ButtonModel JAVADOC_AUTHOR_MODEL; 
    ButtonModel JAVADOC_VERSION_MODEL;
    Document JAVADOC_WINDOW_TITLE_MODEL;
    ButtonModel JAVADOC_PREVIEW_MODEL; 
    Document JAVADOC_ADDITIONALPARAM_MODEL;

    // CustomizerRun
    Map<String/*|null*/,Map<String,String/*|null*/>/*|null*/> RUN_CONFIGS;
    String activeConfig;
    
    // CustomizerApplication
    Document APPLICATION_TITLE_DOC;
    Document APPLICATION_VENDOR_DOC;
    Document APPLICATION_DESC_DOC;
    Document APPLICATION_HOMEPAGE_DOC;
    
    //customizer license headers
    String LICENSE_NAME_VALUE;
    String LICENSE_PATH_VALUE;
    String CHANGED_LICENSE_PATH_CONTENT;
    
    // CustomizerRunTest

    // Private fields ----------------------------------------------------------    
    private J2SEModularProject project;
    private UpdateHelper updateHelper;
    private PropertyEvaluator evaluator;
    private ReferenceHelper refHelper;
    private GeneratedFilesHelper genFileHelper;
    
    private StoreGroup privateGroup; 
    private StoreGroup projectGroup;
    
    private Map<String,String> additionalProperties;

    private String includes, excludes;
    private final List<ActionListener> optionListeners = new CopyOnWriteArrayList<ActionListener>();
    private final List<ClassPathSupport.Item> runModulePathExtension;
    
    J2SEModularProject getProject() {
        return project;
    }
    
    /** Creates a new instance of J2SEUIProperties and initializes them */
    public J2SEModularProjectProperties( J2SEModularProject project, UpdateHelper updateHelper, PropertyEvaluator evaluator, ReferenceHelper refHelper, GeneratedFilesHelper genFileHelper ) {
        this.project = project;
        this.updateHelper  = updateHelper;
        this.evaluator = evaluator;
        this.refHelper = refHelper;
        this.genFileHelper = genFileHelper;
        this.cs = new ClassPathSupport(evaluator, refHelper, updateHelper.getAntProjectHelper(), updateHelper, null);
                
        privateGroup = new StoreGroup();
        projectGroup = new StoreGroup();
        
        additionalProperties = new HashMap<>();
        runModulePathExtension = new ArrayList<>();
        init(); // Load known properties        
    }
    

    public PropertyEvaluator getEvaluator() {
        return evaluator;
    }
    

    /** Initializes the visual models 
     */
    private void init() {
        
        CLASS_PATH_LIST_RENDERER = ClassPathListCellRenderer.createClassPathListRenderer(evaluator, project.getProjectDirectory());
        
        // CustomizerSources
        MODULE_ROOTS_MODEL = SourceRootsUi.createModel( project.getModuleRoots());
        TEST_MODULE_ROOTS_MODEL = SourceRootsUi.createModel( project.getTestModuleRoots());        
        includes = evaluator.getProperty(ProjectProperties.INCLUDES);
        if (includes == null) {
            includes = "**"; // NOI18N
        }
        excludes = evaluator.getProperty(ProjectProperties.EXCLUDES);
        if (excludes == null) {
            excludes = ""; // NOI18N
        }
                
        // CustomizerLibraries
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );                
        
        JAVAC_MODULEPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(projectProperties.get(ProjectProperties.JAVAC_MODULEPATH)));
        JAVAC_CLASSPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(projectProperties.get(ProjectProperties.JAVAC_CLASSPATH)));
        String processorPath = projectProperties.get(ProjectProperties.JAVAC_PROCESSORPATH);
        processorPath = processorPath == null ? "${javac.classpath}" : processorPath;
        JAVAC_PROCESSORPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(processorPath));
        JAVAC_PROCESSORMODULEPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(projectProperties.get(ProjectProperties.JAVAC_PROCESSORMODULEPATH)));
        JAVAC_TEST_MODULEPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(projectProperties.get(ProjectProperties.JAVAC_TEST_MODULEPATH)));
        JAVAC_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(projectProperties.get(ProjectProperties.JAVAC_TEST_CLASSPATH)));
        RUN_MODULEPATH_MODEL = ClassPathUiSupport.createListModel(createExtendedPathItems(projectProperties, ProjectProperties.RUN_MODULEPATH, null, isNamedModule() ? ProjectProperties.BUILD_CLASSES_DIR : null, runModulePathExtension));
        RUN_CLASSPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(projectProperties.get(ProjectProperties.RUN_CLASSPATH)));
        RUN_TEST_MODULEPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(projectProperties.get(ProjectProperties.RUN_TEST_MODULEPATH)));
        RUN_TEST_CLASSPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(projectProperties.get(ProjectProperties.RUN_TEST_CLASSPATH)));
        ENDORSED_CLASSPATH_MODEL = ClassPathUiSupport.createListModel(cs.itemsIterator(projectProperties.get(ProjectProperties.ENDORSED_CLASSPATH)));
        final Collection<PlatformFilter> filters = new ArrayList<>();
        filters.add(MODULAR_PLATFORM_FILTER);
        filters.addAll(project.getLookup().lookupAll(PlatformFilter.class));
        PLATFORM_MODEL = PlatformUiSupport.createPlatformComboBoxModel (project, evaluator, evaluator.getProperty(PLATFORM_ACTIVE), filters);
        PLATFORM_LIST_RENDERER = PlatformUiSupport.createPlatformListCellRenderer();
        JAVAC_SOURCE_MODEL = PlatformUiSupport.createSourceLevelComboBoxModel (PLATFORM_MODEL, evaluator.getProperty(JAVAC_SOURCE), evaluator.getProperty(JAVAC_TARGET), J2SEModularProjectUtil.MIN_SOURCE_LEVEL);
        JAVAC_SOURCE_RENDERER = PlatformUiSupport.createSourceLevelListCellRenderer ();
        JAVAC_PROFILE_MODEL = PlatformUiSupport.createProfileComboBoxModel(JAVAC_SOURCE_MODEL, evaluator.getProperty(JAVAC_PROFILE), null);
        JAVAC_PROFILE_RENDERER = PlatformUiSupport.createProfileListCellRenderer();

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
        JAVAC_DEBUG_MODEL = createToggleButtonModel( evaluator, JAVAC_DEBUG, true, kind);
        javacDebugBooleanKind = kind[0];

        DO_DEPEND_MODEL = privateGroup.createToggleButtonModel(evaluator, ProjectProperties.DO_DEPEND);

        COMPILE_ON_SAVE_MODEL = privateGroup.createToggleButtonModel(evaluator, ProjectProperties.COMPILE_ON_SAVE);

        NO_DEPENDENCIES_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, NO_DEPENDENCIES );
        ENABLE_ANNOTATION_PROCESSING_MODEL =projectGroup.createToggleButtonModel(evaluator, ProjectProperties.ANNOTATION_PROCESSING_ENABLED);
        ENABLE_ANNOTATION_PROCESSING_IN_EDITOR_MODEL = projectGroup.createToggleButtonModel(evaluator, ProjectProperties.ANNOTATION_PROCESSING_ENABLED_IN_EDITOR);
        String annotationProcessors = projectProperties.get(ProjectProperties.ANNOTATION_PROCESSING_PROCESSORS_LIST);
        if (annotationProcessors == null)
            annotationProcessors = ""; //NOI18N
        ANNOTATION_PROCESSORS_MODEL = ClassPathUiSupport.createListModel(
                (annotationProcessors.length() > 0 ? Arrays.asList(annotationProcessors.split(",")) : Collections.emptyList()).iterator()); //NOI18N
        String processorOptions = projectProperties.get(ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS);
        if (processorOptions == null)
            processorOptions = ""; //NOI18N
        PROCESSOR_OPTIONS_MODEL = new DefaultTableModel(new String[][]{}, new String[] {
            NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Processor_Options_Key"), //NOI18N
            NbBundle.getMessage(CustomizerCompile.class, "LBL_CustomizeCompile_Processor_Options_Value") //NOI18N
        }) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (String option : processorOptions.split("\\s")) { //NOI18N
            if (option.startsWith("-A") && option.length() > 2) { //NOI18N
                int sepIndex = option.indexOf('='); //NOI18N
                String key = null;
                String value = null;
                if (sepIndex == -1)
                    key = option.substring(2);
                else if (sepIndex >= 3) {
                    key = option.substring(2, sepIndex);
                    value = (sepIndex < option.length() - 1) ? option.substring(sepIndex + 1) : null;
                }
                PROCESSOR_OPTIONS_MODEL.addRow(new String[] {key, value});
            }
        }
        JAVAC_EXTERNAL_VM_MODEL = projectGroup.createToggleButtonModel(evaluator, JAVAC_EXTERNAL_VM);
        JAVAC_COMPILER_ARG_MODEL = projectGroup.createStringDocument( evaluator, JAVAC_COMPILERARGS );
        
        // CustomizerJar
        BUILD_CLASSES_EXCLUDES_MODEL = projectGroup.createStringDocument( evaluator, BUILD_CLASSES_EXCLUDES );
        JAR_COMPRESS_MODEL = projectGroup.createToggleButtonModel( evaluator, JAR_COMPRESS );
        DO_JAR_MODEL = createToggleButtonModel(evaluator, ProjectProperties.DO_JAR, true, kind);
        doJarBooleanKind = kind[0];
        JLINK_MODEL = createToggleButtonModel(evaluator, ProjectProperties.DO_JLINK, false, kind);
        doJLinkKind = kind[0];
        JLINK_STRIP_MODEL = createToggleButtonModel(evaluator, ProjectProperties.JLINK_STRIP, false, kind);
        jLinkStripKind = kind[0];
        JLINK_LAUNCHER_MODEL = createToggleButtonModel(evaluator, ProjectProperties.JLINK_LAUNCHER, false, kind);
        jLinkLauncherKind = kind[0];
        JLINK_LAUNCHER_NAME_MODEL = projectGroup.createStringDocument( evaluator, JLINK_LAUNCHER_NAME);
        final String launcherName = evaluator.getProperty(JLINK_LAUNCHER_NAME);
        if (launcherName == null) {
            try {
                JLINK_LAUNCHER_NAME_MODEL.insertString(
                        0,
                        PropertyUtils.getUsablePropertyName(ProjectUtils.getInformation(project).getDisplayName()),
                        null);
            } catch (BadLocationException ex) {
                // just do not set anything
            }
        }
        
        // CustomizerJavadoc
        JAVADOC_PRIVATE_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_PRIVATE );
        JAVADOC_NO_TREE_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, JAVADOC_NO_TREE );
        JAVADOC_USE_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_USE );
        JAVADOC_NO_NAVBAR_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, JAVADOC_NO_NAVBAR );
        JAVADOC_NO_INDEX_MODEL = projectGroup.createInverseToggleButtonModel( evaluator, JAVADOC_NO_INDEX ); 
        JAVADOC_SPLIT_INDEX_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_SPLIT_INDEX );
        JAVADOC_HTML5_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_HTML5 );
        JAVADOC_AUTHOR_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_AUTHOR );
        JAVADOC_VERSION_MODEL = projectGroup.createToggleButtonModel( evaluator, JAVADOC_VERSION );
        JAVADOC_WINDOW_TITLE_MODEL = projectGroup.createStringDocument( evaluator, JAVADOC_WINDOW_TITLE );
        //Hotfix of the issue #70058
        //Should use the StoreGroup when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel        
        JAVADOC_PREVIEW_MODEL = createToggleButtonModel ( evaluator, JAVADOC_PREVIEW, true, kind);
        javadocPreviewBooleanKind = kind[0];
        
        JAVADOC_ADDITIONALPARAM_MODEL = projectGroup.createStringDocument( evaluator, JAVADOC_ADDITIONALPARAM );
        
        // CustomizerApplication
        APPLICATION_TITLE_DOC = projectGroup.createStringDocument(evaluator, APPLICATION_TITLE);
        String title = evaluator.getProperty(APPLICATION_TITLE);
        if (title == null) {
            try {
                APPLICATION_TITLE_DOC.insertString(0, ProjectUtils.getInformation(project).getDisplayName(), null);
            } catch (BadLocationException ex) {
                // just do not set anything
            }
        }
        APPLICATION_VENDOR_DOC = projectGroup.createStringDocument(evaluator, APPLICATION_VENDOR);
        String vendor = evaluator.getProperty("application.vendor");
        if (vendor == null) {
            try {
                APPLICATION_VENDOR_DOC.insertString(0, System.getProperty("user.name", "User Name"), null);
            } catch (BadLocationException ex) {
                // just do not set anything
            }
        }
        APPLICATION_DESC_DOC = projectGroup.createStringDocument(evaluator, APPLICATION_DESC);
        APPLICATION_HOMEPAGE_DOC = projectGroup.createStringDocument(evaluator, APPLICATION_HOMEPAGE);
        
        //oh well we want unresolved value, force it.
        LICENSE_PATH_VALUE = projectProperties.get(LICENSE_PATH);
        LICENSE_NAME_VALUE = projectProperties.get(LICENSE_NAME);
        
        CHANGED_LICENSE_PATH_CONTENT = null;
        
        if(!isFXProject()) {
            // CustomizerRun
            RUN_CONFIGS = readRunConfigs();
            activeConfig = evaluator.getProperty("config");
        }
                
    }
    
    public void save() {
        try {                        
            saveLibrariesLocation();
            if (CHANGED_LICENSE_PATH_CONTENT != null) {
                String path = LICENSE_PATH_VALUE;
                assert path != null; //path needs to exist once we have content?
                String evaluated = getEvaluator().evaluate(path);
                File file = project.getAntProjectHelper().resolveFile(evaluated);
                FileObject fo;
                if (!file.exists()) {
                    fo = FileUtil.createData(file);
                } else {
                    fo = FileUtil.toFileObject(file);
                }
                OutputStream out = fo.getOutputStream();
                try {
                    FileUtil.copy(new ByteArrayInputStream(CHANGED_LICENSE_PATH_CONTENT.getBytes()), out);
                } finally {
                    out.close();
                }
            }
            // Store properties
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws IOException {
                    storeProperties();
                    //Delete COS mark
                    if (!COMPILE_ON_SAVE_MODEL.isSelected()) {
                        String buildClassesDir = evaluator.getProperty(ProjectProperties.BUILD_CLASSES_DIR);
                        if (buildClassesDir == null) {
                            //BUILD_CLASSES_DIR is mandatory property => broken project?
                            //Log
                            StringBuilder logRecord = new StringBuilder();
                            logRecord.append("EVALUATOR: "+evaluator.getProperties().toString()+";");       //NOI18N
                            logRecord.append("PROJECT_PROPS: "+updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).entrySet()+";");    //NOI18N
                            logRecord.append("PRIVATE_PROPS: "+updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).entrySet()+";");    //NOI18N
                            assert buildClassesDir != null : logRecord.toString();  //In dev build throw an ae to get issue to find why BUILD_CLASSES_DIR is null
                            LOG.warning("No build.classes.dir property: " + logRecord.toString()); //In release at least log
                        }
                        else {
                            FileObject buildClasses = updateHelper.getAntProjectHelper().resolveFileObject(buildClassesDir);
                            if (buildClasses != null) {
                                FileObject mark = buildClasses.getFileObject(COS_MARK);
                                if (mark != null) {
                                    final ActionProvider ap = project.getLookup().lookup(ActionProvider.class);
                                    assert ap != null;
                                    Mutex.EVENT.writeAccess(new Runnable() {
                                        @Override public void run() {
                                            ap.invokeAction(ActionProvider.COMMAND_CLEAN, Lookups.fixed(project));
                                        }
                                    });
                                }
                            }
                        }
                    }
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
        } 
        catch (MutexException e) {
            ErrorManager.getDefault().notify((IOException)e.getException());
        }
        catch ( IOException ex ) {
            ErrorManager.getDefault().notify( ex );
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
            ErrorManager.getDefault().notify(x);
        }
    }
        
    private void storeProperties() throws IOException {
        // Store special properties
        
        // Modify the project dependencies properly        
        resolveProjectDependencies();
        
        // Encode all paths (this may change the project properties)
        String[] javac_mp = cs.encodeToStrings( ClassPathUiSupport.getList( JAVAC_MODULEPATH_MODEL ) );
        String[] javac_cp = cs.encodeToStrings( ClassPathUiSupport.getList( JAVAC_CLASSPATH_MODEL ) );
        String[] javac_pmp = cs.encodeToStrings( ClassPathUiSupport.getList( JAVAC_PROCESSORMODULEPATH_MODEL ) );
        String[] javac_pp = cs.encodeToStrings( ClassPathUiSupport.getList( JAVAC_PROCESSORPATH_MODEL ) );
        String[] javac_test_mp = cs.encodeToStrings( ClassPathUiSupport.getList( JAVAC_TEST_MODULEPATH_MODEL ) );
        String[] javac_test_cp = cs.encodeToStrings( ClassPathUiSupport.getList( JAVAC_TEST_CLASSPATH_MODEL ) );
        List<ClassPathSupport.Item> l = new ArrayList<>(ClassPathUiSupport.getList( RUN_MODULEPATH_MODEL ));
        l.removeAll(runModulePathExtension);
        String[] run_mp = cs.encodeToStrings(l);
        String[] run_cp = cs.encodeToStrings( ClassPathUiSupport.getList( RUN_CLASSPATH_MODEL ) );
        String[] run_test_mp = cs.encodeToStrings( ClassPathUiSupport.getList( RUN_TEST_MODULEPATH_MODEL ) );
        String[] run_test_cp = cs.encodeToStrings( ClassPathUiSupport.getList( RUN_TEST_CLASSPATH_MODEL ) );
        String[] endorsed_cp = cs.encodeToStrings( ClassPathUiSupport.getList( ENDORSED_CLASSPATH_MODEL ) );
                
        // Store module roots
        storeRoots( project.getModuleRoots(), MODULE_ROOTS_MODEL );
        storeRoots( project.getTestModuleRoots(), TEST_MODULE_ROOTS_MODEL );
                
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
        
        if (LICENSE_PATH_VALUE != null) {
            projectProperties.setProperty(LICENSE_PATH, LICENSE_PATH_VALUE);
        } else {
            projectProperties.remove(LICENSE_PATH);
        }
        if (LICENSE_NAME_VALUE != null) {
            projectProperties.setProperty(LICENSE_NAME, LICENSE_NAME_VALUE);
        } else {
            projectProperties.remove(LICENSE_NAME);
        }
        
        final boolean isFXProject = isFXProject();
        if(!isFXProject) {
            storeRunConfigs(RUN_CONFIGS, projectProperties, privateProperties);
            EditableProperties ep = updateHelper.getProperties("nbproject/private/config.properties");
            if (activeConfig == null) {
                ep.remove("config");
            } else {
                ep.setProperty("config", activeConfig);
            }
            updateHelper.putProperties("nbproject/private/config.properties", ep);
        }
        
        //Hotfix of the issue #70058
        //Should use the StoreGroup when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
        //Save javac.debug
        privateProperties.setProperty(JAVAC_DEBUG, encodeBoolean (JAVAC_DEBUG_MODEL.isSelected(), javacDebugBooleanKind));
        privateProperties.setProperty(ProjectProperties.DO_JAR, encodeBoolean(DO_JAR_MODEL.isSelected(), doJarBooleanKind));
        //JLink
        privateProperties.setProperty(ProjectProperties.DO_JLINK, encodeBoolean(JLINK_MODEL.isSelected(), doJLinkKind));
        privateProperties.setProperty(ProjectProperties.JLINK_STRIP, encodeBoolean(JLINK_STRIP_MODEL.isSelected(), jLinkStripKind));
        projectProperties.setProperty(ProjectProperties.JLINK_LAUNCHER, encodeBoolean(JLINK_LAUNCHER_MODEL.isSelected(), jLinkLauncherKind));
        //Hotfix of the issue #70058
        //Should use the StoreGroup when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
        //Save javadoc.preview
        privateProperties.setProperty(JAVADOC_PREVIEW, encodeBoolean (JAVADOC_PREVIEW_MODEL.isSelected(), javadocPreviewBooleanKind));
                
        // Save all paths
        projectProperties.setProperty(ProjectProperties.JAVAC_MODULEPATH, javac_mp );
        projectProperties.setProperty( ProjectProperties.JAVAC_CLASSPATH, javac_cp );
        projectProperties.setProperty( ProjectProperties.JAVAC_PROCESSORMODULEPATH, javac_pmp );
        projectProperties.setProperty( ProjectProperties.JAVAC_PROCESSORPATH, javac_pp );
        projectProperties.setProperty( ProjectProperties.JAVAC_TEST_MODULEPATH, javac_test_mp );
        projectProperties.setProperty( ProjectProperties.JAVAC_TEST_CLASSPATH, javac_test_cp );
        projectProperties.setProperty( ProjectProperties.RUN_MODULEPATH, run_mp );
        projectProperties.setProperty( ProjectProperties.RUN_CLASSPATH, run_cp );
        projectProperties.setProperty( ProjectProperties.RUN_TEST_MODULEPATH, run_test_mp );
        projectProperties.setProperty( ProjectProperties.RUN_TEST_CLASSPATH, run_test_cp );
        projectProperties.setProperty( ProjectProperties.ENDORSED_CLASSPATH, endorsed_cp );
        
        //Handle platform selection and javac.source javac.target properties
        PlatformUiSupport.storePlatform (
                projectProperties,
                updateHelper,
                J2SEModularProject.PROJECT_CONFIGURATION_NAMESPACE,
                PLATFORM_MODEL.getSelectedItem(),
                JAVAC_SOURCE_MODEL.getSelectedItem(),
                JAVAC_PROFILE_MODEL.getSelectedItem(),
                !isFXProject);
                                
        // Handle other special cases
        if ( NO_DEPENDENCIES_MODEL.isSelected() ) { // NOI18N
            projectProperties.remove( NO_DEPENDENCIES ); // Remove the property completely if not set
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

        sb = new StringBuilder();
        for (int i = 0; i < PROCESSOR_OPTIONS_MODEL.getRowCount(); i++) {
            String key = (String) PROCESSOR_OPTIONS_MODEL.getValueAt(i, 0);
            String value = (String) PROCESSOR_OPTIONS_MODEL.getValueAt(i, 1);
            sb.append("-A").append(key); //NOI18N
            if (value != null && value.length() > 0) {
                sb.append('=').append(value); //NOI18N
            }
            if (i < PROCESSOR_OPTIONS_MODEL.getRowCount() - 1) {
                sb.append(' '); //NOI18N
            }
        }
        if (sb.length() > 0) {
            projectProperties.put(ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS, sb.toString());
        } else {
            projectProperties.remove(ProjectProperties.ANNOTATION_PROCESSING_PROCESSOR_OPTIONS);
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
    
    /** Finds out what are new and removed project dependencies and 
     * applyes the info to the project
     */
    private void resolveProjectDependencies() {
            
        // Create a set of old and new artifacts.
        Set<ClassPathSupport.Item> oldArtifacts = new HashSet<>();
        EditableProperties projectProperties = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );        
        oldArtifacts.addAll( cs.itemsList( projectProperties.get( ProjectProperties.JAVAC_MODULEPATH ) ) );
        oldArtifacts.addAll( cs.itemsList( projectProperties.get( ProjectProperties.JAVAC_CLASSPATH ) ) );
        oldArtifacts.addAll( cs.itemsList( projectProperties.get( ProjectProperties.JAVAC_PROCESSORMODULEPATH ) ) );
        oldArtifacts.addAll( cs.itemsList( projectProperties.get( ProjectProperties.JAVAC_PROCESSORPATH ) ) );
        oldArtifacts.addAll( cs.itemsList( projectProperties.get( ProjectProperties.JAVAC_TEST_MODULEPATH ) ) );
        oldArtifacts.addAll( cs.itemsList( projectProperties.get( ProjectProperties.JAVAC_TEST_CLASSPATH ) ) );
        oldArtifacts.addAll( cs.itemsList( projectProperties.get( ProjectProperties.RUN_MODULEPATH ) ) );
        oldArtifacts.addAll( cs.itemsList( projectProperties.get( ProjectProperties.RUN_CLASSPATH ) ) );
        oldArtifacts.addAll( cs.itemsList( projectProperties.get( ProjectProperties.RUN_TEST_MODULEPATH ) ) );
        oldArtifacts.addAll( cs.itemsList( projectProperties.get( ProjectProperties.RUN_TEST_CLASSPATH ) ) );
        oldArtifacts.addAll( cs.itemsList( projectProperties.get( ProjectProperties.ENDORSED_CLASSPATH ) ) );
                   
        Set<ClassPathSupport.Item> newArtifacts = new HashSet<>();
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_MODULEPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_CLASSPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_PROCESSORMODULEPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_PROCESSORPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_TEST_MODULEPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( JAVAC_TEST_CLASSPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( RUN_MODULEPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( RUN_CLASSPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( RUN_TEST_MODULEPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( RUN_TEST_CLASSPATH_MODEL ) );
        newArtifacts.addAll( ClassPathUiSupport.getList( ENDORSED_CLASSPATH_MODEL ) );
                
        // Create set of removed artifacts and remove them
        Set<ClassPathSupport.Item> removed = new HashSet<>(oldArtifacts);
        removed.removeAll( newArtifacts );
        Set<ClassPathSupport.Item> added = new HashSet<>(newArtifacts);
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
        
        boolean changed = false;
        // 2. now read project.properties and modify rest
        EditableProperties ep = updateHelper.getProperties( AntProjectHelper.PROJECT_PROPERTIES_PATH );
        
        for (ClassPathSupport.Item item : removed) {
            if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                // remove helper property pointing to library jar if there is any
                String prop = item.getReference();
                prop = CommonProjectUtils.getAntPropertyName(prop);
                ep.remove(prop);
                changed = true;
            }
        }
        if (changed) {
            updateHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        }
    }            
    
    private void storeRoots( ModuleRoots roots, DefaultTableModel tableModel ) throws MalformedURLException {
        Vector<Vector> data = tableModel.getDataVector();
        URL[] rootURLs = new URL[data.size()];
        String []rootPaths = new String[data.size()];
        final LinkedList<URL> oldRootURLs = new LinkedList<>(Arrays.asList (roots.getRootURLs (false)));
        final LinkedList<String> oldRootPathProps = new LinkedList<>(Arrays.asList (roots.getRootPathProperties()));
        boolean rootsAreSame = true;
        for (int i=0; i<data.size();i++) {
            File f = (File)data.elementAt(i).elementAt(0);
            rootURLs[i] = Utilities.toURI(f).toURL();
            if (!rootURLs[i].toExternalForm().endsWith("/")) {  //NOI18N
                rootURLs[i] = new URL(rootURLs[i]+"/");
            }
            validateURL(rootURLs[i],f);
            rootPaths[i] = (String)data.elementAt(i).elementAt(1);
            rootsAreSame &= !oldRootURLs.isEmpty() &&
                            oldRootURLs.removeFirst().equals(rootURLs[i]) &&
                            roots.getRootPath(oldRootPathProps.removeFirst()).equals(rootPaths[i]);
        }
        if (!rootsAreSame || !oldRootURLs.isEmpty ()) {
            roots.putModuleRoots(rootURLs, rootPaths);
        }
    }

    private void validateURL(final URL url, final File file) {
        try {
            final URI uri = url.toURI();
            if (!uri.isAbsolute()) {
                throw new IllegalArgumentException("URI is not absolute: " + uri.toString() + " File: " + file.getAbsolutePath());   //NOI18N
            }
            if (uri.isOpaque()) {
                throw new IllegalArgumentException("URI is not hierarchical: " + uri.toString() + " File: " + file.getAbsolutePath());   //NOI18N
            }
            if (!"file".equals(uri.getScheme())) {
                throw new IllegalArgumentException("URI scheme is not \"file\": " + uri.toString() + " File: " + file.getAbsolutePath());   //NOI18N
            }
        } catch (URISyntaxException use) {
            throw new IllegalArgumentException(use);
        }
    }
    
    /* This is used by CustomizerWSServiceHost */
    public void putAdditionalProperty(String propertyName, String propertyValue) {
        additionalProperties.put(propertyName, propertyValue);
    }
    
        
    //Hotfix of the issue #70058
    //Should be removed when the StoreGroup SPI will be extended to allow false default value in ToggleButtonModel
    private static String encodeBoolean (boolean value, Integer kind) {
        if ( kind == BOOLEAN_KIND_ED ) {
            return value ? "on" : "off"; // NOI18N
        }
        else if ( kind == BOOLEAN_KIND_YN ) { // NOI18N
            return value ? "yes" : "no";
        }
        else {
            return value ? "true" : "false"; // NOI18N
        }
    }
    
    //Hotfix of the issue #70058
    //Should be removed when the StoreGroup SPI will be extended to allow true default value in ToggleButtonModel
    private static JToggleButton.ToggleButtonModel createToggleButtonModel (
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final String propName,
            final boolean defaultValue,
            @NonNull final Integer[] kind) {
        assert evaluator != null;
        assert propName != null;
        assert kind != null && kind.length == 1;
        String value = evaluator.getProperty( propName );
        boolean isSelected = false;
        if (value == null) {
            isSelected = defaultValue;
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
    
    /**
     * A mess.
     */
    Map<String/*|null*/,Map<String,String>> readRunConfigs() {
        Map<String,Map<String,String>> m = new TreeMap<String,Map<String,String>>(new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s1 != null ? (s2 != null ? s1.compareTo(s2) : 1) : (s2 != null ? -1 : 0);
            }
        });
        Map<String,String> def = new TreeMap<>();
        for (String prop : CONFIG_AWARE_PROPERTIES) {
            String v = updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(prop);
            if (v == null) {
                v = updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(prop);
            }
            if (v != null) {
                def.put(prop, v);
            }
        }
        m.put(null, def);
        FileObject configs = project.getProjectDirectory().getFileObject("nbproject/configs");
        if (configs != null) {
            for (FileObject kid : configs.getChildren()) {
                if (!kid.hasExt("properties")) {
                    continue;
                }
                final String relPath = FileUtil.getRelativePath(project.getProjectDirectory(), kid);
                if (relPath != null) {
                    m.put(kid.getName(), new TreeMap<>(updateHelper.getProperties(relPath)));
                }
            }
        }
        configs = project.getProjectDirectory().getFileObject("nbproject/private/configs");
        if (configs != null) {
            for (FileObject kid : configs.getChildren()) {
                if (!kid.hasExt("properties")) {
                    continue;
                }
                Map<String,String> c = m.get(kid.getName());
                if (c == null) {
                    continue;
                }
                final String relPath = FileUtil.getRelativePath(project.getProjectDirectory(), kid);
                if (relPath != null) {
                    c.putAll(new HashMap<>(updateHelper.getProperties(relPath)));
                }
            }
        }
        //System.err.println("readRunConfigs: " + m);
        return m;
    }

    /**
     * A royal mess.
     */
    void storeRunConfigs(Map<String/*|null*/,Map<String,String/*|null*/>/*|null*/> configs,
            EditableProperties projectProperties, EditableProperties privateProperties) throws IOException {
        //System.err.println("storeRunConfigs: " + configs);
        Map<String,String> def = configs.get(null);
        for (String prop : CONFIG_AWARE_PROPERTIES) {
            String v = def.get(prop);
            EditableProperties ep =
                    (prop.equals(ProjectProperties.APPLICATION_ARGS) ||
                    prop.equals(ProjectProperties.RUN_WORK_DIR)  ||
                    privateProperties.containsKey(prop)) ?
                privateProperties : projectProperties;
            if (!Utilities.compareObjects(v, ep.getProperty(prop))) {
                if (v != null && v.length() > 0) {
                    ep.setProperty(prop, v);
                } else {
                    ep.remove(prop);
                }
            }
        }
        for (Map.Entry<String,Map<String,String>> entry : configs.entrySet()) {
            String config = entry.getKey();
            if (config == null) {
                continue;
            }
            String sharedPath = "nbproject/configs/" + config + ".properties"; // NOI18N
            String privatePath = "nbproject/private/configs/" + config + ".properties"; // NOI18N
            Map<String,String> c = entry.getValue();
            if (c == null) {
                updateHelper.putProperties(sharedPath, null);
                updateHelper.putProperties(privatePath, null);
                continue;
            }
            final EditableProperties sharedCfgProps = updateHelper.getProperties(sharedPath);
            final EditableProperties privateCfgProps = updateHelper.getProperties(privatePath);
            boolean privatePropsChanged = false;
            for (Map.Entry<String,String> entry2 : c.entrySet()) {
                String prop = entry2.getKey();
                String v = entry2.getValue();
                EditableProperties ep =
                        (prop.equals(ProjectProperties.APPLICATION_ARGS) ||
                         prop.equals(ProjectProperties.RUN_WORK_DIR) ||
                         privateCfgProps.containsKey(prop)) ?
                    privateCfgProps : sharedCfgProps;
                if (!Utilities.compareObjects(v, ep.getProperty(prop))) {
                    if (v != null && (v.length() > 0 || (def.get(prop) != null && def.get(prop).length() > 0))) {
                        ep.setProperty(prop, v);
                    } else {
                        ep.remove(prop);
                    }
                    privatePropsChanged |= ep == privateCfgProps;
                }
            }
            updateHelper.putProperties(sharedPath, sharedCfgProps);    //Make sure the definition file is always created, even if it is empty.
            if (privatePropsChanged) {                              //Definition file is written, only when changed
                updateHelper.putProperties(privatePath, privateCfgProps);
            }
        }
    }

    /**
     * Checks presence of JavaFX 2.0 project extension.
     * Used to disable JSE Run config read/save mechanism
     * to enable its replacement in JFX2 Project implementation.
     */
    boolean isFXProject() {
        return J2SEModularProjectUtil.isTrue(evaluator.getProperty("javafx.enabled")); // NOI18N
    }
    
    void loadIncludesExcludes(IncludeExcludeVisualizer v) {
        Set<File> roots = new HashSet<>();
        for (DefaultTableModel model : new DefaultTableModel[] {MODULE_ROOTS_MODEL, TEST_MODULE_ROOTS_MODEL}) {
            for (Object row : model.getDataVector()) {
                File d = (File) ((Vector) row).elementAt(0);
                if (/* #104996 */d.isDirectory()) {
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

    boolean makeSharable() {
        List<String> libs = new ArrayList<>();
        List<String> jars = new ArrayList<>();
        collectLibs(JAVAC_CLASSPATH_MODEL, libs, jars);
        collectLibs(JAVAC_PROCESSORPATH_MODEL, libs, jars);
        collectLibs(JAVAC_TEST_CLASSPATH_MODEL, libs, jars);
        collectLibs(RUN_CLASSPATH_MODEL, libs, jars);
        collectLibs(RUN_TEST_CLASSPATH_MODEL, libs, jars);
        collectLibs(ENDORSED_CLASSPATH_MODEL, libs, jars);
        libs.add("CopyLibs"); // #132201 - copylibs is integral part of j2seproject
        String customTasksLibs = getProject().evaluator().getProperty(AntBuildExtender.ANT_CUSTOMTASKS_LIBS_PROPNAME);
        if (customTasksLibs != null) {
            String libIDs[] = customTasksLibs.split(",");
            for (String libID : libIDs) {
                libs.add(libID.trim());
            }
        }
        return SharableLibrariesUtils.showMakeSharableWizard(getProject().getAntProjectHelper(),
                getProject().getReferenceHelper(), libs, jars);
    }
    private void collectLibs(DefaultListModel model, List<String> libs, List<String> jarReferences) {
        for (int i = 0; i < model.size(); i++) {
            ClassPathSupport.Item item = (ClassPathSupport.Item) model.get(i);
            if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                if (!item.isBroken() && !libs.contains(item.getLibrary().getName())) {
                    libs.add(item.getLibrary().getName());
                }
            }
            if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
                if (item.getReference() != null && item.getVariableBasedProperty() == null && !jarReferences.contains(item.getReference())) {
                    //TODO reference is null for not yet persisted items.
                    // there seems to be no way to generate a reference string without actually
                    // creating and writing the property..
                    jarReferences.add(item.getReference());
                }
            }
        }
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

    private boolean isNamedModule() {
        final String sl = SourceLevelQuery.getSourceLevel2(project.getProjectDirectory()).getSourceLevel();
        if (sl == null || J2SEModularProjectUtil.MIN_SOURCE_LEVEL.compareTo(new SpecificationVersion(sl)) > 0) {
            return false;
        }
        return J2SEModularProjectUtil.hasModuleInfo(project.getSourceRoots());
    }

    @NonNull
    private Iterator<ClassPathSupport.Item> createExtendedPathItems(
            @NonNull final EditableProperties projectProperties,
            @NonNull final String propertyName,
            @NullAllowed final String prependPropertyName,
            @NullAllowed final String appendPropertyName,
            @NonNull final Collection<? super ClassPathSupport.Item> patch) {
        final Iterator<ClassPathSupport.Item> base = cs.itemsIterator(projectProperties.get(propertyName));
        if (prependPropertyName == null && appendPropertyName == null) {
            return base;
        }
        final List<ClassPathSupport.Item> extended = new ArrayList<>();
        final Set<File> artefacts = Arrays.stream(PropertyUtils.tokenizePath(evaluator.getProperty(propertyName)))
                .map((p) -> updateHelper.getAntProjectHelper().resolveFile(p))
                .collect(Collectors.toSet());
        if (prependPropertyName != null) {
            final boolean newItem = Optional.ofNullable(evaluator.getProperty(prependPropertyName))
                .map((p) -> updateHelper.getAntProjectHelper().resolveFile(p))
                .filter((f) -> !artefacts.contains(f))
                .isPresent();
            if (newItem) {
                final ClassPathSupport.Item i = ClassPathSupport.Item.create(String.format("${%s}", prependPropertyName));     //NOI18N
                patch.add(i);
                extended.add(i);
            }
        }
        base.forEachRemaining(extended::add);
        if (appendPropertyName != null) {
            final boolean newItem = Optional.ofNullable(evaluator.getProperty(appendPropertyName))
                .map((p) -> updateHelper.getAntProjectHelper().resolveFile(p))
                .filter((f) -> !artefacts.contains(f))
                .isPresent();
            if (newItem) {
                final ClassPathSupport.Item i = ClassPathSupport.Item.create(String.format("${%s}", appendPropertyName));   //NOI18N
                patch.add(i);
                extended.add(i);
            }
        }
        return Collections.unmodifiableList(extended).iterator();
    }

}
