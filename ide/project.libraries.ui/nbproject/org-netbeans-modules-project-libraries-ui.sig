#Signature file v4.1
#Version 1.75

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public final org.netbeans.api.project.libraries.LibrariesCustomizer
meth public static boolean showCustomizer(org.netbeans.api.project.libraries.Library)
meth public static boolean showCustomizer(org.netbeans.api.project.libraries.Library,org.netbeans.api.project.libraries.LibraryManager)
meth public static boolean showSingleLibraryCustomizer(org.netbeans.api.project.libraries.Library)
meth public static org.netbeans.api.project.libraries.Library showCreateNewLibraryCustomizer(org.netbeans.api.project.libraries.LibraryManager)
supr java.lang.Object

CLSS public final org.netbeans.api.project.libraries.Library
fld public final static java.lang.String PROP_CONTENT = "content"
fld public final static java.lang.String PROP_DESCRIPTION = "description"
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_PROPERTIES = "properties"
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getDescription()
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public java.util.List<java.net.URI> getURIContent(java.lang.String)
meth public java.util.List<java.net.URL> getContent(java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.String> getProperties()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.project.libraries.LibraryManager getManager()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds LOG,impl,listener,listeners,manager

CLSS public final org.netbeans.api.project.libraries.LibraryChooser
innr public abstract interface static Filter
innr public abstract interface static LibraryImportHandler
innr public abstract interface static Panel
meth public static java.util.Set<org.netbeans.api.project.libraries.Library> showDialog(org.netbeans.api.project.libraries.LibraryManager,org.netbeans.api.project.libraries.LibraryChooser$Filter,org.netbeans.api.project.libraries.LibraryChooser$LibraryImportHandler)
meth public static org.netbeans.api.project.libraries.LibraryChooser$Panel createPanel(org.netbeans.api.project.libraries.LibraryManager,org.netbeans.api.project.libraries.LibraryChooser$Filter)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.api.project.libraries.LibraryChooser$Filter
 outer org.netbeans.api.project.libraries.LibraryChooser
meth public abstract boolean accept(org.netbeans.api.project.libraries.Library)

CLSS public abstract interface static org.netbeans.api.project.libraries.LibraryChooser$LibraryImportHandler
 outer org.netbeans.api.project.libraries.LibraryChooser
meth public abstract org.netbeans.api.project.libraries.Library importLibrary(org.netbeans.api.project.libraries.Library) throws java.io.IOException

CLSS public abstract interface static org.netbeans.api.project.libraries.LibraryChooser$Panel
 outer org.netbeans.api.project.libraries.LibraryChooser
fld public final static java.lang.String PROP_SELECTED_LIBRARIES = "selectedLibraries"
meth public abstract java.awt.Component getVisualComponent()
meth public abstract java.util.Set<org.netbeans.api.project.libraries.Library> getSelectedLibraries()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public final org.netbeans.api.project.libraries.LibraryManager
fld public final static java.lang.String PROP_LIBRARIES = "libraries"
fld public final static java.lang.String PROP_OPEN_LIBRARY_MANAGERS = "openManagers"
meth public java.lang.String getDisplayName()
meth public java.lang.String toString()
meth public java.net.URL getLocation()
meth public org.netbeans.api.project.libraries.Library createLibrary(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.util.List<java.net.URL>>) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.project.libraries.Library createLibrary(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.util.List<java.net.URL>>,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.project.libraries.Library createLibrary(java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.util.List<java.net.URL>>) throws java.io.IOException
meth public org.netbeans.api.project.libraries.Library createURILibrary(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.util.List<java.net.URI>>) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.project.libraries.Library createURILibrary(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.util.List<java.net.URI>>,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.project.libraries.Library createURILibrary(java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.util.List<java.net.URI>>) throws java.io.IOException
meth public org.netbeans.api.project.libraries.Library getLibrary(java.lang.String)
meth public org.netbeans.api.project.libraries.Library[] getLibraries()
meth public static java.util.Collection<org.netbeans.api.project.libraries.LibraryManager> getOpenManagers()
meth public static org.netbeans.api.project.libraries.LibraryManager forLocation(java.net.URL)
meth public static org.netbeans.api.project.libraries.LibraryManager getDefault()
meth public static void addOpenManagersPropertyChangeListener(java.beans.PropertyChangeListener)
meth public static void removeOpenManagersPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addLibrary(org.netbeans.api.project.libraries.Library) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeLibrary(org.netbeans.api.project.libraries.Library) throws java.io.IOException
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds AREAL_LIBRARY_PROVIDER_LISTENER,alp,area,areaProvidersLookupResult,cache,currentAreaProviders,currentStorages,eventId,instance,listeners,lookupListener,openLibraryManagerListListeners,plistener,result

CLSS public abstract interface org.netbeans.spi.project.libraries.ArealLibraryProvider<%0 extends org.netbeans.spi.project.libraries.LibraryStorageArea, %1 extends org.netbeans.spi.project.libraries.LibraryImplementation2>
fld public final static java.lang.String PROP_OPEN_AREAS = "openAreas"
meth public abstract java.lang.Class<{org.netbeans.spi.project.libraries.ArealLibraryProvider%0}> areaType()
meth public abstract java.lang.Class<{org.netbeans.spi.project.libraries.ArealLibraryProvider%1}> libraryType()
meth public abstract java.util.Set<{org.netbeans.spi.project.libraries.ArealLibraryProvider%0}> getOpenAreas()
meth public abstract org.netbeans.spi.project.libraries.LibraryProvider<{org.netbeans.spi.project.libraries.ArealLibraryProvider%1}> getLibraries({org.netbeans.spi.project.libraries.ArealLibraryProvider%0})
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void remove({org.netbeans.spi.project.libraries.ArealLibraryProvider%1}) throws java.io.IOException
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract {org.netbeans.spi.project.libraries.ArealLibraryProvider%0} createArea()
meth public abstract {org.netbeans.spi.project.libraries.ArealLibraryProvider%0} loadArea(java.net.URL)
meth public abstract {org.netbeans.spi.project.libraries.ArealLibraryProvider%1} createLibrary(java.lang.String,java.lang.String,{org.netbeans.spi.project.libraries.ArealLibraryProvider%0},java.util.Map<java.lang.String,java.util.List<java.net.URI>>) throws java.io.IOException

CLSS public org.netbeans.spi.project.libraries.LibraryCustomizerContext
cons public init(org.netbeans.spi.project.libraries.LibraryImplementation,org.netbeans.spi.project.libraries.LibraryStorageArea)
meth public org.netbeans.spi.project.libraries.LibraryImplementation getLibraryImplementation()
meth public org.netbeans.spi.project.libraries.LibraryImplementation2 getLibraryImplementation2()
meth public org.netbeans.spi.project.libraries.LibraryStorageArea getLibraryStorageArea()
supr java.lang.Object
hfds libraryImplementation,libraryStorageArea

CLSS public org.netbeans.spi.project.libraries.LibraryFactory
meth public static org.netbeans.api.project.libraries.Library createLibrary(org.netbeans.spi.project.libraries.LibraryImplementation)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.project.libraries.LibraryImplementation
fld public final static java.lang.String PROP_CONTENT = "content"
fld public final static java.lang.String PROP_DESCRIPTION = "description"
fld public final static java.lang.String PROP_NAME = "name"
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getLocalizingBundle()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getType()
meth public abstract java.util.List<java.net.URL> getContent(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setContent(java.lang.String,java.util.List<java.net.URL>)
meth public abstract void setDescription(java.lang.String)
meth public abstract void setLocalizingBundle(java.lang.String)
meth public abstract void setName(java.lang.String)

CLSS public abstract interface org.netbeans.spi.project.libraries.LibraryImplementation2
intf org.netbeans.spi.project.libraries.LibraryImplementation
meth public abstract java.util.List<java.net.URI> getURIContent(java.lang.String)
meth public abstract void setURIContent(java.lang.String,java.util.List<java.net.URI>)

CLSS public abstract interface org.netbeans.spi.project.libraries.LibraryImplementation3
fld public final static java.lang.String PROP_PROPERTIES = "properties"
intf org.netbeans.spi.project.libraries.NamedLibraryImplementation
meth public abstract java.util.Map<java.lang.String,java.lang.String> getProperties()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setProperties(java.util.Map<java.lang.String,java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.project.libraries.LibraryProvider<%0 extends org.netbeans.spi.project.libraries.LibraryImplementation>
fld public final static java.lang.String PROP_LIBRARIES = "libraries"
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract {org.netbeans.spi.project.libraries.LibraryProvider%0}[] getLibraries()

CLSS public abstract interface org.netbeans.spi.project.libraries.LibraryStorageArea
fld public final static org.netbeans.spi.project.libraries.LibraryStorageArea GLOBAL
meth public abstract java.lang.String getDisplayName()
meth public abstract java.net.URL getLocation()

CLSS public abstract interface org.netbeans.spi.project.libraries.LibraryStorageAreaCache
meth public abstract java.util.Collection<? extends java.net.URL> getCachedAreas()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.project.libraries.LibraryTypeProvider
intf org.openide.util.Lookup$Provider
meth public abstract java.beans.Customizer getCustomizer(java.lang.String)
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getLibraryType()
meth public abstract java.lang.String[] getSupportedVolumeTypes()
meth public abstract org.netbeans.spi.project.libraries.LibraryImplementation createLibrary()
meth public abstract void libraryCreated(org.netbeans.spi.project.libraries.LibraryImplementation)
meth public abstract void libraryDeleted(org.netbeans.spi.project.libraries.LibraryImplementation)

CLSS public abstract interface org.netbeans.spi.project.libraries.NamedLibraryImplementation
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
intf org.netbeans.spi.project.libraries.LibraryImplementation
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract void setDisplayName(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()

CLSS public abstract interface org.netbeans.spi.project.libraries.WritableLibraryProvider<%0 extends org.netbeans.spi.project.libraries.LibraryImplementation>
intf org.netbeans.spi.project.libraries.LibraryProvider<{org.netbeans.spi.project.libraries.WritableLibraryProvider%0}>
meth public abstract boolean addLibrary({org.netbeans.spi.project.libraries.WritableLibraryProvider%0}) throws java.io.IOException
meth public abstract boolean removeLibrary({org.netbeans.spi.project.libraries.WritableLibraryProvider%0}) throws java.io.IOException
meth public abstract boolean updateLibrary({org.netbeans.spi.project.libraries.WritableLibraryProvider%0},{org.netbeans.spi.project.libraries.WritableLibraryProvider%0}) throws java.io.IOException

CLSS public abstract org.openide.util.Lookup
cons public init()
fld public final static org.openide.util.Lookup EMPTY
innr public abstract interface static Provider
innr public abstract static Item
innr public abstract static Result
innr public final static Template
meth public <%0 extends java.lang.Object> java.util.Collection<? extends {%%0}> lookupAll(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookupResult(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public static org.openide.util.Lookup getDefault()
supr java.lang.Object
hfds LOG,defaultLookup,defaultLookupProvider
hcls DefLookup,Empty

CLSS public abstract interface static org.openide.util.Lookup$Provider
 outer org.openide.util.Lookup
meth public abstract org.openide.util.Lookup getLookup()

