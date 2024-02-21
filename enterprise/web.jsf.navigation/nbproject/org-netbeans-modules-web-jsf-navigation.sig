#Signature file v4.1
#Version 2.49

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

CLSS public org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentItem
cons public init(java.lang.String,java.lang.String,java.awt.Image)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.awt.Image)
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public java.awt.Image getBufferedIcon()
meth public java.awt.Image getIcon()
meth public java.lang.String getFromAction()
meth public java.lang.String getFromOutcome()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public javax.swing.Action[] getActions()
meth public void setFromAction(java.lang.String)
meth public void setFromOutcome(java.lang.String)
meth public void setName(java.lang.String)
supr java.lang.Object
hfds LOG,UNKONWN_ICON,actions,bufferedIcon,fromAction,fromOutcome,icon,name

CLSS public abstract org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModel
cons public init()
meth public abstract java.util.Collection<org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentItem> getPageContentItems()
meth public abstract javax.swing.Action[] getActions()
meth public abstract void addPageContentItem(org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentItem)
meth public abstract void destroy() throws java.io.IOException
meth public abstract void removePageContentItem(org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentItem)
meth public final void addChangeListener(javax.swing.event.ChangeListener)
meth public final void handleModelChangeEvent()
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds listeners,pageContentItems

CLSS public abstract interface org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModelProvider
meth public abstract org.netbeans.modules.web.jsf.navigation.pagecontentmodel.PageContentModel getPageContentModel(org.openide.filesystems.FileObject)
meth public abstract org.openide.filesystems.FileObject isNewPageContentModel(org.openide.filesystems.FileObject)

