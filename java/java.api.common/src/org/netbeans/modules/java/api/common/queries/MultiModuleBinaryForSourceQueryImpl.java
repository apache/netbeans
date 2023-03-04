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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
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
final class MultiModuleBinaryForSourceQueryImpl implements BinaryForSourceQueryImplementation, PropertyChangeListener {
    private static final Logger LOG = Logger.getLogger(MultiModuleBinaryForSourceQueryImpl.class.getName());
    private static final URL[] EMPTY = new URL[0];

    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final MultiModule modules;
    private final MultiModule testModules;
    private final String[] binaryTemplates;
    private final String[] testBinaryTemplates;
    private final ConcurrentMap<URI,R> cache;

    MultiModuleBinaryForSourceQueryImpl(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final MultiModule modules,
            @NonNull final MultiModule testModules,
            @NonNull final String[] binaryTemplates,
            @NonNull final String[] testBinaryTemplates) {
        Parameters.notNull("helper", helper);   //NOI18N
        Parameters.notNull("evaluator", evaluator); //NOI18N
        Parameters.notNull("modules", modules); //NOI18N
        Parameters.notNull("testModules", testModules); //NOI18N
        Parameters.notNull("binaryTemplates", binaryTemplates); //NOI18N
        Parameters.notNull("testBinaryTemplates", testBinaryTemplates); //NOI18N
        this.helper = helper;
        this.evaluator = evaluator;
        this.modules = modules;
        this.testModules = testModules;
        this.binaryTemplates = binaryTemplates;
        this.testBinaryTemplates = testBinaryTemplates;
        this.cache = new ConcurrentHashMap<>();
        this.modules.addPropertyChangeListener(WeakListeners.propertyChange(this, this.modules));
        this.testModules.addPropertyChangeListener(WeakListeners.propertyChange(this, this.testModules));
    }

    @Override
    public BinaryForSourceQuery.Result findBinaryRoots(URL sourceRoot) {
        R res = null;
        try {
            final URI sourceRootURI = sourceRoot.toURI();
            res = cache.get(sourceRootURI);
            if (res == null) {
                res = createResult(sourceRoot, modules, binaryTemplates);
                if (res == null) {
                    res = createResult(sourceRoot, testModules, testBinaryTemplates);
                }
                R prev = cache.get(sourceRootURI);
                if (prev != null) {
                    res = prev;
                } else if (res != null) {
                    prev = cache.putIfAbsent(sourceRootURI, res);
                    if (prev != null) {
                        res = prev;
                    }
                }
            }
        } catch (URISyntaxException e) {
            LOG.log(
                    Level.WARNING,
                    "Invalid URI: {0}", //NOI18N
                    sourceRoot.toExternalForm());
        }
        return res;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final Object source = evt.getSource();
        if (source == this.modules) {
            for (Iterator<Map.Entry<URI,R>> it = cache.entrySet().iterator(); it.hasNext();) {
                final Map.Entry<URI,R> e = it.next();
                final R r = e.getValue();
                if (!r.isValid(this.modules)) {
                    it.remove();
                }
            }
        } else if (source == this.testModules) {
            for (Iterator<Map.Entry<URI,R>> it = cache.entrySet().iterator(); it.hasNext();) {
                final Map.Entry<URI,R> e = it.next();
                final R r = e.getValue();
                if (!r.isValid(this.testModules)) {
                    it.remove();
                }
            }
        }
    }

    @CheckForNull
    private R createResult(
            @NonNull final URL artifact,
            @NonNull final MultiModule modules,
            @NonNull final String[] templates) {
        for (String moduleName : modules.getModuleNames()) {
            final ClassPath scp = modules.getModuleSources(moduleName);
            if (scp != null) {
                for (ClassPath.Entry e : scp.entries()) {
                    if (artifact.equals(e.getURL())) {
                        return new R(
                                artifact,
                                modules,
                                moduleName,
                                templates);
                    }
                }
            }
        }
        return null;
    }

    private final class R implements BinaryForSourceQuery.Result, PropertyChangeListener {
        private final URL url;
        private final EvaluatorPropertyProvider pp;
        private final PropertyEvaluator evaluator;
        private final MultiModule modules;
        private final String moduleName;
        private final String[] templates;
        private final AtomicReference<URL[]> cache;
        private final ChangeSupport listeners;
        private final AtomicReference<Set<String>> propsCache;
        private final AtomicReference<Pair<ClassPath,PropertyChangeListener>> scp;

        R(
                @NonNull final URL url,
                @NonNull final MultiModule modules,
                @NonNull final String moduleName,
                @NonNull final String[] templates) {
            Parameters.notNull("url", url);             //NOI18N
            Parameters.notNull("modules", modules); //NOI18N
            Parameters.notNull("moduleName", moduleName);   //NOI18N
            Parameters.notNull("templates", templates);         //NOI18N
            this.url = url;
            this.pp = new EvaluatorPropertyProvider(MultiModuleBinaryForSourceQueryImpl.this.evaluator);
            this.evaluator = PropertyUtils.sequentialPropertyEvaluator(
                PropertyUtils.fixedPropertyProvider(Collections.singletonMap("module.name",moduleName)), //NOI18N
                pp);
            this.modules = modules;
            this.moduleName = moduleName;
            this.templates = templates;
            this.cache = new AtomicReference();
            this.listeners = new ChangeSupport(this);
            this.propsCache = new AtomicReference<>();
            this.scp = new AtomicReference<>();
            MultiModuleBinaryForSourceQueryImpl.this.evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, MultiModuleBinaryForSourceQueryImpl.this.evaluator));
            this.modules.addPropertyChangeListener(WeakListeners.propertyChange(this, this.modules));
        }

        @Override
        public URL[] getRoots() {
            URL[] res = cache.get();
            if (res == null) {
                try {
                    if (isSourceRoot()) {
                        Pair<ClassPath,PropertyChangeListener> cplp = scp.get();
                        if (cplp == null) {
                            final ClassPath cp = modules.getModuleSources(moduleName);
                            final PropertyChangeListener l = WeakListeners.propertyChange(this, cp);
                            cplp = Pair.of(cp,l);
                            if (scp.compareAndSet(null, cplp)) {
                                cp.addPropertyChangeListener(l);
                            }
                        }
                        res = new URL[templates.length];
                        for (int i=0; i<templates.length; i++) {
                            final File f = MultiModuleBinaryForSourceQueryImpl.this.helper.resolveFile(evaluator.evaluate(templates[i]));
                            URL u = BaseUtilities.toURI(f).toURL();
                            //The FileUtil.urlForArchiveOrDir does not work for qualified module name in build folder
                            //custom implementation
                            final String su = u.toExternalForm();
                            if (su.length() > 4 && ".jar".equals(su.substring(su.length()-4).toLowerCase(Locale.ENGLISH))) { //NOI18N
                                u = FileUtil.getArchiveRoot(u);
                            } else if (!su.endsWith("/")) { //NOI18N
                                u = new URL(su+'/');        //NOI18N
                            }
                            res[i] = u;
                        }
                    } else {
                        res = EMPTY;
                    }
                } catch (MalformedURLException e) {
                    res = new URL[0];
                }
                cache.set(res);
            }
            return Arrays.copyOf(res, res.length);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            this.listeners.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            this.listeners.removeChangeListener(l);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final Object source = evt.getSource();
            final String propName = evt.getPropertyName();
            if (source == this.modules) {
                if (!isSourceRoot()) {
                    cache.set(null);
                    Pair<ClassPath,PropertyChangeListener> cplp = scp.get();
                    if(cplp != null && scp.compareAndSet(cplp, null)) {
                        cplp.first().removePropertyChangeListener(cplp.second());
                    }
                    this.listeners.fireChange();
                }
            } else if (source == Optional.ofNullable(scp.get()).map((p)->p.first()).orElse(null) && ClassPath.PROP_ENTRIES.equals(propName)) {
                if (!isSourceRoot()) {
                    cache.set(null);
                    MultiModuleBinaryForSourceQueryImpl.this.propertyChange(new PropertyChangeEvent(this.modules, ClassPath.PROP_ENTRIES, null, null));
                    this.listeners.fireChange();
                }
            } else if (propName == null || getImportantProperties().contains(propName)) {
                pp.update();
                cache.set(null);
                this.listeners.fireChange();
            }
        }

        boolean isValid(@NonNull final MultiModule model) {
            if (model != this.modules) {
                return true;
            }
            return isSourceRoot();
        }

        private boolean isSourceRoot() {
            final ClassPath cp = modules.getModuleSources(moduleName);
            if (cp == null) {
                return false;
            }
            for (ClassPath.Entry e : cp.entries()) {
                if (e.getURL().equals(url)) {
                    return true;
                }
            }
            return false;
        }

        @NonNull
        private Set<? extends String> getImportantProperties() {
            Set<String> res = propsCache.get();
            if (res == null) {
                res = new HashSet<>();
                for (String template : templates) {
                    int propStart = -2;
                    for (int i=0; i<template.length(); i++) {
                        final char c = template.charAt(i);
                        switch (propStart) {
                            case -2:
                                if (c == '$') { //NOI18N
                                    propStart = -1;
                                }
                                break;
                            case -1:
                                switch (c) {
                                    case '{':   //NOI18N
                                        propStart = i+1;
                                        break;
                                    case '$':   //NOI18N
                                        propStart = -1;
                                        break;
                                    default:
                                        propStart = -2;
                                        break;
                                }
                                break;
                            default:
                                if (c == '}') { //NOI18N
                                    final String propName = template.substring(propStart, i);
                                    res.add(propName);
                                    propStart = -2;
                                }
                        }
                    }
                }
                propsCache.set(res);
            }
            return res;
        }
    }
}
