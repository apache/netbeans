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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.discovery.api.DiscoveryProvider;
import org.netbeans.modules.cnd.discovery.api.ItemProperties;
import org.netbeans.modules.cnd.discovery.api.Progress;
import org.netbeans.modules.cnd.discovery.api.ProjectProxy;
import org.netbeans.modules.cnd.discovery.api.ProviderProperty;
import org.netbeans.modules.cnd.discovery.api.ProviderPropertyType;
import org.netbeans.modules.cnd.discovery.api.SourceFileProperties;
import org.netbeans.modules.cnd.discovery.wizard.api.support.ProjectBridge;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
public abstract class BaseProvider implements DiscoveryProvider {
    
    private final AtomicBoolean isStoped = new AtomicBoolean(false);
    private final Interrupter stopIterrupter;
    private Interrupter projectInterrupter;
    private RelocatablePathMapperImpl mapper;
    private CompilerSettings myCommpilerSettings;
    private FileSystem fileSystem;
    private RelocatablePathMapper.FS fs;
    protected final ProviderProperty<String> RESTRICT_SOURCE_ROOT_PROPERTY;
    protected final ProviderProperty<String> RESTRICT_COMPILE_ROOT_PROPERTY;

    public BaseProvider() {
        stopIterrupter = new Interrupter() {

            @Override
            public boolean cancelled() {
                if (isStoped.get()) {
                    return true;
                }
                Interrupter aProjectInterrupter = projectInterrupter;
                if (aProjectInterrupter != null && aProjectInterrupter.cancelled()) {
                    return true;
                }
                return false;
            }
        };
        RESTRICT_SOURCE_ROOT_PROPERTY = new ProviderProperty<String>() {
            private String myPath = "";
            @Override
            public String getName() {
                return i18n("RESTRICT_SOURCE_ROOT"); // NOI18N
            }
            @Override
            public String getDescription() {
                return i18n("RESTRICT_SOURCE_ROOT"); // NOI18N
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
                return ProviderPropertyType.RestrictSourceRootPropertyType;
            }
        };       
        
        RESTRICT_COMPILE_ROOT_PROPERTY = new ProviderProperty<String>() {
            private String myPath = "";
            @Override
            public String getName() {
                return i18n("RESTRICT_COMPILE_ROOT"); // NOI18N
            }
            @Override
            public String getDescription() {
                return i18n("RESTRICT_COMPILE_ROOT"); // NOI18N
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
                return ProviderPropertyType.RestrictCompileRootPropertyType;
            }
        };        
    }
    
    public final void init(ProjectProxy project) {
        myCommpilerSettings = new CompilerSettings(project);
        mapper = new RelocatablePathMapperImpl(project);
        fileSystem  = getFileSystem(project);
        fs = new FSImpl(fileSystem);
    }

    public final void store(ProjectProxy project) {
        mapper.save();
    }

    @Override
    public boolean isApplicable(ProjectProxy project) {
        return true;
    }
    
    @Override
    public final boolean cancel() {
        isStoped.set(true);
        return true;
    }

    protected final void resetStopInterrupter(Interrupter projectInterrupter) {
        this.projectInterrupter = projectInterrupter;
        isStoped.set(false);
    }

    protected final Interrupter getStopInterrupter() {
        return stopIterrupter;
    }
    
    protected final FileSystem getFileSystem(ProjectProxy project) {
        if (project != null) {
            Project p = project.getProject();
            if (p != null) {                
                return RemoteFileUtil.getProjectSourceFileSystem(p);
            }
        }
        return CndFileUtils.getLocalFileSystem();
    }
    
    protected FileSystem getSourceFileSystem() {
        return fileSystem;
    }
    
    protected final RelocatablePathMapper getRelocatablePathMapper() {
        return mapper;
    }
    
    protected  final CompilerSettings getCommpilerSettings(){
        return myCommpilerSettings;
    }    

    abstract protected List<SourceFileProperties> getSourceFileProperties(String objFileName, Map<String, SourceFileProperties> map, ProjectProxy project, Set<String> dlls,
            List<String> buildArtifacts, Map<ItemProperties.LanguageKind,Map<String,Integer>> buildTools, CompileLineStorage storage);
    
    protected void before() {
    }

    protected void after() {
    }
    
    protected final List<SourceFileProperties> getSourceFileProperties(String[] objFileName, Progress progress, ProjectProxy project,
            Set<String> dlls, List<String> buildArtifacts, Map<ItemProperties.LanguageKind,Map<String,Integer>> buildTools, CompileLineStorage storage){
        try{
            before();
            Map<String,SourceFileProperties> map = new ConcurrentHashMap<String,SourceFileProperties>();
            if (objFileName.length == 1) {
                String oldThreadName = Thread.currentThread().getName();
                try {
                    Thread.currentThread().setName("Analyzing "+objFileName[0]); // NOI18N
                    processArtifactFile(objFileName[0], map, progress, project, dlls, buildArtifacts, buildTools, storage);
                } catch (Throwable ex) {
                    ex.printStackTrace(System.err);
                }
                Thread.currentThread().setName(oldThreadName);
            } else {
                CountDownLatch countDownLatch = new CountDownLatch(objFileName.length);
                RequestProcessor rp = new RequestProcessor("Parallel analyzing", CndUtils.getNumberCndWorkerThreads()); // NOI18N
                for (String file : objFileName) {
                    MyRunnable r = new MyRunnable(countDownLatch, file, map, progress, project, dlls, buildArtifacts, buildTools, storage);
                    rp.post(r);
                }
                try {
                    countDownLatch.await();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            List<SourceFileProperties> list = new ArrayList<SourceFileProperties>();
            list.addAll(map.values());
            return list;
        } finally {
            PathCache.dispose();
            getCommpilerSettings().dispose();
            after();
        }
    }

    private FileObject resolvePath(ProjectProxy project, String buildArtifact, SourceFileProperties f, String name) {
        FileObject fo = fileSystem.findResource(name);
        if (!(f instanceof Relocatable)) {
            return fo;
        }
        String sourceRoot = null;
        if (project != null) {
            sourceRoot = project.getSourceRoot();
            if (sourceRoot != null && sourceRoot.length() < 2) {
                sourceRoot = null;
            }
        }
        if (sourceRoot == null) {
            sourceRoot = PathUtilities.getDirName(buildArtifact);
            if (sourceRoot != null && sourceRoot.length() < 2) {
                sourceRoot = null;
            }
        }
        if (sourceRoot != null) {
            sourceRoot = sourceRoot.replace('\\', '/');
        }
        if (fo == null || !fo.isValid()) {
            RelocatablePathMapper.ResolvedPath resolvedPath = mapper.getPath(name);
            if (resolvedPath == null) {
                if (sourceRoot != null) {
                    if (mapper.discover(fs, sourceRoot, name)) {
                        resolvedPath = mapper.getPath(name);
                        fo = fileSystem.findResource(resolvedPath.getPath());
                        if (fo != null && fo.isValid() && fo.isData()) {
                            ((Relocatable) f).resetItemPath(resolvedPath, mapper, fs);
                            return fo;
                        }
                    }
                }
            } else {
                fo = fileSystem.findResource(resolvedPath.getPath());
                if (fo != null && fo.isValid() && fo.isData()) {
                    ((Relocatable) f).resetItemPath(resolvedPath, mapper, fs);
                    return fo;
                }
            }
        }
        if (fo != null && fo.isData()) {
            name = fo.getPath();
            RelocatablePathMapper.ResolvedPath resolvedPath = mapper.getPath(name);
            if (resolvedPath == null) {
                if (sourceRoot != null) {
                    if (!name.startsWith(sourceRoot)) {
                        if (mapper.discover(fs, sourceRoot, name)) {
                            resolvedPath = mapper.getPath(name);
                            FileObject resolved = fileSystem.findResource(resolvedPath.getPath());
                            if (resolved != null && resolved.isValid() && resolved.isData()) {
                                ((Relocatable) f).resetItemPath(resolvedPath, mapper, fs);
                                return resolved;
                            }
                        }
                    }
                }
            } else {
                FileObject resolved = fileSystem.findResource(resolvedPath.getPath());
                if (resolved != null && resolved.isValid() && resolved.isData()) {
                    ((Relocatable) f).resetItemPath(resolvedPath, mapper, fs);
                    return resolved;
                }
            }
            sourceRoot = null;
            if (project != null) {
                sourceRoot = project.getSourceRoot();
                if (sourceRoot != null && sourceRoot.length() < 2) {
                    sourceRoot = null;
                }
            }
            if (sourceRoot == null) {
                sourceRoot = PathUtilities.getBaseName(name);
                if (sourceRoot != null && sourceRoot.length() < 2) {
                    sourceRoot = null;
                }
            }
            if (sourceRoot != null) {
                ((Relocatable) f).resolveIncludePaths(sourceRoot, mapper, fs);
            }
            return fo;
        }
        return null;
    }
        
    private boolean processArtifactFile(String file, Map<String, SourceFileProperties> map, Progress progress, ProjectProxy project, Set<String> dlls,
            List<String> buildArtifacts, Map<ItemProperties.LanguageKind,Map<String,Integer>> buildTools, CompileLineStorage storage) {
        if (isStoped.get()) {
            return true;
        }
        ProjectBridge bridge = null;
        if (project.getProject() != null) {
            bridge = new ProjectBridge(project.getProject());
        }
        String restrictSourceRoot = RESTRICT_SOURCE_ROOT_PROPERTY.getValue();
        if (restrictSourceRoot != null && !restrictSourceRoot.isEmpty()) {
            restrictSourceRoot = CndFileUtils.normalizeAbsolutePath(fileSystem, restrictSourceRoot);
        }
        String restrictCompileRoot = RESTRICT_COMPILE_ROOT_PROPERTY.getValue();
        if (restrictCompileRoot != null && !restrictCompileRoot.isEmpty()) {
            restrictCompileRoot = CndFileUtils.normalizeAbsolutePath(fileSystem, restrictCompileRoot);
        }
        for (SourceFileProperties f : getSourceFileProperties(file, map, project, dlls, buildArtifacts, buildTools, storage)) {
            if (isStoped.get()) {
                break;
            }
            String name = f.getItemPath();
            if (name == null) {
                continue;
            }
            if (restrictSourceRoot != null) {
                if (!name.startsWith(restrictSourceRoot)) {
                    continue;
                }
            }
            FileObject fo = resolvePath(project, file, f, name);
            if (fo == null) {
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE, "Not Exist {0}", name); // NOI18N
                }
                continue;
            }
            boolean skip = false;
            if (restrictCompileRoot != null) {
                if (f.getCompilePath() != null && !f.getCompilePath().startsWith(restrictCompileRoot)) {
                    skip = true;
                    if (bridge != null) {
                        String relPath = bridge.getRelativepath(fo.getPath());
                        Item item = bridge.getProjectItem(relPath);
                        if (item != null) {
                            skip = false;
                        }
                    }
                }
            }
            if (skip) {
                if (DwarfSource.LOG.isLoggable(Level.FINE)) {
                    DwarfSource.LOG.log(Level.FINE, "Skiped {0}", name); // NOI18N
                }
                continue;
            }

            name = fo.getPath();
            SourceFileProperties existed = map.get(name);
            if (existed == null) {
                map.put(name, f);
            } else {
                // Duplicated
                if (existed.getUserInludePaths().size() < f.getUserInludePaths().size()) {
                    map.put(name, f);
                } else if (existed.getUserInludePaths().size() == f.getUserInludePaths().size()) {
                    if (existed.getUserMacros().size() < f.getUserMacros().size()) {
                        map.put(name, f);
                    } else if (existed.getUserMacros().size() == f.getUserMacros().size()) {
                        if (macrosWeight(existed) < macrosWeight(f)) {
                            map.put(name, f);
                        } else {
                            // ignore
                        }
                    } else {
                        // ignore
                    }
                } else {
                    // ignore
                }
            }
        }
        if (progress != null) {
            synchronized(progress) {
                progress.increment(file);
            }
        }
        return false;
    }
    
    private int macrosWeight(SourceFileProperties f) {
        int sum = 0;
        for(String m : f.getUserMacros().keySet()) {
            for(int i = 0; i < m.length(); i++) {
                sum += m.charAt(i);
            }
        }
        return sum;
    }
        
    private class MyRunnable implements Runnable {
        private final String file;
        private final Map<String, SourceFileProperties> map;
        private final Progress progress;
        private final CountDownLatch countDownLatch;
        private final ProjectProxy project;
        private final Set<String> dlls;
        private final List<String> buildArtifacts;
        private final Map<ItemProperties.LanguageKind,Map<String,Integer>> buildTools;
        private final CompileLineStorage storage;

        private MyRunnable(CountDownLatch countDownLatch, String file, Map<String, SourceFileProperties> map, Progress progress, ProjectProxy project,
                Set<String> dlls, List<String> buildArtifacts, Map<ItemProperties.LanguageKind,Map<String,Integer>> buildTools, CompileLineStorage storage){
            this.file = file;
            this.map = map;
            this.progress = progress;
            this.countDownLatch = countDownLatch;
            this.project = project;
            this.dlls = dlls;
            this.buildArtifacts = buildArtifacts;
            this.buildTools = buildTools;
            this.storage = storage;
        }
        @Override
        public void run() {
            try {
                if (!isStoped.get()) {
                    Thread.currentThread().setName("Parallel analyzing "+file); // NOI18N
                    processArtifactFile(file, map, progress, project, dlls, buildArtifacts, buildTools, storage);
                }
            } finally {
                countDownLatch.countDown();
            }
        }
    }
    
    private static String i18n(String id) {
        return NbBundle.getMessage(BaseProvider.class, id);
    }    
}
