#Signature file v4.1
#Version 2.59

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

CLSS public final org.netbeans.api.sendopts.CommandException
cons public init(int)
cons public init(int,java.lang.String)
meth public int getExitCode()
meth public java.lang.String getLocalizedMessage()
supr java.lang.Exception
hfds exitCode,locMsg

CLSS public final org.netbeans.api.sendopts.CommandLine
meth public !varargs static org.netbeans.api.sendopts.CommandLine create(java.lang.Class<?>[])
meth public !varargs static org.netbeans.api.sendopts.CommandLine create(java.lang.Object[])
meth public !varargs void process(java.lang.String[]) throws org.netbeans.api.sendopts.CommandException
meth public static org.netbeans.api.sendopts.CommandLine getDefault()
meth public void process(java.lang.String[],java.io.InputStream,java.io.OutputStream,java.io.OutputStream,java.io.File) throws org.netbeans.api.sendopts.CommandException
meth public void usage(java.io.PrintWriter)
supr java.lang.Object
hfds ERROR_BASE,processors

CLSS public abstract interface !annotation org.netbeans.spi.sendopts.Arg
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean implicit()
meth public abstract !hasdefault char shortName()
meth public abstract !hasdefault java.lang.String defaultValue()
meth public abstract java.lang.String longName()

CLSS public abstract interface org.netbeans.spi.sendopts.ArgsProcessor
meth public abstract void process(org.netbeans.spi.sendopts.Env) throws org.netbeans.api.sendopts.CommandException

CLSS public abstract interface !annotation org.netbeans.spi.sendopts.Description
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String displayName()
meth public abstract java.lang.String shortDescription()

CLSS public final org.netbeans.spi.sendopts.Env
meth public java.io.File getCurrentDirectory()
meth public java.io.InputStream getInputStream()
meth public java.io.PrintStream getErrorStream()
meth public java.io.PrintStream getOutputStream()
meth public void usage()
meth public void usage(java.io.OutputStream)
supr java.lang.Object
hfds cmd,currentDir,err,is,os

CLSS public final org.netbeans.spi.sendopts.Option
fld public final static char NO_SHORT_NAME = '\uffff'
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public static org.netbeans.spi.sendopts.Option additionalArguments(char,java.lang.String)
meth public static org.netbeans.spi.sendopts.Option always()
meth public static org.netbeans.spi.sendopts.Option defaultArguments()
meth public static org.netbeans.spi.sendopts.Option displayName(org.netbeans.spi.sendopts.Option,java.lang.String,java.lang.String)
meth public static org.netbeans.spi.sendopts.Option optionalArgument(char,java.lang.String)
meth public static org.netbeans.spi.sendopts.Option requiredArgument(char,java.lang.String)
meth public static org.netbeans.spi.sendopts.Option shortDescription(org.netbeans.spi.sendopts.Option,java.lang.String,java.lang.String)
meth public static org.netbeans.spi.sendopts.Option withoutArgument(char,java.lang.String)
supr java.lang.Object
hfds EMPTY,bundles,impl,keys,longName,shortName

CLSS public final org.netbeans.spi.sendopts.OptionGroups
meth public !varargs static org.netbeans.spi.sendopts.Option allOf(org.netbeans.spi.sendopts.Option[])
meth public !varargs static org.netbeans.spi.sendopts.Option anyOf(org.netbeans.spi.sendopts.Option[])
meth public !varargs static org.netbeans.spi.sendopts.Option oneOf(org.netbeans.spi.sendopts.Option[])
meth public !varargs static org.netbeans.spi.sendopts.Option someOf(org.netbeans.spi.sendopts.Option[])
supr java.lang.Object

CLSS public abstract org.netbeans.spi.sendopts.OptionProcessor
cons protected init()
meth protected abstract java.util.Set<org.netbeans.spi.sendopts.Option> getOptions()
meth protected abstract void process(org.netbeans.spi.sendopts.Env,java.util.Map<org.netbeans.spi.sendopts.Option,java.lang.String[]>) throws org.netbeans.api.sendopts.CommandException
supr java.lang.Object

