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

package org.netbeans.modules.cnd.modeldiscovery.provider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeFileItem.Language;
import org.netbeans.modules.cnd.discovery.api.ApplicableImpl;
import org.netbeans.modules.cnd.discovery.api.Configuration;
import org.netbeans.modules.cnd.discovery.api.DiscoveryExtensionInterface;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectImpl;
import org.netbeans.modules.cnd.discovery.api.ProjectProperties;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.ProviderPropertyType;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager;
import org.netbeans.modules.cnd.makeproject.spi.configurations.PkgConfigManager.PkgConfig;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 */
public class AnalyzeModel implements DiscoveryProvider {
    public static final String MODEL_FOLDER_PROVIDER_ID = "model-folder"; // NOI18N
    private final Map<String,ProviderProperty> myProperties = new HashMap<String,ProviderProperty>();
    private final ProviderProperty<String> MODEL_FOLDER_PROPERTY;
    private final ProviderProperty<Boolean> PREFER_LOCAL_FILES_PROPERTY;
    private boolean isStoped = false;
    
    public AnalyzeModel() {
        myProperties.clear();
        MODEL_FOLDER_PROPERTY = new ProviderProperty<String>(){
            private String myPath;
            @Override
            public String getName() {
                return i18n("Model_Files_Name"); // NOI18N
            }
            @Override
            public String getDescription() {
                return i18n("Model_Files_Description"); // NOI18N
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
                return ProviderPropertyType.ModelFolderPropertyType;
            }
        };
        myProperties.put(MODEL_FOLDER_PROPERTY.getPropertyType().key(), MODEL_FOLDER_PROPERTY);
        
        PREFER_LOCAL_FILES_PROPERTY = new ProviderProperty<Boolean>(){
            private Boolean myValue = Boolean.FALSE;
            @Override
            public String getName() {
                return i18n("Prefer_Local_Files"); // NOI18N
            }
            @Override
            public String getDescription() {
                return i18n("Prefer_Local_Files_Description"); // NOI18N
            }
            @Override
            public Boolean getValue() {
                return myValue;
            }
            @Override
            public void setValue(Boolean value) {
                myValue = value;
            }
            @Override
            public ProviderPropertyType<Boolean> getPropertyType() {
                return ProviderPropertyType.PreferLocalFilesPropertyType;
            }
        };
        myProperties.put(PREFER_LOCAL_FILES_PROPERTY.getPropertyType().key(), PREFER_LOCAL_FILES_PROPERTY);
    }
    
    @Override
    public String getID() {
        return MODEL_FOLDER_PROVIDER_ID; // NOI18N
    }
    
    @Override
    public String getName() {
        return i18n("Model_Provider_Name"); // NOI18N
    }
    
    @Override
    public String getDescription() {
        return i18n("Model_Provider_Description"); // NOI18N
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
    public boolean cancel() {
        isStoped = true;
        return true;
    }
    
    @Override
    public List<Configuration> analyze(ProjectProxy project, Progress progress, Interrupter interrupter) {
        isStoped = false;
        MyConfiguration conf = new MyConfiguration(project, progress);
        List<Configuration> confs = new ArrayList<Configuration>();
        confs.add(conf);
        return confs;
    }
    
    
    private Map<String,List<String>> search(String root){
        HashSet<String> set = new HashSet<String>();
        HashSet<String> antiLoop = new HashSet<String>();
        ArrayList<String> list = new ArrayList<String>();
        list.add(root);
        for (Iterator<String> it = list.iterator(); it.hasNext();){
            if (isStoped) {
                break;
            }
            File f = new File(it.next());
            gatherSubFolders(f, set, antiLoop);
        }
        HashMap<String,List<String>> map = new HashMap<String,List<String>>();
        for (Iterator<String> it = set.iterator(); it.hasNext();){
            if (isStoped) {
                break;
            }
            File d = new File(it.next());
            if (d.exists() && d.isDirectory() && d.canRead()){
                File[] ff = d.listFiles();
                if (ff != null) {
                    for (int i = 0; i < ff.length; i++) {
                        if (ff[i].isFile()) {
                            List<String> l = map.get(ff[i].getName());
                            if (l==null){
                                l = new ArrayList<String>();
                                map.put(ff[i].getName(),l);
                            }
                            String path = ff[i].getAbsolutePath();
                            if (Utilities.isWindows()) {
                                path = path.replace('\\', '/');
                            }
                            l.add(path);
                        }
                    }
                }
            }
        }
        return map;
    }
    
    private void gatherSubFolders(File d, HashSet<String> set, HashSet<String> antiLoop){
        if (isStoped) {
            return;
        }
        if (d.exists() && d.isDirectory() && d.canRead()){
            if (CndPathUtilities.isIgnoredFolder(d)){
                return;
            }
            String canPath;
            try {
                canPath = d.getCanonicalPath();
            } catch (IOException ex) {
                return;
            }
            if (!antiLoop.contains(canPath)){
                antiLoop.add(canPath);
                String path = d.getAbsolutePath();
                if (Utilities.isWindows()) {
                    path = path.replace('\\', '/');
                }
                set.add(path);
                File[] ff = d.listFiles();
                if (ff != null) {
                    for (int i = 0; i < ff.length; i++) {
                        if (ff[i].isDirectory()) {
                            gatherSubFolders(ff[i], set, antiLoop);
                        }
                    }
                }
            }
        }
    }
    
    private static String i18n(String id) {
        return NbBundle.getMessage(AnalyzeModel.class,id);
    }
    
    @Override
    public boolean isApplicable(ProjectProxy project) {
        if (project.getProject() != null){
            Project makeProject = project.getProject();
            ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (pdp.gotDescriptor()) {
                CsmProject langProject = CsmModelAccessor.getModel().getProject(makeProject);
                if (langProject != null/* && langProject.isStable(null)*/){
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public DiscoveryExtensionInterface.Applicable canAnalyze(ProjectProxy project, Interrupter interrupter) {
        return new ApplicableImpl(true, null, null, 40, false, null, null, null, null);
    }
    
    private class MyConfiguration implements Configuration{
        private List<SourceFileProperties> myFileProperties;
        private List<String> myIncludedFiles;
        private final MakeConfigurationDescriptor makeConfigurationDescriptor;
        private final CsmProject langProject;
        private final ProjectProxy project;
        private final Progress progress;
        
        private MyConfiguration(ProjectProxy project, Progress progress){
            Project makeProject = project.getProject();
            this.progress = progress;
            this.project =project;
            langProject = CsmModelAccessor.getModel().getProject(makeProject);
            ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
            makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
        }
        
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
        
        public boolean isExcluded(Item item){
            MakeConfiguration makeConfiguration = item.getFolder().getConfigurationDescriptor().getActiveConfiguration();
            ItemConfiguration itemConfiguration = item.getItemConfiguration(makeConfiguration); //ItemConfiguration)makeConfiguration.getAuxObject(ItemConfiguration.getId(item.getPath()));
            if (itemConfiguration == null) {
                return true;
            }
            BooleanConfiguration excl =itemConfiguration.getExcluded();
            return excl.getValue();
        }
        
        private ExecutionEnvironment getExecutionEnvironment() {
            MakeConfiguration activeConfiguration = makeConfigurationDescriptor.getActiveConfiguration();
            ExecutionEnvironment env = null;
            if (activeConfiguration != null) {
                env = activeConfiguration.getDevelopmentHost().getExecutionEnvironment();
            }
            if (env == null) {
                env = ExecutionEnvironmentFactory.getLocal();
            }
            return env;
        }
        
        private List<SourceFileProperties> getSourceFileProperties(String root){
            List<SourceFileProperties> res = new ArrayList<SourceFileProperties>();
            if (root != null && langProject != null) {
                Map<String,List<String>> searchBase = search(root);
                PkgConfig pkgConfig = PkgConfigManager.getDefault().getPkgConfig(getExecutionEnvironment(), makeConfigurationDescriptor.getActiveConfiguration());
                boolean preferLocal = PREFER_LOCAL_FILES_PROPERTY.getValue();
                Item[] items = makeConfigurationDescriptor.getProjectItems();
                Map<String,Item> projectSearchBase = new HashMap<String,Item>();
                for (int i = 0; i < items.length; i++){
                    if (isStoped) {
                        break;
                    }
                    Item item = items[i];
                    String path = item.getNormalizedPath();
                    projectSearchBase.put(path, item);
                }
                for (int i = 0; i < items.length; i++){
                    if (isStoped) {
                        break;
                    }
                    Item item = items[i];
                    if (!isExcluded(item)) {
                        Language lang = item.getLanguage();
                        if (lang == Language.C || lang == Language.CPP){
                            CsmFile langFile = langProject.findFile(item, true, false);
                            if (langFile != null) {
                                SourceFileProperties source = new ModelSource(item, langFile, searchBase, projectSearchBase, pkgConfig, preferLocal);
                                res.add(source);
                            }
                        }
                    }
                }
            }
            return res;
        }
        
        @Override
        public List<SourceFileProperties> getSourcesConfiguration() {
            if (myFileProperties == null){
                myFileProperties = getSourceFileProperties(MODEL_FOLDER_PROPERTY.getValue());
            }
            return myFileProperties;
        }
        
        @Override
        public List<String> getIncludedFiles(){
            if (myIncludedFiles == null) {
                HashSet<String> unique = new HashSet<String>();
                Item[] items = makeConfigurationDescriptor.getProjectItems();
                if (progress != null){
                    progress.start(items.length);
                }
                try {
                    for (int i = 0; i < items.length; i++){
                        if (isStoped) {
                            break;
                        }
                        Item item = items[i];
                        if (isExcluded(item)) {
                            continue;
                        }
                        String path = item.getAbsPath();
                        File file = new File(path);
                        if (CndFileUtils.exists(file)) {
                            unique.add(CndFileUtils.normalizeAbsolutePath(file.getAbsolutePath()));
                        }
                    }
                    HashSet<CharSequence> unUnique = new HashSet<CharSequence>();
                    for(SourceFileProperties source : getSourcesConfiguration()){
                        if (source instanceof ModelSource){
                            unUnique.addAll( ((ModelSource)source).getIncludedFiles() );
                        }
                        if (progress != null){
                            progress.increment(null);
                        }
                    }
                    for(CharSequence path : unUnique){
                        File file = new File(path.toString());
                        if (CndFileUtils.exists(file)) {
                            unique.add(CndFileUtils.normalizeAbsolutePath(file.getAbsolutePath()));
                        }
                    }
                    myIncludedFiles = new ArrayList<String>(unique);
                } finally {
                    if (progress != null){
                        progress.done();
                    }
                }
            }
            return myIncludedFiles;
        }
    }
}
