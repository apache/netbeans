#Signature file v4.1
#Version 1.50

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

CLSS public abstract interface org.netbeans.spi.quicksearch.SearchProvider
meth public abstract void evaluate(org.netbeans.spi.quicksearch.SearchRequest,org.netbeans.spi.quicksearch.SearchResponse)

CLSS public final org.netbeans.spi.quicksearch.SearchRequest
meth public java.lang.String getText()
meth public java.util.List<? extends javax.swing.KeyStroke> getShortcut()
supr java.lang.Object
hfds stroke,text

CLSS public final org.netbeans.spi.quicksearch.SearchResponse
meth public boolean addResult(java.lang.Runnable,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
meth public boolean addResult(java.lang.Runnable,java.lang.String,java.lang.String,java.util.List<? extends javax.swing.KeyStroke>)
 anno 0 org.netbeans.api.annotations.common.CheckReturnValue()
meth public boolean isObsolete()
supr java.lang.Object
hfds catResult,sRequest

