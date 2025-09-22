#Signature file v4.1
#Version 0.58

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

CLSS public final org.netbeans.modules.javascript.nodejs.api.DebuggerOptions
meth public boolean isBreakAtFirstLine()
meth public boolean isLiveEdit()
meth public java.lang.String getDebuggerProtocol()
meth public static org.netbeans.modules.javascript.nodejs.api.DebuggerOptions getInstance()
meth public void addPreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
meth public void removePreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
meth public void setBreakAtFirstLine(boolean)
meth public void setDebuggerProtocol(java.lang.String)
meth public void setLiveEdit(boolean)
supr java.lang.Object
hfds DEBUGGER_PREFS,INSTANCE,PROP_BRK_1ST,PROP_DEBUGGER_PROTOCOL,PROP_LIVE_EDIT,prefs

CLSS public final org.netbeans.modules.javascript.nodejs.api.NodeJsSupport
meth public boolean isEnabled(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getNode(org.netbeans.api.project.Project)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public static org.netbeans.modules.javascript.nodejs.api.NodeJsSupport getInstance()
meth public void openNodeSettings(org.netbeans.api.project.Project)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds INSTANCE

CLSS public abstract interface org.netbeans.modules.javascript.nodejs.spi.DebuggerStartModifier
meth public abstract boolean startProcessingDone()
meth public abstract java.util.List<java.lang.String> getArguments(org.openide.util.Lookup)
meth public abstract void processOutputLine(java.lang.String)

CLSS public abstract interface org.netbeans.modules.javascript.nodejs.spi.DebuggerStartModifierFactory
meth public abstract org.netbeans.modules.javascript.nodejs.spi.DebuggerStartModifier create(org.netbeans.api.project.Project,java.util.List<java.lang.String>,java.util.List<java.lang.String>,java.util.List<java.lang.String>,java.util.concurrent.atomic.AtomicReference<java.util.concurrent.Future<java.lang.Integer>>)

