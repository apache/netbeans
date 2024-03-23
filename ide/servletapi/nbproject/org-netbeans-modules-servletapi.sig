#Signature file v4.1
#Version 1.62

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

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

CLSS public abstract java.io.OutputStream
cons public init()
intf java.io.Closeable
intf java.io.Flushable
meth public abstract void write(int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface java.lang.Cloneable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract java.lang.Enum<%0 extends java.lang.Enum<{java.lang.Enum%0}>>
cons protected init(java.lang.String,int)
intf java.io.Serializable
intf java.lang.Comparable<{java.lang.Enum%0}>
meth protected final java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected final void finalize()
meth public final boolean equals(java.lang.Object)
meth public final int compareTo({java.lang.Enum%0})
meth public final int hashCode()
meth public final int ordinal()
meth public final java.lang.Class<{java.lang.Enum%0}> getDeclaringClass()
meth public final java.lang.String name()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} valueOf(java.lang.Class<{%%0}>,java.lang.String)
supr java.lang.Object

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

CLSS public abstract interface !annotation java.lang.annotation.Inherited
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

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface javax.servlet.AsyncContext
fld public final static java.lang.String ASYNC_CONTEXT_PATH = "javax.servlet.async.context_path"
fld public final static java.lang.String ASYNC_MAPPING = "javax.servlet.async.mapping"
fld public final static java.lang.String ASYNC_PATH_INFO = "javax.servlet.async.path_info"
fld public final static java.lang.String ASYNC_QUERY_STRING = "javax.servlet.async.query_string"
fld public final static java.lang.String ASYNC_REQUEST_URI = "javax.servlet.async.request_uri"
fld public final static java.lang.String ASYNC_SERVLET_PATH = "javax.servlet.async.servlet_path"
meth public abstract <%0 extends javax.servlet.AsyncListener> {%%0} createListener(java.lang.Class<{%%0}>) throws javax.servlet.ServletException
meth public abstract boolean hasOriginalRequestAndResponse()
meth public abstract javax.servlet.ServletRequest getRequest()
meth public abstract javax.servlet.ServletResponse getResponse()
meth public abstract long getTimeout()
meth public abstract void addListener(javax.servlet.AsyncListener)
meth public abstract void addListener(javax.servlet.AsyncListener,javax.servlet.ServletRequest,javax.servlet.ServletResponse)
meth public abstract void complete()
meth public abstract void dispatch()
meth public abstract void dispatch(java.lang.String)
meth public abstract void dispatch(javax.servlet.ServletContext,java.lang.String)
meth public abstract void setTimeout(long)
meth public abstract void start(java.lang.Runnable)

CLSS public javax.servlet.AsyncEvent
cons public init(javax.servlet.AsyncContext)
cons public init(javax.servlet.AsyncContext,java.lang.Throwable)
cons public init(javax.servlet.AsyncContext,javax.servlet.ServletRequest,javax.servlet.ServletResponse)
cons public init(javax.servlet.AsyncContext,javax.servlet.ServletRequest,javax.servlet.ServletResponse,java.lang.Throwable)
meth public java.lang.Throwable getThrowable()
meth public javax.servlet.AsyncContext getAsyncContext()
meth public javax.servlet.ServletRequest getSuppliedRequest()
meth public javax.servlet.ServletResponse getSuppliedResponse()
supr java.lang.Object
hfds context,request,response,throwable

CLSS public abstract interface javax.servlet.AsyncListener
intf java.util.EventListener
meth public abstract void onComplete(javax.servlet.AsyncEvent) throws java.io.IOException
meth public abstract void onError(javax.servlet.AsyncEvent) throws java.io.IOException
meth public abstract void onStartAsync(javax.servlet.AsyncEvent) throws java.io.IOException
meth public abstract void onTimeout(javax.servlet.AsyncEvent) throws java.io.IOException

CLSS public final !enum javax.servlet.DispatcherType
fld public final static javax.servlet.DispatcherType ASYNC
fld public final static javax.servlet.DispatcherType ERROR
fld public final static javax.servlet.DispatcherType FORWARD
fld public final static javax.servlet.DispatcherType INCLUDE
fld public final static javax.servlet.DispatcherType REQUEST
meth public static javax.servlet.DispatcherType valueOf(java.lang.String)
meth public static javax.servlet.DispatcherType[] values()
supr java.lang.Enum<javax.servlet.DispatcherType>

CLSS public abstract interface javax.servlet.Filter
meth public abstract void doFilter(javax.servlet.ServletRequest,javax.servlet.ServletResponse,javax.servlet.FilterChain) throws java.io.IOException,javax.servlet.ServletException
meth public void destroy()
meth public void init(javax.servlet.FilterConfig) throws javax.servlet.ServletException

CLSS public abstract interface javax.servlet.FilterChain
meth public abstract void doFilter(javax.servlet.ServletRequest,javax.servlet.ServletResponse) throws java.io.IOException,javax.servlet.ServletException

CLSS public abstract interface javax.servlet.FilterConfig
meth public abstract java.lang.String getFilterName()
meth public abstract java.lang.String getInitParameter(java.lang.String)
meth public abstract java.util.Enumeration<java.lang.String> getInitParameterNames()
meth public abstract javax.servlet.ServletContext getServletContext()

CLSS public abstract interface javax.servlet.FilterRegistration
innr public abstract interface static Dynamic
intf javax.servlet.Registration
meth public abstract !varargs void addMappingForServletNames(java.util.EnumSet<javax.servlet.DispatcherType>,boolean,java.lang.String[])
meth public abstract !varargs void addMappingForUrlPatterns(java.util.EnumSet<javax.servlet.DispatcherType>,boolean,java.lang.String[])
meth public abstract java.util.Collection<java.lang.String> getServletNameMappings()
meth public abstract java.util.Collection<java.lang.String> getUrlPatternMappings()

CLSS public abstract interface static javax.servlet.FilterRegistration$Dynamic
 outer javax.servlet.FilterRegistration
intf javax.servlet.FilterRegistration
intf javax.servlet.Registration$Dynamic

CLSS public abstract javax.servlet.GenericFilter
cons public init()
intf java.io.Serializable
intf javax.servlet.Filter
intf javax.servlet.FilterConfig
meth public java.lang.String getFilterName()
meth public java.lang.String getInitParameter(java.lang.String)
meth public java.util.Enumeration<java.lang.String> getInitParameterNames()
meth public javax.servlet.FilterConfig getFilterConfig()
meth public javax.servlet.ServletContext getServletContext()
meth public void init() throws javax.servlet.ServletException
meth public void init(javax.servlet.FilterConfig) throws javax.servlet.ServletException
supr java.lang.Object
hfds LSTRING_FILE,config,lStrings,serialVersionUID

CLSS public abstract javax.servlet.GenericServlet
cons public init()
intf java.io.Serializable
intf javax.servlet.Servlet
intf javax.servlet.ServletConfig
meth public abstract void service(javax.servlet.ServletRequest,javax.servlet.ServletResponse) throws java.io.IOException,javax.servlet.ServletException
meth public java.lang.String getInitParameter(java.lang.String)
meth public java.lang.String getServletInfo()
meth public java.lang.String getServletName()
meth public java.util.Enumeration<java.lang.String> getInitParameterNames()
meth public javax.servlet.ServletConfig getServletConfig()
meth public javax.servlet.ServletContext getServletContext()
meth public void destroy()
meth public void init() throws javax.servlet.ServletException
meth public void init(javax.servlet.ServletConfig) throws javax.servlet.ServletException
meth public void log(java.lang.String)
meth public void log(java.lang.String,java.lang.Throwable)
supr java.lang.Object
hfds LSTRING_FILE,config,lStrings,serialVersionUID

CLSS public javax.servlet.HttpConstraintElement
cons public !varargs init(javax.servlet.annotation.ServletSecurity$EmptyRoleSemantic,javax.servlet.annotation.ServletSecurity$TransportGuarantee,java.lang.String[])
cons public !varargs init(javax.servlet.annotation.ServletSecurity$TransportGuarantee,java.lang.String[])
cons public init()
cons public init(javax.servlet.annotation.ServletSecurity$EmptyRoleSemantic)
meth public java.lang.String[] getRolesAllowed()
meth public javax.servlet.annotation.ServletSecurity$EmptyRoleSemantic getEmptyRoleSemantic()
meth public javax.servlet.annotation.ServletSecurity$TransportGuarantee getTransportGuarantee()
supr java.lang.Object
hfds emptyRoleSemantic,rolesAllowed,transportGuarantee

CLSS public javax.servlet.HttpMethodConstraintElement
cons public init(java.lang.String)
cons public init(java.lang.String,javax.servlet.HttpConstraintElement)
meth public java.lang.String getMethodName()
supr javax.servlet.HttpConstraintElement
hfds methodName

CLSS public javax.servlet.MultipartConfigElement
cons public init(java.lang.String)
cons public init(java.lang.String,long,long,int)
cons public init(javax.servlet.annotation.MultipartConfig)
meth public int getFileSizeThreshold()
meth public java.lang.String getLocation()
meth public long getMaxFileSize()
meth public long getMaxRequestSize()
supr java.lang.Object
hfds fileSizeThreshold,location,maxFileSize,maxRequestSize

CLSS public abstract interface javax.servlet.ReadListener
intf java.util.EventListener
meth public abstract void onAllDataRead() throws java.io.IOException
meth public abstract void onDataAvailable() throws java.io.IOException
meth public abstract void onError(java.lang.Throwable)

CLSS public abstract interface javax.servlet.Registration
innr public abstract interface static Dynamic
meth public abstract boolean setInitParameter(java.lang.String,java.lang.String)
meth public abstract java.lang.String getClassName()
meth public abstract java.lang.String getInitParameter(java.lang.String)
meth public abstract java.lang.String getName()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getInitParameters()
meth public abstract java.util.Set<java.lang.String> setInitParameters(java.util.Map<java.lang.String,java.lang.String>)

CLSS public abstract interface static javax.servlet.Registration$Dynamic
 outer javax.servlet.Registration
intf javax.servlet.Registration
meth public abstract void setAsyncSupported(boolean)

CLSS public abstract interface javax.servlet.RequestDispatcher
fld public final static java.lang.String ERROR_EXCEPTION = "javax.servlet.error.exception"
fld public final static java.lang.String ERROR_EXCEPTION_TYPE = "javax.servlet.error.exception_type"
fld public final static java.lang.String ERROR_MESSAGE = "javax.servlet.error.message"
fld public final static java.lang.String ERROR_REQUEST_URI = "javax.servlet.error.request_uri"
fld public final static java.lang.String ERROR_SERVLET_NAME = "javax.servlet.error.servlet_name"
fld public final static java.lang.String ERROR_STATUS_CODE = "javax.servlet.error.status_code"
fld public final static java.lang.String FORWARD_CONTEXT_PATH = "javax.servlet.forward.context_path"
fld public final static java.lang.String FORWARD_MAPPING = "javax.servlet.forward.mapping"
fld public final static java.lang.String FORWARD_PATH_INFO = "javax.servlet.forward.path_info"
fld public final static java.lang.String FORWARD_QUERY_STRING = "javax.servlet.forward.query_string"
fld public final static java.lang.String FORWARD_REQUEST_URI = "javax.servlet.forward.request_uri"
fld public final static java.lang.String FORWARD_SERVLET_PATH = "javax.servlet.forward.servlet_path"
fld public final static java.lang.String INCLUDE_CONTEXT_PATH = "javax.servlet.include.context_path"
fld public final static java.lang.String INCLUDE_MAPPING = "javax.servlet.include.mapping"
fld public final static java.lang.String INCLUDE_PATH_INFO = "javax.servlet.include.path_info"
fld public final static java.lang.String INCLUDE_QUERY_STRING = "javax.servlet.include.query_string"
fld public final static java.lang.String INCLUDE_REQUEST_URI = "javax.servlet.include.request_uri"
fld public final static java.lang.String INCLUDE_SERVLET_PATH = "javax.servlet.include.servlet_path"
meth public abstract void forward(javax.servlet.ServletRequest,javax.servlet.ServletResponse) throws java.io.IOException,javax.servlet.ServletException
meth public abstract void include(javax.servlet.ServletRequest,javax.servlet.ServletResponse) throws java.io.IOException,javax.servlet.ServletException

CLSS public abstract interface javax.servlet.Servlet
meth public abstract java.lang.String getServletInfo()
meth public abstract javax.servlet.ServletConfig getServletConfig()
meth public abstract void destroy()
meth public abstract void init(javax.servlet.ServletConfig) throws javax.servlet.ServletException
meth public abstract void service(javax.servlet.ServletRequest,javax.servlet.ServletResponse) throws java.io.IOException,javax.servlet.ServletException

CLSS public abstract interface javax.servlet.ServletConfig
meth public abstract java.lang.String getInitParameter(java.lang.String)
meth public abstract java.lang.String getServletName()
meth public abstract java.util.Enumeration<java.lang.String> getInitParameterNames()
meth public abstract javax.servlet.ServletContext getServletContext()

CLSS public abstract interface javax.servlet.ServletContainerInitializer
meth public abstract void onStartup(java.util.Set<java.lang.Class<?>>,javax.servlet.ServletContext) throws javax.servlet.ServletException

CLSS public abstract interface javax.servlet.ServletContext
fld public final static java.lang.String ORDERED_LIBS = "javax.servlet.context.orderedLibs"
fld public final static java.lang.String TEMPDIR = "javax.servlet.context.tempdir"
meth public abstract !varargs void declareRoles(java.lang.String[])
meth public abstract <%0 extends java.util.EventListener> void addListener({%%0})
meth public abstract <%0 extends java.util.EventListener> {%%0} createListener(java.lang.Class<{%%0}>) throws javax.servlet.ServletException
meth public abstract <%0 extends javax.servlet.Filter> {%%0} createFilter(java.lang.Class<{%%0}>) throws javax.servlet.ServletException
meth public abstract <%0 extends javax.servlet.Servlet> {%%0} createServlet(java.lang.Class<{%%0}>) throws javax.servlet.ServletException
meth public abstract boolean setInitParameter(java.lang.String,java.lang.String)
meth public abstract int getEffectiveMajorVersion()
meth public abstract int getEffectiveMinorVersion()
meth public abstract int getMajorVersion()
meth public abstract int getMinorVersion()
meth public abstract int getSessionTimeout()
meth public abstract java.io.InputStream getResourceAsStream(java.lang.String)
meth public abstract java.lang.ClassLoader getClassLoader()
meth public abstract java.lang.Object getAttribute(java.lang.String)
meth public abstract java.lang.String getContextPath()
meth public abstract java.lang.String getInitParameter(java.lang.String)
meth public abstract java.lang.String getMimeType(java.lang.String)
meth public abstract java.lang.String getRealPath(java.lang.String)
meth public abstract java.lang.String getRequestCharacterEncoding()
meth public abstract java.lang.String getResponseCharacterEncoding()
meth public abstract java.lang.String getServerInfo()
meth public abstract java.lang.String getServletContextName()
meth public abstract java.lang.String getVirtualServerName()
meth public abstract java.net.URL getResource(java.lang.String) throws java.net.MalformedURLException
meth public abstract java.util.Enumeration<java.lang.String> getAttributeNames()
meth public abstract java.util.Enumeration<java.lang.String> getInitParameterNames()
meth public abstract java.util.Enumeration<java.lang.String> getServletNames()
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Enumeration<javax.servlet.Servlet> getServlets()
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Map<java.lang.String,? extends javax.servlet.FilterRegistration> getFilterRegistrations()
meth public abstract java.util.Map<java.lang.String,? extends javax.servlet.ServletRegistration> getServletRegistrations()
meth public abstract java.util.Set<java.lang.String> getResourcePaths(java.lang.String)
meth public abstract java.util.Set<javax.servlet.SessionTrackingMode> getDefaultSessionTrackingModes()
meth public abstract java.util.Set<javax.servlet.SessionTrackingMode> getEffectiveSessionTrackingModes()
meth public abstract javax.servlet.FilterRegistration getFilterRegistration(java.lang.String)
meth public abstract javax.servlet.FilterRegistration$Dynamic addFilter(java.lang.String,java.lang.Class<? extends javax.servlet.Filter>)
meth public abstract javax.servlet.FilterRegistration$Dynamic addFilter(java.lang.String,java.lang.String)
meth public abstract javax.servlet.FilterRegistration$Dynamic addFilter(java.lang.String,javax.servlet.Filter)
meth public abstract javax.servlet.RequestDispatcher getNamedDispatcher(java.lang.String)
meth public abstract javax.servlet.RequestDispatcher getRequestDispatcher(java.lang.String)
meth public abstract javax.servlet.Servlet getServlet(java.lang.String) throws javax.servlet.ServletException
 anno 0 java.lang.Deprecated()
meth public abstract javax.servlet.ServletContext getContext(java.lang.String)
meth public abstract javax.servlet.ServletRegistration getServletRegistration(java.lang.String)
meth public abstract javax.servlet.ServletRegistration$Dynamic addJspFile(java.lang.String,java.lang.String)
meth public abstract javax.servlet.ServletRegistration$Dynamic addServlet(java.lang.String,java.lang.Class<? extends javax.servlet.Servlet>)
meth public abstract javax.servlet.ServletRegistration$Dynamic addServlet(java.lang.String,java.lang.String)
meth public abstract javax.servlet.ServletRegistration$Dynamic addServlet(java.lang.String,javax.servlet.Servlet)
meth public abstract javax.servlet.SessionCookieConfig getSessionCookieConfig()
meth public abstract javax.servlet.descriptor.JspConfigDescriptor getJspConfigDescriptor()
meth public abstract void addListener(java.lang.Class<? extends java.util.EventListener>)
meth public abstract void addListener(java.lang.String)
meth public abstract void log(java.lang.Exception,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public abstract void log(java.lang.String)
meth public abstract void log(java.lang.String,java.lang.Throwable)
meth public abstract void removeAttribute(java.lang.String)
meth public abstract void setAttribute(java.lang.String,java.lang.Object)
meth public abstract void setRequestCharacterEncoding(java.lang.String)
meth public abstract void setResponseCharacterEncoding(java.lang.String)
meth public abstract void setSessionTimeout(int)
meth public abstract void setSessionTrackingModes(java.util.Set<javax.servlet.SessionTrackingMode>)

CLSS public javax.servlet.ServletContextAttributeEvent
cons public init(javax.servlet.ServletContext,java.lang.String,java.lang.Object)
meth public java.lang.Object getValue()
meth public java.lang.String getName()
supr javax.servlet.ServletContextEvent
hfds name,serialVersionUID,value

CLSS public abstract interface javax.servlet.ServletContextAttributeListener
intf java.util.EventListener
meth public void attributeAdded(javax.servlet.ServletContextAttributeEvent)
meth public void attributeRemoved(javax.servlet.ServletContextAttributeEvent)
meth public void attributeReplaced(javax.servlet.ServletContextAttributeEvent)

CLSS public javax.servlet.ServletContextEvent
cons public init(javax.servlet.ServletContext)
meth public javax.servlet.ServletContext getServletContext()
supr java.util.EventObject
hfds serialVersionUID

CLSS public abstract interface javax.servlet.ServletContextListener
intf java.util.EventListener
meth public void contextDestroyed(javax.servlet.ServletContextEvent)
meth public void contextInitialized(javax.servlet.ServletContextEvent)

CLSS public javax.servlet.ServletException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public java.lang.Throwable getRootCause()
supr java.lang.Exception
hfds rootCause,serialVersionUID

CLSS public abstract javax.servlet.ServletInputStream
cons protected init()
meth public abstract boolean isFinished()
meth public abstract boolean isReady()
meth public abstract void setReadListener(javax.servlet.ReadListener)
meth public int readLine(byte[],int,int) throws java.io.IOException
supr java.io.InputStream

CLSS public abstract javax.servlet.ServletOutputStream
cons protected init()
meth public abstract boolean isReady()
meth public abstract void setWriteListener(javax.servlet.WriteListener)
meth public void print(boolean) throws java.io.IOException
meth public void print(char) throws java.io.IOException
meth public void print(double) throws java.io.IOException
meth public void print(float) throws java.io.IOException
meth public void print(int) throws java.io.IOException
meth public void print(java.lang.String) throws java.io.IOException
meth public void print(long) throws java.io.IOException
meth public void println() throws java.io.IOException
meth public void println(boolean) throws java.io.IOException
meth public void println(char) throws java.io.IOException
meth public void println(double) throws java.io.IOException
meth public void println(float) throws java.io.IOException
meth public void println(int) throws java.io.IOException
meth public void println(java.lang.String) throws java.io.IOException
meth public void println(long) throws java.io.IOException
supr java.io.OutputStream
hfds LSTRING_FILE,lStrings

CLSS public abstract interface javax.servlet.ServletRegistration
innr public abstract interface static Dynamic
intf javax.servlet.Registration
meth public abstract !varargs java.util.Set<java.lang.String> addMapping(java.lang.String[])
meth public abstract java.lang.String getRunAsRole()
meth public abstract java.util.Collection<java.lang.String> getMappings()

CLSS public abstract interface static javax.servlet.ServletRegistration$Dynamic
 outer javax.servlet.ServletRegistration
intf javax.servlet.Registration$Dynamic
intf javax.servlet.ServletRegistration
meth public abstract java.util.Set<java.lang.String> setServletSecurity(javax.servlet.ServletSecurityElement)
meth public abstract void setLoadOnStartup(int)
meth public abstract void setMultipartConfig(javax.servlet.MultipartConfigElement)
meth public abstract void setRunAsRole(java.lang.String)

CLSS public abstract interface javax.servlet.ServletRequest
meth public abstract boolean isAsyncStarted()
meth public abstract boolean isAsyncSupported()
meth public abstract boolean isSecure()
meth public abstract int getContentLength()
meth public abstract int getLocalPort()
meth public abstract int getRemotePort()
meth public abstract int getServerPort()
meth public abstract java.io.BufferedReader getReader() throws java.io.IOException
meth public abstract java.lang.Object getAttribute(java.lang.String)
meth public abstract java.lang.String getCharacterEncoding()
meth public abstract java.lang.String getContentType()
meth public abstract java.lang.String getLocalAddr()
meth public abstract java.lang.String getLocalName()
meth public abstract java.lang.String getParameter(java.lang.String)
meth public abstract java.lang.String getProtocol()
meth public abstract java.lang.String getRealPath(java.lang.String)
meth public abstract java.lang.String getRemoteAddr()
meth public abstract java.lang.String getRemoteHost()
meth public abstract java.lang.String getScheme()
meth public abstract java.lang.String getServerName()
meth public abstract java.lang.String[] getParameterValues(java.lang.String)
meth public abstract java.util.Enumeration<java.lang.String> getAttributeNames()
meth public abstract java.util.Enumeration<java.lang.String> getParameterNames()
meth public abstract java.util.Enumeration<java.util.Locale> getLocales()
meth public abstract java.util.Locale getLocale()
meth public abstract java.util.Map<java.lang.String,java.lang.String[]> getParameterMap()
meth public abstract javax.servlet.AsyncContext getAsyncContext()
meth public abstract javax.servlet.AsyncContext startAsync()
meth public abstract javax.servlet.AsyncContext startAsync(javax.servlet.ServletRequest,javax.servlet.ServletResponse)
meth public abstract javax.servlet.DispatcherType getDispatcherType()
meth public abstract javax.servlet.RequestDispatcher getRequestDispatcher(java.lang.String)
meth public abstract javax.servlet.ServletContext getServletContext()
meth public abstract javax.servlet.ServletInputStream getInputStream() throws java.io.IOException
meth public abstract long getContentLengthLong()
meth public abstract void removeAttribute(java.lang.String)
meth public abstract void setAttribute(java.lang.String,java.lang.Object)
meth public abstract void setCharacterEncoding(java.lang.String) throws java.io.UnsupportedEncodingException

CLSS public javax.servlet.ServletRequestAttributeEvent
cons public init(javax.servlet.ServletContext,javax.servlet.ServletRequest,java.lang.String,java.lang.Object)
meth public java.lang.Object getValue()
meth public java.lang.String getName()
supr javax.servlet.ServletRequestEvent
hfds name,serialVersionUID,value

CLSS public abstract interface javax.servlet.ServletRequestAttributeListener
intf java.util.EventListener
meth public void attributeAdded(javax.servlet.ServletRequestAttributeEvent)
meth public void attributeRemoved(javax.servlet.ServletRequestAttributeEvent)
meth public void attributeReplaced(javax.servlet.ServletRequestAttributeEvent)

CLSS public javax.servlet.ServletRequestEvent
cons public init(javax.servlet.ServletContext,javax.servlet.ServletRequest)
meth public javax.servlet.ServletContext getServletContext()
meth public javax.servlet.ServletRequest getServletRequest()
supr java.util.EventObject
hfds request,serialVersionUID

CLSS public abstract interface javax.servlet.ServletRequestListener
intf java.util.EventListener
meth public void requestDestroyed(javax.servlet.ServletRequestEvent)
meth public void requestInitialized(javax.servlet.ServletRequestEvent)

CLSS public javax.servlet.ServletRequestWrapper
cons public init(javax.servlet.ServletRequest)
intf javax.servlet.ServletRequest
meth public boolean isAsyncStarted()
meth public boolean isAsyncSupported()
meth public boolean isSecure()
meth public boolean isWrapperFor(java.lang.Class<?>)
meth public boolean isWrapperFor(javax.servlet.ServletRequest)
meth public int getContentLength()
meth public int getLocalPort()
meth public int getRemotePort()
meth public int getServerPort()
meth public java.io.BufferedReader getReader() throws java.io.IOException
meth public java.lang.Object getAttribute(java.lang.String)
meth public java.lang.String getCharacterEncoding()
meth public java.lang.String getContentType()
meth public java.lang.String getLocalAddr()
meth public java.lang.String getLocalName()
meth public java.lang.String getParameter(java.lang.String)
meth public java.lang.String getProtocol()
meth public java.lang.String getRealPath(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.String getRemoteAddr()
meth public java.lang.String getRemoteHost()
meth public java.lang.String getScheme()
meth public java.lang.String getServerName()
meth public java.lang.String[] getParameterValues(java.lang.String)
meth public java.util.Enumeration<java.lang.String> getAttributeNames()
meth public java.util.Enumeration<java.lang.String> getParameterNames()
meth public java.util.Enumeration<java.util.Locale> getLocales()
meth public java.util.Locale getLocale()
meth public java.util.Map<java.lang.String,java.lang.String[]> getParameterMap()
meth public javax.servlet.AsyncContext getAsyncContext()
meth public javax.servlet.AsyncContext startAsync()
meth public javax.servlet.AsyncContext startAsync(javax.servlet.ServletRequest,javax.servlet.ServletResponse)
meth public javax.servlet.DispatcherType getDispatcherType()
meth public javax.servlet.RequestDispatcher getRequestDispatcher(java.lang.String)
meth public javax.servlet.ServletContext getServletContext()
meth public javax.servlet.ServletInputStream getInputStream() throws java.io.IOException
meth public javax.servlet.ServletRequest getRequest()
meth public long getContentLengthLong()
meth public void removeAttribute(java.lang.String)
meth public void setAttribute(java.lang.String,java.lang.Object)
meth public void setCharacterEncoding(java.lang.String) throws java.io.UnsupportedEncodingException
meth public void setRequest(javax.servlet.ServletRequest)
supr java.lang.Object
hfds request

CLSS public abstract interface javax.servlet.ServletResponse
meth public abstract boolean isCommitted()
meth public abstract int getBufferSize()
meth public abstract java.io.PrintWriter getWriter() throws java.io.IOException
meth public abstract java.lang.String getCharacterEncoding()
meth public abstract java.lang.String getContentType()
meth public abstract java.util.Locale getLocale()
meth public abstract javax.servlet.ServletOutputStream getOutputStream() throws java.io.IOException
meth public abstract void flushBuffer() throws java.io.IOException
meth public abstract void reset()
meth public abstract void resetBuffer()
meth public abstract void setBufferSize(int)
meth public abstract void setCharacterEncoding(java.lang.String)
meth public abstract void setContentLength(int)
meth public abstract void setContentLengthLong(long)
meth public abstract void setContentType(java.lang.String)
meth public abstract void setLocale(java.util.Locale)

CLSS public javax.servlet.ServletResponseWrapper
cons public init(javax.servlet.ServletResponse)
intf javax.servlet.ServletResponse
meth public boolean isCommitted()
meth public boolean isWrapperFor(java.lang.Class<?>)
meth public boolean isWrapperFor(javax.servlet.ServletResponse)
meth public int getBufferSize()
meth public java.io.PrintWriter getWriter() throws java.io.IOException
meth public java.lang.String getCharacterEncoding()
meth public java.lang.String getContentType()
meth public java.util.Locale getLocale()
meth public javax.servlet.ServletOutputStream getOutputStream() throws java.io.IOException
meth public javax.servlet.ServletResponse getResponse()
meth public void flushBuffer() throws java.io.IOException
meth public void reset()
meth public void resetBuffer()
meth public void setBufferSize(int)
meth public void setCharacterEncoding(java.lang.String)
meth public void setContentLength(int)
meth public void setContentLengthLong(long)
meth public void setContentType(java.lang.String)
meth public void setLocale(java.util.Locale)
meth public void setResponse(javax.servlet.ServletResponse)
supr java.lang.Object
hfds response

CLSS public javax.servlet.ServletSecurityElement
cons public init()
cons public init(java.util.Collection<javax.servlet.HttpMethodConstraintElement>)
cons public init(javax.servlet.HttpConstraintElement)
cons public init(javax.servlet.HttpConstraintElement,java.util.Collection<javax.servlet.HttpMethodConstraintElement>)
cons public init(javax.servlet.annotation.ServletSecurity)
meth public java.util.Collection<java.lang.String> getMethodNames()
meth public java.util.Collection<javax.servlet.HttpMethodConstraintElement> getHttpMethodConstraints()
supr javax.servlet.HttpConstraintElement
hfds methodConstraints,methodNames

CLSS public abstract interface javax.servlet.SessionCookieConfig
meth public abstract boolean isHttpOnly()
meth public abstract boolean isSecure()
meth public abstract int getMaxAge()
meth public abstract java.lang.String getComment()
meth public abstract java.lang.String getDomain()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getPath()
meth public abstract void setComment(java.lang.String)
meth public abstract void setDomain(java.lang.String)
meth public abstract void setHttpOnly(boolean)
meth public abstract void setMaxAge(int)
meth public abstract void setName(java.lang.String)
meth public abstract void setPath(java.lang.String)
meth public abstract void setSecure(boolean)

CLSS public final !enum javax.servlet.SessionTrackingMode
fld public final static javax.servlet.SessionTrackingMode COOKIE
fld public final static javax.servlet.SessionTrackingMode SSL
fld public final static javax.servlet.SessionTrackingMode URL
meth public static javax.servlet.SessionTrackingMode valueOf(java.lang.String)
meth public static javax.servlet.SessionTrackingMode[] values()
supr java.lang.Enum<javax.servlet.SessionTrackingMode>

CLSS public abstract interface javax.servlet.SingleThreadModel
 anno 0 java.lang.Deprecated()

CLSS public javax.servlet.UnavailableException
cons public init(int,javax.servlet.Servlet,java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(javax.servlet.Servlet,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public boolean isPermanent()
meth public int getUnavailableSeconds()
meth public javax.servlet.Servlet getServlet()
 anno 0 java.lang.Deprecated()
supr javax.servlet.ServletException
hfds permanent,seconds,serialVersionUID,servlet

CLSS public abstract interface javax.servlet.WriteListener
intf java.util.EventListener
meth public abstract void onError(java.lang.Throwable)
meth public abstract void onWritePossible() throws java.io.IOException

CLSS public abstract interface !annotation javax.servlet.annotation.HandlesTypes
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class<?>[] value()

CLSS public abstract interface !annotation javax.servlet.annotation.HttpConstraint
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] rolesAllowed()
meth public abstract !hasdefault javax.servlet.annotation.ServletSecurity$EmptyRoleSemantic value()
meth public abstract !hasdefault javax.servlet.annotation.ServletSecurity$TransportGuarantee transportGuarantee()

CLSS public abstract interface !annotation javax.servlet.annotation.HttpMethodConstraint
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] rolesAllowed()
meth public abstract !hasdefault javax.servlet.annotation.ServletSecurity$EmptyRoleSemantic emptyRoleSemantic()
meth public abstract !hasdefault javax.servlet.annotation.ServletSecurity$TransportGuarantee transportGuarantee()
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation javax.servlet.annotation.MultipartConfig
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int fileSizeThreshold()
meth public abstract !hasdefault java.lang.String location()
meth public abstract !hasdefault long maxFileSize()
meth public abstract !hasdefault long maxRequestSize()

CLSS public abstract interface !annotation javax.servlet.annotation.ServletSecurity
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Inherited()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
innr public final static !enum EmptyRoleSemantic
innr public final static !enum TransportGuarantee
intf java.lang.annotation.Annotation
meth public abstract !hasdefault javax.servlet.annotation.HttpConstraint value()
meth public abstract !hasdefault javax.servlet.annotation.HttpMethodConstraint[] httpMethodConstraints()

CLSS public final static !enum javax.servlet.annotation.ServletSecurity$EmptyRoleSemantic
 outer javax.servlet.annotation.ServletSecurity
fld public final static javax.servlet.annotation.ServletSecurity$EmptyRoleSemantic DENY
fld public final static javax.servlet.annotation.ServletSecurity$EmptyRoleSemantic PERMIT
meth public static javax.servlet.annotation.ServletSecurity$EmptyRoleSemantic valueOf(java.lang.String)
meth public static javax.servlet.annotation.ServletSecurity$EmptyRoleSemantic[] values()
supr java.lang.Enum<javax.servlet.annotation.ServletSecurity$EmptyRoleSemantic>

CLSS public final static !enum javax.servlet.annotation.ServletSecurity$TransportGuarantee
 outer javax.servlet.annotation.ServletSecurity
fld public final static javax.servlet.annotation.ServletSecurity$TransportGuarantee CONFIDENTIAL
fld public final static javax.servlet.annotation.ServletSecurity$TransportGuarantee NONE
meth public static javax.servlet.annotation.ServletSecurity$TransportGuarantee valueOf(java.lang.String)
meth public static javax.servlet.annotation.ServletSecurity$TransportGuarantee[] values()
supr java.lang.Enum<javax.servlet.annotation.ServletSecurity$TransportGuarantee>

CLSS public abstract interface !annotation javax.servlet.annotation.WebFilter
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean asyncSupported()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String displayName()
meth public abstract !hasdefault java.lang.String filterName()
meth public abstract !hasdefault java.lang.String largeIcon()
meth public abstract !hasdefault java.lang.String smallIcon()
meth public abstract !hasdefault java.lang.String[] servletNames()
meth public abstract !hasdefault java.lang.String[] urlPatterns()
meth public abstract !hasdefault java.lang.String[] value()
meth public abstract !hasdefault javax.servlet.DispatcherType[] dispatcherTypes()
meth public abstract !hasdefault javax.servlet.annotation.WebInitParam[] initParams()

CLSS public abstract interface !annotation javax.servlet.annotation.WebInitParam
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String description()
meth public abstract java.lang.String name()
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation javax.servlet.annotation.WebListener
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface !annotation javax.servlet.annotation.WebServlet
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean asyncSupported()
meth public abstract !hasdefault int loadOnStartup()
meth public abstract !hasdefault java.lang.String description()
meth public abstract !hasdefault java.lang.String displayName()
meth public abstract !hasdefault java.lang.String largeIcon()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String smallIcon()
meth public abstract !hasdefault java.lang.String[] urlPatterns()
meth public abstract !hasdefault java.lang.String[] value()
meth public abstract !hasdefault javax.servlet.annotation.WebInitParam[] initParams()

CLSS public abstract interface javax.servlet.descriptor.JspConfigDescriptor
meth public abstract java.util.Collection<javax.servlet.descriptor.JspPropertyGroupDescriptor> getJspPropertyGroups()
meth public abstract java.util.Collection<javax.servlet.descriptor.TaglibDescriptor> getTaglibs()

CLSS public abstract interface javax.servlet.descriptor.JspPropertyGroupDescriptor
meth public abstract java.lang.String getBuffer()
meth public abstract java.lang.String getDefaultContentType()
meth public abstract java.lang.String getDeferredSyntaxAllowedAsLiteral()
meth public abstract java.lang.String getElIgnored()
meth public abstract java.lang.String getErrorOnUndeclaredNamespace()
meth public abstract java.lang.String getIsXml()
meth public abstract java.lang.String getPageEncoding()
meth public abstract java.lang.String getScriptingInvalid()
meth public abstract java.lang.String getTrimDirectiveWhitespaces()
meth public abstract java.util.Collection<java.lang.String> getIncludeCodas()
meth public abstract java.util.Collection<java.lang.String> getIncludePreludes()
meth public abstract java.util.Collection<java.lang.String> getUrlPatterns()

CLSS public abstract interface javax.servlet.descriptor.TaglibDescriptor
meth public abstract java.lang.String getTaglibLocation()
meth public abstract java.lang.String getTaglibURI()

CLSS public javax.servlet.http.Cookie
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean getSecure()
meth public boolean isHttpOnly()
meth public int getMaxAge()
meth public int getVersion()
meth public java.lang.Object clone()
meth public java.lang.String getComment()
meth public java.lang.String getDomain()
meth public java.lang.String getName()
meth public java.lang.String getPath()
meth public java.lang.String getValue()
meth public void setComment(java.lang.String)
meth public void setDomain(java.lang.String)
meth public void setHttpOnly(boolean)
meth public void setMaxAge(int)
meth public void setPath(java.lang.String)
meth public void setSecure(boolean)
meth public void setValue(java.lang.String)
meth public void setVersion(int)
supr java.lang.Object
hfds LSTRING_FILE,TSPECIALS,comment,domain,isHttpOnly,lStrings,maxAge,name,path,secure,serialVersionUID,value,version

CLSS public abstract javax.servlet.http.HttpFilter
cons public init()
meth protected void doFilter(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse,javax.servlet.FilterChain) throws java.io.IOException,javax.servlet.ServletException
meth public void doFilter(javax.servlet.ServletRequest,javax.servlet.ServletResponse,javax.servlet.FilterChain) throws java.io.IOException,javax.servlet.ServletException
supr javax.servlet.GenericFilter
hfds serialVersionUID

CLSS public abstract javax.servlet.http.HttpServlet
cons public init()
meth protected long getLastModified(javax.servlet.http.HttpServletRequest)
meth protected void doDelete(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse) throws java.io.IOException,javax.servlet.ServletException
meth protected void doGet(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse) throws java.io.IOException,javax.servlet.ServletException
meth protected void doHead(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse) throws java.io.IOException,javax.servlet.ServletException
meth protected void doOptions(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse) throws java.io.IOException,javax.servlet.ServletException
meth protected void doPost(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse) throws java.io.IOException,javax.servlet.ServletException
meth protected void doPut(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse) throws java.io.IOException,javax.servlet.ServletException
meth protected void doTrace(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse) throws java.io.IOException,javax.servlet.ServletException
meth protected void service(javax.servlet.http.HttpServletRequest,javax.servlet.http.HttpServletResponse) throws java.io.IOException,javax.servlet.ServletException
meth public void service(javax.servlet.ServletRequest,javax.servlet.ServletResponse) throws java.io.IOException,javax.servlet.ServletException
supr javax.servlet.GenericServlet
hfds HEADER_IFMODSINCE,HEADER_LASTMOD,LSTRING_FILE,METHOD_DELETE,METHOD_GET,METHOD_HEAD,METHOD_OPTIONS,METHOD_POST,METHOD_PUT,METHOD_TRACE,lStrings,serialVersionUID

CLSS public abstract interface javax.servlet.http.HttpServletMapping
meth public abstract java.lang.String getMatchValue()
meth public abstract java.lang.String getPattern()
meth public abstract java.lang.String getServletName()
meth public abstract javax.servlet.http.MappingMatch getMappingMatch()

CLSS public abstract interface javax.servlet.http.HttpServletRequest
fld public final static java.lang.String BASIC_AUTH = "BASIC"
fld public final static java.lang.String CLIENT_CERT_AUTH = "CLIENT_CERT"
fld public final static java.lang.String DIGEST_AUTH = "DIGEST"
fld public final static java.lang.String FORM_AUTH = "FORM"
intf javax.servlet.ServletRequest
meth public abstract <%0 extends javax.servlet.http.HttpUpgradeHandler> {%%0} upgrade(java.lang.Class<{%%0}>) throws java.io.IOException,javax.servlet.ServletException
meth public abstract boolean authenticate(javax.servlet.http.HttpServletResponse) throws java.io.IOException,javax.servlet.ServletException
meth public abstract boolean isRequestedSessionIdFromCookie()
meth public abstract boolean isRequestedSessionIdFromURL()
meth public abstract boolean isRequestedSessionIdFromUrl()
 anno 0 java.lang.Deprecated()
meth public abstract boolean isRequestedSessionIdValid()
meth public abstract boolean isUserInRole(java.lang.String)
meth public abstract int getIntHeader(java.lang.String)
meth public abstract java.lang.String changeSessionId()
meth public abstract java.lang.String getAuthType()
meth public abstract java.lang.String getContextPath()
meth public abstract java.lang.String getHeader(java.lang.String)
meth public abstract java.lang.String getMethod()
meth public abstract java.lang.String getPathInfo()
meth public abstract java.lang.String getPathTranslated()
meth public abstract java.lang.String getQueryString()
meth public abstract java.lang.String getRemoteUser()
meth public abstract java.lang.String getRequestURI()
meth public abstract java.lang.String getRequestedSessionId()
meth public abstract java.lang.String getServletPath()
meth public abstract java.lang.StringBuffer getRequestURL()
meth public abstract java.security.Principal getUserPrincipal()
meth public abstract java.util.Collection<javax.servlet.http.Part> getParts() throws java.io.IOException,javax.servlet.ServletException
meth public abstract java.util.Enumeration<java.lang.String> getHeaderNames()
meth public abstract java.util.Enumeration<java.lang.String> getHeaders(java.lang.String)
meth public abstract javax.servlet.http.Cookie[] getCookies()
meth public abstract javax.servlet.http.HttpSession getSession()
meth public abstract javax.servlet.http.HttpSession getSession(boolean)
meth public abstract javax.servlet.http.Part getPart(java.lang.String) throws java.io.IOException,javax.servlet.ServletException
meth public abstract long getDateHeader(java.lang.String)
meth public abstract void login(java.lang.String,java.lang.String) throws javax.servlet.ServletException
meth public abstract void logout() throws javax.servlet.ServletException
meth public boolean isTrailerFieldsReady()
meth public java.util.Map<java.lang.String,java.lang.String> getTrailerFields()
meth public javax.servlet.http.HttpServletMapping getHttpServletMapping()
meth public javax.servlet.http.PushBuilder newPushBuilder()

CLSS public javax.servlet.http.HttpServletRequestWrapper
cons public init(javax.servlet.http.HttpServletRequest)
intf javax.servlet.http.HttpServletRequest
meth public <%0 extends javax.servlet.http.HttpUpgradeHandler> {%%0} upgrade(java.lang.Class<{%%0}>) throws java.io.IOException,javax.servlet.ServletException
meth public boolean authenticate(javax.servlet.http.HttpServletResponse) throws java.io.IOException,javax.servlet.ServletException
meth public boolean isRequestedSessionIdFromCookie()
meth public boolean isRequestedSessionIdFromURL()
meth public boolean isRequestedSessionIdFromUrl()
 anno 0 java.lang.Deprecated()
meth public boolean isRequestedSessionIdValid()
meth public boolean isTrailerFieldsReady()
meth public boolean isUserInRole(java.lang.String)
meth public int getIntHeader(java.lang.String)
meth public java.lang.String changeSessionId()
meth public java.lang.String getAuthType()
meth public java.lang.String getContextPath()
meth public java.lang.String getHeader(java.lang.String)
meth public java.lang.String getMethod()
meth public java.lang.String getPathInfo()
meth public java.lang.String getPathTranslated()
meth public java.lang.String getQueryString()
meth public java.lang.String getRemoteUser()
meth public java.lang.String getRequestURI()
meth public java.lang.String getRequestedSessionId()
meth public java.lang.String getServletPath()
meth public java.lang.StringBuffer getRequestURL()
meth public java.security.Principal getUserPrincipal()
meth public java.util.Collection<javax.servlet.http.Part> getParts() throws java.io.IOException,javax.servlet.ServletException
meth public java.util.Enumeration<java.lang.String> getHeaderNames()
meth public java.util.Enumeration<java.lang.String> getHeaders(java.lang.String)
meth public java.util.Map<java.lang.String,java.lang.String> getTrailerFields()
meth public javax.servlet.http.Cookie[] getCookies()
meth public javax.servlet.http.HttpServletMapping getHttpServletMapping()
meth public javax.servlet.http.HttpSession getSession()
meth public javax.servlet.http.HttpSession getSession(boolean)
meth public javax.servlet.http.Part getPart(java.lang.String) throws java.io.IOException,javax.servlet.ServletException
meth public javax.servlet.http.PushBuilder newPushBuilder()
meth public long getDateHeader(java.lang.String)
meth public void login(java.lang.String,java.lang.String) throws javax.servlet.ServletException
meth public void logout() throws javax.servlet.ServletException
supr javax.servlet.ServletRequestWrapper

CLSS public abstract interface javax.servlet.http.HttpServletResponse
fld public final static int SC_ACCEPTED = 202
fld public final static int SC_BAD_GATEWAY = 502
fld public final static int SC_BAD_REQUEST = 400
fld public final static int SC_CONFLICT = 409
fld public final static int SC_CONTINUE = 100
fld public final static int SC_CREATED = 201
fld public final static int SC_EXPECTATION_FAILED = 417
fld public final static int SC_FORBIDDEN = 403
fld public final static int SC_FOUND = 302
fld public final static int SC_GATEWAY_TIMEOUT = 504
fld public final static int SC_GONE = 410
fld public final static int SC_HTTP_VERSION_NOT_SUPPORTED = 505
fld public final static int SC_INTERNAL_SERVER_ERROR = 500
fld public final static int SC_LENGTH_REQUIRED = 411
fld public final static int SC_METHOD_NOT_ALLOWED = 405
fld public final static int SC_MOVED_PERMANENTLY = 301
fld public final static int SC_MOVED_TEMPORARILY = 302
fld public final static int SC_MULTIPLE_CHOICES = 300
fld public final static int SC_NON_AUTHORITATIVE_INFORMATION = 203
fld public final static int SC_NOT_ACCEPTABLE = 406
fld public final static int SC_NOT_FOUND = 404
fld public final static int SC_NOT_IMPLEMENTED = 501
fld public final static int SC_NOT_MODIFIED = 304
fld public final static int SC_NO_CONTENT = 204
fld public final static int SC_OK = 200
fld public final static int SC_PARTIAL_CONTENT = 206
fld public final static int SC_PAYMENT_REQUIRED = 402
fld public final static int SC_PRECONDITION_FAILED = 412
fld public final static int SC_PROXY_AUTHENTICATION_REQUIRED = 407
fld public final static int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416
fld public final static int SC_REQUEST_ENTITY_TOO_LARGE = 413
fld public final static int SC_REQUEST_TIMEOUT = 408
fld public final static int SC_REQUEST_URI_TOO_LONG = 414
fld public final static int SC_RESET_CONTENT = 205
fld public final static int SC_SEE_OTHER = 303
fld public final static int SC_SERVICE_UNAVAILABLE = 503
fld public final static int SC_SWITCHING_PROTOCOLS = 101
fld public final static int SC_TEMPORARY_REDIRECT = 307
fld public final static int SC_UNAUTHORIZED = 401
fld public final static int SC_UNSUPPORTED_MEDIA_TYPE = 415
fld public final static int SC_USE_PROXY = 305
intf javax.servlet.ServletResponse
meth public abstract boolean containsHeader(java.lang.String)
meth public abstract int getStatus()
meth public abstract java.lang.String encodeRedirectURL(java.lang.String)
meth public abstract java.lang.String encodeRedirectUrl(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.String encodeURL(java.lang.String)
meth public abstract java.lang.String encodeUrl(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.String getHeader(java.lang.String)
meth public abstract java.util.Collection<java.lang.String> getHeaderNames()
meth public abstract java.util.Collection<java.lang.String> getHeaders(java.lang.String)
meth public abstract void addCookie(javax.servlet.http.Cookie)
meth public abstract void addDateHeader(java.lang.String,long)
meth public abstract void addHeader(java.lang.String,java.lang.String)
meth public abstract void addIntHeader(java.lang.String,int)
meth public abstract void sendError(int) throws java.io.IOException
meth public abstract void sendError(int,java.lang.String) throws java.io.IOException
meth public abstract void sendRedirect(java.lang.String) throws java.io.IOException
meth public abstract void setDateHeader(java.lang.String,long)
meth public abstract void setHeader(java.lang.String,java.lang.String)
meth public abstract void setIntHeader(java.lang.String,int)
meth public abstract void setStatus(int)
meth public abstract void setStatus(int,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.util.function.Supplier<java.util.Map<java.lang.String,java.lang.String>> getTrailerFields()
meth public void setTrailerFields(java.util.function.Supplier<java.util.Map<java.lang.String,java.lang.String>>)

CLSS public javax.servlet.http.HttpServletResponseWrapper
cons public init(javax.servlet.http.HttpServletResponse)
intf javax.servlet.http.HttpServletResponse
meth public boolean containsHeader(java.lang.String)
meth public int getStatus()
meth public java.lang.String encodeRedirectURL(java.lang.String)
meth public java.lang.String encodeRedirectUrl(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.String encodeURL(java.lang.String)
meth public java.lang.String encodeUrl(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.String getHeader(java.lang.String)
meth public java.util.Collection<java.lang.String> getHeaderNames()
meth public java.util.Collection<java.lang.String> getHeaders(java.lang.String)
meth public java.util.function.Supplier<java.util.Map<java.lang.String,java.lang.String>> getTrailerFields()
meth public void addCookie(javax.servlet.http.Cookie)
meth public void addDateHeader(java.lang.String,long)
meth public void addHeader(java.lang.String,java.lang.String)
meth public void addIntHeader(java.lang.String,int)
meth public void sendError(int) throws java.io.IOException
meth public void sendError(int,java.lang.String) throws java.io.IOException
meth public void sendRedirect(java.lang.String) throws java.io.IOException
meth public void setDateHeader(java.lang.String,long)
meth public void setHeader(java.lang.String,java.lang.String)
meth public void setIntHeader(java.lang.String,int)
meth public void setStatus(int)
meth public void setStatus(int,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setTrailerFields(java.util.function.Supplier<java.util.Map<java.lang.String,java.lang.String>>)
supr javax.servlet.ServletResponseWrapper

CLSS public abstract interface javax.servlet.http.HttpSession
meth public abstract boolean isNew()
meth public abstract int getMaxInactiveInterval()
meth public abstract java.lang.Object getAttribute(java.lang.String)
meth public abstract java.lang.Object getValue(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.String getId()
meth public abstract java.lang.String[] getValueNames()
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Enumeration<java.lang.String> getAttributeNames()
meth public abstract javax.servlet.ServletContext getServletContext()
meth public abstract javax.servlet.http.HttpSessionContext getSessionContext()
 anno 0 java.lang.Deprecated()
meth public abstract long getCreationTime()
meth public abstract long getLastAccessedTime()
meth public abstract void invalidate()
meth public abstract void putValue(java.lang.String,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth public abstract void removeAttribute(java.lang.String)
meth public abstract void removeValue(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public abstract void setAttribute(java.lang.String,java.lang.Object)
meth public abstract void setMaxInactiveInterval(int)

CLSS public abstract interface javax.servlet.http.HttpSessionActivationListener
intf java.util.EventListener
meth public void sessionDidActivate(javax.servlet.http.HttpSessionEvent)
meth public void sessionWillPassivate(javax.servlet.http.HttpSessionEvent)

CLSS public abstract interface javax.servlet.http.HttpSessionAttributeListener
intf java.util.EventListener
meth public void attributeAdded(javax.servlet.http.HttpSessionBindingEvent)
meth public void attributeRemoved(javax.servlet.http.HttpSessionBindingEvent)
meth public void attributeReplaced(javax.servlet.http.HttpSessionBindingEvent)

CLSS public javax.servlet.http.HttpSessionBindingEvent
cons public init(javax.servlet.http.HttpSession,java.lang.String)
cons public init(javax.servlet.http.HttpSession,java.lang.String,java.lang.Object)
meth public java.lang.Object getValue()
meth public java.lang.String getName()
meth public javax.servlet.http.HttpSession getSession()
supr javax.servlet.http.HttpSessionEvent
hfds name,serialVersionUID,value

CLSS public abstract interface javax.servlet.http.HttpSessionBindingListener
intf java.util.EventListener
meth public void valueBound(javax.servlet.http.HttpSessionBindingEvent)
meth public void valueUnbound(javax.servlet.http.HttpSessionBindingEvent)

CLSS public abstract interface javax.servlet.http.HttpSessionContext
 anno 0 java.lang.Deprecated()
meth public abstract java.util.Enumeration<java.lang.String> getIds()
 anno 0 java.lang.Deprecated()
meth public abstract javax.servlet.http.HttpSession getSession(java.lang.String)
 anno 0 java.lang.Deprecated()

CLSS public javax.servlet.http.HttpSessionEvent
cons public init(javax.servlet.http.HttpSession)
meth public javax.servlet.http.HttpSession getSession()
supr java.util.EventObject
hfds serialVersionUID

CLSS public abstract interface javax.servlet.http.HttpSessionIdListener
intf java.util.EventListener
meth public abstract void sessionIdChanged(javax.servlet.http.HttpSessionEvent,java.lang.String)

CLSS public abstract interface javax.servlet.http.HttpSessionListener
intf java.util.EventListener
meth public void sessionCreated(javax.servlet.http.HttpSessionEvent)
meth public void sessionDestroyed(javax.servlet.http.HttpSessionEvent)

CLSS public abstract interface javax.servlet.http.HttpUpgradeHandler
meth public abstract void destroy()
meth public abstract void init(javax.servlet.http.WebConnection)

CLSS public javax.servlet.http.HttpUtils
 anno 0 java.lang.Deprecated()
cons public init()
meth public static java.lang.StringBuffer getRequestURL(javax.servlet.http.HttpServletRequest)
meth public static java.util.Hashtable<java.lang.String,java.lang.String[]> parsePostData(int,javax.servlet.ServletInputStream)
meth public static java.util.Hashtable<java.lang.String,java.lang.String[]> parseQueryString(java.lang.String)
supr java.lang.Object
hfds LSTRING_FILE,lStrings

CLSS public final !enum javax.servlet.http.MappingMatch
fld public final static javax.servlet.http.MappingMatch CONTEXT_ROOT
fld public final static javax.servlet.http.MappingMatch DEFAULT
fld public final static javax.servlet.http.MappingMatch EXACT
fld public final static javax.servlet.http.MappingMatch EXTENSION
fld public final static javax.servlet.http.MappingMatch PATH
meth public static javax.servlet.http.MappingMatch valueOf(java.lang.String)
meth public static javax.servlet.http.MappingMatch[] values()
supr java.lang.Enum<javax.servlet.http.MappingMatch>

CLSS public abstract interface javax.servlet.http.Part
meth public abstract java.io.InputStream getInputStream() throws java.io.IOException
meth public abstract java.lang.String getContentType()
meth public abstract java.lang.String getHeader(java.lang.String)
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getSubmittedFileName()
meth public abstract java.util.Collection<java.lang.String> getHeaderNames()
meth public abstract java.util.Collection<java.lang.String> getHeaders(java.lang.String)
meth public abstract long getSize()
meth public abstract void delete() throws java.io.IOException
meth public abstract void write(java.lang.String) throws java.io.IOException

CLSS public abstract interface javax.servlet.http.PushBuilder
meth public abstract java.lang.String getHeader(java.lang.String)
meth public abstract java.lang.String getMethod()
meth public abstract java.lang.String getPath()
meth public abstract java.lang.String getQueryString()
meth public abstract java.lang.String getSessionId()
meth public abstract java.util.Set<java.lang.String> getHeaderNames()
meth public abstract javax.servlet.http.PushBuilder addHeader(java.lang.String,java.lang.String)
meth public abstract javax.servlet.http.PushBuilder method(java.lang.String)
meth public abstract javax.servlet.http.PushBuilder path(java.lang.String)
meth public abstract javax.servlet.http.PushBuilder queryString(java.lang.String)
meth public abstract javax.servlet.http.PushBuilder removeHeader(java.lang.String)
meth public abstract javax.servlet.http.PushBuilder sessionId(java.lang.String)
meth public abstract javax.servlet.http.PushBuilder setHeader(java.lang.String,java.lang.String)
meth public abstract void push()

CLSS public abstract interface javax.servlet.http.WebConnection
intf java.lang.AutoCloseable
meth public abstract javax.servlet.ServletInputStream getInputStream() throws java.io.IOException
meth public abstract javax.servlet.ServletOutputStream getOutputStream() throws java.io.IOException

