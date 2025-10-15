#Signature file v4.1
#Version 1.6

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="9")
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

CLSS public final org.netbeans.modules.java.file.launcher.api.SourceLauncher
cons public init()
meth public static boolean isIndexedSourceLauncherFile(org.openide.filesystems.FileObject)
meth public static boolean isSourceLauncherFile(org.openide.filesystems.FileObject)
meth public static java.lang.String joinCommandLines(java.lang.Iterable<? extends java.lang.String>)
supr java.lang.Object
hfds CLASSPATH,CLASS_PATH,CP,ENABLE_PREVIEW,MODULE_PATH,P,SOURCE

CLSS public abstract interface org.netbeans.modules.java.file.launcher.spi.SingleFileOptionsQueryImplementation
innr public abstract interface static Result
meth public abstract org.netbeans.modules.java.file.launcher.spi.SingleFileOptionsQueryImplementation$Result optionsFor(org.openide.filesystems.FileObject)

CLSS public abstract interface static org.netbeans.modules.java.file.launcher.spi.SingleFileOptionsQueryImplementation$Result
 outer org.netbeans.modules.java.file.launcher.spi.SingleFileOptionsQueryImplementation
meth public abstract java.lang.String getOptions()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public boolean registerRoot()
meth public java.net.URI getWorkDirectory()
 anno 0 org.netbeans.api.annotations.common.NonNull()

