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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.modules.java.api.common.impl.Utilities;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * Implementation of {@link ClassPathProvider} for the Multi-Module project.
 * @author Tomas Zezula
 * @since 1.99
 */
public final class MultiModuleClassPathProvider extends AbstractClassPathProvider {
    private static final Logger LOG = Logger.getLogger(MultiModuleClassPathProvider.class.getName());
    private static final String INTERNAL_MODULE_BINARIES_PATH = "internal-module-bin-path"; //NOI18N
    private final AntProjectHelper helper;
    private final File projectDirectory;
    private final PropertyEvaluator eval;
    private final Map<String,Function<Owner,ClassPath>> modSensitivePrjPathFcts;
    private final Cache sourceCache;
    private final Cache testCache;
    private final String[] modulePath;
    private final String[] testModulePath;
    private final String[] javacClassPath;
    private final String[] testJavacClassPath;
    private final String[] executeModulePath;
    private final String[] testExecuteModulePath;
    private final String[] executeClassPath;
    private final String[] testExecuteClassPath;
    private final String[] processorClassPath;
    private final String[] testProcessorClassPath;
    private final String[] processorModulePath;
    private final String[] testProcessorModulePath;
    private final String platformType;
    private final String buildModulesDirProperty;
    private final Map</*@GuardedBy("this")*/String,URL> urlCache = new ConcurrentHashMap<>();
    private final Map</*@GuardedBy("this")*/String,FileObject> dirCache = new ConcurrentHashMap<>();

    private MultiModuleClassPathProvider(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator eval,
            @NonNull final MultiModule modules,
            @NonNull final MultiModule testModules,
            @NonNull final String[] modulePath,
            @NonNull final String[] testModulePath,
            @NonNull final String[] javacClassPath,
            @NonNull final String[] testJavacClassPath,
            @NonNull final String[] executeModulePath,
            @NonNull final String[] testExecuteModulePath,
            @NonNull final String[] executeClassPath,
            @NonNull final String[] testExecuteClassPath,
            @NonNull final String[] processorModulePath,
            @NonNull final String[] testProcessorModulePath,
            @NonNull final String[] processorClassPath,
            @NonNull final String[] testProcessorClassPath,
            @NonNull final String platformType,
            @NonNull final String buildModulesDirProperty) {
        Parameters.notNull("helper", helper);   //NOI18N
        Parameters.notNull("eval", eval);   //NOI18N
        Parameters.notNull("modules", modules); //NOI18N
        Parameters.notNull("testModules", testModules); //NOI18N
        Parameters.notNull("modulePath", modulePath);           //NOI18N
        Parameters.notNull("testModulePath", testModulePath);   //NOI18N
        Parameters.notNull("javacClassPath", javacClassPath);   //NOI18N
        Parameters.notNull("testJavacClassPath", testJavacClassPath);   //NOI18N
        Parameters.notNull("executeModulePath", executeModulePath);   //NOI18N
        Parameters.notNull("testExecuteModulePath", testExecuteModulePath);   //NOI18N
        Parameters.notNull("executeClassPath", executeClassPath);   //NOI18N
        Parameters.notNull("testExecuteClassPath", testExecuteClassPath);   //NOI18N
        Parameters.notNull("processorModulePath", processorModulePath);           //NOI18N
        Parameters.notNull("testProcessorModulePath", testProcessorModulePath);   //NOI18N
        Parameters.notNull("processorClassPath", processorClassPath);           //NOI18N
        Parameters.notNull("testProcessorClassPath", testProcessorClassPath);   //NOI18N
        Parameters.notNull("platformType", platformType);   //NOI18N
        Parameters.notNull("buildModulesDirProperty", buildModulesDirProperty);
        this.helper = helper;
        this.projectDirectory = FileUtil.toFile(helper.getProjectDirectory());
        this.eval = eval;
        this.modSensitivePrjPathFcts = new HashMap<>();
        this.modSensitivePrjPathFcts.put(ClassPath.BOOT,(o) -> getBootClassPath(o));
        this.modSensitivePrjPathFcts.put(ClassPath.COMPILE,(o)-> getCompileTimeClasspath(o));
        this.modSensitivePrjPathFcts.put(ClassPath.SOURCE,(o)-> getSourcepath(o));
        this.modSensitivePrjPathFcts.put(ClassPath.EXECUTE,(o) -> getRunTimeClasspath(o));
        this.sourceCache = new Cache(modules, () -> fireClassPathsChange(modSensitivePrjPathFcts.keySet()));
        this.testCache = new Cache(testModules, () -> fireClassPathsChange(modSensitivePrjPathFcts.keySet()));
        this.modulePath = modulePath;
        this.testModulePath = testModulePath;
        this.javacClassPath = javacClassPath;
        this.testJavacClassPath = testJavacClassPath;
        this.executeModulePath = executeModulePath;
        this.testExecuteModulePath = testExecuteModulePath;
        this.executeClassPath = executeClassPath;
        this.testExecuteClassPath = testExecuteClassPath;
        this.processorModulePath = processorModulePath;
        this.testProcessorModulePath = testProcessorModulePath;
        this.processorClassPath = processorClassPath;
        this.testProcessorClassPath = testProcessorClassPath;
        this.platformType = platformType;
        this.buildModulesDirProperty = buildModulesDirProperty;
    }

    @CheckForNull
    private URL getURL(@NonNull final String propname) {
        URL u = urlCache.get(propname);
        if (u == null) {
            final String prop = eval.getProperty(propname);
            if (prop != null) {
                final File file = helper.resolveFile(prop);
                if (file != null) {
                    try {
                        u = BaseUtilities.toURI(file).toURL();
                        urlCache.put (propname, u);
                    } catch (MalformedURLException e) {
                        LOG.log(
                                Level.WARNING,
                                "Cannot convert to URL: {0}",   //NOI18N
                                file.getAbsolutePath());
                    }
                }
            }
        }
        return u;
    }

    @CheckForNull
    private FileObject getDir(@NonNull final String propname) {
        FileObject fo = dirCache.get(propname);
        if (fo == null || !fo.isValid()) {
            final URL u = getURL(propname);
            if (u != null) {
                fo = URLMapper.findFileObject(u);
                if (fo != null) {
                    dirCache.put (propname, fo);
                }
            }
        }
        return fo;
    }

    @CheckForNull
    private Owner getOwner(
            @NonNull final FileObject file) {
        String modName = sourceCache.getModules().getModuleName(file);
        if (modName != null) {
            return new Owner(false, Location.SOURCE, modName);
        }
        modName = testCache.getModules().getModuleName(file);
        if (modName != null) {
            return new Owner(true, Location.SOURCE, modName);
        }
        return null;
    }

    @CheckForNull
    private ClassPath getModuleSourcePath(
            @NonNull final FileObject file) {
        return getModuleSourcePath(getOwner(file));
    }

    @CheckForNull
    private ClassPath getModuleSourcePath(
            @NullAllowed final Owner owner) {
        if (owner != null && owner.isSource()) {
            return cacheFor(owner).computeIfAbsent(
                    null,
                    JavaClassPathConstants.MODULE_SOURCE_PATH,
                    (mods) -> mods.getSourceModulePath());
        } else {
            return null;
        }
    }

    @CheckForNull
    private ClassPath getModuleBootPath() {
        final Owner owner = Owner.GLOBAL_SOURCE;
        return cacheFor(owner).computeIfAbsent(
                null,
                JavaClassPathConstants.MODULE_BOOT_PATH,
                (mods) -> ClassPathFactory.createClassPath(ModuleClassPaths.createPlatformModulePath(eval, platformType)));
    }

    @CheckForNull
    private ClassPath getModuleCompilePath(
            @NonNull final FileObject file) {
        return getModuleCompilePath(getOwner(file));
    }

    @CheckForNull
    private ClassPath getModuleCompilePath(
            @NullAllowed final Owner owner) {
        if (owner != null && owner.isSource()) {
            return cacheFor(owner).computeIfAbsent(
                    null,
                    JavaClassPathConstants.MODULE_COMPILE_PATH,
                    !owner.isTest() ?
                            (mods) -> {
                                ClassPath impl = ClassPathFactory.createClassPath(ModuleClassPaths.createPropertyBasedModulePath(
                                        projectDirectory,
                                        eval,
                                        null,
                                        modulePath));
                                impl = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(
                                        getMultiModuleBinariesPath(Owner.GLOBAL_SOURCE),
                                        impl);
                                return impl;
                            }:
                          (mods) -> {
                              ClassPath impl = ClassPathFactory.createClassPath(ModuleClassPaths.createPropertyBasedModulePath(
                                        projectDirectory,
                                        eval,
                                        (root) -> {
                                            final URL buildModules = getURL(buildModulesDirProperty);
                                            return buildModules == null || !Utilities.isParentOf(buildModules, root);
                                        },
                                        testModulePath));
                                impl = org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(
                                        getMultiModuleBinariesPath(Owner.GLOBAL_TESTS),
                                        ClassPathFactory.createClassPath(new TranslateBuildModules(getMultiModuleBinariesPath(Owner.GLOBAL_SOURCE), testModulePath)),
                                        impl);
                                return impl;
                          });
        } else {
            return null;
        }
    }

    @CheckForNull
    private ClassPath getModuleLegacyClassPath(
            @NonNull final FileObject file) {
        return getModuleLegacyClassPath(getOwner(file));
    }

    @CheckForNull
    private ClassPath getModuleLegacyClassPath(
            @NullAllowed final Owner owner) {
        if (owner != null && owner.isSource()) {
            return cacheFor(owner).computeIfAbsent(
                    null,
                    JavaClassPathConstants.MODULE_CLASS_PATH,
                    (mods) -> {
                        return ClassPathFactory.createClassPath(ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                                projectDirectory,
                                eval,
                                owner.isTest() ? testJavacClassPath : javacClassPath));
                    });
        } else {
            return null;
        }
    }

    @CheckForNull
    private ClassPath getModuleExecutePath(
            @NonNull final FileObject file) {
        return getModuleExecutePath(getOwner(file));
    }

    @CheckForNull
    private ClassPath getModuleExecutePath(
            @NullAllowed final Owner owner) {
        if (owner != null) {
            return cacheFor(owner).computeIfAbsent(
                    null,
                    upgradeType(JavaClassPathConstants.MODULE_EXECUTE_PATH, owner),
                    (mods) -> {
                        return ClassPathFactory.createClassPath(org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPathImplementation(
                            ModuleClassPaths.createMultiModuleBinariesPath(mods, owner.getLocation() == Location.DIST, owner.isTest()),
                            ModuleClassPaths.createPropertyBasedModulePath(
                                    projectDirectory,
                                    eval,
                                    null,
                                    owner.isTest() ? testExecuteModulePath : executeModulePath)));
                    });
        } else {
            return null;
        }
    }

    @CheckForNull
    private ClassPath getModuleLegacyExecuteClassPath(
            @NonNull final FileObject file) {
        return getModuleLegacyExecuteClassPath(getOwner(file));
    }

    @CheckForNull
    private ClassPath getModuleLegacyExecuteClassPath(
            @NullAllowed final Owner owner) {
        if (owner != null) {
            return cacheFor(owner).computeIfAbsent(
                    null,
                    JavaClassPathConstants.MODULE_EXECUTE_CLASS_PATH,
                    (mods) -> {
                        return ClassPathFactory.createClassPath(ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                            projectDirectory,
                            eval,
                            owner.isTest() ? testExecuteClassPath : executeClassPath));
                    });
        } else {
            return null;
        }
    }

    @CheckForNull
    private ClassPath getProcessorClasspath(
            @NonNull final FileObject file) {
        return getProcessorClasspath(getOwner(file));
    }

    @CheckForNull
    private ClassPath getProcessorClasspath(
            @NullAllowed final Owner owner) {
        if (owner != null && owner.isSource()) {
            return cacheFor(owner).computeIfAbsent(
                    null,
                    JavaClassPathConstants.PROCESSOR_PATH,
                    (mods) -> {
                        return ClassPathFactory.createClassPath(ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                            projectDirectory,
                           eval,
                           owner.isTest() ? testProcessorClassPath : processorClassPath));
                    });
        } else {
            return null;
        }
    }

    @CheckForNull
    private ClassPath getProcessorModulepath(
            @NonNull final FileObject file) {
        return getProcessorModulepath(getOwner(file));
    }

    @CheckForNull
    private ClassPath getProcessorModulepath(
            @NullAllowed final Owner owner) {
        if (owner != null && owner.isSource()) {
            return cacheFor(owner).computeIfAbsent(
                    null,
                    JavaClassPathConstants.MODULE_PROCESSOR_PATH,
                    (mods) -> {
                        return ClassPathFactory.createClassPath(ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                            projectDirectory,
                           eval,
                           owner.isTest() ? testProcessorModulePath : processorModulePath));
                    });
        } else {
            return null;
        }
    }

    @CheckForNull
    private ClassPath getSourcepath(
            @NonNull final FileObject file) {
        return getSourcepath(getOwner(file));
    }

    @CheckForNull
    private ClassPath getSourcepath(
            @NullAllowed final Owner owner) {
        if (owner != null && owner.isSource() && owner.getModuleName() != null) {
            //No need to cache, the MultiModule already does
            return cacheFor(owner).getModules().getModuleSources(owner.getModuleName());
        } else {
            return null;
        }
    }

    @CheckForNull
    private ClassPath getBootClassPath(
            @NonNull final FileObject file) {
        return getBootClassPath(getOwner(file));
    }

    private ClassPath getBootClassPath(
            @NullAllowed final Owner owner) {
        if (owner != null && owner.getModuleName() != null) {
            return cacheFor(owner).computeIfAbsent(
                    owner.getModuleName(),
                    ClassPath.BOOT,
                    (mods) -> {
                        final ClassPath sourcePath = getSourcepath(owner);
                        final ClassPath systemModules = getModuleBootPath();
                        final ClassPath modulePath = getModuleCompilePath(owner);
                        if (sourcePath != null && systemModules != null && modulePath != null) {
                            return ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                                    systemModules,
                                    sourcePath,
                                    systemModules,
                                    modulePath,
                                    null,
                                    null));
                        } else {
                            return null;
                        }
                    });
        } else {
            return null;
        }
    }

    @CheckForNull
    private ClassPath getCompileTimeClasspath(
            @NonNull final FileObject file) {
        return getCompileTimeClasspath(getOwner(file));
    }

    @CheckForNull
    private ClassPath getCompileTimeClasspath(
            @NullAllowed final Owner owner) {
        if (owner != null && owner.isSource() && owner.getModuleName() != null) {
            return cacheFor(owner).computeIfAbsent(
                    owner.getModuleName(),
                    ClassPath.COMPILE,
                    (mods) -> {
                        final ClassPath sourcePath = getSourcepath(owner);
                        final ClassPath systemModules = getModuleBootPath();
                        final ClassPath modulePath = getModuleCompilePath(owner);
                        if (sourcePath != null && systemModules != null && modulePath != null) {
                            return ClassPathFactory.createClassPath(ModuleClassPaths.createModuleInfoBasedPath(
                                    modulePath,
                                    sourcePath,
                                    systemModules,
                                    modulePath,
                                    getModuleLegacyClassPath(owner),
                                    null));
                        } else {
                            return null;
                        }
                    });
        } else {
            return null;
        }
    }

    @CheckForNull
    private ClassPath getRunTimeClasspath(
            @NonNull final FileObject file) {
        return getRunTimeClasspath(getOwner(file));
    }

    @CheckForNull
    private ClassPath getRunTimeClasspath(
            @NullAllowed final Owner owner) {
        if (owner != null && owner.getModuleName() != null) {
            return cacheFor(owner).computeIfAbsent(
                    owner.getModuleName(),
                    ClassPath.EXECUTE,
                    (mods) -> {
                        final ClassPath sourcePath = getSourcepath(owner);
                        final ClassPath systemModules = getModuleBootPath();
                        final ClassPath execPath = getModuleExecutePath(owner);
                        if (sourcePath != null && systemModules != null && execPath != null) {
                            return ClassPathFactory.createClassPath(
                                    ModuleClassPaths.createModuleInfoBasedPath(
                                        execPath,
                                        sourcePath,
                                        systemModules,
                                        execPath,
                                        getModuleLegacyExecuteClassPath(owner),
                                        getFilter(owner)));
                        } else {
                            return null;
                        }
                    });
        } else {
            return null;
        }
    }

    @NonNull
    private ClassPath getMultiModuleBinariesPath(@NonNull final Owner owner) {
        return cacheFor(owner).computeIfAbsent(
                null,
                INTERNAL_MODULE_BINARIES_PATH,
                (mods) -> ClassPathFactory.createClassPath(ModuleClassPaths.createMultiModuleBinariesPath(mods, !owner.isTest(), owner.isTest())));
    }

    @NonNull
    private Function<URL,Boolean> getFilter(@NonNull final Owner owner) {
        if (owner.isTest()) {
            //Todo: Correct test modules properties
            if (owner.getLocation() == Location.DIST) {
                return new Filter(helper, eval, ProjectProperties.DIST_DIR);
            } else {
                return new Filter(helper, eval, ProjectProperties.BUILD_MODULES_DIR);
            }
        } else {
            if (owner.getLocation() == Location.DIST) {
                return new Filter(helper, eval, ProjectProperties.DIST_DIR);
            } else {
                return new Filter(helper, eval, ProjectProperties.BUILD_MODULES_DIR);
            }
        }
    }

    @NonNull
    private static String upgradeType(
            @NonNull final String type,
            @NonNull final Owner owner) {
        if (owner.getLocation() == Location.DIST) {
            return String.format("%s+dist",type);   //NOI18N
        } else {
            return type;
        }
    }

    @NonNull
    private Cache cacheFor(@NonNull final Owner owner) {
        return owner.isTest() ? testCache : sourceCache;
    }

    private final class TranslateBuildModules implements ClassPathImplementation, PropertyChangeListener {
        private final ClassPath delegate;
        private final Collection<String> props;
        private final AtomicReference<List<PathResourceImplementation>> cache;
        private final PropertyChangeSupport listeners;

        TranslateBuildModules(
                @NonNull final ClassPath delegate,
                @NonNull final String... props) {
            this.delegate = delegate;
            this.props = new HashSet<>();
            Collections.addAll(this.props, props);
            this.cache = new AtomicReference<>();
            this.listeners = new PropertyChangeSupport(this);
            this.delegate.addPropertyChangeListener(WeakListeners.propertyChange(this, this.delegate));
            eval.addPropertyChangeListener(WeakListeners.propertyChange(this, eval));
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            List<PathResourceImplementation> res = cache.get();
            if (res == null) {
                final Set<File> seen = props.stream()
                        .map((p) -> eval.getProperty(p))
                        .filter((p) -> p != null)
                        .flatMap((p) -> Arrays.stream(PropertyUtils.tokenizePath(p)))
                        .map((p) -> helper.resolveFile(p))
                        .collect(Collectors.toSet());
                final URL url = getURL(buildModulesDirProperty);
                res = Collections.emptyList();
                if (url != null) {
                    try {
                        final File buildModules = BaseUtilities.toFile(url.toURI());
                        if (seen.contains(buildModules)) {  //TODO: If contains only module under buildModules add just the module
                            res = Collections.unmodifiableList(delegate.entries().stream()
                                .map((e) -> org.netbeans.spi.java.classpath.support.ClassPathSupport.createResource(e.getURL()))
                                .collect(Collectors.toList()));
                        }
                    } catch (URISyntaxException e) {
                        LOG.log(
                                Level.WARNING,
                                "Cannot convert to URI: {0}",   //NOI18N
                                url);
                    }
                }
                if (!cache.compareAndSet(null, res)) {
                    res = Optional.ofNullable(cache.get()).orElse(res);
                }
            }
            return res;
        }

        @Override
        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            listeners.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            listeners.removePropertyChangeListener(listener);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (ClassPath.PROP_ENTRIES.equals(propName) ||
                propName == null ||
                props.contains(propName)) {
                cache.set(null);
                listeners.firePropertyChange(PROP_RESOURCES, null, null);
            }
        }
    }

    private static final class Filter implements Function<URL, Boolean>, PropertyChangeListener {
        private final AntProjectHelper helper;
        private final PropertyEvaluator eval;
        private final String ownerProp;
        //@GuardedBy(this)
        private Set<URL> includeIn;

        private Filter(
                @NonNull final AntProjectHelper helper,
                @NonNull final PropertyEvaluator eval,
                @NonNull final String ownerProp) {
            Parameters.notNull("helper", helper);   //NOI18N
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("ownerProp", ownerProp); //NOI18N
            this.helper = helper;
            this.eval = eval;
            this.ownerProp = ownerProp;
            this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
        }

        @Override
        public Boolean apply(URL t) {
            final Collection<? extends URL> roots = getRoots();
            for (URL u : roots) {
                if (isParentOf(u, t)) {
                    return true;
                }
            }
            return null;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (propName == null || propName.equals(ownerProp)) {
                synchronized (this) {
                    includeIn = null;
                }
            }
        }

        @NonNull
        private Collection<? extends URL> getRoots() {
            synchronized (this) {
                if (includeIn != null) {
                    return includeIn;
                }
            }
            URL url = null;
            final String val = eval.getProperty(ownerProp);
            if (val != null) {
                try {
                    File f = helper.resolveFile(val);
                    url = BaseUtilities.toURI(f).toURL();
                } catch (MalformedURLException e) {
                    LOG.warning(e.toString());
                }
            }
            synchronized (this) {
                if (includeIn == null) {
                    includeIn = url == null ?
                            Collections.emptySet() :
                            Collections.singleton(url);
                }
                return includeIn;
            }
        }

        private static boolean isParentOf(
                @NonNull final URL folder,
                @NonNull final URL file) {
            String sfld = folder.toExternalForm();
            if (sfld.charAt(sfld.length()-1) != '/') {  //NOI18N
                sfld = sfld + '/';                      //NOI18N
            }
            final String sfil = file.toExternalForm();
            return sfil.startsWith(sfld);
        }
    }

    private static enum Location {
        SOURCE(true),
        BUILD(false),
        DIST(false);

        private final boolean source;

        private Location(final boolean source) {
            this.source = source;
        }

        boolean isSource() {
            return source;
        }
    }

    private static final class Owner {
        private static final Owner GLOBAL_SOURCE = new Owner(false, Location.SOURCE, null);
        private static final Owner GLOBAL_TESTS = new Owner(true, Location.SOURCE, null);

        private final boolean test;
        private final Location loc;
        private final String moduleName;

        Owner(
                final boolean test,
                @NonNull final Location location,
                @NullAllowed final String moduleName) {
            this.test = test;
            this.loc = location;
            this.moduleName = moduleName;
        }

        boolean isTest() {
            return test;
        }

        boolean isSource() {
            return loc.isSource();
        }

        @NonNull
        Location getLocation() {
            return loc;
        }

        @CheckForNull
        String getModuleName() {
            return moduleName;
        }
    }

    private static final class Cache implements PropertyChangeListener {
        private final MultiModule modules;
        private final Runnable firer;
        private final Map</*GuardedBy("this")*/String,ClassPath> globals;
        private final Map</*GuardedBy("this")*/String,Map<String,ClassPath>> perModule;
        private final Set</*GuardedBy("this")*/String> currentModuleNames;

        Cache(
                @NonNull final MultiModule modules,
                @NonNull final Runnable firer) {
            this.modules = modules;
            this.firer = firer;
            this.globals = new HashMap<>();
            this.perModule = new HashMap<>();
            this.currentModuleNames = new HashSet<>(this.modules.getModuleNames());
            this.modules.addPropertyChangeListener(WeakListeners.propertyChange(this, this.modules));
        }

        @NonNull
        MultiModule getModules() {
            return this.modules;
        }

        @CheckForNull
        ClassPath computeIfAbsent(
            @NullAllowed final String module,
            @NonNull final String cpType,
            @NonNull final Function<MultiModule,ClassPath> factory) {
            if (module == null) {
                return computeIfAbsentGlobal(cpType, factory);
            } else {
                return computeIfAbsentModule(module, cpType, factory);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (MultiModule.PROP_MODULES.equals(evt.getPropertyName())) {
                boolean fire = false;
                synchronized (this) {
                    final Set<String> toRemove = new HashSet<>(currentModuleNames);
                    final Collection<? extends String> newModuleNames = modules.getModuleNames();
                    final Set<String> toAdd = new HashSet<>(newModuleNames);
                    toAdd.removeAll(toRemove);
                    toRemove.removeAll(newModuleNames);
                    for (String removedModule : toRemove) {
                        fire |= perModule.remove(removedModule) != null;
                        currentModuleNames.remove(removedModule);
                    }
                    for (String addedModule : toAdd) {
                        fire |= currentModuleNames.add(addedModule);
                    }
                }
                if (fire) {
                    firer.run();
                }
            }
        }

        @CheckForNull
        private ClassPath computeIfAbsentGlobal(
                @NonNull final String cpType,
                @NonNull final Function<MultiModule,ClassPath> factory) {
            synchronized (this) {
                ClassPath res = globals.get(cpType);
                if (res != null) {
                    return res;
                }
            }
            return ProjectManager.mutex().readAccess(()-> {
                synchronized(this) {
                    //Map.computeIfAbsent cannot be used as factory may be reentrant
                    ClassPath cp = globals.get(cpType);
                    if (cp == null) {
                        cp = factory.apply(modules);
                        ClassPath oldCp = globals.putIfAbsent(cpType, cp);
                        if (oldCp != null) {
                            cp = oldCp;
                        }
                    }
                    return cp;
                }
            });
        }

        @CheckForNull
        private ClassPath computeIfAbsentModule(
                @NonNull final String module,
                @NonNull final String cpType,
                @NonNull final Function<MultiModule,ClassPath> factory) {
            synchronized (this) {
                final ClassPath cp = Optional.ofNullable(perModule.get(module))
                        .map((m) -> m.get(cpType))
                        .orElse(null);
                if (cp != null) {
                    return cp;
                }
            }
            return ProjectManager.mutex().readAccess(() -> {
                synchronized (this) {
                    final Map<String,ClassPath> moduleCps = perModule.computeIfAbsent(module, (mn) -> new HashMap<>());
                    //Map.computeIfAbsent cannot be used as factory may be reentrant
                    ClassPath cp = moduleCps.get(cpType);
                    if (cp == null) {
                        cp = factory.apply(modules);
                        ClassPath oldCp = moduleCps.putIfAbsent(cpType, cp);
                        if (oldCp != null) {
                            cp = oldCp;
                        }
                    }
                    return cp;
                }
            });
        }
    }

    private void collectPath(
            @NonNull final List<? super ClassPath> collector,
            @NonNull final Owner owner,
            @NonNull final Function<Owner,ClassPath> f) {
        for (String mn : cacheFor(owner).getModules().getModuleNames()) {
            final ClassPath cp = f.apply(new Owner(owner.isTest(), owner.getLocation(), mn));
            if (cp != null) {
                collector.add(cp);
            }
        }
    }

    @Override
    public ClassPath findClassPath(@NonNull final FileObject file, @NonNull final String type) {
        Parameters.notNull("file", file);   //NOI18N
        Parameters.notNull("type", type);   //NOI18N
        LOG.log(Level.FINE,
                "Find ClassPath of type {0} for {1}",   //NOI18N
                new Object[] {
                    type,
                    file
                });
        final ClassPath res;
        if (type.equals(JavaClassPathConstants.MODULE_SOURCE_PATH)) {
            res = getModuleSourcePath(file);
        } else if (type.equals(JavaClassPathConstants.MODULE_BOOT_PATH)) {
            res = getModuleBootPath();
        } else if (type.equals(JavaClassPathConstants.MODULE_COMPILE_PATH)) {
            res = getModuleCompilePath(file);
        } else if (type.equals(JavaClassPathConstants.MODULE_CLASS_PATH)) {
            res = getModuleLegacyClassPath(file);
        } else if (type.equals(JavaClassPathConstants.MODULE_PROCESSOR_PATH)) {
            res = getProcessorModulepath(file);
        } else if (type.equals(JavaClassPathConstants.MODULE_EXECUTE_PATH)) {
            res = getModuleExecutePath(file);
        } else if (type.equals(JavaClassPathConstants.MODULE_EXECUTE_CLASS_PATH)) {
            res = getModuleLegacyExecuteClassPath(file);
        } else if (type.equals(ClassPathSupport.ENDORSED)) {
            res = ClassPath.EMPTY;  //Compatibility
        } else if (type.equals(JavaClassPathConstants.PROCESSOR_PATH)) {
            res = getProcessorClasspath(file);
        } else if (type.equals(ClassPath.SOURCE)) {
            res = getSourcepath(file);
        } else if (type.equals(ClassPath.BOOT)) {
            res = getBootClassPath(file);
        } else if (type.equals(ClassPath.COMPILE)) {
            res = getCompileTimeClasspath(file);
        } else if (type.equals(ClassPath.EXECUTE)) {
            res = getRunTimeClasspath(file);
        } else {
            res = null;
        }
        LOG.log(Level.FINE,
                "Result: ",   //NOI18N
                res);
        return res;
    }

    @Override
    public String[] getPropertyName (SourceGroup sg, String type) {
        FileObject root = sg.getRootFolder();
        final Owner fOwner = getOwner(root);
        if (fOwner == null) {
            return null;
        } else if (fOwner.isTest()) {
            switch (type) {
                case ClassPath.COMPILE:
                    return testJavacClassPath;
                case ClassPath.EXECUTE:
                    return testExecuteClassPath;
                case JavaClassPathConstants.PROCESSOR_PATH:
                    return testProcessorClassPath;
                case JavaClassPathConstants.MODULE_COMPILE_PATH:
                    return testModulePath;
                case JavaClassPathConstants.MODULE_PROCESSOR_PATH:
                    return testProcessorModulePath;
                case JavaClassPathConstants.MODULE_EXECUTE_PATH:
                    return testExecuteModulePath;
                default:
                    return null;
            }
        } else {
            switch (type) {
                case ClassPath.COMPILE:
                    return javacClassPath;
                case ClassPath.EXECUTE:
                    return executeClassPath;
                case JavaClassPathConstants.PROCESSOR_PATH:
                    return processorClassPath;
                case JavaClassPathConstants.MODULE_COMPILE_PATH:
                    return modulePath;
                case JavaClassPathConstants.MODULE_PROCESSOR_PATH:
                    return processorModulePath;
                case JavaClassPathConstants.MODULE_EXECUTE_PATH:
                    return executeModulePath;
                default:
                    return null;
            }
        }
    }

    @Override
    public ClassPath[] getProjectClassPaths(String type) {
        return ProjectManager.mutex().readAccess(() -> {
            if (JavaClassPathConstants.MODULE_BOOT_PATH.equals(type)) {
                return new ClassPath[] {getModuleBootPath()};
            }
            if (JavaClassPathConstants.MODULE_SOURCE_PATH.equals(type)) {
                return new ClassPath[] {
                    getModuleSourcePath(Owner.GLOBAL_SOURCE),
                    getModuleSourcePath(Owner.GLOBAL_TESTS)
                };
            }
            if (JavaClassPathConstants.MODULE_CLASS_PATH.equals(type)) {
                return new ClassPath[] {
                    getModuleLegacyClassPath(Owner.GLOBAL_SOURCE),
                    getModuleLegacyClassPath(Owner.GLOBAL_TESTS)
                };
            }
            if (JavaClassPathConstants.MODULE_COMPILE_PATH.equals(type)) {
                return new ClassPath[] {
                    getModuleCompilePath(Owner.GLOBAL_SOURCE),
                    getModuleCompilePath(Owner.GLOBAL_TESTS)
                };
            }
            if (JavaClassPathConstants.MODULE_PROCESSOR_PATH.equals(type)) {
                return new ClassPath[] {
                    getProcessorModulepath(Owner.GLOBAL_SOURCE),
                    getProcessorModulepath(Owner.GLOBAL_TESTS)
                };
            }
            if (JavaClassPathConstants.PROCESSOR_PATH.equals(type)) {
                return new ClassPath[] {
                    getProcessorClasspath(Owner.GLOBAL_SOURCE),
                    getProcessorClasspath(Owner.GLOBAL_TESTS)
                };
            }
            if (JavaClassPathConstants.MODULE_EXECUTE_PATH.equals(type)) {
                return new ClassPath[] {
                    getModuleExecutePath(Owner.GLOBAL_SOURCE),
                    getModuleExecutePath(Owner.GLOBAL_TESTS),
                };
            }
            if (JavaClassPathConstants.MODULE_EXECUTE_CLASS_PATH.equals(type)) {
                return new ClassPath[] {
                    getModuleLegacyExecuteClassPath(Owner.GLOBAL_SOURCE),
                    getModuleLegacyExecuteClassPath(Owner.GLOBAL_TESTS),
                };
            }
            final Function<Owner,ClassPath> f = modSensitivePrjPathFcts.get(type);
            if (f != null) {
                final List<ClassPath> cps = new ArrayList<>();
                collectPath(cps, Owner.GLOBAL_SOURCE, f);
                collectPath(cps, Owner.GLOBAL_TESTS, f);
                return cps.toArray(new ClassPath[0]);
            }
            assert false : "Unsupported ClassPath type: " + type;   //NOI18N
            return new ClassPath[0];
        });
    }

    /**
     * Returns the given type of the classpath for the project sources
     * (i.e., excluding tests roots).
     */
    public ClassPath getProjectSourcesClassPath(String type) {
        return ProjectManager.mutex().readAccess(() -> {
            if (JavaClassPathConstants.MODULE_BOOT_PATH.equals(type)) {
                return getModuleBootPath();
            }
            if (JavaClassPathConstants.MODULE_SOURCE_PATH.equals(type)) {
                return getModuleSourcePath(Owner.GLOBAL_SOURCE);
            }
            if (JavaClassPathConstants.MODULE_CLASS_PATH.equals(type)) {
                return getModuleLegacyClassPath(Owner.GLOBAL_SOURCE);
            }
            if (JavaClassPathConstants.MODULE_COMPILE_PATH.equals(type)) {
                return getModuleCompilePath(Owner.GLOBAL_SOURCE);
            }
            if (JavaClassPathConstants.MODULE_PROCESSOR_PATH.equals(type)) {
                return getProcessorModulepath(Owner.GLOBAL_SOURCE);
            }
            if (JavaClassPathConstants.PROCESSOR_PATH.equals(type)) {
                return getProcessorClasspath(Owner.GLOBAL_SOURCE);
            }
            if (JavaClassPathConstants.MODULE_EXECUTE_PATH.equals(type)) {
                return getModuleExecutePath(Owner.GLOBAL_SOURCE);
            }
            if (JavaClassPathConstants.MODULE_EXECUTE_CLASS_PATH.equals(type)) {
                return getModuleLegacyExecuteClassPath(Owner.GLOBAL_SOURCE);
            }
            final Function<Owner,ClassPath> f = modSensitivePrjPathFcts.get(type);
            if (f != null) {
                final List<ClassPath> cps = new ArrayList<>();
                collectPath(cps, Owner.GLOBAL_SOURCE, f);
                return cps.isEmpty() ? null : org.netbeans.spi.java.classpath.support.ClassPathSupport.createProxyClassPath(cps.toArray(new ClassPath[0]));
            }
            assert false : "Unsupported ClassPath type: " + type;   //NOI18N
            return null;
        });
    }

    public static final class Builder {

        private final AntProjectHelper helper;
        private final PropertyEvaluator eval;
        private final MultiModule modules;
        private final MultiModule testModules;
        private String[] modulePath = new String[] {ProjectProperties.JAVAC_MODULEPATH};
        private String[] testModulePath = new String[] {ProjectProperties.JAVAC_TEST_MODULEPATH};
        private String[] javacClassPath = new String[] {ProjectProperties.JAVAC_CLASSPATH};
        private String[] testJavacClassPath = new String[] {ProjectProperties.JAVAC_TEST_CLASSPATH};
        private String[] executeModulePath = new String[] {ProjectProperties.RUN_MODULEPATH};
        private String[] testExecuteModulePath = new String[] {ProjectProperties.RUN_TEST_MODULEPATH};
        private String[] executeClassPath = new String[] {ProjectProperties.RUN_CLASSPATH};
        private String[] testExecuteClassPath = new String[] {ProjectProperties.RUN_TEST_CLASSPATH};
        private String[] processorModulePath = new String[] {ProjectProperties.JAVAC_PROCESSORMODULEPATH};
        private String[] testProcessorModulePath = new String[] {"javac.test.processormodulepath"};    //NOI18N
        private String[] processorClassPath = new String[] {ProjectProperties.JAVAC_PROCESSORPATH};
        private String[] testProcessorClassPath = new String[] {"javac.test.processorpath"};    //NOI18N
        private String platformType = CommonProjectUtils.J2SE_PLATFORM_TYPE;
        private String buildModulesDirProperty = ProjectProperties.BUILD_MODULES_DIR;

        private Builder(
                @NonNull final AntProjectHelper helper,
                @NonNull final PropertyEvaluator evaluator,
                @NonNull final MultiModule modules,
                @NonNull final MultiModule testModules) {
            Parameters.notNull("helper", helper);       //NOI18N
            Parameters.notNull("evaluator", evaluator);   //NOI18N
            Parameters.notNull("modules", modules); //NOI18N
            Parameters.notNull("testModules", testModules); //NOI18N
            this.helper = helper;
            this.eval = evaluator;
            this.modules = modules;
            this.testModules = testModules;
        }

        /**
         * Creates a new {@link Builder}.
         * @param helper the {@link AntProjectHelper}.
         * @param evaluator the {@link PropertyEvaluator}
         * @param sourceModules the {@link SourceRoots} describing the module-source-path
         * @param srcRoots the {@link SourceRoots} describing the java source roots
         * @param testModules the {@link SourceRoots} describing the module-source-path for tests
         * @param testRoots the {@link SourceRoots} describing the java source roots for tests
         * @return the {@link Builder} instance
         */
        @NonNull
        public static Builder newInstance(
                @NonNull final AntProjectHelper helper,
                @NonNull final PropertyEvaluator evaluator,
                @NonNull final SourceRoots sourceModules,
                @NonNull final SourceRoots srcRoots,
                @NonNull final SourceRoots testModules,
                @NonNull final SourceRoots testRoots) {
            return new Builder(
                    helper,
                    evaluator,
                    MultiModule.getOrCreate(sourceModules, srcRoots),
                    MultiModule.getOrCreate(testModules, testRoots));
        }

        /**
         * Sets module path properties.
         * @param modulepathProperties  the names of properties containing the module path, by default {@link ProjectProperties#JAVAC_MODULEPATH}
         * @return {@link Builder}
         */
        @NonNull
        public Builder setModulepathProperties(@NonNull final String[] modulepathProperties) {
            Parameters.notNull("modulePathProperties", modulepathProperties);
            this.modulePath = Arrays.copyOf(modulepathProperties, modulepathProperties.length);
            return this;
        }

        /**
         * Sets test module path properties.
         * @param modulepathProperties  the names of properties containing the test module path, by default {@link ProjectProperties#JAVAC_TEST_MODULEPATH}
         * @return {@link Builder}
         */
        @NonNull
        public Builder setTestModulepathProperties(@NonNull final String[] modulepathProperties) {
            Parameters.notNull("modulePathProperties", modulepathProperties);
            this.testModulePath = Arrays.copyOf(modulepathProperties, modulepathProperties.length);
            return this;
        }

        /**
         * Sets javac {@link ClassPath} properties for source roots.
         * @param classpathProperties the names of properties containing the compiler classpath for sources, by default {@link ProjectProperties#JAVAC_CLASSPATH}
         * @return {@link Builder}
         */
        @NonNull
        public Builder setJavacClasspathProperties(@NonNull final String[] classpathProperties) {
            Parameters.notNull("classPathProperties", classpathProperties);   //NOI18N
            this.javacClassPath = Arrays.copyOf(classpathProperties, classpathProperties.length);
            return this;
        }

        /**
         * Sets javac {@link ClassPath} properties for test roots.
         * @param classpathProperties  the names of properties containing the compiler classpath for tests, by default {@link ProjectProperties#JAVAC_TEST_CLASSPATH}
         * @return {@link Builder}
         */
        @NonNull
        public Builder setTestJavacClasspathProperties(@NonNull final String[] classpathProperties) {
            Parameters.notNull("classpathProperties", classpathProperties);   //NOI18N
            this.testJavacClassPath = Arrays.copyOf(classpathProperties, classpathProperties.length);
            return this;
        }

        /**
         * Sets runtime module path properties.
         * @param modulePathProperties the names of properties containing the runtime module path, by default {@link ProjectProperties#RUN_MODULEPATH}
         * @return {@link Builder}
         */
        @NonNull
        public Builder setRunModulepathProperties(@NonNull final String[] modulePathProperties) {
            Parameters.notNull("modulePathProperties", modulePathProperties);
            this.executeModulePath = Arrays.copyOf(modulePathProperties, modulePathProperties.length);
            return this;
        }

        /**
         * Sets test runtime module path properties.
         * @param modulePathProperties  the names of properties containing the test runtime module path, by default {@link ProjectProperties#RUN_TEST_MODULEPATH}
         * @return {@link Builder}
         */
        @NonNull
        public Builder setTestRunModulepathProperties(@NonNull final String[] modulePathProperties) {
            Parameters.notNull("modulePathProperties", modulePathProperties);
            this.testExecuteModulePath = Arrays.copyOf(modulePathProperties, modulePathProperties.length);
            return this;
        }

        /**
         * Sets runtime {@link ClassPath} properties.
         * @param runClasspathProperties the names of properties containing the runtime classpath for sources, by default {@link ProjectProperties#RUN_CLASSPATH}
         * @return {@link Builder}
         */
        @NonNull
        public Builder setRunClasspathProperties(@NonNull final String[] runClasspathProperties) {
            Parameters.notNull("runClasspathProperties", runClasspathProperties);   //NOI18N
            this.executeClassPath = Arrays.copyOf(runClasspathProperties, runClasspathProperties.length);
            return this;
        }

        /**
         * Sets runtime {@link ClassPath} properties.
         * @param runTestClasspathProperties  the names of properties containing the runtime classpath for tests, by default {@link ProjectProperties#RUN_TEST_CLASSPATH}
         * @return {@link Builder}
         */
        @NonNull
        public Builder setTestRunClasspathProperties(@NonNull final String[] runTestClasspathProperties) {
            Parameters.notNull("runTestClasspathProperties", runTestClasspathProperties);   //NOI18N
            this.testExecuteClassPath = Arrays.copyOf(runTestClasspathProperties, runTestClasspathProperties.length);
            return this;
        }

        /**
         * Sets javac processor module path properties.
         * @param processorModulepathProperties the names of properties containing the compiler processor module path for sources, by default {@link ProjectProperties#JAVAC_PROCESSORMODULEPATH}
         * @return {@link Builder}
         */
        @NonNull
        public Builder setProcessorModulepathProperties(@NonNull final String[] processorModulepathProperties) {
            Parameters.notNull("processorModulepathProperties", processorModulepathProperties);
            this.processorModulePath = Arrays.copyOf(processorModulepathProperties, processorModulepathProperties.length);
            return this;
        }

        /**
         * Sets javac processor module path properties for test roots.
         * @param processorModulepathProperties the names of properties containing the compiler processor module path for sources, by default {@link ProjectProperties#JAVAC_PROCESSORMODULEPATH}
         * @return {@link Builder}
         */
        @NonNull
        public Builder setTestProcessorModulepathProperties(@NonNull final String[] processorModulepathProperties) {
            Parameters.notNull("processorModulepathProperties", processorModulepathProperties);
            this.testProcessorModulePath = Arrays.copyOf(processorModulepathProperties, processorModulepathProperties.length);
            return this;
        }

        /**
         * Sets javac processor {@link ClassPath} properties.
         * @param processorClasspathProperties the names of properties containing the compiler processor path for sources, by default {@link ProjectProperties#JAVAC_PROCESSORPATH}
         * @return {@link Builder}
         */
        @NonNull
        public Builder setProcessorClasspathProperties(@NonNull final String[] processorClasspathProperties) {
            Parameters.notNull("processorClasspathProperties", processorClasspathProperties);
            this.processorClassPath = Arrays.copyOf(processorClasspathProperties, processorClasspathProperties.length);
            return this;
        }

        /**
         * Sets javac processor {@link ClassPath} properties for test roots.
         * @param processorClasspathProperties the names of properties containing the compiler processor path for sources, by default {@link ProjectProperties#JAVAC_PROCESSORPATH}
         * @return {@link Builder}
         */
        @NonNull
        public Builder setTestProcessorClasspathProperties(@NonNull final String[] processorClasspathProperties) {
            Parameters.notNull("processorClasspathProperties", processorClasspathProperties);
            this.testProcessorClassPath = Arrays.copyOf(processorClasspathProperties, processorClasspathProperties.length);
            return this;
        }

        /**
         * Sets a {@link org.netbeans.api.java.platform.JavaPlatform} type for boot {@link ClassPath} lookup.
         * @param platformType the type of {@link org.netbeans.api.java.platform.JavaPlatform}, by default "j2se"
         * @return {@link Builder}
         */
        @NonNull
        public Builder setPlatformType(@NonNull final String platformType) {
            Parameters.notNull("platformType", platformType);   //NOI18N
            this.platformType = platformType;
            return this;
        }

        /**
         * Sets a property name containing build modules directory.
         * @param buildModulesDirProperty  the name of property containing the build modules directory, by default {@link ProjectProperties#BUILD_MODULES_DIR}
         * @return {@link Builder}
         * @since 1.106
         */
        @NonNull
        public Builder setBuildModulesDirProperty(@NonNull final String buildModulesDirProperty) {
            Parameters.notNull("buildModulesDirProperty", buildModulesDirProperty);   //NOI18N
            this.buildModulesDirProperty = buildModulesDirProperty;
            return this;
        }

        /**
         * Creates a new {@link MultiModuleClassPathProvider}.
         * @return the {@link MultiModuleClassPathProvider} instance
         */
        @NonNull
        public MultiModuleClassPathProvider build() {
            return new MultiModuleClassPathProvider(
                    helper,
                    eval,
                    modules,
                    testModules,
                    modulePath,
                    testModulePath,
                    javacClassPath,
                    testJavacClassPath,
                    executeModulePath,
                    testExecuteModulePath,
                    executeClassPath,
                    testExecuteClassPath,
                    processorModulePath,
                    testProcessorModulePath,
                    processorClassPath,
                    testProcessorClassPath,
                    platformType,
                    buildModulesDirProperty);
        }
    }
}
