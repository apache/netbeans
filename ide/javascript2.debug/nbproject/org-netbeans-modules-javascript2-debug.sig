#Signature file v4.1
#Version 1.41

CLSS public abstract interface java.beans.BeanInfo
fld public final static int ICON_COLOR_16x16 = 1
fld public final static int ICON_COLOR_32x32 = 2
fld public final static int ICON_MONO_16x16 = 3
fld public final static int ICON_MONO_32x32 = 4
meth public abstract int getDefaultEventIndex()
meth public abstract int getDefaultPropertyIndex()
meth public abstract java.awt.Image getIcon(int)
meth public abstract java.beans.BeanDescriptor getBeanDescriptor()
meth public abstract java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public abstract java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public abstract java.beans.MethodDescriptor[] getMethodDescriptors()
meth public abstract java.beans.PropertyDescriptor[] getPropertyDescriptors()

CLSS public abstract interface java.beans.PropertyChangeListener
intf java.util.EventListener
meth public abstract void propertyChange(java.beans.PropertyChangeEvent)

CLSS public java.beans.SimpleBeanInfo
cons public init()
intf java.beans.BeanInfo
meth public int getDefaultEventIndex()
meth public int getDefaultPropertyIndex()
meth public java.awt.Image getIcon(int)
meth public java.awt.Image loadImage(java.lang.String)
meth public java.beans.BeanDescriptor getBeanDescriptor()
meth public java.beans.BeanInfo[] getAdditionalBeanInfo()
meth public java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public java.beans.MethodDescriptor[] getMethodDescriptors()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.lang.Object

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

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

CLSS public abstract interface java.util.Collection<%0 extends java.lang.Object>
intf java.lang.Iterable<{java.util.Collection%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Collection%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Collection%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Collection%0}> iterator()
meth public abstract void clear()
meth public boolean removeIf(java.util.function.Predicate<? super {java.util.Collection%0}>)
meth public java.util.Spliterator<{java.util.Collection%0}> spliterator()
meth public java.util.stream.Stream<{java.util.Collection%0}> parallelStream()
meth public java.util.stream.Stream<{java.util.Collection%0}> stream()

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface java.util.Set<%0 extends java.lang.Object>
intf java.util.Collection<{java.util.Set%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Set%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Set%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Set%0}> iterator()
meth public abstract void clear()
meth public java.util.Spliterator<{java.util.Set%0}> spliterator()

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

CLSS public abstract interface org.netbeans.api.debugger.DebuggerManagerListener
intf java.beans.PropertyChangeListener
meth public abstract org.netbeans.api.debugger.Breakpoint[] initBreakpoints()
meth public abstract void breakpointAdded(org.netbeans.api.debugger.Breakpoint)
meth public abstract void breakpointRemoved(org.netbeans.api.debugger.Breakpoint)
meth public abstract void engineAdded(org.netbeans.api.debugger.DebuggerEngine)
meth public abstract void engineRemoved(org.netbeans.api.debugger.DebuggerEngine)
meth public abstract void initWatches()
meth public abstract void sessionAdded(org.netbeans.api.debugger.Session)
meth public abstract void sessionRemoved(org.netbeans.api.debugger.Session)
meth public abstract void watchAdded(org.netbeans.api.debugger.Watch)
meth public abstract void watchRemoved(org.netbeans.api.debugger.Watch)

CLSS public abstract interface org.netbeans.api.debugger.LazyDebuggerManagerListener
intf org.netbeans.api.debugger.DebuggerManagerListener
meth public abstract java.lang.String[] getProperties()

CLSS public abstract org.netbeans.api.debugger.Properties
cons public init()
innr public abstract interface static Initializer
innr public abstract interface static Reader
meth public abstract boolean getBoolean(java.lang.String,boolean)
meth public abstract byte getByte(java.lang.String,byte)
meth public abstract char getChar(java.lang.String,char)
meth public abstract double getDouble(java.lang.String,double)
meth public abstract float getFloat(java.lang.String,float)
meth public abstract int getInt(java.lang.String,int)
meth public abstract java.lang.Object getObject(java.lang.String,java.lang.Object)
meth public abstract java.lang.Object[] getArray(java.lang.String,java.lang.Object[])
meth public abstract java.lang.String getString(java.lang.String,java.lang.String)
meth public abstract java.util.Collection getCollection(java.lang.String,java.util.Collection)
meth public abstract java.util.Map getMap(java.lang.String,java.util.Map)
meth public abstract long getLong(java.lang.String,long)
meth public abstract org.netbeans.api.debugger.Properties getProperties(java.lang.String)
meth public abstract short getShort(java.lang.String,short)
meth public abstract void setArray(java.lang.String,java.lang.Object[])
meth public abstract void setBoolean(java.lang.String,boolean)
meth public abstract void setByte(java.lang.String,byte)
meth public abstract void setChar(java.lang.String,char)
meth public abstract void setCollection(java.lang.String,java.util.Collection)
meth public abstract void setDouble(java.lang.String,double)
meth public abstract void setFloat(java.lang.String,float)
meth public abstract void setInt(java.lang.String,int)
meth public abstract void setLong(java.lang.String,long)
meth public abstract void setMap(java.lang.String,java.util.Map)
meth public abstract void setObject(java.lang.String,java.lang.Object)
meth public abstract void setShort(java.lang.String,short)
meth public abstract void setString(java.lang.String,java.lang.String)
meth public static org.netbeans.api.debugger.Properties getDefault()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds LOG,defaultProperties
hcls DelegatingProperties,PrimitiveRegister,PropertiesImpl

CLSS public abstract interface static org.netbeans.api.debugger.Properties$Reader
 outer org.netbeans.api.debugger.Properties
meth public abstract java.lang.Object read(java.lang.String,org.netbeans.api.debugger.Properties)
meth public abstract java.lang.String[] getSupportedClassNames()
meth public abstract void write(java.lang.Object,org.netbeans.api.debugger.Properties)

CLSS public abstract interface org.netbeans.modules.javascript2.debug.EditorLineHandler
fld public final static java.lang.String PROP_LINE_NUMBER = "lineNumber"
meth public abstract int getLineNumber()
meth public abstract java.net.URL getURL()
meth public abstract org.openide.filesystems.FileObject getFileObject()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void dispose()
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setLineNumber(int)

CLSS public abstract org.netbeans.modules.javascript2.debug.EditorLineHandlerFactory
cons protected init()
meth public abstract org.netbeans.modules.javascript2.debug.EditorLineHandler get(java.net.URL,int)
meth public abstract org.netbeans.modules.javascript2.debug.EditorLineHandler get(org.openide.filesystems.FileObject,int)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static org.netbeans.modules.javascript2.debug.EditorLineHandler getHandler(java.net.URL,int)
meth public static org.netbeans.modules.javascript2.debug.EditorLineHandler getHandler(org.openide.filesystems.FileObject,int)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
supr java.lang.Object

CLSS public final org.netbeans.modules.javascript2.debug.NamesTranslator
meth public java.lang.String reverseTranslate(java.lang.String)
meth public java.lang.String translate(java.lang.String)
meth public java.lang.String translateDeclarationNodeName(java.lang.String)
meth public static org.netbeans.modules.javascript2.debug.NamesTranslator create(org.netbeans.modules.web.common.sourcemap.SourceMapsTranslator,org.openide.filesystems.FileObject,int,int)
supr java.lang.Object
hfds USE_SOURCE_MAPS,declarationNodeName,directMap,fileObject,offset,reverseMap,smt,source,varTranslationsRegistered

CLSS public org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointStatus
meth public final static void resetValidity(org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint)
meth public final static void setInvalid(org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint,java.lang.String)
meth public static org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint getActive()
meth public static void setActive(org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint)
meth public static void setValid(org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint,java.lang.String)
supr java.lang.Object
hfds activeBPRef

CLSS public abstract interface org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointsInfo
fld public final static java.lang.String PROP_BREAKPOINTS_ACTIVE = "breakpointsActive"
meth public abstract boolean areBreakpointsActivated()
meth public abstract boolean isAnnotatable(org.openide.filesystems.FileObject)
meth public abstract boolean isTransientURL(java.net.URL)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointsInfoManager
meth public boolean areBreakpointsActivated()
meth public boolean isAnnotatable(org.openide.filesystems.FileObject)
meth public boolean isTransientURL(java.net.URL)
meth public static org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointsInfoManager getDefault()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds INSTANCE,infoServices,infoServicesLock,lastActivated,pcs,servicePCL
hcls ServicePropertyChangeListener

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

CLSS public org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpointBeanInfo
cons public init()
meth public java.beans.BeanDescriptor getBeanDescriptor()
supr java.beans.SimpleBeanInfo
hfds LOG

CLSS public final org.netbeans.modules.javascript2.debug.breakpoints.io.BreakpointsFromGroup
cons public init(java.lang.String)
cons public init(org.netbeans.modules.javascript2.debug.breakpoints.io.BreakpointsFromGroup$TestGroupProperties)
innr public final static TestGroupProperties
intf java.util.Set<org.netbeans.api.debugger.Breakpoint>
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public boolean add(org.netbeans.api.debugger.Breakpoint)
meth public boolean addAll(java.util.Collection<? extends org.netbeans.api.debugger.Breakpoint>)
meth public boolean contains(java.lang.Object)
meth public boolean containsAll(java.util.Collection<?>)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean retainAll(java.util.Collection<?>)
meth public int size()
meth public java.lang.Object[] toArray()
meth public java.lang.String getGroupName()
meth public java.util.Iterator<org.netbeans.api.debugger.Breakpoint> iterator()
meth public org.netbeans.modules.javascript2.debug.breakpoints.io.BreakpointsFromGroup$TestGroupProperties getTestGroupProperties()
meth public void clear()
supr java.lang.Object
hfds groupName,testProperties

CLSS public final static org.netbeans.modules.javascript2.debug.breakpoints.io.BreakpointsFromGroup$TestGroupProperties
 outer org.netbeans.modules.javascript2.debug.breakpoints.io.BreakpointsFromGroup
cons public init(java.lang.String)
cons public init(org.netbeans.api.project.Project)
cons public init(org.openide.filesystems.FileObject)
meth public java.lang.String getType()
meth public org.netbeans.api.project.Project getProject()
meth public org.openide.filesystems.FileObject getFileObject()
supr java.lang.Object
hfds fo,p,type

CLSS public org.netbeans.modules.javascript2.debug.breakpoints.io.JSBreakpointReader
cons public init()
intf org.netbeans.api.debugger.Properties$Reader
meth public java.lang.Object read(java.lang.String,org.netbeans.api.debugger.Properties)
meth public java.lang.String[] getSupportedClassNames()
meth public void write(java.lang.Object,org.netbeans.api.debugger.Properties)
supr java.lang.Object
hfds BP_CUSTOM_GROUP,BP_FILE_GROUP,BP_PROJECT_GROUP,BP_TYPE_GROUP,BREAKPOINTS_TO_DISABLE,BREAKPOINTS_TO_ENABLE,LOG

CLSS public org.netbeans.modules.javascript2.debug.breakpoints.io.PersistenceManager
cons public init()
intf org.netbeans.api.debugger.LazyDebuggerManagerListener
meth public java.lang.String[] getProperties()
meth public org.netbeans.api.debugger.Breakpoint[] initBreakpoints()
meth public void breakpointAdded(org.netbeans.api.debugger.Breakpoint)
meth public void breakpointRemoved(org.netbeans.api.debugger.Breakpoint)
meth public void engineAdded(org.netbeans.api.debugger.DebuggerEngine)
meth public void engineRemoved(org.netbeans.api.debugger.DebuggerEngine)
meth public void initWatches()
meth public void propertyChange(java.beans.PropertyChangeEvent)
meth public void sessionAdded(org.netbeans.api.debugger.Session)
meth public void sessionRemoved(org.netbeans.api.debugger.Session)
meth public void watchAdded(org.netbeans.api.debugger.Watch)
meth public void watchRemoved(org.netbeans.api.debugger.Watch)
supr java.lang.Object
hfds JS_PROPERTY

CLSS public abstract interface org.netbeans.modules.javascript2.debug.sources.SourceContent
meth public abstract java.lang.String getContent() throws java.io.IOException
meth public abstract long getLength()

CLSS public final org.netbeans.modules.javascript2.debug.sources.SourceFilesCache
fld public final static java.lang.String URL_PROTOCOL = "js-scripts"
meth public java.net.URL getSourceFile(java.lang.String,int,java.lang.String)
meth public java.net.URL getSourceFile(java.lang.String,int,org.netbeans.modules.javascript2.debug.sources.SourceContent)
meth public static org.netbeans.modules.javascript2.debug.sources.SourceFilesCache getDefault()
supr java.lang.Object
hfds DEFAULT,fs
hcls StringContent

CLSS public final org.netbeans.modules.javascript2.debug.sources.SourceURLMapper
cons public init()
innr public final static SourceURLHandler
meth public java.net.URL getURL(org.openide.filesystems.FileObject,int)
meth public org.openide.filesystems.FileObject[] getFileObjects(java.net.URL)
meth public static java.lang.String percentDecode(java.lang.String)
meth public static java.lang.String percentEncode(java.lang.String)
supr org.openide.filesystems.URLMapper
hfds HOST,filesystems

CLSS public final static org.netbeans.modules.javascript2.debug.sources.SourceURLMapper$SourceURLHandler
 outer org.netbeans.modules.javascript2.debug.sources.SourceURLMapper
cons public init()
meth protected java.net.URLConnection openConnection(java.net.URL) throws java.io.IOException
supr java.net.URLStreamHandler

CLSS public abstract interface org.netbeans.modules.javascript2.debug.spi.SourceElementsQuery
innr public final static Var
meth public abstract int getObjectOffsetAt(org.netbeans.modules.parsing.api.Source,int)
meth public abstract java.util.Collection<org.netbeans.modules.javascript2.debug.spi.SourceElementsQuery$Var> getVarsAt(org.netbeans.modules.parsing.api.Source,int)

CLSS public final static org.netbeans.modules.javascript2.debug.spi.SourceElementsQuery$Var
 outer org.netbeans.modules.javascript2.debug.spi.SourceElementsQuery
cons public init(java.lang.String,int)
meth public int getOffset()
meth public java.lang.String getName()
supr java.lang.Object
hfds name,offset

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

