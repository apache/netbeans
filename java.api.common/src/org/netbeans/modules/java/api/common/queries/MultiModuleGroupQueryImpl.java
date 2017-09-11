/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2017 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2017 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.api.common.queries;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.api.common.Roots;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.impl.RootsAccessor;
import org.netbeans.modules.java.api.common.util.CommonModuleUtils;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 * Implementation of MultiModuleGroupQuery suitable for multi-module projects
 * @author sdedic
 */
class MultiModuleGroupQueryImpl implements MultiModuleGroupQuery, ChangeListener, PropertyChangeListener, AntProjectListener {
    private final Collection<Roots> roots;
    private final AntProjectHelper  helper;
    private final PropertyEvaluator evaluator;
    private final Sources           sources;
    private final AtomicInteger     version = new AtomicInteger(1);
    
    public MultiModuleGroupQueryImpl(AntProjectHelper helper, PropertyEvaluator evaluator, Sources src, Roots... roots) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.roots = Collections.unmodifiableList(Arrays.asList(roots));
        this.sources = src;
        for (Roots r : this.roots) {
            r.addPropertyChangeListener(WeakListeners.propertyChange(this, r));
        }
        helper.addAntProjectListener(WeakListeners.create(AntProjectListener.class, this, helper));
        evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        sources.addChangeListener(WeakListeners.change(this, sources));
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if (e.getSource() == sources) {
            flush();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (SourceRoots.PROP_ROOTS.equals(evt.getPropertyName())) {
            flush();
        }
    }

    @Override
    public SourceGroup[] filterModuleGroups(String modName, SourceGroup[] original) {
        Set<SourceGroup>  result = new LinkedHashSet<>(original.length);
        for (Roots r : roots) {
            if (!RootsAccessor.getInstance().isSourceRoot(r)) {
                continue;
            }
            String[] pathProps = RootsAccessor.getInstance().getRootPathProperties(r);
            String[] propNames = r.getRootProperties();
            for (int i = 0; i < propNames.length; i++) {
                final String prop = propNames[i];
                final String pathProp = pathProps[i];
                final String type = RootsAccessor.getInstance().getType(r);

                if (pathProp == null || JavaProjectConstants.SOURCES_TYPE_MODULES.equals(type)) {  //NOI18N
                    continue;
                }
                final String pathToModules = evaluator.getProperty(prop);
                final File file = helper.resolveFile(pathToModules);
                
                final Collection<? extends String> spVariants = Arrays.stream(PropertyUtils.tokenizePath(evaluator.getProperty(pathProp)))
                        .map((p) -> CommonModuleUtils.parseSourcePathVariants(p))
                        .flatMap((lv) -> lv.stream())
                        .collect(Collectors.toList());
                for (String variant : spVariants) {
                    final String pathInModule = variant;
                    if (!file.exists()) {
                        continue;
                    }
                    final File modDir = new File(new File(file, modName), pathInModule);
                    if (!file.exists() || !file.isDirectory()) {
                        continue;
                    }
                    FileObject modDirFo = FileUtil.toFileObject(modDir);
                    if (modDirFo == null || !modDirFo.isValid()) {
                        continue;
                    }
                    for (SourceGroup g : original) {
                        if (g.getRootFolder().equals(modDirFo)) {
                            result.add(g);
                        }
                    }
                }
            }
        }
        return result.toArray(new SourceGroup[result.size()]);
    }

    @Override
    public void configurationXmlChanged(AntProjectEvent ev) {
        flush();
    }

    @Override
    public void propertiesChanged(AntProjectEvent ev) {}
    
    private final Map<SourceGroup, MultiModuleGroupQuery.Result>    cachedModuleInfo = new WeakHashMap<>();
    private final Set<String> watchedProperties = new HashSet<>();
    
    private static final MultiModuleGroupQuery.Result NONE = new MultiModuleGroupQuery.Result(null, null);
    
    private void flush() {
        synchronized (this) {
            cachedModuleInfo.clear();
            watchedProperties.clear();
            version.incrementAndGet();
        }
    }
    
    public MultiModuleGroupQuery.Result findModuleInfo(SourceGroup grp) {
        MultiModuleGroupQuery.Result res;
        
        synchronized (this) {
            res = cachedModuleInfo.get(grp);
            if (res != null) {
                return res == NONE ? null : res;
            }
        }
        int ver = version.get();
        Set<String> props = new HashSet<>();
        Set<FileObject> newmodRoots = new HashSet<>();
        for (Roots r : roots) {
            if (!RootsAccessor.getInstance().isSourceRoot(r)) {
                continue;
            }
            FileObject groot = grp.getRootFolder();
            File rootFile = FileUtil.toFile(groot);
            Path rootPath = rootFile.toPath();
            String[] propNames = r.getRootProperties();
            String[] rootPathPropNames = RootsAccessor.getInstance().getRootPathProperties(r);
            for (int i = 0; i < propNames.length; i++) {
                final String prop = propNames[i];
                final String pathProp = rootPathPropNames[i];
                final String type = RootsAccessor.getInstance().getType(r);

                if (pathProp == null || JavaProjectConstants.SOURCES_TYPE_MODULES.equals(type)) {  //NOI18N
                    continue;
                }
                final String pathToModules = evaluator.getProperty(prop);
                final String loc = evaluator.getProperty(pathProp);
                final File file = helper.resolveFile(pathToModules);
                props.add(prop);
                final Collection<? extends String> spVariants = Arrays.stream(PropertyUtils.tokenizePath(loc))
                        .map((p) -> CommonModuleUtils.parseSourcePathVariants(p))
                        .flatMap((lv) -> lv.stream())
                        .collect(Collectors.toList());
                for (File f : file.listFiles()) {
                    if (!f.isDirectory()) {
                        continue;
                    }
                    for (String variant : spVariants) {
                        final String pathInModule = variant;
                        FileObject rfo = FileUtil.toFileObject(file);
                        if (rfo != null) {
                            newmodRoots.add(rfo);
                        }
                        Path fPath = file.toPath();
                        if (rootPath.startsWith(fPath)) {
                            Path rel = fPath.relativize(rootPath);
                            Path intra = Paths.get(pathInModule);
                            String modName = rel.getName(0).toString();
                            if (!pathInModule.isEmpty()) {
                                if (rel.getNameCount() == 1 ||
                                    !rel.subpath(1, rel.getNameCount()).equals(intra)) {
                                    continue;
                                }
                            }
                            MultiModuleGroupQuery.Result fres = new MultiModuleGroupQuery.Result(modName, pathInModule);
                            synchronized (this) {
                                if (ver == version.get()) {
                                    return cachedModuleInfo.computeIfAbsent(grp, (g) -> fres);
                                } else {
                                    return fres;
                                }
                            }
                        }
                    }
                }
            }
        }
        synchronized (this) {
            if (ver == version.get() && !cachedModuleInfo.containsKey(grp)) {
                cachedModuleInfo.put(grp, NONE);
                watchedProperties.addAll(props);
            }
        }
        return null;
    }
}
