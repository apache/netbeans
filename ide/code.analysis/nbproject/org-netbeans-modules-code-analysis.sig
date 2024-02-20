#Signature file v4.1
#Version 1.49

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

CLSS public org.netbeans.modules.analysis.api.CodeAnalysis
cons public init()
meth public static void open(org.netbeans.modules.analysis.spi.Analyzer$WarningDescription)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.analysis.spi.AnalysisScopeProvider
meth public abstract org.netbeans.modules.refactoring.api.Scope getScope()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()

CLSS public abstract interface org.netbeans.modules.analysis.spi.Analyzer
innr public abstract interface static CustomizerProvider
innr public abstract static AnalyzerFactory
innr public final static Context
innr public final static CustomizerContext
innr public final static MissingPlugin
innr public final static Result
innr public final static WarningDescription
intf org.openide.util.Cancellable
meth public abstract java.lang.Iterable<? extends org.netbeans.spi.editor.hints.ErrorDescription> analyze()

CLSS public abstract static org.netbeans.modules.analysis.spi.Analyzer$AnalyzerFactory
 outer org.netbeans.modules.analysis.spi.Analyzer
cons public init(java.lang.String,java.lang.String,java.awt.Image)
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public abstract <%0 extends java.lang.Object, %1 extends javax.swing.JComponent> org.netbeans.modules.analysis.spi.Analyzer$CustomizerProvider<{%%0},{%%1}> getCustomizerProvider()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public abstract java.lang.Iterable<? extends org.netbeans.modules.analysis.spi.Analyzer$WarningDescription> getWarnings()
meth public abstract org.netbeans.modules.analysis.spi.Analyzer createAnalyzer(org.netbeans.modules.analysis.spi.Analyzer$Context)
meth public java.util.Collection<? extends org.netbeans.modules.analysis.spi.Analyzer$MissingPlugin> requiredPlugins(org.netbeans.modules.analysis.spi.Analyzer$Context)
meth public org.netbeans.modules.analysis.spi.Analyzer createAnalyzer(org.netbeans.modules.analysis.spi.Analyzer$Context,org.netbeans.modules.analysis.spi.Analyzer$Result)
meth public void warningOpened(org.netbeans.spi.editor.hints.ErrorDescription)
supr java.lang.Object
hfds displayName,icon,iconPath,id

CLSS public final static org.netbeans.modules.analysis.spi.Analyzer$Context
 outer org.netbeans.modules.analysis.spi.Analyzer
meth public java.lang.String getSingleWarningId()
meth public java.util.prefs.Preferences getSettings()
meth public org.netbeans.modules.refactoring.api.Scope getScope()
meth public void finish()
meth public void progress(int)
meth public void progress(java.lang.String)
meth public void progress(java.lang.String,int)
meth public void reportAnalysisProblem(java.lang.String,java.lang.CharSequence)
meth public void start(int)
supr java.lang.Object
hfds bucketSize,bucketStart,problems,progress,scope,settings,singleWarningId,totalWork

CLSS public final static org.netbeans.modules.analysis.spi.Analyzer$CustomizerContext<%0 extends java.lang.Object, %1 extends javax.swing.JComponent>
 outer org.netbeans.modules.analysis.spi.Analyzer
cons public init(java.util.prefs.Preferences,java.lang.String,{org.netbeans.modules.analysis.spi.Analyzer$CustomizerContext%1},{org.netbeans.modules.analysis.spi.Analyzer$CustomizerContext%0},org.netbeans.modules.analysis.ui.AdjustConfigurationPanel$ErrorListener)
meth public java.lang.String getPreselectId()
meth public java.util.prefs.Preferences getSettings()
meth public void setError(java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public void setSelectedId(java.lang.String)
meth public {org.netbeans.modules.analysis.spi.Analyzer$CustomizerContext%0} getData()
meth public {org.netbeans.modules.analysis.spi.Analyzer$CustomizerContext%1} getPreviousComponent()
supr java.lang.Object
hfds data,errorListener,preferences,preselectId,previousComponent,selectedId

CLSS public abstract interface static org.netbeans.modules.analysis.spi.Analyzer$CustomizerProvider<%0 extends java.lang.Object, %1 extends javax.swing.JComponent>
 outer org.netbeans.modules.analysis.spi.Analyzer
meth public abstract {org.netbeans.modules.analysis.spi.Analyzer$CustomizerProvider%0} initialize()
meth public abstract {org.netbeans.modules.analysis.spi.Analyzer$CustomizerProvider%1} createComponent(org.netbeans.modules.analysis.spi.Analyzer$CustomizerContext<{org.netbeans.modules.analysis.spi.Analyzer$CustomizerProvider%0},{org.netbeans.modules.analysis.spi.Analyzer$CustomizerProvider%1}>)

CLSS public final static org.netbeans.modules.analysis.spi.Analyzer$MissingPlugin
 outer org.netbeans.modules.analysis.spi.Analyzer
cons public init(java.lang.String,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
supr java.lang.Object
hfds cnb,displayName

CLSS public final static org.netbeans.modules.analysis.spi.Analyzer$Result
 outer org.netbeans.modules.analysis.spi.Analyzer
meth public void reportAnalysisProblem(java.lang.String,java.lang.CharSequence)
meth public void reportError(org.netbeans.api.project.Project,org.netbeans.spi.editor.hints.ErrorDescription)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public void reportError(org.netbeans.spi.editor.hints.ErrorDescription)
 anno 1 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds analysisProblems,errors,errorsToProjects

CLSS public final static org.netbeans.modules.analysis.spi.Analyzer$WarningDescription
 outer org.netbeans.modules.analysis.spi.Analyzer
meth public static org.netbeans.modules.analysis.spi.Analyzer$WarningDescription create(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object
hfds categoryDisplayName,categoryId,warningDisplayName,warningId

CLSS public abstract interface org.openide.util.Cancellable
meth public abstract boolean cancel()

