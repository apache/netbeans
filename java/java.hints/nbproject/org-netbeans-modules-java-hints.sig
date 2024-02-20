#Signature file v4.1
#Version 1.106.0

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

CLSS public abstract interface org.netbeans.modules.java.hints.friendapi.OverrideErrorMessage<%0 extends java.lang.Object>
intf org.netbeans.modules.java.hints.spi.ErrorRule<{org.netbeans.modules.java.hints.friendapi.OverrideErrorMessage%0}>
meth public abstract java.lang.String createMessage(org.netbeans.api.java.source.CompilationInfo,javax.tools.Diagnostic,int,com.sun.source.util.TreePath,org.netbeans.modules.java.hints.spi.ErrorRule$Data<{org.netbeans.modules.java.hints.friendapi.OverrideErrorMessage%0}>)

CLSS public org.netbeans.modules.java.hints.friendapi.SourceChangeUtils
cons public init()
meth public static void doOrganizeImports(org.netbeans.api.java.source.WorkingCopy,java.util.Set<javax.lang.model.element.Element>,boolean)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.java.hints.spi.ErrorRule<%0 extends java.lang.Object>
innr public final static Data
intf org.netbeans.modules.java.hints.spi.Rule
meth public abstract java.util.List<org.netbeans.spi.editor.hints.Fix> run(org.netbeans.api.java.source.CompilationInfo,java.lang.String,int,com.sun.source.util.TreePath,org.netbeans.modules.java.hints.spi.ErrorRule$Data<{org.netbeans.modules.java.hints.spi.ErrorRule%0}>)
meth public abstract java.util.Set<java.lang.String> getCodes()

CLSS public abstract interface org.netbeans.modules.java.hints.spi.Rule
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getId()
meth public abstract void cancel()

