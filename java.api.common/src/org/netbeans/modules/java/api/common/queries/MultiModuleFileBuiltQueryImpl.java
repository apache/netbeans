/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
            res = helper.createGlobFileBuiltQuery(
                    eval,
                    from.toArray(new String[from.size()]),
                    to.toArray(new String[to.size()]));
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
