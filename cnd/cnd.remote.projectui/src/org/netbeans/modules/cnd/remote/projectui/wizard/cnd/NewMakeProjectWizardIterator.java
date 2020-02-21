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
package org.netbeans.modules.cnd.remote.projectui.wizard.cnd;

import java.awt.Component;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.remote.ui.SelectHostWizardProvider;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.makeproject.api.wizards.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CompilerSet2Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.DevelopmentHostConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.QmakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.ui.DatabaseProjectProviderEx;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.IteratorExtension;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.ProjectWizardPanels;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.ProjectWizardPanels.NamedPanel;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import org.netbeans.modules.cnd.makeproject.spi.DatabaseProjectProvider;
import org.netbeans.modules.cnd.utils.CndLanguageStandards;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 * Wizard to create a new Make project.
 */
public class NewMakeProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator<WizardDescriptor> {

    private static final long serialVersionUID = 1L;
    public static final String APPLICATION_PROJECT_NAME = "CppApplication"; // NOI18N
    public static final String DYNAMICLIBRARY_PROJECT_NAME = "CppDynamicLibrary";  // NOI18N
    public static final String STATICLIBRARY_PROJECT_NAME = "CppStaticLibrary"; // NOI18N
    public static final String MAKEFILEPROJECT_PROJECT_NAME = "MakefileProject"; // NOI18N
    public static final String BINARY_PROJECT_NAME = "BinaryProject"; // NOI18N
    public static final String FULL_REMOTE_PROJECT_NAME = "FullRemoteProject"; // NOI18N
    public static final String QTAPPLICATION_PROJECT_NAME = "QtApplication"; // NOI18N
    public static final String QTDYNAMICLIBRARY_PROJECT_NAME = "QtDynamicLibrary"; // NOI18N
    public static final String QTSTATICLIBRARY_PROJECT_NAME = "QtStaticLibrary"; // NOI18N
    public static final String DBAPPLICATION_PROJECT_NAME = "DbApplication"; // NOI18N
    static final String PROP_NAME_INDEX = "nameIndex"; // NOI18N
    // Wizard types
    public static final int TYPE_MAKEFILE = 0;
    public static final int TYPE_APPLICATION = 1;
    public static final int TYPE_DYNAMIC_LIB = 2;
    public static final int TYPE_STATIC_LIB = 3;
    public static final int TYPE_QT_APPLICATION = 4;
    public static final int TYPE_QT_DYNAMIC_LIB = 5;
    public static final int TYPE_QT_STATIC_LIB = 6;
    public static final int TYPE_BINARY = 7;
    public static final int TYPE_DB_APPLICATION = 8;

    private final int wizardtype;

    private Boolean lastSimpleMode = null;
    private String lastHostUid = null;
    private Boolean lastSetupHost = null;

    private SelectHostWizardProvider selectHostWizardProvider;
    private WizardDescriptor.Panel<WizardDescriptor> selectHostPanel;
    private WizardDescriptor.Panel<WizardDescriptor> selectBinaryPanel;
    private int lastNewHostPanel = -1;
    
    private ProjectWizardPanels.MakeModePanel<WizardDescriptor> selectModePanel;
    private final ProjectWizardPanels.MakeSamplePanel<WizardDescriptor> panelConfigureProjectTrue;
//    private final PanelConfigureProject panelConfigureProjectFalse;
//    private final MakefileOrConfigureDescriptorPanel makefileOrConfigureDescriptorPanel;
//    private final BuildActionsDescriptorPanel buildActionsDescriptorPanel;
//    private final SourceFoldersDescriptorPanel sourceFoldersDescriptorPanel;
//    private final ParserConfigurationDescriptorPanel parserConfigurationDescriptorPanel;
    private final List<WizardDescriptor.Panel<WizardDescriptor>> advancedPanels;

    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N
    
    private NewMakeProjectWizardIterator(int wizardtype, String name, String wizardTitle, String wizardACSD, String helpCtx) {        
        this.wizardtype = wizardtype;
        name = name.replaceAll(" ", ""); // NOI18N

        panelConfigureProjectTrue = ProjectWizardPanels.getMakeSampleProjectWizardPanel(wizardtype, name, wizardTitle, wizardACSD, true, helpCtx);

//        panelConfigureProjectFalse = new PanelConfigureProject(name, wizardtype, wizardTitle, wizardACSD, false);
//        makefileOrConfigureDescriptorPanel = new MakefileOrConfigureDescriptorPanel();
//        buildActionsDescriptorPanel = new BuildActionsDescriptorPanel();
//        sourceFoldersDescriptorPanel = new SourceFoldersDescriptorPanel();
//        parserConfigurationDescriptorPanel = new ParserConfigurationDescriptorPanel();
        advancedPanels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        advancedPanels.addAll(ProjectWizardPanels.getNewProjectWizardPanels(wizardtype, name, wizardTitle, wizardACSD, false));
    }
    

    private NewMakeProjectWizardIterator(int wizardtype, String name, String wizardTitle, String wizardACSD) {
        this(wizardtype, name, wizardTitle, wizardACSD, null);
    }

    private synchronized ProjectWizardPanels.MakeModePanel<WizardDescriptor> getSelectModePanel() {
        if (selectModePanel == null) {
            selectModePanel = ProjectWizardPanels.getSelectModePanel();
            selectModePanel.addChangeListener(new ChangeListener() {

                @Override
                public void stateChanged(ChangeEvent e) {
                    setupPanelsAndStepsIfNeed();
                }
            });
        }
        return selectModePanel;
    }
    
    public static NewMakeProjectWizardIterator newApplication() {
        String name = APPLICATION_PROJECT_NAME; //getString("NativeNewApplicationName"); // NOI18N
        String wizardTitle = getString("Templates/Project/Native/newApplication.xml"); // NOI18N
        String wizardACSD = getString("NativeNewLibraryACSD"); // NOI18N
        return new NewMakeProjectWizardIterator(TYPE_APPLICATION, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator newDynamicLibrary() {
        String name = DYNAMICLIBRARY_PROJECT_NAME; //getString("NativeNewDynamicLibraryName"); // NOI18N
        String wizardTitle = getString("Templates/Project/Native/newDynamicLibrary.xml"); // NOI18N
        String wizardACSD = getString("NativeNewDynamicLibraryACSD"); // NOI18N
        return new NewMakeProjectWizardIterator(TYPE_DYNAMIC_LIB, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator newStaticLibrary() {
        String name = STATICLIBRARY_PROJECT_NAME; //getString("NativeNewStaticLibraryName");
        String wizardTitle = getString("Templates/Project/Native/newStaticLibrary.xml");
        String wizardACSD = getString("NativeNewStaticLibraryACSD");
        return new NewMakeProjectWizardIterator(TYPE_STATIC_LIB, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator newQtApplication() {
        String name = QTAPPLICATION_PROJECT_NAME;
        String wizardTitle = getString("Templates/Project/Native/newQtApplication.xml");
        String wizardACSD = getString("NativeNewQtApplicationACSD");
        return new NewMakeProjectWizardIterator(TYPE_QT_APPLICATION, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator newQtDynamicLibrary() {
        String name = QTDYNAMICLIBRARY_PROJECT_NAME;
        String wizardTitle = getString("Templates/Project/Native/newQtDynamicLibrary.xml");
        String wizardACSD = getString("NativeNewQtDynamicLibraryACSD");
        return new NewMakeProjectWizardIterator(TYPE_QT_DYNAMIC_LIB, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator newQtStaticLibrary() {
        String name = QTSTATICLIBRARY_PROJECT_NAME;
        String wizardTitle = getString("Templates/Project/Native/newQtStaticLibrary.xml");
        String wizardACSD = getString("NativeNewQtStaticLibraryACSD");
        return new NewMakeProjectWizardIterator(TYPE_QT_STATIC_LIB, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator newDBApplication(Map<String,?> inst) {
        String helpCtx = (String)inst.get("helpCtx"); //NOI18N
        String name = DBAPPLICATION_PROJECT_NAME;
        String wizardTitle = getString("Templates/Project/Native/newDBApplication.xml");
        String wizardACSD = getString("NativeNewDBApplicationACSD");
        return new NewMakeProjectWizardIterator(TYPE_DB_APPLICATION, name, wizardTitle, wizardACSD, helpCtx);
    }

    public static NewMakeProjectWizardIterator makefile() {
        String name = MAKEFILEPROJECT_PROJECT_NAME; //getString("NativeMakefileName"); // NOI18N
        String wizardTitle = getString("Templates/Project/Native/makefile.xml"); // NOI18N
        String wizardACSD = getString("NativeMakefileNameACSD"); // NOI18N
        return new NewMakeProjectWizardIterator(TYPE_MAKEFILE, name, wizardTitle, wizardACSD);
    }

    public static NewMakeProjectWizardIterator binary() {
        String name = BINARY_PROJECT_NAME;
        String wizardTitle = getString("Templates/Project/Native/binary.xml"); // NOI18N
        String wizardACSD = getString("NativeBinaryNameACSD"); // NOI18N
        return new NewMakeProjectWizardIterator(TYPE_BINARY, name, wizardTitle, wizardACSD);
    }

    private static boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        } else {
            return o1.equals(o2);
        }
    }

    private void setupPanelsAndStepsIfNeed() {        
        if (wizardtype == TYPE_APPLICATION || wizardtype == TYPE_DYNAMIC_LIB || wizardtype == TYPE_STATIC_LIB || wizardtype == TYPE_QT_APPLICATION || wizardtype == TYPE_QT_DYNAMIC_LIB || wizardtype == TYPE_QT_STATIC_LIB) {
            if (panels == null) {
                panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
                panels.add(panelConfigureProjectTrue);
                String[] steps = createSteps(panels);
            }
        } else if (wizardtype == TYPE_MAKEFILE) {
            String hostUID = (wiz == null) ? null : WizardConstants.PROPERTY_HOST_UID.get(wiz);
            Boolean setupHost = null;

            if (panels != null) {
                if (equals(lastSimpleMode, isSimple())) {
                    return;
                }
            }
            lastHostUid = hostUID;
            lastSimpleMode = Boolean.valueOf(isSimple());
            lastSetupHost = setupHost;
            lastNewHostPanel = -1;

            LOGGER.log(Level.FINE, "refreshing panels and steps");

            List<WizardDescriptor.Panel<WizardDescriptor>> panelsList = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
            final WizardDescriptor.Panel<WizardDescriptor> modeSelectionPanel = getSelectModePanel();
            panelsList.add(modeSelectionPanel);
            if (!isSimple()) {
                panelsList.addAll(advancedPanels);
            }
            panels = panelsList;
            setupSteps();
        } else if (wizardtype == TYPE_BINARY) {
            if (selectBinaryPanel == null) {
                selectBinaryPanel = ProjectWizardPanels.getSelectBinaryPanel();
            }
            if (panels == null) {
                panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
                panels.add(selectBinaryPanel);
                //panels.add(advancedPanels.get(1)); // buildActionsDescriptorPanel
                //panels.add(advancedPanels.get(2)); // sourceFoldersDescriptorPanel
                panels.add(advancedPanels.get(4)); // panelConfigureProject
                String[] steps = createSteps(panels);
            }
        } else if(wizardtype == TYPE_DB_APPLICATION) {
            if (panels == null) {
                panels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
                panelConfigureProjectTrue.setFinishPanel(false);
                panels.add(panelConfigureProjectTrue);
                DatabaseProjectProviderEx provider = Lookup.getDefault().lookup(DatabaseProjectProviderEx.class);
                if(provider != null) {
                    provider.setupAdditionalWizardPanels(panels);
                }
                String[] steps = createSteps(panels);
            }
        } else {
            throw new IllegalStateException("Illegal wizard type: " + wizardtype); //NOI18N
        }
    }

    private void setupSteps() {
        String[] steps = createSteps(panels);
        String[] advanced = new String[]{ steps[0], "..."}; // NOI18N
        Component c = panels.get(0).getComponent();
        if (c instanceof JComponent) { // assume Swing components
            JComponent jc = (JComponent) c;
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, advanced);
        }
    }

    private String[] createSteps(List<WizardDescriptor.Panel<WizardDescriptor>> panels) {
        String[] steps = new String[panels.size()];
        for (int i = 0; i < panels.size(); i++) {
            if (panels.get(i) instanceof NamedPanel) {
                steps[i] = ((NamedPanel) panels.get(i)).getName();
            } else {
                steps[i] = panels.get(i).getComponent().getName();
            }
        }
        for (int i = 0; i < panels.size(); i++) {
            Component c = panels.get(i).getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            }
        }
        return steps;
    }

    @Override
    public Set<FileObject> instantiate(ProgressHandle handle) throws IOException {
        try {
            handle.start();
            return instantiate();
        } catch (IOException ex) {
            ex.printStackTrace(System.err); // since caller doesn't report this
            throw ex;
        } finally {
            handle.finish();
        }
    }


    @Override
    public Set<FileObject> instantiate() throws IOException {
        Set<FileObject> resultSet = new HashSet<FileObject>();
        FSPath dirF = WizardConstants.PROPERTY_PROJECT_FOLDER.get(wiz);
        //do not see any reasons why to use local env here
        final ExecutionEnvironment env = WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV.get(wiz);
        String hostUID = ExecutionEnvironmentFactory.toUniqueID(env);
        CompilerSet toolchain = WizardConstants.PROPERTY_TOOLCHAIN.get(wiz);
        boolean defaultToolchain = Boolean.TRUE.equals(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT.get(wiz));
        if (dirF != null) {
            dirF = new FSPath(dirF.getFileSystem(), RemoteFileUtil.normalizeAbsolutePath(dirF.getPath(), WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV.get(wiz)));
        }
        String projectName = WizardConstants.PROPERTY_NAME.get(wiz);
        String makefileName = WizardConstants.PROPERTY_GENERATED_MAKEFILE_NAME.get(wiz);
        if (isSimple()) {
            IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            if (extension != null) {
                resultSet.addAll(extension.createProject(wiz));
            }
        } else if (wizardtype == TYPE_MAKEFILE) { // thp
            IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            if (extension != null) {
                resultSet.addAll(extension.createProject(wiz));
            }
        } else if (wizardtype == TYPE_BINARY) {
            IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
            if (extension != null) {
                IteratorExtension.ProjectKind kind = WizardConstants.PROPERTY_DEPENDENCY_KIND.get(wiz);
                if (kind == null) {
                    kind = IteratorExtension.ProjectKind.IncludeDependencies;
                }
                extension.discoverProject(wiz.getProperties(), null, kind);
                //resultSet.addAll(extension.createProject(wiz));
            }
//            String binary = WizardConstants.PROPERTY_BUILD_RESULT.get(wiz);
//            boolean trueSourceRoot = WizardConstants.PROPERTY_TRUE_SOURCE_ROOT.get(wiz);
//            List<String> dlls = WizardConstants.PROPERTY_DEPENDENCIES.get(wiz);
//            String libraries = null;
//            if (dlls != null && !dlls.isEmpty()) {
//                StringBuilder buf = new StringBuilder();
//                for(String s : dlls) {
//                    if (!s.isEmpty()) {
//                        if (buf.length()>0) {
//                            buf.append(':');
//                        }
//                        buf.append(s);
//                    }
//                }
//                libraries = buf.toString();
//            }
//
//            CreateProjectFromBinary creator = new CreateProjectFromBinary(dirF.getFileSystem(), dirF.getPath(), binary, !trueSourceRoot, libraries, ProjectKind.IncludeDependencies);
//            Project createRemoteProject = creator.createRemoteProject();
//            if (createRemoteProject != null) {
//                //resultSet.add(createRemoteProject);
//            }
        } else if (wizardtype == TYPE_APPLICATION || wizardtype == TYPE_DYNAMIC_LIB || wizardtype == TYPE_STATIC_LIB || wizardtype == TYPE_QT_APPLICATION || wizardtype == TYPE_QT_DYNAMIC_LIB || wizardtype == TYPE_QT_STATIC_LIB || wizardtype == TYPE_DB_APPLICATION) {
            int conftype = -1;
            String customizerId = null;
            if (wizardtype == TYPE_APPLICATION) {
                conftype = MakeConfiguration.TYPE_APPLICATION;
            } else if (wizardtype == TYPE_DYNAMIC_LIB) {
                conftype = MakeConfiguration.TYPE_DYNAMIC_LIB;
            } else if (wizardtype == TYPE_STATIC_LIB) {
                conftype = MakeConfiguration.TYPE_STATIC_LIB;
            } else if (wizardtype == TYPE_QT_APPLICATION) {
                conftype = MakeConfiguration.TYPE_QT_APPLICATION;
            } else if (wizardtype == TYPE_QT_DYNAMIC_LIB) {
                conftype = MakeConfiguration.TYPE_QT_DYNAMIC_LIB;
            } else if (wizardtype == TYPE_QT_STATIC_LIB) {
                conftype = MakeConfiguration.TYPE_QT_STATIC_LIB;
            } else if (wizardtype == TYPE_DB_APPLICATION) {
                conftype = MakeConfiguration.TYPE_DB_APPLICATION;
            }
            String mainFile = null;
            if (WizardConstants.PROPERTY_CREATE_MAIN_FILE.get(wiz)) { // NOI18N
                WizardConstants.PROPERTY_MAIN_FILE_NAME.get(wiz);
                String fname = WizardConstants.PROPERTY_MAIN_FILE_NAME.get(wiz);
                String template = WizardConstants.PROPERTY_MAIN_TEMPLATE_NAME.get(wiz);
                mainFile = fname + "|" + template; // NOI18N
            }
            String langStandard = WizardConstants.PROPERTY_LANGUAGE_STANDARD.get(wiz);

            MakeConfiguration debug = MakeConfiguration.createConfiguration(dirF, "Debug", conftype, customizerId,  hostUID, toolchain, defaultToolchain); // NOI18N
            debug.getCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
            debug.getCCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
            debug.getFortranCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
            debug.getAssemblerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
            debug.getQmakeConfiguration().getBuildMode().setValue(QmakeConfiguration.DEBUG_MODE);
            setupLanguageStandard(debug, langStandard);
            //debug.setRemoteMode(Mode.REMOTE_SOURCES);

            int platform = CompilerSetManager.get((env)).getPlatform();
            debug.getDevelopmentHost().setBuildPlatform(platform);
            DevelopmentHostConfiguration toolCollecctionDevelopmentHost = new DevelopmentHostConfiguration(ExecutionEnvironmentFactory.fromUniqueID(hostUID));
            CompilerSet2Configuration compilerSet2Configuration;
            if (defaultToolchain) {
                compilerSet2Configuration = new CompilerSet2Configuration(toolCollecctionDevelopmentHost);
            } else {
                CompilerSet defCS = (toolchain != null) ? toolchain : CompilerSetManager.get(env).getDefaultCompilerSet();
                compilerSet2Configuration = new CompilerSet2Configuration(toolCollecctionDevelopmentHost, defCS);
            }
            debug.setCompilerSet(compilerSet2Configuration);
            if (wizardtype == TYPE_DB_APPLICATION) {
                DatabaseProjectProvider provider = Lookup.getDefault().lookup(DatabaseProjectProvider.class);
                if(provider != null) {
                    provider.setupDebugConfiguration(debug);
                }
            }
            MakeConfiguration release = MakeConfiguration.createConfiguration(dirF, "Release", conftype, customizerId, hostUID, toolchain, defaultToolchain); // NOI18N
            release.getCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
            release.getCCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
            release.getFortranCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
            release.getAssemblerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
            release.getQmakeConfiguration().getBuildMode().setValue(QmakeConfiguration.RELEASE_MODE);
            setupLanguageStandard(release, langStandard);
            //release.setRemoteMode(Mode.REMOTE_SOURCES);
            release.getDevelopmentHost().setBuildPlatform(platform);
            release.setCompilerSet(compilerSet2Configuration);
            if (wizardtype == TYPE_DB_APPLICATION) {
                DatabaseProjectProvider provider = Lookup.getDefault().lookup(DatabaseProjectProvider.class);
                if(provider != null) {
                    provider.setupReleaseConfiguration(release);
                }
            }
            MakeConfiguration[] confs = new MakeConfiguration[]{debug, release};
            ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(projectName, dirF);
            prjParams.setMakefileName(makefileName);
            prjParams.setConfigurations(confs);
            prjParams.setMainFile(mainFile);
            prjParams.setHostUID(hostUID);

            if (wizardtype == TYPE_DB_APPLICATION) {
                Object connection = wiz.getProperties().get("connectionName"); // NOI18N
                if(connection instanceof String) {
                    prjParams.setDatabaseConnection((String)connection);
                }
            }
            prjParams.setTemplateParams(new HashMap<String, Object>(wiz.getProperties()));
            ProjectGenerator.getDefault().createProject(prjParams);
            FileObject dir = dirF.getFileObject();
            resultSet.add(dir);
        }
        
        return resultSet;
    }
    private transient int index;
    private transient List<WizardDescriptor.Panel<WizardDescriptor>> panels;
    private transient WizardDescriptor wiz;
    
//<editor-fold defaultstate="collapsed" desc="Copy from PanelProjectLocationVisual">
    private static final String[] CPP = new String[]{"C++", // NOI18N
        CndLanguageStandards.CndLanguageStandard.CPP98.toString(),
        CndLanguageStandards.CndLanguageStandard.CPP11.toString(),
        CndLanguageStandards.CndLanguageStandard.CPP14.toString(),
        CndLanguageStandards.CndLanguageStandard.CPP17.toString()
    };
    private static final String[] C = new String[]{"C", // NOI18N
        CndLanguageStandards.CndLanguageStandard.C89.toString(),
        CndLanguageStandards.CndLanguageStandard.C99.toString(),
        CndLanguageStandards.CndLanguageStandard.C11.toString()
    };
    private static final String[] FORTRAN = new String[]{"Fortran90 Fixed", // NOI18N
        "Fortran90 Free", // NOI18N
        "Fortran95", // NOI18N
        "Fortran2003", // NOI18N
        "Fortran2008" // NOI18N
    };
    
    private static Pair<String,Integer> getLanguageStandard(String value) {
        if (value == null) {
            return null;
        }
        for(int i = 0; i < C.length; i++) {
            if (value.equals(C[i])) {
                return Pair.of(C[0], i);
            }
        }
        for(int i = 0; i < CPP.length; i++) {
            if (value.equals(CPP[i])) {
                return Pair.of(CPP[0], i);
            }
        }
        for(int i = 0; i < FORTRAN.length; i++) {
            if (value.equals(FORTRAN[i])) {
                return Pair.of(FORTRAN[0], i);
            }
        }
        return null;
    }
    
    private void setupLanguageStandard(MakeConfiguration conf, String langStandard) {
        Pair<String, Integer> languageStandard = getLanguageStandard(langStandard);
        if (languageStandard != null) {
            if (C[0].equals(languageStandard.first())) {
                conf.getCCompilerConfiguration().getCStandard().setValue(languageStandard.second());
            } else if (CPP[0].equals(languageStandard.first())) {
                conf.getCCCompilerConfiguration().getCppStandard().setValue(languageStandard.second());
            } else if (FORTRAN[0].equals(languageStandard.first())) {
                //conf.getFortranCompilerConfiguration().getFortranStandard().setValue(languageStandard.second());
            }
        }
    }
//</editor-fold>
    
    @Override
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        WizardConstants.PROPERTY_SOURCE_HOST_ENV.put(wiz, getDefaultSourceEnvironment());
        index = 0;
        setupPanelsAndStepsIfNeed();
    }

    private static ExecutionEnvironment getDefaultSourceEnvironment() {
        String externalForm = System.getProperty("cnd.default.project.source.env"); //NOI18N
        if (externalForm != null) {
            ExecutionEnvironment env = ExecutionEnvironmentFactory.fromUniqueID(externalForm);
            if (env != null) {
                return env;
            }
        }
        return ExecutionEnvironmentFactory.getLocal();
    }

    @Override
    public void uninitialize(WizardDescriptor wiz) {
        if (wiz != null) {
            WizardConstants.PROPERTY_PROJECT_FOLDER.put(wiz, null);
            WizardConstants.PROPERTY_NAME.put(wiz, null);
            WizardConstants.MAIN_CLASS.put(wiz, null); // NOI18N
            if (wizardtype == TYPE_MAKEFILE) {
                wiz.putProperty("sourceRoot", null); // NOI18N
            }
            this.wiz = null;
            panels = null;
        }
    }

    @Override
    public String name() {
        return NbBundle.getMessage(NewMakeProjectWizardIterator.class, "LAB_IteratorName",Integer.valueOf(index + 1), Integer.valueOf(panels.size())); //NOI18N
    }

    private boolean isSimple() {
        return wizardtype == TYPE_MAKEFILE && wiz != null && Boolean.TRUE.equals(WizardConstants.PROPERTY_SIMPLE_MODE.get(wiz));
    }

    @Override
    public boolean hasNext() {
        setupPanelsAndStepsIfNeed();
        boolean result = index < panels.size() - 1;
        LOGGER.log(Level.FINE, "hasNext()=={0} (index=={1}, panels.length=={2})", new Object[]{result, index, panels.size()});
        return result;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) { // will call setupPanelsAndStepsIfNeed();
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        setupPanelsAndStepsIfNeed();
        return panels.get(index);
    }

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }
    
    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    private void fireStateChanged() {
        ChangeEvent event = new ChangeEvent(this);
        ChangeListener[] listenersCopy;
        synchronized (listeners) {
            listenersCopy = listeners.toArray(new ChangeListener[listeners.size()]);
        }
        for (ChangeListener listener : listenersCopy) {
            listener.stateChanged(event);
        }
    }

    /** Look up i18n strings here */
    private static ResourceBundle bundle;

    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(NewMakeProjectWizardIterator.class);
        }
        return bundle.getString(s);
    }
}
