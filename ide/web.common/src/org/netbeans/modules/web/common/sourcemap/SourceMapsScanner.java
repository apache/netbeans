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
package org.netbeans.modules.web.common.sourcemap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.DependencyProjectProvider;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 * Scanner for source maps in the projects scope.
 * 
 * @author Martin Entlicher
 */
public final class SourceMapsScanner {

    private static final Logger LOG = Logger.getLogger(SourceMapsScanner.class.getName());

    private static final String SRC_MAP_EXT = "map";       // NOI18N
    
    private static final SourceMapsScanner INSTANCE = new SourceMapsScanner();
    
    private final Map<Project, ProjectSourceMapsScanner> projectsMaps = new WeakHashMap<>();

    private SourceMapsScanner() {
    }

    /**
     * Get a default instance of this scanner.
     * @return The default scanner instance.
     */
    public static SourceMapsScanner getInstance() {
        return INSTANCE;
    }

    /**
     * Get a {@link SourceMapsTranslator} based on all source maps found in the
     * project's scope. The call blocks until scanning is finished.
     * @param p The project
     * @return A new or cached instance of {@link SourceMapsTranslator}
     */
    public SourceMapsTranslator scan(Project p) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("scan("+p+"), proj. dir = "+p.getProjectDirectory());
        }
        ProjectSourceMapsScanner ps;
        boolean isNew = false;
        synchronized (projectsMaps) {
            ps = projectsMaps.get(p);
            if (ps == null) {
                ps = new ProjectSourceMapsScanner();
                projectsMaps.put(p, ps);
                isNew = true;
            }
        }
        if (isNew) {
            ps.init(p);
        }
        ps.waitScanned();
        
        return ps.getSourceMapTranslator();
    }

    /**
     * Create a {@link SourceMapsTranslator} based on all source maps found under
     * the provided roots. The call blocks until scanning is finished.
     * @param roots The source roots to scan
     * @return A new instance of {@link SourceMapsTranslator}
     */
    public SourceMapsTranslator scan(FileObject[] roots) {
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("scan("+Arrays.toString(roots)+")");
        }
        ProjectSourceMapsScanner ps = new ProjectSourceMapsScanner();
        ps.init(roots);
        ps.waitScanned();
        return ps.getSourceMapTranslator();
    }
    
    private static final class ProjectSourceMapsScanner implements ChangeListener,
                                                                   FileChangeListener,
                                                                   Runnable {
        
        private static final RequestProcessor RP = new RequestProcessor(ProjectSourceMapsScanner.class);
        
        private final SourceMapsTranslator smt;
        private FileObject[] roots;
        private final Object rootsLock = new Object();
        private final Set<FileObject> rootsToScan = new HashSet<>();
        private final Map<FileObject, SourceMap> sourceMaps = new HashMap<>();
        private RequestProcessor.Task scanningTask;
        private final Object scanningTaskLock = new Object();
        private ProjectDependencyManager projectDependencyManager;
        
        ProjectSourceMapsScanner() {
            this.smt = new SourceMapsTranslatorDelegate(SourceMapsTranslator.create());
        }
        
        SourceMapsTranslator getSourceMapTranslator() {
            return smt;
        }
        
        void init(Project p) {
            Sources sources = ProjectUtils.getSources(p);
            sources.addChangeListener(this);
            SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            roots = new FileObject[groups.length];
            for (int i = 0; i < groups.length; i++) {
                SourceGroup group = groups[i];
                FileObject rootFolder = group.getRootFolder();
                roots[i] = rootFolder;
                rootFolder.addRecursiveListener(this);
                //scan(rootFolder);
                rootsToScan.add(rootFolder);
            }
            scanningTask = RP.post(this);
            DependencyProjectProvider prov = p.getLookup().lookup(DependencyProjectProvider.class);
            if (prov != null) {
                projectDependencyManager = new ProjectDependencyManager(prov);
            }
        }
        
        void init(FileObject[] roots) {
            this.roots = roots;
            for (int i = 0; i < roots.length; i++) {
                roots[i].addRecursiveListener(this);
                rootsToScan.add(roots[i]);
            }
            scanningTask = RP.post(this);
        }
        
        void waitScanned() {
            synchronized (scanningTaskLock) {
                if (scanningTask != null) {
                    scanningTask.waitFinished();
                }
            }
        }
        
        @Override
        public void run() {
            // Scan rootsToScan:
            FileObject[] roots;
            synchronized (rootsToScan) {
                roots = rootsToScan.toArray(new FileObject[0]);
            }
            for (FileObject root : roots) {
                scan(root);
            }
        }
        
        private void scan(FileObject root) {
            LOG.log(Level.FINE, "Scanning folder {0}", root);
            Enumeration<? extends FileObject> children = root.getChildren(true);
            while (children.hasMoreElements()) {
                FileObject fo = children.nextElement();
                registerIfSourceMap(fo);
            }
        }
        
        private void registerIfSourceMap(FileObject fo) {
            if (!fo.isValid() || !fo.isData() ||
                !fo.getExt().equalsIgnoreCase(SRC_MAP_EXT) ||
                !fo.canRead()) {

                return ;
            }
            LOG.log(Level.FINE, "  found source map (?) {0}", fo);
            SourceMap sm;
            try {
                sm = SourceMap.parse(fo.asText("UTF-8"));
            } catch (IOException | IllegalArgumentException ex) {
                return ;
            }
            FileObject compiledFO;
            String compiledFile = sm.getFile();
            if (compiledFile == null) {
                compiledFO = findCompiledFile(fo);
            } else {
                compiledFO = fo.getParent().getFileObject(compiledFile);
            }
            LOG.log(Level.FINE, "  have source map for generated file {0}", compiledFO);
            if (compiledFO != null) {
                synchronized (sourceMaps) {
                    sourceMaps.put(fo, sm);
                }
                smt.registerTranslation(compiledFO, sm);
            }
        }
        
        private void unregisterSourceMap(FileObject fo) {
            SourceMap sm;
            synchronized (sourceMaps) {
                sm = sourceMaps.remove(fo);
            }
            if (sm != null) {
                FileObject compiledFO;
                String compiledFile = sm.getFile();
                if (compiledFile == null) {
                    compiledFO = findCompiledFile(fo);
                } else {
                    compiledFO = fo.getParent().getFileObject(compiledFile);
                }
                smt.unregisterTranslation(compiledFO);
            }
        }
        
        private static FileObject findCompiledFile(FileObject sourceMapFile) {
            String name = sourceMapFile.getName();
            FileObject parent = sourceMapFile.getParent();
            FileObject compiledFO = parent.getFileObject(name);
            if (compiledFO != null) {
                return compiledFO;
            }
            compiledFO = parent.getFileObject(name, "js");
            if (compiledFO != null) {
                return compiledFO;
            }
            return null;
        }

        /**
         * Sources changed.
         * @param e 
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            Sources sources = (Sources) e.getSource();
            SourceGroup[] groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            FileObject[] roots2 = new FileObject[groups.length];
            List<FileObject> newRoots = new ArrayList<>(groups.length);
            List<FileObject> removedRoots;
            for (int i = 0; i < groups.length; i++) {
                SourceGroup group = groups[i];
                FileObject rootFolder = group.getRootFolder();
                roots2[i] = rootFolder;
                newRoots.add(rootFolder);
            }
            synchronized (rootsLock) {
                removedRoots = new ArrayList<>(roots.length);
                for (FileObject r : roots) {
                    if (!newRoots.remove(r)) {
                        removedRoots.add(r);
                    }
                }
                roots = roots2;
            }
            // Release removedRoots
            if (!removedRoots.isEmpty()) {
                List<FileObject> removedSourceMaps = new ArrayList<>();
                synchronized (sourceMaps) {
                    for (FileObject fo : sourceMaps.keySet()) {
                        for (FileObject rr : removedRoots) {
                            if (FileUtil.isParentOf(rr, fo)) {
                                removedSourceMaps.add(fo);
                                break;
                            }
                        }
                    }
                    sourceMaps.keySet().removeAll(removedSourceMaps);
                }
                for (FileObject fo : removedSourceMaps) {
                    unregisterSourceMap(fo);
                }
            }
            // Add newRoots
            synchronized (rootsToScan) {
                rootsToScan.addAll(newRoots);
            }
            scanningTask = RP.post(this);
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {}

        @Override
        public void fileDataCreated(FileEvent fe) {
            registerIfSourceMap(fe.getFile());
        }

        @Override
        public void fileChanged(FileEvent fe) {
            FileObject fo = fe.getFile();
            unregisterSourceMap(fo);
            registerIfSourceMap(fo);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            FileObject fo = fe.getFile();
            unregisterSourceMap(fo);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {}
        
        private static class SourceMapsTranslatorDelegate implements SourceMapsTranslator {
            
            private final SourceMapsTranslator mainSmt;
            private final List<SourceMapsTranslator> delegates = new CopyOnWriteArrayList<>();
            
            public SourceMapsTranslatorDelegate(SourceMapsTranslator smt) {
                this.mainSmt = smt;
                delegates.add(smt);
            }
            
            void add(SourceMapsTranslator smt) {
                delegates.add(smt);
            }
            
            void remove(SourceMapsTranslator smt) {
                delegates.remove(smt);
            }

            void removeAllButTheFirst() {
                delegates.retainAll(delegates.subList(0, 1));
            }
            
            @Override
            public boolean registerTranslation(FileObject source, String sourceMapFileName) {
                return mainSmt.registerTranslation(source, sourceMapFileName);
            }

            @Override
            public boolean registerTranslation(FileObject source, SourceMap sourceMap) {
                return mainSmt.registerTranslation(source, sourceMap);
            }

            @Override
            public void unregisterTranslation(FileObject source) {
                mainSmt.unregisterTranslation(source);
            }
            
            @Override
            public Location getSourceLocation(Location loc) {
                for (SourceMapsTranslator smt : delegates) {
                    Location l = smt.getSourceLocation(loc);
                    if (l != loc) {
                        return l;
                    }
                }
                return loc;
            }

            @Override
            public Location getSourceLocation(Location loc, String sourceMapFileName) {
                for (SourceMapsTranslator smt : delegates) {
                    Location l = smt.getSourceLocation(loc, sourceMapFileName);
                    if (l != loc) {
                        return l;
                    }
                }
                return loc;
            }

            @Override
            public Location getCompiledLocation(Location loc) {
                for (SourceMapsTranslator smt : delegates) {
                    Location l = smt.getCompiledLocation(loc);
                    if (l != loc) {
                        return l;
                    }
                }
                return loc;
            }

            @Override
            public List<FileObject> getSourceFiles(FileObject compiledFile) {
                for (SourceMapsTranslator smt : delegates) {
                    List<FileObject> sourceFiles = smt.getSourceFiles(compiledFile);
                    if (sourceFiles != null) {
                        return sourceFiles;
                    }
                }
                return null;
            }

        }
        
        private class ProjectDependencyManager implements ChangeListener {

            public ProjectDependencyManager(DependencyProjectProvider prov) {
                prov.addChangeListener(this);
                init(prov);
            }
            
            private void init(DependencyProjectProvider prov) {
                DependencyProjectProvider.Result res = prov.getDependencyProjects();
                Set<? extends Project> projects = res.getProjects();
                SourceMapsTranslatorDelegate smtd = (SourceMapsTranslatorDelegate) ProjectSourceMapsScanner.this.smt;
                smtd.removeAllButTheFirst();
                if (!projects.isEmpty()) {
                    for (Project p : projects) {
                        SourceMapsTranslator psmt = INSTANCE.scan(p);
                        smtd.add(psmt);
                    }
                }
            }

            @Override
            public void stateChanged(ChangeEvent e) {
                DependencyProjectProvider prov = (DependencyProjectProvider) e.getSource();
                init(prov);
            }
        }
    }

}
