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

package org.netbeans.modules.cnd.dwarfdiscovery.provider;

import org.netbeans.modules.cnd.dwarfdiscovery.RemoteJavaExecution;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.cnd.discovery.api.ApplicableImpl;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.discovery.api.DiscoveryUtils;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectImpl;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.ProviderPropertyType;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.dwarfdump.source.SourceFile;
import org.netbeans.modules.cnd.dwarfdump.reader.ElfReader;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

/**
 *
 */
public class AnalyzeExecutable extends BaseDwarfProvider {
    public static final String EXECUTABLE_PROVIDER_ID = "dwarf-executable"; // NOI18N
    private final Map<String,ProviderProperty> myProperties = new LinkedHashMap<String,ProviderProperty>();
    private final ProviderProperty<String> EXECUTABLE_PROPERTY;
    private final ProviderProperty<String[]> LIBRARIES_PROPERTY;
    private final ProviderProperty<Boolean> FIND_MAIN_PROPERTY;

    public AnalyzeExecutable() {
        myProperties.clear();
        EXECUTABLE_PROPERTY = new ProviderProperty<String>(){
            private String myPath;
            @Override
            public String getName() {
                return i18n("Executable_Files_Name"); // NOI18N
            }
            @Override
            public String getDescription() {
                return i18n("Executable_Files_Description"); // NOI18N
            }
            @Override
            public String getValue() {
                return myPath;
            }
            @Override
            public void setValue(String value) {
                myPath = value;
            }
            @Override
            public ProviderPropertyType<String> getPropertyType() {
                return ProviderPropertyType.ExecutablePropertyType;
            }
        };
        myProperties.put(EXECUTABLE_PROPERTY.getPropertyType().key(), EXECUTABLE_PROPERTY);
        
        LIBRARIES_PROPERTY = new ProviderProperty<String[]>(){
            private String myPath[];
            @Override
            public String getName() {
                return i18n("Libraries_Files_Name"); // NOI18N
            }
            @Override
            public String getDescription() {
                return i18n("Libraries_Files_Description"); // NOI18N
            }
            @Override
            public String[] getValue() {
                return myPath;
            }
            @Override
            public void setValue(String[] value) {
                myPath = value;
            }
            @Override
            public ProviderPropertyType<String[]> getPropertyType() {
                return ProviderPropertyType.LibrariesPropertyType;
            }
        };
        myProperties.put(LIBRARIES_PROPERTY.getPropertyType().key(), LIBRARIES_PROPERTY);
        
        FIND_MAIN_PROPERTY = new ProviderProperty<Boolean>(){
            private Boolean findMain = Boolean.TRUE;
            @Override
            public String getName() {
                return ""; // NOI18N
            }
            @Override
            public String getDescription() {
                return ""; // NOI18N
            }
            @Override
            public Boolean getValue() {
                return findMain;
            }
            @Override
            public void setValue(Boolean value) {
                findMain = value;
            }
            @Override
            public ProviderPropertyType<Boolean> getPropertyType() {
                return ProviderPropertyType.FindMainPropertyType;
            }
        };
        myProperties.put(FIND_MAIN_PROPERTY.getPropertyType().key(), FIND_MAIN_PROPERTY);
        // inherited properties
        myProperties.put(BYNARY_FILESYSTEM_PROPERTY.getPropertyType().key(), BYNARY_FILESYSTEM_PROPERTY);
        myProperties.put(RESTRICT_SOURCE_ROOT_PROPERTY.getPropertyType().key(), RESTRICT_SOURCE_ROOT_PROPERTY);
        myProperties.put(RESTRICT_COMPILE_ROOT_PROPERTY.getPropertyType().key(), RESTRICT_COMPILE_ROOT_PROPERTY);
    }
    
    @Override
    public String getID() {
        return EXECUTABLE_PROVIDER_ID; // NOI18N
    }
    
    @Override
    public String getName() {
        return i18n("Executable_Provider_Name"); // NOI18N
    }
    
    @Override
    public String getDescription() {
        return i18n("Executable_Provider_Description"); // NOI18N
    }
    
    @Override
    public List<String> getPropertyKeys() {
        return new ArrayList<String>(myProperties.keySet());
    }
    
    @Override
    public ProviderProperty getProperty(String key) {
        return myProperties.get(key);
    }
    
    @Override
    public DiscoveryExtensionInterface.Applicable canAnalyze(ProjectProxy project, Interrupter interrupter) {
        init(project);
        String set = EXECUTABLE_PROPERTY.getValue();
        if (set == null || set.length() == 0) {
            return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(AnalyzeExecutable.class, "NoExecutable")));
        }
        String[] additionalLibs = LIBRARIES_PROPERTY.getValue();
        boolean findMain = FIND_MAIN_PROPERTY.getValue();
        Set<String> dlls = new HashSet<String>();
        FileSystem fs = BYNARY_FILESYSTEM_PROPERTY.getValue();
        if (fs == null || CndFileUtils.isLocalFileSystem(fs)) {
            ArrayList<String> list = new ArrayList<String>();
            list.add(set);
            if (additionalLibs != null) {
                for(String l : additionalLibs) {
                    list.add(l);
                }
            }
            ApplicableImpl applicable = sizeComilationUnit(project, list, dlls, findMain);
            if (applicable.isApplicable()) {
                return new ApplicableImpl(true, applicable.getErrors(), applicable.getCompilerName(), 70, applicable.isSunStudio(),
                        applicable.getDependencies(), applicable.getSearchPaths(), applicable.getSourceRoot(), applicable.getMainFunction());
            }
            if (applicable.getErrors().size() > 0) {
                return ApplicableImpl.getNotApplicable(applicable.getErrors());
            }
        } else {
            ExecutionEnvironment ee = FileSystemProvider.getExecutionEnvironment(fs);
            if (ee.isRemote() && !ConnectionManager.getInstance().isConnectedTo(ee)) {
                return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(AnalyzeExecutable.class, "CannotAnalyzeExecutable",set)));
            }
            RemoteJavaExecution processor = new RemoteJavaExecution(fs);
            List<SourceFile> compileLines = processor.getCompileLines(set, false);
            if (compileLines != null) {
                DiscoveryExtensionInterface.Position main = null;
                for(final SourceFile source : compileLines) {
                    if (source.hasMain()) {
                        main = new DiscoveryExtensionInterface.Position() {

                            @Override
                            public String getFilePath() {
                                return source.getSourceFileAbsolutePath();
                            }

                            @Override
                            public int getLine() {
                                return source.getMainLine();
                            }
                        };
                        break;
                    }
                }
                ElfReader.SharedLibraries libs = processor.getDlls(set);
                if (libs == null) {
                    return new ApplicableImpl(true, null, null, 0, false, Collections.<String>emptyList(), Collections.<String>emptyList(), processor.getSourceRoot(compileLines), main);
                } else {
                    return new ApplicableImpl(true, null, null, 0, false, libs.getDlls(), libs.getPaths(), processor.getSourceRoot(compileLines), main);
                }
            }
        }
        return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(AnalyzeExecutable.class, "CannotAnalyzeExecutable",set)));
    }
    
    @Override
    public List<Configuration> analyze(final ProjectProxy project, Progress progress, Interrupter interrupter) {
        resetStopInterrupter(interrupter);
        List<Configuration> confs = new ArrayList<Configuration>();
        init(project);
        if (!getStopInterrupter().cancelled()){
            Configuration conf = new Configuration(){
                private List<SourceFileProperties> myFileProperties;
                private List<String> myIncludedFiles;
                private Set<String> myDependencies;
                @Override
                public List<ProjectProperties> getProjectConfiguration() {
                    return ProjectImpl.divideByLanguage(getSourcesConfiguration(), project);
                }
                
                @Override
                public List<String> getDependencies() {
                    if (myDependencies == null) {
                        getSourcesConfiguration();
                    }
                    return new ArrayList<String>(myDependencies);
                }

                @Override
                public List<String> getBuildArtifacts() {
                    return null;
                }

                @Override
                public Map<ItemProperties.LanguageKind, Map<String, Integer>> getBuildTools() {
                    return null;
                }
                
                @Override
                public List<SourceFileProperties> getSourcesConfiguration() {
                    if (myFileProperties == null){
                        myDependencies = new HashSet<String>();
                        String set = EXECUTABLE_PROPERTY.getValue();
                        if (set != null && set.length() > 0) {
                            String[] add = LIBRARIES_PROPERTY.getValue();
                            if (add == null || add.length==0) {
                                myFileProperties = getSourceFileProperties(new String[]{set},null, project, myDependencies, null, null, new CompileLineStorage());
                            } else {
                                String[] all = new String[add.length+1];
                                all[0] = set;
                                System.arraycopy(add, 0, all, 1, add.length);
                                myFileProperties = getSourceFileProperties(all,null, project, myDependencies, null, null, new CompileLineStorage());
                            }
                            store(project);
                        }
                    }
                    return myFileProperties;
                }
                
                @Override
                public List<String> getIncludedFiles(){
                    if (myIncludedFiles == null) {
                        HashSet<String> set = new HashSet<String>();
                        for(SourceFileProperties source : getSourcesConfiguration()){
                            if (getStopInterrupter().cancelled()) {
                                break;
                            }
                            if (source instanceof DwarfSource) {
                                set.addAll( ((DwarfSource)source).getIncludedFiles() );
                                set.add(source.getItemPath());
                            }
                        }
                        HashSet<String> unique = new HashSet<String>();
                        for(String path : set){
                            if (getStopInterrupter().cancelled()) {
                                break;
                            }
                            FileObject file = getSourceFileSystem().findResource(path);
                            if (file != null && file.isValid()) {
                                String absolutePath = CndFileUtils.normalizePath(file);
                                if (project.resolveSymbolicLinks()) {
                                    String s = DiscoveryUtils.resolveSymbolicLink(getSourceFileSystem(), absolutePath);
                                    if (s != null) {
                                        absolutePath = s;
                                    }
                                }
                                unique.add(absolutePath);
                            }
                        }
                        myIncludedFiles = new ArrayList<String>(unique);
                    }
                    return myIncludedFiles;
                }
            };
            confs.add(conf);
        }
        return confs;
    }
    
    private static String i18n(String id) {
        return NbBundle.getMessage(AnalyzeFolder.class,id);
    }
}
