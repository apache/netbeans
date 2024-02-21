#Signature file v4.1
#Version 1.54

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

CLSS public abstract interface org.netbeans.core.netigso.spi.BundleContent
meth public abstract byte[] resource(java.lang.String) throws java.io.IOException

CLSS public final org.netbeans.core.netigso.spi.NetigsoArchive
meth public boolean isActive()
meth public byte[] fromArchive(java.lang.String) throws java.io.IOException
meth public final byte[] patchByteCode(java.lang.ClassLoader,java.lang.String,java.security.ProtectionDomain,byte[])
meth public org.netbeans.core.netigso.spi.NetigsoArchive forBundle(long,org.netbeans.core.netigso.spi.BundleContent)
supr java.lang.Object
hfds bundleId,content,netigso

