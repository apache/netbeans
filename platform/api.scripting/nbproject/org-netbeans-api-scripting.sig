#Signature file v4.1
#Version 1.20

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

CLSS public final org.netbeans.api.scripting.Scripting
meth public javax.script.ScriptEngineManager build()
meth public org.netbeans.api.scripting.Scripting allowAllAccess(boolean)
meth public static javax.script.ScriptEngineManager createManager()
meth public static org.netbeans.api.scripting.Scripting newBuilder()
supr java.lang.Object
hfds allowAllAccess
hcls EngineManager

CLSS abstract interface org.netbeans.api.scripting.package-info

CLSS public abstract interface org.netbeans.spi.scripting.EngineProvider
meth public abstract java.util.List<javax.script.ScriptEngineFactory> factories()
meth public java.util.List<? extends javax.script.ScriptEngineFactory> factories(javax.script.ScriptEngineManager)

CLSS abstract interface org.netbeans.spi.scripting.package-info

