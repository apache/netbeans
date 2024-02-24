#Signature file v4.1
#Version 1.20

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

CLSS public abstract java.net.URLStreamHandler
cons public init()
meth protected abstract java.net.URLConnection openConnection(java.net.URL) throws java.io.IOException
meth protected boolean equals(java.net.URL,java.net.URL)
meth protected boolean hostsEqual(java.net.URL,java.net.URL)
meth protected boolean sameFile(java.net.URL,java.net.URL)
meth protected int getDefaultPort()
meth protected int hashCode(java.net.URL)
meth protected java.lang.String toExternalForm(java.net.URL)
meth protected java.net.InetAddress getHostAddress(java.net.URL)
meth protected java.net.URLConnection openConnection(java.net.URL,java.net.Proxy) throws java.io.IOException
meth protected void parseURL(java.net.URL,java.lang.String,int,int)
meth protected void setURL(java.net.URL,java.lang.String,java.lang.String,int,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth protected void setURL(java.net.URL,java.lang.String,java.lang.String,int,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public abstract org.netbeans.api.debugger.Breakpoint
cons public init()
fld public final static java.lang.String PROP_DISPOSED = "disposed"
fld public final static java.lang.String PROP_ENABLED = "enabled"
fld public final static java.lang.String PROP_GROUP_NAME = "groupName"
fld public final static java.lang.String PROP_GROUP_PROPERTIES = "groupProperties"
fld public final static java.lang.String PROP_HIT_COUNT_FILTER = "hitCountFilter"
fld public final static java.lang.String PROP_VALIDITY = "validity"
innr public abstract static GroupProperties
innr public final static !enum HIT_COUNT_FILTERING_STYLE
innr public final static !enum VALIDITY
meth protected final void setValidity(org.netbeans.api.debugger.Breakpoint$VALIDITY,java.lang.String)
meth protected void dispose()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract boolean isEnabled()
meth public abstract void disable()
meth public abstract void enable()
meth public boolean canHaveDependentBreakpoints()
meth public final int getHitCountFilter()
meth public final java.lang.String getValidityMessage()
meth public final org.netbeans.api.debugger.Breakpoint$HIT_COUNT_FILTERING_STYLE getHitCountFilteringStyle()
meth public final org.netbeans.api.debugger.Breakpoint$VALIDITY getValidity()
meth public final void setHitCountFilter(int,org.netbeans.api.debugger.Breakpoint$HIT_COUNT_FILTERING_STYLE)
meth public java.lang.String getGroupName()
meth public java.util.Set<org.netbeans.api.debugger.Breakpoint> getBreakpointsToDisable()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.Set<org.netbeans.api.debugger.Breakpoint> getBreakpointsToEnable()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public org.netbeans.api.debugger.Breakpoint$GroupProperties getGroupProperties()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void setBreakpointsToDisable(java.util.Set<org.netbeans.api.debugger.Breakpoint>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setBreakpointsToEnable(java.util.Set<org.netbeans.api.debugger.Breakpoint>)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public void setGroupName(java.lang.String)
supr java.lang.Object
hfds breakpointsToDisable,breakpointsToEnable,groupName,hitCountFilter,hitCountFilteringStyle,pcs,validity,validityMessage

CLSS public org.netbeans.modules.debugger.jpda.truffle.breakpoints.TruffleLineBreakpoint
cons public init(java.net.URL,int)
cons public init(org.netbeans.modules.javascript2.debug.EditorLineHandler)
meth public final boolean isSuspend()
meth public final java.lang.String getPrintText()
meth public final void setPrintText(java.lang.String)
meth public final void setSuspend(boolean)
supr org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint
hfds printText,suspend
hcls FixedLineHandler

CLSS public final org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame
cons public init(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.JPDAThread,int,org.netbeans.api.debugger.jpda.ObjectVariable,java.lang.String,com.sun.jdi.StringReference,org.netbeans.modules.debugger.jpda.truffle.vars.impl.TruffleScope[],org.netbeans.api.debugger.jpda.ObjectVariable,boolean)
meth public boolean isHost()
meth public boolean isInternal()
meth public final int getDepth()
meth public final org.netbeans.api.debugger.jpda.JPDADebugger getDebugger()
meth public final org.netbeans.api.debugger.jpda.JPDAThread getThread()
meth public java.lang.String getDisplayName()
meth public java.lang.String getHostClassName()
meth public java.lang.String getHostMethodName()
meth public java.lang.String getMethodName()
meth public java.lang.String getSourceLocation()
meth public org.netbeans.api.debugger.jpda.ObjectVariable getStackFrameInstance()
meth public org.netbeans.api.debugger.jpda.ObjectVariable getThis()
meth public org.netbeans.modules.debugger.jpda.truffle.LanguageName getLanguage()
meth public org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition getSourcePosition()
meth public org.netbeans.modules.debugger.jpda.truffle.vars.impl.TruffleScope[] getScopes()
meth public void popToHere()
supr java.lang.Object
hfds LOG,codeRef,debugger,depth,frameInstance,hostClassName,hostMethodName,isHost,isInternal,language,methodName,mimeType,scopes,sourceId,sourceLocation,sourceName,sourcePath,sourceSection,sourceURI,thisObject,thread

CLSS public final org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackInfo
cons public init(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.JPDAThread,org.netbeans.api.debugger.jpda.ObjectVariable)
 anno 0 java.lang.Deprecated()
cons public init(org.netbeans.api.debugger.jpda.JPDADebugger,org.netbeans.api.debugger.jpda.JPDAThread,org.netbeans.api.debugger.jpda.ObjectVariable,boolean)
meth public boolean hasInternalFrames()
meth public boolean hasJavaFrames()
meth public org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame[] getStackFrames(boolean)
supr java.lang.Object
hfds METHOD_GET_FRAMES_INFO,METHOD_GET_FRAMES_INFO_SIG,areInternalFrames,debugger,includedInternalFrames,stackFrames,stackTrace,supportsJavaFrames,thread

CLSS public final org.netbeans.modules.debugger.jpda.truffle.source.Source
fld public final static java.lang.String URL_PROTOCOL = "truffle-scripts"
meth public java.lang.String getContent()
meth public java.lang.String getHostMethodName()
meth public java.lang.String getMimeType()
meth public java.lang.String getName()
meth public java.lang.String getPath()
meth public java.net.URI getURI()
meth public java.net.URL getUrl()
meth public long getHash()
meth public static java.net.URI getTruffleInternalURI(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.debugger.jpda.truffle.source.Source getExistingSource(org.netbeans.api.debugger.jpda.JPDADebugger,long)
meth public static org.netbeans.modules.debugger.jpda.truffle.source.Source getSource(org.netbeans.api.debugger.jpda.JPDADebugger,long,java.lang.String,java.lang.String,java.lang.String,java.net.URI,java.lang.String,com.sun.jdi.StringReference)
meth public static org.netbeans.modules.debugger.jpda.truffle.source.Source getSource(org.netbeans.api.debugger.jpda.JPDADebugger,long,java.lang.String,java.lang.String,java.net.URI,com.sun.jdi.StringReference)
meth public static org.netbeans.modules.debugger.jpda.truffle.source.Source getSource(org.netbeans.api.debugger.jpda.JPDADebugger,long,java.lang.String,java.lang.String,java.net.URI,java.lang.String,com.sun.jdi.StringReference)
supr java.lang.Object
hfds ATTR_URI,KNOWN_SOURCES,codeRef,content,hash,hostMethodName,mimeType,name,path,uri,url

CLSS public final org.netbeans.modules.debugger.jpda.truffle.source.SourceBinaryTranslator
cons public init()
meth public static java.net.URI binary2Source(java.net.URI)
meth public static java.net.URI source2Binary(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds SOURCE_IDS

CLSS public org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition
cons public init(org.netbeans.api.debugger.jpda.JPDADebugger,long,org.netbeans.modules.debugger.jpda.truffle.source.Source,java.lang.String)
meth public int getEndColumn()
meth public int getEndLine()
meth public int getStartColumn()
meth public int getStartLine()
meth public org.netbeans.modules.debugger.jpda.truffle.source.Source getSource()
supr java.lang.Object
hfds endColumn,endLine,id,src,startColumn,startLine

CLSS public final org.netbeans.modules.debugger.jpda.truffle.source.SourceURLMapper
cons public init()
innr public final static SourceURLHandler
meth public java.net.URL getURL(org.openide.filesystems.FileObject,int)
meth public org.openide.filesystems.FileObject[] getFileObjects(java.net.URL)
meth public static java.lang.String percentDecode(java.lang.String)
meth public static java.lang.String percentEncode(java.lang.String)
supr org.openide.filesystems.URLMapper
hfds HOST,filesystems

CLSS public final static org.netbeans.modules.debugger.jpda.truffle.source.SourceURLMapper$SourceURLHandler
 outer org.netbeans.modules.debugger.jpda.truffle.source.SourceURLMapper
cons public init()
meth protected java.net.URLConnection openConnection(java.net.URL) throws java.io.IOException
supr java.net.URLStreamHandler

CLSS public abstract interface org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable
meth public abstract boolean hasTypeSource()
meth public abstract boolean hasValueSource()
meth public abstract boolean isInternal()
meth public abstract boolean isLeaf()
meth public abstract boolean isReadable()
meth public abstract boolean isWritable()
meth public abstract java.lang.Object getValue()
meth public abstract java.lang.Object[] getChildren()
meth public abstract java.lang.String getDisplayValue()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getType()
meth public abstract org.netbeans.api.debugger.jpda.ObjectVariable setValue(org.netbeans.api.debugger.jpda.JPDADebugger,java.lang.String)
meth public abstract org.netbeans.modules.debugger.jpda.truffle.LanguageName getLanguage()
meth public abstract org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition getTypeSource()
meth public abstract org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition getValueSource()
meth public boolean isReceiver()
meth public static org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable get(org.netbeans.api.debugger.jpda.Variable)

CLSS public org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint
cons public init(org.netbeans.modules.javascript2.debug.EditorLineHandler)
fld public final static java.lang.String PROP_CONDITION = "condition"
fld public final static java.lang.String PROP_FILE = "fileChanged"
fld public final static java.lang.String PROP_LINE_NUMBER = "lineNumber"
fld public final static java.lang.String PROP_URL = "url"
meth protected void dispose()
meth public boolean isEnabled()
meth public final boolean isConditional()
meth public final java.lang.String getCondition()
meth public final void setCondition(java.lang.String)
meth public int getLineNumber()
meth public java.lang.String toString()
meth public java.net.URL getURL()
meth public org.netbeans.modules.javascript2.debug.EditorLineHandler getLineHandler()
meth public org.openide.filesystems.FileObject getFileObject()
meth public void disable()
meth public void enable()
meth public void setLine(int)
meth public void setLineHandler(org.netbeans.modules.javascript2.debug.EditorLineHandler)
supr org.netbeans.api.debugger.Breakpoint
hfds condition,isEnabled,line,lineChangesWeak,lineChangeslistener,myListener,myWeakListener
hcls FileRemoveListener,LineChangesListener

CLSS public abstract org.openide.filesystems.URLMapper
cons public init()
fld public final static int EXTERNAL = 1
fld public final static int INTERNAL = 0
fld public final static int NETWORK = 2
meth public abstract java.net.URL getURL(org.openide.filesystems.FileObject,int)
meth public abstract org.openide.filesystems.FileObject[] getFileObjects(java.net.URL)
meth public static java.net.URL findURL(org.openide.filesystems.FileObject,int)
meth public static org.openide.filesystems.FileObject findFileObject(java.net.URL)
meth public static org.openide.filesystems.FileObject[] findFileObjects(java.net.URL)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds CACHE_JUST_COMPUTING,cache,result,threadCache
hcls DefaultURLMapper

