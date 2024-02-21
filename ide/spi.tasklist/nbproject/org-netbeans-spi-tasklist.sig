#Signature file v4.1
#Version 1.58.0

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

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

CLSS public abstract org.netbeans.spi.tasklist.FileTaskScanner
cons public init(java.lang.String,java.lang.String,java.lang.String)
innr public final static Callback
meth public abstract java.util.List<? extends org.netbeans.spi.tasklist.Task> scan(org.openide.filesystems.FileObject)
meth public abstract void attach(org.netbeans.spi.tasklist.FileTaskScanner$Callback)
meth public void notifyFinish()
meth public void notifyPrepare()
supr java.lang.Object
hfds description,displayName,optionsPath

CLSS public final static org.netbeans.spi.tasklist.FileTaskScanner$Callback
 outer org.netbeans.spi.tasklist.FileTaskScanner
meth public !varargs void refresh(org.openide.filesystems.FileObject[])
meth public void refreshAll()
supr java.lang.Object
hfds scanner,tm

CLSS public abstract org.netbeans.spi.tasklist.PushTaskScanner
cons public init(java.lang.String,java.lang.String,java.lang.String)
innr public final static Callback
meth public abstract void setScope(org.netbeans.spi.tasklist.TaskScanningScope,org.netbeans.spi.tasklist.PushTaskScanner$Callback)
supr java.lang.Object
hfds description,displayName,optionsPath

CLSS public final static org.netbeans.spi.tasklist.PushTaskScanner$Callback
 outer org.netbeans.spi.tasklist.PushTaskScanner
meth public boolean isCurrentEditorScope()
meth public boolean isObserved()
meth public void clearAllTasks()
meth public void finished()
meth public void setTasks(java.util.List<? extends org.netbeans.spi.tasklist.Task>)
meth public void setTasks(org.openide.filesystems.FileObject,java.util.List<? extends org.netbeans.spi.tasklist.Task>)
meth public void started()
supr java.lang.Object
hfds scanner,tm

CLSS public final org.netbeans.spi.tasklist.Task
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public static org.netbeans.spi.tasklist.Task create(java.net.URL,java.lang.String,java.lang.String)
meth public static org.netbeans.spi.tasklist.Task create(java.net.URL,java.lang.String,java.lang.String,java.awt.event.ActionListener,javax.swing.Action[])
meth public static org.netbeans.spi.tasklist.Task create(org.openide.filesystems.FileObject,java.lang.String,java.lang.String,int)
meth public static org.netbeans.spi.tasklist.Task create(org.openide.filesystems.FileObject,java.lang.String,java.lang.String,java.awt.event.ActionListener)
supr java.lang.Object
hfds actions,defaultAction,description,file,group,line,unknownTaskGroups,url

CLSS public abstract org.netbeans.spi.tasklist.TaskScanningScope
cons public init(java.lang.String,java.lang.String,java.awt.Image)
cons public init(java.lang.String,java.lang.String,java.awt.Image,boolean)
innr public final static Callback
intf java.lang.Iterable<org.openide.filesystems.FileObject>
intf org.openide.util.Lookup$Provider
meth public abstract boolean isInScope(org.openide.filesystems.FileObject)
meth public abstract org.openide.util.Lookup getLookup()
meth public abstract void attach(org.netbeans.spi.tasklist.TaskScanningScope$Callback)
supr java.lang.Object
hfds description,displayName,icon,isDefault

CLSS public final static org.netbeans.spi.tasklist.TaskScanningScope$Callback
 outer org.netbeans.spi.tasklist.TaskScanningScope
meth public void refresh()
supr java.lang.Object
hfds scope,tm

CLSS public abstract org.openide.util.Lookup
cons public init()
fld public final static org.openide.util.Lookup EMPTY
innr public abstract interface static Provider
innr public abstract static Item
innr public abstract static Result
innr public final static Template
meth public <%0 extends java.lang.Object> java.util.Collection<? extends {%%0}> lookupAll(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookupResult(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public static org.openide.util.Lookup getDefault()
supr java.lang.Object
hfds LOG,defaultLookup,defaultLookupProvider
hcls DefLookup,Empty

CLSS public abstract interface static org.openide.util.Lookup$Provider
 outer org.openide.util.Lookup
meth public abstract org.openide.util.Lookup getLookup()

