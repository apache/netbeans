#Signature file v4.1
#Version 1.52.0

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

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

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

CLSS public final org.netbeans.insane.live.CancelException
cons public init()
supr java.lang.RuntimeException

CLSS public final org.netbeans.insane.live.LiveReferences
meth public static java.util.Map<java.lang.Object,org.netbeans.insane.live.Path> fromRoots(java.util.Collection<java.lang.Object>)
meth public static java.util.Map<java.lang.Object,org.netbeans.insane.live.Path> fromRoots(java.util.Collection<java.lang.Object>,java.util.Set<java.lang.Object>,javax.swing.BoundedRangeModel)
meth public static java.util.Map<java.lang.Object,org.netbeans.insane.live.Path> fromRoots(java.util.Collection<java.lang.Object>,java.util.Set<java.lang.Object>,javax.swing.BoundedRangeModel,org.netbeans.insane.scanner.Filter)
supr java.lang.Object

CLSS public final org.netbeans.insane.live.Path
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object getObject()
meth public java.lang.String describeReference()
meth public java.lang.String toString()
meth public org.netbeans.insane.live.Path nextNode()
meth public static java.lang.String describeObject(java.lang.Object)
supr java.lang.Object
hfds item,nextElement

CLSS public abstract interface org.netbeans.insane.model.HeapModel
meth public abstract java.util.Collection<java.lang.String> getRoots()
meth public abstract java.util.Collection<org.netbeans.insane.model.Item> getObjectsOfType(java.lang.String)
meth public abstract java.util.Iterator<org.netbeans.insane.model.Item> getAllItems()
meth public abstract org.netbeans.insane.model.Item getItem(int)
meth public abstract org.netbeans.insane.model.Item getObjectAt(java.lang.String)

CLSS public abstract interface org.netbeans.insane.model.Item
meth public abstract int getId()
meth public abstract int getSize()
meth public abstract java.lang.String getType()
meth public abstract java.lang.String getValue()
meth public abstract java.util.Enumeration<java.lang.Object> incomming()
meth public abstract java.util.Enumeration<org.netbeans.insane.model.Item> outgoing()

CLSS public final org.netbeans.insane.model.Support
meth public static org.netbeans.insane.model.HeapModel openSimpleBinaryDump(java.io.File) throws java.lang.Exception
meth public static org.netbeans.insane.model.HeapModel parseSimpleXMLDump(java.io.File) throws java.lang.Exception
meth public static void convertSimpleDump(java.io.File,java.io.File) throws java.lang.Exception
meth public static void findRoots(org.netbeans.insane.model.HeapModel,org.netbeans.insane.model.Item,boolean)
supr java.lang.Object
hcls PathElement

CLSS public org.netbeans.insane.scanner.CountingVisitor
cons public init()
intf org.netbeans.insane.scanner.Visitor
meth public int getCountForClass(java.lang.Class)
meth public int getSizeForClass(java.lang.Class)
meth public int getTotalCount()
meth public int getTotalSize()
meth public java.util.Set<java.lang.Class<?>> getClasses()
meth public void visitArrayReference(org.netbeans.insane.scanner.ObjectMap,java.lang.Object,java.lang.Object,int)
meth public void visitClass(java.lang.Class<?>)
meth public void visitObject(org.netbeans.insane.scanner.ObjectMap,java.lang.Object)
meth public void visitObjectReference(org.netbeans.insane.scanner.ObjectMap,java.lang.Object,java.lang.Object,java.lang.reflect.Field)
meth public void visitStaticReference(org.netbeans.insane.scanner.ObjectMap,java.lang.Object,java.lang.reflect.Field)
supr java.lang.Object
hfds count,infoMap,size
hcls Info

CLSS public abstract interface org.netbeans.insane.scanner.Filter
meth public abstract boolean accept(java.lang.Object,java.lang.Object,java.lang.reflect.Field)

CLSS public abstract interface org.netbeans.insane.scanner.ObjectMap
meth public abstract boolean isKnown(java.lang.Object)
meth public abstract java.lang.String getID(java.lang.Object)

CLSS public final org.netbeans.insane.scanner.ScannerUtils
meth public static int recursiveSizeOf(java.util.Collection,org.netbeans.insane.scanner.Filter) throws java.lang.Exception
meth public static int sizeOf(java.lang.Object)
meth public static java.util.Set<java.lang.Object> interestingRoots()
meth public static org.netbeans.insane.scanner.Filter compoundFilter(org.netbeans.insane.scanner.Filter[])
meth public static org.netbeans.insane.scanner.Filter noFilter()
meth public static org.netbeans.insane.scanner.Filter skipNonStrongReferencesFilter()
meth public static org.netbeans.insane.scanner.Filter skipObjectsFilter(java.util.Collection<java.lang.Object>,boolean)
meth public static org.netbeans.insane.scanner.Filter skipReferencesFilter(java.util.Collection<java.lang.reflect.Field>)
meth public static org.netbeans.insane.scanner.Visitor compoundVisitor(org.netbeans.insane.scanner.Visitor[])
meth public static void scan(org.netbeans.insane.scanner.Filter,org.netbeans.insane.scanner.Visitor,java.util.Collection,boolean) throws java.lang.Exception
meth public static void scanExclusivelyInAWT(org.netbeans.insane.scanner.Filter,org.netbeans.insane.scanner.Visitor,java.util.Set) throws java.lang.Exception
supr java.lang.Object
hcls ClassInfo

CLSS public final org.netbeans.insane.scanner.SimpleXmlVisitor
cons public init(java.io.File) throws java.io.IOException
intf org.netbeans.insane.scanner.Visitor
meth public void close() throws java.io.IOException
meth public void visitArrayReference(org.netbeans.insane.scanner.ObjectMap,java.lang.Object,java.lang.Object,int)
meth public void visitClass(java.lang.Class)
meth public void visitObject(org.netbeans.insane.scanner.ObjectMap,java.lang.Object)
meth public void visitObjectReference(org.netbeans.insane.scanner.ObjectMap,java.lang.Object,java.lang.Object,java.lang.reflect.Field)
meth public void visitStaticReference(org.netbeans.insane.scanner.ObjectMap,java.lang.Object,java.lang.reflect.Field)
supr java.lang.Object
hfds CHAR_ARRAY,pom,storedException,writer

CLSS public abstract interface org.netbeans.insane.scanner.Visitor
meth public abstract void visitArrayReference(org.netbeans.insane.scanner.ObjectMap,java.lang.Object,java.lang.Object,int)
meth public abstract void visitClass(java.lang.Class<?>)
meth public abstract void visitObject(org.netbeans.insane.scanner.ObjectMap,java.lang.Object)
meth public abstract void visitObjectReference(org.netbeans.insane.scanner.ObjectMap,java.lang.Object,java.lang.Object,java.lang.reflect.Field)
meth public abstract void visitStaticReference(org.netbeans.insane.scanner.ObjectMap,java.lang.Object,java.lang.reflect.Field)

