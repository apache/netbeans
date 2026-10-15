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

package org.netbeans.modules.gradle.java.classpath;

import org.netbeans.modules.gradle.api.NbGradleProject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FlaggedClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 *
 * @author Laszlo Kishalmi
 */
abstract class AbstractGradleClassPathImpl implements FlaggedClassPathImplementation {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final PropertyChangeListener listener;
    private List<URL> rawResources;
    private List<PathResourceImplementation> resources;

    protected final Project project;
    private final NbGradleProject watcher;

    protected AbstractGradleClassPathImpl(Project proj) {
        this.project = proj;
        watcher = proj.getLookup().lookup(NbGradleProject.class);
        listener = (PropertyChangeEvent evt) -> {
            if (watcher.isUnloadable()) {
                return;
            }
            List<URL> newValue = createPath();
            boolean hasChanged;
            synchronized (AbstractGradleClassPathImpl.this) {
                hasChanged = hasChanged(rawResources, newValue);
                if (hasChanged) {
                    rawResources = newValue;
                    resources = null;
                }
            }
            if (hasChanged) {
                support.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
            }
            support.firePropertyChange(FlaggedClassPathImplementation.PROP_FLAGS, null, null);
        };
        watcher.addPropertyChangeListener(WeakListeners.propertyChange(listener, watcher));
    }

    @Override
    public Set<ClassPath.Flag> getFlags() {
        if (watcher.isUnloadable()) {
            return Collections.singleton(ClassPath.Flag.INCOMPLETE);
        }
        return Collections.emptySet();
    }

    protected abstract List<URL> createPath();

    private boolean hasChanged(List<URL> oldValue, List<URL> newValue) {
        boolean ret = (oldValue == null) || (oldValue.size() != newValue.size());
        if (!ret) {
            assert oldValue != null;
            Iterator<URL> ol = oldValue.iterator();
            Iterator<URL> nl = newValue.iterator();
            while (!ret && ol.hasNext()) {
                ret = !ol.next().equals(nl.next());
            }
        }
        return ret;
    }

    @Override
    public final synchronized List<? extends PathResourceImplementation> getResources() {
        if (resources == null) {
            resources = createPath().stream().map(ClassPathSupport::createResource).toList();
        }
        return resources;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        synchronized (support) {
            support.addPropertyChangeListener(propertyChangeListener);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        synchronized (support) {
            support.removePropertyChangeListener(propertyChangeListener);
        }
    }

    final synchronized void clearResourceCache() {
        resources = null;
        support.firePropertyChange(ClassPathImplementation.PROP_RESOURCES, null, null);
    }

    static void addAllFile(Collection<URL> ret, @NonNull Collection<File> files) {
        assert files != null;
        for (File f : files) {
            URL u = FileUtil.urlForArchiveOrDir(f);
            if (u != null) {
                ret.add(u);
            }
        }
    }
}
