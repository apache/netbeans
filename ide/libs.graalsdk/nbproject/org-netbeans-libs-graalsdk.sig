#Signature file v4.1
#Version 1.2

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

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
hfds serialVersionUID

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
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.collections.EconomicMap<{%%0},{%%1}> wrapMap(java.util.Map<{%%0},{%%1}>)
meth public void putAll(org.graalvm.collections.EconomicMap<{org.graalvm.collections.EconomicMap%0},{org.graalvm.collections.EconomicMap%1}>)
meth public void putAll(org.graalvm.collections.UnmodifiableEconomicMap<? extends {org.graalvm.collections.EconomicMap%0},? extends {org.graalvm.collections.EconomicMap%1}>)

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

CLSS public abstract interface org.graalvm.collections.MapCursor<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf org.graalvm.collections.UnmodifiableMapCursor<{org.graalvm.collections.MapCursor%0},{org.graalvm.collections.MapCursor%1}>
meth public abstract void remove()

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

CLSS public abstract interface org.graalvm.collections.UnmodifiableEconomicMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract boolean containsKey({org.graalvm.collections.UnmodifiableEconomicMap%0})
meth public abstract boolean isEmpty()
meth public abstract int size()
meth public abstract java.lang.Iterable<{org.graalvm.collections.UnmodifiableEconomicMap%0}> getKeys()
meth public abstract java.lang.Iterable<{org.graalvm.collections.UnmodifiableEconomicMap%1}> getValues()
meth public abstract org.graalvm.collections.UnmodifiableMapCursor<{org.graalvm.collections.UnmodifiableEconomicMap%0},{org.graalvm.collections.UnmodifiableEconomicMap%1}> getEntries()
meth public abstract {org.graalvm.collections.UnmodifiableEconomicMap%1} get({org.graalvm.collections.UnmodifiableEconomicMap%0})
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

CLSS public final !enum org.graalvm.options.OptionCategory
fld public final static org.graalvm.options.OptionCategory DEBUG
 anno 0 java.lang.Deprecated()
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
meth public int hashCode()
meth public java.lang.String getHelp()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public org.graalvm.options.OptionCategory getCategory()
meth public org.graalvm.options.OptionKey<?> getKey()
meth public org.graalvm.options.OptionStability getStability()
meth public static <%0 extends java.lang.Object> org.graalvm.options.OptionDescriptor$Builder newBuilder(org.graalvm.options.OptionKey<{%%0}>,java.lang.String)
supr java.lang.Object
hfds EMPTY,deprecated,help,key,kind,name,stability

CLSS public final org.graalvm.options.OptionDescriptor$Builder
 outer org.graalvm.options.OptionDescriptor
meth public org.graalvm.options.OptionDescriptor build()
meth public org.graalvm.options.OptionDescriptor$Builder category(org.graalvm.options.OptionCategory)
meth public org.graalvm.options.OptionDescriptor$Builder deprecated(boolean)
meth public org.graalvm.options.OptionDescriptor$Builder help(java.lang.String)
meth public org.graalvm.options.OptionDescriptor$Builder stability(org.graalvm.options.OptionStability)
supr java.lang.Object
hfds category,deprecated,help,key,name,stability

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
meth public {org.graalvm.options.OptionKey%0} getDefaultValue()
meth public {org.graalvm.options.OptionKey%0} getValue(org.graalvm.options.OptionValues)
supr java.lang.Object
hfds defaultValue,type

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
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,{org.graalvm.options.OptionType%0},java.util.function.Function<java.lang.String,{org.graalvm.options.OptionType%0}>,java.util.function.Consumer<{org.graalvm.options.OptionType%0}>)
 anno 0 java.lang.Deprecated()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Object> org.graalvm.options.OptionType<{%%0}> defaultType(java.lang.Class<{%%0}>)
meth public static <%0 extends java.lang.Object> org.graalvm.options.OptionType<{%%0}> defaultType({%%0})
meth public void validate({org.graalvm.options.OptionType%0})
meth public {org.graalvm.options.OptionType%0} convert(java.lang.String)
meth public {org.graalvm.options.OptionType%0} getDefaultValue()
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds DEFAULTTYPES,name,stringConverter,validator

CLSS public abstract interface org.graalvm.options.OptionValues
meth public abstract <%0 extends java.lang.Object> void set(org.graalvm.options.OptionKey<{%%0}>,{%%0})
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
meth public static org.graalvm.polyglot.Context getCurrent()
meth public void close()
meth public void close(boolean)
meth public void enter()
meth public void leave()
supr java.lang.Object
hfds ALL_HOST_CLASSES,EMPTY,NO_HOST_CLASSES,UNSET_HOST_LOOKUP,impl

CLSS public final org.graalvm.polyglot.Context$Builder
 outer org.graalvm.polyglot.Context
meth public org.graalvm.polyglot.Context build()
meth public org.graalvm.polyglot.Context$Builder allowAllAccess(boolean)
meth public org.graalvm.polyglot.Context$Builder allowCreateThread(boolean)
meth public org.graalvm.polyglot.Context$Builder allowExperimentalOptions(boolean)
meth public org.graalvm.polyglot.Context$Builder allowHostAccess(boolean)
 anno 0 java.lang.Deprecated()
meth public org.graalvm.polyglot.Context$Builder allowHostAccess(org.graalvm.polyglot.HostAccess)
meth public org.graalvm.polyglot.Context$Builder allowHostClassLoading(boolean)
meth public org.graalvm.polyglot.Context$Builder allowHostClassLookup(java.util.function.Predicate<java.lang.String>)
meth public org.graalvm.polyglot.Context$Builder allowIO(boolean)
meth public org.graalvm.polyglot.Context$Builder allowNativeAccess(boolean)
meth public org.graalvm.polyglot.Context$Builder allowPolyglotAccess(org.graalvm.polyglot.PolyglotAccess)
meth public org.graalvm.polyglot.Context$Builder arguments(java.lang.String,java.lang.String[])
meth public org.graalvm.polyglot.Context$Builder engine(org.graalvm.polyglot.Engine)
meth public org.graalvm.polyglot.Context$Builder err(java.io.OutputStream)
meth public org.graalvm.polyglot.Context$Builder fileSystem(org.graalvm.polyglot.io.FileSystem)
meth public org.graalvm.polyglot.Context$Builder hostClassFilter(java.util.function.Predicate<java.lang.String>)
 anno 0 java.lang.Deprecated()
meth public org.graalvm.polyglot.Context$Builder in(java.io.InputStream)
meth public org.graalvm.polyglot.Context$Builder logHandler(java.io.OutputStream)
meth public org.graalvm.polyglot.Context$Builder logHandler(java.util.logging.Handler)
meth public org.graalvm.polyglot.Context$Builder option(java.lang.String,java.lang.String)
meth public org.graalvm.polyglot.Context$Builder options(java.util.Map<java.lang.String,java.lang.String>)
meth public org.graalvm.polyglot.Context$Builder out(java.io.OutputStream)
meth public org.graalvm.polyglot.Context$Builder serverTransport(org.graalvm.polyglot.io.MessageTransport)
supr java.lang.Object
hfds allowAllAccess,allowCreateThread,allowExperimentalOptions,allowHostAccess,allowHostClassLoading,allowIO,allowNativeAccess,arguments,customFileSystem,customLogHandler,err,hostAccess,hostClassFilter,in,messageTransport,onlyLanguages,options,out,polylgotAccess,sharedEngine

CLSS public final org.graalvm.polyglot.Engine
innr public final Builder
intf java.lang.AutoCloseable
meth public java.lang.String getImplementationName()
meth public java.lang.String getVersion()
meth public java.util.Map<java.lang.String,org.graalvm.polyglot.Instrument> getInstruments()
meth public java.util.Map<java.lang.String,org.graalvm.polyglot.Language> getLanguages()
meth public org.graalvm.options.OptionDescriptors getOptions()
meth public static java.nio.file.Path findHome()
meth public static org.graalvm.polyglot.Engine create()
meth public static org.graalvm.polyglot.Engine$Builder newBuilder()
meth public void close()
meth public void close(boolean)
supr java.lang.Object
hfds EMPTY,JDK8_OR_EARLIER,impl
hcls APIAccessImpl,ImplHolder,PolyglotInvalid

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
meth public org.graalvm.polyglot.Engine$Builder serverTransport(org.graalvm.polyglot.io.MessageTransport)
meth public org.graalvm.polyglot.Engine$Builder useSystemProperties(boolean)
supr java.lang.Object
hfds allowExperimentalOptions,boundEngine,customLogHandler,err,in,messageTransport,options,out,useSystemProperties

CLSS public final org.graalvm.polyglot.HostAccess
fld public final static org.graalvm.polyglot.HostAccess ALL
fld public final static org.graalvm.polyglot.HostAccess EXPLICIT
fld public final static org.graalvm.polyglot.HostAccess NONE
innr public abstract interface static !annotation Export
innr public abstract interface static !annotation Implementable
innr public final Builder
meth public java.lang.String toString()
meth public static org.graalvm.polyglot.HostAccess$Builder newBuilder()
meth public static org.graalvm.polyglot.HostAccess$Builder newBuilder(org.graalvm.polyglot.HostAccess)
supr java.lang.Object
hfds EMPTY,accessAnnotations,allowAllImplementations,allowArrayAccess,allowListAccess,allowPublic,excludeTypes,impl,implementableAnnotations,implementableTypes,members,name,targetMappings

CLSS public final org.graalvm.polyglot.HostAccess$Builder
 outer org.graalvm.polyglot.HostAccess
meth public <%0 extends java.lang.Object, %1 extends java.lang.Object> org.graalvm.polyglot.HostAccess$Builder targetTypeMapping(java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,java.util.function.Predicate<{%%0}>,java.util.function.Function<{%%0},{%%1}>)
meth public org.graalvm.polyglot.HostAccess build()
meth public org.graalvm.polyglot.HostAccess$Builder allowAccess(java.lang.reflect.Executable)
meth public org.graalvm.polyglot.HostAccess$Builder allowAccess(java.lang.reflect.Field)
meth public org.graalvm.polyglot.HostAccess$Builder allowAccessAnnotatedBy(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public org.graalvm.polyglot.HostAccess$Builder allowAllImplementations(boolean)
meth public org.graalvm.polyglot.HostAccess$Builder allowArrayAccess(boolean)
meth public org.graalvm.polyglot.HostAccess$Builder allowImplementations(java.lang.Class<?>)
meth public org.graalvm.polyglot.HostAccess$Builder allowImplementationsAnnotatedBy(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public org.graalvm.polyglot.HostAccess$Builder allowListAccess(boolean)
meth public org.graalvm.polyglot.HostAccess$Builder allowPublicAccess(boolean)
meth public org.graalvm.polyglot.HostAccess$Builder denyAccess(java.lang.Class<?>)
meth public org.graalvm.polyglot.HostAccess$Builder denyAccess(java.lang.Class<?>,boolean)
supr java.lang.Object
hfds accessAnnotations,allowAllImplementations,allowArrayAccess,allowListAccess,allowPublic,excludeTypes,implementableTypes,implementationAnnotations,members,name,targetMappings

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

CLSS public final org.graalvm.polyglot.Instrument
meth public <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public java.lang.String getVersion()
meth public org.graalvm.options.OptionDescriptors getOptions()
supr java.lang.Object
hfds impl

CLSS public final org.graalvm.polyglot.Language
meth public boolean isInteractive()
meth public java.lang.String getDefaultMimeType()
meth public java.lang.String getId()
meth public java.lang.String getImplementationName()
meth public java.lang.String getName()
meth public java.lang.String getVersion()
meth public java.util.Set<java.lang.String> getMimeTypes()
meth public org.graalvm.options.OptionDescriptors getOptions()
supr java.lang.Object
hfds impl

CLSS public final org.graalvm.polyglot.PolyglotAccess
fld public final static org.graalvm.polyglot.PolyglotAccess ALL
fld public final static org.graalvm.polyglot.PolyglotAccess NONE
supr java.lang.Object

CLSS public final org.graalvm.polyglot.PolyglotException
innr public final StackFrame
meth public boolean equals(java.lang.Object)
meth public boolean isCancelled()
meth public boolean isExit()
meth public boolean isGuestException()
meth public boolean isHostException()
meth public boolean isIncompleteSource()
meth public boolean isInternalError()
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
hfds impl

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
 anno 0 java.lang.Deprecated()
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
hfds EMPTY,IMPL,impl,language

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
 anno 0 java.lang.Deprecated()
meth public java.lang.String toString()
meth public org.graalvm.polyglot.Source getSource()
supr java.lang.Object
hfds IMPL,impl,source

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
meth public boolean fitsInByte()
meth public boolean fitsInDouble()
meth public boolean fitsInFloat()
meth public boolean fitsInInt()
meth public boolean fitsInLong()
meth public boolean fitsInShort()
meth public boolean hasArrayElements()
meth public boolean hasMember(java.lang.String)
meth public boolean hasMembers()
meth public boolean isBoolean()
meth public boolean isHostObject()
meth public boolean isNativePointer()
meth public boolean isNull()
meth public boolean isNumber()
meth public boolean isProxyObject()
meth public boolean isString()
meth public boolean removeArrayElement(long)
meth public boolean removeMember(java.lang.String)
meth public byte asByte()
meth public double asDouble()
meth public float asFloat()
meth public int asInt()
meth public java.lang.String asString()
meth public java.lang.String toString()
meth public java.util.Set<java.lang.String> getMemberKeys()
meth public long asLong()
meth public long asNativePointer()
meth public long getArraySize()
meth public org.graalvm.polyglot.SourceSection getSourceLocation()
meth public org.graalvm.polyglot.Value getArrayElement(long)
meth public org.graalvm.polyglot.Value getMember(java.lang.String)
meth public org.graalvm.polyglot.Value getMetaObject()
meth public short asShort()
meth public static org.graalvm.polyglot.Value asValue(java.lang.Object)
meth public void putMember(java.lang.String,java.lang.Object)
meth public void setArrayElement(long,java.lang.Object)
supr java.lang.Object
hfds impl,receiver

CLSS public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init()
innr public abstract static APIAccess
innr public abstract static AbstractContextImpl
innr public abstract static AbstractEngineImpl
innr public abstract static AbstractExceptionImpl
innr public abstract static AbstractExecutionListenerImpl
innr public abstract static AbstractInstrumentImpl
innr public abstract static AbstractLanguageImpl
innr public abstract static AbstractSourceImpl
innr public abstract static AbstractSourceSectionImpl
innr public abstract static AbstractStackFrameImpl
innr public abstract static AbstractValueImpl
innr public abstract static MonitoringAccess
meth protected void initialize()
meth public abstract <%0 extends java.lang.Object, %1 extends java.lang.Object> java.lang.Object newTargetTypeMapping(java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,java.util.function.Predicate<{%%0}>,java.util.function.Function<{%%0},{%%1}>)
meth public abstract java.lang.Class<?> loadLanguageClass(java.lang.String)
meth public abstract java.nio.file.Path findHome()
meth public abstract java.util.Collection<org.graalvm.polyglot.Engine> findActiveEngines()
meth public abstract org.graalvm.polyglot.Engine buildEngine(java.io.OutputStream,java.io.OutputStream,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>,long,java.util.concurrent.TimeUnit,boolean,long,boolean,boolean,boolean,org.graalvm.polyglot.io.MessageTransport,java.lang.Object,org.graalvm.polyglot.HostAccess)
meth public abstract org.graalvm.polyglot.Value asValue(java.lang.Object)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractExecutionListenerImpl getExecutionListenerImpl()
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractSourceImpl getSourceImpl()
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractSourceSectionImpl getSourceSectionImpl()
meth public abstract void preInitializeEngine()
meth public abstract void resetPreInitializedEngine()
meth public final void setConstructors(org.graalvm.polyglot.impl.AbstractPolyglotImpl$APIAccess)
meth public final void setMonitoring(org.graalvm.polyglot.impl.AbstractPolyglotImpl$MonitoringAccess)
meth public org.graalvm.polyglot.Context getCurrentContext()
meth public org.graalvm.polyglot.impl.AbstractPolyglotImpl$APIAccess getAPIAccess()
meth public org.graalvm.polyglot.impl.AbstractPolyglotImpl$MonitoringAccess getMonitoring()
supr java.lang.Object
hfds api,monitoring

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$APIAccess
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init()
meth public abstract boolean allowsAccess(org.graalvm.polyglot.HostAccess,java.lang.reflect.AnnotatedElement)
meth public abstract boolean allowsImplementation(org.graalvm.polyglot.HostAccess,java.lang.Class<?>)
meth public abstract boolean isArrayAccessible(org.graalvm.polyglot.HostAccess)
meth public abstract boolean isListAccessible(org.graalvm.polyglot.HostAccess)
meth public abstract boolean useContextClassLoader()
meth public abstract java.lang.Object getHostAccessImpl(org.graalvm.polyglot.HostAccess)
meth public abstract java.lang.Object getReceiver(org.graalvm.polyglot.Value)
meth public abstract java.util.List<java.lang.Object> getTargetMappings(org.graalvm.polyglot.HostAccess)
meth public abstract org.graalvm.polyglot.Context newContext(org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractContextImpl)
meth public abstract org.graalvm.polyglot.Engine newEngine(org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractEngineImpl)
meth public abstract org.graalvm.polyglot.Instrument newInstrument(org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractInstrumentImpl)
meth public abstract org.graalvm.polyglot.Language newLanguage(org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractLanguageImpl)
meth public abstract org.graalvm.polyglot.PolyglotException newLanguageException(java.lang.String,org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractExceptionImpl)
meth public abstract org.graalvm.polyglot.PolyglotException$StackFrame newPolyglotStackTraceElement(org.graalvm.polyglot.PolyglotException,org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractStackFrameImpl)
meth public abstract org.graalvm.polyglot.Source newSource(java.lang.String,java.lang.Object)
meth public abstract org.graalvm.polyglot.SourceSection newSourceSection(org.graalvm.polyglot.Source,java.lang.Object)
meth public abstract org.graalvm.polyglot.Value newValue(java.lang.Object,org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractValueImpl)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractEngineImpl getImpl(org.graalvm.polyglot.Engine)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractExceptionImpl getImpl(org.graalvm.polyglot.PolyglotException)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractInstrumentImpl getImpl(org.graalvm.polyglot.Instrument)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractLanguageImpl getImpl(org.graalvm.polyglot.Language)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractStackFrameImpl getImpl(org.graalvm.polyglot.PolyglotException$StackFrame)
meth public abstract org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractValueImpl getImpl(org.graalvm.polyglot.Value)
meth public abstract void setHostAccessImpl(org.graalvm.polyglot.HostAccess,java.lang.Object)
supr java.lang.Object

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractContextImpl
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract boolean initializeLanguage(java.lang.String)
meth public abstract org.graalvm.polyglot.Engine getEngineImpl(org.graalvm.polyglot.Context)
meth public abstract org.graalvm.polyglot.Value asValue(java.lang.Object)
meth public abstract org.graalvm.polyglot.Value eval(java.lang.String,java.lang.Object)
meth public abstract org.graalvm.polyglot.Value getBindings(java.lang.String)
meth public abstract org.graalvm.polyglot.Value getPolyglotBindings()
meth public abstract void close(org.graalvm.polyglot.Context,boolean)
meth public abstract void explicitEnter(org.graalvm.polyglot.Context)
meth public abstract void explicitLeave(org.graalvm.polyglot.Context)
supr java.lang.Object

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractEngineImpl
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract java.lang.String getImplementationName()
meth public abstract java.lang.String getVersion()
meth public abstract java.util.Map<java.lang.String,org.graalvm.polyglot.Instrument> getInstruments()
meth public abstract java.util.Map<java.lang.String,org.graalvm.polyglot.Language> getLanguages()
meth public abstract org.graalvm.options.OptionDescriptors getOptions()
meth public abstract org.graalvm.polyglot.Context createContext(java.io.OutputStream,java.io.OutputStream,java.io.InputStream,boolean,org.graalvm.polyglot.HostAccess,org.graalvm.polyglot.PolyglotAccess,boolean,boolean,boolean,boolean,boolean,java.util.function.Predicate<java.lang.String>,java.util.Map<java.lang.String,java.lang.String>,java.util.Map<java.lang.String,java.lang.String[]>,java.lang.String[],org.graalvm.polyglot.io.FileSystem,java.lang.Object)
meth public abstract org.graalvm.polyglot.Instrument requirePublicInstrument(java.lang.String)
meth public abstract org.graalvm.polyglot.Language requirePublicLanguage(java.lang.String)
meth public abstract void close(org.graalvm.polyglot.Engine,boolean)
supr java.lang.Object

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractExceptionImpl
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract boolean isCancelled()
meth public abstract boolean isExit()
meth public abstract boolean isHostException()
meth public abstract boolean isIncompleteSource()
meth public abstract boolean isInternalError()
meth public abstract boolean isSyntaxError()
meth public abstract int getExitStatus()
meth public abstract java.lang.Iterable<org.graalvm.polyglot.PolyglotException$StackFrame> getPolyglotStackTrace()
meth public abstract java.lang.StackTraceElement[] getStackTrace()
meth public abstract java.lang.String getMessage()
meth public abstract java.lang.Throwable asHostException()
meth public abstract org.graalvm.polyglot.SourceSection getSourceLocation()
meth public abstract org.graalvm.polyglot.Value getGuestObject()
meth public abstract void onCreate(org.graalvm.polyglot.PolyglotException)
meth public abstract void printStackTrace(java.io.PrintStream)
meth public abstract void printStackTrace(java.io.PrintWriter)
supr java.lang.Object

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractExecutionListenerImpl
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract boolean isExpression(java.lang.Object)
meth public abstract boolean isRoot(java.lang.Object)
meth public abstract boolean isStatement(java.lang.Object)
meth public abstract java.lang.Object attachExecutionListener(org.graalvm.polyglot.Engine,java.util.function.Consumer<org.graalvm.polyglot.management.ExecutionEvent>,java.util.function.Consumer<org.graalvm.polyglot.management.ExecutionEvent>,boolean,boolean,boolean,java.util.function.Predicate<org.graalvm.polyglot.Source>,java.util.function.Predicate<java.lang.String>,boolean,boolean,boolean)
meth public abstract java.lang.String getRootName(java.lang.Object)
meth public abstract java.util.List<org.graalvm.polyglot.Value> getInputValues(java.lang.Object)
meth public abstract org.graalvm.polyglot.PolyglotException getException(java.lang.Object)
meth public abstract org.graalvm.polyglot.SourceSection getLocation(java.lang.Object)
meth public abstract org.graalvm.polyglot.Value getReturnValue(java.lang.Object)
meth public abstract void closeExecutionListener(java.lang.Object)
supr java.lang.Object

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractInstrumentImpl
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getVersion()
meth public abstract org.graalvm.options.OptionDescriptors getOptions()
supr java.lang.Object

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractLanguageImpl
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract boolean isInteractive()
meth public abstract java.lang.String getDefaultMimeType()
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getImplementationName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getVersion()
meth public abstract java.util.Set<java.lang.String> getMimeTypes()
meth public abstract org.graalvm.options.OptionDescriptors getOptions()
supr java.lang.Object

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractSourceImpl
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
fld protected final org.graalvm.polyglot.impl.AbstractPolyglotImpl engineImpl
meth public abstract boolean equals(java.lang.Object,java.lang.Object)
meth public abstract boolean hasBytes(java.lang.Object)
meth public abstract boolean hasCharacters(java.lang.Object)
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
meth public abstract java.lang.CharSequence getCode(java.lang.Object)
meth public abstract java.lang.CharSequence getCode(java.lang.Object,int)
meth public abstract java.lang.String findLanguage(java.io.File) throws java.io.IOException
meth public abstract java.lang.String findLanguage(java.lang.String)
meth public abstract java.lang.String findLanguage(java.net.URL) throws java.io.IOException
meth public abstract java.lang.String findMimeType(java.io.File) throws java.io.IOException
meth public abstract java.lang.String findMimeType(java.net.URL) throws java.io.IOException
meth public abstract java.lang.String getMimeType(java.lang.Object)
meth public abstract java.lang.String getName(java.lang.Object)
meth public abstract java.lang.String getPath(java.lang.Object)
meth public abstract java.lang.String toString(java.lang.Object)
meth public abstract java.net.URI getURI(java.lang.Object)
meth public abstract java.net.URL getURL(java.lang.Object)
meth public abstract org.graalvm.polyglot.Source build(java.lang.String,java.lang.Object,java.net.URI,java.lang.String,java.lang.String,java.lang.Object,boolean,boolean,boolean,java.nio.charset.Charset) throws java.io.IOException
meth public abstract org.graalvm.polyglot.io.ByteSequence getBytes(java.lang.Object)
supr java.lang.Object

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractSourceSectionImpl
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
supr java.lang.Object

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractStackFrameImpl
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract boolean isHostFrame()
meth public abstract java.lang.StackTraceElement toHostFrame()
meth public abstract java.lang.String getRootName()
meth public abstract java.lang.String toStringImpl(int)
meth public abstract org.graalvm.polyglot.Language getLanguage()
meth public abstract org.graalvm.polyglot.SourceSection getSourceLocation()
supr java.lang.Object

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$AbstractValueImpl
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init(org.graalvm.polyglot.impl.AbstractPolyglotImpl)
meth public abstract <%0 extends java.lang.Object> {%%0} as(java.lang.Object,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} as(java.lang.Object,org.graalvm.polyglot.TypeLiteral<{%%0}>)
meth public abstract boolean asBoolean(java.lang.Object)
meth public abstract boolean removeArrayElement(java.lang.Object,long)
meth public abstract boolean removeMember(java.lang.Object,java.lang.String)
meth public abstract byte asByte(java.lang.Object)
meth public abstract double asDouble(java.lang.Object)
meth public abstract float asFloat(java.lang.Object)
meth public abstract int asInt(java.lang.Object)
meth public abstract java.lang.Object asHostObject(java.lang.Object)
meth public abstract java.lang.Object asProxyObject(java.lang.Object)
meth public abstract java.lang.String asString(java.lang.Object)
meth public abstract java.lang.String toString(java.lang.Object)
meth public abstract long asLong(java.lang.Object)
meth public abstract long asNativePointer(java.lang.Object)
meth public abstract long getArraySize(java.lang.Object)
meth public abstract org.graalvm.polyglot.SourceSection getSourceLocation(java.lang.Object)
meth public abstract org.graalvm.polyglot.Value execute(java.lang.Object)
meth public abstract org.graalvm.polyglot.Value execute(java.lang.Object,java.lang.Object[])
meth public abstract org.graalvm.polyglot.Value getArrayElement(java.lang.Object,long)
meth public abstract org.graalvm.polyglot.Value getMember(java.lang.Object,java.lang.String)
meth public abstract org.graalvm.polyglot.Value getMetaObject(java.lang.Object)
meth public abstract org.graalvm.polyglot.Value invoke(java.lang.Object,java.lang.String)
meth public abstract org.graalvm.polyglot.Value invoke(java.lang.Object,java.lang.String,java.lang.Object[])
meth public abstract org.graalvm.polyglot.Value newInstance(java.lang.Object,java.lang.Object[])
meth public abstract short asShort(java.lang.Object)
meth public abstract void executeVoid(java.lang.Object)
meth public abstract void executeVoid(java.lang.Object,java.lang.Object[])
meth public abstract void putMember(java.lang.Object,java.lang.String,java.lang.Object)
meth public abstract void setArrayElement(java.lang.Object,long,java.lang.Object)
meth public boolean canExecute(java.lang.Object)
meth public boolean canInstantiate(java.lang.Object)
meth public boolean canInvoke(java.lang.String,java.lang.Object)
meth public boolean fitsInByte(java.lang.Object)
meth public boolean fitsInDouble(java.lang.Object)
meth public boolean fitsInFloat(java.lang.Object)
meth public boolean fitsInInt(java.lang.Object)
meth public boolean fitsInLong(java.lang.Object)
meth public boolean fitsInShort(java.lang.Object)
meth public boolean hasArrayElements(java.lang.Object)
meth public boolean hasMember(java.lang.Object,java.lang.String)
meth public boolean hasMembers(java.lang.Object)
meth public boolean isBoolean(java.lang.Object)
meth public boolean isHostObject(java.lang.Object)
meth public boolean isNativePointer(java.lang.Object)
meth public boolean isNull(java.lang.Object)
meth public boolean isNumber(java.lang.Object)
meth public boolean isProxyObject(java.lang.Object)
meth public boolean isString(java.lang.Object)
meth public java.util.Set<java.lang.String> getMemberKeys(java.lang.Object)
supr java.lang.Object

CLSS public abstract static org.graalvm.polyglot.impl.AbstractPolyglotImpl$MonitoringAccess
 outer org.graalvm.polyglot.impl.AbstractPolyglotImpl
cons protected init()
meth public abstract org.graalvm.polyglot.management.ExecutionEvent newExecutionEvent(java.lang.Object)
supr java.lang.Object

CLSS public abstract interface org.graalvm.polyglot.io.ByteSequence
meth public abstract byte byteAt(int)
meth public abstract int length()
meth public byte[] toByteArray()
meth public java.util.stream.IntStream bytes()
meth public org.graalvm.polyglot.io.ByteSequence subSequence(int,int)
meth public static org.graalvm.polyglot.io.ByteSequence create(byte[])

CLSS public abstract interface org.graalvm.polyglot.io.FileSystem
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
meth public java.lang.String getSeparator()
meth public java.nio.charset.Charset getEncoding(java.nio.file.Path)
meth public java.nio.file.Path readSymbolicLink(java.nio.file.Path) throws java.io.IOException
meth public void createLink(java.nio.file.Path,java.nio.file.Path) throws java.io.IOException
meth public void setCurrentWorkingDirectory(java.nio.file.Path)

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
hfds impl

CLSS public final org.graalvm.polyglot.management.ExecutionListener
innr public final Builder
intf java.lang.AutoCloseable
meth public static org.graalvm.polyglot.management.ExecutionListener$Builder newBuilder()
meth public void close()
supr java.lang.Object
hfds EMPTY,IMPL,impl
hcls MonitoringAccessImpl

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
intf org.graalvm.polyglot.proxy.Proxy
meth public !varargs static org.graalvm.polyglot.proxy.ProxyArray fromArray(java.lang.Object[])
meth public abstract java.lang.Object get(long)
meth public abstract long getSize()
meth public abstract void set(long,org.graalvm.polyglot.Value)
meth public boolean remove(long)
meth public static org.graalvm.polyglot.proxy.ProxyArray fromList(java.util.List<java.lang.Object>)

CLSS public abstract interface org.graalvm.polyglot.proxy.ProxyExecutable
 anno 0 java.lang.FunctionalInterface()
intf org.graalvm.polyglot.proxy.Proxy
meth public abstract !varargs java.lang.Object execute(org.graalvm.polyglot.Value[])

CLSS public abstract interface org.graalvm.polyglot.proxy.ProxyInstantiable
 anno 0 java.lang.FunctionalInterface()
intf org.graalvm.polyglot.proxy.Proxy
meth public abstract !varargs java.lang.Object newInstance(org.graalvm.polyglot.Value[])

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

CLSS public final org.netbeans.libs.graalsdk.GraalSDK
supr java.lang.Object

CLSS abstract interface org.netbeans.libs.graalsdk.package-info

