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
package org.netbeans.modules.java.mx.project;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.java.mx.project.suitepy.MxDistribution;
import org.netbeans.modules.java.mx.project.suitepy.MxImports;
import org.netbeans.modules.java.mx.project.suitepy.MxLibrary;
import org.netbeans.modules.java.mx.project.suitepy.MxProject;
import org.netbeans.modules.java.mx.project.suitepy.MxSuite;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.AnnotationProcessingQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FlaggedClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation2;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import java.util.stream.Collectors;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.queries.SourceLevelQuery;
import static org.netbeans.spi.java.classpath.FlaggedClassPathImplementation.PROP_FLAGS;
import org.netbeans.spi.java.queries.MultipleRootsUnitTestForSourceQueryImplementation;
import org.netbeans.spi.java.queries.SourceLevelQueryImplementation2;
import org.netbeans.spi.project.SubprojectProvider;

final class SuiteSources implements Sources,
                BinaryForSourceQueryImplementation2<SuiteSources.Group>, SourceForBinaryQueryImplementation2,
                SourceLevelQueryImplementation2, SubprojectProvider, MultipleRootsUnitTestForSourceQueryImplementation {
    private static final Logger LOG = Logger.getLogger(SuiteSources.class.getName());
    private static final SuiteSources CORE;

    static {
        MxSuite coreSuite = CoreSuite.CORE_5_279_0;
        CORE = new SuiteSources(null, null, null, coreSuite);
    }

    private final MxSuite suite;
    private final List<Group> groups;
    private final List<Library> libraries;
    private final List<Dist> distributions;
    private final FileObject dir;
    /**
     * non-null if the dependencies haven't yet been properly initialized
     */
    private Map<String, Dep> transitiveDeps;
    /**
     * avoid GC of imported projects
     */
    private final SuiteProject prj;
    private final Map<String, SuiteSources> imported;
    private final Jdks jdks;

    SuiteSources(SuiteProject owner, Jdks jdks, FileObject dir, MxSuite suite) {
        final Map<String, Dep> fillDeps = new HashMap<>();
        this.prj = owner;
        this.jdks = jdks;
        this.dir = dir;
        this.groups = findGroups(fillDeps, suite, dir);
        this.libraries = findLibraries(fillDeps, suite);
        this.imported = findImportedSuites(dir, suite, fillDeps);
        this.distributions = findDistributions(suite, this.libraries, this.groups, fillDeps);
        this.suite = suite;
        this.transitiveDeps = fillDeps;
    }

    @Override
    public String toString() {
        return "MxSources[" + (dir == null ? "mx" : dir.toURI()) + "]";
    }

    private List<Group> findGroups(Map<String, Dep> fillDeps, MxSuite s, FileObject dir) {
        List<Group> arr = new ArrayList<>();
        for (Map.Entry<String, MxProject> entry : s.projects().entrySet()) {
            String name = entry.getKey();
            MxProject mxPrj = entry.getValue();
            FileObject prjDir = findPrjDir(dir, name, mxPrj);
            if (prjDir == null) {
                fillDeps.put(name, new Group(name, mxPrj, null, null, null, name, name));
                continue;
            }
            String prevName = null;
            Group firstGroup = null;
            String binPrefix = "mxbuild/";
            for (String rel : mxPrj.sourceDirs()) {
                FileObject srcDir = prjDir.getFileObject(rel);
                FileObject binDir = getSubDir(dir, binPrefix + name + "/bin");
                FileObject srcGenDir = getSubDir(dir, binPrefix + name + "/src_gen");
                if (srcDir != null && binDir != null) {
                    String prgName = name + "-" + rel;
                    String displayName;
                    if (prevName == null) {
                        displayName = name;
                    } else {
                        displayName = name + "[" + rel + "]";
                    }
                    Group g = new Group(name, mxPrj, srcDir, srcGenDir, binDir, prgName, displayName);
                    arr.add(g);
                    if (firstGroup == null) {
                        firstGroup = g;
                    }
                    prevName = displayName;
                }
            }
            if (firstGroup != null) {
                fillDeps.put(name, firstGroup);
            }
        }
        return arr;
    }

    private static FileObject getSubDir(FileObject dir, String relPath) {
        FileObject subDir = dir.getFileObject(relPath);
        if (subDir == null) {
            try {
                subDir = FileUtil.createFolder(dir, relPath);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return subDir;
    }

    private List<Library> findLibraries(Map<String, Dep> fillDeps, MxSuite suite) {
        final Map<String, MxLibrary> allLibraries = new HashMap<>();
        registerLibs(allLibraries, null, suite.libraries());

        List<Library> arr = new ArrayList<>();
        for (Map.Entry<String, MxLibrary> entry : allLibraries.entrySet()) {
            final Library library = new Library(entry.getKey(), entry.getValue());
            arr.add(library);
            fillDeps.put(library.getName(), library);
        }
        for (Map.Entry<String, MxLibrary> entry : suite.jdklibraries().entrySet()) {
            final JdkLibrary library = new JdkLibrary(entry.getKey(), entry.getValue());
            arr.add(library);
            fillDeps.put(library.getName(), library);
        }
        return arr;
    }

    private static Map<String, SuiteSources> findImportedSuites(FileObject dir, MxSuite s, Map<String, Dep> fillDeps) {
        if (dir == null) {
            return Collections.emptyMap();
        }
        CORE.registerDeps("mx", fillDeps);
        final MxImports imports = s.imports();
        if (imports != null) {
            Map<String, SuiteSources> imported = new LinkedHashMap<>();
            for (MxImports.Suite imp : imports.suites()) {
                SuiteSources impSources = findSuiteSources(dir, imp);
                final String suiteName = imp.name();
                if (impSources == null) {
                    LOG.log(Level.INFO, "cannot find imported suite: {0}", suiteName);
                    continue;
                }
                imported.put(suiteName, impSources);
                impSources.registerDeps(suiteName, fillDeps);
            }
            return imported;
        }
        return Collections.emptyMap();
    }

    private List<Dist> findDistributions(MxSuite s, List<Library> libraries, List<Group> groups, Map<String, Dep> fillDeps) {
        List<Dist> dists = new ArrayList<>();
        for (Map.Entry<String, MxDistribution> entry : s.distributions().entrySet()) {
            Dist d = new Dist(entry.getKey(), entry.getValue());
            dists.add(d);
            fillDeps.put(d.getName(), d);
        }
        return dists;
    }

    final synchronized void ensureTransitiveDependenciesAreComputed() {
        Map<String, Dep> collectedDeps = this.transitiveDeps;
        if (collectedDeps == null) {
            return;
        }
        this.transitiveDeps = null;
        for (Library l : this.libraries) {
            transitiveDeps(l, collectedDeps);
        }
        for (Group g : this.groups) {
            transitiveDeps(g, collectedDeps);
        }
        for (Dist d : this.distributions) {
            transitiveDeps(d, collectedDeps);
        }
        for (Group g : groups) {
            g.computeClassPath(collectedDeps);
        }
        for (Dist d : this.distributions) {
            d.computeSourceRoots(collectedDeps);
        }
    }

    private static SuiteSources findSuiteSources(FileObject dir, MxImports.Suite imp) throws IllegalArgumentException {
        SuiteSources sources = findSuiteSources(dir.getParent(), imp.name());
        if (sources != null) {
            return sources;
        }
        if (imp.subdir()) {
            for (FileObject subDir : dir.getParent().getChildren()) {
                sources = findSuiteSources(subDir, imp.name());
                if (sources != null) {
                    return sources;
                }
            }
            for (FileObject subDir : dir.getParent().getParent().getChildren()) {
                sources = findSuiteSources(subDir, imp.name());
                if (sources != null) {
                    return sources;
                }
            }
        }
        return null;
    }

    private static SuiteSources findSuiteSources(FileObject root, String name) throws IllegalArgumentException {
        FileObject impDir = root.getFileObject(name);
        if (impDir != null) {
            try {
                Project impPrj = ProjectManager.getDefault().findProject(impDir);
                return impPrj == null ? null : impPrj.getLookup().lookup(SuiteSources.class);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    @Override
    public SourceGroup[] getSourceGroups(String string) {
        return groups();
    }

    Group[] groups() {
        return groups.toArray(new Group[0]);
    }

    Group findGroup(FileObject fo) {
        for (Group g : groups) {
            if (g.contains(fo)) {
                return g;
            }
        }
        return null;
    }

    @Override
    public void addChangeListener(ChangeListener cl) {
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
    }

    private static FileObject findPrjDir(FileObject dir, String prjName, MxProject prj) {
        if (dir == null) {
            return null;
        }
        if (prj.dir() != null) {
            return dir.getFileObject(prj.dir());
        }
        if (prj.subDir() != null) {
            dir = dir.getFileObject(prj.subDir());
            if (dir == null) {
                return null;
            }
        }
        return dir.getFileObject(prjName);
    }

    private Collection<Dep> transitiveDeps(Dep current, Map<String, Dep> fill) {
        current.owner().ensureTransitiveDependenciesAreComputed();
        final Collection<Dep> currentAllDeps = current.allDeps();
        if (currentAllDeps == Collections.<Dep>emptySet()) {
            throw new IllegalStateException("Cyclic dep on " + current.getName());
        } else if (currentAllDeps != null) {
            return currentAllDeps;
        }
        current.setAllDeps(Collections.emptySet());
        TreeSet<Dep> computing = new TreeSet<>();
        computing.add(current);
        for (String depName : current.depNames()) {
            Dep dep = fill.get(depName);
            if (dep == null) {
                int colon = depName.lastIndexOf(':');
                dep = fill.get(depName.substring(colon + 1));
                if (dep == null) {
                    LOG.log(Level.INFO, "dep not found: {0}", depName);
                    continue;
                }
            }
            Collection<Dep> allDeps = transitiveDeps(dep, fill);
            computing.addAll(allDeps);
        }
        current.setAllDeps(computing);
        return computing;
    }

    private static void registerLibs(Map<String, MxLibrary> collect, String prefix, Map<String, MxLibrary> libraries) {
        for (Map.Entry<String, MxLibrary> entry : libraries.entrySet()) {
            String key = entry.getKey();
            MxLibrary lib = entry.getValue();
            if (prefix == null) {
                collect.put(key, lib);
            } else {
                collect.put(prefix + ":" + key, lib);
            }
        }
    }

    private void registerDeps(String prefix, Map<String, Dep> fillDeps) {
        for (Library library : libraries) {
            fillDeps.put(prefix + ":" + library.getName(), library);
        }
        for (Dist d : distributions) {
            fillDeps.put(prefix + ":" + d.getName(), d);
        }
        for (Map.Entry<String, SuiteSources> s : imported.entrySet()) {
            s.getValue().registerDeps(s.getKey(), fillDeps);
        }
    }

    @Override
    public Group findBinaryRoots2(URL url) {
        final FileObject srcFo = URLMapper.findFileObject(url);
        for (Group group : this.groups) {
            if (group.contains(srcFo)) {
                return group;
            }
        }
        return null;
    }

    @Override
    public URL[] computeRoots(Group group) {
        if (group.binDir != null) {
            return new URL[] { group.binDir.toURL() };
        } else {
            return new URL[0];
        }
    }

    @Override
    public boolean computePreferBinaries(Group result) {
        return true;
    }

    @Override
    public void computeChangeListener(Group result, boolean bln, ChangeListener cl) {
    }

    @Override
    public SourceForBinaryQueryImplementation2.Result findSourceRoots2(URL url) {
        this.ensureTransitiveDependenciesAreComputed();
        for (Dist dist : this.distributions) {
            if (dist.isRootJar(url)) {
                List<FileObject> roots = new ArrayList<>();
                for (Group d : dist.getContributingGroups()) {
                    roots.add(d.srcDir);
                    roots.add(d.srcGenDir);
                }
                return new ImmutableResult(roots.toArray(new FileObject[0]));
            }
        }
        for (Group group : this.groups) {
            if (group.binDir != null && group.binDir.toURL().equals(url)) {
                return new ImmutableResult(group.srcDir, group.srcGenDir);
            }
        }
        return null;
    }

    @Override
    public SourceForBinaryQuery.Result findSourceRoots(URL url) {
        return findSourceRoots2(url);
    }

    @Override
    public SourceLevelQueryImplementation2.Result getSourceLevel(FileObject fo) {
        Group g = findGroup(fo);
        if (g == null) {
            return null;
        }
        return new SourceLevelQueryImplementation2.Result2() {
            @Override
            public SourceLevelQuery.Profile getProfile() {
                return SourceLevelQuery.Profile.DEFAULT;
            }

            @Override
            public String getSourceLevel() {
                return g.getCompliance().getSourceLevel();
            }

            @Override
            public void addChangeListener(ChangeListener cl) {
            }

            @Override
            public void removeChangeListener(ChangeListener cl) {
            }
        };
    }

    @Override
    public Set<? extends Project> getSubprojects() {
        Set<Project> prjs = new HashSet<>();
        for (SuiteSources imp : imported.values()) {
            prjs.add(imp.prj);
        }
        return prjs;
    }

    @Override
    public URL[] findUnitTests(FileObject fo) {
        return new URL[0];
    }

    @Override
    public URL[] findSources(FileObject fo) {
        Group g = findGroup(fo);
        return g == null ? new URL[0] : new URL[] { g.getRootFolder().toURL() };
    }

    static interface Dep extends Comparable<Dep> {
        String getName();

        Collection<String> depNames();

        Collection<Dep> allDeps();

        void setAllDeps(Collection<Dep> set);

        @Override
        public default int compareTo(Dep o) {
            return getName().compareTo(o.getName());
        }

        SuiteSources owner();
    }

    abstract class SharedSupport {
        private final PropertyChangeSupport support = new PropertyChangeSupport(this);
        private Boolean exists;

        public final Set<ClassPath.Flag> getFlags() {
            return Boolean.TRUE.equals(exists) ? Collections.emptySet() : Collections.singleton(ClassPath.Flag.INCOMPLETE);
        }

        public void addPropertyChangeListener(PropertyChangeListener pl) {
            support.addPropertyChangeListener(pl);
        }

        public void removePropertyChangeListener(PropertyChangeListener pl) {
            support.removePropertyChangeListener(pl);
        }

        final boolean isInitialized() {
            return exists != null;
        }

        final void updateExists(boolean existsNow) {
            if (exists == null) {
                exists = existsNow;
            } else {
                if (exists != existsNow) {
                    exists = existsNow;
                    support.firePropertyChange(PROP_FLAGS, !exists, (boolean) exists);
                }
            }
        }

    }

    final class Dist extends SharedSupport implements Dep, FlaggedClassPathImplementation {
        final String name;
        final MxDistribution dist;
        Collection<Dep> allDeps;
        private Collection<Group> groups;

        public Dist(String name, MxDistribution dist) {
            this.name = name;
            this.dist = dist;
        }

        @Override
        public Collection<String> depNames() {
            Set<String> deps = new TreeSet<>();
            deps.addAll(dist.distDependencies());
            deps.addAll(dist.exclude());
            return deps;
        }

        @Override
        public Collection<Dep> allDeps() {
            return this.allDeps;
        }

        @Override
        public void setAllDeps(Collection<Dep> set) {
            this.allDeps = set;
        }

        @Override
        public String getName() {
            return this.name;
        }

        private FileObject getJar() {
            if (SuiteSources.this.dir == null) {
                return null;
            }

            FileObject dists = mxBuildDists();
            List<FileObject> candidates = findCandidates(dists);
            if (candidates.isEmpty()) {
                return dists.getFileObject(name.toLowerCase().replace("_", "-") + ".jar", false);
            } else {
                return candidates.get(0);
            }
        }

        private FileObject mxBuildDists() {
            FileObject dists = getSubDir(SuiteSources.this.dir, "mxbuild/dists");
            return dists;
        }

        boolean isRootJar(URL url) {
            try {
                if (url == null) {
                    return false;
                }
                URI uri = url.toURI();
                for (FileObject fo : findCandidates(mxBuildDists())) {
                    if (uri.equals(toJarURL(fo).toURI())) {
                        return true;
                    }
                }
            } catch (MalformedURLException | URISyntaxException ex) {
                // ignore
            }
            return false;
        }

        private List<FileObject> findCandidates(FileObject dists) {
            List<FileObject> candidates = new ArrayList<>();
            List<FileObject> dist = Arrays.stream(dists.getChildren()).
                    filter((fo) -> fo.isFolder() && fo.getName().startsWith("jdk")).
                    collect(Collectors.toList());
            dist.sort((fo1, fo2) -> fo2.getName().compareTo(fo1.getName()));
            for (FileObject jdkDir : dist) {
                FileObject jar = jdkDir.getFileObject(name.toLowerCase().replace("_", "-") + ".jar");
                if (jar != null) {
                    candidates.add(jar);
                }
            }
            return candidates;
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            ensureTransitiveDependenciesAreComputed();
            FileObject jar = getJar();
            updateExists(jar != null && jar.isData());
            if (jar != null) {
                PathResourceImplementation res;
                try {
                    res = ClassPathSupport.createResource(getJarRoot());
                    return Collections.singletonList(res);
                } catch (MalformedURLException | URISyntaxException ex) {
                    // OK
                }
            }
            return Collections.emptyList();
        }

        private URL getJarRoot() throws MalformedURLException, URISyntaxException {
            return toJarURL(getJar());
        }

        private URL toJarURL(FileObject jar) throws MalformedURLException, URISyntaxException {
            if (jar != null) {
                return new URI("jar:" + jar.toURL() + "!/").toURL();
            } else {
                return null;
            }
        }

        @Override
        public SuiteSources owner() {
            return SuiteSources.this;
        }

        private void computeSourceRoots(Map<String, Dep> collectedDeps) {
            if (groups != null) {
                return;
            }
            Set<Group> contributingGroups = new LinkedHashSet<>();
            for (String d : this.dist.dependencies()) {
                Dep dep = collectedDeps.get(d);
                if (dep == null || dep.allDeps() == null) {
                    continue;
                }
                for (Dep d2 : dep.allDeps()) {
                    if (d2 instanceof Group) {
                        contributingGroups.add((Group) d2);
                    }
                }
            }
            for (String d : this.dist.distDependencies()) {
                final Dep anyDep = collectedDeps.get(d);
                if (anyDep instanceof Dist) {
                    Dist dep = (Dist) anyDep;
                    dep.computeSourceRoots(collectedDeps);
                    contributingGroups.removeAll(dep.getContributingGroups());
                }
            }
            groups = contributingGroups;
        }

        public Collection<Group> getContributingGroups() {
            return groups;
        }

        @Override
        public String toString() {
            return "Dist[name=" + name + "]";
        }
    }

    final class Group implements SourceGroup, Dep, AnnotationProcessingQuery.Result,
            Compliance.Provider {
        private final String mxName;
        private final MxProject mxPrj;
        private final FileObject srcDir;
        private final FileObject srcGenDir;
        private final FileObject binDir;
        private final String name;
        private final String displayName;
        private final Compliance compliance;
        private ClassPath sourceCP;
        private ClassPath cp;
        private ClassPath processorPath;
        private Collection<Dep> allDeps;
        private ClassPath bootCP;
        private Object platformOrThis;

        Group(String mxName, MxProject mxPrj, FileObject srcDir, FileObject srcGenDir, FileObject binDir, String name, String displayName) {
            this.mxName = mxName;
            this.mxPrj = mxPrj;
            this.srcDir = srcDir;
            this.srcGenDir = srcGenDir;
            this.binDir = binDir;
            this.name = name;
            this.displayName = displayName;
            this.compliance = Compliance.parse(mxPrj.javaCompliance());
        }

        @Override
        public FileObject getRootFolder() {
            return srcDir;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public Icon getIcon(boolean opened) {
            return null;
        }

        @Override
        public Compliance getCompliance() {
            return compliance;
        }

        @Override
        public boolean contains(FileObject file) {
            if (file == srcDir || file == srcGenDir || FileUtil.isParentOf(srcDir, file) || (srcGenDir != null && FileUtil.isParentOf(srcGenDir, file))) {
                return true;
            }
            return false;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }

        @Override
        public String toString() {
            return "SuiteSources.Group[name=" + name + ",rootFolder=" + srcDir + "]"; // NOI18N
        }

        ClassPath getSourceCP() {
            ensureTransitiveDependenciesAreComputed();
            return sourceCP;
        }

        ClassPath getCP() {
            ensureTransitiveDependenciesAreComputed();
            return cp;
        }

        @Override
        public Collection<String> depNames() {
            List<String> both = new ArrayList<>();
            both.addAll(mxPrj.dependencies());
            both.addAll(mxPrj.generatedDependencies());
            return both;
        }

        @Override
        public void setAllDeps(Collection<Dep> set) {
            allDeps = set;
        }

        @Override
        public Collection<Dep> allDeps() {
            return allDeps;
        }

        private void computeClassPath(Map<String, Dep> transDeps) {
            for (Dep d : transDeps.values()) {
                d.owner().ensureTransitiveDependenciesAreComputed();
            }

            List<Group> arr = new ArrayList<>();
            List<ClassPathImplementation> libs = new ArrayList<>();
            processTransDep(transDeps.get(mxName), arr, libs);
            cp = composeClassPath(arr, libs);
            List<FileObject> roots = new ArrayList<>();
            if (srcDir != null) {
                roots.add(srcDir);
            }
            if (srcGenDir != null) {
                roots.add(srcGenDir);
            }
            sourceCP = ClassPathSupport.createClassPath(roots.toArray(new FileObject[0]));

            if (mxPrj.annotationProcessors().isEmpty()) {
                processorPath = null;
            } else {
                List<Group> groups = new ArrayList<>();
                List<ClassPathImplementation> jars = new ArrayList<>();
                for (String dep : mxPrj.annotationProcessors()) {
                    processTransDep(transDeps.get(dep), groups, jars);
                }
                processorPath = composeClassPath(groups, jars);
            }
        }

        private void processTransDep(Dep dep, List<Group> addGroups, List<ClassPathImplementation> addJars) {
            if (dep != null) {
                dep.owner().ensureTransitiveDependenciesAreComputed();
                for (Dep d : dep.allDeps()) {
                    if (d == this) {
                        continue;
                    }
                    d.owner().ensureTransitiveDependenciesAreComputed();
                    if (d instanceof Group) {
                        addGroups.add((Group) d);
                    } else if (d instanceof ClassPathImplementation) {
                        addJars.add((ClassPathImplementation) d);
                    }
                }
            }
        }

        private ClassPath composeClassPath(List<Group> arr, List<ClassPathImplementation> libs) {
            Set<FileObject> roots = new LinkedHashSet<>();
            final int depsCount = arr.size();
            for (int i = 0; i < depsCount; i++) {
                final Group g = arr.get(i);
                if (g.binDir != null) {
                    roots.add(g.binDir);
                }
            }
            ClassPath prjCp = ClassPathSupport.createClassPath(roots.toArray(new FileObject[0]));
            if (!libs.isEmpty()) {
                if (libs.size() == 1) {
                    prjCp = ClassPathSupport.createProxyClassPath(prjCp,
                                                                  ClassPathFactory.createClassPath(libs.get(0))
                    );
                } else {
                    prjCp = ClassPathSupport.createProxyClassPath(prjCp,
                                                                  ClassPathFactory.createClassPath(
                                                                                  ClassPathSupport.createProxyClassPathImplementation(
                                                                                                  libs.toArray(new ClassPathImplementation[0])
                                                                                  )
                                                                  )
                    );
                }
            }
            return prjCp;
        }

        ClassPath getProcessorCP() {
            ensureTransitiveDependenciesAreComputed();
            return processorPath;
        }

        @Override
        public Set<? extends AnnotationProcessingQuery.Trigger> annotationProcessingEnabled() {
            return EnumSet.of(AnnotationProcessingQuery.Trigger.ON_SCAN, AnnotationProcessingQuery.Trigger.IN_EDITOR);
        }

        @Override
        public Iterable<? extends String> annotationProcessorsToRun() {
            return null;
        }

        @Override
        public URL sourceOutputDirectory() {
            return srcGenDir == null ? null : srcGenDir.toURL();
        }

        @Override
        public Map<? extends String, ? extends String> processorOptions() {
            return Collections.emptyMap();
        }

        @Override
        public void addChangeListener(ChangeListener l) {
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
        }

        @Override
        public SuiteSources owner() {
            return SuiteSources.this;
        }

        ClassPath getBootCP() {
            if (this.bootCP == null) {
                JavaPlatform platform = getJavaPlatform();
                if (platform == null) {
                    platform = JavaPlatform.getDefault();
                }
                List<ClassPath.Entry> entries = platform.getBootstrapLibraries().entries();
                List<URL> roots = new ArrayList<>();
                for (ClassPath.Entry entry : entries) {
                    URL root = entry.getURL();
                    if (root.getPath().contains("/graal-sdk.jar")) {
                        continue;
                    }
                    if (root.getPath().contains("/graaljs-scriptengine.jar")) {
                        continue;
                    }
                    if (root.getPath().contains("/graal-sdk.src.zip")) {
                        continue;
                    }
                    roots.add(entry.getURL());
                }
                this.bootCP = ClassPathSupport.createClassPath(roots.toArray(new URL[0]));
            }
            return this.bootCP;
        }

        final JavaPlatform getJavaPlatform() {
            if (this.platformOrThis == null) {
                JavaPlatform p = jdks.find(compliance);
                if (p == null) {
                    this.platformOrThis = this;
                } else {
                    this.platformOrThis = p;
                }
            }
            return this.platformOrThis instanceof JavaPlatform ? (JavaPlatform) this.platformOrThis : null;
        }
    }

    private class Library extends SharedSupport implements FlaggedClassPathImplementation, Dep {
        final MxLibrary lib;
        final String libName;
        Collection<Dep> allDeps;

        Library(String libName, MxLibrary lib) {
            this.libName = libName;
            this.lib = getOSSLibrary(lib);
        }

        final MxLibrary getOSSLibrary(MxLibrary lib) {
            if (lib.sha1() == null && !lib.os_arch().isEmpty()) {
                Map<String, MxLibrary.Arch> os_dep_libs = lib.os_arch();
                String os = System.getProperty("os.name").toLowerCase();
                for (Map.Entry<String, MxLibrary.Arch> entry : os_dep_libs.entrySet()) {
                    if (os.contains(entry.getKey())) {
                        return entry.getValue().amd64();
                    }
                }
            }
            return lib;
        }

        File getJar(boolean dumpIfMissing) {
            File mxCache;
            String cache = System.getenv("MX_CACHE_DIR");
            if (cache != null) {
                mxCache = new File(cache);
            } else {
                mxCache = new File(new File(new File(System.getProperty("user.home")), ".mx"), "cache");
            }
            int prefix = libName.indexOf(':');
            final String simpleName = libName.substring(prefix + 1);

            File simpleJar = null;
            if (lib.path() != null && !lib.path().isEmpty()) {
                FileObject relativePath = dir.getFileObject(lib.path());
                if (relativePath != null) {
                    simpleJar = FileUtil.toFile(relativePath);
                }
            }
            if (simpleJar == null) {
                simpleJar = new File(mxCache, simpleName + "_" + lib.sha1() + ".jar");
            }
            if (simpleJar.exists()) {
                return simpleJar;
            }
            File dir = new File(mxCache, simpleName + "_" + lib.sha1());
            File jar = new File(dir, simpleName.replace('_', '-').toLowerCase(Locale.ENGLISH) + ".jar");

            if (dumpIfMissing && !jar.exists()) {
                for (File f = jar;; f = f.getParentFile()) {
                    if (!f.exists()) {
                        LOG.log(Level.WARNING, "{0} does not exist", f);
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append(f).append(" exists:\n");
                        String[] kids = f.list();
                        if (kids != null) {
                            for (String n : kids) {
                                sb.append("  ").append(n).append("\n");
                            }
                        }
                        LOG.log(Level.INFO, sb.toString());
                        break;
                    }
                }
            }
            return jar;
        }

        @Override
        public String getName() {
            return libName;
        }

        @Override
        public Collection<String> depNames() {
            return lib.dependencies();
        }

        @Override
        public Collection<Dep> allDeps() {
            return allDeps;
        }

        @Override
        public void setAllDeps(Collection<Dep> set) {
            this.allDeps = set;
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            File jar = getJar(!isInitialized());
            updateExists(jar.exists());
            PathResourceImplementation res;
            try {
                res = ClassPathSupport.createResource(new URI("jar:" + Utilities.toURI(jar).toURL() + "!/").toURL());
                return Collections.singletonList(res);
            } catch (MalformedURLException | URISyntaxException ex) {
                return Collections.emptyList();
            }
        }

        @Override
        public SuiteSources owner() {
            return SuiteSources.this;
        }
    }

    private class JdkLibrary extends Library {
        JdkLibrary(String libName, MxLibrary lib) {
            super(libName, lib);
        }

        @Override
        File getJar(boolean dumpIfMissing) {
            File first = null;
            for (File jdk : jdks.jdks()) {
                File jre = new File(jdk, "jre");
                File jrePath = new File(jre, lib.path().replace('/', File.separatorChar));
                if (jrePath.exists()) {
                    return jrePath;
                }

                if (first == null) {
                    first = jrePath;
                }

                File jdkPath = new File(jdk, lib.path().replace('/', File.separatorChar));

                if (jdkPath.exists()) {
                    return jdkPath;
                }

            }

            if (dumpIfMissing) {
                for (File jdk : jdks.jdks()) {
                    File libPath = new File(jdk, lib.path().replace('/', File.separatorChar));
                    if (!libPath.exists()) {
                        LOG.log(Level.WARNING, "{0} does not exist", libPath);
                    } else {
                        StringBuilder sb = new StringBuilder();
                        sb.append(libPath).append(" exists:\n");
                        String[] kids = libPath.list();
                        if (kids != null) {
                            for (String n : kids) {
                                sb.append("  ").append(n).append("\n");
                            }
                        }
                        LOG.log(Level.INFO, sb.toString());
                        break;
                    }
                }
            }
            return first;
        }

        @Override
        public String getName() {
            return libName;
        }

        @Override
        public Collection<String> depNames() {
            return lib.dependencies();
        }

        @Override
        public Collection<Dep> allDeps() {
            return allDeps;
        }

        @Override
        public void setAllDeps(Collection<Dep> set) {
            this.allDeps = set;
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            File jar = getJar(isInitialized());
            updateExists(jar.exists());
            PathResourceImplementation res;
            try {
                res = ClassPathSupport.createResource(new URI("jar:" + Utilities.toURI(jar).toURL() + "!/").toURL());
                return Collections.singletonList(res);
            } catch (MalformedURLException | URISyntaxException ex) {
                return Collections.emptyList();
            }
        }

        @Override
        public SuiteSources owner() {
            return SuiteSources.this;
        }
    }

    private static class ImmutableResult implements SourceForBinaryQueryImplementation2.Result {
        private final FileObject[] roots;

        ImmutableResult(FileObject... candidates) {
            this(Arrays.asList(candidates));
        }

        ImmutableResult(Iterable<FileObject> all) {
            int cnt = 0;
            Iterator<FileObject> it = all.iterator();
            while (it.hasNext()) {
                if (it.next() != null) {
                    cnt++;
                }
            }
            it = all.iterator();
            roots = new FileObject[cnt];
            for (int at = 0; at < cnt;) {
                FileObject c = it.next();
                if (c != null) {
                    roots[at++] = c;
                }
            }
        }

        @Override
        public boolean preferSources() {
            return true;
        }

        @Override
        public FileObject[] getRoots() {
            return roots;
        }

        @Override
        public void addChangeListener(ChangeListener cl) {
        }

        @Override
        public void removeChangeListener(ChangeListener cl) {
        }
    }
}
