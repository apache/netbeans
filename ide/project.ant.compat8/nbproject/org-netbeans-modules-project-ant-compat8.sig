#Signature file v4.1
#Version 1.90

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

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

CLSS public abstract interface java.lang.annotation.Annotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation java.lang.annotation.Documented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Retention
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.RetentionPolicy value()

CLSS public abstract interface !annotation java.lang.annotation.Target
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.ElementType[] value()

CLSS public abstract java.util.AbstractMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init()
innr public static SimpleEntry
innr public static SimpleImmutableEntry
intf java.util.Map<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>> entrySet()
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.String toString()
meth public java.util.Collection<{java.util.AbstractMap%1}> values()
meth public java.util.Set<{java.util.AbstractMap%0}> keySet()
meth public void clear()
meth public void putAll(java.util.Map<? extends {java.util.AbstractMap%0},? extends {java.util.AbstractMap%1}>)
meth public {java.util.AbstractMap%1} get(java.lang.Object)
meth public {java.util.AbstractMap%1} put({java.util.AbstractMap%0},{java.util.AbstractMap%1})
meth public {java.util.AbstractMap%1} remove(java.lang.Object)
supr java.lang.Object

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface java.util.Map<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public abstract interface static Entry
meth public abstract boolean containsKey(java.lang.Object)
meth public abstract boolean containsValue(java.lang.Object)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.util.Collection<{java.util.Map%1}> values()
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.Map%0},{java.util.Map%1}>> entrySet()
meth public abstract java.util.Set<{java.util.Map%0}> keySet()
meth public abstract void clear()
meth public abstract void putAll(java.util.Map<? extends {java.util.Map%0},? extends {java.util.Map%1}>)
meth public abstract {java.util.Map%1} get(java.lang.Object)
meth public abstract {java.util.Map%1} put({java.util.Map%0},{java.util.Map%1})
meth public abstract {java.util.Map%1} remove(java.lang.Object)
meth public boolean remove(java.lang.Object,java.lang.Object)
meth public boolean replace({java.util.Map%0},{java.util.Map%1},{java.util.Map%1})
meth public void forEach(java.util.function.BiConsumer<? super {java.util.Map%0},? super {java.util.Map%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} compute({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfAbsent({java.util.Map%0},java.util.function.Function<? super {java.util.Map%0},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfPresent({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} getOrDefault(java.lang.Object,{java.util.Map%1})
meth public {java.util.Map%1} merge({java.util.Map%0},{java.util.Map%1},java.util.function.BiFunction<? super {java.util.Map%1},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} putIfAbsent({java.util.Map%0},{java.util.Map%1})
meth public {java.util.Map%1} replace({java.util.Map%0},{java.util.Map%1})

CLSS public abstract interface !annotation org.netbeans.spi.project.support.ant.AntBasedProjectRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String privateName()
meth public abstract !hasdefault java.lang.String sharedName()
meth public abstract java.lang.String iconResource()
meth public abstract java.lang.String privateNamespace()
meth public abstract java.lang.String sharedNamespace()
meth public abstract java.lang.String type()

CLSS public abstract interface org.netbeans.spi.project.support.ant.AntBasedProjectType
meth public abstract java.lang.String getPrimaryConfigurationDataElementName(boolean)
meth public abstract java.lang.String getPrimaryConfigurationDataElementNamespace(boolean)
meth public abstract java.lang.String getType()
meth public abstract org.netbeans.api.project.Project createProject(org.netbeans.spi.project.support.ant.AntProjectHelper) throws java.io.IOException

CLSS public final org.netbeans.spi.project.support.ant.AntProjectEvent
meth public boolean isExpected()
meth public java.lang.String getPath()
meth public org.netbeans.spi.project.support.ant.AntProjectHelper getHelper()
supr java.util.EventObject
hfds expected,path

CLSS public final org.netbeans.spi.project.support.ant.AntProjectHelper
fld public final static java.lang.String PRIVATE_PROPERTIES_PATH = "nbproject/private/private.properties"
fld public final static java.lang.String PRIVATE_XML_PATH = "nbproject/private/private.xml"
fld public final static java.lang.String PROJECT_PROPERTIES_PATH = "nbproject/project.properties"
fld public final static java.lang.String PROJECT_XML_PATH = "nbproject/project.xml"
meth public boolean isSharableProject()
meth public java.io.File resolveFile(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getLibrariesLocation()
meth public java.lang.String resolvePath(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String toString()
meth public org.netbeans.api.project.ant.AntArtifact createSimpleAntArtifact(java.lang.String,java.lang.String,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,java.lang.String)
meth public org.netbeans.api.project.ant.AntArtifact createSimpleAntArtifact(java.lang.String,java.lang.String,org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,java.lang.String,java.lang.String)
meth public org.netbeans.spi.project.AuxiliaryConfiguration createAuxiliaryConfiguration()
meth public org.netbeans.spi.project.AuxiliaryProperties createAuxiliaryProperties()
meth public org.netbeans.spi.project.CacheDirectoryProvider createCacheDirectoryProvider()
meth public org.netbeans.spi.project.support.ant.EditableProperties getProperties(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.spi.project.support.ant.PropertyEvaluator getStandardPropertyEvaluator()
meth public org.netbeans.spi.project.support.ant.PropertyProvider getProjectLibrariesPropertyProvider()
meth public org.netbeans.spi.project.support.ant.PropertyProvider getPropertyProvider(java.lang.String)
meth public org.netbeans.spi.project.support.ant.PropertyProvider getStockPropertyPreprovider()
meth public org.netbeans.spi.queries.FileBuiltQueryImplementation createGlobFileBuiltQuery(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String[],java.lang.String[])
meth public org.netbeans.spi.queries.SharabilityQueryImplementation createSharabilityQuery(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String[],java.lang.String[])
 anno 0 java.lang.Deprecated()
meth public org.netbeans.spi.queries.SharabilityQueryImplementation2 createSharabilityQuery2(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String[],java.lang.String[])
meth public org.openide.filesystems.FileObject getProjectDirectory()
meth public org.openide.filesystems.FileObject resolveFileObject(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.w3c.dom.Element getPrimaryConfigurationData(boolean)
meth public void addAntProjectListener(org.netbeans.spi.project.support.ant.AntProjectListener)
meth public void notifyDeleted()
meth public void putPrimaryConfigurationData(org.w3c.dom.Element,boolean)
meth public void putProperties(java.lang.String,org.netbeans.spi.project.support.ant.EditableProperties)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void removeAntProjectListener(org.netbeans.spi.project.support.ant.AntProjectListener)
meth public void setLibrariesLocation(java.lang.String)
supr java.lang.Object
hfds LOG,NONEXISTENT,PRIVATE_NS,PROJECT_NS,QUIETLY_SWALLOW_XML_LOAD_ERRORS,RP,addedProjectXmlPath,db,dir,fileListener,fileListenerSet,listeners,modifiedMetadataPaths,pendingHook,pendingHookCount,privateXml,privateXmlValid,projectXml,projectXmlValid,properties,saveActions,state,type
hcls ActionImpl,FileListener,RunnableImpl

CLSS public abstract interface org.netbeans.spi.project.support.ant.AntProjectListener
intf java.util.EventListener
meth public abstract void configurationXmlChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)
meth public abstract void propertiesChanged(org.netbeans.spi.project.support.ant.AntProjectEvent)

CLSS public final org.netbeans.spi.project.support.ant.EditableProperties
cons public init()
 anno 0 java.lang.Deprecated()
cons public init(boolean)
cons public init(java.util.Map<java.lang.String,java.lang.String>)
intf java.lang.Cloneable
meth public java.lang.Object clone()
meth public java.lang.String get(java.lang.Object)
meth public java.lang.String getProperty(java.lang.String)
meth public java.lang.String put(java.lang.String,java.lang.String)
meth public java.lang.String setProperty(java.lang.String,java.lang.String)
meth public java.lang.String setProperty(java.lang.String,java.lang.String[])
meth public java.lang.String[] getComment(java.lang.String)
meth public java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.String>> entrySet()
meth public org.netbeans.spi.project.support.ant.EditableProperties cloneProperties()
meth public void load(java.io.InputStream) throws java.io.IOException
meth public void setComment(java.lang.String,java.lang.String[],boolean)
meth public void store(java.io.OutputStream) throws java.io.IOException
supr java.util.AbstractMap<java.lang.String,java.lang.String>
hfds delegate

CLSS public abstract org.netbeans.spi.project.support.ant.FilterPropertyProvider
cons protected init(org.netbeans.spi.project.support.ant.PropertyProvider)
intf org.netbeans.spi.project.support.ant.PropertyProvider
meth protected final void setDelegate(org.netbeans.spi.project.support.ant.PropertyProvider)
meth public final java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public final void addChangeListener(javax.swing.event.ChangeListener)
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds cs,delegate,strongListener,weakListener

CLSS public final org.netbeans.spi.project.support.ant.GeneratedFilesHelper
cons public init(org.netbeans.spi.project.support.ant.AntProjectHelper)
cons public init(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.api.project.ant.AntBuildExtender)
cons public init(org.openide.filesystems.FileObject)
fld public final static int FLAG_MISSING = 2
fld public final static int FLAG_MODIFIED = 4
fld public final static int FLAG_OLD_PROJECT_XML = 8
fld public final static int FLAG_OLD_STYLESHEET = 16
fld public final static int FLAG_UNKNOWN = 32
fld public final static java.lang.String BUILD_IMPL_XML_PATH = "nbproject/build-impl.xml"
fld public final static java.lang.String BUILD_XML_PATH = "build.xml"
meth public boolean refreshBuildScript(java.lang.String,java.net.URL,boolean) throws java.io.IOException
meth public int getBuildScriptState(java.lang.String,java.net.URL)
meth public void generateBuildScriptFromStylesheet(java.lang.String,java.net.URL) throws java.io.IOException
supr java.lang.Object
hfds GENFILES_PROPERTIES_PATH,KEY_SUFFIX_DATA_CRC,KEY_SUFFIX_SCRIPT_CRC,KEY_SUFFIX_STYLESHEET_CRC_PLUS_VERSION,ORACLE_IS_STANDALONE,STYLESHEET_VERSIONS,crcCache,crcCacheTimestampsXorSizes,dir,extender,h
hcls EolFilterOutputStream

CLSS public final org.netbeans.spi.project.support.ant.PathMatcher
cons public init(java.lang.String,java.lang.String,java.io.File)
meth public boolean matches(java.lang.String,boolean)
meth public java.lang.String toString()
meth public java.util.Set<java.io.File> findIncludedRoots()
supr java.lang.Object
hfds base,excludePattern,excludes,includePattern,includes,knownIncludes

CLSS public org.netbeans.spi.project.support.ant.ProjectGenerator
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper createProject(org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public static org.netbeans.spi.project.support.ant.AntProjectHelper createProject(org.openide.filesystems.FileObject,java.lang.String,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.netbeans.spi.project.support.ant.ProjectXmlSavedHook
cons protected init()
meth protected abstract void projectXmlSaved() throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.project.support.ant.PropertyEvaluator
meth public abstract java.lang.String evaluate(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getProperty(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getProperties()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.spi.project.support.ant.PropertyProvider
meth public abstract java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public org.netbeans.spi.project.support.ant.PropertyUtils
meth public !varargs static org.netbeans.spi.project.support.ant.PropertyEvaluator sequentialPropertyEvaluator(org.netbeans.spi.project.support.ant.PropertyProvider,org.netbeans.spi.project.support.ant.PropertyProvider[])
meth public static boolean isUsablePropertyName(java.lang.String)
meth public static java.io.File resolveFile(java.io.File,java.lang.String)
meth public static java.lang.String getUsablePropertyName(java.lang.String)
meth public static java.lang.String relativizeFile(java.io.File,java.io.File)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static java.lang.String[] tokenizePath(java.lang.String)
meth public static org.netbeans.spi.project.support.ant.EditableProperties getGlobalProperties()
meth public static org.netbeans.spi.project.support.ant.PropertyProvider fixedPropertyProvider(java.util.Map<java.lang.String,java.lang.String>)
meth public static org.netbeans.spi.project.support.ant.PropertyProvider globalPropertyProvider()
meth public static org.netbeans.spi.project.support.ant.PropertyProvider propertiesFilePropertyProvider(java.io.File)
meth public static org.netbeans.spi.project.support.ant.PropertyProvider userPropertiesProvider(org.netbeans.spi.project.support.ant.PropertyEvaluator,java.lang.String,java.io.File)
meth public static void putGlobalProperties(org.netbeans.spi.project.support.ant.EditableProperties) throws java.io.IOException
supr java.lang.Object
hfds RELATIVE_SLASH_SEPARATED_PATH,VALID_PROPERTY_NAME,currentGlobalProperties,currentGlobalPropertiesFile,currentGlobalPropertiesLastModified,currentGlobalPropertiesLength,globalPropertyProviders
hcls FilePropertyProvider,FixedPropertyProvider,UserPropertiesProvider

CLSS public final org.netbeans.spi.project.support.ant.ReferenceHelper
cons public init(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.AuxiliaryConfiguration,org.netbeans.spi.project.support.ant.PropertyEvaluator)
innr public final static RawReference
meth public boolean addRawReference(org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference)
meth public boolean addReference(org.netbeans.api.project.ant.AntArtifact)
 anno 0 java.lang.Deprecated()
meth public boolean destroyReference(java.lang.String)
meth public boolean isReferenced(org.netbeans.api.project.ant.AntArtifact,java.net.URI)
meth public boolean removeRawReference(java.lang.String,java.lang.String)
meth public boolean removeReference(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public boolean removeReference(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.Object[] findArtifactAndLocation(java.lang.String)
meth public java.lang.String addReference(org.netbeans.api.project.ant.AntArtifact,java.net.URI)
meth public java.lang.String createExtraForeignFileReferenceAsIs(java.lang.String,java.lang.String)
meth public java.lang.String createForeignFileReference(java.io.File,java.lang.String)
meth public java.lang.String createForeignFileReference(org.netbeans.api.project.ant.AntArtifact)
 anno 0 java.lang.Deprecated()
meth public java.lang.String createForeignFileReferenceAsIs(java.lang.String,java.lang.String)
meth public java.lang.String createLibraryReference(org.netbeans.api.project.libraries.Library,java.lang.String)
meth public org.netbeans.api.project.ant.AntArtifact getForeignFileReferenceAsArtifact(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public org.netbeans.api.project.libraries.Library copyLibrary(org.netbeans.api.project.libraries.Library) throws java.io.IOException
meth public org.netbeans.api.project.libraries.Library findLibrary(java.lang.String)
meth public org.netbeans.api.project.libraries.LibraryManager getProjectLibraryManager()
meth public org.netbeans.spi.project.SubprojectProvider createSubprojectProvider()
meth public org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference getRawReference(java.lang.String,java.lang.String)
meth public org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference[] getRawReferences()
meth public static org.netbeans.api.project.libraries.Library copyLibrary(org.netbeans.api.project.libraries.Library,java.io.File) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.project.libraries.LibraryManager getProjectLibraryManager(org.netbeans.api.project.Project)
meth public void addExtraBaseDirectory(java.lang.String)
meth public void destroyForeignFileReference(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void fixReferences(java.io.File)
meth public void removeExtraBaseDirectory(java.lang.String)
supr java.lang.Object
hfds FOREIGN_FILE_REFERENCE,FOREIGN_FILE_REFERENCE_OLD,FOREIGN_PLAIN_FILE_REFERENCE,LIBRARY_REFERENCE,REFS_NAME,REFS_NS,REFS_NS2,REF_NAME,aux,eval,extraBaseDirectories,h

CLSS public final static org.netbeans.spi.project.support.ant.ReferenceHelper$RawReference
 outer org.netbeans.spi.project.support.ant.ReferenceHelper
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Properties)
cons public init(java.lang.String,java.lang.String,java.net.URI,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getArtifactType()
meth public java.lang.String getCleanTargetName()
meth public java.lang.String getForeignProjectName()
meth public java.lang.String getID()
meth public java.lang.String getScriptLocationValue()
meth public java.lang.String getTargetName()
meth public java.lang.String toString()
meth public java.net.URI getScriptLocation()
 anno 0 java.lang.Deprecated()
meth public java.util.Properties getProperties()
meth public org.netbeans.api.project.ant.AntArtifact toAntArtifact(org.netbeans.spi.project.support.ant.ReferenceHelper)
supr java.lang.Object
hfds SUB_ELEMENT_NAMES,artifactID,artifactType,cleanTargetName,foreignProjectName,newScriptLocation,props,scriptLocation,targetName

CLSS public abstract org.netbeans.spi.project.support.ant.ReferenceHelperCompat
cons public init()
meth public org.netbeans.api.project.libraries.LibraryChooser$LibraryImportHandler getLibraryChooserImportHandler()
meth public org.netbeans.api.project.libraries.LibraryChooser$LibraryImportHandler getLibraryChooserImportHandler(java.net.URL)
supr java.lang.Object
hfds referenceHelperHandler,uriHandler

CLSS public final org.netbeans.spi.project.support.ant.SourcesHelper
cons public init(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator)
cons public init(org.netbeans.spi.project.support.ant.AntProjectHelper,org.netbeans.spi.project.support.ant.PropertyEvaluator)
 anno 0 java.lang.Deprecated()
innr public final SourceRootConfig
meth public org.netbeans.api.project.Sources createSources()
meth public org.netbeans.spi.project.SourceGroupModifierImplementation createSourceGroupModifierImplementation()
meth public org.netbeans.spi.project.support.ant.SourcesHelper$SourceRootConfig sourceRoot(java.lang.String)
meth public void addNonSourceRoot(java.lang.String)
meth public void addOwnedFile(java.lang.String)
meth public void addPrincipalSourceRoot(java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon)
 anno 0 java.lang.Deprecated()
meth public void addPrincipalSourceRoot(java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon)
 anno 0 java.lang.Deprecated()
meth public void addTypedSourceRoot(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon)
 anno 0 java.lang.Deprecated()
meth public void addTypedSourceRoot(java.lang.String,java.lang.String,java.lang.String,javax.swing.Icon,javax.swing.Icon)
 anno 0 java.lang.Deprecated()
meth public void registerExternalRoots(int)
meth public void registerExternalRoots(int,boolean)
supr java.lang.Object
hfds LOG,aph,evaluator,knownSources,lastRegisteredRoots,minimalSubfolders,nonSourceRoots,ownedFiles,principalSourceRoots,project,propChangeL,registeredRootAlgorithm,typedSourceRoots
hcls Key,PropChangeL,Root,SourceGroupModifierImpl,SourceRoot,SourcesImpl,TypedSourceRoot

CLSS public final org.netbeans.spi.project.support.ant.SourcesHelper$SourceRootConfig
 outer org.netbeans.spi.project.support.ant.SourcesHelper
meth public !varargs org.netbeans.spi.project.support.ant.SourcesHelper$SourceRootConfig inParts(java.lang.String[])
meth public org.netbeans.spi.project.support.ant.SourcesHelper$SourceRootConfig add()
meth public org.netbeans.spi.project.support.ant.SourcesHelper$SourceRootConfig displayName(java.lang.String)
meth public org.netbeans.spi.project.support.ant.SourcesHelper$SourceRootConfig excludes(java.lang.String)
meth public org.netbeans.spi.project.support.ant.SourcesHelper$SourceRootConfig hint(java.lang.String)
meth public org.netbeans.spi.project.support.ant.SourcesHelper$SourceRootConfig icon(javax.swing.Icon)
meth public org.netbeans.spi.project.support.ant.SourcesHelper$SourceRootConfig includes(java.lang.String)
meth public org.netbeans.spi.project.support.ant.SourcesHelper$SourceRootConfig openedIcon(javax.swing.Icon)
meth public org.netbeans.spi.project.support.ant.SourcesHelper$SourceRootConfig type(java.lang.String)
supr java.lang.Object
hfds displayName,excludes,hint,icon,includes,location,openedIcon,parts,type

CLSS public abstract interface !annotation org.openide.modules.PatchFor
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
fld public final static java.lang.String MANIFEST_FRAGMENT_HOST = "OpenIDE-Module-Fragment-Host"
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?> value()

