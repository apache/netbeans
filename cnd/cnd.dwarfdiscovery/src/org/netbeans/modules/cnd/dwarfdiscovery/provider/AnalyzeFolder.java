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

//import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 */
public class AnalyzeFolder extends BaseDwarfProvider {
    public static final String FOLDER_PROVIDER_ID = "dwarf-folder"; // NOI18N
    private final Map<String,ProviderProperty> myProperties = new HashMap<String,ProviderProperty>();
    private final ProviderProperty<String> BINARY_FOLDER_PROPERTY;
    
    public AnalyzeFolder() {
        myProperties.clear();
        BINARY_FOLDER_PROPERTY = new ProviderProperty<String>(){
            private String myPath;
            @Override
            public String getName() {
                return i18n("Folder_Files_Name"); // NOI18N
            }
            @Override
            public String getDescription() {
                return i18n("Folder_Files_Description"); // NOI18N
            }
            @Override
            public String getValue() {
                return myPath;
            }
            @Override
            public void setValue(String value) {
                myPath = (String)value;
            }
            @Override
            public ProviderPropertyType<String> getPropertyType() {
                return ProviderPropertyType.ExecutableFolderPropertyType;
            }
        };
        myProperties.put(BINARY_FOLDER_PROPERTY.getPropertyType().key(), BINARY_FOLDER_PROPERTY);
        // inherited properties
        myProperties.put(BYNARY_FILESYSTEM_PROPERTY.getPropertyType().key(), BYNARY_FILESYSTEM_PROPERTY);
        myProperties.put(RESTRICT_SOURCE_ROOT_PROPERTY.getPropertyType().key(), RESTRICT_SOURCE_ROOT_PROPERTY);
        myProperties.put(RESTRICT_COMPILE_ROOT_PROPERTY.getPropertyType().key(), RESTRICT_COMPILE_ROOT_PROPERTY);
    }
    
    @Override
    public String getID() {
        return FOLDER_PROVIDER_ID; // NOI18N
    }
    
    @Override
    public String getName() {
        return i18n("Folder_Provider_Name"); // NOI18N
    }
    
    @Override
    public String getDescription() {
        return i18n("Folder_Provider_Description"); // NOI18N
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
        resetStopInterrupter(interrupter);
        String root = BINARY_FOLDER_PROPERTY.getValue();
        if (root == null || root.length() == 0) {
            return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(AnalyzeFolder.class, "NoBaseFolder")));
        }
        Set<String> set = getObjectFiles(root);
        if (set.isEmpty()) {
            return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(AnalyzeFolder.class, "NotFoundExecutablesInFolder", root)));
        }
        int i = 0;
        for(String obj : set){
            i++;
            DiscoveryExtensionInterface.Applicable applicable = sizeComilationUnit(project, Collections.singleton(obj), null, false);
            if (applicable.isApplicable()) {
                return new ApplicableImpl(true, applicable.getErrors(), applicable.getCompilerName(), 50, applicable.isSunStudio(), null, null, null, null);
            }
            if (i > 25) {
                return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(AnalyzeFolder.class, "NotFoundExecutableWithDebugInformation", root)));
            }
        }
        return ApplicableImpl.getNotApplicable(Collections.singletonList(NbBundle.getMessage(AnalyzeFolder.class, "NotFoundExecutableWithDebugInformation", root)));
    }
    
    @Override
    public List<Configuration> analyze(final ProjectProxy project, final Progress progress, final Interrupter interrupter) {
        resetStopInterrupter(interrupter);
        List<Configuration> confs = new ArrayList<Configuration>();
        init(project);
        if (!getStopInterrupter().cancelled()){
            Configuration conf = new Configuration(){
                private List<SourceFileProperties> myFileProperties;
                private List<String> myIncludedFiles;
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
                    return null;
                }

                @Override
                public Map<ItemProperties.LanguageKind, Map<String, Integer>> getBuildTools() {
                    return null;
                }
                
                @Override
                public List<SourceFileProperties> getSourcesConfiguration() {
                    if (myFileProperties == null){
                        if (progress != null) {
                            progress.start();
                        }
                        try {
                            Set<String> set = getObjectFiles(BINARY_FOLDER_PROPERTY.getValue());
                            if (progress != null) {
                                progress.start(set.size());
                            }
                            if (set.size() > 0) {
                                myFileProperties = getSourceFileProperties(set.toArray(new String[set.size()]), progress, project, null, null, null, new CompileLineStorage());
                                store(project);
                            } else {
                                myFileProperties = new ArrayList<SourceFileProperties>();
                            }
                        } finally {
                            if (progress != null) {
                                progress.done();
                            }
                        }
                    }
                    return myFileProperties;
                }
                
                @Override
                public List<String> getIncludedFiles(){
                    if (myIncludedFiles == null) {
                        Set<String> set = new HashSet<String>();
                        for(SourceFileProperties source : getSourcesConfiguration()){
                            if (getStopInterrupter().cancelled()) {
                                break;
                            }
                            set.addAll( ((DwarfSource)source).getIncludedFiles() );
                            set.add(source.getItemPath());
                        }
                        if (progress != null) {
                            progress.start(set.size());
                        }
                        Set<String> unique = new HashSet<String>();
                        for(String path : set){
                            if (getStopInterrupter().cancelled()) {
                                break;
                            }
                            if (progress != null) {
                                synchronized(progress) {
                                    progress.increment(path);
                                }
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
                        if (progress != null) {
                            progress.done();
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
    
    private Set<String> getObjectFiles(String root){
        FileSystem fs = BYNARY_FILESYSTEM_PROPERTY.getValue();
        if (fs == null) {
            fs = CndFileUtils.getLocalFileSystem();
        }
        FileObject rootFO = fs.findResource(root);
        HashSet<String> map = new HashSet<String>();
        gatherSubFolders(rootFO, map, new HashSet<String>());
        return map;
    }
    
    private boolean isExecutable(FileObject file){
        String name = file.getNameExt();
        if (CndFileUtils.isLocalFileSystem(file) && Utilities.isWindows()) {
            return name.endsWith(".exe") || name.endsWith(".dll");  // NOI18N
        } else {
            // FIXUP: There are no way to detect "executable".
            //return name.indexOf('.') < 0;
            try{
                //Since 1.6
                return name.indexOf('.') < 0 && MIMENames.isBinaryExecutable(file.getMIMEType());
            } catch (SecurityException ex) {
            }
        }
        return false;
    }
    
    private void gatherSubFolders(FileObject d, HashSet<String> map, HashSet<String> antiLoop){
        if (getStopInterrupter().cancelled()) {
            return;
        }
        if (d != null && d.isValid() && d.isFolder() && d.canRead()){
            if (CndPathUtilities.isIgnoredFolder(d.getPath())){
                return;
            }
            String canPath;
            try {
                canPath = CndFileUtils.getCanonicalPath(d);
            } catch (IOException ex) {
                return;
            }
            if (!antiLoop.contains(canPath)){
                antiLoop.add(canPath);
                FileObject[] ff = d.getChildren();
                if (ff != null) {
                    for (int i = 0; i < ff.length; i++) {
                        if (getStopInterrupter().cancelled()) {
                            break;
                        }
                        if (ff[i].isFolder()) {
                            gatherSubFolders(ff[i], map, antiLoop);
                        } else if (ff[i].isData()) {
                            String name = ff[i].getNameExt();
                            if (name.endsWith(".o") ||  // NOI18N
                                name.endsWith(".so") ||  // NOI18N
                                name.endsWith(".dylib") ||  // NOI18N
                                name.endsWith(".a") ||  // NOI18N
                                isExecutable(ff[i])){
                                String path = ff[i].getPath();
                                if (Utilities.isWindows()) {
                                    path = path.replace('\\', '/');
                                }
                                map.add(path);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private static String i18n(String id) {
        return NbBundle.getMessage(AnalyzeFolder.class,id);
    }
}
