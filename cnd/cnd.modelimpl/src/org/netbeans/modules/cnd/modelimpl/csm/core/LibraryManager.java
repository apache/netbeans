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
package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.impl.spi.LayeringSupport;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.repository.support.AbstractObjectFactory;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;
import org.openide.util.Parameters;

/**
 * Artificial libraries manager.
 * Manage auto created libraries (artificial libraries) for included files.
 *
 *
 */
public final class LibraryManager {

    private static final Map<Integer, LibraryManager> instances = new HashMap<>();

    private final int repositoryId;

    public  static LibraryManager getInstance(int sourceUnitId) {
        LayeringSupport layeringSupport = Repository.getLayeringSupport(sourceUnitId);
        if (layeringSupport == null) {
            return null;
        }
        int repositoryId = layeringSupport.getStorageID();
        return getInstanceByRepositoryId(repositoryId);
    }

    private static LibraryManager getInstanceByRepositoryId(int repositoryId) {
        synchronized (instances) {
            LibraryManager manager = instances.get(repositoryId);
            if (manager == null) {
                manager = new LibraryManager(repositoryId);
                instances.put(repositoryId, manager);
            }
            return manager;
        }
    }

    private LibraryManager(int repositoryId) {
        this.repositoryId = repositoryId;
    }

    private final ConcurrentHashMap<LibraryKey, LibraryEntry> librariesEntries = new ConcurrentHashMap<>();

    private static final class Lock {}
    private final Object lock = new Lock();

    public static void shutdown(){
        Collection<LibraryManager> list;
        synchronized (instances) {
            list = instances.values();
        }
        for (LibraryManager manager : list) {
            manager.shutdownImpl();
        }
    }

    private void shutdownImpl(){
        librariesEntries.clear();
    }

    /**
     * Returns collection of artificial libraries used in project
     */
    public List<LibProjectImpl> getLibraries(ProjectImpl project) {
        List<LibProjectImpl> res = new ArrayList<>();
        CsmUID<CsmProject> projectUid = project.getUID();
        for (LibraryEntry entry : librariesEntries.values()) {
            if (entry.containsProject(projectUid)) {
                CsmUID<CsmProject> library = entry.getLibrary();
                CsmProject lib = library.getObject();
                if (lib instanceof LibProjectImpl) {
                    res.add((LibProjectImpl)lib);
                }
            }
        }
        return res;
    }

    public Collection<ProjectBase> getProjectsByLibrary(LibProjectImpl library) {
        //getDependentProjects();
        LibraryKey libraryKey = new LibraryKey(library.getFileSystem(), library.getPath());
        LibraryEntry entry = librariesEntries.get(libraryKey);
        if (entry == null) {
            return Collections.<ProjectBase>emptyList();
        } else {
            Collection<CsmUID<CsmProject>> uids = entry.getDependentProjects();
            List<ProjectBase> projects = new ArrayList<>(uids.size());
            for (CsmUID<CsmProject> uid : uids) {
                ProjectBase project = (ProjectBase) uid.getObject();
                if (project != null && ! project.isDisposing() && project.isValid()) {
                    projects.add(project);
                }
            }
            return projects;
        }
    }

    /**
     * Returns collection uids of artificial libraries used in project
     */
    public Collection<CsmUID<CsmProject>> getLirariesKeys(CsmUID<CsmProject> projectUid) {
        List<CsmUID<CsmProject>> res = new ArrayList<>();
        for (LibraryEntry entry : librariesEntries.values()) {
            if (entry.containsProject(projectUid)) {
                res.add(entry.getLibrary());
            }
        }
        return res;
    }

    private void trace(String where, FileImpl curFile, ResolvedPath resolvedPath, ProjectBase res, ProjectBase start) {
        System.out.println("Resolved Path " + resolvedPath.getPath()); //NOI18N
        System.out.println("    start project " + start); //NOI18N
        System.out.println("    found in " + where + " " + res); //NOI18N
        System.out.println("    included from " + curFile); //NOI18N
        System.out.println("    file from project " + curFile.getProject()); //NOI18N
        for (CsmProject prj : start.getLibraries()) {
            System.out.println("    search lib " + prj); //NOI18N
        }
    }

    /**
     * Find project for resolved file.
     * Search for project in project, dependencies, artificial libraries.
     * If search is false then method creates artificial library or returns base project.
     *
     * Can return NULL !
     */
    public ProjectBase resolveFileProjectOnInclude(ProjectBase baseProject, FileImpl curFile, ResolvedPath resolvedPath) {
        CharSequence absPath = resolvedPath.getPath();
        Set<ProjectBase> antiLoop = new HashSet<>();
        ProjectBase res = searchInProjectFiles(baseProject, resolvedPath, antiLoop);
        if (res != null) {
            baseProject.prepareIncludeStorage(res);
            if (TraceFlags.TRACE_RESOLVED_LIBRARY) {
                trace("Projects", curFile, resolvedPath, res, baseProject);//NOI18N
            }
            return res;
        }
        final CharSequence folder = resolvedPath.getFolder(); // always normalized
        antiLoop.clear();
        res = searchInProjectRoots(baseProject, resolvedPath.getFileSystem(), getPathToFolder(folder, absPath), antiLoop);
        if (res != null) {
            baseProject.prepareIncludeStorage(res);
            if (TraceFlags.TRACE_RESOLVED_LIBRARY) {
                trace("Projects roots", curFile, resolvedPath, res, baseProject);//NOI18N
            }
            return res;
        }
        List<CsmProject> libraries = baseProject.getLibraries();
        res = searchInProjectFilesArtificial(libraries, resolvedPath, antiLoop);
        if (res != null) {
            baseProject.prepareIncludeStorage(res);
            if (TraceFlags.TRACE_RESOLVED_LIBRARY) {
                trace("Libraries", curFile, resolvedPath, res, baseProject);//NOI18N
            }
            return res;
        }
        synchronized (lock) {
            antiLoop.clear();
            res = searchInProjectRootsArtificial(libraries, resolvedPath.getFileSystem(), getPathToFolder(folder, absPath), antiLoop);
            if (res == null) {
                if (resolvedPath.isDefaultSearchPath()) {
                    res = curFile.getProjectImpl(true);
                    if (res != null) {
                        if (resolvedPath.getFileSystem() == res.getFileSystem()) {
                            if (TraceFlags.TRACE_RESOLVED_LIBRARY) {
                                trace("Base Project as Default Search Path", curFile, resolvedPath, res, baseProject);//NOI18N
                            }
                        } else {
                            CndUtils.assertTrue(false, "Wrong FS for " + resolvedPath + ": " + res.getFileSystem() + " vs " + resolvedPath.getFileSystem()); // NOI18N
                            res = null;
                        }
                    }
                    // if (res != null && res.isArtificial()) { // TODO: update lib source roots here }
                } else if (!baseProject.isArtificial()) {
                    res = getLibrary((ProjectImpl) baseProject, resolvedPath.getFileSystem(), folder);
                    if (res == null && CndUtils.isDebugMode() && baseProject.isValid() ) {
                        CndUtils.assertTrue(false, "Can not create library; folder=" + folder + " curFile=" + //NOI18N
                                curFile + " path=" + resolvedPath + " baseProject=" + baseProject); //NOI18N
                    }
                    if (TraceFlags.TRACE_RESOLVED_LIBRARY) {
                        trace("Library for folder " + folder, curFile, resolvedPath, res, baseProject); //NOI18N
                    }
                } else {
                    if (CndUtils.isDebugMode() && baseProject.isValid()) {
                        CndUtils.assertTrue(false, "Can not get library for artificial project; ","folder=" + folder + " curFile=" + //NOI18N
                                curFile + " path=" + resolvedPath + " baseProject=" + baseProject); //NOI18N
                    }
                    if (TraceFlags.TRACE_RESOLVED_LIBRARY) {
                        trace("Base Project", curFile, resolvedPath, res, baseProject);//NOI18N
                    }
                }
            } else {
                if (TraceFlags.TRACE_RESOLVED_LIBRARY) {
                    trace("Libraries roots", curFile, resolvedPath, res, baseProject);//NOI18N
                }
            }
        }
        if (res != null) {
            baseProject.prepareIncludeStorage(res);
        }
        return res;
    }

    private List<CharSequence> getPathToFolder(CharSequence folder, CharSequence path) {
        List<CharSequence> res = new ArrayList<>(3);
        res.add(folder);
        if (CharSequenceUtils.startsWith(path, folder)) {
            while (true) {
                CharSequence dir = getDirName(path);
                if (dir == null || (CharSequences.comparator().compare(folder, dir) == 0) || !CharSequenceUtils.startsWith(dir, folder)) {
                    break;
                }
                res.add(dir);
                if (res.size() == 3) {
                    break;
                }
                path = dir;
            }
        }
        return res;
    }
    private static CharSequence getDirName(CharSequence path) {
        if (path == null) {
            return null;
        }
        path = trimRightSlashes(path);
        int sep = CharSequenceUtils.lastIndexOf(path, '/');
        if (sep == -1) {
            sep = CharSequenceUtils.lastIndexOf(path, '\\');
        }
        if (sep != -1) {
            return trimRightSlashes(path.subSequence(0, sep));
        }
        return null;
    }

    private static CharSequence trimRightSlashes(CharSequence path) {
        int length = path.length();
        while (length > 0 && (path.charAt(length - 1) == '\\' || path.charAt(length - 1) == '/')) {
            path = path.subSequence(0, length - 1);
            break;
        }
        return path;
    }

    private static ProjectBase searchInProjectFiles(ProjectBase baseProject, ResolvedPath searchFor, Set<ProjectBase> set) {
        if (set.contains(baseProject)) {
            return null;
        }
        set.add(baseProject);
        if (baseProject.getFileSystem() == searchFor.getFileSystem()) {
            baseProject.ensureFilesCreated();
            CsmUID<CsmFile> file = baseProject.getFileUID(searchFor.getPath(), true);
            if (file != null) {
                return baseProject;
            }
        }
        List<CsmProject> libraries = baseProject.getLibraries();
        for (CsmProject prj : libraries) {
            if (prj.isArtificial()) {
                break;
            }
            ProjectBase res = searchInProjectFiles((ProjectBase) prj, searchFor, set);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    private static ProjectBase searchInProjectFilesArtificial(List<CsmProject> libraries, ResolvedPath searchFor, Set<ProjectBase> antiLoop) {
        for (CsmProject prj : libraries) {
            if (prj.isArtificial()) {
                antiLoop.clear();
                ProjectBase res = searchInProjectFiles((ProjectBase) prj, searchFor, antiLoop);
                if (res != null) {
                    return res;
                }
            }
        }
        return null;
    }

    private static ProjectBase searchInProjectRoots(ProjectBase baseProject, FileSystem fs, List<CharSequence> folders, Set<ProjectBase> set) {
        if (set.contains(baseProject)) {
            return null;
        }
        set.add(baseProject);
        if (baseProject.getFileSystem() == fs) {
            for (CharSequence folder : folders) {
                if (baseProject.isMySource(folder)) {
                    return baseProject;
                }
            }
        }
        List<CsmProject> libraries = baseProject.getLibraries();
        for (CsmProject prj : libraries) {
            if (prj.isArtificial()) {
                break;
            }
            ProjectBase res = searchInProjectRoots((ProjectBase) prj, fs, folders, set);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    private static ProjectBase searchInProjectRootsArtificial(List<CsmProject> libraries, FileSystem fs, List<CharSequence> folders, Set<ProjectBase> set) {
        ProjectBase candidate = null;
        for (CsmProject prj : libraries) {
            if (prj.isArtificial()) {
                set.clear();
                ProjectBase res = searchInProjectRoots((ProjectBase) prj, fs, folders, set);
                if (res != null) {
                    if (candidate == null) {
                        candidate = res;
                    } else {
                        CharSequence path1 = ((LibProjectImpl)candidate).getPath();
                        CharSequence path2 = ((LibProjectImpl)res).getPath();
                        if (path2.length() > path1.length()) {
                            candidate = res;
                        }
                    }
                }
            }
        }
        return candidate;
    }

    private LibProjectImpl getLibrary(ProjectImpl project, FileSystem fs, CharSequence folder) {
        CsmUID<CsmProject> projectUid = project.getUID();
        LibraryKey libraryKey = new LibraryKey(fs, folder);
        LibraryEntry entry = librariesEntries.get(libraryKey);
        if (entry == null) {
            if (!project.isValid()) {
                return null;
            }
            entry = getOrCreateLibrary(libraryKey);
        }
        if (!entry.containsProject(projectUid)) {
            entry.addProject(projectUid);
            Notificator.instance().registerChangedLibraryDependency(project);
            Notificator.instance().flush(); // should we rely on subsequent flush instead?
        }
        return (LibProjectImpl) entry.getLibrary().getObject();
    }

    private LibraryEntry getOrCreateLibrary(LibraryKey libraryKey) {
        LibraryEntry entry = librariesEntries.get(libraryKey);
        if (entry == null) {
            boolean needFire = false;
            entry = new LibraryEntry(libraryKey);
            LibraryEntry old = librariesEntries.putIfAbsent(libraryKey, entry);
            if (old == null) {
                needFire = true;
            } else {
                entry = old;
            }
            if (needFire) {
                final LibraryEntry passEntry = entry;
                ModelImpl.instance().enqueueModelTask(new Runnable() {

                    @Override
                    public void run() {
                        ListenersImpl.getImpl().fireProjectOpened((ProjectBase) passEntry.getLibrary().getObject());
                    }
                }, "postponed library opened " + libraryKey.fileSystem + ":" + libraryKey.folder); // NOI18N
            }
        }
        return entry;
    }

    public void onProjectPropertyChanged(ProjectBase project) {
        project.getGraph().clear();
        CsmUID<CsmProject> uid = project.getUID();
        boolean notify = false;
        for (LibraryEntry entry : librariesEntries.values()) {
            Boolean removed = entry.removeProject(uid);
            if (removed != null) {
                project.invalidateLibraryStorage(entry.libraryUID);
                notify = true;
            }
        }
        if (notify) {
            Notificator.instance().registerChangedLibraryDependency(project);
            Notificator.instance().flush(); // should we rely on subsequent flush instead?
        }
    }

    /**
     * Close unused artificial libraries.
     */
    public void onProjectClose(CsmUID<CsmProject> project, boolean cleanRepository) {
        List<LibraryEntry> toClose = new ArrayList<>();
        for (LibraryEntry entry : librariesEntries.values()) {
            entry.removeProject(project);
            if (entry.isEmpty()) {
                toClose.add(entry);
            }
        }
        if (toClose.size() > 0) {
            for (LibraryEntry entry : toClose) {
                librariesEntries.remove(entry.getKey());
            }
        }
        closeLibraries(toClose, cleanRepository);
    }

    /*package*/
    static void cleanLibrariesData(Collection<LibProjectImpl> libs) {
        Map<Integer, Collection<LibProjectImpl>> map = new HashMap<>();
        for (LibProjectImpl lib : libs) {
            int unitID = ((ProjectBase)lib).getUnitId();
            int repoId = Repository.getLayeringSupport(unitID).getStorageID();
            Collection<LibProjectImpl> coll = map.get(repoId);
            if (coll == null) {
                coll = new ArrayList<>();
                map.put(repoId, coll);
            }
            coll.add(lib);
        }
        for (Map.Entry<Integer, Collection<LibProjectImpl>> entry : map.entrySet()) {
            getInstanceByRepositoryId(entry.getKey()).cleanLibrariesDataImpl(entry.getValue());
        }
    }

    private void cleanLibrariesDataImpl(Collection<LibProjectImpl> libs) {
        for (LibProjectImpl entry : libs) {
            librariesEntries.remove(new LibraryKey(entry.getFileSystem(), entry.getPath()));
            entry.dispose(true);
        }
    }

    private void closeLibraries(Collection<LibraryEntry> entries, boolean cleanRepository) {
        ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
        for (LibraryEntry entry : entries) {
            CsmUID<CsmProject> uid = entry.getLibrary();
            ProjectBase lib = (ProjectBase) uid.getObject();
            assert lib != null : "Null project for UID " + uid;
            model.disposeProject(lib, cleanRepository);
        }
    }

    /**
     * Write artificial libraries for project
     */
    /*package-local*/ void writeProjectLibraries(CsmUID<CsmProject> project, RepositoryDataOutput aStream) throws IOException {
        assert aStream != null;
        Set<LibraryKey> keys = new HashSet<>();
        for (Map.Entry<LibraryKey, LibraryEntry> entry : librariesEntries.entrySet()) {
            if (entry.getValue().containsProject(project)) {
                keys.add(entry.getKey());
            }
        }
        aStream.writeInt(keys.size());
        for (LibraryKey libraryKey : keys) {
            libraryKey.write(aStream);
        }
    }

    /**
     * Read artificial libraries for project
     */
    /*package-local*/ void readProjectLibraries(CsmUID<CsmProject> project, RepositoryDataInput input) throws IOException {
        assert input != null;
        int len = input.readInt();
        if (len != AbstractObjectFactory.NULL_POINTER) {
            for (int i = 0; i < len; i++) {
                LibraryKey key =  new LibraryKey(input);
                LibraryEntry entry =  getOrCreateLibrary(key);
                entry.addProject(project); // no need to notiy here, we are called from project constructor here
            }
        }
    }

    private static final class LibraryKey {

        private final FileSystem fileSystem;
        private final CharSequence folder;

        public LibraryKey(FileSystem fileSystem, CharSequence folder) {
            Parameters.notNull("fileSystem", fileSystem);
            this.fileSystem = fileSystem;
            this.folder = folder;
        }

        private LibraryKey(RepositoryDataInput input) throws IOException {
            this.fileSystem = PersistentUtils.readFileSystem(input);
            assert fileSystem != null;
            this.folder = input.readFilePathForFileSystem(fileSystem);
        }

        private void write(RepositoryDataOutput out) throws IOException {
            PersistentUtils.writeFileSystem(fileSystem, out);
            out.writeFilePathForFileSystem(fileSystem, folder);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 17 * hash + Objects.hashCode(this.fileSystem);
            hash = 17 * hash + Objects.hashCode(this.folder);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LibraryKey other = (LibraryKey) obj;
            if (!Objects.equals(this.fileSystem, other.fileSystem)) {
                return false;
            }
            if (!Objects.equals(this.folder, other.folder)) {
                return false;
            }
            return true;
        }

    }

    private final class LibraryEntry {

        private final LibraryKey key;
        private volatile CsmUID<CsmProject> libraryUID;
        private final ConcurrentMap<CsmUID<CsmProject>, Boolean> dependentProjects;

        private LibraryEntry(LibraryKey folder) {
            this.key = folder;
            dependentProjects = new ConcurrentHashMap<>();
        }

        private CharSequence getFolder() {
            return key.folder;
        }

        private FileSystem getFileSystem() {
            return key.fileSystem;
        }

        public LibraryKey getKey() {
            return key;
        }

        private CsmUID<CsmProject> getLibrary() {
            if (libraryUID == null) {
                createUID();
            }
            assert libraryUID != null : "libraryUID is null for folder " + getFolder();
            return libraryUID;
        }

        private synchronized void createUID() {
            if (libraryUID == null) {
                ModelImpl model = (ModelImpl) CsmModelAccessor.getModel();
                LibProjectImpl library = LibProjectImpl.createInstance(model, getFileSystem(), getFolder(), repositoryId);
                libraryUID = library.getUID();
            }
        }

        private boolean isEmpty() {
            return dependentProjects.size() == 0;
        }

        private boolean containsProject(CsmUID<CsmProject> project) {
            return dependentProjects.containsKey(project);
        }

        private Collection<CsmUID<CsmProject>> getDependentProjects() {
            return dependentProjects.keySet();
        }

        private void addProject(CsmUID<CsmProject> project) {
            dependentProjects.put(project, Boolean.TRUE);
        }

        private Boolean removeProject(CsmUID<CsmProject> project) {
            return dependentProjects.remove(project);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("folder=").append(key).append(",\nlibraryUID=").append(libraryUID);// NOI18N
            if (dependentProjects.isEmpty()) {
                sb.append(" NO DEPENDENT PROJECTS!");// NOI18N
            } else {
                sb.append("\ndependentProjects=");// NOI18N
                for (CsmUID<CsmProject> csmUID : dependentProjects.keySet()) {
                    sb.append("\n(").append(System.identityHashCode(csmUID)).append(")").append(csmUID);// NOI18N
                }
            }
            return sb.toString();
        }

    }

    public static void dumpInfo(PrintWriter printOut, boolean withContainers) {
        Collection<LibraryManager> list;
        synchronized (instances) {
            list = instances.values();
        }
        for (LibraryManager manager : list) {
            manager.dumpInfoImpl(printOut, withContainers);
        }
    }

    private void dumpInfoImpl(PrintWriter printOut, boolean withContainers) {
        printOut.printf("LibraryManager [%d]: libs=%d%n", repositoryId, librariesEntries.size());// NOI18N
        int ind = 1;
        for (Map.Entry<LibraryKey, LibraryEntry> entry : librariesEntries.entrySet()) {
            printOut.printf("Lib[%d] %s with LibEntry %s%n", ind++, entry.getKey(), entry.getValue());// NOI18N
            if (withContainers) {
                CsmProject library = UIDCsmConverter.UIDtoProject(entry.getValue().libraryUID);
                if (library == null) {
                    printOut.printf("Library was NOT restored from repository%n");// NOI18N
                } else if (library instanceof ProjectBase) {
                    printOut.printf("[%d] disposing=%s%n", ind, ((ProjectBase)library).isDisposing());// NOI18N
                    ProjectBase.dumpFileContainer(library, printOut);
                } else {
                    printOut.printf("Library's project has unexpected class type %s%n", library.getClass().getName());// NOI18N
                }
            }
        }
        printOut.flush();
    }

}
