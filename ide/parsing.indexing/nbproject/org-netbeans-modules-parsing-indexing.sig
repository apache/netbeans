#Signature file v4.1
#Version 9.32.0

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

CLSS public final org.netbeans.modules.parsing.api.indexing.IndexingManager
meth public !varargs void refreshAllIndices(boolean,boolean,java.io.File[])
meth public !varargs void refreshAllIndices(boolean,boolean,org.openide.filesystems.FileObject[])
meth public !varargs void refreshAllIndices(org.openide.filesystems.FileObject[])
meth public <%0 extends java.lang.Object> {%%0} runProtected(java.util.concurrent.Callable<{%%0}>) throws java.lang.Exception
meth public boolean isIndexing()
meth public static org.netbeans.modules.parsing.api.indexing.IndexingManager getDefault()
meth public void refreshAllIndices(java.lang.String)
meth public void refreshIndex(java.net.URL,java.util.Collection<? extends java.net.URL>)
meth public void refreshIndex(java.net.URL,java.util.Collection<? extends java.net.URL>,boolean)
meth public void refreshIndex(java.net.URL,java.util.Collection<? extends java.net.URL>,boolean,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public void refreshIndexAndWait(java.net.URL,java.util.Collection<? extends java.net.URL>)
meth public void refreshIndexAndWait(java.net.URL,java.util.Collection<? extends java.net.URL>,boolean)
meth public void refreshIndexAndWait(java.net.URL,java.util.Collection<? extends java.net.URL>,boolean,boolean)
supr java.lang.Object
hfds LOG,instance

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.BinaryIndexer
cons public init()
meth protected abstract void index(org.netbeans.modules.parsing.spi.indexing.Context)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.BinaryIndexerFactory
cons public init()
meth public abstract int getIndexVersion()
meth public abstract java.lang.String getIndexerName()
meth public abstract org.netbeans.modules.parsing.spi.indexing.BinaryIndexer createIndexer()
meth public abstract void rootsRemoved(java.lang.Iterable<? extends java.net.URL>)
meth public boolean scanStarted(org.netbeans.modules.parsing.spi.indexing.Context)
meth public void scanFinished(org.netbeans.modules.parsing.spi.indexing.Context)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.ConstrainedBinaryIndexer
cons public init()
innr public abstract interface static !annotation Registration
meth protected abstract void index(java.util.Map<java.lang.String,? extends java.lang.Iterable<? extends org.openide.filesystems.FileObject>>,org.netbeans.modules.parsing.spi.indexing.Context)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth protected boolean scanStarted(org.netbeans.modules.parsing.spi.indexing.Context)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth protected void rootsRemoved(java.lang.Iterable<? extends java.net.URL>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth protected void scanFinished(org.netbeans.modules.parsing.spi.indexing.Context)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract interface static !annotation org.netbeans.modules.parsing.spi.indexing.ConstrainedBinaryIndexer$Registration
 outer org.netbeans.modules.parsing.spi.indexing.ConstrainedBinaryIndexer
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String namePattern()
meth public abstract !hasdefault java.lang.String[] mimeType()
meth public abstract !hasdefault java.lang.String[] requiredResource()
meth public abstract int indexVersion()
meth public abstract java.lang.String indexerName()

CLSS public final org.netbeans.modules.parsing.spi.indexing.Context
meth public boolean checkForEditorModifications()
meth public boolean isAllFilesIndexing()
meth public boolean isCancelled()
meth public boolean isSourceForBinaryRootIndexing()
meth public boolean isSupplementaryFilesIndexing()
meth public java.net.URL getRootURI()
meth public org.netbeans.modules.parsing.spi.indexing.SuspendStatus getSuspendStatus()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.filesystems.FileObject getIndexFolder()
meth public org.openide.filesystems.FileObject getRoot()
meth public void addSupplementaryFiles(java.net.URL,java.util.Collection<? extends java.net.URL>)
supr java.lang.Object
hfds allFilesJob,cancelRequest,checkForEditorModifications,factory,followUpJob,indexBaseFolder,indexBaseFolderFactory,indexFolder,indexerName,indexerVersion,indexingSupport,logContext,props,root,rootURL,sourceForBinaryRoot,suspendedStatus

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.CustomIndexer
cons public init()
meth protected abstract void index(java.lang.Iterable<? extends org.netbeans.modules.parsing.spi.indexing.Indexable>,org.netbeans.modules.parsing.spi.indexing.Context)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory
cons public init()
meth public abstract boolean supportsEmbeddedIndexers()
meth public abstract org.netbeans.modules.parsing.spi.indexing.CustomIndexer createIndexer()
supr org.netbeans.modules.parsing.spi.indexing.SourceIndexerFactory

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer
cons public init()
meth protected abstract void index(org.netbeans.modules.parsing.spi.indexing.Indexable,org.netbeans.modules.parsing.spi.Parser$Result,org.netbeans.modules.parsing.spi.indexing.Context)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory
cons public init()
meth public abstract org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer createIndexer(org.netbeans.modules.parsing.spi.indexing.Indexable,org.netbeans.modules.parsing.api.Snapshot)
supr org.netbeans.modules.parsing.spi.indexing.SourceIndexerFactory

CLSS public org.netbeans.modules.parsing.spi.indexing.ErrorsCache
innr public abstract interface static Convertor
innr public final static !enum ErrorKind
meth public static <%0 extends java.lang.Object> void setErrors(java.net.URL,org.netbeans.modules.parsing.spi.indexing.Indexable,java.lang.Iterable<? extends {%%0}>,org.netbeans.modules.parsing.spi.indexing.ErrorsCache$Convertor<{%%0}>)
meth public static boolean isInError(org.openide.filesystems.FileObject,boolean)
meth public static java.util.Collection<? extends java.net.URL> getAllFilesInError(java.net.URL) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.parsing.spi.indexing.ErrorsCache$Convertor<%0 extends java.lang.Object>
 outer org.netbeans.modules.parsing.spi.indexing.ErrorsCache
meth public abstract int getLineNumber({org.netbeans.modules.parsing.spi.indexing.ErrorsCache$Convertor%0})
meth public abstract java.lang.String getMessage({org.netbeans.modules.parsing.spi.indexing.ErrorsCache$Convertor%0})
meth public abstract org.netbeans.modules.parsing.spi.indexing.ErrorsCache$ErrorKind getKind({org.netbeans.modules.parsing.spi.indexing.ErrorsCache$Convertor%0})

CLSS public final static !enum org.netbeans.modules.parsing.spi.indexing.ErrorsCache$ErrorKind
 outer org.netbeans.modules.parsing.spi.indexing.ErrorsCache
fld public final static org.netbeans.modules.parsing.spi.indexing.ErrorsCache$ErrorKind ERROR
fld public final static org.netbeans.modules.parsing.spi.indexing.ErrorsCache$ErrorKind ERROR_NO_BADGE
fld public final static org.netbeans.modules.parsing.spi.indexing.ErrorsCache$ErrorKind WARNING
meth public static org.netbeans.modules.parsing.spi.indexing.ErrorsCache$ErrorKind valueOf(java.lang.String)
meth public static org.netbeans.modules.parsing.spi.indexing.ErrorsCache$ErrorKind[] values()
supr java.lang.Enum<org.netbeans.modules.parsing.spi.indexing.ErrorsCache$ErrorKind>

CLSS public abstract interface org.netbeans.modules.parsing.spi.indexing.IndexabilityQueryImplementation
innr public final static IndexabilityQueryContext
meth public abstract int getVersion()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getStateIdentifier()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public boolean preventIndexing(org.netbeans.modules.parsing.spi.indexing.IndexabilityQueryImplementation$IndexabilityQueryContext)

CLSS public final static org.netbeans.modules.parsing.spi.indexing.IndexabilityQueryImplementation$IndexabilityQueryContext
 outer org.netbeans.modules.parsing.spi.indexing.IndexabilityQueryImplementation
meth public java.lang.String getIndexerName()
meth public java.net.URL getIndexable()
meth public java.net.URL getRoot()
supr java.lang.Object
hfds indexable,indexerName,root
hcls Accessor

CLSS public final org.netbeans.modules.parsing.spi.indexing.Indexable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getMimeType()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getRelativePath()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String toString()
meth public java.net.URL getURL()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object
hfds delegate
hcls MyAccessor

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.PathRecognizer
cons public init()
meth public abstract java.util.Set<java.lang.String> getBinaryLibraryPathIds()
meth public abstract java.util.Set<java.lang.String> getLibraryPathIds()
meth public abstract java.util.Set<java.lang.String> getMimeTypes()
meth public abstract java.util.Set<java.lang.String> getSourcePathIds()
supr java.lang.Object

CLSS public abstract interface !annotation org.netbeans.modules.parsing.spi.indexing.PathRecognizerRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] binaryLibraryPathIds()
meth public abstract !hasdefault java.lang.String[] libraryPathIds()
meth public abstract !hasdefault java.lang.String[] mimeTypes()
meth public abstract !hasdefault java.lang.String[] sourcePathIds()

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.SourceIndexerFactory
cons public init()
meth public abstract int getIndexVersion()
meth public abstract java.lang.String getIndexerName()
meth public abstract void filesDeleted(java.lang.Iterable<? extends org.netbeans.modules.parsing.spi.indexing.Indexable>,org.netbeans.modules.parsing.spi.indexing.Context)
meth public abstract void filesDirty(java.lang.Iterable<? extends org.netbeans.modules.parsing.spi.indexing.Indexable>,org.netbeans.modules.parsing.spi.indexing.Context)
meth public boolean scanStarted(org.netbeans.modules.parsing.spi.indexing.Context)
meth public int getPriority()
meth public void rootsRemoved(java.lang.Iterable<? extends java.net.URL>)
meth public void scanFinished(org.netbeans.modules.parsing.spi.indexing.Context)
supr java.lang.Object

CLSS public final org.netbeans.modules.parsing.spi.indexing.SuspendStatus
meth public boolean isSuspendSupported()
meth public boolean isSuspended()
meth public void parkWhileSuspended() throws java.lang.InterruptedException
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.modules.parsing.spi.indexing.support.IndexDocument
meth public void addPair(java.lang.String,java.lang.String,boolean,boolean)
supr java.lang.Object
hfds spi

CLSS public final org.netbeans.modules.parsing.spi.indexing.support.IndexResult
meth public java.lang.String getRelativePath()
meth public java.lang.String getValue(java.lang.String)
meth public java.lang.String[] getValues(java.lang.String)
meth public java.net.URL getRoot()
meth public java.net.URL getUrl()
meth public org.netbeans.modules.parsing.spi.indexing.Indexable getIndexable()
meth public org.openide.filesystems.FileObject getFile()
supr java.lang.Object
hfds LOG,cachedFile,cachedUrl,root,spi

CLSS public final org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport
meth public boolean isValid()
meth public org.netbeans.modules.parsing.spi.indexing.support.IndexDocument createDocument(org.netbeans.modules.parsing.spi.indexing.Indexable)
meth public org.netbeans.modules.parsing.spi.indexing.support.IndexDocument createDocument(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static boolean isIndexingTask(org.netbeans.modules.parsing.api.Task)
meth public static org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport getInstance(org.netbeans.modules.parsing.spi.indexing.Context) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void addDocument(org.netbeans.modules.parsing.spi.indexing.support.IndexDocument)
meth public void markDirtyDocuments(org.netbeans.modules.parsing.spi.indexing.Indexable)
meth public void removeDocuments(org.netbeans.modules.parsing.spi.indexing.Indexable)
supr java.lang.Object
hfds LOG,context,spiFactory,spiIndex

CLSS public final org.netbeans.modules.parsing.spi.indexing.support.QuerySupport
innr public final static !enum Kind
innr public final static Query
meth public !varargs java.util.Collection<? extends org.netbeans.modules.parsing.spi.indexing.support.IndexResult> query(java.lang.String,java.lang.String,org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind,java.lang.String[]) throws java.io.IOException
meth public !varargs static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport forRoots(java.lang.String,int,java.net.URL[]) throws java.io.IOException
meth public !varargs static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport forRoots(java.lang.String,int,org.openide.filesystems.FileObject[]) throws java.io.IOException
meth public org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Query$Factory getQueryFactory()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.Collection<org.openide.filesystems.FileObject> findDependentRoots(org.openide.filesystems.FileObject,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.Collection<org.openide.filesystems.FileObject> findRoots(org.netbeans.api.project.Project,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.util.Collection<org.openide.filesystems.FileObject> findRoots(org.openide.filesystems.FileObject,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.Map<org.netbeans.api.project.Project,java.util.Collection<org.openide.filesystems.FileObject>> findRoots(java.util.Collection<? extends org.netbeans.api.project.Project>,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds LOG,indexerQuery,roots
hcls DocumentToResultConvertor,IndexerQuery

CLSS public final static !enum org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind
 outer org.netbeans.modules.parsing.spi.indexing.support.QuerySupport
fld public final static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind CAMEL_CASE
fld public final static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind CASE_INSENSITIVE_CAMEL_CASE
fld public final static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind CASE_INSENSITIVE_PREFIX
fld public final static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind CASE_INSENSITIVE_REGEXP
fld public final static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind EXACT
fld public final static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind PREFIX
fld public final static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind REGEXP
meth public static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind valueOf(java.lang.String)
meth public static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind[] values()
supr java.lang.Enum<org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind>

CLSS public final static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Query
 outer org.netbeans.modules.parsing.spi.indexing.support.QuerySupport
innr public final static Factory
meth public !varargs java.util.Collection<? extends org.netbeans.modules.parsing.spi.indexing.support.IndexResult> execute(java.lang.String[]) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public java.lang.String toString()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds qs,queryImpl

CLSS public final static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Query$Factory
 outer org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Query
meth public !varargs org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Query and(org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Query[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public !varargs org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Query or(org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Query[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Query field(java.lang.String,java.lang.String,org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Kind)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Query file(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Query file(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Query$Factory setCamelCasePart(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.modules.parsing.spi.indexing.support.QuerySupport$Query$Factory setCamelCaseSeparator(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds camelCasePart,camelCaseSeparator,qs

