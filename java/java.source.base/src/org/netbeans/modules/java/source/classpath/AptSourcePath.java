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
package org.netbeans.modules.java.source.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.*;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.AnnotationProcessingQuery;
import org.netbeans.modules.java.source.indexing.APTUtils;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
public final class AptSourcePath implements ClassPathImplementation, PropertyChangeListener {

    private final ClassPath delegate;
    private final Function<List<ClassPath.Entry>,List<PathResourceImplementation>> fnc;
    private final PropertyChangeSupport pcSupport;
    //@GuardedBy("this")
    private List<PathResourceImplementation> resources;
    //@GuardedBy("this")
    private long eventId;

    @SuppressWarnings("LeakingThisInConstructor")
    private AptSourcePath(
            @NonNull final ClassPath delegate,
            @NonNull final Function<List<ClassPath.Entry>,List<PathResourceImplementation>> fnc) {
        assert delegate != null;
        this.delegate = delegate;
        this.fnc = fnc;
        this.pcSupport = new PropertyChangeSupport(this);
        delegate.addPropertyChangeListener(WeakListeners.propertyChange(this, delegate));
    }

    @Override
    public List<? extends PathResourceImplementation> getResources() {
        long currentEventId;
        synchronized (this) {
            if (resources != null) {
                return this.resources;
            }
            currentEventId = this.eventId;
        }

        List<PathResourceImplementation> res = fnc.apply(delegate.entries());

        synchronized (this) {
            if (currentEventId == this.eventId) {
                if (this.resources == null) {
                    this.resources = res;
                }
                else {
                    res = this.resources;
                }
            }
        }
        assert res != null;
        return res;
    }

    @Override
    public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        assert listener != null;
        pcSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        assert listener != null;
        pcSupport.removePropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        synchronized (this) {
            this.resources = null;
            this.eventId++;
        }
        pcSupport.firePropertyChange(PROP_RESOURCES, null, null);
    }


    public static ClassPathImplementation sources (@NonNull final ClassPath cp) {
        assert cp != null;
        return new AptSourcePath(cp, new MapToSources());
    }

    public static ClassPathImplementation aptCache (@NonNull final ClassPath cp) {
        assert cp != null;
        return new AptSourcePath(cp, new MapToAptCache());
    }

    public static ClassPathImplementation aptOputput(@NonNull final ClassPath cp) {
        assert cp != null;
        return new AptSourcePath(cp, new MapToAptGenerated());
    }


    private static class MapToSources implements Function<List<ClassPath.Entry>,List<PathResourceImplementation>> {

        @Override
        public List<PathResourceImplementation> apply(List<ClassPath.Entry> entries) {
            final List<PathResourceImplementation> res = new ArrayList<PathResourceImplementation>();
            final Set<? extends URL> aptBuildGenerated = getAptBuildGeneratedFolders(entries);
            for (ClassPath.Entry entry : entries) {
                if (!aptBuildGenerated.contains(entry.getURL())) {
                    res.add(new FR (entry));
                }
            }
            return res;
        }
    }

    private static class MapToAptCache implements Function<List<ClassPath.Entry>,List<PathResourceImplementation>> {

        @Override
        public List<PathResourceImplementation> apply(List<ClassPath.Entry> entries) {
            final List<PathResourceImplementation> res = new ArrayList<PathResourceImplementation>();
            for (ClassPath.Entry entry : entries) {
                final URL aptRoot = AptCacheForSourceQuery.getAptFolder(entry.getURL());
                if (aptRoot != null) {
                    res.add(ClassPathSupport.createResource(aptRoot));
                }
            }
            return res;
        }

    }

    private static class MapToAptGenerated implements Function<List<ClassPath.Entry>,List<PathResourceImplementation>> {

        @Override
        public List<PathResourceImplementation> apply(List<ClassPath.Entry> entries) {
            final Set<? extends URL> aptGenerated = getAptBuildGeneratedFolders(entries);
            final List<PathResourceImplementation> resources = new ArrayList<PathResourceImplementation>(aptGenerated.size());
            for (URL agr : aptGenerated) {
                resources.add(ClassPathSupport.createResource(agr));
            }
            return resources;
        }
    }

    @NonNull
    static Set<? extends URL> getAptBuildGeneratedFolders(@NonNull final List<ClassPath.Entry> entries) {
        final Set<URL> roots = new HashSet<>();
        for (ClassPath.Entry entry : entries) {
            roots.add(entry.getURL());
        }
        for (ClassPath.Entry entry : entries) {
            final FileObject fo = entry.getRoot();
            if (fo != null) {
                URL aptRoot;
                final APTUtils aptUtils = APTUtils.getIfExist(fo);
                if (aptUtils != null) {
                    aptRoot = aptUtils.sourceOutputDirectory();
                } else {
                    aptRoot = AnnotationProcessingQuery.getAnnotationProcessingOptions(fo).sourceOutputDirectory();
                }
                if (roots.contains(aptRoot)) {
                    return Collections.singleton(aptRoot);
                }
            }
        }
        return Collections.<URL>emptySet();
    }

    private static class FR implements FilteringPathResourceImplementation, PropertyChangeListener {

        private final ClassPath classPath;
        private final ClassPath.Entry entry;
        private final PropertyChangeSupport support;
        private final URL[] cache;

        @SuppressWarnings("LeakingThisInConstructor")
        public FR (final ClassPath.Entry entry) {
            assert entry != null;
            this.support = new PropertyChangeSupport(this);
            this.entry = entry;
            this.classPath = entry.getDefiningClassPath();
            this.classPath.addPropertyChangeListener(WeakListeners.propertyChange(this, classPath));
            this.cache = new URL[] {entry.getURL()};
        }

        @Override
        public boolean includes(URL root, String resource) {
            assert this.cache[0].equals(root);
            return entry.includes(resource);
        }

        @Override
        public URL[] getRoots() {
            return cache;
        }

        @Override
        public ClassPathImplementation getContent() {
            return null;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            this.support.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            this.support.removePropertyChangeListener(listener);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (ClassPath.PROP_INCLUDES.equals(evt.getPropertyName())) {
                this.support.firePropertyChange(PROP_INCLUDES, null, null);
            }
        }
    }
}
