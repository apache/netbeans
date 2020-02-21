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

package org.netbeans.modules.cnd.discovery.wizard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.discovery.api.ApplicableImpl;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProviderFactory;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderPropertyType;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.discovery.projectimport.ImportExecutable;
import org.netbeans.modules.cnd.discovery.projectimport.ImportProject;
import org.netbeans.modules.cnd.discovery.services.DiscoveryManagerImpl;
import org.netbeans.modules.cnd.discovery.wizard.SelectConfigurationPanel.MyProgress;
import org.netbeans.modules.cnd.discovery.wizard.api.ConfigurationFactory;
import org.netbeans.modules.cnd.discovery.wizard.api.ConsolidationStrategy;
import org.netbeans.modules.cnd.discovery.wizard.api.DiscoveryDescriptor;
import org.netbeans.modules.cnd.discovery.wizard.api.ProjectConfiguration;
import org.netbeans.modules.cnd.discovery.wizard.support.impl.DiscoveryProjectGeneratorImpl;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.IteratorExtension;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.makeproject.api.ui.wizard.IteratorExtension.class)
public class DiscoveryExtension implements IteratorExtension, DiscoveryExtensionInterface {
    public static final String DWARF_PROVIDER = "dwarf-executable";// NOI18N
    public static final String FOLDER_PROVIDER = "dwarf-folder"; // NOI18N
    public static final String MAKE_LOG_PROVIDER = "make-log"; // NOI18N
    public static final String EXEC_LOG_PROVIDER = "exec-log"; // NOI18N
    public static final String MODEL_FOLDER_PROVIDER = "model-folder"; // NOI18N

    /** Creates a new instance of DiscoveryExtension */
    public DiscoveryExtension() {
    }

    @Override
    public void discoverArtifacts(Map<String, Object> map) {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(map);
        Applicable applicable = isApplicable(descriptor, null, false);
        if (applicable.isApplicable()) {
            descriptor.setCompilerName(applicable.getCompilerName());
            descriptor.setDependencies(applicable.getDependencies());
            descriptor.setSearchPaths(applicable.getSearchPaths());
            descriptor.setRootFolder(applicable.getSourceRoot());
            descriptor.setErrors(applicable.getErrors());
        } else {
            descriptor.setErrors(applicable.getErrors());
        }
    }

    @Override
    public Set<FileObject> createProject(WizardDescriptor wizard) throws IOException{
        return new ImportProject(wizard).create();
    }

    @Override
    public void apply(Map<String, Object> map, Project project) throws IOException {
        apply(map, project, null);
    }

    @Override
    public void apply(Map<String, Object> map, Project project, Interrupter interrupter) throws IOException {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(map);
        descriptor.setProject(project);
        DiscoveryProjectGeneratorImpl generator = new DiscoveryProjectGeneratorImpl(descriptor);
        generator.makeProject();
    }

    public DiscoveryExtensionInterface.Applicable isApplicable(DiscoveryDescriptor descriptor, Interrupter interrupter, boolean findMain) {
        Progress progress = new MyProgress(NbBundle.getMessage(DiscoveryExtension.class, "AnalyzingProjectProgress"));
        progress.start(0);
        try {
            List<String> errors = new  ArrayList<>();
            DiscoveryExtensionInterface.Applicable applicable;
            applicable = isApplicableExecLog(descriptor);
            if (applicable.isApplicable()){
                return applicable;
            }
            applicable = isApplicableDwarfExecutable(descriptor, findMain);
            if (applicable.isApplicable()){
                return applicable;
            }
            if (applicable.getErrors() != null) {
                errors.addAll(applicable.getErrors());
            }
            applicable = isApplicableMakeLog(descriptor);
            if (applicable.isApplicable()){
                return applicable;
            }
            if (applicable.getErrors() != null) {
                errors.addAll(applicable.getErrors());
            }
            applicable = isApplicableDwarfFolder(descriptor, interrupter);
            if (applicable.isApplicable()){
                return applicable;
            }
            if (applicable.getErrors() != null) {
                errors.addAll(applicable.getErrors());
            }
            if (!errors.isEmpty()) {
                return ApplicableImpl.getNotApplicable(errors);
            } else {
                return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "NoExecutable_NoBaseFolder"))); // NOI18N
            }
        } finally {
            progress.done();
        }
    }

    private DiscoveryExtensionInterface.Applicable isApplicableDwarfExecutable(DiscoveryDescriptor descriptor, boolean findMain){
        String selectedExecutable = descriptor.getBuildResult();
        if (selectedExecutable == null) {
            return ApplicableImpl.getNotApplicable(null);
        }
        FileSystem fileSystem = descriptor.getFileSystem();
        if (fileSystem == null) {
            fileSystem = FileSystemProvider.getFileSystem(ExecutionEnvironmentFactory.getLocal());
        }
        FileObject file = new FSPath(fileSystem, selectedExecutable).getFileObject();
        if (file == null || !file.isValid()) {
            return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "NotFoundExecutable",selectedExecutable))); // NOI18N
        }
        ProjectProxy proxy = new ProjectProxyImpl(descriptor);
        DiscoveryProvider provider = DiscoveryProviderFactory.findProvider(DiscoveryExtension.DWARF_PROVIDER);
        if (provider != null && provider.isApplicable(proxy)){
            ProviderPropertyType.ExecutablePropertyType.setProperty(provider, selectedExecutable);
            String aditionalLibraries = descriptor.getAditionalLibraries();
            if (aditionalLibraries == null || aditionalLibraries.isEmpty()) {
                ProviderPropertyType.LibrariesPropertyType.setProperty(provider, new String[0]);
            } else {
                ProviderPropertyType.LibrariesPropertyType.setProperty(provider, aditionalLibraries.split(";")); //NOI18N
            }
            ProviderPropertyType.BinaryFileSystemPropertyType.setProperty(provider, descriptor.getFileSystem());
            ProviderPropertyType.FindMainPropertyType.setProperty(provider, findMain);
            Applicable canAnalyze = provider.canAnalyze(proxy, null);
            if (canAnalyze.isApplicable()){
                descriptor.setProvider(provider);
                return canAnalyze;
            } else {
                if (canAnalyze.getErrors().size() > 0) {
                    return ApplicableImpl.getNotApplicable(canAnalyze.getErrors());
                } else {
                    return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "CannotAnalyzeExecutable",selectedExecutable))); // NOI18N
                }
            }
        }
        return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "NotFoundDiscoveryProvider"))); // NOI18N
    }

    private DiscoveryExtensionInterface.Applicable  isApplicableDwarfFolder(DiscoveryDescriptor descriptor, Interrupter interrupter){
        String buildFolder = descriptor.getBuildFolder();
        if (buildFolder == null) {
            buildFolder = descriptor.getRootFolder();
        }
        if (buildFolder == null) {
            return ApplicableImpl.getNotApplicable(null);
        }
        ProjectProxy proxy = new ProjectProxyImpl(descriptor);
        DiscoveryProvider provider = DiscoveryProviderFactory.findProvider(DiscoveryExtension.FOLDER_PROVIDER);
        if (provider != null && provider.isApplicable(proxy)){
            ProviderPropertyType.ExecutableFolderPropertyType.setProperty(provider, buildFolder);
            Applicable canAnalyze = provider.canAnalyze(proxy, interrupter);
            if (canAnalyze.isApplicable()){
                descriptor.setProvider(provider);
                return canAnalyze;
            } else {
                if (canAnalyze.getErrors().size() > 0) {
                    return ApplicableImpl.getNotApplicable(canAnalyze.getErrors());
                } else {
                    return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "CannotAnalyzeFolder",buildFolder))); // NOI18N
                }
            }
        }
        return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "NotFoundDiscoveryProvider"))); // NOI18N
    }

    private DiscoveryExtensionInterface.Applicable  isApplicableMakeLog(DiscoveryDescriptor descriptor){
        String rootFolder = descriptor.getRootFolder();
        if (rootFolder == null) {
            return ApplicableImpl.getNotApplicable(null);
        }
        String logFile = descriptor.getBuildLog();
        ProjectProxy proxy = new ProjectProxyImpl(descriptor);
        DiscoveryProvider provider = DiscoveryProviderFactory.findProvider(DiscoveryExtension.MAKE_LOG_PROVIDER);
        if (provider != null && provider.isApplicable(proxy)){
            ProviderPropertyType.MakeLogPropertyType.setProperty(provider, logFile);
            Applicable canAnalyze = provider.canAnalyze(proxy, null);
            if (canAnalyze.isApplicable()){
                descriptor.setProvider(provider);
                return canAnalyze;
            } else {
                if (canAnalyze.getErrors().size() > 0) {
                    return ApplicableImpl.getNotApplicable(canAnalyze.getErrors());
                } else {
                    return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "CannotAnalyzeBuildLog",logFile))); // NOI18N
                }
            }
        }
        return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "NotFoundDiscoveryProvider"))); // NOI18N
    }

    private DiscoveryExtensionInterface.Applicable  isApplicableExecLog(DiscoveryDescriptor descriptor){
        String rootFolder = descriptor.getRootFolder();
        if (rootFolder == null) {
            return ApplicableImpl.getNotApplicable(null);
        }
        String logFile = descriptor.getExecLog();
        if (logFile == null) {
            logFile = descriptor.getBuildLog();
        }
        ProjectProxy proxy = new ProjectProxyImpl(descriptor);
        DiscoveryProvider provider = DiscoveryProviderFactory.findProvider(DiscoveryExtension.EXEC_LOG_PROVIDER);
        if (provider != null) {
            ProviderPropertyType.ExecLogPropertyType.setProperty(provider, logFile);
            if (provider.isApplicable(proxy)){
                Applicable canAnalyze = provider.canAnalyze(proxy, null);
                if (canAnalyze.isApplicable()){
                    descriptor.setProvider(provider);
                    return canAnalyze;
                } else {
                    if (canAnalyze.getErrors().size() > 0) {
                        return ApplicableImpl.getNotApplicable(canAnalyze.getErrors());
                    } else {
                        return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "CannotAnalyzeBuildLog",logFile))); // NOI18N
                    }
                }
            }
        }
        return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(DiscoveryExtension.class, "NotFoundDiscoveryProvider"))); // NOI18N
    }

    public DiscoveryExtensionInterface.Applicable isApplicable(Map<String,Object> map, Project project, boolean findMain) {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(map);
        return isApplicable(descriptor, null, findMain);
    }

    boolean canApply(DiscoveryDescriptor descriptor, Interrupter interrupter) {
        if (!isApplicable(descriptor, interrupter, false).isApplicable()){
            return false;
        }
        DiscoveryProvider provider = descriptor.getProvider();
        if (provider == null){
            return false;
        }
        if (DiscoveryExtension.DWARF_PROVIDER.equals(provider.getID())){
            String selectedExecutable = descriptor.getBuildResult();
            String additional = descriptor.getAditionalLibraries();
            ProviderPropertyType.ExecutablePropertyType.setProperty(provider, selectedExecutable);
            ProviderPropertyType.FindMainPropertyType.setProperty(provider, Boolean.TRUE);
            if (additional != null && additional.length()>0){
                List<String> list = new ArrayList<>();
                StringTokenizer st = new StringTokenizer(additional,";");  // NOI18N
                while(st.hasMoreTokens()){
                    list.add(st.nextToken());
                }
                ProviderPropertyType.LibrariesPropertyType.setProperty(provider, list.toArray(new String[list.size()]));
            } else {
                ProviderPropertyType.LibrariesPropertyType.setProperty(provider, new String[0]);
            }
        } else if (DiscoveryExtension.FOLDER_PROVIDER.equals(provider.getID())){
            String buildFolder = descriptor.getBuildFolder();
            if (buildFolder == null) {
                buildFolder = descriptor.getRootFolder();
            }
            ProviderPropertyType.ExecutableFolderPropertyType.setProperty(provider, buildFolder);
        } else if (DiscoveryExtension.MAKE_LOG_PROVIDER.equals(provider.getID())){
        } else if (DiscoveryExtension.EXEC_LOG_PROVIDER.equals(provider.getID())){
        } else {
            return false;
        }
        buildModel(descriptor, interrupter);
        if (interrupter != null && interrupter.cancelled()) {
            return false;
        }
        return !descriptor.isInvokeProvider()
            && descriptor.getConfigurations() != null && descriptor.getConfigurations().size() > 0
            && descriptor.getIncludedFiles() != null;
    }

    public static void buildModel(final DiscoveryDescriptor wizardDescriptor, Interrupter interrupter){
        String rootFolder = wizardDescriptor.getRootFolder();
        DiscoveryProvider provider = wizardDescriptor.getProvider();
        List<Configuration> configs = provider.analyze(new ProjectProxy() {
            @Override
            public boolean createSubProjects() {
                return false;
            }
            @Override
            public Project getProject() {
                return wizardDescriptor.getProject();
            }

            @Override
            public String getMakefile() {
                return null;
            }

            @Override
            public String getSourceRoot() {
                return wizardDescriptor.getRootFolder();
            }

            @Override
            public String getExecutable() {
                return wizardDescriptor.getBuildResult();
            }

            @Override
            public String getWorkingFolder() {
                return null;
            }

            @Override
            public boolean mergeProjectProperties() {
                return wizardDescriptor.isIncrementalMode();
            }

            @Override
            public boolean resolveSymbolicLinks() {
                return wizardDescriptor.isResolveSymbolicLinks();
            }

        }, new MyProgress(NbBundle.getMessage(DiscoveryExtension.class, "AnalyzingProjectProgress")), interrupter);
        if (interrupter != null && interrupter.cancelled()) {
            return;
        }
        MyProgress myProgress = new MyProgress(NbBundle.getMessage(DiscoveryExtension.class, "BuildCodeAssistanceProgress"));
        try {
            myProgress.start();
            List<ProjectConfiguration> projectConfigurations = new ArrayList<>();
            List<String> includedFiles = new ArrayList<>();
            wizardDescriptor.setIncludedFiles(includedFiles);
            Map<String, AtomicInteger> compilers = new HashMap<>();
            Set<String> dep = new HashSet<>();
            Set<String> buildArtifacts = new HashSet<>();
            Map<ItemProperties.LanguageKind, Map<String, Integer>> buildTools = new HashMap<ItemProperties.LanguageKind, Map<String, Integer>>();
            for (Iterator<Configuration> it = configs.iterator(); it.hasNext();) {
                Configuration conf = it.next();
                includedFiles.addAll(conf.getIncludedFiles());
                List<ProjectProperties> langList = conf.getProjectConfiguration();
                for (Iterator<ProjectProperties> it2 = langList.iterator(); it2.hasNext();) {
                    ProjectConfiguration project = ConfigurationFactory.makeRoot(it2.next(), rootFolder);
                    ConsolidationStrategy.consolidateModel(project);
                    projectConfigurations.add(project);
                }
                for (SourceFileProperties source : conf.getSourcesConfiguration()) {
                    String compiler = source.getCompilerName();
                    if (compiler != null) {
                        AtomicInteger count = compilers.get(compiler);
                        if (count == null) {
                            count = new AtomicInteger();
                            compilers.put(compiler, count);
                        }
                        count.incrementAndGet();
                    }
                }
                if (conf.getDependencies() != null) {
                    dep.addAll(conf.getDependencies());
                }
                if (conf.getBuildArtifacts() != null) {
                    buildArtifacts.addAll(conf.getBuildArtifacts());
                }
                if (conf.getBuildTools() != null) {
                    Map<ItemProperties.LanguageKind, Map<String, Integer>> tools = conf.getBuildTools();
                    for(Map.Entry<ItemProperties.LanguageKind, Map<String, Integer>> entry : tools.entrySet()) {
                        Map<String, Integer> old = buildTools.get(entry.getKey());
                        if (old == null) {
                            buildTools.put(entry.getKey(), entry.getValue());
                        } else {
                            //merge maps
                            for (Map.Entry<String, Integer> e : entry.getValue().entrySet()) {
                                Integer count = old.get(e.getKey());
                                if (count == null) {
                                    old.put(e.getKey(), e.getValue());
                                } else {
                                    old.put(e.getKey(), count + e.getValue());
                                }
                            }
                        }
                    }
                }
            }
            wizardDescriptor.setInvokeProvider(false);
            wizardDescriptor.setDependencies(new ArrayList<>(dep));
            wizardDescriptor.setBuildArtifacts(new ArrayList<>(buildArtifacts));
            wizardDescriptor.setBuildTools(buildTools);
            wizardDescriptor.setConfigurations(projectConfigurations);
            int max = 0;
            String top = "";
            for(Map.Entry<String, AtomicInteger> entry : compilers.entrySet()){
                if (entry.getValue().get() > max) {
                    max = entry.getValue().get();
                    top = entry.getKey();
                }
            }
            wizardDescriptor.setCompilerName(top);
        } finally {
            myProgress.done();
        }
    }

    @Override
    public boolean canApply(Map<String, Object> map, Project project) {
        return canApply(map, project, null);
    }

    @Override
    public boolean canApply(Map<String, Object> map, Project project, Interrupter interrupter) {
        DiscoveryDescriptor descriptor = DiscoveryWizardDescriptor.adaptee(map);
        descriptor.setProject(project);
        return canApply(descriptor, interrupter);
    }

    @Override
    public void discoverProject(final Map<String, Object> map, final Project lastSelectedProject, ProjectKind projectKind) {
        ImportExecutable importer = new ImportExecutable(map, lastSelectedProject, projectKind);
        if (lastSelectedProject != null) {
            importer.process(this);
        }
    }

    @Override
    public void discoverHeadersByModel(Project project) {
        DiscoveryManagerImpl.discoverHeadersByModel(project);
    }

    @Override
    public void disableModel(Project makeProject) {
        final CsmModel model = CsmModelAccessor.getModel();
        if (model != null && makeProject != null) {
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            model.disableProject(np);
        }
    }

    private static class ProjectProxyImpl implements ProjectProxy {

        private final DiscoveryDescriptor descriptor;

        private ProjectProxyImpl(DiscoveryDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public boolean createSubProjects() {
            return false;
        }

        @Override
        public Project getProject() {
            return null;
        }

        @Override
        public String getMakefile() {
            return null;
        }

        @Override
        public String getSourceRoot() {
            return descriptor.getRootFolder();
        }

        @Override
        public String getExecutable() {
            return descriptor.getBuildResult();
        }

        @Override
        public String getWorkingFolder() {
            return null;
        }

        @Override
        public boolean mergeProjectProperties() {
            return false;
        }

        @Override
        public boolean resolveSymbolicLinks() {
            return descriptor.isResolveSymbolicLinks();
        }
    };

}
