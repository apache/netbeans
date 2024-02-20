#Signature file v4.1
#Version 2.63

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public java.io.FileNotFoundException
cons public init()
cons public init(java.lang.String)
supr java.io.IOException

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract java.io.Reader
cons protected init()
cons protected init(java.lang.Object)
fld protected java.lang.Object lock
intf java.io.Closeable
intf java.lang.Readable
meth public abstract int read(char[],int,int) throws java.io.IOException
meth public abstract void close() throws java.io.IOException
meth public boolean markSupported()
meth public boolean ready() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(char[]) throws java.io.IOException
meth public int read(java.nio.CharBuffer) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public long transferTo(java.io.Writer) throws java.io.IOException
meth public static java.io.Reader nullReader()
meth public void mark(int) throws java.io.IOException
meth public void reset() throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Appendable
meth public abstract java.lang.Appendable append(char) throws java.io.IOException
meth public abstract java.lang.Appendable append(java.lang.CharSequence) throws java.io.IOException
meth public abstract java.lang.Appendable append(java.lang.CharSequence,int,int) throws java.io.IOException

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface java.lang.CharSequence
meth public abstract char charAt(int)
meth public abstract int length()
meth public abstract java.lang.CharSequence subSequence(int,int)
meth public abstract java.lang.String toString()
meth public java.util.stream.IntStream chars()
meth public java.util.stream.IntStream codePoints()
meth public static int compare(java.lang.CharSequence,java.lang.CharSequence)

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, MODULE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean forRemoval()
meth public abstract !hasdefault java.lang.String since()

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

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public java.lang.IllegalStateException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="9")
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public abstract interface java.lang.Readable
meth public abstract int read(java.nio.CharBuffer) throws java.io.IOException

CLSS public abstract interface java.lang.Runnable
 anno 0 java.lang.FunctionalInterface()
meth public abstract void run()

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public java.lang.Thread
cons public init()
cons public init(java.lang.Runnable)
cons public init(java.lang.Runnable,java.lang.String)
cons public init(java.lang.String)
cons public init(java.lang.ThreadGroup,java.lang.Runnable)
cons public init(java.lang.ThreadGroup,java.lang.Runnable,java.lang.String)
cons public init(java.lang.ThreadGroup,java.lang.Runnable,java.lang.String,long)
cons public init(java.lang.ThreadGroup,java.lang.Runnable,java.lang.String,long,boolean)
cons public init(java.lang.ThreadGroup,java.lang.String)
fld public final static int MAX_PRIORITY = 10
fld public final static int MIN_PRIORITY = 1
fld public final static int NORM_PRIORITY = 5
innr public abstract interface static UncaughtExceptionHandler
innr public final static !enum State
intf java.lang.Runnable
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public boolean isInterrupted()
meth public final boolean isAlive()
meth public final boolean isDaemon()
meth public final int getPriority()
meth public final java.lang.String getName()
meth public final java.lang.ThreadGroup getThreadGroup()
meth public final void checkAccess()
meth public final void join() throws java.lang.InterruptedException
meth public final void join(long) throws java.lang.InterruptedException
meth public final void join(long,int) throws java.lang.InterruptedException
meth public final void resume()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="1.2")
meth public final void setDaemon(boolean)
meth public final void setName(java.lang.String)
meth public final void setPriority(int)
meth public final void stop()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="1.2")
meth public final void suspend()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="1.2")
meth public int countStackFrames()
 anno 0 java.lang.Deprecated(boolean forRemoval=true, java.lang.String since="1.2")
meth public java.lang.ClassLoader getContextClassLoader()
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String toString()
meth public java.lang.Thread$State getState()
meth public java.lang.Thread$UncaughtExceptionHandler getUncaughtExceptionHandler()
meth public long getId()
meth public static boolean holdsLock(java.lang.Object)
meth public static boolean interrupted()
meth public static int activeCount()
meth public static int enumerate(java.lang.Thread[])
meth public static java.lang.Thread currentThread()
meth public static java.lang.Thread$UncaughtExceptionHandler getDefaultUncaughtExceptionHandler()
meth public static java.util.Map<java.lang.Thread,java.lang.StackTraceElement[]> getAllStackTraces()
meth public static void dumpStack()
meth public static void onSpinWait()
meth public static void setDefaultUncaughtExceptionHandler(java.lang.Thread$UncaughtExceptionHandler)
meth public static void sleep(long) throws java.lang.InterruptedException
meth public static void sleep(long,int) throws java.lang.InterruptedException
meth public static void yield()
meth public void interrupt()
meth public void run()
meth public void setContextClassLoader(java.lang.ClassLoader)
meth public void setUncaughtExceptionHandler(java.lang.Thread$UncaughtExceptionHandler)
meth public void start()
supr java.lang.Object

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

CLSS public abstract java.util.AbstractCollection<%0 extends java.lang.Object>
cons protected init()
intf java.util.Collection<{java.util.AbstractCollection%0}>
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract int size()
meth public abstract java.util.Iterator<{java.util.AbstractCollection%0}> iterator()
meth public boolean add({java.util.AbstractCollection%0})
meth public boolean addAll(java.util.Collection<? extends {java.util.AbstractCollection%0}>)
meth public boolean contains(java.lang.Object)
meth public boolean containsAll(java.util.Collection<?>)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean retainAll(java.util.Collection<?>)
meth public java.lang.Object[] toArray()
meth public java.lang.String toString()
meth public void clear()
supr java.lang.Object

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

CLSS public abstract java.util.AbstractSet<%0 extends java.lang.Object>
cons protected init()
intf java.util.Set<{java.util.AbstractSet%0}>
meth public boolean equals(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public int hashCode()
supr java.util.AbstractCollection<{java.util.AbstractSet%0}>

CLSS public abstract interface java.util.Collection<%0 extends java.lang.Object>
intf java.lang.Iterable<{java.util.Collection%0}>
meth public <%0 extends java.lang.Object> {%%0}[] toArray(java.util.function.IntFunction<{%%0}[]>)
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Collection%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Collection%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Collection%0}> iterator()
meth public abstract void clear()
meth public boolean removeIf(java.util.function.Predicate<? super {java.util.Collection%0}>)
meth public java.util.Spliterator<{java.util.Collection%0}> spliterator()
meth public java.util.stream.Stream<{java.util.Collection%0}> parallelStream()
meth public java.util.stream.Stream<{java.util.Collection%0}> stream()

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

CLSS public java.util.HashMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(int,float)
cons public init(java.util.Map<? extends {java.util.HashMap%0},? extends {java.util.HashMap%1}>)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.Map<{java.util.HashMap%0},{java.util.HashMap%1}>
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object,java.lang.Object)
meth public boolean replace({java.util.HashMap%0},{java.util.HashMap%1},{java.util.HashMap%1})
meth public int size()
meth public java.lang.Object clone()
meth public java.util.Collection<{java.util.HashMap%1}> values()
meth public java.util.Set<java.util.Map$Entry<{java.util.HashMap%0},{java.util.HashMap%1}>> entrySet()
meth public java.util.Set<{java.util.HashMap%0}> keySet()
meth public void clear()
meth public void forEach(java.util.function.BiConsumer<? super {java.util.HashMap%0},? super {java.util.HashMap%1}>)
meth public void putAll(java.util.Map<? extends {java.util.HashMap%0},? extends {java.util.HashMap%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.HashMap%0},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} compute({java.util.HashMap%0},java.util.function.BiFunction<? super {java.util.HashMap%0},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} computeIfAbsent({java.util.HashMap%0},java.util.function.Function<? super {java.util.HashMap%0},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} computeIfPresent({java.util.HashMap%0},java.util.function.BiFunction<? super {java.util.HashMap%0},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} get(java.lang.Object)
meth public {java.util.HashMap%1} getOrDefault(java.lang.Object,{java.util.HashMap%1})
meth public {java.util.HashMap%1} merge({java.util.HashMap%0},{java.util.HashMap%1},java.util.function.BiFunction<? super {java.util.HashMap%1},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} put({java.util.HashMap%0},{java.util.HashMap%1})
meth public {java.util.HashMap%1} putIfAbsent({java.util.HashMap%0},{java.util.HashMap%1})
meth public {java.util.HashMap%1} remove(java.lang.Object)
meth public {java.util.HashMap%1} replace({java.util.HashMap%0},{java.util.HashMap%1})
supr java.util.AbstractMap<{java.util.HashMap%0},{java.util.HashMap%1}>

CLSS public abstract interface java.util.Iterator<%0 extends java.lang.Object>
meth public abstract boolean hasNext()
meth public abstract {java.util.Iterator%0} next()
meth public void forEachRemaining(java.util.function.Consumer<? super {java.util.Iterator%0}>)
meth public void remove()

CLSS public abstract interface java.util.Map<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public abstract interface static Entry
meth public !varargs static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> ofEntries(java.util.Map$Entry<? extends {%%0},? extends {%%1}>[])
 anno 0 java.lang.SafeVarargs()
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
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map$Entry<{%%0},{%%1}> entry({%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> copyOf(java.util.Map<? extends {%%0},? extends {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> of({%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1},{%%0},{%%1})
meth public void forEach(java.util.function.BiConsumer<? super {java.util.Map%0},? super {java.util.Map%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} compute({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfAbsent({java.util.Map%0},java.util.function.Function<? super {java.util.Map%0},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfPresent({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} getOrDefault(java.lang.Object,{java.util.Map%1})
meth public {java.util.Map%1} merge({java.util.Map%0},{java.util.Map%1},java.util.function.BiFunction<? super {java.util.Map%1},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} putIfAbsent({java.util.Map%0},{java.util.Map%1})
meth public {java.util.Map%1} replace({java.util.Map%0},{java.util.Map%1})

CLSS public java.util.Observable
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="9")
cons public init()
meth protected void clearChanged()
meth protected void setChanged()
meth public boolean hasChanged()
meth public int countObservers()
meth public void addObserver(java.util.Observer)
meth public void deleteObserver(java.util.Observer)
meth public void deleteObservers()
meth public void notifyObservers()
meth public void notifyObservers(java.lang.Object)
supr java.lang.Object

CLSS public abstract interface java.util.Set<%0 extends java.lang.Object>
intf java.util.Collection<{java.util.Set%0}>
meth public !varargs static <%0 extends java.lang.Object> java.util.Set<{%%0}> of({%%0}[])
 anno 0 java.lang.SafeVarargs()
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Set%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Set%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Set%0}> iterator()
meth public abstract void clear()
meth public java.util.Spliterator<{java.util.Set%0}> spliterator()
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> copyOf(java.util.Collection<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> of()
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> of({%%0})
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> of({%%0},{%%0})
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> of({%%0},{%%0},{%%0})
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> of({%%0},{%%0},{%%0},{%%0})
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> of({%%0},{%%0},{%%0},{%%0},{%%0})
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> of({%%0},{%%0},{%%0},{%%0},{%%0},{%%0})
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> of({%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0})
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> of({%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0})
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> of({%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0})
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> of({%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0},{%%0})

CLSS public abstract java.util.concurrent.AbstractExecutorService
cons public init()
intf java.util.concurrent.ExecutorService
meth protected <%0 extends java.lang.Object> java.util.concurrent.RunnableFuture<{%%0}> newTaskFor(java.lang.Runnable,{%%0})
meth protected <%0 extends java.lang.Object> java.util.concurrent.RunnableFuture<{%%0}> newTaskFor(java.util.concurrent.Callable<{%%0}>)
meth public <%0 extends java.lang.Object> java.util.List<java.util.concurrent.Future<{%%0}>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>) throws java.lang.InterruptedException
meth public <%0 extends java.lang.Object> java.util.List<java.util.concurrent.Future<{%%0}>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>,long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.lang.Runnable,{%%0})
meth public <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.util.concurrent.Callable<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException
meth public <%0 extends java.lang.Object> {%%0} invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>,long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException,java.util.concurrent.TimeoutException
meth public java.util.concurrent.Future<?> submit(java.lang.Runnable)
supr java.lang.Object

CLSS public abstract interface java.util.concurrent.Executor
meth public abstract void execute(java.lang.Runnable)

CLSS public abstract interface java.util.concurrent.ExecutorService
intf java.util.concurrent.Executor
meth public abstract <%0 extends java.lang.Object> java.util.List<java.util.concurrent.Future<{%%0}>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>) throws java.lang.InterruptedException
meth public abstract <%0 extends java.lang.Object> java.util.List<java.util.concurrent.Future<{%%0}>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>,long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.lang.Runnable,{%%0})
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.util.concurrent.Callable<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException
meth public abstract <%0 extends java.lang.Object> {%%0} invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>,long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException,java.util.concurrent.TimeoutException
meth public abstract boolean awaitTermination(long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public abstract boolean isShutdown()
meth public abstract boolean isTerminated()
meth public abstract java.util.List<java.lang.Runnable> shutdownNow()
meth public abstract java.util.concurrent.Future<?> submit(java.lang.Runnable)
meth public abstract void shutdown()

CLSS public abstract interface java.util.concurrent.ThreadFactory
meth public abstract java.lang.Thread newThread(java.lang.Runnable)

CLSS public abstract interface java.util.zip.Checksum
meth public abstract long getValue()
meth public abstract void reset()
meth public abstract void update(byte[],int,int)
meth public abstract void update(int)
meth public void update(byte[])
meth public void update(java.nio.ByteBuffer)

CLSS public abstract org.apache.lucene.analysis.AbstractAnalysisFactory
cons protected init()
cons protected init(java.util.Map<java.lang.String,java.lang.String>)
fld protected final org.apache.lucene.util.Version luceneMatchVersion
fld public final static java.lang.String LUCENE_MATCH_VERSION_PARAM = "luceneMatchVersion"
meth protected final boolean getBoolean(java.util.Map<java.lang.String,java.lang.String>,java.lang.String,boolean)
meth protected final boolean requireBoolean(java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
meth protected final float getFloat(java.util.Map<java.lang.String,java.lang.String>,java.lang.String,float)
meth protected final float requireFloat(java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
meth protected final int getInt(java.util.Map<java.lang.String,java.lang.String>,java.lang.String,int)
meth protected final int requireInt(java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
meth protected final java.util.List<java.lang.String> getLines(org.apache.lucene.util.ResourceLoader,java.lang.String) throws java.io.IOException
meth protected final java.util.List<java.lang.String> splitAt(char,java.lang.String)
meth protected final java.util.List<java.lang.String> splitFileNames(java.lang.String)
meth protected final java.util.regex.Pattern getPattern(java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
meth protected final org.apache.lucene.analysis.CharArraySet getSnowballWordSet(org.apache.lucene.util.ResourceLoader,java.lang.String,boolean) throws java.io.IOException
meth protected final org.apache.lucene.analysis.CharArraySet getWordSet(org.apache.lucene.util.ResourceLoader,java.lang.String,boolean) throws java.io.IOException
meth protected static java.lang.RuntimeException defaultCtorException()
meth public boolean isExplicitLuceneMatchVersion()
meth public char getChar(java.util.Map<java.lang.String,java.lang.String>,java.lang.String,char)
meth public char requireChar(java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
meth public final java.util.Map<java.lang.String,java.lang.String> getOriginalArgs()
meth public final org.apache.lucene.util.Version getLuceneMatchVersion()
meth public java.lang.String get(java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
meth public java.lang.String get(java.util.Map<java.lang.String,java.lang.String>,java.lang.String,java.lang.String)
meth public java.lang.String get(java.util.Map<java.lang.String,java.lang.String>,java.lang.String,java.util.Collection<java.lang.String>)
meth public java.lang.String get(java.util.Map<java.lang.String,java.lang.String>,java.lang.String,java.util.Collection<java.lang.String>,java.lang.String)
meth public java.lang.String get(java.util.Map<java.lang.String,java.lang.String>,java.lang.String,java.util.Collection<java.lang.String>,java.lang.String,boolean)
meth public java.lang.String getClassArg()
meth public java.lang.String require(java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
meth public java.lang.String require(java.util.Map<java.lang.String,java.lang.String>,java.lang.String,java.util.Collection<java.lang.String>)
meth public java.lang.String require(java.util.Map<java.lang.String,java.lang.String>,java.lang.String,java.util.Collection<java.lang.String>,boolean)
meth public java.util.Set<java.lang.String> getSet(java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
meth public void setExplicitLuceneMatchVersion(boolean)
supr java.lang.Object
hfds CLASS_NAME,ITEM_PATTERN,SPI_NAME,isExplicitLuceneMatchVersion,originalArgs

CLSS public final org.apache.lucene.analysis.AnalysisSPILoader<%0 extends org.apache.lucene.analysis.AbstractAnalysisFactory>
cons public init(java.lang.Class<{org.apache.lucene.analysis.AnalysisSPILoader%0}>)
cons public init(java.lang.Class<{org.apache.lucene.analysis.AnalysisSPILoader%0}>,java.lang.ClassLoader)
meth public java.lang.Class<? extends {org.apache.lucene.analysis.AnalysisSPILoader%0}> lookupClass(java.lang.String)
meth public java.util.Set<java.lang.String> availableServices()
meth public static <%0 extends org.apache.lucene.analysis.AbstractAnalysisFactory> {%%0} newFactoryClassInstance(java.lang.Class<{%%0}>,java.util.Map<java.lang.String,java.lang.String>)
meth public static java.lang.String lookupSPIName(java.lang.Class<? extends org.apache.lucene.analysis.AbstractAnalysisFactory>) throws java.lang.IllegalAccessException,java.lang.NoSuchFieldException
meth public void reload(java.lang.ClassLoader)
meth public {org.apache.lucene.analysis.AnalysisSPILoader%0} newInstance(java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
supr java.lang.Object
hfds SERVICE_NAME_PATTERN,clazz,originalNames,services

CLSS public abstract org.apache.lucene.analysis.Analyzer
cons protected init()
cons protected init(org.apache.lucene.analysis.Analyzer$ReuseStrategy)
fld public final static org.apache.lucene.analysis.Analyzer$ReuseStrategy GLOBAL_REUSE_STRATEGY
fld public final static org.apache.lucene.analysis.Analyzer$ReuseStrategy PER_FIELD_REUSE_STRATEGY
innr public abstract static ReuseStrategy
innr public final static TokenStreamComponents
intf java.io.Closeable
meth protected abstract org.apache.lucene.analysis.Analyzer$TokenStreamComponents createComponents(java.lang.String)
meth protected java.io.Reader initReader(java.lang.String,java.io.Reader)
meth protected java.io.Reader initReaderForNormalization(java.lang.String,java.io.Reader)
meth protected org.apache.lucene.analysis.TokenStream normalize(java.lang.String,org.apache.lucene.analysis.TokenStream)
meth protected org.apache.lucene.util.AttributeFactory attributeFactory(java.lang.String)
meth public final org.apache.lucene.analysis.Analyzer$ReuseStrategy getReuseStrategy()
meth public final org.apache.lucene.analysis.TokenStream tokenStream(java.lang.String,java.io.Reader)
meth public final org.apache.lucene.analysis.TokenStream tokenStream(java.lang.String,java.lang.String)
meth public final org.apache.lucene.util.BytesRef normalize(java.lang.String,java.lang.String)
meth public int getOffsetGap(java.lang.String)
meth public int getPositionIncrementGap(java.lang.String)
meth public void close()
supr java.lang.Object
hfds reuseStrategy,storedValue
hcls StringTokenStream

CLSS public abstract static org.apache.lucene.analysis.Analyzer$ReuseStrategy
 outer org.apache.lucene.analysis.Analyzer
cons protected init()
meth protected final java.lang.Object getStoredValue(org.apache.lucene.analysis.Analyzer)
meth protected final void setStoredValue(org.apache.lucene.analysis.Analyzer,java.lang.Object)
meth public abstract org.apache.lucene.analysis.Analyzer$TokenStreamComponents getReusableComponents(org.apache.lucene.analysis.Analyzer,java.lang.String)
meth public abstract void setReusableComponents(org.apache.lucene.analysis.Analyzer,java.lang.String,org.apache.lucene.analysis.Analyzer$TokenStreamComponents)
supr java.lang.Object

CLSS public final static org.apache.lucene.analysis.Analyzer$TokenStreamComponents
 outer org.apache.lucene.analysis.Analyzer
cons public init(java.util.function.Consumer<java.io.Reader>,org.apache.lucene.analysis.TokenStream)
cons public init(org.apache.lucene.analysis.Tokenizer)
cons public init(org.apache.lucene.analysis.Tokenizer,org.apache.lucene.analysis.TokenStream)
fld protected final java.util.function.Consumer<java.io.Reader> source
fld protected final org.apache.lucene.analysis.TokenStream sink
meth public java.util.function.Consumer<java.io.Reader> getSource()
meth public org.apache.lucene.analysis.TokenStream getTokenStream()
supr java.lang.Object
hfds reusableStringReader

CLSS public abstract org.apache.lucene.analysis.AnalyzerWrapper
cons protected init(org.apache.lucene.analysis.Analyzer$ReuseStrategy)
meth protected abstract org.apache.lucene.analysis.Analyzer getWrappedAnalyzer(java.lang.String)
meth protected final java.io.Reader initReaderForNormalization(java.lang.String,java.io.Reader)
meth protected final org.apache.lucene.analysis.Analyzer$TokenStreamComponents createComponents(java.lang.String)
meth protected final org.apache.lucene.analysis.TokenStream normalize(java.lang.String,org.apache.lucene.analysis.TokenStream)
meth protected final org.apache.lucene.util.AttributeFactory attributeFactory(java.lang.String)
meth protected java.io.Reader wrapReader(java.lang.String,java.io.Reader)
meth protected java.io.Reader wrapReaderForNormalization(java.lang.String,java.io.Reader)
meth protected org.apache.lucene.analysis.Analyzer$TokenStreamComponents wrapComponents(java.lang.String,org.apache.lucene.analysis.Analyzer$TokenStreamComponents)
meth protected org.apache.lucene.analysis.TokenStream wrapTokenStreamForNormalization(java.lang.String,org.apache.lucene.analysis.TokenStream)
meth public final java.io.Reader initReader(java.lang.String,java.io.Reader)
meth public int getOffsetGap(java.lang.String)
meth public int getPositionIncrementGap(java.lang.String)
supr org.apache.lucene.analysis.Analyzer

CLSS public org.apache.lucene.analysis.AutomatonToTokenStream
meth public static org.apache.lucene.analysis.TokenStream toTokenStream(org.apache.lucene.util.automaton.Automaton)
supr java.lang.Object
hcls EdgeToken,RemapNode,TopoTokenStream

CLSS public final org.apache.lucene.analysis.CachingTokenFilter
cons public init(org.apache.lucene.analysis.TokenStream)
meth public boolean isCached()
meth public final boolean incrementToken() throws java.io.IOException
meth public final void end()
meth public void reset() throws java.io.IOException
supr org.apache.lucene.analysis.TokenFilter
hfds cache,finalState,iterator

CLSS public org.apache.lucene.analysis.CharArrayMap<%0 extends java.lang.Object>
cons public init(int,boolean)
cons public init(java.util.Map<?,? extends {org.apache.lucene.analysis.CharArrayMap%0}>,boolean)
innr public EntryIterator
innr public final EntrySet
meth public boolean containsKey(char[],int,int)
meth public boolean containsKey(java.lang.CharSequence)
meth public boolean containsKey(java.lang.Object)
meth public final org.apache.lucene.analysis.CharArrayMap$EntrySet entrySet()
meth public final org.apache.lucene.analysis.CharArraySet keySet()
meth public int size()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Object> org.apache.lucene.analysis.CharArrayMap<{%%0}> copy(java.util.Map<?,? extends {%%0}>)
meth public static <%0 extends java.lang.Object> org.apache.lucene.analysis.CharArrayMap<{%%0}> emptyMap()
meth public static <%0 extends java.lang.Object> org.apache.lucene.analysis.CharArrayMap<{%%0}> unmodifiableMap(org.apache.lucene.analysis.CharArrayMap<{%%0}>)
meth public void clear()
meth public {org.apache.lucene.analysis.CharArrayMap%0} get(char[],int,int)
meth public {org.apache.lucene.analysis.CharArrayMap%0} get(java.lang.CharSequence)
meth public {org.apache.lucene.analysis.CharArrayMap%0} get(java.lang.Object)
meth public {org.apache.lucene.analysis.CharArrayMap%0} put(char[],{org.apache.lucene.analysis.CharArrayMap%0})
meth public {org.apache.lucene.analysis.CharArrayMap%0} put(java.lang.CharSequence,{org.apache.lucene.analysis.CharArrayMap%0})
meth public {org.apache.lucene.analysis.CharArrayMap%0} put(java.lang.Object,{org.apache.lucene.analysis.CharArrayMap%0})
meth public {org.apache.lucene.analysis.CharArrayMap%0} put(java.lang.String,{org.apache.lucene.analysis.CharArrayMap%0})
meth public {org.apache.lucene.analysis.CharArrayMap%0} remove(java.lang.Object)
supr java.util.AbstractMap<java.lang.Object,{org.apache.lucene.analysis.CharArrayMap%0}>
hfds EMPTY_MAP,INIT_SIZE,count,entrySet,ignoreCase,keySet,keys,values
hcls EmptyCharArrayMap,MapEntry,UnmodifiableCharArrayMap

CLSS public org.apache.lucene.analysis.CharArrayMap$EntryIterator
 outer org.apache.lucene.analysis.CharArrayMap
intf java.util.Iterator<java.util.Map$Entry<java.lang.Object,{org.apache.lucene.analysis.CharArrayMap%0}>>
meth public boolean hasNext()
meth public char[] nextKey()
meth public java.lang.String nextKeyString()
meth public java.util.Map$Entry<java.lang.Object,{org.apache.lucene.analysis.CharArrayMap%0}> next()
meth public void remove()
meth public {org.apache.lucene.analysis.CharArrayMap%0} currentValue()
meth public {org.apache.lucene.analysis.CharArrayMap%0} setValue({org.apache.lucene.analysis.CharArrayMap%0})
supr java.lang.Object
hfds allowModify,lastPos,pos

CLSS public final org.apache.lucene.analysis.CharArrayMap$EntrySet
 outer org.apache.lucene.analysis.CharArrayMap
meth public boolean contains(java.lang.Object)
meth public boolean remove(java.lang.Object)
meth public int size()
meth public org.apache.lucene.analysis.CharArrayMap$EntryIterator iterator()
meth public void clear()
supr java.util.AbstractSet<java.util.Map$Entry<java.lang.Object,{org.apache.lucene.analysis.CharArrayMap%0}>>
hfds allowModify

CLSS public org.apache.lucene.analysis.CharArraySet
cons public init(int,boolean)
cons public init(java.util.Collection<?>,boolean)
fld public final static org.apache.lucene.analysis.CharArraySet EMPTY_SET
meth public boolean add(char[])
meth public boolean add(java.lang.CharSequence)
meth public boolean add(java.lang.Object)
meth public boolean add(java.lang.String)
meth public boolean contains(char[],int,int)
meth public boolean contains(java.lang.CharSequence)
meth public boolean contains(java.lang.Object)
meth public int size()
meth public java.lang.String toString()
meth public java.util.Iterator<java.lang.Object> iterator()
meth public static org.apache.lucene.analysis.CharArraySet copy(java.util.Set<?>)
meth public static org.apache.lucene.analysis.CharArraySet unmodifiableSet(org.apache.lucene.analysis.CharArraySet)
meth public void clear()
supr java.util.AbstractSet<java.lang.Object>
hfds PLACEHOLDER,map

CLSS public abstract org.apache.lucene.analysis.CharFilter
cons public init(java.io.Reader)
fld protected final java.io.Reader input
meth protected abstract int correct(int)
meth public final int correctOffset(int)
meth public void close() throws java.io.IOException
supr java.io.Reader

CLSS public abstract org.apache.lucene.analysis.CharFilterFactory
cons protected init()
cons protected init(java.util.Map<java.lang.String,java.lang.String>)
meth public abstract java.io.Reader create(java.io.Reader)
meth public java.io.Reader normalize(java.io.Reader)
meth public static java.lang.Class<? extends org.apache.lucene.analysis.CharFilterFactory> lookupClass(java.lang.String)
meth public static java.lang.String findSPIName(java.lang.Class<? extends org.apache.lucene.analysis.CharFilterFactory>)
meth public static java.util.Set<java.lang.String> availableCharFilters()
meth public static org.apache.lucene.analysis.CharFilterFactory forName(java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
meth public static void reloadCharFilters(java.lang.ClassLoader)
supr org.apache.lucene.analysis.AbstractAnalysisFactory
hcls Holder

CLSS public final org.apache.lucene.analysis.CharacterUtils
innr public final static CharacterBuffer
meth public static boolean fill(org.apache.lucene.analysis.CharacterUtils$CharacterBuffer,java.io.Reader) throws java.io.IOException
meth public static boolean fill(org.apache.lucene.analysis.CharacterUtils$CharacterBuffer,java.io.Reader,int) throws java.io.IOException
meth public static int toChars(int[],int,int,char[],int)
meth public static int toCodePoints(char[],int,int,int[],int)
meth public static org.apache.lucene.analysis.CharacterUtils$CharacterBuffer newCharacterBuffer(int)
meth public static void toLowerCase(char[],int,int)
meth public static void toUpperCase(char[],int,int)
supr java.lang.Object

CLSS public final static org.apache.lucene.analysis.CharacterUtils$CharacterBuffer
 outer org.apache.lucene.analysis.CharacterUtils
meth public char[] getBuffer()
meth public int getLength()
meth public int getOffset()
meth public void reset()
supr java.lang.Object
hfds buffer,lastTrailingHighSurrogate,length,offset

CLSS public abstract org.apache.lucene.analysis.DelegatingAnalyzerWrapper
cons protected init(org.apache.lucene.analysis.Analyzer$ReuseStrategy)
meth protected final java.io.Reader wrapReader(java.lang.String,java.io.Reader)
meth protected final java.io.Reader wrapReaderForNormalization(java.lang.String,java.io.Reader)
meth protected final org.apache.lucene.analysis.Analyzer$TokenStreamComponents wrapComponents(java.lang.String,org.apache.lucene.analysis.Analyzer$TokenStreamComponents)
meth protected final org.apache.lucene.analysis.TokenStream wrapTokenStreamForNormalization(java.lang.String,org.apache.lucene.analysis.TokenStream)
supr org.apache.lucene.analysis.AnalyzerWrapper
hcls DelegatingReuseStrategy

CLSS public abstract org.apache.lucene.analysis.FilteringTokenFilter
cons public init(org.apache.lucene.analysis.TokenStream)
meth protected abstract boolean accept() throws java.io.IOException
meth public final boolean incrementToken() throws java.io.IOException
meth public void end() throws java.io.IOException
meth public void reset() throws java.io.IOException
supr org.apache.lucene.analysis.TokenFilter
hfds posIncrAtt,skippedPositions

CLSS public abstract org.apache.lucene.analysis.GraphTokenFilter
cons public init(org.apache.lucene.analysis.TokenStream)
fld public final static int MAX_GRAPH_STACK_SIZE = 1000
fld public final static int MAX_TOKEN_CACHE_SIZE = 100
meth protected final boolean incrementBaseToken() throws java.io.IOException
meth protected final boolean incrementGraph() throws java.io.IOException
meth protected final boolean incrementGraphToken() throws java.io.IOException
meth public int getTrailingPositions()
meth public void end() throws java.io.IOException
meth public void reset() throws java.io.IOException
supr org.apache.lucene.analysis.TokenFilter
hfds baseToken,cacheSize,currentGraph,finalOffsets,graphDepth,graphPos,offsetAtt,posIncAtt,stackSize,tokenPool,trailingPositions
hcls Token

CLSS public org.apache.lucene.analysis.LowerCaseFilter
cons public init(org.apache.lucene.analysis.TokenStream)
meth public final boolean incrementToken() throws java.io.IOException
supr org.apache.lucene.analysis.TokenFilter
hfds termAtt

CLSS public org.apache.lucene.analysis.StopFilter
cons public init(org.apache.lucene.analysis.TokenStream,org.apache.lucene.analysis.CharArraySet)
meth protected boolean accept()
meth public !varargs static org.apache.lucene.analysis.CharArraySet makeStopSet(java.lang.String[])
meth public static org.apache.lucene.analysis.CharArraySet makeStopSet(java.lang.String[],boolean)
meth public static org.apache.lucene.analysis.CharArraySet makeStopSet(java.util.List<?>)
meth public static org.apache.lucene.analysis.CharArraySet makeStopSet(java.util.List<?>,boolean)
supr org.apache.lucene.analysis.FilteringTokenFilter
hfds stopWords,termAtt

CLSS public abstract org.apache.lucene.analysis.StopwordAnalyzerBase
cons protected init()
cons protected init(org.apache.lucene.analysis.CharArraySet)
fld protected final org.apache.lucene.analysis.CharArraySet stopwords
meth protected static org.apache.lucene.analysis.CharArraySet loadStopwordSet(boolean,java.lang.Class<? extends org.apache.lucene.analysis.Analyzer>,java.lang.String,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=true, java.lang.String since="9.1")
meth protected static org.apache.lucene.analysis.CharArraySet loadStopwordSet(java.io.Reader) throws java.io.IOException
meth protected static org.apache.lucene.analysis.CharArraySet loadStopwordSet(java.nio.file.Path) throws java.io.IOException
meth public org.apache.lucene.analysis.CharArraySet getStopwordSet()
supr org.apache.lucene.analysis.Analyzer

CLSS public abstract org.apache.lucene.analysis.TokenFilter
cons protected init(org.apache.lucene.analysis.TokenStream)
fld protected final org.apache.lucene.analysis.TokenStream input
intf org.apache.lucene.util.Unwrappable<org.apache.lucene.analysis.TokenStream>
meth public org.apache.lucene.analysis.TokenStream unwrap()
meth public void close() throws java.io.IOException
meth public void end() throws java.io.IOException
meth public void reset() throws java.io.IOException
supr org.apache.lucene.analysis.TokenStream

CLSS public abstract org.apache.lucene.analysis.TokenFilterFactory
cons protected init()
cons protected init(java.util.Map<java.lang.String,java.lang.String>)
meth public abstract org.apache.lucene.analysis.TokenStream create(org.apache.lucene.analysis.TokenStream)
meth public org.apache.lucene.analysis.TokenStream normalize(org.apache.lucene.analysis.TokenStream)
meth public static java.lang.Class<? extends org.apache.lucene.analysis.TokenFilterFactory> lookupClass(java.lang.String)
meth public static java.lang.String findSPIName(java.lang.Class<? extends org.apache.lucene.analysis.TokenFilterFactory>)
meth public static java.util.Set<java.lang.String> availableTokenFilters()
meth public static org.apache.lucene.analysis.TokenFilterFactory forName(java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
meth public static void reloadTokenFilters(java.lang.ClassLoader)
supr org.apache.lucene.analysis.AbstractAnalysisFactory
hcls Holder

CLSS public abstract org.apache.lucene.analysis.TokenStream
cons protected init()
cons protected init(org.apache.lucene.util.AttributeFactory)
cons protected init(org.apache.lucene.util.AttributeSource)
fld public final static org.apache.lucene.util.AttributeFactory DEFAULT_TOKEN_ATTRIBUTE_FACTORY
intf java.io.Closeable
meth public abstract boolean incrementToken() throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void end() throws java.io.IOException
meth public void reset() throws java.io.IOException
supr org.apache.lucene.util.AttributeSource

CLSS public org.apache.lucene.analysis.TokenStreamToAutomaton
cons public init()
fld public final static int HOLE = 30
fld public final static int POS_SEP = 31
meth protected org.apache.lucene.util.BytesRef changeToken(org.apache.lucene.util.BytesRef)
meth public org.apache.lucene.util.automaton.Automaton toAutomaton(org.apache.lucene.analysis.TokenStream) throws java.io.IOException
meth public void setFinalOffsetGapAsHole(boolean)
meth public void setPreservePositionIncrements(boolean)
meth public void setUnicodeArcs(boolean)
supr java.lang.Object
hfds finalOffsetGapAsHole,preservePositionIncrements,unicodeArcs
hcls Position,Positions

CLSS public abstract org.apache.lucene.analysis.Tokenizer
cons protected init()
cons protected init(org.apache.lucene.util.AttributeFactory)
fld protected java.io.Reader input
meth protected final int correctOffset(int)
meth protected void setReaderTestPoint()
meth public final void setReader(java.io.Reader)
meth public void close() throws java.io.IOException
meth public void reset() throws java.io.IOException
supr org.apache.lucene.analysis.TokenStream
hfds ILLEGAL_STATE_READER,inputPending

CLSS public abstract org.apache.lucene.analysis.TokenizerFactory
cons protected init()
cons protected init(java.util.Map<java.lang.String,java.lang.String>)
meth public abstract org.apache.lucene.analysis.Tokenizer create(org.apache.lucene.util.AttributeFactory)
meth public final org.apache.lucene.analysis.Tokenizer create()
meth public static java.lang.Class<? extends org.apache.lucene.analysis.TokenizerFactory> lookupClass(java.lang.String)
meth public static java.lang.String findSPIName(java.lang.Class<? extends org.apache.lucene.analysis.TokenizerFactory>)
meth public static java.util.Set<java.lang.String> availableTokenizers()
meth public static org.apache.lucene.analysis.TokenizerFactory forName(java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
meth public static void reloadTokenizers(java.lang.ClassLoader)
supr org.apache.lucene.analysis.AbstractAnalysisFactory
hcls Holder

CLSS public org.apache.lucene.analysis.WordlistLoader
meth public static java.util.List<java.lang.String> getLines(java.io.InputStream,java.nio.charset.Charset) throws java.io.IOException
meth public static org.apache.lucene.analysis.CharArrayMap<java.lang.String> getStemDict(java.io.Reader,org.apache.lucene.analysis.CharArrayMap<java.lang.String>) throws java.io.IOException
meth public static org.apache.lucene.analysis.CharArraySet getSnowballWordSet(java.io.InputStream) throws java.io.IOException
meth public static org.apache.lucene.analysis.CharArraySet getSnowballWordSet(java.io.InputStream,java.nio.charset.Charset) throws java.io.IOException
meth public static org.apache.lucene.analysis.CharArraySet getSnowballWordSet(java.io.Reader) throws java.io.IOException
meth public static org.apache.lucene.analysis.CharArraySet getSnowballWordSet(java.io.Reader,org.apache.lucene.analysis.CharArraySet) throws java.io.IOException
meth public static org.apache.lucene.analysis.CharArraySet getWordSet(java.io.InputStream) throws java.io.IOException
meth public static org.apache.lucene.analysis.CharArraySet getWordSet(java.io.InputStream,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.analysis.CharArraySet getWordSet(java.io.InputStream,java.nio.charset.Charset) throws java.io.IOException
meth public static org.apache.lucene.analysis.CharArraySet getWordSet(java.io.InputStream,java.nio.charset.Charset,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.analysis.CharArraySet getWordSet(java.io.Reader) throws java.io.IOException
meth public static org.apache.lucene.analysis.CharArraySet getWordSet(java.io.Reader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.analysis.CharArraySet getWordSet(java.io.Reader,java.lang.String,org.apache.lucene.analysis.CharArraySet) throws java.io.IOException
meth public static org.apache.lucene.analysis.CharArraySet getWordSet(java.io.Reader,org.apache.lucene.analysis.CharArraySet) throws java.io.IOException
supr java.lang.Object
hfds INITIAL_CAPACITY

CLSS public final org.apache.lucene.analysis.standard.StandardAnalyzer
cons public init()
cons public init(java.io.Reader) throws java.io.IOException
cons public init(org.apache.lucene.analysis.CharArraySet)
fld public final static int DEFAULT_MAX_TOKEN_LENGTH = 255
meth protected org.apache.lucene.analysis.Analyzer$TokenStreamComponents createComponents(java.lang.String)
meth protected org.apache.lucene.analysis.TokenStream normalize(java.lang.String,org.apache.lucene.analysis.TokenStream)
meth public int getMaxTokenLength()
meth public void setMaxTokenLength(int)
supr org.apache.lucene.analysis.StopwordAnalyzerBase
hfds maxTokenLength

CLSS public final org.apache.lucene.analysis.standard.StandardTokenizer
cons public init()
cons public init(org.apache.lucene.util.AttributeFactory)
fld public final static int ALPHANUM = 0
fld public final static int EMOJI = 7
fld public final static int HANGUL = 6
fld public final static int HIRAGANA = 4
fld public final static int IDEOGRAPHIC = 3
fld public final static int KATAKANA = 5
fld public final static int MAX_TOKEN_LENGTH_LIMIT = 1048576
fld public final static int NUM = 1
fld public final static int SOUTHEAST_ASIAN = 2
fld public final static java.lang.String[] TOKEN_TYPES
meth public final boolean incrementToken() throws java.io.IOException
meth public final void end() throws java.io.IOException
meth public int getMaxTokenLength()
meth public void close() throws java.io.IOException
meth public void reset() throws java.io.IOException
meth public void setMaxTokenLength(int)
supr org.apache.lucene.analysis.Tokenizer
hfds maxTokenLength,offsetAtt,posIncrAtt,scanner,skippedPositions,termAtt,typeAtt

CLSS public org.apache.lucene.analysis.standard.StandardTokenizerFactory
cons public init()
cons public init(java.util.Map<java.lang.String,java.lang.String>)
fld public final static java.lang.String NAME = "standard"
meth public org.apache.lucene.analysis.standard.StandardTokenizer create(org.apache.lucene.util.AttributeFactory)
supr org.apache.lucene.analysis.TokenizerFactory
hfds maxTokenLength

CLSS public final org.apache.lucene.analysis.standard.StandardTokenizerImpl
cons public init(java.io.Reader)
fld public final static int EMOJI_TYPE = 7
fld public final static int HANGUL_TYPE = 6
fld public final static int HIRAGANA_TYPE = 4
fld public final static int IDEOGRAPHIC_TYPE = 3
fld public final static int KATAKANA_TYPE = 5
fld public final static int NUMERIC_TYPE = 1
fld public final static int SOUTH_EAST_ASIAN_TYPE = 2
fld public final static int WORD_TYPE = 0
fld public final static int YYEOF = -1
fld public final static int YYINITIAL = 0
meth public final boolean yyatEOF()
meth public final char yycharat(int)
meth public final int yychar()
meth public final int yylength()
meth public final int yystate()
meth public final java.lang.String yytext()
meth public final void getText(org.apache.lucene.analysis.tokenattributes.CharTermAttribute)
meth public final void setBufferSize(int)
meth public final void yybegin(int)
meth public final void yyclose() throws java.io.IOException
meth public final void yyreset(java.io.Reader)
meth public int getNextToken() throws java.io.IOException
meth public void yypushback(int)
supr java.lang.Object
hfds ZZ_ACTION,ZZ_ACTION_PACKED_0,ZZ_ATTRIBUTE,ZZ_ATTRIBUTE_PACKED_0,ZZ_BUFFERSIZE,ZZ_CMAP_BLOCKS,ZZ_CMAP_BLOCKS_PACKED_0,ZZ_CMAP_TOP,ZZ_CMAP_TOP_PACKED_0,ZZ_ERROR_MSG,ZZ_LEXSTATE,ZZ_NO_MATCH,ZZ_PUSHBACK_2BIG,ZZ_ROWMAP,ZZ_ROWMAP_PACKED_0,ZZ_TRANS,ZZ_TRANS_PACKED_0,ZZ_UNKNOWN_ERROR,yychar,yycolumn,yyline,zzAtBOL,zzAtEOF,zzBuffer,zzCurrentPos,zzEOFDone,zzEndRead,zzFinalHighSurrogate,zzLexicalState,zzMarkedPos,zzReader,zzStartRead,zzState

CLSS public abstract interface org.apache.lucene.analysis.tokenattributes.BytesTermAttribute
intf org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute
meth public abstract void setBytesRef(org.apache.lucene.util.BytesRef)

CLSS public org.apache.lucene.analysis.tokenattributes.BytesTermAttributeImpl
cons public init()
intf org.apache.lucene.analysis.tokenattributes.BytesTermAttribute
intf org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public org.apache.lucene.util.AttributeImpl clone()
meth public org.apache.lucene.util.BytesRef getBytesRef()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public void setBytesRef(org.apache.lucene.util.BytesRef)
supr org.apache.lucene.util.AttributeImpl
hfds bytes

CLSS public abstract interface org.apache.lucene.analysis.tokenattributes.CharTermAttribute
intf java.lang.Appendable
intf java.lang.CharSequence
intf org.apache.lucene.util.Attribute
meth public abstract char[] buffer()
meth public abstract char[] resizeBuffer(int)
meth public abstract org.apache.lucene.analysis.tokenattributes.CharTermAttribute append(char)
meth public abstract org.apache.lucene.analysis.tokenattributes.CharTermAttribute append(java.lang.CharSequence)
meth public abstract org.apache.lucene.analysis.tokenattributes.CharTermAttribute append(java.lang.CharSequence,int,int)
meth public abstract org.apache.lucene.analysis.tokenattributes.CharTermAttribute append(java.lang.String)
meth public abstract org.apache.lucene.analysis.tokenattributes.CharTermAttribute append(java.lang.StringBuilder)
meth public abstract org.apache.lucene.analysis.tokenattributes.CharTermAttribute append(org.apache.lucene.analysis.tokenattributes.CharTermAttribute)
meth public abstract org.apache.lucene.analysis.tokenattributes.CharTermAttribute setEmpty()
meth public abstract org.apache.lucene.analysis.tokenattributes.CharTermAttribute setLength(int)
meth public abstract void copyBuffer(char[],int,int)

CLSS public org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl
cons public init()
fld protected org.apache.lucene.util.BytesRefBuilder builder
intf org.apache.lucene.analysis.tokenattributes.CharTermAttribute
intf org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute
meth public boolean equals(java.lang.Object)
meth public final char charAt(int)
meth public final char[] buffer()
meth public final char[] resizeBuffer(int)
meth public final int length()
meth public final java.lang.CharSequence subSequence(int,int)
meth public final org.apache.lucene.analysis.tokenattributes.CharTermAttribute append(char)
meth public final org.apache.lucene.analysis.tokenattributes.CharTermAttribute append(java.lang.CharSequence)
meth public final org.apache.lucene.analysis.tokenattributes.CharTermAttribute append(java.lang.CharSequence,int,int)
meth public final org.apache.lucene.analysis.tokenattributes.CharTermAttribute append(java.lang.String)
meth public final org.apache.lucene.analysis.tokenattributes.CharTermAttribute append(java.lang.StringBuilder)
meth public final org.apache.lucene.analysis.tokenattributes.CharTermAttribute append(org.apache.lucene.analysis.tokenattributes.CharTermAttribute)
meth public final org.apache.lucene.analysis.tokenattributes.CharTermAttribute setEmpty()
meth public final org.apache.lucene.analysis.tokenattributes.CharTermAttribute setLength(int)
meth public final void copyBuffer(char[],int,int)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl clone()
meth public org.apache.lucene.util.BytesRef getBytesRef()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
supr org.apache.lucene.util.AttributeImpl
hfds MIN_BUFFER_SIZE,termBuffer,termLength

CLSS public abstract interface org.apache.lucene.analysis.tokenattributes.FlagsAttribute
intf org.apache.lucene.util.Attribute
meth public abstract int getFlags()
meth public abstract void setFlags(int)

CLSS public org.apache.lucene.analysis.tokenattributes.FlagsAttributeImpl
cons public init()
intf org.apache.lucene.analysis.tokenattributes.FlagsAttribute
meth public boolean equals(java.lang.Object)
meth public int getFlags()
meth public int hashCode()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public void setFlags(int)
supr org.apache.lucene.util.AttributeImpl
hfds flags

CLSS public abstract interface org.apache.lucene.analysis.tokenattributes.KeywordAttribute
intf org.apache.lucene.util.Attribute
meth public abstract boolean isKeyword()
meth public abstract void setKeyword(boolean)

CLSS public final org.apache.lucene.analysis.tokenattributes.KeywordAttributeImpl
cons public init()
intf org.apache.lucene.analysis.tokenattributes.KeywordAttribute
meth public boolean equals(java.lang.Object)
meth public boolean isKeyword()
meth public int hashCode()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public void setKeyword(boolean)
supr org.apache.lucene.util.AttributeImpl
hfds keyword

CLSS public abstract interface org.apache.lucene.analysis.tokenattributes.OffsetAttribute
intf org.apache.lucene.util.Attribute
meth public abstract int endOffset()
meth public abstract int startOffset()
meth public abstract void setOffset(int,int)

CLSS public org.apache.lucene.analysis.tokenattributes.OffsetAttributeImpl
cons public init()
intf org.apache.lucene.analysis.tokenattributes.OffsetAttribute
meth public boolean equals(java.lang.Object)
meth public int endOffset()
meth public int hashCode()
meth public int startOffset()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public void setOffset(int,int)
supr org.apache.lucene.util.AttributeImpl
hfds endOffset,startOffset

CLSS public org.apache.lucene.analysis.tokenattributes.PackedTokenAttributeImpl
cons public init()
intf org.apache.lucene.analysis.tokenattributes.OffsetAttribute
intf org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute
intf org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute
intf org.apache.lucene.analysis.tokenattributes.TermFrequencyAttribute
intf org.apache.lucene.analysis.tokenattributes.TypeAttribute
meth public boolean equals(java.lang.Object)
meth public final int endOffset()
meth public final int getTermFrequency()
meth public final int startOffset()
meth public final java.lang.String type()
meth public final void setTermFrequency(int)
meth public final void setType(java.lang.String)
meth public int getPositionIncrement()
meth public int getPositionLength()
meth public int hashCode()
meth public org.apache.lucene.analysis.tokenattributes.PackedTokenAttributeImpl clone()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void end()
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public void setOffset(int,int)
meth public void setPositionIncrement(int)
meth public void setPositionLength(int)
supr org.apache.lucene.analysis.tokenattributes.CharTermAttributeImpl
hfds endOffset,positionIncrement,positionLength,startOffset,termFrequency,type

CLSS public abstract interface org.apache.lucene.analysis.tokenattributes.PayloadAttribute
intf org.apache.lucene.util.Attribute
meth public abstract org.apache.lucene.util.BytesRef getPayload()
meth public abstract void setPayload(org.apache.lucene.util.BytesRef)

CLSS public org.apache.lucene.analysis.tokenattributes.PayloadAttributeImpl
cons public init()
cons public init(org.apache.lucene.util.BytesRef)
intf org.apache.lucene.analysis.tokenattributes.PayloadAttribute
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public org.apache.lucene.analysis.tokenattributes.PayloadAttributeImpl clone()
meth public org.apache.lucene.util.BytesRef getPayload()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public void setPayload(org.apache.lucene.util.BytesRef)
supr org.apache.lucene.util.AttributeImpl
hfds payload

CLSS public abstract interface org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute
intf org.apache.lucene.util.Attribute
meth public abstract int getPositionIncrement()
meth public abstract void setPositionIncrement(int)

CLSS public org.apache.lucene.analysis.tokenattributes.PositionIncrementAttributeImpl
cons public init()
intf org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute
meth public boolean equals(java.lang.Object)
meth public int getPositionIncrement()
meth public int hashCode()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void end()
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public void setPositionIncrement(int)
supr org.apache.lucene.util.AttributeImpl
hfds positionIncrement

CLSS public abstract interface org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute
intf org.apache.lucene.util.Attribute
meth public abstract int getPositionLength()
meth public abstract void setPositionLength(int)

CLSS public org.apache.lucene.analysis.tokenattributes.PositionLengthAttributeImpl
cons public init()
intf org.apache.lucene.analysis.tokenattributes.PositionLengthAttribute
meth public boolean equals(java.lang.Object)
meth public int getPositionLength()
meth public int hashCode()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public void setPositionLength(int)
supr org.apache.lucene.util.AttributeImpl
hfds positionLength

CLSS public abstract interface org.apache.lucene.analysis.tokenattributes.SentenceAttribute
intf org.apache.lucene.util.Attribute
meth public abstract int getSentenceIndex()
meth public abstract void setSentenceIndex(int)

CLSS public org.apache.lucene.analysis.tokenattributes.SentenceAttributeImpl
cons public init()
intf org.apache.lucene.analysis.tokenattributes.SentenceAttribute
meth public boolean equals(java.lang.Object)
meth public int getSentenceIndex()
meth public int hashCode()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public void setSentenceIndex(int)
supr org.apache.lucene.util.AttributeImpl
hfds index

CLSS public abstract interface org.apache.lucene.analysis.tokenattributes.TermFrequencyAttribute
intf org.apache.lucene.util.Attribute
meth public abstract int getTermFrequency()
meth public abstract void setTermFrequency(int)

CLSS public org.apache.lucene.analysis.tokenattributes.TermFrequencyAttributeImpl
cons public init()
intf org.apache.lucene.analysis.tokenattributes.TermFrequencyAttribute
meth public boolean equals(java.lang.Object)
meth public int getTermFrequency()
meth public int hashCode()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void end()
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public void setTermFrequency(int)
supr org.apache.lucene.util.AttributeImpl
hfds termFrequency

CLSS public abstract interface org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute
intf org.apache.lucene.util.Attribute
meth public abstract org.apache.lucene.util.BytesRef getBytesRef()

CLSS public abstract interface org.apache.lucene.analysis.tokenattributes.TypeAttribute
fld public final static java.lang.String DEFAULT_TYPE = "word"
intf org.apache.lucene.util.Attribute
meth public abstract java.lang.String type()
meth public abstract void setType(java.lang.String)

CLSS public org.apache.lucene.analysis.tokenattributes.TypeAttributeImpl
cons public init()
cons public init(java.lang.String)
intf org.apache.lucene.analysis.tokenattributes.TypeAttribute
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String type()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public void setType(java.lang.String)
supr org.apache.lucene.util.AttributeImpl
hfds type

CLSS public abstract org.apache.lucene.codecs.DocValuesProducer
cons protected init()
intf java.io.Closeable
meth public abstract org.apache.lucene.index.BinaryDocValues getBinary(org.apache.lucene.index.FieldInfo) throws java.io.IOException
meth public abstract org.apache.lucene.index.NumericDocValues getNumeric(org.apache.lucene.index.FieldInfo) throws java.io.IOException
meth public abstract org.apache.lucene.index.SortedDocValues getSorted(org.apache.lucene.index.FieldInfo) throws java.io.IOException
meth public abstract org.apache.lucene.index.SortedNumericDocValues getSortedNumeric(org.apache.lucene.index.FieldInfo) throws java.io.IOException
meth public abstract org.apache.lucene.index.SortedSetDocValues getSortedSet(org.apache.lucene.index.FieldInfo) throws java.io.IOException
meth public abstract void checkIntegrity() throws java.io.IOException
meth public org.apache.lucene.codecs.DocValuesProducer getMergeInstance()
supr java.lang.Object

CLSS public org.apache.lucene.document.BinaryDocValuesField
cons public init(java.lang.String,org.apache.lucene.util.BytesRef)
fld public final static org.apache.lucene.document.FieldType TYPE
supr org.apache.lucene.document.Field

CLSS public final org.apache.lucene.document.BinaryPoint
cons public !varargs init(java.lang.String,byte[][])
cons public init(java.lang.String,byte[],org.apache.lucene.index.IndexableFieldType)
meth public !varargs static org.apache.lucene.search.Query newSetQuery(java.lang.String,byte[][])
meth public static org.apache.lucene.search.Query newExactQuery(java.lang.String,byte[])
meth public static org.apache.lucene.search.Query newRangeQuery(java.lang.String,byte[],byte[])
meth public static org.apache.lucene.search.Query newRangeQuery(java.lang.String,byte[][],byte[][])
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.BinaryRangeDocValues
cons public init(org.apache.lucene.index.BinaryDocValues,int,int)
meth public boolean advanceExact(int) throws java.io.IOException
meth public byte[] getPackedValue()
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int nextDoc() throws java.io.IOException
meth public long cost()
meth public org.apache.lucene.util.BytesRef binaryValue() throws java.io.IOException
supr org.apache.lucene.index.BinaryDocValues
hfds docID,in,numBytesPerDimension,numDims,packedValue

CLSS public org.apache.lucene.document.DateTools
innr public final static !enum Resolution
meth public static java.lang.String dateToString(java.util.Date,org.apache.lucene.document.DateTools$Resolution)
meth public static java.lang.String timeToString(long,org.apache.lucene.document.DateTools$Resolution)
meth public static java.util.Date round(java.util.Date,org.apache.lucene.document.DateTools$Resolution)
meth public static java.util.Date stringToDate(java.lang.String) throws java.text.ParseException
meth public static long round(long,org.apache.lucene.document.DateTools$Resolution)
meth public static long stringToTime(java.lang.String) throws java.text.ParseException
supr java.lang.Object
hfds GMT,TL_CAL,TL_FORMATS

CLSS public final static !enum org.apache.lucene.document.DateTools$Resolution
 outer org.apache.lucene.document.DateTools
fld public final static org.apache.lucene.document.DateTools$Resolution DAY
fld public final static org.apache.lucene.document.DateTools$Resolution HOUR
fld public final static org.apache.lucene.document.DateTools$Resolution MILLISECOND
fld public final static org.apache.lucene.document.DateTools$Resolution MINUTE
fld public final static org.apache.lucene.document.DateTools$Resolution MONTH
fld public final static org.apache.lucene.document.DateTools$Resolution SECOND
fld public final static org.apache.lucene.document.DateTools$Resolution YEAR
meth public java.lang.String toString()
meth public static org.apache.lucene.document.DateTools$Resolution valueOf(java.lang.String)
meth public static org.apache.lucene.document.DateTools$Resolution[] values()
supr java.lang.Enum<org.apache.lucene.document.DateTools$Resolution>
hfds format,formatLen

CLSS public final org.apache.lucene.document.Document
cons public init()
intf java.lang.Iterable<org.apache.lucene.index.IndexableField>
meth public final java.lang.String get(java.lang.String)
meth public final java.lang.String toString()
meth public final java.lang.String[] getValues(java.lang.String)
meth public final java.util.List<org.apache.lucene.index.IndexableField> getFields()
meth public final org.apache.lucene.index.IndexableField getField(java.lang.String)
meth public final org.apache.lucene.util.BytesRef getBinaryValue(java.lang.String)
meth public final org.apache.lucene.util.BytesRef[] getBinaryValues(java.lang.String)
meth public final void add(org.apache.lucene.index.IndexableField)
meth public final void removeField(java.lang.String)
meth public final void removeFields(java.lang.String)
meth public java.util.Iterator<org.apache.lucene.index.IndexableField> iterator()
meth public org.apache.lucene.index.IndexableField[] getFields(java.lang.String)
meth public void clear()
supr java.lang.Object
hfds NO_STRINGS,fields

CLSS public org.apache.lucene.document.DocumentStoredFieldVisitor
cons public !varargs init(java.lang.String[])
cons public init()
cons public init(java.util.Set<java.lang.String>)
meth public org.apache.lucene.document.Document getDocument()
meth public org.apache.lucene.index.StoredFieldVisitor$Status needsField(org.apache.lucene.index.FieldInfo) throws java.io.IOException
meth public void binaryField(org.apache.lucene.index.FieldInfo,byte[]) throws java.io.IOException
meth public void doubleField(org.apache.lucene.index.FieldInfo,double)
meth public void floatField(org.apache.lucene.index.FieldInfo,float)
meth public void intField(org.apache.lucene.index.FieldInfo,int)
meth public void longField(org.apache.lucene.index.FieldInfo,long)
meth public void stringField(org.apache.lucene.index.FieldInfo,java.lang.String) throws java.io.IOException
supr org.apache.lucene.index.StoredFieldVisitor
hfds doc,fieldsToAdd

CLSS public org.apache.lucene.document.DoubleDocValuesField
cons public init(java.lang.String,double)
meth public void setDoubleValue(double)
meth public void setLongValue(long)
supr org.apache.lucene.document.NumericDocValuesField

CLSS public final org.apache.lucene.document.DoubleField
cons public init(java.lang.String,double)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(java.lang.String,double,org.apache.lucene.document.Field$Store)
meth public !varargs static org.apache.lucene.search.Query newSetQuery(java.lang.String,double[])
meth public java.lang.String toString()
meth public org.apache.lucene.document.StoredValue storedValue()
meth public org.apache.lucene.util.BytesRef binaryValue()
meth public static org.apache.lucene.search.Query newExactQuery(java.lang.String,double)
meth public static org.apache.lucene.search.Query newRangeQuery(java.lang.String,double,double)
meth public static org.apache.lucene.search.SortField newSortField(java.lang.String,boolean,org.apache.lucene.search.SortedNumericSelector$Type)
meth public void setDoubleValue(double)
meth public void setLongValue(long)
supr org.apache.lucene.document.Field
hfds FIELD_TYPE,FIELD_TYPE_STORED,storedValue

CLSS public final org.apache.lucene.document.DoublePoint
cons public !varargs init(java.lang.String,double[])
meth public !varargs static org.apache.lucene.search.Query newSetQuery(java.lang.String,double[])
meth public !varargs static org.apache.lucene.util.BytesRef pack(double[])
meth public !varargs void setDoubleValues(double[])
meth public java.lang.Number numericValue()
meth public java.lang.String toString()
meth public static double decodeDimension(byte[],int)
meth public static double nextDown(double)
meth public static double nextUp(double)
meth public static org.apache.lucene.search.Query newExactQuery(java.lang.String,double)
meth public static org.apache.lucene.search.Query newRangeQuery(java.lang.String,double,double)
meth public static org.apache.lucene.search.Query newRangeQuery(java.lang.String,double[],double[])
meth public static org.apache.lucene.search.Query newSetQuery(java.lang.String,java.util.Collection<java.lang.Double>)
meth public static void encodeDimension(double,byte[],int)
meth public void setBytesValue(org.apache.lucene.util.BytesRef)
meth public void setDoubleValue(double)
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.DoubleRange
cons public init(java.lang.String,double[],double[])
fld public final static int BYTES = 8
meth public double getMax(int)
meth public double getMin(int)
meth public java.lang.String toString()
meth public static org.apache.lucene.search.Query newContainsQuery(java.lang.String,double[],double[])
meth public static org.apache.lucene.search.Query newCrossesQuery(java.lang.String,double[],double[])
meth public static org.apache.lucene.search.Query newIntersectsQuery(java.lang.String,double[],double[])
meth public static org.apache.lucene.search.Query newWithinQuery(java.lang.String,double[],double[])
meth public static void verifyAndEncode(double[],double[],byte[])
meth public void setRangeValues(double[],double[])
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.DoubleRangeDocValuesField
cons public init(java.lang.String,double[],double[])
meth public double getMax(int)
meth public double getMin(int)
meth public static org.apache.lucene.search.Query newSlowIntersectsQuery(java.lang.String,double[],double[])
supr org.apache.lucene.document.BinaryDocValuesField
hfds field,max,min

CLSS public final org.apache.lucene.document.FeatureField
cons public init(java.lang.String,java.lang.String,float)
meth public org.apache.lucene.analysis.TokenStream tokenStream(org.apache.lucene.analysis.Analyzer,org.apache.lucene.analysis.TokenStream)
meth public static org.apache.lucene.search.DoubleValuesSource newDoubleValues(java.lang.String,java.lang.String)
meth public static org.apache.lucene.search.Query newLinearQuery(java.lang.String,java.lang.String,float)
meth public static org.apache.lucene.search.Query newLogQuery(java.lang.String,java.lang.String,float,float)
meth public static org.apache.lucene.search.Query newSaturationQuery(java.lang.String,java.lang.String)
meth public static org.apache.lucene.search.Query newSaturationQuery(java.lang.String,java.lang.String,float,float)
meth public static org.apache.lucene.search.Query newSigmoidQuery(java.lang.String,java.lang.String,float,float,float)
meth public static org.apache.lucene.search.SortField newFeatureSort(java.lang.String,java.lang.String)
meth public void setFeatureValue(float)
supr org.apache.lucene.document.Field
hfds FIELD_TYPE,MAX_FREQ,MAX_WEIGHT,featureValue
hcls FeatureFunction,FeatureTokenStream,LinearFunction,LogFunction,SaturationFunction,SigmoidFunction

CLSS public org.apache.lucene.document.Field
cons protected init(java.lang.String,org.apache.lucene.index.IndexableFieldType)
cons public init(java.lang.String,byte[],int,int,org.apache.lucene.index.IndexableFieldType)
cons public init(java.lang.String,byte[],org.apache.lucene.index.IndexableFieldType)
cons public init(java.lang.String,java.io.Reader,org.apache.lucene.index.IndexableFieldType)
cons public init(java.lang.String,java.lang.CharSequence,org.apache.lucene.index.IndexableFieldType)
cons public init(java.lang.String,org.apache.lucene.analysis.TokenStream,org.apache.lucene.index.IndexableFieldType)
cons public init(java.lang.String,org.apache.lucene.util.BytesRef,org.apache.lucene.index.IndexableFieldType)
fld protected final java.lang.String name
fld protected final org.apache.lucene.index.IndexableFieldType type
fld protected java.lang.Object fieldsData
fld protected org.apache.lucene.analysis.TokenStream tokenStream
innr public final static !enum Store
intf org.apache.lucene.index.IndexableField
meth public java.io.Reader readerValue()
meth public java.lang.CharSequence getCharSequenceValue()
meth public java.lang.Number numericValue()
meth public java.lang.String name()
meth public java.lang.String stringValue()
meth public java.lang.String toString()
meth public org.apache.lucene.analysis.TokenStream tokenStream(org.apache.lucene.analysis.Analyzer,org.apache.lucene.analysis.TokenStream)
meth public org.apache.lucene.analysis.TokenStream tokenStreamValue()
meth public org.apache.lucene.document.InvertableType invertableType()
meth public org.apache.lucene.document.StoredValue storedValue()
meth public org.apache.lucene.index.IndexableFieldType fieldType()
meth public org.apache.lucene.util.BytesRef binaryValue()
meth public void setByteValue(byte)
meth public void setBytesValue(byte[])
meth public void setBytesValue(org.apache.lucene.util.BytesRef)
meth public void setDoubleValue(double)
meth public void setFloatValue(float)
meth public void setIntValue(int)
meth public void setLongValue(long)
meth public void setReaderValue(java.io.Reader)
meth public void setShortValue(short)
meth public void setStringValue(java.lang.String)
meth public void setTokenStream(org.apache.lucene.analysis.TokenStream)
supr java.lang.Object
hcls BinaryTokenStream,StringTokenStream

CLSS public final static !enum org.apache.lucene.document.Field$Store
 outer org.apache.lucene.document.Field
fld public final static org.apache.lucene.document.Field$Store NO
fld public final static org.apache.lucene.document.Field$Store YES
meth public static org.apache.lucene.document.Field$Store valueOf(java.lang.String)
meth public static org.apache.lucene.document.Field$Store[] values()
supr java.lang.Enum<org.apache.lucene.document.Field$Store>

CLSS public org.apache.lucene.document.FieldType
cons public init()
cons public init(org.apache.lucene.index.IndexableFieldType)
intf org.apache.lucene.index.IndexableFieldType
meth protected void checkIfFrozen()
meth public boolean equals(java.lang.Object)
meth public boolean omitNorms()
meth public boolean storeTermVectorOffsets()
meth public boolean storeTermVectorPayloads()
meth public boolean storeTermVectorPositions()
meth public boolean storeTermVectors()
meth public boolean stored()
meth public boolean tokenized()
meth public int hashCode()
meth public int pointDimensionCount()
meth public int pointIndexDimensionCount()
meth public int pointNumBytes()
meth public int vectorDimension()
meth public java.lang.String putAttribute(java.lang.String,java.lang.String)
meth public java.lang.String toString()
meth public java.util.Map<java.lang.String,java.lang.String> getAttributes()
meth public org.apache.lucene.index.DocValuesType docValuesType()
meth public org.apache.lucene.index.IndexOptions indexOptions()
meth public org.apache.lucene.index.VectorEncoding vectorEncoding()
meth public org.apache.lucene.index.VectorSimilarityFunction vectorSimilarityFunction()
meth public void freeze()
meth public void setDimensions(int,int)
meth public void setDimensions(int,int,int)
meth public void setDocValuesType(org.apache.lucene.index.DocValuesType)
meth public void setIndexOptions(org.apache.lucene.index.IndexOptions)
meth public void setOmitNorms(boolean)
meth public void setStoreTermVectorOffsets(boolean)
meth public void setStoreTermVectorPayloads(boolean)
meth public void setStoreTermVectorPositions(boolean)
meth public void setStoreTermVectors(boolean)
meth public void setStored(boolean)
meth public void setTokenized(boolean)
meth public void setVectorAttributes(int,org.apache.lucene.index.VectorEncoding,org.apache.lucene.index.VectorSimilarityFunction)
supr java.lang.Object
hfds attributes,dimensionCount,dimensionNumBytes,docValuesType,frozen,indexDimensionCount,indexOptions,omitNorms,storeTermVectorOffsets,storeTermVectorPayloads,storeTermVectorPositions,storeTermVectors,stored,tokenized,vectorDimension,vectorEncoding,vectorSimilarityFunction

CLSS public org.apache.lucene.document.FloatDocValuesField
cons public init(java.lang.String,float)
meth public void setFloatValue(float)
meth public void setLongValue(long)
supr org.apache.lucene.document.NumericDocValuesField

CLSS public final org.apache.lucene.document.FloatField
cons public init(java.lang.String,float)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(java.lang.String,float,org.apache.lucene.document.Field$Store)
meth public !varargs static org.apache.lucene.search.Query newSetQuery(java.lang.String,float[])
meth public java.lang.String toString()
meth public org.apache.lucene.document.StoredValue storedValue()
meth public org.apache.lucene.util.BytesRef binaryValue()
meth public static org.apache.lucene.search.Query newExactQuery(java.lang.String,float)
meth public static org.apache.lucene.search.Query newRangeQuery(java.lang.String,float,float)
meth public static org.apache.lucene.search.SortField newSortField(java.lang.String,boolean,org.apache.lucene.search.SortedNumericSelector$Type)
meth public void setFloatValue(float)
meth public void setLongValue(long)
supr org.apache.lucene.document.Field
hfds FIELD_TYPE,FIELD_TYPE_STORED,storedValue

CLSS public final org.apache.lucene.document.FloatPoint
cons public !varargs init(java.lang.String,float[])
meth public !varargs static org.apache.lucene.search.Query newSetQuery(java.lang.String,float[])
meth public !varargs static org.apache.lucene.util.BytesRef pack(float[])
meth public !varargs void setFloatValues(float[])
meth public java.lang.Number numericValue()
meth public java.lang.String toString()
meth public static float decodeDimension(byte[],int)
meth public static float nextDown(float)
meth public static float nextUp(float)
meth public static org.apache.lucene.search.Query newExactQuery(java.lang.String,float)
meth public static org.apache.lucene.search.Query newRangeQuery(java.lang.String,float,float)
meth public static org.apache.lucene.search.Query newRangeQuery(java.lang.String,float[],float[])
meth public static org.apache.lucene.search.Query newSetQuery(java.lang.String,java.util.Collection<java.lang.Float>)
meth public static void encodeDimension(float,byte[],int)
meth public void setBytesValue(org.apache.lucene.util.BytesRef)
meth public void setFloatValue(float)
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.FloatRange
cons public init(java.lang.String,float[],float[])
fld public final static int BYTES = 4
meth public float getMax(int)
meth public float getMin(int)
meth public java.lang.String toString()
meth public static org.apache.lucene.search.Query newContainsQuery(java.lang.String,float[],float[])
meth public static org.apache.lucene.search.Query newCrossesQuery(java.lang.String,float[],float[])
meth public static org.apache.lucene.search.Query newIntersectsQuery(java.lang.String,float[],float[])
meth public static org.apache.lucene.search.Query newWithinQuery(java.lang.String,float[],float[])
meth public void setRangeValues(float[],float[])
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.FloatRangeDocValuesField
cons public init(java.lang.String,float[],float[])
meth public float getMax(int)
meth public float getMin(int)
meth public static org.apache.lucene.search.Query newSlowIntersectsQuery(java.lang.String,float[],float[])
supr org.apache.lucene.document.BinaryDocValuesField
hfds field,max,min

CLSS public org.apache.lucene.document.InetAddressPoint
cons public init(java.lang.String,java.net.InetAddress)
fld public final static int BYTES = 16
fld public final static java.net.InetAddress MAX_VALUE
fld public final static java.net.InetAddress MIN_VALUE
meth public !varargs static org.apache.lucene.search.Query newSetQuery(java.lang.String,java.net.InetAddress[])
meth public java.lang.String toString()
meth public static byte[] encode(java.net.InetAddress)
meth public static java.net.InetAddress decode(byte[])
meth public static java.net.InetAddress nextDown(java.net.InetAddress)
meth public static java.net.InetAddress nextUp(java.net.InetAddress)
meth public static org.apache.lucene.search.Query newExactQuery(java.lang.String,java.net.InetAddress)
meth public static org.apache.lucene.search.Query newPrefixQuery(java.lang.String,java.net.InetAddress,int)
meth public static org.apache.lucene.search.Query newRangeQuery(java.lang.String,java.net.InetAddress,java.net.InetAddress)
meth public void setBytesValue(org.apache.lucene.util.BytesRef)
meth public void setInetAddressValue(java.net.InetAddress)
supr org.apache.lucene.document.Field
hfds IPV4_PREFIX,TYPE

CLSS public org.apache.lucene.document.InetAddressRange
cons public init(java.lang.String,java.net.InetAddress,java.net.InetAddress)
fld public final static int BYTES = 16
meth public static org.apache.lucene.search.Query newContainsQuery(java.lang.String,java.net.InetAddress,java.net.InetAddress)
meth public static org.apache.lucene.search.Query newCrossesQuery(java.lang.String,java.net.InetAddress,java.net.InetAddress)
meth public static org.apache.lucene.search.Query newIntersectsQuery(java.lang.String,java.net.InetAddress,java.net.InetAddress)
meth public static org.apache.lucene.search.Query newWithinQuery(java.lang.String,java.net.InetAddress,java.net.InetAddress)
meth public void setRangeValues(java.net.InetAddress,java.net.InetAddress)
supr org.apache.lucene.document.Field
hfds TYPE

CLSS public final org.apache.lucene.document.IntField
cons public init(java.lang.String,int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(java.lang.String,int,org.apache.lucene.document.Field$Store)
meth public !varargs static org.apache.lucene.search.Query newSetQuery(java.lang.String,int[])
meth public java.lang.String toString()
meth public org.apache.lucene.document.StoredValue storedValue()
meth public org.apache.lucene.util.BytesRef binaryValue()
meth public static org.apache.lucene.search.Query newExactQuery(java.lang.String,int)
meth public static org.apache.lucene.search.Query newRangeQuery(java.lang.String,int,int)
meth public static org.apache.lucene.search.SortField newSortField(java.lang.String,boolean,org.apache.lucene.search.SortedNumericSelector$Type)
meth public void setIntValue(int)
supr org.apache.lucene.document.Field
hfds FIELD_TYPE,FIELD_TYPE_STORED,storedValue

CLSS public final org.apache.lucene.document.IntPoint
cons public !varargs init(java.lang.String,int[])
meth public !varargs static org.apache.lucene.search.Query newSetQuery(java.lang.String,int[])
meth public !varargs static org.apache.lucene.util.BytesRef pack(int[])
meth public !varargs void setIntValues(int[])
meth public java.lang.Number numericValue()
meth public java.lang.String toString()
meth public static int decodeDimension(byte[],int)
meth public static org.apache.lucene.search.Query newExactQuery(java.lang.String,int)
meth public static org.apache.lucene.search.Query newRangeQuery(java.lang.String,int,int)
meth public static org.apache.lucene.search.Query newRangeQuery(java.lang.String,int[],int[])
meth public static org.apache.lucene.search.Query newSetQuery(java.lang.String,java.util.Collection<java.lang.Integer>)
meth public static void encodeDimension(int,byte[],int)
meth public void setBytesValue(org.apache.lucene.util.BytesRef)
meth public void setIntValue(int)
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.IntRange
cons public init(java.lang.String,int[],int[])
fld public final static int BYTES = 4
meth public int getMax(int)
meth public int getMin(int)
meth public java.lang.String toString()
meth public static org.apache.lucene.search.Query newContainsQuery(java.lang.String,int[],int[])
meth public static org.apache.lucene.search.Query newCrossesQuery(java.lang.String,int[],int[])
meth public static org.apache.lucene.search.Query newIntersectsQuery(java.lang.String,int[],int[])
meth public static org.apache.lucene.search.Query newWithinQuery(java.lang.String,int[],int[])
meth public void setRangeValues(int[],int[])
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.IntRangeDocValuesField
cons public init(java.lang.String,int[],int[])
meth public int getMax(int)
meth public int getMin(int)
meth public static org.apache.lucene.search.Query newSlowIntersectsQuery(java.lang.String,int[],int[])
supr org.apache.lucene.document.BinaryDocValuesField
hfds field,max,min

CLSS public final !enum org.apache.lucene.document.InvertableType
fld public final static org.apache.lucene.document.InvertableType BINARY
fld public final static org.apache.lucene.document.InvertableType TOKEN_STREAM
meth public static org.apache.lucene.document.InvertableType valueOf(java.lang.String)
meth public static org.apache.lucene.document.InvertableType[] values()
supr java.lang.Enum<org.apache.lucene.document.InvertableType>

CLSS public org.apache.lucene.document.KeywordField
cons public init(java.lang.String,java.lang.String,org.apache.lucene.document.Field$Store)
cons public init(java.lang.String,org.apache.lucene.util.BytesRef,org.apache.lucene.document.Field$Store)
meth public !varargs static org.apache.lucene.search.Query newSetQuery(java.lang.String,org.apache.lucene.util.BytesRef[])
meth public org.apache.lucene.document.InvertableType invertableType()
meth public org.apache.lucene.document.StoredValue storedValue()
meth public org.apache.lucene.util.BytesRef binaryValue()
meth public static org.apache.lucene.search.Query newExactQuery(java.lang.String,java.lang.String)
meth public static org.apache.lucene.search.Query newExactQuery(java.lang.String,org.apache.lucene.util.BytesRef)
meth public static org.apache.lucene.search.SortField newSortField(java.lang.String,boolean,org.apache.lucene.search.SortedSetSelector$Type)
meth public void setBytesValue(org.apache.lucene.util.BytesRef)
meth public void setStringValue(java.lang.String)
supr org.apache.lucene.document.Field
hfds FIELD_TYPE,FIELD_TYPE_STORED,binaryValue,storedValue

CLSS public org.apache.lucene.document.KnnByteVectorField
cons public init(java.lang.String,byte[])
cons public init(java.lang.String,byte[],org.apache.lucene.document.FieldType)
cons public init(java.lang.String,byte[],org.apache.lucene.index.VectorSimilarityFunction)
meth public byte[] vectorValue()
meth public static org.apache.lucene.document.FieldType createFieldType(int,org.apache.lucene.index.VectorSimilarityFunction)
meth public static org.apache.lucene.search.Query newVectorQuery(java.lang.String,byte[],int)
meth public void setVectorValue(byte[])
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.KnnFloatVectorField
cons public init(java.lang.String,float[])
cons public init(java.lang.String,float[],org.apache.lucene.document.FieldType)
cons public init(java.lang.String,float[],org.apache.lucene.index.VectorSimilarityFunction)
meth public float[] vectorValue()
meth public static org.apache.lucene.document.FieldType createFieldType(int,org.apache.lucene.index.VectorSimilarityFunction)
meth public static org.apache.lucene.search.Query newVectorQuery(java.lang.String,float[],int)
meth public void setVectorValue(float[])
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.KnnVectorField
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(java.lang.String,float[])
cons public init(java.lang.String,float[],org.apache.lucene.document.FieldType)
cons public init(java.lang.String,float[],org.apache.lucene.index.VectorSimilarityFunction)
supr org.apache.lucene.document.KnnFloatVectorField

CLSS public org.apache.lucene.document.LatLonDocValuesField
cons public init(java.lang.String,double,double)
fld public final static org.apache.lucene.document.FieldType TYPE
meth public !varargs static org.apache.lucene.search.Query newSlowGeometryQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,org.apache.lucene.geo.LatLonGeometry[])
meth public !varargs static org.apache.lucene.search.Query newSlowPolygonQuery(java.lang.String,org.apache.lucene.geo.Polygon[])
meth public java.lang.String toString()
meth public static org.apache.lucene.search.Query newSlowBoxQuery(java.lang.String,double,double,double,double)
meth public static org.apache.lucene.search.Query newSlowDistanceQuery(java.lang.String,double,double,double)
meth public static org.apache.lucene.search.SortField newDistanceSort(java.lang.String,double,double)
meth public void setLocationValue(double,double)
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.LatLonPoint
cons public init(java.lang.String,double,double)
fld public final static int BYTES = 4
fld public final static org.apache.lucene.document.FieldType TYPE
meth public !varargs static org.apache.lucene.search.Query newGeometryQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,org.apache.lucene.geo.LatLonGeometry[])
meth public !varargs static org.apache.lucene.search.Query newPolygonQuery(java.lang.String,org.apache.lucene.geo.Polygon[])
meth public java.lang.String toString()
meth public static org.apache.lucene.search.Query newBoxQuery(java.lang.String,double,double,double,double)
meth public static org.apache.lucene.search.Query newDistanceFeatureQuery(java.lang.String,float,double,double,double)
meth public static org.apache.lucene.search.Query newDistanceQuery(java.lang.String,double,double,double)
meth public void setLocationValue(double,double)
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.LatLonShape
meth public !varargs static org.apache.lucene.search.Query newDistanceQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,org.apache.lucene.geo.Circle[])
meth public !varargs static org.apache.lucene.search.Query newGeometryQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,org.apache.lucene.geo.LatLonGeometry[])
meth public !varargs static org.apache.lucene.search.Query newLineQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,org.apache.lucene.geo.Line[])
meth public !varargs static org.apache.lucene.search.Query newPointQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,double[][])
meth public !varargs static org.apache.lucene.search.Query newPolygonQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,org.apache.lucene.geo.Polygon[])
meth public static org.apache.lucene.document.Field[] createIndexableFields(java.lang.String,double,double)
meth public static org.apache.lucene.document.Field[] createIndexableFields(java.lang.String,org.apache.lucene.geo.Line)
meth public static org.apache.lucene.document.Field[] createIndexableFields(java.lang.String,org.apache.lucene.geo.Polygon)
meth public static org.apache.lucene.document.Field[] createIndexableFields(java.lang.String,org.apache.lucene.geo.Polygon,boolean)
meth public static org.apache.lucene.document.LatLonShapeDocValues createLatLonShapeDocValues(org.apache.lucene.util.BytesRef)
meth public static org.apache.lucene.document.LatLonShapeDocValuesField createDocValueField(java.lang.String,double,double)
meth public static org.apache.lucene.document.LatLonShapeDocValuesField createDocValueField(java.lang.String,java.util.List<org.apache.lucene.document.ShapeField$DecodedTriangle>)
meth public static org.apache.lucene.document.LatLonShapeDocValuesField createDocValueField(java.lang.String,org.apache.lucene.document.Field[])
meth public static org.apache.lucene.document.LatLonShapeDocValuesField createDocValueField(java.lang.String,org.apache.lucene.geo.Line)
meth public static org.apache.lucene.document.LatLonShapeDocValuesField createDocValueField(java.lang.String,org.apache.lucene.geo.Polygon)
meth public static org.apache.lucene.document.LatLonShapeDocValuesField createDocValueField(java.lang.String,org.apache.lucene.geo.Polygon,boolean)
meth public static org.apache.lucene.document.LatLonShapeDocValuesField createDocValueField(java.lang.String,org.apache.lucene.util.BytesRef)
meth public static org.apache.lucene.search.Query newBoxQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,double,double,double,double)
meth public static org.apache.lucene.search.Query newSlowDocValuesBoxQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,double,double,double,double)
supr java.lang.Object

CLSS public final org.apache.lucene.document.LatLonShapeDocValues
cons protected init(java.util.List<org.apache.lucene.document.ShapeField$DecodedTriangle>)
cons protected init(org.apache.lucene.util.BytesRef)
fld protected final java.lang.Object shapeComparator
fld protected final org.apache.lucene.geo.Geometry boundingBox
fld protected final org.apache.lucene.geo.Geometry centroid
fld protected final static byte VERSION = 0
innr protected abstract interface static Encoder
meth protected int getEncodedCentroidX()
meth protected int getEncodedCentroidY()
meth protected org.apache.lucene.document.ShapeDocValues$Encoder getEncoder()
meth protected org.apache.lucene.geo.Point computeCentroid()
meth protected org.apache.lucene.geo.Rectangle computeBoundingBox()
meth protected org.apache.lucene.util.BytesRef binaryValue()
meth protected static int vIntSize(int)
meth protected static int vLongSize(long)
meth public !varargs static org.apache.lucene.search.Query newGeometryQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,java.lang.Object[])
meth public int getEncodedMaxX()
meth public int getEncodedMaxY()
meth public int getEncodedMinX()
meth public int getEncodedMinY()
meth public int numberOfTerms()
meth public org.apache.lucene.document.ShapeField$DecodedTriangle$TYPE getHighestDimension()
meth public org.apache.lucene.geo.Point getCentroid()
meth public org.apache.lucene.geo.Rectangle getBoundingBox()
meth public org.apache.lucene.index.PointValues$Relation relate(org.apache.lucene.geo.Component2D) throws java.io.IOException
supr java.lang.Object

CLSS public final org.apache.lucene.document.LatLonShapeDocValuesField
cons protected init(java.lang.String,java.util.List<org.apache.lucene.document.ShapeField$DecodedTriangle>)
cons protected init(java.lang.String,org.apache.lucene.util.BytesRef)
meth protected double decodeX(int)
meth protected double decodeY(int)
meth public org.apache.lucene.geo.Point getCentroid()
meth public org.apache.lucene.geo.Rectangle getBoundingBox()
supr org.apache.lucene.document.ShapeDocValuesField

CLSS public final org.apache.lucene.document.LongField
cons public init(java.lang.String,long)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(java.lang.String,long,org.apache.lucene.document.Field$Store)
meth public !varargs static org.apache.lucene.search.Query newSetQuery(java.lang.String,long[])
meth public java.lang.String toString()
meth public org.apache.lucene.document.StoredValue storedValue()
meth public org.apache.lucene.util.BytesRef binaryValue()
meth public static org.apache.lucene.search.Query newDistanceFeatureQuery(java.lang.String,float,long,long)
meth public static org.apache.lucene.search.Query newExactQuery(java.lang.String,long)
meth public static org.apache.lucene.search.Query newRangeQuery(java.lang.String,long,long)
meth public static org.apache.lucene.search.SortField newSortField(java.lang.String,boolean,org.apache.lucene.search.SortedNumericSelector$Type)
meth public void setLongValue(long)
supr org.apache.lucene.document.Field
hfds FIELD_TYPE,FIELD_TYPE_STORED,storedValue

CLSS public final org.apache.lucene.document.LongPoint
cons public !varargs init(java.lang.String,long[])
meth public !varargs static org.apache.lucene.search.Query newSetQuery(java.lang.String,long[])
meth public !varargs static org.apache.lucene.util.BytesRef pack(long[])
meth public !varargs void setLongValues(long[])
meth public java.lang.Number numericValue()
meth public java.lang.String toString()
meth public static long decodeDimension(byte[],int)
meth public static org.apache.lucene.search.Query newDistanceFeatureQuery(java.lang.String,float,long,long)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static org.apache.lucene.search.Query newExactQuery(java.lang.String,long)
meth public static org.apache.lucene.search.Query newRangeQuery(java.lang.String,long,long)
meth public static org.apache.lucene.search.Query newRangeQuery(java.lang.String,long[],long[])
meth public static org.apache.lucene.search.Query newSetQuery(java.lang.String,java.util.Collection<java.lang.Long>)
meth public static void encodeDimension(long,byte[],int)
meth public static void unpack(org.apache.lucene.util.BytesRef,int,long[])
meth public void setBytesValue(org.apache.lucene.util.BytesRef)
meth public void setLongValue(long)
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.LongRange
cons public init(java.lang.String,long[],long[])
fld public final static int BYTES = 8
meth public java.lang.String toString()
meth public long getMax(int)
meth public long getMin(int)
meth public static org.apache.lucene.search.Query newContainsQuery(java.lang.String,long[],long[])
meth public static org.apache.lucene.search.Query newCrossesQuery(java.lang.String,long[],long[])
meth public static org.apache.lucene.search.Query newIntersectsQuery(java.lang.String,long[],long[])
meth public static org.apache.lucene.search.Query newWithinQuery(java.lang.String,long[],long[])
meth public static void verifyAndEncode(long[],long[],byte[])
meth public void setRangeValues(long[],long[])
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.LongRangeDocValuesField
cons public init(java.lang.String,long[],long[])
meth public long getMax(int)
meth public long getMin(int)
meth public static org.apache.lucene.search.Query newSlowIntersectsQuery(java.lang.String,long[],long[])
supr org.apache.lucene.document.BinaryDocValuesField
hfds field,max,min

CLSS public org.apache.lucene.document.NumericDocValuesField
cons public init(java.lang.String,java.lang.Long)
cons public init(java.lang.String,long)
fld public final static org.apache.lucene.document.FieldType TYPE
meth public !varargs static org.apache.lucene.search.Query newSlowSetQuery(java.lang.String,long[])
meth public static org.apache.lucene.search.Query newSlowExactQuery(java.lang.String,long)
meth public static org.apache.lucene.search.Query newSlowRangeQuery(java.lang.String,long,long)
supr org.apache.lucene.document.Field

CLSS public abstract org.apache.lucene.document.RangeFieldQuery
cons protected init(java.lang.String,byte[],int,org.apache.lucene.document.RangeFieldQuery$QueryType)
innr public abstract static !enum QueryType
meth protected abstract java.lang.String toString(byte[],int)
meth protected boolean equalsTo(org.apache.lucene.document.RangeFieldQuery)
meth public final boolean equals(java.lang.Object)
meth public final org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds bytesPerDim,comparator,field,numDims,queryType,ranges

CLSS public abstract static !enum org.apache.lucene.document.RangeFieldQuery$QueryType
 outer org.apache.lucene.document.RangeFieldQuery
fld public final static org.apache.lucene.document.RangeFieldQuery$QueryType CONTAINS
fld public final static org.apache.lucene.document.RangeFieldQuery$QueryType CROSSES
fld public final static org.apache.lucene.document.RangeFieldQuery$QueryType INTERSECTS
fld public final static org.apache.lucene.document.RangeFieldQuery$QueryType WITHIN
meth public boolean matches(byte[],byte[],int,int,org.apache.lucene.util.ArrayUtil$ByteArrayComparator)
meth public static org.apache.lucene.document.RangeFieldQuery$QueryType valueOf(java.lang.String)
meth public static org.apache.lucene.document.RangeFieldQuery$QueryType[] values()
supr java.lang.Enum<org.apache.lucene.document.RangeFieldQuery$QueryType>

CLSS public abstract org.apache.lucene.document.ShapeDocValuesField
fld protected final java.lang.Object shapeDocValues
fld protected final static org.apache.lucene.document.FieldType FIELD_TYPE
meth protected abstract double decodeX(int)
meth protected abstract double decodeY(int)
meth public !varargs static org.apache.lucene.search.Query newGeometryQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,java.lang.Object[])
meth public abstract org.apache.lucene.geo.Geometry getBoundingBox()
meth public abstract org.apache.lucene.geo.Geometry getCentroid()
meth public int numberOfTerms()
meth public java.lang.String name()
meth public java.lang.String stringValue()
meth public org.apache.lucene.analysis.TokenStream tokenStream(org.apache.lucene.analysis.Analyzer,org.apache.lucene.analysis.TokenStream)
meth public org.apache.lucene.document.ShapeField$DecodedTriangle$TYPE getHighestDimensionType()
meth public org.apache.lucene.index.IndexableFieldType fieldType()
supr org.apache.lucene.document.Field

CLSS public final org.apache.lucene.document.ShapeField
fld protected final static org.apache.lucene.document.FieldType TYPE
innr public final static !enum QueryRelation
innr public static DecodedTriangle
innr public static Triangle
meth public static void decodeTriangle(byte[],org.apache.lucene.document.ShapeField$DecodedTriangle)
meth public static void encodeTriangle(byte[],int,int,boolean,int,int,boolean,int,int,boolean)
supr java.lang.Object
hfds BYTES,MAXY_MINX_MINY_MAXX_Y_X,MAXY_MINX_MINY_X_Y_MAXX,MAXY_MINX_Y_X_MINY_MAXX,MINY_MINX_MAXY_MAXX_Y_X,MINY_MINX_Y_MAXX_MAXY_X,MINY_MINX_Y_X_MAXY_MAXX,Y_MINX_MINY_MAXX_MAXY_X,Y_MINX_MINY_X_MAXY_MAXX

CLSS public static org.apache.lucene.document.ShapeField$DecodedTriangle
 outer org.apache.lucene.document.ShapeField
cons public init()
fld public boolean ab
fld public boolean bc
fld public boolean ca
fld public int aX
fld public int aY
fld public int bX
fld public int bY
fld public int cX
fld public int cY
fld public org.apache.lucene.document.ShapeField$DecodedTriangle$TYPE type
innr public final static !enum TYPE
meth protected void setValues(int,int,boolean,int,int,boolean,int,int,boolean)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final static !enum org.apache.lucene.document.ShapeField$DecodedTriangle$TYPE
 outer org.apache.lucene.document.ShapeField$DecodedTriangle
fld public final static org.apache.lucene.document.ShapeField$DecodedTriangle$TYPE LINE
fld public final static org.apache.lucene.document.ShapeField$DecodedTriangle$TYPE POINT
fld public final static org.apache.lucene.document.ShapeField$DecodedTriangle$TYPE TRIANGLE
meth public static org.apache.lucene.document.ShapeField$DecodedTriangle$TYPE valueOf(java.lang.String)
meth public static org.apache.lucene.document.ShapeField$DecodedTriangle$TYPE[] values()
supr java.lang.Enum<org.apache.lucene.document.ShapeField$DecodedTriangle$TYPE>

CLSS public final static !enum org.apache.lucene.document.ShapeField$QueryRelation
 outer org.apache.lucene.document.ShapeField
fld public final static org.apache.lucene.document.ShapeField$QueryRelation CONTAINS
fld public final static org.apache.lucene.document.ShapeField$QueryRelation DISJOINT
fld public final static org.apache.lucene.document.ShapeField$QueryRelation INTERSECTS
fld public final static org.apache.lucene.document.ShapeField$QueryRelation WITHIN
meth public static org.apache.lucene.document.ShapeField$QueryRelation valueOf(java.lang.String)
meth public static org.apache.lucene.document.ShapeField$QueryRelation[] values()
supr java.lang.Enum<org.apache.lucene.document.ShapeField$QueryRelation>

CLSS public static org.apache.lucene.document.ShapeField$Triangle
 outer org.apache.lucene.document.ShapeField
meth protected void setTriangleValue(int,int,boolean,int,int,boolean,int,int,boolean)
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.SortedDocValuesField
cons public init(java.lang.String,org.apache.lucene.util.BytesRef)
fld public final static org.apache.lucene.document.FieldType TYPE
meth public !varargs static org.apache.lucene.search.Query newSlowSetQuery(java.lang.String,org.apache.lucene.util.BytesRef[])
meth public static org.apache.lucene.search.Query newSlowExactQuery(java.lang.String,org.apache.lucene.util.BytesRef)
meth public static org.apache.lucene.search.Query newSlowRangeQuery(java.lang.String,org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef,boolean,boolean)
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.SortedNumericDocValuesField
cons public init(java.lang.String,long)
fld public final static org.apache.lucene.document.FieldType TYPE
meth public !varargs static org.apache.lucene.search.Query newSlowSetQuery(java.lang.String,long[])
meth public static org.apache.lucene.search.Query newSlowExactQuery(java.lang.String,long)
meth public static org.apache.lucene.search.Query newSlowRangeQuery(java.lang.String,long,long)
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.SortedSetDocValuesField
cons public init(java.lang.String,org.apache.lucene.util.BytesRef)
fld public final static org.apache.lucene.document.FieldType TYPE
meth public !varargs static org.apache.lucene.search.Query newSlowSetQuery(java.lang.String,org.apache.lucene.util.BytesRef[])
meth public static org.apache.lucene.search.Query newSlowExactQuery(java.lang.String,org.apache.lucene.util.BytesRef)
meth public static org.apache.lucene.search.Query newSlowRangeQuery(java.lang.String,org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef,boolean,boolean)
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.StoredField
cons protected init(java.lang.String,org.apache.lucene.document.FieldType)
cons public init(java.lang.String,byte[])
cons public init(java.lang.String,byte[],int,int)
cons public init(java.lang.String,double)
cons public init(java.lang.String,float)
cons public init(java.lang.String,int)
cons public init(java.lang.String,java.lang.CharSequence,org.apache.lucene.document.FieldType)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,org.apache.lucene.document.FieldType)
cons public init(java.lang.String,long)
cons public init(java.lang.String,org.apache.lucene.util.BytesRef)
cons public init(java.lang.String,org.apache.lucene.util.BytesRef,org.apache.lucene.document.FieldType)
fld public final static org.apache.lucene.document.FieldType TYPE
supr org.apache.lucene.document.Field

CLSS public final org.apache.lucene.document.StoredValue
cons public init(double)
cons public init(float)
cons public init(int)
cons public init(java.lang.String)
cons public init(long)
cons public init(org.apache.lucene.util.BytesRef)
innr public final static !enum Type
meth public double getDoubleValue()
meth public float getFloatValue()
meth public int getIntValue()
meth public java.lang.String getStringValue()
meth public long getLongValue()
meth public org.apache.lucene.document.StoredValue$Type getType()
meth public org.apache.lucene.util.BytesRef getBinaryValue()
meth public void setBinaryValue(org.apache.lucene.util.BytesRef)
meth public void setDoubleValue(double)
meth public void setFloatValue(float)
meth public void setIntValue(int)
meth public void setLongValue(long)
meth public void setStringValue(java.lang.String)
supr java.lang.Object
hfds binaryValue,doubleValue,floatValue,intValue,longValue,stringValue,type

CLSS public final static !enum org.apache.lucene.document.StoredValue$Type
 outer org.apache.lucene.document.StoredValue
fld public final static org.apache.lucene.document.StoredValue$Type BINARY
fld public final static org.apache.lucene.document.StoredValue$Type DOUBLE
fld public final static org.apache.lucene.document.StoredValue$Type FLOAT
fld public final static org.apache.lucene.document.StoredValue$Type INTEGER
fld public final static org.apache.lucene.document.StoredValue$Type LONG
fld public final static org.apache.lucene.document.StoredValue$Type STRING
meth public static org.apache.lucene.document.StoredValue$Type valueOf(java.lang.String)
meth public static org.apache.lucene.document.StoredValue$Type[] values()
supr java.lang.Enum<org.apache.lucene.document.StoredValue$Type>

CLSS public final org.apache.lucene.document.StringField
cons public init(java.lang.String,java.lang.String,org.apache.lucene.document.Field$Store)
cons public init(java.lang.String,org.apache.lucene.util.BytesRef,org.apache.lucene.document.Field$Store)
fld public final static org.apache.lucene.document.FieldType TYPE_NOT_STORED
fld public final static org.apache.lucene.document.FieldType TYPE_STORED
meth public org.apache.lucene.document.InvertableType invertableType()
meth public org.apache.lucene.document.StoredValue storedValue()
meth public org.apache.lucene.util.BytesRef binaryValue()
meth public void setBytesValue(org.apache.lucene.util.BytesRef)
meth public void setStringValue(java.lang.String)
supr org.apache.lucene.document.Field
hfds binaryValue,storedValue

CLSS public final org.apache.lucene.document.TextField
cons public init(java.lang.String,java.io.Reader)
cons public init(java.lang.String,java.lang.String,org.apache.lucene.document.Field$Store)
cons public init(java.lang.String,org.apache.lucene.analysis.TokenStream)
fld public final static org.apache.lucene.document.FieldType TYPE_NOT_STORED
fld public final static org.apache.lucene.document.FieldType TYPE_STORED
meth public org.apache.lucene.document.StoredValue storedValue()
meth public void setStringValue(java.lang.String)
supr org.apache.lucene.document.Field
hfds storedValue

CLSS public org.apache.lucene.document.XYDocValuesField
cons public init(java.lang.String,float,float)
fld public final static org.apache.lucene.document.FieldType TYPE
meth public !varargs static org.apache.lucene.search.Query newSlowGeometryQuery(java.lang.String,org.apache.lucene.geo.XYGeometry[])
meth public !varargs static org.apache.lucene.search.Query newSlowPolygonQuery(java.lang.String,org.apache.lucene.geo.XYPolygon[])
meth public java.lang.String toString()
meth public static org.apache.lucene.search.Query newSlowBoxQuery(java.lang.String,float,float,float,float)
meth public static org.apache.lucene.search.Query newSlowDistanceQuery(java.lang.String,float,float,float)
meth public static org.apache.lucene.search.SortField newDistanceSort(java.lang.String,float,float)
meth public void setLocationValue(float,float)
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.XYDocValuesPointInGeometryQuery
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds field,geometries

CLSS public org.apache.lucene.document.XYPointField
cons public init(java.lang.String,float,float)
fld public final static int BYTES = 4
fld public final static org.apache.lucene.document.FieldType TYPE
meth public !varargs static org.apache.lucene.search.Query newGeometryQuery(java.lang.String,org.apache.lucene.geo.XYGeometry[])
meth public !varargs static org.apache.lucene.search.Query newPolygonQuery(java.lang.String,org.apache.lucene.geo.XYPolygon[])
meth public java.lang.String toString()
meth public static org.apache.lucene.search.Query newBoxQuery(java.lang.String,float,float,float,float)
meth public static org.apache.lucene.search.Query newDistanceQuery(java.lang.String,float,float,float)
meth public void setLocationValue(float,float)
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.XYShape
meth public !varargs static org.apache.lucene.search.Query newDistanceQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,org.apache.lucene.geo.XYCircle[])
meth public !varargs static org.apache.lucene.search.Query newGeometryQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,org.apache.lucene.geo.XYGeometry[])
meth public !varargs static org.apache.lucene.search.Query newLineQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,org.apache.lucene.geo.XYLine[])
meth public !varargs static org.apache.lucene.search.Query newPointQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,float[][])
meth public !varargs static org.apache.lucene.search.Query newPolygonQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,org.apache.lucene.geo.XYPolygon[])
meth public static org.apache.lucene.document.Field[] createIndexableFields(java.lang.String,float,float)
meth public static org.apache.lucene.document.Field[] createIndexableFields(java.lang.String,org.apache.lucene.geo.XYLine)
meth public static org.apache.lucene.document.Field[] createIndexableFields(java.lang.String,org.apache.lucene.geo.XYPolygon)
meth public static org.apache.lucene.document.Field[] createIndexableFields(java.lang.String,org.apache.lucene.geo.XYPolygon,boolean)
meth public static org.apache.lucene.document.XYShapeDocValues createXYShapeDocValues(org.apache.lucene.util.BytesRef)
meth public static org.apache.lucene.document.XYShapeDocValuesField createDocValueField(java.lang.String,float,float)
meth public static org.apache.lucene.document.XYShapeDocValuesField createDocValueField(java.lang.String,java.util.List<org.apache.lucene.document.ShapeField$DecodedTriangle>)
meth public static org.apache.lucene.document.XYShapeDocValuesField createDocValueField(java.lang.String,org.apache.lucene.geo.XYLine)
meth public static org.apache.lucene.document.XYShapeDocValuesField createDocValueField(java.lang.String,org.apache.lucene.geo.XYPolygon)
meth public static org.apache.lucene.document.XYShapeDocValuesField createDocValueField(java.lang.String,org.apache.lucene.geo.XYPolygon,boolean)
meth public static org.apache.lucene.document.XYShapeDocValuesField createDocValueField(java.lang.String,org.apache.lucene.util.BytesRef)
meth public static org.apache.lucene.search.Query newBoxQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,float,float,float,float)
meth public static org.apache.lucene.search.Query newSlowDocValuesBoxQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,float,float,float,float)
supr java.lang.Object

CLSS public final org.apache.lucene.document.XYShapeDocValues
cons protected init(java.util.List<org.apache.lucene.document.ShapeField$DecodedTriangle>)
cons protected init(org.apache.lucene.util.BytesRef)
fld protected final java.lang.Object shapeComparator
fld protected final org.apache.lucene.geo.Geometry boundingBox
fld protected final org.apache.lucene.geo.Geometry centroid
fld protected final static byte VERSION = 0
innr protected abstract interface static Encoder
meth protected int getEncodedCentroidX()
meth protected int getEncodedCentroidY()
meth protected org.apache.lucene.document.ShapeDocValues$Encoder getEncoder()
meth protected org.apache.lucene.geo.XYPoint computeCentroid()
meth protected org.apache.lucene.geo.XYRectangle computeBoundingBox()
meth protected org.apache.lucene.util.BytesRef binaryValue()
meth protected static int vIntSize(int)
meth protected static int vLongSize(long)
meth public !varargs static org.apache.lucene.search.Query newGeometryQuery(java.lang.String,org.apache.lucene.document.ShapeField$QueryRelation,java.lang.Object[])
meth public int getEncodedMaxX()
meth public int getEncodedMaxY()
meth public int getEncodedMinX()
meth public int getEncodedMinY()
meth public int numberOfTerms()
meth public org.apache.lucene.document.ShapeField$DecodedTriangle$TYPE getHighestDimension()
meth public org.apache.lucene.geo.XYPoint getCentroid()
meth public org.apache.lucene.geo.XYRectangle getBoundingBox()
meth public org.apache.lucene.index.PointValues$Relation relate(org.apache.lucene.geo.Component2D) throws java.io.IOException
supr java.lang.Object

CLSS public final org.apache.lucene.document.XYShapeDocValuesField
cons protected init(java.lang.String,java.util.List<org.apache.lucene.document.ShapeField$DecodedTriangle>)
cons protected init(java.lang.String,org.apache.lucene.util.BytesRef)
meth protected double decodeX(int)
meth protected double decodeY(int)
meth public org.apache.lucene.geo.XYPoint getCentroid()
meth public org.apache.lucene.geo.XYRectangle getBoundingBox()
supr org.apache.lucene.document.ShapeDocValuesField

CLSS public org.apache.lucene.index.AutomatonTermsEnum
cons public init(org.apache.lucene.index.TermsEnum,org.apache.lucene.util.automaton.CompiledAutomaton)
meth protected org.apache.lucene.index.FilteredTermsEnum$AcceptStatus accept(org.apache.lucene.util.BytesRef)
meth protected org.apache.lucene.util.BytesRef nextSeekTerm(org.apache.lucene.util.BytesRef) throws java.io.IOException
supr org.apache.lucene.index.FilteredTermsEnum
hfds automaton,commonSuffixRef,curGen,finite,linear,linearUpperBound,runAutomaton,savedStates,seekBytesRef,transition,visited

CLSS public abstract org.apache.lucene.index.BaseCompositeReader<%0 extends org.apache.lucene.index.IndexReader>
cons protected init({org.apache.lucene.index.BaseCompositeReader%0}[],java.util.Comparator<{org.apache.lucene.index.BaseCompositeReader%0}>) throws java.io.IOException
fld protected final java.util.Comparator<{org.apache.lucene.index.BaseCompositeReader%0}> subReadersSorter
meth protected final int readerBase(int)
meth protected final int readerIndex(int)
meth protected final java.util.List<? extends {org.apache.lucene.index.BaseCompositeReader%0}> getSequentialSubReaders()
meth public final int docFreq(org.apache.lucene.index.Term) throws java.io.IOException
meth public final int getDocCount(java.lang.String) throws java.io.IOException
meth public final int maxDoc()
meth public final int numDocs()
meth public final long getSumDocFreq(java.lang.String) throws java.io.IOException
meth public final long getSumTotalTermFreq(java.lang.String) throws java.io.IOException
meth public final long totalTermFreq(org.apache.lucene.index.Term) throws java.io.IOException
meth public final org.apache.lucene.index.Fields getTermVectors(int) throws java.io.IOException
meth public final org.apache.lucene.index.StoredFields storedFields() throws java.io.IOException
meth public final org.apache.lucene.index.TermVectors termVectors() throws java.io.IOException
meth public final void document(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
supr org.apache.lucene.index.CompositeReader
hfds maxDoc,numDocs,starts,subReaders,subReadersList

CLSS public abstract org.apache.lucene.index.BaseTermsEnum
cons protected init()
meth public boolean seekExact(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public org.apache.lucene.index.TermState termState() throws java.io.IOException
meth public org.apache.lucene.util.AttributeSource attributes()
meth public void seekExact(org.apache.lucene.util.BytesRef,org.apache.lucene.index.TermState) throws java.io.IOException
supr org.apache.lucene.index.TermsEnum
hfds atts

CLSS public abstract org.apache.lucene.index.BinaryDocValues
cons protected init()
meth public abstract boolean advanceExact(int) throws java.io.IOException
meth public abstract org.apache.lucene.util.BytesRef binaryValue() throws java.io.IOException
supr org.apache.lucene.search.DocIdSetIterator

CLSS public abstract org.apache.lucene.index.ByteVectorValues
cons protected init()
meth public abstract byte[] vectorValue() throws java.io.IOException
meth public abstract int dimension()
meth public abstract int size()
meth public final long cost()
supr org.apache.lucene.search.DocIdSetIterator

CLSS public final org.apache.lucene.index.CheckIndex
cons public init(org.apache.lucene.store.Directory) throws java.io.IOException
cons public init(org.apache.lucene.store.Directory,org.apache.lucene.store.Lock)
innr public static CheckIndexException
innr public static Options
innr public static Status
innr public static VerifyPointsVisitor
intf java.io.Closeable
meth public boolean doSlowChecks()
meth public boolean getChecksumsOnly()
meth public boolean getFailFast()
meth public int doCheck(org.apache.lucene.index.CheckIndex$Options) throws java.io.IOException,java.lang.InterruptedException
meth public org.apache.lucene.index.CheckIndex$Status checkIndex() throws java.io.IOException
meth public org.apache.lucene.index.CheckIndex$Status checkIndex(java.util.List<java.lang.String>) throws java.io.IOException
meth public org.apache.lucene.index.CheckIndex$Status checkIndex(java.util.List<java.lang.String>,java.util.concurrent.ExecutorService) throws java.io.IOException
meth public static boolean assertsOn()
meth public static org.apache.lucene.index.CheckIndex$Options parseOptions(java.lang.String[])
meth public static org.apache.lucene.index.CheckIndex$Status$DocValuesStatus testDocValues(org.apache.lucene.index.CodecReader,java.io.PrintStream,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$FieldInfoStatus testFieldInfos(org.apache.lucene.index.CodecReader,java.io.PrintStream,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$FieldNormStatus testFieldNorms(org.apache.lucene.index.CodecReader,java.io.PrintStream,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$IndexSortStatus testSort(org.apache.lucene.index.CodecReader,org.apache.lucene.search.Sort,java.io.PrintStream,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$LiveDocStatus testLiveDocs(org.apache.lucene.index.CodecReader,java.io.PrintStream,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$PointsStatus testPoints(org.apache.lucene.index.CodecReader,java.io.PrintStream,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$StoredFieldStatus testStoredFields(org.apache.lucene.index.CodecReader,java.io.PrintStream,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$TermIndexStatus testPostings(org.apache.lucene.index.CodecReader,java.io.PrintStream) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$TermIndexStatus testPostings(org.apache.lucene.index.CodecReader,java.io.PrintStream,boolean,boolean,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$TermVectorStatus testTermVectors(org.apache.lucene.index.CodecReader,java.io.PrintStream) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$TermVectorStatus testTermVectors(org.apache.lucene.index.CodecReader,java.io.PrintStream,boolean,boolean,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$VectorValuesStatus testVectors(org.apache.lucene.index.CodecReader,java.io.PrintStream,boolean) throws java.io.IOException
meth public static void main(java.lang.String[]) throws java.io.IOException,java.lang.InterruptedException
meth public void close() throws java.io.IOException
meth public void exorciseIndex(org.apache.lucene.index.CheckIndex$Status) throws java.io.IOException
meth public void setChecksumsOnly(boolean)
meth public void setDoSlowChecks(boolean)
meth public void setFailFast(boolean)
meth public void setInfoStream(java.io.PrintStream)
meth public void setInfoStream(java.io.PrintStream,boolean)
meth public void setThreadCount(int)
supr java.lang.Object
hfds assertsOn,checksumsOnly,closed,dir,doSlowChecks,failFast,infoStream,nf,threadCount,verbose,writeLock
hcls ConstantRelationIntersectVisitor,DocValuesIteratorSupplier

CLSS public static org.apache.lucene.index.CheckIndex$CheckIndexException
 outer org.apache.lucene.index.CheckIndex
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public static org.apache.lucene.index.CheckIndex$Options
 outer org.apache.lucene.index.CheckIndex
cons public init()
meth public java.lang.String getDirImpl()
meth public java.lang.String getIndexPath()
meth public void setOut(java.io.PrintStream)
supr java.lang.Object
hfds dirImpl,doChecksumsOnly,doExorcise,doSlowChecks,indexPath,onlySegments,out,threadCount,verbose

CLSS public static org.apache.lucene.index.CheckIndex$Status
 outer org.apache.lucene.index.CheckIndex
fld public boolean clean
fld public boolean missingSegments
fld public boolean partial
fld public boolean toolOutOfDate
fld public boolean validCounter
fld public int numBadSegments
fld public int numSegments
fld public int totLoseDocCount
fld public java.lang.String segmentsFileName
fld public java.util.List<java.lang.String> segmentsChecked
fld public java.util.List<org.apache.lucene.index.CheckIndex$Status$SegmentInfoStatus> segmentInfos
fld public java.util.Map<java.lang.String,java.lang.String> userData
fld public long maxSegmentName
fld public org.apache.lucene.store.Directory dir
innr public final static DocValuesStatus
innr public final static FieldInfoStatus
innr public final static FieldNormStatus
innr public final static IndexSortStatus
innr public final static LiveDocStatus
innr public final static PointsStatus
innr public final static SoftDeletsStatus
innr public final static StoredFieldStatus
innr public final static TermIndexStatus
innr public final static TermVectorStatus
innr public final static VectorValuesStatus
innr public static SegmentInfoStatus
supr java.lang.Object
hfds newSegments

CLSS public final static org.apache.lucene.index.CheckIndex$Status$DocValuesStatus
 outer org.apache.lucene.index.CheckIndex$Status
fld public java.lang.Throwable error
fld public long totalBinaryFields
fld public long totalNumericFields
fld public long totalSortedFields
fld public long totalSortedNumericFields
fld public long totalSortedSetFields
fld public long totalValueFields
supr java.lang.Object

CLSS public final static org.apache.lucene.index.CheckIndex$Status$FieldInfoStatus
 outer org.apache.lucene.index.CheckIndex$Status
fld public java.lang.Throwable error
fld public long totFields
supr java.lang.Object

CLSS public final static org.apache.lucene.index.CheckIndex$Status$FieldNormStatus
 outer org.apache.lucene.index.CheckIndex$Status
fld public java.lang.Throwable error
fld public long totFields
supr java.lang.Object

CLSS public final static org.apache.lucene.index.CheckIndex$Status$IndexSortStatus
 outer org.apache.lucene.index.CheckIndex$Status
fld public java.lang.Throwable error
supr java.lang.Object

CLSS public final static org.apache.lucene.index.CheckIndex$Status$LiveDocStatus
 outer org.apache.lucene.index.CheckIndex$Status
fld public int numDeleted
fld public java.lang.Throwable error
supr java.lang.Object

CLSS public final static org.apache.lucene.index.CheckIndex$Status$PointsStatus
 outer org.apache.lucene.index.CheckIndex$Status
fld public int totalValueFields
fld public java.lang.Throwable error
fld public long totalValuePoints
supr java.lang.Object

CLSS public static org.apache.lucene.index.CheckIndex$Status$SegmentInfoStatus
 outer org.apache.lucene.index.CheckIndex$Status
fld public boolean compound
fld public boolean hasDeletions
fld public boolean openReaderPassed
fld public double sizeMB
fld public int maxDoc
fld public int numFiles
fld public int toLoseDocCount
fld public java.lang.String name
fld public java.lang.Throwable error
fld public java.util.Map<java.lang.String,java.lang.String> diagnostics
fld public long deletionsGen
fld public org.apache.lucene.codecs.Codec codec
fld public org.apache.lucene.index.CheckIndex$Status$DocValuesStatus docValuesStatus
fld public org.apache.lucene.index.CheckIndex$Status$FieldInfoStatus fieldInfoStatus
fld public org.apache.lucene.index.CheckIndex$Status$FieldNormStatus fieldNormStatus
fld public org.apache.lucene.index.CheckIndex$Status$IndexSortStatus indexSortStatus
fld public org.apache.lucene.index.CheckIndex$Status$LiveDocStatus liveDocStatus
fld public org.apache.lucene.index.CheckIndex$Status$PointsStatus pointsStatus
fld public org.apache.lucene.index.CheckIndex$Status$SoftDeletsStatus softDeletesStatus
fld public org.apache.lucene.index.CheckIndex$Status$StoredFieldStatus storedFieldStatus
fld public org.apache.lucene.index.CheckIndex$Status$TermIndexStatus termIndexStatus
fld public org.apache.lucene.index.CheckIndex$Status$TermVectorStatus termVectorStatus
fld public org.apache.lucene.index.CheckIndex$Status$VectorValuesStatus vectorValuesStatus
supr java.lang.Object

CLSS public final static org.apache.lucene.index.CheckIndex$Status$SoftDeletsStatus
 outer org.apache.lucene.index.CheckIndex$Status
fld public java.lang.Throwable error
supr java.lang.Object

CLSS public final static org.apache.lucene.index.CheckIndex$Status$StoredFieldStatus
 outer org.apache.lucene.index.CheckIndex$Status
fld public int docCount
fld public java.lang.Throwable error
fld public long totFields
supr java.lang.Object

CLSS public final static org.apache.lucene.index.CheckIndex$Status$TermIndexStatus
 outer org.apache.lucene.index.CheckIndex$Status
fld public java.lang.Throwable error
fld public java.util.Map<java.lang.String,java.lang.Object> blockTreeStats
fld public long delTermCount
fld public long termCount
fld public long totFreq
fld public long totPos
supr java.lang.Object

CLSS public final static org.apache.lucene.index.CheckIndex$Status$TermVectorStatus
 outer org.apache.lucene.index.CheckIndex$Status
fld public int docCount
fld public java.lang.Throwable error
fld public long totVectors
supr java.lang.Object

CLSS public final static org.apache.lucene.index.CheckIndex$Status$VectorValuesStatus
 outer org.apache.lucene.index.CheckIndex$Status
fld public int totalKnnVectorFields
fld public java.lang.Throwable error
fld public long totalVectorValues
supr java.lang.Object

CLSS public static org.apache.lucene.index.CheckIndex$VerifyPointsVisitor
 outer org.apache.lucene.index.CheckIndex
cons public init(java.lang.String,int,org.apache.lucene.index.PointValues) throws java.io.IOException
intf org.apache.lucene.index.PointValues$IntersectVisitor
meth public long getDocCountSeen()
meth public long getPointCountSeen()
meth public org.apache.lucene.index.PointValues$Relation compare(byte[],byte[])
meth public void visit(int)
meth public void visit(int,byte[])
supr java.lang.Object
hfds bytesPerDim,comparator,docsSeen,fieldName,globalMaxPackedValue,globalMinPackedValue,lastDocID,lastMaxPackedValue,lastMinPackedValue,lastPackedValue,numDataDims,numIndexDims,packedBytesCount,packedIndexBytesCount,pointCountSeen

CLSS public abstract org.apache.lucene.index.CodecReader
cons protected init()
meth protected void doClose() throws java.io.IOException
meth public abstract org.apache.lucene.codecs.DocValuesProducer getDocValuesReader()
meth public abstract org.apache.lucene.codecs.FieldsProducer getPostingsReader()
meth public abstract org.apache.lucene.codecs.KnnVectorsReader getVectorReader()
meth public abstract org.apache.lucene.codecs.NormsProducer getNormsReader()
meth public abstract org.apache.lucene.codecs.PointsReader getPointsReader()
meth public abstract org.apache.lucene.codecs.StoredFieldsReader getFieldsReader()
meth public abstract org.apache.lucene.codecs.TermVectorsReader getTermVectorsReader()
meth public final org.apache.lucene.index.BinaryDocValues getBinaryDocValues(java.lang.String) throws java.io.IOException
meth public final org.apache.lucene.index.ByteVectorValues getByteVectorValues(java.lang.String) throws java.io.IOException
meth public final org.apache.lucene.index.FloatVectorValues getFloatVectorValues(java.lang.String) throws java.io.IOException
meth public final org.apache.lucene.index.NumericDocValues getNormValues(java.lang.String) throws java.io.IOException
meth public final org.apache.lucene.index.NumericDocValues getNumericDocValues(java.lang.String) throws java.io.IOException
meth public final org.apache.lucene.index.PointValues getPointValues(java.lang.String) throws java.io.IOException
meth public final org.apache.lucene.index.SortedDocValues getSortedDocValues(java.lang.String) throws java.io.IOException
meth public final org.apache.lucene.index.SortedNumericDocValues getSortedNumericDocValues(java.lang.String) throws java.io.IOException
meth public final org.apache.lucene.index.SortedSetDocValues getSortedSetDocValues(java.lang.String) throws java.io.IOException
meth public final org.apache.lucene.index.StoredFields storedFields() throws java.io.IOException
meth public final org.apache.lucene.index.TermVectors termVectors() throws java.io.IOException
meth public final org.apache.lucene.index.Terms terms(java.lang.String) throws java.io.IOException
meth public final void searchNearestVectors(java.lang.String,byte[],org.apache.lucene.search.KnnCollector,org.apache.lucene.util.Bits) throws java.io.IOException
meth public final void searchNearestVectors(java.lang.String,float[],org.apache.lucene.search.KnnCollector,org.apache.lucene.util.Bits) throws java.io.IOException
meth public org.apache.lucene.index.Fields getTermVectors(int) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void checkIntegrity() throws java.io.IOException
meth public void document(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
supr org.apache.lucene.index.LeafReader

CLSS public abstract org.apache.lucene.index.CompositeReader
cons protected init()
meth protected abstract java.util.List<? extends org.apache.lucene.index.IndexReader> getSequentialSubReaders()
meth public final org.apache.lucene.index.CompositeReaderContext getContext()
meth public java.lang.String toString()
supr org.apache.lucene.index.IndexReader
hfds readerContext

CLSS public final org.apache.lucene.index.CompositeReaderContext
meth public java.util.List<org.apache.lucene.index.IndexReaderContext> children()
meth public java.util.List<org.apache.lucene.index.LeafReaderContext> leaves()
meth public org.apache.lucene.index.CompositeReader reader()
supr org.apache.lucene.index.IndexReaderContext
hfds children,leaves,reader
hcls Builder

CLSS public org.apache.lucene.index.ConcurrentMergeScheduler
cons public init()
fld protected double targetMBPerSec
fld protected final java.util.List<org.apache.lucene.index.ConcurrentMergeScheduler$MergeThread> mergeThreads
fld protected int mergeThreadCount
fld public final static int AUTO_DETECT_MERGES_AND_THREADS = -1
fld public final static java.lang.String DEFAULT_CPU_CORE_COUNT_PROPERTY = "lucene.cms.override_core_count"
innr protected MergeThread
meth protected boolean maybeStall(org.apache.lucene.index.MergeScheduler$MergeSource)
meth protected org.apache.lucene.index.ConcurrentMergeScheduler$MergeThread getMergeThread(org.apache.lucene.index.MergeScheduler$MergeSource,org.apache.lucene.index.MergePolicy$OneMerge) throws java.io.IOException
meth protected void doMerge(org.apache.lucene.index.MergeScheduler$MergeSource,org.apache.lucene.index.MergePolicy$OneMerge) throws java.io.IOException
meth protected void doStall()
meth protected void handleMergeException(java.lang.Throwable)
meth protected void targetMBPerSecChanged()
meth protected void updateMergeThreads()
meth public boolean getAutoIOThrottle()
meth public double getForceMergeMBPerSec()
meth public double getIORateLimitMBPerSec()
meth public int getMaxMergeCount()
meth public int getMaxThreadCount()
meth public int mergeThreadCount()
meth public java.lang.String toString()
meth public org.apache.lucene.store.Directory wrapForMerge(org.apache.lucene.index.MergePolicy$OneMerge,org.apache.lucene.store.Directory)
meth public void close()
meth public void disableAutoIOThrottle()
meth public void enableAutoIOThrottle()
meth public void merge(org.apache.lucene.index.MergeScheduler$MergeSource,org.apache.lucene.index.MergeTrigger) throws java.io.IOException
meth public void setDefaultMaxMergesAndThreads(boolean)
meth public void setForceMergeMBPerSec(double)
meth public void setMaxMergesAndThreads(int,int)
meth public void sync()
supr org.apache.lucene.index.MergeScheduler
hfds MAX_MERGE_MB_PER_SEC,MIN_BIG_MERGE_MB,MIN_MERGE_MB_PER_SEC,START_MB_PER_SEC,doAutoIOThrottle,forceMergeMBPerSec,maxMergeCount,maxThreadCount,suppressExceptions

CLSS protected org.apache.lucene.index.ConcurrentMergeScheduler$MergeThread
 outer org.apache.lucene.index.ConcurrentMergeScheduler
cons public init(org.apache.lucene.index.ConcurrentMergeScheduler,org.apache.lucene.index.MergeScheduler$MergeSource,org.apache.lucene.index.MergePolicy$OneMerge)
intf java.lang.Comparable<org.apache.lucene.index.ConcurrentMergeScheduler$MergeThread>
meth public int compareTo(org.apache.lucene.index.ConcurrentMergeScheduler$MergeThread)
meth public void run()
supr java.lang.Thread
hfds merge,mergeSource,rateLimiter

CLSS public org.apache.lucene.index.CorruptIndexException
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,org.apache.lucene.store.DataInput)
cons public init(java.lang.String,org.apache.lucene.store.DataInput,java.lang.Throwable)
cons public init(java.lang.String,org.apache.lucene.store.DataOutput)
cons public init(java.lang.String,org.apache.lucene.store.DataOutput,java.lang.Throwable)
meth public java.lang.String getOriginalMessage()
meth public java.lang.String getResourceDescription()
supr java.io.IOException
hfds message,resourceDescription

CLSS public abstract org.apache.lucene.index.DirectoryReader
cons protected init(org.apache.lucene.store.Directory,org.apache.lucene.index.LeafReader[],java.util.Comparator<org.apache.lucene.index.LeafReader>) throws java.io.IOException
fld protected final org.apache.lucene.store.Directory directory
meth protected abstract org.apache.lucene.index.DirectoryReader doOpenIfChanged() throws java.io.IOException
meth protected abstract org.apache.lucene.index.DirectoryReader doOpenIfChanged(org.apache.lucene.index.IndexCommit) throws java.io.IOException
meth protected abstract org.apache.lucene.index.DirectoryReader doOpenIfChanged(org.apache.lucene.index.IndexWriter,boolean) throws java.io.IOException
meth public abstract boolean isCurrent() throws java.io.IOException
meth public abstract long getVersion()
meth public abstract org.apache.lucene.index.IndexCommit getIndexCommit() throws java.io.IOException
meth public final org.apache.lucene.store.Directory directory()
meth public static boolean indexExists(org.apache.lucene.store.Directory) throws java.io.IOException
meth public static java.util.List<org.apache.lucene.index.IndexCommit> listCommits(org.apache.lucene.store.Directory) throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader open(org.apache.lucene.index.IndexCommit) throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader open(org.apache.lucene.index.IndexCommit,int,java.util.Comparator<org.apache.lucene.index.LeafReader>) throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader open(org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader open(org.apache.lucene.index.IndexWriter,boolean,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader open(org.apache.lucene.store.Directory) throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader open(org.apache.lucene.store.Directory,java.util.Comparator<org.apache.lucene.index.LeafReader>) throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader openIfChanged(org.apache.lucene.index.DirectoryReader) throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader openIfChanged(org.apache.lucene.index.DirectoryReader,org.apache.lucene.index.IndexCommit) throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader openIfChanged(org.apache.lucene.index.DirectoryReader,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader openIfChanged(org.apache.lucene.index.DirectoryReader,org.apache.lucene.index.IndexWriter,boolean) throws java.io.IOException
supr org.apache.lucene.index.BaseCompositeReader<org.apache.lucene.index.LeafReader>

CLSS public abstract org.apache.lucene.index.DocIDMerger<%0 extends org.apache.lucene.index.DocIDMerger$Sub>
innr public abstract static Sub
meth public abstract void reset() throws java.io.IOException
meth public abstract {org.apache.lucene.index.DocIDMerger%0} next() throws java.io.IOException
meth public static <%0 extends org.apache.lucene.index.DocIDMerger$Sub> org.apache.lucene.index.DocIDMerger<{%%0}> of(java.util.List<{%%0}>,boolean) throws java.io.IOException
meth public static <%0 extends org.apache.lucene.index.DocIDMerger$Sub> org.apache.lucene.index.DocIDMerger<{%%0}> of(java.util.List<{%%0}>,int,boolean) throws java.io.IOException
supr java.lang.Object
hcls SequentialDocIDMerger,SortedDocIDMerger

CLSS public abstract static org.apache.lucene.index.DocIDMerger$Sub
 outer org.apache.lucene.index.DocIDMerger
cons protected init(org.apache.lucene.index.MergeState$DocMap)
fld public final org.apache.lucene.index.MergeState$DocMap docMap
fld public int mappedDocID
meth public abstract int nextDoc() throws java.io.IOException
meth public final int nextMappedDoc() throws java.io.IOException
supr java.lang.Object

CLSS public final org.apache.lucene.index.DocValues
meth public !varargs static boolean isCacheable(org.apache.lucene.index.LeafReaderContext,java.lang.String[])
meth public final static org.apache.lucene.index.BinaryDocValues emptyBinary()
meth public final static org.apache.lucene.index.NumericDocValues emptyNumeric()
meth public final static org.apache.lucene.index.SortedDocValues emptySorted()
meth public final static org.apache.lucene.index.SortedNumericDocValues emptySortedNumeric()
meth public final static org.apache.lucene.index.SortedSetDocValues emptySortedSet()
meth public static org.apache.lucene.index.BinaryDocValues getBinary(org.apache.lucene.index.LeafReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.NumericDocValues getNumeric(org.apache.lucene.index.LeafReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.NumericDocValues unwrapSingleton(org.apache.lucene.index.SortedNumericDocValues)
meth public static org.apache.lucene.index.SortedDocValues getSorted(org.apache.lucene.index.LeafReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.SortedDocValues unwrapSingleton(org.apache.lucene.index.SortedSetDocValues)
meth public static org.apache.lucene.index.SortedNumericDocValues getSortedNumeric(org.apache.lucene.index.LeafReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.SortedNumericDocValues singleton(org.apache.lucene.index.NumericDocValues)
meth public static org.apache.lucene.index.SortedSetDocValues getSortedSet(org.apache.lucene.index.LeafReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.SortedSetDocValues singleton(org.apache.lucene.index.SortedDocValues)
supr java.lang.Object

CLSS public final !enum org.apache.lucene.index.DocValuesType
fld public final static org.apache.lucene.index.DocValuesType BINARY
fld public final static org.apache.lucene.index.DocValuesType NONE
fld public final static org.apache.lucene.index.DocValuesType NUMERIC
fld public final static org.apache.lucene.index.DocValuesType SORTED
fld public final static org.apache.lucene.index.DocValuesType SORTED_NUMERIC
fld public final static org.apache.lucene.index.DocValuesType SORTED_SET
meth public static org.apache.lucene.index.DocValuesType valueOf(java.lang.String)
meth public static org.apache.lucene.index.DocValuesType[] values()
supr java.lang.Enum<org.apache.lucene.index.DocValuesType>

CLSS public final org.apache.lucene.index.DocsWithFieldSet
cons public init()
meth public int cardinality()
meth public long ramBytesUsed()
meth public org.apache.lucene.search.DocIdSetIterator iterator()
meth public void add(int)
supr org.apache.lucene.search.DocIdSet
hfds BASE_RAM_BYTES_USED,cardinality,lastDocId,set

CLSS public abstract org.apache.lucene.index.EmptyDocValuesProducer
cons protected init()
meth public org.apache.lucene.index.BinaryDocValues getBinary(org.apache.lucene.index.FieldInfo) throws java.io.IOException
meth public org.apache.lucene.index.NumericDocValues getNumeric(org.apache.lucene.index.FieldInfo) throws java.io.IOException
meth public org.apache.lucene.index.SortedDocValues getSorted(org.apache.lucene.index.FieldInfo) throws java.io.IOException
meth public org.apache.lucene.index.SortedNumericDocValues getSortedNumeric(org.apache.lucene.index.FieldInfo) throws java.io.IOException
meth public org.apache.lucene.index.SortedSetDocValues getSortedSet(org.apache.lucene.index.FieldInfo) throws java.io.IOException
meth public void checkIntegrity()
meth public void close()
supr org.apache.lucene.codecs.DocValuesProducer

CLSS public org.apache.lucene.index.ExitableDirectoryReader
cons public init(org.apache.lucene.index.DirectoryReader,org.apache.lucene.index.QueryTimeout) throws java.io.IOException
innr public static ExitableFilterAtomicReader
innr public static ExitableSubReaderWrapper
innr public static ExitableTerms
innr public static ExitableTermsEnum
innr public static ExitingReaderException
meth protected org.apache.lucene.index.DirectoryReader doWrapDirectoryReader(org.apache.lucene.index.DirectoryReader) throws java.io.IOException
meth public java.lang.String toString()
meth public org.apache.lucene.index.IndexReader$CacheHelper getReaderCacheHelper()
meth public static org.apache.lucene.index.DirectoryReader wrap(org.apache.lucene.index.DirectoryReader,org.apache.lucene.index.QueryTimeout) throws java.io.IOException
supr org.apache.lucene.index.FilterDirectoryReader
hfds queryTimeout
hcls ExitableIntersectVisitor,ExitablePointTree,ExitablePointValues

CLSS public static org.apache.lucene.index.ExitableDirectoryReader$ExitableFilterAtomicReader
 outer org.apache.lucene.index.ExitableDirectoryReader
cons public init(org.apache.lucene.index.LeafReader,org.apache.lucene.index.QueryTimeout)
meth public org.apache.lucene.index.BinaryDocValues getBinaryDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.ByteVectorValues getByteVectorValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.FloatVectorValues getFloatVectorValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.IndexReader$CacheHelper getCoreCacheHelper()
meth public org.apache.lucene.index.IndexReader$CacheHelper getReaderCacheHelper()
meth public org.apache.lucene.index.NumericDocValues getNumericDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.PointValues getPointValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedDocValues getSortedDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedNumericDocValues getSortedNumericDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedSetDocValues getSortedSetDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.Terms terms(java.lang.String) throws java.io.IOException
meth public void searchNearestVectors(java.lang.String,byte[],org.apache.lucene.search.KnnCollector,org.apache.lucene.util.Bits) throws java.io.IOException
meth public void searchNearestVectors(java.lang.String,float[],org.apache.lucene.search.KnnCollector,org.apache.lucene.util.Bits) throws java.io.IOException
supr org.apache.lucene.index.FilterLeafReader
hfds DOCS_BETWEEN_TIMEOUT_CHECK,queryTimeout
hcls ExitableByteVectorValues,ExitableFloatVectorValues

CLSS public static org.apache.lucene.index.ExitableDirectoryReader$ExitableSubReaderWrapper
 outer org.apache.lucene.index.ExitableDirectoryReader
cons public init(org.apache.lucene.index.QueryTimeout)
meth public org.apache.lucene.index.LeafReader wrap(org.apache.lucene.index.LeafReader)
supr org.apache.lucene.index.FilterDirectoryReader$SubReaderWrapper
hfds queryTimeout

CLSS public static org.apache.lucene.index.ExitableDirectoryReader$ExitableTerms
 outer org.apache.lucene.index.ExitableDirectoryReader
cons public init(org.apache.lucene.index.Terms,org.apache.lucene.index.QueryTimeout)
meth public org.apache.lucene.index.TermsEnum intersect(org.apache.lucene.util.automaton.CompiledAutomaton,org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum iterator() throws java.io.IOException
meth public org.apache.lucene.util.BytesRef getMax() throws java.io.IOException
meth public org.apache.lucene.util.BytesRef getMin() throws java.io.IOException
supr org.apache.lucene.index.FilterLeafReader$FilterTerms
hfds queryTimeout

CLSS public static org.apache.lucene.index.ExitableDirectoryReader$ExitableTermsEnum
 outer org.apache.lucene.index.ExitableDirectoryReader
cons public init(org.apache.lucene.index.TermsEnum,org.apache.lucene.index.QueryTimeout)
meth public org.apache.lucene.util.BytesRef next() throws java.io.IOException
supr org.apache.lucene.index.FilterLeafReader$FilterTermsEnum
hfds NUM_CALLS_PER_TIMEOUT_CHECK,calls,queryTimeout

CLSS public static org.apache.lucene.index.ExitableDirectoryReader$ExitingReaderException
 outer org.apache.lucene.index.ExitableDirectoryReader
cons public init(java.lang.String)
supr java.lang.RuntimeException

CLSS public final org.apache.lucene.index.FieldInfo
cons public init(java.lang.String,int,boolean,boolean,boolean,org.apache.lucene.index.IndexOptions,org.apache.lucene.index.DocValuesType,long,java.util.Map<java.lang.String,java.lang.String>,int,int,int,int,org.apache.lucene.index.VectorEncoding,org.apache.lucene.index.VectorSimilarityFunction,boolean)
fld public final int number
fld public final java.lang.String name
meth public boolean hasNorms()
meth public boolean hasPayloads()
meth public boolean hasVectorValues()
meth public boolean hasVectors()
meth public boolean isSoftDeletesField()
meth public boolean omitsNorms()
meth public int getFieldNumber()
meth public int getPointDimensionCount()
meth public int getPointIndexDimensionCount()
meth public int getPointNumBytes()
meth public int getVectorDimension()
meth public java.lang.String getAttribute(java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String putAttribute(java.lang.String,java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.String> attributes()
meth public long getDocValuesGen()
meth public org.apache.lucene.index.DocValuesType getDocValuesType()
meth public org.apache.lucene.index.IndexOptions getIndexOptions()
meth public org.apache.lucene.index.VectorEncoding getVectorEncoding()
meth public org.apache.lucene.index.VectorSimilarityFunction getVectorSimilarityFunction()
meth public void checkConsistency()
meth public void setDocValuesType(org.apache.lucene.index.DocValuesType)
meth public void setOmitsNorms()
meth public void setPointDimensions(int,int,int)
supr java.lang.Object
hfds attributes,docValuesType,dvGen,indexOptions,omitNorms,pointDimensionCount,pointIndexDimensionCount,pointNumBytes,softDeletesField,storePayloads,storeTermVector,vectorDimension,vectorEncoding,vectorSimilarityFunction

CLSS public org.apache.lucene.index.FieldInfos
cons public init(org.apache.lucene.index.FieldInfo[])
fld public final static org.apache.lucene.index.FieldInfos EMPTY
intf java.lang.Iterable<org.apache.lucene.index.FieldInfo>
meth public boolean hasDocValues()
meth public boolean hasFreq()
meth public boolean hasNorms()
meth public boolean hasOffsets()
meth public boolean hasPayloads()
meth public boolean hasPointValues()
meth public boolean hasPostings()
meth public boolean hasProx()
meth public boolean hasVectorValues()
meth public boolean hasVectors()
meth public int size()
meth public java.lang.String getSoftDeletesField()
meth public java.util.Iterator<org.apache.lucene.index.FieldInfo> iterator()
meth public org.apache.lucene.index.FieldInfo fieldInfo(int)
meth public org.apache.lucene.index.FieldInfo fieldInfo(java.lang.String)
meth public static java.util.Collection<java.lang.String> getIndexedFields(org.apache.lucene.index.IndexReader)
meth public static org.apache.lucene.index.FieldInfos getMergedFieldInfos(org.apache.lucene.index.IndexReader)
supr java.lang.Object
hfds byName,byNumber,hasDocValues,hasFreq,hasNorms,hasOffsets,hasPayloads,hasPointValues,hasPostings,hasProx,hasVectorValues,hasVectors,softDeletesField,values
hcls Builder,FieldDimensions,FieldNumbers,FieldVectorProperties

CLSS public final org.apache.lucene.index.FieldInvertState
cons public init(int,java.lang.String,org.apache.lucene.index.IndexOptions)
cons public init(int,java.lang.String,org.apache.lucene.index.IndexOptions,int,int,int,int,int,int)
meth public int getIndexCreatedVersionMajor()
meth public int getLength()
meth public int getMaxTermFrequency()
meth public int getNumOverlap()
meth public int getOffset()
meth public int getPosition()
meth public int getUniqueTermCount()
meth public java.lang.String getName()
meth public org.apache.lucene.index.IndexOptions getIndexOptions()
meth public org.apache.lucene.util.AttributeSource getAttributeSource()
meth public void setLength(int)
meth public void setNumOverlap(int)
supr java.lang.Object
hfds attributeSource,indexCreatedVersionMajor,indexOptions,lastPosition,lastStartOffset,length,maxTermFrequency,name,numOverlap,offset,offsetAttribute,payloadAttribute,posIncrAttribute,position,termAttribute,termFreqAttribute,uniqueTermCount

CLSS public abstract org.apache.lucene.index.Fields
cons protected init()
fld public final static org.apache.lucene.index.Fields[] EMPTY_ARRAY
intf java.lang.Iterable<java.lang.String>
meth public abstract int size()
meth public abstract java.util.Iterator<java.lang.String> iterator()
meth public abstract org.apache.lucene.index.Terms terms(java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.apache.lucene.index.FilterBinaryDocValues
cons protected init(org.apache.lucene.index.BinaryDocValues)
fld protected final org.apache.lucene.index.BinaryDocValues in
meth public boolean advanceExact(int) throws java.io.IOException
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int nextDoc() throws java.io.IOException
meth public long cost()
meth public org.apache.lucene.util.BytesRef binaryValue() throws java.io.IOException
supr org.apache.lucene.index.BinaryDocValues

CLSS public abstract org.apache.lucene.index.FilterCodecReader
cons public init(org.apache.lucene.index.CodecReader)
fld protected final org.apache.lucene.index.CodecReader in
meth protected void doClose() throws java.io.IOException
meth public int maxDoc()
meth public int numDocs()
meth public org.apache.lucene.codecs.DocValuesProducer getDocValuesReader()
meth public org.apache.lucene.codecs.FieldsProducer getPostingsReader()
meth public org.apache.lucene.codecs.KnnVectorsReader getVectorReader()
meth public org.apache.lucene.codecs.NormsProducer getNormsReader()
meth public org.apache.lucene.codecs.PointsReader getPointsReader()
meth public org.apache.lucene.codecs.StoredFieldsReader getFieldsReader()
meth public org.apache.lucene.codecs.TermVectorsReader getTermVectorsReader()
meth public org.apache.lucene.index.CodecReader getDelegate()
meth public org.apache.lucene.index.FieldInfos getFieldInfos()
meth public org.apache.lucene.index.Fields getTermVectors(int) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.apache.lucene.index.LeafMetaData getMetaData()
meth public org.apache.lucene.util.Bits getLiveDocs()
meth public static org.apache.lucene.index.CodecReader unwrap(org.apache.lucene.index.CodecReader)
meth public void checkIntegrity() throws java.io.IOException
meth public void document(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
supr org.apache.lucene.index.CodecReader

CLSS public abstract org.apache.lucene.index.FilterDirectoryReader
cons public init(org.apache.lucene.index.DirectoryReader,org.apache.lucene.index.FilterDirectoryReader$SubReaderWrapper) throws java.io.IOException
fld protected final org.apache.lucene.index.DirectoryReader in
innr protected static DelegatingCacheHelper
innr public abstract static SubReaderWrapper
meth protected abstract org.apache.lucene.index.DirectoryReader doWrapDirectoryReader(org.apache.lucene.index.DirectoryReader) throws java.io.IOException
meth protected final org.apache.lucene.index.DirectoryReader doOpenIfChanged() throws java.io.IOException
meth protected final org.apache.lucene.index.DirectoryReader doOpenIfChanged(org.apache.lucene.index.IndexCommit) throws java.io.IOException
meth protected final org.apache.lucene.index.DirectoryReader doOpenIfChanged(org.apache.lucene.index.IndexWriter,boolean) throws java.io.IOException
meth protected void doClose() throws java.io.IOException
meth public boolean isCurrent() throws java.io.IOException
meth public long getVersion()
meth public org.apache.lucene.index.DirectoryReader getDelegate()
meth public org.apache.lucene.index.IndexCommit getIndexCommit() throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader unwrap(org.apache.lucene.index.DirectoryReader)
supr org.apache.lucene.index.DirectoryReader

CLSS protected static org.apache.lucene.index.FilterDirectoryReader$DelegatingCacheHelper
 outer org.apache.lucene.index.FilterDirectoryReader
cons protected init(org.apache.lucene.index.IndexReader$CacheHelper)
intf org.apache.lucene.index.IndexReader$CacheHelper
meth public org.apache.lucene.index.IndexReader$CacheKey getKey()
meth public void addClosedListener(org.apache.lucene.index.IndexReader$ClosedListener)
supr java.lang.Object
hfds cacheKey,delegate

CLSS public abstract static org.apache.lucene.index.FilterDirectoryReader$SubReaderWrapper
 outer org.apache.lucene.index.FilterDirectoryReader
cons public init()
meth protected org.apache.lucene.index.LeafReader[] wrap(java.util.List<? extends org.apache.lucene.index.LeafReader>)
meth public abstract org.apache.lucene.index.LeafReader wrap(org.apache.lucene.index.LeafReader)
supr java.lang.Object

CLSS public abstract org.apache.lucene.index.FilterLeafReader
cons protected init(org.apache.lucene.index.LeafReader)
fld protected final org.apache.lucene.index.LeafReader in
innr public abstract static FilterFields
innr public abstract static FilterPostingsEnum
innr public abstract static FilterTerms
innr public abstract static FilterTermsEnum
meth protected void doClose() throws java.io.IOException
meth public int maxDoc()
meth public int numDocs()
meth public java.lang.String toString()
meth public org.apache.lucene.index.BinaryDocValues getBinaryDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.ByteVectorValues getByteVectorValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.FieldInfos getFieldInfos()
meth public org.apache.lucene.index.Fields getTermVectors(int) throws java.io.IOException
meth public org.apache.lucene.index.FloatVectorValues getFloatVectorValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.LeafMetaData getMetaData()
meth public org.apache.lucene.index.LeafReader getDelegate()
meth public org.apache.lucene.index.NumericDocValues getNormValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.NumericDocValues getNumericDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.PointValues getPointValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedDocValues getSortedDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedNumericDocValues getSortedNumericDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedSetDocValues getSortedSetDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.StoredFields storedFields() throws java.io.IOException
meth public org.apache.lucene.index.TermVectors termVectors() throws java.io.IOException
meth public org.apache.lucene.index.Terms terms(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.util.Bits getLiveDocs()
meth public static org.apache.lucene.index.LeafReader unwrap(org.apache.lucene.index.LeafReader)
meth public void checkIntegrity() throws java.io.IOException
meth public void document(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
meth public void searchNearestVectors(java.lang.String,byte[],org.apache.lucene.search.KnnCollector,org.apache.lucene.util.Bits) throws java.io.IOException
meth public void searchNearestVectors(java.lang.String,float[],org.apache.lucene.search.KnnCollector,org.apache.lucene.util.Bits) throws java.io.IOException
supr org.apache.lucene.index.LeafReader

CLSS public abstract static org.apache.lucene.index.FilterLeafReader$FilterFields
 outer org.apache.lucene.index.FilterLeafReader
cons protected init(org.apache.lucene.index.Fields)
fld protected final org.apache.lucene.index.Fields in
meth public int size()
meth public java.util.Iterator<java.lang.String> iterator()
meth public org.apache.lucene.index.Terms terms(java.lang.String) throws java.io.IOException
supr org.apache.lucene.index.Fields

CLSS public abstract static org.apache.lucene.index.FilterLeafReader$FilterPostingsEnum
 outer org.apache.lucene.index.FilterLeafReader
cons protected init(org.apache.lucene.index.PostingsEnum)
fld protected final org.apache.lucene.index.PostingsEnum in
intf org.apache.lucene.util.Unwrappable<org.apache.lucene.index.PostingsEnum>
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int endOffset() throws java.io.IOException
meth public int freq() throws java.io.IOException
meth public int nextDoc() throws java.io.IOException
meth public int nextPosition() throws java.io.IOException
meth public int startOffset() throws java.io.IOException
meth public long cost()
meth public org.apache.lucene.index.PostingsEnum unwrap()
meth public org.apache.lucene.util.BytesRef getPayload() throws java.io.IOException
supr org.apache.lucene.index.PostingsEnum

CLSS public abstract static org.apache.lucene.index.FilterLeafReader$FilterTerms
 outer org.apache.lucene.index.FilterLeafReader
cons protected init(org.apache.lucene.index.Terms)
fld protected final org.apache.lucene.index.Terms in
meth public boolean hasFreqs()
meth public boolean hasOffsets()
meth public boolean hasPayloads()
meth public boolean hasPositions()
meth public int getDocCount() throws java.io.IOException
meth public java.lang.Object getStats() throws java.io.IOException
meth public long getSumDocFreq() throws java.io.IOException
meth public long getSumTotalTermFreq() throws java.io.IOException
meth public long size() throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum iterator() throws java.io.IOException
supr org.apache.lucene.index.Terms

CLSS public abstract static org.apache.lucene.index.FilterLeafReader$FilterTermsEnum
 outer org.apache.lucene.index.FilterLeafReader
cons protected init(org.apache.lucene.index.TermsEnum)
fld protected final org.apache.lucene.index.TermsEnum in
meth public boolean seekExact(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public int docFreq() throws java.io.IOException
meth public long ord() throws java.io.IOException
meth public long totalTermFreq() throws java.io.IOException
meth public org.apache.lucene.index.ImpactsEnum impacts(int) throws java.io.IOException
meth public org.apache.lucene.index.PostingsEnum postings(org.apache.lucene.index.PostingsEnum,int) throws java.io.IOException
meth public org.apache.lucene.index.TermState termState() throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum$SeekStatus seekCeil(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public org.apache.lucene.util.AttributeSource attributes()
meth public org.apache.lucene.util.BytesRef next() throws java.io.IOException
meth public org.apache.lucene.util.BytesRef term() throws java.io.IOException
meth public void seekExact(long) throws java.io.IOException
meth public void seekExact(org.apache.lucene.util.BytesRef,org.apache.lucene.index.TermState) throws java.io.IOException
supr org.apache.lucene.index.TermsEnum

CLSS public org.apache.lucene.index.FilterMergePolicy
cons public init(org.apache.lucene.index.MergePolicy)
fld protected final org.apache.lucene.index.MergePolicy in
intf org.apache.lucene.util.Unwrappable<org.apache.lucene.index.MergePolicy>
meth protected long maxFullFlushMergeSize()
meth protected long size(org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public !varargs org.apache.lucene.index.MergePolicy$MergeSpecification findMerges(org.apache.lucene.index.CodecReader[]) throws java.io.IOException
meth public boolean keepFullyDeletedSegment(org.apache.lucene.util.IOSupplier<org.apache.lucene.index.CodecReader>) throws java.io.IOException
meth public boolean useCompoundFile(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public double getNoCFSRatio()
meth public final double getMaxCFSSegmentSizeMB()
meth public final void setMaxCFSSegmentSizeMB(double)
meth public final void setNoCFSRatio(double)
meth public int numDeletesToMerge(org.apache.lucene.index.SegmentCommitInfo,int,org.apache.lucene.util.IOSupplier<org.apache.lucene.index.CodecReader>) throws java.io.IOException
meth public java.lang.String toString()
meth public org.apache.lucene.index.MergePolicy unwrap()
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedDeletesMerges(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedMerges(org.apache.lucene.index.SegmentInfos,int,java.util.Map<org.apache.lucene.index.SegmentCommitInfo,java.lang.Boolean>,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findFullFlushMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
supr org.apache.lucene.index.MergePolicy

CLSS public abstract org.apache.lucene.index.FilterNumericDocValues
cons protected init(org.apache.lucene.index.NumericDocValues)
fld protected final org.apache.lucene.index.NumericDocValues in
meth public boolean advanceExact(int) throws java.io.IOException
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int nextDoc() throws java.io.IOException
meth public long cost()
meth public long longValue() throws java.io.IOException
supr org.apache.lucene.index.NumericDocValues

CLSS public abstract org.apache.lucene.index.FilterSortedDocValues
cons public init(org.apache.lucene.index.SortedDocValues)
fld protected final org.apache.lucene.index.SortedDocValues in
meth public boolean advanceExact(int) throws java.io.IOException
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int getValueCount()
meth public int lookupTerm(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public int nextDoc() throws java.io.IOException
meth public int ordValue() throws java.io.IOException
meth public long cost()
meth public org.apache.lucene.index.TermsEnum intersect(org.apache.lucene.util.automaton.CompiledAutomaton) throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum termsEnum() throws java.io.IOException
meth public org.apache.lucene.util.BytesRef lookupOrd(int) throws java.io.IOException
supr org.apache.lucene.index.SortedDocValues

CLSS public abstract org.apache.lucene.index.FilterSortedNumericDocValues
cons public init(org.apache.lucene.index.SortedNumericDocValues)
fld protected final org.apache.lucene.index.SortedNumericDocValues in
meth public boolean advanceExact(int) throws java.io.IOException
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int docValueCount()
meth public int nextDoc() throws java.io.IOException
meth public long cost()
meth public long nextValue() throws java.io.IOException
supr org.apache.lucene.index.SortedNumericDocValues

CLSS public org.apache.lucene.index.FilterSortedSetDocValues
cons public init(org.apache.lucene.index.SortedSetDocValues)
fld protected final org.apache.lucene.index.SortedSetDocValues in
meth public boolean advanceExact(int) throws java.io.IOException
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int docValueCount()
meth public int nextDoc() throws java.io.IOException
meth public long cost()
meth public long getValueCount()
meth public long lookupTerm(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public long nextOrd() throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum intersect(org.apache.lucene.util.automaton.CompiledAutomaton) throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum termsEnum() throws java.io.IOException
meth public org.apache.lucene.util.BytesRef lookupOrd(long) throws java.io.IOException
supr org.apache.lucene.index.SortedSetDocValues

CLSS public abstract org.apache.lucene.index.FilterVectorValues
cons protected init(org.apache.lucene.index.FloatVectorValues)
fld protected final org.apache.lucene.index.FloatVectorValues in
meth public float[] vectorValue() throws java.io.IOException
meth public int advance(int) throws java.io.IOException
meth public int dimension()
meth public int docID()
meth public int nextDoc() throws java.io.IOException
meth public int size()
supr org.apache.lucene.index.VectorValues

CLSS public abstract org.apache.lucene.index.FilteredTermsEnum
cons protected init(org.apache.lucene.index.TermsEnum)
cons protected init(org.apache.lucene.index.TermsEnum,boolean)
fld protected final org.apache.lucene.index.TermsEnum tenum
fld protected org.apache.lucene.util.BytesRef actualTerm
innr protected final static !enum AcceptStatus
meth protected abstract org.apache.lucene.index.FilteredTermsEnum$AcceptStatus accept(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth protected final void setInitialSeekTerm(org.apache.lucene.util.BytesRef)
meth protected org.apache.lucene.util.BytesRef nextSeekTerm(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public boolean seekExact(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public int docFreq() throws java.io.IOException
meth public long ord() throws java.io.IOException
meth public long totalTermFreq() throws java.io.IOException
meth public org.apache.lucene.index.ImpactsEnum impacts(int) throws java.io.IOException
meth public org.apache.lucene.index.PostingsEnum postings(org.apache.lucene.index.PostingsEnum,int) throws java.io.IOException
meth public org.apache.lucene.index.TermState termState() throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum$SeekStatus seekCeil(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public org.apache.lucene.util.AttributeSource attributes()
meth public org.apache.lucene.util.BytesRef next() throws java.io.IOException
meth public org.apache.lucene.util.BytesRef term() throws java.io.IOException
meth public void seekExact(long) throws java.io.IOException
meth public void seekExact(org.apache.lucene.util.BytesRef,org.apache.lucene.index.TermState) throws java.io.IOException
supr org.apache.lucene.index.TermsEnum
hfds doSeek,initialSeekTerm

CLSS protected final static !enum org.apache.lucene.index.FilteredTermsEnum$AcceptStatus
 outer org.apache.lucene.index.FilteredTermsEnum
fld public final static org.apache.lucene.index.FilteredTermsEnum$AcceptStatus END
fld public final static org.apache.lucene.index.FilteredTermsEnum$AcceptStatus NO
fld public final static org.apache.lucene.index.FilteredTermsEnum$AcceptStatus NO_AND_SEEK
fld public final static org.apache.lucene.index.FilteredTermsEnum$AcceptStatus YES
fld public final static org.apache.lucene.index.FilteredTermsEnum$AcceptStatus YES_AND_SEEK
meth public static org.apache.lucene.index.FilteredTermsEnum$AcceptStatus valueOf(java.lang.String)
meth public static org.apache.lucene.index.FilteredTermsEnum$AcceptStatus[] values()
supr java.lang.Enum<org.apache.lucene.index.FilteredTermsEnum$AcceptStatus>

CLSS public abstract org.apache.lucene.index.FloatVectorValues
cons protected init()
meth public abstract float[] vectorValue() throws java.io.IOException
meth public abstract int dimension()
meth public abstract int size()
meth public final long cost()
supr org.apache.lucene.search.DocIdSetIterator

CLSS public final org.apache.lucene.index.Impact
cons public init(int,long)
fld public int freq
fld public long norm
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract org.apache.lucene.index.Impacts
cons protected init()
meth public abstract int getDocIdUpTo(int)
meth public abstract int numLevels()
meth public abstract java.util.List<org.apache.lucene.index.Impact> getImpacts(int)
supr java.lang.Object

CLSS public abstract org.apache.lucene.index.ImpactsEnum
cons protected init()
intf org.apache.lucene.index.ImpactsSource
supr org.apache.lucene.index.PostingsEnum

CLSS public abstract interface org.apache.lucene.index.ImpactsSource
meth public abstract org.apache.lucene.index.Impacts getImpacts() throws java.io.IOException
meth public abstract void advanceShallow(int) throws java.io.IOException

CLSS public abstract org.apache.lucene.index.IndexCommit
cons protected init()
intf java.lang.Comparable<org.apache.lucene.index.IndexCommit>
meth public abstract boolean isDeleted()
meth public abstract int getSegmentCount()
meth public abstract java.lang.String getSegmentsFileName()
meth public abstract java.util.Collection<java.lang.String> getFileNames() throws java.io.IOException
meth public abstract java.util.Map<java.lang.String,java.lang.String> getUserData() throws java.io.IOException
meth public abstract long getGeneration()
meth public abstract org.apache.lucene.store.Directory getDirectory()
meth public abstract void delete()
meth public boolean equals(java.lang.Object)
meth public int compareTo(org.apache.lucene.index.IndexCommit)
meth public int hashCode()
supr java.lang.Object

CLSS public abstract org.apache.lucene.index.IndexDeletionPolicy
cons protected init()
meth public abstract void onCommit(java.util.List<? extends org.apache.lucene.index.IndexCommit>) throws java.io.IOException
meth public abstract void onInit(java.util.List<? extends org.apache.lucene.index.IndexCommit>) throws java.io.IOException
supr java.lang.Object

CLSS public final org.apache.lucene.index.IndexFileNames
fld public final static java.lang.String PENDING_SEGMENTS = "pending_segments"
fld public final static java.lang.String SEGMENTS = "segments"
fld public final static java.util.regex.Pattern CODEC_FILE_PATTERN
meth public static boolean matchesExtension(java.lang.String,java.lang.String)
meth public static java.lang.String fileNameFromGeneration(java.lang.String,java.lang.String,long)
meth public static java.lang.String getExtension(java.lang.String)
meth public static java.lang.String parseSegmentName(java.lang.String)
meth public static java.lang.String segmentFileName(java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String stripExtension(java.lang.String)
meth public static java.lang.String stripSegmentName(java.lang.String)
meth public static long parseGeneration(java.lang.String)
supr java.lang.Object

CLSS public org.apache.lucene.index.IndexFormatTooNewException
cons public init(java.lang.String,int,int,int)
cons public init(org.apache.lucene.store.DataInput,int,int,int)
meth public int getMaxVersion()
meth public int getMinVersion()
meth public int getVersion()
meth public java.lang.String getResourceDescription()
supr java.io.IOException
hfds maxVersion,minVersion,resourceDescription,version

CLSS public org.apache.lucene.index.IndexFormatTooOldException
cons public init(java.lang.String,int,int,int)
cons public init(java.lang.String,java.lang.String)
cons public init(org.apache.lucene.store.DataInput,int,int,int)
cons public init(org.apache.lucene.store.DataInput,java.lang.String)
meth public java.lang.Integer getMaxVersion()
meth public java.lang.Integer getMinVersion()
meth public java.lang.Integer getVersion()
meth public java.lang.String getReason()
meth public java.lang.String getResourceDescription()
supr java.io.IOException
hfds maxVersion,minVersion,reason,resourceDescription,version

CLSS public final org.apache.lucene.index.IndexNotFoundException
cons public init(java.lang.String)
supr java.io.FileNotFoundException

CLSS public final !enum org.apache.lucene.index.IndexOptions
fld public final static org.apache.lucene.index.IndexOptions DOCS
fld public final static org.apache.lucene.index.IndexOptions DOCS_AND_FREQS
fld public final static org.apache.lucene.index.IndexOptions DOCS_AND_FREQS_AND_POSITIONS
fld public final static org.apache.lucene.index.IndexOptions DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS
fld public final static org.apache.lucene.index.IndexOptions NONE
meth public static org.apache.lucene.index.IndexOptions valueOf(java.lang.String)
meth public static org.apache.lucene.index.IndexOptions[] values()
supr java.lang.Enum<org.apache.lucene.index.IndexOptions>

CLSS public abstract org.apache.lucene.index.IndexReader
innr public abstract interface static CacheHelper
innr public abstract interface static ClosedListener
innr public final static CacheKey
intf java.io.Closeable
meth protected abstract void doClose() throws java.io.IOException
meth protected final void ensureOpen()
meth protected void notifyReaderClosedListeners() throws java.io.IOException
meth public abstract int docFreq(org.apache.lucene.index.Term) throws java.io.IOException
meth public abstract int getDocCount(java.lang.String) throws java.io.IOException
meth public abstract int maxDoc()
meth public abstract int numDocs()
meth public abstract long getSumDocFreq(java.lang.String) throws java.io.IOException
meth public abstract long getSumTotalTermFreq(java.lang.String) throws java.io.IOException
meth public abstract long totalTermFreq(org.apache.lucene.index.Term) throws java.io.IOException
meth public abstract org.apache.lucene.index.Fields getTermVectors(int) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract org.apache.lucene.index.IndexReader$CacheHelper getReaderCacheHelper()
meth public abstract org.apache.lucene.index.IndexReaderContext getContext()
meth public abstract org.apache.lucene.index.StoredFields storedFields() throws java.io.IOException
meth public abstract org.apache.lucene.index.TermVectors termVectors() throws java.io.IOException
meth public abstract void document(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean hasDeletions()
meth public final boolean equals(java.lang.Object)
meth public final boolean tryIncRef()
meth public final int getRefCount()
meth public final int hashCode()
meth public final int numDeletedDocs()
meth public final java.util.List<org.apache.lucene.index.LeafReaderContext> leaves()
meth public final org.apache.lucene.document.Document document(int) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public final org.apache.lucene.document.Document document(int,java.util.Set<java.lang.String>) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public final org.apache.lucene.index.Terms getTermVector(int,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public final void close() throws java.io.IOException
meth public final void decRef() throws java.io.IOException
meth public final void incRef()
meth public final void registerParentReader(org.apache.lucene.index.IndexReader)
supr java.lang.Object
hfds closed,closedByChild,parentReaders,refCount

CLSS public abstract interface static org.apache.lucene.index.IndexReader$CacheHelper
 outer org.apache.lucene.index.IndexReader
meth public abstract org.apache.lucene.index.IndexReader$CacheKey getKey()
meth public abstract void addClosedListener(org.apache.lucene.index.IndexReader$ClosedListener)

CLSS public final static org.apache.lucene.index.IndexReader$CacheKey
 outer org.apache.lucene.index.IndexReader
supr java.lang.Object

CLSS public abstract interface static org.apache.lucene.index.IndexReader$ClosedListener
 outer org.apache.lucene.index.IndexReader
 anno 0 java.lang.FunctionalInterface()
meth public abstract void onClose(org.apache.lucene.index.IndexReader$CacheKey) throws java.io.IOException

CLSS public abstract org.apache.lucene.index.IndexReaderContext
fld public final boolean isTopLevel
fld public final int docBaseInParent
fld public final int ordInParent
fld public final org.apache.lucene.index.CompositeReaderContext parent
meth public abstract java.util.List<org.apache.lucene.index.IndexReaderContext> children()
meth public abstract java.util.List<org.apache.lucene.index.LeafReaderContext> leaves()
meth public abstract org.apache.lucene.index.IndexReader reader()
meth public java.lang.Object id()
supr java.lang.Object
hfds identity

CLSS public abstract interface org.apache.lucene.index.IndexSorter
innr public abstract interface static ComparableProvider
innr public abstract interface static DocComparator
innr public abstract interface static NumericDocValuesProvider
innr public abstract interface static SortedDocValuesProvider
innr public final static DoubleSorter
innr public final static FloatSorter
innr public final static IntSorter
innr public final static LongSorter
innr public final static StringSorter
meth public abstract java.lang.String getProviderName()
meth public abstract org.apache.lucene.index.IndexSorter$ComparableProvider[] getComparableProviders(java.util.List<? extends org.apache.lucene.index.LeafReader>) throws java.io.IOException
meth public abstract org.apache.lucene.index.IndexSorter$DocComparator getDocComparator(org.apache.lucene.index.LeafReader,int) throws java.io.IOException

CLSS public abstract interface static org.apache.lucene.index.IndexSorter$ComparableProvider
 outer org.apache.lucene.index.IndexSorter
meth public abstract long getAsComparableLong(int) throws java.io.IOException

CLSS public abstract interface static org.apache.lucene.index.IndexSorter$DocComparator
 outer org.apache.lucene.index.IndexSorter
meth public abstract int compare(int,int)

CLSS public final static org.apache.lucene.index.IndexSorter$DoubleSorter
 outer org.apache.lucene.index.IndexSorter
cons public init(java.lang.String,java.lang.Double,boolean,org.apache.lucene.index.IndexSorter$NumericDocValuesProvider)
intf org.apache.lucene.index.IndexSorter
meth public java.lang.String getProviderName()
meth public org.apache.lucene.index.IndexSorter$ComparableProvider[] getComparableProviders(java.util.List<? extends org.apache.lucene.index.LeafReader>) throws java.io.IOException
meth public org.apache.lucene.index.IndexSorter$DocComparator getDocComparator(org.apache.lucene.index.LeafReader,int) throws java.io.IOException
supr java.lang.Object
hfds missingValue,providerName,reverseMul,valuesProvider

CLSS public final static org.apache.lucene.index.IndexSorter$FloatSorter
 outer org.apache.lucene.index.IndexSorter
cons public init(java.lang.String,java.lang.Float,boolean,org.apache.lucene.index.IndexSorter$NumericDocValuesProvider)
intf org.apache.lucene.index.IndexSorter
meth public java.lang.String getProviderName()
meth public org.apache.lucene.index.IndexSorter$ComparableProvider[] getComparableProviders(java.util.List<? extends org.apache.lucene.index.LeafReader>) throws java.io.IOException
meth public org.apache.lucene.index.IndexSorter$DocComparator getDocComparator(org.apache.lucene.index.LeafReader,int) throws java.io.IOException
supr java.lang.Object
hfds missingValue,providerName,reverseMul,valuesProvider

CLSS public final static org.apache.lucene.index.IndexSorter$IntSorter
 outer org.apache.lucene.index.IndexSorter
cons public init(java.lang.String,java.lang.Integer,boolean,org.apache.lucene.index.IndexSorter$NumericDocValuesProvider)
intf org.apache.lucene.index.IndexSorter
meth public java.lang.String getProviderName()
meth public org.apache.lucene.index.IndexSorter$ComparableProvider[] getComparableProviders(java.util.List<? extends org.apache.lucene.index.LeafReader>) throws java.io.IOException
meth public org.apache.lucene.index.IndexSorter$DocComparator getDocComparator(org.apache.lucene.index.LeafReader,int) throws java.io.IOException
supr java.lang.Object
hfds missingValue,providerName,reverseMul,valuesProvider

CLSS public final static org.apache.lucene.index.IndexSorter$LongSorter
 outer org.apache.lucene.index.IndexSorter
cons public init(java.lang.String,java.lang.Long,boolean,org.apache.lucene.index.IndexSorter$NumericDocValuesProvider)
intf org.apache.lucene.index.IndexSorter
meth public java.lang.String getProviderName()
meth public org.apache.lucene.index.IndexSorter$ComparableProvider[] getComparableProviders(java.util.List<? extends org.apache.lucene.index.LeafReader>) throws java.io.IOException
meth public org.apache.lucene.index.IndexSorter$DocComparator getDocComparator(org.apache.lucene.index.LeafReader,int) throws java.io.IOException
supr java.lang.Object
hfds missingValue,providerName,reverseMul,valuesProvider

CLSS public abstract interface static org.apache.lucene.index.IndexSorter$NumericDocValuesProvider
 outer org.apache.lucene.index.IndexSorter
meth public abstract org.apache.lucene.index.NumericDocValues get(org.apache.lucene.index.LeafReader) throws java.io.IOException

CLSS public abstract interface static org.apache.lucene.index.IndexSorter$SortedDocValuesProvider
 outer org.apache.lucene.index.IndexSorter
meth public abstract org.apache.lucene.index.SortedDocValues get(org.apache.lucene.index.LeafReader) throws java.io.IOException

CLSS public final static org.apache.lucene.index.IndexSorter$StringSorter
 outer org.apache.lucene.index.IndexSorter
cons public init(java.lang.String,java.lang.Object,boolean,org.apache.lucene.index.IndexSorter$SortedDocValuesProvider)
intf org.apache.lucene.index.IndexSorter
meth public java.lang.String getProviderName()
meth public org.apache.lucene.index.IndexSorter$ComparableProvider[] getComparableProviders(java.util.List<? extends org.apache.lucene.index.LeafReader>) throws java.io.IOException
meth public org.apache.lucene.index.IndexSorter$DocComparator getDocComparator(org.apache.lucene.index.LeafReader,int) throws java.io.IOException
supr java.lang.Object
hfds missingValue,providerName,reverseMul,valuesProvider

CLSS public final org.apache.lucene.index.IndexUpgrader
cons public init(org.apache.lucene.store.Directory)
cons public init(org.apache.lucene.store.Directory,org.apache.lucene.index.IndexWriterConfig,boolean)
cons public init(org.apache.lucene.store.Directory,org.apache.lucene.util.InfoStream,boolean)
meth public static void main(java.lang.String[]) throws java.io.IOException
meth public void upgrade() throws java.io.IOException
supr java.lang.Object
hfds LOG_PREFIX,deletePriorCommits,dir,iwc

CLSS public org.apache.lucene.index.IndexWriter
cons public init(org.apache.lucene.store.Directory,org.apache.lucene.index.IndexWriterConfig) throws java.io.IOException
fld public final static int MAX_DOCS = 2147483519
fld public final static int MAX_POSITION = 2147483519
fld public final static int MAX_STORED_STRING_LENGTH
fld public final static int MAX_TERM_LENGTH = 32766
fld public final static java.lang.String SOURCE = "source"
fld public final static java.lang.String SOURCE_ADDINDEXES_READERS = "addIndexes(CodecReader...)"
fld public final static java.lang.String SOURCE_FLUSH = "flush"
fld public final static java.lang.String SOURCE_MERGE = "merge"
fld public final static java.lang.String WRITE_LOCK_NAME = "write.lock"
innr public abstract interface static IndexReaderWarmer
innr public final static DocStats
intf java.io.Closeable
intf org.apache.lucene.index.MergePolicy$MergeContext
intf org.apache.lucene.index.TwoPhaseCommit
intf org.apache.lucene.util.Accountable
meth protected boolean isEnableTestPoints()
meth protected final void ensureOpen()
meth protected final void ensureOpen(boolean)
meth protected void doAfterFlush() throws java.io.IOException
meth protected void doBeforeFlush() throws java.io.IOException
meth protected void merge(org.apache.lucene.index.MergePolicy$OneMerge) throws java.io.IOException
meth protected void mergeSuccess(org.apache.lucene.index.MergePolicy$OneMerge)
meth public !varargs long addIndexes(org.apache.lucene.index.CodecReader[]) throws java.io.IOException
meth public !varargs long addIndexes(org.apache.lucene.store.Directory[]) throws java.io.IOException
meth public !varargs long deleteDocuments(org.apache.lucene.index.Term[]) throws java.io.IOException
meth public !varargs long deleteDocuments(org.apache.lucene.search.Query[]) throws java.io.IOException
meth public !varargs long softUpdateDocument(org.apache.lucene.index.Term,java.lang.Iterable<? extends org.apache.lucene.index.IndexableField>,org.apache.lucene.document.Field[]) throws java.io.IOException
meth public !varargs long softUpdateDocuments(org.apache.lucene.index.Term,java.lang.Iterable<? extends java.lang.Iterable<? extends org.apache.lucene.index.IndexableField>>,org.apache.lucene.document.Field[]) throws java.io.IOException
meth public !varargs long tryUpdateDocValue(org.apache.lucene.index.IndexReader,int,org.apache.lucene.document.Field[]) throws java.io.IOException
meth public !varargs long updateDocValues(org.apache.lucene.index.Term,org.apache.lucene.document.Field[]) throws java.io.IOException
meth public boolean hasDeletions()
meth public boolean hasPendingMerges()
meth public boolean isOpen()
meth public final boolean flushNextBuffer() throws java.io.IOException
meth public final boolean hasUncommittedChanges()
meth public final int numDeletesToMerge(org.apache.lucene.index.SegmentCommitInfo) throws java.io.IOException
meth public final int numRamDocs()
meth public final java.lang.Iterable<java.util.Map$Entry<java.lang.String,java.lang.String>> getLiveCommitData()
meth public final long commit() throws java.io.IOException
meth public final long getFlushingBytes()
meth public final long prepareCommit() throws java.io.IOException
meth public final long ramBytesUsed()
meth public final void flush() throws java.io.IOException
meth public final void maybeMerge() throws java.io.IOException
meth public final void setLiveCommitData(java.lang.Iterable<java.util.Map$Entry<java.lang.String,java.lang.String>>)
meth public final void setLiveCommitData(java.lang.Iterable<java.util.Map$Entry<java.lang.String,java.lang.String>>,boolean)
meth public int numDeletedDocs(org.apache.lucene.index.SegmentCommitInfo)
meth public java.lang.Throwable getTragicException()
meth public java.util.Set<java.lang.String> getFieldNames()
meth public java.util.Set<org.apache.lucene.index.SegmentCommitInfo> getMergingSegments()
meth public long addDocument(java.lang.Iterable<? extends org.apache.lucene.index.IndexableField>) throws java.io.IOException
meth public long addDocuments(java.lang.Iterable<? extends java.lang.Iterable<? extends org.apache.lucene.index.IndexableField>>) throws java.io.IOException
meth public long deleteAll() throws java.io.IOException
meth public long getMaxCompletedSequenceNumber()
meth public long getPendingNumDocs()
meth public long tryDeleteDocument(org.apache.lucene.index.IndexReader,int) throws java.io.IOException
meth public long updateBinaryDocValue(org.apache.lucene.index.Term,java.lang.String,org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public long updateDocument(org.apache.lucene.index.Term,java.lang.Iterable<? extends org.apache.lucene.index.IndexableField>) throws java.io.IOException
meth public long updateDocuments(org.apache.lucene.index.Term,java.lang.Iterable<? extends java.lang.Iterable<? extends org.apache.lucene.index.IndexableField>>) throws java.io.IOException
meth public long updateDocuments(org.apache.lucene.search.Query,java.lang.Iterable<? extends java.lang.Iterable<? extends org.apache.lucene.index.IndexableField>>) throws java.io.IOException
meth public long updateNumericDocValue(org.apache.lucene.index.Term,java.lang.String,long) throws java.io.IOException
meth public org.apache.lucene.analysis.Analyzer getAnalyzer()
meth public org.apache.lucene.index.IndexWriter$DocStats getDocStats()
meth public org.apache.lucene.index.LiveIndexWriterConfig getConfig()
meth public org.apache.lucene.store.Directory getDirectory()
meth public org.apache.lucene.util.InfoStream getInfoStream()
meth public void addIndexesReaderMerge(org.apache.lucene.index.MergePolicy$OneMerge) throws java.io.IOException
meth public void advanceSegmentInfosVersion(long)
meth public void close() throws java.io.IOException
meth public void decRefDeleter(org.apache.lucene.index.SegmentInfos) throws java.io.IOException
meth public void deleteUnusedFiles() throws java.io.IOException
meth public void forceMerge(int) throws java.io.IOException
meth public void forceMerge(int,boolean) throws java.io.IOException
meth public void forceMergeDeletes() throws java.io.IOException
meth public void forceMergeDeletes(boolean) throws java.io.IOException
meth public void incRefDeleter(org.apache.lucene.index.SegmentInfos) throws java.io.IOException
meth public void onTragicEvent(java.lang.Throwable,java.lang.String)
meth public void rollback() throws java.io.IOException
supr java.lang.Object
hfds UNBOUNDED_MAX_MERGE_SEGMENTS,actualMaxDocs,addIndexesMergeSource,bufferedUpdatesStream,changeCount,closed,closing,commitLock,commitUserData,config,deleter,didMessageState,directory,directoryOrig,docWriter,enableTestPoints,eventListener,eventQueue,filesToCommit,flushCount,flushDeletesCount,flushNotifications,fullFlushLock,globalFieldNumberMap,infoStream,lastCommitChangeCount,maybeMerge,mergeExceptions,mergeFinishedGen,mergeGen,mergeMaxNumSegments,mergeScheduler,mergeSource,merges,mergingSegments,pendingCommit,pendingCommitChangeCount,pendingMerges,pendingNumDocs,pendingSeqNo,readerPool,rollbackSegments,runningAddIndexesMerges,runningMerges,segmentInfos,segmentsToMerge,softDeletesEnabled,startCommitTime,tragedy,writeDocValuesLock,writeLock
hcls AddIndexesMergeSource,DocModifier,Event,EventQueue,IndexWriterMergeSource,Merges

CLSS public final static org.apache.lucene.index.IndexWriter$DocStats
 outer org.apache.lucene.index.IndexWriter
fld public final int maxDoc
fld public final int numDocs
supr java.lang.Object

CLSS public abstract interface static org.apache.lucene.index.IndexWriter$IndexReaderWarmer
 outer org.apache.lucene.index.IndexWriter
 anno 0 java.lang.FunctionalInterface()
meth public abstract void warm(org.apache.lucene.index.LeafReader) throws java.io.IOException

CLSS public final org.apache.lucene.index.IndexWriterConfig
cons public init()
cons public init(org.apache.lucene.analysis.Analyzer)
fld public final static boolean DEFAULT_COMMIT_ON_CLOSE = true
fld public final static boolean DEFAULT_READER_POOLING = true
fld public final static boolean DEFAULT_USE_COMPOUND_FILE_SYSTEM = true
fld public final static double DEFAULT_RAM_BUFFER_SIZE_MB = 16.0
fld public final static int DEFAULT_MAX_BUFFERED_DELETE_TERMS = -1
fld public final static int DEFAULT_MAX_BUFFERED_DOCS = -1
fld public final static int DEFAULT_RAM_PER_THREAD_HARD_LIMIT_MB = 1945
fld public final static int DISABLE_AUTO_FLUSH = -1
fld public final static long DEFAULT_MAX_FULL_FLUSH_MERGE_WAIT_MILLIS = 500
innr public final static !enum OpenMode
meth public boolean getReaderPooling()
meth public double getRAMBufferSizeMB()
meth public int getMaxBufferedDocs()
meth public int getRAMPerThreadHardLimitMB()
meth public java.lang.String toString()
meth public org.apache.lucene.analysis.Analyzer getAnalyzer()
meth public org.apache.lucene.codecs.Codec getCodec()
meth public org.apache.lucene.index.IndexCommit getIndexCommit()
meth public org.apache.lucene.index.IndexDeletionPolicy getIndexDeletionPolicy()
meth public org.apache.lucene.index.IndexWriter$IndexReaderWarmer getMergedSegmentWarmer()
meth public org.apache.lucene.index.IndexWriterConfig setCheckPendingFlushUpdate(boolean)
meth public org.apache.lucene.index.IndexWriterConfig setCodec(org.apache.lucene.codecs.Codec)
meth public org.apache.lucene.index.IndexWriterConfig setCommitOnClose(boolean)
meth public org.apache.lucene.index.IndexWriterConfig setIndexCommit(org.apache.lucene.index.IndexCommit)
meth public org.apache.lucene.index.IndexWriterConfig setIndexCreatedVersionMajor(int)
meth public org.apache.lucene.index.IndexWriterConfig setIndexDeletionPolicy(org.apache.lucene.index.IndexDeletionPolicy)
meth public org.apache.lucene.index.IndexWriterConfig setIndexSort(org.apache.lucene.search.Sort)
meth public org.apache.lucene.index.IndexWriterConfig setIndexWriterEventListener(org.apache.lucene.index.IndexWriterEventListener)
meth public org.apache.lucene.index.IndexWriterConfig setInfoStream(java.io.PrintStream)
meth public org.apache.lucene.index.IndexWriterConfig setInfoStream(org.apache.lucene.util.InfoStream)
meth public org.apache.lucene.index.IndexWriterConfig setLeafSorter(java.util.Comparator<org.apache.lucene.index.LeafReader>)
meth public org.apache.lucene.index.IndexWriterConfig setMaxBufferedDocs(int)
meth public org.apache.lucene.index.IndexWriterConfig setMaxFullFlushMergeWaitMillis(long)
meth public org.apache.lucene.index.IndexWriterConfig setMergePolicy(org.apache.lucene.index.MergePolicy)
meth public org.apache.lucene.index.IndexWriterConfig setMergeScheduler(org.apache.lucene.index.MergeScheduler)
meth public org.apache.lucene.index.IndexWriterConfig setMergedSegmentWarmer(org.apache.lucene.index.IndexWriter$IndexReaderWarmer)
meth public org.apache.lucene.index.IndexWriterConfig setOpenMode(org.apache.lucene.index.IndexWriterConfig$OpenMode)
meth public org.apache.lucene.index.IndexWriterConfig setRAMBufferSizeMB(double)
meth public org.apache.lucene.index.IndexWriterConfig setRAMPerThreadHardLimitMB(int)
meth public org.apache.lucene.index.IndexWriterConfig setReaderPooling(boolean)
meth public org.apache.lucene.index.IndexWriterConfig setSimilarity(org.apache.lucene.search.similarities.Similarity)
meth public org.apache.lucene.index.IndexWriterConfig setSoftDeletesField(java.lang.String)
meth public org.apache.lucene.index.IndexWriterConfig setUseCompoundFile(boolean)
meth public org.apache.lucene.index.IndexWriterConfig$OpenMode getOpenMode()
meth public org.apache.lucene.index.MergePolicy getMergePolicy()
meth public org.apache.lucene.index.MergeScheduler getMergeScheduler()
meth public org.apache.lucene.search.similarities.Similarity getSimilarity()
meth public org.apache.lucene.util.InfoStream getInfoStream()
supr org.apache.lucene.index.LiveIndexWriterConfig
hfds writer

CLSS public final static !enum org.apache.lucene.index.IndexWriterConfig$OpenMode
 outer org.apache.lucene.index.IndexWriterConfig
fld public final static org.apache.lucene.index.IndexWriterConfig$OpenMode APPEND
fld public final static org.apache.lucene.index.IndexWriterConfig$OpenMode CREATE
fld public final static org.apache.lucene.index.IndexWriterConfig$OpenMode CREATE_OR_APPEND
meth public static org.apache.lucene.index.IndexWriterConfig$OpenMode valueOf(java.lang.String)
meth public static org.apache.lucene.index.IndexWriterConfig$OpenMode[] values()
supr java.lang.Enum<org.apache.lucene.index.IndexWriterConfig$OpenMode>

CLSS public abstract interface org.apache.lucene.index.IndexWriterEventListener
fld public final static org.apache.lucene.index.IndexWriterEventListener NO_OP_LISTENER
meth public abstract void beginMergeOnFullFlush(org.apache.lucene.index.MergePolicy$MergeSpecification)
meth public abstract void endMergeOnFullFlush(org.apache.lucene.index.MergePolicy$MergeSpecification)

CLSS public abstract interface org.apache.lucene.index.IndexableField
meth public abstract java.io.Reader readerValue()
meth public abstract java.lang.Number numericValue()
meth public abstract java.lang.String name()
meth public abstract java.lang.String stringValue()
meth public abstract org.apache.lucene.analysis.TokenStream tokenStream(org.apache.lucene.analysis.Analyzer,org.apache.lucene.analysis.TokenStream)
meth public abstract org.apache.lucene.document.InvertableType invertableType()
meth public abstract org.apache.lucene.document.StoredValue storedValue()
meth public abstract org.apache.lucene.index.IndexableFieldType fieldType()
meth public abstract org.apache.lucene.util.BytesRef binaryValue()
meth public java.lang.CharSequence getCharSequenceValue()

CLSS public abstract interface org.apache.lucene.index.IndexableFieldType
meth public abstract boolean omitNorms()
meth public abstract boolean storeTermVectorOffsets()
meth public abstract boolean storeTermVectorPayloads()
meth public abstract boolean storeTermVectorPositions()
meth public abstract boolean storeTermVectors()
meth public abstract boolean stored()
meth public abstract boolean tokenized()
meth public abstract int pointDimensionCount()
meth public abstract int pointIndexDimensionCount()
meth public abstract int pointNumBytes()
meth public abstract int vectorDimension()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getAttributes()
meth public abstract org.apache.lucene.index.DocValuesType docValuesType()
meth public abstract org.apache.lucene.index.IndexOptions indexOptions()
meth public abstract org.apache.lucene.index.VectorEncoding vectorEncoding()
meth public abstract org.apache.lucene.index.VectorSimilarityFunction vectorSimilarityFunction()

CLSS public final org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy
cons public init()
meth public void onCommit(java.util.List<? extends org.apache.lucene.index.IndexCommit>)
meth public void onInit(java.util.List<? extends org.apache.lucene.index.IndexCommit>)
supr org.apache.lucene.index.IndexDeletionPolicy

CLSS public final org.apache.lucene.index.LeafMetaData
cons public init(int,org.apache.lucene.util.Version,org.apache.lucene.search.Sort,boolean)
meth public boolean hasBlocks()
meth public int getCreatedVersionMajor()
meth public org.apache.lucene.search.Sort getSort()
meth public org.apache.lucene.util.Version getMinVersion()
supr java.lang.Object
hfds createdVersionMajor,hasBlocks,minVersion,sort

CLSS public abstract org.apache.lucene.index.LeafReader
cons protected init()
meth public abstract org.apache.lucene.index.BinaryDocValues getBinaryDocValues(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.index.ByteVectorValues getByteVectorValues(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.index.FieldInfos getFieldInfos()
meth public abstract org.apache.lucene.index.FloatVectorValues getFloatVectorValues(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.index.IndexReader$CacheHelper getCoreCacheHelper()
meth public abstract org.apache.lucene.index.LeafMetaData getMetaData()
meth public abstract org.apache.lucene.index.NumericDocValues getNormValues(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.index.NumericDocValues getNumericDocValues(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.index.PointValues getPointValues(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.index.SortedDocValues getSortedDocValues(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.index.SortedNumericDocValues getSortedNumericDocValues(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.index.SortedSetDocValues getSortedSetDocValues(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.index.Terms terms(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.util.Bits getLiveDocs()
meth public abstract void checkIntegrity() throws java.io.IOException
meth public abstract void searchNearestVectors(java.lang.String,byte[],org.apache.lucene.search.KnnCollector,org.apache.lucene.util.Bits) throws java.io.IOException
meth public abstract void searchNearestVectors(java.lang.String,float[],org.apache.lucene.search.KnnCollector,org.apache.lucene.util.Bits) throws java.io.IOException
meth public final int docFreq(org.apache.lucene.index.Term) throws java.io.IOException
meth public final int getDocCount(java.lang.String) throws java.io.IOException
meth public final long getSumDocFreq(java.lang.String) throws java.io.IOException
meth public final long getSumTotalTermFreq(java.lang.String) throws java.io.IOException
meth public final long totalTermFreq(org.apache.lucene.index.Term) throws java.io.IOException
meth public final org.apache.lucene.index.LeafReaderContext getContext()
meth public final org.apache.lucene.index.PostingsEnum postings(org.apache.lucene.index.Term) throws java.io.IOException
meth public final org.apache.lucene.index.PostingsEnum postings(org.apache.lucene.index.Term,int) throws java.io.IOException
meth public final org.apache.lucene.search.TopDocs searchNearestVectors(java.lang.String,byte[],int,org.apache.lucene.util.Bits,int) throws java.io.IOException
meth public final org.apache.lucene.search.TopDocs searchNearestVectors(java.lang.String,float[],int,org.apache.lucene.util.Bits,int) throws java.io.IOException
supr org.apache.lucene.index.IndexReader
hfds readerContext

CLSS public final org.apache.lucene.index.LeafReaderContext
fld public final int docBase
fld public final int ord
meth public java.lang.String toString()
meth public java.util.List<org.apache.lucene.index.IndexReaderContext> children()
meth public java.util.List<org.apache.lucene.index.LeafReaderContext> leaves()
meth public org.apache.lucene.index.LeafReader reader()
supr org.apache.lucene.index.IndexReaderContext
hfds leaves,reader

CLSS public org.apache.lucene.index.LiveIndexWriterConfig
fld protected boolean commitOnClose
fld protected int createdVersionMajor
fld protected java.lang.String softDeletesField
fld protected java.util.Comparator<org.apache.lucene.index.LeafReader> leafSorter
fld protected java.util.Set<java.lang.String> indexSortFields
fld protected org.apache.lucene.index.IndexWriterEventListener eventListener
fld protected org.apache.lucene.search.Sort indexSort
fld protected volatile boolean checkPendingFlushOnUpdate
fld protected volatile boolean readerPooling
fld protected volatile boolean useCompoundFile
fld protected volatile int perThreadHardLimitMB
fld protected volatile java.lang.Object flushPolicy
fld protected volatile long maxFullFlushMergeWaitMillis
fld protected volatile org.apache.lucene.codecs.Codec codec
fld protected volatile org.apache.lucene.index.IndexCommit commit
fld protected volatile org.apache.lucene.index.IndexDeletionPolicy delPolicy
fld protected volatile org.apache.lucene.index.IndexWriterConfig$OpenMode openMode
fld protected volatile org.apache.lucene.index.MergePolicy mergePolicy
fld protected volatile org.apache.lucene.index.MergeScheduler mergeScheduler
fld protected volatile org.apache.lucene.search.similarities.Similarity similarity
fld protected volatile org.apache.lucene.util.InfoStream infoStream
meth public boolean getCommitOnClose()
meth public boolean getReaderPooling()
meth public boolean getUseCompoundFile()
meth public boolean isCheckPendingFlushOnUpdate()
meth public double getRAMBufferSizeMB()
meth public int getIndexCreatedVersionMajor()
meth public int getMaxBufferedDocs()
meth public int getRAMPerThreadHardLimitMB()
meth public java.lang.String getSoftDeletesField()
meth public java.lang.String toString()
meth public java.util.Comparator<org.apache.lucene.index.LeafReader> getLeafSorter()
meth public java.util.Set<java.lang.String> getIndexSortFields()
meth public long getMaxFullFlushMergeWaitMillis()
meth public org.apache.lucene.analysis.Analyzer getAnalyzer()
meth public org.apache.lucene.codecs.Codec getCodec()
meth public org.apache.lucene.index.IndexCommit getIndexCommit()
meth public org.apache.lucene.index.IndexDeletionPolicy getIndexDeletionPolicy()
meth public org.apache.lucene.index.IndexWriter$IndexReaderWarmer getMergedSegmentWarmer()
meth public org.apache.lucene.index.IndexWriterConfig$OpenMode getOpenMode()
meth public org.apache.lucene.index.IndexWriterEventListener getIndexWriterEventListener()
meth public org.apache.lucene.index.LiveIndexWriterConfig setCheckPendingFlushUpdate(boolean)
meth public org.apache.lucene.index.LiveIndexWriterConfig setMaxBufferedDocs(int)
meth public org.apache.lucene.index.LiveIndexWriterConfig setMergePolicy(org.apache.lucene.index.MergePolicy)
meth public org.apache.lucene.index.LiveIndexWriterConfig setMergedSegmentWarmer(org.apache.lucene.index.IndexWriter$IndexReaderWarmer)
meth public org.apache.lucene.index.LiveIndexWriterConfig setRAMBufferSizeMB(double)
meth public org.apache.lucene.index.LiveIndexWriterConfig setUseCompoundFile(boolean)
meth public org.apache.lucene.index.MergePolicy getMergePolicy()
meth public org.apache.lucene.index.MergeScheduler getMergeScheduler()
meth public org.apache.lucene.search.Sort getIndexSort()
meth public org.apache.lucene.search.similarities.Similarity getSimilarity()
meth public org.apache.lucene.util.InfoStream getInfoStream()
supr java.lang.Object
hfds analyzer,maxBufferedDocs,mergedSegmentWarmer,ramBufferSizeMB

CLSS public org.apache.lucene.index.LogByteSizeMergePolicy
cons public init()
fld public final static double DEFAULT_MAX_MERGE_MB = 2048.0
fld public final static double DEFAULT_MAX_MERGE_MB_FOR_FORCED_MERGE = 9.223372036854776E18
fld public final static double DEFAULT_MIN_MERGE_MB = 1.6
meth protected long size(org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public double getMaxMergeMB()
meth public double getMaxMergeMBForForcedMerge()
meth public double getMinMergeMB()
meth public void setMaxMergeMB(double)
meth public void setMaxMergeMBForForcedMerge(double)
meth public void setMinMergeMB(double)
supr org.apache.lucene.index.LogMergePolicy

CLSS public org.apache.lucene.index.LogDocMergePolicy
cons public init()
fld public final static int DEFAULT_MIN_MERGE_DOCS = 1000
meth protected long size(org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public int getMinMergeDocs()
meth public void setMinMergeDocs(int)
supr org.apache.lucene.index.LogMergePolicy

CLSS public abstract org.apache.lucene.index.LogMergePolicy
cons public init()
fld protected boolean calibrateSizeByDeletes
fld protected int maxMergeDocs
fld protected int mergeFactor
fld protected long maxMergeSize
fld protected long maxMergeSizeForForcedMerge
fld protected long minMergeSize
fld public final static double DEFAULT_NO_CFS_RATIO = 0.1
fld public final static double LEVEL_LOG_SPAN = 0.75
fld public final static int DEFAULT_MAX_MERGE_DOCS = 2147483647
fld public final static int DEFAULT_MERGE_FACTOR = 10
meth protected boolean isMerged(org.apache.lucene.index.SegmentInfos,int,java.util.Map<org.apache.lucene.index.SegmentCommitInfo,java.lang.Boolean>,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth protected long maxFullFlushMergeSize()
meth protected long sizeBytes(org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth protected long sizeDocs(org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public boolean getCalibrateSizeByDeletes()
meth public int getMaxMergeDocs()
meth public int getMergeFactor()
meth public java.lang.String toString()
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedDeletesMerges(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedMerges(org.apache.lucene.index.SegmentInfos,int,java.util.Map<org.apache.lucene.index.SegmentCommitInfo,java.lang.Boolean>,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public void setCalibrateSizeByDeletes(boolean)
meth public void setMaxMergeDocs(int)
meth public void setMergeFactor(int)
supr org.apache.lucene.index.MergePolicy
hcls SegmentInfoAndLevel

CLSS public org.apache.lucene.index.MappedMultiFields
cons public init(org.apache.lucene.index.MergeState,org.apache.lucene.index.MultiFields)
meth public org.apache.lucene.index.Terms terms(java.lang.String) throws java.io.IOException
supr org.apache.lucene.index.FilterLeafReader$FilterFields
hfds mergeState
hcls MappedMultiTerms,MappedMultiTermsEnum

CLSS public abstract org.apache.lucene.index.MergePolicy
cons protected init()
cons protected init(double,long)
fld protected double noCFSRatio
fld protected final static double DEFAULT_NO_CFS_RATIO = 1.0
fld protected final static long DEFAULT_MAX_CFS_SEGMENT_SIZE = 9223372036854775807
fld protected long maxCFSSegmentSize
innr public abstract interface static MergeContext
innr public static MergeAbortedException
innr public static MergeException
innr public static MergeSpecification
innr public static OneMerge
innr public static OneMergeProgress
meth protected final boolean assertDelCount(int,org.apache.lucene.index.SegmentCommitInfo)
meth protected final boolean isMerged(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth protected final boolean verbose(org.apache.lucene.index.MergePolicy$MergeContext)
meth protected final java.lang.String segString(org.apache.lucene.index.MergePolicy$MergeContext,java.lang.Iterable<org.apache.lucene.index.SegmentCommitInfo>)
meth protected final void message(java.lang.String,org.apache.lucene.index.MergePolicy$MergeContext)
meth protected long maxFullFlushMergeSize()
meth protected long size(org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public !varargs org.apache.lucene.index.MergePolicy$MergeSpecification findMerges(org.apache.lucene.index.CodecReader[]) throws java.io.IOException
meth public abstract org.apache.lucene.index.MergePolicy$MergeSpecification findForcedDeletesMerges(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public abstract org.apache.lucene.index.MergePolicy$MergeSpecification findForcedMerges(org.apache.lucene.index.SegmentInfos,int,java.util.Map<org.apache.lucene.index.SegmentCommitInfo,java.lang.Boolean>,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public abstract org.apache.lucene.index.MergePolicy$MergeSpecification findMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public boolean keepFullyDeletedSegment(org.apache.lucene.util.IOSupplier<org.apache.lucene.index.CodecReader>) throws java.io.IOException
meth public boolean useCompoundFile(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public double getMaxCFSSegmentSizeMB()
meth public double getNoCFSRatio()
meth public int numDeletesToMerge(org.apache.lucene.index.SegmentCommitInfo,int,org.apache.lucene.util.IOSupplier<org.apache.lucene.index.CodecReader>) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findFullFlushMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public void setMaxCFSSegmentSizeMB(double)
meth public void setNoCFSRatio(double)
supr java.lang.Object
hcls MergeReader

CLSS public static org.apache.lucene.index.MergePolicy$MergeAbortedException
 outer org.apache.lucene.index.MergePolicy
cons public init()
cons public init(java.lang.String)
supr java.io.IOException

CLSS public abstract interface static org.apache.lucene.index.MergePolicy$MergeContext
 outer org.apache.lucene.index.MergePolicy
meth public abstract int numDeletedDocs(org.apache.lucene.index.SegmentCommitInfo)
meth public abstract int numDeletesToMerge(org.apache.lucene.index.SegmentCommitInfo) throws java.io.IOException
meth public abstract java.util.Set<org.apache.lucene.index.SegmentCommitInfo> getMergingSegments()
meth public abstract org.apache.lucene.util.InfoStream getInfoStream()

CLSS public static org.apache.lucene.index.MergePolicy$MergeException
 outer org.apache.lucene.index.MergePolicy
cons public init(java.lang.String)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public static org.apache.lucene.index.MergePolicy$MergeSpecification
 outer org.apache.lucene.index.MergePolicy
cons public init()
fld public final java.util.List<org.apache.lucene.index.MergePolicy$OneMerge> merges
meth public java.lang.String segString(org.apache.lucene.store.Directory)
meth public java.lang.String toString()
meth public void add(org.apache.lucene.index.MergePolicy$OneMerge)
supr java.lang.Object

CLSS public static org.apache.lucene.index.MergePolicy$OneMerge
 outer org.apache.lucene.index.MergePolicy
cons protected init(org.apache.lucene.index.MergePolicy$OneMerge)
cons public !varargs init(org.apache.lucene.index.CodecReader[])
cons public init(java.util.List<org.apache.lucene.index.SegmentCommitInfo>)
fld public final java.util.List<org.apache.lucene.index.SegmentCommitInfo> segments
fld public volatile long estimatedMergeBytes
meth public boolean isAborted()
meth public int totalNumDocs()
meth public java.lang.String segString()
meth public long totalBytesSize()
meth public org.apache.lucene.index.CodecReader wrapForMerge(org.apache.lucene.index.CodecReader) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$OneMergeProgress getMergeProgress()
meth public org.apache.lucene.index.SegmentCommitInfo getMergeInfo()
meth public org.apache.lucene.index.Sorter$DocMap reorder(org.apache.lucene.index.CodecReader,org.apache.lucene.store.Directory) throws java.io.IOException
meth public org.apache.lucene.store.MergeInfo getStoreMergeInfo()
meth public void checkAborted() throws org.apache.lucene.index.MergePolicy$MergeAbortedException
meth public void mergeFinished(boolean,boolean) throws java.io.IOException
meth public void mergeInit() throws java.io.IOException
meth public void setAborted()
meth public void setMergeInfo(org.apache.lucene.index.SegmentCommitInfo)
supr java.lang.Object
hfds error,info,isExternal,maxNumSegments,mergeCompleted,mergeGen,mergeProgress,mergeReaders,mergeStartNS,registerDone,totalMaxDoc,totalMergeBytes,usesPooledReaders

CLSS public static org.apache.lucene.index.MergePolicy$OneMergeProgress
 outer org.apache.lucene.index.MergePolicy
cons public init()
innr public final static !enum PauseReason
meth public boolean isAborted()
meth public java.util.Map<org.apache.lucene.index.MergePolicy$OneMergeProgress$PauseReason,java.lang.Long> getPauseTimes()
meth public void abort()
meth public void pauseNanos(long,org.apache.lucene.index.MergePolicy$OneMergeProgress$PauseReason,java.util.function.BooleanSupplier) throws java.lang.InterruptedException
meth public void wakeup()
supr java.lang.Object
hfds aborted,owner,pauseLock,pauseTimesNS,pausing

CLSS public final static !enum org.apache.lucene.index.MergePolicy$OneMergeProgress$PauseReason
 outer org.apache.lucene.index.MergePolicy$OneMergeProgress
fld public final static org.apache.lucene.index.MergePolicy$OneMergeProgress$PauseReason OTHER
fld public final static org.apache.lucene.index.MergePolicy$OneMergeProgress$PauseReason PAUSED
fld public final static org.apache.lucene.index.MergePolicy$OneMergeProgress$PauseReason STOPPED
meth public static org.apache.lucene.index.MergePolicy$OneMergeProgress$PauseReason valueOf(java.lang.String)
meth public static org.apache.lucene.index.MergePolicy$OneMergeProgress$PauseReason[] values()
supr java.lang.Enum<org.apache.lucene.index.MergePolicy$OneMergeProgress$PauseReason>

CLSS public org.apache.lucene.index.MergeRateLimiter
cons public init(org.apache.lucene.index.MergePolicy$OneMergeProgress)
meth public double getMBPerSec()
meth public long getMinPauseCheckBytes()
meth public long getTotalBytesWritten()
meth public long getTotalPausedNS()
meth public long getTotalStoppedNS()
meth public long pause(long) throws org.apache.lucene.index.MergePolicy$MergeAbortedException
meth public void setMBPerSec(double)
supr org.apache.lucene.store.RateLimiter
hfds MAX_PAUSE_NS,MIN_PAUSE_CHECK_MSEC,MIN_PAUSE_NS,lastNS,mbPerSec,mergeProgress,minPauseCheckBytes,totalBytesWritten

CLSS public abstract org.apache.lucene.index.MergeScheduler
cons protected init()
fld protected org.apache.lucene.util.InfoStream infoStream
innr public abstract interface static MergeSource
intf java.io.Closeable
meth protected boolean verbose()
meth protected void message(java.lang.String)
meth public abstract void close() throws java.io.IOException
meth public abstract void merge(org.apache.lucene.index.MergeScheduler$MergeSource,org.apache.lucene.index.MergeTrigger) throws java.io.IOException
meth public org.apache.lucene.store.Directory wrapForMerge(org.apache.lucene.index.MergePolicy$OneMerge,org.apache.lucene.store.Directory)
supr java.lang.Object

CLSS public abstract interface static org.apache.lucene.index.MergeScheduler$MergeSource
 outer org.apache.lucene.index.MergeScheduler
meth public abstract boolean hasPendingMerges()
meth public abstract org.apache.lucene.index.MergePolicy$OneMerge getNextMerge()
meth public abstract void merge(org.apache.lucene.index.MergePolicy$OneMerge) throws java.io.IOException
meth public abstract void onMergeFinished(org.apache.lucene.index.MergePolicy$OneMerge)

CLSS public org.apache.lucene.index.MergeState
fld public boolean needsIndexSort
fld public final int[] maxDocs
fld public final org.apache.lucene.codecs.DocValuesProducer[] docValuesProducers
fld public final org.apache.lucene.codecs.FieldsProducer[] fieldsProducers
fld public final org.apache.lucene.codecs.KnnVectorsReader[] knnVectorsReaders
fld public final org.apache.lucene.codecs.NormsProducer[] normsProducers
fld public final org.apache.lucene.codecs.PointsReader[] pointsReaders
fld public final org.apache.lucene.codecs.StoredFieldsReader[] storedFieldsReaders
fld public final org.apache.lucene.codecs.TermVectorsReader[] termVectorsReaders
fld public final org.apache.lucene.index.FieldInfos[] fieldInfos
fld public final org.apache.lucene.index.MergeState$DocMap[] docMaps
fld public final org.apache.lucene.index.SegmentInfo segmentInfo
fld public final org.apache.lucene.util.Bits[] liveDocs
fld public final org.apache.lucene.util.InfoStream infoStream
fld public org.apache.lucene.index.FieldInfos mergeFieldInfos
innr public abstract interface static DocMap
supr java.lang.Object

CLSS public abstract interface static org.apache.lucene.index.MergeState$DocMap
 outer org.apache.lucene.index.MergeState
 anno 0 java.lang.FunctionalInterface()
meth public abstract int get(int)

CLSS public final !enum org.apache.lucene.index.MergeTrigger
fld public final static org.apache.lucene.index.MergeTrigger ADD_INDEXES
fld public final static org.apache.lucene.index.MergeTrigger CLOSING
fld public final static org.apache.lucene.index.MergeTrigger COMMIT
fld public final static org.apache.lucene.index.MergeTrigger EXPLICIT
fld public final static org.apache.lucene.index.MergeTrigger FULL_FLUSH
fld public final static org.apache.lucene.index.MergeTrigger GET_READER
fld public final static org.apache.lucene.index.MergeTrigger MERGE_FINISHED
fld public final static org.apache.lucene.index.MergeTrigger SEGMENT_FLUSH
meth public static org.apache.lucene.index.MergeTrigger valueOf(java.lang.String)
meth public static org.apache.lucene.index.MergeTrigger[] values()
supr java.lang.Enum<org.apache.lucene.index.MergeTrigger>

CLSS public final org.apache.lucene.index.MultiBits
intf org.apache.lucene.util.Bits
meth public boolean get(int)
meth public int length()
meth public java.lang.String toString()
meth public static org.apache.lucene.util.Bits getLiveDocs(org.apache.lucene.index.IndexReader)
supr java.lang.Object
hfds defaultValue,starts,subs

CLSS public org.apache.lucene.index.MultiDocValues
innr public static MultiSortedDocValues
innr public static MultiSortedSetDocValues
meth public static org.apache.lucene.index.BinaryDocValues getBinaryValues(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.NumericDocValues getNormValues(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.NumericDocValues getNumericValues(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.SortedDocValues getSortedValues(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.SortedNumericDocValues getSortedNumericValues(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.SortedSetDocValues getSortedSetValues(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public static org.apache.lucene.index.MultiDocValues$MultiSortedDocValues
 outer org.apache.lucene.index.MultiDocValues
cons public init(org.apache.lucene.index.SortedDocValues[],int[],org.apache.lucene.index.OrdinalMap,long)
fld public final int[] docStarts
fld public final org.apache.lucene.index.OrdinalMap mapping
fld public final org.apache.lucene.index.SortedDocValues[] values
meth public boolean advanceExact(int) throws java.io.IOException
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int getValueCount()
meth public int nextDoc() throws java.io.IOException
meth public int ordValue() throws java.io.IOException
meth public long cost()
meth public org.apache.lucene.util.BytesRef lookupOrd(int) throws java.io.IOException
supr org.apache.lucene.index.SortedDocValues
hfds currentDocStart,currentValues,docID,nextLeaf,totalCost

CLSS public static org.apache.lucene.index.MultiDocValues$MultiSortedSetDocValues
 outer org.apache.lucene.index.MultiDocValues
cons public init(org.apache.lucene.index.SortedSetDocValues[],int[],org.apache.lucene.index.OrdinalMap,long)
fld public final int[] docStarts
fld public final org.apache.lucene.index.OrdinalMap mapping
fld public final org.apache.lucene.index.SortedSetDocValues[] values
meth public boolean advanceExact(int) throws java.io.IOException
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int docValueCount()
meth public int nextDoc() throws java.io.IOException
meth public long cost()
meth public long getValueCount()
meth public long nextOrd() throws java.io.IOException
meth public org.apache.lucene.util.BytesRef lookupOrd(long) throws java.io.IOException
supr org.apache.lucene.index.SortedSetDocValues
hfds currentDocStart,currentValues,docID,nextLeaf,totalCost

CLSS public final org.apache.lucene.index.MultiFields
cons public init(org.apache.lucene.index.Fields[],org.apache.lucene.index.ReaderSlice[])
meth public int size()
meth public java.util.Iterator<java.lang.String> iterator()
meth public org.apache.lucene.index.Terms terms(java.lang.String) throws java.io.IOException
supr org.apache.lucene.index.Fields
hfds subSlices,subs,terms

CLSS public org.apache.lucene.index.MultiLeafReader
supr java.lang.Object

CLSS public final org.apache.lucene.index.MultiPostingsEnum
cons public init(org.apache.lucene.index.MultiTermsEnum,int)
innr public final static EnumWithSlice
meth public boolean canReuse(org.apache.lucene.index.MultiTermsEnum)
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int endOffset() throws java.io.IOException
meth public int freq() throws java.io.IOException
meth public int getNumSubs()
meth public int nextDoc() throws java.io.IOException
meth public int nextPosition() throws java.io.IOException
meth public int startOffset() throws java.io.IOException
meth public java.lang.String toString()
meth public long cost()
meth public org.apache.lucene.index.MultiPostingsEnum reset(org.apache.lucene.index.MultiPostingsEnum$EnumWithSlice[],int)
meth public org.apache.lucene.index.MultiPostingsEnum$EnumWithSlice[] getSubs()
meth public org.apache.lucene.util.BytesRef getPayload() throws java.io.IOException
supr org.apache.lucene.index.PostingsEnum
hfds current,currentBase,doc,numSubs,parent,subPostingsEnums,subs,upto

CLSS public final static org.apache.lucene.index.MultiPostingsEnum$EnumWithSlice
 outer org.apache.lucene.index.MultiPostingsEnum
fld public org.apache.lucene.index.PostingsEnum postingsEnum
fld public org.apache.lucene.index.ReaderSlice slice
meth public java.lang.String toString()
supr java.lang.Object

CLSS public org.apache.lucene.index.MultiReader
cons public !varargs init(org.apache.lucene.index.IndexReader[]) throws java.io.IOException
cons public init(org.apache.lucene.index.IndexReader[],boolean) throws java.io.IOException
cons public init(org.apache.lucene.index.IndexReader[],java.util.Comparator<org.apache.lucene.index.IndexReader>,boolean) throws java.io.IOException
meth protected void doClose() throws java.io.IOException
meth public org.apache.lucene.index.IndexReader$CacheHelper getReaderCacheHelper()
supr org.apache.lucene.index.BaseCompositeReader<org.apache.lucene.index.IndexReader>
hfds closeSubReaders

CLSS public final org.apache.lucene.index.MultiTerms
cons public init(org.apache.lucene.index.Terms[],org.apache.lucene.index.ReaderSlice[]) throws java.io.IOException
meth public boolean hasFreqs()
meth public boolean hasOffsets()
meth public boolean hasPayloads()
meth public boolean hasPositions()
meth public int getDocCount() throws java.io.IOException
meth public long getSumDocFreq() throws java.io.IOException
meth public long getSumTotalTermFreq() throws java.io.IOException
meth public long size()
meth public org.apache.lucene.index.ReaderSlice[] getSubSlices()
meth public org.apache.lucene.index.TermsEnum intersect(org.apache.lucene.util.automaton.CompiledAutomaton,org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum iterator() throws java.io.IOException
meth public org.apache.lucene.index.Terms[] getSubTerms()
meth public org.apache.lucene.util.BytesRef getMax() throws java.io.IOException
meth public org.apache.lucene.util.BytesRef getMin() throws java.io.IOException
meth public static org.apache.lucene.index.PostingsEnum getTermPostingsEnum(org.apache.lucene.index.IndexReader,java.lang.String,org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public static org.apache.lucene.index.PostingsEnum getTermPostingsEnum(org.apache.lucene.index.IndexReader,java.lang.String,org.apache.lucene.util.BytesRef,int) throws java.io.IOException
meth public static org.apache.lucene.index.Terms getTerms(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
supr org.apache.lucene.index.Terms
hfds hasFreqs,hasOffsets,hasPayloads,hasPositions,subSlices,subs

CLSS public final org.apache.lucene.index.MultiTermsEnum
cons public init(org.apache.lucene.index.ReaderSlice[])
meth public boolean seekExact(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public int docFreq() throws java.io.IOException
meth public int getMatchCount()
meth public java.lang.Object getMatchArray()
meth public java.lang.String toString()
meth public long ord()
meth public long totalTermFreq() throws java.io.IOException
meth public org.apache.lucene.index.ImpactsEnum impacts(int) throws java.io.IOException
meth public org.apache.lucene.index.PostingsEnum postings(org.apache.lucene.index.PostingsEnum,int) throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum reset(org.apache.lucene.index.TermsEnumIndex[]) throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum$SeekStatus seekCeil(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public org.apache.lucene.util.BytesRef next() throws java.io.IOException
meth public org.apache.lucene.util.BytesRef term()
meth public void seekExact(long)
supr org.apache.lucene.index.BaseTermsEnum
hfds INDEX_COMPARATOR,current,currentSubs,lastSeek,lastSeekExact,lastSeekScratch,numSubs,numTop,queue,subDocs,subs,top
hcls TermMergeQueue,TermsEnumWithSlice

CLSS public final org.apache.lucene.index.NoDeletionPolicy
fld public final static org.apache.lucene.index.IndexDeletionPolicy INSTANCE
meth public void onCommit(java.util.List<? extends org.apache.lucene.index.IndexCommit>)
meth public void onInit(java.util.List<? extends org.apache.lucene.index.IndexCommit>)
supr org.apache.lucene.index.IndexDeletionPolicy

CLSS public final org.apache.lucene.index.NoMergePolicy
fld public final static org.apache.lucene.index.MergePolicy INSTANCE
meth protected long size(org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public !varargs org.apache.lucene.index.MergePolicy$MergeSpecification findMerges(org.apache.lucene.index.CodecReader[]) throws java.io.IOException
meth public boolean keepFullyDeletedSegment(org.apache.lucene.util.IOSupplier<org.apache.lucene.index.CodecReader>) throws java.io.IOException
meth public boolean useCompoundFile(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.MergePolicy$MergeContext)
meth public double getMaxCFSSegmentSizeMB()
meth public double getNoCFSRatio()
meth public int numDeletesToMerge(org.apache.lucene.index.SegmentCommitInfo,int,org.apache.lucene.util.IOSupplier<org.apache.lucene.index.CodecReader>) throws java.io.IOException
meth public java.lang.String toString()
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedDeletesMerges(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext)
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedMerges(org.apache.lucene.index.SegmentInfos,int,java.util.Map<org.apache.lucene.index.SegmentCommitInfo,java.lang.Boolean>,org.apache.lucene.index.MergePolicy$MergeContext)
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findFullFlushMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext)
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext)
meth public void setMaxCFSSegmentSizeMB(double)
meth public void setNoCFSRatio(double)
supr org.apache.lucene.index.MergePolicy

CLSS public final org.apache.lucene.index.NoMergeScheduler
fld public final static org.apache.lucene.index.MergeScheduler INSTANCE
meth public org.apache.lucene.index.MergeScheduler clone()
meth public org.apache.lucene.store.Directory wrapForMerge(org.apache.lucene.index.MergePolicy$OneMerge,org.apache.lucene.store.Directory)
meth public void close()
meth public void merge(org.apache.lucene.index.MergeScheduler$MergeSource,org.apache.lucene.index.MergeTrigger)
supr org.apache.lucene.index.MergeScheduler

CLSS public abstract org.apache.lucene.index.NumericDocValues
cons protected init()
meth public abstract boolean advanceExact(int) throws java.io.IOException
meth public abstract long longValue() throws java.io.IOException
supr org.apache.lucene.search.DocIdSetIterator

CLSS public org.apache.lucene.index.OneMergeWrappingMergePolicy
cons public init(org.apache.lucene.index.MergePolicy,java.util.function.UnaryOperator<org.apache.lucene.index.MergePolicy$OneMerge>)
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedDeletesMerges(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedMerges(org.apache.lucene.index.SegmentInfos,int,java.util.Map<org.apache.lucene.index.SegmentCommitInfo,java.lang.Boolean>,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findFullFlushMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
supr org.apache.lucene.index.FilterMergePolicy
hfds wrapOneMerge

CLSS public org.apache.lucene.index.OrdTermState
cons public init()
fld public long ord
meth public java.lang.String toString()
meth public void copyFrom(org.apache.lucene.index.TermState)
supr org.apache.lucene.index.TermState

CLSS public org.apache.lucene.index.OrdinalMap
fld public final org.apache.lucene.index.IndexReader$CacheKey owner
intf org.apache.lucene.util.Accountable
meth public int getFirstSegmentNumber(long)
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public long getFirstSegmentOrd(long)
meth public long getValueCount()
meth public long ramBytesUsed()
meth public org.apache.lucene.util.LongValues getGlobalOrds(int)
meth public static org.apache.lucene.index.OrdinalMap build(org.apache.lucene.index.IndexReader$CacheKey,org.apache.lucene.index.SortedDocValues[],float) throws java.io.IOException
meth public static org.apache.lucene.index.OrdinalMap build(org.apache.lucene.index.IndexReader$CacheKey,org.apache.lucene.index.SortedSetDocValues[],float) throws java.io.IOException
meth public static org.apache.lucene.index.OrdinalMap build(org.apache.lucene.index.IndexReader$CacheKey,org.apache.lucene.index.TermsEnum[],long[],float) throws java.io.IOException
supr java.lang.Object
hfds BASE_RAM_BYTES_USED,firstSegments,globalOrdDeltas,ramBytesUsed,segmentMap,segmentToGlobalOrds,valueCount
hcls SegmentMap,TermsEnumPriorityQueue

CLSS public org.apache.lucene.index.ParallelCompositeReader
cons public !varargs init(boolean,org.apache.lucene.index.CompositeReader[]) throws java.io.IOException
cons public !varargs init(org.apache.lucene.index.CompositeReader[]) throws java.io.IOException
cons public init(boolean,org.apache.lucene.index.CompositeReader[],org.apache.lucene.index.CompositeReader[]) throws java.io.IOException
meth protected void doClose() throws java.io.IOException
meth public org.apache.lucene.index.IndexReader$CacheHelper getReaderCacheHelper()
supr org.apache.lucene.index.BaseCompositeReader<org.apache.lucene.index.LeafReader>
hfds cacheHelper,closeSubReaders,completeReaderSet

CLSS public org.apache.lucene.index.ParallelLeafReader
cons public !varargs init(boolean,org.apache.lucene.index.LeafReader[]) throws java.io.IOException
cons public !varargs init(org.apache.lucene.index.LeafReader[]) throws java.io.IOException
cons public init(boolean,org.apache.lucene.index.LeafReader[],org.apache.lucene.index.LeafReader[]) throws java.io.IOException
meth protected void doClose() throws java.io.IOException
meth public int maxDoc()
meth public int numDocs()
meth public java.lang.String toString()
meth public org.apache.lucene.index.BinaryDocValues getBinaryDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.ByteVectorValues getByteVectorValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.FieldInfos getFieldInfos()
meth public org.apache.lucene.index.Fields getTermVectors(int) throws java.io.IOException
meth public org.apache.lucene.index.FloatVectorValues getFloatVectorValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.IndexReader$CacheHelper getCoreCacheHelper()
meth public org.apache.lucene.index.IndexReader$CacheHelper getReaderCacheHelper()
meth public org.apache.lucene.index.LeafMetaData getMetaData()
meth public org.apache.lucene.index.LeafReader[] getParallelReaders()
meth public org.apache.lucene.index.NumericDocValues getNormValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.NumericDocValues getNumericDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.PointValues getPointValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedDocValues getSortedDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedNumericDocValues getSortedNumericDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedSetDocValues getSortedSetDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.StoredFields storedFields() throws java.io.IOException
meth public org.apache.lucene.index.TermVectors termVectors() throws java.io.IOException
meth public org.apache.lucene.index.Terms terms(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.util.Bits getLiveDocs()
meth public void checkIntegrity() throws java.io.IOException
meth public void document(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
meth public void searchNearestVectors(java.lang.String,byte[],org.apache.lucene.search.KnnCollector,org.apache.lucene.util.Bits) throws java.io.IOException
meth public void searchNearestVectors(java.lang.String,float[],org.apache.lucene.search.KnnCollector,org.apache.lucene.util.Bits) throws java.io.IOException
supr org.apache.lucene.index.LeafReader
hfds closeSubReaders,completeReaderSet,fieldInfos,fieldToReader,hasDeletions,maxDoc,metaData,numDocs,parallelReaders,storedFieldsReaders,termsFieldToReader,tvFieldToReader
hcls ParallelFields

CLSS public org.apache.lucene.index.PersistentSnapshotDeletionPolicy
cons public init(org.apache.lucene.index.IndexDeletionPolicy,org.apache.lucene.store.Directory) throws java.io.IOException
cons public init(org.apache.lucene.index.IndexDeletionPolicy,org.apache.lucene.store.Directory,org.apache.lucene.index.IndexWriterConfig$OpenMode) throws java.io.IOException
fld public final static java.lang.String SNAPSHOTS_PREFIX = "snapshots_"
meth public java.lang.String getLastSaveFile()
meth public org.apache.lucene.index.IndexCommit snapshot() throws java.io.IOException
meth public void release(long) throws java.io.IOException
meth public void release(org.apache.lucene.index.IndexCommit) throws java.io.IOException
supr org.apache.lucene.index.SnapshotDeletionPolicy
hfds CODEC_NAME,VERSION_CURRENT,VERSION_START,dir,nextWriteGen

CLSS public abstract org.apache.lucene.index.PointValues
cons protected init()
fld public final static int MAX_DIMENSIONS = 16
fld public final static int MAX_INDEX_DIMENSIONS = 8
fld public final static int MAX_NUM_BYTES = 16
innr public abstract interface static IntersectVisitor
innr public abstract interface static PointTree
innr public final static !enum Relation
meth public abstract byte[] getMaxPackedValue() throws java.io.IOException
meth public abstract byte[] getMinPackedValue() throws java.io.IOException
meth public abstract int getBytesPerDimension() throws java.io.IOException
meth public abstract int getDocCount()
meth public abstract int getNumDimensions() throws java.io.IOException
meth public abstract int getNumIndexDimensions() throws java.io.IOException
meth public abstract long size()
meth public abstract org.apache.lucene.index.PointValues$PointTree getPointTree() throws java.io.IOException
meth public final long estimateDocCount(org.apache.lucene.index.PointValues$IntersectVisitor)
meth public final long estimatePointCount(org.apache.lucene.index.PointValues$IntersectVisitor)
meth public final void intersect(org.apache.lucene.index.PointValues$IntersectVisitor) throws java.io.IOException
meth public static byte[] getMaxPackedValue(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
meth public static byte[] getMinPackedValue(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
meth public static int getDocCount(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
meth public static long size(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface static org.apache.lucene.index.PointValues$IntersectVisitor
 outer org.apache.lucene.index.PointValues
meth public abstract org.apache.lucene.index.PointValues$Relation compare(byte[],byte[])
meth public abstract void visit(int) throws java.io.IOException
meth public abstract void visit(int,byte[]) throws java.io.IOException
meth public void grow(int)
meth public void visit(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
meth public void visit(org.apache.lucene.search.DocIdSetIterator,byte[]) throws java.io.IOException

CLSS public abstract interface static org.apache.lucene.index.PointValues$PointTree
 outer org.apache.lucene.index.PointValues
intf java.lang.Cloneable
meth public abstract boolean moveToChild() throws java.io.IOException
meth public abstract boolean moveToParent() throws java.io.IOException
meth public abstract boolean moveToSibling() throws java.io.IOException
meth public abstract byte[] getMaxPackedValue()
meth public abstract byte[] getMinPackedValue()
meth public abstract long size()
meth public abstract org.apache.lucene.index.PointValues$PointTree clone()
meth public abstract void visitDocIDs(org.apache.lucene.index.PointValues$IntersectVisitor) throws java.io.IOException
meth public abstract void visitDocValues(org.apache.lucene.index.PointValues$IntersectVisitor) throws java.io.IOException

CLSS public final static !enum org.apache.lucene.index.PointValues$Relation
 outer org.apache.lucene.index.PointValues
fld public final static org.apache.lucene.index.PointValues$Relation CELL_CROSSES_QUERY
fld public final static org.apache.lucene.index.PointValues$Relation CELL_INSIDE_QUERY
fld public final static org.apache.lucene.index.PointValues$Relation CELL_OUTSIDE_QUERY
meth public static org.apache.lucene.index.PointValues$Relation valueOf(java.lang.String)
meth public static org.apache.lucene.index.PointValues$Relation[] values()
supr java.lang.Enum<org.apache.lucene.index.PointValues$Relation>

CLSS public abstract org.apache.lucene.index.PostingsEnum
cons protected init()
fld public final static short ALL = 120
fld public final static short FREQS = 8
fld public final static short NONE = 0
fld public final static short OFFSETS = 56
fld public final static short PAYLOADS = 88
fld public final static short POSITIONS = 24
meth public abstract int endOffset() throws java.io.IOException
meth public abstract int freq() throws java.io.IOException
meth public abstract int nextPosition() throws java.io.IOException
meth public abstract int startOffset() throws java.io.IOException
meth public abstract org.apache.lucene.util.BytesRef getPayload() throws java.io.IOException
meth public static boolean featureRequested(int,short)
supr org.apache.lucene.search.DocIdSetIterator

CLSS public org.apache.lucene.index.PrefixCodedTerms
innr public static Builder
innr public static TermIterator
intf org.apache.lucene.util.Accountable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public long ramBytesUsed()
meth public long size()
meth public org.apache.lucene.index.PrefixCodedTerms$TermIterator iterator()
meth public void setDelGen(long)
supr java.lang.Object
hfds content,delGen,lazyHash,size

CLSS public static org.apache.lucene.index.PrefixCodedTerms$Builder
 outer org.apache.lucene.index.PrefixCodedTerms
cons public init()
meth public org.apache.lucene.index.PrefixCodedTerms finish()
meth public void add(java.lang.String,org.apache.lucene.util.BytesRef)
meth public void add(org.apache.lucene.index.Term)
supr java.lang.Object
hfds lastTerm,lastTermBytes,output,size

CLSS public static org.apache.lucene.index.PrefixCodedTerms$TermIterator
 outer org.apache.lucene.index.PrefixCodedTerms
intf org.apache.lucene.util.BytesRefIterator
meth public java.lang.String field()
meth public long delGen()
meth public org.apache.lucene.util.BytesRef next()
supr java.lang.Object
hfds builder,bytes,delGen,end,field,input

CLSS public abstract interface org.apache.lucene.index.QueryTimeout
meth public abstract boolean shouldExit()

CLSS public org.apache.lucene.index.QueryTimeoutImpl
cons public init(long)
intf org.apache.lucene.index.QueryTimeout
meth public boolean shouldExit()
meth public java.lang.Long getTimeoutAt()
meth public java.lang.String toString()
meth public void reset()
supr java.lang.Object
hfds timeoutAt

CLSS public final org.apache.lucene.index.ReaderManager
cons public init(org.apache.lucene.index.DirectoryReader) throws java.io.IOException
cons public init(org.apache.lucene.index.IndexWriter) throws java.io.IOException
cons public init(org.apache.lucene.index.IndexWriter,boolean,boolean) throws java.io.IOException
cons public init(org.apache.lucene.store.Directory) throws java.io.IOException
meth protected boolean tryIncRef(org.apache.lucene.index.DirectoryReader)
meth protected int getRefCount(org.apache.lucene.index.DirectoryReader)
meth protected org.apache.lucene.index.DirectoryReader refreshIfNeeded(org.apache.lucene.index.DirectoryReader) throws java.io.IOException
meth protected void decRef(org.apache.lucene.index.DirectoryReader) throws java.io.IOException
supr org.apache.lucene.search.ReferenceManager<org.apache.lucene.index.DirectoryReader>

CLSS public final org.apache.lucene.index.ReaderSlice
cons public init(int,int,int)
fld public final int length
fld public final int readerIndex
fld public final int start
fld public final static org.apache.lucene.index.ReaderSlice[] EMPTY_ARRAY
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.apache.lucene.index.ReaderUtil
meth public static int subIndex(int,int[])
meth public static int subIndex(int,java.util.List<org.apache.lucene.index.LeafReaderContext>)
meth public static org.apache.lucene.index.IndexReaderContext getTopLevelContext(org.apache.lucene.index.IndexReaderContext)
supr java.lang.Object

CLSS public org.apache.lucene.index.SegmentCommitInfo
cons public init(org.apache.lucene.index.SegmentInfo,int,int,long,long,long,byte[])
fld public final org.apache.lucene.index.SegmentInfo info
meth public boolean hasDeletions()
meth public boolean hasFieldUpdates()
meth public byte[] getId()
meth public int getDelCount()
meth public int getSoftDelCount()
meth public java.lang.String toString()
meth public java.lang.String toString(int)
meth public java.util.Collection<java.lang.String> files() throws java.io.IOException
meth public java.util.Map<java.lang.Integer,java.util.Set<java.lang.String>> getDocValuesUpdatesFiles()
meth public java.util.Set<java.lang.String> getFieldInfosFiles()
meth public long getDelGen()
meth public long getDocValuesGen()
meth public long getFieldInfosGen()
meth public long getNextDelGen()
meth public long getNextDocValuesGen()
meth public long getNextFieldInfosGen()
meth public long sizeInBytes() throws java.io.IOException
meth public org.apache.lucene.index.SegmentCommitInfo clone()
meth public void setDocValuesUpdatesFiles(java.util.Map<java.lang.Integer,java.util.Set<java.lang.String>>)
meth public void setFieldInfosFiles(java.util.Set<java.lang.String>)
supr java.lang.Object
hfds bufferedDeletesGen,delCount,delGen,docValuesGen,dvUpdatesFiles,fieldInfosFiles,fieldInfosGen,id,nextWriteDelGen,nextWriteDocValuesGen,nextWriteFieldInfosGen,sizeInBytes,softDelCount

CLSS public final org.apache.lucene.index.SegmentInfo
cons public init(org.apache.lucene.store.Directory,org.apache.lucene.util.Version,org.apache.lucene.util.Version,java.lang.String,int,boolean,boolean,org.apache.lucene.codecs.Codec,java.util.Map<java.lang.String,java.lang.String>,byte[],java.util.Map<java.lang.String,java.lang.String>,org.apache.lucene.search.Sort)
fld public final java.lang.String name
fld public final org.apache.lucene.store.Directory dir
fld public final static int NO = -1
fld public final static int YES = 1
meth public boolean equals(java.lang.Object)
meth public boolean getHasBlocks()
meth public boolean getUseCompoundFile()
meth public byte[] getId()
meth public int hashCode()
meth public int maxDoc()
meth public java.lang.String getAttribute(java.lang.String)
meth public java.lang.String putAttribute(java.lang.String,java.lang.String)
meth public java.lang.String toString()
meth public java.lang.String toString(int)
meth public java.util.Map<java.lang.String,java.lang.String> getAttributes()
meth public java.util.Map<java.lang.String,java.lang.String> getDiagnostics()
meth public java.util.Set<java.lang.String> files()
meth public org.apache.lucene.codecs.Codec getCodec()
meth public org.apache.lucene.search.Sort getIndexSort()
meth public org.apache.lucene.util.Version getMinVersion()
meth public org.apache.lucene.util.Version getVersion()
meth public void addDiagnostics(java.util.Map<java.lang.String,java.lang.String>)
meth public void addFile(java.lang.String)
meth public void addFiles(java.util.Collection<java.lang.String>)
meth public void setCodec(org.apache.lucene.codecs.Codec)
meth public void setFiles(java.util.Collection<java.lang.String>)
supr java.lang.Object
hfds attributes,codec,diagnostics,hasBlocks,id,indexSort,isCompoundFile,maxDoc,minVersion,setFiles,version

CLSS public final org.apache.lucene.index.SegmentInfos
cons public init(int)
fld public final static int VERSION_70 = 7
fld public final static int VERSION_72 = 8
fld public final static int VERSION_74 = 9
fld public final static int VERSION_86 = 10
fld public java.util.Map<java.lang.String,java.lang.String> userData
fld public long counter
fld public long version
innr public abstract static FindSegmentsFile
intf java.lang.Cloneable
intf java.lang.Iterable<org.apache.lucene.index.SegmentCommitInfo>
meth public boolean remove(org.apache.lucene.index.SegmentCommitInfo)
meth public byte[] getId()
meth public final static org.apache.lucene.index.SegmentInfos readCommit(org.apache.lucene.store.Directory,java.lang.String) throws java.io.IOException
meth public final static org.apache.lucene.index.SegmentInfos readCommit(org.apache.lucene.store.Directory,org.apache.lucene.store.ChecksumIndexInput,long) throws java.io.IOException
meth public final static org.apache.lucene.index.SegmentInfos readLatestCommit(org.apache.lucene.store.Directory) throws java.io.IOException
meth public final static org.apache.lucene.index.SegmentInfos readLatestCommit(org.apache.lucene.store.Directory,int) throws java.io.IOException
meth public final void commit(org.apache.lucene.store.Directory) throws java.io.IOException
meth public int getIndexCreatedVersionMajor()
meth public int size()
meth public int totalMaxDoc()
meth public java.lang.String getSegmentsFileName()
meth public java.lang.String toString()
meth public java.util.Collection<java.lang.String> files(boolean) throws java.io.IOException
meth public java.util.Iterator<org.apache.lucene.index.SegmentCommitInfo> iterator()
meth public java.util.List<org.apache.lucene.index.SegmentCommitInfo> asList()
meth public java.util.Map<java.lang.String,java.lang.String> getUserData()
meth public long getGeneration()
meth public long getLastGeneration()
meth public long getVersion()
meth public org.apache.lucene.index.SegmentCommitInfo info(int)
meth public org.apache.lucene.index.SegmentInfos clone()
meth public org.apache.lucene.util.Version getCommitLuceneVersion()
meth public org.apache.lucene.util.Version getMinSegmentLuceneVersion()
meth public static java.io.PrintStream getInfoStream()
meth public static java.lang.String getLastCommitSegmentsFileName(java.lang.String[])
meth public static java.lang.String getLastCommitSegmentsFileName(org.apache.lucene.store.Directory) throws java.io.IOException
meth public static long generationFromSegmentsFileName(java.lang.String)
meth public static long getLastCommitGeneration(java.lang.String[])
meth public static long getLastCommitGeneration(org.apache.lucene.store.Directory) throws java.io.IOException
meth public static void setInfoStream(java.io.PrintStream)
meth public void add(org.apache.lucene.index.SegmentCommitInfo)
meth public void addAll(java.lang.Iterable<org.apache.lucene.index.SegmentCommitInfo>)
meth public void changed()
meth public void clear()
meth public void setNextWriteGeneration(long)
meth public void setUserData(java.util.Map<java.lang.String,java.lang.String>,boolean)
meth public void updateGeneration(org.apache.lucene.index.SegmentInfos)
meth public void write(org.apache.lucene.store.IndexOutput) throws java.io.IOException
supr java.lang.Object
hfds OLD_SEGMENTS_GEN,VERSION_CURRENT,generation,id,indexCreatedVersionMajor,infoStream,lastGeneration,luceneVersion,minSegmentLuceneVersion,pendingCommit,segments

CLSS public abstract static org.apache.lucene.index.SegmentInfos$FindSegmentsFile<%0 extends java.lang.Object>
 outer org.apache.lucene.index.SegmentInfos
cons protected init(org.apache.lucene.store.Directory)
meth protected abstract {org.apache.lucene.index.SegmentInfos$FindSegmentsFile%0} doBody(java.lang.String) throws java.io.IOException
meth public {org.apache.lucene.index.SegmentInfos$FindSegmentsFile%0} run() throws java.io.IOException
meth public {org.apache.lucene.index.SegmentInfos$FindSegmentsFile%0} run(org.apache.lucene.index.IndexCommit) throws java.io.IOException
supr java.lang.Object
hfds directory

CLSS public org.apache.lucene.index.SegmentReadState
cons public init(org.apache.lucene.index.SegmentReadState,java.lang.String)
cons public init(org.apache.lucene.store.Directory,org.apache.lucene.index.SegmentInfo,org.apache.lucene.index.FieldInfos,org.apache.lucene.store.IOContext)
cons public init(org.apache.lucene.store.Directory,org.apache.lucene.index.SegmentInfo,org.apache.lucene.index.FieldInfos,org.apache.lucene.store.IOContext,java.lang.String)
fld public final java.lang.String segmentSuffix
fld public final org.apache.lucene.index.FieldInfos fieldInfos
fld public final org.apache.lucene.index.SegmentInfo segmentInfo
fld public final org.apache.lucene.store.Directory directory
fld public final org.apache.lucene.store.IOContext context
supr java.lang.Object

CLSS public final org.apache.lucene.index.SegmentReader
meth protected void doClose() throws java.io.IOException
meth protected void notifyReaderClosedListeners() throws java.io.IOException
meth public final org.apache.lucene.index.Fields getTermVectors(int) throws java.io.IOException
meth public final void document(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
meth public int maxDoc()
meth public int numDocs()
meth public java.lang.String getSegmentName()
meth public java.lang.String toString()
meth public org.apache.lucene.codecs.DocValuesProducer getDocValuesReader()
meth public org.apache.lucene.codecs.FieldsProducer getPostingsReader()
meth public org.apache.lucene.codecs.KnnVectorsReader getVectorReader()
meth public org.apache.lucene.codecs.NormsProducer getNormsReader()
meth public org.apache.lucene.codecs.PointsReader getPointsReader()
meth public org.apache.lucene.codecs.StoredFieldsReader getFieldsReader()
meth public org.apache.lucene.codecs.TermVectorsReader getTermVectorsReader()
meth public org.apache.lucene.index.FieldInfos getFieldInfos()
meth public org.apache.lucene.index.IndexReader$CacheHelper getCoreCacheHelper()
meth public org.apache.lucene.index.IndexReader$CacheHelper getReaderCacheHelper()
meth public org.apache.lucene.index.LeafMetaData getMetaData()
meth public org.apache.lucene.index.SegmentCommitInfo getSegmentInfo()
meth public org.apache.lucene.store.Directory directory()
meth public org.apache.lucene.util.Bits getHardLiveDocs()
meth public org.apache.lucene.util.Bits getLiveDocs()
meth public void checkIntegrity() throws java.io.IOException
supr org.apache.lucene.index.CodecReader
hfds core,coreCacheHelper,docValuesProducer,fieldInfos,hardLiveDocs,isNRT,liveDocs,metaData,numDocs,originalSi,readerCacheHelper,readerClosedListeners,segDocValues,si

CLSS public org.apache.lucene.index.SegmentWriteState
cons public init(org.apache.lucene.index.SegmentWriteState,java.lang.String)
cons public init(org.apache.lucene.util.InfoStream,org.apache.lucene.store.Directory,org.apache.lucene.index.SegmentInfo,org.apache.lucene.index.FieldInfos,org.apache.lucene.index.BufferedUpdates,org.apache.lucene.store.IOContext)
cons public init(org.apache.lucene.util.InfoStream,org.apache.lucene.store.Directory,org.apache.lucene.index.SegmentInfo,org.apache.lucene.index.FieldInfos,org.apache.lucene.index.BufferedUpdates,org.apache.lucene.store.IOContext,java.lang.String)
fld public final java.lang.String segmentSuffix
fld public final org.apache.lucene.index.FieldInfos fieldInfos
fld public final org.apache.lucene.index.SegmentInfo segmentInfo
fld public final org.apache.lucene.store.Directory directory
fld public final org.apache.lucene.store.IOContext context
fld public final org.apache.lucene.util.Accountable segUpdates
fld public final org.apache.lucene.util.InfoStream infoStream
fld public int delCountOnFlush
fld public int softDelCountOnFlush
fld public org.apache.lucene.util.FixedBitSet liveDocs
supr java.lang.Object

CLSS public org.apache.lucene.index.SerialMergeScheduler
cons public init()
meth public void close()
meth public void merge(org.apache.lucene.index.MergeScheduler$MergeSource,org.apache.lucene.index.MergeTrigger) throws java.io.IOException
supr org.apache.lucene.index.MergeScheduler

CLSS public org.apache.lucene.index.SimpleMergedSegmentWarmer
cons public init(org.apache.lucene.util.InfoStream)
intf org.apache.lucene.index.IndexWriter$IndexReaderWarmer
meth public void warm(org.apache.lucene.index.LeafReader) throws java.io.IOException
supr java.lang.Object
hfds infoStream

CLSS public final org.apache.lucene.index.SingleTermsEnum
cons public init(org.apache.lucene.index.TermsEnum,org.apache.lucene.util.BytesRef)
meth protected org.apache.lucene.index.FilteredTermsEnum$AcceptStatus accept(org.apache.lucene.util.BytesRef)
supr org.apache.lucene.index.FilteredTermsEnum
hfds singleRef

CLSS public final org.apache.lucene.index.SlowCodecReaderWrapper
meth public static org.apache.lucene.index.CodecReader wrap(org.apache.lucene.index.LeafReader) throws java.io.IOException
supr java.lang.Object

CLSS public final org.apache.lucene.index.SlowImpactsEnum
cons public init(org.apache.lucene.index.PostingsEnum)
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int endOffset() throws java.io.IOException
meth public int freq() throws java.io.IOException
meth public int nextDoc() throws java.io.IOException
meth public int nextPosition() throws java.io.IOException
meth public int startOffset() throws java.io.IOException
meth public long cost()
meth public org.apache.lucene.index.Impacts getImpacts()
meth public org.apache.lucene.util.BytesRef getPayload() throws java.io.IOException
meth public void advanceShallow(int)
supr org.apache.lucene.index.ImpactsEnum
hfds DUMMY_IMPACTS,delegate

CLSS public org.apache.lucene.index.SnapshotDeletionPolicy
cons public init(org.apache.lucene.index.IndexDeletionPolicy)
fld protected final java.util.Map<java.lang.Long,java.lang.Integer> refCounts
fld protected final java.util.Map<java.lang.Long,org.apache.lucene.index.IndexCommit> indexCommits
fld protected org.apache.lucene.index.IndexCommit lastCommit
meth protected void incRef(org.apache.lucene.index.IndexCommit)
meth protected void releaseGen(long) throws java.io.IOException
meth public int getSnapshotCount()
meth public java.util.List<org.apache.lucene.index.IndexCommit> getSnapshots()
meth public org.apache.lucene.index.IndexCommit getIndexCommit(long)
meth public org.apache.lucene.index.IndexCommit snapshot() throws java.io.IOException
meth public void onCommit(java.util.List<? extends org.apache.lucene.index.IndexCommit>) throws java.io.IOException
meth public void onInit(java.util.List<? extends org.apache.lucene.index.IndexCommit>) throws java.io.IOException
meth public void release(org.apache.lucene.index.IndexCommit) throws java.io.IOException
supr org.apache.lucene.index.IndexDeletionPolicy
hfds initCalled,primary
hcls SnapshotCommitPoint

CLSS public final org.apache.lucene.index.SoftDeletesDirectoryReaderWrapper
cons public init(org.apache.lucene.index.DirectoryReader,java.lang.String) throws java.io.IOException
meth protected org.apache.lucene.index.DirectoryReader doWrapDirectoryReader(org.apache.lucene.index.DirectoryReader) throws java.io.IOException
meth public org.apache.lucene.index.IndexReader$CacheHelper getReaderCacheHelper()
supr org.apache.lucene.index.FilterDirectoryReader
hfds field,readerCacheHelper
hcls SoftDeletesFilterCodecReader,SoftDeletesFilterLeafReader,SoftDeletesSubReaderWrapper

CLSS public final org.apache.lucene.index.SoftDeletesRetentionMergePolicy
cons public init(java.lang.String,java.util.function.Supplier<org.apache.lucene.search.Query>,org.apache.lucene.index.MergePolicy)
meth public boolean keepFullyDeletedSegment(org.apache.lucene.util.IOSupplier<org.apache.lucene.index.CodecReader>) throws java.io.IOException
meth public int numDeletesToMerge(org.apache.lucene.index.SegmentCommitInfo,int,org.apache.lucene.util.IOSupplier<org.apache.lucene.index.CodecReader>) throws java.io.IOException
supr org.apache.lucene.index.OneMergeWrappingMergePolicy
hfds field,retentionQuerySupplier

CLSS public abstract org.apache.lucene.index.SortFieldProvider
cons protected init(java.lang.String)
fld protected final java.lang.String name
intf org.apache.lucene.util.NamedSPILoader$NamedSPI
meth public abstract org.apache.lucene.search.SortField readSortField(org.apache.lucene.store.DataInput) throws java.io.IOException
meth public abstract void writeSortField(org.apache.lucene.search.SortField,org.apache.lucene.store.DataOutput) throws java.io.IOException
meth public java.lang.String getName()
meth public static java.util.Set<java.lang.String> availableSortFieldProviders()
meth public static org.apache.lucene.index.SortFieldProvider forName(java.lang.String)
meth public static void reloadSortFieldProviders(java.lang.ClassLoader)
meth public static void write(org.apache.lucene.search.SortField,org.apache.lucene.store.DataOutput) throws java.io.IOException
supr java.lang.Object
hcls Holder

CLSS public abstract org.apache.lucene.index.SortedDocValues
cons protected init()
meth public abstract boolean advanceExact(int) throws java.io.IOException
meth public abstract int getValueCount()
meth public abstract int ordValue() throws java.io.IOException
meth public abstract org.apache.lucene.util.BytesRef lookupOrd(int) throws java.io.IOException
meth public int lookupTerm(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum intersect(org.apache.lucene.util.automaton.CompiledAutomaton) throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum termsEnum() throws java.io.IOException
supr org.apache.lucene.search.DocIdSetIterator

CLSS public abstract org.apache.lucene.index.SortedNumericDocValues
cons protected init()
meth public abstract boolean advanceExact(int) throws java.io.IOException
meth public abstract int docValueCount()
meth public abstract long nextValue() throws java.io.IOException
supr org.apache.lucene.search.DocIdSetIterator

CLSS public abstract org.apache.lucene.index.SortedSetDocValues
cons protected init()
fld public final static long NO_MORE_ORDS = -1
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public abstract boolean advanceExact(int) throws java.io.IOException
meth public abstract int docValueCount()
meth public abstract long getValueCount()
meth public abstract long nextOrd() throws java.io.IOException
meth public abstract org.apache.lucene.util.BytesRef lookupOrd(long) throws java.io.IOException
meth public long lookupTerm(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum intersect(org.apache.lucene.util.automaton.CompiledAutomaton) throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum termsEnum() throws java.io.IOException
supr org.apache.lucene.search.DocIdSetIterator

CLSS public final org.apache.lucene.index.Sorter
innr public abstract static DocMap
meth public java.lang.String getID()
meth public java.lang.String toString()
supr java.lang.Object
hfds sort
hcls DocValueSorter

CLSS public abstract static org.apache.lucene.index.Sorter$DocMap
 outer org.apache.lucene.index.Sorter
cons protected init()
meth public abstract int newToOld(int)
meth public abstract int oldToNew(int)
meth public abstract int size()
supr java.lang.Object

CLSS public final org.apache.lucene.index.SortingCodecReader
meth public java.lang.String toString()
meth public org.apache.lucene.codecs.DocValuesProducer getDocValuesReader()
meth public org.apache.lucene.codecs.FieldsProducer getPostingsReader()
meth public org.apache.lucene.codecs.KnnVectorsReader getVectorReader()
meth public org.apache.lucene.codecs.NormsProducer getNormsReader()
meth public org.apache.lucene.codecs.PointsReader getPointsReader()
meth public org.apache.lucene.codecs.StoredFieldsReader getFieldsReader()
meth public org.apache.lucene.codecs.TermVectorsReader getTermVectorsReader()
meth public org.apache.lucene.index.IndexReader$CacheHelper getCoreCacheHelper()
meth public org.apache.lucene.index.IndexReader$CacheHelper getReaderCacheHelper()
meth public org.apache.lucene.index.LeafMetaData getMetaData()
meth public org.apache.lucene.util.Bits getLiveDocs()
meth public static org.apache.lucene.index.CodecReader wrap(org.apache.lucene.index.CodecReader,org.apache.lucene.index.Sorter$DocMap,org.apache.lucene.search.Sort)
meth public static org.apache.lucene.index.CodecReader wrap(org.apache.lucene.index.CodecReader,org.apache.lucene.search.Sort) throws java.io.IOException
supr org.apache.lucene.index.FilterCodecReader
hfds cacheIsNorms,cacheStats,cachedField,cachedObject,docMap,metaData
hcls SortingBits,SortingByteVectorValues,SortingFloatVectorValues,SortingIntersectVisitor,SortingPointTree,SortingPointValues

CLSS public final org.apache.lucene.index.StandardDirectoryReader
meth protected org.apache.lucene.index.DirectoryReader doOpenIfChanged() throws java.io.IOException
meth protected org.apache.lucene.index.DirectoryReader doOpenIfChanged(org.apache.lucene.index.IndexCommit) throws java.io.IOException
meth protected org.apache.lucene.index.DirectoryReader doOpenIfChanged(org.apache.lucene.index.IndexWriter,boolean) throws java.io.IOException
meth protected void doClose() throws java.io.IOException
meth protected void notifyReaderClosedListeners() throws java.io.IOException
meth public boolean isCurrent() throws java.io.IOException
meth public java.lang.String toString()
meth public long getVersion()
meth public org.apache.lucene.index.IndexCommit getIndexCommit() throws java.io.IOException
meth public org.apache.lucene.index.IndexReader$CacheHelper getReaderCacheHelper()
meth public org.apache.lucene.index.SegmentInfos getSegmentInfos()
meth public static org.apache.lucene.index.DirectoryReader open(org.apache.lucene.store.Directory,org.apache.lucene.index.SegmentInfos,java.util.List<? extends org.apache.lucene.index.LeafReader>,java.util.Comparator<org.apache.lucene.index.LeafReader>) throws java.io.IOException
supr org.apache.lucene.index.DirectoryReader
hfds applyAllDeletes,cacheHelper,readerClosedListeners,segmentInfos,writeAllDeletes,writer
hcls ReaderCommit

CLSS public abstract org.apache.lucene.index.StoredFieldVisitor
cons protected init()
innr public final static !enum Status
meth public abstract org.apache.lucene.index.StoredFieldVisitor$Status needsField(org.apache.lucene.index.FieldInfo) throws java.io.IOException
meth public void binaryField(org.apache.lucene.index.FieldInfo,byte[]) throws java.io.IOException
meth public void binaryField(org.apache.lucene.index.FieldInfo,org.apache.lucene.store.DataInput,int) throws java.io.IOException
meth public void doubleField(org.apache.lucene.index.FieldInfo,double) throws java.io.IOException
meth public void floatField(org.apache.lucene.index.FieldInfo,float) throws java.io.IOException
meth public void intField(org.apache.lucene.index.FieldInfo,int) throws java.io.IOException
meth public void longField(org.apache.lucene.index.FieldInfo,long) throws java.io.IOException
meth public void stringField(org.apache.lucene.index.FieldInfo,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public final static !enum org.apache.lucene.index.StoredFieldVisitor$Status
 outer org.apache.lucene.index.StoredFieldVisitor
fld public final static org.apache.lucene.index.StoredFieldVisitor$Status NO
fld public final static org.apache.lucene.index.StoredFieldVisitor$Status STOP
fld public final static org.apache.lucene.index.StoredFieldVisitor$Status YES
meth public static org.apache.lucene.index.StoredFieldVisitor$Status valueOf(java.lang.String)
meth public static org.apache.lucene.index.StoredFieldVisitor$Status[] values()
supr java.lang.Enum<org.apache.lucene.index.StoredFieldVisitor$Status>

CLSS public abstract org.apache.lucene.index.StoredFields
cons protected init()
meth public abstract void document(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
meth public final org.apache.lucene.document.Document document(int) throws java.io.IOException
meth public final org.apache.lucene.document.Document document(int,java.util.Set<java.lang.String>) throws java.io.IOException
supr java.lang.Object

CLSS public final org.apache.lucene.index.Term
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,org.apache.lucene.util.BytesRef)
cons public init(java.lang.String,org.apache.lucene.util.BytesRefBuilder)
intf java.lang.Comparable<org.apache.lucene.index.Term>
intf org.apache.lucene.util.Accountable
meth public boolean equals(java.lang.Object)
meth public final int compareTo(org.apache.lucene.index.Term)
meth public final java.lang.String field()
meth public final java.lang.String text()
meth public final java.lang.String toString()
meth public final org.apache.lucene.util.BytesRef bytes()
meth public final static java.lang.String toString(org.apache.lucene.util.BytesRef)
meth public int hashCode()
meth public long ramBytesUsed()
supr java.lang.Object
hfds BASE_RAM_BYTES,bytes,field

CLSS public abstract org.apache.lucene.index.TermState
cons protected init()
intf java.lang.Cloneable
meth public abstract void copyFrom(org.apache.lucene.index.TermState)
meth public java.lang.String toString()
meth public org.apache.lucene.index.TermState clone()
supr java.lang.Object

CLSS public final org.apache.lucene.index.TermStates
cons public init(org.apache.lucene.index.IndexReaderContext)
cons public init(org.apache.lucene.index.IndexReaderContext,org.apache.lucene.index.TermState,int,int,long)
meth public boolean wasBuiltFor(org.apache.lucene.index.IndexReaderContext)
meth public int docFreq()
meth public java.lang.String toString()
meth public long totalTermFreq()
meth public org.apache.lucene.index.TermState get(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public static org.apache.lucene.index.TermStates build(org.apache.lucene.search.IndexSearcher,org.apache.lucene.index.Term,boolean) throws java.io.IOException
meth public void accumulateStatistics(int,long)
meth public void clear()
meth public void register(org.apache.lucene.index.TermState,int)
meth public void register(org.apache.lucene.index.TermState,int,int,long)
supr java.lang.Object
hfds EMPTY_TERMSTATE,docFreq,states,term,topReaderContextIdentity,totalTermFreq
hcls TermStateInfo

CLSS public abstract org.apache.lucene.index.TermVectors
cons protected init()
fld public final static org.apache.lucene.index.TermVectors EMPTY
meth public abstract org.apache.lucene.index.Fields get(int) throws java.io.IOException
meth public final org.apache.lucene.index.Terms get(int,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.apache.lucene.index.Terms
cons protected init()
fld public final static org.apache.lucene.index.Terms[] EMPTY_ARRAY
meth public abstract boolean hasFreqs()
meth public abstract boolean hasOffsets()
meth public abstract boolean hasPayloads()
meth public abstract boolean hasPositions()
meth public abstract int getDocCount() throws java.io.IOException
meth public abstract long getSumDocFreq() throws java.io.IOException
meth public abstract long getSumTotalTermFreq() throws java.io.IOException
meth public abstract long size() throws java.io.IOException
meth public abstract org.apache.lucene.index.TermsEnum iterator() throws java.io.IOException
meth public java.lang.Object getStats() throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum intersect(org.apache.lucene.util.automaton.CompiledAutomaton,org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public org.apache.lucene.util.BytesRef getMax() throws java.io.IOException
meth public org.apache.lucene.util.BytesRef getMin() throws java.io.IOException
meth public static org.apache.lucene.index.Terms getTerms(org.apache.lucene.index.LeafReader,java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds EMPTY

CLSS public abstract org.apache.lucene.index.TermsEnum
cons protected init()
fld public final static org.apache.lucene.index.TermsEnum EMPTY
innr public final static !enum SeekStatus
intf org.apache.lucene.util.BytesRefIterator
meth public abstract boolean seekExact(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public abstract int docFreq() throws java.io.IOException
meth public abstract long ord() throws java.io.IOException
meth public abstract long totalTermFreq() throws java.io.IOException
meth public abstract org.apache.lucene.index.ImpactsEnum impacts(int) throws java.io.IOException
meth public abstract org.apache.lucene.index.PostingsEnum postings(org.apache.lucene.index.PostingsEnum,int) throws java.io.IOException
meth public abstract org.apache.lucene.index.TermState termState() throws java.io.IOException
meth public abstract org.apache.lucene.index.TermsEnum$SeekStatus seekCeil(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public abstract org.apache.lucene.util.AttributeSource attributes()
meth public abstract org.apache.lucene.util.BytesRef term() throws java.io.IOException
meth public abstract void seekExact(long) throws java.io.IOException
meth public abstract void seekExact(org.apache.lucene.util.BytesRef,org.apache.lucene.index.TermState) throws java.io.IOException
meth public final org.apache.lucene.index.PostingsEnum postings(org.apache.lucene.index.PostingsEnum) throws java.io.IOException
supr java.lang.Object

CLSS public final static !enum org.apache.lucene.index.TermsEnum$SeekStatus
 outer org.apache.lucene.index.TermsEnum
fld public final static org.apache.lucene.index.TermsEnum$SeekStatus END
fld public final static org.apache.lucene.index.TermsEnum$SeekStatus FOUND
fld public final static org.apache.lucene.index.TermsEnum$SeekStatus NOT_FOUND
meth public static org.apache.lucene.index.TermsEnum$SeekStatus valueOf(java.lang.String)
meth public static org.apache.lucene.index.TermsEnum$SeekStatus[] values()
supr java.lang.Enum<org.apache.lucene.index.TermsEnum$SeekStatus>

CLSS public org.apache.lucene.index.TieredMergePolicy
cons public init()
fld public final static double DEFAULT_NO_CFS_RATIO = 0.1
innr protected abstract static MergeScore
meth protected long maxFullFlushMergeSize()
meth protected org.apache.lucene.index.TieredMergePolicy$MergeScore score(java.util.List<org.apache.lucene.index.SegmentCommitInfo>,boolean,java.util.Map<org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.TieredMergePolicy$SegmentSizeAndDocs>) throws java.io.IOException
meth public double getDeletesPctAllowed()
meth public double getFloorSegmentMB()
meth public double getForceMergeDeletesPctAllowed()
meth public double getMaxMergedSegmentMB()
meth public double getSegmentsPerTier()
meth public int getMaxMergeAtOnce()
meth public java.lang.String toString()
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedDeletesMerges(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedMerges(org.apache.lucene.index.SegmentInfos,int,java.util.Map<org.apache.lucene.index.SegmentCommitInfo,java.lang.Boolean>,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public org.apache.lucene.index.TieredMergePolicy setDeletesPctAllowed(double)
meth public org.apache.lucene.index.TieredMergePolicy setFloorSegmentMB(double)
meth public org.apache.lucene.index.TieredMergePolicy setForceMergeDeletesPctAllowed(double)
meth public org.apache.lucene.index.TieredMergePolicy setMaxMergeAtOnce(int)
meth public org.apache.lucene.index.TieredMergePolicy setMaxMergedSegmentMB(double)
meth public org.apache.lucene.index.TieredMergePolicy setSegmentsPerTier(double)
supr org.apache.lucene.index.MergePolicy
hfds deletesPctAllowed,floorSegmentBytes,forceMergeDeletesPctAllowed,maxMergeAtOnce,maxMergedSegmentBytes,segsPerTier
hcls MERGE_TYPE,SegmentSizeAndDocs

CLSS protected abstract static org.apache.lucene.index.TieredMergePolicy$MergeScore
 outer org.apache.lucene.index.TieredMergePolicy
cons protected init()
supr java.lang.Object

CLSS public abstract interface org.apache.lucene.index.TwoPhaseCommit
meth public abstract long commit() throws java.io.IOException
meth public abstract long prepareCommit() throws java.io.IOException
meth public abstract void rollback() throws java.io.IOException

CLSS public final org.apache.lucene.index.TwoPhaseCommitTool
innr public static CommitFailException
innr public static PrepareCommitFailException
meth public !varargs static void execute(org.apache.lucene.index.TwoPhaseCommit[]) throws org.apache.lucene.index.TwoPhaseCommitTool$CommitFailException,org.apache.lucene.index.TwoPhaseCommitTool$PrepareCommitFailException
supr java.lang.Object

CLSS public static org.apache.lucene.index.TwoPhaseCommitTool$CommitFailException
 outer org.apache.lucene.index.TwoPhaseCommitTool
cons public init(java.lang.Throwable,org.apache.lucene.index.TwoPhaseCommit)
supr java.io.IOException

CLSS public static org.apache.lucene.index.TwoPhaseCommitTool$PrepareCommitFailException
 outer org.apache.lucene.index.TwoPhaseCommitTool
cons public init(java.lang.Throwable,org.apache.lucene.index.TwoPhaseCommit)
supr java.io.IOException

CLSS public org.apache.lucene.index.UpgradeIndexMergePolicy
cons public init(org.apache.lucene.index.MergePolicy)
meth protected boolean shouldUpgradeSegment(org.apache.lucene.index.SegmentCommitInfo)
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedMerges(org.apache.lucene.index.SegmentInfos,int,java.util.Map<org.apache.lucene.index.SegmentCommitInfo,java.lang.Boolean>,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.MergePolicy$MergeContext) throws java.io.IOException
supr org.apache.lucene.index.FilterMergePolicy

CLSS public final !enum org.apache.lucene.index.VectorEncoding
fld public final int byteSize
fld public final static org.apache.lucene.index.VectorEncoding BYTE
fld public final static org.apache.lucene.index.VectorEncoding FLOAT32
meth public static org.apache.lucene.index.VectorEncoding valueOf(java.lang.String)
meth public static org.apache.lucene.index.VectorEncoding[] values()
supr java.lang.Enum<org.apache.lucene.index.VectorEncoding>

CLSS public abstract !enum org.apache.lucene.index.VectorSimilarityFunction
fld public final static org.apache.lucene.index.VectorSimilarityFunction COSINE
fld public final static org.apache.lucene.index.VectorSimilarityFunction DOT_PRODUCT
fld public final static org.apache.lucene.index.VectorSimilarityFunction EUCLIDEAN
fld public final static org.apache.lucene.index.VectorSimilarityFunction MAXIMUM_INNER_PRODUCT
meth public abstract float compare(byte[],byte[])
meth public abstract float compare(float[],float[])
meth public static org.apache.lucene.index.VectorSimilarityFunction valueOf(java.lang.String)
meth public static org.apache.lucene.index.VectorSimilarityFunction[] values()
supr java.lang.Enum<org.apache.lucene.index.VectorSimilarityFunction>

CLSS public abstract org.apache.lucene.index.VectorValues
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
supr org.apache.lucene.index.FloatVectorValues

CLSS public abstract org.apache.lucene.search.AbstractKnnCollector
cons protected init(int,long)
intf org.apache.lucene.search.KnnCollector
meth public abstract boolean collect(int,float)
meth public abstract float minCompetitiveSimilarity()
meth public abstract org.apache.lucene.search.TopDocs topDocs()
meth public final boolean earlyTerminated()
meth public final int k()
meth public final long visitLimit()
meth public final long visitedCount()
meth public final void incVisitedCount(int)
supr java.lang.Object
hfds k,visitLimit,visitedCount

CLSS public org.apache.lucene.search.AutomatonQuery
cons public init(org.apache.lucene.index.Term,org.apache.lucene.util.automaton.Automaton)
cons public init(org.apache.lucene.index.Term,org.apache.lucene.util.automaton.Automaton,int)
cons public init(org.apache.lucene.index.Term,org.apache.lucene.util.automaton.Automaton,int,boolean)
cons public init(org.apache.lucene.index.Term,org.apache.lucene.util.automaton.Automaton,int,boolean,org.apache.lucene.search.MultiTermQuery$RewriteMethod)
fld protected final boolean automatonIsBinary
fld protected final org.apache.lucene.index.Term term
fld protected final org.apache.lucene.util.automaton.Automaton automaton
fld protected final org.apache.lucene.util.automaton.CompiledAutomaton compiled
intf org.apache.lucene.util.Accountable
meth protected org.apache.lucene.index.TermsEnum getTermsEnum(org.apache.lucene.index.Terms,org.apache.lucene.util.AttributeSource) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public boolean isAutomatonBinary()
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public long ramBytesUsed()
meth public org.apache.lucene.util.automaton.Automaton getAutomaton()
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.MultiTermQuery
hfds BASE_RAM_BYTES,ramBytesUsed

CLSS public final org.apache.lucene.search.BlendedTermQuery
fld public final static org.apache.lucene.search.BlendedTermQuery$RewriteMethod BOOLEAN_REWRITE
fld public final static org.apache.lucene.search.BlendedTermQuery$RewriteMethod DISJUNCTION_MAX_REWRITE
innr public abstract static RewriteMethod
innr public static Builder
innr public static DisjunctionMaxRewrite
meth public boolean equals(java.lang.Object)
meth public final org.apache.lucene.search.Query rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds boosts,contexts,rewriteMethod,terms

CLSS public static org.apache.lucene.search.BlendedTermQuery$Builder
 outer org.apache.lucene.search.BlendedTermQuery
cons public init()
meth public org.apache.lucene.search.BlendedTermQuery build()
meth public org.apache.lucene.search.BlendedTermQuery$Builder add(org.apache.lucene.index.Term)
meth public org.apache.lucene.search.BlendedTermQuery$Builder add(org.apache.lucene.index.Term,float)
meth public org.apache.lucene.search.BlendedTermQuery$Builder add(org.apache.lucene.index.Term,float,org.apache.lucene.index.TermStates)
meth public org.apache.lucene.search.BlendedTermQuery$Builder setRewriteMethod(org.apache.lucene.search.BlendedTermQuery$RewriteMethod)
supr java.lang.Object
hfds boosts,contexts,numTerms,rewriteMethod,terms

CLSS public static org.apache.lucene.search.BlendedTermQuery$DisjunctionMaxRewrite
 outer org.apache.lucene.search.BlendedTermQuery
cons public init(float)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.Query[])
supr org.apache.lucene.search.BlendedTermQuery$RewriteMethod
hfds tieBreakerMultiplier

CLSS public abstract static org.apache.lucene.search.BlendedTermQuery$RewriteMethod
 outer org.apache.lucene.search.BlendedTermQuery
cons protected init()
meth public abstract org.apache.lucene.search.Query rewrite(org.apache.lucene.search.Query[])
supr java.lang.Object

CLSS public final org.apache.lucene.search.BooleanClause
cons public init(org.apache.lucene.search.Query,org.apache.lucene.search.BooleanClause$Occur)
innr public static !enum Occur
meth public boolean equals(java.lang.Object)
meth public boolean isProhibited()
meth public boolean isRequired()
meth public boolean isScoring()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.apache.lucene.search.BooleanClause$Occur getOccur()
meth public org.apache.lucene.search.Query getQuery()
supr java.lang.Object
hfds occur,query

CLSS public static !enum org.apache.lucene.search.BooleanClause$Occur
 outer org.apache.lucene.search.BooleanClause
fld public final static org.apache.lucene.search.BooleanClause$Occur FILTER
fld public final static org.apache.lucene.search.BooleanClause$Occur MUST
fld public final static org.apache.lucene.search.BooleanClause$Occur MUST_NOT
fld public final static org.apache.lucene.search.BooleanClause$Occur SHOULD
meth public static org.apache.lucene.search.BooleanClause$Occur valueOf(java.lang.String)
meth public static org.apache.lucene.search.BooleanClause$Occur[] values()
supr java.lang.Enum<org.apache.lucene.search.BooleanClause$Occur>

CLSS public org.apache.lucene.search.BooleanQuery
innr public static Builder
innr public static TooManyClauses
intf java.lang.Iterable<org.apache.lucene.search.BooleanClause>
meth public boolean equals(java.lang.Object)
meth public final java.util.Iterator<org.apache.lucene.search.BooleanClause> iterator()
meth public int getMinimumNumberShouldMatch()
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public java.util.List<org.apache.lucene.search.BooleanClause> clauses()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public static int getMaxClauseCount()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static void setMaxClauseCount(int)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds clauseSets,clauses,hashCode,minimumNumberShouldMatch

CLSS public static org.apache.lucene.search.BooleanQuery$Builder
 outer org.apache.lucene.search.BooleanQuery
cons public init()
meth public org.apache.lucene.search.BooleanQuery build()
meth public org.apache.lucene.search.BooleanQuery$Builder add(org.apache.lucene.search.BooleanClause)
meth public org.apache.lucene.search.BooleanQuery$Builder add(org.apache.lucene.search.Query,org.apache.lucene.search.BooleanClause$Occur)
meth public org.apache.lucene.search.BooleanQuery$Builder setMinimumNumberShouldMatch(int)
supr java.lang.Object
hfds clauses,minimumNumberShouldMatch

CLSS public static org.apache.lucene.search.BooleanQuery$TooManyClauses
 outer org.apache.lucene.search.BooleanQuery
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init()
supr org.apache.lucene.search.IndexSearcher$TooManyClauses

CLSS public abstract interface org.apache.lucene.search.BoostAttribute
fld public final static float DEFAULT_BOOST = 1.0
intf org.apache.lucene.util.Attribute
meth public abstract float getBoost()
meth public abstract void setBoost(float)

CLSS public final org.apache.lucene.search.BoostAttributeImpl
cons public init()
intf org.apache.lucene.search.BoostAttribute
meth public float getBoost()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public void setBoost(float)
supr org.apache.lucene.util.AttributeImpl
hfds boost

CLSS public final org.apache.lucene.search.BoostQuery
cons public init(org.apache.lucene.search.Query,float)
meth public boolean equals(java.lang.Object)
meth public float getBoost()
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query getQuery()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds boost,query

CLSS public abstract org.apache.lucene.search.BulkScorer
cons public init()
meth public abstract int score(org.apache.lucene.search.LeafCollector,org.apache.lucene.util.Bits,int,int) throws java.io.IOException
meth public abstract long cost()
meth public void score(org.apache.lucene.search.LeafCollector,org.apache.lucene.util.Bits) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.CachingCollector
meth public abstract void replay(org.apache.lucene.search.Collector) throws java.io.IOException
meth public final boolean isCached()
meth public static org.apache.lucene.search.CachingCollector create(boolean,double)
meth public static org.apache.lucene.search.CachingCollector create(org.apache.lucene.search.Collector,boolean,double)
meth public static org.apache.lucene.search.CachingCollector create(org.apache.lucene.search.Collector,boolean,int)
supr org.apache.lucene.search.FilterCollector
hfds INITIAL_ARRAY_SIZE,cached
hcls CachedScorable,NoScoreCachingCollector,NoScoreCachingLeafCollector,ScoreCachingCollector,ScoreCachingLeafCollector

CLSS public abstract interface org.apache.lucene.search.CheckedIntConsumer<%0 extends java.lang.Exception>
 anno 0 java.lang.FunctionalInterface()
meth public abstract void accept(int) throws {org.apache.lucene.search.CheckedIntConsumer%0}

CLSS public org.apache.lucene.search.CollectionStatistics
cons public init(java.lang.String,long,long,long,long)
meth public final java.lang.String field()
meth public final long docCount()
meth public final long maxDoc()
meth public final long sumDocFreq()
meth public final long sumTotalTermFreq()
meth public java.lang.String toString()
supr java.lang.Object
hfds docCount,field,maxDoc,sumDocFreq,sumTotalTermFreq

CLSS public final org.apache.lucene.search.CollectionTerminatedException
cons public init()
meth public java.lang.Throwable fillInStackTrace()
supr java.lang.RuntimeException

CLSS public abstract interface org.apache.lucene.search.Collector
meth public abstract org.apache.lucene.search.LeafCollector getLeafCollector(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public abstract org.apache.lucene.search.ScoreMode scoreMode()
meth public void setWeight(org.apache.lucene.search.Weight)

CLSS public abstract interface org.apache.lucene.search.CollectorManager<%0 extends org.apache.lucene.search.Collector, %1 extends java.lang.Object>
meth public abstract {org.apache.lucene.search.CollectorManager%0} newCollector() throws java.io.IOException
meth public abstract {org.apache.lucene.search.CollectorManager%1} reduce(java.util.Collection<{org.apache.lucene.search.CollectorManager%0}>) throws java.io.IOException

CLSS public final org.apache.lucene.search.ConjunctionUtils
cons public init()
meth public static org.apache.lucene.search.DocIdSetIterator createConjunction(java.util.List<org.apache.lucene.search.DocIdSetIterator>,java.util.List<org.apache.lucene.search.TwoPhaseIterator>)
meth public static org.apache.lucene.search.DocIdSetIterator intersectIterators(java.util.List<? extends org.apache.lucene.search.DocIdSetIterator>)
meth public static org.apache.lucene.search.DocIdSetIterator intersectScorers(java.util.Collection<org.apache.lucene.search.Scorer>)
meth public static void addIterator(org.apache.lucene.search.DocIdSetIterator,java.util.List<org.apache.lucene.search.DocIdSetIterator>,java.util.List<org.apache.lucene.search.TwoPhaseIterator>)
meth public static void addTwoPhaseIterator(org.apache.lucene.search.TwoPhaseIterator,java.util.List<org.apache.lucene.search.DocIdSetIterator>,java.util.List<org.apache.lucene.search.TwoPhaseIterator>)
supr java.lang.Object

CLSS public final org.apache.lucene.search.ConstantScoreQuery
cons public init(org.apache.lucene.search.Query)
innr protected static ConstantBulkScorer
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query getQuery()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds query

CLSS protected static org.apache.lucene.search.ConstantScoreQuery$ConstantBulkScorer
 outer org.apache.lucene.search.ConstantScoreQuery
cons public init(org.apache.lucene.search.BulkScorer,org.apache.lucene.search.Weight,float)
meth public int score(org.apache.lucene.search.LeafCollector,org.apache.lucene.util.Bits,int,int) throws java.io.IOException
meth public long cost()
supr org.apache.lucene.search.BulkScorer
hfds bulkScorer,theScore,weight

CLSS public final org.apache.lucene.search.ConstantScoreScorer
cons public init(org.apache.lucene.search.Weight,float,org.apache.lucene.search.ScoreMode,org.apache.lucene.search.DocIdSetIterator)
cons public init(org.apache.lucene.search.Weight,float,org.apache.lucene.search.ScoreMode,org.apache.lucene.search.TwoPhaseIterator)
meth public float getMaxScore(int) throws java.io.IOException
meth public float score() throws java.io.IOException
meth public int docID()
meth public org.apache.lucene.search.DocIdSetIterator iterator()
meth public org.apache.lucene.search.TwoPhaseIterator twoPhaseIterator()
meth public void setMinCompetitiveScore(float) throws java.io.IOException
supr org.apache.lucene.search.Scorer
hfds approximation,disi,score,scoreMode,twoPhaseIterator
hcls DocIdSetIteratorWrapper

CLSS public abstract org.apache.lucene.search.ConstantScoreWeight
cons protected init(org.apache.lucene.search.Query,float)
meth protected final float score()
meth public org.apache.lucene.search.Explanation explain(org.apache.lucene.index.LeafReaderContext,int) throws java.io.IOException
supr org.apache.lucene.search.Weight
hfds score

CLSS public org.apache.lucene.search.ControlledRealTimeReopenThread<%0 extends java.lang.Object>
cons public init(org.apache.lucene.index.IndexWriter,org.apache.lucene.search.ReferenceManager<{org.apache.lucene.search.ControlledRealTimeReopenThread%0}>,double,double)
intf java.io.Closeable
meth public boolean waitForGeneration(long,int) throws java.lang.InterruptedException
meth public long getSearchingGen()
meth public void close()
meth public void run()
meth public void waitForGeneration(long) throws java.lang.InterruptedException
supr java.lang.Thread
hfds finish,manager,refreshStartGen,reopenCond,reopenLock,searchingGen,targetMaxStaleNS,targetMinStaleNS,waitingGen,writer
hcls HandleRefresh

CLSS public final org.apache.lucene.search.DisiPriorityQueue
cons public init(int)
intf java.lang.Iterable<org.apache.lucene.search.DisiWrapper>
meth public int size()
meth public java.util.Iterator<org.apache.lucene.search.DisiWrapper> iterator()
meth public org.apache.lucene.search.DisiWrapper add(org.apache.lucene.search.DisiWrapper)
meth public org.apache.lucene.search.DisiWrapper pop()
meth public org.apache.lucene.search.DisiWrapper top()
meth public org.apache.lucene.search.DisiWrapper top2()
meth public org.apache.lucene.search.DisiWrapper topList()
meth public org.apache.lucene.search.DisiWrapper updateTop()
meth public void addAll(org.apache.lucene.search.DisiWrapper[],int,int)
meth public void clear()
supr java.lang.Object
hfds heap,size

CLSS public org.apache.lucene.search.DisiWrapper
cons public init(org.apache.lucene.search.Scorer)
fld public final float matchCost
fld public final long cost
fld public final org.apache.lucene.search.DocIdSetIterator approximation
fld public final org.apache.lucene.search.DocIdSetIterator iterator
fld public final org.apache.lucene.search.Scorer scorer
fld public final org.apache.lucene.search.TwoPhaseIterator twoPhaseView
fld public int doc
fld public org.apache.lucene.search.DisiWrapper next
supr java.lang.Object
hfds maxWindowScore,scaledMaxScore

CLSS public org.apache.lucene.search.DisjunctionDISIApproximation
cons public init(org.apache.lucene.search.DisiPriorityQueue)
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int nextDoc() throws java.io.IOException
meth public long cost()
supr org.apache.lucene.search.DocIdSetIterator
hfds cost,subIterators

CLSS public final org.apache.lucene.search.DisjunctionMaxQuery
cons public init(java.util.Collection<org.apache.lucene.search.Query>,float)
innr protected DisjunctionMaxWeight
intf java.lang.Iterable<org.apache.lucene.search.Query>
meth public boolean equals(java.lang.Object)
meth public float getTieBreakerMultiplier()
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public java.util.Collection<org.apache.lucene.search.Query> getDisjuncts()
meth public java.util.Iterator<org.apache.lucene.search.Query> iterator()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds disjuncts,tieBreakerMultiplier

CLSS protected org.apache.lucene.search.DisjunctionMaxQuery$DisjunctionMaxWeight
 outer org.apache.lucene.search.DisjunctionMaxQuery
cons public init(org.apache.lucene.search.DisjunctionMaxQuery,org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
fld protected final java.util.ArrayList<org.apache.lucene.search.Weight> weights
meth public boolean isCacheable(org.apache.lucene.index.LeafReaderContext)
meth public org.apache.lucene.search.Explanation explain(org.apache.lucene.index.LeafReaderContext,int) throws java.io.IOException
meth public org.apache.lucene.search.Matches matches(org.apache.lucene.index.LeafReaderContext,int) throws java.io.IOException
meth public org.apache.lucene.search.Scorer scorer(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public org.apache.lucene.search.ScorerSupplier scorerSupplier(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
supr org.apache.lucene.search.Weight
hfds scoreMode

CLSS public abstract org.apache.lucene.search.DocIdSet
cons public init()
fld public final static org.apache.lucene.search.DocIdSet EMPTY
intf org.apache.lucene.util.Accountable
meth public abstract org.apache.lucene.search.DocIdSetIterator iterator() throws java.io.IOException
meth public org.apache.lucene.util.Bits bits() throws java.io.IOException
meth public static org.apache.lucene.search.DocIdSet all(int)
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.DocIdSetIterator
cons public init()
fld public final static int NO_MORE_DOCS = 2147483647
meth protected final int slowAdvance(int) throws java.io.IOException
meth public abstract int advance(int) throws java.io.IOException
meth public abstract int docID()
meth public abstract int nextDoc() throws java.io.IOException
meth public abstract long cost()
meth public final static org.apache.lucene.search.DocIdSetIterator all(int)
meth public final static org.apache.lucene.search.DocIdSetIterator empty()
meth public final static org.apache.lucene.search.DocIdSetIterator range(int,int)
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.DocIdStream
cons protected init()
meth public abstract void forEach(org.apache.lucene.search.CheckedIntConsumer<java.io.IOException>) throws java.io.IOException
meth public int count() throws java.io.IOException
supr java.lang.Object

CLSS public final org.apache.lucene.search.DocValuesFieldExistsQuery
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(java.lang.String)
supr org.apache.lucene.search.FieldExistsQuery

CLSS public final org.apache.lucene.search.DocValuesRewriteMethod
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader,org.apache.lucene.search.MultiTermQuery)
supr org.apache.lucene.search.MultiTermQuery$RewriteMethod
hcls MultiTermQueryDocValuesWrapper

CLSS public abstract org.apache.lucene.search.DoubleValues
cons public init()
fld public final static org.apache.lucene.search.DoubleValues EMPTY
meth public abstract boolean advanceExact(int) throws java.io.IOException
meth public abstract double doubleValue() throws java.io.IOException
meth public static org.apache.lucene.search.DoubleValues withDefault(org.apache.lucene.search.DoubleValues,double)
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.DoubleValuesSource
cons public init()
fld public final static org.apache.lucene.search.DoubleValuesSource SCORES
intf org.apache.lucene.search.SegmentCacheable
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean needsScores()
meth public abstract int hashCode()
meth public abstract java.lang.String toString()
meth public abstract org.apache.lucene.search.DoubleValues getValues(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.DoubleValues) throws java.io.IOException
meth public abstract org.apache.lucene.search.DoubleValuesSource rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public final org.apache.lucene.search.LongValuesSource toLongValuesSource()
meth public org.apache.lucene.search.Explanation explain(org.apache.lucene.index.LeafReaderContext,int,org.apache.lucene.search.Explanation) throws java.io.IOException
meth public org.apache.lucene.search.SortField getSortField(boolean)
meth public static org.apache.lucene.search.DoubleValues fromScorer(org.apache.lucene.search.Scorable)
meth public static org.apache.lucene.search.DoubleValues similarityToQueryVector(org.apache.lucene.index.LeafReaderContext,byte[],java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.search.DoubleValues similarityToQueryVector(org.apache.lucene.index.LeafReaderContext,float[],java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.search.DoubleValuesSource constant(double)
meth public static org.apache.lucene.search.DoubleValuesSource fromDoubleField(java.lang.String)
meth public static org.apache.lucene.search.DoubleValuesSource fromField(java.lang.String,java.util.function.LongToDoubleFunction)
meth public static org.apache.lucene.search.DoubleValuesSource fromFloatField(java.lang.String)
meth public static org.apache.lucene.search.DoubleValuesSource fromIntField(java.lang.String)
meth public static org.apache.lucene.search.DoubleValuesSource fromLongField(java.lang.String)
meth public static org.apache.lucene.search.DoubleValuesSource fromQuery(org.apache.lucene.search.Query)
supr java.lang.Object
hcls ConstantValuesSource,DoubleValuesComparatorSource,DoubleValuesHolder,DoubleValuesSortField,FieldValuesSource,LongDoubleValuesSource,QueryDoubleValuesSource,WeightDoubleValuesSource

CLSS public final org.apache.lucene.search.ExactPhraseMatcher
cons public init(org.apache.lucene.search.PhraseQuery$PostingsAndFreq[],org.apache.lucene.search.ScoreMode,org.apache.lucene.search.similarities.Similarity$SimScorer,float)
meth public boolean nextMatch() throws java.io.IOException
meth public int endOffset() throws java.io.IOException
meth public int endPosition()
meth public int startOffset() throws java.io.IOException
meth public int startPosition()
meth public void reset() throws java.io.IOException
supr org.apache.lucene.search.PhraseMatcher
hfds approximation,impactsApproximation,postings
hcls PostingsAndPosition

CLSS public final org.apache.lucene.search.Explanation
meth public !varargs static org.apache.lucene.search.Explanation match(java.lang.Number,java.lang.String,org.apache.lucene.search.Explanation[])
meth public !varargs static org.apache.lucene.search.Explanation noMatch(java.lang.String,org.apache.lucene.search.Explanation[])
meth public boolean equals(java.lang.Object)
meth public boolean isMatch()
meth public int hashCode()
meth public java.lang.Number getValue()
meth public java.lang.String getDescription()
meth public java.lang.String toString()
meth public org.apache.lucene.search.Explanation[] getDetails()
meth public static org.apache.lucene.search.Explanation match(java.lang.Number,java.lang.String,java.util.Collection<org.apache.lucene.search.Explanation>)
meth public static org.apache.lucene.search.Explanation noMatch(java.lang.String,java.util.Collection<org.apache.lucene.search.Explanation>)
supr java.lang.Object
hfds description,details,match,value

CLSS public abstract org.apache.lucene.search.FieldComparator<%0 extends java.lang.Object>
cons public init()
innr public final static RelevanceComparator
innr public static TermValComparator
meth public abstract int compare(int,int)
meth public abstract org.apache.lucene.search.LeafFieldComparator getLeafComparator(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public abstract void setTopValue({org.apache.lucene.search.FieldComparator%0})
meth public abstract {org.apache.lucene.search.FieldComparator%0} value(int)
meth public int compareValues({org.apache.lucene.search.FieldComparator%0},{org.apache.lucene.search.FieldComparator%0})
meth public void disableSkipping()
meth public void setSingleSort()
supr java.lang.Object

CLSS public final static org.apache.lucene.search.FieldComparator$RelevanceComparator
 outer org.apache.lucene.search.FieldComparator
cons public init(int)
intf org.apache.lucene.search.LeafFieldComparator
meth public int compare(int,int)
meth public int compareBottom(int) throws java.io.IOException
meth public int compareTop(int) throws java.io.IOException
meth public int compareValues(java.lang.Float,java.lang.Float)
meth public java.lang.Float value(int)
meth public org.apache.lucene.search.LeafFieldComparator getLeafComparator(org.apache.lucene.index.LeafReaderContext)
meth public void copy(int,int) throws java.io.IOException
meth public void setBottom(int)
meth public void setScorer(org.apache.lucene.search.Scorable)
meth public void setTopValue(java.lang.Float)
supr org.apache.lucene.search.FieldComparator<java.lang.Float>
hfds bottom,scorer,scores,topValue

CLSS public static org.apache.lucene.search.FieldComparator$TermValComparator
 outer org.apache.lucene.search.FieldComparator
cons public init(int,java.lang.String,boolean)
intf org.apache.lucene.search.LeafFieldComparator
meth protected org.apache.lucene.index.BinaryDocValues getBinaryDocValues(org.apache.lucene.index.LeafReaderContext,java.lang.String) throws java.io.IOException
meth public int compare(int,int)
meth public int compareBottom(int) throws java.io.IOException
meth public int compareTop(int) throws java.io.IOException
meth public int compareValues(org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef)
meth public org.apache.lucene.search.LeafFieldComparator getLeafComparator(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public org.apache.lucene.util.BytesRef value(int)
meth public void copy(int,int) throws java.io.IOException
meth public void setBottom(int)
meth public void setScorer(org.apache.lucene.search.Scorable)
meth public void setTopValue(org.apache.lucene.util.BytesRef)
supr org.apache.lucene.search.FieldComparator<org.apache.lucene.util.BytesRef>
hfds bottom,docTerms,field,missingSortCmp,tempBRs,topValue,values

CLSS public abstract org.apache.lucene.search.FieldComparatorSource
cons public init()
meth public abstract org.apache.lucene.search.FieldComparator<?> newComparator(java.lang.String,int,org.apache.lucene.search.Pruning,boolean)
supr java.lang.Object

CLSS public org.apache.lucene.search.FieldDoc
cons public init(int,float)
cons public init(int,float,java.lang.Object[])
cons public init(int,float,java.lang.Object[],int)
fld public java.lang.Object[] fields
meth public java.lang.String toString()
supr org.apache.lucene.search.ScoreDoc

CLSS public org.apache.lucene.search.FieldExistsQuery
cons public init(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getField()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float)
meth public static org.apache.lucene.search.DocIdSetIterator getDocValuesDocIdSetIterator(java.lang.String,org.apache.lucene.index.LeafReader) throws java.io.IOException
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds field

CLSS public abstract org.apache.lucene.search.FieldValueHitQueue<%0 extends org.apache.lucene.search.FieldValueHitQueue$Entry>
fld protected final int[] reverseMul
fld protected final org.apache.lucene.search.FieldComparator<?>[] comparators
fld protected final org.apache.lucene.search.SortField[] fields
innr public static Entry
meth protected abstract boolean lessThan(org.apache.lucene.search.FieldValueHitQueue$Entry,org.apache.lucene.search.FieldValueHitQueue$Entry)
meth public int[] getReverseMul()
meth public org.apache.lucene.search.FieldComparator<?>[] getComparators()
meth public org.apache.lucene.search.LeafFieldComparator[] getComparators(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public static <%0 extends org.apache.lucene.search.FieldValueHitQueue$Entry> org.apache.lucene.search.FieldValueHitQueue<{%%0}> create(org.apache.lucene.search.SortField[],int)
supr org.apache.lucene.util.PriorityQueue<{org.apache.lucene.search.FieldValueHitQueue%0}>
hcls MultiComparatorsFieldValueHitQueue,OneComparatorFieldValueHitQueue

CLSS public static org.apache.lucene.search.FieldValueHitQueue$Entry
 outer org.apache.lucene.search.FieldValueHitQueue
cons public init(int,int)
fld public int slot
meth public java.lang.String toString()
supr org.apache.lucene.search.ScoreDoc

CLSS public abstract org.apache.lucene.search.FilterCollector
cons public init(org.apache.lucene.search.Collector)
fld protected final org.apache.lucene.search.Collector in
intf org.apache.lucene.search.Collector
meth public java.lang.String toString()
meth public org.apache.lucene.search.LeafCollector getLeafCollector(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public org.apache.lucene.search.ScoreMode scoreMode()
meth public void setWeight(org.apache.lucene.search.Weight)
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.FilterLeafCollector
cons public init(org.apache.lucene.search.LeafCollector)
fld protected final org.apache.lucene.search.LeafCollector in
intf org.apache.lucene.search.LeafCollector
meth public java.lang.String toString()
meth public void collect(int) throws java.io.IOException
meth public void finish() throws java.io.IOException
meth public void setScorer(org.apache.lucene.search.Scorable) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.FilterMatchesIterator
cons protected init(org.apache.lucene.search.MatchesIterator)
fld protected final org.apache.lucene.search.MatchesIterator in
intf org.apache.lucene.search.MatchesIterator
meth public boolean next() throws java.io.IOException
meth public int endOffset() throws java.io.IOException
meth public int endPosition()
meth public int startOffset() throws java.io.IOException
meth public int startPosition()
meth public org.apache.lucene.search.MatchesIterator getSubMatches() throws java.io.IOException
meth public org.apache.lucene.search.Query getQuery()
supr java.lang.Object

CLSS public org.apache.lucene.search.FilterScorable
cons public init(org.apache.lucene.search.Scorable)
fld protected final org.apache.lucene.search.Scorable in
meth public float score() throws java.io.IOException
meth public int docID()
meth public java.util.Collection<org.apache.lucene.search.Scorable$ChildScorable> getChildren() throws java.io.IOException
supr org.apache.lucene.search.Scorable

CLSS public abstract org.apache.lucene.search.FilterScorer
cons public init(org.apache.lucene.search.Scorer)
cons public init(org.apache.lucene.search.Scorer,org.apache.lucene.search.Weight)
fld protected final org.apache.lucene.search.Scorer in
intf org.apache.lucene.util.Unwrappable<org.apache.lucene.search.Scorer>
meth public final int docID()
meth public final org.apache.lucene.search.DocIdSetIterator iterator()
meth public final org.apache.lucene.search.TwoPhaseIterator twoPhaseIterator()
meth public float score() throws java.io.IOException
meth public org.apache.lucene.search.Scorer unwrap()
supr org.apache.lucene.search.Scorer

CLSS public abstract org.apache.lucene.search.FilterWeight
cons protected init(org.apache.lucene.search.Query,org.apache.lucene.search.Weight)
cons protected init(org.apache.lucene.search.Weight)
fld protected final org.apache.lucene.search.Weight in
meth public boolean isCacheable(org.apache.lucene.index.LeafReaderContext)
meth public org.apache.lucene.search.Explanation explain(org.apache.lucene.index.LeafReaderContext,int) throws java.io.IOException
meth public org.apache.lucene.search.Matches matches(org.apache.lucene.index.LeafReaderContext,int) throws java.io.IOException
meth public org.apache.lucene.search.Scorer scorer(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
supr org.apache.lucene.search.Weight

CLSS public abstract org.apache.lucene.search.FilteredDocIdSetIterator
cons public init(org.apache.lucene.search.DocIdSetIterator)
fld protected org.apache.lucene.search.DocIdSetIterator _innerIter
meth protected abstract boolean match(int) throws java.io.IOException
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int nextDoc() throws java.io.IOException
meth public long cost()
meth public org.apache.lucene.search.DocIdSetIterator getDelegate()
supr org.apache.lucene.search.DocIdSetIterator
hfds doc

CLSS public org.apache.lucene.search.FuzzyQuery
cons public init(org.apache.lucene.index.Term)
cons public init(org.apache.lucene.index.Term,int)
cons public init(org.apache.lucene.index.Term,int,int)
cons public init(org.apache.lucene.index.Term,int,int,int,boolean)
cons public init(org.apache.lucene.index.Term,int,int,int,boolean,org.apache.lucene.search.MultiTermQuery$RewriteMethod)
fld public final static boolean defaultTranspositions = true
fld public final static int defaultMaxEdits = 2
fld public final static int defaultMaxExpansions = 50
fld public final static int defaultPrefixLength = 0
meth protected org.apache.lucene.index.TermsEnum getTermsEnum(org.apache.lucene.index.Terms,org.apache.lucene.util.AttributeSource) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public boolean getTranspositions()
meth public int getMaxEdits()
meth public int getPrefixLength()
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.index.Term getTerm()
meth public org.apache.lucene.util.automaton.CompiledAutomaton getAutomata()
meth public static int floatToEdits(float,int)
meth public static org.apache.lucene.search.MultiTermQuery$RewriteMethod defaultRewriteMethod(int)
meth public static org.apache.lucene.util.automaton.CompiledAutomaton getFuzzyAutomaton(java.lang.String,int,int,boolean)
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.MultiTermQuery
hfds maxEdits,maxExpansions,prefixLength,term,transpositions

CLSS public final org.apache.lucene.search.FuzzyTermsEnum
cons public init(org.apache.lucene.index.Terms,org.apache.lucene.index.Term,int,int,boolean) throws java.io.IOException
innr public static FuzzyTermsException
meth public boolean seekExact(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public float getBoost()
meth public int docFreq() throws java.io.IOException
meth public long ord() throws java.io.IOException
meth public long totalTermFreq() throws java.io.IOException
meth public org.apache.lucene.index.ImpactsEnum impacts(int) throws java.io.IOException
meth public org.apache.lucene.index.PostingsEnum postings(org.apache.lucene.index.PostingsEnum,int) throws java.io.IOException
meth public org.apache.lucene.index.TermState termState() throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum$SeekStatus seekCeil(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public org.apache.lucene.util.AttributeSource attributes()
meth public org.apache.lucene.util.BytesRef next() throws java.io.IOException
meth public org.apache.lucene.util.BytesRef term() throws java.io.IOException
meth public void seekExact(long) throws java.io.IOException
meth public void seekExact(org.apache.lucene.util.BytesRef,org.apache.lucene.index.TermState) throws java.io.IOException
meth public void setMaxNonCompetitiveBoost(float)
supr org.apache.lucene.index.TermsEnum
hfds actualEnum,atts,automata,boostAtt,bottom,bottomTerm,maxBoostAtt,maxEdits,queuedBottom,term,termLength,terms
hcls AutomatonAttribute,AutomatonAttributeImpl

CLSS public static org.apache.lucene.search.FuzzyTermsEnum$FuzzyTermsException
 outer org.apache.lucene.search.FuzzyTermsEnum
supr java.lang.RuntimeException

CLSS public final org.apache.lucene.search.HitQueue
cons public init(int,boolean)
meth protected final boolean lessThan(org.apache.lucene.search.ScoreDoc,org.apache.lucene.search.ScoreDoc)
supr org.apache.lucene.util.PriorityQueue<org.apache.lucene.search.ScoreDoc>

CLSS public final org.apache.lucene.search.ImpactsDISI
cons public init(org.apache.lucene.search.DocIdSetIterator,org.apache.lucene.search.MaxScoreCache)
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int nextDoc() throws java.io.IOException
meth public long cost()
meth public org.apache.lucene.search.MaxScoreCache getMaxScoreCache()
meth public void setMinCompetitiveScore(float)
supr org.apache.lucene.search.DocIdSetIterator
hfds in,maxScore,maxScoreCache,minCompetitiveScore,upTo

CLSS public final org.apache.lucene.search.IndexOrDocValuesQuery
cons public init(org.apache.lucene.search.Query,org.apache.lucene.search.Query)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query getIndexQuery()
meth public org.apache.lucene.search.Query getRandomAccessQuery()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds dvQuery,indexQuery

CLSS public org.apache.lucene.search.IndexSearcher
cons public init(org.apache.lucene.index.IndexReader)
cons public init(org.apache.lucene.index.IndexReader,java.util.concurrent.Executor)
cons public init(org.apache.lucene.index.IndexReaderContext)
cons public init(org.apache.lucene.index.IndexReaderContext,java.util.concurrent.Executor)
fld protected final java.util.List<org.apache.lucene.index.LeafReaderContext> leafContexts
fld protected final org.apache.lucene.index.IndexReaderContext readerContext
innr public static LeafSlice
innr public static TooManyClauses
innr public static TooManyNestedClauses
meth protected org.apache.lucene.search.Explanation explain(org.apache.lucene.search.Weight,int) throws java.io.IOException
meth protected org.apache.lucene.search.IndexSearcher$LeafSlice[] slices(java.util.List<org.apache.lucene.index.LeafReaderContext>)
meth protected void search(java.util.List<org.apache.lucene.index.LeafReaderContext>,org.apache.lucene.search.Weight,org.apache.lucene.search.Collector) throws java.io.IOException
meth public <%0 extends org.apache.lucene.search.Collector, %1 extends java.lang.Object> {%%1} search(org.apache.lucene.search.Query,org.apache.lucene.search.CollectorManager<{%%0},{%%1}>) throws java.io.IOException
meth public boolean timedOut()
meth public final org.apache.lucene.search.IndexSearcher$LeafSlice[] getSlices()
meth public int count(org.apache.lucene.search.Query) throws java.io.IOException
meth public java.lang.String toString()
meth public java.util.List<org.apache.lucene.index.LeafReaderContext> getLeafContexts()
meth public java.util.concurrent.Executor getExecutor()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.apache.lucene.document.Document doc(int) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.apache.lucene.document.Document doc(int,java.util.Set<java.lang.String>) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.apache.lucene.index.IndexReader getIndexReader()
meth public org.apache.lucene.index.IndexReaderContext getTopReaderContext()
meth public org.apache.lucene.index.StoredFields storedFields() throws java.io.IOException
meth public org.apache.lucene.search.CollectionStatistics collectionStatistics(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.search.Explanation explain(org.apache.lucene.search.Query,int) throws java.io.IOException
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.Query) throws java.io.IOException
meth public org.apache.lucene.search.QueryCache getQueryCache()
meth public org.apache.lucene.search.QueryCachingPolicy getQueryCachingPolicy()
meth public org.apache.lucene.search.TaskExecutor getTaskExecutor()
meth public org.apache.lucene.search.TermStatistics termStatistics(org.apache.lucene.index.Term,int,long) throws java.io.IOException
meth public org.apache.lucene.search.TopDocs search(org.apache.lucene.search.Query,int) throws java.io.IOException
meth public org.apache.lucene.search.TopDocs searchAfter(org.apache.lucene.search.ScoreDoc,org.apache.lucene.search.Query,int) throws java.io.IOException
meth public org.apache.lucene.search.TopDocs searchAfter(org.apache.lucene.search.ScoreDoc,org.apache.lucene.search.Query,int,org.apache.lucene.search.Sort) throws java.io.IOException
meth public org.apache.lucene.search.TopFieldDocs search(org.apache.lucene.search.Query,int,org.apache.lucene.search.Sort) throws java.io.IOException
meth public org.apache.lucene.search.TopFieldDocs search(org.apache.lucene.search.Query,int,org.apache.lucene.search.Sort,boolean) throws java.io.IOException
meth public org.apache.lucene.search.TopFieldDocs searchAfter(org.apache.lucene.search.ScoreDoc,org.apache.lucene.search.Query,int,org.apache.lucene.search.Sort,boolean) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.Query,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public org.apache.lucene.search.similarities.Similarity getSimilarity()
meth public static int getMaxClauseCount()
meth public static org.apache.lucene.search.IndexSearcher$LeafSlice[] slices(java.util.List<org.apache.lucene.index.LeafReaderContext>,int,int)
meth public static org.apache.lucene.search.QueryCache getDefaultQueryCache()
meth public static org.apache.lucene.search.QueryCachingPolicy getDefaultQueryCachingPolicy()
meth public static org.apache.lucene.search.similarities.Similarity getDefaultSimilarity()
meth public static void setDefaultQueryCache(org.apache.lucene.search.QueryCache)
meth public static void setDefaultQueryCachingPolicy(org.apache.lucene.search.QueryCachingPolicy)
meth public static void setMaxClauseCount(int)
meth public void doc(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void search(org.apache.lucene.search.Query,org.apache.lucene.search.Collector) throws java.io.IOException
meth public void setQueryCache(org.apache.lucene.search.QueryCache)
meth public void setQueryCachingPolicy(org.apache.lucene.search.QueryCachingPolicy)
meth public void setSimilarity(org.apache.lucene.search.similarities.Similarity)
meth public void setTimeout(org.apache.lucene.index.QueryTimeout)
supr java.lang.Object
hfds DEFAULT_CACHING_POLICY,DEFAULT_QUERY_CACHE,MAX_DOCS_PER_SLICE,MAX_SEGMENTS_PER_SLICE,TOTAL_HITS_THRESHOLD,defaultSimilarity,executor,leafSlicesSupplier,maxClauseCount,partialResult,queryCache,queryCachingPolicy,queryTimeout,reader,similarity,taskExecutor
hcls CachingLeafSlicesSupplier

CLSS public static org.apache.lucene.search.IndexSearcher$LeafSlice
 outer org.apache.lucene.search.IndexSearcher
cons public init(java.util.List<org.apache.lucene.index.LeafReaderContext>)
fld public final org.apache.lucene.index.LeafReaderContext[] leaves
supr java.lang.Object

CLSS public static org.apache.lucene.search.IndexSearcher$TooManyClauses
 outer org.apache.lucene.search.IndexSearcher
cons public init()
cons public init(java.lang.String)
meth public int getMaxClauseCount()
supr java.lang.RuntimeException
hfds maxClauseCount

CLSS public static org.apache.lucene.search.IndexSearcher$TooManyNestedClauses
 outer org.apache.lucene.search.IndexSearcher
cons public init()
supr org.apache.lucene.search.IndexSearcher$TooManyClauses

CLSS public org.apache.lucene.search.IndexSortSortedNumericDocValuesRangeQuery
cons public init(java.lang.String,long,long,org.apache.lucene.search.Query)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query getFallbackQuery()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds fallbackQuery,field,lowerValue,upperValue
hcls BoundedDocIdSetIterator,IteratorAndCount,ValueAndDoc,ValueComparator

CLSS public org.apache.lucene.search.IndriAndQuery
cons public init(java.util.List<org.apache.lucene.search.BooleanClause>)
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
supr org.apache.lucene.search.IndriQuery

CLSS public org.apache.lucene.search.IndriAndScorer
cons protected init(org.apache.lucene.search.Weight,java.util.List<org.apache.lucene.search.Scorer>,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public float score(java.util.List<org.apache.lucene.search.Scorer>) throws java.io.IOException
meth public float smoothingScore(java.util.List<org.apache.lucene.search.Scorer>,int) throws java.io.IOException
supr org.apache.lucene.search.IndriDisjunctionScorer

CLSS public org.apache.lucene.search.IndriAndWeight
cons public init(org.apache.lucene.search.IndriAndQuery,org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public boolean isCacheable(org.apache.lucene.index.LeafReaderContext)
meth public org.apache.lucene.search.BulkScorer bulkScorer(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public org.apache.lucene.search.Explanation explain(org.apache.lucene.index.LeafReaderContext,int) throws java.io.IOException
meth public org.apache.lucene.search.Scorer scorer(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
supr org.apache.lucene.search.Weight
hfds boost,query,scoreMode,weights

CLSS public abstract org.apache.lucene.search.IndriDisjunctionScorer
cons protected init(org.apache.lucene.search.Weight,java.util.List<org.apache.lucene.search.Scorer>,org.apache.lucene.search.ScoreMode,float)
meth public abstract float smoothingScore(java.util.List<org.apache.lucene.search.Scorer>,int) throws java.io.IOException
meth public float getMaxScore(int) throws java.io.IOException
meth public float score() throws java.io.IOException
meth public float smoothingScore(int) throws java.io.IOException
meth public int docID()
meth public java.util.List<org.apache.lucene.search.Scorer> getSubMatches() throws java.io.IOException
meth public org.apache.lucene.search.DocIdSetIterator iterator()
supr org.apache.lucene.search.IndriScorer
hfds approximation,subScorers,subScorersList

CLSS public abstract org.apache.lucene.search.IndriQuery
cons public init(java.util.List<org.apache.lucene.search.BooleanClause>)
intf java.lang.Iterable<org.apache.lucene.search.BooleanClause>
meth public abstract org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public java.util.Iterator<org.apache.lucene.search.BooleanClause> iterator()
meth public java.util.List<org.apache.lucene.search.BooleanClause> getClauses()
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds clauses

CLSS public abstract org.apache.lucene.search.IndriScorer
cons protected init(org.apache.lucene.search.Weight,float)
meth public abstract float getMaxScore(int) throws java.io.IOException
meth public abstract float score() throws java.io.IOException
meth public abstract float smoothingScore(int) throws java.io.IOException
meth public abstract int docID()
meth public abstract org.apache.lucene.search.DocIdSetIterator iterator()
meth public float getBoost()
supr org.apache.lucene.search.Scorer
hfds boost

CLSS public org.apache.lucene.search.KnnByteVectorQuery
cons public init(java.lang.String,byte[],int)
cons public init(java.lang.String,byte[],int,org.apache.lucene.search.Query)
fld protected final int k
fld protected final java.lang.String field
meth protected org.apache.lucene.search.TopDocs approximateSearch(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.util.Bits,int) throws java.io.IOException
meth protected org.apache.lucene.search.TopDocs exactSearch(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
meth protected org.apache.lucene.search.TopDocs mergeLeafResults(org.apache.lucene.search.TopDocs[])
meth public boolean equals(java.lang.Object)
meth public byte[] getTargetCopy()
meth public int getK()
meth public int hashCode()
meth public java.lang.String getField()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query getFilter()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds NO_RESULTS,target

CLSS public abstract interface org.apache.lucene.search.KnnCollector
meth public abstract boolean collect(int,float)
meth public abstract boolean earlyTerminated()
meth public abstract float minCompetitiveSimilarity()
meth public abstract int k()
meth public abstract long visitLimit()
meth public abstract long visitedCount()
meth public abstract org.apache.lucene.search.TopDocs topDocs()
meth public abstract void incVisitedCount(int)

CLSS public org.apache.lucene.search.KnnFloatVectorQuery
cons public init(java.lang.String,float[],int)
cons public init(java.lang.String,float[],int,org.apache.lucene.search.Query)
fld protected final int k
fld protected final java.lang.String field
meth protected org.apache.lucene.search.TopDocs approximateSearch(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.util.Bits,int) throws java.io.IOException
meth protected org.apache.lucene.search.TopDocs exactSearch(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
meth protected org.apache.lucene.search.TopDocs mergeLeafResults(org.apache.lucene.search.TopDocs[])
meth public boolean equals(java.lang.Object)
meth public float[] getTargetCopy()
meth public int getK()
meth public int hashCode()
meth public java.lang.String getField()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query getFilter()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds NO_RESULTS,target

CLSS public org.apache.lucene.search.KnnVectorFieldExistsQuery
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(java.lang.String)
supr org.apache.lucene.search.FieldExistsQuery

CLSS public org.apache.lucene.search.KnnVectorQuery
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(java.lang.String,float[],int)
cons public init(java.lang.String,float[],int,org.apache.lucene.search.Query)
supr org.apache.lucene.search.KnnFloatVectorQuery

CLSS public org.apache.lucene.search.LRUQueryCache
cons public init(int,long)
cons public init(int,long,java.util.function.Predicate<org.apache.lucene.index.LeafReaderContext>,float)
innr protected static CacheAndCount
intf org.apache.lucene.search.QueryCache
intf org.apache.lucene.util.Accountable
meth protected org.apache.lucene.search.LRUQueryCache$CacheAndCount cacheImpl(org.apache.lucene.search.BulkScorer,int) throws java.io.IOException
meth protected void onClear()
meth protected void onDocIdSetCache(java.lang.Object,long)
meth protected void onDocIdSetEviction(java.lang.Object,int,long)
meth protected void onHit(java.lang.Object,org.apache.lucene.search.Query)
meth protected void onMiss(java.lang.Object,org.apache.lucene.search.Query)
meth protected void onQueryCache(org.apache.lucene.search.Query,long)
meth protected void onQueryEviction(org.apache.lucene.search.Query,long)
meth public final long getCacheCount()
meth public final long getCacheSize()
meth public final long getEvictionCount()
meth public final long getHitCount()
meth public final long getMissCount()
meth public final long getTotalCount()
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public long ramBytesUsed()
meth public org.apache.lucene.search.Weight doCache(org.apache.lucene.search.Weight,org.apache.lucene.search.QueryCachingPolicy)
meth public void clear()
meth public void clearCoreCacheKey(java.lang.Object)
meth public void clearQuery(org.apache.lucene.search.Query)
supr java.lang.Object
hfds cache,cacheCount,cacheSize,hitCount,leavesToCache,lock,maxRamBytesUsed,maxSize,missCount,mostRecentlyUsedQueries,ramBytesUsed,skipCacheFactor,uniqueQueries
hcls CachingWrapperWeight,LeafCache,MinSegmentSizePredicate

CLSS protected static org.apache.lucene.search.LRUQueryCache$CacheAndCount
 outer org.apache.lucene.search.LRUQueryCache
cons public init(org.apache.lucene.search.DocIdSet,int)
fld protected final static org.apache.lucene.search.LRUQueryCache$CacheAndCount EMPTY
intf org.apache.lucene.util.Accountable
meth public int count()
meth public long ramBytesUsed()
meth public org.apache.lucene.search.DocIdSetIterator iterator() throws java.io.IOException
supr java.lang.Object
hfds BASE_RAM_BYTES_USED,cache,count

CLSS public abstract interface org.apache.lucene.search.LeafCollector
meth public abstract void collect(int) throws java.io.IOException
meth public abstract void setScorer(org.apache.lucene.search.Scorable) throws java.io.IOException
meth public org.apache.lucene.search.DocIdSetIterator competitiveIterator() throws java.io.IOException
meth public void collect(org.apache.lucene.search.DocIdStream) throws java.io.IOException
meth public void finish() throws java.io.IOException

CLSS public abstract interface org.apache.lucene.search.LeafFieldComparator
meth public abstract int compareBottom(int) throws java.io.IOException
meth public abstract int compareTop(int) throws java.io.IOException
meth public abstract void copy(int,int) throws java.io.IOException
meth public abstract void setBottom(int) throws java.io.IOException
meth public abstract void setScorer(org.apache.lucene.search.Scorable) throws java.io.IOException
meth public org.apache.lucene.search.DocIdSetIterator competitiveIterator() throws java.io.IOException
meth public void setHitsThresholdReached() throws java.io.IOException

CLSS public final org.apache.lucene.search.LeafSimScorer
cons public init(org.apache.lucene.search.similarities.Similarity$SimScorer,org.apache.lucene.index.LeafReader,java.lang.String,boolean) throws java.io.IOException
meth public float score(int,float) throws java.io.IOException
meth public org.apache.lucene.search.Explanation explain(int,org.apache.lucene.search.Explanation) throws java.io.IOException
meth public org.apache.lucene.search.similarities.Similarity$SimScorer getSimScorer()
supr java.lang.Object
hfds norms,scorer

CLSS public abstract org.apache.lucene.search.LiveFieldValues<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(org.apache.lucene.search.ReferenceManager<{org.apache.lucene.search.LiveFieldValues%0}>,{org.apache.lucene.search.LiveFieldValues%1})
intf java.io.Closeable
intf org.apache.lucene.search.ReferenceManager$RefreshListener
meth protected abstract {org.apache.lucene.search.LiveFieldValues%1} lookupFromSearcher({org.apache.lucene.search.LiveFieldValues%0},java.lang.String) throws java.io.IOException
meth public int size()
meth public void add(java.lang.String,{org.apache.lucene.search.LiveFieldValues%1})
meth public void afterRefresh(boolean) throws java.io.IOException
meth public void beforeRefresh() throws java.io.IOException
meth public void close()
meth public void delete(java.lang.String)
meth public {org.apache.lucene.search.LiveFieldValues%1} get(java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds current,mgr,missingValue,old

CLSS public abstract org.apache.lucene.search.LongValues
cons public init()
meth public abstract boolean advanceExact(int) throws java.io.IOException
meth public abstract long longValue() throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.LongValuesSource
cons public init()
innr public static ConstantLongValuesSource
intf org.apache.lucene.search.SegmentCacheable
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean needsScores()
meth public abstract int hashCode()
meth public abstract java.lang.String toString()
meth public abstract org.apache.lucene.search.LongValues getValues(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.DoubleValues) throws java.io.IOException
meth public abstract org.apache.lucene.search.LongValuesSource rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.DoubleValuesSource toDoubleValuesSource()
meth public org.apache.lucene.search.SortField getSortField(boolean)
meth public static org.apache.lucene.search.LongValuesSource constant(long)
meth public static org.apache.lucene.search.LongValuesSource fromIntField(java.lang.String)
meth public static org.apache.lucene.search.LongValuesSource fromLongField(java.lang.String)
supr java.lang.Object
hcls DoubleLongValuesSource,FieldValuesSource,LongValuesComparatorSource,LongValuesHolder,LongValuesSortField

CLSS public static org.apache.lucene.search.LongValuesSource$ConstantLongValuesSource
 outer org.apache.lucene.search.LongValuesSource
meth public boolean equals(java.lang.Object)
meth public boolean isCacheable(org.apache.lucene.index.LeafReaderContext)
meth public boolean needsScores()
meth public int hashCode()
meth public java.lang.String toString()
meth public long getValue()
meth public org.apache.lucene.search.LongValues getValues(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.DoubleValues) throws java.io.IOException
meth public org.apache.lucene.search.LongValuesSource rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
supr org.apache.lucene.search.LongValuesSource
hfds value

CLSS public final org.apache.lucene.search.MatchAllDocsQuery
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float)
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query

CLSS public org.apache.lucene.search.MatchNoDocsQuery
cons public init()
cons public init(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds reason

CLSS public abstract interface org.apache.lucene.search.Matches
intf java.lang.Iterable<java.lang.String>
meth public abstract java.util.Collection<org.apache.lucene.search.Matches> getSubMatches()
meth public abstract org.apache.lucene.search.MatchesIterator getMatches(java.lang.String) throws java.io.IOException

CLSS public abstract interface org.apache.lucene.search.MatchesIterator
meth public abstract boolean next() throws java.io.IOException
meth public abstract int endOffset() throws java.io.IOException
meth public abstract int endPosition()
meth public abstract int startOffset() throws java.io.IOException
meth public abstract int startPosition()
meth public abstract org.apache.lucene.search.MatchesIterator getSubMatches() throws java.io.IOException
meth public abstract org.apache.lucene.search.Query getQuery()

CLSS public final org.apache.lucene.search.MatchesUtils
fld public final static org.apache.lucene.search.Matches MATCH_WITH_NO_TERMS
meth public static org.apache.lucene.search.Matches forField(java.lang.String,org.apache.lucene.util.IOSupplier<org.apache.lucene.search.MatchesIterator>) throws java.io.IOException
meth public static org.apache.lucene.search.Matches fromSubMatches(java.util.List<org.apache.lucene.search.Matches>)
meth public static org.apache.lucene.search.MatchesIterator disjunction(java.util.List<org.apache.lucene.search.MatchesIterator>) throws java.io.IOException
meth public static org.apache.lucene.search.MatchesIterator disjunction(org.apache.lucene.index.LeafReaderContext,int,org.apache.lucene.search.Query,java.lang.String,org.apache.lucene.util.BytesRefIterator) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface org.apache.lucene.search.MaxNonCompetitiveBoostAttribute
intf org.apache.lucene.util.Attribute
meth public abstract float getMaxNonCompetitiveBoost()
meth public abstract org.apache.lucene.util.BytesRef getCompetitiveTerm()
meth public abstract void setCompetitiveTerm(org.apache.lucene.util.BytesRef)
meth public abstract void setMaxNonCompetitiveBoost(float)

CLSS public final org.apache.lucene.search.MaxNonCompetitiveBoostAttributeImpl
cons public init()
intf org.apache.lucene.search.MaxNonCompetitiveBoostAttribute
meth public float getMaxNonCompetitiveBoost()
meth public org.apache.lucene.util.BytesRef getCompetitiveTerm()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public void setCompetitiveTerm(org.apache.lucene.util.BytesRef)
meth public void setMaxNonCompetitiveBoost(float)
supr org.apache.lucene.util.AttributeImpl
hfds competitiveTerm,maxNonCompetitiveBoost

CLSS public final org.apache.lucene.search.MaxScoreCache
cons public init(org.apache.lucene.index.ImpactsSource,org.apache.lucene.search.similarities.Similarity$SimScorer)
meth public float getMaxScore(int) throws java.io.IOException
meth public int advanceShallow(int) throws java.io.IOException
supr java.lang.Object
hfds globalMaxScore,impactsSource,maxScoreCache,maxScoreCacheUpTo,scorer

CLSS public org.apache.lucene.search.MultiCollector
intf org.apache.lucene.search.Collector
meth public !varargs static org.apache.lucene.search.Collector wrap(org.apache.lucene.search.Collector[])
meth public org.apache.lucene.search.Collector[] getCollectors()
meth public org.apache.lucene.search.LeafCollector getLeafCollector(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public org.apache.lucene.search.ScoreMode scoreMode()
meth public static org.apache.lucene.search.Collector wrap(java.lang.Iterable<? extends org.apache.lucene.search.Collector>)
meth public void setWeight(org.apache.lucene.search.Weight)
supr java.lang.Object
hfds cacheScores,collectors
hcls MinCompetitiveScoreAwareScorable,MultiLeafCollector

CLSS public org.apache.lucene.search.MultiCollectorManager
cons public !varargs init(org.apache.lucene.search.CollectorManager<? extends org.apache.lucene.search.Collector,?>[])
 anno 0 java.lang.SafeVarargs()
intf org.apache.lucene.search.CollectorManager<org.apache.lucene.search.Collector,java.lang.Object[]>
meth public java.lang.Object[] reduce(java.util.Collection<org.apache.lucene.search.Collector>) throws java.io.IOException
meth public org.apache.lucene.search.Collector newCollector() throws java.io.IOException
supr java.lang.Object
hfds collectorManagers

CLSS public org.apache.lucene.search.MultiPhraseQuery
innr public static Builder
innr public static UnionFullPostingsEnum
innr public static UnionPostingsEnum
meth public boolean equals(java.lang.Object)
meth public final java.lang.String toString(java.lang.String)
meth public int getSlop()
meth public int hashCode()
meth public int[] getPositions()
meth public org.apache.lucene.index.Term[][] getTermArrays()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds field,positions,slop,termArrays
hcls PostingsAndPosition

CLSS public static org.apache.lucene.search.MultiPhraseQuery$Builder
 outer org.apache.lucene.search.MultiPhraseQuery
cons public init()
cons public init(org.apache.lucene.search.MultiPhraseQuery)
meth public org.apache.lucene.search.MultiPhraseQuery build()
meth public org.apache.lucene.search.MultiPhraseQuery$Builder add(org.apache.lucene.index.Term)
meth public org.apache.lucene.search.MultiPhraseQuery$Builder add(org.apache.lucene.index.Term[])
meth public org.apache.lucene.search.MultiPhraseQuery$Builder add(org.apache.lucene.index.Term[],int)
meth public org.apache.lucene.search.MultiPhraseQuery$Builder setSlop(int)
supr java.lang.Object
hfds field,positions,slop,termArrays

CLSS public static org.apache.lucene.search.MultiPhraseQuery$UnionFullPostingsEnum
 outer org.apache.lucene.search.MultiPhraseQuery
cons public init(java.util.List<org.apache.lucene.index.PostingsEnum>)
meth public int endOffset() throws java.io.IOException
meth public int freq() throws java.io.IOException
meth public int nextPosition() throws java.io.IOException
meth public int startOffset() throws java.io.IOException
meth public org.apache.lucene.util.BytesRef getPayload() throws java.io.IOException
supr org.apache.lucene.search.MultiPhraseQuery$UnionPostingsEnum
hfds freq,posQueue,started,subs

CLSS public static org.apache.lucene.search.MultiPhraseQuery$UnionPostingsEnum
 outer org.apache.lucene.search.MultiPhraseQuery
cons public init(java.util.Collection<org.apache.lucene.index.PostingsEnum>)
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int endOffset() throws java.io.IOException
meth public int freq() throws java.io.IOException
meth public int nextDoc() throws java.io.IOException
meth public int nextPosition() throws java.io.IOException
meth public int startOffset() throws java.io.IOException
meth public long cost()
meth public org.apache.lucene.util.BytesRef getPayload() throws java.io.IOException
supr org.apache.lucene.index.PostingsEnum
hfds cost,docsQueue,posQueue,posQueueDoc,subs
hcls DocsQueue,PositionsQueue

CLSS public abstract org.apache.lucene.search.MultiTermQuery
cons public init(java.lang.String,org.apache.lucene.search.MultiTermQuery$RewriteMethod)
fld protected final java.lang.String field
fld protected org.apache.lucene.search.MultiTermQuery$RewriteMethod rewriteMethod
fld public final static org.apache.lucene.search.MultiTermQuery$RewriteMethod CONSTANT_SCORE_BLENDED_REWRITE
fld public final static org.apache.lucene.search.MultiTermQuery$RewriteMethod CONSTANT_SCORE_BOOLEAN_REWRITE
fld public final static org.apache.lucene.search.MultiTermQuery$RewriteMethod CONSTANT_SCORE_REWRITE
fld public final static org.apache.lucene.search.MultiTermQuery$RewriteMethod DOC_VALUES_REWRITE
fld public final static org.apache.lucene.search.MultiTermQuery$RewriteMethod SCORING_BOOLEAN_REWRITE
innr public abstract static RewriteMethod
innr public final static TopTermsBlendedFreqScoringRewrite
innr public final static TopTermsBoostOnlyBooleanQueryRewrite
innr public final static TopTermsScoringBooleanQueryRewrite
meth protected abstract org.apache.lucene.index.TermsEnum getTermsEnum(org.apache.lucene.index.Terms,org.apache.lucene.util.AttributeSource) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public final java.lang.String getField()
meth public final org.apache.lucene.index.TermsEnum getTermsEnum(org.apache.lucene.index.Terms) throws java.io.IOException
meth public final org.apache.lucene.search.Query rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public int hashCode()
meth public long getTermsCount() throws java.io.IOException
meth public org.apache.lucene.search.MultiTermQuery$RewriteMethod getRewriteMethod()
meth public void setRewriteMethod(org.apache.lucene.search.MultiTermQuery$RewriteMethod)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
supr org.apache.lucene.search.Query

CLSS public abstract static org.apache.lucene.search.MultiTermQuery$RewriteMethod
 outer org.apache.lucene.search.MultiTermQuery
cons public init()
meth protected org.apache.lucene.index.TermsEnum getTermsEnum(org.apache.lucene.search.MultiTermQuery,org.apache.lucene.index.Terms,org.apache.lucene.util.AttributeSource) throws java.io.IOException
meth public abstract org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader,org.apache.lucene.search.MultiTermQuery) throws java.io.IOException
supr java.lang.Object

CLSS public final static org.apache.lucene.search.MultiTermQuery$TopTermsBlendedFreqScoringRewrite
 outer org.apache.lucene.search.MultiTermQuery
cons public init(int)
meth protected int getMaxSize()
meth protected org.apache.lucene.search.BlendedTermQuery$Builder getTopLevelBuilder()
meth protected org.apache.lucene.search.Query build(org.apache.lucene.search.BlendedTermQuery$Builder)
meth protected void addClause(org.apache.lucene.search.BlendedTermQuery$Builder,org.apache.lucene.index.Term,int,float,org.apache.lucene.index.TermStates)
supr org.apache.lucene.search.TopTermsRewrite<org.apache.lucene.search.BlendedTermQuery$Builder>

CLSS public final static org.apache.lucene.search.MultiTermQuery$TopTermsBoostOnlyBooleanQueryRewrite
 outer org.apache.lucene.search.MultiTermQuery
cons public init(int)
meth protected int getMaxSize()
meth protected org.apache.lucene.search.BooleanQuery$Builder getTopLevelBuilder()
meth protected org.apache.lucene.search.Query build(org.apache.lucene.search.BooleanQuery$Builder)
meth protected void addClause(org.apache.lucene.search.BooleanQuery$Builder,org.apache.lucene.index.Term,int,float,org.apache.lucene.index.TermStates)
supr org.apache.lucene.search.TopTermsRewrite<org.apache.lucene.search.BooleanQuery$Builder>

CLSS public final static org.apache.lucene.search.MultiTermQuery$TopTermsScoringBooleanQueryRewrite
 outer org.apache.lucene.search.MultiTermQuery
cons public init(int)
meth protected int getMaxSize()
meth protected org.apache.lucene.search.BooleanQuery$Builder getTopLevelBuilder()
meth protected org.apache.lucene.search.Query build(org.apache.lucene.search.BooleanQuery$Builder)
meth protected void addClause(org.apache.lucene.search.BooleanQuery$Builder,org.apache.lucene.index.Term,int,float,org.apache.lucene.index.TermStates)
supr org.apache.lucene.search.TopTermsRewrite<org.apache.lucene.search.BooleanQuery$Builder>

CLSS public final org.apache.lucene.search.Multiset<%0 extends java.lang.Object>
cons public init()
meth public boolean add({org.apache.lucene.search.Multiset%0})
meth public boolean contains(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean remove(java.lang.Object)
meth public int hashCode()
meth public int size()
meth public java.util.Iterator<{org.apache.lucene.search.Multiset%0}> iterator()
meth public void clear()
supr java.util.AbstractCollection<{org.apache.lucene.search.Multiset%0}>
hfds map,size

CLSS public org.apache.lucene.search.NGramPhraseQuery
cons public init(int,org.apache.lucene.search.PhraseQuery)
meth public boolean equals(java.lang.Object)
meth public int getN()
meth public int hashCode()
meth public int[] getPositions()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.index.Term[] getTerms()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds n,phraseQuery

CLSS public org.apache.lucene.search.NamedMatches
cons public init(java.lang.String,org.apache.lucene.search.Matches)
intf org.apache.lucene.search.Matches
meth public java.lang.String getName()
meth public java.util.Collection<org.apache.lucene.search.Matches> getSubMatches()
meth public java.util.Iterator<java.lang.String> iterator()
meth public org.apache.lucene.search.MatchesIterator getMatches(java.lang.String) throws java.io.IOException
meth public static java.util.List<org.apache.lucene.search.NamedMatches> findNamedMatches(org.apache.lucene.search.Matches)
meth public static org.apache.lucene.search.Query wrapQuery(java.lang.String,org.apache.lucene.search.Query)
supr java.lang.Object
hfds in,name
hcls NamedQuery

CLSS public final org.apache.lucene.search.NormsFieldExistsQuery
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(java.lang.String)
supr org.apache.lucene.search.FieldExistsQuery

CLSS public abstract org.apache.lucene.search.PhraseMatcher
meth public abstract boolean nextMatch() throws java.io.IOException
meth public abstract void reset() throws java.io.IOException
meth public float getMatchCost()
supr java.lang.Object
hfds matchCost

CLSS public org.apache.lucene.search.PhraseQuery
cons public !varargs init(int,java.lang.String,java.lang.String[])
cons public !varargs init(int,java.lang.String,org.apache.lucene.util.BytesRef[])
cons public !varargs init(java.lang.String,java.lang.String[])
cons public !varargs init(java.lang.String,org.apache.lucene.util.BytesRef[])
innr public static Builder
innr public static PostingsAndFreq
meth public boolean equals(java.lang.Object)
meth public int getSlop()
meth public int hashCode()
meth public int[] getPositions()
meth public java.lang.String getField()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.index.Term[] getTerms()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public static float termPositionsCost(org.apache.lucene.index.TermsEnum) throws java.io.IOException
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds TERM_OPS_PER_POS,TERM_POSNS_SEEK_OPS_PER_DOC,field,positions,slop,terms

CLSS public static org.apache.lucene.search.PhraseQuery$Builder
 outer org.apache.lucene.search.PhraseQuery
cons public init()
meth public org.apache.lucene.search.PhraseQuery build()
meth public org.apache.lucene.search.PhraseQuery$Builder add(org.apache.lucene.index.Term)
meth public org.apache.lucene.search.PhraseQuery$Builder add(org.apache.lucene.index.Term,int)
meth public org.apache.lucene.search.PhraseQuery$Builder setSlop(int)
supr java.lang.Object
hfds positions,slop,terms

CLSS public static org.apache.lucene.search.PhraseQuery$PostingsAndFreq
 outer org.apache.lucene.search.PhraseQuery
cons public !varargs init(org.apache.lucene.index.PostingsEnum,org.apache.lucene.index.ImpactsEnum,int,org.apache.lucene.index.Term[])
cons public init(org.apache.lucene.index.PostingsEnum,org.apache.lucene.index.ImpactsEnum,int,java.util.List<org.apache.lucene.index.Term>)
intf java.lang.Comparable<org.apache.lucene.search.PhraseQuery$PostingsAndFreq>
meth public boolean equals(java.lang.Object)
meth public int compareTo(org.apache.lucene.search.PhraseQuery$PostingsAndFreq)
meth public int hashCode()
supr java.lang.Object
hfds impacts,nTerms,position,postings,terms

CLSS public abstract org.apache.lucene.search.PhraseWeight
cons protected init(org.apache.lucene.search.Query,java.lang.String,org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode) throws java.io.IOException
meth protected abstract org.apache.lucene.search.PhraseMatcher getPhraseMatcher(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.similarities.Similarity$SimScorer,boolean) throws java.io.IOException
meth protected abstract org.apache.lucene.search.similarities.Similarity$SimScorer getStats(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public boolean isCacheable(org.apache.lucene.index.LeafReaderContext)
meth public org.apache.lucene.search.Explanation explain(org.apache.lucene.index.LeafReaderContext,int) throws java.io.IOException
meth public org.apache.lucene.search.Matches matches(org.apache.lucene.index.LeafReaderContext,int) throws java.io.IOException
meth public org.apache.lucene.search.Scorer scorer(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
supr org.apache.lucene.search.Weight
hfds field,scoreMode,similarity,stats

CLSS public abstract org.apache.lucene.search.PointInSetQuery
cons protected init(java.lang.String,int,int,org.apache.lucene.search.PointInSetQuery$Stream)
fld protected final static long BASE_RAM_BYTES
innr public abstract static Stream
intf org.apache.lucene.util.Accountable
meth protected abstract java.lang.String toString(byte[])
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final java.lang.String toString(java.lang.String)
meth public final org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public int getBytesPerDim()
meth public int getNumDims()
meth public java.lang.String getField()
meth public java.util.Collection<byte[]> getPackedPoints()
meth public long ramBytesUsed()
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds bytesPerDim,field,numDims,ramBytesUsed,sortedPackedPoints,sortedPackedPointsHashCode
hcls MergePointVisitor,SinglePointVisitor

CLSS public abstract static org.apache.lucene.search.PointInSetQuery$Stream
 outer org.apache.lucene.search.PointInSetQuery
cons public init()
intf org.apache.lucene.util.BytesRefIterator
meth public abstract org.apache.lucene.util.BytesRef next()
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.PointRangeQuery
cons protected init(java.lang.String,byte[],byte[],int)
meth protected abstract java.lang.String toString(int,byte[])
meth public byte[] getLowerPoint()
meth public byte[] getUpperPoint()
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final java.lang.String toString(java.lang.String)
meth public final org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public int getBytesPerDim()
meth public int getNumDims()
meth public java.lang.String getField()
meth public static void checkArgs(java.lang.String,java.lang.Object,java.lang.Object)
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds bytesPerDim,field,lowerPoint,numDims,upperPoint

CLSS public org.apache.lucene.search.PositiveScoresOnlyCollector
cons public init(org.apache.lucene.search.Collector)
meth public org.apache.lucene.search.LeafCollector getLeafCollector(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
supr org.apache.lucene.search.FilterCollector

CLSS public org.apache.lucene.search.PrefixQuery
cons public init(org.apache.lucene.index.Term)
cons public init(org.apache.lucene.index.Term,org.apache.lucene.search.MultiTermQuery$RewriteMethod)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.index.Term getPrefix()
meth public static org.apache.lucene.util.automaton.Automaton toAutomaton(org.apache.lucene.util.BytesRef)
supr org.apache.lucene.search.AutomatonQuery

CLSS public final !enum org.apache.lucene.search.Pruning
fld public final static org.apache.lucene.search.Pruning GREATER_THAN
fld public final static org.apache.lucene.search.Pruning GREATER_THAN_OR_EQUAL_TO
fld public final static org.apache.lucene.search.Pruning NONE
meth public static org.apache.lucene.search.Pruning valueOf(java.lang.String)
meth public static org.apache.lucene.search.Pruning[] values()
supr java.lang.Enum<org.apache.lucene.search.Pruning>

CLSS public abstract org.apache.lucene.search.Query
cons public init()
meth protected final boolean sameClassAs(java.lang.Object)
meth protected final int classHash()
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.String toString(java.lang.String)
meth public abstract void visit(org.apache.lucene.search.QueryVisitor)
meth public final java.lang.String toString()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
supr java.lang.Object
hfds CLASS_NAME_HASH,isDeprecatedRewriteMethodOverridden,newMethod,oldMethod

CLSS public abstract interface org.apache.lucene.search.QueryCache
meth public abstract org.apache.lucene.search.Weight doCache(org.apache.lucene.search.Weight,org.apache.lucene.search.QueryCachingPolicy)

CLSS public abstract interface org.apache.lucene.search.QueryCachingPolicy
meth public abstract boolean shouldCache(org.apache.lucene.search.Query) throws java.io.IOException
meth public abstract void onUse(org.apache.lucene.search.Query)

CLSS public abstract org.apache.lucene.search.QueryRescorer
cons public init(org.apache.lucene.search.Query)
meth protected abstract float combine(float,boolean,float)
meth public org.apache.lucene.search.Explanation explain(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.Explanation,int) throws java.io.IOException
meth public org.apache.lucene.search.TopDocs rescore(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.TopDocs,int) throws java.io.IOException
meth public static org.apache.lucene.search.TopDocs rescore(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.TopDocs,org.apache.lucene.search.Query,double,int) throws java.io.IOException
supr org.apache.lucene.search.Rescorer
hfds query

CLSS public abstract org.apache.lucene.search.QueryVisitor
cons public init()
fld public final static org.apache.lucene.search.QueryVisitor EMPTY_VISITOR
meth public !varargs void consumeTerms(org.apache.lucene.search.Query,org.apache.lucene.index.Term[])
meth public boolean acceptField(java.lang.String)
meth public org.apache.lucene.search.QueryVisitor getSubVisitor(org.apache.lucene.search.BooleanClause$Occur,org.apache.lucene.search.Query)
meth public static org.apache.lucene.search.QueryVisitor termCollector(java.util.Set<org.apache.lucene.index.Term>)
meth public void consumeTermsMatching(org.apache.lucene.search.Query,java.lang.String,java.util.function.Supplier<org.apache.lucene.util.automaton.ByteRunAutomaton>)
meth public void visitLeaf(org.apache.lucene.search.Query)
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.ReferenceManager<%0 extends java.lang.Object>
cons public init()
fld protected volatile {org.apache.lucene.search.ReferenceManager%0} current
innr public abstract interface static RefreshListener
intf java.io.Closeable
meth protected abstract boolean tryIncRef({org.apache.lucene.search.ReferenceManager%0}) throws java.io.IOException
meth protected abstract int getRefCount({org.apache.lucene.search.ReferenceManager%0})
meth protected abstract void decRef({org.apache.lucene.search.ReferenceManager%0}) throws java.io.IOException
meth protected abstract {org.apache.lucene.search.ReferenceManager%0} refreshIfNeeded({org.apache.lucene.search.ReferenceManager%0}) throws java.io.IOException
meth protected void afterClose() throws java.io.IOException
meth protected void afterMaybeRefresh() throws java.io.IOException
meth public final boolean maybeRefresh() throws java.io.IOException
meth public final void close() throws java.io.IOException
meth public final void maybeRefreshBlocking() throws java.io.IOException
meth public final void release({org.apache.lucene.search.ReferenceManager%0}) throws java.io.IOException
meth public final {org.apache.lucene.search.ReferenceManager%0} acquire() throws java.io.IOException
meth public void addListener(org.apache.lucene.search.ReferenceManager$RefreshListener)
meth public void removeListener(org.apache.lucene.search.ReferenceManager$RefreshListener)
supr java.lang.Object
hfds REFERENCE_MANAGER_IS_CLOSED_MSG,refreshListeners,refreshLock

CLSS public abstract interface static org.apache.lucene.search.ReferenceManager$RefreshListener
 outer org.apache.lucene.search.ReferenceManager
meth public abstract void afterRefresh(boolean) throws java.io.IOException
meth public abstract void beforeRefresh() throws java.io.IOException

CLSS public org.apache.lucene.search.RegexpQuery
cons public init(org.apache.lucene.index.Term)
cons public init(org.apache.lucene.index.Term,int)
cons public init(org.apache.lucene.index.Term,int,int)
cons public init(org.apache.lucene.index.Term,int,int,int)
cons public init(org.apache.lucene.index.Term,int,int,org.apache.lucene.util.automaton.AutomatonProvider,int,org.apache.lucene.search.MultiTermQuery$RewriteMethod)
cons public init(org.apache.lucene.index.Term,int,org.apache.lucene.util.automaton.AutomatonProvider,int)
fld public final static org.apache.lucene.util.automaton.AutomatonProvider DEFAULT_PROVIDER
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.index.Term getRegexp()
supr org.apache.lucene.search.AutomatonQuery

CLSS public abstract org.apache.lucene.search.Rescorer
cons public init()
meth public abstract org.apache.lucene.search.Explanation explain(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.Explanation,int) throws java.io.IOException
meth public abstract org.apache.lucene.search.TopDocs rescore(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.TopDocs,int) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.Scorable
cons public init()
innr public static ChildScorable
meth public abstract float score() throws java.io.IOException
meth public abstract int docID()
meth public float smoothingScore(int) throws java.io.IOException
meth public java.util.Collection<org.apache.lucene.search.Scorable$ChildScorable> getChildren() throws java.io.IOException
meth public void setMinCompetitiveScore(float) throws java.io.IOException
supr java.lang.Object

CLSS public static org.apache.lucene.search.Scorable$ChildScorable
 outer org.apache.lucene.search.Scorable
cons public init(org.apache.lucene.search.Scorable,java.lang.String)
fld public final java.lang.String relationship
fld public final org.apache.lucene.search.Scorable child
supr java.lang.Object

CLSS public final org.apache.lucene.search.ScoreCachingWrappingScorer
meth public float score() throws java.io.IOException
meth public int docID()
meth public java.util.Collection<org.apache.lucene.search.Scorable$ChildScorable> getChildren()
meth public static org.apache.lucene.search.Scorable wrap(org.apache.lucene.search.Scorable)
meth public void setMinCompetitiveScore(float) throws java.io.IOException
supr org.apache.lucene.search.Scorable
hfds curDoc,curScore,in

CLSS public org.apache.lucene.search.ScoreDoc
cons public init(int,float)
cons public init(int,float,int)
fld public float score
fld public int doc
fld public int shardIndex
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final !enum org.apache.lucene.search.ScoreMode
fld public final static org.apache.lucene.search.ScoreMode COMPLETE
fld public final static org.apache.lucene.search.ScoreMode COMPLETE_NO_SCORES
fld public final static org.apache.lucene.search.ScoreMode TOP_DOCS
fld public final static org.apache.lucene.search.ScoreMode TOP_DOCS_WITH_SCORES
fld public final static org.apache.lucene.search.ScoreMode TOP_SCORES
meth public boolean isExhaustive()
meth public boolean needsScores()
meth public static org.apache.lucene.search.ScoreMode valueOf(java.lang.String)
meth public static org.apache.lucene.search.ScoreMode[] values()
supr java.lang.Enum<org.apache.lucene.search.ScoreMode>
hfds isExhaustive,needsScores

CLSS public abstract org.apache.lucene.search.Scorer
cons protected init(org.apache.lucene.search.Weight)
fld protected final org.apache.lucene.search.Weight weight
meth public abstract float getMaxScore(int) throws java.io.IOException
meth public abstract org.apache.lucene.search.DocIdSetIterator iterator()
meth public int advanceShallow(int) throws java.io.IOException
meth public org.apache.lucene.search.TwoPhaseIterator twoPhaseIterator()
meth public org.apache.lucene.search.Weight getWeight()
supr org.apache.lucene.search.Scorable

CLSS public abstract org.apache.lucene.search.ScorerSupplier
cons public init()
meth public abstract long cost()
meth public abstract org.apache.lucene.search.Scorer get(long) throws java.io.IOException
meth public void setTopLevelScoringClause() throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.ScoringRewrite<%0 extends java.lang.Object>
cons public init()
fld public final static org.apache.lucene.search.MultiTermQuery$RewriteMethod CONSTANT_SCORE_BOOLEAN_REWRITE
fld public final static org.apache.lucene.search.ScoringRewrite<org.apache.lucene.search.BooleanQuery$Builder> SCORING_BOOLEAN_REWRITE
meth protected abstract org.apache.lucene.search.Query build({org.apache.lucene.search.ScoringRewrite%0})
meth protected abstract void addClause({org.apache.lucene.search.ScoringRewrite%0},org.apache.lucene.index.Term,int,float,org.apache.lucene.index.TermStates) throws java.io.IOException
meth protected abstract void checkMaxClauseCount(int) throws java.io.IOException
meth protected abstract {org.apache.lucene.search.ScoringRewrite%0} getTopLevelBuilder() throws java.io.IOException
meth protected final void addClause({org.apache.lucene.search.ScoringRewrite%0},org.apache.lucene.index.Term,int,float) throws java.io.IOException
meth public final org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader,org.apache.lucene.search.MultiTermQuery) throws java.io.IOException
supr org.apache.lucene.search.MultiTermQuery$RewriteMethod<{org.apache.lucene.search.ScoringRewrite%0}>
hcls ParallelArraysTermCollector,TermFreqBoostByteStart

CLSS public org.apache.lucene.search.SearcherFactory
cons public init()
meth public org.apache.lucene.search.IndexSearcher newSearcher(org.apache.lucene.index.IndexReader,org.apache.lucene.index.IndexReader) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.lucene.search.SearcherLifetimeManager
cons public init()
innr public abstract interface static Pruner
innr public final static PruneByAge
intf java.io.Closeable
meth public long record(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.IndexSearcher acquire(long)
meth public void close() throws java.io.IOException
meth public void prune(org.apache.lucene.search.SearcherLifetimeManager$Pruner) throws java.io.IOException
meth public void release(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
supr java.lang.Object
hfds NANOS_PER_SEC,closed,searchers
hcls SearcherTracker

CLSS public final static org.apache.lucene.search.SearcherLifetimeManager$PruneByAge
 outer org.apache.lucene.search.SearcherLifetimeManager
cons public init(double)
intf org.apache.lucene.search.SearcherLifetimeManager$Pruner
meth public boolean doPrune(double,org.apache.lucene.search.IndexSearcher)
supr java.lang.Object
hfds maxAgeSec

CLSS public abstract interface static org.apache.lucene.search.SearcherLifetimeManager$Pruner
 outer org.apache.lucene.search.SearcherLifetimeManager
meth public abstract boolean doPrune(double,org.apache.lucene.search.IndexSearcher)

CLSS public final org.apache.lucene.search.SearcherManager
cons public init(org.apache.lucene.index.DirectoryReader,org.apache.lucene.search.SearcherFactory) throws java.io.IOException
cons public init(org.apache.lucene.index.IndexWriter,boolean,boolean,org.apache.lucene.search.SearcherFactory) throws java.io.IOException
cons public init(org.apache.lucene.index.IndexWriter,org.apache.lucene.search.SearcherFactory) throws java.io.IOException
cons public init(org.apache.lucene.store.Directory,org.apache.lucene.search.SearcherFactory) throws java.io.IOException
meth protected boolean tryIncRef(org.apache.lucene.search.IndexSearcher)
meth protected int getRefCount(org.apache.lucene.search.IndexSearcher)
meth protected org.apache.lucene.search.IndexSearcher refreshIfNeeded(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth protected void decRef(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public boolean isSearcherCurrent() throws java.io.IOException
meth public static org.apache.lucene.search.IndexSearcher getSearcher(org.apache.lucene.search.SearcherFactory,org.apache.lucene.index.IndexReader,org.apache.lucene.index.IndexReader) throws java.io.IOException
supr org.apache.lucene.search.ReferenceManager<org.apache.lucene.search.IndexSearcher>
hfds searcherFactory

CLSS public abstract interface org.apache.lucene.search.SegmentCacheable
meth public abstract boolean isCacheable(org.apache.lucene.index.LeafReaderContext)

CLSS public abstract org.apache.lucene.search.SimpleCollector
cons public init()
intf org.apache.lucene.search.Collector
intf org.apache.lucene.search.LeafCollector
meth protected void doSetNextReader(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public abstract void collect(int) throws java.io.IOException
meth public final org.apache.lucene.search.LeafCollector getLeafCollector(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public void setScorer(org.apache.lucene.search.Scorable) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.SimpleFieldComparator<%0 extends java.lang.Object>
cons public init()
intf org.apache.lucene.search.LeafFieldComparator
meth protected abstract void doSetNextReader(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public final org.apache.lucene.search.LeafFieldComparator getLeafComparator(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public void setScorer(org.apache.lucene.search.Scorable) throws java.io.IOException
supr org.apache.lucene.search.FieldComparator<{org.apache.lucene.search.SimpleFieldComparator%0}>

CLSS public final org.apache.lucene.search.SloppyPhraseMatcher
cons public init(org.apache.lucene.search.PhraseQuery$PostingsAndFreq[],int,org.apache.lucene.search.ScoreMode,org.apache.lucene.search.similarities.Similarity$SimScorer,float,boolean)
meth public boolean nextMatch() throws java.io.IOException
meth public int endOffset() throws java.io.IOException
meth public int endPosition()
meth public int startOffset() throws java.io.IOException
meth public int startPosition()
meth public void reset() throws java.io.IOException
supr org.apache.lucene.search.PhraseMatcher
hfds approximation,captureLeadMatch,checkedRpts,end,hasMultiTermRpts,hasRpts,impactsApproximation,leadEndOffset,leadOffset,leadOrd,leadPosition,matchLength,numPostings,phrasePositions,positioned,pq,rptGroups,rptStack,slop

CLSS public final org.apache.lucene.search.Sort
cons public !varargs init(org.apache.lucene.search.SortField[])
cons public init()
fld public final static org.apache.lucene.search.Sort INDEXORDER
fld public final static org.apache.lucene.search.Sort RELEVANCE
meth public boolean equals(java.lang.Object)
meth public boolean needsScores()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.apache.lucene.search.Sort rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.SortField[] getSort()
supr java.lang.Object
hfds fields

CLSS public org.apache.lucene.search.SortField
cons public init(java.lang.String,org.apache.lucene.search.FieldComparatorSource)
cons public init(java.lang.String,org.apache.lucene.search.FieldComparatorSource,boolean)
cons public init(java.lang.String,org.apache.lucene.search.SortField$Type)
cons public init(java.lang.String,org.apache.lucene.search.SortField$Type,boolean)
fld protected java.lang.Object missingValue
fld public final static java.lang.Object STRING_FIRST
fld public final static java.lang.Object STRING_LAST
fld public final static org.apache.lucene.search.SortField FIELD_DOC
fld public final static org.apache.lucene.search.SortField FIELD_SCORE
innr public final static !enum Type
innr public final static Provider
meth protected static org.apache.lucene.search.SortField$Type readType(org.apache.lucene.store.DataInput) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public boolean getOptimizeSortWithIndexedData()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean getOptimizeSortWithPoints()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean getReverse()
meth public boolean needsScores()
meth public int hashCode()
meth public java.lang.Object getMissingValue()
meth public java.lang.String getField()
meth public java.lang.String toString()
meth public java.util.Comparator<org.apache.lucene.util.BytesRef> getBytesComparator()
meth public org.apache.lucene.index.IndexSorter getIndexSorter()
meth public org.apache.lucene.search.FieldComparator<?> getComparator(int,org.apache.lucene.search.Pruning)
meth public org.apache.lucene.search.FieldComparatorSource getComparatorSource()
meth public org.apache.lucene.search.SortField rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.SortField$Type getType()
meth public void setBytesComparator(java.util.Comparator<org.apache.lucene.util.BytesRef>)
meth public void setMissingValue(java.lang.Object)
meth public void setOptimizeSortWithIndexedData(boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void setOptimizeSortWithPoints(boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
supr java.lang.Object
hfds bytesComparator,comparatorSource,field,optimizeSortWithIndexedData,reverse,type

CLSS public final static org.apache.lucene.search.SortField$Provider
 outer org.apache.lucene.search.SortField
cons public init()
fld public final static java.lang.String NAME = "SortField"
meth public org.apache.lucene.search.SortField readSortField(org.apache.lucene.store.DataInput) throws java.io.IOException
meth public void writeSortField(org.apache.lucene.search.SortField,org.apache.lucene.store.DataOutput) throws java.io.IOException
supr org.apache.lucene.index.SortFieldProvider

CLSS public final static !enum org.apache.lucene.search.SortField$Type
 outer org.apache.lucene.search.SortField
fld public final static org.apache.lucene.search.SortField$Type CUSTOM
fld public final static org.apache.lucene.search.SortField$Type DOC
fld public final static org.apache.lucene.search.SortField$Type DOUBLE
fld public final static org.apache.lucene.search.SortField$Type FLOAT
fld public final static org.apache.lucene.search.SortField$Type INT
fld public final static org.apache.lucene.search.SortField$Type LONG
fld public final static org.apache.lucene.search.SortField$Type REWRITEABLE
fld public final static org.apache.lucene.search.SortField$Type SCORE
fld public final static org.apache.lucene.search.SortField$Type STRING
fld public final static org.apache.lucene.search.SortField$Type STRING_VAL
meth public static org.apache.lucene.search.SortField$Type valueOf(java.lang.String)
meth public static org.apache.lucene.search.SortField$Type[] values()
supr java.lang.Enum<org.apache.lucene.search.SortField$Type>

CLSS public org.apache.lucene.search.SortRescorer
cons public init(org.apache.lucene.search.Sort)
meth public org.apache.lucene.search.Explanation explain(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.Explanation,int) throws java.io.IOException
meth public org.apache.lucene.search.TopDocs rescore(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.TopDocs,int) throws java.io.IOException
supr org.apache.lucene.search.Rescorer
hfds sort

CLSS public org.apache.lucene.search.SortedNumericSelector
cons public init()
innr public final static !enum Type
meth public static org.apache.lucene.index.NumericDocValues wrap(org.apache.lucene.index.SortedNumericDocValues,org.apache.lucene.search.SortedNumericSelector$Type,org.apache.lucene.search.SortField$Type)
supr java.lang.Object
hcls MaxValue,MinValue

CLSS public final static !enum org.apache.lucene.search.SortedNumericSelector$Type
 outer org.apache.lucene.search.SortedNumericSelector
fld public final static org.apache.lucene.search.SortedNumericSelector$Type MAX
fld public final static org.apache.lucene.search.SortedNumericSelector$Type MIN
meth public static org.apache.lucene.search.SortedNumericSelector$Type valueOf(java.lang.String)
meth public static org.apache.lucene.search.SortedNumericSelector$Type[] values()
supr java.lang.Enum<org.apache.lucene.search.SortedNumericSelector$Type>

CLSS public org.apache.lucene.search.SortedNumericSortField
cons public init(java.lang.String,org.apache.lucene.search.SortField$Type)
cons public init(java.lang.String,org.apache.lucene.search.SortField$Type,boolean)
cons public init(java.lang.String,org.apache.lucene.search.SortField$Type,boolean,org.apache.lucene.search.SortedNumericSelector$Type)
innr public final static Provider
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.apache.lucene.index.IndexSorter getIndexSorter()
meth public org.apache.lucene.search.FieldComparator<?> getComparator(int,org.apache.lucene.search.Pruning)
meth public org.apache.lucene.search.SortField$Type getNumericType()
meth public org.apache.lucene.search.SortedNumericSelector$Type getSelector()
meth public void setMissingValue(java.lang.Object)
supr org.apache.lucene.search.SortField
hfds selector,type

CLSS public final static org.apache.lucene.search.SortedNumericSortField$Provider
 outer org.apache.lucene.search.SortedNumericSortField
cons public init()
fld public final static java.lang.String NAME = "SortedNumericSortField"
meth public org.apache.lucene.search.SortField readSortField(org.apache.lucene.store.DataInput) throws java.io.IOException
meth public void writeSortField(org.apache.lucene.search.SortField,org.apache.lucene.store.DataOutput) throws java.io.IOException
supr org.apache.lucene.index.SortFieldProvider

CLSS public org.apache.lucene.search.SortedSetSelector
cons public init()
innr public final static !enum Type
meth public static org.apache.lucene.index.SortedDocValues wrap(org.apache.lucene.index.SortedSetDocValues,org.apache.lucene.search.SortedSetSelector$Type)
supr java.lang.Object
hcls MaxValue,MiddleMaxValue,MiddleMinValue,MinValue

CLSS public final static !enum org.apache.lucene.search.SortedSetSelector$Type
 outer org.apache.lucene.search.SortedSetSelector
fld public final static org.apache.lucene.search.SortedSetSelector$Type MAX
fld public final static org.apache.lucene.search.SortedSetSelector$Type MIDDLE_MAX
fld public final static org.apache.lucene.search.SortedSetSelector$Type MIDDLE_MIN
fld public final static org.apache.lucene.search.SortedSetSelector$Type MIN
meth public static org.apache.lucene.search.SortedSetSelector$Type valueOf(java.lang.String)
meth public static org.apache.lucene.search.SortedSetSelector$Type[] values()
supr java.lang.Enum<org.apache.lucene.search.SortedSetSelector$Type>

CLSS public org.apache.lucene.search.SortedSetSortField
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,boolean,org.apache.lucene.search.SortedSetSelector$Type)
innr public final static Provider
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.apache.lucene.index.IndexSorter getIndexSorter()
meth public org.apache.lucene.search.FieldComparator<?> getComparator(int,org.apache.lucene.search.Pruning)
meth public org.apache.lucene.search.SortedSetSelector$Type getSelector()
meth public void setMissingValue(java.lang.Object)
supr org.apache.lucene.search.SortField
hfds selector

CLSS public final static org.apache.lucene.search.SortedSetSortField$Provider
 outer org.apache.lucene.search.SortedSetSortField
cons public init()
fld public final static java.lang.String NAME = "SortedSetSortField"
meth public org.apache.lucene.search.SortField readSortField(org.apache.lucene.store.DataInput) throws java.io.IOException
meth public void writeSortField(org.apache.lucene.search.SortField,org.apache.lucene.store.DataOutput) throws java.io.IOException
supr org.apache.lucene.index.SortFieldProvider

CLSS public final org.apache.lucene.search.SynonymQuery
innr public static Builder
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public java.util.List<org.apache.lucene.index.Term> getTerms()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds field,terms
hcls DisiWrapperFreq,FreqBoostTermScorer,SynonymScorer,SynonymWeight,TermAndBoost

CLSS public static org.apache.lucene.search.SynonymQuery$Builder
 outer org.apache.lucene.search.SynonymQuery
cons public init(java.lang.String)
meth public org.apache.lucene.search.SynonymQuery build()
meth public org.apache.lucene.search.SynonymQuery$Builder addTerm(org.apache.lucene.index.Term)
meth public org.apache.lucene.search.SynonymQuery$Builder addTerm(org.apache.lucene.index.Term,float)
meth public org.apache.lucene.search.SynonymQuery$Builder addTerm(org.apache.lucene.util.BytesRef,float)
supr java.lang.Object
hfds field,terms

CLSS public final org.apache.lucene.search.TaskExecutor
cons public init(java.util.concurrent.Executor)
meth public <%0 extends java.lang.Object> java.util.List<{%%0}> invokeAll(java.util.Collection<java.util.concurrent.Callable<{%%0}>>) throws java.io.IOException
meth public java.lang.String toString()
supr java.lang.Object
hfds executor,numberOfRunningTasksInCurrentThread
hcls TaskGroup

CLSS public org.apache.lucene.search.TermInSetQuery
cons public !varargs init(java.lang.String,org.apache.lucene.util.BytesRef[])
cons public !varargs init(org.apache.lucene.search.MultiTermQuery$RewriteMethod,java.lang.String,org.apache.lucene.util.BytesRef[])
cons public init(java.lang.String,java.util.Collection<org.apache.lucene.util.BytesRef>)
cons public init(org.apache.lucene.search.MultiTermQuery$RewriteMethod,java.lang.String,java.util.Collection<org.apache.lucene.util.BytesRef>)
intf org.apache.lucene.util.Accountable
meth protected org.apache.lucene.index.TermsEnum getTermsEnum(org.apache.lucene.index.Terms,org.apache.lucene.util.AttributeSource) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public long getTermsCount() throws java.io.IOException
meth public long ramBytesUsed()
meth public org.apache.lucene.index.PrefixCodedTerms getTermData()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.MultiTermQuery
hfds BASE_RAM_BYTES_USED,field,termData,termDataHashCode
hcls SetEnum

CLSS public org.apache.lucene.search.TermQuery
cons public init(org.apache.lucene.index.Term)
cons public init(org.apache.lucene.index.Term,org.apache.lucene.index.TermStates)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.index.Term getTerm()
meth public org.apache.lucene.index.TermStates getTermStates()
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.ScoreMode,float) throws java.io.IOException
meth public void visit(org.apache.lucene.search.QueryVisitor)
supr org.apache.lucene.search.Query
hfds perReaderTermState,term
hcls TermWeight

CLSS public org.apache.lucene.search.TermRangeQuery
cons public init(java.lang.String,org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef,boolean,boolean)
cons public init(java.lang.String,org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef,boolean,boolean,org.apache.lucene.search.MultiTermQuery$RewriteMethod)
meth public boolean equals(java.lang.Object)
meth public boolean includesLower()
meth public boolean includesUpper()
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.util.BytesRef getLowerTerm()
meth public org.apache.lucene.util.BytesRef getUpperTerm()
meth public static org.apache.lucene.search.TermRangeQuery newStringRange(java.lang.String,java.lang.String,java.lang.String,boolean,boolean)
meth public static org.apache.lucene.search.TermRangeQuery newStringRange(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,org.apache.lucene.search.MultiTermQuery$RewriteMethod)
meth public static org.apache.lucene.util.automaton.Automaton toAutomaton(org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef,boolean,boolean)
supr org.apache.lucene.search.AutomatonQuery
hfds includeLower,includeUpper,lowerTerm,upperTerm

CLSS public final org.apache.lucene.search.TermScorer
cons public init(org.apache.lucene.search.Weight,org.apache.lucene.index.ImpactsEnum,org.apache.lucene.search.LeafSimScorer,boolean)
cons public init(org.apache.lucene.search.Weight,org.apache.lucene.index.PostingsEnum,org.apache.lucene.search.LeafSimScorer)
meth public final int freq() throws java.io.IOException
meth public float getMaxScore(int) throws java.io.IOException
meth public float score() throws java.io.IOException
meth public float smoothingScore(int) throws java.io.IOException
meth public int advanceShallow(int) throws java.io.IOException
meth public int docID()
meth public java.lang.String toString()
meth public org.apache.lucene.search.DocIdSetIterator iterator()
meth public void setMinCompetitiveScore(float)
supr org.apache.lucene.search.Scorer
hfds docScorer,impactsDisi,iterator,maxScoreCache,postingsEnum

CLSS public org.apache.lucene.search.TermStatistics
cons public init(org.apache.lucene.util.BytesRef,long,long)
meth public final long docFreq()
meth public final long totalTermFreq()
meth public final org.apache.lucene.util.BytesRef term()
meth public java.lang.String toString()
supr java.lang.Object
hfds docFreq,term,totalTermFreq

CLSS public org.apache.lucene.search.TimeLimitingCollector
cons public init(org.apache.lucene.search.Collector,org.apache.lucene.util.Counter,long)
innr public final static TimerThread
innr public static TimeExceededException
intf org.apache.lucene.search.Collector
meth public boolean isGreedy()
meth public org.apache.lucene.search.LeafCollector getLeafCollector(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public org.apache.lucene.search.ScoreMode scoreMode()
meth public static org.apache.lucene.search.TimeLimitingCollector$TimerThread getGlobalTimerThread()
meth public static org.apache.lucene.util.Counter getGlobalCounter()
meth public void setBaseline()
meth public void setBaseline(long)
meth public void setCollector(org.apache.lucene.search.Collector)
meth public void setGreedy(boolean)
supr java.lang.Object
hfds clock,collector,docBase,greedy,t0,ticksAllowed,timeout
hcls TimerThreadHolder

CLSS public static org.apache.lucene.search.TimeLimitingCollector$TimeExceededException
 outer org.apache.lucene.search.TimeLimitingCollector
meth public int getLastDocCollected()
meth public long getTimeAllowed()
meth public long getTimeElapsed()
supr java.lang.RuntimeException
hfds lastDocCollected,timeAllowed,timeElapsed

CLSS public final static org.apache.lucene.search.TimeLimitingCollector$TimerThread
 outer org.apache.lucene.search.TimeLimitingCollector
cons public init(long,org.apache.lucene.util.Counter)
cons public init(org.apache.lucene.util.Counter)
fld public final static int DEFAULT_RESOLUTION = 20
fld public final static java.lang.String THREAD_NAME = "TimeLimitedCollector timer thread"
meth public long getMilliseconds()
meth public long getResolution()
meth public void run()
meth public void setResolution(long)
meth public void stopTimer()
supr java.lang.Thread
hfds counter,resolution,stop,time

CLSS public org.apache.lucene.search.TopDocs
cons public init(org.apache.lucene.search.TotalHits,org.apache.lucene.search.ScoreDoc[])
fld public org.apache.lucene.search.ScoreDoc[] scoreDocs
fld public org.apache.lucene.search.TotalHits totalHits
meth public static org.apache.lucene.search.TopDocs merge(int,int,org.apache.lucene.search.TopDocs[])
meth public static org.apache.lucene.search.TopDocs merge(int,int,org.apache.lucene.search.TopDocs[],java.util.Comparator<org.apache.lucene.search.ScoreDoc>)
meth public static org.apache.lucene.search.TopDocs merge(int,org.apache.lucene.search.TopDocs[])
meth public static org.apache.lucene.search.TopFieldDocs merge(org.apache.lucene.search.Sort,int,int,org.apache.lucene.search.TopFieldDocs[])
meth public static org.apache.lucene.search.TopFieldDocs merge(org.apache.lucene.search.Sort,int,int,org.apache.lucene.search.TopFieldDocs[],java.util.Comparator<org.apache.lucene.search.ScoreDoc>)
meth public static org.apache.lucene.search.TopFieldDocs merge(org.apache.lucene.search.Sort,int,org.apache.lucene.search.TopFieldDocs[])
supr java.lang.Object
hfds DEFAULT_TIE_BREAKER,DOC_ID_TIE_BREAKER,SHARD_INDEX_TIE_BREAKER
hcls MergeSortQueue,ScoreMergeSortQueue,ShardRef

CLSS public abstract org.apache.lucene.search.TopDocsCollector<%0 extends org.apache.lucene.search.ScoreDoc>
cons protected init(org.apache.lucene.util.PriorityQueue<{org.apache.lucene.search.TopDocsCollector%0}>)
fld protected final org.apache.lucene.util.PriorityQueue<{org.apache.lucene.search.TopDocsCollector%0}> pq
fld protected int totalHits
fld protected org.apache.lucene.search.TotalHits$Relation totalHitsRelation
fld public final static org.apache.lucene.search.TopDocs EMPTY_TOPDOCS
intf org.apache.lucene.search.Collector
meth protected int topDocsSize()
meth protected org.apache.lucene.search.TopDocs newTopDocs(org.apache.lucene.search.ScoreDoc[],int)
meth protected void populateResults(org.apache.lucene.search.ScoreDoc[],int)
meth public int getTotalHits()
meth public org.apache.lucene.search.TopDocs topDocs()
meth public org.apache.lucene.search.TopDocs topDocs(int)
meth public org.apache.lucene.search.TopDocs topDocs(int,int)
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.TopFieldCollector
meth protected org.apache.lucene.search.TopDocs newTopDocs(org.apache.lucene.search.ScoreDoc[],int)
meth protected void populateResults(org.apache.lucene.search.ScoreDoc[],int)
meth protected void updateGlobalMinCompetitiveScore(org.apache.lucene.search.Scorable) throws java.io.IOException
meth protected void updateMinCompetitiveScore(org.apache.lucene.search.Scorable) throws java.io.IOException
meth public boolean isEarlyTerminated()
meth public org.apache.lucene.search.ScoreMode scoreMode()
meth public org.apache.lucene.search.TopFieldDocs topDocs()
meth public static org.apache.lucene.search.CollectorManager<org.apache.lucene.search.TopFieldCollector,org.apache.lucene.search.TopFieldDocs> createSharedManager(org.apache.lucene.search.Sort,int,org.apache.lucene.search.FieldDoc,int)
meth public static org.apache.lucene.search.TopFieldCollector create(org.apache.lucene.search.Sort,int,int)
meth public static org.apache.lucene.search.TopFieldCollector create(org.apache.lucene.search.Sort,int,org.apache.lucene.search.FieldDoc,int)
meth public static void populateScores(org.apache.lucene.search.ScoreDoc[],org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.Query) throws java.io.IOException
supr org.apache.lucene.search.TopDocsCollector<org.apache.lucene.search.FieldValueHitQueue$Entry>
hfds EMPTY_SCOREDOCS,bottom,canSetMinScore,docBase,firstComparator,hitsThresholdChecker,minCompetitiveScore,minScoreAcc,needsScores,numComparators,numHits,queueFull,scoreMode,searchSortPartOfIndexSort
hcls PagingFieldCollector,SimpleFieldCollector,TopFieldLeafCollector

CLSS public org.apache.lucene.search.TopFieldDocs
cons public init(org.apache.lucene.search.TotalHits,org.apache.lucene.search.ScoreDoc[],org.apache.lucene.search.SortField[])
fld public org.apache.lucene.search.SortField[] fields
supr org.apache.lucene.search.TopDocs

CLSS public final org.apache.lucene.search.TopKnnCollector
cons public init(int,int)
meth public boolean collect(int,float)
meth public float minCompetitiveSimilarity()
meth public java.lang.String toString()
meth public org.apache.lucene.search.TopDocs topDocs()
supr org.apache.lucene.search.AbstractKnnCollector
hfds queue

CLSS public abstract org.apache.lucene.search.TopScoreDocCollector
innr public abstract static ScorerLeafCollector
meth protected org.apache.lucene.search.TopDocs newTopDocs(org.apache.lucene.search.ScoreDoc[],int)
meth protected void updateGlobalMinCompetitiveScore(org.apache.lucene.search.Scorable) throws java.io.IOException
meth protected void updateMinCompetitiveScore(org.apache.lucene.search.Scorable) throws java.io.IOException
meth public org.apache.lucene.search.ScoreMode scoreMode()
meth public static org.apache.lucene.search.CollectorManager<org.apache.lucene.search.TopScoreDocCollector,org.apache.lucene.search.TopDocs> createSharedManager(int,org.apache.lucene.search.ScoreDoc,int)
meth public static org.apache.lucene.search.TopScoreDocCollector create(int,int)
meth public static org.apache.lucene.search.TopScoreDocCollector create(int,org.apache.lucene.search.ScoreDoc,int)
supr org.apache.lucene.search.TopDocsCollector<org.apache.lucene.search.ScoreDoc>
hfds docBase,hitsThresholdChecker,minCompetitiveScore,minScoreAcc,pqTop
hcls PagingTopScoreDocCollector,SimpleTopScoreDocCollector

CLSS public abstract static org.apache.lucene.search.TopScoreDocCollector$ScorerLeafCollector
 outer org.apache.lucene.search.TopScoreDocCollector
cons public init()
fld protected org.apache.lucene.search.Scorable scorer
intf org.apache.lucene.search.LeafCollector
meth public void setScorer(org.apache.lucene.search.Scorable) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.TopTermsRewrite<%0 extends java.lang.Object>
cons public init(int)
meth protected abstract int getMaxSize()
meth protected abstract org.apache.lucene.search.Query build({org.apache.lucene.search.TopTermsRewrite%0})
meth protected abstract void addClause({org.apache.lucene.search.TopTermsRewrite%0},org.apache.lucene.index.Term,int,float,org.apache.lucene.index.TermStates) throws java.io.IOException
meth protected abstract {org.apache.lucene.search.TopTermsRewrite%0} getTopLevelBuilder() throws java.io.IOException
meth protected final void addClause({org.apache.lucene.search.TopTermsRewrite%0},org.apache.lucene.index.Term,int,float) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public final org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader,org.apache.lucene.search.MultiTermQuery) throws java.io.IOException
meth public int getSize()
meth public int hashCode()
supr org.apache.lucene.search.MultiTermQuery$RewriteMethod<{org.apache.lucene.search.TopTermsRewrite%0}>
hfds scoreTermSortByTermComp,size
hcls ScoreTerm

CLSS public org.apache.lucene.search.TotalHitCountCollector
cons public init()
intf org.apache.lucene.search.Collector
meth public int getTotalHits()
meth public org.apache.lucene.search.LeafCollector getLeafCollector(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public org.apache.lucene.search.ScoreMode scoreMode()
meth public void setWeight(org.apache.lucene.search.Weight)
supr java.lang.Object
hfds totalHits,weight

CLSS public org.apache.lucene.search.TotalHitCountCollectorManager
cons public init()
intf org.apache.lucene.search.CollectorManager<org.apache.lucene.search.TotalHitCountCollector,java.lang.Integer>
meth public java.lang.Integer reduce(java.util.Collection<org.apache.lucene.search.TotalHitCountCollector>) throws java.io.IOException
meth public org.apache.lucene.search.TotalHitCountCollector newCollector() throws java.io.IOException
supr java.lang.Object

CLSS public final org.apache.lucene.search.TotalHits
cons public init(long,org.apache.lucene.search.TotalHits$Relation)
fld public final long value
fld public final org.apache.lucene.search.TotalHits$Relation relation
innr public final static !enum Relation
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final static !enum org.apache.lucene.search.TotalHits$Relation
 outer org.apache.lucene.search.TotalHits
fld public final static org.apache.lucene.search.TotalHits$Relation EQUAL_TO
fld public final static org.apache.lucene.search.TotalHits$Relation GREATER_THAN_OR_EQUAL_TO
meth public static org.apache.lucene.search.TotalHits$Relation valueOf(java.lang.String)
meth public static org.apache.lucene.search.TotalHits$Relation[] values()
supr java.lang.Enum<org.apache.lucene.search.TotalHits$Relation>

CLSS public abstract org.apache.lucene.search.TwoPhaseIterator
cons protected init(org.apache.lucene.search.DocIdSetIterator)
fld protected final org.apache.lucene.search.DocIdSetIterator approximation
meth public abstract boolean matches() throws java.io.IOException
meth public abstract float matchCost()
meth public org.apache.lucene.search.DocIdSetIterator approximation()
meth public static org.apache.lucene.search.DocIdSetIterator asDocIdSetIterator(org.apache.lucene.search.TwoPhaseIterator)
meth public static org.apache.lucene.search.TwoPhaseIterator unwrap(org.apache.lucene.search.DocIdSetIterator)
supr java.lang.Object
hcls TwoPhaseIteratorAsDocIdSetIterator

CLSS public org.apache.lucene.search.UsageTrackingQueryCachingPolicy
cons public init()
cons public init(int)
intf org.apache.lucene.search.QueryCachingPolicy
meth protected int minFrequencyToCache(org.apache.lucene.search.Query)
meth public boolean shouldCache(org.apache.lucene.search.Query) throws java.io.IOException
meth public void onUse(org.apache.lucene.search.Query)
supr java.lang.Object
hfds SENTINEL,recentlyUsedFilters

CLSS public abstract org.apache.lucene.search.Weight
cons protected init(org.apache.lucene.search.Query)
fld protected final org.apache.lucene.search.Query parentQuery
innr protected static DefaultBulkScorer
intf org.apache.lucene.search.SegmentCacheable
meth public abstract org.apache.lucene.search.Explanation explain(org.apache.lucene.index.LeafReaderContext,int) throws java.io.IOException
meth public abstract org.apache.lucene.search.Scorer scorer(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public final org.apache.lucene.search.Query getQuery()
meth public int count(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public org.apache.lucene.search.BulkScorer bulkScorer(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public org.apache.lucene.search.Matches matches(org.apache.lucene.index.LeafReaderContext,int) throws java.io.IOException
meth public org.apache.lucene.search.ScorerSupplier scorerSupplier(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
supr java.lang.Object

CLSS protected static org.apache.lucene.search.Weight$DefaultBulkScorer
 outer org.apache.lucene.search.Weight
cons public init(org.apache.lucene.search.Scorer)
meth public int score(org.apache.lucene.search.LeafCollector,org.apache.lucene.util.Bits,int,int) throws java.io.IOException
meth public long cost()
supr org.apache.lucene.search.BulkScorer
hfds iterator,scorer,twoPhase

CLSS public org.apache.lucene.search.WildcardQuery
cons public init(org.apache.lucene.index.Term)
cons public init(org.apache.lucene.index.Term,int)
cons public init(org.apache.lucene.index.Term,int,org.apache.lucene.search.MultiTermQuery$RewriteMethod)
fld public final static char WILDCARD_CHAR = '?'
fld public final static char WILDCARD_ESCAPE = '\u005c'
fld public final static char WILDCARD_STRING = '*'
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.index.Term getTerm()
meth public static org.apache.lucene.util.automaton.Automaton toAutomaton(org.apache.lucene.index.Term)
supr org.apache.lucene.search.AutomatonQuery

CLSS public org.apache.lucene.search.highlight.DefaultEncoder
cons public init()
intf org.apache.lucene.search.highlight.Encoder
meth public java.lang.String encodeText(java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.apache.lucene.search.highlight.Encoder
meth public abstract java.lang.String encodeText(java.lang.String)

CLSS public abstract interface org.apache.lucene.search.highlight.Formatter
meth public abstract java.lang.String highlightTerm(java.lang.String,org.apache.lucene.search.highlight.TokenGroup)

CLSS public abstract interface org.apache.lucene.search.highlight.Fragmenter
meth public abstract boolean isNewFragment()
meth public abstract void start(java.lang.String,org.apache.lucene.analysis.TokenStream)

CLSS public org.apache.lucene.search.highlight.GradientFormatter
cons public init(float,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
fld protected boolean highlightBackground
fld protected boolean highlightForeground
intf org.apache.lucene.search.highlight.Formatter
meth protected java.lang.String getBackgroundColorString(float)
meth protected java.lang.String getForegroundColorString(float)
meth public final static int hexToInt(java.lang.String)
meth public java.lang.String highlightTerm(java.lang.String,org.apache.lucene.search.highlight.TokenGroup)
supr java.lang.Object
hfds bgBMax,bgBMin,bgGMax,bgGMin,bgRMax,bgRMin,fgBMax,fgBMin,fgGMax,fgGMin,fgRMax,fgRMin,hexDigits,maxScore

CLSS public org.apache.lucene.search.highlight.Highlighter
cons public init(org.apache.lucene.search.highlight.Formatter,org.apache.lucene.search.highlight.Encoder,org.apache.lucene.search.highlight.Scorer)
cons public init(org.apache.lucene.search.highlight.Formatter,org.apache.lucene.search.highlight.Scorer)
cons public init(org.apache.lucene.search.highlight.Scorer)
fld public final static int DEFAULT_MAX_CHARS_TO_ANALYZE = 51200
meth public final java.lang.String getBestFragment(org.apache.lucene.analysis.Analyzer,java.lang.String,java.lang.String) throws java.io.IOException,org.apache.lucene.search.highlight.InvalidTokenOffsetsException
meth public final java.lang.String getBestFragment(org.apache.lucene.analysis.TokenStream,java.lang.String) throws java.io.IOException,org.apache.lucene.search.highlight.InvalidTokenOffsetsException
meth public final java.lang.String getBestFragments(org.apache.lucene.analysis.TokenStream,java.lang.String,int,java.lang.String) throws java.io.IOException,org.apache.lucene.search.highlight.InvalidTokenOffsetsException
meth public final java.lang.String[] getBestFragments(org.apache.lucene.analysis.Analyzer,java.lang.String,java.lang.String,int) throws java.io.IOException,org.apache.lucene.search.highlight.InvalidTokenOffsetsException
meth public final java.lang.String[] getBestFragments(org.apache.lucene.analysis.TokenStream,java.lang.String,int) throws java.io.IOException,org.apache.lucene.search.highlight.InvalidTokenOffsetsException
meth public final org.apache.lucene.search.highlight.TextFragment[] getBestTextFragments(org.apache.lucene.analysis.TokenStream,java.lang.String,boolean,int) throws java.io.IOException,org.apache.lucene.search.highlight.InvalidTokenOffsetsException
meth public int getMaxDocCharsToAnalyze()
meth public org.apache.lucene.search.highlight.Encoder getEncoder()
meth public org.apache.lucene.search.highlight.Fragmenter getTextFragmenter()
meth public org.apache.lucene.search.highlight.Scorer getFragmentScorer()
meth public void setEncoder(org.apache.lucene.search.highlight.Encoder)
meth public void setFragmentScorer(org.apache.lucene.search.highlight.Scorer)
meth public void setMaxDocCharsToAnalyze(int)
meth public void setTextFragmenter(org.apache.lucene.search.highlight.Fragmenter)
supr java.lang.Object
hfds encoder,formatter,fragmentScorer,maxDocCharsToAnalyze,textFragmenter
hcls FragmentQueue

CLSS public org.apache.lucene.search.highlight.InvalidTokenOffsetsException
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public org.apache.lucene.search.highlight.NullFragmenter
cons public init()
intf org.apache.lucene.search.highlight.Fragmenter
meth public boolean isNewFragment()
meth public void start(java.lang.String,org.apache.lucene.analysis.TokenStream)
supr java.lang.Object

CLSS public final org.apache.lucene.search.highlight.OffsetLimitTokenFilter
cons public init(org.apache.lucene.analysis.TokenStream,int)
meth public boolean incrementToken() throws java.io.IOException
meth public void reset() throws java.io.IOException
supr org.apache.lucene.analysis.TokenFilter
hfds offsetAttrib,offsetCount,offsetLimit

CLSS public org.apache.lucene.search.highlight.PositionSpan
cons public init(int,int)
supr java.lang.Object
hfds end,start

CLSS public org.apache.lucene.search.highlight.QueryScorer
cons public init(org.apache.lucene.search.Query)
cons public init(org.apache.lucene.search.Query,java.lang.String)
cons public init(org.apache.lucene.search.Query,java.lang.String,java.lang.String)
cons public init(org.apache.lucene.search.Query,org.apache.lucene.index.IndexReader,java.lang.String)
cons public init(org.apache.lucene.search.Query,org.apache.lucene.index.IndexReader,java.lang.String,java.lang.String)
cons public init(org.apache.lucene.search.highlight.WeightedSpanTerm[])
intf org.apache.lucene.search.highlight.Scorer
meth protected org.apache.lucene.search.highlight.WeightedSpanTermExtractor newTermExtractor(java.lang.String)
meth public boolean isExpandMultiTermQuery()
meth public boolean isUsePayloads()
meth public float getFragmentScore()
meth public float getMaxTermWeight()
meth public float getTokenScore()
meth public org.apache.lucene.analysis.TokenStream init(org.apache.lucene.analysis.TokenStream) throws java.io.IOException
meth public org.apache.lucene.search.highlight.WeightedSpanTerm getWeightedSpanTerm(java.lang.String)
meth public void setExpandMultiTermQuery(boolean)
meth public void setMaxDocCharsToAnalyze(int)
meth public void setUsePayloads(boolean)
meth public void setWrapIfNotCachingTokenFilter(boolean)
meth public void startFragment(org.apache.lucene.search.highlight.TextFragment)
supr java.lang.Object
hfds defaultField,expandMultiTermQuery,field,fieldWeightedSpanTerms,foundTerms,maxCharsToAnalyze,maxTermWeight,posIncAtt,position,query,reader,skipInitExtractor,termAtt,totalScore,usePayloads,wrapToCaching

CLSS public final org.apache.lucene.search.highlight.QueryTermExtractor
cons public init()
meth public final static org.apache.lucene.search.highlight.WeightedTerm[] getIdfWeightedTerms(org.apache.lucene.search.Query,org.apache.lucene.index.IndexReader,java.lang.String)
meth public final static org.apache.lucene.search.highlight.WeightedTerm[] getTerms(org.apache.lucene.search.Query)
meth public final static org.apache.lucene.search.highlight.WeightedTerm[] getTerms(org.apache.lucene.search.Query,boolean)
meth public static org.apache.lucene.search.highlight.WeightedTerm[] getTerms(org.apache.lucene.search.Query,boolean,java.lang.String)
supr java.lang.Object
hfds EMPTY_INDEXSEARCHER
hcls BoostedTermExtractor

CLSS public org.apache.lucene.search.highlight.QueryTermScorer
cons public init(org.apache.lucene.search.Query)
cons public init(org.apache.lucene.search.Query,java.lang.String)
cons public init(org.apache.lucene.search.Query,org.apache.lucene.index.IndexReader,java.lang.String)
cons public init(org.apache.lucene.search.highlight.WeightedTerm[])
intf org.apache.lucene.search.highlight.Scorer
meth public float getFragmentScore()
meth public float getMaxTermWeight()
meth public float getTokenScore()
meth public org.apache.lucene.analysis.TokenStream init(org.apache.lucene.analysis.TokenStream)
meth public void allFragmentsProcessed()
meth public void startFragment(org.apache.lucene.search.highlight.TextFragment)
supr java.lang.Object
hfds currentTextFragment,maxTermWeight,termAtt,termsToFind,totalScore,uniqueTermsInFragment

CLSS public abstract interface org.apache.lucene.search.highlight.Scorer
meth public abstract float getFragmentScore()
meth public abstract float getTokenScore()
meth public abstract org.apache.lucene.analysis.TokenStream init(org.apache.lucene.analysis.TokenStream) throws java.io.IOException
meth public abstract void startFragment(org.apache.lucene.search.highlight.TextFragment)

CLSS public org.apache.lucene.search.highlight.SimpleFragmenter
cons public init()
cons public init(int)
intf org.apache.lucene.search.highlight.Fragmenter
meth public boolean isNewFragment()
meth public int getFragmentSize()
meth public void setFragmentSize(int)
meth public void start(java.lang.String,org.apache.lucene.analysis.TokenStream)
supr java.lang.Object
hfds DEFAULT_FRAGMENT_SIZE,currentNumFrags,fragmentSize,offsetAtt

CLSS public org.apache.lucene.search.highlight.SimpleHTMLEncoder
cons public init()
intf org.apache.lucene.search.highlight.Encoder
meth public final static java.lang.String htmlEncode(java.lang.String)
meth public java.lang.String encodeText(java.lang.String)
supr java.lang.Object

CLSS public org.apache.lucene.search.highlight.SimpleHTMLFormatter
cons public init()
cons public init(java.lang.String,java.lang.String)
intf org.apache.lucene.search.highlight.Formatter
meth public java.lang.String highlightTerm(java.lang.String,org.apache.lucene.search.highlight.TokenGroup)
supr java.lang.Object
hfds DEFAULT_POST_TAG,DEFAULT_PRE_TAG,postTag,preTag

CLSS public org.apache.lucene.search.highlight.SimpleSpanFragmenter
cons public init(org.apache.lucene.search.highlight.QueryScorer)
cons public init(org.apache.lucene.search.highlight.QueryScorer,int)
intf org.apache.lucene.search.highlight.Fragmenter
meth public boolean isNewFragment()
meth public void start(java.lang.String,org.apache.lucene.analysis.TokenStream)
supr java.lang.Object
hfds DEFAULT_FRAGMENT_SIZE,currentNumFrags,fragmentSize,offsetAtt,posIncAtt,position,queryScorer,termAtt,textSize,waitForPos

CLSS public org.apache.lucene.search.highlight.SpanGradientFormatter
cons public init(float,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String highlightTerm(java.lang.String,org.apache.lucene.search.highlight.TokenGroup)
supr org.apache.lucene.search.highlight.GradientFormatter
hfds EXTRA,TEMPLATE

CLSS public org.apache.lucene.search.highlight.TermVectorLeafReader
cons public init(java.lang.String,org.apache.lucene.index.Terms)
meth protected void doClose() throws java.io.IOException
meth public int maxDoc()
meth public int numDocs()
meth public org.apache.lucene.index.BinaryDocValues getBinaryDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.ByteVectorValues getByteVectorValues(java.lang.String)
meth public org.apache.lucene.index.FieldInfos getFieldInfos()
meth public org.apache.lucene.index.Fields getTermVectors(int) throws java.io.IOException
meth public org.apache.lucene.index.FloatVectorValues getFloatVectorValues(java.lang.String)
meth public org.apache.lucene.index.IndexReader$CacheHelper getCoreCacheHelper()
meth public org.apache.lucene.index.IndexReader$CacheHelper getReaderCacheHelper()
meth public org.apache.lucene.index.LeafMetaData getMetaData()
meth public org.apache.lucene.index.NumericDocValues getNormValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.NumericDocValues getNumericDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.PointValues getPointValues(java.lang.String)
meth public org.apache.lucene.index.SortedDocValues getSortedDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedNumericDocValues getSortedNumericDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedSetDocValues getSortedSetDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.StoredFields storedFields() throws java.io.IOException
meth public org.apache.lucene.index.TermVectors termVectors() throws java.io.IOException
meth public org.apache.lucene.index.Terms terms(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.util.Bits getLiveDocs()
meth public void checkIntegrity() throws java.io.IOException
meth public void document(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
meth public void searchNearestVectors(java.lang.String,byte[],org.apache.lucene.search.KnnCollector,org.apache.lucene.util.Bits)
meth public void searchNearestVectors(java.lang.String,float[],org.apache.lucene.search.KnnCollector,org.apache.lucene.util.Bits)
supr org.apache.lucene.index.LeafReader
hfds fieldInfos,fields

CLSS public org.apache.lucene.search.highlight.TextFragment
cons public init(java.lang.CharSequence,int,int)
meth public boolean follows(org.apache.lucene.search.highlight.TextFragment)
meth public float getScore()
meth public int getFragNum()
meth public java.lang.String toString()
meth public void merge(org.apache.lucene.search.highlight.TextFragment)
supr java.lang.Object
hfds fragNum,markedUpText,score,textEndPos,textStartPos

CLSS public org.apache.lucene.search.highlight.TokenGroup
cons public init(org.apache.lucene.analysis.TokenStream)
meth public float getScore(int)
meth public float getTotalScore()
meth public int getEndOffset()
meth public int getNumTokens()
meth public int getStartOffset()
supr java.lang.Object
hfds MAX_NUM_TOKENS_PER_GROUP,endOffset,matchEndOffset,matchStartOffset,numTokens,offsetAtt,scores,startOffset,tot

CLSS public org.apache.lucene.search.highlight.TokenSources
meth public static org.apache.lucene.analysis.TokenStream getAnyTokenStream(org.apache.lucene.index.IndexReader,int,java.lang.String,org.apache.lucene.analysis.Analyzer) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static org.apache.lucene.analysis.TokenStream getAnyTokenStream(org.apache.lucene.index.IndexReader,int,java.lang.String,org.apache.lucene.document.Document,org.apache.lucene.analysis.Analyzer) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static org.apache.lucene.analysis.TokenStream getTermVectorTokenStreamOrNull(java.lang.String,org.apache.lucene.index.Fields,int) throws java.io.IOException
meth public static org.apache.lucene.analysis.TokenStream getTokenStream(java.lang.String,java.lang.String,org.apache.lucene.analysis.Analyzer)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static org.apache.lucene.analysis.TokenStream getTokenStream(java.lang.String,org.apache.lucene.index.Fields,java.lang.String,org.apache.lucene.analysis.Analyzer,int) throws java.io.IOException
meth public static org.apache.lucene.analysis.TokenStream getTokenStream(org.apache.lucene.document.Document,java.lang.String,org.apache.lucene.analysis.Analyzer)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static org.apache.lucene.analysis.TokenStream getTokenStream(org.apache.lucene.index.IndexReader,int,java.lang.String,org.apache.lucene.analysis.Analyzer) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static org.apache.lucene.analysis.TokenStream getTokenStream(org.apache.lucene.index.Terms) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static org.apache.lucene.analysis.TokenStream getTokenStream(org.apache.lucene.index.Terms,boolean) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static org.apache.lucene.analysis.TokenStream getTokenStreamWithOffsets(org.apache.lucene.index.IndexReader,int,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
supr java.lang.Object

CLSS public final org.apache.lucene.search.highlight.TokenStreamFromTermVector
cons public init(org.apache.lucene.index.Terms,int) throws java.io.IOException
meth public boolean incrementToken() throws java.io.IOException
meth public org.apache.lucene.index.Terms getTermVectorTerms()
meth public void reset() throws java.io.IOException
supr org.apache.lucene.analysis.TokenStream
hfds firstToken,incrementToken,initialized,maxStartOffset,offsetAttribute,payloadAttribute,payloadsBytesRefArray,positionIncrementAttribute,spareBytesRefBuilder,termAttribute,termCharsBuilder,vector
hcls TokenLL

CLSS public org.apache.lucene.search.highlight.WeightedSpanTerm
cons public init(float,java.lang.String)
cons public init(float,java.lang.String,boolean)
meth public boolean checkPosition(int)
meth public boolean isPositionSensitive()
meth public java.util.List<org.apache.lucene.search.highlight.PositionSpan> getPositionSpans()
meth public void addPositionSpans(java.util.List<org.apache.lucene.search.highlight.PositionSpan>)
meth public void setPositionSensitive(boolean)
supr org.apache.lucene.search.highlight.WeightedTerm
hfds positionSensitive,positionSpans

CLSS public org.apache.lucene.search.highlight.WeightedSpanTermExtractor
cons public init()
cons public init(java.lang.String)
innr protected static PositionCheckingMap
meth protected boolean fieldNameComparator(java.lang.String)
meth protected boolean isQueryUnsupported(java.lang.Class<? extends org.apache.lucene.search.Query>)
meth protected boolean mustRewriteQuery(org.apache.lucene.queries.spans.SpanQuery)
meth protected final void setMaxDocCharsToAnalyze(int)
meth protected org.apache.lucene.index.LeafReaderContext getLeafContext() throws java.io.IOException
meth protected void collectSpanQueryFields(org.apache.lucene.queries.spans.SpanQuery,java.util.Set<java.lang.String>)
meth protected void extract(org.apache.lucene.search.Query,float,java.util.Map<java.lang.String,org.apache.lucene.search.highlight.WeightedSpanTerm>) throws java.io.IOException
meth protected void extractUnknownQuery(org.apache.lucene.search.Query,java.util.Map<java.lang.String,org.apache.lucene.search.highlight.WeightedSpanTerm>) throws java.io.IOException
meth protected void extractWeightedSpanTerms(java.util.Map<java.lang.String,org.apache.lucene.search.highlight.WeightedSpanTerm>,org.apache.lucene.queries.spans.SpanQuery,float) throws java.io.IOException
meth protected void extractWeightedTerms(java.util.Map<java.lang.String,org.apache.lucene.search.highlight.WeightedSpanTerm>,org.apache.lucene.search.Query,float) throws java.io.IOException
meth public boolean getExpandMultiTermQuery()
meth public boolean isCachedTokenStream()
meth public boolean isUsePayloads()
meth public java.util.Map<java.lang.String,org.apache.lucene.search.highlight.WeightedSpanTerm> getWeightedSpanTerms(org.apache.lucene.search.Query,float,org.apache.lucene.analysis.TokenStream) throws java.io.IOException
meth public java.util.Map<java.lang.String,org.apache.lucene.search.highlight.WeightedSpanTerm> getWeightedSpanTerms(org.apache.lucene.search.Query,float,org.apache.lucene.analysis.TokenStream,java.lang.String) throws java.io.IOException
meth public java.util.Map<java.lang.String,org.apache.lucene.search.highlight.WeightedSpanTerm> getWeightedSpanTermsWithScores(org.apache.lucene.search.Query,float,org.apache.lucene.analysis.TokenStream,java.lang.String,org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.analysis.TokenStream getTokenStream()
meth public void setExpandMultiTermQuery(boolean)
meth public void setUsePayloads(boolean)
meth public void setWrapIfNotCachingTokenFilter(boolean)
supr java.lang.Object
hfds cachedTokenStream,defaultField,expandMultiTermQuery,fieldName,internalReader,maxDocCharsToAnalyze,tokenStream,usePayloads,wrapToCaching
hcls DelegatingLeafReader

CLSS protected static org.apache.lucene.search.highlight.WeightedSpanTermExtractor$PositionCheckingMap<%0 extends java.lang.Object>
 outer org.apache.lucene.search.highlight.WeightedSpanTermExtractor
cons protected init()
meth public org.apache.lucene.search.highlight.WeightedSpanTerm put({org.apache.lucene.search.highlight.WeightedSpanTermExtractor$PositionCheckingMap%0},org.apache.lucene.search.highlight.WeightedSpanTerm)
meth public void putAll(java.util.Map<? extends {org.apache.lucene.search.highlight.WeightedSpanTermExtractor$PositionCheckingMap%0},? extends org.apache.lucene.search.highlight.WeightedSpanTerm>)
supr java.util.HashMap<{org.apache.lucene.search.highlight.WeightedSpanTermExtractor$PositionCheckingMap%0},org.apache.lucene.search.highlight.WeightedSpanTerm>

CLSS public org.apache.lucene.search.highlight.WeightedTerm
cons public init(float,java.lang.String)
meth public float getWeight()
meth public java.lang.String getTerm()
meth public void setTerm(java.lang.String)
meth public void setWeight(float)
supr java.lang.Object
hfds term,weight

CLSS public org.apache.lucene.store.AlreadyClosedException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.IllegalStateException

CLSS public abstract org.apache.lucene.store.BaseDirectory
cons protected init(org.apache.lucene.store.LockFactory)
fld protected final org.apache.lucene.store.LockFactory lockFactory
fld protected volatile boolean isOpen
meth protected final void ensureOpen()
meth public final org.apache.lucene.store.Lock obtainLock(java.lang.String) throws java.io.IOException
meth public java.lang.String toString()
supr org.apache.lucene.store.Directory

CLSS public org.apache.lucene.store.BufferedChecksum
cons public init(java.util.zip.Checksum)
cons public init(java.util.zip.Checksum,int)
fld public final static int DEFAULT_BUFFERSIZE = 1024
intf java.util.zip.Checksum
meth public long getValue()
meth public void reset()
meth public void update(byte[],int,int)
meth public void update(int)
supr java.lang.Object
hfds buffer,in,upto

CLSS public org.apache.lucene.store.BufferedChecksumIndexInput
cons public init(org.apache.lucene.store.IndexInput)
meth public byte readByte() throws java.io.IOException
meth public long getChecksum()
meth public long getFilePointer()
meth public long length()
meth public org.apache.lucene.store.IndexInput clone()
meth public org.apache.lucene.store.IndexInput slice(java.lang.String,long,long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void readBytes(byte[],int,int) throws java.io.IOException
supr org.apache.lucene.store.ChecksumIndexInput
hfds digest,main

CLSS public abstract org.apache.lucene.store.BufferedIndexInput
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,org.apache.lucene.store.IOContext)
fld public final static int BUFFER_SIZE = 1024
fld public final static int MERGE_BUFFER_SIZE = 4096
fld public final static int MIN_BUFFER_SIZE = 8
intf org.apache.lucene.store.RandomAccessInput
meth protected abstract void readInternal(java.nio.ByteBuffer) throws java.io.IOException
meth protected abstract void seekInternal(long) throws java.io.IOException
meth public final byte readByte() throws java.io.IOException
meth public final byte readByte(long) throws java.io.IOException
meth public final int getBufferSize()
meth public final int readInt() throws java.io.IOException
meth public final int readInt(long) throws java.io.IOException
meth public final int readVInt() throws java.io.IOException
meth public final long getFilePointer()
meth public final long readLong() throws java.io.IOException
meth public final long readLong(long) throws java.io.IOException
meth public final long readVLong() throws java.io.IOException
meth public final short readShort() throws java.io.IOException
meth public final short readShort(long) throws java.io.IOException
meth public final void readBytes(byte[],int,int) throws java.io.IOException
meth public final void readBytes(byte[],int,int,boolean) throws java.io.IOException
meth public final void seek(long) throws java.io.IOException
meth public org.apache.lucene.store.BufferedIndexInput clone()
meth public org.apache.lucene.store.IndexInput slice(java.lang.String,long,long) throws java.io.IOException
meth public static int bufferSize(org.apache.lucene.store.IOContext)
meth public static org.apache.lucene.store.BufferedIndexInput wrap(java.lang.String,org.apache.lucene.store.IndexInput,long,long)
meth public void readFloats(float[],int,int) throws java.io.IOException
meth public void readInts(int[],int,int) throws java.io.IOException
meth public void readLongs(long[],int,int) throws java.io.IOException
supr org.apache.lucene.store.IndexInput
hfds EMPTY_BYTEBUFFER,buffer,bufferSize,bufferStart
hcls SlicedIndexInput

CLSS public final org.apache.lucene.store.ByteArrayDataInput
cons public init()
cons public init(byte[])
cons public init(byte[],int,int)
meth public boolean eof()
meth public byte readByte()
meth public int getPosition()
meth public int length()
meth public int readInt()
meth public int readVInt()
meth public long readLong()
meth public long readVLong()
meth public short readShort()
meth public void readBytes(byte[],int,int)
meth public void reset(byte[])
meth public void reset(byte[],int,int)
meth public void rewind()
meth public void setPosition(int)
meth public void skipBytes(long)
supr org.apache.lucene.store.DataInput
hfds bytes,limit,pos

CLSS public org.apache.lucene.store.ByteArrayDataOutput
cons public init()
cons public init(byte[])
cons public init(byte[],int,int)
meth public int getPosition()
meth public void reset(byte[])
meth public void reset(byte[],int,int)
meth public void writeByte(byte)
meth public void writeBytes(byte[],int,int)
meth public void writeInt(int)
meth public void writeLong(long)
meth public void writeShort(short)
supr org.apache.lucene.store.DataOutput
hfds bytes,limit,pos

CLSS public abstract org.apache.lucene.store.ByteBufferIndexInput
fld protected boolean isClone
fld protected final int chunkSizePower
fld protected final java.lang.Object guard
fld protected final long chunkSizeMask
fld protected final long length
fld protected int curBufIndex
fld protected java.nio.ByteBuffer curBuf
fld protected java.nio.ByteBuffer[] buffers
intf org.apache.lucene.store.RandomAccessInput
meth protected org.apache.lucene.store.ByteBufferIndexInput buildSlice(java.lang.String,long,long)
meth protected org.apache.lucene.store.ByteBufferIndexInput newCloneInstance(java.lang.String,java.nio.ByteBuffer[],int,long)
meth protected void setCurBuf(java.nio.ByteBuffer)
meth public byte readByte(long) throws java.io.IOException
meth public final byte readByte() throws java.io.IOException
meth public final int readInt() throws java.io.IOException
meth public final long length()
meth public final long readLong() throws java.io.IOException
meth public final org.apache.lucene.store.ByteBufferIndexInput clone()
meth public final org.apache.lucene.store.ByteBufferIndexInput slice(java.lang.String,long,long)
meth public final short readShort() throws java.io.IOException
meth public final void close() throws java.io.IOException
meth public final void readBytes(byte[],int,int) throws java.io.IOException
meth public final void readFloats(float[],int,int) throws java.io.IOException
meth public int readInt(long) throws java.io.IOException
meth public long getFilePointer()
meth public long readLong(long) throws java.io.IOException
meth public short readShort(long) throws java.io.IOException
meth public static org.apache.lucene.store.ByteBufferIndexInput newInstance(java.lang.String,java.nio.ByteBuffer[],long,int,org.apache.lucene.store.ByteBufferGuard)
meth public void readInts(int[],int,int) throws java.io.IOException
meth public void readLongs(long[],int,int) throws java.io.IOException
meth public void seek(long) throws java.io.IOException
supr org.apache.lucene.store.IndexInput
hfds EMPTY_FLOATBUFFER,EMPTY_INTBUFFER,EMPTY_LONGBUFFER,curFloatBufferViews,curIntBufferViews,curLongBufferViews
hcls MultiBufferImpl,SingleBufferImpl

CLSS public final org.apache.lucene.store.ByteBuffersDataInput
cons public init(java.util.List<java.nio.ByteBuffer>)
intf org.apache.lucene.store.RandomAccessInput
intf org.apache.lucene.util.Accountable
meth public byte readByte() throws java.io.EOFException
meth public byte readByte(long)
meth public int readInt() throws java.io.IOException
meth public int readInt(long)
meth public java.lang.String toString()
meth public long length()
meth public long position()
meth public long ramBytesUsed()
meth public long readLong() throws java.io.IOException
meth public long readLong(long)
meth public long size()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public org.apache.lucene.store.ByteBuffersDataInput slice(long,long)
meth public short readShort() throws java.io.IOException
meth public short readShort(long)
meth public void readBytes(byte[],int,int) throws java.io.EOFException
meth public void readBytes(java.nio.ByteBuffer,int) throws java.io.EOFException
meth public void readFloats(float[],int,int) throws java.io.EOFException
meth public void readLongs(long[],int,int) throws java.io.EOFException
meth public void seek(long) throws java.io.EOFException
meth public void skipBytes(long) throws java.io.IOException
supr org.apache.lucene.store.DataInput
hfds blockBits,blockMask,blocks,floatBuffers,length,longBuffers,offset,pos

CLSS public final org.apache.lucene.store.ByteBuffersDataOutput
cons public init()
cons public init(int,int,java.util.function.IntFunction<java.nio.ByteBuffer>,java.util.function.Consumer<java.nio.ByteBuffer>)
cons public init(long)
fld public final static int DEFAULT_MAX_BITS_PER_BLOCK = 26
fld public final static int DEFAULT_MIN_BITS_PER_BLOCK = 10
fld public final static int LIMIT_MAX_BITS_PER_BLOCK = 31
fld public final static int LIMIT_MIN_BITS_PER_BLOCK = 1
fld public final static java.util.function.Consumer<java.nio.ByteBuffer> NO_REUSE
fld public final static java.util.function.IntFunction<java.nio.ByteBuffer> ALLOCATE_BB_ON_HEAP
innr public final static ByteBufferRecycler
intf org.apache.lucene.util.Accountable
meth public byte[] toArrayCopy()
meth public java.lang.String toString()
meth public java.util.ArrayList<java.nio.ByteBuffer> toBufferList()
meth public java.util.ArrayList<java.nio.ByteBuffer> toWriteableBufferList()
meth public long ramBytesUsed()
meth public long size()
meth public org.apache.lucene.store.ByteBuffersDataInput toDataInput()
meth public static org.apache.lucene.store.ByteBuffersDataOutput newResettableInstance()
meth public void copyBytes(org.apache.lucene.store.DataInput,long) throws java.io.IOException
meth public void copyTo(org.apache.lucene.store.DataOutput) throws java.io.IOException
meth public void reset()
meth public void writeByte(byte)
meth public void writeBytes(byte[])
meth public void writeBytes(byte[],int)
meth public void writeBytes(byte[],int,int)
meth public void writeBytes(java.nio.ByteBuffer)
meth public void writeInt(int)
meth public void writeLong(long)
meth public void writeMapOfStrings(java.util.Map<java.lang.String,java.lang.String>)
meth public void writeSetOfStrings(java.util.Set<java.lang.String>)
meth public void writeShort(short)
meth public void writeString(java.lang.String)
supr org.apache.lucene.store.DataOutput
hfds EMPTY,EMPTY_BYTE_ARRAY,MAX_BLOCKS_BEFORE_BLOCK_EXPANSION,MAX_CHARS_PER_WINDOW,blockAllocate,blockBits,blockReuse,blocks,currentBlock,maxBitsPerBlock,ramBytesUsed

CLSS public final static org.apache.lucene.store.ByteBuffersDataOutput$ByteBufferRecycler
 outer org.apache.lucene.store.ByteBuffersDataOutput
cons public init(java.util.function.IntFunction<java.nio.ByteBuffer>)
meth public java.nio.ByteBuffer allocate(int)
meth public void reuse(java.nio.ByteBuffer)
supr java.lang.Object
hfds delegate,reuse

CLSS public final org.apache.lucene.store.ByteBuffersDirectory
cons public init()
cons public init(org.apache.lucene.store.LockFactory)
cons public init(org.apache.lucene.store.LockFactory,java.util.function.Supplier<org.apache.lucene.store.ByteBuffersDataOutput>,java.util.function.BiFunction<java.lang.String,org.apache.lucene.store.ByteBuffersDataOutput,org.apache.lucene.store.IndexInput>)
fld public final static java.util.function.BiFunction<java.lang.String,org.apache.lucene.store.ByteBuffersDataOutput,org.apache.lucene.store.IndexInput> OUTPUT_AS_BYTE_ARRAY
fld public final static java.util.function.BiFunction<java.lang.String,org.apache.lucene.store.ByteBuffersDataOutput,org.apache.lucene.store.IndexInput> OUTPUT_AS_MANY_BUFFERS
fld public final static java.util.function.BiFunction<java.lang.String,org.apache.lucene.store.ByteBuffersDataOutput,org.apache.lucene.store.IndexInput> OUTPUT_AS_MANY_BUFFERS_LUCENE
fld public final static java.util.function.BiFunction<java.lang.String,org.apache.lucene.store.ByteBuffersDataOutput,org.apache.lucene.store.IndexInput> OUTPUT_AS_ONE_BUFFER
meth public boolean fileExists(java.lang.String)
meth public java.lang.String[] listAll() throws java.io.IOException
meth public java.util.Set<java.lang.String> getPendingDeletions()
meth public long fileLength(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.store.IndexInput openInput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.IndexOutput createOutput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.IndexOutput createTempOutput(java.lang.String,java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void deleteFile(java.lang.String) throws java.io.IOException
meth public void rename(java.lang.String,java.lang.String) throws java.io.IOException
meth public void sync(java.util.Collection<java.lang.String>) throws java.io.IOException
meth public void syncMetaData() throws java.io.IOException
supr org.apache.lucene.store.BaseDirectory
hfds bbOutputSupplier,files,outputToInput,tempFileName
hcls FileEntry

CLSS public final org.apache.lucene.store.ByteBuffersIndexInput
cons public init(org.apache.lucene.store.ByteBuffersDataInput,java.lang.String)
intf org.apache.lucene.store.RandomAccessInput
meth public byte readByte() throws java.io.IOException
meth public byte readByte(long) throws java.io.IOException
meth public int readInt() throws java.io.IOException
meth public int readInt(long) throws java.io.IOException
meth public int readVInt() throws java.io.IOException
meth public int readZInt() throws java.io.IOException
meth public java.lang.String readString() throws java.io.IOException
meth public java.util.Map<java.lang.String,java.lang.String> readMapOfStrings() throws java.io.IOException
meth public java.util.Set<java.lang.String> readSetOfStrings() throws java.io.IOException
meth public long getFilePointer()
meth public long length()
meth public long readLong() throws java.io.IOException
meth public long readLong(long) throws java.io.IOException
meth public long readVLong() throws java.io.IOException
meth public long readZLong() throws java.io.IOException
meth public org.apache.lucene.store.ByteBuffersIndexInput slice(java.lang.String,long,long) throws java.io.IOException
meth public org.apache.lucene.store.IndexInput clone()
meth public org.apache.lucene.store.RandomAccessInput randomAccessSlice(long,long) throws java.io.IOException
meth public short readShort() throws java.io.IOException
meth public short readShort(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void readBytes(byte[],int,int) throws java.io.IOException
meth public void readBytes(byte[],int,int,boolean) throws java.io.IOException
meth public void readFloats(float[],int,int) throws java.io.IOException
meth public void readLongs(long[],int,int) throws java.io.IOException
meth public void seek(long) throws java.io.IOException
meth public void skipBytes(long) throws java.io.IOException
supr org.apache.lucene.store.IndexInput
hfds in

CLSS public final org.apache.lucene.store.ByteBuffersIndexOutput
cons public init(org.apache.lucene.store.ByteBuffersDataOutput,java.lang.String,java.lang.String)
cons public init(org.apache.lucene.store.ByteBuffersDataOutput,java.lang.String,java.lang.String,java.util.zip.Checksum,java.util.function.Consumer<org.apache.lucene.store.ByteBuffersDataOutput>)
meth public byte[] toArrayCopy()
meth public long getChecksum() throws java.io.IOException
meth public long getFilePointer()
meth public void close() throws java.io.IOException
meth public void copyBytes(org.apache.lucene.store.DataInput,long) throws java.io.IOException
meth public void writeByte(byte) throws java.io.IOException
meth public void writeBytes(byte[],int) throws java.io.IOException
meth public void writeBytes(byte[],int,int) throws java.io.IOException
meth public void writeInt(int) throws java.io.IOException
meth public void writeLong(long) throws java.io.IOException
meth public void writeMapOfStrings(java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException
meth public void writeSetOfStrings(java.util.Set<java.lang.String>) throws java.io.IOException
meth public void writeShort(short) throws java.io.IOException
meth public void writeString(java.lang.String) throws java.io.IOException
supr org.apache.lucene.store.IndexOutput
hfds checksum,delegate,lastChecksum,lastChecksumPosition,onClose

CLSS public abstract org.apache.lucene.store.ChecksumIndexInput
cons protected init(java.lang.String)
meth public abstract long getChecksum() throws java.io.IOException
meth public void seek(long) throws java.io.IOException
supr org.apache.lucene.store.IndexInput
hfds SKIP_BUFFER_SIZE,skipBuffer

CLSS public abstract org.apache.lucene.store.DataInput
cons public init()
intf java.lang.Cloneable
meth public abstract byte readByte() throws java.io.IOException
meth public abstract void readBytes(byte[],int,int) throws java.io.IOException
meth public abstract void skipBytes(long) throws java.io.IOException
meth public int readInt() throws java.io.IOException
meth public int readVInt() throws java.io.IOException
meth public int readZInt() throws java.io.IOException
meth public java.lang.String readString() throws java.io.IOException
meth public java.util.Map<java.lang.String,java.lang.String> readMapOfStrings() throws java.io.IOException
meth public java.util.Set<java.lang.String> readSetOfStrings() throws java.io.IOException
meth public long readLong() throws java.io.IOException
meth public long readVLong() throws java.io.IOException
meth public long readZLong() throws java.io.IOException
meth public org.apache.lucene.store.DataInput clone()
meth public short readShort() throws java.io.IOException
meth public void readBytes(byte[],int,int,boolean) throws java.io.IOException
meth public void readFloats(float[],int,int) throws java.io.IOException
meth public void readInts(int[],int,int) throws java.io.IOException
meth public void readLongs(long[],int,int) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.apache.lucene.store.DataOutput
cons public init()
meth public abstract void writeByte(byte) throws java.io.IOException
meth public abstract void writeBytes(byte[],int,int) throws java.io.IOException
meth public final void writeVInt(int) throws java.io.IOException
meth public final void writeVLong(long) throws java.io.IOException
meth public final void writeZInt(int) throws java.io.IOException
meth public final void writeZLong(long) throws java.io.IOException
meth public void copyBytes(org.apache.lucene.store.DataInput,long) throws java.io.IOException
meth public void writeBytes(byte[],int) throws java.io.IOException
meth public void writeInt(int) throws java.io.IOException
meth public void writeLong(long) throws java.io.IOException
meth public void writeMapOfStrings(java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException
meth public void writeSetOfStrings(java.util.Set<java.lang.String>) throws java.io.IOException
meth public void writeShort(short) throws java.io.IOException
meth public void writeString(java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds COPY_BUFFER_SIZE,copyBuffer

CLSS public abstract org.apache.lucene.store.Directory
cons public init()
intf java.io.Closeable
meth protected static java.lang.String getTempFileName(java.lang.String,java.lang.String,long)
meth protected void ensureOpen()
meth public abstract java.lang.String[] listAll() throws java.io.IOException
meth public abstract java.util.Set<java.lang.String> getPendingDeletions() throws java.io.IOException
meth public abstract long fileLength(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.store.IndexInput openInput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public abstract org.apache.lucene.store.IndexOutput createOutput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public abstract org.apache.lucene.store.IndexOutput createTempOutput(java.lang.String,java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public abstract org.apache.lucene.store.Lock obtainLock(java.lang.String) throws java.io.IOException
meth public abstract void close() throws java.io.IOException
meth public abstract void deleteFile(java.lang.String) throws java.io.IOException
meth public abstract void rename(java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract void sync(java.util.Collection<java.lang.String>) throws java.io.IOException
meth public abstract void syncMetaData() throws java.io.IOException
meth public java.lang.String toString()
meth public org.apache.lucene.store.ChecksumIndexInput openChecksumInput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public void copyFrom(org.apache.lucene.store.Directory,java.lang.String,java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.apache.lucene.store.FSDirectory
cons protected init(java.nio.file.Path,org.apache.lucene.store.LockFactory) throws java.io.IOException
fld protected final java.nio.file.Path directory
meth protected void ensureCanRead(java.lang.String) throws java.io.IOException
meth protected void fsync(java.lang.String) throws java.io.IOException
meth public java.lang.String toString()
meth public java.lang.String[] listAll() throws java.io.IOException
meth public java.nio.file.Path getDirectory()
meth public java.util.Set<java.lang.String> getPendingDeletions() throws java.io.IOException
meth public long fileLength(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.store.IndexOutput createOutput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.IndexOutput createTempOutput(java.lang.String,java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public static java.lang.String[] listAll(java.nio.file.Path) throws java.io.IOException
meth public static org.apache.lucene.store.FSDirectory open(java.nio.file.Path) throws java.io.IOException
meth public static org.apache.lucene.store.FSDirectory open(java.nio.file.Path,org.apache.lucene.store.LockFactory) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void deleteFile(java.lang.String) throws java.io.IOException
meth public void deletePendingFiles() throws java.io.IOException
meth public void rename(java.lang.String,java.lang.String) throws java.io.IOException
meth public void sync(java.util.Collection<java.lang.String>) throws java.io.IOException
meth public void syncMetaData() throws java.io.IOException
supr org.apache.lucene.store.BaseDirectory
hfds nextTempFileCounter,opsSinceLastDelete,pendingDeletes
hcls FSIndexOutput

CLSS public abstract org.apache.lucene.store.FSLockFactory
cons public init()
meth protected abstract org.apache.lucene.store.Lock obtainFSLock(org.apache.lucene.store.FSDirectory,java.lang.String) throws java.io.IOException
meth public final org.apache.lucene.store.Lock obtainLock(org.apache.lucene.store.Directory,java.lang.String) throws java.io.IOException
meth public final static org.apache.lucene.store.FSLockFactory getDefault()
supr org.apache.lucene.store.LockFactory

CLSS public org.apache.lucene.store.FileSwitchDirectory
cons public init(java.util.Set<java.lang.String>,org.apache.lucene.store.Directory,org.apache.lucene.store.Directory,boolean)
meth public java.lang.String[] listAll() throws java.io.IOException
meth public java.util.Set<java.lang.String> getPendingDeletions() throws java.io.IOException
meth public long fileLength(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.store.Directory getPrimaryDir()
meth public org.apache.lucene.store.Directory getSecondaryDir()
meth public org.apache.lucene.store.IndexInput openInput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.IndexOutput createOutput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.IndexOutput createTempOutput(java.lang.String,java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.Lock obtainLock(java.lang.String) throws java.io.IOException
meth public static java.lang.String getExtension(java.lang.String)
meth public void close() throws java.io.IOException
meth public void deleteFile(java.lang.String) throws java.io.IOException
meth public void rename(java.lang.String,java.lang.String) throws java.io.IOException
meth public void sync(java.util.Collection<java.lang.String>) throws java.io.IOException
meth public void syncMetaData() throws java.io.IOException
supr org.apache.lucene.store.Directory
hfds EXT_PATTERN,doClose,primaryDir,primaryExtensions,secondaryDir

CLSS public abstract org.apache.lucene.store.FilterDirectory
cons protected init(org.apache.lucene.store.Directory)
fld protected final org.apache.lucene.store.Directory in
meth protected void ensureOpen()
meth public final org.apache.lucene.store.Directory getDelegate()
meth public java.lang.String toString()
meth public java.lang.String[] listAll() throws java.io.IOException
meth public java.util.Set<java.lang.String> getPendingDeletions() throws java.io.IOException
meth public long fileLength(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.store.IndexInput openInput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.IndexOutput createOutput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.IndexOutput createTempOutput(java.lang.String,java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.Lock obtainLock(java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.store.Directory unwrap(org.apache.lucene.store.Directory)
meth public void close() throws java.io.IOException
meth public void deleteFile(java.lang.String) throws java.io.IOException
meth public void rename(java.lang.String,java.lang.String) throws java.io.IOException
meth public void sync(java.util.Collection<java.lang.String>) throws java.io.IOException
meth public void syncMetaData() throws java.io.IOException
supr org.apache.lucene.store.Directory

CLSS public org.apache.lucene.store.FilterIndexInput
cons public init(java.lang.String,org.apache.lucene.store.IndexInput)
fld protected final org.apache.lucene.store.IndexInput in
meth public byte readByte() throws java.io.IOException
meth public long getFilePointer()
meth public long length()
meth public org.apache.lucene.store.IndexInput getDelegate()
meth public org.apache.lucene.store.IndexInput slice(java.lang.String,long,long) throws java.io.IOException
meth public static org.apache.lucene.store.IndexInput unwrap(org.apache.lucene.store.IndexInput)
meth public void close() throws java.io.IOException
meth public void readBytes(byte[],int,int) throws java.io.IOException
meth public void seek(long) throws java.io.IOException
supr org.apache.lucene.store.IndexInput

CLSS public org.apache.lucene.store.FilterIndexOutput
cons protected init(java.lang.String,java.lang.String,org.apache.lucene.store.IndexOutput)
fld protected final org.apache.lucene.store.IndexOutput out
meth public final org.apache.lucene.store.IndexOutput getDelegate()
meth public long getChecksum() throws java.io.IOException
meth public long getFilePointer()
meth public static org.apache.lucene.store.IndexOutput unwrap(org.apache.lucene.store.IndexOutput)
meth public void close() throws java.io.IOException
meth public void writeByte(byte) throws java.io.IOException
meth public void writeBytes(byte[],int,int) throws java.io.IOException
supr org.apache.lucene.store.IndexOutput

CLSS public org.apache.lucene.store.FlushInfo
cons public init(int,long)
fld public final int numDocs
fld public final long estimatedSegmentSize
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public org.apache.lucene.store.IOContext
cons public init()
cons public init(org.apache.lucene.store.FlushInfo)
cons public init(org.apache.lucene.store.IOContext$Context)
cons public init(org.apache.lucene.store.IOContext,boolean)
cons public init(org.apache.lucene.store.MergeInfo)
fld public final boolean load
fld public final boolean readOnce
fld public final org.apache.lucene.store.FlushInfo flushInfo
fld public final org.apache.lucene.store.IOContext$Context context
fld public final org.apache.lucene.store.MergeInfo mergeInfo
fld public final static org.apache.lucene.store.IOContext DEFAULT
fld public final static org.apache.lucene.store.IOContext LOAD
fld public final static org.apache.lucene.store.IOContext READ
fld public final static org.apache.lucene.store.IOContext READONCE
innr public final static !enum Context
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final static !enum org.apache.lucene.store.IOContext$Context
 outer org.apache.lucene.store.IOContext
fld public final static org.apache.lucene.store.IOContext$Context DEFAULT
fld public final static org.apache.lucene.store.IOContext$Context FLUSH
fld public final static org.apache.lucene.store.IOContext$Context MERGE
fld public final static org.apache.lucene.store.IOContext$Context READ
meth public static org.apache.lucene.store.IOContext$Context valueOf(java.lang.String)
meth public static org.apache.lucene.store.IOContext$Context[] values()
supr java.lang.Enum<org.apache.lucene.store.IOContext$Context>

CLSS public abstract org.apache.lucene.store.IndexInput
cons protected init(java.lang.String)
intf java.io.Closeable
meth protected java.lang.String getFullSliceDescription(java.lang.String)
meth public abstract long getFilePointer()
meth public abstract long length()
meth public abstract org.apache.lucene.store.IndexInput slice(java.lang.String,long,long) throws java.io.IOException
meth public abstract void close() throws java.io.IOException
meth public abstract void seek(long) throws java.io.IOException
meth public java.lang.String toString()
meth public org.apache.lucene.store.IndexInput clone()
meth public org.apache.lucene.store.RandomAccessInput randomAccessSlice(long,long) throws java.io.IOException
meth public void skipBytes(long) throws java.io.IOException
supr org.apache.lucene.store.DataInput
hfds resourceDescription

CLSS public abstract org.apache.lucene.store.IndexOutput
cons protected init(java.lang.String,java.lang.String)
intf java.io.Closeable
meth public abstract long getChecksum() throws java.io.IOException
meth public abstract long getFilePointer()
meth public abstract void close() throws java.io.IOException
meth public final long alignFilePointer(int) throws java.io.IOException
meth public final static long alignOffset(long,int)
meth public java.lang.String getName()
meth public java.lang.String toString()
supr org.apache.lucene.store.DataOutput
hfds name,resourceDescription

CLSS public org.apache.lucene.store.InputStreamDataInput
cons public init(java.io.InputStream)
intf java.io.Closeable
meth public byte readByte() throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void readBytes(byte[],int,int) throws java.io.IOException
meth public void skipBytes(long) throws java.io.IOException
supr org.apache.lucene.store.DataInput
hfds is

CLSS public abstract org.apache.lucene.store.Lock
cons public init()
intf java.io.Closeable
meth public abstract void close() throws java.io.IOException
meth public abstract void ensureValid() throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.apache.lucene.store.LockFactory
cons public init()
meth public abstract org.apache.lucene.store.Lock obtainLock(org.apache.lucene.store.Directory,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.lucene.store.LockObtainFailedException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.io.IOException

CLSS public org.apache.lucene.store.LockReleaseFailedException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.io.IOException

CLSS public org.apache.lucene.store.LockStressTest
cons public init()
meth public static void main(java.lang.String[]) throws java.lang.Exception
supr java.lang.Object
hfds LOCK_FILE_NAME

CLSS public final org.apache.lucene.store.LockValidatingDirectoryWrapper
cons public init(org.apache.lucene.store.Directory,org.apache.lucene.store.Lock)
meth public org.apache.lucene.store.IndexOutput createOutput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public void copyFrom(org.apache.lucene.store.Directory,java.lang.String,java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public void deleteFile(java.lang.String) throws java.io.IOException
meth public void rename(java.lang.String,java.lang.String) throws java.io.IOException
meth public void sync(java.util.Collection<java.lang.String>) throws java.io.IOException
meth public void syncMetaData() throws java.io.IOException
supr org.apache.lucene.store.FilterDirectory
hfds writeLock

CLSS public org.apache.lucene.store.LockVerifyServer
cons public init()
fld public final static int START_GUN_SIGNAL = 43
meth public static void main(java.lang.String[]) throws java.lang.Exception
supr java.lang.Object

CLSS public org.apache.lucene.store.MMapDirectory
cons public init(java.nio.file.Path) throws java.io.IOException
cons public init(java.nio.file.Path,int) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(java.nio.file.Path,long) throws java.io.IOException
cons public init(java.nio.file.Path,org.apache.lucene.store.LockFactory) throws java.io.IOException
cons public init(java.nio.file.Path,org.apache.lucene.store.LockFactory,int) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
cons public init(java.nio.file.Path,org.apache.lucene.store.LockFactory,long) throws java.io.IOException
fld public final static boolean UNMAP_SUPPORTED
fld public final static java.lang.String ENABLE_MEMORY_SEGMENTS_SYSPROP = "org.apache.lucene.store.MMapDirectory.enableMemorySegments"
fld public final static java.lang.String ENABLE_UNMAP_HACK_SYSPROP = "org.apache.lucene.store.MMapDirectory.enableUnmapHack"
fld public final static java.lang.String UNMAP_NOT_SUPPORTED_REASON
fld public final static java.util.function.BiPredicate<java.lang.String,org.apache.lucene.store.IOContext> ALL_FILES
fld public final static java.util.function.BiPredicate<java.lang.String,org.apache.lucene.store.IOContext> BASED_ON_LOAD_IO_CONTEXT
fld public final static java.util.function.BiPredicate<java.lang.String,org.apache.lucene.store.IOContext> NO_FILES
fld public final static long DEFAULT_MAX_CHUNK_SIZE
meth public boolean getPreload()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean getUseUnmap()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public final long getMaxChunkSize()
meth public org.apache.lucene.store.IndexInput openInput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public void setPreload(boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public void setPreload(java.util.function.BiPredicate<java.lang.String,org.apache.lucene.store.IOContext>)
meth public void setUseUnmap(boolean)
 anno 0 java.lang.Deprecated(boolean forRemoval=true, java.lang.String since="")
supr org.apache.lucene.store.FSDirectory
hfds LOG,PROVIDER,chunkSizePower,preload
hcls MMapIndexInputProvider

CLSS public org.apache.lucene.store.MergeInfo
cons public init(int,long,boolean,int)
fld public final boolean isExternal
fld public final int mergeMaxNumSegments
fld public final int totalMaxDoc
fld public final long estimatedMergeBytes
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public org.apache.lucene.store.NIOFSDirectory
cons public init(java.nio.file.Path) throws java.io.IOException
cons public init(java.nio.file.Path,org.apache.lucene.store.LockFactory) throws java.io.IOException
meth public org.apache.lucene.store.IndexInput openInput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
supr org.apache.lucene.store.FSDirectory
hcls NIOFSIndexInput

CLSS public org.apache.lucene.store.NRTCachingDirectory
cons public init(org.apache.lucene.store.Directory,double,double)
intf org.apache.lucene.util.Accountable
meth protected boolean doCacheWrite(java.lang.String,org.apache.lucene.store.IOContext)
meth public java.lang.String toString()
meth public java.lang.String[] listAll() throws java.io.IOException
meth public java.lang.String[] listCachedFiles()
meth public long fileLength(java.lang.String) throws java.io.IOException
meth public long ramBytesUsed()
meth public org.apache.lucene.store.IndexInput openInput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.IndexOutput createOutput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.IndexOutput createTempOutput(java.lang.String,java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void deleteFile(java.lang.String) throws java.io.IOException
meth public void rename(java.lang.String,java.lang.String) throws java.io.IOException
meth public void sync(java.util.Collection<java.lang.String>) throws java.io.IOException
supr org.apache.lucene.store.FilterDirectory
hfds VERBOSE,cacheDirectory,cacheSize,closed,maxCachedBytes,maxMergeSizeBytes

CLSS public final org.apache.lucene.store.NativeFSLockFactory
fld public final static org.apache.lucene.store.NativeFSLockFactory INSTANCE
meth protected org.apache.lucene.store.Lock obtainFSLock(org.apache.lucene.store.FSDirectory,java.lang.String) throws java.io.IOException
supr org.apache.lucene.store.FSLockFactory
hfds LOCK_HELD
hcls NativeFSLock

CLSS public final org.apache.lucene.store.NoLockFactory
fld public final static org.apache.lucene.store.NoLockFactory INSTANCE
meth public org.apache.lucene.store.Lock obtainLock(org.apache.lucene.store.Directory,java.lang.String)
supr org.apache.lucene.store.LockFactory
hfds SINGLETON_LOCK
hcls NoLock

CLSS public org.apache.lucene.store.OutputStreamDataOutput
cons public init(java.io.OutputStream)
intf java.io.Closeable
meth public void close() throws java.io.IOException
meth public void writeByte(byte) throws java.io.IOException
meth public void writeBytes(byte[],int,int) throws java.io.IOException
supr org.apache.lucene.store.DataOutput
hfds os

CLSS public org.apache.lucene.store.OutputStreamIndexOutput
cons public init(java.lang.String,java.lang.String,java.io.OutputStream,int)
meth public final long getChecksum() throws java.io.IOException
meth public final long getFilePointer()
meth public final void writeByte(byte) throws java.io.IOException
meth public final void writeBytes(byte[],int,int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void writeInt(int) throws java.io.IOException
meth public void writeLong(long) throws java.io.IOException
meth public void writeShort(short) throws java.io.IOException
supr org.apache.lucene.store.IndexOutput
hfds bytesWritten,crc,flushedOnClose,os
hcls XBufferedOutputStream

CLSS public abstract interface org.apache.lucene.store.RandomAccessInput
meth public abstract byte readByte(long) throws java.io.IOException
meth public abstract int readInt(long) throws java.io.IOException
meth public abstract long length()
meth public abstract long readLong(long) throws java.io.IOException
meth public abstract short readShort(long) throws java.io.IOException

CLSS public final org.apache.lucene.store.RateLimitedIndexOutput
cons public init(org.apache.lucene.store.RateLimiter,org.apache.lucene.store.IndexOutput)
meth public void writeByte(byte) throws java.io.IOException
meth public void writeBytes(byte[],int,int) throws java.io.IOException
meth public void writeInt(int) throws java.io.IOException
meth public void writeLong(long) throws java.io.IOException
meth public void writeShort(short) throws java.io.IOException
supr org.apache.lucene.store.FilterIndexOutput
hfds bytesSinceLastPause,currentMinPauseCheckBytes,rateLimiter

CLSS public abstract org.apache.lucene.store.RateLimiter
cons public init()
innr public static SimpleRateLimiter
meth public abstract double getMBPerSec()
meth public abstract long getMinPauseCheckBytes()
meth public abstract long pause(long) throws java.io.IOException
meth public abstract void setMBPerSec(double)
supr java.lang.Object

CLSS public static org.apache.lucene.store.RateLimiter$SimpleRateLimiter
 outer org.apache.lucene.store.RateLimiter
cons public init(double)
meth public double getMBPerSec()
meth public long getMinPauseCheckBytes()
meth public long pause(long)
meth public void setMBPerSec(double)
supr org.apache.lucene.store.RateLimiter
hfds MIN_PAUSE_CHECK_MSEC,lastNS,mbPerSec,minPauseCheckBytes

CLSS public final org.apache.lucene.store.SimpleFSLockFactory
fld public final static org.apache.lucene.store.SimpleFSLockFactory INSTANCE
meth protected org.apache.lucene.store.Lock obtainFSLock(org.apache.lucene.store.FSDirectory,java.lang.String) throws java.io.IOException
supr org.apache.lucene.store.FSLockFactory
hcls SimpleFSLock

CLSS public final org.apache.lucene.store.SingleInstanceLockFactory
cons public init()
meth public org.apache.lucene.store.Lock obtainLock(org.apache.lucene.store.Directory,java.lang.String) throws java.io.IOException
supr org.apache.lucene.store.LockFactory
hfds locks
hcls SingleInstanceLock

CLSS public final org.apache.lucene.store.SleepingLockWrapper
cons public init(org.apache.lucene.store.Directory,long)
cons public init(org.apache.lucene.store.Directory,long,long)
fld public final static long LOCK_OBTAIN_WAIT_FOREVER = -1
fld public static long DEFAULT_POLL_INTERVAL
meth public java.lang.String toString()
meth public org.apache.lucene.store.Lock obtainLock(java.lang.String) throws java.io.IOException
supr org.apache.lucene.store.FilterDirectory
hfds lockWaitTimeout,pollInterval

CLSS public final org.apache.lucene.store.TrackingDirectoryWrapper
cons public init(org.apache.lucene.store.Directory)
meth public java.util.Set<java.lang.String> getCreatedFiles()
meth public org.apache.lucene.store.IndexOutput createOutput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.IndexOutput createTempOutput(java.lang.String,java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public void clearCreatedFiles()
meth public void copyFrom(org.apache.lucene.store.Directory,java.lang.String,java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public void deleteFile(java.lang.String) throws java.io.IOException
meth public void rename(java.lang.String,java.lang.String) throws java.io.IOException
supr org.apache.lucene.store.FilterDirectory
hfds createdFileNames

CLSS public final org.apache.lucene.store.VerifyingLockFactory
cons public init(org.apache.lucene.store.LockFactory,java.io.InputStream,java.io.OutputStream) throws java.io.IOException
fld public final static int MSG_LOCK_ACQUIRED = 1
fld public final static int MSG_LOCK_RELEASED = 0
meth public org.apache.lucene.store.Lock obtainLock(org.apache.lucene.store.Directory,java.lang.String) throws java.io.IOException
supr org.apache.lucene.store.LockFactory
hfds in,lf,out
hcls CheckedLock

CLSS public abstract interface org.apache.lucene.util.Accountable
fld public final static org.apache.lucene.util.Accountable NULL_ACCOUNTABLE
meth public abstract long ramBytesUsed()
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()

CLSS public org.apache.lucene.util.Accountables
meth public static java.lang.String toString(org.apache.lucene.util.Accountable)
meth public static java.util.Collection<org.apache.lucene.util.Accountable> namedAccountables(java.lang.String,java.util.Map<?,? extends org.apache.lucene.util.Accountable>)
meth public static org.apache.lucene.util.Accountable namedAccountable(java.lang.String,java.util.Collection<org.apache.lucene.util.Accountable>,long)
meth public static org.apache.lucene.util.Accountable namedAccountable(java.lang.String,long)
meth public static org.apache.lucene.util.Accountable namedAccountable(java.lang.String,org.apache.lucene.util.Accountable)
supr java.lang.Object

CLSS public final org.apache.lucene.util.ArrayUtil
fld public final static int MAX_ARRAY_LENGTH
innr public abstract interface static ByteArrayComparator
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> void introSort({%%0}[])
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> void introSort({%%0}[],int,int)
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> void timSort({%%0}[])
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> void timSort({%%0}[],int,int)
meth public static <%0 extends java.lang.Object> void introSort({%%0}[],int,int,java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> void introSort({%%0}[],java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> void select({%%0}[],int,int,int,java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> void swap({%%0}[],int,int)
meth public static <%0 extends java.lang.Object> void timSort({%%0}[],int,int,java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> void timSort({%%0}[],java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> {%%0}[] copyOfSubArray({%%0}[],int,int)
meth public static <%0 extends java.lang.Object> {%%0}[] grow({%%0}[])
meth public static <%0 extends java.lang.Object> {%%0}[] grow({%%0}[],int)
meth public static <%0 extends java.lang.Object> {%%0}[] growExact({%%0}[],int)
meth public static byte[] copyOfSubArray(byte[],int,int)
meth public static byte[] grow(byte[])
meth public static byte[] grow(byte[],int)
meth public static byte[] growExact(byte[],int)
meth public static byte[] growNoCopy(byte[],int)
meth public static char[] copyOfSubArray(char[],int,int)
meth public static char[] grow(char[])
meth public static char[] grow(char[],int)
meth public static char[] growExact(char[],int)
meth public static double[] copyOfSubArray(double[],int,int)
meth public static double[] grow(double[])
meth public static double[] grow(double[],int)
meth public static double[] growExact(double[],int)
meth public static float[] copyOfSubArray(float[],int,int)
meth public static float[] grow(float[])
meth public static float[] grow(float[],int)
meth public static float[] growExact(float[],int)
meth public static int compareUnsigned4(byte[],int,byte[],int)
meth public static int compareUnsigned8(byte[],int,byte[],int)
meth public static int hashCode(char[],int,int)
meth public static int oversize(int,int)
meth public static int parseInt(char[],int,int)
meth public static int parseInt(char[],int,int,int)
meth public static int[] copyOfSubArray(int[],int,int)
meth public static int[] grow(int[])
meth public static int[] grow(int[],int)
meth public static int[] growExact(int[],int)
meth public static int[] growNoCopy(int[],int)
meth public static long[] copyOfSubArray(long[],int,int)
meth public static long[] grow(long[])
meth public static long[] grow(long[],int)
meth public static long[] growExact(long[],int)
meth public static long[] growNoCopy(long[],int)
meth public static org.apache.lucene.util.ArrayUtil$ByteArrayComparator getUnsignedComparator(int)
meth public static short[] copyOfSubArray(short[],int,int)
meth public static short[] grow(short[])
meth public static short[] grow(short[],int)
meth public static short[] growExact(short[],int)
supr java.lang.Object

CLSS public abstract interface static org.apache.lucene.util.ArrayUtil$ByteArrayComparator
 outer org.apache.lucene.util.ArrayUtil
 anno 0 java.lang.FunctionalInterface()
meth public abstract int compare(byte[],int,byte[],int)

CLSS public abstract interface org.apache.lucene.util.Attribute

CLSS public abstract org.apache.lucene.util.AttributeFactory
cons public init()
fld public final static org.apache.lucene.util.AttributeFactory DEFAULT_ATTRIBUTE_FACTORY
innr public abstract static StaticImplementationAttributeFactory
meth public abstract org.apache.lucene.util.AttributeImpl createAttributeInstance(java.lang.Class<? extends org.apache.lucene.util.Attribute>)
meth public static <%0 extends org.apache.lucene.util.AttributeImpl> org.apache.lucene.util.AttributeFactory getStaticImplementation(org.apache.lucene.util.AttributeFactory,java.lang.Class<{%%0}>)
supr java.lang.Object
hfds NO_ARG_CTOR,NO_ARG_RETURNING_ATTRIBUTEIMPL,lookup
hcls DefaultAttributeFactory

CLSS public abstract static org.apache.lucene.util.AttributeFactory$StaticImplementationAttributeFactory<%0 extends org.apache.lucene.util.AttributeImpl>
 outer org.apache.lucene.util.AttributeFactory
cons protected init(org.apache.lucene.util.AttributeFactory,java.lang.Class<{org.apache.lucene.util.AttributeFactory$StaticImplementationAttributeFactory%0}>)
meth protected abstract {org.apache.lucene.util.AttributeFactory$StaticImplementationAttributeFactory%0} createInstance()
meth public boolean equals(java.lang.Object)
meth public final org.apache.lucene.util.AttributeImpl createAttributeInstance(java.lang.Class<? extends org.apache.lucene.util.Attribute>)
meth public int hashCode()
supr org.apache.lucene.util.AttributeFactory
hfds clazz,delegate

CLSS public abstract org.apache.lucene.util.AttributeImpl
cons public init()
intf java.lang.Cloneable
intf org.apache.lucene.util.Attribute
meth public abstract void clear()
meth public abstract void copyTo(org.apache.lucene.util.AttributeImpl)
meth public abstract void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public final java.lang.String reflectAsString(boolean)
meth public org.apache.lucene.util.AttributeImpl clone()
meth public void end()
supr java.lang.Object

CLSS public abstract interface org.apache.lucene.util.AttributeReflector
 anno 0 java.lang.FunctionalInterface()
meth public abstract void reflect(java.lang.Class<? extends org.apache.lucene.util.Attribute>,java.lang.String,java.lang.Object)

CLSS public org.apache.lucene.util.AttributeSource
cons public init()
cons public init(org.apache.lucene.util.AttributeFactory)
cons public init(org.apache.lucene.util.AttributeSource)
innr public final static State
meth public boolean equals(java.lang.Object)
meth public final <%0 extends org.apache.lucene.util.Attribute> {%%0} addAttribute(java.lang.Class<{%%0}>)
meth public final <%0 extends org.apache.lucene.util.Attribute> {%%0} getAttribute(java.lang.Class<{%%0}>)
meth public final boolean hasAttribute(java.lang.Class<? extends org.apache.lucene.util.Attribute>)
meth public final boolean hasAttributes()
meth public final java.lang.String reflectAsString(boolean)
meth public final java.util.Iterator<java.lang.Class<? extends org.apache.lucene.util.Attribute>> getAttributeClassesIterator()
meth public final java.util.Iterator<org.apache.lucene.util.AttributeImpl> getAttributeImplsIterator()
meth public final org.apache.lucene.util.AttributeFactory getAttributeFactory()
meth public final org.apache.lucene.util.AttributeSource cloneAttributes()
meth public final org.apache.lucene.util.AttributeSource$State captureState()
meth public final void addAttributeImpl(org.apache.lucene.util.AttributeImpl)
meth public final void clearAttributes()
meth public final void copyTo(org.apache.lucene.util.AttributeSource)
meth public final void endAttributes()
meth public final void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public final void removeAllAttributes()
meth public final void restoreState(org.apache.lucene.util.AttributeSource$State)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds attributeImpls,attributes,currentState,factory,implInterfaces

CLSS public final static org.apache.lucene.util.AttributeSource$State
 outer org.apache.lucene.util.AttributeSource
cons public init()
intf java.lang.Cloneable
meth public org.apache.lucene.util.AttributeSource$State clone()
supr java.lang.Object
hfds attribute,next

CLSS public org.apache.lucene.util.BitDocIdSet
cons public init(org.apache.lucene.util.BitSet)
cons public init(org.apache.lucene.util.BitSet,long)
meth public java.lang.String toString()
meth public long ramBytesUsed()
meth public org.apache.lucene.search.DocIdSetIterator iterator()
meth public org.apache.lucene.util.BitSet bits()
supr org.apache.lucene.search.DocIdSet
hfds BASE_RAM_BYTES_USED,cost,set

CLSS public abstract org.apache.lucene.util.BitSet
cons public init()
intf org.apache.lucene.util.Accountable
intf org.apache.lucene.util.Bits
meth protected final void checkUnpositioned(org.apache.lucene.search.DocIdSetIterator)
meth public abstract boolean getAndSet(int)
meth public abstract int approximateCardinality()
meth public abstract int cardinality()
meth public abstract int nextSetBit(int)
meth public abstract int prevSetBit(int)
meth public abstract void clear(int)
meth public abstract void clear(int,int)
meth public abstract void set(int)
meth public static org.apache.lucene.util.BitSet of(org.apache.lucene.search.DocIdSetIterator,int) throws java.io.IOException
meth public void clear()
meth public void or(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.lucene.util.BitSetIterator
cons public init(org.apache.lucene.util.BitSet,long)
meth public int advance(int)
meth public int docID()
meth public int nextDoc()
meth public long cost()
meth public org.apache.lucene.util.BitSet getBitSet()
meth public static org.apache.lucene.util.FixedBitSet getFixedBitSetOrNull(org.apache.lucene.search.DocIdSetIterator)
meth public static org.apache.lucene.util.SparseFixedBitSet getSparseFixedBitSetOrNull(org.apache.lucene.search.DocIdSetIterator)
meth public void setDocId(int)
supr org.apache.lucene.search.DocIdSetIterator
hfds bits,cost,doc,length

CLSS public final org.apache.lucene.util.BitUtil
fld public final static java.lang.invoke.VarHandle VH_BE_DOUBLE
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static java.lang.invoke.VarHandle VH_BE_FLOAT
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static java.lang.invoke.VarHandle VH_BE_INT
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static java.lang.invoke.VarHandle VH_BE_LONG
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static java.lang.invoke.VarHandle VH_BE_SHORT
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static java.lang.invoke.VarHandle VH_LE_DOUBLE
fld public final static java.lang.invoke.VarHandle VH_LE_FLOAT
fld public final static java.lang.invoke.VarHandle VH_LE_INT
fld public final static java.lang.invoke.VarHandle VH_LE_LONG
fld public final static java.lang.invoke.VarHandle VH_LE_SHORT
meth public static int nextHighestPowerOfTwo(int)
meth public static int zigZagDecode(int)
meth public static int zigZagEncode(int)
meth public static long deinterleave(long)
meth public static long flipFlop(long)
meth public static long interleave(int,int)
meth public static long nextHighestPowerOfTwo(long)
meth public static long zigZagDecode(long)
meth public static long zigZagEncode(long)
supr java.lang.Object
hfds MAGIC0,MAGIC1,MAGIC2,MAGIC3,MAGIC4,MAGIC5,MAGIC6,SHIFT0,SHIFT1,SHIFT2,SHIFT3,SHIFT4

CLSS public abstract interface org.apache.lucene.util.Bits
fld public final static org.apache.lucene.util.Bits[] EMPTY_ARRAY
innr public static MatchAllBits
innr public static MatchNoBits
meth public abstract boolean get(int)
meth public abstract int length()

CLSS public static org.apache.lucene.util.Bits$MatchAllBits
 outer org.apache.lucene.util.Bits
cons public init(int)
intf org.apache.lucene.util.Bits
meth public boolean get(int)
meth public int length()
supr java.lang.Object
hfds len

CLSS public static org.apache.lucene.util.Bits$MatchNoBits
 outer org.apache.lucene.util.Bits
cons public init(int)
intf org.apache.lucene.util.Bits
meth public boolean get(int)
meth public int length()
supr java.lang.Object
hfds len

CLSS public final org.apache.lucene.util.ByteBlockPool
cons public init(org.apache.lucene.util.ByteBlockPool$Allocator)
fld public byte[] buffer
fld public final static int BYTE_BLOCK_MASK = 32767
fld public final static int BYTE_BLOCK_SHIFT = 15
fld public final static int BYTE_BLOCK_SIZE = 32768
fld public int byteOffset
fld public int byteUpto
innr public abstract static Allocator
innr public final static DirectAllocator
innr public static DirectTrackingAllocator
intf org.apache.lucene.util.Accountable
meth public byte readByte(long)
meth public byte[] getBuffer(int)
meth public long getPosition()
meth public long ramBytesUsed()
meth public void append(byte[])
meth public void append(byte[],int,int)
meth public void append(org.apache.lucene.util.ByteBlockPool,long,int)
meth public void append(org.apache.lucene.util.BytesRef)
meth public void nextBuffer()
meth public void readBytes(long,byte[],int,int)
meth public void reset(boolean,boolean)
supr java.lang.Object
hfds BASE_RAM_BYTES,allocator,bufferUpto,buffers

CLSS public abstract static org.apache.lucene.util.ByteBlockPool$Allocator
 outer org.apache.lucene.util.ByteBlockPool
cons protected init(int)
fld protected final int blockSize
meth public abstract void recycleByteBlocks(byte[][],int,int)
meth public byte[] getByteBlock()
supr java.lang.Object

CLSS public final static org.apache.lucene.util.ByteBlockPool$DirectAllocator
 outer org.apache.lucene.util.ByteBlockPool
cons public init()
meth public void recycleByteBlocks(byte[][],int,int)
supr org.apache.lucene.util.ByteBlockPool$Allocator

CLSS public static org.apache.lucene.util.ByteBlockPool$DirectTrackingAllocator
 outer org.apache.lucene.util.ByteBlockPool
cons public init(org.apache.lucene.util.Counter)
meth public byte[] getByteBlock()
meth public void recycleByteBlocks(byte[][],int,int)
supr org.apache.lucene.util.ByteBlockPool$Allocator
hfds bytesUsed

CLSS public final org.apache.lucene.util.BytesRef
cons public init()
cons public init(byte[])
cons public init(byte[],int,int)
cons public init(int)
cons public init(java.lang.CharSequence)
fld public byte[] bytes
fld public final static byte[] EMPTY_BYTES
fld public int length
fld public int offset
intf java.lang.Cloneable
intf java.lang.Comparable<org.apache.lucene.util.BytesRef>
meth public boolean bytesEquals(org.apache.lucene.util.BytesRef)
meth public boolean equals(java.lang.Object)
meth public boolean isValid()
meth public int compareTo(org.apache.lucene.util.BytesRef)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.lang.String utf8ToString()
meth public org.apache.lucene.util.BytesRef clone()
meth public static org.apache.lucene.util.BytesRef deepCopyOf(org.apache.lucene.util.BytesRef)
supr java.lang.Object

CLSS public final org.apache.lucene.util.BytesRefArray
cons public init(org.apache.lucene.util.Counter)
innr public abstract interface static IndexedBytesRefIterator
innr public final static SortState
meth public int append(org.apache.lucene.util.BytesRef)
meth public int size()
meth public org.apache.lucene.util.BytesRef get(org.apache.lucene.util.BytesRefBuilder,int)
meth public org.apache.lucene.util.BytesRefArray$IndexedBytesRefIterator iterator(org.apache.lucene.util.BytesRefArray$SortState)
meth public org.apache.lucene.util.BytesRefArray$SortState sort(java.util.Comparator<org.apache.lucene.util.BytesRef>,boolean)
meth public org.apache.lucene.util.BytesRefIterator iterator()
meth public org.apache.lucene.util.BytesRefIterator iterator(java.util.Comparator<org.apache.lucene.util.BytesRef>)
meth public void clear()
supr java.lang.Object
hfds bytesUsed,currentOffset,lastElement,offsets,pool

CLSS public abstract interface static org.apache.lucene.util.BytesRefArray$IndexedBytesRefIterator
 outer org.apache.lucene.util.BytesRefArray
intf org.apache.lucene.util.BytesRefIterator
meth public abstract int ord()

CLSS public final static org.apache.lucene.util.BytesRefArray$SortState
 outer org.apache.lucene.util.BytesRefArray
intf org.apache.lucene.util.Accountable
meth public long ramBytesUsed()
supr java.lang.Object
hfds indices

CLSS public org.apache.lucene.util.BytesRefBlockPool
cons public init()
cons public init(org.apache.lucene.util.ByteBlockPool)
intf org.apache.lucene.util.Accountable
meth public int addBytesRef(org.apache.lucene.util.BytesRef)
meth public long ramBytesUsed()
meth public void fillBytesRef(org.apache.lucene.util.BytesRef,int)
supr java.lang.Object
hfds BASE_RAM_BYTES,byteBlockPool

CLSS public org.apache.lucene.util.BytesRefBuilder
cons public init()
meth public boolean equals(java.lang.Object)
meth public byte byteAt(int)
meth public byte[] bytes()
meth public int hashCode()
meth public int length()
meth public org.apache.lucene.util.BytesRef get()
meth public org.apache.lucene.util.BytesRef toBytesRef()
meth public void append(byte)
meth public void append(byte[],int,int)
meth public void append(org.apache.lucene.util.BytesRef)
meth public void append(org.apache.lucene.util.BytesRefBuilder)
meth public void clear()
meth public void copyBytes(byte[],int,int)
meth public void copyBytes(org.apache.lucene.util.BytesRef)
meth public void copyBytes(org.apache.lucene.util.BytesRefBuilder)
meth public void copyChars(char[],int,int)
meth public void copyChars(java.lang.CharSequence)
meth public void copyChars(java.lang.CharSequence,int,int)
meth public void grow(int)
meth public void setByteAt(int,byte)
meth public void setLength(int)
supr java.lang.Object
hfds ref

CLSS public abstract org.apache.lucene.util.BytesRefComparator
cons protected init(int)
fld public final static org.apache.lucene.util.BytesRefComparator NATURAL
intf java.util.Comparator<org.apache.lucene.util.BytesRef>
meth protected abstract int byteAt(org.apache.lucene.util.BytesRef,int)
meth public final int compare(org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef)
meth public int compare(org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef,int)
supr java.lang.Object
hfds comparedBytesCount

CLSS public final org.apache.lucene.util.BytesRefHash
cons public init()
cons public init(org.apache.lucene.util.ByteBlockPool)
cons public init(org.apache.lucene.util.ByteBlockPool,int,org.apache.lucene.util.BytesRefHash$BytesStartArray)
fld public final static int DEFAULT_CAPACITY = 16
innr public abstract static BytesStartArray
innr public static DirectBytesStartArray
innr public static MaxBytesLengthExceededException
intf org.apache.lucene.util.Accountable
meth public int add(org.apache.lucene.util.BytesRef)
meth public int addByPoolOffset(int)
meth public int byteStart(int)
meth public int find(org.apache.lucene.util.BytesRef)
meth public int size()
meth public int[] compact()
meth public int[] sort()
meth public long ramBytesUsed()
meth public org.apache.lucene.util.BytesRef get(int,org.apache.lucene.util.BytesRef)
meth public void clear()
meth public void clear(boolean)
meth public void close()
meth public void reinit()
supr java.lang.Object
hfds BASE_RAM_BYTES,bytesStart,bytesStartArray,bytesUsed,count,hashHalfSize,hashMask,hashSize,ids,lastCount,pool

CLSS public abstract static org.apache.lucene.util.BytesRefHash$BytesStartArray
 outer org.apache.lucene.util.BytesRefHash
cons public init()
meth public abstract int[] clear()
meth public abstract int[] grow()
meth public abstract int[] init()
meth public abstract org.apache.lucene.util.Counter bytesUsed()
supr java.lang.Object

CLSS public static org.apache.lucene.util.BytesRefHash$DirectBytesStartArray
 outer org.apache.lucene.util.BytesRefHash
cons public init(int)
cons public init(int,org.apache.lucene.util.Counter)
fld protected final int initSize
meth public int[] clear()
meth public int[] grow()
meth public int[] init()
meth public org.apache.lucene.util.Counter bytesUsed()
supr org.apache.lucene.util.BytesRefHash$BytesStartArray
hfds bytesStart,bytesUsed

CLSS public static org.apache.lucene.util.BytesRefHash$MaxBytesLengthExceededException
 outer org.apache.lucene.util.BytesRefHash
supr java.lang.RuntimeException

CLSS public abstract interface org.apache.lucene.util.BytesRefIterator
fld public final static org.apache.lucene.util.BytesRefIterator EMPTY
meth public abstract org.apache.lucene.util.BytesRef next() throws java.io.IOException

CLSS public final org.apache.lucene.util.CharsRef
cons public init()
cons public init(char[],int,int)
cons public init(int)
cons public init(java.lang.String)
fld public char[] chars
fld public final static char[] EMPTY_CHARS
fld public int length
fld public int offset
intf java.lang.CharSequence
intf java.lang.Cloneable
intf java.lang.Comparable<org.apache.lucene.util.CharsRef>
meth public boolean charsEquals(org.apache.lucene.util.CharsRef)
meth public boolean equals(java.lang.Object)
meth public boolean isValid()
meth public char charAt(int)
meth public int compareTo(org.apache.lucene.util.CharsRef)
meth public int hashCode()
meth public int length()
meth public java.lang.CharSequence subSequence(int,int)
meth public java.lang.String toString()
meth public org.apache.lucene.util.CharsRef clone()
meth public static int stringHashCode(char[],int,int)
meth public static java.util.Comparator<org.apache.lucene.util.CharsRef> getUTF16SortedAsUTF8Comparator()
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public static org.apache.lucene.util.CharsRef deepCopyOf(org.apache.lucene.util.CharsRef)
supr java.lang.Object
hfds utf16SortedAsUTF8SortOrder
hcls UTF16SortedAsUTF8Comparator

CLSS public org.apache.lucene.util.CharsRefBuilder
cons public init()
intf java.lang.Appendable
meth public boolean equals(java.lang.Object)
meth public char charAt(int)
meth public char[] chars()
meth public int hashCode()
meth public int length()
meth public java.lang.String toString()
meth public org.apache.lucene.util.CharsRef get()
meth public org.apache.lucene.util.CharsRef toCharsRef()
meth public org.apache.lucene.util.CharsRefBuilder append(char)
meth public org.apache.lucene.util.CharsRefBuilder append(java.lang.CharSequence)
meth public org.apache.lucene.util.CharsRefBuilder append(java.lang.CharSequence,int,int)
meth public void append(char[],int,int)
meth public void clear()
meth public void copyChars(char[],int,int)
meth public void copyChars(org.apache.lucene.util.CharsRef)
meth public void copyUTF8Bytes(byte[],int,int)
meth public void copyUTF8Bytes(org.apache.lucene.util.BytesRef)
meth public void grow(int)
meth public void setCharAt(int,char)
meth public void setLength(int)
supr java.lang.Object
hfds NULL_STRING,ref

CLSS public abstract interface org.apache.lucene.util.ClassLoaderUtils
meth public static boolean isParentClassLoader(java.lang.ClassLoader,java.lang.ClassLoader)

CLSS public final org.apache.lucene.util.ClasspathResourceLoader
cons public init(java.lang.Class<?>)
cons public init(java.lang.ClassLoader)
intf org.apache.lucene.util.ResourceLoader
meth public <%0 extends java.lang.Object> java.lang.Class<? extends {%%0}> findClass(java.lang.String,java.lang.Class<{%%0}>)
meth public java.io.InputStream openResource(java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds clazz,loader

CLSS public org.apache.lucene.util.CloseableThreadLocal<%0 extends java.lang.Object>
cons public init()
intf java.io.Closeable
meth protected {org.apache.lucene.util.CloseableThreadLocal%0} initialValue()
meth public void close()
meth public void set({org.apache.lucene.util.CloseableThreadLocal%0})
meth public {org.apache.lucene.util.CloseableThreadLocal%0} get()
supr java.lang.Object
hfds PURGE_MULTIPLIER,countUntilPurge,hardRefs,t

CLSS public final org.apache.lucene.util.CollectionUtil
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> void introSort(java.util.List<{%%0}>)
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> void timSort(java.util.List<{%%0}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.HashMap<{%%0},{%%1}> newHashMap(int)
meth public static <%0 extends java.lang.Object> java.util.HashSet<{%%0}> newHashSet(int)
meth public static <%0 extends java.lang.Object> void introSort(java.util.List<{%%0}>,java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> void timSort(java.util.List<{%%0}>,java.util.Comparator<? super {%%0}>)
supr java.lang.Object
hcls ListIntroSorter,ListTimSorter

CLSS public final org.apache.lucene.util.CommandLineUtil
meth public static java.lang.Class<? extends org.apache.lucene.store.Directory> loadDirectoryClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public static java.lang.Class<? extends org.apache.lucene.store.FSDirectory> loadFSDirectoryClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public static org.apache.lucene.store.FSDirectory newFSDirectory(java.lang.Class<? extends org.apache.lucene.store.FSDirectory>,java.nio.file.Path) throws java.lang.ReflectiveOperationException
meth public static org.apache.lucene.store.FSDirectory newFSDirectory(java.lang.Class<? extends org.apache.lucene.store.FSDirectory>,java.nio.file.Path,org.apache.lucene.store.LockFactory) throws java.lang.ReflectiveOperationException
meth public static org.apache.lucene.store.FSDirectory newFSDirectory(java.lang.String,java.nio.file.Path)
meth public static org.apache.lucene.store.FSDirectory newFSDirectory(java.lang.String,java.nio.file.Path,org.apache.lucene.store.LockFactory)
supr java.lang.Object

CLSS public final org.apache.lucene.util.Constants
fld public final static boolean FREE_BSD
fld public final static boolean HAS_FAST_SCALAR_FMA
fld public final static boolean HAS_FAST_VECTOR_FMA
fld public final static boolean IS_CLIENT_VM
fld public final static boolean IS_HOTSPOT_VM
fld public final static boolean IS_JVMCI_VM
fld public final static boolean JRE_IS_64BIT
fld public final static boolean JRE_IS_MINIMUM_JAVA11 = true
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static boolean JRE_IS_MINIMUM_JAVA8 = true
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static boolean JRE_IS_MINIMUM_JAVA9 = true
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static boolean LINUX
fld public final static boolean MAC_OS_X
fld public final static boolean SUN_OS
fld public final static boolean WINDOWS
fld public final static java.lang.String JAVA_VENDOR
fld public final static java.lang.String JAVA_VERSION
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static java.lang.String JVM_NAME
fld public final static java.lang.String JVM_SPEC_VERSION
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static java.lang.String JVM_VENDOR
fld public final static java.lang.String JVM_VERSION
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static java.lang.String OS_ARCH
fld public final static java.lang.String OS_NAME
fld public final static java.lang.String OS_VERSION
supr java.lang.Object
hfds HAS_FMA,HAS_SSE4A,MAX_VECTOR_SIZE,UNKNOWN

CLSS public abstract org.apache.lucene.util.Counter
cons public init()
meth public abstract long addAndGet(long)
meth public abstract long get()
meth public static org.apache.lucene.util.Counter newCounter()
meth public static org.apache.lucene.util.Counter newCounter(boolean)
supr java.lang.Object
hcls AtomicCounter,SerialCounter

CLSS public org.apache.lucene.util.DocBaseBitSetIterator
cons public init(org.apache.lucene.util.FixedBitSet,long,int)
meth public int advance(int)
meth public int docID()
meth public int getDocBase()
meth public int nextDoc()
meth public long cost()
meth public org.apache.lucene.util.FixedBitSet getBitSet()
supr org.apache.lucene.search.DocIdSetIterator
hfds bits,cost,doc,docBase,length

CLSS public final org.apache.lucene.util.DocIdSetBuilder
cons public init(int)
cons public init(int,org.apache.lucene.index.PointValues,java.lang.String) throws java.io.IOException
cons public init(int,org.apache.lucene.index.Terms) throws java.io.IOException
innr public abstract static BulkAdder
meth public org.apache.lucene.search.DocIdSet build()
meth public org.apache.lucene.util.DocIdSetBuilder$BulkAdder grow(int)
meth public void add(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
supr java.lang.Object
hfds adder,bitSet,buffers,counter,maxDoc,multivalued,numValuesPerDoc,threshold,totalAllocated
hcls Buffer,BufferAdder,FixedBitSetAdder

CLSS public abstract static org.apache.lucene.util.DocIdSetBuilder$BulkAdder
 outer org.apache.lucene.util.DocIdSetBuilder
cons public init()
meth public abstract void add(int)
meth public void add(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
supr java.lang.Object

CLSS public final org.apache.lucene.util.FileDeleter
cons public init(org.apache.lucene.store.Directory,java.util.function.BiConsumer<org.apache.lucene.util.FileDeleter$MsgType,java.lang.String>)
innr public final static !enum MsgType
innr public final static RefCount
meth public boolean exists(java.lang.String)
meth public int getRefCount(java.lang.String)
meth public java.util.Set<java.lang.String> getAllFiles()
meth public java.util.Set<java.lang.String> getUnrefedFiles()
meth public void decRef(java.util.Collection<java.lang.String>) throws java.io.IOException
meth public void deleteFileIfNoRef(java.lang.String) throws java.io.IOException
meth public void deleteFilesIfNoRef(java.util.Collection<java.lang.String>) throws java.io.IOException
meth public void forceDelete(java.lang.String) throws java.io.IOException
meth public void incRef(java.lang.String)
meth public void incRef(java.util.Collection<java.lang.String>)
meth public void initRefCount(java.lang.String)
supr java.lang.Object
hfds ZERO_REF,directory,messenger,refCounts

CLSS public final static !enum org.apache.lucene.util.FileDeleter$MsgType
 outer org.apache.lucene.util.FileDeleter
fld public final static org.apache.lucene.util.FileDeleter$MsgType FILE
fld public final static org.apache.lucene.util.FileDeleter$MsgType REF
meth public static org.apache.lucene.util.FileDeleter$MsgType valueOf(java.lang.String)
meth public static org.apache.lucene.util.FileDeleter$MsgType[] values()
supr java.lang.Enum<org.apache.lucene.util.FileDeleter$MsgType>

CLSS public final static org.apache.lucene.util.FileDeleter$RefCount
 outer org.apache.lucene.util.FileDeleter
meth public int decRef()
meth public int incRef()
supr java.lang.Object
hfds count,fileName,initDone

CLSS public abstract org.apache.lucene.util.FilterIterator<%0 extends java.lang.Object, %1 extends {org.apache.lucene.util.FilterIterator%0}>
cons public init(java.util.Iterator<{org.apache.lucene.util.FilterIterator%1}>)
intf java.util.Iterator<{org.apache.lucene.util.FilterIterator%0}>
meth protected abstract boolean predicateFunction({org.apache.lucene.util.FilterIterator%1})
meth public final boolean hasNext()
meth public final void remove()
meth public final {org.apache.lucene.util.FilterIterator%0} next()
supr java.lang.Object
hfds iterator,next,nextIsSet

CLSS public final org.apache.lucene.util.FixedBitSet
cons public init(int)
cons public init(long[],int)
meth public boolean equals(java.lang.Object)
meth public boolean get(int)
meth public boolean getAndClear(int)
meth public boolean getAndSet(int)
meth public boolean intersects(org.apache.lucene.util.FixedBitSet)
meth public boolean scanIsEmpty()
meth public int approximateCardinality()
meth public int cardinality()
meth public int hashCode()
meth public int length()
meth public int nextSetBit(int)
meth public int prevSetBit(int)
meth public long ramBytesUsed()
meth public long[] getBits()
meth public org.apache.lucene.util.Bits asReadOnlyBits()
meth public org.apache.lucene.util.FixedBitSet clone()
meth public static int bits2words(int)
meth public static long andNotCount(org.apache.lucene.util.FixedBitSet,org.apache.lucene.util.FixedBitSet)
meth public static long intersectionCount(org.apache.lucene.util.FixedBitSet,org.apache.lucene.util.FixedBitSet)
meth public static long unionCount(org.apache.lucene.util.FixedBitSet,org.apache.lucene.util.FixedBitSet)
meth public static org.apache.lucene.util.FixedBitSet copyOf(org.apache.lucene.util.Bits)
meth public static org.apache.lucene.util.FixedBitSet ensureCapacity(org.apache.lucene.util.FixedBitSet,int)
meth public void and(org.apache.lucene.util.FixedBitSet)
meth public void andNot(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
meth public void andNot(org.apache.lucene.util.FixedBitSet)
meth public void clear()
meth public void clear(int)
meth public void clear(int,int)
meth public void flip(int)
meth public void flip(int,int)
meth public void or(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
meth public void or(org.apache.lucene.util.FixedBitSet)
meth public void set(int)
meth public void set(int,int)
meth public void xor(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
meth public void xor(org.apache.lucene.util.FixedBitSet)
supr org.apache.lucene.util.BitSet
hfds BASE_RAM_BYTES_USED,bits,numBits,numWords

CLSS public final org.apache.lucene.util.FrequencyTrackingRingBuffer
cons public init(int,int)
intf org.apache.lucene.util.Accountable
meth public int frequency(int)
meth public long ramBytesUsed()
meth public void add(int)
supr java.lang.Object
hfds BASE_RAM_BYTES_USED,buffer,frequencies,maxSize,position
hcls IntBag

CLSS public abstract interface org.apache.lucene.util.IOConsumer<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
intf org.apache.lucene.util.IOUtils$IOConsumer<{org.apache.lucene.util.IOConsumer%0}>
meth public abstract void accept({org.apache.lucene.util.IOConsumer%0}) throws java.io.IOException

CLSS public abstract interface org.apache.lucene.util.IOFunction<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
intf org.apache.lucene.util.IOUtils$IOFunction<{org.apache.lucene.util.IOFunction%0},{org.apache.lucene.util.IOFunction%1}>
meth public abstract {org.apache.lucene.util.IOFunction%1} apply({org.apache.lucene.util.IOFunction%0}) throws java.io.IOException

CLSS public abstract interface org.apache.lucene.util.IOSupplier<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract {org.apache.lucene.util.IOSupplier%0} get() throws java.io.IOException

CLSS public final org.apache.lucene.util.IOUtils
fld public final static java.lang.String UTF_8
innr public abstract interface static IOConsumer
innr public abstract interface static IOFunction
meth public !varargs static void close(java.io.Closeable[]) throws java.io.IOException
meth public !varargs static void closeWhileHandlingException(java.io.Closeable[])
meth public !varargs static void deleteFilesIfExist(java.nio.file.Path[]) throws java.io.IOException
meth public !varargs static void deleteFilesIgnoringExceptions(java.nio.file.Path[])
meth public !varargs static void deleteFilesIgnoringExceptions(org.apache.lucene.store.Directory,java.lang.String[])
meth public !varargs static void rm(java.nio.file.Path[]) throws java.io.IOException
meth public static <%0 extends java.lang.Object> void applyToAll(java.util.Collection<{%%0}>,org.apache.lucene.util.IOUtils$IOConsumer<{%%0}>) throws java.io.IOException
meth public static <%0 extends java.lang.Object> {%%0} requireResourceNonNull({%%0},java.lang.String) throws java.io.IOException
meth public static <%0 extends java.lang.Throwable> {%%0} useOrSuppress({%%0},{%%0})
meth public static java.io.Reader getDecodingReader(java.io.InputStream,java.nio.charset.Charset)
meth public static java.io.Reader getDecodingReader(java.lang.Class<?>,java.lang.String,java.nio.charset.Charset) throws java.io.IOException
 anno 0 java.lang.Deprecated(boolean forRemoval=true, java.lang.String since="9.1")
meth public static java.lang.Error rethrowAlways(java.lang.Throwable) throws java.io.IOException
meth public static void close(java.lang.Iterable<? extends java.io.Closeable>) throws java.io.IOException
meth public static void closeWhileHandlingException(java.lang.Iterable<? extends java.io.Closeable>)
meth public static void deleteFiles(org.apache.lucene.store.Directory,java.util.Collection<java.lang.String>) throws java.io.IOException
meth public static void deleteFilesIfExist(java.util.Collection<? extends java.nio.file.Path>) throws java.io.IOException
meth public static void deleteFilesIgnoringExceptions(java.util.Collection<? extends java.nio.file.Path>)
meth public static void deleteFilesIgnoringExceptions(org.apache.lucene.store.Directory,java.util.Collection<java.lang.String>)
meth public static void fsync(java.nio.file.Path,boolean) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface static org.apache.lucene.util.IOUtils$IOConsumer<%0 extends java.lang.Object>
 outer org.apache.lucene.util.IOUtils
 anno 0 java.lang.Deprecated(boolean forRemoval=true, java.lang.String since="9.1")
 anno 0 java.lang.FunctionalInterface()
meth public abstract void accept({org.apache.lucene.util.IOUtils$IOConsumer%0}) throws java.io.IOException

CLSS public abstract interface static org.apache.lucene.util.IOUtils$IOFunction<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.apache.lucene.util.IOUtils
 anno 0 java.lang.Deprecated(boolean forRemoval=true, java.lang.String since="9.1")
 anno 0 java.lang.FunctionalInterface()
meth public abstract {org.apache.lucene.util.IOUtils$IOFunction%1} apply({org.apache.lucene.util.IOUtils$IOFunction%0}) throws java.io.IOException

CLSS public abstract interface !annotation org.apache.lucene.util.IgnoreRandomChains
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String reason()

CLSS public abstract org.apache.lucene.util.InPlaceMergeSorter
cons public init()
meth public final void sort(int,int)
supr org.apache.lucene.util.Sorter

CLSS public abstract org.apache.lucene.util.InfoStream
cons public init()
fld public final static org.apache.lucene.util.InfoStream NO_OUTPUT
intf java.io.Closeable
meth public abstract boolean isEnabled(java.lang.String)
meth public abstract void message(java.lang.String,java.lang.String)
meth public static org.apache.lucene.util.InfoStream getDefault()
meth public static void setDefault(org.apache.lucene.util.InfoStream)
supr java.lang.Object
hfds defaultInfoStream
hcls NoOutput

CLSS public org.apache.lucene.util.IntBlockPool
cons public init()
cons public init(org.apache.lucene.util.IntBlockPool$Allocator)
fld public final static int INT_BLOCK_MASK = 8191
fld public final static int INT_BLOCK_SHIFT = 13
fld public final static int INT_BLOCK_SIZE = 8192
fld public int intOffset
fld public int intUpto
fld public int[] buffer
fld public int[][] buffers
innr public abstract static Allocator
innr public final static DirectAllocator
meth public void nextBuffer()
meth public void reset(boolean,boolean)
supr java.lang.Object
hfds allocator,bufferUpto

CLSS public abstract static org.apache.lucene.util.IntBlockPool$Allocator
 outer org.apache.lucene.util.IntBlockPool
cons protected init(int)
fld protected final int blockSize
meth public abstract void recycleIntBlocks(int[][],int,int)
meth public int[] getIntBlock()
supr java.lang.Object

CLSS public final static org.apache.lucene.util.IntBlockPool$DirectAllocator
 outer org.apache.lucene.util.IntBlockPool
cons public init()
meth public void recycleIntBlocks(int[][],int,int)
supr org.apache.lucene.util.IntBlockPool$Allocator

CLSS public abstract org.apache.lucene.util.IntroSelector
cons public init()
meth protected abstract int comparePivot(int)
meth protected abstract void setPivot(int)
meth protected int compare(int,int)
meth public final void select(int,int,int)
supr org.apache.lucene.util.Selector
hfds random

CLSS public abstract org.apache.lucene.util.IntroSorter
cons public init()
meth protected abstract int comparePivot(int)
meth protected abstract void setPivot(int)
meth protected int compare(int,int)
meth public final void sort(int,int)
supr org.apache.lucene.util.Sorter
hfds SINGLE_MEDIAN_THRESHOLD

CLSS public final org.apache.lucene.util.IntsRef
cons public init()
cons public init(int)
cons public init(int[],int,int)
fld public final static int[] EMPTY_INTS
fld public int length
fld public int offset
fld public int[] ints
intf java.lang.Cloneable
intf java.lang.Comparable<org.apache.lucene.util.IntsRef>
meth public boolean equals(java.lang.Object)
meth public boolean intsEquals(org.apache.lucene.util.IntsRef)
meth public boolean isValid()
meth public int compareTo(org.apache.lucene.util.IntsRef)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.apache.lucene.util.IntsRef clone()
meth public static org.apache.lucene.util.IntsRef deepCopyOf(org.apache.lucene.util.IntsRef)
supr java.lang.Object

CLSS public org.apache.lucene.util.IntsRefBuilder
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public int intAt(int)
meth public int length()
meth public int[] ints()
meth public org.apache.lucene.util.IntsRef get()
meth public org.apache.lucene.util.IntsRef toIntsRef()
meth public void append(int)
meth public void clear()
meth public void copyInts(int[],int,int)
meth public void copyInts(org.apache.lucene.util.IntsRef)
meth public void copyUTF8Bytes(org.apache.lucene.util.BytesRef)
meth public void grow(int)
meth public void setIntAt(int,int)
meth public void setLength(int)
supr java.lang.Object
hfds ref

CLSS public final org.apache.lucene.util.JavaLoggingInfoStream
cons public init(java.lang.String,java.util.logging.Level)
cons public init(java.util.function.Function<java.lang.String,java.lang.String>,java.util.logging.Level)
cons public init(java.util.logging.Level)
meth public boolean isEnabled(java.lang.String)
meth public void close()
meth public void message(java.lang.String,java.lang.String)
supr org.apache.lucene.util.InfoStream
hfds cache,componentToLoggerName,level

CLSS public final org.apache.lucene.util.LSBRadixSorter
cons public init()
meth public void sort(int,int[],int)
supr java.lang.Object
hfds HISTOGRAM_SIZE,INSERTION_SORT_THRESHOLD,buffer,histogram

CLSS public final org.apache.lucene.util.LongBitSet
cons public init(long)
cons public init(long[],long)
fld public final static long MAX_NUM_BITS
intf org.apache.lucene.util.Accountable
meth public boolean equals(java.lang.Object)
meth public boolean get(long)
meth public boolean getAndClear(long)
meth public boolean getAndSet(long)
meth public boolean intersects(org.apache.lucene.util.LongBitSet)
meth public boolean scanIsEmpty()
meth public int hashCode()
meth public long cardinality()
meth public long length()
meth public long nextSetBit(long)
meth public long prevSetBit(long)
meth public long ramBytesUsed()
meth public long[] getBits()
meth public org.apache.lucene.util.LongBitSet clone()
meth public static int bits2words(long)
meth public static org.apache.lucene.util.LongBitSet ensureCapacity(org.apache.lucene.util.LongBitSet,long)
meth public void and(org.apache.lucene.util.LongBitSet)
meth public void andNot(org.apache.lucene.util.LongBitSet)
meth public void clear(long)
meth public void clear(long,long)
meth public void flip(long)
meth public void flip(long,long)
meth public void or(org.apache.lucene.util.LongBitSet)
meth public void set(long)
meth public void set(long,long)
meth public void xor(org.apache.lucene.util.LongBitSet)
supr java.lang.Object
hfds BASE_RAM_BYTES,bits,numBits,numWords

CLSS public final org.apache.lucene.util.LongHeap
cons public init(int)
meth public boolean insertWithOverflow(long)
meth public final int size()
meth public final long pop()
meth public final long push(long)
meth public final long top()
meth public final long updateTop(long)
meth public final void clear()
meth public long get(int)
meth public void pushAll(org.apache.lucene.util.LongHeap)
supr java.lang.Object
hfds heap,maxSize,size

CLSS public abstract org.apache.lucene.util.LongValues
cons public init()
fld public final static org.apache.lucene.util.LongValues IDENTITY
fld public final static org.apache.lucene.util.LongValues ZEROES
meth public abstract long get(long)
supr java.lang.Object

CLSS public final org.apache.lucene.util.LongsRef
cons public init()
cons public init(int)
cons public init(long[],int,int)
fld public final static long[] EMPTY_LONGS
fld public int length
fld public int offset
fld public long[] longs
intf java.lang.Cloneable
intf java.lang.Comparable<org.apache.lucene.util.LongsRef>
meth public boolean equals(java.lang.Object)
meth public boolean isValid()
meth public boolean longsEquals(org.apache.lucene.util.LongsRef)
meth public int compareTo(org.apache.lucene.util.LongsRef)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.apache.lucene.util.LongsRef clone()
meth public static org.apache.lucene.util.LongsRef deepCopyOf(org.apache.lucene.util.LongsRef)
supr java.lang.Object

CLSS public abstract org.apache.lucene.util.MSBRadixSorter
cons protected init(int)
fld protected final int maxLength
fld protected final static int HISTOGRAM_SIZE = 257
fld protected final static int LENGTH_THRESHOLD = 100
fld protected final static int LEVEL_THRESHOLD = 8
meth protected abstract int byteAt(int,int)
meth protected boolean shouldFallback(int,int,int)
meth protected final int compare(int,int)
meth protected int getBucket(int,int)
meth protected org.apache.lucene.util.Sorter getFallbackSorter(int)
meth protected void buildHistogram(int,int,int,int,int,int[])
meth protected void reorder(int,int,int[],int[],int)
meth protected void sort(int,int,int,int)
meth public void sort(int,int)
supr org.apache.lucene.util.Sorter
hfds commonPrefix,endOffsets,histograms

CLSS public org.apache.lucene.util.MapOfSets<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(java.util.Map<{org.apache.lucene.util.MapOfSets%0},java.util.Set<{org.apache.lucene.util.MapOfSets%1}>>)
meth public int put({org.apache.lucene.util.MapOfSets%0},{org.apache.lucene.util.MapOfSets%1})
meth public int putAll({org.apache.lucene.util.MapOfSets%0},java.util.Collection<? extends {org.apache.lucene.util.MapOfSets%1}>)
meth public java.util.Map<{org.apache.lucene.util.MapOfSets%0},java.util.Set<{org.apache.lucene.util.MapOfSets%1}>> getMap()
supr java.lang.Object
hfds theMap

CLSS public final org.apache.lucene.util.MathUtil
meth public static double acosh(double)
meth public static double asinh(double)
meth public static double atanh(double)
meth public static double log(double,double)
meth public static double sumRelativeErrorBound(int)
meth public static double sumUpperBound(double,int)
meth public static int log(long,int)
meth public static long gcd(long,long)
supr java.lang.Object

CLSS public final org.apache.lucene.util.MergedIterator<%0 extends java.lang.Comparable<{org.apache.lucene.util.MergedIterator%0}>>
cons public !varargs init(boolean,java.util.Iterator<{org.apache.lucene.util.MergedIterator%0}>[])
cons public !varargs init(java.util.Iterator<{org.apache.lucene.util.MergedIterator%0}>[])
intf java.util.Iterator<{org.apache.lucene.util.MergedIterator%0}>
meth public boolean hasNext()
meth public void remove()
meth public {org.apache.lucene.util.MergedIterator%0} next()
supr java.lang.Object
hfds current,numTop,queue,removeDuplicates,top
hcls SubIterator,TermMergeQueue

CLSS public final org.apache.lucene.util.ModuleResourceLoader
cons public init(java.lang.Module)
intf org.apache.lucene.util.ResourceLoader
meth public <%0 extends java.lang.Object> java.lang.Class<? extends {%%0}> findClass(java.lang.String,java.lang.Class<{%%0}>)
meth public java.io.InputStream openResource(java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds module

CLSS public final org.apache.lucene.util.NamedSPILoader<%0 extends org.apache.lucene.util.NamedSPILoader$NamedSPI>
cons public init(java.lang.Class<{org.apache.lucene.util.NamedSPILoader%0}>)
cons public init(java.lang.Class<{org.apache.lucene.util.NamedSPILoader%0}>,java.lang.ClassLoader)
innr public abstract interface static NamedSPI
intf java.lang.Iterable<{org.apache.lucene.util.NamedSPILoader%0}>
meth public java.util.Iterator<{org.apache.lucene.util.NamedSPILoader%0}> iterator()
meth public java.util.Set<java.lang.String> availableServices()
meth public static void checkServiceName(java.lang.String)
meth public void reload(java.lang.ClassLoader)
meth public {org.apache.lucene.util.NamedSPILoader%0} lookup(java.lang.String)
supr java.lang.Object
hfds clazz,services

CLSS public abstract interface static org.apache.lucene.util.NamedSPILoader$NamedSPI
 outer org.apache.lucene.util.NamedSPILoader
meth public abstract java.lang.String getName()

CLSS public org.apache.lucene.util.NamedThreadFactory
cons public init(java.lang.String)
intf java.util.concurrent.ThreadFactory
meth public java.lang.Thread newThread(java.lang.Runnable)
supr java.lang.Object
hfds NAME_PATTERN,group,threadNamePrefix,threadNumber,threadPoolNumber

CLSS public final org.apache.lucene.util.NotDocIdSet
cons public init(int,org.apache.lucene.search.DocIdSet)
meth public long ramBytesUsed()
meth public org.apache.lucene.search.DocIdSetIterator iterator() throws java.io.IOException
meth public org.apache.lucene.util.Bits bits() throws java.io.IOException
supr org.apache.lucene.search.DocIdSet
hfds BASE_RAM_BYTES_USED,in,maxDoc

CLSS public final org.apache.lucene.util.NumericUtils
meth public static boolean nextDown(byte[])
meth public static boolean nextUp(byte[])
meth public static double sortableLongToDouble(long)
meth public static float sortableIntToFloat(int)
meth public static int floatToSortableInt(float)
meth public static int sortableBytesToInt(byte[],int)
meth public static int sortableFloatBits(int)
meth public static java.math.BigInteger sortableBytesToBigInt(byte[],int,int)
meth public static long doubleToSortableLong(double)
meth public static long sortableBytesToLong(byte[],int)
meth public static long sortableDoubleBits(long)
meth public static void add(int,int,byte[],byte[],byte[])
meth public static void bigIntToSortableBytes(java.math.BigInteger,int,byte[],int)
meth public static void intToSortableBytes(int,byte[],int)
meth public static void longToSortableBytes(long,byte[],int)
meth public static void subtract(int,int,byte[],byte[],byte[])
supr java.lang.Object

CLSS public org.apache.lucene.util.OfflineSorter
cons public init(org.apache.lucene.store.Directory,java.lang.String) throws java.io.IOException
cons public init(org.apache.lucene.store.Directory,java.lang.String,java.util.Comparator<org.apache.lucene.util.BytesRef>) throws java.io.IOException
cons public init(org.apache.lucene.store.Directory,java.lang.String,java.util.Comparator<org.apache.lucene.util.BytesRef>,org.apache.lucene.util.OfflineSorter$BufferSize,int,int,java.util.concurrent.ExecutorService,int)
fld public final static int MAX_TEMPFILES = 10
fld public final static java.util.Comparator<org.apache.lucene.util.BytesRef> DEFAULT_COMPARATOR
fld public final static long ABSOLUTE_MIN_SORT_BUFFER_SIZE = 524288
fld public final static long GB = 1073741824
fld public final static long MB = 1048576
fld public final static long MIN_BUFFER_SIZE_MB = 32
innr public SortInfo
innr public final static BufferSize
innr public static ByteSequencesReader
innr public static ByteSequencesWriter
meth protected org.apache.lucene.util.OfflineSorter$ByteSequencesReader getReader(org.apache.lucene.store.ChecksumIndexInput,java.lang.String) throws java.io.IOException
meth protected org.apache.lucene.util.OfflineSorter$ByteSequencesWriter getWriter(org.apache.lucene.store.IndexOutput,long) throws java.io.IOException
meth public java.lang.String getTempFileNamePrefix()
meth public java.lang.String sort(java.lang.String) throws java.io.IOException
meth public java.util.Comparator<org.apache.lucene.util.BytesRef> getComparator()
meth public org.apache.lucene.store.Directory getDirectory()
supr java.lang.Object
hfds MIN_BUFFER_SIZE_MSG,comparator,dir,exec,maxTempFiles,partitionsInRAM,ramBufferSize,sortInfo,tempFileNamePrefix,valueLength
hcls FileAndTop,MergePartitionsTask,Partition,SortPartitionTask

CLSS public final static org.apache.lucene.util.OfflineSorter$BufferSize
 outer org.apache.lucene.util.OfflineSorter
meth public static org.apache.lucene.util.OfflineSorter$BufferSize automatic()
meth public static org.apache.lucene.util.OfflineSorter$BufferSize megabytes(long)
supr java.lang.Object
hfds bytes

CLSS public static org.apache.lucene.util.OfflineSorter$ByteSequencesReader
 outer org.apache.lucene.util.OfflineSorter
cons public init(org.apache.lucene.store.ChecksumIndexInput,java.lang.String)
fld protected final java.lang.String name
fld protected final long end
fld protected final org.apache.lucene.store.ChecksumIndexInput in
fld protected final org.apache.lucene.util.BytesRefBuilder ref
intf java.io.Closeable
intf org.apache.lucene.util.BytesRefIterator
meth public org.apache.lucene.util.BytesRef next() throws java.io.IOException
meth public void close() throws java.io.IOException
supr java.lang.Object

CLSS public static org.apache.lucene.util.OfflineSorter$ByteSequencesWriter
 outer org.apache.lucene.util.OfflineSorter
cons public init(org.apache.lucene.store.IndexOutput)
fld protected final org.apache.lucene.store.IndexOutput out
intf java.io.Closeable
meth public final void write(byte[]) throws java.io.IOException
meth public final void write(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.lucene.util.OfflineSorter$SortInfo
 outer org.apache.lucene.util.OfflineSorter
cons public init(org.apache.lucene.util.OfflineSorter)
fld public final java.util.concurrent.atomic.AtomicLong mergeTimeMS
fld public final java.util.concurrent.atomic.AtomicLong sortTimeMS
fld public final long bufferSize
fld public int mergeRounds
fld public int tempMergeFiles
fld public long lineCount
fld public long readTimeMS
fld public long totalTimeMS
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.apache.lucene.util.PagedBytes
cons public init(int)
innr public final PagedBytesDataInput
innr public final PagedBytesDataOutput
innr public final static Reader
intf org.apache.lucene.util.Accountable
meth public long copyUsingLengthPrefix(org.apache.lucene.util.BytesRef)
meth public long getPointer()
meth public long ramBytesUsed()
meth public org.apache.lucene.util.PagedBytes$PagedBytesDataInput getDataInput()
meth public org.apache.lucene.util.PagedBytes$PagedBytesDataOutput getDataOutput()
meth public org.apache.lucene.util.PagedBytes$Reader freeze(boolean)
meth public void copy(org.apache.lucene.store.IndexInput,long) throws java.io.IOException
meth public void copy(org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef)
supr java.lang.Object
hfds BASE_RAM_BYTES_USED,EMPTY_BYTES,blockBits,blockMask,blockSize,blocks,bytesUsedPerBlock,currentBlock,didSkipBytes,frozen,numBlocks,upto

CLSS public final org.apache.lucene.util.PagedBytes$PagedBytesDataInput
 outer org.apache.lucene.util.PagedBytes
meth public byte readByte()
meth public long getPosition()
meth public org.apache.lucene.util.PagedBytes$PagedBytesDataInput clone()
meth public void readBytes(byte[],int,int)
meth public void setPosition(long)
meth public void skipBytes(long)
supr org.apache.lucene.store.DataInput
hfds currentBlock,currentBlockIndex,currentBlockUpto

CLSS public final org.apache.lucene.util.PagedBytes$PagedBytesDataOutput
 outer org.apache.lucene.util.PagedBytes
cons public init(org.apache.lucene.util.PagedBytes)
meth public long getPosition()
meth public void writeByte(byte)
meth public void writeBytes(byte[],int,int)
supr org.apache.lucene.store.DataOutput

CLSS public final static org.apache.lucene.util.PagedBytes$Reader
 outer org.apache.lucene.util.PagedBytes
intf org.apache.lucene.util.Accountable
meth public byte getByte(long)
meth public java.lang.String toString()
meth public long ramBytesUsed()
meth public void fill(org.apache.lucene.util.BytesRef,long)
meth public void fillSlice(org.apache.lucene.util.BytesRef,long,int)
supr java.lang.Object
hfds BASE_RAM_BYTES_USED,blockBits,blockMask,blockSize,blocks,bytesUsedPerBlock

CLSS public org.apache.lucene.util.PrintStreamInfoStream
cons public init(java.io.PrintStream)
cons public init(java.io.PrintStream,int)
fld protected final int messageID
fld protected final java.io.PrintStream stream
meth protected java.lang.String getTimestamp()
meth public boolean isEnabled(java.lang.String)
meth public boolean isSystemStream()
meth public void close() throws java.io.IOException
meth public void message(java.lang.String,java.lang.String)
supr org.apache.lucene.util.InfoStream
hfds MESSAGE_ID

CLSS public abstract org.apache.lucene.util.PriorityQueue<%0 extends java.lang.Object>
cons public init(int)
cons public init(int,java.util.function.Supplier<{org.apache.lucene.util.PriorityQueue%0}>)
intf java.lang.Iterable<{org.apache.lucene.util.PriorityQueue%0}>
meth protected abstract boolean lessThan({org.apache.lucene.util.PriorityQueue%0},{org.apache.lucene.util.PriorityQueue%0})
meth protected final java.lang.Object[] getHeapArray()
meth public final boolean remove({org.apache.lucene.util.PriorityQueue%0})
meth public final int size()
meth public final void clear()
meth public final {org.apache.lucene.util.PriorityQueue%0} add({org.apache.lucene.util.PriorityQueue%0})
meth public final {org.apache.lucene.util.PriorityQueue%0} pop()
meth public final {org.apache.lucene.util.PriorityQueue%0} top()
meth public final {org.apache.lucene.util.PriorityQueue%0} updateTop()
meth public final {org.apache.lucene.util.PriorityQueue%0} updateTop({org.apache.lucene.util.PriorityQueue%0})
meth public java.util.Iterator<{org.apache.lucene.util.PriorityQueue%0}> iterator()
meth public void addAll(java.util.Collection<{org.apache.lucene.util.PriorityQueue%0}>)
meth public {org.apache.lucene.util.PriorityQueue%0} insertWithOverflow({org.apache.lucene.util.PriorityQueue%0})
supr java.lang.Object
hfds heap,maxSize,size

CLSS public org.apache.lucene.util.QueryBuilder
cons public init(org.apache.lucene.analysis.Analyzer)
fld protected boolean autoGenerateMultiTermSynonymsPhraseQuery
fld protected boolean enableGraphQueries
fld protected boolean enablePositionIncrements
fld protected org.apache.lucene.analysis.Analyzer analyzer
innr public static TermAndBoost
meth protected org.apache.lucene.search.BooleanQuery$Builder newBooleanQuery()
meth protected org.apache.lucene.search.MultiPhraseQuery$Builder newMultiPhraseQueryBuilder()
meth protected org.apache.lucene.search.Query analyzeBoolean(java.lang.String,org.apache.lucene.analysis.TokenStream) throws java.io.IOException
meth protected org.apache.lucene.search.Query analyzeGraphBoolean(java.lang.String,org.apache.lucene.analysis.TokenStream,org.apache.lucene.search.BooleanClause$Occur) throws java.io.IOException
meth protected org.apache.lucene.search.Query analyzeGraphPhrase(org.apache.lucene.analysis.TokenStream,java.lang.String,int) throws java.io.IOException
meth protected org.apache.lucene.search.Query analyzeMultiBoolean(java.lang.String,org.apache.lucene.analysis.TokenStream,org.apache.lucene.search.BooleanClause$Occur) throws java.io.IOException
meth protected org.apache.lucene.search.Query analyzeMultiPhrase(java.lang.String,org.apache.lucene.analysis.TokenStream,int) throws java.io.IOException
meth protected org.apache.lucene.search.Query analyzePhrase(java.lang.String,org.apache.lucene.analysis.TokenStream,int) throws java.io.IOException
meth protected org.apache.lucene.search.Query analyzeTerm(java.lang.String,org.apache.lucene.analysis.TokenStream) throws java.io.IOException
meth protected org.apache.lucene.search.Query createFieldQuery(org.apache.lucene.analysis.Analyzer,org.apache.lucene.search.BooleanClause$Occur,java.lang.String,java.lang.String,boolean,int)
meth protected org.apache.lucene.search.Query createFieldQuery(org.apache.lucene.analysis.TokenStream,org.apache.lucene.search.BooleanClause$Occur,java.lang.String,boolean,int)
meth protected org.apache.lucene.search.Query newGraphSynonymQuery(java.util.Iterator<org.apache.lucene.search.Query>)
meth protected org.apache.lucene.search.Query newSynonymQuery(java.lang.String,org.apache.lucene.util.QueryBuilder$TermAndBoost[])
meth protected org.apache.lucene.search.Query newTermQuery(org.apache.lucene.index.Term,float)
meth protected void add(java.lang.String,org.apache.lucene.search.BooleanQuery$Builder,java.util.List<org.apache.lucene.util.QueryBuilder$TermAndBoost>,org.apache.lucene.search.BooleanClause$Occur)
meth public boolean getAutoGenerateMultiTermSynonymsPhraseQuery()
meth public boolean getEnableGraphQueries()
meth public boolean getEnablePositionIncrements()
meth public org.apache.lucene.analysis.Analyzer getAnalyzer()
meth public org.apache.lucene.search.Query createBooleanQuery(java.lang.String,java.lang.String)
meth public org.apache.lucene.search.Query createBooleanQuery(java.lang.String,java.lang.String,org.apache.lucene.search.BooleanClause$Occur)
meth public org.apache.lucene.search.Query createMinShouldMatchQuery(java.lang.String,java.lang.String,float)
meth public org.apache.lucene.search.Query createPhraseQuery(java.lang.String,java.lang.String)
meth public org.apache.lucene.search.Query createPhraseQuery(java.lang.String,java.lang.String,int)
meth public void setAnalyzer(org.apache.lucene.analysis.Analyzer)
meth public void setAutoGenerateMultiTermSynonymsPhraseQuery(boolean)
meth public void setEnableGraphQueries(boolean)
meth public void setEnablePositionIncrements(boolean)
supr java.lang.Object

CLSS public static org.apache.lucene.util.QueryBuilder$TermAndBoost
 outer org.apache.lucene.util.QueryBuilder
cons public init(org.apache.lucene.util.BytesRef,float)
fld public final float boost
fld public final org.apache.lucene.util.BytesRef term
supr java.lang.Object

CLSS public abstract org.apache.lucene.util.RadixSelector
cons protected init(int)
meth protected abstract int byteAt(int,int)
meth protected org.apache.lucene.util.Selector getFallbackSelector(int)
meth public void select(int,int,int)
supr org.apache.lucene.util.Selector
hfds HISTOGRAM_SIZE,LENGTH_THRESHOLD,LEVEL_THRESHOLD,commonPrefix,histogram,maxLength

CLSS public final org.apache.lucene.util.RamUsageEstimator
fld public final static boolean COMPRESSED_REFS_ENABLED
fld public final static int MAX_DEPTH = 1
fld public final static int NUM_BYTES_ARRAY_HEADER
fld public final static int NUM_BYTES_OBJECT_ALIGNMENT
fld public final static int NUM_BYTES_OBJECT_HEADER
fld public final static int NUM_BYTES_OBJECT_REF
fld public final static int QUERY_DEFAULT_RAM_BYTES_USED = 1024
fld public final static int UNKNOWN_DEFAULT_RAM_BYTES_USED = 256
fld public final static java.util.Map<java.lang.Class<?>,java.lang.Integer> primitiveSizes
fld public final static long HASHTABLE_RAM_BYTES_PER_ENTRY
fld public final static long LINKED_HASHTABLE_RAM_BYTES_PER_ENTRY
fld public final static long ONE_GB = 1073741824
fld public final static long ONE_KB = 1024
fld public final static long ONE_MB = 1048576
meth public static java.lang.String humanReadableUnits(long)
meth public static java.lang.String humanReadableUnits(long,java.text.DecimalFormat)
meth public static long adjustForField(long,java.lang.reflect.Field)
meth public static long alignObjectSize(long)
meth public static long shallowSizeOf(boolean[])
meth public static long shallowSizeOf(byte[])
meth public static long shallowSizeOf(char[])
meth public static long shallowSizeOf(double[])
meth public static long shallowSizeOf(float[])
meth public static long shallowSizeOf(int[])
meth public static long shallowSizeOf(java.lang.Object)
meth public static long shallowSizeOf(java.lang.Object[])
meth public static long shallowSizeOf(long[])
meth public static long shallowSizeOf(short[])
meth public static long shallowSizeOfInstance(java.lang.Class<?>)
meth public static long sizeOf(boolean[])
meth public static long sizeOf(byte[])
meth public static long sizeOf(char[])
meth public static long sizeOf(double[])
meth public static long sizeOf(float[])
meth public static long sizeOf(int[])
meth public static long sizeOf(java.lang.Integer)
meth public static long sizeOf(java.lang.Long)
meth public static long sizeOf(java.lang.String)
meth public static long sizeOf(java.lang.String[])
meth public static long sizeOf(long[])
meth public static long sizeOf(org.apache.lucene.search.Query)
meth public static long sizeOf(org.apache.lucene.search.Query,long)
meth public static long sizeOf(org.apache.lucene.util.Accountable)
meth public static long sizeOf(org.apache.lucene.util.Accountable[])
meth public static long sizeOf(short[])
meth public static long sizeOfCollection(java.util.Collection<?>)
meth public static long sizeOfCollection(java.util.Collection<?>,long)
meth public static long sizeOfMap(java.util.Map<?,?>)
meth public static long sizeOfMap(java.util.Map<?,?>,long)
meth public static long sizeOfObject(java.lang.Object)
meth public static long sizeOfObject(java.lang.Object,long)
supr java.lang.Object
hfds INTEGER_SIZE,JVM_IS_HOTSPOT_64BIT,LONG_SIZE,STRING_SIZE
hcls RamUsageQueryVisitor

CLSS public final org.apache.lucene.util.RecyclingByteBlockAllocator
cons public init()
cons public init(int)
cons public init(int,org.apache.lucene.util.Counter)
fld public final static int DEFAULT_BUFFERED_BLOCKS = 64
meth public byte[] getByteBlock()
meth public int freeBlocks(int)
meth public int maxBufferedBlocks()
meth public int numBufferedBlocks()
meth public long bytesUsed()
meth public void recycleByteBlocks(byte[][],int,int)
supr org.apache.lucene.util.ByteBlockPool$Allocator
hfds bytesUsed,freeBlocks,freeByteBlocks,maxBufferedBlocks

CLSS public final org.apache.lucene.util.RecyclingIntBlockAllocator
cons public init()
cons public init(int,int)
cons public init(int,int,org.apache.lucene.util.Counter)
fld public final static int DEFAULT_BUFFERED_BLOCKS = 64
meth public int freeBlocks(int)
meth public int maxBufferedBlocks()
meth public int numBufferedBlocks()
meth public int[] getIntBlock()
meth public long bytesUsed()
meth public void recycleIntBlocks(int[][],int,int)
supr org.apache.lucene.util.IntBlockPool$Allocator
hfds bytesUsed,freeBlocks,freeByteBlocks,maxBufferedBlocks

CLSS public org.apache.lucene.util.RefCount<%0 extends java.lang.Object>
cons public init({org.apache.lucene.util.RefCount%0})
fld protected final {org.apache.lucene.util.RefCount%0} object
meth protected void release() throws java.io.IOException
meth public final int getRefCount()
meth public final void decRef() throws java.io.IOException
meth public final void incRef()
meth public final {org.apache.lucene.util.RefCount%0} get()
supr java.lang.Object
hfds refCount

CLSS public abstract interface org.apache.lucene.util.ResourceLoader
meth public <%0 extends java.lang.Object> {%%0} newInstance(java.lang.String,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.lang.Class<? extends {%%0}> findClass(java.lang.String,java.lang.Class<{%%0}>)
meth public abstract java.io.InputStream openResource(java.lang.String) throws java.io.IOException

CLSS public abstract interface org.apache.lucene.util.ResourceLoaderAware
meth public abstract void inform(org.apache.lucene.util.ResourceLoader) throws java.io.IOException

CLSS public org.apache.lucene.util.RoaringDocIdSet
innr public static Builder
meth public int cardinality()
meth public java.lang.String toString()
meth public long ramBytesUsed()
meth public org.apache.lucene.search.DocIdSetIterator iterator() throws java.io.IOException
supr org.apache.lucene.search.DocIdSet
hfds BASE_RAM_BYTES_USED,BLOCK_SIZE,MAX_ARRAY_LENGTH,cardinality,docIdSets,ramBytesUsed
hcls Iterator,ShortArrayDocIdSet

CLSS public static org.apache.lucene.util.RoaringDocIdSet$Builder
 outer org.apache.lucene.util.RoaringDocIdSet
cons public init(int)
meth public org.apache.lucene.util.RoaringDocIdSet build()
meth public org.apache.lucene.util.RoaringDocIdSet$Builder add(int)
meth public org.apache.lucene.util.RoaringDocIdSet$Builder add(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
supr java.lang.Object
hfds buffer,cardinality,currentBlock,currentBlockCardinality,denseBuffer,lastDocId,maxDoc,sets

CLSS public abstract org.apache.lucene.util.RollingBuffer<%0 extends org.apache.lucene.util.RollingBuffer$Resettable>
cons public init()
innr public abstract interface static Resettable
meth protected abstract {org.apache.lucene.util.RollingBuffer%0} newInstance()
meth public int getBufferSize()
meth public int getMaxPos()
meth public void freeBefore(int)
meth public void reset()
meth public {org.apache.lucene.util.RollingBuffer%0} get(int)
supr java.lang.Object
hfds buffer,count,nextPos,nextWrite

CLSS public abstract interface static org.apache.lucene.util.RollingBuffer$Resettable
 outer org.apache.lucene.util.RollingBuffer
meth public abstract void reset()

CLSS public final org.apache.lucene.util.SameThreadExecutorService
cons public init()
meth public boolean awaitTermination(long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public boolean isShutdown()
meth public boolean isTerminated()
meth public java.util.List<java.lang.Runnable> shutdownNow()
meth public void execute(java.lang.Runnable)
meth public void shutdown()
supr java.util.concurrent.AbstractExecutorService
hfds shutdown

CLSS public abstract interface org.apache.lucene.util.ScalarQuantizedVectorSimilarity
innr public static DotProduct
innr public static Euclidean
innr public static MaximumInnerProduct
meth public abstract float score(byte[],float,byte[],float)
meth public static org.apache.lucene.util.ScalarQuantizedVectorSimilarity fromVectorSimilarity(org.apache.lucene.index.VectorSimilarityFunction,float)

CLSS public static org.apache.lucene.util.ScalarQuantizedVectorSimilarity$DotProduct
 outer org.apache.lucene.util.ScalarQuantizedVectorSimilarity
cons public init(float)
intf org.apache.lucene.util.ScalarQuantizedVectorSimilarity
meth public float score(byte[],float,byte[],float)
supr java.lang.Object
hfds constMultiplier

CLSS public static org.apache.lucene.util.ScalarQuantizedVectorSimilarity$Euclidean
 outer org.apache.lucene.util.ScalarQuantizedVectorSimilarity
cons public init(float)
intf org.apache.lucene.util.ScalarQuantizedVectorSimilarity
meth public float score(byte[],float,byte[],float)
supr java.lang.Object
hfds constMultiplier

CLSS public static org.apache.lucene.util.ScalarQuantizedVectorSimilarity$MaximumInnerProduct
 outer org.apache.lucene.util.ScalarQuantizedVectorSimilarity
cons public init(float)
intf org.apache.lucene.util.ScalarQuantizedVectorSimilarity
meth public float score(byte[],float,byte[],float)
supr java.lang.Object
hfds constMultiplier

CLSS public org.apache.lucene.util.ScalarQuantizer
cons public init(float,float,float)
fld public final static int SCALAR_QUANTIZATION_SAMPLE_SIZE = 25000
meth public float getConfidenceInterval()
meth public float getConstantMultiplier()
meth public float getLowerQuantile()
meth public float getUpperQuantile()
meth public float quantize(float[],byte[],org.apache.lucene.index.VectorSimilarityFunction)
meth public float recalculateCorrectiveOffset(byte[],org.apache.lucene.util.ScalarQuantizer,org.apache.lucene.index.VectorSimilarityFunction)
meth public java.lang.String toString()
meth public static org.apache.lucene.util.ScalarQuantizer fromVectors(org.apache.lucene.index.FloatVectorValues,float) throws java.io.IOException
meth public void deQuantize(byte[],float[])
supr java.lang.Object
hfds alpha,confidenceInterval,maxQuantile,minQuantile,random,scale
hcls FloatSelector

CLSS public abstract org.apache.lucene.util.Selector
cons public init()
meth protected abstract void swap(int,int)
meth public abstract void select(int,int,int)
supr java.lang.Object

CLSS public org.apache.lucene.util.SentinelIntSet
cons public init(int,int)
fld public final int emptyVal
fld public int count
fld public int rehashCount
fld public int[] keys
meth public boolean exists(int)
meth public int find(int)
meth public int getSlot(int)
meth public int hash(int)
meth public int put(int)
meth public int size()
meth public long ramBytesUsed()
meth public void clear()
meth public void rehash()
supr java.lang.Object

CLSS public final org.apache.lucene.util.SetOnce<%0 extends java.lang.Object>
cons public init()
cons public init({org.apache.lucene.util.SetOnce%0})
innr public final static AlreadySetException
intf java.lang.Cloneable
meth public final boolean trySet({org.apache.lucene.util.SetOnce%0})
meth public final void set({org.apache.lucene.util.SetOnce%0})
meth public final {org.apache.lucene.util.SetOnce%0} get()
supr java.lang.Object
hfds set
hcls Wrapper

CLSS public final static org.apache.lucene.util.SetOnce$AlreadySetException
 outer org.apache.lucene.util.SetOnce
cons public init()
supr java.lang.IllegalStateException

CLSS public org.apache.lucene.util.SloppyMath
cons public init()
meth public static double asin(double)
meth public static double cos(double)
meth public static double haversinMeters(double)
meth public static double haversinMeters(double,double,double,double)
meth public static double haversinSortKey(double,double,double,double)
supr java.lang.Object
hfds ASIN_DELTA,ASIN_INDEXER,ASIN_MAX_VALUE_FOR_TABS,ASIN_PIO2_HI,ASIN_PIO2_LO,ASIN_PS0,ASIN_PS1,ASIN_PS2,ASIN_PS3,ASIN_PS4,ASIN_PS5,ASIN_QS1,ASIN_QS2,ASIN_QS3,ASIN_QS4,ASIN_TABS_SIZE,ONE_DIV_F2,ONE_DIV_F3,ONE_DIV_F4,PIO2_HI,PIO2_LO,SIN_COS_DELTA_HI,SIN_COS_DELTA_LO,SIN_COS_INDEXER,SIN_COS_MAX_VALUE_FOR_INT_MODULO,SIN_COS_TABS_SIZE,TO_METERS,TWOPI_HI,TWOPI_LO,asinDer1DivF1Tab,asinDer2DivF2Tab,asinDer3DivF3Tab,asinDer4DivF4Tab,asinTab,cosTab,sinTab

CLSS public org.apache.lucene.util.SmallFloat
meth public final static long int4ToLong(int)
meth public static byte floatToByte(float,int,int)
meth public static byte floatToByte315(float)
meth public static byte intToByte4(int)
meth public static float byte315ToFloat(byte)
meth public static float byteToFloat(byte,int,int)
meth public static int byte4ToInt(byte)
meth public static int longToInt4(long)
supr java.lang.Object
hfds MAX_INT4,NUM_FREE_VALUES

CLSS public abstract org.apache.lucene.util.Sorter
cons protected init()
meth protected abstract int compare(int,int)
meth protected abstract void swap(int,int)
meth protected int comparePivot(int)
meth protected void setPivot(int)
meth public abstract void sort(int,int)
supr java.lang.Object
hfds BINARY_SORT_THRESHOLD,INSERTION_SORT_THRESHOLD,pivotIndex

CLSS public org.apache.lucene.util.SparseFixedBitSet
cons public init(int)
meth public boolean get(int)
meth public boolean getAndSet(int)
meth public int approximateCardinality()
meth public int cardinality()
meth public int length()
meth public int nextSetBit(int)
meth public int prevSetBit(int)
meth public java.lang.String toString()
meth public long ramBytesUsed()
meth public void clear()
meth public void clear(int)
meth public void clear(int,int)
meth public void or(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
meth public void set(int)
supr org.apache.lucene.util.BitSet
hfds BASE_RAM_BYTES_USED,MASK_4096,SINGLE_ELEMENT_ARRAY_BYTES_USED,bits,indices,length,nonZeroLongCount,ramBytesUsed

CLSS public abstract org.apache.lucene.util.StableMSBRadixSorter
cons public init(int)
innr protected abstract static MergeSorter
meth protected abstract void restore(int,int)
meth protected abstract void save(int,int)
meth protected org.apache.lucene.util.Sorter getFallbackSorter(int)
meth protected void reorder(int,int,int[],int[],int)
supr org.apache.lucene.util.MSBRadixSorter
hfds fixedStartOffsets

CLSS protected abstract static org.apache.lucene.util.StableMSBRadixSorter$MergeSorter
 outer org.apache.lucene.util.StableMSBRadixSorter
cons protected init()
meth protected abstract void restore(int,int)
meth protected abstract void save(int,int)
meth public void sort(int,int)
supr org.apache.lucene.util.Sorter

CLSS public abstract org.apache.lucene.util.StringHelper
fld public final static int GOOD_FAST_HASH_SEED
fld public final static int ID_LENGTH = 16
meth public static boolean endsWith(org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef)
meth public static boolean startsWith(byte[],org.apache.lucene.util.BytesRef)
meth public static boolean startsWith(org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef)
meth public static byte[] randomId()
meth public static int bytesDifference(org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef)
meth public static int murmurhash3_x86_32(byte[],int,int,int)
meth public static int murmurhash3_x86_32(org.apache.lucene.util.BytesRef,int)
meth public static int sortKeyLength(org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef)
meth public static java.lang.String idToString(byte[])
meth public static org.apache.lucene.util.BytesRef intsRefToBytesRef(org.apache.lucene.util.IntsRef)
supr java.lang.Object
hfds idLock,mask128,nextId

CLSS public abstract org.apache.lucene.util.StringSorter
cons protected init(java.util.Comparator<org.apache.lucene.util.BytesRef>)
fld protected final org.apache.lucene.util.BytesRef pivot
fld protected final org.apache.lucene.util.BytesRef scratchBytes1
fld protected final org.apache.lucene.util.BytesRef scratchBytes2
fld protected final org.apache.lucene.util.BytesRefBuilder pivotBuilder
fld protected final org.apache.lucene.util.BytesRefBuilder scratch1
fld protected final org.apache.lucene.util.BytesRefBuilder scratch2
innr protected MSBStringRadixSorter
meth protected abstract void get(org.apache.lucene.util.BytesRefBuilder,org.apache.lucene.util.BytesRef,int)
meth protected int compare(int,int)
meth protected org.apache.lucene.util.Sorter fallbackSorter(java.util.Comparator<org.apache.lucene.util.BytesRef>)
meth protected org.apache.lucene.util.Sorter radixSorter(org.apache.lucene.util.BytesRefComparator)
meth public void sort(int,int)
supr org.apache.lucene.util.Sorter
hfds cmp

CLSS protected org.apache.lucene.util.StringSorter$MSBStringRadixSorter
 outer org.apache.lucene.util.StringSorter
cons protected init(org.apache.lucene.util.StringSorter,org.apache.lucene.util.BytesRefComparator)
meth protected int byteAt(int,int)
meth protected org.apache.lucene.util.Sorter getFallbackSorter(int)
meth protected void swap(int,int)
supr org.apache.lucene.util.MSBRadixSorter
hfds cmp

CLSS public abstract interface !annotation org.apache.lucene.util.SuppressForbidden
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, METHOD, TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String reason()

CLSS public org.apache.lucene.util.TermAndVector
cons public init(org.apache.lucene.util.BytesRef,float[])
meth public float[] getVector()
meth public int size()
meth public java.lang.String toString()
meth public org.apache.lucene.util.BytesRef getTerm()
meth public void normalizeVector()
supr java.lang.Object
hfds term,vector

CLSS public final org.apache.lucene.util.ThreadInterruptedException
cons public init(java.lang.InterruptedException)
supr java.lang.RuntimeException

CLSS public abstract org.apache.lucene.util.TimSorter
cons protected init(int)
meth protected abstract int compareSaved(int,int)
meth protected abstract void copy(int,int)
meth protected abstract void restore(int,int)
meth protected abstract void save(int,int)
meth public void sort(int,int)
supr org.apache.lucene.util.Sorter
hfds MINRUN,MIN_GALLOP,STACKSIZE,THRESHOLD,maxTempSlots,minRun,runEnds,stackSize,to

CLSS public final org.apache.lucene.util.ToStringUtils
meth public static java.lang.String longHex(long)
meth public static void byteArray(java.lang.StringBuilder,byte[])
supr java.lang.Object
hfds HEX

CLSS public final org.apache.lucene.util.UnicodeUtil
fld public final static int MAX_UTF8_BYTES_PER_CHAR = 3
fld public final static int UNI_REPLACEMENT_CHAR = 65533
fld public final static int UNI_SUR_HIGH_END = 56319
fld public final static int UNI_SUR_HIGH_START = 55296
fld public final static int UNI_SUR_LOW_END = 57343
fld public final static int UNI_SUR_LOW_START = 56320
fld public final static org.apache.lucene.util.BytesRef BIG_TERM
innr public final static UTF8CodePoint
meth public static boolean validUTF16String(char[],int)
meth public static boolean validUTF16String(java.lang.CharSequence)
meth public static int UTF16toUTF8(char[],int,int,byte[])
meth public static int UTF16toUTF8(java.lang.CharSequence,int,int,byte[])
meth public static int UTF16toUTF8(java.lang.CharSequence,int,int,byte[],int)
meth public static int UTF8toUTF16(byte[],int,int,char[])
meth public static int UTF8toUTF16(org.apache.lucene.util.BytesRef,char[])
meth public static int UTF8toUTF32(org.apache.lucene.util.BytesRef,int[])
meth public static int calcUTF16toUTF8Length(java.lang.CharSequence,int,int)
meth public static int codePointCount(org.apache.lucene.util.BytesRef)
meth public static int maxUTF8Length(int)
meth public static java.lang.String newString(int[],int,int)
meth public static java.lang.String toHexString(java.lang.String)
meth public static org.apache.lucene.util.UnicodeUtil$UTF8CodePoint codePointAt(byte[],int,org.apache.lucene.util.UnicodeUtil$UTF8CodePoint)
supr java.lang.Object
hfds HALF_MASK,HALF_SHIFT,LEAD_SURROGATE_MIN_VALUE,LEAD_SURROGATE_OFFSET_,LEAD_SURROGATE_SHIFT_,SUPPLEMENTARY_MIN_VALUE,SURROGATE_OFFSET,TRAIL_SURROGATE_MASK_,TRAIL_SURROGATE_MIN_VALUE,UNI_MAX_BMP,utf8CodeLength

CLSS public final static org.apache.lucene.util.UnicodeUtil$UTF8CodePoint
 outer org.apache.lucene.util.UnicodeUtil
cons public init()
fld public int codePoint
fld public int numBytes
supr java.lang.Object

CLSS public abstract interface org.apache.lucene.util.Unwrappable<%0 extends java.lang.Object>
meth public abstract {org.apache.lucene.util.Unwrappable%0} unwrap()
meth public static <%0 extends java.lang.Object> {%%0} unwrapAll({%%0})

CLSS public final org.apache.lucene.util.VectorUtil
meth public static float cosine(byte[],byte[])
meth public static float cosine(float[],float[])
meth public static float dotProduct(float[],float[])
meth public static float dotProductScore(byte[],byte[])
meth public static float scaleMaxInnerProductScore(float)
meth public static float squareDistance(float[],float[])
meth public static float[] checkFinite(float[])
meth public static float[] l2normalize(float[])
meth public static float[] l2normalize(float[],boolean)
meth public static int dotProduct(byte[],byte[])
meth public static int squareDistance(byte[],byte[])
meth public static void add(float[],float[])
supr java.lang.Object
hfds IMPL

CLSS public final org.apache.lucene.util.Version
fld public final int bugfix
fld public final int major
fld public final int minor
fld public final int prerelease
fld public final static int MIN_SUPPORTED_MAJOR
fld public final static org.apache.lucene.util.Version LATEST
fld public final static org.apache.lucene.util.Version LUCENE_8_0_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_10_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_10_1
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_11_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_11_1
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_11_2
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_12_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_1_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_1_1
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_2_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_3_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_3_1
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_4_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_4_1
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_5_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_5_1
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_5_2
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_6_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_6_1
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_6_2
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_6_3
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_7_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_8_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_8_1
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_8_2
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_8_9_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_9_0_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_9_1_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_9_2_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_9_3_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_9_4_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_9_4_1
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_9_4_2
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_9_5_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_9_6_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_9_7_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_9_8_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_9_9_0
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
fld public final static org.apache.lucene.util.Version LUCENE_9_9_1
fld public final static org.apache.lucene.util.Version LUCENE_CURRENT
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="")
meth public boolean equals(java.lang.Object)
meth public boolean onOrAfter(org.apache.lucene.util.Version)
meth public int hashCode()
meth public java.lang.String toString()
meth public static java.lang.String getPackageImplementationVersion()
meth public static org.apache.lucene.util.Version fromBits(int,int,int)
meth public static org.apache.lucene.util.Version parse(java.lang.String) throws java.text.ParseException
meth public static org.apache.lucene.util.Version parseLeniently(java.lang.String) throws java.text.ParseException
supr java.lang.Object
hfds encodedValue,implementationVersion

CLSS public final org.apache.lucene.util.VirtualMethod<%0 extends java.lang.Object>
cons public !varargs init(java.lang.Class<{org.apache.lucene.util.VirtualMethod%0}>,java.lang.String,java.lang.Class<?>[])
meth public boolean isOverriddenAsOf(java.lang.Class<? extends {org.apache.lucene.util.VirtualMethod%0}>)
meth public int getImplementationDistance(java.lang.Class<? extends {org.apache.lucene.util.VirtualMethod%0}>)
meth public static <%0 extends java.lang.Object> int compareImplementationDistance(java.lang.Class<? extends {%%0}>,org.apache.lucene.util.VirtualMethod<{%%0}>,org.apache.lucene.util.VirtualMethod<{%%0}>)
supr java.lang.Object
hfds baseClass,distanceOfClass,method,parameters,singletonSet

CLSS public final org.apache.lucene.util.WeakIdentityMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public boolean containsKey(java.lang.Object)
meth public boolean isEmpty()
meth public int size()
meth public java.util.Iterator<{org.apache.lucene.util.WeakIdentityMap%0}> keyIterator()
meth public java.util.Iterator<{org.apache.lucene.util.WeakIdentityMap%1}> valueIterator()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.apache.lucene.util.WeakIdentityMap<{%%0},{%%1}> newConcurrentHashMap()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.apache.lucene.util.WeakIdentityMap<{%%0},{%%1}> newConcurrentHashMap(boolean)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.apache.lucene.util.WeakIdentityMap<{%%0},{%%1}> newHashMap()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.apache.lucene.util.WeakIdentityMap<{%%0},{%%1}> newHashMap(boolean)
meth public void clear()
meth public void reap()
meth public {org.apache.lucene.util.WeakIdentityMap%1} get(java.lang.Object)
meth public {org.apache.lucene.util.WeakIdentityMap%1} put({org.apache.lucene.util.WeakIdentityMap%0},{org.apache.lucene.util.WeakIdentityMap%1})
meth public {org.apache.lucene.util.WeakIdentityMap%1} remove(java.lang.Object)
supr java.lang.Object
hfds NULL,backingStore,queue,reapOnRead
hcls IdentityWeakReference

CLSS public org.netbeans.modules.maven.indexer.api.NBArtifactInfo
cons public init(java.lang.String)
meth public boolean addAllVersionInfos(java.util.Collection<? extends org.netbeans.modules.maven.indexer.api.NBVersionInfo>)
meth public boolean addVersionInfo(org.netbeans.modules.maven.indexer.api.NBVersionInfo)
meth public boolean removeVersionInfo(java.lang.Object)
meth public java.lang.String getName()
meth public java.util.List<org.netbeans.modules.maven.indexer.api.NBVersionInfo> getVersionInfos()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds name,versionInfos

CLSS public org.netbeans.modules.maven.indexer.api.NBGroupInfo
cons public init(java.lang.String)
meth public boolean addAllArtifactsInfos(java.util.Collection<? extends org.netbeans.modules.maven.indexer.api.NBArtifactInfo>)
meth public boolean addArtifactInfo(org.netbeans.modules.maven.indexer.api.NBArtifactInfo)
meth public java.lang.String getName()
meth public java.util.List<org.netbeans.modules.maven.indexer.api.NBArtifactInfo> getArtifactInfos()
meth public void removeArtifactInfo(java.lang.Object)
supr java.lang.Object
hfds artifactInfos,name

CLSS public final org.netbeans.modules.maven.indexer.api.NBVersionInfo
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
intf java.lang.Comparable<org.netbeans.modules.maven.indexer.api.NBVersionInfo>
meth public boolean equals(java.lang.Object)
meth public boolean isJavadocExists()
meth public boolean isSignatureExists()
meth public boolean isSourcesExists()
meth public float getLuceneScore()
meth public int compareTo(org.netbeans.modules.maven.indexer.api.NBVersionInfo)
meth public int compareToWithoutRepoId(org.netbeans.modules.maven.indexer.api.NBVersionInfo)
meth public int hashCode()
meth public java.lang.String getArtifactId()
meth public java.lang.String getClassifier()
meth public java.lang.String getGroupId()
meth public java.lang.String getPackaging()
meth public java.lang.String getProjectDescription()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getProjectName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getRepoId()
meth public java.lang.String getType()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public long getLastModified()
meth public long getSize()
meth public void setJavadocExists(boolean)
meth public void setLastModified(long)
meth public void setLuceneScore(float)
meth public void setSignatureExists(boolean)
meth public void setSize(long)
meth public void setSourcesExists(boolean)
supr java.lang.Object
hfds artifactId,classifier,comparableVersion,groupId,javadocExists,lastModified,luceneScore,packaging,projectDescription,projectName,repoId,signatureExists,size,sourcesExists,type,version

CLSS public org.netbeans.modules.maven.indexer.api.PluginIndexManager
innr public static ParameterDetail
meth public static java.util.Map<java.lang.String,java.util.List<java.lang.String>> getLifecyclePlugins(java.lang.String,java.lang.String,java.lang.String[]) throws java.lang.Exception
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.util.Set<java.lang.String> getPluginGoalNames(java.util.Set<java.lang.String>) throws java.lang.Exception
meth public static java.util.Set<java.lang.String> getPluginGoals(java.lang.String,java.lang.String,java.lang.String) throws java.lang.Exception
meth public static java.util.Set<java.lang.String> getPluginsForGoalPrefix(java.lang.String) throws java.lang.Exception
meth public static java.util.Set<org.netbeans.modules.maven.indexer.api.PluginIndexManager$ParameterDetail> getPluginParameters(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws java.lang.Exception
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds LOG

CLSS public static org.netbeans.modules.maven.indexer.api.PluginIndexManager$ParameterDetail
 outer org.netbeans.modules.maven.indexer.api.PluginIndexManager
meth public boolean isRequired()
meth public java.lang.String getDefaultValue()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getDescription()
meth public java.lang.String getExpression()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getHtmlDetails(boolean)
meth public java.lang.String getName()
meth public java.lang.String toString()
supr java.lang.Object
hfds defaultValue,description,expression,mojos,name,required

CLSS public final org.netbeans.modules.maven.indexer.api.QueryField
cons public init()
fld public final static int MATCH_ANY = 1
fld public final static int MATCH_EXACT = 0
fld public final static int OCCUR_MUST = 0
fld public final static int OCCUR_SHOULD = 1
fld public final static java.lang.String FIELD_ANY = "any"
fld public final static java.lang.String FIELD_ARTIFACTID = "artifactId"
fld public final static java.lang.String FIELD_CLASSES = "classes"
fld public final static java.lang.String FIELD_DESCRIPTION = "description"
fld public final static java.lang.String FIELD_GROUPID = "groupId"
fld public final static java.lang.String FIELD_NAME = "name"
fld public final static java.lang.String FIELD_PACKAGING = "packaging"
fld public final static java.lang.String FIELD_VERSION = "version"
meth public int getMatch()
meth public int getOccur()
meth public java.lang.String getField()
meth public java.lang.String getValue()
meth public void setField(java.lang.String)
meth public void setMatch(int)
meth public void setOccur(int)
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds field,match,occur,value

CLSS public final org.netbeans.modules.maven.indexer.api.QueryRequest
cons public init(java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>,java.util.Observer)
 anno 2 org.netbeans.api.annotations.common.NonNull()
cons public init(java.util.List<org.netbeans.modules.maven.indexer.api.QueryField>,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>,java.util.Observer)
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public boolean isFinished()
meth public java.lang.String getClassName()
meth public java.util.List<org.netbeans.modules.maven.indexer.api.NBVersionInfo> getResults()
meth public java.util.List<org.netbeans.modules.maven.indexer.api.QueryField> getQueryFields()
meth public java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo> getRepositories()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void addResults(java.util.List<org.netbeans.modules.maven.indexer.api.NBVersionInfo>,boolean)
meth public void changeFields(java.util.List<org.netbeans.modules.maven.indexer.api.QueryField>)
supr java.util.Observable
hfds className,queryFields,queryFinished,repositories,results

CLSS public final org.netbeans.modules.maven.indexer.api.RepositoryIndexer
cons public init()
meth public static void deleteArtifactFromIndex(org.netbeans.modules.maven.indexer.api.RepositoryInfo,org.apache.maven.artifact.Artifact)
meth public static void indexRepo(org.netbeans.modules.maven.indexer.api.RepositoryInfo)
meth public static void updateIndexWithArtifacts(org.netbeans.modules.maven.indexer.api.RepositoryInfo,java.util.Collection<org.apache.maven.artifact.Artifact>)
supr java.lang.Object

CLSS public final org.netbeans.modules.maven.indexer.api.RepositoryInfo
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws java.net.URISyntaxException
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
fld public final static java.lang.String PROP_INDEX_CHANGE = "index.change"
fld public final static java.lang.String PROP_NO_REMOTE_INDEX = "no.remote.index"
innr public final static !enum MirrorStrategy
meth public boolean equals(java.lang.Object)
meth public boolean isLocal()
meth public boolean isMirror()
meth public boolean isRemoteDownloadable()
meth public int hashCode()
meth public java.lang.String getId()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getIndexUpdateUrl()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getRepositoryPath()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String getRepositoryUrl()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo> getMirroredRepositories()
meth public org.netbeans.modules.maven.indexer.api.RepositoryInfo$MirrorStrategy getMirrorStrategy()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void fireIndexChange()
meth public void fireNoIndex()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setMirrorStrategy(org.netbeans.modules.maven.indexer.api.RepositoryInfo$MirrorStrategy)
supr java.lang.Object
hfds DEFAULT_INDEX_SUFFIX,id,indexUpdateUrl,mirrorOf,mirrorStrategy,name,repositoryPath,repositoryUrl,support

CLSS public final static !enum org.netbeans.modules.maven.indexer.api.RepositoryInfo$MirrorStrategy
 outer org.netbeans.modules.maven.indexer.api.RepositoryInfo
fld public final static org.netbeans.modules.maven.indexer.api.RepositoryInfo$MirrorStrategy ALL
fld public final static org.netbeans.modules.maven.indexer.api.RepositoryInfo$MirrorStrategy NONE
fld public final static org.netbeans.modules.maven.indexer.api.RepositoryInfo$MirrorStrategy NON_WILDCARD
meth public static org.netbeans.modules.maven.indexer.api.RepositoryInfo$MirrorStrategy valueOf(java.lang.String)
meth public static org.netbeans.modules.maven.indexer.api.RepositoryInfo$MirrorStrategy[] values()
supr java.lang.Enum<org.netbeans.modules.maven.indexer.api.RepositoryInfo$MirrorStrategy>

CLSS public final org.netbeans.modules.maven.indexer.api.RepositoryPreferences
fld public final static int FREQ_ONCE_DAY = 1
fld public final static int FREQ_ONCE_WEEK = 0
fld public final static int FREQ_STARTUP = 2
fld public final static java.lang.String PROP_DOWNLOAD_INDEX = "downloadIndex"
fld public final static java.lang.String PROP_INDEX = "createIndex"
fld public final static java.lang.String PROP_INDEX_DATE_CUTOFF_FILTER = "indexDateCotoffFilter"
fld public final static java.lang.String PROP_INDEX_FREQ = "indexUpdateFrequency"
fld public final static java.lang.String PROP_LAST_INDEX_UPDATE = "lastIndexUpdate"
fld public final static java.lang.String PROP_MT_INDEX_EXTRACTION = "indexMultiThreadedExtraction"
meth public boolean isPersistent(java.lang.String)
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> remoteRepositories(org.netbeans.modules.maven.embedder.MavenEmbedder)
meth public java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo> getRepositoryInfos()
meth public org.netbeans.modules.maven.indexer.api.RepositoryInfo getLocalRepository()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.maven.indexer.api.RepositoryInfo getRepositoryInfoById(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static boolean isIndexDownloadAllowedFor(org.netbeans.modules.maven.indexer.api.RepositoryInfo)
meth public static boolean isIndexDownloadDeniedFor(org.netbeans.modules.maven.indexer.api.RepositoryInfo)
meth public static boolean isIndexDownloadEnabled()
meth public static boolean isIndexDownloadEnabledEffective()
meth public static boolean isIndexDownloadPaused()
meth public static boolean isIndexRepositories()
meth public static boolean isMultiThreadedIndexExtractionEnabled()
meth public static int getIndexDateCutoffFilter()
meth public static int getIndexUpdateFrequency()
meth public static java.util.Date getLastIndexUpdate(java.lang.String)
meth public static java.util.Map<java.lang.String,java.lang.Boolean> getIndexDownloadPermissions()
meth public static org.netbeans.modules.maven.indexer.api.RepositoryPreferences getInstance()
meth public static void allowIndexDownloadFor(org.netbeans.modules.maven.indexer.api.RepositoryInfo)
meth public static void continueIndexDownloads()
meth public static void denyIndexDownloadFor(org.netbeans.modules.maven.indexer.api.RepositoryInfo)
meth public static void pauseIndexDownloadsFor(long,java.time.temporal.ChronoUnit)
meth public static void setIndexDateCutoffFilter(int)
meth public static void setIndexDownloadEnabled(boolean)
meth public static void setIndexDownloadPermissions(java.util.Map<java.lang.String,java.lang.Boolean>)
meth public static void setIndexRepositories(boolean)
meth public static void setIndexUpdateFrequency(int)
meth public static void setLastIndexUpdate(java.lang.String,java.util.Date)
meth public static void setMultiThreadedIndexExtractionEnabled(boolean)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void addOrModifyRepositoryInfo(org.netbeans.modules.maven.indexer.api.RepositoryInfo)
meth public void addTransientRepository(java.lang.Object,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.maven.indexer.api.RepositoryInfo$MirrorStrategy) throws java.net.URISyntaxException
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void removeRepositoryInfo(org.netbeans.modules.maven.indexer.api.RepositoryInfo)
meth public void removeTransientRepositories(java.lang.Object)
supr java.lang.Object
hfds ALT_CENTRAL_URL,KEY_DISPLAY_NAME,KEY_INDEX_URL,KEY_PATH,KEY_REPO_URL,LOG,PROP_INDEX_DOWNLOAD_PERMISSIONS,central,cs,indexDownloadPauseEnd,infoCache,instance,local,permissions,transients

CLSS public final org.netbeans.modules.maven.indexer.api.RepositoryQueries
cons public init()
innr public final static ClassUsage
innr public final static Result
meth public static java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo> getLoadedContexts()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.maven.indexer.api.RepositoryQueries$Result<java.lang.String> filterPluginArtifactIdsResult(java.lang.String,java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.maven.indexer.api.RepositoryQueries$Result<java.lang.String> filterPluginGroupIdsResult(java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.maven.indexer.api.RepositoryQueries$Result<java.lang.String> getArtifactsResult(java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.maven.indexer.api.RepositoryQueries$Result<java.lang.String> getGAVsForPackaging(java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.maven.indexer.api.RepositoryQueries$Result<java.lang.String> getGroupsResult(java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.maven.indexer.api.RepositoryQueries$Result<org.netbeans.modules.maven.indexer.api.NBGroupInfo> findDependencyUsageResult(java.lang.String,java.lang.String,java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.maven.indexer.api.RepositoryQueries$Result<org.netbeans.modules.maven.indexer.api.NBVersionInfo> findArchetypesResult(java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.maven.indexer.api.RepositoryQueries$Result<org.netbeans.modules.maven.indexer.api.NBVersionInfo> findBySHA1Result(java.io.File,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.maven.indexer.api.RepositoryQueries$Result<org.netbeans.modules.maven.indexer.api.NBVersionInfo> findResult(java.util.List<org.netbeans.modules.maven.indexer.api.QueryField>,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.maven.indexer.api.RepositoryQueries$Result<org.netbeans.modules.maven.indexer.api.NBVersionInfo> findVersionsByClassResult(java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.maven.indexer.api.RepositoryQueries$Result<org.netbeans.modules.maven.indexer.api.NBVersionInfo> getRecordsResult(java.lang.String,java.lang.String,java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.maven.indexer.api.RepositoryQueries$Result<org.netbeans.modules.maven.indexer.api.NBVersionInfo> getVersionsResult(java.lang.String,java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.maven.indexer.api.RepositoryQueries$Result<org.netbeans.modules.maven.indexer.api.RepositoryQueries$ClassUsage> findClassUsagesResult(java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public static void find(org.netbeans.modules.maven.indexer.api.QueryRequest)
meth public static void findVersionsByClass(org.netbeans.modules.maven.indexer.api.QueryRequest)
supr java.lang.Object
hcls CompositeResult,QueryCall

CLSS public final static org.netbeans.modules.maven.indexer.api.RepositoryQueries$ClassUsage
 outer org.netbeans.modules.maven.indexer.api.RepositoryQueries
cons public init(org.netbeans.modules.maven.indexer.api.NBVersionInfo,java.util.Set<java.lang.String>)
meth public java.lang.String toString()
meth public java.util.Set<java.lang.String> getClasses()
meth public org.netbeans.modules.maven.indexer.api.NBVersionInfo getArtifact()
supr java.lang.Object
hfds artifact,classes

CLSS public final static org.netbeans.modules.maven.indexer.api.RepositoryQueries$Result<%0 extends java.lang.Object>
 outer org.netbeans.modules.maven.indexer.api.RepositoryQueries
cons public init(org.netbeans.modules.maven.indexer.spi.ResultImplementation<{org.netbeans.modules.maven.indexer.api.RepositoryQueries$Result%0}>)
meth public boolean isPartial()
meth public int getReturnedResultCount()
meth public int getTotalResultCount()
meth public java.util.List<{org.netbeans.modules.maven.indexer.api.RepositoryQueries$Result%0}> getResults()
meth public void waitForSkipped()
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.modules.maven.indexer.api.RepositoryUtil
meth public static java.io.File downloadArtifact(org.netbeans.modules.maven.indexer.api.NBVersionInfo) throws java.lang.Exception
meth public static java.lang.String calculateSHA1Checksum(java.io.File) throws java.io.IOException
meth public static org.apache.maven.artifact.Artifact createArtifact(org.netbeans.modules.maven.indexer.api.NBVersionInfo)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.apache.maven.artifact.Artifact createJavadocArtifact(org.netbeans.modules.maven.indexer.api.NBVersionInfo)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.ArchetypeQueries
meth public abstract org.netbeans.modules.maven.indexer.spi.ResultImplementation<org.netbeans.modules.maven.indexer.api.NBVersionInfo> findArchetypes(java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.BaseQueries
meth public abstract org.netbeans.modules.maven.indexer.spi.ResultImplementation<java.lang.String> filterPluginArtifactIds(java.lang.String,java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
meth public abstract org.netbeans.modules.maven.indexer.spi.ResultImplementation<java.lang.String> filterPluginGroupIds(java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
meth public abstract org.netbeans.modules.maven.indexer.spi.ResultImplementation<java.lang.String> getArtifacts(java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
meth public abstract org.netbeans.modules.maven.indexer.spi.ResultImplementation<java.lang.String> getGAVsForPackaging(java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
meth public abstract org.netbeans.modules.maven.indexer.spi.ResultImplementation<java.lang.String> getGroups(java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
meth public abstract org.netbeans.modules.maven.indexer.spi.ResultImplementation<org.netbeans.modules.maven.indexer.api.NBVersionInfo> getRecords(java.lang.String,java.lang.String,java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
meth public abstract org.netbeans.modules.maven.indexer.spi.ResultImplementation<org.netbeans.modules.maven.indexer.api.NBVersionInfo> getVersions(java.lang.String,java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.ChecksumQueries
meth public abstract org.netbeans.modules.maven.indexer.spi.ResultImplementation<org.netbeans.modules.maven.indexer.api.NBVersionInfo> findBySHA1(java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.ClassUsageQuery
meth public abstract org.netbeans.modules.maven.indexer.spi.ResultImplementation<org.netbeans.modules.maven.indexer.api.RepositoryQueries$ClassUsage> findClassUsages(java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.ClassesQuery
meth public abstract org.netbeans.modules.maven.indexer.spi.ResultImplementation<org.netbeans.modules.maven.indexer.api.NBVersionInfo> findVersionsByClass(java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.ContextLoadedQuery
meth public abstract java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo> getLoaded(java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.DependencyInfoQueries
meth public abstract org.netbeans.modules.maven.indexer.spi.ResultImplementation<org.netbeans.modules.maven.indexer.api.NBGroupInfo> findDependencyUsageGroups(java.lang.String,java.lang.String,java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)
meth public abstract org.netbeans.modules.maven.indexer.spi.ResultImplementation<org.netbeans.modules.maven.indexer.api.NBVersionInfo> findDependencyUsage(java.lang.String,java.lang.String,java.lang.String,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.GenericFindQuery
meth public abstract org.netbeans.modules.maven.indexer.spi.ResultImplementation<org.netbeans.modules.maven.indexer.api.NBVersionInfo> find(java.util.List<org.netbeans.modules.maven.indexer.api.QueryField>,java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo>)

CLSS public final org.netbeans.modules.maven.indexer.spi.NullResultImpl<%0 extends java.lang.Object>
cons public init()
intf org.netbeans.modules.maven.indexer.spi.ResultImplementation<{org.netbeans.modules.maven.indexer.spi.NullResultImpl%0}>
meth public boolean isPartial()
meth public int getReturnedResultCount()
meth public int getTotalResultCount()
meth public java.util.List<{org.netbeans.modules.maven.indexer.spi.NullResultImpl%0}> getResults()
meth public void waitForSkipped()
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.RepositoryIndexQueryProvider
meth public abstract boolean handlesRepository(org.netbeans.modules.maven.indexer.api.RepositoryInfo)
meth public abstract org.netbeans.modules.maven.indexer.spi.ArchetypeQueries getArchetypeQueries()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.maven.indexer.spi.BaseQueries getBaseQueries()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.maven.indexer.spi.ChecksumQueries getChecksumQueries()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.maven.indexer.spi.ClassUsageQuery getClassUsageQuery()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.maven.indexer.spi.ClassesQuery getClassesQuery()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.maven.indexer.spi.ContextLoadedQuery getContextLoadedQuery()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.maven.indexer.spi.DependencyInfoQueries getDependencyInfoQueries()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.maven.indexer.spi.GenericFindQuery getGenericFindQuery()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.ResultImplementation<%0 extends java.lang.Object>
meth public abstract boolean isPartial()
meth public abstract int getReturnedResultCount()
meth public abstract int getTotalResultCount()
meth public abstract java.util.List<{org.netbeans.modules.maven.indexer.spi.ResultImplementation%0}> getResults()
meth public abstract void waitForSkipped()

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.impl.IndexingNotificationProvider
meth public abstract void notifyError(java.lang.String)
meth public abstract void requestPermissionsFor(org.netbeans.modules.maven.indexer.api.RepositoryInfo)

CLSS public org.netbeans.modules.maven.indexer.spi.impl.LoggingIndexingNotificationProvider
cons public init()
intf org.netbeans.modules.maven.indexer.spi.impl.IndexingNotificationProvider
meth public void notifyError(java.lang.String)
meth public void requestPermissionsFor(org.netbeans.modules.maven.indexer.api.RepositoryInfo)
supr java.lang.Object
hfds LOG

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.impl.Redo<%0 extends java.lang.Object>
meth public abstract void run(org.netbeans.modules.maven.indexer.ResultImpl<{org.netbeans.modules.maven.indexer.spi.impl.Redo%0}>)

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.impl.RepositoryIndexerImplementation
meth public abstract void deleteArtifactFromIndex(org.netbeans.modules.maven.indexer.api.RepositoryInfo,org.apache.maven.artifact.Artifact)
meth public abstract void indexRepo(org.netbeans.modules.maven.indexer.api.RepositoryInfo)
meth public abstract void updateIndexWithArtifacts(org.netbeans.modules.maven.indexer.api.RepositoryInfo,java.util.Collection<org.apache.maven.artifact.Artifact>)

