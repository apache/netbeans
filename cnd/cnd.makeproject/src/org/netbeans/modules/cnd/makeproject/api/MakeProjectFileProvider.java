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
package org.netbeans.modules.cnd.makeproject.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeFileSearch;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.spi.configurations.UserOptionsProvider;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;
import org.openide.util.Lookup;

/**
 *
 */
public class MakeProjectFileProvider {
    
    protected static final ConcurrentMap<Lookup.Provider, ConcurrentMap<Folder,List<CharSequence>>> searchBase = new ConcurrentHashMap<>();
    protected static final ConcurrentMap<Lookup.Provider, ConcurrentMap<CharSequence,List<CharSequence>>> fileNameSearchBase = new ConcurrentHashMap<>();
    protected static final Collection<? extends UserOptionsProvider> packageSearch = Lookup.getDefault().lookupAll(UserOptionsProvider.class);

    /**
     * Store/update/remove list of non cnd files for project folder
     *
     * @param project
     * @param folder
     * @param list
     */
    public static void updateSearchBase(Project project, Folder folder, List<CharSequence> list){
        ConcurrentMap<Folder, List<CharSequence>> projectSearchBase = searchBase.get(project);
        if (projectSearchBase == null) {
            projectSearchBase = new ConcurrentHashMap<>();
            ConcurrentMap<Folder, List<CharSequence>> old = searchBase.putIfAbsent(project, projectSearchBase);
            if (old != null) {
                projectSearchBase = old;
            }
        }
        synchronized (projectSearchBase) {
            if (list == null) {
                projectSearchBase.remove(folder);
            } else {
                if (list.isEmpty()) {
                    projectSearchBase.put(folder, new ArrayList<CharSequence>(0));
                } else {
                    projectSearchBase.put(folder, list);
                }
            }
        }
    }

    /**
     * Get set of non cnd files for project folder
     * 
     * @param project
     * @param folder
     * @return 
     */
    public static Set<CharSequence> getSearchBase(Project project, Folder folder) {
        Set<CharSequence> result = new HashSet<>();
        ConcurrentMap<Folder,List<CharSequence>> projectSearchBase = searchBase.get(project);
        if (projectSearchBase != null) {
            List<CharSequence> list = projectSearchBase.get(folder);
            if (list != null) {
                synchronized(list) {
                    result.addAll(list);
                }
            }
        }
        return result;
    }
    
    /**
     * Remove project files search base
     *
     * @param project
     */
    public static void removeSearchBase(Project project){
        searchBase.remove(project);
        // 223003 - memory leaked project instance 
        fileNameSearchBase.remove(project); // prevent leak
    }

    public static void removeFromSearchBase(Project project, Folder folder, CharSequence item) {
        updateSearchBaseImpl(project, folder, item, true);
    }

    public static void addToSearchBase(Project project, Folder folder, CharSequence item) {
        updateSearchBaseImpl(project, folder, item, false);
    }

    private static void updateSearchBaseImpl(Project project, Folder folder, CharSequence item, boolean remove) {
        ConcurrentMap<Folder, List<CharSequence>> projectSearchBase = searchBase.get(project);
        if (projectSearchBase != null) {
            synchronized (projectSearchBase) {
                List<CharSequence> folderItems = projectSearchBase.get(folder);
                if (folderItems != null) {
                    synchronized(folderItems) {
                        if (remove) {
                            folderItems.remove(item);
                        } else {
                            folderItems.add(item);
                        }
                    }
                }
            }
        }
        ConcurrentMap<CharSequence, List<CharSequence>> projectFileNames = fileNameSearchBase.get(project);
        if (projectFileNames != null) {
            String fileName = item.toString();
            int i = fileName.lastIndexOf('/');
            String name = fileName;
            if (i >= 0) {
                name = fileName.substring(i + 1);
            }
            Collection<CharSequence> res = projectFileNames.get(CharSequences.create(name));
            if (res != null) {
                synchronized (res) {
                    if (remove) {
                        res.remove(item);
                    } else {
                        res.add(item);
                    }
                }
            }
        }
    }
    
    @org.openide.util.lookup.ServiceProvider(service=NativeFileSearch.class)
    public static class NativeFileSearchImpl implements NativeFileSearch {
        public NativeFileSearchImpl() {
        }

        @Override
        public Collection<FSPath> searchFile(NativeProject project, String fileName) {
            if (MakeOptions.getInstance().isFixUnresolvedInclude()) {
                Collection<FSPath> res;
                for(NativeProject np : NativeProjectRegistry.getDefault().getOpenProjects()) {
                    if (np == project) {
                        Lookup.Provider p = np.getProject();
                        if (p instanceof Project) {
                            ConcurrentMap<CharSequence,List<CharSequence>> projectSearchBase = fileNameSearchBase.get(p);
                            if (projectSearchBase == null) {
                                projectSearchBase = computeProjectFiles(p);
                                fileNameSearchBase.put(p, projectSearchBase);
                            }
                            int i = fileName.lastIndexOf('/');
                            String name = fileName;
                            if (i >= 0) {
                                name = fileName.substring(i+1);
                            }
                            MakeConfiguration conf = ConfigurationSupport.getProjectActiveConfiguration((Project)p);
                            List<CharSequence> list = projectSearchBase.get(CharSequences.create(name));
                            if (list != null) {
                                synchronized(list) {
                                    if (list.size() > 0) {
                                        FileSystem fileSystem;
                                        if (conf != null) {
                                            fileSystem = conf.getFileSystem();
                                        } else {
                                            fileSystem = FileSystemProvider.getFileSystem(ExecutionEnvironmentFactory.getLocal());
                                        }
                                        res = new ArrayList<>(list.size());
                                        list.forEach((absPath) -> {
                                            res.add(new FSPath(fileSystem, absPath.toString()));
                                        });
                                        return res;
                                    }
                                }
                            }
                            ExecutionEnvironment env;
                            if (conf != null){
                                env = conf.getDevelopmentHost().getExecutionEnvironment();
                            } else {
                                env = ExecutionEnvironmentFactory.getLocal();
                            }
                            res = defaultSearch(project, fileName, env, conf);
                            if (res != null && res.size() > 0) {
                                return res;
                            }
                            return Collections.<FSPath>emptyList();
                        }
                    }
                }
                // Standalone project
                ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(project.getFileSystem());
                if (env == null) {
                    env = ExecutionEnvironmentFactory.getLocal();
                }
                res = defaultSearch(project, fileName, env, null);
                if (res != null && res.size() > 0) {
                    return res;
                }
            }
            return Collections.<FSPath>emptyList();
        }

        private Collection<FSPath> defaultSearch(NativeProject project, String fileName, ExecutionEnvironment env, MakeConfiguration conf) {
            Collection<FSPath> res = null;
            if (env == null) {
                env = ExecutionEnvironmentFactory.getLocal();
            }
            boolean isDoSearch = false;
            if (env.isLocal()) {
                isDoSearch = true;
            } else {
                if (Boolean.valueOf(System.getProperty("cnd.pkg.search.enabled", "true"))) {
                    isDoSearch = ConnectionManager.getInstance().isConnectedTo(env);
                }
            }
            if (!packageSearch.isEmpty() && isDoSearch) {
                for (UserOptionsProvider userOptionsProvider : packageSearch) {
                    NativeFileSearch search = userOptionsProvider.getPackageFileSearch(env, conf);
                    if (search != null) {
                        res = search.searchFile(project, fileName);
                        if(res != null) {
                            break;
                        }
                    }
                }
            }
            return res;
        }
        
        private ConcurrentMap<CharSequence,List<CharSequence>> computeProjectFiles(Lookup.Provider project) {
            ConcurrentMap<CharSequence,List<CharSequence>> result = new ConcurrentHashMap<>();
            ConfigurationDescriptorProvider provider = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (provider != null && provider.gotDescriptor()) {
                MakeConfigurationDescriptor descriptor = provider.getConfigurationDescriptor();
                for (Item item : descriptor.getProjectItems()) {
                    CharSequence name = CharSequences.create(item.getName());
                    List<CharSequence> list = result.get(name);
                    if (list == null) {
                        List<CharSequence> prev = result.putIfAbsent(name, list = new ArrayList<>(1));
                        if (prev != null) {
                            list = prev;
                        }
                    }
                    list.add(CharSequences.create(item.getAbsPath()));
                }
                if (!MakeOptions.getInstance().isFullFileIndexer()) {
                    final ConcurrentMap<Folder,List<CharSequence>> projectSearchBase = searchBase.get(project);
                    if (projectSearchBase != null) {
                        // create copy of data
                        Map<Folder,List<CharSequence>> copy;
                        synchronized (projectSearchBase) {
                            copy = new HashMap<>(projectSearchBase);
                        }
                        for (List<CharSequence> files : copy.values()) {
                            if (files != null) {
                                synchronized(files) {
                                    for(CharSequence path : files) {
                                        String absPath = path.toString();
                                        int i = absPath.lastIndexOf('/');
                                        if (i < 0) {
                                            i = absPath.lastIndexOf('\\');
                                        }
                                        if (i >= 0) {
                                            CharSequence name = CharSequences.create(absPath.substring(i+1));
                                            List<CharSequence> list = result.get(name);
                                            if (list == null) {
                                                List<CharSequence> prev = result.putIfAbsent(name, list = new ArrayList<>(1));
                                                if (prev != null) {
                                                    list = prev;
                                                }
                                            }
                                            list.add(path);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return result;
        }
    }
}
