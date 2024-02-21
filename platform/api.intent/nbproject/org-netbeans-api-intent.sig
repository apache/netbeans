#Signature file v4.1
#Version 1.25

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract interface org.netbeans.api.intent.Callback
meth public abstract void failure(java.lang.Exception)
 anno 1 org.netbeans.api.annotations.common.NonNull()
meth public abstract void success(java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()

CLSS public final org.netbeans.api.intent.Intent
cons public init(java.lang.String,java.net.URI)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
fld public final static java.lang.String ACTION_EDIT = "EDIT"
fld public final static java.lang.String ACTION_VIEW = "VIEW"
meth public java.lang.String getAction()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String toString()
meth public java.net.URI getUri()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.SortedSet<? extends org.netbeans.api.intent.IntentAction> getIntentActions()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.concurrent.Future<java.lang.Object> execute()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void execute(org.netbeans.api.intent.Callback)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds LOG,action,uri

CLSS public final org.netbeans.api.intent.IntentAction
meth public java.lang.String getDisplayName()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.lang.String getIcon()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public java.util.concurrent.Future<java.lang.Object> execute()
 anno 0 org.netbeans.api.annotations.common.NonNull()
meth public void execute(org.netbeans.api.intent.Callback)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds delegate,intent

CLSS public org.netbeans.api.intent.NoAvailableHandlerException
cons public init(org.netbeans.api.intent.Intent)
cons public init(org.netbeans.api.intent.Intent,java.lang.Throwable)
supr java.lang.Exception

CLSS abstract interface org.netbeans.api.intent.package-info

CLSS public abstract interface !annotation org.netbeans.spi.intent.IntentHandlerRegistration
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String icon()
meth public abstract int position()
meth public abstract java.lang.String displayName()
meth public abstract java.lang.String uriPattern()
meth public abstract java.lang.String[] actions()

CLSS public abstract interface org.netbeans.spi.intent.Result
meth public abstract void setException(java.lang.Exception)
meth public abstract void setResult(java.lang.Object)
 anno 1 org.netbeans.api.annotations.common.NullAllowed()

CLSS abstract interface org.netbeans.spi.intent.package-info

