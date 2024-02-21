#Signature file v4.1
#Version 1.66

CLSS public abstract interface java.io.Serializable

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

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public javax.swing.event.ChangeEvent
cons public init(java.lang.Object)
supr java.util.EventObject

CLSS public abstract interface org.netbeans.api.fileinfo.NonRecursiveFolder
meth public abstract org.openide.filesystems.FileObject getFolder()

CLSS public final org.netbeans.api.queries.CollocationQuery
meth public static boolean areCollocated(java.io.File,java.io.File)
 anno 0 java.lang.Deprecated()
meth public static boolean areCollocated(java.net.URI,java.net.URI)
meth public static java.io.File findRoot(java.io.File)
 anno 0 java.lang.Deprecated()
meth public static java.net.URI findRoot(java.net.URI)
supr java.lang.Object
hfds implementations,implementations2

CLSS public final org.netbeans.api.queries.FileBuiltQuery
innr public abstract interface static Status
meth public static org.netbeans.api.queries.FileBuiltQuery$Status getStatus(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds implementations

CLSS public abstract interface static org.netbeans.api.queries.FileBuiltQuery$Status
 outer org.netbeans.api.queries.FileBuiltQuery
meth public abstract boolean isBuilt()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public org.netbeans.api.queries.FileEncodingQuery
meth public static java.nio.charset.Charset getDefaultEncoding()
meth public static java.nio.charset.Charset getEncoding(org.openide.filesystems.FileObject)
meth public static void setDefaultEncoding(java.nio.charset.Charset)
supr java.lang.Object
hfds BUFSIZ,DECODER_SELECTED,DEFAULT_ENCODING,ENCODER_SELECTED,LOG,UTF_8
hcls ProxyCharset

CLSS public final org.netbeans.api.queries.SharabilityQuery
fld public final static int MIXED = 3
 anno 0 java.lang.Deprecated()
fld public final static int NOT_SHARABLE = 2
 anno 0 java.lang.Deprecated()
fld public final static int SHARABLE = 1
 anno 0 java.lang.Deprecated()
fld public final static int UNKNOWN = 0
 anno 0 java.lang.Deprecated()
innr public final static !enum Sharability
meth public static int getSharability(java.io.File)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.api.queries.SharabilityQuery$Sharability getSharability(java.net.URI)
meth public static org.netbeans.api.queries.SharabilityQuery$Sharability getSharability(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds LOG,implementations,implementations2

CLSS public final static !enum org.netbeans.api.queries.SharabilityQuery$Sharability
 outer org.netbeans.api.queries.SharabilityQuery
fld public final static org.netbeans.api.queries.SharabilityQuery$Sharability MIXED
fld public final static org.netbeans.api.queries.SharabilityQuery$Sharability NOT_SHARABLE
fld public final static org.netbeans.api.queries.SharabilityQuery$Sharability SHARABLE
fld public final static org.netbeans.api.queries.SharabilityQuery$Sharability UNKNOWN
meth public static org.netbeans.api.queries.SharabilityQuery$Sharability valueOf(java.lang.String)
meth public static org.netbeans.api.queries.SharabilityQuery$Sharability[] values()
supr java.lang.Enum<org.netbeans.api.queries.SharabilityQuery$Sharability>

CLSS public final org.netbeans.api.queries.VersioningQuery
meth public static boolean isManaged(java.net.URI)
meth public static java.lang.String getRemoteLocation(java.net.URI)
supr java.lang.Object
hfds LOG,implementations

CLSS public final org.netbeans.api.queries.VisibilityQuery
meth public boolean isVisible(java.io.File)
meth public boolean isVisible(org.openide.filesystems.FileObject)
meth public final static org.netbeans.api.queries.VisibilityQuery getDefault()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds INSTANCE,cachedVqiInstances,listeners,resultListener,vqiListener,vqiResult
hcls ResultListener,VqiChangedListener

CLSS public abstract interface org.netbeans.spi.queries.CollocationQueryImplementation
 anno 0 java.lang.Deprecated()
meth public abstract boolean areCollocated(java.io.File,java.io.File)
meth public abstract java.io.File findRoot(java.io.File)

CLSS public abstract interface org.netbeans.spi.queries.CollocationQueryImplementation2
meth public abstract boolean areCollocated(java.net.URI,java.net.URI)
meth public abstract java.net.URI findRoot(java.net.URI)

CLSS public abstract interface org.netbeans.spi.queries.FileBuiltQueryImplementation
meth public abstract org.netbeans.api.queries.FileBuiltQuery$Status getStatus(org.openide.filesystems.FileObject)

CLSS public abstract org.netbeans.spi.queries.FileEncodingQueryImplementation
cons public init()
meth protected static void throwUnknownEncoding()
meth public abstract java.nio.charset.Charset getEncoding(org.openide.filesystems.FileObject)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.queries.SharabilityQueryImplementation
 anno 0 java.lang.Deprecated()
meth public abstract int getSharability(java.io.File)

CLSS public abstract interface org.netbeans.spi.queries.SharabilityQueryImplementation2
meth public abstract org.netbeans.api.queries.SharabilityQuery$Sharability getSharability(java.net.URI)

CLSS public abstract interface org.netbeans.spi.queries.VersioningQueryImplementation
meth public abstract boolean isManaged(java.net.URI)
meth public abstract java.lang.String getRemoteLocation(java.net.URI)

CLSS public final org.netbeans.spi.queries.VisibilityQueryChangeEvent
cons public init(java.lang.Object,org.openide.filesystems.FileObject[])
meth public org.openide.filesystems.FileObject[] getFileObjects()
supr javax.swing.event.ChangeEvent
hfds fileObjects

CLSS public abstract interface org.netbeans.spi.queries.VisibilityQueryImplementation
meth public abstract boolean isVisible(org.openide.filesystems.FileObject)
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public abstract interface org.netbeans.spi.queries.VisibilityQueryImplementation2
intf org.netbeans.spi.queries.VisibilityQueryImplementation
meth public abstract boolean isVisible(java.io.File)

