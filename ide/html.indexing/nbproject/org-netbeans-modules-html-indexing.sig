#Signature file v4.1
#Version 1.15

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

CLSS public org.netbeans.modules.html.editor.api.index.HtmlIndex
fld public final static int VERSION = 3
fld public final static java.lang.String NAME = "html"
fld public final static java.lang.String REFERS_KEY = "imports"
innr public static AllDependenciesMaps
meth public java.util.Collection<org.openide.filesystems.FileObject> find(java.lang.String,java.lang.String)
meth public java.util.List<java.net.URL> getAllRemoteDependencies() throws java.io.IOException
meth public org.netbeans.modules.html.editor.api.index.HtmlIndex$AllDependenciesMaps getAllDependencies() throws java.io.IOException
meth public org.netbeans.modules.web.common.api.DependenciesGraph getDependencies(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.html.editor.api.index.HtmlIndex get(org.netbeans.api.project.Project) throws java.io.IOException
meth public static org.netbeans.modules.html.editor.api.index.HtmlIndex get(org.netbeans.api.project.Project,boolean) throws java.io.IOException
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void notifyChange()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds INDEXES,changeSupport,querySupport

CLSS public static org.netbeans.modules.html.editor.api.index.HtmlIndex$AllDependenciesMaps
 outer org.netbeans.modules.html.editor.api.index.HtmlIndex
cons public init(java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<org.netbeans.modules.web.common.api.FileReference>>,java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<org.netbeans.modules.web.common.api.FileReference>>)
meth public java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<org.netbeans.modules.web.common.api.FileReference>> getDest2source()
meth public java.util.Map<org.openide.filesystems.FileObject,java.util.Collection<org.netbeans.modules.web.common.api.FileReference>> getSource2dest()
supr java.lang.Object
hfds dest2source,source2dest

