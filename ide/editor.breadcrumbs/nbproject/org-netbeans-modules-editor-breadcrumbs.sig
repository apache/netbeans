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

CLSS public org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsController
fld public final static java.awt.Image NO_ICON
fld public final static java.lang.Class<? extends org.netbeans.modules.parsing.spi.Scheduler> BREADCRUMBS_SCHEDULER
meth public static boolean areBreadCrumsEnabled(javax.swing.text.Document)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.editor.SideBarFactory createSideBarFactory()
meth public static void addBreadCrumbsEnabledListener(javax.swing.event.ChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void setBreadcrumbs(javax.swing.text.Document,org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsElement)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static void setBreadcrumbs(javax.swing.text.Document,org.openide.nodes.Node,org.openide.nodes.Node)
 anno 0 java.lang.Deprecated()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds WORKER

CLSS public abstract interface org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsElement
meth public abstract java.awt.Image getIcon(int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.awt.Image getOpenedIcon(int)
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getHtmlDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.List<org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsElement> getChildren()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.netbeans.modules.editor.breadcrumbs.spi.BreadcrumbsElement getParent()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract org.openide.util.Lookup getLookup()
 anno 0 org.netbeans.api.annotations.common.NonNull()

