#Signature file v4.1
#Version 1.24.0

CLSS public abstract interface java.io.Serializable

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

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

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

CLSS public java.util.HashSet<%0 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(int,float)
cons public init(java.util.Collection<? extends {java.util.HashSet%0}>)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.Set<{java.util.HashSet%0}>
meth public boolean add({java.util.HashSet%0})
meth public boolean contains(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public int size()
meth public java.lang.Object clone()
meth public java.util.Iterator<{java.util.HashSet%0}> iterator()
meth public java.util.Spliterator<{java.util.HashSet%0}> spliterator()
meth public void clear()
supr java.util.AbstractSet<{java.util.HashSet%0}>

CLSS public abstract interface java.util.Iterator<%0 extends java.lang.Object>
meth public abstract boolean hasNext()
meth public abstract {java.util.Iterator%0} next()
meth public void forEachRemaining(java.util.function.Consumer<? super {java.util.Iterator%0}>)
meth public void remove()

CLSS public java.util.LinkedHashMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(int,float)
cons public init(int,float,boolean)
cons public init(java.util.Map<? extends {java.util.LinkedHashMap%0},? extends {java.util.LinkedHashMap%1}>)
intf java.util.Map<{java.util.LinkedHashMap%0},{java.util.LinkedHashMap%1}>
meth protected boolean removeEldestEntry(java.util.Map$Entry<{java.util.LinkedHashMap%0},{java.util.LinkedHashMap%1}>)
meth public boolean containsValue(java.lang.Object)
meth public java.util.Collection<{java.util.LinkedHashMap%1}> values()
meth public java.util.Set<java.util.Map$Entry<{java.util.LinkedHashMap%0},{java.util.LinkedHashMap%1}>> entrySet()
meth public java.util.Set<{java.util.LinkedHashMap%0}> keySet()
meth public void clear()
meth public void forEach(java.util.function.BiConsumer<? super {java.util.LinkedHashMap%0},? super {java.util.LinkedHashMap%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.LinkedHashMap%0},? super {java.util.LinkedHashMap%1},? extends {java.util.LinkedHashMap%1}>)
meth public {java.util.LinkedHashMap%1} get(java.lang.Object)
meth public {java.util.LinkedHashMap%1} getOrDefault(java.lang.Object,{java.util.LinkedHashMap%1})
supr java.util.HashMap<{java.util.LinkedHashMap%0},{java.util.LinkedHashMap%1}>

CLSS public java.util.LinkedHashSet<%0 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(int,float)
cons public init(java.util.Collection<? extends {java.util.LinkedHashSet%0}>)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.Set<{java.util.LinkedHashSet%0}>
meth public java.util.Spliterator<{java.util.LinkedHashSet%0}> spliterator()
supr java.util.HashSet<{java.util.LinkedHashSet%0}>

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

CLSS public abstract interface java.util.Set<%0 extends java.lang.Object>
intf java.util.Collection<{java.util.Set%0}>
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

CLSS public java.util.concurrent.CancellationException
cons public init()
cons public init(java.lang.String)
supr java.lang.IllegalStateException

CLSS public abstract interface org.antlr.v4.runtime.ANTLRErrorListener
meth public abstract void reportAmbiguity(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.dfa.DFA,int,int,boolean,java.util.BitSet,org.antlr.v4.runtime.atn.ATNConfigSet)
meth public abstract void reportAttemptingFullContext(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.dfa.DFA,int,int,java.util.BitSet,org.antlr.v4.runtime.atn.ATNConfigSet)
meth public abstract void reportContextSensitivity(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.dfa.DFA,int,int,int,org.antlr.v4.runtime.atn.ATNConfigSet)
meth public abstract void syntaxError(org.antlr.v4.runtime.Recognizer<?,?>,java.lang.Object,int,int,java.lang.String,org.antlr.v4.runtime.RecognitionException)

CLSS public abstract interface org.antlr.v4.runtime.ANTLRErrorStrategy
meth public abstract boolean inErrorRecoveryMode(org.antlr.v4.runtime.Parser)
meth public abstract org.antlr.v4.runtime.Token recoverInline(org.antlr.v4.runtime.Parser)
meth public abstract void recover(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.RecognitionException)
meth public abstract void reportError(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.RecognitionException)
meth public abstract void reportMatch(org.antlr.v4.runtime.Parser)
meth public abstract void reset(org.antlr.v4.runtime.Parser)
meth public abstract void sync(org.antlr.v4.runtime.Parser)

CLSS public org.antlr.v4.runtime.ANTLRFileStream
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String) throws java.io.IOException
cons public init(java.lang.String,java.lang.String) throws java.io.IOException
fld protected java.lang.String fileName
meth public java.lang.String getSourceName()
meth public void load(java.lang.String,java.lang.String) throws java.io.IOException
supr org.antlr.v4.runtime.ANTLRInputStream

CLSS public org.antlr.v4.runtime.ANTLRInputStream
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(char[],int)
cons public init(java.io.InputStream) throws java.io.IOException
cons public init(java.io.InputStream,int) throws java.io.IOException
cons public init(java.io.InputStream,int,int) throws java.io.IOException
cons public init(java.io.Reader) throws java.io.IOException
cons public init(java.io.Reader,int) throws java.io.IOException
cons public init(java.io.Reader,int,int) throws java.io.IOException
cons public init(java.lang.String)
fld protected char[] data
fld protected int n
fld protected int p
fld public final static int INITIAL_BUFFER_SIZE = 1024
fld public final static int READ_BUFFER_SIZE = 1024
fld public java.lang.String name
intf org.antlr.v4.runtime.CharStream
meth public int LA(int)
meth public int LT(int)
meth public int index()
meth public int mark()
meth public int size()
meth public java.lang.String getSourceName()
meth public java.lang.String getText(org.antlr.v4.runtime.misc.Interval)
meth public java.lang.String toString()
meth public void consume()
meth public void load(java.io.Reader,int,int) throws java.io.IOException
meth public void release(int)
meth public void reset()
meth public void seek(int)
supr java.lang.Object

CLSS public org.antlr.v4.runtime.BailErrorStrategy
cons public init()
meth public org.antlr.v4.runtime.Token recoverInline(org.antlr.v4.runtime.Parser)
meth public void recover(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.RecognitionException)
meth public void sync(org.antlr.v4.runtime.Parser)
supr org.antlr.v4.runtime.DefaultErrorStrategy

CLSS public org.antlr.v4.runtime.BaseErrorListener
cons public init()
intf org.antlr.v4.runtime.ANTLRErrorListener
meth public void reportAmbiguity(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.dfa.DFA,int,int,boolean,java.util.BitSet,org.antlr.v4.runtime.atn.ATNConfigSet)
meth public void reportAttemptingFullContext(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.dfa.DFA,int,int,java.util.BitSet,org.antlr.v4.runtime.atn.ATNConfigSet)
meth public void reportContextSensitivity(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.dfa.DFA,int,int,int,org.antlr.v4.runtime.atn.ATNConfigSet)
meth public void syntaxError(org.antlr.v4.runtime.Recognizer<?,?>,java.lang.Object,int,int,java.lang.String,org.antlr.v4.runtime.RecognitionException)
supr java.lang.Object

CLSS public org.antlr.v4.runtime.BufferedTokenStream
cons public init(org.antlr.v4.runtime.TokenSource)
fld protected boolean fetchedEOF
fld protected int p
fld protected java.util.List<org.antlr.v4.runtime.Token> tokens
fld protected org.antlr.v4.runtime.TokenSource tokenSource
intf org.antlr.v4.runtime.TokenStream
meth protected boolean sync(int)
meth protected final void lazyInit()
meth protected int adjustSeekIndex(int)
meth protected int fetch(int)
meth protected int nextTokenOnChannel(int,int)
meth protected int previousTokenOnChannel(int,int)
meth protected java.util.List<org.antlr.v4.runtime.Token> filterForChannel(int,int,int)
meth protected org.antlr.v4.runtime.Token LB(int)
meth protected void setup()
meth public int LA(int)
meth public int index()
meth public int mark()
meth public int size()
meth public java.lang.String getSourceName()
meth public java.lang.String getText()
meth public java.lang.String getText(org.antlr.v4.runtime.RuleContext)
meth public java.lang.String getText(org.antlr.v4.runtime.Token,org.antlr.v4.runtime.Token)
meth public java.lang.String getText(org.antlr.v4.runtime.misc.Interval)
meth public java.util.List<org.antlr.v4.runtime.Token> get(int,int)
meth public java.util.List<org.antlr.v4.runtime.Token> getHiddenTokensToLeft(int)
meth public java.util.List<org.antlr.v4.runtime.Token> getHiddenTokensToLeft(int,int)
meth public java.util.List<org.antlr.v4.runtime.Token> getHiddenTokensToRight(int)
meth public java.util.List<org.antlr.v4.runtime.Token> getHiddenTokensToRight(int,int)
meth public java.util.List<org.antlr.v4.runtime.Token> getTokens()
meth public java.util.List<org.antlr.v4.runtime.Token> getTokens(int,int)
meth public java.util.List<org.antlr.v4.runtime.Token> getTokens(int,int,int)
meth public java.util.List<org.antlr.v4.runtime.Token> getTokens(int,int,java.util.Set<java.lang.Integer>)
meth public org.antlr.v4.runtime.Token LT(int)
meth public org.antlr.v4.runtime.Token get(int)
meth public org.antlr.v4.runtime.TokenSource getTokenSource()
meth public void consume()
meth public void fill()
meth public void release(int)
meth public void reset()
 anno 0 java.lang.Deprecated()
meth public void seek(int)
meth public void setTokenSource(org.antlr.v4.runtime.TokenSource)
supr java.lang.Object

CLSS public abstract interface org.antlr.v4.runtime.CharStream
intf org.antlr.v4.runtime.IntStream
meth public abstract java.lang.String getText(org.antlr.v4.runtime.misc.Interval)

CLSS public final org.antlr.v4.runtime.CharStreams
meth public static org.antlr.v4.runtime.CharStream fromChannel(java.nio.channels.ReadableByteChannel) throws java.io.IOException
meth public static org.antlr.v4.runtime.CharStream fromChannel(java.nio.channels.ReadableByteChannel,java.nio.charset.Charset) throws java.io.IOException
meth public static org.antlr.v4.runtime.CharStream fromFileName(java.lang.String) throws java.io.IOException
meth public static org.antlr.v4.runtime.CharStream fromFileName(java.lang.String,java.nio.charset.Charset) throws java.io.IOException
meth public static org.antlr.v4.runtime.CharStream fromPath(java.nio.file.Path) throws java.io.IOException
meth public static org.antlr.v4.runtime.CharStream fromPath(java.nio.file.Path,java.nio.charset.Charset) throws java.io.IOException
meth public static org.antlr.v4.runtime.CharStream fromStream(java.io.InputStream) throws java.io.IOException
meth public static org.antlr.v4.runtime.CharStream fromStream(java.io.InputStream,java.nio.charset.Charset) throws java.io.IOException
meth public static org.antlr.v4.runtime.CharStream fromStream(java.io.InputStream,java.nio.charset.Charset,long) throws java.io.IOException
meth public static org.antlr.v4.runtime.CodePointCharStream fromChannel(java.nio.channels.ReadableByteChannel,int,java.nio.charset.CodingErrorAction,java.lang.String) throws java.io.IOException
meth public static org.antlr.v4.runtime.CodePointCharStream fromChannel(java.nio.channels.ReadableByteChannel,java.nio.charset.Charset,int,java.nio.charset.CodingErrorAction,java.lang.String,long) throws java.io.IOException
meth public static org.antlr.v4.runtime.CodePointCharStream fromReader(java.io.Reader) throws java.io.IOException
meth public static org.antlr.v4.runtime.CodePointCharStream fromReader(java.io.Reader,java.lang.String) throws java.io.IOException
meth public static org.antlr.v4.runtime.CodePointCharStream fromString(java.lang.String)
meth public static org.antlr.v4.runtime.CodePointCharStream fromString(java.lang.String,java.lang.String)
supr java.lang.Object
hfds DEFAULT_BUFFER_SIZE

CLSS public org.antlr.v4.runtime.CodePointBuffer
innr public final static !enum Type
innr public static Builder
meth public int get(int)
meth public int position()
meth public int remaining()
meth public static org.antlr.v4.runtime.CodePointBuffer withBytes(java.nio.ByteBuffer)
meth public static org.antlr.v4.runtime.CodePointBuffer withChars(java.nio.CharBuffer)
meth public static org.antlr.v4.runtime.CodePointBuffer withInts(java.nio.IntBuffer)
meth public static org.antlr.v4.runtime.CodePointBuffer$Builder builder(int)
meth public void position(int)
supr java.lang.Object
hfds byteBuffer,charBuffer,intBuffer,type

CLSS public static org.antlr.v4.runtime.CodePointBuffer$Builder
 outer org.antlr.v4.runtime.CodePointBuffer
meth public org.antlr.v4.runtime.CodePointBuffer build()
meth public void append(java.nio.CharBuffer)
meth public void ensureRemaining(int)
supr java.lang.Object
hfds byteBuffer,charBuffer,intBuffer,prevHighSurrogate,type

CLSS public final static !enum org.antlr.v4.runtime.CodePointBuffer$Type
 outer org.antlr.v4.runtime.CodePointBuffer
fld public final static org.antlr.v4.runtime.CodePointBuffer$Type BYTE
fld public final static org.antlr.v4.runtime.CodePointBuffer$Type CHAR
fld public final static org.antlr.v4.runtime.CodePointBuffer$Type INT
meth public static org.antlr.v4.runtime.CodePointBuffer$Type valueOf(java.lang.String)
meth public static org.antlr.v4.runtime.CodePointBuffer$Type[] values()
supr java.lang.Enum<org.antlr.v4.runtime.CodePointBuffer$Type>

CLSS public abstract org.antlr.v4.runtime.CodePointCharStream
fld protected final int size
fld protected final java.lang.String name
fld protected int position
intf org.antlr.v4.runtime.CharStream
meth public final int index()
meth public final int mark()
meth public final int size()
meth public final java.lang.String getSourceName()
meth public final java.lang.String toString()
meth public final void consume()
meth public final void release(int)
meth public final void seek(int)
meth public static org.antlr.v4.runtime.CodePointCharStream fromBuffer(org.antlr.v4.runtime.CodePointBuffer)
meth public static org.antlr.v4.runtime.CodePointCharStream fromBuffer(org.antlr.v4.runtime.CodePointBuffer,java.lang.String)
supr java.lang.Object
hcls CodePoint16BitCharStream,CodePoint32BitCharStream,CodePoint8BitCharStream

CLSS public org.antlr.v4.runtime.CommonToken
cons public init(int)
cons public init(int,java.lang.String)
cons public init(org.antlr.v4.runtime.Token)
cons public init(org.antlr.v4.runtime.misc.Pair<org.antlr.v4.runtime.TokenSource,org.antlr.v4.runtime.CharStream>,int,int,int,int)
fld protected final static org.antlr.v4.runtime.misc.Pair<org.antlr.v4.runtime.TokenSource,org.antlr.v4.runtime.CharStream> EMPTY_SOURCE
fld protected int channel
fld protected int charPositionInLine
fld protected int index
fld protected int line
fld protected int start
fld protected int stop
fld protected int type
fld protected java.lang.String text
fld protected org.antlr.v4.runtime.misc.Pair<org.antlr.v4.runtime.TokenSource,org.antlr.v4.runtime.CharStream> source
intf java.io.Serializable
intf org.antlr.v4.runtime.WritableToken
meth public int getChannel()
meth public int getCharPositionInLine()
meth public int getLine()
meth public int getStartIndex()
meth public int getStopIndex()
meth public int getTokenIndex()
meth public int getType()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public java.lang.String toString(org.antlr.v4.runtime.Recognizer<?,?>)
meth public org.antlr.v4.runtime.CharStream getInputStream()
meth public org.antlr.v4.runtime.TokenSource getTokenSource()
meth public void setChannel(int)
meth public void setCharPositionInLine(int)
meth public void setLine(int)
meth public void setStartIndex(int)
meth public void setStopIndex(int)
meth public void setText(java.lang.String)
meth public void setTokenIndex(int)
meth public void setType(int)
supr java.lang.Object

CLSS public org.antlr.v4.runtime.CommonTokenFactory
cons public init()
cons public init(boolean)
fld protected final boolean copyText
fld public final static org.antlr.v4.runtime.TokenFactory<org.antlr.v4.runtime.CommonToken> DEFAULT
intf org.antlr.v4.runtime.TokenFactory<org.antlr.v4.runtime.CommonToken>
meth public org.antlr.v4.runtime.CommonToken create(int,java.lang.String)
meth public org.antlr.v4.runtime.CommonToken create(org.antlr.v4.runtime.misc.Pair<org.antlr.v4.runtime.TokenSource,org.antlr.v4.runtime.CharStream>,int,java.lang.String,int,int,int,int,int)
supr java.lang.Object

CLSS public org.antlr.v4.runtime.CommonTokenStream
cons public init(org.antlr.v4.runtime.TokenSource)
cons public init(org.antlr.v4.runtime.TokenSource,int)
fld protected int channel
meth protected int adjustSeekIndex(int)
meth protected org.antlr.v4.runtime.Token LB(int)
meth public int getNumberOfOnChannelTokens()
meth public org.antlr.v4.runtime.Token LT(int)
supr org.antlr.v4.runtime.BufferedTokenStream

CLSS public org.antlr.v4.runtime.ConsoleErrorListener
cons public init()
fld public final static org.antlr.v4.runtime.ConsoleErrorListener INSTANCE
meth public void syntaxError(org.antlr.v4.runtime.Recognizer<?,?>,java.lang.Object,int,int,java.lang.String,org.antlr.v4.runtime.RecognitionException)
supr org.antlr.v4.runtime.BaseErrorListener

CLSS public org.antlr.v4.runtime.DefaultErrorStrategy
cons public init()
fld protected boolean errorRecoveryMode
fld protected int lastErrorIndex
fld protected int nextTokensState
fld protected org.antlr.v4.runtime.ParserRuleContext nextTokensContext
fld protected org.antlr.v4.runtime.misc.IntervalSet lastErrorStates
intf org.antlr.v4.runtime.ANTLRErrorStrategy
meth protected boolean singleTokenInsertion(org.antlr.v4.runtime.Parser)
meth protected int getSymbolType(org.antlr.v4.runtime.Token)
meth protected java.lang.String escapeWSAndQuote(java.lang.String)
meth protected java.lang.String getSymbolText(org.antlr.v4.runtime.Token)
meth protected java.lang.String getTokenErrorDisplay(org.antlr.v4.runtime.Token)
meth protected org.antlr.v4.runtime.Token getMissingSymbol(org.antlr.v4.runtime.Parser)
meth protected org.antlr.v4.runtime.Token singleTokenDeletion(org.antlr.v4.runtime.Parser)
meth protected org.antlr.v4.runtime.misc.IntervalSet getErrorRecoverySet(org.antlr.v4.runtime.Parser)
meth protected org.antlr.v4.runtime.misc.IntervalSet getExpectedTokens(org.antlr.v4.runtime.Parser)
meth protected void beginErrorCondition(org.antlr.v4.runtime.Parser)
meth protected void consumeUntil(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.misc.IntervalSet)
meth protected void endErrorCondition(org.antlr.v4.runtime.Parser)
meth protected void reportFailedPredicate(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.FailedPredicateException)
meth protected void reportInputMismatch(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.InputMismatchException)
meth protected void reportMissingToken(org.antlr.v4.runtime.Parser)
meth protected void reportNoViableAlternative(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.NoViableAltException)
meth protected void reportUnwantedToken(org.antlr.v4.runtime.Parser)
meth public boolean inErrorRecoveryMode(org.antlr.v4.runtime.Parser)
meth public org.antlr.v4.runtime.Token recoverInline(org.antlr.v4.runtime.Parser)
meth public void recover(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.RecognitionException)
meth public void reportError(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.RecognitionException)
meth public void reportMatch(org.antlr.v4.runtime.Parser)
meth public void reset(org.antlr.v4.runtime.Parser)
meth public void sync(org.antlr.v4.runtime.Parser)
supr java.lang.Object

CLSS public org.antlr.v4.runtime.DiagnosticErrorListener
cons public init()
cons public init(boolean)
fld protected final boolean exactOnly
meth protected java.lang.String getDecisionDescription(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.dfa.DFA)
meth protected java.util.BitSet getConflictingAlts(java.util.BitSet,org.antlr.v4.runtime.atn.ATNConfigSet)
meth public void reportAmbiguity(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.dfa.DFA,int,int,boolean,java.util.BitSet,org.antlr.v4.runtime.atn.ATNConfigSet)
meth public void reportAttemptingFullContext(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.dfa.DFA,int,int,java.util.BitSet,org.antlr.v4.runtime.atn.ATNConfigSet)
meth public void reportContextSensitivity(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.dfa.DFA,int,int,int,org.antlr.v4.runtime.atn.ATNConfigSet)
supr org.antlr.v4.runtime.BaseErrorListener

CLSS public org.antlr.v4.runtime.FailedPredicateException
cons public init(org.antlr.v4.runtime.Parser)
cons public init(org.antlr.v4.runtime.Parser,java.lang.String)
cons public init(org.antlr.v4.runtime.Parser,java.lang.String,java.lang.String)
meth public int getPredIndex()
meth public int getRuleIndex()
meth public java.lang.String getPredicate()
supr org.antlr.v4.runtime.RecognitionException
hfds predicate,predicateIndex,ruleIndex

CLSS public org.antlr.v4.runtime.InputMismatchException
cons public init(org.antlr.v4.runtime.Parser)
cons public init(org.antlr.v4.runtime.Parser,int,org.antlr.v4.runtime.ParserRuleContext)
supr org.antlr.v4.runtime.RecognitionException

CLSS public abstract interface org.antlr.v4.runtime.IntStream
fld public final static int EOF = -1
fld public final static java.lang.String UNKNOWN_SOURCE_NAME = "<unknown>"
meth public abstract int LA(int)
meth public abstract int index()
meth public abstract int mark()
meth public abstract int size()
meth public abstract java.lang.String getSourceName()
meth public abstract void consume()
meth public abstract void release(int)
meth public abstract void seek(int)

CLSS public org.antlr.v4.runtime.InterpreterRuleContext
cons public init()
cons public init(org.antlr.v4.runtime.ParserRuleContext,int,int)
fld protected int ruleIndex
meth public int getRuleIndex()
supr org.antlr.v4.runtime.ParserRuleContext

CLSS public abstract org.antlr.v4.runtime.Lexer
cons public init()
cons public init(org.antlr.v4.runtime.CharStream)
fld protected org.antlr.v4.runtime.TokenFactory<?> _factory
fld protected org.antlr.v4.runtime.misc.Pair<org.antlr.v4.runtime.TokenSource,org.antlr.v4.runtime.CharStream> _tokenFactorySourcePair
fld public boolean _hitEOF
fld public final org.antlr.v4.runtime.misc.IntegerStack _modeStack
fld public final static int DEFAULT_MODE = 0
fld public final static int DEFAULT_TOKEN_CHANNEL = 0
fld public final static int HIDDEN = 1
fld public final static int MAX_CHAR_VALUE = 1114111
fld public final static int MIN_CHAR_VALUE = 0
fld public final static int MORE = -2
fld public final static int SKIP = -3
fld public int _channel
fld public int _mode
fld public int _tokenStartCharIndex
fld public int _tokenStartCharPositionInLine
fld public int _tokenStartLine
fld public int _type
fld public java.lang.String _text
fld public org.antlr.v4.runtime.CharStream _input
fld public org.antlr.v4.runtime.Token _token
intf org.antlr.v4.runtime.TokenSource
meth public int getChannel()
meth public int getCharIndex()
meth public int getCharPositionInLine()
meth public int getLine()
meth public int getType()
meth public int popMode()
meth public java.lang.String getCharErrorDisplay(int)
meth public java.lang.String getErrorDisplay(int)
meth public java.lang.String getErrorDisplay(java.lang.String)
meth public java.lang.String getSourceName()
meth public java.lang.String getText()
meth public java.lang.String[] getChannelNames()
meth public java.lang.String[] getModeNames()
meth public java.lang.String[] getTokenNames()
 anno 0 java.lang.Deprecated()
meth public java.util.List<? extends org.antlr.v4.runtime.Token> getAllTokens()
meth public org.antlr.v4.runtime.CharStream getInputStream()
meth public org.antlr.v4.runtime.Token emit()
meth public org.antlr.v4.runtime.Token emitEOF()
meth public org.antlr.v4.runtime.Token getToken()
meth public org.antlr.v4.runtime.Token nextToken()
meth public org.antlr.v4.runtime.TokenFactory<? extends org.antlr.v4.runtime.Token> getTokenFactory()
meth public void emit(org.antlr.v4.runtime.Token)
meth public void mode(int)
meth public void more()
meth public void notifyListeners(org.antlr.v4.runtime.LexerNoViableAltException)
meth public void pushMode(int)
meth public void recover(org.antlr.v4.runtime.LexerNoViableAltException)
meth public void recover(org.antlr.v4.runtime.RecognitionException)
meth public void reset()
meth public void setChannel(int)
meth public void setCharPositionInLine(int)
meth public void setInputStream(org.antlr.v4.runtime.IntStream)
meth public void setLine(int)
meth public void setText(java.lang.String)
meth public void setToken(org.antlr.v4.runtime.Token)
meth public void setTokenFactory(org.antlr.v4.runtime.TokenFactory<?>)
meth public void setType(int)
meth public void skip()
supr org.antlr.v4.runtime.Recognizer<java.lang.Integer,org.antlr.v4.runtime.atn.LexerATNSimulator>

CLSS public org.antlr.v4.runtime.LexerInterpreter
cons public init(java.lang.String,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,org.antlr.v4.runtime.atn.ATN,org.antlr.v4.runtime.CharStream)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,org.antlr.v4.runtime.Vocabulary,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,org.antlr.v4.runtime.atn.ATN,org.antlr.v4.runtime.CharStream)
cons public init(java.lang.String,org.antlr.v4.runtime.Vocabulary,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,org.antlr.v4.runtime.atn.ATN,org.antlr.v4.runtime.CharStream)
 anno 0 java.lang.Deprecated()
fld protected final java.lang.String grammarFileName
fld protected final java.lang.String[] channelNames
fld protected final java.lang.String[] modeNames
fld protected final java.lang.String[] ruleNames
fld protected final java.lang.String[] tokenNames
 anno 0 java.lang.Deprecated()
fld protected final org.antlr.v4.runtime.atn.ATN atn
fld protected final org.antlr.v4.runtime.atn.PredictionContextCache _sharedContextCache
fld protected final org.antlr.v4.runtime.dfa.DFA[] _decisionToDFA
meth public java.lang.String getGrammarFileName()
meth public java.lang.String[] getChannelNames()
meth public java.lang.String[] getModeNames()
meth public java.lang.String[] getRuleNames()
meth public java.lang.String[] getTokenNames()
 anno 0 java.lang.Deprecated()
meth public org.antlr.v4.runtime.Vocabulary getVocabulary()
meth public org.antlr.v4.runtime.atn.ATN getATN()
supr org.antlr.v4.runtime.Lexer
hfds vocabulary

CLSS public org.antlr.v4.runtime.LexerNoViableAltException
cons public init(org.antlr.v4.runtime.Lexer,org.antlr.v4.runtime.CharStream,int,org.antlr.v4.runtime.atn.ATNConfigSet)
meth public int getStartIndex()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.CharStream getInputStream()
meth public org.antlr.v4.runtime.atn.ATNConfigSet getDeadEndConfigs()
supr org.antlr.v4.runtime.RecognitionException
hfds deadEndConfigs,startIndex

CLSS public org.antlr.v4.runtime.ListTokenSource
cons public init(java.util.List<? extends org.antlr.v4.runtime.Token>)
cons public init(java.util.List<? extends org.antlr.v4.runtime.Token>,java.lang.String)
fld protected final java.util.List<? extends org.antlr.v4.runtime.Token> tokens
fld protected int i
fld protected org.antlr.v4.runtime.Token eofToken
intf org.antlr.v4.runtime.TokenSource
meth public int getCharPositionInLine()
meth public int getLine()
meth public java.lang.String getSourceName()
meth public org.antlr.v4.runtime.CharStream getInputStream()
meth public org.antlr.v4.runtime.Token nextToken()
meth public org.antlr.v4.runtime.TokenFactory<?> getTokenFactory()
meth public void setTokenFactory(org.antlr.v4.runtime.TokenFactory<?>)
supr java.lang.Object
hfds _factory,sourceName

CLSS public org.antlr.v4.runtime.NoViableAltException
cons public init(org.antlr.v4.runtime.Parser)
cons public init(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.TokenStream,org.antlr.v4.runtime.Token,org.antlr.v4.runtime.Token,org.antlr.v4.runtime.atn.ATNConfigSet,org.antlr.v4.runtime.ParserRuleContext)
meth public org.antlr.v4.runtime.Token getStartToken()
meth public org.antlr.v4.runtime.atn.ATNConfigSet getDeadEndConfigs()
supr org.antlr.v4.runtime.RecognitionException
hfds deadEndConfigs,startToken

CLSS public abstract org.antlr.v4.runtime.Parser
cons public init(org.antlr.v4.runtime.TokenStream)
fld protected boolean _buildParseTrees
fld protected boolean matchedEOF
fld protected final org.antlr.v4.runtime.misc.IntegerStack _precedenceStack
fld protected int _syntaxErrors
fld protected java.util.List<org.antlr.v4.runtime.tree.ParseTreeListener> _parseListeners
fld protected org.antlr.v4.runtime.ANTLRErrorStrategy _errHandler
fld protected org.antlr.v4.runtime.ParserRuleContext _ctx
fld protected org.antlr.v4.runtime.TokenStream _input
innr public TraceListener
innr public static TrimToSizeListener
meth protected void addContextToParseTree()
meth protected void triggerEnterRuleEvent()
meth protected void triggerExitRuleEvent()
meth public boolean getBuildParseTree()
meth public boolean getTrimParseTree()
meth public boolean inContext(java.lang.String)
meth public boolean isExpectedToken(int)
meth public boolean isMatchedEOF()
meth public boolean isTrace()
meth public boolean precpred(org.antlr.v4.runtime.RuleContext,int)
meth public final int getPrecedence()
meth public final void notifyErrorListeners(java.lang.String)
meth public final void setInputStream(org.antlr.v4.runtime.IntStream)
meth public int getNumberOfSyntaxErrors()
meth public int getRuleIndex(java.lang.String)
meth public java.lang.String getSourceName()
meth public java.util.List<java.lang.String> getDFAStrings()
meth public java.util.List<java.lang.String> getRuleInvocationStack()
meth public java.util.List<java.lang.String> getRuleInvocationStack(org.antlr.v4.runtime.RuleContext)
meth public java.util.List<org.antlr.v4.runtime.tree.ParseTreeListener> getParseListeners()
meth public org.antlr.v4.runtime.ANTLRErrorStrategy getErrorHandler()
meth public org.antlr.v4.runtime.ParserRuleContext getContext()
meth public org.antlr.v4.runtime.ParserRuleContext getInvokingContext(int)
meth public org.antlr.v4.runtime.ParserRuleContext getRuleContext()
meth public org.antlr.v4.runtime.Token consume()
meth public org.antlr.v4.runtime.Token getCurrentToken()
meth public org.antlr.v4.runtime.Token match(int)
meth public org.antlr.v4.runtime.Token matchWildcard()
meth public org.antlr.v4.runtime.TokenFactory<?> getTokenFactory()
meth public org.antlr.v4.runtime.TokenStream getInputStream()
meth public org.antlr.v4.runtime.TokenStream getTokenStream()
meth public org.antlr.v4.runtime.atn.ATN getATNWithBypassAlts()
meth public org.antlr.v4.runtime.atn.ParseInfo getParseInfo()
meth public org.antlr.v4.runtime.misc.IntervalSet getExpectedTokens()
meth public org.antlr.v4.runtime.misc.IntervalSet getExpectedTokensWithinCurrentRule()
meth public org.antlr.v4.runtime.tree.ErrorNode createErrorNode(org.antlr.v4.runtime.ParserRuleContext,org.antlr.v4.runtime.Token)
meth public org.antlr.v4.runtime.tree.TerminalNode createTerminalNode(org.antlr.v4.runtime.ParserRuleContext,org.antlr.v4.runtime.Token)
meth public org.antlr.v4.runtime.tree.pattern.ParseTreePattern compileParseTreePattern(java.lang.String,int)
meth public org.antlr.v4.runtime.tree.pattern.ParseTreePattern compileParseTreePattern(java.lang.String,int,org.antlr.v4.runtime.Lexer)
meth public void addParseListener(org.antlr.v4.runtime.tree.ParseTreeListener)
meth public void dumpDFA()
meth public void dumpDFA(java.io.PrintStream)
meth public void enterOuterAlt(org.antlr.v4.runtime.ParserRuleContext,int)
meth public void enterRecursionRule(org.antlr.v4.runtime.ParserRuleContext,int)
 anno 0 java.lang.Deprecated()
meth public void enterRecursionRule(org.antlr.v4.runtime.ParserRuleContext,int,int,int)
meth public void enterRule(org.antlr.v4.runtime.ParserRuleContext,int,int)
meth public void exitRule()
meth public void notifyErrorListeners(org.antlr.v4.runtime.Token,java.lang.String,org.antlr.v4.runtime.RecognitionException)
meth public void pushNewRecursionContext(org.antlr.v4.runtime.ParserRuleContext,int,int)
meth public void removeParseListener(org.antlr.v4.runtime.tree.ParseTreeListener)
meth public void removeParseListeners()
meth public void reset()
meth public void setBuildParseTree(boolean)
meth public void setContext(org.antlr.v4.runtime.ParserRuleContext)
meth public void setErrorHandler(org.antlr.v4.runtime.ANTLRErrorStrategy)
meth public void setProfile(boolean)
meth public void setTokenFactory(org.antlr.v4.runtime.TokenFactory<?>)
meth public void setTokenStream(org.antlr.v4.runtime.TokenStream)
meth public void setTrace(boolean)
meth public void setTrimParseTree(boolean)
meth public void unrollRecursionContexts(org.antlr.v4.runtime.ParserRuleContext)
supr org.antlr.v4.runtime.Recognizer<org.antlr.v4.runtime.Token,org.antlr.v4.runtime.atn.ParserATNSimulator>
hfds _tracer,bypassAltsAtnCache

CLSS public org.antlr.v4.runtime.Parser$TraceListener
 outer org.antlr.v4.runtime.Parser
cons public init(org.antlr.v4.runtime.Parser)
intf org.antlr.v4.runtime.tree.ParseTreeListener
meth public void enterEveryRule(org.antlr.v4.runtime.ParserRuleContext)
meth public void exitEveryRule(org.antlr.v4.runtime.ParserRuleContext)
meth public void visitErrorNode(org.antlr.v4.runtime.tree.ErrorNode)
meth public void visitTerminal(org.antlr.v4.runtime.tree.TerminalNode)
supr java.lang.Object

CLSS public static org.antlr.v4.runtime.Parser$TrimToSizeListener
 outer org.antlr.v4.runtime.Parser
cons public init()
fld public final static org.antlr.v4.runtime.Parser$TrimToSizeListener INSTANCE
intf org.antlr.v4.runtime.tree.ParseTreeListener
meth public void enterEveryRule(org.antlr.v4.runtime.ParserRuleContext)
meth public void exitEveryRule(org.antlr.v4.runtime.ParserRuleContext)
meth public void visitErrorNode(org.antlr.v4.runtime.tree.ErrorNode)
meth public void visitTerminal(org.antlr.v4.runtime.tree.TerminalNode)
supr java.lang.Object

CLSS public org.antlr.v4.runtime.ParserInterpreter
cons public init(java.lang.String,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,org.antlr.v4.runtime.atn.ATN,org.antlr.v4.runtime.TokenStream)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,org.antlr.v4.runtime.Vocabulary,java.util.Collection<java.lang.String>,org.antlr.v4.runtime.atn.ATN,org.antlr.v4.runtime.TokenStream)
fld protected boolean overrideDecisionReached
fld protected final java.lang.String grammarFileName
fld protected final java.lang.String[] ruleNames
fld protected final java.lang.String[] tokenNames
 anno 0 java.lang.Deprecated()
fld protected final java.util.Deque<org.antlr.v4.runtime.misc.Pair<org.antlr.v4.runtime.ParserRuleContext,java.lang.Integer>> _parentContextStack
fld protected final org.antlr.v4.runtime.atn.ATN atn
fld protected final org.antlr.v4.runtime.atn.PredictionContextCache sharedContextCache
fld protected final org.antlr.v4.runtime.dfa.DFA[] decisionToDFA
fld protected int overrideDecision
fld protected int overrideDecisionAlt
fld protected int overrideDecisionInputIndex
fld protected org.antlr.v4.runtime.InterpreterRuleContext overrideDecisionRoot
fld protected org.antlr.v4.runtime.InterpreterRuleContext rootContext
meth protected int visitDecisionState(org.antlr.v4.runtime.atn.DecisionState)
meth protected org.antlr.v4.runtime.InterpreterRuleContext createInterpreterRuleContext(org.antlr.v4.runtime.ParserRuleContext,int,int)
meth protected org.antlr.v4.runtime.Token recoverInline()
meth protected org.antlr.v4.runtime.atn.ATNState getATNState()
meth protected void recover(org.antlr.v4.runtime.RecognitionException)
meth protected void visitRuleStopState(org.antlr.v4.runtime.atn.ATNState)
meth protected void visitState(org.antlr.v4.runtime.atn.ATNState)
meth public java.lang.String getGrammarFileName()
meth public java.lang.String[] getRuleNames()
meth public java.lang.String[] getTokenNames()
 anno 0 java.lang.Deprecated()
meth public org.antlr.v4.runtime.InterpreterRuleContext getOverrideDecisionRoot()
meth public org.antlr.v4.runtime.InterpreterRuleContext getRootContext()
meth public org.antlr.v4.runtime.ParserRuleContext parse(int)
meth public org.antlr.v4.runtime.Vocabulary getVocabulary()
meth public org.antlr.v4.runtime.atn.ATN getATN()
meth public void addDecisionOverride(int,int,int)
meth public void enterRecursionRule(org.antlr.v4.runtime.ParserRuleContext,int,int,int)
meth public void reset()
supr org.antlr.v4.runtime.Parser
hfds vocabulary

CLSS public org.antlr.v4.runtime.ParserRuleContext
cons public init()
cons public init(org.antlr.v4.runtime.ParserRuleContext,int)
fld public final static org.antlr.v4.runtime.ParserRuleContext EMPTY
fld public java.util.List<org.antlr.v4.runtime.tree.ParseTree> children
fld public org.antlr.v4.runtime.RecognitionException exception
fld public org.antlr.v4.runtime.Token start
fld public org.antlr.v4.runtime.Token stop
meth public <%0 extends org.antlr.v4.runtime.ParserRuleContext> java.util.List<{%%0}> getRuleContexts(java.lang.Class<? extends {%%0}>)
meth public <%0 extends org.antlr.v4.runtime.ParserRuleContext> {%%0} getRuleContext(java.lang.Class<? extends {%%0}>,int)
meth public <%0 extends org.antlr.v4.runtime.tree.ParseTree> {%%0} addAnyChild({%%0})
meth public <%0 extends org.antlr.v4.runtime.tree.ParseTree> {%%0} getChild(java.lang.Class<? extends {%%0}>,int)
meth public int getChildCount()
meth public java.lang.String toInfoString(org.antlr.v4.runtime.Parser)
meth public java.util.List<org.antlr.v4.runtime.tree.TerminalNode> getTokens(int)
meth public org.antlr.v4.runtime.ParserRuleContext getParent()
meth public org.antlr.v4.runtime.RuleContext addChild(org.antlr.v4.runtime.RuleContext)
meth public org.antlr.v4.runtime.Token getStart()
meth public org.antlr.v4.runtime.Token getStop()
meth public org.antlr.v4.runtime.misc.Interval getSourceInterval()
meth public org.antlr.v4.runtime.tree.ErrorNode addErrorNode(org.antlr.v4.runtime.Token)
 anno 0 java.lang.Deprecated()
meth public org.antlr.v4.runtime.tree.ErrorNode addErrorNode(org.antlr.v4.runtime.tree.ErrorNode)
meth public org.antlr.v4.runtime.tree.ParseTree getChild(int)
meth public org.antlr.v4.runtime.tree.TerminalNode addChild(org.antlr.v4.runtime.Token)
 anno 0 java.lang.Deprecated()
meth public org.antlr.v4.runtime.tree.TerminalNode addChild(org.antlr.v4.runtime.tree.TerminalNode)
meth public org.antlr.v4.runtime.tree.TerminalNode getToken(int,int)
meth public void copyFrom(org.antlr.v4.runtime.ParserRuleContext)
meth public void enterRule(org.antlr.v4.runtime.tree.ParseTreeListener)
meth public void exitRule(org.antlr.v4.runtime.tree.ParseTreeListener)
meth public void removeLastChild()
supr org.antlr.v4.runtime.RuleContext

CLSS public org.antlr.v4.runtime.ProxyErrorListener
cons public init(java.util.Collection<? extends org.antlr.v4.runtime.ANTLRErrorListener>)
intf org.antlr.v4.runtime.ANTLRErrorListener
meth public void reportAmbiguity(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.dfa.DFA,int,int,boolean,java.util.BitSet,org.antlr.v4.runtime.atn.ATNConfigSet)
meth public void reportAttemptingFullContext(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.dfa.DFA,int,int,java.util.BitSet,org.antlr.v4.runtime.atn.ATNConfigSet)
meth public void reportContextSensitivity(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.dfa.DFA,int,int,int,org.antlr.v4.runtime.atn.ATNConfigSet)
meth public void syntaxError(org.antlr.v4.runtime.Recognizer<?,?>,java.lang.Object,int,int,java.lang.String,org.antlr.v4.runtime.RecognitionException)
supr java.lang.Object
hfds delegates

CLSS public org.antlr.v4.runtime.RecognitionException
cons public init(java.lang.String,org.antlr.v4.runtime.Recognizer<?,?>,org.antlr.v4.runtime.IntStream,org.antlr.v4.runtime.ParserRuleContext)
cons public init(org.antlr.v4.runtime.Recognizer<?,?>,org.antlr.v4.runtime.IntStream,org.antlr.v4.runtime.ParserRuleContext)
meth protected final void setOffendingState(int)
meth protected final void setOffendingToken(org.antlr.v4.runtime.Token)
meth public int getOffendingState()
meth public org.antlr.v4.runtime.IntStream getInputStream()
meth public org.antlr.v4.runtime.Recognizer<?,?> getRecognizer()
meth public org.antlr.v4.runtime.RuleContext getCtx()
meth public org.antlr.v4.runtime.Token getOffendingToken()
meth public org.antlr.v4.runtime.misc.IntervalSet getExpectedTokens()
supr java.lang.RuntimeException
hfds ctx,input,offendingState,offendingToken,recognizer

CLSS public abstract org.antlr.v4.runtime.Recognizer<%0 extends java.lang.Object, %1 extends org.antlr.v4.runtime.atn.ATNSimulator>
cons public init()
fld protected {org.antlr.v4.runtime.Recognizer%1} _interp
fld public final static int EOF = -1
meth public abstract java.lang.String getGrammarFileName()
meth public abstract java.lang.String[] getRuleNames()
meth public abstract java.lang.String[] getTokenNames()
 anno 0 java.lang.Deprecated()
meth public abstract org.antlr.v4.runtime.IntStream getInputStream()
meth public abstract org.antlr.v4.runtime.TokenFactory<?> getTokenFactory()
meth public abstract org.antlr.v4.runtime.atn.ATN getATN()
meth public abstract void setInputStream(org.antlr.v4.runtime.IntStream)
meth public abstract void setTokenFactory(org.antlr.v4.runtime.TokenFactory<?>)
meth public boolean precpred(org.antlr.v4.runtime.RuleContext,int)
meth public boolean sempred(org.antlr.v4.runtime.RuleContext,int,int)
meth public final int getState()
meth public final void setState(int)
meth public int getTokenType(java.lang.String)
meth public java.lang.String getErrorHeader(org.antlr.v4.runtime.RecognitionException)
meth public java.lang.String getSerializedATN()
meth public java.lang.String getTokenErrorDisplay(org.antlr.v4.runtime.Token)
 anno 0 java.lang.Deprecated()
meth public java.util.List<? extends org.antlr.v4.runtime.ANTLRErrorListener> getErrorListeners()
meth public java.util.Map<java.lang.String,java.lang.Integer> getRuleIndexMap()
meth public java.util.Map<java.lang.String,java.lang.Integer> getTokenTypeMap()
meth public org.antlr.v4.runtime.ANTLRErrorListener getErrorListenerDispatch()
meth public org.antlr.v4.runtime.Vocabulary getVocabulary()
meth public org.antlr.v4.runtime.atn.ParseInfo getParseInfo()
meth public void action(org.antlr.v4.runtime.RuleContext,int,int)
meth public void addErrorListener(org.antlr.v4.runtime.ANTLRErrorListener)
meth public void removeErrorListener(org.antlr.v4.runtime.ANTLRErrorListener)
meth public void removeErrorListeners()
meth public void setInterpreter({org.antlr.v4.runtime.Recognizer%1})
meth public {org.antlr.v4.runtime.Recognizer%1} getInterpreter()
supr java.lang.Object
hfds _listeners,_stateNumber,ruleIndexMapCache,tokenTypeMapCache

CLSS public org.antlr.v4.runtime.RuleContext
cons public init()
cons public init(org.antlr.v4.runtime.RuleContext,int)
fld public int invokingState
fld public org.antlr.v4.runtime.RuleContext parent
intf org.antlr.v4.runtime.tree.RuleNode
meth public <%0 extends java.lang.Object> {%%0} accept(org.antlr.v4.runtime.tree.ParseTreeVisitor<? extends {%%0}>)
meth public boolean isEmpty()
meth public final java.lang.String toString(java.util.List<java.lang.String>)
meth public final java.lang.String toString(org.antlr.v4.runtime.Recognizer<?,?>)
meth public int depth()
meth public int getAltNumber()
meth public int getChildCount()
meth public int getRuleIndex()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public java.lang.String toString(java.util.List<java.lang.String>,org.antlr.v4.runtime.RuleContext)
meth public java.lang.String toString(org.antlr.v4.runtime.Recognizer<?,?>,org.antlr.v4.runtime.RuleContext)
meth public java.lang.String toStringTree()
meth public java.lang.String toStringTree(java.util.List<java.lang.String>)
meth public java.lang.String toStringTree(org.antlr.v4.runtime.Parser)
meth public org.antlr.v4.runtime.RuleContext getParent()
meth public org.antlr.v4.runtime.RuleContext getPayload()
meth public org.antlr.v4.runtime.RuleContext getRuleContext()
meth public org.antlr.v4.runtime.misc.Interval getSourceInterval()
meth public org.antlr.v4.runtime.tree.ParseTree getChild(int)
meth public void setAltNumber(int)
meth public void setParent(org.antlr.v4.runtime.RuleContext)
supr java.lang.Object

CLSS public org.antlr.v4.runtime.RuleContextWithAltNum
cons public init()
cons public init(org.antlr.v4.runtime.ParserRuleContext,int)
fld public int altNum
meth public int getAltNumber()
meth public void setAltNumber(int)
supr org.antlr.v4.runtime.ParserRuleContext

CLSS public org.antlr.v4.runtime.RuntimeMetaData
cons public init()
fld public final static java.lang.String VERSION = "4.11.1"
meth public static java.lang.String getMajorMinorVersion(java.lang.String)
meth public static java.lang.String getRuntimeVersion()
meth public static void checkVersion(java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.antlr.v4.runtime.Token
fld public final static int DEFAULT_CHANNEL = 0
fld public final static int EOF = -1
fld public final static int EPSILON = -2
fld public final static int HIDDEN_CHANNEL = 1
fld public final static int INVALID_TYPE = 0
fld public final static int MIN_USER_CHANNEL_VALUE = 2
fld public final static int MIN_USER_TOKEN_TYPE = 1
meth public abstract int getChannel()
meth public abstract int getCharPositionInLine()
meth public abstract int getLine()
meth public abstract int getStartIndex()
meth public abstract int getStopIndex()
meth public abstract int getTokenIndex()
meth public abstract int getType()
meth public abstract java.lang.String getText()
meth public abstract org.antlr.v4.runtime.CharStream getInputStream()
meth public abstract org.antlr.v4.runtime.TokenSource getTokenSource()

CLSS public abstract interface org.antlr.v4.runtime.TokenFactory<%0 extends org.antlr.v4.runtime.Token>
meth public abstract {org.antlr.v4.runtime.TokenFactory%0} create(int,java.lang.String)
meth public abstract {org.antlr.v4.runtime.TokenFactory%0} create(org.antlr.v4.runtime.misc.Pair<org.antlr.v4.runtime.TokenSource,org.antlr.v4.runtime.CharStream>,int,java.lang.String,int,int,int,int,int)

CLSS public abstract interface org.antlr.v4.runtime.TokenSource
meth public abstract int getCharPositionInLine()
meth public abstract int getLine()
meth public abstract java.lang.String getSourceName()
meth public abstract org.antlr.v4.runtime.CharStream getInputStream()
meth public abstract org.antlr.v4.runtime.Token nextToken()
meth public abstract org.antlr.v4.runtime.TokenFactory<?> getTokenFactory()
meth public abstract void setTokenFactory(org.antlr.v4.runtime.TokenFactory<?>)

CLSS public abstract interface org.antlr.v4.runtime.TokenStream
intf org.antlr.v4.runtime.IntStream
meth public abstract java.lang.String getText()
meth public abstract java.lang.String getText(org.antlr.v4.runtime.RuleContext)
meth public abstract java.lang.String getText(org.antlr.v4.runtime.Token,org.antlr.v4.runtime.Token)
meth public abstract java.lang.String getText(org.antlr.v4.runtime.misc.Interval)
meth public abstract org.antlr.v4.runtime.Token LT(int)
meth public abstract org.antlr.v4.runtime.Token get(int)
meth public abstract org.antlr.v4.runtime.TokenSource getTokenSource()

CLSS public org.antlr.v4.runtime.TokenStreamRewriter
cons public init(org.antlr.v4.runtime.TokenStream)
fld protected final java.util.Map<java.lang.String,java.lang.Integer> lastRewriteTokenIndexes
fld protected final java.util.Map<java.lang.String,java.util.List<org.antlr.v4.runtime.TokenStreamRewriter$RewriteOperation>> programs
fld protected final org.antlr.v4.runtime.TokenStream tokens
fld public final static int MIN_TOKEN_INDEX = 0
fld public final static int PROGRAM_INIT_SIZE = 100
fld public final static java.lang.String DEFAULT_PROGRAM_NAME = "default"
innr public RewriteOperation
meth protected <%0 extends org.antlr.v4.runtime.TokenStreamRewriter$RewriteOperation> java.util.List<? extends {%%0}> getKindOfOps(java.util.List<? extends org.antlr.v4.runtime.TokenStreamRewriter$RewriteOperation>,java.lang.Class<{%%0}>,int)
meth protected int getLastRewriteTokenIndex(java.lang.String)
meth protected java.lang.String catOpText(java.lang.Object,java.lang.Object)
meth protected java.util.List<org.antlr.v4.runtime.TokenStreamRewriter$RewriteOperation> getProgram(java.lang.String)
meth protected java.util.Map<java.lang.Integer,org.antlr.v4.runtime.TokenStreamRewriter$RewriteOperation> reduceToSingleOperationPerIndex(java.util.List<org.antlr.v4.runtime.TokenStreamRewriter$RewriteOperation>)
meth protected void setLastRewriteTokenIndex(java.lang.String,int)
meth public final org.antlr.v4.runtime.TokenStream getTokenStream()
meth public int getLastRewriteTokenIndex()
meth public java.lang.String getText()
meth public java.lang.String getText(java.lang.String)
meth public java.lang.String getText(java.lang.String,org.antlr.v4.runtime.misc.Interval)
meth public java.lang.String getText(org.antlr.v4.runtime.misc.Interval)
meth public void delete(int)
meth public void delete(int,int)
meth public void delete(java.lang.String,int,int)
meth public void delete(java.lang.String,org.antlr.v4.runtime.Token,org.antlr.v4.runtime.Token)
meth public void delete(org.antlr.v4.runtime.Token)
meth public void delete(org.antlr.v4.runtime.Token,org.antlr.v4.runtime.Token)
meth public void deleteProgram()
meth public void deleteProgram(java.lang.String)
meth public void insertAfter(int,java.lang.Object)
meth public void insertAfter(java.lang.String,int,java.lang.Object)
meth public void insertAfter(java.lang.String,org.antlr.v4.runtime.Token,java.lang.Object)
meth public void insertAfter(org.antlr.v4.runtime.Token,java.lang.Object)
meth public void insertBefore(int,java.lang.Object)
meth public void insertBefore(java.lang.String,int,java.lang.Object)
meth public void insertBefore(java.lang.String,org.antlr.v4.runtime.Token,java.lang.Object)
meth public void insertBefore(org.antlr.v4.runtime.Token,java.lang.Object)
meth public void replace(int,int,java.lang.Object)
meth public void replace(int,java.lang.Object)
meth public void replace(java.lang.String,int,int,java.lang.Object)
meth public void replace(java.lang.String,org.antlr.v4.runtime.Token,org.antlr.v4.runtime.Token,java.lang.Object)
meth public void replace(org.antlr.v4.runtime.Token,java.lang.Object)
meth public void replace(org.antlr.v4.runtime.Token,org.antlr.v4.runtime.Token,java.lang.Object)
meth public void rollback(int)
meth public void rollback(java.lang.String,int)
supr java.lang.Object
hcls InsertAfterOp,InsertBeforeOp,ReplaceOp

CLSS public org.antlr.v4.runtime.TokenStreamRewriter$RewriteOperation
 outer org.antlr.v4.runtime.TokenStreamRewriter
cons protected init(org.antlr.v4.runtime.TokenStreamRewriter,int)
cons protected init(org.antlr.v4.runtime.TokenStreamRewriter,int,java.lang.Object)
fld protected int index
fld protected int instructionIndex
fld protected java.lang.Object text
meth public int execute(java.lang.StringBuilder)
meth public java.lang.String toString()
supr java.lang.Object

CLSS public org.antlr.v4.runtime.UnbufferedCharStream
cons public init()
cons public init(int)
cons public init(java.io.InputStream)
cons public init(java.io.InputStream,int)
cons public init(java.io.InputStream,int,java.nio.charset.Charset)
cons public init(java.io.Reader)
cons public init(java.io.Reader,int)
fld protected int currentCharIndex
fld protected int lastChar
fld protected int lastCharBufferStart
fld protected int n
fld protected int numMarkers
fld protected int p
fld protected int[] data
fld protected java.io.Reader input
fld public java.lang.String name
intf org.antlr.v4.runtime.CharStream
meth protected final int getBufferStartIndex()
meth protected int fill(int)
meth protected int nextChar() throws java.io.IOException
meth protected void add(int)
meth protected void sync(int)
meth public int LA(int)
meth public int index()
meth public int mark()
meth public int size()
meth public java.lang.String getSourceName()
meth public java.lang.String getText(org.antlr.v4.runtime.misc.Interval)
meth public void consume()
meth public void release(int)
meth public void seek(int)
supr java.lang.Object

CLSS public org.antlr.v4.runtime.UnbufferedTokenStream<%0 extends org.antlr.v4.runtime.Token>
cons public init(org.antlr.v4.runtime.TokenSource)
cons public init(org.antlr.v4.runtime.TokenSource,int)
fld protected int currentTokenIndex
fld protected int n
fld protected int numMarkers
fld protected int p
fld protected org.antlr.v4.runtime.Token lastToken
fld protected org.antlr.v4.runtime.Token lastTokenBufferStart
fld protected org.antlr.v4.runtime.TokenSource tokenSource
fld protected org.antlr.v4.runtime.Token[] tokens
intf org.antlr.v4.runtime.TokenStream
meth protected final int getBufferStartIndex()
meth protected int fill(int)
meth protected void add(org.antlr.v4.runtime.Token)
meth protected void sync(int)
meth public int LA(int)
meth public int index()
meth public int mark()
meth public int size()
meth public java.lang.String getSourceName()
meth public java.lang.String getText()
meth public java.lang.String getText(org.antlr.v4.runtime.RuleContext)
meth public java.lang.String getText(org.antlr.v4.runtime.Token,org.antlr.v4.runtime.Token)
meth public java.lang.String getText(org.antlr.v4.runtime.misc.Interval)
meth public org.antlr.v4.runtime.Token LT(int)
meth public org.antlr.v4.runtime.Token get(int)
meth public org.antlr.v4.runtime.TokenSource getTokenSource()
meth public void consume()
meth public void release(int)
meth public void seek(int)
supr java.lang.Object

CLSS public abstract interface org.antlr.v4.runtime.Vocabulary
meth public abstract int getMaxTokenType()
meth public abstract java.lang.String getDisplayName(int)
meth public abstract java.lang.String getLiteralName(int)
meth public abstract java.lang.String getSymbolicName(int)

CLSS public org.antlr.v4.runtime.VocabularyImpl
cons public init(java.lang.String[],java.lang.String[])
cons public init(java.lang.String[],java.lang.String[],java.lang.String[])
fld public final static org.antlr.v4.runtime.VocabularyImpl EMPTY_VOCABULARY
intf org.antlr.v4.runtime.Vocabulary
meth public int getMaxTokenType()
meth public java.lang.String getDisplayName(int)
meth public java.lang.String getLiteralName(int)
meth public java.lang.String getSymbolicName(int)
meth public java.lang.String[] getDisplayNames()
meth public java.lang.String[] getLiteralNames()
meth public java.lang.String[] getSymbolicNames()
meth public static org.antlr.v4.runtime.Vocabulary fromTokenNames(java.lang.String[])
supr java.lang.Object
hfds EMPTY_NAMES,displayNames,literalNames,maxTokenType,symbolicNames

CLSS public abstract interface org.antlr.v4.runtime.WritableToken
intf org.antlr.v4.runtime.Token
meth public abstract void setChannel(int)
meth public abstract void setCharPositionInLine(int)
meth public abstract void setLine(int)
meth public abstract void setText(java.lang.String)
meth public abstract void setTokenIndex(int)
meth public abstract void setType(int)

CLSS public org.antlr.v4.runtime.atn.ATN
cons public init(org.antlr.v4.runtime.atn.ATNType,int)
fld public final int maxTokenType
fld public final java.util.List<org.antlr.v4.runtime.atn.ATNState> states
fld public final java.util.List<org.antlr.v4.runtime.atn.DecisionState> decisionToState
fld public final java.util.List<org.antlr.v4.runtime.atn.TokensStartState> modeToStartState
fld public final java.util.Map<java.lang.String,org.antlr.v4.runtime.atn.TokensStartState> modeNameToStartState
fld public final org.antlr.v4.runtime.atn.ATNType grammarType
fld public final static int INVALID_ALT_NUMBER = 0
fld public int[] ruleToTokenType
fld public org.antlr.v4.runtime.atn.LexerAction[] lexerActions
fld public org.antlr.v4.runtime.atn.RuleStartState[] ruleToStartState
fld public org.antlr.v4.runtime.atn.RuleStopState[] ruleToStopState
meth public int defineDecisionState(org.antlr.v4.runtime.atn.DecisionState)
meth public int getNumberOfDecisions()
meth public org.antlr.v4.runtime.atn.DecisionState getDecisionState(int)
meth public org.antlr.v4.runtime.misc.IntervalSet getExpectedTokens(int,org.antlr.v4.runtime.RuleContext)
meth public org.antlr.v4.runtime.misc.IntervalSet nextTokens(org.antlr.v4.runtime.atn.ATNState)
meth public org.antlr.v4.runtime.misc.IntervalSet nextTokens(org.antlr.v4.runtime.atn.ATNState,org.antlr.v4.runtime.RuleContext)
meth public void addState(org.antlr.v4.runtime.atn.ATNState)
meth public void removeState(org.antlr.v4.runtime.atn.ATNState)
supr java.lang.Object

CLSS public org.antlr.v4.runtime.atn.ATNConfig
cons public init(org.antlr.v4.runtime.atn.ATNConfig)
cons public init(org.antlr.v4.runtime.atn.ATNConfig,org.antlr.v4.runtime.atn.ATNState)
cons public init(org.antlr.v4.runtime.atn.ATNConfig,org.antlr.v4.runtime.atn.ATNState,org.antlr.v4.runtime.atn.PredictionContext)
cons public init(org.antlr.v4.runtime.atn.ATNConfig,org.antlr.v4.runtime.atn.ATNState,org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.SemanticContext)
cons public init(org.antlr.v4.runtime.atn.ATNConfig,org.antlr.v4.runtime.atn.ATNState,org.antlr.v4.runtime.atn.SemanticContext)
cons public init(org.antlr.v4.runtime.atn.ATNConfig,org.antlr.v4.runtime.atn.SemanticContext)
cons public init(org.antlr.v4.runtime.atn.ATNState,int,org.antlr.v4.runtime.atn.PredictionContext)
cons public init(org.antlr.v4.runtime.atn.ATNState,int,org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.SemanticContext)
fld public final int alt
fld public final org.antlr.v4.runtime.atn.ATNState state
fld public final org.antlr.v4.runtime.atn.SemanticContext semanticContext
fld public int reachesIntoOuterContext
fld public org.antlr.v4.runtime.atn.PredictionContext context
meth public boolean equals(java.lang.Object)
meth public boolean equals(org.antlr.v4.runtime.atn.ATNConfig)
meth public final boolean isPrecedenceFilterSuppressed()
meth public final int getOuterContextDepth()
meth public final void setPrecedenceFilterSuppressed(boolean)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.lang.String toString(org.antlr.v4.runtime.Recognizer<?,?>,boolean)
supr java.lang.Object
hfds SUPPRESS_PRECEDENCE_FILTER

CLSS public org.antlr.v4.runtime.atn.ATNConfigSet
cons public init()
cons public init(boolean)
cons public init(org.antlr.v4.runtime.atn.ATNConfigSet)
fld protected boolean readonly
fld protected java.util.BitSet conflictingAlts
fld public boolean dipsIntoOuterContext
fld public boolean hasSemanticContext
fld public final boolean fullCtx
fld public final java.util.ArrayList<org.antlr.v4.runtime.atn.ATNConfig> configs
fld public int uniqueAlt
fld public org.antlr.v4.runtime.atn.ATNConfigSet$AbstractConfigHashSet configLookup
innr public abstract static AbstractConfigHashSet
innr public final static ConfigEqualityComparator
innr public static ConfigHashSet
intf java.util.Set<org.antlr.v4.runtime.atn.ATNConfig>
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public boolean add(org.antlr.v4.runtime.atn.ATNConfig)
meth public boolean add(org.antlr.v4.runtime.atn.ATNConfig,org.antlr.v4.runtime.misc.DoubleKeyMap<org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.PredictionContext>)
meth public boolean addAll(java.util.Collection<? extends org.antlr.v4.runtime.atn.ATNConfig>)
meth public boolean contains(java.lang.Object)
meth public boolean containsAll(java.util.Collection<?>)
meth public boolean containsFast(org.antlr.v4.runtime.atn.ATNConfig)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public boolean isReadonly()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean retainAll(java.util.Collection<?>)
meth public int hashCode()
meth public int size()
meth public java.lang.String toString()
meth public java.util.BitSet getAlts()
meth public java.util.Iterator<org.antlr.v4.runtime.atn.ATNConfig> iterator()
meth public java.util.List<org.antlr.v4.runtime.atn.ATNConfig> elements()
meth public java.util.List<org.antlr.v4.runtime.atn.SemanticContext> getPredicates()
meth public java.util.Set<org.antlr.v4.runtime.atn.ATNState> getStates()
meth public org.antlr.v4.runtime.atn.ATNConfig get(int)
meth public org.antlr.v4.runtime.atn.ATNConfig[] toArray()
meth public void clear()
meth public void optimizeConfigs(org.antlr.v4.runtime.atn.ATNSimulator)
meth public void setReadonly(boolean)
supr java.lang.Object
hfds cachedHashCode

CLSS public abstract static org.antlr.v4.runtime.atn.ATNConfigSet$AbstractConfigHashSet
 outer org.antlr.v4.runtime.atn.ATNConfigSet
cons public init(org.antlr.v4.runtime.misc.AbstractEqualityComparator<? super org.antlr.v4.runtime.atn.ATNConfig>)
cons public init(org.antlr.v4.runtime.misc.AbstractEqualityComparator<? super org.antlr.v4.runtime.atn.ATNConfig>,int,int)
meth protected final org.antlr.v4.runtime.atn.ATNConfig asElementType(java.lang.Object)
meth protected final org.antlr.v4.runtime.atn.ATNConfig[] createBucket(int)
meth protected final org.antlr.v4.runtime.atn.ATNConfig[][] createBuckets(int)
supr org.antlr.v4.runtime.misc.Array2DHashSet<org.antlr.v4.runtime.atn.ATNConfig>

CLSS public final static org.antlr.v4.runtime.atn.ATNConfigSet$ConfigEqualityComparator
 outer org.antlr.v4.runtime.atn.ATNConfigSet
fld public final static org.antlr.v4.runtime.atn.ATNConfigSet$ConfigEqualityComparator INSTANCE
meth public boolean equals(org.antlr.v4.runtime.atn.ATNConfig,org.antlr.v4.runtime.atn.ATNConfig)
meth public int hashCode(org.antlr.v4.runtime.atn.ATNConfig)
supr org.antlr.v4.runtime.misc.AbstractEqualityComparator<org.antlr.v4.runtime.atn.ATNConfig>

CLSS public static org.antlr.v4.runtime.atn.ATNConfigSet$ConfigHashSet
 outer org.antlr.v4.runtime.atn.ATNConfigSet
cons public init()
supr org.antlr.v4.runtime.atn.ATNConfigSet$AbstractConfigHashSet

CLSS public org.antlr.v4.runtime.atn.ATNDeserializationOptions
cons public init()
cons public init(org.antlr.v4.runtime.atn.ATNDeserializationOptions)
meth protected void throwIfReadOnly()
meth public final boolean isGenerateRuleBypassTransitions()
meth public final boolean isReadOnly()
meth public final boolean isVerifyATN()
meth public final void makeReadOnly()
meth public final void setGenerateRuleBypassTransitions(boolean)
meth public final void setVerifyATN(boolean)
meth public static org.antlr.v4.runtime.atn.ATNDeserializationOptions getDefaultOptions()
supr java.lang.Object
hfds defaultOptions,generateRuleBypassTransitions,readOnly,verifyATN

CLSS public org.antlr.v4.runtime.atn.ATNDeserializer
cons public init()
cons public init(org.antlr.v4.runtime.atn.ATNDeserializationOptions)
fld public final static int SERIALIZED_VERSION
meth protected org.antlr.v4.runtime.atn.ATNState stateFactory(int,int)
meth protected org.antlr.v4.runtime.atn.LexerAction lexerActionFactory(org.antlr.v4.runtime.atn.LexerActionType,int,int)
meth protected org.antlr.v4.runtime.atn.Transition edgeFactory(org.antlr.v4.runtime.atn.ATN,int,int,int,int,int,int,java.util.List<org.antlr.v4.runtime.misc.IntervalSet>)
meth protected static int toInt(char)
meth protected static int toInt32(char[],int)
meth protected static int toInt32(int[],int)
meth protected void checkCondition(boolean)
meth protected void checkCondition(boolean,java.lang.String)
meth protected void markPrecedenceDecisions(org.antlr.v4.runtime.atn.ATN)
meth protected void verifyATN(org.antlr.v4.runtime.atn.ATN)
meth public org.antlr.v4.runtime.atn.ATN deserialize(char[])
meth public org.antlr.v4.runtime.atn.ATN deserialize(int[])
meth public static int[] decodeIntsEncodedAs16BitWords(char[])
meth public static int[] decodeIntsEncodedAs16BitWords(char[],boolean)
meth public static org.antlr.v4.runtime.misc.IntegerList encodeIntsWith16BitWords(org.antlr.v4.runtime.misc.IntegerList)
supr java.lang.Object
hfds deserializationOptions

CLSS public org.antlr.v4.runtime.atn.ATNSerializer
cons public init(org.antlr.v4.runtime.atn.ATN)
fld public org.antlr.v4.runtime.atn.ATN atn
meth public org.antlr.v4.runtime.misc.IntegerList serialize()
meth public static org.antlr.v4.runtime.misc.IntegerList getSerialized(org.antlr.v4.runtime.atn.ATN)
supr java.lang.Object
hfds data,nonGreedyStates,precedenceStates,sets

CLSS public abstract org.antlr.v4.runtime.atn.ATNSimulator
cons public init(org.antlr.v4.runtime.atn.ATN,org.antlr.v4.runtime.atn.PredictionContextCache)
fld protected final org.antlr.v4.runtime.atn.PredictionContextCache sharedContextCache
fld public final org.antlr.v4.runtime.atn.ATN atn
fld public final static org.antlr.v4.runtime.dfa.DFAState ERROR
meth public abstract void reset()
meth public org.antlr.v4.runtime.atn.PredictionContext getCachedContext(org.antlr.v4.runtime.atn.PredictionContext)
meth public org.antlr.v4.runtime.atn.PredictionContextCache getSharedContextCache()
meth public void clearDFA()
supr java.lang.Object

CLSS public abstract org.antlr.v4.runtime.atn.ATNState
cons public init()
fld protected final java.util.List<org.antlr.v4.runtime.atn.Transition> transitions
fld public boolean epsilonOnlyTransitions
fld public final static int BASIC = 1
fld public final static int BLOCK_END = 8
fld public final static int BLOCK_START = 3
fld public final static int INITIAL_NUM_TRANSITIONS = 4
fld public final static int INVALID_STATE_NUMBER = -1
fld public final static int INVALID_TYPE = 0
fld public final static int LOOP_END = 12
fld public final static int PLUS_BLOCK_START = 4
fld public final static int PLUS_LOOP_BACK = 11
fld public final static int RULE_START = 2
fld public final static int RULE_STOP = 7
fld public final static int STAR_BLOCK_START = 5
fld public final static int STAR_LOOP_BACK = 9
fld public final static int STAR_LOOP_ENTRY = 10
fld public final static int TOKEN_START = 6
fld public final static java.util.List<java.lang.String> serializationNames
fld public int ruleIndex
fld public int stateNumber
fld public org.antlr.v4.runtime.atn.ATN atn
fld public org.antlr.v4.runtime.misc.IntervalSet nextTokenWithinRule
meth public abstract int getStateType()
meth public boolean equals(java.lang.Object)
meth public boolean isNonGreedyExitState()
meth public final boolean onlyHasEpsilonTransitions()
meth public int getNumberOfTransitions()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.atn.Transition removeTransition(int)
meth public org.antlr.v4.runtime.atn.Transition transition(int)
meth public org.antlr.v4.runtime.atn.Transition[] getTransitions()
meth public void addTransition(int,org.antlr.v4.runtime.atn.Transition)
meth public void addTransition(org.antlr.v4.runtime.atn.Transition)
meth public void setRuleIndex(int)
meth public void setTransition(int,org.antlr.v4.runtime.atn.Transition)
supr java.lang.Object

CLSS public final !enum org.antlr.v4.runtime.atn.ATNType
fld public final static org.antlr.v4.runtime.atn.ATNType LEXER
fld public final static org.antlr.v4.runtime.atn.ATNType PARSER
meth public static org.antlr.v4.runtime.atn.ATNType valueOf(java.lang.String)
meth public static org.antlr.v4.runtime.atn.ATNType[] values()
supr java.lang.Enum<org.antlr.v4.runtime.atn.ATNType>

CLSS public abstract org.antlr.v4.runtime.atn.AbstractPredicateTransition
cons public init(org.antlr.v4.runtime.atn.ATNState)
supr org.antlr.v4.runtime.atn.Transition

CLSS public final org.antlr.v4.runtime.atn.ActionTransition
cons public init(org.antlr.v4.runtime.atn.ATNState,int)
cons public init(org.antlr.v4.runtime.atn.ATNState,int,int,boolean)
fld public final boolean isCtxDependent
fld public final int actionIndex
fld public final int ruleIndex
meth public boolean isEpsilon()
meth public boolean matches(int,int,int)
meth public int getSerializationType()
meth public java.lang.String toString()
supr org.antlr.v4.runtime.atn.Transition

CLSS public org.antlr.v4.runtime.atn.AmbiguityInfo
cons public init(int,org.antlr.v4.runtime.atn.ATNConfigSet,java.util.BitSet,org.antlr.v4.runtime.TokenStream,int,int,boolean)
fld public java.util.BitSet ambigAlts
supr org.antlr.v4.runtime.atn.DecisionEventInfo

CLSS public org.antlr.v4.runtime.atn.ArrayPredictionContext
cons public init(org.antlr.v4.runtime.atn.PredictionContext[],int[])
cons public init(org.antlr.v4.runtime.atn.SingletonPredictionContext)
fld public final int[] returnStates
fld public final org.antlr.v4.runtime.atn.PredictionContext[] parents
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int getReturnState(int)
meth public int size()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.atn.PredictionContext getParent(int)
supr org.antlr.v4.runtime.atn.PredictionContext

CLSS public final org.antlr.v4.runtime.atn.AtomTransition
cons public init(org.antlr.v4.runtime.atn.ATNState,int)
fld public final int label
meth public boolean matches(int,int,int)
meth public int getSerializationType()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.misc.IntervalSet label()
supr org.antlr.v4.runtime.atn.Transition

CLSS public final org.antlr.v4.runtime.atn.BasicBlockStartState
cons public init()
meth public int getStateType()
supr org.antlr.v4.runtime.atn.BlockStartState

CLSS public final org.antlr.v4.runtime.atn.BasicState
cons public init()
meth public int getStateType()
supr org.antlr.v4.runtime.atn.ATNState

CLSS public final org.antlr.v4.runtime.atn.BlockEndState
cons public init()
fld public org.antlr.v4.runtime.atn.BlockStartState startState
meth public int getStateType()
supr org.antlr.v4.runtime.atn.ATNState

CLSS public abstract org.antlr.v4.runtime.atn.BlockStartState
cons public init()
fld public org.antlr.v4.runtime.atn.BlockEndState endState
supr org.antlr.v4.runtime.atn.DecisionState

CLSS public abstract org.antlr.v4.runtime.atn.CodePointTransitions
cons public init()
meth public static org.antlr.v4.runtime.atn.Transition createWithCodePoint(org.antlr.v4.runtime.atn.ATNState,int)
meth public static org.antlr.v4.runtime.atn.Transition createWithCodePointRange(org.antlr.v4.runtime.atn.ATNState,int,int)
supr java.lang.Object

CLSS public org.antlr.v4.runtime.atn.ContextSensitivityInfo
cons public init(int,org.antlr.v4.runtime.atn.ATNConfigSet,org.antlr.v4.runtime.TokenStream,int,int)
supr org.antlr.v4.runtime.atn.DecisionEventInfo

CLSS public org.antlr.v4.runtime.atn.DecisionEventInfo
cons public init(int,org.antlr.v4.runtime.atn.ATNConfigSet,org.antlr.v4.runtime.TokenStream,int,int,boolean)
fld public final boolean fullCtx
fld public final int decision
fld public final int startIndex
fld public final int stopIndex
fld public final org.antlr.v4.runtime.TokenStream input
fld public final org.antlr.v4.runtime.atn.ATNConfigSet configs
supr java.lang.Object

CLSS public org.antlr.v4.runtime.atn.DecisionInfo
cons public init(int)
fld public final int decision
fld public final java.util.List<org.antlr.v4.runtime.atn.AmbiguityInfo> ambiguities
fld public final java.util.List<org.antlr.v4.runtime.atn.ContextSensitivityInfo> contextSensitivities
fld public final java.util.List<org.antlr.v4.runtime.atn.ErrorInfo> errors
fld public final java.util.List<org.antlr.v4.runtime.atn.PredicateEvalInfo> predicateEvals
fld public long LL_ATNTransitions
fld public long LL_DFATransitions
fld public long LL_Fallback
fld public long LL_MaxLook
fld public long LL_MinLook
fld public long LL_TotalLook
fld public long SLL_ATNTransitions
fld public long SLL_DFATransitions
fld public long SLL_MaxLook
fld public long SLL_MinLook
fld public long SLL_TotalLook
fld public long invocations
fld public long timeInPrediction
fld public org.antlr.v4.runtime.atn.LookaheadEventInfo LL_MaxLookEvent
fld public org.antlr.v4.runtime.atn.LookaheadEventInfo SLL_MaxLookEvent
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract org.antlr.v4.runtime.atn.DecisionState
cons public init()
fld public boolean nonGreedy
fld public int decision
supr org.antlr.v4.runtime.atn.ATNState

CLSS public org.antlr.v4.runtime.atn.EmptyPredictionContext
fld public final static org.antlr.v4.runtime.atn.EmptyPredictionContext Instance
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int getReturnState(int)
meth public int size()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.atn.PredictionContext getParent(int)
supr org.antlr.v4.runtime.atn.SingletonPredictionContext

CLSS public final org.antlr.v4.runtime.atn.EpsilonTransition
cons public init(org.antlr.v4.runtime.atn.ATNState)
cons public init(org.antlr.v4.runtime.atn.ATNState,int)
meth public boolean isEpsilon()
meth public boolean matches(int,int,int)
meth public int getSerializationType()
meth public int outermostPrecedenceReturn()
meth public java.lang.String toString()
supr org.antlr.v4.runtime.atn.Transition
hfds outermostPrecedenceReturn

CLSS public org.antlr.v4.runtime.atn.ErrorInfo
cons public init(int,org.antlr.v4.runtime.atn.ATNConfigSet,org.antlr.v4.runtime.TokenStream,int,int,boolean)
supr org.antlr.v4.runtime.atn.DecisionEventInfo

CLSS public org.antlr.v4.runtime.atn.LL1Analyzer
cons public init(org.antlr.v4.runtime.atn.ATN)
fld public final org.antlr.v4.runtime.atn.ATN atn
fld public final static int HIT_PRED = 0
meth protected void _LOOK(org.antlr.v4.runtime.atn.ATNState,org.antlr.v4.runtime.atn.ATNState,org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.misc.IntervalSet,java.util.Set<org.antlr.v4.runtime.atn.ATNConfig>,java.util.BitSet,boolean,boolean)
meth public org.antlr.v4.runtime.misc.IntervalSet LOOK(org.antlr.v4.runtime.atn.ATNState,org.antlr.v4.runtime.RuleContext)
meth public org.antlr.v4.runtime.misc.IntervalSet LOOK(org.antlr.v4.runtime.atn.ATNState,org.antlr.v4.runtime.atn.ATNState,org.antlr.v4.runtime.RuleContext)
meth public org.antlr.v4.runtime.misc.IntervalSet[] getDecisionLookahead(org.antlr.v4.runtime.atn.ATNState)
supr java.lang.Object

CLSS public org.antlr.v4.runtime.atn.LexerATNConfig
cons public init(org.antlr.v4.runtime.atn.ATNState,int,org.antlr.v4.runtime.atn.PredictionContext)
cons public init(org.antlr.v4.runtime.atn.ATNState,int,org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.LexerActionExecutor)
cons public init(org.antlr.v4.runtime.atn.LexerATNConfig,org.antlr.v4.runtime.atn.ATNState)
cons public init(org.antlr.v4.runtime.atn.LexerATNConfig,org.antlr.v4.runtime.atn.ATNState,org.antlr.v4.runtime.atn.LexerActionExecutor)
cons public init(org.antlr.v4.runtime.atn.LexerATNConfig,org.antlr.v4.runtime.atn.ATNState,org.antlr.v4.runtime.atn.PredictionContext)
meth public boolean equals(org.antlr.v4.runtime.atn.ATNConfig)
meth public final boolean hasPassedThroughNonGreedyDecision()
meth public final org.antlr.v4.runtime.atn.LexerActionExecutor getLexerActionExecutor()
meth public int hashCode()
supr org.antlr.v4.runtime.atn.ATNConfig
hfds lexerActionExecutor,passedThroughNonGreedyDecision

CLSS public org.antlr.v4.runtime.atn.LexerATNSimulator
cons public init(org.antlr.v4.runtime.Lexer,org.antlr.v4.runtime.atn.ATN,org.antlr.v4.runtime.dfa.DFA[],org.antlr.v4.runtime.atn.PredictionContextCache)
cons public init(org.antlr.v4.runtime.atn.ATN,org.antlr.v4.runtime.dfa.DFA[],org.antlr.v4.runtime.atn.PredictionContextCache)
fld protected final org.antlr.v4.runtime.Lexer recog
fld protected final org.antlr.v4.runtime.atn.LexerATNSimulator$SimState prevAccept
fld protected int charPositionInLine
fld protected int line
fld protected int mode
fld protected int startIndex
fld public final org.antlr.v4.runtime.dfa.DFA[] decisionToDFA
fld public final static boolean debug = false
fld public final static boolean dfa_debug = false
fld public final static int MAX_DFA_EDGE = 127
fld public final static int MIN_DFA_EDGE = 0
innr protected static SimState
meth protected boolean closure(org.antlr.v4.runtime.CharStream,org.antlr.v4.runtime.atn.LexerATNConfig,org.antlr.v4.runtime.atn.ATNConfigSet,boolean,boolean,boolean)
meth protected boolean evaluatePredicate(org.antlr.v4.runtime.CharStream,int,int,boolean)
meth protected int execATN(org.antlr.v4.runtime.CharStream,org.antlr.v4.runtime.dfa.DFAState)
meth protected int failOrAccept(org.antlr.v4.runtime.atn.LexerATNSimulator$SimState,org.antlr.v4.runtime.CharStream,org.antlr.v4.runtime.atn.ATNConfigSet,int)
meth protected int matchATN(org.antlr.v4.runtime.CharStream)
meth protected org.antlr.v4.runtime.atn.ATNConfigSet computeStartState(org.antlr.v4.runtime.CharStream,org.antlr.v4.runtime.atn.ATNState)
meth protected org.antlr.v4.runtime.atn.ATNState getReachableTarget(org.antlr.v4.runtime.atn.Transition,int)
meth protected org.antlr.v4.runtime.atn.LexerATNConfig getEpsilonTarget(org.antlr.v4.runtime.CharStream,org.antlr.v4.runtime.atn.LexerATNConfig,org.antlr.v4.runtime.atn.Transition,org.antlr.v4.runtime.atn.ATNConfigSet,boolean,boolean)
meth protected org.antlr.v4.runtime.dfa.DFAState addDFAEdge(org.antlr.v4.runtime.dfa.DFAState,int,org.antlr.v4.runtime.atn.ATNConfigSet)
meth protected org.antlr.v4.runtime.dfa.DFAState addDFAState(org.antlr.v4.runtime.atn.ATNConfigSet)
meth protected org.antlr.v4.runtime.dfa.DFAState computeTargetState(org.antlr.v4.runtime.CharStream,org.antlr.v4.runtime.dfa.DFAState,int)
meth protected org.antlr.v4.runtime.dfa.DFAState getExistingTargetState(org.antlr.v4.runtime.dfa.DFAState,int)
meth protected void accept(org.antlr.v4.runtime.CharStream,org.antlr.v4.runtime.atn.LexerActionExecutor,int,int,int,int)
meth protected void addDFAEdge(org.antlr.v4.runtime.dfa.DFAState,int,org.antlr.v4.runtime.dfa.DFAState)
meth protected void captureSimState(org.antlr.v4.runtime.atn.LexerATNSimulator$SimState,org.antlr.v4.runtime.CharStream,org.antlr.v4.runtime.dfa.DFAState)
meth protected void getReachableConfigSet(org.antlr.v4.runtime.CharStream,org.antlr.v4.runtime.atn.ATNConfigSet,org.antlr.v4.runtime.atn.ATNConfigSet,int)
meth public final org.antlr.v4.runtime.dfa.DFA getDFA(int)
meth public int getCharPositionInLine()
meth public int getLine()
meth public int match(org.antlr.v4.runtime.CharStream,int)
meth public java.lang.String getText(org.antlr.v4.runtime.CharStream)
meth public java.lang.String getTokenName(int)
meth public void clearDFA()
meth public void consume(org.antlr.v4.runtime.CharStream)
meth public void copyState(org.antlr.v4.runtime.atn.LexerATNSimulator)
meth public void reset()
meth public void setCharPositionInLine(int)
meth public void setLine(int)
supr org.antlr.v4.runtime.atn.ATNSimulator

CLSS protected static org.antlr.v4.runtime.atn.LexerATNSimulator$SimState
 outer org.antlr.v4.runtime.atn.LexerATNSimulator
cons protected init()
fld protected int charPos
fld protected int index
fld protected int line
fld protected org.antlr.v4.runtime.dfa.DFAState dfaState
meth protected void reset()
supr java.lang.Object

CLSS public abstract interface org.antlr.v4.runtime.atn.LexerAction
meth public abstract boolean isPositionDependent()
meth public abstract org.antlr.v4.runtime.atn.LexerActionType getActionType()
meth public abstract void execute(org.antlr.v4.runtime.Lexer)

CLSS public org.antlr.v4.runtime.atn.LexerActionExecutor
cons public init(org.antlr.v4.runtime.atn.LexerAction[])
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public org.antlr.v4.runtime.atn.LexerActionExecutor fixOffsetBeforeMatch(int)
meth public org.antlr.v4.runtime.atn.LexerAction[] getLexerActions()
meth public static org.antlr.v4.runtime.atn.LexerActionExecutor append(org.antlr.v4.runtime.atn.LexerActionExecutor,org.antlr.v4.runtime.atn.LexerAction)
meth public void execute(org.antlr.v4.runtime.Lexer,org.antlr.v4.runtime.CharStream,int)
supr java.lang.Object
hfds hashCode,lexerActions

CLSS public final !enum org.antlr.v4.runtime.atn.LexerActionType
fld public final static org.antlr.v4.runtime.atn.LexerActionType CHANNEL
fld public final static org.antlr.v4.runtime.atn.LexerActionType CUSTOM
fld public final static org.antlr.v4.runtime.atn.LexerActionType MODE
fld public final static org.antlr.v4.runtime.atn.LexerActionType MORE
fld public final static org.antlr.v4.runtime.atn.LexerActionType POP_MODE
fld public final static org.antlr.v4.runtime.atn.LexerActionType PUSH_MODE
fld public final static org.antlr.v4.runtime.atn.LexerActionType SKIP
fld public final static org.antlr.v4.runtime.atn.LexerActionType TYPE
meth public static org.antlr.v4.runtime.atn.LexerActionType valueOf(java.lang.String)
meth public static org.antlr.v4.runtime.atn.LexerActionType[] values()
supr java.lang.Enum<org.antlr.v4.runtime.atn.LexerActionType>

CLSS public final org.antlr.v4.runtime.atn.LexerChannelAction
cons public init(int)
intf org.antlr.v4.runtime.atn.LexerAction
meth public boolean equals(java.lang.Object)
meth public boolean isPositionDependent()
meth public int getChannel()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.atn.LexerActionType getActionType()
meth public void execute(org.antlr.v4.runtime.Lexer)
supr java.lang.Object
hfds channel

CLSS public final org.antlr.v4.runtime.atn.LexerCustomAction
cons public init(int,int)
intf org.antlr.v4.runtime.atn.LexerAction
meth public boolean equals(java.lang.Object)
meth public boolean isPositionDependent()
meth public int getActionIndex()
meth public int getRuleIndex()
meth public int hashCode()
meth public org.antlr.v4.runtime.atn.LexerActionType getActionType()
meth public void execute(org.antlr.v4.runtime.Lexer)
supr java.lang.Object
hfds actionIndex,ruleIndex

CLSS public final org.antlr.v4.runtime.atn.LexerIndexedCustomAction
cons public init(int,org.antlr.v4.runtime.atn.LexerAction)
intf org.antlr.v4.runtime.atn.LexerAction
meth public boolean equals(java.lang.Object)
meth public boolean isPositionDependent()
meth public int getOffset()
meth public int hashCode()
meth public org.antlr.v4.runtime.atn.LexerAction getAction()
meth public org.antlr.v4.runtime.atn.LexerActionType getActionType()
meth public void execute(org.antlr.v4.runtime.Lexer)
supr java.lang.Object
hfds action,offset

CLSS public final org.antlr.v4.runtime.atn.LexerModeAction
cons public init(int)
intf org.antlr.v4.runtime.atn.LexerAction
meth public boolean equals(java.lang.Object)
meth public boolean isPositionDependent()
meth public int getMode()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.atn.LexerActionType getActionType()
meth public void execute(org.antlr.v4.runtime.Lexer)
supr java.lang.Object
hfds mode

CLSS public final org.antlr.v4.runtime.atn.LexerMoreAction
fld public final static org.antlr.v4.runtime.atn.LexerMoreAction INSTANCE
intf org.antlr.v4.runtime.atn.LexerAction
meth public boolean equals(java.lang.Object)
meth public boolean isPositionDependent()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.atn.LexerActionType getActionType()
meth public void execute(org.antlr.v4.runtime.Lexer)
supr java.lang.Object

CLSS public final org.antlr.v4.runtime.atn.LexerPopModeAction
fld public final static org.antlr.v4.runtime.atn.LexerPopModeAction INSTANCE
intf org.antlr.v4.runtime.atn.LexerAction
meth public boolean equals(java.lang.Object)
meth public boolean isPositionDependent()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.atn.LexerActionType getActionType()
meth public void execute(org.antlr.v4.runtime.Lexer)
supr java.lang.Object

CLSS public final org.antlr.v4.runtime.atn.LexerPushModeAction
cons public init(int)
intf org.antlr.v4.runtime.atn.LexerAction
meth public boolean equals(java.lang.Object)
meth public boolean isPositionDependent()
meth public int getMode()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.atn.LexerActionType getActionType()
meth public void execute(org.antlr.v4.runtime.Lexer)
supr java.lang.Object
hfds mode

CLSS public final org.antlr.v4.runtime.atn.LexerSkipAction
fld public final static org.antlr.v4.runtime.atn.LexerSkipAction INSTANCE
intf org.antlr.v4.runtime.atn.LexerAction
meth public boolean equals(java.lang.Object)
meth public boolean isPositionDependent()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.atn.LexerActionType getActionType()
meth public void execute(org.antlr.v4.runtime.Lexer)
supr java.lang.Object

CLSS public org.antlr.v4.runtime.atn.LexerTypeAction
cons public init(int)
intf org.antlr.v4.runtime.atn.LexerAction
meth public boolean equals(java.lang.Object)
meth public boolean isPositionDependent()
meth public int getType()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.atn.LexerActionType getActionType()
meth public void execute(org.antlr.v4.runtime.Lexer)
supr java.lang.Object
hfds type

CLSS public org.antlr.v4.runtime.atn.LookaheadEventInfo
cons public init(int,org.antlr.v4.runtime.atn.ATNConfigSet,int,org.antlr.v4.runtime.TokenStream,int,int,boolean)
fld public int predictedAlt
supr org.antlr.v4.runtime.atn.DecisionEventInfo

CLSS public final org.antlr.v4.runtime.atn.LoopEndState
cons public init()
fld public org.antlr.v4.runtime.atn.ATNState loopBackState
meth public int getStateType()
supr org.antlr.v4.runtime.atn.ATNState

CLSS public final org.antlr.v4.runtime.atn.NotSetTransition
cons public init(org.antlr.v4.runtime.atn.ATNState,org.antlr.v4.runtime.misc.IntervalSet)
meth public boolean matches(int,int,int)
meth public int getSerializationType()
meth public java.lang.String toString()
supr org.antlr.v4.runtime.atn.SetTransition

CLSS public org.antlr.v4.runtime.atn.OrderedATNConfigSet
cons public init()
innr public static LexerConfigHashSet
supr org.antlr.v4.runtime.atn.ATNConfigSet

CLSS public static org.antlr.v4.runtime.atn.OrderedATNConfigSet$LexerConfigHashSet
 outer org.antlr.v4.runtime.atn.OrderedATNConfigSet
cons public init()
supr org.antlr.v4.runtime.atn.ATNConfigSet$AbstractConfigHashSet

CLSS public org.antlr.v4.runtime.atn.ParseInfo
cons public init(org.antlr.v4.runtime.atn.ProfilingATNSimulator)
fld protected final org.antlr.v4.runtime.atn.ProfilingATNSimulator atnSimulator
meth public int getDFASize()
meth public int getDFASize(int)
meth public java.util.List<java.lang.Integer> getLLDecisions()
meth public long getTotalATNLookaheadOps()
meth public long getTotalLLATNLookaheadOps()
meth public long getTotalLLLookaheadOps()
meth public long getTotalSLLATNLookaheadOps()
meth public long getTotalSLLLookaheadOps()
meth public long getTotalTimeInPrediction()
meth public org.antlr.v4.runtime.atn.DecisionInfo[] getDecisionInfo()
supr java.lang.Object

CLSS public org.antlr.v4.runtime.atn.ParserATNSimulator
cons public init(org.antlr.v4.runtime.Parser,org.antlr.v4.runtime.atn.ATN,org.antlr.v4.runtime.dfa.DFA[],org.antlr.v4.runtime.atn.PredictionContextCache)
cons public init(org.antlr.v4.runtime.atn.ATN,org.antlr.v4.runtime.dfa.DFA[],org.antlr.v4.runtime.atn.PredictionContextCache)
fld protected final org.antlr.v4.runtime.Parser parser
fld protected int _startIndex
fld protected org.antlr.v4.runtime.ParserRuleContext _outerContext
fld protected org.antlr.v4.runtime.TokenStream _input
fld protected org.antlr.v4.runtime.dfa.DFA _dfa
fld protected org.antlr.v4.runtime.misc.DoubleKeyMap<org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.PredictionContext> mergeCache
fld public final org.antlr.v4.runtime.dfa.DFA[] decisionToDFA
fld public final static boolean TURN_OFF_LR_LOOP_ENTRY_BRANCH_OPT
fld public final static boolean debug = false
fld public final static boolean debug_list_atn_decisions = false
fld public final static boolean dfa_debug = false
fld public final static boolean retry_debug = false
meth protected boolean canDropLoopEntryEdgeInLeftRecursiveRule(org.antlr.v4.runtime.atn.ATNConfig)
meth protected boolean evalSemanticContext(org.antlr.v4.runtime.atn.SemanticContext,org.antlr.v4.runtime.ParserRuleContext,int,boolean)
meth protected int execATN(org.antlr.v4.runtime.dfa.DFA,org.antlr.v4.runtime.dfa.DFAState,org.antlr.v4.runtime.TokenStream,int,org.antlr.v4.runtime.ParserRuleContext)
meth protected int execATNWithFullContext(org.antlr.v4.runtime.dfa.DFA,org.antlr.v4.runtime.dfa.DFAState,org.antlr.v4.runtime.atn.ATNConfigSet,org.antlr.v4.runtime.TokenStream,int,org.antlr.v4.runtime.ParserRuleContext)
meth protected int getAltThatFinishedDecisionEntryRule(org.antlr.v4.runtime.atn.ATNConfigSet)
meth protected int getSynValidOrSemInvalidAltThatFinishedDecisionEntryRule(org.antlr.v4.runtime.atn.ATNConfigSet,org.antlr.v4.runtime.ParserRuleContext)
meth protected java.util.BitSet evalSemanticContext(org.antlr.v4.runtime.dfa.DFAState$PredPrediction[],org.antlr.v4.runtime.ParserRuleContext,boolean)
meth protected java.util.BitSet getConflictingAlts(org.antlr.v4.runtime.atn.ATNConfigSet)
meth protected java.util.BitSet getConflictingAltsOrUniqueAlt(org.antlr.v4.runtime.atn.ATNConfigSet)
meth protected org.antlr.v4.runtime.NoViableAltException noViableAlt(org.antlr.v4.runtime.TokenStream,org.antlr.v4.runtime.ParserRuleContext,org.antlr.v4.runtime.atn.ATNConfigSet,int)
meth protected org.antlr.v4.runtime.atn.ATNConfig actionTransition(org.antlr.v4.runtime.atn.ATNConfig,org.antlr.v4.runtime.atn.ActionTransition)
meth protected org.antlr.v4.runtime.atn.ATNConfig getEpsilonTarget(org.antlr.v4.runtime.atn.ATNConfig,org.antlr.v4.runtime.atn.Transition,boolean,boolean,boolean,boolean)
meth protected org.antlr.v4.runtime.atn.ATNConfig predTransition(org.antlr.v4.runtime.atn.ATNConfig,org.antlr.v4.runtime.atn.PredicateTransition,boolean,boolean,boolean)
meth protected org.antlr.v4.runtime.atn.ATNConfig ruleTransition(org.antlr.v4.runtime.atn.ATNConfig,org.antlr.v4.runtime.atn.RuleTransition)
meth protected org.antlr.v4.runtime.atn.ATNConfigSet applyPrecedenceFilter(org.antlr.v4.runtime.atn.ATNConfigSet)
meth protected org.antlr.v4.runtime.atn.ATNConfigSet computeReachSet(org.antlr.v4.runtime.atn.ATNConfigSet,int,boolean)
meth protected org.antlr.v4.runtime.atn.ATNConfigSet computeStartState(org.antlr.v4.runtime.atn.ATNState,org.antlr.v4.runtime.RuleContext,boolean)
meth protected org.antlr.v4.runtime.atn.ATNConfigSet removeAllConfigsNotInRuleStopState(org.antlr.v4.runtime.atn.ATNConfigSet,boolean)
meth protected org.antlr.v4.runtime.atn.ATNState getReachableTarget(org.antlr.v4.runtime.atn.Transition,int)
meth protected org.antlr.v4.runtime.atn.SemanticContext[] getPredsForAmbigAlts(java.util.BitSet,org.antlr.v4.runtime.atn.ATNConfigSet,int)
meth protected org.antlr.v4.runtime.dfa.DFAState addDFAEdge(org.antlr.v4.runtime.dfa.DFA,org.antlr.v4.runtime.dfa.DFAState,int,org.antlr.v4.runtime.dfa.DFAState)
meth protected org.antlr.v4.runtime.dfa.DFAState addDFAState(org.antlr.v4.runtime.dfa.DFA,org.antlr.v4.runtime.dfa.DFAState)
meth protected org.antlr.v4.runtime.dfa.DFAState computeTargetState(org.antlr.v4.runtime.dfa.DFA,org.antlr.v4.runtime.dfa.DFAState,int)
meth protected org.antlr.v4.runtime.dfa.DFAState getExistingTargetState(org.antlr.v4.runtime.dfa.DFAState,int)
meth protected org.antlr.v4.runtime.dfa.DFAState$PredPrediction[] getPredicatePredictions(java.util.BitSet,org.antlr.v4.runtime.atn.SemanticContext[])
meth protected org.antlr.v4.runtime.misc.Pair<org.antlr.v4.runtime.atn.ATNConfigSet,org.antlr.v4.runtime.atn.ATNConfigSet> splitAccordingToSemanticValidity(org.antlr.v4.runtime.atn.ATNConfigSet,org.antlr.v4.runtime.ParserRuleContext)
meth protected static int getUniqueAlt(org.antlr.v4.runtime.atn.ATNConfigSet)
meth protected void closure(org.antlr.v4.runtime.atn.ATNConfig,org.antlr.v4.runtime.atn.ATNConfigSet,java.util.Set<org.antlr.v4.runtime.atn.ATNConfig>,boolean,boolean,boolean)
meth protected void closureCheckingStopState(org.antlr.v4.runtime.atn.ATNConfig,org.antlr.v4.runtime.atn.ATNConfigSet,java.util.Set<org.antlr.v4.runtime.atn.ATNConfig>,boolean,boolean,int,boolean)
meth protected void closure_(org.antlr.v4.runtime.atn.ATNConfig,org.antlr.v4.runtime.atn.ATNConfigSet,java.util.Set<org.antlr.v4.runtime.atn.ATNConfig>,boolean,boolean,int,boolean)
meth protected void predicateDFAState(org.antlr.v4.runtime.dfa.DFAState,org.antlr.v4.runtime.atn.DecisionState)
meth protected void reportAmbiguity(org.antlr.v4.runtime.dfa.DFA,org.antlr.v4.runtime.dfa.DFAState,int,int,boolean,java.util.BitSet,org.antlr.v4.runtime.atn.ATNConfigSet)
meth protected void reportAttemptingFullContext(org.antlr.v4.runtime.dfa.DFA,java.util.BitSet,org.antlr.v4.runtime.atn.ATNConfigSet,int,int)
meth protected void reportContextSensitivity(org.antlr.v4.runtime.dfa.DFA,int,org.antlr.v4.runtime.atn.ATNConfigSet,int,int)
meth public final org.antlr.v4.runtime.atn.PredictionMode getPredictionMode()
meth public final void setPredictionMode(org.antlr.v4.runtime.atn.PredictionMode)
meth public int adaptivePredict(org.antlr.v4.runtime.TokenStream,int,org.antlr.v4.runtime.ParserRuleContext)
meth public java.lang.String getLookaheadName(org.antlr.v4.runtime.TokenStream)
meth public java.lang.String getRuleName(int)
meth public java.lang.String getTokenName(int)
meth public org.antlr.v4.runtime.Parser getParser()
meth public org.antlr.v4.runtime.atn.ATNConfig precedenceTransition(org.antlr.v4.runtime.atn.ATNConfig,org.antlr.v4.runtime.atn.PrecedencePredicateTransition,boolean,boolean,boolean)
meth public static java.lang.String getSafeEnv(java.lang.String)
meth public void clearDFA()
meth public void dumpDeadEndConfigs(org.antlr.v4.runtime.NoViableAltException)
meth public void reset()
supr org.antlr.v4.runtime.atn.ATNSimulator
hfds mode

CLSS public final org.antlr.v4.runtime.atn.PlusBlockStartState
cons public init()
fld public org.antlr.v4.runtime.atn.PlusLoopbackState loopBackState
meth public int getStateType()
supr org.antlr.v4.runtime.atn.BlockStartState

CLSS public final org.antlr.v4.runtime.atn.PlusLoopbackState
cons public init()
meth public int getStateType()
supr org.antlr.v4.runtime.atn.DecisionState

CLSS public final org.antlr.v4.runtime.atn.PrecedencePredicateTransition
cons public init(org.antlr.v4.runtime.atn.ATNState,int)
fld public final int precedence
meth public boolean isEpsilon()
meth public boolean matches(int,int,int)
meth public int getSerializationType()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.atn.SemanticContext$PrecedencePredicate getPredicate()
supr org.antlr.v4.runtime.atn.AbstractPredicateTransition

CLSS public org.antlr.v4.runtime.atn.PredicateEvalInfo
cons public init(int,org.antlr.v4.runtime.TokenStream,int,int,org.antlr.v4.runtime.atn.SemanticContext,boolean,int,boolean)
fld public final boolean evalResult
fld public final int predictedAlt
fld public final org.antlr.v4.runtime.atn.SemanticContext semctx
supr org.antlr.v4.runtime.atn.DecisionEventInfo

CLSS public final org.antlr.v4.runtime.atn.PredicateTransition
cons public init(org.antlr.v4.runtime.atn.ATNState,int,int,boolean)
fld public final boolean isCtxDependent
fld public final int predIndex
fld public final int ruleIndex
meth public boolean isEpsilon()
meth public boolean matches(int,int,int)
meth public int getSerializationType()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.atn.SemanticContext$Predicate getPredicate()
supr org.antlr.v4.runtime.atn.AbstractPredicateTransition

CLSS public abstract org.antlr.v4.runtime.atn.PredictionContext
cons protected init(int)
fld public final int cachedHashCode
fld public final int id
fld public final static int EMPTY_RETURN_STATE = 2147483647
meth protected static int calculateEmptyHashCode()
meth protected static int calculateHashCode(org.antlr.v4.runtime.atn.PredictionContext,int)
meth protected static int calculateHashCode(org.antlr.v4.runtime.atn.PredictionContext[],int[])
meth protected static void combineCommonParents(org.antlr.v4.runtime.atn.PredictionContext[])
meth public abstract boolean equals(java.lang.Object)
meth public abstract int getReturnState(int)
meth public abstract int size()
meth public abstract org.antlr.v4.runtime.atn.PredictionContext getParent(int)
meth public boolean hasEmptyPath()
meth public boolean isEmpty()
meth public final int hashCode()
meth public java.lang.String toString(org.antlr.v4.runtime.Recognizer<?,?>)
meth public java.lang.String[] toStrings(org.antlr.v4.runtime.Recognizer<?,?>,int)
meth public java.lang.String[] toStrings(org.antlr.v4.runtime.Recognizer<?,?>,org.antlr.v4.runtime.atn.PredictionContext,int)
meth public static java.lang.String toDOTString(org.antlr.v4.runtime.atn.PredictionContext)
meth public static java.util.List<org.antlr.v4.runtime.atn.PredictionContext> getAllContextNodes(org.antlr.v4.runtime.atn.PredictionContext)
meth public static org.antlr.v4.runtime.atn.PredictionContext fromRuleContext(org.antlr.v4.runtime.atn.ATN,org.antlr.v4.runtime.RuleContext)
meth public static org.antlr.v4.runtime.atn.PredictionContext getCachedContext(org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.PredictionContextCache,java.util.IdentityHashMap<org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.PredictionContext>)
meth public static org.antlr.v4.runtime.atn.PredictionContext merge(org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.PredictionContext,boolean,org.antlr.v4.runtime.misc.DoubleKeyMap<org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.PredictionContext>)
meth public static org.antlr.v4.runtime.atn.PredictionContext mergeArrays(org.antlr.v4.runtime.atn.ArrayPredictionContext,org.antlr.v4.runtime.atn.ArrayPredictionContext,boolean,org.antlr.v4.runtime.misc.DoubleKeyMap<org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.PredictionContext>)
meth public static org.antlr.v4.runtime.atn.PredictionContext mergeRoot(org.antlr.v4.runtime.atn.SingletonPredictionContext,org.antlr.v4.runtime.atn.SingletonPredictionContext,boolean)
meth public static org.antlr.v4.runtime.atn.PredictionContext mergeSingletons(org.antlr.v4.runtime.atn.SingletonPredictionContext,org.antlr.v4.runtime.atn.SingletonPredictionContext,boolean,org.antlr.v4.runtime.misc.DoubleKeyMap<org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.PredictionContext>)
meth public static void getAllContextNodes_(org.antlr.v4.runtime.atn.PredictionContext,java.util.List<org.antlr.v4.runtime.atn.PredictionContext>,java.util.Map<org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.PredictionContext>)
supr java.lang.Object
hfds INITIAL_HASH,globalNodeCount

CLSS public org.antlr.v4.runtime.atn.PredictionContextCache
cons public init()
fld protected final java.util.Map<org.antlr.v4.runtime.atn.PredictionContext,org.antlr.v4.runtime.atn.PredictionContext> cache
meth public int size()
meth public org.antlr.v4.runtime.atn.PredictionContext add(org.antlr.v4.runtime.atn.PredictionContext)
meth public org.antlr.v4.runtime.atn.PredictionContext get(org.antlr.v4.runtime.atn.PredictionContext)
supr java.lang.Object

CLSS public final !enum org.antlr.v4.runtime.atn.PredictionMode
fld public final static org.antlr.v4.runtime.atn.PredictionMode LL
fld public final static org.antlr.v4.runtime.atn.PredictionMode LL_EXACT_AMBIG_DETECTION
fld public final static org.antlr.v4.runtime.atn.PredictionMode SLL
meth public static boolean allConfigsInRuleStopStates(org.antlr.v4.runtime.atn.ATNConfigSet)
meth public static boolean allSubsetsConflict(java.util.Collection<java.util.BitSet>)
meth public static boolean allSubsetsEqual(java.util.Collection<java.util.BitSet>)
meth public static boolean hasConfigInRuleStopState(org.antlr.v4.runtime.atn.ATNConfigSet)
meth public static boolean hasConflictingAltSet(java.util.Collection<java.util.BitSet>)
meth public static boolean hasNonConflictingAltSet(java.util.Collection<java.util.BitSet>)
meth public static boolean hasSLLConflictTerminatingPrediction(org.antlr.v4.runtime.atn.PredictionMode,org.antlr.v4.runtime.atn.ATNConfigSet)
meth public static boolean hasStateAssociatedWithOneAlt(org.antlr.v4.runtime.atn.ATNConfigSet)
meth public static int getSingleViableAlt(java.util.Collection<java.util.BitSet>)
meth public static int getUniqueAlt(java.util.Collection<java.util.BitSet>)
meth public static int resolvesToJustOneViableAlt(java.util.Collection<java.util.BitSet>)
meth public static java.util.BitSet getAlts(java.util.Collection<java.util.BitSet>)
meth public static java.util.BitSet getAlts(org.antlr.v4.runtime.atn.ATNConfigSet)
meth public static java.util.Collection<java.util.BitSet> getConflictingAltSubsets(org.antlr.v4.runtime.atn.ATNConfigSet)
meth public static java.util.Map<org.antlr.v4.runtime.atn.ATNState,java.util.BitSet> getStateToAltMap(org.antlr.v4.runtime.atn.ATNConfigSet)
meth public static org.antlr.v4.runtime.atn.PredictionMode valueOf(java.lang.String)
meth public static org.antlr.v4.runtime.atn.PredictionMode[] values()
supr java.lang.Enum<org.antlr.v4.runtime.atn.PredictionMode>
hcls AltAndContextConfigEqualityComparator,AltAndContextMap

CLSS public org.antlr.v4.runtime.atn.ProfilingATNSimulator
cons public init(org.antlr.v4.runtime.Parser)
fld protected final org.antlr.v4.runtime.atn.DecisionInfo[] decisions
fld protected int _llStopIndex
fld protected int _sllStopIndex
fld protected int conflictingAltResolvedBySLL
fld protected int currentDecision
fld protected int numDecisions
fld protected org.antlr.v4.runtime.dfa.DFAState currentState
meth protected boolean evalSemanticContext(org.antlr.v4.runtime.atn.SemanticContext,org.antlr.v4.runtime.ParserRuleContext,int,boolean)
meth protected org.antlr.v4.runtime.atn.ATNConfigSet computeReachSet(org.antlr.v4.runtime.atn.ATNConfigSet,int,boolean)
meth protected org.antlr.v4.runtime.dfa.DFAState computeTargetState(org.antlr.v4.runtime.dfa.DFA,org.antlr.v4.runtime.dfa.DFAState,int)
meth protected org.antlr.v4.runtime.dfa.DFAState getExistingTargetState(org.antlr.v4.runtime.dfa.DFAState,int)
meth protected void reportAmbiguity(org.antlr.v4.runtime.dfa.DFA,org.antlr.v4.runtime.dfa.DFAState,int,int,boolean,java.util.BitSet,org.antlr.v4.runtime.atn.ATNConfigSet)
meth protected void reportAttemptingFullContext(org.antlr.v4.runtime.dfa.DFA,java.util.BitSet,org.antlr.v4.runtime.atn.ATNConfigSet,int,int)
meth protected void reportContextSensitivity(org.antlr.v4.runtime.dfa.DFA,int,org.antlr.v4.runtime.atn.ATNConfigSet,int,int)
meth public int adaptivePredict(org.antlr.v4.runtime.TokenStream,int,org.antlr.v4.runtime.ParserRuleContext)
meth public org.antlr.v4.runtime.atn.DecisionInfo[] getDecisionInfo()
meth public org.antlr.v4.runtime.dfa.DFAState getCurrentState()
supr org.antlr.v4.runtime.atn.ParserATNSimulator

CLSS public final org.antlr.v4.runtime.atn.RangeTransition
cons public init(org.antlr.v4.runtime.atn.ATNState,int,int)
fld public final int from
fld public final int to
meth public boolean matches(int,int,int)
meth public int getSerializationType()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.misc.IntervalSet label()
supr org.antlr.v4.runtime.atn.Transition

CLSS public final org.antlr.v4.runtime.atn.RuleStartState
cons public init()
fld public boolean isLeftRecursiveRule
fld public org.antlr.v4.runtime.atn.RuleStopState stopState
meth public int getStateType()
supr org.antlr.v4.runtime.atn.ATNState

CLSS public final org.antlr.v4.runtime.atn.RuleStopState
cons public init()
meth public int getStateType()
supr org.antlr.v4.runtime.atn.ATNState

CLSS public final org.antlr.v4.runtime.atn.RuleTransition
cons public init(org.antlr.v4.runtime.atn.RuleStartState,int,int,org.antlr.v4.runtime.atn.ATNState)
cons public init(org.antlr.v4.runtime.atn.RuleStartState,int,org.antlr.v4.runtime.atn.ATNState)
 anno 0 java.lang.Deprecated()
fld public final int precedence
fld public final int ruleIndex
fld public org.antlr.v4.runtime.atn.ATNState followState
meth public boolean isEpsilon()
meth public boolean matches(int,int,int)
meth public int getSerializationType()
supr org.antlr.v4.runtime.atn.Transition

CLSS public abstract org.antlr.v4.runtime.atn.SemanticContext
cons public init()
innr public abstract static Operator
innr public static AND
innr public static Empty
innr public static OR
innr public static PrecedencePredicate
innr public static Predicate
meth public abstract boolean eval(org.antlr.v4.runtime.Recognizer<?,?>,org.antlr.v4.runtime.RuleContext)
meth public org.antlr.v4.runtime.atn.SemanticContext evalPrecedence(org.antlr.v4.runtime.Recognizer<?,?>,org.antlr.v4.runtime.RuleContext)
meth public static org.antlr.v4.runtime.atn.SemanticContext and(org.antlr.v4.runtime.atn.SemanticContext,org.antlr.v4.runtime.atn.SemanticContext)
meth public static org.antlr.v4.runtime.atn.SemanticContext or(org.antlr.v4.runtime.atn.SemanticContext,org.antlr.v4.runtime.atn.SemanticContext)
supr java.lang.Object

CLSS public static org.antlr.v4.runtime.atn.SemanticContext$AND
 outer org.antlr.v4.runtime.atn.SemanticContext
cons public init(org.antlr.v4.runtime.atn.SemanticContext,org.antlr.v4.runtime.atn.SemanticContext)
fld public final org.antlr.v4.runtime.atn.SemanticContext[] opnds
meth public boolean equals(java.lang.Object)
meth public boolean eval(org.antlr.v4.runtime.Recognizer<?,?>,org.antlr.v4.runtime.RuleContext)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.Collection<org.antlr.v4.runtime.atn.SemanticContext> getOperands()
meth public org.antlr.v4.runtime.atn.SemanticContext evalPrecedence(org.antlr.v4.runtime.Recognizer<?,?>,org.antlr.v4.runtime.RuleContext)
supr org.antlr.v4.runtime.atn.SemanticContext$Operator

CLSS public static org.antlr.v4.runtime.atn.SemanticContext$Empty
 outer org.antlr.v4.runtime.atn.SemanticContext
cons public init()
fld public final static org.antlr.v4.runtime.atn.SemanticContext$Empty Instance
meth public boolean eval(org.antlr.v4.runtime.Recognizer<?,?>,org.antlr.v4.runtime.RuleContext)
supr org.antlr.v4.runtime.atn.SemanticContext

CLSS public static org.antlr.v4.runtime.atn.SemanticContext$OR
 outer org.antlr.v4.runtime.atn.SemanticContext
cons public init(org.antlr.v4.runtime.atn.SemanticContext,org.antlr.v4.runtime.atn.SemanticContext)
fld public final org.antlr.v4.runtime.atn.SemanticContext[] opnds
meth public boolean equals(java.lang.Object)
meth public boolean eval(org.antlr.v4.runtime.Recognizer<?,?>,org.antlr.v4.runtime.RuleContext)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.Collection<org.antlr.v4.runtime.atn.SemanticContext> getOperands()
meth public org.antlr.v4.runtime.atn.SemanticContext evalPrecedence(org.antlr.v4.runtime.Recognizer<?,?>,org.antlr.v4.runtime.RuleContext)
supr org.antlr.v4.runtime.atn.SemanticContext$Operator

CLSS public abstract static org.antlr.v4.runtime.atn.SemanticContext$Operator
 outer org.antlr.v4.runtime.atn.SemanticContext
cons public init()
meth public abstract java.util.Collection<org.antlr.v4.runtime.atn.SemanticContext> getOperands()
supr org.antlr.v4.runtime.atn.SemanticContext

CLSS public static org.antlr.v4.runtime.atn.SemanticContext$PrecedencePredicate
 outer org.antlr.v4.runtime.atn.SemanticContext
cons protected init()
cons public init(int)
fld public final int precedence
intf java.lang.Comparable<org.antlr.v4.runtime.atn.SemanticContext$PrecedencePredicate>
meth public boolean equals(java.lang.Object)
meth public boolean eval(org.antlr.v4.runtime.Recognizer<?,?>,org.antlr.v4.runtime.RuleContext)
meth public int compareTo(org.antlr.v4.runtime.atn.SemanticContext$PrecedencePredicate)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.atn.SemanticContext evalPrecedence(org.antlr.v4.runtime.Recognizer<?,?>,org.antlr.v4.runtime.RuleContext)
supr org.antlr.v4.runtime.atn.SemanticContext

CLSS public static org.antlr.v4.runtime.atn.SemanticContext$Predicate
 outer org.antlr.v4.runtime.atn.SemanticContext
cons protected init()
cons public init(int,int,boolean)
fld public final boolean isCtxDependent
fld public final int predIndex
fld public final int ruleIndex
meth public boolean equals(java.lang.Object)
meth public boolean eval(org.antlr.v4.runtime.Recognizer<?,?>,org.antlr.v4.runtime.RuleContext)
meth public int hashCode()
meth public java.lang.String toString()
supr org.antlr.v4.runtime.atn.SemanticContext

CLSS public org.antlr.v4.runtime.atn.SetTransition
cons public init(org.antlr.v4.runtime.atn.ATNState,org.antlr.v4.runtime.misc.IntervalSet)
fld public final org.antlr.v4.runtime.misc.IntervalSet set
meth public boolean matches(int,int,int)
meth public int getSerializationType()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.misc.IntervalSet label()
supr org.antlr.v4.runtime.atn.Transition

CLSS public org.antlr.v4.runtime.atn.SingletonPredictionContext
fld public final int returnState
fld public final org.antlr.v4.runtime.atn.PredictionContext parent
meth public boolean equals(java.lang.Object)
meth public int getReturnState(int)
meth public int size()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.atn.PredictionContext getParent(int)
meth public static org.antlr.v4.runtime.atn.SingletonPredictionContext create(org.antlr.v4.runtime.atn.PredictionContext,int)
supr org.antlr.v4.runtime.atn.PredictionContext

CLSS public final org.antlr.v4.runtime.atn.StarBlockStartState
cons public init()
meth public int getStateType()
supr org.antlr.v4.runtime.atn.BlockStartState

CLSS public final org.antlr.v4.runtime.atn.StarLoopEntryState
cons public init()
fld public boolean isPrecedenceDecision
fld public org.antlr.v4.runtime.atn.StarLoopbackState loopBackState
meth public int getStateType()
supr org.antlr.v4.runtime.atn.DecisionState

CLSS public final org.antlr.v4.runtime.atn.StarLoopbackState
cons public init()
meth public final org.antlr.v4.runtime.atn.StarLoopEntryState getLoopEntryState()
meth public int getStateType()
supr org.antlr.v4.runtime.atn.ATNState

CLSS public final org.antlr.v4.runtime.atn.TokensStartState
cons public init()
meth public int getStateType()
supr org.antlr.v4.runtime.atn.DecisionState

CLSS public abstract org.antlr.v4.runtime.atn.Transition
cons protected init(org.antlr.v4.runtime.atn.ATNState)
fld public final static int ACTION = 6
fld public final static int ATOM = 5
fld public final static int EPSILON = 1
fld public final static int NOT_SET = 8
fld public final static int PRECEDENCE = 10
fld public final static int PREDICATE = 4
fld public final static int RANGE = 2
fld public final static int RULE = 3
fld public final static int SET = 7
fld public final static int WILDCARD = 9
fld public final static java.util.List<java.lang.String> serializationNames
fld public final static java.util.Map<java.lang.Class<? extends org.antlr.v4.runtime.atn.Transition>,java.lang.Integer> serializationTypes
fld public org.antlr.v4.runtime.atn.ATNState target
meth public abstract boolean matches(int,int,int)
meth public abstract int getSerializationType()
meth public boolean isEpsilon()
meth public org.antlr.v4.runtime.misc.IntervalSet label()
supr java.lang.Object

CLSS public final org.antlr.v4.runtime.atn.WildcardTransition
cons public init(org.antlr.v4.runtime.atn.ATNState)
meth public boolean matches(int,int,int)
meth public int getSerializationType()
meth public java.lang.String toString()
supr org.antlr.v4.runtime.atn.Transition

CLSS public org.antlr.v4.runtime.dfa.DFA
cons public init(org.antlr.v4.runtime.atn.DecisionState)
cons public init(org.antlr.v4.runtime.atn.DecisionState,int)
fld public final int decision
fld public final java.util.Map<org.antlr.v4.runtime.dfa.DFAState,org.antlr.v4.runtime.dfa.DFAState> states
fld public final org.antlr.v4.runtime.atn.DecisionState atnStartState
fld public volatile org.antlr.v4.runtime.dfa.DFAState s0
meth public final boolean isPrecedenceDfa()
meth public final org.antlr.v4.runtime.dfa.DFAState getPrecedenceStartState(int)
meth public final void setPrecedenceDfa(boolean)
 anno 0 java.lang.Deprecated()
meth public final void setPrecedenceStartState(int,org.antlr.v4.runtime.dfa.DFAState)
meth public java.lang.String toLexerString()
meth public java.lang.String toString()
meth public java.lang.String toString(java.lang.String[])
 anno 0 java.lang.Deprecated()
meth public java.lang.String toString(org.antlr.v4.runtime.Vocabulary)
meth public java.util.List<org.antlr.v4.runtime.dfa.DFAState> getStates()
supr java.lang.Object
hfds precedenceDfa

CLSS public org.antlr.v4.runtime.dfa.DFASerializer
cons public init(org.antlr.v4.runtime.dfa.DFA,java.lang.String[])
 anno 0 java.lang.Deprecated()
cons public init(org.antlr.v4.runtime.dfa.DFA,org.antlr.v4.runtime.Vocabulary)
meth protected java.lang.String getEdgeLabel(int)
meth protected java.lang.String getStateString(org.antlr.v4.runtime.dfa.DFAState)
meth public java.lang.String toString()
supr java.lang.Object
hfds dfa,vocabulary

CLSS public org.antlr.v4.runtime.dfa.DFAState
cons public init()
cons public init(int)
cons public init(org.antlr.v4.runtime.atn.ATNConfigSet)
fld public boolean isAcceptState
fld public boolean requiresFullContext
fld public int prediction
fld public int stateNumber
fld public org.antlr.v4.runtime.atn.ATNConfigSet configs
fld public org.antlr.v4.runtime.atn.LexerActionExecutor lexerActionExecutor
fld public org.antlr.v4.runtime.dfa.DFAState$PredPrediction[] predicates
fld public org.antlr.v4.runtime.dfa.DFAState[] edges
innr public static PredPrediction
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.Set<java.lang.Integer> getAltSet()
supr java.lang.Object

CLSS public static org.antlr.v4.runtime.dfa.DFAState$PredPrediction
 outer org.antlr.v4.runtime.dfa.DFAState
cons public init(org.antlr.v4.runtime.atn.SemanticContext,int)
fld public int alt
fld public org.antlr.v4.runtime.atn.SemanticContext pred
meth public java.lang.String toString()
supr java.lang.Object

CLSS public org.antlr.v4.runtime.dfa.LexerDFASerializer
cons public init(org.antlr.v4.runtime.dfa.DFA)
meth protected java.lang.String getEdgeLabel(int)
supr org.antlr.v4.runtime.dfa.DFASerializer

CLSS public abstract org.antlr.v4.runtime.misc.AbstractEqualityComparator<%0 extends java.lang.Object>
cons public init()
intf org.antlr.v4.runtime.misc.EqualityComparator<{org.antlr.v4.runtime.misc.AbstractEqualityComparator%0}>
supr java.lang.Object

CLSS public org.antlr.v4.runtime.misc.Array2DHashSet<%0 extends java.lang.Object>
cons public init()
cons public init(org.antlr.v4.runtime.misc.AbstractEqualityComparator<? super {org.antlr.v4.runtime.misc.Array2DHashSet%0}>)
cons public init(org.antlr.v4.runtime.misc.AbstractEqualityComparator<? super {org.antlr.v4.runtime.misc.Array2DHashSet%0}>,int,int)
fld protected final int initialBucketCapacity
fld protected final int initialCapacity
fld protected final org.antlr.v4.runtime.misc.AbstractEqualityComparator<? super {org.antlr.v4.runtime.misc.Array2DHashSet%0}> comparator
fld protected int currentPrime
fld protected int n
fld protected int threshold
fld protected {org.antlr.v4.runtime.misc.Array2DHashSet%0}[][] buckets
fld public final static double LOAD_FACTOR = 0.75
fld public final static int INITAL_BUCKET_CAPACITY = 8
fld public final static int INITAL_CAPACITY = 16
innr protected SetIterator
intf java.util.Set<{org.antlr.v4.runtime.misc.Array2DHashSet%0}>
meth protected final int getBucket({org.antlr.v4.runtime.misc.Array2DHashSet%0})
meth protected void expand()
meth protected {org.antlr.v4.runtime.misc.Array2DHashSet%0} asElementType(java.lang.Object)
meth protected {org.antlr.v4.runtime.misc.Array2DHashSet%0} getOrAddImpl({org.antlr.v4.runtime.misc.Array2DHashSet%0})
meth protected {org.antlr.v4.runtime.misc.Array2DHashSet%0}[] createBucket(int)
meth protected {org.antlr.v4.runtime.misc.Array2DHashSet%0}[][] createBuckets(int)
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public boolean addAll(java.util.Collection<? extends {org.antlr.v4.runtime.misc.Array2DHashSet%0}>)
meth public boolean containsAll(java.util.Collection<?>)
meth public boolean containsFast({org.antlr.v4.runtime.misc.Array2DHashSet%0})
meth public boolean equals(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean removeFast({org.antlr.v4.runtime.misc.Array2DHashSet%0})
meth public boolean retainAll(java.util.Collection<?>)
meth public final boolean add({org.antlr.v4.runtime.misc.Array2DHashSet%0})
meth public final boolean contains(java.lang.Object)
meth public final boolean isEmpty()
meth public final boolean remove(java.lang.Object)
meth public final int size()
meth public final {org.antlr.v4.runtime.misc.Array2DHashSet%0} getOrAdd({org.antlr.v4.runtime.misc.Array2DHashSet%0})
meth public int hashCode()
meth public java.lang.String toString()
meth public java.lang.String toTableString()
meth public java.util.Iterator<{org.antlr.v4.runtime.misc.Array2DHashSet%0}> iterator()
meth public void clear()
meth public {org.antlr.v4.runtime.misc.Array2DHashSet%0} get({org.antlr.v4.runtime.misc.Array2DHashSet%0})
meth public {org.antlr.v4.runtime.misc.Array2DHashSet%0}[] toArray()
supr java.lang.Object

CLSS protected org.antlr.v4.runtime.misc.Array2DHashSet$SetIterator
 outer org.antlr.v4.runtime.misc.Array2DHashSet
cons public init({org.antlr.v4.runtime.misc.Array2DHashSet%0}[])
intf java.util.Iterator<{org.antlr.v4.runtime.misc.Array2DHashSet%0}>
meth public boolean hasNext()
meth public void remove()
meth public {org.antlr.v4.runtime.misc.Array2DHashSet%0} next()
supr java.lang.Object
hfds data,nextIndex,removed

CLSS public org.antlr.v4.runtime.misc.DoubleKeyMap<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object>
cons public init()
meth public java.util.Collection<{org.antlr.v4.runtime.misc.DoubleKeyMap%2}> values({org.antlr.v4.runtime.misc.DoubleKeyMap%0})
meth public java.util.Map<{org.antlr.v4.runtime.misc.DoubleKeyMap%1},{org.antlr.v4.runtime.misc.DoubleKeyMap%2}> get({org.antlr.v4.runtime.misc.DoubleKeyMap%0})
meth public java.util.Set<{org.antlr.v4.runtime.misc.DoubleKeyMap%0}> keySet()
meth public java.util.Set<{org.antlr.v4.runtime.misc.DoubleKeyMap%1}> keySet({org.antlr.v4.runtime.misc.DoubleKeyMap%0})
meth public {org.antlr.v4.runtime.misc.DoubleKeyMap%2} get({org.antlr.v4.runtime.misc.DoubleKeyMap%0},{org.antlr.v4.runtime.misc.DoubleKeyMap%1})
meth public {org.antlr.v4.runtime.misc.DoubleKeyMap%2} put({org.antlr.v4.runtime.misc.DoubleKeyMap%0},{org.antlr.v4.runtime.misc.DoubleKeyMap%1},{org.antlr.v4.runtime.misc.DoubleKeyMap%2})
supr java.lang.Object
hfds data

CLSS public abstract interface org.antlr.v4.runtime.misc.EqualityComparator<%0 extends java.lang.Object>
meth public abstract boolean equals({org.antlr.v4.runtime.misc.EqualityComparator%0},{org.antlr.v4.runtime.misc.EqualityComparator%0})
meth public abstract int hashCode({org.antlr.v4.runtime.misc.EqualityComparator%0})

CLSS public org.antlr.v4.runtime.misc.FlexibleHashMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(org.antlr.v4.runtime.misc.AbstractEqualityComparator<? super {org.antlr.v4.runtime.misc.FlexibleHashMap%0}>)
cons public init(org.antlr.v4.runtime.misc.AbstractEqualityComparator<? super {org.antlr.v4.runtime.misc.FlexibleHashMap%0}>,int,int)
fld protected final int initialBucketCapacity
fld protected final int initialCapacity
fld protected final org.antlr.v4.runtime.misc.AbstractEqualityComparator<? super {org.antlr.v4.runtime.misc.FlexibleHashMap%0}> comparator
fld protected int currentPrime
fld protected int n
fld protected int threshold
fld protected java.util.LinkedList<org.antlr.v4.runtime.misc.FlexibleHashMap$Entry<{org.antlr.v4.runtime.misc.FlexibleHashMap%0},{org.antlr.v4.runtime.misc.FlexibleHashMap%1}>>[] buckets
fld public final static double LOAD_FACTOR = 0.75
fld public final static int INITAL_BUCKET_CAPACITY = 8
fld public final static int INITAL_CAPACITY = 16
innr public static Entry
intf java.util.Map<{org.antlr.v4.runtime.misc.FlexibleHashMap%0},{org.antlr.v4.runtime.misc.FlexibleHashMap%1}>
meth protected int getBucket({org.antlr.v4.runtime.misc.FlexibleHashMap%0})
meth protected void expand()
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.String toString()
meth public java.lang.String toTableString()
meth public java.util.Collection<{org.antlr.v4.runtime.misc.FlexibleHashMap%1}> values()
meth public java.util.Set<java.util.Map$Entry<{org.antlr.v4.runtime.misc.FlexibleHashMap%0},{org.antlr.v4.runtime.misc.FlexibleHashMap%1}>> entrySet()
meth public java.util.Set<{org.antlr.v4.runtime.misc.FlexibleHashMap%0}> keySet()
meth public static void main(java.lang.String[])
meth public void clear()
meth public void putAll(java.util.Map<? extends {org.antlr.v4.runtime.misc.FlexibleHashMap%0},? extends {org.antlr.v4.runtime.misc.FlexibleHashMap%1}>)
meth public {org.antlr.v4.runtime.misc.FlexibleHashMap%1} get(java.lang.Object)
meth public {org.antlr.v4.runtime.misc.FlexibleHashMap%1} put({org.antlr.v4.runtime.misc.FlexibleHashMap%0},{org.antlr.v4.runtime.misc.FlexibleHashMap%1})
meth public {org.antlr.v4.runtime.misc.FlexibleHashMap%1} remove(java.lang.Object)
supr java.lang.Object

CLSS public static org.antlr.v4.runtime.misc.FlexibleHashMap$Entry<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.antlr.v4.runtime.misc.FlexibleHashMap
cons public init({org.antlr.v4.runtime.misc.FlexibleHashMap$Entry%0},{org.antlr.v4.runtime.misc.FlexibleHashMap$Entry%1})
fld public final {org.antlr.v4.runtime.misc.FlexibleHashMap$Entry%0} key
fld public {org.antlr.v4.runtime.misc.FlexibleHashMap$Entry%1} value
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface org.antlr.v4.runtime.misc.IntSet
meth public abstract boolean contains(int)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isNil()
meth public abstract int size()
meth public abstract java.lang.String toString()
meth public abstract java.util.List<java.lang.Integer> toList()
meth public abstract org.antlr.v4.runtime.misc.IntSet addAll(org.antlr.v4.runtime.misc.IntSet)
meth public abstract org.antlr.v4.runtime.misc.IntSet and(org.antlr.v4.runtime.misc.IntSet)
meth public abstract org.antlr.v4.runtime.misc.IntSet complement(org.antlr.v4.runtime.misc.IntSet)
meth public abstract org.antlr.v4.runtime.misc.IntSet or(org.antlr.v4.runtime.misc.IntSet)
meth public abstract org.antlr.v4.runtime.misc.IntSet subtract(org.antlr.v4.runtime.misc.IntSet)
meth public abstract void add(int)
meth public abstract void remove(int)

CLSS public org.antlr.v4.runtime.misc.IntegerList
cons public init()
cons public init(int)
cons public init(java.util.Collection<java.lang.Integer>)
cons public init(org.antlr.v4.runtime.misc.IntegerList)
meth public boolean equals(java.lang.Object)
meth public final boolean contains(int)
meth public final boolean isEmpty()
meth public final char[] toCharArray()
meth public final int binarySearch(int)
meth public final int binarySearch(int,int,int)
meth public final int get(int)
meth public final int removeAt(int)
meth public final int set(int,int)
meth public final int size()
meth public final int[] toArray()
meth public final void add(int)
meth public final void addAll(int[])
meth public final void addAll(java.util.Collection<java.lang.Integer>)
meth public final void addAll(org.antlr.v4.runtime.misc.IntegerList)
meth public final void clear()
meth public final void removeRange(int,int)
meth public final void sort()
meth public final void trimToSize()
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds EMPTY_DATA,INITIAL_SIZE,MAX_ARRAY_SIZE,_data,_size

CLSS public org.antlr.v4.runtime.misc.IntegerStack
cons public init()
cons public init(int)
cons public init(org.antlr.v4.runtime.misc.IntegerStack)
meth public final int peek()
meth public final int pop()
meth public final void push(int)
supr org.antlr.v4.runtime.misc.IntegerList

CLSS public org.antlr.v4.runtime.misc.InterpreterDataReader
cons public init()
innr public static InterpreterData
meth public static org.antlr.v4.runtime.misc.InterpreterDataReader$InterpreterData parseFile(java.lang.String)
supr java.lang.Object

CLSS public static org.antlr.v4.runtime.misc.InterpreterDataReader$InterpreterData
 outer org.antlr.v4.runtime.misc.InterpreterDataReader
cons public init()
supr java.lang.Object
hfds atn,channels,modes,ruleNames,vocabulary

CLSS public org.antlr.v4.runtime.misc.Interval
cons public init(int,int)
fld public final static int INTERVAL_POOL_MAX_VALUE = 1000
fld public final static org.antlr.v4.runtime.misc.Interval INVALID
fld public int a
fld public int b
meth public boolean adjacent(org.antlr.v4.runtime.misc.Interval)
meth public boolean disjoint(org.antlr.v4.runtime.misc.Interval)
meth public boolean equals(java.lang.Object)
meth public boolean properlyContains(org.antlr.v4.runtime.misc.Interval)
meth public boolean startsAfter(org.antlr.v4.runtime.misc.Interval)
meth public boolean startsAfterDisjoint(org.antlr.v4.runtime.misc.Interval)
meth public boolean startsAfterNonDisjoint(org.antlr.v4.runtime.misc.Interval)
meth public boolean startsBeforeDisjoint(org.antlr.v4.runtime.misc.Interval)
meth public boolean startsBeforeNonDisjoint(org.antlr.v4.runtime.misc.Interval)
meth public int hashCode()
meth public int length()
meth public java.lang.String toString()
meth public org.antlr.v4.runtime.misc.Interval differenceNotProperlyContained(org.antlr.v4.runtime.misc.Interval)
meth public org.antlr.v4.runtime.misc.Interval intersection(org.antlr.v4.runtime.misc.Interval)
meth public org.antlr.v4.runtime.misc.Interval union(org.antlr.v4.runtime.misc.Interval)
meth public static org.antlr.v4.runtime.misc.Interval of(int,int)
supr java.lang.Object
hfds cache

CLSS public org.antlr.v4.runtime.misc.IntervalSet
cons public !varargs init(int[])
cons public init(java.util.List<org.antlr.v4.runtime.misc.Interval>)
cons public init(org.antlr.v4.runtime.misc.IntervalSet)
fld protected boolean readonly
fld protected java.util.List<org.antlr.v4.runtime.misc.Interval> intervals
fld public final static org.antlr.v4.runtime.misc.IntervalSet COMPLETE_CHAR_SET
fld public final static org.antlr.v4.runtime.misc.IntervalSet EMPTY_SET
intf org.antlr.v4.runtime.misc.IntSet
meth protected java.lang.String elementName(java.lang.String[],int)
 anno 0 java.lang.Deprecated()
meth protected java.lang.String elementName(org.antlr.v4.runtime.Vocabulary,int)
meth protected void add(org.antlr.v4.runtime.misc.Interval)
meth public boolean contains(int)
meth public boolean equals(java.lang.Object)
meth public boolean isNil()
meth public boolean isReadonly()
meth public int get(int)
meth public int getMaxElement()
meth public int getMinElement()
meth public int hashCode()
meth public int size()
meth public int[] toArray()
meth public java.lang.String toString()
meth public java.lang.String toString(boolean)
meth public java.lang.String toString(java.lang.String[])
 anno 0 java.lang.Deprecated()
meth public java.lang.String toString(org.antlr.v4.runtime.Vocabulary)
meth public java.util.List<java.lang.Integer> toList()
meth public java.util.List<org.antlr.v4.runtime.misc.Interval> getIntervals()
meth public java.util.Set<java.lang.Integer> toSet()
meth public org.antlr.v4.runtime.misc.IntegerList toIntegerList()
meth public org.antlr.v4.runtime.misc.IntervalSet addAll(org.antlr.v4.runtime.misc.IntSet)
meth public org.antlr.v4.runtime.misc.IntervalSet and(org.antlr.v4.runtime.misc.IntSet)
meth public org.antlr.v4.runtime.misc.IntervalSet complement(int,int)
meth public org.antlr.v4.runtime.misc.IntervalSet complement(org.antlr.v4.runtime.misc.IntSet)
meth public org.antlr.v4.runtime.misc.IntervalSet or(org.antlr.v4.runtime.misc.IntSet)
meth public org.antlr.v4.runtime.misc.IntervalSet subtract(org.antlr.v4.runtime.misc.IntSet)
meth public static org.antlr.v4.runtime.misc.IntervalSet of(int)
meth public static org.antlr.v4.runtime.misc.IntervalSet of(int,int)
meth public static org.antlr.v4.runtime.misc.IntervalSet or(org.antlr.v4.runtime.misc.IntervalSet[])
meth public static org.antlr.v4.runtime.misc.IntervalSet subtract(org.antlr.v4.runtime.misc.IntervalSet,org.antlr.v4.runtime.misc.IntervalSet)
meth public void add(int)
meth public void add(int,int)
meth public void clear()
meth public void remove(int)
meth public void setReadonly(boolean)
supr java.lang.Object

CLSS public org.antlr.v4.runtime.misc.LogManager
cons public init()
fld protected java.util.List<org.antlr.v4.runtime.misc.LogManager$Record> records
innr protected static Record
meth public java.lang.String save() throws java.io.IOException
meth public java.lang.String toString()
meth public static void main(java.lang.String[]) throws java.io.IOException
meth public void log(java.lang.String)
meth public void log(java.lang.String,java.lang.String)
meth public void save(java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS protected static org.antlr.v4.runtime.misc.LogManager$Record
 outer org.antlr.v4.runtime.misc.LogManager
cons public init()
meth public java.lang.String toString()
supr java.lang.Object
hfds component,location,msg,timestamp

CLSS public org.antlr.v4.runtime.misc.MultiMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
meth public java.util.List<org.antlr.v4.runtime.misc.Pair<{org.antlr.v4.runtime.misc.MultiMap%0},{org.antlr.v4.runtime.misc.MultiMap%1}>> getPairs()
meth public void map({org.antlr.v4.runtime.misc.MultiMap%0},{org.antlr.v4.runtime.misc.MultiMap%1})
supr java.util.LinkedHashMap<{org.antlr.v4.runtime.misc.MultiMap%0},java.util.List<{org.antlr.v4.runtime.misc.MultiMap%1}>>

CLSS public final org.antlr.v4.runtime.misc.MurmurHash
meth public static <%0 extends java.lang.Object> int hashCode({%%0}[],int)
meth public static int finish(int,int)
meth public static int initialize()
meth public static int initialize(int)
meth public static int update(int,int)
meth public static int update(int,java.lang.Object)
supr java.lang.Object
hfds DEFAULT_SEED

CLSS public abstract interface !annotation org.antlr.v4.runtime.misc.NotNull
 anno 0 java.lang.Deprecated()
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD, METHOD, PARAMETER, LOCAL_VARIABLE])
intf java.lang.annotation.Annotation

CLSS public final org.antlr.v4.runtime.misc.ObjectEqualityComparator
cons public init()
fld public final static org.antlr.v4.runtime.misc.ObjectEqualityComparator INSTANCE
meth public boolean equals(java.lang.Object,java.lang.Object)
meth public int hashCode(java.lang.Object)
supr org.antlr.v4.runtime.misc.AbstractEqualityComparator<java.lang.Object>

CLSS public org.antlr.v4.runtime.misc.OrderedHashSet<%0 extends java.lang.Object>
cons public init()
fld protected java.util.ArrayList<{org.antlr.v4.runtime.misc.OrderedHashSet%0}> elements
meth public boolean add({org.antlr.v4.runtime.misc.OrderedHashSet%0})
meth public boolean equals(java.lang.Object)
meth public boolean remove(int)
meth public boolean remove(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.Object[] toArray()
meth public java.lang.String toString()
meth public java.util.Iterator<{org.antlr.v4.runtime.misc.OrderedHashSet%0}> iterator()
meth public java.util.List<{org.antlr.v4.runtime.misc.OrderedHashSet%0}> elements()
meth public void clear()
meth public {org.antlr.v4.runtime.misc.OrderedHashSet%0} get(int)
meth public {org.antlr.v4.runtime.misc.OrderedHashSet%0} set(int,{org.antlr.v4.runtime.misc.OrderedHashSet%0})
supr java.util.LinkedHashSet<{org.antlr.v4.runtime.misc.OrderedHashSet%0}>

CLSS public org.antlr.v4.runtime.misc.Pair<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init({org.antlr.v4.runtime.misc.Pair%0},{org.antlr.v4.runtime.misc.Pair%1})
fld public final {org.antlr.v4.runtime.misc.Pair%0} a
fld public final {org.antlr.v4.runtime.misc.Pair%1} b
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public org.antlr.v4.runtime.misc.ParseCancellationException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.util.concurrent.CancellationException

CLSS public abstract interface org.antlr.v4.runtime.misc.Predicate<%0 extends java.lang.Object>
meth public abstract boolean test({org.antlr.v4.runtime.misc.Predicate%0})

CLSS public org.antlr.v4.runtime.misc.TestRig
 anno 0 java.lang.Deprecated()
cons public init()
meth public static void main(java.lang.String[])
supr java.lang.Object

CLSS public org.antlr.v4.runtime.misc.Triple<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object>
cons public init({org.antlr.v4.runtime.misc.Triple%0},{org.antlr.v4.runtime.misc.Triple%1},{org.antlr.v4.runtime.misc.Triple%2})
fld public final {org.antlr.v4.runtime.misc.Triple%0} a
fld public final {org.antlr.v4.runtime.misc.Triple%1} b
fld public final {org.antlr.v4.runtime.misc.Triple%2} c
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public org.antlr.v4.runtime.misc.Utils
cons public init()
meth public static <%0 extends java.lang.Object> java.lang.String join(java.util.Iterator<{%%0}>,java.lang.String)
meth public static <%0 extends java.lang.Object> java.lang.String join({%%0}[],java.lang.String)
meth public static <%0 extends java.lang.Object> void removeAllElements(java.util.Collection<{%%0}>,{%%0})
meth public static char[] readFile(java.lang.String) throws java.io.IOException
meth public static char[] readFile(java.lang.String,java.lang.String) throws java.io.IOException
meth public static char[] toCharArray(org.antlr.v4.runtime.misc.IntegerList)
meth public static int count(java.lang.String,char)
meth public static int numNonnull(java.lang.Object[])
meth public static java.lang.String escapeWhitespace(java.lang.String,boolean)
meth public static java.lang.String expandTabs(java.lang.String,int)
meth public static java.lang.String newlines(int)
meth public static java.lang.String sequence(int,java.lang.String)
meth public static java.lang.String spaces(int)
meth public static java.util.Map<java.lang.String,java.lang.Integer> toMap(java.lang.String[])
meth public static org.antlr.v4.runtime.misc.IntervalSet toSet(java.util.BitSet)
meth public static void writeFile(java.lang.String,java.lang.String) throws java.io.IOException
meth public static void writeFile(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.antlr.v4.runtime.tree.AbstractParseTreeVisitor<%0 extends java.lang.Object>
cons public init()
intf org.antlr.v4.runtime.tree.ParseTreeVisitor<{org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0}>
meth protected boolean shouldVisitNextChild(org.antlr.v4.runtime.tree.RuleNode,{org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0})
meth protected {org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0} aggregateResult({org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0},{org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0})
meth protected {org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0} defaultResult()
meth public {org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0} visit(org.antlr.v4.runtime.tree.ParseTree)
meth public {org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0} visitChildren(org.antlr.v4.runtime.tree.RuleNode)
meth public {org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0} visitErrorNode(org.antlr.v4.runtime.tree.ErrorNode)
meth public {org.antlr.v4.runtime.tree.AbstractParseTreeVisitor%0} visitTerminal(org.antlr.v4.runtime.tree.TerminalNode)
supr java.lang.Object

CLSS public abstract interface org.antlr.v4.runtime.tree.ErrorNode
intf org.antlr.v4.runtime.tree.TerminalNode

CLSS public org.antlr.v4.runtime.tree.ErrorNodeImpl
cons public init(org.antlr.v4.runtime.Token)
intf org.antlr.v4.runtime.tree.ErrorNode
meth public <%0 extends java.lang.Object> {%%0} accept(org.antlr.v4.runtime.tree.ParseTreeVisitor<? extends {%%0}>)
supr org.antlr.v4.runtime.tree.TerminalNodeImpl

CLSS public org.antlr.v4.runtime.tree.IterativeParseTreeWalker
cons public init()
meth public void walk(org.antlr.v4.runtime.tree.ParseTreeListener,org.antlr.v4.runtime.tree.ParseTree)
supr org.antlr.v4.runtime.tree.ParseTreeWalker

CLSS public abstract interface org.antlr.v4.runtime.tree.ParseTree
intf org.antlr.v4.runtime.tree.SyntaxTree
meth public abstract <%0 extends java.lang.Object> {%%0} accept(org.antlr.v4.runtime.tree.ParseTreeVisitor<? extends {%%0}>)
meth public abstract java.lang.String getText()
meth public abstract java.lang.String toStringTree(org.antlr.v4.runtime.Parser)
meth public abstract org.antlr.v4.runtime.tree.ParseTree getChild(int)
meth public abstract org.antlr.v4.runtime.tree.ParseTree getParent()
meth public abstract void setParent(org.antlr.v4.runtime.RuleContext)

CLSS public abstract interface org.antlr.v4.runtime.tree.ParseTreeListener
meth public abstract void enterEveryRule(org.antlr.v4.runtime.ParserRuleContext)
meth public abstract void exitEveryRule(org.antlr.v4.runtime.ParserRuleContext)
meth public abstract void visitErrorNode(org.antlr.v4.runtime.tree.ErrorNode)
meth public abstract void visitTerminal(org.antlr.v4.runtime.tree.TerminalNode)

CLSS public org.antlr.v4.runtime.tree.ParseTreeProperty<%0 extends java.lang.Object>
cons public init()
fld protected java.util.Map<org.antlr.v4.runtime.tree.ParseTree,{org.antlr.v4.runtime.tree.ParseTreeProperty%0}> annotations
meth public void put(org.antlr.v4.runtime.tree.ParseTree,{org.antlr.v4.runtime.tree.ParseTreeProperty%0})
meth public {org.antlr.v4.runtime.tree.ParseTreeProperty%0} get(org.antlr.v4.runtime.tree.ParseTree)
meth public {org.antlr.v4.runtime.tree.ParseTreeProperty%0} removeFrom(org.antlr.v4.runtime.tree.ParseTree)
supr java.lang.Object

CLSS public abstract interface org.antlr.v4.runtime.tree.ParseTreeVisitor<%0 extends java.lang.Object>
meth public abstract {org.antlr.v4.runtime.tree.ParseTreeVisitor%0} visit(org.antlr.v4.runtime.tree.ParseTree)
meth public abstract {org.antlr.v4.runtime.tree.ParseTreeVisitor%0} visitChildren(org.antlr.v4.runtime.tree.RuleNode)
meth public abstract {org.antlr.v4.runtime.tree.ParseTreeVisitor%0} visitErrorNode(org.antlr.v4.runtime.tree.ErrorNode)
meth public abstract {org.antlr.v4.runtime.tree.ParseTreeVisitor%0} visitTerminal(org.antlr.v4.runtime.tree.TerminalNode)

CLSS public org.antlr.v4.runtime.tree.ParseTreeWalker
cons public init()
fld public final static org.antlr.v4.runtime.tree.ParseTreeWalker DEFAULT
meth protected void enterRule(org.antlr.v4.runtime.tree.ParseTreeListener,org.antlr.v4.runtime.tree.RuleNode)
meth protected void exitRule(org.antlr.v4.runtime.tree.ParseTreeListener,org.antlr.v4.runtime.tree.RuleNode)
meth public void walk(org.antlr.v4.runtime.tree.ParseTreeListener,org.antlr.v4.runtime.tree.ParseTree)
supr java.lang.Object

CLSS public abstract interface org.antlr.v4.runtime.tree.RuleNode
intf org.antlr.v4.runtime.tree.ParseTree
meth public abstract org.antlr.v4.runtime.RuleContext getRuleContext()

CLSS public abstract interface org.antlr.v4.runtime.tree.SyntaxTree
intf org.antlr.v4.runtime.tree.Tree
meth public abstract org.antlr.v4.runtime.misc.Interval getSourceInterval()

CLSS public abstract interface org.antlr.v4.runtime.tree.TerminalNode
intf org.antlr.v4.runtime.tree.ParseTree
meth public abstract org.antlr.v4.runtime.Token getSymbol()

CLSS public org.antlr.v4.runtime.tree.TerminalNodeImpl
cons public init(org.antlr.v4.runtime.Token)
fld public org.antlr.v4.runtime.Token symbol
fld public org.antlr.v4.runtime.tree.ParseTree parent
intf org.antlr.v4.runtime.tree.TerminalNode
meth public <%0 extends java.lang.Object> {%%0} accept(org.antlr.v4.runtime.tree.ParseTreeVisitor<? extends {%%0}>)
meth public int getChildCount()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public java.lang.String toStringTree()
meth public java.lang.String toStringTree(org.antlr.v4.runtime.Parser)
meth public org.antlr.v4.runtime.Token getPayload()
meth public org.antlr.v4.runtime.Token getSymbol()
meth public org.antlr.v4.runtime.misc.Interval getSourceInterval()
meth public org.antlr.v4.runtime.tree.ParseTree getChild(int)
meth public org.antlr.v4.runtime.tree.ParseTree getParent()
meth public void setParent(org.antlr.v4.runtime.RuleContext)
supr java.lang.Object

CLSS public abstract interface org.antlr.v4.runtime.tree.Tree
meth public abstract int getChildCount()
meth public abstract java.lang.Object getPayload()
meth public abstract java.lang.String toStringTree()
meth public abstract org.antlr.v4.runtime.tree.Tree getChild(int)
meth public abstract org.antlr.v4.runtime.tree.Tree getParent()

CLSS public org.antlr.v4.runtime.tree.Trees
meth public static boolean isAncestorOf(org.antlr.v4.runtime.tree.Tree,org.antlr.v4.runtime.tree.Tree)
meth public static java.lang.String getNodeText(org.antlr.v4.runtime.tree.Tree,java.util.List<java.lang.String>)
meth public static java.lang.String getNodeText(org.antlr.v4.runtime.tree.Tree,org.antlr.v4.runtime.Parser)
meth public static java.lang.String toStringTree(org.antlr.v4.runtime.tree.Tree)
meth public static java.lang.String toStringTree(org.antlr.v4.runtime.tree.Tree,java.util.List<java.lang.String>)
meth public static java.lang.String toStringTree(org.antlr.v4.runtime.tree.Tree,org.antlr.v4.runtime.Parser)
meth public static java.util.Collection<org.antlr.v4.runtime.tree.ParseTree> findAllRuleNodes(org.antlr.v4.runtime.tree.ParseTree,int)
meth public static java.util.Collection<org.antlr.v4.runtime.tree.ParseTree> findAllTokenNodes(org.antlr.v4.runtime.tree.ParseTree,int)
meth public static java.util.List<? extends org.antlr.v4.runtime.tree.Tree> getAncestors(org.antlr.v4.runtime.tree.Tree)
meth public static java.util.List<org.antlr.v4.runtime.tree.ParseTree> descendants(org.antlr.v4.runtime.tree.ParseTree)
 anno 0 java.lang.Deprecated()
meth public static java.util.List<org.antlr.v4.runtime.tree.ParseTree> findAllNodes(org.antlr.v4.runtime.tree.ParseTree,int,boolean)
meth public static java.util.List<org.antlr.v4.runtime.tree.ParseTree> getDescendants(org.antlr.v4.runtime.tree.ParseTree)
meth public static java.util.List<org.antlr.v4.runtime.tree.Tree> getChildren(org.antlr.v4.runtime.tree.Tree)
meth public static org.antlr.v4.runtime.ParserRuleContext getRootOfSubtreeEnclosingRegion(org.antlr.v4.runtime.tree.ParseTree,int,int)
meth public static org.antlr.v4.runtime.tree.Tree findNodeSuchThat(org.antlr.v4.runtime.tree.Tree,org.antlr.v4.runtime.misc.Predicate<org.antlr.v4.runtime.tree.Tree>)
meth public static void _findAllNodes(org.antlr.v4.runtime.tree.ParseTree,int,boolean,java.util.List<? super org.antlr.v4.runtime.tree.ParseTree>)
meth public static void stripChildrenOutOfRange(org.antlr.v4.runtime.ParserRuleContext,org.antlr.v4.runtime.ParserRuleContext,int,int)
supr java.lang.Object

