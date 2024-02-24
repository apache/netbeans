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
package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.queries.FileBuiltQuery;
import org.netbeans.modules.java.api.common.impl.MultiModule;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.queries.FileBuiltQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.BaseUtilities;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
final class MultiModuleFileBuiltQueryImpl implements FileBuiltQueryImplementation, PropertyChangeListener {
    private static final Logger LOG = Logger.getLogger(MultiModuleFileBuiltQueryImpl.class.getName());

    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final MultiModule sourceModules;
    private final MultiModule testModules;
    //@GuardedBy("this")
    private FileBuiltQueryImplementation delegate;
    private final Collection<Pair<ClassPath,PropertyChangeListener>> currentPaths;

    MultiModuleFileBuiltQueryImpl(
            @NonNull final AntProjectHelper helper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final MultiModule sourceModules,
            @NonNull final MultiModule testModules) {
        Parameters.notNull("helper", helper);           //NOI18N
        Parameters.notNull("evaluator", evaluator);     //NOI18N
        Parameters.notNull("sourceModules", sourceModules); //NOI18N
        Parameters.notNull("testModules", testModules);
        this.helper = helper;
        this.eval = evaluator;
        this.sourceModules = sourceModules;
        this.testModules = testModules;
        this.currentPaths = new ArrayList<>();
        this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
        this.sourceModules.addPropertyChangeListener(WeakListeners.propertyChange(this, this.sourceModules));
        this.testModules.addPropertyChangeListener(WeakListeners.propertyChange(this, this.testModules));
    }

    @Override
    public FileBuiltQuery.Status getStatus(FileObject file) {
        return ProjectManager.mutex().readAccess(() -> {
                return getDelegate().getStatus(file);
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final String propName = evt.getPropertyName();
        if (propName == null) {
            invalidate();
        } else {
            switch(propName) {
                case ProjectProperties.BUILD_MODULES_DIR:
                case ProjectProperties.BUILD_TEST_MODULES_DIR:
                    if (evt.getSource() == this.eval) {
                        invalidate();
                    }
                    break;
                case MultiModule.PROP_MODULES:
                    if (evt.getSource() == this.sourceModules ||
                        evt.getSource() == this.testModules) {
                        invalidate();
                    }
                    break;
                case ClassPath.PROP_ENTRIES:
                    final Object src = evt.getSource();
                    if ((src instanceof ClassPath) && isCurrentPath((ClassPath)src)) {
                        invalidate();
                    }
            }
        }
    }

    @NonNull
    private FileBuiltQueryImplementation getDelegate() {
        FileBuiltQueryImplementation res;
        synchronized (this) {
             res = delegate;
        }
        if (res == null) {
            final List<String> from = new ArrayList<>();
            final List<String> to = new ArrayList<>();
            final Set<ClassPath> classpaths = Collections.newSetFromMap(new IdentityHashMap<>());
            collectRoots(sourceModules, ProjectProperties.BUILD_MODULES_DIR, from, to, classpaths);
            collectRoots(testModules, ProjectProperties.BUILD_TEST_MODULES_DIR, from, to, classpaths);
            res = helper.createGlobFileBuiltQuery(eval,
                    from.toArray(new String[0]),
                    to.toArray(new String[0]));
            synchronized (this) {
                if (delegate == null) {
                    for (Pair<ClassPath,PropertyChangeListener> cplp : currentPaths) {
                        cplp.first().removePropertyChangeListener(cplp.second());
                    }
                    currentPaths.clear();
                    for (ClassPath scp : classpaths) {
                        final PropertyChangeListener l = WeakListeners.propertyChange(this, scp);
                        scp.addPropertyChangeListener(l);
                        currentPaths.add(Pair.of(scp, l));
                    }
                    delegate = res;
                } else {
                    res = delegate;
                }
            }
        }
        return res;
    }

    private synchronized boolean isCurrentPath(@NonNull final ClassPath cp) {
        for (Pair<ClassPath,PropertyChangeListener> p : currentPaths) {
            if (p.first() == cp) {
                return true;
            }
        }
        return false;
    }

    private synchronized void invalidate() {
        delegate = null;
    }

    private static void collectRoots (
            @NonNull final MultiModule modules,
            @NonNull final String buildDirProp,
            @NonNull final List<? super String> from,
            @NonNull final List<? super String> to,
            @NonNull final Set<? super ClassPath> cps) {
        for (String moduleName : modules.getModuleNames()) {
            final String dest = String.format(
                    "${%s}/%s/*.class", //NOI18N
                    buildDirProp,
                    moduleName);
            final ClassPath scp = modules.getModuleSources(moduleName);
            if (scp != null) {
                for (ClassPath.Entry e : scp.entries()) {
                    try {
                        final File f = BaseUtilities.toFile(e.getURL().toURI());
                        String source = String.format(
                                "%s/*.java",       //NOI18N
                                f.getAbsolutePath());
                        from.add(source);
                        to.add(dest);
                    } catch (IllegalArgumentException | URISyntaxException exc) {
                        LOG.log(
                                Level.WARNING,
                                "Cannot convert source root: {0} to file.", //NOI18N
                                e.getURL());
                    }
                }
                cps.add(scp);
            }
        }
    }
}
