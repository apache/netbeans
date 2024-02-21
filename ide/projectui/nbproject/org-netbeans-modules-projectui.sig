#Signature file v4.1
#Version 1.82.0

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

CLSS public org.netbeans.modules.project.ui.api.ProjectActionUtils
cons public init()
meth public static void makeProjectTabVisible()
meth public static void selectAndExpandProject(org.netbeans.api.project.Project)
supr java.lang.Object

CLSS public org.netbeans.modules.project.ui.api.ProjectTemplates
fld public final static java.lang.String PRESELECT_CATEGORY = "PRESELECT_CATEGORY"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String PRESELECT_TEMPLATE = "PRESELECT_TEMPLATE"
supr java.lang.Object

CLSS public final org.netbeans.modules.project.ui.api.RecentProjects
fld public final static java.lang.String PROP_RECENT_PROJECT_INFO = "RecentProjectInformation"
meth public java.util.List<org.netbeans.modules.project.ui.api.UnloadedProjectInformation> getRecentProjectInformation()
meth public static org.netbeans.modules.project.ui.api.RecentProjects getDefault()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds INSTANCE,pch

CLSS public final org.netbeans.modules.project.ui.api.UnloadedProjectInformation
meth public java.lang.String getDisplayName()
meth public java.lang.String toString()
meth public java.net.URL getURL()
meth public javax.swing.Icon getIcon()
supr java.lang.Object
hfds displayName,icon,url

CLSS public abstract interface org.netbeans.modules.project.ui.spi.TemplateCategorySorter
meth public abstract java.util.List<org.openide.loaders.DataObject> sort(java.util.List<org.openide.loaders.DataObject>)

