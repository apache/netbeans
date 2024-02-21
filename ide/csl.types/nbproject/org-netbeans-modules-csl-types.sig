#Signature file v4.1
#Version 1.23

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

CLSS public final org.netbeans.modules.csl.api.Documentation
meth public java.lang.String getContent()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.net.URL getUrl()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.csl.api.Documentation create(java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.csl.api.Documentation create(java.lang.String,java.net.URL)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds content,url

CLSS public abstract interface org.netbeans.modules.csl.api.ElementHandle
innr public static UrlHandle
meth public abstract boolean signatureEquals(org.netbeans.modules.csl.api.ElementHandle)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getIn()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getMimeType()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.ElementKind getKind()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.csl.api.OffsetRange getOffsetRange(org.netbeans.modules.csl.spi.ParserResult)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject getFileObject()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public static org.netbeans.modules.csl.api.ElementHandle$UrlHandle
 outer org.netbeans.modules.csl.api.ElementHandle
cons public init(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
intf org.netbeans.modules.csl.api.ElementHandle
meth public boolean signatureEquals(org.netbeans.modules.csl.api.ElementHandle)
meth public java.lang.String getIn()
meth public java.lang.String getMimeType()
meth public java.lang.String getName()
meth public java.lang.String getUrl()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Set<org.netbeans.modules.csl.api.Modifier> getModifiers()
meth public org.netbeans.modules.csl.api.ElementKind getKind()
meth public org.netbeans.modules.csl.api.OffsetRange getOffsetRange(org.netbeans.modules.csl.spi.ParserResult)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.filesystems.FileObject getFileObject()
supr java.lang.Object
hfds url

CLSS public final !enum org.netbeans.modules.csl.api.ElementKind
fld public final static org.netbeans.modules.csl.api.ElementKind ATTRIBUTE
fld public final static org.netbeans.modules.csl.api.ElementKind CALL
fld public final static org.netbeans.modules.csl.api.ElementKind CLASS
fld public final static org.netbeans.modules.csl.api.ElementKind CONSTANT
fld public final static org.netbeans.modules.csl.api.ElementKind CONSTRUCTOR
fld public final static org.netbeans.modules.csl.api.ElementKind DB
fld public final static org.netbeans.modules.csl.api.ElementKind ERROR
fld public final static org.netbeans.modules.csl.api.ElementKind FIELD
fld public final static org.netbeans.modules.csl.api.ElementKind FILE
fld public final static org.netbeans.modules.csl.api.ElementKind GLOBAL
fld public final static org.netbeans.modules.csl.api.ElementKind INTERFACE
fld public final static org.netbeans.modules.csl.api.ElementKind KEYWORD
fld public final static org.netbeans.modules.csl.api.ElementKind METHOD
fld public final static org.netbeans.modules.csl.api.ElementKind MODULE
fld public final static org.netbeans.modules.csl.api.ElementKind OTHER
fld public final static org.netbeans.modules.csl.api.ElementKind PACKAGE
fld public final static org.netbeans.modules.csl.api.ElementKind PARAMETER
fld public final static org.netbeans.modules.csl.api.ElementKind PROPERTY
fld public final static org.netbeans.modules.csl.api.ElementKind RULE
fld public final static org.netbeans.modules.csl.api.ElementKind TAG
fld public final static org.netbeans.modules.csl.api.ElementKind TEST
fld public final static org.netbeans.modules.csl.api.ElementKind VARIABLE
meth public static org.netbeans.modules.csl.api.ElementKind valueOf(java.lang.String)
meth public static org.netbeans.modules.csl.api.ElementKind[] values()
supr java.lang.Enum<org.netbeans.modules.csl.api.ElementKind>

CLSS public abstract interface org.netbeans.modules.csl.api.Error
innr public abstract interface static Badging
meth public abstract boolean isLineError()
meth public abstract int getEndPosition()
meth public abstract int getStartPosition()
meth public abstract java.lang.Object[] getParameters()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getKey()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.netbeans.modules.csl.api.Severity getSeverity()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.filesystems.FileObject getFile()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface static org.netbeans.modules.csl.api.Error$Badging
 outer org.netbeans.modules.csl.api.Error
intf org.netbeans.modules.csl.api.Error
meth public abstract boolean showExplorerBadge()

CLSS public final !enum org.netbeans.modules.csl.api.Modifier
fld public final static org.netbeans.modules.csl.api.Modifier ABSTRACT
fld public final static org.netbeans.modules.csl.api.Modifier DEPRECATED
fld public final static org.netbeans.modules.csl.api.Modifier PRIVATE
fld public final static org.netbeans.modules.csl.api.Modifier PROTECTED
fld public final static org.netbeans.modules.csl.api.Modifier PUBLIC
fld public final static org.netbeans.modules.csl.api.Modifier STATIC
meth public static org.netbeans.modules.csl.api.Modifier valueOf(java.lang.String)
meth public static org.netbeans.modules.csl.api.Modifier[] values()
supr java.lang.Enum<org.netbeans.modules.csl.api.Modifier>

CLSS public final org.netbeans.modules.csl.api.OffsetRange
cons public init(int,int)
fld public final static org.netbeans.modules.csl.api.OffsetRange NONE
intf java.lang.Comparable<org.netbeans.modules.csl.api.OffsetRange>
meth public boolean containsInclusive(int)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public boolean overlaps(org.netbeans.modules.csl.api.OffsetRange)
meth public int compareTo(org.netbeans.modules.csl.api.OffsetRange)
meth public int getEnd()
meth public int getLength()
meth public int getStart()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.netbeans.modules.csl.api.OffsetRange boundTo(int,int)
supr java.lang.Object
hfds end,start

CLSS public final !enum org.netbeans.modules.csl.api.Severity
fld public final static org.netbeans.modules.csl.api.Severity ERROR
fld public final static org.netbeans.modules.csl.api.Severity FATAL
fld public final static org.netbeans.modules.csl.api.Severity INFO
fld public final static org.netbeans.modules.csl.api.Severity WARNING
meth public static org.netbeans.modules.csl.api.Severity valueOf(java.lang.String)
meth public static org.netbeans.modules.csl.api.Severity[] values()
supr java.lang.Enum<org.netbeans.modules.csl.api.Severity>

CLSS public abstract org.netbeans.modules.csl.spi.ParserResult
cons protected init(org.netbeans.modules.parsing.api.Snapshot)
meth public abstract java.util.List<? extends org.netbeans.modules.csl.api.Error> getDiagnostics()
supr org.netbeans.modules.parsing.spi.Parser$Result

CLSS public abstract org.netbeans.modules.parsing.spi.Parser
cons public init()
innr public abstract static Result
innr public final static !enum CancelReason
meth public abstract org.netbeans.modules.parsing.spi.Parser$Result getResult(org.netbeans.modules.parsing.api.Task) throws org.netbeans.modules.parsing.spi.ParseException
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void parse(org.netbeans.modules.parsing.api.Snapshot,org.netbeans.modules.parsing.api.Task,org.netbeans.modules.parsing.spi.SourceModificationEvent) throws org.netbeans.modules.parsing.spi.ParseException
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public void cancel()
 anno 0 java.lang.Deprecated()
meth public void cancel(org.netbeans.modules.parsing.spi.Parser$CancelReason,org.netbeans.modules.parsing.spi.SourceModificationEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hcls MyAccessor

CLSS public abstract static org.netbeans.modules.parsing.spi.Parser$Result
 outer org.netbeans.modules.parsing.spi.Parser
cons protected init(org.netbeans.modules.parsing.api.Snapshot)
meth protected abstract void invalidate()
meth protected boolean processingFinished()
meth public org.netbeans.modules.parsing.api.Snapshot getSnapshot()
supr java.lang.Object
hfds snapshot

