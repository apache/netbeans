#Signature file v4.1
#Version 1.112.0

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

CLSS public org.netbeans.api.project.ui.ProjectProblems
meth public static boolean isBroken(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void showAlert(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public static void showCustomizer(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.project.ui.CustomizerProvider
meth public abstract void showCustomizer()

CLSS public abstract interface org.netbeans.spi.project.ui.CustomizerProvider2
intf org.netbeans.spi.project.ui.CustomizerProvider
meth public abstract void showCustomizer(java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.spi.project.ui.LogicalViewProvider
intf org.netbeans.spi.project.ui.PathFinder
meth public abstract org.openide.nodes.Node createLogicalView()

CLSS public abstract interface org.netbeans.spi.project.ui.PathFinder
meth public abstract org.openide.nodes.Node findPath(org.openide.nodes.Node,java.lang.Object)

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

CLSS public abstract interface org.netbeans.spi.project.ui.ProjectProblemsImplementation
meth public abstract java.util.concurrent.CompletableFuture<java.lang.Void> showAlert(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.concurrent.CompletableFuture<java.lang.Void> showCustomizer(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()

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

CLSS public final org.netbeans.spi.project.ui.support.BuildExecutionSupport
innr public abstract interface static ActionItem
innr public abstract interface static Item
meth public static org.netbeans.spi.project.ui.support.BuildExecutionSupport$Item getLastFinishedItem()
meth public static void addChangeListener(javax.swing.event.ChangeListener)
meth public static void registerFinishedItem(org.netbeans.spi.project.ui.support.BuildExecutionSupport$Item)
meth public static void registerRunningItem(org.netbeans.spi.project.ui.support.BuildExecutionSupport$Item)
meth public static void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.spi.project.ui.support.BuildExecutionSupport$ActionItem
 outer org.netbeans.spi.project.ui.support.BuildExecutionSupport
intf org.netbeans.spi.project.ui.support.BuildExecutionSupport$Item
meth public abstract java.lang.String getAction()
meth public abstract org.openide.filesystems.FileObject getProjectDirectory()

CLSS public abstract interface static org.netbeans.spi.project.ui.support.BuildExecutionSupport$Item
 outer org.netbeans.spi.project.ui.support.BuildExecutionSupport
meth public abstract boolean isRunning()
meth public abstract java.lang.String getDisplayName()
meth public abstract void repeatExecution()
meth public abstract void stopRunning()

CLSS public org.netbeans.spi.project.ui.support.CommonProjectActions
fld public final static java.lang.String EXISTING_SOURCES_FOLDER = "existingSourcesFolder"
fld public final static java.lang.String INITIAL_VALUE_PROPERTIES = "initialValueProperties"
fld public final static java.lang.String PRESELECT_CATEGORY = "PRESELECT_CATEGORY"
fld public final static java.lang.String PROJECT_PARENT_FOLDER = "projdir"
meth public static javax.swing.Action closeProjectAction()
meth public static javax.swing.Action copyProjectAction()
meth public static javax.swing.Action customizeProjectAction()
meth public static javax.swing.Action deleteProjectAction()
meth public static javax.swing.Action moveProjectAction()
meth public static javax.swing.Action newFileAction()
meth public static javax.swing.Action newProjectAction()
meth public static javax.swing.Action newProjectAction(java.lang.String,java.util.Map<java.lang.String,java.lang.Object>)
meth public static javax.swing.Action openSubprojectsAction()
meth public static javax.swing.Action renameProjectAction()
meth public static javax.swing.Action setAsMainProjectAction()
meth public static javax.swing.Action setProjectConfigurationAction()
meth public static javax.swing.Action[] forType(java.lang.String)
supr java.lang.Object

CLSS public final org.netbeans.spi.project.ui.support.DefaultProjectOperations
meth public static void performDefaultCopyOperation(org.netbeans.api.project.Project)
meth public static void performDefaultDeleteOperation(org.netbeans.api.project.Project)
meth public static void performDefaultMoveOperation(org.netbeans.api.project.Project)
meth public static void performDefaultRenameOperation(org.netbeans.api.project.Project,java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.project.ui.support.FileActionPerformer
meth public abstract boolean enable(org.openide.filesystems.FileObject)
meth public abstract void perform(org.openide.filesystems.FileObject)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public org.netbeans.spi.project.ui.support.FileSensitiveActions
meth public static javax.swing.Action fileCommandAction(java.lang.String,java.lang.String,javax.swing.Icon)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
meth public static javax.swing.Action fileSensitiveAction(org.netbeans.spi.project.ui.support.FileActionPerformer,java.lang.String,javax.swing.Icon)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object

CLSS public org.netbeans.spi.project.ui.support.MainProjectSensitiveActions
meth public static javax.swing.Action mainProjectCommandAction(java.lang.String,java.lang.String,javax.swing.Icon)
meth public static javax.swing.Action mainProjectSensitiveAction(org.netbeans.spi.project.ui.support.ProjectActionPerformer,java.lang.String,javax.swing.Icon)
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.project.ui.support.NodeFactory
innr public abstract interface static !annotation Registration
meth public abstract org.netbeans.spi.project.ui.support.NodeList<?> createNodes(org.netbeans.api.project.Project)

CLSS public abstract interface static !annotation org.netbeans.spi.project.ui.support.NodeFactory$Registration
 outer org.netbeans.spi.project.ui.support.NodeFactory
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.String parentPath()
meth public abstract java.lang.String[] projectType()

CLSS public org.netbeans.spi.project.ui.support.NodeFactorySupport
meth public !varargs static org.netbeans.spi.project.ui.support.NodeList<?> fixedNodeList(org.openide.nodes.Node[])
meth public static org.openide.nodes.Children createCompositeChildren(org.netbeans.api.project.Project,java.lang.String)
supr java.lang.Object
hfds LOADING_KEY,RP
hcls DelegateChildren,FixedNodeList,NodeListKeyWrapper

CLSS public abstract interface org.netbeans.spi.project.ui.support.NodeList<%0 extends java.lang.Object>
meth public abstract java.util.List<{org.netbeans.spi.project.ui.support.NodeList%0}> keys()
meth public abstract org.openide.nodes.Node node({org.netbeans.spi.project.ui.support.NodeList%0})
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void addNotify()
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeNotify()

CLSS public abstract interface org.netbeans.spi.project.ui.support.ProjectActionPerformer
meth public abstract boolean enable(org.netbeans.api.project.Project)
meth public abstract void perform(org.netbeans.api.project.Project)

CLSS public org.netbeans.spi.project.ui.support.ProjectChooser
meth public static java.io.File getProjectsFolder()
meth public static javax.swing.JFileChooser projectChooser()
meth public static void setProjectsFolder(java.io.File)
supr java.lang.Object

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

CLSS public final org.netbeans.spi.project.ui.support.ProjectCustomizer
innr public abstract interface static CategoryComponentProvider
innr public abstract interface static CompositeCategoryProvider
innr public final static Category
meth public static java.awt.Dialog createCustomizerDialog(java.lang.String,org.openide.util.Lookup,java.lang.String,java.awt.event.ActionListener,java.awt.event.ActionListener,org.openide.util.HelpCtx)
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.awt.Dialog createCustomizerDialog(java.lang.String,org.openide.util.Lookup,java.lang.String,java.awt.event.ActionListener,org.openide.util.HelpCtx)
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static java.awt.Dialog createCustomizerDialog(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category[],org.netbeans.spi.project.ui.support.ProjectCustomizer$CategoryComponentProvider,java.lang.String,java.awt.event.ActionListener,java.awt.event.ActionListener,org.openide.util.HelpCtx)
 anno 4 org.netbeans.api.annotations.common.NonNull()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public static java.awt.Dialog createCustomizerDialog(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category[],org.netbeans.spi.project.ui.support.ProjectCustomizer$CategoryComponentProvider,java.lang.String,java.awt.event.ActionListener,org.openide.util.HelpCtx)
 anno 4 org.netbeans.api.annotations.common.NonNull()
meth public static javax.swing.ComboBoxModel encodingModel(java.lang.String)
meth public static javax.swing.ListCellRenderer encodingRenderer()
supr java.lang.Object
hfds LOG
hcls DelegateCategoryProvider,EncodingModel,EncodingRenderer

CLSS public final static org.netbeans.spi.project.ui.support.ProjectCustomizer$Category
 outer org.netbeans.spi.project.ui.support.ProjectCustomizer
meth public !varargs static org.netbeans.spi.project.ui.support.ProjectCustomizer$Category create(java.lang.String,java.lang.String,java.awt.Image,org.netbeans.spi.project.ui.support.ProjectCustomizer$Category[])
meth public boolean isValid()
meth public java.awt.Image getIcon()
meth public java.awt.event.ActionListener getCloseListener()
meth public java.awt.event.ActionListener getOkButtonListener()
meth public java.awt.event.ActionListener getStoreListener()
meth public java.lang.String getDisplayName()
meth public java.lang.String getErrorMessage()
meth public java.lang.String getName()
meth public org.netbeans.spi.project.ui.support.ProjectCustomizer$Category[] getSubcategories()
meth public void setCloseListener(java.awt.event.ActionListener)
meth public void setErrorMessage(java.lang.String)
meth public void setOkButtonListener(java.awt.event.ActionListener)
meth public void setStoreListener(java.awt.event.ActionListener)
meth public void setValid(boolean)
supr java.lang.Object
hfds closeListener,displayName,errorMessage,icon,name,okListener,storeListener,subcategories,valid

CLSS public abstract interface static org.netbeans.spi.project.ui.support.ProjectCustomizer$CategoryComponentProvider
 outer org.netbeans.spi.project.ui.support.ProjectCustomizer
meth public abstract javax.swing.JComponent create(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category)

CLSS public abstract interface static org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider
 outer org.netbeans.spi.project.ui.support.ProjectCustomizer
innr public abstract interface static !annotation Registration
innr public abstract interface static !annotation Registrations
meth public abstract javax.swing.JComponent createComponent(org.netbeans.spi.project.ui.support.ProjectCustomizer$Category,org.openide.util.Lookup)
meth public abstract org.netbeans.spi.project.ui.support.ProjectCustomizer$Category createCategory(org.openide.util.Lookup)

CLSS public abstract interface static !annotation org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider$Registration
 outer org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, PACKAGE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.String category()
meth public abstract !hasdefault java.lang.String categoryLabel()
meth public abstract java.lang.String projectType()

CLSS public abstract interface static !annotation org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider$Registrations
 outer org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD, PACKAGE])
intf java.lang.annotation.Annotation
meth public abstract org.netbeans.spi.project.ui.support.ProjectCustomizer$CompositeCategoryProvider$Registration[] value()

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

CLSS public org.netbeans.spi.project.ui.support.ProjectSensitiveActions
meth public static javax.swing.Action projectCommandAction(java.lang.String,java.lang.String,javax.swing.Icon)
meth public static javax.swing.Action projectSensitiveAction(org.netbeans.spi.project.ui.support.ProjectActionPerformer,java.lang.String,javax.swing.Icon)
supr java.lang.Object

CLSS public final org.netbeans.spi.project.ui.support.UILookupMergerSupport
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.project.ui.PrivilegedTemplates> createPrivilegedTemplatesMerger()
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.project.ui.ProjectOpenedHook> createProjectOpenHookMerger(org.netbeans.spi.project.ui.ProjectOpenedHook)
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.project.ui.ProjectProblemsProvider> createProjectProblemsProviderMerger()
meth public static org.netbeans.spi.project.LookupMerger<org.netbeans.spi.project.ui.RecommendedTemplates> createRecommendedTemplatesMerger()
supr java.lang.Object
hcls OpenHookImpl,OpenMerger,PrivilegedMerger,PrivilegedTemplatesImpl,ProjectProblemsProviderImpl,ProjectProblemsProviderMerger,RecommendedMerger,RecommendedTemplatesImpl

CLSS public org.netbeans.spi.project.ui.templates.support.Templates
innr public final static SimpleTargetChooserBuilder
meth public static boolean getDefinesMainProject(org.openide.WizardDescriptor)
meth public static java.lang.String getTargetName(org.openide.WizardDescriptor)
meth public static org.netbeans.api.project.Project getProject(org.openide.WizardDescriptor)
meth public static org.netbeans.spi.project.ui.templates.support.Templates$SimpleTargetChooserBuilder buildSimpleTargetChooser(org.netbeans.api.project.Project,org.netbeans.api.project.SourceGroup[])
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor> createSimpleTargetChooser(org.netbeans.api.project.Project,org.netbeans.api.project.SourceGroup[])
 anno 0 java.lang.Deprecated()
meth public static org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor> createSimpleTargetChooser(org.netbeans.api.project.Project,org.netbeans.api.project.SourceGroup[],org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>)
 anno 0 java.lang.Deprecated()
meth public static org.openide.filesystems.FileObject getExistingSourcesFolder(org.openide.WizardDescriptor)
meth public static org.openide.filesystems.FileObject getTargetFolder(org.openide.WizardDescriptor)
meth public static org.openide.filesystems.FileObject getTemplate(org.openide.WizardDescriptor)
meth public static void setDefinesMainProject(org.openide.WizardDescriptor,boolean)
 anno 0 java.lang.Deprecated()
meth public static void setTargetFolder(org.openide.WizardDescriptor,org.openide.filesystems.FileObject)
meth public static void setTargetName(org.openide.WizardDescriptor,java.lang.String)
supr java.lang.Object
hfds SET_AS_MAIN

CLSS public final static org.netbeans.spi.project.ui.templates.support.Templates$SimpleTargetChooserBuilder
 outer org.netbeans.spi.project.ui.templates.support.Templates
meth public org.netbeans.spi.project.ui.templates.support.Templates$SimpleTargetChooserBuilder bottomPanel(org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor>)
meth public org.netbeans.spi.project.ui.templates.support.Templates$SimpleTargetChooserBuilder freeFileExtension()
meth public org.openide.WizardDescriptor$Panel<org.openide.WizardDescriptor> create()
supr java.lang.Object
hfds bottomPanel,folders,freeFileExtension,project

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

