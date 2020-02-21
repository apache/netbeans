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

package org.netbeans.modules.cnd.discovery.projectimport;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.model.CsmListeners;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProgressListener;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface.Applicable;
import org.netbeans.modules.cnd.discovery.wizard.DiscoveryExtension;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.support.DiscoveryProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.wizards.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem.ProjectItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.Env;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.api.wizards.CommonUtilities;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.IteratorExtension;
import org.netbeans.modules.cnd.makeproject.api.wizards.DefaultMakeProjectLocationProvider;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * 
 */
public class CreateDependencies implements PropertyChangeListener {
    private static final RequestProcessor RP = new RequestProcessor(ImportExecutable.class.getName(), 1);
    private final Project mainProject;
    private final List<String> dependencies;
    private List<String> paths;
    private final List<String> searchPaths;
    private final String binary;
    private final Map<Project, String> createdProjects = new HashMap<>();
    private MakeConfigurationDescriptor mainConfigurationDescriptor;
    private final CsmModel model = CsmModelAccessor.getModel();
    private final IteratorExtension extension = Lookup.getDefault().lookup(IteratorExtension.class);
    private final FileSystem sourceFileSystem;

    public CreateDependencies(Project mainProject, FileSystem sourceFileSystem, List<String> dependencies, List<String> paths, List<String> searchPaths, String binary) {
        this.mainProject = mainProject;
        this.dependencies = dependencies;
        this.paths = paths;
        this.searchPaths = searchPaths;
        this.binary = binary;
        this.sourceFileSystem = sourceFileSystem;
    }

    public void create() {
        ConfigurationDescriptorProvider pdp = mainProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        if (!pdp.gotDescriptor()) {
            return;
        }
        mainConfigurationDescriptor = pdp.getConfigurationDescriptor();
        if (paths == null) {
            if (dependencies == null || dependencies.isEmpty()) {
                return;
            }
            Set<String> checkedDll = new HashSet<>();
            checkedDll.add(binary);
            Map<String,String> dllPaths = new HashMap<>();
            String root = ImportExecutable.findFolderPath(mainConfigurationDescriptor, ImportExecutable.getRoot(mainConfigurationDescriptor));
            if (root != null) {
                MakeConfiguration activeConfiguration = mainConfigurationDescriptor.getActiveConfiguration();
                String ldLibPath = CommonUtilities.getLdLibraryPath(activeConfiguration);
                ldLibPath = CommonUtilities.addSearchPaths(ldLibPath, searchPaths, binary);
                for(String dll : dependencies) {
                    dllPaths.put(dll, ImportExecutable.findLocation(sourceFileSystem, dll, ldLibPath));
                }
                while(true) {
                    List<String> secondary = new ArrayList<>();
                    for(Map.Entry<String,String> entry : dllPaths.entrySet()) {
                        if (entry.getValue() != null) {
                            if (!checkedDll.contains(entry.getValue())) {
                                checkedDll.add(entry.getValue());
                                final Map<String, Object> extMap = new HashMap<>();
                                DiscoveryDescriptor.BUILD_RESULT.toMap(extMap, entry.getValue());
                                DiscoveryDescriptor.RESOLVE_SYMBOLIC_LINKS.toMap(extMap, MakeProjectOptions.getResolveSymbolicLinks());
                                if (extension != null) {
                                    extension.discoverArtifacts(extMap);
                                    List<String> dlls = DiscoveryDescriptor.DEPENDENCIES.fromMap(extMap);
                                    if (dlls != null) {
                                        for(String so : dlls) {
                                            if (!dllPaths.containsKey(so)) {
                                                secondary.add(so);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    for(String so : secondary) {
                        dllPaths.put(so, ImportExecutable.findLocation(sourceFileSystem, so, ldLibPath));
                    }
                    int search = 0;
                    for(Map.Entry<String,String> entry : dllPaths.entrySet()) {
                        if (entry.getValue() == null) {
                            search++;
                        }
                    }
                    if (search > 0 && root.length() > 1) {
                        FileObject rootFO = sourceFileSystem.findResource(root);
                        if (rootFO != null && rootFO.isValid()) {
                            ImportExecutable.gatherSubFolders(rootFO, new HashSet<String>(), dllPaths);
                        }
                    }
                    int newSearch = 0;
                    for(Map.Entry<String,String> entry : dllPaths.entrySet()) {
                        if (entry.getValue() == null) {
                            newSearch++;
                        }
                    }
                    if (newSearch == search && secondary.isEmpty()) {
                        break;
                    }
                }
            }
            paths = new ArrayList<>();
            for(Map.Entry<String, String> entry : dllPaths.entrySet()) {
                if (entry.getValue() != null) {
                    if (ImportExecutable.isMyDll(entry.getValue(), root)) {
                        paths.add(entry.getValue());
                    }
                }
            }
        }
        for(String  entry : paths) {
            try {
                Project createProject = createProject(entry, "", "", ""); // NOI18N
                createdProjects.put(createProject, entry);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (!createdProjects.isEmpty()) {
            OpenProjects.getDefault().addPropertyChangeListener(this);
            Project[] toOpen = new Project[createdProjects.size()];
            int i = 0;
            for(Project p : createdProjects.keySet()) {
                toOpen[i] = p;
                ImportExecutable.switchModel(model, false, p);
                i++;
            }
            OpenProjects.getDefault().open(toOpen, false);
        }
    }

    private static void updateRunProfile(String baseDir, RunProfile runProfile, String arguments, String dir, String envText) {
        // Arguments
        runProfile.setArgs(arguments);
        // Working dir
        String wd = dir;
        wd = CndPathUtilities.toRelativePath(baseDir, wd);
        wd = CndPathUtilities.normalizeSlashes(wd);
        runProfile.setRunDirectory(wd);
        // Environment
        Env env = runProfile.getEnvironment();
	env.removeAll();
        env.decode(envText);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
            if (evt.getNewValue() instanceof Project[]) {
                Project[] projects = (Project[])evt.getNewValue();
                if (projects.length == 0) {
                    return;
                }
                for(Project aProject : projects) {
                    if (createdProjects.containsKey(aProject)){
                        addReqProject(aProject);
                    }
                }
                DiscoveryProjectGenerator.saveMakeConfigurationDescriptor(mainProject, null);
                for(Project aProject : projects) {
                    String executable;
                    if (createdProjects.containsKey(aProject)){
                        executable = createdProjects.get(aProject);
                        createdProjects.remove(aProject);
                    } else {
                        continue;
                    }
                    if (extension != null) {
                        Map<String, Object> map = new HashMap<>();
                        DiscoveryDescriptor.BUILD_RESULT.toMap(map, executable);
                        DiscoveryDescriptor.ROOT_FOLDER.toMap(map, aProject.getProjectDirectory().getPath());
                        DiscoveryDescriptor.RESOLVE_SYMBOLIC_LINKS.toMap(map, MakeProjectOptions.getResolveSymbolicLinks());
                        process((DiscoveryExtension)extension, aProject, map);
                    }
                }
                if (createdProjects.isEmpty()) {
                    OpenProjects.getDefault().removePropertyChangeListener(this);
                }
            }
        }
    }

    private void addReqProject(Project lastSelectedProject) {
        ConfigurationDescriptorProvider provider = lastSelectedProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
        MakeConfigurationDescriptor configurationDescriptor = provider.getConfigurationDescriptor();
        mainConfigurationDescriptor.getActiveConfiguration().getRequiredProjectsConfiguration().add(
                new ProjectItem(new MakeArtifact(configurationDescriptor, configurationDescriptor.getActiveConfiguration())));
    }

    private void process(final DiscoveryExtension extension, final Project lastSelectedProject, final Map<String, Object> map){
        RP.post(new Runnable() {

            @Override
            public void run() {
                ProgressHandle progress = ProgressHandleFactory.createHandle(NbBundle.getMessage(ImportExecutable.class, "ImportExecutable.Progress")); // NOI18N
                progress.start();
                try {
                    ConfigurationDescriptorProvider provider = lastSelectedProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
                    MakeConfigurationDescriptor configurationDescriptor = provider.getConfigurationDescriptor();
                    for(Folder folder : configurationDescriptor.getLogicalFolders().getFolders()) {
                        if (MakeConfigurationDescriptor.HEADER_FILES_FOLDER.equals(folder.getName()) ||
                            MakeConfigurationDescriptor.RESOURCE_FILES_FOLDER.equals(folder.getName())) {
                            configurationDescriptor.getLogicalFolders().removeFolderAction(folder);
                        }
                    }
                    Applicable applicable = extension.isApplicable(map, lastSelectedProject, false);
                    if (applicable.isApplicable()) {
                        ImportExecutable.resetCompilerSet(configurationDescriptor.getActiveConfiguration(), applicable);
                        configurationDescriptor.getActiveConfiguration().getCodeAssistanceConfiguration().getResolveSymbolicLinks().setValue(MakeProjectOptions.getResolveSymbolicLinks());
                        if (extension.canApply(map, lastSelectedProject, null)) {
                            try {
                                extension.apply(map, lastSelectedProject, null);
                                DiscoveryProjectGenerator.saveMakeConfigurationDescriptor(lastSelectedProject, null);
                            } catch (IOException ex) {
                                ex.printStackTrace(System.err);
                            }
                        }
                    }
                    onProjectParsingFinished(lastSelectedProject);
                } catch (Throwable ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    progress.finish();
                }
            }
        });
    }
    private static final List<CsmProgressListener> listeners = new ArrayList<>(1);

    private void onProjectParsingFinished(final Project makeProject) {
        if (makeProject != null) {
            final NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            CsmProgressListener listener = new CsmProgressAdapter() {

                @Override
                public void projectParsingFinished(CsmProject project) {
                    final Object id = project.getPlatformProject();
                    if (id != null && id.equals(np)) {
                        CsmListeners.getDefault().removeProgressListener(this);
                        listeners.remove(this);
                        DiscoveryProjectGenerator.fixExcludedHeaderFiles(makeProject, ImportProject.logger);
                    }
                }
            };
            listeners.add(listener);
            CsmListeners.getDefault().addProgressListener(listener);
            ImportExecutable.switchModel(model, true, makeProject);
        }
    }
    
    private Project createProject(String executablePath, String arguments, String dir, String envText) throws IOException {
        Project project;
        String projectParentFolder = DefaultMakeProjectLocationProvider.getDefault().getDefaultProjectFolder();
        String projectName = ProjectGenerator.getDefault().getValidProjectName(projectParentFolder, CndPathUtilities.getBaseName(executablePath));
        String baseDir = projectParentFolder + CndFileUtils.getFileSeparatorChar(sourceFileSystem) + projectName;
        MakeConfiguration conf =  MakeConfiguration.createDefaultHostMakefileConfiguration(baseDir, "Default"); // NOI18N
        // Working dir
        String wd = CndPathUtilities.getDirName(executablePath);
        wd = CndPathUtilities.toRelativePath(baseDir, wd);
        wd = CndPathUtilities.normalizeSlashes(wd);
        conf.getMakefileConfiguration().getBuildCommandWorkingDir().setValue(wd);
        // Executable
        String exe = executablePath;
        exe = CndPathUtilities.toRelativePath(baseDir, exe);
        exe = CndPathUtilities.normalizeSlashes(exe);
        conf.getMakefileConfiguration().getOutput().setValue(exe);
        updateRunProfile(baseDir, conf.getProfile(), arguments, dir, envText);
        FSPath projectFolder = new FSPath(sourceFileSystem, projectParentFolder+CndFileUtils.getFileSeparatorChar(sourceFileSystem)+projectName);
        ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(projectName, projectFolder);
        prjParams.setOpenFlag(false).setConfiguration(conf).setImportantFiles(Collections.<String>singletonList(exe).iterator());
        project = ProjectGenerator.getDefault().createBlankProject(prjParams);
        return project;
    }
}
