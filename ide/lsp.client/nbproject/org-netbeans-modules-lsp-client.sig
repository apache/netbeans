#Signature file v4.1
#Version 1.31.0

CLSS public java.lang.Object
cons public init()
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth protected void finalize() throws java.lang.Throwable
 anno 0 java.lang.Deprecated(boolean forRemoval=false, java.lang.String since="9")
meth public boolean equals(java.lang.Object)
meth public final java.lang.Class<?> getClass()
meth public final void notify()
meth public final void notifyAll()
meth public final void wait() throws java.lang.InterruptedException
meth public final void wait(long) throws java.lang.InterruptedException
meth public final void wait(long,int) throws java.lang.InterruptedException
meth public int hashCode()
meth public java.lang.String toString()

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

CLSS public org.netbeans.modules.lsp.client.debugger.api.DAPConfiguration
meth public org.netbeans.modules.lsp.client.debugger.api.DAPConfiguration addConfiguration(java.util.Map<java.lang.String,java.lang.Object>)
meth public org.netbeans.modules.lsp.client.debugger.api.DAPConfiguration delayLaunch()
meth public org.netbeans.modules.lsp.client.debugger.api.DAPConfiguration setSessionName(java.lang.String)
meth public static org.netbeans.modules.lsp.client.debugger.api.DAPConfiguration create(java.io.InputStream,java.io.OutputStream)
meth public void attach()
meth public void launch()
supr java.lang.Object
hfds configuration,delayLaunch,in,out,sessionName

CLSS public final org.netbeans.modules.lsp.client.debugger.api.RegisterDAPBreakpoints
meth public static org.netbeans.modules.lsp.client.debugger.api.RegisterDAPBreakpoints newInstance()
supr java.lang.Object

CLSS public abstract interface !annotation org.netbeans.modules.lsp.client.debugger.api.RegisterDAPDebugger
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String[] mimeType()

CLSS public abstract interface org.netbeans.modules.lsp.client.debugger.spi.BreakpointConvertor
innr public static ConvertedBreakpointConsumer
meth public abstract void convert(org.netbeans.api.debugger.Breakpoint,org.netbeans.modules.lsp.client.debugger.spi.BreakpointConvertor$ConvertedBreakpointConsumer)

CLSS public static org.netbeans.modules.lsp.client.debugger.spi.BreakpointConvertor$ConvertedBreakpointConsumer
 outer org.netbeans.modules.lsp.client.debugger.spi.BreakpointConvertor
meth public void lineBreakpoint(java.net.URI,int,java.lang.String)
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds lineBreakpoints

CLSS public abstract interface org.netbeans.modules.lsp.client.spi.LanguageServerProvider
innr public final static LanguageServerDescription
meth public abstract org.netbeans.modules.lsp.client.spi.LanguageServerProvider$LanguageServerDescription startServer(org.openide.util.Lookup)
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()

CLSS public final static org.netbeans.modules.lsp.client.spi.LanguageServerProvider$LanguageServerDescription
 outer org.netbeans.modules.lsp.client.spi.LanguageServerProvider
meth public static org.netbeans.modules.lsp.client.spi.LanguageServerProvider$LanguageServerDescription create(java.io.InputStream,java.io.OutputStream,java.lang.Process)
 anno 0 org.netbeans.api.annotations.common.NonNull()
 anno 1 org.netbeans.api.annotations.common.NonNull()
 anno 2 org.netbeans.api.annotations.common.NonNull()
 anno 3 org.netbeans.api.annotations.common.NullAllowed()
supr java.lang.Object
hfds bindings,in,out,process,server

CLSS public abstract interface org.netbeans.modules.lsp.client.spi.MultiMimeLanguageServerProvider
intf org.netbeans.modules.lsp.client.spi.LanguageServerProvider
meth public abstract java.util.Set<java.lang.String> getMimeTypes()

CLSS public abstract interface org.netbeans.modules.lsp.client.spi.ServerRestarter
meth public abstract void restart()

