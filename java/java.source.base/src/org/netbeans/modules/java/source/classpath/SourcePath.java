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

package org.netbeans.modules.java.source.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.ClassIndexManagerEvent;
import org.netbeans.modules.java.source.usages.ClassIndexManagerListener;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
public class SourcePath implements ClassPathImplementation, PropertyChangeListener {

    private static final boolean NO_SOURCE_FILTER = Boolean.getBoolean("SourcePath.no.source.filter");  //NOI18N

    private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    private final ClassPathImplementation delegate;
    private final boolean forcePreferSources;
    private final Function<Pair<Boolean,List<? extends PathResourceImplementation>>,List<PathResourceImplementation>> f;
    //@GuardedBy("this")
    private long eventId;
    //@GuardedBy("this")
    private List<PathResourceImplementation> resources;

    @SuppressWarnings("LeakingThisInConstructor")
    private SourcePath (
            @NonNull final ClassPathImplementation delegate,
            final boolean forcePreferSources,
            @NonNull final Function<Pair<Boolean,List<? extends PathResourceImplementation>>,List<PathResourceImplementation>> f) {
        assert delegate != null;
        assert f != null;
        this.delegate = delegate;
        this.forcePreferSources = forcePreferSources;
        this.f = f;
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
        List<PathResourceImplementation> res = f.apply(
                Pair.<Boolean,List<? extends PathResourceImplementation>>of(forcePreferSources,delegate.getResources()));
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
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        listeners.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        listeners.removePropertyChangeListener(listener);
    }

    @Override
    public void propertyChange (final PropertyChangeEvent event) {
        synchronized (this) {
            this.resources = null;
            this.eventId++;
        }
        listeners.firePropertyChange(PROP_RESOURCES, null, null);
    }

    public static ClassPathImplementation filtered (
            @NonNull final ClassPathImplementation cpImpl,
            final boolean bkgComp) {
        assert cpImpl != null;
        return new SourcePath(
                cpImpl,
                bkgComp,
                NO_SOURCE_FILTER ? new AllRoots() : new FilterNonOpened());
    }


    private static class FilterNonOpened implements  Function<
            Pair<Boolean,List<? extends PathResourceImplementation>>,
            List<PathResourceImplementation>> {

        @Override
        public List<PathResourceImplementation> apply(
                @NonNull final Pair<Boolean,List<? extends PathResourceImplementation>> resources) {
            final List<PathResourceImplementation> res = new ArrayList<PathResourceImplementation>(resources.second().size());
            for (PathResourceImplementation pr : resources.second()) {
                res.add(new FR(pr,resources.first()));
            }
            return res;
        }
    }

    private static class AllRoots implements Function<
            Pair<Boolean,List<? extends PathResourceImplementation>>,
            List<PathResourceImplementation>> {

        @Override
        public List<PathResourceImplementation> apply(Pair<Boolean, List<? extends PathResourceImplementation>> resources) {
            final List<PathResourceImplementation> res = new ArrayList<PathResourceImplementation>(resources.second().size());
            for (PathResourceImplementation pr : resources.second()) {
                res.add(new FR(pr,true));
            }
            return res;
        }

    }

    private static class FR implements FilteringPathResourceImplementation, PropertyChangeListener, ClassIndexManagerListener {

        private final PathResourceImplementation pr;
        private final boolean forcePreferSources;
        private final PropertyChangeSupport support;
        //@GuardedBy("this")
        private URL[] cache;
        //@GuardedBy("this")
        private long eventId;

        @SuppressWarnings("LeakingThisInConstructor")
        public FR (
                @NonNull final PathResourceImplementation pr,
                final boolean forcePreferSources) {
            assert pr != null;
            this.pr = pr;
            this.forcePreferSources = forcePreferSources;
            this.support = new PropertyChangeSupport(this);
            this.pr.addPropertyChangeListener(WeakListeners.propertyChange(this, pr));
            final ClassIndexManager manager = ClassIndexManager.getDefault();
            manager.addClassIndexManagerListener(WeakListeners.create(ClassIndexManagerListener.class, this, manager));
        }

        @Override
        public boolean includes(URL root, String resource) {
            final URL[] roots = getRoots();
            boolean contains = false;
            for (URL r : roots) {
                if (r.equals(root)) {
                    contains = true;
                }
            }
            return contains &&
                (!(pr instanceof FilteringPathResourceImplementation) ||
                    ((FilteringPathResourceImplementation)pr).includes(root, resource));
        }

        @Override
        public URL[] getRoots() {
            long currentEventId;
            synchronized (this) {
                if (cache != null) {
                    return cache;
                }
                currentEventId = this.eventId;
            }
            final URL[] origRoots = pr.getRoots();
            final List<URL> rootsList = new ArrayList<URL>(origRoots.length);
            for (URL url : origRoots) {
                if (forcePreferSources || JavaIndex.hasSourceCache(url,true)) {
                    rootsList.add(url);
                }
            }
            URL[] res = rootsList.toArray(new URL[0]);
            synchronized (this) {
                if (currentEventId == this.eventId) {
                    if (cache == null) {
                        cache = res;
                    } else {
                        res = cache;
                    }
                }
            }
            assert res != null;
            return res;
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
            if (PROP_ROOTS.equals(evt.getPropertyName())) {
                synchronized (this) {
                    this.cache = null;
                    this.eventId++;
                }
                this.support.firePropertyChange(PROP_ROOTS, null, null);
            } else if (PROP_INCLUDES.equals(evt.getPropertyName())) {
                synchronized (this) {
                    this.cache = null;
                    this.eventId++;
                }
                this.support.firePropertyChange(PROP_INCLUDES, null, null);
            }
        }

        @Override
        public void classIndexAdded(ClassIndexManagerEvent event) {
            if (forcePreferSources) {
                return;
            }
            final Set<? extends URL> newRoots = event.getRoots();
            boolean changed = false;
            for (URL url : pr.getRoots()) {
                changed = newRoots.contains(url);
                if (changed) {
                    break;
                }
            }
            if (changed) {
                synchronized (this) {
                    this.cache = null;
                    this.eventId++;
                }
                this.support.firePropertyChange(PROP_ROOTS, null, null);
            }
        }

        @Override
        public void classIndexRemoved(ClassIndexManagerEvent event) {
        }
    }
}
