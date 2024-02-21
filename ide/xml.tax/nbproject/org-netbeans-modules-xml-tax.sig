#Signature file v4.1
#Version 1.67.0

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

CLSS public java.beans.FeatureDescriptor
cons public init()
meth public boolean isExpert()
meth public boolean isHidden()
meth public boolean isPreferred()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String getShortDescription()
meth public java.lang.String toString()
meth public java.util.Enumeration<java.lang.String> attributeNames()
meth public void setDisplayName(java.lang.String)
meth public void setExpert(boolean)
meth public void setHidden(boolean)
meth public void setName(java.lang.String)
meth public void setPreferred(boolean)
meth public void setShortDescription(java.lang.String)
meth public void setValue(java.lang.String,java.lang.Object)
supr java.lang.Object

CLSS public java.beans.PropertyChangeEvent
cons public init(java.lang.Object,java.lang.String,java.lang.Object,java.lang.Object)
meth public java.lang.Object getNewValue()
meth public java.lang.Object getOldValue()
meth public java.lang.Object getPropagationId()
meth public java.lang.String getPropertyName()
meth public java.lang.String toString()
meth public void setPropagationId(java.lang.Object)
supr java.util.EventObject

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

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract java.io.InputStream
cons public init()
intf java.io.Closeable
meth public abstract int read() throws java.io.IOException
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.lang.Object

CLSS public abstract java.io.Reader
cons protected init()
cons protected init(java.lang.Object)
fld protected java.lang.Object lock
intf java.io.Closeable
intf java.lang.Readable
meth public abstract int read(char[],int,int) throws java.io.IOException
meth public abstract void close() throws java.io.IOException
meth public boolean markSupported()
meth public boolean ready() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(char[]) throws java.io.IOException
meth public int read(java.nio.CharBuffer) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void mark(int) throws java.io.IOException
meth public void reset() throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract java.lang.ClassLoader
cons protected init()
cons protected init(java.lang.ClassLoader)
meth protected final java.lang.Class<?> defineClass(byte[],int,int)
 anno 0 java.lang.Deprecated()
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int)
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int,java.security.ProtectionDomain)
meth protected final java.lang.Class<?> defineClass(java.lang.String,java.nio.ByteBuffer,java.security.ProtectionDomain)
meth protected final java.lang.Class<?> findLoadedClass(java.lang.String)
meth protected final java.lang.Class<?> findSystemClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected final void resolveClass(java.lang.Class<?>)
meth protected final void setSigners(java.lang.Class<?>,java.lang.Object[])
meth protected java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Class<?> loadClass(java.lang.String,boolean) throws java.lang.ClassNotFoundException
meth protected java.lang.Object getClassLoadingLock(java.lang.String)
meth protected java.lang.Package definePackage(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.net.URL)
meth protected java.lang.Package getPackage(java.lang.String)
meth protected java.lang.Package[] getPackages()
meth protected java.lang.String findLibrary(java.lang.String)
meth protected java.net.URL findResource(java.lang.String)
meth protected java.util.Enumeration<java.net.URL> findResources(java.lang.String) throws java.io.IOException
meth protected static boolean registerAsParallelCapable()
meth public final java.lang.ClassLoader getParent()
meth public java.io.InputStream getResourceAsStream(java.lang.String)
meth public java.lang.Class<?> loadClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.net.URL getResource(java.lang.String)
meth public java.util.Enumeration<java.net.URL> getResources(java.lang.String) throws java.io.IOException
meth public static java.io.InputStream getSystemResourceAsStream(java.lang.String)
meth public static java.lang.ClassLoader getSystemClassLoader()
meth public static java.net.URL getSystemResource(java.lang.String)
meth public static java.util.Enumeration<java.net.URL> getSystemResources(java.lang.String) throws java.io.IOException
meth public void clearAssertionStatus()
meth public void setClassAssertionStatus(java.lang.String,boolean)
meth public void setDefaultAssertionStatus(boolean)
meth public void setPackageAssertionStatus(java.lang.String,boolean)
supr java.lang.Object

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

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

CLSS public abstract interface java.lang.Readable
meth public abstract int read(java.nio.CharBuffer) throws java.io.IOException

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

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

CLSS public java.lang.UnsupportedOperationException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public java.net.URLClassLoader
cons public init(java.net.URL[])
cons public init(java.net.URL[],java.lang.ClassLoader)
cons public init(java.net.URL[],java.lang.ClassLoader,java.net.URLStreamHandlerFactory)
intf java.io.Closeable
meth protected java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Package definePackage(java.lang.String,java.util.jar.Manifest,java.net.URL)
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
meth protected void addURL(java.net.URL)
meth public java.io.InputStream getResourceAsStream(java.lang.String)
meth public java.net.URL findResource(java.lang.String)
meth public java.net.URL[] getURLs()
meth public java.util.Enumeration<java.net.URL> findResources(java.lang.String) throws java.io.IOException
meth public static java.net.URLClassLoader newInstance(java.net.URL[])
meth public static java.net.URLClassLoader newInstance(java.net.URL[],java.lang.ClassLoader)
meth public void close() throws java.io.IOException
supr java.security.SecureClassLoader

CLSS public java.security.SecureClassLoader
cons protected init()
cons protected init(java.lang.ClassLoader)
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int,java.security.CodeSource)
meth protected final java.lang.Class<?> defineClass(java.lang.String,java.nio.ByteBuffer,java.security.CodeSource)
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
supr java.lang.ClassLoader

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

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface java.util.List<%0 extends java.lang.Object>
intf java.util.Collection<{java.util.List%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.List%0})
meth public abstract boolean addAll(int,java.util.Collection<? extends {java.util.List%0}>)
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.List%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int indexOf(java.lang.Object)
meth public abstract int lastIndexOf(java.lang.Object)
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.List%0}> iterator()
meth public abstract java.util.List<{java.util.List%0}> subList(int,int)
meth public abstract java.util.ListIterator<{java.util.List%0}> listIterator()
meth public abstract java.util.ListIterator<{java.util.List%0}> listIterator(int)
meth public abstract void add(int,{java.util.List%0})
meth public abstract void clear()
meth public abstract {java.util.List%0} get(int)
meth public abstract {java.util.List%0} remove(int)
meth public abstract {java.util.List%0} set(int,{java.util.List%0})
meth public java.util.Spliterator<{java.util.List%0}> spliterator()
meth public void replaceAll(java.util.function.UnaryOperator<{java.util.List%0}>)
meth public void sort(java.util.Comparator<? super {java.util.List%0}>)

CLSS public abstract org.netbeans.modules.xml.cookies.CookieFactory
cons public init()
intf org.openide.nodes.CookieSet$Factory
meth protected abstract java.lang.Class[] supportedCookies()
meth public final void registerCookies(org.openide.nodes.CookieSet)
meth public final void unregisterCookies(org.openide.nodes.CookieSet)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.xml.cookies.UpdateDocumentCookie
intf org.openide.nodes.Node$Cookie
meth public abstract void updateDocumentRoot()

CLSS public abstract interface org.netbeans.modules.xml.sync.Representation
meth public abstract boolean isModified()
meth public abstract boolean isValid()
meth public abstract boolean represents(java.lang.Class)
meth public abstract int level()
meth public abstract java.lang.Class getUpdateClass()
meth public abstract java.lang.Object getChange(java.lang.Class)
meth public abstract java.lang.String getDisplayName()
meth public abstract void update(java.lang.Object)

CLSS public abstract org.netbeans.modules.xml.sync.SyncRepresentation
cons public init(org.netbeans.modules.xml.sync.Synchronizator)
intf org.netbeans.modules.xml.sync.Representation
meth protected final org.netbeans.modules.xml.sync.Synchronizator getSynchronizator()
meth protected final void changed(java.lang.Class)
meth public boolean isValid()
supr java.lang.Object
hfds sync

CLSS public org.netbeans.modules.xml.tax.cookies.DTDTreeRepresentation
cons public init(org.netbeans.modules.xml.tax.cookies.TreeEditorCookieImpl,org.netbeans.modules.xml.sync.Synchronizator)
meth public boolean isModified()
meth public java.lang.Object getChange(java.lang.Class)
meth public void update(java.lang.Object)
supr org.netbeans.modules.xml.tax.cookies.TreeRepresentation

CLSS public abstract interface org.netbeans.modules.xml.tax.cookies.TreeDocumentCookie
innr public abstract interface static Editor
intf org.openide.nodes.Node$Cookie
meth public abstract org.netbeans.tax.TreeDocumentRoot getDocumentRoot()

CLSS public abstract interface static org.netbeans.modules.xml.tax.cookies.TreeDocumentCookie$Editor
 outer org.netbeans.modules.xml.tax.cookies.TreeDocumentCookie
intf org.netbeans.modules.xml.tax.cookies.TreeDocumentCookie
intf org.openide.cookies.EditorCookie
meth public abstract javax.swing.text.Element treeToText(org.netbeans.tax.TreeNode)
meth public abstract org.netbeans.tax.TreeNode textToTree(javax.swing.text.Element)

CLSS public abstract interface org.netbeans.modules.xml.tax.cookies.TreeEditorCookie
fld public final static int STATUS_ERROR = 3
fld public final static int STATUS_NOT = 0
fld public final static int STATUS_OK = 1
fld public final static int STATUS_WARNING = 2
fld public final static java.lang.String PROP_DOCUMENT_ROOT = "documentRoot"
fld public final static java.lang.String PROP_STATUS = "status"
intf org.openide.nodes.Node$Cookie
meth public abstract int getStatus()
meth public abstract org.netbeans.tax.TreeDocumentRoot getDocumentRoot()
meth public abstract org.netbeans.tax.TreeDocumentRoot openDocumentRoot() throws java.io.IOException,org.netbeans.tax.TreeException
meth public abstract org.openide.util.Task prepareDocumentRoot()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public org.netbeans.modules.xml.tax.cookies.TreeEditorCookieImpl
cons public init(org.netbeans.modules.xml.XMLDataObjectLook)
innr public static CookieFactoryImpl
intf org.netbeans.modules.xml.cookies.UpdateDocumentCookie
intf org.netbeans.modules.xml.tax.cookies.TreeEditorCookie
meth public int getStatus()
meth public org.netbeans.tax.TreeDocumentRoot getDocumentRoot()
meth public org.netbeans.tax.TreeDocumentRoot openDocumentRoot() throws java.io.IOException,org.netbeans.tax.TreeException
meth public org.openide.util.Task prepareDocumentRoot()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void updateDocumentRoot()
meth public void updateTree(java.lang.Object)
supr java.lang.Object
hfds cookieMgr,oldStatus,pchs,prepareException,prepareTask,rep,status,tree,treeDocumentCookie,treeListener,treeLock,xmlDO
hcls TreeDocumentCookieImpl,TreeListener,TreeLock,TreeReference

CLSS public static org.netbeans.modules.xml.tax.cookies.TreeEditorCookieImpl$CookieFactoryImpl
 outer org.netbeans.modules.xml.tax.cookies.TreeEditorCookieImpl
cons public init(org.netbeans.modules.xml.XMLDataObjectLook)
meth public java.lang.Class[] supportedCookies()
meth public org.openide.nodes.Node$Cookie createCookie(java.lang.Class)
supr org.netbeans.modules.xml.cookies.CookieFactory
hfds dobj,editor

CLSS public abstract org.netbeans.modules.xml.tax.cookies.TreeRepresentation
cons public init(org.netbeans.modules.xml.tax.cookies.TreeEditorCookieImpl,org.netbeans.modules.xml.sync.Synchronizator)
fld protected final org.netbeans.modules.xml.tax.cookies.TreeEditorCookieImpl editor
meth public boolean isValid()
meth public boolean represents(java.lang.Class)
meth public int level()
meth public java.lang.Class getUpdateClass()
meth public java.lang.Object getChange(java.lang.Class)
meth public java.lang.String getDisplayName()
supr org.netbeans.modules.xml.sync.SyncRepresentation

CLSS public org.netbeans.modules.xml.tax.cookies.XMLTreeRepresentation
cons public init(org.netbeans.modules.xml.tax.cookies.TreeEditorCookieImpl,org.netbeans.modules.xml.sync.Synchronizator)
meth public boolean isModified()
meth public void update(java.lang.Object)
supr org.netbeans.modules.xml.tax.cookies.TreeRepresentation

CLSS public org.netbeans.modules.xml.tax.parser.DTDParsingSupport
cons public init()
meth public org.netbeans.tax.TreeDocumentRoot parse(org.xml.sax.InputSource) throws java.io.IOException,org.netbeans.tax.TreeException
supr org.netbeans.modules.xml.tax.parser.ParsingSupport

CLSS public final org.netbeans.modules.xml.tax.parser.ParserLoader
meth public java.io.InputStream getResourceAsStream(java.lang.String)
meth public java.lang.Class loadClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.net.URL getResource(java.lang.String)
meth public java.util.Enumeration findResources(java.lang.String) throws java.io.IOException
meth public static org.netbeans.modules.xml.tax.parser.ParserLoader getInstance()
meth public static void main(java.lang.String[]) throws java.lang.Exception
supr java.net.URLClassLoader
hfds CODENAME_BASE,MODULE_ARCHIVE,PARSER_PACKAGE,USER_PREFIXES,XERCES_ARCHIVE,instance,parentLoader

CLSS public abstract org.netbeans.modules.xml.tax.parser.ParsingSupport
cons public init()
meth protected org.netbeans.tax.TreeDocumentRoot parse(javax.swing.text.Document) throws java.io.IOException,org.netbeans.tax.TreeException
meth protected org.netbeans.tax.TreeDocumentRoot parse(org.netbeans.tax.TreeDocumentRoot) throws java.io.IOException,org.netbeans.tax.TreeException
meth protected org.netbeans.tax.TreeDocumentRoot parse(org.openide.filesystems.FileObject) throws java.io.IOException,org.netbeans.tax.TreeException
meth public abstract org.netbeans.tax.TreeDocumentRoot parse(org.xml.sax.InputSource) throws java.io.IOException,org.netbeans.tax.TreeException
supr java.lang.Object

CLSS public org.netbeans.modules.xml.tax.parser.TreeStreamSource
cons public init(java.lang.Class,org.xml.sax.InputSource,java.net.URL)
intf org.netbeans.tax.io.TreeInputSource
meth public org.netbeans.tax.io.TreeBuilder getBuilder()
supr java.lang.Object
hfds BUILDER_IMPL,buildClass,errorHolder,inputSource
hcls EmptyEntityResolver,EntityResolverWrapper,ErrorHolder

CLSS public org.netbeans.modules.xml.tax.parser.XMLParsingSupport
cons public init()
meth public org.netbeans.tax.TreeDocumentRoot parse(org.xml.sax.InputSource) throws java.io.IOException,org.netbeans.tax.TreeException
supr org.netbeans.modules.xml.tax.parser.ParsingSupport

CLSS public abstract org.netbeans.tax.AbstractTreeDTD
cons protected init()
cons protected init(org.netbeans.tax.AbstractTreeDTD,boolean)
innr protected abstract ChildListContentManager
meth public final java.util.Collection getAttlistDeclarations()
meth public final java.util.Collection getAttributeDeclarations(java.lang.String)
meth public final java.util.Collection getElementDeclarations()
meth public final java.util.Collection getEntityDeclarations()
meth public final java.util.Collection getNotationDeclarations()
supr org.netbeans.tax.TreeParentNode

CLSS protected abstract org.netbeans.tax.AbstractTreeDTD$ChildListContentManager
 outer org.netbeans.tax.AbstractTreeDTD
cons protected init(org.netbeans.tax.AbstractTreeDTD)
supr org.netbeans.tax.TreeParentNode$ChildListContentManager

CLSS public abstract org.netbeans.tax.AbstractUtil
cons public init()
meth protected final java.util.ResourceBundle getBundle()
meth public final boolean isLoggable()
meth public final char getChar(java.lang.String)
meth public final java.lang.String getString(java.lang.String)
meth public final java.lang.String getString(java.lang.String,java.lang.Object)
meth public final void debug(java.lang.String)
meth public final void debug(java.lang.String,java.lang.Throwable)
meth public final void debug(java.lang.Throwable)
meth public static boolean equals(java.lang.Object,java.lang.Object)
supr java.lang.Object
hfds bundle,loggable,loggableInit

CLSS public org.netbeans.tax.CannotMergeException
cons public init(org.netbeans.tax.TreeObject)
cons public init(org.netbeans.tax.TreeObject,java.lang.Exception)
supr org.netbeans.tax.TreeException
hfds serialVersionUID

CLSS public org.netbeans.tax.InvalidArgumentException
cons public init(java.lang.Exception)
cons public init(java.lang.Object,java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
meth public final java.lang.Object getArgument()
meth public final java.lang.String getMessage()
supr org.netbeans.tax.TreeException
hfds argument,serialVersionUID

CLSS public org.netbeans.tax.InvalidStateException
cons public init(java.lang.String)
supr org.netbeans.tax.TreeException
hfds serialVersionUID

CLSS public org.netbeans.tax.NotSupportedException
cons public init(java.lang.String)
supr org.netbeans.tax.TreeException
hfds serialVersionUID

CLSS public org.netbeans.tax.ReadOnlyException
cons public init(org.netbeans.tax.TreeObject)
supr org.netbeans.tax.TreeException
hfds serialVersionUID

CLSS public org.netbeans.tax.TreeAttlistDecl
cons protected init(org.netbeans.tax.TreeAttlistDecl)
cons public init(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
fld public final static java.lang.String PROP_ATTRIBUTE_DEF_MAP_ADD = "map.add"
fld public final static java.lang.String PROP_ATTRIBUTE_DEF_MAP_CONTENT = "map.content"
fld public final static java.lang.String PROP_ATTRIBUTE_DEF_MAP_REMOVE = "map.remove"
fld public final static java.lang.String PROP_ELEMENT_NAME = "elementName"
innr protected AttlistContentManager
intf org.netbeans.tax.spec.ConditionalSection$Child
intf org.netbeans.tax.spec.DTD$Child
intf org.netbeans.tax.spec.DocumentType$Child
intf org.netbeans.tax.spec.ParameterEntityReference$Child
meth protected final void checkElementName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected org.netbeans.tax.TreeNamedObjectMap$ContentManager createAttlistContentManager()
meth protected void setReadOnly(boolean)
meth public boolean equals(java.lang.Object,boolean)
meth public final java.lang.String getElementName()
meth public final org.netbeans.tax.TreeAttlistDeclAttributeDef getAttributeDef(java.lang.String)
meth public final org.netbeans.tax.TreeAttlistDeclAttributeDef removeAttributeDef(java.lang.String) throws org.netbeans.tax.ReadOnlyException
meth public final org.netbeans.tax.TreeNamedObjectMap getAttributeDefs()
meth public final void setAttributeDef(org.netbeans.tax.TreeAttlistDeclAttributeDef) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setElementName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public java.lang.Object clone()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeNodeDecl
hfds attributeDefs,elementName

CLSS protected org.netbeans.tax.TreeAttlistDecl$AttlistContentManager
 outer org.netbeans.tax.TreeAttlistDecl
cons protected init(org.netbeans.tax.TreeAttlistDecl)
meth public org.netbeans.tax.TreeNode getOwnerNode()
meth public void checkAssignableObject(java.lang.Object)
meth public void objectInserted(org.netbeans.tax.TreeObject)
meth public void objectRemoved(org.netbeans.tax.TreeObject)
meth public void orderChanged(int[])
supr org.netbeans.tax.TreeNamedObjectMap$ContentManager

CLSS public org.netbeans.tax.TreeAttlistDeclAttributeDef
cons protected init(org.netbeans.tax.TreeAttlistDeclAttributeDef)
cons public init(java.lang.String,short,java.lang.String[],short,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
fld public final static java.lang.String PROP_DEFAULT_TYPE = "defaultType"
fld public final static java.lang.String PROP_DEFAULT_VALUE = "defaultValue"
fld public final static java.lang.String PROP_ENUMERATED_TYPE = "enumeratedType"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_TYPE = "type"
fld public final static java.lang.String[] NAMED_DEFAULT_TYPE_LIST
fld public final static java.lang.String[] NAMED_TYPE_LIST
fld public final static short DEFAULT_TYPE_FIXED = 3
fld public final static short DEFAULT_TYPE_IMPLIED = 2
fld public final static short DEFAULT_TYPE_NULL = 0
fld public final static short DEFAULT_TYPE_REQUIRED = 1
fld public final static short TYPE_CDATA = 0
fld public final static short TYPE_ENTITIES = 5
fld public final static short TYPE_ENTITY = 4
fld public final static short TYPE_ENUMERATED = 8
fld public final static short TYPE_ID = 1
fld public final static short TYPE_IDREF = 2
fld public final static short TYPE_IDREFS = 3
fld public final static short TYPE_NMTOKEN = 6
fld public final static short TYPE_NMTOKENS = 7
fld public final static short TYPE_NOTATION = 9
intf org.netbeans.tax.TreeNamedObjectMap$NamedObject
meth protected final void checkDefaultType(short,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkType(short,java.lang.String[]) throws org.netbeans.tax.InvalidArgumentException
meth public boolean equals(java.lang.Object,boolean)
meth public final java.lang.String getDefaultTypeName()
meth public final java.lang.String getDefaultValue()
meth public final java.lang.String getElementName()
meth public final java.lang.String getEnumeratedTypeString()
meth public final java.lang.String getName()
meth public final java.lang.String getTypeName()
meth public final java.lang.String[] getEnumeratedType()
meth public final org.netbeans.tax.TreeAttlistDecl getOwnerAttlistDecl()
meth public final short getDefaultType()
meth public final short getType()
meth public final static java.lang.String[] createEnumeratedType(java.lang.String)
meth public final static short findDefaultType(java.lang.String)
meth public final static short findType(java.lang.String)
meth public final void removeFromContext() throws org.netbeans.tax.ReadOnlyException
meth public final void setDefaultType(short,java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setType(short,java.lang.String[]) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public java.lang.Object clone()
meth public java.lang.Object mapKey()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
meth public void setKeyListener(org.netbeans.tax.TreeNamedObjectMap$KeyListener)
supr org.netbeans.tax.TreeNodeDecl$Content
hfds defaultType,defaultValue,enumeratedType,mapKeyListener,name,type

CLSS public org.netbeans.tax.TreeAttribute
cons protected init(org.netbeans.tax.TreeAttribute)
cons public init(java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
cons public init(java.lang.String,java.lang.String,boolean) throws org.netbeans.tax.InvalidArgumentException
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_OWNER_ELEMENT = "ownerElement"
fld public final static java.lang.String PROP_SPECIFIED = "specified"
fld public final static java.lang.String PROP_VALUE = "value"
innr protected ValueListContentManager
intf org.netbeans.tax.TreeNamedObjectMap$NamedObject
intf org.netbeans.tax.spec.Element$Attribute
meth protected final void checkName(org.netbeans.tax.TreeName) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkValue(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void setOwnerElement(org.netbeans.tax.TreeElement)
meth protected org.netbeans.tax.TreeObjectList$ContentManager createValueListContentManager()
meth protected void setReadOnly(boolean)
meth public boolean equals(java.lang.Object,boolean)
meth public boolean isSpecified()
meth public final boolean isInContext()
meth public final java.lang.String getLocalName()
meth public final java.lang.String getNamespacePrefix()
meth public final java.lang.String getNamespaceURI()
meth public final java.lang.String getNonNormalizedValue()
meth public final java.lang.String getQName()
meth public final java.lang.String getValue()
meth public final org.netbeans.tax.TreeDocumentRoot getOwnerDocument()
meth public final org.netbeans.tax.TreeElement getOwnerElement()
meth public final org.netbeans.tax.TreeName getTreeName()
meth public final org.netbeans.tax.TreeNamespace getNamespace()
meth public final org.netbeans.tax.TreeObjectList getValueList()
meth public final void removeFromContext() throws org.netbeans.tax.ReadOnlyException
meth public final void setQName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setTreeName(org.netbeans.tax.TreeName) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setValue(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public java.lang.Object clone()
meth public java.lang.Object mapKey()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
meth public void setKeyListener(org.netbeans.tax.TreeNamedObjectMap$KeyListener)
supr org.netbeans.tax.TreeNode
hfds mapKeyListener,name,ownerElement,specified,valueList

CLSS protected org.netbeans.tax.TreeAttribute$ValueListContentManager
 outer org.netbeans.tax.TreeAttribute
cons protected init(org.netbeans.tax.TreeAttribute)
meth public org.netbeans.tax.TreeNode getOwnerNode()
meth public void checkAssignableObject(java.lang.Object)
meth public void objectInserted(org.netbeans.tax.TreeObject)
meth public void objectRemoved(org.netbeans.tax.TreeObject)
meth public void orderChanged(int[])
supr org.netbeans.tax.TreeObjectList$ContentManager

CLSS public org.netbeans.tax.TreeCDATASection
cons protected init(org.netbeans.tax.TreeCDATASection)
cons public init(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
intf org.netbeans.tax.TreeCharacterData
intf org.netbeans.tax.spec.DocumentFragment$Child
intf org.netbeans.tax.spec.Element$Child
intf org.netbeans.tax.spec.GeneralEntityReference$Child
meth protected final void checkData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected org.netbeans.tax.TreeData createData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public boolean equals(java.lang.Object,boolean)
meth public java.lang.Object clone()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeData

CLSS public abstract interface org.netbeans.tax.TreeCharacterData
meth public abstract java.lang.String getData()

CLSS public org.netbeans.tax.TreeCharacterReference
cons protected init(org.netbeans.tax.TreeCharacterReference)
cons public init(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
fld public final static java.lang.String PROP_NAME = "name"
intf org.netbeans.tax.TreeCharacterData
intf org.netbeans.tax.TreeReference
intf org.netbeans.tax.spec.Attribute$Value
intf org.netbeans.tax.spec.DocumentFragment$Child
intf org.netbeans.tax.spec.Element$Child
intf org.netbeans.tax.spec.GeneralEntityReference$Child
meth protected final void checkName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public boolean equals(java.lang.Object,boolean)
meth public final java.lang.String getData()
meth public final java.lang.String getName()
meth public final void setName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public java.lang.Object clone()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeChild
hfds name

CLSS public abstract org.netbeans.tax.TreeChild
cons protected init()
cons protected init(org.netbeans.tax.TreeChild)
fld public final static java.lang.String PROP_PARENT_NODE = "parentNode"
meth protected final void setParentNode(org.netbeans.tax.TreeParentNode)
meth public final boolean isDescendantOf(org.netbeans.tax.TreeParentNode)
meth public final boolean isInContext()
meth public final int index()
meth public final org.netbeans.tax.TreeChild getNextSibling()
meth public final org.netbeans.tax.TreeChild getPreviousSibling()
meth public final org.netbeans.tax.TreeDocumentRoot getOwnerDocument()
meth public final org.netbeans.tax.TreeParentNode getParentNode()
meth public final void removeFromContext() throws org.netbeans.tax.ReadOnlyException
supr org.netbeans.tax.TreeNode
hfds parentNode

CLSS public org.netbeans.tax.TreeComment
cons protected init(org.netbeans.tax.TreeComment)
cons public init(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
intf org.netbeans.tax.spec.ConditionalSection$Child
intf org.netbeans.tax.spec.DTD$Child
intf org.netbeans.tax.spec.Document$Child
intf org.netbeans.tax.spec.DocumentFragment$Child
intf org.netbeans.tax.spec.DocumentType$Child
intf org.netbeans.tax.spec.Element$Child
intf org.netbeans.tax.spec.GeneralEntityReference$Child
intf org.netbeans.tax.spec.ParameterEntityReference$Child
meth protected final void checkData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected org.netbeans.tax.TreeData createData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public boolean equals(java.lang.Object,boolean)
meth public java.lang.Object clone()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeData

CLSS public org.netbeans.tax.TreeConditionalSection
cons protected init(org.netbeans.tax.TreeConditionalSection,boolean)
cons public init(boolean)
fld public final static boolean IGNORE = false
fld public final static boolean INCLUDE = true
fld public final static java.lang.String PROP_IGNORED_CONTENT = "ignoredContent"
fld public final static java.lang.String PROP_INCLUDE = "include"
innr protected ChildListContentManager
intf org.netbeans.tax.spec.ConditionalSection$Child
intf org.netbeans.tax.spec.DTD$Child
intf org.netbeans.tax.spec.ParameterEntityReference$Child
meth protected org.netbeans.tax.TreeObjectList$ContentManager createChildListContentManager()
meth public boolean equals(java.lang.Object,boolean)
meth public final boolean isInclude()
meth public final java.lang.String getIgnoredContent()
meth public final void setIgnoredContent(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setInclude(boolean) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public java.lang.Object clone(boolean)
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.AbstractTreeDTD
hfds ignoredContent,include

CLSS protected org.netbeans.tax.TreeConditionalSection$ChildListContentManager
 outer org.netbeans.tax.TreeConditionalSection
cons protected init(org.netbeans.tax.TreeConditionalSection)
meth public org.netbeans.tax.TreeNode getOwnerNode()
meth public void checkAssignableObject(java.lang.Object)
supr org.netbeans.tax.AbstractTreeDTD$ChildListContentManager

CLSS public org.netbeans.tax.TreeDTD
cons protected init(org.netbeans.tax.TreeDTD,boolean)
cons public init() throws org.netbeans.tax.InvalidArgumentException
cons public init(java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
fld public final static java.lang.String PROP_ENCODING = "encoding"
fld public final static java.lang.String PROP_VERSION = "version"
innr protected ChildListContentManager
intf org.netbeans.tax.TreeDTDRoot
intf org.netbeans.tax.TreeDocumentRoot
meth protected final void checkEncoding(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkHeader(java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkVersion(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected org.netbeans.tax.TreeObjectList$ContentManager createChildListContentManager()
meth public boolean equals(java.lang.Object,boolean)
meth public final void setEncoding(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setHeader(java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setVersion(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public java.lang.Object clone(boolean)
meth public java.lang.String getEncoding()
meth public java.lang.String getVersion()
meth public org.netbeans.tax.event.TreeEventManager getRootEventManager()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.AbstractTreeDTD
hfds encoding,eventManager,version

CLSS protected org.netbeans.tax.TreeDTD$ChildListContentManager
 outer org.netbeans.tax.TreeDTD
cons protected init(org.netbeans.tax.TreeDTD)
meth public org.netbeans.tax.TreeNode getOwnerNode()
meth public void checkAssignableObject(java.lang.Object)
supr org.netbeans.tax.AbstractTreeDTD$ChildListContentManager

CLSS public org.netbeans.tax.TreeDTDFragment
cons protected init(org.netbeans.tax.TreeDTDFragment,boolean)
cons public init() throws org.netbeans.tax.InvalidArgumentException
innr protected ExternalDTDContentManager
meth protected org.netbeans.tax.TreeObjectList$ContentManager createChildListContentManager()
meth public java.lang.Object clone(boolean)
supr org.netbeans.tax.TreeDocumentFragment

CLSS protected org.netbeans.tax.TreeDTDFragment$ExternalDTDContentManager
 outer org.netbeans.tax.TreeDTDFragment
cons protected init(org.netbeans.tax.TreeDTDFragment)
meth public org.netbeans.tax.TreeNode getOwnerNode()
meth public void checkAssignableObject(java.lang.Object)
supr org.netbeans.tax.TreeParentNode$ChildListContentManager

CLSS public abstract interface org.netbeans.tax.TreeDTDRoot
meth public abstract java.util.Collection getAttlistDeclarations()
meth public abstract java.util.Collection getAttributeDeclarations(java.lang.String)
meth public abstract java.util.Collection getElementDeclarations()
meth public abstract java.util.Collection getEntityDeclarations()
meth public abstract java.util.Collection getNotationDeclarations()
meth public abstract org.netbeans.tax.TreeDocumentRoot getOwnerDocument()

CLSS public abstract org.netbeans.tax.TreeData
cons protected init(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
cons protected init(org.netbeans.tax.TreeData)
fld public final static java.lang.String PROP_DATA = "data"
meth protected abstract org.netbeans.tax.TreeData createData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected abstract void checkData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public boolean equals(java.lang.Object,boolean)
meth public final boolean onlyWhiteSpaces()
meth public final int getLength()
meth public final java.lang.String getData()
meth public final java.lang.String substringData(int,int) throws org.netbeans.tax.InvalidArgumentException
meth public final org.netbeans.tax.TreeData splitData(int) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void appendData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void deleteData(int,int) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void insertData(int,java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void replaceData(int,int,java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeChild
hfds data

CLSS public org.netbeans.tax.TreeDocument
cons protected init(org.netbeans.tax.TreeDocument,boolean)
cons public init() throws org.netbeans.tax.InvalidArgumentException
cons public init(java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
fld public final static java.lang.String PROP_ENCODING = "encoding"
fld public final static java.lang.String PROP_STANDALONE = "standalone"
fld public final static java.lang.String PROP_VERSION = "version"
innr protected ChildListContentManager
intf org.netbeans.tax.TreeDocumentRoot
meth protected final void checkEncoding(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkHeader(java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkStandalone(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkVersion(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected org.netbeans.tax.TreeObjectList$ContentManager createChildListContentManager()
meth public boolean equals(java.lang.Object,boolean)
meth public final java.lang.String getEncoding()
meth public final java.lang.String getStandalone()
meth public final java.lang.String getVersion()
meth public final org.netbeans.tax.TreeDocumentType getDocumentType()
meth public final org.netbeans.tax.TreeElement getDocumentElement()
meth public final org.netbeans.tax.event.TreeEventManager getRootEventManager()
meth public final void setDocumentElement(org.netbeans.tax.TreeElement) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setDocumentType(org.netbeans.tax.TreeDocumentType) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setEncoding(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setHeader(java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setStandalone(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setVersion(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public java.lang.Object clone(boolean)
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeParentNode
hfds documentType,encoding,eventManager,rootElement,standalone,version

CLSS protected org.netbeans.tax.TreeDocument$ChildListContentManager
 outer org.netbeans.tax.TreeDocument
cons protected init(org.netbeans.tax.TreeDocument)
meth public org.netbeans.tax.TreeNode getOwnerNode()
meth public void checkAssignableObject(java.lang.Object)
meth public void objectInserted(org.netbeans.tax.TreeObject)
meth public void objectRemoved(org.netbeans.tax.TreeObject)
supr org.netbeans.tax.TreeParentNode$ChildListContentManager

CLSS public org.netbeans.tax.TreeDocumentFragment
cons protected init(org.netbeans.tax.TreeDocumentFragment,boolean)
cons public init() throws org.netbeans.tax.InvalidArgumentException
cons public init(java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
fld public final static java.lang.String PROP_ENCODING = "encoding"
fld public final static java.lang.String PROP_VERSION = "version"
innr protected ChildListContentManager
intf org.netbeans.tax.TreeDocumentRoot
meth protected final void checkEncoding(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkHeader(java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkVersion(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected org.netbeans.tax.TreeObjectList$ContentManager createChildListContentManager()
meth public boolean equals(java.lang.Object,boolean)
meth public final void setEncoding(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setHeader(java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setVersion(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public java.lang.Object clone(boolean)
meth public java.lang.String getEncoding()
meth public java.lang.String getVersion()
meth public org.netbeans.tax.event.TreeEventManager getRootEventManager()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeParentNode
hfds encoding,eventManager,version

CLSS protected org.netbeans.tax.TreeDocumentFragment$ChildListContentManager
 outer org.netbeans.tax.TreeDocumentFragment
cons protected init(org.netbeans.tax.TreeDocumentFragment)
meth public org.netbeans.tax.TreeNode getOwnerNode()
meth public void checkAssignableObject(java.lang.Object)
supr org.netbeans.tax.TreeParentNode$ChildListContentManager

CLSS public abstract interface org.netbeans.tax.TreeDocumentRoot
meth public abstract java.lang.String getEncoding()
meth public abstract java.lang.String getVersion()
meth public abstract org.netbeans.tax.TreeObjectList getChildNodes()
meth public abstract org.netbeans.tax.event.TreeEventManager getRootEventManager()
meth public abstract void setEncoding(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public abstract void setVersion(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException

CLSS public org.netbeans.tax.TreeDocumentType
cons protected init(org.netbeans.tax.TreeDocumentType,boolean)
cons public init(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
cons public init(java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
fld public final static java.lang.String PROP_ELEMENT_NAME = "elementName"
fld public final static java.lang.String PROP_PUBLIC_ID = "publicId"
fld public final static java.lang.String PROP_SYSTEM_ID = "systemId"
innr protected ChildListContentManager
innr public final DTDIdentity
intf org.netbeans.tax.TreeDTDRoot
intf org.netbeans.tax.spec.Document$Child
meth protected final void checkElementName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkPublicId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkSystemId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected org.netbeans.tax.TreeObjectList$ContentManager createChildListContentManager()
meth protected void setReadOnly(boolean)
meth public boolean equals(java.lang.Object,boolean)
meth public boolean hasChildNodes(java.lang.Class,boolean)
meth public final java.lang.String getElementName()
meth public final java.lang.String getPublicId()
meth public final java.lang.String getSystemId()
meth public final org.netbeans.tax.TreeDocumentType$DTDIdentity getDTDIdentity()
meth public final org.netbeans.tax.TreeObjectList getExternalDTD()
meth public final void setElementName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setExternalDTD(org.netbeans.tax.TreeDocumentFragment)
meth public final void setPublicId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setSystemId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public java.lang.Object clone(boolean)
meth public java.util.Collection getChildNodes(java.lang.Class,boolean)
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.AbstractTreeDTD
hfds dtdIdentity,elementName,externalEntities,internalDTDText,publicId,systemId

CLSS protected org.netbeans.tax.TreeDocumentType$ChildListContentManager
 outer org.netbeans.tax.TreeDocumentType
cons protected init(org.netbeans.tax.TreeDocumentType)
meth public org.netbeans.tax.TreeNode getOwnerNode()
meth public void checkAssignableObject(java.lang.Object)
supr org.netbeans.tax.AbstractTreeDTD$ChildListContentManager

CLSS public final org.netbeans.tax.TreeDocumentType$DTDIdentity
 outer org.netbeans.tax.TreeDocumentType
meth public boolean equals(java.lang.Object)
meth public int hashCode()
supr java.lang.Object

CLSS public org.netbeans.tax.TreeElement
cons protected init(org.netbeans.tax.TreeElement,boolean)
cons public init(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
cons public init(java.lang.String,boolean) throws org.netbeans.tax.InvalidArgumentException
fld public final static java.lang.String PROP_ATTRIBUTES = "attributes"
fld public final static java.lang.String PROP_TAG_NAME = "tagName"
innr protected AttributesContentManager
innr protected ChildListContentManager
intf org.netbeans.tax.spec.Document$Child
intf org.netbeans.tax.spec.DocumentFragment$Child
intf org.netbeans.tax.spec.Element$Child
intf org.netbeans.tax.spec.GeneralEntityReference$Child
meth protected final org.netbeans.tax.TreeNamespaceContext getNamespaceContext()
meth protected final void checkTagName(org.netbeans.tax.TreeName) throws org.netbeans.tax.InvalidArgumentException
meth protected org.netbeans.tax.TreeNamedObjectMap$ContentManager createAttributesContentManager()
meth protected org.netbeans.tax.TreeObjectList$ContentManager createChildListContentManager()
meth protected void setReadOnly(boolean)
meth public boolean equals(java.lang.Object,boolean)
meth public boolean isEmpty()
meth public final boolean containsCharacterData()
meth public final boolean hasAttribute(java.lang.String)
meth public final boolean hasAttributes()
meth public final int getAttributesNumber()
meth public final java.lang.String getLocalName()
meth public final java.lang.String getNamespacePrefix()
meth public final java.lang.String getNamespaceURI()
meth public final java.lang.String getQName()
meth public final org.netbeans.tax.TreeAttribute addAttribute(java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final org.netbeans.tax.TreeAttribute getAttribute(java.lang.String)
meth public final org.netbeans.tax.TreeAttribute removeAttribute(java.lang.String) throws org.netbeans.tax.ReadOnlyException
meth public final org.netbeans.tax.TreeAttribute removeAttribute(org.netbeans.tax.TreeAttribute) throws org.netbeans.tax.ReadOnlyException
meth public final org.netbeans.tax.TreeName getTreeName()
meth public final org.netbeans.tax.TreeNamedObjectMap getAttributes()
meth public final org.netbeans.tax.TreeNamespace getNamespace()
meth public final void addAttribute(org.netbeans.tax.TreeAttribute) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void normalize() throws org.netbeans.tax.ReadOnlyException
meth public final void removeAttributes() throws org.netbeans.tax.ReadOnlyException
meth public final void setQName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setTreeName(org.netbeans.tax.TreeName) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public java.lang.Object clone(boolean)
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeParentNode
hfds attributes,containsCharacterData,empty,namespaceContext,tagName

CLSS protected org.netbeans.tax.TreeElement$AttributesContentManager
 outer org.netbeans.tax.TreeElement
cons protected init(org.netbeans.tax.TreeElement)
meth public org.netbeans.tax.TreeNode getOwnerNode()
meth public void checkAssignableObject(java.lang.Object)
meth public void objectInserted(org.netbeans.tax.TreeObject)
meth public void objectRemoved(org.netbeans.tax.TreeObject)
meth public void orderChanged(int[])
supr org.netbeans.tax.TreeNamedObjectMap$ContentManager

CLSS protected org.netbeans.tax.TreeElement$ChildListContentManager
 outer org.netbeans.tax.TreeElement
cons protected init(org.netbeans.tax.TreeElement)
meth public org.netbeans.tax.TreeNode getOwnerNode()
meth public void checkAssignableObject(java.lang.Object)
supr org.netbeans.tax.TreeParentNode$ChildListContentManager

CLSS public org.netbeans.tax.TreeElementDecl
cons protected init(org.netbeans.tax.TreeElementDecl)
cons public init(java.lang.String,org.netbeans.tax.TreeElementDecl$ContentType) throws org.netbeans.tax.InvalidArgumentException
fld public final static java.lang.String PROP_CONTENT_TYPE = "contentType"
fld public final static java.lang.String PROP_NAME = "name"
innr public abstract static ContentType
intf org.netbeans.tax.spec.ConditionalSection$Child
intf org.netbeans.tax.spec.DTD$Child
intf org.netbeans.tax.spec.DocumentType$Child
intf org.netbeans.tax.spec.ParameterEntityReference$Child
meth protected final void checkContentType(org.netbeans.tax.TreeElementDecl$ContentType) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public boolean allowElements()
meth public boolean allowText()
meth public boolean equals(java.lang.Object,boolean)
meth public boolean isEmpty()
meth public boolean isMixed()
meth public final java.lang.String getName()
meth public final org.netbeans.tax.TreeElementDecl$ContentType getContentType()
meth public final void setContentType(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setContentType(org.netbeans.tax.TreeElementDecl$ContentType) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public java.lang.Object clone()
meth public java.util.Collection getAttributeDefs()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeNodeDecl
hfds contentType,name

CLSS public abstract static org.netbeans.tax.TreeElementDecl$ContentType
 outer org.netbeans.tax.TreeElementDecl
cons protected init()
cons protected init(org.netbeans.tax.TreeElementDecl$ContentType)
cons protected init(org.netbeans.tax.TreeElementDecl)
intf java.lang.Comparable
meth public abstract boolean allowElements()
meth public abstract boolean allowText()
meth public abstract java.lang.String toString()
meth public boolean equals(java.lang.Object,boolean)
meth public boolean isMixed()
meth public final org.netbeans.tax.TreeElementDecl getOwnerElementDecl()
meth public final void removeFromContext() throws org.netbeans.tax.ReadOnlyException
meth public int compareTo(java.lang.Object)
meth public java.lang.String getMultiplicity()
meth public void addMultiplicity(java.lang.String)
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
meth public void setMultiplicity(char)
meth public void setMultiplicity(java.lang.String)
supr org.netbeans.tax.TreeNodeDecl$Content
hfds counter,index,multiplicity

CLSS public org.netbeans.tax.TreeEntityDecl
cons protected init(org.netbeans.tax.TreeEntityDecl)
cons public init(boolean,java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
cons public init(boolean,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
cons public init(java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
cons public init(java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
fld public final static boolean GENERAL_DECL = false
fld public final static boolean PARAMETER_DECL = true
fld public final static java.lang.String PROP_INTERNAL_TEXT = "internalText"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_NOTATION_NAME = "notationName"
fld public final static java.lang.String PROP_PARAMETER = "parameter"
fld public final static java.lang.String PROP_PUBLIC_ID = "publicId"
fld public final static java.lang.String PROP_SYSTEM_ID = "systemId"
fld public final static java.lang.String PROP_TYPE = "type"
fld public final static short TYPE_EXTERNAL = 2
fld public final static short TYPE_INTERNAL = 1
fld public final static short TYPE_UNPARSED = 3
intf org.netbeans.tax.spec.ConditionalSection$Child
intf org.netbeans.tax.spec.DTD$Child
intf org.netbeans.tax.spec.DocumentType$Child
intf org.netbeans.tax.spec.ParameterEntityReference$Child
meth protected final void checkExternalDecl(java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkExternalId(java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkInternalText(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkNotationName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkPublicId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkSystemId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkUnparsedDecl(java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public boolean equals(java.lang.Object,boolean)
meth public final boolean isParameter()
meth public final java.lang.String getInternalText()
meth public final java.lang.String getName()
meth public final java.lang.String getNotationName()
meth public final java.lang.String getPublicId()
meth public final java.lang.String getSystemId()
meth public final short getType()
meth public final void setExternalDecl(java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setInternalText(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setNotationName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.InvalidStateException,org.netbeans.tax.ReadOnlyException
meth public final void setParameter(boolean) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.InvalidStateException,org.netbeans.tax.ReadOnlyException
meth public final void setPublicId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.InvalidStateException,org.netbeans.tax.ReadOnlyException
meth public final void setSystemId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.InvalidStateException,org.netbeans.tax.ReadOnlyException
meth public final void setUnparsedDecl(java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.InvalidStateException,org.netbeans.tax.ReadOnlyException
meth public java.lang.Object clone()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeNodeDecl
hfds internalText,name,notationName,parameter,publicId,systemId,type

CLSS public abstract org.netbeans.tax.TreeEntityReference
cons protected init(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
cons protected init(org.netbeans.tax.TreeEntityReference,boolean)
fld public final static java.lang.String PROP_NAME = "name"
innr protected abstract ChildListContentManager
intf org.netbeans.tax.TreeReference
meth protected abstract void checkName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public boolean equals(java.lang.Object,boolean)
meth public final void setName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public java.lang.String getName()
meth public org.netbeans.tax.TreeEntityDecl getEntityDecl()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeParentNode
hfds entityDecl,name

CLSS protected abstract org.netbeans.tax.TreeEntityReference$ChildListContentManager
 outer org.netbeans.tax.TreeEntityReference
cons protected init(org.netbeans.tax.TreeEntityReference)
supr org.netbeans.tax.TreeParentNode$ChildListContentManager

CLSS public org.netbeans.tax.TreeException
cons public init(java.lang.Exception)
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
meth public java.lang.Exception getException()
supr java.lang.Exception
hfds serialVersionUID

CLSS public org.netbeans.tax.TreeGeneralEntityReference
cons protected init(org.netbeans.tax.TreeGeneralEntityReference,boolean)
cons public init(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
innr protected ChildListContentManager
intf org.netbeans.tax.spec.Attribute$Value
intf org.netbeans.tax.spec.DocumentFragment$Child
intf org.netbeans.tax.spec.Element$Child
intf org.netbeans.tax.spec.GeneralEntityReference$Child
meth protected final void checkName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected org.netbeans.tax.TreeObjectList$ContentManager createChildListContentManager()
meth public boolean equals(java.lang.Object,boolean)
meth public java.lang.Object clone(boolean)
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeEntityReference

CLSS protected org.netbeans.tax.TreeGeneralEntityReference$ChildListContentManager
 outer org.netbeans.tax.TreeGeneralEntityReference
cons protected init(org.netbeans.tax.TreeGeneralEntityReference)
meth public org.netbeans.tax.TreeNode getOwnerNode()
meth public void checkAssignableObject(java.lang.Object)
supr org.netbeans.tax.TreeEntityReference$ChildListContentManager

CLSS public final org.netbeans.tax.TreeName
cons public init(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
cons public init(java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String getPrefix()
meth public java.lang.String getQualifiedName()
meth public java.lang.String toString()
supr java.lang.Object
hfds name,prefix,rawName

CLSS public org.netbeans.tax.TreeNamedObjectMap
cons protected init(org.netbeans.tax.TreeNamedObjectMap$ContentManager)
cons protected init(org.netbeans.tax.TreeNamedObjectMap)
innr protected abstract static ContentManager
innr public KeyListener
innr public abstract interface static NamedObject
meth protected boolean addImpl(java.lang.Object)
meth protected boolean removeImpl(java.lang.Object)
meth protected int findMergeCandidate(org.netbeans.tax.TreeObject,org.netbeans.tax.TreeObject[])
meth protected java.lang.Object removeImpl(int)
meth protected java.lang.Object setImpl(int,java.lang.Object)
meth protected void addImpl(int,java.lang.Object)
meth public boolean equals(java.lang.Object,boolean)
meth public final java.lang.Object get(java.lang.Object)
meth public java.lang.Object clone()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeObjectList
hfds keyListener,map

CLSS protected abstract static org.netbeans.tax.TreeNamedObjectMap$ContentManager
 outer org.netbeans.tax.TreeNamedObjectMap
cons protected init()
meth public void checkAssignableObject(java.lang.Object)
supr org.netbeans.tax.TreeObjectList$ContentManager

CLSS public org.netbeans.tax.TreeNamedObjectMap$KeyListener
 outer org.netbeans.tax.TreeNamedObjectMap
meth public void mapKeyChanged(java.lang.Object)
supr java.lang.Object

CLSS public abstract interface static org.netbeans.tax.TreeNamedObjectMap$NamedObject
 outer org.netbeans.tax.TreeNamedObjectMap
meth public abstract java.lang.Object mapKey()
meth public abstract void setKeyListener(org.netbeans.tax.TreeNamedObjectMap$KeyListener)

CLSS public final org.netbeans.tax.TreeNamespace
cons protected init(java.lang.String,java.lang.String)
cons protected init(org.netbeans.tax.TreeNamespace)
fld public final static java.lang.String DEFAULT_NS_PREFIX = ""
fld public final static org.netbeans.tax.TreeNamespace NO_NAMESPACE
fld public final static org.netbeans.tax.TreeNamespace XMLNS_NAMESPACE
fld public final static org.netbeans.tax.TreeNamespace XML_NAMESPACE
meth public java.lang.String getPrefix()
meth public java.lang.String getURI()
supr java.lang.Object
hfds prefix,uri

CLSS public org.netbeans.tax.TreeNamespaceContext
cons protected init(org.netbeans.tax.TreeElement)
meth public java.lang.String getURI(java.lang.String)
supr java.lang.Object
hfds definedNS,element

CLSS public abstract org.netbeans.tax.TreeNode
cons protected init()
cons protected init(org.netbeans.tax.TreeNode)
fld public final static java.lang.String PROP_NODE = "this"
meth public abstract org.netbeans.tax.TreeDocumentRoot getOwnerDocument()
meth public final org.netbeans.tax.event.TreeEventManager getEventManager()
supr org.netbeans.tax.TreeObject

CLSS public abstract org.netbeans.tax.TreeNodeDecl
cons protected init()
cons protected init(org.netbeans.tax.TreeNodeDecl)
innr protected static TokenList
innr public abstract static Content
meth public final org.netbeans.tax.TreeDTDRoot getOwnerDTD()
supr org.netbeans.tax.TreeChild

CLSS public abstract static org.netbeans.tax.TreeNodeDecl$Content
 outer org.netbeans.tax.TreeNodeDecl
cons protected init()
cons protected init(org.netbeans.tax.TreeNodeDecl$Content)
cons protected init(org.netbeans.tax.TreeNodeDecl)
meth protected void setNodeDecl(org.netbeans.tax.TreeNodeDecl)
meth public final boolean isInContext()
meth public final org.netbeans.tax.TreeNodeDecl getNodeDecl()
meth public final org.netbeans.tax.event.TreeEventManager getEventManager()
supr org.netbeans.tax.TreeObject
hfds nodeDecl

CLSS protected static org.netbeans.tax.TreeNodeDecl$TokenList
 outer org.netbeans.tax.TreeNodeDecl
cons public init()
meth public int size()
meth public void add(java.lang.Object)
meth public void remove(java.lang.Object)
supr java.lang.Object
hfds tokenList

CLSS public org.netbeans.tax.TreeNotationDecl
cons protected init(org.netbeans.tax.TreeNotationDecl)
cons public init(java.lang.String,java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_PUBLIC_ID = "publicId"
fld public final static java.lang.String PROP_SYSTEM_ID = "systemId"
intf org.netbeans.tax.spec.ConditionalSection$Child
intf org.netbeans.tax.spec.DTD$Child
intf org.netbeans.tax.spec.DocumentType$Child
intf org.netbeans.tax.spec.ParameterEntityReference$Child
meth protected final void checkExternalId(java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkPublicId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkSystemId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public boolean equals(java.lang.Object,boolean)
meth public final void setName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setPublicId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void setSystemId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public java.lang.Object clone()
meth public java.lang.String getName()
meth public java.lang.String getPublicId()
meth public java.lang.String getSystemId()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeNodeDecl
hfds name,publicId,systemId

CLSS public abstract org.netbeans.tax.TreeObject
cons protected init()
cons protected init(org.netbeans.tax.TreeObject)
fld public final static java.lang.String PROP_READ_ONLY = "readOnly"
intf org.netbeans.tax.event.TreeEventModel
meth protected final boolean isInstance(java.lang.Object)
meth protected final org.netbeans.tax.event.TreeEventChangeSupport getEventChangeSupport()
meth protected final void bubblePropertyChange(org.netbeans.tax.event.TreeEvent)
meth protected final void checkMergeObject(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
meth protected final void checkReadOnly() throws org.netbeans.tax.ReadOnlyException
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void firePropertyChange(org.netbeans.tax.event.TreeEvent)
meth protected void setReadOnly(boolean)
meth public abstract boolean isInContext()
meth public abstract java.lang.Object clone()
meth public abstract org.netbeans.tax.event.TreeEventManager getEventManager()
meth public abstract void removeFromContext() throws org.netbeans.tax.ReadOnlyException
meth public boolean equals(java.lang.Object)
meth public boolean equals(java.lang.Object,boolean)
meth public final boolean hasPropertyChangeListeners(java.lang.String)
meth public final boolean isReadOnly()
meth public final java.lang.String listListeners()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public final void addReadonlyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public final void removeReadonlyChangeListener(java.beans.PropertyChangeListener)
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr java.lang.Object
hfds eventChangeSupport,readOnly

CLSS public org.netbeans.tax.TreeObjectList
cons protected init(org.netbeans.tax.TreeObjectList$ContentManager)
cons protected init(org.netbeans.tax.TreeObjectList)
fld public final static java.lang.String PROP_CONTENT_INSERT = "contentInsert"
fld public final static java.lang.String PROP_CONTENT_ORDER = "contentOrder"
fld public final static java.lang.String PROP_CONTENT_REMOVE = "contentRemove"
innr public abstract static ContentManager
intf java.util.List
intf org.netbeans.tax.event.TreeNodeContentEventModel
meth protected boolean addImpl(java.lang.Object)
meth protected boolean removeImpl(java.lang.Object)
meth protected final boolean equals(int[])
meth protected final org.netbeans.tax.TreeNode contentManagerGetOwnerNode()
meth protected final void checkReorder(int[]) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkUnsupportedOperation()
meth protected final void contentManagerCheckAssignableObject(java.lang.Object)
meth protected final void contentManagerObjectInserted(org.netbeans.tax.TreeObject)
meth protected final void contentManagerObjectRemoved(org.netbeans.tax.TreeObject)
meth protected final void contentManagerOrderChanged(int[])
meth protected int findMergeCandidate(org.netbeans.tax.TreeObject,org.netbeans.tax.TreeObject[])
meth protected java.lang.Object removeImpl(int)
meth protected java.lang.Object setImpl(int,java.lang.Object)
meth protected void addImpl(int,java.lang.Object)
meth protected void setReadOnly(boolean)
meth public boolean equals(java.lang.Object,boolean)
meth public final boolean add(java.lang.Object)
meth public final boolean addAll(int,java.util.Collection)
meth public final boolean addAll(java.util.Collection)
meth public final boolean contains(java.lang.Object)
meth public final boolean containsAll(java.util.Collection)
meth public final boolean equals(java.lang.Object)
meth public final boolean hasContentChangeListeners()
meth public final boolean isAssignableObject(java.lang.Object)
meth public final boolean isEmpty()
meth public final boolean isInContext()
meth public final boolean remove(java.lang.Object)
meth public final boolean removeAll(java.util.Collection)
meth public final boolean retainAll(java.util.Collection)
meth public final int hashCode()
meth public final int indexOf(java.lang.Object)
meth public final int lastIndexOf(java.lang.Object)
meth public final int size()
meth public final java.lang.Object get(int)
meth public final java.lang.Object remove(int)
meth public final java.lang.Object set(int,java.lang.Object)
meth public final java.lang.Object[] toArray()
meth public final java.lang.Object[] toArray(java.lang.Object[])
meth public final java.util.Iterator iterator()
meth public final java.util.List subList(int,int)
meth public final java.util.ListIterator listIterator()
meth public final java.util.ListIterator listIterator(int)
meth public final org.netbeans.tax.TreeObjectList$ContentManager getContentManager()
meth public final org.netbeans.tax.event.TreeEventManager getEventManager()
meth public final void add(int,java.lang.Object)
meth public final void addContentChangeListener(java.beans.PropertyChangeListener)
meth public final void clear()
meth public final void firePropertyInsert(org.netbeans.tax.TreeObject)
meth public final void firePropertyOrder(int[])
meth public final void firePropertyRemove(org.netbeans.tax.TreeObject)
meth public final void removeContentChangeListener(java.beans.PropertyChangeListener)
meth public final void removeFromContext() throws org.netbeans.tax.ReadOnlyException
meth public final void reorder(int[]) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public final void switchObjects(int,int) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeObject
hfds contentManager,list

CLSS public abstract static org.netbeans.tax.TreeObjectList$ContentManager
 outer org.netbeans.tax.TreeObjectList
cons public init()
meth protected final void checkAssignableClass(java.lang.Class,java.lang.Object)
meth public abstract org.netbeans.tax.TreeNode getOwnerNode()
meth public abstract void objectInserted(org.netbeans.tax.TreeObject)
meth public abstract void objectRemoved(org.netbeans.tax.TreeObject)
meth public abstract void orderChanged(int[])
meth public void checkAssignableObject(java.lang.Object)
supr java.lang.Object

CLSS public org.netbeans.tax.TreeParameterEntityReference
cons protected init(org.netbeans.tax.TreeParameterEntityReference,boolean)
cons public init(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
innr protected ChildListContentManager
intf org.netbeans.tax.spec.ConditionalSection$Child
intf org.netbeans.tax.spec.DTD$Child
intf org.netbeans.tax.spec.DocumentType$Child
intf org.netbeans.tax.spec.ParameterEntityReference$Child
meth protected final void checkName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected org.netbeans.tax.TreeObjectList$ContentManager createChildListContentManager()
meth public boolean equals(java.lang.Object,boolean)
meth public java.lang.Object clone(boolean)
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeEntityReference

CLSS protected org.netbeans.tax.TreeParameterEntityReference$ChildListContentManager
 outer org.netbeans.tax.TreeParameterEntityReference
cons protected init(org.netbeans.tax.TreeParameterEntityReference)
meth public org.netbeans.tax.TreeNode getOwnerNode()
meth public void checkAssignableObject(java.lang.Object)
supr org.netbeans.tax.TreeEntityReference$ChildListContentManager

CLSS public abstract org.netbeans.tax.TreeParentNode
cons protected init()
cons protected init(org.netbeans.tax.TreeParentNode,boolean)
fld public final static java.lang.String PROP_CHILD_LIST = "childList"
innr protected abstract ChildListContentManager
meth protected abstract org.netbeans.tax.TreeObjectList$ContentManager createChildListContentManager()
meth protected void setReadOnly(boolean)
meth public abstract java.lang.Object clone(boolean)
meth public boolean equals(java.lang.Object,boolean)
meth public boolean hasChildNodes(java.lang.Class,boolean)
meth public boolean isAssignableChild(org.netbeans.tax.TreeChild)
meth public final boolean hasChildNodes()
meth public final boolean hasChildNodes(java.lang.Class)
meth public final int getChildrenNumber()
meth public final int indexOf(org.netbeans.tax.TreeChild)
meth public final java.lang.Object clone()
meth public final java.util.Collection getChildNodes(java.lang.Class)
meth public final org.netbeans.tax.TreeChild getFirstChild()
meth public final org.netbeans.tax.TreeChild getLastChild()
meth public final org.netbeans.tax.TreeChild item(int)
meth public final org.netbeans.tax.TreeObjectList getChildNodes()
meth public final void appendChild(org.netbeans.tax.TreeChild) throws org.netbeans.tax.ReadOnlyException
meth public final void insertBefore(org.netbeans.tax.TreeChild,org.netbeans.tax.TreeChild) throws org.netbeans.tax.ReadOnlyException
meth public final void insertChildAt(org.netbeans.tax.TreeChild,int) throws org.netbeans.tax.ReadOnlyException
meth public final void removeChild(org.netbeans.tax.TreeChild) throws org.netbeans.tax.ReadOnlyException
meth public final void replaceChild(org.netbeans.tax.TreeChild,org.netbeans.tax.TreeChild) throws org.netbeans.tax.ReadOnlyException
meth public java.util.Collection getChildNodes(java.lang.Class,boolean)
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeChild
hfds childList

CLSS protected abstract org.netbeans.tax.TreeParentNode$ChildListContentManager
 outer org.netbeans.tax.TreeParentNode
cons protected init(org.netbeans.tax.TreeParentNode)
meth public void checkAssignableObject(java.lang.Object)
meth public void objectInserted(org.netbeans.tax.TreeObject)
meth public void objectRemoved(org.netbeans.tax.TreeObject)
meth public void orderChanged(int[])
supr org.netbeans.tax.TreeObjectList$ContentManager

CLSS public org.netbeans.tax.TreeProcessingInstruction
cons protected init(org.netbeans.tax.TreeProcessingInstruction)
cons public init(java.lang.String,java.lang.String) throws org.netbeans.tax.InvalidArgumentException
fld public final static java.lang.String PROP_TARGET = "target"
intf org.netbeans.tax.spec.ConditionalSection$Child
intf org.netbeans.tax.spec.DTD$Child
intf org.netbeans.tax.spec.Document$Child
intf org.netbeans.tax.spec.DocumentFragment$Child
intf org.netbeans.tax.spec.DocumentType$Child
intf org.netbeans.tax.spec.Element$Child
intf org.netbeans.tax.spec.GeneralEntityReference$Child
intf org.netbeans.tax.spec.ParameterEntityReference$Child
meth protected final void checkData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected org.netbeans.tax.TreeData createData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public boolean equals(java.lang.Object,boolean)
meth public final java.lang.String getTarget()
meth public final void checkTarget(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final void setTarget(java.lang.String) throws org.netbeans.tax.InvalidArgumentException,org.netbeans.tax.ReadOnlyException
meth public java.lang.Object clone()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeData
hfds target

CLSS public abstract interface org.netbeans.tax.TreeReference
meth public abstract java.lang.String getName()

CLSS public org.netbeans.tax.TreeText
cons protected init(org.netbeans.tax.TreeText)
cons public init(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
intf org.netbeans.tax.TreeCharacterData
intf org.netbeans.tax.spec.Attribute$Value
intf org.netbeans.tax.spec.DocumentFragment$Child
intf org.netbeans.tax.spec.Element$Child
intf org.netbeans.tax.spec.GeneralEntityReference$Child
meth protected final org.netbeans.tax.TreeData createData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth protected final void checkData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public boolean equals(java.lang.Object,boolean)
meth public java.lang.Object clone()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
supr org.netbeans.tax.TreeData

CLSS public org.netbeans.tax.TreeUnsupportedOperationException
cons public init(org.netbeans.tax.TreeException)
meth public org.netbeans.tax.TreeException getException()
supr java.lang.UnsupportedOperationException
hfds serialVersionUID

CLSS public final org.netbeans.tax.TreeUtilities
cons public init()
meth public final static boolean isValidAttlistDeclAttributeDefaultType(short)
meth public final static boolean isValidAttlistDeclAttributeDefaultValue(java.lang.String)
meth public final static boolean isValidAttlistDeclAttributeEnumeratedType(java.lang.String[])
meth public final static boolean isValidAttlistDeclAttributeName(java.lang.String)
meth public final static boolean isValidAttlistDeclAttributeType(short)
meth public final static boolean isValidAttlistDeclElementName(java.lang.String)
meth public final static boolean isValidAttributeName(org.netbeans.tax.TreeName)
meth public final static boolean isValidAttributeValue(java.lang.String)
meth public final static boolean isValidCDATASectionData(java.lang.String)
meth public final static boolean isValidCharacterReferenceName(java.lang.String)
meth public final static boolean isValidCommentData(java.lang.String)
meth public final static boolean isValidDTDEncoding(java.lang.String)
meth public final static boolean isValidDTDVersion(java.lang.String)
meth public final static boolean isValidDocumentEncoding(java.lang.String)
meth public final static boolean isValidDocumentFragmentEncoding(java.lang.String)
meth public final static boolean isValidDocumentFragmentVersion(java.lang.String)
meth public final static boolean isValidDocumentStandalone(java.lang.String)
meth public final static boolean isValidDocumentTypeElementName(java.lang.String)
meth public final static boolean isValidDocumentTypePublicId(java.lang.String)
meth public final static boolean isValidDocumentTypeSystemId(java.lang.String)
meth public final static boolean isValidDocumentVersion(java.lang.String)
meth public final static boolean isValidElementDeclContentType(org.netbeans.tax.TreeElementDecl$ContentType)
meth public final static boolean isValidElementDeclName(java.lang.String)
meth public final static boolean isValidElementTagName(org.netbeans.tax.TreeName)
meth public final static boolean isValidEntityDeclInternalText(java.lang.String)
meth public final static boolean isValidEntityDeclName(java.lang.String)
meth public final static boolean isValidEntityDeclNotationName(java.lang.String)
meth public final static boolean isValidEntityDeclPublicId(java.lang.String)
meth public final static boolean isValidEntityDeclSystemId(java.lang.String)
meth public final static boolean isValidGeneralEntityReferenceName(java.lang.String)
meth public final static boolean isValidNotationDeclName(java.lang.String)
meth public final static boolean isValidNotationDeclPublicId(java.lang.String)
meth public final static boolean isValidNotationDeclSystemId(java.lang.String)
meth public final static boolean isValidParameterEntityReferenceName(java.lang.String)
meth public final static boolean isValidProcessingInstructionData(java.lang.String)
meth public final static boolean isValidProcessingInstructionTarget(java.lang.String)
meth public final static boolean isValidTextData(java.lang.String)
meth public final static java.lang.String iana2java(java.lang.String)
meth public final static java.util.Collection getSupportedEncodings()
meth public final static void checkAttlistDeclAttributeDefaultType(short) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkAttlistDeclAttributeDefaultValue(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkAttlistDeclAttributeEnumeratedType(java.lang.String[]) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkAttlistDeclAttributeName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkAttlistDeclAttributeType(short) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkAttlistDeclElementName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkAttributeName(org.netbeans.tax.TreeName) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkAttributeValue(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkCDATASectionData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkCharacterReferenceName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkCommentData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkDTDEncoding(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkDTDVersion(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkDocumentEncoding(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkDocumentFragmentEncoding(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkDocumentFragmentVersion(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkDocumentStandalone(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkDocumentTypeElementName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkDocumentTypePublicId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkDocumentTypeSystemId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkDocumentVersion(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkElementDeclContentType(org.netbeans.tax.TreeElementDecl$ContentType) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkElementDeclName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkElementTagName(org.netbeans.tax.TreeName) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkEntityDeclInternalText(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkEntityDeclName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkEntityDeclNotationName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkEntityDeclPublicId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkEntityDeclSystemId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkGeneralEntityReferenceName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkNotationDeclName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkNotationDeclPublicId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkNotationDeclSystemId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkParameterEntityReferenceName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkProcessingInstructionData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkProcessingInstructionTarget(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public final static void checkTextData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
supr java.lang.Object
hfds constraints
hcls Constraints,EncodingUtil

CLSS public org.netbeans.tax.UnicodeClasses
meth public static boolean isXMLBaseChar(int)
meth public static boolean isXMLChar(int)
meth public static boolean isXMLCombiningChar(int)
meth public static boolean isXMLDigit(int)
meth public static boolean isXMLExtender(int)
meth public static boolean isXMLIdeographic(int)
meth public static boolean isXMLLetter(int)
meth public static boolean isXMLNCNameChar(int)
meth public static boolean isXMLNCNameStartChar(int)
meth public static boolean isXMLNameChar(int)
meth public static boolean isXMLNameStartChar(int)
meth public static boolean isXMLPubidLiteral(char)
supr java.lang.Object

CLSS public org.netbeans.tax.decl.ANYType
cons public init()
cons public init(org.netbeans.tax.decl.ANYType)
meth public boolean allowElements()
meth public boolean allowText()
meth public java.lang.Object clone()
meth public java.lang.String getName()
meth public java.lang.String toString()
supr org.netbeans.tax.decl.LeafType

CLSS public org.netbeans.tax.decl.ANYTypeBeanInfo
cons public init()
meth public java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public java.beans.MethodDescriptor[] getMethodDescriptors()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public abstract org.netbeans.tax.decl.ChildrenType
cons public init()
cons public init(java.util.Collection)
cons public init(org.netbeans.tax.decl.ChildrenType)
fld protected java.util.List context
intf org.netbeans.tax.decl.TypeCollection
meth protected final void initNodeDecl()
meth protected void setNodeDecl(org.netbeans.tax.TreeNodeDecl)
meth public abstract java.lang.String getSeparator()
meth public boolean allowElements()
meth public boolean allowText()
meth public boolean hasChildren()
meth public java.util.Collection getTypes()
meth public void addType(org.netbeans.tax.TreeElementDecl$ContentType)
meth public void addTypes(java.util.Collection)
supr org.netbeans.tax.TreeElementDecl$ContentType

CLSS public org.netbeans.tax.decl.ChoiceType
cons public init()
cons public init(java.util.Collection)
cons public init(org.netbeans.tax.decl.ChoiceType)
meth public java.lang.Object clone()
meth public java.lang.String getName()
meth public java.lang.String getSeparator()
meth public java.lang.String toString()
supr org.netbeans.tax.decl.ChildrenType

CLSS public org.netbeans.tax.decl.ChoiceTypeBeanInfo
cons public init()
meth public java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public java.beans.MethodDescriptor[] getMethodDescriptors()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.tax.decl.EMPTYType
cons public init()
cons public init(org.netbeans.tax.decl.EMPTYType)
meth public java.lang.Object clone()
meth public java.lang.String getName()
meth public java.lang.String toString()
supr org.netbeans.tax.decl.LeafType

CLSS public org.netbeans.tax.decl.EMPTYTypeBeanInfo
cons public init()
meth public java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public java.beans.MethodDescriptor[] getMethodDescriptors()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public abstract org.netbeans.tax.decl.LeafType
cons public init()
cons public init(org.netbeans.tax.decl.LeafType)
meth public abstract java.lang.String getName()
meth public boolean allowElements()
meth public boolean allowText()
supr org.netbeans.tax.TreeElementDecl$ContentType

CLSS public org.netbeans.tax.decl.MixedType
cons public init()
cons public init(java.util.Collection)
cons public init(org.netbeans.tax.decl.MixedType)
meth public boolean allowElements()
meth public boolean allowText()
meth public java.lang.Object clone()
meth public java.lang.String getName()
meth public java.lang.String toString()
supr org.netbeans.tax.decl.ChoiceType

CLSS public org.netbeans.tax.decl.MixedTypeBeanInfo
cons public init()
meth public java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public java.beans.MethodDescriptor[] getMethodDescriptors()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.tax.decl.NameType
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
cons public init(org.netbeans.tax.decl.NameType)
fld public final static java.lang.String PROP_TYPE_NAME = "nt-name"
meth public boolean allowElements()
meth public boolean allowText()
meth public boolean equals(java.lang.Object,boolean)
meth public java.lang.Object clone()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public void merge(org.netbeans.tax.TreeObject) throws org.netbeans.tax.CannotMergeException
meth public void setName(java.lang.String)
supr org.netbeans.tax.decl.LeafType
hfds name

CLSS public org.netbeans.tax.decl.NameTypeBeanInfo
cons public init()
meth public java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public java.beans.MethodDescriptor[] getMethodDescriptors()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public org.netbeans.tax.decl.SequenceType
cons public init()
cons public init(java.util.Collection)
cons public init(org.netbeans.tax.decl.SequenceType)
meth public java.lang.Object clone()
meth public java.lang.String getName()
meth public java.lang.String getSeparator()
meth public java.lang.String toString()
supr org.netbeans.tax.decl.ChildrenType

CLSS public org.netbeans.tax.decl.SequenceTypeBeanInfo
cons public init()
meth public java.beans.EventSetDescriptor[] getEventSetDescriptors()
meth public java.beans.MethodDescriptor[] getMethodDescriptors()
meth public java.beans.PropertyDescriptor[] getPropertyDescriptors()
supr java.beans.SimpleBeanInfo

CLSS public abstract interface org.netbeans.tax.decl.TypeCollection
meth public abstract java.util.Collection getTypes()

CLSS public org.netbeans.tax.event.TreeEvent
cons public init(org.netbeans.tax.TreeObject,java.lang.String,java.lang.Object,java.lang.Object)
meth public final boolean isBubbling()
meth public final java.lang.String getOriginalPropertyName()
meth public final org.netbeans.tax.TreeObject getOriginalSource()
meth public final org.netbeans.tax.event.TreeEvent createBubbling(org.netbeans.tax.TreeNode)
supr java.beans.PropertyChangeEvent
hfds bubbling,originalPropertyName,originalSource,serialVersionUID

CLSS public final org.netbeans.tax.event.TreeEventChangeSupport
cons public init(org.netbeans.tax.TreeObject)
meth protected final org.netbeans.tax.TreeObject getEventSource()
meth protected final void clearPropertyChangeCache()
meth protected final void firePropertyChangeCache()
meth protected final void firePropertyChangeLater(org.netbeans.tax.event.TreeEvent)
meth protected final void firePropertyChangeNow(org.netbeans.tax.event.TreeEvent)
meth public final boolean hasPropertyChangeListeners(java.lang.String)
meth public final java.lang.String listListeners()
meth public final org.netbeans.tax.event.TreeEvent createEvent(java.lang.String,java.lang.Object,java.lang.Object)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public final void firePropertyChange(org.netbeans.tax.event.TreeEvent)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
supr java.lang.Object
hfds eventCache,eventSource,propertyChangeSupport
hcls EventCache

CLSS public final org.netbeans.tax.event.TreeEventManager
cons public init()
cons public init(org.netbeans.tax.event.TreeEventManager)
cons public init(short)
fld public final static short FIRE_LATER = 2
fld public final static short FIRE_NOW = 1
meth public final short getFirePolicy()
meth public final void firePropertyChange(org.netbeans.tax.event.TreeEventChangeSupport,org.netbeans.tax.event.TreeEvent)
meth public final void setFirePolicy(short)
supr java.lang.Object
hfds cachedSupports,firePolicy

CLSS public abstract interface org.netbeans.tax.event.TreeEventModel
meth public abstract boolean hasPropertyChangeListeners(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)

CLSS public abstract interface org.netbeans.tax.event.TreeNodeContentEventModel
intf org.netbeans.tax.event.TreeEventModel
meth public abstract boolean hasContentChangeListeners()
meth public abstract void addContentChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removeContentChangeListener(java.beans.PropertyChangeListener)

CLSS public final org.netbeans.tax.io.Convertors
cons public init()
meth public static byte[] treeToByteArray(org.netbeans.tax.TreeDocumentRoot) throws java.io.IOException
meth public static java.lang.String documentToString(javax.swing.text.Document)
meth public static java.lang.String readerToString(java.io.Reader) throws java.io.IOException
meth public static java.lang.String treeToString(org.netbeans.tax.TreeDocumentRoot) throws java.io.IOException
meth public static org.xml.sax.InputSource documentToInputSource(javax.swing.text.Document)
supr java.lang.Object

CLSS public org.netbeans.tax.io.RememberingReader
cons public init(java.io.Reader)
meth public int read(char[],int,int) throws java.io.IOException
meth public java.lang.StringBuffer stopRemembering()
meth public void close() throws java.io.IOException
meth public void startRemembering()
supr java.io.Reader
hfds memory,peer

CLSS public final org.netbeans.tax.io.StringUtil
cons public init()
meth public static boolean isWS(char)
meth public static int skipDelimited(java.lang.String,int,char,char,java.lang.String)
meth public static int skipDelimited(java.lang.String,int,java.lang.String,java.lang.String)
meth public static int skipWS(java.lang.String,int)
meth public static void main(java.lang.String[])
supr java.lang.Object

CLSS public abstract interface org.netbeans.tax.io.TreeBuilder
meth public abstract org.netbeans.tax.TreeDocumentRoot buildDocument() throws org.netbeans.tax.TreeException

CLSS public final org.netbeans.tax.io.TreeEntityManager
cons public init()
meth public java.lang.String expandSystemId(java.lang.String)
meth public java.lang.String expandSystemId(java.lang.String,java.lang.String)
meth public org.netbeans.tax.io.TreeInputSource resolveEntity(java.lang.String,java.lang.String)
meth public org.netbeans.tax.io.TreeInputSource resolveEntity(java.lang.String,java.lang.String,java.lang.String)
meth public void addEntityResolver(org.netbeans.tax.io.TreeEntityResolver)
meth public void removeEntityResolver(org.netbeans.tax.io.TreeEntityResolver)
supr org.netbeans.tax.io.TreeEntityResolver

CLSS public abstract org.netbeans.tax.io.TreeEntityResolver
cons public init()
meth public abstract org.netbeans.tax.io.TreeInputSource resolveEntity(java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String expandSystemId(java.lang.String)
meth public java.lang.String expandSystemId(java.lang.String,java.lang.String)
meth public org.netbeans.tax.io.TreeInputSource resolveEntity(java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.netbeans.tax.io.TreeIOFilter
intf org.netbeans.tax.io.TreeInputSource
intf org.netbeans.tax.io.TreeOutputResult

CLSS public abstract interface org.netbeans.tax.io.TreeInputSource
meth public abstract org.netbeans.tax.io.TreeBuilder getBuilder()

CLSS public org.netbeans.tax.io.TreeInputStream
cons public init(org.netbeans.tax.TreeDocumentRoot) throws java.io.IOException
meth public int read() throws java.io.IOException
meth public void close() throws java.io.IOException
supr java.io.InputStream
hfds input

CLSS public abstract interface org.netbeans.tax.io.TreeOutputResult
meth public abstract org.netbeans.tax.io.TreeWriter getWriter(org.netbeans.tax.TreeDocumentRoot)

CLSS public org.netbeans.tax.io.TreeReader
cons public init(org.netbeans.tax.TreeDocumentRoot) throws java.io.IOException
meth public int read(char[],int,int) throws java.io.IOException
meth public void close() throws java.io.IOException
supr java.io.Reader
hfds reader

CLSS public abstract interface org.netbeans.tax.io.TreeStreamBuilderErrorHandler
fld public final static int ERROR_ERROR = 1
fld public final static int ERROR_FATAL_ERROR = 2
fld public final static int ERROR_WARNING = 0
fld public final static java.lang.String[] ERROR_NAME
meth public abstract void message(int,org.xml.sax.SAXParseException)

CLSS public org.netbeans.tax.io.TreeStreamResult
cons public init(java.io.OutputStream)
cons public init(java.io.PipedWriter)
cons public init(java.io.StringWriter)
innr public final static TreeStreamWriter
intf org.netbeans.tax.io.TreeOutputResult
meth public final org.netbeans.tax.io.TreeWriter getWriter(org.netbeans.tax.TreeDocumentRoot)
supr java.lang.Object
hfds writer

CLSS public final static org.netbeans.tax.io.TreeStreamResult$TreeStreamWriter
 outer org.netbeans.tax.io.TreeStreamResult
cons public init(java.io.OutputStream)
cons public init(java.io.PipedWriter)
cons public init(java.io.StringWriter)
intf org.netbeans.tax.io.TreeWriter
intf org.netbeans.tax.spec.AttlistDecl$Writer
intf org.netbeans.tax.spec.Attribute$Writer
intf org.netbeans.tax.spec.CDATASection$Writer
intf org.netbeans.tax.spec.CharacterReference$Writer
intf org.netbeans.tax.spec.Comment$Writer
intf org.netbeans.tax.spec.ConditionalSection$Writer
intf org.netbeans.tax.spec.DTD$Writer
intf org.netbeans.tax.spec.Document$Writer
intf org.netbeans.tax.spec.DocumentFragment$Writer
intf org.netbeans.tax.spec.DocumentType$Writer
intf org.netbeans.tax.spec.Element$Writer
intf org.netbeans.tax.spec.ElementDecl$Writer
intf org.netbeans.tax.spec.EntityDecl$Writer
intf org.netbeans.tax.spec.GeneralEntityReference$Writer
intf org.netbeans.tax.spec.NotationDecl$Writer
intf org.netbeans.tax.spec.ParameterEntityReference$Writer
intf org.netbeans.tax.spec.ProcessingInstruction$Writer
intf org.netbeans.tax.spec.Text$Writer
meth public java.io.OutputStream getOutputStream()
meth public java.io.Writer getWriter()
meth public void setDocument(org.netbeans.tax.TreeDocumentRoot)
meth public void writeAttlistDecl(org.netbeans.tax.TreeAttlistDecl) throws org.netbeans.tax.TreeException
meth public void writeAttribute(org.netbeans.tax.TreeAttribute) throws org.netbeans.tax.TreeException
meth public void writeCDATASection(org.netbeans.tax.TreeCDATASection) throws org.netbeans.tax.TreeException
meth public void writeCharacterReference(org.netbeans.tax.TreeCharacterReference) throws org.netbeans.tax.TreeException
meth public void writeComment(org.netbeans.tax.TreeComment) throws org.netbeans.tax.TreeException
meth public void writeConditionalSection(org.netbeans.tax.TreeConditionalSection) throws org.netbeans.tax.TreeException
meth public void writeDTD(org.netbeans.tax.TreeDTD) throws org.netbeans.tax.TreeException
meth public void writeDocument() throws org.netbeans.tax.TreeException
meth public void writeDocument(org.netbeans.tax.TreeDocument) throws org.netbeans.tax.TreeException
meth public void writeDocumentFragment(org.netbeans.tax.TreeDocumentFragment) throws org.netbeans.tax.TreeException
meth public void writeDocumentType(org.netbeans.tax.TreeDocumentType) throws org.netbeans.tax.TreeException
meth public void writeElement(org.netbeans.tax.TreeElement) throws org.netbeans.tax.TreeException
meth public void writeElementDecl(org.netbeans.tax.TreeElementDecl) throws org.netbeans.tax.TreeException
meth public void writeEntityDecl(org.netbeans.tax.TreeEntityDecl) throws org.netbeans.tax.TreeException
meth public void writeGeneralEntityReference(org.netbeans.tax.TreeGeneralEntityReference) throws org.netbeans.tax.TreeException
meth public void writeNode(org.netbeans.tax.TreeNode) throws org.netbeans.tax.TreeException
meth public void writeNotationDecl(org.netbeans.tax.TreeNotationDecl) throws org.netbeans.tax.TreeException
meth public void writeParameterEntityReference(org.netbeans.tax.TreeParameterEntityReference) throws org.netbeans.tax.TreeException
meth public void writeProcessingInstruction(org.netbeans.tax.TreeProcessingInstruction) throws org.netbeans.tax.TreeException
meth public void writeText(org.netbeans.tax.TreeText) throws org.netbeans.tax.TreeException
supr java.lang.Object
hfds AMPERSAND,APOSTROPHE,ASSIGN,ATTLIST_DECL_START,BRACKET_LEFT,CDATA_END,CDATA_START,CHAR_REF_HEX_START,CHAR_REF_START,COMMENT_END,COMMENT_START,DOCTYPE_INTERN_END,DOCTYPE_START,ELEMENT_DECL_START,ELEMENT_EMPTY_END,ELEMENT_END_START,ENTITY_DECL_START,GREAT_THAN,LESS_THAN,NOTATION_DECL_START,PER_CENT,PI_END,PI_START,PUBLIC,QUOTE,SEMICOLON,SPACE,SYSTEM,XML_ENCODING,XML_HEADER,XML_STANDALONE,XML_VERSION,document,indent,indent_step,outputStream,writer

CLSS public abstract interface org.netbeans.tax.io.TreeWriter
meth public abstract void writeDocument() throws org.netbeans.tax.TreeException

CLSS public org.netbeans.tax.io.Util
fld public final static org.netbeans.tax.io.Util THIS
supr org.netbeans.tax.AbstractUtil

CLSS public final org.netbeans.tax.io.XMLStringResult
meth public final static java.lang.String toString(org.netbeans.tax.TreeNode) throws org.netbeans.tax.TreeException
supr org.netbeans.tax.io.TreeStreamResult

CLSS public final org.netbeans.tax.io.XNIBuilder
cons public init(java.lang.Class,org.xml.sax.InputSource,org.xml.sax.EntityResolver,org.netbeans.tax.io.TreeStreamBuilderErrorHandler)
intf org.netbeans.tax.io.TreeBuilder
meth public org.netbeans.tax.TreeDocumentRoot buildDocument() throws org.netbeans.tax.TreeException
supr java.lang.Object
hfds ASSERT,DTD_WRAPPER,buildClass,entityResolver,errorHandler,inputSource
hcls DTDEntityResolver,DTDStopException,XMLBuilder

CLSS public abstract interface org.netbeans.tax.spec.AttlistDecl
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.AttlistDecl$Constraints
 outer org.netbeans.tax.spec.AttlistDecl
meth public abstract boolean isValidAttlistDeclAttributeDefaultType(short)
meth public abstract boolean isValidAttlistDeclAttributeDefaultValue(java.lang.String)
meth public abstract boolean isValidAttlistDeclAttributeEnumeratedType(java.lang.String[])
meth public abstract boolean isValidAttlistDeclAttributeName(java.lang.String)
meth public abstract boolean isValidAttlistDeclAttributeType(short)
meth public abstract boolean isValidAttlistDeclElementName(java.lang.String)
meth public abstract void checkAttlistDeclAttributeDefaultType(short) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkAttlistDeclAttributeDefaultValue(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkAttlistDeclAttributeEnumeratedType(java.lang.String[]) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkAttlistDeclAttributeName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkAttlistDeclAttributeType(short) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkAttlistDeclElementName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.AttlistDecl$Creator
 outer org.netbeans.tax.spec.AttlistDecl
meth public abstract org.netbeans.tax.TreeAttlistDecl createAttlistDecl(java.lang.String)

CLSS public abstract interface static org.netbeans.tax.spec.AttlistDecl$Writer
 outer org.netbeans.tax.spec.AttlistDecl
meth public abstract void writeAttlistDecl(org.netbeans.tax.TreeAttlistDecl) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.Attribute
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Value
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.Attribute$Constraints
 outer org.netbeans.tax.spec.Attribute
meth public abstract boolean isValidAttributeName(org.netbeans.tax.TreeName)
meth public abstract boolean isValidAttributeValue(java.lang.String)
meth public abstract void checkAttributeName(org.netbeans.tax.TreeName) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkAttributeValue(java.lang.String) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.Attribute$Creator
 outer org.netbeans.tax.spec.Attribute
meth public abstract org.netbeans.tax.TreeAttribute createAttribute(java.lang.String,java.lang.String)

CLSS public abstract interface static org.netbeans.tax.spec.Attribute$Value
 outer org.netbeans.tax.spec.Attribute

CLSS public abstract interface static org.netbeans.tax.spec.Attribute$Writer
 outer org.netbeans.tax.spec.Attribute
meth public abstract void writeAttribute(org.netbeans.tax.TreeAttribute) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.CDATASection
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.CDATASection$Constraints
 outer org.netbeans.tax.spec.CDATASection
meth public abstract boolean isValidCDATASectionData(java.lang.String)
meth public abstract void checkCDATASectionData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.CDATASection$Creator
 outer org.netbeans.tax.spec.CDATASection
meth public abstract org.netbeans.tax.TreeCDATASection createCDATASection(java.lang.String)

CLSS public abstract interface static org.netbeans.tax.spec.CDATASection$Writer
 outer org.netbeans.tax.spec.CDATASection
meth public abstract void writeCDATASection(org.netbeans.tax.TreeCDATASection) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.CharacterReference
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.CharacterReference$Constraints
 outer org.netbeans.tax.spec.CharacterReference
meth public abstract boolean isValidCharacterReferenceName(java.lang.String)
meth public abstract void checkCharacterReferenceName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.CharacterReference$Creator
 outer org.netbeans.tax.spec.CharacterReference
meth public abstract org.netbeans.tax.TreeCharacterReference createCharacterReference(java.lang.String)

CLSS public abstract interface static org.netbeans.tax.spec.CharacterReference$Writer
 outer org.netbeans.tax.spec.CharacterReference
meth public abstract void writeCharacterReference(org.netbeans.tax.TreeCharacterReference) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.Comment
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.Comment$Constraints
 outer org.netbeans.tax.spec.Comment
meth public abstract boolean isValidCommentData(java.lang.String)
meth public abstract void checkCommentData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.Comment$Creator
 outer org.netbeans.tax.spec.Comment
meth public abstract org.netbeans.tax.TreeComment createComment(java.lang.String)

CLSS public abstract interface static org.netbeans.tax.spec.Comment$Writer
 outer org.netbeans.tax.spec.Comment
meth public abstract void writeComment(org.netbeans.tax.TreeComment) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.ConditionalSection
innr public abstract interface static Child
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.ConditionalSection$Child
 outer org.netbeans.tax.spec.ConditionalSection

CLSS public abstract interface static org.netbeans.tax.spec.ConditionalSection$Constraints
 outer org.netbeans.tax.spec.ConditionalSection

CLSS public abstract interface static org.netbeans.tax.spec.ConditionalSection$Creator
 outer org.netbeans.tax.spec.ConditionalSection
meth public abstract org.netbeans.tax.TreeConditionalSection createConditionalSection(boolean)

CLSS public abstract interface static org.netbeans.tax.spec.ConditionalSection$Writer
 outer org.netbeans.tax.spec.ConditionalSection
meth public abstract void writeConditionalSection(org.netbeans.tax.TreeConditionalSection) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.DTD
innr public abstract interface static Child
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.DTD$Child
 outer org.netbeans.tax.spec.DTD

CLSS public abstract interface static org.netbeans.tax.spec.DTD$Constraints
 outer org.netbeans.tax.spec.DTD
meth public abstract boolean isValidDTDEncoding(java.lang.String)
meth public abstract boolean isValidDTDVersion(java.lang.String)
meth public abstract void checkDTDEncoding(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkDTDVersion(java.lang.String) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.DTD$Creator
 outer org.netbeans.tax.spec.DTD
meth public abstract org.netbeans.tax.TreeDTD createDTD(java.lang.String,java.lang.String)

CLSS public abstract interface static org.netbeans.tax.spec.DTD$Writer
 outer org.netbeans.tax.spec.DTD
meth public abstract void writeDTD(org.netbeans.tax.TreeDTD) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.Document
innr public abstract interface static Child
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.Document$Child
 outer org.netbeans.tax.spec.Document

CLSS public abstract interface static org.netbeans.tax.spec.Document$Constraints
 outer org.netbeans.tax.spec.Document
meth public abstract boolean isValidDocumentEncoding(java.lang.String)
meth public abstract boolean isValidDocumentStandalone(java.lang.String)
meth public abstract boolean isValidDocumentVersion(java.lang.String)
meth public abstract void checkDocumentEncoding(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkDocumentStandalone(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkDocumentVersion(java.lang.String) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.Document$Creator
 outer org.netbeans.tax.spec.Document
meth public abstract org.netbeans.tax.TreeDocument createDocument(java.lang.String,java.lang.String,java.lang.String)

CLSS public abstract interface static org.netbeans.tax.spec.Document$Writer
 outer org.netbeans.tax.spec.Document
meth public abstract void writeDocument(org.netbeans.tax.TreeDocument) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.DocumentFragment
innr public abstract interface static Child
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.DocumentFragment$Child
 outer org.netbeans.tax.spec.DocumentFragment

CLSS public abstract interface static org.netbeans.tax.spec.DocumentFragment$Constraints
 outer org.netbeans.tax.spec.DocumentFragment
meth public abstract boolean isValidDocumentFragmentEncoding(java.lang.String)
meth public abstract boolean isValidDocumentFragmentVersion(java.lang.String)
meth public abstract void checkDocumentFragmentEncoding(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkDocumentFragmentVersion(java.lang.String) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.DocumentFragment$Creator
 outer org.netbeans.tax.spec.DocumentFragment
meth public abstract org.netbeans.tax.TreeDocumentFragment createDocumentFragment(java.lang.String,java.lang.String)

CLSS public abstract interface static org.netbeans.tax.spec.DocumentFragment$Writer
 outer org.netbeans.tax.spec.DocumentFragment
meth public abstract void writeDocumentFragment(org.netbeans.tax.TreeDocumentFragment) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.DocumentType
innr public abstract interface static Child
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.DocumentType$Child
 outer org.netbeans.tax.spec.DocumentType

CLSS public abstract interface static org.netbeans.tax.spec.DocumentType$Constraints
 outer org.netbeans.tax.spec.DocumentType
meth public abstract boolean isValidDocumentTypeElementName(java.lang.String)
meth public abstract boolean isValidDocumentTypePublicId(java.lang.String)
meth public abstract boolean isValidDocumentTypeSystemId(java.lang.String)
meth public abstract void checkDocumentTypeElementName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkDocumentTypePublicId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkDocumentTypeSystemId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.DocumentType$Creator
 outer org.netbeans.tax.spec.DocumentType
meth public abstract org.netbeans.tax.TreeDocumentType createDocumentType(java.lang.String)
meth public abstract org.netbeans.tax.TreeDocumentType createDocumentType(java.lang.String,java.lang.String,java.lang.String)

CLSS public abstract interface static org.netbeans.tax.spec.DocumentType$Writer
 outer org.netbeans.tax.spec.DocumentType
meth public abstract void writeDocumentType(org.netbeans.tax.TreeDocumentType) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.Element
innr public abstract interface static Attribute
innr public abstract interface static Child
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.Element$Attribute
 outer org.netbeans.tax.spec.Element

CLSS public abstract interface static org.netbeans.tax.spec.Element$Child
 outer org.netbeans.tax.spec.Element

CLSS public abstract interface static org.netbeans.tax.spec.Element$Constraints
 outer org.netbeans.tax.spec.Element
meth public abstract boolean isValidElementTagName(org.netbeans.tax.TreeName)
meth public abstract void checkElementTagName(org.netbeans.tax.TreeName) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.Element$Creator
 outer org.netbeans.tax.spec.Element
meth public abstract org.netbeans.tax.TreeElement createElement(java.lang.String)

CLSS public abstract interface static org.netbeans.tax.spec.Element$Writer
 outer org.netbeans.tax.spec.Element
meth public abstract void writeElement(org.netbeans.tax.TreeElement) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.ElementDecl
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.ElementDecl$Constraints
 outer org.netbeans.tax.spec.ElementDecl
meth public abstract boolean isValidElementDeclContentType(org.netbeans.tax.TreeElementDecl$ContentType)
meth public abstract boolean isValidElementDeclName(java.lang.String)
meth public abstract void checkElementDeclContentType(org.netbeans.tax.TreeElementDecl$ContentType) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkElementDeclName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.ElementDecl$Creator
 outer org.netbeans.tax.spec.ElementDecl
meth public abstract org.netbeans.tax.TreeElementDecl createElementDecl(java.lang.String,org.netbeans.tax.TreeElementDecl$ContentType)

CLSS public abstract interface static org.netbeans.tax.spec.ElementDecl$Writer
 outer org.netbeans.tax.spec.ElementDecl
meth public abstract void writeElementDecl(org.netbeans.tax.TreeElementDecl) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.EntityDecl
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.EntityDecl$Constraints
 outer org.netbeans.tax.spec.EntityDecl
meth public abstract boolean isValidEntityDeclInternalText(java.lang.String)
meth public abstract boolean isValidEntityDeclName(java.lang.String)
meth public abstract boolean isValidEntityDeclNotationName(java.lang.String)
meth public abstract boolean isValidEntityDeclPublicId(java.lang.String)
meth public abstract boolean isValidEntityDeclSystemId(java.lang.String)
meth public abstract void checkEntityDeclInternalText(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkEntityDeclName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkEntityDeclNotationName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkEntityDeclPublicId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkEntityDeclSystemId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.EntityDecl$Creator
 outer org.netbeans.tax.spec.EntityDecl
meth public abstract org.netbeans.tax.TreeEntityDecl createEntityDecl(boolean,java.lang.String,java.lang.String)
meth public abstract org.netbeans.tax.TreeEntityDecl createEntityDecl(boolean,java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.netbeans.tax.TreeEntityDecl createEntityDecl(java.lang.String,java.lang.String)
meth public abstract org.netbeans.tax.TreeEntityDecl createEntityDecl(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.netbeans.tax.TreeEntityDecl createEntityDecl(java.lang.String,java.lang.String,java.lang.String,java.lang.String)

CLSS public abstract interface static org.netbeans.tax.spec.EntityDecl$Writer
 outer org.netbeans.tax.spec.EntityDecl
meth public abstract void writeEntityDecl(org.netbeans.tax.TreeEntityDecl) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.GeneralEntityReference
innr public abstract interface static Child
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.GeneralEntityReference$Child
 outer org.netbeans.tax.spec.GeneralEntityReference

CLSS public abstract interface static org.netbeans.tax.spec.GeneralEntityReference$Constraints
 outer org.netbeans.tax.spec.GeneralEntityReference
meth public abstract boolean isValidGeneralEntityReferenceName(java.lang.String)
meth public abstract void checkGeneralEntityReferenceName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.GeneralEntityReference$Creator
 outer org.netbeans.tax.spec.GeneralEntityReference
meth public abstract org.netbeans.tax.TreeGeneralEntityReference createGeneralEntityReference(java.lang.String)

CLSS public abstract interface static org.netbeans.tax.spec.GeneralEntityReference$Writer
 outer org.netbeans.tax.spec.GeneralEntityReference
meth public abstract void writeGeneralEntityReference(org.netbeans.tax.TreeGeneralEntityReference) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.NotationDecl
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.NotationDecl$Constraints
 outer org.netbeans.tax.spec.NotationDecl
meth public abstract boolean isValidNotationDeclName(java.lang.String)
meth public abstract boolean isValidNotationDeclPublicId(java.lang.String)
meth public abstract boolean isValidNotationDeclSystemId(java.lang.String)
meth public abstract void checkNotationDeclName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkNotationDeclPublicId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkNotationDeclSystemId(java.lang.String) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.NotationDecl$Creator
 outer org.netbeans.tax.spec.NotationDecl
meth public abstract org.netbeans.tax.TreeNotationDecl createNotationDecl(java.lang.String,java.lang.String,java.lang.String)

CLSS public abstract interface static org.netbeans.tax.spec.NotationDecl$Writer
 outer org.netbeans.tax.spec.NotationDecl
meth public abstract void writeNotationDecl(org.netbeans.tax.TreeNotationDecl) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.ParameterEntityReference
innr public abstract interface static Child
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.ParameterEntityReference$Child
 outer org.netbeans.tax.spec.ParameterEntityReference

CLSS public abstract interface static org.netbeans.tax.spec.ParameterEntityReference$Constraints
 outer org.netbeans.tax.spec.ParameterEntityReference
meth public abstract boolean isValidParameterEntityReferenceName(java.lang.String)
meth public abstract void checkParameterEntityReferenceName(java.lang.String) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.ParameterEntityReference$Creator
 outer org.netbeans.tax.spec.ParameterEntityReference
meth public abstract org.netbeans.tax.TreeParameterEntityReference createParameterEntityReference(java.lang.String)

CLSS public abstract interface static org.netbeans.tax.spec.ParameterEntityReference$Writer
 outer org.netbeans.tax.spec.ParameterEntityReference
meth public abstract void writeParameterEntityReference(org.netbeans.tax.TreeParameterEntityReference) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.ProcessingInstruction
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.ProcessingInstruction$Constraints
 outer org.netbeans.tax.spec.ProcessingInstruction
meth public abstract boolean isValidProcessingInstructionData(java.lang.String)
meth public abstract boolean isValidProcessingInstructionTarget(java.lang.String)
meth public abstract void checkProcessingInstructionData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException
meth public abstract void checkProcessingInstructionTarget(java.lang.String) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.ProcessingInstruction$Creator
 outer org.netbeans.tax.spec.ProcessingInstruction
meth public abstract org.netbeans.tax.TreeProcessingInstruction createProcessingInstruction(java.lang.String,java.lang.String)

CLSS public abstract interface static org.netbeans.tax.spec.ProcessingInstruction$Writer
 outer org.netbeans.tax.spec.ProcessingInstruction
meth public abstract void writeProcessingInstruction(org.netbeans.tax.TreeProcessingInstruction) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.netbeans.tax.spec.Text
innr public abstract interface static Constraints
innr public abstract interface static Creator
innr public abstract interface static Writer

CLSS public abstract interface static org.netbeans.tax.spec.Text$Constraints
 outer org.netbeans.tax.spec.Text
meth public abstract boolean isValidTextData(java.lang.String)
meth public abstract void checkTextData(java.lang.String) throws org.netbeans.tax.InvalidArgumentException

CLSS public abstract interface static org.netbeans.tax.spec.Text$Creator
 outer org.netbeans.tax.spec.Text
meth public abstract org.netbeans.tax.TreeText createText(java.lang.String)

CLSS public abstract interface static org.netbeans.tax.spec.Text$Writer
 outer org.netbeans.tax.spec.Text
meth public abstract void writeText(org.netbeans.tax.TreeText) throws org.netbeans.tax.TreeException

CLSS public abstract interface org.openide.cookies.EditorCookie
innr public abstract interface static Observable
intf org.openide.cookies.LineCookie
meth public abstract boolean close()
meth public abstract boolean isModified()
meth public abstract javax.swing.JEditorPane[] getOpenedPanes()
meth public abstract javax.swing.text.StyledDocument getDocument()
meth public abstract javax.swing.text.StyledDocument openDocument() throws java.io.IOException
meth public abstract org.openide.util.Task prepareDocument()
meth public abstract void open()
meth public abstract void saveDocument() throws java.io.IOException

CLSS public abstract interface org.openide.cookies.LineCookie
intf org.openide.nodes.Node$Cookie
meth public abstract org.openide.text.Line$Set getLineSet()

CLSS public final org.openide.nodes.CookieSet
cons public init()
innr public abstract interface static Before
innr public abstract interface static Factory
intf org.openide.util.Lookup$Provider
meth public !varargs <%0 extends java.lang.Object> void assign(java.lang.Class<? extends {%%0}>,{%%0}[])
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public org.openide.util.Lookup getLookup()
meth public static org.openide.nodes.CookieSet createGeneric(org.openide.nodes.CookieSet$Before)
meth public void add(java.lang.Class<? extends org.openide.nodes.Node$Cookie>,org.openide.nodes.CookieSet$Factory)
meth public void add(java.lang.Class<? extends org.openide.nodes.Node$Cookie>[],org.openide.nodes.CookieSet$Factory)
meth public void add(org.openide.nodes.Node$Cookie)
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void remove(java.lang.Class<? extends org.openide.nodes.Node$Cookie>,org.openide.nodes.CookieSet$Factory)
meth public void remove(java.lang.Class<? extends org.openide.nodes.Node$Cookie>[],org.openide.nodes.CookieSet$Factory)
meth public void remove(org.openide.nodes.Node$Cookie)
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds QUERY_MODE,cs,ic,lookup,map
hcls C,CookieEntry,CookieEntryPair,PairWrap,R

CLSS public abstract interface static org.openide.nodes.CookieSet$Factory
 outer org.openide.nodes.CookieSet
meth public abstract <%0 extends org.openide.nodes.Node$Cookie> {%%0} createCookie(java.lang.Class<{%%0}>)

CLSS public abstract org.openide.nodes.Node
cons protected init(org.openide.nodes.Children)
cons protected init(org.openide.nodes.Children,org.openide.util.Lookup)
fld public final static java.lang.String PROP_COOKIE = "cookie"
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
fld public final static java.lang.String PROP_ICON = "icon"
fld public final static java.lang.String PROP_LEAF = "leaf"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_OPENED_ICON = "openedIcon"
fld public final static java.lang.String PROP_PARENT_NODE = "parentNode"
fld public final static java.lang.String PROP_PROPERTY_SETS = "propertySets"
fld public final static java.lang.String PROP_SHORT_DESCRIPTION = "shortDescription"
fld public final static org.openide.nodes.Node EMPTY
innr public abstract interface static Cookie
innr public abstract interface static Handle
innr public abstract static IndexedProperty
innr public abstract static Property
innr public abstract static PropertySet
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected final boolean hasPropertyChangeListener()
meth protected final void fireCookieChange()
meth protected final void fireDisplayNameChange(java.lang.String,java.lang.String)
meth protected final void fireIconChange()
meth protected final void fireNameChange(java.lang.String,java.lang.String)
meth protected final void fireNodeDestroyed()
meth protected final void fireOpenedIconChange()
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void firePropertySetsChange(org.openide.nodes.Node$PropertySet[],org.openide.nodes.Node$PropertySet[])
meth protected final void fireShortDescriptionChange(java.lang.String,java.lang.String)
meth protected final void setChildren(org.openide.nodes.Children)
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public abstract boolean canCopy()
meth public abstract boolean canCut()
meth public abstract boolean canDestroy()
meth public abstract boolean canRename()
meth public abstract boolean hasCustomizer()
meth public abstract java.awt.Component getCustomizer()
meth public abstract java.awt.Image getIcon(int)
meth public abstract java.awt.Image getOpenedIcon(int)
meth public abstract java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public abstract java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public abstract java.awt.datatransfer.Transferable drag() throws java.io.IOException
meth public abstract org.openide.nodes.Node cloneNode()
meth public abstract org.openide.nodes.Node$Handle getHandle()
meth public abstract org.openide.nodes.Node$PropertySet[] getPropertySets()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract org.openide.util.datatransfer.NewType[] getNewTypes()
meth public abstract org.openide.util.datatransfer.PasteType getDropType(java.awt.datatransfer.Transferable,int,int)
meth public abstract org.openide.util.datatransfer.PasteType[] getPasteTypes(java.awt.datatransfer.Transferable)
meth public boolean equals(java.lang.Object)
meth public final boolean isLeaf()
meth public final javax.swing.JPopupMenu getContextMenu()
meth public final org.openide.nodes.Children getChildren()
meth public final org.openide.nodes.Node getParentNode()
meth public final org.openide.util.Lookup getLookup()
meth public final void addNodeListener(org.openide.nodes.NodeListener)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removeNodeListener(org.openide.nodes.NodeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public int hashCode()
meth public java.lang.String getHtmlDisplayName()
meth public java.lang.String toString()
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.util.actions.SystemAction getDefaultAction()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getContextActions()
 anno 0 java.lang.Deprecated()
meth public void destroy() throws java.io.IOException
meth public void setDisplayName(java.lang.String)
meth public void setHidden(boolean)
 anno 0 java.lang.Deprecated()
meth public void setName(java.lang.String)
meth public void setShortDescription(java.lang.String)
supr java.beans.FeatureDescriptor
hfds BLOCK_EVENTS,INIT_LOCK,LOCK,TEMPL_COOKIE,err,hierarchy,listeners,lookups,parent,warnedBadProperties
hcls LookupEventList,PropertyEditorRef

CLSS public abstract interface static org.openide.nodes.Node$Cookie
 outer org.openide.nodes.Node

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

