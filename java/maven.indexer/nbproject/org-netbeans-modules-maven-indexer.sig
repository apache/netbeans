#Signature file v4.1
#Version 2.44

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public java.io.FileNotFoundException
cons public init()
cons public init(java.lang.String)
supr java.io.IOException
hfds serialVersionUID

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

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
meth public void mark(int) throws java.io.IOException
meth public void reset() throws java.io.IOException
supr java.lang.Object
hfds maxSkipBufferSize,skipBuffer

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

CLSS public abstract interface java.lang.Cloneable

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
hfds name,ordinal

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

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
hfds serialVersionUID

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

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
hfds serialVersionUID

CLSS public java.lang.Thread
cons public init()
cons public init(java.lang.Runnable)
cons public init(java.lang.Runnable,java.lang.String)
cons public init(java.lang.String)
cons public init(java.lang.ThreadGroup,java.lang.Runnable)
cons public init(java.lang.ThreadGroup,java.lang.Runnable,java.lang.String)
cons public init(java.lang.ThreadGroup,java.lang.Runnable,java.lang.String,long)
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
 anno 0 java.lang.Deprecated()
meth public final void setDaemon(boolean)
meth public final void setName(java.lang.String)
meth public final void setPriority(int)
meth public final void stop()
 anno 0 java.lang.Deprecated()
meth public final void stop(java.lang.Throwable)
 anno 0 java.lang.Deprecated()
meth public final void suspend()
 anno 0 java.lang.Deprecated()
meth public int countStackFrames()
 anno 0 java.lang.Deprecated()
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
meth public static void setDefaultUncaughtExceptionHandler(java.lang.Thread$UncaughtExceptionHandler)
meth public static void sleep(long) throws java.lang.InterruptedException
meth public static void sleep(long,int) throws java.lang.InterruptedException
meth public static void yield()
meth public void destroy()
 anno 0 java.lang.Deprecated()
meth public void interrupt()
meth public void run()
meth public void setContextClassLoader(java.lang.ClassLoader)
meth public void setUncaughtExceptionHandler(java.lang.Thread$UncaughtExceptionHandler)
meth public void start()
supr java.lang.Object
hfds EMPTY_STACK_TRACE,SUBCLASS_IMPLEMENTATION_PERMISSION,blocker,blockerLock,contextClassLoader,daemon,defaultUncaughtExceptionHandler,eetop,group,inheritableThreadLocals,inheritedAccessControlContext,name,nativeParkEventPointer,parkBlocker,priority,single_step,stackSize,stillborn,target,threadInitNumber,threadLocalRandomProbe,threadLocalRandomSecondarySeed,threadLocalRandomSeed,threadLocals,threadQ,threadSeqNumber,threadStatus,tid,uncaughtExceptionHandler
hcls Caches,WeakClassKey

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
hfds CAUSE_CAPTION,EMPTY_THROWABLE_ARRAY,NULL_CAUSE_MESSAGE,SELF_SUPPRESSION_MESSAGE,SUPPRESSED_CAPTION,SUPPRESSED_SENTINEL,UNASSIGNED_STACK,backtrace,cause,detailMessage,serialVersionUID,stackTrace,suppressedExceptions
hcls PrintStreamOrWriter,SentinelHolder,WrappedPrintStream,WrappedPrintWriter

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
hfds keySet,values

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
hfds DEFAULT_INITIAL_CAPACITY,DEFAULT_LOAD_FACTOR,MAXIMUM_CAPACITY,MIN_TREEIFY_CAPACITY,TREEIFY_THRESHOLD,UNTREEIFY_THRESHOLD,entrySet,loadFactor,modCount,serialVersionUID,size,table,threshold
hcls EntryIterator,EntrySet,EntrySpliterator,HashIterator,HashMapSpliterator,KeyIterator,KeySet,KeySpliterator,Node,TreeNode,ValueIterator,ValueSpliterator,Values

CLSS public abstract interface java.util.Iterator<%0 extends java.lang.Object>
meth public abstract boolean hasNext()
meth public abstract {java.util.Iterator%0} next()
meth public void forEachRemaining(java.util.function.Consumer<? super {java.util.Iterator%0}>)
meth public void remove()

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

CLSS public java.util.Observable
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
hfds changed,obs

CLSS public abstract interface java.util.concurrent.ThreadFactory
meth public abstract java.lang.Thread newThread(java.lang.Runnable)

CLSS public abstract interface java.util.zip.Checksum
meth public abstract long getValue()
meth public abstract void reset()
meth public abstract void update(byte[],int,int)
meth public abstract void update(int)

CLSS public final org.apache.lucene.LucenePackage
meth public static java.lang.Package get()
supr java.lang.Object

CLSS public abstract org.apache.lucene.analysis.Analyzer
cons public init()
cons public init(org.apache.lucene.analysis.Analyzer$ReuseStrategy)
fld public final static org.apache.lucene.analysis.Analyzer$ReuseStrategy GLOBAL_REUSE_STRATEGY
fld public final static org.apache.lucene.analysis.Analyzer$ReuseStrategy PER_FIELD_REUSE_STRATEGY
innr public abstract static ReuseStrategy
innr public static TokenStreamComponents
intf java.io.Closeable
meth protected abstract org.apache.lucene.analysis.Analyzer$TokenStreamComponents createComponents(java.lang.String)
meth protected java.io.Reader initReader(java.lang.String,java.io.Reader)
meth public final org.apache.lucene.analysis.Analyzer$ReuseStrategy getReuseStrategy()
meth public final org.apache.lucene.analysis.TokenStream tokenStream(java.lang.String,java.io.Reader)
meth public final org.apache.lucene.analysis.TokenStream tokenStream(java.lang.String,java.lang.String)
meth public int getOffsetGap(java.lang.String)
meth public int getPositionIncrementGap(java.lang.String)
meth public org.apache.lucene.util.Version getVersion()
meth public void close()
meth public void setVersion(org.apache.lucene.util.Version)
supr java.lang.Object
hfds reuseStrategy,storedValue,version

CLSS public abstract static org.apache.lucene.analysis.Analyzer$ReuseStrategy
 outer org.apache.lucene.analysis.Analyzer
cons public init()
meth protected final java.lang.Object getStoredValue(org.apache.lucene.analysis.Analyzer)
meth protected final void setStoredValue(org.apache.lucene.analysis.Analyzer,java.lang.Object)
meth public abstract org.apache.lucene.analysis.Analyzer$TokenStreamComponents getReusableComponents(org.apache.lucene.analysis.Analyzer,java.lang.String)
meth public abstract void setReusableComponents(org.apache.lucene.analysis.Analyzer,java.lang.String,org.apache.lucene.analysis.Analyzer$TokenStreamComponents)
supr java.lang.Object

CLSS public static org.apache.lucene.analysis.Analyzer$TokenStreamComponents
 outer org.apache.lucene.analysis.Analyzer
cons public init(org.apache.lucene.analysis.Tokenizer)
cons public init(org.apache.lucene.analysis.Tokenizer,org.apache.lucene.analysis.TokenStream)
fld protected final org.apache.lucene.analysis.TokenStream sink
fld protected final org.apache.lucene.analysis.Tokenizer source
meth protected void setReader(java.io.Reader)
meth public org.apache.lucene.analysis.TokenStream getTokenStream()
meth public org.apache.lucene.analysis.Tokenizer getTokenizer()
supr java.lang.Object
hfds reusableStringReader

CLSS public abstract org.apache.lucene.analysis.AnalyzerWrapper
cons protected init(org.apache.lucene.analysis.Analyzer$ReuseStrategy)
meth protected abstract org.apache.lucene.analysis.Analyzer getWrappedAnalyzer(java.lang.String)
meth protected final org.apache.lucene.analysis.Analyzer$TokenStreamComponents createComponents(java.lang.String)
meth protected java.io.Reader wrapReader(java.lang.String,java.io.Reader)
meth protected org.apache.lucene.analysis.Analyzer$TokenStreamComponents wrapComponents(java.lang.String,org.apache.lucene.analysis.Analyzer$TokenStreamComponents)
meth public final java.io.Reader initReader(java.lang.String,java.io.Reader)
meth public int getOffsetGap(java.lang.String)
meth public int getPositionIncrementGap(java.lang.String)
supr org.apache.lucene.analysis.Analyzer

CLSS public final org.apache.lucene.analysis.CachingTokenFilter
cons public init(org.apache.lucene.analysis.TokenStream)
meth public boolean isCached()
meth public final boolean incrementToken() throws java.io.IOException
meth public final void end()
meth public void reset() throws java.io.IOException
supr org.apache.lucene.analysis.TokenFilter
hfds cache,finalState,iterator

CLSS public abstract org.apache.lucene.analysis.CharFilter
cons public init(java.io.Reader)
fld protected final java.io.Reader input
meth protected abstract int correct(int)
meth public final int correctOffset(int)
meth public void close() throws java.io.IOException
supr java.io.Reader

CLSS public abstract org.apache.lucene.analysis.DelegatingAnalyzerWrapper
cons protected init(org.apache.lucene.analysis.Analyzer$ReuseStrategy)
meth protected final java.io.Reader wrapReader(java.lang.String,java.io.Reader)
meth protected final org.apache.lucene.analysis.Analyzer$TokenStreamComponents wrapComponents(java.lang.String,org.apache.lucene.analysis.Analyzer$TokenStreamComponents)
supr org.apache.lucene.analysis.AnalyzerWrapper
hcls DelegatingReuseStrategy

CLSS public final org.apache.lucene.analysis.NumericTokenStream
cons public init()
cons public init(int)
cons public init(org.apache.lucene.util.AttributeFactory,int)
fld public final static java.lang.String TOKEN_TYPE_FULL_PREC = "fullPrecNumeric"
fld public final static java.lang.String TOKEN_TYPE_LOWER_PREC = "lowerPrecNumeric"
innr public abstract interface static NumericTermAttribute
innr public final static NumericTermAttributeImpl
meth public boolean incrementToken()
meth public int getPrecisionStep()
meth public java.lang.String toString()
meth public org.apache.lucene.analysis.NumericTokenStream setDoubleValue(double)
meth public org.apache.lucene.analysis.NumericTokenStream setFloatValue(float)
meth public org.apache.lucene.analysis.NumericTokenStream setIntValue(int)
meth public org.apache.lucene.analysis.NumericTokenStream setLongValue(long)
meth public void reset()
supr org.apache.lucene.analysis.TokenStream
hfds numericAtt,posIncrAtt,precisionStep,typeAtt,valSize
hcls NumericAttributeFactory

CLSS public abstract interface static org.apache.lucene.analysis.NumericTokenStream$NumericTermAttribute
 outer org.apache.lucene.analysis.NumericTokenStream
intf org.apache.lucene.util.Attribute
meth public abstract int getShift()
meth public abstract int getValueSize()
meth public abstract int incShift()
meth public abstract long getRawValue()
meth public abstract void init(long,int,int,int)
meth public abstract void setShift(int)

CLSS public final static org.apache.lucene.analysis.NumericTokenStream$NumericTermAttributeImpl
 outer org.apache.lucene.analysis.NumericTokenStream
cons public init()
intf org.apache.lucene.analysis.NumericTokenStream$NumericTermAttribute
intf org.apache.lucene.analysis.tokenattributes.TermToBytesRefAttribute
meth public boolean equals(java.lang.Object)
meth public int getShift()
meth public int getValueSize()
meth public int hashCode()
meth public int incShift()
meth public long getRawValue()
meth public org.apache.lucene.analysis.NumericTokenStream$NumericTermAttributeImpl clone()
meth public org.apache.lucene.util.BytesRef getBytesRef()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void init(long,int,int,int)
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public void setShift(int)
supr org.apache.lucene.util.AttributeImpl
hfds bytes,precisionStep,shift,value,valueSize

CLSS public org.apache.lucene.analysis.Token
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(java.lang.CharSequence,int,int)
fld public final static org.apache.lucene.util.AttributeFactory TOKEN_ATTRIBUTE_FACTORY
intf org.apache.lucene.analysis.tokenattributes.FlagsAttribute
intf org.apache.lucene.analysis.tokenattributes.PayloadAttribute
meth public boolean equals(java.lang.Object)
meth public int getFlags()
meth public int hashCode()
meth public org.apache.lucene.analysis.Token clone()
meth public org.apache.lucene.util.BytesRef getPayload()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
meth public void reinit(org.apache.lucene.analysis.Token)
meth public void setFlags(int)
meth public void setPayload(org.apache.lucene.util.BytesRef)
supr org.apache.lucene.analysis.tokenattributes.PackedTokenAttributeImpl
hfds flags,payload

CLSS public abstract org.apache.lucene.analysis.TokenFilter
cons protected init(org.apache.lucene.analysis.TokenStream)
fld protected final org.apache.lucene.analysis.TokenStream input
meth public void close() throws java.io.IOException
meth public void end() throws java.io.IOException
meth public void reset() throws java.io.IOException
supr org.apache.lucene.analysis.TokenStream

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
meth public void setPreservePositionIncrements(boolean)
meth public void setUnicodeArcs(boolean)
supr java.lang.Object
hfds preservePositionIncrements,unicodeArcs
hcls Position,Positions

CLSS public abstract org.apache.lucene.analysis.Tokenizer
cons protected init()
cons protected init(org.apache.lucene.util.AttributeFactory)
fld protected java.io.Reader input
meth protected final int correctOffset(int)
meth public final void setReader(java.io.Reader)
meth public void close() throws java.io.IOException
meth public void reset() throws java.io.IOException
supr org.apache.lucene.analysis.TokenStream
hfds ILLEGAL_STATE_READER,inputPending

CLSS abstract interface org.apache.lucene.analysis.package-info

CLSS public final org.apache.lucene.analysis.standard.ClassicAnalyzer
cons public init()
cons public init(java.io.Reader) throws java.io.IOException
cons public init(org.apache.lucene.analysis.util.CharArraySet)
fld public final static int DEFAULT_MAX_TOKEN_LENGTH = 255
fld public final static org.apache.lucene.analysis.util.CharArraySet STOP_WORDS_SET
meth protected org.apache.lucene.analysis.Analyzer$TokenStreamComponents createComponents(java.lang.String)
meth public int getMaxTokenLength()
meth public void setMaxTokenLength(int)
supr org.apache.lucene.analysis.util.StopwordAnalyzerBase
hfds maxTokenLength

CLSS public org.apache.lucene.analysis.standard.ClassicFilter
cons public init(org.apache.lucene.analysis.TokenStream)
meth public final boolean incrementToken() throws java.io.IOException
supr org.apache.lucene.analysis.TokenFilter
hfds ACRONYM_TYPE,APOSTROPHE_TYPE,termAtt,typeAtt

CLSS public org.apache.lucene.analysis.standard.ClassicFilterFactory
cons public init(java.util.Map<java.lang.String,java.lang.String>)
meth public org.apache.lucene.analysis.TokenFilter create(org.apache.lucene.analysis.TokenStream)
supr org.apache.lucene.analysis.util.TokenFilterFactory

CLSS public final org.apache.lucene.analysis.standard.ClassicTokenizer
cons public init()
cons public init(org.apache.lucene.util.AttributeFactory)
fld public final static int ACRONYM = 2
fld public final static int ACRONYM_DEP = 8
fld public final static int ALPHANUM = 0
fld public final static int APOSTROPHE = 1
fld public final static int CJ = 7
fld public final static int COMPANY = 3
fld public final static int EMAIL = 4
fld public final static int HOST = 5
fld public final static int NUM = 6
fld public final static java.lang.String[] TOKEN_TYPES
meth public final boolean incrementToken() throws java.io.IOException
meth public final void end() throws java.io.IOException
meth public int getMaxTokenLength()
meth public void close() throws java.io.IOException
meth public void reset() throws java.io.IOException
meth public void setMaxTokenLength(int)
supr org.apache.lucene.analysis.Tokenizer
hfds maxTokenLength,offsetAtt,posIncrAtt,scanner,skippedPositions,termAtt,typeAtt

CLSS public org.apache.lucene.analysis.standard.ClassicTokenizerFactory
cons public init(java.util.Map<java.lang.String,java.lang.String>)
meth public org.apache.lucene.analysis.standard.ClassicTokenizer create(org.apache.lucene.util.AttributeFactory)
supr org.apache.lucene.analysis.util.TokenizerFactory
hfds maxTokenLength

CLSS public final org.apache.lucene.analysis.standard.StandardAnalyzer
cons public init()
cons public init(java.io.Reader) throws java.io.IOException
cons public init(org.apache.lucene.analysis.util.CharArraySet)
fld public final static int DEFAULT_MAX_TOKEN_LENGTH = 255
fld public final static org.apache.lucene.analysis.util.CharArraySet STOP_WORDS_SET
meth protected org.apache.lucene.analysis.Analyzer$TokenStreamComponents createComponents(java.lang.String)
meth public int getMaxTokenLength()
meth public void setMaxTokenLength(int)
supr org.apache.lucene.analysis.util.StopwordAnalyzerBase
hfds maxTokenLength

CLSS public org.apache.lucene.analysis.standard.StandardFilter
cons public init(org.apache.lucene.analysis.TokenStream)
meth public final boolean incrementToken() throws java.io.IOException
supr org.apache.lucene.analysis.TokenFilter

CLSS public org.apache.lucene.analysis.standard.StandardFilterFactory
cons public init(java.util.Map<java.lang.String,java.lang.String>)
meth public org.apache.lucene.analysis.standard.StandardFilter create(org.apache.lucene.analysis.TokenStream)
supr org.apache.lucene.analysis.util.TokenFilterFactory

CLSS public final org.apache.lucene.analysis.standard.StandardTokenizer
cons public init()
cons public init(org.apache.lucene.util.AttributeFactory)
fld public final static int ACRONYM = 2
 anno 0 java.lang.Deprecated()
fld public final static int ACRONYM_DEP = 8
 anno 0 java.lang.Deprecated()
fld public final static int ALPHANUM = 0
fld public final static int APOSTROPHE = 1
 anno 0 java.lang.Deprecated()
fld public final static int CJ = 7
 anno 0 java.lang.Deprecated()
fld public final static int COMPANY = 3
 anno 0 java.lang.Deprecated()
fld public final static int EMAIL = 4
fld public final static int HANGUL = 13
fld public final static int HIRAGANA = 11
fld public final static int HOST = 5
 anno 0 java.lang.Deprecated()
fld public final static int IDEOGRAPHIC = 10
fld public final static int KATAKANA = 12
fld public final static int MAX_TOKEN_LENGTH_LIMIT = 1048576
fld public final static int NUM = 6
fld public final static int SOUTHEAST_ASIAN = 9
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
cons public init(java.util.Map<java.lang.String,java.lang.String>)
meth public org.apache.lucene.analysis.Tokenizer create(org.apache.lucene.util.AttributeFactory)
supr org.apache.lucene.analysis.util.TokenizerFactory
hfds maxTokenLength

CLSS public final org.apache.lucene.analysis.standard.StandardTokenizerImpl
cons public init(java.io.Reader)
fld public final static int HANGUL_TYPE = 13
fld public final static int HIRAGANA_TYPE = 11
fld public final static int IDEOGRAPHIC_TYPE = 10
fld public final static int KATAKANA_TYPE = 12
fld public final static int NUMERIC_TYPE = 6
fld public final static int SOUTH_EAST_ASIAN_TYPE = 9
fld public final static int WORD_TYPE = 0
fld public final static int YYEOF = -1
fld public final static int YYINITIAL = 0
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
hfds ZZ_ACTION,ZZ_ACTION_PACKED_0,ZZ_ATTRIBUTE,ZZ_ATTRIBUTE_PACKED_0,ZZ_BUFFERSIZE,ZZ_CMAP,ZZ_CMAP_PACKED,ZZ_ERROR_MSG,ZZ_LEXSTATE,ZZ_NO_MATCH,ZZ_PUSHBACK_2BIG,ZZ_ROWMAP,ZZ_ROWMAP_PACKED_0,ZZ_TRANS,ZZ_TRANS_PACKED_0,ZZ_UNKNOWN_ERROR,yychar,yycolumn,yyline,zzAtBOL,zzAtEOF,zzBuffer,zzCurrentPos,zzEOFDone,zzEndRead,zzFinalHighSurrogate,zzLexicalState,zzMarkedPos,zzReader,zzStartRead,zzState

CLSS public final org.apache.lucene.analysis.standard.UAX29URLEmailAnalyzer
cons public init()
cons public init(java.io.Reader) throws java.io.IOException
cons public init(org.apache.lucene.analysis.util.CharArraySet)
fld public final static int DEFAULT_MAX_TOKEN_LENGTH = 255
fld public final static org.apache.lucene.analysis.util.CharArraySet STOP_WORDS_SET
meth protected org.apache.lucene.analysis.Analyzer$TokenStreamComponents createComponents(java.lang.String)
meth public int getMaxTokenLength()
meth public void setMaxTokenLength(int)
supr org.apache.lucene.analysis.util.StopwordAnalyzerBase
hfds maxTokenLength

CLSS public final org.apache.lucene.analysis.standard.UAX29URLEmailTokenizer
cons public init()
cons public init(org.apache.lucene.util.AttributeFactory)
fld public final static int ALPHANUM = 0
fld public final static int EMAIL = 8
fld public final static int HANGUL = 6
fld public final static int HIRAGANA = 4
fld public final static int IDEOGRAPHIC = 3
fld public final static int KATAKANA = 5
fld public final static int NUM = 1
fld public final static int SOUTHEAST_ASIAN = 2
fld public final static int URL = 7
fld public final static java.lang.String[] TOKEN_TYPES
meth public final boolean incrementToken() throws java.io.IOException
meth public final void end() throws java.io.IOException
meth public int getMaxTokenLength()
meth public void close() throws java.io.IOException
meth public void reset() throws java.io.IOException
meth public void setMaxTokenLength(int)
supr org.apache.lucene.analysis.Tokenizer
hfds maxTokenLength,offsetAtt,posIncrAtt,scanner,skippedPositions,termAtt,typeAtt

CLSS public org.apache.lucene.analysis.standard.UAX29URLEmailTokenizerFactory
cons public init(java.util.Map<java.lang.String,java.lang.String>)
meth public org.apache.lucene.analysis.Tokenizer create(org.apache.lucene.util.AttributeFactory)
supr org.apache.lucene.analysis.util.TokenizerFactory
hfds maxTokenLength

CLSS public final org.apache.lucene.analysis.standard.UAX29URLEmailTokenizerImpl
cons public init(java.io.Reader)
fld public final static int AVOID_BAD_URL = 2
fld public final static int EMAIL_TYPE = 8
fld public final static int HANGUL_TYPE = 6
fld public final static int HIRAGANA_TYPE = 4
fld public final static int IDEOGRAPHIC_TYPE = 3
fld public final static int KATAKANA_TYPE = 5
fld public final static int NUMERIC_TYPE = 1
fld public final static int SOUTH_EAST_ASIAN_TYPE = 2
fld public final static int URL_TYPE = 7
fld public final static int WORD_TYPE = 0
fld public final static int YYEOF = -1
fld public final static int YYINITIAL = 0
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
hfds ZZ_ACTION,ZZ_ACTION_PACKED_0,ZZ_ATTRIBUTE,ZZ_ATTRIBUTE_PACKED_0,ZZ_BUFFERSIZE,ZZ_CMAP,ZZ_CMAP_PACKED,ZZ_ERROR_MSG,ZZ_LEXSTATE,ZZ_NO_MATCH,ZZ_PUSHBACK_2BIG,ZZ_ROWMAP,ZZ_ROWMAP_PACKED_0,ZZ_TRANS,ZZ_TRANS_PACKED_0,ZZ_TRANS_PACKED_1,ZZ_TRANS_PACKED_2,ZZ_UNKNOWN_ERROR,yychar,yycolumn,yyline,zzAtBOL,zzAtEOF,zzBuffer,zzCurrentPos,zzEOFDone,zzEndRead,zzFinalHighSurrogate,zzLexicalState,zzMarkedPos,zzReader,zzStartRead,zzState

CLSS abstract interface org.apache.lucene.analysis.standard.package-info

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
intf java.lang.Cloneable
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
intf java.lang.Cloneable
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
intf java.lang.Cloneable
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
intf org.apache.lucene.analysis.tokenattributes.TypeAttribute
meth public boolean equals(java.lang.Object)
meth public final int endOffset()
meth public final int startOffset()
meth public final java.lang.String type()
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
hfds endOffset,positionIncrement,positionLength,startOffset,type

CLSS public abstract interface org.apache.lucene.analysis.tokenattributes.PayloadAttribute
intf org.apache.lucene.util.Attribute
meth public abstract org.apache.lucene.util.BytesRef getPayload()
meth public abstract void setPayload(org.apache.lucene.util.BytesRef)

CLSS public org.apache.lucene.analysis.tokenattributes.PayloadAttributeImpl
cons public init()
cons public init(org.apache.lucene.util.BytesRef)
intf java.lang.Cloneable
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
intf java.lang.Cloneable
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
intf java.lang.Cloneable
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
intf java.lang.Cloneable
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

CLSS abstract interface org.apache.lucene.analysis.tokenattributes.package-info

CLSS public abstract org.apache.lucene.analysis.util.AbstractAnalysisFactory
cons protected init(java.util.Map<java.lang.String,java.lang.String>)
fld protected final org.apache.lucene.util.Version luceneMatchVersion
fld public final static java.lang.String LUCENE_MATCH_VERSION_PARAM = "luceneMatchVersion"
meth protected final boolean getBoolean(java.util.Map<java.lang.String,java.lang.String>,java.lang.String,boolean)
meth protected final boolean requireBoolean(java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
meth protected final float getFloat(java.util.Map<java.lang.String,java.lang.String>,java.lang.String,float)
meth protected final float requireFloat(java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
meth protected final int getInt(java.util.Map<java.lang.String,java.lang.String>,java.lang.String,int)
meth protected final int requireInt(java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
meth protected final java.util.List<java.lang.String> getLines(org.apache.lucene.analysis.util.ResourceLoader,java.lang.String) throws java.io.IOException
meth protected final java.util.List<java.lang.String> splitFileNames(java.lang.String)
meth protected final java.util.regex.Pattern getPattern(java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
meth protected final org.apache.lucene.analysis.util.CharArraySet getSnowballWordSet(org.apache.lucene.analysis.util.ResourceLoader,java.lang.String,boolean) throws java.io.IOException
meth protected final org.apache.lucene.analysis.util.CharArraySet getWordSet(org.apache.lucene.analysis.util.ResourceLoader,java.lang.String,boolean) throws java.io.IOException
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
hfds CLASS_NAME,ITEM_PATTERN,isExplicitLuceneMatchVersion,originalArgs

CLSS public abstract org.apache.lucene.analysis.util.StopwordAnalyzerBase
cons protected init()
cons protected init(org.apache.lucene.analysis.util.CharArraySet)
fld protected final org.apache.lucene.analysis.util.CharArraySet stopwords
meth protected static org.apache.lucene.analysis.util.CharArraySet loadStopwordSet(boolean,java.lang.Class<? extends org.apache.lucene.analysis.Analyzer>,java.lang.String,java.lang.String) throws java.io.IOException
meth protected static org.apache.lucene.analysis.util.CharArraySet loadStopwordSet(java.io.Reader) throws java.io.IOException
meth protected static org.apache.lucene.analysis.util.CharArraySet loadStopwordSet(java.nio.file.Path) throws java.io.IOException
meth public org.apache.lucene.analysis.util.CharArraySet getStopwordSet()
supr org.apache.lucene.analysis.Analyzer

CLSS public abstract org.apache.lucene.analysis.util.TokenFilterFactory
cons protected init(java.util.Map<java.lang.String,java.lang.String>)
meth public abstract org.apache.lucene.analysis.TokenStream create(org.apache.lucene.analysis.TokenStream)
meth public static java.lang.Class<? extends org.apache.lucene.analysis.util.TokenFilterFactory> lookupClass(java.lang.String)
meth public static java.util.Set<java.lang.String> availableTokenFilters()
meth public static org.apache.lucene.analysis.util.TokenFilterFactory forName(java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
meth public static void reloadTokenFilters(java.lang.ClassLoader)
supr org.apache.lucene.analysis.util.AbstractAnalysisFactory
hfds loader

CLSS public abstract org.apache.lucene.analysis.util.TokenizerFactory
cons protected init(java.util.Map<java.lang.String,java.lang.String>)
meth public abstract org.apache.lucene.analysis.Tokenizer create(org.apache.lucene.util.AttributeFactory)
meth public final org.apache.lucene.analysis.Tokenizer create()
meth public static java.lang.Class<? extends org.apache.lucene.analysis.util.TokenizerFactory> lookupClass(java.lang.String)
meth public static java.util.Set<java.lang.String> availableTokenizers()
meth public static org.apache.lucene.analysis.util.TokenizerFactory forName(java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
meth public static void reloadTokenizers(java.lang.ClassLoader)
supr org.apache.lucene.analysis.util.AbstractAnalysisFactory
hfds loader

CLSS public org.apache.lucene.document.BinaryDocValuesField
cons public init(java.lang.String,org.apache.lucene.util.BytesRef)
fld public final static org.apache.lucene.document.FieldType TYPE
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.CompressionTools
meth public static byte[] compress(byte[])
meth public static byte[] compress(byte[],int,int)
meth public static byte[] compress(byte[],int,int,int)
meth public static byte[] compressString(java.lang.String)
meth public static byte[] compressString(java.lang.String,int)
meth public static byte[] decompress(byte[]) throws java.util.zip.DataFormatException
meth public static byte[] decompress(byte[],int,int) throws java.util.zip.DataFormatException
meth public static byte[] decompress(org.apache.lucene.util.BytesRef) throws java.util.zip.DataFormatException
meth public static java.lang.String decompressString(byte[]) throws java.util.zip.DataFormatException
meth public static java.lang.String decompressString(byte[],int,int) throws java.util.zip.DataFormatException
meth public static java.lang.String decompressString(org.apache.lucene.util.BytesRef) throws java.util.zip.DataFormatException
supr java.lang.Object

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
meth public void stringField(org.apache.lucene.index.FieldInfo,byte[]) throws java.io.IOException
supr org.apache.lucene.index.StoredFieldVisitor
hfds doc,fieldsToAdd

CLSS public org.apache.lucene.document.DoubleDocValuesField
cons public init(java.lang.String,double)
meth public void setDoubleValue(double)
meth public void setLongValue(long)
supr org.apache.lucene.document.NumericDocValuesField

CLSS public final org.apache.lucene.document.DoubleField
cons public init(java.lang.String,double,org.apache.lucene.document.Field$Store)
cons public init(java.lang.String,double,org.apache.lucene.document.FieldType)
fld public final static org.apache.lucene.document.FieldType TYPE_NOT_STORED
fld public final static org.apache.lucene.document.FieldType TYPE_STORED
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.Field
cons protected init(java.lang.String,org.apache.lucene.document.FieldType)
cons public init(java.lang.String,byte[])
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,byte[],int,int)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,byte[],int,int,org.apache.lucene.document.FieldType)
cons public init(java.lang.String,byte[],org.apache.lucene.document.FieldType)
cons public init(java.lang.String,java.io.Reader)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.io.Reader,org.apache.lucene.document.Field$TermVector)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.io.Reader,org.apache.lucene.document.FieldType)
cons public init(java.lang.String,java.lang.String,org.apache.lucene.document.Field$Store,org.apache.lucene.document.Field$Index)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String,org.apache.lucene.document.Field$Store,org.apache.lucene.document.Field$Index,org.apache.lucene.document.Field$TermVector)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String,org.apache.lucene.document.FieldType)
cons public init(java.lang.String,org.apache.lucene.analysis.TokenStream)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,org.apache.lucene.analysis.TokenStream,org.apache.lucene.document.Field$TermVector)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,org.apache.lucene.analysis.TokenStream,org.apache.lucene.document.FieldType)
cons public init(java.lang.String,org.apache.lucene.util.BytesRef,org.apache.lucene.document.FieldType)
fld protected final java.lang.String name
fld protected final org.apache.lucene.document.FieldType type
fld protected float boost
fld protected java.lang.Object fieldsData
fld protected org.apache.lucene.analysis.TokenStream tokenStream
innr public abstract static !enum Index
innr public abstract static !enum TermVector
innr public final static !enum Store
intf org.apache.lucene.index.IndexableField
meth public final static org.apache.lucene.document.FieldType translateFieldType(org.apache.lucene.document.Field$Store,org.apache.lucene.document.Field$Index,org.apache.lucene.document.Field$TermVector)
 anno 0 java.lang.Deprecated()
meth public float boost()
meth public java.io.Reader readerValue()
meth public java.lang.Number numericValue()
meth public java.lang.String name()
meth public java.lang.String stringValue()
meth public java.lang.String toString()
meth public org.apache.lucene.analysis.TokenStream tokenStream(org.apache.lucene.analysis.Analyzer,org.apache.lucene.analysis.TokenStream)
meth public org.apache.lucene.analysis.TokenStream tokenStreamValue()
meth public org.apache.lucene.document.FieldType fieldType()
meth public org.apache.lucene.util.BytesRef binaryValue()
meth public void setBoost(float)
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

CLSS public abstract static !enum org.apache.lucene.document.Field$Index
 outer org.apache.lucene.document.Field
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.document.Field$Index ANALYZED
fld public final static org.apache.lucene.document.Field$Index ANALYZED_NO_NORMS
fld public final static org.apache.lucene.document.Field$Index NO
fld public final static org.apache.lucene.document.Field$Index NOT_ANALYZED
fld public final static org.apache.lucene.document.Field$Index NOT_ANALYZED_NO_NORMS
meth public abstract boolean isAnalyzed()
meth public abstract boolean isIndexed()
meth public abstract boolean omitNorms()
meth public static org.apache.lucene.document.Field$Index toIndex(boolean,boolean)
meth public static org.apache.lucene.document.Field$Index toIndex(boolean,boolean,boolean)
meth public static org.apache.lucene.document.Field$Index valueOf(java.lang.String)
meth public static org.apache.lucene.document.Field$Index[] values()
supr java.lang.Enum<org.apache.lucene.document.Field$Index>

CLSS public final static !enum org.apache.lucene.document.Field$Store
 outer org.apache.lucene.document.Field
fld public final static org.apache.lucene.document.Field$Store NO
fld public final static org.apache.lucene.document.Field$Store YES
meth public static org.apache.lucene.document.Field$Store valueOf(java.lang.String)
meth public static org.apache.lucene.document.Field$Store[] values()
supr java.lang.Enum<org.apache.lucene.document.Field$Store>

CLSS public abstract static !enum org.apache.lucene.document.Field$TermVector
 outer org.apache.lucene.document.Field
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.document.Field$TermVector NO
fld public final static org.apache.lucene.document.Field$TermVector WITH_OFFSETS
fld public final static org.apache.lucene.document.Field$TermVector WITH_POSITIONS
fld public final static org.apache.lucene.document.Field$TermVector WITH_POSITIONS_OFFSETS
fld public final static org.apache.lucene.document.Field$TermVector YES
meth public abstract boolean isStored()
meth public abstract boolean withOffsets()
meth public abstract boolean withPositions()
meth public static org.apache.lucene.document.Field$TermVector toTermVector(boolean,boolean,boolean)
meth public static org.apache.lucene.document.Field$TermVector valueOf(java.lang.String)
meth public static org.apache.lucene.document.Field$TermVector[] values()
supr java.lang.Enum<org.apache.lucene.document.Field$TermVector>

CLSS public org.apache.lucene.document.FieldType
cons public init()
cons public init(org.apache.lucene.document.FieldType)
innr public final static !enum NumericType
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
meth public final java.lang.String toString()
meth public int hashCode()
meth public int numericPrecisionStep()
meth public org.apache.lucene.document.FieldType$NumericType numericType()
meth public org.apache.lucene.index.DocValuesType docValuesType()
meth public org.apache.lucene.index.IndexOptions indexOptions()
meth public void freeze()
meth public void setDocValuesType(org.apache.lucene.index.DocValuesType)
meth public void setIndexOptions(org.apache.lucene.index.IndexOptions)
meth public void setNumericPrecisionStep(int)
meth public void setNumericType(org.apache.lucene.document.FieldType$NumericType)
meth public void setOmitNorms(boolean)
meth public void setStoreTermVectorOffsets(boolean)
meth public void setStoreTermVectorPayloads(boolean)
meth public void setStoreTermVectorPositions(boolean)
meth public void setStoreTermVectors(boolean)
meth public void setStored(boolean)
meth public void setTokenized(boolean)
supr java.lang.Object
hfds docValuesType,frozen,indexOptions,numericPrecisionStep,numericType,omitNorms,storeTermVectorOffsets,storeTermVectorPayloads,storeTermVectorPositions,storeTermVectors,stored,tokenized

CLSS public final static !enum org.apache.lucene.document.FieldType$NumericType
 outer org.apache.lucene.document.FieldType
fld public final static org.apache.lucene.document.FieldType$NumericType DOUBLE
fld public final static org.apache.lucene.document.FieldType$NumericType FLOAT
fld public final static org.apache.lucene.document.FieldType$NumericType INT
fld public final static org.apache.lucene.document.FieldType$NumericType LONG
meth public static org.apache.lucene.document.FieldType$NumericType valueOf(java.lang.String)
meth public static org.apache.lucene.document.FieldType$NumericType[] values()
supr java.lang.Enum<org.apache.lucene.document.FieldType$NumericType>

CLSS public org.apache.lucene.document.FloatDocValuesField
cons public init(java.lang.String,float)
meth public void setFloatValue(float)
meth public void setLongValue(long)
supr org.apache.lucene.document.NumericDocValuesField

CLSS public final org.apache.lucene.document.FloatField
cons public init(java.lang.String,float,org.apache.lucene.document.Field$Store)
cons public init(java.lang.String,float,org.apache.lucene.document.FieldType)
fld public final static org.apache.lucene.document.FieldType TYPE_NOT_STORED
fld public final static org.apache.lucene.document.FieldType TYPE_STORED
supr org.apache.lucene.document.Field

CLSS public final org.apache.lucene.document.IntField
cons public init(java.lang.String,int,org.apache.lucene.document.Field$Store)
cons public init(java.lang.String,int,org.apache.lucene.document.FieldType)
fld public final static org.apache.lucene.document.FieldType TYPE_NOT_STORED
fld public final static org.apache.lucene.document.FieldType TYPE_STORED
supr org.apache.lucene.document.Field

CLSS public final org.apache.lucene.document.LongField
cons public init(java.lang.String,long,org.apache.lucene.document.Field$Store)
cons public init(java.lang.String,long,org.apache.lucene.document.FieldType)
fld public final static org.apache.lucene.document.FieldType TYPE_NOT_STORED
fld public final static org.apache.lucene.document.FieldType TYPE_STORED
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.NumericDocValuesField
cons public init(java.lang.String,long)
fld public final static org.apache.lucene.document.FieldType TYPE
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.SortedDocValuesField
cons public init(java.lang.String,org.apache.lucene.util.BytesRef)
fld public final static org.apache.lucene.document.FieldType TYPE
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.SortedNumericDocValuesField
cons public init(java.lang.String,long)
fld public final static org.apache.lucene.document.FieldType TYPE
supr org.apache.lucene.document.Field

CLSS public org.apache.lucene.document.SortedSetDocValuesField
cons public init(java.lang.String,org.apache.lucene.util.BytesRef)
fld public final static org.apache.lucene.document.FieldType TYPE
supr org.apache.lucene.document.Field

CLSS public final org.apache.lucene.document.StoredField
cons public init(java.lang.String,byte[])
cons public init(java.lang.String,byte[],int,int)
cons public init(java.lang.String,double)
cons public init(java.lang.String,float)
cons public init(java.lang.String,int)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,long)
cons public init(java.lang.String,org.apache.lucene.util.BytesRef)
fld public final static org.apache.lucene.document.FieldType TYPE
supr org.apache.lucene.document.Field

CLSS public final org.apache.lucene.document.StringField
cons public init(java.lang.String,java.lang.String,org.apache.lucene.document.Field$Store)
cons public init(java.lang.String,org.apache.lucene.util.BytesRef,org.apache.lucene.document.Field$Store)
fld public final static org.apache.lucene.document.FieldType TYPE_NOT_STORED
fld public final static org.apache.lucene.document.FieldType TYPE_STORED
supr org.apache.lucene.document.Field

CLSS public final org.apache.lucene.document.TextField
cons public init(java.lang.String,java.io.Reader)
cons public init(java.lang.String,java.lang.String,org.apache.lucene.document.Field$Store)
cons public init(java.lang.String,org.apache.lucene.analysis.TokenStream)
fld public final static org.apache.lucene.document.FieldType TYPE_NOT_STORED
fld public final static org.apache.lucene.document.FieldType TYPE_STORED
supr org.apache.lucene.document.Field

CLSS abstract interface org.apache.lucene.document.package-info

CLSS public org.apache.lucene.index.AutomatonTermsEnum
cons public init(org.apache.lucene.index.TermsEnum,org.apache.lucene.util.automaton.CompiledAutomaton)
meth protected org.apache.lucene.index.FilteredTermsEnum$AcceptStatus accept(org.apache.lucene.util.BytesRef)
meth protected org.apache.lucene.util.BytesRef nextSeekTerm(org.apache.lucene.util.BytesRef) throws java.io.IOException
supr org.apache.lucene.index.FilteredTermsEnum
hfds automaton,commonSuffixRef,curGen,finite,linear,linearUpperBound,runAutomaton,savedStates,seekBytesRef,transition,visited

CLSS public abstract org.apache.lucene.index.BaseCompositeReader<%0 extends org.apache.lucene.index.IndexReader>
cons protected init({org.apache.lucene.index.BaseCompositeReader%0}[]) throws java.io.IOException
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
meth public final void document(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
supr org.apache.lucene.index.CompositeReader
hfds maxDoc,numDocs,starts,subReaders,subReadersList

CLSS public abstract org.apache.lucene.index.BinaryDocValues
cons protected init()
meth public abstract org.apache.lucene.util.BytesRef get(int)
supr java.lang.Object

CLSS public final org.apache.lucene.index.CheckIndex
cons public init(org.apache.lucene.store.Directory) throws java.io.IOException
cons public init(org.apache.lucene.store.Directory,org.apache.lucene.store.Lock) throws java.io.IOException
innr public static Options
innr public static Status
intf java.io.Closeable
meth public boolean getChecksumsOnly()
meth public boolean getCrossCheckTermVectors()
meth public boolean getFailFast()
meth public int doCheck(org.apache.lucene.index.CheckIndex$Options) throws java.io.IOException,java.lang.InterruptedException
meth public org.apache.lucene.index.CheckIndex$Status checkIndex() throws java.io.IOException
meth public org.apache.lucene.index.CheckIndex$Status checkIndex(java.util.List<java.lang.String>) throws java.io.IOException
meth public static boolean assertsOn()
meth public static org.apache.lucene.index.CheckIndex$Options parseOptions(java.lang.String[])
meth public static org.apache.lucene.index.CheckIndex$Status$DocValuesStatus testDocValues(org.apache.lucene.index.CodecReader,java.io.PrintStream,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$FieldInfoStatus testFieldInfos(org.apache.lucene.index.CodecReader,java.io.PrintStream,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$FieldNormStatus testFieldNorms(org.apache.lucene.index.CodecReader,java.io.PrintStream,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$LiveDocStatus testLiveDocs(org.apache.lucene.index.CodecReader,java.io.PrintStream,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$StoredFieldStatus testStoredFields(org.apache.lucene.index.CodecReader,java.io.PrintStream,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$TermIndexStatus testPostings(org.apache.lucene.index.CodecReader,java.io.PrintStream) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$TermIndexStatus testPostings(org.apache.lucene.index.CodecReader,java.io.PrintStream,boolean,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$TermVectorStatus testTermVectors(org.apache.lucene.index.CodecReader,java.io.PrintStream) throws java.io.IOException
meth public static org.apache.lucene.index.CheckIndex$Status$TermVectorStatus testTermVectors(org.apache.lucene.index.CodecReader,java.io.PrintStream,boolean,boolean,boolean) throws java.io.IOException
meth public static void main(java.lang.String[]) throws java.io.IOException,java.lang.InterruptedException
meth public void close() throws java.io.IOException
meth public void exorciseIndex(org.apache.lucene.index.CheckIndex$Status) throws java.io.IOException
meth public void setChecksumsOnly(boolean)
meth public void setCrossCheckTermVectors(boolean)
meth public void setFailFast(boolean)
meth public void setInfoStream(java.io.PrintStream)
meth public void setInfoStream(java.io.PrintStream,boolean)
supr java.lang.Object
hfds assertsOn,checksumsOnly,closed,crossCheckTermVectors,dir,failFast,infoStream,verbose,writeLock

CLSS public static org.apache.lucene.index.CheckIndex$Options
 outer org.apache.lucene.index.CheckIndex
cons public init()
meth public java.lang.String getDirImpl()
meth public java.lang.String getIndexPath()
meth public void setOut(java.io.PrintStream)
supr java.lang.Object
hfds dirImpl,doChecksumsOnly,doCrossCheckTermVectors,doExorcise,indexPath,onlySegments,out,verbose

CLSS public static org.apache.lucene.index.CheckIndex$Status
 outer org.apache.lucene.index.CheckIndex
fld public boolean cantOpenSegments
fld public boolean clean
fld public boolean missingSegmentVersion
fld public boolean missingSegments
fld public boolean partial
fld public boolean toolOutOfDate
fld public boolean validCounter
fld public int maxSegmentName
fld public int numBadSegments
fld public int numSegments
fld public int totLoseDocCount
fld public java.lang.String segmentsFileName
fld public java.util.List<java.lang.String> segmentsChecked
fld public java.util.List<org.apache.lucene.index.CheckIndex$Status$SegmentInfoStatus> segmentInfos
fld public java.util.Map<java.lang.String,java.lang.String> userData
fld public org.apache.lucene.store.Directory dir
innr public final static DocValuesStatus
innr public final static FieldInfoStatus
innr public final static FieldNormStatus
innr public final static LiveDocStatus
innr public final static StoredFieldStatus
innr public final static TermIndexStatus
innr public final static TermVectorStatus
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

CLSS public final static org.apache.lucene.index.CheckIndex$Status$LiveDocStatus
 outer org.apache.lucene.index.CheckIndex$Status
fld public int numDeleted
fld public java.lang.Throwable error
supr java.lang.Object

CLSS public static org.apache.lucene.index.CheckIndex$Status$SegmentInfoStatus
 outer org.apache.lucene.index.CheckIndex$Status
fld public boolean compound
fld public boolean hasDeletions
fld public boolean openReaderPassed
fld public double sizeMB
fld public int maxDoc
fld public int numFiles
fld public java.lang.String name
fld public java.util.Map<java.lang.String,java.lang.String> diagnostics
fld public long deletionsGen
fld public org.apache.lucene.codecs.Codec codec
fld public org.apache.lucene.index.CheckIndex$Status$DocValuesStatus docValuesStatus
fld public org.apache.lucene.index.CheckIndex$Status$FieldInfoStatus fieldInfoStatus
fld public org.apache.lucene.index.CheckIndex$Status$FieldNormStatus fieldNormStatus
fld public org.apache.lucene.index.CheckIndex$Status$LiveDocStatus liveDocStatus
fld public org.apache.lucene.index.CheckIndex$Status$StoredFieldStatus storedFieldStatus
fld public org.apache.lucene.index.CheckIndex$Status$TermIndexStatus termIndexStatus
fld public org.apache.lucene.index.CheckIndex$Status$TermVectorStatus termVectorStatus
fld public org.apache.lucene.util.Version version
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

CLSS public abstract org.apache.lucene.index.CodecReader
cons protected init()
intf org.apache.lucene.util.Accountable
meth protected void doClose() throws java.io.IOException
meth public abstract org.apache.lucene.codecs.DocValuesProducer getDocValuesReader()
meth public abstract org.apache.lucene.codecs.FieldsProducer getPostingsReader()
meth public abstract org.apache.lucene.codecs.NormsProducer getNormsReader()
meth public abstract org.apache.lucene.codecs.StoredFieldsReader getFieldsReader()
meth public abstract org.apache.lucene.codecs.TermVectorsReader getTermVectorsReader()
meth public final org.apache.lucene.index.BinaryDocValues getBinaryDocValues(java.lang.String) throws java.io.IOException
meth public final org.apache.lucene.index.Fields fields()
meth public final org.apache.lucene.index.Fields getTermVectors(int) throws java.io.IOException
meth public final org.apache.lucene.index.NumericDocValues getNormValues(java.lang.String) throws java.io.IOException
meth public final org.apache.lucene.index.NumericDocValues getNumericDocValues(java.lang.String) throws java.io.IOException
meth public final org.apache.lucene.index.SortedDocValues getSortedDocValues(java.lang.String) throws java.io.IOException
meth public final org.apache.lucene.index.SortedNumericDocValues getSortedNumericDocValues(java.lang.String) throws java.io.IOException
meth public final org.apache.lucene.index.SortedSetDocValues getSortedSetDocValues(java.lang.String) throws java.io.IOException
meth public final org.apache.lucene.util.Bits getDocsWithField(java.lang.String) throws java.io.IOException
meth public final void document(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public long ramBytesUsed()
meth public void checkIntegrity() throws java.io.IOException
supr org.apache.lucene.index.LeafReader
hfds docValuesLocal,docsWithFieldLocal,normsLocal

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
fld public final static java.lang.String DEFAULT_SPINS_PROPERTY = "lucene.cms.override_spins"
innr protected MergeThread
meth protected boolean maybeStall(org.apache.lucene.index.IndexWriter)
meth protected org.apache.lucene.index.ConcurrentMergeScheduler$MergeThread getMergeThread(org.apache.lucene.index.IndexWriter,org.apache.lucene.index.MergePolicy$OneMerge) throws java.io.IOException
meth protected void doMerge(org.apache.lucene.index.IndexWriter,org.apache.lucene.index.MergePolicy$OneMerge) throws java.io.IOException
meth protected void doStall()
meth protected void handleMergeException(org.apache.lucene.store.Directory,java.lang.Throwable)
meth protected void targetMBPerSecChanged()
meth protected void updateMergeThreads()
meth public boolean getAutoIOThrottle()
meth public double getForceMergeMBPerSec()
meth public double getIORateLimitMBPerSec()
meth public int getMaxMergeCount()
meth public int getMaxThreadCount()
meth public int mergeThreadCount()
meth public java.lang.String toString()
meth public void close()
meth public void disableAutoIOThrottle()
meth public void enableAutoIOThrottle()
meth public void merge(org.apache.lucene.index.IndexWriter,org.apache.lucene.index.MergeTrigger,boolean) throws java.io.IOException
meth public void setDefaultMaxMergesAndThreads(boolean)
meth public void setForceMergeMBPerSec(double)
meth public void setMaxMergesAndThreads(int,int)
meth public void sync()
supr org.apache.lucene.index.MergeScheduler
hfds MAX_MERGE_MB_PER_SEC,MIN_BIG_MERGE_MB,MIN_MERGE_MB_PER_SEC,START_MB_PER_SEC,doAutoIOThrottle,forceMergeMBPerSec,maxMergeCount,maxThreadCount,suppressExceptions

CLSS protected org.apache.lucene.index.ConcurrentMergeScheduler$MergeThread
 outer org.apache.lucene.index.ConcurrentMergeScheduler
cons public init(org.apache.lucene.index.ConcurrentMergeScheduler,org.apache.lucene.index.IndexWriter,org.apache.lucene.index.MergePolicy$OneMerge)
intf java.lang.Comparable<org.apache.lucene.index.ConcurrentMergeScheduler$MergeThread>
meth public int compareTo(org.apache.lucene.index.ConcurrentMergeScheduler$MergeThread)
meth public void run()
supr java.lang.Thread
hfds merge,writer

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
cons protected init(org.apache.lucene.store.Directory,org.apache.lucene.index.LeafReader[]) throws java.io.IOException
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
meth public static org.apache.lucene.index.DirectoryReader open(org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader open(org.apache.lucene.index.IndexWriter,boolean) throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader open(org.apache.lucene.store.Directory) throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader openIfChanged(org.apache.lucene.index.DirectoryReader) throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader openIfChanged(org.apache.lucene.index.DirectoryReader,org.apache.lucene.index.IndexCommit) throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader openIfChanged(org.apache.lucene.index.DirectoryReader,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public static org.apache.lucene.index.DirectoryReader openIfChanged(org.apache.lucene.index.DirectoryReader,org.apache.lucene.index.IndexWriter,boolean) throws java.io.IOException
supr org.apache.lucene.index.BaseCompositeReader<org.apache.lucene.index.LeafReader>

CLSS public final org.apache.lucene.index.DocValues
meth public final static org.apache.lucene.index.BinaryDocValues emptyBinary()
meth public final static org.apache.lucene.index.NumericDocValues emptyNumeric()
meth public final static org.apache.lucene.index.RandomAccessOrds emptySortedSet()
meth public final static org.apache.lucene.index.SortedDocValues emptySorted()
meth public final static org.apache.lucene.index.SortedNumericDocValues emptySortedNumeric(int)
meth public static org.apache.lucene.index.BinaryDocValues getBinary(org.apache.lucene.index.LeafReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.NumericDocValues getNumeric(org.apache.lucene.index.LeafReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.NumericDocValues unwrapSingleton(org.apache.lucene.index.SortedNumericDocValues)
meth public static org.apache.lucene.index.RandomAccessOrds singleton(org.apache.lucene.index.SortedDocValues)
meth public static org.apache.lucene.index.SortedDocValues getSorted(org.apache.lucene.index.LeafReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.SortedDocValues unwrapSingleton(org.apache.lucene.index.SortedSetDocValues)
meth public static org.apache.lucene.index.SortedNumericDocValues getSortedNumeric(org.apache.lucene.index.LeafReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.SortedNumericDocValues singleton(org.apache.lucene.index.NumericDocValues,org.apache.lucene.util.Bits)
meth public static org.apache.lucene.index.SortedSetDocValues getSortedSet(org.apache.lucene.index.LeafReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.util.Bits docsWithValue(org.apache.lucene.index.SortedDocValues,int)
meth public static org.apache.lucene.util.Bits docsWithValue(org.apache.lucene.index.SortedNumericDocValues,int)
meth public static org.apache.lucene.util.Bits docsWithValue(org.apache.lucene.index.SortedSetDocValues,int)
meth public static org.apache.lucene.util.Bits getDocsWithField(org.apache.lucene.index.LeafReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.util.Bits unwrapSingletonBits(org.apache.lucene.index.SortedNumericDocValues)
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

CLSS public abstract org.apache.lucene.index.DocsAndPositionsEnum
 anno 0 java.lang.Deprecated()
cons protected init()
fld public final static int FLAG_OFFSETS = 1
fld public final static int FLAG_PAYLOADS = 2
fld public final static short OLD_NULL_SEMANTICS = 16384
 anno 0 java.lang.Deprecated()
meth public abstract int endOffset() throws java.io.IOException
meth public abstract int nextPosition() throws java.io.IOException
meth public abstract int startOffset() throws java.io.IOException
meth public abstract org.apache.lucene.util.BytesRef getPayload() throws java.io.IOException
supr org.apache.lucene.index.DocsEnum
hcls DocsAndPositionsEnumWrapper

CLSS public abstract org.apache.lucene.index.DocsEnum
 anno 0 java.lang.Deprecated()
cons protected init()
fld public final static int FLAG_FREQS = 1
fld public final static int FLAG_NONE = 0
meth public int endOffset() throws java.io.IOException
meth public int nextPosition() throws java.io.IOException
meth public int startOffset() throws java.io.IOException
meth public org.apache.lucene.util.BytesRef getPayload() throws java.io.IOException
supr org.apache.lucene.index.PostingsEnum

CLSS public org.apache.lucene.index.ExitableDirectoryReader
cons public init(org.apache.lucene.index.DirectoryReader,org.apache.lucene.index.QueryTimeout) throws java.io.IOException
innr public static ExitableFields
innr public static ExitableFilterAtomicReader
innr public static ExitableSubReaderWrapper
innr public static ExitableTerms
innr public static ExitableTermsEnum
innr public static ExitingReaderException
meth protected org.apache.lucene.index.DirectoryReader doWrapDirectoryReader(org.apache.lucene.index.DirectoryReader) throws java.io.IOException
meth public java.lang.String toString()
meth public static org.apache.lucene.index.DirectoryReader wrap(org.apache.lucene.index.DirectoryReader,org.apache.lucene.index.QueryTimeout) throws java.io.IOException
supr org.apache.lucene.index.FilterDirectoryReader
hfds queryTimeout

CLSS public static org.apache.lucene.index.ExitableDirectoryReader$ExitableFields
 outer org.apache.lucene.index.ExitableDirectoryReader
cons public init(org.apache.lucene.index.Fields,org.apache.lucene.index.QueryTimeout)
meth public org.apache.lucene.index.Terms terms(java.lang.String) throws java.io.IOException
supr org.apache.lucene.index.FilterLeafReader$FilterFields
hfds queryTimeout

CLSS public static org.apache.lucene.index.ExitableDirectoryReader$ExitableFilterAtomicReader
 outer org.apache.lucene.index.ExitableDirectoryReader
cons public init(org.apache.lucene.index.LeafReader,org.apache.lucene.index.QueryTimeout)
meth public java.lang.Object getCombinedCoreAndDeletesKey()
meth public java.lang.Object getCoreCacheKey()
meth public org.apache.lucene.index.Fields fields() throws java.io.IOException
supr org.apache.lucene.index.FilterLeafReader
hfds queryTimeout

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
supr org.apache.lucene.index.FilterLeafReader$FilterTerms
hfds queryTimeout

CLSS public static org.apache.lucene.index.ExitableDirectoryReader$ExitableTermsEnum
 outer org.apache.lucene.index.ExitableDirectoryReader
cons public init(org.apache.lucene.index.TermsEnum,org.apache.lucene.index.QueryTimeout)
meth public org.apache.lucene.util.BytesRef next() throws java.io.IOException
supr org.apache.lucene.index.FilterLeafReader$FilterTermsEnum
hfds queryTimeout

CLSS public static org.apache.lucene.index.ExitableDirectoryReader$ExitingReaderException
 outer org.apache.lucene.index.ExitableDirectoryReader
supr java.lang.RuntimeException

CLSS public final org.apache.lucene.index.FieldInfo
cons public init(java.lang.String,int,boolean,boolean,boolean,org.apache.lucene.index.IndexOptions,org.apache.lucene.index.DocValuesType,long,java.util.Map<java.lang.String,java.lang.String>)
fld public final int number
fld public final java.lang.String name
meth public boolean checkConsistency()
meth public boolean hasNorms()
meth public boolean hasPayloads()
meth public boolean hasVectors()
meth public boolean omitsNorms()
meth public java.lang.String getAttribute(java.lang.String)
meth public java.lang.String putAttribute(java.lang.String,java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.String> attributes()
meth public long getDocValuesGen()
meth public org.apache.lucene.index.DocValuesType getDocValuesType()
meth public org.apache.lucene.index.IndexOptions getIndexOptions()
meth public void setIndexOptions(org.apache.lucene.index.IndexOptions)
meth public void setOmitsNorms()
supr java.lang.Object
hfds attributes,docValuesType,dvGen,indexOptions,omitNorms,storePayloads,storeTermVector

CLSS public org.apache.lucene.index.FieldInfos
cons public init(org.apache.lucene.index.FieldInfo[])
intf java.lang.Iterable<org.apache.lucene.index.FieldInfo>
meth public boolean hasDocValues()
meth public boolean hasFreq()
meth public boolean hasNorms()
meth public boolean hasOffsets()
meth public boolean hasPayloads()
meth public boolean hasProx()
meth public boolean hasVectors()
meth public int size()
meth public java.util.Iterator<org.apache.lucene.index.FieldInfo> iterator()
meth public org.apache.lucene.index.FieldInfo fieldInfo(int)
meth public org.apache.lucene.index.FieldInfo fieldInfo(java.lang.String)
supr java.lang.Object
hfds byName,byNumberMap,byNumberTable,hasDocValues,hasFreq,hasNorms,hasOffsets,hasPayloads,hasProx,hasVectors,values
hcls Builder,FieldNumbers

CLSS public final org.apache.lucene.index.FieldInvertState
cons public init(java.lang.String)
cons public init(java.lang.String,int,int,int,int,float)
meth public float getBoost()
meth public int getLength()
meth public int getMaxTermFrequency()
meth public int getNumOverlap()
meth public int getOffset()
meth public int getPosition()
meth public int getUniqueTermCount()
meth public java.lang.String getName()
meth public org.apache.lucene.util.AttributeSource getAttributeSource()
meth public void setBoost(float)
meth public void setLength(int)
meth public void setNumOverlap(int)
supr java.lang.Object
hfds attributeSource,boost,lastPosition,lastStartOffset,length,maxTermFrequency,name,numOverlap,offset,offsetAttribute,payloadAttribute,posIncrAttribute,position,termAttribute,uniqueTermCount

CLSS public abstract org.apache.lucene.index.Fields
cons protected init()
fld public final static org.apache.lucene.index.Fields[] EMPTY_ARRAY
intf java.lang.Iterable<java.lang.String>
meth public abstract int size()
meth public abstract java.util.Iterator<java.lang.String> iterator()
meth public abstract org.apache.lucene.index.Terms terms(java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.lucene.index.FilterCodecReader
cons public init(org.apache.lucene.index.CodecReader)
fld protected final org.apache.lucene.index.CodecReader in
meth public int maxDoc()
meth public int numDocs()
meth public org.apache.lucene.codecs.DocValuesProducer getDocValuesReader()
meth public org.apache.lucene.codecs.FieldsProducer getPostingsReader()
meth public org.apache.lucene.codecs.NormsProducer getNormsReader()
meth public org.apache.lucene.codecs.StoredFieldsReader getFieldsReader()
meth public org.apache.lucene.codecs.TermVectorsReader getTermVectorsReader()
meth public org.apache.lucene.index.FieldInfos getFieldInfos()
meth public org.apache.lucene.util.Bits getLiveDocs()
meth public void addCoreClosedListener(org.apache.lucene.index.LeafReader$CoreClosedListener)
meth public void removeCoreClosedListener(org.apache.lucene.index.LeafReader$CoreClosedListener)
supr org.apache.lucene.index.CodecReader

CLSS public abstract org.apache.lucene.index.FilterDirectoryReader
cons public init(org.apache.lucene.index.DirectoryReader,org.apache.lucene.index.FilterDirectoryReader$SubReaderWrapper) throws java.io.IOException
fld protected final org.apache.lucene.index.DirectoryReader in
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

CLSS public abstract static org.apache.lucene.index.FilterDirectoryReader$SubReaderWrapper
 outer org.apache.lucene.index.FilterDirectoryReader
cons public init()
meth public abstract org.apache.lucene.index.LeafReader wrap(org.apache.lucene.index.LeafReader)
supr java.lang.Object

CLSS public org.apache.lucene.index.FilterLeafReader
cons public init(org.apache.lucene.index.LeafReader)
fld protected final org.apache.lucene.index.LeafReader in
innr public static FilterFields
innr public static FilterPostingsEnum
innr public static FilterTerms
innr public static FilterTermsEnum
meth protected void doClose() throws java.io.IOException
meth public int maxDoc()
meth public int numDocs()
meth public java.lang.String toString()
meth public org.apache.lucene.index.BinaryDocValues getBinaryDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.FieldInfos getFieldInfos()
meth public org.apache.lucene.index.Fields fields() throws java.io.IOException
meth public org.apache.lucene.index.Fields getTermVectors(int) throws java.io.IOException
meth public org.apache.lucene.index.LeafReader getDelegate()
meth public org.apache.lucene.index.NumericDocValues getNormValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.NumericDocValues getNumericDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedDocValues getSortedDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedNumericDocValues getSortedNumericDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedSetDocValues getSortedSetDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.util.Bits getDocsWithField(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.util.Bits getLiveDocs()
meth public static org.apache.lucene.index.LeafReader unwrap(org.apache.lucene.index.LeafReader)
meth public void addCoreClosedListener(org.apache.lucene.index.LeafReader$CoreClosedListener)
meth public void checkIntegrity() throws java.io.IOException
meth public void document(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
meth public void removeCoreClosedListener(org.apache.lucene.index.LeafReader$CoreClosedListener)
supr org.apache.lucene.index.LeafReader
hcls CoreClosedListenerWrapper

CLSS public static org.apache.lucene.index.FilterLeafReader$FilterFields
 outer org.apache.lucene.index.FilterLeafReader
cons public init(org.apache.lucene.index.Fields)
fld protected final org.apache.lucene.index.Fields in
meth public int size()
meth public java.util.Iterator<java.lang.String> iterator()
meth public org.apache.lucene.index.Terms terms(java.lang.String) throws java.io.IOException
supr org.apache.lucene.index.Fields

CLSS public static org.apache.lucene.index.FilterLeafReader$FilterPostingsEnum
 outer org.apache.lucene.index.FilterLeafReader
cons public init(org.apache.lucene.index.PostingsEnum)
fld protected final org.apache.lucene.index.PostingsEnum in
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int endOffset() throws java.io.IOException
meth public int freq() throws java.io.IOException
meth public int nextDoc() throws java.io.IOException
meth public int nextPosition() throws java.io.IOException
meth public int startOffset() throws java.io.IOException
meth public long cost()
meth public org.apache.lucene.util.AttributeSource attributes()
meth public org.apache.lucene.util.BytesRef getPayload() throws java.io.IOException
supr org.apache.lucene.index.PostingsEnum

CLSS public static org.apache.lucene.index.FilterLeafReader$FilterTerms
 outer org.apache.lucene.index.FilterLeafReader
cons public init(org.apache.lucene.index.Terms)
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

CLSS public static org.apache.lucene.index.FilterLeafReader$FilterTermsEnum
 outer org.apache.lucene.index.FilterLeafReader
cons public init(org.apache.lucene.index.TermsEnum)
fld protected final org.apache.lucene.index.TermsEnum in
meth public int docFreq() throws java.io.IOException
meth public long ord() throws java.io.IOException
meth public long totalTermFreq() throws java.io.IOException
meth public org.apache.lucene.index.PostingsEnum postings(org.apache.lucene.index.PostingsEnum,int) throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum$SeekStatus seekCeil(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public org.apache.lucene.util.AttributeSource attributes()
meth public org.apache.lucene.util.BytesRef next() throws java.io.IOException
meth public org.apache.lucene.util.BytesRef term() throws java.io.IOException
meth public void seekExact(long) throws java.io.IOException
supr org.apache.lucene.index.TermsEnum

CLSS public abstract org.apache.lucene.index.FilteredTermsEnum
cons public init(org.apache.lucene.index.TermsEnum)
cons public init(org.apache.lucene.index.TermsEnum,boolean)
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
fld public final static java.lang.String OLD_SEGMENTS_GEN = "segments.gen"
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
innr public abstract interface static ReaderClosedListener
intf java.io.Closeable
meth protected abstract void doClose() throws java.io.IOException
meth protected final void ensureOpen()
meth public abstract int docFreq(org.apache.lucene.index.Term) throws java.io.IOException
meth public abstract int getDocCount(java.lang.String) throws java.io.IOException
meth public abstract int maxDoc()
meth public abstract int numDocs()
meth public abstract long getSumDocFreq(java.lang.String) throws java.io.IOException
meth public abstract long getSumTotalTermFreq(java.lang.String) throws java.io.IOException
meth public abstract long totalTermFreq(org.apache.lucene.index.Term) throws java.io.IOException
meth public abstract org.apache.lucene.index.Fields getTermVectors(int) throws java.io.IOException
meth public abstract org.apache.lucene.index.IndexReaderContext getContext()
meth public abstract void document(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
meth public boolean hasDeletions()
meth public final boolean equals(java.lang.Object)
meth public final boolean tryIncRef()
meth public final int getRefCount()
meth public final int hashCode()
meth public final int numDeletedDocs()
meth public final java.util.List<org.apache.lucene.index.LeafReaderContext> leaves()
meth public final org.apache.lucene.document.Document document(int) throws java.io.IOException
meth public final org.apache.lucene.document.Document document(int,java.util.Set<java.lang.String>) throws java.io.IOException
meth public final org.apache.lucene.index.Terms getTermVector(int,java.lang.String) throws java.io.IOException
meth public final void addReaderClosedListener(org.apache.lucene.index.IndexReader$ReaderClosedListener)
meth public final void close() throws java.io.IOException
meth public final void decRef() throws java.io.IOException
meth public final void incRef()
meth public final void registerParentReader(org.apache.lucene.index.IndexReader)
meth public final void removeReaderClosedListener(org.apache.lucene.index.IndexReader$ReaderClosedListener)
meth public java.lang.Object getCombinedCoreAndDeletesKey()
meth public java.lang.Object getCoreCacheKey()
supr java.lang.Object
hfds closed,closedByChild,parentReaders,readerClosedListeners,refCount

CLSS public abstract interface static org.apache.lucene.index.IndexReader$ReaderClosedListener
 outer org.apache.lucene.index.IndexReader
meth public abstract void onClose(org.apache.lucene.index.IndexReader) throws java.io.IOException

CLSS public abstract org.apache.lucene.index.IndexReaderContext
fld public final boolean isTopLevel
fld public final int docBaseInParent
fld public final int ordInParent
fld public final org.apache.lucene.index.CompositeReaderContext parent
meth public abstract java.util.List<org.apache.lucene.index.IndexReaderContext> children()
meth public abstract java.util.List<org.apache.lucene.index.LeafReaderContext> leaves()
meth public abstract org.apache.lucene.index.IndexReader reader()
supr java.lang.Object
hfds identity

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
fld public final static int MAX_TERM_LENGTH = 32766
fld public final static java.lang.String SOURCE = "source"
fld public final static java.lang.String SOURCE_ADDINDEXES_READERS = "addIndexes(CodecReader...)"
fld public final static java.lang.String SOURCE_FLUSH = "flush"
fld public final static java.lang.String SOURCE_MERGE = "merge"
fld public final static java.lang.String WRITE_LOCK_NAME = "write.lock"
innr public abstract static IndexReaderWarmer
intf java.io.Closeable
intf org.apache.lucene.index.TwoPhaseCommit
intf org.apache.lucene.util.Accountable
meth protected final void ensureOpen()
meth protected final void ensureOpen(boolean)
meth protected void doAfterFlush() throws java.io.IOException
meth protected void doBeforeFlush() throws java.io.IOException
meth public !varargs void addIndexes(org.apache.lucene.index.CodecReader[]) throws java.io.IOException
meth public !varargs void addIndexes(org.apache.lucene.store.Directory[]) throws java.io.IOException
meth public !varargs void deleteDocuments(org.apache.lucene.index.Term[]) throws java.io.IOException
meth public !varargs void deleteDocuments(org.apache.lucene.search.Query[]) throws java.io.IOException
meth public !varargs void updateDocValues(org.apache.lucene.index.Term,org.apache.lucene.document.Field[]) throws java.io.IOException
meth public boolean hasDeletions()
meth public boolean hasPendingMerges()
meth public boolean isOpen()
meth public boolean tryDeleteDocument(org.apache.lucene.index.IndexReader,int) throws java.io.IOException
meth public final boolean hasUncommittedChanges()
meth public final int numRamDocs()
meth public final java.util.Map<java.lang.String,java.lang.String> getCommitData()
meth public final long ramBytesUsed()
meth public final void commit() throws java.io.IOException
meth public final void flush() throws java.io.IOException
meth public final void maybeMerge() throws java.io.IOException
meth public final void prepareCommit() throws java.io.IOException
meth public final void setCommitData(java.util.Map<java.lang.String,java.lang.String>)
meth public int maxDoc()
meth public int numDeletedDocs(org.apache.lucene.index.SegmentCommitInfo)
meth public int numDocs()
meth public java.lang.Throwable getTragicException()
meth public java.util.Collection<org.apache.lucene.index.SegmentCommitInfo> getMergingSegments()
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public org.apache.lucene.analysis.Analyzer getAnalyzer()
meth public org.apache.lucene.index.LiveIndexWriterConfig getConfig()
meth public org.apache.lucene.index.MergePolicy$OneMerge getNextMerge()
meth public org.apache.lucene.store.Directory getDirectory()
meth public static boolean isLocked(org.apache.lucene.store.Directory) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public void addDocument(java.lang.Iterable<? extends org.apache.lucene.index.IndexableField>) throws java.io.IOException
meth public void addDocuments(java.lang.Iterable<? extends java.lang.Iterable<? extends org.apache.lucene.index.IndexableField>>) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void deleteAll() throws java.io.IOException
meth public void deleteUnusedFiles() throws java.io.IOException
meth public void forceMerge(int) throws java.io.IOException
meth public void forceMerge(int,boolean) throws java.io.IOException
meth public void forceMergeDeletes() throws java.io.IOException
meth public void forceMergeDeletes(boolean) throws java.io.IOException
meth public void merge(org.apache.lucene.index.MergePolicy$OneMerge) throws java.io.IOException
meth public void rollback() throws java.io.IOException
meth public void updateBinaryDocValue(org.apache.lucene.index.Term,java.lang.String,org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public void updateDocument(org.apache.lucene.index.Term,java.lang.Iterable<? extends org.apache.lucene.index.IndexableField>) throws java.io.IOException
meth public void updateDocuments(org.apache.lucene.index.Term,java.lang.Iterable<? extends java.lang.Iterable<? extends org.apache.lucene.index.IndexableField>>) throws java.io.IOException
meth public void updateNumericDocValue(org.apache.lucene.index.Term,java.lang.String,long) throws java.io.IOException
supr java.lang.Object
hfds UNBOUNDED_MAX_MERGE_SEGMENTS,actualMaxDocs,analyzer,bufferedUpdatesStream,changeCount,closed,closing,codec,commitLock,config,deleter,didMessageState,directory,directoryOrig,docWriter,enableTestPoints,eventQueue,filesToCommit,flushCount,flushDeletesCount,fullFlushLock,globalFieldNumberMap,infoStream,keepFullyDeletedSegments,lastCommitChangeCount,mergeDirectory,mergeExceptions,mergeGen,mergeMaxNumSegments,mergeScheduler,mergingSegments,pendingCommit,pendingCommitChangeCount,pendingMerges,pendingNumDocs,poolReaders,rateLimiters,readerPool,rollbackSegments,runningMerges,segmentInfos,segmentsToMerge,startCommitTime,stopMerges,tragedy,writeLock
hcls Event,MergedDeletesAndUpdates,ReaderPool

CLSS public abstract static org.apache.lucene.index.IndexWriter$IndexReaderWarmer
 outer org.apache.lucene.index.IndexWriter
cons protected init()
meth public abstract void warm(org.apache.lucene.index.LeafReader) throws java.io.IOException
supr java.lang.Object

CLSS public final org.apache.lucene.index.IndexWriterConfig
cons public init(org.apache.lucene.analysis.Analyzer)
fld public final static boolean DEFAULT_COMMIT_ON_CLOSE = true
fld public final static boolean DEFAULT_READER_POOLING = false
fld public final static boolean DEFAULT_USE_COMPOUND_FILE_SYSTEM = true
fld public final static double DEFAULT_RAM_BUFFER_SIZE_MB = 16.0
fld public final static int DEFAULT_MAX_BUFFERED_DELETE_TERMS = -1
fld public final static int DEFAULT_MAX_BUFFERED_DOCS = -1
fld public final static int DEFAULT_RAM_PER_THREAD_HARD_LIMIT_MB = 1945
fld public final static int DISABLE_AUTO_FLUSH = -1
fld public final static long WRITE_LOCK_TIMEOUT = 0
 anno 0 java.lang.Deprecated()
innr public final static !enum OpenMode
meth public boolean getReaderPooling()
meth public double getRAMBufferSizeMB()
meth public int getMaxBufferedDeleteTerms()
meth public int getMaxBufferedDocs()
meth public int getRAMPerThreadHardLimitMB()
meth public java.lang.String toString()
meth public long getWriteLockTimeout()
meth public org.apache.lucene.analysis.Analyzer getAnalyzer()
meth public org.apache.lucene.codecs.Codec getCodec()
meth public org.apache.lucene.index.IndexCommit getIndexCommit()
meth public org.apache.lucene.index.IndexDeletionPolicy getIndexDeletionPolicy()
meth public org.apache.lucene.index.IndexWriter$IndexReaderWarmer getMergedSegmentWarmer()
meth public org.apache.lucene.index.IndexWriterConfig setCodec(org.apache.lucene.codecs.Codec)
meth public org.apache.lucene.index.IndexWriterConfig setCommitOnClose(boolean)
meth public org.apache.lucene.index.IndexWriterConfig setIndexCommit(org.apache.lucene.index.IndexCommit)
meth public org.apache.lucene.index.IndexWriterConfig setIndexDeletionPolicy(org.apache.lucene.index.IndexDeletionPolicy)
meth public org.apache.lucene.index.IndexWriterConfig setInfoStream(java.io.PrintStream)
meth public org.apache.lucene.index.IndexWriterConfig setInfoStream(org.apache.lucene.util.InfoStream)
meth public org.apache.lucene.index.IndexWriterConfig setMaxBufferedDeleteTerms(int)
meth public org.apache.lucene.index.IndexWriterConfig setMaxBufferedDocs(int)
meth public org.apache.lucene.index.IndexWriterConfig setMergePolicy(org.apache.lucene.index.MergePolicy)
meth public org.apache.lucene.index.IndexWriterConfig setMergeScheduler(org.apache.lucene.index.MergeScheduler)
meth public org.apache.lucene.index.IndexWriterConfig setMergedSegmentWarmer(org.apache.lucene.index.IndexWriter$IndexReaderWarmer)
meth public org.apache.lucene.index.IndexWriterConfig setOpenMode(org.apache.lucene.index.IndexWriterConfig$OpenMode)
meth public org.apache.lucene.index.IndexWriterConfig setRAMBufferSizeMB(double)
meth public org.apache.lucene.index.IndexWriterConfig setRAMPerThreadHardLimitMB(int)
meth public org.apache.lucene.index.IndexWriterConfig setReaderPooling(boolean)
meth public org.apache.lucene.index.IndexWriterConfig setSimilarity(org.apache.lucene.search.similarities.Similarity)
meth public org.apache.lucene.index.IndexWriterConfig setUseCompoundFile(boolean)
meth public org.apache.lucene.index.IndexWriterConfig setWriteLockTimeout(long)
 anno 0 java.lang.Deprecated()
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

CLSS public abstract interface org.apache.lucene.index.IndexableField
meth public abstract float boost()
meth public abstract java.io.Reader readerValue()
meth public abstract java.lang.Number numericValue()
meth public abstract java.lang.String name()
meth public abstract java.lang.String stringValue()
meth public abstract org.apache.lucene.analysis.TokenStream tokenStream(org.apache.lucene.analysis.Analyzer,org.apache.lucene.analysis.TokenStream)
meth public abstract org.apache.lucene.index.IndexableFieldType fieldType()
meth public abstract org.apache.lucene.util.BytesRef binaryValue()

CLSS public abstract interface org.apache.lucene.index.IndexableFieldType
meth public abstract boolean omitNorms()
meth public abstract boolean storeTermVectorOffsets()
meth public abstract boolean storeTermVectorPayloads()
meth public abstract boolean storeTermVectorPositions()
meth public abstract boolean storeTermVectors()
meth public abstract boolean stored()
meth public abstract boolean tokenized()
meth public abstract org.apache.lucene.index.DocValuesType docValuesType()
meth public abstract org.apache.lucene.index.IndexOptions indexOptions()

CLSS public final org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy
cons public init()
meth public void onCommit(java.util.List<? extends org.apache.lucene.index.IndexCommit>)
meth public void onInit(java.util.List<? extends org.apache.lucene.index.IndexCommit>)
supr org.apache.lucene.index.IndexDeletionPolicy

CLSS public abstract org.apache.lucene.index.LeafReader
cons protected init()
innr public abstract interface static CoreClosedListener
meth protected static void addCoreClosedListenerAsReaderClosedListener(org.apache.lucene.index.IndexReader,org.apache.lucene.index.LeafReader$CoreClosedListener)
meth protected static void removeCoreClosedListenerAsReaderClosedListener(org.apache.lucene.index.IndexReader,org.apache.lucene.index.LeafReader$CoreClosedListener)
meth public abstract org.apache.lucene.index.BinaryDocValues getBinaryDocValues(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.index.FieldInfos getFieldInfos()
meth public abstract org.apache.lucene.index.Fields fields() throws java.io.IOException
meth public abstract org.apache.lucene.index.NumericDocValues getNormValues(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.index.NumericDocValues getNumericDocValues(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.index.SortedDocValues getSortedDocValues(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.index.SortedNumericDocValues getSortedNumericDocValues(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.index.SortedSetDocValues getSortedSetDocValues(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.util.Bits getDocsWithField(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.util.Bits getLiveDocs()
meth public abstract void addCoreClosedListener(org.apache.lucene.index.LeafReader$CoreClosedListener)
meth public abstract void checkIntegrity() throws java.io.IOException
meth public abstract void removeCoreClosedListener(org.apache.lucene.index.LeafReader$CoreClosedListener)
meth public final int docFreq(org.apache.lucene.index.Term) throws java.io.IOException
meth public final int getDocCount(java.lang.String) throws java.io.IOException
meth public final long getSumDocFreq(java.lang.String) throws java.io.IOException
meth public final long getSumTotalTermFreq(java.lang.String) throws java.io.IOException
meth public final long totalTermFreq(org.apache.lucene.index.Term) throws java.io.IOException
meth public final org.apache.lucene.index.DocsAndPositionsEnum termPositionsEnum(org.apache.lucene.index.Term) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public final org.apache.lucene.index.DocsEnum termDocsEnum(org.apache.lucene.index.Term) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public final org.apache.lucene.index.LeafReaderContext getContext()
meth public final org.apache.lucene.index.PostingsEnum postings(org.apache.lucene.index.Term) throws java.io.IOException
meth public final org.apache.lucene.index.PostingsEnum postings(org.apache.lucene.index.Term,int) throws java.io.IOException
meth public final org.apache.lucene.index.Terms terms(java.lang.String) throws java.io.IOException
supr org.apache.lucene.index.IndexReader
hfds readerContext
hcls CoreClosedListenerWrapper

CLSS public abstract interface static org.apache.lucene.index.LeafReader$CoreClosedListener
 outer org.apache.lucene.index.LeafReader
meth public abstract void onClose(java.lang.Object) throws java.io.IOException

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
fld protected volatile boolean readerPooling
fld protected volatile boolean useCompoundFile
fld protected volatile int perThreadHardLimitMB
fld protected volatile java.lang.Object flushPolicy
fld protected volatile java.lang.Object indexerThreadPool
fld protected volatile java.lang.Object indexingChain
fld protected volatile long writeLockTimeout
 anno 0 java.lang.Deprecated()
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
meth public double getRAMBufferSizeMB()
meth public int getMaxBufferedDeleteTerms()
meth public int getMaxBufferedDocs()
meth public int getRAMPerThreadHardLimitMB()
meth public java.lang.String toString()
meth public long getWriteLockTimeout()
 anno 0 java.lang.Deprecated()
meth public org.apache.lucene.analysis.Analyzer getAnalyzer()
meth public org.apache.lucene.codecs.Codec getCodec()
meth public org.apache.lucene.index.IndexCommit getIndexCommit()
meth public org.apache.lucene.index.IndexDeletionPolicy getIndexDeletionPolicy()
meth public org.apache.lucene.index.IndexWriter$IndexReaderWarmer getMergedSegmentWarmer()
meth public org.apache.lucene.index.IndexWriterConfig$OpenMode getOpenMode()
meth public org.apache.lucene.index.LiveIndexWriterConfig setMaxBufferedDeleteTerms(int)
meth public org.apache.lucene.index.LiveIndexWriterConfig setMaxBufferedDocs(int)
meth public org.apache.lucene.index.LiveIndexWriterConfig setMergePolicy(org.apache.lucene.index.MergePolicy)
meth public org.apache.lucene.index.LiveIndexWriterConfig setMergedSegmentWarmer(org.apache.lucene.index.IndexWriter$IndexReaderWarmer)
meth public org.apache.lucene.index.LiveIndexWriterConfig setRAMBufferSizeMB(double)
meth public org.apache.lucene.index.LiveIndexWriterConfig setUseCompoundFile(boolean)
meth public org.apache.lucene.index.MergePolicy getMergePolicy()
meth public org.apache.lucene.index.MergeScheduler getMergeScheduler()
meth public org.apache.lucene.search.similarities.Similarity getSimilarity()
meth public org.apache.lucene.util.InfoStream getInfoStream()
supr java.lang.Object
hfds analyzer,maxBufferedDeleteTerms,maxBufferedDocs,mergedSegmentWarmer,ramBufferSizeMB

CLSS public org.apache.lucene.index.LogByteSizeMergePolicy
cons public init()
fld public final static double DEFAULT_MAX_MERGE_MB = 2048.0
fld public final static double DEFAULT_MAX_MERGE_MB_FOR_FORCED_MERGE = 9.223372036854776E18
fld public final static double DEFAULT_MIN_MERGE_MB = 1.6
meth protected long size(org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.IndexWriter) throws java.io.IOException
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
meth protected long size(org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.IndexWriter) throws java.io.IOException
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
meth protected boolean isMerged(org.apache.lucene.index.SegmentInfos,int,java.util.Map<org.apache.lucene.index.SegmentCommitInfo,java.lang.Boolean>,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth protected boolean verbose(org.apache.lucene.index.IndexWriter)
meth protected long sizeBytes(org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth protected long sizeDocs(org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth protected void message(java.lang.String,org.apache.lucene.index.IndexWriter)
meth public boolean getCalibrateSizeByDeletes()
meth public int getMaxMergeDocs()
meth public int getMergeFactor()
meth public java.lang.String toString()
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedDeletesMerges(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedMerges(org.apache.lucene.index.SegmentInfos,int,java.util.Map<org.apache.lucene.index.SegmentCommitInfo,java.lang.Boolean>,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.IndexWriter) throws java.io.IOException
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
cons protected init(double,long)
cons public init()
fld protected double noCFSRatio
fld protected final static double DEFAULT_NO_CFS_RATIO = 1.0
fld protected final static long DEFAULT_MAX_CFS_SEGMENT_SIZE = 9223372036854775807
fld protected long maxCFSSegmentSize
innr public abstract static DocMap
innr public static MergeAbortedException
innr public static MergeException
innr public static MergeSpecification
innr public static OneMerge
meth protected final boolean isMerged(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth protected long size(org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public abstract org.apache.lucene.index.MergePolicy$MergeSpecification findForcedDeletesMerges(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public abstract org.apache.lucene.index.MergePolicy$MergeSpecification findForcedMerges(org.apache.lucene.index.SegmentInfos,int,java.util.Map<org.apache.lucene.index.SegmentCommitInfo,java.lang.Boolean>,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public abstract org.apache.lucene.index.MergePolicy$MergeSpecification findMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public boolean useCompoundFile(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public double getNoCFSRatio()
meth public final double getMaxCFSSegmentSizeMB()
meth public void setMaxCFSSegmentSizeMB(double)
meth public void setNoCFSRatio(double)
supr java.lang.Object

CLSS public abstract static org.apache.lucene.index.MergePolicy$DocMap
 outer org.apache.lucene.index.MergePolicy
cons protected init()
meth public abstract int map(int)
supr java.lang.Object

CLSS public static org.apache.lucene.index.MergePolicy$MergeAbortedException
 outer org.apache.lucene.index.MergePolicy
cons public init()
cons public init(java.lang.String)
supr java.io.IOException

CLSS public static org.apache.lucene.index.MergePolicy$MergeException
 outer org.apache.lucene.index.MergePolicy
cons public init(java.lang.String,org.apache.lucene.store.Directory)
cons public init(java.lang.Throwable,org.apache.lucene.store.Directory)
meth public org.apache.lucene.store.Directory getDirectory()
supr java.lang.RuntimeException
hfds dir

CLSS public static org.apache.lucene.index.MergePolicy$MergeSpecification
 outer org.apache.lucene.index.MergePolicy
cons public init()
fld public final java.util.List<org.apache.lucene.index.MergePolicy$OneMerge> merges
meth public java.lang.String segString(org.apache.lucene.store.Directory)
meth public void add(org.apache.lucene.index.MergePolicy$OneMerge)
supr java.lang.Object

CLSS public static org.apache.lucene.index.MergePolicy$OneMerge
 outer org.apache.lucene.index.MergePolicy
cons public init(java.util.List<org.apache.lucene.index.SegmentCommitInfo>)
fld public final int totalMaxDoc
fld public final java.util.List<org.apache.lucene.index.SegmentCommitInfo> segments
fld public final org.apache.lucene.index.MergeRateLimiter rateLimiter
fld public volatile long estimatedMergeBytes
meth public int totalNumDocs() throws java.io.IOException
meth public java.lang.String segString()
meth public java.util.List<org.apache.lucene.index.CodecReader> getMergeReaders() throws java.io.IOException
meth public long totalBytesSize() throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$DocMap getDocMap(org.apache.lucene.index.MergeState)
meth public org.apache.lucene.index.SegmentCommitInfo getMergeInfo()
meth public org.apache.lucene.store.MergeInfo getStoreMergeInfo()
meth public void mergeFinished() throws java.io.IOException
meth public void setMergeInfo(org.apache.lucene.index.SegmentCommitInfo)
supr java.lang.Object
hfds error,info,isExternal,maxNumSegments,mergeGen,mergeStartNS,readers,registerDone,totalMergeBytes

CLSS public org.apache.lucene.index.MergePolicyWrapper
cons public init(org.apache.lucene.index.MergePolicy)
fld protected final org.apache.lucene.index.MergePolicy in
meth protected long size(org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public boolean useCompoundFile(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public double getNoCFSRatio()
meth public final void setMaxCFSSegmentSizeMB(double)
meth public final void setNoCFSRatio(double)
meth public java.lang.String toString()
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedDeletesMerges(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedMerges(org.apache.lucene.index.SegmentInfos,int,java.util.Map<org.apache.lucene.index.SegmentCommitInfo,java.lang.Boolean>,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.IndexWriter) throws java.io.IOException
supr org.apache.lucene.index.MergePolicy

CLSS public org.apache.lucene.index.MergeRateLimiter
cons public init(org.apache.lucene.index.MergePolicy$OneMerge)
meth public boolean getAbort()
meth public double getMBPerSec()
meth public long getMinPauseCheckBytes()
meth public long getTotalBytesWritten()
meth public long getTotalPausedNS()
meth public long getTotalStoppedNS()
meth public long pause(long) throws org.apache.lucene.index.MergePolicy$MergeAbortedException
meth public void checkAbort() throws org.apache.lucene.index.MergePolicy$MergeAbortedException
meth public void setAbort()
meth public void setMBPerSec(double)
supr org.apache.lucene.store.RateLimiter
hfds MIN_PAUSE_CHECK_MSEC,abort,lastNS,mbPerSec,merge,minPauseCheckBytes,totalBytesWritten,totalPausedNS,totalStoppedNS
hcls PauseResult

CLSS public abstract org.apache.lucene.index.MergeScheduler
cons protected init()
fld protected org.apache.lucene.util.InfoStream infoStream
intf java.io.Closeable
meth protected boolean verbose()
meth protected void message(java.lang.String)
meth public abstract void close() throws java.io.IOException
meth public abstract void merge(org.apache.lucene.index.IndexWriter,org.apache.lucene.index.MergeTrigger,boolean) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.lucene.index.MergeState
fld public final int[] docBase
fld public final int[] maxDocs
fld public final org.apache.lucene.codecs.DocValuesProducer[] docValuesProducers
fld public final org.apache.lucene.codecs.FieldsProducer[] fieldsProducers
fld public final org.apache.lucene.codecs.NormsProducer[] normsProducers
fld public final org.apache.lucene.codecs.StoredFieldsReader[] storedFieldsReaders
fld public final org.apache.lucene.codecs.TermVectorsReader[] termVectorsReaders
fld public final org.apache.lucene.index.FieldInfos[] fieldInfos
fld public final org.apache.lucene.index.MergeState$DocMap[] docMaps
fld public final org.apache.lucene.index.SegmentInfo segmentInfo
fld public final org.apache.lucene.util.Bits[] liveDocs
fld public final org.apache.lucene.util.InfoStream infoStream
fld public org.apache.lucene.index.FieldInfos mergeFieldInfos
innr public abstract static DocMap
supr java.lang.Object
hcls NoDelDocMap

CLSS public abstract static org.apache.lucene.index.MergeState$DocMap
 outer org.apache.lucene.index.MergeState
meth public abstract int get(int)
meth public abstract int maxDoc()
meth public abstract int numDeletedDocs()
meth public boolean hasDeletions()
meth public final int numDocs()
meth public static org.apache.lucene.index.MergeState$DocMap build(org.apache.lucene.index.CodecReader)
supr java.lang.Object

CLSS public final !enum org.apache.lucene.index.MergeTrigger
fld public final static org.apache.lucene.index.MergeTrigger CLOSING
fld public final static org.apache.lucene.index.MergeTrigger EXPLICIT
fld public final static org.apache.lucene.index.MergeTrigger FULL_FLUSH
fld public final static org.apache.lucene.index.MergeTrigger MERGE_FINISHED
fld public final static org.apache.lucene.index.MergeTrigger SEGMENT_FLUSH
meth public static org.apache.lucene.index.MergeTrigger valueOf(java.lang.String)
meth public static org.apache.lucene.index.MergeTrigger[] values()
supr java.lang.Enum<org.apache.lucene.index.MergeTrigger>

CLSS public org.apache.lucene.index.MultiDocValues
innr public static MultiSortedDocValues
innr public static MultiSortedSetDocValues
innr public static OrdinalMap
meth public static org.apache.lucene.index.BinaryDocValues getBinaryValues(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.NumericDocValues getNormValues(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.NumericDocValues getNumericValues(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.SortedDocValues getSortedValues(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.SortedNumericDocValues getSortedNumericValues(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.index.SortedSetDocValues getSortedSetValues(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.util.Bits getDocsWithField(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public static org.apache.lucene.index.MultiDocValues$MultiSortedDocValues
 outer org.apache.lucene.index.MultiDocValues
fld public final int[] docStarts
fld public final org.apache.lucene.index.MultiDocValues$OrdinalMap mapping
fld public final org.apache.lucene.index.SortedDocValues[] values
meth public int getOrd(int)
meth public int getValueCount()
meth public org.apache.lucene.util.BytesRef lookupOrd(int)
supr org.apache.lucene.index.SortedDocValues

CLSS public static org.apache.lucene.index.MultiDocValues$MultiSortedSetDocValues
 outer org.apache.lucene.index.MultiDocValues
fld public final int[] docStarts
fld public final org.apache.lucene.index.MultiDocValues$OrdinalMap mapping
fld public final org.apache.lucene.index.SortedSetDocValues[] values
meth public long getValueCount()
meth public long nextOrd()
meth public org.apache.lucene.util.BytesRef lookupOrd(long)
meth public void setDocument(int)
supr org.apache.lucene.index.SortedSetDocValues
hfds currentGlobalOrds,currentSubIndex

CLSS public static org.apache.lucene.index.MultiDocValues$OrdinalMap
 outer org.apache.lucene.index.MultiDocValues
intf org.apache.lucene.util.Accountable
meth public int getFirstSegmentNumber(long)
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public long getFirstSegmentOrd(long)
meth public long getValueCount()
meth public long ramBytesUsed()
meth public org.apache.lucene.util.LongValues getGlobalOrds(int)
meth public static org.apache.lucene.index.MultiDocValues$OrdinalMap build(java.lang.Object,org.apache.lucene.index.SortedDocValues[],float) throws java.io.IOException
meth public static org.apache.lucene.index.MultiDocValues$OrdinalMap build(java.lang.Object,org.apache.lucene.index.SortedSetDocValues[],float) throws java.io.IOException
meth public static org.apache.lucene.index.MultiDocValues$OrdinalMap build(java.lang.Object,org.apache.lucene.index.TermsEnum[],long[],float) throws java.io.IOException
supr java.lang.Object
hfds BASE_RAM_BYTES_USED,firstSegments,globalOrdDeltas,owner,ramBytesUsed,segmentMap,segmentToGlobalOrds
hcls SegmentMap

CLSS public final org.apache.lucene.index.MultiFields
cons public init(org.apache.lucene.index.Fields[],org.apache.lucene.index.ReaderSlice[])
meth public int size()
meth public java.util.Iterator<java.lang.String> iterator()
meth public org.apache.lucene.index.Terms terms(java.lang.String) throws java.io.IOException
meth public static java.util.Collection<java.lang.String> getIndexedFields(org.apache.lucene.index.IndexReader)
meth public static org.apache.lucene.index.FieldInfos getMergedFieldInfos(org.apache.lucene.index.IndexReader)
meth public static org.apache.lucene.index.Fields getFields(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public static org.apache.lucene.index.PostingsEnum getTermDocsEnum(org.apache.lucene.index.IndexReader,java.lang.String,org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public static org.apache.lucene.index.PostingsEnum getTermDocsEnum(org.apache.lucene.index.IndexReader,java.lang.String,org.apache.lucene.util.BytesRef,int) throws java.io.IOException
meth public static org.apache.lucene.index.PostingsEnum getTermPositionsEnum(org.apache.lucene.index.IndexReader,java.lang.String,org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public static org.apache.lucene.index.PostingsEnum getTermPositionsEnum(org.apache.lucene.index.IndexReader,java.lang.String,org.apache.lucene.util.BytesRef,int) throws java.io.IOException
meth public static org.apache.lucene.index.Terms getTerms(org.apache.lucene.index.IndexReader,java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.util.Bits getLiveDocs(org.apache.lucene.index.IndexReader)
supr org.apache.lucene.index.Fields
hfds subSlices,subs,terms

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
meth protected void doClose() throws java.io.IOException
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
meth public org.apache.lucene.index.PostingsEnum postings(org.apache.lucene.index.PostingsEnum,int) throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum reset(org.apache.lucene.index.MultiTermsEnum$TermsEnumIndex[]) throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum$SeekStatus seekCeil(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public org.apache.lucene.util.BytesRef next() throws java.io.IOException
meth public org.apache.lucene.util.BytesRef term()
meth public void seekExact(long)
supr org.apache.lucene.index.TermsEnum
hfds INDEX_COMPARATOR,current,currentSubs,lastSeek,lastSeekExact,lastSeekScratch,numSubs,numTop,queue,subDocs,subs,top
hcls TermMergeQueue,TermsEnumIndex,TermsEnumWithSlice

CLSS public final org.apache.lucene.index.NoDeletionPolicy
fld public final static org.apache.lucene.index.IndexDeletionPolicy INSTANCE
meth public org.apache.lucene.index.IndexDeletionPolicy clone()
meth public void onCommit(java.util.List<? extends org.apache.lucene.index.IndexCommit>)
meth public void onInit(java.util.List<? extends org.apache.lucene.index.IndexCommit>)
supr org.apache.lucene.index.IndexDeletionPolicy

CLSS public final org.apache.lucene.index.NoMergePolicy
fld public final static org.apache.lucene.index.MergePolicy INSTANCE
meth protected long size(org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public boolean useCompoundFile(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.index.IndexWriter)
meth public double getNoCFSRatio()
meth public java.lang.String toString()
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedDeletesMerges(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.IndexWriter)
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedMerges(org.apache.lucene.index.SegmentInfos,int,java.util.Map<org.apache.lucene.index.SegmentCommitInfo,java.lang.Boolean>,org.apache.lucene.index.IndexWriter)
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.IndexWriter)
meth public void setMaxCFSSegmentSizeMB(double)
meth public void setNoCFSRatio(double)
supr org.apache.lucene.index.MergePolicy

CLSS public final org.apache.lucene.index.NoMergeScheduler
fld public final static org.apache.lucene.index.MergeScheduler INSTANCE
meth public org.apache.lucene.index.MergeScheduler clone()
meth public void close()
meth public void merge(org.apache.lucene.index.IndexWriter,org.apache.lucene.index.MergeTrigger,boolean)
supr org.apache.lucene.index.MergeScheduler

CLSS public abstract org.apache.lucene.index.NumericDocValues
cons protected init()
meth public abstract long get(int)
supr java.lang.Object

CLSS public org.apache.lucene.index.OrdTermState
cons public init()
fld public long ord
meth public java.lang.String toString()
meth public void copyFrom(org.apache.lucene.index.TermState)
supr org.apache.lucene.index.TermState

CLSS public org.apache.lucene.index.ParallelCompositeReader
cons public !varargs init(boolean,org.apache.lucene.index.CompositeReader[]) throws java.io.IOException
cons public !varargs init(org.apache.lucene.index.CompositeReader[]) throws java.io.IOException
cons public init(boolean,org.apache.lucene.index.CompositeReader[],org.apache.lucene.index.CompositeReader[]) throws java.io.IOException
meth protected void doClose() throws java.io.IOException
supr org.apache.lucene.index.BaseCompositeReader<org.apache.lucene.index.LeafReader>
hfds closeSubReaders,completeReaderSet

CLSS public org.apache.lucene.index.ParallelLeafReader
cons public !varargs init(boolean,org.apache.lucene.index.LeafReader[]) throws java.io.IOException
cons public !varargs init(org.apache.lucene.index.LeafReader[]) throws java.io.IOException
cons public init(boolean,org.apache.lucene.index.LeafReader[],org.apache.lucene.index.LeafReader[]) throws java.io.IOException
meth protected void doClose() throws java.io.IOException
meth public int maxDoc()
meth public int numDocs()
meth public java.lang.String toString()
meth public org.apache.lucene.index.BinaryDocValues getBinaryDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.FieldInfos getFieldInfos()
meth public org.apache.lucene.index.Fields fields()
meth public org.apache.lucene.index.Fields getTermVectors(int) throws java.io.IOException
meth public org.apache.lucene.index.LeafReader[] getParallelReaders()
meth public org.apache.lucene.index.NumericDocValues getNormValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.NumericDocValues getNumericDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedDocValues getSortedDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedNumericDocValues getSortedNumericDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedSetDocValues getSortedSetDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.util.Bits getDocsWithField(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.util.Bits getLiveDocs()
meth public void addCoreClosedListener(org.apache.lucene.index.LeafReader$CoreClosedListener)
meth public void checkIntegrity() throws java.io.IOException
meth public void document(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
meth public void removeCoreClosedListener(org.apache.lucene.index.LeafReader$CoreClosedListener)
supr org.apache.lucene.index.LeafReader
hfds closeSubReaders,completeReaderSet,fieldInfos,fieldToReader,fields,hasDeletions,maxDoc,numDocs,parallelReaders,storedFieldsReaders,tvFieldToReader
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
meth public org.apache.lucene.util.AttributeSource attributes()
meth public static boolean featureRequested(int,short)
supr org.apache.lucene.search.DocIdSetIterator
hfds atts

CLSS public org.apache.lucene.index.PrefixCodedTerms
innr public static Builder
innr public static TermIterator
intf org.apache.lucene.util.Accountable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public long ramBytesUsed()
meth public long size()
meth public org.apache.lucene.index.PrefixCodedTerms$TermIterator iterator()
meth public void setDelGen(long)
supr java.lang.Object
hfds buffer,delGen,size

CLSS public static org.apache.lucene.index.PrefixCodedTerms$Builder
 outer org.apache.lucene.index.PrefixCodedTerms
cons public init()
meth public org.apache.lucene.index.PrefixCodedTerms finish()
meth public void add(java.lang.String,org.apache.lucene.util.BytesRef)
meth public void add(org.apache.lucene.index.Term)
supr java.lang.Object
hfds buffer,lastTerm,lastTermBytes,output,size

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

CLSS public abstract org.apache.lucene.index.RandomAccessOrds
cons protected init()
meth public abstract int cardinality()
meth public abstract long ordAt(int)
supr org.apache.lucene.index.SortedSetDocValues

CLSS public final org.apache.lucene.index.ReaderManager
cons public init(org.apache.lucene.index.DirectoryReader) throws java.io.IOException
cons public init(org.apache.lucene.index.IndexWriter) throws java.io.IOException
cons public init(org.apache.lucene.index.IndexWriter,boolean) throws java.io.IOException
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
cons public init(org.apache.lucene.index.SegmentInfo,int,long,long,long)
fld public final org.apache.lucene.index.SegmentInfo info
meth public boolean hasDeletions()
meth public boolean hasFieldUpdates()
meth public int getDelCount()
meth public java.lang.String toString()
meth public java.lang.String toString(int)
meth public java.lang.String toString(org.apache.lucene.store.Directory,int)
 anno 0 java.lang.Deprecated()
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
meth public void setGenUpdatesFiles(java.util.Map<java.lang.Long,java.util.Set<java.lang.String>>)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds bufferedDeletesGen,delCount,delGen,docValuesGen,dvUpdatesFiles,fieldInfosFiles,fieldInfosGen,genUpdatesFiles,nextWriteDelGen,nextWriteDocValuesGen,nextWriteFieldInfosGen,sizeInBytes

CLSS public final org.apache.lucene.index.SegmentInfo
cons public init(org.apache.lucene.store.Directory,org.apache.lucene.util.Version,java.lang.String,int,boolean,org.apache.lucene.codecs.Codec,java.util.Map<java.lang.String,java.lang.String>,byte[],java.util.Map<java.lang.String,java.lang.String>)
fld public final java.lang.String name
fld public final org.apache.lucene.store.Directory dir
fld public final static int NO = -1
fld public final static int YES = 1
meth public boolean equals(java.lang.Object)
meth public boolean getUseCompoundFile()
meth public byte[] getId()
meth public int hashCode()
meth public int maxDoc()
meth public java.lang.String getAttribute(java.lang.String)
meth public java.lang.String putAttribute(java.lang.String,java.lang.String)
meth public java.lang.String toString()
meth public java.lang.String toString(int)
meth public java.lang.String toString(org.apache.lucene.store.Directory,int)
 anno 0 java.lang.Deprecated()
meth public java.util.Map<java.lang.String,java.lang.String> getAttributes()
meth public java.util.Map<java.lang.String,java.lang.String> getDiagnostics()
meth public java.util.Set<java.lang.String> files()
meth public org.apache.lucene.codecs.Codec getCodec()
meth public org.apache.lucene.util.Version getVersion()
meth public void addFile(java.lang.String)
meth public void addFiles(java.util.Collection<java.lang.String>)
meth public void setCodec(org.apache.lucene.codecs.Codec)
meth public void setFiles(java.util.Collection<java.lang.String>)
supr java.lang.Object
hfds attributes,codec,diagnostics,id,isCompoundFile,maxDoc,setFiles,version

CLSS public final org.apache.lucene.index.SegmentInfos
cons public init()
fld public final static int VERSION_40 = 0
fld public final static int VERSION_46 = 1
fld public final static int VERSION_48 = 2
fld public final static int VERSION_49 = 3
fld public final static int VERSION_50 = 4
fld public final static int VERSION_51 = 5
fld public final static int VERSION_53 = 6
fld public int counter
fld public java.util.Map<java.lang.String,java.lang.String> userData
fld public long version
innr public abstract static FindSegmentsFile
intf java.lang.Cloneable
intf java.lang.Iterable<org.apache.lucene.index.SegmentCommitInfo>
meth public byte[] getId()
meth public final java.util.Collection<java.lang.String> files(org.apache.lucene.store.Directory,boolean) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public final static org.apache.lucene.index.SegmentInfos readCommit(org.apache.lucene.store.Directory,java.lang.String) throws java.io.IOException
meth public final static org.apache.lucene.index.SegmentInfos readLatestCommit(org.apache.lucene.store.Directory) throws java.io.IOException
meth public int size()
meth public int totalMaxDoc()
meth public java.lang.String getSegmentsFileName()
meth public java.lang.String toString()
meth public java.lang.String toString(org.apache.lucene.store.Directory)
 anno 0 java.lang.Deprecated()
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
meth public void remove(org.apache.lucene.index.SegmentCommitInfo)
supr java.lang.Object
hfds VERSION_CURRENT,generation,id,infoStream,lastGeneration,luceneVersion,minSegmentLuceneVersion,pendingCommit,segments,unsupportedCodecs

CLSS public abstract static org.apache.lucene.index.SegmentInfos$FindSegmentsFile<%0 extends java.lang.Object>
 outer org.apache.lucene.index.SegmentInfos
cons public init(org.apache.lucene.store.Directory)
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
cons public init(org.apache.lucene.index.SegmentCommitInfo,org.apache.lucene.store.IOContext) throws java.io.IOException
meth protected void doClose() throws java.io.IOException
meth public int maxDoc()
meth public int numDocs()
meth public java.lang.Object getCombinedCoreAndDeletesKey()
meth public java.lang.Object getCoreCacheKey()
meth public java.lang.String getSegmentName()
meth public java.lang.String toString()
meth public org.apache.lucene.codecs.DocValuesProducer getDocValuesReader()
meth public org.apache.lucene.codecs.FieldsProducer getPostingsReader()
meth public org.apache.lucene.codecs.NormsProducer getNormsReader()
meth public org.apache.lucene.codecs.StoredFieldsReader getFieldsReader()
meth public org.apache.lucene.codecs.TermVectorsReader getTermVectorsReader()
meth public org.apache.lucene.index.FieldInfos getFieldInfos()
meth public org.apache.lucene.index.SegmentCommitInfo getSegmentInfo()
meth public org.apache.lucene.store.Directory directory()
meth public org.apache.lucene.util.Bits getLiveDocs()
meth public void addCoreClosedListener(org.apache.lucene.index.LeafReader$CoreClosedListener)
meth public void removeCoreClosedListener(org.apache.lucene.index.LeafReader$CoreClosedListener)
supr org.apache.lucene.index.CodecReader
hfds core,docValuesProducer,fieldInfos,liveDocs,numDocs,segDocValues,si

CLSS public org.apache.lucene.index.SegmentWriteState
cons public init(org.apache.lucene.index.SegmentWriteState,java.lang.String)
cons public init(org.apache.lucene.util.InfoStream,org.apache.lucene.store.Directory,org.apache.lucene.index.SegmentInfo,org.apache.lucene.index.FieldInfos,org.apache.lucene.index.BufferedUpdates,org.apache.lucene.store.IOContext)
cons public init(org.apache.lucene.util.InfoStream,org.apache.lucene.store.Directory,org.apache.lucene.index.SegmentInfo,org.apache.lucene.index.FieldInfos,org.apache.lucene.index.BufferedUpdates,org.apache.lucene.store.IOContext,java.lang.String)
fld public final java.lang.Object segUpdates
fld public final java.lang.String segmentSuffix
fld public final org.apache.lucene.index.FieldInfos fieldInfos
fld public final org.apache.lucene.index.SegmentInfo segmentInfo
fld public final org.apache.lucene.store.Directory directory
fld public final org.apache.lucene.store.IOContext context
fld public final org.apache.lucene.util.InfoStream infoStream
fld public int delCountOnFlush
fld public org.apache.lucene.util.MutableBits liveDocs
supr java.lang.Object

CLSS public org.apache.lucene.index.SerialMergeScheduler
cons public init()
meth public void close()
meth public void merge(org.apache.lucene.index.IndexWriter,org.apache.lucene.index.MergeTrigger,boolean) throws java.io.IOException
supr org.apache.lucene.index.MergeScheduler

CLSS public org.apache.lucene.index.SimpleMergedSegmentWarmer
cons public init(org.apache.lucene.util.InfoStream)
meth public void warm(org.apache.lucene.index.LeafReader) throws java.io.IOException
supr org.apache.lucene.index.IndexWriter$IndexReaderWarmer
hfds infoStream

CLSS public final org.apache.lucene.index.SingleTermsEnum
cons public init(org.apache.lucene.index.TermsEnum,org.apache.lucene.util.BytesRef)
meth protected org.apache.lucene.index.FilteredTermsEnum$AcceptStatus accept(org.apache.lucene.util.BytesRef)
supr org.apache.lucene.index.FilteredTermsEnum
hfds singleRef

CLSS public final org.apache.lucene.index.SlowCodecReaderWrapper
meth public static org.apache.lucene.index.CodecReader wrap(org.apache.lucene.index.LeafReader) throws java.io.IOException
supr java.lang.Object

CLSS public final org.apache.lucene.index.SlowCompositeReaderWrapper
meth protected void doClose() throws java.io.IOException
meth public int maxDoc()
meth public int numDocs()
meth public java.lang.Object getCombinedCoreAndDeletesKey()
meth public java.lang.Object getCoreCacheKey()
meth public java.lang.String toString()
meth public org.apache.lucene.index.BinaryDocValues getBinaryDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.FieldInfos getFieldInfos()
meth public org.apache.lucene.index.Fields fields()
meth public org.apache.lucene.index.Fields getTermVectors(int) throws java.io.IOException
meth public org.apache.lucene.index.NumericDocValues getNormValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.NumericDocValues getNumericDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedDocValues getSortedDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedNumericDocValues getSortedNumericDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedSetDocValues getSortedSetDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.util.Bits getDocsWithField(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.util.Bits getLiveDocs()
meth public static org.apache.lucene.index.LeafReader wrap(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public void addCoreClosedListener(org.apache.lucene.index.LeafReader$CoreClosedListener)
meth public void checkIntegrity() throws java.io.IOException
meth public void document(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
meth public void removeCoreClosedListener(org.apache.lucene.index.LeafReader$CoreClosedListener)
supr org.apache.lucene.index.LeafReader
hfds cachedOrdMaps,fields,in,merging

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

CLSS public abstract org.apache.lucene.index.SortedDocValues
cons protected init()
meth public abstract int getOrd(int)
meth public abstract int getValueCount()
meth public abstract org.apache.lucene.util.BytesRef lookupOrd(int)
meth public int lookupTerm(org.apache.lucene.util.BytesRef)
meth public org.apache.lucene.index.TermsEnum termsEnum()
meth public org.apache.lucene.util.BytesRef get(int)
supr org.apache.lucene.index.BinaryDocValues
hfds empty

CLSS public abstract org.apache.lucene.index.SortedNumericDocValues
cons protected init()
meth public abstract int count()
meth public abstract long valueAt(int)
meth public abstract void setDocument(int)
supr java.lang.Object

CLSS public abstract org.apache.lucene.index.SortedSetDocValues
cons protected init()
fld public final static long NO_MORE_ORDS = -1
meth public abstract long getValueCount()
meth public abstract long nextOrd()
meth public abstract org.apache.lucene.util.BytesRef lookupOrd(long)
meth public abstract void setDocument(int)
meth public long lookupTerm(org.apache.lucene.util.BytesRef)
meth public org.apache.lucene.index.TermsEnum termsEnum()
supr java.lang.Object

CLSS public abstract org.apache.lucene.index.StoredFieldVisitor
cons protected init()
innr public final static !enum Status
meth public abstract org.apache.lucene.index.StoredFieldVisitor$Status needsField(org.apache.lucene.index.FieldInfo) throws java.io.IOException
meth public void binaryField(org.apache.lucene.index.FieldInfo,byte[]) throws java.io.IOException
meth public void doubleField(org.apache.lucene.index.FieldInfo,double) throws java.io.IOException
meth public void floatField(org.apache.lucene.index.FieldInfo,float) throws java.io.IOException
meth public void intField(org.apache.lucene.index.FieldInfo,int) throws java.io.IOException
meth public void longField(org.apache.lucene.index.FieldInfo,long) throws java.io.IOException
meth public void stringField(org.apache.lucene.index.FieldInfo,byte[]) throws java.io.IOException
supr java.lang.Object

CLSS public final static !enum org.apache.lucene.index.StoredFieldVisitor$Status
 outer org.apache.lucene.index.StoredFieldVisitor
fld public final static org.apache.lucene.index.StoredFieldVisitor$Status NO
fld public final static org.apache.lucene.index.StoredFieldVisitor$Status STOP
fld public final static org.apache.lucene.index.StoredFieldVisitor$Status YES
meth public static org.apache.lucene.index.StoredFieldVisitor$Status valueOf(java.lang.String)
meth public static org.apache.lucene.index.StoredFieldVisitor$Status[] values()
supr java.lang.Enum<org.apache.lucene.index.StoredFieldVisitor$Status>

CLSS public final org.apache.lucene.index.Term
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,org.apache.lucene.util.BytesRef)
cons public init(java.lang.String,org.apache.lucene.util.BytesRefBuilder)
intf java.lang.Comparable<org.apache.lucene.index.Term>
meth public boolean equals(java.lang.Object)
meth public final int compareTo(org.apache.lucene.index.Term)
meth public final java.lang.String field()
meth public final java.lang.String text()
meth public final java.lang.String toString()
meth public final org.apache.lucene.util.BytesRef bytes()
meth public final static java.lang.String toString(org.apache.lucene.util.BytesRef)
meth public int hashCode()
supr java.lang.Object
hfds bytes,field

CLSS public final org.apache.lucene.index.TermContext
cons public init(org.apache.lucene.index.IndexReaderContext)
cons public init(org.apache.lucene.index.IndexReaderContext,org.apache.lucene.index.TermState,int,int,long)
meth public boolean hasOnlyRealTerms()
meth public boolean wasBuiltFor(org.apache.lucene.index.IndexReaderContext)
meth public int docFreq()
meth public java.lang.String toString()
meth public long totalTermFreq()
meth public org.apache.lucene.index.TermState get(int)
meth public static org.apache.lucene.index.TermContext build(org.apache.lucene.index.IndexReaderContext,org.apache.lucene.index.Term) throws java.io.IOException
meth public void accumulateStatistics(int,long)
meth public void clear()
meth public void register(org.apache.lucene.index.TermState,int)
meth public void register(org.apache.lucene.index.TermState,int,int,long)
supr java.lang.Object
hfds docFreq,states,topReaderContextIdentity,totalTermFreq

CLSS public abstract org.apache.lucene.index.TermState
cons protected init()
intf java.lang.Cloneable
meth public abstract void copyFrom(org.apache.lucene.index.TermState)
meth public boolean isRealTerm()
meth public java.lang.String toString()
meth public org.apache.lucene.index.TermState clone()
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
supr java.lang.Object

CLSS public abstract org.apache.lucene.index.TermsEnum
cons protected init()
fld public final static org.apache.lucene.index.TermsEnum EMPTY
innr public final static !enum SeekStatus
intf org.apache.lucene.util.BytesRefIterator
meth public abstract int docFreq() throws java.io.IOException
meth public abstract long ord() throws java.io.IOException
meth public abstract long totalTermFreq() throws java.io.IOException
meth public abstract org.apache.lucene.index.PostingsEnum postings(org.apache.lucene.index.PostingsEnum,int) throws java.io.IOException
meth public abstract org.apache.lucene.index.TermsEnum$SeekStatus seekCeil(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public abstract org.apache.lucene.util.BytesRef term() throws java.io.IOException
meth public abstract void seekExact(long) throws java.io.IOException
meth public boolean seekExact(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public final org.apache.lucene.index.DocsAndPositionsEnum docsAndPositions(org.apache.lucene.util.Bits,org.apache.lucene.index.DocsAndPositionsEnum) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public final org.apache.lucene.index.DocsAndPositionsEnum docsAndPositions(org.apache.lucene.util.Bits,org.apache.lucene.index.DocsAndPositionsEnum,int) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public final org.apache.lucene.index.DocsEnum docs(org.apache.lucene.util.Bits,org.apache.lucene.index.DocsEnum) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public final org.apache.lucene.index.DocsEnum docs(org.apache.lucene.util.Bits,org.apache.lucene.index.DocsEnum,int) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public final org.apache.lucene.index.PostingsEnum postings(org.apache.lucene.index.PostingsEnum) throws java.io.IOException
meth public org.apache.lucene.index.TermState termState() throws java.io.IOException
meth public org.apache.lucene.util.AttributeSource attributes()
meth public void seekExact(org.apache.lucene.util.BytesRef,org.apache.lucene.index.TermState) throws java.io.IOException
supr java.lang.Object
hfds atts

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
meth protected org.apache.lucene.index.TieredMergePolicy$MergeScore score(java.util.List<org.apache.lucene.index.SegmentCommitInfo>,boolean,long,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public double getFloorSegmentMB()
meth public double getForceMergeDeletesPctAllowed()
meth public double getMaxMergedSegmentMB()
meth public double getReclaimDeletesWeight()
meth public double getSegmentsPerTier()
meth public int getMaxMergeAtOnce()
meth public int getMaxMergeAtOnceExplicit()
meth public java.lang.String toString()
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedDeletesMerges(org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedMerges(org.apache.lucene.index.SegmentInfos,int,java.util.Map<org.apache.lucene.index.SegmentCommitInfo,java.lang.Boolean>,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public org.apache.lucene.index.TieredMergePolicy setFloorSegmentMB(double)
meth public org.apache.lucene.index.TieredMergePolicy setForceMergeDeletesPctAllowed(double)
meth public org.apache.lucene.index.TieredMergePolicy setMaxMergeAtOnce(int)
meth public org.apache.lucene.index.TieredMergePolicy setMaxMergeAtOnceExplicit(int)
meth public org.apache.lucene.index.TieredMergePolicy setMaxMergedSegmentMB(double)
meth public org.apache.lucene.index.TieredMergePolicy setReclaimDeletesWeight(double)
meth public org.apache.lucene.index.TieredMergePolicy setSegmentsPerTier(double)
supr org.apache.lucene.index.MergePolicy
hfds floorSegmentBytes,forceMergeDeletesPctAllowed,maxMergeAtOnce,maxMergeAtOnceExplicit,maxMergedSegmentBytes,reclaimDeletesWeight,segsPerTier
hcls SegmentByteSizeDescending

CLSS protected abstract static org.apache.lucene.index.TieredMergePolicy$MergeScore
 outer org.apache.lucene.index.TieredMergePolicy
cons protected init()
supr java.lang.Object

CLSS public org.apache.lucene.index.TrackingIndexWriter
cons public init(org.apache.lucene.index.IndexWriter)
meth public !varargs long addIndexes(org.apache.lucene.index.CodecReader[]) throws java.io.IOException
meth public !varargs long addIndexes(org.apache.lucene.store.Directory[]) throws java.io.IOException
meth public !varargs long deleteDocuments(org.apache.lucene.index.Term[]) throws java.io.IOException
meth public !varargs long deleteDocuments(org.apache.lucene.search.Query[]) throws java.io.IOException
meth public long addDocument(java.lang.Iterable<? extends org.apache.lucene.index.IndexableField>) throws java.io.IOException
meth public long addDocuments(java.lang.Iterable<? extends java.lang.Iterable<? extends org.apache.lucene.index.IndexableField>>) throws java.io.IOException
meth public long deleteAll() throws java.io.IOException
meth public long deleteDocuments(org.apache.lucene.index.Term) throws java.io.IOException
meth public long deleteDocuments(org.apache.lucene.search.Query) throws java.io.IOException
meth public long getAndIncrementGeneration()
meth public long getGeneration()
meth public long tryDeleteDocument(org.apache.lucene.index.IndexReader,int) throws java.io.IOException
meth public long updateDocument(org.apache.lucene.index.Term,java.lang.Iterable<? extends org.apache.lucene.index.IndexableField>) throws java.io.IOException
meth public long updateDocuments(org.apache.lucene.index.Term,java.lang.Iterable<? extends java.lang.Iterable<? extends org.apache.lucene.index.IndexableField>>) throws java.io.IOException
meth public org.apache.lucene.index.IndexWriter getIndexWriter()
supr java.lang.Object
hfds indexingGen,writer

CLSS public abstract interface org.apache.lucene.index.TwoPhaseCommit
meth public abstract void commit() throws java.io.IOException
meth public abstract void prepareCommit() throws java.io.IOException
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
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findForcedMerges(org.apache.lucene.index.SegmentInfos,int,java.util.Map<org.apache.lucene.index.SegmentCommitInfo,java.lang.Boolean>,org.apache.lucene.index.IndexWriter) throws java.io.IOException
meth public org.apache.lucene.index.MergePolicy$MergeSpecification findMerges(org.apache.lucene.index.MergeTrigger,org.apache.lucene.index.SegmentInfos,org.apache.lucene.index.IndexWriter) throws java.io.IOException
supr org.apache.lucene.index.MergePolicyWrapper

CLSS abstract interface org.apache.lucene.index.package-info

CLSS abstract interface org.apache.lucene.package-info

CLSS public org.apache.lucene.search.AutomatonQuery
cons public init(org.apache.lucene.index.Term,org.apache.lucene.util.automaton.Automaton)
cons public init(org.apache.lucene.index.Term,org.apache.lucene.util.automaton.Automaton,int)
cons public init(org.apache.lucene.index.Term,org.apache.lucene.util.automaton.Automaton,int,boolean)
fld protected final org.apache.lucene.index.Term term
fld protected final org.apache.lucene.util.automaton.Automaton automaton
fld protected final org.apache.lucene.util.automaton.CompiledAutomaton compiled
meth protected org.apache.lucene.index.TermsEnum getTermsEnum(org.apache.lucene.index.Terms,org.apache.lucene.util.AttributeSource) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.util.automaton.Automaton getAutomaton()
supr org.apache.lucene.search.MultiTermQuery

CLSS public final org.apache.lucene.search.BitsFilteredDocIdSet
 anno 0 java.lang.Deprecated()
cons public init(org.apache.lucene.search.DocIdSet,org.apache.lucene.util.Bits)
meth protected boolean match(int)
meth public static org.apache.lucene.search.DocIdSet wrap(org.apache.lucene.search.DocIdSet,org.apache.lucene.util.Bits)
supr org.apache.lucene.search.FilteredDocIdSet
hfds acceptDocs

CLSS public final org.apache.lucene.search.BlendedTermQuery
fld public final static org.apache.lucene.search.BlendedTermQuery$RewriteMethod BOOLEAN_REWRITE
fld public final static org.apache.lucene.search.BlendedTermQuery$RewriteMethod DISJUNCTION_MAX_REWRITE
innr public abstract static RewriteMethod
innr public static Builder
innr public static DisjunctionMaxRewrite
meth public boolean equals(java.lang.Object)
meth public final org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
supr org.apache.lucene.search.Query
hfds boosts,contexts,rewriteMethod,terms

CLSS public static org.apache.lucene.search.BlendedTermQuery$Builder
 outer org.apache.lucene.search.BlendedTermQuery
cons public init()
meth public org.apache.lucene.search.BlendedTermQuery build()
meth public org.apache.lucene.search.BlendedTermQuery$Builder add(org.apache.lucene.index.Term)
meth public org.apache.lucene.search.BlendedTermQuery$Builder add(org.apache.lucene.index.Term,float)
meth public org.apache.lucene.search.BlendedTermQuery$Builder add(org.apache.lucene.index.Term,float,org.apache.lucene.index.TermContext)
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
meth public void setOccur(org.apache.lucene.search.BooleanClause$Occur)
 anno 0 java.lang.Deprecated()
meth public void setQuery(org.apache.lucene.search.Query)
 anno 0 java.lang.Deprecated()
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
cons public init()
 anno 0 java.lang.Deprecated()
cons public init(boolean)
 anno 0 java.lang.Deprecated()
innr public static Builder
innr public static TooManyClauses
intf java.lang.Iterable<org.apache.lucene.search.BooleanClause>
meth public boolean equals(java.lang.Object)
meth public boolean isCoordDisabled()
meth public final java.util.Iterator<org.apache.lucene.search.BooleanClause> iterator()
meth public int getMinimumNumberShouldMatch()
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public java.util.List<org.apache.lucene.search.BooleanClause> clauses()
meth public org.apache.lucene.search.BooleanClause[] getClauses()
 anno 0 java.lang.Deprecated()
meth public org.apache.lucene.search.BooleanQuery clone()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
meth public static int getMaxClauseCount()
meth public static void setMaxClauseCount(int)
meth public void add(org.apache.lucene.search.BooleanClause)
 anno 0 java.lang.Deprecated()
meth public void add(org.apache.lucene.search.Query,org.apache.lucene.search.BooleanClause$Occur)
 anno 0 java.lang.Deprecated()
meth public void setMinimumNumberShouldMatch(int)
 anno 0 java.lang.Deprecated()
supr org.apache.lucene.search.Query
hfds clauseSets,clauses,disableCoord,hashCode,maxClauseCount,minimumNumberShouldMatch,mutable

CLSS public static org.apache.lucene.search.BooleanQuery$Builder
 outer org.apache.lucene.search.BooleanQuery
cons public init()
meth public org.apache.lucene.search.BooleanQuery build()
meth public org.apache.lucene.search.BooleanQuery$Builder add(org.apache.lucene.search.BooleanClause)
meth public org.apache.lucene.search.BooleanQuery$Builder add(org.apache.lucene.search.Query,org.apache.lucene.search.BooleanClause$Occur)
meth public org.apache.lucene.search.BooleanQuery$Builder setDisableCoord(boolean)
meth public org.apache.lucene.search.BooleanQuery$Builder setMinimumNumberShouldMatch(int)
supr java.lang.Object
hfds clauses,disableCoord,minimumNumberShouldMatch

CLSS public static org.apache.lucene.search.BooleanQuery$TooManyClauses
 outer org.apache.lucene.search.BooleanQuery
cons public init()
supr java.lang.RuntimeException

CLSS public abstract interface org.apache.lucene.search.BoostAttribute
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
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
supr org.apache.lucene.search.Query
hfds NO_PARENS_REQUIRED_QUERIES,query

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
hcls CachedScorer,NoScoreCachingCollector,NoScoreCachingLeafCollector,ScoreCachingCollector,ScoreCachingLeafCollector

CLSS public org.apache.lucene.search.CachingWrapperFilter
 anno 0 java.lang.Deprecated()
cons public init(org.apache.lucene.search.Filter)
cons public init(org.apache.lucene.search.Filter,org.apache.lucene.search.FilterCachingPolicy)
intf org.apache.lucene.util.Accountable
meth protected org.apache.lucene.search.DocIdSet cacheImpl(org.apache.lucene.search.DocIdSetIterator,org.apache.lucene.index.LeafReader) throws java.io.IOException
meth protected org.apache.lucene.search.DocIdSet docIdSetToCache(org.apache.lucene.search.DocIdSet,org.apache.lucene.index.LeafReader) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public long ramBytesUsed()
meth public org.apache.lucene.search.DocIdSet getDocIdSet(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.util.Bits) throws java.io.IOException
meth public org.apache.lucene.search.Filter getFilter()
supr org.apache.lucene.search.Filter
hfds cache,filter,hitCount,missCount,policy

CLSS public org.apache.lucene.search.CachingWrapperQuery
 anno 0 java.lang.Deprecated()
cons public init(org.apache.lucene.search.Query)
cons public init(org.apache.lucene.search.Query,org.apache.lucene.search.QueryCachingPolicy)
intf java.lang.Cloneable
intf org.apache.lucene.util.Accountable
meth protected org.apache.lucene.search.DocIdSet cacheImpl(org.apache.lucene.search.DocIdSetIterator,org.apache.lucene.index.LeafReader) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public long ramBytesUsed()
meth public org.apache.lucene.search.Query getQuery()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
supr org.apache.lucene.search.Query
hfds cache,hitCount,missCount,policy,query

CLSS public org.apache.lucene.search.CollectionStatistics
cons public init(java.lang.String,long,long,long,long)
meth public final java.lang.String field()
meth public final long docCount()
meth public final long maxDoc()
meth public final long sumDocFreq()
meth public final long sumTotalTermFreq()
supr java.lang.Object
hfds docCount,field,maxDoc,sumDocFreq,sumTotalTermFreq

CLSS public final org.apache.lucene.search.CollectionTerminatedException
cons public init()
supr java.lang.RuntimeException

CLSS public abstract interface org.apache.lucene.search.Collector
meth public abstract boolean needsScores()
meth public abstract org.apache.lucene.search.LeafCollector getLeafCollector(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException

CLSS public abstract interface org.apache.lucene.search.CollectorManager<%0 extends org.apache.lucene.search.Collector, %1 extends java.lang.Object>
meth public abstract {org.apache.lucene.search.CollectorManager%0} newCollector() throws java.io.IOException
meth public abstract {org.apache.lucene.search.CollectorManager%1} reduce(java.util.Collection<{org.apache.lucene.search.CollectorManager%0}>) throws java.io.IOException

CLSS public org.apache.lucene.search.ConjunctionDISI
meth protected boolean matches() throws java.io.IOException
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int nextDoc() throws java.io.IOException
meth public long cost()
meth public static org.apache.lucene.search.ConjunctionDISI intersectIterators(java.util.List<org.apache.lucene.search.DocIdSetIterator>)
meth public static org.apache.lucene.search.ConjunctionDISI intersectScorers(java.util.List<org.apache.lucene.search.Scorer>)
meth public static org.apache.lucene.search.ConjunctionDISI intersectSpans(java.util.List<org.apache.lucene.search.spans.Spans>)
supr org.apache.lucene.search.DocIdSetIterator
hfds lead,others
hcls TwoPhase,TwoPhaseConjunctionDISI

CLSS public final org.apache.lucene.search.ConstantScoreQuery
cons public init(org.apache.lucene.search.Query)
innr protected ConstantBulkScorer
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query getQuery()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
supr org.apache.lucene.search.Query
hfds query

CLSS protected org.apache.lucene.search.ConstantScoreQuery$ConstantBulkScorer
 outer org.apache.lucene.search.ConstantScoreQuery
cons public init(org.apache.lucene.search.ConstantScoreQuery,org.apache.lucene.search.BulkScorer,org.apache.lucene.search.Weight,float)
meth public int score(org.apache.lucene.search.LeafCollector,org.apache.lucene.util.Bits,int,int) throws java.io.IOException
meth public long cost()
supr org.apache.lucene.search.BulkScorer
hfds bulkScorer,theScore,weight

CLSS public final org.apache.lucene.search.ConstantScoreScorer
cons public init(org.apache.lucene.search.Weight,float,org.apache.lucene.search.DocIdSetIterator)
cons public init(org.apache.lucene.search.Weight,float,org.apache.lucene.search.TwoPhaseIterator)
meth public float score() throws java.io.IOException
meth public int docID()
meth public int freq() throws java.io.IOException
meth public org.apache.lucene.search.DocIdSetIterator iterator()
meth public org.apache.lucene.search.TwoPhaseIterator twoPhaseIterator()
supr org.apache.lucene.search.Scorer
hfds disi,score,twoPhaseIterator

CLSS public abstract org.apache.lucene.search.ConstantScoreWeight
cons protected init(org.apache.lucene.search.Query)
meth protected final float boost()
meth protected final float queryNorm()
meth protected final float score()
meth public final float getValueForNormalization() throws java.io.IOException
meth public org.apache.lucene.search.Explanation explain(org.apache.lucene.index.LeafReaderContext,int) throws java.io.IOException
meth public void extractTerms(java.util.Set<org.apache.lucene.index.Term>)
meth public void normalize(float,float)
supr org.apache.lucene.search.Weight
hfds boost,queryNorm,queryWeight

CLSS public org.apache.lucene.search.ControlledRealTimeReopenThread<%0 extends java.lang.Object>
cons public init(org.apache.lucene.index.TrackingIndexWriter,org.apache.lucene.search.ReferenceManager<{org.apache.lucene.search.ControlledRealTimeReopenThread%0}>,double,double)
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
meth public org.apache.lucene.search.DisiWrapper topList()
meth public org.apache.lucene.search.DisiWrapper updateTop()
supr java.lang.Object
hfds heap,size

CLSS public org.apache.lucene.search.DisiWrapper
cons public init(org.apache.lucene.search.Scorer)
cons public init(org.apache.lucene.search.spans.Spans)
fld public final float matchCost
fld public final long cost
fld public final org.apache.lucene.search.DocIdSetIterator approximation
fld public final org.apache.lucene.search.DocIdSetIterator iterator
fld public final org.apache.lucene.search.Scorer scorer
fld public final org.apache.lucene.search.TwoPhaseIterator twoPhaseView
fld public final org.apache.lucene.search.spans.Spans spans
fld public int doc
fld public int lastApproxMatchDoc
fld public int lastApproxNonMatchDoc
fld public org.apache.lucene.search.DisiWrapper next
supr java.lang.Object

CLSS public org.apache.lucene.search.DisjunctionDISIApproximation
cons public init(org.apache.lucene.search.DisiPriorityQueue)
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int nextDoc() throws java.io.IOException
meth public long cost()
supr org.apache.lucene.search.DocIdSetIterator
hfds cost,subIterators

CLSS public final org.apache.lucene.search.DisjunctionMaxQuery
cons public init(float)
 anno 0 java.lang.Deprecated()
cons public init(java.util.Collection<org.apache.lucene.search.Query>,float)
innr protected DisjunctionMaxWeight
intf java.lang.Iterable<org.apache.lucene.search.Query>
meth public boolean equals(java.lang.Object)
meth public float getTieBreakerMultiplier()
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public java.util.ArrayList<org.apache.lucene.search.Query> getDisjuncts()
meth public java.util.Iterator<org.apache.lucene.search.Query> iterator()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
meth public void add(java.util.Collection<org.apache.lucene.search.Query>)
 anno 0 java.lang.Deprecated()
meth public void add(org.apache.lucene.search.Query)
 anno 0 java.lang.Deprecated()
supr org.apache.lucene.search.Query
hfds disjuncts,tieBreakerMultiplier

CLSS protected org.apache.lucene.search.DisjunctionMaxQuery$DisjunctionMaxWeight
 outer org.apache.lucene.search.DisjunctionMaxQuery
cons public init(org.apache.lucene.search.DisjunctionMaxQuery,org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
fld protected final java.util.ArrayList<org.apache.lucene.search.Weight> weights
meth public float getValueForNormalization() throws java.io.IOException
meth public org.apache.lucene.search.Explanation explain(org.apache.lucene.index.LeafReaderContext,int) throws java.io.IOException
meth public org.apache.lucene.search.Scorer scorer(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public void extractTerms(java.util.Set<org.apache.lucene.index.Term>)
meth public void normalize(float,float)
supr org.apache.lucene.search.Weight
hfds needsScores

CLSS public abstract org.apache.lucene.search.DocIdSet
cons public init()
fld public final static org.apache.lucene.search.DocIdSet EMPTY
intf org.apache.lucene.util.Accountable
meth public abstract org.apache.lucene.search.DocIdSetIterator iterator() throws java.io.IOException
meth public boolean isCacheable()
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public org.apache.lucene.util.Bits bits() throws java.io.IOException
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
supr java.lang.Object

CLSS public final org.apache.lucene.search.DocTermOrdsRewriteMethod
 anno 0 java.lang.Deprecated()
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader,org.apache.lucene.search.MultiTermQuery)
supr org.apache.lucene.search.MultiTermQuery$RewriteMethod
hfds rewriteMethod

CLSS public abstract org.apache.lucene.search.DocValuesDocIdSet
cons public init(int,org.apache.lucene.util.Bits)
fld protected final int maxDoc
fld protected final org.apache.lucene.util.Bits acceptDocs
meth protected abstract boolean matchDoc(int)
meth public final org.apache.lucene.search.DocIdSetIterator iterator() throws java.io.IOException
meth public final org.apache.lucene.util.Bits bits()
meth public long ramBytesUsed()
supr org.apache.lucene.search.DocIdSet

CLSS public final org.apache.lucene.search.DocValuesRewriteMethod
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader,org.apache.lucene.search.MultiTermQuery)
supr org.apache.lucene.search.MultiTermQuery$RewriteMethod
hcls MultiTermQueryDocValuesWrapper

CLSS public final org.apache.lucene.search.Explanation
meth public !varargs static org.apache.lucene.search.Explanation match(float,java.lang.String,org.apache.lucene.search.Explanation[])
meth public !varargs static org.apache.lucene.search.Explanation noMatch(java.lang.String,org.apache.lucene.search.Explanation[])
meth public boolean isMatch()
meth public float getValue()
meth public java.lang.String getDescription()
meth public java.lang.String toHtml()
meth public java.lang.String toString()
meth public org.apache.lucene.search.Explanation[] getDetails()
meth public static org.apache.lucene.search.Explanation match(float,java.lang.String,java.util.Collection<org.apache.lucene.search.Explanation>)
meth public static org.apache.lucene.search.Explanation noMatch(java.lang.String,java.util.Collection<org.apache.lucene.search.Explanation>)
supr java.lang.Object
hfds description,details,match,value

CLSS public abstract org.apache.lucene.search.FieldComparator<%0 extends java.lang.Object>
cons public init()
innr public abstract static NumericComparator
innr public final static DocComparator
innr public final static RelevanceComparator
innr public static DoubleComparator
innr public static FloatComparator
innr public static IntComparator
innr public static LongComparator
innr public static TermOrdValComparator
innr public static TermValComparator
meth public abstract int compare(int,int)
meth public abstract org.apache.lucene.search.LeafFieldComparator getLeafComparator(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public abstract void setTopValue({org.apache.lucene.search.FieldComparator%0})
meth public abstract {org.apache.lucene.search.FieldComparator%0} value(int)
meth public int compareValues({org.apache.lucene.search.FieldComparator%0},{org.apache.lucene.search.FieldComparator%0})
supr java.lang.Object

CLSS public final static org.apache.lucene.search.FieldComparator$DocComparator
 outer org.apache.lucene.search.FieldComparator
cons public init(int)
intf org.apache.lucene.search.LeafFieldComparator
meth public int compare(int,int)
meth public int compareBottom(int)
meth public int compareTop(int)
meth public java.lang.Integer value(int)
meth public org.apache.lucene.search.LeafFieldComparator getLeafComparator(org.apache.lucene.index.LeafReaderContext)
meth public void copy(int,int)
meth public void setBottom(int)
meth public void setScorer(org.apache.lucene.search.Scorer)
meth public void setTopValue(java.lang.Integer)
supr org.apache.lucene.search.FieldComparator<java.lang.Integer>
hfds bottom,docBase,docIDs,topValue

CLSS public static org.apache.lucene.search.FieldComparator$DoubleComparator
 outer org.apache.lucene.search.FieldComparator
cons public init(int,java.lang.String,java.lang.Double)
meth public int compare(int,int)
meth public int compareBottom(int)
meth public int compareTop(int)
meth public java.lang.Double value(int)
meth public void copy(int,int)
meth public void setBottom(int)
meth public void setTopValue(java.lang.Double)
supr org.apache.lucene.search.FieldComparator$NumericComparator<java.lang.Double>
hfds bottom,topValue,values

CLSS public static org.apache.lucene.search.FieldComparator$FloatComparator
 outer org.apache.lucene.search.FieldComparator
cons public init(int,java.lang.String,java.lang.Float)
meth public int compare(int,int)
meth public int compareBottom(int)
meth public int compareTop(int)
meth public java.lang.Float value(int)
meth public void copy(int,int)
meth public void setBottom(int)
meth public void setTopValue(java.lang.Float)
supr org.apache.lucene.search.FieldComparator$NumericComparator<java.lang.Float>
hfds bottom,topValue,values

CLSS public static org.apache.lucene.search.FieldComparator$IntComparator
 outer org.apache.lucene.search.FieldComparator
cons public init(int,java.lang.String,java.lang.Integer)
meth public int compare(int,int)
meth public int compareBottom(int)
meth public int compareTop(int)
meth public java.lang.Integer value(int)
meth public void copy(int,int)
meth public void setBottom(int)
meth public void setTopValue(java.lang.Integer)
supr org.apache.lucene.search.FieldComparator$NumericComparator<java.lang.Integer>
hfds bottom,topValue,values

CLSS public static org.apache.lucene.search.FieldComparator$LongComparator
 outer org.apache.lucene.search.FieldComparator
cons public init(int,java.lang.String,java.lang.Long)
meth public int compare(int,int)
meth public int compareBottom(int)
meth public int compareTop(int)
meth public java.lang.Long value(int)
meth public void copy(int,int)
meth public void setBottom(int)
meth public void setTopValue(java.lang.Long)
supr org.apache.lucene.search.FieldComparator$NumericComparator<java.lang.Long>
hfds bottom,topValue,values

CLSS public abstract static org.apache.lucene.search.FieldComparator$NumericComparator<%0 extends java.lang.Number>
 outer org.apache.lucene.search.FieldComparator
cons public init(java.lang.String,{org.apache.lucene.search.FieldComparator$NumericComparator%0})
fld protected final java.lang.String field
fld protected final {org.apache.lucene.search.FieldComparator$NumericComparator%0} missingValue
fld protected org.apache.lucene.index.NumericDocValues currentReaderValues
fld protected org.apache.lucene.util.Bits docsWithField
meth protected org.apache.lucene.index.NumericDocValues getNumericDocValues(org.apache.lucene.index.LeafReaderContext,java.lang.String) throws java.io.IOException
meth protected org.apache.lucene.util.Bits getDocsWithValue(org.apache.lucene.index.LeafReaderContext,java.lang.String) throws java.io.IOException
meth protected void doSetNextReader(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
supr org.apache.lucene.search.SimpleFieldComparator<{org.apache.lucene.search.FieldComparator$NumericComparator%0}>

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
meth public void setScorer(org.apache.lucene.search.Scorer)
meth public void setTopValue(java.lang.Float)
supr org.apache.lucene.search.FieldComparator<java.lang.Float>
hfds bottom,scorer,scores,topValue

CLSS public static org.apache.lucene.search.FieldComparator$TermOrdValComparator
 outer org.apache.lucene.search.FieldComparator
cons public init(int,java.lang.String)
cons public init(int,java.lang.String,boolean)
intf org.apache.lucene.search.LeafFieldComparator
meth protected org.apache.lucene.index.SortedDocValues getSortedDocValues(org.apache.lucene.index.LeafReaderContext,java.lang.String) throws java.io.IOException
meth public int compare(int,int)
meth public int compareBottom(int)
meth public int compareTop(int)
meth public int compareValues(org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef)
meth public org.apache.lucene.search.LeafFieldComparator getLeafComparator(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public org.apache.lucene.util.BytesRef value(int)
meth public void copy(int,int)
meth public void setBottom(int)
meth public void setScorer(org.apache.lucene.search.Scorer)
meth public void setTopValue(org.apache.lucene.util.BytesRef)
supr org.apache.lucene.search.FieldComparator<org.apache.lucene.util.BytesRef>
hfds bottomOrd,bottomSameReader,bottomSlot,bottomValue,currentReaderGen,field,missingOrd,missingSortCmp,ords,readerGen,tempBRs,termsIndex,topOrd,topSameReader,topValue,values

CLSS public static org.apache.lucene.search.FieldComparator$TermValComparator
 outer org.apache.lucene.search.FieldComparator
cons public init(int,java.lang.String,boolean)
intf org.apache.lucene.search.LeafFieldComparator
meth protected boolean isNull(int,org.apache.lucene.util.BytesRef)
meth protected org.apache.lucene.index.BinaryDocValues getBinaryDocValues(org.apache.lucene.index.LeafReaderContext,java.lang.String) throws java.io.IOException
meth protected org.apache.lucene.util.Bits getDocsWithField(org.apache.lucene.index.LeafReaderContext,java.lang.String) throws java.io.IOException
meth public int compare(int,int)
meth public int compareBottom(int)
meth public int compareTop(int)
meth public int compareValues(org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef)
meth public org.apache.lucene.search.LeafFieldComparator getLeafComparator(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public org.apache.lucene.util.BytesRef value(int)
meth public void copy(int,int)
meth public void setBottom(int)
meth public void setScorer(org.apache.lucene.search.Scorer)
meth public void setTopValue(org.apache.lucene.util.BytesRef)
supr org.apache.lucene.search.FieldComparator<org.apache.lucene.util.BytesRef>
hfds bottom,docTerms,docsWithField,field,missingSortCmp,tempBRs,topValue,values

CLSS public abstract org.apache.lucene.search.FieldComparatorSource
cons public init()
meth public abstract org.apache.lucene.search.FieldComparator<?> newComparator(java.lang.String,int,int,boolean) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.lucene.search.FieldDoc
cons public init(int,float)
cons public init(int,float,java.lang.Object[])
cons public init(int,float,java.lang.Object[],int)
fld public java.lang.Object[] fields
meth public java.lang.String toString()
supr org.apache.lucene.search.ScoreDoc

CLSS public org.apache.lucene.search.FieldValueFilter
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
meth public boolean equals(java.lang.Object)
meth public boolean negate()
meth public int hashCode()
meth public java.lang.String field()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.DocIdSet getDocIdSet(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.util.Bits) throws java.io.IOException
supr org.apache.lucene.search.Filter
hfds field,negate

CLSS public abstract org.apache.lucene.search.FieldValueHitQueue<%0 extends org.apache.lucene.search.FieldValueHitQueue$Entry>
fld protected final int[] reverseMul
fld protected final org.apache.lucene.search.FieldComparator<?>[] comparators
fld protected final org.apache.lucene.search.SortField[] fields
innr public static Entry
meth protected abstract boolean lessThan(org.apache.lucene.search.FieldValueHitQueue$Entry,org.apache.lucene.search.FieldValueHitQueue$Entry)
meth public int[] getReverseMul()
meth public org.apache.lucene.search.FieldComparator<?>[] getComparators()
meth public org.apache.lucene.search.LeafFieldComparator[] getComparators(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public static <%0 extends org.apache.lucene.search.FieldValueHitQueue$Entry> org.apache.lucene.search.FieldValueHitQueue<{%%0}> create(org.apache.lucene.search.SortField[],int) throws java.io.IOException
supr org.apache.lucene.util.PriorityQueue<{org.apache.lucene.search.FieldValueHitQueue%0}>
hcls MultiComparatorsFieldValueHitQueue,OneComparatorFieldValueHitQueue

CLSS public static org.apache.lucene.search.FieldValueHitQueue$Entry
 outer org.apache.lucene.search.FieldValueHitQueue
cons public init(int,int,float)
fld public int slot
meth public java.lang.String toString()
supr org.apache.lucene.search.ScoreDoc

CLSS public final org.apache.lucene.search.FieldValueQuery
cons public init(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
supr org.apache.lucene.search.Query
hfds field

CLSS public abstract org.apache.lucene.search.Filter
 anno 0 java.lang.Deprecated()
cons public init()
meth public abstract org.apache.lucene.search.DocIdSet getDocIdSet(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.util.Bits) throws java.io.IOException
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
supr org.apache.lucene.search.Query

CLSS public abstract interface org.apache.lucene.search.FilterCache
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.lucene.search.Filter doCache(org.apache.lucene.search.Filter,org.apache.lucene.search.FilterCachingPolicy)

CLSS public abstract interface org.apache.lucene.search.FilterCachingPolicy
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.search.FilterCachingPolicy ALWAYS_CACHE
innr public static CacheOnLargeSegments
meth public abstract boolean shouldCache(org.apache.lucene.search.Filter,org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.DocIdSet) throws java.io.IOException
meth public abstract void onUse(org.apache.lucene.search.Filter)

CLSS public static org.apache.lucene.search.FilterCachingPolicy$CacheOnLargeSegments
 outer org.apache.lucene.search.FilterCachingPolicy
cons public init(float)
fld public final static org.apache.lucene.search.FilterCachingPolicy$CacheOnLargeSegments DEFAULT
intf org.apache.lucene.search.FilterCachingPolicy
meth public boolean shouldCache(org.apache.lucene.search.Filter,org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.DocIdSet) throws java.io.IOException
meth public void onUse(org.apache.lucene.search.Filter)
supr java.lang.Object
hfds minSizeRatio

CLSS public org.apache.lucene.search.FilterCollector
cons public init(org.apache.lucene.search.Collector)
fld protected final org.apache.lucene.search.Collector in
intf org.apache.lucene.search.Collector
meth public boolean needsScores()
meth public java.lang.String toString()
meth public org.apache.lucene.search.LeafCollector getLeafCollector(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.lucene.search.FilterLeafCollector
cons public init(org.apache.lucene.search.LeafCollector)
fld protected final org.apache.lucene.search.LeafCollector in
intf org.apache.lucene.search.LeafCollector
meth public java.lang.String toString()
meth public void collect(int) throws java.io.IOException
meth public void setScorer(org.apache.lucene.search.Scorer) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.FilterScorer
cons public init(org.apache.lucene.search.Scorer)
cons public init(org.apache.lucene.search.Scorer,org.apache.lucene.search.Weight)
fld protected final org.apache.lucene.search.Scorer in
meth public final int docID()
meth public final org.apache.lucene.search.DocIdSetIterator iterator()
meth public final org.apache.lucene.search.TwoPhaseIterator twoPhaseIterator()
meth public float score() throws java.io.IOException
meth public int freq() throws java.io.IOException
supr org.apache.lucene.search.Scorer

CLSS public abstract org.apache.lucene.search.FilteredDocIdSet
 anno 0 java.lang.Deprecated()
cons public init(org.apache.lucene.search.DocIdSet)
meth protected abstract boolean match(int)
meth public boolean isCacheable()
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public long ramBytesUsed()
meth public org.apache.lucene.search.DocIdSet getDelegate()
meth public org.apache.lucene.search.DocIdSetIterator iterator() throws java.io.IOException
meth public org.apache.lucene.util.Bits bits() throws java.io.IOException
supr org.apache.lucene.search.DocIdSet
hfds _innerSet

CLSS public abstract org.apache.lucene.search.FilteredDocIdSetIterator
cons public init(org.apache.lucene.search.DocIdSetIterator)
fld protected org.apache.lucene.search.DocIdSetIterator _innerIter
meth protected abstract boolean match(int)
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int nextDoc() throws java.io.IOException
meth public long cost()
meth public org.apache.lucene.search.DocIdSetIterator getDelegate()
supr org.apache.lucene.search.DocIdSetIterator
hfds doc

CLSS public org.apache.lucene.search.FilteredQuery
 anno 0 java.lang.Deprecated()
cons public init(org.apache.lucene.search.Query,org.apache.lucene.search.Filter)
cons public init(org.apache.lucene.search.Query,org.apache.lucene.search.Filter,org.apache.lucene.search.FilteredQuery$FilterStrategy)
fld public final static org.apache.lucene.search.FilteredQuery$FilterStrategy LEAP_FROG_FILTER_FIRST_STRATEGY
fld public final static org.apache.lucene.search.FilteredQuery$FilterStrategy LEAP_FROG_QUERY_FIRST_STRATEGY
fld public final static org.apache.lucene.search.FilteredQuery$FilterStrategy QUERY_FIRST_FILTER_STRATEGY
fld public final static org.apache.lucene.search.FilteredQuery$FilterStrategy RANDOM_ACCESS_FILTER_STRATEGY
innr public abstract static FilterStrategy
innr public static RandomAccessFilterStrategy
meth public boolean equals(java.lang.Object)
meth public final org.apache.lucene.search.Filter getFilter()
meth public final org.apache.lucene.search.Query getQuery()
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.FilteredQuery$FilterStrategy getFilterStrategy()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
supr org.apache.lucene.search.Query
hfds filter,query,strategy
hcls RandomAccessFilterWrapperQuery

CLSS public abstract static org.apache.lucene.search.FilteredQuery$FilterStrategy
 outer org.apache.lucene.search.FilteredQuery
cons public init()
meth public abstract org.apache.lucene.search.Query rewrite(org.apache.lucene.search.Filter)
supr java.lang.Object

CLSS public static org.apache.lucene.search.FilteredQuery$RandomAccessFilterStrategy
 outer org.apache.lucene.search.FilteredQuery
cons public init()
meth protected boolean useRandomAccess(org.apache.lucene.util.Bits,long)
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.Filter)
supr org.apache.lucene.search.FilteredQuery$FilterStrategy

CLSS public org.apache.lucene.search.FuzzyQuery
cons public init(org.apache.lucene.index.Term)
cons public init(org.apache.lucene.index.Term,int)
cons public init(org.apache.lucene.index.Term,int,int)
cons public init(org.apache.lucene.index.Term,int,int,int,boolean)
fld public final static boolean defaultTranspositions = true
fld public final static float defaultMinSimilarity = 2.0
 anno 0 java.lang.Deprecated()
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
meth public static int floatToEdits(float,int)
 anno 0 java.lang.Deprecated()
supr org.apache.lucene.search.MultiTermQuery
hfds maxEdits,maxExpansions,prefixLength,term,transpositions

CLSS public org.apache.lucene.search.FuzzyTermsEnum
cons public init(org.apache.lucene.index.Terms,org.apache.lucene.util.AttributeSource,org.apache.lucene.index.Term,float,int,boolean) throws java.io.IOException
fld protected final boolean raw
fld protected final float minSimilarity
fld protected final float scale_factor
fld protected final int realPrefixLength
fld protected final int termLength
fld protected final int[] termText
fld protected final org.apache.lucene.index.Terms terms
fld protected int maxEdits
innr public abstract interface static LevenshteinAutomataAttribute
innr public final static LevenshteinAutomataAttributeImpl
meth protected org.apache.lucene.index.TermsEnum getAutomatonEnum(int,org.apache.lucene.util.BytesRef) throws java.io.IOException
meth protected void maxEditDistanceChanged(org.apache.lucene.util.BytesRef,int,boolean) throws java.io.IOException
meth protected void setEnum(org.apache.lucene.index.TermsEnum)
meth public boolean seekExact(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public float getMinSimilarity()
meth public float getScaleFactor()
meth public int docFreq() throws java.io.IOException
meth public long ord() throws java.io.IOException
meth public long totalTermFreq() throws java.io.IOException
meth public org.apache.lucene.index.PostingsEnum postings(org.apache.lucene.index.PostingsEnum,int) throws java.io.IOException
meth public org.apache.lucene.index.TermState termState() throws java.io.IOException
meth public org.apache.lucene.index.TermsEnum$SeekStatus seekCeil(org.apache.lucene.util.BytesRef) throws java.io.IOException
meth public org.apache.lucene.util.BytesRef next() throws java.io.IOException
meth public org.apache.lucene.util.BytesRef term() throws java.io.IOException
meth public void seekExact(long) throws java.io.IOException
meth public void seekExact(org.apache.lucene.util.BytesRef,org.apache.lucene.index.TermState) throws java.io.IOException
supr org.apache.lucene.index.TermsEnum
hfds actualBoostAtt,actualEnum,boostAtt,bottom,bottomTerm,dfaAtt,maxBoostAtt,queuedBottom,term,termComparator,transpositions
hcls AutomatonFuzzyTermsEnum

CLSS public abstract interface static org.apache.lucene.search.FuzzyTermsEnum$LevenshteinAutomataAttribute
 outer org.apache.lucene.search.FuzzyTermsEnum
intf org.apache.lucene.util.Attribute
meth public abstract java.util.List<org.apache.lucene.util.automaton.CompiledAutomaton> automata()

CLSS public final static org.apache.lucene.search.FuzzyTermsEnum$LevenshteinAutomataAttributeImpl
 outer org.apache.lucene.search.FuzzyTermsEnum
cons public init()
intf org.apache.lucene.search.FuzzyTermsEnum$LevenshteinAutomataAttribute
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.util.List<org.apache.lucene.util.automaton.CompiledAutomaton> automata()
meth public void clear()
meth public void copyTo(org.apache.lucene.util.AttributeImpl)
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
supr org.apache.lucene.util.AttributeImpl
hfds automata

CLSS public org.apache.lucene.search.IndexSearcher
cons public init(org.apache.lucene.index.IndexReader)
cons public init(org.apache.lucene.index.IndexReader,java.util.concurrent.ExecutorService)
cons public init(org.apache.lucene.index.IndexReaderContext)
cons public init(org.apache.lucene.index.IndexReaderContext,java.util.concurrent.ExecutorService)
fld protected final java.util.List<org.apache.lucene.index.LeafReaderContext> leafContexts
fld protected final org.apache.lucene.index.IndexReaderContext readerContext
fld protected final org.apache.lucene.search.IndexSearcher$LeafSlice[] leafSlices
innr public static LeafSlice
meth protected org.apache.lucene.search.Explanation explain(org.apache.lucene.search.Weight,int) throws java.io.IOException
meth protected org.apache.lucene.search.IndexSearcher$LeafSlice[] slices(java.util.List<org.apache.lucene.index.LeafReaderContext>)
meth protected org.apache.lucene.search.Query wrapFilter(org.apache.lucene.search.Query,org.apache.lucene.search.Filter)
 anno 0 java.lang.Deprecated()
meth protected void search(java.util.List<org.apache.lucene.index.LeafReaderContext>,org.apache.lucene.search.Weight,org.apache.lucene.search.Collector) throws java.io.IOException
meth public <%0 extends org.apache.lucene.search.Collector, %1 extends java.lang.Object> {%%1} search(org.apache.lucene.search.Query,org.apache.lucene.search.CollectorManager<{%%0},{%%1}>) throws java.io.IOException
meth public final org.apache.lucene.search.TopDocs search(org.apache.lucene.search.Query,org.apache.lucene.search.Filter,int) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public final org.apache.lucene.search.TopDocs searchAfter(org.apache.lucene.search.ScoreDoc,org.apache.lucene.search.Query,org.apache.lucene.search.Filter,int) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public final org.apache.lucene.search.TopFieldDocs search(org.apache.lucene.search.Query,int,org.apache.lucene.search.Sort,boolean,boolean) throws java.io.IOException
meth public final org.apache.lucene.search.TopFieldDocs search(org.apache.lucene.search.Query,org.apache.lucene.search.Filter,int,org.apache.lucene.search.Sort) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public final org.apache.lucene.search.TopFieldDocs search(org.apache.lucene.search.Query,org.apache.lucene.search.Filter,int,org.apache.lucene.search.Sort,boolean,boolean) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public final org.apache.lucene.search.TopFieldDocs searchAfter(org.apache.lucene.search.ScoreDoc,org.apache.lucene.search.Query,int,org.apache.lucene.search.Sort,boolean,boolean) throws java.io.IOException
meth public final org.apache.lucene.search.TopFieldDocs searchAfter(org.apache.lucene.search.ScoreDoc,org.apache.lucene.search.Query,org.apache.lucene.search.Filter,int,org.apache.lucene.search.Sort) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public final org.apache.lucene.search.TopFieldDocs searchAfter(org.apache.lucene.search.ScoreDoc,org.apache.lucene.search.Query,org.apache.lucene.search.Filter,int,org.apache.lucene.search.Sort,boolean,boolean) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public final void search(org.apache.lucene.search.Query,org.apache.lucene.search.Filter,org.apache.lucene.search.Collector) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public int count(org.apache.lucene.search.Query) throws java.io.IOException
meth public java.lang.String toString()
meth public org.apache.lucene.document.Document doc(int) throws java.io.IOException
meth public org.apache.lucene.document.Document doc(int,java.util.Set<java.lang.String>) throws java.io.IOException
meth public org.apache.lucene.index.IndexReader getIndexReader()
meth public org.apache.lucene.index.IndexReaderContext getTopReaderContext()
meth public org.apache.lucene.search.CollectionStatistics collectionStatistics(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.search.Explanation explain(org.apache.lucene.search.Query,int) throws java.io.IOException
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.search.Query) throws java.io.IOException
meth public org.apache.lucene.search.QueryCache getQueryCache()
meth public org.apache.lucene.search.QueryCachingPolicy getQueryCachingPolicy()
meth public org.apache.lucene.search.TermStatistics termStatistics(org.apache.lucene.index.Term,org.apache.lucene.index.TermContext) throws java.io.IOException
meth public org.apache.lucene.search.TopDocs search(org.apache.lucene.search.Query,int) throws java.io.IOException
meth public org.apache.lucene.search.TopDocs searchAfter(org.apache.lucene.search.ScoreDoc,org.apache.lucene.search.Query,int) throws java.io.IOException
meth public org.apache.lucene.search.TopDocs searchAfter(org.apache.lucene.search.ScoreDoc,org.apache.lucene.search.Query,int,org.apache.lucene.search.Sort) throws java.io.IOException
meth public org.apache.lucene.search.TopFieldDocs search(org.apache.lucene.search.Query,int,org.apache.lucene.search.Sort) throws java.io.IOException
meth public org.apache.lucene.search.Weight createNormalizedWeight(org.apache.lucene.search.Query,boolean) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.Query,boolean) throws java.io.IOException
meth public org.apache.lucene.search.similarities.Similarity getSimilarity(boolean)
meth public static org.apache.lucene.search.QueryCache getDefaultQueryCache()
meth public static org.apache.lucene.search.QueryCachingPolicy getDefaultQueryCachingPolicy()
meth public static org.apache.lucene.search.similarities.Similarity getDefaultSimilarity()
meth public static void setDefaultQueryCache(org.apache.lucene.search.QueryCache)
meth public static void setDefaultQueryCachingPolicy(org.apache.lucene.search.QueryCachingPolicy)
meth public void doc(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
meth public void search(org.apache.lucene.search.Query,org.apache.lucene.search.Collector) throws java.io.IOException
meth public void setQueryCache(org.apache.lucene.search.QueryCache)
meth public void setQueryCachingPolicy(org.apache.lucene.search.QueryCachingPolicy)
meth public void setSimilarity(org.apache.lucene.search.similarities.Similarity)
supr java.lang.Object
hfds DEFAULT_CACHING_POLICY,DEFAULT_QUERY_CACHE,NON_SCORING_SIMILARITY,defaultSimilarity,executor,queryCache,queryCachingPolicy,reader,similarity

CLSS public static org.apache.lucene.search.IndexSearcher$LeafSlice
 outer org.apache.lucene.search.IndexSearcher
cons public !varargs init(org.apache.lucene.index.LeafReaderContext[])
supr java.lang.Object
hfds leaves

CLSS public org.apache.lucene.search.LRUFilterCache
 anno 0 java.lang.Deprecated()
cons public init(int,long)
intf org.apache.lucene.search.FilterCache
intf org.apache.lucene.util.Accountable
meth protected long ramBytesUsed(org.apache.lucene.search.Filter)
meth protected org.apache.lucene.search.DocIdSet cacheImpl(org.apache.lucene.search.DocIdSetIterator,org.apache.lucene.index.LeafReader) throws java.io.IOException
meth protected org.apache.lucene.search.DocIdSet docIdSetToCache(org.apache.lucene.search.DocIdSet,org.apache.lucene.index.LeafReader) throws java.io.IOException
meth protected void onClear()
meth protected void onDocIdSetCache(java.lang.Object,long)
meth protected void onDocIdSetEviction(java.lang.Object,int,long)
meth protected void onFilterCache(org.apache.lucene.search.Filter,long)
meth protected void onFilterEviction(org.apache.lucene.search.Filter,long)
meth protected void onHit(java.lang.Object,org.apache.lucene.search.Filter)
meth protected void onMiss(java.lang.Object,org.apache.lucene.search.Filter)
meth public final long getCacheCount()
meth public final long getCacheSize()
meth public final long getEvictionCount()
meth public final long getHitCount()
meth public final long getMissCount()
meth public final long getTotalCount()
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public long ramBytesUsed()
meth public org.apache.lucene.search.Filter doCache(org.apache.lucene.search.Filter,org.apache.lucene.search.FilterCachingPolicy)
meth public void clear()
meth public void clearCoreCacheKey(java.lang.Object)
meth public void clearFilter(org.apache.lucene.search.Filter)
supr java.lang.Object
hfds FILTER_DEFAULT_RAM_BYTES_USED,HASHTABLE_RAM_BYTES_PER_ENTRY,LINKED_HASHTABLE_RAM_BYTES_PER_ENTRY,cache,cacheCount,cacheSize,hitCount,maxRamBytesUsed,maxSize,missCount,mostRecentlyUsedFilters,ramBytesUsed,uniqueFilters
hcls CachingWrapperFilter,LeafCache

CLSS public org.apache.lucene.search.LRUQueryCache
cons public init(int,long)
intf org.apache.lucene.search.QueryCache
intf org.apache.lucene.util.Accountable
meth protected long ramBytesUsed(org.apache.lucene.search.Query)
meth protected org.apache.lucene.search.DocIdSet cacheImpl(org.apache.lucene.search.BulkScorer,int) throws java.io.IOException
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
hfds HASHTABLE_RAM_BYTES_PER_ENTRY,LINKED_HASHTABLE_RAM_BYTES_PER_ENTRY,QUERY_DEFAULT_RAM_BYTES_USED,cache,cacheCount,cacheSize,hitCount,maxRamBytesUsed,maxSize,missCount,mostRecentlyUsedQueries,ramBytesUsed,uniqueQueries
hcls CachingWrapperWeight,LeafCache

CLSS public abstract interface org.apache.lucene.search.LeafCollector
meth public abstract void collect(int) throws java.io.IOException
meth public abstract void setScorer(org.apache.lucene.search.Scorer) throws java.io.IOException

CLSS public abstract interface org.apache.lucene.search.LeafFieldComparator
meth public abstract int compareBottom(int) throws java.io.IOException
meth public abstract int compareTop(int) throws java.io.IOException
meth public abstract void copy(int,int) throws java.io.IOException
meth public abstract void setBottom(int)
meth public abstract void setScorer(org.apache.lucene.search.Scorer)

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

CLSS public final org.apache.lucene.search.MatchAllDocsQuery
cons public init()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,boolean)
supr org.apache.lucene.search.Query

CLSS public org.apache.lucene.search.MatchNoDocsQuery
cons public init()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
supr org.apache.lucene.search.Query

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

CLSS public org.apache.lucene.search.MultiCollector
intf org.apache.lucene.search.Collector
meth public !varargs static org.apache.lucene.search.Collector wrap(org.apache.lucene.search.Collector[])
meth public boolean needsScores()
meth public org.apache.lucene.search.LeafCollector getLeafCollector(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public static org.apache.lucene.search.Collector wrap(java.lang.Iterable<? extends org.apache.lucene.search.Collector>)
supr java.lang.Object
hfds cacheScores,collectors
hcls MultiLeafCollector

CLSS public org.apache.lucene.search.MultiPhraseQuery
cons public init()
meth public boolean equals(java.lang.Object)
meth public final java.lang.String toString(java.lang.String)
meth public int getSlop()
meth public int hashCode()
meth public int[] getPositions()
meth public java.util.List<org.apache.lucene.index.Term[]> getTermArrays()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
meth public void add(org.apache.lucene.index.Term)
meth public void add(org.apache.lucene.index.Term[])
meth public void add(org.apache.lucene.index.Term[],int)
meth public void setSlop(int)
supr org.apache.lucene.search.Query
hfds field,positions,slop,termArrays
hcls MultiPhraseWeight,UnionPostingsEnum

CLSS public abstract org.apache.lucene.search.MultiTermQuery
cons public init(java.lang.String)
fld protected final java.lang.String field
fld protected org.apache.lucene.search.MultiTermQuery$RewriteMethod rewriteMethod
fld public final static org.apache.lucene.search.MultiTermQuery$RewriteMethod CONSTANT_SCORE_BOOLEAN_QUERY_REWRITE
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.search.MultiTermQuery$RewriteMethod CONSTANT_SCORE_BOOLEAN_REWRITE
fld public final static org.apache.lucene.search.MultiTermQuery$RewriteMethod CONSTANT_SCORE_FILTER_REWRITE
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.search.MultiTermQuery$RewriteMethod CONSTANT_SCORE_REWRITE
fld public final static org.apache.lucene.search.MultiTermQuery$RewriteMethod SCORING_BOOLEAN_QUERY_REWRITE
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.search.MultiTermQuery$RewriteMethod SCORING_BOOLEAN_REWRITE
innr public abstract static RewriteMethod
innr public final static TopTermsBlendedFreqScoringRewrite
innr public final static TopTermsBoostOnlyBooleanQueryRewrite
innr public final static TopTermsScoringBooleanQueryRewrite
meth protected abstract org.apache.lucene.index.TermsEnum getTermsEnum(org.apache.lucene.index.Terms,org.apache.lucene.util.AttributeSource) throws java.io.IOException
meth protected final org.apache.lucene.index.TermsEnum getTermsEnum(org.apache.lucene.index.Terms) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public final java.lang.String getField()
meth public final org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public int hashCode()
meth public org.apache.lucene.search.MultiTermQuery$RewriteMethod getRewriteMethod()
meth public void setRewriteMethod(org.apache.lucene.search.MultiTermQuery$RewriteMethod)
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
meth protected void addClause(org.apache.lucene.search.BlendedTermQuery$Builder,org.apache.lucene.index.Term,int,float,org.apache.lucene.index.TermContext)
supr org.apache.lucene.search.TopTermsRewrite<org.apache.lucene.search.BlendedTermQuery$Builder>

CLSS public final static org.apache.lucene.search.MultiTermQuery$TopTermsBoostOnlyBooleanQueryRewrite
 outer org.apache.lucene.search.MultiTermQuery
cons public init(int)
meth protected int getMaxSize()
meth protected org.apache.lucene.search.BooleanQuery$Builder getTopLevelBuilder()
meth protected org.apache.lucene.search.Query build(org.apache.lucene.search.BooleanQuery$Builder)
meth protected void addClause(org.apache.lucene.search.BooleanQuery$Builder,org.apache.lucene.index.Term,int,float,org.apache.lucene.index.TermContext)
supr org.apache.lucene.search.TopTermsRewrite<org.apache.lucene.search.BooleanQuery$Builder>

CLSS public final static org.apache.lucene.search.MultiTermQuery$TopTermsScoringBooleanQueryRewrite
 outer org.apache.lucene.search.MultiTermQuery
cons public init(int)
meth protected int getMaxSize()
meth protected org.apache.lucene.search.BooleanQuery$Builder getTopLevelBuilder()
meth protected org.apache.lucene.search.Query build(org.apache.lucene.search.BooleanQuery$Builder)
meth protected void addClause(org.apache.lucene.search.BooleanQuery$Builder,org.apache.lucene.index.Term,int,float,org.apache.lucene.index.TermContext)
supr org.apache.lucene.search.TopTermsRewrite<org.apache.lucene.search.BooleanQuery$Builder>

CLSS public org.apache.lucene.search.MultiTermQueryWrapperFilter<%0 extends org.apache.lucene.search.MultiTermQuery>
 anno 0 java.lang.Deprecated()
cons protected init({org.apache.lucene.search.MultiTermQueryWrapperFilter%0})
fld protected final {org.apache.lucene.search.MultiTermQueryWrapperFilter%0} query
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final java.lang.String getField()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.DocIdSet getDocIdSet(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.util.Bits) throws java.io.IOException
supr org.apache.lucene.search.Filter

CLSS public org.apache.lucene.search.NGramPhraseQuery
cons public init(int,org.apache.lucene.search.PhraseQuery)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public int[] getPositions()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.index.Term[] getTerms()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
supr org.apache.lucene.search.Query
hfds n,phraseQuery

CLSS public final org.apache.lucene.search.NumericRangeFilter<%0 extends java.lang.Number>
 anno 0 java.lang.Deprecated()
meth public boolean includesMax()
meth public boolean includesMin()
meth public int getPrecisionStep()
meth public static org.apache.lucene.search.NumericRangeFilter<java.lang.Double> newDoubleRange(java.lang.String,int,java.lang.Double,java.lang.Double,boolean,boolean)
meth public static org.apache.lucene.search.NumericRangeFilter<java.lang.Double> newDoubleRange(java.lang.String,java.lang.Double,java.lang.Double,boolean,boolean)
meth public static org.apache.lucene.search.NumericRangeFilter<java.lang.Float> newFloatRange(java.lang.String,int,java.lang.Float,java.lang.Float,boolean,boolean)
meth public static org.apache.lucene.search.NumericRangeFilter<java.lang.Float> newFloatRange(java.lang.String,java.lang.Float,java.lang.Float,boolean,boolean)
meth public static org.apache.lucene.search.NumericRangeFilter<java.lang.Integer> newIntRange(java.lang.String,int,java.lang.Integer,java.lang.Integer,boolean,boolean)
meth public static org.apache.lucene.search.NumericRangeFilter<java.lang.Integer> newIntRange(java.lang.String,java.lang.Integer,java.lang.Integer,boolean,boolean)
meth public static org.apache.lucene.search.NumericRangeFilter<java.lang.Long> newLongRange(java.lang.String,int,java.lang.Long,java.lang.Long,boolean,boolean)
meth public static org.apache.lucene.search.NumericRangeFilter<java.lang.Long> newLongRange(java.lang.String,java.lang.Long,java.lang.Long,boolean,boolean)
meth public {org.apache.lucene.search.NumericRangeFilter%0} getMax()
meth public {org.apache.lucene.search.NumericRangeFilter%0} getMin()
supr org.apache.lucene.search.MultiTermQueryWrapperFilter<org.apache.lucene.search.NumericRangeQuery<{org.apache.lucene.search.NumericRangeFilter%0}>>

CLSS public final org.apache.lucene.search.NumericRangeQuery<%0 extends java.lang.Number>
meth protected org.apache.lucene.index.TermsEnum getTermsEnum(org.apache.lucene.index.Terms,org.apache.lucene.util.AttributeSource) throws java.io.IOException
meth public boolean includesMax()
meth public boolean includesMin()
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public int getPrecisionStep()
meth public java.lang.String toString(java.lang.String)
meth public static org.apache.lucene.search.NumericRangeQuery<java.lang.Double> newDoubleRange(java.lang.String,int,java.lang.Double,java.lang.Double,boolean,boolean)
meth public static org.apache.lucene.search.NumericRangeQuery<java.lang.Double> newDoubleRange(java.lang.String,java.lang.Double,java.lang.Double,boolean,boolean)
meth public static org.apache.lucene.search.NumericRangeQuery<java.lang.Float> newFloatRange(java.lang.String,int,java.lang.Float,java.lang.Float,boolean,boolean)
meth public static org.apache.lucene.search.NumericRangeQuery<java.lang.Float> newFloatRange(java.lang.String,java.lang.Float,java.lang.Float,boolean,boolean)
meth public static org.apache.lucene.search.NumericRangeQuery<java.lang.Integer> newIntRange(java.lang.String,int,java.lang.Integer,java.lang.Integer,boolean,boolean)
meth public static org.apache.lucene.search.NumericRangeQuery<java.lang.Integer> newIntRange(java.lang.String,java.lang.Integer,java.lang.Integer,boolean,boolean)
meth public static org.apache.lucene.search.NumericRangeQuery<java.lang.Long> newLongRange(java.lang.String,int,java.lang.Long,java.lang.Long,boolean,boolean)
meth public static org.apache.lucene.search.NumericRangeQuery<java.lang.Long> newLongRange(java.lang.String,java.lang.Long,java.lang.Long,boolean,boolean)
meth public {org.apache.lucene.search.NumericRangeQuery%0} getMax()
meth public {org.apache.lucene.search.NumericRangeQuery%0} getMin()
supr org.apache.lucene.search.MultiTermQuery
hfds INT_NEGATIVE_INFINITY,INT_POSITIVE_INFINITY,LONG_NEGATIVE_INFINITY,LONG_POSITIVE_INFINITY,dataType,max,maxInclusive,min,minInclusive,precisionStep
hcls NumericRangeTermsEnum

CLSS public org.apache.lucene.search.PhraseQuery
cons public !varargs init(int,java.lang.String,java.lang.String[])
cons public !varargs init(int,java.lang.String,org.apache.lucene.util.BytesRef[])
cons public !varargs init(java.lang.String,java.lang.String[])
cons public !varargs init(java.lang.String,org.apache.lucene.util.BytesRef[])
cons public init()
 anno 0 java.lang.Deprecated()
innr public static Builder
meth public boolean equals(java.lang.Object)
meth public int getSlop()
meth public int hashCode()
meth public int[] getPositions()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.index.Term[] getTerms()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
meth public void add(org.apache.lucene.index.Term)
 anno 0 java.lang.Deprecated()
meth public void add(org.apache.lucene.index.Term,int)
 anno 0 java.lang.Deprecated()
meth public void setSlop(int)
 anno 0 java.lang.Deprecated()
supr org.apache.lucene.search.Query
hfds TERM_OPS_PER_POS,TERM_POSNS_SEEK_OPS_PER_DOC,field,mutable,positions,slop,terms
hcls PhraseWeight,PostingsAndFreq

CLSS public static org.apache.lucene.search.PhraseQuery$Builder
 outer org.apache.lucene.search.PhraseQuery
cons public init()
meth public org.apache.lucene.search.PhraseQuery build()
meth public org.apache.lucene.search.PhraseQuery$Builder add(org.apache.lucene.index.Term)
meth public org.apache.lucene.search.PhraseQuery$Builder add(org.apache.lucene.index.Term,int)
meth public org.apache.lucene.search.PhraseQuery$Builder setSlop(int)
supr java.lang.Object
hfds positions,slop,terms

CLSS public org.apache.lucene.search.PositiveScoresOnlyCollector
cons public init(org.apache.lucene.search.Collector)
meth public org.apache.lucene.search.LeafCollector getLeafCollector(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
supr org.apache.lucene.search.FilterCollector

CLSS public org.apache.lucene.search.PrefixFilter
 anno 0 java.lang.Deprecated()
cons public init(org.apache.lucene.index.Term)
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.index.Term getPrefix()
supr org.apache.lucene.search.MultiTermQueryWrapperFilter<org.apache.lucene.search.PrefixQuery>

CLSS public org.apache.lucene.search.PrefixQuery
cons public init(org.apache.lucene.index.Term)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.index.Term getPrefix()
meth public static org.apache.lucene.util.automaton.Automaton toAutomaton(org.apache.lucene.util.BytesRef)
supr org.apache.lucene.search.AutomatonQuery

CLSS public abstract org.apache.lucene.search.Query
cons public init()
intf java.lang.Cloneable
meth public abstract java.lang.String toString(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public final java.lang.String toString()
meth public float getBoost()
 anno 0 java.lang.Deprecated()
meth public int hashCode()
meth public org.apache.lucene.search.Query clone()
 anno 0 java.lang.Deprecated()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
meth public void setBoost(float)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds boost

CLSS public abstract interface org.apache.lucene.search.QueryCache
meth public abstract org.apache.lucene.search.Weight doCache(org.apache.lucene.search.Weight,org.apache.lucene.search.QueryCachingPolicy)

CLSS public abstract interface org.apache.lucene.search.QueryCachingPolicy
fld public final static org.apache.lucene.search.QueryCachingPolicy ALWAYS_CACHE
innr public static CacheOnLargeSegments
meth public abstract boolean shouldCache(org.apache.lucene.search.Query,org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public abstract void onUse(org.apache.lucene.search.Query)

CLSS public static org.apache.lucene.search.QueryCachingPolicy$CacheOnLargeSegments
 outer org.apache.lucene.search.QueryCachingPolicy
cons public init(int,float)
fld public final static org.apache.lucene.search.QueryCachingPolicy$CacheOnLargeSegments DEFAULT
intf org.apache.lucene.search.QueryCachingPolicy
meth public boolean shouldCache(org.apache.lucene.search.Query,org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public void onUse(org.apache.lucene.search.Query)
supr java.lang.Object
hfds minIndexSize,minSizeRatio

CLSS public abstract org.apache.lucene.search.QueryRescorer
cons public init(org.apache.lucene.search.Query)
meth protected abstract float combine(float,boolean,float)
meth public org.apache.lucene.search.Explanation explain(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.Explanation,int) throws java.io.IOException
meth public org.apache.lucene.search.TopDocs rescore(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.TopDocs,int) throws java.io.IOException
meth public static org.apache.lucene.search.TopDocs rescore(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.TopDocs,org.apache.lucene.search.Query,double,int) throws java.io.IOException
supr org.apache.lucene.search.Rescorer
hfds query

CLSS public org.apache.lucene.search.QueryWrapperFilter
 anno 0 java.lang.Deprecated()
cons public init(org.apache.lucene.search.Query)
meth public boolean equals(java.lang.Object)
meth public final org.apache.lucene.search.Query getQuery()
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.DocIdSet getDocIdSet(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.util.Bits) throws java.io.IOException
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
supr org.apache.lucene.search.Filter
hfds query

CLSS public abstract org.apache.lucene.search.RandomAccessWeight
cons protected init(org.apache.lucene.search.Query)
meth protected abstract org.apache.lucene.util.Bits getMatchingDocs(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public final org.apache.lucene.search.Scorer scorer(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
supr org.apache.lucene.search.ConstantScoreWeight

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
cons public init(org.apache.lucene.index.Term,int,org.apache.lucene.util.automaton.AutomatonProvider,int)
meth public java.lang.String toString(java.lang.String)
supr org.apache.lucene.search.AutomatonQuery
hfds defaultProvider

CLSS public abstract org.apache.lucene.search.Rescorer
cons public init()
meth public abstract org.apache.lucene.search.Explanation explain(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.Explanation,int) throws java.io.IOException
meth public abstract org.apache.lucene.search.TopDocs rescore(org.apache.lucene.search.IndexSearcher,org.apache.lucene.search.TopDocs,int) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.lucene.search.ScoreCachingWrappingScorer
cons public init(org.apache.lucene.search.Scorer)
meth public float score() throws java.io.IOException
meth public java.util.Collection<org.apache.lucene.search.Scorer$ChildScorer> getChildren()
supr org.apache.lucene.search.FilterScorer
hfds curDoc,curScore

CLSS public org.apache.lucene.search.ScoreDoc
cons public init(int,float)
cons public init(int,float,int)
fld public float score
fld public int doc
fld public int shardIndex
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.Scorer
cons protected init(org.apache.lucene.search.Weight)
fld protected final org.apache.lucene.search.Weight weight
innr public static ChildScorer
meth public abstract float score() throws java.io.IOException
meth public abstract int docID()
meth public abstract int freq() throws java.io.IOException
meth public abstract org.apache.lucene.search.DocIdSetIterator iterator()
meth public java.util.Collection<org.apache.lucene.search.Scorer$ChildScorer> getChildren()
meth public org.apache.lucene.search.TwoPhaseIterator twoPhaseIterator()
meth public org.apache.lucene.search.Weight getWeight()
supr java.lang.Object

CLSS public static org.apache.lucene.search.Scorer$ChildScorer
 outer org.apache.lucene.search.Scorer
cons public init(org.apache.lucene.search.Scorer,java.lang.String)
fld public final java.lang.String relationship
fld public final org.apache.lucene.search.Scorer child
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.ScoringRewrite<%0 extends java.lang.Object>
cons public init()
fld public final static org.apache.lucene.search.MultiTermQuery$RewriteMethod CONSTANT_SCORE_BOOLEAN_REWRITE
fld public final static org.apache.lucene.search.ScoringRewrite<org.apache.lucene.search.BooleanQuery$Builder> SCORING_BOOLEAN_REWRITE
meth protected abstract org.apache.lucene.search.Query build({org.apache.lucene.search.ScoringRewrite%0})
meth protected abstract void addClause({org.apache.lucene.search.ScoringRewrite%0},org.apache.lucene.index.Term,int,float,org.apache.lucene.index.TermContext) throws java.io.IOException
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
cons public init(org.apache.lucene.index.IndexWriter,boolean,org.apache.lucene.search.SearcherFactory) throws java.io.IOException
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

CLSS public abstract org.apache.lucene.search.SimpleCollector
cons public init()
intf org.apache.lucene.search.Collector
intf org.apache.lucene.search.LeafCollector
meth protected void doSetNextReader(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public abstract void collect(int) throws java.io.IOException
meth public final org.apache.lucene.search.LeafCollector getLeafCollector(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public void setScorer(org.apache.lucene.search.Scorer) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.SimpleFieldComparator<%0 extends java.lang.Object>
cons public init()
intf org.apache.lucene.search.LeafFieldComparator
meth protected abstract void doSetNextReader(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public final org.apache.lucene.search.LeafFieldComparator getLeafComparator(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public void setScorer(org.apache.lucene.search.Scorer)
supr org.apache.lucene.search.FieldComparator<{org.apache.lucene.search.SimpleFieldComparator%0}>

CLSS public org.apache.lucene.search.Sort
cons public !varargs init(org.apache.lucene.search.SortField[])
cons public init()
cons public init(org.apache.lucene.search.SortField)
fld public final static org.apache.lucene.search.Sort INDEXORDER
fld public final static org.apache.lucene.search.Sort RELEVANCE
meth public !varargs void setSort(org.apache.lucene.search.SortField[])
meth public boolean equals(java.lang.Object)
meth public boolean needsScores()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.apache.lucene.search.Sort rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.SortField[] getSort()
meth public void setSort(org.apache.lucene.search.SortField)
supr java.lang.Object
hfds fields

CLSS public org.apache.lucene.search.SortField
cons public init(java.lang.String,org.apache.lucene.search.FieldComparatorSource)
cons public init(java.lang.String,org.apache.lucene.search.FieldComparatorSource,boolean)
cons public init(java.lang.String,org.apache.lucene.search.SortField$Type)
cons public init(java.lang.String,org.apache.lucene.search.SortField$Type,boolean)
fld public final static java.lang.Object STRING_FIRST
fld public final static java.lang.Object STRING_LAST
fld public final static org.apache.lucene.search.SortField FIELD_DOC
fld public final static org.apache.lucene.search.SortField FIELD_SCORE
fld public java.lang.Object missingValue
innr public final static !enum Type
meth public boolean equals(java.lang.Object)
meth public boolean getReverse()
meth public boolean needsScores()
meth public int hashCode()
meth public java.lang.String getField()
meth public java.lang.String toString()
meth public java.util.Comparator<org.apache.lucene.util.BytesRef> getBytesComparator()
meth public org.apache.lucene.search.FieldComparator<?> getComparator(int,int) throws java.io.IOException
meth public org.apache.lucene.search.FieldComparatorSource getComparatorSource()
meth public org.apache.lucene.search.SortField rewrite(org.apache.lucene.search.IndexSearcher) throws java.io.IOException
meth public org.apache.lucene.search.SortField$Type getType()
meth public void setBytesComparator(java.util.Comparator<org.apache.lucene.util.BytesRef>)
meth public void setMissingValue(java.lang.Object)
supr java.lang.Object
hfds bytesComparator,comparatorSource,field,reverse,type

CLSS public final static !enum org.apache.lucene.search.SortField$Type
 outer org.apache.lucene.search.SortField
fld public final static org.apache.lucene.search.SortField$Type BYTES
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
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.apache.lucene.search.FieldComparator<?> getComparator(int,int) throws java.io.IOException
meth public org.apache.lucene.search.SortedNumericSelector$Type getSelector()
meth public void setMissingValue(java.lang.Object)
supr org.apache.lucene.search.SortField
hfds selector,type

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
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.apache.lucene.search.FieldComparator<?> getComparator(int,int) throws java.io.IOException
meth public org.apache.lucene.search.SortedSetSelector$Type getSelector()
meth public void setMissingValue(java.lang.Object)
supr org.apache.lucene.search.SortField
hfds selector

CLSS public org.apache.lucene.search.TermQuery
cons public init(org.apache.lucene.index.Term)
cons public init(org.apache.lucene.index.Term,org.apache.lucene.index.TermContext)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.index.Term getTerm()
meth public org.apache.lucene.search.Weight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
supr org.apache.lucene.search.Query
hfds perReaderTermState,term
hcls TermWeight

CLSS public org.apache.lucene.search.TermRangeFilter
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef,boolean,boolean)
meth public boolean includesLower()
meth public boolean includesUpper()
meth public org.apache.lucene.util.BytesRef getLowerTerm()
meth public org.apache.lucene.util.BytesRef getUpperTerm()
meth public static org.apache.lucene.search.TermRangeFilter Less(java.lang.String,org.apache.lucene.util.BytesRef)
meth public static org.apache.lucene.search.TermRangeFilter More(java.lang.String,org.apache.lucene.util.BytesRef)
meth public static org.apache.lucene.search.TermRangeFilter newStringRange(java.lang.String,java.lang.String,java.lang.String,boolean,boolean)
supr org.apache.lucene.search.MultiTermQueryWrapperFilter<org.apache.lucene.search.TermRangeQuery>

CLSS public org.apache.lucene.search.TermRangeQuery
cons public init(java.lang.String,org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef,boolean,boolean)
meth public boolean equals(java.lang.Object)
meth public boolean includesLower()
meth public boolean includesUpper()
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.util.BytesRef getLowerTerm()
meth public org.apache.lucene.util.BytesRef getUpperTerm()
meth public static org.apache.lucene.search.TermRangeQuery newStringRange(java.lang.String,java.lang.String,java.lang.String,boolean,boolean)
meth public static org.apache.lucene.util.automaton.Automaton toAutomaton(org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef,boolean,boolean)
supr org.apache.lucene.search.AutomatonQuery
hfds includeLower,includeUpper,lowerTerm,upperTerm

CLSS public org.apache.lucene.search.TermStatistics
cons public init(org.apache.lucene.util.BytesRef,long,long)
meth public final long docFreq()
meth public final long totalTermFreq()
meth public final org.apache.lucene.util.BytesRef term()
supr java.lang.Object
hfds docFreq,term,totalTermFreq

CLSS public org.apache.lucene.search.TimeLimitingCollector
cons public init(org.apache.lucene.search.Collector,org.apache.lucene.util.Counter,long)
innr public final static TimerThread
innr public static TimeExceededException
intf org.apache.lucene.search.Collector
meth public boolean isGreedy()
meth public boolean needsScores()
meth public org.apache.lucene.search.LeafCollector getLeafCollector(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
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
cons public init(int,org.apache.lucene.search.ScoreDoc[],float)
fld public int totalHits
fld public org.apache.lucene.search.ScoreDoc[] scoreDocs
meth public float getMaxScore()
meth public static org.apache.lucene.search.TopDocs merge(int,int,org.apache.lucene.search.TopDocs[]) throws java.io.IOException
meth public static org.apache.lucene.search.TopDocs merge(int,org.apache.lucene.search.TopDocs[]) throws java.io.IOException
meth public static org.apache.lucene.search.TopFieldDocs merge(org.apache.lucene.search.Sort,int,int,org.apache.lucene.search.TopFieldDocs[]) throws java.io.IOException
meth public static org.apache.lucene.search.TopFieldDocs merge(org.apache.lucene.search.Sort,int,org.apache.lucene.search.TopFieldDocs[]) throws java.io.IOException
meth public void setMaxScore(float)
supr java.lang.Object
hfds maxScore
hcls MergeSortQueue,ScoreMergeSortQueue,ShardRef

CLSS public abstract org.apache.lucene.search.TopDocsCollector<%0 extends org.apache.lucene.search.ScoreDoc>
cons protected init(org.apache.lucene.util.PriorityQueue<{org.apache.lucene.search.TopDocsCollector%0}>)
fld protected final static org.apache.lucene.search.TopDocs EMPTY_TOPDOCS
fld protected int totalHits
fld protected org.apache.lucene.util.PriorityQueue<{org.apache.lucene.search.TopDocsCollector%0}> pq
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
meth public boolean needsScores()
meth public org.apache.lucene.search.TopFieldDocs topDocs()
meth public static org.apache.lucene.search.TopFieldCollector create(org.apache.lucene.search.Sort,int,boolean,boolean,boolean) throws java.io.IOException
meth public static org.apache.lucene.search.TopFieldCollector create(org.apache.lucene.search.Sort,int,org.apache.lucene.search.FieldDoc,boolean,boolean,boolean) throws java.io.IOException
supr org.apache.lucene.search.TopDocsCollector<org.apache.lucene.search.FieldValueHitQueue$Entry>
hfds EMPTY_SCOREDOCS,bottom,docBase,fillFields,maxScore,needsScores,numHits,queueFull
hcls MultiComparatorLeafCollector,OneComparatorLeafCollector,PagingFieldCollector,SimpleFieldCollector

CLSS public org.apache.lucene.search.TopFieldDocs
cons public init(int,org.apache.lucene.search.ScoreDoc[],org.apache.lucene.search.SortField[],float)
fld public org.apache.lucene.search.SortField[] fields
supr org.apache.lucene.search.TopDocs

CLSS public abstract org.apache.lucene.search.TopScoreDocCollector
meth protected org.apache.lucene.search.TopDocs newTopDocs(org.apache.lucene.search.ScoreDoc[],int)
meth public boolean needsScores()
meth public static org.apache.lucene.search.TopScoreDocCollector create(int)
meth public static org.apache.lucene.search.TopScoreDocCollector create(int,org.apache.lucene.search.ScoreDoc)
supr org.apache.lucene.search.TopDocsCollector<org.apache.lucene.search.ScoreDoc>
hfds pqTop
hcls PagingTopScoreDocCollector,ScorerLeafCollector,SimpleTopScoreDocCollector

CLSS public abstract org.apache.lucene.search.TopTermsRewrite<%0 extends java.lang.Object>
cons public init(int)
meth protected abstract int getMaxSize()
meth protected abstract org.apache.lucene.search.Query build({org.apache.lucene.search.TopTermsRewrite%0})
meth protected abstract void addClause({org.apache.lucene.search.TopTermsRewrite%0},org.apache.lucene.index.Term,int,float,org.apache.lucene.index.TermContext) throws java.io.IOException
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
meth public boolean needsScores()
meth public int getTotalHits()
meth public void collect(int)
supr org.apache.lucene.search.SimpleCollector
hfds totalHits

CLSS public abstract org.apache.lucene.search.TwoPhaseDocIdSetIterator
cons public init()
meth public abstract boolean matches() throws java.io.IOException
meth public abstract org.apache.lucene.search.DocIdSetIterator approximation()
meth public static org.apache.lucene.search.DocIdSetIterator asDocIdSetIterator(org.apache.lucene.search.TwoPhaseDocIdSetIterator)
supr java.lang.Object

CLSS public abstract org.apache.lucene.search.TwoPhaseIterator
cons protected init(org.apache.lucene.search.DocIdSetIterator)
fld protected final org.apache.lucene.search.DocIdSetIterator approximation
meth public abstract boolean matches() throws java.io.IOException
meth public abstract float matchCost()
meth public org.apache.lucene.search.DocIdSetIterator approximation()
meth public static org.apache.lucene.search.DocIdSetIterator asDocIdSetIterator(org.apache.lucene.search.TwoPhaseIterator)
supr java.lang.Object

CLSS public final org.apache.lucene.search.UsageTrackingFilterCachingPolicy
cons public init()
cons public init(float,int,int,int,int)
intf org.apache.lucene.search.FilterCachingPolicy
meth public boolean shouldCache(org.apache.lucene.search.Filter,org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.DocIdSet) throws java.io.IOException
meth public void onUse(org.apache.lucene.search.Filter)
supr java.lang.Object
hfds SENTINEL,minFrequencyCheapFilters,minFrequencyCostlyFilters,minFrequencyOtherFilters,recentlyUsedFilters,segmentPolicy

CLSS public final org.apache.lucene.search.UsageTrackingQueryCachingPolicy
cons public init()
cons public init(int,float,int)
intf org.apache.lucene.search.QueryCachingPolicy
meth protected int minFrequencyToCache(org.apache.lucene.search.Query)
meth public boolean shouldCache(org.apache.lucene.search.Query,org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public void onUse(org.apache.lucene.search.Query)
supr java.lang.Object
hfds SENTINEL,recentlyUsedFilters,segmentPolicy

CLSS public abstract org.apache.lucene.search.Weight
cons protected init(org.apache.lucene.search.Query)
fld protected final org.apache.lucene.search.Query parentQuery
innr protected static DefaultBulkScorer
meth public abstract float getValueForNormalization() throws java.io.IOException
meth public abstract org.apache.lucene.search.Explanation explain(org.apache.lucene.index.LeafReaderContext,int) throws java.io.IOException
meth public abstract org.apache.lucene.search.Scorer scorer(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public abstract void extractTerms(java.util.Set<org.apache.lucene.index.Term>)
meth public abstract void normalize(float,float)
meth public final org.apache.lucene.search.Query getQuery()
meth public org.apache.lucene.search.BulkScorer bulkScorer(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
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
meth public final static org.apache.lucene.search.highlight.WeightedTerm[] getTerms(org.apache.lucene.search.Query,boolean,java.lang.String)
supr java.lang.Object
hfds EMPTY_INDEXSEARCHER

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
meth public org.apache.lucene.index.FieldInfos getFieldInfos()
meth public org.apache.lucene.index.Fields fields() throws java.io.IOException
meth public org.apache.lucene.index.Fields getTermVectors(int) throws java.io.IOException
meth public org.apache.lucene.index.NumericDocValues getNormValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.NumericDocValues getNumericDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedDocValues getSortedDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedNumericDocValues getSortedNumericDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.index.SortedSetDocValues getSortedSetDocValues(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.util.Bits getDocsWithField(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.util.Bits getLiveDocs()
meth public void addCoreClosedListener(org.apache.lucene.index.LeafReader$CoreClosedListener)
meth public void checkIntegrity() throws java.io.IOException
meth public void document(int,org.apache.lucene.index.StoredFieldVisitor) throws java.io.IOException
meth public void removeCoreClosedListener(org.apache.lucene.index.LeafReader$CoreClosedListener)
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
meth public org.apache.lucene.analysis.Token getToken(int)
supr java.lang.Object
hfds MAX_NUM_TOKENS_PER_GROUP,endOffset,matchEndOffset,matchStartOffset,numTokens,offsetAtt,scores,startOffset,termAtt,tokens,tot

CLSS public org.apache.lucene.search.highlight.TokenSources
meth public static org.apache.lucene.analysis.TokenStream getAnyTokenStream(org.apache.lucene.index.IndexReader,int,java.lang.String,org.apache.lucene.analysis.Analyzer) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static org.apache.lucene.analysis.TokenStream getAnyTokenStream(org.apache.lucene.index.IndexReader,int,java.lang.String,org.apache.lucene.document.Document,org.apache.lucene.analysis.Analyzer) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static org.apache.lucene.analysis.TokenStream getTermVectorTokenStreamOrNull(java.lang.String,org.apache.lucene.index.Fields,int) throws java.io.IOException
meth public static org.apache.lucene.analysis.TokenStream getTokenStream(java.lang.String,java.lang.String,org.apache.lucene.analysis.Analyzer)
 anno 0 java.lang.Deprecated()
meth public static org.apache.lucene.analysis.TokenStream getTokenStream(java.lang.String,org.apache.lucene.index.Fields,java.lang.String,org.apache.lucene.analysis.Analyzer,int) throws java.io.IOException
meth public static org.apache.lucene.analysis.TokenStream getTokenStream(org.apache.lucene.document.Document,java.lang.String,org.apache.lucene.analysis.Analyzer)
 anno 0 java.lang.Deprecated()
meth public static org.apache.lucene.analysis.TokenStream getTokenStream(org.apache.lucene.index.IndexReader,int,java.lang.String,org.apache.lucene.analysis.Analyzer) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static org.apache.lucene.analysis.TokenStream getTokenStream(org.apache.lucene.index.Terms) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static org.apache.lucene.analysis.TokenStream getTokenStream(org.apache.lucene.index.Terms,boolean) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static org.apache.lucene.analysis.TokenStream getTokenStreamWithOffsets(org.apache.lucene.index.IndexReader,int,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
supr java.lang.Object

CLSS public final org.apache.lucene.search.highlight.TokenStreamFromTermVector
cons public init(org.apache.lucene.index.Terms,int) throws java.io.IOException
fld public final static org.apache.lucene.util.AttributeFactory ATTRIBUTE_FACTORY
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
meth protected boolean mustRewriteQuery(org.apache.lucene.search.spans.SpanQuery)
meth protected final void setMaxDocCharsToAnalyze(int)
meth protected org.apache.lucene.index.LeafReaderContext getLeafContext() throws java.io.IOException
meth protected void collectSpanQueryFields(org.apache.lucene.search.spans.SpanQuery,java.util.Set<java.lang.String>)
meth protected void extract(org.apache.lucene.search.Query,float,java.util.Map<java.lang.String,org.apache.lucene.search.highlight.WeightedSpanTerm>) throws java.io.IOException
meth protected void extractUnknownQuery(org.apache.lucene.search.Query,java.util.Map<java.lang.String,org.apache.lucene.search.highlight.WeightedSpanTerm>) throws java.io.IOException
meth protected void extractWeightedSpanTerms(java.util.Map<java.lang.String,org.apache.lucene.search.highlight.WeightedSpanTerm>,org.apache.lucene.search.spans.SpanQuery,float) throws java.io.IOException
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

CLSS abstract interface org.apache.lucene.search.highlight.package-info

CLSS abstract interface org.apache.lucene.search.package-info

CLSS public final org.apache.lucene.search.spans.FieldMaskingSpanQuery
cons public init(org.apache.lucene.search.spans.SpanQuery,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getField()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.spans.SpanQuery getMaskedQuery()
meth public org.apache.lucene.search.spans.SpanWeight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
supr org.apache.lucene.search.spans.SpanQuery
hfds field,maskedQuery

CLSS public abstract org.apache.lucene.search.spans.FilterSpans
cons protected init(org.apache.lucene.search.spans.Spans)
fld protected final org.apache.lucene.search.spans.Spans in
innr public final static !enum AcceptStatus
meth protected abstract org.apache.lucene.search.spans.FilterSpans$AcceptStatus accept(org.apache.lucene.search.spans.Spans) throws java.io.IOException
meth public final int advance(int) throws java.io.IOException
meth public final int docID()
meth public final int endPosition()
meth public final int nextDoc() throws java.io.IOException
meth public final int nextStartPosition() throws java.io.IOException
meth public final int startPosition()
meth public final long cost()
meth public final org.apache.lucene.search.TwoPhaseIterator asTwoPhaseIterator()
meth public float positionsCost()
meth public int width()
meth public java.lang.String toString()
meth public void collect(org.apache.lucene.search.spans.SpanCollector) throws java.io.IOException
supr org.apache.lucene.search.spans.Spans
hfds atFirstInCurrentDoc,startPos

CLSS public final static !enum org.apache.lucene.search.spans.FilterSpans$AcceptStatus
 outer org.apache.lucene.search.spans.FilterSpans
fld public final static org.apache.lucene.search.spans.FilterSpans$AcceptStatus NO
fld public final static org.apache.lucene.search.spans.FilterSpans$AcceptStatus NO_MORE_IN_CURRENT_DOC
fld public final static org.apache.lucene.search.spans.FilterSpans$AcceptStatus YES
meth public static org.apache.lucene.search.spans.FilterSpans$AcceptStatus valueOf(java.lang.String)
meth public static org.apache.lucene.search.spans.FilterSpans$AcceptStatus[] values()
supr java.lang.Enum<org.apache.lucene.search.spans.FilterSpans$AcceptStatus>

CLSS public org.apache.lucene.search.spans.NearSpansOrdered
cons public init(int,java.util.List<org.apache.lucene.search.spans.Spans>) throws java.io.IOException
fld protected int matchEnd
fld protected int matchStart
fld protected int matchWidth
meth public float positionsCost()
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int endPosition()
meth public int nextDoc() throws java.io.IOException
meth public int nextStartPosition() throws java.io.IOException
meth public int startPosition()
meth public int width()
meth public long cost()
meth public org.apache.lucene.search.TwoPhaseIterator asTwoPhaseIterator()
meth public org.apache.lucene.search.spans.Spans[] getSubSpans()
meth public void collect(org.apache.lucene.search.spans.SpanCollector) throws java.io.IOException
supr org.apache.lucene.search.spans.Spans
hfds allowedSlop

CLSS public org.apache.lucene.search.spans.NearSpansUnordered
cons public init(int,java.util.List<org.apache.lucene.search.spans.Spans>) throws java.io.IOException
meth public float positionsCost()
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int endPosition()
meth public int nextDoc() throws java.io.IOException
meth public int nextStartPosition() throws java.io.IOException
meth public int startPosition()
meth public int width()
meth public long cost()
meth public org.apache.lucene.search.TwoPhaseIterator asTwoPhaseIterator()
meth public org.apache.lucene.search.spans.Spans[] getSubSpans()
meth public void collect(org.apache.lucene.search.spans.SpanCollector) throws java.io.IOException
supr org.apache.lucene.search.spans.Spans
hfds allowedSlop,maxEndPositionCell,spanPositionQueue,subSpanCells,totalSpanLength
hcls SpanPositionQueue,SpansCell

CLSS public org.apache.lucene.search.spans.ScoringWrapperSpans
cons public init(org.apache.lucene.search.spans.Spans,org.apache.lucene.search.similarities.Similarity$SimScorer)
meth public float positionsCost()
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int endPosition()
meth public int nextDoc() throws java.io.IOException
meth public int nextStartPosition() throws java.io.IOException
meth public int startPosition()
meth public int width()
meth public long cost()
meth public org.apache.lucene.search.TwoPhaseIterator asTwoPhaseIterator()
meth public void collect(org.apache.lucene.search.spans.SpanCollector) throws java.io.IOException
supr org.apache.lucene.search.spans.Spans
hfds in

CLSS public final org.apache.lucene.search.spans.SpanBoostQuery
cons public init(org.apache.lucene.search.spans.SpanQuery,float)
meth public boolean equals(java.lang.Object)
meth public float getBoost()
meth public int hashCode()
meth public java.lang.String getField()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.spans.SpanQuery getQuery()
meth public org.apache.lucene.search.spans.SpanWeight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
supr org.apache.lucene.search.spans.SpanQuery
hfds NO_PARENS_REQUIRED_QUERIES,query

CLSS public abstract interface org.apache.lucene.search.spans.SpanCollector
meth public abstract void collectLeaf(org.apache.lucene.index.PostingsEnum,int,org.apache.lucene.index.Term) throws java.io.IOException
meth public abstract void reset()

CLSS public final org.apache.lucene.search.spans.SpanContainingQuery
cons public init(org.apache.lucene.search.spans.SpanQuery,org.apache.lucene.search.spans.SpanQuery)
innr public SpanContainingWeight
innr public abstract SpanContainWeight
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getField()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.spans.SpanWeight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
supr org.apache.lucene.search.spans.SpanQuery

CLSS public org.apache.lucene.search.spans.SpanContainingQuery$SpanContainingWeight
 outer org.apache.lucene.search.spans.SpanContainingQuery
cons public init(org.apache.lucene.search.IndexSearcher,java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext>,org.apache.lucene.search.spans.SpanWeight,org.apache.lucene.search.spans.SpanWeight) throws java.io.IOException
meth public org.apache.lucene.search.spans.Spans getSpans(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.spans.SpanWeight$Postings) throws java.io.IOException
meth public void extractTermContexts(java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext>)
meth public void extractTerms(java.util.Set<org.apache.lucene.index.Term>)
supr org.apache.lucene.search.spans.SpanWeight

CLSS public org.apache.lucene.search.spans.SpanFirstQuery
cons public init(org.apache.lucene.search.spans.SpanQuery,int)
meth protected org.apache.lucene.search.spans.FilterSpans$AcceptStatus acceptPosition(org.apache.lucene.search.spans.Spans) throws java.io.IOException
meth public java.lang.String toString(java.lang.String)
supr org.apache.lucene.search.spans.SpanPositionRangeQuery

CLSS public org.apache.lucene.search.spans.SpanMultiTermQueryWrapper<%0 extends org.apache.lucene.search.MultiTermQuery>
cons public init({org.apache.lucene.search.spans.SpanMultiTermQueryWrapper%0})
fld protected final {org.apache.lucene.search.spans.SpanMultiTermQueryWrapper%0} query
fld public final static org.apache.lucene.search.spans.SpanMultiTermQueryWrapper$SpanRewriteMethod SCORING_SPAN_QUERY_REWRITE
innr public abstract static SpanRewriteMethod
innr public final static TopTermsSpanBooleanQueryRewrite
meth public boolean equals(java.lang.Object)
meth public final org.apache.lucene.search.spans.SpanMultiTermQueryWrapper$SpanRewriteMethod getRewriteMethod()
meth public final void setRewriteMethod(org.apache.lucene.search.spans.SpanMultiTermQueryWrapper$SpanRewriteMethod)
meth public int hashCode()
meth public java.lang.String getField()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query getWrappedQuery()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.spans.SpanWeight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
supr org.apache.lucene.search.spans.SpanQuery
hfds rewriteMethod

CLSS public abstract static org.apache.lucene.search.spans.SpanMultiTermQueryWrapper$SpanRewriteMethod
 outer org.apache.lucene.search.spans.SpanMultiTermQueryWrapper
cons public init()
meth public abstract org.apache.lucene.search.spans.SpanQuery rewrite(org.apache.lucene.index.IndexReader,org.apache.lucene.search.MultiTermQuery) throws java.io.IOException
supr org.apache.lucene.search.MultiTermQuery$RewriteMethod

CLSS public final static org.apache.lucene.search.spans.SpanMultiTermQueryWrapper$TopTermsSpanBooleanQueryRewrite
 outer org.apache.lucene.search.spans.SpanMultiTermQueryWrapper
cons public init(int)
meth public boolean equals(java.lang.Object)
meth public int getSize()
meth public int hashCode()
meth public org.apache.lucene.search.spans.SpanQuery rewrite(org.apache.lucene.index.IndexReader,org.apache.lucene.search.MultiTermQuery) throws java.io.IOException
supr org.apache.lucene.search.spans.SpanMultiTermQueryWrapper$SpanRewriteMethod
hfds delegate

CLSS public org.apache.lucene.search.spans.SpanNearQuery
cons public init(org.apache.lucene.search.spans.SpanQuery[],int,boolean)
cons public init(org.apache.lucene.search.spans.SpanQuery[],int,boolean,boolean)
 anno 0 java.lang.Deprecated()
fld protected boolean inOrder
fld protected int slop
fld protected java.lang.String field
fld protected java.util.List<org.apache.lucene.search.spans.SpanQuery> clauses
innr public SpanNearWeight
innr public static Builder
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public boolean isInOrder()
meth public int getSlop()
meth public int hashCode()
meth public java.lang.String getField()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.spans.SpanQuery[] getClauses()
meth public org.apache.lucene.search.spans.SpanWeight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
meth public static org.apache.lucene.search.spans.SpanNearQuery$Builder newOrderedNearQuery(java.lang.String)
meth public static org.apache.lucene.search.spans.SpanNearQuery$Builder newUnorderedNearQuery(java.lang.String)
supr org.apache.lucene.search.spans.SpanQuery
hcls GapSpans,SpanGapQuery

CLSS public static org.apache.lucene.search.spans.SpanNearQuery$Builder
 outer org.apache.lucene.search.spans.SpanNearQuery
cons public init(java.lang.String,boolean)
meth public org.apache.lucene.search.spans.SpanNearQuery build()
meth public org.apache.lucene.search.spans.SpanNearQuery$Builder addClause(org.apache.lucene.search.spans.SpanQuery)
meth public org.apache.lucene.search.spans.SpanNearQuery$Builder addGap(int)
meth public org.apache.lucene.search.spans.SpanNearQuery$Builder setSlop(int)
supr java.lang.Object
hfds clauses,field,ordered,slop

CLSS public org.apache.lucene.search.spans.SpanNearQuery$SpanNearWeight
 outer org.apache.lucene.search.spans.SpanNearQuery
cons public init(java.util.List<org.apache.lucene.search.spans.SpanWeight>,org.apache.lucene.search.IndexSearcher,java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext>) throws java.io.IOException
meth public org.apache.lucene.search.spans.Spans getSpans(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.spans.SpanWeight$Postings) throws java.io.IOException
meth public void extractTermContexts(java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext>)
meth public void extractTerms(java.util.Set<org.apache.lucene.index.Term>)
supr org.apache.lucene.search.spans.SpanWeight
hfds subWeights

CLSS public final org.apache.lucene.search.spans.SpanNotQuery
cons public init(org.apache.lucene.search.spans.SpanQuery,org.apache.lucene.search.spans.SpanQuery)
cons public init(org.apache.lucene.search.spans.SpanQuery,org.apache.lucene.search.spans.SpanQuery,int)
cons public init(org.apache.lucene.search.spans.SpanQuery,org.apache.lucene.search.spans.SpanQuery,int,int)
innr public SpanNotWeight
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getField()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.spans.SpanQuery getExclude()
meth public org.apache.lucene.search.spans.SpanQuery getInclude()
meth public org.apache.lucene.search.spans.SpanWeight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
supr org.apache.lucene.search.spans.SpanQuery
hfds exclude,include,post,pre

CLSS public org.apache.lucene.search.spans.SpanNotQuery$SpanNotWeight
 outer org.apache.lucene.search.spans.SpanNotQuery
cons public init(org.apache.lucene.search.IndexSearcher,java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext>,org.apache.lucene.search.spans.SpanWeight,org.apache.lucene.search.spans.SpanWeight) throws java.io.IOException
meth public org.apache.lucene.search.spans.Spans getSpans(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.spans.SpanWeight$Postings) throws java.io.IOException
meth public void extractTermContexts(java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext>)
meth public void extractTerms(java.util.Set<org.apache.lucene.index.Term>)
supr org.apache.lucene.search.spans.SpanWeight
hfds excludeWeight,includeWeight

CLSS public final org.apache.lucene.search.spans.SpanOrQuery
cons public !varargs init(org.apache.lucene.search.spans.SpanQuery[])
innr public SpanOrWeight
meth public boolean equals(java.lang.Object)
meth public final void addClause(org.apache.lucene.search.spans.SpanQuery)
 anno 0 java.lang.Deprecated()
meth public int hashCode()
meth public java.lang.String getField()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.spans.SpanQuery[] getClauses()
meth public org.apache.lucene.search.spans.SpanWeight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
supr org.apache.lucene.search.spans.SpanQuery
hfds clauses,field

CLSS public org.apache.lucene.search.spans.SpanOrQuery$SpanOrWeight
 outer org.apache.lucene.search.spans.SpanOrQuery
cons public init(org.apache.lucene.search.IndexSearcher,java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext>,java.util.List<org.apache.lucene.search.spans.SpanWeight>) throws java.io.IOException
meth public org.apache.lucene.search.spans.Spans getSpans(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.spans.SpanWeight$Postings) throws java.io.IOException
meth public void extractTermContexts(java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext>)
meth public void extractTerms(java.util.Set<org.apache.lucene.index.Term>)
supr org.apache.lucene.search.spans.SpanWeight
hfds subWeights

CLSS public abstract org.apache.lucene.search.spans.SpanPositionCheckQuery
cons public init(org.apache.lucene.search.spans.SpanQuery)
fld protected org.apache.lucene.search.spans.SpanQuery match
innr public SpanPositionCheckWeight
intf java.lang.Cloneable
meth protected abstract org.apache.lucene.search.spans.FilterSpans$AcceptStatus acceptPosition(org.apache.lucene.search.spans.Spans) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getField()
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.spans.SpanQuery getMatch()
meth public org.apache.lucene.search.spans.SpanWeight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
supr org.apache.lucene.search.spans.SpanQuery

CLSS public org.apache.lucene.search.spans.SpanPositionCheckQuery$SpanPositionCheckWeight
 outer org.apache.lucene.search.spans.SpanPositionCheckQuery
cons public init(org.apache.lucene.search.spans.SpanWeight,org.apache.lucene.search.IndexSearcher,java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext>) throws java.io.IOException
meth public org.apache.lucene.search.spans.Spans getSpans(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.spans.SpanWeight$Postings) throws java.io.IOException
meth public void extractTermContexts(java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext>)
meth public void extractTerms(java.util.Set<org.apache.lucene.index.Term>)
supr org.apache.lucene.search.spans.SpanWeight
hfds matchWeight

CLSS public org.apache.lucene.search.spans.SpanPositionRangeQuery
cons public init(org.apache.lucene.search.spans.SpanQuery,int,int)
fld protected int end
fld protected int start
meth protected org.apache.lucene.search.spans.FilterSpans$AcceptStatus acceptPosition(org.apache.lucene.search.spans.Spans) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public int getEnd()
meth public int getStart()
meth public int hashCode()
meth public java.lang.String toString(java.lang.String)
supr org.apache.lucene.search.spans.SpanPositionCheckQuery

CLSS public abstract org.apache.lucene.search.spans.SpanQuery
cons public init()
meth public !varargs static java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext> getTermContexts(org.apache.lucene.search.spans.SpanWeight[])
meth public abstract java.lang.String getField()
meth public abstract org.apache.lucene.search.spans.SpanWeight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public static java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext> getTermContexts(java.util.Collection<org.apache.lucene.search.spans.SpanWeight>)
supr org.apache.lucene.search.Query

CLSS public org.apache.lucene.search.spans.SpanScorer
cons public init(org.apache.lucene.search.spans.SpanWeight,org.apache.lucene.search.spans.Spans,org.apache.lucene.search.similarities.Similarity$SimScorer)
fld protected final org.apache.lucene.search.similarities.Similarity$SimScorer docScorer
fld protected final org.apache.lucene.search.spans.Spans spans
meth protected final void setFreqCurrentDoc() throws java.io.IOException
meth protected float scoreCurrentDoc() throws java.io.IOException
meth public final float score() throws java.io.IOException
meth public final float sloppyFreq() throws java.io.IOException
meth public final int freq() throws java.io.IOException
meth public int docID()
meth public org.apache.lucene.search.TwoPhaseIterator twoPhaseIterator()
meth public org.apache.lucene.search.spans.Spans getSpans()
meth public org.apache.lucene.search.spans.Spans iterator()
supr org.apache.lucene.search.Scorer
hfds freq,lastScoredDoc,numMatches

CLSS public org.apache.lucene.search.spans.SpanTermQuery
cons public init(org.apache.lucene.index.Term)
cons public init(org.apache.lucene.index.Term,org.apache.lucene.index.TermContext)
fld protected final org.apache.lucene.index.Term term
fld protected final org.apache.lucene.index.TermContext termContext
innr public SpanTermWeight
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getField()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.index.Term getTerm()
meth public org.apache.lucene.search.spans.SpanWeight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
supr org.apache.lucene.search.spans.SpanQuery
hfds PHRASE_TO_SPAN_TERM_POSITIONS_COST,TERM_OPS_PER_POS,TERM_POSNS_SEEK_OPS_PER_DOC

CLSS public org.apache.lucene.search.spans.SpanTermQuery$SpanTermWeight
 outer org.apache.lucene.search.spans.SpanTermQuery
cons public init(org.apache.lucene.index.TermContext,org.apache.lucene.search.IndexSearcher,java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext>) throws java.io.IOException
meth public org.apache.lucene.search.spans.Spans getSpans(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.spans.SpanWeight$Postings) throws java.io.IOException
meth public void extractTermContexts(java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext>)
meth public void extractTerms(java.util.Set<org.apache.lucene.index.Term>)
supr org.apache.lucene.search.spans.SpanWeight
hfds termContext

CLSS public abstract org.apache.lucene.search.spans.SpanWeight
cons public init(org.apache.lucene.search.spans.SpanQuery,org.apache.lucene.search.IndexSearcher,java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext>) throws java.io.IOException
fld protected final java.lang.String field
fld protected final org.apache.lucene.search.similarities.Similarity similarity
fld protected final org.apache.lucene.search.similarities.Similarity$SimWeight simWeight
innr public abstract static !enum Postings
meth public abstract org.apache.lucene.search.spans.Spans getSpans(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.spans.SpanWeight$Postings) throws java.io.IOException
meth public abstract void extractTermContexts(java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext>)
meth public float getValueForNormalization() throws java.io.IOException
meth public org.apache.lucene.search.Explanation explain(org.apache.lucene.index.LeafReaderContext,int) throws java.io.IOException
meth public org.apache.lucene.search.similarities.Similarity$SimScorer getSimScorer(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public org.apache.lucene.search.spans.SpanScorer scorer(org.apache.lucene.index.LeafReaderContext) throws java.io.IOException
meth public void normalize(float,float)
supr org.apache.lucene.search.Weight

CLSS public abstract static !enum org.apache.lucene.search.spans.SpanWeight$Postings
 outer org.apache.lucene.search.spans.SpanWeight
fld public final static org.apache.lucene.search.spans.SpanWeight$Postings OFFSETS
fld public final static org.apache.lucene.search.spans.SpanWeight$Postings PAYLOADS
fld public final static org.apache.lucene.search.spans.SpanWeight$Postings POSITIONS
meth public abstract int getRequiredPostings()
meth public org.apache.lucene.search.spans.SpanWeight$Postings atLeast(org.apache.lucene.search.spans.SpanWeight$Postings)
meth public static org.apache.lucene.search.spans.SpanWeight$Postings valueOf(java.lang.String)
meth public static org.apache.lucene.search.spans.SpanWeight$Postings[] values()
supr java.lang.Enum<org.apache.lucene.search.spans.SpanWeight$Postings>

CLSS public final org.apache.lucene.search.spans.SpanWithinQuery
cons public init(org.apache.lucene.search.spans.SpanQuery,org.apache.lucene.search.spans.SpanQuery)
innr public SpanWithinWeight
innr public abstract SpanContainWeight
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getField()
meth public java.lang.String toString(java.lang.String)
meth public org.apache.lucene.search.Query rewrite(org.apache.lucene.index.IndexReader) throws java.io.IOException
meth public org.apache.lucene.search.spans.SpanWeight createWeight(org.apache.lucene.search.IndexSearcher,boolean) throws java.io.IOException
supr org.apache.lucene.search.spans.SpanQuery

CLSS public org.apache.lucene.search.spans.SpanWithinQuery$SpanWithinWeight
 outer org.apache.lucene.search.spans.SpanWithinQuery
cons public init(org.apache.lucene.search.IndexSearcher,java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext>,org.apache.lucene.search.spans.SpanWeight,org.apache.lucene.search.spans.SpanWeight) throws java.io.IOException
meth public org.apache.lucene.search.spans.Spans getSpans(org.apache.lucene.index.LeafReaderContext,org.apache.lucene.search.spans.SpanWeight$Postings) throws java.io.IOException
meth public void extractTermContexts(java.util.Map<org.apache.lucene.index.Term,org.apache.lucene.index.TermContext>)
meth public void extractTerms(java.util.Set<org.apache.lucene.index.Term>)
supr org.apache.lucene.search.spans.SpanWeight

CLSS public abstract org.apache.lucene.search.spans.Spans
cons public init()
fld public final static int NO_MORE_POSITIONS = 2147483647
meth protected void doCurrentSpans() throws java.io.IOException
meth protected void doStartCurrentDoc() throws java.io.IOException
meth public abstract float positionsCost()
meth public abstract int endPosition()
meth public abstract int nextStartPosition() throws java.io.IOException
meth public abstract int startPosition()
meth public abstract int width()
meth public abstract void collect(org.apache.lucene.search.spans.SpanCollector) throws java.io.IOException
meth public java.lang.String toString()
meth public org.apache.lucene.search.TwoPhaseIterator asTwoPhaseIterator()
supr org.apache.lucene.search.DocIdSetIterator

CLSS public org.apache.lucene.search.spans.TermSpans
cons public init(org.apache.lucene.search.similarities.Similarity$SimScorer,org.apache.lucene.index.PostingsEnum,org.apache.lucene.index.Term,float)
fld protected boolean readPayload
fld protected final org.apache.lucene.index.PostingsEnum postings
fld protected final org.apache.lucene.index.Term term
fld protected int count
fld protected int doc
fld protected int freq
fld protected int position
meth public float positionsCost()
meth public int advance(int) throws java.io.IOException
meth public int docID()
meth public int endPosition()
meth public int nextDoc() throws java.io.IOException
meth public int nextStartPosition() throws java.io.IOException
meth public int startPosition()
meth public int width()
meth public java.lang.String toString()
meth public long cost()
meth public org.apache.lucene.index.PostingsEnum getPostings()
meth public void collect(org.apache.lucene.search.spans.SpanCollector) throws java.io.IOException
supr org.apache.lucene.search.spans.Spans
hfds positionsCost

CLSS abstract interface org.apache.lucene.search.spans.package-info

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
fld public final static int DEFAULT_BUFFERSIZE = 256
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
fld protected byte[] buffer
fld public final static int BUFFER_SIZE = 1024
fld public final static int MERGE_BUFFER_SIZE = 4096
fld public final static int MIN_BUFFER_SIZE = 8
intf org.apache.lucene.store.RandomAccessInput
meth protected abstract void readInternal(byte[],int,int) throws java.io.IOException
meth protected abstract void seekInternal(long) throws java.io.IOException
meth protected final int flushBuffer(org.apache.lucene.store.IndexOutput,long) throws java.io.IOException
meth protected void newBuffer(byte[])
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
meth public final void setBufferSize(int)
meth public org.apache.lucene.store.BufferedIndexInput clone()
meth public org.apache.lucene.store.IndexInput slice(java.lang.String,long,long) throws java.io.IOException
meth public static int bufferSize(org.apache.lucene.store.IOContext)
meth public static org.apache.lucene.store.BufferedIndexInput wrap(java.lang.String,org.apache.lucene.store.IndexInput,long,long)
supr org.apache.lucene.store.IndexInput
hfds bufferLength,bufferPosition,bufferSize,bufferStart
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
supr org.apache.lucene.store.DataOutput
hfds bytes,limit,pos

CLSS public abstract org.apache.lucene.store.ChecksumIndexInput
cons protected init(java.lang.String)
meth public abstract long getChecksum() throws java.io.IOException
meth public void seek(long) throws java.io.IOException
supr org.apache.lucene.store.IndexInput

CLSS public abstract org.apache.lucene.store.DataInput
cons public init()
intf java.lang.Cloneable
meth public abstract byte readByte() throws java.io.IOException
meth public abstract void readBytes(byte[],int,int) throws java.io.IOException
meth public int readInt() throws java.io.IOException
meth public int readVInt() throws java.io.IOException
meth public int readZInt() throws java.io.IOException
meth public java.lang.String readString() throws java.io.IOException
meth public java.util.Map<java.lang.String,java.lang.String> readMapOfStrings() throws java.io.IOException
meth public java.util.Map<java.lang.String,java.lang.String> readStringStringMap() throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public java.util.Set<java.lang.String> readSetOfStrings() throws java.io.IOException
meth public java.util.Set<java.lang.String> readStringSet() throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public long readLong() throws java.io.IOException
meth public long readVLong() throws java.io.IOException
meth public long readZLong() throws java.io.IOException
meth public org.apache.lucene.store.DataInput clone()
meth public short readShort() throws java.io.IOException
meth public void readBytes(byte[],int,int,boolean) throws java.io.IOException
meth public void skipBytes(long) throws java.io.IOException
supr java.lang.Object
hfds SKIP_BUFFER_SIZE,skipBuffer

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
meth public void writeStringSet(java.util.Set<java.lang.String>) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public void writeStringStringMap(java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds COPY_BUFFER_SIZE,copyBuffer

CLSS public abstract org.apache.lucene.store.Directory
cons public init()
intf java.io.Closeable
meth protected void ensureOpen()
meth public abstract java.lang.String[] listAll() throws java.io.IOException
meth public abstract long fileLength(java.lang.String) throws java.io.IOException
meth public abstract org.apache.lucene.store.IndexInput openInput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public abstract org.apache.lucene.store.IndexOutput createOutput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public abstract org.apache.lucene.store.Lock obtainLock(java.lang.String) throws java.io.IOException
meth public abstract void close() throws java.io.IOException
meth public abstract void deleteFile(java.lang.String) throws java.io.IOException
meth public abstract void renameFile(java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract void sync(java.util.Collection<java.lang.String>) throws java.io.IOException
meth public java.lang.String toString()
meth public org.apache.lucene.store.ChecksumIndexInput openChecksumInput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public void copyFrom(org.apache.lucene.store.Directory,java.lang.String,java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.apache.lucene.store.FSDirectory
cons protected init(java.nio.file.Path,org.apache.lucene.store.LockFactory) throws java.io.IOException
fld protected final java.nio.file.Path directory
meth protected void fsync(java.lang.String) throws java.io.IOException
meth public java.lang.String toString()
meth public java.lang.String[] listAll() throws java.io.IOException
meth public java.nio.file.Path getDirectory()
meth public long fileLength(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.store.IndexOutput createOutput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public static java.lang.String[] listAll(java.nio.file.Path) throws java.io.IOException
meth public static org.apache.lucene.store.FSDirectory open(java.nio.file.Path) throws java.io.IOException
meth public static org.apache.lucene.store.FSDirectory open(java.nio.file.Path,org.apache.lucene.store.LockFactory) throws java.io.IOException
meth public void close()
meth public void deleteFile(java.lang.String) throws java.io.IOException
meth public void renameFile(java.lang.String,java.lang.String) throws java.io.IOException
meth public void sync(java.util.Collection<java.lang.String>) throws java.io.IOException
supr org.apache.lucene.store.BaseDirectory
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
meth public long fileLength(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.store.Directory getPrimaryDir()
meth public org.apache.lucene.store.Directory getSecondaryDir()
meth public org.apache.lucene.store.IndexInput openInput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.IndexOutput createOutput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.Lock obtainLock(java.lang.String) throws java.io.IOException
meth public static java.lang.String getExtension(java.lang.String)
meth public void close() throws java.io.IOException
meth public void deleteFile(java.lang.String) throws java.io.IOException
meth public void renameFile(java.lang.String,java.lang.String) throws java.io.IOException
meth public void sync(java.util.Collection<java.lang.String>) throws java.io.IOException
supr org.apache.lucene.store.Directory
hfds doClose,primaryDir,primaryExtensions,secondaryDir

CLSS public org.apache.lucene.store.FilterDirectory
cons protected init(org.apache.lucene.store.Directory)
fld protected final org.apache.lucene.store.Directory in
meth public final org.apache.lucene.store.Directory getDelegate()
meth public java.lang.String toString()
meth public java.lang.String[] listAll() throws java.io.IOException
meth public long fileLength(java.lang.String) throws java.io.IOException
meth public org.apache.lucene.store.IndexInput openInput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.IndexOutput createOutput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.Lock obtainLock(java.lang.String) throws java.io.IOException
meth public static org.apache.lucene.store.Directory unwrap(org.apache.lucene.store.Directory)
meth public void close() throws java.io.IOException
meth public void deleteFile(java.lang.String) throws java.io.IOException
meth public void renameFile(java.lang.String,java.lang.String) throws java.io.IOException
meth public void sync(java.util.Collection<java.lang.String>) throws java.io.IOException
supr org.apache.lucene.store.Directory

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
fld public final boolean readOnce
fld public final org.apache.lucene.store.FlushInfo flushInfo
fld public final org.apache.lucene.store.IOContext$Context context
fld public final org.apache.lucene.store.MergeInfo mergeInfo
fld public final static org.apache.lucene.store.IOContext DEFAULT
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
intf java.lang.Cloneable
meth protected java.lang.String getFullSliceDescription(java.lang.String)
meth public abstract long getFilePointer()
meth public abstract long length()
meth public abstract org.apache.lucene.store.IndexInput slice(java.lang.String,long,long) throws java.io.IOException
meth public abstract void close() throws java.io.IOException
meth public abstract void seek(long) throws java.io.IOException
meth public java.lang.String toString()
meth public org.apache.lucene.store.IndexInput clone()
meth public org.apache.lucene.store.RandomAccessInput randomAccessSlice(long,long) throws java.io.IOException
supr org.apache.lucene.store.DataInput
hfds resourceDescription

CLSS public abstract org.apache.lucene.store.IndexOutput
cons protected init(java.lang.String)
intf java.io.Closeable
meth public abstract long getChecksum() throws java.io.IOException
meth public abstract long getFilePointer()
meth public abstract void close() throws java.io.IOException
meth public java.lang.String toString()
supr org.apache.lucene.store.DataOutput
hfds resourceDescription

CLSS public org.apache.lucene.store.InputStreamDataInput
cons public init(java.io.InputStream)
intf java.io.Closeable
meth public byte readByte() throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void readBytes(byte[],int,int) throws java.io.IOException
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
meth public void renameFile(java.lang.String,java.lang.String) throws java.io.IOException
meth public void sync(java.util.Collection<java.lang.String>) throws java.io.IOException
supr org.apache.lucene.store.FilterDirectory
hfds writeLock

CLSS public org.apache.lucene.store.LockVerifyServer
cons public init()
meth public static void main(java.lang.String[]) throws java.lang.Exception
supr java.lang.Object

CLSS public org.apache.lucene.store.MMapDirectory
cons public init(java.nio.file.Path) throws java.io.IOException
cons public init(java.nio.file.Path,int) throws java.io.IOException
cons public init(java.nio.file.Path,org.apache.lucene.store.LockFactory) throws java.io.IOException
cons public init(java.nio.file.Path,org.apache.lucene.store.LockFactory,int) throws java.io.IOException
fld public final static boolean UNMAP_SUPPORTED
fld public final static int DEFAULT_MAX_CHUNK_SIZE
fld public final static java.lang.String UNMAP_NOT_SUPPORTED_REASON
meth public boolean getPreload()
meth public boolean getUseUnmap()
meth public final int getMaxChunkSize()
meth public org.apache.lucene.store.IndexInput openInput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public void setPreload(boolean)
meth public void setUseUnmap(boolean)
supr org.apache.lucene.store.FSDirectory
hfds CLEANER,chunkSizePower,preload,useUnmapHack

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
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public long fileLength(java.lang.String) throws java.io.IOException
meth public long ramBytesUsed()
meth public org.apache.lucene.store.IndexInput openInput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.IndexOutput createOutput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void deleteFile(java.lang.String) throws java.io.IOException
meth public void renameFile(java.lang.String,java.lang.String) throws java.io.IOException
meth public void sync(java.util.Collection<java.lang.String>) throws java.io.IOException
supr org.apache.lucene.store.FilterDirectory
hfds VERBOSE,cache,maxCachedBytes,maxMergeSizeBytes,uncacheLock

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
cons public init(java.lang.String,java.io.OutputStream,int)
meth public final long getChecksum() throws java.io.IOException
meth public final long getFilePointer()
meth public final void writeByte(byte) throws java.io.IOException
meth public final void writeBytes(byte[],int,int) throws java.io.IOException
meth public void close() throws java.io.IOException
supr org.apache.lucene.store.IndexOutput
hfds bytesWritten,crc,flushedOnClose,os

CLSS public org.apache.lucene.store.RAMDirectory
cons public init()
cons public init(org.apache.lucene.store.FSDirectory,org.apache.lucene.store.IOContext) throws java.io.IOException
cons public init(org.apache.lucene.store.LockFactory)
fld protected final java.util.Map<java.lang.String,org.apache.lucene.store.RAMFile> fileMap
fld protected final java.util.concurrent.atomic.AtomicLong sizeInBytes
intf org.apache.lucene.util.Accountable
meth protected org.apache.lucene.store.RAMFile newRAMFile()
meth public final boolean fileNameExists(java.lang.String)
meth public final java.lang.String[] listAll()
meth public final long fileLength(java.lang.String) throws java.io.IOException
meth public final long ramBytesUsed()
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public org.apache.lucene.store.IndexInput openInput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public org.apache.lucene.store.IndexOutput createOutput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public void close()
meth public void deleteFile(java.lang.String) throws java.io.IOException
meth public void renameFile(java.lang.String,java.lang.String) throws java.io.IOException
meth public void sync(java.util.Collection<java.lang.String>) throws java.io.IOException
supr org.apache.lucene.store.BaseDirectory

CLSS public org.apache.lucene.store.RAMFile
cons public init()
fld protected final java.util.ArrayList<byte[]> buffers
fld protected long sizeInBytes
intf org.apache.lucene.util.Accountable
meth protected byte[] newBuffer(int)
meth protected final byte[] addBuffer(int)
meth protected final byte[] getBuffer(int)
meth protected final int numBuffers()
meth protected void setLength(long)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public long getLength()
meth public long ramBytesUsed()
supr java.lang.Object
hfds directory,length

CLSS public org.apache.lucene.store.RAMInputStream
cons public init(java.lang.String,org.apache.lucene.store.RAMFile) throws java.io.IOException
intf java.lang.Cloneable
meth public byte readByte() throws java.io.IOException
meth public long getFilePointer()
meth public long length()
meth public org.apache.lucene.store.IndexInput slice(java.lang.String,long,long) throws java.io.IOException
meth public void close()
meth public void readBytes(byte[],int,int) throws java.io.IOException
meth public void seek(long) throws java.io.IOException
supr org.apache.lucene.store.IndexInput
hfds bufferLength,bufferPosition,currentBuffer,currentBufferIndex,file,length

CLSS public org.apache.lucene.store.RAMOutputStream
cons public init()
cons public init(java.lang.String,org.apache.lucene.store.RAMFile,boolean)
cons public init(org.apache.lucene.store.RAMFile,boolean)
intf org.apache.lucene.util.Accountable
meth protected void flush() throws java.io.IOException
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public long getChecksum() throws java.io.IOException
meth public long getFilePointer()
meth public long ramBytesUsed()
meth public void close() throws java.io.IOException
meth public void reset()
meth public void writeByte(byte) throws java.io.IOException
meth public void writeBytes(byte[],int,int) throws java.io.IOException
meth public void writeTo(byte[],int) throws java.io.IOException
meth public void writeTo(org.apache.lucene.store.DataOutput) throws java.io.IOException
supr org.apache.lucene.store.IndexOutput
hfds BUFFER_SIZE,bufferLength,bufferPosition,bufferStart,crc,currentBuffer,currentBufferIndex,file

CLSS public abstract interface org.apache.lucene.store.RandomAccessInput
meth public abstract byte readByte(long) throws java.io.IOException
meth public abstract int readInt(long) throws java.io.IOException
meth public abstract long readLong(long) throws java.io.IOException
meth public abstract short readShort(long) throws java.io.IOException

CLSS public final org.apache.lucene.store.RateLimitedIndexOutput
cons public init(org.apache.lucene.store.RateLimiter,org.apache.lucene.store.IndexOutput)
meth public long getChecksum() throws java.io.IOException
meth public long getFilePointer()
meth public void close() throws java.io.IOException
meth public void writeByte(byte) throws java.io.IOException
meth public void writeBytes(byte[],int,int) throws java.io.IOException
supr org.apache.lucene.store.IndexOutput
hfds bytesSinceLastPause,currentMinPauseCheckBytes,delegate,rateLimiter

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

CLSS public org.apache.lucene.store.SimpleFSDirectory
cons public init(java.nio.file.Path) throws java.io.IOException
cons public init(java.nio.file.Path,org.apache.lucene.store.LockFactory) throws java.io.IOException
meth public org.apache.lucene.store.IndexInput openInput(java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
supr org.apache.lucene.store.FSDirectory
hcls SimpleFSIndexInput

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
meth public void copyFrom(org.apache.lucene.store.Directory,java.lang.String,java.lang.String,org.apache.lucene.store.IOContext) throws java.io.IOException
meth public void deleteFile(java.lang.String) throws java.io.IOException
meth public void renameFile(java.lang.String,java.lang.String) throws java.io.IOException
supr org.apache.lucene.store.FilterDirectory
hfds createdFileNames

CLSS public final org.apache.lucene.store.VerifyingLockFactory
cons public init(org.apache.lucene.store.LockFactory,java.io.InputStream,java.io.OutputStream) throws java.io.IOException
meth public org.apache.lucene.store.Lock obtainLock(org.apache.lucene.store.Directory,java.lang.String) throws java.io.IOException
supr org.apache.lucene.store.LockFactory
hfds in,lf,out
hcls CheckedLock

CLSS abstract interface org.apache.lucene.store.package-info

CLSS public abstract interface org.apache.lucene.util.Accountable
meth public abstract java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public abstract long ramBytesUsed()

CLSS public org.apache.lucene.util.Accountables
meth public static java.lang.String toString(org.apache.lucene.util.Accountable)
meth public static java.util.Collection<org.apache.lucene.util.Accountable> namedAccountables(java.lang.String,java.util.Map<?,? extends org.apache.lucene.util.Accountable>)
meth public static org.apache.lucene.util.Accountable namedAccountable(java.lang.String,java.util.Collection<org.apache.lucene.util.Accountable>,long)
meth public static org.apache.lucene.util.Accountable namedAccountable(java.lang.String,long)
meth public static org.apache.lucene.util.Accountable namedAccountable(java.lang.String,org.apache.lucene.util.Accountable)
supr java.lang.Object

CLSS public final org.apache.lucene.util.ArrayUtil
fld public final static int MAX_ARRAY_LENGTH
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{%%0}> naturalComparator()
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> void introSort({%%0}[])
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> void introSort({%%0}[],int,int)
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> void timSort({%%0}[])
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> void timSort({%%0}[],int,int)
meth public static <%0 extends java.lang.Object> void introSort({%%0}[],int,int,java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> void introSort({%%0}[],java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> void swap({%%0}[],int,int)
meth public static <%0 extends java.lang.Object> void timSort({%%0}[],int,int,java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> void timSort({%%0}[],java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> {%%0}[] grow({%%0}[],int)
meth public static boolean equals(byte[],int,byte[],int,int)
meth public static boolean equals(char[],int,char[],int,int)
meth public static boolean equals(int[],int,int[],int,int)
meth public static boolean[] grow(boolean[])
meth public static boolean[] grow(boolean[],int)
meth public static boolean[] shrink(boolean[],int)
meth public static byte[] grow(byte[])
meth public static byte[] grow(byte[],int)
meth public static byte[] shrink(byte[],int)
meth public static char[] grow(char[])
meth public static char[] grow(char[],int)
meth public static char[] shrink(char[],int)
meth public static double[] grow(double[])
meth public static double[] grow(double[],int)
meth public static float[] grow(float[])
meth public static float[] grow(float[],int)
meth public static float[][] grow(float[][])
meth public static float[][] grow(float[][],int)
meth public static float[][] shrink(float[][],int)
meth public static int getShrinkSize(int,int,int)
meth public static int hashCode(byte[],int,int)
meth public static int hashCode(char[],int,int)
meth public static int oversize(int,int)
meth public static int parseInt(char[])
meth public static int parseInt(char[],int,int)
meth public static int parseInt(char[],int,int,int)
meth public static int[] grow(int[])
meth public static int[] grow(int[],int)
meth public static int[] shrink(int[],int)
meth public static int[] toIntArray(java.util.Collection<java.lang.Integer>)
meth public static int[][] grow(int[][])
meth public static int[][] grow(int[][],int)
meth public static int[][] shrink(int[][],int)
meth public static long[] grow(long[])
meth public static long[] grow(long[],int)
meth public static long[] shrink(long[],int)
meth public static short[] grow(short[])
meth public static short[] grow(short[],int)
meth public static short[] shrink(short[],int)
supr java.lang.Object
hfds NATURAL_COMPARATOR
hcls NaturalComparator

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
cons public init(org.apache.lucene.util.AttributeFactory,java.lang.Class<{org.apache.lucene.util.AttributeFactory$StaticImplementationAttributeFactory%0}>)
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
meth public final java.lang.String reflectAsString(boolean)
meth public org.apache.lucene.util.AttributeImpl clone()
meth public void end()
meth public void reflectWith(org.apache.lucene.util.AttributeReflector)
supr java.lang.Object

CLSS public abstract interface org.apache.lucene.util.AttributeReflector
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
innr public final static Builder
meth public boolean isCacheable()
meth public java.lang.String toString()
meth public long ramBytesUsed()
meth public org.apache.lucene.search.DocIdSetIterator iterator()
meth public org.apache.lucene.util.BitSet bits()
supr org.apache.lucene.search.DocIdSet
hfds BASE_RAM_BYTES_USED,cost,set

CLSS public final static org.apache.lucene.util.BitDocIdSet$Builder
 outer org.apache.lucene.util.BitDocIdSet
cons public init(int)
cons public init(int,boolean)
meth public boolean isDefinitelyEmpty()
meth public org.apache.lucene.util.BitDocIdSet build()
meth public void and(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public void andNot(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public void or(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
supr java.lang.Object
hfds costUpperBound,denseSet,maxDoc,sparseSet,threshold

CLSS public abstract org.apache.lucene.util.BitSet
cons public init()
intf org.apache.lucene.util.Accountable
intf org.apache.lucene.util.MutableBits
meth protected final void assertUnpositioned(org.apache.lucene.search.DocIdSetIterator)
meth public abstract int cardinality()
meth public abstract int nextSetBit(int)
meth public abstract int prevSetBit(int)
meth public abstract void clear(int,int)
meth public abstract void set(int)
meth public int approximateCardinality()
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public static org.apache.lucene.util.BitSet of(org.apache.lucene.search.DocIdSetIterator,int) throws java.io.IOException
meth public void and(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public void andNot(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public void or(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
supr java.lang.Object
hcls LeapFrogCallBack

CLSS public org.apache.lucene.util.BitSetIterator
cons public init(org.apache.lucene.util.BitSet,long)
meth public int advance(int)
meth public int docID()
meth public int nextDoc()
meth public long cost()
meth public static org.apache.lucene.util.FixedBitSet getFixedBitSetOrNull(org.apache.lucene.search.DocIdSetIterator)
meth public static org.apache.lucene.util.SparseFixedBitSet getSparseFixedBitSetOrNull(org.apache.lucene.search.DocIdSetIterator)
supr org.apache.lucene.search.DocIdSetIterator
hfds bits,cost,doc,length

CLSS public final org.apache.lucene.util.BitUtil
meth public final static long flipFlop(long)
meth public static int bitCount(byte)
 anno 0 java.lang.Deprecated()
meth public static int bitList(byte)
 anno 0 java.lang.Deprecated()
meth public static int nextHighestPowerOfTwo(int)
meth public static int zigZagDecode(int)
meth public static int zigZagEncode(int)
meth public static long deinterleave(long)
meth public static long interleave(long,long)
meth public static long nextHighestPowerOfTwo(long)
meth public static long pop_andnot(long[],long[],int,int)
meth public static long pop_array(long[],int,int)
meth public static long pop_intersect(long[],long[],int,int)
meth public static long pop_union(long[],long[],int,int)
meth public static long pop_xor(long[],long[],int,int)
meth public static long zigZagDecode(long)
meth public static long zigZagEncode(long)
supr java.lang.Object
hfds BIT_LISTS,BYTE_COUNTS,MAGIC,SHIFT

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
fld public byte[][] buffers
fld public final static int BYTE_BLOCK_MASK = 32767
fld public final static int BYTE_BLOCK_SHIFT = 15
fld public final static int BYTE_BLOCK_SIZE = 32768
fld public final static int FIRST_LEVEL_SIZE
fld public final static int[] LEVEL_SIZE_ARRAY
fld public final static int[] NEXT_LEVEL_ARRAY
fld public int byteOffset
fld public int byteUpto
innr public abstract static Allocator
innr public final static DirectAllocator
innr public static DirectTrackingAllocator
meth public int allocSlice(byte[],int)
meth public int newSlice(int)
meth public void append(org.apache.lucene.util.BytesRef)
meth public void nextBuffer()
meth public void readBytes(long,byte[],int,int)
meth public void reset()
meth public void reset(boolean,boolean)
meth public void setBytesRef(org.apache.lucene.util.BytesRef,int)
supr java.lang.Object
hfds allocator,bufferUpto

CLSS public abstract static org.apache.lucene.util.ByteBlockPool$Allocator
 outer org.apache.lucene.util.ByteBlockPool
cons public init(int)
fld protected final int blockSize
meth public abstract void recycleByteBlocks(byte[][],int,int)
meth public byte[] getByteBlock()
meth public void recycleByteBlocks(java.util.List<byte[]>)
supr java.lang.Object

CLSS public final static org.apache.lucene.util.ByteBlockPool$DirectAllocator
 outer org.apache.lucene.util.ByteBlockPool
cons public init()
cons public init(int)
meth public void recycleByteBlocks(byte[][],int,int)
supr org.apache.lucene.util.ByteBlockPool$Allocator

CLSS public static org.apache.lucene.util.ByteBlockPool$DirectTrackingAllocator
 outer org.apache.lucene.util.ByteBlockPool
cons public init(int,org.apache.lucene.util.Counter)
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
meth public static java.util.Comparator<org.apache.lucene.util.BytesRef> getUTF8SortedAsUTF16Comparator()
 anno 0 java.lang.Deprecated()
meth public static java.util.Comparator<org.apache.lucene.util.BytesRef> getUTF8SortedAsUnicodeComparator()
meth public static org.apache.lucene.util.BytesRef deepCopyOf(org.apache.lucene.util.BytesRef)
supr java.lang.Object
hfds utf8SortedAsUTF16SortOrder,utf8SortedAsUnicodeSortOrder
hcls UTF8SortedAsUTF16Comparator,UTF8SortedAsUnicodeComparator

CLSS public final org.apache.lucene.util.BytesRefArray
cons public init(org.apache.lucene.util.Counter)
meth public int append(org.apache.lucene.util.BytesRef)
meth public int size()
meth public org.apache.lucene.util.BytesRef get(org.apache.lucene.util.BytesRefBuilder,int)
meth public org.apache.lucene.util.BytesRefIterator iterator()
meth public org.apache.lucene.util.BytesRefIterator iterator(java.util.Comparator<org.apache.lucene.util.BytesRef>)
meth public void clear()
supr java.lang.Object
hfds bytesUsed,currentOffset,lastElement,offsets,pool

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

CLSS public final org.apache.lucene.util.BytesRefHash
cons public init()
cons public init(org.apache.lucene.util.ByteBlockPool)
cons public init(org.apache.lucene.util.ByteBlockPool,int,org.apache.lucene.util.BytesRefHash$BytesStartArray)
fld public final static int DEFAULT_CAPACITY = 16
innr public abstract static BytesStartArray
innr public static DirectBytesStartArray
innr public static MaxBytesLengthExceededException
meth public int add(org.apache.lucene.util.BytesRef)
meth public int addByPoolOffset(int)
meth public int byteStart(int)
meth public int find(org.apache.lucene.util.BytesRef)
meth public int size()
meth public int[] sort(java.util.Comparator<org.apache.lucene.util.BytesRef>)
meth public org.apache.lucene.util.BytesRef get(int,org.apache.lucene.util.BytesRef)
meth public void clear()
meth public void clear(boolean)
meth public void close()
meth public void reinit()
supr java.lang.Object
hfds bytesStart,bytesStartArray,bytesUsed,count,hashHalfSize,hashMask,hashSize,ids,lastCount,pool,scratch1

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
meth public static java.util.Comparator<org.apache.lucene.util.CharsRef> getUTF16SortedAsUTF8Comparator()
 anno 0 java.lang.Deprecated()
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
fld public final static boolean JRE_IS_64BIT
fld public final static boolean JRE_IS_MINIMUM_JAVA8
fld public final static boolean JRE_IS_MINIMUM_JAVA9
fld public final static boolean LINUX
fld public final static boolean MAC_OS_X
fld public final static boolean SUN_OS
fld public final static boolean WINDOWS
fld public final static java.lang.String JAVA_VENDOR
fld public final static java.lang.String JAVA_VERSION
fld public final static java.lang.String JVM_NAME
fld public final static java.lang.String JVM_SPEC_VERSION
fld public final static java.lang.String JVM_VENDOR
fld public final static java.lang.String JVM_VERSION
fld public final static java.lang.String LUCENE_MAIN_VERSION
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String LUCENE_VERSION
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String OS_ARCH
fld public final static java.lang.String OS_NAME
fld public final static java.lang.String OS_VERSION
supr java.lang.Object
hfds JVM_MAJOR_VERSION,JVM_MINOR_VERSION

CLSS public abstract org.apache.lucene.util.Counter
cons public init()
meth public abstract long addAndGet(long)
meth public abstract long get()
meth public static org.apache.lucene.util.Counter newCounter()
meth public static org.apache.lucene.util.Counter newCounter(boolean)
supr java.lang.Object
hcls AtomicCounter,SerialCounter

CLSS public final org.apache.lucene.util.DocIdSetBuilder
cons public init(int)
meth public org.apache.lucene.search.DocIdSet build()
meth public org.apache.lucene.search.DocIdSet build(long)
meth public void add(int)
meth public void add(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
meth public void grow(int)
supr java.lang.Object
hfds bitSet,buffer,bufferSize,maxDoc,threshold

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
intf org.apache.lucene.util.Accountable
intf org.apache.lucene.util.MutableBits
meth public boolean equals(java.lang.Object)
meth public boolean get(int)
meth public boolean getAndClear(int)
meth public boolean getAndSet(int)
meth public boolean intersects(org.apache.lucene.util.FixedBitSet)
meth public boolean scanIsEmpty()
meth public int cardinality()
meth public int hashCode()
meth public int length()
meth public int nextSetBit(int)
meth public int prevSetBit(int)
meth public long ramBytesUsed()
meth public long[] getBits()
meth public org.apache.lucene.util.FixedBitSet clone()
meth public static int bits2words(int)
meth public static long andNotCount(org.apache.lucene.util.FixedBitSet,org.apache.lucene.util.FixedBitSet)
meth public static long intersectionCount(org.apache.lucene.util.FixedBitSet,org.apache.lucene.util.FixedBitSet)
meth public static long unionCount(org.apache.lucene.util.FixedBitSet,org.apache.lucene.util.FixedBitSet)
meth public static org.apache.lucene.util.FixedBitSet ensureCapacity(org.apache.lucene.util.FixedBitSet,int)
meth public void and(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
meth public void and(org.apache.lucene.util.FixedBitSet)
meth public void andNot(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
meth public void andNot(org.apache.lucene.util.FixedBitSet)
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
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
meth public long ramBytesUsed()
meth public void add(int)
supr java.lang.Object
hfds BASE_RAM_BYTES_USED,buffer,frequencies,maxSize,position
hcls IntBag

CLSS public final org.apache.lucene.util.IOUtils
fld public final static java.lang.String UTF_8
fld public final static java.nio.charset.Charset CHARSET_UTF_8
 anno 0 java.lang.Deprecated()
meth public !varargs static void close(java.io.Closeable[]) throws java.io.IOException
meth public !varargs static void closeWhileHandlingException(java.io.Closeable[])
meth public !varargs static void deleteFilesIfExist(java.nio.file.Path[]) throws java.io.IOException
meth public !varargs static void deleteFilesIgnoringExceptions(java.nio.file.Path[])
meth public !varargs static void deleteFilesIgnoringExceptions(org.apache.lucene.store.Directory,java.lang.String[])
meth public !varargs static void rm(java.nio.file.Path[]) throws java.io.IOException
meth public static boolean spins(java.nio.file.Path) throws java.io.IOException
meth public static boolean spins(org.apache.lucene.store.Directory) throws java.io.IOException
meth public static java.io.Reader getDecodingReader(java.io.InputStream,java.nio.charset.Charset)
meth public static java.io.Reader getDecodingReader(java.lang.Class<?>,java.lang.String,java.nio.charset.Charset) throws java.io.IOException
meth public static void close(java.lang.Iterable<? extends java.io.Closeable>) throws java.io.IOException
meth public static void closeWhileHandlingException(java.lang.Iterable<? extends java.io.Closeable>)
meth public static void deleteFilesIfExist(java.util.Collection<? extends java.nio.file.Path>) throws java.io.IOException
meth public static void deleteFilesIgnoringExceptions(java.util.Collection<? extends java.nio.file.Path>)
meth public static void fsync(java.nio.file.Path,boolean) throws java.io.IOException
meth public static void reThrow(java.lang.Throwable) throws java.io.IOException
meth public static void reThrowUnchecked(java.lang.Throwable)
supr java.lang.Object

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

CLSS public final org.apache.lucene.util.IntBlockPool
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
innr public final static SliceReader
innr public static SliceWriter
meth public void nextBuffer()
meth public void reset()
meth public void reset(boolean,boolean)
supr java.lang.Object
hfds FIRST_LEVEL_SIZE,LEVEL_SIZE_ARRAY,NEXT_LEVEL_ARRAY,allocator,bufferUpto

CLSS public abstract static org.apache.lucene.util.IntBlockPool$Allocator
 outer org.apache.lucene.util.IntBlockPool
cons public init(int)
fld protected final int blockSize
meth public abstract void recycleIntBlocks(int[][],int,int)
meth public int[] getIntBlock()
supr java.lang.Object

CLSS public final static org.apache.lucene.util.IntBlockPool$DirectAllocator
 outer org.apache.lucene.util.IntBlockPool
cons public init()
meth public void recycleIntBlocks(int[][],int,int)
supr org.apache.lucene.util.IntBlockPool$Allocator

CLSS public final static org.apache.lucene.util.IntBlockPool$SliceReader
 outer org.apache.lucene.util.IntBlockPool
cons public init(org.apache.lucene.util.IntBlockPool)
meth public boolean endOfSlice()
meth public int readInt()
meth public void reset(int,int)
supr java.lang.Object
hfds buffer,bufferOffset,bufferUpto,end,level,limit,pool,upto

CLSS public static org.apache.lucene.util.IntBlockPool$SliceWriter
 outer org.apache.lucene.util.IntBlockPool
cons public init(org.apache.lucene.util.IntBlockPool)
meth public int getCurrentOffset()
meth public int startNewSlice()
meth public void reset(int)
meth public void writeInt(int)
supr java.lang.Object
hfds offset,pool

CLSS public abstract org.apache.lucene.util.IntroSorter
cons public init()
meth protected abstract int comparePivot(int)
meth protected abstract void setPivot(int)
meth public final void sort(int,int)
supr org.apache.lucene.util.Sorter

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

CLSS public final org.apache.lucene.util.LongBitSet
cons public init(long)
cons public init(long[],long)
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
hfds bits,numBits,numWords

CLSS public abstract org.apache.lucene.util.LongValues
cons public init()
fld public final static org.apache.lucene.util.LongValues IDENTITY
meth public abstract long get(long)
meth public long get(int)
supr org.apache.lucene.index.NumericDocValues

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

CLSS public abstract interface org.apache.lucene.util.MutableBits
intf org.apache.lucene.util.Bits
meth public abstract void clear(int)

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
meth public boolean isCacheable()
meth public long ramBytesUsed()
meth public org.apache.lucene.search.DocIdSetIterator iterator() throws java.io.IOException
meth public org.apache.lucene.util.Bits bits() throws java.io.IOException
supr org.apache.lucene.search.DocIdSet
hfds BASE_RAM_BYTES_USED,in,maxDoc

CLSS public final org.apache.lucene.util.NumericUtils
fld public final static byte SHIFT_START_INT = 96
fld public final static byte SHIFT_START_LONG = 32
fld public final static int BUF_SIZE_INT = 6
fld public final static int BUF_SIZE_LONG = 11
fld public final static int PRECISION_STEP_DEFAULT = 16
fld public final static int PRECISION_STEP_DEFAULT_32 = 8
innr public abstract static IntRangeBuilder
innr public abstract static LongRangeBuilder
meth public static double sortableLongToDouble(long)
meth public static float sortableIntToFloat(int)
meth public static int floatToSortableInt(float)
meth public static int getPrefixCodedIntShift(org.apache.lucene.util.BytesRef)
meth public static int getPrefixCodedLongShift(org.apache.lucene.util.BytesRef)
meth public static int prefixCodedToInt(org.apache.lucene.util.BytesRef)
meth public static int sortableFloatBits(int)
meth public static java.lang.Integer getMaxInt(org.apache.lucene.index.Terms) throws java.io.IOException
meth public static java.lang.Integer getMinInt(org.apache.lucene.index.Terms) throws java.io.IOException
meth public static java.lang.Long getMaxLong(org.apache.lucene.index.Terms) throws java.io.IOException
meth public static java.lang.Long getMinLong(org.apache.lucene.index.Terms) throws java.io.IOException
meth public static long doubleToSortableLong(double)
meth public static long prefixCodedToLong(org.apache.lucene.util.BytesRef)
meth public static long sortableDoubleBits(long)
meth public static org.apache.lucene.index.TermsEnum filterPrefixCodedInts(org.apache.lucene.index.TermsEnum)
meth public static org.apache.lucene.index.TermsEnum filterPrefixCodedLongs(org.apache.lucene.index.TermsEnum)
meth public static void intToPrefixCoded(int,int,org.apache.lucene.util.BytesRefBuilder)
meth public static void intToPrefixCodedBytes(int,int,org.apache.lucene.util.BytesRefBuilder)
 anno 0 java.lang.Deprecated()
meth public static void longToPrefixCoded(long,int,org.apache.lucene.util.BytesRefBuilder)
meth public static void longToPrefixCodedBytes(long,int,org.apache.lucene.util.BytesRefBuilder)
 anno 0 java.lang.Deprecated()
meth public static void splitIntRange(org.apache.lucene.util.NumericUtils$IntRangeBuilder,int,int,int)
meth public static void splitLongRange(org.apache.lucene.util.NumericUtils$LongRangeBuilder,int,long,long)
supr java.lang.Object
hcls SeekingNumericFilteredTermsEnum

CLSS public abstract static org.apache.lucene.util.NumericUtils$IntRangeBuilder
 outer org.apache.lucene.util.NumericUtils
cons public init()
meth public void addRange(int,int,int)
meth public void addRange(org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef)
supr java.lang.Object

CLSS public abstract static org.apache.lucene.util.NumericUtils$LongRangeBuilder
 outer org.apache.lucene.util.NumericUtils
cons public init()
meth public void addRange(long,long,int)
meth public void addRange(org.apache.lucene.util.BytesRef,org.apache.lucene.util.BytesRef)
supr java.lang.Object

CLSS public final org.apache.lucene.util.OfflineSorter
cons public init() throws java.io.IOException
cons public init(java.util.Comparator<org.apache.lucene.util.BytesRef>) throws java.io.IOException
cons public init(java.util.Comparator<org.apache.lucene.util.BytesRef>,org.apache.lucene.util.OfflineSorter$BufferSize,java.nio.file.Path,int)
fld public final static int MAX_TEMPFILES = 128
fld public final static java.util.Comparator<org.apache.lucene.util.BytesRef> DEFAULT_COMPARATOR
fld public final static long ABSOLUTE_MIN_SORT_BUFFER_SIZE = 524288
fld public final static long GB = 1073741824
fld public final static long MB = 1048576
fld public final static long MIN_BUFFER_SIZE_MB = 32
innr public SortInfo
innr public final static BufferSize
innr public static ByteSequencesReader
innr public static ByteSequencesWriter
meth protected java.nio.file.Path sortPartition(int) throws java.io.IOException
meth public java.util.Comparator<org.apache.lucene.util.BytesRef> getComparator()
meth public org.apache.lucene.util.OfflineSorter$SortInfo sort(java.nio.file.Path,java.nio.file.Path) throws java.io.IOException
meth public static java.nio.file.Path getDefaultTempDir() throws java.io.IOException
supr java.lang.Object
hfds DEFAULT_TEMP_DIR,MIN_BUFFER_SIZE_MSG,buffer,bufferBytesUsed,comparator,maxTempFiles,ramBufferSize,sortInfo,tempDirectory
hcls FileAndTop

CLSS public final static org.apache.lucene.util.OfflineSorter$BufferSize
 outer org.apache.lucene.util.OfflineSorter
meth public static org.apache.lucene.util.OfflineSorter$BufferSize automatic()
meth public static org.apache.lucene.util.OfflineSorter$BufferSize megabytes(long)
supr java.lang.Object
hfds bytes

CLSS public static org.apache.lucene.util.OfflineSorter$ByteSequencesReader
 outer org.apache.lucene.util.OfflineSorter
cons public init(java.io.DataInput)
cons public init(java.nio.file.Path) throws java.io.IOException
intf java.io.Closeable
meth public boolean read(org.apache.lucene.util.BytesRefBuilder) throws java.io.IOException
meth public byte[] read() throws java.io.IOException
meth public void close() throws java.io.IOException
supr java.lang.Object
hfds is

CLSS public static org.apache.lucene.util.OfflineSorter$ByteSequencesWriter
 outer org.apache.lucene.util.OfflineSorter
cons public init(java.io.DataOutput)
cons public init(java.nio.file.Path) throws java.io.IOException
intf java.io.Closeable
meth public void close() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(org.apache.lucene.util.BytesRef) throws java.io.IOException
supr java.lang.Object
hfds os

CLSS public org.apache.lucene.util.OfflineSorter$SortInfo
 outer org.apache.lucene.util.OfflineSorter
cons public init(org.apache.lucene.util.OfflineSorter)
fld public final long bufferSize
fld public int lines
fld public int mergeRounds
fld public int tempMergeFiles
fld public long mergeTime
fld public long readTime
fld public long sortTime
fld public long totalTime
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.apache.lucene.util.PagedBytes
cons public init(int)
innr public final PagedBytesDataInput
innr public final PagedBytesDataOutput
innr public final static Reader
intf org.apache.lucene.util.Accountable
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
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
meth public java.lang.String toString()
meth public java.util.Collection<org.apache.lucene.util.Accountable> getChildResources()
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
cons public init(int,boolean)
intf java.lang.Iterable<{org.apache.lucene.util.PriorityQueue%0}>
meth protected abstract boolean lessThan({org.apache.lucene.util.PriorityQueue%0},{org.apache.lucene.util.PriorityQueue%0})
meth protected final java.lang.Object[] getHeapArray()
meth protected {org.apache.lucene.util.PriorityQueue%0} getSentinelObject()
meth public final boolean remove({org.apache.lucene.util.PriorityQueue%0})
meth public final int size()
meth public final void clear()
meth public final {org.apache.lucene.util.PriorityQueue%0} add({org.apache.lucene.util.PriorityQueue%0})
meth public final {org.apache.lucene.util.PriorityQueue%0} pop()
meth public final {org.apache.lucene.util.PriorityQueue%0} top()
meth public final {org.apache.lucene.util.PriorityQueue%0} updateTop()
meth public final {org.apache.lucene.util.PriorityQueue%0} updateTop({org.apache.lucene.util.PriorityQueue%0})
meth public java.util.Iterator<{org.apache.lucene.util.PriorityQueue%0}> iterator()
meth public {org.apache.lucene.util.PriorityQueue%0} insertWithOverflow({org.apache.lucene.util.PriorityQueue%0})
supr java.lang.Object
hfds heap,maxSize,size

CLSS public org.apache.lucene.util.QueryBuilder
cons public init(org.apache.lucene.analysis.Analyzer)
meth protected final org.apache.lucene.search.Query createFieldQuery(org.apache.lucene.analysis.Analyzer,org.apache.lucene.search.BooleanClause$Occur,java.lang.String,java.lang.String,boolean,int)
meth protected org.apache.lucene.search.BooleanQuery$Builder newBooleanQuery(boolean)
meth protected org.apache.lucene.search.MultiPhraseQuery newMultiPhraseQuery()
meth protected org.apache.lucene.search.Query newTermQuery(org.apache.lucene.index.Term)
meth public boolean getEnablePositionIncrements()
meth public org.apache.lucene.analysis.Analyzer getAnalyzer()
meth public org.apache.lucene.search.Query createBooleanQuery(java.lang.String,java.lang.String)
meth public org.apache.lucene.search.Query createBooleanQuery(java.lang.String,java.lang.String,org.apache.lucene.search.BooleanClause$Occur)
meth public org.apache.lucene.search.Query createMinShouldMatchQuery(java.lang.String,java.lang.String,float)
meth public org.apache.lucene.search.Query createPhraseQuery(java.lang.String,java.lang.String)
meth public org.apache.lucene.search.Query createPhraseQuery(java.lang.String,java.lang.String,int)
meth public void setAnalyzer(org.apache.lucene.analysis.Analyzer)
meth public void setEnablePositionIncrements(boolean)
supr java.lang.Object
hfds analyzer,enablePositionIncrements

CLSS public final org.apache.lucene.util.RamUsageEstimator
fld public final static boolean COMPRESSED_REFS_ENABLED
fld public final static int NUM_BYTES_ARRAY_HEADER
fld public final static int NUM_BYTES_BOOLEAN = 1
fld public final static int NUM_BYTES_BYTE = 1
fld public final static int NUM_BYTES_CHAR = 2
fld public final static int NUM_BYTES_DOUBLE = 8
fld public final static int NUM_BYTES_FLOAT = 4
fld public final static int NUM_BYTES_INT = 4
fld public final static int NUM_BYTES_LONG = 8
fld public final static int NUM_BYTES_OBJECT_ALIGNMENT
fld public final static int NUM_BYTES_OBJECT_HEADER
fld public final static int NUM_BYTES_OBJECT_REF
fld public final static int NUM_BYTES_SHORT = 2
fld public final static long ONE_GB = 1073741824
fld public final static long ONE_KB = 1024
fld public final static long ONE_MB = 1048576
meth public static java.lang.String humanReadableUnits(long)
meth public static java.lang.String humanReadableUnits(long,java.text.DecimalFormat)
meth public static long alignObjectSize(long)
meth public static long shallowSizeOf(java.lang.Object)
meth public static long shallowSizeOf(java.lang.Object[])
meth public static long shallowSizeOfInstance(java.lang.Class<?>)
meth public static long sizeOf(boolean[])
meth public static long sizeOf(byte[])
meth public static long sizeOf(char[])
meth public static long sizeOf(double[])
meth public static long sizeOf(float[])
meth public static long sizeOf(int[])
meth public static long sizeOf(java.lang.Long)
meth public static long sizeOf(long[])
meth public static long sizeOf(org.apache.lucene.util.Accountable[])
meth public static long sizeOf(short[])
supr java.lang.Object
hfds HOTSPOT_BEAN_CLASS,JVM_IS_HOTSPOT_64BIT,LONG_CACHE_MAX_VALUE,LONG_CACHE_MIN_VALUE,LONG_SIZE,MANAGEMENT_FACTORY_CLASS,primitiveSizes

CLSS public final org.apache.lucene.util.RecyclingByteBlockAllocator
cons public init()
cons public init(int,int)
cons public init(int,int,org.apache.lucene.util.Counter)
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

CLSS public org.apache.lucene.util.RoaringDocIdSet
innr public static Builder
meth public boolean isCacheable()
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
meth public int getMaxPos()
meth public void freeBefore(int)
meth public void reset()
meth public {org.apache.lucene.util.RollingBuffer%0} get(int)
supr java.lang.Object
hfds buffer,count,nextPos,nextWrite

CLSS public abstract interface static org.apache.lucene.util.RollingBuffer$Resettable
 outer org.apache.lucene.util.RollingBuffer
meth public abstract void reset()

CLSS public final org.apache.lucene.util.SPIClassIterator<%0 extends java.lang.Object>
intf java.util.Iterator<java.lang.Class<? extends {org.apache.lucene.util.SPIClassIterator%0}>>
meth public boolean hasNext()
meth public java.lang.Class<? extends {org.apache.lucene.util.SPIClassIterator%0}> next()
meth public static <%0 extends java.lang.Object> org.apache.lucene.util.SPIClassIterator<{%%0}> get(java.lang.Class<{%%0}>)
meth public static <%0 extends java.lang.Object> org.apache.lucene.util.SPIClassIterator<{%%0}> get(java.lang.Class<{%%0}>,java.lang.ClassLoader)
meth public static boolean isParentClassLoader(java.lang.ClassLoader,java.lang.ClassLoader)
meth public void remove()
supr java.lang.Object
hfds META_INF_SERVICES,clazz,linesIterator,loader,profilesEnum

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
meth public final void set({org.apache.lucene.util.SetOnce%0})
meth public final {org.apache.lucene.util.SetOnce%0} get()
supr java.lang.Object
hfds obj,set

CLSS public final static org.apache.lucene.util.SetOnce$AlreadySetException
 outer org.apache.lucene.util.SetOnce
cons public init()
supr java.lang.IllegalStateException

CLSS public org.apache.lucene.util.SloppyMath
cons public init()
fld public final static double PIO2 = 1.5707963267948966
fld public final static double TO_DEGREES = 57.29577951308232
fld public final static double TO_RADIANS = 0.017453292519943295
meth public static double asin(double)
meth public static double cos(double)
meth public static double earthDiameter(double)
meth public static double haversin(double,double,double,double)
meth public static double sin(double)
meth public static double tan(double)
supr java.lang.Object
hfds ASIN_DELTA,ASIN_INDEXER,ASIN_MAX_VALUE_FOR_TABS,ASIN_PIO2_HI,ASIN_PIO2_LO,ASIN_PS0,ASIN_PS1,ASIN_PS2,ASIN_PS3,ASIN_PS4,ASIN_PS5,ASIN_QS1,ASIN_QS2,ASIN_QS3,ASIN_QS4,ASIN_TABS_SIZE,ONE_DIV_F2,ONE_DIV_F3,ONE_DIV_F4,PIO2_HI,PIO2_LO,RADIUS_DELTA,RADIUS_INDEXER,RADIUS_TABS_SIZE,SIN_COS_DELTA_HI,SIN_COS_DELTA_LO,SIN_COS_INDEXER,SIN_COS_MAX_VALUE_FOR_INT_MODULO,SIN_COS_TABS_SIZE,TWOPI_HI,TWOPI_LO,asinDer1DivF1Tab,asinDer2DivF2Tab,asinDer3DivF3Tab,asinDer4DivF4Tab,asinTab,cosTab,earthDiameterPerLatitude,sinTab

CLSS public org.apache.lucene.util.SmallFloat
meth public static byte floatToByte(float,int,int)
meth public static byte floatToByte315(float)
meth public static byte floatToByte52(float)
meth public static float byte315ToFloat(byte)
meth public static float byte52ToFloat(byte)
meth public static float byteToFloat(byte,int,int)
supr java.lang.Object

CLSS public abstract org.apache.lucene.util.Sorter
cons protected init()
meth protected abstract int compare(int,int)
meth protected abstract void swap(int,int)
meth public abstract void sort(int,int)
supr java.lang.Object
hfds THRESHOLD

CLSS public org.apache.lucene.util.SparseFixedBitSet
cons public init(int)
intf org.apache.lucene.util.Accountable
intf org.apache.lucene.util.Bits
meth public boolean get(int)
meth public int approximateCardinality()
meth public int cardinality()
meth public int length()
meth public int nextSetBit(int)
meth public int prevSetBit(int)
meth public java.lang.String toString()
meth public long ramBytesUsed()
meth public void and(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
meth public void clear(int)
meth public void clear(int,int)
meth public void or(org.apache.lucene.search.DocIdSetIterator) throws java.io.IOException
meth public void set(int)
supr org.apache.lucene.util.BitSet
hfds BASE_RAM_BYTES_USED,MASK_4096,SINGLE_ELEMENT_ARRAY_BYTES_USED,bits,indices,length,nonZeroLongCount,ramBytesUsed

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

CLSS public abstract interface !annotation org.apache.lucene.util.SuppressForbidden
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, METHOD, TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String reason()

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
meth public static java.lang.String boost(float)
 anno 0 java.lang.Deprecated()
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
meth public static java.lang.String newString(int[],int,int)
meth public static java.lang.String toHexString(java.lang.String)
supr java.lang.Object
hfds HALF_MASK,HALF_SHIFT,LEAD_SURROGATE_MIN_VALUE,LEAD_SURROGATE_OFFSET_,LEAD_SURROGATE_SHIFT_,SUPPLEMENTARY_MIN_VALUE,SURROGATE_OFFSET,TRAIL_SURROGATE_MASK_,TRAIL_SURROGATE_MIN_VALUE,UNI_MAX_BMP,utf8CodeLength

CLSS public final org.apache.lucene.util.Version
fld public final int bugfix
fld public final int major
fld public final int minor
fld public final int prerelease
fld public final static org.apache.lucene.util.Version LATEST
fld public final static org.apache.lucene.util.Version LUCENE_4_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_0_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_0_0_ALPHA
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_0_0_BETA
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_1
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_10_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_10_1
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_10_2
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_10_3
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_10_4
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_1_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_2
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_2_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_2_1
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_3
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_3_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_3_1
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_4
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_4_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_5
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_5_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_5_1
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_6
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_6_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_6_1
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_7
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_7_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_7_1
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_7_2
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_8
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_8_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_8_1
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_9
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_9_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_4_9_1
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_5_0_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_5_1_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_5_2_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_5_2_1
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_5_3_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_5_3_1
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_5_3_2
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_5_4_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_5_4_1
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_5_5_0
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_5_5_1
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_5_5_2
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_5_5_3
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_5_5_4
 anno 0 java.lang.Deprecated()
fld public final static org.apache.lucene.util.Version LUCENE_5_5_5
fld public final static org.apache.lucene.util.Version LUCENE_CURRENT
 anno 0 java.lang.Deprecated()
meth public boolean equals(java.lang.Object)
meth public boolean onOrAfter(org.apache.lucene.util.Version)
meth public int hashCode()
meth public java.lang.String toString()
meth public static org.apache.lucene.util.Version fromBits(int,int,int)
meth public static org.apache.lucene.util.Version parse(java.lang.String) throws java.text.ParseException
meth public static org.apache.lucene.util.Version parseLeniently(java.lang.String) throws java.text.ParseException
supr java.lang.Object
hfds encodedValue

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

CLSS abstract interface org.apache.lucene.util.package-info

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
fld public final static int FREQ_NEVER = 3
fld public final static int FREQ_ONCE_DAY = 1
fld public final static int FREQ_ONCE_WEEK = 0
fld public final static int FREQ_STARTUP = 2
fld public final static java.lang.String PROP_INDEX = "createIndex"
fld public final static java.lang.String PROP_INDEX_FREQ = "indexUpdateFrequency"
fld public final static java.lang.String PROP_LAST_INDEX_UPDATE = "lastIndexUpdate"
meth public boolean isPersistent(java.lang.String)
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> remoteRepositories(org.netbeans.modules.maven.embedder.MavenEmbedder)
meth public java.util.List<org.netbeans.modules.maven.indexer.api.RepositoryInfo> getRepositoryInfos()
meth public org.netbeans.modules.maven.indexer.api.RepositoryInfo getLocalRepository()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.modules.maven.indexer.api.RepositoryInfo getRepositoryInfoById(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static boolean isIndexRepositories()
meth public static int getIndexUpdateFrequency()
meth public static java.util.Date getLastIndexUpdate(java.lang.String)
meth public static org.netbeans.modules.maven.indexer.api.RepositoryPreferences getInstance()
meth public static void setIndexRepositories(boolean)
meth public static void setIndexUpdateFrequency(int)
meth public static void setLastIndexUpdate(java.lang.String,java.util.Date)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void addOrModifyRepositoryInfo(org.netbeans.modules.maven.indexer.api.RepositoryInfo)
meth public void addTransientRepository(java.lang.Object,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.maven.indexer.api.RepositoryInfo$MirrorStrategy) throws java.net.URISyntaxException
meth public void removeChangeListener(javax.swing.event.ChangeListener)
meth public void removeRepositoryInfo(org.netbeans.modules.maven.indexer.api.RepositoryInfo)
meth public void removeTransientRepositories(java.lang.Object)
supr java.lang.Object
hfds KEY_DISPLAY_NAME,KEY_INDEX_URL,KEY_PATH,KEY_REPO_URL,LOG,central,cs,infoCache,instance,local,transients

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

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.impl.Redo<%0 extends java.lang.Object>
meth public abstract void run(org.netbeans.modules.maven.indexer.ResultImpl<{org.netbeans.modules.maven.indexer.spi.impl.Redo%0}>)

CLSS public abstract interface org.netbeans.modules.maven.indexer.spi.impl.RepositoryIndexerImplementation
meth public abstract void deleteArtifactFromIndex(org.netbeans.modules.maven.indexer.api.RepositoryInfo,org.apache.maven.artifact.Artifact)
meth public abstract void indexRepo(org.netbeans.modules.maven.indexer.api.RepositoryInfo)
meth public abstract void updateIndexWithArtifacts(org.netbeans.modules.maven.indexer.api.RepositoryInfo,java.util.Collection<org.apache.maven.artifact.Artifact>)

