#Signature file v4.1
#Version 1.40

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

CLSS public abstract org.netbeans.modules.project.indexingbridge.IndexingBridge
cons protected init()
innr public abstract static Ordering
innr public final Lock
meth protected abstract void enterProtectedMode()
meth protected abstract void exitProtectedMode()
meth public final org.netbeans.modules.project.indexingbridge.IndexingBridge$Lock protectedMode()
meth public final org.netbeans.modules.project.indexingbridge.IndexingBridge$Lock protectedMode(boolean)
meth public static org.netbeans.modules.project.indexingbridge.IndexingBridge getDefault()
supr java.lang.Object
hfds LOG
hcls Stack

CLSS public final org.netbeans.modules.project.indexingbridge.IndexingBridge$Lock
 outer org.netbeans.modules.project.indexingbridge.IndexingBridge
cons public init(org.netbeans.modules.project.indexingbridge.IndexingBridge)
meth protected void finalize() throws java.lang.Throwable
meth public void release()
supr java.lang.Object
hfds creationStack,releaseStack

CLSS public abstract static org.netbeans.modules.project.indexingbridge.IndexingBridge$Ordering
 outer org.netbeans.modules.project.indexingbridge.IndexingBridge
cons public init()
meth protected abstract void await() throws java.lang.InterruptedException
supr org.netbeans.modules.project.indexingbridge.IndexingBridge

