#Signature file v4.1
#Version 1.109.0

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected init(java.lang.String,int)
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} valueOf(java.lang.Class<{%%0}>,java.lang.String)
supr java.lang.Object

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

CLSS public abstract interface java.lang.annotation.Annotation
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.Class<? extends java.lang.annotation.Annotation> annotationType()
meth public abstract java.lang.String toString()

CLSS public abstract interface !annotation java.lang.annotation.Documented
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation java.lang.annotation.Retention
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.RetentionPolicy value()

CLSS public abstract interface !annotation java.lang.annotation.Target
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.annotation.ElementType[] value()

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public final org.netbeans.api.project.ui.OpenProjects
fld public final static java.lang.String PROPERTY_MAIN_PROJECT = "MainProject"
fld public final static java.lang.String PROPERTY_OPEN_PROJECTS = "openProjects"
meth public boolean isProjectOpen(org.netbeans.api.project.Project)
meth public java.util.concurrent.Future<org.netbeans.api.project.Project[]> openProjects()
meth public org.netbeans.api.project.Project getMainProject()
meth public org.netbeans.api.project.Project[] getOpenProjects()
meth public org.netbeans.api.project.ui.ProjectGroup getActiveProjectGroup()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.openide.explorer.ExplorerManager createLogicalView()
meth public org.openide.explorer.ExplorerManager createPhysicalView()
meth public static org.netbeans.api.project.ui.OpenProjects getDefault()
meth public void addProjectGroupChangeListener(org.netbeans.api.project.ui.ProjectGroupChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void close(org.netbeans.api.project.Project[])
meth public void open(org.netbeans.api.project.Project[],boolean)
meth public void open(org.netbeans.api.project.Project[],boolean,boolean)
meth public void removeProjectGroupChangeListener(org.netbeans.api.project.ui.ProjectGroupChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setMainProject(org.netbeans.api.project.Project)
supr java.lang.Object
hfds LOG,instances,trampoline

CLSS public final org.netbeans.api.project.ui.ProjectGroup
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.util.prefs.Preferences preferencesForPackage(java.lang.Class)
supr java.lang.Object
hfds name,prefs
hcls AccessorImpl

CLSS public final org.netbeans.api.project.ui.ProjectGroupChangeEvent
cons public init(org.netbeans.api.project.ui.ProjectGroup,org.netbeans.api.project.ui.ProjectGroup)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public org.netbeans.api.project.ui.ProjectGroup getNewGroup()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.api.project.ui.ProjectGroup getOldGroup()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.util.EventObject
hfds newGroup,oldGroup

CLSS public abstract interface org.netbeans.api.project.ui.ProjectGroupChangeListener
intf java.util.EventListener
meth public abstract void projectGroupChanged(org.netbeans.api.project.ui.ProjectGroupChangeEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void projectGroupChanging(org.netbeans.api.project.ui.ProjectGroupChangeEvent)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.project.ui.PrivilegedTemplates
meth public abstract java.lang.String[] getPrivilegedTemplates()

CLSS public abstract interface org.netbeans.spi.project.ui.ProjectConvertor
innr public abstract interface static !annotation Registration
innr public final static Result
meth public abstract org.netbeans.spi.project.ui.ProjectConvertor$Result isProject(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface static !annotation org.netbeans.spi.project.ui.ProjectConvertor$Registration
 outer org.netbeans.spi.project.ui.ProjectConvertor
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract java.lang.String requiredPattern()

CLSS public final static org.netbeans.spi.project.ui.ProjectConvertor$Result
 outer org.netbeans.spi.project.ui.ProjectConvertor
cons public init(org.openide.util.Lookup,java.util.concurrent.Callable<? extends org.netbeans.api.project.Project>,java.lang.String,javax.swing.Icon)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
intf org.openide.util.Lookup$Provider
meth public java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public javax.swing.Icon getIcon()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.api.project.Project createProject() throws java.io.IOException
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.openide.util.Lookup getLookup()
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds displayName,icon,lkp,projectFactory

CLSS public abstract org.netbeans.spi.project.ui.ProjectOpenedHook
cons protected init()
meth protected abstract void projectClosed()
meth protected abstract void projectOpened()
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.project.ui.ProjectProblemResolver
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.util.concurrent.Future<org.netbeans.spi.project.ui.ProjectProblemsProvider$Result> resolve()

CLSS public abstract interface org.netbeans.spi.project.ui.ProjectProblemsProvider
fld public final static java.lang.String PROP_PROBLEMS = "problems"
innr public final static !enum Severity
innr public final static !enum Status
innr public final static ProjectProblem
innr public final static Result
meth public abstract java.util.Collection<? extends org.netbeans.spi.project.ui.ProjectProblemsProvider$ProjectProblem> getProblems()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.spi.project.ui.ProjectProblemsProvider$ProjectProblem
 outer org.netbeans.spi.project.ui.ProjectProblemsProvider
meth public boolean equals(java.lang.Object)
meth public boolean isResolvable()
meth public int hashCode()
meth public java.lang.String getDescription()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String toString()
meth public java.util.concurrent.Future<org.netbeans.spi.project.ui.ProjectProblemsProvider$Result> resolve()
meth public org.netbeans.spi.project.ui.ProjectProblemsProvider$Severity getSeverity()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.project.ui.ProjectProblemsProvider$ProjectProblem createError(java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.project.ui.ProjectProblemsProvider$ProjectProblem createError(java.lang.String,java.lang.String,org.netbeans.spi.project.ui.ProjectProblemResolver)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.project.ui.ProjectProblemsProvider$ProjectProblem createWarning(java.lang.String,java.lang.String)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.project.ui.ProjectProblemsProvider$ProjectProblem createWarning(java.lang.String,java.lang.String,org.netbeans.spi.project.ui.ProjectProblemResolver)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds description,displayName,resolver,severity

CLSS public final static org.netbeans.spi.project.ui.ProjectProblemsProvider$Result
 outer org.netbeans.spi.project.ui.ProjectProblemsProvider
meth public boolean isResolved()
meth public java.lang.String getMessage()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public org.netbeans.spi.project.ui.ProjectProblemsProvider$Status getStatus()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.project.ui.ProjectProblemsProvider$Result create(org.netbeans.spi.project.ui.ProjectProblemsProvider$Status)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.project.ui.ProjectProblemsProvider$Result create(org.netbeans.spi.project.ui.ProjectProblemsProvider$Status,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds message,status

CLSS public final static !enum org.netbeans.spi.project.ui.ProjectProblemsProvider$Severity
 outer org.netbeans.spi.project.ui.ProjectProblemsProvider
fld public final static org.netbeans.spi.project.ui.ProjectProblemsProvider$Severity ERROR
fld public final static org.netbeans.spi.project.ui.ProjectProblemsProvider$Severity WARNING
meth public static org.netbeans.spi.project.ui.ProjectProblemsProvider$Severity valueOf(java.lang.String)
meth public static org.netbeans.spi.project.ui.ProjectProblemsProvider$Severity[] values()
supr java.lang.Enum<org.netbeans.spi.project.ui.ProjectProblemsProvider$Severity>

CLSS public final static !enum org.netbeans.spi.project.ui.ProjectProblemsProvider$Status
 outer org.netbeans.spi.project.ui.ProjectProblemsProvider
fld public final static org.netbeans.spi.project.ui.ProjectProblemsProvider$Status RESOLVED
fld public final static org.netbeans.spi.project.ui.ProjectProblemsProvider$Status RESOLVED_WITH_WARNING
fld public final static org.netbeans.spi.project.ui.ProjectProblemsProvider$Status UNRESOLVED
meth public static org.netbeans.spi.project.ui.ProjectProblemsProvider$Status valueOf(java.lang.String)
meth public static org.netbeans.spi.project.ui.ProjectProblemsProvider$Status[] values()
supr java.lang.Enum<org.netbeans.spi.project.ui.ProjectProblemsProvider$Status>

CLSS public abstract interface org.netbeans.spi.project.ui.RecommendedTemplates
meth public abstract java.lang.String[] getRecommendedTypes()

CLSS public final org.netbeans.spi.project.ui.support.ProjectConvertors
meth public !varargs static org.openide.util.Lookup createProjectConvertorLookup(java.lang.Object[])
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static boolean isConvertorProject(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.api.project.Project getNonConvertorOwner(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.spi.queries.FileEncodingQueryImplementation createFileEncodingQuery()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.openide.util.Lookup createDelegateToOwnerLookup(org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void unregisterConvertorProject(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hcls CloseableLookup,ConvertorFileEncodingQuery,OwnerLookup

CLSS public final org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport
cons public init(java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NonNull()
innr public abstract interface static ProblemsCollector
meth public java.util.Collection<? extends org.netbeans.spi.project.ui.ProjectProblemsProvider$ProjectProblem> getProblems(org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport$ProblemsCollector)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void fireProblemsChange()
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds changeId,problems,problemsLock,propertyChangeSupport

CLSS public abstract interface static org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport$ProblemsCollector
 outer org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport
meth public abstract java.util.Collection<? extends org.netbeans.spi.project.ui.ProjectProblemsProvider$ProjectProblem> collectProblems()
 anno 0 org.netbeans.api.annotations.common.NonNull()

CLSS public final org.netbeans.spi.project.ui.support.UILookupMergerSupport
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.project.ui.PrivilegedTemplates> createPrivilegedTemplatesMerger()
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.project.ui.ProjectOpenedHook> createProjectOpenHookMerger(org.netbeans.spi.project.ui.ProjectOpenedHook)
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.project.ui.ProjectProblemsProvider> createProjectProblemsProviderMerger()
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.project.ui.RecommendedTemplates> createRecommendedTemplatesMerger()
supr java.lang.Object
hcls OpenHookImpl,OpenMerger,PrivilegedMerger,PrivilegedTemplatesImpl,ProjectProblemsProviderImpl,ProjectProblemsProviderMerger,RecommendedMerger,RecommendedTemplatesImpl

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

