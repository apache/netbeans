#Signature file v4.1
#Version 1.34

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface java.io.Serializable

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

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

CLSS public java.lang.Throwable
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
intf java.io.Serializable
meth public final java.lang.Throwable[] getSuppressed()
meth public final void addSuppressed(java.lang.Throwable)
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable fillInStackTrace()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
meth public void setStackTrace(java.lang.StackTraceElement[])
supr java.lang.Object

CLSS public org.netbeans.modules.hudson.ui.api.HudsonSCMHelper
meth public static java.lang.String xpath(java.lang.String,org.w3c.dom.Element)
 anno 0 java.lang.Deprecated()
meth public static void addTrigger(org.w3c.dom.Document)
meth public static void noteWillShowDiff(java.lang.String)
meth public static void showDiff(org.netbeans.api.diff.StreamSource,org.netbeans.api.diff.StreamSource,java.lang.String)
supr java.lang.Object
hfds LOG
hcls DiffTopComponent

CLSS public org.netbeans.modules.hudson.ui.api.UI
meth public !varargs static void selectNode(java.lang.String[])
meth public static javax.swing.Action showChangesAction(org.netbeans.modules.hudson.api.HudsonJobBuild)
meth public static javax.swing.Action showConsoleAction(org.netbeans.modules.hudson.api.HudsonJobBuild)
meth public static javax.swing.Action showConsoleAction(org.netbeans.modules.hudson.api.HudsonMavenModuleBuild)
meth public static org.openide.util.ContextAwareAction showFailuresAction()
supr java.lang.Object
hfds LOG

CLSS public abstract interface org.netbeans.modules.hudson.ui.spi.ProjectHudsonJobCreatorFactory
innr public abstract interface static ProjectHudsonJobCreator
innr public final static Helper
meth public abstract org.netbeans.modules.hudson.ui.spi.ProjectHudsonJobCreatorFactory$ProjectHudsonJobCreator forProject(org.netbeans.api.project.Project)

CLSS public final static org.netbeans.modules.hudson.ui.spi.ProjectHudsonJobCreatorFactory$Helper
 outer org.netbeans.modules.hudson.ui.spi.ProjectHudsonJobCreatorFactory
meth public static org.netbeans.modules.hudson.spi.HudsonSCM$Configuration prepareSCM(java.io.File)
meth public static org.netbeans.modules.hudson.spi.HudsonSCM$ConfigurationStatus noSCMError()
meth public static void addLogRotator(org.w3c.dom.Document)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.modules.hudson.ui.spi.ProjectHudsonJobCreatorFactory$ProjectHudsonJobCreator
 outer org.netbeans.modules.hudson.ui.spi.ProjectHudsonJobCreatorFactory
innr public static SilentIOException
meth public abstract java.lang.String jobName()
meth public abstract javax.swing.JComponent customizer()
meth public abstract org.netbeans.modules.hudson.spi.HudsonSCM$ConfigurationStatus status()
meth public abstract org.w3c.dom.Document configure() throws java.io.IOException
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)

CLSS public static org.netbeans.modules.hudson.ui.spi.ProjectHudsonJobCreatorFactory$ProjectHudsonJobCreator$SilentIOException
 outer org.netbeans.modules.hudson.ui.spi.ProjectHudsonJobCreatorFactory$ProjectHudsonJobCreator
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.io.IOException

CLSS public abstract org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider
cons public init()
innr public final static Association
meth public abstract boolean recordAssociation(org.netbeans.api.project.Project,org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider$Association)
meth public abstract org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider$Association findAssociation(org.netbeans.api.project.Project)
meth public org.netbeans.api.project.Project findAssociatedProject(org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider$Association)
meth public static org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider getDefault()
supr java.lang.Object

CLSS public final static org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider$Association
 outer org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getJobName()
meth public java.lang.String getServerUrl()
meth public java.lang.String getViewName()
meth public java.lang.String toString()
meth public org.netbeans.modules.hudson.api.HudsonJob getJob()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider$Association forJob(org.netbeans.modules.hudson.api.HudsonJob)
meth public static org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider$Association fromString(java.lang.String)
supr java.lang.Object
hfds URL_PATTERN,jobPath,jobURL,viewName

