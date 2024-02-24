#Signature file v4.1
#Version 2.41

CLSS public abstract interface java.io.Serializable

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

CLSS public final org.netbeans.modules.profiler.api.icons.Icons
cons public init()
innr public abstract interface static Keys
meth public static java.awt.Image getImage(java.lang.String)
meth public static java.lang.String getResource(java.lang.String)
meth public static javax.swing.Icon getIcon(java.lang.String)
meth public static javax.swing.ImageIcon getImageIcon(java.lang.String)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.profiler.api.icons.Icons$Keys
 outer org.netbeans.modules.profiler.api.icons.Icons

CLSS public final org.netbeans.modules.profiler.oql.engine.api.OQLEngine
cons public init(org.netbeans.lib.profiler.heap.Heap)
innr public abstract interface static ObjectVisitor
innr public abstract static OQLQuery
meth public boolean isCancelled()
meth public java.lang.Object unwrapJavaObject(java.lang.Object)
meth public java.lang.Object unwrapJavaObject(java.lang.Object,boolean)
meth public org.netbeans.lib.profiler.heap.Heap getHeap()
meth public org.netbeans.modules.profiler.oql.engine.api.OQLEngine$OQLQuery parseQuery(java.lang.String) throws org.netbeans.modules.profiler.oql.engine.api.OQLException
meth public static boolean isOQLSupported()
meth public void cancelQuery() throws org.netbeans.modules.profiler.oql.engine.api.OQLException
meth public void executeQuery(java.lang.String,org.netbeans.modules.profiler.oql.engine.api.OQLEngine$ObjectVisitor) throws org.netbeans.modules.profiler.oql.engine.api.OQLException
supr java.lang.Object
hfds LOGGER,delegate,heap

CLSS public abstract static org.netbeans.modules.profiler.oql.engine.api.OQLEngine$OQLQuery
 outer org.netbeans.modules.profiler.oql.engine.api.OQLEngine
cons public init()
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.profiler.oql.engine.api.OQLEngine$ObjectVisitor
 outer org.netbeans.modules.profiler.oql.engine.api.OQLEngine
fld public final static org.netbeans.modules.profiler.oql.engine.api.OQLEngine$ObjectVisitor DEFAULT
meth public abstract boolean visit(java.lang.Object)

CLSS public final org.netbeans.modules.profiler.oql.engine.api.OQLException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public final org.netbeans.modules.profiler.oql.engine.api.ReferenceChain
cons public init(org.netbeans.lib.profiler.heap.Heap,java.lang.Object,org.netbeans.modules.profiler.oql.engine.api.ReferenceChain)
meth public boolean contains(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public int getDepth()
meth public int hashCode()
meth public java.lang.Object getObj()
meth public java.lang.String toString()
meth public org.netbeans.modules.profiler.oql.engine.api.ReferenceChain getNext()
supr java.lang.Object
hfds TYPE_CLASS,TYPE_INSTANCE,heap,id,next,obj,type

CLSS public abstract interface org.netbeans.modules.profiler.oql.icons.OQLIcons
fld public final static java.lang.String OQL = "OQLIcons.OQL"
intf org.netbeans.modules.profiler.api.icons.Icons$Keys

CLSS public final org.netbeans.modules.profiler.oql.repository.api.OQLQueryCategory
meth public java.lang.String getDescription()
meth public java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String toString()
meth public java.util.List<? extends org.netbeans.modules.profiler.oql.repository.api.OQLQueryDefinition> listQueries()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.List<? extends org.netbeans.modules.profiler.oql.repository.api.OQLQueryDefinition> listQueries(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds description,id,name,repository

CLSS public final org.netbeans.modules.profiler.oql.repository.api.OQLQueryDefinition
cons public init(java.lang.String,java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getContent()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getDescription()
meth public java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String toString()
supr java.lang.Object
hfds content,description,name

CLSS public final org.netbeans.modules.profiler.oql.repository.api.OQLQueryRepository
meth public java.util.List<? extends org.netbeans.modules.profiler.oql.repository.api.OQLQueryCategory> listCategories()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.List<? extends org.netbeans.modules.profiler.oql.repository.api.OQLQueryCategory> listCategories(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.util.List<? extends org.netbeans.modules.profiler.oql.repository.api.OQLQueryDefinition> listQueries()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.List<? extends org.netbeans.modules.profiler.oql.repository.api.OQLQueryDefinition> listQueries(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.util.List<? extends org.netbeans.modules.profiler.oql.repository.api.OQLQueryDefinition> listQueries(org.netbeans.modules.profiler.oql.repository.api.OQLQueryCategory)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.util.List<? extends org.netbeans.modules.profiler.oql.repository.api.OQLQueryDefinition> listQueries(org.netbeans.modules.profiler.oql.repository.api.OQLQueryCategory,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.profiler.oql.repository.api.OQLQueryRepository getInstance()
supr java.lang.Object
hfds LOGGER,MATCH_ALL
hcls Singleton

CLSS public abstract org.netbeans.modules.profiler.oql.spi.OQLEditorImpl
cons public init()
fld public final static java.lang.String VALIDITY_PROPERTY = "document#valid"
innr public abstract interface static ValidationCallback
meth protected final static org.netbeans.modules.profiler.oql.engine.api.OQLEngine getEngine(javax.swing.text.Document)
meth protected final static org.netbeans.modules.profiler.oql.spi.OQLEditorImpl$ValidationCallback getValidationCallback(javax.swing.text.Document)
meth public abstract javax.swing.JEditorPane getEditorPane()
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.profiler.oql.spi.OQLEditorImpl$ValidationCallback
 outer org.netbeans.modules.profiler.oql.spi.OQLEditorImpl
meth public abstract void callback(boolean)

