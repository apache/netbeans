#Signature file v4.1
#Version 1.63

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

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface java.lang.Runnable
 anno 0 java.lang.FunctionalInterface()
meth public abstract void run()

CLSS public java.lang.Thread
cons public init()
cons public init(java.lang.Runnable)
cons public init(java.lang.Runnable,java.lang.String)
cons public init(java.lang.String)
cons public init(java.lang.ThreadGroup,java.lang.Runnable)
cons public init(java.lang.ThreadGroup,java.lang.Runnable,java.lang.String)
cons public init(java.lang.ThreadGroup,java.lang.Runnable,java.lang.String,long)
cons public init(java.lang.ThreadGroup,java.lang.String)
fld public final static int MAX_PRIORITY = 10
fld public final static int MIN_PRIORITY = 1
fld public final static int NORM_PRIORITY = 5
innr public abstract interface static UncaughtExceptionHandler
innr public final static !enum State
intf java.lang.Runnable
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public boolean isInterrupted()
meth public final boolean isAlive()
meth public final boolean isDaemon()
meth public final int getPriority()
meth public final java.lang.String getName()
meth public final java.lang.ThreadGroup getThreadGroup()
meth public final void checkAccess()
meth public final void join() throws java.lang.InterruptedException
meth public final void join(long) throws java.lang.InterruptedException
meth public final void join(long,int) throws java.lang.InterruptedException
meth public final void resume()
 anno 0 java.lang.Deprecated()
meth public final void setDaemon(boolean)
meth public final void setName(java.lang.String)
meth public final void setPriority(int)
meth public final void stop()
 anno 0 java.lang.Deprecated()
meth public final void stop(java.lang.Throwable)
 anno 0 java.lang.Deprecated()
meth public final void suspend()
 anno 0 java.lang.Deprecated()
meth public int countStackFrames()
 anno 0 java.lang.Deprecated()
meth public java.lang.ClassLoader getContextClassLoader()
meth public java.lang.StackTraceElement[] getStackTrace()
meth public java.lang.String toString()
meth public java.lang.Thread$State getState()
meth public java.lang.Thread$UncaughtExceptionHandler getUncaughtExceptionHandler()
meth public long getId()
meth public static boolean holdsLock(java.lang.Object)
meth public static boolean interrupted()
meth public static int activeCount()
meth public static int enumerate(java.lang.Thread[])
meth public static java.lang.Thread currentThread()
meth public static java.lang.Thread$UncaughtExceptionHandler getDefaultUncaughtExceptionHandler()
meth public static java.util.Map<java.lang.Thread,java.lang.StackTraceElement[]> getAllStackTraces()
meth public static void dumpStack()
meth public static void setDefaultUncaughtExceptionHandler(java.lang.Thread$UncaughtExceptionHandler)
meth public static void sleep(long) throws java.lang.InterruptedException
meth public static void sleep(long,int) throws java.lang.InterruptedException
meth public static void yield()
meth public void destroy()
 anno 0 java.lang.Deprecated()
meth public void interrupt()
meth public void run()
meth public void setContextClassLoader(java.lang.ClassLoader)
meth public void setUncaughtExceptionHandler(java.lang.Thread$UncaughtExceptionHandler)
meth public void start()
supr java.lang.Object

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

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory
cons public init()
fld protected final static java.util.logging.Logger LOG
fld public final static java.lang.String JAVAHL_WIN32_MODULE_CODE_NAME = "org.netbeans.libs.svnjavahlwin32"
innr public final static !enum Client
meth protected abstract boolean isAvailable()
meth protected abstract org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory$Client provides()
meth public abstract org.tigris.subversion.svnclientadapter.ISVNClientAdapter createClient()
meth public static org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory getInstance(org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory$Client)
supr java.lang.Object
hfds client,instance

CLSS public final static !enum org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory$Client
 outer org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory
fld public final static org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory$Client JAVAHL
fld public final static org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory$Client SVNKIT
meth public static org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory$Client valueOf(java.lang.String)
meth public static org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory$Client[] values()
supr java.lang.Enum<org.netbeans.libs.svnclientadapter.SvnClientAdapterFactory$Client>

CLSS public abstract org.tigris.subversion.svnclientadapter.AbstractClientAdapter
cons public init()
intf org.tigris.subversion.svnclientadapter.ISVNClientAdapter
meth protected void notImplementedYet() throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public boolean canCommitAcrossWC()
meth public boolean statusReturnsRemoteInfo()
meth public java.util.List getIgnoredPatterns(java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public long[] commitAcrossWC(java.io.File[],java.lang.String,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public org.tigris.subversion.svnclientadapter.ISVNInfo getInfo(org.tigris.subversion.svnclientadapter.SVNUrl) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean,long) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean,long,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(org.tigris.subversion.svnclientadapter.SVNUrl,java.lang.String[],org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean,long) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean,long,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public org.tigris.subversion.svnclientadapter.ISVNProperty propertyGet(org.tigris.subversion.svnclientadapter.SVNUrl,java.lang.String) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public org.tigris.subversion.svnclientadapter.SVNKeywords addKeywords(java.io.File,org.tigris.subversion.svnclientadapter.SVNKeywords) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public org.tigris.subversion.svnclientadapter.SVNKeywords getKeywords(java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public org.tigris.subversion.svnclientadapter.SVNKeywords removeKeywords(java.io.File,org.tigris.subversion.svnclientadapter.SVNKeywords) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public static boolean isOsWindows()
meth public void addPasswordCallback(org.tigris.subversion.svnclientadapter.ISVNPromptUserPassword)
meth public void addToIgnoredPatterns(java.io.File,java.lang.String) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public void createPatch(java.io.File[],java.io.File,java.io.File,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public void diff(java.io.File[],java.io.File,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public void merge(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public void merge(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public void mkdir(org.tigris.subversion.svnclientadapter.SVNUrl,boolean,java.lang.String) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public void setIgnoredPatterns(java.io.File,java.util.List) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public void setKeywords(java.io.File,org.tigris.subversion.svnclientadapter.SVNKeywords,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
supr java.lang.Object

CLSS public org.tigris.subversion.svnclientadapter.Annotations
cons public init()
innr protected static AnnotateInputStream
innr public static Annotation
intf org.tigris.subversion.svnclientadapter.ISVNAnnotations
meth protected org.tigris.subversion.svnclientadapter.Annotations$Annotation getAnnotation(int)
meth public int numberOfLines()
meth public java.io.InputStream getInputStream()
meth public java.lang.String getAuthor(int)
meth public java.lang.String getLine(int)
meth public java.util.Date getChanged(int)
meth public long getRevision(int)
meth public void addAnnotation(org.tigris.subversion.svnclientadapter.Annotations$Annotation)
supr java.lang.Object
hfds annotations

CLSS protected static org.tigris.subversion.svnclientadapter.Annotations$AnnotateInputStream
 outer org.tigris.subversion.svnclientadapter.Annotations
cons public init(org.tigris.subversion.svnclientadapter.ISVNAnnotations)
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public void reset() throws java.io.IOException
supr java.io.InputStream
hfds annotations,available,currentLine,currentLineNumber,currentPos

CLSS public static org.tigris.subversion.svnclientadapter.Annotations$Annotation
 outer org.tigris.subversion.svnclientadapter.Annotations
cons public init(long,java.lang.String,java.util.Date,java.lang.String)
meth public java.lang.String getAuthor()
meth public java.lang.String getLine()
meth public java.lang.String toString()
meth public java.util.Date getChanged()
meth public long getRevision()
meth public void setLine(java.lang.String)
supr java.lang.Object
hfds author,changed,line,revision

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNAnnotations
meth public abstract int numberOfLines()
meth public abstract java.io.InputStream getInputStream()
meth public abstract java.lang.String getAuthor(int)
meth public abstract java.lang.String getLine(int)
meth public abstract java.util.Date getChanged(int)
meth public abstract long getRevision(int)

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNClientAdapter
fld public final static java.lang.String REPOSITORY_FSTYPE_BDB = "bdb"
fld public final static java.lang.String REPOSITORY_FSTYPE_FSFS = "fsfs"
fld public final static java.lang.String[] DEFAULT_LOG_PROPERTIES
meth public abstract boolean canCommitAcrossWC()
meth public abstract boolean isAdminDirectory(java.lang.String)
meth public abstract boolean isThreadsafe()
meth public abstract boolean statusReturnsRemoteInfo()
meth public abstract java.io.InputStream getContent(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract java.io.InputStream getContent(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract java.io.InputStream getContent(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract java.lang.String getAdminDirectoryName()
meth public abstract java.lang.String getPostCommitError()
meth public abstract java.lang.String getRevProperty(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision$Number,java.lang.String) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract java.lang.String[] suggestMergeSources(java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract java.lang.String[] suggestMergeSources(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract java.util.List getIgnoredPatterns(java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract long commit(java.io.File[],java.lang.String,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract long commit(java.io.File[],java.lang.String,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract long update(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract long update(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,int,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract long[] commitAcrossWC(java.io.File[],java.lang.String,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract long[] update(java.io.File[],org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract long[] update(java.io.File[],org.tigris.subversion.svnclientadapter.SVNRevision,int,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNAnnotations annotate(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNAnnotations annotate(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNAnnotations annotate(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNAnnotations annotate(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNAnnotations annotate(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNDirEntry getDirEntry(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNDirEntry getDirEntry(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNDirEntryWithLock[] getListWithLocks(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNDirEntry[] getList(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNDirEntry[] getList(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNDirEntry[] getList(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNDirEntry[] getList(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNInfo getInfo(java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNInfo getInfo(org.tigris.subversion.svnclientadapter.SVNUrl) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNInfo getInfo(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNInfo getInfoFromWorkingCopy(java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNInfo[] getInfo(java.io.File,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean,long) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean,long,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(org.tigris.subversion.svnclientadapter.SVNUrl,java.lang.String[],org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean,long) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean,long,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getMergeinfoLog(int,java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getMergeinfoLog(int,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNMergeInfo getMergeInfo(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNMergeInfo getMergeInfo(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNProperty propertyGet(java.io.File,java.lang.String) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNProperty propertyGet(org.tigris.subversion.svnclientadapter.SVNUrl,java.lang.String) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNProperty propertyGet(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,java.lang.String) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNProperty[] getProperties(java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNProperty[] getProperties(java.io.File,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNProperty[] getProperties(org.tigris.subversion.svnclientadapter.SVNUrl) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNProperty[] getProperties(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNProperty[] getProperties(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNProperty[] getPropertiesIncludingInherited(java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNProperty[] getPropertiesIncludingInherited(java.io.File,boolean,boolean,java.util.List<java.lang.String>) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNProperty[] getPropertiesIncludingInherited(org.tigris.subversion.svnclientadapter.SVNUrl) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNProperty[] getPropertiesIncludingInherited(org.tigris.subversion.svnclientadapter.SVNUrl,boolean,boolean,java.util.List<java.lang.String>) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNProperty[] getRevProperties(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision$Number) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNStatus getSingleStatus(java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNStatus[] getStatus(java.io.File,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNStatus[] getStatus(java.io.File,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNStatus[] getStatus(java.io.File,boolean,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNStatus[] getStatus(java.io.File,boolean,boolean,boolean,boolean,boolean,org.tigris.subversion.svnclientadapter.ISVNStatusCallback) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNStatus[] getStatus(java.io.File,boolean,boolean,boolean,boolean,org.tigris.subversion.svnclientadapter.ISVNStatusCallback) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.ISVNStatus[] getStatus(java.io.File[]) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.SVNDiffSummary[] diffSummarize(java.io.File,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.SVNDiffSummary[] diffSummarize(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,int,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.SVNDiffSummary[] diffSummarize(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,int,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.SVNKeywords addKeywords(java.io.File,org.tigris.subversion.svnclientadapter.SVNKeywords) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.SVNKeywords getKeywords(java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.SVNKeywords removeKeywords(java.io.File,org.tigris.subversion.svnclientadapter.SVNKeywords) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract org.tigris.subversion.svnclientadapter.SVNNotificationHandler getNotificationHandler()
meth public abstract void addConflictResolutionCallback(org.tigris.subversion.svnclientadapter.ISVNConflictResolver)
meth public abstract void addDirectory(java.io.File,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void addDirectory(java.io.File,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void addFile(java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void addNotifyListener(org.tigris.subversion.svnclientadapter.ISVNNotifyListener)
meth public abstract void addPasswordCallback(org.tigris.subversion.svnclientadapter.ISVNPromptUserPassword)
meth public abstract void addToIgnoredPatterns(java.io.File,java.lang.String) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void cancelOperation() throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void checkout(org.tigris.subversion.svnclientadapter.SVNUrl,java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void checkout(org.tigris.subversion.svnclientadapter.SVNUrl,java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,int,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void cleanup(java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void copy(java.io.File,java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void copy(java.io.File,org.tigris.subversion.svnclientadapter.SVNUrl,java.lang.String) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void copy(java.io.File[],org.tigris.subversion.svnclientadapter.SVNUrl,java.lang.String,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void copy(org.tigris.subversion.svnclientadapter.SVNUrl,java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void copy(org.tigris.subversion.svnclientadapter.SVNUrl,java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void copy(org.tigris.subversion.svnclientadapter.SVNUrl,java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void copy(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNUrl,java.lang.String,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void copy(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNUrl,java.lang.String,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void copy(org.tigris.subversion.svnclientadapter.SVNUrl[],org.tigris.subversion.svnclientadapter.SVNUrl,java.lang.String,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void createPatch(java.io.File[],java.io.File,java.io.File,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void createRepository(java.io.File,java.lang.String) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void diff(java.io.File,java.io.File,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void diff(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void diff(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,boolean,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void diff(java.io.File,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void diff(java.io.File[],java.io.File,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void diff(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void diff(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void diff(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,int,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void diff(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void diff(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,boolean,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void dispose()
meth public abstract void doExport(java.io.File,java.io.File,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void doExport(org.tigris.subversion.svnclientadapter.SVNUrl,java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void doImport(java.io.File,org.tigris.subversion.svnclientadapter.SVNUrl,java.lang.String,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void getLogMessages(java.io.File,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean,long,boolean,java.lang.String[],org.tigris.subversion.svnclientadapter.ISVNLogMessageCallback) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void getLogMessages(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,boolean,boolean,long,boolean,java.lang.String[],org.tigris.subversion.svnclientadapter.ISVNLogMessageCallback) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void lock(java.io.File[],java.lang.String,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void lock(org.tigris.subversion.svnclientadapter.SVNUrl[],java.lang.String,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void merge(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevisionRange[],java.io.File,boolean,int,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void merge(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void merge(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void merge(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,boolean,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void merge(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,boolean,int,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void mergeReintegrate(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,java.io.File,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void mkdir(java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void mkdir(org.tigris.subversion.svnclientadapter.SVNUrl,boolean,java.lang.String) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void mkdir(org.tigris.subversion.svnclientadapter.SVNUrl,java.lang.String) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void move(java.io.File,java.io.File,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void move(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNUrl,java.lang.String,org.tigris.subversion.svnclientadapter.SVNRevision) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void propertyDel(java.io.File,java.lang.String,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void propertySet(java.io.File,java.lang.String,java.io.File,boolean) throws java.io.IOException,org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void propertySet(java.io.File,java.lang.String,java.lang.String,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void propertySet(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision$Number,java.lang.String,java.lang.String,java.lang.String) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void relocate(java.lang.String,java.lang.String,java.lang.String,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void remove(java.io.File[],boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void remove(org.tigris.subversion.svnclientadapter.SVNUrl[],java.lang.String) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void removeNotifyListener(org.tigris.subversion.svnclientadapter.ISVNNotifyListener)
meth public abstract void resolve(java.io.File,int) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void resolved(java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void revert(java.io.File,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void setConfigDirectory(java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void setIgnoredPatterns(java.io.File,java.util.List) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void setKeywords(java.io.File,org.tigris.subversion.svnclientadapter.SVNKeywords,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void setPassword(java.lang.String)
meth public abstract void setProgressListener(org.tigris.subversion.svnclientadapter.ISVNProgressListener)
meth public abstract void setRevProperty(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision$Number,java.lang.String,java.lang.String,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void setUsername(java.lang.String)
meth public abstract void switchToUrl(java.io.File,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void switchToUrl(java.io.File,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,int,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void switchToUrl(java.io.File,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,int,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void switchToUrl(java.io.File,org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision,int,boolean,boolean,boolean,boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void unlock(java.io.File[],boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void unlock(org.tigris.subversion.svnclientadapter.SVNUrl[],boolean) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public abstract void upgrade(java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNConflictResolver
innr public final static Choice
meth public abstract org.tigris.subversion.svnclientadapter.SVNConflictResult resolve(org.tigris.subversion.svnclientadapter.SVNConflictDescriptor) throws org.tigris.subversion.svnclientadapter.SVNClientException

CLSS public final static org.tigris.subversion.svnclientadapter.ISVNConflictResolver$Choice
 outer org.tigris.subversion.svnclientadapter.ISVNConflictResolver
cons public init()
fld public final static int chooseBase = 1
fld public final static int chooseMerged = 6
fld public final static int chooseMine = 5
fld public final static int chooseMineFull = 3
fld public final static int chooseTheirs = 4
fld public final static int chooseTheirsFull = 2
fld public final static int postpone = 0
supr java.lang.Object

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNDirEntry
meth public abstract boolean getHasProps()
meth public abstract java.lang.String getLastCommitAuthor()
meth public abstract java.lang.String getPath()
meth public abstract java.util.Date getLastChangedDate()
meth public abstract long getSize()
meth public abstract org.tigris.subversion.svnclientadapter.SVNNodeKind getNodeKind()
meth public abstract org.tigris.subversion.svnclientadapter.SVNRevision$Number getLastChangedRevision()

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNDirEntryWithLock
meth public abstract org.tigris.subversion.svnclientadapter.ISVNDirEntry getDirEntry()
meth public abstract org.tigris.subversion.svnclientadapter.ISVNLock getLock()

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNInfo
meth public abstract boolean isCopied()
meth public abstract int getDepth()
meth public abstract java.io.File getFile()
meth public abstract java.lang.String getLastCommitAuthor()
meth public abstract java.lang.String getLockComment()
meth public abstract java.lang.String getLockOwner()
meth public abstract java.lang.String getUrlString()
meth public abstract java.lang.String getUuid()
meth public abstract java.util.Date getLastChangedDate()
meth public abstract java.util.Date getLastDatePropsUpdate()
meth public abstract java.util.Date getLastDateTextUpdate()
meth public abstract java.util.Date getLockCreationDate()
meth public abstract org.tigris.subversion.svnclientadapter.SVNNodeKind getNodeKind()
meth public abstract org.tigris.subversion.svnclientadapter.SVNRevision$Number getCopyRev()
meth public abstract org.tigris.subversion.svnclientadapter.SVNRevision$Number getLastChangedRevision()
meth public abstract org.tigris.subversion.svnclientadapter.SVNRevision$Number getRevision()
meth public abstract org.tigris.subversion.svnclientadapter.SVNScheduleKind getSchedule()
meth public abstract org.tigris.subversion.svnclientadapter.SVNUrl getCopyUrl()
meth public abstract org.tigris.subversion.svnclientadapter.SVNUrl getRepository()
meth public abstract org.tigris.subversion.svnclientadapter.SVNUrl getUrl()

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNLock
meth public abstract java.lang.String getComment()
meth public abstract java.lang.String getOwner()
meth public abstract java.lang.String getPath()
meth public abstract java.lang.String getToken()
meth public abstract java.util.Date getCreationDate()
meth public abstract java.util.Date getExpirationDate()

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNLogMessage
fld public final static java.lang.String AUTHOR = "svn:author"
fld public final static java.lang.String DATE = "svn:date"
fld public final static java.lang.String MESSAGE = "svn:log"
fld public final static java.lang.String TIME_MICROS = "svnclientadapter:timemicros"
meth public abstract boolean hasChildren()
meth public abstract java.lang.String getAuthor()
meth public abstract java.lang.String getMessage()
meth public abstract java.util.Date getDate()
meth public abstract long getNumberOfChildren()
meth public abstract long getTimeMicros()
meth public abstract long getTimeMillis()
meth public abstract org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath[] getChangedPaths()
meth public abstract org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getChildMessages()
meth public abstract org.tigris.subversion.svnclientadapter.SVNRevision$Number getRevision()
meth public abstract void addChild(org.tigris.subversion.svnclientadapter.ISVNLogMessage)

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNLogMessageCallback
meth public abstract void singleMessage(org.tigris.subversion.svnclientadapter.ISVNLogMessage)

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath
meth public abstract char getAction()
meth public abstract java.lang.String getCopySrcPath()
meth public abstract java.lang.String getPath()
meth public abstract org.tigris.subversion.svnclientadapter.SVNRevision$Number getCopySrcRevision()

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNMergeInfo
meth public abstract java.lang.String[] getPaths()
meth public abstract org.tigris.subversion.svnclientadapter.SVNRevisionRange[] getRevisionRange(java.lang.String)
meth public abstract org.tigris.subversion.svnclientadapter.SVNRevisionRange[] getRevisions(java.lang.String)
meth public abstract void addRevisionRange(java.lang.String,org.tigris.subversion.svnclientadapter.SVNRevisionRange)
meth public abstract void addRevisions(java.lang.String,org.tigris.subversion.svnclientadapter.SVNRevisionRange[])
meth public abstract void loadFromMergeInfoProperty(java.lang.String)

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNMergeinfoLogKind
fld public final static int eligible = 0
fld public final static int merged = 1

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNNotifyListener
innr public final static Command
meth public abstract void logCommandLine(java.lang.String)
meth public abstract void logCompleted(java.lang.String)
meth public abstract void logError(java.lang.String)
meth public abstract void logMessage(java.lang.String)
meth public abstract void logRevision(long,java.lang.String)
meth public abstract void onNotify(java.io.File,org.tigris.subversion.svnclientadapter.SVNNodeKind)
meth public abstract void setCommand(int)

CLSS public final static org.tigris.subversion.svnclientadapter.ISVNNotifyListener$Command
 outer org.tigris.subversion.svnclientadapter.ISVNNotifyListener
cons public init()
fld public final static int ADD = 1
fld public final static int ANNOTATE = 25
fld public final static int CAT = 18
fld public final static int CHECKOUT = 2
fld public final static int CLEANUP = 24
fld public final static int COMMIT = 3
fld public final static int COPY = 6
fld public final static int CREATE_REPOSITORY = 23
fld public final static int DIFF = 17
fld public final static int EXPORT = 8
fld public final static int IMPORT = 9
fld public final static int INFO = 19
fld public final static int LOCK = 28
fld public final static int LOG = 13
fld public final static int LS = 11
fld public final static int MERGE = 27
fld public final static int MERGEINFO = 32
fld public final static int MKDIR = 10
fld public final static int MOVE = 5
fld public final static int PROPDEL = 15
fld public final static int PROPGET = 20
fld public final static int PROPLIST = 21
fld public final static int PROPSET = 14
fld public final static int RELOCATE = 30
fld public final static int REMOVE = 7
fld public final static int RESOLVE = 31
fld public final static int RESOLVED = 22
fld public final static int REVERT = 16
fld public final static int STATUS = 12
fld public final static int SWITCH = 26
fld public final static int UNDEFINED = 0
fld public final static int UNLOCK = 29
fld public final static int UPDATE = 4
fld public final static int UPGRADE = 33
supr java.lang.Object

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNProgressListener
meth public abstract void onProgress(org.tigris.subversion.svnclientadapter.SVNProgressEvent)

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNPromptUserPassword
fld public final static int AcceptPermanently = 2
fld public final static int AcceptTemporary = 1
fld public final static int Reject = 0
meth public abstract boolean askYesNo(java.lang.String,java.lang.String,boolean)
meth public abstract boolean prompt(java.lang.String,java.lang.String,boolean)
meth public abstract boolean promptSSH(java.lang.String,java.lang.String,int,boolean)
meth public abstract boolean promptSSL(java.lang.String,boolean)
meth public abstract boolean promptUser(java.lang.String,java.lang.String,boolean)
meth public abstract boolean userAllowedSave()
meth public abstract int askTrustSSLServer(java.lang.String,boolean)
meth public abstract int getSSHPort()
meth public abstract java.lang.String askQuestion(java.lang.String,java.lang.String,boolean,boolean)
meth public abstract java.lang.String getPassword()
meth public abstract java.lang.String getSSHPrivateKeyPassphrase()
meth public abstract java.lang.String getSSHPrivateKeyPath()
meth public abstract java.lang.String getSSLClientCertPassword()
meth public abstract java.lang.String getSSLClientCertPath()
meth public abstract java.lang.String getUsername()

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNProperty
fld public final static java.lang.String EOL_STYLE = "svn:eol-style"
fld public final static java.lang.String EXECUTABLE = "svn:executable"
fld public final static java.lang.String EXECUTABLE_VALUE = "*"
fld public final static java.lang.String EXTERNALS = "svn:externals"
fld public final static java.lang.String IGNORE = "svn:ignore"
fld public final static java.lang.String KEYWORDS = "svn:keywords"
fld public final static java.lang.String MIME_TYPE = "svn:mime-type"
fld public final static java.lang.String REV_AUTHOR = "svn:author"
fld public final static java.lang.String REV_DATE = "svn:date"
fld public final static java.lang.String REV_LOG = "svn:log"
fld public final static java.lang.String REV_ORIGINAL_DATE = "svn:original-date"
meth public abstract byte[] getData()
meth public abstract java.io.File getFile()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()
meth public abstract org.tigris.subversion.svnclientadapter.SVNUrl getUrl()

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNStatus
meth public abstract boolean hasTreeConflict()
meth public abstract boolean isCopied()
meth public abstract boolean isFileExternal()
meth public abstract boolean isSwitched()
meth public abstract boolean isWcLocked()
meth public abstract java.io.File getConflictNew()
meth public abstract java.io.File getConflictOld()
meth public abstract java.io.File getConflictWorking()
meth public abstract java.io.File getFile()
meth public abstract java.lang.String getLastCommitAuthor()
meth public abstract java.lang.String getLockComment()
meth public abstract java.lang.String getLockOwner()
meth public abstract java.lang.String getMovedFromAbspath()
meth public abstract java.lang.String getMovedToAbspath()
meth public abstract java.lang.String getPath()
meth public abstract java.lang.String getUrlString()
meth public abstract java.util.Date getLastChangedDate()
meth public abstract java.util.Date getLockCreationDate()
meth public abstract org.tigris.subversion.svnclientadapter.SVNConflictDescriptor getConflictDescriptor()
meth public abstract org.tigris.subversion.svnclientadapter.SVNNodeKind getNodeKind()
meth public abstract org.tigris.subversion.svnclientadapter.SVNRevision$Number getLastChangedRevision()
meth public abstract org.tigris.subversion.svnclientadapter.SVNRevision$Number getRevision()
meth public abstract org.tigris.subversion.svnclientadapter.SVNStatusKind getPropStatus()
meth public abstract org.tigris.subversion.svnclientadapter.SVNStatusKind getRepositoryPropStatus()
meth public abstract org.tigris.subversion.svnclientadapter.SVNStatusKind getRepositoryTextStatus()
meth public abstract org.tigris.subversion.svnclientadapter.SVNStatusKind getTextStatus()
meth public abstract org.tigris.subversion.svnclientadapter.SVNUrl getUrl()

CLSS public abstract interface org.tigris.subversion.svnclientadapter.ISVNStatusCallback
meth public abstract void doStatus(java.lang.String,org.tigris.subversion.svnclientadapter.ISVNStatus)

CLSS public org.tigris.subversion.svnclientadapter.SVNBaseDir
cons public init()
meth protected static java.io.File getCommonPart(java.io.File,java.io.File)
meth public static java.io.File getBaseDir(java.io.File)
meth public static java.io.File getBaseDir(java.io.File[])
meth public static java.io.File getRootDir(java.io.File[])
meth public static java.lang.String getRelativePath(java.io.File,java.io.File) throws org.tigris.subversion.svnclientadapter.SVNClientException
supr java.lang.Object

CLSS public abstract org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory
cons public init()
meth protected abstract java.lang.String getClientType()
meth protected abstract org.tigris.subversion.svnclientadapter.ISVNClientAdapter createSVNClientImpl()
meth protected static void registerAdapterFactory(org.tigris.subversion.svnclientadapter.SVNClientAdapterFactory) throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public static boolean isSVNClientAvailable(java.lang.String)
meth public static java.lang.String getPreferredSVNClientType() throws org.tigris.subversion.svnclientadapter.SVNClientException
meth public static org.tigris.subversion.svnclientadapter.ISVNClientAdapter createSVNClient(java.lang.String)
supr java.lang.Object
hfds ourFactoriesMap,preferredFactory

CLSS public org.tigris.subversion.svnclientadapter.SVNClientException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
fld public final static int MERGE_CONFLICT = 155015
fld public final static int NONE = -1
fld public final static int UNSUPPORTED_FEATURE = 200007
fld public final static java.lang.String OPERATION_INTERRUPTED = "operation was interrupted"
meth public boolean operationInterrupted()
meth public int getAprError()
meth public static org.tigris.subversion.svnclientadapter.SVNClientException wrapException(java.lang.Exception)
meth public void setAprError(int)
supr java.lang.Exception
hfds aprError,serialVersionUID

CLSS public org.tigris.subversion.svnclientadapter.SVNConflictDescriptor
cons public init(java.lang.String,int,int,int,org.tigris.subversion.svnclientadapter.SVNConflictVersion,org.tigris.subversion.svnclientadapter.SVNConflictVersion)
cons public init(java.lang.String,int,int,java.lang.String,boolean,java.lang.String,int,int,int,org.tigris.subversion.svnclientadapter.SVNConflictVersion,org.tigris.subversion.svnclientadapter.SVNConflictVersion,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
innr public final Action
innr public final Kind
innr public final Operation
innr public final Reason
meth public boolean isBinary()
meth public boolean isTreeConflict()
meth public int getAction()
meth public int getConflictKind()
meth public int getNodeKind()
meth public int getOperation()
meth public int getReason()
meth public java.lang.String getBasePath()
meth public java.lang.String getMIMEType()
meth public java.lang.String getMergedPath()
meth public java.lang.String getMyPath()
meth public java.lang.String getPath()
meth public java.lang.String getPropertyName()
meth public java.lang.String getTheirPath()
meth public org.tigris.subversion.svnclientadapter.SVNConflictVersion getSrcLeftVersion()
meth public org.tigris.subversion.svnclientadapter.SVNConflictVersion getSrcRightVersion()
supr java.lang.Object
hfds action,basePath,conflictKind,isBinary,mergedPath,mimeType,myPath,nodeKind,operation,path,propertyName,reason,srcLeftVersion,srcRightVersion,theirPath

CLSS public final org.tigris.subversion.svnclientadapter.SVNConflictDescriptor$Action
 outer org.tigris.subversion.svnclientadapter.SVNConflictDescriptor
cons public init(org.tigris.subversion.svnclientadapter.SVNConflictDescriptor)
fld public final static int add = 1
fld public final static int delete = 2
fld public final static int edit = 0
supr java.lang.Object

CLSS public final org.tigris.subversion.svnclientadapter.SVNConflictDescriptor$Kind
 outer org.tigris.subversion.svnclientadapter.SVNConflictDescriptor
cons public init(org.tigris.subversion.svnclientadapter.SVNConflictDescriptor)
fld public final static int property = 1
fld public final static int text = 0
supr java.lang.Object

CLSS public final org.tigris.subversion.svnclientadapter.SVNConflictDescriptor$Operation
 outer org.tigris.subversion.svnclientadapter.SVNConflictDescriptor
cons public init(org.tigris.subversion.svnclientadapter.SVNConflictDescriptor)
fld public final static int _merge = 3
fld public final static int _none = 0
fld public final static int _switch = 2
fld public final static int _update = 1
supr java.lang.Object

CLSS public final org.tigris.subversion.svnclientadapter.SVNConflictDescriptor$Reason
 outer org.tigris.subversion.svnclientadapter.SVNConflictDescriptor
cons public init(org.tigris.subversion.svnclientadapter.SVNConflictDescriptor)
fld public final static int added = 5
fld public final static int deleted = 2
fld public final static int edited = 0
fld public final static int missing = 3
fld public final static int moved_away = 7
fld public final static int moved_here = 8
fld public final static int obstructed = 1
fld public final static int replaced = 6
fld public final static int unversioned = 4
supr java.lang.Object

CLSS public org.tigris.subversion.svnclientadapter.SVNConflictResult
cons public init(int,java.lang.String)
fld public final static int chooseBase = 1
fld public final static int chooseMerged = 6
fld public final static int chooseMine = 5
fld public final static int chooseMineFull = 3
fld public final static int chooseTheirs = 4
fld public final static int chooseTheirsFull = 2
fld public final static int postpone = 0
meth public int getChoice()
meth public java.lang.String getMergedPath()
supr java.lang.Object
hfds choice,mergedPath

CLSS public org.tigris.subversion.svnclientadapter.SVNConflictVersion
cons public init(java.lang.String,long,java.lang.String,int)
innr public final NodeKind
meth public int getNodeKind()
meth public java.lang.String getPathInRepos()
meth public java.lang.String getReposURL()
meth public java.lang.String toString()
meth public long getPegRevision()
supr java.lang.Object
hfds nodeKind,pathInRepos,pegRevision,reposURL

CLSS public final org.tigris.subversion.svnclientadapter.SVNConflictVersion$NodeKind
 outer org.tigris.subversion.svnclientadapter.SVNConflictVersion
cons public init(org.tigris.subversion.svnclientadapter.SVNConflictVersion)
fld public final static int directory = 2
fld public final static int file = 1
fld public final static int none = 0
supr java.lang.Object

CLSS public org.tigris.subversion.svnclientadapter.SVNConstants
cons public init()
fld public final static java.lang.String SVN_DIRPROPS = "dir-props"
fld public final static java.lang.String SVN_ENTRIES = "wc.db"
fld public final static java.lang.String SVN_PROPS = "props"
supr java.lang.Object

CLSS public org.tigris.subversion.svnclientadapter.SVNCopySource
cons public init(java.lang.String,org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision)
meth public java.lang.String getPath()
meth public org.tigris.subversion.svnclientadapter.SVNRevision getPegRevision()
meth public org.tigris.subversion.svnclientadapter.SVNRevision getRevision()
supr java.lang.Object
hfds path,pegRevision,revision

CLSS public org.tigris.subversion.svnclientadapter.SVNDiffSummary
cons public init(java.lang.String,org.tigris.subversion.svnclientadapter.SVNDiffSummary$SVNDiffKind,boolean,int)
innr public static SVNDiffKind
meth public boolean propsChanged()
meth public int getNodeKind()
meth public java.lang.String getPath()
meth public java.lang.String toString()
meth public org.tigris.subversion.svnclientadapter.SVNDiffSummary$SVNDiffKind getDiffKind()
supr java.util.EventObject
hfds diffKind,nodeKind,propsChanged,serialVersionUID

CLSS public static org.tigris.subversion.svnclientadapter.SVNDiffSummary$SVNDiffKind
 outer org.tigris.subversion.svnclientadapter.SVNDiffSummary
fld public static org.tigris.subversion.svnclientadapter.SVNDiffSummary$SVNDiffKind ADDED
fld public static org.tigris.subversion.svnclientadapter.SVNDiffSummary$SVNDiffKind DELETED
fld public static org.tigris.subversion.svnclientadapter.SVNDiffSummary$SVNDiffKind MODIFIED
fld public static org.tigris.subversion.svnclientadapter.SVNDiffSummary$SVNDiffKind NORMAL
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public static org.tigris.subversion.svnclientadapter.SVNDiffSummary$SVNDiffKind getInstance(int)
supr java.lang.Object
hfds kind

CLSS public org.tigris.subversion.svnclientadapter.SVNInfoUnversioned
cons public init(java.io.File)
intf org.tigris.subversion.svnclientadapter.ISVNInfo
meth public boolean isCopied()
meth public int getDepth()
meth public java.io.File getFile()
meth public java.lang.String getLastCommitAuthor()
meth public java.lang.String getLockComment()
meth public java.lang.String getLockOwner()
meth public java.lang.String getUrlString()
meth public java.lang.String getUuid()
meth public java.util.Date getLastChangedDate()
meth public java.util.Date getLastDatePropsUpdate()
meth public java.util.Date getLastDateTextUpdate()
meth public java.util.Date getLockCreationDate()
meth public org.tigris.subversion.svnclientadapter.SVNNodeKind getNodeKind()
meth public org.tigris.subversion.svnclientadapter.SVNRevision$Number getCopyRev()
meth public org.tigris.subversion.svnclientadapter.SVNRevision$Number getLastChangedRevision()
meth public org.tigris.subversion.svnclientadapter.SVNRevision$Number getRevision()
meth public org.tigris.subversion.svnclientadapter.SVNScheduleKind getSchedule()
meth public org.tigris.subversion.svnclientadapter.SVNUrl getCopyUrl()
meth public org.tigris.subversion.svnclientadapter.SVNUrl getRepository()
meth public org.tigris.subversion.svnclientadapter.SVNUrl getUrl()
supr java.lang.Object
hfds file

CLSS public org.tigris.subversion.svnclientadapter.SVNKeywords
cons public init()
cons public init(boolean,boolean,boolean,boolean,boolean)
cons public init(java.lang.String)
fld public final static java.lang.String AUTHOR = "Author"
fld public final static java.lang.String DATE = "Date"
fld public final static java.lang.String HEAD_URL = "HeadURL"
fld public final static java.lang.String ID = "Id"
fld public final static java.lang.String LAST_CHANGED_BY = "LastChangedBy"
fld public final static java.lang.String LAST_CHANGED_DATE = "LastChangedDate"
fld public final static java.lang.String LAST_CHANGED_REVISION = "LastChangedRevision"
fld public final static java.lang.String REV = "Rev"
fld public final static java.lang.String URL = "URL"
meth public boolean isHeadUrl()
meth public boolean isId()
meth public boolean isLastChangedBy()
meth public boolean isLastChangedDate()
meth public boolean isLastChangedRevision()
meth public java.lang.String toString()
meth public java.util.List getKeywordsList()
meth public void setHeadUrl(boolean)
meth public void setId(boolean)
meth public void setLastChangedBy(boolean)
meth public void setLastChangedDate(boolean)
meth public void setLastChangedRevision(boolean)
supr java.lang.Object
hfds headUrl,id,lastChangedBy,lastChangedDate,lastChangedRevision

CLSS public org.tigris.subversion.svnclientadapter.SVNLogMessageCallback
cons public init()
intf org.tigris.subversion.svnclientadapter.ISVNLogMessageCallback
meth public org.tigris.subversion.svnclientadapter.ISVNLogMessage[] getLogMessages()
meth public void singleMessage(org.tigris.subversion.svnclientadapter.ISVNLogMessage)
supr java.lang.Object
hfds messages,stack

CLSS public org.tigris.subversion.svnclientadapter.SVNLogMessageChangePath
cons public init(java.lang.String,org.tigris.subversion.svnclientadapter.SVNRevision$Number,java.lang.String,char)
intf org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath
meth public char getAction()
meth public java.lang.String getCopySrcPath()
meth public java.lang.String getPath()
meth public java.lang.String toString()
meth public org.tigris.subversion.svnclientadapter.SVNRevision$Number getCopySrcRevision()
supr java.lang.Object
hfds action,copySrcPath,copySrcRevision,path

CLSS public org.tigris.subversion.svnclientadapter.SVNNodeKind
fld public final static org.tigris.subversion.svnclientadapter.SVNNodeKind DIR
fld public final static org.tigris.subversion.svnclientadapter.SVNNodeKind FILE
fld public final static org.tigris.subversion.svnclientadapter.SVNNodeKind NONE
fld public final static org.tigris.subversion.svnclientadapter.SVNNodeKind UNKNOWN
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public int toInt()
meth public java.lang.String toString()
meth public static org.tigris.subversion.svnclientadapter.SVNNodeKind fromInt(int)
meth public static org.tigris.subversion.svnclientadapter.SVNNodeKind fromString(java.lang.String)
supr java.lang.Object
hfds dir,file,kind,none,unknown

CLSS public abstract org.tigris.subversion.svnclientadapter.SVNNotificationHandler
cons public init()
fld protected boolean logEnabled
fld protected int command
fld protected java.io.File baseDir
fld protected java.util.Set notifylisteners
meth protected boolean skipCommand()
meth public void add(org.tigris.subversion.svnclientadapter.ISVNNotifyListener)
meth public void disableLog()
meth public void enableLog()
meth public void logCommandLine(java.lang.String)
meth public void logCompleted(java.lang.String)
meth public void logError(java.lang.String)
meth public void logException(java.lang.Exception)
meth public void logMessage(java.lang.String)
meth public void logRevision(long,java.lang.String)
meth public void notifyListenersOfChange(java.lang.String)
meth public void notifyListenersOfChange(java.lang.String,org.tigris.subversion.svnclientadapter.SVNNodeKind)
meth public void remove(org.tigris.subversion.svnclientadapter.ISVNNotifyListener)
meth public void setBaseDir()
meth public void setBaseDir(java.io.File)
meth public void setCommand(int)
supr java.lang.Object

CLSS public org.tigris.subversion.svnclientadapter.SVNProgressEvent
cons public init(long,long)
fld public final static long UNKNOWN = -1
meth public long getProgress()
meth public long getTotal()
supr java.lang.Object
hfds progress,total

CLSS public org.tigris.subversion.svnclientadapter.SVNRevision
cons public init(int)
fld protected final static org.tigris.subversion.svnclientadapter.utils.SafeSimpleDateFormat dateFormat
fld protected int revKind
fld public final static int SVN_INVALID_REVNUM = -1
fld public final static org.tigris.subversion.svnclientadapter.SVNRevision BASE
fld public final static org.tigris.subversion.svnclientadapter.SVNRevision COMMITTED
fld public final static org.tigris.subversion.svnclientadapter.SVNRevision HEAD
fld public final static org.tigris.subversion.svnclientadapter.SVNRevision PREVIOUS
fld public final static org.tigris.subversion.svnclientadapter.SVNRevision START
fld public final static org.tigris.subversion.svnclientadapter.SVNRevision WORKING
fld public final static org.tigris.subversion.svnclientadapter.SVNRevision$Number INVALID_REVISION
innr public final static Kind
innr public static DateSpec
innr public static Number
meth public boolean equals(java.lang.Object)
meth public int getKind()
meth public int hashCode()
meth public java.lang.String toString()
meth public static org.tigris.subversion.svnclientadapter.SVNRevision getRevision(java.lang.String) throws java.text.ParseException
meth public static org.tigris.subversion.svnclientadapter.SVNRevision getRevision(java.lang.String,java.text.SimpleDateFormat) throws java.text.ParseException
supr java.lang.Object

CLSS public static org.tigris.subversion.svnclientadapter.SVNRevision$DateSpec
 outer org.tigris.subversion.svnclientadapter.SVNRevision
cons public init(java.util.Date)
fld protected java.util.Date revDate
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.Date getDate()
supr org.tigris.subversion.svnclientadapter.SVNRevision

CLSS public final static org.tigris.subversion.svnclientadapter.SVNRevision$Kind
 outer org.tigris.subversion.svnclientadapter.SVNRevision
cons public init()
fld public final static int base = 5
fld public final static int committed = 3
fld public final static int date = 2
fld public final static int head = 7
fld public final static int number = 1
fld public final static int previous = 4
fld public final static int unspecified = 0
fld public final static int working = 6
supr java.lang.Object

CLSS public static org.tigris.subversion.svnclientadapter.SVNRevision$Number
 outer org.tigris.subversion.svnclientadapter.SVNRevision
cons public init(long)
fld protected long revNumber
intf java.lang.Comparable
meth public boolean equals(java.lang.Object)
meth public int compareTo(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public long getNumber()
supr org.tigris.subversion.svnclientadapter.SVNRevision

CLSS public org.tigris.subversion.svnclientadapter.SVNRevisionRange
cons public init(java.lang.String)
cons public init(org.tigris.subversion.svnclientadapter.SVNRevision$Number,org.tigris.subversion.svnclientadapter.SVNRevision$Number,boolean)
cons public init(org.tigris.subversion.svnclientadapter.SVNRevision,org.tigris.subversion.svnclientadapter.SVNRevision)
intf java.io.Serializable
intf java.lang.Comparable
meth public boolean contains(org.tigris.subversion.svnclientadapter.SVNRevision,boolean)
meth public boolean equals(java.lang.Object)
meth public int compareTo(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toMergeString()
meth public java.lang.String toString()
meth public org.tigris.subversion.svnclientadapter.SVNRevision getFromRevision()
meth public org.tigris.subversion.svnclientadapter.SVNRevision getToRevision()
meth public static java.lang.Long getRevisionAsLong(org.tigris.subversion.svnclientadapter.SVNRevision)
meth public static org.tigris.subversion.svnclientadapter.SVNRevisionRange[] getRevisions(org.tigris.subversion.svnclientadapter.SVNRevision$Number[],org.tigris.subversion.svnclientadapter.SVNRevision$Number[])
supr java.lang.Object
hfds from,serialVersionUID,to

CLSS public org.tigris.subversion.svnclientadapter.SVNScheduleKind
fld public final static org.tigris.subversion.svnclientadapter.SVNScheduleKind ADD
fld public final static org.tigris.subversion.svnclientadapter.SVNScheduleKind DELETE
fld public final static org.tigris.subversion.svnclientadapter.SVNScheduleKind NORMAL
fld public final static org.tigris.subversion.svnclientadapter.SVNScheduleKind REPLACE
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public int toInt()
meth public java.lang.String toString()
meth public org.tigris.subversion.svnclientadapter.SVNScheduleKind fromInt(int)
meth public static org.tigris.subversion.svnclientadapter.SVNScheduleKind fromString(java.lang.String)
supr java.lang.Object
hfds add,delete,kind,normal,replace

CLSS public org.tigris.subversion.svnclientadapter.SVNStatusCallback
cons public init()
intf org.tigris.subversion.svnclientadapter.ISVNStatusCallback
meth public org.tigris.subversion.svnclientadapter.ISVNStatus[] getStatuses()
meth public void doStatus(java.lang.String,org.tigris.subversion.svnclientadapter.ISVNStatus)
supr java.lang.Object
hfds statuses

CLSS public org.tigris.subversion.svnclientadapter.SVNStatusKind
fld public final static org.tigris.subversion.svnclientadapter.SVNStatusKind ADDED
fld public final static org.tigris.subversion.svnclientadapter.SVNStatusKind CONFLICTED
fld public final static org.tigris.subversion.svnclientadapter.SVNStatusKind DELETED
fld public final static org.tigris.subversion.svnclientadapter.SVNStatusKind EXTERNAL
fld public final static org.tigris.subversion.svnclientadapter.SVNStatusKind IGNORED
fld public final static org.tigris.subversion.svnclientadapter.SVNStatusKind INCOMPLETE
fld public final static org.tigris.subversion.svnclientadapter.SVNStatusKind MERGED
fld public final static org.tigris.subversion.svnclientadapter.SVNStatusKind MISSING
fld public final static org.tigris.subversion.svnclientadapter.SVNStatusKind MODIFIED
fld public final static org.tigris.subversion.svnclientadapter.SVNStatusKind NONE
fld public final static org.tigris.subversion.svnclientadapter.SVNStatusKind NORMAL
fld public final static org.tigris.subversion.svnclientadapter.SVNStatusKind OBSTRUCTED
fld public final static org.tigris.subversion.svnclientadapter.SVNStatusKind REPLACED
fld public final static org.tigris.subversion.svnclientadapter.SVNStatusKind UNVERSIONED
meth public boolean equals(java.lang.Object)
meth public boolean hasTreeConflict()
meth public int hashCode()
meth public int toInt()
meth public java.lang.String toString()
meth public static org.tigris.subversion.svnclientadapter.SVNStatusKind fromInt(int)
meth public static org.tigris.subversion.svnclientadapter.SVNStatusKind fromString(java.lang.String)
meth public void setTreeConflicted(boolean)
supr java.lang.Object
hfds added,conflicted,deleted,external,ignored,incomplete,kind,merged,missing,modified,none,normal,obstructed,replaced,treeConflicted,unversioned

CLSS public org.tigris.subversion.svnclientadapter.SVNStatusUnversioned
cons public init(java.io.File)
cons public init(java.io.File,boolean)
intf org.tigris.subversion.svnclientadapter.ISVNStatus
meth public boolean hasTreeConflict()
meth public boolean isCopied()
meth public boolean isFileExternal()
meth public boolean isSwitched()
meth public boolean isWcLocked()
meth public java.io.File getConflictNew()
meth public java.io.File getConflictOld()
meth public java.io.File getConflictWorking()
meth public java.io.File getFile()
meth public java.lang.String getLastCommitAuthor()
meth public java.lang.String getLockComment()
meth public java.lang.String getLockOwner()
meth public java.lang.String getMovedFromAbspath()
meth public java.lang.String getMovedToAbspath()
meth public java.lang.String getPath()
meth public java.lang.String getUrlString()
meth public java.util.Date getLastChangedDate()
meth public java.util.Date getLockCreationDate()
meth public org.tigris.subversion.svnclientadapter.SVNConflictDescriptor getConflictDescriptor()
meth public org.tigris.subversion.svnclientadapter.SVNNodeKind getNodeKind()
meth public org.tigris.subversion.svnclientadapter.SVNRevision$Number getLastChangedRevision()
meth public org.tigris.subversion.svnclientadapter.SVNRevision$Number getRevision()
meth public org.tigris.subversion.svnclientadapter.SVNStatusKind getPropStatus()
meth public org.tigris.subversion.svnclientadapter.SVNStatusKind getRepositoryPropStatus()
meth public org.tigris.subversion.svnclientadapter.SVNStatusKind getRepositoryTextStatus()
meth public org.tigris.subversion.svnclientadapter.SVNStatusKind getTextStatus()
meth public org.tigris.subversion.svnclientadapter.SVNUrl getUrl()
supr java.lang.Object
hfds file,isIgnored

CLSS public org.tigris.subversion.svnclientadapter.SVNUrl
cons public init(java.lang.String) throws java.net.MalformedURLException
fld protected final static char SEGMENT_SEPARATOR = '/'
meth public boolean equals(java.lang.Object)
meth public int getPort()
meth public int hashCode()
meth public java.lang.String getHost()
meth public java.lang.String getLastPathSegment()
meth public java.lang.String getProtocol()
meth public java.lang.String toString()
meth public java.lang.String[] getPathSegments()
meth public org.tigris.subversion.svnclientadapter.SVNUrl appendPath(java.lang.String)
meth public org.tigris.subversion.svnclientadapter.SVNUrl getParent()
meth public static int getDefaultPort(java.lang.String)
supr java.lang.Object
hfds FILE_PROTOCOL,HTTPS_PROTOCOL,HTTP_PROTOCOL,SVNSSH_PROTOCOL,SVN_PROTOCOL,host,port,protocol,segments

CLSS public org.tigris.subversion.svnclientadapter.utils.Command
cons public init(java.lang.String)
meth public int waitFor() throws java.lang.InterruptedException
meth public java.lang.Process getProcess()
meth public void exec() throws java.io.IOException
meth public void kill()
meth public void setErr(java.io.OutputStream)
meth public void setOut(java.io.OutputStream)
meth public void setParameters(java.lang.String[])
supr java.lang.Object
hfds command,err,out,parameters,process

CLSS public org.tigris.subversion.svnclientadapter.utils.Depth
cons public init()
fld public final static int empty = 2
fld public final static int exclude = 1
fld public final static int files = 3
fld public final static int immediates = 4
fld public final static int infinity = 5
fld public final static int unknown = 0
meth public final static int fromRecurse(boolean)
supr java.lang.Object

CLSS public org.tigris.subversion.svnclientadapter.utils.Messages
cons public init()
fld protected static java.util.ResourceBundle bundle
meth public static java.lang.String bind(java.lang.String)
meth public static java.lang.String bind(java.lang.String,java.lang.Object[])
meth public static java.lang.String bind(java.lang.String,java.lang.String)
meth public static java.lang.String bind(java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object
hfds BUNDLE_NAME

CLSS public org.tigris.subversion.svnclientadapter.utils.ReaderThread
cons public init(java.io.InputStream,java.io.OutputStream)
meth public void run()
supr java.lang.Thread
hfds myInputStream,myOutputStream

CLSS public org.tigris.subversion.svnclientadapter.utils.SVNStatusUtils
cons public init()
meth public static boolean hasRemote(org.tigris.subversion.svnclientadapter.ISVNStatus)
meth public static boolean isAdded(org.tigris.subversion.svnclientadapter.ISVNStatus)
meth public static boolean isDeleted(org.tigris.subversion.svnclientadapter.ISVNStatus)
meth public static boolean isIgnored(org.tigris.subversion.svnclientadapter.ISVNStatus)
meth public static boolean isManaged(org.tigris.subversion.svnclientadapter.ISVNStatus)
meth public static boolean isManaged(org.tigris.subversion.svnclientadapter.SVNStatusKind)
meth public static boolean isMissing(org.tigris.subversion.svnclientadapter.ISVNStatus)
meth public static boolean isPropConflicted(org.tigris.subversion.svnclientadapter.ISVNStatus)
meth public static boolean isPropModified(org.tigris.subversion.svnclientadapter.ISVNStatus)
meth public static boolean isReadyForCommit(org.tigris.subversion.svnclientadapter.ISVNStatus)
meth public static boolean isReadyForRevert(org.tigris.subversion.svnclientadapter.ISVNStatus)
meth public static boolean isReplaced(org.tigris.subversion.svnclientadapter.ISVNStatus)
meth public static boolean isTextConflicted(org.tigris.subversion.svnclientadapter.ISVNStatus)
meth public static boolean isTextMerged(org.tigris.subversion.svnclientadapter.ISVNStatus)
meth public static boolean isTextModified(org.tigris.subversion.svnclientadapter.ISVNStatus)
supr java.lang.Object

CLSS public org.tigris.subversion.svnclientadapter.utils.SVNUrlUtils
cons public init()
meth public static java.lang.String getRelativePath(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNUrl)
meth public static java.lang.String getRelativePath(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNUrl,boolean)
meth public static org.tigris.subversion.svnclientadapter.SVNUrl getCommonRootUrl(org.tigris.subversion.svnclientadapter.SVNUrl,org.tigris.subversion.svnclientadapter.SVNUrl)
meth public static org.tigris.subversion.svnclientadapter.SVNUrl getCommonRootUrl(org.tigris.subversion.svnclientadapter.SVNUrl[])
meth public static org.tigris.subversion.svnclientadapter.SVNUrl getUrlFromLocalFileName(java.lang.String,java.lang.String,java.lang.String)
meth public static org.tigris.subversion.svnclientadapter.SVNUrl getUrlFromLocalFileName(java.lang.String,org.tigris.subversion.svnclientadapter.SVNUrl,java.lang.String)
supr java.lang.Object

CLSS public org.tigris.subversion.svnclientadapter.utils.SafeSimpleDateFormat
cons public init(java.lang.String)
meth public java.lang.String format(java.lang.Object)
meth public java.lang.String format(java.util.Date)
meth public java.util.Date parse(java.lang.String) throws java.text.ParseException
meth public void set2DigitYearStart(java.util.Date)
meth public void setCalendar(java.util.Calendar)
meth public void setDateFormatSymbols(java.text.DateFormatSymbols)
meth public void setLenient(boolean)
meth public void setNumberFormat(java.text.NumberFormat)
meth public void setTimeZone(java.util.TimeZone)
supr java.lang.Object
hfds _dateFormats,_format

CLSS public org.tigris.subversion.svnclientadapter.utils.StringUtils
cons public init()
meth public static java.lang.String stripStart(java.lang.String,java.lang.String)
meth public static java.lang.String[] split(java.lang.String,char)
meth public static java.lang.String[] split(java.lang.String,java.lang.String)
supr java.lang.Object

