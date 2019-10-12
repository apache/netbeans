#Signature file v4.1
#Version 2.13

CLSS public abstract interface !annotation com.google.common.annotations.GwtCompatible
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=false, boolean serializable=false)
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean emulated()
meth public abstract !hasdefault boolean serializable()

CLSS public abstract interface com.google.common.base.Function<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 com.google.common.annotations.GwtCompatible(boolean emulated=false, boolean serializable=false)
meth public abstract boolean equals(java.lang.Object)
 anno 1 javax.annotation.Nullable()
meth public abstract {com.google.common.base.Function%1} apply({com.google.common.base.Function%0})
 anno 0 javax.annotation.Nullable()
 anno 1 javax.annotation.Nullable()

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

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
hfds name,ordinal

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

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

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

CLSS public abstract interface java.util.Map<%0 extends java.lang.Object, %1 extends java.lang.Object>
innr public abstract interface static Entry
meth public abstract boolean containsKey(java.lang.Object)
meth public abstract boolean containsValue(java.lang.Object)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.util.Collection<{java.util.Map%1}> values()
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.Map%0},{java.util.Map%1}>> entrySet()
meth public abstract java.util.Set<{java.util.Map%0}> keySet()
meth public abstract void clear()
meth public abstract void putAll(java.util.Map<? extends {java.util.Map%0},? extends {java.util.Map%1}>)
meth public abstract {java.util.Map%1} get(java.lang.Object)
meth public abstract {java.util.Map%1} put({java.util.Map%0},{java.util.Map%1})
meth public abstract {java.util.Map%1} remove(java.lang.Object)
meth public boolean remove(java.lang.Object,java.lang.Object)
meth public boolean replace({java.util.Map%0},{java.util.Map%1},{java.util.Map%1})
meth public void forEach(java.util.function.BiConsumer<? super {java.util.Map%0},? super {java.util.Map%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} compute({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfAbsent({java.util.Map%0},java.util.function.Function<? super {java.util.Map%0},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} computeIfPresent({java.util.Map%0},java.util.function.BiFunction<? super {java.util.Map%0},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} getOrDefault(java.lang.Object,{java.util.Map%1})
meth public {java.util.Map%1} merge({java.util.Map%0},{java.util.Map%1},java.util.function.BiFunction<? super {java.util.Map%1},? super {java.util.Map%1},? extends {java.util.Map%1}>)
meth public {java.util.Map%1} putIfAbsent({java.util.Map%0},{java.util.Map%1})
meth public {java.util.Map%1} replace({java.util.Map%0},{java.util.Map%1})

CLSS public abstract interface !annotation javax.ws.rs.ApplicationPath
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS public javax.ws.rs.BadRequestException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,javax.ws.rs.core.Response)
cons public init(java.lang.String,javax.ws.rs.core.Response,java.lang.Throwable)
cons public init(java.lang.Throwable)
cons public init(javax.ws.rs.core.Response)
cons public init(javax.ws.rs.core.Response,java.lang.Throwable)
supr javax.ws.rs.ClientErrorException
hfds serialVersionUID

CLSS public abstract interface !annotation javax.ws.rs.BeanParam
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER, METHOD, FIELD])
intf java.lang.annotation.Annotation

CLSS public javax.ws.rs.ClientErrorException
cons public init(int)
cons public init(int,java.lang.Throwable)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,java.lang.Throwable)
cons public init(java.lang.String,javax.ws.rs.core.Response$Status)
cons public init(java.lang.String,javax.ws.rs.core.Response$Status,java.lang.Throwable)
cons public init(java.lang.String,javax.ws.rs.core.Response)
cons public init(java.lang.String,javax.ws.rs.core.Response,java.lang.Throwable)
cons public init(javax.ws.rs.core.Response$Status)
cons public init(javax.ws.rs.core.Response$Status,java.lang.Throwable)
cons public init(javax.ws.rs.core.Response)
cons public init(javax.ws.rs.core.Response,java.lang.Throwable)
supr javax.ws.rs.WebApplicationException
hfds serialVersionUID

CLSS public abstract interface !annotation javax.ws.rs.ConstrainedTo
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract javax.ws.rs.RuntimeType value()

CLSS public abstract interface !annotation javax.ws.rs.Consumes
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Inherited()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] value()

CLSS public abstract interface !annotation javax.ws.rs.CookieParam
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation javax.ws.rs.DELETE
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
 anno 0 javax.ws.rs.HttpMethod(java.lang.String value="DELETE")
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.ws.rs.DefaultValue
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation javax.ws.rs.Encoded
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER, METHOD, FIELD, CONSTRUCTOR, TYPE])
intf java.lang.annotation.Annotation

CLSS public javax.ws.rs.ForbiddenException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,javax.ws.rs.core.Response)
cons public init(java.lang.String,javax.ws.rs.core.Response,java.lang.Throwable)
cons public init(java.lang.Throwable)
cons public init(javax.ws.rs.core.Response)
cons public init(javax.ws.rs.core.Response,java.lang.Throwable)
supr javax.ws.rs.ClientErrorException
hfds serialVersionUID

CLSS public abstract interface !annotation javax.ws.rs.FormParam
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation javax.ws.rs.GET
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
 anno 0 javax.ws.rs.HttpMethod(java.lang.String value="GET")
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.ws.rs.HEAD
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
 anno 0 javax.ws.rs.HttpMethod(java.lang.String value="HEAD")
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.ws.rs.HeaderParam
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation javax.ws.rs.HttpMethod
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
fld public final static java.lang.String DELETE = "DELETE"
fld public final static java.lang.String GET = "GET"
fld public final static java.lang.String HEAD = "HEAD"
fld public final static java.lang.String OPTIONS = "OPTIONS"
fld public final static java.lang.String POST = "POST"
fld public final static java.lang.String PUT = "PUT"
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS public javax.ws.rs.InternalServerErrorException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,javax.ws.rs.core.Response)
cons public init(java.lang.String,javax.ws.rs.core.Response,java.lang.Throwable)
cons public init(java.lang.Throwable)
cons public init(javax.ws.rs.core.Response)
cons public init(javax.ws.rs.core.Response,java.lang.Throwable)
supr javax.ws.rs.ServerErrorException
hfds serialVersionUID

CLSS public abstract interface !annotation javax.ws.rs.MatrixParam
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation javax.ws.rs.NameBinding
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation

CLSS public javax.ws.rs.NotAcceptableException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,javax.ws.rs.core.Response)
cons public init(java.lang.String,javax.ws.rs.core.Response,java.lang.Throwable)
cons public init(java.lang.Throwable)
cons public init(javax.ws.rs.core.Response)
cons public init(javax.ws.rs.core.Response,java.lang.Throwable)
supr javax.ws.rs.ClientErrorException
hfds serialVersionUID

CLSS public javax.ws.rs.NotAllowedException
cons public !varargs init(java.lang.String,java.lang.String,java.lang.String[])
cons public !varargs init(java.lang.String,java.lang.String[])
cons public !varargs init(java.lang.String,java.lang.Throwable,java.lang.String[])
cons public !varargs init(java.lang.Throwable,java.lang.String[])
cons public init(java.lang.String,javax.ws.rs.core.Response)
cons public init(java.lang.String,javax.ws.rs.core.Response,java.lang.Throwable)
cons public init(javax.ws.rs.core.Response)
cons public init(javax.ws.rs.core.Response,java.lang.Throwable)
supr javax.ws.rs.ClientErrorException
hfds serialVersionUID

CLSS public javax.ws.rs.NotAuthorizedException
cons public !varargs init(java.lang.Object,java.lang.Object[])
cons public !varargs init(java.lang.String,java.lang.Object,java.lang.Object[])
cons public !varargs init(java.lang.String,java.lang.Throwable,java.lang.Object,java.lang.Object[])
cons public !varargs init(java.lang.Throwable,java.lang.Object,java.lang.Object[])
cons public init(java.lang.String,javax.ws.rs.core.Response)
cons public init(java.lang.String,javax.ws.rs.core.Response,java.lang.Throwable)
cons public init(javax.ws.rs.core.Response)
cons public init(javax.ws.rs.core.Response,java.lang.Throwable)
meth public java.util.List<java.lang.Object> getChallenges()
supr javax.ws.rs.ClientErrorException
hfds challenges,serialVersionUID

CLSS public javax.ws.rs.NotFoundException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,javax.ws.rs.core.Response)
cons public init(java.lang.String,javax.ws.rs.core.Response,java.lang.Throwable)
cons public init(java.lang.Throwable)
cons public init(javax.ws.rs.core.Response)
cons public init(javax.ws.rs.core.Response,java.lang.Throwable)
supr javax.ws.rs.ClientErrorException
hfds serialVersionUID

CLSS public javax.ws.rs.NotSupportedException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,javax.ws.rs.core.Response)
cons public init(java.lang.String,javax.ws.rs.core.Response,java.lang.Throwable)
cons public init(java.lang.Throwable)
cons public init(javax.ws.rs.core.Response)
cons public init(javax.ws.rs.core.Response,java.lang.Throwable)
supr javax.ws.rs.ClientErrorException
hfds serialVersionUID

CLSS public abstract interface !annotation javax.ws.rs.OPTIONS
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
 anno 0 javax.ws.rs.HttpMethod(java.lang.String value="OPTIONS")
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.ws.rs.POST
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
 anno 0 javax.ws.rs.HttpMethod(java.lang.String value="POST")
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.ws.rs.PUT
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[METHOD])
 anno 0 javax.ws.rs.HttpMethod(java.lang.String value="PUT")
intf java.lang.annotation.Annotation

CLSS public abstract interface !annotation javax.ws.rs.Path
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS public abstract interface !annotation javax.ws.rs.PathParam
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS public final javax.ws.rs.Priorities
fld public final static int AUTHENTICATION = 1000
fld public final static int AUTHORIZATION = 2000
fld public final static int ENTITY_CODER = 4000
fld public final static int HEADER_DECORATOR = 3000
fld public final static int USER = 5000
supr java.lang.Object

CLSS public javax.ws.rs.ProcessingException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public abstract interface !annotation javax.ws.rs.Produces
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Inherited()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String[] value()

CLSS public abstract interface !annotation javax.ws.rs.QueryParam
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER, METHOD, FIELD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String value()

CLSS public javax.ws.rs.RedirectionException
cons public init(int,java.net.URI)
cons public init(java.lang.String,int,java.net.URI)
cons public init(java.lang.String,javax.ws.rs.core.Response$Status,java.net.URI)
cons public init(java.lang.String,javax.ws.rs.core.Response)
cons public init(javax.ws.rs.core.Response$Status,java.net.URI)
cons public init(javax.ws.rs.core.Response)
meth public java.net.URI getLocation()
supr javax.ws.rs.WebApplicationException
hfds serialVersionUID

CLSS public final !enum javax.ws.rs.RuntimeType
fld public final static javax.ws.rs.RuntimeType CLIENT
fld public final static javax.ws.rs.RuntimeType SERVER
meth public static javax.ws.rs.RuntimeType valueOf(java.lang.String)
meth public static javax.ws.rs.RuntimeType[] values()
supr java.lang.Enum<javax.ws.rs.RuntimeType>

CLSS public javax.ws.rs.ServerErrorException
cons public init(int)
cons public init(int,java.lang.Throwable)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,java.lang.Throwable)
cons public init(java.lang.String,javax.ws.rs.core.Response$Status)
cons public init(java.lang.String,javax.ws.rs.core.Response$Status,java.lang.Throwable)
cons public init(java.lang.String,javax.ws.rs.core.Response)
cons public init(java.lang.String,javax.ws.rs.core.Response,java.lang.Throwable)
cons public init(javax.ws.rs.core.Response$Status)
cons public init(javax.ws.rs.core.Response$Status,java.lang.Throwable)
cons public init(javax.ws.rs.core.Response)
cons public init(javax.ws.rs.core.Response,java.lang.Throwable)
supr javax.ws.rs.WebApplicationException
hfds serialVersionUID

CLSS public javax.ws.rs.ServiceUnavailableException
cons public init()
cons public init(java.lang.Long)
cons public init(java.lang.Long,java.lang.Throwable)
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Long)
cons public init(java.lang.String,java.lang.Long,java.lang.Throwable)
cons public init(java.lang.String,java.util.Date)
cons public init(java.lang.String,java.util.Date,java.lang.Throwable)
cons public init(java.lang.String,javax.ws.rs.core.Response)
cons public init(java.lang.String,javax.ws.rs.core.Response,java.lang.Throwable)
cons public init(java.util.Date)
cons public init(java.util.Date,java.lang.Throwable)
cons public init(javax.ws.rs.core.Response)
cons public init(javax.ws.rs.core.Response,java.lang.Throwable)
meth public boolean hasRetryAfter()
meth public java.util.Date getRetryTime(java.util.Date)
supr javax.ws.rs.ServerErrorException
hfds serialVersionUID

CLSS public javax.ws.rs.WebApplicationException
cons public init()
cons public init(int)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,java.lang.Throwable,int)
cons public init(java.lang.String,java.lang.Throwable,javax.ws.rs.core.Response$Status)
cons public init(java.lang.String,java.lang.Throwable,javax.ws.rs.core.Response)
cons public init(java.lang.String,javax.ws.rs.core.Response$Status)
cons public init(java.lang.String,javax.ws.rs.core.Response)
cons public init(java.lang.Throwable)
cons public init(java.lang.Throwable,int)
cons public init(java.lang.Throwable,javax.ws.rs.core.Response$Status)
cons public init(java.lang.Throwable,javax.ws.rs.core.Response)
cons public init(javax.ws.rs.core.Response$Status)
cons public init(javax.ws.rs.core.Response)
meth public javax.ws.rs.core.Response getResponse()
supr java.lang.RuntimeException
hfds response,serialVersionUID

CLSS public abstract interface javax.ws.rs.client.AsyncInvoker
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> delete(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> delete(javax.ws.rs.client.InvocationCallback<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> delete(javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> get(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> get(javax.ws.rs.client.InvocationCallback<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> get(javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> method(java.lang.String,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> method(java.lang.String,javax.ws.rs.client.Entity<?>,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> method(java.lang.String,javax.ws.rs.client.Entity<?>,javax.ws.rs.client.InvocationCallback<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> method(java.lang.String,javax.ws.rs.client.Entity<?>,javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> method(java.lang.String,javax.ws.rs.client.InvocationCallback<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> method(java.lang.String,javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> options(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> options(javax.ws.rs.client.InvocationCallback<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> options(javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> post(javax.ws.rs.client.Entity<?>,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> post(javax.ws.rs.client.Entity<?>,javax.ws.rs.client.InvocationCallback<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> post(javax.ws.rs.client.Entity<?>,javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> put(javax.ws.rs.client.Entity<?>,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> put(javax.ws.rs.client.Entity<?>,javax.ws.rs.client.InvocationCallback<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> put(javax.ws.rs.client.Entity<?>,javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> trace(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> trace(javax.ws.rs.client.InvocationCallback<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> trace(javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract java.util.concurrent.Future<javax.ws.rs.core.Response> delete()
meth public abstract java.util.concurrent.Future<javax.ws.rs.core.Response> get()
meth public abstract java.util.concurrent.Future<javax.ws.rs.core.Response> head()
meth public abstract java.util.concurrent.Future<javax.ws.rs.core.Response> head(javax.ws.rs.client.InvocationCallback<javax.ws.rs.core.Response>)
meth public abstract java.util.concurrent.Future<javax.ws.rs.core.Response> method(java.lang.String)
meth public abstract java.util.concurrent.Future<javax.ws.rs.core.Response> method(java.lang.String,javax.ws.rs.client.Entity<?>)
meth public abstract java.util.concurrent.Future<javax.ws.rs.core.Response> options()
meth public abstract java.util.concurrent.Future<javax.ws.rs.core.Response> post(javax.ws.rs.client.Entity<?>)
meth public abstract java.util.concurrent.Future<javax.ws.rs.core.Response> put(javax.ws.rs.client.Entity<?>)
meth public abstract java.util.concurrent.Future<javax.ws.rs.core.Response> trace()

CLSS public abstract interface javax.ws.rs.client.Client
intf javax.ws.rs.core.Configurable<javax.ws.rs.client.Client>
meth public abstract javax.net.ssl.HostnameVerifier getHostnameVerifier()
meth public abstract javax.net.ssl.SSLContext getSslContext()
meth public abstract javax.ws.rs.client.Invocation$Builder invocation(javax.ws.rs.core.Link)
meth public abstract javax.ws.rs.client.WebTarget target(java.lang.String)
meth public abstract javax.ws.rs.client.WebTarget target(java.net.URI)
meth public abstract javax.ws.rs.client.WebTarget target(javax.ws.rs.core.Link)
meth public abstract javax.ws.rs.client.WebTarget target(javax.ws.rs.core.UriBuilder)
meth public abstract void close()

CLSS public abstract javax.ws.rs.client.ClientBuilder
cons protected init()
fld public final static java.lang.String JAXRS_DEFAULT_CLIENT_BUILDER_PROPERTY = "javax.ws.rs.client.ClientBuilder"
intf javax.ws.rs.core.Configurable<javax.ws.rs.client.ClientBuilder>
meth public abstract javax.ws.rs.client.Client build()
meth public abstract javax.ws.rs.client.ClientBuilder hostnameVerifier(javax.net.ssl.HostnameVerifier)
meth public abstract javax.ws.rs.client.ClientBuilder keyStore(java.security.KeyStore,char[])
meth public abstract javax.ws.rs.client.ClientBuilder sslContext(javax.net.ssl.SSLContext)
meth public abstract javax.ws.rs.client.ClientBuilder trustStore(java.security.KeyStore)
meth public abstract javax.ws.rs.client.ClientBuilder withConfig(javax.ws.rs.core.Configuration)
meth public javax.ws.rs.client.ClientBuilder keyStore(java.security.KeyStore,java.lang.String)
meth public static javax.ws.rs.client.Client newClient()
meth public static javax.ws.rs.client.Client newClient(javax.ws.rs.core.Configuration)
meth public static javax.ws.rs.client.ClientBuilder newBuilder()
supr java.lang.Object
hfds JAXRS_DEFAULT_CLIENT_BUILDER

CLSS public abstract interface javax.ws.rs.client.ClientRequestContext
meth public abstract boolean hasEntity()
meth public abstract java.io.OutputStream getEntityStream()
meth public abstract java.lang.Class<?> getEntityClass()
meth public abstract java.lang.Object getEntity()
meth public abstract java.lang.Object getProperty(java.lang.String)
meth public abstract java.lang.String getHeaderString(java.lang.String)
meth public abstract java.lang.String getMethod()
meth public abstract java.lang.annotation.Annotation[] getEntityAnnotations()
meth public abstract java.lang.reflect.Type getEntityType()
meth public abstract java.net.URI getUri()
meth public abstract java.util.Collection<java.lang.String> getPropertyNames()
meth public abstract java.util.Date getDate()
meth public abstract java.util.List<java.util.Locale> getAcceptableLanguages()
meth public abstract java.util.List<javax.ws.rs.core.MediaType> getAcceptableMediaTypes()
meth public abstract java.util.Locale getLanguage()
meth public abstract java.util.Map<java.lang.String,javax.ws.rs.core.Cookie> getCookies()
meth public abstract javax.ws.rs.client.Client getClient()
meth public abstract javax.ws.rs.core.Configuration getConfiguration()
meth public abstract javax.ws.rs.core.MediaType getMediaType()
meth public abstract javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.Object> getHeaders()
meth public abstract javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String> getStringHeaders()
meth public abstract void abortWith(javax.ws.rs.core.Response)
meth public abstract void removeProperty(java.lang.String)
meth public abstract void setEntity(java.lang.Object)
meth public abstract void setEntity(java.lang.Object,java.lang.annotation.Annotation[],javax.ws.rs.core.MediaType)
meth public abstract void setEntityStream(java.io.OutputStream)
meth public abstract void setMethod(java.lang.String)
meth public abstract void setProperty(java.lang.String,java.lang.Object)
meth public abstract void setUri(java.net.URI)

CLSS public abstract interface javax.ws.rs.client.ClientRequestFilter
meth public abstract void filter(javax.ws.rs.client.ClientRequestContext) throws java.io.IOException

CLSS public abstract interface javax.ws.rs.client.ClientResponseContext
meth public abstract boolean hasEntity()
meth public abstract boolean hasLink(java.lang.String)
meth public abstract int getLength()
meth public abstract int getStatus()
meth public abstract java.io.InputStream getEntityStream()
meth public abstract java.lang.String getHeaderString(java.lang.String)
meth public abstract java.net.URI getLocation()
meth public abstract java.util.Date getDate()
meth public abstract java.util.Date getLastModified()
meth public abstract java.util.Locale getLanguage()
meth public abstract java.util.Map<java.lang.String,javax.ws.rs.core.NewCookie> getCookies()
meth public abstract java.util.Set<java.lang.String> getAllowedMethods()
meth public abstract java.util.Set<javax.ws.rs.core.Link> getLinks()
meth public abstract javax.ws.rs.core.EntityTag getEntityTag()
meth public abstract javax.ws.rs.core.Link getLink(java.lang.String)
meth public abstract javax.ws.rs.core.Link$Builder getLinkBuilder(java.lang.String)
meth public abstract javax.ws.rs.core.MediaType getMediaType()
meth public abstract javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String> getHeaders()
meth public abstract javax.ws.rs.core.Response$StatusType getStatusInfo()
meth public abstract void setEntityStream(java.io.InputStream)
meth public abstract void setStatus(int)
meth public abstract void setStatusInfo(javax.ws.rs.core.Response$StatusType)

CLSS public abstract interface javax.ws.rs.client.ClientResponseFilter
meth public abstract void filter(javax.ws.rs.client.ClientRequestContext,javax.ws.rs.client.ClientResponseContext) throws java.io.IOException

CLSS public final javax.ws.rs.client.Entity<%0 extends java.lang.Object>
meth public java.lang.String getEncoding()
meth public java.lang.annotation.Annotation[] getAnnotations()
meth public java.util.Locale getLanguage()
meth public javax.ws.rs.core.MediaType getMediaType()
meth public javax.ws.rs.core.Variant getVariant()
meth public static <%0 extends java.lang.Object> javax.ws.rs.client.Entity<{%%0}> entity({%%0},java.lang.String)
meth public static <%0 extends java.lang.Object> javax.ws.rs.client.Entity<{%%0}> entity({%%0},javax.ws.rs.core.MediaType)
meth public static <%0 extends java.lang.Object> javax.ws.rs.client.Entity<{%%0}> entity({%%0},javax.ws.rs.core.MediaType,java.lang.annotation.Annotation[])
meth public static <%0 extends java.lang.Object> javax.ws.rs.client.Entity<{%%0}> entity({%%0},javax.ws.rs.core.Variant)
meth public static <%0 extends java.lang.Object> javax.ws.rs.client.Entity<{%%0}> entity({%%0},javax.ws.rs.core.Variant,java.lang.annotation.Annotation[])
meth public static <%0 extends java.lang.Object> javax.ws.rs.client.Entity<{%%0}> html({%%0})
meth public static <%0 extends java.lang.Object> javax.ws.rs.client.Entity<{%%0}> json({%%0})
meth public static <%0 extends java.lang.Object> javax.ws.rs.client.Entity<{%%0}> text({%%0})
meth public static <%0 extends java.lang.Object> javax.ws.rs.client.Entity<{%%0}> xhtml({%%0})
meth public static <%0 extends java.lang.Object> javax.ws.rs.client.Entity<{%%0}> xml({%%0})
meth public static javax.ws.rs.client.Entity<javax.ws.rs.core.Form> form(javax.ws.rs.core.Form)
meth public static javax.ws.rs.client.Entity<javax.ws.rs.core.Form> form(javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String>)
meth public {javax.ws.rs.client.Entity%0} getEntity()
supr java.lang.Object
hfds EMPTY_ANNOTATIONS,annotations,entity,variant

CLSS public abstract interface javax.ws.rs.client.Invocation
innr public abstract interface static Builder
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(javax.ws.rs.client.InvocationCallback<{%%0}>)
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} invoke(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} invoke(javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract java.util.concurrent.Future<javax.ws.rs.core.Response> submit()
meth public abstract javax.ws.rs.client.Invocation property(java.lang.String,java.lang.Object)
meth public abstract javax.ws.rs.core.Response invoke()

CLSS public abstract interface static javax.ws.rs.client.Invocation$Builder
 outer javax.ws.rs.client.Invocation
intf javax.ws.rs.client.SyncInvoker
meth public abstract !varargs javax.ws.rs.client.Invocation$Builder accept(java.lang.String[])
meth public abstract !varargs javax.ws.rs.client.Invocation$Builder accept(javax.ws.rs.core.MediaType[])
meth public abstract !varargs javax.ws.rs.client.Invocation$Builder acceptEncoding(java.lang.String[])
meth public abstract !varargs javax.ws.rs.client.Invocation$Builder acceptLanguage(java.lang.String[])
meth public abstract !varargs javax.ws.rs.client.Invocation$Builder acceptLanguage(java.util.Locale[])
meth public abstract javax.ws.rs.client.AsyncInvoker async()
meth public abstract javax.ws.rs.client.Invocation build(java.lang.String)
meth public abstract javax.ws.rs.client.Invocation build(java.lang.String,javax.ws.rs.client.Entity<?>)
meth public abstract javax.ws.rs.client.Invocation buildDelete()
meth public abstract javax.ws.rs.client.Invocation buildGet()
meth public abstract javax.ws.rs.client.Invocation buildPost(javax.ws.rs.client.Entity<?>)
meth public abstract javax.ws.rs.client.Invocation buildPut(javax.ws.rs.client.Entity<?>)
meth public abstract javax.ws.rs.client.Invocation$Builder cacheControl(javax.ws.rs.core.CacheControl)
meth public abstract javax.ws.rs.client.Invocation$Builder cookie(java.lang.String,java.lang.String)
meth public abstract javax.ws.rs.client.Invocation$Builder cookie(javax.ws.rs.core.Cookie)
meth public abstract javax.ws.rs.client.Invocation$Builder header(java.lang.String,java.lang.Object)
meth public abstract javax.ws.rs.client.Invocation$Builder headers(javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.Object>)
meth public abstract javax.ws.rs.client.Invocation$Builder property(java.lang.String,java.lang.Object)

CLSS public abstract interface javax.ws.rs.client.InvocationCallback<%0 extends java.lang.Object>
meth public abstract void completed({javax.ws.rs.client.InvocationCallback%0})
meth public abstract void failed(java.lang.Throwable)

CLSS public javax.ws.rs.client.ResponseProcessingException
cons public init(javax.ws.rs.core.Response,java.lang.String)
cons public init(javax.ws.rs.core.Response,java.lang.String,java.lang.Throwable)
cons public init(javax.ws.rs.core.Response,java.lang.Throwable)
meth public javax.ws.rs.core.Response getResponse()
supr javax.ws.rs.ProcessingException
hfds response,serialVersionUID

CLSS public abstract interface javax.ws.rs.client.SyncInvoker
meth public abstract <%0 extends java.lang.Object> {%%0} delete(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} delete(javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} get(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} get(javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} method(java.lang.String,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} method(java.lang.String,javax.ws.rs.client.Entity<?>,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} method(java.lang.String,javax.ws.rs.client.Entity<?>,javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} method(java.lang.String,javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} options(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} options(javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} post(javax.ws.rs.client.Entity<?>,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} post(javax.ws.rs.client.Entity<?>,javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} put(javax.ws.rs.client.Entity<?>,java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} put(javax.ws.rs.client.Entity<?>,javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} trace(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} trace(javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract javax.ws.rs.core.Response delete()
meth public abstract javax.ws.rs.core.Response get()
meth public abstract javax.ws.rs.core.Response head()
meth public abstract javax.ws.rs.core.Response method(java.lang.String)
meth public abstract javax.ws.rs.core.Response method(java.lang.String,javax.ws.rs.client.Entity<?>)
meth public abstract javax.ws.rs.core.Response options()
meth public abstract javax.ws.rs.core.Response post(javax.ws.rs.client.Entity<?>)
meth public abstract javax.ws.rs.core.Response put(javax.ws.rs.client.Entity<?>)
meth public abstract javax.ws.rs.core.Response trace()

CLSS public abstract interface javax.ws.rs.client.WebTarget
intf javax.ws.rs.core.Configurable<javax.ws.rs.client.WebTarget>
meth public abstract !varargs javax.ws.rs.client.Invocation$Builder request(java.lang.String[])
meth public abstract !varargs javax.ws.rs.client.Invocation$Builder request(javax.ws.rs.core.MediaType[])
meth public abstract !varargs javax.ws.rs.client.WebTarget matrixParam(java.lang.String,java.lang.Object[])
meth public abstract !varargs javax.ws.rs.client.WebTarget queryParam(java.lang.String,java.lang.Object[])
meth public abstract java.net.URI getUri()
meth public abstract javax.ws.rs.client.Invocation$Builder request()
meth public abstract javax.ws.rs.client.WebTarget path(java.lang.String)
meth public abstract javax.ws.rs.client.WebTarget resolveTemplate(java.lang.String,java.lang.Object)
meth public abstract javax.ws.rs.client.WebTarget resolveTemplate(java.lang.String,java.lang.Object,boolean)
meth public abstract javax.ws.rs.client.WebTarget resolveTemplateFromEncoded(java.lang.String,java.lang.Object)
meth public abstract javax.ws.rs.client.WebTarget resolveTemplates(java.util.Map<java.lang.String,java.lang.Object>)
meth public abstract javax.ws.rs.client.WebTarget resolveTemplates(java.util.Map<java.lang.String,java.lang.Object>,boolean)
meth public abstract javax.ws.rs.client.WebTarget resolveTemplatesFromEncoded(java.util.Map<java.lang.String,java.lang.Object>)
meth public abstract javax.ws.rs.core.UriBuilder getUriBuilder()

CLSS public abstract interface javax.ws.rs.container.AsyncResponse
fld public final static long NO_TIMEOUT = 0
meth public abstract !varargs java.util.Map<java.lang.Class<?>,java.util.Collection<java.lang.Class<?>>> register(java.lang.Class<?>,java.lang.Class<?>[])
meth public abstract !varargs java.util.Map<java.lang.Class<?>,java.util.Collection<java.lang.Class<?>>> register(java.lang.Object,java.lang.Object[])
meth public abstract boolean cancel()
meth public abstract boolean cancel(int)
meth public abstract boolean cancel(java.util.Date)
meth public abstract boolean isCancelled()
meth public abstract boolean isDone()
meth public abstract boolean isSuspended()
meth public abstract boolean resume(java.lang.Object)
meth public abstract boolean resume(java.lang.Throwable)
meth public abstract boolean setTimeout(long,java.util.concurrent.TimeUnit)
meth public abstract java.util.Collection<java.lang.Class<?>> register(java.lang.Class<?>)
meth public abstract java.util.Collection<java.lang.Class<?>> register(java.lang.Object)
meth public abstract void setTimeoutHandler(javax.ws.rs.container.TimeoutHandler)

CLSS public abstract interface javax.ws.rs.container.CompletionCallback
meth public abstract void onComplete(java.lang.Throwable)

CLSS public abstract interface javax.ws.rs.container.ConnectionCallback
meth public abstract void onDisconnect(javax.ws.rs.container.AsyncResponse)

CLSS public abstract interface javax.ws.rs.container.ContainerRequestContext
meth public abstract boolean hasEntity()
meth public abstract int getLength()
meth public abstract java.io.InputStream getEntityStream()
meth public abstract java.lang.Object getProperty(java.lang.String)
meth public abstract java.lang.String getHeaderString(java.lang.String)
meth public abstract java.lang.String getMethod()
meth public abstract java.util.Collection<java.lang.String> getPropertyNames()
meth public abstract java.util.Date getDate()
meth public abstract java.util.List<java.util.Locale> getAcceptableLanguages()
meth public abstract java.util.List<javax.ws.rs.core.MediaType> getAcceptableMediaTypes()
meth public abstract java.util.Locale getLanguage()
meth public abstract java.util.Map<java.lang.String,javax.ws.rs.core.Cookie> getCookies()
meth public abstract javax.ws.rs.core.MediaType getMediaType()
meth public abstract javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String> getHeaders()
meth public abstract javax.ws.rs.core.Request getRequest()
meth public abstract javax.ws.rs.core.SecurityContext getSecurityContext()
meth public abstract javax.ws.rs.core.UriInfo getUriInfo()
meth public abstract void abortWith(javax.ws.rs.core.Response)
meth public abstract void removeProperty(java.lang.String)
meth public abstract void setEntityStream(java.io.InputStream)
meth public abstract void setMethod(java.lang.String)
meth public abstract void setProperty(java.lang.String,java.lang.Object)
meth public abstract void setRequestUri(java.net.URI)
meth public abstract void setRequestUri(java.net.URI,java.net.URI)
meth public abstract void setSecurityContext(javax.ws.rs.core.SecurityContext)

CLSS public abstract interface javax.ws.rs.container.ContainerRequestFilter
meth public abstract void filter(javax.ws.rs.container.ContainerRequestContext) throws java.io.IOException

CLSS public abstract interface javax.ws.rs.container.ContainerResponseContext
meth public abstract boolean hasEntity()
meth public abstract boolean hasLink(java.lang.String)
meth public abstract int getLength()
meth public abstract int getStatus()
meth public abstract java.io.OutputStream getEntityStream()
meth public abstract java.lang.Class<?> getEntityClass()
meth public abstract java.lang.Object getEntity()
meth public abstract java.lang.String getHeaderString(java.lang.String)
meth public abstract java.lang.annotation.Annotation[] getEntityAnnotations()
meth public abstract java.lang.reflect.Type getEntityType()
meth public abstract java.net.URI getLocation()
meth public abstract java.util.Date getDate()
meth public abstract java.util.Date getLastModified()
meth public abstract java.util.Locale getLanguage()
meth public abstract java.util.Map<java.lang.String,javax.ws.rs.core.NewCookie> getCookies()
meth public abstract java.util.Set<java.lang.String> getAllowedMethods()
meth public abstract java.util.Set<javax.ws.rs.core.Link> getLinks()
meth public abstract javax.ws.rs.core.EntityTag getEntityTag()
meth public abstract javax.ws.rs.core.Link getLink(java.lang.String)
meth public abstract javax.ws.rs.core.Link$Builder getLinkBuilder(java.lang.String)
meth public abstract javax.ws.rs.core.MediaType getMediaType()
meth public abstract javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.Object> getHeaders()
meth public abstract javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String> getStringHeaders()
meth public abstract javax.ws.rs.core.Response$StatusType getStatusInfo()
meth public abstract void setEntity(java.lang.Object)
meth public abstract void setEntity(java.lang.Object,java.lang.annotation.Annotation[],javax.ws.rs.core.MediaType)
meth public abstract void setEntityStream(java.io.OutputStream)
meth public abstract void setStatus(int)
meth public abstract void setStatusInfo(javax.ws.rs.core.Response$StatusType)

CLSS public abstract interface javax.ws.rs.container.ContainerResponseFilter
meth public abstract void filter(javax.ws.rs.container.ContainerRequestContext,javax.ws.rs.container.ContainerResponseContext) throws java.io.IOException

CLSS public abstract interface javax.ws.rs.container.DynamicFeature
meth public abstract void configure(javax.ws.rs.container.ResourceInfo,javax.ws.rs.core.FeatureContext)

CLSS public abstract interface !annotation javax.ws.rs.container.PreMatching
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface javax.ws.rs.container.ResourceContext
meth public abstract <%0 extends java.lang.Object> {%%0} getResource(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} initResource({%%0})

CLSS public abstract interface javax.ws.rs.container.ResourceInfo
meth public abstract java.lang.Class<?> getResourceClass()
meth public abstract java.lang.reflect.Method getResourceMethod()

CLSS public abstract interface !annotation javax.ws.rs.container.Suspended
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
intf java.lang.annotation.Annotation

CLSS public abstract interface javax.ws.rs.container.TimeoutHandler
meth public abstract void handleTimeout(javax.ws.rs.container.AsyncResponse)

CLSS public abstract javax.ws.rs.core.AbstractMultivaluedMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(java.util.Map<{javax.ws.rs.core.AbstractMultivaluedMap%0},java.util.List<{javax.ws.rs.core.AbstractMultivaluedMap%1}>>)
fld protected final java.util.Map<{javax.ws.rs.core.AbstractMultivaluedMap%0},java.util.List<{javax.ws.rs.core.AbstractMultivaluedMap%1}>> store
intf javax.ws.rs.core.MultivaluedMap<{javax.ws.rs.core.AbstractMultivaluedMap%0},{javax.ws.rs.core.AbstractMultivaluedMap%1}>
meth protected final java.util.List<{javax.ws.rs.core.AbstractMultivaluedMap%1}> getValues({javax.ws.rs.core.AbstractMultivaluedMap%0})
meth protected void addFirstNull(java.util.List<{javax.ws.rs.core.AbstractMultivaluedMap%1}>)
meth protected void addNull(java.util.List<{javax.ws.rs.core.AbstractMultivaluedMap%1}>)
meth public !varargs final void addAll({javax.ws.rs.core.AbstractMultivaluedMap%0},{javax.ws.rs.core.AbstractMultivaluedMap%1}[])
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean equalsIgnoreValueOrder(javax.ws.rs.core.MultivaluedMap<{javax.ws.rs.core.AbstractMultivaluedMap%0},{javax.ws.rs.core.AbstractMultivaluedMap%1}>)
meth public boolean isEmpty()
meth public final void add({javax.ws.rs.core.AbstractMultivaluedMap%0},{javax.ws.rs.core.AbstractMultivaluedMap%1})
meth public final void addAll({javax.ws.rs.core.AbstractMultivaluedMap%0},java.util.List<{javax.ws.rs.core.AbstractMultivaluedMap%1}>)
meth public final void addFirst({javax.ws.rs.core.AbstractMultivaluedMap%0},{javax.ws.rs.core.AbstractMultivaluedMap%1})
meth public final void putSingle({javax.ws.rs.core.AbstractMultivaluedMap%0},{javax.ws.rs.core.AbstractMultivaluedMap%1})
meth public final {javax.ws.rs.core.AbstractMultivaluedMap%1} getFirst({javax.ws.rs.core.AbstractMultivaluedMap%0})
meth public int hashCode()
meth public int size()
meth public java.lang.String toString()
meth public java.util.Collection<java.util.List<{javax.ws.rs.core.AbstractMultivaluedMap%1}>> values()
meth public java.util.List<{javax.ws.rs.core.AbstractMultivaluedMap%1}> get(java.lang.Object)
meth public java.util.List<{javax.ws.rs.core.AbstractMultivaluedMap%1}> put({javax.ws.rs.core.AbstractMultivaluedMap%0},java.util.List<{javax.ws.rs.core.AbstractMultivaluedMap%1}>)
meth public java.util.List<{javax.ws.rs.core.AbstractMultivaluedMap%1}> remove(java.lang.Object)
meth public java.util.Set<java.util.Map$Entry<{javax.ws.rs.core.AbstractMultivaluedMap%0},java.util.List<{javax.ws.rs.core.AbstractMultivaluedMap%1}>>> entrySet()
meth public java.util.Set<{javax.ws.rs.core.AbstractMultivaluedMap%0}> keySet()
meth public void clear()
meth public void putAll(java.util.Map<? extends {javax.ws.rs.core.AbstractMultivaluedMap%0},? extends java.util.List<{javax.ws.rs.core.AbstractMultivaluedMap%1}>>)
supr java.lang.Object

CLSS public javax.ws.rs.core.Application
cons public init()
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public java.util.Set<java.lang.Class<?>> getClasses()
meth public java.util.Set<java.lang.Object> getSingletons()
supr java.lang.Object

CLSS public javax.ws.rs.core.CacheControl
cons public init()
meth public boolean equals(java.lang.Object)
meth public boolean isMustRevalidate()
meth public boolean isNoCache()
meth public boolean isNoStore()
meth public boolean isNoTransform()
meth public boolean isPrivate()
meth public boolean isProxyRevalidate()
meth public int getMaxAge()
meth public int getSMaxAge()
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getNoCacheFields()
meth public java.util.List<java.lang.String> getPrivateFields()
meth public java.util.Map<java.lang.String,java.lang.String> getCacheExtension()
meth public static javax.ws.rs.core.CacheControl valueOf(java.lang.String)
meth public void setMaxAge(int)
meth public void setMustRevalidate(boolean)
meth public void setNoCache(boolean)
meth public void setNoStore(boolean)
meth public void setNoTransform(boolean)
meth public void setPrivate(boolean)
meth public void setProxyRevalidate(boolean)
meth public void setSMaxAge(int)
supr java.lang.Object
hfds HEADER_DELEGATE,cacheExtension,maxAge,mustRevalidate,noCache,noCacheFields,noStore,noTransform,privateFields,privateFlag,proxyRevalidate,sMaxAge

CLSS public abstract interface javax.ws.rs.core.Configurable<%0 extends javax.ws.rs.core.Configurable>
meth public abstract !varargs {javax.ws.rs.core.Configurable%0} register(java.lang.Class<?>,java.lang.Class<?>[])
meth public abstract !varargs {javax.ws.rs.core.Configurable%0} register(java.lang.Object,java.lang.Class<?>[])
meth public abstract javax.ws.rs.core.Configuration getConfiguration()
meth public abstract {javax.ws.rs.core.Configurable%0} property(java.lang.String,java.lang.Object)
meth public abstract {javax.ws.rs.core.Configurable%0} register(java.lang.Class<?>)
meth public abstract {javax.ws.rs.core.Configurable%0} register(java.lang.Class<?>,int)
meth public abstract {javax.ws.rs.core.Configurable%0} register(java.lang.Class<?>,java.util.Map<java.lang.Class<?>,java.lang.Integer>)
meth public abstract {javax.ws.rs.core.Configurable%0} register(java.lang.Object)
meth public abstract {javax.ws.rs.core.Configurable%0} register(java.lang.Object,int)
meth public abstract {javax.ws.rs.core.Configurable%0} register(java.lang.Object,java.util.Map<java.lang.Class<?>,java.lang.Integer>)

CLSS public abstract interface javax.ws.rs.core.Configuration
meth public abstract boolean isEnabled(java.lang.Class<? extends javax.ws.rs.core.Feature>)
meth public abstract boolean isEnabled(javax.ws.rs.core.Feature)
meth public abstract boolean isRegistered(java.lang.Class<?>)
meth public abstract boolean isRegistered(java.lang.Object)
meth public abstract java.lang.Object getProperty(java.lang.String)
meth public abstract java.util.Collection<java.lang.String> getPropertyNames()
meth public abstract java.util.Map<java.lang.Class<?>,java.lang.Integer> getContracts(java.lang.Class<?>)
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public abstract java.util.Set<java.lang.Class<?>> getClasses()
meth public abstract java.util.Set<java.lang.Object> getInstances()
meth public abstract javax.ws.rs.RuntimeType getRuntimeType()

CLSS public abstract interface !annotation javax.ws.rs.core.Context
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER, METHOD, FIELD])
intf java.lang.annotation.Annotation

CLSS public javax.ws.rs.core.Cookie
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,int)
fld public final static int DEFAULT_VERSION = 1
meth public boolean equals(java.lang.Object)
meth public int getVersion()
meth public int hashCode()
meth public java.lang.String getDomain()
meth public java.lang.String getName()
meth public java.lang.String getPath()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public static javax.ws.rs.core.Cookie valueOf(java.lang.String)
supr java.lang.Object
hfds HEADER_DELEGATE,domain,name,path,value,version

CLSS public javax.ws.rs.core.EntityTag
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
meth public boolean equals(java.lang.Object)
meth public boolean isWeak()
meth public int hashCode()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public static javax.ws.rs.core.EntityTag valueOf(java.lang.String)
supr java.lang.Object
hfds HEADER_DELEGATE,value,weak

CLSS public abstract interface javax.ws.rs.core.Feature
meth public abstract boolean configure(javax.ws.rs.core.FeatureContext)

CLSS public abstract interface javax.ws.rs.core.FeatureContext
intf javax.ws.rs.core.Configurable<javax.ws.rs.core.FeatureContext>

CLSS public javax.ws.rs.core.Form
cons public init()
cons public init(java.lang.String,java.lang.String)
cons public init(javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String>)
meth public javax.ws.rs.core.Form param(java.lang.String,java.lang.String)
meth public javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String> asMap()
supr java.lang.Object
hfds parameters

CLSS public javax.ws.rs.core.GenericEntity<%0 extends java.lang.Object>
cons protected init({javax.ws.rs.core.GenericEntity%0})
cons public init({javax.ws.rs.core.GenericEntity%0},java.lang.reflect.Type)
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getRawType()
meth public final java.lang.reflect.Type getType()
meth public final {javax.ws.rs.core.GenericEntity%0} getEntity()
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds entity,rawType,type

CLSS public javax.ws.rs.core.GenericType<%0 extends java.lang.Object>
cons protected init()
cons public init(java.lang.reflect.Type)
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getRawType()
meth public final java.lang.reflect.Type getType()
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds rawType,type

CLSS public abstract interface javax.ws.rs.core.HttpHeaders
fld public final static java.lang.String ACCEPT = "Accept"
fld public final static java.lang.String ACCEPT_CHARSET = "Accept-Charset"
fld public final static java.lang.String ACCEPT_ENCODING = "Accept-Encoding"
fld public final static java.lang.String ACCEPT_LANGUAGE = "Accept-Language"
fld public final static java.lang.String ALLOW = "Allow"
fld public final static java.lang.String AUTHORIZATION = "Authorization"
fld public final static java.lang.String CACHE_CONTROL = "Cache-Control"
fld public final static java.lang.String CONTENT_DISPOSITION = "Content-Disposition"
fld public final static java.lang.String CONTENT_ENCODING = "Content-Encoding"
fld public final static java.lang.String CONTENT_ID = "Content-ID"
fld public final static java.lang.String CONTENT_LANGUAGE = "Content-Language"
fld public final static java.lang.String CONTENT_LENGTH = "Content-Length"
fld public final static java.lang.String CONTENT_LOCATION = "Content-Location"
fld public final static java.lang.String CONTENT_TYPE = "Content-Type"
fld public final static java.lang.String COOKIE = "Cookie"
fld public final static java.lang.String DATE = "Date"
fld public final static java.lang.String ETAG = "ETag"
fld public final static java.lang.String EXPIRES = "Expires"
fld public final static java.lang.String HOST = "Host"
fld public final static java.lang.String IF_MATCH = "If-Match"
fld public final static java.lang.String IF_MODIFIED_SINCE = "If-Modified-Since"
fld public final static java.lang.String IF_NONE_MATCH = "If-None-Match"
fld public final static java.lang.String IF_UNMODIFIED_SINCE = "If-Unmodified-Since"
fld public final static java.lang.String LAST_MODIFIED = "Last-Modified"
fld public final static java.lang.String LINK = "Link"
fld public final static java.lang.String LOCATION = "Location"
fld public final static java.lang.String RETRY_AFTER = "Retry-After"
fld public final static java.lang.String SET_COOKIE = "Set-Cookie"
fld public final static java.lang.String USER_AGENT = "User-Agent"
fld public final static java.lang.String VARY = "Vary"
fld public final static java.lang.String WWW_AUTHENTICATE = "WWW-Authenticate"
meth public abstract int getLength()
meth public abstract java.lang.String getHeaderString(java.lang.String)
meth public abstract java.util.Date getDate()
meth public abstract java.util.List<java.lang.String> getRequestHeader(java.lang.String)
meth public abstract java.util.List<java.util.Locale> getAcceptableLanguages()
meth public abstract java.util.List<javax.ws.rs.core.MediaType> getAcceptableMediaTypes()
meth public abstract java.util.Locale getLanguage()
meth public abstract java.util.Map<java.lang.String,javax.ws.rs.core.Cookie> getCookies()
meth public abstract javax.ws.rs.core.MediaType getMediaType()
meth public abstract javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String> getRequestHeaders()

CLSS public abstract javax.ws.rs.core.Link
cons public init()
fld public final static java.lang.String REL = "rel"
fld public final static java.lang.String TITLE = "title"
fld public final static java.lang.String TYPE = "type"
innr public abstract interface static Builder
innr public static JaxbAdapter
innr public static JaxbLink
meth public abstract java.lang.String getRel()
meth public abstract java.lang.String getTitle()
meth public abstract java.lang.String getType()
meth public abstract java.lang.String toString()
meth public abstract java.net.URI getUri()
meth public abstract java.util.List<java.lang.String> getRels()
meth public abstract java.util.Map<java.lang.String,java.lang.String> getParams()
meth public abstract javax.ws.rs.core.UriBuilder getUriBuilder()
meth public static javax.ws.rs.core.Link valueOf(java.lang.String)
meth public static javax.ws.rs.core.Link$Builder fromLink(javax.ws.rs.core.Link)
meth public static javax.ws.rs.core.Link$Builder fromMethod(java.lang.Class<?>,java.lang.String)
meth public static javax.ws.rs.core.Link$Builder fromPath(java.lang.String)
meth public static javax.ws.rs.core.Link$Builder fromResource(java.lang.Class<?>)
meth public static javax.ws.rs.core.Link$Builder fromUri(java.lang.String)
meth public static javax.ws.rs.core.Link$Builder fromUri(java.net.URI)
meth public static javax.ws.rs.core.Link$Builder fromUriBuilder(javax.ws.rs.core.UriBuilder)
supr java.lang.Object

CLSS public abstract interface static javax.ws.rs.core.Link$Builder
 outer javax.ws.rs.core.Link
meth public abstract !varargs javax.ws.rs.core.Link build(java.lang.Object[])
meth public abstract !varargs javax.ws.rs.core.Link buildRelativized(java.net.URI,java.lang.Object[])
meth public abstract javax.ws.rs.core.Link$Builder baseUri(java.lang.String)
meth public abstract javax.ws.rs.core.Link$Builder baseUri(java.net.URI)
meth public abstract javax.ws.rs.core.Link$Builder link(java.lang.String)
meth public abstract javax.ws.rs.core.Link$Builder link(javax.ws.rs.core.Link)
meth public abstract javax.ws.rs.core.Link$Builder param(java.lang.String,java.lang.String)
meth public abstract javax.ws.rs.core.Link$Builder rel(java.lang.String)
meth public abstract javax.ws.rs.core.Link$Builder title(java.lang.String)
meth public abstract javax.ws.rs.core.Link$Builder type(java.lang.String)
meth public abstract javax.ws.rs.core.Link$Builder uri(java.lang.String)
meth public abstract javax.ws.rs.core.Link$Builder uri(java.net.URI)
meth public abstract javax.ws.rs.core.Link$Builder uriBuilder(javax.ws.rs.core.UriBuilder)

CLSS public static javax.ws.rs.core.Link$JaxbAdapter
 outer javax.ws.rs.core.Link
cons public init()
meth public javax.ws.rs.core.Link unmarshal(javax.ws.rs.core.Link$JaxbLink)
meth public javax.ws.rs.core.Link$JaxbLink marshal(javax.ws.rs.core.Link)
supr javax.xml.bind.annotation.adapters.XmlAdapter<javax.ws.rs.core.Link$JaxbLink,javax.ws.rs.core.Link>

CLSS public static javax.ws.rs.core.Link$JaxbLink
 outer javax.ws.rs.core.Link
cons public init()
cons public init(java.net.URI)
cons public init(java.net.URI,java.util.Map<javax.xml.namespace.QName,java.lang.Object>)
meth public java.net.URI getUri()
meth public java.util.Map<javax.xml.namespace.QName,java.lang.Object> getParams()
supr java.lang.Object
hfds params,uri

CLSS public javax.ws.rs.core.MediaType
cons public init()
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.String>)
fld public final static java.lang.String APPLICATION_ATOM_XML = "application/atom+xml"
fld public final static java.lang.String APPLICATION_FORM_URLENCODED = "application/x-www-form-urlencoded"
fld public final static java.lang.String APPLICATION_JSON = "application/json"
fld public final static java.lang.String APPLICATION_OCTET_STREAM = "application/octet-stream"
fld public final static java.lang.String APPLICATION_SVG_XML = "application/svg+xml"
fld public final static java.lang.String APPLICATION_XHTML_XML = "application/xhtml+xml"
fld public final static java.lang.String APPLICATION_XML = "application/xml"
fld public final static java.lang.String CHARSET_PARAMETER = "charset"
fld public final static java.lang.String MEDIA_TYPE_WILDCARD = "*"
fld public final static java.lang.String MULTIPART_FORM_DATA = "multipart/form-data"
fld public final static java.lang.String TEXT_HTML = "text/html"
fld public final static java.lang.String TEXT_PLAIN = "text/plain"
fld public final static java.lang.String TEXT_XML = "text/xml"
fld public final static java.lang.String WILDCARD = "*/*"
fld public final static javax.ws.rs.core.MediaType APPLICATION_ATOM_XML_TYPE
fld public final static javax.ws.rs.core.MediaType APPLICATION_FORM_URLENCODED_TYPE
fld public final static javax.ws.rs.core.MediaType APPLICATION_JSON_TYPE
fld public final static javax.ws.rs.core.MediaType APPLICATION_OCTET_STREAM_TYPE
fld public final static javax.ws.rs.core.MediaType APPLICATION_SVG_XML_TYPE
fld public final static javax.ws.rs.core.MediaType APPLICATION_XHTML_XML_TYPE
fld public final static javax.ws.rs.core.MediaType APPLICATION_XML_TYPE
fld public final static javax.ws.rs.core.MediaType MULTIPART_FORM_DATA_TYPE
fld public final static javax.ws.rs.core.MediaType TEXT_HTML_TYPE
fld public final static javax.ws.rs.core.MediaType TEXT_PLAIN_TYPE
fld public final static javax.ws.rs.core.MediaType TEXT_XML_TYPE
fld public final static javax.ws.rs.core.MediaType WILDCARD_TYPE
meth public boolean equals(java.lang.Object)
meth public boolean isCompatible(javax.ws.rs.core.MediaType)
meth public boolean isWildcardSubtype()
meth public boolean isWildcardType()
meth public int hashCode()
meth public java.lang.String getSubtype()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public java.util.Map<java.lang.String,java.lang.String> getParameters()
meth public javax.ws.rs.core.MediaType withCharset(java.lang.String)
meth public static javax.ws.rs.core.MediaType valueOf(java.lang.String)
supr java.lang.Object
hfds parameters,subtype,type

CLSS public javax.ws.rs.core.MultivaluedHashMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(int,float)
cons public init(java.util.Map<? extends {javax.ws.rs.core.MultivaluedHashMap%0},? extends {javax.ws.rs.core.MultivaluedHashMap%1}>)
cons public init(javax.ws.rs.core.MultivaluedMap<? extends {javax.ws.rs.core.MultivaluedHashMap%0},? extends {javax.ws.rs.core.MultivaluedHashMap%1}>)
intf java.io.Serializable
supr javax.ws.rs.core.AbstractMultivaluedMap<{javax.ws.rs.core.MultivaluedHashMap%0},{javax.ws.rs.core.MultivaluedHashMap%1}>
hfds serialVersionUID

CLSS public abstract interface javax.ws.rs.core.MultivaluedMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf java.util.Map<{javax.ws.rs.core.MultivaluedMap%0},java.util.List<{javax.ws.rs.core.MultivaluedMap%1}>>
meth public abstract !varargs void addAll({javax.ws.rs.core.MultivaluedMap%0},{javax.ws.rs.core.MultivaluedMap%1}[])
meth public abstract boolean equalsIgnoreValueOrder(javax.ws.rs.core.MultivaluedMap<{javax.ws.rs.core.MultivaluedMap%0},{javax.ws.rs.core.MultivaluedMap%1}>)
meth public abstract void add({javax.ws.rs.core.MultivaluedMap%0},{javax.ws.rs.core.MultivaluedMap%1})
meth public abstract void addAll({javax.ws.rs.core.MultivaluedMap%0},java.util.List<{javax.ws.rs.core.MultivaluedMap%1}>)
meth public abstract void addFirst({javax.ws.rs.core.MultivaluedMap%0},{javax.ws.rs.core.MultivaluedMap%1})
meth public abstract void putSingle({javax.ws.rs.core.MultivaluedMap%0},{javax.ws.rs.core.MultivaluedMap%1})
meth public abstract {javax.ws.rs.core.MultivaluedMap%1} getFirst({javax.ws.rs.core.MultivaluedMap%0})

CLSS public javax.ws.rs.core.NewCookie
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,int,java.lang.String,int,boolean)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,int,java.lang.String,int,java.util.Date,boolean,boolean)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,int,boolean)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,int,boolean,boolean)
cons public init(javax.ws.rs.core.Cookie)
cons public init(javax.ws.rs.core.Cookie,java.lang.String,int,boolean)
cons public init(javax.ws.rs.core.Cookie,java.lang.String,int,java.util.Date,boolean,boolean)
fld public final static int DEFAULT_MAX_AGE = -1
meth public boolean equals(java.lang.Object)
meth public boolean isHttpOnly()
meth public boolean isSecure()
meth public int getMaxAge()
meth public int hashCode()
meth public java.lang.String getComment()
meth public java.lang.String toString()
meth public java.util.Date getExpiry()
meth public javax.ws.rs.core.Cookie toCookie()
meth public static javax.ws.rs.core.NewCookie valueOf(java.lang.String)
supr javax.ws.rs.core.Cookie
hfds comment,delegate,expiry,httpOnly,maxAge,secure

CLSS public javax.ws.rs.core.NoContentException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.io.IOException
hfds serialVersionUID

CLSS public abstract interface javax.ws.rs.core.PathSegment
meth public abstract java.lang.String getPath()
meth public abstract javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String> getMatrixParameters()

CLSS public abstract interface javax.ws.rs.core.Request
meth public abstract java.lang.String getMethod()
meth public abstract javax.ws.rs.core.Response$ResponseBuilder evaluatePreconditions()
meth public abstract javax.ws.rs.core.Response$ResponseBuilder evaluatePreconditions(java.util.Date)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder evaluatePreconditions(java.util.Date,javax.ws.rs.core.EntityTag)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder evaluatePreconditions(javax.ws.rs.core.EntityTag)
meth public abstract javax.ws.rs.core.Variant selectVariant(java.util.List<javax.ws.rs.core.Variant>)

CLSS public abstract javax.ws.rs.core.Response
cons protected init()
innr public abstract interface static StatusType
innr public abstract static ResponseBuilder
innr public final static !enum Status
meth public abstract <%0 extends java.lang.Object> {%%0} readEntity(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} readEntity(java.lang.Class<{%%0}>,java.lang.annotation.Annotation[])
meth public abstract <%0 extends java.lang.Object> {%%0} readEntity(javax.ws.rs.core.GenericType<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} readEntity(javax.ws.rs.core.GenericType<{%%0}>,java.lang.annotation.Annotation[])
meth public abstract boolean bufferEntity()
meth public abstract boolean hasEntity()
meth public abstract boolean hasLink(java.lang.String)
meth public abstract int getLength()
meth public abstract int getStatus()
meth public abstract java.lang.Object getEntity()
meth public abstract java.lang.String getHeaderString(java.lang.String)
meth public abstract java.net.URI getLocation()
meth public abstract java.util.Date getDate()
meth public abstract java.util.Date getLastModified()
meth public abstract java.util.Locale getLanguage()
meth public abstract java.util.Map<java.lang.String,javax.ws.rs.core.NewCookie> getCookies()
meth public abstract java.util.Set<java.lang.String> getAllowedMethods()
meth public abstract java.util.Set<javax.ws.rs.core.Link> getLinks()
meth public abstract javax.ws.rs.core.EntityTag getEntityTag()
meth public abstract javax.ws.rs.core.Link getLink(java.lang.String)
meth public abstract javax.ws.rs.core.Link$Builder getLinkBuilder(java.lang.String)
meth public abstract javax.ws.rs.core.MediaType getMediaType()
meth public abstract javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.Object> getMetadata()
meth public abstract javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String> getStringHeaders()
meth public abstract javax.ws.rs.core.Response$StatusType getStatusInfo()
meth public abstract void close()
meth public javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.Object> getHeaders()
meth public static javax.ws.rs.core.Response$ResponseBuilder accepted()
meth public static javax.ws.rs.core.Response$ResponseBuilder accepted(java.lang.Object)
meth public static javax.ws.rs.core.Response$ResponseBuilder created(java.net.URI)
meth public static javax.ws.rs.core.Response$ResponseBuilder fromResponse(javax.ws.rs.core.Response)
meth public static javax.ws.rs.core.Response$ResponseBuilder noContent()
meth public static javax.ws.rs.core.Response$ResponseBuilder notAcceptable(java.util.List<javax.ws.rs.core.Variant>)
meth public static javax.ws.rs.core.Response$ResponseBuilder notModified()
meth public static javax.ws.rs.core.Response$ResponseBuilder notModified(java.lang.String)
meth public static javax.ws.rs.core.Response$ResponseBuilder notModified(javax.ws.rs.core.EntityTag)
meth public static javax.ws.rs.core.Response$ResponseBuilder ok()
meth public static javax.ws.rs.core.Response$ResponseBuilder ok(java.lang.Object)
meth public static javax.ws.rs.core.Response$ResponseBuilder ok(java.lang.Object,java.lang.String)
meth public static javax.ws.rs.core.Response$ResponseBuilder ok(java.lang.Object,javax.ws.rs.core.MediaType)
meth public static javax.ws.rs.core.Response$ResponseBuilder ok(java.lang.Object,javax.ws.rs.core.Variant)
meth public static javax.ws.rs.core.Response$ResponseBuilder seeOther(java.net.URI)
meth public static javax.ws.rs.core.Response$ResponseBuilder serverError()
meth public static javax.ws.rs.core.Response$ResponseBuilder status(int)
meth public static javax.ws.rs.core.Response$ResponseBuilder status(javax.ws.rs.core.Response$Status)
meth public static javax.ws.rs.core.Response$ResponseBuilder status(javax.ws.rs.core.Response$StatusType)
meth public static javax.ws.rs.core.Response$ResponseBuilder temporaryRedirect(java.net.URI)
supr java.lang.Object

CLSS public abstract static javax.ws.rs.core.Response$ResponseBuilder
 outer javax.ws.rs.core.Response
cons protected init()
meth protected static javax.ws.rs.core.Response$ResponseBuilder newInstance()
meth public abstract !varargs javax.ws.rs.core.Response$ResponseBuilder allow(java.lang.String[])
meth public abstract !varargs javax.ws.rs.core.Response$ResponseBuilder cookie(javax.ws.rs.core.NewCookie[])
meth public abstract !varargs javax.ws.rs.core.Response$ResponseBuilder links(javax.ws.rs.core.Link[])
meth public abstract !varargs javax.ws.rs.core.Response$ResponseBuilder variants(javax.ws.rs.core.Variant[])
meth public abstract javax.ws.rs.core.Response build()
meth public abstract javax.ws.rs.core.Response$ResponseBuilder allow(java.util.Set<java.lang.String>)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder cacheControl(javax.ws.rs.core.CacheControl)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder clone()
meth public abstract javax.ws.rs.core.Response$ResponseBuilder contentLocation(java.net.URI)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder encoding(java.lang.String)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder entity(java.lang.Object)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder entity(java.lang.Object,java.lang.annotation.Annotation[])
meth public abstract javax.ws.rs.core.Response$ResponseBuilder expires(java.util.Date)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder header(java.lang.String,java.lang.Object)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder language(java.lang.String)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder language(java.util.Locale)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder lastModified(java.util.Date)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder link(java.lang.String,java.lang.String)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder link(java.net.URI,java.lang.String)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder location(java.net.URI)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder replaceAll(javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.Object>)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder status(int)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder tag(java.lang.String)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder tag(javax.ws.rs.core.EntityTag)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder type(java.lang.String)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder type(javax.ws.rs.core.MediaType)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder variant(javax.ws.rs.core.Variant)
meth public abstract javax.ws.rs.core.Response$ResponseBuilder variants(java.util.List<javax.ws.rs.core.Variant>)
meth public javax.ws.rs.core.Response$ResponseBuilder status(javax.ws.rs.core.Response$Status)
meth public javax.ws.rs.core.Response$ResponseBuilder status(javax.ws.rs.core.Response$StatusType)
supr java.lang.Object

CLSS public final static !enum javax.ws.rs.core.Response$Status
 outer javax.ws.rs.core.Response
fld public final static javax.ws.rs.core.Response$Status ACCEPTED
fld public final static javax.ws.rs.core.Response$Status BAD_GATEWAY
fld public final static javax.ws.rs.core.Response$Status BAD_REQUEST
fld public final static javax.ws.rs.core.Response$Status CONFLICT
fld public final static javax.ws.rs.core.Response$Status CREATED
fld public final static javax.ws.rs.core.Response$Status EXPECTATION_FAILED
fld public final static javax.ws.rs.core.Response$Status FORBIDDEN
fld public final static javax.ws.rs.core.Response$Status FOUND
fld public final static javax.ws.rs.core.Response$Status GATEWAY_TIMEOUT
fld public final static javax.ws.rs.core.Response$Status GONE
fld public final static javax.ws.rs.core.Response$Status HTTP_VERSION_NOT_SUPPORTED
fld public final static javax.ws.rs.core.Response$Status INTERNAL_SERVER_ERROR
fld public final static javax.ws.rs.core.Response$Status LENGTH_REQUIRED
fld public final static javax.ws.rs.core.Response$Status METHOD_NOT_ALLOWED
fld public final static javax.ws.rs.core.Response$Status MOVED_PERMANENTLY
fld public final static javax.ws.rs.core.Response$Status NOT_ACCEPTABLE
fld public final static javax.ws.rs.core.Response$Status NOT_FOUND
fld public final static javax.ws.rs.core.Response$Status NOT_IMPLEMENTED
fld public final static javax.ws.rs.core.Response$Status NOT_MODIFIED
fld public final static javax.ws.rs.core.Response$Status NO_CONTENT
fld public final static javax.ws.rs.core.Response$Status OK
fld public final static javax.ws.rs.core.Response$Status PARTIAL_CONTENT
fld public final static javax.ws.rs.core.Response$Status PAYMENT_REQUIRED
fld public final static javax.ws.rs.core.Response$Status PRECONDITION_FAILED
fld public final static javax.ws.rs.core.Response$Status PROXY_AUTHENTICATION_REQUIRED
fld public final static javax.ws.rs.core.Response$Status REQUESTED_RANGE_NOT_SATISFIABLE
fld public final static javax.ws.rs.core.Response$Status REQUEST_ENTITY_TOO_LARGE
fld public final static javax.ws.rs.core.Response$Status REQUEST_TIMEOUT
fld public final static javax.ws.rs.core.Response$Status REQUEST_URI_TOO_LONG
fld public final static javax.ws.rs.core.Response$Status RESET_CONTENT
fld public final static javax.ws.rs.core.Response$Status SEE_OTHER
fld public final static javax.ws.rs.core.Response$Status SERVICE_UNAVAILABLE
fld public final static javax.ws.rs.core.Response$Status TEMPORARY_REDIRECT
fld public final static javax.ws.rs.core.Response$Status UNAUTHORIZED
fld public final static javax.ws.rs.core.Response$Status UNSUPPORTED_MEDIA_TYPE
fld public final static javax.ws.rs.core.Response$Status USE_PROXY
innr public final static !enum Family
intf javax.ws.rs.core.Response$StatusType
meth public int getStatusCode()
meth public java.lang.String getReasonPhrase()
meth public java.lang.String toString()
meth public javax.ws.rs.core.Response$Status$Family getFamily()
meth public static javax.ws.rs.core.Response$Status fromStatusCode(int)
meth public static javax.ws.rs.core.Response$Status valueOf(java.lang.String)
meth public static javax.ws.rs.core.Response$Status[] values()
supr java.lang.Enum<javax.ws.rs.core.Response$Status>
hfds code,family,reason

CLSS public final static !enum javax.ws.rs.core.Response$Status$Family
 outer javax.ws.rs.core.Response$Status
fld public final static javax.ws.rs.core.Response$Status$Family CLIENT_ERROR
fld public final static javax.ws.rs.core.Response$Status$Family INFORMATIONAL
fld public final static javax.ws.rs.core.Response$Status$Family OTHER
fld public final static javax.ws.rs.core.Response$Status$Family REDIRECTION
fld public final static javax.ws.rs.core.Response$Status$Family SERVER_ERROR
fld public final static javax.ws.rs.core.Response$Status$Family SUCCESSFUL
meth public static javax.ws.rs.core.Response$Status$Family familyOf(int)
meth public static javax.ws.rs.core.Response$Status$Family valueOf(java.lang.String)
meth public static javax.ws.rs.core.Response$Status$Family[] values()
supr java.lang.Enum<javax.ws.rs.core.Response$Status$Family>

CLSS public abstract interface static javax.ws.rs.core.Response$StatusType
 outer javax.ws.rs.core.Response
meth public abstract int getStatusCode()
meth public abstract java.lang.String getReasonPhrase()
meth public abstract javax.ws.rs.core.Response$Status$Family getFamily()

CLSS public abstract interface javax.ws.rs.core.SecurityContext
fld public final static java.lang.String BASIC_AUTH = "BASIC"
fld public final static java.lang.String CLIENT_CERT_AUTH = "CLIENT_CERT"
fld public final static java.lang.String DIGEST_AUTH = "DIGEST"
fld public final static java.lang.String FORM_AUTH = "FORM"
meth public abstract boolean isSecure()
meth public abstract boolean isUserInRole(java.lang.String)
meth public abstract java.lang.String getAuthenticationScheme()
meth public abstract java.security.Principal getUserPrincipal()

CLSS public abstract interface javax.ws.rs.core.StreamingOutput
meth public abstract void write(java.io.OutputStream) throws java.io.IOException

CLSS public abstract javax.ws.rs.core.UriBuilder
cons protected init()
meth protected static javax.ws.rs.core.UriBuilder newInstance()
meth public abstract !varargs java.net.URI build(java.lang.Object[])
meth public abstract !varargs java.net.URI buildFromEncoded(java.lang.Object[])
meth public abstract !varargs javax.ws.rs.core.UriBuilder matrixParam(java.lang.String,java.lang.Object[])
meth public abstract !varargs javax.ws.rs.core.UriBuilder queryParam(java.lang.String,java.lang.Object[])
meth public abstract !varargs javax.ws.rs.core.UriBuilder replaceMatrixParam(java.lang.String,java.lang.Object[])
meth public abstract !varargs javax.ws.rs.core.UriBuilder replaceQueryParam(java.lang.String,java.lang.Object[])
meth public abstract !varargs javax.ws.rs.core.UriBuilder segment(java.lang.String[])
meth public abstract java.lang.String toTemplate()
meth public abstract java.net.URI build(java.lang.Object[],boolean)
meth public abstract java.net.URI buildFromEncodedMap(java.util.Map<java.lang.String,?>)
meth public abstract java.net.URI buildFromMap(java.util.Map<java.lang.String,?>)
meth public abstract java.net.URI buildFromMap(java.util.Map<java.lang.String,?>,boolean)
meth public abstract javax.ws.rs.core.UriBuilder clone()
meth public abstract javax.ws.rs.core.UriBuilder fragment(java.lang.String)
meth public abstract javax.ws.rs.core.UriBuilder host(java.lang.String)
meth public abstract javax.ws.rs.core.UriBuilder path(java.lang.Class)
meth public abstract javax.ws.rs.core.UriBuilder path(java.lang.Class,java.lang.String)
meth public abstract javax.ws.rs.core.UriBuilder path(java.lang.String)
meth public abstract javax.ws.rs.core.UriBuilder path(java.lang.reflect.Method)
meth public abstract javax.ws.rs.core.UriBuilder port(int)
meth public abstract javax.ws.rs.core.UriBuilder replaceMatrix(java.lang.String)
meth public abstract javax.ws.rs.core.UriBuilder replacePath(java.lang.String)
meth public abstract javax.ws.rs.core.UriBuilder replaceQuery(java.lang.String)
meth public abstract javax.ws.rs.core.UriBuilder resolveTemplate(java.lang.String,java.lang.Object)
meth public abstract javax.ws.rs.core.UriBuilder resolveTemplate(java.lang.String,java.lang.Object,boolean)
meth public abstract javax.ws.rs.core.UriBuilder resolveTemplateFromEncoded(java.lang.String,java.lang.Object)
meth public abstract javax.ws.rs.core.UriBuilder resolveTemplates(java.util.Map<java.lang.String,java.lang.Object>)
meth public abstract javax.ws.rs.core.UriBuilder resolveTemplates(java.util.Map<java.lang.String,java.lang.Object>,boolean)
meth public abstract javax.ws.rs.core.UriBuilder resolveTemplatesFromEncoded(java.util.Map<java.lang.String,java.lang.Object>)
meth public abstract javax.ws.rs.core.UriBuilder scheme(java.lang.String)
meth public abstract javax.ws.rs.core.UriBuilder schemeSpecificPart(java.lang.String)
meth public abstract javax.ws.rs.core.UriBuilder uri(java.lang.String)
meth public abstract javax.ws.rs.core.UriBuilder uri(java.net.URI)
meth public abstract javax.ws.rs.core.UriBuilder userInfo(java.lang.String)
meth public static javax.ws.rs.core.UriBuilder fromLink(javax.ws.rs.core.Link)
meth public static javax.ws.rs.core.UriBuilder fromMethod(java.lang.Class<?>,java.lang.String)
meth public static javax.ws.rs.core.UriBuilder fromPath(java.lang.String)
meth public static javax.ws.rs.core.UriBuilder fromResource(java.lang.Class<?>)
meth public static javax.ws.rs.core.UriBuilder fromUri(java.lang.String)
meth public static javax.ws.rs.core.UriBuilder fromUri(java.net.URI)
supr java.lang.Object

CLSS public javax.ws.rs.core.UriBuilderException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public abstract interface javax.ws.rs.core.UriInfo
meth public abstract java.lang.String getPath()
meth public abstract java.lang.String getPath(boolean)
meth public abstract java.net.URI getAbsolutePath()
meth public abstract java.net.URI getBaseUri()
meth public abstract java.net.URI getRequestUri()
meth public abstract java.net.URI relativize(java.net.URI)
meth public abstract java.net.URI resolve(java.net.URI)
meth public abstract java.util.List<java.lang.Object> getMatchedResources()
meth public abstract java.util.List<java.lang.String> getMatchedURIs()
meth public abstract java.util.List<java.lang.String> getMatchedURIs(boolean)
meth public abstract java.util.List<javax.ws.rs.core.PathSegment> getPathSegments()
meth public abstract java.util.List<javax.ws.rs.core.PathSegment> getPathSegments(boolean)
meth public abstract javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String> getPathParameters()
meth public abstract javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String> getPathParameters(boolean)
meth public abstract javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String> getQueryParameters()
meth public abstract javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String> getQueryParameters(boolean)
meth public abstract javax.ws.rs.core.UriBuilder getAbsolutePathBuilder()
meth public abstract javax.ws.rs.core.UriBuilder getBaseUriBuilder()
meth public abstract javax.ws.rs.core.UriBuilder getRequestUriBuilder()

CLSS public javax.ws.rs.core.Variant
cons public init(javax.ws.rs.core.MediaType,java.lang.String,java.lang.String)
cons public init(javax.ws.rs.core.MediaType,java.lang.String,java.lang.String,java.lang.String)
cons public init(javax.ws.rs.core.MediaType,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
cons public init(javax.ws.rs.core.MediaType,java.util.Locale,java.lang.String)
innr public abstract static VariantListBuilder
meth public !varargs static javax.ws.rs.core.Variant$VariantListBuilder encodings(java.lang.String[])
meth public !varargs static javax.ws.rs.core.Variant$VariantListBuilder languages(java.util.Locale[])
meth public !varargs static javax.ws.rs.core.Variant$VariantListBuilder mediaTypes(javax.ws.rs.core.MediaType[])
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getEncoding()
meth public java.lang.String getLanguageString()
meth public java.lang.String toString()
meth public java.util.Locale getLanguage()
meth public javax.ws.rs.core.MediaType getMediaType()
supr java.lang.Object
hfds encoding,language,mediaType

CLSS public abstract static javax.ws.rs.core.Variant$VariantListBuilder
 outer javax.ws.rs.core.Variant
cons protected init()
meth public abstract !varargs javax.ws.rs.core.Variant$VariantListBuilder encodings(java.lang.String[])
meth public abstract !varargs javax.ws.rs.core.Variant$VariantListBuilder languages(java.util.Locale[])
meth public abstract !varargs javax.ws.rs.core.Variant$VariantListBuilder mediaTypes(javax.ws.rs.core.MediaType[])
meth public abstract java.util.List<javax.ws.rs.core.Variant> build()
meth public abstract javax.ws.rs.core.Variant$VariantListBuilder add()
meth public static javax.ws.rs.core.Variant$VariantListBuilder newInstance()
supr java.lang.Object

CLSS public abstract interface javax.ws.rs.ext.ContextResolver<%0 extends java.lang.Object>
meth public abstract {javax.ws.rs.ext.ContextResolver%0} getContext(java.lang.Class<?>)

CLSS public abstract interface javax.ws.rs.ext.ExceptionMapper<%0 extends java.lang.Throwable>
meth public abstract javax.ws.rs.core.Response toResponse({javax.ws.rs.ext.ExceptionMapper%0})

CLSS public abstract interface javax.ws.rs.ext.InterceptorContext
meth public abstract java.lang.Class<?> getType()
meth public abstract java.lang.Object getProperty(java.lang.String)
meth public abstract java.lang.annotation.Annotation[] getAnnotations()
meth public abstract java.lang.reflect.Type getGenericType()
meth public abstract java.util.Collection<java.lang.String> getPropertyNames()
meth public abstract javax.ws.rs.core.MediaType getMediaType()
meth public abstract void removeProperty(java.lang.String)
meth public abstract void setAnnotations(java.lang.annotation.Annotation[])
meth public abstract void setGenericType(java.lang.reflect.Type)
meth public abstract void setMediaType(javax.ws.rs.core.MediaType)
meth public abstract void setProperty(java.lang.String,java.lang.Object)
meth public abstract void setType(java.lang.Class<?>)

CLSS public abstract interface javax.ws.rs.ext.MessageBodyReader<%0 extends java.lang.Object>
meth public abstract boolean isReadable(java.lang.Class<?>,java.lang.reflect.Type,java.lang.annotation.Annotation[],javax.ws.rs.core.MediaType)
meth public abstract {javax.ws.rs.ext.MessageBodyReader%0} readFrom(java.lang.Class<{javax.ws.rs.ext.MessageBodyReader%0}>,java.lang.reflect.Type,java.lang.annotation.Annotation[],javax.ws.rs.core.MediaType,javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String>,java.io.InputStream) throws java.io.IOException

CLSS public abstract interface javax.ws.rs.ext.MessageBodyWriter<%0 extends java.lang.Object>
meth public abstract boolean isWriteable(java.lang.Class<?>,java.lang.reflect.Type,java.lang.annotation.Annotation[],javax.ws.rs.core.MediaType)
meth public abstract long getSize({javax.ws.rs.ext.MessageBodyWriter%0},java.lang.Class<?>,java.lang.reflect.Type,java.lang.annotation.Annotation[],javax.ws.rs.core.MediaType)
meth public abstract void writeTo({javax.ws.rs.ext.MessageBodyWriter%0},java.lang.Class<?>,java.lang.reflect.Type,java.lang.annotation.Annotation[],javax.ws.rs.core.MediaType,javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.Object>,java.io.OutputStream) throws java.io.IOException

CLSS public abstract interface javax.ws.rs.ext.ParamConverter<%0 extends java.lang.Object>
innr public abstract interface static !annotation Lazy
meth public abstract java.lang.String toString({javax.ws.rs.ext.ParamConverter%0})
meth public abstract {javax.ws.rs.ext.ParamConverter%0} fromString(java.lang.String)

CLSS public abstract interface static !annotation javax.ws.rs.ext.ParamConverter$Lazy
 outer javax.ws.rs.ext.ParamConverter
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface javax.ws.rs.ext.ParamConverterProvider
meth public abstract <%0 extends java.lang.Object> javax.ws.rs.ext.ParamConverter<{%%0}> getConverter(java.lang.Class<{%%0}>,java.lang.reflect.Type,java.lang.annotation.Annotation[])

CLSS public abstract interface !annotation javax.ws.rs.ext.Provider
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

CLSS public abstract interface javax.ws.rs.ext.Providers
meth public abstract <%0 extends java.lang.Object> javax.ws.rs.ext.ContextResolver<{%%0}> getContextResolver(java.lang.Class<{%%0}>,javax.ws.rs.core.MediaType)
meth public abstract <%0 extends java.lang.Object> javax.ws.rs.ext.MessageBodyReader<{%%0}> getMessageBodyReader(java.lang.Class<{%%0}>,java.lang.reflect.Type,java.lang.annotation.Annotation[],javax.ws.rs.core.MediaType)
meth public abstract <%0 extends java.lang.Object> javax.ws.rs.ext.MessageBodyWriter<{%%0}> getMessageBodyWriter(java.lang.Class<{%%0}>,java.lang.reflect.Type,java.lang.annotation.Annotation[],javax.ws.rs.core.MediaType)
meth public abstract <%0 extends java.lang.Throwable> javax.ws.rs.ext.ExceptionMapper<{%%0}> getExceptionMapper(java.lang.Class<{%%0}>)

CLSS public abstract interface javax.ws.rs.ext.ReaderInterceptor
meth public abstract java.lang.Object aroundReadFrom(javax.ws.rs.ext.ReaderInterceptorContext) throws java.io.IOException

CLSS public abstract interface javax.ws.rs.ext.ReaderInterceptorContext
intf javax.ws.rs.ext.InterceptorContext
meth public abstract java.io.InputStream getInputStream()
meth public abstract java.lang.Object proceed() throws java.io.IOException
meth public abstract javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String> getHeaders()
meth public abstract void setInputStream(java.io.InputStream)

CLSS public abstract javax.ws.rs.ext.RuntimeDelegate
cons protected init()
fld public final static java.lang.String JAXRS_RUNTIME_DELEGATE_PROPERTY = "javax.ws.rs.ext.RuntimeDelegate"
innr public abstract interface static HeaderDelegate
meth public abstract <%0 extends java.lang.Object> javax.ws.rs.ext.RuntimeDelegate$HeaderDelegate<{%%0}> createHeaderDelegate(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} createEndpoint(javax.ws.rs.core.Application,java.lang.Class<{%%0}>)
meth public abstract javax.ws.rs.core.Link$Builder createLinkBuilder()
meth public abstract javax.ws.rs.core.Response$ResponseBuilder createResponseBuilder()
meth public abstract javax.ws.rs.core.UriBuilder createUriBuilder()
meth public abstract javax.ws.rs.core.Variant$VariantListBuilder createVariantListBuilder()
meth public static javax.ws.rs.ext.RuntimeDelegate getInstance()
meth public static void setInstance(javax.ws.rs.ext.RuntimeDelegate)
supr java.lang.Object
hfds JAXRS_DEFAULT_RUNTIME_DELEGATE,RD_LOCK,cachedDelegate,suppressAccessChecksPermission

CLSS public abstract interface static javax.ws.rs.ext.RuntimeDelegate$HeaderDelegate<%0 extends java.lang.Object>
 outer javax.ws.rs.ext.RuntimeDelegate
meth public abstract java.lang.String toString({javax.ws.rs.ext.RuntimeDelegate$HeaderDelegate%0})
meth public abstract {javax.ws.rs.ext.RuntimeDelegate$HeaderDelegate%0} fromString(java.lang.String)

CLSS public abstract interface javax.ws.rs.ext.WriterInterceptor
meth public abstract void aroundWriteTo(javax.ws.rs.ext.WriterInterceptorContext) throws java.io.IOException

CLSS public abstract interface javax.ws.rs.ext.WriterInterceptorContext
intf javax.ws.rs.ext.InterceptorContext
meth public abstract java.io.OutputStream getOutputStream()
meth public abstract java.lang.Object getEntity()
meth public abstract javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.Object> getHeaders()
meth public abstract void proceed() throws java.io.IOException
meth public abstract void setEntity(java.lang.Object)
meth public abstract void setOutputStream(java.io.OutputStream)

CLSS public abstract javax.xml.bind.annotation.adapters.XmlAdapter<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init()
meth public abstract {javax.xml.bind.annotation.adapters.XmlAdapter%0} marshal({javax.xml.bind.annotation.adapters.XmlAdapter%1}) throws java.lang.Exception
meth public abstract {javax.xml.bind.annotation.adapters.XmlAdapter%1} unmarshal({javax.xml.bind.annotation.adapters.XmlAdapter%0}) throws java.lang.Exception
supr java.lang.Object

CLSS public abstract interface !annotation org.glassfish.jersey.Beta
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE, TYPE, CONSTRUCTOR, METHOD, FIELD, PACKAGE])
intf java.lang.annotation.Annotation

CLSS public abstract interface org.glassfish.jersey.client.ChunkParser
meth public abstract byte[] readChunk(java.io.InputStream) throws java.io.IOException

CLSS public org.glassfish.jersey.client.ChunkedInput<%0 extends java.lang.Object>
cons protected init(java.lang.reflect.Type,java.io.InputStream,java.lang.annotation.Annotation[],javax.ws.rs.core.MediaType,javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String>,org.glassfish.jersey.message.MessageBodyWorkers,org.glassfish.jersey.internal.PropertiesDelegate)
intf java.io.Closeable
meth public boolean isClosed()
meth public javax.ws.rs.core.MediaType getChunkType()
meth public org.glassfish.jersey.client.ChunkParser getParser()
meth public static org.glassfish.jersey.client.ChunkParser createParser(byte[])
meth public static org.glassfish.jersey.client.ChunkParser createParser(java.lang.String)
meth public void close()
meth public void setChunkType(java.lang.String)
meth public void setChunkType(javax.ws.rs.core.MediaType)
meth public void setParser(org.glassfish.jersey.client.ChunkParser)
meth public {org.glassfish.jersey.client.ChunkedInput%0} read()
supr javax.ws.rs.core.GenericType<{org.glassfish.jersey.client.ChunkedInput%0}>
hfds LOGGER,annotations,closed,headers,inputStream,mediaType,messageBodyWorkers,parser,propertiesDelegate
hcls FixedBoundaryParser

CLSS public org.glassfish.jersey.client.ClientConfig
cons public !varargs init(java.lang.Class<?>[])
cons public !varargs init(java.lang.Object[])
cons public init()
intf javax.ws.rs.core.Configurable<org.glassfish.jersey.client.ClientConfig>
intf javax.ws.rs.core.Configuration
meth public !varargs org.glassfish.jersey.client.ClientConfig register(java.lang.Class<?>,java.lang.Class<?>[])
meth public !varargs org.glassfish.jersey.client.ClientConfig register(java.lang.Object,java.lang.Class<?>[])
meth public boolean equals(java.lang.Object)
meth public boolean isEnabled(java.lang.Class<? extends javax.ws.rs.core.Feature>)
meth public boolean isEnabled(javax.ws.rs.core.Feature)
meth public boolean isRegistered(java.lang.Class<?>)
meth public boolean isRegistered(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.util.Collection<java.lang.String> getPropertyNames()
meth public java.util.Map<java.lang.Class<?>,java.lang.Integer> getContracts(java.lang.Class<?>)
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public java.util.Set<java.lang.Class<?>> getClasses()
meth public java.util.Set<java.lang.Object> getInstances()
meth public javax.ws.rs.RuntimeType getRuntimeType()
meth public org.glassfish.jersey.client.ClientConfig connectorProvider(org.glassfish.jersey.client.spi.ConnectorProvider)
meth public org.glassfish.jersey.client.ClientConfig getConfiguration()
meth public org.glassfish.jersey.client.ClientConfig loadFrom(javax.ws.rs.core.Configuration)
meth public org.glassfish.jersey.client.ClientConfig property(java.lang.String,java.lang.Object)
meth public org.glassfish.jersey.client.ClientConfig register(java.lang.Class<?>)
meth public org.glassfish.jersey.client.ClientConfig register(java.lang.Class<?>,int)
meth public org.glassfish.jersey.client.ClientConfig register(java.lang.Class<?>,java.util.Map<java.lang.Class<?>,java.lang.Integer>)
meth public org.glassfish.jersey.client.ClientConfig register(java.lang.Object)
meth public org.glassfish.jersey.client.ClientConfig register(java.lang.Object,int)
meth public org.glassfish.jersey.client.ClientConfig register(java.lang.Object,java.util.Map<java.lang.Class<?>,java.lang.Integer>)
meth public org.glassfish.jersey.client.JerseyClient getClient()
meth public org.glassfish.jersey.client.spi.Connector getConnector()
meth public org.glassfish.jersey.client.spi.ConnectorProvider getConnectorProvider()
supr java.lang.Object
hfds state
hcls State

CLSS public final org.glassfish.jersey.client.ClientProperties
fld public final static java.lang.String ASYNC_THREADPOOL_SIZE = "jersey.config.client.async.threadPoolSize"
fld public final static java.lang.String CHUNKED_ENCODING_SIZE = "jersey.config.client.chunkedEncodingSize"
fld public final static java.lang.String CONNECT_TIMEOUT = "jersey.config.client.connectTimeout"
fld public final static java.lang.String DIGESTAUTH_URI_CACHE_SIZELIMIT = "jersey.config.client.digestAuthUriCacheSizeLimit"
fld public final static java.lang.String FEATURE_AUTO_DISCOVERY_DISABLE = "jersey.config.disableAutoDiscovery.client"
fld public final static java.lang.String FOLLOW_REDIRECTS = "jersey.config.client.followRedirects"
fld public final static java.lang.String JSON_PROCESSING_FEATURE_DISABLE = "jersey.config.disableJsonProcessing.client"
fld public final static java.lang.String METAINF_SERVICES_LOOKUP_DISABLE = "jersey.config.disableMetainfServicesLookup.client"
fld public final static java.lang.String MOXY_JSON_FEATURE_DISABLE = "jersey.config.disableMoxyJson.client"
fld public final static java.lang.String OUTBOUND_CONTENT_LENGTH_BUFFER = "jersey.config.contentLength.buffer.client"
fld public final static java.lang.String PROXY_PASSWORD = "jersey.config.client.proxy.password"
fld public final static java.lang.String PROXY_URI = "jersey.config.client.proxy.uri"
fld public final static java.lang.String PROXY_USERNAME = "jersey.config.client.proxy.username"
fld public final static java.lang.String READ_TIMEOUT = "jersey.config.client.readTimeout"
fld public final static java.lang.String REQUEST_ENTITY_PROCESSING = "jersey.config.client.request.entity.processing"
fld public final static java.lang.String SUPPRESS_HTTP_COMPLIANCE_VALIDATION = "jersey.config.client.suppressHttpComplianceValidation"
fld public final static java.lang.String USE_ENCODING = "jersey.config.client.useEncoding"
supr java.lang.Object

CLSS public org.glassfish.jersey.client.ClientRequest
cons protected init(java.net.URI,org.glassfish.jersey.client.ClientConfig,org.glassfish.jersey.internal.PropertiesDelegate)
cons public init(org.glassfish.jersey.client.ClientRequest)
intf javax.ws.rs.client.ClientRequestContext
meth public !varargs void accept(java.lang.String[])
meth public !varargs void accept(javax.ws.rs.core.MediaType[])
meth public !varargs void acceptLanguage(java.lang.String[])
meth public !varargs void acceptLanguage(java.util.Locale[])
meth public <%0 extends java.lang.Object> {%%0} resolveProperty(java.lang.String,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} resolveProperty(java.lang.String,{%%0})
meth public boolean isAsynchronous()
meth public java.lang.Iterable<javax.ws.rs.ext.ReaderInterceptor> getReaderInterceptors()
meth public java.lang.Iterable<javax.ws.rs.ext.WriterInterceptor> getWriterInterceptors()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.String getMethod()
meth public java.net.URI getUri()
meth public java.util.Collection<java.lang.String> getPropertyNames()
meth public java.util.Map<java.lang.String,javax.ws.rs.core.Cookie> getCookies()
meth public javax.ws.rs.core.Configuration getConfiguration()
meth public javax.ws.rs.core.Response getAbortResponse()
meth public org.glassfish.jersey.client.JerseyClient getClient()
meth public org.glassfish.jersey.message.MessageBodyWorkers getWorkers()
meth public void abortWith(javax.ws.rs.core.Response)
meth public void cacheControl(javax.ws.rs.core.CacheControl)
meth public void cookie(javax.ws.rs.core.Cookie)
meth public void enableBuffering()
meth public void encoding(java.lang.String)
meth public void language(java.lang.String)
meth public void language(java.util.Locale)
meth public void removeProperty(java.lang.String)
meth public void setMethod(java.lang.String)
meth public void setProperty(java.lang.String,java.lang.Object)
meth public void setUri(java.net.URI)
meth public void setWorkers(org.glassfish.jersey.message.MessageBodyWorkers)
meth public void type(java.lang.String)
meth public void type(javax.ws.rs.core.MediaType)
meth public void variant(javax.ws.rs.core.Variant)
meth public void writeEntity() throws java.io.IOException
supr org.glassfish.jersey.message.internal.OutboundMessageContext
hfds LOGGER,abortResponse,asynchronous,clientConfig,entityWritten,httpMethod,propertiesDelegate,readerInterceptors,requestUri,workers,writerInterceptors

CLSS public org.glassfish.jersey.client.ClientResponse
cons public init(javax.ws.rs.core.Response$StatusType,org.glassfish.jersey.client.ClientRequest)
cons public init(org.glassfish.jersey.client.ClientRequest,javax.ws.rs.core.Response)
intf javax.ws.rs.client.ClientResponseContext
meth public <%0 extends java.lang.Object> {%%0} readEntity(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} readEntity(java.lang.Class<{%%0}>,java.lang.annotation.Annotation[])
meth public <%0 extends java.lang.Object> {%%0} readEntity(javax.ws.rs.core.GenericType<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} readEntity(javax.ws.rs.core.GenericType<{%%0}>,java.lang.annotation.Annotation[])
meth public int getStatus()
meth public java.lang.Object getEntity()
meth public java.lang.String toString()
meth public java.util.Map<java.lang.String,javax.ws.rs.core.NewCookie> getCookies()
meth public java.util.Set<javax.ws.rs.core.Link> getLinks()
meth public javax.ws.rs.core.Response$StatusType getStatusInfo()
meth public org.glassfish.jersey.client.ClientRequest getRequestContext()
meth public void setStatus(int)
meth public void setStatusInfo(javax.ws.rs.core.Response$StatusType)
supr org.glassfish.jersey.message.internal.InboundMessageContext
hfds requestContext,status

CLSS public org.glassfish.jersey.client.CustomProvidersFeature
cons public init(java.util.Collection<java.lang.Class<?>>)
intf javax.ws.rs.core.Feature
meth public boolean configure(javax.ws.rs.core.FeatureContext)
supr java.lang.Object
hfds providers

CLSS public org.glassfish.jersey.client.HttpUrlConnectorProvider
cons public init()
fld public final static java.lang.String SET_METHOD_WORKAROUND = "jersey.config.client.httpUrlConnection.setMethodWorkaround"
fld public final static java.lang.String USE_FIXED_LENGTH_STREAMING = "jersey.config.client.httpUrlConnector.useFixedLengthStreaming"
innr public abstract interface static ConnectionFactory
intf org.glassfish.jersey.client.spi.ConnectorProvider
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public org.glassfish.jersey.client.HttpUrlConnectorProvider chunkSize(int)
meth public org.glassfish.jersey.client.HttpUrlConnectorProvider connectionFactory(org.glassfish.jersey.client.HttpUrlConnectorProvider$ConnectionFactory)
meth public org.glassfish.jersey.client.HttpUrlConnectorProvider useFixedLengthStreaming()
meth public org.glassfish.jersey.client.HttpUrlConnectorProvider useSetMethodWorkaround()
meth public org.glassfish.jersey.client.spi.Connector getConnector(javax.ws.rs.client.Client,javax.ws.rs.core.Configuration)
supr java.lang.Object
hfds DEFAULT_CONNECTION_FACTORY,DEFAULT_HTTP_CHUNK_SIZE,LOGGER,chunkSize,connectionFactory,useFixedLengthStreaming,useSetMethodWorkaround
hcls DefaultConnectionFactory

CLSS public abstract interface static org.glassfish.jersey.client.HttpUrlConnectorProvider$ConnectionFactory
 outer org.glassfish.jersey.client.HttpUrlConnectorProvider
meth public abstract java.net.HttpURLConnection getConnection(java.net.URL) throws java.io.IOException

CLSS public org.glassfish.jersey.client.JerseyClient
cons protected init()
cons protected init(javax.ws.rs.core.Configuration,javax.net.ssl.SSLContext,javax.net.ssl.HostnameVerifier)
cons protected init(javax.ws.rs.core.Configuration,org.glassfish.jersey.internal.util.collection.UnsafeValue<javax.net.ssl.SSLContext,java.lang.IllegalStateException>,javax.net.ssl.HostnameVerifier)
intf javax.ws.rs.client.Client
meth public !varargs org.glassfish.jersey.client.JerseyClient register(java.lang.Class<?>,java.lang.Class<?>[])
meth public !varargs org.glassfish.jersey.client.JerseyClient register(java.lang.Object,java.lang.Class<?>[])
meth public boolean isClosed()
meth public javax.net.ssl.HostnameVerifier getHostnameVerifier()
meth public javax.net.ssl.SSLContext getSslContext()
meth public org.glassfish.jersey.client.ClientConfig getConfiguration()
meth public org.glassfish.jersey.client.JerseyClient preInitialize()
meth public org.glassfish.jersey.client.JerseyClient property(java.lang.String,java.lang.Object)
meth public org.glassfish.jersey.client.JerseyClient register(java.lang.Class<?>)
meth public org.glassfish.jersey.client.JerseyClient register(java.lang.Class<?>,int)
meth public org.glassfish.jersey.client.JerseyClient register(java.lang.Class<?>,java.util.Map<java.lang.Class<?>,java.lang.Integer>)
meth public org.glassfish.jersey.client.JerseyClient register(java.lang.Object)
meth public org.glassfish.jersey.client.JerseyClient register(java.lang.Object,int)
meth public org.glassfish.jersey.client.JerseyClient register(java.lang.Object,java.util.Map<java.lang.Class<?>,java.lang.Integer>)
meth public org.glassfish.jersey.client.JerseyInvocation$Builder invocation(javax.ws.rs.core.Link)
meth public org.glassfish.jersey.client.JerseyWebTarget target(java.lang.String)
meth public org.glassfish.jersey.client.JerseyWebTarget target(java.net.URI)
meth public org.glassfish.jersey.client.JerseyWebTarget target(javax.ws.rs.core.Link)
meth public org.glassfish.jersey.client.JerseyWebTarget target(javax.ws.rs.core.UriBuilder)
meth public void addListener(org.glassfish.jersey.client.JerseyClient$LifecycleListener)
meth public void close()
supr java.lang.Object
hfds closedFlag,config,hostnameVerifier,listeners,sslContext
hcls LifecycleListener

CLSS public org.glassfish.jersey.client.JerseyClientBuilder
cons public init()
meth public !varargs org.glassfish.jersey.client.JerseyClientBuilder register(java.lang.Class<?>,java.lang.Class<?>[])
meth public !varargs org.glassfish.jersey.client.JerseyClientBuilder register(java.lang.Object,java.lang.Class<?>[])
meth public org.glassfish.jersey.client.ClientConfig getConfiguration()
meth public org.glassfish.jersey.client.JerseyClient build()
meth public org.glassfish.jersey.client.JerseyClientBuilder hostnameVerifier(javax.net.ssl.HostnameVerifier)
meth public org.glassfish.jersey.client.JerseyClientBuilder keyStore(java.security.KeyStore,char[])
meth public org.glassfish.jersey.client.JerseyClientBuilder property(java.lang.String,java.lang.Object)
meth public org.glassfish.jersey.client.JerseyClientBuilder register(java.lang.Class<?>)
meth public org.glassfish.jersey.client.JerseyClientBuilder register(java.lang.Class<?>,int)
meth public org.glassfish.jersey.client.JerseyClientBuilder register(java.lang.Class<?>,java.util.Map<java.lang.Class<?>,java.lang.Integer>)
meth public org.glassfish.jersey.client.JerseyClientBuilder register(java.lang.Object)
meth public org.glassfish.jersey.client.JerseyClientBuilder register(java.lang.Object,int)
meth public org.glassfish.jersey.client.JerseyClientBuilder register(java.lang.Object,java.util.Map<java.lang.Class<?>,java.lang.Integer>)
meth public org.glassfish.jersey.client.JerseyClientBuilder sslContext(javax.net.ssl.SSLContext)
meth public org.glassfish.jersey.client.JerseyClientBuilder trustStore(java.security.KeyStore)
meth public org.glassfish.jersey.client.JerseyClientBuilder withConfig(javax.ws.rs.core.Configuration)
meth public static org.glassfish.jersey.client.JerseyClient createClient()
meth public static org.glassfish.jersey.client.JerseyClient createClient(javax.ws.rs.core.Configuration)
supr javax.ws.rs.client.ClientBuilder
hfds config,hostnameVerifier,sslConfigurator,sslContext

CLSS public org.glassfish.jersey.client.JerseyInvocation
innr public static Builder
intf javax.ws.rs.client.Invocation
meth public <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(javax.ws.rs.client.InvocationCallback<{%%0}>)
meth public <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(javax.ws.rs.core.GenericType<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} invoke(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} invoke(javax.ws.rs.core.GenericType<{%%0}>)
meth public java.util.concurrent.Future<javax.ws.rs.core.Response> submit()
meth public javax.ws.rs.core.Response invoke()
meth public org.glassfish.jersey.client.JerseyInvocation property(java.lang.String,java.lang.Object)
supr java.lang.Object
hfds LOGGER,METHODS,requestContext
hcls AsyncInvoker,EntityPresence

CLSS public static org.glassfish.jersey.client.JerseyInvocation$Builder
 outer org.glassfish.jersey.client.JerseyInvocation
cons protected init(java.net.URI,org.glassfish.jersey.client.ClientConfig)
intf javax.ws.rs.client.Invocation$Builder
meth public !varargs javax.ws.rs.client.Invocation$Builder acceptEncoding(java.lang.String[])
meth public !varargs org.glassfish.jersey.client.JerseyInvocation$Builder accept(java.lang.String[])
meth public !varargs org.glassfish.jersey.client.JerseyInvocation$Builder accept(javax.ws.rs.core.MediaType[])
meth public !varargs org.glassfish.jersey.client.JerseyInvocation$Builder acceptLanguage(java.lang.String[])
meth public !varargs org.glassfish.jersey.client.JerseyInvocation$Builder acceptLanguage(java.util.Locale[])
meth public <%0 extends java.lang.Object> {%%0} delete(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} delete(javax.ws.rs.core.GenericType<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} get(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} get(javax.ws.rs.core.GenericType<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} method(java.lang.String,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} method(java.lang.String,javax.ws.rs.client.Entity<?>,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} method(java.lang.String,javax.ws.rs.client.Entity<?>,javax.ws.rs.core.GenericType<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} method(java.lang.String,javax.ws.rs.core.GenericType<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} options(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} options(javax.ws.rs.core.GenericType<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} post(javax.ws.rs.client.Entity<?>,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} post(javax.ws.rs.client.Entity<?>,javax.ws.rs.core.GenericType<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} put(javax.ws.rs.client.Entity<?>,java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} put(javax.ws.rs.client.Entity<?>,javax.ws.rs.core.GenericType<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} trace(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} trace(javax.ws.rs.core.GenericType<{%%0}>)
meth public javax.ws.rs.client.AsyncInvoker async()
meth public javax.ws.rs.core.Response delete()
meth public javax.ws.rs.core.Response get()
meth public javax.ws.rs.core.Response head()
meth public javax.ws.rs.core.Response method(java.lang.String)
meth public javax.ws.rs.core.Response method(java.lang.String,javax.ws.rs.client.Entity<?>)
meth public javax.ws.rs.core.Response options()
meth public javax.ws.rs.core.Response post(javax.ws.rs.client.Entity<?>)
meth public javax.ws.rs.core.Response put(javax.ws.rs.client.Entity<?>)
meth public javax.ws.rs.core.Response trace()
meth public org.glassfish.jersey.client.JerseyInvocation build(java.lang.String)
meth public org.glassfish.jersey.client.JerseyInvocation build(java.lang.String,javax.ws.rs.client.Entity<?>)
meth public org.glassfish.jersey.client.JerseyInvocation buildDelete()
meth public org.glassfish.jersey.client.JerseyInvocation buildGet()
meth public org.glassfish.jersey.client.JerseyInvocation buildPost(javax.ws.rs.client.Entity<?>)
meth public org.glassfish.jersey.client.JerseyInvocation buildPut(javax.ws.rs.client.Entity<?>)
meth public org.glassfish.jersey.client.JerseyInvocation$Builder cacheControl(javax.ws.rs.core.CacheControl)
meth public org.glassfish.jersey.client.JerseyInvocation$Builder cookie(java.lang.String,java.lang.String)
meth public org.glassfish.jersey.client.JerseyInvocation$Builder cookie(javax.ws.rs.core.Cookie)
meth public org.glassfish.jersey.client.JerseyInvocation$Builder header(java.lang.String,java.lang.Object)
meth public org.glassfish.jersey.client.JerseyInvocation$Builder headers(javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.Object>)
meth public org.glassfish.jersey.client.JerseyInvocation$Builder property(java.lang.String,java.lang.Object)
supr java.lang.Object
hfds requestContext

CLSS public org.glassfish.jersey.client.JerseyWebTarget
cons protected init(javax.ws.rs.core.UriBuilder,org.glassfish.jersey.client.ClientConfig)
cons protected init(javax.ws.rs.core.UriBuilder,org.glassfish.jersey.client.JerseyWebTarget)
intf javax.ws.rs.client.WebTarget
meth public !varargs org.glassfish.jersey.client.JerseyInvocation$Builder request(java.lang.String[])
meth public !varargs org.glassfish.jersey.client.JerseyInvocation$Builder request(javax.ws.rs.core.MediaType[])
meth public !varargs org.glassfish.jersey.client.JerseyWebTarget matrixParam(java.lang.String,java.lang.Object[])
meth public !varargs org.glassfish.jersey.client.JerseyWebTarget queryParam(java.lang.String,java.lang.Object[])
meth public !varargs org.glassfish.jersey.client.JerseyWebTarget register(java.lang.Class<?>,java.lang.Class<?>[])
meth public !varargs org.glassfish.jersey.client.JerseyWebTarget register(java.lang.Object,java.lang.Class<?>[])
meth public java.net.URI getUri()
meth public javax.ws.rs.core.UriBuilder getUriBuilder()
meth public org.glassfish.jersey.client.ClientConfig getConfiguration()
meth public org.glassfish.jersey.client.JerseyInvocation$Builder request()
meth public org.glassfish.jersey.client.JerseyWebTarget path(java.lang.String)
meth public org.glassfish.jersey.client.JerseyWebTarget preInitialize()
meth public org.glassfish.jersey.client.JerseyWebTarget property(java.lang.String,java.lang.Object)
meth public org.glassfish.jersey.client.JerseyWebTarget register(java.lang.Class<?>)
meth public org.glassfish.jersey.client.JerseyWebTarget register(java.lang.Class<?>,int)
meth public org.glassfish.jersey.client.JerseyWebTarget register(java.lang.Class<?>,java.util.Map<java.lang.Class<?>,java.lang.Integer>)
meth public org.glassfish.jersey.client.JerseyWebTarget register(java.lang.Object)
meth public org.glassfish.jersey.client.JerseyWebTarget register(java.lang.Object,int)
meth public org.glassfish.jersey.client.JerseyWebTarget register(java.lang.Object,java.util.Map<java.lang.Class<?>,java.lang.Integer>)
meth public org.glassfish.jersey.client.JerseyWebTarget resolveTemplate(java.lang.String,java.lang.Object)
meth public org.glassfish.jersey.client.JerseyWebTarget resolveTemplate(java.lang.String,java.lang.Object,boolean)
meth public org.glassfish.jersey.client.JerseyWebTarget resolveTemplateFromEncoded(java.lang.String,java.lang.Object)
meth public org.glassfish.jersey.client.JerseyWebTarget resolveTemplates(java.util.Map<java.lang.String,java.lang.Object>)
meth public org.glassfish.jersey.client.JerseyWebTarget resolveTemplates(java.util.Map<java.lang.String,java.lang.Object>,boolean)
meth public org.glassfish.jersey.client.JerseyWebTarget resolveTemplatesFromEncoded(java.util.Map<java.lang.String,java.lang.Object>)
supr java.lang.Object
hfds config,targetUri

CLSS public final !enum org.glassfish.jersey.client.RequestEntityProcessing
fld public final static org.glassfish.jersey.client.RequestEntityProcessing BUFFERED
fld public final static org.glassfish.jersey.client.RequestEntityProcessing CHUNKED
meth public static org.glassfish.jersey.client.RequestEntityProcessing valueOf(java.lang.String)
meth public static org.glassfish.jersey.client.RequestEntityProcessing[] values()
supr java.lang.Enum<org.glassfish.jersey.client.RequestEntityProcessing>

CLSS public org.glassfish.jersey.client.RequestProcessingInitializationStage
cons public init(javax.inject.Provider<org.glassfish.jersey.internal.util.collection.Ref<org.glassfish.jersey.client.ClientRequest>>,javax.inject.Provider<org.glassfish.jersey.message.MessageBodyWorkers>,org.glassfish.hk2.api.ServiceLocator)
 anno 0 javax.inject.Inject()
intf com.google.common.base.Function<org.glassfish.jersey.client.ClientRequest,org.glassfish.jersey.client.ClientRequest>
meth public org.glassfish.jersey.client.ClientRequest apply(org.glassfish.jersey.client.ClientRequest)
supr java.lang.Object
hfds readerInterceptors,requestRefProvider,workersProvider,writerInterceptors

CLSS public org.glassfish.jersey.client.filter.CsrfProtectionFilter
cons public init()
cons public init(java.lang.String)
fld public final static java.lang.String HEADER_NAME = "X-Requested-By"
intf javax.ws.rs.client.ClientRequestFilter
meth public void filter(javax.ws.rs.client.ClientRequestContext) throws java.io.IOException
supr java.lang.Object
hfds METHODS_TO_IGNORE,requestedBy

CLSS public org.glassfish.jersey.client.filter.EncodingFeature
cons public !varargs init(java.lang.Class<?>[])
cons public !varargs init(java.lang.String,java.lang.Class<?>[])
intf javax.ws.rs.core.Feature
meth public boolean configure(javax.ws.rs.core.FeatureContext)
supr java.lang.Object
hfds encodingProviders,useEncoding

CLSS public final org.glassfish.jersey.client.filter.EncodingFilter
cons public init()
intf javax.ws.rs.client.ClientRequestFilter
meth public void filter(javax.ws.rs.client.ClientRequestContext) throws java.io.IOException
supr java.lang.Object
hfds serviceLocator,supportedEncodings

CLSS public final org.glassfish.jersey.client.filter.HttpBasicAuthFilter
cons public init(java.lang.String,byte[])
cons public init(java.lang.String,java.lang.String)
intf javax.ws.rs.client.ClientRequestFilter
meth public void filter(javax.ws.rs.client.ClientRequestContext) throws java.io.IOException
supr java.lang.Object
hfds CHARACTER_SET,authentication

CLSS public org.glassfish.jersey.client.filter.HttpDigestAuthFilter
 anno 0 javax.ws.rs.ext.Provider()
cons public init(java.lang.String,java.lang.String)
intf javax.ws.rs.client.ClientRequestFilter
intf javax.ws.rs.client.ClientResponseFilter
meth public void filter(javax.ws.rs.client.ClientRequestContext) throws java.io.IOException
meth public void filter(javax.ws.rs.client.ClientRequestContext,javax.ws.rs.client.ClientResponseContext) throws java.io.IOException
supr java.lang.Object
hfds CHARACTER_SET,CLIENT_NONCE_BYTE_COUNT,HEADER_DIGEST_SCHEME,HEX_ARRAY,KEY_VALUE_PAIR_PATTERN,MAXIMUM_DIGEST_CACHE_SIZE,config,digestCache,logger,password,randomGenerator,username
hcls Algorithm,DigestScheme,QOP

CLSS public abstract interface org.glassfish.jersey.client.spi.AsyncConnectorCallback
meth public abstract void failure(java.lang.Throwable)
meth public abstract void response(org.glassfish.jersey.client.ClientResponse)

CLSS public abstract interface org.glassfish.jersey.client.spi.Connector
intf org.glassfish.jersey.process.Inflector<org.glassfish.jersey.client.ClientRequest,org.glassfish.jersey.client.ClientResponse>
meth public abstract java.lang.String getName()
meth public abstract java.util.concurrent.Future<?> apply(org.glassfish.jersey.client.ClientRequest,org.glassfish.jersey.client.spi.AsyncConnectorCallback)
meth public abstract org.glassfish.jersey.client.ClientResponse apply(org.glassfish.jersey.client.ClientRequest)
meth public abstract void close()

CLSS public abstract interface org.glassfish.jersey.client.spi.ConnectorProvider
 anno 0 org.glassfish.jersey.Beta()
meth public abstract org.glassfish.jersey.client.spi.Connector getConnector(javax.ws.rs.client.Client,javax.ws.rs.core.Configuration)

CLSS public org.glassfish.jersey.message.internal.InboundMessageContext
cons public init()
cons public init(boolean)
meth public !varargs org.glassfish.jersey.message.internal.InboundMessageContext headers(java.lang.String,java.lang.Object[])
meth public <%0 extends java.lang.Object> {%%0} readEntity(java.lang.Class<{%%0}>,java.lang.annotation.Annotation[],org.glassfish.jersey.internal.PropertiesDelegate)
meth public <%0 extends java.lang.Object> {%%0} readEntity(java.lang.Class<{%%0}>,java.lang.reflect.Type,java.lang.annotation.Annotation[],org.glassfish.jersey.internal.PropertiesDelegate)
meth public <%0 extends java.lang.Object> {%%0} readEntity(java.lang.Class<{%%0}>,java.lang.reflect.Type,org.glassfish.jersey.internal.PropertiesDelegate)
meth public <%0 extends java.lang.Object> {%%0} readEntity(java.lang.Class<{%%0}>,org.glassfish.jersey.internal.PropertiesDelegate)
meth public boolean bufferEntity()
meth public boolean hasEntity()
meth public boolean hasLink(java.lang.String)
meth public int getLength()
meth public java.io.InputStream getEntityStream()
meth public java.lang.String getHeaderString(java.lang.String)
meth public java.net.URI getLocation()
meth public java.util.Date getDate()
meth public java.util.Date getLastModified()
meth public java.util.List<org.glassfish.jersey.message.internal.AcceptableLanguageTag> getQualifiedAcceptableLanguages()
meth public java.util.List<org.glassfish.jersey.message.internal.AcceptableMediaType> getQualifiedAcceptableMediaTypes()
meth public java.util.List<org.glassfish.jersey.message.internal.AcceptableToken> getQualifiedAcceptCharset()
meth public java.util.List<org.glassfish.jersey.message.internal.AcceptableToken> getQualifiedAcceptEncoding()
meth public java.util.Locale getLanguage()
meth public java.util.Map<java.lang.String,javax.ws.rs.core.Cookie> getRequestCookies()
meth public java.util.Map<java.lang.String,javax.ws.rs.core.NewCookie> getResponseCookies()
meth public java.util.Set<java.lang.String> getAllowedMethods()
meth public java.util.Set<javax.ws.rs.core.Link> getLinks()
meth public java.util.Set<org.glassfish.jersey.message.internal.MatchingEntityTag> getIfMatch()
meth public java.util.Set<org.glassfish.jersey.message.internal.MatchingEntityTag> getIfNoneMatch()
meth public javax.ws.rs.core.EntityTag getEntityTag()
meth public javax.ws.rs.core.Link getLink(java.lang.String)
meth public javax.ws.rs.core.Link$Builder getLinkBuilder(java.lang.String)
meth public javax.ws.rs.core.MediaType getMediaType()
meth public javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String> getHeaders()
meth public org.glassfish.jersey.message.MessageBodyWorkers getWorkers()
meth public org.glassfish.jersey.message.internal.InboundMessageContext header(java.lang.String,java.lang.Object)
meth public org.glassfish.jersey.message.internal.InboundMessageContext headers(java.lang.String,java.lang.Iterable<?>)
meth public org.glassfish.jersey.message.internal.InboundMessageContext headers(java.util.Map<java.lang.String,java.util.List<java.lang.String>>)
meth public org.glassfish.jersey.message.internal.InboundMessageContext headers(javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String>)
meth public org.glassfish.jersey.message.internal.InboundMessageContext remove(java.lang.String)
meth public void close()
meth public void setEntityStream(java.io.InputStream)
meth public void setReaderInterceptors(org.glassfish.jersey.internal.util.collection.Value<java.lang.Iterable<javax.ws.rs.ext.ReaderInterceptor>>)
meth public void setWorkers(org.glassfish.jersey.message.MessageBodyWorkers)
supr java.lang.Object
hfds EMPTY,EMPTY_ANNOTATIONS,entityContent,headers,readerInterceptors,translateNce,workers
hcls EntityContent

CLSS public org.glassfish.jersey.message.internal.OutboundMessageContext
cons public init()
cons public init(org.glassfish.jersey.message.internal.OutboundMessageContext)
innr public abstract interface static StreamProvider
meth public boolean hasEntity()
meth public boolean hasLink(java.lang.String)
meth public boolean isCommitted()
meth public int getLength()
meth public java.io.OutputStream getEntityStream()
meth public java.lang.Class<?> getEntityClass()
meth public java.lang.Object getEntity()
meth public java.lang.String getHeaderString(java.lang.String)
meth public java.lang.annotation.Annotation[] getEntityAnnotations()
meth public java.lang.reflect.Type getEntityType()
meth public java.net.URI getLocation()
meth public java.util.Date getDate()
meth public java.util.Date getLastModified()
meth public java.util.List<java.util.Locale> getAcceptableLanguages()
meth public java.util.List<javax.ws.rs.core.MediaType> getAcceptableMediaTypes()
meth public java.util.Locale getLanguage()
meth public java.util.Map<java.lang.String,javax.ws.rs.core.Cookie> getRequestCookies()
meth public java.util.Map<java.lang.String,javax.ws.rs.core.NewCookie> getResponseCookies()
meth public java.util.Set<java.lang.String> getAllowedMethods()
meth public java.util.Set<javax.ws.rs.core.Link> getLinks()
meth public javax.ws.rs.core.EntityTag getEntityTag()
meth public javax.ws.rs.core.Link getLink(java.lang.String)
meth public javax.ws.rs.core.Link$Builder getLinkBuilder(java.lang.String)
meth public javax.ws.rs.core.MediaType getMediaType()
meth public javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.Object> getHeaders()
meth public javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.String> getStringHeaders()
meth public void close()
meth public void commitStream() throws java.io.IOException
meth public void enableBuffering(javax.ws.rs.core.Configuration)
meth public void replaceHeaders(javax.ws.rs.core.MultivaluedMap<java.lang.String,java.lang.Object>)
meth public void setEntity(java.lang.Object)
meth public void setEntity(java.lang.Object,java.lang.annotation.Annotation[])
meth public void setEntity(java.lang.Object,java.lang.annotation.Annotation[],javax.ws.rs.core.MediaType)
meth public void setEntity(java.lang.Object,java.lang.reflect.Type,java.lang.annotation.Annotation[])
meth public void setEntityAnnotations(java.lang.annotation.Annotation[])
meth public void setEntityStream(java.io.OutputStream)
meth public void setEntityType(java.lang.reflect.Type)
meth public void setMediaType(javax.ws.rs.core.MediaType)
meth public void setStreamProvider(org.glassfish.jersey.message.internal.OutboundMessageContext$StreamProvider)
supr java.lang.Object
hfds EMPTY_ANNOTATIONS,committingOutputStream,entity,entityAnnotations,entityStream,entityType,headers

CLSS public abstract interface org.glassfish.jersey.process.Inflector<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {org.glassfish.jersey.process.Inflector%1} apply({org.glassfish.jersey.process.Inflector%0})

