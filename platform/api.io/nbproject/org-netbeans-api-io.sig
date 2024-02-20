#Signature file v4.1
#Version 1.26

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

CLSS public java.io.PrintWriter
cons public init(java.io.File) throws java.io.FileNotFoundException
cons public init(java.io.File,java.lang.String) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
cons public init(java.io.OutputStream)
cons public init(java.io.OutputStream,boolean)
cons public init(java.io.Writer)
cons public init(java.io.Writer,boolean)
cons public init(java.lang.String) throws java.io.FileNotFoundException
cons public init(java.lang.String,java.lang.String) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
fld protected java.io.Writer out
meth protected void clearError()
meth protected void setError()
meth public !varargs java.io.PrintWriter format(java.lang.String,java.lang.Object[])
meth public !varargs java.io.PrintWriter format(java.util.Locale,java.lang.String,java.lang.Object[])
meth public !varargs java.io.PrintWriter printf(java.lang.String,java.lang.Object[])
meth public !varargs java.io.PrintWriter printf(java.util.Locale,java.lang.String,java.lang.Object[])
meth public boolean checkError()
meth public java.io.PrintWriter append(char)
meth public java.io.PrintWriter append(java.lang.CharSequence)
meth public java.io.PrintWriter append(java.lang.CharSequence,int,int)
meth public void close()
meth public void flush()
meth public void print(boolean)
meth public void print(char)
meth public void print(char[])
meth public void print(double)
meth public void print(float)
meth public void print(int)
meth public void print(java.lang.Object)
meth public void print(java.lang.String)
meth public void print(long)
meth public void println()
meth public void println(boolean)
meth public void println(char)
meth public void println(char[])
meth public void println(double)
meth public void println(float)
meth public void println(int)
meth public void println(java.lang.Object)
meth public void println(java.lang.String)
meth public void println(long)
meth public void write(char[])
meth public void write(char[],int,int)
meth public void write(int)
meth public void write(java.lang.String)
meth public void write(java.lang.String,int,int)
supr java.io.Writer

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

CLSS public abstract org.netbeans.api.io.Fold
meth public abstract void setExpanded(boolean)
meth public final void collapse()
meth public final void expand()
supr java.lang.Object
hfds UNSUPPORTED
hcls Impl

CLSS public abstract org.netbeans.api.io.Hyperlink
meth public static org.netbeans.api.io.Hyperlink from(java.lang.Runnable)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.io.Hyperlink from(java.lang.Runnable,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.io.Hyperlink from(org.netbeans.api.intent.Intent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.io.Hyperlink from(org.netbeans.api.intent.Intent,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds important
hcls IntentHyperlink,OnClickHyperlink

CLSS public abstract org.netbeans.api.io.IOProvider
meth public abstract java.lang.String getId()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.api.io.InputOutput getIO(java.lang.String,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.api.io.InputOutput getIO(java.lang.String,boolean,org.openide.util.Lookup)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.io.IOProvider get(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.io.IOProvider getDefault()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hcls Impl,Trivial

CLSS public abstract org.netbeans.api.io.InputOutput
intf org.openide.util.Lookup$Provider
meth public abstract boolean isClosed()
meth public abstract java.io.Reader getIn()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.api.io.OutputWriter getErr()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.api.io.OutputWriter getOut()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.util.Lookup getLookup()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void close()
meth public abstract void reset()
meth public abstract void setDescription(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void show(java.util.Set<org.netbeans.api.io.ShowOperation>)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public final void show()
meth public static org.netbeans.api.io.InputOutput get(java.lang.String,boolean)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.io.InputOutput get(java.lang.String,boolean,org.openide.util.Lookup)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds DEFAULT_SHOW_OPERATIONS
hcls Impl

CLSS public abstract org.netbeans.api.io.OutputColor
meth public static org.netbeans.api.io.OutputColor debug()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.io.OutputColor failure()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.io.OutputColor rgb(int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.io.OutputColor rgb(int,int,int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.io.OutputColor success()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.io.OutputColor warning()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds CLR_DEBUG,CLR_FAILURE,CLR_SUCCESS,CLR_WARNING,type
hcls RgbColor,TypeColor

CLSS public abstract org.netbeans.api.io.OutputWriter
meth public abstract org.netbeans.api.io.Fold startFold(boolean)
meth public abstract org.netbeans.api.io.Position getCurrentPosition()
meth public abstract void endFold(org.netbeans.api.io.Fold)
meth public abstract void print(java.lang.String,org.netbeans.api.io.Hyperlink)
meth public abstract void print(java.lang.String,org.netbeans.api.io.Hyperlink,org.netbeans.api.io.OutputColor)
meth public abstract void print(java.lang.String,org.netbeans.api.io.OutputColor)
meth public abstract void println(java.lang.String,org.netbeans.api.io.Hyperlink)
meth public abstract void println(java.lang.String,org.netbeans.api.io.Hyperlink,org.netbeans.api.io.OutputColor)
meth public abstract void println(java.lang.String,org.netbeans.api.io.OutputColor)
supr java.io.PrintWriter
hcls DummyWriter,Impl

CLSS public abstract org.netbeans.api.io.Position
meth public abstract void scrollTo()
supr java.lang.Object
hfds UNSUPPORTED
hcls Impl

CLSS public final !enum org.netbeans.api.io.ShowOperation
fld public final static org.netbeans.api.io.ShowOperation ACTIVATE
fld public final static org.netbeans.api.io.ShowOperation MAKE_VISIBLE
fld public final static org.netbeans.api.io.ShowOperation OPEN
meth public static org.netbeans.api.io.ShowOperation valueOf(java.lang.String)
meth public static org.netbeans.api.io.ShowOperation[] values()
supr java.lang.Enum<org.netbeans.api.io.ShowOperation>

CLSS public abstract interface org.netbeans.spi.io.InputOutputProvider<%0 extends java.lang.Object, %1 extends java.io.PrintWriter, %2 extends java.lang.Object, %3 extends java.lang.Object>
meth public abstract boolean isIOClosed({org.netbeans.spi.io.InputOutputProvider%0})
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.io.Reader getIn({org.netbeans.spi.io.InputOutputProvider%0})
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getIODescription({org.netbeans.spi.io.InputOutputProvider%0})
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getId()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.util.Lookup getIOLookup({org.netbeans.spi.io.InputOutputProvider%0})
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void closeIO({org.netbeans.spi.io.InputOutputProvider%0})
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void endFold({org.netbeans.spi.io.InputOutputProvider%0},{org.netbeans.spi.io.InputOutputProvider%1},{org.netbeans.spi.io.InputOutputProvider%3})
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract void print({org.netbeans.spi.io.InputOutputProvider%0},{org.netbeans.spi.io.InputOutputProvider%1},java.lang.String,org.netbeans.api.io.Hyperlink,org.netbeans.api.io.OutputColor,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void resetIO({org.netbeans.spi.io.InputOutputProvider%0})
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void scrollTo({org.netbeans.spi.io.InputOutputProvider%0},{org.netbeans.spi.io.InputOutputProvider%1},{org.netbeans.spi.io.InputOutputProvider%2})
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setFoldExpanded({org.netbeans.spi.io.InputOutputProvider%0},{org.netbeans.spi.io.InputOutputProvider%1},{org.netbeans.spi.io.InputOutputProvider%3},boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setIODescription({org.netbeans.spi.io.InputOutputProvider%0},java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void showIO({org.netbeans.spi.io.InputOutputProvider%0},java.util.Set<org.netbeans.api.io.ShowOperation>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract {org.netbeans.spi.io.InputOutputProvider%0} getIO(java.lang.String,boolean,org.openide.util.Lookup)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract {org.netbeans.spi.io.InputOutputProvider%1} getErr({org.netbeans.spi.io.InputOutputProvider%0})
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract {org.netbeans.spi.io.InputOutputProvider%1} getOut({org.netbeans.spi.io.InputOutputProvider%0})
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract {org.netbeans.spi.io.InputOutputProvider%2} getCurrentPosition({org.netbeans.spi.io.InputOutputProvider%0},{org.netbeans.spi.io.InputOutputProvider%1})
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract {org.netbeans.spi.io.InputOutputProvider%3} startFold({org.netbeans.spi.io.InputOutputProvider%0},{org.netbeans.spi.io.InputOutputProvider%1},boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public final !enum org.netbeans.spi.io.support.HyperlinkType
fld public final static org.netbeans.spi.io.support.HyperlinkType FROM_INTENT
fld public final static org.netbeans.spi.io.support.HyperlinkType FROM_RUNNABLE
meth public static org.netbeans.spi.io.support.HyperlinkType valueOf(java.lang.String)
meth public static org.netbeans.spi.io.support.HyperlinkType[] values()
supr java.lang.Enum<org.netbeans.spi.io.support.HyperlinkType>

CLSS public final org.netbeans.spi.io.support.Hyperlinks
meth public static boolean isImportant(org.netbeans.api.io.Hyperlink)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static java.lang.Runnable getRunnable(org.netbeans.api.io.Hyperlink)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.intent.Intent getIntent(org.netbeans.api.io.Hyperlink)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.io.support.HyperlinkType getType(org.netbeans.api.io.Hyperlink)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void invoke(org.netbeans.api.io.Hyperlink)
supr java.lang.Object

CLSS public final !enum org.netbeans.spi.io.support.OutputColorType
fld public final static org.netbeans.spi.io.support.OutputColorType DEBUG
fld public final static org.netbeans.spi.io.support.OutputColorType FAILURE
fld public final static org.netbeans.spi.io.support.OutputColorType RGB
fld public final static org.netbeans.spi.io.support.OutputColorType SUCCESS
fld public final static org.netbeans.spi.io.support.OutputColorType WARNING
meth public static org.netbeans.spi.io.support.OutputColorType valueOf(java.lang.String)
meth public static org.netbeans.spi.io.support.OutputColorType[] values()
supr java.lang.Enum<org.netbeans.spi.io.support.OutputColorType>

CLSS public final org.netbeans.spi.io.support.OutputColors
meth public static int getRGB(org.netbeans.api.io.OutputColor)
meth public static org.netbeans.spi.io.support.OutputColorType getType(org.netbeans.api.io.OutputColor)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract org.openide.util.Lookup
cons public init()
fld public final static org.openide.util.Lookup EMPTY
innr public abstract interface static Provider
innr public abstract static Item
innr public abstract static Result
innr public final static Template
meth public <%0 extends java.lang.Object> java.util.Collection<? extends {%%0}> lookupAll(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookupResult(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public static org.openide.util.Lookup getDefault()
supr java.lang.Object
hfds LOG,defaultLookup,defaultLookupProvider
hcls DefLookup,Empty

CLSS public abstract interface static org.openide.util.Lookup$Provider
 outer org.openide.util.Lookup
meth public abstract org.openide.util.Lookup getLookup()

