#Signature file v4.1
#Version 2.78

CLSS public abstract interface !annotation com.google.common.annotations.GwtCompatible
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=false, boolean serializable=false)
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean emulated()
meth public abstract !hasdefault boolean serializable()

CLSS public abstract interface !annotation com.google.common.annotations.GwtIncompatible
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=false, boolean serializable=false)
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, CONSTRUCTOR, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface !annotation com.google.common.annotations.J2ktIncompatible
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=false, boolean serializable=false)
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, CONSTRUCTOR, FIELD])
intf java.lang.annotation.Annotation

CLSS public final com.google.common.base.Ascii
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=false, boolean serializable=false)
fld public final static byte ACK = 6
fld public final static byte BEL = 7
fld public final static byte BS = 8
fld public final static byte CAN = 24
fld public final static byte CR = 13
fld public final static byte DC1 = 17
fld public final static byte DC2 = 18
fld public final static byte DC3 = 19
fld public final static byte DC4 = 20
fld public final static byte DEL = 127
fld public final static byte DLE = 16
fld public final static byte EM = 25
fld public final static byte ENQ = 5
fld public final static byte EOT = 4
fld public final static byte ESC = 27
fld public final static byte ETB = 23
fld public final static byte ETX = 3
fld public final static byte FF = 12
fld public final static byte FS = 28
fld public final static byte GS = 29
fld public final static byte HT = 9
fld public final static byte LF = 10
fld public final static byte NAK = 21
fld public final static byte NL = 10
fld public final static byte NUL = 0
fld public final static byte RS = 30
fld public final static byte SI = 15
fld public final static byte SO = 14
fld public final static byte SOH = 1
fld public final static byte SP = 32
fld public final static byte SPACE = 32
fld public final static byte STX = 2
fld public final static byte SUB = 26
fld public final static byte SYN = 22
fld public final static byte US = 31
fld public final static byte VT = 11
fld public final static byte XOFF = 19
fld public final static byte XON = 17
fld public final static char MAX = '\u007f'
fld public final static char MIN = '\u0000'
meth public static boolean equalsIgnoreCase(java.lang.CharSequence,java.lang.CharSequence)
meth public static boolean isLowerCase(char)
meth public static boolean isUpperCase(char)
meth public static char toLowerCase(char)
meth public static char toUpperCase(char)
meth public static java.lang.String toLowerCase(java.lang.CharSequence)
meth public static java.lang.String toLowerCase(java.lang.String)
meth public static java.lang.String toUpperCase(java.lang.CharSequence)
meth public static java.lang.String toUpperCase(java.lang.String)
meth public static java.lang.String truncate(java.lang.CharSequence,int,java.lang.String)
supr java.lang.Object
hfds CASE_MASK

CLSS public abstract !enum com.google.common.base.CaseFormat
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=false, boolean serializable=false)
fld public final static com.google.common.base.CaseFormat LOWER_CAMEL
fld public final static com.google.common.base.CaseFormat LOWER_HYPHEN
fld public final static com.google.common.base.CaseFormat LOWER_UNDERSCORE
fld public final static com.google.common.base.CaseFormat UPPER_CAMEL
fld public final static com.google.common.base.CaseFormat UPPER_UNDERSCORE
meth public com.google.common.base.Converter<java.lang.String,java.lang.String> converterTo(com.google.common.base.CaseFormat)
meth public final java.lang.String to(com.google.common.base.CaseFormat,java.lang.String)
meth public static com.google.common.base.CaseFormat valueOf(java.lang.String)
meth public static com.google.common.base.CaseFormat[] values()
supr java.lang.Enum<com.google.common.base.CaseFormat>
hfds wordBoundary,wordSeparator
hcls StringConverter

CLSS public abstract com.google.common.base.CharMatcher
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=true, boolean serializable=false)
cons protected init()
intf com.google.common.base.Predicate<java.lang.Character>
meth public abstract boolean matches(char)
meth public boolean apply(java.lang.Character)
 anno 0 java.lang.Deprecated()
meth public boolean matchesAllOf(java.lang.CharSequence)
meth public boolean matchesAnyOf(java.lang.CharSequence)
meth public boolean matchesNoneOf(java.lang.CharSequence)
meth public com.google.common.base.CharMatcher and(com.google.common.base.CharMatcher)
meth public com.google.common.base.CharMatcher negate()
meth public com.google.common.base.CharMatcher or(com.google.common.base.CharMatcher)
meth public com.google.common.base.CharMatcher precomputed()
meth public int countIn(java.lang.CharSequence)
meth public int indexIn(java.lang.CharSequence)
meth public int indexIn(java.lang.CharSequence,int)
meth public int lastIndexIn(java.lang.CharSequence)
meth public java.lang.String collapseFrom(java.lang.CharSequence,char)
meth public java.lang.String removeFrom(java.lang.CharSequence)
meth public java.lang.String replaceFrom(java.lang.CharSequence,char)
meth public java.lang.String replaceFrom(java.lang.CharSequence,java.lang.CharSequence)
meth public java.lang.String retainFrom(java.lang.CharSequence)
meth public java.lang.String toString()
meth public java.lang.String trimAndCollapseFrom(java.lang.CharSequence,char)
meth public java.lang.String trimFrom(java.lang.CharSequence)
meth public java.lang.String trimLeadingFrom(java.lang.CharSequence)
meth public java.lang.String trimTrailingFrom(java.lang.CharSequence)
meth public static com.google.common.base.CharMatcher any()
meth public static com.google.common.base.CharMatcher anyOf(java.lang.CharSequence)
meth public static com.google.common.base.CharMatcher ascii()
meth public static com.google.common.base.CharMatcher breakingWhitespace()
meth public static com.google.common.base.CharMatcher digit()
 anno 0 java.lang.Deprecated()
meth public static com.google.common.base.CharMatcher forPredicate(com.google.common.base.Predicate<? super java.lang.Character>)
meth public static com.google.common.base.CharMatcher inRange(char,char)
meth public static com.google.common.base.CharMatcher invisible()
 anno 0 java.lang.Deprecated()
meth public static com.google.common.base.CharMatcher is(char)
meth public static com.google.common.base.CharMatcher isNot(char)
meth public static com.google.common.base.CharMatcher javaDigit()
 anno 0 java.lang.Deprecated()
meth public static com.google.common.base.CharMatcher javaIsoControl()
meth public static com.google.common.base.CharMatcher javaLetter()
 anno 0 java.lang.Deprecated()
meth public static com.google.common.base.CharMatcher javaLetterOrDigit()
 anno 0 java.lang.Deprecated()
meth public static com.google.common.base.CharMatcher javaLowerCase()
 anno 0 java.lang.Deprecated()
meth public static com.google.common.base.CharMatcher javaUpperCase()
 anno 0 java.lang.Deprecated()
meth public static com.google.common.base.CharMatcher none()
meth public static com.google.common.base.CharMatcher noneOf(java.lang.CharSequence)
meth public static com.google.common.base.CharMatcher singleWidth()
 anno 0 java.lang.Deprecated()
meth public static com.google.common.base.CharMatcher whitespace()
supr java.lang.Object
hfds DISTINCT_CHARS
hcls And,Any,AnyOf,Ascii,BitSetMatcher,BreakingWhitespace,Digit,FastMatcher,ForPredicate,InRange,Invisible,Is,IsEither,IsNot,JavaDigit,JavaIsoControl,JavaLetter,JavaLetterOrDigit,JavaLowerCase,JavaUpperCase,NamedFastMatcher,Negated,NegatedFastMatcher,None,Or,RangesMatcher,SingleWidth,Whitespace

CLSS public final com.google.common.base.Charsets
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=true, boolean serializable=false)
fld public final static java.nio.charset.Charset ISO_8859_1
fld public final static java.nio.charset.Charset US_ASCII
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
fld public final static java.nio.charset.Charset UTF_16
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
fld public final static java.nio.charset.Charset UTF_16BE
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
fld public final static java.nio.charset.Charset UTF_16LE
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
fld public final static java.nio.charset.Charset UTF_8
supr java.lang.Object

CLSS public abstract com.google.common.base.Converter<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=false, boolean serializable=false)
cons protected init()
intf com.google.common.base.Function<{com.google.common.base.Converter%0},{com.google.common.base.Converter%1}>
meth protected abstract {com.google.common.base.Converter%0} doBackward({com.google.common.base.Converter%1})
meth protected abstract {com.google.common.base.Converter%1} doForward({com.google.common.base.Converter%0})
meth public boolean equals(java.lang.Object)
meth public com.google.common.base.Converter<{com.google.common.base.Converter%1},{com.google.common.base.Converter%0}> reverse()
meth public final <%0 extends java.lang.Object> com.google.common.base.Converter<{com.google.common.base.Converter%0},{%%0}> andThen(com.google.common.base.Converter<{com.google.common.base.Converter%1},{%%0}>)
meth public final {com.google.common.base.Converter%1} apply({com.google.common.base.Converter%0})
 anno 0 java.lang.Deprecated()
meth public final {com.google.common.base.Converter%1} convert({com.google.common.base.Converter%0})
meth public java.lang.Iterable<{com.google.common.base.Converter%1}> convertAll(java.lang.Iterable<? extends {com.google.common.base.Converter%0}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> com.google.common.base.Converter<{%%0},{%%1}> from(com.google.common.base.Function<? super {%%0},? extends {%%1}>,com.google.common.base.Function<? super {%%1},? extends {%%0}>)
meth public static <%0 extends java.lang.Object> com.google.common.base.Converter<{%%0},{%%0}> identity()
supr java.lang.Object
hfds handleNullAutomatically,reverse
hcls ConverterComposition,FunctionBasedConverter,IdentityConverter,ReverseConverter

CLSS public final com.google.common.base.Defaults
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
meth public static <%0 extends java.lang.Object> {%%0} defaultValue(java.lang.Class<{%%0}>)
supr java.lang.Object
hfds DOUBLE_DEFAULT,FLOAT_DEFAULT

CLSS public final com.google.common.base.Enums
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
meth public static <%0 extends java.lang.Enum<{%%0}>> com.google.common.base.Converter<java.lang.String,{%%0}> stringConverter(java.lang.Class<{%%0}>)
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
meth public static <%0 extends java.lang.Enum<{%%0}>> com.google.common.base.Optional<{%%0}> getIfPresent(java.lang.Class<{%%0}>,java.lang.String)
meth public static java.lang.reflect.Field getField(java.lang.Enum<?>)
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
supr java.lang.Object
hfds enumConstantCache
hcls StringConverter

CLSS public abstract com.google.common.base.Equivalence
intf java.util.function.BiPredicate
supr java.lang.Object

CLSS public final com.google.common.base.Equivalence$Wrapper
intf java.io.Serializable
supr java.lang.Object

CLSS public abstract com.google.common.base.FinalizablePhantomReference<%0 extends java.lang.Object>
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
cons protected init({com.google.common.base.FinalizablePhantomReference%0},com.google.common.base.FinalizableReferenceQueue)
intf com.google.common.base.FinalizableReference
supr java.lang.ref.PhantomReference<{com.google.common.base.FinalizablePhantomReference%0}>

CLSS public abstract interface com.google.common.base.FinalizableReference
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
meth public abstract void finalizeReferent()

CLSS public com.google.common.base.FinalizableReferenceQueue
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
cons public init()
intf java.io.Closeable
meth public void close()
supr java.lang.Object
hfds FINALIZER_CLASS_NAME,frqRef,logger,queue,startFinalizer,threadStarted
hcls DecoupledLoader,DirectLoader,FinalizerLoader,SystemLoader

CLSS public abstract com.google.common.base.FinalizableSoftReference<%0 extends java.lang.Object>
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
cons protected init({com.google.common.base.FinalizableSoftReference%0},com.google.common.base.FinalizableReferenceQueue)
intf com.google.common.base.FinalizableReference
supr java.lang.ref.SoftReference<{com.google.common.base.FinalizableSoftReference%0}>

CLSS public abstract com.google.common.base.FinalizableWeakReference<%0 extends java.lang.Object>
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
cons protected init({com.google.common.base.FinalizableWeakReference%0},com.google.common.base.FinalizableReferenceQueue)
intf com.google.common.base.FinalizableReference
supr java.lang.ref.WeakReference<{com.google.common.base.FinalizableWeakReference%0}>

CLSS public abstract interface com.google.common.base.Function
intf java.util.function.Function
meth public abstract boolean equals(java.lang.Object)
meth public abstract java.lang.Object apply(java.lang.Object)

CLSS public final com.google.common.base.Functions
supr java.lang.Object

CLSS public com.google.common.base.Joiner
supr java.lang.Object
hfds separator

CLSS public final static com.google.common.base.Joiner$MapJoiner
 outer com.google.common.base.Joiner
meth public <%0 extends java.lang.Appendable> {%%0} appendTo({%%0},java.lang.Iterable<? extends java.util.Map$Entry<?,?>>) throws java.io.IOException
meth public <%0 extends java.lang.Appendable> {%%0} appendTo({%%0},java.util.Iterator<? extends java.util.Map$Entry<?,?>>) throws java.io.IOException
meth public <%0 extends java.lang.Appendable> {%%0} appendTo({%%0},java.util.Map<?,?>) throws java.io.IOException
meth public com.google.common.base.Joiner$MapJoiner useForNull(java.lang.String)
meth public java.lang.String join(java.lang.Iterable<? extends java.util.Map$Entry<?,?>>)
meth public java.lang.String join(java.util.Iterator<? extends java.util.Map$Entry<?,?>>)
meth public java.lang.String join(java.util.Map<?,?>)
meth public java.lang.StringBuilder appendTo(java.lang.StringBuilder,java.lang.Iterable<? extends java.util.Map$Entry<?,?>>)
meth public java.lang.StringBuilder appendTo(java.lang.StringBuilder,java.util.Iterator<? extends java.util.Map$Entry<?,?>>)
meth public java.lang.StringBuilder appendTo(java.lang.StringBuilder,java.util.Map<?,?>)
supr java.lang.Object
hfds joiner,keyValueSeparator

CLSS public final com.google.common.base.MoreObjects
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=false, boolean serializable=false)
innr public final static ToStringHelper
meth public static <%0 extends java.lang.Object> {%%0} firstNonNull({%%0},{%%0})
meth public static com.google.common.base.MoreObjects$ToStringHelper toStringHelper(java.lang.Class<?>)
meth public static com.google.common.base.MoreObjects$ToStringHelper toStringHelper(java.lang.Object)
meth public static com.google.common.base.MoreObjects$ToStringHelper toStringHelper(java.lang.String)
supr java.lang.Object

CLSS public final static com.google.common.base.MoreObjects$ToStringHelper
 outer com.google.common.base.MoreObjects
meth public com.google.common.base.MoreObjects$ToStringHelper add(java.lang.String,boolean)
meth public com.google.common.base.MoreObjects$ToStringHelper add(java.lang.String,char)
meth public com.google.common.base.MoreObjects$ToStringHelper add(java.lang.String,double)
meth public com.google.common.base.MoreObjects$ToStringHelper add(java.lang.String,float)
meth public com.google.common.base.MoreObjects$ToStringHelper add(java.lang.String,int)
meth public com.google.common.base.MoreObjects$ToStringHelper add(java.lang.String,java.lang.Object)
meth public com.google.common.base.MoreObjects$ToStringHelper add(java.lang.String,long)
meth public com.google.common.base.MoreObjects$ToStringHelper addValue(boolean)
meth public com.google.common.base.MoreObjects$ToStringHelper addValue(char)
meth public com.google.common.base.MoreObjects$ToStringHelper addValue(double)
meth public com.google.common.base.MoreObjects$ToStringHelper addValue(float)
meth public com.google.common.base.MoreObjects$ToStringHelper addValue(int)
meth public com.google.common.base.MoreObjects$ToStringHelper addValue(java.lang.Object)
meth public com.google.common.base.MoreObjects$ToStringHelper addValue(long)
meth public com.google.common.base.MoreObjects$ToStringHelper omitNullValues()
meth public java.lang.String toString()
supr java.lang.Object
hfds className,holderHead,holderTail,omitEmptyValues,omitNullValues
hcls UnconditionalValueHolder,ValueHolder

CLSS public final com.google.common.base.Objects
supr java.lang.Object

CLSS public abstract com.google.common.base.Optional<%0 extends java.lang.Object>
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=false, boolean serializable=true)
intf java.io.Serializable
meth public abstract <%0 extends java.lang.Object> com.google.common.base.Optional<{%%0}> transform(com.google.common.base.Function<? super {com.google.common.base.Optional%0},{%%0}>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isPresent()
meth public abstract com.google.common.base.Optional<{com.google.common.base.Optional%0}> or(com.google.common.base.Optional<? extends {com.google.common.base.Optional%0}>)
meth public abstract int hashCode()
meth public abstract java.lang.String toString()
meth public abstract java.util.Set<{com.google.common.base.Optional%0}> asSet()
meth public abstract {com.google.common.base.Optional%0} get()
meth public abstract {com.google.common.base.Optional%0} or(com.google.common.base.Supplier<? extends {com.google.common.base.Optional%0}>)
meth public abstract {com.google.common.base.Optional%0} or({com.google.common.base.Optional%0})
meth public abstract {com.google.common.base.Optional%0} orNull()
meth public java.util.Optional<{com.google.common.base.Optional%0}> toJavaUtil()
meth public static <%0 extends java.lang.Object> com.google.common.base.Optional<{%%0}> absent()
meth public static <%0 extends java.lang.Object> com.google.common.base.Optional<{%%0}> fromJavaUtil(java.util.Optional<{%%0}>)
meth public static <%0 extends java.lang.Object> com.google.common.base.Optional<{%%0}> fromNullable({%%0})
meth public static <%0 extends java.lang.Object> com.google.common.base.Optional<{%%0}> of({%%0})
meth public static <%0 extends java.lang.Object> java.lang.Iterable<{%%0}> presentInstances(java.lang.Iterable<? extends com.google.common.base.Optional<? extends {%%0}>>)
meth public static <%0 extends java.lang.Object> java.util.Optional<{%%0}> toJavaUtil(com.google.common.base.Optional<{%%0}>)
supr java.lang.Object
hfds serialVersionUID

CLSS public final com.google.common.base.Preconditions
supr java.lang.Object

CLSS public abstract interface com.google.common.base.Predicate
intf java.util.function.Predicate
meth public abstract boolean apply(java.lang.Object)
meth public abstract boolean equals(java.lang.Object)
meth public boolean test(java.lang.Object)

CLSS public final com.google.common.base.Predicates
supr java.lang.Object

CLSS public final com.google.common.base.Splitter
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=true, boolean serializable=false)
innr public final static MapSplitter
meth public com.google.common.base.Splitter limit(int)
meth public com.google.common.base.Splitter omitEmptyStrings()
meth public com.google.common.base.Splitter trimResults()
meth public com.google.common.base.Splitter trimResults(com.google.common.base.CharMatcher)
meth public com.google.common.base.Splitter$MapSplitter withKeyValueSeparator(char)
meth public com.google.common.base.Splitter$MapSplitter withKeyValueSeparator(com.google.common.base.Splitter)
meth public com.google.common.base.Splitter$MapSplitter withKeyValueSeparator(java.lang.String)
meth public java.lang.Iterable<java.lang.String> split(java.lang.CharSequence)
meth public java.util.List<java.lang.String> splitToList(java.lang.CharSequence)
meth public java.util.stream.Stream<java.lang.String> splitToStream(java.lang.CharSequence)
meth public static com.google.common.base.Splitter fixedLength(int)
meth public static com.google.common.base.Splitter on(char)
meth public static com.google.common.base.Splitter on(com.google.common.base.CharMatcher)
meth public static com.google.common.base.Splitter on(java.lang.String)
meth public static com.google.common.base.Splitter on(java.util.regex.Pattern)
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
meth public static com.google.common.base.Splitter onPattern(java.lang.String)
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
supr java.lang.Object
hfds limit,omitEmptyStrings,strategy,trimmer
hcls SplittingIterator,Strategy

CLSS public final static com.google.common.base.Splitter$MapSplitter
 outer com.google.common.base.Splitter
meth public java.util.Map<java.lang.String,java.lang.String> split(java.lang.CharSequence)
supr java.lang.Object
hfds INVALID_ENTRY_MESSAGE,entrySplitter,outerSplitter

CLSS public final !enum com.google.common.base.StandardSystemProperty
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
fld public final static com.google.common.base.StandardSystemProperty FILE_SEPARATOR
fld public final static com.google.common.base.StandardSystemProperty JAVA_CLASS_PATH
fld public final static com.google.common.base.StandardSystemProperty JAVA_CLASS_VERSION
fld public final static com.google.common.base.StandardSystemProperty JAVA_COMPILER
fld public final static com.google.common.base.StandardSystemProperty JAVA_EXT_DIRS
 anno 0 java.lang.Deprecated()
fld public final static com.google.common.base.StandardSystemProperty JAVA_HOME
fld public final static com.google.common.base.StandardSystemProperty JAVA_IO_TMPDIR
fld public final static com.google.common.base.StandardSystemProperty JAVA_LIBRARY_PATH
fld public final static com.google.common.base.StandardSystemProperty JAVA_SPECIFICATION_NAME
fld public final static com.google.common.base.StandardSystemProperty JAVA_SPECIFICATION_VENDOR
fld public final static com.google.common.base.StandardSystemProperty JAVA_SPECIFICATION_VERSION
fld public final static com.google.common.base.StandardSystemProperty JAVA_VENDOR
fld public final static com.google.common.base.StandardSystemProperty JAVA_VENDOR_URL
fld public final static com.google.common.base.StandardSystemProperty JAVA_VERSION
fld public final static com.google.common.base.StandardSystemProperty JAVA_VM_NAME
fld public final static com.google.common.base.StandardSystemProperty JAVA_VM_SPECIFICATION_NAME
fld public final static com.google.common.base.StandardSystemProperty JAVA_VM_SPECIFICATION_VENDOR
fld public final static com.google.common.base.StandardSystemProperty JAVA_VM_SPECIFICATION_VERSION
fld public final static com.google.common.base.StandardSystemProperty JAVA_VM_VENDOR
fld public final static com.google.common.base.StandardSystemProperty JAVA_VM_VERSION
fld public final static com.google.common.base.StandardSystemProperty LINE_SEPARATOR
fld public final static com.google.common.base.StandardSystemProperty OS_ARCH
fld public final static com.google.common.base.StandardSystemProperty OS_NAME
fld public final static com.google.common.base.StandardSystemProperty OS_VERSION
fld public final static com.google.common.base.StandardSystemProperty PATH_SEPARATOR
fld public final static com.google.common.base.StandardSystemProperty USER_DIR
fld public final static com.google.common.base.StandardSystemProperty USER_HOME
fld public final static com.google.common.base.StandardSystemProperty USER_NAME
meth public java.lang.String key()
meth public java.lang.String toString()
meth public java.lang.String value()
meth public static com.google.common.base.StandardSystemProperty valueOf(java.lang.String)
meth public static com.google.common.base.StandardSystemProperty[] values()
supr java.lang.Enum<com.google.common.base.StandardSystemProperty>
hfds key

CLSS public final com.google.common.base.Stopwatch
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=true, boolean serializable=false)
meth public boolean isRunning()
meth public com.google.common.base.Stopwatch reset()
meth public com.google.common.base.Stopwatch start()
meth public com.google.common.base.Stopwatch stop()
meth public java.lang.String toString()
meth public java.time.Duration elapsed()
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
meth public long elapsed(java.util.concurrent.TimeUnit)
meth public static com.google.common.base.Stopwatch createStarted()
meth public static com.google.common.base.Stopwatch createStarted(com.google.common.base.Ticker)
meth public static com.google.common.base.Stopwatch createUnstarted()
meth public static com.google.common.base.Stopwatch createUnstarted(com.google.common.base.Ticker)
supr java.lang.Object
hfds elapsedNanos,isRunning,startTick,ticker

CLSS public final com.google.common.base.Strings
supr java.lang.Object

CLSS public abstract interface com.google.common.base.Supplier
intf java.util.function.Supplier
meth public abstract java.lang.Object get()

CLSS public final com.google.common.base.Suppliers
supr java.lang.Object

CLSS public final com.google.common.base.Throwables
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=true, boolean serializable=false)
meth public static <%0 extends java.lang.Throwable, %1 extends java.lang.Throwable> void propagateIfPossible(java.lang.Throwable,java.lang.Class<{%%0}>,java.lang.Class<{%%1}>) throws {%%0},{%%1}
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
meth public static <%0 extends java.lang.Throwable> void propagateIfInstanceOf(java.lang.Throwable,java.lang.Class<{%%0}>) throws {%%0}
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
 anno 0 java.lang.Deprecated()
meth public static <%0 extends java.lang.Throwable> void propagateIfPossible(java.lang.Throwable,java.lang.Class<{%%0}>) throws {%%0}
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
meth public static <%0 extends java.lang.Throwable> void throwIfInstanceOf(java.lang.Throwable,java.lang.Class<{%%0}>) throws {%%0}
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
meth public static <%0 extends java.lang.Throwable> {%%0} getCauseAs(java.lang.Throwable,java.lang.Class<{%%0}>)
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
meth public static boolean lazyStackTraceIsLazy()
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
 anno 0 java.lang.Deprecated()
meth public static java.lang.RuntimeException propagate(java.lang.Throwable)
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
 anno 0 java.lang.Deprecated()
meth public static java.lang.String getStackTraceAsString(java.lang.Throwable)
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
meth public static java.lang.Throwable getRootCause(java.lang.Throwable)
meth public static java.util.List<java.lang.StackTraceElement> lazyStackTrace(java.lang.Throwable)
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
 anno 0 java.lang.Deprecated()
meth public static java.util.List<java.lang.Throwable> getCausalChain(java.lang.Throwable)
meth public static void propagateIfPossible(java.lang.Throwable)
 anno 0 com.google.common.annotations.GwtIncompatible(java.lang.String value="")
 anno 0 java.lang.Deprecated()
meth public static void throwIfUnchecked(java.lang.Throwable)
supr java.lang.Object
hfds JAVA_LANG_ACCESS_CLASSNAME,SHARED_SECRETS_CLASSNAME,getStackTraceDepthMethod,getStackTraceElementMethod,jla

CLSS public abstract com.google.common.base.Ticker
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=false, boolean serializable=false)
cons protected init()
meth public abstract long read()
meth public static com.google.common.base.Ticker systemTicker()
supr java.lang.Object
hfds SYSTEM_TICKER

CLSS public final com.google.common.base.Utf8
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=true, boolean serializable=false)
meth public static boolean isWellFormed(byte[])
meth public static boolean isWellFormed(byte[],int,int)
meth public static int encodedLength(java.lang.CharSequence)
supr java.lang.Object

CLSS public final com.google.common.base.Verify
supr java.lang.Object

CLSS public com.google.common.base.VerifyException
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=false, boolean serializable=false)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

CLSS abstract interface com.google.common.base.package-info

CLSS public abstract com.google.inject.AbstractModule
cons public init()
intf com.google.inject.Module
meth protected !varargs void addError(java.lang.String,java.lang.Object[])
meth protected !varargs void bindInterceptor(com.google.inject.matcher.Matcher<? super java.lang.Class<?>>,com.google.inject.matcher.Matcher<? super java.lang.reflect.Method>,org.aopalliance.intercept.MethodInterceptor[])
meth protected !varargs void bindListener(com.google.inject.matcher.Matcher<? super com.google.inject.Binding<?>>,com.google.inject.spi.ProvisionListener[])
meth protected !varargs void requestStaticInjection(java.lang.Class<?>[])
meth protected <%0 extends java.lang.Object> com.google.inject.MembersInjector<{%%0}> getMembersInjector(com.google.inject.TypeLiteral<{%%0}>)
meth protected <%0 extends java.lang.Object> com.google.inject.MembersInjector<{%%0}> getMembersInjector(java.lang.Class<{%%0}>)
meth protected <%0 extends java.lang.Object> com.google.inject.Provider<{%%0}> getProvider(com.google.inject.Key<{%%0}>)
meth protected <%0 extends java.lang.Object> com.google.inject.Provider<{%%0}> getProvider(java.lang.Class<{%%0}>)
meth protected <%0 extends java.lang.Object> com.google.inject.binder.AnnotatedBindingBuilder<{%%0}> bind(com.google.inject.TypeLiteral<{%%0}>)
meth protected <%0 extends java.lang.Object> com.google.inject.binder.AnnotatedBindingBuilder<{%%0}> bind(java.lang.Class<{%%0}>)
meth protected <%0 extends java.lang.Object> com.google.inject.binder.LinkedBindingBuilder<{%%0}> bind(com.google.inject.Key<{%%0}>)
meth protected com.google.inject.Binder binder()
meth protected com.google.inject.Stage currentStage()
meth protected com.google.inject.binder.AnnotatedConstantBindingBuilder bindConstant()
meth protected void addError(com.google.inject.spi.Message)
meth protected void addError(java.lang.Throwable)
meth protected void bindListener(com.google.inject.matcher.Matcher<? super com.google.inject.TypeLiteral<?>>,com.google.inject.spi.TypeListener)
meth protected void bindScope(java.lang.Class<? extends java.lang.annotation.Annotation>,com.google.inject.Scope)
meth protected void configure()
meth protected void convertToTypes(com.google.inject.matcher.Matcher<? super com.google.inject.TypeLiteral<?>>,com.google.inject.spi.TypeConverter)
meth protected void install(com.google.inject.Module)
meth protected void requestInjection(java.lang.Object)
meth protected void requireBinding(com.google.inject.Key<?>)
meth protected void requireBinding(java.lang.Class<?>)
meth public final void configure(com.google.inject.Binder)
supr java.lang.Object
hfds binder

CLSS public abstract interface com.google.inject.Binder
meth public abstract !varargs com.google.inject.Binder skipSources(java.lang.Class<?>[])
meth public abstract !varargs void addError(java.lang.String,java.lang.Object[])
meth public abstract !varargs void bindInterceptor(com.google.inject.matcher.Matcher<? super java.lang.Class<?>>,com.google.inject.matcher.Matcher<? super java.lang.reflect.Method>,org.aopalliance.intercept.MethodInterceptor[])
meth public abstract !varargs void bindListener(com.google.inject.matcher.Matcher<? super com.google.inject.Binding<?>>,com.google.inject.spi.ProvisionListener[])
meth public abstract !varargs void requestStaticInjection(java.lang.Class<?>[])
meth public abstract <%0 extends java.lang.Object> com.google.inject.MembersInjector<{%%0}> getMembersInjector(com.google.inject.TypeLiteral<{%%0}>)
meth public abstract <%0 extends java.lang.Object> com.google.inject.MembersInjector<{%%0}> getMembersInjector(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> com.google.inject.Provider<{%%0}> getProvider(com.google.inject.Key<{%%0}>)
meth public abstract <%0 extends java.lang.Object> com.google.inject.Provider<{%%0}> getProvider(com.google.inject.spi.Dependency<{%%0}>)
meth public abstract <%0 extends java.lang.Object> com.google.inject.Provider<{%%0}> getProvider(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> com.google.inject.binder.AnnotatedBindingBuilder<{%%0}> bind(com.google.inject.TypeLiteral<{%%0}>)
meth public abstract <%0 extends java.lang.Object> com.google.inject.binder.AnnotatedBindingBuilder<{%%0}> bind(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> com.google.inject.binder.LinkedBindingBuilder<{%%0}> bind(com.google.inject.Key<{%%0}>)
meth public abstract <%0 extends java.lang.Object> void requestInjection(com.google.inject.TypeLiteral<{%%0}>,{%%0})
meth public abstract com.google.inject.Binder withSource(java.lang.Object)
meth public abstract com.google.inject.PrivateBinder newPrivateBinder()
meth public abstract com.google.inject.Stage currentStage()
meth public abstract com.google.inject.binder.AnnotatedConstantBindingBuilder bindConstant()
meth public abstract void addError(com.google.inject.spi.Message)
meth public abstract void addError(java.lang.Throwable)
meth public abstract void bindListener(com.google.inject.matcher.Matcher<? super com.google.inject.TypeLiteral<?>>,com.google.inject.spi.TypeListener)
meth public abstract void bindScope(java.lang.Class<? extends java.lang.annotation.Annotation>,com.google.inject.Scope)
meth public abstract void convertToTypes(com.google.inject.matcher.Matcher<? super com.google.inject.TypeLiteral<?>>,com.google.inject.spi.TypeConverter)
meth public abstract void disableCircularProxies()
meth public abstract void install(com.google.inject.Module)
meth public abstract void requestInjection(java.lang.Object)
meth public abstract void requireAtInjectOnConstructors()
meth public abstract void requireExactBindingAnnotations()
meth public abstract void requireExplicitBindings()
meth public abstract void scanModulesForAnnotatedMethods(com.google.inject.spi.ModuleAnnotatedMethodScanner)

CLSS public abstract interface com.google.inject.Binding<%0 extends java.lang.Object>
intf com.google.inject.spi.Element
meth public abstract <%0 extends java.lang.Object> {%%0} acceptScopingVisitor(com.google.inject.spi.BindingScopingVisitor<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} acceptTargetVisitor(com.google.inject.spi.BindingTargetVisitor<? super {com.google.inject.Binding%0},{%%0}>)
meth public abstract com.google.inject.Key<{com.google.inject.Binding%0}> getKey()
meth public abstract com.google.inject.Provider<{com.google.inject.Binding%0}> getProvider()

CLSS public abstract interface !annotation com.google.inject.BindingAnnotation
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public final com.google.inject.ConfigurationException
cons public init(java.lang.Iterable<com.google.inject.spi.Message>)
meth public <%0 extends java.lang.Object> {%%0} getPartialValue()
meth public com.google.inject.ConfigurationException withPartialValue(java.lang.Object)
meth public java.lang.String getMessage()
meth public java.util.Collection<com.google.inject.spi.Message> getErrorMessages()
supr java.lang.RuntimeException
hfds messages,partialValue,serialVersionUID

CLSS public com.google.inject.CreationException
cons public init(java.util.Collection<com.google.inject.spi.Message>)
meth public java.lang.String getMessage()
meth public java.util.Collection<com.google.inject.spi.Message> getErrorMessages()
supr java.lang.RuntimeException
hfds messages,serialVersionUID

CLSS public abstract interface !annotation com.google.inject.Exposed
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public final com.google.inject.Guice
meth public !varargs static com.google.inject.Injector createInjector(com.google.inject.Module[])
meth public !varargs static com.google.inject.Injector createInjector(com.google.inject.Stage,com.google.inject.Module[])
meth public static com.google.inject.Injector createInjector(com.google.inject.Stage,java.lang.Iterable<? extends com.google.inject.Module>)
meth public static com.google.inject.Injector createInjector(java.lang.Iterable<? extends com.google.inject.Module>)
supr java.lang.Object

CLSS public abstract interface !annotation com.google.inject.ImplementedBy
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?> value()

CLSS public abstract interface !annotation com.google.inject.Inject
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, CONSTRUCTOR, FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean optional()

CLSS public abstract interface com.google.inject.Injector
meth public abstract !varargs com.google.inject.Injector createChildInjector(com.google.inject.Module[])
meth public abstract <%0 extends java.lang.Object> com.google.inject.Binding<{%%0}> getBinding(com.google.inject.Key<{%%0}>)
meth public abstract <%0 extends java.lang.Object> com.google.inject.Binding<{%%0}> getBinding(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> com.google.inject.Binding<{%%0}> getExistingBinding(com.google.inject.Key<{%%0}>)
meth public abstract <%0 extends java.lang.Object> com.google.inject.MembersInjector<{%%0}> getMembersInjector(com.google.inject.TypeLiteral<{%%0}>)
meth public abstract <%0 extends java.lang.Object> com.google.inject.MembersInjector<{%%0}> getMembersInjector(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> com.google.inject.Provider<{%%0}> getProvider(com.google.inject.Key<{%%0}>)
meth public abstract <%0 extends java.lang.Object> com.google.inject.Provider<{%%0}> getProvider(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.List<com.google.inject.Binding<{%%0}>> findBindingsByType(com.google.inject.TypeLiteral<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} getInstance(com.google.inject.Key<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} getInstance(java.lang.Class<{%%0}>)
meth public abstract com.google.inject.Injector createChildInjector(java.lang.Iterable<? extends com.google.inject.Module>)
meth public abstract com.google.inject.Injector getParent()
meth public abstract java.util.List<com.google.inject.spi.Element> getElements()
meth public abstract java.util.Map<com.google.inject.Key<?>,com.google.inject.Binding<?>> getAllBindings()
meth public abstract java.util.Map<com.google.inject.Key<?>,com.google.inject.Binding<?>> getBindings()
meth public abstract java.util.Map<com.google.inject.TypeLiteral<?>,java.util.List<com.google.inject.spi.InjectionPoint>> getAllMembersInjectorInjectionPoints()
meth public abstract java.util.Map<java.lang.Class<? extends java.lang.annotation.Annotation>,com.google.inject.Scope> getScopeBindings()
meth public abstract java.util.Set<com.google.inject.spi.TypeConverterBinding> getTypeConverterBindings()
meth public abstract void injectMembers(java.lang.Object)

CLSS public com.google.inject.Key<%0 extends java.lang.Object>
cons protected init()
cons protected init(java.lang.Class<? extends java.lang.annotation.Annotation>)
cons protected init(java.lang.annotation.Annotation)
meth public <%0 extends java.lang.Object> com.google.inject.Key<{%%0}> ofType(com.google.inject.TypeLiteral<{%%0}>)
meth public <%0 extends java.lang.Object> com.google.inject.Key<{%%0}> ofType(java.lang.Class<{%%0}>)
meth public boolean hasAttributes()
meth public com.google.inject.Key<?> ofType(java.lang.reflect.Type)
meth public com.google.inject.Key<{com.google.inject.Key%0}> withAnnotation(java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public com.google.inject.Key<{com.google.inject.Key%0}> withAnnotation(java.lang.annotation.Annotation)
meth public com.google.inject.Key<{com.google.inject.Key%0}> withoutAttributes()
meth public final boolean equals(java.lang.Object)
meth public final com.google.inject.TypeLiteral<{com.google.inject.Key%0}> getTypeLiteral()
meth public final int hashCode()
meth public final java.lang.Class<? extends java.lang.annotation.Annotation> getAnnotationType()
meth public final java.lang.String toString()
meth public final java.lang.annotation.Annotation getAnnotation()
meth public static <%0 extends java.lang.Object> com.google.inject.Key<{%%0}> get(com.google.inject.TypeLiteral<{%%0}>)
meth public static <%0 extends java.lang.Object> com.google.inject.Key<{%%0}> get(com.google.inject.TypeLiteral<{%%0}>,java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public static <%0 extends java.lang.Object> com.google.inject.Key<{%%0}> get(com.google.inject.TypeLiteral<{%%0}>,java.lang.annotation.Annotation)
meth public static <%0 extends java.lang.Object> com.google.inject.Key<{%%0}> get(java.lang.Class<{%%0}>)
meth public static <%0 extends java.lang.Object> com.google.inject.Key<{%%0}> get(java.lang.Class<{%%0}>,java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public static <%0 extends java.lang.Object> com.google.inject.Key<{%%0}> get(java.lang.Class<{%%0}>,java.lang.annotation.Annotation)
meth public static com.google.inject.Key<?> get(java.lang.reflect.Type)
meth public static com.google.inject.Key<?> get(java.lang.reflect.Type,java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public static com.google.inject.Key<?> get(java.lang.reflect.Type,java.lang.annotation.Annotation)
supr java.lang.Object
hfds annotationStrategy,hashCode,toString,typeLiteral
hcls AnnotationInstanceStrategy,AnnotationStrategy,AnnotationTypeStrategy,NullAnnotationStrategy

CLSS public abstract interface com.google.inject.MembersInjector<%0 extends java.lang.Object>
meth public abstract void injectMembers({com.google.inject.MembersInjector%0})

CLSS public abstract interface com.google.inject.Module
meth public abstract void configure(com.google.inject.Binder)

CLSS public final com.google.inject.OutOfScopeException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public abstract interface com.google.inject.PrivateBinder
intf com.google.inject.Binder
meth public abstract !varargs com.google.inject.PrivateBinder skipSources(java.lang.Class<?>[])
meth public abstract com.google.inject.PrivateBinder withSource(java.lang.Object)
meth public abstract com.google.inject.binder.AnnotatedElementBuilder expose(com.google.inject.TypeLiteral<?>)
meth public abstract com.google.inject.binder.AnnotatedElementBuilder expose(java.lang.Class<?>)
meth public abstract void expose(com.google.inject.Key<?>)

CLSS public abstract com.google.inject.PrivateModule
cons public init()
intf com.google.inject.Module
meth protected !varargs final void addError(java.lang.String,java.lang.Object[])
meth protected !varargs final void bindInterceptor(com.google.inject.matcher.Matcher<? super java.lang.Class<?>>,com.google.inject.matcher.Matcher<? super java.lang.reflect.Method>,org.aopalliance.intercept.MethodInterceptor[])
meth protected !varargs final void requestStaticInjection(java.lang.Class<?>[])
meth protected !varargs void bindListener(com.google.inject.matcher.Matcher<? super com.google.inject.Binding<?>>,com.google.inject.spi.ProvisionListener[])
meth protected <%0 extends java.lang.Object> com.google.inject.MembersInjector<{%%0}> getMembersInjector(com.google.inject.TypeLiteral<{%%0}>)
meth protected <%0 extends java.lang.Object> com.google.inject.MembersInjector<{%%0}> getMembersInjector(java.lang.Class<{%%0}>)
meth protected abstract void configure()
meth protected final <%0 extends java.lang.Object> com.google.inject.Provider<{%%0}> getProvider(com.google.inject.Key<{%%0}>)
meth protected final <%0 extends java.lang.Object> com.google.inject.Provider<{%%0}> getProvider(java.lang.Class<{%%0}>)
meth protected final <%0 extends java.lang.Object> com.google.inject.binder.AnnotatedBindingBuilder<{%%0}> bind(com.google.inject.TypeLiteral<{%%0}>)
meth protected final <%0 extends java.lang.Object> com.google.inject.binder.AnnotatedBindingBuilder<{%%0}> bind(java.lang.Class<{%%0}>)
meth protected final <%0 extends java.lang.Object> com.google.inject.binder.LinkedBindingBuilder<{%%0}> bind(com.google.inject.Key<{%%0}>)
meth protected final <%0 extends java.lang.Object> void expose(com.google.inject.Key<{%%0}>)
meth protected final com.google.inject.PrivateBinder binder()
meth protected final com.google.inject.Stage currentStage()
meth protected final com.google.inject.binder.AnnotatedConstantBindingBuilder bindConstant()
meth protected final com.google.inject.binder.AnnotatedElementBuilder expose(com.google.inject.TypeLiteral<?>)
meth protected final com.google.inject.binder.AnnotatedElementBuilder expose(java.lang.Class<?>)
meth protected final void addError(com.google.inject.spi.Message)
meth protected final void addError(java.lang.Throwable)
meth protected final void bindScope(java.lang.Class<? extends java.lang.annotation.Annotation>,com.google.inject.Scope)
meth protected final void convertToTypes(com.google.inject.matcher.Matcher<? super com.google.inject.TypeLiteral<?>>,com.google.inject.spi.TypeConverter)
meth protected final void install(com.google.inject.Module)
meth protected final void requestInjection(java.lang.Object)
meth protected final void requireBinding(com.google.inject.Key<?>)
meth protected final void requireBinding(java.lang.Class<?>)
meth protected void bindListener(com.google.inject.matcher.Matcher<? super com.google.inject.TypeLiteral<?>>,com.google.inject.spi.TypeListener)
meth public final void configure(com.google.inject.Binder)
supr java.lang.Object
hfds binder

CLSS public abstract interface !annotation com.google.inject.ProvidedBy
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<? extends javax.inject.Provider<?>> value()

CLSS public abstract interface com.google.inject.Provider<%0 extends java.lang.Object>
intf javax.inject.Provider<{com.google.inject.Provider%0}>
meth public abstract {com.google.inject.Provider%0} get()

CLSS public abstract interface !annotation com.google.inject.Provides
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
intf java.lang.annotation.Annotation

CLSS public final com.google.inject.ProvisionException
cons public init(java.lang.Iterable<com.google.inject.spi.Message>)
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
meth public java.lang.String getMessage()
meth public java.util.Collection<com.google.inject.spi.Message> getErrorMessages()
supr java.lang.RuntimeException
hfds messages,serialVersionUID

CLSS public abstract interface !annotation com.google.inject.RestrictedBindingSource
 anno 0 java.lang.annotation.Inherited()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
innr public abstract interface static !annotation Permit
innr public final static !enum RestrictionLevel
intf java.lang.annotation.Annotation
meth public abstract !hasdefault com.google.inject.RestrictedBindingSource$RestrictionLevel restrictionLevel()
meth public abstract !hasdefault java.lang.String exemptModules()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation>[] permits()
meth public abstract java.lang.String explanation()

CLSS public abstract interface static !annotation com.google.inject.RestrictedBindingSource$Permit
 outer com.google.inject.RestrictedBindingSource
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public final static !enum com.google.inject.RestrictedBindingSource$RestrictionLevel
 outer com.google.inject.RestrictedBindingSource
fld public final static com.google.inject.RestrictedBindingSource$RestrictionLevel ERROR
fld public final static com.google.inject.RestrictedBindingSource$RestrictionLevel WARNING
meth public static com.google.inject.RestrictedBindingSource$RestrictionLevel valueOf(java.lang.String)
meth public static com.google.inject.RestrictedBindingSource$RestrictionLevel[] values()
supr java.lang.Enum<com.google.inject.RestrictedBindingSource$RestrictionLevel>

CLSS public abstract interface com.google.inject.Scope
meth public abstract <%0 extends java.lang.Object> com.google.inject.Provider<{%%0}> scope(com.google.inject.Key<{%%0}>,com.google.inject.Provider<{%%0}>)
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation com.google.inject.ScopeAnnotation
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public com.google.inject.Scopes
fld public final static com.google.inject.Scope NO_SCOPE
fld public final static com.google.inject.Scope SINGLETON
meth public static boolean isCircularProxy(java.lang.Object)
meth public static boolean isScoped(com.google.inject.Binding<?>,com.google.inject.Scope,java.lang.Class<? extends java.lang.annotation.Annotation>)
meth public static boolean isSingleton(com.google.inject.Binding<?>)
supr java.lang.Object
hfds IS_SINGLETON_VISITOR

CLSS public abstract interface !annotation com.google.inject.Singleton
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation

CLSS public final !enum com.google.inject.Stage
fld public final static com.google.inject.Stage DEVELOPMENT
fld public final static com.google.inject.Stage PRODUCTION
fld public final static com.google.inject.Stage TOOL
meth public static com.google.inject.Stage valueOf(java.lang.String)
meth public static com.google.inject.Stage[] values()
supr java.lang.Enum<com.google.inject.Stage>

CLSS public com.google.inject.TypeLiteral<%0 extends java.lang.Object>
cons protected init()
meth public com.google.inject.TypeLiteral<?> getFieldType(java.lang.reflect.Field)
meth public com.google.inject.TypeLiteral<?> getReturnType(java.lang.reflect.Method)
meth public com.google.inject.TypeLiteral<?> getSupertype(java.lang.Class<?>)
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final java.lang.Class<? super {com.google.inject.TypeLiteral%0}> getRawType()
meth public final java.lang.String toString()
meth public final java.lang.reflect.Type getType()
meth public java.util.List<com.google.inject.TypeLiteral<?>> getExceptionTypes(java.lang.reflect.Member)
meth public java.util.List<com.google.inject.TypeLiteral<?>> getParameterTypes(java.lang.reflect.Member)
meth public static <%0 extends java.lang.Object> com.google.inject.TypeLiteral<{%%0}> get(java.lang.Class<{%%0}>)
meth public static com.google.inject.TypeLiteral<?> get(java.lang.reflect.Type)
supr java.lang.Object
hfds hashCode,rawType,type

CLSS public abstract interface com.google.inject.spi.Element
meth public abstract <%0 extends java.lang.Object> {%%0} acceptVisitor(com.google.inject.spi.ElementVisitor<{%%0}>)
meth public abstract java.lang.Object getSource()
meth public abstract void applyTo(com.google.inject.Binder)

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract java.io.FilterReader
cons protected init(java.io.Reader)
fld protected java.io.Reader in
meth public boolean markSupported()
meth public boolean ready() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(char[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int) throws java.io.IOException
meth public void reset() throws java.io.IOException
supr java.io.Reader

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract java.io.InputStream
cons public init()
intf java.io.Closeable
meth public abstract int read() throws java.io.IOException
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.lang.Object

CLSS public abstract java.io.OutputStream
cons public init()
intf java.io.Closeable
intf java.io.Flushable
meth public abstract void write(int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
supr java.lang.Object

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

CLSS public abstract interface java.io.Serializable

CLSS public abstract java.io.Writer
cons protected init()
cons protected init(java.lang.Object)
fld protected java.lang.Object lock
intf java.io.Closeable
intf java.io.Flushable
intf java.lang.Appendable
meth public abstract void close() throws java.io.IOException
meth public abstract void flush() throws java.io.IOException
meth public abstract void write(char[],int,int) throws java.io.IOException
meth public java.io.Writer append(char) throws java.io.IOException
meth public java.io.Writer append(java.lang.CharSequence) throws java.io.IOException
meth public java.io.Writer append(java.lang.CharSequence,int,int) throws java.io.IOException
meth public void write(char[]) throws java.io.IOException
meth public void write(int) throws java.io.IOException
meth public void write(java.lang.String) throws java.io.IOException
meth public void write(java.lang.String,int,int) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.lang.Appendable
meth public abstract java.lang.Appendable append(char) throws java.io.IOException
meth public abstract java.lang.Appendable append(java.lang.CharSequence) throws java.io.IOException
meth public abstract java.lang.Appendable append(java.lang.CharSequence,int,int) throws java.io.IOException

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract java.lang.ClassLoader
cons protected init()
cons protected init(java.lang.ClassLoader)
meth protected final java.lang.Class<?> defineClass(byte[],int,int)
 anno 0 java.lang.Deprecated()
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int)
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int,java.security.ProtectionDomain)
meth protected final java.lang.Class<?> defineClass(java.lang.String,java.nio.ByteBuffer,java.security.ProtectionDomain)
meth protected final java.lang.Class<?> findLoadedClass(java.lang.String)
meth protected final java.lang.Class<?> findSystemClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected final void resolveClass(java.lang.Class<?>)
meth protected final void setSigners(java.lang.Class<?>,java.lang.Object[])
meth protected java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Class<?> loadClass(java.lang.String,boolean) throws java.lang.ClassNotFoundException
meth protected java.lang.Object getClassLoadingLock(java.lang.String)
meth protected java.lang.Package definePackage(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.net.URL)
meth protected java.lang.Package getPackage(java.lang.String)
meth protected java.lang.Package[] getPackages()
meth protected java.lang.String findLibrary(java.lang.String)
meth protected java.net.URL findResource(java.lang.String)
meth protected java.util.Enumeration<java.net.URL> findResources(java.lang.String) throws java.io.IOException
meth protected static boolean registerAsParallelCapable()
meth public final java.lang.ClassLoader getParent()
meth public java.io.InputStream getResourceAsStream(java.lang.String)
meth public java.lang.Class<?> loadClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.net.URL getResource(java.lang.String)
meth public java.util.Enumeration<java.net.URL> getResources(java.lang.String) throws java.io.IOException
meth public static java.io.InputStream getSystemResourceAsStream(java.lang.String)
meth public static java.lang.ClassLoader getSystemClassLoader()
meth public static java.net.URL getSystemResource(java.lang.String)
meth public static java.util.Enumeration<java.net.URL> getSystemResources(java.lang.String) throws java.io.IOException
meth public void clearAssertionStatus()
meth public void setClassAssertionStatus(java.lang.String,boolean)
meth public void setDefaultAssertionStatus(boolean)
meth public void setPackageAssertionStatus(java.lang.String,boolean)
supr java.lang.Object

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

CLSS public java.lang.IllegalArgumentException
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

CLSS public abstract interface !annotation java.lang.annotation.Inherited
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

CLSS public java.lang.ref.PhantomReference<%0 extends java.lang.Object>
cons public init({java.lang.ref.PhantomReference%0},java.lang.ref.ReferenceQueue<? super {java.lang.ref.PhantomReference%0}>)
meth public {java.lang.ref.PhantomReference%0} get()
supr java.lang.ref.Reference<{java.lang.ref.PhantomReference%0}>

CLSS public abstract java.lang.ref.Reference<%0 extends java.lang.Object>
meth public boolean enqueue()
meth public boolean isEnqueued()
meth public void clear()
meth public {java.lang.ref.Reference%0} get()
supr java.lang.Object

CLSS public java.lang.ref.SoftReference<%0 extends java.lang.Object>
cons public init({java.lang.ref.SoftReference%0})
cons public init({java.lang.ref.SoftReference%0},java.lang.ref.ReferenceQueue<? super {java.lang.ref.SoftReference%0}>)
meth public {java.lang.ref.SoftReference%0} get()
supr java.lang.ref.Reference<{java.lang.ref.SoftReference%0}>

CLSS public java.lang.ref.WeakReference<%0 extends java.lang.Object>
cons public init({java.lang.ref.WeakReference%0})
cons public init({java.lang.ref.WeakReference%0},java.lang.ref.ReferenceQueue<? super {java.lang.ref.WeakReference%0}>)
supr java.lang.ref.Reference<{java.lang.ref.WeakReference%0}>

CLSS public java.net.URLClassLoader
cons public init(java.net.URL[])
cons public init(java.net.URL[],java.lang.ClassLoader)
cons public init(java.net.URL[],java.lang.ClassLoader,java.net.URLStreamHandlerFactory)
intf java.io.Closeable
meth protected java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Package definePackage(java.lang.String,java.util.jar.Manifest,java.net.URL)
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
meth protected void addURL(java.net.URL)
meth public java.io.InputStream getResourceAsStream(java.lang.String)
meth public java.net.URL findResource(java.lang.String)
meth public java.net.URL[] getURLs()
meth public java.util.Enumeration<java.net.URL> findResources(java.lang.String) throws java.io.IOException
meth public static java.net.URLClassLoader newInstance(java.net.URL[])
meth public static java.net.URLClassLoader newInstance(java.net.URL[],java.lang.ClassLoader)
meth public void close() throws java.io.IOException
supr java.security.SecureClassLoader

CLSS public java.security.SecureClassLoader
cons protected init()
cons protected init(java.lang.ClassLoader)
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int,java.security.CodeSource)
meth protected final java.lang.Class<?> defineClass(java.lang.String,java.nio.ByteBuffer,java.security.CodeSource)
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
supr java.lang.ClassLoader

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

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

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

CLSS public abstract interface java.util.concurrent.Callable<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract {java.util.concurrent.Callable%0} call() throws java.lang.Exception

CLSS public abstract interface java.util.function.BiPredicate<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract boolean test({java.util.function.BiPredicate%0},{java.util.function.BiPredicate%1})
meth public java.util.function.BiPredicate<{java.util.function.BiPredicate%0},{java.util.function.BiPredicate%1}> and(java.util.function.BiPredicate<? super {java.util.function.BiPredicate%0},? super {java.util.function.BiPredicate%1}>)
meth public java.util.function.BiPredicate<{java.util.function.BiPredicate%0},{java.util.function.BiPredicate%1}> negate()
meth public java.util.function.BiPredicate<{java.util.function.BiPredicate%0},{java.util.function.BiPredicate%1}> or(java.util.function.BiPredicate<? super {java.util.function.BiPredicate%0},? super {java.util.function.BiPredicate%1}>)

CLSS public abstract interface java.util.function.Function<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public <%0 extends java.lang.Object> java.util.function.Function<{%%0},{java.util.function.Function%1}> compose(java.util.function.Function<? super {%%0},? extends {java.util.function.Function%0}>)
meth public <%0 extends java.lang.Object> java.util.function.Function<{java.util.function.Function%0},{%%0}> andThen(java.util.function.Function<? super {java.util.function.Function%1},? extends {%%0}>)
meth public abstract {java.util.function.Function%1} apply({java.util.function.Function%0})
meth public static <%0 extends java.lang.Object> java.util.function.Function<{%%0},{%%0}> identity()

CLSS public abstract interface java.util.function.Predicate<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract boolean test({java.util.function.Predicate%0})
meth public java.util.function.Predicate<{java.util.function.Predicate%0}> and(java.util.function.Predicate<? super {java.util.function.Predicate%0}>)
meth public java.util.function.Predicate<{java.util.function.Predicate%0}> negate()
meth public java.util.function.Predicate<{java.util.function.Predicate%0}> or(java.util.function.Predicate<? super {java.util.function.Predicate%0}>)
meth public static <%0 extends java.lang.Object> java.util.function.Predicate<{%%0}> isEqual(java.lang.Object)

CLSS public abstract interface java.util.function.Supplier<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract {java.util.function.Supplier%0} get()

CLSS public abstract interface !annotation javax.annotation.Nonnull
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.annotation.meta.When when()

CLSS public abstract interface !annotation javax.inject.Inject
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD, CONSTRUCTOR, FIELD])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.inject.Named
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 javax.inject.Qualifier()
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface javax.inject.Provider<%0 extends java.lang.Object>
meth public abstract {javax.inject.Provider%0} get()

CLSS public abstract interface !annotation javax.inject.Qualifier
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.inject.Scope
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.inject.Singleton
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 javax.inject.Scope()
intf java.lang.annotation.Annotation

CLSS public abstract interface javax.xml.namespace.NamespaceContext
meth public abstract java.lang.String getNamespaceURI(java.lang.String)
meth public abstract java.lang.String getPrefix(java.lang.String)
meth public abstract java.util.Iterator getPrefixes(java.lang.String)

CLSS public abstract interface javax.xml.stream.XMLStreamWriter
meth public abstract java.lang.Object getProperty(java.lang.String)
meth public abstract java.lang.String getPrefix(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract javax.xml.namespace.NamespaceContext getNamespaceContext()
meth public abstract void close() throws javax.xml.stream.XMLStreamException
meth public abstract void flush() throws javax.xml.stream.XMLStreamException
meth public abstract void setDefaultNamespace(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void setNamespaceContext(javax.xml.namespace.NamespaceContext) throws javax.xml.stream.XMLStreamException
meth public abstract void setPrefix(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeAttribute(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeAttribute(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeAttribute(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeCData(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeCharacters(char[],int,int) throws javax.xml.stream.XMLStreamException
meth public abstract void writeCharacters(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeComment(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeDTD(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeDefaultNamespace(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeEmptyElement(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeEmptyElement(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeEmptyElement(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeEndDocument() throws javax.xml.stream.XMLStreamException
meth public abstract void writeEndElement() throws javax.xml.stream.XMLStreamException
meth public abstract void writeEntityRef(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeNamespace(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeProcessingInstruction(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeProcessingInstruction(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeStartDocument() throws javax.xml.stream.XMLStreamException
meth public abstract void writeStartDocument(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeStartDocument(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeStartElement(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeStartElement(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public abstract void writeStartElement(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException

CLSS public abstract interface !annotation org.apache.http.annotation.Contract
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault org.apache.http.annotation.ThreadingBehavior threading()

CLSS public abstract interface org.apache.http.client.RedirectStrategy
meth public abstract boolean isRedirected(org.apache.http.HttpRequest,org.apache.http.HttpResponse,org.apache.http.protocol.HttpContext) throws org.apache.http.ProtocolException
meth public abstract org.apache.http.client.methods.HttpUriRequest getRedirect(org.apache.http.HttpRequest,org.apache.http.HttpResponse,org.apache.http.protocol.HttpContext) throws org.apache.http.ProtocolException

CLSS public abstract interface org.apache.http.client.ServiceUnavailableRetryStrategy
meth public abstract boolean retryRequest(org.apache.http.HttpResponse,int,org.apache.http.protocol.HttpContext)
meth public abstract long getRetryInterval()

CLSS public abstract interface org.apache.http.conn.ssl.TrustStrategy
intf org.apache.http.ssl.TrustStrategy

CLSS public org.apache.http.impl.client.DefaultRedirectStrategy
 anno 0 org.apache.http.annotation.Contract(org.apache.http.annotation.ThreadingBehavior threading=IMMUTABLE)
cons public init()
cons public init(java.lang.String[])
fld public final static int SC_PERMANENT_REDIRECT = 308
fld public final static java.lang.String REDIRECT_LOCATIONS = "http.protocol.redirect-locations"
 anno 0 java.lang.Deprecated()
fld public final static org.apache.http.impl.client.DefaultRedirectStrategy INSTANCE
intf org.apache.http.client.RedirectStrategy
meth protected boolean isRedirectable(java.lang.String)
meth protected java.net.URI createLocationURI(java.lang.String) throws org.apache.http.ProtocolException
meth public boolean isRedirected(org.apache.http.HttpRequest,org.apache.http.HttpResponse,org.apache.http.protocol.HttpContext) throws org.apache.http.ProtocolException
meth public java.net.URI getLocationURI(org.apache.http.HttpRequest,org.apache.http.HttpResponse,org.apache.http.protocol.HttpContext) throws org.apache.http.ProtocolException
meth public org.apache.http.client.methods.HttpUriRequest getRedirect(org.apache.http.HttpRequest,org.apache.http.HttpResponse,org.apache.http.protocol.HttpContext) throws org.apache.http.ProtocolException
supr java.lang.Object
hfds log,redirectMethods

CLSS public abstract interface org.apache.http.ssl.TrustStrategy
meth public abstract boolean isTrusted(java.security.cert.X509Certificate[],java.lang.String) throws java.security.cert.CertificateException

CLSS public abstract org.apache.maven.AbstractMavenLifecycleParticipant
cons public init()
meth public void afterProjectsRead(org.apache.maven.execution.MavenSession) throws org.apache.maven.MavenExecutionException
meth public void afterSessionEnd(org.apache.maven.execution.MavenSession) throws org.apache.maven.MavenExecutionException
meth public void afterSessionStart(org.apache.maven.execution.MavenSession) throws org.apache.maven.MavenExecutionException
supr java.lang.Object

CLSS public abstract interface org.apache.maven.ArtifactFilterManager
meth public abstract java.util.Set<java.lang.String> getCoreArtifactExcludes()
meth public abstract org.apache.maven.artifact.resolver.filter.ArtifactFilter getArtifactFilter()
meth public abstract org.apache.maven.artifact.resolver.filter.ArtifactFilter getCoreArtifactFilter()
meth public abstract void excludeArtifact(java.lang.String)

CLSS public abstract interface org.apache.maven.ArtifactFilterManagerDelegate
meth public abstract void addCoreExcludes(java.util.Set<java.lang.String>)
meth public abstract void addExcludes(java.util.Set<java.lang.String>)

CLSS public org.apache.maven.BuildAbort
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Error

CLSS public org.apache.maven.BuildFailureException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception

CLSS public org.apache.maven.DefaultArtifactFilterManager
 anno 0 javax.inject.Named(java.lang.String value="")
 anno 0 javax.inject.Singleton()
cons public init(java.util.List<org.apache.maven.ArtifactFilterManagerDelegate>,org.apache.maven.extension.internal.CoreExports)
 anno 0 javax.inject.Inject()
fld protected final java.util.List<org.apache.maven.ArtifactFilterManagerDelegate> delegates
fld protected java.util.Set<java.lang.String> excludedArtifacts
intf org.apache.maven.ArtifactFilterManager
meth public java.util.Set<java.lang.String> getCoreArtifactExcludes()
meth public org.apache.maven.artifact.resolver.filter.ArtifactFilter getArtifactFilter()
meth public org.apache.maven.artifact.resolver.filter.ArtifactFilter getCoreArtifactFilter()
meth public void excludeArtifact(java.lang.String)
supr java.lang.Object
hfds coreArtifacts

CLSS public org.apache.maven.DefaultMaven
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.Maven, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
fld protected org.apache.maven.project.ProjectBuilder projectBuilder
 anno 0 org.codehaus.plexus.component.annotations.Requirement(boolean optional=false, java.lang.Class<?> role=class java.lang.Object, java.lang.String hint="", java.lang.String[] hints=[])
fld protected org.codehaus.plexus.PlexusContainer container
 anno 0 org.codehaus.plexus.component.annotations.Requirement(boolean optional=false, java.lang.Class<?> role=class java.lang.Object, java.lang.String hint="", java.lang.String[] hints=[])
intf org.apache.maven.Maven
meth protected <%0 extends java.lang.Object> java.util.Collection<{%%0}> getProjectScopedExtensionComponents(java.util.Collection<org.apache.maven.project.MavenProject>,java.lang.Class<{%%0}>)
meth protected org.codehaus.plexus.logging.Logger getLogger()
 anno 0 java.lang.Deprecated()
meth public org.apache.maven.execution.MavenExecutionResult execute(org.apache.maven.execution.MavenExecutionRequest)
meth public org.eclipse.aether.RepositorySystemSession newRepositorySession(org.apache.maven.execution.MavenExecutionRequest)
supr java.lang.Object
hfds eventCatapult,graphBuilder,legacySupport,lifecycleStarter,logger,repositorySessionFactory,sessionScope

CLSS public org.apache.maven.DefaultProjectDependenciesResolver
 anno 0 java.lang.Deprecated()
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.ProjectDependenciesResolver, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.ProjectDependenciesResolver
meth public java.util.Set<org.apache.maven.artifact.Artifact> resolve(java.util.Collection<? extends org.apache.maven.project.MavenProject>,java.util.Collection<java.lang.String>,org.apache.maven.execution.MavenSession) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public java.util.Set<org.apache.maven.artifact.Artifact> resolve(org.apache.maven.project.MavenProject,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,org.apache.maven.execution.MavenSession) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public java.util.Set<org.apache.maven.artifact.Artifact> resolve(org.apache.maven.project.MavenProject,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,org.apache.maven.execution.MavenSession,java.util.Set<org.apache.maven.artifact.Artifact>) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public java.util.Set<org.apache.maven.artifact.Artifact> resolve(org.apache.maven.project.MavenProject,java.util.Collection<java.lang.String>,org.apache.maven.execution.MavenSession) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
supr java.lang.Object
hfds repositorySystem,resolutionErrorHandler

CLSS public org.apache.maven.DuplicateProjectException
cons public init(java.lang.String,java.util.Map<java.lang.String,java.util.List<java.io.File>>)
meth public java.util.Map<java.lang.String,java.util.List<java.io.File>> getCollisions()
supr org.apache.maven.MavenExecutionException
hfds collisions

CLSS public org.apache.maven.InternalErrorException
cons public init(java.lang.String,java.lang.Throwable)
supr org.apache.maven.MavenExecutionException

CLSS public abstract interface org.apache.maven.Maven
fld public final static java.lang.String POMv4 = "pom.xml"
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.execution.MavenExecutionResult execute(org.apache.maven.execution.MavenExecutionRequest)

CLSS public org.apache.maven.MavenExecutionException
cons public init(java.lang.String,java.io.File)
cons public init(java.lang.String,java.io.File,org.apache.maven.project.ProjectBuildingException)
cons public init(java.lang.String,java.lang.Throwable)
meth public java.io.File getPomFile()
supr java.lang.Exception
hfds pomFile

CLSS public org.apache.maven.MissingModuleException
cons public init(java.lang.String,java.io.File,java.io.File)
meth public java.io.File getModuleFile()
meth public java.lang.String getModuleName()
supr org.apache.maven.MavenExecutionException
hfds moduleFile,moduleName

CLSS public org.apache.maven.ProjectBuildFailureException
cons public init(java.lang.String,org.apache.maven.plugin.MojoFailureException)
meth public java.lang.String getProjectId()
meth public org.apache.maven.plugin.MojoFailureException getMojoFailureException()
supr org.apache.maven.BuildFailureException
hfds projectId

CLSS public org.apache.maven.ProjectCycleException
cons public init(java.lang.String)
cons public init(java.lang.String,org.codehaus.plexus.util.dag.CycleDetectedException)
supr org.apache.maven.BuildFailureException

CLSS public abstract interface org.apache.maven.ProjectDependenciesResolver
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Set<org.apache.maven.artifact.Artifact> resolve(java.util.Collection<? extends org.apache.maven.project.MavenProject>,java.util.Collection<java.lang.String>,org.apache.maven.execution.MavenSession) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public abstract java.util.Set<org.apache.maven.artifact.Artifact> resolve(org.apache.maven.project.MavenProject,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,org.apache.maven.execution.MavenSession) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public abstract java.util.Set<org.apache.maven.artifact.Artifact> resolve(org.apache.maven.project.MavenProject,java.util.Collection<java.lang.String>,java.util.Collection<java.lang.String>,org.apache.maven.execution.MavenSession,java.util.Set<org.apache.maven.artifact.Artifact>) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public abstract java.util.Set<org.apache.maven.artifact.Artifact> resolve(org.apache.maven.project.MavenProject,java.util.Collection<java.lang.String>,org.apache.maven.execution.MavenSession) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException

CLSS public org.apache.maven.RepositoryUtils
cons public init()
meth public static boolean repositoriesEquals(java.util.List<org.eclipse.aether.repository.RemoteRepository>,java.util.List<org.eclipse.aether.repository.RemoteRepository>)
meth public static int repositoriesHashCode(java.util.List<org.eclipse.aether.repository.RemoteRepository>)
meth public static java.lang.String getLayout(org.apache.maven.artifact.repository.ArtifactRepository)
meth public static java.util.Collection<org.eclipse.aether.artifact.Artifact> toArtifacts(java.util.Collection<org.apache.maven.artifact.Artifact>)
meth public static java.util.List<org.eclipse.aether.repository.RemoteRepository> toRepos(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public static org.apache.maven.artifact.Artifact toArtifact(org.eclipse.aether.artifact.Artifact)
meth public static org.apache.maven.artifact.handler.ArtifactHandler newHandler(org.eclipse.aether.artifact.Artifact)
meth public static org.eclipse.aether.artifact.Artifact toArtifact(org.apache.maven.artifact.Artifact)
meth public static org.eclipse.aether.artifact.ArtifactType newArtifactType(java.lang.String,org.apache.maven.artifact.handler.ArtifactHandler)
meth public static org.eclipse.aether.artifact.ArtifactTypeRegistry newArtifactTypeRegistry(org.apache.maven.artifact.handler.manager.ArtifactHandlerManager)
meth public static org.eclipse.aether.graph.Dependency toDependency(org.apache.maven.artifact.Artifact,java.util.Collection<org.apache.maven.model.Exclusion>)
meth public static org.eclipse.aether.graph.Dependency toDependency(org.apache.maven.model.Dependency,org.eclipse.aether.artifact.ArtifactTypeRegistry)
meth public static org.eclipse.aether.repository.RemoteRepository toRepo(org.apache.maven.artifact.repository.ArtifactRepository)
meth public static org.eclipse.aether.repository.WorkspaceRepository getWorkspace(org.eclipse.aether.RepositorySystemSession)
meth public static void toArtifacts(java.util.Collection<org.apache.maven.artifact.Artifact>,java.util.Collection<? extends org.eclipse.aether.graph.DependencyNode>,java.util.List<java.lang.String>,org.eclipse.aether.graph.DependencyFilter)
supr java.lang.Object
hcls MavenArtifactTypeRegistry

CLSS public abstract interface !annotation org.apache.maven.SessionScoped
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface org.apache.maven.artifact.Artifact
fld public final static java.lang.String LATEST_VERSION = "LATEST"
fld public final static java.lang.String RELEASE_VERSION = "RELEASE"
fld public final static java.lang.String SCOPE_COMPILE = "compile"
fld public final static java.lang.String SCOPE_COMPILE_PLUS_RUNTIME = "compile+runtime"
fld public final static java.lang.String SCOPE_IMPORT = "import"
fld public final static java.lang.String SCOPE_PROVIDED = "provided"
fld public final static java.lang.String SCOPE_RUNTIME = "runtime"
fld public final static java.lang.String SCOPE_RUNTIME_PLUS_SYSTEM = "runtime+system"
fld public final static java.lang.String SCOPE_SYSTEM = "system"
fld public final static java.lang.String SCOPE_TEST = "test"
fld public final static java.lang.String SNAPSHOT_VERSION = "SNAPSHOT"
fld public final static java.util.regex.Pattern VERSION_FILE_PATTERN
intf java.lang.Comparable<org.apache.maven.artifact.Artifact>
meth public abstract boolean hasClassifier()
meth public abstract boolean isOptional()
meth public abstract boolean isRelease()
meth public abstract boolean isResolved()
meth public abstract boolean isSelectedVersionKnown() throws org.apache.maven.artifact.versioning.OverConstrainedVersionException
meth public abstract boolean isSnapshot()
meth public abstract java.io.File getFile()
meth public abstract java.lang.String getArtifactId()
meth public abstract java.lang.String getBaseVersion()
meth public abstract java.lang.String getClassifier()
meth public abstract java.lang.String getDependencyConflictId()
meth public abstract java.lang.String getDownloadUrl()
meth public abstract java.lang.String getGroupId()
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getScope()
meth public abstract java.lang.String getType()
meth public abstract java.lang.String getVersion()
meth public abstract java.util.Collection<org.apache.maven.artifact.metadata.ArtifactMetadata> getMetadataList()
meth public abstract java.util.List<java.lang.String> getDependencyTrail()
meth public abstract java.util.List<org.apache.maven.artifact.versioning.ArtifactVersion> getAvailableVersions()
meth public abstract org.apache.maven.artifact.handler.ArtifactHandler getArtifactHandler()
meth public abstract org.apache.maven.artifact.repository.ArtifactRepository getRepository()
meth public abstract org.apache.maven.artifact.resolver.filter.ArtifactFilter getDependencyFilter()
meth public abstract org.apache.maven.artifact.versioning.ArtifactVersion getSelectedVersion() throws org.apache.maven.artifact.versioning.OverConstrainedVersionException
meth public abstract org.apache.maven.artifact.versioning.VersionRange getVersionRange()
meth public abstract void addMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata)
meth public abstract void selectVersion(java.lang.String)
meth public abstract void setArtifactHandler(org.apache.maven.artifact.handler.ArtifactHandler)
meth public abstract void setArtifactId(java.lang.String)
meth public abstract void setAvailableVersions(java.util.List<org.apache.maven.artifact.versioning.ArtifactVersion>)
meth public abstract void setBaseVersion(java.lang.String)
meth public abstract void setDependencyFilter(org.apache.maven.artifact.resolver.filter.ArtifactFilter)
meth public abstract void setDependencyTrail(java.util.List<java.lang.String>)
meth public abstract void setDownloadUrl(java.lang.String)
meth public abstract void setFile(java.io.File)
meth public abstract void setGroupId(java.lang.String)
meth public abstract void setOptional(boolean)
meth public abstract void setRelease(boolean)
meth public abstract void setRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public abstract void setResolved(boolean)
meth public abstract void setResolvedVersion(java.lang.String)
meth public abstract void setScope(java.lang.String)
meth public abstract void setVersion(java.lang.String)
meth public abstract void setVersionRange(org.apache.maven.artifact.versioning.VersionRange)
meth public abstract void updateVersion(java.lang.String,org.apache.maven.artifact.repository.ArtifactRepository)

CLSS public final !enum org.apache.maven.artifact.ArtifactScopeEnum
fld public final static org.apache.maven.artifact.ArtifactScopeEnum DEFAULT_SCOPE
fld public final static org.apache.maven.artifact.ArtifactScopeEnum compile
fld public final static org.apache.maven.artifact.ArtifactScopeEnum provided
fld public final static org.apache.maven.artifact.ArtifactScopeEnum runtime
fld public final static org.apache.maven.artifact.ArtifactScopeEnum runtime_plus_system
fld public final static org.apache.maven.artifact.ArtifactScopeEnum system
fld public final static org.apache.maven.artifact.ArtifactScopeEnum test
meth public boolean encloses(org.apache.maven.artifact.ArtifactScopeEnum)
meth public java.lang.String getScope()
meth public static org.apache.maven.artifact.ArtifactScopeEnum checkScope(org.apache.maven.artifact.ArtifactScopeEnum)
meth public static org.apache.maven.artifact.ArtifactScopeEnum valueOf(java.lang.String)
meth public static org.apache.maven.artifact.ArtifactScopeEnum[] values()
supr java.lang.Enum<org.apache.maven.artifact.ArtifactScopeEnum>
hfds COMPLIANCY_SETS,id

CLSS public final org.apache.maven.artifact.ArtifactStatus
fld public final static org.apache.maven.artifact.ArtifactStatus CONVERTED
fld public final static org.apache.maven.artifact.ArtifactStatus DEPLOYED
fld public final static org.apache.maven.artifact.ArtifactStatus GENERATED
fld public final static org.apache.maven.artifact.ArtifactStatus NONE
fld public final static org.apache.maven.artifact.ArtifactStatus PARTNER
fld public final static org.apache.maven.artifact.ArtifactStatus VERIFIED
intf java.lang.Comparable<org.apache.maven.artifact.ArtifactStatus>
meth public boolean equals(java.lang.Object)
meth public int compareTo(org.apache.maven.artifact.ArtifactStatus)
meth public int hashCode()
meth public java.lang.String toString()
meth public static org.apache.maven.artifact.ArtifactStatus valueOf(java.lang.String)
supr java.lang.Object
hfds key,map,rank

CLSS public final org.apache.maven.artifact.ArtifactUtils
cons public init()
meth public static <%0 extends java.lang.Object, %1 extends java.util.Map<{%%0},org.apache.maven.artifact.Artifact>> {%%1} copyArtifacts(java.util.Map<{%%0},? extends org.apache.maven.artifact.Artifact>,{%%1})
meth public static <%0 extends java.util.Collection<org.apache.maven.artifact.Artifact>> {%%0} copyArtifacts(java.util.Collection<org.apache.maven.artifact.Artifact>,{%%0})
meth public static boolean isSnapshot(java.lang.String)
meth public static java.lang.String key(java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String key(org.apache.maven.artifact.Artifact)
meth public static java.lang.String toSnapshotVersion(java.lang.String)
meth public static java.lang.String versionlessKey(java.lang.String,java.lang.String)
meth public static java.lang.String versionlessKey(org.apache.maven.artifact.Artifact)
meth public static java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact> artifactMapByVersionlessId(java.util.Collection<org.apache.maven.artifact.Artifact>)
meth public static org.apache.maven.artifact.Artifact copyArtifact(org.apache.maven.artifact.Artifact)
meth public static org.apache.maven.artifact.Artifact copyArtifactSafe(org.apache.maven.artifact.Artifact)
supr java.lang.Object

CLSS public org.apache.maven.artifact.DefaultArtifact
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.apache.maven.artifact.handler.ArtifactHandler)
cons public init(java.lang.String,java.lang.String,org.apache.maven.artifact.versioning.VersionRange,java.lang.String,java.lang.String,java.lang.String,org.apache.maven.artifact.handler.ArtifactHandler)
cons public init(java.lang.String,java.lang.String,org.apache.maven.artifact.versioning.VersionRange,java.lang.String,java.lang.String,java.lang.String,org.apache.maven.artifact.handler.ArtifactHandler,boolean)
intf org.apache.maven.artifact.Artifact
meth protected java.lang.String getBaseVersionInternal()
meth protected void setBaseVersionInternal(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public boolean hasClassifier()
meth public boolean isOptional()
meth public boolean isRelease()
meth public boolean isResolved()
meth public boolean isSelectedVersionKnown() throws org.apache.maven.artifact.versioning.OverConstrainedVersionException
meth public boolean isSnapshot()
meth public int compareTo(org.apache.maven.artifact.Artifact)
meth public int hashCode()
meth public java.io.File getFile()
meth public java.lang.String getArtifactId()
meth public java.lang.String getBaseVersion()
meth public java.lang.String getClassifier()
meth public java.lang.String getDependencyConflictId()
meth public java.lang.String getDownloadUrl()
meth public java.lang.String getGroupId()
meth public java.lang.String getId()
meth public java.lang.String getScope()
meth public java.lang.String getType()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public java.util.Collection<org.apache.maven.artifact.metadata.ArtifactMetadata> getMetadataList()
meth public java.util.List<java.lang.String> getDependencyTrail()
meth public java.util.List<org.apache.maven.artifact.versioning.ArtifactVersion> getAvailableVersions()
meth public org.apache.maven.artifact.handler.ArtifactHandler getArtifactHandler()
meth public org.apache.maven.artifact.repository.ArtifactRepository getRepository()
meth public org.apache.maven.artifact.resolver.filter.ArtifactFilter getDependencyFilter()
meth public org.apache.maven.artifact.versioning.ArtifactVersion getSelectedVersion() throws org.apache.maven.artifact.versioning.OverConstrainedVersionException
meth public org.apache.maven.artifact.versioning.VersionRange getVersionRange()
meth public void addMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata)
meth public void selectVersion(java.lang.String)
meth public void setArtifactHandler(org.apache.maven.artifact.handler.ArtifactHandler)
meth public void setArtifactId(java.lang.String)
meth public void setAvailableVersions(java.util.List<org.apache.maven.artifact.versioning.ArtifactVersion>)
meth public void setBaseVersion(java.lang.String)
meth public void setDependencyFilter(org.apache.maven.artifact.resolver.filter.ArtifactFilter)
meth public void setDependencyTrail(java.util.List<java.lang.String>)
meth public void setDownloadUrl(java.lang.String)
meth public void setFile(java.io.File)
meth public void setGroupId(java.lang.String)
meth public void setOptional(boolean)
meth public void setRelease(boolean)
meth public void setRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public void setResolved(boolean)
meth public void setResolvedVersion(java.lang.String)
meth public void setScope(java.lang.String)
meth public void setVersion(java.lang.String)
meth public void setVersionRange(org.apache.maven.artifact.versioning.VersionRange)
meth public void updateVersion(java.lang.String,org.apache.maven.artifact.repository.ArtifactRepository)
supr java.lang.Object
hfds artifactHandler,artifactId,availableVersions,baseVersion,classifier,dependencyFilter,dependencyTrail,downloadUrl,file,groupId,metadataMap,optional,release,repository,resolved,scope,type,version,versionRange

CLSS public org.apache.maven.artifact.DependencyResolutionRequiredException
cons public init(org.apache.maven.artifact.Artifact)
supr java.lang.Exception

CLSS public org.apache.maven.artifact.InvalidArtifactRTException
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Throwable)
meth public java.lang.String getArtifactId()
meth public java.lang.String getArtifactKey()
meth public java.lang.String getBaseMessage()
meth public java.lang.String getGroupId()
meth public java.lang.String getMessage()
meth public java.lang.String getType()
meth public java.lang.String getVersion()
supr java.lang.RuntimeException
hfds artifactId,baseMessage,groupId,type,version

CLSS public org.apache.maven.artifact.InvalidRepositoryException
cons protected init(java.lang.String,java.lang.String,org.codehaus.plexus.component.repository.exception.ComponentLookupException)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.net.MalformedURLException)
cons public init(java.lang.String,java.lang.Throwable)
 anno 0 java.lang.Deprecated()
meth public java.lang.String getRepositoryId()
supr java.lang.Exception
hfds repositoryId

CLSS public org.apache.maven.artifact.UnknownRepositoryLayoutException
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,org.codehaus.plexus.component.repository.exception.ComponentLookupException)
meth public java.lang.String getLayoutId()
supr org.apache.maven.artifact.InvalidRepositoryException
hfds layoutId

CLSS public abstract interface org.apache.maven.artifact.factory.ArtifactFactory
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String ROLE
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.artifact.Artifact createArtifact(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.apache.maven.artifact.Artifact createArtifactWithClassifier(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.apache.maven.artifact.Artifact createBuildArtifact(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.apache.maven.artifact.Artifact createDependencyArtifact(java.lang.String,java.lang.String,org.apache.maven.artifact.versioning.VersionRange,java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.apache.maven.artifact.Artifact createDependencyArtifact(java.lang.String,java.lang.String,org.apache.maven.artifact.versioning.VersionRange,java.lang.String,java.lang.String,java.lang.String,boolean)
meth public abstract org.apache.maven.artifact.Artifact createDependencyArtifact(java.lang.String,java.lang.String,org.apache.maven.artifact.versioning.VersionRange,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.apache.maven.artifact.Artifact createDependencyArtifact(java.lang.String,java.lang.String,org.apache.maven.artifact.versioning.VersionRange,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean)
meth public abstract org.apache.maven.artifact.Artifact createExtensionArtifact(java.lang.String,java.lang.String,org.apache.maven.artifact.versioning.VersionRange)
meth public abstract org.apache.maven.artifact.Artifact createParentArtifact(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.apache.maven.artifact.Artifact createPluginArtifact(java.lang.String,java.lang.String,org.apache.maven.artifact.versioning.VersionRange)
meth public abstract org.apache.maven.artifact.Artifact createProjectArtifact(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.apache.maven.artifact.Artifact createProjectArtifact(java.lang.String,java.lang.String,java.lang.String,java.lang.String)

CLSS public org.apache.maven.artifact.factory.DefaultArtifactFactory
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.artifact.factory.ArtifactFactory, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.artifact.factory.ArtifactFactory
meth public org.apache.maven.artifact.Artifact createArtifact(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public org.apache.maven.artifact.Artifact createArtifactWithClassifier(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public org.apache.maven.artifact.Artifact createBuildArtifact(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public org.apache.maven.artifact.Artifact createDependencyArtifact(java.lang.String,java.lang.String,org.apache.maven.artifact.versioning.VersionRange,java.lang.String,java.lang.String,java.lang.String)
meth public org.apache.maven.artifact.Artifact createDependencyArtifact(java.lang.String,java.lang.String,org.apache.maven.artifact.versioning.VersionRange,java.lang.String,java.lang.String,java.lang.String,boolean)
meth public org.apache.maven.artifact.Artifact createDependencyArtifact(java.lang.String,java.lang.String,org.apache.maven.artifact.versioning.VersionRange,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public org.apache.maven.artifact.Artifact createDependencyArtifact(java.lang.String,java.lang.String,org.apache.maven.artifact.versioning.VersionRange,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean)
meth public org.apache.maven.artifact.Artifact createExtensionArtifact(java.lang.String,java.lang.String,org.apache.maven.artifact.versioning.VersionRange)
meth public org.apache.maven.artifact.Artifact createParentArtifact(java.lang.String,java.lang.String,java.lang.String)
meth public org.apache.maven.artifact.Artifact createPluginArtifact(java.lang.String,java.lang.String,org.apache.maven.artifact.versioning.VersionRange)
meth public org.apache.maven.artifact.Artifact createProjectArtifact(java.lang.String,java.lang.String,java.lang.String)
meth public org.apache.maven.artifact.Artifact createProjectArtifact(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object
hfds artifactHandlerManager

CLSS public abstract interface org.apache.maven.artifact.handler.ArtifactHandler
fld public final static java.lang.String ROLE
meth public abstract boolean isAddedToClasspath()
meth public abstract boolean isIncludesDependencies()
meth public abstract java.lang.String getClassifier()
meth public abstract java.lang.String getDirectory()
meth public abstract java.lang.String getExtension()
meth public abstract java.lang.String getLanguage()
meth public abstract java.lang.String getPackaging()

CLSS public org.apache.maven.artifact.handler.DefaultArtifactHandler
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.artifact.handler.ArtifactHandler, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
cons public init(java.lang.String)
intf org.apache.maven.artifact.handler.ArtifactHandler
meth public boolean isAddedToClasspath()
meth public boolean isIncludesDependencies()
meth public java.lang.String getClassifier()
meth public java.lang.String getDirectory()
meth public java.lang.String getExtension()
meth public java.lang.String getLanguage()
meth public java.lang.String getPackaging()
meth public java.lang.String getType()
meth public void setAddedToClasspath(boolean)
meth public void setExtension(java.lang.String)
meth public void setIncludesDependencies(boolean)
meth public void setLanguage(java.lang.String)
supr java.lang.Object
hfds addedToClasspath,classifier,directory,extension,includesDependencies,language,packaging,type

CLSS public abstract interface org.apache.maven.artifact.handler.manager.ArtifactHandlerManager
fld public final static java.lang.String ROLE
meth public abstract org.apache.maven.artifact.handler.ArtifactHandler getArtifactHandler(java.lang.String)
meth public abstract void addHandlers(java.util.Map<java.lang.String,org.apache.maven.artifact.handler.ArtifactHandler>)
 anno 0 java.lang.Deprecated()

CLSS public org.apache.maven.artifact.handler.manager.DefaultArtifactHandlerManager
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.artifact.handler.manager.ArtifactHandlerManager, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.artifact.handler.manager.ArtifactHandlerManager
meth public java.util.Set<java.lang.String> getHandlerTypes()
 anno 0 java.lang.Deprecated()
meth public org.apache.maven.artifact.handler.ArtifactHandler getArtifactHandler(java.lang.String)
meth public void addHandlers(java.util.Map<java.lang.String,org.apache.maven.artifact.handler.ArtifactHandler>)
supr java.lang.Object
hfds allHandlers,artifactHandlers

CLSS public abstract org.apache.maven.artifact.metadata.AbstractArtifactMetadata
 anno 0 java.lang.Deprecated()
cons protected init(org.apache.maven.artifact.Artifact)
intf org.apache.maven.artifact.metadata.ArtifactMetadata
supr org.apache.maven.repository.legacy.metadata.AbstractArtifactMetadata

CLSS public abstract interface org.apache.maven.artifact.metadata.ArtifactMetadata
 anno 0 java.lang.Deprecated()
intf org.apache.maven.repository.legacy.metadata.ArtifactMetadata
meth public abstract void merge(org.apache.maven.artifact.metadata.ArtifactMetadata)

CLSS public org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.Throwable)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.Throwable,org.apache.maven.artifact.Artifact)
cons public init(java.lang.Throwable)
 anno 0 java.lang.Deprecated()
supr org.apache.maven.repository.legacy.metadata.ArtifactMetadataRetrievalException

CLSS public abstract interface org.apache.maven.artifact.metadata.ArtifactMetadataSource
 anno 0 java.lang.Deprecated()
intf org.apache.maven.repository.legacy.metadata.ArtifactMetadataSource
meth public abstract java.util.List<org.apache.maven.artifact.versioning.ArtifactVersion> retrieveAvailableVersions(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>) throws org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException
meth public abstract java.util.List<org.apache.maven.artifact.versioning.ArtifactVersion> retrieveAvailableVersions(org.apache.maven.repository.legacy.metadata.MetadataResolutionRequest) throws org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException
meth public abstract java.util.List<org.apache.maven.artifact.versioning.ArtifactVersion> retrieveAvailableVersionsFromDeploymentRepository(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException
meth public abstract org.apache.maven.artifact.metadata.ResolutionGroup retrieve(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>) throws org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException
meth public abstract org.apache.maven.artifact.metadata.ResolutionGroup retrieve(org.apache.maven.repository.legacy.metadata.MetadataResolutionRequest) throws org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException

CLSS public org.apache.maven.artifact.metadata.ResolutionGroup
 anno 0 java.lang.Deprecated()
cons public init(org.apache.maven.artifact.Artifact,java.util.Set<org.apache.maven.artifact.Artifact>,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
cons public init(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact,java.util.Set<org.apache.maven.artifact.Artifact>,java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
supr org.apache.maven.repository.legacy.metadata.ResolutionGroup

CLSS public abstract interface org.apache.maven.artifact.repository.ArtifactRepository
 anno 0 java.lang.Deprecated()
meth public abstract boolean isBlacklisted()
 anno 0 java.lang.Deprecated()
meth public abstract boolean isBlocked()
meth public abstract boolean isProjectAware()
meth public abstract boolean isUniqueVersion()
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.String getBasedir()
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getKey()
meth public abstract java.lang.String getProtocol()
meth public abstract java.lang.String getUrl()
meth public abstract java.lang.String pathOf(org.apache.maven.artifact.Artifact)
meth public abstract java.lang.String pathOfLocalRepositoryMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository)
meth public abstract java.lang.String pathOfRemoteRepositoryMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata)
meth public abstract java.util.List<java.lang.String> findVersions(org.apache.maven.artifact.Artifact)
meth public abstract java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getMirroredRepositories()
meth public abstract org.apache.maven.artifact.Artifact find(org.apache.maven.artifact.Artifact)
meth public abstract org.apache.maven.artifact.repository.ArtifactRepositoryPolicy getReleases()
meth public abstract org.apache.maven.artifact.repository.ArtifactRepositoryPolicy getSnapshots()
meth public abstract org.apache.maven.artifact.repository.Authentication getAuthentication()
meth public abstract org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout getLayout()
meth public abstract org.apache.maven.repository.Proxy getProxy()
meth public abstract void setAuthentication(org.apache.maven.artifact.repository.Authentication)
meth public abstract void setBlacklisted(boolean)
 anno 0 java.lang.Deprecated()
meth public abstract void setBlocked(boolean)
meth public abstract void setId(java.lang.String)
meth public abstract void setLayout(org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout)
meth public abstract void setMirroredRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public abstract void setProxy(org.apache.maven.repository.Proxy)
meth public abstract void setReleaseUpdatePolicy(org.apache.maven.artifact.repository.ArtifactRepositoryPolicy)
meth public abstract void setSnapshotUpdatePolicy(org.apache.maven.artifact.repository.ArtifactRepositoryPolicy)
meth public abstract void setUrl(java.lang.String)

CLSS public abstract interface org.apache.maven.artifact.repository.ArtifactRepositoryFactory
fld public final static java.lang.String DEFAULT_LAYOUT_ID = "default"
fld public final static java.lang.String LOCAL_REPOSITORY_ID = "local"
fld public final static java.lang.String ROLE
meth public abstract org.apache.maven.artifact.repository.ArtifactRepository createArtifactRepository(java.lang.String,java.lang.String,java.lang.String,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy) throws org.apache.maven.artifact.UnknownRepositoryLayoutException
meth public abstract org.apache.maven.artifact.repository.ArtifactRepository createArtifactRepository(java.lang.String,java.lang.String,org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy)
meth public abstract org.apache.maven.artifact.repository.ArtifactRepository createDeploymentArtifactRepository(java.lang.String,java.lang.String,java.lang.String,boolean) throws org.apache.maven.artifact.UnknownRepositoryLayoutException
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.artifact.repository.ArtifactRepository createDeploymentArtifactRepository(java.lang.String,java.lang.String,org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout,boolean)
meth public abstract org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout getLayout(java.lang.String) throws org.apache.maven.artifact.UnknownRepositoryLayoutException
 anno 0 java.lang.Deprecated()
meth public abstract void setGlobalChecksumPolicy(java.lang.String)
meth public abstract void setGlobalUpdatePolicy(java.lang.String)

CLSS public org.apache.maven.artifact.repository.ArtifactRepositoryPolicy
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(boolean,java.lang.String,java.lang.String)
cons public init(org.apache.maven.artifact.repository.ArtifactRepositoryPolicy)
fld public final static java.lang.String CHECKSUM_POLICY_FAIL = "fail"
fld public final static java.lang.String CHECKSUM_POLICY_IGNORE = "ignore"
fld public final static java.lang.String CHECKSUM_POLICY_WARN = "warn"
fld public final static java.lang.String UPDATE_POLICY_ALWAYS = "always"
fld public final static java.lang.String UPDATE_POLICY_DAILY = "daily"
fld public final static java.lang.String UPDATE_POLICY_INTERVAL = "interval"
fld public final static java.lang.String UPDATE_POLICY_NEVER = "never"
meth public boolean checkOutOfDate(java.util.Date)
meth public boolean isEnabled()
meth public java.lang.String getChecksumPolicy()
meth public java.lang.String getUpdatePolicy()
meth public java.lang.String toString()
meth public void merge(org.apache.maven.artifact.repository.ArtifactRepositoryPolicy)
meth public void setChecksumPolicy(java.lang.String)
meth public void setEnabled(boolean)
meth public void setUpdatePolicy(java.lang.String)
supr java.lang.Object
hfds checksumPolicy,enabled,updatePolicy

CLSS public org.apache.maven.artifact.repository.Authentication
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getPassphrase()
meth public java.lang.String getPassword()
meth public java.lang.String getPrivateKey()
meth public java.lang.String getUsername()
meth public void setPassphrase(java.lang.String)
meth public void setPassword(java.lang.String)
meth public void setPrivateKey(java.lang.String)
meth public void setUsername(java.lang.String)
supr java.lang.Object
hfds passphrase,password,privateKey,username

CLSS public org.apache.maven.artifact.repository.DefaultArtifactRepository
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String,org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout)
cons public init(java.lang.String,java.lang.String,org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout,boolean)
cons public init(java.lang.String,java.lang.String,org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy)
intf org.apache.maven.artifact.repository.ArtifactRepository
meth public boolean isBlacklisted()
meth public boolean isBlocked()
meth public boolean isProjectAware()
meth public boolean isUniqueVersion()
meth public java.lang.String getKey()
meth public java.lang.String pathOf(org.apache.maven.artifact.Artifact)
meth public java.lang.String pathOfLocalRepositoryMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository)
meth public java.lang.String pathOfRemoteRepositoryMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata)
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> findVersions(org.apache.maven.artifact.Artifact)
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getMirroredRepositories()
meth public org.apache.maven.artifact.Artifact find(org.apache.maven.artifact.Artifact)
meth public org.apache.maven.artifact.repository.ArtifactRepositoryPolicy getReleases()
meth public org.apache.maven.artifact.repository.ArtifactRepositoryPolicy getSnapshots()
meth public org.apache.maven.artifact.repository.Authentication getAuthentication()
meth public org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout getLayout()
meth public org.apache.maven.repository.Proxy getProxy()
meth public void setAuthentication(org.apache.maven.artifact.repository.Authentication)
meth public void setBlacklisted(boolean)
meth public void setBlocked(boolean)
meth public void setLayout(org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout)
meth public void setMirroredRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public void setProxy(org.apache.maven.repository.Proxy)
meth public void setReleaseUpdatePolicy(org.apache.maven.artifact.repository.ArtifactRepositoryPolicy)
meth public void setSnapshotUpdatePolicy(org.apache.maven.artifact.repository.ArtifactRepositoryPolicy)
supr org.apache.maven.wagon.repository.Repository
hfds authentication,blacklisted,blocked,layout,mirroredRepositories,proxy,releases,snapshots

CLSS public org.apache.maven.artifact.repository.DefaultArtifactRepositoryFactory
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.artifact.repository.ArtifactRepositoryFactory, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.artifact.repository.ArtifactRepositoryFactory
meth public org.apache.maven.artifact.repository.ArtifactRepository createArtifactRepository(java.lang.String,java.lang.String,java.lang.String,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy) throws org.apache.maven.artifact.UnknownRepositoryLayoutException
meth public org.apache.maven.artifact.repository.ArtifactRepository createArtifactRepository(java.lang.String,java.lang.String,org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy)
meth public org.apache.maven.artifact.repository.ArtifactRepository createDeploymentArtifactRepository(java.lang.String,java.lang.String,java.lang.String,boolean) throws org.apache.maven.artifact.UnknownRepositoryLayoutException
meth public org.apache.maven.artifact.repository.ArtifactRepository createDeploymentArtifactRepository(java.lang.String,java.lang.String,org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout,boolean)
meth public org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout getLayout(java.lang.String) throws org.apache.maven.artifact.UnknownRepositoryLayoutException
meth public void setGlobalChecksumPolicy(java.lang.String)
meth public void setGlobalUpdatePolicy(java.lang.String)
supr java.lang.Object
hfds factory,legacySupport,repositorySystem

CLSS public org.apache.maven.artifact.repository.DefaultRepositoryRequest
cons public init()
cons public init(org.apache.maven.artifact.repository.RepositoryRequest)
intf org.apache.maven.artifact.repository.RepositoryRequest
meth public boolean isForceUpdate()
meth public boolean isOffline()
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getRemoteRepositories()
meth public org.apache.maven.artifact.repository.ArtifactRepository getLocalRepository()
meth public org.apache.maven.artifact.repository.DefaultRepositoryRequest setForceUpdate(boolean)
meth public org.apache.maven.artifact.repository.DefaultRepositoryRequest setLocalRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public org.apache.maven.artifact.repository.DefaultRepositoryRequest setOffline(boolean)
meth public org.apache.maven.artifact.repository.DefaultRepositoryRequest setRemoteRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public static org.apache.maven.artifact.repository.RepositoryRequest getRepositoryRequest(org.apache.maven.execution.MavenSession,org.apache.maven.project.MavenProject)
supr java.lang.Object
hfds forceUpdate,localRepository,offline,remoteRepositories

CLSS public org.apache.maven.artifact.repository.LegacyLocalRepositoryManager
intf org.eclipse.aether.repository.LocalRepositoryManager
meth public java.lang.String getPathForLocalArtifact(org.eclipse.aether.artifact.Artifact)
meth public java.lang.String getPathForLocalMetadata(org.eclipse.aether.metadata.Metadata)
meth public java.lang.String getPathForRemoteArtifact(org.eclipse.aether.artifact.Artifact,org.eclipse.aether.repository.RemoteRepository,java.lang.String)
meth public java.lang.String getPathForRemoteMetadata(org.eclipse.aether.metadata.Metadata,org.eclipse.aether.repository.RemoteRepository,java.lang.String)
meth public org.eclipse.aether.repository.LocalArtifactResult find(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.LocalArtifactRequest)
meth public org.eclipse.aether.repository.LocalMetadataResult find(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.LocalMetadataRequest)
meth public org.eclipse.aether.repository.LocalRepository getRepository()
meth public static org.eclipse.aether.RepositorySystemSession overlay(org.apache.maven.artifact.repository.ArtifactRepository,org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.RepositorySystem)
meth public void add(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.LocalArtifactRegistration)
meth public void add(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.LocalMetadataRegistration)
supr java.lang.Object
hfds delegate,realLocalRepo,repo
hcls ArtifactMetadataAdapter,ArtifactRepositoryAdapter

CLSS public org.apache.maven.artifact.repository.MavenArtifactRepository
cons public init()
cons public init(java.lang.String,java.lang.String,org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy)
intf org.apache.maven.artifact.repository.ArtifactRepository
meth protected static <%0 extends java.lang.Object> boolean eq({%%0},{%%0})
meth public boolean equals(java.lang.Object)
meth public boolean isBlacklisted()
meth public boolean isBlocked()
meth public boolean isProjectAware()
meth public boolean isUniqueVersion()
meth public int hashCode()
meth public java.lang.String getBasedir()
meth public java.lang.String getId()
meth public java.lang.String getKey()
meth public java.lang.String getProtocol()
meth public java.lang.String getUrl()
meth public java.lang.String pathOf(org.apache.maven.artifact.Artifact)
meth public java.lang.String pathOfLocalRepositoryMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository)
meth public java.lang.String pathOfRemoteRepositoryMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata)
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> findVersions(org.apache.maven.artifact.Artifact)
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getMirroredRepositories()
meth public org.apache.maven.artifact.Artifact find(org.apache.maven.artifact.Artifact)
meth public org.apache.maven.artifact.repository.ArtifactRepositoryPolicy getReleases()
meth public org.apache.maven.artifact.repository.ArtifactRepositoryPolicy getSnapshots()
meth public org.apache.maven.artifact.repository.Authentication getAuthentication()
meth public org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout getLayout()
meth public org.apache.maven.repository.Proxy getProxy()
meth public void setAuthentication(org.apache.maven.artifact.repository.Authentication)
meth public void setBlacklisted(boolean)
meth public void setBlocked(boolean)
meth public void setId(java.lang.String)
meth public void setLayout(org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout)
meth public void setMirroredRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public void setProxy(org.apache.maven.repository.Proxy)
meth public void setReleaseUpdatePolicy(org.apache.maven.artifact.repository.ArtifactRepositoryPolicy)
meth public void setSnapshotUpdatePolicy(org.apache.maven.artifact.repository.ArtifactRepositoryPolicy)
meth public void setUrl(java.lang.String)
supr java.lang.Object
hfds authentication,basedir,blocked,id,layout,mirroredRepositories,protocol,proxy,releases,snapshots,url

CLSS public abstract interface org.apache.maven.artifact.repository.RepositoryCache
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.Object get(org.apache.maven.artifact.repository.RepositoryRequest,java.lang.Object)
meth public abstract void put(org.apache.maven.artifact.repository.RepositoryRequest,java.lang.Object,java.lang.Object)

CLSS public abstract interface org.apache.maven.artifact.repository.RepositoryRequest
meth public abstract boolean isForceUpdate()
meth public abstract boolean isOffline()
meth public abstract java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getRemoteRepositories()
meth public abstract org.apache.maven.artifact.repository.ArtifactRepository getLocalRepository()
meth public abstract org.apache.maven.artifact.repository.RepositoryRequest setForceUpdate(boolean)
meth public abstract org.apache.maven.artifact.repository.RepositoryRequest setLocalRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public abstract org.apache.maven.artifact.repository.RepositoryRequest setOffline(boolean)
meth public abstract org.apache.maven.artifact.repository.RepositoryRequest setRemoteRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)

CLSS public abstract interface org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String ROLE
meth public abstract java.lang.String getId()
meth public abstract java.lang.String pathOf(org.apache.maven.artifact.Artifact)
meth public abstract java.lang.String pathOfLocalRepositoryMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository)
meth public abstract java.lang.String pathOfRemoteRepositoryMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata)

CLSS public abstract interface org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout2
intf org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout
meth public abstract org.apache.maven.artifact.repository.ArtifactRepository newMavenArtifactRepository(java.lang.String,java.lang.String,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy)

CLSS public org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="default", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout
meth public java.lang.String getId()
meth public java.lang.String pathOf(org.apache.maven.artifact.Artifact)
meth public java.lang.String pathOfLocalRepositoryMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository)
meth public java.lang.String pathOfRemoteRepositoryMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata)
meth public java.lang.String toString()
supr java.lang.Object
hfds ARTIFACT_SEPARATOR,GROUP_SEPARATOR,PATH_SEPARATOR

CLSS public org.apache.maven.artifact.repository.layout.FlatRepositoryLayout
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="flat", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout
meth public java.lang.String getId()
meth public java.lang.String pathOf(org.apache.maven.artifact.Artifact)
meth public java.lang.String pathOfLocalRepositoryMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository)
meth public java.lang.String pathOfRemoteRepositoryMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata)
meth public java.lang.String toString()
supr java.lang.Object
hfds ARTIFACT_SEPARATOR,GROUP_SEPARATOR

CLSS public abstract org.apache.maven.artifact.repository.metadata.AbstractRepositoryMetadata
cons protected init(org.apache.maven.artifact.repository.metadata.Metadata)
intf org.apache.maven.artifact.repository.metadata.RepositoryMetadata
meth protected static org.apache.maven.artifact.repository.metadata.Metadata createMetadata(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.metadata.Versioning)
meth protected static org.apache.maven.artifact.repository.metadata.Versioning createVersioning(org.apache.maven.artifact.repository.metadata.Snapshot)
meth protected void updateRepositoryMetadata(org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.repository.ArtifactRepository) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public int getNature()
meth public java.lang.String extendedToString()
meth public java.lang.String getLocalFilename(org.apache.maven.artifact.repository.ArtifactRepository)
meth public java.lang.String getRemoteFilename()
meth public java.lang.String toString()
meth public org.apache.maven.artifact.repository.ArtifactRepositoryPolicy getPolicy(org.apache.maven.artifact.repository.ArtifactRepository)
meth public org.apache.maven.artifact.repository.metadata.Metadata getMetadata()
meth public void merge(org.apache.maven.artifact.metadata.ArtifactMetadata)
meth public void merge(org.apache.maven.repository.legacy.metadata.ArtifactMetadata)
meth public void setMetadata(org.apache.maven.artifact.repository.metadata.Metadata)
meth public void storeInLocalRepository(org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.repository.metadata.RepositoryMetadataStoreException
supr java.lang.Object
hfds metadata

CLSS public org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata
cons public init(org.apache.maven.artifact.Artifact)
cons public init(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.metadata.Versioning)
meth public boolean isSnapshot()
meth public boolean storedInArtifactVersionDirectory()
meth public boolean storedInGroupDirectory()
meth public int getNature()
meth public java.lang.Object getKey()
meth public java.lang.String getArtifactId()
meth public java.lang.String getBaseVersion()
meth public java.lang.String getGroupId()
meth public org.apache.maven.artifact.repository.ArtifactRepository getRepository()
meth public void setRepository(org.apache.maven.artifact.repository.ArtifactRepository)
supr org.apache.maven.artifact.repository.metadata.AbstractRepositoryMetadata
hfds artifact

CLSS public org.apache.maven.artifact.repository.metadata.DefaultRepositoryMetadataManager
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager
meth protected org.apache.maven.artifact.repository.metadata.Metadata readMetadata(java.io.File) throws org.apache.maven.artifact.repository.metadata.RepositoryMetadataReadException
meth public void deploy(org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.repository.metadata.RepositoryMetadataDeploymentException
meth public void install(org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.repository.metadata.RepositoryMetadataInstallationException
meth public void resolve(org.apache.maven.artifact.repository.metadata.RepositoryMetadata,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException
meth public void resolve(org.apache.maven.artifact.repository.metadata.RepositoryMetadata,org.apache.maven.artifact.repository.RepositoryRequest) throws org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException
meth public void resolveAlways(org.apache.maven.artifact.repository.metadata.RepositoryMetadata,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException
supr org.codehaus.plexus.logging.AbstractLogEnabled
hfds updateCheckManager,wagonManager

CLSS public org.apache.maven.artifact.repository.metadata.GroupRepositoryMetadata
cons public init(java.lang.String)
meth public boolean isSnapshot()
meth public boolean storedInArtifactVersionDirectory()
meth public boolean storedInGroupDirectory()
meth public java.lang.Object getKey()
meth public java.lang.String getArtifactId()
meth public java.lang.String getBaseVersion()
meth public java.lang.String getGroupId()
meth public org.apache.maven.artifact.repository.ArtifactRepository getRepository()
meth public void addPluginMapping(java.lang.String,java.lang.String)
meth public void addPluginMapping(java.lang.String,java.lang.String,java.lang.String)
meth public void setRepository(org.apache.maven.artifact.repository.ArtifactRepository)
supr org.apache.maven.artifact.repository.metadata.AbstractRepositoryMetadata
hfds groupId

CLSS public org.apache.maven.artifact.repository.metadata.Metadata
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean merge(org.apache.maven.artifact.repository.metadata.Metadata)
meth public java.lang.String getArtifactId()
meth public java.lang.String getGroupId()
meth public java.lang.String getModelEncoding()
meth public java.lang.String getModelVersion()
meth public java.lang.String getVersion()
meth public java.util.List<org.apache.maven.artifact.repository.metadata.Plugin> getPlugins()
meth public org.apache.maven.artifact.repository.metadata.Metadata clone()
meth public org.apache.maven.artifact.repository.metadata.Versioning getVersioning()
meth public void addPlugin(org.apache.maven.artifact.repository.metadata.Plugin)
meth public void removePlugin(org.apache.maven.artifact.repository.metadata.Plugin)
meth public void setArtifactId(java.lang.String)
meth public void setGroupId(java.lang.String)
meth public void setModelEncoding(java.lang.String)
meth public void setModelVersion(java.lang.String)
meth public void setPlugins(java.util.List<org.apache.maven.artifact.repository.metadata.Plugin>)
meth public void setVersion(java.lang.String)
meth public void setVersioning(org.apache.maven.artifact.repository.metadata.Versioning)
supr java.lang.Object
hfds artifactId,groupId,modelEncoding,modelVersion,plugins,version,versioning

CLSS public final org.apache.maven.artifact.repository.metadata.MetadataBridge
cons public init(org.apache.maven.artifact.metadata.ArtifactMetadata)
intf org.eclipse.aether.metadata.MergeableMetadata
meth public boolean isMerged()
meth public java.io.File getFile()
meth public java.lang.String getArtifactId()
meth public java.lang.String getGroupId()
meth public java.lang.String getType()
meth public java.lang.String getVersion()
meth public java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public org.apache.maven.artifact.repository.metadata.MetadataBridge setFile(java.io.File)
meth public org.eclipse.aether.metadata.Metadata setProperties(java.util.Map<java.lang.String,java.lang.String>)
meth public org.eclipse.aether.metadata.Metadata$Nature getNature()
meth public void merge(java.io.File,java.io.File) throws org.eclipse.aether.RepositoryException
supr org.eclipse.aether.metadata.AbstractMetadata
hfds merged,metadata
hcls MetadataRepository

CLSS public org.apache.maven.artifact.repository.metadata.Plugin
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.String getArtifactId()
meth public java.lang.String getName()
meth public java.lang.String getPrefix()
meth public org.apache.maven.artifact.repository.metadata.Plugin clone()
meth public void setArtifactId(java.lang.String)
meth public void setName(java.lang.String)
meth public void setPrefix(java.lang.String)
supr java.lang.Object
hfds artifactId,name,prefix

CLSS public abstract interface org.apache.maven.artifact.repository.metadata.RepositoryMetadata
fld public final static int RELEASE = 1
fld public final static int RELEASE_OR_SNAPSHOT = 3
fld public final static int SNAPSHOT = 2
intf org.apache.maven.artifact.metadata.ArtifactMetadata
meth public abstract boolean isSnapshot()
meth public abstract int getNature()
meth public abstract org.apache.maven.artifact.repository.ArtifactRepository getRepository()
meth public abstract org.apache.maven.artifact.repository.ArtifactRepositoryPolicy getPolicy(org.apache.maven.artifact.repository.ArtifactRepository)
meth public abstract org.apache.maven.artifact.repository.metadata.Metadata getMetadata()
meth public abstract void setMetadata(org.apache.maven.artifact.repository.metadata.Metadata)
meth public abstract void setRepository(org.apache.maven.artifact.repository.ArtifactRepository)

CLSS public org.apache.maven.artifact.repository.metadata.RepositoryMetadataDeploymentException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
supr java.lang.Throwable

CLSS public org.apache.maven.artifact.repository.metadata.RepositoryMetadataInstallationException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
supr java.lang.Throwable

CLSS public abstract interface org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager
meth public abstract void deploy(org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.repository.metadata.RepositoryMetadataDeploymentException
meth public abstract void install(org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.repository.metadata.RepositoryMetadataInstallationException
meth public abstract void resolve(org.apache.maven.artifact.repository.metadata.RepositoryMetadata,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException
meth public abstract void resolve(org.apache.maven.artifact.repository.metadata.RepositoryMetadata,org.apache.maven.artifact.repository.RepositoryRequest) throws org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException
meth public abstract void resolveAlways(org.apache.maven.artifact.repository.metadata.RepositoryMetadata,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException

CLSS public org.apache.maven.artifact.repository.metadata.RepositoryMetadataReadException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
supr java.lang.Exception

CLSS public org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
supr java.lang.Exception

CLSS public org.apache.maven.artifact.repository.metadata.RepositoryMetadataStoreException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
supr java.lang.Exception

CLSS public org.apache.maven.artifact.repository.metadata.Snapshot
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean isLocalCopy()
meth public int getBuildNumber()
meth public java.lang.String getTimestamp()
meth public org.apache.maven.artifact.repository.metadata.Snapshot clone()
meth public void setBuildNumber(int)
meth public void setLocalCopy(boolean)
meth public void setTimestamp(java.lang.String)
supr java.lang.Object
hfds buildNumber,localCopy,timestamp

CLSS public org.apache.maven.artifact.repository.metadata.SnapshotArtifactRepositoryMetadata
cons public init(org.apache.maven.artifact.Artifact)
cons public init(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.metadata.Snapshot)
meth public boolean isSnapshot()
meth public boolean storedInArtifactVersionDirectory()
meth public boolean storedInGroupDirectory()
meth public int getNature()
meth public java.lang.Object getKey()
meth public java.lang.String getArtifactId()
meth public java.lang.String getBaseVersion()
meth public java.lang.String getGroupId()
meth public org.apache.maven.artifact.repository.ArtifactRepository getRepository()
meth public void setRepository(org.apache.maven.artifact.repository.ArtifactRepository)
supr org.apache.maven.artifact.repository.metadata.AbstractRepositoryMetadata
hfds artifact

CLSS public org.apache.maven.artifact.repository.metadata.SnapshotVersion
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getClassifier()
meth public java.lang.String getExtension()
meth public java.lang.String getUpdated()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public org.apache.maven.artifact.repository.metadata.SnapshotVersion clone()
meth public void setClassifier(java.lang.String)
meth public void setExtension(java.lang.String)
meth public void setUpdated(java.lang.String)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds classifier,extension,updated,version

CLSS public org.apache.maven.artifact.repository.metadata.Versioning
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.String getLastUpdated()
meth public java.lang.String getLatest()
meth public java.lang.String getRelease()
meth public java.util.List<java.lang.String> getVersions()
meth public java.util.List<org.apache.maven.artifact.repository.metadata.SnapshotVersion> getSnapshotVersions()
meth public org.apache.maven.artifact.repository.metadata.Snapshot getSnapshot()
meth public org.apache.maven.artifact.repository.metadata.Versioning clone()
meth public void addSnapshotVersion(org.apache.maven.artifact.repository.metadata.SnapshotVersion)
meth public void addVersion(java.lang.String)
meth public void removeSnapshotVersion(org.apache.maven.artifact.repository.metadata.SnapshotVersion)
meth public void removeVersion(java.lang.String)
meth public void setLastUpdated(java.lang.String)
meth public void setLastUpdatedTimestamp(java.util.Date)
meth public void setLatest(java.lang.String)
meth public void setRelease(java.lang.String)
meth public void setSnapshot(org.apache.maven.artifact.repository.metadata.Snapshot)
meth public void setSnapshotVersions(java.util.List<org.apache.maven.artifact.repository.metadata.SnapshotVersion>)
meth public void setVersions(java.util.List<java.lang.String>)
meth public void updateTimestamp()
supr java.lang.Object
hfds lastUpdated,latest,release,snapshot,snapshotVersions,versions

CLSS public org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader
cons public init()
cons public init(org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader$ContentTransformer)
fld public final org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader$ContentTransformer contentTransformer
innr public abstract interface static ContentTransformer
meth public boolean getAddDefaultEntities()
meth public org.apache.maven.artifact.repository.metadata.Metadata read(java.io.InputStream) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.artifact.repository.metadata.Metadata read(java.io.InputStream,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.artifact.repository.metadata.Metadata read(java.io.Reader) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.artifact.repository.metadata.Metadata read(java.io.Reader,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.artifact.repository.metadata.Metadata read(org.codehaus.plexus.util.xml.pull.XmlPullParser,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public void setAddDefaultEntities(boolean)
supr java.lang.Object
hfds addDefaultEntities

CLSS public abstract interface static org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader$ContentTransformer
 outer org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader
meth public abstract java.lang.String transform(java.lang.String,java.lang.String)

CLSS public org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer
cons public init()
meth public void setFileComment(java.lang.String)
meth public void write(java.io.OutputStream,org.apache.maven.artifact.repository.metadata.Metadata) throws java.io.IOException
meth public void write(java.io.Writer,org.apache.maven.artifact.repository.metadata.Metadata) throws java.io.IOException
supr java.lang.Object
hfds NAMESPACE,fileComment

CLSS public org.apache.maven.artifact.resolver.AbstractArtifactResolutionException
cons protected init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,java.util.List<java.lang.String>)
cons protected init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,java.util.List<java.lang.String>,java.lang.Throwable)
cons protected init(java.lang.String,org.apache.maven.artifact.Artifact)
cons protected init(java.lang.String,org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
cons protected init(java.lang.String,org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,java.lang.Throwable)
meth protected static java.lang.String constructArtifactPath(java.util.List<java.lang.String>,java.lang.String)
meth protected static java.lang.String constructMissingArtifactMessage(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.List<java.lang.String>)
meth public java.lang.String getArtifactId()
meth public java.lang.String getArtifactPath()
meth public java.lang.String getClassifier()
meth public java.lang.String getGroupId()
meth public java.lang.String getOriginalMessage()
meth public java.lang.String getPath()
meth public java.lang.String getType()
meth public java.lang.String getVersion()
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getRemoteRepositories()
meth public org.apache.maven.artifact.Artifact getArtifact()
supr java.lang.Exception
hfds LS,artifact,artifactId,classifier,groupId,originalMessage,path,remoteRepositories,type,version

CLSS public abstract interface org.apache.maven.artifact.resolver.ArtifactCollector
 anno 0 java.lang.Deprecated()
intf org.apache.maven.repository.legacy.resolver.LegacyArtifactCollector
meth public abstract org.apache.maven.artifact.resolver.ArtifactResolutionResult collect(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter,java.util.List<org.apache.maven.artifact.resolver.ResolutionListener>) throws org.apache.maven.artifact.resolver.ArtifactResolutionException
 anno 0 java.lang.Deprecated()

CLSS public org.apache.maven.artifact.resolver.ArtifactNotFoundException
cons protected init(java.lang.String,org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
cons protected init(java.lang.String,org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,java.lang.Throwable)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,java.lang.String,java.util.List<java.lang.String>,java.lang.Throwable)
cons public init(java.lang.String,org.apache.maven.artifact.Artifact)
meth public java.lang.String getDownloadUrl()
supr org.apache.maven.artifact.resolver.AbstractArtifactResolutionException
hfds downloadUrl

CLSS public org.apache.maven.artifact.resolver.ArtifactResolutionException
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,java.util.List<java.lang.String>,java.lang.Throwable)
cons public init(java.lang.String,org.apache.maven.artifact.Artifact)
cons public init(java.lang.String,org.apache.maven.artifact.Artifact,java.lang.Throwable)
cons public init(java.lang.String,org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
cons public init(java.lang.String,org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,java.lang.Throwable)
supr org.apache.maven.artifact.resolver.AbstractArtifactResolutionException

CLSS public org.apache.maven.artifact.resolver.ArtifactResolutionRequest
cons public init()
cons public init(org.apache.maven.artifact.repository.RepositoryRequest)
intf org.apache.maven.artifact.repository.RepositoryRequest
meth public boolean isForceUpdate()
meth public boolean isOffline()
meth public boolean isResolveRoot()
meth public boolean isResolveTransitively()
meth public java.lang.String toString()
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getRemoteRepositories()
meth public java.util.List<org.apache.maven.artifact.resolver.ResolutionListener> getListeners()
meth public java.util.List<org.apache.maven.settings.Mirror> getMirrors()
meth public java.util.List<org.apache.maven.settings.Proxy> getProxies()
meth public java.util.List<org.apache.maven.settings.Server> getServers()
meth public java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact> getManagedVersionMap()
meth public java.util.Set<org.apache.maven.artifact.Artifact> getArtifactDependencies()
meth public org.apache.maven.artifact.Artifact getArtifact()
meth public org.apache.maven.artifact.repository.ArtifactRepository getLocalRepository()
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest addListener(org.apache.maven.artifact.resolver.ResolutionListener)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest setArtifact(org.apache.maven.artifact.Artifact)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest setArtifactDependencies(java.util.Set<org.apache.maven.artifact.Artifact>)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest setCache(org.apache.maven.artifact.repository.RepositoryCache)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest setCollectionFilter(org.apache.maven.artifact.resolver.filter.ArtifactFilter)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest setForceUpdate(boolean)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest setListeners(java.util.List<org.apache.maven.artifact.resolver.ResolutionListener>)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest setLocalRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest setManagedVersionMap(java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest setMirrors(java.util.List<org.apache.maven.settings.Mirror>)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest setOffline(boolean)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest setProxies(java.util.List<org.apache.maven.settings.Proxy>)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest setRemoteRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest setResolutionFilter(org.apache.maven.artifact.resolver.filter.ArtifactFilter)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest setResolveRoot(boolean)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest setResolveTransitively(boolean)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionRequest setServers(java.util.List<org.apache.maven.settings.Server>)
meth public org.apache.maven.artifact.resolver.filter.ArtifactFilter getCollectionFilter()
meth public org.apache.maven.artifact.resolver.filter.ArtifactFilter getResolutionFilter()
supr java.lang.Object
hfds artifact,artifactDependencies,collectionFilter,forceUpdate,listeners,localRepository,managedVersionMap,mirrors,offline,proxies,remoteRepositories,resolutionFilter,resolveRoot,resolveTransitively,servers

CLSS public org.apache.maven.artifact.resolver.ArtifactResolutionResult
cons public init()
meth public boolean hasCircularDependencyExceptions()
meth public boolean hasErrorArtifactExceptions()
meth public boolean hasExceptions()
meth public boolean hasMetadataResolutionExceptions()
meth public boolean hasMissingArtifacts()
meth public boolean hasVersionRangeViolations()
meth public boolean isSuccess()
meth public java.lang.String toString()
meth public java.util.List<java.lang.Exception> getExceptions()
meth public java.util.List<java.lang.Exception> getVersionRangeViolations()
meth public java.util.List<org.apache.maven.artifact.Artifact> getMissingArtifacts()
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getRepositories()
meth public java.util.List<org.apache.maven.artifact.resolver.ArtifactResolutionException> getErrorArtifactExceptions()
meth public java.util.List<org.apache.maven.artifact.resolver.ArtifactResolutionException> getMetadataResolutionExceptions()
meth public java.util.List<org.apache.maven.artifact.resolver.CyclicDependencyException> getCircularDependencyExceptions()
meth public java.util.Set<org.apache.maven.artifact.Artifact> getArtifacts()
meth public java.util.Set<org.apache.maven.artifact.resolver.ResolutionNode> getArtifactResolutionNodes()
meth public org.apache.maven.artifact.Artifact getOriginatingArtifact()
meth public org.apache.maven.artifact.resolver.ArtifactResolutionException getMetadataResolutionException(int)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult addCircularDependencyException(org.apache.maven.artifact.resolver.CyclicDependencyException)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult addErrorArtifactException(org.apache.maven.artifact.resolver.ArtifactResolutionException)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult addMetadataResolutionException(org.apache.maven.artifact.resolver.ArtifactResolutionException)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult addMissingArtifact(org.apache.maven.artifact.Artifact)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult addVersionRangeViolation(java.lang.Exception)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult setOriginatingArtifact(org.apache.maven.artifact.Artifact)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult setRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult setUnresolvedArtifacts(java.util.List<org.apache.maven.artifact.Artifact>)
meth public org.apache.maven.artifact.resolver.CyclicDependencyException getCircularDependencyException(int)
meth public org.apache.maven.artifact.versioning.OverConstrainedVersionException getVersionRangeViolation(int)
meth public void addArtifact(org.apache.maven.artifact.Artifact)
meth public void setArtifactResolutionNodes(java.util.Set<org.apache.maven.artifact.resolver.ResolutionNode>)
meth public void setArtifacts(java.util.Set<org.apache.maven.artifact.Artifact>)
supr java.lang.Object
hfds artifacts,circularDependencyExceptions,errorArtifactExceptions,exceptions,metadataResolutionExceptions,missingArtifacts,originatingArtifact,repositories,resolutionNodes,versionRangeViolations

CLSS public abstract interface org.apache.maven.artifact.resolver.ArtifactResolver
fld public final static java.lang.String ROLE
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.artifact.resolver.ArtifactResolutionResult resolve(org.apache.maven.artifact.resolver.ArtifactResolutionRequest)
meth public abstract org.apache.maven.artifact.resolver.ArtifactResolutionResult resolveTransitively(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.metadata.ArtifactMetadataSource) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.artifact.resolver.ArtifactResolutionResult resolveTransitively(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.metadata.ArtifactMetadataSource,java.util.List<org.apache.maven.artifact.resolver.ResolutionListener>) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.artifact.resolver.ArtifactResolutionResult resolveTransitively(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.metadata.ArtifactMetadataSource) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.artifact.resolver.ArtifactResolutionResult resolveTransitively(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.artifact.resolver.ArtifactResolutionResult resolveTransitively(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter,java.util.List<org.apache.maven.artifact.resolver.ResolutionListener>) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.artifact.resolver.ArtifactResolutionResult resolveTransitively(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
 anno 0 java.lang.Deprecated()
meth public abstract void resolve(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
 anno 0 java.lang.Deprecated()
meth public abstract void resolve(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.wagon.events.TransferListener) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
 anno 0 java.lang.Deprecated()
meth public abstract void resolveAlways(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
 anno 0 java.lang.Deprecated()

CLSS public org.apache.maven.artifact.resolver.CyclicDependencyException
cons public init(java.lang.String,org.apache.maven.artifact.Artifact)
meth public org.apache.maven.artifact.Artifact getArtifact()
supr org.apache.maven.artifact.resolver.ArtifactResolutionException
hfds artifact

CLSS public org.apache.maven.artifact.resolver.DebugResolutionListener
cons public init(org.codehaus.plexus.logging.Logger)
intf org.apache.maven.artifact.resolver.ResolutionListener
intf org.apache.maven.artifact.resolver.ResolutionListenerForDepMgmt
meth public void endProcessChildren(org.apache.maven.artifact.Artifact)
meth public void includeArtifact(org.apache.maven.artifact.Artifact)
meth public void manageArtifact(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact)
meth public void manageArtifactScope(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact)
meth public void manageArtifactSystemPath(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact)
meth public void manageArtifactVersion(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact)
meth public void omitForCycle(org.apache.maven.artifact.Artifact)
meth public void omitForNearer(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact)
meth public void restrictRange(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact,org.apache.maven.artifact.versioning.VersionRange)
meth public void selectVersionFromRange(org.apache.maven.artifact.Artifact)
meth public void startProcessChildren(org.apache.maven.artifact.Artifact)
meth public void testArtifact(org.apache.maven.artifact.Artifact)
meth public void updateScope(org.apache.maven.artifact.Artifact,java.lang.String)
meth public void updateScopeCurrentPom(org.apache.maven.artifact.Artifact,java.lang.String)
supr java.lang.Object
hfds ignoredArtifacts,indent,logger

CLSS public org.apache.maven.artifact.resolver.DefaultArtifactCollector
 anno 0 java.lang.Deprecated()
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.artifact.resolver.ArtifactCollector, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.repository.legacy.resolver.LegacyArtifactCollector, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.artifact.resolver.ArtifactCollector
supr org.apache.maven.repository.legacy.resolver.DefaultLegacyArtifactCollector

CLSS public org.apache.maven.artifact.resolver.DefaultArtifactResolver
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.artifact.resolver.ArtifactResolver, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
fld protected org.apache.maven.artifact.factory.ArtifactFactory artifactFactory
 anno 0 org.codehaus.plexus.component.annotations.Requirement(boolean optional=false, java.lang.Class<?> role=class java.lang.Object, java.lang.String hint="", java.lang.String[] hints=[])
intf org.apache.maven.artifact.resolver.ArtifactResolver
intf org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult resolve(org.apache.maven.artifact.resolver.ArtifactResolutionRequest)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult resolveTransitively(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.metadata.ArtifactMetadataSource) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult resolveTransitively(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.metadata.ArtifactMetadataSource,java.util.List<org.apache.maven.artifact.resolver.ResolutionListener>) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult resolveTransitively(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.metadata.ArtifactMetadataSource) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult resolveTransitively(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult resolveTransitively(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter,java.util.List<org.apache.maven.artifact.resolver.ResolutionListener>) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult resolveTransitively(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter,java.util.List<org.apache.maven.artifact.resolver.ResolutionListener>,java.util.List<org.apache.maven.repository.legacy.resolver.conflict.ConflictResolver>) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult resolveTransitively(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult resolveWithExceptions(org.apache.maven.artifact.resolver.ArtifactResolutionRequest) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public void dispose()
meth public void resolve(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public void resolve(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.wagon.events.TransferListener) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public void resolveAlways(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
supr java.lang.Object
hfds artifactCollector,container,executor,legacySupport,logger,repoSystem,resolutionErrorHandler,source
hcls DaemonThreadCreator,ResolveTask

CLSS public org.apache.maven.artifact.resolver.DefaultResolutionErrorHandler
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.artifact.resolver.ResolutionErrorHandler, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.artifact.resolver.ResolutionErrorHandler
meth public void throwErrors(org.apache.maven.artifact.resolver.ArtifactResolutionRequest,org.apache.maven.artifact.resolver.ArtifactResolutionResult) throws org.apache.maven.artifact.resolver.ArtifactResolutionException
supr java.lang.Object

CLSS public org.apache.maven.artifact.resolver.MultipleArtifactsNotFoundException
cons public init(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.Artifact>,java.util.List<org.apache.maven.artifact.Artifact>,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
cons public init(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.Artifact>,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
 anno 0 java.lang.Deprecated()
meth public java.util.List<org.apache.maven.artifact.Artifact> getMissingArtifacts()
meth public java.util.List<org.apache.maven.artifact.Artifact> getResolvedArtifacts()
supr org.apache.maven.artifact.resolver.ArtifactResolutionException
hfds missingArtifacts,resolvedArtifacts

CLSS public abstract interface org.apache.maven.artifact.resolver.ResolutionErrorHandler
meth public abstract void throwErrors(org.apache.maven.artifact.resolver.ArtifactResolutionRequest,org.apache.maven.artifact.resolver.ArtifactResolutionResult) throws org.apache.maven.artifact.resolver.ArtifactResolutionException

CLSS public abstract interface org.apache.maven.artifact.resolver.ResolutionListener
fld public final static int FINISH_PROCESSING_CHILDREN = 3
fld public final static int INCLUDE_ARTIFACT = 4
fld public final static int MANAGE_ARTIFACT = 7
 anno 0 java.lang.Deprecated()
fld public final static int MANAGE_ARTIFACT_SCOPE = 13
fld public final static int MANAGE_ARTIFACT_SYSTEM_PATH = 14
fld public final static int MANAGE_ARTIFACT_VERSION = 12
fld public final static int OMIT_FOR_CYCLE = 8
fld public final static int OMIT_FOR_NEARER = 5
fld public final static int PROCESS_CHILDREN = 2
fld public final static int RESTRICT_RANGE = 11
fld public final static int SELECT_VERSION_FROM_RANGE = 10
fld public final static int TEST_ARTIFACT = 1
fld public final static int UPDATE_SCOPE = 6
fld public final static int UPDATE_SCOPE_CURRENT_POM = 9
fld public final static java.lang.String ROLE
meth public abstract void endProcessChildren(org.apache.maven.artifact.Artifact)
meth public abstract void includeArtifact(org.apache.maven.artifact.Artifact)
meth public abstract void manageArtifact(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact)
 anno 0 java.lang.Deprecated()
meth public abstract void omitForCycle(org.apache.maven.artifact.Artifact)
meth public abstract void omitForNearer(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact)
meth public abstract void restrictRange(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact,org.apache.maven.artifact.versioning.VersionRange)
meth public abstract void selectVersionFromRange(org.apache.maven.artifact.Artifact)
meth public abstract void startProcessChildren(org.apache.maven.artifact.Artifact)
meth public abstract void testArtifact(org.apache.maven.artifact.Artifact)
meth public abstract void updateScope(org.apache.maven.artifact.Artifact,java.lang.String)
meth public abstract void updateScopeCurrentPom(org.apache.maven.artifact.Artifact,java.lang.String)

CLSS public abstract interface org.apache.maven.artifact.resolver.ResolutionListenerForDepMgmt
 anno 0 java.lang.Deprecated()
meth public abstract void manageArtifactScope(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact)
meth public abstract void manageArtifactSystemPath(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact)
meth public abstract void manageArtifactVersion(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact)

CLSS public org.apache.maven.artifact.resolver.ResolutionNode
cons public init(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
cons public init(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.resolver.ResolutionNode)
meth public boolean filterTrail(org.apache.maven.artifact.resolver.filter.ArtifactFilter) throws org.apache.maven.artifact.versioning.OverConstrainedVersionException
meth public boolean isActive()
meth public boolean isChildOfRootNode()
meth public boolean isResolved()
meth public int getDepth()
meth public java.lang.Object getKey()
meth public java.lang.String toString()
meth public java.util.Iterator<org.apache.maven.artifact.resolver.ResolutionNode> getChildrenIterator()
meth public java.util.List<java.lang.String> getDependencyTrail() throws org.apache.maven.artifact.versioning.OverConstrainedVersionException
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getRemoteRepositories()
meth public org.apache.maven.artifact.Artifact getArtifact()
meth public void addDependencies(java.util.Set<org.apache.maven.artifact.Artifact>,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.resolver.filter.ArtifactFilter) throws org.apache.maven.artifact.resolver.CyclicDependencyException,org.apache.maven.artifact.versioning.OverConstrainedVersionException
meth public void disable()
meth public void enable()
meth public void setArtifact(org.apache.maven.artifact.Artifact)
supr java.lang.Object
hfds active,artifact,children,depth,parent,parents,remoteRepositories,trail

CLSS public org.apache.maven.artifact.resolver.UnresolvedArtifacts
cons public init(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.Artifact>,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public java.util.List<org.apache.maven.artifact.Artifact> getArtifacts()
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getRemoteRepositories()
meth public org.apache.maven.artifact.Artifact getOriginatingArtifact()
supr java.lang.Object
hfds artifacts,originatingArtifact,remoteRepositories

CLSS public org.apache.maven.artifact.resolver.WarningResolutionListener
cons public init(org.codehaus.plexus.logging.Logger)
intf org.apache.maven.artifact.resolver.ResolutionListener
meth public void endProcessChildren(org.apache.maven.artifact.Artifact)
meth public void includeArtifact(org.apache.maven.artifact.Artifact)
meth public void manageArtifact(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact)
meth public void omitForCycle(org.apache.maven.artifact.Artifact)
meth public void omitForNearer(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact)
meth public void restrictRange(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact,org.apache.maven.artifact.versioning.VersionRange)
meth public void selectVersionFromRange(org.apache.maven.artifact.Artifact)
meth public void startProcessChildren(org.apache.maven.artifact.Artifact)
meth public void testArtifact(org.apache.maven.artifact.Artifact)
meth public void updateScope(org.apache.maven.artifact.Artifact,java.lang.String)
meth public void updateScopeCurrentPom(org.apache.maven.artifact.Artifact,java.lang.String)
supr java.lang.Object
hfds logger

CLSS public org.apache.maven.artifact.resolver.filter.AndArtifactFilter
cons public init()
cons public init(java.util.List<org.apache.maven.artifact.resolver.filter.ArtifactFilter>)
intf org.apache.maven.artifact.resolver.filter.ArtifactFilter
meth public boolean equals(java.lang.Object)
meth public boolean include(org.apache.maven.artifact.Artifact)
meth public int hashCode()
meth public java.util.List<org.apache.maven.artifact.resolver.filter.ArtifactFilter> getFilters()
meth public void add(org.apache.maven.artifact.resolver.filter.ArtifactFilter)
supr java.lang.Object
hfds filters

CLSS public abstract interface org.apache.maven.artifact.resolver.filter.ArtifactFilter
meth public abstract boolean include(org.apache.maven.artifact.Artifact)

CLSS public org.apache.maven.artifact.resolver.filter.CumulativeScopeArtifactFilter
cons public !varargs init(org.apache.maven.artifact.resolver.filter.CumulativeScopeArtifactFilter[])
cons public init(java.util.Collection<java.lang.String>)
intf org.apache.maven.artifact.resolver.filter.ArtifactFilter
meth public boolean equals(java.lang.Object)
meth public boolean include(org.apache.maven.artifact.Artifact)
meth public int hashCode()
meth public java.util.Set<java.lang.String> getScopes()
supr java.lang.Object
hfds scopes

CLSS public org.apache.maven.artifact.resolver.filter.ExcludesArtifactFilter
cons public init(java.util.List<java.lang.String>)
meth public boolean include(org.apache.maven.artifact.Artifact)
supr org.apache.maven.artifact.resolver.filter.IncludesArtifactFilter

CLSS public org.apache.maven.artifact.resolver.filter.ExclusionArtifactFilter
cons public init(java.util.List<org.apache.maven.model.Exclusion>)
intf org.apache.maven.artifact.resolver.filter.ArtifactFilter
meth public boolean include(org.apache.maven.artifact.Artifact)
supr java.lang.Object
hfds WILDCARD,artifactIdIsWildcard,exclusions,groupIdAndArtifactIdIsWildcard,groupIdIsWildcard

CLSS public org.apache.maven.artifact.resolver.filter.ExclusionSetFilter
cons public init(java.lang.String[])
cons public init(java.util.Set<java.lang.String>)
intf org.apache.maven.artifact.resolver.filter.ArtifactFilter
meth public boolean equals(java.lang.Object)
meth public boolean include(org.apache.maven.artifact.Artifact)
meth public int hashCode()
supr java.lang.Object
hfds excludes

CLSS public org.apache.maven.artifact.resolver.filter.IncludesArtifactFilter
cons public init(java.util.List<java.lang.String>)
intf org.apache.maven.artifact.resolver.filter.ArtifactFilter
meth public boolean equals(java.lang.Object)
meth public boolean include(org.apache.maven.artifact.Artifact)
meth public int hashCode()
meth public java.util.List<java.lang.String> getPatterns()
supr java.lang.Object
hfds patterns

CLSS public org.apache.maven.artifact.resolver.filter.InversionArtifactFilter
cons public init(org.apache.maven.artifact.resolver.filter.ArtifactFilter)
intf org.apache.maven.artifact.resolver.filter.ArtifactFilter
meth public boolean equals(java.lang.Object)
meth public boolean include(org.apache.maven.artifact.Artifact)
meth public int hashCode()
supr java.lang.Object
hfds toInvert

CLSS public org.apache.maven.artifact.resolver.filter.OrArtifactFilter
cons public init()
cons public init(java.util.Collection<org.apache.maven.artifact.resolver.filter.ArtifactFilter>)
intf org.apache.maven.artifact.resolver.filter.ArtifactFilter
meth public boolean equals(java.lang.Object)
meth public boolean include(org.apache.maven.artifact.Artifact)
meth public int hashCode()
meth public void add(org.apache.maven.artifact.resolver.filter.ArtifactFilter)
supr java.lang.Object
hfds filters

CLSS public org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter
cons public init(java.lang.String)
intf org.apache.maven.artifact.resolver.filter.ArtifactFilter
meth public boolean equals(java.lang.Object)
meth public boolean include(org.apache.maven.artifact.Artifact)
meth public int hashCode()
meth public java.lang.String getScope()
supr java.lang.Object
hfds scope

CLSS public org.apache.maven.artifact.resolver.filter.TypeArtifactFilter
cons public init(java.lang.String)
intf org.apache.maven.artifact.resolver.filter.ArtifactFilter
meth public boolean equals(java.lang.Object)
meth public boolean include(org.apache.maven.artifact.Artifact)
meth public int hashCode()
supr java.lang.Object
hfds type

CLSS public abstract interface org.apache.maven.artifact.versioning.ArtifactVersion
intf java.lang.Comparable<org.apache.maven.artifact.versioning.ArtifactVersion>
meth public abstract int getBuildNumber()
meth public abstract int getIncrementalVersion()
meth public abstract int getMajorVersion()
meth public abstract int getMinorVersion()
meth public abstract java.lang.String getQualifier()
meth public abstract void parseVersion(java.lang.String)

CLSS public org.apache.maven.artifact.versioning.ComparableVersion
cons public init(java.lang.String)
intf java.lang.Comparable<org.apache.maven.artifact.versioning.ComparableVersion>
meth public !varargs static void main(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public final void parseVersion(java.lang.String)
meth public int compareTo(org.apache.maven.artifact.versioning.ComparableVersion)
meth public int hashCode()
meth public java.lang.String getCanonical()
meth public java.lang.String toString()
supr java.lang.Object
hfds MAX_INTITEM_LENGTH,MAX_LONGITEM_LENGTH,canonical,items,value
hcls BigIntegerItem,IntItem,Item,ListItem,LongItem,StringItem

CLSS public org.apache.maven.artifact.versioning.DefaultArtifactVersion
cons public init(java.lang.String)
intf org.apache.maven.artifact.versioning.ArtifactVersion
meth public boolean equals(java.lang.Object)
meth public final void parseVersion(java.lang.String)
meth public int compareTo(org.apache.maven.artifact.versioning.ArtifactVersion)
meth public int getBuildNumber()
meth public int getIncrementalVersion()
meth public int getMajorVersion()
meth public int getMinorVersion()
meth public int hashCode()
meth public java.lang.String getQualifier()
meth public java.lang.String toString()
supr java.lang.Object
hfds buildNumber,comparable,incrementalVersion,majorVersion,minorVersion,qualifier

CLSS public org.apache.maven.artifact.versioning.InvalidVersionSpecificationException
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public org.apache.maven.artifact.versioning.ManagedVersionMap
 anno 0 java.lang.Deprecated()
cons public init(java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>)
meth public java.lang.String toString()
supr java.util.HashMap<java.lang.String,org.apache.maven.artifact.Artifact>

CLSS public org.apache.maven.artifact.versioning.OverConstrainedVersionException
cons public init(java.lang.String,org.apache.maven.artifact.Artifact)
cons public init(java.lang.String,org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
supr org.apache.maven.artifact.resolver.ArtifactResolutionException

CLSS public org.apache.maven.artifact.versioning.Restriction
cons public init(org.apache.maven.artifact.versioning.ArtifactVersion,boolean,org.apache.maven.artifact.versioning.ArtifactVersion,boolean)
fld public final static org.apache.maven.artifact.versioning.Restriction EVERYTHING
meth public boolean containsVersion(org.apache.maven.artifact.versioning.ArtifactVersion)
meth public boolean equals(java.lang.Object)
meth public boolean isLowerBoundInclusive()
meth public boolean isUpperBoundInclusive()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.apache.maven.artifact.versioning.ArtifactVersion getLowerBound()
meth public org.apache.maven.artifact.versioning.ArtifactVersion getUpperBound()
supr java.lang.Object
hfds lowerBound,lowerBoundInclusive,upperBound,upperBoundInclusive

CLSS public org.apache.maven.artifact.versioning.VersionRange
meth public boolean containsVersion(org.apache.maven.artifact.versioning.ArtifactVersion)
meth public boolean equals(java.lang.Object)
meth public boolean hasRestrictions()
meth public boolean isSelectedVersionKnown(org.apache.maven.artifact.Artifact) throws org.apache.maven.artifact.versioning.OverConstrainedVersionException
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<org.apache.maven.artifact.versioning.Restriction> getRestrictions()
meth public org.apache.maven.artifact.versioning.ArtifactVersion getRecommendedVersion()
meth public org.apache.maven.artifact.versioning.ArtifactVersion getSelectedVersion(org.apache.maven.artifact.Artifact) throws org.apache.maven.artifact.versioning.OverConstrainedVersionException
meth public org.apache.maven.artifact.versioning.ArtifactVersion matchVersion(java.util.List<org.apache.maven.artifact.versioning.ArtifactVersion>)
meth public org.apache.maven.artifact.versioning.VersionRange cloneOf()
 anno 0 java.lang.Deprecated()
meth public org.apache.maven.artifact.versioning.VersionRange restrict(org.apache.maven.artifact.versioning.VersionRange)
meth public static org.apache.maven.artifact.versioning.VersionRange createFromVersion(java.lang.String)
meth public static org.apache.maven.artifact.versioning.VersionRange createFromVersionSpec(java.lang.String) throws org.apache.maven.artifact.versioning.InvalidVersionSpecificationException
supr java.lang.Object
hfds CACHE_SPEC,CACHE_VERSION,recommendedVersion,restrictions

CLSS public org.apache.maven.building.FileSource
cons public init(java.io.File)
intf org.apache.maven.building.Source
meth public java.io.File getFile()
meth public java.io.InputStream getInputStream() throws java.io.IOException
meth public java.lang.String getLocation()
meth public java.lang.String toString()
supr java.lang.Object
hfds file

CLSS public abstract interface org.apache.maven.building.Problem
innr public final static !enum Severity
meth public abstract int getColumnNumber()
meth public abstract int getLineNumber()
meth public abstract java.lang.Exception getException()
meth public abstract java.lang.String getLocation()
meth public abstract java.lang.String getMessage()
meth public abstract java.lang.String getSource()
meth public abstract org.apache.maven.building.Problem$Severity getSeverity()

CLSS public final static !enum org.apache.maven.building.Problem$Severity
 outer org.apache.maven.building.Problem
fld public final static org.apache.maven.building.Problem$Severity ERROR
fld public final static org.apache.maven.building.Problem$Severity FATAL
fld public final static org.apache.maven.building.Problem$Severity WARNING
meth public static org.apache.maven.building.Problem$Severity valueOf(java.lang.String)
meth public static org.apache.maven.building.Problem$Severity[] values()
supr java.lang.Enum<org.apache.maven.building.Problem$Severity>

CLSS public abstract interface org.apache.maven.building.ProblemCollector
meth public abstract java.util.List<org.apache.maven.building.Problem> getProblems()
meth public abstract void add(org.apache.maven.building.Problem$Severity,java.lang.String,int,int,java.lang.Exception)
meth public abstract void setSource(java.lang.String)

CLSS public org.apache.maven.building.ProblemCollectorFactory
cons public init()
meth public static org.apache.maven.building.ProblemCollector newInstance(java.util.List<org.apache.maven.building.Problem>)
supr java.lang.Object

CLSS public abstract interface org.apache.maven.building.Source
meth public abstract java.io.InputStream getInputStream() throws java.io.IOException
meth public abstract java.lang.String getLocation()

CLSS public org.apache.maven.building.StringSource
cons public init(java.lang.CharSequence)
cons public init(java.lang.CharSequence,java.lang.String)
intf org.apache.maven.building.Source
meth public java.io.InputStream getInputStream() throws java.io.IOException
meth public java.lang.String getContent()
meth public java.lang.String getLocation()
meth public java.lang.String toString()
supr java.lang.Object
hfds content,location

CLSS public org.apache.maven.building.UrlSource
cons public init(java.net.URL)
intf org.apache.maven.building.Source
meth public java.io.InputStream getInputStream() throws java.io.IOException
meth public java.lang.String getLocation()
meth public java.lang.String toString()
meth public java.net.URL getUrl()
supr java.lang.Object
hfds url

CLSS public org.apache.maven.cli.CLIManager
cons public init()
fld protected org.apache.commons.cli.Options options
fld public final static char ACTIVATE_PROFILES = 'P'
fld public final static char ALTERNATE_POM_FILE = 'f'
fld public final static char ALTERNATE_USER_SETTINGS = 's'
fld public final static char ALTERNATE_USER_TOOLCHAINS = 't'
fld public final static char BATCH_MODE = 'B'
fld public final static char CHECKSUM_FAILURE_POLICY = 'C'
fld public final static char CHECKSUM_WARNING_POLICY = 'c'
fld public final static char DEBUG = 'X'
fld public final static char ERRORS = 'e'
fld public final static char HELP = 'h'
fld public final static char NON_RECURSIVE = 'N'
fld public final static char OFFLINE = 'o'
fld public final static char QUIET = 'q'
fld public final static char SET_SYSTEM_PROPERTY = 'D'
 anno 0 java.lang.Deprecated()
fld public final static char SET_USER_PROPERTY = 'D'
fld public final static char SHOW_VERSION = 'V'
fld public final static char UPDATE_SNAPSHOTS = 'U'
fld public final static char VERSION = 'v'
fld public final static java.lang.String ALSO_MAKE = "am"
fld public final static java.lang.String ALSO_MAKE_DEPENDENTS = "amd"
fld public final static java.lang.String ALTERNATE_GLOBAL_SETTINGS = "gs"
fld public final static java.lang.String ALTERNATE_GLOBAL_TOOLCHAINS = "gt"
fld public final static java.lang.String BUILDER = "b"
fld public final static java.lang.String COLOR = "color"
fld public final static java.lang.String ENCRYPT_MASTER_PASSWORD = "emp"
fld public final static java.lang.String ENCRYPT_PASSWORD = "ep"
fld public final static java.lang.String FAIL_AT_END = "fae"
fld public final static java.lang.String FAIL_FAST = "ff"
fld public final static java.lang.String FAIL_NEVER = "fn"
fld public final static java.lang.String LOG_FILE = "l"
fld public final static java.lang.String NO_TRANSFER_PROGRESS = "ntp"
fld public final static java.lang.String PROJECT_LIST = "pl"
fld public final static java.lang.String RESUME_FROM = "rf"
fld public final static java.lang.String SUPRESS_SNAPSHOT_UPDATES = "nsu"
fld public final static java.lang.String THREADS = "T"
meth public org.apache.commons.cli.CommandLine parse(java.lang.String[]) throws org.apache.commons.cli.ParseException
meth public void displayHelp(java.io.PrintStream)
supr java.lang.Object

CLSS public final org.apache.maven.cli.CLIReportingUtils
cons public init()
fld public final static java.lang.String BUILD_VERSION_PROPERTY = "version"
fld public final static long MB = 1048576
meth public static java.lang.String formatDuration(long)
meth public static java.lang.String formatTimestamp(long)
meth public static java.lang.String showVersion()
meth public static java.lang.String showVersionMinimal()
meth public static void showError(org.slf4j.Logger,java.lang.String,java.lang.Throwable,boolean)
supr java.lang.Object
hfds ONE_DAY,ONE_HOUR,ONE_MINUTE,ONE_SECOND

CLSS public org.apache.maven.cli.CleanArgument
cons public init()
meth public static java.lang.String[] cleanArgs(java.lang.String[])
supr java.lang.Object

CLSS public org.apache.maven.cli.CliRequest
meth public boolean isDebug()
meth public boolean isQuiet()
meth public boolean isShowErrors()
meth public java.io.File getMultiModuleProjectDirectory()
meth public java.lang.String getWorkingDirectory()
meth public java.lang.String[] getArgs()
meth public java.util.Properties getSystemProperties()
meth public java.util.Properties getUserProperties()
meth public org.apache.commons.cli.CommandLine getCommandLine()
meth public org.apache.maven.execution.MavenExecutionRequest getRequest()
meth public org.codehaus.plexus.classworlds.ClassWorld getClassWorld()
meth public void setUserProperties(java.util.Properties)
supr java.lang.Object
hfds args,classWorld,commandLine,debug,multiModuleProjectDirectory,quiet,request,rootDirectory,showErrors,systemProperties,topDirectory,userProperties,workingDirectory

CLSS public org.apache.maven.cli.MavenCli
cons public init()
cons public init(org.codehaus.plexus.classworlds.ClassWorld)
fld public final static java.io.File DEFAULT_GLOBAL_TOOLCHAINS_FILE
fld public final static java.io.File DEFAULT_USER_TOOLCHAINS_FILE
fld public final static java.io.File USER_MAVEN_CONFIGURATION_HOME
fld public final static java.lang.String LOCAL_REPO_PROPERTY = "maven.repo.local"
fld public final static java.lang.String MULTIMODULE_PROJECT_DIRECTORY = "maven.multiModuleProjectDirectory"
fld public final static java.lang.String STYLE_COLOR_PROPERTY = "style.color"
fld public final static java.lang.String USER_HOME
meth protected !varargs static org.codehaus.plexus.interpolation.StringSearchInterpolator createInterpolator(org.apache.maven.cli.CliRequest,java.util.Properties[])
meth protected boolean isAcceptableRootDirectory(java.nio.file.Path)
meth protected java.nio.file.Path searchAcceptableRootDirectory(java.nio.file.Path)
meth protected org.apache.maven.model.building.ModelProcessor createModelProcessor(org.codehaus.plexus.PlexusContainer) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth protected org.eclipse.aether.transfer.TransferListener getBatchTransferListener()
meth protected org.eclipse.aether.transfer.TransferListener getConsoleTransferListener(boolean)
meth protected void customizeContainer(org.codehaus.plexus.PlexusContainer)
meth public int doMain(java.lang.String[],java.lang.String,java.io.PrintStream,java.io.PrintStream)
meth public int doMain(org.apache.maven.cli.CliRequest)
meth public static int doMain(java.lang.String[],org.codehaus.plexus.classworlds.ClassWorld)
meth public static int main(java.lang.String[],org.codehaus.plexus.classworlds.ClassWorld)
meth public static void main(java.lang.String[])
supr java.lang.Object
hfds ANSI_RESET,DOT_MVN,EXTENSIONS_FILENAME,EXT_CLASS_PATH,LAST_ANSI_SEQUENCE,MVN_MAVEN_CONFIG,NEXT_LINE,UNABLE_TO_FIND_ROOT_PROJECT_MESSAGE,classWorld,cliManager,configurationProcessors,dispatcher,eventSpyDispatcher,executionRequestPopulator,maven,modelProcessor,plexusLoggerManager,slf4jLogger,slf4jLoggerFactory,toolchainsBuilder
hcls ExitException,IllegalUseOfUndefinedProperty

CLSS public org.apache.maven.cli.ResolveFile
cons public init()
meth public static java.io.File resolveFile(java.io.File,java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.apache.maven.cli.configuration.ConfigurationProcessor
meth public abstract void process(org.apache.maven.cli.CliRequest) throws java.lang.Exception

CLSS public org.apache.maven.cli.configuration.SettingsXmlConfigurationProcessor
 anno 0 javax.inject.Named(java.lang.String value="settings")
 anno 0 javax.inject.Singleton()
cons public init()
fld public final static java.io.File DEFAULT_GLOBAL_SETTINGS_FILE
fld public final static java.io.File DEFAULT_USER_SETTINGS_FILE
fld public final static java.io.File USER_MAVEN_CONFIGURATION_HOME
fld public final static java.lang.String HINT = "settings"
fld public final static java.lang.String USER_HOME
intf org.apache.maven.cli.configuration.ConfigurationProcessor
meth public void process(org.apache.maven.cli.CliRequest) throws java.lang.Exception
supr java.lang.Object
hfds logger,settingsBuilder,settingsDecrypter

CLSS public org.apache.maven.execution.AbstractExecutionListener
cons public init()
intf org.apache.maven.execution.ExecutionListener
meth public void forkFailed(org.apache.maven.execution.ExecutionEvent)
meth public void forkStarted(org.apache.maven.execution.ExecutionEvent)
meth public void forkSucceeded(org.apache.maven.execution.ExecutionEvent)
meth public void forkedProjectFailed(org.apache.maven.execution.ExecutionEvent)
meth public void forkedProjectStarted(org.apache.maven.execution.ExecutionEvent)
meth public void forkedProjectSucceeded(org.apache.maven.execution.ExecutionEvent)
meth public void mojoFailed(org.apache.maven.execution.ExecutionEvent)
meth public void mojoSkipped(org.apache.maven.execution.ExecutionEvent)
meth public void mojoStarted(org.apache.maven.execution.ExecutionEvent)
meth public void mojoSucceeded(org.apache.maven.execution.ExecutionEvent)
meth public void projectDiscoveryStarted(org.apache.maven.execution.ExecutionEvent)
meth public void projectFailed(org.apache.maven.execution.ExecutionEvent)
meth public void projectSkipped(org.apache.maven.execution.ExecutionEvent)
meth public void projectStarted(org.apache.maven.execution.ExecutionEvent)
meth public void projectSucceeded(org.apache.maven.execution.ExecutionEvent)
meth public void sessionEnded(org.apache.maven.execution.ExecutionEvent)
meth public void sessionStarted(org.apache.maven.execution.ExecutionEvent)
supr java.lang.Object

CLSS public org.apache.maven.execution.BuildFailure
cons public init(org.apache.maven.project.MavenProject,long,java.lang.Throwable)
meth public java.lang.Throwable getCause()
supr org.apache.maven.execution.BuildSummary
hfds cause

CLSS public org.apache.maven.execution.BuildSuccess
cons public init(org.apache.maven.project.MavenProject,long)
supr org.apache.maven.execution.BuildSummary

CLSS public abstract org.apache.maven.execution.BuildSummary
cons protected init(org.apache.maven.project.MavenProject,long)
meth public long getTime()
meth public org.apache.maven.project.MavenProject getProject()
supr java.lang.Object
hfds project,time

CLSS public org.apache.maven.execution.DefaultMavenExecutionRequest
cons public init()
intf org.apache.maven.execution.MavenExecutionRequest
meth public boolean isCacheNotFound()
meth public boolean isCacheTransferError()
meth public boolean isInteractiveMode()
meth public boolean isNoSnapshotUpdates()
meth public boolean isOffline()
meth public boolean isProjectPresent()
meth public boolean isRecursive()
meth public boolean isShowErrors()
meth public boolean isUpdateSnapshots()
meth public boolean isUseLegacyLocalRepository()
meth public boolean useReactor()
meth public int getDegreeOfConcurrency()
meth public int getLoggingLevel()
meth public java.io.File getGlobalSettingsFile()
meth public java.io.File getGlobalToolchainsFile()
meth public java.io.File getLocalRepositoryPath()
meth public java.io.File getMultiModuleProjectDirectory()
meth public java.io.File getPom()
meth public java.io.File getUserSettingsFile()
meth public java.io.File getUserToolchainsFile()
meth public java.lang.String getBaseDirectory()
meth public java.lang.String getBuilderId()
meth public java.lang.String getGlobalChecksumPolicy()
meth public java.lang.String getMakeBehavior()
meth public java.lang.String getReactorFailureBehavior()
meth public java.lang.String getResumeFrom()
meth public java.util.Date getStartTime()
meth public java.util.List<java.lang.String> getActiveProfiles()
meth public java.util.List<java.lang.String> getExcludedProjects()
meth public java.util.List<java.lang.String> getGoals()
meth public java.util.List<java.lang.String> getInactiveProfiles()
meth public java.util.List<java.lang.String> getPluginGroups()
meth public java.util.List<java.lang.String> getSelectedProjects()
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getPluginArtifactRepositories()
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getRemoteRepositories()
meth public java.util.List<org.apache.maven.model.Profile> getProfiles()
meth public java.util.List<org.apache.maven.settings.Mirror> getMirrors()
meth public java.util.List<org.apache.maven.settings.Proxy> getProxies()
meth public java.util.List<org.apache.maven.settings.Server> getServers()
meth public java.util.Map<java.lang.String,java.lang.Object> getData()
meth public java.util.Map<java.lang.String,java.util.List<org.apache.maven.toolchain.model.ToolchainModel>> getToolchains()
meth public java.util.Properties getSystemProperties()
meth public java.util.Properties getUserProperties()
meth public org.apache.maven.artifact.repository.ArtifactRepository getLocalRepository()
meth public org.apache.maven.eventspy.internal.EventSpyDispatcher getEventSpyDispatcher()
meth public org.apache.maven.execution.ExecutionListener getExecutionListener()
meth public org.apache.maven.execution.MavenExecutionRequest addActiveProfile(java.lang.String)
meth public org.apache.maven.execution.MavenExecutionRequest addActiveProfiles(java.util.List<java.lang.String>)
meth public org.apache.maven.execution.MavenExecutionRequest addInactiveProfile(java.lang.String)
meth public org.apache.maven.execution.MavenExecutionRequest addInactiveProfiles(java.util.List<java.lang.String>)
meth public org.apache.maven.execution.MavenExecutionRequest addMirror(org.apache.maven.settings.Mirror)
meth public org.apache.maven.execution.MavenExecutionRequest addPluginArtifactRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public org.apache.maven.execution.MavenExecutionRequest addPluginGroup(java.lang.String)
meth public org.apache.maven.execution.MavenExecutionRequest addPluginGroups(java.util.List<java.lang.String>)
meth public org.apache.maven.execution.MavenExecutionRequest addProfile(org.apache.maven.model.Profile)
meth public org.apache.maven.execution.MavenExecutionRequest addProxy(org.apache.maven.settings.Proxy)
meth public org.apache.maven.execution.MavenExecutionRequest addRemoteRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public org.apache.maven.execution.MavenExecutionRequest addServer(org.apache.maven.settings.Server)
meth public org.apache.maven.execution.MavenExecutionRequest setActiveProfiles(java.util.List<java.lang.String>)
meth public org.apache.maven.execution.MavenExecutionRequest setBaseDirectory(java.io.File)
meth public org.apache.maven.execution.MavenExecutionRequest setBuilderId(java.lang.String)
meth public org.apache.maven.execution.MavenExecutionRequest setCacheNotFound(boolean)
meth public org.apache.maven.execution.MavenExecutionRequest setCacheTransferError(boolean)
meth public org.apache.maven.execution.MavenExecutionRequest setEventSpyDispatcher(org.apache.maven.eventspy.internal.EventSpyDispatcher)
meth public org.apache.maven.execution.MavenExecutionRequest setExcludedProjects(java.util.List<java.lang.String>)
meth public org.apache.maven.execution.MavenExecutionRequest setExecutionListener(org.apache.maven.execution.ExecutionListener)
meth public org.apache.maven.execution.MavenExecutionRequest setGlobalChecksumPolicy(java.lang.String)
meth public org.apache.maven.execution.MavenExecutionRequest setGlobalSettingsFile(java.io.File)
meth public org.apache.maven.execution.MavenExecutionRequest setGlobalToolchainsFile(java.io.File)
meth public org.apache.maven.execution.MavenExecutionRequest setGoals(java.util.List<java.lang.String>)
meth public org.apache.maven.execution.MavenExecutionRequest setInactiveProfiles(java.util.List<java.lang.String>)
meth public org.apache.maven.execution.MavenExecutionRequest setInteractiveMode(boolean)
meth public org.apache.maven.execution.MavenExecutionRequest setLocalRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public org.apache.maven.execution.MavenExecutionRequest setLocalRepositoryPath(java.io.File)
meth public org.apache.maven.execution.MavenExecutionRequest setLocalRepositoryPath(java.lang.String)
meth public org.apache.maven.execution.MavenExecutionRequest setLoggingLevel(int)
meth public org.apache.maven.execution.MavenExecutionRequest setMakeBehavior(java.lang.String)
meth public org.apache.maven.execution.MavenExecutionRequest setMirrors(java.util.List<org.apache.maven.settings.Mirror>)
meth public org.apache.maven.execution.MavenExecutionRequest setNoSnapshotUpdates(boolean)
meth public org.apache.maven.execution.MavenExecutionRequest setOffline(boolean)
meth public org.apache.maven.execution.MavenExecutionRequest setPluginArtifactRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public org.apache.maven.execution.MavenExecutionRequest setPluginGroups(java.util.List<java.lang.String>)
meth public org.apache.maven.execution.MavenExecutionRequest setPom(java.io.File)
meth public org.apache.maven.execution.MavenExecutionRequest setPomFile(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public org.apache.maven.execution.MavenExecutionRequest setProfiles(java.util.List<org.apache.maven.model.Profile>)
meth public org.apache.maven.execution.MavenExecutionRequest setProjectPresent(boolean)
meth public org.apache.maven.execution.MavenExecutionRequest setProxies(java.util.List<org.apache.maven.settings.Proxy>)
meth public org.apache.maven.execution.MavenExecutionRequest setReactorFailureBehavior(java.lang.String)
meth public org.apache.maven.execution.MavenExecutionRequest setRecursive(boolean)
meth public org.apache.maven.execution.MavenExecutionRequest setRemoteRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public org.apache.maven.execution.MavenExecutionRequest setRepositoryCache(org.eclipse.aether.RepositoryCache)
meth public org.apache.maven.execution.MavenExecutionRequest setResumeFrom(java.lang.String)
meth public org.apache.maven.execution.MavenExecutionRequest setSelectedProjects(java.util.List<java.lang.String>)
meth public org.apache.maven.execution.MavenExecutionRequest setServers(java.util.List<org.apache.maven.settings.Server>)
meth public org.apache.maven.execution.MavenExecutionRequest setShowErrors(boolean)
meth public org.apache.maven.execution.MavenExecutionRequest setStartTime(java.util.Date)
meth public org.apache.maven.execution.MavenExecutionRequest setSystemProperties(java.util.Properties)
meth public org.apache.maven.execution.MavenExecutionRequest setToolchains(java.util.Map<java.lang.String,java.util.List<org.apache.maven.toolchain.model.ToolchainModel>>)
meth public org.apache.maven.execution.MavenExecutionRequest setTransferListener(org.eclipse.aether.transfer.TransferListener)
meth public org.apache.maven.execution.MavenExecutionRequest setUpdateSnapshots(boolean)
meth public org.apache.maven.execution.MavenExecutionRequest setUseLegacyLocalRepository(boolean)
meth public org.apache.maven.execution.MavenExecutionRequest setUseReactor(boolean)
meth public org.apache.maven.execution.MavenExecutionRequest setUserProperties(java.util.Properties)
meth public org.apache.maven.execution.MavenExecutionRequest setUserSettingsFile(java.io.File)
meth public org.apache.maven.execution.MavenExecutionRequest setUserToolchainsFile(java.io.File)
meth public org.apache.maven.execution.MavenExecutionRequest setWorkspaceReader(org.eclipse.aether.repository.WorkspaceReader)
meth public org.apache.maven.project.ProjectBuildingRequest getProjectBuildingRequest()
meth public org.eclipse.aether.RepositoryCache getRepositoryCache()
meth public org.eclipse.aether.repository.WorkspaceReader getWorkspaceReader()
meth public org.eclipse.aether.transfer.TransferListener getTransferListener()
meth public static org.apache.maven.execution.MavenExecutionRequest copy(org.apache.maven.execution.MavenExecutionRequest)
meth public void setDegreeOfConcurrency(int)
meth public void setMultiModuleProjectDirectory(java.io.File)
meth public void setProjectBuildingConfiguration(org.apache.maven.project.ProjectBuildingRequest)
supr java.lang.Object
hfds activeProfiles,basedir,builderId,cacheNotFound,cacheTransferError,data,degreeOfConcurrency,eventSpyDispatcher,excludedProjects,executionListener,globalChecksumPolicy,globalSettingsFile,globalToolchainsFile,goals,inactiveProfiles,interactiveMode,isProjectPresent,localRepository,localRepositoryPath,loggingLevel,makeBehavior,mirrors,multiModuleProjectDirectory,noSnapshotUpdates,offline,pluginArtifactRepositories,pluginGroups,pom,profiles,projectBuildingRequest,proxies,reactorFailureBehavior,recursive,remoteRepositories,repositoryCache,resumeFrom,selectedProjects,servers,showErrors,startTime,systemProperties,toolchains,transferListener,updateSnapshots,useLegacyLocalRepositoryManager,useReactor,userProperties,userSettingsFile,userToolchainsFile,workspaceReader

CLSS public org.apache.maven.execution.DefaultMavenExecutionRequestPopulator
 anno 0 javax.inject.Named(java.lang.String value="")
cons public init(org.apache.maven.bridge.MavenRepositorySystem)
 anno 0 javax.inject.Inject()
intf org.apache.maven.execution.MavenExecutionRequestPopulator
meth public org.apache.maven.execution.MavenExecutionRequest populateDefaults(org.apache.maven.execution.MavenExecutionRequest) throws org.apache.maven.execution.MavenExecutionRequestPopulationException
meth public org.apache.maven.execution.MavenExecutionRequest populateFromSettings(org.apache.maven.execution.MavenExecutionRequest,org.apache.maven.settings.Settings) throws org.apache.maven.execution.MavenExecutionRequestPopulationException
 anno 0 java.lang.Deprecated()
meth public org.apache.maven.execution.MavenExecutionRequest populateFromToolchains(org.apache.maven.execution.MavenExecutionRequest,org.apache.maven.toolchain.model.PersistedToolchains) throws org.apache.maven.execution.MavenExecutionRequestPopulationException
supr java.lang.Object
hfds repositorySystem

CLSS public org.apache.maven.execution.DefaultMavenExecutionResult
cons public init()
intf org.apache.maven.execution.MavenExecutionResult
meth public boolean hasExceptions()
meth public java.util.List<java.lang.Throwable> getExceptions()
meth public java.util.List<org.apache.maven.project.MavenProject> getTopologicallySortedProjects()
meth public org.apache.maven.execution.BuildSummary getBuildSummary(org.apache.maven.project.MavenProject)
meth public org.apache.maven.execution.MavenExecutionResult addException(java.lang.Throwable)
meth public org.apache.maven.execution.MavenExecutionResult setDependencyResolutionResult(org.apache.maven.project.DependencyResolutionResult)
meth public org.apache.maven.execution.MavenExecutionResult setProject(org.apache.maven.project.MavenProject)
meth public org.apache.maven.execution.MavenExecutionResult setTopologicallySortedProjects(java.util.List<org.apache.maven.project.MavenProject>)
meth public org.apache.maven.project.DependencyResolutionResult getDependencyResolutionResult()
meth public org.apache.maven.project.MavenProject getProject()
meth public void addBuildSummary(org.apache.maven.execution.BuildSummary)
supr java.lang.Object
hfds buildSummaries,dependencyResolutionResult,exceptions,project,topologicallySortedProjects

CLSS public org.apache.maven.execution.DefaultRuntimeInformation
 anno 0 java.lang.Deprecated()
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.execution.RuntimeInformation, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.execution.RuntimeInformation
intf org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable
meth public org.apache.maven.artifact.versioning.ArtifactVersion getApplicationVersion()
meth public void initialize() throws org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException
supr java.lang.Object
hfds applicationVersion,rtInfo

CLSS public abstract interface org.apache.maven.execution.ExecutionEvent
innr public final static !enum Type
meth public abstract java.lang.Exception getException()
meth public abstract org.apache.maven.execution.ExecutionEvent$Type getType()
meth public abstract org.apache.maven.execution.MavenSession getSession()
meth public abstract org.apache.maven.plugin.MojoExecution getMojoExecution()
meth public abstract org.apache.maven.project.MavenProject getProject()

CLSS public final static !enum org.apache.maven.execution.ExecutionEvent$Type
 outer org.apache.maven.execution.ExecutionEvent
fld public final static org.apache.maven.execution.ExecutionEvent$Type ForkFailed
fld public final static org.apache.maven.execution.ExecutionEvent$Type ForkStarted
fld public final static org.apache.maven.execution.ExecutionEvent$Type ForkSucceeded
fld public final static org.apache.maven.execution.ExecutionEvent$Type ForkedProjectFailed
fld public final static org.apache.maven.execution.ExecutionEvent$Type ForkedProjectStarted
fld public final static org.apache.maven.execution.ExecutionEvent$Type ForkedProjectSucceeded
fld public final static org.apache.maven.execution.ExecutionEvent$Type MojoFailed
fld public final static org.apache.maven.execution.ExecutionEvent$Type MojoSkipped
fld public final static org.apache.maven.execution.ExecutionEvent$Type MojoStarted
fld public final static org.apache.maven.execution.ExecutionEvent$Type MojoSucceeded
fld public final static org.apache.maven.execution.ExecutionEvent$Type ProjectDiscoveryStarted
fld public final static org.apache.maven.execution.ExecutionEvent$Type ProjectFailed
fld public final static org.apache.maven.execution.ExecutionEvent$Type ProjectSkipped
fld public final static org.apache.maven.execution.ExecutionEvent$Type ProjectStarted
fld public final static org.apache.maven.execution.ExecutionEvent$Type ProjectSucceeded
fld public final static org.apache.maven.execution.ExecutionEvent$Type SessionEnded
fld public final static org.apache.maven.execution.ExecutionEvent$Type SessionStarted
meth public static org.apache.maven.execution.ExecutionEvent$Type valueOf(java.lang.String)
meth public static org.apache.maven.execution.ExecutionEvent$Type[] values()
supr java.lang.Enum<org.apache.maven.execution.ExecutionEvent$Type>

CLSS public abstract interface org.apache.maven.execution.ExecutionListener
meth public abstract void forkFailed(org.apache.maven.execution.ExecutionEvent)
meth public abstract void forkStarted(org.apache.maven.execution.ExecutionEvent)
meth public abstract void forkSucceeded(org.apache.maven.execution.ExecutionEvent)
meth public abstract void forkedProjectFailed(org.apache.maven.execution.ExecutionEvent)
meth public abstract void forkedProjectStarted(org.apache.maven.execution.ExecutionEvent)
meth public abstract void forkedProjectSucceeded(org.apache.maven.execution.ExecutionEvent)
meth public abstract void mojoFailed(org.apache.maven.execution.ExecutionEvent)
meth public abstract void mojoSkipped(org.apache.maven.execution.ExecutionEvent)
meth public abstract void mojoStarted(org.apache.maven.execution.ExecutionEvent)
meth public abstract void mojoSucceeded(org.apache.maven.execution.ExecutionEvent)
meth public abstract void projectDiscoveryStarted(org.apache.maven.execution.ExecutionEvent)
meth public abstract void projectFailed(org.apache.maven.execution.ExecutionEvent)
meth public abstract void projectSkipped(org.apache.maven.execution.ExecutionEvent)
meth public abstract void projectStarted(org.apache.maven.execution.ExecutionEvent)
meth public abstract void projectSucceeded(org.apache.maven.execution.ExecutionEvent)
meth public abstract void sessionEnded(org.apache.maven.execution.ExecutionEvent)
meth public abstract void sessionStarted(org.apache.maven.execution.ExecutionEvent)

CLSS public abstract interface org.apache.maven.execution.MavenExecutionRequest
fld public final static int LOGGING_LEVEL_DEBUG = 0
fld public final static int LOGGING_LEVEL_DISABLED = 5
fld public final static int LOGGING_LEVEL_ERROR = 3
fld public final static int LOGGING_LEVEL_FATAL = 4
fld public final static int LOGGING_LEVEL_INFO = 1
fld public final static int LOGGING_LEVEL_WARN = 2
fld public final static java.lang.String CHECKSUM_POLICY_FAIL = "fail"
fld public final static java.lang.String CHECKSUM_POLICY_WARN = "warn"
fld public final static java.lang.String REACTOR_FAIL_AT_END = "FAIL_AT_END"
fld public final static java.lang.String REACTOR_FAIL_FAST = "FAIL_FAST"
fld public final static java.lang.String REACTOR_FAIL_NEVER = "FAIL_NEVER"
fld public final static java.lang.String REACTOR_MAKE_BOTH = "make-both"
fld public final static java.lang.String REACTOR_MAKE_DOWNSTREAM = "make-downstream"
fld public final static java.lang.String REACTOR_MAKE_UPSTREAM = "make-upstream"
meth public abstract boolean isCacheNotFound()
meth public abstract boolean isCacheTransferError()
meth public abstract boolean isInteractiveMode()
meth public abstract boolean isNoSnapshotUpdates()
meth public abstract boolean isOffline()
meth public abstract boolean isProjectPresent()
meth public abstract boolean isRecursive()
meth public abstract boolean isShowErrors()
meth public abstract boolean isUpdateSnapshots()
meth public abstract boolean isUseLegacyLocalRepository()
 anno 0 java.lang.Deprecated()
meth public abstract int getDegreeOfConcurrency()
meth public abstract int getLoggingLevel()
meth public abstract java.io.File getGlobalSettingsFile()
meth public abstract java.io.File getGlobalToolchainsFile()
meth public abstract java.io.File getLocalRepositoryPath()
meth public abstract java.io.File getMultiModuleProjectDirectory()
meth public abstract java.io.File getPom()
meth public abstract java.io.File getUserSettingsFile()
meth public abstract java.io.File getUserToolchainsFile()
meth public abstract java.lang.String getBaseDirectory()
meth public abstract java.lang.String getBuilderId()
meth public abstract java.lang.String getGlobalChecksumPolicy()
meth public abstract java.lang.String getMakeBehavior()
meth public abstract java.lang.String getReactorFailureBehavior()
meth public abstract java.lang.String getResumeFrom()
meth public abstract java.util.Date getStartTime()
meth public abstract java.util.List<java.lang.String> getActiveProfiles()
meth public abstract java.util.List<java.lang.String> getExcludedProjects()
meth public abstract java.util.List<java.lang.String> getGoals()
meth public abstract java.util.List<java.lang.String> getInactiveProfiles()
meth public abstract java.util.List<java.lang.String> getPluginGroups()
meth public abstract java.util.List<java.lang.String> getSelectedProjects()
meth public abstract java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getPluginArtifactRepositories()
meth public abstract java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getRemoteRepositories()
meth public abstract java.util.List<org.apache.maven.model.Profile> getProfiles()
meth public abstract java.util.List<org.apache.maven.settings.Mirror> getMirrors()
meth public abstract java.util.List<org.apache.maven.settings.Proxy> getProxies()
meth public abstract java.util.List<org.apache.maven.settings.Server> getServers()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getData()
meth public abstract java.util.Map<java.lang.String,java.util.List<org.apache.maven.toolchain.model.ToolchainModel>> getToolchains()
meth public abstract java.util.Properties getSystemProperties()
meth public abstract java.util.Properties getUserProperties()
meth public abstract org.apache.maven.artifact.repository.ArtifactRepository getLocalRepository()
meth public abstract org.apache.maven.eventspy.internal.EventSpyDispatcher getEventSpyDispatcher()
meth public abstract org.apache.maven.execution.ExecutionListener getExecutionListener()
meth public abstract org.apache.maven.execution.MavenExecutionRequest addActiveProfile(java.lang.String)
meth public abstract org.apache.maven.execution.MavenExecutionRequest addActiveProfiles(java.util.List<java.lang.String>)
meth public abstract org.apache.maven.execution.MavenExecutionRequest addInactiveProfile(java.lang.String)
meth public abstract org.apache.maven.execution.MavenExecutionRequest addInactiveProfiles(java.util.List<java.lang.String>)
meth public abstract org.apache.maven.execution.MavenExecutionRequest addMirror(org.apache.maven.settings.Mirror)
meth public abstract org.apache.maven.execution.MavenExecutionRequest addPluginArtifactRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public abstract org.apache.maven.execution.MavenExecutionRequest addPluginGroup(java.lang.String)
meth public abstract org.apache.maven.execution.MavenExecutionRequest addPluginGroups(java.util.List<java.lang.String>)
meth public abstract org.apache.maven.execution.MavenExecutionRequest addProfile(org.apache.maven.model.Profile)
meth public abstract org.apache.maven.execution.MavenExecutionRequest addProxy(org.apache.maven.settings.Proxy)
meth public abstract org.apache.maven.execution.MavenExecutionRequest addRemoteRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public abstract org.apache.maven.execution.MavenExecutionRequest addServer(org.apache.maven.settings.Server)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setActiveProfiles(java.util.List<java.lang.String>)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setBaseDirectory(java.io.File)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setBuilderId(java.lang.String)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setCacheNotFound(boolean)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setCacheTransferError(boolean)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setEventSpyDispatcher(org.apache.maven.eventspy.internal.EventSpyDispatcher)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setExcludedProjects(java.util.List<java.lang.String>)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setExecutionListener(org.apache.maven.execution.ExecutionListener)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setGlobalChecksumPolicy(java.lang.String)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setGlobalSettingsFile(java.io.File)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setGlobalToolchainsFile(java.io.File)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setGoals(java.util.List<java.lang.String>)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setInactiveProfiles(java.util.List<java.lang.String>)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setInteractiveMode(boolean)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setLocalRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setLocalRepositoryPath(java.io.File)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setLocalRepositoryPath(java.lang.String)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setLoggingLevel(int)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setMakeBehavior(java.lang.String)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setMirrors(java.util.List<org.apache.maven.settings.Mirror>)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setNoSnapshotUpdates(boolean)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setOffline(boolean)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setPluginArtifactRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setPluginGroups(java.util.List<java.lang.String>)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setPom(java.io.File)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setProfiles(java.util.List<org.apache.maven.model.Profile>)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setProjectPresent(boolean)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setProxies(java.util.List<org.apache.maven.settings.Proxy>)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setReactorFailureBehavior(java.lang.String)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setRecursive(boolean)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setRemoteRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setRepositoryCache(org.eclipse.aether.RepositoryCache)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setResumeFrom(java.lang.String)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setSelectedProjects(java.util.List<java.lang.String>)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setServers(java.util.List<org.apache.maven.settings.Server>)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setShowErrors(boolean)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setStartTime(java.util.Date)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setSystemProperties(java.util.Properties)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setToolchains(java.util.Map<java.lang.String,java.util.List<org.apache.maven.toolchain.model.ToolchainModel>>)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setTransferListener(org.eclipse.aether.transfer.TransferListener)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setUpdateSnapshots(boolean)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setUseLegacyLocalRepository(boolean)
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.execution.MavenExecutionRequest setUserProperties(java.util.Properties)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setUserSettingsFile(java.io.File)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setUserToolchainsFile(java.io.File)
meth public abstract org.apache.maven.execution.MavenExecutionRequest setWorkspaceReader(org.eclipse.aether.repository.WorkspaceReader)
meth public abstract org.apache.maven.project.ProjectBuildingRequest getProjectBuildingRequest()
meth public abstract org.eclipse.aether.RepositoryCache getRepositoryCache()
meth public abstract org.eclipse.aether.repository.WorkspaceReader getWorkspaceReader()
meth public abstract org.eclipse.aether.transfer.TransferListener getTransferListener()
meth public abstract void setDegreeOfConcurrency(int)
meth public abstract void setMultiModuleProjectDirectory(java.io.File)

CLSS public org.apache.maven.execution.MavenExecutionRequestPopulationException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface org.apache.maven.execution.MavenExecutionRequestPopulator
meth public abstract org.apache.maven.execution.MavenExecutionRequest populateDefaults(org.apache.maven.execution.MavenExecutionRequest) throws org.apache.maven.execution.MavenExecutionRequestPopulationException
meth public abstract org.apache.maven.execution.MavenExecutionRequest populateFromSettings(org.apache.maven.execution.MavenExecutionRequest,org.apache.maven.settings.Settings) throws org.apache.maven.execution.MavenExecutionRequestPopulationException
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.execution.MavenExecutionRequest populateFromToolchains(org.apache.maven.execution.MavenExecutionRequest,org.apache.maven.toolchain.model.PersistedToolchains) throws org.apache.maven.execution.MavenExecutionRequestPopulationException

CLSS public abstract interface org.apache.maven.execution.MavenExecutionResult
meth public abstract boolean hasExceptions()
meth public abstract java.util.List<java.lang.Throwable> getExceptions()
meth public abstract java.util.List<org.apache.maven.project.MavenProject> getTopologicallySortedProjects()
meth public abstract org.apache.maven.execution.BuildSummary getBuildSummary(org.apache.maven.project.MavenProject)
meth public abstract org.apache.maven.execution.MavenExecutionResult addException(java.lang.Throwable)
meth public abstract org.apache.maven.execution.MavenExecutionResult setDependencyResolutionResult(org.apache.maven.project.DependencyResolutionResult)
meth public abstract org.apache.maven.execution.MavenExecutionResult setProject(org.apache.maven.project.MavenProject)
meth public abstract org.apache.maven.execution.MavenExecutionResult setTopologicallySortedProjects(java.util.List<org.apache.maven.project.MavenProject>)
meth public abstract org.apache.maven.project.DependencyResolutionResult getDependencyResolutionResult()
meth public abstract org.apache.maven.project.MavenProject getProject()
meth public abstract void addBuildSummary(org.apache.maven.execution.BuildSummary)

CLSS public org.apache.maven.execution.MavenSession
cons public init(org.codehaus.plexus.PlexusContainer,org.apache.maven.execution.MavenExecutionRequest,org.apache.maven.execution.MavenExecutionResult,java.util.List<org.apache.maven.project.MavenProject>)
 anno 0 java.lang.Deprecated()
cons public init(org.codehaus.plexus.PlexusContainer,org.apache.maven.execution.MavenExecutionRequest,org.apache.maven.execution.MavenExecutionResult,org.apache.maven.project.MavenProject)
 anno 0 java.lang.Deprecated()
cons public init(org.codehaus.plexus.PlexusContainer,org.apache.maven.settings.Settings,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.monitor.event.EventDispatcher,org.apache.maven.execution.ReactorManager,java.util.List<java.lang.String>,java.lang.String,java.util.Properties,java.util.Date)
 anno 0 java.lang.Deprecated()
cons public init(org.codehaus.plexus.PlexusContainer,org.apache.maven.settings.Settings,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.monitor.event.EventDispatcher,org.apache.maven.execution.ReactorManager,java.util.List<java.lang.String>,java.lang.String,java.util.Properties,java.util.Properties,java.util.Date)
 anno 0 java.lang.Deprecated()
cons public init(org.codehaus.plexus.PlexusContainer,org.eclipse.aether.RepositorySystemSession,org.apache.maven.execution.MavenExecutionRequest,org.apache.maven.execution.MavenExecutionResult)
 anno 0 java.lang.Deprecated()
intf java.lang.Cloneable
meth public boolean isOffline()
meth public boolean isParallel()
meth public boolean isUsingPOMsFromFilesystem()
 anno 0 java.lang.Deprecated()
meth public java.lang.Object lookup(java.lang.String) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
 anno 0 java.lang.Deprecated()
meth public java.lang.Object lookup(java.lang.String,java.lang.String) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
 anno 0 java.lang.Deprecated()
meth public java.lang.String getExecutionRootDirectory()
meth public java.lang.String getReactorFailureBehavior()
meth public java.util.Date getStartTime()
meth public java.util.List<java.lang.Object> lookupList(java.lang.String) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
 anno 0 java.lang.Deprecated()
meth public java.util.List<java.lang.String> getGoals()
meth public java.util.List<java.lang.String> getPluginGroups()
meth public java.util.List<org.apache.maven.project.MavenProject> getAllProjects()
meth public java.util.List<org.apache.maven.project.MavenProject> getProjects()
meth public java.util.List<org.apache.maven.project.MavenProject> getSortedProjects()
 anno 0 java.lang.Deprecated()
meth public java.util.Map<java.lang.String,java.lang.Object> getPluginContext(org.apache.maven.plugin.descriptor.PluginDescriptor,org.apache.maven.project.MavenProject)
meth public java.util.Map<java.lang.String,java.lang.Object> lookupMap(java.lang.String) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
 anno 0 java.lang.Deprecated()
meth public java.util.Map<java.lang.String,org.apache.maven.project.MavenProject> getProjectMap()
 anno 0 java.lang.Deprecated()
meth public java.util.Properties getExecutionProperties()
 anno 0 java.lang.Deprecated()
meth public java.util.Properties getSystemProperties()
meth public java.util.Properties getUserProperties()
meth public org.apache.maven.artifact.repository.ArtifactRepository getLocalRepository()
meth public org.apache.maven.artifact.repository.RepositoryCache getRepositoryCache()
 anno 0 java.lang.Deprecated()
meth public org.apache.maven.execution.MavenExecutionRequest getRequest()
meth public org.apache.maven.execution.MavenExecutionResult getResult()
meth public org.apache.maven.execution.MavenSession clone()
meth public org.apache.maven.execution.ProjectDependencyGraph getProjectDependencyGraph()
meth public org.apache.maven.monitor.event.EventDispatcher getEventDispatcher()
 anno 0 java.lang.Deprecated()
meth public org.apache.maven.project.MavenProject getCurrentProject()
meth public org.apache.maven.project.MavenProject getTopLevelProject()
meth public org.apache.maven.project.ProjectBuildingRequest getProjectBuildingRequest()
meth public org.apache.maven.settings.Settings getSettings()
meth public org.codehaus.plexus.PlexusContainer getContainer()
 anno 0 java.lang.Deprecated()
meth public org.eclipse.aether.RepositorySystemSession getRepositorySession()
meth public void setAllProjects(java.util.List<org.apache.maven.project.MavenProject>)
meth public void setCurrentProject(org.apache.maven.project.MavenProject)
meth public void setParallel(boolean)
meth public void setProjectDependencyGraph(org.apache.maven.execution.ProjectDependencyGraph)
meth public void setProjectMap(java.util.Map<java.lang.String,org.apache.maven.project.MavenProject>)
meth public void setProjects(java.util.List<org.apache.maven.project.MavenProject>)
supr java.lang.Object
hfds allProjects,container,currentProject,executionProperties,parallel,pluginContextsByProjectAndPluginKey,projectDependencyGraph,projectMap,projects,repositorySession,request,result,settings,topLevelProject

CLSS public org.apache.maven.execution.MojoExecutionEvent
cons public init(org.apache.maven.execution.MavenSession,org.apache.maven.project.MavenProject,org.apache.maven.plugin.MojoExecution,org.apache.maven.plugin.Mojo)
cons public init(org.apache.maven.execution.MavenSession,org.apache.maven.project.MavenProject,org.apache.maven.plugin.MojoExecution,org.apache.maven.plugin.Mojo,java.lang.Throwable)
meth public java.lang.Throwable getCause()
meth public org.apache.maven.execution.MavenSession getSession()
meth public org.apache.maven.plugin.Mojo getMojo()
meth public org.apache.maven.plugin.MojoExecution getExecution()
meth public org.apache.maven.project.MavenProject getProject()
supr java.lang.Object
hfds cause,mojo,mojoExecution,project,session

CLSS public abstract interface org.apache.maven.execution.MojoExecutionListener
meth public abstract void afterExecutionFailure(org.apache.maven.execution.MojoExecutionEvent)
meth public abstract void afterMojoExecutionSuccess(org.apache.maven.execution.MojoExecutionEvent) throws org.apache.maven.plugin.MojoExecutionException
meth public abstract void beforeMojoExecution(org.apache.maven.execution.MojoExecutionEvent) throws org.apache.maven.plugin.MojoExecutionException

CLSS public abstract interface org.apache.maven.execution.ProjectDependencyGraph
meth public abstract java.util.List<org.apache.maven.project.MavenProject> getAllProjects()
meth public abstract java.util.List<org.apache.maven.project.MavenProject> getDownstreamProjects(org.apache.maven.project.MavenProject,boolean)
meth public abstract java.util.List<org.apache.maven.project.MavenProject> getSortedProjects()
meth public abstract java.util.List<org.apache.maven.project.MavenProject> getUpstreamProjects(org.apache.maven.project.MavenProject,boolean)

CLSS public org.apache.maven.execution.ProjectExecutionEvent
cons public init(org.apache.maven.execution.MavenSession,org.apache.maven.project.MavenProject)
cons public init(org.apache.maven.execution.MavenSession,org.apache.maven.project.MavenProject,java.lang.Throwable)
cons public init(org.apache.maven.execution.MavenSession,org.apache.maven.project.MavenProject,java.util.List<org.apache.maven.plugin.MojoExecution>)
cons public init(org.apache.maven.execution.MavenSession,org.apache.maven.project.MavenProject,java.util.List<org.apache.maven.plugin.MojoExecution>,java.lang.Throwable)
meth public java.lang.Throwable getCause()
meth public java.util.List<org.apache.maven.plugin.MojoExecution> getExecutionPlan()
meth public org.apache.maven.execution.MavenSession getSession()
meth public org.apache.maven.project.MavenProject getProject()
supr java.lang.Object
hfds cause,executionPlan,project,session

CLSS public abstract interface org.apache.maven.execution.ProjectExecutionListener
meth public abstract void afterProjectExecutionFailure(org.apache.maven.execution.ProjectExecutionEvent)
meth public abstract void afterProjectExecutionSuccess(org.apache.maven.execution.ProjectExecutionEvent) throws org.apache.maven.lifecycle.LifecycleExecutionException
meth public abstract void beforeProjectExecution(org.apache.maven.execution.ProjectExecutionEvent) throws org.apache.maven.lifecycle.LifecycleExecutionException
meth public abstract void beforeProjectLifecycleExecution(org.apache.maven.execution.ProjectExecutionEvent) throws org.apache.maven.lifecycle.LifecycleExecutionException

CLSS public org.apache.maven.execution.ReactorManager
 anno 0 java.lang.Deprecated()
cons public init(java.util.List<org.apache.maven.project.MavenProject>) throws org.apache.maven.project.DuplicateProjectException,org.codehaus.plexus.util.dag.CycleDetectedException
fld public final static java.lang.String FAIL_AT_END = "fail-at-end"
fld public final static java.lang.String FAIL_FAST = "fail-fast"
fld public final static java.lang.String FAIL_NEVER = "fail-never"
fld public final static java.lang.String MAKE_BOTH_MODE = "make-both"
fld public final static java.lang.String MAKE_DEPENDENTS_MODE = "make-dependents"
fld public final static java.lang.String MAKE_MODE = "make"
meth public boolean executedMultipleProjects()
meth public boolean hasBuildFailure(org.apache.maven.project.MavenProject)
meth public boolean hasBuildFailures()
meth public boolean hasBuildSuccess(org.apache.maven.project.MavenProject)
meth public boolean hasMultipleProjects()
meth public boolean isBlackListed(org.apache.maven.project.MavenProject)
meth public java.lang.String getFailureBehavior()
meth public java.util.List<org.apache.maven.project.MavenProject> getSortedProjects()
meth public java.util.Map getPluginContext(org.apache.maven.plugin.descriptor.PluginDescriptor,org.apache.maven.project.MavenProject)
meth public org.apache.maven.execution.BuildFailure getBuildFailure(org.apache.maven.project.MavenProject)
meth public org.apache.maven.execution.BuildSuccess getBuildSuccess(org.apache.maven.project.MavenProject)
meth public void blackList(org.apache.maven.project.MavenProject)
meth public void registerBuildFailure(org.apache.maven.project.MavenProject,java.lang.Exception,java.lang.String,long)
meth public void registerBuildSuccess(org.apache.maven.project.MavenProject,long)
meth public void setFailureBehavior(java.lang.String)
supr java.lang.Object
hfds blackList,buildFailuresByProject,buildSuccessesByProject,failureBehavior,pluginContextsByProjectAndPluginKey,sorter

CLSS public abstract interface org.apache.maven.execution.RuntimeInformation
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.artifact.versioning.ArtifactVersion getApplicationVersion()

CLSS public org.apache.maven.lifecycle.DefaultLifecycleExecutor
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.lifecycle.LifecycleExecutor, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.lifecycle.LifecycleExecutor
meth public !varargs org.apache.maven.lifecycle.MavenExecutionPlan calculateExecutionPlan(org.apache.maven.execution.MavenSession,boolean,java.lang.String[]) throws org.apache.maven.lifecycle.LifecycleNotFoundException,org.apache.maven.lifecycle.LifecyclePhaseNotFoundException,org.apache.maven.plugin.InvalidPluginDescriptorException,org.apache.maven.plugin.MojoNotFoundException,org.apache.maven.plugin.PluginDescriptorParsingException,org.apache.maven.plugin.PluginManagerException,org.apache.maven.plugin.PluginNotFoundException,org.apache.maven.plugin.PluginResolutionException,org.apache.maven.plugin.prefix.NoPluginFoundForPrefixException,org.apache.maven.plugin.version.PluginVersionResolutionException
meth public !varargs org.apache.maven.lifecycle.MavenExecutionPlan calculateExecutionPlan(org.apache.maven.execution.MavenSession,java.lang.String[]) throws org.apache.maven.lifecycle.LifecycleNotFoundException,org.apache.maven.lifecycle.LifecyclePhaseNotFoundException,org.apache.maven.plugin.InvalidPluginDescriptorException,org.apache.maven.plugin.MojoNotFoundException,org.apache.maven.plugin.PluginDescriptorParsingException,org.apache.maven.plugin.PluginManagerException,org.apache.maven.plugin.PluginNotFoundException,org.apache.maven.plugin.PluginResolutionException,org.apache.maven.plugin.prefix.NoPluginFoundForPrefixException,org.apache.maven.plugin.version.PluginVersionResolutionException
meth public java.util.List<org.apache.maven.project.MavenProject> executeForkedExecutions(org.apache.maven.plugin.MojoExecution,org.apache.maven.execution.MavenSession) throws org.apache.maven.lifecycle.LifecycleExecutionException
meth public java.util.Map<java.lang.String,org.apache.maven.lifecycle.Lifecycle> getPhaseToLifecycleMap()
 anno 0 java.lang.Deprecated()
meth public java.util.Set<org.apache.maven.model.Plugin> getPluginsBoundByDefaultToAllLifecycles(java.lang.String)
meth public void calculateForkedExecutions(org.apache.maven.plugin.MojoExecution,org.apache.maven.execution.MavenSession) throws org.apache.maven.lifecycle.LifecycleNotFoundException,org.apache.maven.lifecycle.LifecyclePhaseNotFoundException,org.apache.maven.plugin.InvalidPluginDescriptorException,org.apache.maven.plugin.MojoNotFoundException,org.apache.maven.plugin.PluginDescriptorParsingException,org.apache.maven.plugin.PluginNotFoundException,org.apache.maven.plugin.PluginResolutionException,org.apache.maven.plugin.prefix.NoPluginFoundForPrefixException,org.apache.maven.plugin.version.PluginVersionResolutionException
meth public void execute(org.apache.maven.execution.MavenSession)
supr java.lang.Object
hfds defaultLifeCycles,lifeCyclePluginAnalyzer,lifecycleExecutionPlanCalculator,lifecycleStarter,lifecycleTaskSegmentCalculator,mojoDescriptorCreator,mojoExecutor

CLSS public org.apache.maven.lifecycle.DefaultLifecycles
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.lifecycle.DefaultLifecycles, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
cons public init(java.util.Map<java.lang.String,org.apache.maven.lifecycle.Lifecycle>,org.codehaus.plexus.logging.Logger)
fld public final static java.lang.String[] STANDARD_LIFECYCLES
meth public java.lang.String getLifecyclePhaseList()
meth public java.util.List<org.apache.maven.lifecycle.Lifecycle> getLifeCycles()
meth public java.util.Map<java.lang.String,org.apache.maven.lifecycle.Lifecycle> getPhaseToLifecycleMap()
meth public org.apache.maven.lifecycle.Lifecycle get(java.lang.String)
supr java.lang.Object
hfds lifecycles,logger

CLSS public abstract interface org.apache.maven.lifecycle.LifeCyclePluginAnalyzer
meth public abstract java.util.Set<org.apache.maven.model.Plugin> getPluginsBoundByDefaultToAllLifecycles(java.lang.String)

CLSS public org.apache.maven.lifecycle.Lifecycle
cons public init()
cons public init(java.lang.String,java.util.List<java.lang.String>,java.util.Map<java.lang.String,org.apache.maven.lifecycle.mapping.LifecyclePhase>)
meth public java.lang.String getId()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getPhases()
meth public java.util.Map<java.lang.String,java.lang.String> getDefaultPhases()
 anno 0 java.lang.Deprecated()
meth public java.util.Map<java.lang.String,org.apache.maven.lifecycle.mapping.LifecyclePhase> getDefaultLifecyclePhases()
supr java.lang.Object
hfds defaultPhases,id,phases

CLSS public org.apache.maven.lifecycle.LifecycleExecutionException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,org.apache.maven.plugin.MojoExecution,org.apache.maven.project.MavenProject)
cons public init(java.lang.String,org.apache.maven.plugin.MojoExecution,org.apache.maven.project.MavenProject,java.lang.Throwable)
cons public init(java.lang.String,org.apache.maven.project.MavenProject)
cons public init(java.lang.Throwable)
cons public init(org.apache.maven.plugin.MojoExecution,org.apache.maven.project.MavenProject,java.lang.Throwable)
meth public org.apache.maven.project.MavenProject getProject()
supr java.lang.Exception
hfds project

CLSS public abstract interface org.apache.maven.lifecycle.LifecycleExecutor
fld public final static java.lang.String ROLE
 anno 0 java.lang.Deprecated()
meth public abstract !varargs org.apache.maven.lifecycle.MavenExecutionPlan calculateExecutionPlan(org.apache.maven.execution.MavenSession,boolean,java.lang.String[]) throws org.apache.maven.lifecycle.LifecycleNotFoundException,org.apache.maven.lifecycle.LifecyclePhaseNotFoundException,org.apache.maven.plugin.InvalidPluginDescriptorException,org.apache.maven.plugin.MojoNotFoundException,org.apache.maven.plugin.PluginDescriptorParsingException,org.apache.maven.plugin.PluginManagerException,org.apache.maven.plugin.PluginNotFoundException,org.apache.maven.plugin.PluginResolutionException,org.apache.maven.plugin.prefix.NoPluginFoundForPrefixException,org.apache.maven.plugin.version.PluginVersionResolutionException
meth public abstract !varargs org.apache.maven.lifecycle.MavenExecutionPlan calculateExecutionPlan(org.apache.maven.execution.MavenSession,java.lang.String[]) throws org.apache.maven.lifecycle.LifecycleNotFoundException,org.apache.maven.lifecycle.LifecyclePhaseNotFoundException,org.apache.maven.plugin.InvalidPluginDescriptorException,org.apache.maven.plugin.MojoNotFoundException,org.apache.maven.plugin.PluginDescriptorParsingException,org.apache.maven.plugin.PluginManagerException,org.apache.maven.plugin.PluginNotFoundException,org.apache.maven.plugin.PluginResolutionException,org.apache.maven.plugin.prefix.NoPluginFoundForPrefixException,org.apache.maven.plugin.version.PluginVersionResolutionException
meth public abstract java.util.List<org.apache.maven.project.MavenProject> executeForkedExecutions(org.apache.maven.plugin.MojoExecution,org.apache.maven.execution.MavenSession) throws org.apache.maven.lifecycle.LifecycleExecutionException
meth public abstract java.util.Set<org.apache.maven.model.Plugin> getPluginsBoundByDefaultToAllLifecycles(java.lang.String)
meth public abstract void calculateForkedExecutions(org.apache.maven.plugin.MojoExecution,org.apache.maven.execution.MavenSession) throws org.apache.maven.lifecycle.LifecycleNotFoundException,org.apache.maven.lifecycle.LifecyclePhaseNotFoundException,org.apache.maven.plugin.InvalidPluginDescriptorException,org.apache.maven.plugin.MojoNotFoundException,org.apache.maven.plugin.PluginDescriptorParsingException,org.apache.maven.plugin.PluginNotFoundException,org.apache.maven.plugin.PluginResolutionException,org.apache.maven.plugin.prefix.NoPluginFoundForPrefixException,org.apache.maven.plugin.version.PluginVersionResolutionException
meth public abstract void execute(org.apache.maven.execution.MavenSession)

CLSS public abstract interface org.apache.maven.lifecycle.LifecycleMappingDelegate
meth public abstract java.util.Map<java.lang.String,java.util.List<org.apache.maven.plugin.MojoExecution>> calculateLifecycleMappings(org.apache.maven.execution.MavenSession,org.apache.maven.project.MavenProject,org.apache.maven.lifecycle.Lifecycle,java.lang.String) throws org.apache.maven.plugin.InvalidPluginDescriptorException,org.apache.maven.plugin.MojoNotFoundException,org.apache.maven.plugin.PluginDescriptorParsingException,org.apache.maven.plugin.PluginNotFoundException,org.apache.maven.plugin.PluginResolutionException

CLSS public org.apache.maven.lifecycle.LifecycleNotFoundException
cons public init(java.lang.String)
meth public java.lang.String getLifecycleId()
supr java.lang.Exception
hfds lifecycleId

CLSS public org.apache.maven.lifecycle.LifecyclePhaseNotFoundException
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getLifecyclePhase()
supr java.lang.Exception
hfds lifecyclePhase

CLSS public org.apache.maven.lifecycle.MavenExecutionPlan
cons public init(java.util.List<org.apache.maven.lifecycle.internal.ExecutionPlanItem>,org.apache.maven.lifecycle.DefaultLifecycles)
intf java.lang.Iterable<org.apache.maven.lifecycle.internal.ExecutionPlanItem>
meth public int size()
meth public java.util.Iterator<org.apache.maven.lifecycle.internal.ExecutionPlanItem> iterator()
meth public java.util.List<org.apache.maven.plugin.MojoExecution> getExecutions()
 anno 0 java.lang.Deprecated()
meth public java.util.List<org.apache.maven.plugin.MojoExecution> getMojoExecutions()
meth public java.util.Set<org.apache.maven.model.Plugin> getNonThreadSafePlugins()
meth public java.util.Set<org.apache.maven.plugin.descriptor.MojoDescriptor> getNonThreadSafeMojos()
meth public org.apache.maven.lifecycle.internal.ExecutionPlanItem findLastInPhase(java.lang.String)
supr java.lang.Object
hfds lastMojoExecutionForAllPhases,phasesInExecutionPlan,planItem

CLSS public org.apache.maven.lifecycle.MissingProjectException
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public abstract interface org.apache.maven.lifecycle.MojoExecutionConfigurator
meth public abstract void configure(org.apache.maven.project.MavenProject,org.apache.maven.plugin.MojoExecution,boolean)

CLSS public org.apache.maven.lifecycle.NoGoalSpecifiedException
cons public init(java.lang.String)
supr java.lang.Exception

CLSS public org.apache.maven.lifecycle.mapping.DefaultLifecycleMapping
cons public init()
 anno 0 java.lang.Deprecated()
cons public init(java.util.List<org.apache.maven.lifecycle.mapping.Lifecycle>)
intf org.apache.maven.lifecycle.mapping.LifecycleMapping
meth public java.util.List<java.lang.String> getOptionalMojos(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.util.Map<java.lang.String,java.lang.String> getPhases(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.util.Map<java.lang.String,org.apache.maven.lifecycle.mapping.Lifecycle> getLifecycles()
supr java.lang.Object
hfds lifecycleMap,lifecycles,phases

CLSS public org.apache.maven.lifecycle.mapping.Lifecycle
cons public init()
meth public java.lang.String getId()
meth public java.util.Map<java.lang.String,java.lang.String> getPhases()
 anno 0 java.lang.Deprecated()
meth public java.util.Map<java.lang.String,org.apache.maven.lifecycle.mapping.LifecyclePhase> getLifecyclePhases()
meth public void setId(java.lang.String)
meth public void setLifecyclePhases(java.util.Map<java.lang.String,org.apache.maven.lifecycle.mapping.LifecyclePhase>)
meth public void setPhases(java.util.Map<java.lang.String,java.lang.String>)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds id,lifecyclePhases,optionalMojos

CLSS public abstract interface org.apache.maven.lifecycle.mapping.LifecycleMapping
fld public final static java.lang.String ROLE
 anno 0 java.lang.Deprecated()
meth public abstract java.util.List<java.lang.String> getOptionalMojos(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getPhases(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Map<java.lang.String,org.apache.maven.lifecycle.mapping.Lifecycle> getLifecycles()

CLSS public org.apache.maven.lifecycle.mapping.LifecycleMojo
cons public init()
meth public java.lang.String getGoal()
meth public java.util.List<org.apache.maven.model.Dependency> getDependencies()
meth public org.codehaus.plexus.util.xml.Xpp3Dom getConfiguration()
meth public void setConfiguration(org.codehaus.plexus.util.xml.Xpp3Dom)
meth public void setDependencies(java.util.List<org.apache.maven.model.Dependency>)
meth public void setGoal(java.lang.String)
supr java.lang.Object
hfds configuration,dependencies,goal

CLSS public org.apache.maven.lifecycle.mapping.LifecyclePhase
cons public init()
cons public init(java.lang.String)
meth public java.lang.String toString()
meth public java.util.List<org.apache.maven.lifecycle.mapping.LifecycleMojo> getMojos()
meth public static java.util.Map<java.lang.String,java.lang.String> toLegacyMap(java.util.Map<java.lang.String,org.apache.maven.lifecycle.mapping.LifecyclePhase>)
 anno 0 java.lang.Deprecated()
meth public void set(java.lang.String)
meth public void setMojos(java.util.List<org.apache.maven.lifecycle.mapping.LifecycleMojo>)
supr java.lang.Object
hfds mojos

CLSS public org.apache.maven.model.Activation
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public boolean isActiveByDefault()
meth public java.lang.String getJdk()
meth public org.apache.maven.model.Activation clone()
meth public org.apache.maven.model.ActivationFile getFile()
meth public org.apache.maven.model.ActivationOS getOs()
meth public org.apache.maven.model.ActivationProperty getProperty()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public void setActiveByDefault(boolean)
meth public void setFile(org.apache.maven.model.ActivationFile)
meth public void setJdk(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setOs(org.apache.maven.model.ActivationOS)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setProperty(org.apache.maven.model.ActivationProperty)
supr java.lang.Object
hfds activeByDefault,activeByDefaultLocation,file,fileLocation,jdk,jdkLocation,location,locations,os,osLocation,property,propertyLocation

CLSS public org.apache.maven.model.ActivationFile
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.String getExists()
meth public java.lang.String getMissing()
meth public org.apache.maven.model.ActivationFile clone()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public void setExists(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setMissing(java.lang.String)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
supr java.lang.Object
hfds exists,existsLocation,location,locations,missing,missingLocation

CLSS public org.apache.maven.model.ActivationOS
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.String getArch()
meth public java.lang.String getFamily()
meth public java.lang.String getName()
meth public java.lang.String getVersion()
meth public org.apache.maven.model.ActivationOS clone()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public void setArch(java.lang.String)
meth public void setFamily(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setName(java.lang.String)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds arch,archLocation,family,familyLocation,location,locations,name,nameLocation,version,versionLocation

CLSS public org.apache.maven.model.ActivationProperty
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.String getName()
meth public java.lang.String getValue()
meth public org.apache.maven.model.ActivationProperty clone()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setName(java.lang.String)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds location,locations,name,nameLocation,value,valueLocation

CLSS public org.apache.maven.model.Build
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.String getOutputDirectory()
meth public java.lang.String getScriptSourceDirectory()
meth public java.lang.String getSourceDirectory()
meth public java.lang.String getTestOutputDirectory()
meth public java.lang.String getTestSourceDirectory()
meth public java.lang.String toString()
meth public java.util.List<org.apache.maven.model.Extension> getExtensions()
meth public org.apache.maven.model.Build clone()
meth public void addExtension(org.apache.maven.model.Extension)
meth public void removeExtension(org.apache.maven.model.Extension)
meth public void setExtensions(java.util.List<org.apache.maven.model.Extension>)
meth public void setOutputDirectory(java.lang.String)
meth public void setScriptSourceDirectory(java.lang.String)
meth public void setSourceDirectory(java.lang.String)
meth public void setTestOutputDirectory(java.lang.String)
meth public void setTestSourceDirectory(java.lang.String)
supr org.apache.maven.model.BuildBase
hfds extensions,outputDirectory,scriptSourceDirectory,sourceDirectory,testOutputDirectory,testSourceDirectory

CLSS public org.apache.maven.model.BuildBase
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.String getDefaultGoal()
meth public java.lang.String getDirectory()
meth public java.lang.String getFinalName()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getFilters()
meth public java.util.List<org.apache.maven.model.Resource> getResources()
meth public java.util.List<org.apache.maven.model.Resource> getTestResources()
meth public org.apache.maven.model.BuildBase clone()
meth public void addFilter(java.lang.String)
meth public void addResource(org.apache.maven.model.Resource)
meth public void addTestResource(org.apache.maven.model.Resource)
meth public void removeFilter(java.lang.String)
meth public void removeResource(org.apache.maven.model.Resource)
meth public void removeTestResource(org.apache.maven.model.Resource)
meth public void setDefaultGoal(java.lang.String)
meth public void setDirectory(java.lang.String)
meth public void setFilters(java.util.List<java.lang.String>)
meth public void setFinalName(java.lang.String)
meth public void setResources(java.util.List<org.apache.maven.model.Resource>)
meth public void setTestResources(java.util.List<org.apache.maven.model.Resource>)
supr org.apache.maven.model.PluginConfiguration
hfds defaultGoal,directory,filters,finalName,resources,testResources

CLSS public org.apache.maven.model.CiManagement
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.String getSystem()
meth public java.lang.String getUrl()
meth public java.util.List<org.apache.maven.model.Notifier> getNotifiers()
meth public org.apache.maven.model.CiManagement clone()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public void addNotifier(org.apache.maven.model.Notifier)
meth public void removeNotifier(org.apache.maven.model.Notifier)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setNotifiers(java.util.List<org.apache.maven.model.Notifier>)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setSystem(java.lang.String)
meth public void setUrl(java.lang.String)
supr java.lang.Object
hfds location,locations,notifiers,notifiersLocation,system,systemLocation,url,urlLocation

CLSS public org.apache.maven.model.ConfigurationContainer
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public boolean isInheritanceApplied()
meth public boolean isInherited()
meth public java.lang.Object getConfiguration()
meth public java.lang.String getInherited()
meth public org.apache.maven.model.ConfigurationContainer clone()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public void setConfiguration(java.lang.Object)
meth public void setInherited(boolean)
meth public void setInherited(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void unsetInheritanceApplied()
supr java.lang.Object
hfds configuration,configurationLocation,inheritanceApplied,inherited,inheritedLocation,location,locations

CLSS public org.apache.maven.model.Contributor
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.String getEmail()
meth public java.lang.String getName()
meth public java.lang.String getOrganization()
meth public java.lang.String getOrganizationUrl()
meth public java.lang.String getTimezone()
meth public java.lang.String getUrl()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getRoles()
meth public java.util.Properties getProperties()
meth public org.apache.maven.model.Contributor clone()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public void addProperty(java.lang.String,java.lang.String)
meth public void addRole(java.lang.String)
meth public void removeRole(java.lang.String)
meth public void setEmail(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setName(java.lang.String)
meth public void setOrganization(java.lang.String)
meth public void setOrganizationUrl(java.lang.String)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setProperties(java.util.Properties)
meth public void setRoles(java.util.List<java.lang.String>)
meth public void setTimezone(java.lang.String)
meth public void setUrl(java.lang.String)
supr java.lang.Object
hfds email,emailLocation,location,locations,name,nameLocation,organization,organizationLocation,organizationUrl,organizationUrlLocation,properties,propertiesLocation,roles,rolesLocation,timezone,timezoneLocation,url,urlLocation

CLSS public org.apache.maven.model.Dependency
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public boolean isOptional()
meth public java.lang.String getArtifactId()
meth public java.lang.String getClassifier()
meth public java.lang.String getGroupId()
meth public java.lang.String getManagementKey()
meth public java.lang.String getOptional()
meth public java.lang.String getScope()
meth public java.lang.String getSystemPath()
meth public java.lang.String getType()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public java.util.List<org.apache.maven.model.Exclusion> getExclusions()
meth public org.apache.maven.model.Dependency clone()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public void addExclusion(org.apache.maven.model.Exclusion)
meth public void clearManagementKey()
meth public void removeExclusion(org.apache.maven.model.Exclusion)
meth public void setArtifactId(java.lang.String)
meth public void setClassifier(java.lang.String)
meth public void setExclusions(java.util.List<org.apache.maven.model.Exclusion>)
meth public void setGroupId(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setOptional(boolean)
meth public void setOptional(java.lang.String)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setScope(java.lang.String)
meth public void setSystemPath(java.lang.String)
meth public void setType(java.lang.String)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds artifactId,artifactIdLocation,classifier,classifierLocation,exclusions,exclusionsLocation,groupId,groupIdLocation,location,locations,managementKey,optional,optionalLocation,scope,scopeLocation,systemPath,systemPathLocation,type,typeLocation,version,versionLocation

CLSS public org.apache.maven.model.DependencyManagement
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.util.List<org.apache.maven.model.Dependency> getDependencies()
meth public org.apache.maven.model.DependencyManagement clone()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public void addDependency(org.apache.maven.model.Dependency)
meth public void removeDependency(org.apache.maven.model.Dependency)
meth public void setDependencies(java.util.List<org.apache.maven.model.Dependency>)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
supr java.lang.Object
hfds dependencies,dependenciesLocation,location,locations

CLSS public org.apache.maven.model.DeploymentRepository
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean isUniqueVersion()
meth public org.apache.maven.model.DeploymentRepository clone()
meth public void setUniqueVersion(boolean)
supr org.apache.maven.model.Repository
hfds uniqueVersion

CLSS public org.apache.maven.model.Developer
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.String getId()
meth public java.lang.String toString()
meth public org.apache.maven.model.Developer clone()
meth public void setId(java.lang.String)
supr org.apache.maven.model.Contributor
hfds id

CLSS public org.apache.maven.model.DistributionManagement
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.String getDownloadUrl()
meth public java.lang.String getStatus()
meth public org.apache.maven.model.DeploymentRepository getRepository()
meth public org.apache.maven.model.DeploymentRepository getSnapshotRepository()
meth public org.apache.maven.model.DistributionManagement clone()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.Relocation getRelocation()
meth public org.apache.maven.model.Site getSite()
meth public void setDownloadUrl(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setRelocation(org.apache.maven.model.Relocation)
meth public void setRepository(org.apache.maven.model.DeploymentRepository)
meth public void setSite(org.apache.maven.model.Site)
meth public void setSnapshotRepository(org.apache.maven.model.DeploymentRepository)
meth public void setStatus(java.lang.String)
supr java.lang.Object
hfds downloadUrl,downloadUrlLocation,location,locations,relocation,relocationLocation,repository,repositoryLocation,site,siteLocation,snapshotRepository,snapshotRepositoryLocation,status,statusLocation

CLSS public org.apache.maven.model.Exclusion
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.String getArtifactId()
meth public java.lang.String getGroupId()
meth public org.apache.maven.model.Exclusion clone()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public void setArtifactId(java.lang.String)
meth public void setGroupId(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
supr java.lang.Object
hfds artifactId,artifactIdLocation,groupId,groupIdLocation,location,locations

CLSS public org.apache.maven.model.Extension
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getArtifactId()
meth public java.lang.String getGroupId()
meth public java.lang.String getVersion()
meth public org.apache.maven.model.Extension clone()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public void setArtifactId(java.lang.String)
meth public void setGroupId(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds artifactId,artifactIdLocation,groupId,groupIdLocation,location,locations,version,versionLocation

CLSS public org.apache.maven.model.FileSet
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.String getDirectory()
meth public java.lang.String toString()
meth public org.apache.maven.model.FileSet clone()
meth public void setDirectory(java.lang.String)
supr org.apache.maven.model.PatternSet
hfds directory

CLSS public final org.apache.maven.model.InputLocation
cons public init(int,int)
cons public init(int,int,org.apache.maven.model.InputSource)
innr public abstract static StringFormatter
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.String toString()
meth public java.util.Map<java.lang.Object,org.apache.maven.model.InputLocation> getLocations()
meth public org.apache.maven.model.InputLocation clone()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.InputSource getSource()
meth public static org.apache.maven.model.InputLocation merge(org.apache.maven.model.InputLocation,org.apache.maven.model.InputLocation,boolean)
meth public static org.apache.maven.model.InputLocation merge(org.apache.maven.model.InputLocation,org.apache.maven.model.InputLocation,java.util.Collection<java.lang.Integer>)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setLocations(java.util.Map<java.lang.Object,org.apache.maven.model.InputLocation>)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
supr java.lang.Object
hfds columnNumber,lineNumber,location,locations,source

CLSS public abstract static org.apache.maven.model.InputLocation$StringFormatter
 outer org.apache.maven.model.InputLocation
cons public init()
meth public abstract java.lang.String toString(org.apache.maven.model.InputLocation)
supr java.lang.Object

CLSS public abstract interface org.apache.maven.model.InputLocationTracker
meth public abstract org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public abstract void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)

CLSS public org.apache.maven.model.InputSource
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.String getLocation()
meth public java.lang.String getModelId()
meth public java.lang.String toString()
meth public org.apache.maven.model.InputSource clone()
meth public void setLocation(java.lang.String)
meth public void setModelId(java.lang.String)
supr java.lang.Object
hfds location,modelId

CLSS public org.apache.maven.model.IssueManagement
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.String getSystem()
meth public java.lang.String getUrl()
meth public java.lang.String toString()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.IssueManagement clone()
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setSystem(java.lang.String)
meth public void setUrl(java.lang.String)
supr java.lang.Object
hfds location,locations,system,systemLocation,url,urlLocation

CLSS public org.apache.maven.model.License
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.String getComments()
meth public java.lang.String getDistribution()
meth public java.lang.String getName()
meth public java.lang.String getUrl()
meth public java.lang.String toString()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.License clone()
meth public void setComments(java.lang.String)
meth public void setDistribution(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setName(java.lang.String)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setUrl(java.lang.String)
supr java.lang.Object
hfds comments,commentsLocation,distribution,distributionLocation,location,locations,name,nameLocation,url,urlLocation

CLSS public org.apache.maven.model.MailingList
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.String getArchive()
meth public java.lang.String getName()
meth public java.lang.String getPost()
meth public java.lang.String getSubscribe()
meth public java.lang.String getUnsubscribe()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getOtherArchives()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.MailingList clone()
meth public void addOtherArchive(java.lang.String)
meth public void removeOtherArchive(java.lang.String)
meth public void setArchive(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setName(java.lang.String)
meth public void setOtherArchives(java.util.List<java.lang.String>)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setPost(java.lang.String)
meth public void setSubscribe(java.lang.String)
meth public void setUnsubscribe(java.lang.String)
supr java.lang.Object
hfds archive,archiveLocation,location,locations,name,nameLocation,otherArchives,otherArchivesLocation,post,postLocation,subscribe,subscribeLocation,unsubscribe,unsubscribeLocation

CLSS public org.apache.maven.model.Model
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean isChildProjectUrlInheritAppendPath()
meth public java.io.File getPomFile()
meth public java.io.File getProjectDirectory()
meth public java.lang.String getArtifactId()
meth public java.lang.String getChildProjectUrlInheritAppendPath()
meth public java.lang.String getDescription()
meth public java.lang.String getGroupId()
meth public java.lang.String getId()
meth public java.lang.String getInceptionYear()
meth public java.lang.String getModelEncoding()
meth public java.lang.String getModelVersion()
meth public java.lang.String getName()
meth public java.lang.String getPackaging()
meth public java.lang.String getUrl()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public java.util.List<org.apache.maven.model.Contributor> getContributors()
meth public java.util.List<org.apache.maven.model.Developer> getDevelopers()
meth public java.util.List<org.apache.maven.model.License> getLicenses()
meth public java.util.List<org.apache.maven.model.MailingList> getMailingLists()
meth public java.util.List<org.apache.maven.model.Profile> getProfiles()
meth public org.apache.maven.model.Build getBuild()
meth public org.apache.maven.model.CiManagement getCiManagement()
meth public org.apache.maven.model.IssueManagement getIssueManagement()
meth public org.apache.maven.model.Model clone()
meth public org.apache.maven.model.Organization getOrganization()
meth public org.apache.maven.model.Parent getParent()
meth public org.apache.maven.model.Prerequisites getPrerequisites()
meth public org.apache.maven.model.Scm getScm()
meth public void addContributor(org.apache.maven.model.Contributor)
meth public void addDeveloper(org.apache.maven.model.Developer)
meth public void addLicense(org.apache.maven.model.License)
meth public void addMailingList(org.apache.maven.model.MailingList)
meth public void addProfile(org.apache.maven.model.Profile)
meth public void removeContributor(org.apache.maven.model.Contributor)
meth public void removeDeveloper(org.apache.maven.model.Developer)
meth public void removeLicense(org.apache.maven.model.License)
meth public void removeMailingList(org.apache.maven.model.MailingList)
meth public void removeProfile(org.apache.maven.model.Profile)
meth public void setArtifactId(java.lang.String)
meth public void setBuild(org.apache.maven.model.Build)
meth public void setChildProjectUrlInheritAppendPath(boolean)
meth public void setChildProjectUrlInheritAppendPath(java.lang.String)
meth public void setCiManagement(org.apache.maven.model.CiManagement)
meth public void setContributors(java.util.List<org.apache.maven.model.Contributor>)
meth public void setDescription(java.lang.String)
meth public void setDevelopers(java.util.List<org.apache.maven.model.Developer>)
meth public void setGroupId(java.lang.String)
meth public void setInceptionYear(java.lang.String)
meth public void setIssueManagement(org.apache.maven.model.IssueManagement)
meth public void setLicenses(java.util.List<org.apache.maven.model.License>)
meth public void setMailingLists(java.util.List<org.apache.maven.model.MailingList>)
meth public void setModelEncoding(java.lang.String)
meth public void setModelVersion(java.lang.String)
meth public void setName(java.lang.String)
meth public void setOrganization(org.apache.maven.model.Organization)
meth public void setPackaging(java.lang.String)
meth public void setParent(org.apache.maven.model.Parent)
meth public void setPomFile(java.io.File)
meth public void setPrerequisites(org.apache.maven.model.Prerequisites)
meth public void setProfiles(java.util.List<org.apache.maven.model.Profile>)
meth public void setScm(org.apache.maven.model.Scm)
meth public void setUrl(java.lang.String)
meth public void setVersion(java.lang.String)
supr org.apache.maven.model.ModelBase
hfds artifactId,build,childProjectUrlInheritAppendPath,ciManagement,contributors,description,developers,groupId,inceptionYear,issueManagement,licenses,mailingLists,modelEncoding,modelVersion,name,organization,packaging,parent,pomFile,prerequisites,profiles,scm,url,version

CLSS public org.apache.maven.model.ModelBase
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.Object getReports()
meth public java.util.List<java.lang.String> getModules()
meth public java.util.List<org.apache.maven.model.Dependency> getDependencies()
meth public java.util.List<org.apache.maven.model.Repository> getPluginRepositories()
meth public java.util.List<org.apache.maven.model.Repository> getRepositories()
meth public java.util.Properties getProperties()
meth public org.apache.maven.model.DependencyManagement getDependencyManagement()
meth public org.apache.maven.model.DistributionManagement getDistributionManagement()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.ModelBase clone()
meth public org.apache.maven.model.Reporting getReporting()
meth public void addDependency(org.apache.maven.model.Dependency)
meth public void addModule(java.lang.String)
meth public void addPluginRepository(org.apache.maven.model.Repository)
meth public void addProperty(java.lang.String,java.lang.String)
meth public void addRepository(org.apache.maven.model.Repository)
meth public void removeDependency(org.apache.maven.model.Dependency)
meth public void removeModule(java.lang.String)
meth public void removePluginRepository(org.apache.maven.model.Repository)
meth public void removeRepository(org.apache.maven.model.Repository)
meth public void setDependencies(java.util.List<org.apache.maven.model.Dependency>)
meth public void setDependencyManagement(org.apache.maven.model.DependencyManagement)
meth public void setDistributionManagement(org.apache.maven.model.DistributionManagement)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setModules(java.util.List<java.lang.String>)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setPluginRepositories(java.util.List<org.apache.maven.model.Repository>)
meth public void setProperties(java.util.Properties)
meth public void setReporting(org.apache.maven.model.Reporting)
meth public void setReports(java.lang.Object)
meth public void setRepositories(java.util.List<org.apache.maven.model.Repository>)
supr java.lang.Object
hfds dependencies,dependenciesLocation,dependencyManagement,dependencyManagementLocation,distributionManagement,distributionManagementLocation,location,locations,modules,modulesLocation,pluginRepositories,pluginRepositoriesLocation,properties,propertiesLocation,reporting,reportingLocation,reports,reportsLocation,repositories,repositoriesLocation

CLSS public org.apache.maven.model.Notifier
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public boolean isSendOnError()
meth public boolean isSendOnFailure()
meth public boolean isSendOnSuccess()
meth public boolean isSendOnWarning()
meth public java.lang.String getAddress()
meth public java.lang.String getType()
meth public java.util.Properties getConfiguration()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.Notifier clone()
meth public void addConfiguration(java.lang.String,java.lang.String)
meth public void setAddress(java.lang.String)
meth public void setConfiguration(java.util.Properties)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setSendOnError(boolean)
meth public void setSendOnFailure(boolean)
meth public void setSendOnSuccess(boolean)
meth public void setSendOnWarning(boolean)
meth public void setType(java.lang.String)
supr java.lang.Object
hfds address,addressLocation,configuration,configurationLocation,location,locations,sendOnError,sendOnErrorLocation,sendOnFailure,sendOnFailureLocation,sendOnSuccess,sendOnSuccessLocation,sendOnWarning,sendOnWarningLocation,type,typeLocation

CLSS public org.apache.maven.model.Organization
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.String getName()
meth public java.lang.String getUrl()
meth public java.lang.String toString()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.Organization clone()
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setName(java.lang.String)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setUrl(java.lang.String)
supr java.lang.Object
hfds location,locations,name,nameLocation,url,urlLocation

CLSS public org.apache.maven.model.Parent
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.String getArtifactId()
meth public java.lang.String getGroupId()
meth public java.lang.String getId()
meth public java.lang.String getRelativePath()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.Parent clone()
meth public void setArtifactId(java.lang.String)
meth public void setGroupId(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setRelativePath(java.lang.String)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds artifactId,artifactIdLocation,groupId,groupIdLocation,location,locations,relativePath,relativePathLocation,version,versionLocation

CLSS public org.apache.maven.model.PatternSet
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getExcludes()
meth public java.util.List<java.lang.String> getIncludes()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.PatternSet clone()
meth public void addExclude(java.lang.String)
meth public void addInclude(java.lang.String)
meth public void removeExclude(java.lang.String)
meth public void removeInclude(java.lang.String)
meth public void setExcludes(java.util.List<java.lang.String>)
meth public void setIncludes(java.util.List<java.lang.String>)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
supr java.lang.Object
hfds excludes,excludesLocation,includes,includesLocation,location,locations

CLSS public org.apache.maven.model.Plugin
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public boolean isExtensions()
meth public int hashCode()
meth public java.lang.Object getGoals()
meth public java.lang.String getArtifactId()
meth public java.lang.String getExtensions()
meth public java.lang.String getGroupId()
meth public java.lang.String getId()
meth public java.lang.String getKey()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public java.util.List<org.apache.maven.model.Dependency> getDependencies()
meth public java.util.List<org.apache.maven.model.PluginExecution> getExecutions()
meth public java.util.Map<java.lang.String,org.apache.maven.model.PluginExecution> getExecutionsAsMap()
meth public org.apache.maven.model.Plugin clone()
meth public static java.lang.String constructKey(java.lang.String,java.lang.String)
meth public void addDependency(org.apache.maven.model.Dependency)
meth public void addExecution(org.apache.maven.model.PluginExecution)
meth public void flushExecutionMap()
meth public void removeDependency(org.apache.maven.model.Dependency)
meth public void removeExecution(org.apache.maven.model.PluginExecution)
meth public void setArtifactId(java.lang.String)
meth public void setDependencies(java.util.List<org.apache.maven.model.Dependency>)
meth public void setExecutions(java.util.List<org.apache.maven.model.PluginExecution>)
meth public void setExtensions(boolean)
meth public void setExtensions(java.lang.String)
meth public void setGoals(java.lang.Object)
meth public void setGroupId(java.lang.String)
meth public void setVersion(java.lang.String)
supr org.apache.maven.model.ConfigurationContainer
hfds artifactId,dependencies,executionMap,executions,extensions,goals,groupId,version

CLSS public org.apache.maven.model.PluginConfiguration
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.String toString()
meth public org.apache.maven.model.PluginConfiguration clone()
meth public org.apache.maven.model.PluginManagement getPluginManagement()
meth public void setPluginManagement(org.apache.maven.model.PluginManagement)
supr org.apache.maven.model.PluginContainer
hfds pluginManagement

CLSS public org.apache.maven.model.PluginContainer
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.String toString()
meth public java.util.List<org.apache.maven.model.Plugin> getPlugins()
meth public java.util.Map<java.lang.String,org.apache.maven.model.Plugin> getPluginsAsMap()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.PluginContainer clone()
meth public void addPlugin(org.apache.maven.model.Plugin)
meth public void flushPluginMap()
meth public void removePlugin(org.apache.maven.model.Plugin)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setPlugins(java.util.List<org.apache.maven.model.Plugin>)
supr java.lang.Object
hfds location,locations,pluginMap,plugins,pluginsLocation

CLSS public org.apache.maven.model.PluginExecution
cons public init()
fld public final static java.lang.String DEFAULT_EXECUTION_ID = "default"
intf java.io.Serializable
intf java.lang.Cloneable
meth public int getPriority()
meth public java.lang.String getId()
meth public java.lang.String getPhase()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getGoals()
meth public org.apache.maven.model.PluginExecution clone()
meth public void addGoal(java.lang.String)
meth public void removeGoal(java.lang.String)
meth public void setGoals(java.util.List<java.lang.String>)
meth public void setId(java.lang.String)
meth public void setPhase(java.lang.String)
meth public void setPriority(int)
supr org.apache.maven.model.ConfigurationContainer
hfds goals,id,phase,priority

CLSS public org.apache.maven.model.PluginManagement
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public org.apache.maven.model.PluginManagement clone()
supr org.apache.maven.model.PluginContainer

CLSS public org.apache.maven.model.Prerequisites
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.String getMaven()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.Prerequisites clone()
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setMaven(java.lang.String)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
supr java.lang.Object
hfds location,locations,maven,mavenLocation

CLSS public org.apache.maven.model.Profile
cons public init()
fld public final static java.lang.String SOURCE_POM = "pom"
fld public final static java.lang.String SOURCE_SETTINGS = "settings.xml"
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.String getId()
meth public java.lang.String getSource()
meth public java.lang.String toString()
meth public org.apache.maven.model.Activation getActivation()
meth public org.apache.maven.model.BuildBase getBuild()
meth public org.apache.maven.model.Profile clone()
meth public void setActivation(org.apache.maven.model.Activation)
meth public void setBuild(org.apache.maven.model.BuildBase)
meth public void setId(java.lang.String)
meth public void setSource(java.lang.String)
supr org.apache.maven.model.ModelBase
hfds activation,build,id,source

CLSS public org.apache.maven.model.Relocation
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public java.lang.String getArtifactId()
meth public java.lang.String getGroupId()
meth public java.lang.String getMessage()
meth public java.lang.String getVersion()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.Relocation clone()
meth public void setArtifactId(java.lang.String)
meth public void setGroupId(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setMessage(java.lang.String)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds artifactId,artifactIdLocation,groupId,groupIdLocation,location,locations,message,messageLocation,version,versionLocation

CLSS public org.apache.maven.model.ReportPlugin
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.String getArtifactId()
meth public java.lang.String getGroupId()
meth public java.lang.String getKey()
meth public java.lang.String getVersion()
meth public java.util.List<org.apache.maven.model.ReportSet> getReportSets()
meth public java.util.Map<java.lang.String,org.apache.maven.model.ReportSet> getReportSetsAsMap()
meth public org.apache.maven.model.ReportPlugin clone()
meth public static java.lang.String constructKey(java.lang.String,java.lang.String)
meth public void addReportSet(org.apache.maven.model.ReportSet)
meth public void flushReportSetMap()
meth public void removeReportSet(org.apache.maven.model.ReportSet)
meth public void setArtifactId(java.lang.String)
meth public void setGroupId(java.lang.String)
meth public void setReportSets(java.util.List<org.apache.maven.model.ReportSet>)
meth public void setVersion(java.lang.String)
supr org.apache.maven.model.ConfigurationContainer
hfds artifactId,groupId,reportSetMap,reportSets,version

CLSS public org.apache.maven.model.ReportSet
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.String getId()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getReports()
meth public org.apache.maven.model.ReportSet clone()
meth public void addReport(java.lang.String)
meth public void removeReport(java.lang.String)
meth public void setId(java.lang.String)
meth public void setReports(java.util.List<java.lang.String>)
supr org.apache.maven.model.ConfigurationContainer
hfds id,reports

CLSS public org.apache.maven.model.Reporting
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public boolean isExcludeDefaults()
meth public java.lang.String getExcludeDefaults()
meth public java.lang.String getOutputDirectory()
meth public java.util.List<org.apache.maven.model.ReportPlugin> getPlugins()
meth public java.util.Map<java.lang.String,org.apache.maven.model.ReportPlugin> getReportPluginsAsMap()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.Reporting clone()
meth public void addPlugin(org.apache.maven.model.ReportPlugin)
meth public void flushReportPluginMap()
meth public void removePlugin(org.apache.maven.model.ReportPlugin)
meth public void setExcludeDefaults(boolean)
meth public void setExcludeDefaults(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setOutputDirectory(java.lang.String)
meth public void setPlugins(java.util.List<org.apache.maven.model.ReportPlugin>)
supr java.lang.Object
hfds excludeDefaults,excludeDefaultsLocation,location,locations,outputDirectory,outputDirectoryLocation,plugins,pluginsLocation,reportPluginMap

CLSS public org.apache.maven.model.Repository
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public org.apache.maven.model.Repository clone()
meth public org.apache.maven.model.RepositoryPolicy getReleases()
meth public org.apache.maven.model.RepositoryPolicy getSnapshots()
meth public void setReleases(org.apache.maven.model.RepositoryPolicy)
meth public void setSnapshots(org.apache.maven.model.RepositoryPolicy)
supr org.apache.maven.model.RepositoryBase
hfds releases,snapshots

CLSS public org.apache.maven.model.RepositoryBase
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getId()
meth public java.lang.String getLayout()
meth public java.lang.String getName()
meth public java.lang.String getUrl()
meth public java.lang.String toString()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.RepositoryBase clone()
meth public void setId(java.lang.String)
meth public void setLayout(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setName(java.lang.String)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setUrl(java.lang.String)
supr java.lang.Object
hfds id,idLocation,layout,layoutLocation,location,locations,name,nameLocation,url,urlLocation

CLSS public org.apache.maven.model.RepositoryPolicy
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public boolean isEnabled()
meth public java.lang.String getChecksumPolicy()
meth public java.lang.String getEnabled()
meth public java.lang.String getUpdatePolicy()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.RepositoryPolicy clone()
meth public void setChecksumPolicy(java.lang.String)
meth public void setEnabled(boolean)
meth public void setEnabled(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setUpdatePolicy(java.lang.String)
supr java.lang.Object
hfds checksumPolicy,checksumPolicyLocation,enabled,enabledLocation,location,locations,updatePolicy,updatePolicyLocation

CLSS public org.apache.maven.model.Resource
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean isFiltering()
meth public java.lang.String getFiltering()
meth public java.lang.String getMergeId()
meth public java.lang.String getTargetPath()
meth public java.lang.String toString()
meth public org.apache.maven.model.Resource clone()
meth public void initMergeId()
meth public void setFiltering(boolean)
meth public void setFiltering(java.lang.String)
meth public void setMergeId(java.lang.String)
meth public void setTargetPath(java.lang.String)
supr org.apache.maven.model.FileSet
hfds filtering,mergeId,mergeIdCounter,targetPath

CLSS public org.apache.maven.model.Scm
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public boolean isChildScmConnectionInheritAppendPath()
meth public boolean isChildScmDeveloperConnectionInheritAppendPath()
meth public boolean isChildScmUrlInheritAppendPath()
meth public java.lang.String getChildScmConnectionInheritAppendPath()
meth public java.lang.String getChildScmDeveloperConnectionInheritAppendPath()
meth public java.lang.String getChildScmUrlInheritAppendPath()
meth public java.lang.String getConnection()
meth public java.lang.String getDeveloperConnection()
meth public java.lang.String getTag()
meth public java.lang.String getUrl()
meth public java.lang.String toString()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.Scm clone()
meth public void setChildScmConnectionInheritAppendPath(boolean)
meth public void setChildScmConnectionInheritAppendPath(java.lang.String)
meth public void setChildScmDeveloperConnectionInheritAppendPath(boolean)
meth public void setChildScmDeveloperConnectionInheritAppendPath(java.lang.String)
meth public void setChildScmUrlInheritAppendPath(boolean)
meth public void setChildScmUrlInheritAppendPath(java.lang.String)
meth public void setConnection(java.lang.String)
meth public void setDeveloperConnection(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setTag(java.lang.String)
meth public void setUrl(java.lang.String)
supr java.lang.Object
hfds childScmConnectionInheritAppendPath,childScmConnectionInheritAppendPathLocation,childScmDeveloperConnectionInheritAppendPath,childScmDeveloperConnectionInheritAppendPathLocation,childScmUrlInheritAppendPath,childScmUrlInheritAppendPathLocation,connection,connectionLocation,developerConnection,developerConnectionLocation,location,locations,tag,tagLocation,url,urlLocation

CLSS public org.apache.maven.model.Site
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.maven.model.InputLocationTracker
meth public boolean isChildSiteUrlInheritAppendPath()
meth public java.lang.String getChildSiteUrlInheritAppendPath()
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public java.lang.String getUrl()
meth public org.apache.maven.model.InputLocation getLocation(java.lang.Object)
meth public org.apache.maven.model.Site clone()
meth public void setChildSiteUrlInheritAppendPath(boolean)
meth public void setChildSiteUrlInheritAppendPath(java.lang.String)
meth public void setId(java.lang.String)
meth public void setLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setName(java.lang.String)
meth public void setOtherLocation(java.lang.Object,org.apache.maven.model.InputLocation)
meth public void setUrl(java.lang.String)
supr java.lang.Object
hfds childSiteUrlInheritAppendPath,childSiteUrlInheritAppendPathLocation,id,idLocation,location,locations,name,nameLocation,url,urlLocation

CLSS public org.apache.maven.model.building.AbstractModelBuildingListener
cons public init()
intf org.apache.maven.model.building.ModelBuildingListener
meth public void buildExtensionsAssembled(org.apache.maven.model.building.ModelBuildingEvent)
supr java.lang.Object

CLSS public org.apache.maven.model.building.DefaultModelBuilder
 anno 0 javax.inject.Named(java.lang.String value="")
 anno 0 javax.inject.Singleton()
cons public init()
intf org.apache.maven.model.building.ModelBuilder
meth protected boolean hasFatalErrors(org.apache.maven.model.building.ModelProblemCollectorExt)
meth protected boolean hasModelErrors(org.apache.maven.model.building.ModelProblemCollectorExt)
meth protected org.apache.maven.model.building.ModelBuildingResult build(org.apache.maven.model.building.ModelBuildingRequest,java.util.Collection<java.lang.String>) throws org.apache.maven.model.building.ModelBuildingException
meth public org.apache.maven.model.building.DefaultModelBuilder setDependencyManagementImporter(org.apache.maven.model.composition.DependencyManagementImporter)
meth public org.apache.maven.model.building.DefaultModelBuilder setDependencyManagementInjector(org.apache.maven.model.management.DependencyManagementInjector)
meth public org.apache.maven.model.building.DefaultModelBuilder setInheritanceAssembler(org.apache.maven.model.inheritance.InheritanceAssembler)
meth public org.apache.maven.model.building.DefaultModelBuilder setLifecycleBindingsInjector(org.apache.maven.model.plugin.LifecycleBindingsInjector)
meth public org.apache.maven.model.building.DefaultModelBuilder setModelInterpolator(org.apache.maven.model.interpolation.ModelInterpolator)
meth public org.apache.maven.model.building.DefaultModelBuilder setModelNormalizer(org.apache.maven.model.normalization.ModelNormalizer)
meth public org.apache.maven.model.building.DefaultModelBuilder setModelPathTranslator(org.apache.maven.model.path.ModelPathTranslator)
meth public org.apache.maven.model.building.DefaultModelBuilder setModelProcessor(org.apache.maven.model.building.ModelProcessor)
meth public org.apache.maven.model.building.DefaultModelBuilder setModelUrlNormalizer(org.apache.maven.model.path.ModelUrlNormalizer)
meth public org.apache.maven.model.building.DefaultModelBuilder setModelValidator(org.apache.maven.model.validation.ModelValidator)
meth public org.apache.maven.model.building.DefaultModelBuilder setPluginConfigurationExpander(org.apache.maven.model.plugin.PluginConfigurationExpander)
meth public org.apache.maven.model.building.DefaultModelBuilder setPluginManagementInjector(org.apache.maven.model.management.PluginManagementInjector)
meth public org.apache.maven.model.building.DefaultModelBuilder setProfileActivationFilePathInterpolator(org.apache.maven.model.path.ProfileActivationFilePathInterpolator)
meth public org.apache.maven.model.building.DefaultModelBuilder setProfileInjector(org.apache.maven.model.profile.ProfileInjector)
meth public org.apache.maven.model.building.DefaultModelBuilder setProfileSelector(org.apache.maven.model.profile.ProfileSelector)
meth public org.apache.maven.model.building.DefaultModelBuilder setReportConfigurationExpander(org.apache.maven.model.plugin.ReportConfigurationExpander)
meth public org.apache.maven.model.building.DefaultModelBuilder setReportingConverter(org.apache.maven.model.plugin.ReportingConverter)
meth public org.apache.maven.model.building.DefaultModelBuilder setSuperPomProvider(org.apache.maven.model.superpom.SuperPomProvider)
meth public org.apache.maven.model.building.ModelBuildingResult build(org.apache.maven.model.building.ModelBuildingRequest) throws org.apache.maven.model.building.ModelBuildingException
meth public org.apache.maven.model.building.ModelBuildingResult build(org.apache.maven.model.building.ModelBuildingRequest,org.apache.maven.model.building.ModelBuildingResult) throws org.apache.maven.model.building.ModelBuildingException
meth public org.apache.maven.model.building.Result<? extends org.apache.maven.model.Model> buildRawModel(java.io.File,int,boolean)
supr java.lang.Object
hfds dependencyManagementImporter,dependencyManagementInjector,inheritanceAssembler,lifecycleBindingsInjector,modelInterpolator,modelNormalizer,modelPathTranslator,modelProcessor,modelUrlNormalizer,modelValidator,pluginConfigurationExpander,pluginManagementInjector,profileActivationFilePathInterpolator,profileInjector,profileSelector,reportConfigurationExpander,reportingConverter,superPomProvider

CLSS public org.apache.maven.model.building.DefaultModelBuilderFactory
cons public init()
meth protected org.apache.maven.model.building.ModelProcessor newModelProcessor()
meth protected org.apache.maven.model.composition.DependencyManagementImporter newDependencyManagementImporter()
meth protected org.apache.maven.model.inheritance.InheritanceAssembler newInheritanceAssembler()
meth protected org.apache.maven.model.interpolation.ModelInterpolator newModelInterpolator()
meth protected org.apache.maven.model.interpolation.ModelVersionProcessor newModelVersionPropertiesProcessor()
meth protected org.apache.maven.model.io.ModelReader newModelReader()
meth protected org.apache.maven.model.locator.ModelLocator newModelLocator()
meth protected org.apache.maven.model.management.DependencyManagementInjector newDependencyManagementInjector()
meth protected org.apache.maven.model.management.PluginManagementInjector newPluginManagementInjector()
meth protected org.apache.maven.model.normalization.ModelNormalizer newModelNormalizer()
meth protected org.apache.maven.model.path.ModelPathTranslator newModelPathTranslator()
meth protected org.apache.maven.model.path.ModelUrlNormalizer newModelUrlNormalizer()
meth protected org.apache.maven.model.path.PathTranslator newPathTranslator()
meth protected org.apache.maven.model.path.ProfileActivationFilePathInterpolator newProfileActivationFilePathInterpolator()
meth protected org.apache.maven.model.path.UrlNormalizer newUrlNormalizer()
meth protected org.apache.maven.model.plugin.LifecycleBindingsInjector newLifecycleBindingsInjector()
meth protected org.apache.maven.model.plugin.PluginConfigurationExpander newPluginConfigurationExpander()
meth protected org.apache.maven.model.plugin.ReportConfigurationExpander newReportConfigurationExpander()
meth protected org.apache.maven.model.plugin.ReportingConverter newReportingConverter()
meth protected org.apache.maven.model.profile.ProfileInjector newProfileInjector()
meth protected org.apache.maven.model.profile.ProfileSelector newProfileSelector()
meth protected org.apache.maven.model.profile.activation.ProfileActivator[] newProfileActivators()
meth protected org.apache.maven.model.superpom.SuperPomProvider newSuperPomProvider()
meth protected org.apache.maven.model.validation.ModelValidator newModelValidator()
meth public org.apache.maven.model.building.DefaultModelBuilder newInstance()
supr java.lang.Object
hcls StubLifecycleBindingsInjector

CLSS public org.apache.maven.model.building.DefaultModelBuildingRequest
cons public init()
cons public init(org.apache.maven.model.building.ModelBuildingRequest)
intf org.apache.maven.model.building.ModelBuildingRequest
meth public boolean isLocationTracking()
meth public boolean isProcessPlugins()
meth public boolean isTwoPhaseBuilding()
meth public int getValidationLevel()
meth public java.io.File getPomFile()
meth public java.util.Date getBuildStartTime()
meth public java.util.List<java.lang.String> getActiveProfileIds()
meth public java.util.List<java.lang.String> getInactiveProfileIds()
meth public java.util.List<org.apache.maven.model.Profile> getProfiles()
meth public java.util.Properties getSystemProperties()
meth public java.util.Properties getUserProperties()
meth public org.apache.maven.model.Model getRawModel()
meth public org.apache.maven.model.building.DefaultModelBuildingRequest setActiveProfileIds(java.util.List<java.lang.String>)
meth public org.apache.maven.model.building.DefaultModelBuildingRequest setInactiveProfileIds(java.util.List<java.lang.String>)
meth public org.apache.maven.model.building.DefaultModelBuildingRequest setLocationTracking(boolean)
meth public org.apache.maven.model.building.DefaultModelBuildingRequest setModelCache(org.apache.maven.model.building.ModelCache)
meth public org.apache.maven.model.building.DefaultModelBuildingRequest setModelResolver(org.apache.maven.model.resolution.ModelResolver)
meth public org.apache.maven.model.building.DefaultModelBuildingRequest setModelSource(org.apache.maven.model.building.ModelSource)
meth public org.apache.maven.model.building.DefaultModelBuildingRequest setPomFile(java.io.File)
meth public org.apache.maven.model.building.DefaultModelBuildingRequest setProcessPlugins(boolean)
meth public org.apache.maven.model.building.DefaultModelBuildingRequest setProfiles(java.util.List<org.apache.maven.model.Profile>)
meth public org.apache.maven.model.building.DefaultModelBuildingRequest setSystemProperties(java.util.Properties)
meth public org.apache.maven.model.building.DefaultModelBuildingRequest setTwoPhaseBuilding(boolean)
meth public org.apache.maven.model.building.DefaultModelBuildingRequest setUserProperties(java.util.Properties)
meth public org.apache.maven.model.building.DefaultModelBuildingRequest setValidationLevel(int)
meth public org.apache.maven.model.building.ModelBuildingListener getModelBuildingListener()
meth public org.apache.maven.model.building.ModelBuildingRequest setBuildStartTime(java.util.Date)
meth public org.apache.maven.model.building.ModelBuildingRequest setModelBuildingListener(org.apache.maven.model.building.ModelBuildingListener)
meth public org.apache.maven.model.building.ModelBuildingRequest setRawModel(org.apache.maven.model.Model)
meth public org.apache.maven.model.building.ModelBuildingRequest setWorkspaceModelResolver(org.apache.maven.model.resolution.WorkspaceModelResolver)
meth public org.apache.maven.model.building.ModelCache getModelCache()
meth public org.apache.maven.model.building.ModelSource getModelSource()
meth public org.apache.maven.model.resolution.ModelResolver getModelResolver()
meth public org.apache.maven.model.resolution.WorkspaceModelResolver getWorkspaceModelResolver()
supr java.lang.Object
hfds activeProfileIds,buildStartTime,inactiveProfileIds,locationTracking,modelBuildingListener,modelCache,modelResolver,modelSource,pomFile,processPlugins,profiles,rawModel,systemProperties,twoPhaseBuilding,userProperties,validationLevel,workspaceResolver

CLSS public org.apache.maven.model.building.DefaultModelProblem
cons public init(java.lang.String,org.apache.maven.model.building.ModelProblem$Severity,org.apache.maven.model.building.ModelProblem$Version,java.lang.String,int,int,java.lang.String,java.lang.Exception)
cons public init(java.lang.String,org.apache.maven.model.building.ModelProblem$Severity,org.apache.maven.model.building.ModelProblem$Version,org.apache.maven.model.Model,int,int,java.lang.Exception)
intf org.apache.maven.model.building.ModelProblem
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.Exception getException()
meth public java.lang.String getMessage()
meth public java.lang.String getModelId()
meth public java.lang.String getSource()
meth public java.lang.String toString()
meth public org.apache.maven.model.building.ModelProblem$Severity getSeverity()
meth public org.apache.maven.model.building.ModelProblem$Version getVersion()
supr java.lang.Object
hfds columnNumber,exception,lineNumber,message,modelId,severity,source,version

CLSS public org.apache.maven.model.building.DefaultModelProcessor
 anno 0 javax.inject.Named(java.lang.String value="core-default")
 anno 0 javax.inject.Singleton()
 anno 0 org.eclipse.sisu.Typed(java.lang.Class<?>[] value=[class org.apache.maven.model.building.ModelProcessor])
cons public init()
intf org.apache.maven.model.building.ModelProcessor
meth public java.io.File locatePom(java.io.File)
meth public org.apache.maven.model.Model read(java.io.File,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public org.apache.maven.model.Model read(java.io.InputStream,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public org.apache.maven.model.Model read(java.io.Reader,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public org.apache.maven.model.building.DefaultModelProcessor setModelLocator(org.apache.maven.model.locator.ModelLocator)
meth public org.apache.maven.model.building.DefaultModelProcessor setModelReader(org.apache.maven.model.io.ModelReader)
supr java.lang.Object
hfds locator,reader

CLSS public org.apache.maven.model.building.FileModelSource
cons public init(java.io.File)
intf org.apache.maven.model.building.ModelSource2
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.io.File getPomFile()
 anno 0 java.lang.Deprecated()
meth public java.net.URI getLocationURI()
meth public org.apache.maven.model.building.ModelSource2 getRelatedSource(java.lang.String)
supr org.apache.maven.building.FileSource

CLSS public abstract interface org.apache.maven.model.building.ModelBuilder
meth public abstract org.apache.maven.model.building.ModelBuildingResult build(org.apache.maven.model.building.ModelBuildingRequest) throws org.apache.maven.model.building.ModelBuildingException
meth public abstract org.apache.maven.model.building.ModelBuildingResult build(org.apache.maven.model.building.ModelBuildingRequest,org.apache.maven.model.building.ModelBuildingResult) throws org.apache.maven.model.building.ModelBuildingException
meth public abstract org.apache.maven.model.building.Result<? extends org.apache.maven.model.Model> buildRawModel(java.io.File,int,boolean)

CLSS public abstract interface org.apache.maven.model.building.ModelBuildingEvent
meth public abstract org.apache.maven.model.Model getModel()
meth public abstract org.apache.maven.model.building.ModelBuildingRequest getRequest()
meth public abstract org.apache.maven.model.building.ModelProblemCollector getProblems()

CLSS public org.apache.maven.model.building.ModelBuildingException
cons public init(org.apache.maven.model.Model,java.lang.String,java.util.List<org.apache.maven.model.building.ModelProblem>)
 anno 0 java.lang.Deprecated()
cons public init(org.apache.maven.model.building.ModelBuildingResult)
meth public java.lang.String getModelId()
meth public java.util.List<org.apache.maven.model.building.ModelProblem> getProblems()
meth public org.apache.maven.model.Model getModel()
meth public org.apache.maven.model.building.ModelBuildingResult getResult()
supr java.lang.Exception
hfds result

CLSS public abstract interface org.apache.maven.model.building.ModelBuildingListener
meth public abstract void buildExtensionsAssembled(org.apache.maven.model.building.ModelBuildingEvent)

CLSS public abstract interface org.apache.maven.model.building.ModelBuildingRequest
fld public final static int VALIDATION_LEVEL_MAVEN_2_0 = 20
fld public final static int VALIDATION_LEVEL_MAVEN_3_0 = 30
fld public final static int VALIDATION_LEVEL_MAVEN_3_1 = 31
fld public final static int VALIDATION_LEVEL_MINIMAL = 0
fld public final static int VALIDATION_LEVEL_STRICT = 30
meth public abstract boolean isLocationTracking()
meth public abstract boolean isProcessPlugins()
meth public abstract boolean isTwoPhaseBuilding()
meth public abstract int getValidationLevel()
meth public abstract java.io.File getPomFile()
meth public abstract java.util.Date getBuildStartTime()
meth public abstract java.util.List<java.lang.String> getActiveProfileIds()
meth public abstract java.util.List<java.lang.String> getInactiveProfileIds()
meth public abstract java.util.List<org.apache.maven.model.Profile> getProfiles()
meth public abstract java.util.Properties getSystemProperties()
meth public abstract java.util.Properties getUserProperties()
meth public abstract org.apache.maven.model.Model getRawModel()
meth public abstract org.apache.maven.model.building.ModelBuildingListener getModelBuildingListener()
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setActiveProfileIds(java.util.List<java.lang.String>)
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setBuildStartTime(java.util.Date)
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setInactiveProfileIds(java.util.List<java.lang.String>)
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setLocationTracking(boolean)
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setModelBuildingListener(org.apache.maven.model.building.ModelBuildingListener)
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setModelCache(org.apache.maven.model.building.ModelCache)
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setModelResolver(org.apache.maven.model.resolution.ModelResolver)
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setModelSource(org.apache.maven.model.building.ModelSource)
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setPomFile(java.io.File)
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setProcessPlugins(boolean)
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setProfiles(java.util.List<org.apache.maven.model.Profile>)
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setRawModel(org.apache.maven.model.Model)
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setSystemProperties(java.util.Properties)
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setTwoPhaseBuilding(boolean)
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setUserProperties(java.util.Properties)
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setValidationLevel(int)
meth public abstract org.apache.maven.model.building.ModelBuildingRequest setWorkspaceModelResolver(org.apache.maven.model.resolution.WorkspaceModelResolver)
meth public abstract org.apache.maven.model.building.ModelCache getModelCache()
meth public abstract org.apache.maven.model.building.ModelSource getModelSource()
meth public abstract org.apache.maven.model.resolution.ModelResolver getModelResolver()
meth public abstract org.apache.maven.model.resolution.WorkspaceModelResolver getWorkspaceModelResolver()

CLSS public abstract interface org.apache.maven.model.building.ModelBuildingResult
meth public abstract java.util.List<java.lang.String> getModelIds()
meth public abstract java.util.List<org.apache.maven.model.Profile> getActiveExternalProfiles()
meth public abstract java.util.List<org.apache.maven.model.Profile> getActivePomProfiles(java.lang.String)
meth public abstract java.util.List<org.apache.maven.model.building.ModelProblem> getProblems()
meth public abstract org.apache.maven.model.Model getEffectiveModel()
meth public abstract org.apache.maven.model.Model getRawModel()
meth public abstract org.apache.maven.model.Model getRawModel(java.lang.String)

CLSS public abstract interface org.apache.maven.model.building.ModelCache
meth public abstract java.lang.Object get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public abstract void put(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Object)

CLSS public abstract interface org.apache.maven.model.building.ModelProblem
innr public final static !enum Severity
innr public final static !enum Version
meth public abstract int getColumnNumber()
meth public abstract int getLineNumber()
meth public abstract java.lang.Exception getException()
meth public abstract java.lang.String getMessage()
meth public abstract java.lang.String getModelId()
meth public abstract java.lang.String getSource()
meth public abstract org.apache.maven.model.building.ModelProblem$Severity getSeverity()
meth public abstract org.apache.maven.model.building.ModelProblem$Version getVersion()

CLSS public final static !enum org.apache.maven.model.building.ModelProblem$Severity
 outer org.apache.maven.model.building.ModelProblem
fld public final static org.apache.maven.model.building.ModelProblem$Severity ERROR
fld public final static org.apache.maven.model.building.ModelProblem$Severity FATAL
fld public final static org.apache.maven.model.building.ModelProblem$Severity WARNING
meth public static org.apache.maven.model.building.ModelProblem$Severity valueOf(java.lang.String)
meth public static org.apache.maven.model.building.ModelProblem$Severity[] values()
supr java.lang.Enum<org.apache.maven.model.building.ModelProblem$Severity>

CLSS public final static !enum org.apache.maven.model.building.ModelProblem$Version
 outer org.apache.maven.model.building.ModelProblem
fld public final static org.apache.maven.model.building.ModelProblem$Version BASE
fld public final static org.apache.maven.model.building.ModelProblem$Version V20
fld public final static org.apache.maven.model.building.ModelProblem$Version V30
fld public final static org.apache.maven.model.building.ModelProblem$Version V31
meth public static org.apache.maven.model.building.ModelProblem$Version valueOf(java.lang.String)
meth public static org.apache.maven.model.building.ModelProblem$Version[] values()
supr java.lang.Enum<org.apache.maven.model.building.ModelProblem$Version>

CLSS public abstract interface org.apache.maven.model.building.ModelProblemCollector
meth public abstract void add(org.apache.maven.model.building.ModelProblemCollectorRequest)

CLSS public abstract interface org.apache.maven.model.building.ModelProblemCollectorExt
intf org.apache.maven.model.building.ModelProblemCollector
meth public abstract java.util.List<org.apache.maven.model.building.ModelProblem> getProblems()

CLSS public final org.apache.maven.model.building.ModelProblemCollectorRequest
cons public init(org.apache.maven.model.building.ModelProblem$Severity,org.apache.maven.model.building.ModelProblem$Version)
meth public java.lang.Exception getException()
meth public java.lang.String getMessage()
meth public org.apache.maven.model.InputLocation getLocation()
meth public org.apache.maven.model.building.ModelProblem$Severity getSeverity()
meth public org.apache.maven.model.building.ModelProblem$Version getVersion()
meth public org.apache.maven.model.building.ModelProblemCollectorRequest setException(java.lang.Exception)
meth public org.apache.maven.model.building.ModelProblemCollectorRequest setLocation(org.apache.maven.model.InputLocation)
meth public org.apache.maven.model.building.ModelProblemCollectorRequest setMessage(java.lang.String)
supr java.lang.Object
hfds exception,location,message,severity,version

CLSS public org.apache.maven.model.building.ModelProblemUtils
cons public init()
meth public static java.lang.String formatLocation(org.apache.maven.model.building.ModelProblem,java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.apache.maven.model.building.ModelProcessor
fld public final static java.lang.String SOURCE = "org.apache.maven.model.building.source"
intf org.apache.maven.model.io.ModelReader
intf org.apache.maven.model.locator.ModelLocator

CLSS public abstract interface org.apache.maven.model.building.ModelSource
 anno 0 java.lang.Deprecated()
intf org.apache.maven.building.Source

CLSS public abstract interface org.apache.maven.model.building.ModelSource2
intf org.apache.maven.model.building.ModelSource
meth public abstract java.net.URI getLocationURI()
meth public abstract org.apache.maven.model.building.ModelSource2 getRelatedSource(java.lang.String)

CLSS public org.apache.maven.model.building.Result<%0 extends java.lang.Object>
meth public !varargs static <%0 extends java.lang.Object> org.apache.maven.model.building.Result<{%%0}> addProblems(org.apache.maven.model.building.Result<{%%0}>,org.apache.maven.model.building.Result<?>[])
meth public !varargs static <%0 extends java.lang.Object> org.apache.maven.model.building.Result<{%%0}> error(org.apache.maven.model.building.Result<?>[])
meth public !varargs static <%0 extends java.lang.Object> org.apache.maven.model.building.Result<{%%0}> success({%%0},org.apache.maven.model.building.Result<?>[])
meth public boolean hasErrors()
meth public java.lang.Iterable<? extends org.apache.maven.model.building.ModelProblem> getProblems()
meth public static <%0 extends java.lang.Object> org.apache.maven.model.building.Result<java.lang.Iterable<{%%0}>> newResultSet(java.lang.Iterable<? extends org.apache.maven.model.building.Result<? extends {%%0}>>)
meth public static <%0 extends java.lang.Object> org.apache.maven.model.building.Result<{%%0}> addProblem(org.apache.maven.model.building.Result<{%%0}>,org.apache.maven.model.building.ModelProblem)
meth public static <%0 extends java.lang.Object> org.apache.maven.model.building.Result<{%%0}> addProblems(org.apache.maven.model.building.Result<{%%0}>,java.lang.Iterable<? extends org.apache.maven.model.building.ModelProblem>)
meth public static <%0 extends java.lang.Object> org.apache.maven.model.building.Result<{%%0}> error(java.lang.Iterable<? extends org.apache.maven.model.building.ModelProblem>)
meth public static <%0 extends java.lang.Object> org.apache.maven.model.building.Result<{%%0}> error(org.apache.maven.model.building.Result<?>)
meth public static <%0 extends java.lang.Object> org.apache.maven.model.building.Result<{%%0}> error({%%0})
meth public static <%0 extends java.lang.Object> org.apache.maven.model.building.Result<{%%0}> error({%%0},java.lang.Iterable<? extends org.apache.maven.model.building.ModelProblem>)
meth public static <%0 extends java.lang.Object> org.apache.maven.model.building.Result<{%%0}> newResult({%%0},java.lang.Iterable<? extends org.apache.maven.model.building.ModelProblem>)
meth public static <%0 extends java.lang.Object> org.apache.maven.model.building.Result<{%%0}> success({%%0})
meth public static <%0 extends java.lang.Object> org.apache.maven.model.building.Result<{%%0}> success({%%0},java.lang.Iterable<? extends org.apache.maven.model.building.ModelProblem>)
meth public {org.apache.maven.model.building.Result%0} get()
supr java.lang.Object
hfds errors,problems,value

CLSS public org.apache.maven.model.building.StringModelSource
 anno 0 java.lang.Deprecated()
cons public init(java.lang.CharSequence)
cons public init(java.lang.CharSequence,java.lang.String)
intf org.apache.maven.model.building.ModelSource
supr org.apache.maven.building.StringSource

CLSS public org.apache.maven.model.building.UrlModelSource
 anno 0 java.lang.Deprecated()
cons public init(java.net.URL)
intf org.apache.maven.model.building.ModelSource
supr org.apache.maven.building.UrlSource

CLSS public org.apache.maven.model.io.DefaultModelReader
 anno 0 javax.inject.Named(java.lang.String value="")
 anno 0 javax.inject.Singleton()
cons public init()
intf org.apache.maven.model.io.ModelReader
meth public org.apache.maven.model.Model read(java.io.File,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public org.apache.maven.model.Model read(java.io.InputStream,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public org.apache.maven.model.Model read(java.io.Reader,java.util.Map<java.lang.String,?>) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.maven.model.io.DefaultModelWriter
 anno 0 javax.inject.Named(java.lang.String value="")
 anno 0 javax.inject.Singleton()
cons public init()
intf org.apache.maven.model.io.ModelWriter
meth public void write(java.io.File,java.util.Map<java.lang.String,java.lang.Object>,org.apache.maven.model.Model) throws java.io.IOException
meth public void write(java.io.OutputStream,java.util.Map<java.lang.String,java.lang.Object>,org.apache.maven.model.Model) throws java.io.IOException
meth public void write(java.io.Writer,java.util.Map<java.lang.String,java.lang.Object>,org.apache.maven.model.Model) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.maven.model.io.ModelParseException
cons public init(java.lang.String,int,int)
cons public init(java.lang.String,int,int,java.lang.Throwable)
meth public int getColumnNumber()
meth public int getLineNumber()
supr java.io.IOException
hfds columnNumber,lineNumber

CLSS public abstract interface org.apache.maven.model.io.ModelReader
fld public final static java.lang.String INPUT_SOURCE = "org.apache.maven.model.io.inputSource"
fld public final static java.lang.String IS_STRICT = "org.apache.maven.model.io.isStrict"
meth public abstract org.apache.maven.model.Model read(java.io.File,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public abstract org.apache.maven.model.Model read(java.io.InputStream,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public abstract org.apache.maven.model.Model read(java.io.Reader,java.util.Map<java.lang.String,?>) throws java.io.IOException

CLSS public abstract interface org.apache.maven.model.io.ModelWriter
meth public abstract void write(java.io.File,java.util.Map<java.lang.String,java.lang.Object>,org.apache.maven.model.Model) throws java.io.IOException
meth public abstract void write(java.io.OutputStream,java.util.Map<java.lang.String,java.lang.Object>,org.apache.maven.model.Model) throws java.io.IOException
meth public abstract void write(java.io.Writer,java.util.Map<java.lang.String,java.lang.Object>,org.apache.maven.model.Model) throws java.io.IOException

CLSS public org.apache.maven.model.io.xpp3.MavenXpp3Reader
cons public init()
cons public init(org.apache.maven.model.io.xpp3.MavenXpp3Reader$ContentTransformer)
fld public final org.apache.maven.model.io.xpp3.MavenXpp3Reader$ContentTransformer contentTransformer
innr public abstract interface static ContentTransformer
meth public boolean getAddDefaultEntities()
meth public org.apache.maven.model.Model read(java.io.InputStream) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.model.Model read(java.io.InputStream,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.model.Model read(java.io.Reader) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.model.Model read(java.io.Reader,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.model.Model read(org.codehaus.plexus.util.xml.pull.XmlPullParser,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public void setAddDefaultEntities(boolean)
supr java.lang.Object
hfds addDefaultEntities

CLSS public abstract interface static org.apache.maven.model.io.xpp3.MavenXpp3Reader$ContentTransformer
 outer org.apache.maven.model.io.xpp3.MavenXpp3Reader
meth public abstract java.lang.String transform(java.lang.String,java.lang.String)

CLSS public org.apache.maven.model.io.xpp3.MavenXpp3ReaderEx
cons public init()
cons public init(org.apache.maven.model.io.xpp3.MavenXpp3ReaderEx$ContentTransformer)
fld public final org.apache.maven.model.io.xpp3.MavenXpp3ReaderEx$ContentTransformer contentTransformer
innr public abstract interface static ContentTransformer
meth public boolean getAddDefaultEntities()
meth public org.apache.maven.model.Model read(java.io.InputStream,boolean,org.apache.maven.model.InputSource) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.model.Model read(java.io.Reader,boolean,org.apache.maven.model.InputSource) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.model.Model read(org.codehaus.plexus.util.xml.pull.XmlPullParser,boolean,org.apache.maven.model.InputSource) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public void setAddDefaultEntities(boolean)
supr java.lang.Object
hfds addDefaultEntities
hcls Xpp3DomBuilderInputLocationBuilder

CLSS public abstract interface static org.apache.maven.model.io.xpp3.MavenXpp3ReaderEx$ContentTransformer
 outer org.apache.maven.model.io.xpp3.MavenXpp3ReaderEx
meth public abstract java.lang.String transform(java.lang.String,java.lang.String)

CLSS public org.apache.maven.model.io.xpp3.MavenXpp3Writer
cons public init()
meth public void setFileComment(java.lang.String)
meth public void write(java.io.OutputStream,org.apache.maven.model.Model) throws java.io.IOException
meth public void write(java.io.Writer,org.apache.maven.model.Model) throws java.io.IOException
supr java.lang.Object
hfds NAMESPACE,fileComment

CLSS public org.apache.maven.model.io.xpp3.MavenXpp3WriterEx
cons public init()
fld protected org.apache.maven.model.InputLocation$StringFormatter stringFormatter
meth protected java.lang.String toString(org.apache.maven.model.InputLocation)
meth protected void writeXpp3DomToSerializer(org.codehaus.plexus.util.xml.Xpp3Dom,org.codehaus.plexus.util.xml.pull.XmlSerializer) throws java.io.IOException
meth public void setFileComment(java.lang.String)
meth public void setStringFormatter(org.apache.maven.model.InputLocation$StringFormatter)
meth public void write(java.io.OutputStream,org.apache.maven.model.Model) throws java.io.IOException
meth public void write(java.io.Writer,org.apache.maven.model.Model) throws java.io.IOException
supr java.lang.Object
hfds NAMESPACE,fileComment

CLSS abstract interface org.apache.maven.model.io.xpp3.package-info

CLSS public abstract interface org.apache.maven.model.locator.ModelLocator
meth public abstract java.io.File locatePom(java.io.File)

CLSS abstract interface org.apache.maven.model.package-info

CLSS public org.apache.maven.model.resolution.InvalidRepositoryException
cons public init(java.lang.String,org.apache.maven.model.Repository)
cons public init(java.lang.String,org.apache.maven.model.Repository,java.lang.Throwable)
meth public org.apache.maven.model.Repository getRepository()
supr java.lang.Exception
hfds repository

CLSS public abstract interface org.apache.maven.model.resolution.ModelResolver
meth public abstract org.apache.maven.model.building.ModelSource resolveModel(java.lang.String,java.lang.String,java.lang.String) throws org.apache.maven.model.resolution.UnresolvableModelException
meth public abstract org.apache.maven.model.building.ModelSource resolveModel(org.apache.maven.model.Dependency) throws org.apache.maven.model.resolution.UnresolvableModelException
meth public abstract org.apache.maven.model.building.ModelSource resolveModel(org.apache.maven.model.Parent) throws org.apache.maven.model.resolution.UnresolvableModelException
meth public abstract org.apache.maven.model.resolution.ModelResolver newCopy()
meth public abstract void addRepository(org.apache.maven.model.Repository) throws org.apache.maven.model.resolution.InvalidRepositoryException
meth public abstract void addRepository(org.apache.maven.model.Repository,boolean) throws org.apache.maven.model.resolution.InvalidRepositoryException

CLSS public org.apache.maven.model.resolution.UnresolvableModelException
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getArtifactId()
meth public java.lang.String getGroupId()
meth public java.lang.String getVersion()
supr java.lang.Exception
hfds artifactId,groupId,version

CLSS public abstract interface org.apache.maven.model.resolution.WorkspaceModelResolver
meth public abstract org.apache.maven.model.Model resolveEffectiveModel(java.lang.String,java.lang.String,java.lang.String) throws org.apache.maven.model.resolution.UnresolvableModelException
meth public abstract org.apache.maven.model.Model resolveRawModel(java.lang.String,java.lang.String,java.lang.String) throws org.apache.maven.model.resolution.UnresolvableModelException

CLSS public abstract org.apache.maven.plugin.AbstractMojo
cons public init()
intf org.apache.maven.plugin.ContextEnabled
intf org.apache.maven.plugin.Mojo
meth public java.util.Map getPluginContext()
meth public org.apache.maven.plugin.logging.Log getLog()
meth public void setLog(org.apache.maven.plugin.logging.Log)
meth public void setPluginContext(java.util.Map)
supr java.lang.Object
hfds log,pluginContext

CLSS public abstract org.apache.maven.plugin.AbstractMojoExecutionException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
fld protected java.lang.Object source
fld protected java.lang.String longMessage
meth public java.lang.Object getSource()
meth public java.lang.String getLongMessage()
supr java.lang.Exception

CLSS public abstract interface org.apache.maven.plugin.BuildPluginManager
meth public abstract org.apache.maven.plugin.descriptor.MojoDescriptor getMojoDescriptor(org.apache.maven.model.Plugin,java.lang.String,java.util.List<org.eclipse.aether.repository.RemoteRepository>,org.eclipse.aether.RepositorySystemSession) throws org.apache.maven.plugin.InvalidPluginDescriptorException,org.apache.maven.plugin.MojoNotFoundException,org.apache.maven.plugin.PluginDescriptorParsingException,org.apache.maven.plugin.PluginNotFoundException,org.apache.maven.plugin.PluginResolutionException
meth public abstract org.apache.maven.plugin.descriptor.PluginDescriptor loadPlugin(org.apache.maven.model.Plugin,java.util.List<org.eclipse.aether.repository.RemoteRepository>,org.eclipse.aether.RepositorySystemSession) throws org.apache.maven.plugin.InvalidPluginDescriptorException,org.apache.maven.plugin.PluginDescriptorParsingException,org.apache.maven.plugin.PluginNotFoundException,org.apache.maven.plugin.PluginResolutionException
meth public abstract org.codehaus.plexus.classworlds.realm.ClassRealm getPluginRealm(org.apache.maven.execution.MavenSession,org.apache.maven.plugin.descriptor.PluginDescriptor) throws org.apache.maven.plugin.PluginManagerException,org.apache.maven.plugin.PluginResolutionException
meth public abstract void executeMojo(org.apache.maven.execution.MavenSession,org.apache.maven.plugin.MojoExecution) throws org.apache.maven.plugin.MojoExecutionException,org.apache.maven.plugin.MojoFailureException,org.apache.maven.plugin.PluginConfigurationException,org.apache.maven.plugin.PluginManagerException

CLSS public abstract interface org.apache.maven.plugin.ContextEnabled
meth public abstract java.util.Map getPluginContext()
meth public abstract void setPluginContext(java.util.Map)

CLSS public org.apache.maven.plugin.CycleDetectedInPluginGraphException
cons public init(org.apache.maven.model.Plugin,org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException)
meth public org.apache.maven.model.Plugin getPlugin()
supr java.lang.Exception
hfds plugin

CLSS public org.apache.maven.plugin.DebugConfigurationListener
 anno 0 java.lang.Deprecated()
cons public init(org.codehaus.plexus.logging.Logger)
intf org.codehaus.plexus.component.configurator.ConfigurationListener
meth public void notifyFieldChangeUsingReflection(java.lang.String,java.lang.Object,java.lang.Object)
meth public void notifyFieldChangeUsingSetter(java.lang.String,java.lang.Object,java.lang.Object)
supr java.lang.Object
hfds logger

CLSS public org.apache.maven.plugin.DefaultBuildPluginManager
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.plugin.BuildPluginManager, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.plugin.BuildPluginManager
meth public org.apache.maven.plugin.descriptor.MojoDescriptor getMojoDescriptor(org.apache.maven.model.Plugin,java.lang.String,java.util.List<org.eclipse.aether.repository.RemoteRepository>,org.eclipse.aether.RepositorySystemSession) throws org.apache.maven.plugin.InvalidPluginDescriptorException,org.apache.maven.plugin.MojoNotFoundException,org.apache.maven.plugin.PluginDescriptorParsingException,org.apache.maven.plugin.PluginNotFoundException,org.apache.maven.plugin.PluginResolutionException
meth public org.apache.maven.plugin.descriptor.PluginDescriptor loadPlugin(org.apache.maven.model.Plugin,java.util.List<org.eclipse.aether.repository.RemoteRepository>,org.eclipse.aether.RepositorySystemSession) throws org.apache.maven.plugin.InvalidPluginDescriptorException,org.apache.maven.plugin.PluginDescriptorParsingException,org.apache.maven.plugin.PluginNotFoundException,org.apache.maven.plugin.PluginResolutionException
meth public org.codehaus.plexus.classworlds.realm.ClassRealm getPluginRealm(org.apache.maven.execution.MavenSession,org.apache.maven.plugin.descriptor.PluginDescriptor) throws org.apache.maven.plugin.PluginManagerException,org.apache.maven.plugin.PluginResolutionException
meth public void executeMojo(org.apache.maven.execution.MavenSession,org.apache.maven.plugin.MojoExecution) throws org.apache.maven.plugin.MojoExecutionException,org.apache.maven.plugin.MojoFailureException,org.apache.maven.plugin.PluginConfigurationException,org.apache.maven.plugin.PluginManagerException
meth public void setMojoExecutionListeners(java.util.List<org.apache.maven.execution.MojoExecutionListener>)
supr java.lang.Object
hfds legacySupport,mavenPluginManager,mojoExecutionListener,mojoExecutionListeners,scope

CLSS public org.apache.maven.plugin.DefaultExtensionRealmCache
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.plugin.ExtensionRealmCache, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
fld protected final java.util.Map<org.apache.maven.plugin.ExtensionRealmCache$Key,org.apache.maven.plugin.ExtensionRealmCache$CacheRecord> cache
innr protected static CacheKey
intf org.apache.maven.plugin.ExtensionRealmCache
intf org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable
meth public org.apache.maven.plugin.ExtensionRealmCache$CacheRecord get(org.apache.maven.plugin.ExtensionRealmCache$Key)
meth public org.apache.maven.plugin.ExtensionRealmCache$CacheRecord put(org.apache.maven.plugin.ExtensionRealmCache$Key,org.codehaus.plexus.classworlds.realm.ClassRealm,org.apache.maven.project.ExtensionDescriptor,java.util.List<org.apache.maven.artifact.Artifact>)
meth public org.apache.maven.plugin.ExtensionRealmCache$Key createKey(java.util.List<org.apache.maven.artifact.Artifact>)
meth public void dispose()
meth public void flush()
meth public void register(org.apache.maven.project.MavenProject,org.apache.maven.plugin.ExtensionRealmCache$Key,org.apache.maven.plugin.ExtensionRealmCache$CacheRecord)
supr java.lang.Object

CLSS protected static org.apache.maven.plugin.DefaultExtensionRealmCache$CacheKey
 outer org.apache.maven.plugin.DefaultExtensionRealmCache
cons public init(java.util.List<org.apache.maven.artifact.Artifact>)
intf org.apache.maven.plugin.ExtensionRealmCache$Key
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds files,hashCode,ids,sizes,timestamps

CLSS public org.apache.maven.plugin.DefaultMojosExecutionStrategy
 anno 0 javax.inject.Named(java.lang.String value="")
 anno 0 javax.inject.Singleton()
cons public init()
intf org.apache.maven.plugin.MojosExecutionStrategy
meth public void execute(java.util.List<org.apache.maven.plugin.MojoExecution>,org.apache.maven.execution.MavenSession,org.apache.maven.plugin.MojoExecutionRunner) throws org.apache.maven.lifecycle.LifecycleExecutionException
supr java.lang.Object

CLSS public org.apache.maven.plugin.DefaultPluginArtifactsCache
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.plugin.PluginArtifactsCache, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
fld protected final java.util.Map<org.apache.maven.plugin.PluginArtifactsCache$Key,org.apache.maven.plugin.PluginArtifactsCache$CacheRecord> cache
innr protected static CacheKey
intf org.apache.maven.plugin.PluginArtifactsCache
meth protected static boolean pluginEquals(org.apache.maven.model.Plugin,org.apache.maven.model.Plugin)
meth protected static int pluginHashCode(org.apache.maven.model.Plugin)
meth protected void assertUniqueKey(org.apache.maven.plugin.PluginArtifactsCache$Key)
meth public org.apache.maven.plugin.PluginArtifactsCache$CacheRecord get(org.apache.maven.plugin.PluginArtifactsCache$Key) throws org.apache.maven.plugin.PluginResolutionException
meth public org.apache.maven.plugin.PluginArtifactsCache$CacheRecord put(org.apache.maven.plugin.PluginArtifactsCache$Key,java.util.List<org.apache.maven.artifact.Artifact>)
meth public org.apache.maven.plugin.PluginArtifactsCache$CacheRecord put(org.apache.maven.plugin.PluginArtifactsCache$Key,org.apache.maven.plugin.PluginResolutionException)
meth public org.apache.maven.plugin.PluginArtifactsCache$Key createKey(org.apache.maven.model.Plugin,org.eclipse.aether.graph.DependencyFilter,java.util.List<org.eclipse.aether.repository.RemoteRepository>,org.eclipse.aether.RepositorySystemSession)
meth public void flush()
meth public void register(org.apache.maven.project.MavenProject,org.apache.maven.plugin.PluginArtifactsCache$Key,org.apache.maven.plugin.PluginArtifactsCache$CacheRecord)
supr java.lang.Object

CLSS protected static org.apache.maven.plugin.DefaultPluginArtifactsCache$CacheKey
 outer org.apache.maven.plugin.DefaultPluginArtifactsCache
cons public init(org.apache.maven.model.Plugin,org.eclipse.aether.graph.DependencyFilter,java.util.List<org.eclipse.aether.repository.RemoteRepository>,org.eclipse.aether.RepositorySystemSession)
intf org.apache.maven.plugin.PluginArtifactsCache$Key
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds filter,hashCode,localRepo,plugin,repositories,workspace

CLSS public org.apache.maven.plugin.DefaultPluginDescriptorCache
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.plugin.PluginDescriptorCache, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.plugin.PluginDescriptorCache
meth protected static org.apache.maven.plugin.descriptor.PluginDescriptor clone(org.apache.maven.plugin.descriptor.PluginDescriptor)
meth public org.apache.maven.plugin.PluginDescriptorCache$Key createKey(org.apache.maven.model.Plugin,java.util.List<org.eclipse.aether.repository.RemoteRepository>,org.eclipse.aether.RepositorySystemSession)
meth public org.apache.maven.plugin.descriptor.PluginDescriptor get(org.apache.maven.plugin.PluginDescriptorCache$Key)
meth public org.apache.maven.plugin.descriptor.PluginDescriptor get(org.apache.maven.plugin.PluginDescriptorCache$Key,org.apache.maven.plugin.PluginDescriptorCache$PluginDescriptorSupplier) throws org.apache.maven.plugin.InvalidPluginDescriptorException,org.apache.maven.plugin.PluginDescriptorParsingException,org.apache.maven.plugin.PluginResolutionException
meth public void flush()
meth public void put(org.apache.maven.plugin.PluginDescriptorCache$Key,org.apache.maven.plugin.descriptor.PluginDescriptor)
supr java.lang.Object
hfds descriptors
hcls CacheKey

CLSS public org.apache.maven.plugin.DefaultPluginRealmCache
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.plugin.PluginRealmCache, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
fld protected final java.util.Map<org.apache.maven.plugin.PluginRealmCache$Key,org.apache.maven.plugin.PluginRealmCache$CacheRecord> cache
innr protected static CacheKey
intf org.apache.maven.plugin.PluginRealmCache
intf org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable
meth protected static boolean pluginEquals(org.apache.maven.model.Plugin,org.apache.maven.model.Plugin)
meth protected static int pluginHashCode(org.apache.maven.model.Plugin)
meth public org.apache.maven.plugin.PluginRealmCache$CacheRecord get(org.apache.maven.plugin.PluginRealmCache$Key)
meth public org.apache.maven.plugin.PluginRealmCache$CacheRecord get(org.apache.maven.plugin.PluginRealmCache$Key,org.apache.maven.plugin.PluginRealmCache$PluginRealmSupplier) throws org.apache.maven.plugin.PluginContainerException,org.apache.maven.plugin.PluginResolutionException
meth public org.apache.maven.plugin.PluginRealmCache$CacheRecord put(org.apache.maven.plugin.PluginRealmCache$Key,org.codehaus.plexus.classworlds.realm.ClassRealm,java.util.List<org.apache.maven.artifact.Artifact>)
meth public org.apache.maven.plugin.PluginRealmCache$Key createKey(org.apache.maven.model.Plugin,java.lang.ClassLoader,java.util.Map<java.lang.String,java.lang.ClassLoader>,org.eclipse.aether.graph.DependencyFilter,java.util.List<org.eclipse.aether.repository.RemoteRepository>,org.eclipse.aether.RepositorySystemSession)
meth public void dispose()
meth public void flush()
meth public void register(org.apache.maven.project.MavenProject,org.apache.maven.plugin.PluginRealmCache$Key,org.apache.maven.plugin.PluginRealmCache$CacheRecord)
supr java.lang.Object

CLSS protected static org.apache.maven.plugin.DefaultPluginRealmCache$CacheKey
 outer org.apache.maven.plugin.DefaultPluginRealmCache
cons public init(org.apache.maven.model.Plugin,java.lang.ClassLoader,java.util.Map<java.lang.String,java.lang.ClassLoader>,org.eclipse.aether.graph.DependencyFilter,java.util.List<org.eclipse.aether.repository.RemoteRepository>,org.eclipse.aether.RepositorySystemSession)
intf org.apache.maven.plugin.PluginRealmCache$Key
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds filter,foreignImports,hashCode,localRepo,parentRealm,plugin,repositories,workspace

CLSS public abstract interface org.apache.maven.plugin.ExtensionRealmCache
innr public abstract interface static Key
innr public static CacheRecord
meth public abstract org.apache.maven.plugin.ExtensionRealmCache$CacheRecord get(org.apache.maven.plugin.ExtensionRealmCache$Key)
meth public abstract org.apache.maven.plugin.ExtensionRealmCache$CacheRecord put(org.apache.maven.plugin.ExtensionRealmCache$Key,org.codehaus.plexus.classworlds.realm.ClassRealm,org.apache.maven.project.ExtensionDescriptor,java.util.List<org.apache.maven.artifact.Artifact>)
meth public abstract org.apache.maven.plugin.ExtensionRealmCache$Key createKey(java.util.List<org.apache.maven.artifact.Artifact>)
meth public abstract void flush()
meth public abstract void register(org.apache.maven.project.MavenProject,org.apache.maven.plugin.ExtensionRealmCache$Key,org.apache.maven.plugin.ExtensionRealmCache$CacheRecord)

CLSS public static org.apache.maven.plugin.ExtensionRealmCache$CacheRecord
 outer org.apache.maven.plugin.ExtensionRealmCache
meth public java.util.List<org.apache.maven.artifact.Artifact> getArtifacts()
meth public org.apache.maven.project.ExtensionDescriptor getDescriptor()
meth public org.codehaus.plexus.classworlds.realm.ClassRealm getRealm()
supr java.lang.Object
hfds artifacts,descriptor,realm

CLSS public abstract interface static org.apache.maven.plugin.ExtensionRealmCache$Key
 outer org.apache.maven.plugin.ExtensionRealmCache

CLSS public org.apache.maven.plugin.InvalidPluginDescriptorException
cons public init(java.lang.String,java.util.List<java.lang.String>)
supr java.lang.Exception

CLSS public org.apache.maven.plugin.InvalidPluginException
cons public init(java.lang.String)
cons public init(java.lang.String,org.apache.maven.project.ProjectBuildingException)
cons public init(java.lang.String,org.apache.maven.project.artifact.InvalidDependencyVersionException)
supr java.lang.Exception

CLSS public abstract interface org.apache.maven.plugin.LegacySupport
meth public abstract org.apache.maven.execution.MavenSession getSession()
meth public abstract org.eclipse.aether.RepositorySystemSession getRepositorySession()
meth public abstract void setSession(org.apache.maven.execution.MavenSession)

CLSS public abstract interface org.apache.maven.plugin.MavenPluginManager
meth public abstract <%0 extends java.lang.Object> {%%0} getConfiguredMojo(java.lang.Class<{%%0}>,org.apache.maven.execution.MavenSession,org.apache.maven.plugin.MojoExecution) throws org.apache.maven.plugin.PluginConfigurationException,org.apache.maven.plugin.PluginContainerException
meth public abstract org.apache.maven.plugin.ExtensionRealmCache$CacheRecord setupExtensionsRealm(org.apache.maven.project.MavenProject,org.apache.maven.model.Plugin,org.eclipse.aether.RepositorySystemSession) throws org.apache.maven.plugin.PluginManagerException
meth public abstract org.apache.maven.plugin.descriptor.MojoDescriptor getMojoDescriptor(org.apache.maven.model.Plugin,java.lang.String,java.util.List<org.eclipse.aether.repository.RemoteRepository>,org.eclipse.aether.RepositorySystemSession) throws org.apache.maven.plugin.InvalidPluginDescriptorException,org.apache.maven.plugin.MojoNotFoundException,org.apache.maven.plugin.PluginDescriptorParsingException,org.apache.maven.plugin.PluginResolutionException
meth public abstract org.apache.maven.plugin.descriptor.PluginDescriptor getPluginDescriptor(org.apache.maven.model.Plugin,java.util.List<org.eclipse.aether.repository.RemoteRepository>,org.eclipse.aether.RepositorySystemSession) throws org.apache.maven.plugin.InvalidPluginDescriptorException,org.apache.maven.plugin.PluginDescriptorParsingException,org.apache.maven.plugin.PluginResolutionException
meth public abstract void checkRequiredMavenVersion(org.apache.maven.plugin.descriptor.PluginDescriptor) throws org.apache.maven.plugin.PluginIncompatibleException
meth public abstract void releaseMojo(java.lang.Object,org.apache.maven.plugin.MojoExecution)
meth public abstract void setupPluginRealm(org.apache.maven.plugin.descriptor.PluginDescriptor,org.apache.maven.execution.MavenSession,java.lang.ClassLoader,java.util.List<java.lang.String>,org.eclipse.aether.graph.DependencyFilter) throws org.apache.maven.plugin.PluginContainerException,org.apache.maven.plugin.PluginResolutionException

CLSS public org.apache.maven.plugin.MavenPluginValidator
cons public init(org.apache.maven.artifact.Artifact)
meth public boolean hasErrors()
meth public java.util.List<java.lang.String> getErrors()
meth public void validate(org.apache.maven.plugin.descriptor.PluginDescriptor)
supr java.lang.Object
hfds errors,firstDescriptor,pluginArtifact

CLSS public abstract interface org.apache.maven.plugin.Mojo
fld public final static java.lang.String ROLE
meth public abstract org.apache.maven.plugin.logging.Log getLog()
meth public abstract void execute() throws org.apache.maven.plugin.MojoExecutionException,org.apache.maven.plugin.MojoFailureException
meth public abstract void setLog(org.apache.maven.plugin.logging.Log)

CLSS public org.apache.maven.plugin.MojoExecution
cons public init(org.apache.maven.model.Plugin,java.lang.String,java.lang.String)
cons public init(org.apache.maven.plugin.descriptor.MojoDescriptor)
cons public init(org.apache.maven.plugin.descriptor.MojoDescriptor,java.lang.String)
cons public init(org.apache.maven.plugin.descriptor.MojoDescriptor,java.lang.String,org.apache.maven.plugin.MojoExecution$Source)
cons public init(org.apache.maven.plugin.descriptor.MojoDescriptor,org.codehaus.plexus.util.xml.Xpp3Dom)
innr public final static !enum Source
meth public java.lang.String getArtifactId()
meth public java.lang.String getExecutionId()
meth public java.lang.String getGoal()
meth public java.lang.String getGroupId()
meth public java.lang.String getLifecyclePhase()
meth public java.lang.String getVersion()
meth public java.lang.String identify()
meth public java.lang.String toString()
meth public java.util.Map<java.lang.String,java.util.List<org.apache.maven.plugin.MojoExecution>> getForkedExecutions()
meth public org.apache.maven.model.Plugin getPlugin()
meth public org.apache.maven.plugin.MojoExecution$Source getSource()
meth public org.apache.maven.plugin.descriptor.MojoDescriptor getMojoDescriptor()
meth public org.codehaus.plexus.util.xml.Xpp3Dom getConfiguration()
meth public void setConfiguration(org.codehaus.plexus.util.xml.Xpp3Dom)
meth public void setForkedExecutions(java.lang.String,java.util.List<org.apache.maven.plugin.MojoExecution>)
meth public void setLifecyclePhase(java.lang.String)
meth public void setMojoDescriptor(org.apache.maven.plugin.descriptor.MojoDescriptor)
supr java.lang.Object
hfds configuration,executionId,forkedExecutions,goal,lifecyclePhase,mojoDescriptor,plugin,source

CLSS public final static !enum org.apache.maven.plugin.MojoExecution$Source
 outer org.apache.maven.plugin.MojoExecution
fld public final static org.apache.maven.plugin.MojoExecution$Source CLI
fld public final static org.apache.maven.plugin.MojoExecution$Source LIFECYCLE
meth public static org.apache.maven.plugin.MojoExecution$Source valueOf(java.lang.String)
meth public static org.apache.maven.plugin.MojoExecution$Source[] values()
supr java.lang.Enum<org.apache.maven.plugin.MojoExecution$Source>

CLSS public org.apache.maven.plugin.MojoExecutionException
cons public init(java.lang.Object,java.lang.String,java.lang.String)
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr org.apache.maven.plugin.AbstractMojoExecutionException

CLSS public abstract interface org.apache.maven.plugin.MojoExecutionRunner
meth public abstract void run(org.apache.maven.plugin.MojoExecution) throws org.apache.maven.lifecycle.LifecycleExecutionException

CLSS public org.apache.maven.plugin.MojoFailureException
cons public init(java.lang.Object,java.lang.String,java.lang.String)
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr org.apache.maven.plugin.AbstractMojoExecutionException

CLSS public org.apache.maven.plugin.MojoNotFoundException
cons public init(java.lang.String,org.apache.maven.plugin.descriptor.PluginDescriptor)
meth public java.lang.String getGoal()
meth public org.apache.maven.plugin.descriptor.PluginDescriptor getPluginDescriptor()
supr java.lang.Exception
hfds goal,pluginDescriptor

CLSS public abstract interface org.apache.maven.plugin.MojosExecutionStrategy
meth public abstract void execute(java.util.List<org.apache.maven.plugin.MojoExecution>,org.apache.maven.execution.MavenSession,org.apache.maven.plugin.MojoExecutionRunner) throws org.apache.maven.lifecycle.LifecycleExecutionException

CLSS public abstract interface org.apache.maven.plugin.PluginArtifactsCache
innr public abstract interface static Key
innr public static CacheRecord
meth public abstract org.apache.maven.plugin.PluginArtifactsCache$CacheRecord get(org.apache.maven.plugin.PluginArtifactsCache$Key) throws org.apache.maven.plugin.PluginResolutionException
meth public abstract org.apache.maven.plugin.PluginArtifactsCache$CacheRecord put(org.apache.maven.plugin.PluginArtifactsCache$Key,java.util.List<org.apache.maven.artifact.Artifact>)
meth public abstract org.apache.maven.plugin.PluginArtifactsCache$CacheRecord put(org.apache.maven.plugin.PluginArtifactsCache$Key,org.apache.maven.plugin.PluginResolutionException)
meth public abstract org.apache.maven.plugin.PluginArtifactsCache$Key createKey(org.apache.maven.model.Plugin,org.eclipse.aether.graph.DependencyFilter,java.util.List<org.eclipse.aether.repository.RemoteRepository>,org.eclipse.aether.RepositorySystemSession)
meth public abstract void flush()
meth public abstract void register(org.apache.maven.project.MavenProject,org.apache.maven.plugin.PluginArtifactsCache$Key,org.apache.maven.plugin.PluginArtifactsCache$CacheRecord)

CLSS public static org.apache.maven.plugin.PluginArtifactsCache$CacheRecord
 outer org.apache.maven.plugin.PluginArtifactsCache
cons public init(java.util.List<org.apache.maven.artifact.Artifact>)
cons public init(org.apache.maven.plugin.PluginResolutionException)
meth public java.util.List<org.apache.maven.artifact.Artifact> getArtifacts()
meth public org.apache.maven.plugin.PluginResolutionException getException()
supr java.lang.Object
hfds artifacts,exception

CLSS public abstract interface static org.apache.maven.plugin.PluginArtifactsCache$Key
 outer org.apache.maven.plugin.PluginArtifactsCache

CLSS public org.apache.maven.plugin.PluginConfigurationException
cons public init(org.apache.maven.plugin.descriptor.PluginDescriptor,java.lang.String)
cons public init(org.apache.maven.plugin.descriptor.PluginDescriptor,java.lang.String,java.lang.Throwable)
cons public init(org.apache.maven.plugin.descriptor.PluginDescriptor,java.lang.String,org.codehaus.plexus.component.configurator.ComponentConfigurationException)
cons public init(org.apache.maven.plugin.descriptor.PluginDescriptor,java.lang.String,org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException)
cons public init(org.apache.maven.plugin.descriptor.PluginDescriptor,java.lang.String,org.codehaus.plexus.component.repository.exception.ComponentLookupException)
supr java.lang.Exception
hfds originalMessage,pluginDescriptor

CLSS public org.apache.maven.plugin.PluginContainerException
cons public init(org.apache.maven.model.Plugin,org.codehaus.plexus.classworlds.realm.ClassRealm,java.lang.String,java.lang.Throwable)
cons public init(org.apache.maven.model.Plugin,org.codehaus.plexus.classworlds.realm.ClassRealm,java.lang.String,org.codehaus.plexus.component.repository.exception.ComponentRepositoryException)
cons public init(org.apache.maven.model.Plugin,org.codehaus.plexus.classworlds.realm.ClassRealm,java.lang.String,org.codehaus.plexus.configuration.PlexusConfigurationException)
cons public init(org.apache.maven.plugin.descriptor.MojoDescriptor,org.codehaus.plexus.classworlds.realm.ClassRealm,java.lang.String,java.lang.Throwable)
cons public init(org.apache.maven.plugin.descriptor.MojoDescriptor,org.codehaus.plexus.classworlds.realm.ClassRealm,java.lang.String,org.codehaus.plexus.component.repository.exception.ComponentLookupException)
meth public org.codehaus.plexus.classworlds.realm.ClassRealm getPluginRealm()
supr org.apache.maven.plugin.PluginManagerException
hfds pluginRealm

CLSS public abstract interface org.apache.maven.plugin.PluginDescriptorCache
innr public abstract interface static Key
innr public abstract interface static PluginDescriptorSupplier
meth public abstract org.apache.maven.plugin.PluginDescriptorCache$Key createKey(org.apache.maven.model.Plugin,java.util.List<org.eclipse.aether.repository.RemoteRepository>,org.eclipse.aether.RepositorySystemSession)
meth public abstract org.apache.maven.plugin.descriptor.PluginDescriptor get(org.apache.maven.plugin.PluginDescriptorCache$Key)
meth public abstract void flush()
meth public abstract void put(org.apache.maven.plugin.PluginDescriptorCache$Key,org.apache.maven.plugin.descriptor.PluginDescriptor)
meth public org.apache.maven.plugin.descriptor.PluginDescriptor get(org.apache.maven.plugin.PluginDescriptorCache$Key,org.apache.maven.plugin.PluginDescriptorCache$PluginDescriptorSupplier) throws org.apache.maven.plugin.InvalidPluginDescriptorException,org.apache.maven.plugin.PluginDescriptorParsingException,org.apache.maven.plugin.PluginResolutionException

CLSS public abstract interface static org.apache.maven.plugin.PluginDescriptorCache$Key
 outer org.apache.maven.plugin.PluginDescriptorCache

CLSS public abstract interface static org.apache.maven.plugin.PluginDescriptorCache$PluginDescriptorSupplier
 outer org.apache.maven.plugin.PluginDescriptorCache
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.maven.plugin.descriptor.PluginDescriptor load() throws org.apache.maven.plugin.InvalidPluginDescriptorException,org.apache.maven.plugin.PluginDescriptorParsingException,org.apache.maven.plugin.PluginResolutionException

CLSS public org.apache.maven.plugin.PluginDescriptorParsingException
cons public init(org.apache.maven.model.Plugin,java.lang.String,java.lang.Throwable)
supr java.lang.Exception

CLSS public org.apache.maven.plugin.PluginExecutionException
cons public init(org.apache.maven.plugin.MojoExecution,org.apache.maven.project.MavenProject,java.lang.Exception)
cons public init(org.apache.maven.plugin.MojoExecution,org.apache.maven.project.MavenProject,java.lang.String)
cons public init(org.apache.maven.plugin.MojoExecution,org.apache.maven.project.MavenProject,java.lang.String,java.lang.Throwable)
cons public init(org.apache.maven.plugin.MojoExecution,org.apache.maven.project.MavenProject,org.apache.maven.project.DuplicateArtifactAttachmentException)
meth public org.apache.maven.plugin.MojoExecution getMojoExecution()
supr org.apache.maven.plugin.PluginManagerException
hfds mojoExecution

CLSS public org.apache.maven.plugin.PluginIncompatibleException
cons public init(org.apache.maven.model.Plugin,java.lang.String)
supr org.apache.maven.plugin.PluginManagerException

CLSS public org.apache.maven.plugin.PluginLoaderException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(org.apache.maven.model.Plugin,java.lang.String)
cons public init(org.apache.maven.model.Plugin,java.lang.String,org.apache.maven.artifact.resolver.ArtifactNotFoundException)
cons public init(org.apache.maven.model.Plugin,java.lang.String,org.apache.maven.artifact.resolver.ArtifactResolutionException)
cons public init(org.apache.maven.model.Plugin,java.lang.String,org.apache.maven.artifact.versioning.InvalidVersionSpecificationException)
cons public init(org.apache.maven.model.Plugin,java.lang.String,org.apache.maven.plugin.InvalidPluginException)
cons public init(org.apache.maven.model.Plugin,java.lang.String,org.apache.maven.plugin.PluginManagerException)
cons public init(org.apache.maven.model.Plugin,java.lang.String,org.apache.maven.plugin.PluginNotFoundException)
cons public init(org.apache.maven.model.Plugin,java.lang.String,org.apache.maven.plugin.version.PluginVersionNotFoundException)
cons public init(org.apache.maven.model.Plugin,java.lang.String,org.apache.maven.plugin.version.PluginVersionResolutionException)
cons public init(org.apache.maven.model.ReportPlugin,java.lang.String)
cons public init(org.apache.maven.model.ReportPlugin,java.lang.String,java.lang.Throwable)
meth public java.lang.String getPluginKey()
supr java.lang.Exception
hfds pluginKey

CLSS public abstract interface org.apache.maven.plugin.PluginManager
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String ROLE
meth public abstract java.lang.Object getPluginComponent(org.apache.maven.model.Plugin,java.lang.String,java.lang.String) throws org.apache.maven.plugin.PluginManagerException,org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getPluginComponents(org.apache.maven.model.Plugin,java.lang.String) throws org.apache.maven.plugin.PluginManagerException,org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public abstract org.apache.maven.model.Plugin getPluginDefinitionForPrefix(java.lang.String,org.apache.maven.execution.MavenSession,org.apache.maven.project.MavenProject)
meth public abstract org.apache.maven.plugin.descriptor.PluginDescriptor getPluginDescriptorForPrefix(java.lang.String)
meth public abstract org.apache.maven.plugin.descriptor.PluginDescriptor loadPluginDescriptor(org.apache.maven.model.Plugin,org.apache.maven.project.MavenProject,org.apache.maven.execution.MavenSession) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException,org.apache.maven.artifact.versioning.InvalidVersionSpecificationException,org.apache.maven.plugin.InvalidPluginException,org.apache.maven.plugin.PluginManagerException,org.apache.maven.plugin.PluginNotFoundException,org.apache.maven.plugin.version.PluginVersionNotFoundException,org.apache.maven.plugin.version.PluginVersionResolutionException
meth public abstract org.apache.maven.plugin.descriptor.PluginDescriptor loadPluginFully(org.apache.maven.model.Plugin,org.apache.maven.project.MavenProject,org.apache.maven.execution.MavenSession) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException,org.apache.maven.artifact.versioning.InvalidVersionSpecificationException,org.apache.maven.plugin.InvalidPluginException,org.apache.maven.plugin.PluginManagerException,org.apache.maven.plugin.PluginNotFoundException,org.apache.maven.plugin.version.PluginVersionNotFoundException,org.apache.maven.plugin.version.PluginVersionResolutionException
meth public abstract org.apache.maven.plugin.descriptor.PluginDescriptor verifyPlugin(org.apache.maven.model.Plugin,org.apache.maven.project.MavenProject,org.apache.maven.settings.Settings,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException,org.apache.maven.artifact.versioning.InvalidVersionSpecificationException,org.apache.maven.plugin.InvalidPluginException,org.apache.maven.plugin.PluginManagerException,org.apache.maven.plugin.PluginNotFoundException,org.apache.maven.plugin.version.PluginVersionNotFoundException,org.apache.maven.plugin.version.PluginVersionResolutionException
meth public abstract void executeMojo(org.apache.maven.project.MavenProject,org.apache.maven.plugin.MojoExecution,org.apache.maven.execution.MavenSession) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException,org.apache.maven.plugin.MojoExecutionException,org.apache.maven.plugin.MojoFailureException,org.apache.maven.plugin.PluginConfigurationException,org.apache.maven.plugin.PluginManagerException,org.apache.maven.project.artifact.InvalidDependencyVersionException

CLSS public org.apache.maven.plugin.PluginManagerException
cons protected init(org.apache.maven.model.Plugin,java.lang.String,org.apache.maven.project.MavenProject,java.lang.Throwable)
cons protected init(org.apache.maven.plugin.descriptor.MojoDescriptor,java.lang.String,java.lang.Throwable)
cons protected init(org.apache.maven.plugin.descriptor.MojoDescriptor,org.apache.maven.project.MavenProject,java.lang.String)
cons protected init(org.apache.maven.plugin.descriptor.MojoDescriptor,org.apache.maven.project.MavenProject,java.lang.String,java.lang.Throwable)
cons public init(org.apache.maven.model.Plugin,java.lang.String,java.lang.Throwable)
cons public init(org.apache.maven.model.Plugin,java.lang.String,org.apache.maven.project.MavenProject)
cons public init(org.apache.maven.model.Plugin,java.lang.String,org.codehaus.plexus.PlexusContainerException)
cons public init(org.apache.maven.model.Plugin,java.lang.String,org.codehaus.plexus.component.repository.exception.ComponentRepositoryException)
cons public init(org.apache.maven.model.Plugin,java.lang.String,org.codehaus.plexus.configuration.PlexusConfigurationException)
cons public init(org.apache.maven.model.Plugin,org.apache.maven.artifact.versioning.InvalidVersionSpecificationException)
cons public init(org.apache.maven.plugin.descriptor.MojoDescriptor,java.lang.String,org.apache.maven.project.MavenProject,org.codehaus.plexus.PlexusContainerException)
cons public init(org.apache.maven.plugin.descriptor.MojoDescriptor,org.apache.maven.project.MavenProject,java.lang.String,org.codehaus.plexus.classworlds.realm.NoSuchRealmException)
meth public java.lang.String getGoal()
meth public java.lang.String getPluginArtifactId()
meth public java.lang.String getPluginGroupId()
meth public java.lang.String getPluginVersion()
meth public org.apache.maven.project.MavenProject getProject()
supr java.lang.Exception
hfds goal,pluginArtifactId,pluginGroupId,pluginVersion,project

CLSS public org.apache.maven.plugin.PluginNotFoundException
cons public init(org.apache.maven.model.Plugin,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
cons public init(org.apache.maven.model.Plugin,org.apache.maven.artifact.resolver.ArtifactNotFoundException)
meth public org.apache.maven.model.Plugin getPlugin()
supr org.apache.maven.artifact.resolver.AbstractArtifactResolutionException
hfds plugin

CLSS public org.apache.maven.plugin.PluginParameterException
cons public init(org.apache.maven.plugin.descriptor.MojoDescriptor,java.util.List<org.apache.maven.plugin.descriptor.Parameter>)
meth public java.lang.String buildDiagnosticMessage()
meth public java.util.List<org.apache.maven.plugin.descriptor.Parameter> getParameters()
meth public org.apache.maven.plugin.descriptor.MojoDescriptor getMojoDescriptor()
supr org.apache.maven.plugin.PluginConfigurationException
hfds mojo,parameters

CLSS public org.apache.maven.plugin.PluginParameterExpressionEvaluator
cons public init(org.apache.maven.execution.MavenSession)
cons public init(org.apache.maven.execution.MavenSession,org.apache.maven.plugin.MojoExecution)
cons public init(org.apache.maven.execution.MavenSession,org.apache.maven.plugin.MojoExecution,org.apache.maven.project.path.PathTranslator,org.codehaus.plexus.logging.Logger,org.apache.maven.project.MavenProject,java.util.Properties)
 anno 0 java.lang.Deprecated()
intf org.codehaus.plexus.component.configurator.expression.TypeAwareExpressionEvaluator
meth public java.io.File alignToBaseDirectory(java.io.File)
meth public java.lang.Object evaluate(java.lang.String) throws org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException
meth public java.lang.Object evaluate(java.lang.String,java.lang.Class<?>) throws org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException
supr java.lang.Object
hfds basedir,mojoExecution,project,properties,session

CLSS public abstract interface org.apache.maven.plugin.PluginRealmCache
innr public abstract interface static Key
innr public abstract interface static PluginRealmSupplier
innr public static CacheRecord
meth public abstract org.apache.maven.plugin.PluginRealmCache$CacheRecord get(org.apache.maven.plugin.PluginRealmCache$Key)
meth public abstract org.apache.maven.plugin.PluginRealmCache$CacheRecord put(org.apache.maven.plugin.PluginRealmCache$Key,org.codehaus.plexus.classworlds.realm.ClassRealm,java.util.List<org.apache.maven.artifact.Artifact>)
meth public abstract org.apache.maven.plugin.PluginRealmCache$Key createKey(org.apache.maven.model.Plugin,java.lang.ClassLoader,java.util.Map<java.lang.String,java.lang.ClassLoader>,org.eclipse.aether.graph.DependencyFilter,java.util.List<org.eclipse.aether.repository.RemoteRepository>,org.eclipse.aether.RepositorySystemSession)
meth public abstract void flush()
meth public abstract void register(org.apache.maven.project.MavenProject,org.apache.maven.plugin.PluginRealmCache$Key,org.apache.maven.plugin.PluginRealmCache$CacheRecord)
meth public org.apache.maven.plugin.PluginRealmCache$CacheRecord get(org.apache.maven.plugin.PluginRealmCache$Key,org.apache.maven.plugin.PluginRealmCache$PluginRealmSupplier) throws org.apache.maven.plugin.PluginContainerException,org.apache.maven.plugin.PluginResolutionException

CLSS public static org.apache.maven.plugin.PluginRealmCache$CacheRecord
 outer org.apache.maven.plugin.PluginRealmCache
cons public init(org.codehaus.plexus.classworlds.realm.ClassRealm,java.util.List<org.apache.maven.artifact.Artifact>)
meth public java.util.List<org.apache.maven.artifact.Artifact> getArtifacts()
meth public org.codehaus.plexus.classworlds.realm.ClassRealm getRealm()
supr java.lang.Object
hfds artifacts,realm

CLSS public abstract interface static org.apache.maven.plugin.PluginRealmCache$Key
 outer org.apache.maven.plugin.PluginRealmCache

CLSS public abstract interface static org.apache.maven.plugin.PluginRealmCache$PluginRealmSupplier
 outer org.apache.maven.plugin.PluginRealmCache
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.maven.plugin.PluginRealmCache$CacheRecord load() throws org.apache.maven.plugin.PluginContainerException,org.apache.maven.plugin.PluginResolutionException

CLSS public org.apache.maven.plugin.PluginResolutionException
cons public init(org.apache.maven.model.Plugin,java.lang.Throwable)
meth public org.apache.maven.model.Plugin getPlugin()
supr java.lang.Exception
hfds plugin

CLSS public abstract interface org.apache.maven.plugin.PluginValidationManager
innr public final static !enum IssueLocality
meth public abstract void reportPluginMojoValidationIssue(org.apache.maven.plugin.PluginValidationManager$IssueLocality,org.apache.maven.execution.MavenSession,org.apache.maven.plugin.descriptor.MojoDescriptor,java.lang.Class<?>,java.lang.String)
meth public abstract void reportPluginValidationIssue(org.apache.maven.plugin.PluginValidationManager$IssueLocality,org.apache.maven.execution.MavenSession,org.apache.maven.plugin.descriptor.MojoDescriptor,java.lang.String)
meth public abstract void reportPluginValidationIssue(org.apache.maven.plugin.PluginValidationManager$IssueLocality,org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.artifact.Artifact,java.lang.String)

CLSS public final static !enum org.apache.maven.plugin.PluginValidationManager$IssueLocality
 outer org.apache.maven.plugin.PluginValidationManager
fld public final static org.apache.maven.plugin.PluginValidationManager$IssueLocality EXTERNAL
fld public final static org.apache.maven.plugin.PluginValidationManager$IssueLocality INTERNAL
meth public static org.apache.maven.plugin.PluginValidationManager$IssueLocality valueOf(java.lang.String)
meth public static org.apache.maven.plugin.PluginValidationManager$IssueLocality[] values()
supr java.lang.Enum<org.apache.maven.plugin.PluginValidationManager$IssueLocality>

CLSS public org.apache.maven.profiles.Activation
cons public init()
intf java.io.Serializable
meth public boolean isActiveByDefault()
meth public java.lang.String getJdk()
meth public org.apache.maven.profiles.ActivationFile getFile()
meth public org.apache.maven.profiles.ActivationOS getOs()
meth public org.apache.maven.profiles.ActivationProperty getProperty()
meth public void setActiveByDefault(boolean)
meth public void setFile(org.apache.maven.profiles.ActivationFile)
meth public void setJdk(java.lang.String)
meth public void setOs(org.apache.maven.profiles.ActivationOS)
meth public void setProperty(org.apache.maven.profiles.ActivationProperty)
supr java.lang.Object
hfds activeByDefault,file,jdk,os,property

CLSS public org.apache.maven.profiles.ActivationFile
cons public init()
intf java.io.Serializable
meth public java.lang.String getExists()
meth public java.lang.String getMissing()
meth public void setExists(java.lang.String)
meth public void setMissing(java.lang.String)
supr java.lang.Object
hfds exists,missing

CLSS public org.apache.maven.profiles.ActivationOS
cons public init()
intf java.io.Serializable
meth public java.lang.String getArch()
meth public java.lang.String getFamily()
meth public java.lang.String getName()
meth public java.lang.String getVersion()
meth public void setArch(java.lang.String)
meth public void setFamily(java.lang.String)
meth public void setName(java.lang.String)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds arch,family,name,version

CLSS public org.apache.maven.profiles.ActivationProperty
cons public init()
intf java.io.Serializable
meth public java.lang.String getName()
meth public java.lang.String getValue()
meth public void setName(java.lang.String)
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds name,value

CLSS public org.apache.maven.profiles.DefaultMavenProfilesBuilder
 anno 0 java.lang.Deprecated()
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.profiles.MavenProfilesBuilder, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.profiles.MavenProfilesBuilder
meth public org.apache.maven.profiles.ProfilesRoot buildProfiles(java.io.File) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
supr org.codehaus.plexus.logging.AbstractLogEnabled
hfds PROFILES_XML_FILE

CLSS public org.apache.maven.profiles.DefaultProfileManager
 anno 0 java.lang.Deprecated()
cons public init(org.codehaus.plexus.PlexusContainer)
 anno 0 java.lang.Deprecated()
cons public init(org.codehaus.plexus.PlexusContainer,java.util.Properties)
intf org.apache.maven.profiles.ProfileManager
meth public java.util.List getActiveProfiles() throws org.apache.maven.profiles.activation.ProfileActivationException
meth public java.util.List getIdsActivatedByDefault()
meth public java.util.List<java.lang.String> getExplicitlyActivatedIds()
meth public java.util.List<java.lang.String> getExplicitlyDeactivatedIds()
meth public java.util.Map<java.lang.String,org.apache.maven.model.Profile> getProfilesById()
meth public java.util.Properties getRequestProperties()
meth public void activateAsDefault(java.lang.String)
meth public void addProfile(org.apache.maven.model.Profile)
meth public void addProfiles(java.util.List<org.apache.maven.model.Profile>)
meth public void explicitlyActivate(java.lang.String)
meth public void explicitlyActivate(java.util.List<java.lang.String>)
meth public void explicitlyDeactivate(java.lang.String)
meth public void explicitlyDeactivate(java.util.List<java.lang.String>)
supr java.lang.Object
hfds activatedIds,deactivatedIds,defaultIds,logger,profileSelector,profilesById,requestProperties

CLSS public abstract interface org.apache.maven.profiles.MavenProfilesBuilder
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String ROLE
meth public abstract org.apache.maven.profiles.ProfilesRoot buildProfiles(java.io.File) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException

CLSS public org.apache.maven.profiles.Profile
cons public init()
intf java.io.Serializable
meth public java.lang.String getId()
meth public java.util.List<org.apache.maven.profiles.Repository> getPluginRepositories()
meth public java.util.List<org.apache.maven.profiles.Repository> getRepositories()
meth public java.util.Properties getProperties()
meth public org.apache.maven.profiles.Activation getActivation()
meth public void addPluginRepository(org.apache.maven.profiles.Repository)
meth public void addProperty(java.lang.String,java.lang.String)
meth public void addRepository(org.apache.maven.profiles.Repository)
meth public void removePluginRepository(org.apache.maven.profiles.Repository)
meth public void removeRepository(org.apache.maven.profiles.Repository)
meth public void setActivation(org.apache.maven.profiles.Activation)
meth public void setId(java.lang.String)
meth public void setPluginRepositories(java.util.List<org.apache.maven.profiles.Repository>)
meth public void setProperties(java.util.Properties)
meth public void setRepositories(java.util.List<org.apache.maven.profiles.Repository>)
supr java.lang.Object
hfds activation,id,pluginRepositories,properties,repositories

CLSS public abstract interface org.apache.maven.profiles.ProfileManager
 anno 0 java.lang.Deprecated()
meth public abstract java.util.List getActiveProfiles() throws org.apache.maven.profiles.activation.ProfileActivationException
meth public abstract java.util.List getIdsActivatedByDefault()
meth public abstract java.util.List<java.lang.String> getExplicitlyActivatedIds()
meth public abstract java.util.List<java.lang.String> getExplicitlyDeactivatedIds()
meth public abstract java.util.Map getProfilesById()
meth public abstract java.util.Properties getRequestProperties()
meth public abstract void addProfile(org.apache.maven.model.Profile)
meth public abstract void addProfiles(java.util.List<org.apache.maven.model.Profile>)
meth public abstract void explicitlyActivate(java.lang.String)
meth public abstract void explicitlyActivate(java.util.List<java.lang.String>)
meth public abstract void explicitlyDeactivate(java.lang.String)
meth public abstract void explicitlyDeactivate(java.util.List<java.lang.String>)

CLSS public org.apache.maven.profiles.ProfilesConversionUtils
 anno 0 java.lang.Deprecated()
meth public static org.apache.maven.model.Profile convertFromProfileXmlProfile(org.apache.maven.profiles.Profile)
supr java.lang.Object

CLSS public org.apache.maven.profiles.ProfilesRoot
cons public init()
intf java.io.Serializable
meth public java.lang.String getModelEncoding()
meth public java.util.List<java.lang.String> getActiveProfiles()
meth public java.util.List<org.apache.maven.profiles.Profile> getProfiles()
meth public void addActiveProfile(java.lang.String)
meth public void addProfile(org.apache.maven.profiles.Profile)
meth public void removeActiveProfile(java.lang.String)
meth public void removeProfile(org.apache.maven.profiles.Profile)
meth public void setActiveProfiles(java.util.List<java.lang.String>)
meth public void setModelEncoding(java.lang.String)
meth public void setProfiles(java.util.List<org.apache.maven.profiles.Profile>)
supr java.lang.Object
hfds activeProfiles,modelEncoding,profiles

CLSS public org.apache.maven.profiles.Repository
cons public init()
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public org.apache.maven.profiles.RepositoryPolicy getReleases()
meth public org.apache.maven.profiles.RepositoryPolicy getSnapshots()
meth public void setReleases(org.apache.maven.profiles.RepositoryPolicy)
meth public void setSnapshots(org.apache.maven.profiles.RepositoryPolicy)
supr org.apache.maven.profiles.RepositoryBase
hfds releases,snapshots

CLSS public org.apache.maven.profiles.RepositoryBase
cons public init()
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public java.lang.String getId()
meth public java.lang.String getLayout()
meth public java.lang.String getName()
meth public java.lang.String getUrl()
meth public void setId(java.lang.String)
meth public void setLayout(java.lang.String)
meth public void setName(java.lang.String)
meth public void setUrl(java.lang.String)
supr java.lang.Object
hfds id,layout,name,url

CLSS public org.apache.maven.profiles.RepositoryPolicy
cons public init()
intf java.io.Serializable
meth public boolean isEnabled()
meth public java.lang.String getChecksumPolicy()
meth public java.lang.String getUpdatePolicy()
meth public void setChecksumPolicy(java.lang.String)
meth public void setEnabled(boolean)
meth public void setUpdatePolicy(java.lang.String)
supr java.lang.Object
hfds checksumPolicy,enabled,updatePolicy

CLSS public org.apache.maven.profiles.io.xpp3.ProfilesXpp3Reader
cons public init()
cons public init(org.apache.maven.profiles.io.xpp3.ProfilesXpp3Reader$ContentTransformer)
fld public final org.apache.maven.profiles.io.xpp3.ProfilesXpp3Reader$ContentTransformer contentTransformer
innr public abstract interface static ContentTransformer
meth public boolean getAddDefaultEntities()
meth public org.apache.maven.profiles.ProfilesRoot read(java.io.InputStream) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.profiles.ProfilesRoot read(java.io.InputStream,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.profiles.ProfilesRoot read(java.io.Reader) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.profiles.ProfilesRoot read(java.io.Reader,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.profiles.ProfilesRoot read(org.codehaus.plexus.util.xml.pull.XmlPullParser,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public void setAddDefaultEntities(boolean)
supr java.lang.Object
hfds addDefaultEntities

CLSS public abstract interface static org.apache.maven.profiles.io.xpp3.ProfilesXpp3Reader$ContentTransformer
 outer org.apache.maven.profiles.io.xpp3.ProfilesXpp3Reader
meth public abstract java.lang.String transform(java.lang.String,java.lang.String)

CLSS public org.apache.maven.profiles.io.xpp3.ProfilesXpp3Writer
cons public init()
meth public void setFileComment(java.lang.String)
meth public void write(java.io.OutputStream,org.apache.maven.profiles.ProfilesRoot) throws java.io.IOException
meth public void write(java.io.Writer,org.apache.maven.profiles.ProfilesRoot) throws java.io.IOException
supr java.lang.Object
hfds NAMESPACE,fileComment

CLSS public org.apache.maven.project.DefaultDependencyResolutionRequest
cons public init()
cons public init(org.apache.maven.project.MavenProject,org.eclipse.aether.RepositorySystemSession)
intf org.apache.maven.project.DependencyResolutionRequest
meth public org.apache.maven.project.DependencyResolutionRequest setMavenProject(org.apache.maven.project.MavenProject)
meth public org.apache.maven.project.DependencyResolutionRequest setRepositorySession(org.eclipse.aether.RepositorySystemSession)
meth public org.apache.maven.project.DependencyResolutionRequest setResolutionFilter(org.eclipse.aether.graph.DependencyFilter)
meth public org.apache.maven.project.MavenProject getMavenProject()
meth public org.eclipse.aether.RepositorySystemSession getRepositorySession()
meth public org.eclipse.aether.graph.DependencyFilter getResolutionFilter()
supr java.lang.Object
hfds filter,project,session

CLSS public org.apache.maven.project.DefaultMavenProjectBuilder
 anno 0 java.lang.Deprecated()
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.project.MavenProjectBuilder, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.project.MavenProjectBuilder
meth public org.apache.maven.project.MavenProject build(java.io.File,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.profiles.ProfileManager) throws org.apache.maven.project.ProjectBuildingException
meth public org.apache.maven.project.MavenProject build(java.io.File,org.apache.maven.project.ProjectBuilderConfiguration) throws org.apache.maven.project.ProjectBuildingException
meth public org.apache.maven.project.MavenProject buildFromRepository(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.project.ProjectBuildingException
meth public org.apache.maven.project.MavenProject buildFromRepository(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository,boolean) throws org.apache.maven.project.ProjectBuildingException
meth public org.apache.maven.project.MavenProject buildFromRepository(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.project.ProjectBuilderConfiguration,boolean) throws org.apache.maven.project.ProjectBuildingException
meth public org.apache.maven.project.MavenProject buildStandaloneSuperProject(org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.project.ProjectBuildingException
meth public org.apache.maven.project.MavenProject buildStandaloneSuperProject(org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.profiles.ProfileManager) throws org.apache.maven.project.ProjectBuildingException
meth public org.apache.maven.project.MavenProject buildStandaloneSuperProject(org.apache.maven.project.ProjectBuilderConfiguration) throws org.apache.maven.project.ProjectBuildingException
meth public org.apache.maven.project.MavenProject buildWithDependencies(java.io.File,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.profiles.ProfileManager) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException,org.apache.maven.project.ProjectBuildingException
meth public org.apache.maven.project.MavenProject buildWithDependencies(java.io.File,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.profiles.ProfileManager,org.apache.maven.wagon.events.TransferListener) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException,org.apache.maven.project.ProjectBuildingException
supr java.lang.Object
hfds legacySupport,projectBuilder,repositorySystem

CLSS public org.apache.maven.project.DefaultMavenProjectHelper
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.project.MavenProjectHelper, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.project.MavenProjectHelper
meth public void addResource(org.apache.maven.project.MavenProject,java.lang.String,java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth public void addTestResource(org.apache.maven.project.MavenProject,java.lang.String,java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth public void attachArtifact(org.apache.maven.project.MavenProject,java.io.File,java.lang.String)
meth public void attachArtifact(org.apache.maven.project.MavenProject,java.lang.String,java.io.File)
meth public void attachArtifact(org.apache.maven.project.MavenProject,java.lang.String,java.lang.String,java.io.File)
meth public void attachArtifact(org.apache.maven.project.MavenProject,org.apache.maven.artifact.Artifact)
supr org.codehaus.plexus.logging.AbstractLogEnabled
hfds artifactHandlerManager

CLSS public org.apache.maven.project.DefaultModelBuildingListener
cons public init(org.apache.maven.project.MavenProject,org.apache.maven.project.ProjectBuildingHelper,org.apache.maven.project.ProjectBuildingRequest)
meth public org.apache.maven.project.MavenProject getProject()
meth public void buildExtensionsAssembled(org.apache.maven.model.building.ModelBuildingEvent)
supr org.apache.maven.model.building.AbstractModelBuildingListener
hfds pluginRepositories,project,projectBuildingHelper,projectBuildingRequest,remoteRepositories

CLSS public org.apache.maven.project.DefaultProjectBuilder
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.project.ProjectBuilder, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
fld public final static java.lang.String DISABLE_GLOBAL_MODEL_CACHE_SYSTEM_PROPERTY = "maven.defaultProjectBuilder.disableGlobalModelCache"
intf org.apache.maven.project.ProjectBuilder
meth public java.util.List<org.apache.maven.project.ProjectBuildingResult> build(java.util.List<java.io.File>,boolean,org.apache.maven.project.ProjectBuildingRequest) throws org.apache.maven.project.ProjectBuildingException
meth public org.apache.maven.project.ProjectBuildingResult build(java.io.File,org.apache.maven.project.ProjectBuildingRequest) throws org.apache.maven.project.ProjectBuildingException
meth public org.apache.maven.project.ProjectBuildingResult build(org.apache.maven.artifact.Artifact,boolean,org.apache.maven.project.ProjectBuildingRequest) throws org.apache.maven.project.ProjectBuildingException
meth public org.apache.maven.project.ProjectBuildingResult build(org.apache.maven.artifact.Artifact,org.apache.maven.project.ProjectBuildingRequest) throws org.apache.maven.project.ProjectBuildingException
meth public org.apache.maven.project.ProjectBuildingResult build(org.apache.maven.model.building.ModelSource,org.apache.maven.project.ProjectBuildingRequest) throws org.apache.maven.project.ProjectBuildingException
supr java.lang.Object
hfds dependencyResolver,logger,modelBuilder,modelCacheFactory,modelProcessor,projectBuildingHelper,repoSystem,repositoryManager,repositorySystem
hcls InterimResult,InternalConfig

CLSS public org.apache.maven.project.DefaultProjectBuilderConfiguration
 anno 0 java.lang.Deprecated()
cons public init()
intf org.apache.maven.project.ProjectBuilderConfiguration
meth public java.util.Date getBuildStartTime()
meth public java.util.Properties getExecutionProperties()
meth public java.util.Properties getUserProperties()
meth public org.apache.maven.artifact.repository.ArtifactRepository getLocalRepository()
meth public org.apache.maven.profiles.ProfileManager getGlobalProfileManager()
meth public org.apache.maven.project.ProjectBuilderConfiguration setBuildStartTime(java.util.Date)
meth public org.apache.maven.project.ProjectBuilderConfiguration setExecutionProperties(java.util.Properties)
meth public org.apache.maven.project.ProjectBuilderConfiguration setGlobalProfileManager(org.apache.maven.profiles.ProfileManager)
meth public org.apache.maven.project.ProjectBuilderConfiguration setLocalRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public org.apache.maven.project.ProjectBuilderConfiguration setUserProperties(java.util.Properties)
supr java.lang.Object
hfds buildStartTime,executionProperties,globalProfileManager,localRepository,userProperties

CLSS public org.apache.maven.project.DefaultProjectBuildingHelper
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.project.ProjectBuildingHelper, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.project.ProjectBuildingHelper
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> createArtifactRepositories(java.util.List<org.apache.maven.model.Repository>,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.project.ProjectBuildingRequest) throws org.apache.maven.artifact.InvalidRepositoryException
meth public org.apache.maven.project.ProjectRealmCache$CacheRecord createProjectRealm(org.apache.maven.project.MavenProject,org.apache.maven.model.Model,org.apache.maven.project.ProjectBuildingRequest) throws org.apache.maven.plugin.PluginManagerException,org.apache.maven.plugin.PluginResolutionException,org.apache.maven.plugin.version.PluginVersionResolutionException
meth public void selectProjectRealm(org.apache.maven.project.MavenProject)
supr java.lang.Object
hfds classRealmManager,container,logger,pluginManager,projectRealmCache,repositorySystem

CLSS public org.apache.maven.project.DefaultProjectBuildingRequest
cons public init()
cons public init(org.apache.maven.project.ProjectBuildingRequest)
intf org.apache.maven.project.ProjectBuildingRequest
meth public boolean isProcessPlugins()
meth public boolean isResolveDependencies()
meth public boolean isResolveVersionRanges()
 anno 0 java.lang.Deprecated()
meth public int getValidationLevel()
meth public java.util.Date getBuildStartTime()
meth public java.util.List<java.lang.String> getActiveProfileIds()
meth public java.util.List<java.lang.String> getInactiveProfileIds()
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getPluginArtifactRepositories()
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getRemoteRepositories()
meth public java.util.List<org.apache.maven.model.Profile> getProfiles()
meth public java.util.Properties getSystemProperties()
meth public java.util.Properties getUserProperties()
meth public org.apache.maven.artifact.repository.ArtifactRepository getLocalRepository()
meth public org.apache.maven.project.DefaultProjectBuildingRequest setRepositoryMerging(org.apache.maven.project.ProjectBuildingRequest$RepositoryMerging)
meth public org.apache.maven.project.DefaultProjectBuildingRequest setRepositorySession(org.eclipse.aether.RepositorySystemSession)
meth public org.apache.maven.project.MavenProject getProject()
meth public org.apache.maven.project.ProjectBuildingRequest setLocalRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public org.apache.maven.project.ProjectBuildingRequest setPluginArtifactRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public org.apache.maven.project.ProjectBuildingRequest setProcessPlugins(boolean)
meth public org.apache.maven.project.ProjectBuildingRequest setRemoteRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public org.apache.maven.project.ProjectBuildingRequest setResolveDependencies(boolean)
meth public org.apache.maven.project.ProjectBuildingRequest setResolveVersionRanges(boolean)
 anno 0 java.lang.Deprecated()
meth public org.apache.maven.project.ProjectBuildingRequest setSystemProperties(java.util.Properties)
meth public org.apache.maven.project.ProjectBuildingRequest setUserProperties(java.util.Properties)
meth public org.apache.maven.project.ProjectBuildingRequest setValidationLevel(int)
meth public org.apache.maven.project.ProjectBuildingRequest$RepositoryMerging getRepositoryMerging()
meth public org.eclipse.aether.RepositorySystemSession getRepositorySession()
meth public void addProfile(org.apache.maven.model.Profile)
meth public void setActiveProfileIds(java.util.List<java.lang.String>)
meth public void setBuildStartTime(java.util.Date)
meth public void setInactiveProfileIds(java.util.List<java.lang.String>)
meth public void setProfiles(java.util.List<org.apache.maven.model.Profile>)
meth public void setProject(org.apache.maven.project.MavenProject)
supr java.lang.Object
hfds activeProfileIds,buildStartTime,inactiveProfileIds,localRepository,pluginArtifactRepositories,processPlugins,profiles,project,remoteRepositories,repositoryMerging,repositorySession,resolveDependencies,resolveVersionRanges,systemProperties,userProperties,validationLevel

CLSS public org.apache.maven.project.DefaultProjectDependenciesResolver
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.project.ProjectDependenciesResolver, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.project.ProjectDependenciesResolver
meth public org.apache.maven.project.DependencyResolutionResult resolve(org.apache.maven.project.DependencyResolutionRequest) throws org.apache.maven.project.DependencyResolutionException
supr java.lang.Object
hfds decorators,logger,repoSystem
hcls GraphLogger

CLSS public org.apache.maven.project.DefaultProjectRealmCache
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.project.ProjectRealmCache, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
fld protected final java.util.Map<org.apache.maven.project.ProjectRealmCache$Key,org.apache.maven.project.ProjectRealmCache$CacheRecord> cache
innr protected static CacheKey
intf org.apache.maven.project.ProjectRealmCache
intf org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable
meth public org.apache.maven.project.ProjectRealmCache$CacheRecord get(org.apache.maven.project.ProjectRealmCache$Key)
meth public org.apache.maven.project.ProjectRealmCache$CacheRecord put(org.apache.maven.project.ProjectRealmCache$Key,org.codehaus.plexus.classworlds.realm.ClassRealm,org.eclipse.aether.graph.DependencyFilter)
meth public org.apache.maven.project.ProjectRealmCache$Key createKey(java.util.List<? extends org.codehaus.plexus.classworlds.realm.ClassRealm>)
meth public void dispose()
meth public void flush()
meth public void register(org.apache.maven.project.MavenProject,org.apache.maven.project.ProjectRealmCache$Key,org.apache.maven.project.ProjectRealmCache$CacheRecord)
supr java.lang.Object

CLSS protected static org.apache.maven.project.DefaultProjectRealmCache$CacheKey
 outer org.apache.maven.project.DefaultProjectRealmCache
cons public init(java.util.List<? extends org.codehaus.plexus.classworlds.realm.ClassRealm>)
intf org.apache.maven.project.ProjectRealmCache$Key
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds extensionRealms,hashCode

CLSS public org.apache.maven.project.DependencyResolutionException
cons public init(org.apache.maven.project.DependencyResolutionResult,java.lang.String,java.lang.Throwable)
meth public org.apache.maven.project.DependencyResolutionResult getResult()
supr java.lang.Exception
hfds result

CLSS public abstract interface org.apache.maven.project.DependencyResolutionRequest
meth public abstract org.apache.maven.project.DependencyResolutionRequest setMavenProject(org.apache.maven.project.MavenProject)
meth public abstract org.apache.maven.project.DependencyResolutionRequest setRepositorySession(org.eclipse.aether.RepositorySystemSession)
meth public abstract org.apache.maven.project.DependencyResolutionRequest setResolutionFilter(org.eclipse.aether.graph.DependencyFilter)
meth public abstract org.apache.maven.project.MavenProject getMavenProject()
meth public abstract org.eclipse.aether.RepositorySystemSession getRepositorySession()
meth public abstract org.eclipse.aether.graph.DependencyFilter getResolutionFilter()

CLSS public abstract interface org.apache.maven.project.DependencyResolutionResult
meth public abstract java.util.List<java.lang.Exception> getCollectionErrors()
meth public abstract java.util.List<java.lang.Exception> getResolutionErrors(org.eclipse.aether.graph.Dependency)
meth public abstract java.util.List<org.eclipse.aether.graph.Dependency> getDependencies()
meth public abstract java.util.List<org.eclipse.aether.graph.Dependency> getResolvedDependencies()
meth public abstract java.util.List<org.eclipse.aether.graph.Dependency> getUnresolvedDependencies()
meth public abstract org.eclipse.aether.graph.DependencyNode getDependencyGraph()

CLSS public org.apache.maven.project.DuplicateArtifactAttachmentException
cons public init(org.apache.maven.project.MavenProject,org.apache.maven.artifact.Artifact)
meth public org.apache.maven.artifact.Artifact getArtifact()
meth public org.apache.maven.project.MavenProject getProject()
supr java.lang.RuntimeException
hfds DEFAULT_MESSAGE,artifact,project

CLSS public org.apache.maven.project.DuplicateProjectException
cons public init(java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.io.File,java.io.File,java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
 anno 0 java.lang.Deprecated()
meth public java.io.File getConflictingProjectFile()
meth public java.io.File getExistingProjectFile()
meth public java.lang.String getProjectId()
supr java.lang.Exception
hfds conflictingProjectFile,existingProjectFile,projectId

CLSS public org.apache.maven.project.ExtensionDescriptor
meth public java.util.List<java.lang.String> getExportedArtifacts()
meth public java.util.List<java.lang.String> getExportedPackages()
meth public void setExportedArtifacts(java.util.List<java.lang.String>)
meth public void setExportedPackages(java.util.List<java.lang.String>)
supr java.lang.Object
hfds exportedArtifacts,exportedPackages

CLSS public org.apache.maven.project.ExtensionDescriptorBuilder
cons public init()
meth public java.lang.String getExtensionDescriptorLocation()
meth public org.apache.maven.project.ExtensionDescriptor build(java.io.File) throws java.io.IOException
meth public org.apache.maven.project.ExtensionDescriptor build(java.io.InputStream) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.maven.project.InvalidProjectModelException
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String,java.io.File)
cons public init(java.lang.String,java.lang.String,java.io.File,org.apache.maven.project.validation.ModelValidationResult)
cons public init(java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String,java.lang.String,org.apache.maven.project.validation.ModelValidationResult)
 anno 0 java.lang.Deprecated()
meth public final org.apache.maven.project.validation.ModelValidationResult getValidationResult()
supr org.apache.maven.project.ProjectBuildingException
hfds validationResult

CLSS public org.apache.maven.project.InvalidProjectVersionException
cons public init(java.lang.String,java.lang.String,java.lang.String,java.io.File,org.apache.maven.artifact.versioning.InvalidVersionSpecificationException)
meth public java.lang.String getLocationInPom()
meth public java.lang.String getOffendingVersion()
supr org.apache.maven.project.ProjectBuildingException
hfds locationInPom,offendingVersion

CLSS public org.apache.maven.project.MavenProject
cons public init()
cons public init(org.apache.maven.model.Model)
cons public init(org.apache.maven.project.MavenProject)
fld public final static java.lang.String EMPTY_PROJECT_ARTIFACT_ID = "empty-project"
fld public final static java.lang.String EMPTY_PROJECT_GROUP_ID = "unknown"
fld public final static java.lang.String EMPTY_PROJECT_VERSION = "0"
intf java.lang.Cloneable
meth protected org.apache.maven.artifact.repository.ArtifactRepository getReleaseArtifactRepository()
meth protected org.apache.maven.artifact.repository.ArtifactRepository getSnapshotArtifactRepository()
meth protected void setAttachedArtifacts(java.util.List<org.apache.maven.artifact.Artifact>)
meth protected void setCompileSourceRoots(java.util.List<java.lang.String>)
meth protected void setScriptSourceRoots(java.util.List<java.lang.String>)
 anno 0 java.lang.Deprecated()
meth protected void setTestCompileSourceRoots(java.util.List<java.lang.String>)
meth public boolean equals(java.lang.Object)
meth public boolean hasLifecyclePhase(java.lang.String)
meth public boolean hasParent()
meth public boolean isExecutionRoot()
meth public int hashCode()
meth public java.io.File getBasedir()
meth public java.io.File getFile()
meth public java.io.File getParentFile()
meth public java.lang.Object getContextValue(java.lang.String)
meth public java.lang.String getArtifactId()
meth public java.lang.String getDefaultGoal()
meth public java.lang.String getDescription()
meth public java.lang.String getGroupId()
meth public java.lang.String getId()
meth public java.lang.String getInceptionYear()
meth public java.lang.String getModelVersion()
meth public java.lang.String getModulePathAdjustment(org.apache.maven.project.MavenProject) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public java.lang.String getName()
meth public java.lang.String getPackaging()
meth public java.lang.String getUrl()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getCompileClasspathElements() throws org.apache.maven.artifact.DependencyResolutionRequiredException
meth public java.util.List<java.lang.String> getCompileSourceRoots()
meth public java.util.List<java.lang.String> getFilters()
meth public java.util.List<java.lang.String> getModules()
meth public java.util.List<java.lang.String> getRuntimeClasspathElements() throws org.apache.maven.artifact.DependencyResolutionRequiredException
meth public java.util.List<java.lang.String> getScriptSourceRoots()
 anno 0 java.lang.Deprecated()
meth public java.util.List<java.lang.String> getSystemClasspathElements() throws org.apache.maven.artifact.DependencyResolutionRequiredException
 anno 0 java.lang.Deprecated()
meth public java.util.List<java.lang.String> getTestClasspathElements() throws org.apache.maven.artifact.DependencyResolutionRequiredException
meth public java.util.List<java.lang.String> getTestCompileSourceRoots()
meth public java.util.List<org.apache.maven.artifact.Artifact> getAttachedArtifacts()
meth public java.util.List<org.apache.maven.artifact.Artifact> getCompileArtifacts()
 anno 0 java.lang.Deprecated()
meth public java.util.List<org.apache.maven.artifact.Artifact> getRuntimeArtifacts()
 anno 0 java.lang.Deprecated()
meth public java.util.List<org.apache.maven.artifact.Artifact> getSystemArtifacts()
 anno 0 java.lang.Deprecated()
meth public java.util.List<org.apache.maven.artifact.Artifact> getTestArtifacts()
 anno 0 java.lang.Deprecated()
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getPluginArtifactRepositories()
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getRemoteArtifactRepositories()
meth public java.util.List<org.apache.maven.model.Contributor> getContributors()
meth public java.util.List<org.apache.maven.model.Dependency> getCompileDependencies()
 anno 0 java.lang.Deprecated()
meth public java.util.List<org.apache.maven.model.Dependency> getDependencies()
meth public java.util.List<org.apache.maven.model.Dependency> getRuntimeDependencies()
 anno 0 java.lang.Deprecated()
meth public java.util.List<org.apache.maven.model.Dependency> getSystemDependencies()
 anno 0 java.lang.Deprecated()
meth public java.util.List<org.apache.maven.model.Dependency> getTestDependencies()
 anno 0 java.lang.Deprecated()
meth public java.util.List<org.apache.maven.model.Developer> getDevelopers()
meth public java.util.List<org.apache.maven.model.Extension> getBuildExtensions()
meth public java.util.List<org.apache.maven.model.License> getLicenses()
meth public java.util.List<org.apache.maven.model.MailingList> getMailingLists()
meth public java.util.List<org.apache.maven.model.Plugin> getBuildPlugins()
meth public java.util.List<org.apache.maven.model.Profile> getActiveProfiles()
meth public java.util.List<org.apache.maven.model.ReportPlugin> getReportPlugins()
 anno 0 java.lang.Deprecated()
meth public java.util.List<org.apache.maven.model.Repository> getPluginRepositories()
meth public java.util.List<org.apache.maven.model.Repository> getRepositories()
meth public java.util.List<org.apache.maven.model.Resource> getResources()
meth public java.util.List<org.apache.maven.model.Resource> getTestResources()
meth public java.util.List<org.apache.maven.project.MavenProject> getCollectedProjects()
meth public java.util.List<org.eclipse.aether.repository.RemoteRepository> getRemotePluginRepositories()
meth public java.util.List<org.eclipse.aether.repository.RemoteRepository> getRemoteProjectRepositories()
meth public java.util.Map<java.lang.String,java.util.List<java.lang.String>> getInjectedProfileIds()
meth public java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact> getArtifactMap()
meth public java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact> getExtensionArtifactMap()
 anno 0 java.lang.Deprecated()
meth public java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact> getManagedVersionMap()
meth public java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact> getPluginArtifactMap()
meth public java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact> getReportArtifactMap()
 anno 0 java.lang.Deprecated()
meth public java.util.Map<java.lang.String,org.apache.maven.project.MavenProject> getProjectReferences()
meth public java.util.Properties getProperties()
meth public java.util.Set<org.apache.maven.artifact.Artifact> createArtifacts(org.apache.maven.artifact.factory.ArtifactFactory,java.lang.String,org.apache.maven.artifact.resolver.filter.ArtifactFilter) throws org.apache.maven.project.artifact.InvalidDependencyVersionException
 anno 0 java.lang.Deprecated()
meth public java.util.Set<org.apache.maven.artifact.Artifact> getArtifacts()
meth public java.util.Set<org.apache.maven.artifact.Artifact> getDependencyArtifacts()
 anno 0 java.lang.Deprecated()
meth public java.util.Set<org.apache.maven.artifact.Artifact> getExtensionArtifacts()
 anno 0 java.lang.Deprecated()
meth public java.util.Set<org.apache.maven.artifact.Artifact> getPluginArtifacts()
meth public java.util.Set<org.apache.maven.artifact.Artifact> getReportArtifacts()
 anno 0 java.lang.Deprecated()
meth public org.apache.maven.artifact.Artifact getArtifact()
meth public org.apache.maven.artifact.Artifact getParentArtifact()
meth public org.apache.maven.artifact.Artifact replaceWithActiveArtifact(org.apache.maven.artifact.Artifact)
 anno 0 java.lang.Deprecated()
meth public org.apache.maven.artifact.repository.ArtifactRepository getDistributionManagementArtifactRepository()
meth public org.apache.maven.model.Build getBuild()
meth public org.apache.maven.model.CiManagement getCiManagement()
meth public org.apache.maven.model.DependencyManagement getDependencyManagement()
meth public org.apache.maven.model.DistributionManagement getDistributionManagement()
meth public org.apache.maven.model.IssueManagement getIssueManagement()
meth public org.apache.maven.model.Model getModel()
meth public org.apache.maven.model.Model getOriginalModel()
meth public org.apache.maven.model.Organization getOrganization()
meth public org.apache.maven.model.Plugin getPlugin(java.lang.String)
meth public org.apache.maven.model.PluginManagement getPluginManagement()
meth public org.apache.maven.model.Prerequisites getPrerequisites()
meth public org.apache.maven.model.Reporting getReporting()
 anno 0 java.lang.Deprecated()
meth public org.apache.maven.model.Scm getScm()
meth public org.apache.maven.project.MavenProject clone()
meth public org.apache.maven.project.MavenProject getExecutionProject()
meth public org.apache.maven.project.MavenProject getParent()
meth public org.apache.maven.project.ProjectBuildingRequest getProjectBuildingRequest()
 anno 0 java.lang.Deprecated()
meth public org.codehaus.plexus.classworlds.realm.ClassRealm getClassRealm()
meth public org.codehaus.plexus.util.xml.Xpp3Dom getGoalConfiguration(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public org.codehaus.plexus.util.xml.Xpp3Dom getReportConfiguration(java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public org.eclipse.aether.graph.DependencyFilter getExtensionDependencyFilter()
meth public void addAttachedArtifact(org.apache.maven.artifact.Artifact)
meth public void addCompileSourceRoot(java.lang.String)
meth public void addContributor(org.apache.maven.model.Contributor)
meth public void addDeveloper(org.apache.maven.model.Developer)
meth public void addLicense(org.apache.maven.model.License)
meth public void addLifecyclePhase(java.lang.String)
meth public void addMailingList(org.apache.maven.model.MailingList)
meth public void addProjectReference(org.apache.maven.project.MavenProject)
meth public void addResource(org.apache.maven.model.Resource)
meth public void addScriptSourceRoot(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void addTestCompileSourceRoot(java.lang.String)
meth public void addTestResource(org.apache.maven.model.Resource)
meth public void attachArtifact(java.lang.String,java.lang.String,java.io.File)
 anno 0 java.lang.Deprecated()
meth public void setActiveProfiles(java.util.List<org.apache.maven.model.Profile>)
meth public void setArtifact(org.apache.maven.artifact.Artifact)
meth public void setArtifactFilter(org.apache.maven.artifact.resolver.filter.ArtifactFilter)
meth public void setArtifactId(java.lang.String)
meth public void setArtifacts(java.util.Set<org.apache.maven.artifact.Artifact>)
meth public void setBuild(org.apache.maven.model.Build)
meth public void setCiManagement(org.apache.maven.model.CiManagement)
meth public void setClassRealm(org.codehaus.plexus.classworlds.realm.ClassRealm)
meth public void setCollectedProjects(java.util.List<org.apache.maven.project.MavenProject>)
meth public void setContextValue(java.lang.String,java.lang.Object)
meth public void setContributors(java.util.List<org.apache.maven.model.Contributor>)
meth public void setDependencies(java.util.List<org.apache.maven.model.Dependency>)
meth public void setDependencyArtifacts(java.util.Set<org.apache.maven.artifact.Artifact>)
 anno 0 java.lang.Deprecated()
meth public void setDescription(java.lang.String)
meth public void setDevelopers(java.util.List<org.apache.maven.model.Developer>)
meth public void setDistributionManagement(org.apache.maven.model.DistributionManagement)
meth public void setExecutionProject(org.apache.maven.project.MavenProject)
meth public void setExecutionRoot(boolean)
meth public void setExtensionArtifacts(java.util.Set<org.apache.maven.artifact.Artifact>)
 anno 0 java.lang.Deprecated()
meth public void setExtensionDependencyFilter(org.eclipse.aether.graph.DependencyFilter)
meth public void setFile(java.io.File)
meth public void setGroupId(java.lang.String)
meth public void setInceptionYear(java.lang.String)
meth public void setInjectedProfileIds(java.lang.String,java.util.List<java.lang.String>)
meth public void setIssueManagement(org.apache.maven.model.IssueManagement)
meth public void setLicenses(java.util.List<org.apache.maven.model.License>)
meth public void setMailingLists(java.util.List<org.apache.maven.model.MailingList>)
meth public void setManagedVersionMap(java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>)
meth public void setModel(org.apache.maven.model.Model)
meth public void setModelVersion(java.lang.String)
meth public void setName(java.lang.String)
meth public void setOrganization(org.apache.maven.model.Organization)
meth public void setOriginalModel(org.apache.maven.model.Model)
meth public void setPackaging(java.lang.String)
meth public void setParent(org.apache.maven.project.MavenProject)
meth public void setParentArtifact(org.apache.maven.artifact.Artifact)
meth public void setParentFile(java.io.File)
meth public void setPluginArtifactRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public void setPluginArtifacts(java.util.Set<org.apache.maven.artifact.Artifact>)
meth public void setPomFile(java.io.File)
meth public void setProjectBuildingRequest(org.apache.maven.project.ProjectBuildingRequest)
 anno 0 java.lang.Deprecated()
meth public void setReleaseArtifactRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public void setRemoteArtifactRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public void setReportArtifacts(java.util.Set<org.apache.maven.artifact.Artifact>)
 anno 0 java.lang.Deprecated()
meth public void setReporting(org.apache.maven.model.Reporting)
 anno 0 java.lang.Deprecated()
meth public void setResolvedArtifacts(java.util.Set<org.apache.maven.artifact.Artifact>)
meth public void setScm(org.apache.maven.model.Scm)
meth public void setSnapshotArtifactRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public void setUrl(java.lang.String)
meth public void setVersion(java.lang.String)
meth public void writeModel(java.io.Writer) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public void writeOriginalModel(java.io.Writer) throws java.io.IOException
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds LOGGER,activeProfiles,artifact,artifactFilter,artifactMap,artifacts,attachedArtifacts,basedir,classRealm,collectedProjects,compileSourceRoots,context,dependencyArtifacts,executionProject,executionRoot,extensionArtifactMap,extensionArtifacts,extensionDependencyFilter,file,injectedProfileIds,lifecyclePhases,managedVersionMap,model,moduleAdjustments,originalModel,parent,parentArtifact,parentFile,pluginArtifactMap,pluginArtifactRepositories,pluginArtifacts,projectBuilderConfiguration,projectReferences,releaseArtifactRepository,remoteArtifactRepositories,remotePluginRepositories,remoteProjectRepositories,reportArtifactMap,reportArtifacts,resolvedArtifacts,scriptSourceRoots,snapshotArtifactRepository,testCompileSourceRoots

CLSS public abstract interface org.apache.maven.project.MavenProjectBuilder
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.project.MavenProject build(java.io.File,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.profiles.ProfileManager) throws org.apache.maven.project.ProjectBuildingException
meth public abstract org.apache.maven.project.MavenProject build(java.io.File,org.apache.maven.project.ProjectBuilderConfiguration) throws org.apache.maven.project.ProjectBuildingException
meth public abstract org.apache.maven.project.MavenProject buildFromRepository(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.project.ProjectBuildingException
meth public abstract org.apache.maven.project.MavenProject buildFromRepository(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository,boolean) throws org.apache.maven.project.ProjectBuildingException
meth public abstract org.apache.maven.project.MavenProject buildStandaloneSuperProject(org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.project.ProjectBuildingException
meth public abstract org.apache.maven.project.MavenProject buildStandaloneSuperProject(org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.profiles.ProfileManager) throws org.apache.maven.project.ProjectBuildingException
meth public abstract org.apache.maven.project.MavenProject buildStandaloneSuperProject(org.apache.maven.project.ProjectBuilderConfiguration) throws org.apache.maven.project.ProjectBuildingException
meth public abstract org.apache.maven.project.MavenProject buildWithDependencies(java.io.File,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.profiles.ProfileManager) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException,org.apache.maven.project.ProjectBuildingException
meth public abstract org.apache.maven.project.MavenProject buildWithDependencies(java.io.File,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.profiles.ProfileManager,org.apache.maven.wagon.events.TransferListener) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException,org.apache.maven.project.ProjectBuildingException

CLSS public abstract interface org.apache.maven.project.MavenProjectHelper
fld public final static java.lang.String ROLE
meth public abstract void addResource(org.apache.maven.project.MavenProject,java.lang.String,java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth public abstract void addTestResource(org.apache.maven.project.MavenProject,java.lang.String,java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth public abstract void attachArtifact(org.apache.maven.project.MavenProject,java.io.File,java.lang.String)
meth public abstract void attachArtifact(org.apache.maven.project.MavenProject,java.lang.String,java.io.File)
meth public abstract void attachArtifact(org.apache.maven.project.MavenProject,java.lang.String,java.lang.String,java.io.File)

CLSS public org.apache.maven.project.MissingRepositoryElementException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
supr org.apache.maven.artifact.InvalidRepositoryException

CLSS public final org.apache.maven.project.ModelUtils
 anno 0 java.lang.Deprecated()
cons public init()
meth public static java.util.List<org.apache.maven.model.Plugin> orderAfterMerge(java.util.List<org.apache.maven.model.Plugin>,java.util.List<org.apache.maven.model.Plugin>,java.util.List<org.apache.maven.model.Plugin>)
meth public static java.util.List<org.apache.maven.model.Repository> mergeRepositoryLists(java.util.List<org.apache.maven.model.Repository>,java.util.List<org.apache.maven.model.Repository>)
meth public static void mergeFilterLists(java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth public static void mergePluginDefinitions(org.apache.maven.model.Plugin,org.apache.maven.model.Plugin,boolean)
meth public static void mergePluginLists(org.apache.maven.model.PluginContainer,org.apache.maven.model.PluginContainer,boolean)
supr java.lang.Object

CLSS public abstract interface org.apache.maven.project.ProjectBuilder
meth public abstract java.util.List<org.apache.maven.project.ProjectBuildingResult> build(java.util.List<java.io.File>,boolean,org.apache.maven.project.ProjectBuildingRequest) throws org.apache.maven.project.ProjectBuildingException
meth public abstract org.apache.maven.project.ProjectBuildingResult build(java.io.File,org.apache.maven.project.ProjectBuildingRequest) throws org.apache.maven.project.ProjectBuildingException
meth public abstract org.apache.maven.project.ProjectBuildingResult build(org.apache.maven.artifact.Artifact,boolean,org.apache.maven.project.ProjectBuildingRequest) throws org.apache.maven.project.ProjectBuildingException
meth public abstract org.apache.maven.project.ProjectBuildingResult build(org.apache.maven.artifact.Artifact,org.apache.maven.project.ProjectBuildingRequest) throws org.apache.maven.project.ProjectBuildingException
meth public abstract org.apache.maven.project.ProjectBuildingResult build(org.apache.maven.model.building.ModelSource,org.apache.maven.project.ProjectBuildingRequest) throws org.apache.maven.project.ProjectBuildingException

CLSS public abstract interface org.apache.maven.project.ProjectBuilderConfiguration
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Date getBuildStartTime()
meth public abstract java.util.Properties getExecutionProperties()
meth public abstract java.util.Properties getUserProperties()
meth public abstract org.apache.maven.artifact.repository.ArtifactRepository getLocalRepository()
meth public abstract org.apache.maven.profiles.ProfileManager getGlobalProfileManager()
meth public abstract org.apache.maven.project.ProjectBuilderConfiguration setBuildStartTime(java.util.Date)
meth public abstract org.apache.maven.project.ProjectBuilderConfiguration setExecutionProperties(java.util.Properties)
meth public abstract org.apache.maven.project.ProjectBuilderConfiguration setGlobalProfileManager(org.apache.maven.profiles.ProfileManager)
meth public abstract org.apache.maven.project.ProjectBuilderConfiguration setLocalRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public abstract org.apache.maven.project.ProjectBuilderConfiguration setUserProperties(java.util.Properties)

CLSS public org.apache.maven.project.ProjectBuildingException
cons protected init(java.lang.String,java.lang.String,java.io.File,java.lang.Throwable)
cons public init(java.lang.String,java.lang.String,java.io.File)
cons public init(java.lang.String,java.lang.String,java.lang.Throwable)
cons public init(java.util.List<org.apache.maven.project.ProjectBuildingResult>)
meth public java.io.File getPomFile()
meth public java.lang.String getPomLocation()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getProjectId()
meth public java.util.List<org.apache.maven.project.ProjectBuildingResult> getResults()
supr java.lang.Exception
hfds pomFile,projectId,results

CLSS public abstract interface org.apache.maven.project.ProjectBuildingHelper
meth public abstract java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> createArtifactRepositories(java.util.List<org.apache.maven.model.Repository>,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.project.ProjectBuildingRequest) throws org.apache.maven.artifact.InvalidRepositoryException
meth public abstract org.apache.maven.project.ProjectRealmCache$CacheRecord createProjectRealm(org.apache.maven.project.MavenProject,org.apache.maven.model.Model,org.apache.maven.project.ProjectBuildingRequest) throws org.apache.maven.plugin.PluginManagerException,org.apache.maven.plugin.PluginResolutionException,org.apache.maven.plugin.version.PluginVersionResolutionException
meth public abstract void selectProjectRealm(org.apache.maven.project.MavenProject)

CLSS public abstract interface org.apache.maven.project.ProjectBuildingRequest
innr public final static !enum RepositoryMerging
meth public abstract boolean isProcessPlugins()
meth public abstract boolean isResolveDependencies()
meth public abstract boolean isResolveVersionRanges()
 anno 0 java.lang.Deprecated()
meth public abstract int getValidationLevel()
meth public abstract java.util.Date getBuildStartTime()
meth public abstract java.util.List<java.lang.String> getActiveProfileIds()
meth public abstract java.util.List<java.lang.String> getInactiveProfileIds()
meth public abstract java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getPluginArtifactRepositories()
meth public abstract java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getRemoteRepositories()
meth public abstract java.util.List<org.apache.maven.model.Profile> getProfiles()
meth public abstract java.util.Properties getSystemProperties()
meth public abstract java.util.Properties getUserProperties()
meth public abstract org.apache.maven.artifact.repository.ArtifactRepository getLocalRepository()
meth public abstract org.apache.maven.project.MavenProject getProject()
meth public abstract org.apache.maven.project.ProjectBuildingRequest setLocalRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public abstract org.apache.maven.project.ProjectBuildingRequest setPluginArtifactRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public abstract org.apache.maven.project.ProjectBuildingRequest setProcessPlugins(boolean)
meth public abstract org.apache.maven.project.ProjectBuildingRequest setRemoteRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public abstract org.apache.maven.project.ProjectBuildingRequest setRepositoryMerging(org.apache.maven.project.ProjectBuildingRequest$RepositoryMerging)
meth public abstract org.apache.maven.project.ProjectBuildingRequest setRepositorySession(org.eclipse.aether.RepositorySystemSession)
meth public abstract org.apache.maven.project.ProjectBuildingRequest setResolveDependencies(boolean)
meth public abstract org.apache.maven.project.ProjectBuildingRequest setResolveVersionRanges(boolean)
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.project.ProjectBuildingRequest setSystemProperties(java.util.Properties)
meth public abstract org.apache.maven.project.ProjectBuildingRequest setUserProperties(java.util.Properties)
meth public abstract org.apache.maven.project.ProjectBuildingRequest setValidationLevel(int)
meth public abstract org.apache.maven.project.ProjectBuildingRequest$RepositoryMerging getRepositoryMerging()
meth public abstract org.eclipse.aether.RepositorySystemSession getRepositorySession()
meth public abstract void addProfile(org.apache.maven.model.Profile)
meth public abstract void setActiveProfileIds(java.util.List<java.lang.String>)
meth public abstract void setBuildStartTime(java.util.Date)
meth public abstract void setInactiveProfileIds(java.util.List<java.lang.String>)
meth public abstract void setProfiles(java.util.List<org.apache.maven.model.Profile>)
meth public abstract void setProject(org.apache.maven.project.MavenProject)

CLSS public final static !enum org.apache.maven.project.ProjectBuildingRequest$RepositoryMerging
 outer org.apache.maven.project.ProjectBuildingRequest
fld public final static org.apache.maven.project.ProjectBuildingRequest$RepositoryMerging POM_DOMINANT
fld public final static org.apache.maven.project.ProjectBuildingRequest$RepositoryMerging REQUEST_DOMINANT
meth public static org.apache.maven.project.ProjectBuildingRequest$RepositoryMerging valueOf(java.lang.String)
meth public static org.apache.maven.project.ProjectBuildingRequest$RepositoryMerging[] values()
supr java.lang.Enum<org.apache.maven.project.ProjectBuildingRequest$RepositoryMerging>

CLSS public abstract interface org.apache.maven.project.ProjectBuildingResult
meth public abstract java.io.File getPomFile()
meth public abstract java.lang.String getProjectId()
meth public abstract java.util.List<org.apache.maven.model.building.ModelProblem> getProblems()
meth public abstract org.apache.maven.project.DependencyResolutionResult getDependencyResolutionResult()
meth public abstract org.apache.maven.project.MavenProject getProject()

CLSS public abstract interface org.apache.maven.project.ProjectDependenciesResolver
meth public abstract org.apache.maven.project.DependencyResolutionResult resolve(org.apache.maven.project.DependencyResolutionRequest) throws org.apache.maven.project.DependencyResolutionException

CLSS public org.apache.maven.project.ProjectModelResolver
cons public init(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.RequestTrace,org.eclipse.aether.RepositorySystem,org.eclipse.aether.impl.RemoteRepositoryManager,java.util.List<org.eclipse.aether.repository.RemoteRepository>,org.apache.maven.project.ProjectBuildingRequest$RepositoryMerging,org.apache.maven.project.ReactorModelPool)
intf org.apache.maven.model.resolution.ModelResolver
meth public org.apache.maven.model.building.ModelSource resolveModel(java.lang.String,java.lang.String,java.lang.String) throws org.apache.maven.model.resolution.UnresolvableModelException
meth public org.apache.maven.model.building.ModelSource resolveModel(org.apache.maven.model.Dependency) throws org.apache.maven.model.resolution.UnresolvableModelException
meth public org.apache.maven.model.building.ModelSource resolveModel(org.apache.maven.model.Parent) throws org.apache.maven.model.resolution.UnresolvableModelException
meth public org.apache.maven.model.resolution.ModelResolver newCopy()
meth public void addRepository(org.apache.maven.model.Repository) throws org.apache.maven.model.resolution.InvalidRepositoryException
meth public void addRepository(org.apache.maven.model.Repository,boolean) throws org.apache.maven.model.resolution.InvalidRepositoryException
supr java.lang.Object
hfds context,externalRepositories,modelPool,pomRepositories,remoteRepositoryManager,repositories,repositoryIds,repositoryMerging,resolver,session,trace

CLSS public abstract interface org.apache.maven.project.ProjectRealmCache
innr public abstract interface static Key
innr public static CacheRecord
meth public abstract org.apache.maven.project.ProjectRealmCache$CacheRecord get(org.apache.maven.project.ProjectRealmCache$Key)
meth public abstract org.apache.maven.project.ProjectRealmCache$CacheRecord put(org.apache.maven.project.ProjectRealmCache$Key,org.codehaus.plexus.classworlds.realm.ClassRealm,org.eclipse.aether.graph.DependencyFilter)
meth public abstract org.apache.maven.project.ProjectRealmCache$Key createKey(java.util.List<? extends org.codehaus.plexus.classworlds.realm.ClassRealm>)
meth public abstract void flush()
meth public abstract void register(org.apache.maven.project.MavenProject,org.apache.maven.project.ProjectRealmCache$Key,org.apache.maven.project.ProjectRealmCache$CacheRecord)

CLSS public static org.apache.maven.project.ProjectRealmCache$CacheRecord
 outer org.apache.maven.project.ProjectRealmCache
meth public org.codehaus.plexus.classworlds.realm.ClassRealm getRealm()
meth public org.eclipse.aether.graph.DependencyFilter getExtensionArtifactFilter()
supr java.lang.Object
hfds extensionArtifactFilter,realm

CLSS public abstract interface static org.apache.maven.project.ProjectRealmCache$Key
 outer org.apache.maven.project.ProjectRealmCache

CLSS public org.apache.maven.project.ProjectSorter
cons public init(java.util.Collection<org.apache.maven.project.MavenProject>) throws org.apache.maven.project.DuplicateProjectException,org.codehaus.plexus.util.dag.CycleDetectedException
meth public boolean hasMultipleProjects()
meth public java.util.List<java.lang.String> getDependencies(java.lang.String)
meth public java.util.List<java.lang.String> getDependents(java.lang.String)
meth public java.util.List<org.apache.maven.project.MavenProject> getSortedProjects()
meth public java.util.Map<java.lang.String,org.apache.maven.project.MavenProject> getProjectMap()
meth public org.apache.maven.project.MavenProject getTopLevelProject()
meth public org.codehaus.plexus.util.dag.DAG getDAG()
meth public static java.lang.String getId(org.apache.maven.project.MavenProject)
supr java.lang.Object
hfds dag,projectMap,sortedProjects,topLevelProject

CLSS public final org.apache.maven.project.ProjectUtils
 anno 0 java.lang.Deprecated()
meth public static java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> buildArtifactRepositories(java.util.List<org.apache.maven.model.Repository>,org.apache.maven.artifact.repository.ArtifactRepositoryFactory,org.codehaus.plexus.PlexusContainer) throws org.apache.maven.artifact.InvalidRepositoryException
meth public static org.apache.maven.artifact.repository.ArtifactRepository buildArtifactRepository(org.apache.maven.model.Repository,org.apache.maven.artifact.repository.ArtifactRepositoryFactory,org.codehaus.plexus.PlexusContainer) throws org.apache.maven.artifact.InvalidRepositoryException
meth public static org.apache.maven.artifact.repository.ArtifactRepository buildDeploymentArtifactRepository(org.apache.maven.model.DeploymentRepository,org.apache.maven.artifact.repository.ArtifactRepositoryFactory,org.codehaus.plexus.PlexusContainer) throws org.apache.maven.artifact.InvalidRepositoryException
supr java.lang.Object

CLSS public abstract interface org.apache.maven.project.RepositorySessionDecorator
meth public abstract org.eclipse.aether.RepositorySystemSession decorate(org.apache.maven.project.MavenProject,org.eclipse.aether.RepositorySystemSession)

CLSS public org.apache.maven.project.path.DefaultPathTranslator
 anno 0 java.lang.Deprecated()
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.project.path.PathTranslator, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.project.path.PathTranslator
meth public java.lang.String alignToBaseDirectory(java.lang.String,java.io.File)
meth public java.lang.String unalignFromBaseDirectory(java.lang.String,java.io.File)
meth public void alignToBaseDirectory(org.apache.maven.model.Model,java.io.File)
meth public void unalignFromBaseDirectory(org.apache.maven.model.Model,java.io.File)
supr java.lang.Object
hfds BASEDIR_EXPRESSIONS

CLSS public abstract interface org.apache.maven.project.path.PathTranslator
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String ROLE
meth public abstract java.lang.String alignToBaseDirectory(java.lang.String,java.io.File)
meth public abstract java.lang.String unalignFromBaseDirectory(java.lang.String,java.io.File)
meth public abstract void alignToBaseDirectory(org.apache.maven.model.Model,java.io.File)
meth public abstract void unalignFromBaseDirectory(org.apache.maven.model.Model,java.io.File)

CLSS public org.apache.maven.repository.ArtifactDoesNotExistException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception

CLSS public org.apache.maven.repository.ArtifactTransferEvent
cons public init(java.lang.String,int,int,org.apache.maven.repository.ArtifactTransferResource)
cons public init(java.lang.String,java.lang.Exception,int,org.apache.maven.repository.ArtifactTransferResource)
fld public final static int REQUEST_GET = 5
fld public final static int REQUEST_PUT = 6
fld public final static int TRANSFER_COMPLETED = 2
fld public final static int TRANSFER_ERROR = 4
fld public final static int TRANSFER_INITIATED = 0
fld public final static int TRANSFER_PROGRESS = 3
fld public final static int TRANSFER_STARTED = 1
meth public boolean equals(java.lang.Object)
meth public byte[] getDataBuffer()
meth public int getDataLength()
meth public int getDataOffset()
meth public int getEventType()
meth public int getRequestType()
meth public int hashCode()
meth public java.io.File getLocalFile()
meth public java.lang.Exception getException()
meth public java.lang.String toString()
meth public long getTransferredBytes()
meth public org.apache.maven.repository.ArtifactTransferResource getResource()
meth public void setDataBuffer(byte[])
meth public void setDataLength(int)
meth public void setDataOffset(int)
meth public void setEventType(int)
meth public void setLocalFile(java.io.File)
meth public void setRequestType(int)
meth public void setTransferredBytes(long)
supr java.util.EventObject
hfds artifact,dataBuffer,dataLength,dataOffset,eventType,exception,localFile,requestType,transferredBytes

CLSS public org.apache.maven.repository.ArtifactTransferFailedException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface org.apache.maven.repository.ArtifactTransferListener
meth public abstract boolean isShowChecksumEvents()
meth public abstract void setShowChecksumEvents(boolean)
meth public abstract void transferCompleted(org.apache.maven.repository.ArtifactTransferEvent)
meth public abstract void transferInitiated(org.apache.maven.repository.ArtifactTransferEvent)
meth public abstract void transferProgress(org.apache.maven.repository.ArtifactTransferEvent)
meth public abstract void transferStarted(org.apache.maven.repository.ArtifactTransferEvent)

CLSS public abstract interface org.apache.maven.repository.ArtifactTransferResource
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getRepositoryUrl()
meth public abstract java.lang.String getUrl()
meth public abstract long getContentLength()
meth public abstract long getTransferStartTime()

CLSS public org.apache.maven.repository.DefaultMirrorSelector
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.repository.MirrorSelector, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.repository.MirrorSelector
meth public org.apache.maven.settings.Mirror getMirror(org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.settings.Mirror>)
supr java.lang.Object
hfds EXTERNAL_HTTP_WILDCARD,EXTERNAL_WILDCARD,WILDCARD

CLSS public org.apache.maven.repository.DelegatingLocalArtifactRepository
 anno 0 java.lang.Deprecated()
cons public init(org.apache.maven.artifact.repository.ArtifactRepository)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getBasedir()
meth public java.lang.String getId()
meth public java.lang.String getKey()
meth public java.lang.String getUrl()
meth public java.lang.String pathOf(org.apache.maven.artifact.Artifact)
meth public java.lang.String pathOfLocalRepositoryMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository)
meth public java.util.List<java.lang.String> findVersions(org.apache.maven.artifact.Artifact)
meth public org.apache.maven.artifact.Artifact find(org.apache.maven.artifact.Artifact)
meth public org.apache.maven.artifact.repository.ArtifactRepositoryPolicy getReleases()
meth public org.apache.maven.artifact.repository.ArtifactRepositoryPolicy getSnapshots()
meth public org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout getLayout()
meth public org.apache.maven.repository.LocalArtifactRepository getIdeWorkspace()
meth public org.apache.maven.repository.LocalArtifactRepository getIdeWorspace()
 anno 0 java.lang.Deprecated()
meth public void setBuildReactor(org.apache.maven.repository.LocalArtifactRepository)
meth public void setIdeWorkspace(org.apache.maven.repository.LocalArtifactRepository)
supr org.apache.maven.artifact.repository.MavenArtifactRepository
hfds buildReactor,ideWorkspace,userLocalArtifactRepository

CLSS public abstract org.apache.maven.repository.LocalArtifactRepository
cons public init()
fld public final static java.lang.String IDE_WORKSPACE = "ide-workspace"
meth public abstract boolean hasLocalMetadata()
meth public abstract org.apache.maven.artifact.Artifact find(org.apache.maven.artifact.Artifact)
supr org.apache.maven.artifact.repository.MavenArtifactRepository

CLSS public org.apache.maven.repository.LocalRepositoryNotAccessibleException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.io.IOException

CLSS public org.apache.maven.repository.MavenArtifactMetadata
cons public init()
fld public final static java.lang.String DEFAULT_TYPE = "jar"
meth public java.lang.Object getDatum()
meth public java.lang.String getArtifactId()
meth public java.lang.String getClassifier()
meth public java.lang.String getGroupId()
meth public java.lang.String getScope()
meth public java.lang.String getType()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public void setArtifactId(java.lang.String)
meth public void setClassifier(java.lang.String)
meth public void setDatum(java.lang.Object)
meth public void setGroupId(java.lang.String)
meth public void setScope(java.lang.String)
meth public void setType(java.lang.String)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds artifactId,classifier,datum,groupId,scope,type,version

CLSS public org.apache.maven.repository.MetadataGraph
cons public init()
cons public init(org.apache.maven.repository.MetadataGraphNode)
meth public java.util.Collection<org.apache.maven.repository.MetadataGraphNode> getNodes()
meth public org.apache.maven.repository.MetadataGraphNode findNode(org.apache.maven.repository.MavenArtifactMetadata)
meth public org.apache.maven.repository.MetadataGraphNode getEntry()
meth public void addNode(org.apache.maven.repository.MetadataGraphNode)
supr java.lang.Object
hfds entry,nodes

CLSS public org.apache.maven.repository.MetadataGraphNode
cons public init()
cons public init(org.apache.maven.repository.MavenArtifactMetadata)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public org.apache.maven.repository.MetadataGraphNode addExident(org.apache.maven.repository.MetadataGraphNode)
meth public org.apache.maven.repository.MetadataGraphNode addIncident(org.apache.maven.repository.MetadataGraphNode)
supr java.lang.Object
hfds exNodes,inNodes,metadata

CLSS public org.apache.maven.repository.MetadataResolutionRequest
cons public init()
cons public init(org.apache.maven.repository.MavenArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public boolean isAsDirtyTree()
meth public boolean isAsGraph()
meth public boolean isAsList()
meth public boolean isAsResolvedTree()
meth public java.lang.String getScope()
meth public java.lang.String toString()
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getRemoteRepositories()
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getRemoteRepostories()
 anno 0 java.lang.Deprecated()
meth public java.util.Map getManagedVersionMap()
meth public java.util.Set<org.apache.maven.artifact.Artifact> getArtifactDependencies()
meth public org.apache.maven.artifact.repository.ArtifactRepository getLocalRepository()
meth public org.apache.maven.repository.MavenArtifactMetadata getArtifactMetadata()
meth public org.apache.maven.repository.MetadataResolutionRequest setArtifactDependencies(java.util.Set<org.apache.maven.artifact.Artifact>)
meth public org.apache.maven.repository.MetadataResolutionRequest setArtifactMetadata(org.apache.maven.repository.MavenArtifactMetadata)
meth public org.apache.maven.repository.MetadataResolutionRequest setAsDirtyTree(boolean)
meth public org.apache.maven.repository.MetadataResolutionRequest setAsGraph(boolean)
meth public org.apache.maven.repository.MetadataResolutionRequest setAsList(boolean)
meth public org.apache.maven.repository.MetadataResolutionRequest setAsResolvedTree(boolean)
meth public org.apache.maven.repository.MetadataResolutionRequest setLocalRepository(org.apache.maven.artifact.repository.ArtifactRepository)
meth public org.apache.maven.repository.MetadataResolutionRequest setManagedVersionMap(java.util.Map)
meth public org.apache.maven.repository.MetadataResolutionRequest setRemoteRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public org.apache.maven.repository.MetadataResolutionRequest setRemoteRepostories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
 anno 0 java.lang.Deprecated()
meth public org.apache.maven.repository.MetadataResolutionRequest setScope(java.lang.String)
supr java.lang.Object
hfds artifactDependencies,asDirtyTree,asGraph,asList,asResolvedTree,localRepository,mad,managedVersionMap,remoteRepositories,scope

CLSS public org.apache.maven.repository.MetadataResolutionResult
cons public init()
meth public boolean hasCircularDependencyExceptions()
meth public boolean hasErrorArtifactExceptions()
meth public boolean hasExceptions()
meth public boolean hasMetadataResolutionExceptions()
meth public boolean hasMissingArtifacts()
meth public boolean hasVersionRangeViolations()
meth public java.lang.String toString()
meth public java.util.List<java.lang.Exception> getExceptions()
meth public java.util.List<java.lang.Exception> getVersionRangeViolations()
meth public java.util.List<org.apache.maven.artifact.Artifact> getMissingArtifacts()
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getRepositories()
meth public java.util.List<org.apache.maven.artifact.resolver.ArtifactResolutionException> getErrorArtifactExceptions()
meth public java.util.List<org.apache.maven.artifact.resolver.ArtifactResolutionException> getMetadataResolutionExceptions()
meth public java.util.List<org.apache.maven.artifact.resolver.CyclicDependencyException> getCircularDependencyExceptions()
meth public java.util.Set<org.apache.maven.artifact.Artifact> getArtifacts()
meth public java.util.Set<org.apache.maven.artifact.Artifact> getRequestedArtifacts()
meth public org.apache.maven.artifact.Artifact getOriginatingArtifact()
meth public org.apache.maven.artifact.resolver.ArtifactResolutionException getMetadataResolutionException(int)
meth public org.apache.maven.artifact.resolver.CyclicDependencyException getCircularDependencyException(int)
meth public org.apache.maven.artifact.versioning.OverConstrainedVersionException getVersionRangeViolation(int)
meth public org.apache.maven.repository.MetadataGraph getResolvedTree()
meth public org.apache.maven.repository.MetadataResolutionResult addCircularDependencyException(org.apache.maven.artifact.resolver.CyclicDependencyException)
meth public org.apache.maven.repository.MetadataResolutionResult addError(java.lang.Exception)
meth public org.apache.maven.repository.MetadataResolutionResult addMetadataResolutionException(org.apache.maven.artifact.resolver.ArtifactResolutionException)
meth public org.apache.maven.repository.MetadataResolutionResult addMissingArtifact(org.apache.maven.artifact.Artifact)
meth public org.apache.maven.repository.MetadataResolutionResult addVersionRangeViolation(java.lang.Exception)
meth public org.apache.maven.repository.MetadataResolutionResult listOriginatingArtifact(org.apache.maven.artifact.Artifact)
meth public org.apache.maven.repository.MetadataResolutionResult setRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public org.apache.maven.repository.MetadataResolutionResult setUnresolvedArtifacts(java.util.List<org.apache.maven.artifact.Artifact>)
meth public void addArtifact(org.apache.maven.artifact.Artifact)
meth public void addRequestedArtifact(org.apache.maven.artifact.Artifact)
meth public void setResolvedTree(org.apache.maven.repository.MetadataGraph)
supr java.lang.Object
hfds artifacts,circularDependencyExceptions,dirtyTree,errorArtifactExceptions,exceptions,metadataResolutionExceptions,missingArtifacts,originatingArtifact,repositories,requestedArtifacts,resolvedGraph,resolvedTree,versionRangeViolations

CLSS public abstract interface org.apache.maven.repository.MirrorSelector
meth public abstract org.apache.maven.settings.Mirror getMirror(org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.settings.Mirror>)

CLSS public org.apache.maven.repository.Proxy
cons public init()
fld public final static java.lang.String PROXY_HTTP = "HTTP"
fld public final static java.lang.String PROXY_SOCKS4 = "SOCKS4"
fld public final static java.lang.String PROXY_SOCKS5 = "SOCKS_5"
meth public int getPort()
meth public java.lang.String getHost()
meth public java.lang.String getNonProxyHosts()
meth public java.lang.String getNtlmDomain()
meth public java.lang.String getNtlmHost()
meth public java.lang.String getPassword()
meth public java.lang.String getProtocol()
meth public java.lang.String getUserName()
meth public void setHost(java.lang.String)
meth public void setNonProxyHosts(java.lang.String)
meth public void setNtlmDomain(java.lang.String)
meth public void setNtlmHost(java.lang.String)
meth public void setPassword(java.lang.String)
meth public void setPort(int)
meth public void setProtocol(java.lang.String)
meth public void setUserName(java.lang.String)
supr java.lang.Object
hfds host,nonProxyHosts,ntlmDomain,ntlmHost,password,port,protocol,userName

CLSS public abstract interface org.apache.maven.repository.RepositorySystem
fld public final static java.io.File defaultUserLocalRepository
fld public final static java.io.File userMavenConfigurationHome
fld public final static java.lang.String DEFAULT_LOCAL_REPO_ID = "local"
fld public final static java.lang.String DEFAULT_REMOTE_REPO_ID = "central"
fld public final static java.lang.String DEFAULT_REMOTE_REPO_URL = "https://repo.maven.apache.org/maven2"
fld public final static java.lang.String userHome
meth public abstract java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getEffectiveRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public abstract org.apache.maven.artifact.Artifact createArtifact(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.apache.maven.artifact.Artifact createArtifact(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.apache.maven.artifact.Artifact createArtifactWithClassifier(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.apache.maven.artifact.Artifact createDependencyArtifact(org.apache.maven.model.Dependency)
meth public abstract org.apache.maven.artifact.Artifact createPluginArtifact(org.apache.maven.model.Plugin)
meth public abstract org.apache.maven.artifact.Artifact createProjectArtifact(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.apache.maven.artifact.repository.ArtifactRepository buildArtifactRepository(org.apache.maven.model.Repository) throws org.apache.maven.artifact.InvalidRepositoryException
meth public abstract org.apache.maven.artifact.repository.ArtifactRepository createArtifactRepository(java.lang.String,java.lang.String,org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy)
meth public abstract org.apache.maven.artifact.repository.ArtifactRepository createDefaultLocalRepository() throws org.apache.maven.artifact.InvalidRepositoryException
meth public abstract org.apache.maven.artifact.repository.ArtifactRepository createDefaultRemoteRepository() throws org.apache.maven.artifact.InvalidRepositoryException
meth public abstract org.apache.maven.artifact.repository.ArtifactRepository createLocalRepository(java.io.File) throws org.apache.maven.artifact.InvalidRepositoryException
meth public abstract org.apache.maven.artifact.resolver.ArtifactResolutionResult resolve(org.apache.maven.artifact.resolver.ArtifactResolutionRequest)
meth public abstract org.apache.maven.settings.Mirror getMirror(org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.settings.Mirror>)
meth public abstract void injectAuthentication(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,java.util.List<org.apache.maven.settings.Server>)
meth public abstract void injectAuthentication(org.eclipse.aether.RepositorySystemSession,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public abstract void injectMirror(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,java.util.List<org.apache.maven.settings.Mirror>)
meth public abstract void injectMirror(org.eclipse.aether.RepositorySystemSession,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public abstract void injectProxy(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,java.util.List<org.apache.maven.settings.Proxy>)
meth public abstract void injectProxy(org.eclipse.aether.RepositorySystemSession,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public abstract void publish(org.apache.maven.artifact.repository.ArtifactRepository,java.io.File,java.lang.String,org.apache.maven.repository.ArtifactTransferListener) throws org.apache.maven.repository.ArtifactTransferFailedException
meth public abstract void retrieve(org.apache.maven.artifact.repository.ArtifactRepository,java.io.File,java.lang.String,org.apache.maven.repository.ArtifactTransferListener) throws org.apache.maven.repository.ArtifactDoesNotExistException,org.apache.maven.repository.ArtifactTransferFailedException

CLSS public org.apache.maven.repository.UserLocalArtifactRepository
cons public init(org.apache.maven.artifact.repository.ArtifactRepository)
meth public boolean hasLocalMetadata()
meth public java.lang.String getId()
meth public java.lang.String pathOf(org.apache.maven.artifact.Artifact)
meth public java.lang.String pathOfLocalRepositoryMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository)
meth public org.apache.maven.artifact.Artifact find(org.apache.maven.artifact.Artifact)
supr org.apache.maven.repository.LocalArtifactRepository
hfds localRepository

CLSS public org.apache.maven.repository.VersionNotFoundException
cons public init(java.lang.String,org.apache.maven.model.Dependency,java.io.File,org.apache.maven.artifact.versioning.InvalidVersionSpecificationException)
meth public java.io.File getPomFile()
meth public java.lang.String getProjectId()
meth public org.apache.maven.artifact.versioning.InvalidVersionSpecificationException getCauseException()
meth public org.apache.maven.model.Dependency getDependency()
supr java.lang.Exception
hfds cause,dependency,pomFile,projectId

CLSS public org.apache.maven.repository.legacy.ChecksumFailedException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr org.apache.maven.wagon.TransferFailedException

CLSS public org.apache.maven.repository.legacy.DefaultUpdateCheckManager
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.repository.legacy.UpdateCheckManager, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
cons public init(org.codehaus.plexus.logging.Logger)
fld public final static java.lang.String LAST_UPDATE_TAG = ".lastUpdated"
intf org.apache.maven.repository.legacy.UpdateCheckManager
meth public boolean isUpdateRequired(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository)
meth public boolean isUpdateRequired(org.apache.maven.artifact.repository.metadata.RepositoryMetadata,org.apache.maven.artifact.repository.ArtifactRepository,java.io.File)
meth public java.lang.String getError(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository)
meth public void touch(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository,java.lang.String)
meth public void touch(org.apache.maven.artifact.repository.metadata.RepositoryMetadata,org.apache.maven.artifact.repository.ArtifactRepository,java.io.File)
supr org.codehaus.plexus.logging.AbstractLogEnabled
hfds ERROR_KEY_SUFFIX,TOUCHFILE_NAME

CLSS public org.apache.maven.repository.legacy.DefaultWagonManager
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.repository.legacy.WagonManager, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.repository.legacy.WagonManager
meth public org.apache.maven.wagon.Wagon getWagon(java.lang.String) throws org.apache.maven.wagon.UnsupportedProtocolException
 anno 0 java.lang.Deprecated()
meth public org.apache.maven.wagon.Wagon getWagon(org.apache.maven.wagon.repository.Repository) throws org.apache.maven.wagon.UnsupportedProtocolException
 anno 0 java.lang.Deprecated()
meth public void getArtifact(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.wagon.events.TransferListener,boolean) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException
meth public void getArtifact(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.wagon.events.TransferListener,boolean) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException
meth public void getArtifactMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository,java.io.File,java.lang.String) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException
meth public void getArtifactMetadataFromDeploymentRepository(org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository,java.io.File,java.lang.String) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException
meth public void getRemoteFile(org.apache.maven.artifact.repository.ArtifactRepository,java.io.File,java.lang.String,org.apache.maven.wagon.events.TransferListener,java.lang.String,boolean) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException
meth public void putArtifact(java.io.File,org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.wagon.events.TransferListener) throws org.apache.maven.wagon.TransferFailedException
meth public void putArtifactMetadata(java.io.File,org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.wagon.TransferFailedException
meth public void putRemoteFile(org.apache.maven.artifact.repository.ArtifactRepository,java.io.File,java.lang.String,org.apache.maven.wagon.events.TransferListener) throws org.apache.maven.wagon.TransferFailedException
supr java.lang.Object
hfds CHECKSUM_ALGORITHMS,CHECKSUM_IDS,container,legacySupport,logger,updateCheckManager

CLSS public org.apache.maven.repository.legacy.LegacyRepositorySystem
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.repository.RepositorySystem, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="default", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.repository.RepositorySystem
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getEffectiveRepositories(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public org.apache.maven.artifact.Artifact createArtifact(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public org.apache.maven.artifact.Artifact createArtifact(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public org.apache.maven.artifact.Artifact createArtifactWithClassifier(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public org.apache.maven.artifact.Artifact createDependencyArtifact(org.apache.maven.model.Dependency)
meth public org.apache.maven.artifact.Artifact createExtensionArtifact(java.lang.String,java.lang.String,java.lang.String)
meth public org.apache.maven.artifact.Artifact createParentArtifact(java.lang.String,java.lang.String,java.lang.String)
meth public org.apache.maven.artifact.Artifact createPluginArtifact(org.apache.maven.model.Plugin)
meth public org.apache.maven.artifact.Artifact createProjectArtifact(java.lang.String,java.lang.String,java.lang.String)
meth public org.apache.maven.artifact.repository.ArtifactRepository buildArtifactRepository(org.apache.maven.model.Repository) throws org.apache.maven.artifact.InvalidRepositoryException
meth public org.apache.maven.artifact.repository.ArtifactRepository createArtifactRepository(java.lang.String,java.lang.String,org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy,org.apache.maven.artifact.repository.ArtifactRepositoryPolicy)
meth public org.apache.maven.artifact.repository.ArtifactRepository createDefaultLocalRepository() throws org.apache.maven.artifact.InvalidRepositoryException
meth public org.apache.maven.artifact.repository.ArtifactRepository createDefaultRemoteRepository() throws org.apache.maven.artifact.InvalidRepositoryException
meth public org.apache.maven.artifact.repository.ArtifactRepository createLocalRepository(java.io.File) throws org.apache.maven.artifact.InvalidRepositoryException
meth public org.apache.maven.artifact.repository.ArtifactRepository createLocalRepository(java.lang.String,java.lang.String) throws java.io.IOException
meth public org.apache.maven.artifact.repository.ArtifactRepositoryPolicy buildArtifactRepositoryPolicy(org.apache.maven.model.RepositoryPolicy)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult resolve(org.apache.maven.artifact.resolver.ArtifactResolutionRequest)
meth public org.apache.maven.settings.Mirror getMirror(org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.settings.Mirror>)
meth public void injectAuthentication(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,java.util.List<org.apache.maven.settings.Server>)
meth public void injectAuthentication(org.eclipse.aether.RepositorySystemSession,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public void injectMirror(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,java.util.List<org.apache.maven.settings.Mirror>)
meth public void injectMirror(org.eclipse.aether.RepositorySystemSession,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public void injectProxy(java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,java.util.List<org.apache.maven.settings.Proxy>)
meth public void injectProxy(org.eclipse.aether.RepositorySystemSession,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public void publish(org.apache.maven.artifact.repository.ArtifactRepository,java.io.File,java.lang.String,org.apache.maven.repository.ArtifactTransferListener) throws org.apache.maven.repository.ArtifactTransferFailedException
meth public void retrieve(org.apache.maven.artifact.repository.ArtifactRepository,java.io.File,java.lang.String,org.apache.maven.repository.ArtifactTransferListener) throws org.apache.maven.repository.ArtifactDoesNotExistException,org.apache.maven.repository.ArtifactTransferFailedException
supr java.lang.Object
hfds artifactFactory,artifactRepositoryFactory,artifactResolver,layouts,logger,mirrorSelector,plexus,settingsDecrypter,wagonManager
hcls UnknownRepositoryLayout

CLSS public org.apache.maven.repository.legacy.TransferListenerAdapter
intf org.apache.maven.wagon.events.TransferListener
meth public static org.apache.maven.wagon.events.TransferListener newAdapter(org.apache.maven.repository.ArtifactTransferListener)
meth public void debug(java.lang.String)
meth public void transferCompleted(org.apache.maven.wagon.events.TransferEvent)
meth public void transferError(org.apache.maven.wagon.events.TransferEvent)
meth public void transferInitiated(org.apache.maven.wagon.events.TransferEvent)
meth public void transferProgress(org.apache.maven.wagon.events.TransferEvent,byte[],int)
meth public void transferStarted(org.apache.maven.wagon.events.TransferEvent)
supr java.lang.Object
hfds artifacts,listener,transfers

CLSS public abstract interface org.apache.maven.repository.legacy.UpdateCheckManager
meth public abstract boolean isUpdateRequired(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository)
meth public abstract boolean isUpdateRequired(org.apache.maven.artifact.repository.metadata.RepositoryMetadata,org.apache.maven.artifact.repository.ArtifactRepository,java.io.File)
meth public abstract java.lang.String getError(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository)
meth public abstract void touch(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository,java.lang.String)
meth public abstract void touch(org.apache.maven.artifact.repository.metadata.RepositoryMetadata,org.apache.maven.artifact.repository.ArtifactRepository,java.io.File)

CLSS public org.apache.maven.repository.legacy.WagonConfigurationException
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.Throwable)
meth public final java.lang.String getOriginalMessage()
meth public final java.lang.String getRepositoryId()
supr org.apache.maven.wagon.TransferFailedException
hfds originalMessage,repositoryId,serialVersionUID

CLSS public abstract interface org.apache.maven.repository.legacy.WagonManager
meth public abstract org.apache.maven.wagon.Wagon getWagon(java.lang.String) throws org.apache.maven.wagon.UnsupportedProtocolException
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.wagon.Wagon getWagon(org.apache.maven.wagon.repository.Repository) throws org.apache.maven.repository.legacy.WagonConfigurationException,org.apache.maven.wagon.UnsupportedProtocolException
 anno 0 java.lang.Deprecated()
meth public abstract void getArtifact(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.wagon.events.TransferListener,boolean) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException
meth public abstract void getArtifact(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.wagon.events.TransferListener,boolean) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException
meth public abstract void getArtifactMetadata(org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository,java.io.File,java.lang.String) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException
meth public abstract void getArtifactMetadataFromDeploymentRepository(org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository,java.io.File,java.lang.String) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException
meth public abstract void getRemoteFile(org.apache.maven.artifact.repository.ArtifactRepository,java.io.File,java.lang.String,org.apache.maven.wagon.events.TransferListener,java.lang.String,boolean) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException
meth public abstract void putArtifact(java.io.File,org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.wagon.events.TransferListener) throws org.apache.maven.wagon.TransferFailedException
meth public abstract void putArtifactMetadata(java.io.File,org.apache.maven.artifact.metadata.ArtifactMetadata,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.wagon.TransferFailedException
meth public abstract void putRemoteFile(org.apache.maven.artifact.repository.ArtifactRepository,java.io.File,java.lang.String,org.apache.maven.wagon.events.TransferListener) throws org.apache.maven.wagon.TransferFailedException

CLSS public abstract org.apache.maven.repository.legacy.metadata.AbstractArtifactMetadata
cons protected init(org.apache.maven.artifact.Artifact)
fld protected org.apache.maven.artifact.Artifact artifact
intf org.apache.maven.repository.legacy.metadata.ArtifactMetadata
meth public boolean storedInGroupDirectory()
meth public java.lang.String extendedToString()
meth public java.lang.String getArtifactId()
meth public java.lang.String getGroupId()
supr java.lang.Object

CLSS public abstract interface org.apache.maven.repository.legacy.metadata.ArtifactMetadata
meth public abstract boolean storedInArtifactVersionDirectory()
meth public abstract boolean storedInGroupDirectory()
meth public abstract java.lang.Object getKey()
meth public abstract java.lang.String extendedToString()
meth public abstract java.lang.String getArtifactId()
meth public abstract java.lang.String getBaseVersion()
meth public abstract java.lang.String getGroupId()
meth public abstract java.lang.String getLocalFilename(org.apache.maven.artifact.repository.ArtifactRepository)
meth public abstract java.lang.String getRemoteFilename()
meth public abstract void merge(org.apache.maven.repository.legacy.metadata.ArtifactMetadata)
meth public abstract void storeInLocalRepository(org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.repository.metadata.RepositoryMetadataStoreException

CLSS public org.apache.maven.repository.legacy.metadata.ArtifactMetadataRetrievalException
cons public init(java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.Throwable)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.Throwable,org.apache.maven.artifact.Artifact)
cons public init(java.lang.Throwable)
 anno 0 java.lang.Deprecated()
meth public org.apache.maven.artifact.Artifact getArtifact()
supr java.lang.Exception
hfds artifact

CLSS public abstract interface org.apache.maven.repository.legacy.metadata.ArtifactMetadataSource
meth public abstract java.util.List<org.apache.maven.artifact.versioning.ArtifactVersion> retrieveAvailableVersions(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>) throws org.apache.maven.repository.legacy.metadata.ArtifactMetadataRetrievalException
meth public abstract java.util.List<org.apache.maven.artifact.versioning.ArtifactVersion> retrieveAvailableVersionsFromDeploymentRepository(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.repository.legacy.metadata.ArtifactMetadataRetrievalException
meth public abstract org.apache.maven.repository.legacy.metadata.ResolutionGroup retrieve(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>) throws org.apache.maven.repository.legacy.metadata.ArtifactMetadataRetrievalException
meth public abstract org.apache.maven.repository.legacy.metadata.ResolutionGroup retrieve(org.apache.maven.repository.legacy.metadata.MetadataResolutionRequest) throws org.apache.maven.repository.legacy.metadata.ArtifactMetadataRetrievalException

CLSS public org.apache.maven.repository.legacy.metadata.ResolutionGroup
cons public init(org.apache.maven.artifact.Artifact,java.util.Set<org.apache.maven.artifact.Artifact>,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
cons public init(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact,java.util.Set<org.apache.maven.artifact.Artifact>,java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>)
meth public java.util.List<org.apache.maven.artifact.repository.ArtifactRepository> getResolutionRepositories()
meth public java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact> getManagedVersions()
meth public java.util.Set<org.apache.maven.artifact.Artifact> getArtifacts()
meth public org.apache.maven.artifact.Artifact getPomArtifact()
meth public org.apache.maven.artifact.Artifact getRelocatedArtifact()
supr java.lang.Object
hfds artifacts,managedVersions,pomArtifact,relocatedArtifact,resolutionRepositories

CLSS public org.apache.maven.repository.legacy.resolver.DefaultLegacyArtifactCollector
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.repository.legacy.resolver.LegacyArtifactCollector, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.repository.legacy.resolver.LegacyArtifactCollector
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult collect(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter,java.util.List<org.apache.maven.artifact.resolver.ResolutionListener>)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult collect(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter,java.util.List<org.apache.maven.artifact.resolver.ResolutionListener>,java.util.List<org.apache.maven.repository.legacy.resolver.conflict.ConflictResolver>)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult collect(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.resolver.ArtifactResolutionRequest,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter,java.util.List<org.apache.maven.artifact.resolver.ResolutionListener>,java.util.List<org.apache.maven.repository.legacy.resolver.conflict.ConflictResolver>)
meth public org.apache.maven.artifact.resolver.ArtifactResolutionResult collect(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter,java.util.List<org.apache.maven.artifact.resolver.ResolutionListener>)
supr java.lang.Object
hfds defaultConflictResolver,legacySupport,logger

CLSS public abstract interface org.apache.maven.repository.legacy.resolver.LegacyArtifactCollector
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.artifact.resolver.ArtifactResolutionResult collect(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter,java.util.List<org.apache.maven.artifact.resolver.ResolutionListener>)
 anno 0 java.lang.Deprecated()
meth public abstract org.apache.maven.artifact.resolver.ArtifactResolutionResult collect(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.repository.ArtifactRepository,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter,java.util.List<org.apache.maven.artifact.resolver.ResolutionListener>,java.util.List<org.apache.maven.repository.legacy.resolver.conflict.ConflictResolver>)
meth public abstract org.apache.maven.artifact.resolver.ArtifactResolutionResult collect(java.util.Set<org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.Artifact,java.util.Map<java.lang.String,org.apache.maven.artifact.Artifact>,org.apache.maven.artifact.resolver.ArtifactResolutionRequest,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter,java.util.List<org.apache.maven.artifact.resolver.ResolutionListener>,java.util.List<org.apache.maven.repository.legacy.resolver.conflict.ConflictResolver>)

CLSS public org.apache.maven.settings.Activation
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean isActiveByDefault()
meth public java.lang.String getJdk()
meth public org.apache.maven.settings.Activation clone()
meth public org.apache.maven.settings.ActivationFile getFile()
meth public org.apache.maven.settings.ActivationOS getOs()
meth public org.apache.maven.settings.ActivationProperty getProperty()
meth public void setActiveByDefault(boolean)
meth public void setFile(org.apache.maven.settings.ActivationFile)
meth public void setJdk(java.lang.String)
meth public void setOs(org.apache.maven.settings.ActivationOS)
meth public void setProperty(org.apache.maven.settings.ActivationProperty)
supr java.lang.Object
hfds activeByDefault,file,jdk,os,property

CLSS public org.apache.maven.settings.ActivationFile
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.String getExists()
meth public java.lang.String getMissing()
meth public org.apache.maven.settings.ActivationFile clone()
meth public void setExists(java.lang.String)
meth public void setMissing(java.lang.String)
supr java.lang.Object
hfds exists,missing

CLSS public org.apache.maven.settings.ActivationOS
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.String getArch()
meth public java.lang.String getFamily()
meth public java.lang.String getName()
meth public java.lang.String getVersion()
meth public org.apache.maven.settings.ActivationOS clone()
meth public void setArch(java.lang.String)
meth public void setFamily(java.lang.String)
meth public void setName(java.lang.String)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds arch,family,name,version

CLSS public org.apache.maven.settings.ActivationProperty
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.String getName()
meth public java.lang.String getValue()
meth public org.apache.maven.settings.ActivationProperty clone()
meth public void setName(java.lang.String)
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds name,value

CLSS public org.apache.maven.settings.DefaultMavenSettingsBuilder
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.settings.MavenSettingsBuilder, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.settings.MavenSettingsBuilder
meth public org.apache.maven.settings.Settings buildSettings() throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.settings.Settings buildSettings(boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.settings.Settings buildSettings(java.io.File) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.settings.Settings buildSettings(java.io.File,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.settings.Settings buildSettings(org.apache.maven.execution.MavenExecutionRequest) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
supr org.codehaus.plexus.logging.AbstractLogEnabled
hfds settingsBuilder

CLSS public org.apache.maven.settings.IdentifiableBase
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.String getId()
meth public org.apache.maven.settings.IdentifiableBase clone()
meth public void setId(java.lang.String)
supr org.apache.maven.settings.TrackableBase
hfds id

CLSS public abstract interface org.apache.maven.settings.MavenSettingsBuilder
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String ALT_GLOBAL_SETTINGS_XML_LOCATION = "org.apache.maven.global-settings"
fld public final static java.lang.String ALT_LOCAL_REPOSITORY_LOCATION = "maven.repo.local"
fld public final static java.lang.String ALT_USER_SETTINGS_XML_LOCATION = "org.apache.maven.user-settings"
fld public final static java.lang.String ROLE
meth public abstract org.apache.maven.settings.Settings buildSettings() throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract org.apache.maven.settings.Settings buildSettings(boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract org.apache.maven.settings.Settings buildSettings(java.io.File) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract org.apache.maven.settings.Settings buildSettings(java.io.File,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract org.apache.maven.settings.Settings buildSettings(org.apache.maven.execution.MavenExecutionRequest) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException

CLSS public org.apache.maven.settings.Mirror
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean isBlocked()
meth public java.lang.String getLayout()
meth public java.lang.String getMirrorOf()
meth public java.lang.String getMirrorOfLayouts()
meth public java.lang.String getName()
meth public java.lang.String getUrl()
meth public java.lang.String toString()
meth public org.apache.maven.settings.Mirror clone()
meth public void setBlocked(boolean)
meth public void setLayout(java.lang.String)
meth public void setMirrorOf(java.lang.String)
meth public void setMirrorOfLayouts(java.lang.String)
meth public void setName(java.lang.String)
meth public void setUrl(java.lang.String)
supr org.apache.maven.settings.IdentifiableBase
hfds blocked,layout,mirrorOf,mirrorOfLayouts,name,url

CLSS public org.apache.maven.settings.Profile
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.util.List<org.apache.maven.settings.Repository> getPluginRepositories()
meth public java.util.List<org.apache.maven.settings.Repository> getRepositories()
meth public java.util.Properties getProperties()
meth public org.apache.maven.settings.Activation getActivation()
meth public org.apache.maven.settings.Profile clone()
meth public void addPluginRepository(org.apache.maven.settings.Repository)
meth public void addProperty(java.lang.String,java.lang.String)
meth public void addRepository(org.apache.maven.settings.Repository)
meth public void removePluginRepository(org.apache.maven.settings.Repository)
meth public void removeRepository(org.apache.maven.settings.Repository)
meth public void setActivation(org.apache.maven.settings.Activation)
meth public void setPluginRepositories(java.util.List<org.apache.maven.settings.Repository>)
meth public void setProperties(java.util.Properties)
meth public void setRepositories(java.util.List<org.apache.maven.settings.Repository>)
supr org.apache.maven.settings.IdentifiableBase
hfds activation,pluginRepositories,properties,repositories

CLSS public org.apache.maven.settings.Proxy
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean isActive()
meth public int getPort()
meth public java.lang.String getHost()
meth public java.lang.String getNonProxyHosts()
meth public java.lang.String getPassword()
meth public java.lang.String getProtocol()
meth public java.lang.String getUsername()
meth public org.apache.maven.settings.Proxy clone()
meth public void setActive(boolean)
meth public void setHost(java.lang.String)
meth public void setNonProxyHosts(java.lang.String)
meth public void setPassword(java.lang.String)
meth public void setPort(int)
meth public void setProtocol(java.lang.String)
meth public void setUsername(java.lang.String)
supr org.apache.maven.settings.IdentifiableBase
hfds active,host,nonProxyHosts,password,port,protocol,username

CLSS public org.apache.maven.settings.Repository
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public org.apache.maven.settings.Repository clone()
meth public org.apache.maven.settings.RepositoryPolicy getReleases()
meth public org.apache.maven.settings.RepositoryPolicy getSnapshots()
meth public void setReleases(org.apache.maven.settings.RepositoryPolicy)
meth public void setSnapshots(org.apache.maven.settings.RepositoryPolicy)
supr org.apache.maven.settings.RepositoryBase
hfds releases,snapshots

CLSS public org.apache.maven.settings.RepositoryBase
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public java.lang.String getId()
meth public java.lang.String getLayout()
meth public java.lang.String getName()
meth public java.lang.String getUrl()
meth public org.apache.maven.settings.RepositoryBase clone()
meth public void setId(java.lang.String)
meth public void setLayout(java.lang.String)
meth public void setName(java.lang.String)
meth public void setUrl(java.lang.String)
supr java.lang.Object
hfds id,layout,name,url

CLSS public org.apache.maven.settings.RepositoryPolicy
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean isEnabled()
meth public java.lang.String getChecksumPolicy()
meth public java.lang.String getUpdatePolicy()
meth public org.apache.maven.settings.RepositoryPolicy clone()
meth public void setChecksumPolicy(java.lang.String)
meth public void setEnabled(boolean)
meth public void setUpdatePolicy(java.lang.String)
supr java.lang.Object
hfds checksumPolicy,enabled,updatePolicy

CLSS public org.apache.maven.settings.RuntimeInfo
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(java.io.File)
fld public final static java.io.File DEFAULT_USER_SETTINGS_FILE
fld public final static java.io.File userMavenConfigurationHome
fld public final static java.lang.String userHome
meth public java.io.File getFile()
supr java.lang.Object
hfds settings

CLSS public org.apache.maven.settings.Server
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.Object getConfiguration()
meth public java.lang.String getDirectoryPermissions()
meth public java.lang.String getFilePermissions()
meth public java.lang.String getPassphrase()
meth public java.lang.String getPassword()
meth public java.lang.String getPrivateKey()
meth public java.lang.String getUsername()
meth public org.apache.maven.settings.Server clone()
meth public void setConfiguration(java.lang.Object)
meth public void setDirectoryPermissions(java.lang.String)
meth public void setFilePermissions(java.lang.String)
meth public void setPassphrase(java.lang.String)
meth public void setPassword(java.lang.String)
meth public void setPrivateKey(java.lang.String)
meth public void setUsername(java.lang.String)
supr org.apache.maven.settings.IdentifiableBase
hfds configuration,directoryPermissions,filePermissions,passphrase,password,privateKey,username

CLSS public org.apache.maven.settings.Settings
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean isInteractiveMode()
meth public boolean isOffline()
meth public boolean isUsePluginRegistry()
meth public java.lang.Boolean getInteractiveMode()
meth public java.lang.String getLocalRepository()
meth public java.lang.String getModelEncoding()
meth public java.util.List<java.lang.String> getActiveProfiles()
meth public java.util.List<java.lang.String> getPluginGroups()
meth public java.util.List<org.apache.maven.settings.Mirror> getMirrors()
meth public java.util.List<org.apache.maven.settings.Profile> getProfiles()
meth public java.util.List<org.apache.maven.settings.Proxy> getProxies()
meth public java.util.List<org.apache.maven.settings.Server> getServers()
meth public java.util.Map<java.lang.String,org.apache.maven.settings.Profile> getProfilesAsMap()
meth public org.apache.maven.settings.Mirror getMirrorOf(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public org.apache.maven.settings.Proxy getActiveProxy()
meth public org.apache.maven.settings.Server getServer(java.lang.String)
meth public org.apache.maven.settings.Settings clone()
meth public void addActiveProfile(java.lang.String)
meth public void addMirror(org.apache.maven.settings.Mirror)
meth public void addPluginGroup(java.lang.String)
meth public void addProfile(org.apache.maven.settings.Profile)
meth public void addProxy(org.apache.maven.settings.Proxy)
meth public void addServer(org.apache.maven.settings.Server)
meth public void flushActiveProxy()
meth public void flushProfileMap()
meth public void removeActiveProfile(java.lang.String)
meth public void removeMirror(org.apache.maven.settings.Mirror)
meth public void removePluginGroup(java.lang.String)
meth public void removeProfile(org.apache.maven.settings.Profile)
meth public void removeProxy(org.apache.maven.settings.Proxy)
meth public void removeServer(org.apache.maven.settings.Server)
meth public void setActiveProfiles(java.util.List<java.lang.String>)
meth public void setInteractiveMode(boolean)
meth public void setLocalRepository(java.lang.String)
meth public void setMirrors(java.util.List<org.apache.maven.settings.Mirror>)
meth public void setModelEncoding(java.lang.String)
meth public void setOffline(boolean)
meth public void setPluginGroups(java.util.List<java.lang.String>)
meth public void setProfiles(java.util.List<org.apache.maven.settings.Profile>)
meth public void setProxies(java.util.List<org.apache.maven.settings.Proxy>)
meth public void setServers(java.util.List<org.apache.maven.settings.Server>)
meth public void setUsePluginRegistry(boolean)
supr org.apache.maven.settings.TrackableBase
hfds activeProfiles,activeProxy,interactiveMode,localRepository,mirrors,modelEncoding,offline,pluginGroups,profileMap,profiles,proxies,servers,usePluginRegistry

CLSS public org.apache.maven.settings.SettingsConfigurationException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,java.lang.Throwable,int,int)
meth public int getColumnNumber()
meth public int getLineNumber()
supr java.lang.Exception
hfds columnNumber,lineNumber

CLSS public final org.apache.maven.settings.SettingsUtils
meth public static org.apache.maven.model.Profile convertFromSettingsProfile(org.apache.maven.settings.Profile)
meth public static org.apache.maven.settings.Profile convertToSettingsProfile(org.apache.maven.model.Profile)
meth public static org.apache.maven.settings.Settings copySettings(org.apache.maven.settings.Settings)
meth public static void merge(org.apache.maven.settings.Settings,org.apache.maven.settings.Settings,java.lang.String)
supr java.lang.Object

CLSS public org.apache.maven.settings.TrackableBase
cons public init()
fld public final static java.lang.String GLOBAL_LEVEL = "global-level"
fld public final static java.lang.String USER_LEVEL = "user-level"
intf java.io.Serializable
intf java.lang.Cloneable
meth public java.lang.String getSourceLevel()
meth public org.apache.maven.settings.TrackableBase clone()
meth public void setSourceLevel(java.lang.String)
supr java.lang.Object
hfds sourceLevel,sourceLevelSet

CLSS public org.apache.maven.settings.building.DefaultSettingsBuilder
 anno 0 javax.inject.Named(java.lang.String value="")
 anno 0 javax.inject.Singleton()
cons public init(org.apache.maven.settings.io.SettingsReader,org.apache.maven.settings.io.SettingsWriter,org.apache.maven.settings.validation.SettingsValidator)
 anno 0 javax.inject.Inject()
intf org.apache.maven.settings.building.SettingsBuilder
meth public org.apache.maven.settings.building.DefaultSettingsBuilder setSettingsReader(org.apache.maven.settings.io.SettingsReader)
meth public org.apache.maven.settings.building.DefaultSettingsBuilder setSettingsValidator(org.apache.maven.settings.validation.SettingsValidator)
meth public org.apache.maven.settings.building.DefaultSettingsBuilder setSettingsWriter(org.apache.maven.settings.io.SettingsWriter)
meth public org.apache.maven.settings.building.SettingsBuildingResult build(org.apache.maven.settings.building.SettingsBuildingRequest) throws org.apache.maven.settings.building.SettingsBuildingException
supr java.lang.Object
hfds settingsMerger,settingsReader,settingsValidator,settingsWriter

CLSS public org.apache.maven.settings.building.DefaultSettingsBuilderFactory
cons public init()
meth protected org.apache.maven.settings.io.SettingsReader newSettingsReader()
meth protected org.apache.maven.settings.io.SettingsWriter newSettingsWriter()
meth protected org.apache.maven.settings.validation.SettingsValidator newSettingsValidator()
meth public org.apache.maven.settings.building.DefaultSettingsBuilder newInstance()
supr java.lang.Object

CLSS public org.apache.maven.settings.building.DefaultSettingsBuildingRequest
cons public init()
intf org.apache.maven.settings.building.SettingsBuildingRequest
meth public java.io.File getGlobalSettingsFile()
meth public java.io.File getUserSettingsFile()
meth public java.util.Properties getSystemProperties()
meth public java.util.Properties getUserProperties()
meth public org.apache.maven.settings.building.DefaultSettingsBuildingRequest setGlobalSettingsFile(java.io.File)
meth public org.apache.maven.settings.building.DefaultSettingsBuildingRequest setGlobalSettingsSource(org.apache.maven.settings.building.SettingsSource)
meth public org.apache.maven.settings.building.DefaultSettingsBuildingRequest setSystemProperties(java.util.Properties)
meth public org.apache.maven.settings.building.DefaultSettingsBuildingRequest setUserProperties(java.util.Properties)
meth public org.apache.maven.settings.building.DefaultSettingsBuildingRequest setUserSettingsFile(java.io.File)
meth public org.apache.maven.settings.building.DefaultSettingsBuildingRequest setUserSettingsSource(org.apache.maven.settings.building.SettingsSource)
meth public org.apache.maven.settings.building.SettingsSource getGlobalSettingsSource()
meth public org.apache.maven.settings.building.SettingsSource getUserSettingsSource()
supr java.lang.Object
hfds globalSettingsFile,globalSettingsSource,systemProperties,userProperties,userSettingsFile,userSettingsSource

CLSS public org.apache.maven.settings.building.DefaultSettingsProblem
cons public init(java.lang.String,org.apache.maven.settings.building.SettingsProblem$Severity,java.lang.String,int,int,java.lang.Exception)
intf org.apache.maven.settings.building.SettingsProblem
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.Exception getException()
meth public java.lang.String getLocation()
meth public java.lang.String getMessage()
meth public java.lang.String getSource()
meth public java.lang.String toString()
meth public org.apache.maven.settings.building.SettingsProblem$Severity getSeverity()
supr java.lang.Object
hfds columnNumber,exception,lineNumber,message,severity,source

CLSS public org.apache.maven.settings.building.FileSettingsSource
 anno 0 java.lang.Deprecated()
cons public init(java.io.File)
intf org.apache.maven.settings.building.SettingsSource
meth public java.io.File getSettingsFile()
 anno 0 java.lang.Deprecated()
supr org.apache.maven.building.FileSource

CLSS public abstract interface org.apache.maven.settings.building.SettingsBuilder
meth public abstract org.apache.maven.settings.building.SettingsBuildingResult build(org.apache.maven.settings.building.SettingsBuildingRequest) throws org.apache.maven.settings.building.SettingsBuildingException

CLSS public org.apache.maven.settings.building.SettingsBuildingException
cons public init(java.util.List<org.apache.maven.settings.building.SettingsProblem>)
meth public java.util.List<org.apache.maven.settings.building.SettingsProblem> getProblems()
supr java.lang.Exception
hfds problems

CLSS public abstract interface org.apache.maven.settings.building.SettingsBuildingRequest
meth public abstract java.io.File getGlobalSettingsFile()
meth public abstract java.io.File getUserSettingsFile()
meth public abstract java.util.Properties getSystemProperties()
meth public abstract java.util.Properties getUserProperties()
meth public abstract org.apache.maven.settings.building.SettingsBuildingRequest setGlobalSettingsFile(java.io.File)
meth public abstract org.apache.maven.settings.building.SettingsBuildingRequest setGlobalSettingsSource(org.apache.maven.settings.building.SettingsSource)
meth public abstract org.apache.maven.settings.building.SettingsBuildingRequest setSystemProperties(java.util.Properties)
meth public abstract org.apache.maven.settings.building.SettingsBuildingRequest setUserProperties(java.util.Properties)
meth public abstract org.apache.maven.settings.building.SettingsBuildingRequest setUserSettingsFile(java.io.File)
meth public abstract org.apache.maven.settings.building.SettingsBuildingRequest setUserSettingsSource(org.apache.maven.settings.building.SettingsSource)
meth public abstract org.apache.maven.settings.building.SettingsSource getGlobalSettingsSource()
meth public abstract org.apache.maven.settings.building.SettingsSource getUserSettingsSource()

CLSS public abstract interface org.apache.maven.settings.building.SettingsBuildingResult
meth public abstract java.util.List<org.apache.maven.settings.building.SettingsProblem> getProblems()
meth public abstract org.apache.maven.settings.Settings getEffectiveSettings()

CLSS public abstract interface org.apache.maven.settings.building.SettingsProblem
innr public final static !enum Severity
meth public abstract int getColumnNumber()
meth public abstract int getLineNumber()
meth public abstract java.lang.Exception getException()
meth public abstract java.lang.String getLocation()
meth public abstract java.lang.String getMessage()
meth public abstract java.lang.String getSource()
meth public abstract org.apache.maven.settings.building.SettingsProblem$Severity getSeverity()

CLSS public final static !enum org.apache.maven.settings.building.SettingsProblem$Severity
 outer org.apache.maven.settings.building.SettingsProblem
fld public final static org.apache.maven.settings.building.SettingsProblem$Severity ERROR
fld public final static org.apache.maven.settings.building.SettingsProblem$Severity FATAL
fld public final static org.apache.maven.settings.building.SettingsProblem$Severity WARNING
meth public static org.apache.maven.settings.building.SettingsProblem$Severity valueOf(java.lang.String)
meth public static org.apache.maven.settings.building.SettingsProblem$Severity[] values()
supr java.lang.Enum<org.apache.maven.settings.building.SettingsProblem$Severity>

CLSS public abstract interface org.apache.maven.settings.building.SettingsProblemCollector
meth public abstract void add(org.apache.maven.settings.building.SettingsProblem$Severity,java.lang.String,int,int,java.lang.Exception)

CLSS public abstract interface org.apache.maven.settings.building.SettingsSource
 anno 0 java.lang.Deprecated()
intf org.apache.maven.building.Source

CLSS public org.apache.maven.settings.building.StringSettingsSource
 anno 0 java.lang.Deprecated()
cons public init(java.lang.CharSequence)
cons public init(java.lang.CharSequence,java.lang.String)
intf org.apache.maven.settings.building.SettingsSource
meth public java.lang.String getSettings()
 anno 0 java.lang.Deprecated()
supr org.apache.maven.building.StringSource

CLSS public org.apache.maven.settings.building.UrlSettingsSource
 anno 0 java.lang.Deprecated()
cons public init(java.net.URL)
intf org.apache.maven.settings.building.SettingsSource
meth public java.net.URL getSettingsUrl()
 anno 0 java.lang.Deprecated()
supr org.apache.maven.building.UrlSource

CLSS public org.apache.maven.settings.crypto.DefaultSettingsDecrypter
 anno 0 javax.inject.Named(java.lang.String value="")
 anno 0 javax.inject.Singleton()
cons public init(org.sonatype.plexus.components.sec.dispatcher.SecDispatcher)
 anno 0 javax.inject.Inject()
 anno 1 javax.inject.Named(java.lang.String value="maven")
intf org.apache.maven.settings.crypto.SettingsDecrypter
meth public org.apache.maven.settings.crypto.SettingsDecryptionResult decrypt(org.apache.maven.settings.crypto.SettingsDecryptionRequest)
supr java.lang.Object
hfds securityDispatcher

CLSS public org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest
cons public init()
cons public init(org.apache.maven.settings.Proxy)
cons public init(org.apache.maven.settings.Server)
cons public init(org.apache.maven.settings.Settings)
intf org.apache.maven.settings.crypto.SettingsDecryptionRequest
meth public java.util.List<org.apache.maven.settings.Proxy> getProxies()
meth public java.util.List<org.apache.maven.settings.Server> getServers()
meth public org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest setProxies(java.util.List<org.apache.maven.settings.Proxy>)
meth public org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest setServers(java.util.List<org.apache.maven.settings.Server>)
supr java.lang.Object
hfds proxies,servers

CLSS public abstract interface org.apache.maven.settings.crypto.SettingsDecrypter
meth public abstract org.apache.maven.settings.crypto.SettingsDecryptionResult decrypt(org.apache.maven.settings.crypto.SettingsDecryptionRequest)

CLSS public abstract interface org.apache.maven.settings.crypto.SettingsDecryptionRequest
meth public abstract java.util.List<org.apache.maven.settings.Proxy> getProxies()
meth public abstract java.util.List<org.apache.maven.settings.Server> getServers()
meth public abstract org.apache.maven.settings.crypto.SettingsDecryptionRequest setProxies(java.util.List<org.apache.maven.settings.Proxy>)
meth public abstract org.apache.maven.settings.crypto.SettingsDecryptionRequest setServers(java.util.List<org.apache.maven.settings.Server>)

CLSS public abstract interface org.apache.maven.settings.crypto.SettingsDecryptionResult
meth public abstract java.util.List<org.apache.maven.settings.Proxy> getProxies()
meth public abstract java.util.List<org.apache.maven.settings.Server> getServers()
meth public abstract java.util.List<org.apache.maven.settings.building.SettingsProblem> getProblems()
meth public abstract org.apache.maven.settings.Proxy getProxy()
meth public abstract org.apache.maven.settings.Server getServer()

CLSS public org.apache.maven.settings.io.xpp3.SettingsXpp3Reader
cons public init()
cons public init(org.apache.maven.settings.io.xpp3.SettingsXpp3Reader$ContentTransformer)
fld public final org.apache.maven.settings.io.xpp3.SettingsXpp3Reader$ContentTransformer contentTransformer
innr public abstract interface static ContentTransformer
meth public boolean getAddDefaultEntities()
meth public org.apache.maven.settings.Settings read(java.io.InputStream) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.settings.Settings read(java.io.InputStream,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.settings.Settings read(java.io.Reader) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.settings.Settings read(java.io.Reader,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public org.apache.maven.settings.Settings read(org.codehaus.plexus.util.xml.pull.XmlPullParser,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public void setAddDefaultEntities(boolean)
supr java.lang.Object
hfds addDefaultEntities

CLSS public abstract interface static org.apache.maven.settings.io.xpp3.SettingsXpp3Reader$ContentTransformer
 outer org.apache.maven.settings.io.xpp3.SettingsXpp3Reader
meth public abstract java.lang.String transform(java.lang.String,java.lang.String)

CLSS public org.apache.maven.settings.io.xpp3.SettingsXpp3Writer
cons public init()
meth public void setFileComment(java.lang.String)
meth public void write(java.io.OutputStream,org.apache.maven.settings.Settings) throws java.io.IOException
meth public void write(java.io.Writer,org.apache.maven.settings.Settings) throws java.io.IOException
supr java.lang.Object
hfds NAMESPACE,fileComment

CLSS public abstract interface org.apache.maven.shared.dependency.graph.DependencyGraphBuilder
meth public abstract org.apache.maven.shared.dependency.graph.DependencyNode buildDependencyGraph(org.apache.maven.project.MavenProject,org.apache.maven.artifact.resolver.filter.ArtifactFilter) throws org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException
meth public abstract org.apache.maven.shared.dependency.graph.DependencyNode buildDependencyGraph(org.apache.maven.project.MavenProject,org.apache.maven.artifact.resolver.filter.ArtifactFilter,java.util.Collection<org.apache.maven.project.MavenProject>) throws org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException

CLSS public org.apache.maven.shared.dependency.graph.DependencyGraphBuilderException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public abstract interface org.apache.maven.shared.dependency.graph.DependencyNode
meth public abstract boolean accept(org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor)
meth public abstract java.lang.String getPremanagedScope()
meth public abstract java.lang.String getPremanagedVersion()
meth public abstract java.lang.String getVersionConstraint()
meth public abstract java.lang.String toNodeString()
meth public abstract java.util.List<org.apache.maven.shared.dependency.graph.DependencyNode> getChildren()
meth public abstract org.apache.maven.artifact.Artifact getArtifact()
meth public abstract org.apache.maven.shared.dependency.graph.DependencyNode getParent()

CLSS public org.apache.maven.shared.dependency.graph.filter.AncestorOrSelfDependencyNodeFilter
cons public init(java.util.List<org.apache.maven.shared.dependency.graph.DependencyNode>)
cons public init(org.apache.maven.shared.dependency.graph.DependencyNode)
intf org.apache.maven.shared.dependency.graph.filter.DependencyNodeFilter
meth public boolean accept(org.apache.maven.shared.dependency.graph.DependencyNode)
supr java.lang.Object
hfds descendantNodes

CLSS public org.apache.maven.shared.dependency.graph.filter.AndDependencyNodeFilter
cons public init(java.util.List<org.apache.maven.shared.dependency.graph.filter.DependencyNodeFilter>)
cons public init(org.apache.maven.shared.dependency.graph.filter.DependencyNodeFilter,org.apache.maven.shared.dependency.graph.filter.DependencyNodeFilter)
intf org.apache.maven.shared.dependency.graph.filter.DependencyNodeFilter
meth public boolean accept(org.apache.maven.shared.dependency.graph.DependencyNode)
meth public java.util.List<org.apache.maven.shared.dependency.graph.filter.DependencyNodeFilter> getDependencyNodeFilters()
supr java.lang.Object
hfds filters

CLSS public org.apache.maven.shared.dependency.graph.filter.ArtifactDependencyNodeFilter
cons public init(org.apache.maven.artifact.resolver.filter.ArtifactFilter)
intf org.apache.maven.shared.dependency.graph.filter.DependencyNodeFilter
meth public boolean accept(org.apache.maven.shared.dependency.graph.DependencyNode)
meth public org.apache.maven.artifact.resolver.filter.ArtifactFilter getArtifactFilter()
supr java.lang.Object
hfds filter

CLSS public abstract interface org.apache.maven.shared.dependency.graph.filter.DependencyNodeFilter
meth public abstract boolean accept(org.apache.maven.shared.dependency.graph.DependencyNode)

CLSS public org.apache.maven.shared.dependency.graph.traversal.BuildingDependencyNodeVisitor
cons public init()
cons public init(org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor)
intf org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor
meth public boolean endVisit(org.apache.maven.shared.dependency.graph.DependencyNode)
meth public boolean visit(org.apache.maven.shared.dependency.graph.DependencyNode)
meth public org.apache.maven.shared.dependency.graph.DependencyNode getDependencyTree()
meth public org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor getDependencyNodeVisitor()
supr java.lang.Object
hfds parentNodes,rootNode,visitor

CLSS public org.apache.maven.shared.dependency.graph.traversal.CollectingDependencyNodeVisitor
cons public init()
intf org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor
meth public boolean endVisit(org.apache.maven.shared.dependency.graph.DependencyNode)
meth public boolean visit(org.apache.maven.shared.dependency.graph.DependencyNode)
meth public java.util.List<org.apache.maven.shared.dependency.graph.DependencyNode> getNodes()
supr java.lang.Object
hfds nodes

CLSS public abstract interface org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor
meth public abstract boolean endVisit(org.apache.maven.shared.dependency.graph.DependencyNode)
meth public abstract boolean visit(org.apache.maven.shared.dependency.graph.DependencyNode)

CLSS public org.apache.maven.shared.dependency.graph.traversal.FilteringDependencyNodeVisitor
cons public init(org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor,org.apache.maven.shared.dependency.graph.filter.DependencyNodeFilter)
intf org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor
meth public boolean endVisit(org.apache.maven.shared.dependency.graph.DependencyNode)
meth public boolean visit(org.apache.maven.shared.dependency.graph.DependencyNode)
meth public org.apache.maven.shared.dependency.graph.filter.DependencyNodeFilter getDependencyNodeFilter()
meth public org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor getDependencyNodeVisitor()
supr java.lang.Object
hfds filter,visitor

CLSS public org.apache.maven.shared.dependency.graph.traversal.SerializingDependencyNodeVisitor
cons public init(java.io.Writer)
cons public init(java.io.Writer,org.apache.maven.shared.dependency.graph.traversal.SerializingDependencyNodeVisitor$GraphTokens)
fld public final static org.apache.maven.shared.dependency.graph.traversal.SerializingDependencyNodeVisitor$GraphTokens EXTENDED_TOKENS
fld public final static org.apache.maven.shared.dependency.graph.traversal.SerializingDependencyNodeVisitor$GraphTokens STANDARD_TOKENS
fld public final static org.apache.maven.shared.dependency.graph.traversal.SerializingDependencyNodeVisitor$GraphTokens WHITESPACE_TOKENS
innr public static GraphTokens
intf org.apache.maven.shared.dependency.graph.traversal.DependencyNodeVisitor
meth public boolean endVisit(org.apache.maven.shared.dependency.graph.DependencyNode)
meth public boolean visit(org.apache.maven.shared.dependency.graph.DependencyNode)
supr java.lang.Object
hfds depth,tokens,writer

CLSS public static org.apache.maven.shared.dependency.graph.traversal.SerializingDependencyNodeVisitor$GraphTokens
 outer org.apache.maven.shared.dependency.graph.traversal.SerializingDependencyNodeVisitor
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getFillIndent(boolean)
meth public java.lang.String getNodeIndent(boolean)
supr java.lang.Object
hfds fillIndent,lastFillIndent,lastNodeIndent,nodeIndent

CLSS public org.apache.maven.shared.dependency.tree.DefaultDependencyTreeBuilder
 anno 0 org.codehaus.plexus.component.annotations.Component(boolean isolatedRealm=false, java.lang.Class<?> role=class org.apache.maven.shared.dependency.tree.DependencyTreeBuilder, java.lang.String alias="", java.lang.String composer="", java.lang.String configurator="", java.lang.String description="", java.lang.String factory="", java.lang.String hint="", java.lang.String instantiationStrategy="", java.lang.String lifecycleHandler="", java.lang.String profile="", java.lang.String type="", java.lang.String version="")
cons public init()
intf org.apache.maven.shared.dependency.tree.DependencyTreeBuilder
meth protected org.apache.maven.artifact.resolver.ArtifactResolutionResult getArtifactResolutionResult()
meth public org.apache.maven.shared.dependency.tree.DependencyNode buildDependencyTree(org.apache.maven.project.MavenProject) throws org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException
meth public org.apache.maven.shared.dependency.tree.DependencyNode buildDependencyTree(org.apache.maven.project.MavenProject,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.factory.ArtifactFactory,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter,org.apache.maven.artifact.resolver.ArtifactCollector) throws org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException
meth public org.apache.maven.shared.dependency.tree.DependencyNode buildDependencyTree(org.apache.maven.project.MavenProject,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.resolver.filter.ArtifactFilter) throws org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException
meth public org.apache.maven.shared.dependency.tree.DependencyTree buildDependencyTree(org.apache.maven.project.MavenProject,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.factory.ArtifactFactory,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.ArtifactCollector) throws org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException
supr org.codehaus.plexus.logging.AbstractLogEnabled
hfds collector,factory,metadataSource,result

CLSS public org.apache.maven.shared.dependency.tree.DependencyNode
cons public init(org.apache.maven.artifact.Artifact)
cons public init(org.apache.maven.artifact.Artifact,int)
cons public init(org.apache.maven.artifact.Artifact,int,org.apache.maven.artifact.Artifact)
fld public final static int INCLUDED = 0
fld public final static int OMITTED_FOR_CONFLICT = 2
fld public final static int OMITTED_FOR_CYCLE = 3
fld public final static int OMITTED_FOR_DUPLICATE = 1
meth public boolean accept(org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor)
meth public boolean equals(java.lang.Object)
meth public boolean hasChildren()
meth public int getDepth()
meth public int getState()
meth public int hashCode()
meth public java.lang.String getFailedUpdateScope()
meth public java.lang.String getOriginalScope()
meth public java.lang.String getPremanagedScope()
meth public java.lang.String getPremanagedVersion()
meth public java.lang.String toNodeString()
meth public java.lang.String toString()
meth public java.lang.String toString(int)
meth public java.util.Iterator<org.apache.maven.shared.dependency.tree.DependencyNode> inverseIterator()
meth public java.util.Iterator<org.apache.maven.shared.dependency.tree.DependencyNode> iterator()
meth public java.util.Iterator<org.apache.maven.shared.dependency.tree.DependencyNode> preorderIterator()
meth public java.util.List<org.apache.maven.artifact.versioning.ArtifactVersion> getAvailableVersions()
meth public java.util.List<org.apache.maven.shared.dependency.tree.DependencyNode> getChildren()
meth public org.apache.maven.artifact.Artifact getArtifact()
meth public org.apache.maven.artifact.Artifact getRelatedArtifact()
meth public org.apache.maven.artifact.versioning.VersionRange getVersionSelectedFromRange()
meth public org.apache.maven.shared.dependency.tree.DependencyNode getParent()
meth public void addChild(org.apache.maven.shared.dependency.tree.DependencyNode)
meth public void omitForConflict(org.apache.maven.artifact.Artifact)
meth public void omitForCycle()
meth public void removeChild(org.apache.maven.shared.dependency.tree.DependencyNode)
meth public void setAvailableVersions(java.util.List<org.apache.maven.artifact.versioning.ArtifactVersion>)
meth public void setFailedUpdateScope(java.lang.String)
meth public void setOriginalScope(java.lang.String)
meth public void setPremanagedScope(java.lang.String)
meth public void setPremanagedVersion(java.lang.String)
meth public void setVersionSelectedFromRange(org.apache.maven.artifact.versioning.VersionRange)
supr java.lang.Object
hfds HASH_PRIME,artifact,availableVersions,children,failedUpdateScope,originalScope,parent,premanagedScope,premanagedVersion,relatedArtifact,state,versionSelectedFromRange
hcls ItemAppender

CLSS public org.apache.maven.shared.dependency.tree.DependencyTree
cons public init(org.apache.maven.shared.dependency.tree.DependencyNode,java.util.Collection<org.apache.maven.shared.dependency.tree.DependencyNode>)
meth public java.lang.String toString()
meth public java.util.Collection<org.apache.maven.shared.dependency.tree.DependencyNode> getNodes()
meth public java.util.Iterator<org.apache.maven.shared.dependency.tree.DependencyNode> inverseIterator()
meth public java.util.Iterator<org.apache.maven.shared.dependency.tree.DependencyNode> iterator()
meth public java.util.Iterator<org.apache.maven.shared.dependency.tree.DependencyNode> preorderIterator()
meth public java.util.List<org.apache.maven.artifact.Artifact> getArtifacts()
meth public org.apache.maven.shared.dependency.tree.DependencyNode getRootNode()
supr java.lang.Object
hfds nodes,rootNode

CLSS public abstract interface org.apache.maven.shared.dependency.tree.DependencyTreeBuilder
fld public final static java.lang.String ROLE
meth public abstract org.apache.maven.shared.dependency.tree.DependencyNode buildDependencyTree(org.apache.maven.project.MavenProject) throws org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException
meth public abstract org.apache.maven.shared.dependency.tree.DependencyNode buildDependencyTree(org.apache.maven.project.MavenProject,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.factory.ArtifactFactory,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.filter.ArtifactFilter,org.apache.maven.artifact.resolver.ArtifactCollector) throws org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException
meth public abstract org.apache.maven.shared.dependency.tree.DependencyNode buildDependencyTree(org.apache.maven.project.MavenProject,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.resolver.filter.ArtifactFilter) throws org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException
meth public abstract org.apache.maven.shared.dependency.tree.DependencyTree buildDependencyTree(org.apache.maven.project.MavenProject,org.apache.maven.artifact.repository.ArtifactRepository,org.apache.maven.artifact.factory.ArtifactFactory,org.apache.maven.artifact.metadata.ArtifactMetadataSource,org.apache.maven.artifact.resolver.ArtifactCollector) throws org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException

CLSS public org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public org.apache.maven.shared.dependency.tree.DependencyTreeInverseIterator
cons public init(org.apache.maven.shared.dependency.tree.DependencyNode)
intf java.util.Iterator<org.apache.maven.shared.dependency.tree.DependencyNode>
meth public boolean hasNext()
meth public org.apache.maven.shared.dependency.tree.DependencyNode next()
meth public void remove()
supr java.lang.Object
hfds nodesToProcess

CLSS public org.apache.maven.shared.dependency.tree.DependencyTreePreorderIterator
cons public init(org.apache.maven.shared.dependency.tree.DependencyNode)
intf java.util.Iterator<org.apache.maven.shared.dependency.tree.DependencyNode>
meth public boolean hasNext()
meth public org.apache.maven.shared.dependency.tree.DependencyNode next()
meth public void remove()
supr java.lang.Object
hfds nodesToProcess

CLSS public org.apache.maven.shared.dependency.tree.DependencyTreeResolutionListener
cons public init(org.codehaus.plexus.logging.Logger)
intf org.apache.maven.artifact.resolver.ResolutionListener
intf org.apache.maven.artifact.resolver.ResolutionListenerForDepMgmt
meth public java.util.Collection<org.apache.maven.shared.dependency.tree.DependencyNode> getNodes()
meth public org.apache.maven.shared.dependency.tree.DependencyNode getRootNode()
meth public void endProcessChildren(org.apache.maven.artifact.Artifact)
meth public void includeArtifact(org.apache.maven.artifact.Artifact)
meth public void manageArtifact(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact)
meth public void manageArtifactScope(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact)
meth public void manageArtifactVersion(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact)
meth public void omitForCycle(org.apache.maven.artifact.Artifact)
meth public void omitForNearer(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact)
meth public void restrictRange(org.apache.maven.artifact.Artifact,org.apache.maven.artifact.Artifact,org.apache.maven.artifact.versioning.VersionRange)
meth public void selectVersionFromRange(org.apache.maven.artifact.Artifact)
meth public void startProcessChildren(org.apache.maven.artifact.Artifact)
meth public void testArtifact(org.apache.maven.artifact.Artifact)
meth public void updateScope(org.apache.maven.artifact.Artifact,java.lang.String)
meth public void updateScopeCurrentPom(org.apache.maven.artifact.Artifact,java.lang.String)
supr java.lang.Object
hfds currentNode,logger,managedScopes,managedVersions,nodesByArtifact,parentNodes,rootNode

CLSS public org.apache.maven.shared.dependency.tree.traversal.BuildingDependencyNodeVisitor
cons public init()
cons public init(org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor)
intf org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor
meth public boolean endVisit(org.apache.maven.shared.dependency.tree.DependencyNode)
meth public boolean visit(org.apache.maven.shared.dependency.tree.DependencyNode)
meth public org.apache.maven.shared.dependency.tree.DependencyNode getDependencyTree()
meth public org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor getDependencyNodeVisitor()
supr java.lang.Object
hfds parentNodes,rootNode,visitor

CLSS public org.apache.maven.shared.dependency.tree.traversal.CollectingDependencyNodeVisitor
cons public init()
intf org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor
meth public boolean endVisit(org.apache.maven.shared.dependency.tree.DependencyNode)
meth public boolean visit(org.apache.maven.shared.dependency.tree.DependencyNode)
meth public java.util.List<org.apache.maven.shared.dependency.tree.DependencyNode> getNodes()
supr java.lang.Object
hfds nodes

CLSS public abstract interface org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor
meth public abstract boolean endVisit(org.apache.maven.shared.dependency.tree.DependencyNode)
meth public abstract boolean visit(org.apache.maven.shared.dependency.tree.DependencyNode)

CLSS public org.apache.maven.shared.dependency.tree.traversal.FilteringDependencyNodeVisitor
cons public init(org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor,org.apache.maven.shared.dependency.tree.filter.DependencyNodeFilter)
intf org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor
meth public boolean endVisit(org.apache.maven.shared.dependency.tree.DependencyNode)
meth public boolean visit(org.apache.maven.shared.dependency.tree.DependencyNode)
meth public org.apache.maven.shared.dependency.tree.filter.DependencyNodeFilter getDependencyNodeFilter()
meth public org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor getDependencyNodeVisitor()
supr java.lang.Object
hfds filter,visitor

CLSS public org.apache.maven.shared.dependency.tree.traversal.SerializingDependencyNodeVisitor
cons public init(java.io.Writer)
cons public init(java.io.Writer,org.apache.maven.shared.dependency.tree.traversal.SerializingDependencyNodeVisitor$TreeTokens)
fld public final static org.apache.maven.shared.dependency.tree.traversal.SerializingDependencyNodeVisitor$TreeTokens EXTENDED_TOKENS
fld public final static org.apache.maven.shared.dependency.tree.traversal.SerializingDependencyNodeVisitor$TreeTokens STANDARD_TOKENS
fld public final static org.apache.maven.shared.dependency.tree.traversal.SerializingDependencyNodeVisitor$TreeTokens WHITESPACE_TOKENS
innr public static TreeTokens
intf org.apache.maven.shared.dependency.tree.traversal.DependencyNodeVisitor
meth public boolean endVisit(org.apache.maven.shared.dependency.tree.DependencyNode)
meth public boolean visit(org.apache.maven.shared.dependency.tree.DependencyNode)
supr java.lang.Object
hfds depth,tokens,writer

CLSS public static org.apache.maven.shared.dependency.tree.traversal.SerializingDependencyNodeVisitor$TreeTokens
 outer org.apache.maven.shared.dependency.tree.traversal.SerializingDependencyNodeVisitor
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getFillIndent(boolean)
meth public java.lang.String getNodeIndent(boolean)
supr java.lang.Object
hfds fillIndent,lastFillIndent,lastNodeIndent,nodeIndent

CLSS public abstract org.apache.maven.wagon.AbstractWagon
cons public init()
fld protected boolean interactive
fld protected final static int BUFFER_SEGMENT_SIZE = 4096
fld protected final static int DEFAULT_BUFFER_SIZE = 4096
fld protected final static int MAXIMUM_BUFFER_SIZE = 524288
fld protected final static int MINIMUM_AMOUNT_OF_TRANSFER_CHUNKS = 100
fld protected org.apache.maven.wagon.authentication.AuthenticationInfo authenticationInfo
fld protected org.apache.maven.wagon.events.SessionEventSupport sessionEventSupport
fld protected org.apache.maven.wagon.events.TransferEventSupport transferEventSupport
fld protected org.apache.maven.wagon.proxy.ProxyInfo proxyInfo
fld protected org.apache.maven.wagon.repository.Repository repository
intf org.apache.maven.wagon.Wagon
meth protected abstract void closeConnection() throws org.apache.maven.wagon.ConnectionException
meth protected abstract void openConnectionInternal() throws org.apache.maven.wagon.ConnectionException,org.apache.maven.wagon.authentication.AuthenticationException
meth protected int getBufferCapacityForTransfer(long)
meth protected org.apache.maven.wagon.proxy.ProxyInfo getProxyInfo(java.lang.String,java.lang.String)
meth protected static java.lang.String getPath(java.lang.String,java.lang.String)
meth protected void cleanupGetTransfer(org.apache.maven.wagon.resource.Resource)
meth protected void cleanupPutTransfer(org.apache.maven.wagon.resource.Resource)
meth protected void createParentDirectories(java.io.File) throws org.apache.maven.wagon.TransferFailedException
meth protected void finishGetTransfer(org.apache.maven.wagon.resource.Resource,java.io.InputStream,java.io.OutputStream) throws org.apache.maven.wagon.TransferFailedException
meth protected void finishPutTransfer(org.apache.maven.wagon.resource.Resource,java.io.InputStream,java.io.OutputStream) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth protected void fireGetCompleted(org.apache.maven.wagon.resource.Resource,java.io.File)
meth protected void fireGetInitiated(org.apache.maven.wagon.resource.Resource,java.io.File)
meth protected void fireGetStarted(org.apache.maven.wagon.resource.Resource,java.io.File)
meth protected void firePutCompleted(org.apache.maven.wagon.resource.Resource,java.io.File)
meth protected void firePutInitiated(org.apache.maven.wagon.resource.Resource,java.io.File)
meth protected void firePutStarted(org.apache.maven.wagon.resource.Resource,java.io.File)
meth protected void fireSessionConnectionRefused()
meth protected void fireSessionDebug(java.lang.String)
meth protected void fireSessionDisconnected()
meth protected void fireSessionDisconnecting()
meth protected void fireSessionError(java.lang.Exception)
meth protected void fireSessionLoggedIn()
meth protected void fireSessionLoggedOff()
meth protected void fireSessionOpened()
meth protected void fireSessionOpening()
meth protected void fireTransferDebug(java.lang.String)
meth protected void fireTransferError(org.apache.maven.wagon.resource.Resource,java.lang.Exception,int)
meth protected void fireTransferProgress(org.apache.maven.wagon.events.TransferEvent,byte[],int)
meth protected void getTransfer(org.apache.maven.wagon.resource.Resource,java.io.File,java.io.InputStream) throws org.apache.maven.wagon.TransferFailedException
meth protected void getTransfer(org.apache.maven.wagon.resource.Resource,java.io.File,java.io.InputStream,boolean,int) throws org.apache.maven.wagon.TransferFailedException
 anno 0 java.lang.Deprecated()
meth protected void getTransfer(org.apache.maven.wagon.resource.Resource,java.io.File,java.io.InputStream,boolean,long) throws org.apache.maven.wagon.TransferFailedException
meth protected void getTransfer(org.apache.maven.wagon.resource.Resource,java.io.OutputStream,java.io.InputStream) throws org.apache.maven.wagon.TransferFailedException
meth protected void getTransfer(org.apache.maven.wagon.resource.Resource,java.io.OutputStream,java.io.InputStream,boolean,int) throws org.apache.maven.wagon.TransferFailedException
 anno 0 java.lang.Deprecated()
meth protected void getTransfer(org.apache.maven.wagon.resource.Resource,java.io.OutputStream,java.io.InputStream,boolean,long) throws org.apache.maven.wagon.TransferFailedException
meth protected void postProcessListeners(org.apache.maven.wagon.resource.Resource,java.io.File,int) throws org.apache.maven.wagon.TransferFailedException
meth protected void putTransfer(org.apache.maven.wagon.resource.Resource,java.io.File,java.io.OutputStream,boolean) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth protected void putTransfer(org.apache.maven.wagon.resource.Resource,java.io.InputStream,java.io.OutputStream,boolean) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth protected void transfer(org.apache.maven.wagon.resource.Resource,java.io.File,java.io.OutputStream,boolean) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth protected void transfer(org.apache.maven.wagon.resource.Resource,java.io.InputStream,java.io.OutputStream,int) throws java.io.IOException
meth protected void transfer(org.apache.maven.wagon.resource.Resource,java.io.InputStream,java.io.OutputStream,int,int) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth protected void transfer(org.apache.maven.wagon.resource.Resource,java.io.InputStream,java.io.OutputStream,int,long) throws java.io.IOException
meth public boolean hasSessionListener(org.apache.maven.wagon.events.SessionListener)
meth public boolean hasTransferListener(org.apache.maven.wagon.events.TransferListener)
meth public boolean isInteractive()
meth public boolean resourceExists(java.lang.String) throws org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public boolean supportsDirectoryCopy()
meth public int getReadTimeout()
meth public int getTimeout()
meth public java.util.List<java.lang.String> getFileList(java.lang.String) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public org.apache.maven.wagon.authentication.AuthenticationInfo getAuthenticationInfo()
meth public org.apache.maven.wagon.events.SessionEventSupport getSessionEventSupport()
meth public org.apache.maven.wagon.events.TransferEventSupport getTransferEventSupport()
meth public org.apache.maven.wagon.proxy.ProxyInfo getProxyInfo()
meth public org.apache.maven.wagon.repository.Repository getRepository()
meth public org.apache.maven.wagon.repository.RepositoryPermissions getPermissionsOverride()
meth public void addSessionListener(org.apache.maven.wagon.events.SessionListener)
meth public void addTransferListener(org.apache.maven.wagon.events.TransferListener)
meth public void connect(org.apache.maven.wagon.repository.Repository) throws org.apache.maven.wagon.ConnectionException,org.apache.maven.wagon.authentication.AuthenticationException
meth public void connect(org.apache.maven.wagon.repository.Repository,org.apache.maven.wagon.authentication.AuthenticationInfo) throws org.apache.maven.wagon.ConnectionException,org.apache.maven.wagon.authentication.AuthenticationException
meth public void connect(org.apache.maven.wagon.repository.Repository,org.apache.maven.wagon.authentication.AuthenticationInfo,org.apache.maven.wagon.proxy.ProxyInfo) throws org.apache.maven.wagon.ConnectionException,org.apache.maven.wagon.authentication.AuthenticationException
meth public void connect(org.apache.maven.wagon.repository.Repository,org.apache.maven.wagon.authentication.AuthenticationInfo,org.apache.maven.wagon.proxy.ProxyInfoProvider) throws org.apache.maven.wagon.ConnectionException,org.apache.maven.wagon.authentication.AuthenticationException
meth public void connect(org.apache.maven.wagon.repository.Repository,org.apache.maven.wagon.proxy.ProxyInfo) throws org.apache.maven.wagon.ConnectionException,org.apache.maven.wagon.authentication.AuthenticationException
meth public void connect(org.apache.maven.wagon.repository.Repository,org.apache.maven.wagon.proxy.ProxyInfoProvider) throws org.apache.maven.wagon.ConnectionException,org.apache.maven.wagon.authentication.AuthenticationException
meth public void disconnect() throws org.apache.maven.wagon.ConnectionException
meth public void openConnection() throws org.apache.maven.wagon.ConnectionException,org.apache.maven.wagon.authentication.AuthenticationException
meth public void putDirectory(java.io.File,java.lang.String) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public void removeSessionListener(org.apache.maven.wagon.events.SessionListener)
meth public void removeTransferListener(org.apache.maven.wagon.events.TransferListener)
meth public void setInteractive(boolean)
meth public void setPermissionsOverride(org.apache.maven.wagon.repository.RepositoryPermissions)
meth public void setReadTimeout(int)
meth public void setSessionEventSupport(org.apache.maven.wagon.events.SessionEventSupport)
meth public void setTimeout(int)
meth public void setTransferEventSupport(org.apache.maven.wagon.events.TransferEventSupport)
supr java.lang.Object
hfds connectionTimeout,permissionsOverride,proxyInfoProvider,readTimeout

CLSS public org.apache.maven.wagon.CommandExecutionException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr org.apache.maven.wagon.WagonException

CLSS public abstract interface org.apache.maven.wagon.CommandExecutor
fld public final static java.lang.String ROLE
intf org.apache.maven.wagon.Wagon
meth public abstract org.apache.maven.wagon.Streams executeCommand(java.lang.String,boolean) throws org.apache.maven.wagon.CommandExecutionException
meth public abstract void executeCommand(java.lang.String) throws org.apache.maven.wagon.CommandExecutionException

CLSS public org.apache.maven.wagon.ConnectionException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr org.apache.maven.wagon.WagonException

CLSS public org.apache.maven.wagon.InputData
cons public init()
meth public java.io.InputStream getInputStream()
meth public org.apache.maven.wagon.resource.Resource getResource()
meth public void setInputStream(java.io.InputStream)
meth public void setResource(org.apache.maven.wagon.resource.Resource)
supr java.lang.Object
hfds inputStream,resource

CLSS public org.apache.maven.wagon.LazyFileOutputStream
cons public init(java.io.File)
cons public init(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.io.FileDescriptor getFD() throws java.io.IOException
meth public java.lang.String toString()
meth public java.nio.channels.FileChannel getChannel()
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream
hfds delegee,file

CLSS public org.apache.maven.wagon.OutputData
cons public init()
meth public java.io.OutputStream getOutputStream()
meth public org.apache.maven.wagon.resource.Resource getResource()
meth public void setOutputStream(java.io.OutputStream)
meth public void setResource(org.apache.maven.wagon.resource.Resource)
supr java.lang.Object
hfds outputStream,resource

CLSS public final org.apache.maven.wagon.PathUtils
meth public static int port(java.lang.String)
meth public static java.lang.String basedir(java.lang.String)
meth public static java.lang.String dirname(java.lang.String)
meth public static java.lang.String filename(java.lang.String)
meth public static java.lang.String host(java.lang.String)
meth public static java.lang.String password(java.lang.String)
meth public static java.lang.String protocol(java.lang.String)
meth public static java.lang.String toRelative(java.io.File,java.lang.String)
meth public static java.lang.String user(java.lang.String)
meth public static java.lang.String[] dirnames(java.lang.String)
supr java.lang.Object

CLSS public final org.apache.maven.wagon.PermissionModeUtils
meth public static java.lang.String getUserMaskFor(java.lang.String)
supr java.lang.Object

CLSS public org.apache.maven.wagon.ResourceDoesNotExistException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr org.apache.maven.wagon.WagonException

CLSS public abstract org.apache.maven.wagon.StreamWagon
cons public init()
intf org.apache.maven.wagon.StreamingWagon
meth protected java.io.InputStream getInputStream(org.apache.maven.wagon.resource.Resource) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth protected java.io.OutputStream getOutputStream(org.apache.maven.wagon.resource.Resource) throws org.apache.maven.wagon.TransferFailedException
meth protected void checkInputStream(java.io.InputStream,org.apache.maven.wagon.resource.Resource) throws org.apache.maven.wagon.TransferFailedException
meth protected void checkOutputStream(org.apache.maven.wagon.resource.Resource,java.io.OutputStream) throws org.apache.maven.wagon.TransferFailedException
meth protected void putFromStream(java.io.InputStream,org.apache.maven.wagon.resource.Resource) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public abstract void closeConnection() throws org.apache.maven.wagon.ConnectionException
meth public abstract void fillInputData(org.apache.maven.wagon.InputData) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public abstract void fillOutputData(org.apache.maven.wagon.OutputData) throws org.apache.maven.wagon.TransferFailedException
meth public boolean getIfNewer(java.lang.String,java.io.File,long) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public boolean getIfNewerToStream(java.lang.String,java.io.OutputStream,long) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public void get(java.lang.String,java.io.File) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public void getToStream(java.lang.String,java.io.OutputStream) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public void put(java.io.File,java.lang.String) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public void putFromStream(java.io.InputStream,java.lang.String) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public void putFromStream(java.io.InputStream,java.lang.String,long,long) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
supr org.apache.maven.wagon.AbstractWagon

CLSS public abstract interface org.apache.maven.wagon.StreamingWagon
intf org.apache.maven.wagon.Wagon
meth public abstract boolean getIfNewerToStream(java.lang.String,java.io.OutputStream,long) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public abstract void getToStream(java.lang.String,java.io.OutputStream) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public abstract void putFromStream(java.io.InputStream,java.lang.String) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public abstract void putFromStream(java.io.InputStream,java.lang.String,long,long) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException

CLSS public org.apache.maven.wagon.Streams
cons public init()
meth public java.lang.String getErr()
meth public java.lang.String getOut()
meth public void setErr(java.lang.String)
meth public void setOut(java.lang.String)
supr java.lang.Object
hfds err,out

CLSS public org.apache.maven.wagon.TransferFailedException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr org.apache.maven.wagon.WagonException

CLSS public org.apache.maven.wagon.UnsupportedProtocolException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr org.apache.maven.wagon.WagonException

CLSS public abstract interface org.apache.maven.wagon.Wagon
fld public final static int DEFAULT_CONNECTION_TIMEOUT = 60000
fld public final static int DEFAULT_READ_TIMEOUT = 1800000
fld public final static java.lang.String ROLE
meth public abstract boolean getIfNewer(java.lang.String,java.io.File,long) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public abstract boolean hasSessionListener(org.apache.maven.wagon.events.SessionListener)
meth public abstract boolean hasTransferListener(org.apache.maven.wagon.events.TransferListener)
meth public abstract boolean isInteractive()
meth public abstract boolean resourceExists(java.lang.String) throws org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public abstract boolean supportsDirectoryCopy()
meth public abstract int getReadTimeout()
meth public abstract int getTimeout()
meth public abstract java.util.List<java.lang.String> getFileList(java.lang.String) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public abstract org.apache.maven.wagon.repository.Repository getRepository()
meth public abstract void addSessionListener(org.apache.maven.wagon.events.SessionListener)
meth public abstract void addTransferListener(org.apache.maven.wagon.events.TransferListener)
meth public abstract void connect(org.apache.maven.wagon.repository.Repository) throws org.apache.maven.wagon.ConnectionException,org.apache.maven.wagon.authentication.AuthenticationException
meth public abstract void connect(org.apache.maven.wagon.repository.Repository,org.apache.maven.wagon.authentication.AuthenticationInfo) throws org.apache.maven.wagon.ConnectionException,org.apache.maven.wagon.authentication.AuthenticationException
meth public abstract void connect(org.apache.maven.wagon.repository.Repository,org.apache.maven.wagon.authentication.AuthenticationInfo,org.apache.maven.wagon.proxy.ProxyInfo) throws org.apache.maven.wagon.ConnectionException,org.apache.maven.wagon.authentication.AuthenticationException
meth public abstract void connect(org.apache.maven.wagon.repository.Repository,org.apache.maven.wagon.authentication.AuthenticationInfo,org.apache.maven.wagon.proxy.ProxyInfoProvider) throws org.apache.maven.wagon.ConnectionException,org.apache.maven.wagon.authentication.AuthenticationException
meth public abstract void connect(org.apache.maven.wagon.repository.Repository,org.apache.maven.wagon.proxy.ProxyInfo) throws org.apache.maven.wagon.ConnectionException,org.apache.maven.wagon.authentication.AuthenticationException
meth public abstract void connect(org.apache.maven.wagon.repository.Repository,org.apache.maven.wagon.proxy.ProxyInfoProvider) throws org.apache.maven.wagon.ConnectionException,org.apache.maven.wagon.authentication.AuthenticationException
meth public abstract void disconnect() throws org.apache.maven.wagon.ConnectionException
meth public abstract void get(java.lang.String,java.io.File) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public abstract void openConnection() throws org.apache.maven.wagon.ConnectionException,org.apache.maven.wagon.authentication.AuthenticationException
meth public abstract void put(java.io.File,java.lang.String) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public abstract void putDirectory(java.io.File,java.lang.String) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public abstract void removeSessionListener(org.apache.maven.wagon.events.SessionListener)
meth public abstract void removeTransferListener(org.apache.maven.wagon.events.TransferListener)
meth public abstract void setInteractive(boolean)
meth public abstract void setReadTimeout(int)
meth public abstract void setTimeout(int)

CLSS public final org.apache.maven.wagon.WagonConstants
cons public init()
fld public final static int UNKNOWN_LENGTH = -1
fld public final static int UNKNOWN_PORT = -1
supr java.lang.Object

CLSS public abstract org.apache.maven.wagon.WagonException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
supr java.lang.Exception
hfds cause

CLSS public final org.apache.maven.wagon.WagonUtils
meth public static java.lang.String toString(java.lang.String,org.apache.maven.wagon.Wagon) throws java.io.IOException,org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public static void putDirectory(java.io.File,org.apache.maven.wagon.Wagon,boolean) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
supr java.lang.Object

CLSS public org.apache.maven.wagon.authentication.AuthenticationException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr org.apache.maven.wagon.WagonException

CLSS public org.apache.maven.wagon.authentication.AuthenticationInfo
cons public init()
intf java.io.Serializable
meth public java.lang.String getPassphrase()
meth public java.lang.String getPassword()
meth public java.lang.String getPrivateKey()
meth public java.lang.String getUserName()
meth public void setPassphrase(java.lang.String)
meth public void setPassword(java.lang.String)
meth public void setPrivateKey(java.lang.String)
meth public void setUserName(java.lang.String)
supr java.lang.Object
hfds passphrase,password,privateKey,userName

CLSS public org.apache.maven.wagon.authorization.AuthorizationException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr org.apache.maven.wagon.WagonException

CLSS public org.apache.maven.wagon.events.SessionEvent
cons public init(org.apache.maven.wagon.Wagon,int)
cons public init(org.apache.maven.wagon.Wagon,java.lang.Exception)
fld public final static int SESSION_CLOSED = 1
fld public final static int SESSION_CONNECTION_REFUSED = 4
fld public final static int SESSION_DISCONNECTED = 3
fld public final static int SESSION_DISCONNECTING = 2
fld public final static int SESSION_ERROR_OCCURRED = 9
fld public final static int SESSION_LOGGED_IN = 7
fld public final static int SESSION_LOGGED_OFF = 8
fld public final static int SESSION_OPENED = 6
fld public final static int SESSION_OPENING = 5
meth public int getEventType()
meth public java.lang.Exception getException()
meth public java.lang.String toString()
meth public void setEventType(int)
meth public void setException(java.lang.Exception)
supr org.apache.maven.wagon.events.WagonEvent
hfds eventType,exception

CLSS public final org.apache.maven.wagon.events.SessionEventSupport
cons public init()
meth public boolean hasSessionListener(org.apache.maven.wagon.events.SessionListener)
meth public void addSessionListener(org.apache.maven.wagon.events.SessionListener)
meth public void fireDebug(java.lang.String)
meth public void fireSessionConnectionRefused(org.apache.maven.wagon.events.SessionEvent)
meth public void fireSessionDisconnected(org.apache.maven.wagon.events.SessionEvent)
meth public void fireSessionDisconnecting(org.apache.maven.wagon.events.SessionEvent)
meth public void fireSessionError(org.apache.maven.wagon.events.SessionEvent)
meth public void fireSessionLoggedIn(org.apache.maven.wagon.events.SessionEvent)
meth public void fireSessionLoggedOff(org.apache.maven.wagon.events.SessionEvent)
meth public void fireSessionOpened(org.apache.maven.wagon.events.SessionEvent)
meth public void fireSessionOpening(org.apache.maven.wagon.events.SessionEvent)
meth public void removeSessionListener(org.apache.maven.wagon.events.SessionListener)
supr java.lang.Object
hfds listeners

CLSS public abstract interface org.apache.maven.wagon.events.SessionListener
meth public abstract void debug(java.lang.String)
meth public abstract void sessionConnectionRefused(org.apache.maven.wagon.events.SessionEvent)
meth public abstract void sessionDisconnected(org.apache.maven.wagon.events.SessionEvent)
meth public abstract void sessionDisconnecting(org.apache.maven.wagon.events.SessionEvent)
meth public abstract void sessionError(org.apache.maven.wagon.events.SessionEvent)
meth public abstract void sessionLoggedIn(org.apache.maven.wagon.events.SessionEvent)
meth public abstract void sessionLoggedOff(org.apache.maven.wagon.events.SessionEvent)
meth public abstract void sessionOpened(org.apache.maven.wagon.events.SessionEvent)
meth public abstract void sessionOpening(org.apache.maven.wagon.events.SessionEvent)

CLSS public org.apache.maven.wagon.events.TransferEvent
cons public init(org.apache.maven.wagon.Wagon,org.apache.maven.wagon.resource.Resource,int,int)
cons public init(org.apache.maven.wagon.Wagon,org.apache.maven.wagon.resource.Resource,java.lang.Exception,int)
fld public final static int REQUEST_GET = 5
fld public final static int REQUEST_PUT = 6
fld public final static int TRANSFER_COMPLETED = 2
fld public final static int TRANSFER_ERROR = 4
fld public final static int TRANSFER_INITIATED = 0
fld public final static int TRANSFER_PROGRESS = 3
fld public final static int TRANSFER_STARTED = 1
meth public boolean equals(java.lang.Object)
meth public int getEventType()
meth public int getRequestType()
meth public int hashCode()
meth public java.io.File getLocalFile()
meth public java.lang.Exception getException()
meth public java.lang.String toString()
meth public org.apache.maven.wagon.resource.Resource getResource()
meth public void setEventType(int)
meth public void setLocalFile(java.io.File)
meth public void setRequestType(int)
meth public void setResource(org.apache.maven.wagon.resource.Resource)
supr org.apache.maven.wagon.events.WagonEvent
hfds eventType,exception,localFile,requestType,resource

CLSS public final org.apache.maven.wagon.events.TransferEventSupport
cons public init()
meth public boolean hasTransferListener(org.apache.maven.wagon.events.TransferListener)
meth public void addTransferListener(org.apache.maven.wagon.events.TransferListener)
meth public void fireDebug(java.lang.String)
meth public void fireTransferCompleted(org.apache.maven.wagon.events.TransferEvent)
meth public void fireTransferError(org.apache.maven.wagon.events.TransferEvent)
meth public void fireTransferInitiated(org.apache.maven.wagon.events.TransferEvent)
meth public void fireTransferProgress(org.apache.maven.wagon.events.TransferEvent,byte[],int)
meth public void fireTransferStarted(org.apache.maven.wagon.events.TransferEvent)
meth public void removeTransferListener(org.apache.maven.wagon.events.TransferListener)
supr java.lang.Object
hfds listeners

CLSS public abstract interface org.apache.maven.wagon.events.TransferListener
meth public abstract void debug(java.lang.String)
meth public abstract void transferCompleted(org.apache.maven.wagon.events.TransferEvent)
meth public abstract void transferError(org.apache.maven.wagon.events.TransferEvent)
meth public abstract void transferInitiated(org.apache.maven.wagon.events.TransferEvent)
meth public abstract void transferProgress(org.apache.maven.wagon.events.TransferEvent,byte[],int)
meth public abstract void transferStarted(org.apache.maven.wagon.events.TransferEvent)

CLSS public org.apache.maven.wagon.events.WagonEvent
cons public init(org.apache.maven.wagon.Wagon)
fld protected long timestamp
meth public long getTimestamp()
meth public org.apache.maven.wagon.Wagon getWagon()
meth public void setTimestamp(long)
supr java.util.EventObject

CLSS public org.apache.maven.wagon.providers.file.FileWagon
cons public init()
meth protected void openConnectionInternal() throws org.apache.maven.wagon.ConnectionException
meth public boolean resourceExists(java.lang.String) throws org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public boolean supportsDirectoryCopy()
meth public java.util.List<java.lang.String> getFileList(java.lang.String) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public void closeConnection()
meth public void fillInputData(org.apache.maven.wagon.InputData) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException
meth public void fillOutputData(org.apache.maven.wagon.OutputData) throws org.apache.maven.wagon.TransferFailedException
meth public void putDirectory(java.io.File,java.lang.String) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
supr org.apache.maven.wagon.StreamWagon

CLSS public org.apache.maven.wagon.providers.http.HttpWagon
cons public init()
supr org.apache.maven.wagon.shared.http.AbstractHttpClientWagon

CLSS public org.apache.maven.wagon.proxy.ProxyInfo
cons public init()
fld public final static java.lang.String PROXY_HTTP = "HTTP"
fld public final static java.lang.String PROXY_SOCKS4 = "SOCKS4"
fld public final static java.lang.String PROXY_SOCKS5 = "SOCKS_5"
intf java.io.Serializable
meth public int getPort()
meth public java.lang.String getHost()
meth public java.lang.String getNonProxyHosts()
meth public java.lang.String getNtlmDomain()
meth public java.lang.String getNtlmHost()
meth public java.lang.String getPassword()
meth public java.lang.String getType()
meth public java.lang.String getUserName()
meth public java.lang.String toString()
meth public void setHost(java.lang.String)
meth public void setNonProxyHosts(java.lang.String)
meth public void setNtlmDomain(java.lang.String)
meth public void setNtlmHost(java.lang.String)
meth public void setPassword(java.lang.String)
meth public void setPort(int)
meth public void setType(java.lang.String)
meth public void setUserName(java.lang.String)
supr java.lang.Object
hfds host,nonProxyHosts,ntlmDomain,ntlmHost,password,port,type,userName

CLSS public abstract interface org.apache.maven.wagon.proxy.ProxyInfoProvider
meth public abstract org.apache.maven.wagon.proxy.ProxyInfo getProxyInfo(java.lang.String)

CLSS public final org.apache.maven.wagon.proxy.ProxyUtils
meth public static boolean validateNonProxyHosts(org.apache.maven.wagon.proxy.ProxyInfo,java.lang.String)
supr java.lang.Object

CLSS public org.apache.maven.wagon.repository.Repository
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public int getPort()
meth public int hashCode()
meth public java.lang.String getBasedir()
meth public java.lang.String getHost()
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public java.lang.String getParameter(java.lang.String)
meth public java.lang.String getPassword()
meth public java.lang.String getProtocol()
meth public java.lang.String getUrl()
meth public java.lang.String getUsername()
meth public java.lang.String toString()
meth public org.apache.maven.wagon.repository.RepositoryPermissions getPermissions()
meth public void setBasedir(java.lang.String)
meth public void setId(java.lang.String)
meth public void setName(java.lang.String)
meth public void setParameters(java.util.Properties)
meth public void setPermissions(org.apache.maven.wagon.repository.RepositoryPermissions)
meth public void setPort(int)
meth public void setProtocol(java.lang.String)
meth public void setUrl(java.lang.String)
supr java.lang.Object
hfds basedir,host,id,name,parameters,password,permissions,port,protocol,serialVersionUID,url,username

CLSS public org.apache.maven.wagon.repository.RepositoryPermissions
cons public init()
intf java.io.Serializable
meth public java.lang.String getDirectoryMode()
meth public java.lang.String getFileMode()
meth public java.lang.String getGroup()
meth public void setDirectoryMode(java.lang.String)
meth public void setFileMode(java.lang.String)
meth public void setGroup(java.lang.String)
supr java.lang.Object
hfds directoryMode,fileMode,group

CLSS public org.apache.maven.wagon.resource.Resource
cons public init()
cons public init(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String inspect()
meth public java.lang.String toString()
meth public long getContentLength()
meth public long getLastModified()
meth public void setContentLength(long)
meth public void setLastModified(long)
meth public void setName(java.lang.String)
supr java.lang.Object
hfds contentLength,lastModified,name

CLSS public abstract org.apache.maven.wagon.shared.http.AbstractHttpClientWagon
cons public init()
fld protected final static int SC_TOO_MANY_REQUESTS = 429
meth protected int backoff(int,java.lang.String) throws java.lang.InterruptedException,org.apache.maven.wagon.TransferFailedException
meth protected java.lang.String getURL(org.apache.maven.wagon.repository.Repository)
meth protected java.lang.String getUserAgent(org.apache.http.client.methods.HttpUriRequest)
meth protected org.apache.http.client.AuthCache getAuthCache()
meth protected org.apache.http.client.CredentialsProvider getCredentialsProvider()
meth protected org.apache.http.client.methods.CloseableHttpResponse execute(org.apache.http.client.methods.HttpUriRequest) throws java.io.IOException,org.apache.http.HttpException
meth protected void cleanupGetTransfer(org.apache.maven.wagon.resource.Resource)
meth protected void mkdirs(java.lang.String) throws java.io.IOException,org.apache.http.HttpException
meth protected void putFromStream(java.io.InputStream,org.apache.maven.wagon.resource.Resource) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public boolean resourceExists(java.lang.String) throws org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public int getInitialBackoffSeconds()
meth public java.util.Properties getHttpHeaders()
meth public org.apache.maven.wagon.shared.http.BasicAuthScope getBasicAuthScope()
meth public org.apache.maven.wagon.shared.http.BasicAuthScope getProxyBasicAuthScope()
meth public org.apache.maven.wagon.shared.http.HttpConfiguration getHttpConfiguration()
meth public static int getMaxBackoffWaitSeconds()
meth public static org.apache.http.impl.client.CloseableHttpClient getHttpClient()
meth public static void setPersistentPool(boolean)
meth public static void setPoolingHttpClientConnectionManager(org.apache.http.impl.conn.PoolingHttpClientConnectionManager)
meth public void closeConnection()
meth public void fillInputData(org.apache.maven.wagon.InputData) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public void fillOutputData(org.apache.maven.wagon.OutputData) throws org.apache.maven.wagon.TransferFailedException
meth public void openConnectionInternal()
meth public void put(java.io.File,java.lang.String) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public void putFromStream(java.io.InputStream,java.lang.String) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public void putFromStream(java.io.InputStream,java.lang.String,long,long) throws org.apache.maven.wagon.ResourceDoesNotExistException,org.apache.maven.wagon.TransferFailedException,org.apache.maven.wagon.authorization.AuthorizationException
meth public void setBasicAuthScope(org.apache.maven.wagon.shared.http.BasicAuthScope)
meth public void setHeaders(org.apache.http.client.methods.HttpUriRequest)
meth public void setHttpConfiguration(org.apache.maven.wagon.shared.http.HttpConfiguration)
meth public void setHttpHeaders(java.util.Properties)
meth public void setInitialBackoffSeconds(int)
meth public void setProxyBasicAuthScope(org.apache.maven.wagon.shared.http.BasicAuthScope)
supr org.apache.maven.wagon.StreamWagon
hfds CONN_TTL,GMT_TIME_ZONE,IGNORE_SSL_VALIDITY_DATES,MAX_BACKOFF_WAIT_SECONDS,MAX_CONN_PER_ROUTE,MAX_CONN_TOTAL,RETRY_HANDLER_CLASS,RETRY_HANDLER_COUNT,RETRY_HANDLER_EXCEPTIONS,RETRY_HANDLER_REQUEST_SENT_ENABLED,SERVICE_UNAVAILABLE_RETRY_STRATEGY_CLASS,SERVICE_UNAVAILABLE_RETRY_STRATEGY_MAX_RETRIES,SERVICE_UNAVAILABLE_RETRY_STRATEGY_RETRY_INTERVAL,SSL_ALLOW_ALL,SSL_INSECURE,authCache,basicAuth,closeable,credentialsProvider,httpClient,httpClientConnectionManager,httpConfiguration,httpHeaders,initialBackoffSeconds,persistentPool,proxyAuth
hcls WagonHttpEntity

CLSS public org.apache.maven.wagon.shared.http.BasicAuthScope
cons public init()
meth public java.lang.String getHost()
meth public java.lang.String getPort()
meth public java.lang.String getRealm()
meth public org.apache.http.auth.AuthScope getScope(java.lang.String,int)
meth public org.apache.http.auth.AuthScope getScope(org.apache.http.HttpHost)
meth public void setHost(java.lang.String)
meth public void setPort(java.lang.String)
meth public void setRealm(java.lang.String)
supr java.lang.Object
hfds host,port,realm

CLSS public org.apache.maven.wagon.shared.http.ConfigurationUtils
cons public init()
meth public static org.apache.http.Header[] asRequestHeaders(org.apache.maven.wagon.shared.http.HttpMethodConfiguration)
meth public static org.apache.maven.wagon.shared.http.HttpMethodConfiguration merge(org.apache.maven.wagon.shared.http.HttpMethodConfiguration,org.apache.maven.wagon.shared.http.HttpMethodConfiguration)
meth public static org.apache.maven.wagon.shared.http.HttpMethodConfiguration merge(org.apache.maven.wagon.shared.http.HttpMethodConfiguration,org.apache.maven.wagon.shared.http.HttpMethodConfiguration,org.apache.maven.wagon.shared.http.HttpMethodConfiguration)
meth public static void copyConfig(org.apache.maven.wagon.shared.http.HttpMethodConfiguration,org.apache.http.client.config.RequestConfig$Builder)
supr java.lang.Object
hfds ALLOW_CIRCULAR_REDIRECTS,COERCE_PATTERN,CONNECTION_TIMEOUT,CONN_MANAGER_TIMEOUT,COOKIE_POLICY,DEFAULT_PROXY,HANDLE_AUTHENTICATION,HANDLE_CONTENT_COMPRESSION,HANDLE_REDIRECTS,HANDLE_URI_NORMALIZATION,LOCAL_ADDRESS,MAX_REDIRECTS,PROXY_AUTH_PREF,REJECT_RELATIVE_REDIRECT,SO_TIMEOUT,STALE_CONNECTION_CHECK,TARGET_AUTH_PREF,USE_EXPECT_CONTINUE

CLSS public org.apache.maven.wagon.shared.http.EncodingUtil
cons public init()
meth public !varargs static java.lang.String encodeURLToString(java.lang.String,java.lang.String[])
meth public static java.lang.String encodeURLToString(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String encodeURLToString(java.lang.String,java.lang.String)
meth public static java.net.URI encodeURL(java.lang.String) throws java.net.MalformedURLException,java.net.URISyntaxException
 anno 0 java.lang.Deprecated()
supr java.lang.Object

CLSS public org.apache.maven.wagon.shared.http.HttpConfiguration
cons public init()
meth public org.apache.maven.wagon.shared.http.HttpConfiguration setAll(org.apache.maven.wagon.shared.http.HttpMethodConfiguration)
meth public org.apache.maven.wagon.shared.http.HttpConfiguration setGet(org.apache.maven.wagon.shared.http.HttpMethodConfiguration)
meth public org.apache.maven.wagon.shared.http.HttpConfiguration setHead(org.apache.maven.wagon.shared.http.HttpMethodConfiguration)
meth public org.apache.maven.wagon.shared.http.HttpConfiguration setMkcol(org.apache.maven.wagon.shared.http.HttpMethodConfiguration)
meth public org.apache.maven.wagon.shared.http.HttpConfiguration setPut(org.apache.maven.wagon.shared.http.HttpMethodConfiguration)
meth public org.apache.maven.wagon.shared.http.HttpMethodConfiguration getAll()
meth public org.apache.maven.wagon.shared.http.HttpMethodConfiguration getGet()
meth public org.apache.maven.wagon.shared.http.HttpMethodConfiguration getHead()
meth public org.apache.maven.wagon.shared.http.HttpMethodConfiguration getMethodConfiguration(org.apache.http.client.methods.HttpUriRequest)
meth public org.apache.maven.wagon.shared.http.HttpMethodConfiguration getMkcol()
meth public org.apache.maven.wagon.shared.http.HttpMethodConfiguration getPut()
supr java.lang.Object
hfds all,get,head,mkcol,put

CLSS public org.apache.maven.wagon.shared.http.HttpMessageUtils
cons public init()
fld public final static int UNKNOWN_STATUS_CODE = -1
meth public static java.lang.String formatAuthorizationMessage(java.lang.String,int,java.lang.String,org.apache.maven.wagon.proxy.ProxyInfo)
meth public static java.lang.String formatResourceDoesNotExistMessage(java.lang.String,int,java.lang.String,org.apache.maven.wagon.proxy.ProxyInfo)
meth public static java.lang.String formatTransferDebugMessage(java.lang.String,int,java.lang.String,org.apache.maven.wagon.proxy.ProxyInfo)
meth public static java.lang.String formatTransferFailedMessage(java.lang.String,int,java.lang.String,org.apache.maven.wagon.proxy.ProxyInfo)
meth public static java.lang.String formatTransferFailedMessage(java.lang.String,org.apache.maven.wagon.proxy.ProxyInfo)
supr java.lang.Object
hfds SC_FORBIDDEN,SC_GONE,SC_NOT_FOUND,SC_PROXY_AUTH_REQUIRED,SC_UNAUTHORIZED

CLSS public org.apache.maven.wagon.shared.http.HttpMethodConfiguration
cons public init()
meth public boolean isUseDefaultHeaders()
meth public boolean isUsePreemptive()
meth public int getConnectionTimeout()
meth public int getReadTimeout()
meth public java.lang.Boolean getUseDefaultHeaders()
meth public java.util.Properties getHeaders()
meth public java.util.Properties getParams()
meth public org.apache.http.Header[] asRequestHeaders()
meth public org.apache.maven.wagon.shared.http.HttpMethodConfiguration addHeader(java.lang.String,java.lang.String)
meth public org.apache.maven.wagon.shared.http.HttpMethodConfiguration addParam(java.lang.String,java.lang.String)
meth public org.apache.maven.wagon.shared.http.HttpMethodConfiguration setConnectionTimeout(int)
meth public org.apache.maven.wagon.shared.http.HttpMethodConfiguration setHeaders(java.util.Properties)
meth public org.apache.maven.wagon.shared.http.HttpMethodConfiguration setParams(java.util.Properties)
meth public org.apache.maven.wagon.shared.http.HttpMethodConfiguration setReadTimeout(int)
meth public org.apache.maven.wagon.shared.http.HttpMethodConfiguration setUseDefaultHeaders(boolean)
meth public org.apache.maven.wagon.shared.http.HttpMethodConfiguration setUsePreemptive(boolean)
supr java.lang.Object
hfds connectionTimeout,headers,params,readTimeout,useDefaultHeaders,usePreemptive

CLSS public org.apache.maven.wagon.shared.http.RelaxedTrustStrategy
cons public init(boolean)
intf org.apache.http.conn.ssl.TrustStrategy
meth public boolean isTrusted(java.security.cert.X509Certificate[],java.lang.String) throws java.security.cert.CertificateException
supr java.lang.Object
hfds ignoreSSLValidityDates

CLSS public org.apache.maven.wagon.shared.http.StandardServiceUnavailableRetryStrategy
 anno 0 org.apache.http.annotation.Contract(org.apache.http.annotation.ThreadingBehavior threading=IMMUTABLE)
cons public init(int,int)
intf org.apache.http.client.ServiceUnavailableRetryStrategy
meth public boolean retryRequest(org.apache.http.HttpResponse,int,org.apache.http.protocol.HttpContext)
meth public long getRetryInterval()
supr java.lang.Object
hfds maxRetries,retryInterval

CLSS public org.apache.maven.wagon.shared.http.WagonRedirectStrategy
cons public init()
meth public boolean isRedirected(org.apache.http.HttpRequest,org.apache.http.HttpResponse,org.apache.http.protocol.HttpContext) throws org.apache.http.ProtocolException
meth public org.apache.http.client.methods.HttpUriRequest getRedirect(org.apache.http.HttpRequest,org.apache.http.HttpResponse,org.apache.http.protocol.HttpContext) throws org.apache.http.ProtocolException
supr org.apache.http.impl.client.DefaultRedirectStrategy
hfds LOGGER,SC_PERMANENT_REDIRECT

CLSS public abstract interface org.codehaus.plexus.ContainerConfiguration
meth public abstract boolean getAutoWiring()
meth public abstract boolean getJSR250Lifecycle()
meth public abstract java.lang.String getClassPathScanning()
meth public abstract java.lang.String getComponentVisibility()
meth public abstract java.lang.String getContainerConfiguration()
meth public abstract java.net.URL getContainerConfigurationURL()
meth public abstract java.util.Map<java.lang.Object,java.lang.Object> getContext()
meth public abstract org.codehaus.plexus.ContainerConfiguration setAutoWiring(boolean)
meth public abstract org.codehaus.plexus.ContainerConfiguration setClassPathScanning(java.lang.String)
meth public abstract org.codehaus.plexus.ContainerConfiguration setClassWorld(org.codehaus.plexus.classworlds.ClassWorld)
meth public abstract org.codehaus.plexus.ContainerConfiguration setComponentVisibility(java.lang.String)
meth public abstract org.codehaus.plexus.ContainerConfiguration setContainerConfiguration(java.lang.String)
meth public abstract org.codehaus.plexus.ContainerConfiguration setContainerConfigurationURL(java.net.URL)
meth public abstract org.codehaus.plexus.ContainerConfiguration setContext(java.util.Map<java.lang.Object,java.lang.Object>)
meth public abstract org.codehaus.plexus.ContainerConfiguration setContextComponent(org.codehaus.plexus.context.Context)
meth public abstract org.codehaus.plexus.ContainerConfiguration setJSR250Lifecycle(boolean)
meth public abstract org.codehaus.plexus.ContainerConfiguration setName(java.lang.String)
meth public abstract org.codehaus.plexus.ContainerConfiguration setRealm(org.codehaus.plexus.classworlds.realm.ClassRealm)
meth public abstract org.codehaus.plexus.classworlds.ClassWorld getClassWorld()
meth public abstract org.codehaus.plexus.classworlds.realm.ClassRealm getRealm()
meth public abstract org.codehaus.plexus.context.Context getContextComponent()

CLSS public final org.codehaus.plexus.DefaultContainerConfiguration
cons public init()
intf org.codehaus.plexus.ContainerConfiguration
meth public boolean getAutoWiring()
meth public boolean getJSR250Lifecycle()
meth public java.lang.String getClassPathScanning()
meth public java.lang.String getComponentVisibility()
meth public java.lang.String getContainerConfiguration()
meth public java.net.URL getContainerConfigurationURL()
meth public java.util.Map<java.lang.Object,java.lang.Object> getContext()
meth public org.codehaus.plexus.ContainerConfiguration setAutoWiring(boolean)
meth public org.codehaus.plexus.ContainerConfiguration setClassPathScanning(java.lang.String)
meth public org.codehaus.plexus.ContainerConfiguration setClassWorld(org.codehaus.plexus.classworlds.ClassWorld)
meth public org.codehaus.plexus.ContainerConfiguration setComponentVisibility(java.lang.String)
meth public org.codehaus.plexus.ContainerConfiguration setContainerConfiguration(java.lang.String)
meth public org.codehaus.plexus.ContainerConfiguration setContainerConfigurationURL(java.net.URL)
meth public org.codehaus.plexus.ContainerConfiguration setContext(java.util.Map<java.lang.Object,java.lang.Object>)
meth public org.codehaus.plexus.ContainerConfiguration setContextComponent(org.codehaus.plexus.context.Context)
meth public org.codehaus.plexus.ContainerConfiguration setJSR250Lifecycle(boolean)
meth public org.codehaus.plexus.ContainerConfiguration setName(java.lang.String)
meth public org.codehaus.plexus.ContainerConfiguration setRealm(org.codehaus.plexus.classworlds.realm.ClassRealm)
meth public org.codehaus.plexus.classworlds.ClassWorld getClassWorld()
meth public org.codehaus.plexus.classworlds.realm.ClassRealm getRealm()
meth public org.codehaus.plexus.context.Context getContextComponent()
supr java.lang.Object
hfds autoWiring,classPathScanning,classRealm,classWorld,componentVisibility,configurationPath,configurationUrl,contextComponent,contextData,jsr250Lifecycle

CLSS public final org.codehaus.plexus.DefaultPlexusContainer
cons public !varargs init(org.codehaus.plexus.ContainerConfiguration,com.google.inject.Module[]) throws org.codehaus.plexus.PlexusContainerException
cons public init() throws org.codehaus.plexus.PlexusContainerException
cons public init(org.codehaus.plexus.ContainerConfiguration) throws org.codehaus.plexus.PlexusContainerException
intf org.codehaus.plexus.MutablePlexusContainer
meth public !varargs com.google.inject.Injector addPlexusInjector(java.util.List<? extends org.eclipse.sisu.plexus.PlexusBeanModule>,com.google.inject.Module[])
meth public !varargs java.util.List<org.codehaus.plexus.component.repository.ComponentDescriptor<?>> discoverComponents(org.codehaus.plexus.classworlds.realm.ClassRealm,com.google.inject.Module[])
meth public <%0 extends java.lang.Object> java.util.List<org.codehaus.plexus.component.repository.ComponentDescriptor<{%%0}>> getComponentDescriptorList(java.lang.Class<{%%0}>,java.lang.String)
meth public <%0 extends java.lang.Object> java.util.List<{%%0}> lookupList(java.lang.Class<{%%0}>) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public <%0 extends java.lang.Object> java.util.Map<java.lang.String,org.codehaus.plexus.component.repository.ComponentDescriptor<{%%0}>> getComponentDescriptorMap(java.lang.Class<{%%0}>,java.lang.String)
meth public <%0 extends java.lang.Object> java.util.Map<java.lang.String,{%%0}> lookupMap(java.lang.Class<{%%0}>) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public <%0 extends java.lang.Object> org.codehaus.plexus.component.repository.ComponentDescriptor<{%%0}> getComponentDescriptor(java.lang.Class<{%%0}>,java.lang.String,java.lang.String)
meth public <%0 extends java.lang.Object> void addComponent({%%0},java.lang.Class<?>,java.lang.String)
meth public <%0 extends java.lang.Object> void addComponentDescriptor(org.codehaus.plexus.component.repository.ComponentDescriptor<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>,java.lang.String) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>,java.lang.String,java.lang.String) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public boolean hasComponent(java.lang.Class)
meth public boolean hasComponent(java.lang.Class,java.lang.String)
meth public boolean hasComponent(java.lang.Class,java.lang.String,java.lang.String)
meth public boolean hasComponent(java.lang.String)
meth public boolean hasComponent(java.lang.String,java.lang.String)
meth public java.lang.Object lookup(java.lang.String) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public java.lang.Object lookup(java.lang.String,java.lang.String) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public java.util.List getComponentDescriptorList(java.lang.String)
meth public java.util.List<java.lang.Object> lookupList(java.lang.String) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public java.util.List<org.codehaus.plexus.component.repository.ComponentDescriptor<?>> discoverComponents(org.codehaus.plexus.classworlds.realm.ClassRealm)
meth public java.util.Map getComponentDescriptorMap(java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.Object> lookupMap(java.lang.String) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public org.codehaus.plexus.classworlds.ClassWorld getClassWorld()
meth public org.codehaus.plexus.classworlds.realm.ClassRealm createChildRealm(java.lang.String)
meth public org.codehaus.plexus.classworlds.realm.ClassRealm getContainerRealm()
meth public org.codehaus.plexus.classworlds.realm.ClassRealm getLookupRealm()
meth public org.codehaus.plexus.classworlds.realm.ClassRealm setLookupRealm(org.codehaus.plexus.classworlds.realm.ClassRealm)
meth public org.codehaus.plexus.component.repository.ComponentDescriptor<?> getComponentDescriptor(java.lang.String,java.lang.String)
meth public org.codehaus.plexus.context.Context getContext()
meth public org.codehaus.plexus.logging.Logger getLogger()
meth public org.codehaus.plexus.logging.LoggerManager getLoggerManager()
meth public void addComponent(java.lang.Object,java.lang.String)
meth public void dispose()
meth public void release(java.lang.Object)
meth public void releaseAll(java.util.List<?>)
meth public void releaseAll(java.util.Map<java.lang.String,?>)
meth public void setLoggerManager(org.codehaus.plexus.logging.LoggerManager)
 anno 0 com.google.inject.Inject(boolean optional=true)
supr java.lang.Object
hfds DEFAULT_REALM_NAME,NO_CUSTOM_MODULES,componentVisibility,containerModule,containerRealm,context,defaultsModule,descriptorMap,disposing,isAutoWiringEnabled,logger,loggerManager,loggerManagerProvider,lookupRealm,plexusBeanLocator,plexusBeanManager,plexusRank,qualifiedBeanLocator,realmManager,scanning,variables
hcls BootModule,ContainerModule,DefaultsModule,LoggerManagerProvider,LoggerProvider,SLF4JLoggerFactoryProvider

CLSS public abstract interface org.codehaus.plexus.MutablePlexusContainer
intf org.codehaus.plexus.PlexusContainer
meth public abstract org.codehaus.plexus.classworlds.ClassWorld getClassWorld()
meth public abstract org.codehaus.plexus.logging.Logger getLogger()
meth public abstract org.codehaus.plexus.logging.LoggerManager getLoggerManager()
meth public abstract void setLoggerManager(org.codehaus.plexus.logging.LoggerManager)

CLSS public abstract interface org.codehaus.plexus.PlexusConstants
fld public final static java.lang.String GLOBAL_VISIBILITY = "global"
fld public final static java.lang.String PLEXUS_DEFAULT_HINT = "default"
fld public final static java.lang.String PLEXUS_KEY = "plexus"
fld public final static java.lang.String REALM_VISIBILITY = "realm"
fld public final static java.lang.String SCANNING_CACHE = "cache"
fld public final static java.lang.String SCANNING_INDEX = "index"
fld public final static java.lang.String SCANNING_OFF = "off"
fld public final static java.lang.String SCANNING_ON = "on"

CLSS public abstract interface org.codehaus.plexus.PlexusContainer
meth public abstract <%0 extends java.lang.Object> java.util.List<org.codehaus.plexus.component.repository.ComponentDescriptor<{%%0}>> getComponentDescriptorList(java.lang.Class<{%%0}>,java.lang.String)
meth public abstract <%0 extends java.lang.Object> java.util.List<{%%0}> lookupList(java.lang.Class<{%%0}>) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public abstract <%0 extends java.lang.Object> java.util.Map<java.lang.String,org.codehaus.plexus.component.repository.ComponentDescriptor<{%%0}>> getComponentDescriptorMap(java.lang.Class<{%%0}>,java.lang.String)
meth public abstract <%0 extends java.lang.Object> java.util.Map<java.lang.String,{%%0}> lookupMap(java.lang.Class<{%%0}>) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public abstract <%0 extends java.lang.Object> org.codehaus.plexus.component.repository.ComponentDescriptor<{%%0}> getComponentDescriptor(java.lang.Class<{%%0}>,java.lang.String,java.lang.String)
meth public abstract <%0 extends java.lang.Object> void addComponent({%%0},java.lang.Class<?>,java.lang.String)
meth public abstract <%0 extends java.lang.Object> void addComponentDescriptor(org.codehaus.plexus.component.repository.ComponentDescriptor<{%%0}>) throws org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>,java.lang.String) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>,java.lang.String,java.lang.String) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public abstract boolean hasComponent(java.lang.Class<?>)
meth public abstract boolean hasComponent(java.lang.Class<?>,java.lang.String)
meth public abstract boolean hasComponent(java.lang.Class<?>,java.lang.String,java.lang.String)
meth public abstract boolean hasComponent(java.lang.String)
meth public abstract boolean hasComponent(java.lang.String,java.lang.String)
meth public abstract java.lang.Object lookup(java.lang.String) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public abstract java.lang.Object lookup(java.lang.String,java.lang.String) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public abstract java.util.List<java.lang.Object> lookupList(java.lang.String) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public abstract java.util.List<org.codehaus.plexus.component.repository.ComponentDescriptor<?>> discoverComponents(org.codehaus.plexus.classworlds.realm.ClassRealm) throws org.codehaus.plexus.configuration.PlexusConfigurationException
meth public abstract java.util.List<org.codehaus.plexus.component.repository.ComponentDescriptor<?>> getComponentDescriptorList(java.lang.String)
meth public abstract java.util.Map<java.lang.String,java.lang.Object> lookupMap(java.lang.String) throws org.codehaus.plexus.component.repository.exception.ComponentLookupException
meth public abstract java.util.Map<java.lang.String,org.codehaus.plexus.component.repository.ComponentDescriptor<?>> getComponentDescriptorMap(java.lang.String)
meth public abstract org.codehaus.plexus.classworlds.realm.ClassRealm createChildRealm(java.lang.String)
meth public abstract org.codehaus.plexus.classworlds.realm.ClassRealm getContainerRealm()
meth public abstract org.codehaus.plexus.classworlds.realm.ClassRealm getLookupRealm()
meth public abstract org.codehaus.plexus.classworlds.realm.ClassRealm setLookupRealm(org.codehaus.plexus.classworlds.realm.ClassRealm)
meth public abstract org.codehaus.plexus.component.repository.ComponentDescriptor<?> getComponentDescriptor(java.lang.String,java.lang.String)
meth public abstract org.codehaus.plexus.context.Context getContext()
meth public abstract void addComponent(java.lang.Object,java.lang.String)
meth public abstract void dispose()
meth public abstract void release(java.lang.Object) throws org.codehaus.plexus.component.repository.exception.ComponentLifecycleException
meth public abstract void releaseAll(java.util.List<?>) throws org.codehaus.plexus.component.repository.exception.ComponentLifecycleException
meth public abstract void releaseAll(java.util.Map<java.lang.String,?>) throws org.codehaus.plexus.component.repository.exception.ComponentLifecycleException

CLSS public final org.codehaus.plexus.PlexusContainerException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public abstract org.codehaus.plexus.PlexusTestCase
hfds PLEXUS_HOME,container
hcls Lazy

CLSS public org.codehaus.plexus.classworlds.ClassWorld
cons public init()
cons public init(java.lang.String,java.lang.ClassLoader)
intf java.io.Closeable
meth public java.util.Collection<org.codehaus.plexus.classworlds.realm.ClassRealm> getRealms()
meth public org.codehaus.plexus.classworlds.realm.ClassRealm getClassRealm(java.lang.String)
meth public org.codehaus.plexus.classworlds.realm.ClassRealm getRealm(java.lang.String) throws org.codehaus.plexus.classworlds.realm.NoSuchRealmException
meth public org.codehaus.plexus.classworlds.realm.ClassRealm newRealm(java.lang.String) throws org.codehaus.plexus.classworlds.realm.DuplicateRealmException
meth public org.codehaus.plexus.classworlds.realm.ClassRealm newRealm(java.lang.String,java.lang.ClassLoader) throws org.codehaus.plexus.classworlds.realm.DuplicateRealmException
meth public org.codehaus.plexus.classworlds.realm.ClassRealm newRealm(java.lang.String,java.lang.ClassLoader,java.util.function.Predicate<java.lang.String>) throws org.codehaus.plexus.classworlds.realm.DuplicateRealmException
meth public void addListener(org.codehaus.plexus.classworlds.ClassWorldListener)
meth public void close() throws java.io.IOException
meth public void disposeRealm(java.lang.String) throws org.codehaus.plexus.classworlds.realm.NoSuchRealmException
meth public void removeListener(org.codehaus.plexus.classworlds.ClassWorldListener)
supr java.lang.Object
hfds listeners,realms

CLSS public org.codehaus.plexus.classworlds.ClassWorldException
cons public init(org.codehaus.plexus.classworlds.ClassWorld)
cons public init(org.codehaus.plexus.classworlds.ClassWorld,java.lang.String)
meth public org.codehaus.plexus.classworlds.ClassWorld getWorld()
supr java.lang.Exception
hfds world

CLSS public abstract interface org.codehaus.plexus.classworlds.ClassWorldListener
meth public abstract void realmCreated(org.codehaus.plexus.classworlds.realm.ClassRealm)
meth public abstract void realmDisposed(org.codehaus.plexus.classworlds.realm.ClassRealm)

CLSS public org.codehaus.plexus.classworlds.UrlUtils
cons public init()
meth public static java.lang.String normalizeUrlPath(java.lang.String)
meth public static java.util.Set<java.net.URL> getURLs(java.net.URLClassLoader)
supr java.lang.Object

CLSS public org.codehaus.plexus.classworlds.realm.ClassRealm
cons public init(org.codehaus.plexus.classworlds.ClassWorld,java.lang.String,java.lang.ClassLoader)
meth protected java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Class<?> findClass(java.lang.String,java.lang.String)
meth protected java.lang.Class<?> findClassInternal(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Class<?> loadClass(java.lang.String,boolean) throws java.lang.ClassNotFoundException
meth protected java.lang.Object getClassLoadingLock(java.lang.String)
meth public java.lang.Class<?> loadClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.lang.Class<?> loadClassFromImport(java.lang.String)
meth public java.lang.Class<?> loadClassFromParent(java.lang.String)
meth public java.lang.Class<?> loadClassFromSelf(java.lang.String)
meth public java.lang.ClassLoader getImportClassLoader(java.lang.String)
meth public java.lang.ClassLoader getParentClassLoader()
meth public java.lang.String getId()
meth public java.lang.String toString()
meth public java.net.URL findResource(java.lang.String)
meth public java.net.URL getResource(java.lang.String)
meth public java.net.URL loadResourceFromImport(java.lang.String)
meth public java.net.URL loadResourceFromParent(java.lang.String)
meth public java.net.URL loadResourceFromSelf(java.lang.String)
meth public java.util.Collection<org.codehaus.plexus.classworlds.realm.ClassRealm> getImportRealms()
meth public java.util.Enumeration<java.net.URL> findResources(java.lang.String) throws java.io.IOException
meth public java.util.Enumeration<java.net.URL> getResources(java.lang.String) throws java.io.IOException
meth public java.util.Enumeration<java.net.URL> loadResourcesFromImport(java.lang.String)
meth public java.util.Enumeration<java.net.URL> loadResourcesFromParent(java.lang.String)
meth public java.util.Enumeration<java.net.URL> loadResourcesFromSelf(java.lang.String)
meth public org.codehaus.plexus.classworlds.ClassWorld getWorld()
meth public org.codehaus.plexus.classworlds.realm.ClassRealm createChildRealm(java.lang.String) throws org.codehaus.plexus.classworlds.realm.DuplicateRealmException
meth public org.codehaus.plexus.classworlds.realm.ClassRealm getParentRealm()
meth public org.codehaus.plexus.classworlds.strategy.Strategy getStrategy()
meth public void addURL(java.net.URL)
meth public void display()
meth public void display(java.io.PrintStream)
meth public void importFrom(java.lang.ClassLoader,java.lang.String)
meth public void importFrom(java.lang.String,java.lang.String) throws org.codehaus.plexus.classworlds.realm.NoSuchRealmException
meth public void importFromParent(java.lang.String)
meth public void setParentClassLoader(java.lang.ClassLoader)
meth public void setParentRealm(org.codehaus.plexus.classworlds.realm.ClassRealm)
supr java.net.URLClassLoader
hfds foreignImports,id,isParallelCapable,lockMap,parentClassLoader,parentImports,strategy,world

CLSS public org.codehaus.plexus.classworlds.realm.DuplicateRealmException
cons public init(org.codehaus.plexus.classworlds.ClassWorld,java.lang.String)
meth public java.lang.String getId()
supr org.codehaus.plexus.classworlds.ClassWorldException
hfds id

CLSS public org.codehaus.plexus.classworlds.realm.FilteredClassRealm
cons public init(java.util.function.Predicate<java.lang.String>,org.codehaus.plexus.classworlds.ClassWorld,java.lang.String,java.lang.ClassLoader)
meth protected java.lang.Class<?> findClassInternal(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.net.URL findResource(java.lang.String)
meth public java.util.Enumeration<java.net.URL> findResources(java.lang.String) throws java.io.IOException
supr org.codehaus.plexus.classworlds.realm.ClassRealm
hfds filter

CLSS public org.codehaus.plexus.classworlds.realm.NoSuchRealmException
cons public init(org.codehaus.plexus.classworlds.ClassWorld,java.lang.String)
meth public java.lang.String getId()
supr org.codehaus.plexus.classworlds.ClassWorldException
hfds id

CLSS public abstract interface !annotation org.codehaus.plexus.component.annotations.Component
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Inherited()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean isolatedRealm()
meth public abstract !hasdefault java.lang.String alias()
meth public abstract !hasdefault java.lang.String composer()
meth public abstract !hasdefault java.lang.String configurator()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String factory()
meth public abstract !hasdefault java.lang.String hint()
meth public abstract !hasdefault java.lang.String instantiationStrategy()
meth public abstract !hasdefault java.lang.String lifecycleHandler()
meth public abstract !hasdefault java.lang.String profile()
meth public abstract !hasdefault java.lang.String type()
meth public abstract !hasdefault java.lang.String version()
meth public abstract java.lang.Class<?> role()

CLSS public abstract interface !annotation org.codehaus.plexus.component.annotations.Configuration
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Inherited()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String name()
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation org.codehaus.plexus.component.annotations.Requirement
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Inherited()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean optional()
meth public abstract !hasdefault java.lang.Class<?> role()
meth public abstract !hasdefault java.lang.String hint()
meth public abstract !hasdefault java.lang.String[] hints()

CLSS public final org.codehaus.plexus.component.composition.CycleDetectedInComponentGraphException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public abstract interface org.codehaus.plexus.component.configurator.ConfigurationListener
meth public abstract void notifyFieldChangeUsingReflection(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void notifyFieldChangeUsingSetter(java.lang.String,java.lang.Object,java.lang.Object)

CLSS public org.codehaus.plexus.component.configurator.expression.DefaultExpressionEvaluator
cons public init()
intf org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator
meth public java.io.File alignToBaseDirectory(java.io.File)
meth public java.lang.Object evaluate(java.lang.String)
supr java.lang.Object

CLSS public final org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public abstract interface org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator
meth public abstract java.io.File alignToBaseDirectory(java.io.File)
meth public abstract java.lang.Object evaluate(java.lang.String) throws org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException

CLSS public abstract interface org.codehaus.plexus.component.configurator.expression.TypeAwareExpressionEvaluator
intf org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator
meth public abstract java.lang.Object evaluate(java.lang.String,java.lang.Class<?>) throws org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException

CLSS public final org.codehaus.plexus.component.repository.ComponentDependency
cons public init()
meth public java.lang.String getArtifactId()
meth public java.lang.String getGroupId()
meth public java.lang.String getType()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public void setArtifactId(java.lang.String)
meth public void setGroupId(java.lang.String)
meth public void setType(java.lang.String)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds artifactId,groupId,type,version

CLSS public org.codehaus.plexus.component.repository.ComponentDescriptor<%0 extends java.lang.Object>
cons public init()
cons public init(java.lang.Class<{org.codehaus.plexus.component.repository.ComponentDescriptor%0}>,org.codehaus.plexus.classworlds.realm.ClassRealm)
meth public boolean equals(java.lang.Object)
meth public final boolean hasConfiguration()
meth public final boolean isIsolatedRealm()
meth public final java.lang.Class<{org.codehaus.plexus.component.repository.ComponentDescriptor%0}> getImplementationClass()
meth public final java.lang.Class<{org.codehaus.plexus.component.repository.ComponentDescriptor%0}> getRoleClass()
meth public final java.lang.String getAlias()
meth public final java.lang.String getComponentComposer()
meth public final java.lang.String getComponentConfigurator()
meth public final java.lang.String getComponentFactory()
meth public final java.lang.String getComponentProfile()
meth public final java.lang.String getDescription()
meth public final java.lang.String getHumanReadableKey()
meth public final java.lang.String getImplementation()
meth public final java.lang.String getInstantiationStrategy()
meth public final java.lang.String getLifecycleHandler()
meth public final java.lang.String getVersion()
meth public final java.lang.String toString()
meth public final java.util.List<org.codehaus.plexus.component.repository.ComponentRequirement> getRequirements()
meth public final org.codehaus.plexus.classworlds.realm.ClassRealm getRealm()
meth public final org.codehaus.plexus.configuration.PlexusConfiguration getConfiguration()
meth public final void addRequirement(org.codehaus.plexus.component.repository.ComponentRequirement)
meth public final void setAlias(java.lang.String)
meth public final void setComponentComposer(java.lang.String)
meth public final void setComponentConfigurator(java.lang.String)
meth public final void setComponentFactory(java.lang.String)
meth public final void setComponentProfile(java.lang.String)
meth public final void setComponentType(java.lang.String)
meth public final void setConfiguration(org.codehaus.plexus.configuration.PlexusConfiguration)
meth public final void setDescription(java.lang.String)
meth public final void setImplementation(java.lang.String)
meth public final void setImplementationClass(java.lang.Class)
meth public final void setInstantiationStrategy(java.lang.String)
meth public final void setIsolatedRealm(boolean)
meth public final void setLifecycleHandler(java.lang.String)
meth public final void setRealm(org.codehaus.plexus.classworlds.realm.ClassRealm)
meth public final void setRole(java.lang.String)
meth public final void setRoleClass(java.lang.Class<?>)
meth public final void setRoleHint(java.lang.String)
meth public final void setVersion(java.lang.String)
meth public int hashCode()
meth public java.lang.String getComponentType()
meth public java.lang.String getRole()
meth public java.lang.String getRoleHint()
supr java.lang.Object
hfds alias,classRealm,componentComposer,componentConfigurator,componentFactory,componentProfile,componentType,configuration,description,hint,implementation,implementationClass,instantiationStrategy,isolatedRealm,lifecycleHandler,requirements,role,version

CLSS public org.codehaus.plexus.component.repository.ComponentRequirement
cons public init()
meth public boolean equals(java.lang.Object)
meth public final boolean isOptional()
meth public final java.lang.String getFieldName()
meth public final java.lang.String getRole()
meth public final java.lang.String getRoleHint()
meth public final void setFieldMappingType(java.lang.String)
meth public final void setFieldName(java.lang.String)
meth public final void setOptional(boolean)
meth public final void setRole(java.lang.String)
meth public final void setRoleHint(java.lang.String)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds hint,name,optional,role

CLSS public final org.codehaus.plexus.component.repository.ComponentRequirementList
cons public init()
meth public java.util.List<java.lang.String> getRoleHints()
meth public void setRoleHints(java.util.List<java.lang.String>)
supr org.codehaus.plexus.component.repository.ComponentRequirement
hfds hints

CLSS public org.codehaus.plexus.component.repository.ComponentSetDescriptor
cons public init()
meth public final boolean isIsolatedRealm()
meth public final java.lang.String toString()
meth public final java.util.List<org.codehaus.plexus.component.repository.ComponentDependency> getDependencies()
meth public final java.util.List<org.codehaus.plexus.component.repository.ComponentDescriptor<?>> getComponents()
meth public final void addComponentDescriptor(org.codehaus.plexus.component.repository.ComponentDescriptor<?>)
meth public final void addDependency(org.codehaus.plexus.component.repository.ComponentDependency)
meth public final void setComponents(java.util.List<org.codehaus.plexus.component.repository.ComponentDescriptor<?>>)
meth public final void setDependencies(java.util.List<org.codehaus.plexus.component.repository.ComponentDependency>)
meth public final void setId(java.lang.String)
meth public final void setIsolatedRealm(boolean)
supr java.lang.Object
hfds components,dependencies,isolatedRealm

CLSS public final org.codehaus.plexus.component.repository.exception.ComponentLifecycleException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public final org.codehaus.plexus.component.repository.exception.ComponentLookupException
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.Throwable,java.lang.String,java.lang.String)
meth public java.lang.String getMessage()
supr java.lang.Exception
hfds LS,hint,role,serialVersionUID

CLSS public final org.codehaus.plexus.component.repository.exception.ComponentRepositoryException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public org.codehaus.plexus.configuration.DefaultPlexusConfiguration
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
intf org.codehaus.plexus.configuration.PlexusConfiguration
meth protected final org.codehaus.plexus.configuration.PlexusConfiguration add(org.codehaus.plexus.configuration.PlexusConfiguration)
meth protected org.codehaus.plexus.configuration.PlexusConfiguration createChild(java.lang.String)
meth public final int getChildCount()
meth public final java.lang.String getAttribute(java.lang.String)
meth public final java.lang.String getAttribute(java.lang.String,java.lang.String)
meth public final java.lang.String getName()
meth public final java.lang.String getValue()
meth public final java.lang.String getValue(java.lang.String)
meth public final java.lang.String[] getAttributeNames()
meth public final org.codehaus.plexus.configuration.PlexusConfiguration addChild(java.lang.String,java.lang.String)
meth public final org.codehaus.plexus.configuration.PlexusConfiguration getChild(int)
meth public final org.codehaus.plexus.configuration.PlexusConfiguration getChild(java.lang.String)
meth public final org.codehaus.plexus.configuration.PlexusConfiguration getChild(java.lang.String,boolean)
meth public final org.codehaus.plexus.configuration.PlexusConfiguration[] getChildren()
meth public final org.codehaus.plexus.configuration.PlexusConfiguration[] getChildren(java.lang.String)
meth public final void addChild(org.codehaus.plexus.configuration.PlexusConfiguration)
meth public final void setAttribute(java.lang.String,java.lang.String)
meth public final void setValue(java.lang.String)
supr java.lang.Object
hfds NO_CHILDREN,attributeMap,childIndex,childMap,name,value

CLSS public abstract interface org.codehaus.plexus.configuration.PlexusConfiguration
meth public abstract int getChildCount()
meth public abstract java.lang.String getAttribute(java.lang.String)
meth public abstract java.lang.String getAttribute(java.lang.String,java.lang.String)
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()
meth public abstract java.lang.String getValue(java.lang.String)
meth public abstract java.lang.String[] getAttributeNames()
meth public abstract org.codehaus.plexus.configuration.PlexusConfiguration addChild(java.lang.String,java.lang.String)
meth public abstract org.codehaus.plexus.configuration.PlexusConfiguration getChild(int)
meth public abstract org.codehaus.plexus.configuration.PlexusConfiguration getChild(java.lang.String)
meth public abstract org.codehaus.plexus.configuration.PlexusConfiguration getChild(java.lang.String,boolean)
meth public abstract org.codehaus.plexus.configuration.PlexusConfiguration[] getChildren()
meth public abstract org.codehaus.plexus.configuration.PlexusConfiguration[] getChildren(java.lang.String)
meth public abstract void addChild(org.codehaus.plexus.configuration.PlexusConfiguration)
meth public abstract void setAttribute(java.lang.String,java.lang.String)
meth public abstract void setValue(java.lang.String)

CLSS public org.codehaus.plexus.configuration.PlexusConfigurationException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public final org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration
cons public init(java.lang.String)
cons public init(org.codehaus.plexus.util.xml.Xpp3Dom)
meth protected org.codehaus.plexus.configuration.PlexusConfiguration createChild(java.lang.String)
meth public java.lang.String toString()
supr org.codehaus.plexus.configuration.DefaultPlexusConfiguration

CLSS public abstract org.codehaus.plexus.logging.AbstractLogEnabled
cons public init()
intf org.codehaus.plexus.logging.LogEnabled
meth protected final void setupLogger(java.lang.Object)
meth protected final void setupLogger(java.lang.Object,java.lang.String)
meth protected final void setupLogger(java.lang.Object,org.codehaus.plexus.logging.Logger)
meth protected org.codehaus.plexus.logging.Logger getLogger()
meth public void enableLogging(org.codehaus.plexus.logging.Logger)
supr java.lang.Object
hfds logger

CLSS public abstract org.codehaus.plexus.logging.AbstractLogger
cons public init(int,java.lang.String)
intf org.codehaus.plexus.logging.Logger
meth public boolean isDebugEnabled()
meth public boolean isErrorEnabled()
meth public boolean isFatalErrorEnabled()
meth public boolean isInfoEnabled()
meth public boolean isWarnEnabled()
meth public final int getThreshold()
meth public final java.lang.String getName()
meth public final void debug(java.lang.String)
meth public final void error(java.lang.String)
meth public final void fatalError(java.lang.String)
meth public final void info(java.lang.String)
meth public final void setThreshold(int)
meth public final void warn(java.lang.String)
supr java.lang.Object
hfds name,threshold

CLSS public abstract org.codehaus.plexus.logging.AbstractLoggerManager
cons public init()
intf org.codehaus.plexus.logging.LoggerManager
meth public final org.codehaus.plexus.logging.Logger getLoggerForComponent(java.lang.String)
meth public final void returnComponentLogger(java.lang.String)
supr java.lang.Object

CLSS public abstract org.codehaus.plexus.logging.BaseLoggerManager
cons public init()
intf org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable
meth protected abstract org.codehaus.plexus.logging.Logger createLogger(java.lang.String)
meth public final int getActiveLoggerCount()
meth public final int getThreshold()
meth public final org.codehaus.plexus.logging.Logger getLoggerForComponent(java.lang.String,java.lang.String)
meth public final static int parseThreshold(java.lang.String)
meth public final void initialize()
meth public final void returnComponentLogger(java.lang.String,java.lang.String)
meth public final void setThreshold(int)
meth public final void setThresholds(int)
supr org.codehaus.plexus.logging.AbstractLoggerManager
hfds activeLoggers,currentThreshold,threshold

CLSS public abstract interface org.codehaus.plexus.logging.LogEnabled
meth public abstract void enableLogging(org.codehaus.plexus.logging.Logger)

CLSS public abstract interface org.codehaus.plexus.logging.Logger
fld public final static int LEVEL_DEBUG = 0
fld public final static int LEVEL_DISABLED = 5
fld public final static int LEVEL_ERROR = 3
fld public final static int LEVEL_FATAL = 4
fld public final static int LEVEL_INFO = 1
fld public final static int LEVEL_WARN = 2
meth public abstract boolean isDebugEnabled()
meth public abstract boolean isErrorEnabled()
meth public abstract boolean isFatalErrorEnabled()
meth public abstract boolean isInfoEnabled()
meth public abstract boolean isWarnEnabled()
meth public abstract int getThreshold()
meth public abstract java.lang.String getName()
meth public abstract org.codehaus.plexus.logging.Logger getChildLogger(java.lang.String)
meth public abstract void debug(java.lang.String)
meth public abstract void debug(java.lang.String,java.lang.Throwable)
meth public abstract void error(java.lang.String)
meth public abstract void error(java.lang.String,java.lang.Throwable)
meth public abstract void fatalError(java.lang.String)
meth public abstract void fatalError(java.lang.String,java.lang.Throwable)
meth public abstract void info(java.lang.String)
meth public abstract void info(java.lang.String,java.lang.Throwable)
meth public abstract void setThreshold(int)
meth public abstract void warn(java.lang.String)
meth public abstract void warn(java.lang.String,java.lang.Throwable)

CLSS public abstract interface org.codehaus.plexus.logging.LoggerManager
fld public final static java.lang.String ROLE
meth public abstract int getActiveLoggerCount()
meth public abstract int getThreshold()
meth public abstract org.codehaus.plexus.logging.Logger getLoggerForComponent(java.lang.String)
meth public abstract org.codehaus.plexus.logging.Logger getLoggerForComponent(java.lang.String,java.lang.String)
meth public abstract void returnComponentLogger(java.lang.String)
meth public abstract void returnComponentLogger(java.lang.String,java.lang.String)
meth public abstract void setThreshold(int)
meth public abstract void setThresholds(int)

CLSS public abstract interface org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable
meth public abstract void dispose()

CLSS public abstract interface org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable
meth public abstract void initialize() throws org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException

CLSS public abstract org.codehaus.plexus.util.AbstractScanner
cons public init()
fld protected boolean isCaseSensitive
fld protected java.lang.String[] excludes
fld protected java.lang.String[] includes
fld protected java.util.Comparator<java.lang.String> filenameComparator
fld public final static java.lang.String[] DEFAULTEXCLUDES
intf org.codehaus.plexus.util.Scanner
meth protected boolean couldHoldIncluded(java.lang.String)
meth protected boolean isExcluded(java.lang.String)
meth protected boolean isExcluded(java.lang.String,char[][])
meth protected boolean isExcluded(java.lang.String,java.lang.String[])
meth protected boolean isIncluded(java.lang.String)
meth protected boolean isIncluded(java.lang.String,char[][])
meth protected boolean isIncluded(java.lang.String,java.lang.String[])
meth protected static boolean match(java.lang.String,java.lang.String,boolean)
meth protected static boolean matchPath(java.lang.String,java.lang.String)
meth protected static boolean matchPath(java.lang.String,java.lang.String,boolean)
meth protected static boolean matchPatternStart(java.lang.String,java.lang.String)
meth protected static boolean matchPatternStart(java.lang.String,java.lang.String,boolean)
meth protected void setupDefaultFilters()
meth protected void setupMatchPatterns()
meth public static boolean match(java.lang.String,java.lang.String)
meth public void addDefaultExcludes()
meth public void setCaseSensitive(boolean)
meth public void setExcludes(java.lang.String[])
meth public void setFilenameComparator(java.util.Comparator<java.lang.String>)
meth public void setIncludes(java.lang.String[])
supr java.lang.Object
hfds excludesPatterns,includesPatterns

CLSS public org.codehaus.plexus.util.Base64
cons public init()
meth public byte[] decode(byte[])
meth public byte[] encode(byte[])
meth public static boolean isArrayByteBase64(byte[])
meth public static byte[] decodeBase64(byte[])
meth public static byte[] encodeBase64(byte[])
meth public static byte[] encodeBase64(byte[],boolean)
meth public static byte[] encodeBase64Chunked(byte[])
supr java.lang.Object
hfds BASELENGTH,CHUNK_SEPARATOR,CHUNK_SIZE,EIGHTBIT,FOURBYTE,LOOKUPLENGTH,PAD,SIGN,SIXTEENBIT,TWENTYFOURBITGROUP,base64Alphabet,lookUpBase64Alphabet

CLSS public final org.codehaus.plexus.util.CachedMap
cons public init()
cons public init(int)
cons public init(int,java.util.Map)
intf java.util.Map
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int getCacheSize()
meth public int hashCode()
meth public int size()
meth public java.lang.Object get(java.lang.Object)
meth public java.lang.Object put(java.lang.Object,java.lang.Object)
meth public java.lang.Object remove(java.lang.Object)
meth public java.util.Collection values()
meth public java.util.Map getBackingMap()
meth public java.util.Set entrySet()
meth public java.util.Set keySet()
meth public void clear()
meth public void flush()
meth public void putAll(java.util.Map)
supr java.lang.Object
hfds _backingFastMap,_backingMap,_keys,_keysMap,_mask,_values

CLSS public org.codehaus.plexus.util.CollectionUtils
cons public init()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> mergeMaps(java.util.Map<{%%0},{%%1}>,java.util.Map<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> mergeMaps(java.util.Map<{%%0},{%%1}>[])
meth public static <%0 extends java.lang.Object> java.util.Collection<{%%0}> intersection(java.util.Collection<{%%0}>,java.util.Collection<{%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Collection<{%%0}> subtract(java.util.Collection<{%%0}>,java.util.Collection<{%%0}>)
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> iteratorToList(java.util.Iterator<{%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Map<{%%0},java.lang.Integer> getCardinalityMap(java.util.Collection<{%%0}>)
supr java.lang.Object

CLSS public org.codehaus.plexus.util.DirectoryScanner
cons public init()
fld protected boolean everythingIncluded
fld protected boolean haveSlowResults
fld protected java.io.File basedir
fld protected java.util.ArrayList<java.lang.String> dirsDeselected
fld protected java.util.ArrayList<java.lang.String> dirsExcluded
fld protected java.util.ArrayList<java.lang.String> dirsIncluded
fld protected java.util.ArrayList<java.lang.String> dirsNotIncluded
fld protected java.util.ArrayList<java.lang.String> filesDeselected
fld protected java.util.ArrayList<java.lang.String> filesExcluded
fld protected java.util.ArrayList<java.lang.String> filesIncluded
fld protected java.util.ArrayList<java.lang.String> filesNotIncluded
meth protected boolean isSelected(java.lang.String,java.io.File)
meth protected void scandir(java.io.File,java.lang.String,boolean)
meth protected void slowScan()
meth public boolean isEverythingIncluded()
meth public boolean isParentSymbolicLink(java.io.File,java.lang.String) throws java.io.IOException
meth public boolean isSymbolicLink(java.io.File,java.lang.String) throws java.io.IOException
meth public java.io.File getBasedir()
meth public java.lang.String[] getDeselectedDirectories()
meth public java.lang.String[] getDeselectedFiles()
meth public java.lang.String[] getExcludedDirectories()
meth public java.lang.String[] getExcludedFiles()
meth public java.lang.String[] getIncludedDirectories()
meth public java.lang.String[] getIncludedFiles()
meth public java.lang.String[] getNotIncludedDirectories()
meth public java.lang.String[] getNotIncludedFiles()
meth public void scan()
meth public void setBasedir(java.io.File)
meth public void setBasedir(java.lang.String)
meth public void setFollowSymlinks(boolean)
supr org.codehaus.plexus.util.AbstractScanner
hfds EMPTY_STRING_ARRAY,followSymlinks,tokenizedEmpty

CLSS public abstract interface org.codehaus.plexus.util.DirectoryWalkListener
meth public abstract void debug(java.lang.String)
meth public abstract void directoryWalkFinished()
meth public abstract void directoryWalkStarting(java.io.File)
meth public abstract void directoryWalkStep(int,java.io.File)

CLSS public org.codehaus.plexus.util.DirectoryWalker
cons public init()
meth public java.io.File getBaseDir()
meth public java.util.List<java.lang.String> getExcludes()
meth public java.util.List<java.lang.String> getIncludes()
meth public void addDirectoryWalkListener(org.codehaus.plexus.util.DirectoryWalkListener)
meth public void addExclude(java.lang.String)
meth public void addInclude(java.lang.String)
meth public void addSCMExcludes()
meth public void removeDirectoryWalkListener(org.codehaus.plexus.util.DirectoryWalkListener)
meth public void scan()
meth public void setBaseDir(java.io.File)
meth public void setDebugMode(boolean)
meth public void setExcludes(java.util.List<java.lang.String>)
meth public void setIncludes(java.util.List<java.lang.String>)
supr java.lang.Object
hfds baseDir,baseDirOffset,debugEnabled,dirStack,excludes,includes,isCaseSensitive,listeners
hcls DirStackEntry

CLSS public org.codehaus.plexus.util.ExceptionUtils
cons protected init()
fld protected static java.lang.String[] CAUSE_METHOD_NAMES
meth public static boolean isNestedThrowable(java.lang.Throwable)
meth public static int getThrowableCount(java.lang.Throwable)
meth public static int indexOfThrowable(java.lang.Throwable,java.lang.Class)
meth public static int indexOfThrowable(java.lang.Throwable,java.lang.Class,int)
meth public static java.lang.String getFullStackTrace(java.lang.Throwable)
meth public static java.lang.String getStackTrace(java.lang.Throwable)
meth public static java.lang.String[] getRootCauseStackTrace(java.lang.Throwable)
meth public static java.lang.String[] getStackFrames(java.lang.Throwable)
meth public static java.lang.Throwable getCause(java.lang.Throwable)
meth public static java.lang.Throwable getCause(java.lang.Throwable,java.lang.String[])
meth public static java.lang.Throwable getRootCause(java.lang.Throwable)
meth public static java.lang.Throwable[] getThrowables(java.lang.Throwable)
meth public static void addCauseMethodName(java.lang.String)
meth public static void printRootCauseStackTrace(java.lang.Throwable)
meth public static void printRootCauseStackTrace(java.lang.Throwable,java.io.PrintStream)
meth public static void printRootCauseStackTrace(java.lang.Throwable,java.io.PrintWriter)
supr java.lang.Object
hfds WRAPPED_MARKER

CLSS public org.codehaus.plexus.util.Expand
cons public init()
meth protected void expandFile(java.io.File,java.io.File) throws java.lang.Exception
meth protected void extractFile(java.io.File,java.io.File,java.io.InputStream,java.lang.String,java.util.Date,boolean) throws java.lang.Exception
meth public void execute() throws java.lang.Exception
meth public void setDest(java.io.File)
meth public void setOverwrite(boolean)
meth public void setSrc(java.io.File)
supr java.lang.Object
hfds dest,overwrite,source

CLSS public org.codehaus.plexus.util.FastMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(java.util.Map)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.Map<{org.codehaus.plexus.util.FastMap%0},{org.codehaus.plexus.util.FastMap%1}>
meth protected void sizeChanged()
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int capacity()
meth public int hashCode()
meth public int size()
meth public java.lang.Object clone()
meth public java.lang.Object put(java.lang.Object,java.lang.Object)
meth public java.lang.String toString()
meth public java.util.Collection values()
meth public java.util.Map$Entry getEntry(java.lang.Object)
meth public java.util.Set entrySet()
meth public java.util.Set keySet()
meth public void clear()
meth public void putAll(java.util.Map<? extends {org.codehaus.plexus.util.FastMap%0},? extends {org.codehaus.plexus.util.FastMap%1}>)
meth public void setCapacity(int)
meth public {org.codehaus.plexus.util.FastMap%1} get(java.lang.Object)
meth public {org.codehaus.plexus.util.FastMap%1} remove(java.lang.Object)
supr java.lang.Object
hfds _capacity,_entries,_entrySet,_keySet,_mapFirst,_mapLast,_mask,_poolFirst,_size,_values
hcls EntryImpl,EntrySet,KeySet,Values

CLSS public org.codehaus.plexus.util.FileUtils
cons public init()
fld public final static int ONE_GB = 1073741824
fld public final static int ONE_KB = 1024
fld public final static int ONE_MB = 1048576
fld public static java.lang.String FS
innr public abstract static FilterWrapper
meth public static boolean contentEquals(java.io.File,java.io.File) throws java.io.IOException
meth public static boolean copyFileIfModified(java.io.File,java.io.File) throws java.io.IOException
meth public static boolean fileExists(java.lang.String)
meth public static boolean isValidWindowsFileName(java.io.File)
meth public static boolean waitFor(java.io.File,int)
meth public static boolean waitFor(java.lang.String,int)
meth public static java.io.File createTempFile(java.lang.String,java.lang.String,java.io.File)
meth public static java.io.File getFile(java.lang.String)
meth public static java.io.File resolveFile(java.io.File,java.lang.String)
meth public static java.io.File toFile(java.net.URL)
meth public static java.lang.String basename(java.lang.String)
meth public static java.lang.String basename(java.lang.String,java.lang.String)
meth public static java.lang.String byteCountToDisplaySize(int)
meth public static java.lang.String catPath(java.lang.String,java.lang.String)
meth public static java.lang.String dirname(java.lang.String)
meth public static java.lang.String extension(java.lang.String)
meth public static java.lang.String fileRead(java.io.File) throws java.io.IOException
meth public static java.lang.String fileRead(java.io.File,java.lang.String) throws java.io.IOException
meth public static java.lang.String fileRead(java.lang.String) throws java.io.IOException
meth public static java.lang.String fileRead(java.lang.String,java.lang.String) throws java.io.IOException
meth public static java.lang.String filename(java.lang.String)
meth public static java.lang.String getDefaultExcludesAsString()
meth public static java.lang.String getExtension(java.lang.String)
meth public static java.lang.String getPath(java.lang.String)
meth public static java.lang.String getPath(java.lang.String,char)
meth public static java.lang.String normalize(java.lang.String)
meth public static java.lang.String removeExtension(java.lang.String)
meth public static java.lang.String removePath(java.lang.String)
meth public static java.lang.String removePath(java.lang.String,char)
meth public static java.lang.String[] getDefaultExcludes()
meth public static java.lang.String[] getFilesFromExtension(java.lang.String,java.lang.String[])
meth public static java.net.URL[] toURLs(java.io.File[]) throws java.io.IOException
meth public static java.util.List<java.io.File> getFiles(java.io.File,java.lang.String,java.lang.String) throws java.io.IOException
meth public static java.util.List<java.io.File> getFiles(java.io.File,java.lang.String,java.lang.String,boolean) throws java.io.IOException
meth public static java.util.List<java.lang.String> getDefaultExcludesAsList()
meth public static java.util.List<java.lang.String> getDirectoryNames(java.io.File,java.lang.String,java.lang.String,boolean) throws java.io.IOException
meth public static java.util.List<java.lang.String> getDirectoryNames(java.io.File,java.lang.String,java.lang.String,boolean,boolean) throws java.io.IOException
meth public static java.util.List<java.lang.String> getFileAndDirectoryNames(java.io.File,java.lang.String,java.lang.String,boolean,boolean,boolean,boolean) throws java.io.IOException
meth public static java.util.List<java.lang.String> getFileNames(java.io.File,java.lang.String,java.lang.String,boolean) throws java.io.IOException
meth public static java.util.List<java.lang.String> getFileNames(java.io.File,java.lang.String,java.lang.String,boolean,boolean) throws java.io.IOException
meth public static java.util.List<java.lang.String> loadFile(java.io.File) throws java.io.IOException
meth public static long sizeOfDirectory(java.io.File)
meth public static long sizeOfDirectory(java.lang.String)
meth public static void cleanDirectory(java.io.File) throws java.io.IOException
meth public static void cleanDirectory(java.lang.String) throws java.io.IOException
meth public static void copyDirectory(java.io.File,java.io.File) throws java.io.IOException
meth public static void copyDirectory(java.io.File,java.io.File,java.lang.String,java.lang.String) throws java.io.IOException
meth public static void copyDirectoryLayout(java.io.File,java.io.File,java.lang.String[],java.lang.String[]) throws java.io.IOException
meth public static void copyDirectoryStructure(java.io.File,java.io.File) throws java.io.IOException
meth public static void copyDirectoryStructureIfModified(java.io.File,java.io.File) throws java.io.IOException
meth public static void copyFile(java.io.File,java.io.File) throws java.io.IOException
meth public static void copyFile(java.io.File,java.io.File,java.lang.String,org.codehaus.plexus.util.FileUtils$FilterWrapper[]) throws java.io.IOException
meth public static void copyFile(java.io.File,java.io.File,java.lang.String,org.codehaus.plexus.util.FileUtils$FilterWrapper[],boolean) throws java.io.IOException
meth public static void copyFileToDirectory(java.io.File,java.io.File) throws java.io.IOException
meth public static void copyFileToDirectory(java.lang.String,java.lang.String) throws java.io.IOException
meth public static void copyFileToDirectoryIfModified(java.io.File,java.io.File) throws java.io.IOException
meth public static void copyFileToDirectoryIfModified(java.lang.String,java.lang.String) throws java.io.IOException
meth public static void copyStreamToFile(org.codehaus.plexus.util.io.InputStreamFacade,java.io.File) throws java.io.IOException
meth public static void copyURLToFile(java.net.URL,java.io.File) throws java.io.IOException
meth public static void deleteDirectory(java.io.File) throws java.io.IOException
meth public static void deleteDirectory(java.lang.String) throws java.io.IOException
meth public static void fileAppend(java.lang.String,java.lang.String) throws java.io.IOException
meth public static void fileAppend(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public static void fileDelete(java.lang.String)
meth public static void fileWrite(java.io.File,java.lang.String) throws java.io.IOException
meth public static void fileWrite(java.io.File,java.lang.String,java.lang.String) throws java.io.IOException
meth public static void fileWrite(java.lang.String,java.lang.String) throws java.io.IOException
meth public static void fileWrite(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public static void forceDelete(java.io.File) throws java.io.IOException
meth public static void forceDelete(java.lang.String) throws java.io.IOException
meth public static void forceDeleteOnExit(java.io.File) throws java.io.IOException
meth public static void forceMkdir(java.io.File) throws java.io.IOException
meth public static void linkFile(java.io.File,java.io.File) throws java.io.IOException
meth public static void mkDirs(java.io.File,java.lang.String[],java.io.File) throws java.io.IOException
meth public static void mkdir(java.lang.String)
meth public static void rename(java.io.File,java.io.File) throws java.io.IOException
supr java.lang.Object
hfds INVALID_CHARACTERS_FOR_WINDOWS_FILE_NAME

CLSS public abstract static org.codehaus.plexus.util.FileUtils$FilterWrapper
 outer org.codehaus.plexus.util.FileUtils
cons public init()
meth public abstract java.io.Reader getReader(java.io.Reader)
supr java.lang.Object

CLSS public final org.codehaus.plexus.util.IOUtil
meth public static boolean contentEquals(java.io.InputStream,java.io.InputStream) throws java.io.IOException
meth public static byte[] toByteArray(java.io.InputStream) throws java.io.IOException
meth public static byte[] toByteArray(java.io.InputStream,int) throws java.io.IOException
meth public static byte[] toByteArray(java.io.Reader) throws java.io.IOException
meth public static byte[] toByteArray(java.io.Reader,int) throws java.io.IOException
meth public static byte[] toByteArray(java.lang.String) throws java.io.IOException
meth public static byte[] toByteArray(java.lang.String,int) throws java.io.IOException
meth public static java.lang.String toString(byte[]) throws java.io.IOException
meth public static java.lang.String toString(byte[],int) throws java.io.IOException
meth public static java.lang.String toString(byte[],java.lang.String) throws java.io.IOException
meth public static java.lang.String toString(byte[],java.lang.String,int) throws java.io.IOException
meth public static java.lang.String toString(java.io.InputStream) throws java.io.IOException
meth public static java.lang.String toString(java.io.InputStream,int) throws java.io.IOException
meth public static java.lang.String toString(java.io.InputStream,java.lang.String) throws java.io.IOException
meth public static java.lang.String toString(java.io.InputStream,java.lang.String,int) throws java.io.IOException
meth public static java.lang.String toString(java.io.Reader) throws java.io.IOException
meth public static java.lang.String toString(java.io.Reader,int) throws java.io.IOException
meth public static void bufferedCopy(java.io.InputStream,java.io.OutputStream) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static void close(java.io.InputStream)
 anno 0 java.lang.Deprecated()
meth public static void close(java.io.OutputStream)
 anno 0 java.lang.Deprecated()
meth public static void close(java.io.Reader)
 anno 0 java.lang.Deprecated()
meth public static void close(java.io.Writer)
 anno 0 java.lang.Deprecated()
meth public static void close(java.nio.channels.Channel)
 anno 0 java.lang.Deprecated()
meth public static void copy(byte[],java.io.OutputStream) throws java.io.IOException
meth public static void copy(byte[],java.io.OutputStream,int) throws java.io.IOException
meth public static void copy(byte[],java.io.Writer) throws java.io.IOException
meth public static void copy(byte[],java.io.Writer,int) throws java.io.IOException
meth public static void copy(byte[],java.io.Writer,java.lang.String) throws java.io.IOException
meth public static void copy(byte[],java.io.Writer,java.lang.String,int) throws java.io.IOException
meth public static void copy(java.io.InputStream,java.io.OutputStream) throws java.io.IOException
meth public static void copy(java.io.InputStream,java.io.OutputStream,int) throws java.io.IOException
meth public static void copy(java.io.InputStream,java.io.Writer) throws java.io.IOException
meth public static void copy(java.io.InputStream,java.io.Writer,int) throws java.io.IOException
meth public static void copy(java.io.InputStream,java.io.Writer,java.lang.String) throws java.io.IOException
meth public static void copy(java.io.InputStream,java.io.Writer,java.lang.String,int) throws java.io.IOException
meth public static void copy(java.io.Reader,java.io.OutputStream) throws java.io.IOException
meth public static void copy(java.io.Reader,java.io.OutputStream,int) throws java.io.IOException
meth public static void copy(java.io.Reader,java.io.Writer) throws java.io.IOException
meth public static void copy(java.io.Reader,java.io.Writer,int) throws java.io.IOException
meth public static void copy(java.lang.String,java.io.OutputStream) throws java.io.IOException
meth public static void copy(java.lang.String,java.io.OutputStream,int) throws java.io.IOException
meth public static void copy(java.lang.String,java.io.Writer) throws java.io.IOException
supr java.lang.Object
hfds DEFAULT_BUFFER_SIZE

CLSS public org.codehaus.plexus.util.InterpolationFilterReader
cons public init(java.io.Reader,java.util.Map<?,java.lang.Object>,java.lang.String,java.lang.String)
cons public init(java.io.Reader,java.util.Map<java.lang.String,java.lang.Object>)
meth public int read() throws java.io.IOException
meth public int read(char[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
supr java.io.FilterReader
hfds DEFAULT_BEGIN_TOKEN,DEFAULT_END_TOKEN,beginToken,beginTokenLength,endToken,endTokenLength,previousIndex,replaceData,replaceIndex,variables

CLSS public org.codehaus.plexus.util.LineOrientedInterpolatingReader
cons public init(java.io.Reader,java.util.Map<java.lang.String,?>)
cons public init(java.io.Reader,java.util.Map<java.lang.String,?>,java.lang.String,java.lang.String)
cons public init(java.io.Reader,java.util.Map<java.lang.String,?>,java.lang.String,java.lang.String,java.lang.String)
fld public final static java.lang.String DEFAULT_END_DELIM = "}"
fld public final static java.lang.String DEFAULT_ESCAPE_SEQ = "\u005c"
fld public final static java.lang.String DEFAULT_START_DELIM = "${"
meth public int read() throws java.io.IOException
meth public int read(char[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
supr java.io.FilterReader
hfds CARRIAGE_RETURN_CHAR,NEWLINE_CHAR,context,endDelim,escapeSeq,line,lineIdx,minExpressionSize,pushbackReader,reflector,startDelim

CLSS public org.codehaus.plexus.util.MatchPattern
meth public boolean matchPath(java.lang.String,boolean)
meth public boolean matchPatternStart(java.lang.String,boolean)
meth public boolean startsWith(java.lang.String)
meth public char[][] getTokenizedPathChars()
meth public java.lang.String[] getTokenizedPathString()
meth public static org.codehaus.plexus.util.MatchPattern fromString(java.lang.String)
supr java.lang.Object
hfds regexPattern,separator,source,tokenized,tokenizedChar

CLSS public org.codehaus.plexus.util.MatchPatterns
meth public !varargs static org.codehaus.plexus.util.MatchPatterns from(java.lang.String[])
meth public boolean matches(java.lang.String,boolean)
meth public boolean matches(java.lang.String,char[][],boolean)
meth public boolean matches(java.lang.String,java.lang.String[],boolean)
meth public boolean matchesPatternStart(java.lang.String,boolean)
meth public static org.codehaus.plexus.util.MatchPatterns from(java.lang.Iterable<java.lang.String>)
supr java.lang.Object
hfds patterns

CLSS public org.codehaus.plexus.util.NioFiles
cons public init()
meth public static boolean deleteIfExists(java.io.File) throws java.io.IOException
meth public static boolean isSymbolicLink(java.io.File)
meth public static java.io.File copy(java.io.File,java.io.File) throws java.io.IOException
meth public static java.io.File createSymbolicLink(java.io.File,java.io.File) throws java.io.IOException
meth public static java.io.File readSymbolicLink(java.io.File) throws java.io.IOException
meth public static long getLastModified(java.io.File) throws java.io.IOException
meth public static void chmod(java.io.File,int) throws java.io.IOException
supr java.lang.Object

CLSS public org.codehaus.plexus.util.Os
cons public init()
cons public init(java.lang.String)
fld public final static java.lang.String FAMILY_DOS = "dos"
fld public final static java.lang.String FAMILY_MAC = "mac"
fld public final static java.lang.String FAMILY_NETWARE = "netware"
fld public final static java.lang.String FAMILY_OPENVMS = "openvms"
fld public final static java.lang.String FAMILY_OS2 = "os/2"
fld public final static java.lang.String FAMILY_OS400 = "os/400"
fld public final static java.lang.String FAMILY_TANDEM = "tandem"
fld public final static java.lang.String FAMILY_UNIX = "unix"
fld public final static java.lang.String FAMILY_WIN9X = "win9x"
fld public final static java.lang.String FAMILY_WINDOWS = "windows"
fld public final static java.lang.String FAMILY_ZOS = "z/os"
fld public final static java.lang.String OS_ARCH
fld public final static java.lang.String OS_FAMILY
fld public final static java.lang.String OS_NAME
fld public final static java.lang.String OS_VERSION
meth public boolean eval() throws java.lang.Exception
meth public static boolean isArch(java.lang.String)
meth public static boolean isFamily(java.lang.String)
meth public static boolean isName(java.lang.String)
meth public static boolean isOs(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public static boolean isValidFamily(java.lang.String)
meth public static boolean isVersion(java.lang.String)
meth public static java.util.Set<java.lang.String> getValidFamilies()
meth public void setArch(java.lang.String)
meth public void setFamily(java.lang.String)
meth public void setName(java.lang.String)
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds PATH_SEP,arch,family,name,validFamilies,version

CLSS public org.codehaus.plexus.util.PathTool
cons public init()
meth public final static java.lang.String calculateLink(java.lang.String,java.lang.String)
meth public final static java.lang.String getDirectoryComponent(java.lang.String)
meth public final static java.lang.String getRelativeFilePath(java.lang.String,java.lang.String)
meth public final static java.lang.String getRelativePath(java.lang.String)
meth public final static java.lang.String getRelativePath(java.lang.String,java.lang.String)
meth public final static java.lang.String getRelativeWebPath(java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public org.codehaus.plexus.util.PropertyUtils
cons public init()
meth public static java.util.Properties loadProperties(java.io.File) throws java.io.IOException
meth public static java.util.Properties loadProperties(java.io.InputStream) throws java.io.IOException
meth public static java.util.Properties loadProperties(java.net.URL) throws java.io.IOException
supr java.lang.Object

CLSS public org.codehaus.plexus.util.ReaderFactory
cons public init()
fld public final static java.lang.String FILE_ENCODING
fld public final static java.lang.String ISO_8859_1 = "ISO-8859-1"
fld public final static java.lang.String US_ASCII = "US-ASCII"
fld public final static java.lang.String UTF_16 = "UTF-16"
fld public final static java.lang.String UTF_16BE = "UTF-16BE"
fld public final static java.lang.String UTF_16LE = "UTF-16LE"
fld public final static java.lang.String UTF_8 = "UTF-8"
meth public static java.io.Reader newPlatformReader(java.io.File) throws java.io.IOException
meth public static java.io.Reader newPlatformReader(java.io.InputStream)
meth public static java.io.Reader newPlatformReader(java.net.URL) throws java.io.IOException
meth public static java.io.Reader newReader(java.io.File,java.lang.String) throws java.io.IOException
meth public static java.io.Reader newReader(java.io.InputStream,java.lang.String) throws java.io.UnsupportedEncodingException
meth public static java.io.Reader newReader(java.net.URL,java.lang.String) throws java.io.IOException
meth public static org.codehaus.plexus.util.xml.XmlStreamReader newXmlReader(java.io.File) throws java.io.IOException
meth public static org.codehaus.plexus.util.xml.XmlStreamReader newXmlReader(java.io.InputStream) throws java.io.IOException
meth public static org.codehaus.plexus.util.xml.XmlStreamReader newXmlReader(java.net.URL) throws java.io.IOException
supr java.lang.Object

CLSS public final org.codehaus.plexus.util.ReflectionUtils
cons public init()
meth public static boolean isSetter(java.lang.reflect.Method)
meth public static java.lang.Class<?> getSetterType(java.lang.reflect.Method)
meth public static java.lang.Object getValueIncludingSuperclasses(java.lang.String,java.lang.Object) throws java.lang.IllegalAccessException
meth public static java.lang.reflect.Field getFieldByNameIncludingSuperclasses(java.lang.String,java.lang.Class<?>)
meth public static java.lang.reflect.Method getSetter(java.lang.String,java.lang.Class<?>)
meth public static java.util.List<java.lang.reflect.Field> getFieldsIncludingSuperclasses(java.lang.Class<?>)
meth public static java.util.List<java.lang.reflect.Method> getSetters(java.lang.Class<?>)
meth public static java.util.Map<java.lang.String,java.lang.Object> getVariablesAndValuesIncludingSuperclasses(java.lang.Object) throws java.lang.IllegalAccessException
meth public static void setVariableValueInObject(java.lang.Object,java.lang.String,java.lang.Object) throws java.lang.IllegalAccessException
supr java.lang.Object

CLSS public abstract interface org.codehaus.plexus.util.Scanner
meth public abstract java.io.File getBasedir()
meth public abstract java.lang.String[] getIncludedDirectories()
meth public abstract java.lang.String[] getIncludedFiles()
meth public abstract void addDefaultExcludes()
meth public abstract void scan()
meth public abstract void setExcludes(java.lang.String[])
meth public abstract void setFilenameComparator(java.util.Comparator<java.lang.String>)
meth public abstract void setIncludes(java.lang.String[])

CLSS public final org.codehaus.plexus.util.SelectorUtils
fld public final static java.lang.String ANT_HANDLER_PREFIX = "%ant["
fld public final static java.lang.String PATTERN_HANDLER_PREFIX = "["
fld public final static java.lang.String PATTERN_HANDLER_SUFFIX = "]"
fld public final static java.lang.String REGEX_HANDLER_PREFIX = "%regex["
meth public static boolean isOutOfDate(java.io.File,java.io.File,int)
meth public static boolean match(char[],char[],boolean)
meth public static boolean match(java.lang.String,java.lang.String)
meth public static boolean match(java.lang.String,java.lang.String,boolean)
meth public static boolean matchPath(java.lang.String,java.lang.String)
meth public static boolean matchPath(java.lang.String,java.lang.String,boolean)
meth public static boolean matchPath(java.lang.String,java.lang.String,java.lang.String,boolean)
meth public static boolean matchPatternStart(java.lang.String,java.lang.String)
meth public static boolean matchPatternStart(java.lang.String,java.lang.String,boolean)
meth public static java.lang.String removeWhitespace(java.lang.String)
meth public static org.codehaus.plexus.util.SelectorUtils getInstance()
supr java.lang.Object
hfds instance

CLSS public org.codehaus.plexus.util.StringInputStream
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
meth public boolean markSupported()
meth public int read() throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.io.InputStream
hfds in

CLSS public org.codehaus.plexus.util.StringOutputStream
 anno 0 java.lang.Deprecated()
cons public init()
meth public java.lang.String toString()
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream
hfds buf

CLSS public org.codehaus.plexus.util.StringUtils
cons public init()
meth public static boolean contains(java.lang.String,char)
meth public static boolean contains(java.lang.String,java.lang.String)
meth public static boolean equals(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static boolean equalsIgnoreCase(java.lang.String,java.lang.String)
meth public static boolean isAlpha(java.lang.String)
meth public static boolean isAlphaSpace(java.lang.String)
meth public static boolean isAlphanumeric(java.lang.String)
meth public static boolean isAlphanumericSpace(java.lang.String)
meth public static boolean isBlank(java.lang.String)
meth public static boolean isEmpty(java.lang.String)
meth public static boolean isNotBlank(java.lang.String)
meth public static boolean isNotEmpty(java.lang.String)
meth public static boolean isNumeric(java.lang.String)
meth public static boolean isNumericSpace(java.lang.String)
meth public static boolean isWhitespace(java.lang.String)
meth public static int countMatches(java.lang.String,java.lang.String)
meth public static int differenceAt(java.lang.String,java.lang.String)
meth public static int indexOfAny(java.lang.String,java.lang.String[])
meth public static int lastIndexOfAny(java.lang.String,java.lang.String[])
meth public static java.lang.String abbreviate(java.lang.String,int)
meth public static java.lang.String abbreviate(java.lang.String,int,int)
meth public static java.lang.String addAndDeHump(java.lang.String)
meth public static java.lang.String capitalise(java.lang.String)
meth public static java.lang.String capitaliseAllWords(java.lang.String)
meth public static java.lang.String capitalizeFirstLetter(java.lang.String)
meth public static java.lang.String center(java.lang.String,int)
meth public static java.lang.String center(java.lang.String,int,java.lang.String)
meth public static java.lang.String chomp(java.lang.String)
meth public static java.lang.String chomp(java.lang.String,java.lang.String)
meth public static java.lang.String chompLast(java.lang.String)
meth public static java.lang.String chompLast(java.lang.String,java.lang.String)
meth public static java.lang.String chop(java.lang.String)
meth public static java.lang.String chopNewline(java.lang.String)
meth public static java.lang.String clean(java.lang.String)
meth public static java.lang.String concatenate(java.lang.Object[])
meth public static java.lang.String defaultString(java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String defaultString(java.lang.Object,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String deleteWhitespace(java.lang.String)
meth public static java.lang.String difference(java.lang.String,java.lang.String)
meth public static java.lang.String escape(java.lang.String)
meth public static java.lang.String escape(java.lang.String,char[],char)
meth public static java.lang.String escape(java.lang.String,char[],java.lang.String)
meth public static java.lang.String getChomp(java.lang.String,java.lang.String)
meth public static java.lang.String getNestedString(java.lang.String,java.lang.String)
meth public static java.lang.String getNestedString(java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String getPrechomp(java.lang.String,java.lang.String)
meth public static java.lang.String interpolate(java.lang.String,java.util.Map<?,?>)
meth public static java.lang.String join(java.lang.Object[],java.lang.String)
meth public static java.lang.String join(java.util.Iterator<?>,java.lang.String)
meth public static java.lang.String left(java.lang.String,int)
meth public static java.lang.String leftPad(java.lang.String,int)
meth public static java.lang.String leftPad(java.lang.String,int,java.lang.String)
meth public static java.lang.String lowerCase(java.lang.String)
meth public static java.lang.String lowercaseFirstLetter(java.lang.String)
meth public static java.lang.String mid(java.lang.String,int,int)
meth public static java.lang.String overlayString(java.lang.String,java.lang.String,int,int)
meth public static java.lang.String prechomp(java.lang.String,java.lang.String)
meth public static java.lang.String quoteAndEscape(java.lang.String,char)
meth public static java.lang.String quoteAndEscape(java.lang.String,char,char[])
meth public static java.lang.String quoteAndEscape(java.lang.String,char,char[],char,boolean)
meth public static java.lang.String quoteAndEscape(java.lang.String,char,char[],char[],char,boolean)
meth public static java.lang.String quoteAndEscape(java.lang.String,char,char[],char[],java.lang.String,boolean)
meth public static java.lang.String removeAndHump(java.lang.String,java.lang.String)
meth public static java.lang.String removeDuplicateWhitespace(java.lang.String)
meth public static java.lang.String repeat(java.lang.String,int)
meth public static java.lang.String replace(java.lang.String,char,char)
meth public static java.lang.String replace(java.lang.String,char,char,int)
meth public static java.lang.String replace(java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String replace(java.lang.String,java.lang.String,java.lang.String,int)
meth public static java.lang.String replaceOnce(java.lang.String,char,char)
meth public static java.lang.String replaceOnce(java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String reverse(java.lang.String)
meth public static java.lang.String reverseDelimitedString(java.lang.String,java.lang.String)
meth public static java.lang.String right(java.lang.String,int)
meth public static java.lang.String rightPad(java.lang.String,int)
meth public static java.lang.String rightPad(java.lang.String,int,java.lang.String)
meth public static java.lang.String strip(java.lang.String)
meth public static java.lang.String strip(java.lang.String,java.lang.String)
meth public static java.lang.String stripEnd(java.lang.String,java.lang.String)
meth public static java.lang.String stripStart(java.lang.String,java.lang.String)
meth public static java.lang.String substring(java.lang.String,int)
meth public static java.lang.String substring(java.lang.String,int,int)
meth public static java.lang.String swapCase(java.lang.String)
meth public static java.lang.String trim(java.lang.String)
meth public static java.lang.String uncapitalise(java.lang.String)
meth public static java.lang.String uncapitaliseAllWords(java.lang.String)
meth public static java.lang.String unifyLineSeparators(java.lang.String)
meth public static java.lang.String unifyLineSeparators(java.lang.String,java.lang.String)
meth public static java.lang.String upperCase(java.lang.String)
meth public static java.lang.String[] split(java.lang.String)
meth public static java.lang.String[] split(java.lang.String,java.lang.String)
meth public static java.lang.String[] split(java.lang.String,java.lang.String,int)
meth public static java.lang.String[] stripAll(java.lang.String[])
meth public static java.lang.String[] stripAll(java.lang.String[],java.lang.String)
supr java.lang.Object

CLSS public org.codehaus.plexus.util.SweeperPool
cons public init(int,int,int,int,int)
meth public boolean put(java.lang.Object)
meth public int getSize()
meth public java.lang.Object get()
meth public void dispose()
meth public void objectAdded(java.lang.Object)
meth public void objectDisposed(java.lang.Object)
meth public void objectRetrieved(java.lang.Object)
meth public void trim()
supr java.lang.Object
hfds DEBUG,maxSize,minSize,pooledObjects,shuttingDown,sweeper,triggerSize
hcls Sweeper

CLSS public final org.codehaus.plexus.util.TypeFormat
meth public static boolean parseBoolean(java.lang.CharSequence)
meth public static double parseDouble(java.lang.CharSequence)
meth public static float parseFloat(java.lang.CharSequence)
meth public static int indexOf(java.lang.CharSequence,java.lang.CharSequence,int)
meth public static int parseInt(java.lang.CharSequence)
meth public static int parseInt(java.lang.CharSequence,int)
meth public static java.lang.StringBuffer format(boolean,java.lang.StringBuffer)
meth public static java.lang.StringBuffer format(double,double,java.lang.StringBuffer)
meth public static java.lang.StringBuffer format(double,int,java.lang.StringBuffer)
meth public static java.lang.StringBuffer format(double,java.lang.StringBuffer)
meth public static java.lang.StringBuffer format(float,float,java.lang.StringBuffer)
meth public static java.lang.StringBuffer format(float,java.lang.StringBuffer)
meth public static java.lang.StringBuffer format(int,int,java.lang.StringBuffer)
meth public static java.lang.StringBuffer format(int,java.lang.StringBuffer)
meth public static java.lang.StringBuffer format(long,int,java.lang.StringBuffer)
meth public static java.lang.StringBuffer format(long,java.lang.StringBuffer)
meth public static java.lang.StringBuffer format(short,int,java.lang.StringBuffer)
meth public static java.lang.StringBuffer format(short,java.lang.StringBuffer)
meth public static long parseLong(java.lang.CharSequence)
meth public static long parseLong(java.lang.CharSequence,int)
meth public static short parseShort(java.lang.CharSequence)
meth public static short parseShort(java.lang.CharSequence,int)
supr java.lang.Object
hfds DIGITS,DOUBLE_POW_10,DOUBLE_RELATIVE_ERROR,FLOAT_RELATIVE_ERROR,INT_POW_10,LEADING_ZEROS,LOG_10,LONG_POW_10

CLSS public org.codehaus.plexus.util.WriterFactory
cons public init()
fld public final static java.lang.String FILE_ENCODING
fld public final static java.lang.String ISO_8859_1 = "ISO-8859-1"
fld public final static java.lang.String US_ASCII = "US-ASCII"
fld public final static java.lang.String UTF_16 = "UTF-16"
fld public final static java.lang.String UTF_16BE = "UTF-16BE"
fld public final static java.lang.String UTF_16LE = "UTF-16LE"
fld public final static java.lang.String UTF_8 = "UTF-8"
meth public static java.io.Writer newPlatformWriter(java.io.File) throws java.io.IOException
meth public static java.io.Writer newPlatformWriter(java.io.OutputStream)
meth public static java.io.Writer newWriter(java.io.File,java.lang.String) throws java.io.IOException
meth public static java.io.Writer newWriter(java.io.OutputStream,java.lang.String) throws java.io.UnsupportedEncodingException
meth public static org.codehaus.plexus.util.xml.XmlStreamWriter newXmlWriter(java.io.File) throws java.io.IOException
meth public static org.codehaus.plexus.util.xml.XmlStreamWriter newXmlWriter(java.io.OutputStream) throws java.io.IOException
supr java.lang.Object

CLSS public org.codehaus.plexus.util.cli.AbstractStreamHandler
cons public init()
meth protected boolean isDisabled()
meth public boolean isDone()
meth public void disable()
meth public void setDone()
meth public void waitUntilDone() throws java.lang.InterruptedException
supr java.lang.Thread
hfds disabled,done

CLSS public abstract interface org.codehaus.plexus.util.cli.Arg
meth public abstract java.lang.String[] getParts()
meth public abstract void setFile(java.io.File)
meth public abstract void setLine(java.lang.String)
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.codehaus.plexus.util.cli.CommandLineCallable
intf java.util.concurrent.Callable<java.lang.Integer>
meth public abstract java.lang.Integer call() throws org.codehaus.plexus.util.cli.CommandLineException

CLSS public org.codehaus.plexus.util.cli.CommandLineException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception

CLSS public org.codehaus.plexus.util.cli.CommandLineTimeOutException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr org.codehaus.plexus.util.cli.CommandLineException

CLSS public abstract org.codehaus.plexus.util.cli.CommandLineUtils
cons public init()
innr public static StringStreamConsumer
meth public static boolean isAlive(java.lang.Process)
meth public static int executeCommandLine(org.codehaus.plexus.util.cli.Commandline,java.io.InputStream,org.codehaus.plexus.util.cli.StreamConsumer,org.codehaus.plexus.util.cli.StreamConsumer) throws org.codehaus.plexus.util.cli.CommandLineException
meth public static int executeCommandLine(org.codehaus.plexus.util.cli.Commandline,java.io.InputStream,org.codehaus.plexus.util.cli.StreamConsumer,org.codehaus.plexus.util.cli.StreamConsumer,int) throws org.codehaus.plexus.util.cli.CommandLineException
meth public static int executeCommandLine(org.codehaus.plexus.util.cli.Commandline,org.codehaus.plexus.util.cli.StreamConsumer,org.codehaus.plexus.util.cli.StreamConsumer) throws org.codehaus.plexus.util.cli.CommandLineException
meth public static int executeCommandLine(org.codehaus.plexus.util.cli.Commandline,org.codehaus.plexus.util.cli.StreamConsumer,org.codehaus.plexus.util.cli.StreamConsumer,int) throws org.codehaus.plexus.util.cli.CommandLineException
meth public static java.lang.String quote(java.lang.String) throws org.codehaus.plexus.util.cli.CommandLineException
 anno 0 java.lang.Deprecated()
meth public static java.lang.String quote(java.lang.String,boolean) throws org.codehaus.plexus.util.cli.CommandLineException
 anno 0 java.lang.Deprecated()
meth public static java.lang.String quote(java.lang.String,boolean,boolean,boolean) throws org.codehaus.plexus.util.cli.CommandLineException
 anno 0 java.lang.Deprecated()
meth public static java.lang.String toString(java.lang.String[])
meth public static java.lang.String[] translateCommandline(java.lang.String) throws java.lang.Exception
meth public static java.util.Properties getSystemEnvVars()
meth public static java.util.Properties getSystemEnvVars(boolean)
meth public static org.codehaus.plexus.util.cli.CommandLineCallable executeCommandLineAsCallable(org.codehaus.plexus.util.cli.Commandline,java.io.InputStream,org.codehaus.plexus.util.cli.StreamConsumer,org.codehaus.plexus.util.cli.StreamConsumer,int) throws org.codehaus.plexus.util.cli.CommandLineException
supr java.lang.Object
hfds MILLIS_PER_SECOND,NANOS_PER_SECOND

CLSS public static org.codehaus.plexus.util.cli.CommandLineUtils$StringStreamConsumer
 outer org.codehaus.plexus.util.cli.CommandLineUtils
cons public init()
intf org.codehaus.plexus.util.cli.StreamConsumer
meth public java.lang.String getOutput()
meth public void consumeLine(java.lang.String)
supr java.lang.Object
hfds ls,string

CLSS public org.codehaus.plexus.util.cli.Commandline
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,org.codehaus.plexus.util.cli.shell.Shell)
cons public init(org.codehaus.plexus.util.cli.shell.Shell)
fld protected final static java.lang.String OS_NAME = "os.name"
 anno 0 java.lang.Deprecated()
fld protected final static java.lang.String WINDOWS = "Windows"
 anno 0 java.lang.Deprecated()
fld protected java.lang.String executable
 anno 0 java.lang.Deprecated()
fld protected java.util.Map<java.lang.String,java.lang.String> envVars
fld protected java.util.Vector<org.codehaus.plexus.util.cli.Arg> arguments
innr public Marker
innr public static Argument
intf java.lang.Cloneable
meth public int size()
meth public java.io.File getWorkingDirectory()
meth public java.lang.Object clone()
meth public java.lang.Process execute() throws org.codehaus.plexus.util.cli.CommandLineException
meth public java.lang.String getExecutable()
meth public java.lang.String getLiteralExecutable()
meth public java.lang.String toString()
meth public java.lang.String[] getArguments()
meth public java.lang.String[] getCommandline()
meth public java.lang.String[] getEnvironmentVariables() throws org.codehaus.plexus.util.cli.CommandLineException
meth public java.lang.String[] getRawCommandline()
meth public java.lang.String[] getShellCommandline()
meth public java.util.Properties getSystemEnvVars() throws java.lang.Exception
meth public long getPid()
meth public org.codehaus.plexus.util.cli.Arg createArg()
meth public org.codehaus.plexus.util.cli.Arg createArg(boolean)
meth public org.codehaus.plexus.util.cli.Commandline$Argument createArgument()
 anno 0 java.lang.Deprecated()
meth public org.codehaus.plexus.util.cli.Commandline$Argument createArgument(boolean)
 anno 0 java.lang.Deprecated()
meth public org.codehaus.plexus.util.cli.Commandline$Marker createMarker()
meth public org.codehaus.plexus.util.cli.shell.Shell getShell()
meth public static java.lang.String quoteArgument(java.lang.String) throws org.codehaus.plexus.util.cli.CommandLineException
 anno 0 java.lang.Deprecated()
meth public static java.lang.String toString(java.lang.String[])
 anno 0 java.lang.Deprecated()
meth public static java.lang.String[] translateCommandline(java.lang.String) throws java.lang.Exception
 anno 0 java.lang.Deprecated()
meth public void addArg(org.codehaus.plexus.util.cli.Arg)
meth public void addArg(org.codehaus.plexus.util.cli.Arg,boolean)
meth public void addArguments(java.lang.String[])
meth public void addEnvironment(java.lang.String,java.lang.String)
meth public void addSystemEnvironment() throws java.lang.Exception
meth public void clear()
meth public void clearArgs()
meth public void setExecutable(java.lang.String)
meth public void setPid(long)
meth public void setShell(org.codehaus.plexus.util.cli.shell.Shell)
meth public void setWorkingDirectory(java.io.File)
meth public void setWorkingDirectory(java.lang.String)
supr java.lang.Object
hfds pid,shell,workingDir

CLSS public static org.codehaus.plexus.util.cli.Commandline$Argument
 outer org.codehaus.plexus.util.cli.Commandline
cons public init()
intf org.codehaus.plexus.util.cli.Arg
meth public java.lang.String[] getParts()
meth public void setFile(java.io.File)
meth public void setLine(java.lang.String)
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds parts

CLSS public org.codehaus.plexus.util.cli.Commandline$Marker
 outer org.codehaus.plexus.util.cli.Commandline
meth public int getPosition()
supr java.lang.Object
hfds position,realPos

CLSS public org.codehaus.plexus.util.cli.DefaultConsumer
cons public init()
intf org.codehaus.plexus.util.cli.StreamConsumer
meth public void consumeLine(java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public final org.codehaus.plexus.util.cli.EnhancedStringTokenizer
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,boolean)
meth public boolean hasMoreTokens()
meth public java.lang.String nextToken()
supr java.lang.Object
hfds cdelim,cdelimChar,cdelimSingleChar,creturnDelims,cst,delimLast,lastToken

CLSS public abstract interface org.codehaus.plexus.util.cli.StreamConsumer
meth public abstract void consumeLine(java.lang.String) throws java.io.IOException

CLSS public org.codehaus.plexus.util.cli.StreamFeeder
cons public init(java.io.InputStream,java.io.OutputStream)
meth public java.lang.Throwable getException()
meth public void close()
meth public void run()
supr org.codehaus.plexus.util.cli.AbstractStreamHandler
hfds exception,input,output

CLSS public org.codehaus.plexus.util.cli.StreamPumper
cons public init(java.io.InputStream)
cons public init(java.io.InputStream,java.io.PrintWriter)
cons public init(java.io.InputStream,java.io.PrintWriter,org.codehaus.plexus.util.cli.StreamConsumer)
cons public init(java.io.InputStream,org.codehaus.plexus.util.cli.StreamConsumer)
meth public java.lang.Exception getException()
meth public void close()
meth public void flush()
meth public void run()
supr org.codehaus.plexus.util.cli.AbstractStreamHandler
hfds SIZE,consumer,exception,in,out

CLSS public org.codehaus.plexus.util.cli.WriterStreamConsumer
cons public init(java.io.Writer)
intf org.codehaus.plexus.util.cli.StreamConsumer
meth public void consumeLine(java.lang.String)
supr java.lang.Object
hfds writer

CLSS public org.codehaus.plexus.util.dag.CycleDetectedException
cons public init(java.lang.String,java.util.List<java.lang.String>)
meth public java.lang.String cycleToString()
meth public java.lang.String getMessage()
meth public java.util.List<java.lang.String> getCycle()
supr java.lang.Exception
hfds cycle

CLSS public org.codehaus.plexus.util.dag.CycleDetector
cons public init()
meth public static java.util.List<java.lang.String> hasCycle(org.codehaus.plexus.util.dag.DAG)
meth public static java.util.List<java.lang.String> introducesCycle(org.codehaus.plexus.util.dag.Vertex)
meth public static java.util.List<java.lang.String> introducesCycle(org.codehaus.plexus.util.dag.Vertex,java.util.Map<org.codehaus.plexus.util.dag.Vertex,java.lang.Integer>)
supr java.lang.Object
hfds NOT_VISITED,VISITED,VISITING

CLSS public org.codehaus.plexus.util.dag.DAG
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean hasEdge(java.lang.String,java.lang.String)
meth public boolean isConnected(java.lang.String)
meth public java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public java.util.List<java.lang.String> getChildLabels(java.lang.String)
meth public java.util.List<java.lang.String> getParentLabels(java.lang.String)
meth public java.util.List<java.lang.String> getSuccessorLabels(java.lang.String)
meth public java.util.List<org.codehaus.plexus.util.dag.Vertex> getVertices()
meth public java.util.List<org.codehaus.plexus.util.dag.Vertex> getVerticies()
 anno 0 java.lang.Deprecated()
meth public java.util.Set<java.lang.String> getLabels()
meth public org.codehaus.plexus.util.dag.Vertex addVertex(java.lang.String)
meth public org.codehaus.plexus.util.dag.Vertex getVertex(java.lang.String)
meth public void addEdge(java.lang.String,java.lang.String) throws org.codehaus.plexus.util.dag.CycleDetectedException
meth public void addEdge(org.codehaus.plexus.util.dag.Vertex,org.codehaus.plexus.util.dag.Vertex) throws org.codehaus.plexus.util.dag.CycleDetectedException
meth public void removeEdge(java.lang.String,java.lang.String)
meth public void removeEdge(org.codehaus.plexus.util.dag.Vertex,org.codehaus.plexus.util.dag.Vertex)
supr java.lang.Object
hfds vertexList,vertexMap

CLSS public org.codehaus.plexus.util.dag.TopologicalSorter
cons public init()
meth public static java.util.List<java.lang.String> sort(org.codehaus.plexus.util.dag.DAG)
meth public static java.util.List<java.lang.String> sort(org.codehaus.plexus.util.dag.Vertex)
supr java.lang.Object
hfds NOT_VISITED,VISITED,VISITING

CLSS public org.codehaus.plexus.util.dag.Vertex
cons public init(java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean isConnected()
meth public boolean isLeaf()
meth public boolean isRoot()
meth public java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public java.lang.String getLabel()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getChildLabels()
meth public java.util.List<java.lang.String> getParentLabels()
meth public java.util.List<org.codehaus.plexus.util.dag.Vertex> getChildren()
meth public java.util.List<org.codehaus.plexus.util.dag.Vertex> getParents()
meth public void addEdgeFrom(org.codehaus.plexus.util.dag.Vertex)
meth public void addEdgeTo(org.codehaus.plexus.util.dag.Vertex)
meth public void removeEdgeFrom(org.codehaus.plexus.util.dag.Vertex)
meth public void removeEdgeTo(org.codehaus.plexus.util.dag.Vertex)
supr java.lang.Object
hfds children,label,parents

CLSS public org.codehaus.plexus.util.introspection.ClassMap
cons public init(java.lang.Class)
meth public java.lang.reflect.Method findMethod(java.lang.String,java.lang.Object[]) throws org.codehaus.plexus.util.introspection.MethodMap$AmbiguousException
meth public static java.lang.reflect.Method getPublicMethod(java.lang.reflect.Method)
supr java.lang.Object
hfds CACHE_MISS,OBJECT,clazz,methodCache,methodMap
hcls CacheMiss,MethodInfo

CLSS public org.codehaus.plexus.util.introspection.MethodMap
cons public init()
innr public static AmbiguousException
meth public java.lang.reflect.Method find(java.lang.String,java.lang.Object[]) throws org.codehaus.plexus.util.introspection.MethodMap$AmbiguousException
meth public java.util.List<java.lang.reflect.Method> get(java.lang.String)
meth public void add(java.lang.reflect.Method)
supr java.lang.Object
hfds INCOMPARABLE,LESS_SPECIFIC,MORE_SPECIFIC,methodByNameMap

CLSS public static org.codehaus.plexus.util.introspection.MethodMap$AmbiguousException
 outer org.codehaus.plexus.util.introspection.MethodMap
cons public init()
supr java.lang.Exception

CLSS public org.codehaus.plexus.util.introspection.ReflectionValueExtractor
meth public static java.lang.Object evaluate(java.lang.String,java.lang.Object) throws java.lang.Exception
meth public static java.lang.Object evaluate(java.lang.String,java.lang.Object,boolean) throws java.lang.Exception
supr java.lang.Object
hfds CLASS_ARGS,EOF,INDEXED_END,INDEXED_START,MAPPED_END,MAPPED_START,OBJECT_ARGS,PROPERTY_START,classMaps
hcls Tokenizer

CLSS public org.codehaus.plexus.util.xml.CompactXMLWriter
cons public init(java.io.PrintWriter)
cons public init(java.io.Writer)
meth protected void endOfLine()
supr org.codehaus.plexus.util.xml.PrettyPrintXMLWriter

CLSS public org.codehaus.plexus.util.xml.PrettyPrintXMLWriter
cons public init(java.io.PrintWriter)
cons public init(java.io.PrintWriter,java.lang.String)
cons public init(java.io.PrintWriter,java.lang.String,java.lang.String)
cons public init(java.io.PrintWriter,java.lang.String,java.lang.String,java.lang.String)
cons public init(java.io.PrintWriter,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
cons public init(java.io.Writer)
cons public init(java.io.Writer,java.lang.String)
cons public init(java.io.Writer,java.lang.String,java.lang.String)
cons public init(java.io.Writer,java.lang.String,java.lang.String,java.lang.String)
fld protected final static java.lang.String LS
intf org.codehaus.plexus.util.xml.XMLWriter
meth protected int getDepth()
meth protected java.io.PrintWriter getWriter()
meth protected java.lang.String getDocType()
meth protected java.lang.String getEncoding()
meth protected java.lang.String getLineIndenter()
meth protected java.lang.String getLineSeparator()
meth protected java.util.LinkedList<java.lang.String> getElementStack()
meth protected void endOfLine()
meth protected void setDepth(int)
meth protected void setDocType(java.lang.String)
meth protected void setEncoding(java.lang.String)
meth protected void setLineIndenter(java.lang.String)
meth protected void setLineSeparator(java.lang.String)
meth protected void setWriter(java.io.PrintWriter)
meth public void addAttribute(java.lang.String,java.lang.String)
meth public void endElement()
meth public void startElement(java.lang.String)
meth public void writeMarkup(java.lang.String)
meth public void writeText(java.lang.String)
supr java.lang.Object
hfds amp,crlf,crlf_str,depth,docType,dqoute,elementStack,encoding,gt,lineIndenter,lineSeparator,lowers,lt,readyForNewLine,sqoute,tagInProgress,tagIsEmpty,writer

CLSS public org.codehaus.plexus.util.xml.SerializerXMLWriter
cons public init(java.lang.String,org.codehaus.plexus.util.xml.pull.XmlSerializer)
intf org.codehaus.plexus.util.xml.XMLWriter
meth public java.util.List<java.lang.Exception> getExceptions()
meth public void addAttribute(java.lang.String,java.lang.String)
meth public void endElement()
meth public void startElement(java.lang.String)
meth public void writeMarkup(java.lang.String)
meth public void writeText(java.lang.String)
supr java.lang.Object
hfds elements,exceptions,namespace,serializer

CLSS public abstract interface org.codehaus.plexus.util.xml.XMLWriter
meth public abstract void addAttribute(java.lang.String,java.lang.String)
meth public abstract void endElement()
meth public abstract void startElement(java.lang.String)
meth public abstract void writeMarkup(java.lang.String)
meth public abstract void writeText(java.lang.String)

CLSS public org.codehaus.plexus.util.xml.XmlReader
 anno 0 java.lang.Deprecated()
cons public init(java.io.File) throws java.io.IOException
cons public init(java.io.InputStream) throws java.io.IOException
cons public init(java.io.InputStream,boolean) throws java.io.IOException
cons public init(java.io.InputStream,java.lang.String) throws java.io.IOException
cons public init(java.io.InputStream,java.lang.String,boolean) throws java.io.IOException
cons public init(java.io.InputStream,java.lang.String,boolean,java.lang.String) throws java.io.IOException
cons public init(java.net.URL) throws java.io.IOException
cons public init(java.net.URLConnection) throws java.io.IOException
meth public int read(char[],int,int) throws java.io.IOException
meth public java.lang.String getEncoding()
meth public static java.lang.String getDefaultEncoding()
meth public static void setDefaultEncoding(java.lang.String)
meth public void close() throws java.io.IOException
supr java.io.Reader
hfds BUFFER_SIZE,CHARSET_PATTERN,EBCDIC,ENCODING_PATTERN,HTTP_EX_1,HTTP_EX_2,HTTP_EX_3,RAW_EX_1,RAW_EX_2,US_ASCII,UTF_16,UTF_16BE,UTF_16LE,UTF_8,_defaultEncoding,_encoding,_reader,_staticDefaultEncoding

CLSS public org.codehaus.plexus.util.xml.XmlReaderException
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.io.InputStream)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.io.InputStream)
meth public java.io.InputStream getInputStream()
meth public java.lang.String getBomEncoding()
meth public java.lang.String getContentTypeEncoding()
meth public java.lang.String getContentTypeMime()
meth public java.lang.String getXmlEncoding()
meth public java.lang.String getXmlGuessEncoding()
supr java.io.IOException
hfds _bomEncoding,_contentTypeEncoding,_contentTypeMime,_is,_xmlEncoding,_xmlGuessEncoding

CLSS public org.codehaus.plexus.util.xml.XmlStreamReader
cons public init(java.io.File) throws java.io.IOException
cons public init(java.io.InputStream) throws java.io.IOException
cons public init(java.io.InputStream,boolean) throws java.io.IOException
cons public init(java.io.InputStream,java.lang.String) throws java.io.IOException
cons public init(java.io.InputStream,java.lang.String,boolean) throws java.io.IOException
cons public init(java.io.InputStream,java.lang.String,boolean,java.lang.String) throws java.io.IOException
cons public init(java.net.URL) throws java.io.IOException
cons public init(java.net.URLConnection) throws java.io.IOException
supr org.codehaus.plexus.util.xml.XmlReader

CLSS public org.codehaus.plexus.util.xml.XmlStreamReaderException
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.io.InputStream)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.io.InputStream)
supr org.codehaus.plexus.util.xml.XmlReaderException

CLSS public org.codehaus.plexus.util.xml.XmlStreamWriter
cons public init(java.io.File) throws java.io.IOException
cons public init(java.io.OutputStream)
meth public java.lang.String getEncoding()
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(char[],int,int) throws java.io.IOException
supr java.io.Writer
hfds BUFFER_SIZE,ENCODING_PATTERN,encoding,out,writer,xmlPrologWriter

CLSS public org.codehaus.plexus.util.xml.XmlUtil
cons public init()
fld public final static int DEFAULT_INDENTATION_SIZE = 2
fld public final static java.lang.String DEFAULT_LINE_SEPARATOR
meth public static boolean isXml(java.io.File)
meth public static void prettyFormat(java.io.InputStream,java.io.OutputStream) throws java.io.IOException
meth public static void prettyFormat(java.io.InputStream,java.io.OutputStream,int,java.lang.String) throws java.io.IOException
meth public static void prettyFormat(java.io.Reader,java.io.Writer) throws java.io.IOException
meth public static void prettyFormat(java.io.Reader,java.io.Writer,int,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public org.codehaus.plexus.util.xml.XmlWriterUtil
cons public init()
fld public final static int DEFAULT_COLUMN_LINE = 80
fld public final static int DEFAULT_INDENTATION_SIZE = 2
fld public final static java.lang.String LS
meth public static void writeComment(org.codehaus.plexus.util.xml.XMLWriter,java.lang.String)
meth public static void writeComment(org.codehaus.plexus.util.xml.XMLWriter,java.lang.String,int)
meth public static void writeComment(org.codehaus.plexus.util.xml.XMLWriter,java.lang.String,int,int)
meth public static void writeComment(org.codehaus.plexus.util.xml.XMLWriter,java.lang.String,int,int,int)
meth public static void writeCommentLineBreak(org.codehaus.plexus.util.xml.XMLWriter)
meth public static void writeCommentLineBreak(org.codehaus.plexus.util.xml.XMLWriter,int)
meth public static void writeCommentText(org.codehaus.plexus.util.xml.XMLWriter,java.lang.String)
meth public static void writeCommentText(org.codehaus.plexus.util.xml.XMLWriter,java.lang.String,int)
meth public static void writeCommentText(org.codehaus.plexus.util.xml.XMLWriter,java.lang.String,int,int)
meth public static void writeCommentText(org.codehaus.plexus.util.xml.XMLWriter,java.lang.String,int,int,int)
meth public static void writeLineBreak(org.codehaus.plexus.util.xml.XMLWriter)
meth public static void writeLineBreak(org.codehaus.plexus.util.xml.XMLWriter,int)
meth public static void writeLineBreak(org.codehaus.plexus.util.xml.XMLWriter,int,int)
meth public static void writeLineBreak(org.codehaus.plexus.util.xml.XMLWriter,int,int,int)
supr java.lang.Object

CLSS public org.codehaus.plexus.util.xml.Xpp3Dom
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Object)
cons public init(org.codehaus.plexus.util.xml.Xpp3Dom)
cons public init(org.codehaus.plexus.util.xml.Xpp3Dom,java.lang.String)
fld protected final java.util.List<org.codehaus.plexus.util.xml.Xpp3Dom> childList
fld protected java.lang.Object inputLocation
fld protected java.lang.String name
fld protected java.lang.String value
fld protected java.util.Map<java.lang.String,java.lang.String> attributes
fld protected org.codehaus.plexus.util.xml.Xpp3Dom parent
fld public final static java.lang.String CHILDREN_COMBINATION_APPEND = "append"
fld public final static java.lang.String CHILDREN_COMBINATION_MERGE = "merge"
fld public final static java.lang.String CHILDREN_COMBINATION_MODE_ATTRIBUTE = "combine.children"
fld public final static java.lang.String DEFAULT_CHILDREN_COMBINATION_MODE = "merge"
fld public final static java.lang.String DEFAULT_SELF_COMBINATION_MODE = "merge"
fld public final static java.lang.String SELF_COMBINATION_MERGE = "merge"
fld public final static java.lang.String SELF_COMBINATION_MODE_ATTRIBUTE = "combine.self"
fld public final static java.lang.String SELF_COMBINATION_OVERRIDE = "override"
fld public final static java.lang.String SELF_COMBINATION_REMOVE = "remove"
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public boolean removeAttribute(java.lang.String)
meth public int getChildCount()
meth public int hashCode()
meth public java.lang.Object getInputLocation()
meth public java.lang.String getAttribute(java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public java.lang.String toUnescapedString()
meth public java.lang.String[] getAttributeNames()
meth public org.codehaus.plexus.util.xml.Xpp3Dom getChild(int)
meth public org.codehaus.plexus.util.xml.Xpp3Dom getChild(java.lang.String)
meth public org.codehaus.plexus.util.xml.Xpp3Dom getParent()
meth public org.codehaus.plexus.util.xml.Xpp3Dom[] getChildren()
meth public org.codehaus.plexus.util.xml.Xpp3Dom[] getChildren(java.lang.String)
meth public static boolean isEmpty(java.lang.String)
meth public static boolean isNotEmpty(java.lang.String)
meth public static org.codehaus.plexus.util.xml.Xpp3Dom mergeXpp3Dom(org.codehaus.plexus.util.xml.Xpp3Dom,org.codehaus.plexus.util.xml.Xpp3Dom)
meth public static org.codehaus.plexus.util.xml.Xpp3Dom mergeXpp3Dom(org.codehaus.plexus.util.xml.Xpp3Dom,org.codehaus.plexus.util.xml.Xpp3Dom,java.lang.Boolean)
meth public void addChild(org.codehaus.plexus.util.xml.Xpp3Dom)
meth public void removeChild(int)
meth public void removeChild(org.codehaus.plexus.util.xml.Xpp3Dom)
meth public void setAttribute(java.lang.String,java.lang.String)
meth public void setInputLocation(java.lang.Object)
meth public void setParent(org.codehaus.plexus.util.xml.Xpp3Dom)
meth public void setValue(java.lang.String)
meth public void writeToSerializer(java.lang.String,org.codehaus.plexus.util.xml.pull.XmlSerializer) throws java.io.IOException
supr java.lang.Object
hfds EMPTY_DOM_ARRAY,EMPTY_STRING_ARRAY,serialVersionUID

CLSS public org.codehaus.plexus.util.xml.Xpp3DomBuilder
cons public init()
innr public abstract interface static InputLocationBuilder
meth public static org.codehaus.plexus.util.xml.Xpp3Dom build(java.io.InputStream,java.lang.String) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public static org.codehaus.plexus.util.xml.Xpp3Dom build(java.io.InputStream,java.lang.String,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public static org.codehaus.plexus.util.xml.Xpp3Dom build(java.io.Reader) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public static org.codehaus.plexus.util.xml.Xpp3Dom build(java.io.Reader,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public static org.codehaus.plexus.util.xml.Xpp3Dom build(java.io.Reader,boolean,org.codehaus.plexus.util.xml.Xpp3DomBuilder$InputLocationBuilder) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public static org.codehaus.plexus.util.xml.Xpp3Dom build(java.io.Reader,org.codehaus.plexus.util.xml.Xpp3DomBuilder$InputLocationBuilder) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public static org.codehaus.plexus.util.xml.Xpp3Dom build(org.codehaus.plexus.util.xml.pull.XmlPullParser) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public static org.codehaus.plexus.util.xml.Xpp3Dom build(org.codehaus.plexus.util.xml.pull.XmlPullParser,boolean) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public static org.codehaus.plexus.util.xml.Xpp3Dom build(org.codehaus.plexus.util.xml.pull.XmlPullParser,boolean,org.codehaus.plexus.util.xml.Xpp3DomBuilder$InputLocationBuilder) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
supr java.lang.Object
hfds DEFAULT_TRIM

CLSS public abstract interface static org.codehaus.plexus.util.xml.Xpp3DomBuilder$InputLocationBuilder
 outer org.codehaus.plexus.util.xml.Xpp3DomBuilder
meth public abstract java.lang.Object toInputLocation(org.codehaus.plexus.util.xml.pull.XmlPullParser)

CLSS public org.codehaus.plexus.util.xml.Xpp3DomUtils
cons public init()
fld public final static java.lang.String CHILDREN_COMBINATION_APPEND = "append"
fld public final static java.lang.String CHILDREN_COMBINATION_MERGE = "merge"
fld public final static java.lang.String CHILDREN_COMBINATION_MODE_ATTRIBUTE = "combine.children"
fld public final static java.lang.String DEFAULT_CHILDREN_COMBINATION_MODE = "merge"
fld public final static java.lang.String DEFAULT_SELF_COMBINATION_MODE = "merge"
fld public final static java.lang.String ID_COMBINATION_MODE_ATTRIBUTE = "combine.id"
fld public final static java.lang.String KEYS_COMBINATION_MODE_ATTRIBUTE = "combine.keys"
fld public final static java.lang.String SELF_COMBINATION_MERGE = "merge"
fld public final static java.lang.String SELF_COMBINATION_MODE_ATTRIBUTE = "combine.self"
fld public final static java.lang.String SELF_COMBINATION_OVERRIDE = "override"
meth public static boolean isEmpty(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static boolean isNotEmpty(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.plexus.util.xml.Xpp3Dom mergeXpp3Dom(org.codehaus.plexus.util.xml.Xpp3Dom,org.codehaus.plexus.util.xml.Xpp3Dom)
meth public static org.codehaus.plexus.util.xml.Xpp3Dom mergeXpp3Dom(org.codehaus.plexus.util.xml.Xpp3Dom,org.codehaus.plexus.util.xml.Xpp3Dom,java.lang.Boolean)
meth public void writeToSerializer(java.lang.String,org.codehaus.plexus.util.xml.pull.XmlSerializer,org.codehaus.plexus.util.xml.Xpp3Dom) throws java.io.IOException
supr java.lang.Object

CLSS public org.codehaus.plexus.util.xml.Xpp3DomWriter
cons public init()
meth public static void write(java.io.PrintWriter,org.codehaus.plexus.util.xml.Xpp3Dom)
meth public static void write(java.io.Writer,org.codehaus.plexus.util.xml.Xpp3Dom)
meth public static void write(org.codehaus.plexus.util.xml.XMLWriter,org.codehaus.plexus.util.xml.Xpp3Dom)
meth public static void write(org.codehaus.plexus.util.xml.XMLWriter,org.codehaus.plexus.util.xml.Xpp3Dom,boolean)
supr java.lang.Object

CLSS public org.codehaus.plexus.util.xml.pull.EntityReplacementMap
cons public init(java.lang.String[][])
fld public final static org.codehaus.plexus.util.xml.pull.EntityReplacementMap defaultEntityReplacementMap
supr java.lang.Object
hfds entityEnd,entityName,entityNameBuf,entityNameHash,entityReplacement,entityReplacementBuf

CLSS public org.codehaus.plexus.util.xml.pull.MXParser
cons public init()
cons public init(org.codehaus.plexus.util.xml.pull.EntityReplacementMap)
intf org.codehaus.plexus.util.xml.pull.XmlPullParser
meth public boolean getFeature(java.lang.String)
meth public boolean isAttributeDefault(int)
meth public boolean isEmptyElementTag() throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public boolean isWhitespace() throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public char[] getTextCharacters(int[])
meth public int getAttributeCount()
meth public int getColumnNumber()
meth public int getDepth()
meth public int getEventType() throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public int getLineNumber()
meth public int getNamespaceCount(int) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public int next() throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public int nextTag() throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public int nextToken() throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public int parseEndTag() throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public int parseStartTag() throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.String getAttributeName(int)
meth public java.lang.String getAttributeNamespace(int)
meth public java.lang.String getAttributePrefix(int)
meth public java.lang.String getAttributeType(int)
meth public java.lang.String getAttributeValue(int)
meth public java.lang.String getAttributeValue(java.lang.String,java.lang.String)
meth public java.lang.String getInputEncoding()
meth public java.lang.String getName()
meth public java.lang.String getNamespace()
meth public java.lang.String getNamespace(java.lang.String)
meth public java.lang.String getNamespacePrefix(int) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public java.lang.String getNamespaceUri(int) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public java.lang.String getPositionDescription()
meth public java.lang.String getPrefix()
meth public java.lang.String getText()
meth public java.lang.String nextText() throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public void defineEntityReplacementText(java.lang.String,java.lang.String) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public void require(int,java.lang.String,java.lang.String) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public void setFeature(java.lang.String,boolean) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public void setInput(java.io.InputStream,java.lang.String) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public void setInput(java.io.Reader) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public void setProperty(java.lang.String,java.lang.Object) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public void setupFromTemplate()
meth public void skipSubTree() throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
supr java.lang.Object
hfds BUF_AMP,BUF_APO,BUF_GT,BUF_LT,BUF_NOT_RESOLVED,BUF_QUOT,FEATURE_NAMES_INTERNED,FEATURE_XML_ROUNDTRIP,LOOKUP_MAX,LOOKUP_MAX_CHAR,NCODING,NO,PROPERTY_LOCATION,PROPERTY_XMLDECL_CONTENT,PROPERTY_XMLDECL_STANDALONE,PROPERTY_XMLDECL_VERSION,READ_CHUNK_SIZE,TANDALONE,TRACE_SIZING,VERSION,XMLNS_URI,XML_URI,YES,allStringsInterned,attributeCount,attributeName,attributeNameHash,attributePrefix,attributeUri,attributeValue,buf,bufAbsoluteStart,bufEnd,bufLoadFactor,bufSoftLimit,bufStart,bufferLoadFactor,columnNumber,depth,elName,elNamespaceCount,elPrefix,elRawName,elRawNameEnd,elRawNameLine,elUri,emptyElementTag,entityEnd,entityName,entityNameBuf,entityNameHash,entityRefName,entityReplacement,entityReplacementBuf,eventType,fileEncoding,inputEncoding,lineNumber,location,lookupNameChar,lookupNameStartChar,namespaceEnd,namespacePrefix,namespacePrefixHash,namespaceUri,pastEndTag,pc,pcEnd,pcStart,pos,posEnd,posStart,preventBufferCompaction,processNamespaces,reachedEnd,reader,replacementMapTemplate,resolvedEntityRefCharBuf,roundtripSupported,seenAmpersand,seenDocdecl,seenEndTag,seenMarkup,seenRoot,seenStartTag,text,tokenize,usePC,xmlDeclContent,xmlDeclStandalone,xmlDeclVersion

CLSS public org.codehaus.plexus.util.xml.pull.MXSerializer
cons public init()
fld protected boolean attributeUseApostrophe
fld protected boolean doIndent
fld protected boolean finished
fld protected boolean namesInterned
fld protected boolean pastRoot
fld protected boolean seenBracket
fld protected boolean seenBracketBracket
fld protected boolean seenTag
fld protected boolean setPrefixCalled
fld protected boolean startTagIncomplete
fld protected boolean writeIndentation
fld protected boolean writeLineSeparator
fld protected char[] buf
fld protected char[] indentationBuf
fld protected final java.lang.String FEATURE_NAMES_INTERNED = "http://xmlpull.org/v1/doc/features.html#names-interned"
fld protected final java.lang.String FEATURE_SERIALIZER_ATTVALUE_USE_APOSTROPHE = "http://xmlpull.org/v1/doc/features.html#serializer-attvalue-use-apostrophe"
fld protected final java.lang.String PROPERTY_SERIALIZER_INDENTATION = "http://xmlpull.org/v1/doc/properties.html#serializer-indentation"
fld protected final java.lang.String PROPERTY_SERIALIZER_LINE_SEPARATOR = "http://xmlpull.org/v1/doc/properties.html#serializer-line-separator"
fld protected final static java.lang.String PROPERTY_LOCATION = "http://xmlpull.org/v1/doc/properties.html#location"
fld protected final static java.lang.String XMLNS_URI = "http://www.w3.org/2000/xmlns/"
fld protected final static java.lang.String XML_URI = "http://www.w3.org/XML/1998/namespace"
fld protected final static java.lang.String[] precomputedPrefixes
fld protected int autoDeclaredPrefixes
fld protected int depth
fld protected int indentationJump
fld protected int maxIndentLevel
fld protected int namespaceEnd
fld protected int offsetNewLine
fld protected int[] elNamespaceCount
fld protected java.io.Writer out
fld protected java.lang.String indentationString
fld protected java.lang.String lineSeparator
fld protected java.lang.String location
fld protected java.lang.String[] elName
fld protected java.lang.String[] elNamespace
fld protected java.lang.String[] namespacePrefix
fld protected java.lang.String[] namespaceUri
intf org.codehaus.plexus.util.xml.pull.XmlSerializer
meth protected final static java.lang.String printable(char)
meth protected final static java.lang.String printable(java.lang.String)
meth protected java.lang.String lookupOrDeclarePrefix(java.lang.String)
meth protected void closeStartTag() throws java.io.IOException
meth protected void ensureElementsCapacity()
meth protected void ensureNamespacesCapacity()
meth protected void rebuildIndentationBuf()
meth protected void reset()
meth protected void writeAttributeValue(java.lang.String,java.io.Writer) throws java.io.IOException
meth protected void writeElementContent(char[],int,int,java.io.Writer) throws java.io.IOException
meth protected void writeElementContent(java.lang.String,java.io.Writer) throws java.io.IOException
meth protected void writeIndent() throws java.io.IOException
meth public boolean getFeature(java.lang.String)
meth public int getDepth()
meth public java.io.Writer getWriter()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String getNamespace()
meth public java.lang.String getPrefix(java.lang.String,boolean)
meth public org.codehaus.plexus.util.xml.pull.XmlSerializer attribute(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.codehaus.plexus.util.xml.pull.XmlSerializer endTag(java.lang.String,java.lang.String) throws java.io.IOException
meth public org.codehaus.plexus.util.xml.pull.XmlSerializer startTag(java.lang.String,java.lang.String) throws java.io.IOException
meth public org.codehaus.plexus.util.xml.pull.XmlSerializer text(char[],int,int) throws java.io.IOException
meth public org.codehaus.plexus.util.xml.pull.XmlSerializer text(java.lang.String) throws java.io.IOException
meth public void cdsect(java.lang.String) throws java.io.IOException
meth public void comment(java.lang.String) throws java.io.IOException
meth public void docdecl(java.lang.String) throws java.io.IOException
meth public void endDocument() throws java.io.IOException
meth public void entityRef(java.lang.String) throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void ignorableWhitespace(java.lang.String) throws java.io.IOException
meth public void processingInstruction(java.lang.String) throws java.io.IOException
meth public void setFeature(java.lang.String,boolean)
meth public void setOutput(java.io.OutputStream,java.lang.String) throws java.io.IOException
meth public void setOutput(java.io.Writer)
meth public void setPrefix(java.lang.String,java.lang.String) throws java.io.IOException
meth public void setProperty(java.lang.String,java.lang.Object)
meth public void startDocument(java.lang.String,java.lang.Boolean) throws java.io.IOException
supr java.lang.Object
hfds BUF_LEN,TRACE_SIZING,checkNamesInterned

CLSS public abstract interface org.codehaus.plexus.util.xml.pull.XmlPullParser
fld public final static int CDSECT = 5
fld public final static int COMMENT = 9
fld public final static int DOCDECL = 10
fld public final static int END_DOCUMENT = 1
fld public final static int END_TAG = 3
fld public final static int ENTITY_REF = 6
fld public final static int IGNORABLE_WHITESPACE = 7
fld public final static int PROCESSING_INSTRUCTION = 8
fld public final static int START_DOCUMENT = 0
fld public final static int START_TAG = 2
fld public final static int TEXT = 4
fld public final static java.lang.String FEATURE_PROCESS_DOCDECL = "http://xmlpull.org/v1/doc/features.html#process-docdecl"
fld public final static java.lang.String FEATURE_PROCESS_NAMESPACES = "http://xmlpull.org/v1/doc/features.html#process-namespaces"
fld public final static java.lang.String FEATURE_REPORT_NAMESPACE_ATTRIBUTES = "http://xmlpull.org/v1/doc/features.html#report-namespace-prefixes"
fld public final static java.lang.String FEATURE_VALIDATION = "http://xmlpull.org/v1/doc/features.html#validation"
fld public final static java.lang.String NO_NAMESPACE = ""
fld public final static java.lang.String[] TYPES
meth public abstract boolean getFeature(java.lang.String)
meth public abstract boolean isAttributeDefault(int)
meth public abstract boolean isEmptyElementTag() throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract boolean isWhitespace() throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract char[] getTextCharacters(int[])
meth public abstract int getAttributeCount()
meth public abstract int getColumnNumber()
meth public abstract int getDepth()
meth public abstract int getEventType() throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract int getLineNumber()
meth public abstract int getNamespaceCount(int) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract int next() throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract int nextTag() throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract int nextToken() throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract java.lang.Object getProperty(java.lang.String)
meth public abstract java.lang.String getAttributeName(int)
meth public abstract java.lang.String getAttributeNamespace(int)
meth public abstract java.lang.String getAttributePrefix(int)
meth public abstract java.lang.String getAttributeType(int)
meth public abstract java.lang.String getAttributeValue(int)
meth public abstract java.lang.String getAttributeValue(java.lang.String,java.lang.String)
meth public abstract java.lang.String getInputEncoding()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getNamespace()
meth public abstract java.lang.String getNamespace(java.lang.String)
meth public abstract java.lang.String getNamespacePrefix(int) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract java.lang.String getNamespaceUri(int) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract java.lang.String getPositionDescription()
meth public abstract java.lang.String getPrefix()
meth public abstract java.lang.String getText()
meth public abstract java.lang.String nextText() throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract void defineEntityReplacementText(java.lang.String,java.lang.String) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract void require(int,java.lang.String,java.lang.String) throws java.io.IOException,org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract void setFeature(java.lang.String,boolean) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract void setInput(java.io.InputStream,java.lang.String) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract void setInput(java.io.Reader) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException
meth public abstract void setProperty(java.lang.String,java.lang.Object) throws org.codehaus.plexus.util.xml.pull.XmlPullParserException

CLSS public org.codehaus.plexus.util.xml.pull.XmlPullParserException
cons public init(java.lang.String)
cons public init(java.lang.String,org.codehaus.plexus.util.xml.pull.XmlPullParser,java.lang.Throwable)
fld protected int column
fld protected int row
fld protected java.lang.Throwable detail
 anno 0 java.lang.Deprecated()
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.Throwable getDetail()
 anno 0 java.lang.Deprecated()
meth public void printStackTrace()
supr java.lang.Exception

CLSS public abstract interface org.codehaus.plexus.util.xml.pull.XmlSerializer
meth public abstract boolean getFeature(java.lang.String)
meth public abstract int getDepth()
meth public abstract java.lang.Object getProperty(java.lang.String)
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getNamespace()
meth public abstract java.lang.String getPrefix(java.lang.String,boolean)
meth public abstract org.codehaus.plexus.util.xml.pull.XmlSerializer attribute(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract org.codehaus.plexus.util.xml.pull.XmlSerializer endTag(java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract org.codehaus.plexus.util.xml.pull.XmlSerializer startTag(java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract org.codehaus.plexus.util.xml.pull.XmlSerializer text(char[],int,int) throws java.io.IOException
meth public abstract org.codehaus.plexus.util.xml.pull.XmlSerializer text(java.lang.String) throws java.io.IOException
meth public abstract void cdsect(java.lang.String) throws java.io.IOException
meth public abstract void comment(java.lang.String) throws java.io.IOException
meth public abstract void docdecl(java.lang.String) throws java.io.IOException
meth public abstract void endDocument() throws java.io.IOException
meth public abstract void entityRef(java.lang.String) throws java.io.IOException
meth public abstract void flush() throws java.io.IOException
meth public abstract void ignorableWhitespace(java.lang.String) throws java.io.IOException
meth public abstract void processingInstruction(java.lang.String) throws java.io.IOException
meth public abstract void setFeature(java.lang.String,boolean)
meth public abstract void setOutput(java.io.OutputStream,java.lang.String) throws java.io.IOException
meth public abstract void setOutput(java.io.Writer) throws java.io.IOException
meth public abstract void setPrefix(java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract void setProperty(java.lang.String,java.lang.Object)
meth public abstract void startDocument(java.lang.String,java.lang.Boolean) throws java.io.IOException

CLSS public abstract org.eclipse.aether.AbstractForwardingRepositorySystemSession
cons protected init()
intf org.eclipse.aether.RepositorySystemSession
meth protected abstract org.eclipse.aether.RepositorySystemSession getSession()
meth public boolean isIgnoreArtifactDescriptorRepositories()
meth public boolean isOffline()
meth public java.lang.String getChecksumPolicy()
meth public java.lang.String getUpdatePolicy()
meth public java.util.Map<java.lang.String,java.lang.Object> getConfigProperties()
meth public java.util.Map<java.lang.String,java.lang.String> getSystemProperties()
meth public java.util.Map<java.lang.String,java.lang.String> getUserProperties()
meth public org.eclipse.aether.RepositoryCache getCache()
meth public org.eclipse.aether.RepositoryListener getRepositoryListener()
meth public org.eclipse.aether.SessionData getData()
meth public org.eclipse.aether.artifact.ArtifactTypeRegistry getArtifactTypeRegistry()
meth public org.eclipse.aether.collection.DependencyGraphTransformer getDependencyGraphTransformer()
meth public org.eclipse.aether.collection.DependencyManager getDependencyManager()
meth public org.eclipse.aether.collection.DependencySelector getDependencySelector()
meth public org.eclipse.aether.collection.DependencyTraverser getDependencyTraverser()
meth public org.eclipse.aether.collection.VersionFilter getVersionFilter()
meth public org.eclipse.aether.repository.AuthenticationSelector getAuthenticationSelector()
meth public org.eclipse.aether.repository.LocalRepository getLocalRepository()
meth public org.eclipse.aether.repository.LocalRepositoryManager getLocalRepositoryManager()
meth public org.eclipse.aether.repository.MirrorSelector getMirrorSelector()
meth public org.eclipse.aether.repository.ProxySelector getProxySelector()
meth public org.eclipse.aether.repository.WorkspaceReader getWorkspaceReader()
meth public org.eclipse.aether.resolution.ArtifactDescriptorPolicy getArtifactDescriptorPolicy()
meth public org.eclipse.aether.resolution.ResolutionErrorPolicy getResolutionErrorPolicy()
meth public org.eclipse.aether.transfer.TransferListener getTransferListener()
meth public org.eclipse.aether.transform.FileTransformerManager getFileTransformerManager()
supr java.lang.Object

CLSS public abstract org.eclipse.aether.AbstractRepositoryListener
cons protected init()
intf org.eclipse.aether.RepositoryListener
meth public void artifactDeployed(org.eclipse.aether.RepositoryEvent)
meth public void artifactDeploying(org.eclipse.aether.RepositoryEvent)
meth public void artifactDescriptorInvalid(org.eclipse.aether.RepositoryEvent)
meth public void artifactDescriptorMissing(org.eclipse.aether.RepositoryEvent)
meth public void artifactDownloaded(org.eclipse.aether.RepositoryEvent)
meth public void artifactDownloading(org.eclipse.aether.RepositoryEvent)
meth public void artifactInstalled(org.eclipse.aether.RepositoryEvent)
meth public void artifactInstalling(org.eclipse.aether.RepositoryEvent)
meth public void artifactResolved(org.eclipse.aether.RepositoryEvent)
meth public void artifactResolving(org.eclipse.aether.RepositoryEvent)
meth public void metadataDeployed(org.eclipse.aether.RepositoryEvent)
meth public void metadataDeploying(org.eclipse.aether.RepositoryEvent)
meth public void metadataDownloaded(org.eclipse.aether.RepositoryEvent)
meth public void metadataDownloading(org.eclipse.aether.RepositoryEvent)
meth public void metadataInstalled(org.eclipse.aether.RepositoryEvent)
meth public void metadataInstalling(org.eclipse.aether.RepositoryEvent)
meth public void metadataInvalid(org.eclipse.aether.RepositoryEvent)
meth public void metadataResolved(org.eclipse.aether.RepositoryEvent)
meth public void metadataResolving(org.eclipse.aether.RepositoryEvent)
supr java.lang.Object

CLSS public final org.eclipse.aether.ConfigurationProperties
fld public final static boolean DEFAULT_HTTP_PREEMPTIVE_AUTH = false
fld public final static boolean DEFAULT_HTTP_REUSE_CONNECTIONS = true
fld public final static boolean DEFAULT_IMPLICIT_PRIORITIES = false
fld public final static boolean DEFAULT_INTERACTIVE = false
fld public final static boolean DEFAULT_PERSISTED_CHECKSUMS = true
fld public final static int DEFAULT_CONNECT_TIMEOUT = 10000
fld public final static int DEFAULT_HTTP_CONNECTION_MAX_TTL = 300
fld public final static int DEFAULT_HTTP_MAX_CONNECTIONS_PER_ROUTE = 50
fld public final static int DEFAULT_HTTP_RETRY_HANDLER_COUNT = 3
fld public final static int DEFAULT_REQUEST_TIMEOUT = 1800000
fld public final static java.lang.String CONNECT_TIMEOUT = "aether.connector.connectTimeout"
fld public final static java.lang.String DEFAULT_HTTP_CREDENTIAL_ENCODING = "ISO-8859-1"
fld public final static java.lang.String DEFAULT_HTTP_RETRY_HANDLER_SERVICE_UNAVAILABLE = "429,503"
fld public final static java.lang.String DEFAULT_USER_AGENT = "Aether"
fld public final static java.lang.String HTTPS_SECURITY_MODE = "aether.connector.https.securityMode"
fld public final static java.lang.String HTTPS_SECURITY_MODE_DEFAULT = "default"
fld public final static java.lang.String HTTPS_SECURITY_MODE_INSECURE = "insecure"
fld public final static java.lang.String HTTP_CONNECTION_MAX_TTL = "aether.connector.http.connectionMaxTtl"
fld public final static java.lang.String HTTP_CREDENTIAL_ENCODING = "aether.connector.http.credentialEncoding"
fld public final static java.lang.String HTTP_EXPECT_CONTINUE = "aether.connector.http.expectContinue"
fld public final static java.lang.String HTTP_HEADERS = "aether.connector.http.headers"
fld public final static java.lang.String HTTP_MAX_CONNECTIONS_PER_ROUTE = "aether.connector.http.maxConnectionsPerRoute"
fld public final static java.lang.String HTTP_PREEMPTIVE_AUTH = "aether.connector.http.preemptiveAuth"
fld public final static java.lang.String HTTP_RETRY_HANDLER_COUNT = "aether.connector.http.retryHandler.count"
fld public final static java.lang.String HTTP_RETRY_HANDLER_INTERVAL = "aether.connector.http.retryHandler.interval"
fld public final static java.lang.String HTTP_RETRY_HANDLER_INTERVAL_MAX = "aether.connector.http.retryHandler.intervalMax"
fld public final static java.lang.String HTTP_RETRY_HANDLER_SERVICE_UNAVAILABLE = "aether.connector.http.retryHandler.serviceUnavailable"
fld public final static java.lang.String HTTP_REUSE_CONNECTIONS = "aether.connector.http.reuseConnections"
fld public final static java.lang.String IMPLICIT_PRIORITIES = "aether.priority.implicit"
fld public final static java.lang.String INTERACTIVE = "aether.interactive"
fld public final static java.lang.String PERSISTED_CHECKSUMS = "aether.connector.persistedChecksums"
fld public final static java.lang.String PREFIX_PRIORITY = "aether.priority."
fld public final static java.lang.String REQUEST_TIMEOUT = "aether.connector.requestTimeout"
fld public final static java.lang.String USER_AGENT = "aether.connector.userAgent"
fld public final static long DEFAULT_HTTP_RETRY_HANDLER_INTERVAL = 5000
fld public final static long DEFAULT_HTTP_RETRY_HANDLER_INTERVAL_MAX = 300000
supr java.lang.Object
hfds PREFIX_AETHER,PREFIX_CONNECTOR

CLSS public final org.eclipse.aether.DefaultRepositoryCache
cons public init()
intf org.eclipse.aether.RepositoryCache
meth public java.lang.Object get(org.eclipse.aether.RepositorySystemSession,java.lang.Object)
meth public void put(org.eclipse.aether.RepositorySystemSession,java.lang.Object,java.lang.Object)
supr java.lang.Object
hfds cache

CLSS public final org.eclipse.aether.DefaultRepositorySystemSession
cons public init()
cons public init(org.eclipse.aether.RepositorySystemSession)
intf org.eclipse.aether.RepositorySystemSession
meth public boolean isIgnoreArtifactDescriptorRepositories()
meth public boolean isOffline()
meth public java.lang.String getChecksumPolicy()
meth public java.lang.String getUpdatePolicy()
meth public java.util.Map<java.lang.String,java.lang.Object> getConfigProperties()
meth public java.util.Map<java.lang.String,java.lang.String> getSystemProperties()
meth public java.util.Map<java.lang.String,java.lang.String> getUserProperties()
meth public org.eclipse.aether.DefaultRepositorySystemSession setArtifactDescriptorPolicy(org.eclipse.aether.resolution.ArtifactDescriptorPolicy)
meth public org.eclipse.aether.DefaultRepositorySystemSession setArtifactTypeRegistry(org.eclipse.aether.artifact.ArtifactTypeRegistry)
meth public org.eclipse.aether.DefaultRepositorySystemSession setAuthenticationSelector(org.eclipse.aether.repository.AuthenticationSelector)
meth public org.eclipse.aether.DefaultRepositorySystemSession setCache(org.eclipse.aether.RepositoryCache)
meth public org.eclipse.aether.DefaultRepositorySystemSession setChecksumPolicy(java.lang.String)
meth public org.eclipse.aether.DefaultRepositorySystemSession setConfigProperties(java.util.Map<?,?>)
meth public org.eclipse.aether.DefaultRepositorySystemSession setConfigProperty(java.lang.String,java.lang.Object)
meth public org.eclipse.aether.DefaultRepositorySystemSession setData(org.eclipse.aether.SessionData)
meth public org.eclipse.aether.DefaultRepositorySystemSession setDependencyGraphTransformer(org.eclipse.aether.collection.DependencyGraphTransformer)
meth public org.eclipse.aether.DefaultRepositorySystemSession setDependencyManager(org.eclipse.aether.collection.DependencyManager)
meth public org.eclipse.aether.DefaultRepositorySystemSession setDependencySelector(org.eclipse.aether.collection.DependencySelector)
meth public org.eclipse.aether.DefaultRepositorySystemSession setDependencyTraverser(org.eclipse.aether.collection.DependencyTraverser)
meth public org.eclipse.aether.DefaultRepositorySystemSession setFileTransformerManager(org.eclipse.aether.transform.FileTransformerManager)
meth public org.eclipse.aether.DefaultRepositorySystemSession setIgnoreArtifactDescriptorRepositories(boolean)
meth public org.eclipse.aether.DefaultRepositorySystemSession setLocalRepositoryManager(org.eclipse.aether.repository.LocalRepositoryManager)
meth public org.eclipse.aether.DefaultRepositorySystemSession setMirrorSelector(org.eclipse.aether.repository.MirrorSelector)
meth public org.eclipse.aether.DefaultRepositorySystemSession setOffline(boolean)
meth public org.eclipse.aether.DefaultRepositorySystemSession setProxySelector(org.eclipse.aether.repository.ProxySelector)
meth public org.eclipse.aether.DefaultRepositorySystemSession setRepositoryListener(org.eclipse.aether.RepositoryListener)
meth public org.eclipse.aether.DefaultRepositorySystemSession setResolutionErrorPolicy(org.eclipse.aether.resolution.ResolutionErrorPolicy)
meth public org.eclipse.aether.DefaultRepositorySystemSession setSystemProperties(java.util.Map<?,?>)
meth public org.eclipse.aether.DefaultRepositorySystemSession setSystemProperty(java.lang.String,java.lang.String)
meth public org.eclipse.aether.DefaultRepositorySystemSession setTransferListener(org.eclipse.aether.transfer.TransferListener)
meth public org.eclipse.aether.DefaultRepositorySystemSession setUpdatePolicy(java.lang.String)
meth public org.eclipse.aether.DefaultRepositorySystemSession setUserProperties(java.util.Map<?,?>)
meth public org.eclipse.aether.DefaultRepositorySystemSession setUserProperty(java.lang.String,java.lang.String)
meth public org.eclipse.aether.DefaultRepositorySystemSession setVersionFilter(org.eclipse.aether.collection.VersionFilter)
meth public org.eclipse.aether.DefaultRepositorySystemSession setWorkspaceReader(org.eclipse.aether.repository.WorkspaceReader)
meth public org.eclipse.aether.RepositoryCache getCache()
meth public org.eclipse.aether.RepositoryListener getRepositoryListener()
meth public org.eclipse.aether.SessionData getData()
meth public org.eclipse.aether.artifact.ArtifactTypeRegistry getArtifactTypeRegistry()
meth public org.eclipse.aether.collection.DependencyGraphTransformer getDependencyGraphTransformer()
meth public org.eclipse.aether.collection.DependencyManager getDependencyManager()
meth public org.eclipse.aether.collection.DependencySelector getDependencySelector()
meth public org.eclipse.aether.collection.DependencyTraverser getDependencyTraverser()
meth public org.eclipse.aether.collection.VersionFilter getVersionFilter()
meth public org.eclipse.aether.repository.AuthenticationSelector getAuthenticationSelector()
meth public org.eclipse.aether.repository.LocalRepository getLocalRepository()
meth public org.eclipse.aether.repository.LocalRepositoryManager getLocalRepositoryManager()
meth public org.eclipse.aether.repository.MirrorSelector getMirrorSelector()
meth public org.eclipse.aether.repository.ProxySelector getProxySelector()
meth public org.eclipse.aether.repository.WorkspaceReader getWorkspaceReader()
meth public org.eclipse.aether.resolution.ArtifactDescriptorPolicy getArtifactDescriptorPolicy()
meth public org.eclipse.aether.resolution.ResolutionErrorPolicy getResolutionErrorPolicy()
meth public org.eclipse.aether.transfer.TransferListener getTransferListener()
meth public org.eclipse.aether.transform.FileTransformerManager getFileTransformerManager()
meth public void setReadOnly()
supr java.lang.Object
hfds artifactDescriptorPolicy,artifactTypeRegistry,authenticationSelector,cache,checksumPolicy,configProperties,configPropertiesView,data,dependencyGraphTransformer,dependencyManager,dependencySelector,dependencyTraverser,fileTransformerManager,ignoreArtifactDescriptorRepositories,localRepositoryManager,mirrorSelector,offline,proxySelector,readOnly,repositoryListener,resolutionErrorPolicy,systemProperties,systemPropertiesView,transferListener,updatePolicy,userProperties,userPropertiesView,versionFilter,workspaceReader
hcls NullArtifactTypeRegistry,NullAuthenticationSelector,NullFileTransformerManager,NullMirrorSelector,NullProxySelector

CLSS public final org.eclipse.aether.DefaultSessionData
cons public init()
intf org.eclipse.aether.SessionData
meth public boolean set(java.lang.Object,java.lang.Object,java.lang.Object)
meth public java.lang.Object computeIfAbsent(java.lang.Object,java.util.function.Supplier<java.lang.Object>)
meth public java.lang.Object get(java.lang.Object)
meth public void set(java.lang.Object,java.lang.Object)
supr java.lang.Object
hfds data

CLSS public final org.eclipse.aether.MultiRuntimeException
meth public java.util.List<? extends java.lang.Throwable> getThrowables()
meth public static void mayThrow(java.lang.String,java.util.List<? extends java.lang.Throwable>)
supr java.lang.RuntimeException
hfds throwables

CLSS public abstract interface org.eclipse.aether.RepositoryCache
meth public abstract java.lang.Object get(org.eclipse.aether.RepositorySystemSession,java.lang.Object)
meth public abstract void put(org.eclipse.aether.RepositorySystemSession,java.lang.Object,java.lang.Object)

CLSS public final org.eclipse.aether.RepositoryEvent
innr public final static !enum EventType
innr public final static Builder
meth public java.io.File getFile()
meth public java.lang.Exception getException()
meth public java.lang.String toString()
meth public java.util.List<java.lang.Exception> getExceptions()
meth public org.eclipse.aether.RepositoryEvent$EventType getType()
meth public org.eclipse.aether.RepositorySystemSession getSession()
meth public org.eclipse.aether.RequestTrace getTrace()
meth public org.eclipse.aether.artifact.Artifact getArtifact()
meth public org.eclipse.aether.metadata.Metadata getMetadata()
meth public org.eclipse.aether.repository.ArtifactRepository getRepository()
supr java.lang.Object
hfds artifact,exceptions,file,metadata,repository,session,trace,type

CLSS public final static org.eclipse.aether.RepositoryEvent$Builder
 outer org.eclipse.aether.RepositoryEvent
cons public init(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.RepositoryEvent$EventType)
meth public org.eclipse.aether.RepositoryEvent build()
meth public org.eclipse.aether.RepositoryEvent$Builder setArtifact(org.eclipse.aether.artifact.Artifact)
meth public org.eclipse.aether.RepositoryEvent$Builder setException(java.lang.Exception)
meth public org.eclipse.aether.RepositoryEvent$Builder setExceptions(java.util.List<java.lang.Exception>)
meth public org.eclipse.aether.RepositoryEvent$Builder setFile(java.io.File)
meth public org.eclipse.aether.RepositoryEvent$Builder setMetadata(org.eclipse.aether.metadata.Metadata)
meth public org.eclipse.aether.RepositoryEvent$Builder setRepository(org.eclipse.aether.repository.ArtifactRepository)
meth public org.eclipse.aether.RepositoryEvent$Builder setTrace(org.eclipse.aether.RequestTrace)
supr java.lang.Object
hfds artifact,exceptions,file,metadata,repository,session,trace,type

CLSS public final static !enum org.eclipse.aether.RepositoryEvent$EventType
 outer org.eclipse.aether.RepositoryEvent
fld public final static org.eclipse.aether.RepositoryEvent$EventType ARTIFACT_DEPLOYED
fld public final static org.eclipse.aether.RepositoryEvent$EventType ARTIFACT_DEPLOYING
fld public final static org.eclipse.aether.RepositoryEvent$EventType ARTIFACT_DESCRIPTOR_INVALID
fld public final static org.eclipse.aether.RepositoryEvent$EventType ARTIFACT_DESCRIPTOR_MISSING
fld public final static org.eclipse.aether.RepositoryEvent$EventType ARTIFACT_DOWNLOADED
fld public final static org.eclipse.aether.RepositoryEvent$EventType ARTIFACT_DOWNLOADING
fld public final static org.eclipse.aether.RepositoryEvent$EventType ARTIFACT_INSTALLED
fld public final static org.eclipse.aether.RepositoryEvent$EventType ARTIFACT_INSTALLING
fld public final static org.eclipse.aether.RepositoryEvent$EventType ARTIFACT_RESOLVED
fld public final static org.eclipse.aether.RepositoryEvent$EventType ARTIFACT_RESOLVING
fld public final static org.eclipse.aether.RepositoryEvent$EventType METADATA_DEPLOYED
fld public final static org.eclipse.aether.RepositoryEvent$EventType METADATA_DEPLOYING
fld public final static org.eclipse.aether.RepositoryEvent$EventType METADATA_DOWNLOADED
fld public final static org.eclipse.aether.RepositoryEvent$EventType METADATA_DOWNLOADING
fld public final static org.eclipse.aether.RepositoryEvent$EventType METADATA_INSTALLED
fld public final static org.eclipse.aether.RepositoryEvent$EventType METADATA_INSTALLING
fld public final static org.eclipse.aether.RepositoryEvent$EventType METADATA_INVALID
fld public final static org.eclipse.aether.RepositoryEvent$EventType METADATA_RESOLVED
fld public final static org.eclipse.aether.RepositoryEvent$EventType METADATA_RESOLVING
meth public static org.eclipse.aether.RepositoryEvent$EventType valueOf(java.lang.String)
meth public static org.eclipse.aether.RepositoryEvent$EventType[] values()
supr java.lang.Enum<org.eclipse.aether.RepositoryEvent$EventType>

CLSS public org.eclipse.aether.RepositoryException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
meth protected static java.lang.String getMessage(java.lang.String,java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface org.eclipse.aether.RepositoryListener
meth public abstract void artifactDeployed(org.eclipse.aether.RepositoryEvent)
meth public abstract void artifactDeploying(org.eclipse.aether.RepositoryEvent)
meth public abstract void artifactDescriptorInvalid(org.eclipse.aether.RepositoryEvent)
meth public abstract void artifactDescriptorMissing(org.eclipse.aether.RepositoryEvent)
meth public abstract void artifactDownloaded(org.eclipse.aether.RepositoryEvent)
meth public abstract void artifactDownloading(org.eclipse.aether.RepositoryEvent)
meth public abstract void artifactInstalled(org.eclipse.aether.RepositoryEvent)
meth public abstract void artifactInstalling(org.eclipse.aether.RepositoryEvent)
meth public abstract void artifactResolved(org.eclipse.aether.RepositoryEvent)
meth public abstract void artifactResolving(org.eclipse.aether.RepositoryEvent)
meth public abstract void metadataDeployed(org.eclipse.aether.RepositoryEvent)
meth public abstract void metadataDeploying(org.eclipse.aether.RepositoryEvent)
meth public abstract void metadataDownloaded(org.eclipse.aether.RepositoryEvent)
meth public abstract void metadataDownloading(org.eclipse.aether.RepositoryEvent)
meth public abstract void metadataInstalled(org.eclipse.aether.RepositoryEvent)
meth public abstract void metadataInstalling(org.eclipse.aether.RepositoryEvent)
meth public abstract void metadataInvalid(org.eclipse.aether.RepositoryEvent)
meth public abstract void metadataResolved(org.eclipse.aether.RepositoryEvent)
meth public abstract void metadataResolving(org.eclipse.aether.RepositoryEvent)

CLSS public abstract interface org.eclipse.aether.RepositorySystem
meth public abstract java.util.List<org.eclipse.aether.repository.RemoteRepository> newResolutionRepositories(org.eclipse.aether.RepositorySystemSession,java.util.List<org.eclipse.aether.repository.RemoteRepository>)
meth public abstract java.util.List<org.eclipse.aether.resolution.ArtifactResult> resolveArtifacts(org.eclipse.aether.RepositorySystemSession,java.util.Collection<? extends org.eclipse.aether.resolution.ArtifactRequest>) throws org.eclipse.aether.resolution.ArtifactResolutionException
meth public abstract java.util.List<org.eclipse.aether.resolution.MetadataResult> resolveMetadata(org.eclipse.aether.RepositorySystemSession,java.util.Collection<? extends org.eclipse.aether.resolution.MetadataRequest>)
meth public abstract org.eclipse.aether.SyncContext newSyncContext(org.eclipse.aether.RepositorySystemSession,boolean)
meth public abstract org.eclipse.aether.collection.CollectResult collectDependencies(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.collection.CollectRequest) throws org.eclipse.aether.collection.DependencyCollectionException
meth public abstract org.eclipse.aether.deployment.DeployResult deploy(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.deployment.DeployRequest) throws org.eclipse.aether.deployment.DeploymentException
meth public abstract org.eclipse.aether.installation.InstallResult install(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.installation.InstallRequest) throws org.eclipse.aether.installation.InstallationException
meth public abstract org.eclipse.aether.repository.LocalRepositoryManager newLocalRepositoryManager(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.LocalRepository)
meth public abstract org.eclipse.aether.repository.RemoteRepository newDeploymentRepository(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.RemoteRepository)
meth public abstract org.eclipse.aether.resolution.ArtifactDescriptorResult readArtifactDescriptor(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.resolution.ArtifactDescriptorRequest) throws org.eclipse.aether.resolution.ArtifactDescriptorException
meth public abstract org.eclipse.aether.resolution.ArtifactResult resolveArtifact(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.resolution.ArtifactRequest) throws org.eclipse.aether.resolution.ArtifactResolutionException
meth public abstract org.eclipse.aether.resolution.DependencyResult resolveDependencies(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.resolution.DependencyRequest) throws org.eclipse.aether.resolution.DependencyResolutionException
meth public abstract org.eclipse.aether.resolution.VersionRangeResult resolveVersionRange(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.resolution.VersionRangeRequest) throws org.eclipse.aether.resolution.VersionRangeResolutionException
meth public abstract org.eclipse.aether.resolution.VersionResult resolveVersion(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.resolution.VersionRequest) throws org.eclipse.aether.resolution.VersionResolutionException
meth public abstract void addOnSystemEndedHandler(java.lang.Runnable)
meth public abstract void shutdown()

CLSS public abstract interface org.eclipse.aether.RepositorySystemSession
meth public abstract boolean isIgnoreArtifactDescriptorRepositories()
meth public abstract boolean isOffline()
meth public abstract java.lang.String getChecksumPolicy()
meth public abstract java.lang.String getUpdatePolicy()
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getConfigProperties()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getSystemProperties()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getUserProperties()
meth public abstract org.eclipse.aether.RepositoryCache getCache()
meth public abstract org.eclipse.aether.RepositoryListener getRepositoryListener()
meth public abstract org.eclipse.aether.SessionData getData()
meth public abstract org.eclipse.aether.artifact.ArtifactTypeRegistry getArtifactTypeRegistry()
meth public abstract org.eclipse.aether.collection.DependencyGraphTransformer getDependencyGraphTransformer()
meth public abstract org.eclipse.aether.collection.DependencyManager getDependencyManager()
meth public abstract org.eclipse.aether.collection.DependencySelector getDependencySelector()
meth public abstract org.eclipse.aether.collection.DependencyTraverser getDependencyTraverser()
meth public abstract org.eclipse.aether.collection.VersionFilter getVersionFilter()
meth public abstract org.eclipse.aether.repository.AuthenticationSelector getAuthenticationSelector()
meth public abstract org.eclipse.aether.repository.LocalRepository getLocalRepository()
meth public abstract org.eclipse.aether.repository.LocalRepositoryManager getLocalRepositoryManager()
meth public abstract org.eclipse.aether.repository.MirrorSelector getMirrorSelector()
meth public abstract org.eclipse.aether.repository.ProxySelector getProxySelector()
meth public abstract org.eclipse.aether.repository.WorkspaceReader getWorkspaceReader()
meth public abstract org.eclipse.aether.resolution.ArtifactDescriptorPolicy getArtifactDescriptorPolicy()
meth public abstract org.eclipse.aether.resolution.ResolutionErrorPolicy getResolutionErrorPolicy()
meth public abstract org.eclipse.aether.transfer.TransferListener getTransferListener()
meth public abstract org.eclipse.aether.transform.FileTransformerManager getFileTransformerManager()
 anno 0 java.lang.Deprecated()

CLSS public org.eclipse.aether.RequestTrace
cons protected init(org.eclipse.aether.RequestTrace,java.lang.Object)
cons public init(java.lang.Object)
meth public final java.lang.Object getData()
meth public final org.eclipse.aether.RequestTrace getParent()
meth public java.lang.String toString()
meth public org.eclipse.aether.RequestTrace newChild(java.lang.Object)
meth public static org.eclipse.aether.RequestTrace newChild(org.eclipse.aether.RequestTrace,java.lang.Object)
supr java.lang.Object
hfds data,parent

CLSS public abstract interface org.eclipse.aether.SessionData
meth public abstract boolean set(java.lang.Object,java.lang.Object,java.lang.Object)
meth public abstract java.lang.Object computeIfAbsent(java.lang.Object,java.util.function.Supplier<java.lang.Object>)
meth public abstract java.lang.Object get(java.lang.Object)
meth public abstract void set(java.lang.Object,java.lang.Object)

CLSS public abstract interface org.eclipse.aether.SyncContext
intf java.io.Closeable
meth public abstract void acquire(java.util.Collection<? extends org.eclipse.aether.artifact.Artifact>,java.util.Collection<? extends org.eclipse.aether.metadata.Metadata>)
meth public abstract void close()

CLSS public abstract org.eclipse.aether.artifact.AbstractArtifact
cons public init()
intf org.eclipse.aether.artifact.Artifact
meth protected static java.util.Map<java.lang.String,java.lang.String> copyProperties(java.util.Map<java.lang.String,java.lang.String>)
meth public boolean equals(java.lang.Object)
meth public boolean isSnapshot()
meth public int hashCode()
meth public java.lang.String getBaseVersion()
meth public java.lang.String getProperty(java.lang.String,java.lang.String)
meth public java.lang.String toString()
meth public org.eclipse.aether.artifact.Artifact setFile(java.io.File)
meth public org.eclipse.aether.artifact.Artifact setProperties(java.util.Map<java.lang.String,java.lang.String>)
meth public org.eclipse.aether.artifact.Artifact setVersion(java.lang.String)
supr java.lang.Object
hfds SNAPSHOT,SNAPSHOT_TIMESTAMP

CLSS public abstract interface org.eclipse.aether.artifact.Artifact
meth public abstract boolean isSnapshot()
meth public abstract java.io.File getFile()
meth public abstract java.lang.String getArtifactId()
meth public abstract java.lang.String getBaseVersion()
meth public abstract java.lang.String getClassifier()
meth public abstract java.lang.String getExtension()
meth public abstract java.lang.String getGroupId()
meth public abstract java.lang.String getProperty(java.lang.String,java.lang.String)
meth public abstract java.lang.String getVersion()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public abstract org.eclipse.aether.artifact.Artifact setFile(java.io.File)
meth public abstract org.eclipse.aether.artifact.Artifact setProperties(java.util.Map<java.lang.String,java.lang.String>)
meth public abstract org.eclipse.aether.artifact.Artifact setVersion(java.lang.String)

CLSS public final org.eclipse.aether.artifact.ArtifactProperties
fld public final static java.lang.String CONSTITUTES_BUILD_PATH = "constitutesBuildPath"
fld public final static java.lang.String DOWNLOAD_URL = "downloadUrl"
fld public final static java.lang.String INCLUDES_DEPENDENCIES = "includesDependencies"
fld public final static java.lang.String LANGUAGE = "language"
fld public final static java.lang.String LOCAL_PATH = "localPath"
fld public final static java.lang.String TYPE = "type"
supr java.lang.Object

CLSS public abstract interface org.eclipse.aether.artifact.ArtifactType
meth public abstract java.lang.String getClassifier()
meth public abstract java.lang.String getExtension()
meth public abstract java.lang.String getId()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getProperties()

CLSS public abstract interface org.eclipse.aether.artifact.ArtifactTypeRegistry
meth public abstract org.eclipse.aether.artifact.ArtifactType get(java.lang.String)

CLSS public final org.eclipse.aether.artifact.DefaultArtifact
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.String>,java.io.File)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.String>,org.eclipse.aether.artifact.ArtifactType)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.eclipse.aether.artifact.ArtifactType)
cons public init(java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
meth public java.io.File getFile()
meth public java.lang.String getArtifactId()
meth public java.lang.String getClassifier()
meth public java.lang.String getExtension()
meth public java.lang.String getGroupId()
meth public java.lang.String getVersion()
meth public java.util.Map<java.lang.String,java.lang.String> getProperties()
supr org.eclipse.aether.artifact.AbstractArtifact
hfds COORDINATE_PATTERN,artifactId,classifier,extension,file,groupId,properties,version

CLSS public final org.eclipse.aether.artifact.DefaultArtifactType
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean,boolean)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
intf org.eclipse.aether.artifact.ArtifactType
meth public java.lang.String getClassifier()
meth public java.lang.String getExtension()
meth public java.lang.String getId()
meth public java.util.Map<java.lang.String,java.lang.String> getProperties()
supr java.lang.Object
hfds classifier,extension,id,properties

CLSS abstract interface org.eclipse.aether.artifact.package-info

CLSS public abstract org.eclipse.aether.metadata.AbstractMetadata
cons public init()
intf org.eclipse.aether.metadata.Metadata
meth protected static java.util.Map<java.lang.String,java.lang.String> copyProperties(java.util.Map<java.lang.String,java.lang.String>)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getProperty(java.lang.String,java.lang.String)
meth public java.lang.String toString()
meth public org.eclipse.aether.metadata.Metadata setFile(java.io.File)
meth public org.eclipse.aether.metadata.Metadata setProperties(java.util.Map<java.lang.String,java.lang.String>)
supr java.lang.Object

CLSS public abstract interface org.eclipse.aether.metadata.MergeableMetadata
intf org.eclipse.aether.metadata.Metadata
meth public abstract boolean isMerged()
meth public abstract void merge(java.io.File,java.io.File) throws org.eclipse.aether.RepositoryException

CLSS public abstract interface org.eclipse.aether.metadata.Metadata
innr public final static !enum Nature
meth public abstract java.io.File getFile()
meth public abstract java.lang.String getArtifactId()
meth public abstract java.lang.String getGroupId()
meth public abstract java.lang.String getProperty(java.lang.String,java.lang.String)
meth public abstract java.lang.String getType()
meth public abstract java.lang.String getVersion()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public abstract org.eclipse.aether.metadata.Metadata setFile(java.io.File)
meth public abstract org.eclipse.aether.metadata.Metadata setProperties(java.util.Map<java.lang.String,java.lang.String>)
meth public abstract org.eclipse.aether.metadata.Metadata$Nature getNature()

CLSS abstract interface org.eclipse.aether.package-info

CLSS public abstract interface org.eclipse.aether.repository.ArtifactRepository
meth public abstract java.lang.String getContentType()
meth public abstract java.lang.String getId()

CLSS public abstract interface org.eclipse.aether.repository.Authentication
meth public abstract void digest(org.eclipse.aether.repository.AuthenticationDigest)
meth public abstract void fill(org.eclipse.aether.repository.AuthenticationContext,java.lang.String,java.util.Map<java.lang.String,java.lang.String>)

CLSS public final org.eclipse.aether.repository.AuthenticationContext
fld public final static java.lang.String HOST_KEY_ACCEPTANCE = "hostKey.acceptance"
fld public final static java.lang.String HOST_KEY_LOCAL = "hostKey.local"
fld public final static java.lang.String HOST_KEY_REMOTE = "hostKey.remote"
fld public final static java.lang.String NTLM_DOMAIN = "ntlm.domain"
fld public final static java.lang.String NTLM_WORKSTATION = "ntlm.workstation"
fld public final static java.lang.String PASSWORD = "password"
fld public final static java.lang.String PRIVATE_KEY_PASSPHRASE = "privateKey.passphrase"
fld public final static java.lang.String PRIVATE_KEY_PATH = "privateKey.path"
fld public final static java.lang.String SSL_CONTEXT = "ssl.context"
fld public final static java.lang.String SSL_HOSTNAME_VERIFIER = "ssl.hostnameVerifier"
fld public final static java.lang.String USERNAME = "username"
intf java.io.Closeable
meth public <%0 extends java.lang.Object> {%%0} get(java.lang.String,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} get(java.lang.String,java.util.Map<java.lang.String,java.lang.String>,java.lang.Class<{%%0}>)
meth public java.lang.String get(java.lang.String)
meth public org.eclipse.aether.RepositorySystemSession getSession()
meth public org.eclipse.aether.repository.Proxy getProxy()
meth public org.eclipse.aether.repository.RemoteRepository getRepository()
meth public static org.eclipse.aether.repository.AuthenticationContext forProxy(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.RemoteRepository)
meth public static org.eclipse.aether.repository.AuthenticationContext forRepository(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.RemoteRepository)
meth public static void close(org.eclipse.aether.repository.AuthenticationContext)
meth public void close()
meth public void put(java.lang.String,java.lang.Object)
supr java.lang.Object
hfds auth,authData,fillingAuthData,proxy,repository,session

CLSS public final org.eclipse.aether.repository.AuthenticationDigest
meth public !varargs void update(byte[])
meth public !varargs void update(char[])
meth public !varargs void update(java.lang.String[])
meth public org.eclipse.aether.RepositorySystemSession getSession()
meth public org.eclipse.aether.repository.Proxy getProxy()
meth public org.eclipse.aether.repository.RemoteRepository getRepository()
meth public static java.lang.String forProxy(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.RemoteRepository)
meth public static java.lang.String forRepository(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.RemoteRepository)
supr java.lang.Object
hfds digest,proxy,repository,session

CLSS public abstract interface org.eclipse.aether.repository.AuthenticationSelector
meth public abstract org.eclipse.aether.repository.Authentication getAuthentication(org.eclipse.aether.repository.RemoteRepository)

CLSS public final org.eclipse.aether.repository.LocalArtifactRegistration
cons public init()
cons public init(org.eclipse.aether.artifact.Artifact)
cons public init(org.eclipse.aether.artifact.Artifact,org.eclipse.aether.repository.RemoteRepository,java.util.Collection<java.lang.String>)
meth public java.util.Collection<java.lang.String> getContexts()
meth public org.eclipse.aether.artifact.Artifact getArtifact()
meth public org.eclipse.aether.repository.LocalArtifactRegistration setArtifact(org.eclipse.aether.artifact.Artifact)
meth public org.eclipse.aether.repository.LocalArtifactRegistration setContexts(java.util.Collection<java.lang.String>)
meth public org.eclipse.aether.repository.LocalArtifactRegistration setRepository(org.eclipse.aether.repository.RemoteRepository)
meth public org.eclipse.aether.repository.RemoteRepository getRepository()
supr java.lang.Object
hfds artifact,contexts,repository

CLSS public final org.eclipse.aether.repository.LocalArtifactRequest
cons public init()
cons public init(org.eclipse.aether.artifact.Artifact,java.util.List<org.eclipse.aether.repository.RemoteRepository>,java.lang.String)
meth public java.lang.String getContext()
meth public java.lang.String toString()
meth public java.util.List<org.eclipse.aether.repository.RemoteRepository> getRepositories()
meth public org.eclipse.aether.artifact.Artifact getArtifact()
meth public org.eclipse.aether.repository.LocalArtifactRequest setArtifact(org.eclipse.aether.artifact.Artifact)
meth public org.eclipse.aether.repository.LocalArtifactRequest setContext(java.lang.String)
meth public org.eclipse.aether.repository.LocalArtifactRequest setRepositories(java.util.List<org.eclipse.aether.repository.RemoteRepository>)
supr java.lang.Object
hfds artifact,context,repositories

CLSS public final org.eclipse.aether.repository.LocalArtifactResult
cons public init(org.eclipse.aether.repository.LocalArtifactRequest)
meth public boolean isAvailable()
meth public java.io.File getFile()
meth public java.lang.String toString()
meth public org.eclipse.aether.repository.LocalArtifactRequest getRequest()
meth public org.eclipse.aether.repository.LocalArtifactResult setAvailable(boolean)
meth public org.eclipse.aether.repository.LocalArtifactResult setFile(java.io.File)
meth public org.eclipse.aether.repository.LocalArtifactResult setRepository(org.eclipse.aether.repository.RemoteRepository)
meth public org.eclipse.aether.repository.RemoteRepository getRepository()
supr java.lang.Object
hfds available,file,repository,request

CLSS public final org.eclipse.aether.repository.LocalMetadataRegistration
cons public init()
cons public init(org.eclipse.aether.metadata.Metadata)
cons public init(org.eclipse.aether.metadata.Metadata,org.eclipse.aether.repository.RemoteRepository,java.util.Collection<java.lang.String>)
meth public java.util.Collection<java.lang.String> getContexts()
meth public org.eclipse.aether.metadata.Metadata getMetadata()
meth public org.eclipse.aether.repository.LocalMetadataRegistration setContexts(java.util.Collection<java.lang.String>)
meth public org.eclipse.aether.repository.LocalMetadataRegistration setMetadata(org.eclipse.aether.metadata.Metadata)
meth public org.eclipse.aether.repository.LocalMetadataRegistration setRepository(org.eclipse.aether.repository.RemoteRepository)
meth public org.eclipse.aether.repository.RemoteRepository getRepository()
supr java.lang.Object
hfds contexts,metadata,repository

CLSS public final org.eclipse.aether.repository.LocalMetadataRequest
cons public init()
cons public init(org.eclipse.aether.metadata.Metadata,org.eclipse.aether.repository.RemoteRepository,java.lang.String)
meth public java.lang.String getContext()
meth public java.lang.String toString()
meth public org.eclipse.aether.metadata.Metadata getMetadata()
meth public org.eclipse.aether.repository.LocalMetadataRequest setContext(java.lang.String)
meth public org.eclipse.aether.repository.LocalMetadataRequest setMetadata(org.eclipse.aether.metadata.Metadata)
meth public org.eclipse.aether.repository.LocalMetadataRequest setRepository(org.eclipse.aether.repository.RemoteRepository)
meth public org.eclipse.aether.repository.RemoteRepository getRepository()
supr java.lang.Object
hfds context,metadata,repository

CLSS public final org.eclipse.aether.repository.LocalMetadataResult
cons public init(org.eclipse.aether.repository.LocalMetadataRequest)
meth public boolean isStale()
meth public java.io.File getFile()
meth public java.lang.String toString()
meth public org.eclipse.aether.repository.LocalMetadataRequest getRequest()
meth public org.eclipse.aether.repository.LocalMetadataResult setFile(java.io.File)
meth public org.eclipse.aether.repository.LocalMetadataResult setStale(boolean)
supr java.lang.Object
hfds file,request,stale

CLSS public final org.eclipse.aether.repository.LocalRepository
cons public init(java.io.File)
cons public init(java.io.File,java.lang.String)
cons public init(java.lang.String)
intf org.eclipse.aether.repository.ArtifactRepository
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.io.File getBasedir()
meth public java.lang.String getContentType()
meth public java.lang.String getId()
meth public java.lang.String toString()
supr java.lang.Object
hfds basedir,type

CLSS public abstract interface org.eclipse.aether.repository.LocalRepositoryManager
meth public abstract java.lang.String getPathForLocalArtifact(org.eclipse.aether.artifact.Artifact)
meth public abstract java.lang.String getPathForLocalMetadata(org.eclipse.aether.metadata.Metadata)
meth public abstract java.lang.String getPathForRemoteArtifact(org.eclipse.aether.artifact.Artifact,org.eclipse.aether.repository.RemoteRepository,java.lang.String)
meth public abstract java.lang.String getPathForRemoteMetadata(org.eclipse.aether.metadata.Metadata,org.eclipse.aether.repository.RemoteRepository,java.lang.String)
meth public abstract org.eclipse.aether.repository.LocalArtifactResult find(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.LocalArtifactRequest)
meth public abstract org.eclipse.aether.repository.LocalMetadataResult find(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.LocalMetadataRequest)
meth public abstract org.eclipse.aether.repository.LocalRepository getRepository()
meth public abstract void add(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.LocalArtifactRegistration)
meth public abstract void add(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.LocalMetadataRegistration)

CLSS public abstract interface org.eclipse.aether.repository.MirrorSelector
meth public abstract org.eclipse.aether.repository.RemoteRepository getMirror(org.eclipse.aether.repository.RemoteRepository)

CLSS public org.eclipse.aether.repository.NoLocalRepositoryManagerException
cons public init(org.eclipse.aether.repository.LocalRepository)
cons public init(org.eclipse.aether.repository.LocalRepository,java.lang.String)
cons public init(org.eclipse.aether.repository.LocalRepository,java.lang.String,java.lang.Throwable)
cons public init(org.eclipse.aether.repository.LocalRepository,java.lang.Throwable)
meth public org.eclipse.aether.repository.LocalRepository getRepository()
supr org.eclipse.aether.RepositoryException
hfds repository

CLSS public final org.eclipse.aether.repository.Proxy
cons public init(java.lang.String,java.lang.String,int)
cons public init(java.lang.String,java.lang.String,int,org.eclipse.aether.repository.Authentication)
fld public final static java.lang.String TYPE_HTTP = "http"
fld public final static java.lang.String TYPE_HTTPS = "https"
meth public boolean equals(java.lang.Object)
meth public int getPort()
meth public int hashCode()
meth public java.lang.String getHost()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public org.eclipse.aether.repository.Authentication getAuthentication()
supr java.lang.Object
hfds auth,host,port,type

CLSS public abstract interface org.eclipse.aether.repository.ProxySelector
meth public abstract org.eclipse.aether.repository.Proxy getProxy(org.eclipse.aether.repository.RemoteRepository)

CLSS public final org.eclipse.aether.repository.RemoteRepository
innr public final static Builder
intf org.eclipse.aether.repository.ArtifactRepository
meth public boolean equals(java.lang.Object)
meth public boolean isBlocked()
meth public boolean isRepositoryManager()
meth public int hashCode()
meth public java.lang.String getContentType()
meth public java.lang.String getHost()
meth public java.lang.String getId()
meth public java.lang.String getProtocol()
meth public java.lang.String getUrl()
meth public java.lang.String toString()
meth public java.util.List<org.eclipse.aether.repository.RemoteRepository> getMirroredRepositories()
meth public org.eclipse.aether.repository.Authentication getAuthentication()
meth public org.eclipse.aether.repository.Proxy getProxy()
meth public org.eclipse.aether.repository.RepositoryPolicy getPolicy(boolean)
supr java.lang.Object
hfds URL_PATTERN,authentication,blocked,host,id,mirroredRepositories,protocol,proxy,releasePolicy,repositoryManager,snapshotPolicy,type,url

CLSS public final static org.eclipse.aether.repository.RemoteRepository$Builder
 outer org.eclipse.aether.repository.RemoteRepository
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(org.eclipse.aether.repository.RemoteRepository)
meth public org.eclipse.aether.repository.RemoteRepository build()
meth public org.eclipse.aether.repository.RemoteRepository$Builder addMirroredRepository(org.eclipse.aether.repository.RemoteRepository)
meth public org.eclipse.aether.repository.RemoteRepository$Builder setAuthentication(org.eclipse.aether.repository.Authentication)
meth public org.eclipse.aether.repository.RemoteRepository$Builder setBlocked(boolean)
meth public org.eclipse.aether.repository.RemoteRepository$Builder setContentType(java.lang.String)
meth public org.eclipse.aether.repository.RemoteRepository$Builder setId(java.lang.String)
meth public org.eclipse.aether.repository.RemoteRepository$Builder setMirroredRepositories(java.util.List<org.eclipse.aether.repository.RemoteRepository>)
meth public org.eclipse.aether.repository.RemoteRepository$Builder setPolicy(org.eclipse.aether.repository.RepositoryPolicy)
meth public org.eclipse.aether.repository.RemoteRepository$Builder setProxy(org.eclipse.aether.repository.Proxy)
meth public org.eclipse.aether.repository.RemoteRepository$Builder setReleasePolicy(org.eclipse.aether.repository.RepositoryPolicy)
meth public org.eclipse.aether.repository.RemoteRepository$Builder setRepositoryManager(boolean)
meth public org.eclipse.aether.repository.RemoteRepository$Builder setSnapshotPolicy(org.eclipse.aether.repository.RepositoryPolicy)
meth public org.eclipse.aether.repository.RemoteRepository$Builder setUrl(java.lang.String)
supr java.lang.Object
hfds AUTH,BLOCKED,DEFAULT_POLICY,ID,MIRRORED,PROXY,RELEASES,REPOMAN,SNAPSHOTS,TYPE,URL,authentication,blocked,delta,id,mirroredRepositories,prototype,proxy,releasePolicy,repositoryManager,snapshotPolicy,type,url

CLSS public final org.eclipse.aether.repository.RepositoryPolicy
cons public init()
cons public init(boolean,java.lang.String,java.lang.String)
fld public final static java.lang.String CHECKSUM_POLICY_FAIL = "fail"
fld public final static java.lang.String CHECKSUM_POLICY_IGNORE = "ignore"
fld public final static java.lang.String CHECKSUM_POLICY_WARN = "warn"
fld public final static java.lang.String UPDATE_POLICY_ALWAYS = "always"
fld public final static java.lang.String UPDATE_POLICY_DAILY = "daily"
fld public final static java.lang.String UPDATE_POLICY_INTERVAL = "interval"
fld public final static java.lang.String UPDATE_POLICY_NEVER = "never"
meth public boolean equals(java.lang.Object)
meth public boolean isEnabled()
meth public int hashCode()
meth public java.lang.String getChecksumPolicy()
meth public java.lang.String getUpdatePolicy()
meth public java.lang.String toString()
supr java.lang.Object
hfds checksumPolicy,enabled,updatePolicy

CLSS public abstract interface org.eclipse.aether.repository.WorkspaceReader
meth public abstract java.io.File findArtifact(org.eclipse.aether.artifact.Artifact)
meth public abstract java.util.List<java.lang.String> findVersions(org.eclipse.aether.artifact.Artifact)
meth public abstract org.eclipse.aether.repository.WorkspaceRepository getRepository()

CLSS public final org.eclipse.aether.repository.WorkspaceRepository
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Object)
intf org.eclipse.aether.repository.ArtifactRepository
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object getKey()
meth public java.lang.String getContentType()
meth public java.lang.String getId()
meth public java.lang.String toString()
supr java.lang.Object
hfds key,type

CLSS abstract interface org.eclipse.aether.repository.package-info

CLSS public abstract interface org.eclipse.aether.resolution.ArtifactDescriptorPolicy
fld public final static int IGNORE_ERRORS = 3
fld public final static int IGNORE_INVALID = 2
fld public final static int IGNORE_MISSING = 1
fld public final static int STRICT = 0
meth public abstract int getPolicy(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.resolution.ArtifactDescriptorPolicyRequest)

CLSS public abstract interface org.eclipse.aether.resolution.ResolutionErrorPolicy
fld public final static int CACHE_ALL = 3
fld public final static int CACHE_DISABLED = 0
fld public final static int CACHE_NOT_FOUND = 1
fld public final static int CACHE_TRANSFER_ERROR = 2
meth public abstract int getArtifactPolicy(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.resolution.ResolutionErrorPolicyRequest<org.eclipse.aether.artifact.Artifact>)
meth public abstract int getMetadataPolicy(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.resolution.ResolutionErrorPolicyRequest<org.eclipse.aether.metadata.Metadata>)

CLSS public abstract org.eclipse.aether.transfer.AbstractTransferListener
cons protected init()
intf org.eclipse.aether.transfer.TransferListener
meth public void transferCorrupted(org.eclipse.aether.transfer.TransferEvent) throws org.eclipse.aether.transfer.TransferCancelledException
meth public void transferFailed(org.eclipse.aether.transfer.TransferEvent)
meth public void transferInitiated(org.eclipse.aether.transfer.TransferEvent) throws org.eclipse.aether.transfer.TransferCancelledException
meth public void transferProgressed(org.eclipse.aether.transfer.TransferEvent) throws org.eclipse.aether.transfer.TransferCancelledException
meth public void transferStarted(org.eclipse.aether.transfer.TransferEvent) throws org.eclipse.aether.transfer.TransferCancelledException
meth public void transferSucceeded(org.eclipse.aether.transfer.TransferEvent)
supr java.lang.Object

CLSS public org.eclipse.aether.transfer.ArtifactFilteredOutException
cons public init(org.eclipse.aether.artifact.Artifact,org.eclipse.aether.repository.RemoteRepository,java.lang.String)
supr org.eclipse.aether.transfer.ArtifactNotFoundException

CLSS public org.eclipse.aether.transfer.ArtifactNotFoundException
cons public init(org.eclipse.aether.artifact.Artifact,org.eclipse.aether.repository.RemoteRepository)
cons public init(org.eclipse.aether.artifact.Artifact,org.eclipse.aether.repository.RemoteRepository,java.lang.String)
cons public init(org.eclipse.aether.artifact.Artifact,org.eclipse.aether.repository.RemoteRepository,java.lang.String,boolean)
cons public init(org.eclipse.aether.artifact.Artifact,org.eclipse.aether.repository.RemoteRepository,java.lang.String,java.lang.Throwable)
supr org.eclipse.aether.transfer.ArtifactTransferException

CLSS public org.eclipse.aether.transfer.ArtifactTransferException
cons public init(org.eclipse.aether.artifact.Artifact,org.eclipse.aether.repository.RemoteRepository,java.lang.String)
cons public init(org.eclipse.aether.artifact.Artifact,org.eclipse.aether.repository.RemoteRepository,java.lang.String,boolean)
cons public init(org.eclipse.aether.artifact.Artifact,org.eclipse.aether.repository.RemoteRepository,java.lang.String,java.lang.Throwable)
cons public init(org.eclipse.aether.artifact.Artifact,org.eclipse.aether.repository.RemoteRepository,java.lang.Throwable)
meth public boolean isFromCache()
meth public org.eclipse.aether.artifact.Artifact getArtifact()
meth public org.eclipse.aether.repository.RemoteRepository getRepository()
supr org.eclipse.aether.RepositoryException
hfds artifact,fromCache,repository

CLSS public org.eclipse.aether.transfer.ChecksumFailureException
cons public init(boolean,java.lang.String,java.lang.Throwable)
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public boolean isRetryWorthy()
meth public java.lang.String getActual()
meth public java.lang.String getExpected()
meth public java.lang.String getExpectedKind()
supr org.eclipse.aether.RepositoryException
hfds actual,expected,expectedKind,retryWorthy

CLSS public org.eclipse.aether.transfer.MetadataNotFoundException
cons public init(org.eclipse.aether.metadata.Metadata,org.eclipse.aether.repository.LocalRepository)
cons public init(org.eclipse.aether.metadata.Metadata,org.eclipse.aether.repository.RemoteRepository)
cons public init(org.eclipse.aether.metadata.Metadata,org.eclipse.aether.repository.RemoteRepository,java.lang.String)
cons public init(org.eclipse.aether.metadata.Metadata,org.eclipse.aether.repository.RemoteRepository,java.lang.String,boolean)
cons public init(org.eclipse.aether.metadata.Metadata,org.eclipse.aether.repository.RemoteRepository,java.lang.String,java.lang.Throwable)
supr org.eclipse.aether.transfer.MetadataTransferException

CLSS public org.eclipse.aether.transfer.MetadataTransferException
cons public init(org.eclipse.aether.metadata.Metadata,org.eclipse.aether.repository.RemoteRepository,java.lang.String)
cons public init(org.eclipse.aether.metadata.Metadata,org.eclipse.aether.repository.RemoteRepository,java.lang.String,boolean)
cons public init(org.eclipse.aether.metadata.Metadata,org.eclipse.aether.repository.RemoteRepository,java.lang.String,java.lang.Throwable)
cons public init(org.eclipse.aether.metadata.Metadata,org.eclipse.aether.repository.RemoteRepository,java.lang.Throwable)
meth public boolean isFromCache()
meth public org.eclipse.aether.metadata.Metadata getMetadata()
meth public org.eclipse.aether.repository.RemoteRepository getRepository()
supr org.eclipse.aether.RepositoryException
hfds fromCache,metadata,repository

CLSS public org.eclipse.aether.transfer.NoRepositoryConnectorException
cons public init(org.eclipse.aether.repository.RemoteRepository)
cons public init(org.eclipse.aether.repository.RemoteRepository,java.lang.String)
cons public init(org.eclipse.aether.repository.RemoteRepository,java.lang.String,java.lang.Throwable)
cons public init(org.eclipse.aether.repository.RemoteRepository,java.lang.Throwable)
meth public org.eclipse.aether.repository.RemoteRepository getRepository()
supr org.eclipse.aether.RepositoryException
hfds repository

CLSS public org.eclipse.aether.transfer.NoRepositoryLayoutException
cons public init(org.eclipse.aether.repository.RemoteRepository)
cons public init(org.eclipse.aether.repository.RemoteRepository,java.lang.String)
cons public init(org.eclipse.aether.repository.RemoteRepository,java.lang.String,java.lang.Throwable)
cons public init(org.eclipse.aether.repository.RemoteRepository,java.lang.Throwable)
meth public org.eclipse.aether.repository.RemoteRepository getRepository()
supr org.eclipse.aether.RepositoryException
hfds repository

CLSS public org.eclipse.aether.transfer.NoTransporterException
cons public init(org.eclipse.aether.repository.RemoteRepository)
cons public init(org.eclipse.aether.repository.RemoteRepository,java.lang.String)
cons public init(org.eclipse.aether.repository.RemoteRepository,java.lang.String,java.lang.Throwable)
cons public init(org.eclipse.aether.repository.RemoteRepository,java.lang.Throwable)
meth public org.eclipse.aether.repository.RemoteRepository getRepository()
supr org.eclipse.aether.RepositoryException
hfds repository

CLSS public org.eclipse.aether.transfer.RepositoryOfflineException
cons public init(org.eclipse.aether.repository.RemoteRepository)
cons public init(org.eclipse.aether.repository.RemoteRepository,java.lang.String)
meth public org.eclipse.aether.repository.RemoteRepository getRepository()
supr org.eclipse.aether.RepositoryException
hfds repository

CLSS public org.eclipse.aether.transfer.TransferCancelledException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr org.eclipse.aether.RepositoryException

CLSS public final org.eclipse.aether.transfer.TransferEvent
innr public final static !enum EventType
innr public final static !enum RequestType
innr public final static Builder
meth public int getDataLength()
meth public java.lang.Exception getException()
meth public java.lang.String toString()
meth public java.nio.ByteBuffer getDataBuffer()
meth public long getTransferredBytes()
meth public org.eclipse.aether.RepositorySystemSession getSession()
meth public org.eclipse.aether.transfer.TransferEvent$EventType getType()
meth public org.eclipse.aether.transfer.TransferEvent$RequestType getRequestType()
meth public org.eclipse.aether.transfer.TransferResource getResource()
supr java.lang.Object
hfds dataBuffer,exception,requestType,resource,session,transferredBytes,type

CLSS public final static org.eclipse.aether.transfer.TransferEvent$Builder
 outer org.eclipse.aether.transfer.TransferEvent
cons public init(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.transfer.TransferResource)
meth public org.eclipse.aether.transfer.TransferEvent build()
meth public org.eclipse.aether.transfer.TransferEvent$Builder addTransferredBytes(long)
meth public org.eclipse.aether.transfer.TransferEvent$Builder copy()
meth public org.eclipse.aether.transfer.TransferEvent$Builder resetType(org.eclipse.aether.transfer.TransferEvent$EventType)
meth public org.eclipse.aether.transfer.TransferEvent$Builder setDataBuffer(byte[],int,int)
meth public org.eclipse.aether.transfer.TransferEvent$Builder setDataBuffer(java.nio.ByteBuffer)
meth public org.eclipse.aether.transfer.TransferEvent$Builder setException(java.lang.Exception)
meth public org.eclipse.aether.transfer.TransferEvent$Builder setRequestType(org.eclipse.aether.transfer.TransferEvent$RequestType)
meth public org.eclipse.aether.transfer.TransferEvent$Builder setTransferredBytes(long)
meth public org.eclipse.aether.transfer.TransferEvent$Builder setType(org.eclipse.aether.transfer.TransferEvent$EventType)
supr java.lang.Object
hfds dataBuffer,exception,requestType,resource,session,transferredBytes,type

CLSS public final static !enum org.eclipse.aether.transfer.TransferEvent$EventType
 outer org.eclipse.aether.transfer.TransferEvent
fld public final static org.eclipse.aether.transfer.TransferEvent$EventType CORRUPTED
fld public final static org.eclipse.aether.transfer.TransferEvent$EventType FAILED
fld public final static org.eclipse.aether.transfer.TransferEvent$EventType INITIATED
fld public final static org.eclipse.aether.transfer.TransferEvent$EventType PROGRESSED
fld public final static org.eclipse.aether.transfer.TransferEvent$EventType STARTED
fld public final static org.eclipse.aether.transfer.TransferEvent$EventType SUCCEEDED
meth public static org.eclipse.aether.transfer.TransferEvent$EventType valueOf(java.lang.String)
meth public static org.eclipse.aether.transfer.TransferEvent$EventType[] values()
supr java.lang.Enum<org.eclipse.aether.transfer.TransferEvent$EventType>

CLSS public final static !enum org.eclipse.aether.transfer.TransferEvent$RequestType
 outer org.eclipse.aether.transfer.TransferEvent
fld public final static org.eclipse.aether.transfer.TransferEvent$RequestType GET
fld public final static org.eclipse.aether.transfer.TransferEvent$RequestType GET_EXISTENCE
fld public final static org.eclipse.aether.transfer.TransferEvent$RequestType PUT
meth public static org.eclipse.aether.transfer.TransferEvent$RequestType valueOf(java.lang.String)
meth public static org.eclipse.aether.transfer.TransferEvent$RequestType[] values()
supr java.lang.Enum<org.eclipse.aether.transfer.TransferEvent$RequestType>

CLSS public abstract interface org.eclipse.aether.transfer.TransferListener
meth public abstract void transferCorrupted(org.eclipse.aether.transfer.TransferEvent) throws org.eclipse.aether.transfer.TransferCancelledException
meth public abstract void transferFailed(org.eclipse.aether.transfer.TransferEvent)
meth public abstract void transferInitiated(org.eclipse.aether.transfer.TransferEvent) throws org.eclipse.aether.transfer.TransferCancelledException
meth public abstract void transferProgressed(org.eclipse.aether.transfer.TransferEvent) throws org.eclipse.aether.transfer.TransferCancelledException
meth public abstract void transferStarted(org.eclipse.aether.transfer.TransferEvent) throws org.eclipse.aether.transfer.TransferCancelledException
meth public abstract void transferSucceeded(org.eclipse.aether.transfer.TransferEvent)

CLSS public final org.eclipse.aether.transfer.TransferResource
cons public init(java.lang.String,java.lang.String,java.io.File,org.eclipse.aether.RequestTrace)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String,java.lang.String,java.io.File,org.eclipse.aether.RequestTrace)
meth public java.io.File getFile()
meth public java.lang.String getRepositoryId()
meth public java.lang.String getRepositoryUrl()
meth public java.lang.String getResourceName()
meth public java.lang.String toString()
meth public long getContentLength()
meth public long getResumeOffset()
meth public long getTransferStartTime()
meth public org.eclipse.aether.RequestTrace getTrace()
meth public org.eclipse.aether.transfer.TransferResource setContentLength(long)
meth public org.eclipse.aether.transfer.TransferResource setResumeOffset(long)
supr java.lang.Object
hfds contentLength,file,repositoryId,repositoryUrl,resourceName,resumeOffset,startTime,trace

CLSS abstract interface org.eclipse.aether.transfer.package-info

CLSS public final org.eclipse.aether.util.ChecksumUtils
meth public static byte[] fromHexString(java.lang.String)
meth public static java.lang.String read(java.io.File) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static java.lang.String toHexString(byte[])
meth public static java.util.Map<java.lang.String,java.lang.Object> calc(byte[],java.util.Collection<java.lang.String>) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static java.util.Map<java.lang.String,java.lang.Object> calc(java.io.File,java.util.Collection<java.lang.String>) throws java.io.IOException
 anno 0 java.lang.Deprecated()
supr java.lang.Object

CLSS public final org.eclipse.aether.util.ConfigUtils
meth public !varargs static boolean getBoolean(java.util.Map<?,?>,boolean,java.lang.String[])
meth public !varargs static boolean getBoolean(org.eclipse.aether.RepositorySystemSession,boolean,java.lang.String[])
meth public !varargs static float getFloat(java.util.Map<?,?>,float,java.lang.String[])
meth public !varargs static float getFloat(org.eclipse.aether.RepositorySystemSession,float,java.lang.String[])
meth public !varargs static int getInteger(java.util.Map<?,?>,int,java.lang.String[])
meth public !varargs static int getInteger(org.eclipse.aether.RepositorySystemSession,int,java.lang.String[])
meth public !varargs static java.lang.Object getObject(java.util.Map<?,?>,java.lang.Object,java.lang.String[])
meth public !varargs static java.lang.Object getObject(org.eclipse.aether.RepositorySystemSession,java.lang.Object,java.lang.String[])
meth public !varargs static java.lang.String getString(java.util.Map<?,?>,java.lang.String,java.lang.String[])
meth public !varargs static java.lang.String getString(org.eclipse.aether.RepositorySystemSession,java.lang.String,java.lang.String[])
meth public !varargs static java.util.List<?> getList(java.util.Map<?,?>,java.util.List<?>,java.lang.String[])
meth public !varargs static java.util.List<?> getList(org.eclipse.aether.RepositorySystemSession,java.util.List<?>,java.lang.String[])
meth public !varargs static java.util.Map<?,?> getMap(java.util.Map<?,?>,java.util.Map<?,?>,java.lang.String[])
meth public !varargs static java.util.Map<?,?> getMap(org.eclipse.aether.RepositorySystemSession,java.util.Map<?,?>,java.lang.String[])
meth public !varargs static long getLong(java.util.Map<?,?>,long,java.lang.String[])
meth public !varargs static long getLong(org.eclipse.aether.RepositorySystemSession,long,java.lang.String[])
meth public static java.util.List<java.lang.String> parseCommaSeparatedNames(java.lang.String)
meth public static java.util.List<java.lang.String> parseCommaSeparatedUniqueNames(java.lang.String)
supr java.lang.Object

CLSS public final org.eclipse.aether.util.DirectoryUtils
meth public static java.nio.file.Path resolveDirectory(java.lang.String,java.nio.file.Path,boolean) throws java.io.IOException
meth public static java.nio.file.Path resolveDirectory(org.eclipse.aether.RepositorySystemSession,java.lang.String,java.lang.String,boolean) throws java.io.IOException
supr java.lang.Object

CLSS public final org.eclipse.aether.util.FileUtils
innr public abstract interface static CollocatedTempFile
innr public abstract interface static FileWriter
innr public abstract interface static TempFile
meth public static org.eclipse.aether.util.FileUtils$CollocatedTempFile newTempFile(java.nio.file.Path) throws java.io.IOException
meth public static org.eclipse.aether.util.FileUtils$TempFile newTempFile() throws java.io.IOException
meth public static void writeFile(java.nio.file.Path,org.eclipse.aether.util.FileUtils$FileWriter) throws java.io.IOException
meth public static void writeFileWithBackup(java.nio.file.Path,org.eclipse.aether.util.FileUtils$FileWriter) throws java.io.IOException
supr java.lang.Object
hfds IS_WINDOWS

CLSS public abstract interface static org.eclipse.aether.util.FileUtils$CollocatedTempFile
 outer org.eclipse.aether.util.FileUtils
intf org.eclipse.aether.util.FileUtils$TempFile
meth public abstract void move() throws java.io.IOException

CLSS public abstract interface static org.eclipse.aether.util.FileUtils$FileWriter
 outer org.eclipse.aether.util.FileUtils
 anno 0 java.lang.FunctionalInterface()
meth public abstract void write(java.nio.file.Path) throws java.io.IOException

CLSS public abstract interface static org.eclipse.aether.util.FileUtils$TempFile
 outer org.eclipse.aether.util.FileUtils
intf java.io.Closeable
meth public abstract java.nio.file.Path getPath()

CLSS public final org.eclipse.aether.util.StringDigestUtil
cons public init(java.lang.String)
meth public java.lang.String digest()
meth public org.eclipse.aether.util.StringDigestUtil update(java.lang.String)
meth public static java.lang.String sha1(java.lang.String)
meth public static org.eclipse.aether.util.StringDigestUtil sha1()
supr java.lang.Object
hfds digest

CLSS public final org.eclipse.aether.util.StringUtils
 anno 0 java.lang.Deprecated()
meth public static boolean isEmpty(java.lang.String)
 anno 0 java.lang.Deprecated()
supr java.lang.Object

CLSS abstract interface org.eclipse.aether.util.package-info

CLSS public final org.eclipse.aether.util.repository.AuthenticationBuilder
cons public init()
meth public org.eclipse.aether.repository.Authentication build()
meth public org.eclipse.aether.util.repository.AuthenticationBuilder addCustom(org.eclipse.aether.repository.Authentication)
meth public org.eclipse.aether.util.repository.AuthenticationBuilder addHostnameVerifier(javax.net.ssl.HostnameVerifier)
meth public org.eclipse.aether.util.repository.AuthenticationBuilder addNtlm(java.lang.String,java.lang.String)
meth public org.eclipse.aether.util.repository.AuthenticationBuilder addPassword(char[])
meth public org.eclipse.aether.util.repository.AuthenticationBuilder addPassword(java.lang.String)
meth public org.eclipse.aether.util.repository.AuthenticationBuilder addPrivateKey(java.lang.String,char[])
meth public org.eclipse.aether.util.repository.AuthenticationBuilder addPrivateKey(java.lang.String,java.lang.String)
meth public org.eclipse.aether.util.repository.AuthenticationBuilder addSecret(java.lang.String,char[])
meth public org.eclipse.aether.util.repository.AuthenticationBuilder addSecret(java.lang.String,java.lang.String)
meth public org.eclipse.aether.util.repository.AuthenticationBuilder addString(java.lang.String,java.lang.String)
meth public org.eclipse.aether.util.repository.AuthenticationBuilder addUsername(java.lang.String)
supr java.lang.Object
hfds authentications

CLSS public final org.eclipse.aether.util.repository.ChainedLocalRepositoryManager
cons public init(org.eclipse.aether.repository.LocalRepositoryManager,java.util.List<org.eclipse.aether.repository.LocalRepositoryManager>,boolean)
intf org.eclipse.aether.repository.LocalRepositoryManager
meth public java.lang.String getPathForLocalArtifact(org.eclipse.aether.artifact.Artifact)
meth public java.lang.String getPathForLocalMetadata(org.eclipse.aether.metadata.Metadata)
meth public java.lang.String getPathForRemoteArtifact(org.eclipse.aether.artifact.Artifact,org.eclipse.aether.repository.RemoteRepository,java.lang.String)
meth public java.lang.String getPathForRemoteMetadata(org.eclipse.aether.metadata.Metadata,org.eclipse.aether.repository.RemoteRepository,java.lang.String)
meth public java.lang.String toString()
meth public org.eclipse.aether.repository.LocalArtifactResult find(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.LocalArtifactRequest)
meth public org.eclipse.aether.repository.LocalMetadataResult find(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.LocalMetadataRequest)
meth public org.eclipse.aether.repository.LocalRepository getRepository()
meth public void add(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.LocalArtifactRegistration)
meth public void add(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.repository.LocalMetadataRegistration)
supr java.lang.Object
hfds head,ignoreTailAvailability,tail

CLSS public final org.eclipse.aether.util.repository.ChainedWorkspaceReader
cons public !varargs init(org.eclipse.aether.repository.WorkspaceReader[])
intf org.eclipse.aether.repository.WorkspaceReader
meth public java.io.File findArtifact(org.eclipse.aether.artifact.Artifact)
meth public java.util.List<java.lang.String> findVersions(org.eclipse.aether.artifact.Artifact)
meth public org.eclipse.aether.repository.WorkspaceRepository getRepository()
meth public static org.eclipse.aether.repository.WorkspaceReader newInstance(org.eclipse.aether.repository.WorkspaceReader,org.eclipse.aether.repository.WorkspaceReader)
supr java.lang.Object
hfds readers,repository
hcls Key

CLSS public final org.eclipse.aether.util.repository.ConservativeAuthenticationSelector
cons public init(org.eclipse.aether.repository.AuthenticationSelector)
intf org.eclipse.aether.repository.AuthenticationSelector
meth public org.eclipse.aether.repository.Authentication getAuthentication(org.eclipse.aether.repository.RemoteRepository)
supr java.lang.Object
hfds selector

CLSS public final org.eclipse.aether.util.repository.ConservativeProxySelector
cons public init(org.eclipse.aether.repository.ProxySelector)
intf org.eclipse.aether.repository.ProxySelector
meth public org.eclipse.aether.repository.Proxy getProxy(org.eclipse.aether.repository.RemoteRepository)
supr java.lang.Object
hfds selector

CLSS public final org.eclipse.aether.util.repository.DefaultAuthenticationSelector
cons public init()
intf org.eclipse.aether.repository.AuthenticationSelector
meth public org.eclipse.aether.repository.Authentication getAuthentication(org.eclipse.aether.repository.RemoteRepository)
meth public org.eclipse.aether.util.repository.DefaultAuthenticationSelector add(java.lang.String,org.eclipse.aether.repository.Authentication)
supr java.lang.Object
hfds repos

CLSS public final org.eclipse.aether.util.repository.DefaultMirrorSelector
cons public init()
intf org.eclipse.aether.repository.MirrorSelector
meth public org.eclipse.aether.repository.RemoteRepository getMirror(org.eclipse.aether.repository.RemoteRepository)
meth public org.eclipse.aether.util.repository.DefaultMirrorSelector add(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,java.lang.String,java.lang.String)
meth public org.eclipse.aether.util.repository.DefaultMirrorSelector add(java.lang.String,java.lang.String,java.lang.String,boolean,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds EXTERNAL_HTTP_WILDCARD,EXTERNAL_WILDCARD,WILDCARD,mirrors
hcls MirrorDef

CLSS public final org.eclipse.aether.util.repository.DefaultProxySelector
cons public init()
intf org.eclipse.aether.repository.ProxySelector
meth public org.eclipse.aether.repository.Proxy getProxy(org.eclipse.aether.repository.RemoteRepository)
meth public org.eclipse.aether.util.repository.DefaultProxySelector add(org.eclipse.aether.repository.Proxy,java.lang.String)
supr java.lang.Object
hfds proxies
hcls NonProxyHosts,ProxyDef

CLSS public final org.eclipse.aether.util.repository.JreProxySelector
cons public init()
intf org.eclipse.aether.repository.ProxySelector
meth public org.eclipse.aether.repository.Proxy getProxy(org.eclipse.aether.repository.RemoteRepository)
supr java.lang.Object
hcls JreProxyAuthentication

CLSS public final org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy
cons public init(boolean,boolean)
cons public init(int)
intf org.eclipse.aether.resolution.ArtifactDescriptorPolicy
meth public int getPolicy(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.resolution.ArtifactDescriptorPolicyRequest)
supr java.lang.Object
hfds policy

CLSS public final org.eclipse.aether.util.repository.SimpleResolutionErrorPolicy
cons public init(boolean,boolean)
cons public init(int)
cons public init(int,int)
intf org.eclipse.aether.resolution.ResolutionErrorPolicy
meth public int getArtifactPolicy(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.resolution.ResolutionErrorPolicyRequest<org.eclipse.aether.artifact.Artifact>)
meth public int getMetadataPolicy(org.eclipse.aether.RepositorySystemSession,org.eclipse.aether.resolution.ResolutionErrorPolicyRequest<org.eclipse.aether.metadata.Metadata>)
supr java.lang.Object
hfds artifactPolicy,metadataPolicy

CLSS abstract interface org.eclipse.aether.util.repository.package-info

CLSS public final org.eclipse.aether.util.version.GenericVersionScheme
cons public init()
intf org.eclipse.aether.version.VersionScheme
meth public !varargs static void main(java.lang.String[])
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public org.eclipse.aether.version.Version parseVersion(java.lang.String) throws org.eclipse.aether.version.InvalidVersionSpecificationException
meth public org.eclipse.aether.version.VersionConstraint parseVersionConstraint(java.lang.String) throws org.eclipse.aether.version.InvalidVersionSpecificationException
meth public org.eclipse.aether.version.VersionRange parseVersionRange(java.lang.String) throws org.eclipse.aether.version.InvalidVersionSpecificationException
supr java.lang.Object

CLSS public final org.eclipse.aether.util.version.UnionVersionRange
intf org.eclipse.aether.version.VersionRange
meth public !varargs static org.eclipse.aether.version.VersionRange from(org.eclipse.aether.version.VersionRange[])
meth public boolean containsVersion(org.eclipse.aether.version.Version)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.eclipse.aether.version.VersionRange$Bound getLowerBound()
meth public org.eclipse.aether.version.VersionRange$Bound getUpperBound()
meth public static org.eclipse.aether.version.VersionRange from(java.util.Collection<? extends org.eclipse.aether.version.VersionRange>)
supr java.lang.Object
hfds lowerBound,ranges,upperBound

CLSS abstract interface org.eclipse.aether.util.version.package-info

CLSS public org.eclipse.aether.version.InvalidVersionSpecificationException
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,java.lang.Throwable)
meth public java.lang.String getVersion()
supr org.eclipse.aether.RepositoryException
hfds version

CLSS public abstract interface org.eclipse.aether.version.Version
intf java.lang.Comparable<org.eclipse.aether.version.Version>
meth public abstract java.lang.String toString()

CLSS public abstract interface org.eclipse.aether.version.VersionConstraint
meth public abstract boolean containsVersion(org.eclipse.aether.version.Version)
meth public abstract org.eclipse.aether.version.Version getVersion()
meth public abstract org.eclipse.aether.version.VersionRange getRange()

CLSS public abstract interface org.eclipse.aether.version.VersionRange
innr public final static Bound
meth public abstract boolean containsVersion(org.eclipse.aether.version.Version)
meth public abstract org.eclipse.aether.version.VersionRange$Bound getLowerBound()
meth public abstract org.eclipse.aether.version.VersionRange$Bound getUpperBound()

CLSS public final static org.eclipse.aether.version.VersionRange$Bound
 outer org.eclipse.aether.version.VersionRange
cons public init(org.eclipse.aether.version.Version,boolean)
meth public boolean equals(java.lang.Object)
meth public boolean isInclusive()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.eclipse.aether.version.Version getVersion()
supr java.lang.Object
hfds inclusive,version

CLSS public abstract interface org.eclipse.aether.version.VersionScheme
meth public abstract org.eclipse.aether.version.Version parseVersion(java.lang.String) throws org.eclipse.aether.version.InvalidVersionSpecificationException
meth public abstract org.eclipse.aether.version.VersionConstraint parseVersionConstraint(java.lang.String) throws org.eclipse.aether.version.InvalidVersionSpecificationException
meth public abstract org.eclipse.aether.version.VersionRange parseVersionRange(java.lang.String) throws org.eclipse.aether.version.InvalidVersionSpecificationException

CLSS abstract interface org.eclipse.aether.version.package-info

CLSS public abstract interface !annotation org.eclipse.sisu.Typed
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class<?>[] value()

CLSS public abstract interface !annotation org.eclipse.sisu.bean.IgnoreSetters
 anno 0 java.lang.annotation.Inherited()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public org.jdom2.Attribute
cons protected init()
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,int)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String,int,org.jdom2.Namespace)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.lang.String,org.jdom2.AttributeType)
cons public init(java.lang.String,java.lang.String,org.jdom2.AttributeType,org.jdom2.Namespace)
cons public init(java.lang.String,java.lang.String,org.jdom2.Namespace)
fld protected boolean specified
fld protected java.lang.String name
fld protected java.lang.String value
fld protected org.jdom2.AttributeType type
fld protected org.jdom2.Element parent
fld protected org.jdom2.Namespace namespace
fld public final static org.jdom2.AttributeType CDATA_TYPE
fld public final static org.jdom2.AttributeType ENTITIES_TYPE
fld public final static org.jdom2.AttributeType ENTITY_TYPE
fld public final static org.jdom2.AttributeType ENUMERATED_TYPE
fld public final static org.jdom2.AttributeType IDREFS_TYPE
fld public final static org.jdom2.AttributeType IDREF_TYPE
fld public final static org.jdom2.AttributeType ID_TYPE
fld public final static org.jdom2.AttributeType NMTOKENS_TYPE
fld public final static org.jdom2.AttributeType NMTOKEN_TYPE
fld public final static org.jdom2.AttributeType NOTATION_TYPE
fld public final static org.jdom2.AttributeType UNDECLARED_TYPE
intf java.io.Serializable
intf java.lang.Cloneable
intf org.jdom2.NamespaceAware
meth protected org.jdom2.Attribute setParent(org.jdom2.Element)
meth public boolean getBooleanValue() throws org.jdom2.DataConversionException
meth public boolean isSpecified()
meth public double getDoubleValue() throws org.jdom2.DataConversionException
meth public float getFloatValue() throws org.jdom2.DataConversionException
meth public int getIntValue() throws org.jdom2.DataConversionException
meth public java.lang.String getName()
meth public java.lang.String getNamespacePrefix()
meth public java.lang.String getNamespaceURI()
meth public java.lang.String getQualifiedName()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public java.util.List<org.jdom2.Namespace> getNamespacesInScope()
meth public java.util.List<org.jdom2.Namespace> getNamespacesInherited()
meth public java.util.List<org.jdom2.Namespace> getNamespacesIntroduced()
meth public long getLongValue() throws org.jdom2.DataConversionException
meth public org.jdom2.Attribute clone()
meth public org.jdom2.Attribute detach()
meth public org.jdom2.Attribute setAttributeType(int)
 anno 0 java.lang.Deprecated()
meth public org.jdom2.Attribute setAttributeType(org.jdom2.AttributeType)
meth public org.jdom2.Attribute setName(java.lang.String)
meth public org.jdom2.Attribute setNamespace(org.jdom2.Namespace)
meth public org.jdom2.Attribute setValue(java.lang.String)
meth public org.jdom2.AttributeType getAttributeType()
meth public org.jdom2.Document getDocument()
meth public org.jdom2.Element getParent()
meth public org.jdom2.Namespace getNamespace()
meth public void setSpecified(boolean)
supr java.lang.Object
hfds serialVersionUID

CLSS public final !enum org.jdom2.AttributeType
fld public final static org.jdom2.AttributeType CDATA
fld public final static org.jdom2.AttributeType ENTITIES
fld public final static org.jdom2.AttributeType ENTITY
fld public final static org.jdom2.AttributeType ENUMERATION
fld public final static org.jdom2.AttributeType ID
fld public final static org.jdom2.AttributeType IDREF
fld public final static org.jdom2.AttributeType IDREFS
fld public final static org.jdom2.AttributeType NMTOKEN
fld public final static org.jdom2.AttributeType NMTOKENS
fld public final static org.jdom2.AttributeType NOTATION
fld public final static org.jdom2.AttributeType UNDECLARED
meth public final static org.jdom2.AttributeType byIndex(int)
 anno 0 java.lang.Deprecated()
meth public final static org.jdom2.AttributeType getAttributeType(java.lang.String)
meth public static org.jdom2.AttributeType valueOf(java.lang.String)
meth public static org.jdom2.AttributeType[] values()
supr java.lang.Enum<org.jdom2.AttributeType>

CLSS public org.jdom2.CDATA
cons protected init()
cons public init(java.lang.String)
meth protected org.jdom2.CDATA setParent(org.jdom2.Parent)
meth public java.lang.String toString()
meth public org.jdom2.CDATA clone()
meth public org.jdom2.CDATA detach()
meth public org.jdom2.CDATA setText(java.lang.String)
meth public void append(java.lang.String)
meth public void append(org.jdom2.Text)
supr org.jdom2.Text
hfds serialVersionUID

CLSS public org.jdom2.Comment
cons protected init()
cons public init(java.lang.String)
fld protected java.lang.String text
meth protected org.jdom2.Comment setParent(org.jdom2.Parent)
meth public java.lang.String getText()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public org.jdom2.Comment clone()
meth public org.jdom2.Comment detach()
meth public org.jdom2.Comment setText(java.lang.String)
supr org.jdom2.Content
hfds serialVersionUID

CLSS public abstract org.jdom2.Content
cons protected init(org.jdom2.Content$CType)
fld protected final org.jdom2.Content$CType ctype
fld protected org.jdom2.Parent parent
innr public final static !enum CType
intf java.io.Serializable
intf java.lang.Cloneable
intf org.jdom2.NamespaceAware
meth protected org.jdom2.Content setParent(org.jdom2.Parent)
meth public abstract java.lang.String getValue()
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final org.jdom2.Content$CType getCType()
meth public final org.jdom2.Element getParentElement()
meth public java.util.List<org.jdom2.Namespace> getNamespacesInScope()
meth public java.util.List<org.jdom2.Namespace> getNamespacesInherited()
meth public java.util.List<org.jdom2.Namespace> getNamespacesIntroduced()
meth public org.jdom2.Content clone()
meth public org.jdom2.Content detach()
meth public org.jdom2.Document getDocument()
meth public org.jdom2.Parent getParent()
supr java.lang.Object
hfds serialVersionUID

CLSS public final static !enum org.jdom2.Content$CType
 outer org.jdom2.Content
fld public final static org.jdom2.Content$CType CDATA
fld public final static org.jdom2.Content$CType Comment
fld public final static org.jdom2.Content$CType DocType
fld public final static org.jdom2.Content$CType Element
fld public final static org.jdom2.Content$CType EntityRef
fld public final static org.jdom2.Content$CType ProcessingInstruction
fld public final static org.jdom2.Content$CType Text
meth public static org.jdom2.Content$CType valueOf(java.lang.String)
meth public static org.jdom2.Content$CType[] values()
supr java.lang.Enum<org.jdom2.Content$CType>

CLSS public org.jdom2.DataConversionException
cons public init(java.lang.String,java.lang.String)
supr org.jdom2.JDOMException
hfds serialVersionUID

CLSS public org.jdom2.DefaultJDOMFactory
cons public init()
intf org.jdom2.JDOMFactory
meth public final org.jdom2.CDATA cdata(java.lang.String)
meth public final org.jdom2.Comment comment(java.lang.String)
meth public final org.jdom2.DocType docType(java.lang.String)
meth public final org.jdom2.DocType docType(java.lang.String,java.lang.String)
meth public final org.jdom2.DocType docType(java.lang.String,java.lang.String,java.lang.String)
meth public final org.jdom2.Element element(java.lang.String)
meth public final org.jdom2.Element element(java.lang.String,java.lang.String)
meth public final org.jdom2.Element element(java.lang.String,java.lang.String,java.lang.String)
meth public final org.jdom2.Element element(java.lang.String,org.jdom2.Namespace)
meth public final org.jdom2.EntityRef entityRef(java.lang.String)
meth public final org.jdom2.EntityRef entityRef(java.lang.String,java.lang.String)
meth public final org.jdom2.EntityRef entityRef(java.lang.String,java.lang.String,java.lang.String)
meth public final org.jdom2.ProcessingInstruction processingInstruction(java.lang.String)
meth public final org.jdom2.ProcessingInstruction processingInstruction(java.lang.String,java.lang.String)
meth public final org.jdom2.ProcessingInstruction processingInstruction(java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
meth public final org.jdom2.Text text(java.lang.String)
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String)
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String,int)
 anno 0 java.lang.Deprecated()
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String,int,org.jdom2.Namespace)
 anno 0 java.lang.Deprecated()
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String,org.jdom2.AttributeType)
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String,org.jdom2.AttributeType,org.jdom2.Namespace)
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String,org.jdom2.Namespace)
meth public org.jdom2.CDATA cdata(int,int,java.lang.String)
meth public org.jdom2.Comment comment(int,int,java.lang.String)
meth public org.jdom2.DocType docType(int,int,java.lang.String)
meth public org.jdom2.DocType docType(int,int,java.lang.String,java.lang.String)
meth public org.jdom2.DocType docType(int,int,java.lang.String,java.lang.String,java.lang.String)
meth public org.jdom2.Document document(org.jdom2.Element)
meth public org.jdom2.Document document(org.jdom2.Element,org.jdom2.DocType)
meth public org.jdom2.Document document(org.jdom2.Element,org.jdom2.DocType,java.lang.String)
meth public org.jdom2.Element element(int,int,java.lang.String)
meth public org.jdom2.Element element(int,int,java.lang.String,java.lang.String)
meth public org.jdom2.Element element(int,int,java.lang.String,java.lang.String,java.lang.String)
meth public org.jdom2.Element element(int,int,java.lang.String,org.jdom2.Namespace)
meth public org.jdom2.EntityRef entityRef(int,int,java.lang.String)
meth public org.jdom2.EntityRef entityRef(int,int,java.lang.String,java.lang.String)
meth public org.jdom2.EntityRef entityRef(int,int,java.lang.String,java.lang.String,java.lang.String)
meth public org.jdom2.ProcessingInstruction processingInstruction(int,int,java.lang.String)
meth public org.jdom2.ProcessingInstruction processingInstruction(int,int,java.lang.String,java.lang.String)
meth public org.jdom2.ProcessingInstruction processingInstruction(int,int,java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
meth public org.jdom2.Text text(int,int,java.lang.String)
meth public void addContent(org.jdom2.Parent,org.jdom2.Content)
meth public void addNamespaceDeclaration(org.jdom2.Element,org.jdom2.Namespace)
meth public void setAttribute(org.jdom2.Element,org.jdom2.Attribute)
meth public void setRoot(org.jdom2.Document,org.jdom2.Element)
supr java.lang.Object

CLSS public org.jdom2.DocType
cons protected init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
fld protected java.lang.String elementName
fld protected java.lang.String internalSubset
fld protected java.lang.String publicID
fld protected java.lang.String systemID
meth protected org.jdom2.DocType setParent(org.jdom2.Parent)
meth public java.lang.String getElementName()
meth public java.lang.String getInternalSubset()
meth public java.lang.String getPublicID()
meth public java.lang.String getSystemID()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public org.jdom2.DocType clone()
meth public org.jdom2.DocType detach()
meth public org.jdom2.DocType setElementName(java.lang.String)
meth public org.jdom2.DocType setPublicID(java.lang.String)
meth public org.jdom2.DocType setSystemID(java.lang.String)
meth public org.jdom2.Document getParent()
meth public void setInternalSubset(java.lang.String)
supr org.jdom2.Content
hfds serialVersionUID

CLSS public org.jdom2.Document
cons public init()
cons public init(java.util.List<? extends org.jdom2.Content>)
cons public init(org.jdom2.Element)
cons public init(org.jdom2.Element,org.jdom2.DocType)
cons public init(org.jdom2.Element,org.jdom2.DocType,java.lang.String)
fld protected java.lang.String baseURI
intf java.lang.Cloneable
intf org.jdom2.Parent
meth public <%0 extends org.jdom2.Content> java.util.List<{%%0}> getContent(org.jdom2.filter.Filter<{%%0}>)
meth public <%0 extends org.jdom2.Content> java.util.List<{%%0}> removeContent(org.jdom2.filter.Filter<{%%0}>)
meth public <%0 extends org.jdom2.Content> org.jdom2.util.IteratorIterable<{%%0}> getDescendants(org.jdom2.filter.Filter<{%%0}>)
meth public boolean hasRootElement()
meth public boolean removeContent(org.jdom2.Content)
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final java.lang.String getBaseURI()
meth public final void setBaseURI(java.lang.String)
meth public int getContentSize()
meth public int indexOf(org.jdom2.Content)
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.String toString()
meth public java.util.List<org.jdom2.Content> cloneContent()
meth public java.util.List<org.jdom2.Content> getContent()
meth public java.util.List<org.jdom2.Content> removeContent()
meth public java.util.List<org.jdom2.Namespace> getNamespacesInScope()
meth public java.util.List<org.jdom2.Namespace> getNamespacesInherited()
meth public java.util.List<org.jdom2.Namespace> getNamespacesIntroduced()
meth public org.jdom2.Content getContent(int)
meth public org.jdom2.Content removeContent(int)
meth public org.jdom2.DocType getDocType()
meth public org.jdom2.Document addContent(int,java.util.Collection<? extends org.jdom2.Content>)
meth public org.jdom2.Document addContent(int,org.jdom2.Content)
meth public org.jdom2.Document addContent(java.util.Collection<? extends org.jdom2.Content>)
meth public org.jdom2.Document addContent(org.jdom2.Content)
meth public org.jdom2.Document clone()
meth public org.jdom2.Document getDocument()
meth public org.jdom2.Document setContent(int,java.util.Collection<? extends org.jdom2.Content>)
meth public org.jdom2.Document setContent(int,org.jdom2.Content)
meth public org.jdom2.Document setContent(java.util.Collection<? extends org.jdom2.Content>)
meth public org.jdom2.Document setContent(org.jdom2.Content)
meth public org.jdom2.Document setDocType(org.jdom2.DocType)
meth public org.jdom2.Document setRootElement(org.jdom2.Element)
meth public org.jdom2.Element detachRootElement()
meth public org.jdom2.Element getRootElement()
meth public org.jdom2.Parent getParent()
meth public org.jdom2.util.IteratorIterable<org.jdom2.Content> getDescendants()
meth public void canContainContent(org.jdom2.Content,int,boolean)
meth public void setProperty(java.lang.String,java.lang.Object)
supr java.lang.Object
hfds content,propertyMap,serialVersionUID

CLSS public org.jdom2.Element
cons protected init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,org.jdom2.Namespace)
fld protected java.lang.String name
fld protected org.jdom2.Namespace namespace
intf org.jdom2.Parent
meth public <%0 extends org.jdom2.Content> java.util.List<{%%0}> getContent(org.jdom2.filter.Filter<{%%0}>)
meth public <%0 extends org.jdom2.Content> java.util.List<{%%0}> removeContent(org.jdom2.filter.Filter<{%%0}>)
meth public <%0 extends org.jdom2.Content> org.jdom2.util.IteratorIterable<{%%0}> getDescendants(org.jdom2.filter.Filter<{%%0}>)
meth public <%0 extends org.jdom2.Content> void sortContent(org.jdom2.filter.Filter<{%%0}>,java.util.Comparator<? super {%%0}>)
meth public boolean addNamespaceDeclaration(org.jdom2.Namespace)
meth public boolean coalesceText(boolean)
meth public boolean hasAdditionalNamespaces()
meth public boolean hasAttributes()
meth public boolean isAncestor(org.jdom2.Element)
meth public boolean isRootElement()
meth public boolean removeAttribute(java.lang.String)
meth public boolean removeAttribute(java.lang.String,org.jdom2.Namespace)
meth public boolean removeAttribute(org.jdom2.Attribute)
meth public boolean removeChild(java.lang.String)
meth public boolean removeChild(java.lang.String,org.jdom2.Namespace)
meth public boolean removeChildren(java.lang.String)
meth public boolean removeChildren(java.lang.String,org.jdom2.Namespace)
meth public boolean removeContent(org.jdom2.Content)
meth public int getAttributesSize()
meth public int getContentSize()
meth public int indexOf(org.jdom2.Content)
meth public java.lang.String getAttributeValue(java.lang.String)
meth public java.lang.String getAttributeValue(java.lang.String,java.lang.String)
meth public java.lang.String getAttributeValue(java.lang.String,org.jdom2.Namespace)
meth public java.lang.String getAttributeValue(java.lang.String,org.jdom2.Namespace,java.lang.String)
meth public java.lang.String getChildText(java.lang.String)
meth public java.lang.String getChildText(java.lang.String,org.jdom2.Namespace)
meth public java.lang.String getChildTextNormalize(java.lang.String)
meth public java.lang.String getChildTextNormalize(java.lang.String,org.jdom2.Namespace)
meth public java.lang.String getChildTextTrim(java.lang.String)
meth public java.lang.String getChildTextTrim(java.lang.String,org.jdom2.Namespace)
meth public java.lang.String getName()
meth public java.lang.String getNamespacePrefix()
meth public java.lang.String getNamespaceURI()
meth public java.lang.String getQualifiedName()
meth public java.lang.String getText()
meth public java.lang.String getTextNormalize()
meth public java.lang.String getTextTrim()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public java.net.URI getXMLBaseURI() throws java.net.URISyntaxException
meth public java.util.List<org.jdom2.Attribute> getAttributes()
meth public java.util.List<org.jdom2.Content> cloneContent()
meth public java.util.List<org.jdom2.Content> getContent()
meth public java.util.List<org.jdom2.Content> removeContent()
meth public java.util.List<org.jdom2.Element> getChildren()
meth public java.util.List<org.jdom2.Element> getChildren(java.lang.String)
meth public java.util.List<org.jdom2.Element> getChildren(java.lang.String,org.jdom2.Namespace)
meth public java.util.List<org.jdom2.Namespace> getAdditionalNamespaces()
meth public java.util.List<org.jdom2.Namespace> getNamespacesInScope()
meth public java.util.List<org.jdom2.Namespace> getNamespacesInherited()
meth public java.util.List<org.jdom2.Namespace> getNamespacesIntroduced()
meth public org.jdom2.Attribute getAttribute(java.lang.String)
meth public org.jdom2.Attribute getAttribute(java.lang.String,org.jdom2.Namespace)
meth public org.jdom2.Content getContent(int)
meth public org.jdom2.Content removeContent(int)
meth public org.jdom2.Element addContent(int,java.util.Collection<? extends org.jdom2.Content>)
meth public org.jdom2.Element addContent(int,org.jdom2.Content)
meth public org.jdom2.Element addContent(java.lang.String)
meth public org.jdom2.Element addContent(java.util.Collection<? extends org.jdom2.Content>)
meth public org.jdom2.Element addContent(org.jdom2.Content)
meth public org.jdom2.Element clone()
meth public org.jdom2.Element detach()
meth public org.jdom2.Element getChild(java.lang.String)
meth public org.jdom2.Element getChild(java.lang.String,org.jdom2.Namespace)
meth public org.jdom2.Element setAttribute(java.lang.String,java.lang.String)
meth public org.jdom2.Element setAttribute(java.lang.String,java.lang.String,org.jdom2.Namespace)
meth public org.jdom2.Element setAttribute(org.jdom2.Attribute)
meth public org.jdom2.Element setAttributes(java.util.Collection<? extends org.jdom2.Attribute>)
meth public org.jdom2.Element setContent(int,org.jdom2.Content)
meth public org.jdom2.Element setContent(java.util.Collection<? extends org.jdom2.Content>)
meth public org.jdom2.Element setContent(org.jdom2.Content)
meth public org.jdom2.Element setName(java.lang.String)
meth public org.jdom2.Element setNamespace(org.jdom2.Namespace)
meth public org.jdom2.Element setText(java.lang.String)
meth public org.jdom2.Namespace getNamespace()
meth public org.jdom2.Namespace getNamespace(java.lang.String)
meth public org.jdom2.Parent setContent(int,java.util.Collection<? extends org.jdom2.Content>)
meth public org.jdom2.util.IteratorIterable<org.jdom2.Content> getDescendants()
meth public void canContainContent(org.jdom2.Content,int,boolean)
meth public void removeNamespaceDeclaration(org.jdom2.Namespace)
meth public void sortAttributes(java.util.Comparator<? super org.jdom2.Attribute>)
meth public void sortChildren(java.util.Comparator<? super org.jdom2.Element>)
meth public void sortContent(java.util.Comparator<? super org.jdom2.Content>)
supr org.jdom2.Content
hfds INITIAL_ARRAY_SIZE,additionalNamespaces,attributes,content,serialVersionUID

CLSS public org.jdom2.EntityRef
cons protected init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
fld protected java.lang.String name
fld protected java.lang.String publicID
fld protected java.lang.String systemID
meth protected org.jdom2.EntityRef setParent(org.jdom2.Parent)
meth public java.lang.String getName()
meth public java.lang.String getPublicID()
meth public java.lang.String getSystemID()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public org.jdom2.Element getParent()
meth public org.jdom2.EntityRef clone()
meth public org.jdom2.EntityRef detach()
meth public org.jdom2.EntityRef setName(java.lang.String)
meth public org.jdom2.EntityRef setPublicID(java.lang.String)
meth public org.jdom2.EntityRef setSystemID(java.lang.String)
supr org.jdom2.Content
hfds serialVersionUID

CLSS public org.jdom2.IllegalAddException
cons public init(java.lang.String)
supr java.lang.IllegalArgumentException
hfds serialVersionUID

CLSS public org.jdom2.IllegalDataException
cons public init(java.lang.String)
supr java.lang.IllegalArgumentException
hfds serialVersionUID

CLSS public org.jdom2.IllegalNameException
cons public init(java.lang.String)
supr java.lang.IllegalArgumentException
hfds serialVersionUID

CLSS public org.jdom2.IllegalTargetException
cons public init(java.lang.String)
supr java.lang.IllegalArgumentException
hfds serialVersionUID

CLSS public final org.jdom2.JDOMConstants
fld public final static java.lang.String JDOM2_FEATURE_JDOMRESULT = "http://jdom.org/jdom2/transform/JDOMResult/feature"
fld public final static java.lang.String JDOM2_FEATURE_JDOMSOURCE = "http://jdom.org/jdom2/transform/JDOMSource/feature"
fld public final static java.lang.String JDOM2_PROPERTY_LINE_SEPARATOR = "org.jdom2.output.LineSeparator"
fld public final static java.lang.String JDOM2_PROPERTY_XPATH_FACTORY = "org.jdom2.xpath.XPathFactory"
fld public final static java.lang.String NS_PREFIX_DEFAULT = ""
fld public final static java.lang.String NS_PREFIX_XML = "xml"
fld public final static java.lang.String NS_PREFIX_XMLNS = "xmlns"
fld public final static java.lang.String NS_URI_DEFAULT = ""
fld public final static java.lang.String NS_URI_XML = "http://www.w3.org/XML/1998/namespace"
fld public final static java.lang.String NS_URI_XMLNS = "http://www.w3.org/2000/xmlns/"
fld public final static java.lang.String SAX_FEATURE_EXTERNAL_ENT = "http://xml.org/sax/features/external-general-entities"
fld public final static java.lang.String SAX_FEATURE_NAMESPACES = "http://xml.org/sax/features/namespaces"
fld public final static java.lang.String SAX_FEATURE_NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes"
fld public final static java.lang.String SAX_FEATURE_VALIDATION = "http://xml.org/sax/features/validation"
fld public final static java.lang.String SAX_PROPERTY_DECLARATION_HANDLER = "http://xml.org/sax/properties/declaration-handler"
fld public final static java.lang.String SAX_PROPERTY_DECLARATION_HANDLER_ALT = "http://xml.org/sax/handlers/DeclHandler"
fld public final static java.lang.String SAX_PROPERTY_LEXICAL_HANDLER = "http://xml.org/sax/properties/lexical-handler"
fld public final static java.lang.String SAX_PROPERTY_LEXICAL_HANDLER_ALT = "http://xml.org/sax/handlers/LexicalHandler"
supr java.lang.Object

CLSS public org.jdom2.JDOMException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public abstract interface org.jdom2.JDOMFactory
meth public abstract org.jdom2.Attribute attribute(java.lang.String,java.lang.String)
meth public abstract org.jdom2.Attribute attribute(java.lang.String,java.lang.String,int)
 anno 0 java.lang.Deprecated()
meth public abstract org.jdom2.Attribute attribute(java.lang.String,java.lang.String,int,org.jdom2.Namespace)
 anno 0 java.lang.Deprecated()
meth public abstract org.jdom2.Attribute attribute(java.lang.String,java.lang.String,org.jdom2.AttributeType)
meth public abstract org.jdom2.Attribute attribute(java.lang.String,java.lang.String,org.jdom2.AttributeType,org.jdom2.Namespace)
meth public abstract org.jdom2.Attribute attribute(java.lang.String,java.lang.String,org.jdom2.Namespace)
meth public abstract org.jdom2.CDATA cdata(int,int,java.lang.String)
meth public abstract org.jdom2.CDATA cdata(java.lang.String)
meth public abstract org.jdom2.Comment comment(int,int,java.lang.String)
meth public abstract org.jdom2.Comment comment(java.lang.String)
meth public abstract org.jdom2.DocType docType(int,int,java.lang.String)
meth public abstract org.jdom2.DocType docType(int,int,java.lang.String,java.lang.String)
meth public abstract org.jdom2.DocType docType(int,int,java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.jdom2.DocType docType(java.lang.String)
meth public abstract org.jdom2.DocType docType(java.lang.String,java.lang.String)
meth public abstract org.jdom2.DocType docType(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.jdom2.Document document(org.jdom2.Element)
meth public abstract org.jdom2.Document document(org.jdom2.Element,org.jdom2.DocType)
meth public abstract org.jdom2.Document document(org.jdom2.Element,org.jdom2.DocType,java.lang.String)
meth public abstract org.jdom2.Element element(int,int,java.lang.String)
meth public abstract org.jdom2.Element element(int,int,java.lang.String,java.lang.String)
meth public abstract org.jdom2.Element element(int,int,java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.jdom2.Element element(int,int,java.lang.String,org.jdom2.Namespace)
meth public abstract org.jdom2.Element element(java.lang.String)
meth public abstract org.jdom2.Element element(java.lang.String,java.lang.String)
meth public abstract org.jdom2.Element element(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.jdom2.Element element(java.lang.String,org.jdom2.Namespace)
meth public abstract org.jdom2.EntityRef entityRef(int,int,java.lang.String)
meth public abstract org.jdom2.EntityRef entityRef(int,int,java.lang.String,java.lang.String)
meth public abstract org.jdom2.EntityRef entityRef(int,int,java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.jdom2.EntityRef entityRef(java.lang.String)
meth public abstract org.jdom2.EntityRef entityRef(java.lang.String,java.lang.String)
meth public abstract org.jdom2.EntityRef entityRef(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.jdom2.ProcessingInstruction processingInstruction(int,int,java.lang.String)
meth public abstract org.jdom2.ProcessingInstruction processingInstruction(int,int,java.lang.String,java.lang.String)
meth public abstract org.jdom2.ProcessingInstruction processingInstruction(int,int,java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
meth public abstract org.jdom2.ProcessingInstruction processingInstruction(java.lang.String)
meth public abstract org.jdom2.ProcessingInstruction processingInstruction(java.lang.String,java.lang.String)
meth public abstract org.jdom2.ProcessingInstruction processingInstruction(java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
meth public abstract org.jdom2.Text text(int,int,java.lang.String)
meth public abstract org.jdom2.Text text(java.lang.String)
meth public abstract void addContent(org.jdom2.Parent,org.jdom2.Content)
meth public abstract void addNamespaceDeclaration(org.jdom2.Element,org.jdom2.Namespace)
meth public abstract void setAttribute(org.jdom2.Element,org.jdom2.Attribute)
meth public abstract void setRoot(org.jdom2.Document,org.jdom2.Element)

CLSS public final org.jdom2.Namespace
fld public final static org.jdom2.Namespace NO_NAMESPACE
fld public final static org.jdom2.Namespace XML_NAMESPACE
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getPrefix()
meth public java.lang.String getURI()
meth public java.lang.String toString()
meth public static org.jdom2.Namespace getNamespace(java.lang.String)
meth public static org.jdom2.Namespace getNamespace(java.lang.String,java.lang.String)
supr java.lang.Object
hfds XMLNS_NAMESPACE,namespacemap,prefix,serialVersionUID,uri
hcls NamespaceSerializationProxy

CLSS public abstract interface org.jdom2.NamespaceAware
meth public abstract java.util.List<org.jdom2.Namespace> getNamespacesInScope()
meth public abstract java.util.List<org.jdom2.Namespace> getNamespacesInherited()
meth public abstract java.util.List<org.jdom2.Namespace> getNamespacesIntroduced()

CLSS public abstract interface org.jdom2.Parent
intf java.io.Serializable
intf java.lang.Cloneable
intf org.jdom2.NamespaceAware
meth public abstract <%0 extends org.jdom2.Content> java.util.List<{%%0}> getContent(org.jdom2.filter.Filter<{%%0}>)
meth public abstract <%0 extends org.jdom2.Content> java.util.List<{%%0}> removeContent(org.jdom2.filter.Filter<{%%0}>)
meth public abstract <%0 extends org.jdom2.Content> org.jdom2.util.IteratorIterable<{%%0}> getDescendants(org.jdom2.filter.Filter<{%%0}>)
meth public abstract boolean removeContent(org.jdom2.Content)
meth public abstract int getContentSize()
meth public abstract int indexOf(org.jdom2.Content)
meth public abstract java.lang.Object clone()
meth public abstract java.util.List<org.jdom2.Content> cloneContent()
meth public abstract java.util.List<org.jdom2.Content> getContent()
meth public abstract java.util.List<org.jdom2.Content> removeContent()
meth public abstract org.jdom2.Content getContent(int)
meth public abstract org.jdom2.Content removeContent(int)
meth public abstract org.jdom2.Document getDocument()
meth public abstract org.jdom2.Parent addContent(int,java.util.Collection<? extends org.jdom2.Content>)
meth public abstract org.jdom2.Parent addContent(int,org.jdom2.Content)
meth public abstract org.jdom2.Parent addContent(java.util.Collection<? extends org.jdom2.Content>)
meth public abstract org.jdom2.Parent addContent(org.jdom2.Content)
meth public abstract org.jdom2.Parent getParent()
meth public abstract org.jdom2.util.IteratorIterable<org.jdom2.Content> getDescendants()
meth public abstract void canContainContent(org.jdom2.Content,int,boolean)

CLSS public org.jdom2.ProcessingInstruction
cons protected init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
fld protected java.lang.String rawData
fld protected java.lang.String target
fld protected java.util.Map<java.lang.String,java.lang.String> mapData
meth protected org.jdom2.ProcessingInstruction setParent(org.jdom2.Parent)
meth public boolean removePseudoAttribute(java.lang.String)
meth public java.lang.String getData()
meth public java.lang.String getPseudoAttributeValue(java.lang.String)
meth public java.lang.String getTarget()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getPseudoAttributeNames()
meth public org.jdom2.ProcessingInstruction clone()
meth public org.jdom2.ProcessingInstruction detach()
meth public org.jdom2.ProcessingInstruction setData(java.lang.String)
meth public org.jdom2.ProcessingInstruction setData(java.util.Map<java.lang.String,java.lang.String>)
meth public org.jdom2.ProcessingInstruction setPseudoAttribute(java.lang.String,java.lang.String)
meth public org.jdom2.ProcessingInstruction setTarget(java.lang.String)
supr org.jdom2.Content
hfds serialVersionUID

CLSS public org.jdom2.SlimJDOMFactory
cons public init()
cons public init(boolean)
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String)
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String,int)
 anno 0 java.lang.Deprecated()
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String,int,org.jdom2.Namespace)
 anno 0 java.lang.Deprecated()
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String,org.jdom2.AttributeType)
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String,org.jdom2.AttributeType,org.jdom2.Namespace)
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String,org.jdom2.Namespace)
meth public org.jdom2.CDATA cdata(int,int,java.lang.String)
meth public org.jdom2.Comment comment(int,int,java.lang.String)
meth public org.jdom2.DocType docType(int,int,java.lang.String)
meth public org.jdom2.DocType docType(int,int,java.lang.String,java.lang.String)
meth public org.jdom2.DocType docType(int,int,java.lang.String,java.lang.String,java.lang.String)
meth public org.jdom2.Element element(int,int,java.lang.String)
meth public org.jdom2.Element element(int,int,java.lang.String,java.lang.String)
meth public org.jdom2.Element element(int,int,java.lang.String,java.lang.String,java.lang.String)
meth public org.jdom2.Element element(int,int,java.lang.String,org.jdom2.Namespace)
meth public org.jdom2.EntityRef entityRef(int,int,java.lang.String)
meth public org.jdom2.EntityRef entityRef(int,int,java.lang.String,java.lang.String)
meth public org.jdom2.EntityRef entityRef(int,int,java.lang.String,java.lang.String,java.lang.String)
meth public org.jdom2.ProcessingInstruction processingInstruction(int,int,java.lang.String)
meth public org.jdom2.ProcessingInstruction processingInstruction(int,int,java.lang.String,java.lang.String)
meth public org.jdom2.ProcessingInstruction processingInstruction(int,int,java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
meth public org.jdom2.Text text(int,int,java.lang.String)
meth public void clearCache()
supr org.jdom2.DefaultJDOMFactory
hfds cache,cachetext

CLSS public org.jdom2.Text
cons protected init()
cons protected init(org.jdom2.Content$CType)
cons public init(java.lang.String)
fld protected java.lang.String value
meth protected org.jdom2.Text setParent(org.jdom2.Parent)
meth public java.lang.String getText()
meth public java.lang.String getTextNormalize()
meth public java.lang.String getTextTrim()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public org.jdom2.Element getParent()
meth public org.jdom2.Text clone()
meth public org.jdom2.Text detach()
meth public org.jdom2.Text setText(java.lang.String)
meth public static java.lang.String normalizeString(java.lang.String)
meth public void append(java.lang.String)
meth public void append(org.jdom2.Text)
supr org.jdom2.Content
hfds EMPTY_STRING,serialVersionUID

CLSS public org.jdom2.UncheckedJDOMFactory
cons public init()
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String)
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String,int)
 anno 0 java.lang.Deprecated()
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String,int,org.jdom2.Namespace)
 anno 0 java.lang.Deprecated()
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String,org.jdom2.AttributeType)
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String,org.jdom2.AttributeType,org.jdom2.Namespace)
meth public org.jdom2.Attribute attribute(java.lang.String,java.lang.String,org.jdom2.Namespace)
meth public org.jdom2.CDATA cdata(int,int,java.lang.String)
meth public org.jdom2.Comment comment(int,int,java.lang.String)
meth public org.jdom2.DocType docType(int,int,java.lang.String)
meth public org.jdom2.DocType docType(int,int,java.lang.String,java.lang.String)
meth public org.jdom2.DocType docType(int,int,java.lang.String,java.lang.String,java.lang.String)
meth public org.jdom2.Document document(org.jdom2.Element)
meth public org.jdom2.Document document(org.jdom2.Element,org.jdom2.DocType)
meth public org.jdom2.Document document(org.jdom2.Element,org.jdom2.DocType,java.lang.String)
meth public org.jdom2.Element element(int,int,java.lang.String)
meth public org.jdom2.Element element(int,int,java.lang.String,java.lang.String)
meth public org.jdom2.Element element(int,int,java.lang.String,java.lang.String,java.lang.String)
meth public org.jdom2.Element element(int,int,java.lang.String,org.jdom2.Namespace)
meth public org.jdom2.EntityRef entityRef(int,int,java.lang.String)
meth public org.jdom2.EntityRef entityRef(int,int,java.lang.String,java.lang.String)
meth public org.jdom2.EntityRef entityRef(int,int,java.lang.String,java.lang.String,java.lang.String)
meth public org.jdom2.ProcessingInstruction processingInstruction(int,int,java.lang.String)
meth public org.jdom2.ProcessingInstruction processingInstruction(int,int,java.lang.String,java.lang.String)
meth public org.jdom2.ProcessingInstruction processingInstruction(int,int,java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
meth public org.jdom2.Text text(int,int,java.lang.String)
meth public void addContent(org.jdom2.Parent,org.jdom2.Content)
meth public void addNamespaceDeclaration(org.jdom2.Element,org.jdom2.Namespace)
meth public void setAttribute(org.jdom2.Element,org.jdom2.Attribute)
meth public void setRoot(org.jdom2.Document,org.jdom2.Element)
supr org.jdom2.DefaultJDOMFactory

CLSS public final org.jdom2.Verifier
meth public final static boolean isAllXMLWhitespace(java.lang.String)
meth public static boolean isHexDigit(char)
meth public static boolean isHighSurrogate(char)
meth public static boolean isLowSurrogate(char)
meth public static boolean isURICharacter(char)
meth public static boolean isXMLCharacter(int)
meth public static boolean isXMLCombiningChar(char)
meth public static boolean isXMLDigit(char)
meth public static boolean isXMLExtender(char)
meth public static boolean isXMLLetter(char)
meth public static boolean isXMLLetterOrDigit(char)
meth public static boolean isXMLNameCharacter(char)
meth public static boolean isXMLNameStartCharacter(char)
meth public static boolean isXMLPublicIDCharacter(char)
meth public static boolean isXMLWhitespace(char)
meth public static int decodeSurrogatePair(char,char)
meth public static java.lang.String checkAttributeName(java.lang.String)
meth public static java.lang.String checkCDATASection(java.lang.String)
meth public static java.lang.String checkCharacterData(java.lang.String)
meth public static java.lang.String checkCommentData(java.lang.String)
meth public static java.lang.String checkElementName(java.lang.String)
meth public static java.lang.String checkNamespaceCollision(org.jdom2.Attribute,org.jdom2.Element)
meth public static java.lang.String checkNamespaceCollision(org.jdom2.Attribute,org.jdom2.Element,int)
meth public static java.lang.String checkNamespaceCollision(org.jdom2.Namespace,java.util.List<?>)
meth public static java.lang.String checkNamespaceCollision(org.jdom2.Namespace,java.util.List<?>,int)
meth public static java.lang.String checkNamespaceCollision(org.jdom2.Namespace,org.jdom2.Attribute)
meth public static java.lang.String checkNamespaceCollision(org.jdom2.Namespace,org.jdom2.Element)
meth public static java.lang.String checkNamespaceCollision(org.jdom2.Namespace,org.jdom2.Element,int)
meth public static java.lang.String checkNamespaceCollision(org.jdom2.Namespace,org.jdom2.Namespace)
meth public static java.lang.String checkNamespacePrefix(java.lang.String)
meth public static java.lang.String checkNamespaceURI(java.lang.String)
meth public static java.lang.String checkProcessingInstructionData(java.lang.String)
meth public static java.lang.String checkProcessingInstructionTarget(java.lang.String)
meth public static java.lang.String checkPublicID(java.lang.String)
meth public static java.lang.String checkSystemLiteral(java.lang.String)
meth public static java.lang.String checkURI(java.lang.String)
meth public static java.lang.String checkXMLName(java.lang.String)
supr java.lang.Object
hfds CHARCNT,CHARFLAGS,LENCONST,MASKURICHAR,MASKXMLCHARACTER,MASKXMLCOMBINING,MASKXMLDIGIT,MASKXMLLETTER,MASKXMLLETTERORDIGIT,MASKXMLNAMECHAR,MASKXMLSTARTCHAR,VALCONST

CLSS public abstract org.jdom2.filter.AbstractFilter<%0 extends java.lang.Object>
cons public init()
intf org.jdom2.filter.Filter<{org.jdom2.filter.AbstractFilter%0}>
meth public <%0 extends java.lang.Object> org.jdom2.filter.Filter<{%%0}> refine(org.jdom2.filter.Filter<{%%0}>)
meth public final boolean matches(java.lang.Object)
meth public final org.jdom2.filter.Filter<? extends org.jdom2.Content> or(org.jdom2.filter.Filter<?>)
meth public final org.jdom2.filter.Filter<?> negate()
meth public final org.jdom2.filter.Filter<{org.jdom2.filter.AbstractFilter%0}> and(org.jdom2.filter.Filter<?>)
meth public java.util.List<{org.jdom2.filter.AbstractFilter%0}> filter(java.util.List<?>)
supr java.lang.Object
hfds serialVersionUID

CLSS public org.jdom2.filter.AttributeFilter
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,org.jdom2.Namespace)
cons public init(org.jdom2.Namespace)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public org.jdom2.Attribute filter(java.lang.Object)
supr org.jdom2.filter.AbstractFilter<org.jdom2.Attribute>
hfds name,namespace,serialVersionUID

CLSS public org.jdom2.filter.ContentFilter
cons public init()
cons public init(boolean)
cons public init(int)
fld public final static int CDATA = 2
fld public final static int COMMENT = 8
fld public final static int DOCTYPE = 128
fld public final static int DOCUMENT = 64
fld public final static int ELEMENT = 1
fld public final static int ENTITYREF = 32
fld public final static int PI = 16
fld public final static int TEXT = 4
meth public boolean equals(java.lang.Object)
meth public int getFilterMask()
meth public int hashCode()
meth public org.jdom2.Content filter(java.lang.Object)
meth public void setCDATAVisible(boolean)
meth public void setCommentVisible(boolean)
meth public void setDefaultMask()
meth public void setDocTypeVisible(boolean)
meth public void setDocumentContent()
meth public void setElementContent()
meth public void setElementVisible(boolean)
meth public void setEntityRefVisible(boolean)
meth public void setFilterMask(int)
meth public void setPIVisible(boolean)
meth public void setTextVisible(boolean)
supr org.jdom2.filter.AbstractFilter<org.jdom2.Content>
hfds filterMask,serialVersionUID

CLSS public org.jdom2.filter.ElementFilter
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,org.jdom2.Namespace)
cons public init(org.jdom2.Namespace)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.jdom2.Element filter(java.lang.Object)
supr org.jdom2.filter.AbstractFilter<org.jdom2.Element>
hfds name,namespace,serialVersionUID

CLSS public abstract interface org.jdom2.filter.Filter<%0 extends java.lang.Object>
intf java.io.Serializable
meth public abstract <%0 extends java.lang.Object> org.jdom2.filter.Filter<{%%0}> refine(org.jdom2.filter.Filter<{%%0}>)
meth public abstract boolean matches(java.lang.Object)
meth public abstract java.util.List<{org.jdom2.filter.Filter%0}> filter(java.util.List<?>)
meth public abstract org.jdom2.filter.Filter<?> negate()
meth public abstract org.jdom2.filter.Filter<?> or(org.jdom2.filter.Filter<?>)
meth public abstract org.jdom2.filter.Filter<{org.jdom2.filter.Filter%0}> and(org.jdom2.filter.Filter<?>)
meth public abstract {org.jdom2.filter.Filter%0} filter(java.lang.Object)

CLSS public final org.jdom2.filter.Filters
meth public final static <%0 extends java.lang.Object> org.jdom2.filter.Filter<{%%0}> fclass(java.lang.Class<{%%0}>)
meth public final static org.jdom2.filter.Filter<java.lang.Boolean> fboolean()
meth public final static org.jdom2.filter.Filter<java.lang.Double> fdouble()
meth public final static org.jdom2.filter.Filter<java.lang.Object> fpassthrough()
meth public final static org.jdom2.filter.Filter<java.lang.String> fstring()
meth public final static org.jdom2.filter.Filter<org.jdom2.Attribute> attribute()
meth public final static org.jdom2.filter.Filter<org.jdom2.Attribute> attribute(java.lang.String)
meth public final static org.jdom2.filter.Filter<org.jdom2.Attribute> attribute(java.lang.String,org.jdom2.Namespace)
meth public final static org.jdom2.filter.Filter<org.jdom2.Attribute> attribute(org.jdom2.Namespace)
meth public final static org.jdom2.filter.Filter<org.jdom2.CDATA> cdata()
meth public final static org.jdom2.filter.Filter<org.jdom2.Comment> comment()
meth public final static org.jdom2.filter.Filter<org.jdom2.Content> content()
meth public final static org.jdom2.filter.Filter<org.jdom2.DocType> doctype()
meth public final static org.jdom2.filter.Filter<org.jdom2.Document> document()
meth public final static org.jdom2.filter.Filter<org.jdom2.Element> element()
meth public final static org.jdom2.filter.Filter<org.jdom2.Element> element(java.lang.String)
meth public final static org.jdom2.filter.Filter<org.jdom2.Element> element(java.lang.String,org.jdom2.Namespace)
meth public final static org.jdom2.filter.Filter<org.jdom2.Element> element(org.jdom2.Namespace)
meth public final static org.jdom2.filter.Filter<org.jdom2.EntityRef> entityref()
meth public final static org.jdom2.filter.Filter<org.jdom2.ProcessingInstruction> processinginstruction()
meth public final static org.jdom2.filter.Filter<org.jdom2.Text> text()
meth public final static org.jdom2.filter.Filter<org.jdom2.Text> textOnly()
supr java.lang.Object
hfds fattribute,fboolean,fcdata,fcomment,fcontent,fdoctype,fdocument,fdouble,felement,fentityref,fpassthrough,fpi,fstring,ftext,ftextonly

CLSS public org.jdom2.input.DOMBuilder
cons public init()
meth public org.jdom2.CDATA build(org.w3c.dom.CDATASection)
meth public org.jdom2.Comment build(org.w3c.dom.Comment)
meth public org.jdom2.DocType build(org.w3c.dom.DocumentType)
meth public org.jdom2.Document build(org.w3c.dom.Document)
meth public org.jdom2.Element build(org.w3c.dom.Element)
meth public org.jdom2.EntityRef build(org.w3c.dom.EntityReference)
meth public org.jdom2.JDOMFactory getFactory()
meth public org.jdom2.ProcessingInstruction build(org.w3c.dom.ProcessingInstruction)
meth public org.jdom2.Text build(org.w3c.dom.Text)
meth public void setFactory(org.jdom2.JDOMFactory)
supr java.lang.Object
hfds factory

CLSS public org.jdom2.input.JDOMParseException
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,java.lang.Throwable,org.jdom2.Document)
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
meth public org.jdom2.Document getPartialDocument()
supr org.jdom2.JDOMException
hfds partialDocument,serialVersionUID

CLSS public org.jdom2.input.SAXBuilder
cons public init()
cons public init(boolean)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,boolean)
 anno 0 java.lang.Deprecated()
cons public init(org.jdom2.input.sax.XMLReaderJDOMFactory)
cons public init(org.jdom2.input.sax.XMLReaderJDOMFactory,org.jdom2.input.sax.SAXHandlerFactory,org.jdom2.JDOMFactory)
intf org.jdom2.input.sax.SAXEngine
meth protected org.xml.sax.XMLReader createParser() throws org.jdom2.JDOMException
meth protected void configureParser(org.xml.sax.XMLReader,org.jdom2.input.sax.SAXHandler) throws org.jdom2.JDOMException
meth public boolean getExpandEntities()
meth public boolean getIgnoringBoundaryWhitespace()
meth public boolean getIgnoringElementContentWhitespace()
meth public boolean getReuseParser()
meth public boolean getValidation()
 anno 0 java.lang.Deprecated()
meth public boolean isValidating()
meth public java.lang.String getDriverClass()
 anno 0 java.lang.Deprecated()
meth public org.jdom2.Document build(java.io.File) throws java.io.IOException,org.jdom2.JDOMException
meth public org.jdom2.Document build(java.io.InputStream) throws java.io.IOException,org.jdom2.JDOMException
meth public org.jdom2.Document build(java.io.InputStream,java.lang.String) throws java.io.IOException,org.jdom2.JDOMException
meth public org.jdom2.Document build(java.io.Reader) throws java.io.IOException,org.jdom2.JDOMException
meth public org.jdom2.Document build(java.io.Reader,java.lang.String) throws java.io.IOException,org.jdom2.JDOMException
meth public org.jdom2.Document build(java.lang.String) throws java.io.IOException,org.jdom2.JDOMException
meth public org.jdom2.Document build(java.net.URL) throws java.io.IOException,org.jdom2.JDOMException
meth public org.jdom2.Document build(org.xml.sax.InputSource) throws java.io.IOException,org.jdom2.JDOMException
meth public org.jdom2.JDOMFactory getFactory()
 anno 0 java.lang.Deprecated()
meth public org.jdom2.JDOMFactory getJDOMFactory()
meth public org.jdom2.input.sax.SAXEngine buildEngine() throws org.jdom2.JDOMException
meth public org.jdom2.input.sax.SAXHandlerFactory getSAXHandlerFactory()
meth public org.jdom2.input.sax.XMLReaderJDOMFactory getXMLReaderFactory()
meth public org.xml.sax.DTDHandler getDTDHandler()
meth public org.xml.sax.EntityResolver getEntityResolver()
meth public org.xml.sax.ErrorHandler getErrorHandler()
meth public org.xml.sax.XMLFilter getXMLFilter()
meth public void setDTDHandler(org.xml.sax.DTDHandler)
meth public void setEntityResolver(org.xml.sax.EntityResolver)
meth public void setErrorHandler(org.xml.sax.ErrorHandler)
meth public void setExpandEntities(boolean)
meth public void setFactory(org.jdom2.JDOMFactory)
 anno 0 java.lang.Deprecated()
meth public void setFastReconfigure(boolean)
 anno 0 java.lang.Deprecated()
meth public void setFeature(java.lang.String,boolean)
meth public void setIgnoringBoundaryWhitespace(boolean)
meth public void setIgnoringElementContentWhitespace(boolean)
meth public void setJDOMFactory(org.jdom2.JDOMFactory)
meth public void setProperty(java.lang.String,java.lang.Object)
meth public void setReuseParser(boolean)
meth public void setSAXHandlerFactory(org.jdom2.input.sax.SAXHandlerFactory)
meth public void setValidation(boolean)
 anno 0 java.lang.Deprecated()
meth public void setXMLFilter(org.xml.sax.XMLFilter)
meth public void setXMLReaderFactory(org.jdom2.input.sax.XMLReaderJDOMFactory)
supr java.lang.Object
hfds DEFAULTJDOMFAC,DEFAULTSAXHANDLERFAC,engine,features,handlerfac,ignoringBoundaryWhite,ignoringWhite,jdomfac,properties,readerfac,reuseParser,saxDTDHandler,saxEntityResolver,saxErrorHandler,saxXMLFilter

CLSS public org.jdom2.input.StAXEventBuilder
cons public init()
meth public org.jdom2.Document build(javax.xml.stream.XMLEventReader) throws org.jdom2.JDOMException
meth public org.jdom2.JDOMFactory getFactory()
meth public void setFactory(org.jdom2.JDOMFactory)
supr java.lang.Object
hfds factory

CLSS public org.jdom2.input.StAXStreamBuilder
cons public init()
meth public java.util.List<org.jdom2.Content> buildFragments(javax.xml.stream.XMLStreamReader,org.jdom2.input.stax.StAXFilter) throws org.jdom2.JDOMException
meth public org.jdom2.Content fragment(javax.xml.stream.XMLStreamReader) throws org.jdom2.JDOMException
meth public org.jdom2.Document build(javax.xml.stream.XMLStreamReader) throws org.jdom2.JDOMException
meth public org.jdom2.JDOMFactory getFactory()
meth public void setFactory(org.jdom2.JDOMFactory)
supr java.lang.Object
hfds builderfactory

CLSS public org.jdom2.input.StAXStreamWriter
cons public init()
cons public init(org.jdom2.JDOMFactory,boolean)
intf javax.xml.stream.XMLStreamWriter
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.String getPrefix(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public javax.xml.namespace.NamespaceContext getNamespaceContext()
meth public org.jdom2.Document getDocument()
meth public void close() throws javax.xml.stream.XMLStreamException
meth public void flush() throws javax.xml.stream.XMLStreamException
meth public void setDefaultNamespace(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void setNamespaceContext(javax.xml.namespace.NamespaceContext) throws javax.xml.stream.XMLStreamException
meth public void setPrefix(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeAttribute(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeAttribute(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeAttribute(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeCData(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeCharacters(char[],int,int) throws javax.xml.stream.XMLStreamException
meth public void writeCharacters(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeComment(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeDTD(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeDefaultNamespace(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeEmptyElement(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeEmptyElement(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeEmptyElement(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeEndDocument() throws javax.xml.stream.XMLStreamException
meth public void writeEndElement() throws javax.xml.stream.XMLStreamException
meth public void writeEntityRef(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeNamespace(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeProcessingInstruction(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeProcessingInstruction(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeStartDocument() throws javax.xml.stream.XMLStreamException
meth public void writeStartDocument(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeStartDocument(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeStartElement(java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeStartElement(java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
meth public void writeStartElement(java.lang.String,java.lang.String,java.lang.String) throws javax.xml.stream.XMLStreamException
supr java.lang.Object
hfds DEFFAC,activeelement,activetext,boundstack,document,done,factory,genprefix,globalcontext,isempty,parent,pendingns,repairnamespace,usednsstack

CLSS public abstract org.jdom2.input.sax.AbstractReaderSchemaFactory
cons public init(javax.xml.parsers.SAXParserFactory,javax.xml.validation.Schema)
intf org.jdom2.input.sax.XMLReaderJDOMFactory
meth public boolean isValidating()
meth public org.xml.sax.XMLReader createXMLReader() throws org.jdom2.JDOMException
supr java.lang.Object
hfds saxfac

CLSS public org.jdom2.input.sax.AbstractReaderXSDFactory
cons public !varargs init(javax.xml.parsers.SAXParserFactory,org.jdom2.input.sax.AbstractReaderXSDFactory$SchemaFactoryProvider,java.io.File[]) throws org.jdom2.JDOMException
cons public !varargs init(javax.xml.parsers.SAXParserFactory,org.jdom2.input.sax.AbstractReaderXSDFactory$SchemaFactoryProvider,java.lang.String[]) throws org.jdom2.JDOMException
cons public !varargs init(javax.xml.parsers.SAXParserFactory,org.jdom2.input.sax.AbstractReaderXSDFactory$SchemaFactoryProvider,java.net.URL[]) throws org.jdom2.JDOMException
cons public !varargs init(javax.xml.parsers.SAXParserFactory,org.jdom2.input.sax.AbstractReaderXSDFactory$SchemaFactoryProvider,javax.xml.transform.Source[]) throws org.jdom2.JDOMException
innr protected abstract interface static SchemaFactoryProvider
supr org.jdom2.input.sax.AbstractReaderSchemaFactory

CLSS protected abstract interface static org.jdom2.input.sax.AbstractReaderXSDFactory$SchemaFactoryProvider
 outer org.jdom2.input.sax.AbstractReaderXSDFactory
meth public abstract javax.xml.validation.SchemaFactory getSchemaFactory()

CLSS public org.jdom2.input.sax.BuilderErrorHandler
cons public init()
intf org.xml.sax.ErrorHandler
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr java.lang.Object

CLSS public final org.jdom2.input.sax.DefaultSAXHandlerFactory
cons public init()
intf org.jdom2.input.sax.SAXHandlerFactory
meth public org.jdom2.input.sax.SAXHandler createSAXHandler(org.jdom2.JDOMFactory)
supr java.lang.Object
hcls DefaultSAXHandler

CLSS public org.jdom2.input.sax.SAXBuilderEngine
cons public init(org.xml.sax.XMLReader,org.jdom2.input.sax.SAXHandler,boolean)
intf org.jdom2.input.sax.SAXEngine
meth public boolean getExpandEntities()
meth public boolean getIgnoringBoundaryWhitespace()
meth public boolean getIgnoringElementContentWhitespace()
meth public boolean isValidating()
meth public org.jdom2.Document build(java.io.File) throws java.io.IOException,org.jdom2.JDOMException
meth public org.jdom2.Document build(java.io.InputStream) throws java.io.IOException,org.jdom2.JDOMException
meth public org.jdom2.Document build(java.io.InputStream,java.lang.String) throws java.io.IOException,org.jdom2.JDOMException
meth public org.jdom2.Document build(java.io.Reader) throws java.io.IOException,org.jdom2.JDOMException
meth public org.jdom2.Document build(java.io.Reader,java.lang.String) throws java.io.IOException,org.jdom2.JDOMException
meth public org.jdom2.Document build(java.lang.String) throws java.io.IOException,org.jdom2.JDOMException
meth public org.jdom2.Document build(java.net.URL) throws java.io.IOException,org.jdom2.JDOMException
meth public org.jdom2.Document build(org.xml.sax.InputSource) throws java.io.IOException,org.jdom2.JDOMException
meth public org.jdom2.JDOMFactory getJDOMFactory()
meth public org.xml.sax.DTDHandler getDTDHandler()
meth public org.xml.sax.EntityResolver getEntityResolver()
meth public org.xml.sax.ErrorHandler getErrorHandler()
supr java.lang.Object
hfds saxHandler,saxParser,validating

CLSS public abstract interface org.jdom2.input.sax.SAXEngine
meth public abstract boolean getExpandEntities()
meth public abstract boolean getIgnoringBoundaryWhitespace()
meth public abstract boolean getIgnoringElementContentWhitespace()
meth public abstract boolean isValidating()
meth public abstract org.jdom2.Document build(java.io.File) throws java.io.IOException,org.jdom2.JDOMException
meth public abstract org.jdom2.Document build(java.io.InputStream) throws java.io.IOException,org.jdom2.JDOMException
meth public abstract org.jdom2.Document build(java.io.InputStream,java.lang.String) throws java.io.IOException,org.jdom2.JDOMException
meth public abstract org.jdom2.Document build(java.io.Reader) throws java.io.IOException,org.jdom2.JDOMException
meth public abstract org.jdom2.Document build(java.io.Reader,java.lang.String) throws java.io.IOException,org.jdom2.JDOMException
meth public abstract org.jdom2.Document build(java.lang.String) throws java.io.IOException,org.jdom2.JDOMException
meth public abstract org.jdom2.Document build(java.net.URL) throws java.io.IOException,org.jdom2.JDOMException
meth public abstract org.jdom2.Document build(org.xml.sax.InputSource) throws java.io.IOException,org.jdom2.JDOMException
meth public abstract org.jdom2.JDOMFactory getJDOMFactory()
meth public abstract org.xml.sax.DTDHandler getDTDHandler()
meth public abstract org.xml.sax.EntityResolver getEntityResolver()
meth public abstract org.xml.sax.ErrorHandler getErrorHandler()

CLSS public org.jdom2.input.sax.SAXHandler
cons public init()
cons public init(org.jdom2.JDOMFactory)
intf org.xml.sax.DTDHandler
intf org.xml.sax.ext.DeclHandler
intf org.xml.sax.ext.LexicalHandler
meth protected void flushCharacters() throws org.xml.sax.SAXException
meth protected void flushCharacters(java.lang.String) throws org.xml.sax.SAXException
meth protected void pushElement(org.jdom2.Element)
meth protected void resetSubCLass()
meth public boolean getExpandEntities()
meth public boolean getIgnoringBoundaryWhitespace()
meth public boolean getIgnoringElementContentWhitespace()
meth public final void reset()
meth public org.jdom2.Document getDocument()
meth public org.jdom2.Element getCurrentElement() throws org.xml.sax.SAXException
meth public org.jdom2.JDOMFactory getFactory()
meth public org.xml.sax.Locator getDocumentLocator()
meth public void attributeDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void comment(char[],int,int) throws org.xml.sax.SAXException
meth public void elementDecl(java.lang.String,java.lang.String)
meth public void endCDATA() throws org.xml.sax.SAXException
meth public void endDTD()
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void externalEntityDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void internalEntityDecl(java.lang.String,java.lang.String)
meth public void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void setExpandEntities(boolean)
meth public void setIgnoringBoundaryWhitespace(boolean)
meth public void setIgnoringElementContentWhitespace(boolean)
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startCDATA()
meth public void startDTD(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument()
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
supr org.xml.sax.helpers.DefaultHandler
hfds atRoot,currentDocument,currentElement,currentLocator,declaredNamespaces,entityDepth,expand,externalEntities,factory,ignoringBoundaryWhite,ignoringWhite,inCDATA,inDTD,inInternalSubset,internalSubset,lastcol,lastline,previousCDATA,suppress,textBuffer

CLSS public abstract interface org.jdom2.input.sax.SAXHandlerFactory
meth public abstract org.jdom2.input.sax.SAXHandler createSAXHandler(org.jdom2.JDOMFactory)

CLSS public org.jdom2.input.sax.XMLReaderJAXPFactory
cons public init(java.lang.String,java.lang.ClassLoader,boolean)
intf org.jdom2.input.sax.XMLReaderJDOMFactory
meth public boolean isValidating()
meth public org.xml.sax.XMLReader createXMLReader() throws org.jdom2.JDOMException
supr java.lang.Object
hfds instance,validating

CLSS public abstract interface org.jdom2.input.sax.XMLReaderJDOMFactory
meth public abstract boolean isValidating()
meth public abstract org.xml.sax.XMLReader createXMLReader() throws org.jdom2.JDOMException

CLSS public org.jdom2.input.sax.XMLReaderSAX2Factory
cons public init(boolean)
cons public init(boolean,java.lang.String)
intf org.jdom2.input.sax.XMLReaderJDOMFactory
meth public boolean isValidating()
meth public java.lang.String getDriverClassName()
meth public org.xml.sax.XMLReader createXMLReader() throws org.jdom2.JDOMException
supr java.lang.Object
hfds saxdriver,validate

CLSS public org.jdom2.input.sax.XMLReaderSchemaFactory
cons public init(java.lang.String,java.lang.ClassLoader,javax.xml.validation.Schema)
cons public init(javax.xml.validation.Schema)
supr org.jdom2.input.sax.AbstractReaderSchemaFactory

CLSS public org.jdom2.input.sax.XMLReaderXSDFactory
cons public !varargs init(java.io.File[]) throws org.jdom2.JDOMException
cons public !varargs init(java.lang.String,java.lang.ClassLoader,java.io.File[]) throws org.jdom2.JDOMException
cons public !varargs init(java.lang.String,java.lang.ClassLoader,java.lang.String[]) throws org.jdom2.JDOMException
cons public !varargs init(java.lang.String,java.lang.ClassLoader,java.net.URL[]) throws org.jdom2.JDOMException
cons public !varargs init(java.lang.String,java.lang.ClassLoader,javax.xml.transform.Source[]) throws org.jdom2.JDOMException
cons public !varargs init(java.lang.String[]) throws org.jdom2.JDOMException
cons public !varargs init(java.net.URL[]) throws org.jdom2.JDOMException
cons public !varargs init(javax.xml.transform.Source[]) throws org.jdom2.JDOMException
supr org.jdom2.input.sax.AbstractReaderXSDFactory
hfds xsdschemas

CLSS public final !enum org.jdom2.input.sax.XMLReaders
fld public final static org.jdom2.input.sax.XMLReaders DTDVALIDATING
fld public final static org.jdom2.input.sax.XMLReaders NONVALIDATING
fld public final static org.jdom2.input.sax.XMLReaders XSDVALIDATING
intf org.jdom2.input.sax.XMLReaderJDOMFactory
meth public boolean isValidating()
meth public org.xml.sax.XMLReader createXMLReader() throws org.jdom2.JDOMException
meth public static org.jdom2.input.sax.XMLReaders valueOf(java.lang.String)
meth public static org.jdom2.input.sax.XMLReaders[] values()
supr java.lang.Enum<org.jdom2.input.sax.XMLReaders>
hfds singletonID
hcls DTDSingleton,FactorySupplier,NONSingleton,XSDSingleton

CLSS abstract interface org.jdom2.input.sax.package-info

CLSS public org.jdom2.output.DOMOutputter
cons public init()
cons public init(java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(org.jdom2.adapters.DOMAdapter)
cons public init(org.jdom2.adapters.DOMAdapter,org.jdom2.output.Format,org.jdom2.output.support.DOMOutputProcessor)
cons public init(org.jdom2.output.support.DOMOutputProcessor)
meth public boolean getForceNamespaceAware()
 anno 0 java.lang.Deprecated()
meth public java.util.List<org.w3c.dom.Node> output(java.util.List<? extends org.jdom2.Content>) throws org.jdom2.JDOMException
meth public java.util.List<org.w3c.dom.Node> output(org.w3c.dom.Document,java.util.List<? extends org.jdom2.Content>) throws org.jdom2.JDOMException
meth public org.jdom2.adapters.DOMAdapter getDOMAdapter()
meth public org.jdom2.output.Format getFormat()
meth public org.jdom2.output.support.DOMOutputProcessor getDOMOutputProcessor()
meth public org.w3c.dom.Attr output(org.jdom2.Attribute) throws org.jdom2.JDOMException
meth public org.w3c.dom.Attr output(org.w3c.dom.Document,org.jdom2.Attribute) throws org.jdom2.JDOMException
meth public org.w3c.dom.CDATASection output(org.jdom2.CDATA) throws org.jdom2.JDOMException
meth public org.w3c.dom.CDATASection output(org.w3c.dom.Document,org.jdom2.CDATA) throws org.jdom2.JDOMException
meth public org.w3c.dom.Comment output(org.jdom2.Comment) throws org.jdom2.JDOMException
meth public org.w3c.dom.Comment output(org.w3c.dom.Document,org.jdom2.Comment) throws org.jdom2.JDOMException
meth public org.w3c.dom.Document output(org.jdom2.Document) throws org.jdom2.JDOMException
meth public org.w3c.dom.DocumentType output(org.jdom2.DocType) throws org.jdom2.JDOMException
meth public org.w3c.dom.Element output(org.jdom2.Element) throws org.jdom2.JDOMException
meth public org.w3c.dom.Element output(org.w3c.dom.Document,org.jdom2.Element) throws org.jdom2.JDOMException
meth public org.w3c.dom.EntityReference output(org.jdom2.EntityRef) throws org.jdom2.JDOMException
meth public org.w3c.dom.EntityReference output(org.w3c.dom.Document,org.jdom2.EntityRef) throws org.jdom2.JDOMException
meth public org.w3c.dom.ProcessingInstruction output(org.jdom2.ProcessingInstruction) throws org.jdom2.JDOMException
meth public org.w3c.dom.ProcessingInstruction output(org.w3c.dom.Document,org.jdom2.ProcessingInstruction) throws org.jdom2.JDOMException
meth public org.w3c.dom.Text output(org.jdom2.Text) throws org.jdom2.JDOMException
meth public org.w3c.dom.Text output(org.w3c.dom.Document,org.jdom2.Text) throws org.jdom2.JDOMException
meth public void setDOMAdapter(org.jdom2.adapters.DOMAdapter)
meth public void setDOMOutputProcessor(org.jdom2.output.support.DOMOutputProcessor)
meth public void setForceNamespaceAware(boolean)
 anno 0 java.lang.Deprecated()
meth public void setFormat(org.jdom2.output.Format)
supr java.lang.Object
hfds DEFAULT_ADAPTER,DEFAULT_PROCESSOR,adapter,format,processor
hcls DefaultDOMOutputProcessor

CLSS public abstract interface org.jdom2.output.EscapeStrategy
meth public abstract boolean shouldEscape(char)

CLSS public org.jdom2.output.Format
innr public final static !enum TextMode
intf java.lang.Cloneable
meth public boolean getExpandEmptyElements()
meth public boolean getIgnoreTrAXEscapingPIs()
meth public boolean getOmitDeclaration()
meth public boolean getOmitEncoding()
meth public boolean isSpecifiedAttributesOnly()
meth public final static java.lang.String compact(java.lang.String)
meth public final static java.lang.String escapeAttribute(org.jdom2.output.EscapeStrategy,java.lang.String)
meth public final static java.lang.String escapeText(org.jdom2.output.EscapeStrategy,java.lang.String,java.lang.String)
meth public final static java.lang.String trimBoth(java.lang.String)
meth public final static java.lang.String trimLeft(java.lang.String)
meth public final static java.lang.String trimRight(java.lang.String)
meth public java.lang.String getEncoding()
meth public java.lang.String getIndent()
meth public java.lang.String getLineSeparator()
meth public org.jdom2.output.EscapeStrategy getEscapeStrategy()
meth public org.jdom2.output.Format clone()
meth public org.jdom2.output.Format setEncoding(java.lang.String)
meth public org.jdom2.output.Format setEscapeStrategy(org.jdom2.output.EscapeStrategy)
meth public org.jdom2.output.Format setExpandEmptyElements(boolean)
meth public org.jdom2.output.Format setIndent(java.lang.String)
meth public org.jdom2.output.Format setLineSeparator(java.lang.String)
meth public org.jdom2.output.Format setLineSeparator(org.jdom2.output.LineSeparator)
meth public org.jdom2.output.Format setOmitDeclaration(boolean)
meth public org.jdom2.output.Format setOmitEncoding(boolean)
meth public org.jdom2.output.Format setTextMode(org.jdom2.output.Format$TextMode)
meth public org.jdom2.output.Format$TextMode getTextMode()
meth public static org.jdom2.output.Format getCompactFormat()
meth public static org.jdom2.output.Format getPrettyFormat()
meth public static org.jdom2.output.Format getRawFormat()
meth public void setIgnoreTrAXEscapingPIs(boolean)
meth public void setSpecifiedAttributesOnly(boolean)
supr java.lang.Object
hfds Bits7EscapeStrategy,Bits8EscapeStrategy,DefaultEscapeStrategy,STANDARD_ENCODING,STANDARD_INDENT,STANDARD_LINE_SEPARATOR,UTFEscapeStrategy,encoding,escapeStrategy,expandEmptyElements,ignoreTrAXEscapingPIs,indent,lineSeparator,mode,omitDeclaration,omitEncoding,specifiedAttributesOnly
hcls DefaultCharsetEscapeStrategy,EscapeStrategy7Bits,EscapeStrategy8Bits,EscapeStrategyUTF

CLSS public final static !enum org.jdom2.output.Format$TextMode
 outer org.jdom2.output.Format
fld public final static org.jdom2.output.Format$TextMode NORMALIZE
fld public final static org.jdom2.output.Format$TextMode PRESERVE
fld public final static org.jdom2.output.Format$TextMode TRIM
fld public final static org.jdom2.output.Format$TextMode TRIM_FULL_WHITE
meth public static org.jdom2.output.Format$TextMode valueOf(java.lang.String)
meth public static org.jdom2.output.Format$TextMode[] values()
supr java.lang.Enum<org.jdom2.output.Format$TextMode>

CLSS public abstract interface org.jdom2.output.JDOMLocator
intf org.xml.sax.Locator
meth public abstract java.lang.Object getNode()

CLSS public final !enum org.jdom2.output.LineSeparator
fld public final static org.jdom2.output.LineSeparator CR
fld public final static org.jdom2.output.LineSeparator CRNL
fld public final static org.jdom2.output.LineSeparator DEFAULT
fld public final static org.jdom2.output.LineSeparator DOS
fld public final static org.jdom2.output.LineSeparator NL
fld public final static org.jdom2.output.LineSeparator NONE
fld public final static org.jdom2.output.LineSeparator SYSTEM
fld public final static org.jdom2.output.LineSeparator UNIX
meth public java.lang.String value()
meth public static org.jdom2.output.LineSeparator valueOf(java.lang.String)
meth public static org.jdom2.output.LineSeparator[] values()
supr java.lang.Enum<org.jdom2.output.LineSeparator>
hfds value

CLSS public org.jdom2.output.SAXOutputter
cons public init()
cons public init(org.jdom2.output.support.SAXOutputProcessor,org.jdom2.output.Format,org.xml.sax.ContentHandler,org.xml.sax.ErrorHandler,org.xml.sax.DTDHandler,org.xml.sax.EntityResolver,org.xml.sax.ext.LexicalHandler)
cons public init(org.xml.sax.ContentHandler)
cons public init(org.xml.sax.ContentHandler,org.xml.sax.ErrorHandler,org.xml.sax.DTDHandler,org.xml.sax.EntityResolver)
cons public init(org.xml.sax.ContentHandler,org.xml.sax.ErrorHandler,org.xml.sax.DTDHandler,org.xml.sax.EntityResolver,org.xml.sax.ext.LexicalHandler)
meth public boolean getFeature(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public boolean getReportDTDEvents()
meth public boolean getReportNamespaceDeclarations()
meth public java.lang.Object getProperty(java.lang.String) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public org.jdom2.output.Format getFormat()
meth public org.jdom2.output.JDOMLocator getLocator()
 anno 0 java.lang.Deprecated()
meth public org.jdom2.output.support.SAXOutputProcessor getSAXOutputProcessor()
meth public org.xml.sax.ContentHandler getContentHandler()
meth public org.xml.sax.DTDHandler getDTDHandler()
meth public org.xml.sax.EntityResolver getEntityResolver()
meth public org.xml.sax.ErrorHandler getErrorHandler()
meth public org.xml.sax.ext.DeclHandler getDeclHandler()
meth public org.xml.sax.ext.LexicalHandler getLexicalHandler()
meth public void output(java.util.List<? extends org.jdom2.Content>) throws org.jdom2.JDOMException
meth public void output(org.jdom2.Document) throws org.jdom2.JDOMException
meth public void output(org.jdom2.Element) throws org.jdom2.JDOMException
meth public void outputFragment(java.util.List<? extends org.jdom2.Content>) throws org.jdom2.JDOMException
meth public void outputFragment(org.jdom2.Content) throws org.jdom2.JDOMException
meth public void setContentHandler(org.xml.sax.ContentHandler)
meth public void setDTDHandler(org.xml.sax.DTDHandler)
meth public void setDeclHandler(org.xml.sax.ext.DeclHandler)
meth public void setEntityResolver(org.xml.sax.EntityResolver)
meth public void setErrorHandler(org.xml.sax.ErrorHandler)
meth public void setFeature(java.lang.String,boolean) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public void setFormat(org.jdom2.output.Format)
meth public void setLexicalHandler(org.xml.sax.ext.LexicalHandler)
meth public void setProperty(java.lang.String,java.lang.Object) throws org.xml.sax.SAXNotRecognizedException,org.xml.sax.SAXNotSupportedException
meth public void setReportDTDEvents(boolean)
meth public void setReportNamespaceDeclarations(boolean)
meth public void setSAXOutputProcessor(org.jdom2.output.support.SAXOutputProcessor)
supr java.lang.Object
hfds DEFAULT_PROCESSOR,contentHandler,declHandler,declareNamespaces,dtdHandler,entityResolver,errorHandler,format,lexicalHandler,processor,reportDtdEvents
hcls DefaultSAXOutputProcessor

CLSS public final org.jdom2.output.StAXEventOutputter
cons public init()
cons public init(javax.xml.stream.XMLEventFactory)
cons public init(org.jdom2.output.Format)
cons public init(org.jdom2.output.Format,org.jdom2.output.support.StAXEventProcessor,javax.xml.stream.XMLEventFactory)
cons public init(org.jdom2.output.support.StAXEventProcessor)
intf java.lang.Cloneable
meth public final void output(java.util.List<? extends org.jdom2.Content>,javax.xml.stream.util.XMLEventConsumer) throws javax.xml.stream.XMLStreamException
meth public final void output(org.jdom2.CDATA,javax.xml.stream.util.XMLEventConsumer) throws javax.xml.stream.XMLStreamException
meth public final void output(org.jdom2.Comment,javax.xml.stream.util.XMLEventConsumer) throws javax.xml.stream.XMLStreamException
meth public final void output(org.jdom2.DocType,javax.xml.stream.util.XMLEventConsumer) throws javax.xml.stream.XMLStreamException
meth public final void output(org.jdom2.Document,javax.xml.stream.util.XMLEventConsumer) throws javax.xml.stream.XMLStreamException
meth public final void output(org.jdom2.Element,javax.xml.stream.util.XMLEventConsumer) throws javax.xml.stream.XMLStreamException
meth public final void output(org.jdom2.EntityRef,javax.xml.stream.util.XMLEventConsumer) throws javax.xml.stream.XMLStreamException
meth public final void output(org.jdom2.ProcessingInstruction,javax.xml.stream.util.XMLEventConsumer) throws javax.xml.stream.XMLStreamException
meth public final void output(org.jdom2.Text,javax.xml.stream.util.XMLEventConsumer) throws javax.xml.stream.XMLStreamException
meth public final void outputElementContent(org.jdom2.Element,javax.xml.stream.util.XMLEventConsumer) throws javax.xml.stream.XMLStreamException
meth public java.lang.String toString()
meth public javax.xml.stream.XMLEventFactory getEventFactory()
meth public org.jdom2.output.Format getFormat()
meth public org.jdom2.output.StAXEventOutputter clone()
meth public org.jdom2.output.support.StAXEventProcessor getStAXStream()
meth public void setEventFactory(javax.xml.stream.XMLEventFactory)
meth public void setFormat(org.jdom2.output.Format)
meth public void setStAXEventProcessor(org.jdom2.output.support.StAXEventProcessor)
supr java.lang.Object
hfds DEFAULTEVENTFACTORY,DEFAULTPROCESSOR,myEventFactory,myFormat,myProcessor
hcls DefaultStAXEventProcessor

CLSS public final org.jdom2.output.StAXStreamOutputter
cons public init()
cons public init(org.jdom2.output.Format)
cons public init(org.jdom2.output.Format,org.jdom2.output.support.StAXStreamProcessor)
cons public init(org.jdom2.output.support.StAXStreamProcessor)
intf java.lang.Cloneable
meth public final void output(java.util.List<? extends org.jdom2.Content>,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public final void output(org.jdom2.CDATA,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public final void output(org.jdom2.Comment,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public final void output(org.jdom2.DocType,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public final void output(org.jdom2.Document,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public final void output(org.jdom2.Element,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public final void output(org.jdom2.EntityRef,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public final void output(org.jdom2.ProcessingInstruction,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public final void output(org.jdom2.Text,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public final void outputElementContent(org.jdom2.Element,javax.xml.stream.XMLStreamWriter) throws javax.xml.stream.XMLStreamException
meth public java.lang.String toString()
meth public org.jdom2.output.Format getFormat()
meth public org.jdom2.output.StAXStreamOutputter clone()
meth public org.jdom2.output.support.StAXStreamProcessor getStAXStream()
meth public void setFormat(org.jdom2.output.Format)
meth public void setStAXStreamProcessor(org.jdom2.output.support.StAXStreamProcessor)
supr java.lang.Object
hfds DEFAULTPROCESSOR,myFormat,myProcessor
hcls DefaultStAXStreamProcessor

CLSS public final org.jdom2.output.StAXStreamReader
cons public init()
cons public init(org.jdom2.output.Format)
cons public init(org.jdom2.output.Format,org.jdom2.output.support.StAXStreamReaderProcessor)
cons public init(org.jdom2.output.StAXStreamReader)
cons public init(org.jdom2.output.support.StAXStreamReaderProcessor)
intf java.lang.Cloneable
meth public final javax.xml.stream.XMLStreamReader output(org.jdom2.Document)
meth public java.lang.String toString()
meth public org.jdom2.output.Format getFormat()
meth public org.jdom2.output.StAXStreamReader clone()
meth public org.jdom2.output.support.StAXStreamReaderProcessor getStAXAsStreamProcessor()
meth public void setFormat(org.jdom2.output.Format)
meth public void setStAXAsStreamProcessor(org.jdom2.output.support.StAXStreamReaderProcessor)
supr java.lang.Object
hfds DEFAULTPROCESSOR,myFormat,myProcessor
hcls DefaultStAXAsStreamProcessor

CLSS public final org.jdom2.output.XMLOutputter
cons public init()
cons public init(org.jdom2.output.Format)
cons public init(org.jdom2.output.Format,org.jdom2.output.support.XMLOutputProcessor)
cons public init(org.jdom2.output.XMLOutputter)
cons public init(org.jdom2.output.support.XMLOutputProcessor)
intf java.lang.Cloneable
meth public final java.lang.String outputElementContentString(org.jdom2.Element)
meth public final java.lang.String outputString(java.util.List<? extends org.jdom2.Content>)
meth public final java.lang.String outputString(org.jdom2.CDATA)
meth public final java.lang.String outputString(org.jdom2.Comment)
meth public final java.lang.String outputString(org.jdom2.DocType)
meth public final java.lang.String outputString(org.jdom2.Document)
meth public final java.lang.String outputString(org.jdom2.Element)
meth public final java.lang.String outputString(org.jdom2.EntityRef)
meth public final java.lang.String outputString(org.jdom2.ProcessingInstruction)
meth public final java.lang.String outputString(org.jdom2.Text)
meth public final void output(java.util.List<? extends org.jdom2.Content>,java.io.OutputStream) throws java.io.IOException
meth public final void output(java.util.List<? extends org.jdom2.Content>,java.io.Writer) throws java.io.IOException
meth public final void output(org.jdom2.CDATA,java.io.OutputStream) throws java.io.IOException
meth public final void output(org.jdom2.CDATA,java.io.Writer) throws java.io.IOException
meth public final void output(org.jdom2.Comment,java.io.OutputStream) throws java.io.IOException
meth public final void output(org.jdom2.Comment,java.io.Writer) throws java.io.IOException
meth public final void output(org.jdom2.DocType,java.io.OutputStream) throws java.io.IOException
meth public final void output(org.jdom2.DocType,java.io.Writer) throws java.io.IOException
meth public final void output(org.jdom2.Document,java.io.OutputStream) throws java.io.IOException
meth public final void output(org.jdom2.Document,java.io.Writer) throws java.io.IOException
meth public final void output(org.jdom2.Element,java.io.OutputStream) throws java.io.IOException
meth public final void output(org.jdom2.Element,java.io.Writer) throws java.io.IOException
meth public final void output(org.jdom2.EntityRef,java.io.Writer) throws java.io.IOException
meth public final void output(org.jdom2.ProcessingInstruction,java.io.OutputStream) throws java.io.IOException
meth public final void output(org.jdom2.ProcessingInstruction,java.io.Writer) throws java.io.IOException
meth public final void output(org.jdom2.Text,java.io.OutputStream) throws java.io.IOException
meth public final void output(org.jdom2.Text,java.io.Writer) throws java.io.IOException
meth public final void outputElementContent(org.jdom2.Element,java.io.OutputStream) throws java.io.IOException
meth public final void outputElementContent(org.jdom2.Element,java.io.Writer) throws java.io.IOException
meth public java.lang.String escapeAttributeEntities(java.lang.String)
meth public java.lang.String escapeElementEntities(java.lang.String)
meth public java.lang.String toString()
meth public org.jdom2.output.Format getFormat()
meth public org.jdom2.output.XMLOutputter clone()
meth public org.jdom2.output.support.XMLOutputProcessor getXMLOutputProcessor()
meth public void output(org.jdom2.EntityRef,java.io.OutputStream) throws java.io.IOException
meth public void setFormat(org.jdom2.output.Format)
meth public void setXMLOutputProcessor(org.jdom2.output.support.XMLOutputProcessor)
supr java.lang.Object
hfds DEFAULTPROCESSOR,myFormat,myProcessor
hcls DefaultXMLProcessor

CLSS public abstract interface org.jdom2.util.IteratorIterable<%0 extends java.lang.Object>
intf java.lang.Iterable<{org.jdom2.util.IteratorIterable%0}>
intf java.util.Iterator<{org.jdom2.util.IteratorIterable%0}>

CLSS public final org.jdom2.util.JDOMNamespaceContext
cons public init(org.jdom2.Namespace[])
intf javax.xml.namespace.NamespaceContext
meth public java.lang.String getNamespaceURI(java.lang.String)
meth public java.lang.String getPrefix(java.lang.String)
meth public java.util.Iterator getPrefixes(java.lang.String)
supr java.lang.Object
hfds namespacearray

CLSS public final org.jdom2.util.NamespaceStack
cons public init()
cons public init(org.jdom2.Namespace[])
intf java.lang.Iterable<org.jdom2.Namespace>
meth public !varargs void push(org.jdom2.Namespace[])
meth public boolean isInScope(org.jdom2.Namespace)
meth public java.lang.Iterable<org.jdom2.Namespace> addedForward()
meth public java.lang.Iterable<org.jdom2.Namespace> addedReverse()
meth public java.util.Iterator<org.jdom2.Namespace> iterator()
meth public org.jdom2.Namespace getFirstNamespaceForURI(java.lang.String)
meth public org.jdom2.Namespace getNamespaceForPrefix(java.lang.String)
meth public org.jdom2.Namespace getRebound(java.lang.String)
meth public org.jdom2.Namespace[] getAllNamespacesForURI(java.lang.String)
meth public org.jdom2.Namespace[] getScope()
meth public void pop()
meth public void push(java.lang.Iterable<org.jdom2.Namespace>)
meth public void push(org.jdom2.Attribute)
meth public void push(org.jdom2.Element)
supr java.lang.Object
hfds DEFAULTSEED,EMPTY,EMPTYITER,EMPTYLIST,NSCOMP,added,depth,scope
hcls BackwardWalker,EmptyIterable,ForwardWalker,NamespaceIterable

CLSS public abstract interface org.netbeans.modules.maven.embedder.ArtifactFixer
meth public abstract java.io.File resolve(org.eclipse.aether.artifact.Artifact)

CLSS public org.netbeans.modules.maven.embedder.DependencyTreeFactory
cons public init()
meth public static org.apache.maven.shared.dependency.tree.DependencyNode createDependencyTree(org.apache.maven.project.MavenProject,org.netbeans.modules.maven.embedder.MavenEmbedder,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static org.apache.maven.shared.dependency.tree.DependencyNode createDependencyTree(org.apache.maven.project.MavenProject,org.netbeans.modules.maven.embedder.MavenEmbedder,java.util.Collection<java.lang.String>) throws org.apache.maven.MavenExecutionException
supr java.lang.Object
hfds LOG

CLSS public final org.netbeans.modules.maven.embedder.EmbedderFactory
fld public final static java.lang.String PROP_COMMANDLINE_PATH = "commandLineMavenPath"
meth public static boolean isOfflineException(java.lang.Throwable)
meth public static boolean isProjectEmbedderLoaded()
meth public static java.io.File getDefaultMavenHome()
meth public static java.io.File getEffectiveMavenHome()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.io.File getEffectiveMavenHome(org.netbeans.api.project.ui.ProjectGroup)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.io.File getMavenHome()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static java.util.List<org.apache.maven.model.Model> createModelLineage(java.io.File,org.netbeans.modules.maven.embedder.MavenEmbedder) throws org.apache.maven.model.building.ModelBuildingException
 anno 0 java.lang.Deprecated()
meth public static java.util.Properties fillEnvVars(java.util.Properties)
meth public static org.apache.maven.artifact.repository.ArtifactRepository createRemoteRepository(org.netbeans.modules.maven.embedder.MavenEmbedder,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.modules.maven.embedder.MavenEmbedder createProjectLikeEmbedder() throws org.codehaus.plexus.PlexusContainerException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.maven.embedder.MavenEmbedder getOnlineEmbedder()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.maven.embedder.MavenEmbedder getProjectEmbedder()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static void resetCachedEmbedders()
meth public static void setGroupedMavenHome(org.netbeans.api.project.ui.ProjectGroup,java.io.File)
meth public static void setMavenHome(java.io.File)
supr java.lang.Object
hfds LOG,ONLINE_LOCK,PROJECT_LOCK,PROP_DEFAULT_OPTIONS,RP,forbidden,online,project,projectLoaded,statics,warmupTask
hcls NbLoggerManager

CLSS public final org.netbeans.modules.maven.embedder.MavenEmbedder
innr public abstract interface static ModelDescription
meth public <%0 extends java.lang.Object> {%%0} lookupComponent(java.lang.Class<{%%0}>)
meth public java.io.File getLocalRepositoryFile()
meth public java.util.List<java.lang.String> getLifecyclePhases()
meth public java.util.List<org.apache.maven.execution.MavenExecutionResult> readProjectsWithDependencies(org.apache.maven.execution.MavenExecutionRequest,java.util.List<java.io.File>,boolean)
meth public java.util.List<org.apache.maven.model.Model> createModelLineage(java.io.File) throws org.apache.maven.model.building.ModelBuildingException
meth public java.util.Properties getSystemProperties()
meth public org.apache.maven.artifact.Artifact createArtifact(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public org.apache.maven.artifact.Artifact createArtifact(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public org.apache.maven.artifact.Artifact createArtifactWithClassifier(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public org.apache.maven.artifact.Artifact createProjectArtifact(java.lang.String,java.lang.String,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public org.apache.maven.artifact.repository.ArtifactRepository createRemoteRepository(java.lang.String,java.lang.String)
meth public org.apache.maven.artifact.repository.ArtifactRepository getLocalRepository()
meth public org.apache.maven.execution.MavenExecutionRequest createMavenExecutionRequest()
meth public org.apache.maven.execution.MavenExecutionResult execute(org.apache.maven.execution.MavenExecutionRequest)
meth public org.apache.maven.execution.MavenExecutionResult readProjectWithDependencies(org.apache.maven.execution.MavenExecutionRequest)
 anno 0 java.lang.Deprecated()
meth public org.apache.maven.execution.MavenExecutionResult readProjectWithDependencies(org.apache.maven.execution.MavenExecutionRequest,boolean)
meth public org.apache.maven.model.building.ModelBuildingResult executeModelBuilder(java.io.File) throws org.apache.maven.model.building.ModelBuildingException
meth public org.apache.maven.project.ProjectBuildingResult buildProject(org.apache.maven.artifact.Artifact,org.apache.maven.project.ProjectBuildingRequest) throws org.apache.maven.project.ProjectBuildingException
meth public org.apache.maven.settings.Settings getSettings()
meth public org.codehaus.plexus.PlexusContainer getPlexus()
meth public static java.util.List<org.netbeans.modules.maven.embedder.MavenEmbedder$ModelDescription> getModelDescriptors(org.apache.maven.project.MavenProject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static java.util.Set<java.lang.String> getAllProjectProfiles(org.apache.maven.project.MavenProject)
meth public static void normalizePaths(org.apache.maven.project.MavenProject)
meth public void resolve(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
 anno 0 java.lang.Deprecated()
meth public void resolveArtifact(org.apache.maven.artifact.Artifact,java.util.List<org.apache.maven.artifact.repository.ArtifactRepository>,org.apache.maven.artifact.repository.ArtifactRepository) throws org.apache.maven.artifact.resolver.ArtifactNotFoundException,org.apache.maven.artifact.resolver.ArtifactResolutionException
meth public void setUpLegacySupport()
supr java.lang.Object
hfds LOG,embedderConfiguration,lastLocalRepository,lastLocalRepositoryLock,maven,plexus,populator,projectBuilder,repositorySystem,settings,settingsBuilder,settingsDecrypter,settingsTimestamp,testSettings,thisRepositorySession

CLSS public abstract interface static org.netbeans.modules.maven.embedder.MavenEmbedder$ModelDescription
 outer org.netbeans.modules.maven.embedder.MavenEmbedder
meth public abstract java.io.File getLocation()
meth public abstract java.lang.String getArtifactId()
meth public abstract java.lang.String getGroupId()
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getVersion()
meth public abstract java.util.List<java.lang.String> getModules()
meth public abstract java.util.List<java.lang.String> getProfiles()

CLSS public org.netbeans.modules.maven.embedder.NBPluginParameterExpressionEvaluator
 anno 0 java.lang.Deprecated()
cons public init(org.apache.maven.project.MavenProject,org.apache.maven.settings.Settings,java.util.Map<? extends java.lang.String,? extends java.lang.String>)
 anno 0 java.lang.Deprecated()
cons public init(org.apache.maven.project.MavenProject,org.apache.maven.settings.Settings,java.util.Map<? extends java.lang.String,? extends java.lang.String>,java.util.Map<? extends java.lang.String,? extends java.lang.String>)
intf org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator
meth public java.io.File alignToBaseDirectory(java.io.File)
meth public java.lang.Object evaluate(java.lang.String) throws org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException
supr java.lang.Object
hfds basedir,pathTranslator,project,settings,systemProperties,userProperties

CLSS public org.netbeans.modules.maven.embedder.exec.ProgressTransferListener
fld public java.util.concurrent.atomic.AtomicBoolean cancel
intf org.eclipse.aether.transfer.TransferListener
meth public static org.netbeans.modules.maven.embedder.exec.ProgressTransferListener activeListener()
meth public static org.openide.util.Cancellable cancellable()
meth public static void clearAggregateHandle()
meth public static void setAggregateHandle(org.netbeans.api.progress.aggregate.AggregateProgressHandle)
meth public void transferCorrupted(org.eclipse.aether.transfer.TransferEvent) throws org.eclipse.aether.transfer.TransferCancelledException
meth public void transferFailed(org.eclipse.aether.transfer.TransferEvent)
meth public void transferInitiated(org.eclipse.aether.transfer.TransferEvent) throws org.eclipse.aether.transfer.TransferCancelledException
meth public void transferProgressed(org.eclipse.aether.transfer.TransferEvent) throws org.eclipse.aether.transfer.TransferCancelledException
meth public void transferStarted(org.eclipse.aether.transfer.TransferEvent) throws org.eclipse.aether.transfer.TransferCancelledException
meth public void transferSucceeded(org.eclipse.aether.transfer.TransferEvent)
supr java.lang.Object
hfds POM_MAX,activeListener,contrib,contribStack,handle,length,pomCount,pomcontrib

CLSS public abstract interface org.slf4j.ILoggerFactory
meth public abstract org.slf4j.Logger getLogger(java.lang.String)

CLSS public abstract interface org.slf4j.IMarkerFactory
meth public abstract boolean detachMarker(java.lang.String)
meth public abstract boolean exists(java.lang.String)
meth public abstract org.slf4j.Marker getDetachedMarker(java.lang.String)
meth public abstract org.slf4j.Marker getMarker(java.lang.String)

CLSS public abstract interface org.slf4j.Logger
fld public final static java.lang.String ROOT_LOGGER_NAME = "ROOT"
meth public abstract !varargs void debug(java.lang.String,java.lang.Object[])
meth public abstract !varargs void debug(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public abstract !varargs void error(java.lang.String,java.lang.Object[])
meth public abstract !varargs void error(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public abstract !varargs void info(java.lang.String,java.lang.Object[])
meth public abstract !varargs void info(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public abstract !varargs void trace(java.lang.String,java.lang.Object[])
meth public abstract !varargs void trace(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public abstract !varargs void warn(java.lang.String,java.lang.Object[])
meth public abstract !varargs void warn(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public abstract boolean isDebugEnabled()
meth public abstract boolean isDebugEnabled(org.slf4j.Marker)
meth public abstract boolean isErrorEnabled()
meth public abstract boolean isErrorEnabled(org.slf4j.Marker)
meth public abstract boolean isInfoEnabled()
meth public abstract boolean isInfoEnabled(org.slf4j.Marker)
meth public abstract boolean isTraceEnabled()
meth public abstract boolean isTraceEnabled(org.slf4j.Marker)
meth public abstract boolean isWarnEnabled()
meth public abstract boolean isWarnEnabled(org.slf4j.Marker)
meth public abstract java.lang.String getName()
meth public abstract void debug(java.lang.String)
meth public abstract void debug(java.lang.String,java.lang.Object)
meth public abstract void debug(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void debug(java.lang.String,java.lang.Throwable)
meth public abstract void debug(org.slf4j.Marker,java.lang.String)
meth public abstract void debug(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public abstract void debug(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void debug(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
meth public abstract void error(java.lang.String)
meth public abstract void error(java.lang.String,java.lang.Object)
meth public abstract void error(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void error(java.lang.String,java.lang.Throwable)
meth public abstract void error(org.slf4j.Marker,java.lang.String)
meth public abstract void error(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public abstract void error(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void error(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
meth public abstract void info(java.lang.String)
meth public abstract void info(java.lang.String,java.lang.Object)
meth public abstract void info(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void info(java.lang.String,java.lang.Throwable)
meth public abstract void info(org.slf4j.Marker,java.lang.String)
meth public abstract void info(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public abstract void info(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void info(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
meth public abstract void trace(java.lang.String)
meth public abstract void trace(java.lang.String,java.lang.Object)
meth public abstract void trace(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void trace(java.lang.String,java.lang.Throwable)
meth public abstract void trace(org.slf4j.Marker,java.lang.String)
meth public abstract void trace(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public abstract void trace(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void trace(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
meth public abstract void warn(java.lang.String)
meth public abstract void warn(java.lang.String,java.lang.Object)
meth public abstract void warn(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void warn(java.lang.String,java.lang.Throwable)
meth public abstract void warn(org.slf4j.Marker,java.lang.String)
meth public abstract void warn(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public abstract void warn(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void warn(org.slf4j.Marker,java.lang.String,java.lang.Throwable)

CLSS public final org.slf4j.LoggerFactory
meth public static org.slf4j.ILoggerFactory getILoggerFactory()
meth public static org.slf4j.Logger getLogger(java.lang.Class<?>)
meth public static org.slf4j.Logger getLogger(java.lang.String)
supr java.lang.Object
hfds API_COMPATIBILITY_LIST,CODES_PREFIX,DETECT_LOGGER_NAME_MISMATCH,DETECT_LOGGER_NAME_MISMATCH_PROPERTY,FAILED_INITIALIZATION,INITIALIZATION_STATE,JAVA_VENDOR_PROPERTY,LOGGER_NAME_MISMATCH_URL,MULTIPLE_BINDINGS_URL,NOP_FALLBACK_FACTORY,NOP_FALLBACK_INITIALIZATION,NO_STATICLOGGERBINDER_URL,NULL_LF_URL,ONGOING_INITIALIZATION,REPLAY_URL,STATIC_LOGGER_BINDER_PATH,SUBSTITUTE_LOGGER_URL,SUBST_FACTORY,SUCCESSFUL_INITIALIZATION,UNINITIALIZED,UNSUCCESSFUL_INIT_MSG,UNSUCCESSFUL_INIT_URL,VERSION_MISMATCH

CLSS public org.slf4j.MDC
innr public static MDCCloseable
meth public static java.lang.String get(java.lang.String)
meth public static java.util.Map<java.lang.String,java.lang.String> getCopyOfContextMap()
meth public static org.slf4j.MDC$MDCCloseable putCloseable(java.lang.String,java.lang.String)
meth public static org.slf4j.spi.MDCAdapter getMDCAdapter()
meth public static void clear()
meth public static void put(java.lang.String,java.lang.String)
meth public static void remove(java.lang.String)
meth public static void setContextMap(java.util.Map<java.lang.String,java.lang.String>)
supr java.lang.Object
hfds NO_STATIC_MDC_BINDER_URL,NULL_MDCA_URL,mdcAdapter

CLSS public static org.slf4j.MDC$MDCCloseable
 outer org.slf4j.MDC
intf java.io.Closeable
meth public void close()
supr java.lang.Object
hfds key

CLSS public abstract interface org.slf4j.Marker
fld public final static java.lang.String ANY_MARKER = "*"
fld public final static java.lang.String ANY_NON_NULL_MARKER = "+"
intf java.io.Serializable
meth public abstract boolean contains(java.lang.String)
meth public abstract boolean contains(org.slf4j.Marker)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean hasChildren()
meth public abstract boolean hasReferences()
meth public abstract boolean remove(org.slf4j.Marker)
meth public abstract int hashCode()
meth public abstract java.lang.String getName()
meth public abstract java.util.Iterator<org.slf4j.Marker> iterator()
meth public abstract void add(org.slf4j.Marker)

CLSS public org.slf4j.MarkerFactory
meth public static org.slf4j.IMarkerFactory getIMarkerFactory()
meth public static org.slf4j.Marker getDetachedMarker(java.lang.String)
meth public static org.slf4j.Marker getMarker(java.lang.String)
supr java.lang.Object
hfds MARKER_FACTORY

CLSS public org.slf4j.MavenSlf4jFriend
cons public init()
meth public static void reset()
supr java.lang.Object

CLSS public abstract org.slf4j.helpers.MarkerIgnoringBase
cons public init()
fld protected java.lang.String name
intf java.io.Serializable
intf org.slf4j.Logger
meth protected java.lang.Object readResolve() throws java.io.ObjectStreamException
meth public !varargs void debug(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public !varargs void error(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public !varargs void info(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public !varargs void trace(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public !varargs void warn(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public boolean isDebugEnabled(org.slf4j.Marker)
meth public boolean isErrorEnabled(org.slf4j.Marker)
meth public boolean isInfoEnabled(org.slf4j.Marker)
meth public boolean isTraceEnabled(org.slf4j.Marker)
meth public boolean isWarnEnabled(org.slf4j.Marker)
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public void debug(org.slf4j.Marker,java.lang.String)
meth public void debug(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public void debug(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public void debug(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
meth public void error(org.slf4j.Marker,java.lang.String)
meth public void error(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public void error(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public void error(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
meth public void info(org.slf4j.Marker,java.lang.String)
meth public void info(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public void info(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public void info(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
meth public void trace(org.slf4j.Marker,java.lang.String)
meth public void trace(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public void trace(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public void trace(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
meth public void warn(org.slf4j.Marker,java.lang.String)
meth public void warn(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public void warn(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public void warn(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
supr java.lang.Object
hfds serialVersionUID

CLSS public org.slf4j.impl.MavenSimpleLogger
meth protected java.lang.String getLocation(java.lang.StackTraceElement)
meth protected java.lang.String renderLevel(int)
meth protected void writeThrowable(java.lang.Throwable,java.io.PrintStream)
supr org.slf4j.impl.SimpleLogger

CLSS public org.slf4j.impl.MavenSimpleLoggerFactory
cons public init()
meth public org.slf4j.Logger getLogger(java.lang.String)
supr org.slf4j.impl.SimpleLoggerFactory

CLSS public org.slf4j.impl.MavenSlf4jSimpleFriend
cons public init()
meth public static void init()
supr java.lang.Object

CLSS public org.slf4j.impl.SimpleLogger
fld protected final static int LOG_LEVEL_DEBUG = 10
fld protected final static int LOG_LEVEL_ERROR = 40
fld protected final static int LOG_LEVEL_INFO = 20
fld protected final static int LOG_LEVEL_OFF = 50
fld protected final static int LOG_LEVEL_TRACE = 0
fld protected final static int LOG_LEVEL_WARN = 30
fld protected int currentLogLevel
fld public final static java.lang.String CACHE_OUTPUT_STREAM_STRING_KEY = "org.slf4j.simpleLogger.cacheOutputStream"
fld public final static java.lang.String DATE_TIME_FORMAT_KEY = "org.slf4j.simpleLogger.dateTimeFormat"
fld public final static java.lang.String DEFAULT_LOG_LEVEL_KEY = "org.slf4j.simpleLogger.defaultLogLevel"
fld public final static java.lang.String LEVEL_IN_BRACKETS_KEY = "org.slf4j.simpleLogger.levelInBrackets"
fld public final static java.lang.String LOG_FILE_KEY = "org.slf4j.simpleLogger.logFile"
fld public final static java.lang.String LOG_KEY_PREFIX = "org.slf4j.simpleLogger.log."
fld public final static java.lang.String SHOW_DATE_TIME_KEY = "org.slf4j.simpleLogger.showDateTime"
fld public final static java.lang.String SHOW_LOG_NAME_KEY = "org.slf4j.simpleLogger.showLogName"
fld public final static java.lang.String SHOW_SHORT_LOG_NAME_KEY = "org.slf4j.simpleLogger.showShortLogName"
fld public final static java.lang.String SHOW_THREAD_ID_KEY = "org.slf4j.simpleLogger.showThreadId"
fld public final static java.lang.String SHOW_THREAD_NAME_KEY = "org.slf4j.simpleLogger.showThreadName"
fld public final static java.lang.String SYSTEM_PREFIX = "org.slf4j.simpleLogger."
fld public final static java.lang.String WARN_LEVEL_STRING_KEY = "org.slf4j.simpleLogger.warnLevelString"
meth protected boolean isLevelEnabled(int)
meth protected java.lang.String renderLevel(int)
meth protected void writeThrowable(java.lang.Throwable,java.io.PrintStream)
meth public !varargs void debug(java.lang.String,java.lang.Object[])
meth public !varargs void error(java.lang.String,java.lang.Object[])
meth public !varargs void info(java.lang.String,java.lang.Object[])
meth public !varargs void trace(java.lang.String,java.lang.Object[])
meth public !varargs void warn(java.lang.String,java.lang.Object[])
meth public boolean isDebugEnabled()
meth public boolean isErrorEnabled()
meth public boolean isInfoEnabled()
meth public boolean isTraceEnabled()
meth public boolean isWarnEnabled()
meth public void debug(java.lang.String)
meth public void debug(java.lang.String,java.lang.Object)
meth public void debug(java.lang.String,java.lang.Object,java.lang.Object)
meth public void debug(java.lang.String,java.lang.Throwable)
meth public void error(java.lang.String)
meth public void error(java.lang.String,java.lang.Object)
meth public void error(java.lang.String,java.lang.Object,java.lang.Object)
meth public void error(java.lang.String,java.lang.Throwable)
meth public void info(java.lang.String)
meth public void info(java.lang.String,java.lang.Object)
meth public void info(java.lang.String,java.lang.Object,java.lang.Object)
meth public void info(java.lang.String,java.lang.Throwable)
meth public void log(org.slf4j.event.LoggingEvent)
meth public void trace(java.lang.String)
meth public void trace(java.lang.String,java.lang.Object)
meth public void trace(java.lang.String,java.lang.Object,java.lang.Object)
meth public void trace(java.lang.String,java.lang.Throwable)
meth public void warn(java.lang.String)
meth public void warn(java.lang.String,java.lang.Object)
meth public void warn(java.lang.String,java.lang.Object,java.lang.Object)
meth public void warn(java.lang.String,java.lang.Throwable)
supr org.slf4j.helpers.MarkerIgnoringBase
hfds CONFIG_PARAMS,INITIALIZED,START_TIME,TID_PREFIX,serialVersionUID,shortLogName

CLSS public org.slf4j.impl.SimpleLoggerConfiguration
cons public init()
supr java.lang.Object
hfds CACHE_OUTPUT_STREAM_DEFAULT,CONFIGURATION_FILE,DATE_TIME_FORMAT_STR_DEFAULT,DEFAULT_LOG_LEVEL_DEFAULT,LEVEL_IN_BRACKETS_DEFAULT,LOG_FILE_DEFAULT,SHOW_DATE_TIME_DEFAULT,SHOW_LOG_NAME_DEFAULT,SHOW_SHORT_LOG_NAME_DEFAULT,SHOW_THREAD_ID_DEFAULT,SHOW_THREAD_NAME_DEFAULT,WARN_LEVELS_STRING_DEFAULT,cacheOutputStream,dateFormatter,dateTimeFormatStr,defaultLogLevel,levelInBrackets,logFile,outputChoice,properties,showDateTime,showLogName,showShortLogName,showThreadId,showThreadName,warnLevelString

CLSS public org.slf4j.impl.SimpleLoggerFactory
cons public init()
intf org.slf4j.ILoggerFactory
meth public org.slf4j.Logger getLogger(java.lang.String)
supr java.lang.Object
hfds loggerMap

CLSS public final org.slf4j.impl.StaticLoggerBinder
fld public static java.lang.String REQUESTED_API_VERSION
intf org.slf4j.spi.LoggerFactoryBinder
meth public java.lang.String getLoggerFactoryClassStr()
meth public org.slf4j.ILoggerFactory getLoggerFactory()
meth public static org.slf4j.impl.StaticLoggerBinder getSingleton()
supr java.lang.Object
hfds LOGGER_FACTORY_CLASS_STR,SINGLETON,loggerFactory

CLSS public org.slf4j.impl.StaticMDCBinder
fld public final static org.slf4j.impl.StaticMDCBinder SINGLETON
meth public final static org.slf4j.impl.StaticMDCBinder getSingleton()
meth public java.lang.String getMDCAdapterClassStr()
meth public org.slf4j.spi.MDCAdapter getMDCA()
supr java.lang.Object

CLSS public org.slf4j.impl.StaticMarkerBinder
fld public final static org.slf4j.impl.StaticMarkerBinder SINGLETON
intf org.slf4j.spi.MarkerFactoryBinder
meth public java.lang.String getMarkerFactoryClassStr()
meth public org.slf4j.IMarkerFactory getMarkerFactory()
meth public static org.slf4j.impl.StaticMarkerBinder getSingleton()
supr java.lang.Object
hfds markerFactory

CLSS public abstract interface org.slf4j.spi.LoggerFactoryBinder
meth public abstract java.lang.String getLoggerFactoryClassStr()
meth public abstract org.slf4j.ILoggerFactory getLoggerFactory()

CLSS public abstract interface org.slf4j.spi.MarkerFactoryBinder
meth public abstract java.lang.String getMarkerFactoryClassStr()
meth public abstract org.slf4j.IMarkerFactory getMarkerFactory()

CLSS public org.sonatype.plexus.components.cipher.Base64
cons public init()
meth public byte[] decode(byte[])
meth public byte[] encode(byte[])
meth public java.lang.Object decode(java.lang.Object)
meth public java.lang.Object encode(java.lang.Object)
meth public static boolean isArrayByteBase64(byte[])
meth public static boolean isBase64(byte)
meth public static byte[] decodeBase64(byte[])
meth public static byte[] encodeBase64(byte[])
meth public static byte[] encodeBase64(byte[],boolean)
meth public static byte[] encodeBase64Chunked(byte[])
supr java.lang.Object
hfds BASELENGTH,CHUNK_SEPARATOR,CHUNK_SIZE,EIGHTBIT,FOURBYTE,LOOKUPLENGTH,PAD,SIGN,SIXTEENBIT,TWENTYFOURBITGROUP,base64Alphabet,lookUpBase64Alphabet

CLSS public org.sonatype.plexus.components.cipher.DefaultPlexusCipher
 anno 0 javax.inject.Named(java.lang.String value="default")
 anno 0 javax.inject.Singleton()
 anno 0 org.eclipse.sisu.Typed(java.lang.Class<?>[] value=[class org.sonatype.plexus.components.cipher.PlexusCipher])
cons public init()
intf org.sonatype.plexus.components.cipher.PlexusCipher
meth public boolean isEncryptedString(java.lang.String)
meth public java.lang.String decorate(java.lang.String)
meth public java.lang.String decrypt(java.lang.String,java.lang.String) throws org.sonatype.plexus.components.cipher.PlexusCipherException
meth public java.lang.String decryptDecorated(java.lang.String,java.lang.String) throws org.sonatype.plexus.components.cipher.PlexusCipherException
meth public java.lang.String encrypt(java.lang.String,java.lang.String) throws org.sonatype.plexus.components.cipher.PlexusCipherException
meth public java.lang.String encryptAndDecorate(java.lang.String,java.lang.String) throws org.sonatype.plexus.components.cipher.PlexusCipherException
meth public java.lang.String unDecorate(java.lang.String) throws org.sonatype.plexus.components.cipher.PlexusCipherException
meth public static java.lang.String[] getCryptoImpls(java.lang.String)
meth public static java.lang.String[] getServiceTypes()
meth public static void main(java.lang.String[])
supr java.lang.Object
hfds ENCRYPTED_STRING_PATTERN,_cipher

CLSS public org.sonatype.plexus.components.cipher.PBECipher
cons public init()
fld protected final static byte WIPER = 0
fld protected final static int CHUNK_SIZE = 16
fld protected final static int PBE_ITERATIONS = 1000
fld protected final static int SALT_SIZE = 8
fld protected final static int SPICE_SIZE = 16
fld protected final static java.lang.String CIPHER_ALG = "AES/CBC/PKCS5Padding"
fld protected final static java.lang.String DIGEST_ALG = "SHA-256"
fld protected final static java.lang.String KEY_ALG = "AES"
fld protected final static java.lang.String STRING_ENCODING = "UTF8"
meth public java.lang.String decrypt64(java.lang.String,java.lang.String) throws org.sonatype.plexus.components.cipher.PlexusCipherException
meth public java.lang.String encrypt64(java.lang.String,java.lang.String) throws org.sonatype.plexus.components.cipher.PlexusCipherException
supr java.lang.Object
hfds _secureRandom

CLSS public abstract interface org.sonatype.plexus.components.cipher.PlexusCipher
fld public final static char ENCRYPTED_STRING_DECORATION_START = '{'
fld public final static char ENCRYPTED_STRING_DECORATION_STOP = '}'
meth public abstract boolean isEncryptedString(java.lang.String)
meth public abstract java.lang.String decorate(java.lang.String)
meth public abstract java.lang.String decrypt(java.lang.String,java.lang.String) throws org.sonatype.plexus.components.cipher.PlexusCipherException
meth public abstract java.lang.String decryptDecorated(java.lang.String,java.lang.String) throws org.sonatype.plexus.components.cipher.PlexusCipherException
meth public abstract java.lang.String encrypt(java.lang.String,java.lang.String) throws org.sonatype.plexus.components.cipher.PlexusCipherException
meth public abstract java.lang.String encryptAndDecorate(java.lang.String,java.lang.String) throws org.sonatype.plexus.components.cipher.PlexusCipherException
meth public abstract java.lang.String unDecorate(java.lang.String) throws org.sonatype.plexus.components.cipher.PlexusCipherException

CLSS public org.sonatype.plexus.components.cipher.PlexusCipherException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher
 anno 0 javax.inject.Named(java.lang.String value="")
 anno 0 javax.inject.Singleton()
cons public init(org.sonatype.plexus.components.cipher.PlexusCipher)
cons public init(org.sonatype.plexus.components.cipher.PlexusCipher,java.util.Map<java.lang.String,org.sonatype.plexus.components.sec.dispatcher.PasswordDecryptor>,java.lang.String)
 anno 0 javax.inject.Inject()
 anno 3 javax.inject.Named(java.lang.String value="${_configurationFile:-~/.settings-security.xml}")
fld protected final java.util.Map<java.lang.String,org.sonatype.plexus.components.sec.dispatcher.PasswordDecryptor> _decryptors
fld protected final org.sonatype.plexus.components.cipher.PlexusCipher _cipher
fld protected java.lang.String _configurationFile
fld public final static char ATTR_START = '['
fld public final static char ATTR_STOP = ']'
fld public final static java.lang.String SYSTEM_PROPERTY_SEC_LOCATION = "settings.security"
fld public final static java.lang.String TYPE_ATTR = "type"
intf org.sonatype.plexus.components.sec.dispatcher.SecDispatcher
meth public java.lang.String decrypt(java.lang.String) throws org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException
meth public java.lang.String getConfigurationFile()
meth public static void main(java.lang.String[]) throws java.lang.Exception
meth public void setConfigurationFile(java.lang.String)
supr java.lang.Object
hfds DEFAULT_CONFIGURATION

CLSS public abstract interface org.sonatype.plexus.components.sec.dispatcher.PasswordDecryptor
meth public abstract java.lang.String decrypt(java.lang.String,java.util.Map,java.util.Map) throws org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException

CLSS public abstract interface org.sonatype.plexus.components.sec.dispatcher.SecDispatcher
fld public final static java.lang.String[] SYSTEM_PROPERTY_MASTER_PASSWORD
fld public final static java.lang.String[] SYSTEM_PROPERTY_SERVER_PASSWORD
meth public abstract java.lang.String decrypt(java.lang.String) throws org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException

CLSS public org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public org.sonatype.plexus.components.sec.dispatcher.SecUtil
cons public init()
fld public final static int PROTOCOL_DELIM_LEN
fld public final static java.lang.String PROTOCOL_DELIM = "://"
fld public final static java.lang.String[] URL_PROTOCOLS
meth public static java.util.Map<java.lang.String,java.lang.String> getConfig(org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity,java.lang.String)
meth public static org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity read(java.lang.String,boolean) throws org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException
supr java.lang.Object

CLSS public abstract interface org.xml.sax.ContentHandler
meth public abstract void characters(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void endDocument() throws org.xml.sax.SAXException
meth public abstract void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void setDocumentLocator(org.xml.sax.Locator)
meth public abstract void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void startDocument() throws org.xml.sax.SAXException
meth public abstract void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public abstract void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.DTDHandler
meth public abstract void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.EntityResolver
meth public abstract org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.ErrorHandler
meth public abstract void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public abstract void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.Locator
meth public abstract int getColumnNumber()
meth public abstract int getLineNumber()
meth public abstract java.lang.String getPublicId()
meth public abstract java.lang.String getSystemId()

CLSS public abstract interface org.xml.sax.ext.DeclHandler
meth public abstract void attributeDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void elementDecl(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void externalEntityDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void internalEntityDecl(java.lang.String,java.lang.String) throws org.xml.sax.SAXException

CLSS public abstract interface org.xml.sax.ext.LexicalHandler
meth public abstract void comment(char[],int,int) throws org.xml.sax.SAXException
meth public abstract void endCDATA() throws org.xml.sax.SAXException
meth public abstract void endDTD() throws org.xml.sax.SAXException
meth public abstract void endEntity(java.lang.String) throws org.xml.sax.SAXException
meth public abstract void startCDATA() throws org.xml.sax.SAXException
meth public abstract void startDTD(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public abstract void startEntity(java.lang.String) throws org.xml.sax.SAXException

CLSS public org.xml.sax.helpers.DefaultHandler
cons public init()
intf org.xml.sax.ContentHandler
intf org.xml.sax.DTDHandler
intf org.xml.sax.EntityResolver
intf org.xml.sax.ErrorHandler
meth public org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException
meth public void characters(char[],int,int) throws org.xml.sax.SAXException
meth public void endDocument() throws org.xml.sax.SAXException
meth public void endElement(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void endPrefixMapping(java.lang.String) throws org.xml.sax.SAXException
meth public void error(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void fatalError(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
meth public void ignorableWhitespace(char[],int,int) throws org.xml.sax.SAXException
meth public void notationDecl(java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void processingInstruction(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void setDocumentLocator(org.xml.sax.Locator)
meth public void skippedEntity(java.lang.String) throws org.xml.sax.SAXException
meth public void startDocument() throws org.xml.sax.SAXException
meth public void startElement(java.lang.String,java.lang.String,java.lang.String,org.xml.sax.Attributes) throws org.xml.sax.SAXException
meth public void startPrefixMapping(java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void unparsedEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.xml.sax.SAXException
meth public void warning(org.xml.sax.SAXParseException) throws org.xml.sax.SAXException
supr java.lang.Object

