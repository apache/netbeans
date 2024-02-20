#Signature file v4.1
#Version 1.33

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

CLSS public org.netbeans.modules.javascript2.knockout.index.KnockoutCustomElement
cons public init(java.lang.String,java.lang.String,java.util.Collection<java.lang.String>,java.net.URL,int)
meth public int getOffset()
meth public java.lang.String getFqn()
meth public java.lang.String getName()
meth public java.net.URL getDeclarationFile()
meth public java.util.List<java.lang.String> getParameters()
supr java.lang.Object
hfds fqn,name,offset,parameters,url

CLSS public org.netbeans.modules.javascript2.knockout.index.KnockoutIndex
meth public java.util.Collection<java.lang.String> getCustomElementParameters(java.lang.String)
meth public java.util.Collection<org.netbeans.modules.javascript2.knockout.index.KnockoutCustomElement> getCustomElements(java.lang.String,boolean)
meth public static org.netbeans.modules.javascript2.knockout.index.KnockoutIndex get(org.netbeans.api.project.Project) throws java.io.IOException
supr java.lang.Object
hfds INDEXES,querySupport

CLSS public org.netbeans.modules.javascript2.knockout.index.KnockoutIndexer
cons public init()
fld public final static java.lang.String CUSTOM_ELEMENT = "ce"
innr public final static Factory
meth protected void index(org.netbeans.modules.parsing.spi.indexing.Indexable,org.netbeans.modules.parsing.spi.Parser$Result,org.netbeans.modules.parsing.spi.indexing.Context)
meth public static boolean isScannerThread()
meth public static void addCustomElement(java.net.URI,org.netbeans.modules.javascript2.knockout.index.KnockoutCustomElement)
supr org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer
hfds CUSTOM_ELEMENTS,LOG

CLSS public final static org.netbeans.modules.javascript2.knockout.index.KnockoutIndexer$Factory
 outer org.netbeans.modules.javascript2.knockout.index.KnockoutIndexer
cons public init()
fld public final static int VERSION = 1
fld public final static java.lang.String NAME = "knockoutjs"
meth public boolean scanStarted(org.netbeans.modules.parsing.spi.indexing.Context)
meth public int getIndexVersion()
meth public int getPriority()
meth public java.lang.String getIndexerName()
meth public org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer createIndexer(org.netbeans.modules.parsing.spi.indexing.Indexable,org.netbeans.modules.parsing.api.Snapshot)
meth public static boolean isScannerThread()
meth public static void addPostScanTask(java.lang.Runnable)
meth public void filesDeleted(java.lang.Iterable<? extends org.netbeans.modules.parsing.spi.indexing.Indexable>,org.netbeans.modules.parsing.spi.indexing.Context)
meth public void filesDirty(java.lang.Iterable<? extends org.netbeans.modules.parsing.spi.indexing.Indexable>,org.netbeans.modules.parsing.spi.indexing.Context)
meth public void scanFinished(org.netbeans.modules.parsing.spi.indexing.Context)
supr org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory
hfds PRIORITY,postScanTasks

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer
cons public init()
meth protected abstract void index(org.netbeans.modules.parsing.spi.indexing.Indexable,org.netbeans.modules.parsing.spi.Parser$Result,org.netbeans.modules.parsing.spi.indexing.Context)
supr java.lang.Object

CLSS public abstract org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory
cons public init()
meth public abstract org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer createIndexer(org.netbeans.modules.parsing.spi.indexing.Indexable,org.netbeans.modules.parsing.api.Snapshot)
supr org.netbeans.modules.parsing.spi.indexing.SourceIndexerFactory

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

