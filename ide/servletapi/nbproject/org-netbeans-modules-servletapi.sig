#Signature file v4.1
#Version 1.59

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

