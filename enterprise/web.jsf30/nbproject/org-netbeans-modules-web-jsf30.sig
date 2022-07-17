#Signature file v4.1
#Version 1.50.0

CLSS public jakarta.faces.FacesException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public java.lang.Throwable getCause()
supr java.lang.RuntimeException
hfds cause,serialVersionUID

CLSS public abstract interface jakarta.faces.FacesWrapper<%0 extends java.lang.Object>
meth public abstract {jakarta.faces.FacesWrapper%0} getWrapped()

CLSS public final jakarta.faces.FactoryFinder
fld public final static java.lang.String APPLICATION_FACTORY = "jakarta.faces.application.ApplicationFactory"
fld public final static java.lang.String CLIENT_WINDOW_FACTORY = "jakarta.faces.lifecycle.ClientWindowFactory"
fld public final static java.lang.String EXCEPTION_HANDLER_FACTORY = "jakarta.faces.context.ExceptionHandlerFactory"
fld public final static java.lang.String EXTERNAL_CONTEXT_FACTORY = "jakarta.faces.context.ExternalContextFactory"
fld public final static java.lang.String FACELET_CACHE_FACTORY = "jakarta.faces.view.facelets.FaceletCacheFactory"
fld public final static java.lang.String FACES_CONTEXT_FACTORY = "jakarta.faces.context.FacesContextFactory"
fld public final static java.lang.String FLASH_FACTORY = "jakarta.faces.context.FlashFactory"
fld public final static java.lang.String FLOW_HANDLER_FACTORY = "jakarta.faces.flow.FlowHandlerFactory"
fld public final static java.lang.String LIFECYCLE_FACTORY = "jakarta.faces.lifecycle.LifecycleFactory"
fld public final static java.lang.String PARTIAL_VIEW_CONTEXT_FACTORY = "jakarta.faces.context.PartialViewContextFactory"
fld public final static java.lang.String RENDER_KIT_FACTORY = "jakarta.faces.render.RenderKitFactory"
fld public final static java.lang.String SEARCH_EXPRESSION_CONTEXT_FACTORY = "jakarta.faces.component.search.SearchExpressionContextFactory"
fld public final static java.lang.String TAG_HANDLER_DELEGATE_FACTORY = "jakarta.faces.view.facelets.TagHandlerDelegateFactory"
fld public final static java.lang.String VIEW_DECLARATION_LANGUAGE_FACTORY = "jakarta.faces.view.ViewDeclarationLanguageFactory"
fld public final static java.lang.String VISIT_CONTEXT_FACTORY = "jakarta.faces.component.visit.VisitContextFactory"
meth public static java.lang.Object getFactory(java.lang.String)
meth public static void releaseFactories()
meth public static void setFactory(java.lang.String,java.lang.String)
supr java.lang.Object
hfds FACTORIES_CACHE

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

