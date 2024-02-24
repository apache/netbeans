#Signature file v4.1
#Version 1.56

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

CLSS public abstract interface org.netbeans.spi.gototest.TestLocator
innr public abstract interface static LocationListener
innr public final static !enum FileType
innr public static LocationResult
meth public abstract boolean appliesTo(org.openide.filesystems.FileObject)
meth public abstract boolean asynchronous()
meth public abstract org.netbeans.spi.gototest.TestLocator$FileType getFileType(org.openide.filesystems.FileObject)
meth public abstract org.netbeans.spi.gototest.TestLocator$LocationResult findOpposite(org.openide.filesystems.FileObject,int)
meth public abstract void findOpposite(org.openide.filesystems.FileObject,int,org.netbeans.spi.gototest.TestLocator$LocationListener)

CLSS public final static !enum org.netbeans.spi.gototest.TestLocator$FileType
 outer org.netbeans.spi.gototest.TestLocator
fld public final static org.netbeans.spi.gototest.TestLocator$FileType NEITHER
fld public final static org.netbeans.spi.gototest.TestLocator$FileType TEST
fld public final static org.netbeans.spi.gototest.TestLocator$FileType TESTED
meth public static org.netbeans.spi.gototest.TestLocator$FileType valueOf(java.lang.String)
meth public static org.netbeans.spi.gototest.TestLocator$FileType[] values()
supr java.lang.Enum<org.netbeans.spi.gototest.TestLocator$FileType>

CLSS public abstract interface static org.netbeans.spi.gototest.TestLocator$LocationListener
 outer org.netbeans.spi.gototest.TestLocator
meth public abstract void foundLocation(org.openide.filesystems.FileObject,org.netbeans.spi.gototest.TestLocator$LocationResult)

CLSS public static org.netbeans.spi.gototest.TestLocator$LocationResult
 outer org.netbeans.spi.gototest.TestLocator
cons public init(java.lang.String)
cons public init(org.openide.filesystems.FileObject,int)
meth public int getOffset()
meth public java.lang.String getErrorMessage()
meth public org.openide.filesystems.FileObject getFileObject()
supr java.lang.Object
hfds error,file,offset

