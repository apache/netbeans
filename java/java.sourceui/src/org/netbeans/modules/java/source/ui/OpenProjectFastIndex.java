/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.source.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.classpath.GlobalPathRegistryEvent;
import org.netbeans.api.java.classpath.GlobalPathRegistryListener;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.ClassIndexManagerEvent;
import org.netbeans.modules.java.source.usages.ClassIndexManagerListener;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.*;

/**
 * Maintains the 'fast index' which is available very shortly after project open.
 * Attaches to the GlobalPathRegistry, and when "pathAdded" event arrives WHILE opening
 * projects, creates a Watcher which collects further pathAdded events, until 
 * the OpenProjects terminates loading and finishes the {@link OpenProjects#openProjects()}.
 * future. Watcher runs in a private RequestProcessor and blocks on the Future
 * before computing added roots.
 * <p/>
 * Based on the PROPERTY_OPENED_PROJECTS, a list of <b>newly opened</b> projects is computed
 * as a difference between old/new event values. If a single project is added, that project's
 * source roots are scheduled for fast-indexing. If more projects is opened, one of them marked
 * "main", the main project's roots are added to fast-index. If the system cannot determine
 * the 'active' project, nothing happens.
 * <p/>
 * Because of OpenProjects implementation, the PROPERTY_MAIN_PROJECT property may be changed
 * <i>after</i> all projects load and the Future is finished. The Watcher first reschedules itself
 * a few milliseconds in the future, to have a chance to receive that event.
 * 
 * @author sdedic
 */
class OpenProjectFastIndex implements ClassIndexManagerListener {
    private static final Logger LOG = Logger.getLogger(OpenProjectFastIndex.class.getName());
    private static final Strategy  STRATEGY;
    static {
        Strategy s = Strategy.NBFS;
        final String propValue = System.getProperty("OpenProjectFastIndex.impl");   //NOI18N
        if (propValue != null) {
            try {
                s = Strategy.valueOf(propValue);
            } catch (IllegalArgumentException iae) {
                LOG.log(
                    Level.WARNING,
                    "Wrong impl: {0}",  //NNOI18N
                    propValue);
            }
        }
        STRATEGY = s;
    }

    /**
     * The default instance, used from TypeProvider and registered in GPR on startup.
     */
    private static final OpenProjectFastIndex DEFAULT = new OpenProjectFastIndex();
    
    /**
     * Request processor, which executes the task waiting on project completion
     */
    private static final RequestProcessor RP = new RequestProcessor("Project prescan initiator", 1);
    
    /**
     * PathRecognizers, which provide source path IDs.
     */
    private final Lookup.Result<PathRecognizer>   recognizers;
    
    /**
     * IDs of source paths; other than source changes from GPR will be ignored.
     */
    private volatile Set<String> sourcePathIds;
    
    /**
     * The current instance of the Watcher runnable.
     * The reference will be cleared when the Watcher receives 'project open' event.
     */
    private ProjectOpenWatcher watcher;
    
    private final GlobalPathRegistry globalRegistry;
    
    /**
     * Roots indexed by the fast indexer. Entries are added as a response to 
     * GPR change + project open; they are removed when GPR reports source root loss,
     * or the root is indexed by the real indexer.
     */
    // @GuardedBy(this)
    private final Map<FileObject, NameIndex>  indexedRoots = new HashMap<FileObject, NameIndex>();
    
    /**
     * Will collect roots removed during the scan; when all Watchers finish, the reference
     * will be released. The collection is shared between all scheduled Watchers.
     */
    // @GuardedBy(self)
    private Reference<Collection<FileObject>> removedRoots;
    
    /**
     * Cached project information. Added at the first request for project icon or name,
     * removed when the last source root from the project is removed from fast-index
     * (either indexed regularly or removed from the IDE).
     */
    // @GuardedBy(self)
    private final Map<Project, PI> projectInfoCache = new HashMap<Project, PI>();
    
    /**
     * Watcher count
     */
    // @GuardedBy(this)
    private int watchCount;
    
    private final LookupListener weakLookupL;
    private final GlobalPathRegistryListener weakGlobalL;
    private final LookupListener lookupL;
    private final GlobalPathRegistryListener globalL;
    
    public static OpenProjectFastIndex getDefault() {
        return DEFAULT;
    }
    
    OpenProjectFastIndex() {
        this(false);
        recognizers.addLookupListener(weakLookupL);
        globalRegistry.addGlobalPathRegistryListener(weakGlobalL);
        
        ClassIndexManager.getDefault().addClassIndexManagerListener(
                WeakListeners.create(
                    ClassIndexManagerListener.class, 
                    this, ClassIndexManager.getDefault()
                ) 
        );
    }
    
    OpenProjectFastIndex(boolean register) {
        this.globalRegistry = GlobalPathRegistry.getDefault();
        this.recognizers = Lookup.getDefault().lookupResult(PathRecognizer.class);
        updateSourceIds();
        weakLookupL = WeakListeners.create(LookupListener.class, lookupL = new LookupListener() {

            @Override
            public void resultChanged(LookupEvent ev) {
                if (ev.getSource() == recognizers) {
                    updateSourceIds();
                }
            }
        }, recognizers);
        weakGlobalL = WeakListeners.create(
                GlobalPathRegistryListener.class, 
                globalL = new GlobalPathRegistryListener() {
            @Override
            public void pathsAdded(GlobalPathRegistryEvent event) {
                if (STRATEGY.isEnabled()) {
                    Future<Project[]> projects = OpenProjects.getDefault().openProjects();
                    if (!sourcePathIds.contains(event.getId())) {
                        LOG.log(Level.FINE, "Non-source paths added: {0}", event.getId());
                        return;
                    }
                    if (projects.isDone()) {
                        LOG.log(Level.FINE, "Paths added, no project open in progress: {0}", event.getChangedPaths());
                        return;
                    }
                    getWatcher(projects).addChangedPaths(event.getChangedPaths());
                }
            }

            @Override
            public void pathsRemoved(GlobalPathRegistryEvent event) {
                if (STRATEGY.isEnabled()) {
                    if (!sourcePathIds.contains(event.getId())) {
                        LOG.log(Level.FINE, "Non-source removed: {0}", event.getId());
                        return;
                    }
                    Collection<FileObject> roots = getRoots(event.getChangedPaths());
                    LOG.log(Level.FINE, "Paths removed: {0}", roots);
                    removeRoots(roots);
                }
            }
        }, globalRegistry);
    }
    
    /**
     * Removes source roots from Watcher and the index. Should be called
     * when the roots become invalid (e.g. removed from IDE), or they are indexed
     * by real indexer.
     * 
     * @param roots roots to remove
     */
    private synchronized void removeRoots(Collection<FileObject> roots) {
        Collection c = removedRoots == null ? null : removedRoots.get();
        if (c != null) {
            c.addAll(roots);
        }
        LOG.log(Level.FINE, "Removing roots: {0}", roots);
        indexedRoots.keySet().removeAll(roots);
        Collection<Project> retainProjects = new ArrayList<Project>(indexedRoots.size());
        for (NameIndex ni : indexedRoots.values()) {
            retainProjects.add(ni.getProject());
        }
        synchronized (projectInfoCache) {
            Collection ks = projectInfoCache.keySet();
            ks.retainAll(retainProjects);
            LOG.log(Level.FINEST, "Retained project caches: {0}", ks);
        }
    }

    @Override
    public void classIndexAdded(ClassIndexManagerEvent event) {
        Collection<FileObject> c = getFileRoots(event.getRoots());
        LOG.log(Level.FINE, "Index updated, removing {0} ", c);
        removeRoots(c);
    }

    @Override
    public void classIndexRemoved(ClassIndexManagerEvent event) {
        Collection<FileObject> c = getFileRoots(event.getRoots());
        LOG.log(Level.FINE, "Roots removed from ClassIndexes, removing {0} ", c);
        removeRoots(c);
    }

    private Collection<FileObject>  getFileRoots(Iterable<? extends URL> roots) {
        Collection<FileObject> c = new ArrayList<>(5);
        for (Iterator<? extends URL> it = roots.iterator(); it.hasNext(); ) {
            URL rootURL = it.next();
            FileObject fo = URLMapper.findFileObject(rootURL);
            if (fo != null) {
                c.add(fo);
            }
        }
        return c;
    }

    private void updateSourceIds() {
        Set<String> ids = new HashSet<String>();
        for (PathRecognizer r : recognizers.allInstances()) {
            Set<String> rids = r.getSourcePathIds();
            if (rids != null) {
                ids.addAll(rids);
            }
        }
        sourcePathIds = ids;
    }

    /**
     * Returns the current watcher, if still accepting source roots.
     * Otherwise, creates AND schedules a new Watcher
     * 
     * @param f Future that the Watcher waits on, before computing source roots. 
     * @return Watcher instance
     */
    private synchronized ProjectOpenWatcher getWatcher(Future f) {
        if (watcher != null) {
            return watcher;
        }

        Collection<FileObject> removed = removedRoots == null ? null : removedRoots.get();

        if (removed == null) {
            removedRoots = new WeakReference<Collection<FileObject>>(removed = new HashSet<>());
        }
        watcher = new ProjectOpenWatcher(f, removed);
        watchCount++;
        LOG.fine("Starting project open watcher");
        RP.post(watcher);
        
        return watcher;
    }
    
    /**
     * Releases cached project information and clears removed roots list
     * when the last live Watcher completes.
     * @param w watcher instance that completed work
     */
    private synchronized void releaseWatcher(ProjectOpenWatcher w) {
        if (--watchCount == 0) {
            LOG.fine("Last watcher finished, flushing everything");
            removedRoots = null;
            synchronized (projectInfoCache) {
                projectInfoCache.clear();
            }
        }
    }

    // overriden by tests
    void updateNameIndexes(Project project, Collection<FileObject> roots) {
        IndexBuilder b = new IndexBuilder(project, roots, removedRoots.get());
        Map<FileObject, NameIndex> nameIndexes = b.build();

        Collection c = removedRoots.get();
        assert c != null : "The reference must not expire";
        LOG.log(Level.FINE, "Adding roots: {0}", roots);
        synchronized (this) {
            nameIndexes.keySet().removeAll(c);
            this.indexedRoots.putAll(nameIndexes);
        }
    }
    
    public Map<FileObject, NameIndex>  copyIndexes() {
        synchronized (this) {
            return new HashMap<FileObject, NameIndex>(indexedRoots);
        }
        
    }
    
    private static Set<FileObject> getRoots(Set<ClassPath> paths) {
        Set<FileObject> sroots = new HashSet<FileObject>();

        for (ClassPath cp : paths) {
            sroots.addAll(Arrays.asList(cp.getRoots()));
        }
        return sroots;
    }
    
    /**
     * This Task will try to select a project from among the opened projects. Instantiated
     * when the Listener receives a path change WHILE opening projects - a candidate for
     * pre-scanning.
     * <p/>
     * The class only lives for ONE project open. If another project opens, the running instance
     * should be canceled - it will refuse to add additional paths anyway.
     * 
     */
    class ProjectOpenWatcher implements Runnable, PropertyChangeListener {
        private int state;
        
        private final Future waitFor;
        
        /**
        * The main project, or null if main project change was not reported.
        */
        private volatile Project mainProject;

        /**
         * Source paths, which should be eventually indexed with higher priority upon project open
         */
        private Set<FileObject> rootsToIndex = new HashSet<FileObject>();
        
        /**
         * Will be filled when PROPERTY_OPENED_PROJECTS is received, with new projects
         * reported in that property change.
         */
        private Collection<Project> newProjects;
        
        private Collection<FileObject>         removedRoots;

        public ProjectOpenWatcher(Future waitFor, Collection<FileObject> removedRoots) {
            this.waitFor = waitFor;
            this.removedRoots = removedRoots;
            OpenProjects.getDefault().addPropertyChangeListener(this);
        }
        
        public void run() {
            int state;

            synchronized (this) {
                state = this.state++;
            }
            if (state == 0) {
                try {
                    waitFor.get();
                    // close this watcher
                    synchronized (OpenProjectFastIndex.this) {
                        watcher = null;
                        synchronized (this) {
                            this.state++;   
                        }
                    }
                    // aargh, this is because PROPERTY_MAIN_PROJECT is fired after the Future has been released
                    RP.schedule(this, 200, TimeUnit.MILLISECONDS);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                OpenProjects.getDefault().removePropertyChangeListener(this);
                try {
                    // second pass, presumably after MAIN_PROJECT has been fired
                    Project selected = selectProject(newProjects);
                    processProject(selected);
                } finally {
                    releaseWatcher(this);
                }
            }
        }
        
        /**
         * Preindexes the 'selected' project.
         * @param selected 
         */
        void processProject(Project selected) {
            Set<ClassPath> paths;
            if (selected == null) {
                return;
            }
            for (Iterator<FileObject> it = rootsToIndex.iterator(); it.hasNext(); ) {
                FileObject f = it.next();
                // check whether the root has not been indexed yet:
                if (JavaIndex.isIndexed(f.toURL())) {
                    it.remove();
                    continue;
                }

                Project p = FileOwnerQuery.getOwner(f);
                if (p != selected) {
                    it.remove();
                }
            }
            if (rootsToIndex.isEmpty()) {
                LOG.log(Level.FINE, "Nothing to preindex");
                return;
            }
            // finally, run the preindexing job
            
            updateNameIndexes(selected, rootsToIndex);
        }

        public boolean addChangedPaths(Set<ClassPath> paths) {
            Set<FileObject> sroots = getRoots(paths);
            // filter & record source paths that were added for later processing
            synchronized (this) {
                if (state > 1) {
                    return false;
                }
                LOG.log(Level.FINE, "Added source paths: {0}", paths);
                rootsToIndex.addAll(sroots);
            }
            return true;
        }

        private void matchOpenProjects(Project[] old, Project[] current) {
            Map<FileObject, Project> projects = new HashMap<FileObject, Project>();
            for (Project p : current) {
                projects.put(p.getProjectDirectory(), p);
            }
            // remove projects, which were already opened before the operation
            for (Project p : old) {
                projects.remove(p.getProjectDirectory());
            }
            
            newProjects = projects.values();
        }   

        private Project selectProject(Collection<Project> newlyOpenedProjects) {
            if (newlyOpenedProjects.size() == 1) {
                return newlyOpenedProjects.iterator().next();
            }
            if (newlyOpenedProjects.contains(mainProject)) {
                return mainProject;
            }
            return null;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
                matchOpenProjects((Project[])evt.getOldValue(), (Project[])evt.getNewValue());
            } else if (OpenProjects.PROPERTY_MAIN_PROJECT.equals(evt.getPropertyName())) {
                this.mainProject = (Project)evt.getNewValue();
            }
        }
    }
    
    public String getProjectName(Project p) {
        if (p == null) {
            return null;
        }
        return getCacheInfo(p).projectName;
    }
    
    public Icon getProjectIcon(Project p) {
        if (p == null) {
            return null;
        }
        return getCacheInfo(p).projectIcon;
    }
    
    private PI getCacheInfo(Project p) {
        synchronized (projectInfoCache) {
            PI pi = projectInfoCache.get(p);
            if (pi != null) {
                return pi;
            }
        }
        ProjectInformation info =  ProjectUtils.getInformation( p );
        PI pi = new PI();
        pi.projectName = info.getDisplayName();
        pi.projectIcon = info.getIcon();
        synchronized (projectInfoCache) {
            projectInfoCache.put(p, pi);
        }
        return pi;
    }
    
    private static final class PI {
        Icon    projectIcon;
        String  projectName;
    }
    
    /**
     * Index of filenames within a certain source root
     */
    static class NameIndex {
        private final Reference<FileObject>  root;
        /**
         * Newline-delimited filenames
         */
        private final String      fileNames;
        
        /**
         * From 0..size - 1, contains indexes in filenames, where a dir content begins.
         * name of particular directory can be found in dirNames at the same index. 
         * On positions size...size * 2 - 1, this array contains index of parent dir or -1.
         */
        private final int[]       dirStartPositions;
        private final String[]    dirNames;
        private final int         size;
        private final Project     project;
        
        NameIndex(Project p, FileObject root, String files, List<Object[]> indices) {
            this.project = p;
            this.root = new WeakReference<>(root);
            this.size = indices.size();
            this.fileNames = files;
            
            this.dirStartPositions = new int[size * 2];
            this.dirNames = new String[size];
            for (int i = size - 1; i >= 0; i--) {
                Object[] data = indices.get(i);
                dirStartPositions[i] = (Integer)data[1];
                dirStartPositions[i + size] = (Integer)data[2];
                dirNames[i] = (String)data[0];
            }
        }
        
        public CharSequence files() {
            return fileNames;
        }
        
        public CharSequence getFilename(int matchFrom, int matchTo) {
            int startLine = fileNames.lastIndexOf('\n', matchFrom); // NOI18N
            do {
                startLine++;
            } while (fileNames.charAt(startLine) == ' ');
            int endLine = fileNames.indexOf('\n', matchTo); // NOI18N
            return fileNames.subSequence(startLine, endLine);
        }
        
        public String findPath(int namePos) {
            int atIndex = Arrays.binarySearch(dirStartPositions, 0, size, namePos);
            if (atIndex < 0) {
                atIndex = -(atIndex + 1) - 1;
            }
            return appendParents(new StringBuilder(), atIndex).toString();
        }
        
        private StringBuilder appendParents(StringBuilder sb, int dirIndex) {
            int parentIndex = dirStartPositions[size + dirIndex];
            if (parentIndex > 0) {
                appendParents(sb, parentIndex).append("."); // NOI18N
            }
            sb.append(dirNames[dirIndex]);
            return sb;
        }
        
        public FileObject getRoot() {
            return root.get();
        }
        
        public Project  getProject() {
            return project;
        }
    }
    
    /**
     * The index builder; builds file index from dist contents.
     */
    static class IndexBuilder {
        private Collection<FileObject>  rootsToScan;
        private StringBuilder       filenames;
        private Collection<FileObject>     removedRoots;
        private Project             project;
        
        private Map<FileObject, NameIndex>  nameIndexes = new HashMap<FileObject, NameIndex>();
        private List<Object[]>     dirPositions;
        private int                charPtr;

        public IndexBuilder(Project project, Collection<FileObject> rootsToScan, Collection<FileObject> removedRoots) {
            this.rootsToScan = rootsToScan;
            this.removedRoots = removedRoots;
            this.project = project;
        }
        
        private void reset() {
            filenames = new StringBuilder();
            dirPositions = new ArrayList<>();
            charPtr = 0;
        }
        
        public Map<FileObject, NameIndex> build() {
            for (FileObject root : rootsToScan) {
                synchronized (removedRoots) {
                    if (removedRoots.contains(root)) {
                        continue;
                    }
                }
                
                reset();
                scanDir(root, -1);
                
                NameIndex ni = new NameIndex(project, root, filenames.toString(), dirPositions);
                nameIndexes.put(root, ni);
            }
            return nameIndexes;
        }
        
        private void scanDir(FileObject d, int parentIndex) {
            int myIndex = dirPositions.size();

            dirPositions.add(new Object[] {
                d.getNameExt(),
                charPtr,
                parentIndex
            });

            boolean someFile = false;
            for (Enumeration<? extends FileObject> en = d.getData(false); en.hasMoreElements(); ) {
                FileObject f = en.nextElement();
                String fn = f.getName();
                if (Utilities.isJavaIdentifier(fn)) {
                    if (JavaDataLoader.JAVA_MIME_TYPE.equals(f.getMIMEType())) {
                        someFile = true;
                        filenames.append(fn).append("\n"); // NOI18N
                        charPtr += fn.length() + 1;
                    }
                }
            } 
            if (!someFile) {
                filenames.append("\n"); // NOI18N
                charPtr++;
            }
            for (Enumeration<? extends FileObject> en = d.getFolders(false); en.hasMoreElements(); ) {
                FileObject f = en.nextElement();
                if (Utilities.isJavaIdentifier(f.getNameExt())) {
                    scanDir(f, myIndex);
                }
            }
        }
    }

    private enum Strategy {
        NONE {
            @Override
            boolean isEnabled() {
                return false;
            }
        },
        NBFS {
            @Override
            boolean isEnabled() {
                return true;
            }
        };

        abstract boolean isEnabled();
    }
}
