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
package org.netbeans.spi.java.project.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.FlaggedClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.project.LookupMerger;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.WeakListeners;

/**
 * Lookup Merger implementation for ClassPathProvider
 * 
 * @author Tomas Zezula, Milos Kleint
 */
final class ClassPathProviderMerger implements LookupMerger<ClassPathProvider> {

    private final ClassPathProvider defaultProvider;

    ClassPathProviderMerger(final ClassPathProvider defaultProvider) {
        assert defaultProvider != null;
        this.defaultProvider = defaultProvider;
    }

    public Class<ClassPathProvider> getMergeableClass() {
        return ClassPathProvider.class;
    }

    public ClassPathProvider merge(Lookup lookup) {
        return new CPProvider(lookup);
    }

    private class CPProvider implements ClassPathProvider {

        private final Lookup lookup;
        private final Map<FileObject, Map<String, ClassPath>> cpCache = new HashMap<FileObject, Map<String, ClassPath>>();

        public CPProvider(final Lookup lookup) {
            assert lookup != null;
            this.lookup = lookup;
        }

        public ClassPath findClassPath(FileObject file, String type) {
            synchronized (cpCache) {
                Map<String, ClassPath> cptype = cpCache.get(file);
                if (cptype != null) {
                    ClassPath path = cptype.get(type);
                    if (path != null) {
                        return path;
                    }
                }
            }
            ProxyClassPathImplementation result = new ProxyClassPathImplementation(defaultProvider, lookup, file, type);
            if (!result.hasAnyResults()) {
                return null;
            }
            ClassPath cp = ClassPathFactory.createClassPath(result);
            synchronized (cpCache) {
                Map<String, ClassPath> cptype = cpCache.get(file);
                if (cptype == null) {
                    cptype = new HashMap<String, ClassPath>();
                    cpCache.put(file, cptype);
                }
                cptype.put(type, cp);
            }
            return cp;
        }
    }
    
    /** ProxyClassPathImplementation provides read only proxy for PathResourceImplementations based on original ClassPath items.
     *  The order of the resources is given by the order of its delegates.
     *  The proxy is designed to be used as a union of class paths.
     *  E.g. to be able to easily iterate or listen on all design resources = sources + compile resources
     */
    class ProxyClassPathImplementation implements FlaggedClassPathImplementation {

        private ProxyFilteringCPI[] classPaths;
        private List<PathResourceImplementation> resourcesCache;
        private Set<ClassPath.Flag> flagsCache;
        private ArrayList<PropertyChangeListener> listeners;
        private LookupListener lookupList;
        private PropertyChangeListener flagsListener;
        private Lookup.Result<ClassPathProvider> providers;
        private ClassPathProvider mainProvider;
        private FileObject file;
        private String type;
        private boolean hasAny = false;

        public ProxyClassPathImplementation(ClassPathProvider dominant, Lookup context, FileObject fo, String type) {
            assert dominant != null;
            this.type = type;
            this.file = fo;
            mainProvider = dominant;
            providers = context.lookupResult(ClassPathProvider.class);
            flagsListener = (ev) -> {
                if (ProxyFilteringCPI.PROP_CP_FLAGS.equals(ev.getPropertyName())) {
                    synchronized (this) {
                        this.flagsCache = null;
                    }
                    firePropertyChange(new PropertyChangeEvent(this, PROP_FLAGS, null, null));
                }
            };
            checkProviders();
            lookupList = (ev) -> {
                checkProviders();
            };
            providers.addLookupListener(lookupList);
        }

        boolean hasAnyResults() {
            return hasAny;
        }

        private void checkProviders() {
            boolean hasAnyVote = false;
            final List<ProxyFilteringCPI> impls = new ArrayList<>();
            ClassPath mainResult = mainProvider.findClassPath(file, type);
            if (mainResult != null) {
                hasAnyVote = true;
                impls.add(new ProxyFilteringCPI(mainResult));
            }
            for (ClassPathProvider prvd : providers.allInstances()) {
                ClassPath path = prvd.findClassPath(file, type);
                if (path != null) {
                    hasAnyVote = true;
                    impls.add(new ProxyFilteringCPI(path));
                }
            }
            synchronized (this) {
                this.hasAny = hasAnyVote;
                if (this.classPaths != null) {
                    for (ProxyFilteringCPI pr : this.classPaths) {
                        pr.removePropertyChangeListener(flagsListener);
                    }
                }
                this.classPaths = impls.toArray(new ProxyFilteringCPI[0]);
                for (ProxyFilteringCPI pr : this.classPaths) {
                    pr.addPropertyChangeListener(flagsListener);
                }
                //Clean the cache
                resourcesCache = null;
                flagsCache = null;
            }
            firePropertyChange(new PropertyChangeEvent(this, PROP_RESOURCES, null, null));
            firePropertyChange(new PropertyChangeEvent(this, PROP_FLAGS, null, null));
        }

        @Override
        public Set<ClassPath.Flag> getFlags() {
            synchronized (this) {
                if (this.flagsCache != null) {
                    return this.flagsCache;
                }
            }
            final Set<ClassPath.Flag> flags = EnumSet.noneOf(ClassPath.Flag.class);
            for (ProxyFilteringCPI impl : classPaths) {
                flags.addAll(impl.getClassPathFlags());
            }
            synchronized (this) {
                if (this.flagsCache == null) {
                    flagsCache = Collections.unmodifiableSet(flags);
                }
                return this.flagsCache;
            }
        }

        public List<? extends PathResourceImplementation> getResources() {
            synchronized (this) {
                if (this.resourcesCache != null) {
                    return this.resourcesCache;
                }
            }

            ArrayList<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>(classPaths.length * 10);
            for (PathResourceImplementation cpImpl : classPaths) {
                result.add(cpImpl);
            }

            synchronized (this) {
                if (this.resourcesCache == null) {
                    resourcesCache = Collections.unmodifiableList(result);
                }
                return this.resourcesCache;
            }
        }

        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            if (this.listeners == null) {
                this.listeners = new ArrayList<PropertyChangeListener>();
            }
            this.listeners.add(listener);
        }

        public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
            if (this.listeners == null) {
                return;
            }
            this.listeners.remove(listener);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder("[");   //NOI18N

            for (PathResourceImplementation cpImpl : this.classPaths) {
                builder.append(cpImpl.toString());
                builder.append(", ");   //NOI18N

            }
            builder.append("]");   //NOI18N

            return builder.toString();
        }

        private void firePropertyChange(PropertyChangeEvent event) {
            PropertyChangeListener[] _listeners;
            synchronized (this) {
                if (listeners == null) {
                    return;
                }
                _listeners = listeners.toArray(new PropertyChangeListener[ProxyClassPathImplementation.this.listeners.size()]);
            }
            for (PropertyChangeListener l : _listeners) {
                l.propertyChange(event);
            }
        }
    }

    private static class ProxyFilteringCPI implements FilteringPathResourceImplementation, PropertyChangeListener {
        static final String PROP_CP_FLAGS = "classPathFlags";   //NOI18N
        private final ClassPath classpath;
        private final PropertyChangeSupport changeSupport;

        private ProxyFilteringCPI(
                final ClassPath path) {
            assert path != null;
            this.classpath = path;
            this.changeSupport = new PropertyChangeSupport(this);
            this.classpath.addPropertyChangeListener(WeakListeners.propertyChange(this, this.classpath));
        }

        
        public boolean includes(URL root, String resource) {
            for (ClassPath.Entry ent : classpath.entries()) {
                if (ent.getURL().equals(root)) {
                    return ent.includes(resource);
                }
            }
            return false;
        }

        public URL[] getRoots() {
            ArrayList<URL> urls = new ArrayList<URL>();
            for (ClassPath.Entry ent : classpath.entries()) {
                urls.add(ent.getURL());
            }
            return urls.toArray(new URL[0]);
        }

        public ClassPathImplementation getContent() {
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
           this.changeSupport.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            this.changeSupport.removePropertyChangeListener(listener);
        }

        public void propertyChange(final PropertyChangeEvent evt) {
            final String propName = evt.getPropertyName();
            if (ClassPath.PROP_ENTRIES.equals(propName)) {
                this.changeSupport.firePropertyChange(PROP_ROOTS, null, null);
            } else if (ClassPath.PROP_INCLUDES.equals(propName)) {
                this.changeSupport.firePropertyChange(PROP_INCLUDES, null, null);
            } else if (ClassPath.PROP_FLAGS.equals(propName)) {
                this.changeSupport.firePropertyChange(PROP_CP_FLAGS, null, null);
            }
        }

        @NonNull
        Set<ClassPath.Flag> getClassPathFlags() {
            return classpath.getFlags();
        }
    }
}
