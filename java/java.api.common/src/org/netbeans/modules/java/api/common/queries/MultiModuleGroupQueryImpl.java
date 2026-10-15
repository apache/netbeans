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
        return result.toArray(new SourceGroup[0]);
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
