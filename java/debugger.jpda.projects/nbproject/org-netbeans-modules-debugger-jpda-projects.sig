#Signature file v4.1
#Version 1.63

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

CLSS public abstract interface java.util.Comparator<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.function.Function<? super {java.util.Comparator%0},? extends {%%0}>)
meth public <%0 extends java.lang.Object> java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.function.Function<? super {java.util.Comparator%0},? extends {%%0}>,java.util.Comparator<? super {%%0}>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract int compare({java.util.Comparator%0},{java.util.Comparator%0})
meth public java.util.Comparator<{java.util.Comparator%0}> reversed()
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.Comparator<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingDouble(java.util.function.ToDoubleFunction<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingInt(java.util.function.ToIntFunction<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingLong(java.util.function.ToLongFunction<? super {java.util.Comparator%0}>)
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{%%0}> naturalOrder()
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{%%0}> reverseOrder()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Comparable<? super {%%1}>> java.util.Comparator<{%%0}> comparing(java.util.function.Function<? super {%%0},? extends {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Comparator<{%%0}> comparing(java.util.function.Function<? super {%%0},? extends {%%1}>,java.util.Comparator<? super {%%1}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingDouble(java.util.function.ToDoubleFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingInt(java.util.function.ToIntFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingLong(java.util.function.ToLongFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> nullsFirst(java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> nullsLast(java.util.Comparator<? super {%%0}>)

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

CLSS public abstract interface org.netbeans.modules.debugger.jpda.projects.ASTOperationCreationDelegate
meth public abstract org.netbeans.spi.debugger.jpda.EditorContext$Operation createMethodOperation(org.netbeans.spi.debugger.jpda.EditorContext$Position,org.netbeans.spi.debugger.jpda.EditorContext$Position,org.netbeans.spi.debugger.jpda.EditorContext$Position,org.netbeans.spi.debugger.jpda.EditorContext$Position,java.lang.String,java.lang.String,int,boolean)
meth public abstract org.netbeans.spi.debugger.jpda.EditorContext$Position createPosition(int,int,int)
meth public abstract void addNextOperationTo(org.netbeans.spi.debugger.jpda.EditorContext$Operation,org.netbeans.spi.debugger.jpda.EditorContext$Operation)

CLSS public org.netbeans.modules.debugger.jpda.projects.ConstantPool
innr public abstract static Entry
innr public final static EntryModule
innr public final static EntryPackage
innr public static BytesEntry
innr public static EntryClass
innr public static EntryConstantDynamic
innr public static EntryDouble
innr public static EntryFieldMethodRef
innr public static EntryFloat
innr public static EntryInteger
innr public static EntryInvokeDynamic
innr public static EntryLong
innr public static EntryMethodHandle
innr public static EntryMethodType
innr public static EntryNULL
innr public static EntryNameType
innr public static EntryString
innr public static EntryUTF8
meth public java.lang.String getMethodDescriptor(int)
meth public java.lang.String getMethodName(int)
meth public org.netbeans.modules.debugger.jpda.projects.ConstantPool$Entry getEntry(int)
meth public static org.netbeans.modules.debugger.jpda.projects.ConstantPool parse(byte[],java.lang.String)
supr java.lang.Object
hfds TAG_CLASS,TAG_CONSTANTDYNAMIC,TAG_DOUBLE,TAG_FIELDREF,TAG_FLOAT,TAG_INTEGER,TAG_INTERFACEREF,TAG_INVOKEDYNAMIC,TAG_LONG,TAG_METHODHANDLE,TAG_METHODREF,TAG_METHODTYPE,TAG_MODULE,TAG_NAMETYPE,TAG_PACKAGE,TAG_STRING,TAG_UTF8,description,entries

CLSS public static org.netbeans.modules.debugger.jpda.projects.ConstantPool$BytesEntry
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init(byte,java.io.DataInputStream,int) throws java.io.IOException
fld protected final byte[] bytes
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$Entry

CLSS public abstract static org.netbeans.modules.debugger.jpda.projects.ConstantPool$Entry
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons protected init(byte)
meth public final byte getTag()
supr java.lang.Object
hfds tag

CLSS public static org.netbeans.modules.debugger.jpda.projects.ConstantPool$EntryClass
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init(short)
meth public short getClassRef()
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$Entry
hfds classRef

CLSS public static org.netbeans.modules.debugger.jpda.projects.ConstantPool$EntryConstantDynamic
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init(java.io.DataInputStream) throws java.io.IOException
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$BytesEntry

CLSS public static org.netbeans.modules.debugger.jpda.projects.ConstantPool$EntryDouble
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init(double)
meth public double getDouble()
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$Entry
hfds d

CLSS public static org.netbeans.modules.debugger.jpda.projects.ConstantPool$EntryFieldMethodRef
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init(byte,short,short)
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$Entry
hfds classIndex,nameAndTypeIndex

CLSS public static org.netbeans.modules.debugger.jpda.projects.ConstantPool$EntryFloat
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init(float)
meth public float getFloat()
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$Entry
hfds f

CLSS public static org.netbeans.modules.debugger.jpda.projects.ConstantPool$EntryInteger
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init(int)
meth public int getInteger()
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$Entry
hfds i

CLSS public static org.netbeans.modules.debugger.jpda.projects.ConstantPool$EntryInvokeDynamic
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init(java.io.DataInputStream) throws java.io.IOException
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$BytesEntry

CLSS public static org.netbeans.modules.debugger.jpda.projects.ConstantPool$EntryLong
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init(long)
meth public long getLong()
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$Entry
hfds l

CLSS public static org.netbeans.modules.debugger.jpda.projects.ConstantPool$EntryMethodHandle
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init(java.io.DataInputStream) throws java.io.IOException
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$BytesEntry

CLSS public static org.netbeans.modules.debugger.jpda.projects.ConstantPool$EntryMethodType
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init(java.io.DataInputStream) throws java.io.IOException
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$BytesEntry

CLSS public final static org.netbeans.modules.debugger.jpda.projects.ConstantPool$EntryModule
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init(short)
meth public short getModuleRef()
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$Entry
hfds nameIndex

CLSS public static org.netbeans.modules.debugger.jpda.projects.ConstantPool$EntryNULL
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init()
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$Entry

CLSS public static org.netbeans.modules.debugger.jpda.projects.ConstantPool$EntryNameType
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init(short,short)
meth public short getDescriptorIndex()
meth public short getNameIndex()
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$Entry
hfds descriptorIndex,nameIndex

CLSS public final static org.netbeans.modules.debugger.jpda.projects.ConstantPool$EntryPackage
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init(short)
meth public short getPackageRef()
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$Entry
hfds nameIndex

CLSS public static org.netbeans.modules.debugger.jpda.projects.ConstantPool$EntryString
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init(short)
meth public short getStringRef()
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$Entry
hfds stringRef

CLSS public static org.netbeans.modules.debugger.jpda.projects.ConstantPool$EntryUTF8
 outer org.netbeans.modules.debugger.jpda.projects.ConstantPool
cons public init(java.lang.String)
meth public java.lang.String getUTF8()
supr org.netbeans.modules.debugger.jpda.projects.ConstantPool$Entry
hfds utf8

CLSS public final org.netbeans.modules.debugger.jpda.projects.EditorContextSupport
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%0} interpretOrCompileCode(org.netbeans.spi.debugger.jpda.Evaluator$Expression<java.lang.Object>,java.lang.String,int,org.netbeans.api.java.source.support.ErrorAwareTreePathScanner<java.lang.Boolean,{%%1}>,org.netbeans.api.java.source.support.ErrorAwareTreePathScanner<{%%0},{%%1}>,{%%1},boolean,java.util.function.Function<org.openide.util.Pair<java.lang.String,byte[]>,java.lang.Boolean>,org.netbeans.spi.debugger.jpda.SourcePathProvider) throws org.netbeans.api.debugger.jpda.InvalidExpressionException
meth public static int getFieldLineNumber(java.lang.String,java.lang.String,java.lang.String)
meth public static int getMethodLineNumber(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String getClassDeclaredAt(org.openide.filesystems.FileObject,int)
meth public static java.lang.String getClassName(java.lang.String,int)
meth public static java.lang.String getCurrentElement(org.openide.filesystems.FileObject,int,java.lang.String,javax.lang.model.element.ElementKind,java.lang.String[])
meth public static java.lang.String[] getImports(java.lang.String)
meth public static java.lang.String[] getMethodDeclaredAt(org.openide.filesystems.FileObject,int)
meth public static java.util.concurrent.Future<int[]> getMethodLineNumbers(org.openide.filesystems.FileObject,java.lang.String,java.lang.String[],java.lang.String,java.lang.String)
meth public static java.util.concurrent.Future<java.lang.Integer> getClassLineNumber(org.openide.filesystems.FileObject,java.lang.String,java.lang.String[])
meth public static java.util.concurrent.Future<java.lang.Integer> getFieldLineNumber(org.openide.filesystems.FileObject,java.lang.String,java.lang.String)
meth public static javax.lang.model.element.TypeElement getTypeElement(org.netbeans.api.java.source.CompilationController,java.lang.String,java.lang.String[])
meth public static org.netbeans.spi.debugger.jpda.EditorContext$MethodArgument[] computeMethodArguments(org.netbeans.api.java.source.CompilationController,int,int,org.netbeans.modules.debugger.jpda.projects.ASTOperationCreationDelegate) throws java.io.IOException
meth public static org.netbeans.spi.debugger.jpda.EditorContext$MethodArgument[] computeMethodArguments(org.netbeans.api.java.source.CompilationController,org.netbeans.spi.debugger.jpda.EditorContext$Operation,org.netbeans.modules.debugger.jpda.projects.ASTOperationCreationDelegate) throws java.io.IOException
meth public static org.netbeans.spi.debugger.jpda.EditorContext$MethodArgument[] getArguments(java.lang.String,int,org.netbeans.modules.debugger.jpda.projects.ASTOperationCreationDelegate)
meth public static org.netbeans.spi.debugger.jpda.EditorContext$MethodArgument[] getArguments(java.lang.String,org.netbeans.spi.debugger.jpda.EditorContext$Operation,org.netbeans.modules.debugger.jpda.projects.ASTOperationCreationDelegate)
meth public static org.netbeans.spi.debugger.jpda.EditorContext$Operation[] computeOperations(org.netbeans.api.java.source.CompilationController,int,int,org.netbeans.spi.debugger.jpda.EditorContext$BytecodeProvider,org.netbeans.modules.debugger.jpda.projects.ASTOperationCreationDelegate) throws java.io.IOException
meth public static org.netbeans.spi.debugger.jpda.EditorContext$Operation[] getOperations(java.lang.String,int,org.netbeans.spi.debugger.jpda.EditorContext$BytecodeProvider,org.netbeans.modules.debugger.jpda.projects.ASTOperationCreationDelegate)
supr java.lang.Object
hfds LOG,fieldLNCache,preferredCCParser,scanningProcessor
hcls DoneFuture,ScanRunnable

CLSS public org.netbeans.modules.debugger.jpda.projects.FixClassesSupport
cons public init()
innr public final static ClassesToReload
meth public static void reloadClasses(org.netbeans.api.debugger.jpda.JPDADebugger,java.util.Map<java.lang.String,org.openide.filesystems.FileObject>)
supr java.lang.Object

CLSS public final static org.netbeans.modules.debugger.jpda.projects.FixClassesSupport$ClassesToReload
 outer org.netbeans.modules.debugger.jpda.projects.FixClassesSupport
meth public boolean hasClassesToReload(org.netbeans.api.debugger.jpda.JPDADebugger,java.util.Set<org.openide.filesystems.FileObject>)
meth public java.util.Map<java.lang.String,org.openide.filesystems.FileObject> popClassesToReload(org.netbeans.api.debugger.jpda.JPDADebugger,java.util.Set<org.openide.filesystems.FileObject>)
meth public static org.netbeans.modules.debugger.jpda.projects.FixClassesSupport$ClassesToReload getInstance()
meth public void addClassToReload(org.netbeans.api.debugger.jpda.JPDADebugger,org.openide.filesystems.FileObject,java.lang.String,org.openide.filesystems.FileObject)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds classesByDebugger,instance,pch

CLSS public org.netbeans.modules.debugger.jpda.projects.SourcePathProviderImpl
cons public init()
cons public init(org.netbeans.spi.debugger.ContextProvider)
innr public final static FileObjectComparator
meth public java.lang.String getRelativePath(java.lang.String,char,boolean)
meth public java.lang.String getSourceRoot(java.lang.String)
meth public java.lang.String getURL(java.lang.String,boolean)
meth public java.lang.String[] getAdditionalSourceRoots()
meth public java.lang.String[] getAllURLs(java.lang.String,boolean)
meth public java.lang.String[] getOriginalSourceRoots()
meth public java.lang.String[] getProjectSourceRoots()
meth public java.lang.String[] getSourceRoots()
meth public java.util.Set<java.lang.String> getPlatformSourceRoots()
meth public java.util.Set<org.openide.filesystems.FileObject> getSourceRootsFO()
meth public static int[] createPermutation(java.lang.String[],java.util.Map<java.lang.String,java.lang.Integer>,java.lang.String[])
meth public static java.lang.String getRoot(org.openide.filesystems.FileObject)
meth public static java.lang.String normalize(java.lang.String)
meth public static java.util.Map<java.lang.String,java.lang.Integer> getRemoteSourceRootsOrder()
meth public static java.util.Map<java.lang.String,java.lang.Integer> getSourceRootsOrder(java.lang.String)
meth public static void storeSourceRootsOrder(java.lang.String,java.lang.String[],int[])
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void reorderOriginalSourceRoots(int[])
meth public void setSourceRoots(java.lang.String[])
meth public void setSourceRoots(java.lang.String[],java.lang.String[])
supr org.netbeans.spi.debugger.jpda.SourcePathProvider
hfds CAN_FIX_CLASSES_AUTOMATICALLY,additionalSourceRoots,baseDir,logger,originalSourcePath,parentDirectoryPattern,pathRegistryListener,pcs,platformSourceRoots,projectSourceRoots,smartSteppingSourcePath,sourcePathPermutation,thisDirectoryPattern,unorderedOriginalSourcePath,urlCache,urlCacheGlobal,verbose
hcls ArtifactsUpdatedImpl,PathRegistryListener,URLCacheMap

CLSS public final static org.netbeans.modules.debugger.jpda.projects.SourcePathProviderImpl$FileObjectComparator
 outer org.netbeans.modules.debugger.jpda.projects.SourcePathProviderImpl
cons public init()
intf java.util.Comparator<org.openide.filesystems.FileObject>
meth public int compare(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
supr java.lang.Object

CLSS public final org.netbeans.modules.debugger.jpda.projects.WeakHashMapActive<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
meth public int size()
meth public java.util.Set<java.util.Map$Entry<{org.netbeans.modules.debugger.jpda.projects.WeakHashMapActive%0},{org.netbeans.modules.debugger.jpda.projects.WeakHashMapActive%1}>> entrySet()
meth public void clear()
meth public {org.netbeans.modules.debugger.jpda.projects.WeakHashMapActive%1} get(java.lang.Object)
meth public {org.netbeans.modules.debugger.jpda.projects.WeakHashMapActive%1} put({org.netbeans.modules.debugger.jpda.projects.WeakHashMapActive%0},{org.netbeans.modules.debugger.jpda.projects.WeakHashMapActive%1})
meth public {org.netbeans.modules.debugger.jpda.projects.WeakHashMapActive%1} remove(java.lang.Object)
supr java.util.AbstractMap<{org.netbeans.modules.debugger.jpda.projects.WeakHashMapActive%0},{org.netbeans.modules.debugger.jpda.projects.WeakHashMapActive%1}>
hfds map,queue
hcls KeyReference

CLSS public abstract org.netbeans.spi.debugger.jpda.SourcePathProvider
cons public init()
fld public final static java.lang.String PROP_SOURCE_ROOTS = "sourceRoots"
innr public abstract interface static !annotation Registration
meth public abstract java.lang.String getRelativePath(java.lang.String,char,boolean)
meth public abstract java.lang.String getURL(java.lang.String,boolean)
meth public abstract java.lang.String[] getOriginalSourceRoots()
meth public abstract java.lang.String[] getSourceRoots()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setSourceRoots(java.lang.String[])
meth public java.lang.String getSourceRoot(java.lang.String)
supr java.lang.Object
hcls ContextAware

