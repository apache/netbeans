#Signature file v4.1
#Version 2.59.0

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

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

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

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

CLSS public java.lang.Throwable
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
intf java.io.Serializable
meth public final java.lang.Throwable[] getSuppressed()
meth public final void addSuppressed(java.lang.Throwable)
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.parsing.lucene.spi.ScanSuspendImplementation
meth public abstract void resume()
meth public abstract void suspend()

CLSS public abstract interface org.netbeans.modules.parsing.lucene.support.Convertor<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {org.netbeans.modules.parsing.lucene.support.Convertor%1} convert({org.netbeans.modules.parsing.lucene.support.Convertor%0})

CLSS public final org.netbeans.modules.parsing.lucene.support.Convertors
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object> org.netbeans.modules.parsing.lucene.support.Convertor<{%%0},{%%2}> compose(org.netbeans.modules.parsing.lucene.support.Convertor<? super {%%0},? extends {%%1}>,org.netbeans.modules.parsing.lucene.support.Convertor<? super {%%1},? extends {%%2}>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.netbeans.modules.parsing.lucene.support.Convertor<{%%0},{%%1}> firstNonNull(java.lang.Iterable<? extends org.netbeans.modules.parsing.lucene.support.Convertor<? super {%%0},? extends {%%1}>>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static <%0 extends java.lang.Object> org.netbeans.modules.parsing.lucene.support.Convertor<{%%0},{%%0}> identity()
supr java.lang.Object
hfds IDENTITY
hcls CompositeConvertor,FirstNonNull

CLSS public abstract interface org.netbeans.modules.parsing.lucene.support.DocumentIndex
innr public abstract interface static Transactional
meth public abstract !varargs java.util.Collection<? extends org.netbeans.modules.parsing.lucene.support.IndexDocument> findByPrimaryKey(java.lang.String,org.netbeans.modules.parsing.lucene.support.Queries$QueryKind,java.lang.String[]) throws java.io.IOException,java.lang.InterruptedException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract !varargs java.util.Collection<? extends org.netbeans.modules.parsing.lucene.support.IndexDocument> query(java.lang.String,java.lang.String,org.netbeans.modules.parsing.lucene.support.Queries$QueryKind,java.lang.String[]) throws java.io.IOException,java.lang.InterruptedException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract java.util.Collection<? extends java.lang.String> getDirtyKeys()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.parsing.lucene.support.Index$Status getStatus() throws java.io.IOException
meth public abstract void addDocument(org.netbeans.modules.parsing.lucene.support.IndexDocument)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void close() throws java.io.IOException
meth public abstract void markKeyDirty(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeDirtyKeys(java.util.Collection<? extends java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeDocument(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void store(boolean) throws java.io.IOException

CLSS public abstract interface static org.netbeans.modules.parsing.lucene.support.DocumentIndex$Transactional
 outer org.netbeans.modules.parsing.lucene.support.DocumentIndex
intf org.netbeans.modules.parsing.lucene.support.DocumentIndex
meth public abstract void clear() throws java.io.IOException
meth public abstract void commit() throws java.io.IOException
meth public abstract void rollback() throws java.io.IOException
meth public abstract void txStore() throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.parsing.lucene.support.DocumentIndex2
innr public abstract interface static Transactional
intf org.netbeans.modules.parsing.lucene.support.DocumentIndex
meth public abstract !varargs <%0 extends java.lang.Object> java.util.Collection<? extends {%%0}> query(org.apache.lucene.search.Query,org.netbeans.modules.parsing.lucene.support.Convertor<? super org.netbeans.modules.parsing.lucene.support.IndexDocument,? extends {%%0}>,java.lang.String[]) throws java.io.IOException,java.lang.InterruptedException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()

CLSS public abstract interface static org.netbeans.modules.parsing.lucene.support.DocumentIndex2$Transactional
 outer org.netbeans.modules.parsing.lucene.support.DocumentIndex2
intf org.netbeans.modules.parsing.lucene.support.DocumentIndex$Transactional
intf org.netbeans.modules.parsing.lucene.support.DocumentIndex2

CLSS public abstract interface org.netbeans.modules.parsing.lucene.support.DocumentIndexCache
innr public abstract interface static WithCustomIndexDocument
meth public abstract boolean addDocument(org.netbeans.modules.parsing.lucene.support.IndexDocument)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract boolean removeDocument(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Collection<? extends java.lang.String> getRemovedKeys()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Collection<? extends org.netbeans.modules.parsing.lucene.support.IndexDocument> getAddedDocuments()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void clear()

CLSS public abstract interface static org.netbeans.modules.parsing.lucene.support.DocumentIndexCache$WithCustomIndexDocument
 outer org.netbeans.modules.parsing.lucene.support.DocumentIndexCache
intf org.netbeans.modules.parsing.lucene.support.DocumentIndexCache
meth public abstract org.netbeans.modules.parsing.lucene.support.Convertor<org.apache.lucene.document.Document,org.netbeans.modules.parsing.lucene.support.IndexDocument> createQueryConvertor()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.parsing.lucene.support.Convertor<org.netbeans.modules.parsing.lucene.support.IndexDocument,org.apache.lucene.document.Document> createAddConvertor()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.parsing.lucene.support.Index
innr public abstract interface static Transactional
innr public abstract interface static WithTermFrequencies
innr public final static !enum Status
innr public final static IndexClosedException
meth public abstract !varargs <%0 extends java.lang.Object, %1 extends java.lang.Object> void queryDocTerms(java.util.Map<? super {%%1},java.util.Set<{%%0}>>,org.netbeans.modules.parsing.lucene.support.Convertor<? super org.apache.lucene.document.Document,{%%1}>,org.netbeans.modules.parsing.lucene.support.Convertor<? super org.apache.lucene.index.Term,{%%0}>,org.apache.lucene.document.FieldSelector,java.util.concurrent.atomic.AtomicBoolean,org.apache.lucene.search.Query[]) throws java.io.IOException,java.lang.InterruptedException
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
 anno 6 org.netbeans.api.annotations.common.NonNull()
meth public abstract !varargs <%0 extends java.lang.Object> void query(java.util.Collection<? super {%%0}>,org.netbeans.modules.parsing.lucene.support.Convertor<? super org.apache.lucene.document.Document,{%%0}>,org.apache.lucene.document.FieldSelector,java.util.concurrent.atomic.AtomicBoolean,org.apache.lucene.search.Query[]) throws java.io.IOException,java.lang.InterruptedException
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> void store(java.util.Collection<{%%1}>,java.util.Collection<{%%0}>,org.netbeans.modules.parsing.lucene.support.Convertor<? super {%%1},? extends org.apache.lucene.document.Document>,org.netbeans.modules.parsing.lucene.support.Convertor<? super {%%0},? extends org.apache.lucene.search.Query>,boolean) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public abstract <%0 extends java.lang.Object> void queryTerms(java.util.Collection<? super {%%0}>,org.apache.lucene.index.Term,org.netbeans.modules.parsing.lucene.support.StoppableConvertor<org.apache.lucene.index.Term,{%%0}>,java.util.concurrent.atomic.AtomicBoolean) throws java.io.IOException,java.lang.InterruptedException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract org.netbeans.modules.parsing.lucene.support.Index$Status getStatus(boolean) throws java.io.IOException
meth public abstract void clear() throws java.io.IOException
meth public abstract void close() throws java.io.IOException

CLSS public final static org.netbeans.modules.parsing.lucene.support.Index$IndexClosedException
 outer org.netbeans.modules.parsing.lucene.support.Index
cons public init()
supr java.io.IOException

CLSS public final static !enum org.netbeans.modules.parsing.lucene.support.Index$Status
 outer org.netbeans.modules.parsing.lucene.support.Index
fld public final static org.netbeans.modules.parsing.lucene.support.Index$Status EMPTY
fld public final static org.netbeans.modules.parsing.lucene.support.Index$Status INVALID
fld public final static org.netbeans.modules.parsing.lucene.support.Index$Status VALID
fld public final static org.netbeans.modules.parsing.lucene.support.Index$Status WRITING
meth public static org.netbeans.modules.parsing.lucene.support.Index$Status valueOf(java.lang.String)
meth public static org.netbeans.modules.parsing.lucene.support.Index$Status[] values()
supr java.lang.Enum<org.netbeans.modules.parsing.lucene.support.Index$Status>

CLSS public abstract interface static org.netbeans.modules.parsing.lucene.support.Index$Transactional
 outer org.netbeans.modules.parsing.lucene.support.Index
intf org.netbeans.modules.parsing.lucene.support.Index
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> void txStore(java.util.Collection<{%%1}>,java.util.Collection<{%%0}>,org.netbeans.modules.parsing.lucene.support.Convertor<? super {%%1},? extends org.apache.lucene.document.Document>,org.netbeans.modules.parsing.lucene.support.Convertor<? super {%%0},? extends org.apache.lucene.search.Query>) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public abstract void commit() throws java.io.IOException
meth public abstract void rollback() throws java.io.IOException

CLSS public abstract interface static org.netbeans.modules.parsing.lucene.support.Index$WithTermFrequencies
 outer org.netbeans.modules.parsing.lucene.support.Index
innr public final static TermFreq
intf org.netbeans.modules.parsing.lucene.support.Index
meth public abstract <%0 extends java.lang.Object> void queryTermFrequencies(java.util.Collection<? super {%%0}>,org.apache.lucene.index.Term,org.netbeans.modules.parsing.lucene.support.StoppableConvertor<org.netbeans.modules.parsing.lucene.support.Index$WithTermFrequencies$TermFreq,{%%0}>,java.util.concurrent.atomic.AtomicBoolean) throws java.io.IOException,java.lang.InterruptedException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()

CLSS public final static org.netbeans.modules.parsing.lucene.support.Index$WithTermFrequencies$TermFreq
 outer org.netbeans.modules.parsing.lucene.support.Index$WithTermFrequencies
meth public int getFreq()
meth public org.apache.lucene.index.Term getTerm()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds freq,term

CLSS public abstract interface org.netbeans.modules.parsing.lucene.support.IndexDocument
meth public abstract java.lang.String getPrimaryKey()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getValue(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String[] getValues(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addPair(java.lang.String,java.lang.String,boolean,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.modules.parsing.lucene.support.IndexManager
innr public abstract interface static Action
meth public static <%0 extends java.lang.Object> {%%0} priorityAccess(org.netbeans.modules.parsing.lucene.support.IndexManager$Action<{%%0}>) throws java.io.IOException,java.lang.InterruptedException
meth public static <%0 extends java.lang.Object> {%%0} readAccess(org.netbeans.modules.parsing.lucene.support.IndexManager$Action<{%%0}>) throws java.io.IOException,java.lang.InterruptedException
 anno 0 java.lang.Deprecated()
meth public static <%0 extends java.lang.Object> {%%0} writeAccess(org.netbeans.modules.parsing.lucene.support.IndexManager$Action<{%%0}>) throws java.io.IOException,java.lang.InterruptedException
 anno 0 java.lang.Deprecated()
meth public static boolean holdsWriteLock()
meth public static java.util.Map<java.io.File,org.netbeans.modules.parsing.lucene.support.Index> getOpenIndexes()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.parsing.lucene.support.DocumentIndex createDocumentIndex(java.io.File) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.parsing.lucene.support.DocumentIndex createDocumentIndex(java.io.File,boolean) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.parsing.lucene.support.DocumentIndex createDocumentIndex(java.io.File,org.netbeans.modules.parsing.lucene.support.DocumentIndexCache) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.parsing.lucene.support.DocumentIndex createDocumentIndex(org.netbeans.modules.parsing.lucene.support.Index)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.parsing.lucene.support.DocumentIndex createDocumentIndex(org.netbeans.modules.parsing.lucene.support.Index,org.netbeans.modules.parsing.lucene.support.DocumentIndexCache)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.parsing.lucene.support.DocumentIndex$Transactional createTransactionalDocumentIndex(java.io.File) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.parsing.lucene.support.DocumentIndex$Transactional createTransactionalDocumentIndex(java.io.File,org.netbeans.modules.parsing.lucene.support.DocumentIndexCache) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.parsing.lucene.support.DocumentIndex$Transactional createTransactionalDocumentIndex(org.netbeans.modules.parsing.lucene.support.Index$Transactional)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.parsing.lucene.support.DocumentIndex$Transactional createTransactionalDocumentIndex(org.netbeans.modules.parsing.lucene.support.Index$Transactional,org.netbeans.modules.parsing.lucene.support.DocumentIndexCache)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.parsing.lucene.support.Index createIndex(java.io.File,org.apache.lucene.analysis.Analyzer) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.parsing.lucene.support.Index createIndex(java.io.File,org.apache.lucene.analysis.Analyzer,boolean) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.parsing.lucene.support.Index createMemoryIndex(org.apache.lucene.analysis.Analyzer) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.parsing.lucene.support.Index$Transactional createTransactionalIndex(java.io.File,org.apache.lucene.analysis.Analyzer) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.parsing.lucene.support.Index$Transactional createTransactionalIndex(java.io.File,org.apache.lucene.analysis.Analyzer,boolean) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.parsing.lucene.support.IndexDocument createDocument(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds factory,indexes,lock,lookupListener,res,scanSuspendImpls
hcls Ref,SupportAccessorImpl

CLSS public abstract interface static org.netbeans.modules.parsing.lucene.support.IndexManager$Action<%0 extends java.lang.Object>
 outer org.netbeans.modules.parsing.lucene.support.IndexManager
meth public abstract {org.netbeans.modules.parsing.lucene.support.IndexManager$Action%0} run() throws java.io.IOException,java.lang.InterruptedException

CLSS public abstract interface org.netbeans.modules.parsing.lucene.support.IndexReaderInjection
meth public abstract void setIndexReader(org.apache.lucene.index.IndexReader)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()

CLSS public final org.netbeans.modules.parsing.lucene.support.LowMemoryWatcher
meth public boolean isLowMemory()
meth public static org.netbeans.modules.parsing.lucene.support.LowMemoryWatcher getInstance()
meth public void free()
meth public void free(boolean)
supr java.lang.Object
hfds LOG,LOGGER_RATE,instance,strategy,testEnforcesLowMemory
hcls DefaultStrategy

CLSS public final org.netbeans.modules.parsing.lucene.support.Queries
fld public final static java.lang.String OPTION_CAMEL_CASE_PART = "camelCasePart"
fld public final static java.lang.String OPTION_CAMEL_CASE_SEPARATOR = "camelCaseSeparator"
innr public final static !enum QueryKind
meth public !varargs static org.apache.lucene.document.FieldSelector createFieldSelector(java.lang.String[])
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static boolean isCamelCase(java.lang.String,java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.lang.String createCamelCaseRegExp(java.lang.String,java.lang.String,java.lang.String,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.apache.lucene.search.Query createQuery(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.parsing.lucene.support.Queries$QueryKind)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static org.apache.lucene.search.Query createQuery(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.parsing.lucene.support.Queries$QueryKind,java.util.Map<java.lang.String,java.lang.Object>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public static org.apache.lucene.search.Query createTermCollectingQuery(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.parsing.lucene.support.Queries$QueryKind)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static org.apache.lucene.search.Query createTermCollectingQuery(java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.parsing.lucene.support.Queries$QueryKind,java.util.Map<java.lang.String,java.lang.Object>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds CAMEL_CASE_FORMAT,DEFAULT_CAMEL_CASE_PART_CASE_INSENSITIVE,DEFAULT_CAMEL_CASE_PART_CASE_SENSITIVE,DEFAULT_CAMEL_CASE_PATTERN,DEFAULT_CAMEL_CASE_SEPARATOR,cache
hcls AbstractTCFilter,FieldSelectorImpl,HasFieldFilter,PrefixFilter,QueryFactory,RegexpFilter,RegexpTermEnum,StandardQueryFactory,TCBooleanQuery,TCFilter,TCFilteredQuery,TCQueryFactory,TermFilter

CLSS public final static !enum org.netbeans.modules.parsing.lucene.support.Queries$QueryKind
 outer org.netbeans.modules.parsing.lucene.support.Queries
fld public final static org.netbeans.modules.parsing.lucene.support.Queries$QueryKind CAMEL_CASE
fld public final static org.netbeans.modules.parsing.lucene.support.Queries$QueryKind CASE_INSENSITIVE_CAMEL_CASE
fld public final static org.netbeans.modules.parsing.lucene.support.Queries$QueryKind CASE_INSENSITIVE_PREFIX
fld public final static org.netbeans.modules.parsing.lucene.support.Queries$QueryKind CASE_INSENSITIVE_REGEXP
fld public final static org.netbeans.modules.parsing.lucene.support.Queries$QueryKind EXACT
fld public final static org.netbeans.modules.parsing.lucene.support.Queries$QueryKind PREFIX
fld public final static org.netbeans.modules.parsing.lucene.support.Queries$QueryKind REGEXP
meth public static org.netbeans.modules.parsing.lucene.support.Queries$QueryKind valueOf(java.lang.String)
meth public static org.netbeans.modules.parsing.lucene.support.Queries$QueryKind[] values()
supr java.lang.Enum<org.netbeans.modules.parsing.lucene.support.Queries$QueryKind>

CLSS public abstract interface org.netbeans.modules.parsing.lucene.support.StoppableConvertor<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public final static Stop
meth public abstract {org.netbeans.modules.parsing.lucene.support.StoppableConvertor%1} convert({org.netbeans.modules.parsing.lucene.support.StoppableConvertor%0}) throws org.netbeans.modules.parsing.lucene.support.StoppableConvertor$Stop

CLSS public final static org.netbeans.modules.parsing.lucene.support.StoppableConvertor$Stop
 outer org.netbeans.modules.parsing.lucene.support.StoppableConvertor
cons public init()
supr java.lang.Exception

