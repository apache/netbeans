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

package org.netbeans.modules.project.libraries.ui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.spi.project.libraries.WritableLibraryProvider;
import org.netbeans.spi.project.libraries.ArealLibraryProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation2;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.netbeans.spi.project.libraries.LibraryStorageAreaCache;
import org.netbeans.spi.project.libraries.NamedLibraryImplementation;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.util.Lookup;
import org.openide.util.ChangeSupport;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

public class LibrariesModel implements PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger(LibrariesModel.class.getName());

    /**
     * Set of areas which have been explicitly created/loaded in this IDE session (thus static).
     * Keep only URL, <em>not</em> LibraryStorageArea, to avoid memory leaks.
     * Could also be modified to persist a LRU in NbPreferences, etc.
     */
    private static final Set<URL> createdAreas = Collections.synchronizedSet(new HashSet<URL>());

    private final Map<LibraryImplementation,LibraryStorageArea> library2Area = new HashMap<LibraryImplementation,LibraryStorageArea>();
    private final Map<LibraryStorageArea,ArealLibraryProvider> area2Storage = new HashMap<LibraryStorageArea,ArealLibraryProvider>();
    private final Map<LibraryImplementation,LibraryProvider> storageByLib = new HashMap<LibraryImplementation,LibraryProvider>();
    private final Map<LibraryStorageArea,LibraryProvider> area2Provider = new HashMap<LibraryStorageArea,LibraryProvider>();
    private final Collection<LibraryImplementation> actualLibraries = new TreeSet<LibraryImplementation>(new LibrariesComparator());
    private final List<LibraryImplementation> addedLibraries;
    private final List<LibraryImplementation> removedLibraries;
    private final List<ProxyLibraryImplementation> changedLibraries;
    private WritableLibraryProvider writableProvider;
    private final ChangeSupport cs = new ChangeSupport(this);

    public LibrariesModel () {
        this.addedLibraries = new ArrayList<LibraryImplementation>();
        this.removedLibraries = new ArrayList<LibraryImplementation>();
        this.changedLibraries = new ArrayList<ProxyLibraryImplementation>();
        for (LibraryProvider lp : Lookup.getDefault().lookupAll(LibraryProvider.class)) {
            lp.addPropertyChangeListener(WeakListeners.propertyChange(this, lp));
            if (writableProvider == null && lp instanceof WritableLibraryProvider) {
                writableProvider = (WritableLibraryProvider) lp;
            }
        }
        for (ArealLibraryProvider alp : Lookup.getDefault().lookupAll(ArealLibraryProvider.class)) {
            alp.addPropertyChangeListener(WeakListeners.propertyChange(this, alp));
        }
        this.computeLibraries();
    }
    
    public synchronized Collection<? extends LibraryImplementation> getLibraries() {
        return actualLibraries;
    }

    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }

    public LibraryStorageArea createArea() {
        for (ArealLibraryProvider alp : Lookup.getDefault().lookupAll(ArealLibraryProvider.class)) {
            LibraryStorageArea area = alp.createArea();
            if (area != null) {
                createdAreas.add(area.getLocation());
                area2Storage.put(area, alp);
                propertyChange(null); // recompute libraries & fire change
                return area;
            }
        }
        return null;
    }

    public LibraryImplementation createArealLibrary(String type, String name, LibraryStorageArea area) {
        LibraryImplementation impl = new DummyArealLibrary(type, name);
        assert area2Storage.get(area) != null : unknownArea(area);
        library2Area.put(impl, area);
        return impl;
    }

    public Collection<? extends LibraryStorageArea> getAreas() {
        Set<LibraryStorageArea> areas = new HashSet<LibraryStorageArea>();
        for (ArealLibraryProvider<?,?> alp : Lookup.getDefault().lookupAll(ArealLibraryProvider.class)) {
            for (LibraryStorageArea area : alp.getOpenAreas()) {
                area2Storage.put(area, alp);
                areas.add(area);
            }
        }
        for (ArealLibraryProvider alp : Lookup.getDefault().lookupAll(ArealLibraryProvider.class)) {
            for (URL location : createdAreas) {
                LibraryStorageArea area = alp.loadArea(location);
                if (area != null) {
                    assert area.getLocation().equals(location) : "Bad location " + area.getLocation() + " does not match " + location + " from " + alp.getClass().getName();
                    area2Storage.put(area, alp);
                    areas.add(area);
                }
            }
        }
        return areas;
    }

    public LibraryStorageArea getArea(LibraryImplementation library) {
        LibraryStorageArea area = getAreaOrNull(library);
        return area != null ? area : LibraryStorageArea.GLOBAL;
    }
    private LibraryStorageArea getAreaOrNull(LibraryImplementation library) {
        if (library instanceof ProxyLibraryImplementation) {
            library = ((ProxyLibraryImplementation) library).getDelegate();
        }
        return library2Area.get(library);
    }

    public void addLibrary (LibraryImplementation impl) {
        synchronized (this) {
            addedLibraries.add(impl);
            actualLibraries.add(impl);
        }
        cs.fireChange();
    }

    public void removeLibrary (LibraryImplementation impl) {
        synchronized (this) {
            if (addedLibraries.contains(impl)) {
                addedLibraries.remove(impl);
            } else {
                removedLibraries.add(((ProxyLibraryImplementation) impl).getDelegate());
            }
            actualLibraries.remove(impl);
        }
        cs.fireChange();
    }

    public void modifyLibrary(ProxyLibraryImplementation impl) {
        synchronized (this) {
            if (!addedLibraries.contains(impl) && !changedLibraries.contains(impl)) {
                changedLibraries.add(impl);
            }
        }
        cs.fireChange();
    }

    public boolean isLibraryEditable (LibraryImplementation impl) {
        if (this.addedLibraries.contains(impl))
            return true;
        LibraryProvider provider = storageByLib.get
                (((ProxyLibraryImplementation)impl).getDelegate());
        return provider == writableProvider || getAreaOrNull(impl) != null;
    }

    public void apply () throws IOException {
        for (LibraryImplementation impl : removedLibraries) {
            LibraryProvider storage = storageByLib.get(impl);
            if (storage == this.writableProvider) {
                this.writableProvider.removeLibrary (impl);
            } else {
                assert impl instanceof LibraryImplementation2;
                LibraryStorageArea area = getAreaOrNull(impl);
                if (area != null) {
                    ALPUtils.remove(area2Storage.get(area), (LibraryImplementation2)impl);
                } else {
                    throw new IOException("Cannot find storage for library: " + impl.getName()); // NOI18N
                }
            }
        }
        for (LibraryImplementation impl : addedLibraries) {
            LibraryStorageArea area = getAreaOrNull(impl);
            if (area != null) {
                ArealLibraryProvider alp = area2Storage.get(area);
                assert alp != null : "Unknown area: " + area + " known areas: " + area2Storage.keySet();
                final LibraryImplementation2 createdLib = ALPUtils.createLibrary(alp, impl.getType(), impl.getName(), area, ((DummyArealLibrary) impl).contents);
                LibrariesSupport.setDisplayName(createdLib, LibrariesSupport.getDisplayName(impl));
            } else if (writableProvider != null) {
                writableProvider.addLibrary(impl);
            } else {
                throw new IOException("Cannot add libraries, no WritableLibraryProvider."); // NOI18N
            }
        }
        for (ProxyLibraryImplementation proxy : changedLibraries) {
            LibraryImplementation orig = proxy.getDelegate();
            LibraryProvider storage = storageByLib.get(orig);
            if (storage == this.writableProvider) {
                this.writableProvider.updateLibrary(orig, proxy);
            } else {
                LibraryStorageArea area = library2Area.get(orig);
                if (area != null) {
                    if (LibrariesSupport.supportsURIContent(proxy)) {
                        final LibraryImplementation2 orig2 = (LibraryImplementation2) proxy.getDelegate();
                        if (proxy.getNewURIContents() != null) {
                            for (Map.Entry<String,List<URI>> entry : proxy.getNewURIContents().entrySet()) {
                                orig2.setURIContent(entry.getKey(), entry.getValue());
                            }
                        }
                    } else if (proxy.getNewContents() != null) {
                        for (Map.Entry<String,List<URL>> entry : proxy.getNewContents().entrySet()) {
                            orig.setContent(entry.getKey(), entry.getValue());
                        }
                    }
                    final String origDisplayName = LibrariesSupport.getDisplayName(orig);
                    final String newDisplayName = LibrariesSupport.getDisplayName(proxy);
                    if (!(origDisplayName == null ? newDisplayName == null : origDisplayName.equals(newDisplayName))) {
                        LibrariesSupport.setDisplayName(orig, newDisplayName);
                    }
                } else {
                    throw new IOException("Cannot find storage for library: " + orig.getName()); // NOI18N
                }
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        // compute libraries later in AWT thread and not in calling thread
        // to prevent deadlocks
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                computeLibraries();
                cs.fireChange();
            }
        });
    }

    private ProxyLibraryImplementation findModified (LibraryImplementation impl) {
        for (ProxyLibraryImplementation proxy : changedLibraries) {
            if (proxy.getDelegate().equals (impl)) {
                return proxy;
            }
        }
        return null;
    }

    private synchronized void computeLibraries() {
        actualLibraries.clear();
        for (LibraryProvider storage : Lookup.getDefault().lookupAll(LibraryProvider.class)) {
            for (LibraryImplementation lib : storage.getLibraries()) {
                ProxyLibraryImplementation proxy = findModified(lib);
                if (proxy != null) {
                    actualLibraries.add(proxy);
                } else {
                    actualLibraries.add(proxy = ProxyLibraryImplementation.createProxy(lib, this));
                }
                storageByLib.put(lib, storage);
                LOG.log(Level.FINER, "computeLibraries: storage={0} lib={1} proxy={2}", new Object[] {storage, lib, proxy});
            }
        }
        for (LibraryStorageArea area : getAreas()) {
            ArealLibraryProvider alp = area2Storage.get(area);
            assert alp != null : area;
            LibraryProvider prov = area2Provider.get(area);
            if (prov == null) {
                prov = ALPUtils.getLibraries(alp, area);
                prov.addPropertyChangeListener(this); // need not be weak, we just created the source
                area2Provider.put(area, prov);
            }
            for (LibraryImplementation lib : prov.getLibraries()) {
                ProxyLibraryImplementation proxy = findModified(lib);
                if (proxy != null) {
                    actualLibraries.add(proxy);
                } else {
                    actualLibraries.add(proxy = ProxyLibraryImplementation.createProxy(lib, this));
                }
                library2Area.put(lib, area);
                LOG.log(Level.FINER, "computeLibraries: alp={0} area={1} lib={2} proxy={3}", new Object[] {alp, area, lib, proxy});
            }
        }
        actualLibraries.addAll(addedLibraries);
        LOG.log(Level.FINE, "computeLibraries: actualLibraries={0} library2Area={1}", new Object[] {actualLibraries, library2Area});
    }

    private String unknownArea(final LibraryStorageArea area) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Creating library in unknown area: ").    //NOI18N
        append(area).
        append(" known areas: ").                           //NOI18N
        append(area2Storage.keySet()).
        append(" created: ").
        append(createdAreas);
        getAreas();
        sb.append(" known areas after reinit: ").
        append(area2Storage.keySet()).
        append(" providers: ").
        append(Lookup.getDefault().lookupAll(ArealLibraryProvider.class));
        return sb.toString();
    }

    private static class LibrariesComparator implements Comparator<LibraryImplementation> {
        public int compare(LibraryImplementation lib1, LibraryImplementation lib2) {
            String name1 = LibrariesSupport.getLocalizedName(lib1);
            String name2 = LibrariesSupport.getLocalizedName(lib2);
            int r = name1.compareToIgnoreCase(name2);
            return r != 0 ? r : System.identityHashCode(lib1) - System.identityHashCode(lib2);
        }
    }

    private static final class DummyArealLibrary implements LibraryImplementation2, NamedLibraryImplementation {

        private final String type, name;
        final Map<String,List<URI>> contents = new HashMap<String,List<URI>>();
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private String displayName;

        public DummyArealLibrary(String type, String name) {
            this.type = type;
            this.name = name;
        }

        @Override
        public String getType() {
            return type;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String getLocalizingBundle() {
            return null;
        }

        @Override
        public List<URL> getContent(String volumeType) throws IllegalArgumentException {
            return LibrariesSupport.convertURIsToURLs(
                getURIContent(volumeType),
                LibrariesSupport.ConversionMode.WARN);
        }
        
        @Override
        public List<URI> getURIContent(String volumeType) throws IllegalArgumentException {
            List<URI> content = contents.get(volumeType);
            if (content != null) {
                return content; 
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public void setName(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDisplayName(final @NullAllowed String displayName) {
            this.displayName = displayName;
        }

        @Override
        public void setDescription(String text) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLocalizingBundle(String resourceName) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
            pcs.addPropertyChangeListener(l);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
            pcs.removePropertyChangeListener(l);
        }

        @Override
        public void setContent(String volumeType, List<URL> path) throws IllegalArgumentException {
            setURIContent(volumeType, LibrariesSupport.convertURLsToURIs(
                    path,
                    LibrariesSupport.ConversionMode.WARN));
        }

        @Override
        public void setURIContent(String volumeType, List<URI> path) throws IllegalArgumentException {
            contents.put(volumeType, path);
            pcs.firePropertyChange(LibraryImplementation.PROP_CONTENT, null, null);
        }

        @Override
        public String toString() {
            return "DummyArealLibrary[" + name + "]"; // NOI18N
        }

    }

    @ServiceProvider(service = LibraryStorageAreaCache.class)
    public static final class LibrariesModelCache implements LibraryStorageAreaCache {
        @Override
        public Collection<? extends URL> getCachedAreas() {
            return Collections.unmodifiableCollection(new ArrayList<URL>(createdAreas));
        }
    }

}
