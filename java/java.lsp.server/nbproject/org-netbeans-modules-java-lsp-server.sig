#Signature file v4.1
#Version 2.6.0

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

CLSS public org.netbeans.modules.java.lsp.server.ui.AbstractDiagnosticReporter
cons public init()
intf org.netbeans.spi.lsp.DiagnosticReporter
meth public org.netbeans.api.lsp.Diagnostic$ReporterControl findDiagnosticControl(org.openide.util.Lookup,org.openide.filesystems.FileObject)
supr java.lang.Object

CLSS public org.netbeans.modules.java.lsp.server.ui.AbstractDialogDisplayer
cons public init()
meth public <%0 extends org.openide.NotifyDescriptor> java.util.concurrent.CompletableFuture<{%%0}> notifyFuture({%%0})
meth public java.awt.Dialog createDialog(org.openide.DialogDescriptor)
meth public java.lang.Object notify(org.openide.NotifyDescriptor)
meth public void notifyLater(org.openide.NotifyDescriptor)
supr org.openide.DialogDisplayer
hfds LOG

CLSS public org.netbeans.modules.java.lsp.server.ui.AbstractGlobalActionContext
cons public init()
intf org.openide.util.ContextGlobalProvider
intf org.openide.util.Lookup$Provider
meth public org.openide.util.Lookup createGlobalContext()
meth public org.openide.util.Lookup getLookup()
meth public static <%0 extends java.lang.Object> {%%0} withActionContext(org.openide.util.Lookup,java.util.concurrent.Callable<{%%0}>)
supr java.lang.Object
hcls ContextHolder

CLSS public abstract org.netbeans.modules.java.lsp.server.ui.AbstractLspBrokenReferences
cons protected init()
intf org.netbeans.spi.project.ui.ProjectProblemsImplementation
meth public java.util.concurrent.CompletableFuture<java.lang.Void> showAlert(org.netbeans.api.project.Project)
meth public java.util.concurrent.CompletableFuture<java.lang.Void> showCustomizer(org.netbeans.api.project.Project)
supr java.lang.Object
hfds delegate

CLSS public org.netbeans.modules.java.lsp.server.ui.AbstractLspHtmlViewer
cons protected init()
innr protected final View
intf org.netbeans.spi.htmlui.HTMLViewerSpi<org.netbeans.modules.java.lsp.server.ui.AbstractLspHtmlViewer$View,java.lang.Object>
meth public <%0 extends java.lang.Object> {%%0} component(org.netbeans.modules.java.lsp.server.ui.AbstractLspHtmlViewer$View,java.lang.Class<{%%0}>)
meth public java.lang.Object createButton(org.netbeans.modules.java.lsp.server.ui.AbstractLspHtmlViewer$View,java.lang.String)
meth public java.lang.String getId(org.netbeans.modules.java.lsp.server.ui.AbstractLspHtmlViewer$View,java.lang.Object)
meth public org.netbeans.modules.java.lsp.server.ui.AbstractLspHtmlViewer$View newView(org.netbeans.spi.htmlui.HTMLViewerSpi$Context)
meth public void runLater(org.netbeans.modules.java.lsp.server.ui.AbstractLspHtmlViewer$View,java.lang.Runnable)
meth public void setEnabled(org.netbeans.modules.java.lsp.server.ui.AbstractLspHtmlViewer$View,java.lang.Object,boolean)
meth public void setText(org.netbeans.modules.java.lsp.server.ui.AbstractLspHtmlViewer$View,java.lang.Object,java.lang.String)
supr java.lang.Object

CLSS protected final org.netbeans.modules.java.lsp.server.ui.AbstractLspHtmlViewer$View
 outer org.netbeans.modules.java.lsp.server.ui.AbstractLspHtmlViewer
supr java.lang.Object
hfds ctx,presenter,ui

CLSS public abstract org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider
cons protected init()
innr public final static LspIO
intf org.netbeans.spi.io.InputOutputProvider<org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO,java.io.PrintWriter,java.lang.Void,java.lang.Void>
meth public final boolean isIOClosed(org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO)
meth public final java.io.PrintWriter getErr(org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO)
meth public final java.io.PrintWriter getOut(org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO)
meth public final java.io.Reader getIn(org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO)
meth public final java.lang.String getIODescription(org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO)
meth public final java.lang.String getId()
meth public final java.lang.Void getCurrentPosition(org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO,java.io.PrintWriter)
meth public final java.lang.Void startFold(org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO,java.io.PrintWriter,boolean)
meth public final org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO getIO(java.lang.String,boolean,org.openide.util.Lookup)
meth public final org.openide.util.Lookup getIOLookup(org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO)
meth public final void closeIO(org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO)
meth public final void endFold(org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO,java.io.PrintWriter,java.lang.Void)
meth public final void print(org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO,java.io.PrintWriter,java.lang.String,org.netbeans.api.io.Hyperlink,org.netbeans.api.io.OutputColor,boolean)
meth public final void resetIO(org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO)
meth public final void scrollTo(org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO,java.io.PrintWriter,java.lang.Void)
meth public final void setFoldExpanded(org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO,java.io.PrintWriter,java.lang.Void,boolean)
meth public final void setIODescription(org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO,java.lang.String)
meth public final void showIO(org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO,java.util.Set<org.netbeans.api.io.ShowOperation>)
supr java.lang.Object

CLSS public final static org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider$LspIO
 outer org.netbeans.modules.java.lsp.server.ui.AbstractLspInputOutputProvider
supr java.lang.Object
hfds ctx,err,in,lookup,name,out
hcls LspWriter

CLSS public abstract org.netbeans.modules.java.lsp.server.ui.AbstractLspStatusDisplayer
cons protected init()
meth public final java.lang.String getStatusText()
meth public final org.openide.awt.StatusDisplayer$Message setStatusText(java.lang.String,int)
meth public final void addChangeListener(javax.swing.event.ChangeListener)
meth public final void removeChangeListener(javax.swing.event.ChangeListener)
meth public final void setStatusText(java.lang.String)
supr org.openide.awt.StatusDisplayer
hfds text

CLSS public abstract org.netbeans.modules.java.lsp.server.ui.AbstractProgressEnvironment
cons protected init()
cons protected init(org.openide.util.Lookup)
intf org.netbeans.modules.progress.spi.ProgressEnvironment
meth public org.netbeans.api.progress.ProgressHandle createHandle(java.lang.String,org.openide.util.Cancellable,boolean)
meth public org.netbeans.modules.progress.spi.Controller findController(org.netbeans.modules.progress.spi.InternalHandle)
meth public org.netbeans.modules.progress.spi.Controller getController()
supr java.lang.Object
hfds MASK_CANCELLABLES,NO_CONTEXT_CONTROLLER,env,patternMaskingCancellables
hcls NoContextHandle

CLSS public abstract org.netbeans.modules.java.lsp.server.ui.IOContext
cons public init()
meth protected abstract boolean isValid()
meth protected abstract void stdErr(java.lang.String)
meth protected abstract void stdOut(java.lang.String)
meth protected java.io.InputStream getStdIn() throws java.io.IOException
meth protected void stdIn(java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds lastCtx
hcls StdErrContext

CLSS public abstract interface org.netbeans.modules.progress.spi.ProgressEnvironment
meth public abstract org.netbeans.api.progress.ProgressHandle createHandle(java.lang.String,org.openide.util.Cancellable,boolean)
meth public abstract org.netbeans.modules.progress.spi.Controller getController()

CLSS public abstract interface org.netbeans.spi.htmlui.HTMLViewerSpi<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public final static Context
meth public abstract <%0 extends java.lang.Object> {%%0} component({org.netbeans.spi.htmlui.HTMLViewerSpi%0},java.lang.Class<{%%0}>)
meth public abstract java.lang.String getId({org.netbeans.spi.htmlui.HTMLViewerSpi%0},{org.netbeans.spi.htmlui.HTMLViewerSpi%1})
meth public abstract void runLater({org.netbeans.spi.htmlui.HTMLViewerSpi%0},java.lang.Runnable)
meth public abstract void setEnabled({org.netbeans.spi.htmlui.HTMLViewerSpi%0},{org.netbeans.spi.htmlui.HTMLViewerSpi%1},boolean)
meth public abstract void setText({org.netbeans.spi.htmlui.HTMLViewerSpi%0},{org.netbeans.spi.htmlui.HTMLViewerSpi%1},java.lang.String)
meth public abstract {org.netbeans.spi.htmlui.HTMLViewerSpi%0} newView(org.netbeans.spi.htmlui.HTMLViewerSpi$Context)
meth public abstract {org.netbeans.spi.htmlui.HTMLViewerSpi%1} createButton({org.netbeans.spi.htmlui.HTMLViewerSpi%0},java.lang.String)

CLSS public abstract interface org.netbeans.spi.io.InputOutputProvider<%0 extends java.lang.Object, %1 extends java.io.PrintWriter, %2 extends java.lang.Object, %3 extends java.lang.Object>
meth public abstract boolean isIOClosed({org.netbeans.spi.io.InputOutputProvider%0})
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.io.Reader getIn({org.netbeans.spi.io.InputOutputProvider%0})
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getIODescription({org.netbeans.spi.io.InputOutputProvider%0})
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.lang.String getId()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public abstract org.openide.util.Lookup getIOLookup({org.netbeans.spi.io.InputOutputProvider%0})
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void closeIO({org.netbeans.spi.io.InputOutputProvider%0})
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void endFold({org.netbeans.spi.io.InputOutputProvider%0},{org.netbeans.spi.io.InputOutputProvider%1},{org.netbeans.spi.io.InputOutputProvider%3})
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract void print({org.netbeans.spi.io.InputOutputProvider%0},{org.netbeans.spi.io.InputOutputProvider%1},java.lang.String,org.netbeans.api.io.Hyperlink,org.netbeans.api.io.OutputColor,boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
 anno 4 org.netbeans.api.annotations.common.NullAllowed()
 anno 5 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void resetIO({org.netbeans.spi.io.InputOutputProvider%0})
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void scrollTo({org.netbeans.spi.io.InputOutputProvider%0},{org.netbeans.spi.io.InputOutputProvider%1},{org.netbeans.spi.io.InputOutputProvider%2})
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setFoldExpanded({org.netbeans.spi.io.InputOutputProvider%0},{org.netbeans.spi.io.InputOutputProvider%1},{org.netbeans.spi.io.InputOutputProvider%3},boolean)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract void setIODescription({org.netbeans.spi.io.InputOutputProvider%0},java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
meth public abstract void showIO({org.netbeans.spi.io.InputOutputProvider%0},java.util.Set<org.netbeans.api.io.ShowOperation>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract {org.netbeans.spi.io.InputOutputProvider%0} getIO(java.lang.String,boolean,org.openide.util.Lookup)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NonNull()
meth public abstract {org.netbeans.spi.io.InputOutputProvider%1} getErr({org.netbeans.spi.io.InputOutputProvider%0})
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract {org.netbeans.spi.io.InputOutputProvider%1} getOut({org.netbeans.spi.io.InputOutputProvider%0})
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract {org.netbeans.spi.io.InputOutputProvider%2} getCurrentPosition({org.netbeans.spi.io.InputOutputProvider%0},{org.netbeans.spi.io.InputOutputProvider%1})
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
meth public abstract {org.netbeans.spi.io.InputOutputProvider%3} startFold({org.netbeans.spi.io.InputOutputProvider%0},{org.netbeans.spi.io.InputOutputProvider%1},boolean)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract interface org.netbeans.spi.lsp.DiagnosticReporter
meth public abstract org.netbeans.api.lsp.Diagnostic$ReporterControl findDiagnosticControl(org.openide.util.Lookup,org.openide.filesystems.FileObject)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
 anno 2 org.netbeans.api.annotations.common.NullAllowed()

CLSS public abstract interface org.netbeans.spi.project.ui.ProjectProblemsImplementation
meth public abstract java.util.concurrent.CompletableFuture<java.lang.Void> showAlert(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract java.util.concurrent.CompletableFuture<java.lang.Void> showCustomizer(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public abstract org.openide.DialogDisplayer
cons protected init()
meth public <%0 extends org.openide.NotifyDescriptor> java.util.concurrent.CompletableFuture<{%%0}> notifyFuture({%%0})
meth public abstract java.awt.Dialog createDialog(org.openide.DialogDescriptor)
meth public abstract java.lang.Object notify(org.openide.NotifyDescriptor)
meth public java.awt.Dialog createDialog(org.openide.DialogDescriptor,java.awt.Frame)
meth public static org.openide.DialogDisplayer getDefault()
meth public void notifyLater(org.openide.NotifyDescriptor)
supr java.lang.Object
hcls Trivial

CLSS public abstract org.openide.awt.StatusDisplayer
cons protected init()
fld public final static int IMPORTANCE_ANNOTATION = 1000
fld public final static int IMPORTANCE_ERROR_HIGHLIGHT = 700
fld public final static int IMPORTANCE_FIND_OR_REPLACE = 800
fld public final static int IMPORTANCE_INCREMENTAL_FIND = 900
innr public abstract interface static Message
meth public abstract java.lang.String getStatusText()
meth public abstract void addChangeListener(javax.swing.event.ChangeListener)
meth public abstract void removeChangeListener(javax.swing.event.ChangeListener)
meth public abstract void setStatusText(java.lang.String)
meth public org.openide.awt.StatusDisplayer$Message setStatusText(java.lang.String,int)
meth public static org.openide.awt.StatusDisplayer getDefault()
supr java.lang.Object
hfds INSTANCE
hcls Trivial

CLSS public abstract interface org.openide.util.ContextGlobalProvider
meth public abstract org.openide.util.Lookup createGlobalContext()

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

