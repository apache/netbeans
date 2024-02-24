#Signature file v4.1
#Version 1.33.0

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

CLSS public final org.netbeans.modules.javascript.v8debug.api.Connector
innr public final static Properties
meth public static void connect(org.netbeans.modules.javascript.v8debug.api.Connector$Properties,java.lang.Runnable) throws java.io.IOException
 anno 2 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object

CLSS public final static org.netbeans.modules.javascript.v8debug.api.Connector$Properties
 outer org.netbeans.modules.javascript.v8debug.api.Connector
cons public init(java.lang.String,int)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
cons public init(java.lang.String,int,java.util.List<java.lang.String>,java.util.List<java.lang.String>,java.util.Collection<java.lang.String>)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
meth public int getPort()
meth public java.lang.String getHostName()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public java.util.Collection<java.lang.String> getLocalPathExclusionFilters()
meth public java.util.List<java.lang.String> getLocalPaths()
meth public java.util.List<java.lang.String> getServerPaths()
supr java.lang.Object
hfds hostName,localPathExclusionFilters,localPaths,port,serverPaths

CLSS public final org.netbeans.modules.javascript.v8debug.api.DebuggerOptions
meth public boolean isBreakAtFirstLine()
meth public boolean isLiveEdit()
meth public static org.netbeans.modules.javascript.v8debug.api.DebuggerOptions getInstance()
meth public void addPreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
meth public void removePreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
meth public void setBreakAtFirstLine(boolean)
meth public void setLiveEdit(boolean)
supr java.lang.Object
hfds DEBUGGER_PREFS,INSTANCE,PROP_BRK_1ST,PROP_LIVE_EDIT,prefs

