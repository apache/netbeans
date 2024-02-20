#Signature file v4.1
#Version 1.56

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

CLSS public abstract org.netbeans.modules.profiler.projectsupport.AbstractProjectLookupProvider
cons public init()
intf org.netbeans.spi.project.LookupProvider
meth protected abstract java.util.List getAdditionalLookups(org.netbeans.api.project.Project)
meth public org.openide.util.Lookup createAdditionalLookup(org.openide.util.Lookup)
supr java.lang.Object

CLSS public org.netbeans.modules.profiler.projectsupport.utilities.AppletSupport
fld public static java.lang.Boolean unitTestingSupport_isApplet
meth public static java.net.URL generateHtmlFileURL(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,org.openide.filesystems.FileObject,java.lang.String) throws org.openide.filesystems.FileStateInvalidException
meth public static org.openide.filesystems.FileObject generateSecurityPolicy(org.openide.filesystems.FileObject,org.openide.filesystems.FileObject)
supr java.lang.Object
hfds CLASS_EXT,HTML_EXT,JDK_15,POLICY_FILE_EXT,POLICY_FILE_NAME

CLSS public org.netbeans.modules.profiler.projectsupport.utilities.ProjectUtilities
cons public init()
meth public static boolean hasAction(org.netbeans.api.project.Project,java.lang.String)
meth public static boolean hasSubprojects(org.netbeans.api.project.Project)
meth public static boolean isJavaProject(org.netbeans.api.project.Project)
meth public static java.lang.String computeProjectOnlyInstrumentationFilter(org.netbeans.api.project.Project,boolean,java.lang.String[][])
meth public static java.lang.String getDefaultPackageClassNames(org.netbeans.api.project.Project)
meth public static java.lang.String getProjectBuildScript(org.netbeans.api.project.Project)
meth public static java.lang.String getProjectName(org.openide.util.Lookup$Provider)
meth public static java.util.Properties getProjectProperties(org.netbeans.api.project.Project)
meth public static javax.swing.Icon getProjectIcon(org.openide.util.Lookup$Provider)
meth public static org.netbeans.api.java.source.JavaSource getSources(org.netbeans.api.project.Project)
meth public static org.netbeans.api.project.Project getMainProject()
meth public static org.netbeans.api.project.Project getProjectForBuildScript(java.lang.String)
meth public static org.netbeans.api.project.Project[] getOpenedProjects()
meth public static org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[] getProjectDefaultRoots(org.netbeans.api.project.Project,java.lang.String[][])
meth public static org.openide.filesystems.FileObject findTestForFile(org.openide.filesystems.FileObject)
meth public static org.openide.filesystems.FileObject getOrCreateBuildFolder(org.netbeans.api.project.Project,java.lang.String)
meth public static org.openide.filesystems.FileObject[] getSourceRoots(org.openide.util.Lookup$Provider)
meth public static org.openide.filesystems.FileObject[] getSourceRoots(org.openide.util.Lookup$Provider,boolean)
meth public static void computeProjectPackages(org.netbeans.api.project.Project,boolean,java.lang.String[][])
meth public static void fetchSubprojects(org.netbeans.api.project.Project,java.util.Set<org.netbeans.api.project.Project>)
meth public static void invokeAction(org.netbeans.api.project.Project,java.lang.String)
supr java.lang.Object
hfds LOGGER

CLSS public org.netbeans.modules.profiler.projectsupport.utilities.ProjectUtilitiesProviderImpl
cons public init()
meth public boolean hasSubprojects(org.openide.util.Lookup$Provider)
meth public java.lang.String getDisplayName(org.openide.util.Lookup$Provider)
meth public javax.swing.Icon getIcon(org.openide.util.Lookup$Provider)
meth public org.openide.filesystems.FileObject getProjectDirectory(org.openide.util.Lookup$Provider)
meth public org.openide.util.Lookup$Provider getMainProject()
meth public org.openide.util.Lookup$Provider getProject(org.openide.filesystems.FileObject)
meth public org.openide.util.Lookup$Provider[] getOpenedProjects()
meth public void addOpenProjectsListener(javax.swing.event.ChangeListener)
meth public void fetchSubprojects(org.openide.util.Lookup$Provider,java.util.Set<org.openide.util.Lookup$Provider>)
meth public void removeOpenProjectsListener(javax.swing.event.ChangeListener)
supr org.netbeans.modules.profiler.spi.ProjectUtilitiesProvider
hfds listeners

CLSS public abstract org.netbeans.modules.profiler.spi.ProjectUtilitiesProvider
cons public init()
meth public abstract boolean hasSubprojects(org.openide.util.Lookup$Provider)
meth public abstract java.lang.String getDisplayName(org.openide.util.Lookup$Provider)
meth public abstract javax.swing.Icon getIcon(org.openide.util.Lookup$Provider)
meth public abstract org.openide.filesystems.FileObject getProjectDirectory(org.openide.util.Lookup$Provider)
meth public abstract org.openide.util.Lookup$Provider getMainProject()
meth public abstract org.openide.util.Lookup$Provider getProject(org.openide.filesystems.FileObject)
meth public abstract org.openide.util.Lookup$Provider[] getOpenedProjects()
meth public abstract void addOpenProjectsListener(javax.swing.event.ChangeListener)
meth public abstract void fetchSubprojects(org.openide.util.Lookup$Provider,java.util.Set<org.openide.util.Lookup$Provider>)
meth public abstract void removeOpenProjectsListener(javax.swing.event.ChangeListener)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.project.LookupProvider
innr public abstract interface static !annotation Registration
meth public abstract org.openide.util.Lookup createAdditionalLookup(org.openide.util.Lookup)

