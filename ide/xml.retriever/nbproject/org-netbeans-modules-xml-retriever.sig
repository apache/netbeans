#Signature file v4.1
#Version 1.54

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected init(java.lang.String,int)
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} valueOf(java.lang.Class<{%%0}>,java.lang.String)
supr java.lang.Object

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface java.lang.Runnable
 anno 0 java.lang.FunctionalInterface()
meth public abstract void run()

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

CLSS public abstract interface javax.xml.namespace.NamespaceContext
meth public abstract java.lang.String getNamespaceURI(java.lang.String)
meth public abstract java.lang.String getPrefix(java.lang.String)
meth public abstract java.util.Iterator getPrefixes(java.lang.String)

CLSS public org.netbeans.modules.xml.retriever.DocumentParserFactory
cons public init()
meth public static boolean removeRegisteredParser(org.netbeans.modules.xml.retriever.DocumentTypeParser)
meth public static java.util.List<org.netbeans.modules.xml.retriever.DocumentTypeParser> getRegisteredParsers()
meth public static org.netbeans.modules.xml.retriever.DocumentTypeParser getParser(org.netbeans.modules.xml.retriever.catalog.Utilities$DocumentTypesEnum)
meth public static void addParser(org.netbeans.modules.xml.retriever.DocumentTypeParser)
supr java.lang.Object
hfds registereDocumentTypeParsers

CLSS public abstract interface org.netbeans.modules.xml.retriever.DocumentTypeParser
meth public abstract boolean accept(java.lang.String)
meth public abstract java.util.List<java.lang.String> getAllLocationOfReferencedEntities(java.io.File) throws java.lang.Exception
meth public abstract java.util.List<java.lang.String> getAllLocationOfReferencedEntities(org.openide.filesystems.FileObject) throws java.lang.Exception

CLSS public org.netbeans.modules.xml.retriever.RetrieveEntry
cons public init(java.lang.String,java.lang.String,java.io.File,java.io.File,org.netbeans.modules.xml.retriever.catalog.Utilities$DocumentTypesEnum,boolean)
meth public boolean isRecursive()
meth public java.io.File getLocalBaseFile()
meth public java.io.File getSaveFile()
meth public java.lang.String getBaseAddress()
meth public java.lang.String getCurrentAddress()
meth public java.lang.String getEffectiveAddress()
meth public java.lang.String toString()
meth public org.netbeans.modules.xml.retriever.catalog.Utilities$DocumentTypesEnum getDocType()
meth public void setBaseAddress(java.lang.String)
meth public void setDocType(org.netbeans.modules.xml.retriever.catalog.Utilities$DocumentTypesEnum)
meth public void setEffectiveAddress(java.lang.String)
meth public void setLocalBaseFile(java.io.File)
meth public void setRecursive(boolean)
meth public void setSaveFile(java.io.File)
supr java.lang.Object
hfds baseAddress,currentAddress,docType,effectiveAddress,localBaseFile,recursive,saveFile

CLSS public abstract org.netbeans.modules.xml.retriever.Retriever
cons public init()
meth public abstract java.io.File getProjectCatalog()
 anno 0 java.lang.Deprecated()
meth public abstract java.io.File retrieveResource(java.io.File,java.net.URI) throws java.io.IOException,java.net.URISyntaxException
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Map<org.netbeans.modules.xml.retriever.RetrieveEntry,java.lang.Exception> getRetrievedResourceExceptionMap()
meth public abstract org.openide.filesystems.FileObject retrieveResource(org.openide.filesystems.FileObject,java.net.URI) throws java.io.IOException,java.net.URISyntaxException
meth public abstract org.openide.filesystems.FileObject retrieveResource(org.openide.filesystems.FileObject,java.net.URI,java.net.URI) throws java.io.IOException,java.net.URISyntaxException
meth public abstract org.openide.filesystems.FileObject retrieveResourceClosureIntoSingleDirectory(org.openide.filesystems.FileObject,java.net.URI) throws java.io.IOException,java.net.URISyntaxException
meth public abstract void setOverwriteFilesWithSameName(boolean)
meth public abstract void setRecursiveRetrieve(boolean)
meth public static javax.swing.JPanel getCertificationPanel(java.security.cert.X509Certificate)
meth public static org.netbeans.modules.xml.retriever.Retriever getDefault()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.xml.retriever.RetrieverEngine
cons public init()
intf java.lang.Runnable
meth public abstract void addResourceToRetrieve(org.netbeans.modules.xml.retriever.RetrieveEntry)
meth public abstract void setFileOverwrite(boolean)
meth public abstract void start()
meth public static org.netbeans.modules.xml.retriever.RetrieverEngine getRetrieverEngine(java.io.File)
meth public static org.netbeans.modules.xml.retriever.RetrieverEngine getRetrieverEngine(java.io.File,boolean)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.xml.retriever.XMLCatalogProvider
fld public final static java.lang.String TYPE_RETRIEVED = "retrieved"
meth public abstract java.net.URI getCatalog(org.openide.filesystems.FileObject)
meth public abstract java.net.URI getProjectWideCatalog()

CLSS public final !enum org.netbeans.modules.xml.retriever.catalog.CatalogAttribute
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute catalog
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute id
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute name
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute originalResourcePointer
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute prefer
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute publicId
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute publicIdStartString
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute referencingFiles
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute rewritePrefix
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute systemId
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute systemIdStartString
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute uri
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute uriStartString
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute xmlns
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute xprojectCatalogFileLocation
meth public static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.retriever.catalog.CatalogAttribute[] values()
supr java.lang.Enum<org.netbeans.modules.xml.retriever.catalog.CatalogAttribute>

CLSS public final !enum org.netbeans.modules.xml.retriever.catalog.CatalogElement
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogElement Public
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogElement catalog
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogElement delegatePublic
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogElement delegateSystem
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogElement delegateURI
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogElement group
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogElement nextCatalog
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogElement rewriteSystem
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogElement rewriteURI
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogElement system
fld public final static org.netbeans.modules.xml.retriever.catalog.CatalogElement uri
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.retriever.catalog.CatalogElement valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.retriever.catalog.CatalogElement[] values()
supr java.lang.Enum<org.netbeans.modules.xml.retriever.catalog.CatalogElement>

CLSS public abstract interface org.netbeans.modules.xml.retriever.catalog.CatalogEntry
meth public abstract boolean isValid()
meth public abstract java.lang.String getSource()
meth public abstract java.lang.String getTarget()
meth public abstract java.util.HashMap<java.lang.String,java.lang.String> getExtraAttributeMap()
meth public abstract org.netbeans.modules.xml.retriever.catalog.CatalogElement getEntryType()

CLSS public abstract interface org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel
fld public final static java.lang.String CATALOG_FILE_EXTENSION = ".xml"
fld public final static java.lang.String PUBLIC_CATALOG_FILE_NAME = "catalog"
intf org.netbeans.modules.xml.xam.locator.CatalogModel
meth public abstract boolean isWellformed()
meth public abstract java.net.URI searchURI(java.net.URI)
meth public abstract java.util.Collection<org.netbeans.modules.xml.retriever.catalog.CatalogEntry> getCatalogEntries()
meth public abstract org.netbeans.modules.xml.xam.Model$State getState()
meth public abstract org.openide.filesystems.FileObject getCatalogFileObject()
meth public abstract void addNextCatalog(java.net.URI,boolean) throws java.io.IOException
meth public abstract void addPropertychangeListener(java.beans.PropertyChangeListener)
meth public abstract void addURI(java.net.URI,java.net.URI) throws java.io.IOException
meth public abstract void addURI(java.net.URI,org.openide.filesystems.FileObject) throws java.io.IOException
meth public abstract void removeNextCatalog(java.net.URI) throws java.io.IOException
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removeURI(java.net.URI) throws java.io.IOException

CLSS public abstract org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory
cons public init()
meth public abstract org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel getCatalogWriteModelForCatalogFile(org.openide.filesystems.FileObject) throws org.netbeans.modules.xml.xam.locator.CatalogModelException
meth public abstract org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel getCatalogWriteModelForProject(org.openide.filesystems.FileObject) throws org.netbeans.modules.xml.xam.locator.CatalogModelException
meth public static org.netbeans.modules.xml.retriever.catalog.CatalogWriteModelFactory getInstance()
supr org.netbeans.modules.xml.xam.locator.CatalogModelFactory
hfds implObj

CLSS public org.netbeans.modules.xml.retriever.catalog.LSResourceResolverFactory
cons public init()
meth public static org.w3c.dom.ls.LSResourceResolver getDefault()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.xml.retriever.catalog.ProjectCatalogSupport
cons public init()
meth public abstract boolean isProjectProtocol(java.net.URI)
meth public abstract boolean removeCatalogEntry(java.net.URI) throws java.io.IOException
meth public abstract java.net.URI constructProjectProtocol(org.openide.filesystems.FileObject)
meth public abstract java.net.URI createCatalogEntry(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject) throws java.io.IOException,org.netbeans.modules.xml.xam.locator.CatalogModelException
meth public abstract org.openide.filesystems.FileObject resolveProjectProtocol(java.net.URI)
supr java.lang.Object

CLSS public org.netbeans.modules.xml.retriever.catalog.Utilities
cons public init()
fld public final static java.lang.String DEFAULT_PRIVATE_CAHCE_URI_STR = "private/cache/retriever"
fld public final static java.lang.String DEFAULT_PRIVATE_CATALOG_URI_STR = "private/cache/retriever/catalog.xml"
fld public final static java.lang.String NO_NAME_SPACE = "NO_NAME_SPACE"
fld public final static java.lang.String PRIVATE_CAHCE_URI_STR = "retriever"
fld public final static java.lang.String PRIVATE_CATALOG_URI_STR = "retriever/catalog.xml"
innr public final static !enum DocumentTypesEnum
innr public final static HashNamespaceResolver
meth protected static javax.swing.text.Document _getDocument(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static boolean localResourceExists(java.net.URL)
meth public static boolean retrieveAndCache(java.net.URI,org.openide.filesystems.FileObject)
meth public static int countPushdownFolders(java.net.URI,java.net.URI)
meth public static java.io.File downloadURLAndSave(java.net.URL,java.io.File) throws java.io.IOException
meth public static java.io.File downloadURLUsingProxyAndSave(java.net.URL,java.net.Proxy,java.io.File) throws java.io.IOException
meth public static java.io.File toFile(java.net.URL)
meth public static java.io.InputStream getInputStreamOfURL(java.net.URL,java.net.Proxy) throws java.io.IOException
meth public static java.lang.String encodedByte(int)
meth public static java.lang.String normalizeURI(java.lang.String)
meth public static java.lang.String relativize(java.net.URI,java.net.URI)
meth public static java.net.URL appendURL(java.net.URL,java.lang.String)
meth public static java.util.List<java.io.File> getFilesWithExtension(java.io.File,java.lang.String,java.util.List<java.io.File>)
meth public static java.util.List<java.lang.String> runXPathQuery(java.io.File,java.lang.String) throws java.lang.Exception
meth public static java.util.List<org.openide.filesystems.FileObject> getFilesOfNSInProj(org.netbeans.api.project.Project,org.netbeans.modules.xml.retriever.catalog.Utilities$DocumentTypesEnum,java.lang.String,java.util.List<java.lang.String>)
meth public static java.util.List<org.openide.filesystems.FileObject> getFilesOfNoNSInProj(org.netbeans.api.project.Project,org.netbeans.modules.xml.retriever.catalog.Utilities$DocumentTypesEnum,java.util.List<java.lang.String>)
meth public static java.util.Map<org.openide.filesystems.FileObject,java.lang.String> getFiles2NSMappingInProj(java.io.File,org.netbeans.modules.xml.retriever.catalog.Utilities$DocumentTypesEnum)
meth public static java.util.Map<org.openide.filesystems.FileObject,java.lang.String> getFiles2NSMappingInProj(org.netbeans.api.project.Project,org.netbeans.modules.xml.retriever.catalog.Utilities$DocumentTypesEnum,java.util.List<java.lang.String>)
meth public static javax.swing.text.Document getDocument(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.xml.retriever.catalog.CatalogWriteModel getTestCatalogWriteModel() throws java.io.IOException
meth public static org.netbeans.modules.xml.xam.ModelSource createModelSource(org.openide.filesystems.FileObject,boolean) throws org.netbeans.modules.xml.xam.locator.CatalogModelException
meth public static org.netbeans.modules.xml.xam.ModelSource getModelSource(org.openide.filesystems.FileObject,boolean)
meth public static org.netbeans.modules.xml.xam.locator.CatalogModel createCatalogModel(org.openide.filesystems.FileObject) throws org.netbeans.modules.xml.xam.locator.CatalogModelException
meth public static org.netbeans.modules.xml.xam.locator.CatalogModel getCatalogModel(org.netbeans.modules.xml.xam.ModelSource) throws org.netbeans.modules.xml.xam.locator.CatalogModelException
meth public static org.openide.filesystems.FileObject getFileObject(org.netbeans.modules.xml.xam.ModelSource)
meth public static org.openide.filesystems.FileObject getProjectCatalogFileObject(org.netbeans.api.project.Project) throws java.io.IOException
meth public static void deleteRecursively(java.io.File)
supr java.lang.Object
hfds firstRoot,logger,namespaces,prefixes,testCatalogModel

CLSS public final static !enum org.netbeans.modules.xml.retriever.catalog.Utilities$DocumentTypesEnum
 outer org.netbeans.modules.xml.retriever.catalog.Utilities
fld public final static org.netbeans.modules.xml.retriever.catalog.Utilities$DocumentTypesEnum schema
fld public final static org.netbeans.modules.xml.retriever.catalog.Utilities$DocumentTypesEnum wsdl
meth public java.lang.String toString()
meth public static org.netbeans.modules.xml.retriever.catalog.Utilities$DocumentTypesEnum valueOf(java.lang.String)
meth public static org.netbeans.modules.xml.retriever.catalog.Utilities$DocumentTypesEnum[] values()
supr java.lang.Enum<org.netbeans.modules.xml.retriever.catalog.Utilities$DocumentTypesEnum>

CLSS public final static org.netbeans.modules.xml.retriever.catalog.Utilities$HashNamespaceResolver
 outer org.netbeans.modules.xml.retriever.catalog.Utilities
cons public init(java.util.Map<java.lang.String,java.lang.String>)
cons public init(java.util.Map<java.lang.String,java.lang.String>,java.util.Map<java.lang.String,java.lang.String>)
intf javax.xml.namespace.NamespaceContext
meth public java.lang.String getNamespaceURI(java.lang.String)
meth public java.lang.String getPrefix(java.lang.String)
meth public java.util.Iterator getPrefixes(java.lang.String)
supr java.lang.Object
hfds namespaces,prefixes

CLSS public abstract interface org.netbeans.modules.xml.xam.locator.CatalogModel
intf org.w3c.dom.ls.LSResourceResolver
intf org.xml.sax.EntityResolver
meth public abstract org.netbeans.modules.xml.xam.ModelSource getModelSource(java.net.URI) throws org.netbeans.modules.xml.xam.locator.CatalogModelException
meth public abstract org.netbeans.modules.xml.xam.ModelSource getModelSource(java.net.URI,org.netbeans.modules.xml.xam.ModelSource) throws org.netbeans.modules.xml.xam.locator.CatalogModelException

CLSS public abstract org.netbeans.modules.xml.xam.locator.CatalogModelFactory
cons public init()
innr public static Default
meth public abstract org.netbeans.modules.xml.xam.locator.CatalogModel getCatalogModel(org.netbeans.modules.xml.xam.ModelSource) throws org.netbeans.modules.xml.xam.locator.CatalogModelException
meth public abstract org.w3c.dom.ls.LSResourceResolver getLSResourceResolver()
meth public static org.netbeans.modules.xml.xam.locator.CatalogModelFactory getDefault()
supr java.lang.Object
hfds implObj

CLSS public abstract interface org.w3c.dom.ls.LSResourceResolver
meth public abstract org.w3c.dom.ls.LSInput resolveResource(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)

CLSS public abstract interface org.xml.sax.EntityResolver
meth public abstract org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException

