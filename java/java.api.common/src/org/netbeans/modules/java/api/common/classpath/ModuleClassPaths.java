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
package org.netbeans.modules.java.api.common.classpath;

import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.RequiresTree;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.ModuleElement;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndexListener;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.RootsEvent;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TypesEvent;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.java.api.common.util.CommonModuleUtils;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.preprocessorbridge.api.ModuleUtilities;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import static org.netbeans.spi.java.classpath.ClassPathImplementation.PROP_RESOURCES;
import org.netbeans.spi.java.classpath.FlaggedClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
final class ModuleClassPaths {
    private static final Logger LOG = Logger.getLogger(ModuleClassPaths.class.getName());
    /**
     * Changes from ClassIndex are collapsed using runWhenScanFinished and need to be fired asynchronously
     * to make changes done by invalidate visible due to ParserManager.parse nesting.
     */
    private static final RequestProcessor CLASS_INDEX_FIRER = new RequestProcessor(ModuleClassPaths.class);
    private static final String MODULE_INFO_JAVA = "module-info.java";   //NOI18N

    private ModuleClassPaths() {
        throw new IllegalArgumentException("No instance allowed."); //NOI18N
    }

    @NonNull
    static ClassPathImplementation createModuleInfoBasedPath(
            @NonNull final ClassPath base,
            @NonNull final ClassPath sourceRoots,
            @NonNull final ClassPath systemModules,
            @NonNull final ClassPath userModules,
            @NullAllowed final ClassPath legacyClassPath,
            @NullAllowed final Function<URL,Boolean> filter) {
        Parameters.notNull("base", base);                       //NOI18N
        Parameters.notNull("sourceRoots", sourceRoots);         //NOI18N
        Parameters.notNull("systemModules", systemModules);     //NOI18N
        Parameters.notNull("userModules", userModules);         //NOI18N
        if (base != systemModules && base != userModules) {
            throw new IllegalArgumentException("The base must be either systemModules or userModules"); //NOI18N
        }
        return new ModuleInfoClassPathImplementation(
                base,
                sourceRoots,
                systemModules,
                userModules,
                legacyClassPath != null ?
                        legacyClassPath :
                        ClassPath.EMPTY,
                filter);
    }

    @NonNull
    static ClassPathImplementation createPlatformModulePath(
            @NonNull final PropertyEvaluator eval,
            @NonNull final String platformType) {
        return new PlatformModulePath(eval, platformType);
    }

    @NonNull
    static ClassPathImplementation createPropertyBasedModulePath(
            @NonNull final File projectDir,
            @NonNull final PropertyEvaluator eval,
            @NullAllowed final Function<URL,Boolean> filter,
            @NonNull final String... props) {
        return new PropertyModulePath(projectDir, eval, filter, props);
    }

    @NonNull
    static ClassPathImplementation createMultiModuleBinariesPath(
            @NonNull final MultiModule model,
            final boolean archives,
            final boolean requiresModuleInfo) {
        return new MultiModuleBinaries(model, archives, requiresModuleInfo);
    }

    private static final class MultiModuleBinaries extends BaseClassPathImplementation implements PropertyChangeListener, ChangeListener, FileChangeListener {
        private final MultiModule model;
        private final boolean archives;
        private final boolean requiresModuleInfo;
        //@GuardedBy("this")
        private Collection<BinaryForSourceQuery.Result> currentResults;
        //@GuardedBy("this")
        private Collection<ClassPath> currentSourcePaths;
        private final Set</*@GuardedBy("this")*/File> currentModuleInfos;

        MultiModuleBinaries(
                @NonNull final MultiModule model,
                final boolean archives,
                final boolean requiresModuleInfo) {
            Parameters.notNull("model", model); //NOI18N
            this.model = model;
            this.archives = archives;
            this.requiresModuleInfo = requiresModuleInfo;
            this.currentModuleInfos = new HashSet<>();
            this.model.addPropertyChangeListener(WeakListeners.propertyChange(this, this.model));
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            List<PathResourceImplementation> res = getCache();
            if (res != null) {
                return res;
            }
            final List<BinaryForSourceQuery.Result> results = new ArrayList<>();
            final List<ClassPath> sourcePaths = new ArrayList<>();
            final Set<File> moduleInfos = new HashSet<>();
            res = createResources(results, sourcePaths, moduleInfos);
            synchronized (this) {
                assert res != null;
                if (getCache() == null) {
                    setCache(res);
                    if (currentResults != null) {
                        currentResults.forEach((r)->r.removeChangeListener(this));
                    }
                    results.forEach((r)->r.addChangeListener(this));
                    currentResults = results;
                    if (currentSourcePaths != null) {
                        currentSourcePaths.forEach((scp)->scp.removePropertyChangeListener(this));
                    }
                    sourcePaths.forEach((scp)->scp.addPropertyChangeListener(this));
                    currentSourcePaths = sourcePaths;
                    final Set<File> toRemove = new HashSet<>(currentModuleInfos);
                    toRemove.removeAll(moduleInfos);
                    moduleInfos.removeAll(currentModuleInfos);
                    for (File f : toRemove) {
                        FileUtil.removeFileChangeListener(this, f);
                        currentModuleInfos.remove(f);
                    }
                    for (File f : moduleInfos) {
                        FileUtil.addFileChangeListener(this, f);
                        currentModuleInfos.add(f);
                    }
                } else {
                    res = getCache();
                }
            }
            return res;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (MultiModule.PROP_MODULES.equals(propName) || ClassPath.PROP_ENTRIES.equals(propName)) {
                resetCache(PROP_RESOURCES);
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            resetCache(PROP_RESOURCES);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            resetCache(PROP_RESOURCES);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            resetCache(PROP_RESOURCES);
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            resetCache(PROP_RESOURCES);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            resetCache(PROP_RESOURCES);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            //Not important
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            //Not important
        }

        private List<PathResourceImplementation> createResources(
                @NonNull final Collection<? super BinaryForSourceQuery.Result> results,
                @NonNull final Collection<? super ClassPath> sourcePaths,
                @NonNull final Collection<? super File> moduleInfos) {
            final Set<URL> binaries = new LinkedHashSet<>();
            for (String moduleName : model.getModuleNames()) {
                final ClassPath scp = model.getModuleSources(moduleName);
                if (scp != null) {
                    sourcePaths.add(scp);
                    final Consumer<URL> consummer = !requiresModuleInfo || scp.findResource(MODULE_INFO_JAVA) != null ?
                            (u) -> {
                                final BinaryForSourceQuery.Result r = BinaryForSourceQuery.findBinaryRoots(u);
                                results.add(r);
                                binaries.addAll(filterArtefact(archives, r.getRoots()));
                            }:
                            (u) -> {};
                        for (ClassPath.Entry e : scp.entries()) {
                            try {
                                final URL url = e.getURL();
                                consummer.accept(url);
                                Optional.ofNullable(requiresModuleInfo ? BaseUtilities.toFile(url.toURI()) : null)
                                        .map ((root) -> new File(root, MODULE_INFO_JAVA))
                                        .ifPresent(moduleInfos::add);
                            } catch (URISyntaxException use) {
                                LOG.log(
                                        Level.WARNING,
                                        "Cannot convert to URI: {0}",   //NOI18N
                                        e.getURL());
                            }
                        }

                }
            }
            return binaries.stream()
                    .map((url) -> org.netbeans.spi.java.classpath.support.ClassPathSupport.createResource(url))
                    .collect(Collectors.toList());
        }

        

        private static Collection<? extends URL> filterArtefact(
                final boolean archive,
                @NonNull final URL... urls) {
            final Collection<URL> res = new ArrayList<>(urls.length);
            for (URL url : urls) {
                if ((archive && FileUtil.isArchiveArtifact(url)) ||
                    (!archive && !FileUtil.isArchiveArtifact(url))) {
                    res.add(url);
                    break;
                }
            }
            if (res.isEmpty()) {
                Collections.addAll(res, urls);
            }
            return res;
        }
    }

    private static final class PlatformModulePath extends BaseClassPathImplementation implements PropertyChangeListener {
        private static final String PLATFORM_ANT_NAME = "platform.ant.name";    //NOI18N

        private final PropertyEvaluator eval;
        private final String platformType;

        PlatformModulePath(
                @NonNull final PropertyEvaluator eval,
                @NonNull final String platformType) {
            Parameters.notNull("evel", eval);   //NOI18N
            Parameters.notNull("platformType", platformType);   //NOI18N
            this.eval = eval;
            this.platformType = platformType;
            this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
            final JavaPlatformManager jpm = JavaPlatformManager.getDefault();
            jpm.addPropertyChangeListener(WeakListeners.propertyChange(this, jpm));
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            List<PathResourceImplementation> res = getCache();
            if (res != null) {
                return res;
            }
            res = createResources();
            synchronized (this) {
                assert res != null;
                if (getCache() == null) {
                    setCache(res);
                } else {
                    res = getCache();
                }
            }
            return res;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (propName == null ||
                ProjectProperties.PLATFORM_ACTIVE.equals(propName) ||
                (JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(propName) && isActivePlatformChange())) {
                resetCache(PROP_RESOURCES);
            }
        }

        private boolean isActivePlatformChange() {
            List<PathResourceImplementation> current = getCache();
            if (current == null) {
                return false;
            }
            final Stream<JavaPlatform> platforms = getPlatforms();
            return platforms.findFirst().isPresent() ?
                current.isEmpty() :
                !current.isEmpty();
        }

        private List<PathResourceImplementation> createResources() {
            final List<PathResourceImplementation> res = new ArrayList<>();
            getPlatforms()
                .flatMap((plat)->plat.getBootstrapLibraries().entries().stream())
                .map((entry) -> entry.getURL())
                .forEach((root)->{res.add(org.netbeans.spi.java.classpath.support.ClassPathSupport.createResource(root));});
            return res;
        }

        @NonNull
        private Stream<JavaPlatform> getPlatforms() {
            final String platformName = eval.getProperty(ProjectProperties.PLATFORM_ACTIVE);
            return platformName != null && !platformName.isEmpty() ?
                    Arrays.stream(JavaPlatformManager.getDefault().getInstalledPlatforms())
                        .filter((plat)->
                            platformName.equals(plat.getProperties().get(PLATFORM_ANT_NAME)) &&
                            platformType.equals(plat.getSpecification().getName())) :
                    Stream.empty();
        }
    }

    private static final class PropertyModulePath extends BaseClassPathImplementation implements PropertyChangeListener, FileChangeListener {
        private static final String MODULE_INFO_CLASS = "module-info.class";    //NOI18N

        private final File projectDir;
        private final PropertyEvaluator eval;
        private final Function<URL,Boolean> filter;
        private final Set<String> props;
        //@GuardedBy("this")
        private final Set<File> listensOn;

        PropertyModulePath(
            @NonNull final File projectDir,
            @NonNull final PropertyEvaluator eval,
            @NullAllowed final Function<URL,Boolean> filter,
            @NonNull final String... props) {
            Parameters.notNull("projectDir", projectDir);   //NOI18N
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("props", props); //NOI18N
            this.projectDir = projectDir;
            this.eval = eval;
            this.filter = filter == null ?
                    (url) -> true :
                    filter;
            this.props = new LinkedHashSet<>();
            this.listensOn = new HashSet<>();
            Collections.addAll(this.props, props);
            this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
        }

        @Override
        @NonNull
        public List<? extends PathResourceImplementation> getResources() {
            List<PathResourceImplementation> res = getCache();
            if (res == null) {
                final List<PathResourceImplementation> collector = res = new ArrayList<>();
                final Collection<File> modulePathRoots = new HashSet<>();
                props.stream()
                    .map((prop)->eval.getProperty(prop))
                    .flatMap((path)-> {
                        return path == null ?
                            Collections.<String>emptyList().stream() :
                            Arrays.stream(PropertyUtils.tokenizePath(path));
                    })
                    .map((part)->PropertyUtils.resolveFile(projectDir, part))
                    .flatMap((modulePathEntry) -> {
                        if (isArchiveFile(modulePathEntry) || hasModuleInfo(modulePathEntry)) {
                            return Stream.of(modulePathEntry);
                        } else {
                            modulePathRoots.add(modulePathEntry);
                            return findModules(modulePathEntry);
                        }
                    })
                    .forEach((file)->{
                        URL url = FileUtil.urlForArchiveOrDir(file);
                        if (url != null && filter.apply(url) != Boolean.FALSE) {
                            collector.add(org.netbeans.spi.java.classpath.support.ClassPathSupport.createResource(url));
                        }
                    });
                synchronized (this) {
                    List<PathResourceImplementation> cv = getCache();
                    if (cv == null) {
                        final Set<File> toAdd = new HashSet<>(modulePathRoots);
                        final Set<File> toRemove = new HashSet<>(listensOn);
                        toAdd.removeAll(listensOn);
                        toRemove.removeAll(modulePathRoots);
                        for (File f : toRemove) {
                            FileUtil.removeFileChangeListener(this, f);
                            listensOn.remove(f);
                        }
                        for (File f : toAdd) {
                            FileUtil.addFileChangeListener(this, f);
                            listensOn.add(f);
                        }
                        LOG.log(
                            Level.FINE,
                            "{0} setting results: {1}, listening on: {2}",    //NOI18N
                            new Object[]{
                                getClass().getSimpleName(),
                                res,
                                listensOn
                            });
                        setCache(res);
                    } else {
                        res = cv;
                    }
                }
            }
            return res;
        }

        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (propName == null || props.contains(propName)) {
                LOG.log(
                    Level.FINER,
                    "{0} propertyChange: {1}",    //NOI18N
                    new Object[]{
                        getClass().getSimpleName(),
                        propName
                    });
                resetCache(PROP_RESOURCES);
            }
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            handleFileEvent(fe);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            handleFileEvent(fe);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            handleFileEvent(fe);
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            handleFileEvent(fe);
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        @Override
        public void fileChanged(FileEvent fe) {
        }

        private void handleFileEvent(@NonNull final FileEvent fe) {
            LOG.log(
                Level.FINER,
                "{0} file event: {1}",    //NOI18N
                new Object[]{
                    getClass().getSimpleName(),
                    fe.getFile()
                });
            resetCache(PROP_RESOURCES);
        }

        private static boolean isArchiveFile(@NonNull final File file) {
            try {
                return FileUtil.isArchiveFile(BaseUtilities.toURI(file).toURL());
            } catch (MalformedURLException mue) {
                LOG.log(
                        Level.WARNING,
                        "Invalid URL for: {0}",
                        file);
                return false;
            }
        }

        private static boolean hasModuleInfo(@NonNull final File file) {
            //Cannot check just presence of module-info.class, the file can be build/classes of
            //an uncompiled project.
            return SourceUtils.getModuleName(FileUtil.urlForArchiveOrDir(file), true) != null;
        }

        @NonNull
        private static Stream<File> findModules(@NonNull final File modulesFolder) {
            //No project's dist folder do File.list
            File[] modules = modulesFolder.listFiles((File f) -> {
                try {
                    if (f.getName().startsWith(".")) {
                        return false;
                    }
                    return f.isFile() ?
                            FileUtil.isArchiveFile(BaseUtilities.toURI(f).toURL()) :
                            new File(f, MODULE_INFO_CLASS).exists();
                } catch (MalformedURLException e) {
                    Exceptions.printStackTrace(e);
                    return false;
                }
            });
            return modules == null ?
               Stream.empty():
               Arrays.stream(modules);
        }
    }

    private static final class ModuleInfoClassPathImplementation  extends BaseClassPathImplementation implements FlaggedClassPathImplementation, PropertyChangeListener, ChangeListener, FileChangeListener, ClassIndexListener {

        private static final String MOD_JAVA_BASE = "java.base";    //NOI18N
        private static final String MOD_JAVA_SE = "java.se";        //NOI18N
        private static final String MOD_ALL_UNNAMED = "ALL-UNNAMED";    //NOI18N
        private static final String JAVA_ = "java.";            //NOI18N
        private static final List<PathResourceImplementation> TOMBSTONE = Collections.unmodifiableList(new ArrayList<>());
        private static final Predicate<ModuleElement> NON_JAVA_PUBEXP = (e) -> 
                !e.getQualifiedName().toString().startsWith(JAVA_) &&
                e.getDirectives().stream()
                    .filter((d) -> d.getKind() == ModuleElement.DirectiveKind.EXPORTS)
                    .anyMatch((d) -> ((ModuleElement.ExportsDirective)d).getTargetModules() == null);
        private final ClassPath base;
        private final ClassPath sources;
        private final ClassPath systemModules;
        private final ClassPath userModules;
        private final ClassPath legacyClassPath;
        private final Function<URL,Boolean> filter;
        private final ThreadLocal<Object[]> selfRes;
        private final AtomicReference<CompilerOptionsQuery.Result> compilerOptions;

        //@GuardedBy("this")
        private ClasspathInfo activeProjectSourceRoots;
        //@GuardedBy("this")
        private volatile boolean rootsChanging;
        //@GuardedBy("this")
        private Collection<File> moduleInfos;
        private volatile boolean incomplete;

        ModuleInfoClassPathImplementation(
                @NonNull final ClassPath base,
                @NonNull final ClassPath sources,
                @NonNull final ClassPath systemModules,
                @NonNull final ClassPath userModules,
                @NonNull final ClassPath legacyClassPath,
                @NullAllowed final Function<URL,Boolean> filter) {
            super(null);
            Parameters.notNull("base", base);       //NOI18N
            Parameters.notNull("sources", sources); //NOI18N
            Parameters.notNull("systemModules", systemModules); //NOI18N
            Parameters.notNull("userModules", userModules); //NOI18N
            Parameters.notNull("legacyClassPath", legacyClassPath); //NOI18N
            this.base = base;
            this.sources = sources;
            this.systemModules = systemModules;
            this.userModules = userModules;
            this.legacyClassPath = legacyClassPath;
            this.filter = filter == null ?
                    (url) -> null :
                    filter;
            this.selfRes = new ThreadLocal<>();
            this.compilerOptions = new AtomicReference<>();
            this.moduleInfos = Collections.emptyList();
            this.sources.addPropertyChangeListener(WeakListeners.propertyChange(this, this.sources));
            this.systemModules.addPropertyChangeListener(WeakListeners.propertyChange(this, this.systemModules));
            this.userModules.addPropertyChangeListener(WeakListeners.propertyChange(this, this.base));
            this.legacyClassPath.addPropertyChangeListener(WeakListeners.propertyChange(this, this.legacyClassPath));
        }

        @Override
        public Set<ClassPath.Flag> getFlags() {
            getResources(); //Compute incomplete status
            return incomplete ?
                    EnumSet.of(ClassPath.Flag.INCOMPLETE) :
                    Collections.emptySet();
        }

        @Override
        @NonNull
        public List<? extends PathResourceImplementation> getResources() {
            List<PathResourceImplementation> res = getCache();
            boolean needToFire = false;
            if (res == TOMBSTONE) {
                needToFire = true;
                res = null;
            }
            if (res != null) {
                return res;
            }
            final Object[] bestSoFar = selfRes.get();
            if (bestSoFar != null) {
                bestSoFar[1] = Boolean.TRUE;
                return (List<? extends PathResourceImplementation>) bestSoFar[0];
            }
            final Collection<File> newModuleInfos = new LinkedHashSet<>();
            final List<URL> newActiveProjectSourceRoots = new ArrayList<>();
            collectProjectSourceRoots(systemModules, newActiveProjectSourceRoots);
            collectProjectSourceRoots(userModules, newActiveProjectSourceRoots);
            newActiveProjectSourceRoots.addAll(sources.entries().stream()
                .map((e) -> e.getURL())
                .collect(Collectors.toList()));
            ProjectManager.mutex().readAccess(() -> {
                synchronized (this) {
                    if (activeProjectSourceRoots != null) {
                        activeProjectSourceRoots.getClassIndex().removeClassIndexListener(this);
                        activeProjectSourceRoots = null;
                    }
                    if (!newActiveProjectSourceRoots.isEmpty()) {
                        activeProjectSourceRoots = ClasspathInfo.create(
                                ClassPath.EMPTY,
                                ClassPath.EMPTY,
                                org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(newActiveProjectSourceRoots.toArray(new URL[0])));
                        activeProjectSourceRoots.getClassIndex().addClassIndexListener(this);
                        LOG.log(
                            Level.FINER,
                            "{0} for {1} listening on: {2}",    //NOI18N
                            new Object[]{
                                getClass().getSimpleName(),
                                base,
                                newActiveProjectSourceRoots
                            });
                    }
                }
            });
            boolean incompleteVote = false;
            if(supportsModules(systemModules, userModules, sources)) {
                final Map<String, List<URL>> modulesPatches = getPatches();
                String xmodule = getXModule();
                String patchedModule = null;
                final Function<URL,Stream<URL>> patchRootTransformer;
                {
                    final Map<URL,List<URL>> sourcePatches = new HashMap<>();
                    final Map<URL,String> mpBmn = new HashMap<>();
                    modulesPatches.entrySet()
                        .forEach((e) -> e.getValue().forEach((url) -> mpBmn.put(url, e.getKey())));
                    for (ClassPath.Entry e : sources.entries()) {
                        final URL url = e.getURL();
                        final String mn = mpBmn.get(url);
                        if (mn != null) {
                            if (patchedModule == null) {
                                patchedModule = mn;
                            }
                            sourcePatches.put(url, Collections.emptyList());
                        }
                    }
                    for (URL pRoot : mpBmn.keySet()) {
                        final URL[] bin;
                        if (!sourcePatches.containsKey(pRoot) && (bin = BinaryForSourceQuery.findBinaryRoots(pRoot).getRoots()).length > 0) {
                            sourcePatches.put(pRoot, Arrays.asList(bin));
                        }
                    }
                    patchRootTransformer = (u) -> {
                        final List<URL> transformation = sourcePatches.get(u);
                        return transformation == null ?
                                Stream.of(u) :
                                transformation.stream();
                    };
                }
                if (xmodule == null) {
                    xmodule = patchedModule;
                }
                final Map<String,List<URL>> modulesByName = getModulesByName(
                        base,
                        modulesPatches,
                        patchRootTransformer);
                res = modulesByName.values().stream()
                        .flatMap((urls) -> urls.stream())
                        .map((url)->org.netbeans.spi.java.classpath.support.ClassPathSupport.createResource(url))
                        .collect(Collectors.toList());
                final List<PathResourceImplementation> selfResResources;
                if (base == systemModules) {
                    selfResResources = findJavaBase(modulesByName);
                } else {
                    selfResResources = Collections.emptyList();
                }
                LOG.log(
                    Level.FINER,
                    "{0} for {1} self resources: {2}",    //NOI18N
                    new Object[]{
                        ModuleInfoClassPathImplementation.class.getSimpleName(),
                        base,
                        selfResResources
                    });
                LOG.log(
                    Level.FINEST,
                    "{0} for {1} systemModules: {2}, userModules: {4}",    //NOI18N
                    new Object[]{
                        ModuleInfoClassPathImplementation.class.getSimpleName(),
                        base,
                        systemModules,
                        userModules
                    });
                selfRes.set(new Object[]{
                    selfResResources,
                    needToFire});
                try {
                    FileObject found = null;
                    for (ClassPath.Entry cpe : sources.entries()) {
                        final URL root = cpe.getURL();
                        try {
                            final File moduleInfo = FileUtil.normalizeFile(new File(BaseUtilities.toFile(root.toURI()),MODULE_INFO_JAVA));
                            newModuleInfos.add(moduleInfo);
                            if (found == null) {
                                found = FileUtil.toFileObject(moduleInfo);
                            }
                        } catch (URISyntaxException e) {
                            LOG.log(
                                Level.WARNING,
                                "Invalid URL: {0}, reason: {1}",    //NOI18N
                                new Object[]{
                                    root,
                                    e.getMessage()
                                });
                        }
                    }
                    final List<PathResourceImplementation> bcprs = base == systemModules ?
                            selfResResources :   //java.base
                            findJavaBase(getModulesByName(systemModules, modulesPatches, patchRootTransformer));
                    final ClassPath bootCp = org.netbeans.spi.java.classpath.support.ClassPathSupport.createClassPath(bcprs);
                    final JavaSource src;
                    final Predicate<ModuleElement> rootModulesPredicate;
                    if (found != null) {
                        src = JavaSource.create(
                                new ClasspathInfo.Builder(bootCp)
                                        .setModuleBootPath(systemModules)
                                        .setModuleCompilePath(userModules)
                                        .setSourcePath(sources)
                                        .build(),
                                found);
                        final Set<String> additionalModules = getAddMods();
                        additionalModules.remove(MOD_ALL_UNNAMED);
                        rootModulesPredicate = ModuleNames.create(additionalModules);
                    } else {
                        src = JavaSource.create(
                                new ClasspathInfo.Builder(bootCp)
                                        .setModuleBootPath(systemModules)
                                        .setModuleCompilePath(userModules)
                                        .setSourcePath(sources)
                                        .build());
                        final Set<String> additionalModules = getAddMods();
                        additionalModules.remove(MOD_ALL_UNNAMED);
                        if (base == systemModules) {
                            additionalModules.add(MOD_JAVA_SE);
                            rootModulesPredicate = ModuleNames.create(additionalModules)
                                    .or(NON_JAVA_PUBEXP);
                        } else {
                            rootModulesPredicate = ModuleNames.create(additionalModules);
                        }
                    }
                    boolean dependsOnUnnamed = false;
                    if (src != null) {
                        try {
                            final ModuleUtilities mu = ModuleUtilities.get(src);
                            if (mu != null) {
                                final Set<URL> requires = new HashSet<>();
                                if (found != null || xmodule != null) {
                                    final ModuleElement myModule;
                                    final ModuleTree myModuleTree;
                                    if (found != null) {
                                        myModuleTree = mu.parseModule();
                                        myModule = myModuleTree != null ?
                                                mu.resolveModule(myModuleTree) :
                                                null;
                                        if (xmodule != null &&
                                                LOG.isLoggable(Level.WARNING) &&
                                                !xmodule.equals(Optional.ofNullable(myModuleTree)
                                                    .map((mt) -> mt.getName().toString())
                                                    .orElse(xmodule))) {
                                            LOG.log(
                                                    Level.WARNING,
                                                    "Xmodule: {0} combined with module-info: {1}, ignoring xmodule.",   //NOI18N
                                                    new Object[]{
                                                        xmodule,
                                                        FileUtil.getFileDisplayName(found)
                                                    });
                                        }
                                    } else {
                                        final List<URL> xmoduleLocs = modulesByName.get(xmodule);
                                        myModuleTree = null;
                                        myModule = mu.resolveModule(xmodule);
                                        if (xmoduleLocs != null) {
                                            requires.addAll(xmoduleLocs);
                                        }
                                        incompleteVote = myModule == null;
                                    }
                                    if (myModule != null) {
                                        dependsOnUnnamed = dependsOnUnnamed(myModule, true);
                                        requires.addAll(collectRequiredModules(myModule, myModuleTree, true, false, modulesByName));
                                        requires.addAll(modulesByName.getOrDefault(myModule.getQualifiedName().toString(), Collections.emptyList()));
                                    } else if (base == systemModules) {
                                        //When module unresolvable add at least java.base to systemModules
                                        Optional.ofNullable(modulesByName.get(MOD_JAVA_BASE))
                                                .ifPresent(requires::addAll);
                                    }
                                } else {
                                    //Unnamed module
                                    dependsOnUnnamed = true;
                                    for (String moduleName : modulesByName.keySet()) {
                                        Optional.ofNullable(mu.resolveModule(moduleName))
                                                .filter(rootModulesPredicate)
                                                .map((m) -> collectRequiredModules(m, null, true, true, modulesByName))
                                                .ifPresent(requires::addAll);
                                    }
                                }
                                res = filterModules(res, requires, filter);
                            }
                        } catch (IOException ioe) {
                            Exceptions.printStackTrace(ioe);
                        }
                    }
                    if (dependsOnUnnamed) {
                        //Unnamed module - add legacy classpath to classpath.
                        final List<ClassPath.Entry> legacyEntires = legacyClassPath.entries();
                        final List<PathResourceImplementation> tmp = new ArrayList<>(res.size() + legacyEntires.size());
                        legacyEntires.stream()
                                .map((e)->org.netbeans.spi.java.classpath.support.ClassPathSupport.createResource(e.getURL()))
                                .forEach(tmp::add);
                        tmp.addAll(res);
                        res = tmp;
                    }
                } finally {
                    needToFire = selfRes.get()[1] == Boolean.TRUE;
                    selfRes.remove();
                }
            } else {
                res = Collections.emptyList();
            }
            synchronized (this) {
                assert res != null;
                List<PathResourceImplementation> ccv = getCache();
                if (ccv == null || ccv == TOMBSTONE) {
                    LOG.log(
                        Level.FINE,
                        "{0} for {1} setting results: {2}, listening on: {3}",    //NOI18N
                        new Object[]{
                            getClass().getSimpleName(),
                            base,
                            res,
                            newModuleInfos
                        });
                    setCache(res);
                    final Collection<File> added = new LinkedHashSet<>(newModuleInfos);
                    added.removeAll(moduleInfos);
                    final Collection<File> removed = new LinkedHashSet<>(moduleInfos);
                    removed.removeAll(newModuleInfos);
                    removed.stream().forEach((f) -> FileUtil.removeFileChangeListener(this, f));
                    added.stream().forEach((f) -> FileUtil.addFileChangeListener(this, f));
                    moduleInfos = newModuleInfos;
                    incomplete = incompleteVote;
                } else {
                    res = ccv;
                }
            }
            if (needToFire) {
                fire(PROP_RESOURCES);
            }
            return res;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (propName == null || ClassPath.PROP_ENTRIES.equals(propName)) {
                resetOutsideWriteAccess(null, PROP_RESOURCES);
            }
        }

        @Override
        public void stateChanged(@NonNull final ChangeEvent evt) {
            resetOutsideWriteAccess(null, PROP_FLAGS, PROP_RESOURCES);
        }

        @Override
        public void fileDataCreated(FileEvent fe) {
            resetOutsideWriteAccess(fe.getFile(), PROP_RESOURCES);
        }

        @Override
        public void fileChanged(FileEvent fe) {
            resetOutsideWriteAccess(fe.getFile(), PROP_RESOURCES);
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            final ClasspathInfo info = ClasspathInfo.create(
                ClassPath.EMPTY,
                ClassPath.EMPTY,
                sources);
            final Set<ElementHandle<ModuleElement>> mods = info.getClassIndex().getDeclaredModules(
                    "", //NOI18N
                    ClassIndex.NameKind.PREFIX,
                    EnumSet.of(ClassIndex.SearchScope.SOURCE));
            if (mods.isEmpty()) {
                resetOutsideWriteAccess(null, PROP_RESOURCES);
            }
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            resetOutsideWriteAccess(fe.getFile(), PROP_RESOURCES);
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
        }

        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
        }

        @Override
        public void rootsAdded(final RootsEvent event) {
        }

        @Override
        public void rootsRemoved(RootsEvent event) {
        }

        @Override
        public void typesAdded(TypesEvent event) {
            handleModuleChange(event);
        }

        @Override
        public void typesRemoved(TypesEvent event) {
            handleModuleChange(event);
        }

        @Override
        public void typesChanged(TypesEvent event) {
            handleModuleChange(event);
        }
        
        private void resetOutsideWriteAccess(FileObject artifact, String... propNames) {
            final boolean hasDocExclusiveLock = Optional.ofNullable(artifact)
                    .map((fo) -> {
                        try {
                            return DataObject.find(fo).getLookup().lookup(EditorCookie.class);
                        } catch (DataObjectNotFoundException e) {
                            return null;
                        }
                    })
                    .map((ec) -> ec.getDocument())
                    .map(DocumentUtilities::isWriteLocked)
                    .orElse(Boolean.FALSE);
            final Runnable action = () -> resetCache(TOMBSTONE, propNames);
            if (hasDocExclusiveLock) {
                if (LOG.isLoggable(Level.WARNING)) {
                    LOG.log(
                            Level.WARNING,
                            "Firing under editor write lock: {0}",   //NOI18N
                            Arrays.toString(Thread.currentThread().getStackTrace()));
                }
                CLASS_INDEX_FIRER.execute(action);
            } else if (ProjectManager.mutex().isWriteAccess()) {
                ProjectManager.mutex().postReadRequest(action);
            } else {
                action.run();
            }
        }

        private void handleModuleChange(@NonNull final TypesEvent event) {
            if (event.getModule() != null) {
                ClasspathInfo info;
                synchronized (this) {
                    info = activeProjectSourceRoots;
                    if (info != null) {
                        if (rootsChanging) {
                            info = null;
                        } else {
                            rootsChanging = true;
                        }
                    }
                }
                if (info != null) {
                    try {
                        JavaSource.create(info).runWhenScanFinished((cc)->{
                            LOG.log(
                                Level.FINER,
                                "{0} for {1} got class index event due to change of module {2} in {3}",    //NOI18N
                                new Object[]{
                                    ModuleInfoClassPathImplementation.class.getSimpleName(),
                                    base,
                                    event.getModule().getQualifiedName(),
                                    event.getRoot()
                                });
                            rootsChanging = false;
                            CLASS_INDEX_FIRER.execute(()->resetCache(TOMBSTONE, PROP_FLAGS, PROP_RESOURCES));
                        },
                        true);
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            }
        }
        
        @CheckForNull
        private CompilerOptionsQuery.Result getCompilerOptions() {
            CompilerOptionsQuery.Result res = compilerOptions.get();
            if (res == null) {
                final FileObject[] roots = sources.getRoots();
                res = roots.length == 0 ?
                        null :
                        CompilerOptionsQuery.getOptions(roots[0]);
                if (res != null) {
                    if (compilerOptions.compareAndSet(null, res)) {
                        res.addChangeListener(WeakListeners.change(this, res));
                    } else {
                        res = compilerOptions.get();
                        assert res != null;
                    }
                }
            }
            return res;
        }
        
        private Set<String> getAddMods() {
            final CompilerOptionsQuery.Result res = getCompilerOptions();
            return res == null ?
                    new HashSet<>() :
                    CommonModuleUtils.getAddModules(res);
        }

        @CheckForNull
        private String getXModule() {
            final CompilerOptionsQuery.Result res = getCompilerOptions();
            return res == null ?
                    null :
                    CommonModuleUtils.getXModule(res);
        }

        @NonNull
        private Map<String,List<URL>> getPatches() {
            final CompilerOptionsQuery.Result res = getCompilerOptions();
            return res == null ?
                    Collections.emptyMap() :
                    CommonModuleUtils.getPatches(res);
        }

        @NonNull
        private static Map<String,List<URL>> getModulesByName(
                @NonNull final ClassPath cp,
                @NonNull final Map<String,List<URL>> patches,
                @NonNull final Function<URL,Stream<URL>> patchRootTrannsformer) {
            final Map<String,List<URL>> res = new LinkedHashMap<>();
            cp.entries().stream()
                    .map((entry)->entry.getURL())
                    .forEach((url)-> {
                        final String moduleName = SourceUtils.getModuleName(url, true);
                        if (moduleName != null) {
                            final List<URL> roots = new ArrayList<>();
                            final List<URL> patchRoots = patches.get(moduleName);
                            if (patchRoots != null) {
                                patchRoots.stream()
                                        .flatMap(patchRootTrannsformer)
                                        .forEach(roots::add);
                            }
                            roots.add(url);
                            res.put(moduleName, roots);
                        }
                    });
            return res;
        }

        private static void collectProjectSourceRoots(
                @NonNull final ClassPath cp,
                @NonNull final Collection<? super URL> projectSourceRoots) {
            cp.entries().stream()
                    .map((e) -> e.getURL())
                    .forEach((url) -> {
                        final SourceForBinaryQuery.Result2 sfbqRes = SourceForBinaryQuery.findSourceRoots2(url);
                        if (sfbqRes.preferSources()) {
                            Arrays.stream(sfbqRes.getRoots())
                                    .map((fo)->fo.toURL())
                                    .forEach(projectSourceRoots::add);
                        }
                    });
        }

        @NonNull
        private static List<PathResourceImplementation> findJavaBase(final Map<String,List<URL>> modulesByName) {
            return Optional.ofNullable(modulesByName.get(MOD_JAVA_BASE))
                .map((urls) -> {
                    return urls.stream()
                            .map(org.netbeans.spi.java.classpath.support.ClassPathSupport::createResource)
                            .collect(Collectors.toList());                    
                })
                .orElseGet(Collections::emptyList);
        }
        
        private static boolean dependsOnUnnamed(
                @NonNull final ModuleElement module,
                final boolean transitive) {
            return dependsOnUnnamed(module, transitive, true, new HashSet<>());
        }
        
        private static boolean dependsOnUnnamed(
                @NonNull final ModuleElement module,
                final boolean transitive,
                final boolean topLevel,
                final Set<ModuleElement> seen) {
            if (module.isUnnamed()) {
                return true;
            }
            if (seen.add(module)) {
                for (ModuleElement.Directive d : module.getDirectives()) {
                    if (d.getKind() == ModuleElement.DirectiveKind.REQUIRES) {
                        final ModuleElement.RequiresDirective rd = (ModuleElement.RequiresDirective) d;
                        if (topLevel || (transitive && rd.isTransitive())) {
                            if (dependsOnUnnamed(rd.getDependency(), transitive, false, seen)) {
                                return true;
                            }
                        }
                    }
                }
            }
            return false;
        }

        @NonNull
        private static Set<URL> collectRequiredModules(
                @NonNull final ModuleElement module,
                @NullAllowed final ModuleTree moduleTree,
                final boolean transitive,
                final boolean includeTopLevel,
                @NonNull final Map<String,List<URL>> modulesByName) {
            final Set<URL> res = new HashSet<>();
            final Set<ModuleElement> seen = new HashSet<>();
            if (includeTopLevel) {
                final List<URL> moduleLocs = modulesByName.get(module.getQualifiedName().toString());
                if (moduleLocs != null) {
                    res.addAll(moduleLocs);
                }
            }
            collectRequiredModulesImpl(module, moduleTree, transitive, !includeTopLevel, modulesByName, seen, res);
            return res;
        }

        private static boolean collectRequiredModulesImpl(
                @NullAllowed final ModuleElement module,
                @NullAllowed final ModuleTree moduleTree,
                final boolean transitive,
                final boolean topLevel,
                @NonNull final Map<String,List<URL>> modulesByName,
                @NonNull final Collection<? super ModuleElement> seen,
                @NonNull final Collection<? super URL> c) {
            if (module != null && seen.add(module) && !module.isUnnamed()) {
                for (ModuleElement.Directive directive : module.getDirectives()) {
                    if (directive.getKind() == ModuleElement.DirectiveKind.REQUIRES) {
                        ModuleElement.RequiresDirective req = (ModuleElement.RequiresDirective) directive;
                        if (topLevel || req.isTransitive()|| isMandated(req)) {
                            final ModuleElement dependency = req.getDependency();
                            boolean add = true;
                            if (transitive) {
                                add = collectRequiredModulesImpl(dependency, null, transitive, false, modulesByName, seen, c);
                            }
                            if (add) {
                                final List<URL> dependencyURLs = modulesByName.get(dependency.getQualifiedName().toString());
                                if (dependencyURLs != null) {
                                    c.addAll(dependencyURLs);
                                }
                            }
                        }
                    }
                }
                if (moduleTree != null) {
                    //Add dependencies for non resolvable modules.
                    moduleTree.accept(new ErrorAwareTreeScanner<Void, Void>() {
                                @Override
                                public Void visitRequires(RequiresTree node, Void p) {
                                    final String moduleName = node.getModuleName().toString();
                                    Optional.ofNullable(modulesByName.get(moduleName))
                                            .ifPresent(c::addAll);
                                    return super.visitRequires(node, p);
                                }
                            },
                            null);
                }
                return true;
            } else {
                return false;
            }
        }

        @NonNull
        private static List<PathResourceImplementation> filterModules(
                @NonNull List<PathResourceImplementation> modules,
                @NonNull Set<URL> requires,
                @NonNull final Function<URL,Boolean> filter) {
            final List<PathResourceImplementation> res = new ArrayList<>(modules.size());
            for (PathResourceImplementation pr : modules) {
                for (URL url : pr.getRoots()) {
                    final Boolean vote = filter.apply(url);
                    if (vote == Boolean.TRUE || (vote == null && requires.contains(url))) {
                        res.add(pr);
                    }
                }
            }
            return res;
        }
        
        private static boolean supportsModules(
            @NonNull final ClassPath boot,
            @NonNull final ClassPath compile,
            @NonNull final ClassPath src) {
            if (boot.findResource("java/util/zip/CRC32C.class") != null) {  //NOI18N
                return true;
            }
            if (compile.findResource("java/util/zip/CRC32C.class") != null) {   //NOI18N
                return true;
            }
            return src.findResource("java/util/zip/CRC32C.java") != null;   //NOI18N
        }
        
        private static boolean isMandated(@NonNull final ModuleElement.RequiresDirective rd) {
            return Optional.ofNullable(rd.getDependency())
                    .map((me) -> MOD_JAVA_BASE.equals(me.getQualifiedName().toString()))
                    .orElse(Boolean.FALSE);
        }
        
        private static final class ModuleNames implements Predicate<ModuleElement> {
            private final Set<? extends String> moduleNames;

            private ModuleNames(@NonNull final Set<? extends String> names) {
                this.moduleNames = names;
            }

            @Override
            public boolean test(ModuleElement t) {
                return moduleNames.contains(t.getQualifiedName().toString());
            }       

            @NonNull
            static Predicate<ModuleElement> create(@NonNull final Set<? extends String> name) {
                return new ModuleNames(name);
            }
        }
    }

    private abstract static class BaseClassPathImplementation implements ClassPathImplementation {

        private final PropertyChangeSupport listeners;
        //@GuardedBy("this")
        private List<PathResourceImplementation> cache;

        BaseClassPathImplementation() {
            this(null);
        }

        BaseClassPathImplementation(final List<PathResourceImplementation> initialValue) {
            this.listeners = new PropertyChangeSupport(this);
            synchronized (this) {
                this.cache = initialValue;
            }
        }

        @Override
        public final void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            this.listeners.addPropertyChangeListener(listener);
        }

        @Override
        public final void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            this.listeners.removePropertyChangeListener(listener);
        }

        @CheckForNull
        final synchronized List<PathResourceImplementation> getCache() {
            return this.cache;
        }

        final synchronized void setCache(@NullAllowed final List<PathResourceImplementation> cache) {
            this.cache = cache;
        }

        final void resetCache(@NonNull final String... propNames) {
            resetCache(null, propNames);
        }

        final void resetCache(
                @NullAllowed final List<PathResourceImplementation> update,
                @NonNull final String... propNames) {
            synchronized (this) {
                this.cache = update;
            }
            fire(propNames);
        }

        final void fire(@NonNull final String... propNames) {
            for (String pn : propNames) {
                this.listeners.firePropertyChange(pn, null, null);
            }
        }
    }

}
