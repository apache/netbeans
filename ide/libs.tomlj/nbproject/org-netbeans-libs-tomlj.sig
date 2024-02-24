#Signature file v4.1
#Version 1.5

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

CLSS public final !enum org.tomlj.JsonOptions
fld public final static org.tomlj.JsonOptions ALL_VALUES_AS_STRINGS
fld public final static org.tomlj.JsonOptions VALUES_AS_OBJECTS_WITH_TYPE
meth public static org.tomlj.JsonOptions valueOf(java.lang.String)
meth public static org.tomlj.JsonOptions[] values()
supr java.lang.Enum<org.tomlj.JsonOptions>

CLSS public final org.tomlj.Toml
meth public static boolean equals(org.tomlj.TomlArray,org.tomlj.TomlArray)
meth public static boolean equals(org.tomlj.TomlTable,org.tomlj.TomlTable)
meth public static java.lang.String canonicalDottedKey(java.lang.String)
meth public static java.lang.String joinKeyPath(java.util.List<java.lang.String>)
meth public static java.lang.StringBuilder tomlEscape(java.lang.String)
meth public static java.util.List<java.lang.String> parseDottedKey(java.lang.String)
meth public static org.tomlj.TomlParseResult parse(java.io.InputStream) throws java.io.IOException
meth public static org.tomlj.TomlParseResult parse(java.io.InputStream,org.tomlj.TomlVersion) throws java.io.IOException
meth public static org.tomlj.TomlParseResult parse(java.io.Reader) throws java.io.IOException
meth public static org.tomlj.TomlParseResult parse(java.io.Reader,org.tomlj.TomlVersion) throws java.io.IOException
meth public static org.tomlj.TomlParseResult parse(java.lang.String)
meth public static org.tomlj.TomlParseResult parse(java.lang.String,org.tomlj.TomlVersion)
meth public static org.tomlj.TomlParseResult parse(java.nio.channels.ReadableByteChannel) throws java.io.IOException
meth public static org.tomlj.TomlParseResult parse(java.nio.channels.ReadableByteChannel,org.tomlj.TomlVersion) throws java.io.IOException
meth public static org.tomlj.TomlParseResult parse(java.nio.file.Path) throws java.io.IOException
meth public static org.tomlj.TomlParseResult parse(java.nio.file.Path,org.tomlj.TomlVersion) throws java.io.IOException
supr java.lang.Object
hfds simpleKeyPattern

CLSS public abstract interface org.tomlj.TomlArray
meth public !varargs java.lang.String toJson(org.tomlj.JsonOptions[])
meth public !varargs void toJson(java.lang.Appendable,org.tomlj.JsonOptions[]) throws java.io.IOException
meth public abstract boolean containsArrays()
meth public abstract boolean containsBooleans()
meth public abstract boolean containsDoubles()
meth public abstract boolean containsLocalDateTimes()
meth public abstract boolean containsLocalDates()
meth public abstract boolean containsLocalTimes()
meth public abstract boolean containsLongs()
meth public abstract boolean containsOffsetDateTimes()
meth public abstract boolean containsStrings()
meth public abstract boolean containsTables()
meth public abstract boolean isEmpty()
meth public abstract int size()
meth public abstract java.lang.Object get(int)
meth public abstract java.util.List<java.lang.Object> toList()
meth public abstract org.tomlj.TomlPosition inputPositionOf(int)
meth public boolean getBoolean(int)
meth public double getDouble(int)
meth public java.lang.String getString(int)
meth public java.lang.String toJson(java.util.EnumSet<org.tomlj.JsonOptions>)
meth public java.lang.String toToml()
meth public java.time.LocalDate getLocalDate(int)
meth public java.time.LocalDateTime getLocalDateTime(int)
meth public java.time.LocalTime getLocalTime(int)
meth public java.time.OffsetDateTime getOffsetDateTime(int)
meth public long getLong(int)
meth public org.tomlj.TomlArray getArray(int)
meth public org.tomlj.TomlTable getTable(int)
meth public void toJson(java.lang.Appendable,java.util.EnumSet<org.tomlj.JsonOptions>) throws java.io.IOException
meth public void toToml(java.lang.Appendable) throws java.io.IOException

CLSS public org.tomlj.TomlCommand
cons public init()
meth public static void main(java.lang.String[])
supr java.lang.Object

CLSS public org.tomlj.TomlInvalidTypeException
supr java.lang.RuntimeException

CLSS public final org.tomlj.TomlParseError
meth public java.lang.String toString()
meth public org.tomlj.TomlPosition position()
supr java.lang.RuntimeException
hfds position

CLSS public abstract interface org.tomlj.TomlParseResult
intf org.tomlj.TomlTable
meth public abstract java.util.List<org.tomlj.TomlParseError> errors()
meth public boolean hasErrors()

CLSS public final org.tomlj.TomlPosition
meth public boolean equals(java.lang.Object)
meth public int column()
meth public int hashCode()
meth public int line()
meth public java.lang.String toString()
meth public static org.tomlj.TomlPosition positionAt(int,int)
supr java.lang.Object
hfds column,line

CLSS public abstract interface org.tomlj.TomlTable

CLSS public final !enum org.tomlj.TomlVersion
fld public final static org.tomlj.TomlVersion HEAD
fld public final static org.tomlj.TomlVersion LATEST
fld public final static org.tomlj.TomlVersion V0_4_0
fld public final static org.tomlj.TomlVersion V0_5_0
fld public final static org.tomlj.TomlVersion V1_0_0
supr java.lang.Enum
hfds canonical

