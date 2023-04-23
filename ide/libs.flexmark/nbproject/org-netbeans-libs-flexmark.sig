#Signature file v4.1
#Version 1.12

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

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

CLSS public abstract interface org.jsoup.Connection
innr public abstract interface static Base
innr public abstract interface static KeyVal
innr public abstract interface static Request
innr public abstract interface static Response
innr public final static !enum Method
meth public abstract !varargs org.jsoup.Connection data(java.lang.String[])
meth public abstract org.jsoup.Connection cookie(java.lang.String,java.lang.String)
meth public abstract org.jsoup.Connection cookies(java.util.Map<java.lang.String,java.lang.String>)
meth public abstract org.jsoup.Connection data(java.lang.String,java.lang.String)
meth public abstract org.jsoup.Connection data(java.lang.String,java.lang.String,java.io.InputStream)
meth public abstract org.jsoup.Connection data(java.lang.String,java.lang.String,java.io.InputStream,java.lang.String)
meth public abstract org.jsoup.Connection data(java.util.Collection<org.jsoup.Connection$KeyVal>)
meth public abstract org.jsoup.Connection data(java.util.Map<java.lang.String,java.lang.String>)
meth public abstract org.jsoup.Connection followRedirects(boolean)
meth public abstract org.jsoup.Connection header(java.lang.String,java.lang.String)
meth public abstract org.jsoup.Connection headers(java.util.Map<java.lang.String,java.lang.String>)
meth public abstract org.jsoup.Connection ignoreContentType(boolean)
meth public abstract org.jsoup.Connection ignoreHttpErrors(boolean)
meth public abstract org.jsoup.Connection maxBodySize(int)
meth public abstract org.jsoup.Connection method(org.jsoup.Connection$Method)
meth public abstract org.jsoup.Connection parser(org.jsoup.parser.Parser)
meth public abstract org.jsoup.Connection postDataCharset(java.lang.String)
meth public abstract org.jsoup.Connection proxy(java.lang.String,int)
meth public abstract org.jsoup.Connection proxy(java.net.Proxy)
meth public abstract org.jsoup.Connection referrer(java.lang.String)
meth public abstract org.jsoup.Connection request(org.jsoup.Connection$Request)
meth public abstract org.jsoup.Connection requestBody(java.lang.String)
meth public abstract org.jsoup.Connection response(org.jsoup.Connection$Response)
meth public abstract org.jsoup.Connection sslSocketFactory(javax.net.ssl.SSLSocketFactory)
meth public abstract org.jsoup.Connection timeout(int)
meth public abstract org.jsoup.Connection url(java.lang.String)
meth public abstract org.jsoup.Connection url(java.net.URL)
meth public abstract org.jsoup.Connection userAgent(java.lang.String)
meth public abstract org.jsoup.Connection validateTLSCertificates(boolean)
meth public abstract org.jsoup.Connection$KeyVal data(java.lang.String)
meth public abstract org.jsoup.Connection$Request request()
meth public abstract org.jsoup.Connection$Response execute() throws java.io.IOException
meth public abstract org.jsoup.Connection$Response response()
meth public abstract org.jsoup.nodes.Document get() throws java.io.IOException
meth public abstract org.jsoup.nodes.Document post() throws java.io.IOException

CLSS public abstract interface static org.jsoup.Connection$Base<%0 extends org.jsoup.Connection$Base>
 outer org.jsoup.Connection
meth public abstract boolean hasCookie(java.lang.String)
meth public abstract boolean hasHeader(java.lang.String)
meth public abstract boolean hasHeaderWithValue(java.lang.String,java.lang.String)
meth public abstract java.lang.String cookie(java.lang.String)
meth public abstract java.lang.String header(java.lang.String)
meth public abstract java.net.URL url()
meth public abstract java.util.List<java.lang.String> headers(java.lang.String)
meth public abstract java.util.Map<java.lang.String,java.lang.String> cookies()
meth public abstract java.util.Map<java.lang.String,java.lang.String> headers()
meth public abstract java.util.Map<java.lang.String,java.util.List<java.lang.String>> multiHeaders()
meth public abstract org.jsoup.Connection$Method method()
meth public abstract {org.jsoup.Connection$Base%0} addHeader(java.lang.String,java.lang.String)
meth public abstract {org.jsoup.Connection$Base%0} cookie(java.lang.String,java.lang.String)
meth public abstract {org.jsoup.Connection$Base%0} header(java.lang.String,java.lang.String)
meth public abstract {org.jsoup.Connection$Base%0} method(org.jsoup.Connection$Method)
meth public abstract {org.jsoup.Connection$Base%0} removeCookie(java.lang.String)
meth public abstract {org.jsoup.Connection$Base%0} removeHeader(java.lang.String)
meth public abstract {org.jsoup.Connection$Base%0} url(java.net.URL)

CLSS public abstract interface static org.jsoup.Connection$KeyVal
 outer org.jsoup.Connection
meth public abstract boolean hasInputStream()
meth public abstract java.io.InputStream inputStream()
meth public abstract java.lang.String contentType()
meth public abstract java.lang.String key()
meth public abstract java.lang.String value()
meth public abstract org.jsoup.Connection$KeyVal contentType(java.lang.String)
meth public abstract org.jsoup.Connection$KeyVal inputStream(java.io.InputStream)
meth public abstract org.jsoup.Connection$KeyVal key(java.lang.String)
meth public abstract org.jsoup.Connection$KeyVal value(java.lang.String)

CLSS public final static !enum org.jsoup.Connection$Method
 outer org.jsoup.Connection
fld public final static org.jsoup.Connection$Method DELETE
fld public final static org.jsoup.Connection$Method GET
fld public final static org.jsoup.Connection$Method HEAD
fld public final static org.jsoup.Connection$Method OPTIONS
fld public final static org.jsoup.Connection$Method PATCH
fld public final static org.jsoup.Connection$Method POST
fld public final static org.jsoup.Connection$Method PUT
fld public final static org.jsoup.Connection$Method TRACE
meth public final boolean hasBody()
meth public static org.jsoup.Connection$Method valueOf(java.lang.String)
meth public static org.jsoup.Connection$Method[] values()
supr java.lang.Enum<org.jsoup.Connection$Method>
hfds hasBody

CLSS public abstract interface static org.jsoup.Connection$Request
 outer org.jsoup.Connection
intf org.jsoup.Connection$Base<org.jsoup.Connection$Request>
meth public abstract boolean followRedirects()
meth public abstract boolean ignoreContentType()
meth public abstract boolean ignoreHttpErrors()
meth public abstract boolean validateTLSCertificates()
meth public abstract int maxBodySize()
meth public abstract int timeout()
meth public abstract java.lang.String postDataCharset()
meth public abstract java.lang.String requestBody()
meth public abstract java.net.Proxy proxy()
meth public abstract java.util.Collection<org.jsoup.Connection$KeyVal> data()
meth public abstract javax.net.ssl.SSLSocketFactory sslSocketFactory()
meth public abstract org.jsoup.Connection$Request data(org.jsoup.Connection$KeyVal)
meth public abstract org.jsoup.Connection$Request followRedirects(boolean)
meth public abstract org.jsoup.Connection$Request ignoreContentType(boolean)
meth public abstract org.jsoup.Connection$Request ignoreHttpErrors(boolean)
meth public abstract org.jsoup.Connection$Request maxBodySize(int)
meth public abstract org.jsoup.Connection$Request parser(org.jsoup.parser.Parser)
meth public abstract org.jsoup.Connection$Request postDataCharset(java.lang.String)
meth public abstract org.jsoup.Connection$Request proxy(java.lang.String,int)
meth public abstract org.jsoup.Connection$Request proxy(java.net.Proxy)
meth public abstract org.jsoup.Connection$Request requestBody(java.lang.String)
meth public abstract org.jsoup.Connection$Request timeout(int)
meth public abstract org.jsoup.parser.Parser parser()
meth public abstract void sslSocketFactory(javax.net.ssl.SSLSocketFactory)
meth public abstract void validateTLSCertificates(boolean)

CLSS public abstract interface static org.jsoup.Connection$Response
 outer org.jsoup.Connection
intf org.jsoup.Connection$Base<org.jsoup.Connection$Response>
meth public abstract byte[] bodyAsBytes()
meth public abstract int statusCode()
meth public abstract java.io.BufferedInputStream bodyStream()
meth public abstract java.lang.String body()
meth public abstract java.lang.String charset()
meth public abstract java.lang.String contentType()
meth public abstract java.lang.String statusMessage()
meth public abstract org.jsoup.Connection$Response bufferUp()
meth public abstract org.jsoup.Connection$Response charset(java.lang.String)
meth public abstract org.jsoup.nodes.Document parse() throws java.io.IOException

CLSS public org.jsoup.HttpStatusException
cons public init(java.lang.String,int,java.lang.String)
meth public int getStatusCode()
meth public java.lang.String getUrl()
meth public java.lang.String toString()
supr java.io.IOException
hfds statusCode,url

CLSS public org.jsoup.Jsoup
meth public static boolean isValid(java.lang.String,org.jsoup.safety.Whitelist)
meth public static java.lang.String clean(java.lang.String,java.lang.String,org.jsoup.safety.Whitelist)
meth public static java.lang.String clean(java.lang.String,java.lang.String,org.jsoup.safety.Whitelist,org.jsoup.nodes.Document$OutputSettings)
meth public static java.lang.String clean(java.lang.String,org.jsoup.safety.Whitelist)
meth public static org.jsoup.Connection connect(java.lang.String)
meth public static org.jsoup.nodes.Document parse(java.io.File,java.lang.String) throws java.io.IOException
meth public static org.jsoup.nodes.Document parse(java.io.File,java.lang.String,java.lang.String) throws java.io.IOException
meth public static org.jsoup.nodes.Document parse(java.io.InputStream,java.lang.String,java.lang.String) throws java.io.IOException
meth public static org.jsoup.nodes.Document parse(java.io.InputStream,java.lang.String,java.lang.String,org.jsoup.parser.Parser) throws java.io.IOException
meth public static org.jsoup.nodes.Document parse(java.lang.String)
meth public static org.jsoup.nodes.Document parse(java.lang.String,java.lang.String)
meth public static org.jsoup.nodes.Document parse(java.lang.String,java.lang.String,org.jsoup.parser.Parser)
meth public static org.jsoup.nodes.Document parse(java.net.URL,int) throws java.io.IOException
meth public static org.jsoup.nodes.Document parseBodyFragment(java.lang.String)
meth public static org.jsoup.nodes.Document parseBodyFragment(java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public final org.jsoup.SerializationException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

CLSS public org.jsoup.UncheckedIOException
cons public init(java.io.IOException)
meth public java.io.IOException ioException()
supr java.lang.RuntimeException

CLSS public org.jsoup.UnsupportedMimeTypeException
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getMimeType()
meth public java.lang.String getUrl()
meth public java.lang.String toString()
supr java.io.IOException
hfds mimeType,url

