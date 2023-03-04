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
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
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
final class MultiModuleSourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation2, PropertyChangeListener {
    private static final Logger LOG = Logger.getLogger(MultiModuleSourceForBinaryQueryImpl.class.getName());
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final MultiModule modules;
    private final MultiModule testModules;
    private final String[] binaryProperties;
    private final String[] testBinaryProperties;
    private final Map<URI,R> cache;

    MultiModuleSourceForBinaryQueryImpl(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator eval,
            @NonNull MultiModule modules,
            @NonNull MultiModule testModules,
            @NonNull final String[] binaryProperties,
            @NonNull final String[] testBinaryProperties) {
        Parameters.notNull("helper", helper);       //NOI18N
        Parameters.notNull("eval", eval);           //NOI18N
        Parameters.notNull("modules", modules);     //NOI18N
        Parameters.notNull("testModules", testModules); //NOI18N
        Parameters.notNull("binaryProperties", binaryProperties);       //NOI18N
        Parameters.notNull("testBinaryProperties", testBinaryProperties);   //NOI18N
        this.helper = helper;
        this.eval = eval;
        this.modules = modules;
        this.testModules = testModules;
        this.binaryProperties = binaryProperties;
        this.testBinaryProperties = testBinaryProperties;
        this.cache = new ConcurrentHashMap<>();
        this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
        this.modules.addPropertyChangeListener(WeakListeners.propertyChange(this, this.modules));
        this.testModules.addPropertyChangeListener(WeakListeners.propertyChange(this, this.testModules));
    }

    @Override
    public Result findSourceRoots2(@NonNull URL binaryRoot) {
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
                if (res == null) {
                    res = createResult(artefact, archive, testModules, testBinaryProperties);
                }
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
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        return findSourceRoots2(binaryRoot);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final Object source = evt.getSource();
        final String propName = evt.getPropertyName();
        if (source == this.modules) {
            final Collection<? extends String> moduleNames = this.modules.getModuleNames();
            for (Iterator<Map.Entry<URI,R>> it= cache.entrySet().iterator(); it.hasNext();) {
                final R r = it.next().getValue();
                if (contains(r.getProperty(), binaryProperties) && !moduleNames.contains(r.getModuleName())) {
                    it.remove();
                }
            }
        } else if (source == this.testModules) {
            final Collection<? extends String> moduleNames = this.testModules.getModuleNames();
            for (Iterator<Map.Entry<URI,R>> it= cache.entrySet().iterator(); it.hasNext();) {
                final R r = it.next().getValue();
                if (contains(r.getProperty(), testBinaryProperties) && !moduleNames.contains(r.getModuleName())) {
                    it.remove();
                }
            }
        } else if (contains(propName, binaryProperties) || contains(propName, testBinaryProperties)) {
            for (Iterator<Map.Entry<URI,R>> it= cache.entrySet().iterator(); it.hasNext();) {
                final Map.Entry<URI,R> e = it.next();
                final URI uri = e.getKey();
                final R r = e.getValue();
                if (propName.equals(r.getProperty()) && getOwner(eval, helper, uri, new String[]{propName}) == null) {
                    it.remove();
                }
            }
        }
    }

    private static boolean contains(
            @NullAllowed final String prop,
            @NonNull final String[] props) {
        for (String p : props) {
            if (p.equals(prop)) {
                return true;
            }
        }
        return false;
    }

    @CheckForNull
    private R createResult(
            @NonNull final URI artefact,
            final boolean archive,
            @NonNull final MultiModule modules,
            @NonNull final String... properties) {
        final String prop = getOwner(eval, helper, artefact, properties);
        if (prop != null) {
            final String moduleName = getModuleName(artefact, archive);
            if (moduleName != null) {
                final ClassPath cp = modules.getModuleSources(moduleName);
                if (cp != null) {
                    return new R(artefact, cp, eval, helper, moduleName, prop);
                }
            }
        }
        return null;
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
            @NonNull final URI uri,
            final boolean archive) {
        final Path p = Paths.get(uri);
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

    private static final class R implements Result, PropertyChangeListener {
        private static final FileObject[] EMPTY = new FileObject[0];
        private final URI artefact;
        private final ClassPath srcPath;
        private final PropertyEvaluator eval;
        private final AntProjectHelper helper;
        private final String moduleName;
        private final String prop;
        private final ChangeSupport listeners;
        private volatile int state;    //0 - Valid, 1 - Modified, 2 - Invalid

        R(
                @NonNull final URI artefact,
                @NonNull final ClassPath srcPath,
                @NonNull final PropertyEvaluator eval,
                @NonNull final AntProjectHelper helper,
                @NonNull final String moduleName,
                @NonNull final String prop) {
            Parameters.notNull("artefact", artefact);   //NOI18N
            Parameters.notNull("srcPath", srcPath); //NOI18N
            Parameters.notNull("eval", eval);       //NOI18N
            Parameters.notNull("helper", helper);       //NOI18N
            Parameters.notNull("moduleName", moduleName);   //NOI18N
            Parameters.notNull("prop", prop);       //NOI18N
            this.artefact = artefact;
            this.srcPath = srcPath;
            this.eval = eval;
            this.helper = helper;
            this.moduleName = moduleName;
            this.prop = prop;
            this.listeners = new ChangeSupport(this);
            this.srcPath.addPropertyChangeListener(WeakListeners.propertyChange(this, this.srcPath));
            this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
        }

        @Override
        public boolean preferSources() {
            return true;
        }

        @Override
        public FileObject[] getRoots() {
            int st = state;
            if (st == 1) {
                st = state = getOwner(
                        eval,
                        helper,
                        artefact,
                        new String[]{prop}) != null ? 0 : 2;
            }
            return st == 0 ?
                    srcPath.getRoots() :
                    EMPTY;
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener l) {
            listeners.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener l) {
            listeners.removeChangeListener(l);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (ClassPath.PROP_ROOTS.equals(propName)) {
                listeners.fireChange();
            } else if (prop.equals(propName) || propName == null) {
                state = 1;
                listeners.fireChange();
            }
        }

        String getProperty() {
            return prop;
        }

        String getModuleName() {
            return moduleName;
        }
    }

}
