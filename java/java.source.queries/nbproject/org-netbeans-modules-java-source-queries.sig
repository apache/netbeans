#Signature file v4.1
#Version 1.41

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

CLSS public abstract interface org.netbeans.modules.java.source.queries.api.Function<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {org.netbeans.modules.java.source.queries.api.Function%1} apply({org.netbeans.modules.java.source.queries.api.Function%0}) throws org.netbeans.modules.java.source.queries.api.QueryException

CLSS public org.netbeans.modules.java.source.queries.api.Queries
meth public !varargs final int[] getMethodSpan(java.lang.String,java.lang.String,boolean,java.lang.String,java.lang.String[]) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public final java.lang.String getClassBinaryName(java.lang.String) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final java.lang.String getSuperClass(java.lang.String) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final java.net.URL getURL()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public final java.util.Collection<? extends java.lang.String> getFieldNames(java.lang.String,boolean,java.lang.String) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public final java.util.Collection<? extends java.lang.String> getInterfaces(java.lang.String) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public final java.util.Collection<? extends java.lang.String> getMethodNames(java.lang.String,boolean,java.lang.String,java.lang.String[]) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public final java.util.Collection<? extends java.lang.String> getTopLevelClasses() throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static <%0 extends java.lang.Object> {%%0} query(java.net.URL,org.netbeans.modules.java.source.queries.api.Function<org.netbeans.modules.java.source.queries.api.Queries,{%%0}>) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds ctl,forURL,impl
hcls Accessor

CLSS public org.netbeans.modules.java.source.queries.api.QueryException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public org.netbeans.modules.java.source.queries.api.TemplateWizardFactory
meth public static org.openide.WizardDescriptor$InstantiatingIterator<org.openide.WizardDescriptor> create()
meth public static org.openide.WizardDescriptor$InstantiatingIterator<org.openide.WizardDescriptor> createForSuperClass()
supr java.lang.Object

CLSS public final org.netbeans.modules.java.source.queries.api.Updates
meth public static boolean update(java.net.URL,org.netbeans.modules.java.source.queries.api.Function<org.netbeans.modules.java.source.queries.api.Updates,java.lang.Boolean>) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void fixImports(int[][]) throws org.netbeans.modules.java.source.queries.api.QueryException
meth public void modifyInterfaces(java.lang.String,java.util.Collection<? extends java.lang.String>,java.util.Collection<? extends java.lang.String>) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public void renameField(java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public void setSuperClass(java.lang.String,java.lang.String) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr org.netbeans.modules.java.source.queries.api.Queries

CLSS public abstract interface org.netbeans.modules.java.source.queries.spi.ModelOperations
meth public abstract !varargs int[] getMethodSpan(java.lang.String,java.lang.String,boolean,java.lang.String,java.lang.String[]) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getClassBinaryName(java.lang.String) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getSuperClass(java.lang.String) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Collection<? extends java.lang.String> getFieldNames(java.lang.String,boolean,java.lang.String) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract java.util.Collection<? extends java.lang.String> getInterfaces(java.lang.String) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Collection<? extends java.lang.String> getMethodNames(java.lang.String,boolean,java.lang.String,java.lang.String[]) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract java.util.Collection<? extends java.lang.String> getTopLevelClasses() throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void fixImports(int[][]) throws org.netbeans.modules.java.source.queries.api.QueryException
meth public abstract void modifyInterfaces(java.lang.String,java.util.Collection<? extends java.lang.String>,java.util.Collection<? extends java.lang.String>) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract void renameField(java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setSuperClass(java.lang.String,java.lang.String) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.java.source.queries.spi.QueriesController
innr public final static Context
meth public abstract <%0 extends java.lang.Object> {%%0} runQuery(org.netbeans.modules.java.source.queries.spi.QueriesController$Context<{%%0}>) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract boolean runUpdate(org.netbeans.modules.java.source.queries.spi.QueriesController$Context<java.lang.Boolean>) throws org.netbeans.modules.java.source.queries.api.QueryException
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.modules.java.source.queries.spi.QueriesController$Context<%0 extends java.lang.Object>
 outer org.netbeans.modules.java.source.queries.spi.QueriesController
meth public java.net.URL getURL()
meth public {org.netbeans.modules.java.source.queries.spi.QueriesController$Context%0} execute(org.netbeans.modules.java.source.queries.spi.ModelOperations) throws org.netbeans.modules.java.source.queries.api.QueryException
supr java.lang.Object
hfds forURL,toRun
hcls Accessor,SPIFnc

CLSS public abstract interface org.netbeans.modules.java.source.queries.spi.TemplateWizardProvider
meth public abstract org.openide.WizardDescriptor$InstantiatingIterator<org.openide.WizardDescriptor> createWizard()
meth public abstract org.openide.WizardDescriptor$InstantiatingIterator<org.openide.WizardDescriptor> createWizardForSuperClass()

