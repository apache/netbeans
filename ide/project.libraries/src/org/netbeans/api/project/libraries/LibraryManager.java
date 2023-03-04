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

package org.netbeans.api.project.libraries;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.project.libraries.LibraryAccessor;
import org.netbeans.spi.project.libraries.WritableLibraryProvider;
import org.netbeans.spi.project.libraries.ArealLibraryProvider;
import org.netbeans.spi.project.libraries.LibraryImplementation;
import org.netbeans.spi.project.libraries.LibraryImplementation2;
import org.netbeans.spi.project.libraries.LibraryProvider;
import org.netbeans.spi.project.libraries.LibraryStorageArea;
import org.netbeans.spi.project.libraries.LibraryStorageAreaCache;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.util.Lookup;
import org.openide.util.LookupListener;
import org.openide.util.LookupEvent;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * LibraryManager provides registry of the installed libraries.
 * LibraryManager can be used to list all installed libraries or to
 * query library by its system name.
 */
public final class LibraryManager {

    /**
     * Property fired when the set of libraries changes.
     */
    public static final String PROP_LIBRARIES = "libraries"; //NOI18N

    private static LibraryManager instance;

    private Lookup.Result<LibraryProvider> result;
    private final Collection<LibraryProvider> currentStorages = new ArrayList<LibraryProvider>();
    private final PropertyChangeListener plistener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (LibraryProvider.PROP_LIBRARIES.equals(evt.getPropertyName())) {
                resetCache();
            }
        }
    };
    private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);
    private static final PropertyChangeSupport openLibraryManagerListListeners = 
            new PropertyChangeSupport(LibraryManager.class);
    private static final PropertyChangeListener AREAL_LIBRARY_PROVIDER_LISTENER = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                openLibraryManagerListListeners.firePropertyChange(PROP_OPEN_LIBRARY_MANAGERS, null, null);
            }
        };
        
    /** Property fired when list of open library managers changes. */
    public static final String PROP_OPEN_LIBRARY_MANAGERS = "openManagers"; // NOI18N
    private static Lookup.Result<ArealLibraryProvider> areaProvidersLookupResult = null;
    private static Collection<? extends ArealLibraryProvider> currentAreaProviders = new ArrayList<ArealLibraryProvider>();

    private Collection<Library> cache;
    /** null for default manager */
    private final ArealLibraryProvider alp;
    /** null for default manager */
    private final LibraryStorageArea area;
    private LookupListener lookupListener;
    /**Event lamport's clock**/
    private long eventId;

    private LibraryManager () {
        alp = null;
        area = LibraryStorageArea.GLOBAL;
    }

    private LibraryManager(
            @NonNull final ArealLibraryProvider alp,
            @NonNull final LibraryStorageArea area) {
        Parameters.notNull("alp", alp); //NOI18N
        Parameters.notNull("area", area);   //NOI18N
        this.alp = alp;
        this.area = area;
        LibraryProvider lp = LibraryAccessor.getLibraries(alp, area);
        lp.addPropertyChangeListener(WeakListeners.propertyChange(plistener, lp));
        currentStorages.add(lp);
    }

    /**
     * Gets a human-readable description of this manager.
     * This may be used to visually differentiate the global manager from various local managers.
     * @return a localized display name
     * @see LibraryStorageArea#getDisplayName
     * @since org.netbeans.modules.project.libraries/1 1.15
     */
    public String getDisplayName() {
        return area.getDisplayName();
    }

    /**
     * Gets the location associated with this manager.
     * @return a location where library definitions are kept, or null in the case of {@link #getDefault}
     * @see LibraryStorageArea#getLocation
     * @see #forLocation
     * @since org.netbeans.modules.project.libraries/1 1.15
     */
    public URL getLocation() {
        return area.getLocation();
    }

    /**
     * Returns library by its name.
     * @param name of the library, must not be null
     * @return library or null if the library is not found
     */
    public Library getLibrary(String name) {
        Parameters.notNull("name", name);   //NOI18N
        Library[] libs = this.getLibraries();
        for (int i = 0; i < libs.length; i++) {
            if (name.equals(libs[i].getName())) {
                return libs[i];
            }
        }
        return null;
    }

    /**
     * Lists all libraries defined in this manager.
     * @return library definitions (never <code>null</code>)
     */
    public Library[] getLibraries() {

        final List<Library> l = new ArrayList<Library>();
        final Collection<? extends LibraryProvider> instances;
        long myId;
        synchronized (this) {
            if (cache != null) {
                return cache.toArray(new Library[0]);
            }
            if (area == LibraryStorageArea.GLOBAL) {
                if (result == null) {
                    result = Lookup.getDefault().lookupResult(LibraryProvider.class);
                    lookupListener = new LookupListener() {
                        public void resultChanged(LookupEvent ev) {
                            resetCache();
                        }
                    };
                    result.addLookupListener(WeakListeners.create(LookupListener.class, lookupListener, result));
                }
                instances = result.allInstances();
                Collection<LibraryProvider> added = new HashSet<LibraryProvider>(instances);
                added.removeAll(currentStorages);
                Collection<LibraryProvider> removed = new HashSet<LibraryProvider>(currentStorages);
                removed.removeAll(instances);
                currentStorages.clear();
                for (LibraryProvider storage : instances) {
                    currentStorages.add(storage);
                }
                for (LibraryProvider p : removed) {
                    p.removePropertyChangeListener(plistener);
                }
                for (LibraryProvider p : added) {
                    p.addPropertyChangeListener(plistener);
                }
            } else {
                instances = Collections.singleton(currentStorages.iterator().next());
            }
            myId = this.eventId;
        }
        assert instances != null;
        for (LibraryProvider storage : instances) {
            for (LibraryImplementation impl : storage.getLibraries()) {
                l.add(new Library(impl, LibraryManager.this));
            }
        }
        synchronized (this) {
            assert l != null;
            if (this.eventId == myId) {
                cache = l;
                return cache.toArray(new Library[0]);
            }
            else {
                return l.toArray(new Library[0]);
            }
        }
    }
    
    
    /**
     * Installs a new library into the library manager.
     * <div class="nonnormative">
     * <p>
     * A typical usage would be:
     * </p>
     * LibraryManager libraryManager = LibraryManager.getDefault();
     * LibraryImplementation libImpl = LibrariesSupport.getLibraryTypeProvider("j2se").createLibrary();        
     * libImpl.setName("FooLibTest");
     * libImpl.setContent ("classpath",listOfResources);
     * libraryManager.addLibrary(LibraryFactory.createLibrary(libImpl));
     * </div>
     * @param library to be installed, the library has to be created
     * with registered {@link org.netbeans.spi.project.libraries.LibraryTypeProvider}.
     * @throws IOException when the library cannot be stored
     * @throws IllegalArgumentException if the library is not recognized by any 
     * {@link org.netbeans.spi.project.libraries.LibraryTypeProvider} or the library
     * of the same name already exists, or if this manager is not {@link #getDefault}.
     * @since org.netbeans.modules.project.libraries/1 1.14
     * @deprecated Use {@link #createLibrary} instead, as this properly supports local managers.
     */
    @Deprecated
    public void addLibrary (final Library library) throws IOException, IllegalArgumentException {
        Parameters.notNull("library", library); //NOI18N
        if (LibrariesSupport.getLibraryTypeProvider(library.getType()) == null) {
            throw new IllegalArgumentException ("Trying to add a library of unknown type: " + library.getType()); //NOI18N
        }
        String newLibraryName = library.getName();
        if ( newLibraryName == null || getLibrary(newLibraryName)!= null) {
            throw new IllegalArgumentException ("Library hasn't name or the name is already used: " + newLibraryName); //NOI18N
        }
        final Collection<? extends WritableLibraryProvider> providers = Lookup.getDefault().lookupAll(WritableLibraryProvider.class);
        assert providers.size() == 1;        
        providers.iterator().next().addLibrary(library.getLibraryImplementation());
    }

    /**
     * Creates a new library definition and adds it to the list.
     * @param type the type of library, as in {@link LibraryTypeProvider#getLibraryType} or {@link LibraryImplementation#getType}
     * @param name the identifying name of the new library (must not duplicate a name already in use by a library in this manager)
     * @param contents the initial contents of the library's volumes, as a map from volume type to volume content
     * @return a newly created library
     * @throws IOException if the new definition could not be stored
     * @throws IllegalArgumentException if the library type or one of the content volume types is not supported,
     *                                  or if a library of the same name already exists in this manager
     * @see ArealLibraryProvider#createLibrary
     * @since org.netbeans.modules.project.libraries/1 1.15
     */
    public Library createLibrary(String type, String name, Map<String,List<URL>> contents) throws IOException {
        return createLibrary(type, name, null, null, contents);
    }

    /**
     * Creates a new library definition and adds it to the list.
     * @param type the type of library, as in {@link LibraryTypeProvider#getLibraryType} or {@link LibraryImplementation#getType}
     * @param name the identifying name of the new library (must not duplicate a name already in use by a library in this manager)
     * @param displayName the display name of the library. If null the identifying name is used
     * @param description the library description
     * @param contents the initial contents of the library's volumes, as a map from volume type to volume content
     * @return a newly created library
     * @throws IOException if the new definition could not be stored
     * @throws IllegalArgumentException if the library type or one of the content volume types is not supported,
     *                                  or if a library of the same name already exists in this manager
     * @see ArealLibraryProvider#createLibrary
     * @since org.netbeans.modules.project.libraries/1 1.31
     */
    public Library createLibrary(
            @NonNull final  String type,
            @NonNull final String name,
            @NullAllowed final String displayName,
            @NullAllowed final String description,
            @NonNull final Map<String,List<URL>> contents) throws IOException {
        return createLibrary(type, name, displayName, description, contents, Collections.<String,String>emptyMap());
    }

    /**
     * Creates a new library definition and adds it to the list.
     * @param type the type of library, as in {@link LibraryTypeProvider#getLibraryType} or {@link LibraryImplementation#getType}
     * @param name the identifying name of the new library (must not duplicate a name already in use by a library in this manager)
     * @param displayName the display name of the library. If null the identifying name is used
     * @param description the library description
     * @param contents the initial contents of the library's volumes, as a map from volume type to volume content
     * @param properties the optional properties associated with the library.
     * @return a newly created library
     * @throws IOException if the new definition could not be stored
     * @throws IllegalArgumentException if the library type or one of the content volume types is not supported,
     *                                  or if a library of the same name already exists in this manager
     * @see ArealLibraryProvider#createLibrary
     * @since org.netbeans.modules.project.libraries/1 1.38
     */
    public Library createLibrary(
            @NonNull final  String type,
            @NonNull final String name,
            @NullAllowed final String displayName,
            @NullAllowed final String description,
            @NonNull final Map<String,List<URL>> contents,
            @NonNull final Map<String,String> properties) throws IOException {
        Parameters.notNull("properties", properties);   //NOI18N
        if (getLibrary(name) != null) {
            throw new IllegalArgumentException("Name already in use: " + name); // NOI18N
        }
        LibraryImplementation impl;
        if (area == LibraryStorageArea.GLOBAL) {
            LibraryTypeProvider ltp = LibrariesSupport.getLibraryTypeProvider(type);
            if (ltp == null) {
                throw new IllegalArgumentException("Trying to add a library of unknown type: " + type); // NOI18N
            }
            impl = ltp.createLibrary();
            impl.setName(name);
            impl.setDescription(description);
            LibrariesSupport.setDisplayName(impl, displayName);
            LibrariesSupport.setProperties(impl, properties);
            for (Map.Entry<String,List<URL>> entry : contents.entrySet()) {
                impl.setContent(entry.getKey(), entry.getValue());
            }
            Lookup.getDefault().lookup(WritableLibraryProvider.class).addLibrary(impl);
        } else {
            Map<String,List<URI>> cont = new HashMap<String,List<URI>>();
            for (Map.Entry<String,List<URL>> entry : contents.entrySet()) {
                cont.put(entry.getKey(), LibrariesSupport.convertURLsToURIs(
                    entry.getValue(),
                    LibrariesSupport.ConversionMode.WARN));
            }
            impl = LibraryAccessor.createLibrary(alp, type, name, area, cont);
            impl.setDescription(description);
            LibrariesSupport.setDisplayName(impl, displayName);
            LibrariesSupport.setProperties(impl, properties);
        }
        return new Library(impl, this);
    }

    /**
     * Creates a new library definition and adds it to the list.
     * @param type the type of library, as in {@link LibraryTypeProvider#getLibraryType} or {@link LibraryImplementation#getType}
     * @param name the identifying name of the new library (must not duplicate a name already in use by a library in this manager)
     * @param contents the initial contents of the library's volumes, as a map from volume type to volume content
     * @return a newly created library
     * @throws IOException if the new definition could not be stored
     * @throws IllegalArgumentException if the library type or one of the content volume types is not supported,
     *                                  or if a library of the same name already exists in this manager
     * @see ArealLibraryProvider#createLibrary
     * @since org.netbeans.modules.project.libraries/1 1.18
     */
    public Library createURILibrary(String type, String name, Map<String,List<URI>> contents) throws IOException {
        return createURILibrary(type, name, null, null, contents);
    }

    /**
     * Creates a new library definition and adds it to the list.
     * @param type the type of library, as in {@link LibraryTypeProvider#getLibraryType} or {@link LibraryImplementation#getType}
     * @param name the identifying name of the new library (must not duplicate a name already in use by a library in this manager)
     * @param displayName the display name of the library. If null the identifying name is used
     * @param description the library description
     * @param contents the initial contents of the library's volumes, as a map from volume type to volume content
     * @return a newly created library
     * @throws IOException if the new definition could not be stored
     * @throws IllegalArgumentException if the library type or one of the content volume types is not supported,
     *                                  or if a library of the same name already exists in this manager
     * @see ArealLibraryProvider#createLibrary
     * @since org.netbeans.modules.project.libraries/1 1.31
     */
    public Library createURILibrary(
            @NonNull final String type,
            @NonNull final String name,
            @NullAllowed final String displayName,
            @NullAllowed final String description,
            @NonNull final Map<String,List<URI>> contents) throws IOException {
        return createURILibrary(type, name, displayName, description, contents, Collections.<String,String>emptyMap());
    }

    /**
     * Creates a new library definition and adds it to the list.
     * @param type the type of library, as in {@link LibraryTypeProvider#getLibraryType} or {@link LibraryImplementation#getType}
     * @param name the identifying name of the new library (must not duplicate a name already in use by a library in this manager)
     * @param displayName the display name of the library. If null the identifying name is used
     * @param description the library description
     * @param contents the initial contents of the library's volumes, as a map from volume type to volume content
     * @return a newly created library
     * @param properties the optional properties associated with the library.
     * @throws IOException if the new definition could not be stored
     * @throws IllegalArgumentException if the library type or one of the content volume types is not supported,
     *                                  or if a library of the same name already exists in this manager
     * @see ArealLibraryProvider#createLibrary
     * @since org.netbeans.modules.project.libraries/1 1.38
     */
    public Library createURILibrary(
            @NonNull final String type,
            @NonNull final String name,
            @NullAllowed final String displayName,
            @NullAllowed final String description,
            @NonNull final Map<String,List<URI>> contents,
            @NonNull final Map<String,String> properties) throws IOException {
        Parameters.notNull("properties", properties);   //NOI18N
        if (getLibrary(name) != null) {
            throw new IllegalArgumentException("Name already in use: " + name); // NOI18N
        }
        LibraryImplementation impl;
        if (area == LibraryStorageArea.GLOBAL) {
            LibraryTypeProvider ltp = LibrariesSupport.getLibraryTypeProvider(type);
            if (ltp == null) {
                throw new IllegalArgumentException("Trying to add a library of unknown type: " + type); // NOI18N
            }
            impl = ltp.createLibrary();
            impl.setName(name);
            LibrariesSupport.setDisplayName(impl, displayName);
            LibrariesSupport.setProperties(impl, properties);
            for (Map.Entry<String,List<URI>> entry : contents.entrySet()) {
                impl.setContent(entry.getKey(), LibrariesSupport.convertURIsToURLs(
                    entry.getValue(),
                    LibrariesSupport.ConversionMode.FAIL));
            }
            Lookup.getDefault().lookup(WritableLibraryProvider.class).addLibrary(impl);
        } else {
            impl = LibraryAccessor.createLibrary(alp, type, name, area, contents);
            LibrariesSupport.setDisplayName(impl, displayName);
            LibrariesSupport.setProperties(impl, properties);
        }
        return new Library(impl, this);
    }

    /**
     * Removes installed library 
     * @param library to be removed. 
     * @throws IOException when library cannot be deleted.
     * @throws IllegalArgumentException when library is not installed in a writable
     * {@link org.netbeans.spi.project.libraries.LibraryProvider}
     * @since org.netbeans.modules.project.libraries/1 1.14
     */
    public void removeLibrary (final Library library) throws IOException, IllegalArgumentException {
        Parameters.notNull("library", library); //NOI18N
        if (area == LibraryStorageArea.GLOBAL) {
            final Collection<? extends WritableLibraryProvider> providers = Lookup.getDefault().lookupAll(WritableLibraryProvider.class);
            assert providers.size() == 1;
            providers.iterator().next().removeLibrary(library.getLibraryImplementation());
        } else {
            assert library.getLibraryImplementation() instanceof LibraryImplementation2;
            LibraryAccessor.remove(alp, (LibraryImplementation2)library.getLibraryImplementation());
        }
    }

    /**
     * Adds PropertyChangeListener.
     * The listener is notified when library is added or removed.
     * @param listener to be notified
     */
    public void addPropertyChangeListener (PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);  //NOI18N
        this.listeners.addPropertyChangeListener (listener);
    }

    /**
     * Removes PropertyChangeListener
     * @param listener
     */
    public void removePropertyChangeListener (PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.listeners.removePropertyChangeListener (listener);
    }

    final void resetCache () {
        synchronized (this) {
            this.cache = null;
            this.eventId++;
        }
        this.listeners.firePropertyChange(PROP_LIBRARIES, null, null);
    }


    /**
     * Get the default instance of the library manager.
     * @return the singleton instance
     */
    public static synchronized LibraryManager getDefault () {
        if (instance == null) {
            instance = new LibraryManager();
        }
        return instance;
    }

    /**
     * Gets a library manager which loads library definitions from a particular location.
     * There is no guarantee that the return value is the same object from call to call with the same location.
     * @param location any storage location supported by an installed provider
     * @return a library manager whose {@link #getLocation} matches the supplied location
     * @throws IllegalArgumentException if no installed provider is able to manage locations of this kind
     * @see ArealLibraryProvider#loadArea
     * @see ArealLibraryProvider#getLibraries
     * @since org.netbeans.modules.project.libraries/1 1.15
     */
    public static LibraryManager forLocation(URL location) throws IllegalArgumentException {
        for (ArealLibraryProvider alp : Lookup.getDefault().lookupAll(ArealLibraryProvider.class)) {
            LibraryStorageArea area = alp.loadArea(location);
            if (area != null) {
                return new LibraryManager(alp, area);
            }
        }
        throw new IllegalArgumentException(location.toExternalForm());
    }

    /**
     * Gets an unspecified collection of managers which are somehow to be represented as open.
     * For example, library storages referred to from open projects might be returned.
     * You can listen on changes in list of open managers via {@link #addOpenManagersPropertyChangeListener}.
     * There is no guarantee that the non-default managers are the same objects from call to call
     * even if the locations remain the same.
     * @see ArealLibraryProvider#getOpenAreas
     * @return a set of managers, always including at least {@link #getDefault}
     * @since org.netbeans.modules.project.libraries/1 1.15
     */
    public static Collection<LibraryManager> getOpenManagers() {
        List<LibraryManager> managers = new ArrayList<LibraryManager>();
        managers.add(getDefault());
        Set<URL> locations = new HashSet<URL>();
        Collection<? extends ArealLibraryProvider> alps = Lookup.getDefault().lookupAll(ArealLibraryProvider.class);
        for (ArealLibraryProvider alp : alps) {
            for (LibraryStorageArea area : LibraryAccessor.getOpenAreas(alp)) {
                if (locations.add(area.getLocation())) {
                    managers.add(new LibraryManager(alp, area));
                }
            }
        }
        for (LibraryStorageAreaCache lsaCache : Lookup.getDefault().lookupAll(LibraryStorageAreaCache.class)) {
            for (ArealLibraryProvider alp : alps) {
                for (URL location : lsaCache.getCachedAreas()) {
                    LibraryStorageArea area = alp.loadArea(location);
                    if (area != null) {
                        assert area.getLocation().equals(location) : "Bad location " + area.getLocation() + " does not match " + location + " from " + alp.getClass().getName();
                        if (locations.add(location)) {
                            managers.add(new LibraryManager(alp, area));
                        }
                    }
                }
            }
        }
        return managers;
    }

    /**
     * Adds PropertyChangeListener on list of open library managers.
     * The listener is notified when list of open library managers changes via
     * {@link #PROP_OPEN_LIBRARY_MANAGERS}.
     * @param listener to be notified
     */
    public static synchronized void addOpenManagersPropertyChangeListener (PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        if (areaProvidersLookupResult == null) {
            areaProvidersLookupResult = Lookup.getDefault().lookupResult(ArealLibraryProvider.class);
            attachListeners(areaProvidersLookupResult.allInstances());
            areaProvidersLookupResult.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    attachListeners(areaProvidersLookupResult.allInstances());
                }
            });
        }
        openLibraryManagerListListeners.addPropertyChangeListener (listener);
    }
    
    private static synchronized void attachListeners(Collection<? extends ArealLibraryProvider> currentProviders) {
        for (ArealLibraryProvider provider : currentAreaProviders) {
            provider.removePropertyChangeListener(AREAL_LIBRARY_PROVIDER_LISTENER);
        }
        for (ArealLibraryProvider provider : currentProviders) {
            provider.addPropertyChangeListener(AREAL_LIBRARY_PROVIDER_LISTENER);
        }
        currentAreaProviders = currentProviders;
    }

    /**
     * Removes PropertyChangeListener
     * @param listener
     */
    public static void removeOpenManagersPropertyChangeListener (PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        openLibraryManagerListListeners.removePropertyChangeListener (listener);
    }
    
    @Override
    public String toString() {
        URL loc = getLocation();
        return "LibraryManager[" + (loc != null ? loc : "default") + "]"; // NOI18N
    }

    @NonNull
    LibraryStorageArea getArea() {
        return area;
    }

} // end LibraryManager

