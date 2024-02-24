#Signature file v4.1
#Version 1.72.0

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

CLSS public final org.netbeans.modules.java.preprocessorbridge.api.CompileOnSaveActionQuery
meth public static org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction getAction(java.net.URL)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds a2u,instances,u2a
hcls ProxyAction

CLSS public org.netbeans.modules.java.preprocessorbridge.api.JavaSourceUtil
innr public static Handle
meth public static java.util.Map<java.lang.String,byte[]> generate(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.CharSequence,javax.tools.DiagnosticListener<? super javax.tools.JavaFileObject>) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.java.preprocessorbridge.api.JavaSourceUtil$Handle createControllerHandle(org.openide.filesystems.FileObject,int,org.netbeans.modules.java.preprocessorbridge.api.JavaSourceUtil$Handle) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.java.preprocessorbridge.api.JavaSourceUtil$Handle createControllerHandle(org.openide.filesystems.FileObject,org.netbeans.modules.java.preprocessorbridge.api.JavaSourceUtil$Handle) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds result

CLSS public static org.netbeans.modules.java.preprocessorbridge.api.JavaSourceUtil$Handle
 outer org.netbeans.modules.java.preprocessorbridge.api.JavaSourceUtil
meth public java.lang.Object getCompilationController()
supr java.lang.Object
hfds compilationController,id

CLSS public final org.netbeans.modules.java.preprocessorbridge.api.ModuleUtilities
meth public com.sun.source.tree.ModuleTree parseModule() throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String parseModuleName() throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public javax.lang.model.element.ModuleElement resolveModule(com.sun.source.tree.ModuleTree) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public javax.lang.model.element.ModuleElement resolveModule(java.lang.String) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public javax.lang.model.element.TypeElement readClassFile() throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.java.preprocessorbridge.api.ModuleUtilities get(java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds handle,javaSource,result

CLSS public abstract interface org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction
innr public abstract interface static Provider
innr public final static !enum Operation
innr public final static Context
meth public abstract boolean isEnabled()
meth public abstract boolean isUpdateClasses()
meth public abstract boolean isUpdateResources()
meth public abstract java.lang.Boolean performAction(org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction$Context) throws java.io.IOException
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction$Context
 outer org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction
meth public boolean isAllFilesIndexing()
meth public boolean isCopyResources()
meth public boolean isKeepResourcesUpToDate()
meth public java.io.File getCacheRoot()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.io.File getTarget()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.Iterable<? extends java.io.File> getDeleted()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.Iterable<? extends java.io.File> getUpdated()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.Object getOwner()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.net.URL getSourceRoot()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.net.URL getTargetURL()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction$Operation getOperation()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.io.File getTarget(java.net.URL)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.net.URL getTargetURL(java.net.URL)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction$Context clean(java.net.URL)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction$Context sync(java.net.URL,boolean,boolean,java.lang.Object)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction$Context update(java.net.URL,boolean,boolean,java.io.File,java.lang.Iterable<? extends java.io.File>,java.lang.Iterable<? extends java.io.File>,java.util.function.Consumer<java.lang.Iterable<java.io.File>>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NonNull()
 anno 7 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction$Context update(java.net.URL,boolean,java.io.File,java.lang.Iterable<? extends java.io.File>,java.lang.Iterable<? extends java.io.File>,java.util.function.Consumer<java.lang.Iterable<java.io.File>>)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NonNull()
 anno 6 org.netbeans.api.annotations.common.NullAllowed()
meth public void filesUpdated(java.lang.Iterable<java.io.File>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds cacheRoot,deleted,firer,isAllFilesIndexing,isCopyResources,isKeepResourcesUpToDate,operation,owner,srcRoot,updated

CLSS public final static !enum org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction$Operation
 outer org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction
fld public final static org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction$Operation CLEAN
fld public final static org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction$Operation SYNC
fld public final static org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction$Operation UPDATE
meth public static org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction$Operation valueOf(java.lang.String)
meth public static org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction$Operation[] values()
supr java.lang.Enum<org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction$Operation>

CLSS public abstract interface static org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction$Provider
 outer org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction
meth public abstract org.netbeans.modules.java.preprocessorbridge.spi.CompileOnSaveAction forRoot(java.net.URL)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.java.preprocessorbridge.spi.ImportProcessor
meth public abstract void addImport(javax.swing.text.Document,java.lang.String)

CLSS public abstract interface org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation
meth public abstract java.io.Reader filterReader(java.io.Reader)
meth public abstract java.io.Writer filterWriter(java.io.Writer)
meth public abstract java.lang.CharSequence filterCharSequence(java.lang.CharSequence)
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public abstract interface org.netbeans.modules.java.preprocessorbridge.spi.JavaIndexerPlugin
innr public abstract interface static Factory
meth public abstract void delete(org.netbeans.modules.parsing.spi.indexing.Indexable)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void finish()
meth public abstract void process(com.sun.source.tree.CompilationUnitTree,org.netbeans.modules.parsing.spi.indexing.Indexable,org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static org.netbeans.modules.java.preprocessorbridge.spi.JavaIndexerPlugin$Factory
 outer org.netbeans.modules.java.preprocessorbridge.spi.JavaIndexerPlugin
meth public abstract org.netbeans.modules.java.preprocessorbridge.spi.JavaIndexerPlugin create(java.net.URL,org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceProvider
innr public abstract interface static PositionTranslatingJavaFileFilterImplementation
meth public abstract org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceProvider$PositionTranslatingJavaFileFilterImplementation forFileObject(org.openide.filesystems.FileObject)

CLSS public abstract interface static org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceProvider$PositionTranslatingJavaFileFilterImplementation
 outer org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceProvider
intf org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation
meth public abstract int getJavaSourcePosition(int)
meth public abstract int getOriginalPosition(int)

CLSS public abstract org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceUtilImpl
cons protected init()
innr public abstract static ModuleInfoHandle
meth protected abstract long createTaggedCompilationController(org.openide.filesystems.FileObject,long,java.lang.Object[]) throws java.io.IOException
meth protected abstract org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceUtilImpl$ModuleInfoHandle getModuleInfoHandle(java.lang.Object) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth protected java.util.Map<java.lang.String,byte[]> generate(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.CharSequence,javax.tools.DiagnosticListener<? super javax.tools.JavaFileObject>) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth protected long createTaggedCompilationController(org.openide.filesystems.FileObject,int,long,java.lang.Object[]) throws java.io.IOException
supr java.lang.Object
hfds EXPECTED_PACKAGE
hcls MyAccessor

CLSS public abstract static org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceUtilImpl$ModuleInfoHandle
 outer org.netbeans.modules.java.preprocessorbridge.spi.JavaSourceUtilImpl
cons public init()
meth public abstract com.sun.source.tree.ModuleTree parseModule() throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String parseModuleName() throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract javax.lang.model.element.ModuleElement resolveModule(com.sun.source.tree.ModuleTree) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract javax.lang.model.element.ModuleElement resolveModule(java.lang.String) throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract javax.lang.model.element.TypeElement readClassFile() throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider
innr public abstract interface static Result
meth public abstract boolean index()
meth public abstract java.util.Set<java.lang.String> getSupportedExtensions()
meth public abstract void translate(java.lang.Iterable<java.io.File>,java.io.File,org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider$Result)

CLSS public abstract interface static org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider$Result
 outer org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider
meth public abstract void add(java.io.File,java.lang.String,java.lang.String,java.lang.CharSequence)

CLSS public abstract interface org.netbeans.modules.java.preprocessorbridge.spi.WrapperFactory
meth public abstract com.sun.source.util.Trees wrapTrees(com.sun.source.util.Trees)

