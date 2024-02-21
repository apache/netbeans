#Signature file v4.1
#Version 1.26

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

CLSS public final org.netbeans.modules.project.spi.intern.ProjectIDEServices
cons public init()
meth public static boolean isEventDispatchThread()
meth public static boolean isUserQuestionException(java.io.IOException)
meth public static javax.swing.Icon loadImageIcon(java.lang.String,boolean)
meth public static org.netbeans.modules.project.spi.intern.ProjectIDEServicesImplementation$FileBuiltQuerySource createFileBuiltQuerySource(org.openide.filesystems.FileObject)
meth public static void handleUserQuestionException(java.io.IOException,org.netbeans.modules.project.spi.intern.ProjectIDEServicesImplementation$UserQuestionExceptionCallback)
meth public static void notifyWarning(java.lang.String)
supr java.lang.Object
hfds impl

CLSS public abstract interface org.netbeans.modules.project.spi.intern.ProjectIDEServicesImplementation
innr public abstract interface static FileBuiltQuerySource
innr public abstract interface static UserQuestionExceptionCallback
meth public abstract boolean isEventDispatchThread()
meth public abstract boolean isUserQuestionException(java.io.IOException)
meth public abstract javax.swing.Icon loadIcon(java.lang.String,boolean)
meth public abstract org.netbeans.modules.project.spi.intern.ProjectIDEServicesImplementation$FileBuiltQuerySource createFileBuiltQuerySource(org.openide.filesystems.FileObject)
meth public abstract void handleUserQuestionException(java.io.IOException,org.netbeans.modules.project.spi.intern.ProjectIDEServicesImplementation$UserQuestionExceptionCallback)
meth public abstract void notifyWarning(java.lang.String)

CLSS public abstract interface static org.netbeans.modules.project.spi.intern.ProjectIDEServicesImplementation$FileBuiltQuerySource
 outer org.netbeans.modules.project.spi.intern.ProjectIDEServicesImplementation
fld public final static java.lang.String PROP_MODIFIED = "modified"
meth public abstract boolean isModified()
meth public abstract boolean isValid()
meth public abstract org.openide.filesystems.FileObject getFileObject()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public abstract interface static org.netbeans.modules.project.spi.intern.ProjectIDEServicesImplementation$UserQuestionExceptionCallback
 outer org.netbeans.modules.project.spi.intern.ProjectIDEServicesImplementation
meth public abstract void accepted()
meth public abstract void denied()
meth public abstract void error(java.io.IOException)

