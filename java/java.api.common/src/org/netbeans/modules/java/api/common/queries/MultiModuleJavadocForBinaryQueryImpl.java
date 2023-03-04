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
package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.ChangeSupport;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
final class MultiModuleJavadocForBinaryQueryImpl implements JavadocForBinaryQueryImplementation, PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger(MultiModuleJavadocForBinaryQueryImpl.class.getName());
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final MultiModule modules;
    private final String[] binaryProperties;
    private final String javadocProperty;
    private final ConcurrentMap<URI,R> cache;

    public MultiModuleJavadocForBinaryQueryImpl(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final MultiModule modules,
            @NonNull final String[] binaryProperties,
            @NonNull final String javadocProperty) {
        Parameters.notNull("helper", helper);                       //NOI18N
        Parameters.notNull("evaluator", evaluator);                 //NOI18N
        Parameters.notNull("modules", modules);                     //NOI18N
        Parameters.notNull("binaryProperties", binaryProperties);   //NOI18N
        Parameters.notNull("javadocProperty", javadocProperty);     //NOI18N
        this.helper = helper;
        this.evaluator = evaluator;
        this.modules = modules;
        this.binaryProperties = binaryProperties;
        this.javadocProperty = javadocProperty;
        this.cache = new ConcurrentHashMap<>();
        this.evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, this.evaluator));
        this.modules.addPropertyChangeListener(WeakListeners.propertyChange(this, this.modules));
    }

    @Override
    public JavadocForBinaryQuery.Result findJavadoc(URL binaryRoot) {
        boolean archive = false;
        if (FileUtil.isArchiveArtifact(binaryRoot)) {
            binaryRoot = FileUtil.getArchiveFile(binaryRoot);
            archive = true;
        }
        R res = null;
        try {
            URI artefact = binaryRoot.toURI();
            res = cache.get(artefact);
            if (res == null) {
                res = createResult(artefact, archive, modules, binaryProperties);
                R prev = cache.get(artefact);
                if (prev != null) {
                    res = prev;
                } else if (res != null) {
                    prev = cache.putIfAbsent(artefact, res);
                    if (prev != null) {
                        res = prev;
                    }
                }
            }
        } catch (URISyntaxException e) {
            LOG.log(
                    Level.WARNING,
                    "Invalid URI: {0}", //NOI18N
                    binaryRoot.toExternalForm());
        }
        return res;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final Object source = evt.getSource();
        final String propName = evt.getPropertyName();
        if (source == modules) {
            final Collection<? extends String> moduleNames = this.modules.getModuleNames();
            for (Iterator<Map.Entry<URI,R>> it= cache.entrySet().iterator(); it.hasNext();) {
                final R r = it.next().getValue();
                if (!moduleNames.contains(r.getModuleName())) {
                    it.remove();
                }
            }
        } else if (contains(propName, binaryProperties)) {
            for (Iterator<Map.Entry<URI,R>> it= cache.entrySet().iterator(); it.hasNext();) {
                final Map.Entry<URI,R> e = it.next();
                final URI uri = e.getKey();
                final R r = e.getValue();
                if (propName.equals(r.getProperty()) && getOwner(evaluator, helper, uri, new String[]{propName}) == null) {
                    it.remove();
                }
            }
        }
    }

    @CheckForNull
    private R createResult(
            @NonNull final URI artefact,
            final boolean archive,
            @NonNull final MultiModule modules,
            @NonNull final String... properties) {
        final String prop = getOwner(evaluator, helper, artefact, properties);
        if (prop != null) {
            final String moduleName = getModuleName(artefact, archive);
            if (moduleName != null && modules.getModuleNames().contains(moduleName)) {
                return new R(
                        artefact,
                        javadocProperty,
                        modules,
                        evaluator,
                        helper,
                        moduleName,
                        prop);
            }
        }
        return null;
    }

    private static boolean contains(
            @NonNull final String prop,
            @NonNull final String... props) {
        for (String p : props) {
            if (p.equals(prop)) {
                return true;
            }
        }
        return false;
    }

    @CheckForNull
    private static String getOwner(
            @NonNull final PropertyEvaluator eval,
            @NonNull final AntProjectHelper helper,
            @NonNull final URI artefact,
            @NonNull final String[] properties) {
        return Arrays.stream(properties)
                .map((prop) -> {
                    final String val = eval.getProperty(prop);
                    return val == null ? null : Pair.of(prop,val);
                })
                .filter((propPathPair) -> propPathPair != null)
                .map((propPathPair) -> {
                    try {
                        final File f = helper.resolveFile(propPathPair.second());
                        URI uri = BaseUtilities.toURI(f);
                        final String suri = uri.toString();
                        if (!suri.endsWith("/")) {      //NOI18N
                            uri = new URI(suri+'/');    //NOI18N
                        }
                        return Pair.of(propPathPair.first(),uri);
                    } catch (URISyntaxException e) {
                        return null;
                    }
                })
                .filter((propFolderURIPair) -> propFolderURIPair != null && artefact.toString().startsWith(propFolderURIPair.second().toString()))
                .map((propFolderURIPair) -> propFolderURIPair.first())
                .findAny()
                .orElse(null);
    }

    @CheckForNull
    private static String getModuleName(
            @NonNull final URI artefact,
            final boolean archive) {
        final Path p = Paths.get(artefact);
        if (p == null) {
            return null;
        }
        final String nameExt = p.getFileName().toString();
        final int dot = nameExt.lastIndexOf('.');   //NOI18N
        if (dot < 0 || !archive) {
            return nameExt;
        } else if (dot == 0) {
            return null;
        } else {
            return nameExt.substring(0, dot);
        }
    }

    private static final class R implements JavadocForBinaryQuery.Result, PropertyChangeListener {
        private static final URL[] EMPTY = new URL[0];
        private final URI artefact;
        private final String jdocProperty;
        private final MultiModule modules;
        private final PropertyEvaluator evaluator;
        private final AntProjectHelper helper;
        private final String moduleName;
        private final String prop;
        private final ChangeSupport listeners;
        private final AtomicReference<URL[]> cache;
        private final AtomicBoolean currentModuleExists;

        R(
                @NonNull final URI artefact,
                @NonNull final String jdocProperty,
                @NonNull final MultiModule modules,
                @NonNull final PropertyEvaluator evaluator,
                @NonNull final AntProjectHelper helper,
                @NonNull final String moduleName,
                @NonNull final String prop) {
            Parameters.notNull("artefact", artefact);               //NOI18N
            Parameters.notNull("jdocProperty", jdocProperty);       //NOI18N
            Parameters.notNull("modules", modules);                 //NOI18N
            Parameters.notNull("evaluator", evaluator);             //NOI18N
            Parameters.notNull("helper", helper);                   //NOI18N
            Parameters.notNull("moduleName", moduleName);           //NOI18N
            Parameters.notNull("prop", prop);                       //NOI18N
            this.artefact = artefact;
            this.jdocProperty = jdocProperty;
            this.modules = modules;
            this.evaluator = evaluator;
            this.helper = helper;
            this.moduleName = moduleName;
            this.prop = prop;
            this.listeners = new ChangeSupport(this);
            this.cache = new AtomicReference<>();
            this.currentModuleExists = new AtomicBoolean(true);
            this.modules.addPropertyChangeListener(WeakListeners.propertyChange(this, this.modules));
            this.evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, this.evaluator));
        }

        @Override
        public URL[] getRoots() {
            URL[] res = cache.get();
            if (res == null) {
                final boolean exists = moduleExists();
                res = Optional.ofNullable(exists ? evaluator.getProperty(jdocProperty) : null)
                        .filter((path) -> getOwner(evaluator, helper, artefact, new String[]{prop}) != null)
                        .map((path) -> FileUtil.urlForArchiveOrDir(helper.resolveFile(path)))
                        .map((url) -> new URL[]{url})
                        .orElse(EMPTY);
                if (!cache.compareAndSet(null, res)) {
                    URL[] tmp = cache.get();
                    res = tmp != null ?
                            tmp :
                            res;
                } else {
                    currentModuleExists.set(exists);
                }
            }
            return Arrays.copyOf(res, res.length);
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener l) {
            this.listeners.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener l) {
            this.listeners.removeChangeListener(l);
        }

        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent evt) {
            final Object source = evt.getSource();
            final String propName = evt.getPropertyName();
            if (source == modules) {
                final boolean exists = moduleExists();
                final boolean fire = currentModuleExists.get() ^ exists;
                currentModuleExists.set(exists);
                 if (fire) {
                    cache.set(null);
                    listeners.fireChange();
                 }
            } else if (prop.equals(propName) || jdocProperty.equals(propName) || propName == null) {
                cache.set(null);
                listeners.fireChange();
            }
        }

        String getModuleName() {
            return moduleName;
        }

        String getProperty() {
            return prop;
        }

        private boolean moduleExists() {
            return modules.getModuleNames().contains(moduleName);
        }
    }
}
