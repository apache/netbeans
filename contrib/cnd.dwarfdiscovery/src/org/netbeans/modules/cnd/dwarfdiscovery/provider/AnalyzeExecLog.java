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

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.modules.cnd.discovery.api.ApplicableImpl;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectImpl;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.ProviderPropertyType;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.FSPath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;

/**
 *
 */
public class AnalyzeExecLog extends BaseProvider {
    public static final String EXEC_LOG_PROVIDER_ID = "exec-log"; // NOI18N
    private final Map<String, ProviderProperty<?>> myProperties = new LinkedHashMap<String, ProviderProperty<?>>();
    private final ProviderProperty<String> EXEC_LOG_PROPERTY;
    private final ProviderProperty<FileSystem> LOG_FILESYSTEM_PROPERTY;

    public AnalyzeExecLog() {
        myProperties.clear();
        EXEC_LOG_PROPERTY = new ProviderProperty<String>() {
            private String myPath;
            @Override
            public String getName() {
                return i18n("Exec_Log_File_Name"); // NOI18N
            }
            @Override
            public String getDescription() {
                return i18n("Exec_Log_File_Description"); // NOI18N
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
                return ProviderPropertyType.ExecLogPropertyType;
            }
        };
        myProperties.put(EXEC_LOG_PROPERTY.getPropertyType().key(), EXEC_LOG_PROPERTY);
        
        LOG_FILESYSTEM_PROPERTY = new ProviderProperty<FileSystem>(){
            private FileSystem fs;
            @Override
            public String getName() {
                return ""; // NOI18N
            }
            @Override
            public String getDescription() {
                return ""; // NOI18N
            }
            @Override
            public FileSystem getValue() {
                return fs;
            }
            @Override
            public void setValue(FileSystem value) {
                fs = value;
            }
            @Override
            public ProviderPropertyType<FileSystem> getPropertyType() {
                return ProviderPropertyType.LogFileSystemPropertyType;
            }
        };
        myProperties.put(LOG_FILESYSTEM_PROPERTY.getPropertyType().key(), LOG_FILESYSTEM_PROPERTY);
        
        myProperties.put(RESTRICT_SOURCE_ROOT_PROPERTY.getPropertyType().key(), RESTRICT_SOURCE_ROOT_PROPERTY);
        myProperties.put(RESTRICT_COMPILE_ROOT_PROPERTY.getPropertyType().key(), RESTRICT_COMPILE_ROOT_PROPERTY);
    }

    @Override
    public String getID() {
        return EXEC_LOG_PROVIDER_ID; // NOI18N
    }

    @Override
    public String getName() {
        return i18n("Exec_Log_Provider_Name"); // NOI18N
    }

    @Override
    public String getDescription() {
        return i18n("Exec_Log_Provider_Description"); // NOI18N
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
    public boolean isApplicable(ProjectProxy project) {
        String set = EXEC_LOG_PROPERTY.getValue();
        if (set != null && set.length() > 0) {
            if (getLog(set) != null) {
                return true;
            }
        }
        String o = RESTRICT_COMPILE_ROOT_PROPERTY.getValue();
        if (o == null || o.isEmpty()) {
            RESTRICT_COMPILE_ROOT_PROPERTY.setValue(project.getSourceRoot());
            return true;
        }
        return false;
    }

    private FileObject getLog(String set) {
        FileSystem fs = LOG_FILESYSTEM_PROPERTY.getValue();
        if (fs == null) {
            fs = CndFileSystemProvider.getLocalFileSystem();
        }
        FSPath log = new FSPath(fs, set);
        FileObject fo = log.getFileObject();
        if (fo != null && fo.isValid() && fo.isData() && fo.canRead()) {
            return fo;
        }
        return null;
    }
    
    @Override
    public DiscoveryExtensionInterface.Applicable canAnalyze(ProjectProxy project, Interrupter interrupter) {
        init(project);
        String set = EXEC_LOG_PROPERTY.getValue();
        if (set == null || set.length() == 0 || !ExecLogReader.isSupportedLog(getLog(set))) {
            return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(AnalyzeExecLog.class, "NotFoundExecLog")));
        }
        return new ApplicableImpl(true, null, null, 80, false, null, null, null, null);
    }
    
    @Override
    protected List<SourceFileProperties> getSourceFileProperties(String logFileName, Map<String, SourceFileProperties> map, ProjectProxy project, Set<String> dlls,
            List<String> buildArtifacts, Map<ItemProperties.LanguageKind,Map<String,Integer>> buildTools, CompileLineStorage storage) {
        String root = RESTRICT_COMPILE_ROOT_PROPERTY.getValue();
        if (root == null) {
            root = "";
        }
        List<SourceFileProperties> res = runLogReader(getLog(logFileName), root, progress, project, buildArtifacts, buildTools, storage);
        progress = null;
        return res;

    }
    private List<SourceFileProperties> runLogReader(FileObject logFileObject, String root, Progress progress, ProjectProxy project,
            List<String> buildArtifacts, Map<ItemProperties.LanguageKind,Map<String,Integer>> buildTools, CompileLineStorage storage) {
        FileSystem fileSystem = getFileSystem(project);
        ExecLogReader reader = new ExecLogReader(logFileObject, root, project, getRelocatablePathMapper(), fileSystem);
        List<SourceFileProperties> list = reader.getResults(progress, getStopInterrupter(), storage);
        buildArtifacts.addAll(reader.getArtifacts(progress, getStopInterrupter(), storage));
        buildTools.putAll(reader.getTools(progress, getStopInterrupter(), storage));
        return list;
    }
    
    private Progress progress;

    @Override
    public List<Configuration> analyze(final ProjectProxy project, Progress progress, Interrupter interrupter) {
        resetStopInterrupter(interrupter);
        List<Configuration> confs = new ArrayList<Configuration>();
        init(project);
        this.progress = progress;
        if (!getStopInterrupter().cancelled()) {
            Configuration conf = new Configuration() {

                private List<SourceFileProperties> myFileProperties;
                private List<String> myBuildArtifacts;
                private Map<ItemProperties.LanguageKind,Map<String,Integer>> buildTools;
                private final List<String> myIncludedFiles = new ArrayList<String>();

                @Override
                public List<ProjectProperties> getProjectConfiguration() {
                    return ProjectImpl.divideByLanguage(getSourcesConfiguration(), project);
                }

                @Override
                public List<String> getDependencies() {
                    return null;
                }

                @Override
                public List<String> getBuildArtifacts() {
                    if (myBuildArtifacts == null) {
                        process();
                    }
                    return myBuildArtifacts;
                }

                @Override
                public Map<ItemProperties.LanguageKind, Map<String, Integer>> getBuildTools() {
                    if (buildTools == null) {
                        process();
                    }
                    return buildTools;
                }


                @Override
                public List<SourceFileProperties> getSourcesConfiguration() {
                    if (myFileProperties == null) {
                        process();
                    }
                    return myFileProperties;
                }

                @Override
                public List<String> getIncludedFiles() {
                    return myIncludedFiles;
                }

                private void process() {
                    String set = EXEC_LOG_PROPERTY.getValue();
                    if (set != null && set.length() > 0) {
                        myBuildArtifacts = Collections.synchronizedList(new ArrayList<String>());
                        buildTools = new ConcurrentHashMap<ItemProperties.LanguageKind, Map<String, Integer>>();
                        myFileProperties = getSourceFileProperties(new String[]{set}, null, project, null, myBuildArtifacts, buildTools, new CompileLineStorage());
                        store(project);
                    }
                }
            };
            confs.add(conf);
        }
        return confs;
    }

    private static String i18n(String id) {
        return NbBundle.getMessage(AnalyzeExecLog.class, id);
    }    
}
