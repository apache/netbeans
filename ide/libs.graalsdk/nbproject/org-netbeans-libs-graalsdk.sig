#Signature file v4.1
#Version 2.0

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public java.lang.Error
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

CLSS public abstract java.lang.Number
cons public init()
intf java.io.Serializable
meth public abstract double doubleValue()
meth public abstract float floatValue()
meth public abstract int intValue()
meth public abstract long longValue()
meth public byte byteValue()
meth public short shortValue()
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

CLSS public java.util.concurrent.atomic.AtomicLong
cons public init()
cons public init(long)
intf java.io.Serializable
meth public double doubleValue()
meth public final boolean compareAndSet(long,long)
meth public final boolean weakCompareAndSet(long,long)
meth public final long accumulateAndGet(long,java.util.function.LongBinaryOperator)
meth public final long addAndGet(long)
meth public final long decrementAndGet()
meth public final long get()
meth public final long getAndAccumulate(long,java.util.function.LongBinaryOperator)
meth public final long getAndAdd(long)
meth public final long getAndDecrement()
meth public final long getAndIncrement()
meth public final long getAndSet(long)
meth public final long getAndUpdate(java.util.function.LongUnaryOperator)
meth public final long incrementAndGet()
meth public final long updateAndGet(java.util.function.LongUnaryOperator)
meth public final void lazySet(long)
meth public final void set(long)
meth public float floatValue()
meth public int intValue()
meth public java.lang.String toString()
meth public long longValue()
supr java.lang.Number

CLSS public abstract interface org.graalvm.collections.EconomicMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf org.graalvm.collections.UnmodifiableEconomicMap<{org.graalvm.collections.EconomicMap%0},{org.graalvm.collections.EconomicMap%1}>
meth public abstract org.graalvm.collections.MapCursor<{org.graalvm.collections.EconomicMap%0},{org.graalvm.collections.EconomicMap%1}> getEntries()
meth public abstract void clear()
meth public abstract void replaceAll(java.util.function.BiFunction<? super {org.graalvm.collections.EconomicMap%0},? super {org.graalvm.collections.EconomicMap%1},? extends {org.graalvm.collections.EconomicMap%1}>)
meth public abstract {org.graalvm.collections.EconomicMap%1} put({org.graalvm.collections.EconomicMap%0},{org.graalvm.collections.EconomicMap%1})
meth public abstract {org.graalvm.collections.EconomicMap%1} removeKey({org.graalvm.collections.EconomicMap%0})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.EconomicMap<{%%0},{%%1}> create()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.EconomicMap<{%%0},{%%1}> create(int)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.EconomicMap<{%%0},{%%1}> create(org.graalvm.collections.Equivalence)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.EconomicMap<{%%0},{%%1}> create(org.graalvm.collections.Equivalence,int)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.EconomicMap<{%%0},{%%1}> create(org.graalvm.collections.Equivalence,org.graalvm.collections.UnmodifiableEconomicMap<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.EconomicMap<{%%0},{%%1}> create(org.graalvm.collections.UnmodifiableEconomicMap<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.EconomicMap<{%%0},{%%1}> emptyMap()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.EconomicMap<{%%0},{%%1}> of({%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.EconomicMap<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.EconomicMap<{%%0},{%%1}> wrapMap(java.util.Map<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.MapCursor<{%%0},{%%1}> emptyCursor()
meth public void putAll(org.graalvm.collections.EconomicMap<{org.graalvm.collections.EconomicMap%0},{org.graalvm.collections.EconomicMap%1}>)
meth public void putAll(org.graalvm.collections.UnmodifiableEconomicMap<? extends {org.graalvm.collections.EconomicMap%0},? extends {org.graalvm.collections.EconomicMap%1}>)
meth public {org.graalvm.collections.EconomicMap%1} putIfAbsent({org.graalvm.collections.EconomicMap%0},{org.graalvm.collections.EconomicMap%1})

CLSS public final org.graalvm.collections.EconomicMapUtil
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> boolean equals(org.graalvm.collections.UnmodifiableEconomicMap<{%%0},{%%1}>,org.graalvm.collections.UnmodifiableEconomicMap<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> int hashCode(org.graalvm.collections.UnmodifiableEconomicMap<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Comparator<org.graalvm.collections.UnmodifiableEconomicMap<{%%0},{%%1}>> lexicographicalComparator(java.util.Comparator<{%%0}>,java.util.Comparator<{%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.EconomicSet<{%%0}> keySet(org.graalvm.collections.EconomicMap<{%%0},{%%1}>)
supr java.lang.Object

CLSS public org.graalvm.collections.EconomicMapWrap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(java.util.Map<{org.graalvm.collections.EconomicMapWrap%0},{org.graalvm.collections.EconomicMapWrap%1}>)
intf org.graalvm.collections.EconomicMap<{org.graalvm.collections.EconomicMapWrap%0},{org.graalvm.collections.EconomicMapWrap%1}>
meth public boolean containsKey({org.graalvm.collections.EconomicMapWrap%0})
meth public boolean isEmpty()
meth public int size()
meth public java.lang.Iterable<{org.graalvm.collections.EconomicMapWrap%0}> getKeys()
meth public java.lang.Iterable<{org.graalvm.collections.EconomicMapWrap%1}> getValues()
meth public org.graalvm.collections.MapCursor<{org.graalvm.collections.EconomicMapWrap%0},{org.graalvm.collections.EconomicMapWrap%1}> getEntries()
meth public void clear()
meth public void replaceAll(java.util.function.BiFunction<? super {org.graalvm.collections.EconomicMapWrap%0},? super {org.graalvm.collections.EconomicMapWrap%1},? extends {org.graalvm.collections.EconomicMapWrap%1}>)
meth public {org.graalvm.collections.EconomicMapWrap%1} get({org.graalvm.collections.EconomicMapWrap%0})
meth public {org.graalvm.collections.EconomicMapWrap%1} put({org.graalvm.collections.EconomicMapWrap%0},{org.graalvm.collections.EconomicMapWrap%1})
meth public {org.graalvm.collections.EconomicMapWrap%1} putIfAbsent({org.graalvm.collections.EconomicMapWrap%0},{org.graalvm.collections.EconomicMapWrap%1})
meth public {org.graalvm.collections.EconomicMapWrap%1} removeKey({org.graalvm.collections.EconomicMapWrap%0})
supr java.lang.Object
hfds map

CLSS public abstract interface org.graalvm.collections.EconomicSet<%0 extends java.lang.Object>
intf org.graalvm.collections.UnmodifiableEconomicSet<{org.graalvm.collections.EconomicSet%0}>
meth public abstract boolean add({org.graalvm.collections.EconomicSet%0})
meth public abstract void clear()
meth public abstract void remove({org.graalvm.collections.EconomicSet%0})
meth public static <%0 extends java.lang.Object> org.graalvm.collections.EconomicSet<{%%0}> create()
meth public static <%0 extends java.lang.Object> org.graalvm.collections.EconomicSet<{%%0}> create(int)
meth public static <%0 extends java.lang.Object> org.graalvm.collections.EconomicSet<{%%0}> create(org.graalvm.collections.Equivalence)
meth public static <%0 extends java.lang.Object> org.graalvm.collections.EconomicSet<{%%0}> create(org.graalvm.collections.Equivalence,int)
meth public static <%0 extends java.lang.Object> org.graalvm.collections.EconomicSet<{%%0}> create(org.graalvm.collections.Equivalence,org.graalvm.collections.UnmodifiableEconomicSet<{%%0}>)
meth public static <%0 extends java.lang.Object> org.graalvm.collections.EconomicSet<{%%0}> create(org.graalvm.collections.UnmodifiableEconomicSet<{%%0}>)
meth public void addAll(java.lang.Iterable<{org.graalvm.collections.EconomicSet%0}>)
meth public void addAll(java.util.Iterator<{org.graalvm.collections.EconomicSet%0}>)
meth public void addAll(org.graalvm.collections.EconomicSet<{org.graalvm.collections.EconomicSet%0}>)
meth public void removeAll(java.lang.Iterable<{org.graalvm.collections.EconomicSet%0}>)
meth public void removeAll(java.util.Iterator<{org.graalvm.collections.EconomicSet%0}>)
meth public void removeAll(org.graalvm.collections.EconomicSet<{org.graalvm.collections.EconomicSet%0}>)
meth public void retainAll(org.graalvm.collections.EconomicSet<{org.graalvm.collections.EconomicSet%0}>)

CLSS public abstract org.graalvm.collections.Equivalence
cons protected init()
fld public final static org.graalvm.collections.Equivalence DEFAULT
fld public final static org.graalvm.collections.Equivalence IDENTITY
fld public final static org.graalvm.collections.Equivalence IDENTITY_WITH_SYSTEM_HASHCODE
meth public abstract boolean equals(java.lang.Object,java.lang.Object)
meth public abstract int hashCode(java.lang.Object)
supr java.lang.Object

CLSS public org.graalvm.collections.LockFreePool<%0 extends java.lang.Object>
cons public init()
meth public void add({org.graalvm.collections.LockFreePool%0})
meth public {org.graalvm.collections.LockFreePool%0} get()
supr java.lang.Object
hfds head
hcls Node

CLSS public org.graalvm.collections.LockFreePrefixTree
cons public init(org.graalvm.collections.LockFreePrefixTree$Allocator)
innr public abstract static Allocator
innr public static HeapAllocator
innr public static Node
innr public static ObjectPoolingAllocator
meth public <%0 extends java.lang.Object> void topDown({%%0},java.util.function.BiFunction<{%%0},java.lang.Long,{%%0}>,java.util.function.BiConsumer<{%%0},java.lang.Long>)
meth public org.graalvm.collections.LockFreePrefixTree$Allocator allocator()
meth public org.graalvm.collections.LockFreePrefixTree$Node root()
supr java.lang.Object
hfds allocator,root
hcls FailedAllocationException

CLSS public abstract static org.graalvm.collections.LockFreePrefixTree$Allocator
 outer org.graalvm.collections.LockFreePrefixTree
cons public init()
meth public abstract java.util.concurrent.atomic.AtomicReferenceArray newHashChildren(int)
meth public abstract java.util.concurrent.atomic.AtomicReferenceArray newLinearChildren(int)
meth public abstract org.graalvm.collections.LockFreePrefixTree$Node newNode(long)
meth public abstract void shutdown()
supr java.lang.Object

CLSS public static org.graalvm.collections.LockFreePrefixTree$HeapAllocator
 outer org.graalvm.collections.LockFreePrefixTree
cons public init()
meth public java.util.concurrent.atomic.AtomicReferenceArray newHashChildren(int)
meth public java.util.concurrent.atomic.AtomicReferenceArray newLinearChildren(int)
meth public org.graalvm.collections.LockFreePrefixTree$Node newNode(long)
meth public void shutdown()
supr org.graalvm.collections.LockFreePrefixTree$Allocator

CLSS public static org.graalvm.collections.LockFreePrefixTree$Node
 outer org.graalvm.collections.LockFreePrefixTree
meth public java.lang.String toString()
meth public long bitwiseOrValue(long)
meth public long incValue()
meth public long value()
meth public org.graalvm.collections.LockFreePrefixTree$Node at(org.graalvm.collections.LockFreePrefixTree$Allocator,long)
meth public void setValue(long)
supr java.util.concurrent.atomic.AtomicLong
hfds CHILDREN_UPDATER,FROZEN_NODE,INITIAL_HASH_NODE_SIZE,INITIAL_LINEAR_NODE_SIZE,MAX_HASH_SKIPS,MAX_LINEAR_NODE_SIZE,children,key,serialVersionUID
hcls FrozenNode,HashChildren,LinearChildren

CLSS public static org.graalvm.collections.LockFreePrefixTree$ObjectPoolingAllocator
 outer org.graalvm.collections.LockFreePrefixTree
cons public init()
cons public init(int)
meth public java.lang.String status()
meth public java.util.concurrent.atomic.AtomicReferenceArray newHashChildren(int)
meth public java.util.concurrent.atomic.AtomicReferenceArray newLinearChildren(int)
meth public org.graalvm.collections.LockFreePrefixTree$Node newNode(long)
meth public void shutdown()
supr org.graalvm.collections.LockFreePrefixTree$Allocator
hfds DEFAULT_HOUSEKEEPING_PERIOD_MILLIS,EXPECTED_MAX_HASH_NODE_SIZE,FAILED_ALLOCATION_EXCEPTION,INITIAL_HASH_CHILDREN_PREALLOCATION_COUNT,INITIAL_LINEAR_CHILDREN_PREALLOCATION_COUNT,INITIAL_NODE_PREALLOCATION_COUNT,INTERNAL_FAILURE_EXCEPTION,LOGGING,MAX_CHILDREN_PREALLOCATION_COUNT,MAX_NODE_PREALLOCATION_COUNT,MIN_HOUSEKEEPING_PERIOD_MILLIS,SIZE_CLASS_COUNT,UNSUPPORTED_SIZE_EXCEPTION,hashChildrenPool,housekeepingThread,linearChildrenPool,missedHashChildrenRequestCounts,missedLinearChildrenRequestCounts,missedNodePoolRequestCount,nodePool
hcls HousekeepingThread

CLSS public abstract interface org.graalvm.collections.MapCursor<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf org.graalvm.collections.UnmodifiableMapCursor<{org.graalvm.collections.MapCursor%0},{org.graalvm.collections.MapCursor%1}>
meth public abstract void remove()
meth public {org.graalvm.collections.MapCursor%1} setValue({org.graalvm.collections.MapCursor%1})

CLSS public final org.graalvm.collections.Pair<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.Pair<{%%0},{%%1}> create({%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.Pair<{%%0},{%%1}> createLeft({%%0})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.Pair<{%%0},{%%1}> createRight({%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.Pair<{%%0},{%%1}> empty()
meth public {org.graalvm.collections.Pair%0} getLeft()
meth public {org.graalvm.collections.Pair%1} getRight()
supr java.lang.Object
hfds EMPTY,left,right

CLSS public org.graalvm.collections.SeqLockPrefixTree
cons public init()
innr public final static Node
meth public org.graalvm.collections.SeqLockPrefixTree$Node root()
supr java.lang.Object
hfds EMPTY_KEY,HASH_NODE_LOAD_FACTOR,INITIAL_HASH_NODE_SIZE,INITIAL_LINEAR_NODE_SIZE,MAX_LINEAR_NODE_SIZE,root
hcls Visitor

CLSS public final static org.graalvm.collections.SeqLockPrefixTree$Node
 outer org.graalvm.collections.SeqLockPrefixTree
meth public <%0 extends java.lang.Object> void topDown({%%0},java.util.function.BiFunction<{%%0},java.lang.Long,{%%0}>,java.util.function.BiConsumer<{%%0},java.lang.Long>)
meth public java.lang.String toString()
meth public long incValue()
meth public long seqlockValue()
meth public long value()
meth public org.graalvm.collections.SeqLockPrefixTree$Node at(long)
meth public void setValue(long)
supr java.util.concurrent.atomic.AtomicLong
hfds arity,children,keys,seqlock,serialVersionUID

CLSS public abstract interface org.graalvm.collections.UnmodifiableEconomicMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract boolean containsKey({org.graalvm.collections.UnmodifiableEconomicMap%0})
meth public abstract boolean isEmpty()
meth public abstract int size()
meth public abstract java.lang.Iterable<{org.graalvm.collections.UnmodifiableEconomicMap%0}> getKeys()
meth public abstract java.lang.Iterable<{org.graalvm.collections.UnmodifiableEconomicMap%1}> getValues()
meth public abstract org.graalvm.collections.UnmodifiableMapCursor<{org.graalvm.collections.UnmodifiableEconomicMap%0},{org.graalvm.collections.UnmodifiableEconomicMap%1}> getEntries()
meth public abstract {org.graalvm.collections.UnmodifiableEconomicMap%1} get({org.graalvm.collections.UnmodifiableEconomicMap%0})
meth public org.graalvm.collections.Equivalence getEquivalenceStrategy()
meth public {org.graalvm.collections.UnmodifiableEconomicMap%1} get({org.graalvm.collections.UnmodifiableEconomicMap%0},{org.graalvm.collections.UnmodifiableEconomicMap%1})

CLSS public abstract interface org.graalvm.collections.UnmodifiableEconomicSet<%0 extends java.lang.Object>
intf java.lang.Iterable<{org.graalvm.collections.UnmodifiableEconomicSet%0}>
meth public abstract boolean contains({org.graalvm.collections.UnmodifiableEconomicSet%0})
meth public abstract boolean isEmpty()
meth public abstract int size()
meth public {org.graalvm.collections.UnmodifiableEconomicSet%0}[] toArray({org.graalvm.collections.UnmodifiableEconomicSet%0}[])

CLSS public abstract interface org.graalvm.collections.UnmodifiableMapCursor<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract boolean advance()
meth public abstract {org.graalvm.collections.UnmodifiableMapCursor%0} getKey()
meth public abstract {org.graalvm.collections.UnmodifiableMapCursor%1} getValue()

CLSS public abstract org.graalvm.home.HomeFinder
cons public init()
meth public abstract java.lang.String getVersion()
meth public abstract java.nio.file.Path getHomeFolder()
meth public abstract java.util.Map<java.lang.String,java.nio.file.Path> getLanguageHomes()
meth public abstract java.util.Map<java.lang.String,java.nio.file.Path> getToolHomes()
meth public static org.graalvm.home.HomeFinder getInstance()
supr java.lang.Object

CLSS public final org.graalvm.home.Version
intf java.lang.Comparable<org.graalvm.home.Version>
meth public !varargs int compareTo(int[])
meth public !varargs static org.graalvm.home.Version create(int[])
meth public boolean equals(java.lang.Object)
meth public boolean isRelease()
meth public boolean isSnapshot()
meth public int compareTo(org.graalvm.home.Version)
meth public int hashCode()
meth public java.lang.String format(java.lang.String)
meth public java.lang.String toString()
meth public static org.graalvm.home.Version getCurrent()
meth public static org.graalvm.home.Version parse(java.lang.String)
supr java.lang.Object
hfds MIN_VERSION_DIGITS,SNAPSHOT_STRING,SNAPSHOT_SUFFIX,snapshot,suffix,versions

CLSS public final org.graalvm.nativeimage.AnnotationAccess
meth public static <%0 extends java.lang.annotation.Annotation> {%%0} getAnnotation(java.lang.reflect.AnnotatedElement,java.lang.Class<{%%0}>)
meth public static boolean isAnnotationPresent(java.lang.reflect.AnnotatedElement,java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public static java.lang.Class<? extends java.lang.annotation.Annotation>[] getAnnotationTypes(java.lang.reflect.AnnotatedElement)
supr java.lang.Object

CLSS public final org.graalvm.nativeimage.CurrentIsolate
meth public static org.graalvm.nativeimage.Isolate getIsolate()
meth public static org.graalvm.nativeimage.IsolateThread getCurrentThread()
supr java.lang.Object

CLSS public final org.graalvm.nativeimage.ImageInfo
fld public final static java.lang.String PROPERTY_IMAGE_CODE_KEY = "org.graalvm.nativeimage.imagecode"
fld public final static java.lang.String PROPERTY_IMAGE_CODE_VALUE_BUILDTIME = "buildtime"
fld public final static java.lang.String PROPERTY_IMAGE_CODE_VALUE_RUNTIME = "runtime"
fld public final static java.lang.String PROPERTY_IMAGE_KIND_KEY = "org.graalvm.nativeimage.kind"
fld public final static java.lang.String PROPERTY_IMAGE_KIND_VALUE_EXECUTABLE = "executable"
fld public final static java.lang.String PROPERTY_IMAGE_KIND_VALUE_SHARED_LIBRARY = "shared"
meth public static boolean inImageBuildtimeCode()
meth public static boolean inImageCode()
meth public static boolean inImageRuntimeCode()
meth public static boolean isExecutable()
meth public static boolean isSharedLibrary()
supr java.lang.Object

CLSS public final org.graalvm.nativeimage.ImageSingletons
meth public static <%0 extends java.lang.Object> void add(java.lang.Class<{%%0}>,{%%0})
meth public static <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public static boolean contains(java.lang.Class<?>)
supr java.lang.Object

CLSS public abstract interface org.graalvm.nativeimage.Isolate
intf org.graalvm.word.PointerBase

CLSS public abstract interface org.graalvm.nativeimage.IsolateThread
intf org.graalvm.word.PointerBase

CLSS public final org.graalvm.nativeimage.Isolates
innr public abstract interface static ProtectionDomain
innr public final static CreateIsolateParameters
innr public final static IsolateException
meth public static org.graalvm.nativeimage.Isolate getIsolate(org.graalvm.nativeimage.IsolateThread)
meth public static org.graalvm.nativeimage.IsolateThread attachCurrentThread(org.graalvm.nativeimage.Isolate)
meth public static org.graalvm.nativeimage.IsolateThread createIsolate(org.graalvm.nativeimage.Isolates$CreateIsolateParameters)
meth public static org.graalvm.nativeimage.IsolateThread getCurrentThread(org.graalvm.nativeimage.Isolate)
meth public static void detachThread(org.graalvm.nativeimage.IsolateThread)
meth public static void tearDownIsolate(org.graalvm.nativeimage.IsolateThread)
supr java.lang.Object

CLSS public final static org.graalvm.nativeimage.Isolates$CreateIsolateParameters
 outer org.graalvm.nativeimage.Isolates
innr public final static Builder
meth public java.lang.String getAuxiliaryImagePath()
meth public java.util.List<java.lang.String> getArguments()
meth public org.graalvm.nativeimage.Isolates$ProtectionDomain getProtectionDomain()
meth public org.graalvm.word.UnsignedWord getAuxiliaryImageReservedSpaceSize()
meth public org.graalvm.word.UnsignedWord getReservedAddressSpaceSize()
meth public static org.graalvm.nativeimage.Isolates$CreateIsolateParameters getDefault()
supr java.lang.Object
hfds DEFAULT,arguments,auxiliaryImagePath,auxiliaryImageReservedSpaceSize,protectionDomain,reservedAddressSpaceSize

CLSS public final static org.graalvm.nativeimage.Isolates$CreateIsolateParameters$Builder
 outer org.graalvm.nativeimage.Isolates$CreateIsolateParameters
cons public init()
meth public org.graalvm.nativeimage.Isolates$CreateIsolateParameters build()
meth public org.graalvm.nativeimage.Isolates$CreateIsolateParameters$Builder appendArgument(java.lang.String)
meth public org.graalvm.nativeimage.Isolates$CreateIsolateParameters$Builder auxiliaryImagePath(java.lang.String)
meth public org.graalvm.nativeimage.Isolates$CreateIsolateParameters$Builder auxiliaryImageReservedSpaceSize(org.graalvm.word.UnsignedWord)
meth public org.graalvm.nativeimage.Isolates$CreateIsolateParameters$Builder reservedAddressSpaceSize(org.graalvm.word.UnsignedWord)
meth public org.graalvm.nativeimage.Isolates$CreateIsolateParameters$Builder setProtectionDomain(org.graalvm.nativeimage.Isolates$ProtectionDomain)
supr java.lang.Object
hfds arguments,auxiliaryImagePath,auxiliaryImageReservedSpaceSize,protectionDomain,reservedAddressSpaceSize

CLSS public final static org.graalvm.nativeimage.Isolates$IsolateException
 outer org.graalvm.nativeimage.Isolates
cons public init(java.lang.String)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public abstract interface static org.graalvm.nativeimage.Isolates$ProtectionDomain
 outer org.graalvm.nativeimage.Isolates
fld public final static org.graalvm.nativeimage.Isolates$ProtectionDomain NEW_DOMAIN
fld public final static org.graalvm.nativeimage.Isolates$ProtectionDomain NO_DOMAIN

CLSS public abstract interface org.graalvm.nativeimage.LogHandler
meth public abstract void fatalError()
meth public abstract void flush()
meth public abstract void log(org.graalvm.nativeimage.c.type.CCharPointer,org.graalvm.word.UnsignedWord)

CLSS public final org.graalvm.nativeimage.MissingReflectionRegistrationError
cons public init(java.lang.String,java.lang.Class<?>,java.lang.Class<?>,java.lang.String,java.lang.Class<?>[])
meth public java.lang.Class<?> getDeclaringClass()
meth public java.lang.Class<?> getElementType()
meth public java.lang.Class<?>[] getParameterTypes()
meth public java.lang.String getElementName()
supr java.lang.Error
hfds declaringClass,elementName,elementType,parameterTypes,serialVersionUID

CLSS public abstract interface org.graalvm.nativeimage.ObjectHandle
intf org.graalvm.word.ComparableWord

CLSS public abstract interface org.graalvm.nativeimage.ObjectHandles
meth public abstract <%0 extends java.lang.Object> {%%0} get(org.graalvm.nativeimage.ObjectHandle)
meth public abstract org.graalvm.nativeimage.ObjectHandle create(java.lang.Object)
meth public abstract void destroy(org.graalvm.nativeimage.ObjectHandle)
meth public static org.graalvm.nativeimage.ObjectHandles create()
meth public static org.graalvm.nativeimage.ObjectHandles getGlobal()

CLSS public abstract interface org.graalvm.nativeimage.PinnedObject
intf java.lang.AutoCloseable
meth public abstract <%0 extends org.graalvm.word.PointerBase> {%%0} addressOfArrayElement(int)
meth public abstract java.lang.Object getObject()
meth public abstract org.graalvm.word.PointerBase addressOfObject()
meth public abstract void close()
meth public static org.graalvm.nativeimage.PinnedObject create(java.lang.Object)

CLSS public abstract interface org.graalvm.nativeimage.Platform
fld public final static java.lang.String PLATFORM_PROPERTY_NAME = "svm.platform"
innr public abstract interface static AARCH64
innr public abstract interface static AMD64
innr public abstract interface static ANDROID
innr public abstract interface static DARWIN
innr public abstract interface static DARWIN_AARCH64
innr public abstract interface static DARWIN_AMD64
innr public abstract interface static IOS
innr public abstract interface static LINUX
innr public abstract interface static LINUX_AARCH64_BASE
innr public abstract interface static LINUX_AMD64_BASE
innr public abstract interface static MACOS
innr public abstract interface static RISCV64
innr public abstract interface static WINDOWS
innr public final static ANDROID_AARCH64
innr public final static HOSTED_ONLY
innr public final static IOS_AARCH64
innr public final static IOS_AMD64
innr public final static LINUX_AARCH64
innr public final static LINUX_RISCV64
innr public final static MACOS_AARCH64
innr public final static MACOS_AMD64
innr public final static WINDOWS_AARCH64
innr public final static WINDOWS_AMD64
innr public static LINUX_AMD64
meth public java.lang.String getArchitecture()
meth public java.lang.String getOS()
meth public static boolean includedIn(java.lang.Class<? extends org.graalvm.nativeimage.Platform>)

CLSS public abstract interface static org.graalvm.nativeimage.Platform$AARCH64
 outer org.graalvm.nativeimage.Platform
intf org.graalvm.nativeimage.Platform
intf org.graalvm.nativeimage.impl.InternalPlatform$NATIVE_ONLY
meth public java.lang.String getArchitecture()

CLSS public abstract interface static org.graalvm.nativeimage.Platform$AMD64
 outer org.graalvm.nativeimage.Platform
intf org.graalvm.nativeimage.Platform
intf org.graalvm.nativeimage.impl.InternalPlatform$NATIVE_ONLY
meth public java.lang.String getArchitecture()

CLSS public abstract interface static org.graalvm.nativeimage.Platform$ANDROID
 outer org.graalvm.nativeimage.Platform
intf org.graalvm.nativeimage.Platform$LINUX
meth public java.lang.String getOS()

CLSS public final static org.graalvm.nativeimage.Platform$ANDROID_AARCH64
 outer org.graalvm.nativeimage.Platform
cons public init()
intf org.graalvm.nativeimage.Platform$ANDROID
intf org.graalvm.nativeimage.Platform$LINUX_AARCH64_BASE
supr java.lang.Object

CLSS public abstract interface static org.graalvm.nativeimage.Platform$DARWIN
 outer org.graalvm.nativeimage.Platform
intf org.graalvm.nativeimage.impl.InternalPlatform$NATIVE_ONLY
intf org.graalvm.nativeimage.impl.InternalPlatform$PLATFORM_JNI

CLSS public abstract interface static org.graalvm.nativeimage.Platform$DARWIN_AARCH64
 outer org.graalvm.nativeimage.Platform
intf org.graalvm.nativeimage.Platform$AARCH64
intf org.graalvm.nativeimage.Platform$DARWIN

CLSS public abstract interface static org.graalvm.nativeimage.Platform$DARWIN_AMD64
 outer org.graalvm.nativeimage.Platform
intf org.graalvm.nativeimage.Platform$AMD64
intf org.graalvm.nativeimage.Platform$DARWIN

CLSS public final static org.graalvm.nativeimage.Platform$HOSTED_ONLY
 outer org.graalvm.nativeimage.Platform
intf org.graalvm.nativeimage.Platform
supr java.lang.Object

CLSS public abstract interface static org.graalvm.nativeimage.Platform$IOS
 outer org.graalvm.nativeimage.Platform
intf org.graalvm.nativeimage.Platform$DARWIN
meth public java.lang.String getOS()

CLSS public final static org.graalvm.nativeimage.Platform$IOS_AARCH64
 outer org.graalvm.nativeimage.Platform
cons public init()
intf org.graalvm.nativeimage.Platform$DARWIN_AARCH64
intf org.graalvm.nativeimage.Platform$IOS
supr java.lang.Object

CLSS public final static org.graalvm.nativeimage.Platform$IOS_AMD64
 outer org.graalvm.nativeimage.Platform
cons public init()
intf org.graalvm.nativeimage.Platform$DARWIN_AMD64
intf org.graalvm.nativeimage.Platform$IOS
supr java.lang.Object

CLSS public abstract interface static org.graalvm.nativeimage.Platform$LINUX
 outer org.graalvm.nativeimage.Platform
intf org.graalvm.nativeimage.impl.InternalPlatform$NATIVE_ONLY
intf org.graalvm.nativeimage.impl.InternalPlatform$PLATFORM_JNI
meth public java.lang.String getOS()

CLSS public final static org.graalvm.nativeimage.Platform$LINUX_AARCH64
 outer org.graalvm.nativeimage.Platform
cons public init()
intf org.graalvm.nativeimage.Platform$LINUX
intf org.graalvm.nativeimage.Platform$LINUX_AARCH64_BASE
supr java.lang.Object

CLSS public abstract interface static org.graalvm.nativeimage.Platform$LINUX_AARCH64_BASE
 outer org.graalvm.nativeimage.Platform
intf org.graalvm.nativeimage.Platform$AARCH64
intf org.graalvm.nativeimage.Platform$LINUX

CLSS public static org.graalvm.nativeimage.Platform$LINUX_AMD64
 outer org.graalvm.nativeimage.Platform
cons public init()
intf org.graalvm.nativeimage.Platform$LINUX
intf org.graalvm.nativeimage.Platform$LINUX_AMD64_BASE
supr java.lang.Object

CLSS public abstract interface static org.graalvm.nativeimage.Platform$LINUX_AMD64_BASE
 outer org.graalvm.nativeimage.Platform
intf org.graalvm.nativeimage.Platform$AMD64
intf org.graalvm.nativeimage.Platform$LINUX

CLSS public final static org.graalvm.nativeimage.Platform$LINUX_RISCV64
 outer org.graalvm.nativeimage.Platform
cons public init()
intf org.graalvm.nativeimage.Platform$LINUX
intf org.graalvm.nativeimage.Platform$RISCV64
supr java.lang.Object

CLSS public abstract interface static org.graalvm.nativeimage.Platform$MACOS
 outer org.graalvm.nativeimage.Platform
intf org.graalvm.nativeimage.Platform$DARWIN
meth public java.lang.String getOS()

CLSS public final static org.graalvm.nativeimage.Platform$MACOS_AARCH64
 outer org.graalvm.nativeimage.Platform
cons public init()
intf org.graalvm.nativeimage.Platform$DARWIN_AARCH64
intf org.graalvm.nativeimage.Platform$MACOS
supr java.lang.Object

CLSS public final static org.graalvm.nativeimage.Platform$MACOS_AMD64
 outer org.graalvm.nativeimage.Platform
cons public init()
intf org.graalvm.nativeimage.Platform$DARWIN_AMD64
intf org.graalvm.nativeimage.Platform$MACOS
supr java.lang.Object

CLSS public abstract interface static org.graalvm.nativeimage.Platform$RISCV64
 outer org.graalvm.nativeimage.Platform
intf org.graalvm.nativeimage.Platform
meth public java.lang.String getArchitecture()

CLSS public abstract interface static org.graalvm.nativeimage.Platform$WINDOWS
 outer org.graalvm.nativeimage.Platform
intf org.graalvm.nativeimage.impl.InternalPlatform$NATIVE_ONLY
intf org.graalvm.nativeimage.impl.InternalPlatform$PLATFORM_JNI
meth public java.lang.String getOS()

CLSS public final static org.graalvm.nativeimage.Platform$WINDOWS_AARCH64
 outer org.graalvm.nativeimage.Platform
cons public init()
intf org.graalvm.nativeimage.Platform$AARCH64
intf org.graalvm.nativeimage.Platform$WINDOWS
supr java.lang.Object

CLSS public final static org.graalvm.nativeimage.Platform$WINDOWS_AMD64
 outer org.graalvm.nativeimage.Platform
cons public init()
intf org.graalvm.nativeimage.Platform$AMD64
intf org.graalvm.nativeimage.Platform$WINDOWS
supr java.lang.Object

CLSS public abstract interface !annotation org.graalvm.nativeimage.Platforms
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, CONSTRUCTOR, FIELD, PACKAGE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<? extends org.graalvm.nativeimage.Platform>[] value()

CLSS public final org.graalvm.nativeimage.ProcessProperties
meth public !varargs static void exec(java.nio.file.Path,java.lang.String[])
meth public static boolean destroy(long)
meth public static boolean destroyForcibly(long)
meth public static boolean isAlive(long)
meth public static boolean setArgumentVectorProgramName(java.lang.String)
meth public static int getArgumentVectorBlockSize()
meth public static int waitForProcessExit(long)
meth public static java.lang.String getArgumentVectorProgramName()
meth public static java.lang.String getExecutableName()
meth public static java.lang.String getObjectFile(java.lang.String)
meth public static java.lang.String getObjectFile(org.graalvm.nativeimage.c.function.CEntryPointLiteral<?>)
meth public static java.lang.String setLocale(java.lang.String,java.lang.String)
meth public static long getProcessID()
meth public static long getProcessID(java.lang.Process)
meth public static void exec(java.nio.file.Path,java.lang.String[],java.util.Map<java.lang.String,java.lang.String>)
supr java.lang.Object

CLSS public final org.graalvm.nativeimage.RuntimeOptions
innr public final static !enum OptionClass
meth public static <%0 extends java.lang.Object> {%%0} get(java.lang.String)
meth public static org.graalvm.options.OptionDescriptors getOptions()
 anno 0 java.lang.Deprecated(null forRemoval=true, null since="23.0")
meth public static org.graalvm.options.OptionDescriptors getOptions(java.util.EnumSet<org.graalvm.nativeimage.RuntimeOptions$OptionClass>)
 anno 0 java.lang.Deprecated(null forRemoval=true, null since="23.0")
meth public static void set(java.lang.String,java.lang.Object)
supr java.lang.Object

CLSS public final static !enum org.graalvm.nativeimage.RuntimeOptions$OptionClass
 outer org.graalvm.nativeimage.RuntimeOptions
 anno 0 java.lang.Deprecated(null forRemoval=true, null since="23.0")
fld public final static org.graalvm.nativeimage.RuntimeOptions$OptionClass Compiler
fld public final static org.graalvm.nativeimage.RuntimeOptions$OptionClass VM
meth public static org.graalvm.nativeimage.RuntimeOptions$OptionClass valueOf(java.lang.String)
meth public static org.graalvm.nativeimage.RuntimeOptions$OptionClass[] values()
supr java.lang.Enum<org.graalvm.nativeimage.RuntimeOptions$OptionClass>

CLSS public final org.graalvm.nativeimage.StackValue
meth public static <%0 extends org.graalvm.word.PointerBase> {%%0} get(int)
meth public static <%0 extends org.graalvm.word.PointerBase> {%%0} get(int,int)
meth public static <%0 extends org.graalvm.word.PointerBase> {%%0} get(int,java.lang.Class<{%%0}>)
meth public static <%0 extends org.graalvm.word.PointerBase> {%%0} get(java.lang.Class<{%%0}>)
supr java.lang.Object

CLSS public final org.graalvm.nativeimage.Threading
innr public abstract interface static RecurringCallback
innr public abstract interface static RecurringCallbackAccess
meth public static void registerRecurringCallback(long,java.util.concurrent.TimeUnit,org.graalvm.nativeimage.Threading$RecurringCallback)
supr java.lang.Object

CLSS public abstract interface static org.graalvm.nativeimage.Threading$RecurringCallback
 outer org.graalvm.nativeimage.Threading
 anno 0 java.lang.FunctionalInterface()
meth public abstract void run(org.graalvm.nativeimage.Threading$RecurringCallbackAccess)

CLSS public abstract interface static org.graalvm.nativeimage.Threading$RecurringCallbackAccess
 outer org.graalvm.nativeimage.Threading
meth public abstract void throwException(java.lang.Throwable)

CLSS public final org.graalvm.nativeimage.UnmanagedMemory
meth public static <%0 extends org.graalvm.word.PointerBase> {%%0} calloc(int)
meth public static <%0 extends org.graalvm.word.PointerBase> {%%0} calloc(org.graalvm.word.UnsignedWord)
meth public static <%0 extends org.graalvm.word.PointerBase> {%%0} malloc(int)
meth public static <%0 extends org.graalvm.word.PointerBase> {%%0} malloc(org.graalvm.word.UnsignedWord)
meth public static <%0 extends org.graalvm.word.PointerBase> {%%0} realloc({%%0},org.graalvm.word.UnsignedWord)
meth public static void free(org.graalvm.word.PointerBase)
supr java.lang.Object

CLSS public final org.graalvm.nativeimage.VMRuntime
meth public static void dumpHeap(java.lang.String,boolean) throws java.io.IOException
meth public static void initialize()
meth public static void shutdown()
supr java.lang.Object

CLSS public abstract interface !annotation org.graalvm.nativeimage.c.struct.CStruct
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean addStructKeyword()
meth public abstract !hasdefault boolean isIncomplete()
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface org.graalvm.nativeimage.impl.InternalPlatform
innr public abstract interface static NATIVE_ONLY
innr public abstract interface static PLATFORM_JNI

CLSS public abstract interface static org.graalvm.nativeimage.impl.InternalPlatform$NATIVE_ONLY
 outer org.graalvm.nativeimage.impl.InternalPlatform
intf org.graalvm.nativeimage.Platform

CLSS public abstract interface static org.graalvm.nativeimage.impl.InternalPlatform$PLATFORM_JNI
 outer org.graalvm.nativeimage.impl.InternalPlatform
intf org.graalvm.nativeimage.Platform

CLSS public final !enum org.graalvm.options.OptionCategory
fld public final static org.graalvm.options.OptionCategory EXPERT
fld public final static org.graalvm.options.OptionCategory INTERNAL
fld public final static org.graalvm.options.OptionCategory USER
meth public static org.graalvm.options.OptionCategory valueOf(java.lang.String)
meth public static org.graalvm.options.OptionCategory[] values()
supr java.lang.Enum<org.graalvm.options.OptionCategory>

CLSS public final org.graalvm.options.OptionDescriptor
innr public final Builder
meth public boolean equals(java.lang.Object)
meth public boolean isDeprecated()
meth public boolean isOptionMap()
meth public int hashCode()
meth public java.lang.String getDeprecationMessage()
meth public java.lang.String getHelp()
meth public java.lang.String getName()
meth public java.lang.String getUsageSyntax()
meth public java.lang.String toString()
meth public org.graalvm.options.OptionCategory getCategory()
meth public org.graalvm.options.OptionKey<?> getKey()
meth public org.graalvm.options.OptionStability getStability()
meth public static <%0 extends java.lang.Object> org.graalvm.options.OptionDescriptor$Builder newBuilder(org.graalvm.options.OptionKey<{%%0}>,java.lang.String)
supr java.lang.Object
hfds EMPTY,category,deprecated,deprecationMessage,help,key,name,stability,usageSyntax

CLSS public final org.graalvm.options.OptionDescriptor$Builder
 outer org.graalvm.options.OptionDescriptor
meth public org.graalvm.options.OptionDescriptor build()
meth public org.graalvm.options.OptionDescriptor$Builder category(org.graalvm.options.OptionCategory)
meth public org.graalvm.options.OptionDescriptor$Builder deprecated(boolean)
meth public org.graalvm.options.OptionDescriptor$Builder deprecationMessage(java.lang.String)
meth public org.graalvm.options.OptionDescriptor$Builder help(java.lang.String)
meth public org.graalvm.options.OptionDescriptor$Builder stability(org.graalvm.options.OptionStability)
meth public org.graalvm.options.OptionDescriptor$Builder usageSyntax(java.lang.String)
supr java.lang.Object
hfds category,deprecated,deprecationMessage,help,key,name,stability,usageSyntax

CLSS public abstract interface org.graalvm.options.OptionDescriptors
fld public final static org.graalvm.options.OptionDescriptors EMPTY
intf java.lang.Iterable<org.graalvm.options.OptionDescriptor>
meth public !varargs static org.graalvm.options.OptionDescriptors createUnion(org.graalvm.options.OptionDescriptors[])
meth public abstract java.util.Iterator<org.graalvm.options.OptionDescriptor> iterator()
meth public abstract org.graalvm.options.OptionDescriptor get(java.lang.String)
meth public static org.graalvm.options.OptionDescriptors create(java.util.List<org.graalvm.options.OptionDescriptor>)

CLSS public final org.graalvm.options.OptionKey<%0 extends java.lang.Object>
cons public init({org.graalvm.options.OptionKey%0})
cons public init({org.graalvm.options.OptionKey%0},org.graalvm.options.OptionType<{org.graalvm.options.OptionKey%0}>)
meth public boolean hasBeenSet(org.graalvm.options.OptionValues)
meth public org.graalvm.options.OptionType<{org.graalvm.options.OptionKey%0}> getType()
meth public static <%0 extends java.lang.Object> org.graalvm.options.OptionKey<org.graalvm.options.OptionMap<{%%0}>> mapOf(java.lang.Class<{%%0}>)
meth public {org.graalvm.options.OptionKey%0} getDefaultValue()
meth public {org.graalvm.options.OptionKey%0} getValue(org.graalvm.options.OptionValues)
supr java.lang.Object
hfds defaultValue,type

CLSS public final org.graalvm.options.OptionMap<%0 extends java.lang.Object>
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.util.Set<java.util.Map$Entry<java.lang.String,{org.graalvm.options.OptionMap%0}>> entrySet()
meth public static <%0 extends java.lang.Object> org.graalvm.options.OptionMap<{%%0}> empty()
meth public {org.graalvm.options.OptionMap%0} get(java.lang.String)
supr java.lang.Object
hfds EMPTY,backingMap,readonlyMap

CLSS public final !enum org.graalvm.options.OptionStability
fld public final static org.graalvm.options.OptionStability EXPERIMENTAL
fld public final static org.graalvm.options.OptionStability STABLE
meth public static org.graalvm.options.OptionStability valueOf(java.lang.String)
meth public static org.graalvm.options.OptionStability[] values()
supr java.lang.Enum<org.graalvm.options.OptionStability>

CLSS public final org.graalvm.options.OptionType<%0 extends java.lang.Object>
cons public init(java.lang.String,java.util.function.Function<java.lang.String,{org.graalvm.options.OptionType%0}>)
cons public init(java.lang.String,java.util.function.Function<java.lang.String,{org.graalvm.options.OptionType%0}>,java.util.function.Consumer<{org.graalvm.options.OptionType%0}>)
cons public init(java.lang.String,{org.graalvm.options.OptionType%0},java.util.function.Function<java.lang.String,{org.graalvm.options.OptionType%0}>)
 anno 0 java.lang.Deprecated(null since="19.0")
cons public init(java.lang.String,{org.graalvm.options.OptionType%0},java.util.function.Function<java.lang.String,{org.graalvm.options.OptionType%0}>,java.util.function.Consumer<{org.graalvm.options.OptionType%0}>)
 anno 0 java.lang.Deprecated(null since="19.0")
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Object> org.graalvm.options.OptionType<{%%0}> defaultType(java.lang.Class<{%%0}>)
meth public static <%0 extends java.lang.Object> org.graalvm.options.OptionType<{%%0}> defaultType({%%0})
meth public void validate({org.graalvm.options.OptionType%0})
meth public {org.graalvm.options.OptionType%0} convert(java.lang.Object,java.lang.String,java.lang.String)
meth public {org.graalvm.options.OptionType%0} convert(java.lang.String)
meth public {org.graalvm.options.OptionType%0} getDefaultValue()
 anno 0 java.lang.Deprecated(null since="19.0")
supr java.lang.Object
hfds DEFAULTTYPES,EMPTY_VALIDATOR,converter,isDefaultType,isOptionMap,name,validator
hcls Converter

CLSS public abstract interface org.graalvm.options.OptionValues
meth public <%0 extends java.lang.Object> void set(org.graalvm.options.OptionKey<{%%0}>,{%%0})
 anno 0 java.lang.Deprecated(null since="22.0")
meth public abstract <%0 extends java.lang.Object> {%%0} get(org.graalvm.options.OptionKey<{%%0}>)
meth public abstract boolean hasBeenSet(org.graalvm.options.OptionKey<?>)
meth public abstract org.graalvm.options.OptionDescriptors getDescriptors()
meth public boolean hasSetOptions()

CLSS public final org.graalvm.polyglot.Context
innr public final Builder
intf java.lang.AutoCloseable
meth public !varargs static org.graalvm.polyglot.Context create(java.lang.String[])
meth public !varargs static org.graalvm.polyglot.Context$Builder newBuilder(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public boolean initialize(java.lang.String)
meth public int hashCode()
meth public org.graalvm.polyglot.Engine getEngine()
meth public org.graalvm.polyglot.Value asValue(java.lang.Object)
meth public org.graalvm.polyglot.Value eval(java.lang.String,java.lang.CharSequence)
meth public org.graalvm.polyglot.Value eval(org.graalvm.polyglot.Source)
meth public org.graalvm.polyglot.Value getBindings(java.lang.String)
meth public org.graalvm.polyglot.Value getPolyglotBindings()
meth public org.graalvm.polyglot.Value parse(java.lang.String,java.lang.CharSequence)
meth public org.graalvm.polyglot.Value parse(org.graalvm.polyglot.Source)
meth public static org.graalvm.polyglot.Context getCurrent()
meth public void close()
meth public void close(boolean)
meth public void enter()
meth public void interrupt(java.time.Duration) throws java.util.concurrent.TimeoutException
meth public void leave()
meth public void resetLimits()
meth public void safepoint()
supr java.lang.Object
hfds ALL_HOST_CLASSES,EMPTY,NO_HOST_CLASSES,UNSET_HOST_LOOKUP,currentAPI,dispatch,engine,receiver

CLSS public final org.graalvm.polyglot.Context$Builder
 outer org.graalvm.polyglot.Context
meth public org.graalvm.polyglot.Context build()
meth public org.graalvm.polyglot.Context$Builder allowAllAccess(boolean)
meth public org.graalvm.polyglot.Context$Builder allowCreateProcess(boolean)
meth public org.graalvm.polyglot.Context$Builder allowCreateThread(boolean)
meth public org.graalvm.polyglot.Context$Builder allowEnvironmentAccess(org.graalvm.polyglot.EnvironmentAccess)
meth public org.graalvm.polyglot.Context$Builder allowExperimentalOptions(boolean)
meth public org.graalvm.polyglot.Context$Builder allowHostAccess(boolean)
 anno 0 java.lang.Deprecated(null since="19.0")
meth public org.graalvm.polyglot.Context$Builder allowHostAccess(org.graalvm.polyglot.HostAccess)
meth public org.graalvm.polyglot.Context$Builder allowHostClassLoading(boolean)
meth public org.graalvm.polyglot.Context$Builder allowHostClassLookup(java.util.function.Predicate<java.lang.String>)
meth public org.graalvm.polyglot.Context$Builder allowIO(boolean)
 anno 0 java.lang.Deprecated(null since="23.0")
meth public org.graalvm.polyglot.Context$Builder allowIO(org.graalvm.polyglot.io.IOAccess)
meth public org.graalvm.polyglot.Context$Builder allowInnerContextOptions(boolean)
meth public org.graalvm.polyglot.Context$Builder allowNativeAccess(boolean)
meth public org.graalvm.polyglot.Context$Builder allowPolyglotAccess(org.graalvm.polyglot.PolyglotAccess)
meth public org.graalvm.polyglot.Context$Builder allowValueSharing(boolean)
meth public org.graalvm.polyglot.Context$Builder arguments(java.lang.String,java.lang.String[])
meth public org.graalvm.polyglot.Context$Builder currentWorkingDirectory(java.nio.file.Path)
meth public org.graalvm.polyglot.Context$Builder engine(org.graalvm.polyglot.Engine)
meth public org.graalvm.polyglot.Context$Builder environment(java.lang.String,java.lang.String)
meth public org.graalvm.polyglot.Context$Builder environment(java.util.Map<java.lang.String,java.lang.String>)
meth public org.graalvm.polyglot.Context$Builder err(java.io.OutputStream)
meth public org.graalvm.polyglot.Context$Builder fileSystem(org.graalvm.polyglot.io.FileSystem)
 anno 0 java.lang.Deprecated(null since="23.0")
meth public org.graalvm.polyglot.Context$Builder hostClassFilter(java.util.function.Predicate<java.lang.String>)
 anno 0 java.lang.Deprecated(null since="19.0")
meth public org.graalvm.polyglot.Context$Builder hostClassLoader(java.lang.ClassLoader)
meth public org.graalvm.polyglot.Context$Builder in(java.io.InputStream)
meth public org.graalvm.polyglot.Context$Builder logHandler(java.io.OutputStream)
meth public org.graalvm.polyglot.Context$Builder logHandler(java.util.logging.Handler)
meth public org.graalvm.polyglot.Context$Builder option(java.lang.String,java.lang.String)
meth public org.graalvm.polyglot.Context$Builder options(java.util.Map<java.lang.String,java.lang.String>)
meth public org.graalvm.polyglot.Context$Builder out(java.io.OutputStream)
meth public org.graalvm.polyglot.Context$Builder processHandler(org.graalvm.polyglot.io.ProcessHandler)
meth public org.graalvm.polyglot.Context$Builder resourceLimits(org.graalvm.polyglot.ResourceLimits)
meth public org.graalvm.polyglot.Context$Builder sandbox(org.graalvm.polyglot.SandboxPolicy)
meth public org.graalvm.polyglot.Context$Builder serverTransport(org.graalvm.polyglot.io.MessageTransport)
meth public org.graalvm.polyglot.Context$Builder timeZone(java.time.ZoneId)
meth public org.graalvm.polyglot.Context$Builder useSystemExit(boolean)
supr java.lang.Object
hfds allowAllAccess,allowCreateProcess,allowCreateThread,allowExperimentalOptions,allowHostAccess,allowHostClassLoading,allowIO,allowInnerContextOptions,allowNativeAccess,allowValueSharing,arguments,currentWorkingDirectory,customFileSystem,customLogHandler,environment,environmentAccess,err,hostAccess,hostClassFilter,hostClassLoader,in,ioAccess,messageTransport,options,out,permittedLanguages,polyglotAccess,processHandler,resourceLimits,sandboxPolicy,sharedEngine,useSystemExit,zone

CLSS public final org.graalvm.polyglot.Engine
innr public final Builder
intf java.lang.AutoCloseable
meth public !varargs static org.graalvm.polyglot.Engine create(java.lang.String[])
meth public !varargs static org.graalvm.polyglot.Engine$Builder newBuilder(java.lang.String[])
meth public java.lang.String getImplementationName()
meth public java.lang.String getVersion()
meth public java.util.Map<java.lang.String,org.graalvm.polyglot.Instrument> getInstruments()
meth public java.util.Map<java.lang.String,org.graalvm.polyglot.Language> getLanguages()
meth public java.util.Set<org.graalvm.polyglot.Source> getCachedSources()
meth public org.graalvm.options.OptionDescriptors getOptions()
meth public static java.nio.file.Path findHome()
meth public static org.graalvm.polyglot.Engine create()
meth public static org.graalvm.polyglot.Engine$Builder newBuilder()
meth public void close()
meth public void close(boolean)
supr java.lang.Object
hfds EMPTY,ENGINES,currentAPI,dispatch,initializationException,receiver,shutdownHookInitialized
hcls APIAccessImpl,EngineShutDownHook,ImplHolder,PolyglotInvalid

CLSS public final org.graalvm.polyglot.Engine$Builder
 outer org.graalvm.polyglot.Engine
meth public org.graalvm.polyglot.Engine build()
meth public org.graalvm.polyglot.Engine$Builder allowExperimentalOptions(boolean)
meth public org.graalvm.polyglot.Engine$Builder err(java.io.OutputStream)
meth public org.graalvm.polyglot.Engine$Builder in(java.io.InputStream)
meth public org.graalvm.polyglot.Engine$Builder logHandler(java.io.OutputStream)
meth public org.graalvm.polyglot.Engine$Builder logHandler(java.util.logging.Handler)
meth public org.graalvm.polyglot.Engine$Builder option(java.lang.String,java.lang.String)
meth public org.graalvm.polyglot.Engine$Builder options(java.util.Map<java.lang.String,java.lang.String>)
meth public org.graalvm.polyglot.Engine$Builder out(java.io.OutputStream)
meth public org.graalvm.polyglot.Engine$Builder sandbox(org.graalvm.polyglot.SandboxPolicy)
meth public org.graalvm.polyglot.Engine$Builder serverTransport(org.graalvm.polyglot.io.MessageTransport)
meth public org.graalvm.polyglot.Engine$Builder useSystemProperties(boolean)
supr java.lang.Object
hfds allowExperimentalOptionSystemPropertyValue,allowExperimentalOptions,boundEngine,customLogHandler,err,in,messageTransport,options,out,permittedLanguages,sandboxPolicy,useSystemProperties

CLSS public final org.graalvm.polyglot.EnvironmentAccess
fld public final static org.graalvm.polyglot.EnvironmentAccess INHERIT
fld public final static org.graalvm.polyglot.EnvironmentAccess NONE
meth public java.lang.String toString()
supr java.lang.Object
hfds name

CLSS public final org.graalvm.polyglot.HostAccess
fld public final static org.graalvm.polyglot.HostAccess ALL
fld public final static org.graalvm.polyglot.HostAccess CONSTRAINED
fld public final static org.graalvm.polyglot.HostAccess EXPLICIT
fld public final static org.graalvm.polyglot.HostAccess ISOLATED
fld public final static org.graalvm.polyglot.HostAccess NONE
fld public final static org.graalvm.polyglot.HostAccess SCOPED
fld public final static org.graalvm.polyglot.HostAccess UNTRUSTED
innr public abstract interface static !annotation DisableMethodScoping
innr public abstract interface static !annotation Export
innr public abstract interface static !annotation Implementable
innr public final Builder
innr public final static !enum MutableTargetMapping
innr public final static !enum TargetMappingPrecedence
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public static org.graalvm.polyglot.HostAccess$Builder newBuilder()
meth public static org.graalvm.polyglot.HostAccess$Builder newBuilder(org.graalvm.polyglot.HostAccess)
supr java.lang.Object
hfds EMPTY,accessAnnotations,allowAccessInheritance,allowAllClassImplementations,allowAllInterfaceImplementations,allowArrayAccess,allowBigIntegerNumberAccess,allowBufferAccess,allowIterableAccess,allowIteratorAccess,allowListAccess,allowMapAccess,allowMutableTargetMappings,allowPublic,disableMethodScoping,disableMethodScopingAnnotations,excludeTypes,impl,implementableAnnotations,implementableTypes,members,methodScopingDefault,name,targetMappings

CLSS public final org.graalvm.polyglot.HostAccess$Builder
 outer org.graalvm.polyglot.HostAccess
meth public !varargs org.graalvm.polyglot.HostAccess$Builder allowMutableTargetMappings(org.graalvm.polyglot.HostAccess$MutableTargetMapping[])
meth public <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.polyglot.HostAccess$Builder targetTypeMapping(java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,java.util.function.Predicate<{%%0}>,java.util.function.Function<{%%0},{%%1}>)
meth public <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.polyglot.HostAccess$Builder targetTypeMapping(java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,java.util.function.Predicate<{%%0}>,java.util.function.Function<{%%0},{%%1}>,org.graalvm.polyglot.HostAccess$TargetMappingPrecedence)
meth public org.graalvm.polyglot.HostAccess build()
meth public org.graalvm.polyglot.HostAccess$Builder allowAccess(java.lang.reflect.Executable)
meth public org.graalvm.polyglot.HostAccess$Builder allowAccess(java.lang.reflect.Field)
meth public org.graalvm.polyglot.HostAccess$Builder allowAccessAnnotatedBy(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public org.graalvm.polyglot.HostAccess$Builder allowAccessInheritance(boolean)
meth public org.graalvm.polyglot.HostAccess$Builder allowAllClassImplementations(boolean)
meth public org.graalvm.polyglot.HostAccess$Builder allowAllImplementations(boolean)
meth public org.graalvm.polyglot.HostAccess$Builder allowArrayAccess(boolean)
meth public org.graalvm.polyglot.HostAccess$Builder allowBigIntegerNumberAccess(boolean)
meth public org.graalvm.polyglot.HostAccess$Builder allowBufferAccess(boolean)
meth public org.graalvm.polyglot.HostAccess$Builder allowImplementations(java.lang.Class<?>)
meth public org.graalvm.polyglot.HostAccess$Builder allowImplementationsAnnotatedBy(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public org.graalvm.polyglot.HostAccess$Builder allowIterableAccess(boolean)
meth public org.graalvm.polyglot.HostAccess$Builder allowIteratorAccess(boolean)
meth public org.graalvm.polyglot.HostAccess$Builder allowListAccess(boolean)
meth public org.graalvm.polyglot.HostAccess$Builder allowMapAccess(boolean)
meth public org.graalvm.polyglot.HostAccess$Builder allowPublicAccess(boolean)
meth public org.graalvm.polyglot.HostAccess$Builder denyAccess(java.lang.Class<?>)
meth public org.graalvm.polyglot.HostAccess$Builder denyAccess(java.lang.Class<?>,boolean)
meth public org.graalvm.polyglot.HostAccess$Builder disableMethodScoping(java.lang.reflect.Executable)
meth public org.graalvm.polyglot.HostAccess$Builder disableMethodScopingAnnotatedBy(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public org.graalvm.polyglot.HostAccess$Builder methodScoping(boolean)
supr java.lang.Object
hfds accessAnnotations,allowAccessInheritance,allowAllClassImplementations,allowAllImplementations,allowArrayAccess,allowBigIntegerNumberAccess,allowBufferAccess,allowIterableAccess,allowIteratorAccess,allowListAccess,allowMapAccess,allowMutableTargetMappings,allowPublic,disableMethodScoping,disableMethodScopingAnnotations,excludeTypes,implementableTypes,implementationAnnotations,members,methodScopingDefault,name,targetMappings

CLSS public abstract interface static !annotation org.graalvm.polyglot.HostAccess$DisableMethodScoping
 outer org.graalvm.polyglot.HostAccess
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface static !annotation org.graalvm.polyglot.HostAccess$Export
 outer org.graalvm.polyglot.HostAccess
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, METHOD])
intf java.lang.annotation.Annotation

CLSS public abstract interface static !annotation org.graalvm.polyglot.HostAccess$Implementable
 outer org.graalvm.polyglot.HostAccess
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public final static !enum org.graalvm.polyglot.HostAccess$MutableTargetMapping
 outer org.graalvm.polyglot.HostAccess
fld public final static org.graalvm.polyglot.HostAccess$MutableTargetMapping ARRAY_TO_JAVA_LIST
fld public final static org.graalvm.polyglot.HostAccess$MutableTargetMapping EXECUTABLE_TO_JAVA_INTERFACE
fld public final static org.graalvm.polyglot.HostAccess$MutableTargetMapping HASH_TO_JAVA_MAP
fld public final static org.graalvm.polyglot.HostAccess$MutableTargetMapping ITERABLE_TO_JAVA_ITERABLE
fld public final static org.graalvm.polyglot.HostAccess$MutableTargetMapping ITERATOR_TO_JAVA_ITERATOR
fld public final static org.graalvm.polyglot.HostAccess$MutableTargetMapping MEMBERS_TO_JAVA_INTERFACE
fld public final static org.graalvm.polyglot.HostAccess$MutableTargetMapping MEMBERS_TO_JAVA_MAP
meth public static org.graalvm.polyglot.HostAccess$MutableTargetMapping valueOf(java.lang.String)
meth public static org.graalvm.polyglot.HostAccess$MutableTargetMapping[] values()
supr java.lang.Enum<org.graalvm.polyglot.HostAccess$MutableTargetMapping>

CLSS public final static !enum org.graalvm.polyglot.HostAccess$TargetMappingPrecedence
 outer org.graalvm.polyglot.HostAccess
fld public final static org.graalvm.polyglot.HostAccess$TargetMappingPrecedence HIGH
fld public final static org.graalvm.polyglot.HostAccess$TargetMappingPrecedence HIGHEST
fld public final static org.graalvm.polyglot.HostAccess$TargetMappingPrecedence LOW
fld public final static org.graalvm.polyglot.HostAccess$TargetMappingPrecedence LOWEST
meth public static org.graalvm.polyglot.HostAccess$TargetMappingPrecedence valueOf(java.lang.String)
meth public static org.graalvm.polyglot.HostAccess$TargetMappingPrecedence[] values()
supr java.lang.Enum<org.graalvm.polyglot.HostAccess$TargetMappingPrecedence>

CLSS public final org.graalvm.polyglot.Instrument
meth public <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public java.lang.String getVersion()
meth public java.lang.String getWebsite()
meth public org.graalvm.options.OptionDescriptors getOptions()
supr java.lang.Object
hfds dispatch,receiver

CLSS public final org.graalvm.polyglot.Language
meth public boolean isInteractive()
meth public java.lang.String getDefaultMimeType()
meth public java.lang.String getId()
meth public java.lang.String getImplementationName()
meth public java.lang.String getName()
meth public java.lang.String getVersion()
meth public java.lang.String getWebsite()
meth public java.util.Set<java.lang.String> getMimeTypes()
meth public org.graalvm.options.OptionDescriptors getOptions()
supr java.lang.Object
hfds dispatch,receiver

CLSS public final org.graalvm.polyglot.PolyglotAccess
fld public final static org.graalvm.polyglot.PolyglotAccess ALL
fld public final static org.graalvm.polyglot.PolyglotAccess NONE
innr public final Builder
meth public static org.graalvm.polyglot.PolyglotAccess$Builder newBuilder()
supr java.lang.Object
hfds EMPTY,EMPTY_EVAL_ACCESS,allAccess,bindingsAccess,evalAccess

CLSS public final org.graalvm.polyglot.PolyglotAccess$Builder
 outer org.graalvm.polyglot.PolyglotAccess
meth public !varargs org.graalvm.polyglot.PolyglotAccess$Builder allowEvalBetween(java.lang.String[])
meth public !varargs org.graalvm.polyglot.PolyglotAccess$Builder denyEvalBetween(java.lang.String[])
meth public org.graalvm.polyglot.PolyglotAccess build()
meth public org.graalvm.polyglot.PolyglotAccess$Builder allowBindingsAccess(java.lang.String)
meth public org.graalvm.polyglot.PolyglotAccess$Builder allowEval(java.lang.String,java.lang.String)
meth public org.graalvm.polyglot.PolyglotAccess$Builder denyBindingsAccess(java.lang.String)
meth public org.graalvm.polyglot.PolyglotAccess$Builder denyEval(java.lang.String,java.lang.String)
supr java.lang.Object
hfds bindingsAccess,evalAccess

CLSS public final org.graalvm.polyglot.PolyglotException
innr public final StackFrame
meth public boolean equals(java.lang.Object)
meth public boolean isCancelled()
meth public boolean isExit()
meth public boolean isGuestException()
meth public boolean isHostException()
meth public boolean isIncompleteSource()
meth public boolean isInternalError()
meth public boolean isInterrupted()
meth public boolean isResourceExhausted()
meth public boolean isSyntaxError()
meth public int getExitStatus()
meth public int hashCode()
meth public java.lang.Iterable<org.graalvm.polyglot.PolyglotException$StackFrame> getPolyglotStackTrace()
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getMessage()
meth public java.lang.Throwable asHostException()
meth public java.lang.Throwable fillInStackTrace()
meth public org.graalvm.polyglot.SourceSection getSourceLocation()
meth public org.graalvm.polyglot.Value getGuestObject()
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.RuntimeException
hfds dispatch,impl

CLSS public final org.graalvm.polyglot.PolyglotException$StackFrame
 outer org.graalvm.polyglot.PolyglotException
meth public boolean isGuestFrame()
meth public boolean isHostFrame()
meth public java.lang.StackTraceElement toHostFrame()
meth public java.lang.String getRootName()
meth public java.lang.String toString()
meth public org.graalvm.polyglot.Language getLanguage()
meth public org.graalvm.polyglot.SourceSection getSourceLocation()
supr java.lang.Object
hfds impl

CLSS public final org.graalvm.polyglot.ResourceLimitEvent
meth public java.lang.String toString()
meth public org.graalvm.polyglot.Context getContext()
supr java.lang.Object
hfds context

CLSS public final org.graalvm.polyglot.ResourceLimits
innr public final Builder
meth public static org.graalvm.polyglot.ResourceLimits$Builder newBuilder()
supr java.lang.Object
hfds EMPTY,receiver

CLSS public final org.graalvm.polyglot.ResourceLimits$Builder
 outer org.graalvm.polyglot.ResourceLimits
meth public org.graalvm.polyglot.ResourceLimits build()
meth public org.graalvm.polyglot.ResourceLimits$Builder onLimit(java.util.function.Consumer<org.graalvm.polyglot.ResourceLimitEvent>)
meth public org.graalvm.polyglot.ResourceLimits$Builder statementLimit(long,java.util.function.Predicate<org.graalvm.polyglot.Source>)
supr java.lang.Object
hfds onLimit,statementLimit,statementLimitSourceFilter

CLSS public final !enum org.graalvm.polyglot.SandboxPolicy
fld public final static org.graalvm.polyglot.SandboxPolicy CONSTRAINED
fld public final static org.graalvm.polyglot.SandboxPolicy ISOLATED
fld public final static org.graalvm.polyglot.SandboxPolicy TRUSTED
fld public final static org.graalvm.polyglot.SandboxPolicy UNTRUSTED
meth public boolean isStricterOrEqual(org.graalvm.polyglot.SandboxPolicy)
meth public boolean isStricterThan(org.graalvm.polyglot.SandboxPolicy)
meth public static org.graalvm.polyglot.SandboxPolicy valueOf(java.lang.String)
meth public static org.graalvm.polyglot.SandboxPolicy[] values()
supr java.lang.Enum<org.graalvm.polyglot.SandboxPolicy>

CLSS public final org.graalvm.polyglot.Source
innr public Builder
meth public boolean equals(java.lang.Object)
meth public boolean hasBytes()
meth public boolean hasCharacters()
meth public boolean isInteractive()
meth public boolean isInternal()
meth public int getColumnNumber(int)
meth public int getLength()
meth public int getLineCount()
meth public int getLineLength(int)
meth public int getLineNumber(int)
meth public int getLineStartOffset(int)
meth public int hashCode()
meth public java.io.InputStream getInputStream()
 anno 0 java.lang.Deprecated(null since="19.0")
meth public java.io.Reader getReader()
meth public java.lang.CharSequence getCharacters()
meth public java.lang.CharSequence getCharacters(int)
meth public java.lang.String getLanguage()
meth public java.lang.String getMimeType()
meth public java.lang.String getName()
meth public java.lang.String getPath()
meth public java.lang.String toString()
meth public java.net.URI getURI()
meth public java.net.URL getURL()
meth public org.graalvm.polyglot.io.ByteSequence getBytes()
meth public static java.lang.String findLanguage(java.io.File) throws java.io.IOException
meth public static java.lang.String findLanguage(java.lang.String)
meth public static java.lang.String findLanguage(java.net.URL) throws java.io.IOException
meth public static java.lang.String findMimeType(java.io.File) throws java.io.IOException
meth public static java.lang.String findMimeType(java.net.URL) throws java.io.IOException
meth public static org.graalvm.polyglot.Source create(java.lang.String,java.lang.CharSequence)
meth public static org.graalvm.polyglot.Source$Builder newBuilder(java.lang.String,java.io.File)
meth public static org.graalvm.polyglot.Source$Builder newBuilder(java.lang.String,java.io.Reader,java.lang.String)
meth public static org.graalvm.polyglot.Source$Builder newBuilder(java.lang.String,java.lang.CharSequence,java.lang.String)
meth public static org.graalvm.polyglot.Source$Builder newBuilder(java.lang.String,java.net.URL)
meth public static org.graalvm.polyglot.Source$Builder newBuilder(java.lang.String,org.graalvm.polyglot.io.ByteSequence,java.lang.String)
supr java.lang.Object
hfds EMPTY,IMPL,dispatch,receiver

CLSS public org.graalvm.polyglot.Source$Builder
 outer org.graalvm.polyglot.Source
meth public org.graalvm.polyglot.Source build() throws java.io.IOException
meth public org.graalvm.polyglot.Source buildLiteral()
meth public org.graalvm.polyglot.Source$Builder cached(boolean)
meth public org.graalvm.polyglot.Source$Builder content(java.lang.CharSequence)
meth public org.graalvm.polyglot.Source$Builder content(java.lang.String)
meth public org.graalvm.polyglot.Source$Builder content(org.graalvm.polyglot.io.ByteSequence)
meth public org.graalvm.polyglot.Source$Builder encoding(java.nio.charset.Charset)
meth public org.graalvm.polyglot.Source$Builder interactive(boolean)
meth public org.graalvm.polyglot.Source$Builder internal(boolean)
meth public org.graalvm.polyglot.Source$Builder mimeType(java.lang.String)
meth public org.graalvm.polyglot.Source$Builder name(java.lang.String)
meth public org.graalvm.polyglot.Source$Builder uri(java.net.URI)
supr java.lang.Object
hfds cached,content,fileEncoding,interactive,internal,language,mimeType,name,origin,uri

CLSS public final org.graalvm.polyglot.SourceSection
meth public boolean equals(java.lang.Object)
meth public boolean hasCharIndex()
meth public boolean hasColumns()
meth public boolean hasLines()
meth public boolean isAvailable()
meth public int getCharEndIndex()
meth public int getCharIndex()
meth public int getCharLength()
meth public int getEndColumn()
meth public int getEndLine()
meth public int getStartColumn()
meth public int getStartLine()
meth public int hashCode()
meth public java.lang.CharSequence getCharacters()
meth public java.lang.CharSequence getCode()
 anno 0 java.lang.Deprecated(null since="19.0")
meth public java.lang.String toString()
meth public org.graalvm.polyglot.Source getSource()
supr java.lang.Object
hfds dispatch,receiver,source

CLSS public abstract org.graalvm.polyglot.TypeLiteral<%0 extends java.lang.Object>
cons protected init()
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final java.lang.Class<{org.graalvm.polyglot.TypeLiteral%0}> getRawType()
meth public final java.lang.String toString()
meth public final java.lang.reflect.Type getType()
supr java.lang.Object
hfds rawType,type

CLSS public final org.graalvm.polyglot.Value
meth public !varargs org.graalvm.polyglot.Value execute(java.lang.Object[])
meth public !varargs org.graalvm.polyglot.Value invokeMember(java.lang.String,java.lang.Object[])
meth public !varargs org.graalvm.polyglot.Value newInstance(java.lang.Object[])
meth public !varargs void executeVoid(java.lang.Object[])
meth public <%0 extends java.lang.Object> {%%0} as(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} as(org.graalvm.polyglot.TypeLiteral<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} asHostObject()
meth public <%0 extends org.graalvm.polyglot.proxy.Proxy> {%%0} asProxyObject()
meth public boolean asBoolean()
meth public boolean canExecute()
meth public boolean canInstantiate()
meth public boolean canInvokeMember(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public boolean fitsInBigInteger()
meth public boolean fitsInByte()
meth public boolean fitsInDouble()
meth public boolean fitsInFloat()
meth public boolean fitsInInt()
meth public boolean fitsInLong()
meth public boolean fitsInShort()
meth public boolean hasArrayElements()
meth public boolean hasBufferElements()
meth public boolean hasHashEntries()
meth public boolean hasHashEntry(java.lang.Object)
meth public boolean hasIterator()
meth public boolean hasIteratorNextElement()
meth public boolean hasMember(java.lang.String)
meth public boolean hasMembers()
meth public boolean hasMetaParents()
meth public boolean isBoolean()
meth public boolean isBufferWritable()
meth public boolean isDate()
meth public boolean isDuration()
meth public boolean isException()
meth public boolean isHostObject()
meth public boolean isInstant()
meth public boolean isIterator()
meth public boolean isMetaInstance(java.lang.Object)
meth public boolean isMetaObject()
meth public boolean isNativePointer()
meth public boolean isNull()
meth public boolean isNumber()
meth public boolean isProxyObject()
meth public boolean isString()
meth public boolean isTime()
meth public boolean isTimeZone()
meth public boolean removeArrayElement(long)
meth public boolean removeHashEntry(java.lang.Object)
meth public boolean removeMember(java.lang.String)
meth public byte asByte()
meth public byte readBufferByte(long)
meth public double asDouble()
meth public double readBufferDouble(java.nio.ByteOrder,long)
meth public float asFloat()
meth public float readBufferFloat(java.nio.ByteOrder,long)
meth public int asInt()
meth public int hashCode()
meth public int readBufferInt(java.nio.ByteOrder,long)
meth public java.lang.RuntimeException throwException()
meth public java.lang.String asString()
meth public java.lang.String getMetaQualifiedName()
meth public java.lang.String getMetaSimpleName()
meth public java.lang.String toString()
meth public java.math.BigInteger asBigInteger()
meth public java.time.Duration asDuration()
meth public java.time.Instant asInstant()
meth public java.time.LocalDate asDate()
meth public java.time.LocalTime asTime()
meth public java.time.ZoneId asTimeZone()
meth public java.util.Set<java.lang.String> getMemberKeys()
meth public long asLong()
meth public long asNativePointer()
meth public long getArraySize()
meth public long getBufferSize()
meth public long getHashSize()
meth public long readBufferLong(java.nio.ByteOrder,long)
meth public org.graalvm.polyglot.Context getContext()
meth public org.graalvm.polyglot.SourceSection getSourceLocation()
meth public org.graalvm.polyglot.Value getArrayElement(long)
meth public org.graalvm.polyglot.Value getHashEntriesIterator()
meth public org.graalvm.polyglot.Value getHashKeysIterator()
meth public org.graalvm.polyglot.Value getHashValue(java.lang.Object)
meth public org.graalvm.polyglot.Value getHashValueOrDefault(java.lang.Object,java.lang.Object)
meth public org.graalvm.polyglot.Value getHashValuesIterator()
meth public org.graalvm.polyglot.Value getIterator()
meth public org.graalvm.polyglot.Value getIteratorNextElement()
meth public org.graalvm.polyglot.Value getMember(java.lang.String)
meth public org.graalvm.polyglot.Value getMetaObject()
meth public org.graalvm.polyglot.Value getMetaParents()
meth public short asShort()
meth public short readBufferShort(java.nio.ByteOrder,long)
meth public static org.graalvm.polyglot.Value asValue(java.lang.Object)
meth public void pin()
meth public void putHashEntry(java.lang.Object,java.lang.Object)
meth public void putMember(java.lang.String,java.lang.Object)
meth public void setArrayElement(long,java.lang.Object)
meth public void writeBufferByte(long,byte)
meth public void writeBufferDouble(java.nio.ByteOrder,long,double)
meth public void writeBufferFloat(java.nio.ByteOrder,long,float)
meth public void writeBufferInt(java.nio.ByteOrder,long,int)
meth public void writeBufferLong(java.nio.ByteOrder,long,long)
meth public void writeBufferShort(java.nio.ByteOrder,long,short)
supr java.lang.Object

CLSS public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init()
innr public abstract static APIAccess
innr public abstract static AbstractContextDispatch
innr public abstract static AbstractDispatchClass
innr public abstract static AbstractEngineDispatch
innr public abstract static AbstractExceptionDispatch
innr public abstract static AbstractExecutionEventDispatch
innr public abstract static AbstractExecutionListenerDispatch
innr public abstract static AbstractHostAccess
innr public abstract static AbstractHostLanguageService
innr public abstract static AbstractInstrumentDispatch
innr public abstract static AbstractLanguageDispatch
innr public abstract static AbstractPolyglotHostService
innr public abstract static AbstractSourceDispatch
innr public abstract static AbstractSourceSectionDispatch
innr public abstract static AbstractStackFrameImpl
innr public abstract static AbstractValueDispatch
innr public abstract static IOAccessor
innr public abstract static LogHandler
innr public abstract static ManagementAccess
innr public abstract static ThreadScope
meth protected final org.graalvm.options.OptionDescriptors createAllEngineOptionDescriptors()
meth protected org.graalvm.options.OptionDescriptors createEngineOptionDescriptors()
meth protected void initialize()
meth public !varargs org.graalvm.options.OptionDescriptors createUnionOptionDescriptors(org.graalvm.options.OptionDescriptors[])
meth public <%0 extends java.lang.Object, %1 extends java.lang.Object> java.lang.Object newTargetTypeMapping(java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,java.util.function.Predicate<{%%0}>,java.util.function.Function<{%%0},{%%1}>,org.graalvm.polyglot.HostAccess$TargetMappingPrecedence)
meth public abstract int getPriority()
meth public boolean isDefaultProcessHandler(org.graalvm.polyglot.io.ProcessHandler)
meth public boolean isHostFileSystem(org.graalvm.polyglot.io.FileSystem)
meth public boolean isInCurrentEngineHostCallback(java.lang.Object)
meth public boolean isInternalFileSystem(org.graalvm.polyglot.io.FileSystem)
meth public final org.graalvm.polyglot.impl.AbstractPolyglotImpl getNext()
meth public final org.graalvm.polyglot.impl.AbstractPolyglotImpl getRootImpl()
meth public final org.graalvm.polyglot.impl.AbstractPolyglotImpl$APIAccess getAPIAccess()
meth public final org.graalvm.polyglot.impl.AbstractPolyglotImpl$IOAccessor getIO()
meth public final org.graalvm.polyglot.impl.AbstractPolyglotImpl$ManagementAccess getManagement()
meth public final void setConstructors(org.graalvm.polyglot.impl.AbstractPolyglotImpl$APIAccess)
meth public final void setIO(org.graalvm.polyglot.impl.AbstractPolyglotImpl$IOAccessor)
meth public final void setMonitoring(org.graalvm.polyglot.impl.AbstractPolyglotImpl$ManagementAccess)
meth public final void setNext(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public java.lang.Class<?> loadLanguageClass(java.lang.String)
meth public java.lang.Object buildLimits(long,java.util.function.Predicate<org.graalvm.polyglot.Source>,java.util.function.Consumer<org.graalvm.polyglot.ResourceLimitEvent>)
meth public java.lang.Object createHostLanguage(org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractHostAccess)
meth public java.lang.String findLanguage(java.io.File) throws java.io.IOException
meth public java.lang.String findLanguage(java.lang.String)
meth public java.lang.String findLanguage(java.net.URL) throws java.io.IOException
meth public java.lang.String findMimeType(java.io.File) throws java.io.IOException
meth public java.lang.String findMimeType(java.net.URL) throws java.io.IOException
meth public org.graalvm.polyglot.Context getCurrentContext()
meth public org.graalvm.polyglot.Engine buildEngine(java.lang.String[],org.graalvm.polyglot.SandboxPolicy,java.io.OutputStream,java.io.OutputStream,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>,boolean,boolean,org.graalvm.polyglot.io.MessageTransport,org.graalvm.polyglot.impl.AbstractPolyglotImpl$LogHandler,java.lang.Object,boolean,boolean,org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractPolyglotHostService)
meth public org.graalvm.polyglot.Source build(java.lang.String,java.lang.Object,java.net.URI,java.lang.String,java.lang.String,java.lang.Object,boolean,boolean,boolean,java.nio.charset.Charset,java.net.URL,java.lang.String) throws java.io.IOException
meth public org.graalvm.polyglot.Value asValue(java.lang.Object)
meth public org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractHostAccess createHostAccess()
meth public org.graalvm.polyglot.impl.AbstractPolyglotImpl$LogHandler newLogHandler(java.lang.Object)
meth public org.graalvm.polyglot.impl.AbstractPolyglotImpl$ThreadScope createThreadScope()
meth public org.graalvm.polyglot.io.FileSystem allowLanguageHomeAccess(org.graalvm.polyglot.io.FileSystem)
meth public org.graalvm.polyglot.io.FileSystem newDefaultFileSystem()
meth public org.graalvm.polyglot.io.FileSystem newNIOFileSystem(java.nio.file.FileSystem)
meth public org.graalvm.polyglot.io.FileSystem newReadOnlyFileSystem(org.graalvm.polyglot.io.FileSystem)
meth public org.graalvm.polyglot.io.ProcessHandler newDefaultProcessHandler()
meth public void preInitializeEngine()
meth public void resetPreInitializedEngine()
supr java.lang.Object
hfds api,io,management,next,prev

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$APIAccess
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init()
meth public abstract boolean allowsAccess(org.graalvm.polyglot.HostAccess,java.lang.reflect.AnnotatedElement)
meth public abstract boolean allowsAccessInheritance(org.graalvm.polyglot.HostAccess)
meth public abstract boolean allowsImplementation(org.graalvm.polyglot.HostAccess,java.lang.Class<?>)
meth public abstract boolean allowsPublicAccess(org.graalvm.polyglot.HostAccess)
meth public abstract boolean isArrayAccessible(org.graalvm.polyglot.HostAccess)
meth public abstract boolean isBigIntegerAccessibleAsNumber(org.graalvm.polyglot.HostAccess)
meth public abstract boolean isBufferAccessible(org.graalvm.polyglot.HostAccess)
meth public abstract boolean isIterableAccessible(org.graalvm.polyglot.HostAccess)
meth public abstract boolean isIteratorAccessible(org.graalvm.polyglot.HostAccess)
meth public abstract boolean isListAccessible(org.graalvm.polyglot.HostAccess)
meth public abstract boolean isMapAccessible(org.graalvm.polyglot.HostAccess)
meth public abstract boolean isMethodScoped(org.graalvm.polyglot.HostAccess,java.lang.reflect.Executable)
meth public abstract boolean isMethodScopingEnabled(org.graalvm.polyglot.HostAccess)
meth public abstract java.lang.Object getContext(org.graalvm.polyglot.Value)
meth public abstract java.lang.Object getHostAccessImpl(org.graalvm.polyglot.HostAccess)
meth public abstract java.lang.Object getReceiver(org.graalvm.polyglot.Context)
meth public abstract java.lang.Object getReceiver(org.graalvm.polyglot.Engine)
meth public abstract java.lang.Object getReceiver(org.graalvm.polyglot.Instrument)
meth public abstract java.lang.Object getReceiver(org.graalvm.polyglot.Language)
meth public abstract java.lang.Object getReceiver(org.graalvm.polyglot.PolyglotException)
meth public abstract java.lang.Object getReceiver(org.graalvm.polyglot.ResourceLimits)
meth public abstract java.lang.Object getReceiver(org.graalvm.polyglot.Source)
meth public abstract java.lang.Object getReceiver(org.graalvm.polyglot.SourceSection)
meth public abstract java.lang.Object getReceiver(org.graalvm.polyglot.Value)
meth public abstract java.lang.String validatePolyglotAccess(org.graalvm.polyglot.PolyglotAccess,java.util.Set<java.lang.String>)
meth public abstract java.util.List<java.lang.Object> getTargetMappings(org.graalvm.polyglot.HostAccess)
meth public abstract java.util.Map<java.lang.String,java.lang.String> readOptionsFromSystemProperties()
meth public abstract org.graalvm.collections.UnmodifiableEconomicMap<java.lang.String,org.graalvm.collections.UnmodifiableEconomicSet<java.lang.String>> getEvalAccess(org.graalvm.polyglot.PolyglotAccess)
meth public abstract org.graalvm.collections.UnmodifiableEconomicSet<java.lang.String> getBindingsAccess(org.graalvm.polyglot.PolyglotAccess)
meth public abstract org.graalvm.collections.UnmodifiableEconomicSet<java.lang.String> getEvalAccess(org.graalvm.polyglot.PolyglotAccess,java.lang.String)
meth public abstract org.graalvm.polyglot.Context newContext(org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractContextDispatch,java.lang.Object,org.graalvm.polyglot.Engine)
meth public abstract org.graalvm.polyglot.Engine newEngine(org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractEngineDispatch,java.lang.Object,boolean)
meth public abstract org.graalvm.polyglot.HostAccess$MutableTargetMapping[] getMutableTargetMappings(org.graalvm.polyglot.HostAccess)
meth public abstract org.graalvm.polyglot.Instrument newInstrument(org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractInstrumentDispatch,java.lang.Object)
meth public abstract org.graalvm.polyglot.Language newLanguage(org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractLanguageDispatch,java.lang.Object)
meth public abstract org.graalvm.polyglot.PolyglotException newLanguageException(java.lang.String,org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractExceptionDispatch,java.lang.Object)
meth public abstract org.graalvm.polyglot.PolyglotException$StackFrame newPolyglotStackTraceElement(org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractStackFrameImpl,java.lang.Object)
meth public abstract org.graalvm.polyglot.ResourceLimitEvent newResourceLimitsEvent(org.graalvm.polyglot.Context)
meth public abstract org.graalvm.polyglot.Source newSource(org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractSourceDispatch,java.lang.Object)
meth public abstract org.graalvm.polyglot.SourceSection newSourceSection(org.graalvm.polyglot.Source,org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractSourceSectionDispatch,java.lang.Object)
meth public abstract org.graalvm.polyglot.Value newValue(org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractValueDispatch,java.lang.Object,java.lang.Object)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractContextDispatch getDispatch(org.graalvm.polyglot.Context)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractEngineDispatch getDispatch(org.graalvm.polyglot.Engine)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractInstrumentDispatch getDispatch(org.graalvm.polyglot.Instrument)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractLanguageDispatch getDispatch(org.graalvm.polyglot.Language)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractSourceDispatch getDispatch(org.graalvm.polyglot.Source)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractSourceSectionDispatch getDispatch(org.graalvm.polyglot.SourceSection)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractStackFrameImpl getDispatch(org.graalvm.polyglot.PolyglotException$StackFrame)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractValueDispatch getDispatch(org.graalvm.polyglot.Value)
meth public abstract void engineClosed(org.graalvm.polyglot.Engine)
meth public abstract void setHostAccessImpl(org.graalvm.polyglot.HostAccess,java.lang.Object)
supr java.lang.Object

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractContextDispatch
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract boolean initializeLanguage(java.lang.Object,java.lang.String)
meth public abstract boolean interrupt(java.lang.Object,java.time.Duration)
meth public abstract org.graalvm.polyglot.Value asValue(java.lang.Object,java.lang.Object)
meth public abstract org.graalvm.polyglot.Value eval(java.lang.Object,java.lang.String,org.graalvm.polyglot.Source)
meth public abstract org.graalvm.polyglot.Value getBindings(java.lang.Object,java.lang.String)
meth public abstract org.graalvm.polyglot.Value getPolyglotBindings(java.lang.Object)
meth public abstract org.graalvm.polyglot.Value parse(java.lang.Object,java.lang.String,org.graalvm.polyglot.Source)
meth public abstract void close(java.lang.Object,boolean)
meth public abstract void explicitEnter(java.lang.Object)
meth public abstract void explicitLeave(java.lang.Object)
meth public abstract void resetLimits(java.lang.Object)
meth public abstract void safepoint(java.lang.Object)
meth public abstract void setAPI(java.lang.Object,org.graalvm.polyglot.Context)
supr org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractDispatchClass

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractDispatchClass
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons public init()
supr java.lang.Object

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractEngineDispatch
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract java.lang.RuntimeException hostToGuestException(java.lang.Object,java.lang.Throwable)
meth public abstract java.lang.String getImplementationName(java.lang.Object)
meth public abstract java.lang.String getVersion(java.lang.Object)
meth public abstract java.util.Map<java.lang.String,org.graalvm.polyglot.Instrument> getInstruments(java.lang.Object)
meth public abstract java.util.Map<java.lang.String,org.graalvm.polyglot.Language> getLanguages(java.lang.Object)
meth public abstract java.util.Set<org.graalvm.polyglot.Source> getCachedSources(java.lang.Object)
meth public abstract org.graalvm.options.OptionDescriptors getOptions(java.lang.Object)
meth public abstract org.graalvm.polyglot.Context createContext(java.lang.Object,org.graalvm.polyglot.SandboxPolicy,java.io.OutputStream,java.io.OutputStream,java.io.InputStream,boolean,org.graalvm.polyglot.HostAccess,org.graalvm.polyglot.PolyglotAccess,boolean,boolean,boolean,boolean,boolean,java.util.function.Predicate<java.lang.String>,java.util.Map<java.lang.String,java.lang.String>,java.util.Map<java.lang.String,java.lang.String[]>,java.lang.String[],org.graalvm.polyglot.io.IOAccess,org.graalvm.polyglot.impl.AbstractPolyglotImpl$LogHandler,boolean,org.graalvm.polyglot.io.ProcessHandler,org.graalvm.polyglot.EnvironmentAccess,java.util.Map<java.lang.String,java.lang.String>,java.time.ZoneId,java.lang.Object,java.lang.String,java.lang.ClassLoader,boolean,boolean)
meth public abstract org.graalvm.polyglot.Instrument requirePublicInstrument(java.lang.Object,java.lang.String)
meth public abstract org.graalvm.polyglot.Language requirePublicLanguage(java.lang.Object,java.lang.String)
meth public abstract org.graalvm.polyglot.SandboxPolicy getSandboxPolicy(java.lang.Object)
meth public abstract org.graalvm.polyglot.management.ExecutionListener attachExecutionListener(java.lang.Object,java.util.function.Consumer<org.graalvm.polyglot.management.ExecutionEvent>,java.util.function.Consumer<org.graalvm.polyglot.management.ExecutionEvent>,boolean,boolean,boolean,java.util.function.Predicate<org.graalvm.polyglot.Source>,java.util.function.Predicate<java.lang.String>,boolean,boolean,boolean)
meth public abstract void close(java.lang.Object,java.lang.Object,boolean)
meth public abstract void setAPI(java.lang.Object,org.graalvm.polyglot.Engine)
meth public abstract void shutdown(java.lang.Object)
supr org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractDispatchClass

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractExceptionDispatch
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract boolean isCancelled(java.lang.Object)
meth public abstract boolean isExit(java.lang.Object)
meth public abstract boolean isHostException(java.lang.Object)
meth public abstract boolean isIncompleteSource(java.lang.Object)
meth public abstract boolean isInternalError(java.lang.Object)
meth public abstract boolean isInterrupted(java.lang.Object)
meth public abstract boolean isResourceExhausted(java.lang.Object)
meth public abstract boolean isSyntaxError(java.lang.Object)
meth public abstract int getExitStatus(java.lang.Object)
meth public abstract java.lang.Iterable<org.graalvm.polyglot.PolyglotException$StackFrame> getPolyglotStackTrace(java.lang.Object)
meth public abstract java.lang.StackTraceElement[] getStackTrace(java.lang.Object)
meth public abstract java.lang.String getMessage(java.lang.Object)
meth public abstract java.lang.Throwable asHostException(java.lang.Object)
meth public abstract org.graalvm.polyglot.SourceSection getSourceLocation(java.lang.Object)
meth public abstract org.graalvm.polyglot.Value getGuestObject(java.lang.Object)
meth public abstract void onCreate(java.lang.Object,org.graalvm.polyglot.PolyglotException)
meth public abstract void printStackTrace(java.lang.Object,java.io.PrintStream)
meth public abstract void printStackTrace(java.lang.Object,java.io.PrintWriter)
supr org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractDispatchClass

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractExecutionEventDispatch
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract boolean isExecutionEventExpression(java.lang.Object)
meth public abstract boolean isExecutionEventRoot(java.lang.Object)
meth public abstract boolean isExecutionEventStatement(java.lang.Object)
meth public abstract java.lang.String getExecutionEventRootName(java.lang.Object)
meth public abstract java.util.List<org.graalvm.polyglot.Value> getExecutionEventInputValues(java.lang.Object)
meth public abstract org.graalvm.polyglot.PolyglotException getExecutionEventException(java.lang.Object)
meth public abstract org.graalvm.polyglot.SourceSection getExecutionEventLocation(java.lang.Object)
meth public abstract org.graalvm.polyglot.Value getExecutionEventReturnValue(java.lang.Object)
supr org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractDispatchClass

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractExecutionListenerDispatch
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract void closeExecutionListener(java.lang.Object)
supr org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractDispatchClass

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractHostAccess
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map$Entry<{%%0},{%%1}> toMapEntry(java.lang.Object,java.lang.Object,boolean,java.lang.Class<{%%0}>,java.lang.reflect.Type,java.lang.Class<{%%1}>,java.lang.reflect.Type)
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> toMap(java.lang.Object,java.lang.Object,boolean,java.lang.Class<{%%0}>,java.lang.reflect.Type,java.lang.Class<{%%1}>,java.lang.reflect.Type)
meth public abstract <%0 extends java.lang.Object> java.lang.Iterable<{%%0}> toIterable(java.lang.Object,java.lang.Object,boolean,java.lang.Class<{%%0}>,java.lang.reflect.Type)
meth public abstract <%0 extends java.lang.Object> java.util.Iterator<{%%0}> toIterator(java.lang.Object,java.lang.Object,boolean,java.lang.Class<{%%0}>,java.lang.reflect.Type)
meth public abstract <%0 extends java.lang.Object> java.util.List<{%%0}> toList(java.lang.Object,java.lang.Object,boolean,java.lang.Class<{%%0}>,java.lang.reflect.Type)
meth public abstract <%0 extends java.lang.Object> java.util.function.Function<?,?> toFunction(java.lang.Object,java.lang.Object,java.lang.Class<?>,java.lang.reflect.Type,java.lang.Class<?>,java.lang.reflect.Type)
meth public abstract <%0 extends java.lang.Object> {%%0} toFunctionProxy(java.lang.Object,java.lang.Class<{%%0}>,java.lang.Object)
meth public abstract boolean isEngineException(java.lang.RuntimeException)
meth public abstract java.lang.Object toGuestValue(java.lang.Object,java.lang.Object)
meth public abstract java.lang.Object toObjectProxy(java.lang.Object,java.lang.Class<?>,java.lang.Object)
meth public abstract java.lang.RuntimeException toEngineException(java.lang.RuntimeException)
meth public abstract java.lang.RuntimeException unboxEngineException(java.lang.RuntimeException)
meth public abstract java.lang.String getValueInfo(java.lang.Object,java.lang.Object)
meth public abstract org.graalvm.polyglot.PolyglotException toPolyglotException(java.lang.Object,java.lang.Throwable)
meth public abstract org.graalvm.polyglot.Value toValue(java.lang.Object,java.lang.Object)
meth public abstract org.graalvm.polyglot.Value[] toValues(java.lang.Object,java.lang.Object[])
meth public abstract org.graalvm.polyglot.Value[] toValues(java.lang.Object,java.lang.Object[],int)
meth public abstract void rethrowPolyglotException(java.lang.Object,org.graalvm.polyglot.PolyglotException)
supr org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractDispatchClass

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractHostLanguageService
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract <%0 extends java.lang.Object> {%%0} toHostType(java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Class<{%%0}>,java.lang.reflect.Type)
meth public abstract boolean allowsPublicAccess()
meth public abstract boolean isHostException(java.lang.Object)
meth public abstract boolean isHostFunction(java.lang.Object)
meth public abstract boolean isHostObject(java.lang.Object)
meth public abstract boolean isHostProxy(java.lang.Object)
meth public abstract boolean isHostSymbol(java.lang.Object)
meth public abstract boolean isHostValue(java.lang.Object)
meth public abstract int findNextGuestToHostStackTraceElement(java.lang.StackTraceElement,java.lang.StackTraceElement[],int)
meth public abstract java.lang.Error toHostResourceError(java.lang.Throwable)
meth public abstract java.lang.Object asHostDynamicClass(java.lang.Object,java.lang.Class<?>)
meth public abstract java.lang.Object asHostStaticClass(java.lang.Object,java.lang.Class<?>)
meth public abstract java.lang.Object createHostAdapter(java.lang.Object,java.lang.Object[],java.lang.Object)
meth public abstract java.lang.Object findDynamicClass(java.lang.Object,java.lang.String)
meth public abstract java.lang.Object findStaticClass(java.lang.Object,java.lang.String)
meth public abstract java.lang.Object migrateValue(java.lang.Object,java.lang.Object,java.lang.Object)
meth public abstract java.lang.Object toGuestValue(java.lang.Object,java.lang.Object,boolean)
meth public abstract java.lang.Object toHostObject(java.lang.Object,java.lang.Object)
meth public abstract java.lang.Object unboxHostObject(java.lang.Object)
meth public abstract java.lang.Object unboxProxyObject(java.lang.Object)
meth public abstract java.lang.RuntimeException toHostException(java.lang.Object,java.lang.Throwable)
meth public abstract java.lang.Throwable unboxHostException(java.lang.Throwable)
meth public abstract void addToHostClassPath(java.lang.Object,java.lang.Object)
meth public abstract void hostExit(int)
meth public abstract void initializeHostContext(java.lang.Object,java.lang.Object,org.graalvm.polyglot.HostAccess,java.lang.ClassLoader,java.util.function.Predicate<java.lang.String>,boolean,boolean)
meth public abstract void pin(java.lang.Object)
meth public abstract void release()
meth public abstract void throwHostLanguageException(java.lang.String)
meth public final boolean isHostStackTraceVisibleToGuest()
supr org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractDispatchClass

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractInstrumentDispatch
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Object,java.lang.Class<{%%0}>)
meth public abstract java.lang.String getId(java.lang.Object)
meth public abstract java.lang.String getName(java.lang.Object)
meth public abstract java.lang.String getVersion(java.lang.Object)
meth public abstract java.lang.String getWebsite(java.lang.Object)
meth public abstract org.graalvm.options.OptionDescriptors getOptions(java.lang.Object)
supr org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractDispatchClass

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractLanguageDispatch
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract boolean isInteractive(java.lang.Object)
meth public abstract java.lang.String getDefaultMimeType(java.lang.Object)
meth public abstract java.lang.String getId(java.lang.Object)
meth public abstract java.lang.String getImplementationName(java.lang.Object)
meth public abstract java.lang.String getName(java.lang.Object)
meth public abstract java.lang.String getVersion(java.lang.Object)
meth public abstract java.lang.String getWebsite(java.lang.Object)
meth public abstract java.util.Set<java.lang.String> getMimeTypes(java.lang.Object)
meth public abstract org.graalvm.options.OptionDescriptors getOptions(java.lang.Object)
supr org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractDispatchClass

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractPolyglotHostService
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract java.lang.RuntimeException hostToGuestException(org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractHostLanguageService,java.lang.Throwable)
meth public abstract void notifyClearExplicitContextStack(java.lang.Object)
meth public abstract void notifyContextCancellingOrExiting(java.lang.Object,boolean,int,boolean,java.lang.String)
meth public abstract void notifyContextClosed(java.lang.Object,boolean,boolean,java.lang.String)
meth public abstract void notifyEngineClosed(java.lang.Object,boolean)
supr org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractDispatchClass

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractSourceDispatch
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract boolean equals(java.lang.Object,java.lang.Object)
meth public abstract boolean hasBytes(java.lang.Object)
meth public abstract boolean hasCharacters(java.lang.Object)
meth public abstract boolean isCached(java.lang.Object)
meth public abstract boolean isInteractive(java.lang.Object)
meth public abstract boolean isInternal(java.lang.Object)
meth public abstract int getColumnNumber(java.lang.Object,int)
meth public abstract int getLength(java.lang.Object)
meth public abstract int getLineCount(java.lang.Object)
meth public abstract int getLineLength(java.lang.Object,int)
meth public abstract int getLineNumber(java.lang.Object,int)
meth public abstract int getLineStartOffset(java.lang.Object,int)
meth public abstract int hashCode(java.lang.Object)
meth public abstract java.io.InputStream getInputStream(java.lang.Object)
meth public abstract java.io.Reader getReader(java.lang.Object)
meth public abstract java.lang.CharSequence getCharacters(java.lang.Object)
meth public abstract java.lang.CharSequence getCharacters(java.lang.Object,int)
meth public abstract java.lang.String getLanguage(java.lang.Object)
meth public abstract java.lang.String getMimeType(java.lang.Object)
meth public abstract java.lang.String getName(java.lang.Object)
meth public abstract java.lang.String getPath(java.lang.Object)
meth public abstract java.lang.String toString(java.lang.Object)
meth public abstract java.net.URI getURI(java.lang.Object)
meth public abstract java.net.URL getURL(java.lang.Object)
meth public abstract org.graalvm.polyglot.io.ByteSequence getBytes(java.lang.Object)
supr org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractDispatchClass

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractSourceSectionDispatch
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract boolean equals(java.lang.Object,java.lang.Object)
meth public abstract boolean hasCharIndex(java.lang.Object)
meth public abstract boolean hasColumns(java.lang.Object)
meth public abstract boolean hasLines(java.lang.Object)
meth public abstract boolean isAvailable(java.lang.Object)
meth public abstract int getCharEndIndex(java.lang.Object)
meth public abstract int getCharIndex(java.lang.Object)
meth public abstract int getCharLength(java.lang.Object)
meth public abstract int getEndColumn(java.lang.Object)
meth public abstract int getEndLine(java.lang.Object)
meth public abstract int getStartColumn(java.lang.Object)
meth public abstract int getStartLine(java.lang.Object)
meth public abstract int hashCode(java.lang.Object)
meth public abstract java.lang.CharSequence getCode(java.lang.Object)
meth public abstract java.lang.String toString(java.lang.Object)
supr org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractDispatchClass

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractStackFrameImpl
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract boolean isHostFrame()
meth public abstract java.lang.StackTraceElement toHostFrame()
meth public abstract java.lang.String getRootName()
meth public abstract java.lang.String toStringImpl(int)
meth public abstract org.graalvm.polyglot.Language getLanguage()
meth public abstract org.graalvm.polyglot.SourceSection getSourceLocation()
supr org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractDispatchClass

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractValueDispatch
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract <%0 extends java.lang.Object> {%%0} as(java.lang.Object,java.lang.Object,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} as(java.lang.Object,java.lang.Object,org.graalvm.polyglot.TypeLiteral<{%%0}>)
meth public abstract boolean asBoolean(java.lang.Object,java.lang.Object)
meth public abstract boolean equalsImpl(java.lang.Object,java.lang.Object,java.lang.Object)
meth public abstract boolean hasIteratorNextElement(java.lang.Object,java.lang.Object)
meth public abstract boolean hasMetaParents(java.lang.Object,java.lang.Object)
meth public abstract boolean isBufferWritable(java.lang.Object,java.lang.Object)
meth public abstract boolean isMetaInstance(java.lang.Object,java.lang.Object,java.lang.Object)
meth public abstract boolean removeArrayElement(java.lang.Object,java.lang.Object,long)
meth public abstract boolean removeHashEntry(java.lang.Object,java.lang.Object,java.lang.Object)
meth public abstract boolean removeMember(java.lang.Object,java.lang.Object,java.lang.String)
meth public abstract byte asByte(java.lang.Object,java.lang.Object)
meth public abstract byte readBufferByte(java.lang.Object,java.lang.Object,long)
meth public abstract double asDouble(java.lang.Object,java.lang.Object)
meth public abstract double readBufferDouble(java.lang.Object,java.lang.Object,java.nio.ByteOrder,long)
meth public abstract float asFloat(java.lang.Object,java.lang.Object)
meth public abstract float readBufferFloat(java.lang.Object,java.lang.Object,java.nio.ByteOrder,long)
meth public abstract int asInt(java.lang.Object,java.lang.Object)
meth public abstract int hashCodeImpl(java.lang.Object,java.lang.Object)
meth public abstract int readBufferInt(java.lang.Object,java.lang.Object,java.nio.ByteOrder,long)
meth public abstract java.lang.Object asHostObject(java.lang.Object,java.lang.Object)
meth public abstract java.lang.Object asProxyObject(java.lang.Object,java.lang.Object)
meth public abstract java.lang.RuntimeException throwException(java.lang.Object,java.lang.Object)
meth public abstract java.lang.String asString(java.lang.Object,java.lang.Object)
meth public abstract java.lang.String getMetaQualifiedName(java.lang.Object,java.lang.Object)
meth public abstract java.lang.String getMetaSimpleName(java.lang.Object,java.lang.Object)
meth public abstract java.lang.String toString(java.lang.Object,java.lang.Object)
meth public abstract java.math.BigInteger asBigInteger(java.lang.Object,java.lang.Object)
meth public abstract java.time.Duration asDuration(java.lang.Object,java.lang.Object)
meth public abstract java.time.Instant asInstant(java.lang.Object,java.lang.Object)
meth public abstract java.time.LocalDate asDate(java.lang.Object,java.lang.Object)
meth public abstract java.time.LocalTime asTime(java.lang.Object,java.lang.Object)
meth public abstract java.time.ZoneId asTimeZone(java.lang.Object,java.lang.Object)
meth public abstract long asLong(java.lang.Object,java.lang.Object)
meth public abstract long asNativePointer(java.lang.Object,java.lang.Object)
meth public abstract long getArraySize(java.lang.Object,java.lang.Object)
meth public abstract long getBufferSize(java.lang.Object,java.lang.Object)
meth public abstract long getHashSize(java.lang.Object,java.lang.Object)
meth public abstract long readBufferLong(java.lang.Object,java.lang.Object,java.nio.ByteOrder,long)
meth public abstract org.graalvm.polyglot.SourceSection getSourceLocation(java.lang.Object,java.lang.Object)
meth public abstract org.graalvm.polyglot.Value execute(java.lang.Object,java.lang.Object)
meth public abstract org.graalvm.polyglot.Value execute(java.lang.Object,java.lang.Object,java.lang.Object[])
meth public abstract org.graalvm.polyglot.Value getArrayElement(java.lang.Object,java.lang.Object,long)
meth public abstract org.graalvm.polyglot.Value getHashEntriesIterator(java.lang.Object,java.lang.Object)
meth public abstract org.graalvm.polyglot.Value getHashKeysIterator(java.lang.Object,java.lang.Object)
meth public abstract org.graalvm.polyglot.Value getHashValue(java.lang.Object,java.lang.Object,java.lang.Object)
meth public abstract org.graalvm.polyglot.Value getHashValueOrDefault(java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object)
meth public abstract org.graalvm.polyglot.Value getHashValuesIterator(java.lang.Object,java.lang.Object)
meth public abstract org.graalvm.polyglot.Value getIterator(java.lang.Object,java.lang.Object)
meth public abstract org.graalvm.polyglot.Value getIteratorNextElement(java.lang.Object,java.lang.Object)
meth public abstract org.graalvm.polyglot.Value getMember(java.lang.Object,java.lang.Object,java.lang.String)
meth public abstract org.graalvm.polyglot.Value getMetaObject(java.lang.Object,java.lang.Object)
meth public abstract org.graalvm.polyglot.Value getMetaParents(java.lang.Object,java.lang.Object)
meth public abstract org.graalvm.polyglot.Value invoke(java.lang.Object,java.lang.Object,java.lang.String)
meth public abstract org.graalvm.polyglot.Value invoke(java.lang.Object,java.lang.Object,java.lang.String,java.lang.Object[])
meth public abstract org.graalvm.polyglot.Value newInstance(java.lang.Object,java.lang.Object,java.lang.Object[])
meth public abstract short asShort(java.lang.Object,java.lang.Object)
meth public abstract short readBufferShort(java.lang.Object,java.lang.Object,java.nio.ByteOrder,long)
meth public abstract void executeVoid(java.lang.Object,java.lang.Object)
meth public abstract void executeVoid(java.lang.Object,java.lang.Object,java.lang.Object[])
meth public abstract void pin(java.lang.Object,java.lang.Object)
meth public abstract void putHashEntry(java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object)
meth public abstract void putMember(java.lang.Object,java.lang.Object,java.lang.String,java.lang.Object)
meth public abstract void setArrayElement(java.lang.Object,java.lang.Object,long,java.lang.Object)
meth public abstract void writeBufferByte(java.lang.Object,java.lang.Object,long,byte)
meth public abstract void writeBufferDouble(java.lang.Object,java.lang.Object,java.nio.ByteOrder,long,double)
meth public abstract void writeBufferFloat(java.lang.Object,java.lang.Object,java.nio.ByteOrder,long,float)
meth public abstract void writeBufferInt(java.lang.Object,java.lang.Object,java.nio.ByteOrder,long,int)
meth public abstract void writeBufferLong(java.lang.Object,java.lang.Object,java.nio.ByteOrder,long,long)
meth public abstract void writeBufferShort(java.lang.Object,java.lang.Object,java.nio.ByteOrder,long,short)
meth public boolean canExecute(java.lang.Object,java.lang.Object)
meth public boolean canInstantiate(java.lang.Object,java.lang.Object)
meth public boolean canInvoke(java.lang.Object,java.lang.String,java.lang.Object)
meth public boolean fitsInBigInteger(java.lang.Object,java.lang.Object)
meth public boolean fitsInByte(java.lang.Object,java.lang.Object)
meth public boolean fitsInDouble(java.lang.Object,java.lang.Object)
meth public boolean fitsInFloat(java.lang.Object,java.lang.Object)
meth public boolean fitsInInt(java.lang.Object,java.lang.Object)
meth public boolean fitsInLong(java.lang.Object,java.lang.Object)
meth public boolean fitsInShort(java.lang.Object,java.lang.Object)
meth public boolean hasArrayElements(java.lang.Object,java.lang.Object)
meth public boolean hasBufferElements(java.lang.Object,java.lang.Object)
meth public boolean hasHashEntries(java.lang.Object,java.lang.Object)
meth public boolean hasHashEntry(java.lang.Object,java.lang.Object,java.lang.Object)
meth public boolean hasIterator(java.lang.Object,java.lang.Object)
meth public boolean hasMember(java.lang.Object,java.lang.Object,java.lang.String)
meth public boolean hasMembers(java.lang.Object,java.lang.Object)
meth public boolean isBoolean(java.lang.Object,java.lang.Object)
meth public boolean isDate(java.lang.Object,java.lang.Object)
meth public boolean isDuration(java.lang.Object,java.lang.Object)
meth public boolean isException(java.lang.Object,java.lang.Object)
meth public boolean isHostObject(java.lang.Object,java.lang.Object)
meth public boolean isIterator(java.lang.Object,java.lang.Object)
meth public boolean isMetaObject(java.lang.Object,java.lang.Object)
meth public boolean isNativePointer(java.lang.Object,java.lang.Object)
meth public boolean isNull(java.lang.Object,java.lang.Object)
meth public boolean isNumber(java.lang.Object,java.lang.Object)
meth public boolean isProxyObject(java.lang.Object,java.lang.Object)
meth public boolean isString(java.lang.Object,java.lang.Object)
meth public boolean isTime(java.lang.Object,java.lang.Object)
meth public boolean isTimeZone(java.lang.Object,java.lang.Object)
meth public java.util.Set<java.lang.String> getMemberKeys(java.lang.Object,java.lang.Object)
meth public org.graalvm.polyglot.Context getContext(java.lang.Object)
supr org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractDispatchClass

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$IOAccessor
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init()
meth public abstract boolean hasHostFileAccess(org.graalvm.polyglot.io.IOAccess)
meth public abstract boolean hasHostSocketAccess(org.graalvm.polyglot.io.IOAccess)
meth public abstract java.io.OutputStream getOutputStream(org.graalvm.polyglot.io.ProcessHandler$Redirect)
meth public abstract org.graalvm.polyglot.io.FileSystem getFileSystem(org.graalvm.polyglot.io.IOAccess)
meth public abstract org.graalvm.polyglot.io.ProcessHandler$ProcessCommand newProcessCommand(java.util.List<java.lang.String>,java.lang.String,java.util.Map<java.lang.String,java.lang.String>,boolean,org.graalvm.polyglot.io.ProcessHandler$Redirect,org.graalvm.polyglot.io.ProcessHandler$Redirect,org.graalvm.polyglot.io.ProcessHandler$Redirect)
meth public abstract org.graalvm.polyglot.io.ProcessHandler$Redirect createRedirectToStream(java.io.OutputStream)
supr java.lang.Object

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$LogHandler
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract void close()
meth public abstract void flush()
meth public abstract void publish(java.util.logging.LogRecord)
supr java.lang.Object

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$ManagementAccess
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init()
meth public abstract java.lang.Object getReceiver(org.graalvm.polyglot.management.ExecutionEvent)
meth public abstract java.lang.Object getReceiver(org.graalvm.polyglot.management.ExecutionListener)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractExecutionEventDispatch getDispatch(org.graalvm.polyglot.management.ExecutionEvent)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractExecutionListenerDispatch getDispatch(org.graalvm.polyglot.management.ExecutionListener)
meth public abstract org.graalvm.polyglot.management.ExecutionEvent newExecutionEvent(org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractExecutionEventDispatch,java.lang.Object)
meth public abstract org.graalvm.polyglot.management.ExecutionListener newExecutionListener(org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractExecutionListenerDispatch,java.lang.Object)
supr java.lang.Object

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$ThreadScope
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
intf java.lang.AutoCloseable
meth public abstract void close()
supr java.lang.Object

CLSS public abstract interface org.graalvm.polyglot.io.ByteSequence
meth public abstract byte byteAt(int)
meth public abstract int length()
meth public byte[] toByteArray()
meth public java.util.stream.IntStream bytes()
meth public org.graalvm.polyglot.io.ByteSequence subSequence(int,int)
meth public static org.graalvm.polyglot.io.ByteSequence create(byte[])

CLSS public abstract interface org.graalvm.polyglot.io.FileSystem
meth public !varargs boolean isSameFile(java.nio.file.Path,java.nio.file.Path,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void copy(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption[]) throws java.io.IOException
meth public !varargs void createSymbolicLink(java.nio.file.Path,java.nio.file.Path,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs void move(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption[]) throws java.io.IOException
meth public !varargs void setAttribute(java.nio.file.Path,java.lang.String,java.lang.Object,java.nio.file.LinkOption[]) throws java.io.IOException
meth public abstract !varargs java.nio.channels.SeekableByteChannel newByteChannel(java.nio.file.Path,java.util.Set<? extends java.nio.file.OpenOption>,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public abstract !varargs java.nio.file.Path toRealPath(java.nio.file.Path,java.nio.file.LinkOption[]) throws java.io.IOException
meth public abstract !varargs java.util.Map<java.lang.String,java.lang.Object> readAttributes(java.nio.file.Path,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth public abstract !varargs void checkAccess(java.nio.file.Path,java.util.Set<? extends java.nio.file.AccessMode>,java.nio.file.LinkOption[]) throws java.io.IOException
meth public abstract !varargs void createDirectory(java.nio.file.Path,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public abstract java.nio.file.DirectoryStream<java.nio.file.Path> newDirectoryStream(java.nio.file.Path,java.nio.file.DirectoryStream$Filter<? super java.nio.file.Path>) throws java.io.IOException
meth public abstract java.nio.file.Path parsePath(java.lang.String)
meth public abstract java.nio.file.Path parsePath(java.net.URI)
meth public abstract java.nio.file.Path toAbsolutePath(java.nio.file.Path)
meth public abstract void delete(java.nio.file.Path) throws java.io.IOException
meth public java.lang.String getMimeType(java.nio.file.Path)
meth public java.lang.String getPathSeparator()
meth public java.lang.String getSeparator()
meth public java.nio.charset.Charset getEncoding(java.nio.file.Path)
meth public java.nio.file.Path getTempDirectory()
meth public java.nio.file.Path readSymbolicLink(java.nio.file.Path) throws java.io.IOException
meth public static org.graalvm.polyglot.io.FileSystem allowLanguageHomeAccess(org.graalvm.polyglot.io.FileSystem)
meth public static org.graalvm.polyglot.io.FileSystem newDefaultFileSystem()
meth public static org.graalvm.polyglot.io.FileSystem newFileSystem(java.nio.file.FileSystem)
meth public static org.graalvm.polyglot.io.FileSystem newReadOnlyFileSystem(org.graalvm.polyglot.io.FileSystem)
meth public void createLink(java.nio.file.Path,java.nio.file.Path) throws java.io.IOException
meth public void setCurrentWorkingDirectory(java.nio.file.Path)

CLSS public final org.graalvm.polyglot.io.IOAccess
fld public final static org.graalvm.polyglot.io.IOAccess ALL
fld public final static org.graalvm.polyglot.io.IOAccess NONE
innr public final static Builder
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public static org.graalvm.polyglot.io.IOAccess$Builder newBuilder()
meth public static org.graalvm.polyglot.io.IOAccess$Builder newBuilder(org.graalvm.polyglot.io.IOAccess)
supr java.lang.Object
hfds allowHostFileAccess,allowHostSocketAccess,fileSystem,name

CLSS public final static org.graalvm.polyglot.io.IOAccess$Builder
 outer org.graalvm.polyglot.io.IOAccess
meth public org.graalvm.polyglot.io.IOAccess build()
meth public org.graalvm.polyglot.io.IOAccess$Builder allowHostFileAccess(boolean)
meth public org.graalvm.polyglot.io.IOAccess$Builder allowHostSocketAccess(boolean)
meth public org.graalvm.polyglot.io.IOAccess$Builder fileSystem(org.graalvm.polyglot.io.FileSystem)
supr java.lang.Object
hfds allowHostFileAccess,allowHostSocketAccess,customFileSystem,name

CLSS public abstract interface org.graalvm.polyglot.io.MessageEndpoint
meth public abstract void sendBinary(java.nio.ByteBuffer) throws java.io.IOException
meth public abstract void sendClose() throws java.io.IOException
meth public abstract void sendPing(java.nio.ByteBuffer) throws java.io.IOException
meth public abstract void sendPong(java.nio.ByteBuffer) throws java.io.IOException
meth public abstract void sendText(java.lang.String) throws java.io.IOException

CLSS public abstract interface org.graalvm.polyglot.io.MessageTransport
innr public final static VetoException
meth public abstract org.graalvm.polyglot.io.MessageEndpoint open(java.net.URI,org.graalvm.polyglot.io.MessageEndpoint) throws java.io.IOException,org.graalvm.polyglot.io.MessageTransport$VetoException

CLSS public final static org.graalvm.polyglot.io.MessageTransport$VetoException
 outer org.graalvm.polyglot.io.MessageTransport
cons public init(java.lang.String)
supr java.lang.Exception
hfds serialVersionUID

CLSS public abstract interface org.graalvm.polyglot.io.ProcessHandler
innr public final static ProcessCommand
innr public final static Redirect
meth public abstract java.lang.Process start(org.graalvm.polyglot.io.ProcessHandler$ProcessCommand) throws java.io.IOException

CLSS public final static org.graalvm.polyglot.io.ProcessHandler$ProcessCommand
 outer org.graalvm.polyglot.io.ProcessHandler
meth public boolean isRedirectErrorStream()
meth public java.lang.String getDirectory()
meth public java.util.List<java.lang.String> getCommand()
meth public java.util.Map<java.lang.String,java.lang.String> getEnvironment()
meth public org.graalvm.polyglot.io.ProcessHandler$Redirect getErrorRedirect()
meth public org.graalvm.polyglot.io.ProcessHandler$Redirect getInputRedirect()
meth public org.graalvm.polyglot.io.ProcessHandler$Redirect getOutputRedirect()
supr java.lang.Object
hfds cmd,cwd,environment,errorRedirect,inputRedirect,outputRedirect,redirectErrorStream

CLSS public final static org.graalvm.polyglot.io.ProcessHandler$Redirect
 outer org.graalvm.polyglot.io.ProcessHandler
fld public final static org.graalvm.polyglot.io.ProcessHandler$Redirect INHERIT
fld public final static org.graalvm.polyglot.io.ProcessHandler$Redirect PIPE
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds stream,type
hcls Type

CLSS public final org.graalvm.polyglot.management.ExecutionEvent
meth public boolean isExpression()
meth public boolean isRoot()
meth public boolean isStatement()
meth public java.lang.String getRootName()
meth public java.lang.String toString()
meth public java.util.List<org.graalvm.polyglot.Value> getInputValues()
meth public org.graalvm.polyglot.PolyglotException getException()
meth public org.graalvm.polyglot.SourceSection getLocation()
meth public org.graalvm.polyglot.Value getReturnValue()
supr java.lang.Object
hfds dispatch,receiver

CLSS public final org.graalvm.polyglot.management.ExecutionListener
innr public final Builder
intf java.lang.AutoCloseable
meth public static org.graalvm.polyglot.management.ExecutionListener$Builder newBuilder()
meth public void close()
supr java.lang.Object
hfds EMPTY,dispatch,receiver

CLSS public final org.graalvm.polyglot.management.ExecutionListener$Builder
 outer org.graalvm.polyglot.management.ExecutionListener
meth public org.graalvm.polyglot.management.ExecutionListener attach(org.graalvm.polyglot.Engine)
meth public org.graalvm.polyglot.management.ExecutionListener$Builder collectExceptions(boolean)
meth public org.graalvm.polyglot.management.ExecutionListener$Builder collectInputValues(boolean)
meth public org.graalvm.polyglot.management.ExecutionListener$Builder collectReturnValue(boolean)
meth public org.graalvm.polyglot.management.ExecutionListener$Builder expressions(boolean)
meth public org.graalvm.polyglot.management.ExecutionListener$Builder onEnter(java.util.function.Consumer<org.graalvm.polyglot.management.ExecutionEvent>)
meth public org.graalvm.polyglot.management.ExecutionListener$Builder onReturn(java.util.function.Consumer<org.graalvm.polyglot.management.ExecutionEvent>)
meth public org.graalvm.polyglot.management.ExecutionListener$Builder rootNameFilter(java.util.function.Predicate<java.lang.String>)
meth public org.graalvm.polyglot.management.ExecutionListener$Builder roots(boolean)
meth public org.graalvm.polyglot.management.ExecutionListener$Builder sourceFilter(java.util.function.Predicate<org.graalvm.polyglot.Source>)
meth public org.graalvm.polyglot.management.ExecutionListener$Builder statements(boolean)
supr java.lang.Object
hfds collectExceptions,collectInputValues,collectReturnValues,expressions,onEnter,onReturn,rootNameFilter,roots,sourceFilter,statements

CLSS public abstract interface org.graalvm.polyglot.proxy.Proxy

CLSS public abstract interface org.graalvm.polyglot.proxy.ProxyArray
intf org.graalvm.polyglot.proxy.ProxyIterable
meth public !varargs static org.graalvm.polyglot.proxy.ProxyArray fromArray(java.lang.Object[])
meth public abstract java.lang.Object get(long)
meth public abstract long getSize()
meth public abstract void set(long,org.graalvm.polyglot.Value)
meth public boolean remove(long)
meth public java.lang.Object getIterator()
meth public static org.graalvm.polyglot.proxy.ProxyArray fromList(java.util.List<java.lang.Object>)

CLSS public abstract interface org.graalvm.polyglot.proxy.ProxyDate
intf org.graalvm.polyglot.proxy.Proxy
meth public abstract java.time.LocalDate asDate()
meth public static org.graalvm.polyglot.proxy.ProxyDate from(java.time.LocalDate)

CLSS public abstract interface org.graalvm.polyglot.proxy.ProxyDuration
intf org.graalvm.polyglot.proxy.Proxy
meth public abstract java.time.Duration asDuration()
meth public static org.graalvm.polyglot.proxy.ProxyDuration from(java.time.Duration)

CLSS public abstract interface org.graalvm.polyglot.proxy.ProxyExecutable
 anno 0 java.lang.FunctionalInterface()
intf org.graalvm.polyglot.proxy.Proxy
meth public abstract !varargs java.lang.Object execute(org.graalvm.polyglot.Value[])

CLSS public abstract interface org.graalvm.polyglot.proxy.ProxyHashMap
intf org.graalvm.polyglot.proxy.Proxy
meth public abstract boolean hasHashEntry(org.graalvm.polyglot.Value)
meth public abstract java.lang.Object getHashEntriesIterator()
meth public abstract java.lang.Object getHashValue(org.graalvm.polyglot.Value)
meth public abstract long getHashSize()
meth public abstract void putHashEntry(org.graalvm.polyglot.Value,org.graalvm.polyglot.Value)
meth public boolean removeHashEntry(org.graalvm.polyglot.Value)
meth public static org.graalvm.polyglot.proxy.ProxyHashMap from(java.util.Map<java.lang.Object,java.lang.Object>)

CLSS public abstract interface org.graalvm.polyglot.proxy.ProxyInstant
intf org.graalvm.polyglot.proxy.ProxyDate
intf org.graalvm.polyglot.proxy.ProxyTime
intf org.graalvm.polyglot.proxy.ProxyTimeZone
meth public abstract java.time.Instant asInstant()
meth public java.time.LocalDate asDate()
meth public java.time.LocalTime asTime()
meth public java.time.ZoneId asTimeZone()
meth public static org.graalvm.polyglot.proxy.ProxyInstant from(java.time.Instant)

CLSS public abstract interface org.graalvm.polyglot.proxy.ProxyInstantiable
 anno 0 java.lang.FunctionalInterface()
intf org.graalvm.polyglot.proxy.Proxy
meth public abstract !varargs java.lang.Object newInstance(org.graalvm.polyglot.Value[])

CLSS public abstract interface org.graalvm.polyglot.proxy.ProxyIterable
intf org.graalvm.polyglot.proxy.Proxy
meth public abstract java.lang.Object getIterator()
meth public static org.graalvm.polyglot.proxy.ProxyIterable from(java.lang.Iterable<java.lang.Object>)

CLSS public abstract interface org.graalvm.polyglot.proxy.ProxyIterator
intf org.graalvm.polyglot.proxy.Proxy
meth public abstract boolean hasNext()
meth public abstract java.lang.Object getNext()
meth public static org.graalvm.polyglot.proxy.ProxyIterator from(java.util.Iterator<?>)

CLSS public abstract interface org.graalvm.polyglot.proxy.ProxyNativeObject
intf org.graalvm.polyglot.proxy.Proxy
meth public abstract long asPointer()

CLSS public abstract interface org.graalvm.polyglot.proxy.ProxyObject
intf org.graalvm.polyglot.proxy.Proxy
meth public abstract boolean hasMember(java.lang.String)
meth public abstract java.lang.Object getMember(java.lang.String)
meth public abstract java.lang.Object getMemberKeys()
meth public abstract void putMember(java.lang.String,org.graalvm.polyglot.Value)
meth public boolean removeMember(java.lang.String)
meth public static org.graalvm.polyglot.proxy.ProxyObject fromMap(java.util.Map<java.lang.String,java.lang.Object>)

CLSS public abstract interface org.graalvm.polyglot.proxy.ProxyTime
intf org.graalvm.polyglot.proxy.Proxy
meth public abstract java.time.LocalTime asTime()
meth public static org.graalvm.polyglot.proxy.ProxyTime from(java.time.LocalTime)

CLSS public abstract interface org.graalvm.polyglot.proxy.ProxyTimeZone
intf org.graalvm.polyglot.proxy.Proxy
meth public abstract java.time.ZoneId asTimeZone()
meth public static org.graalvm.polyglot.proxy.ProxyTimeZone from(java.time.ZoneId)

CLSS public abstract interface org.graalvm.word.ComparableWord
intf org.graalvm.word.WordBase
meth public abstract boolean equal(org.graalvm.word.ComparableWord)
meth public abstract boolean notEqual(org.graalvm.word.ComparableWord)

CLSS public abstract interface org.graalvm.word.PointerBase
intf org.graalvm.word.ComparableWord
meth public abstract boolean isNonNull()
meth public abstract boolean isNull()

CLSS public abstract interface org.graalvm.word.WordBase
meth public abstract boolean equals(java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public abstract long rawValue()

CLSS public final org.netbeans.libs.graalsdk.GraalSDK
supr java.lang.Object

CLSS abstract interface org.netbeans.libs.graalsdk.package-info

