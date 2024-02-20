#Signature file v4.1
#Version 1.55

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

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

CLSS public final org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<%0 extends java.lang.Object>
meth public <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> runReadActionWhenReady(org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction<{org.netbeans.modules.j2ee.metadata.model.api.MetadataModel%0},{%%0}>) throws java.io.IOException
meth public <%0 extends java.lang.Object> {%%0} runReadAction(org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction<{org.netbeans.modules.j2ee.metadata.model.api.MetadataModel%0},{%%0}>) throws java.io.IOException
meth public boolean isReady()
supr java.lang.Object
hfds impl

CLSS public abstract interface org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction%1} run({org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction%0}) throws java.lang.Exception

CLSS public final org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.Throwable)
supr java.io.IOException

CLSS public final org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory
meth public static <%0 extends java.lang.Object> org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<{%%0}> createMetadataModel(org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation<{%%0}>)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation<%0 extends java.lang.Object>
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> runReadActionWhenReady(org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction<{org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation%0},{%%0}>) throws java.io.IOException
meth public abstract <%0 extends java.lang.Object> {%%0} runReadAction(org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction<{org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelImplementation%0},{%%0}>) throws java.io.IOException
meth public abstract boolean isReady()

