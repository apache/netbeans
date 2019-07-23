#Signature file v4.1
#Version 1.44

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
hfds MAX_SKIP_BUFFER_SIZE

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

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

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
hfds CAUSE_CAPTION,EMPTY_THROWABLE_ARRAY,NULL_CAUSE_MESSAGE,SELF_SUPPRESSION_MESSAGE,SUPPRESSED_CAPTION,SUPPRESSED_SENTINEL,UNASSIGNED_STACK,backtrace,cause,detailMessage,serialVersionUID,stackTrace,suppressedExceptions
hcls PrintStreamOrWriter,SentinelHolder,WrappedPrintStream,WrappedPrintWriter

CLSS public abstract javax.servlet.GenericServlet
cons public init()
intf java.io.Serializable
intf javax.servlet.Servlet
intf javax.servlet.ServletConfig
meth public abstract void service(javax.servlet.ServletRequest,javax.servlet.ServletResponse) throws java.io.IOException,javax.servlet.ServletException
meth public java.lang.String getInitParameter(java.lang.String)
meth public java.lang.String getServletInfo()
meth public java.lang.String getServletName()
meth public java.util.Enumeration getInitParameterNames()
meth public javax.servlet.ServletConfig getServletConfig()
meth public javax.servlet.ServletContext getServletContext()
meth public void destroy()
meth public void init() throws javax.servlet.ServletException
meth public void init(javax.servlet.ServletConfig) throws javax.servlet.ServletException
meth public void log(java.lang.String)
meth public void log(java.lang.String,java.lang.Throwable)
supr java.lang.Object
hfds config

CLSS public abstract interface javax.servlet.RequestDispatcher
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
meth public abstract java.util.Enumeration getInitParameterNames()
meth public abstract javax.servlet.ServletContext getServletContext()

CLSS public abstract interface javax.servlet.ServletContext
meth public abstract int getMajorVersion()
meth public abstract int getMinorVersion()
meth public abstract java.io.InputStream getResourceAsStream(java.lang.String)
meth public abstract java.lang.Object getAttribute(java.lang.String)
meth public abstract java.lang.String getInitParameter(java.lang.String)
meth public abstract java.lang.String getMimeType(java.lang.String)
meth public abstract java.lang.String getRealPath(java.lang.String)
meth public abstract java.lang.String getServerInfo()
meth public abstract java.net.URL getResource(java.lang.String) throws java.net.MalformedURLException
meth public abstract java.util.Enumeration getAttributeNames()
meth public abstract java.util.Enumeration getInitParameterNames()
meth public abstract java.util.Enumeration getServletNames()
meth public abstract java.util.Enumeration getServlets()
meth public abstract javax.servlet.RequestDispatcher getNamedDispatcher(java.lang.String)
meth public abstract javax.servlet.RequestDispatcher getRequestDispatcher(java.lang.String)
meth public abstract javax.servlet.Servlet getServlet(java.lang.String) throws javax.servlet.ServletException
meth public abstract javax.servlet.ServletContext getContext(java.lang.String)
meth public abstract void log(java.lang.Exception,java.lang.String)
meth public abstract void log(java.lang.String)
meth public abstract void log(java.lang.String,java.lang.Throwable)
meth public abstract void removeAttribute(java.lang.String)
meth public abstract void setAttribute(java.lang.String,java.lang.Object)

CLSS public javax.servlet.ServletException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public java.lang.Throwable getRootCause()
supr java.lang.Exception
hfds rootCause

CLSS public abstract javax.servlet.ServletInputStream
cons protected init()
meth public int readLine(byte[],int,int) throws java.io.IOException
supr java.io.InputStream

CLSS public abstract javax.servlet.ServletOutputStream
cons protected init()
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

CLSS public abstract interface javax.servlet.ServletRequest
meth public abstract boolean isSecure()
meth public abstract int getContentLength()
meth public abstract int getServerPort()
meth public abstract java.io.BufferedReader getReader() throws java.io.IOException
meth public abstract java.lang.Object getAttribute(java.lang.String)
meth public abstract java.lang.String getCharacterEncoding()
meth public abstract java.lang.String getContentType()
meth public abstract java.lang.String getParameter(java.lang.String)
meth public abstract java.lang.String getProtocol()
meth public abstract java.lang.String getRealPath(java.lang.String)
meth public abstract java.lang.String getRemoteAddr()
meth public abstract java.lang.String getRemoteHost()
meth public abstract java.lang.String getScheme()
meth public abstract java.lang.String getServerName()
meth public abstract java.lang.String[] getParameterValues(java.lang.String)
meth public abstract java.util.Enumeration getAttributeNames()
meth public abstract java.util.Enumeration getLocales()
meth public abstract java.util.Enumeration getParameterNames()
meth public abstract java.util.Locale getLocale()
meth public abstract javax.servlet.RequestDispatcher getRequestDispatcher(java.lang.String)
meth public abstract javax.servlet.ServletInputStream getInputStream() throws java.io.IOException
meth public abstract void removeAttribute(java.lang.String)
meth public abstract void setAttribute(java.lang.String,java.lang.Object)

CLSS public abstract interface javax.servlet.ServletResponse
meth public abstract boolean isCommitted()
meth public abstract int getBufferSize()
meth public abstract java.io.PrintWriter getWriter() throws java.io.IOException
meth public abstract java.lang.String getCharacterEncoding()
meth public abstract java.util.Locale getLocale()
meth public abstract javax.servlet.ServletOutputStream getOutputStream() throws java.io.IOException
meth public abstract void flushBuffer() throws java.io.IOException
meth public abstract void reset()
meth public abstract void setBufferSize(int)
meth public abstract void setContentLength(int)
meth public abstract void setContentType(java.lang.String)
meth public abstract void setLocale(java.util.Locale)

CLSS public abstract interface javax.servlet.SingleThreadModel

CLSS public javax.servlet.UnavailableException
cons public init(int,javax.servlet.Servlet,java.lang.String)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(javax.servlet.Servlet,java.lang.String)
meth public boolean isPermanent()
meth public int getUnavailableSeconds()
meth public javax.servlet.Servlet getServlet()
supr javax.servlet.ServletException
hfds permanent,seconds,servlet

