#Signature file v4.1
#Version 1.48

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract interface java.io.Externalizable
intf java.io.Serializable
meth public abstract void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeExternal(java.io.ObjectOutput) throws java.io.IOException

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface javax.swing.Action
fld public final static java.lang.String ACCELERATOR_KEY = "AcceleratorKey"
fld public final static java.lang.String ACTION_COMMAND_KEY = "ActionCommandKey"
fld public final static java.lang.String DEFAULT = "Default"
fld public final static java.lang.String DISPLAYED_MNEMONIC_INDEX_KEY = "SwingDisplayedMnemonicIndexKey"
fld public final static java.lang.String LARGE_ICON_KEY = "SwingLargeIconKey"
fld public final static java.lang.String LONG_DESCRIPTION = "LongDescription"
fld public final static java.lang.String MNEMONIC_KEY = "MnemonicKey"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String SELECTED_KEY = "SwingSelectedKey"
fld public final static java.lang.String SHORT_DESCRIPTION = "ShortDescription"
fld public final static java.lang.String SMALL_ICON = "SmallIcon"
intf java.awt.event.ActionListener
meth public abstract boolean isEnabled()
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void putValue(java.lang.String,java.lang.Object)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setEnabled(boolean)

CLSS public abstract interface org.netbeans.api.java.source.Task<%0 extends java.lang.Object>
meth public abstract void run({org.netbeans.api.java.source.Task%0}) throws java.lang.Exception

CLSS public abstract org.netbeans.modules.profiler.api.java.SourceClassInfo
cons public init(java.lang.String,java.lang.String,java.lang.String)
fld public final static java.util.Comparator<org.netbeans.modules.profiler.api.java.SourceClassInfo> COMPARATOR
meth protected final boolean isAnonymous(java.lang.String)
meth public abstract java.util.Set<org.netbeans.modules.profiler.api.java.SourceClassInfo> getInnerClases()
meth public abstract java.util.Set<org.netbeans.modules.profiler.api.java.SourceClassInfo> getInterfaces()
meth public abstract java.util.Set<org.netbeans.modules.profiler.api.java.SourceClassInfo> getSubclasses()
meth public abstract java.util.Set<org.netbeans.modules.profiler.api.java.SourceMethodInfo> getConstructors()
meth public abstract java.util.Set<org.netbeans.modules.profiler.api.java.SourceMethodInfo> getMethods(boolean)
meth public abstract org.netbeans.modules.profiler.api.java.SourceClassInfo getSuperType()
meth public abstract org.openide.filesystems.FileObject getFile()
meth public boolean equals(java.lang.Object)
meth public boolean isAnonymous()
meth public final java.lang.String getQualifiedName()
meth public final java.lang.String getSimpleName()
meth public final java.lang.String getVMName()
meth public int hashCode()
supr java.lang.Object
hfds anonymousInnerClassPattern,qualName,simpleName,vmName

CLSS public org.netbeans.modules.profiler.api.java.SourceMethodInfo
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean,int)
meth public boolean equals(java.lang.Object)
meth public final boolean isExecutable()
meth public final int getModifiers()
meth public final java.lang.String getClassName()
meth public final java.lang.String getName()
meth public final java.lang.String getSignature()
meth public final java.lang.String getVMName()
meth public int hashCode()
supr java.lang.Object
hfds className,execFlag,modifiers,name,signature,vmName

CLSS public abstract org.netbeans.modules.profiler.api.java.SourcePackageInfo
cons public init(java.lang.String,java.lang.String,org.netbeans.modules.profiler.api.java.SourcePackageInfo$Scope)
innr public final static !enum Scope
meth public abstract java.util.Collection<org.netbeans.modules.profiler.api.java.SourceClassInfo> getClasses()
meth public abstract java.util.Collection<org.netbeans.modules.profiler.api.java.SourcePackageInfo> getSubpackages()
meth public java.lang.String getBinaryName()
meth public java.lang.String getSimpleName()
meth public org.netbeans.modules.profiler.api.java.SourcePackageInfo$Scope getScope()
supr java.lang.Object
hfds fqn,scope,simpleName

CLSS public org.netbeans.modules.profiler.nbimpl.actions.AntActions
cons public init()
meth public static javax.swing.Action attachMainProjectAction()
meth public static javax.swing.Action profileMainProjectAction()
meth public static javax.swing.Action profileOsgi()
meth public static javax.swing.Action profileProjectPopup()
meth public static javax.swing.Action profileSingle()
meth public static javax.swing.Action profileTest()
supr java.lang.Object
hfds mainProjectA

CLSS public org.netbeans.modules.profiler.nbimpl.actions.FileSensitivePerformer
cons public init(java.lang.String)
intf org.netbeans.spi.project.ui.support.FileActionPerformer
meth public boolean enable(org.openide.filesystems.FileObject)
meth public void perform(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds command

CLSS public final org.netbeans.modules.profiler.nbimpl.actions.ProfileClassEditorAction
cons public init()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction

CLSS public final org.netbeans.modules.profiler.nbimpl.actions.ProfileElementNavigatorAction
cons public init()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction
hfds name

CLSS public final org.netbeans.modules.profiler.nbimpl.actions.ProfileMethodEditorAction
cons public init()
meth protected boolean enable(org.openide.nodes.Node[])
meth protected void performAction(org.openide.nodes.Node[])
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr org.openide.util.actions.NodeAction

CLSS public org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher
cons public init()
innr public abstract interface static Launcher
innr public abstract interface static LauncherFactory
innr public final static AntLauncherFactory
innr public final static Command
innr public final static Session
innr public final static SessionProvider
meth public static boolean canRelaunch()
meth public static org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher$Session getLastSession()
meth public static org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher$Session newSession(java.lang.String,org.openide.util.Lookup)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public static void clearLastSession()
supr java.lang.Object
hfds AGENT_ARGS,ARGS_PREFIX,LINUX_THREAD_TIMER_KEY,LOG,lastSession
hcls AntLauncher,ProfilerSessionImpl

CLSS public final static org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher$AntLauncherFactory
 outer org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher
cons public init(org.netbeans.api.project.Project)
intf org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher$LauncherFactory
meth public org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher$Launcher createLauncher(org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher$Session)
supr java.lang.Object
hfds prj

CLSS public final static org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher$Command
 outer org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher
cons public init(java.lang.String)
supr java.lang.Object
hfds command

CLSS public abstract interface static org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher$Launcher
 outer org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher
meth public abstract void launch(boolean)

CLSS public abstract interface static org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher$LauncherFactory
 outer org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher
meth public abstract org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher$Launcher createLauncher(org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher$Session)

CLSS public final static org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher$Session
 outer org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher
meth public boolean configure()
meth public boolean hasAttribute(java.lang.String)
meth public boolean isConfigured()
meth public java.lang.Object getAttribute(java.lang.String)
meth public java.lang.String getCommand()
meth public java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public org.netbeans.api.project.Project getProject()
meth public org.netbeans.lib.profiler.common.ProfilingSettings getProfilingSettings()
meth public org.netbeans.lib.profiler.common.SessionSettings getSessionSettings()
meth public org.netbeans.modules.profiler.api.JavaPlatform getPlatform()
meth public org.openide.filesystems.FileObject getFile()
meth public org.openide.util.Lookup getContext()
meth public static org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher$Session createSession(java.lang.String,org.openide.util.Lookup)
meth public static org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher$Session createSession(org.netbeans.api.project.Project)
meth public void run()
meth public void setAttribute(java.lang.String,java.lang.Object)
meth public void setProfilingSettings(org.netbeans.lib.profiler.common.ProfilingSettings)
meth public void setSessionSettings(org.netbeans.lib.profiler.common.SessionSettings)
supr java.lang.Object
hfds command,configured,context,customProps,fo,launcher,platform,project,props,ps,rerun,ss

CLSS public final static org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher$SessionProvider
 outer org.netbeans.modules.profiler.nbimpl.actions.ProfilerLauncher
cons public init()
meth public org.netbeans.modules.profiler.v2.ProfilerSession createSession(org.openide.util.Lookup)
supr org.netbeans.modules.profiler.v2.ProfilerSession$Provider

CLSS public org.netbeans.modules.profiler.nbimpl.actions.ProfilerToolbarDropdownAction
cons public init()
intf javax.swing.Action
intf org.openide.util.actions.Presenter$Toolbar
meth public boolean isEnabled()
meth public java.awt.Component getToolbarPresenter()
meth public java.lang.Object getValue(java.lang.String)
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void putValue(java.lang.String,java.lang.Object)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void setEnabled(boolean)
supr java.lang.Object
hfds defaultAction,toolbarPresenter

CLSS public org.netbeans.modules.profiler.nbimpl.actions.ProjectSensitivePerformer
intf org.netbeans.spi.project.ui.support.ProjectActionPerformer
meth public boolean enable(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.profiler.nbimpl.actions.ProjectSensitivePerformer attachProject()
meth public static org.netbeans.modules.profiler.nbimpl.actions.ProjectSensitivePerformer profileProject(java.lang.String)
meth public void perform(org.netbeans.api.project.Project)
supr java.lang.Object
hfds attach,command

CLSS public org.netbeans.modules.profiler.nbimpl.javac.ClasspathInfoFactory
cons public init()
meth public static org.netbeans.api.java.source.ClasspathInfo infoFor(org.netbeans.api.project.Project)
meth public static org.netbeans.api.java.source.ClasspathInfo infoFor(org.netbeans.api.project.Project,boolean)
meth public static org.netbeans.api.java.source.ClasspathInfo infoFor(org.netbeans.api.project.Project,boolean,boolean,boolean)
supr java.lang.Object

CLSS public org.netbeans.modules.profiler.nbimpl.javac.ElementUtilitiesEx
cons public init()
meth public static java.lang.String getBinaryName(javax.lang.model.element.ExecutableElement,org.netbeans.api.java.source.CompilationInfo)
meth public static java.util.Set<javax.lang.model.element.TypeElement> findImplementorsResolved(org.netbeans.api.java.source.ClasspathInfo,org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>)
meth public static java.util.Set<org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>> findImplementors(org.netbeans.api.java.source.ClasspathInfo,org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>)
meth public static javax.lang.model.element.ExecutableElement resolveMethodByName(org.netbeans.api.java.source.CompilationInfo,javax.lang.model.element.TypeElement,java.lang.String,java.lang.String)
meth public static javax.lang.model.element.TypeElement resolveClassByName(java.lang.String,org.netbeans.api.java.source.CompilationController,boolean)
meth public static org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement> resolveClassByName(java.lang.String,org.netbeans.api.java.source.ClasspathInfo,boolean)
meth public static org.netbeans.api.java.source.JavaSource getSources(org.netbeans.api.project.Project)
supr java.lang.Object
hfds LOG,VM_CONSTRUCTUR_SIG,VM_INITIALIZER_SIG

CLSS public org.netbeans.modules.profiler.nbimpl.javac.JavacClassInfo
cons public init(org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>,org.netbeans.api.java.source.ClasspathInfo)
cons public init(org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement>,org.netbeans.api.java.source.CompilationController)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.Set<org.netbeans.modules.profiler.api.java.SourceClassInfo> getInnerClases()
meth public java.util.Set<org.netbeans.modules.profiler.api.java.SourceClassInfo> getInterfaces()
meth public java.util.Set<org.netbeans.modules.profiler.api.java.SourceClassInfo> getSubclasses()
meth public java.util.Set<org.netbeans.modules.profiler.api.java.SourceMethodInfo> getConstructors()
meth public java.util.Set<org.netbeans.modules.profiler.api.java.SourceMethodInfo> getMethods(boolean)
meth public org.netbeans.modules.profiler.api.java.SourceClassInfo getSuperType()
meth public org.openide.filesystems.FileObject getFile()
supr org.netbeans.modules.profiler.api.java.SourceClassInfo
hfds LOG,cpInfo,handle,sourceRef,src

CLSS public org.netbeans.modules.profiler.nbimpl.javac.JavacMethodInfo
cons public init(javax.lang.model.element.ExecutableElement,org.netbeans.api.java.source.CompilationController)
meth public java.lang.String toString()
meth public javax.lang.model.element.ExecutableElement resolve(org.netbeans.api.java.source.CompilationController)
supr org.netbeans.modules.profiler.api.java.SourceMethodInfo
hfds handle

CLSS public org.netbeans.modules.profiler.nbimpl.javac.JavacPackageInfo
cons public init(org.netbeans.api.java.source.ClasspathInfo,org.netbeans.api.java.source.ClasspathInfo,java.lang.String,java.lang.String,org.netbeans.modules.profiler.api.java.SourcePackageInfo$Scope)
meth public java.util.Collection<org.netbeans.modules.profiler.api.java.SourceClassInfo> getClasses()
meth public java.util.Collection<org.netbeans.modules.profiler.api.java.SourcePackageInfo> getSubpackages()
supr org.netbeans.modules.profiler.api.java.SourcePackageInfo
hfds LOGGER,cpInfo,indexInfo,sScope

CLSS public org.netbeans.modules.profiler.nbimpl.javac.ParsingUtils
cons public init()
meth public static void invokeScanSensitiveTask(org.netbeans.api.java.source.ClasspathInfo,org.netbeans.modules.profiler.nbimpl.javac.ScanSensitiveTask<org.netbeans.api.java.source.CompilationController>)
meth public static void invokeScanSensitiveTask(org.netbeans.api.java.source.JavaSource,org.netbeans.modules.profiler.nbimpl.javac.ScanSensitiveTask<org.netbeans.api.java.source.CompilationController>)
supr java.lang.Object
hfds LOG

CLSS public abstract org.netbeans.modules.profiler.nbimpl.javac.ScanSensitiveTask<%0 extends java.lang.Object>
cons public init()
cons public init(boolean)
intf org.netbeans.api.java.source.Task<{org.netbeans.modules.profiler.nbimpl.javac.ScanSensitiveTask%0}>
meth public boolean shouldRetry()
meth public final boolean requiresUpToDate()
supr java.lang.Object
hfds uptodate

CLSS public final org.netbeans.modules.profiler.nbimpl.project.AntProjectSupport
fld public final static int TARGET_PROFILE = 1
fld public final static int TARGET_PROFILE_SINGLE = 2
fld public final static int TARGET_PROFILE_TEST = 3
fld public final static int TARGET_PROFILE_TEST_SINGLE = 4
meth public org.openide.filesystems.FileObject getProjectBuildScript()
meth public org.openide.filesystems.FileObject getProjectBuildScript(java.lang.String)
meth public static org.netbeans.modules.profiler.nbimpl.project.AntProjectSupport get(org.openide.util.Lookup$Provider)
meth public void configurePropertiesForProfiling(java.util.Map,org.openide.filesystems.FileObject)
supr java.lang.Object
hfds DEFAULT,provider

CLSS public abstract org.netbeans.modules.profiler.nbimpl.project.AntProjectSupportProvider
cons public init()
innr public abstract static Abstract
meth public abstract org.openide.filesystems.FileObject getProjectBuildScript()
meth public abstract org.openide.filesystems.FileObject getProjectBuildScript(java.lang.String)
meth public abstract void configurePropertiesForProfiling(java.util.Map<java.lang.String,java.lang.String>,org.openide.filesystems.FileObject)
supr java.lang.Object
hcls Basic

CLSS public abstract static org.netbeans.modules.profiler.nbimpl.project.AntProjectSupportProvider$Abstract
 outer org.netbeans.modules.profiler.nbimpl.project.AntProjectSupportProvider
cons protected init(org.netbeans.api.project.Project)
meth protected final org.netbeans.api.project.Project getProject()
meth public org.openide.filesystems.FileObject getProjectBuildScript()
meth public org.openide.filesystems.FileObject getProjectBuildScript(java.lang.String)
meth public void configurePropertiesForProfiling(java.util.Map<java.lang.String,java.lang.String>,org.openide.filesystems.FileObject)
supr org.netbeans.modules.profiler.nbimpl.project.AntProjectSupportProvider
hfds project

CLSS public org.netbeans.modules.profiler.nbimpl.project.JavaProjectContentsSupportProvider
cons public init(org.netbeans.api.project.Project)
meth public java.lang.String getInstrumentationFilter(boolean)
meth public org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[] getProfilingRoots(org.openide.filesystems.FileObject,boolean)
meth public void reset()
supr org.netbeans.modules.profiler.spi.project.ProjectContentsSupportProvider
hfds packages,project

CLSS public abstract org.netbeans.modules.profiler.nbimpl.project.JavaProjectProfilingSupportProvider
cons protected init(org.netbeans.api.project.Project)
meth protected abstract org.netbeans.modules.profiler.api.JavaPlatform resolveProjectJavaPlatform()
meth protected final org.netbeans.api.project.Project getProject()
meth protected final org.netbeans.modules.profiler.api.JavaPlatform getPlatformByName(java.lang.String)
meth public boolean areProfilingPointsSupported()
meth public boolean checkProjectCanBeProfiled(org.openide.filesystems.FileObject)
meth public boolean isAttachSupported()
meth public boolean isFileObjectSupported(org.openide.filesystems.FileObject)
meth public boolean isProfilingSupported()
meth public org.netbeans.modules.profiler.api.JavaPlatform getProjectJavaPlatform()
meth public void setupProjectSessionSettings(org.netbeans.lib.profiler.common.SessionSettings)
supr org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider$Basic
hfds project

CLSS public final org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static java.lang.String PROFILER_NAME_SPACE = "http://www.netbeans.org/ns/profiler/1"
innr public final static IntegrationUpdater
meth public static boolean hasAction(org.netbeans.api.project.Project,java.lang.String)
meth public static boolean isJavaProject(org.netbeans.api.project.Project)
meth public static boolean isProfilerIntegrated(org.netbeans.api.project.Project)
meth public static boolean isProjectTypeSupported(org.netbeans.api.project.Project)
meth public static boolean isProjectTypeSupportedForAttach(org.netbeans.api.project.Project)
meth public static java.lang.String getDefaultPackageClassNames(org.netbeans.api.project.Project)
meth public static java.lang.String getProjectBuildScript(org.netbeans.api.project.Project)
meth public static java.lang.String getProjectBuildScript(org.netbeans.api.project.Project,java.lang.String)
meth public static java.lang.String getProjectName(org.netbeans.api.project.Project)
meth public static java.lang.String selectMainClass(org.netbeans.api.project.Project,java.lang.String,java.lang.String,int)
meth public static java.lang.String[] getProjectPackages(org.netbeans.api.project.Project)
meth public static java.net.URL copyAppletHTML(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.openide.filesystems.FileObject,java.lang.String)
meth public static java.net.URL generateAppletHTML(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.PropertyEvaluator,org.openide.filesystems.FileObject)
meth public static javax.swing.Icon getProjectIcon(org.netbeans.api.project.Project)
meth public static org.netbeans.api.project.Project getMainProject()
meth public static org.netbeans.api.project.Project getProjectForBuildScript(java.lang.String)
meth public static org.netbeans.api.project.Project[] getOpenedProjects()
meth public static org.netbeans.api.project.Project[] getOpenedProjectsForAttach()
meth public static org.netbeans.api.project.Project[] getSortedProjects(org.netbeans.api.project.Project[])
meth public static org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[] getProjectDefaultRoots(org.netbeans.api.project.Project,java.lang.String[][])
meth public static org.openide.filesystems.FileObject getOrCreateBuildFolder(org.netbeans.api.project.Project,java.lang.String)
meth public static org.openide.filesystems.FileObject getRootOf(org.openide.filesystems.FileObject[],org.openide.filesystems.FileObject)
meth public static org.openide.filesystems.FileObject[] getSourceRoots(org.netbeans.api.project.Project)
meth public static org.openide.filesystems.FileObject[] getSourceRoots(org.netbeans.api.project.Project,boolean)
meth public static void computeProjectPackages(org.netbeans.api.project.Project,boolean,java.lang.String[][])
meth public static void fetchSubprojects(org.netbeans.api.project.Project,java.util.Set<org.netbeans.api.project.Project>)
meth public static void invokeAction(org.netbeans.api.project.Project,java.lang.String)
meth public static void unintegrateProfiler(org.netbeans.api.project.Project)
supr java.lang.Object
hfds LOGGER,PROFILER_INIT

CLSS public final static org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities$IntegrationUpdater
 outer org.netbeans.modules.profiler.nbimpl.project.ProjectUtilities
cons public init(org.netbeans.api.project.Project)
meth protected void projectClosed()
meth protected void projectOpened()
supr org.netbeans.spi.project.ui.ProjectOpenedHook
hfds prj

CLSS public abstract org.netbeans.modules.profiler.spi.project.ProjectContentsSupportProvider
cons public init()
meth public abstract java.lang.String getInstrumentationFilter(boolean)
meth public abstract org.netbeans.lib.profiler.client.ClientUtils$SourceCodeSelection[] getProfilingRoots(org.openide.filesystems.FileObject,boolean)
meth public abstract void reset()
supr java.lang.Object

CLSS public abstract org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider
cons public init()
innr public static Basic
meth public abstract boolean areProfilingPointsSupported()
meth public abstract boolean checkProjectCanBeProfiled(org.openide.filesystems.FileObject)
meth public abstract boolean isAttachSupported()
meth public abstract boolean isFileObjectSupported(org.openide.filesystems.FileObject)
meth public abstract boolean isProfilingSupported()
meth public abstract boolean startProfilingSession(org.openide.filesystems.FileObject,boolean)
meth public abstract org.netbeans.modules.profiler.api.JavaPlatform getProjectJavaPlatform()
meth public abstract void setupProjectSessionSettings(org.netbeans.lib.profiler.common.SessionSettings)
supr java.lang.Object

CLSS public static org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider$Basic
 outer org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider
cons public init()
meth public boolean areProfilingPointsSupported()
meth public boolean checkProjectCanBeProfiled(org.openide.filesystems.FileObject)
meth public boolean isAttachSupported()
meth public boolean isFileObjectSupported(org.openide.filesystems.FileObject)
meth public boolean isProfilingSupported()
meth public boolean startProfilingSession(org.openide.filesystems.FileObject,boolean)
meth public org.netbeans.modules.profiler.api.JavaPlatform getProjectJavaPlatform()
meth public void setupProjectSessionSettings(org.netbeans.lib.profiler.common.SessionSettings)
supr org.netbeans.modules.profiler.spi.project.ProjectProfilingSupportProvider

CLSS public abstract org.netbeans.modules.profiler.v2.ProfilerSession
cons protected init(org.netbeans.lib.profiler.common.Profiler,org.openide.util.Lookup)
innr public abstract static Provider
meth protected abstract boolean isCompatibleContext(org.openide.util.Lookup)
meth protected abstract boolean modify()
meth protected abstract boolean start()
meth protected abstract boolean stop()
meth protected final org.openide.util.Lookup getContext()
meth public abstract org.openide.filesystems.FileObject getFile()
meth public abstract org.openide.util.Lookup$Provider getProject()
meth public final boolean inProgress()
meth public final boolean isAttach()
meth public final int getState()
meth public final org.netbeans.lib.profiler.common.AttachSettings getAttachSettings()
meth public final org.netbeans.lib.profiler.common.Profiler getProfiler()
meth public final org.netbeans.lib.profiler.common.ProfilingSettings getProfilingSettings()
meth public final org.netbeans.modules.profiler.v2.SessionStorage getStorage()
meth public final void addListener(org.netbeans.lib.profiler.common.event.ProfilingStateListener)
meth public final void open()
meth public final void removeListener(org.netbeans.lib.profiler.common.event.ProfilingStateListener)
meth public final void setAttach(boolean)
meth public static org.netbeans.modules.profiler.v2.ProfilerSession currentSession()
meth public static org.netbeans.modules.profiler.v2.ProfilerSession forContext(org.openide.util.Lookup)
meth public static void findAndConfigure(org.openide.util.Lookup,org.openide.util.Lookup$Provider,java.lang.String)
supr java.lang.Object
hfds CURRENT_SESSION,CURRENT_SESSION_LOCK,attachSettings,context,features,isAttach,plugins,profiler,profilingSettings,profilingStateListeners,storage,window

CLSS public abstract static org.netbeans.modules.profiler.v2.ProfilerSession$Provider
 outer org.netbeans.modules.profiler.v2.ProfilerSession
cons public init()
meth public abstract org.netbeans.modules.profiler.v2.ProfilerSession createSession(org.openide.util.Lookup)
supr java.lang.Object

CLSS public abstract org.netbeans.spi.project.ui.ProjectOpenedHook
cons protected init()
meth protected abstract void projectClosed()
meth protected abstract void projectOpened()
supr java.lang.Object

CLSS public abstract interface org.netbeans.spi.project.ui.support.FileActionPerformer
meth public abstract boolean enable(org.openide.filesystems.FileObject)
meth public abstract void perform(org.openide.filesystems.FileObject)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.project.ui.support.ProjectActionPerformer
meth public abstract boolean enable(org.netbeans.api.project.Project)
meth public abstract void perform(org.netbeans.api.project.Project)

CLSS public abstract interface org.openide.util.ContextAwareAction
intf javax.swing.Action
meth public abstract javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)

CLSS public final org.openide.util.HelpCtx
cons public init(java.lang.Class<?>)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
cons public init(java.net.URL)
 anno 0 java.lang.Deprecated()
fld public final static org.openide.util.HelpCtx DEFAULT_HELP
innr public abstract interface static Displayer
innr public abstract interface static Provider
meth public boolean display()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getHelpID()
meth public java.lang.String toString()
meth public java.net.URL getHelp()
meth public static org.openide.util.HelpCtx findHelp(java.awt.Component)
meth public static org.openide.util.HelpCtx findHelp(java.lang.Object)
meth public static void setHelpIDString(javax.swing.JComponent,java.lang.String)
supr java.lang.Object
hfds err,helpCtx,helpID

CLSS public abstract interface static org.openide.util.HelpCtx$Provider
 outer org.openide.util.HelpCtx
meth public abstract org.openide.util.HelpCtx getHelpCtx()

CLSS public abstract org.openide.util.SharedClassObject
cons protected init()
intf java.io.Externalizable
meth protected boolean clearSharedData()
meth protected final java.lang.Object getLock()
meth protected final java.lang.Object getProperty(java.lang.Object)
meth protected final java.lang.Object putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void finalize() throws java.lang.Throwable
meth protected java.lang.Object writeReplace()
meth protected void addNotify()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void initialize()
meth protected void removeNotify()
meth protected void reset()
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static <%0 extends org.openide.util.SharedClassObject> {%%0} findObject(java.lang.Class<{%%0}>)
meth public static <%0 extends org.openide.util.SharedClassObject> {%%0} findObject(java.lang.Class<{%%0}>,boolean)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr java.lang.Object
hfds PROP_SUPPORT,addNotifySuper,alreadyWarnedAboutDupes,dataEntry,err,first,firstTrace,inReadExternal,initializeSuper,instancesBeingCreated,lock,prematureSystemOptionMutation,removeNotifySuper,serialVersionUID,systemOption,values,waitingOnSystemOption
hcls DataEntry,SetAccessibleAction,WriteReplace

CLSS public abstract org.openide.util.actions.CallableSystemAction
cons public init()
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean asynchronous()
meth public abstract void performAction()
meth public java.awt.Component getToolbarPresenter()
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.openide.util.actions.SystemAction
hfds DEFAULT_ASYNCH,serialVersionUID,warnedAsynchronousActions

CLSS public abstract org.openide.util.actions.NodeAction
cons public init()
intf org.openide.util.ContextAwareAction
meth protected abstract boolean enable(org.openide.nodes.Node[])
meth protected abstract void performAction(org.openide.nodes.Node[])
meth protected boolean surviveFocusChange()
meth protected void addNotify()
meth protected void initialize()
meth protected void removeNotify()
meth public boolean isEnabled()
meth public final org.openide.nodes.Node[] getActivatedNodes()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public void actionPerformed(java.awt.event.ActionEvent)
 anno 0 java.lang.Deprecated()
meth public void performAction()
 anno 0 java.lang.Deprecated()
meth public void setEnabled(boolean)
supr org.openide.util.actions.CallableSystemAction
hfds PROP_HAS_LISTENERS,PROP_LAST_ENABLED,PROP_LAST_NODES,l,listeningActions,serialVersionUID
hcls DelegateAction,NodesL

CLSS public abstract interface org.openide.util.actions.Presenter
innr public abstract interface static Menu
innr public abstract interface static Popup
innr public abstract interface static Toolbar

CLSS public abstract interface static org.openide.util.actions.Presenter$Menu
 outer org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter
meth public abstract javax.swing.JMenuItem getMenuPresenter()

CLSS public abstract interface static org.openide.util.actions.Presenter$Popup
 outer org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter
meth public abstract javax.swing.JMenuItem getPopupPresenter()

CLSS public abstract interface static org.openide.util.actions.Presenter$Toolbar
 outer org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter
meth public abstract java.awt.Component getToolbarPresenter()

CLSS public abstract org.openide.util.actions.SystemAction
cons public init()
fld public final static java.lang.String PROP_ENABLED = "enabled"
fld public final static java.lang.String PROP_ICON = "icon"
intf javax.swing.Action
intf org.openide.util.HelpCtx$Provider
meth protected boolean clearSharedData()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public abstract java.lang.String getName()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract void actionPerformed(java.awt.event.ActionEvent)
meth public boolean isEnabled()
meth public final java.lang.Object getValue(java.lang.String)
meth public final javax.swing.Icon getIcon()
meth public final javax.swing.Icon getIcon(boolean)
meth public final void putValue(java.lang.String,java.lang.Object)
meth public final void setIcon(javax.swing.Icon)
meth public static <%0 extends org.openide.util.actions.SystemAction> {%%0} get(java.lang.Class<{%%0}>)
meth public static javax.swing.JPopupMenu createPopupMenu(org.openide.util.actions.SystemAction[])
 anno 0 java.lang.Deprecated()
meth public static javax.swing.JToolBar createToolbarPresenter(org.openide.util.actions.SystemAction[])
meth public static org.openide.util.actions.SystemAction[] linkActions(org.openide.util.actions.SystemAction[],org.openide.util.actions.SystemAction[])
meth public void setEnabled(boolean)
supr org.openide.util.SharedClassObject
hfds LOG,PROP_ICON_TEXTUAL,relativeIconResourceClasses,serialVersionUID
hcls ComponentIcon

