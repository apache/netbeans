#Signature file v4.1
#Version 1.6.0

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

CLSS public org.netbeans.modules.java.lsp.server.ui.AbstractDialogDisplayer
cons public init()
meth public java.awt.Dialog createDialog(org.openide.DialogDescriptor)
meth public java.lang.Object notify(org.openide.NotifyDescriptor)
meth public void notifyLater(org.openide.NotifyDescriptor)
supr org.openide.DialogDisplayer
hfds context

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

CLSS public abstract org.netbeans.modules.java.lsp.server.ui.IOContext
cons public init()
meth protected abstract boolean isValid()
meth protected abstract void stdErr(java.lang.String)
meth protected abstract void stdOut(java.lang.String)
supr java.lang.Object
hfds lastCtx
hcls StdErrContext

CLSS public abstract org.netbeans.modules.java.lsp.server.ui.UIContext
cons public init()
meth protected abstract boolean isValid()
meth protected abstract java.util.concurrent.CompletableFuture<org.eclipse.lsp4j.MessageActionItem> showMessageRequest(org.eclipse.lsp4j.ShowMessageRequestParams)
meth protected abstract org.openide.awt.StatusDisplayer$Message showStatusMessage(org.netbeans.modules.java.lsp.server.protocol.ShowStatusMessageParams)
meth protected abstract void logMessage(org.eclipse.lsp4j.MessageParams)
meth protected abstract void showMessage(org.eclipse.lsp4j.MessageParams)
meth public static org.netbeans.modules.java.lsp.server.ui.UIContext find()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public static org.netbeans.modules.java.lsp.server.ui.UIContext find(org.openide.util.Lookup)
 anno 0 org.netbeans.api.annotations.common.NonNull()
supr java.lang.Object
hfds lastCtx
hcls LogImpl

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

CLSS public abstract org.openide.DialogDisplayer
cons protected init()
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

