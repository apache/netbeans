#Signature file v4.1
#Version 2.17.1

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

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

CLSS public abstract interface java.lang.Iterable<%0 extends java.lang.Object>
meth public abstract java.util.Iterator<{java.lang.Iterable%0}> iterator()
meth public java.util.Spliterator<{java.lang.Iterable%0}> spliterator()
meth public void forEach(java.util.function.Consumer<? super {java.lang.Iterable%0}>)

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

CLSS public abstract interface java.nio.channels.ByteChannel
intf java.nio.channels.ReadableByteChannel
intf java.nio.channels.WritableByteChannel

CLSS public abstract interface java.nio.channels.Channel
intf java.io.Closeable
meth public abstract boolean isOpen()
meth public abstract void close() throws java.io.IOException

CLSS public abstract java.nio.channels.FileChannel
cons protected init()
innr public static MapMode
intf java.nio.channels.GatheringByteChannel
intf java.nio.channels.ScatteringByteChannel
intf java.nio.channels.SeekableByteChannel
meth public !varargs static java.nio.channels.FileChannel open(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs static java.nio.channels.FileChannel open(java.nio.file.Path,java.util.Set<? extends java.nio.file.OpenOption>,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public abstract int read(java.nio.ByteBuffer) throws java.io.IOException
meth public abstract int read(java.nio.ByteBuffer,long) throws java.io.IOException
meth public abstract int write(java.nio.ByteBuffer) throws java.io.IOException
meth public abstract int write(java.nio.ByteBuffer,long) throws java.io.IOException
meth public abstract java.nio.MappedByteBuffer map(java.nio.channels.FileChannel$MapMode,long,long) throws java.io.IOException
meth public abstract java.nio.channels.FileChannel position(long) throws java.io.IOException
meth public abstract java.nio.channels.FileChannel truncate(long) throws java.io.IOException
meth public abstract java.nio.channels.FileLock lock(long,long,boolean) throws java.io.IOException
meth public abstract java.nio.channels.FileLock tryLock(long,long,boolean) throws java.io.IOException
meth public abstract long position() throws java.io.IOException
meth public abstract long read(java.nio.ByteBuffer[],int,int) throws java.io.IOException
meth public abstract long size() throws java.io.IOException
meth public abstract long transferFrom(java.nio.channels.ReadableByteChannel,long,long) throws java.io.IOException
meth public abstract long transferTo(long,long,java.nio.channels.WritableByteChannel) throws java.io.IOException
meth public abstract long write(java.nio.ByteBuffer[],int,int) throws java.io.IOException
meth public abstract void force(boolean) throws java.io.IOException
meth public final java.nio.channels.FileLock lock() throws java.io.IOException
meth public final java.nio.channels.FileLock tryLock() throws java.io.IOException
meth public final long read(java.nio.ByteBuffer[]) throws java.io.IOException
meth public final long write(java.nio.ByteBuffer[]) throws java.io.IOException
supr java.nio.channels.spi.AbstractInterruptibleChannel

CLSS public abstract interface java.nio.channels.GatheringByteChannel
intf java.nio.channels.WritableByteChannel
meth public abstract long write(java.nio.ByteBuffer[]) throws java.io.IOException
meth public abstract long write(java.nio.ByteBuffer[],int,int) throws java.io.IOException

CLSS public abstract interface java.nio.channels.InterruptibleChannel
intf java.nio.channels.Channel
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.nio.channels.ReadableByteChannel
intf java.nio.channels.Channel
meth public abstract int read(java.nio.ByteBuffer) throws java.io.IOException

CLSS public abstract interface java.nio.channels.ScatteringByteChannel
intf java.nio.channels.ReadableByteChannel
meth public abstract long read(java.nio.ByteBuffer[]) throws java.io.IOException
meth public abstract long read(java.nio.ByteBuffer[],int,int) throws java.io.IOException

CLSS public abstract interface java.nio.channels.SeekableByteChannel
intf java.nio.channels.ByteChannel
meth public abstract int read(java.nio.ByteBuffer) throws java.io.IOException
meth public abstract int write(java.nio.ByteBuffer) throws java.io.IOException
meth public abstract java.nio.channels.SeekableByteChannel position(long) throws java.io.IOException
meth public abstract java.nio.channels.SeekableByteChannel truncate(long) throws java.io.IOException
meth public abstract long position() throws java.io.IOException
meth public abstract long size() throws java.io.IOException

CLSS public abstract interface java.nio.channels.WritableByteChannel
intf java.nio.channels.Channel
meth public abstract int write(java.nio.ByteBuffer) throws java.io.IOException

CLSS public abstract java.nio.channels.spi.AbstractInterruptibleChannel
cons protected init()
intf java.nio.channels.Channel
intf java.nio.channels.InterruptibleChannel
meth protected abstract void implCloseChannel() throws java.io.IOException
meth protected final void begin()
meth protected final void end(boolean) throws java.nio.channels.AsynchronousCloseException
meth public final boolean isOpen()
meth public final void close() throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.nio.file.DirectoryStream<%0 extends java.lang.Object>
innr public abstract interface static Filter
intf java.io.Closeable
intf java.lang.Iterable<{java.nio.file.DirectoryStream%0}>
meth public abstract java.util.Iterator<{java.nio.file.DirectoryStream%0}> iterator()

CLSS public abstract java.nio.file.FileStore
cons protected init()
meth public abstract <%0 extends java.nio.file.attribute.FileStoreAttributeView> {%%0} getFileStoreAttributeView(java.lang.Class<{%%0}>)
meth public abstract boolean isReadOnly()
meth public abstract boolean supportsFileAttributeView(java.lang.Class<? extends java.nio.file.attribute.FileAttributeView>)
meth public abstract boolean supportsFileAttributeView(java.lang.String)
meth public abstract java.lang.Object getAttribute(java.lang.String) throws java.io.IOException
meth public abstract java.lang.String name()
meth public abstract java.lang.String type()
meth public abstract long getTotalSpace() throws java.io.IOException
meth public abstract long getUnallocatedSpace() throws java.io.IOException
meth public abstract long getUsableSpace() throws java.io.IOException
supr java.lang.Object

CLSS public abstract java.nio.file.FileSystem
cons protected init()
intf java.io.Closeable
meth public abstract !varargs java.nio.file.Path getPath(java.lang.String,java.lang.String[])
meth public abstract boolean isOpen()
meth public abstract boolean isReadOnly()
meth public abstract java.lang.Iterable<java.nio.file.FileStore> getFileStores()
meth public abstract java.lang.Iterable<java.nio.file.Path> getRootDirectories()
meth public abstract java.lang.String getSeparator()
meth public abstract java.nio.file.PathMatcher getPathMatcher(java.lang.String)
meth public abstract java.nio.file.WatchService newWatchService() throws java.io.IOException
meth public abstract java.nio.file.attribute.UserPrincipalLookupService getUserPrincipalLookupService()
meth public abstract java.nio.file.spi.FileSystemProvider provider()
meth public abstract java.util.Set<java.lang.String> supportedFileAttributeViews()
meth public abstract void close() throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.nio.file.Path
intf java.lang.Comparable<java.nio.file.Path>
intf java.lang.Iterable<java.nio.file.Path>
intf java.nio.file.Watchable
meth public abstract !varargs java.nio.file.Path toRealPath(java.nio.file.LinkOption[]) throws java.io.IOException
meth public abstract !varargs java.nio.file.WatchKey register(java.nio.file.WatchService,java.nio.file.WatchEvent$Kind<?>[]) throws java.io.IOException
meth public abstract !varargs java.nio.file.WatchKey register(java.nio.file.WatchService,java.nio.file.WatchEvent$Kind<?>[],java.nio.file.WatchEvent$Modifier[]) throws java.io.IOException
meth public abstract boolean endsWith(java.lang.String)
meth public abstract boolean endsWith(java.nio.file.Path)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isAbsolute()
meth public abstract boolean startsWith(java.lang.String)
meth public abstract boolean startsWith(java.nio.file.Path)
meth public abstract int compareTo(java.nio.file.Path)
meth public abstract int getNameCount()
meth public abstract int hashCode()
meth public abstract java.io.File toFile()
meth public abstract java.lang.String toString()
meth public abstract java.net.URI toUri()
meth public abstract java.nio.file.FileSystem getFileSystem()
meth public abstract java.nio.file.Path getFileName()
meth public abstract java.nio.file.Path getName(int)
meth public abstract java.nio.file.Path getParent()
meth public abstract java.nio.file.Path getRoot()
meth public abstract java.nio.file.Path normalize()
meth public abstract java.nio.file.Path relativize(java.nio.file.Path)
meth public abstract java.nio.file.Path resolve(java.lang.String)
meth public abstract java.nio.file.Path resolve(java.nio.file.Path)
meth public abstract java.nio.file.Path resolveSibling(java.lang.String)
meth public abstract java.nio.file.Path resolveSibling(java.nio.file.Path)
meth public abstract java.nio.file.Path subpath(int,int)
meth public abstract java.nio.file.Path toAbsolutePath()
meth public abstract java.util.Iterator<java.nio.file.Path> iterator()

CLSS public abstract interface java.nio.file.Watchable
meth public abstract !varargs java.nio.file.WatchKey register(java.nio.file.WatchService,java.nio.file.WatchEvent$Kind<?>[]) throws java.io.IOException
meth public abstract !varargs java.nio.file.WatchKey register(java.nio.file.WatchService,java.nio.file.WatchEvent$Kind<?>[],java.nio.file.WatchEvent$Modifier[]) throws java.io.IOException

CLSS public abstract interface java.nio.file.attribute.AclFileAttributeView
intf java.nio.file.attribute.FileOwnerAttributeView
meth public abstract java.lang.String name()
meth public abstract java.util.List<java.nio.file.attribute.AclEntry> getAcl() throws java.io.IOException
meth public abstract void setAcl(java.util.List<java.nio.file.attribute.AclEntry>) throws java.io.IOException

CLSS public abstract interface java.nio.file.attribute.AttributeView
meth public abstract java.lang.String name()

CLSS public abstract interface java.nio.file.attribute.BasicFileAttributeView
intf java.nio.file.attribute.FileAttributeView
meth public abstract java.lang.String name()
meth public abstract java.nio.file.attribute.BasicFileAttributes readAttributes() throws java.io.IOException
meth public abstract void setTimes(java.nio.file.attribute.FileTime,java.nio.file.attribute.FileTime,java.nio.file.attribute.FileTime) throws java.io.IOException

CLSS public abstract interface java.nio.file.attribute.BasicFileAttributes
meth public abstract boolean isDirectory()
meth public abstract boolean isOther()
meth public abstract boolean isRegularFile()
meth public abstract boolean isSymbolicLink()
meth public abstract java.lang.Object fileKey()
meth public abstract java.nio.file.attribute.FileTime creationTime()
meth public abstract java.nio.file.attribute.FileTime lastAccessTime()
meth public abstract java.nio.file.attribute.FileTime lastModifiedTime()
meth public abstract long size()

CLSS public abstract interface java.nio.file.attribute.FileAttributeView
intf java.nio.file.attribute.AttributeView

CLSS public abstract interface java.nio.file.attribute.FileOwnerAttributeView
intf java.nio.file.attribute.FileAttributeView
meth public abstract java.lang.String name()
meth public abstract java.nio.file.attribute.UserPrincipal getOwner() throws java.io.IOException
meth public abstract void setOwner(java.nio.file.attribute.UserPrincipal) throws java.io.IOException

CLSS public abstract interface java.nio.file.attribute.GroupPrincipal
intf java.nio.file.attribute.UserPrincipal

CLSS public abstract interface java.nio.file.attribute.PosixFileAttributeView
intf java.nio.file.attribute.BasicFileAttributeView
intf java.nio.file.attribute.FileOwnerAttributeView
meth public abstract java.lang.String name()
meth public abstract java.nio.file.attribute.PosixFileAttributes readAttributes() throws java.io.IOException
meth public abstract void setGroup(java.nio.file.attribute.GroupPrincipal) throws java.io.IOException
meth public abstract void setPermissions(java.util.Set<java.nio.file.attribute.PosixFilePermission>) throws java.io.IOException

CLSS public abstract interface java.nio.file.attribute.PosixFileAttributes
intf java.nio.file.attribute.BasicFileAttributes
meth public abstract java.nio.file.attribute.GroupPrincipal group()
meth public abstract java.nio.file.attribute.UserPrincipal owner()
meth public abstract java.util.Set<java.nio.file.attribute.PosixFilePermission> permissions()

CLSS public abstract interface java.nio.file.attribute.UserPrincipal
intf java.security.Principal

CLSS public abstract java.nio.file.attribute.UserPrincipalLookupService
cons protected init()
meth public abstract java.nio.file.attribute.GroupPrincipal lookupPrincipalByGroupName(java.lang.String) throws java.io.IOException
meth public abstract java.nio.file.attribute.UserPrincipal lookupPrincipalByName(java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public abstract java.nio.file.spi.FileSystemProvider
cons protected init()
meth public !varargs java.io.InputStream newInputStream(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs java.io.OutputStream newOutputStream(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs java.nio.channels.AsynchronousFileChannel newAsynchronousFileChannel(java.nio.file.Path,java.util.Set<? extends java.nio.file.OpenOption>,java.util.concurrent.ExecutorService,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs java.nio.channels.FileChannel newFileChannel(java.nio.file.Path,java.util.Set<? extends java.nio.file.OpenOption>,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs void createSymbolicLink(java.nio.file.Path,java.nio.file.Path,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public abstract !varargs <%0 extends java.nio.file.attribute.BasicFileAttributes> {%%0} readAttributes(java.nio.file.Path,java.lang.Class<{%%0}>,java.nio.file.LinkOption[]) throws java.io.IOException
meth public abstract !varargs <%0 extends java.nio.file.attribute.FileAttributeView> {%%0} getFileAttributeView(java.nio.file.Path,java.lang.Class<{%%0}>,java.nio.file.LinkOption[])
meth public abstract !varargs java.nio.channels.SeekableByteChannel newByteChannel(java.nio.file.Path,java.util.Set<? extends java.nio.file.OpenOption>,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public abstract !varargs java.util.Map<java.lang.String,java.lang.Object> readAttributes(java.nio.file.Path,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth public abstract !varargs void checkAccess(java.nio.file.Path,java.nio.file.AccessMode[]) throws java.io.IOException
meth public abstract !varargs void copy(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption[]) throws java.io.IOException
meth public abstract !varargs void createDirectory(java.nio.file.Path,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public abstract !varargs void move(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption[]) throws java.io.IOException
meth public abstract !varargs void setAttribute(java.nio.file.Path,java.lang.String,java.lang.Object,java.nio.file.LinkOption[]) throws java.io.IOException
meth public abstract boolean isHidden(java.nio.file.Path) throws java.io.IOException
meth public abstract boolean isSameFile(java.nio.file.Path,java.nio.file.Path) throws java.io.IOException
meth public abstract java.lang.String getScheme()
meth public abstract java.nio.file.DirectoryStream<java.nio.file.Path> newDirectoryStream(java.nio.file.Path,java.nio.file.DirectoryStream$Filter<? super java.nio.file.Path>) throws java.io.IOException
meth public abstract java.nio.file.FileStore getFileStore(java.nio.file.Path) throws java.io.IOException
meth public abstract java.nio.file.FileSystem getFileSystem(java.net.URI)
meth public abstract java.nio.file.FileSystem newFileSystem(java.net.URI,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public abstract java.nio.file.Path getPath(java.net.URI)
meth public abstract void delete(java.nio.file.Path) throws java.io.IOException
meth public boolean deleteIfExists(java.nio.file.Path) throws java.io.IOException
meth public java.nio.file.FileSystem newFileSystem(java.nio.file.Path,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public java.nio.file.Path readSymbolicLink(java.nio.file.Path) throws java.io.IOException
meth public static java.util.List<java.nio.file.spi.FileSystemProvider> installedProviders()
meth public void createLink(java.nio.file.Path,java.nio.file.Path) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.security.Principal
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String toString()
meth public boolean implies(javax.security.auth.Subject)

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface java.util.Iterator<%0 extends java.lang.Object>
meth public abstract boolean hasNext()
meth public abstract {java.util.Iterator%0} next()
meth public void forEachRemaining(java.util.function.Consumer<? super {java.util.Iterator%0}>)
meth public void remove()

CLSS public abstract interface java.util.function.Function<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public <%0 extends java.lang.Object> java.util.function.Function<{%%0},{java.util.function.Function%1}> compose(java.util.function.Function<? super {%%0},? extends {java.util.function.Function%0}>)
meth public <%0 extends java.lang.Object> java.util.function.Function<{java.util.function.Function%0},{%%0}> andThen(java.util.function.Function<? super {java.util.function.Function%1},? extends {%%0}>)
meth public abstract {java.util.function.Function%1} apply({java.util.function.Function%0})
meth public static <%0 extends java.lang.Object> java.util.function.Function<{%%0},{%%0}> identity()

CLSS public abstract interface java.util.function.Supplier<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract {java.util.function.Supplier%0} get()

CLSS public abstract org.apache.sshd.client.channel.AbstractClientChannel
cons protected init(java.lang.String)
cons protected init(java.lang.String,java.util.Collection<? extends org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.channel.Channel>>)
fld protected boolean redirectErrorStream
fld protected final java.util.concurrent.atomic.AtomicBoolean opened
fld protected final java.util.concurrent.atomic.AtomicReference<java.lang.Integer> exitStatusHolder
fld protected final java.util.concurrent.atomic.AtomicReference<java.lang.String> exitSignalHolder
fld protected int openFailureReason
fld protected java.io.InputStream in
fld protected java.io.InputStream invertedErr
fld protected java.io.InputStream invertedOut
fld protected java.io.OutputStream err
fld protected java.io.OutputStream invertedIn
fld protected java.io.OutputStream out
fld protected java.lang.String openFailureLang
fld protected java.lang.String openFailureMsg
fld protected org.apache.sshd.client.future.OpenFuture openFuture
fld protected org.apache.sshd.common.channel.ChannelAsyncInputStream asyncErr
fld protected org.apache.sshd.common.channel.ChannelAsyncInputStream asyncOut
fld protected org.apache.sshd.common.channel.ChannelAsyncOutputStream asyncIn
fld protected org.apache.sshd.common.channel.StreamingChannel$Streaming streaming
intf org.apache.sshd.client.channel.ClientChannel
meth protected <%0 extends java.util.Collection<org.apache.sshd.client.channel.ClientChannelEvent>> {%%0} updateCurrentChannelState({%%0})
meth protected abstract void doOpen() throws java.io.IOException
meth protected boolean mayWrite()
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected void addChannelSignalRequestHandlers(org.apache.sshd.common.util.EventNotifier<java.lang.String>)
meth protected void doWriteData(byte[],int,long) throws java.io.IOException
meth protected void doWriteExtendedData(byte[],int,long) throws java.io.IOException
meth public boolean isRedirectErrorStream()
meth public java.io.InputStream getIn()
meth public java.io.InputStream getInvertedErr()
meth public java.io.InputStream getInvertedOut()
meth public java.io.OutputStream getErr()
meth public java.io.OutputStream getInvertedIn()
meth public java.io.OutputStream getOut()
meth public java.lang.Integer getExitStatus()
meth public java.lang.String getChannelType()
meth public java.lang.String getExitSignal()
meth public java.util.Set<org.apache.sshd.client.channel.ClientChannelEvent> getChannelState()
meth public java.util.Set<org.apache.sshd.client.channel.ClientChannelEvent> waitFor(java.util.Collection<org.apache.sshd.client.channel.ClientChannelEvent>,long)
meth public org.apache.sshd.client.future.OpenFuture open() throws java.io.IOException
meth public org.apache.sshd.client.future.OpenFuture open(long,long,long,org.apache.sshd.common.util.buffer.Buffer)
meth public org.apache.sshd.common.channel.StreamingChannel$Streaming getStreaming()
meth public org.apache.sshd.common.io.IoInputStream getAsyncErr()
meth public org.apache.sshd.common.io.IoInputStream getAsyncOut()
meth public org.apache.sshd.common.io.IoOutputStream getAsyncIn()
meth public void handleOpenFailure(org.apache.sshd.common.util.buffer.Buffer)
meth public void handleOpenSuccess(long,long,long,org.apache.sshd.common.util.buffer.Buffer)
meth public void handleWindowAdjust(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void setErr(java.io.OutputStream)
meth public void setIn(java.io.InputStream)
meth public void setOut(java.io.OutputStream)
meth public void setRedirectErrorStream(boolean)
meth public void setStreaming(org.apache.sshd.common.channel.StreamingChannel$Streaming)
supr org.apache.sshd.common.channel.AbstractChannel
hfds NULL_INPUT_STREAM,channelType
hcls NullIoInputStream

CLSS public abstract org.apache.sshd.client.channel.AsyncCapableClientChannel
cons protected init(java.lang.String,boolean)
fld protected final boolean withErrorStream
fld protected java.util.concurrent.Future<?> pumper
fld protected org.apache.sshd.common.util.threads.CloseableExecutorService pumperService
meth protected int securedRead(java.io.InputStream,int,byte[],int,int) throws java.io.IOException
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected void closeImmediately0()
meth protected void doOpen() throws java.io.IOException
meth protected void doWriteExtendedData(byte[],int,long) throws java.io.IOException
meth protected void pumpInputStream()
meth public java.io.InputStream getInvertedErr()
meth public java.io.OutputStream getErr()
meth public org.apache.sshd.common.io.IoInputStream getAsyncErr()
meth public void setErr(java.io.OutputStream)
supr org.apache.sshd.client.channel.AbstractClientChannel

CLSS public org.apache.sshd.client.channel.ChannelSession
cons public init()
fld protected java.util.concurrent.Future<?> pumper
fld protected org.apache.sshd.common.util.threads.CloseableExecutorService pumperService
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleInternalRequest(java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleXonXoff(org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.io.IOException
meth protected void sendEnvVariables(org.apache.sshd.common.session.Session) throws java.io.IOException
meth public java.lang.Object setEnv(java.lang.String,java.lang.Object)
supr org.apache.sshd.client.channel.AsyncCapableClientChannel
hfds env

CLSS public org.apache.sshd.client.channel.ChannelSubsystem
cons public init(java.lang.String)
meth protected void doOpen() throws java.io.IOException
meth public final java.lang.String getSubsystem()
meth public java.lang.String toString()
meth public void handleFailure() throws java.io.IOException
meth public void handleSuccess() throws java.io.IOException
meth public void onClose(java.lang.Runnable)
supr org.apache.sshd.client.channel.ChannelSession
hfds subsystem

CLSS public abstract interface org.apache.sshd.client.channel.ClientChannel
intf org.apache.sshd.client.session.ClientSessionHolder
intf org.apache.sshd.common.channel.Channel
intf org.apache.sshd.common.channel.StreamingChannel
meth public abstract java.io.InputStream getInvertedErr()
meth public abstract java.io.InputStream getInvertedOut()
meth public abstract java.io.OutputStream getInvertedIn()
meth public abstract java.lang.Integer getExitStatus()
meth public abstract java.lang.String getChannelType()
meth public abstract java.lang.String getExitSignal()
meth public abstract java.util.Set<org.apache.sshd.client.channel.ClientChannelEvent> getChannelState()
meth public abstract java.util.Set<org.apache.sshd.client.channel.ClientChannelEvent> waitFor(java.util.Collection<org.apache.sshd.client.channel.ClientChannelEvent>,long)
meth public abstract org.apache.sshd.client.future.OpenFuture open() throws java.io.IOException
meth public abstract org.apache.sshd.common.io.IoInputStream getAsyncErr()
meth public abstract org.apache.sshd.common.io.IoInputStream getAsyncOut()
meth public abstract org.apache.sshd.common.io.IoOutputStream getAsyncIn()
meth public abstract void setErr(java.io.OutputStream)
meth public abstract void setIn(java.io.InputStream)
meth public abstract void setOut(java.io.OutputStream)
meth public abstract void setRedirectErrorStream(boolean)
meth public java.util.Set<org.apache.sshd.client.channel.ClientChannelEvent> waitFor(java.util.Collection<org.apache.sshd.client.channel.ClientChannelEvent>,java.time.Duration)
meth public org.apache.sshd.client.session.ClientSession getClientSession()
meth public static void validateCommandExitStatusCode(java.lang.String,java.lang.Integer) throws java.rmi.RemoteException

CLSS public abstract interface org.apache.sshd.client.channel.ClientChannelHolder
 anno 0 java.lang.FunctionalInterface()
intf org.apache.sshd.common.channel.ChannelHolder
meth public abstract org.apache.sshd.client.channel.ClientChannel getClientChannel()
meth public org.apache.sshd.common.channel.Channel getChannel()

CLSS public abstract interface org.apache.sshd.client.session.ClientSessionHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.client.session.ClientSession getClientSession()

CLSS public abstract org.apache.sshd.client.subsystem.AbstractSubsystemClient
cons protected init()
intf org.apache.sshd.client.subsystem.SubsystemClient
meth public java.lang.String toString()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public abstract interface org.apache.sshd.client.subsystem.SubsystemClient
intf java.nio.channels.Channel
intf org.apache.sshd.client.channel.ClientChannelHolder
intf org.apache.sshd.client.session.ClientSessionHolder
intf org.apache.sshd.common.NamedResource
intf org.apache.sshd.common.session.SessionHolder<org.apache.sshd.client.session.ClientSession>
meth public org.apache.sshd.client.session.ClientSession getSession()

CLSS public abstract interface org.apache.sshd.common.AttributeRepository
innr public static AttributeKey
meth public <%0 extends java.lang.Object> {%%0} resolveAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} getAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public abstract int getAttributesCount()
meth public abstract java.util.Collection<org.apache.sshd.common.AttributeRepository$AttributeKey<?>> attributeKeys()
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.AttributeRepository ofKeyValuePair(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,{%%0})
meth public static org.apache.sshd.common.AttributeRepository ofAttributesMap(java.util.Map<org.apache.sshd.common.AttributeRepository$AttributeKey<?>,?>)

CLSS public abstract interface org.apache.sshd.common.AttributeStore
intf org.apache.sshd.common.AttributeRepository
meth public <%0 extends java.lang.Object> {%%0} computeAttributeIfAbsent(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,java.util.function.Function<? super org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,? extends {%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} removeAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} setAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,{%%0})
meth public abstract void clearAttributes()

CLSS public abstract interface org.apache.sshd.common.Closeable
intf java.nio.channels.Channel
meth public abstract boolean isClosed()
meth public abstract boolean isClosing()
meth public abstract org.apache.sshd.common.future.CloseFuture close(boolean)
meth public abstract void addCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
meth public abstract void removeCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
meth public boolean isOpen()
meth public static java.time.Duration getMaxCloseWaitTime(org.apache.sshd.common.PropertyResolver)
meth public static void close(org.apache.sshd.common.Closeable) throws java.io.IOException
meth public void close() throws java.io.IOException

CLSS public abstract interface org.apache.sshd.common.NamedResource
 anno 0 java.lang.FunctionalInterface()
fld public final static java.util.Comparator<org.apache.sshd.common.NamedResource> BY_NAME_COMPARATOR
fld public final static java.util.function.Function<org.apache.sshd.common.NamedResource,java.lang.String> NAME_EXTRACTOR
meth public abstract java.lang.String getName()
meth public static <%0 extends org.apache.sshd.common.NamedResource> {%%0} findByName(java.lang.String,java.util.Comparator<? super java.lang.String>,java.util.Collection<? extends {%%0}>)
meth public static <%0 extends org.apache.sshd.common.NamedResource> {%%0} findFirstMatchByName(java.util.Collection<java.lang.String>,java.util.Comparator<? super java.lang.String>,java.util.Collection<? extends {%%0}>)
meth public static <%0 extends org.apache.sshd.common.NamedResource> {%%0} removeByName(java.lang.String,java.util.Comparator<? super java.lang.String>,java.util.Collection<? extends {%%0}>)
meth public static int safeCompareByName(org.apache.sshd.common.NamedResource,org.apache.sshd.common.NamedResource,boolean)
meth public static java.lang.String getNames(java.util.Collection<? extends org.apache.sshd.common.NamedResource>)
meth public static java.util.List<java.lang.String> getNameList(java.util.Collection<? extends org.apache.sshd.common.NamedResource>)
meth public static org.apache.sshd.common.NamedResource ofName(java.lang.String)

CLSS public abstract interface org.apache.sshd.common.OptionalFeature
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.common.OptionalFeature FALSE
fld public final static org.apache.sshd.common.OptionalFeature TRUE
meth public abstract boolean isSupported()
meth public static org.apache.sshd.common.OptionalFeature all(java.util.Collection<? extends org.apache.sshd.common.OptionalFeature>)
meth public static org.apache.sshd.common.OptionalFeature any(java.util.Collection<? extends org.apache.sshd.common.OptionalFeature>)
meth public static org.apache.sshd.common.OptionalFeature of(boolean)

CLSS public abstract interface org.apache.sshd.common.PropertyResolver
fld public final static org.apache.sshd.common.PropertyResolver EMPTY
meth public abstract java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public abstract org.apache.sshd.common.PropertyResolver getParentPropertyResolver()
meth public boolean getBooleanProperty(java.lang.String,boolean)
meth public boolean isEmpty()
meth public int getIntProperty(java.lang.String,int)
meth public java.lang.Boolean getBoolean(java.lang.String)
meth public java.lang.Integer getInteger(java.lang.String)
meth public java.lang.Long getLong(java.lang.String)
meth public java.lang.Object getObject(java.lang.String)
meth public java.lang.String getString(java.lang.String)
meth public java.lang.String getStringProperty(java.lang.String,java.lang.String)
meth public java.nio.charset.Charset getCharset(java.lang.String,java.nio.charset.Charset)
meth public long getLongProperty(java.lang.String,long)
meth public static boolean isEmpty(org.apache.sshd.common.PropertyResolver)

CLSS public abstract org.apache.sshd.common.channel.AbstractChannel
cons protected init(boolean)
cons protected init(boolean,java.util.Collection<? extends org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.channel.Channel>>)
cons protected init(java.lang.String,boolean)
cons protected init(java.lang.String,boolean,java.util.Collection<? extends org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.channel.Channel>>,org.apache.sshd.common.util.threads.CloseableExecutorService)
fld protected final java.util.Collection<org.apache.sshd.common.channel.ChannelListener> channelListeners
fld protected final java.util.concurrent.atomic.AtomicBoolean closeSignaled
fld protected final java.util.concurrent.atomic.AtomicBoolean eofReceived
fld protected final java.util.concurrent.atomic.AtomicBoolean eofSent
 anno 0 java.lang.Deprecated()
fld protected final java.util.concurrent.atomic.AtomicBoolean initialized
fld protected final java.util.concurrent.atomic.AtomicBoolean unregisterSignaled
fld protected final org.apache.sshd.common.channel.ChannelListener channelListenerProxy
fld protected final org.apache.sshd.common.future.DefaultCloseFuture gracefulFuture
fld protected java.util.concurrent.atomic.AtomicReference<org.apache.sshd.common.channel.AbstractChannel$GracefulState> gracefulState
fld protected org.apache.sshd.common.session.ConnectionService service
fld public final static java.util.function.IntUnaryOperator RESPONSE_BUFFER_GROWTH_FACTOR
fld public final static org.apache.sshd.common.channel.AbstractChannel$PacketValidator DEFAULT_PACKET_VALIDATOR
innr protected final static !enum GracefulState
innr public GracefulChannelCloseable
innr public abstract interface static PacketValidator
intf org.apache.sshd.common.channel.Channel
intf org.apache.sshd.common.util.threads.ExecutorServiceCarrier
meth protected abstract void doWriteData(byte[],int,long) throws java.io.IOException
meth protected abstract void doWriteExtendedData(byte[],int,long) throws java.io.IOException
meth protected boolean mayWrite()
meth protected java.util.Date addPendingRequest(java.lang.String,boolean)
meth protected java.util.Date removePendingRequest(java.lang.String)
meth protected long validateIncomingDataSize(int,long) throws java.io.IOException
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleInternalRequest(java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.io.IoWriteFuture sendEof() throws java.io.IOException
meth protected org.apache.sshd.common.io.IoWriteFuture sendResponse(org.apache.sshd.common.util.buffer.Buffer,java.lang.String,org.apache.sshd.common.channel.RequestHandler$Result,boolean) throws java.io.IOException
meth protected void configureWindow()
meth protected void handleChannelRequest(java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected void handleUnknownChannelRequest(java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected void invokeChannelSignaller(org.apache.sshd.common.util.io.functors.Invoker<org.apache.sshd.common.channel.ChannelListener,java.lang.Void>) throws java.lang.Throwable
meth protected void notifyStateChanged(java.lang.String)
meth protected void notifyStateChanged(org.apache.sshd.common.channel.ChannelListener,java.lang.String)
meth protected void preClose()
meth protected void sendWindowAdjust(long) throws java.io.IOException
meth protected void setRecipient(long)
meth protected void signalChannelClosed(org.apache.sshd.common.channel.ChannelListener,java.lang.Throwable)
meth protected void signalChannelInitialized() throws java.io.IOException
meth protected void signalChannelInitialized(org.apache.sshd.common.channel.ChannelListener)
meth protected void signalChannelOpenFailure(java.lang.Throwable)
meth protected void signalChannelOpenFailure(org.apache.sshd.common.channel.ChannelListener,java.lang.Throwable)
meth protected void signalChannelOpenSuccess()
meth protected void signalChannelOpenSuccess(org.apache.sshd.common.channel.ChannelListener)
meth public <%0 extends java.lang.Object> {%%0} computeAttributeIfAbsent(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,java.util.function.Function<? super org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,? extends {%%0}>)
meth public <%0 extends java.lang.Object> {%%0} getAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} removeAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} setAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,{%%0})
meth public boolean isEofSent()
meth public boolean isEofSignalled()
meth public boolean isInitialized()
meth public int getAttributesCount()
meth public java.lang.String toString()
meth public java.util.Collection<org.apache.sshd.common.AttributeRepository$AttributeKey<?>> attributeKeys()
meth public java.util.List<org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.channel.Channel>> getRequestHandlers()
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public long getChannelId()
meth public long getRecipient()
meth public org.apache.sshd.common.PropertyResolver getParentPropertyResolver()
meth public org.apache.sshd.common.channel.AbstractChannel$PacketValidator getPacketValidator()
meth public org.apache.sshd.common.channel.ChannelListener getChannelListenerProxy()
meth public org.apache.sshd.common.channel.LocalWindow getLocalWindow()
meth public org.apache.sshd.common.channel.RemoteWindow getRemoteWindow()
meth public org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver getChannelStreamWriterResolver()
meth public org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver resolveChannelStreamWriterResolver()
meth public org.apache.sshd.common.io.IoWriteFuture writePacket(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public org.apache.sshd.common.session.Session getSession()
meth public org.apache.sshd.common.util.threads.CloseableExecutorService getExecutorService()
meth public void addChannelListener(org.apache.sshd.common.channel.ChannelListener)
meth public void addRequestHandler(org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.channel.Channel>)
meth public void clearAttributes()
meth public void handleChannelRegistrationResult(org.apache.sshd.common.session.ConnectionService,org.apache.sshd.common.session.Session,long,boolean)
meth public void handleChannelUnregistration(org.apache.sshd.common.session.ConnectionService)
meth public void handleClose() throws java.io.IOException
meth public void handleData(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void handleEof() throws java.io.IOException
meth public void handleExtendedData(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void handleFailure() throws java.io.IOException
meth public void handleRequest(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void handleSuccess() throws java.io.IOException
meth public void handleWindowAdjust(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void init(org.apache.sshd.common.session.ConnectionService,org.apache.sshd.common.session.Session,long) throws java.io.IOException
meth public void removeChannelListener(org.apache.sshd.common.channel.ChannelListener)
meth public void removeRequestHandler(org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.channel.Channel>)
meth public void setChannelStreamWriterResolver(org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver)
meth public void setPacketValidator(org.apache.sshd.common.channel.AbstractChannel$PacketValidator)
meth public void signalChannelClosed(java.lang.Throwable)
supr org.apache.sshd.common.util.closeable.AbstractInnerCloseable
hfds attributes,channelStreamPacketWriterResolver,eofFuture,executor,id,localWindow,packetValidator,pendingRequests,properties,recipient,remoteWindow,requestHandlers,sessionInstance

CLSS public abstract interface org.apache.sshd.common.channel.Channel
fld public final static java.lang.String CHANNEL_EXEC = "exec"
fld public final static java.lang.String CHANNEL_SHELL = "shell"
fld public final static java.lang.String CHANNEL_SUBSYSTEM = "subsystem"
intf org.apache.sshd.common.AttributeStore
intf org.apache.sshd.common.Closeable
intf org.apache.sshd.common.PropertyResolver
intf org.apache.sshd.common.channel.ChannelIdentifier
intf org.apache.sshd.common.channel.ChannelListenerManager
intf org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolverManager
intf org.apache.sshd.common.session.SessionHolder<org.apache.sshd.common.session.Session>
meth public <%0 extends java.lang.Object> {%%0} resolveAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public abstract boolean isEofSignalled()
meth public abstract boolean isInitialized()
meth public abstract java.util.List<org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.channel.Channel>> getRequestHandlers()
meth public abstract long getRecipient()
meth public abstract org.apache.sshd.client.future.OpenFuture open(long,long,long,org.apache.sshd.common.util.buffer.Buffer)
meth public abstract org.apache.sshd.common.channel.LocalWindow getLocalWindow()
meth public abstract org.apache.sshd.common.channel.RemoteWindow getRemoteWindow()
meth public abstract org.apache.sshd.common.io.IoWriteFuture writePacket(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public abstract void addRequestHandler(org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.channel.Channel>)
meth public abstract void handleChannelRegistrationResult(org.apache.sshd.common.session.ConnectionService,org.apache.sshd.common.session.Session,long,boolean)
meth public abstract void handleChannelUnregistration(org.apache.sshd.common.session.ConnectionService)
meth public abstract void handleClose() throws java.io.IOException
meth public abstract void handleData(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public abstract void handleEof() throws java.io.IOException
meth public abstract void handleExtendedData(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public abstract void handleFailure() throws java.io.IOException
meth public abstract void handleOpenFailure(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public abstract void handleOpenSuccess(long,long,long,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public abstract void handleRequest(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public abstract void handleSuccess() throws java.io.IOException
meth public abstract void handleWindowAdjust(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public abstract void init(org.apache.sshd.common.session.ConnectionService,org.apache.sshd.common.session.Session,long) throws java.io.IOException
meth public abstract void removeRequestHandler(org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.channel.Channel>)
meth public static <%0 extends java.lang.Object> {%%0} resolveAttribute(org.apache.sshd.common.channel.Channel,org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public void addRequestHandlers(java.util.Collection<? extends org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.channel.Channel>>)
meth public void removeRequestHandlers(java.util.Collection<? extends org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.channel.Channel>>)

CLSS public abstract interface org.apache.sshd.common.channel.ChannelHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.common.channel.Channel getChannel()

CLSS public abstract interface org.apache.sshd.common.channel.ChannelIdentifier
 anno 0 java.lang.FunctionalInterface()
meth public abstract long getChannelId()

CLSS public abstract interface org.apache.sshd.common.channel.ChannelListenerManager
meth public abstract org.apache.sshd.common.channel.ChannelListener getChannelListenerProxy()
meth public abstract void addChannelListener(org.apache.sshd.common.channel.ChannelListener)
meth public abstract void removeChannelListener(org.apache.sshd.common.channel.ChannelListener)

CLSS public abstract interface org.apache.sshd.common.channel.StreamingChannel
innr public final static !enum Streaming
meth public abstract org.apache.sshd.common.channel.StreamingChannel$Streaming getStreaming()
meth public abstract void setStreaming(org.apache.sshd.common.channel.StreamingChannel$Streaming)

CLSS public abstract interface org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver NONE
meth public abstract org.apache.sshd.common.channel.throttle.ChannelStreamWriter resolveChannelStreamWriter(org.apache.sshd.common.channel.Channel,byte)

CLSS public abstract interface org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolverManager
intf org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver
meth public abstract org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver getChannelStreamWriterResolver()
meth public abstract void setChannelStreamWriterResolver(org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver)
meth public org.apache.sshd.common.channel.throttle.ChannelStreamWriter resolveChannelStreamWriter(org.apache.sshd.common.channel.Channel,byte)
meth public org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver resolveChannelStreamWriterResolver()

CLSS public abstract interface org.apache.sshd.common.file.FileSystemAware
 anno 0 java.lang.FunctionalInterface()
meth public abstract void setFileSystem(java.nio.file.FileSystem)
meth public void setFileSystemFactory(org.apache.sshd.common.file.FileSystemFactory,org.apache.sshd.common.session.SessionContext) throws java.io.IOException

CLSS public abstract org.apache.sshd.common.file.util.BaseFileSystem<%0 extends java.nio.file.Path>
cons protected init(java.nio.file.spi.FileSystemProvider)
fld protected final org.slf4j.Logger log
meth protected !varargs {org.apache.sshd.common.file.util.BaseFileSystem%0} create(java.lang.String,java.lang.String[])
meth protected abstract {org.apache.sshd.common.file.util.BaseFileSystem%0} create(java.lang.String,java.util.List<java.lang.String>)
meth protected boolean hostFsHasWindowsSeparator()
meth protected java.lang.String globToRegex(java.lang.String)
meth protected java.lang.String handleWindowsSeparator(java.lang.String)
meth protected void appendDedupSep(java.lang.StringBuilder,java.lang.CharSequence)
meth protected {org.apache.sshd.common.file.util.BaseFileSystem%0} create(java.lang.String,java.util.Collection<java.lang.String>)
meth public !varargs {org.apache.sshd.common.file.util.BaseFileSystem%0} getPath(java.lang.String,java.lang.String[])
meth public boolean isReadOnly()
meth public java.lang.Iterable<java.nio.file.FileStore> getFileStores()
meth public java.lang.Iterable<java.nio.file.Path> getRootDirectories()
meth public java.lang.String getSeparator()
meth public java.nio.file.PathMatcher getPathMatcher(java.lang.String)
meth public java.nio.file.WatchService newWatchService() throws java.io.IOException
meth public java.nio.file.spi.FileSystemProvider provider()
meth public {org.apache.sshd.common.file.util.BaseFileSystem%0} getDefaultDir()
supr java.nio.file.FileSystem
hfds fileSystemProvider

CLSS public abstract org.apache.sshd.common.file.util.BasePath<%0 extends org.apache.sshd.common.file.util.BasePath<{org.apache.sshd.common.file.util.BasePath%0},{org.apache.sshd.common.file.util.BasePath%1}>, %1 extends org.apache.sshd.common.file.util.BaseFileSystem<{org.apache.sshd.common.file.util.BasePath%0}>>
cons protected init({org.apache.sshd.common.file.util.BasePath%1},java.lang.String,java.util.List<java.lang.String>)
fld protected final java.lang.String root
fld protected final java.util.List<java.lang.String> names
intf java.nio.file.Path
meth protected !varargs {org.apache.sshd.common.file.util.BasePath%0} create(java.lang.String,java.lang.String[])
meth protected boolean endsWith(java.util.List<?>,java.util.List<?>)
meth protected boolean isNormal()
meth protected boolean startsWith(java.util.List<?>,java.util.List<?>)
meth protected int calculatedHashCode()
meth protected int compare(java.lang.String,java.lang.String)
meth protected java.lang.String asString()
meth protected {org.apache.sshd.common.file.util.BasePath%0} asT()
meth protected {org.apache.sshd.common.file.util.BasePath%0} checkPath(java.nio.file.Path)
meth protected {org.apache.sshd.common.file.util.BasePath%0} create(java.lang.String,java.util.Collection<java.lang.String>)
meth protected {org.apache.sshd.common.file.util.BasePath%0} create(java.lang.String,java.util.List<java.lang.String>)
meth public !varargs java.nio.file.WatchKey register(java.nio.file.WatchService,java.nio.file.WatchEvent$Kind<?>[]) throws java.io.IOException
meth public !varargs java.nio.file.WatchKey register(java.nio.file.WatchService,java.nio.file.WatchEvent$Kind<?>[],java.nio.file.WatchEvent$Modifier[]) throws java.io.IOException
meth public boolean endsWith(java.lang.String)
meth public boolean endsWith(java.nio.file.Path)
meth public boolean equals(java.lang.Object)
meth public boolean isAbsolute()
meth public boolean startsWith(java.lang.String)
meth public boolean startsWith(java.nio.file.Path)
meth public int compareTo(java.nio.file.Path)
meth public int getNameCount()
meth public int hashCode()
meth public java.io.File toFile()
meth public java.lang.String toString()
meth public java.net.URI toUri()
meth public java.nio.file.Path resolveSibling(java.lang.String)
meth public java.nio.file.Path resolveSibling(java.nio.file.Path)
meth public java.util.Iterator<java.nio.file.Path> iterator()
meth public {org.apache.sshd.common.file.util.BasePath%0} getFileName()
meth public {org.apache.sshd.common.file.util.BasePath%0} getName(int)
meth public {org.apache.sshd.common.file.util.BasePath%0} getParent()
meth public {org.apache.sshd.common.file.util.BasePath%0} getRoot()
meth public {org.apache.sshd.common.file.util.BasePath%0} normalize()
meth public {org.apache.sshd.common.file.util.BasePath%0} relativize(java.nio.file.Path)
meth public {org.apache.sshd.common.file.util.BasePath%0} resolve(java.lang.String)
meth public {org.apache.sshd.common.file.util.BasePath%0} resolve(java.nio.file.Path)
meth public {org.apache.sshd.common.file.util.BasePath%0} subpath(int,int)
meth public {org.apache.sshd.common.file.util.BasePath%0} toAbsolutePath()
meth public {org.apache.sshd.common.file.util.BasePath%1} getFileSystem()
supr java.lang.Object
hfds fileSystem,hashValue,strValue

CLSS public abstract interface org.apache.sshd.common.session.SessionContextHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.common.session.SessionContext getSessionContext()

CLSS public abstract interface org.apache.sshd.common.session.SessionHolder<%0 extends org.apache.sshd.common.session.Session>
 anno 0 java.lang.FunctionalInterface()
intf org.apache.sshd.common.session.SessionContextHolder
meth public abstract {org.apache.sshd.common.session.SessionHolder%0} getSession()
meth public org.apache.sshd.common.session.SessionContext getSessionContext()

CLSS public abstract interface org.apache.sshd.common.util.ObjectBuilder<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
intf java.util.function.Supplier<{org.apache.sshd.common.util.ObjectBuilder%0}>
meth public abstract {org.apache.sshd.common.util.ObjectBuilder%0} build()
meth public {org.apache.sshd.common.util.ObjectBuilder%0} get()

CLSS public abstract interface org.apache.sshd.common.util.SshdEventListener
intf java.util.EventListener
meth public static <%0 extends org.apache.sshd.common.util.SshdEventListener> {%%0} validateListener({%%0},java.lang.String)

CLSS public abstract org.apache.sshd.common.util.closeable.AbstractCloseable
cons protected init()
cons protected init(java.lang.String)
fld protected final java.lang.Object futureLock
fld protected final java.util.concurrent.atomic.AtomicReference<org.apache.sshd.common.util.closeable.AbstractCloseable$State> state
fld protected final org.apache.sshd.common.future.CloseFuture closeFuture
innr public final static !enum State
meth protected org.apache.sshd.common.future.CloseFuture doCloseGracefully()
meth protected org.apache.sshd.common.util.closeable.Builder builder()
meth protected void doCloseImmediately()
meth protected void preClose()
meth public final boolean isClosed()
meth public final boolean isClosing()
meth public final org.apache.sshd.common.future.CloseFuture close(boolean)
meth public java.lang.Object getFutureLock()
meth public void addCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
meth public void removeCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
supr org.apache.sshd.common.util.closeable.IoBaseCloseable

CLSS public abstract org.apache.sshd.common.util.closeable.AbstractInnerCloseable
cons protected init()
cons protected init(java.lang.String)
meth protected abstract org.apache.sshd.common.Closeable getInnerCloseable()
meth protected final org.apache.sshd.common.future.CloseFuture doCloseGracefully()
meth protected final void doCloseImmediately()
supr org.apache.sshd.common.util.closeable.AbstractCloseable

CLSS public abstract org.apache.sshd.common.util.closeable.IoBaseCloseable
cons protected init()
cons protected init(java.lang.String)
intf org.apache.sshd.common.Closeable
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public org.apache.sshd.common.util.io.DirectoryScanner
cons public !varargs init(java.nio.file.Path,java.lang.String[])
cons public init()
cons public init(java.nio.file.Path)
cons public init(java.nio.file.Path,java.util.Collection<java.lang.String>)
fld protected java.nio.file.Path basedir
meth protected <%0 extends java.util.Collection<java.nio.file.Path>> {%%0} scandir(java.nio.file.Path,java.nio.file.Path,{%%0}) throws java.io.IOException
meth public <%0 extends java.util.Collection<java.nio.file.Path>> {%%0} scan(java.util.function.Supplier<? extends {%%0}>) throws java.io.IOException
meth public boolean isFilesOnly()
meth public java.nio.file.Path getBasedir()
meth public java.util.Collection<java.nio.file.Path> scan() throws java.io.IOException
meth public void setBasedir(java.nio.file.Path)
meth public void setFilesOnly(boolean)
supr org.apache.sshd.common.util.io.PathScanningMatcher
hfds filesOnly

CLSS public abstract org.apache.sshd.common.util.io.PathScanningMatcher
cons protected init()
fld protected boolean caseSensitive
fld protected java.lang.String separator
fld protected java.util.List<java.lang.String> includePatterns
meth protected boolean couldHoldIncluded(java.lang.String)
meth protected boolean isIncluded(java.lang.String)
meth public !varargs void setIncludes(java.lang.String[])
meth public boolean isCaseSensitive()
meth public java.lang.String getSeparator()
meth public java.util.List<java.lang.String> getIncludes()
meth public static java.lang.String normalizePattern(java.lang.String)
meth public void setCaseSensitive(boolean)
meth public void setIncludes(java.util.Collection<java.lang.String>)
meth public void setSeparator(java.lang.String)
supr java.lang.Object

CLSS public abstract org.apache.sshd.common.util.io.input.InputStreamWithChannel
cons protected init()
intf java.nio.channels.Channel
supr java.io.InputStream

CLSS public abstract org.apache.sshd.common.util.io.output.OutputStreamWithChannel
cons protected init()
intf java.nio.channels.Channel
supr java.io.OutputStream

CLSS public abstract org.apache.sshd.common.util.logging.AbstractLoggingBean
cons protected init()
cons protected init(java.lang.String)
cons protected init(org.slf4j.Logger)
fld protected final org.slf4j.Logger log
meth protected org.apache.sshd.common.util.logging.SimplifiedLog getSimplifiedLogger()
meth protected void debug(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void debug(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void debug(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void debug(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void debug(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void error(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void error(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void error(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void error(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void error(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void info(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void info(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void warn(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void warn(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void warn(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void warn(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void warn(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void warn(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void warn(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth protected void warn(java.lang.String,java.lang.Object,java.lang.Object,java.lang.Throwable)
supr java.lang.Object
hfds simplifiedLog

CLSS public abstract interface org.apache.sshd.common.util.threads.ExecutorServiceCarrier
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.common.util.threads.CloseableExecutorService getExecutorService()

CLSS public abstract interface org.apache.sshd.common.util.threads.ExecutorServiceProvider
 anno 0 java.lang.FunctionalInterface()
meth public abstract java.util.function.Supplier<? extends org.apache.sshd.common.util.threads.CloseableExecutorService> getExecutorServiceProvider()
meth public org.apache.sshd.common.util.threads.CloseableExecutorService resolveExecutorService()

CLSS public abstract interface org.apache.sshd.common.util.threads.ManagedExecutorServiceSupplier
intf org.apache.sshd.common.util.threads.ExecutorServiceProvider
meth public abstract void setExecutorServiceProvider(java.util.function.Supplier<? extends org.apache.sshd.common.util.threads.CloseableExecutorService>)

CLSS public abstract interface org.apache.sshd.server.channel.ChannelDataReceiver
intf java.io.Closeable
meth public abstract int data(org.apache.sshd.server.channel.ChannelSession,byte[],int,int) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.server.channel.ServerChannelSessionHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.server.channel.ChannelSession getServerChannelSession()

CLSS public abstract interface org.apache.sshd.server.command.AsyncCommand
intf org.apache.sshd.server.command.AsyncCommandStreamsAware
intf org.apache.sshd.server.command.Command

CLSS public abstract interface org.apache.sshd.server.command.AsyncCommandErrorStreamAware
 anno 0 java.lang.FunctionalInterface()
meth public abstract void setIoErrorStream(org.apache.sshd.common.io.IoOutputStream)

CLSS public abstract interface org.apache.sshd.server.command.AsyncCommandInputStreamAware
 anno 0 java.lang.FunctionalInterface()
meth public abstract void setIoInputStream(org.apache.sshd.common.io.IoInputStream)

CLSS public abstract interface org.apache.sshd.server.command.AsyncCommandOutputStreamAware
 anno 0 java.lang.FunctionalInterface()
meth public abstract void setIoOutputStream(org.apache.sshd.common.io.IoOutputStream)

CLSS public abstract interface org.apache.sshd.server.command.AsyncCommandStreamsAware
intf org.apache.sshd.server.command.AsyncCommandErrorStreamAware
intf org.apache.sshd.server.command.AsyncCommandInputStreamAware
intf org.apache.sshd.server.command.AsyncCommandOutputStreamAware

CLSS public abstract interface org.apache.sshd.server.command.Command
intf org.apache.sshd.server.command.CommandDirectStreamsAware
intf org.apache.sshd.server.command.CommandLifecycle
meth public abstract void setExitCallback(org.apache.sshd.server.ExitCallback)

CLSS public abstract interface org.apache.sshd.server.command.CommandDirectErrorStreamAware
 anno 0 java.lang.FunctionalInterface()
meth public abstract void setErrorStream(java.io.OutputStream)

CLSS public abstract interface org.apache.sshd.server.command.CommandDirectInputStreamAware
 anno 0 java.lang.FunctionalInterface()
meth public abstract void setInputStream(java.io.InputStream)

CLSS public abstract interface org.apache.sshd.server.command.CommandDirectOutputStreamAware
 anno 0 java.lang.FunctionalInterface()
meth public abstract void setOutputStream(java.io.OutputStream)

CLSS public abstract interface org.apache.sshd.server.command.CommandDirectStreamsAware
intf org.apache.sshd.server.command.CommandDirectErrorStreamAware
intf org.apache.sshd.server.command.CommandDirectInputStreamAware
intf org.apache.sshd.server.command.CommandDirectOutputStreamAware

CLSS public abstract interface org.apache.sshd.server.command.CommandLifecycle
meth public abstract void destroy(org.apache.sshd.server.channel.ChannelSession) throws java.lang.Exception
meth public abstract void start(org.apache.sshd.server.channel.ChannelSession,org.apache.sshd.server.Environment) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.server.session.ServerSessionHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.server.session.ServerSession getServerSession()

CLSS public abstract interface org.apache.sshd.server.subsystem.SubsystemFactory
intf org.apache.sshd.common.NamedResource
meth public abstract org.apache.sshd.server.command.Command createSubsystem(org.apache.sshd.server.channel.ChannelSession) throws java.io.IOException
meth public static org.apache.sshd.server.command.Command createSubsystem(org.apache.sshd.server.channel.ChannelSession,java.util.Collection<? extends org.apache.sshd.server.subsystem.SubsystemFactory>,java.lang.String) throws java.io.IOException

CLSS public final org.apache.sshd.sftp.SftpModuleProperties
fld public final static int DEFAULT_FILE_HANDLE_ROUNDS = 4
fld public final static int DEFAULT_FILE_HANDLE_SIZE = 4
fld public final static int MAX_FILE_HANDLE_ROUNDS = 64
fld public final static int MAX_FILE_HANDLE_SIZE = 64
fld public final static int MIN_FILE_HANDLE_ROUNDS = 1
fld public final static int MIN_FILE_HANDLE_SIZE = 4
fld public final static int MIN_READDATA_PACKET_LENGTH = 32768
fld public final static int MIN_WRITEDATA_PACKET_LENGTH = 32768
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> APPEND_END_OF_LIST_INDICATOR
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> AUTO_FOLLOW_LINKS
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> TOLERATE_EXCESS_DATA
fld public final static org.apache.sshd.common.Property<java.lang.Integer> COPY_BUF_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.Integer> FILE_HANDLE_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.Integer> MAX_FILE_HANDLE_RAND_ROUNDS
fld public final static org.apache.sshd.common.Property<java.lang.Integer> MAX_OPEN_HANDLES_PER_SESSION
fld public final static org.apache.sshd.common.Property<java.lang.Integer> MAX_READDATA_PACKET_LENGTH
fld public final static org.apache.sshd.common.Property<java.lang.Integer> MAX_READDIR_DATA_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.Integer> MAX_WRITEDATA_PACKET_LENGTH
fld public final static org.apache.sshd.common.Property<java.lang.Integer> POOL_CORE_SIZE
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.Property<java.lang.Integer> POOL_SIZE
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.Property<java.lang.Integer> READ_BUFFER_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.Integer> SFTP_VERSION
fld public final static org.apache.sshd.common.Property<java.lang.Integer> WRITE_BUFFER_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.Integer> WRITE_CHUNK_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.String> ACL_SUPPORTED_MASK
fld public final static org.apache.sshd.common.Property<java.lang.String> CLIENT_EXTENSIONS
fld public final static org.apache.sshd.common.Property<java.lang.String> NEWLINE_VALUE
fld public final static org.apache.sshd.common.Property<java.lang.String> OPENSSH_EXTENSIONS
fld public final static org.apache.sshd.common.Property<java.nio.charset.Charset> NAME_DECODER_CHARSET
fld public final static org.apache.sshd.common.Property<java.nio.charset.Charset> NAME_DECODING_CHARSET
fld public final static org.apache.sshd.common.Property<java.time.Duration> AUTH_TIME
fld public final static org.apache.sshd.common.Property<java.time.Duration> CONNECT_TIME
fld public final static org.apache.sshd.common.Property<java.time.Duration> POOL_LIFE_TIME
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.Property<java.time.Duration> SFTP_CHANNEL_OPEN_TIMEOUT
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.sftp.client.FullAccessSftpClient
intf org.apache.sshd.sftp.client.RawSftpClient
intf org.apache.sshd.sftp.client.SftpClient
meth public static org.apache.sshd.sftp.client.SftpClient singleSessionInstance(org.apache.sshd.sftp.client.SftpClient)

CLSS public abstract interface org.apache.sshd.sftp.client.RawSftpClient
meth public abstract int send(int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public abstract org.apache.sshd.common.util.buffer.Buffer receive(int) throws java.io.IOException
meth public abstract org.apache.sshd.common.util.buffer.Buffer receive(int,java.time.Duration) throws java.io.IOException
meth public abstract org.apache.sshd.common.util.buffer.Buffer receive(int,long) throws java.io.IOException
meth public abstract org.apache.sshd.sftp.client.SftpMessage write(int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.sftp.client.SftpClient
fld public final static int IO_BUFFER_SIZE = 32768
fld public final static int MIN_BUFFER_SIZE = 256
fld public final static int MIN_READ_BUFFER_SIZE = 256
fld public final static int MIN_WRITE_BUFFER_SIZE = 256
fld public final static java.util.Set<org.apache.sshd.sftp.client.SftpClient$OpenMode> DEFAULT_CHANNEL_MODES
fld public final static org.apache.sshd.sftp.client.SftpClient$DirEntry[] EMPTY_DIR_ENTRIES
innr public abstract static CloseableHandle
innr public final static !enum Attribute
innr public final static !enum CopyMode
innr public final static !enum OpenMode
innr public static Attributes
innr public static DirEntry
innr public static Handle
intf org.apache.sshd.client.subsystem.SubsystemClient
meth public !varargs java.io.InputStream read(java.lang.String,int,org.apache.sshd.sftp.client.SftpClient$OpenMode[]) throws java.io.IOException
meth public !varargs java.io.InputStream read(java.lang.String,org.apache.sshd.sftp.client.SftpClient$OpenMode[]) throws java.io.IOException
meth public !varargs java.io.OutputStream write(java.lang.String,int,org.apache.sshd.sftp.client.SftpClient$OpenMode[]) throws java.io.IOException
meth public !varargs java.io.OutputStream write(java.lang.String,org.apache.sshd.sftp.client.SftpClient$OpenMode[]) throws java.io.IOException
meth public !varargs java.nio.channels.FileChannel openRemoteFileChannel(java.lang.String,org.apache.sshd.sftp.client.SftpClient$OpenMode[]) throws java.io.IOException
meth public !varargs java.nio.channels.FileChannel openRemotePathChannel(java.lang.String,java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs org.apache.sshd.sftp.client.SftpClient$CloseableHandle open(java.lang.String,org.apache.sshd.sftp.client.SftpClient$OpenMode[]) throws java.io.IOException
meth public !varargs void put(java.io.InputStream,int,java.lang.String,org.apache.sshd.sftp.client.SftpClient$OpenMode[]) throws java.io.IOException
meth public !varargs void put(java.io.InputStream,java.lang.String,org.apache.sshd.sftp.client.SftpClient$OpenMode[]) throws java.io.IOException
meth public !varargs void put(java.nio.file.Path,int,java.lang.String,org.apache.sshd.sftp.client.SftpClient$OpenMode[]) throws java.io.IOException
meth public !varargs void put(java.nio.file.Path,java.lang.String,org.apache.sshd.sftp.client.SftpClient$OpenMode[]) throws java.io.IOException
meth public !varargs void rename(java.lang.String,java.lang.String,org.apache.sshd.sftp.client.SftpClient$CopyMode[]) throws java.io.IOException
meth public <%0 extends org.apache.sshd.sftp.client.extensions.SftpClientExtension> {%%0} getExtension(java.lang.Class<? extends {%%0}>)
meth public abstract boolean isClosing()
meth public abstract int getVersion()
meth public abstract int read(org.apache.sshd.sftp.client.SftpClient$Handle,long,byte[],int,int,java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>) throws java.io.IOException
meth public abstract java.io.InputStream read(java.lang.String,int,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
meth public abstract java.io.OutputStream write(java.lang.String,int,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
meth public abstract java.lang.Iterable<org.apache.sshd.sftp.client.SftpClient$DirEntry> listDir(org.apache.sshd.sftp.client.SftpClient$Handle) throws java.io.IOException
meth public abstract java.lang.Iterable<org.apache.sshd.sftp.client.SftpClient$DirEntry> readDir(java.lang.String) throws java.io.IOException
meth public abstract java.lang.String canonicalPath(java.lang.String) throws java.io.IOException
meth public abstract java.lang.String readLink(java.lang.String) throws java.io.IOException
meth public abstract java.nio.channels.FileChannel openRemoteFileChannel(java.lang.String,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
meth public abstract java.nio.charset.Charset getNameDecodingCharset()
meth public abstract java.util.List<org.apache.sshd.sftp.client.SftpClient$DirEntry> readDir(org.apache.sshd.sftp.client.SftpClient$Handle,java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>) throws java.io.IOException
meth public abstract java.util.NavigableMap<java.lang.String,byte[]> getServerExtensions()
meth public abstract org.apache.sshd.sftp.client.SftpClient$Attributes lstat(java.lang.String) throws java.io.IOException
meth public abstract org.apache.sshd.sftp.client.SftpClient$Attributes stat(java.lang.String) throws java.io.IOException
meth public abstract org.apache.sshd.sftp.client.SftpClient$Attributes stat(org.apache.sshd.sftp.client.SftpClient$Handle) throws java.io.IOException
meth public abstract org.apache.sshd.sftp.client.SftpClient$CloseableHandle open(java.lang.String,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
meth public abstract org.apache.sshd.sftp.client.SftpClient$CloseableHandle openDir(java.lang.String) throws java.io.IOException
meth public abstract org.apache.sshd.sftp.client.extensions.SftpClientExtension getExtension(org.apache.sshd.sftp.client.extensions.SftpClientExtensionFactory)
meth public abstract void close(org.apache.sshd.sftp.client.SftpClient$Handle) throws java.io.IOException
meth public abstract void link(java.lang.String,java.lang.String,boolean) throws java.io.IOException
meth public abstract void lock(org.apache.sshd.sftp.client.SftpClient$Handle,long,long,int) throws java.io.IOException
meth public abstract void mkdir(java.lang.String) throws java.io.IOException
meth public abstract void put(java.io.InputStream,int,java.lang.String,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
meth public abstract void remove(java.lang.String) throws java.io.IOException
meth public abstract void rename(java.lang.String,java.lang.String,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$CopyMode>) throws java.io.IOException
meth public abstract void rmdir(java.lang.String) throws java.io.IOException
meth public abstract void setNameDecodingCharset(java.nio.charset.Charset)
meth public abstract void setStat(java.lang.String,org.apache.sshd.sftp.client.SftpClient$Attributes) throws java.io.IOException
meth public abstract void setStat(org.apache.sshd.sftp.client.SftpClient$Handle,org.apache.sshd.sftp.client.SftpClient$Attributes) throws java.io.IOException
meth public abstract void unlock(org.apache.sshd.sftp.client.SftpClient$Handle,long,long) throws java.io.IOException
meth public abstract void write(org.apache.sshd.sftp.client.SftpClient$Handle,long,byte[],int,int) throws java.io.IOException
meth public int read(org.apache.sshd.sftp.client.SftpClient$Handle,long,byte[]) throws java.io.IOException
meth public int read(org.apache.sshd.sftp.client.SftpClient$Handle,long,byte[],int,int) throws java.io.IOException
meth public int read(org.apache.sshd.sftp.client.SftpClient$Handle,long,byte[],java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>) throws java.io.IOException
meth public java.io.InputStream read(java.lang.String) throws java.io.IOException
meth public java.io.InputStream read(java.lang.String,int) throws java.io.IOException
meth public java.io.InputStream read(java.lang.String,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
meth public java.io.OutputStream write(java.lang.String) throws java.io.IOException
meth public java.io.OutputStream write(java.lang.String,int) throws java.io.IOException
meth public java.io.OutputStream write(java.lang.String,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
meth public java.lang.String getName()
meth public java.nio.channels.FileChannel openRemotePathChannel(java.lang.String,java.util.Collection<? extends java.nio.file.OpenOption>) throws java.io.IOException
meth public java.util.Collection<org.apache.sshd.sftp.client.SftpClient$DirEntry> readEntries(java.lang.String) throws java.io.IOException
meth public java.util.List<org.apache.sshd.sftp.client.SftpClient$DirEntry> readDir(org.apache.sshd.sftp.client.SftpClient$Handle) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient singleSessionInstance()
meth public org.apache.sshd.sftp.client.SftpClient$CloseableHandle open(java.lang.String) throws java.io.IOException
meth public org.apache.sshd.sftp.client.extensions.SftpClientExtension getExtension(java.lang.String)
meth public void put(java.io.InputStream,int,java.lang.String) throws java.io.IOException
meth public void put(java.io.InputStream,java.lang.String) throws java.io.IOException
meth public void put(java.io.InputStream,java.lang.String,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
meth public void put(java.nio.file.Path,int,java.lang.String) throws java.io.IOException
meth public void put(java.nio.file.Path,int,java.lang.String,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
meth public void put(java.nio.file.Path,java.lang.String) throws java.io.IOException
meth public void put(java.nio.file.Path,java.lang.String,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
meth public void rename(java.lang.String,java.lang.String) throws java.io.IOException
meth public void symLink(java.lang.String,java.lang.String) throws java.io.IOException
meth public void write(org.apache.sshd.sftp.client.SftpClient$Handle,long,byte[]) throws java.io.IOException

CLSS public final static !enum org.apache.sshd.sftp.client.SftpClient$Attribute
 outer org.apache.sshd.sftp.client.SftpClient
fld public final static org.apache.sshd.sftp.client.SftpClient$Attribute AccessTime
fld public final static org.apache.sshd.sftp.client.SftpClient$Attribute Acl
fld public final static org.apache.sshd.sftp.client.SftpClient$Attribute CreateTime
fld public final static org.apache.sshd.sftp.client.SftpClient$Attribute Extensions
fld public final static org.apache.sshd.sftp.client.SftpClient$Attribute ModifyTime
fld public final static org.apache.sshd.sftp.client.SftpClient$Attribute OwnerGroup
fld public final static org.apache.sshd.sftp.client.SftpClient$Attribute Perms
fld public final static org.apache.sshd.sftp.client.SftpClient$Attribute Size
fld public final static org.apache.sshd.sftp.client.SftpClient$Attribute UidGid
meth public static org.apache.sshd.sftp.client.SftpClient$Attribute valueOf(java.lang.String)
meth public static org.apache.sshd.sftp.client.SftpClient$Attribute[] values()
supr java.lang.Enum<org.apache.sshd.sftp.client.SftpClient$Attribute>

CLSS public static org.apache.sshd.sftp.client.SftpClient$Attributes
 outer org.apache.sshd.sftp.client.SftpClient
cons public init()
meth public boolean isDirectory()
meth public boolean isOther()
meth public boolean isRegularFile()
meth public boolean isSymbolicLink()
meth public int getGroupId()
meth public int getPermissions()
meth public int getType()
meth public int getUserId()
meth public java.lang.String getGroup()
meth public java.lang.String getOwner()
meth public java.lang.String longName()
meth public java.lang.String toString()
meth public java.nio.file.attribute.FileTime getAccessTime()
meth public java.nio.file.attribute.FileTime getCreateTime()
meth public java.nio.file.attribute.FileTime getModifyTime()
meth public java.util.List<java.nio.file.attribute.AclEntry> getAcl()
meth public java.util.Map<java.lang.String,byte[]> getExtensions()
meth public java.util.Set<org.apache.sshd.sftp.client.SftpClient$Attribute> getFlags()
meth public long getSize()
meth public org.apache.sshd.sftp.client.SftpClient$Attributes accessTime(java.nio.file.attribute.FileTime)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes accessTime(long)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes accessTime(long,java.util.concurrent.TimeUnit)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes acl(java.util.List<java.nio.file.attribute.AclEntry>)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes addFlag(org.apache.sshd.sftp.client.SftpClient$Attribute)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes createTime(java.nio.file.attribute.FileTime)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes createTime(long)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes createTime(long,java.util.concurrent.TimeUnit)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes extensions(java.util.Map<java.lang.String,byte[]>)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes group(java.lang.String)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes modifyTime(java.nio.file.attribute.FileTime)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes modifyTime(long)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes modifyTime(long,java.util.concurrent.TimeUnit)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes owner(int,int)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes owner(java.lang.String)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes perms(int)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes removeFlag(org.apache.sshd.sftp.client.SftpClient$Attribute)
meth public org.apache.sshd.sftp.client.SftpClient$Attributes size(long)
meth public void setAccessTime(java.nio.file.attribute.FileTime)
meth public void setAcl(java.util.List<java.nio.file.attribute.AclEntry>)
meth public void setCreateTime(java.nio.file.attribute.FileTime)
meth public void setExtensions(java.util.Map<java.lang.String,byte[]>)
meth public void setGroup(java.lang.String)
meth public void setLongName(java.lang.String)
meth public void setModifyTime(java.nio.file.attribute.FileTime)
meth public void setOwner(java.lang.String)
meth public void setPermissions(int)
meth public void setSize(long)
meth public void setStringExtensions(java.util.Map<java.lang.String,java.lang.String>)
meth public void setType(int)
supr java.lang.Object
hfds accessTime,acl,createTime,extensions,flags,gid,group,longName,modifyTime,owner,perms,size,type,uid

CLSS public abstract static org.apache.sshd.sftp.client.SftpClient$CloseableHandle
 outer org.apache.sshd.sftp.client.SftpClient
cons protected init(java.lang.String,byte[])
intf java.nio.channels.Channel
supr org.apache.sshd.sftp.client.SftpClient$Handle

CLSS public final static !enum org.apache.sshd.sftp.client.SftpClient$CopyMode
 outer org.apache.sshd.sftp.client.SftpClient
fld public final static org.apache.sshd.sftp.client.SftpClient$CopyMode Atomic
fld public final static org.apache.sshd.sftp.client.SftpClient$CopyMode Overwrite
meth public static org.apache.sshd.sftp.client.SftpClient$CopyMode valueOf(java.lang.String)
meth public static org.apache.sshd.sftp.client.SftpClient$CopyMode[] values()
supr java.lang.Enum<org.apache.sshd.sftp.client.SftpClient$CopyMode>

CLSS public static org.apache.sshd.sftp.client.SftpClient$DirEntry
 outer org.apache.sshd.sftp.client.SftpClient
cons public init(java.lang.String,java.lang.String,org.apache.sshd.sftp.client.SftpClient$Attributes)
cons public init(org.apache.sshd.sftp.client.SftpClient$DirEntry)
fld public final static java.util.Comparator<org.apache.sshd.sftp.client.SftpClient$DirEntry> BY_CASE_INSENSITIVE_FILENAME
fld public final static java.util.Comparator<org.apache.sshd.sftp.client.SftpClient$DirEntry> BY_CASE_SENSITIVE_FILENAME
meth public java.lang.String getFilename()
meth public java.lang.String getLongFilename()
meth public java.lang.String toString()
meth public org.apache.sshd.sftp.client.SftpClient$Attributes getAttributes()
supr java.lang.Object
hfds attributes,filename,longFilename

CLSS public static org.apache.sshd.sftp.client.SftpClient$Handle
 outer org.apache.sshd.sftp.client.SftpClient
meth public boolean equals(java.lang.Object)
meth public byte[] getIdentifier()
meth public int hashCode()
meth public int length()
meth public java.lang.String getPath()
meth public java.lang.String toString()
supr java.lang.Object
hfds id,path

CLSS public final static !enum org.apache.sshd.sftp.client.SftpClient$OpenMode
 outer org.apache.sshd.sftp.client.SftpClient
fld public final static java.util.Set<java.nio.file.OpenOption> SUPPORTED_OPTIONS
fld public final static org.apache.sshd.sftp.client.SftpClient$OpenMode Append
fld public final static org.apache.sshd.sftp.client.SftpClient$OpenMode Create
fld public final static org.apache.sshd.sftp.client.SftpClient$OpenMode Exclusive
fld public final static org.apache.sshd.sftp.client.SftpClient$OpenMode Read
fld public final static org.apache.sshd.sftp.client.SftpClient$OpenMode Truncate
fld public final static org.apache.sshd.sftp.client.SftpClient$OpenMode Write
meth public static java.util.Set<org.apache.sshd.sftp.client.SftpClient$OpenMode> fromOpenOptions(java.util.Collection<? extends java.nio.file.OpenOption>)
meth public static org.apache.sshd.sftp.client.SftpClient$OpenMode valueOf(java.lang.String)
meth public static org.apache.sshd.sftp.client.SftpClient$OpenMode[] values()
supr java.lang.Enum<org.apache.sshd.sftp.client.SftpClient$OpenMode>

CLSS public abstract interface org.apache.sshd.sftp.client.SftpClientFactory
meth public abstract org.apache.sshd.sftp.client.SftpClient createSftpClient(org.apache.sshd.client.session.ClientSession,org.apache.sshd.sftp.client.SftpVersionSelector,org.apache.sshd.sftp.client.SftpErrorDataHandler) throws java.io.IOException
meth public abstract org.apache.sshd.sftp.client.fs.SftpFileSystem createSftpFileSystem(org.apache.sshd.client.session.ClientSession,org.apache.sshd.sftp.client.SftpVersionSelector,org.apache.sshd.sftp.client.SftpErrorDataHandler,int,int) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient createSftpClient(org.apache.sshd.client.session.ClientSession) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient createSftpClient(org.apache.sshd.client.session.ClientSession,int) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient createSftpClient(org.apache.sshd.client.session.ClientSession,org.apache.sshd.sftp.client.SftpErrorDataHandler) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient createSftpClient(org.apache.sshd.client.session.ClientSession,org.apache.sshd.sftp.client.SftpVersionSelector) throws java.io.IOException
meth public org.apache.sshd.sftp.client.fs.SftpFileSystem createSftpFileSystem(org.apache.sshd.client.session.ClientSession) throws java.io.IOException
meth public org.apache.sshd.sftp.client.fs.SftpFileSystem createSftpFileSystem(org.apache.sshd.client.session.ClientSession,int) throws java.io.IOException
meth public org.apache.sshd.sftp.client.fs.SftpFileSystem createSftpFileSystem(org.apache.sshd.client.session.ClientSession,int,int) throws java.io.IOException
meth public org.apache.sshd.sftp.client.fs.SftpFileSystem createSftpFileSystem(org.apache.sshd.client.session.ClientSession,int,int,int) throws java.io.IOException
meth public org.apache.sshd.sftp.client.fs.SftpFileSystem createSftpFileSystem(org.apache.sshd.client.session.ClientSession,org.apache.sshd.sftp.client.SftpVersionSelector) throws java.io.IOException
meth public org.apache.sshd.sftp.client.fs.SftpFileSystem createSftpFileSystem(org.apache.sshd.client.session.ClientSession,org.apache.sshd.sftp.client.SftpVersionSelector,int,int) throws java.io.IOException
meth public static org.apache.sshd.sftp.client.SftpClientFactory instance()

CLSS public abstract interface org.apache.sshd.sftp.client.SftpClientHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.sftp.client.SftpClient getClient()

CLSS public abstract interface org.apache.sshd.sftp.client.SftpErrorDataHandler
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.sftp.client.SftpErrorDataHandler EMPTY
meth public abstract void errorData(byte[],int,int) throws java.io.IOException

CLSS public org.apache.sshd.sftp.client.SftpMessage
cons public init(int,org.apache.sshd.common.io.IoWriteFuture,java.time.Duration)
meth public int getId()
meth public java.time.Duration getTimeout()
meth public org.apache.sshd.common.io.IoWriteFuture getFuture()
meth public void waitUntilSent() throws java.io.IOException
supr java.lang.Object
hfds future,id,timeout

CLSS public abstract interface org.apache.sshd.sftp.client.SftpVersionSelector
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.sftp.client.SftpVersionSelector$NamedVersionSelector CURRENT
fld public final static org.apache.sshd.sftp.client.SftpVersionSelector$NamedVersionSelector MAXIMUM
fld public final static org.apache.sshd.sftp.client.SftpVersionSelector$NamedVersionSelector MINIMUM
innr public static NamedVersionSelector
meth public !varargs static org.apache.sshd.sftp.client.SftpVersionSelector$NamedVersionSelector preferredVersionSelector(int[])
meth public abstract int selectVersion(org.apache.sshd.client.session.ClientSession,boolean,int,java.util.List<java.lang.Integer>)
meth public static org.apache.sshd.sftp.client.SftpVersionSelector$NamedVersionSelector fixedVersionSelector(int)
meth public static org.apache.sshd.sftp.client.SftpVersionSelector$NamedVersionSelector preferredVersionSelector(java.lang.Iterable<? extends java.lang.Number>)
meth public static org.apache.sshd.sftp.client.SftpVersionSelector$NamedVersionSelector resolveVersionSelector(java.lang.String)

CLSS public static org.apache.sshd.sftp.client.SftpVersionSelector$NamedVersionSelector
 outer org.apache.sshd.sftp.client.SftpVersionSelector
cons public init(java.lang.String,org.apache.sshd.sftp.client.SftpVersionSelector)
fld protected final org.apache.sshd.sftp.client.SftpVersionSelector selector
intf org.apache.sshd.common.NamedResource
intf org.apache.sshd.sftp.client.SftpVersionSelector
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public int selectVersion(org.apache.sshd.client.session.ClientSession,boolean,int,java.util.List<java.lang.Integer>)
meth public java.lang.String getName()
meth public java.lang.String toString()
supr java.lang.Object
hfds name

CLSS public abstract interface org.apache.sshd.sftp.client.SimpleSftpClient
intf java.nio.channels.Channel
meth public abstract org.apache.sshd.sftp.client.SftpClient sftpLogin(java.net.SocketAddress,java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract org.apache.sshd.sftp.client.SftpClient sftpLogin(java.net.SocketAddress,java.lang.String,java.security.KeyPair) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient sftpLogin(java.lang.String,int,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient sftpLogin(java.lang.String,int,java.lang.String,java.security.KeyPair) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient sftpLogin(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient sftpLogin(java.lang.String,java.lang.String,java.security.KeyPair) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient sftpLogin(java.net.InetAddress,int,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient sftpLogin(java.net.InetAddress,int,java.lang.String,java.security.KeyPair) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient sftpLogin(java.net.InetAddress,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient sftpLogin(java.net.InetAddress,java.lang.String,java.security.KeyPair) throws java.io.IOException

CLSS public abstract !enum org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions
fld public final static java.util.Set<org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions> VALUES
fld public final static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions CHECK_FILE_HANDLE
fld public final static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions CHECK_FILE_NAME
fld public final static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions COPY_DATA
fld public final static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions COPY_FILE
fld public final static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions MD5_FILE
fld public final static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions MD5_HANDLE
fld public final static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions OPENSSH_FSYNC
fld public final static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions OPENSSH_LIMITS
fld public final static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions OPENSSH_POSIX_RENAME
fld public final static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions OPENSSH_STAT_HANDLE
fld public final static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions OPENSSH_STAT_PATH
fld public final static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions SPACE_AVAILABLE
intf org.apache.sshd.sftp.client.extensions.SftpClientExtensionFactory
meth public final java.lang.Class<? extends org.apache.sshd.sftp.client.extensions.SftpClientExtension> getType()
meth public final java.lang.String getName()
meth public static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions fromInstance(java.lang.Object)
meth public static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions fromName(java.lang.String)
meth public static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions fromType(java.lang.Class<?>)
meth public static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions valueOf(java.lang.String)
meth public static org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions[] values()
supr java.lang.Enum<org.apache.sshd.sftp.client.extensions.BuiltinSftpClientExtensions>
hfds name,type

CLSS public abstract interface org.apache.sshd.sftp.client.extensions.CheckFileHandleExtension
intf org.apache.sshd.sftp.client.extensions.SftpClientExtension
meth public abstract java.util.Map$Entry<java.lang.String,java.util.Collection<byte[]>> checkFileHandle(org.apache.sshd.sftp.client.SftpClient$Handle,java.util.Collection<java.lang.String>,long,long,int) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.sftp.client.extensions.CheckFileNameExtension
intf org.apache.sshd.sftp.client.extensions.SftpClientExtension
meth public abstract java.util.Map$Entry<java.lang.String,java.util.Collection<byte[]>> checkFileName(java.lang.String,java.util.Collection<java.lang.String>,long,long,int) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.sftp.client.extensions.CopyDataExtension
intf org.apache.sshd.sftp.client.extensions.SftpClientExtension
meth public abstract void copyData(org.apache.sshd.sftp.client.SftpClient$Handle,long,long,org.apache.sshd.sftp.client.SftpClient$Handle,long) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.sftp.client.extensions.CopyFileExtension
intf org.apache.sshd.sftp.client.extensions.SftpClientExtension
meth public abstract void copyFile(java.lang.String,java.lang.String,boolean) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.sftp.client.extensions.FilenameTranslationControlExtension
intf org.apache.sshd.sftp.client.extensions.SftpClientExtension
meth public abstract void setFilenameTranslationControl(boolean) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.sftp.client.extensions.MD5FileExtension
intf org.apache.sshd.sftp.client.extensions.SftpClientExtension
meth public abstract byte[] getHash(java.lang.String,long,long,byte[]) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.sftp.client.extensions.MD5HandleExtension
intf org.apache.sshd.sftp.client.extensions.SftpClientExtension
meth public abstract byte[] getHash(org.apache.sshd.sftp.client.SftpClient$Handle,long,long,byte[]) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.sftp.client.extensions.SftpClientExtension
intf org.apache.sshd.common.NamedResource
intf org.apache.sshd.common.OptionalFeature
intf org.apache.sshd.sftp.client.SftpClientHolder

CLSS public abstract interface org.apache.sshd.sftp.client.extensions.SftpClientExtensionFactory
intf org.apache.sshd.common.NamedResource
meth public abstract org.apache.sshd.sftp.client.extensions.SftpClientExtension create(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Map<java.lang.String,byte[]>,java.util.Map<java.lang.String,?>)
meth public org.apache.sshd.sftp.client.extensions.SftpClientExtension create(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient)

CLSS public abstract interface org.apache.sshd.sftp.client.extensions.SpaceAvailableExtension
intf org.apache.sshd.sftp.client.extensions.SftpClientExtension
meth public abstract org.apache.sshd.sftp.common.extensions.SpaceAvailableExtensionInfo available(java.lang.String) throws java.io.IOException

CLSS public abstract org.apache.sshd.sftp.client.extensions.helpers.AbstractCheckFileExtension
cons protected init(java.lang.String,org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Collection<java.lang.String>)
meth protected java.util.AbstractMap$SimpleImmutableEntry<java.lang.String,java.util.Collection<byte[]>> doGetHash(java.lang.Object,java.util.Collection<java.lang.String>,long,long,int) throws java.io.IOException
supr org.apache.sshd.sftp.client.extensions.helpers.AbstractSftpClientExtension

CLSS public abstract org.apache.sshd.sftp.client.extensions.helpers.AbstractMD5HashExtension
cons protected init(java.lang.String,org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Collection<java.lang.String>)
meth protected byte[] doGetHash(java.lang.Object,long,long,byte[]) throws java.io.IOException
supr org.apache.sshd.sftp.client.extensions.helpers.AbstractSftpClientExtension

CLSS public abstract org.apache.sshd.sftp.client.extensions.helpers.AbstractSftpClientExtension
cons protected init(java.lang.String,org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,boolean)
cons protected init(java.lang.String,org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Collection<java.lang.String>)
cons protected init(java.lang.String,org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Map<java.lang.String,byte[]>)
intf org.apache.sshd.sftp.client.RawSftpClient
intf org.apache.sshd.sftp.client.extensions.SftpClientExtension
meth protected int sendExtendedCommand(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.util.buffer.Buffer checkExtendedReplyBuffer(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.util.buffer.Buffer getCommandBuffer(int)
meth protected org.apache.sshd.common.util.buffer.Buffer getCommandBuffer(java.lang.Object)
meth protected org.apache.sshd.common.util.buffer.Buffer getCommandBuffer(java.lang.Object,int)
meth protected void checkStatus(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected void sendAndCheckExtendedCommandStatus(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected void throwStatusException(int,org.apache.sshd.sftp.client.impl.SftpStatus) throws java.io.IOException
meth public final boolean isSupported()
meth public final java.lang.String getName()
meth public final org.apache.sshd.sftp.client.SftpClient getClient()
meth public int send(int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public org.apache.sshd.common.util.buffer.Buffer putTarget(org.apache.sshd.common.util.buffer.Buffer,java.lang.Object)
meth public org.apache.sshd.common.util.buffer.Buffer receive(int) throws java.io.IOException
meth public org.apache.sshd.common.util.buffer.Buffer receive(int,java.time.Duration) throws java.io.IOException
meth public org.apache.sshd.common.util.buffer.Buffer receive(int,long) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpMessage write(int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds client,name,raw,supported

CLSS public org.apache.sshd.sftp.client.extensions.helpers.CheckFileHandleExtensionImpl
cons public init(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Collection<java.lang.String>)
intf org.apache.sshd.sftp.client.extensions.CheckFileHandleExtension
meth public java.util.AbstractMap$SimpleImmutableEntry<java.lang.String,java.util.Collection<byte[]>> checkFileHandle(org.apache.sshd.sftp.client.SftpClient$Handle,java.util.Collection<java.lang.String>,long,long,int) throws java.io.IOException
supr org.apache.sshd.sftp.client.extensions.helpers.AbstractCheckFileExtension

CLSS public org.apache.sshd.sftp.client.extensions.helpers.CheckFileNameExtensionImpl
cons public init(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Collection<java.lang.String>)
intf org.apache.sshd.sftp.client.extensions.CheckFileNameExtension
meth public java.util.AbstractMap$SimpleImmutableEntry<java.lang.String,java.util.Collection<byte[]>> checkFileName(java.lang.String,java.util.Collection<java.lang.String>,long,long,int) throws java.io.IOException
supr org.apache.sshd.sftp.client.extensions.helpers.AbstractCheckFileExtension

CLSS public org.apache.sshd.sftp.client.extensions.helpers.CopyDataExtensionImpl
cons public init(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Collection<java.lang.String>)
intf org.apache.sshd.sftp.client.extensions.CopyDataExtension
meth public void copyData(org.apache.sshd.sftp.client.SftpClient$Handle,long,long,org.apache.sshd.sftp.client.SftpClient$Handle,long) throws java.io.IOException
supr org.apache.sshd.sftp.client.extensions.helpers.AbstractSftpClientExtension

CLSS public org.apache.sshd.sftp.client.extensions.helpers.CopyFileExtensionImpl
cons public init(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Collection<java.lang.String>)
intf org.apache.sshd.sftp.client.extensions.CopyFileExtension
meth public void copyFile(java.lang.String,java.lang.String,boolean) throws java.io.IOException
supr org.apache.sshd.sftp.client.extensions.helpers.AbstractSftpClientExtension

CLSS public org.apache.sshd.sftp.client.extensions.helpers.FilenameTranslationControlExtensionImpl
cons public init(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Collection<java.lang.String>)
cons public init(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Map<java.lang.String,byte[]>)
intf org.apache.sshd.sftp.client.extensions.FilenameTranslationControlExtension
meth public void setFilenameTranslationControl(boolean) throws java.io.IOException
supr org.apache.sshd.sftp.client.extensions.helpers.AbstractSftpClientExtension

CLSS public org.apache.sshd.sftp.client.extensions.helpers.MD5FileExtensionImpl
cons public init(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Collection<java.lang.String>)
intf org.apache.sshd.sftp.client.extensions.MD5FileExtension
meth public byte[] getHash(java.lang.String,long,long,byte[]) throws java.io.IOException
supr org.apache.sshd.sftp.client.extensions.helpers.AbstractMD5HashExtension

CLSS public org.apache.sshd.sftp.client.extensions.helpers.MD5HandleExtensionImpl
cons public init(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Collection<java.lang.String>)
intf org.apache.sshd.sftp.client.extensions.MD5HandleExtension
meth public byte[] getHash(org.apache.sshd.sftp.client.SftpClient$Handle,long,long,byte[]) throws java.io.IOException
supr org.apache.sshd.sftp.client.extensions.helpers.AbstractMD5HashExtension

CLSS public org.apache.sshd.sftp.client.extensions.helpers.SpaceAvailableExtensionImpl
cons public init(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Collection<java.lang.String>)
intf org.apache.sshd.sftp.client.extensions.SpaceAvailableExtension
meth public org.apache.sshd.sftp.common.extensions.SpaceAvailableExtensionInfo available(java.lang.String) throws java.io.IOException
supr org.apache.sshd.sftp.client.extensions.helpers.AbstractSftpClientExtension

CLSS public abstract interface org.apache.sshd.sftp.client.extensions.openssh.OpenSSHFsyncExtension
intf org.apache.sshd.sftp.client.extensions.SftpClientExtension
meth public abstract void fsync(org.apache.sshd.sftp.client.SftpClient$Handle) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.sftp.client.extensions.openssh.OpenSSHLimitsExtension
intf org.apache.sshd.sftp.client.extensions.SftpClientExtension
meth public abstract org.apache.sshd.sftp.client.extensions.openssh.OpenSSHLimitsExtensionInfo limits() throws java.io.IOException

CLSS public org.apache.sshd.sftp.client.extensions.openssh.OpenSSHLimitsExtensionInfo
cons public init()
cons public init(org.apache.sshd.common.PropertyResolver)
cons public init(org.apache.sshd.common.util.buffer.Buffer)
fld public long maxOpenHandles
fld public long maxPacketLength
fld public long maxReadLength
fld public long maxWriteLength
intf java.lang.Cloneable
meth public <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} encode({%%0})
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.apache.sshd.sftp.client.extensions.openssh.OpenSSHLimitsExtensionInfo clone()
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} encode({%%0},org.apache.sshd.sftp.client.extensions.openssh.OpenSSHLimitsExtensionInfo)
meth public static <%0 extends org.apache.sshd.sftp.client.extensions.openssh.OpenSSHLimitsExtensionInfo> {%%0} decode(org.apache.sshd.common.util.buffer.Buffer,{%%0})
meth public static <%0 extends org.apache.sshd.sftp.client.extensions.openssh.OpenSSHLimitsExtensionInfo> {%%0} fill(org.apache.sshd.common.PropertyResolver,{%%0})
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.sftp.client.extensions.openssh.OpenSSHPosixRenameExtension
intf org.apache.sshd.sftp.client.extensions.SftpClientExtension
meth public abstract void posixRename(java.lang.String,java.lang.String) throws java.io.IOException

CLSS public org.apache.sshd.sftp.client.extensions.openssh.OpenSSHStatExtensionInfo
cons public init()
cons public init(org.apache.sshd.common.util.buffer.Buffer)
fld public final static long SSH_FXE_STATVFS_ST_NOSUID = 2
fld public final static long SSH_FXE_STATVFS_ST_RDONLY = 1
fld public long f_bavail
fld public long f_bfree
fld public long f_blocks
fld public long f_bsize
fld public long f_favail
fld public long f_ffree
fld public long f_files
fld public long f_flag
fld public long f_frsize
fld public long f_fsid
fld public long f_namemax
intf java.lang.Cloneable
meth public <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} encode({%%0})
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.apache.sshd.sftp.client.extensions.openssh.OpenSSHStatExtensionInfo clone()
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} encode({%%0},org.apache.sshd.sftp.client.extensions.openssh.OpenSSHStatExtensionInfo)
meth public static <%0 extends org.apache.sshd.sftp.client.extensions.openssh.OpenSSHStatExtensionInfo> {%%0} decode(org.apache.sshd.common.util.buffer.Buffer,{%%0})
meth public static org.apache.sshd.sftp.client.extensions.openssh.OpenSSHStatExtensionInfo decode(org.apache.sshd.common.util.buffer.Buffer)
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.sftp.client.extensions.openssh.OpenSSHStatHandleExtension
intf org.apache.sshd.sftp.client.extensions.SftpClientExtension
meth public abstract org.apache.sshd.sftp.client.extensions.openssh.OpenSSHStatExtensionInfo stat(org.apache.sshd.sftp.client.SftpClient$Handle) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.sftp.client.extensions.openssh.OpenSSHStatPathExtension
intf org.apache.sshd.sftp.client.extensions.SftpClientExtension
meth public abstract org.apache.sshd.sftp.client.extensions.openssh.OpenSSHStatExtensionInfo stat(java.lang.String) throws java.io.IOException

CLSS public abstract org.apache.sshd.sftp.client.extensions.openssh.helpers.AbstractOpenSSHStatCommandExtension
cons protected init(java.lang.String,org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Map<java.lang.String,byte[]>)
meth protected org.apache.sshd.sftp.client.extensions.openssh.OpenSSHStatExtensionInfo doGetStat(java.lang.Object) throws java.io.IOException
supr org.apache.sshd.sftp.client.extensions.helpers.AbstractSftpClientExtension

CLSS public org.apache.sshd.sftp.client.extensions.openssh.helpers.OpenSSHFsyncExtensionImpl
cons public init(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Map<java.lang.String,byte[]>)
intf org.apache.sshd.sftp.client.extensions.openssh.OpenSSHFsyncExtension
meth public void fsync(org.apache.sshd.sftp.client.SftpClient$Handle) throws java.io.IOException
supr org.apache.sshd.sftp.client.extensions.helpers.AbstractSftpClientExtension

CLSS public org.apache.sshd.sftp.client.extensions.openssh.helpers.OpenSSHLimitsExtensionImpl
cons public init(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Map<java.lang.String,byte[]>)
intf org.apache.sshd.sftp.client.extensions.openssh.OpenSSHLimitsExtension
meth public org.apache.sshd.sftp.client.extensions.openssh.OpenSSHLimitsExtensionInfo limits() throws java.io.IOException
supr org.apache.sshd.sftp.client.extensions.helpers.AbstractSftpClientExtension

CLSS public org.apache.sshd.sftp.client.extensions.openssh.helpers.OpenSSHPosixRenameExtensionImpl
cons public init(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Map<java.lang.String,byte[]>)
intf org.apache.sshd.sftp.client.extensions.openssh.OpenSSHPosixRenameExtension
meth public void posixRename(java.lang.String,java.lang.String) throws java.io.IOException
supr org.apache.sshd.sftp.client.extensions.helpers.AbstractSftpClientExtension

CLSS public org.apache.sshd.sftp.client.extensions.openssh.helpers.OpenSSHStatHandleExtensionImpl
cons public init(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Map<java.lang.String,byte[]>)
intf org.apache.sshd.sftp.client.extensions.openssh.OpenSSHStatHandleExtension
meth public org.apache.sshd.sftp.client.extensions.openssh.OpenSSHStatExtensionInfo stat(org.apache.sshd.sftp.client.SftpClient$Handle) throws java.io.IOException
supr org.apache.sshd.sftp.client.extensions.openssh.helpers.AbstractOpenSSHStatCommandExtension

CLSS public org.apache.sshd.sftp.client.extensions.openssh.helpers.OpenSSHStatPathExtensionImpl
cons public init(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.RawSftpClient,java.util.Map<java.lang.String,byte[]>)
intf org.apache.sshd.sftp.client.extensions.openssh.OpenSSHStatPathExtension
meth public org.apache.sshd.sftp.client.extensions.openssh.OpenSSHStatExtensionInfo stat(java.lang.String) throws java.io.IOException
supr org.apache.sshd.sftp.client.extensions.openssh.helpers.AbstractOpenSSHStatCommandExtension

CLSS public org.apache.sshd.sftp.client.fs.SftpAclFileAttributeView
cons public !varargs init(org.apache.sshd.sftp.client.fs.SftpFileSystemProvider,java.nio.file.Path,java.nio.file.LinkOption[])
intf java.nio.file.attribute.AclFileAttributeView
meth public java.lang.String name()
meth public java.nio.file.attribute.UserPrincipal getOwner() throws java.io.IOException
meth public java.util.List<java.nio.file.attribute.AclEntry> getAcl() throws java.io.IOException
meth public void setAcl(java.util.List<java.nio.file.attribute.AclEntry>) throws java.io.IOException
meth public void setOwner(java.nio.file.attribute.UserPrincipal) throws java.io.IOException
supr org.apache.sshd.sftp.client.impl.AbstractSftpFileAttributeView

CLSS public org.apache.sshd.sftp.client.fs.SftpClientDirectoryScanner
cons public !varargs init(java.lang.String,java.lang.String[])
cons public init()
cons public init(boolean)
cons public init(java.lang.String)
cons public init(java.lang.String,java.util.Collection<java.lang.String>)
fld protected java.lang.String basedir
innr public static ScanDirEntry
meth protected <%0 extends java.util.Collection<org.apache.sshd.sftp.client.fs.SftpClientDirectoryScanner$ScanDirEntry>> {%%0} scandir(org.apache.sshd.sftp.client.SftpClient,java.lang.String,java.lang.String,{%%0}) throws java.io.IOException
meth protected java.lang.String createRelativePath(java.lang.String,java.lang.String)
meth public <%0 extends java.util.Collection<org.apache.sshd.sftp.client.fs.SftpClientDirectoryScanner$ScanDirEntry>> {%%0} scan(org.apache.sshd.sftp.client.SftpClient,java.util.function.Supplier<? extends {%%0}>) throws java.io.IOException
meth public boolean isFilesOnly()
meth public java.lang.String getBasedir()
meth public java.lang.String getSeparator()
meth public java.util.Collection<org.apache.sshd.sftp.client.fs.SftpClientDirectoryScanner$ScanDirEntry> scan(org.apache.sshd.sftp.client.SftpClient) throws java.io.IOException
meth public void setBasedir(java.lang.String)
meth public void setFilesOnly(boolean)
meth public void setIncludes(java.util.Collection<java.lang.String>)
meth public void setSeparator(java.lang.String)
supr org.apache.sshd.common.util.io.PathScanningMatcher
hfds filesOnly

CLSS public static org.apache.sshd.sftp.client.fs.SftpClientDirectoryScanner$ScanDirEntry
 outer org.apache.sshd.sftp.client.fs.SftpClientDirectoryScanner
cons public init(java.lang.String,java.lang.String,org.apache.sshd.sftp.client.SftpClient$DirEntry)
meth public java.lang.String getFullPath()
meth public java.lang.String getRelativePath()
meth public java.lang.String toString()
supr org.apache.sshd.sftp.client.SftpClient$DirEntry
hfds fullPath,relativePath

CLSS public org.apache.sshd.sftp.client.fs.SftpDirectoryStream
cons public init(org.apache.sshd.sftp.client.fs.SftpPath) throws java.io.IOException
cons public init(org.apache.sshd.sftp.client.fs.SftpPath,java.nio.file.DirectoryStream$Filter<? super java.nio.file.Path>) throws java.io.IOException
fld protected boolean pathIteratorConsumed
fld protected org.apache.sshd.sftp.client.fs.SftpPathIterator pathIterator
intf java.nio.file.DirectoryStream<java.nio.file.Path>
intf org.apache.sshd.sftp.client.SftpClientHolder
meth public final java.nio.file.DirectoryStream$Filter<? super java.nio.file.Path> getFilter()
meth public final org.apache.sshd.sftp.client.SftpClient getClient()
meth public final org.apache.sshd.sftp.client.fs.SftpPath getRootPath()
meth public java.util.Iterator<java.nio.file.Path> iterator()
meth public void close() throws java.io.IOException
supr java.lang.Object
hfds filter,path,sftp

CLSS public org.apache.sshd.sftp.client.fs.SftpFileStore
cons public init(java.lang.String,org.apache.sshd.sftp.client.fs.SftpFileSystem)
meth public <%0 extends java.nio.file.attribute.FileStoreAttributeView> {%%0} getFileStoreAttributeView(java.lang.Class<{%%0}>)
meth public boolean isReadOnly()
meth public boolean supportsFileAttributeView(java.lang.Class<? extends java.nio.file.attribute.FileAttributeView>)
meth public boolean supportsFileAttributeView(java.lang.String)
meth public final org.apache.sshd.sftp.client.fs.SftpFileSystem getFileSystem()
meth public java.lang.Object getAttribute(java.lang.String) throws java.io.IOException
meth public java.lang.String name()
meth public java.lang.String type()
meth public long getTotalSpace() throws java.io.IOException
meth public long getUnallocatedSpace() throws java.io.IOException
meth public long getUsableSpace() throws java.io.IOException
supr java.nio.file.FileStore
hfds fs,name

CLSS public org.apache.sshd.sftp.client.fs.SftpFileSystem
cons protected init(org.apache.sshd.sftp.client.fs.SftpFileSystemProvider,java.lang.String,org.apache.sshd.sftp.client.SftpClientFactory,org.apache.sshd.sftp.client.SftpVersionSelector,org.apache.sshd.sftp.client.SftpErrorDataHandler)
cons public init(org.apache.sshd.sftp.client.fs.SftpFileSystemProvider,java.lang.String,org.apache.sshd.client.session.ClientSession,org.apache.sshd.sftp.client.SftpClientFactory,org.apache.sshd.sftp.client.SftpVersionSelector,org.apache.sshd.sftp.client.SftpErrorDataHandler) throws java.io.IOException
fld public final static java.util.NavigableSet<java.lang.String> UNIVERSAL_SUPPORTED_VIEWS
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<java.lang.Boolean> OWNED_SESSION
innr public static DefaultGroupPrincipal
innr public static DefaultUserPrincipal
innr public static DefaultUserPrincipalLookupService
intf org.apache.sshd.client.session.ClientSessionHolder
intf org.apache.sshd.common.session.SessionHolder<org.apache.sshd.client.session.ClientSession>
meth protected org.apache.sshd.client.session.ClientSession sessionForSftpClient() throws java.io.IOException
meth protected org.apache.sshd.sftp.client.fs.SftpPath create(java.lang.String,java.util.List<java.lang.String>)
meth protected void init() throws java.io.IOException
meth protected void setClientSession(org.apache.sshd.client.session.ClientSession)
meth public boolean isOpen()
meth public final int getVersion()
meth public final java.lang.String getId()
meth public final org.apache.sshd.sftp.client.SftpVersionSelector getSftpVersionSelector()
meth public int getReadBufferSize()
meth public int getWriteBufferSize()
meth public java.lang.String toString()
meth public java.nio.file.attribute.UserPrincipalLookupService getUserPrincipalLookupService()
meth public java.util.List<java.nio.file.FileStore> getFileStores()
meth public java.util.Set<java.lang.String> supportedFileAttributeViews()
meth public org.apache.sshd.client.session.ClientSession getClientSession()
meth public org.apache.sshd.client.session.ClientSession getSession()
meth public org.apache.sshd.sftp.client.SftpClient getClient() throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpErrorDataHandler getSftpErrorDataHandler()
meth public org.apache.sshd.sftp.client.fs.SftpFileSystemProvider provider()
meth public org.apache.sshd.sftp.client.fs.SftpPath getDefaultDir()
meth public void close() throws java.io.IOException
meth public void setReadBufferSize(int)
meth public void setWriteBufferSize(int)
supr org.apache.sshd.common.file.util.BaseFileSystem<org.apache.sshd.sftp.client.fs.SftpPath>
hfds clientSession,errorDataHandler,factory,id,open,readBufferSize,selector,sftp,stores,writeBufferSize
hcls DelegatingClient,SftpClientEnriched,Wrapper

CLSS public static org.apache.sshd.sftp.client.fs.SftpFileSystem$DefaultGroupPrincipal
 outer org.apache.sshd.sftp.client.fs.SftpFileSystem
cons public init(java.lang.String)
intf java.nio.file.attribute.GroupPrincipal
supr org.apache.sshd.sftp.client.fs.SftpFileSystem$DefaultUserPrincipal

CLSS public static org.apache.sshd.sftp.client.fs.SftpFileSystem$DefaultUserPrincipal
 outer org.apache.sshd.sftp.client.fs.SftpFileSystem
cons public init(java.lang.String)
intf java.nio.file.attribute.UserPrincipal
meth public boolean equals(java.lang.Object)
meth public final java.lang.String getName()
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds name

CLSS public static org.apache.sshd.sftp.client.fs.SftpFileSystem$DefaultUserPrincipalLookupService
 outer org.apache.sshd.sftp.client.fs.SftpFileSystem
cons public init()
fld public final static org.apache.sshd.sftp.client.fs.SftpFileSystem$DefaultUserPrincipalLookupService INSTANCE
meth public java.nio.file.attribute.GroupPrincipal lookupPrincipalByGroupName(java.lang.String) throws java.io.IOException
meth public java.nio.file.attribute.UserPrincipal lookupPrincipalByName(java.lang.String) throws java.io.IOException
supr java.nio.file.attribute.UserPrincipalLookupService

CLSS public org.apache.sshd.sftp.client.fs.SftpFileSystemAutomatic
cons public init(org.apache.sshd.sftp.client.fs.SftpFileSystemProvider,java.lang.String,org.apache.sshd.common.util.io.functors.IOFunction<java.lang.Boolean,org.apache.sshd.client.session.ClientSession>,org.apache.sshd.sftp.client.SftpClientFactory,org.apache.sshd.sftp.client.SftpVersionSelector,org.apache.sshd.sftp.client.SftpErrorDataHandler) throws java.io.IOException
meth protected org.apache.sshd.client.session.ClientSession sessionForSftpClient() throws java.io.IOException
meth public org.apache.sshd.client.session.ClientSession getClientSession()
supr org.apache.sshd.sftp.client.fs.SftpFileSystem
hfds sessionProvider

CLSS public abstract interface org.apache.sshd.sftp.client.fs.SftpFileSystemClientSessionInitializer
fld public final static org.apache.sshd.sftp.client.fs.SftpFileSystemClientSessionInitializer DEFAULT
meth public org.apache.sshd.client.session.ClientSession createClientSession(org.apache.sshd.sftp.client.fs.SftpFileSystemProvider,org.apache.sshd.sftp.client.fs.SftpFileSystemInitializationContext) throws java.io.IOException
meth public org.apache.sshd.sftp.client.fs.SftpFileSystem createSftpFileSystem(org.apache.sshd.sftp.client.fs.SftpFileSystemProvider,org.apache.sshd.sftp.client.fs.SftpFileSystemInitializationContext,org.apache.sshd.client.session.ClientSession,org.apache.sshd.sftp.client.SftpVersionSelector,org.apache.sshd.sftp.client.SftpErrorDataHandler) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public org.apache.sshd.sftp.client.fs.SftpFileSystem createSftpFileSystem(org.apache.sshd.sftp.client.fs.SftpFileSystemProvider,org.apache.sshd.sftp.client.fs.SftpFileSystemInitializationContext,org.apache.sshd.common.util.io.functors.IOFunction<java.lang.Boolean,org.apache.sshd.client.session.ClientSession>,org.apache.sshd.sftp.client.SftpVersionSelector,org.apache.sshd.sftp.client.SftpErrorDataHandler) throws java.io.IOException
meth public void authenticateClientSession(org.apache.sshd.sftp.client.fs.SftpFileSystemProvider,org.apache.sshd.sftp.client.fs.SftpFileSystemInitializationContext,org.apache.sshd.client.session.ClientSession) throws java.io.IOException

CLSS public org.apache.sshd.sftp.client.fs.SftpFileSystemInitializationContext
cons public init(java.lang.String,java.net.URI,java.util.Map<java.lang.String,?>)
meth public int getPort()
meth public java.lang.String getHost()
meth public java.lang.String getId()
meth public java.lang.String toString()
meth public java.net.URI getUri()
meth public java.time.Duration getMaxAuthTime()
meth public java.time.Duration getMaxConnectTime()
meth public java.util.Map<java.lang.String,?> getEnvironment()
meth public org.apache.sshd.common.PropertyResolver getPropertyResolver()
meth public org.apache.sshd.common.auth.BasicCredentialsProvider getCredentials()
meth public void setCredentials(org.apache.sshd.common.auth.BasicCredentialsProvider)
meth public void setHost(java.lang.String)
meth public void setMaxAuthTime(java.time.Duration)
meth public void setMaxConnectTime(java.time.Duration)
meth public void setPort(int)
meth public void setPropertyResolver(org.apache.sshd.common.PropertyResolver)
supr java.lang.Object
hfds credentials,environment,host,id,maxAuthTime,maxConnectTime,port,propertyResolver,uri

CLSS public org.apache.sshd.sftp.client.fs.SftpFileSystemProvider
cons public init()
cons public init(org.apache.sshd.client.SshClient)
cons public init(org.apache.sshd.client.SshClient,org.apache.sshd.sftp.client.SftpClientFactory,org.apache.sshd.sftp.client.SftpVersionSelector)
cons public init(org.apache.sshd.client.SshClient,org.apache.sshd.sftp.client.SftpClientFactory,org.apache.sshd.sftp.client.SftpVersionSelector,org.apache.sshd.sftp.client.SftpErrorDataHandler)
cons public init(org.apache.sshd.client.SshClient,org.apache.sshd.sftp.client.SftpVersionSelector)
cons public init(org.apache.sshd.client.SshClient,org.apache.sshd.sftp.client.SftpVersionSelector,org.apache.sshd.sftp.client.SftpErrorDataHandler)
cons public init(org.apache.sshd.sftp.client.SftpVersionSelector)
fld protected final org.slf4j.Logger log
fld public final static java.lang.String VERSION_PARAM = "version"
fld public final static java.util.Set<java.lang.Class<? extends java.nio.file.attribute.FileAttributeView>> UNIVERSAL_SUPPORTED_VIEWS
meth protected !varargs java.util.Map<java.lang.String,java.lang.Object> readCustomViewAttributes(org.apache.sshd.sftp.client.fs.SftpPath,java.lang.String,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs java.util.NavigableMap<java.lang.String,java.lang.Object> readAclViewAttributes(org.apache.sshd.sftp.client.fs.SftpPath,java.lang.String,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs java.util.NavigableMap<java.lang.String,java.lang.Object> readPosixViewAttributes(org.apache.sshd.sftp.client.fs.SftpPath,java.lang.String,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs org.apache.sshd.sftp.client.SftpClient$Attributes resolveRemoteFileAttributes(org.apache.sshd.sftp.client.fs.SftpPath,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected int attributesToPermissions(java.nio.file.Path,java.util.Collection<java.nio.file.attribute.PosixFilePermission>)
meth protected org.apache.sshd.sftp.client.SftpErrorDataHandler resolveSftpErrorDataHandler(java.net.URI,org.apache.sshd.sftp.client.SftpErrorDataHandler,org.apache.sshd.common.PropertyResolver)
meth protected org.apache.sshd.sftp.client.SftpVersionSelector resolveSftpVersionSelector(java.net.URI,org.apache.sshd.sftp.client.SftpVersionSelector,org.apache.sshd.common.PropertyResolver)
meth public !varargs <%0 extends java.nio.file.attribute.BasicFileAttributes> {%%0} readAttributes(java.nio.file.Path,java.lang.Class<{%%0}>,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs <%0 extends java.nio.file.attribute.FileAttributeView> {%%0} getFileAttributeView(java.nio.file.Path,java.lang.Class<{%%0}>,java.nio.file.LinkOption[])
meth public !varargs java.io.InputStream newInputStream(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs java.io.OutputStream newOutputStream(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs java.nio.channels.FileChannel newByteChannel(java.nio.file.Path,java.util.Set<? extends java.nio.file.OpenOption>,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs java.nio.channels.FileChannel newFileChannel(java.nio.file.Path,java.util.Set<? extends java.nio.file.OpenOption>,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs java.util.Map<java.lang.String,java.lang.Object> readAttributes(java.nio.file.Path,java.lang.String,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs java.util.Map<java.lang.String,java.lang.Object> readAttributes(java.nio.file.Path,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs org.apache.sshd.sftp.client.SftpClient$Attributes readRemoteAttributes(org.apache.sshd.sftp.client.fs.SftpPath,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void checkAccess(java.nio.file.Path,java.nio.file.AccessMode[]) throws java.io.IOException
meth public !varargs void copy(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption[]) throws java.io.IOException
meth public !varargs void createDirectory(java.nio.file.Path,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs void createSymbolicLink(java.nio.file.Path,java.nio.file.Path,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs void move(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption[]) throws java.io.IOException
meth public !varargs void setAttribute(java.nio.file.Path,java.lang.String,java.lang.Object,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void setAttribute(java.nio.file.Path,java.lang.String,java.lang.String,java.lang.Object,java.nio.file.LinkOption[]) throws java.io.IOException
meth public boolean isHidden(java.nio.file.Path) throws java.io.IOException
meth public boolean isSameFile(java.nio.file.Path,java.nio.file.Path) throws java.io.IOException
meth public boolean isSupportedFileAttributeView(java.nio.file.Path,java.lang.Class<? extends java.nio.file.attribute.FileAttributeView>)
meth public boolean isSupportedFileAttributeView(org.apache.sshd.sftp.client.fs.SftpFileSystem,java.lang.Class<? extends java.nio.file.attribute.FileAttributeView>)
meth public final org.apache.sshd.client.SshClient getClientInstance()
meth public final org.apache.sshd.sftp.client.SftpVersionSelector getSftpVersionSelector()
meth public java.lang.String getScheme()
meth public java.nio.file.DirectoryStream<java.nio.file.Path> newDirectoryStream(java.nio.file.Path,java.nio.file.DirectoryStream$Filter<? super java.nio.file.Path>) throws java.io.IOException
meth public java.nio.file.FileStore getFileStore(java.nio.file.Path) throws java.io.IOException
meth public java.nio.file.FileSystem getFileSystem(java.net.URI)
meth public java.nio.file.Path getPath(java.net.URI)
meth public java.nio.file.Path readSymbolicLink(java.nio.file.Path) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClientFactory getSftpClientFactory()
meth public org.apache.sshd.sftp.client.SftpErrorDataHandler getSftpErrorDataHandler()
meth public org.apache.sshd.sftp.client.fs.SftpFileSystem getFileSystem(java.lang.String)
meth public org.apache.sshd.sftp.client.fs.SftpFileSystem newFileSystem(java.net.URI,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public org.apache.sshd.sftp.client.fs.SftpFileSystem newFileSystem(org.apache.sshd.client.session.ClientSession) throws java.io.IOException
meth public org.apache.sshd.sftp.client.fs.SftpFileSystem removeFileSystem(java.lang.String)
meth public org.apache.sshd.sftp.client.fs.SftpFileSystemClientSessionInitializer getSftpFileSystemClientSessionInitializer()
meth public org.apache.sshd.sftp.client.fs.SftpPath toSftpPath(java.nio.file.Path)
meth public static java.lang.String encodeCredentials(java.lang.String,java.lang.String)
meth public static java.lang.String getFileSystemIdentifier(java.lang.String,int,java.lang.String)
meth public static java.lang.String getFileSystemIdentifier(java.net.URI)
meth public static java.lang.String getFileSystemIdentifier(org.apache.sshd.client.session.ClientSession)
meth public static java.lang.String getOctalPermissions(int)
meth public static java.lang.String getOctalPermissions(java.util.Collection<java.nio.file.attribute.PosixFilePermission>)
meth public static java.lang.String getRWXPermissions(int)
meth public static java.net.URI createFileSystemURI(java.lang.String,int,java.lang.String,java.lang.String)
meth public static java.net.URI createFileSystemURI(java.lang.String,int,java.lang.String,java.lang.String,java.util.Map<java.lang.String,?>)
meth public static java.util.Map<java.lang.String,java.lang.Object> parseURIParameters(java.lang.String)
meth public static java.util.Map<java.lang.String,java.lang.Object> parseURIParameters(java.net.URI)
meth public static java.util.Map<java.lang.String,java.lang.Object> resolveFileSystemParameters(java.util.Map<java.lang.String,?>,java.util.Map<java.lang.String,java.lang.Object>)
meth public static java.util.Set<java.nio.file.attribute.PosixFilePermission> permissionsToAttributes(int)
meth public static org.apache.sshd.common.auth.MutableBasicCredentials parseCredentials(java.lang.String)
meth public static org.apache.sshd.common.auth.MutableBasicCredentials parseCredentials(java.net.URI)
meth public void delete(java.nio.file.Path) throws java.io.IOException
meth public void setSftpFileSystemClientSessionInitializer(org.apache.sshd.sftp.client.fs.SftpFileSystemClientSessionInitializer)
supr java.nio.file.spi.FileSystemProvider
hfds clientInstance,errorDataHandler,factory,fileSystems,fsSessionInitializer,versionSelector
hcls SessionProvider

CLSS public org.apache.sshd.sftp.client.fs.SftpPath
cons public init(org.apache.sshd.sftp.client.fs.SftpFileSystem,java.lang.String,java.util.List<java.lang.String>)
intf org.apache.sshd.sftp.client.fs.WithFileAttributes
meth public !varargs org.apache.sshd.sftp.client.fs.SftpPath toRealPath(java.nio.file.LinkOption[]) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient$Attributes getAttributes()
supr org.apache.sshd.common.file.util.BasePath<org.apache.sshd.sftp.client.fs.SftpPath,org.apache.sshd.sftp.client.fs.SftpFileSystem>

CLSS public org.apache.sshd.sftp.client.fs.SftpPathDirectoryScanner
cons public !varargs init(java.nio.file.Path,java.lang.String[])
cons public init()
cons public init(boolean)
cons public init(java.nio.file.Path)
cons public init(java.nio.file.Path,java.util.Collection<java.lang.String>)
meth public java.lang.String getSeparator()
meth public static java.lang.String adjustPattern(java.lang.String)
meth public void setIncludes(java.util.Collection<java.lang.String>)
meth public void setSeparator(java.lang.String)
supr org.apache.sshd.common.util.io.DirectoryScanner

CLSS public org.apache.sshd.sftp.client.fs.SftpPathIterator
cons public init(org.apache.sshd.sftp.client.fs.SftpPath,java.lang.Iterable<? extends org.apache.sshd.sftp.client.SftpClient$DirEntry>)
cons public init(org.apache.sshd.sftp.client.fs.SftpPath,java.lang.Iterable<? extends org.apache.sshd.sftp.client.SftpClient$DirEntry>,java.nio.file.DirectoryStream$Filter<? super java.nio.file.Path>)
cons public init(org.apache.sshd.sftp.client.fs.SftpPath,java.util.Iterator<? extends org.apache.sshd.sftp.client.SftpClient$DirEntry>)
cons public init(org.apache.sshd.sftp.client.fs.SftpPath,java.util.Iterator<? extends org.apache.sshd.sftp.client.SftpClient$DirEntry>,java.nio.file.DirectoryStream$Filter<? super java.nio.file.Path>)
fld protected boolean dotIgnored
fld protected boolean dotdotIgnored
fld protected java.util.Iterator<? extends org.apache.sshd.sftp.client.SftpClient$DirEntry> it
fld protected org.apache.sshd.sftp.client.fs.SftpPath curEntry
intf java.util.Iterator<java.nio.file.Path>
meth protected org.apache.sshd.sftp.client.fs.SftpPath nextEntry(org.apache.sshd.sftp.client.fs.SftpPath,java.nio.file.DirectoryStream$Filter<? super java.nio.file.Path>)
meth public boolean hasNext()
meth public final java.nio.file.DirectoryStream$Filter<? super java.nio.file.Path> getFilter()
meth public final org.apache.sshd.sftp.client.fs.SftpPath getRootPath()
meth public final void close() throws java.io.IOException
meth public java.nio.file.Path next()
meth public void remove()
supr java.lang.Object
hfds filter,path,withDots

CLSS public org.apache.sshd.sftp.client.fs.SftpPosixFileAttributeView
cons public !varargs init(org.apache.sshd.sftp.client.fs.SftpFileSystemProvider,java.nio.file.Path,java.nio.file.LinkOption[])
intf java.nio.file.attribute.PosixFileAttributeView
meth public java.lang.String name()
meth public java.nio.file.attribute.PosixFileAttributes readAttributes() throws java.io.IOException
meth public java.nio.file.attribute.UserPrincipal getOwner() throws java.io.IOException
meth public void setGroup(java.nio.file.attribute.GroupPrincipal) throws java.io.IOException
meth public void setOwner(java.nio.file.attribute.UserPrincipal) throws java.io.IOException
meth public void setPermissions(java.util.Set<java.nio.file.attribute.PosixFilePermission>) throws java.io.IOException
meth public void setTimes(java.nio.file.attribute.FileTime,java.nio.file.attribute.FileTime,java.nio.file.attribute.FileTime) throws java.io.IOException
supr org.apache.sshd.sftp.client.impl.AbstractSftpFileAttributeView

CLSS public org.apache.sshd.sftp.client.fs.SftpPosixFileAttributes
cons public init(java.nio.file.Path,org.apache.sshd.sftp.client.SftpClient$Attributes)
intf java.nio.file.attribute.PosixFileAttributes
meth public boolean isDirectory()
meth public boolean isOther()
meth public boolean isRegularFile()
meth public boolean isSymbolicLink()
meth public final java.nio.file.Path getPath()
meth public java.lang.Object fileKey()
meth public java.nio.file.attribute.FileTime creationTime()
meth public java.nio.file.attribute.FileTime lastAccessTime()
meth public java.nio.file.attribute.FileTime lastModifiedTime()
meth public java.nio.file.attribute.GroupPrincipal group()
meth public java.nio.file.attribute.UserPrincipal owner()
meth public java.util.Set<java.nio.file.attribute.PosixFilePermission> permissions()
meth public long size()
supr java.lang.Object
hfds attributes,path

CLSS public abstract interface org.apache.sshd.sftp.client.fs.WithFileAttributeCache
intf org.apache.sshd.sftp.client.fs.WithFileAttributes
meth public abstract <%0 extends java.lang.Object> {%%0} withAttributeCache(org.apache.sshd.common.util.io.functors.IOFunction<java.nio.file.Path,{%%0}>) throws java.io.IOException
meth public abstract void setAttributes(org.apache.sshd.sftp.client.SftpClient$Attributes)
meth public static <%0 extends java.lang.Object> {%%0} withAttributeCache(java.nio.file.Path,org.apache.sshd.common.util.io.functors.IOFunction<java.nio.file.Path,{%%0}>) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.sftp.client.fs.WithFileAttributes
meth public abstract org.apache.sshd.sftp.client.SftpClient$Attributes getAttributes()

CLSS public final org.apache.sshd.sftp.client.fs.impl.SftpUtils
fld public final static java.lang.ThreadLocal<java.lang.Boolean> DIRECTORY_WITH_DOTS
supr java.lang.Object

CLSS public abstract org.apache.sshd.sftp.client.impl.AbstractSftpClient
cons protected init(org.apache.sshd.sftp.client.SftpErrorDataHandler)
fld protected final org.apache.sshd.sftp.client.SftpErrorDataHandler errorDataHandler
fld public final static int INIT_COMMAND_SIZE = 5
fld public final static org.apache.sshd.common.Property<java.time.Duration> SFTP_CLIENT_CMD_TIMEOUT
intf org.apache.sshd.sftp.client.FullAccessSftpClient
intf org.apache.sshd.sftp.client.SftpErrorDataHandler
meth protected <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} putReferencedName(int,{%%0},java.lang.String,int)
meth protected <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} writeAttributes(int,{%%0},org.apache.sshd.sftp.client.SftpClient$Attributes)
meth protected byte[] checkHandle(int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected byte[] checkHandleResponse(org.apache.sshd.sftp.client.impl.SftpResponse) throws java.io.IOException
meth protected byte[] handleUnexpectedHandlePacket(org.apache.sshd.sftp.client.impl.SftpResponse) throws java.io.IOException
meth protected int getReadBufferSize()
meth protected int getWriteBufferSize()
meth protected int handleUnknownDataPacket(org.apache.sshd.sftp.client.impl.SftpResponse) throws java.io.IOException
meth protected java.io.IOException handleUnexpectedPacket(int,org.apache.sshd.sftp.client.impl.SftpResponse) throws java.io.IOException
meth protected java.lang.String checkOneName(int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected java.lang.String checkOneNameResponse(org.apache.sshd.sftp.client.impl.SftpResponse) throws java.io.IOException
meth protected java.lang.String getReferencedName(int,org.apache.sshd.common.util.buffer.Buffer,int)
meth protected java.lang.String handleUnknownOneNamePacket(org.apache.sshd.sftp.client.impl.SftpResponse) throws java.io.IOException
meth protected java.util.List<org.apache.sshd.sftp.client.SftpClient$DirEntry> checkDirResponse(org.apache.sshd.sftp.client.impl.SftpResponse,java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>) throws java.io.IOException
meth protected java.util.List<org.apache.sshd.sftp.client.SftpClient$DirEntry> handleUnknownDirListingPacket(org.apache.sshd.sftp.client.impl.SftpResponse) throws java.io.IOException
meth protected java.util.Map<java.lang.String,java.lang.Object> getParsedServerExtensions()
meth protected java.util.Map<java.lang.String,java.lang.Object> getParsedServerExtensions(java.util.Map<java.lang.String,byte[]>)
meth protected org.apache.sshd.common.util.buffer.Buffer checkDataResponse(org.apache.sshd.sftp.client.impl.SftpAckData,org.apache.sshd.sftp.client.impl.SftpResponse,java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>) throws java.io.IOException
meth protected org.apache.sshd.sftp.client.SftpClient$Attributes checkAttributes(int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.sftp.client.SftpClient$Attributes checkAttributesResponse(org.apache.sshd.sftp.client.impl.SftpResponse) throws java.io.IOException
meth protected org.apache.sshd.sftp.client.SftpClient$Attributes handleUnexpectedAttributesPacket(org.apache.sshd.sftp.client.impl.SftpResponse) throws java.io.IOException
meth protected org.apache.sshd.sftp.client.SftpClient$Attributes readAttributes(int,org.apache.sshd.common.util.buffer.Buffer,java.util.concurrent.atomic.AtomicInteger) throws java.io.IOException
meth protected org.apache.sshd.sftp.client.impl.SftpResponse response(int,int) throws java.io.IOException
meth protected org.apache.sshd.sftp.client.impl.SftpResponse rpc(int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected void checkCommandStatus(int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected void checkResponseStatus(int,int,org.apache.sshd.sftp.client.impl.SftpStatus) throws java.io.IOException
meth protected void checkResponseStatus(org.apache.sshd.sftp.client.impl.SftpResponse) throws java.io.IOException
meth protected void throwStatusException(int,int,org.apache.sshd.sftp.client.impl.SftpStatus) throws java.io.IOException
meth public <%0 extends org.apache.sshd.sftp.client.extensions.SftpClientExtension> {%%0} getExtension(java.lang.Class<? extends {%%0}>)
meth public int read(org.apache.sshd.sftp.client.SftpClient$Handle,long,byte[],int,int,java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>) throws java.io.IOException
meth public java.io.InputStream read(java.lang.String,int,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
meth public java.lang.Iterable<org.apache.sshd.sftp.client.SftpClient$DirEntry> listDir(org.apache.sshd.sftp.client.SftpClient$Handle) throws java.io.IOException
meth public java.lang.Iterable<org.apache.sshd.sftp.client.SftpClient$DirEntry> readDir(java.lang.String) throws java.io.IOException
meth public java.lang.String canonicalPath(java.lang.String) throws java.io.IOException
meth public java.lang.String readLink(java.lang.String) throws java.io.IOException
meth public java.nio.channels.FileChannel openRemoteFileChannel(java.lang.String,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
meth public java.util.List<org.apache.sshd.sftp.client.SftpClient$DirEntry> readDir(org.apache.sshd.sftp.client.SftpClient$Handle,java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>) throws java.io.IOException
meth public org.apache.sshd.common.channel.Channel getChannel()
meth public org.apache.sshd.sftp.client.SftpClient$Attributes lstat(java.lang.String) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient$Attributes stat(java.lang.String) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient$Attributes stat(org.apache.sshd.sftp.client.SftpClient$Handle) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient$CloseableHandle open(java.lang.String,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient$CloseableHandle openDir(java.lang.String) throws java.io.IOException
meth public org.apache.sshd.sftp.client.extensions.SftpClientExtension getExtension(org.apache.sshd.sftp.client.extensions.SftpClientExtensionFactory)
meth public org.apache.sshd.sftp.client.impl.SftpOutputStreamAsync write(java.lang.String,int,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
meth public void close(org.apache.sshd.sftp.client.SftpClient$Handle) throws java.io.IOException
meth public void errorData(byte[],int,int) throws java.io.IOException
meth public void link(java.lang.String,java.lang.String,boolean) throws java.io.IOException
meth public void lock(org.apache.sshd.sftp.client.SftpClient$Handle,long,long,int) throws java.io.IOException
meth public void mkdir(java.lang.String) throws java.io.IOException
meth public void put(java.io.InputStream,int,java.lang.String,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
meth public void remove(java.lang.String) throws java.io.IOException
meth public void rename(java.lang.String,java.lang.String,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$CopyMode>) throws java.io.IOException
meth public void rmdir(java.lang.String) throws java.io.IOException
meth public void setStat(java.lang.String,org.apache.sshd.sftp.client.SftpClient$Attributes) throws java.io.IOException
meth public void setStat(org.apache.sshd.sftp.client.SftpClient$Handle,org.apache.sshd.sftp.client.SftpClient$Attributes) throws java.io.IOException
meth public void unlock(org.apache.sshd.sftp.client.SftpClient$Handle,long,long) throws java.io.IOException
meth public void write(org.apache.sshd.sftp.client.SftpClient$Handle,long,byte[],int,int) throws java.io.IOException
supr org.apache.sshd.client.subsystem.AbstractSubsystemClient
hfds fileOpenAttributes,parsedExtensionsHolder

CLSS public abstract org.apache.sshd.sftp.client.impl.AbstractSftpFileAttributeView
cons protected !varargs init(org.apache.sshd.sftp.client.fs.SftpFileSystemProvider,java.nio.file.Path,java.nio.file.LinkOption[])
fld protected final java.nio.file.LinkOption[] options
fld protected final java.nio.file.Path path
fld protected final org.apache.sshd.sftp.client.fs.SftpFileSystemProvider provider
intf java.nio.file.attribute.FileAttributeView
meth protected org.apache.sshd.sftp.client.SftpClient$Attributes readRemoteAttributes() throws java.io.IOException
meth protected void writeRemoteAttributes(org.apache.sshd.sftp.client.SftpClient$Attributes) throws java.io.IOException
meth public final java.nio.file.Path getPath()
meth public final org.apache.sshd.sftp.client.fs.SftpFileSystemProvider provider()
meth public java.lang.String name()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public org.apache.sshd.sftp.client.impl.DefaultCloseableHandle
cons public init(org.apache.sshd.sftp.client.SftpClient,java.lang.String,byte[])
meth public boolean equals(java.lang.Object)
meth public boolean isOpen()
meth public final org.apache.sshd.sftp.client.SftpClient getSftpClient()
meth public int hashCode()
meth public void close() throws java.io.IOException
supr org.apache.sshd.sftp.client.SftpClient$CloseableHandle
hfds client,open

CLSS public org.apache.sshd.sftp.client.impl.DefaultSftpClient
cons public init(org.apache.sshd.client.session.ClientSession,org.apache.sshd.sftp.client.SftpVersionSelector,org.apache.sshd.sftp.client.SftpErrorDataHandler) throws java.io.IOException
innr protected SftpChannelSubsystem
meth protected boolean receive(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected int data(byte[],int,int) throws java.io.IOException
meth protected org.apache.sshd.client.channel.ChannelSubsystem createSftpChannelSubsystem(org.apache.sshd.client.session.ClientSession)
meth protected org.apache.sshd.common.util.buffer.Buffer waitForInitResponse(java.time.Duration) throws java.io.IOException
meth protected void handleInitResponse(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected void init(org.apache.sshd.client.session.ClientSession,org.apache.sshd.sftp.client.SftpVersionSelector,java.time.Duration) throws java.io.IOException
meth protected void process(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public boolean isClosing()
meth public boolean isOpen()
meth public int getVersion()
meth public int negotiateVersion(org.apache.sshd.sftp.client.SftpVersionSelector) throws java.io.IOException
meth public int send(int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public java.nio.charset.Charset getNameDecodingCharset()
meth public java.util.NavigableMap<java.lang.String,byte[]> getServerExtensions()
meth public org.apache.sshd.client.channel.ClientChannel getClientChannel()
meth public org.apache.sshd.client.session.ClientSession getClientSession()
meth public org.apache.sshd.common.util.buffer.Buffer receive(int) throws java.io.IOException
meth public org.apache.sshd.common.util.buffer.Buffer receive(int,java.time.Duration) throws java.io.IOException
meth public org.apache.sshd.common.util.buffer.Buffer receive(int,long) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpMessage write(int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void setNameDecodingCharset(java.nio.charset.Charset)
supr org.apache.sshd.sftp.client.impl.AbstractSftpClient
hfds channel,clientSession,closing,cmdId,exposedExtensions,extensions,lastMessage,messages,nameDecodingCharset,receiveBuffer,versionHolder,writeLock

CLSS protected org.apache.sshd.sftp.client.impl.DefaultSftpClient$SftpChannelSubsystem
 outer org.apache.sshd.sftp.client.impl.DefaultSftpClient
cons protected init(org.apache.sshd.sftp.client.impl.DefaultSftpClient)
meth protected java.io.OutputStream createErrOutputStream(org.apache.sshd.common.session.Session)
meth protected java.io.OutputStream createStdOutputStream(org.apache.sshd.common.session.Session)
meth protected org.apache.sshd.common.channel.ChannelAsyncOutputStream createAsyncInput(org.apache.sshd.common.session.Session)
meth protected void doOpen() throws java.io.IOException
supr org.apache.sshd.client.channel.ChannelSubsystem

CLSS public org.apache.sshd.sftp.client.impl.DefaultSftpClientFactory
cons public init()
fld public final static org.apache.sshd.sftp.client.impl.DefaultSftpClientFactory INSTANCE
intf org.apache.sshd.sftp.client.SftpClientFactory
meth protected org.apache.sshd.sftp.client.impl.DefaultSftpClient createDefaultSftpClient(org.apache.sshd.client.session.ClientSession,org.apache.sshd.sftp.client.SftpVersionSelector,org.apache.sshd.sftp.client.SftpErrorDataHandler) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient createSftpClient(org.apache.sshd.client.session.ClientSession,org.apache.sshd.sftp.client.SftpVersionSelector,org.apache.sshd.sftp.client.SftpErrorDataHandler) throws java.io.IOException
meth public org.apache.sshd.sftp.client.fs.SftpFileSystem createSftpFileSystem(org.apache.sshd.client.session.ClientSession,org.apache.sshd.sftp.client.SftpVersionSelector,org.apache.sshd.sftp.client.SftpErrorDataHandler,int,int) throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public org.apache.sshd.sftp.client.impl.SftpAckData
cons public init(int,long,int)
fld public final int id
fld public final int length
fld public final long offset
meth public java.lang.String toString()
supr java.lang.Object

CLSS public org.apache.sshd.sftp.client.impl.SftpDirEntryIterator
cons public init(org.apache.sshd.sftp.client.SftpClient,java.lang.String) throws java.io.IOException
cons public init(org.apache.sshd.sftp.client.SftpClient,java.lang.String,org.apache.sshd.sftp.client.SftpClient$Handle,boolean)
cons public init(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.SftpClient$Handle)
intf java.nio.channels.Channel
intf java.util.Iterator<org.apache.sshd.sftp.client.SftpClient$DirEntry>
intf org.apache.sshd.sftp.client.SftpClientHolder
meth protected java.util.List<org.apache.sshd.sftp.client.SftpClient$DirEntry> load(org.apache.sshd.sftp.client.SftpClient$Handle)
meth public boolean hasNext()
meth public boolean isCloseOnFinished()
meth public boolean isOpen()
meth public final java.lang.String getPath()
meth public final org.apache.sshd.sftp.client.SftpClient getClient()
meth public final org.apache.sshd.sftp.client.SftpClient$Handle getHandle()
meth public org.apache.sshd.sftp.client.SftpClient$DirEntry next()
meth public void close() throws java.io.IOException
meth public void remove()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds client,closeOnFinished,dirEntries,dirHandle,dirPath,eolIndicator,index,open

CLSS public org.apache.sshd.sftp.client.impl.SftpInputStreamAsync
cons public init(org.apache.sshd.sftp.client.impl.AbstractSftpClient,int,java.lang.String,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
cons public init(org.apache.sshd.sftp.client.impl.AbstractSftpClient,int,long,long,java.lang.String,org.apache.sshd.sftp.client.SftpClient$CloseableHandle)
cons public init(org.apache.sshd.sftp.client.impl.AbstractSftpClient,int,long,long,java.lang.String,org.apache.sshd.sftp.client.SftpClient$CloseableHandle,boolean)
fld protected boolean bufferAdjusted
fld protected boolean eofIndicator
fld protected final byte[] bb
fld protected final java.util.Deque<org.apache.sshd.sftp.client.impl.SftpAckData> pendingReads
fld protected final long fileSize
fld protected final org.slf4j.Logger log
fld protected int bufferSize
fld protected int maxReceived
fld protected long clientOffset
fld protected long requestOffset
fld protected long shortReads
fld protected org.apache.sshd.common.util.buffer.Buffer buffer
fld protected org.apache.sshd.sftp.client.SftpClient$CloseableHandle handle
intf org.apache.sshd.sftp.client.SftpClientHolder
meth protected boolean fillData() throws java.io.IOException
meth protected boolean hasNoData()
meth protected int adjustBufferIfNeeded(int,long,int,long)
meth protected void pollBuffer(org.apache.sshd.sftp.client.impl.SftpAckData) throws java.io.IOException
meth protected void sendRequests() throws java.io.IOException
meth public boolean isEof()
meth public boolean isOpen()
meth public final java.lang.String getPath()
meth public final org.apache.sshd.sftp.client.impl.AbstractSftpClient getClient()
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public java.lang.String toString()
meth public long skip(long) throws java.io.IOException
meth public long transferTo(java.io.OutputStream) throws java.io.IOException
meth public long transferTo(long,java.nio.channels.WritableByteChannel) throws java.io.IOException
meth public void close() throws java.io.IOException
supr org.apache.sshd.common.util.io.input.InputStreamWithChannel
hfds MIN_BUFFER_SIZE,clientInstance,ownsHandle,path
hcls BufferConsumer

CLSS public org.apache.sshd.sftp.client.impl.SftpIterableDirEntry
cons public init(org.apache.sshd.sftp.client.SftpClient,java.lang.String)
intf java.lang.Iterable<org.apache.sshd.sftp.client.SftpClient$DirEntry>
intf org.apache.sshd.sftp.client.SftpClientHolder
meth public final java.lang.String getPath()
meth public final org.apache.sshd.sftp.client.SftpClient getClient()
meth public org.apache.sshd.sftp.client.impl.SftpDirEntryIterator iterator()
supr java.lang.Object
hfds client,path

CLSS public org.apache.sshd.sftp.client.impl.SftpOutputStreamAsync
cons public init(org.apache.sshd.sftp.client.impl.AbstractSftpClient,int,java.lang.String,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
cons public init(org.apache.sshd.sftp.client.impl.AbstractSftpClient,int,java.lang.String,org.apache.sshd.sftp.client.SftpClient$CloseableHandle)
cons public init(org.apache.sshd.sftp.client.impl.AbstractSftpClient,int,java.lang.String,org.apache.sshd.sftp.client.SftpClient$CloseableHandle,boolean)
fld protected final byte[] bb
fld protected final int bufferSize
fld protected final java.util.Deque<org.apache.sshd.sftp.client.impl.SftpAckData> pendingAcks
fld protected final org.slf4j.Logger log
fld protected long offset
fld protected org.apache.sshd.common.util.buffer.Buffer buffer
fld protected org.apache.sshd.sftp.client.SftpClient$CloseableHandle handle
intf org.apache.sshd.sftp.client.SftpClientHolder
meth public boolean isOpen()
meth public final java.lang.String getPath()
meth public final org.apache.sshd.sftp.client.impl.AbstractSftpClient getClient()
meth public java.lang.String toString()
meth public long transferFrom(java.io.InputStream) throws java.io.IOException
meth public long transferFrom(java.nio.channels.ReadableByteChannel,long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void setOffset(long)
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr org.apache.sshd.common.util.io.output.OutputStreamWithChannel
hfds bufferPool,clientInstance,handleId,lastMsg,nextBuffer,ownsHandle,packetSize,path,sftpPreamble,usePacket
hcls ByteInput,ChannelReader

CLSS public org.apache.sshd.sftp.client.impl.SftpPathImpl
cons public init(org.apache.sshd.sftp.client.fs.SftpFileSystem,java.lang.String,java.util.List<java.lang.String>)
intf org.apache.sshd.sftp.client.fs.WithFileAttributeCache
meth protected void cacheAttributes(boolean)
meth public <%0 extends java.lang.Object> {%%0} withAttributeCache(org.apache.sshd.common.util.io.functors.IOFunction<java.nio.file.Path,{%%0}>) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient$Attributes getAttributes()
meth public void setAttributes(org.apache.sshd.sftp.client.SftpClient$Attributes)
supr org.apache.sshd.sftp.client.fs.SftpPath
hfds attributes,cachingLevel

CLSS public org.apache.sshd.sftp.client.impl.SftpRemotePathChannel
cons public init(java.lang.String,org.apache.sshd.sftp.client.SftpClient,boolean,java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode>) throws java.io.IOException
fld protected final boolean closeOnExit
fld protected final java.lang.Object lock
fld protected final java.util.Collection<org.apache.sshd.sftp.client.SftpClient$OpenMode> modes
fld protected final java.util.concurrent.atomic.AtomicLong posTracker
fld protected final java.util.concurrent.atomic.AtomicReference<java.lang.Thread> blockingThreadHolder
fld protected final org.apache.sshd.sftp.client.SftpClient sftp
fld protected final org.apache.sshd.sftp.client.SftpClient$CloseableHandle handle
fld protected final org.slf4j.Logger log
fld public final static java.util.Set<org.apache.sshd.sftp.client.SftpClient$OpenMode> READ_MODES
fld public final static java.util.Set<org.apache.sshd.sftp.client.SftpClient$OpenMode> WRITE_MODES
meth protected long doRead(java.util.Collection<? extends java.nio.ByteBuffer>,long) throws java.io.IOException
meth protected long doWrite(java.util.Collection<? extends java.nio.ByteBuffer>,long) throws java.io.IOException
meth protected void beginBlocking(java.lang.Object)
meth protected void endBlocking(java.lang.Object,boolean) throws java.nio.channels.AsynchronousCloseException
meth protected void implCloseChannel() throws java.io.IOException
meth public int read(java.nio.ByteBuffer) throws java.io.IOException
meth public int read(java.nio.ByteBuffer,long) throws java.io.IOException
meth public int write(java.nio.ByteBuffer) throws java.io.IOException
meth public int write(java.nio.ByteBuffer,long) throws java.io.IOException
meth public java.lang.String getRemotePath()
meth public java.lang.String toString()
meth public java.nio.MappedByteBuffer map(java.nio.channels.FileChannel$MapMode,long,long) throws java.io.IOException
meth public java.nio.channels.FileChannel position(long) throws java.io.IOException
meth public java.nio.channels.FileChannel truncate(long) throws java.io.IOException
meth public java.nio.channels.FileLock lock(long,long,boolean) throws java.io.IOException
meth public java.nio.channels.FileLock tryLock(long,long,boolean) throws java.io.IOException
meth public long position() throws java.io.IOException
meth public long read(java.nio.ByteBuffer[],int,int) throws java.io.IOException
meth public long size() throws java.io.IOException
meth public long transferFrom(java.nio.channels.ReadableByteChannel,long,long) throws java.io.IOException
meth public long transferTo(long,long,java.nio.channels.WritableByteChannel) throws java.io.IOException
meth public long write(java.nio.ByteBuffer[],int,int) throws java.io.IOException
meth public void force(boolean) throws java.io.IOException
supr java.nio.channels.FileChannel
hfds path

CLSS public final org.apache.sshd.sftp.client.impl.SftpResponse
meth public int getCmd()
meth public int getId()
meth public int getLength()
meth public int getType()
meth public org.apache.sshd.common.util.buffer.Buffer getBuffer()
meth public static org.apache.sshd.sftp.client.impl.SftpResponse parse(int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public static void validateIncomingResponse(int,int,int,int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr java.lang.Object
hfds buffer,cmd,id,length,type

CLSS public final org.apache.sshd.sftp.client.impl.SftpStatus
meth public boolean isOk()
meth public int getStatusCode()
meth public java.lang.String getLanguage()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public static org.apache.sshd.sftp.client.impl.SftpStatus parse(org.apache.sshd.sftp.client.impl.SftpResponse) throws org.apache.sshd.sftp.common.SftpException
supr java.lang.Object
hfds language,message,statusCode

CLSS public org.apache.sshd.sftp.client.impl.SimpleSftpClientImpl
cons public init()
cons public init(org.apache.sshd.client.simple.SimpleClient)
cons public init(org.apache.sshd.client.simple.SimpleClient,org.apache.sshd.sftp.client.SftpClientFactory)
intf org.apache.sshd.sftp.client.SimpleSftpClient
meth protected org.apache.sshd.sftp.client.SftpClient createSftpClient(org.apache.sshd.client.session.ClientSession) throws java.io.IOException
meth protected org.apache.sshd.sftp.client.SftpClient createSftpClient(org.apache.sshd.common.util.io.functors.IOFunction<? super org.apache.sshd.client.simple.SimpleClient,? extends org.apache.sshd.client.session.ClientSession>) throws java.io.IOException
meth public boolean isOpen()
meth public org.apache.sshd.client.simple.SimpleClient getClient()
meth public org.apache.sshd.sftp.client.SftpClient sftpLogin(java.net.SocketAddress,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClient sftpLogin(java.net.SocketAddress,java.lang.String,java.security.KeyPair) throws java.io.IOException
meth public org.apache.sshd.sftp.client.SftpClientFactory getSftpClientFactory()
meth public void close() throws java.io.IOException
meth public void setClient(org.apache.sshd.client.simple.SimpleClient)
meth public void setSftpClientFactory(org.apache.sshd.sftp.client.SftpClientFactory)
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds clientInstance,sftpClientFactory

CLSS public org.apache.sshd.sftp.client.impl.StfpIterableDirHandle
cons public init(org.apache.sshd.sftp.client.SftpClient,org.apache.sshd.sftp.client.SftpClient$Handle)
intf java.lang.Iterable<org.apache.sshd.sftp.client.SftpClient$DirEntry>
intf org.apache.sshd.sftp.client.SftpClientHolder
meth public final org.apache.sshd.sftp.client.SftpClient getClient()
meth public final org.apache.sshd.sftp.client.SftpClient$Handle getHandle()
meth public org.apache.sshd.sftp.client.impl.SftpDirEntryIterator iterator()
supr java.lang.Object
hfds client,handle

CLSS public final org.apache.sshd.sftp.common.SftpConstants
fld public final static int ACE4_ACCESS_ALLOWED_ACE_TYPE = 0
fld public final static int ACE4_ACCESS_DENIED_ACE_TYPE = 1
fld public final static int ACE4_ADD_FILE = 2
fld public final static int ACE4_ADD_SUBDIRECTORY = 4
fld public final static int ACE4_APPEND_DATA = 4
fld public final static int ACE4_DELETE = 65536
fld public final static int ACE4_DELETE_CHILD = 64
fld public final static int ACE4_DIRECTORY_INHERIT_ACE = 2
fld public final static int ACE4_EXECUTE = 32
fld public final static int ACE4_FAILED_ACCESS_ACE_FLAG = 32
fld public final static int ACE4_FILE_INHERIT_ACE = 1
fld public final static int ACE4_IDENTIFIER_GROUP = 64
fld public final static int ACE4_INHERIT_ONLY_ACE = 8
fld public final static int ACE4_LIST_DIRECTORY = 1
fld public final static int ACE4_NO_PROPAGATE_INHERIT_ACE = 4
fld public final static int ACE4_READ_ACL = 131072
fld public final static int ACE4_READ_ATTRIBUTES = 128
fld public final static int ACE4_READ_DATA = 1
fld public final static int ACE4_READ_NAMED_ATTRS = 8
fld public final static int ACE4_SUCCESSFUL_ACCESS_ACE_FLAG = 16
fld public final static int ACE4_SYNCHRONIZE = 1048576
fld public final static int ACE4_SYSTEM_ALARM_ACE_TYPE = 3
fld public final static int ACE4_SYSTEM_AUDIT_ACE_TYPE = 2
fld public final static int ACE4_WRITE_ACL = 262144
fld public final static int ACE4_WRITE_ATTRIBUTES = 256
fld public final static int ACE4_WRITE_DATA = 2
fld public final static int ACE4_WRITE_NAMED_ATTRS = 16
fld public final static int ACE4_WRITE_OWNER = 524288
fld public final static int MD5_QUICK_HASH_SIZE = 2048
fld public final static int MIN_CHKFILE_BLOCKSIZE = 256
fld public final static int SFTP_V3 = 3
fld public final static int SFTP_V4 = 4
fld public final static int SFTP_V5 = 5
fld public final static int SFTP_V6 = 6
fld public final static int SFX_ACL_AUDIT_ALARM_INCLUDED = 16
fld public final static int SFX_ACL_AUDIT_ALARM_INHERITED = 32
fld public final static int SFX_ACL_CONTROL_INCLUDED = 1
fld public final static int SFX_ACL_CONTROL_INHERITED = 4
fld public final static int SFX_ACL_CONTROL_PRESENT = 2
fld public final static int SSH_ACL_CAP_ALARM = 8
fld public final static int SSH_ACL_CAP_ALLOW = 1
fld public final static int SSH_ACL_CAP_AUDIT = 4
fld public final static int SSH_ACL_CAP_DENY = 2
fld public final static int SSH_ACL_CAP_INHERIT_ACCESS = 16
fld public final static int SSH_ACL_CAP_INHERIT_AUDIT_ALARM = 32
fld public final static int SSH_FILEXFER_ATTR_ACCESSTIME = 8
fld public final static int SSH_FILEXFER_ATTR_ACL = 64
fld public final static int SSH_FILEXFER_ATTR_ACMODTIME = 8
fld public final static int SSH_FILEXFER_ATTR_ALL = 65535
fld public final static int SSH_FILEXFER_ATTR_ALLOCATION_SIZE = 1024
fld public final static int SSH_FILEXFER_ATTR_BITS = 512
fld public final static int SSH_FILEXFER_ATTR_CREATETIME = 16
fld public final static int SSH_FILEXFER_ATTR_CTIME = 32768
fld public final static int SSH_FILEXFER_ATTR_EXTENDED = -2147483648
fld public final static int SSH_FILEXFER_ATTR_FLAGS_APPEND_ONLY = 256
fld public final static int SSH_FILEXFER_ATTR_FLAGS_ARCHIVE = 16
fld public final static int SSH_FILEXFER_ATTR_FLAGS_CASE_INSENSITIVE = 8
fld public final static int SSH_FILEXFER_ATTR_FLAGS_COMPRESSED = 64
fld public final static int SSH_FILEXFER_ATTR_FLAGS_ENCRYPTED = 32
fld public final static int SSH_FILEXFER_ATTR_FLAGS_HIDDEN = 4
fld public final static int SSH_FILEXFER_ATTR_FLAGS_IMMUTABLE = 512
fld public final static int SSH_FILEXFER_ATTR_FLAGS_READONLY = 1
fld public final static int SSH_FILEXFER_ATTR_FLAGS_SPARSE = 128
fld public final static int SSH_FILEXFER_ATTR_FLAGS_SYNC = 1024
fld public final static int SSH_FILEXFER_ATTR_FLAGS_SYSTEM = 2
fld public final static int SSH_FILEXFER_ATTR_LINK_COUNT = 8192
fld public final static int SSH_FILEXFER_ATTR_MIME_TYPE = 4096
fld public final static int SSH_FILEXFER_ATTR_MODIFYTIME = 32
fld public final static int SSH_FILEXFER_ATTR_OWNERGROUP = 128
fld public final static int SSH_FILEXFER_ATTR_PERMISSIONS = 4
fld public final static int SSH_FILEXFER_ATTR_SIZE = 1
fld public final static int SSH_FILEXFER_ATTR_SUBSECOND_TIMES = 256
fld public final static int SSH_FILEXFER_ATTR_TEXT_HINT = 2048
fld public final static int SSH_FILEXFER_ATTR_UIDGID = 2
fld public final static int SSH_FILEXFER_ATTR_UNTRANSLATED_NAME = 16384
fld public final static int SSH_FILEXFER_TYPE_BLOCK_DEVICE = 8
fld public final static int SSH_FILEXFER_TYPE_CHAR_DEVICE = 7
fld public final static int SSH_FILEXFER_TYPE_DIRECTORY = 2
fld public final static int SSH_FILEXFER_TYPE_FIFO = 9
fld public final static int SSH_FILEXFER_TYPE_REGULAR = 1
fld public final static int SSH_FILEXFER_TYPE_SOCKET = 6
fld public final static int SSH_FILEXFER_TYPE_SPECIAL = 4
fld public final static int SSH_FILEXFER_TYPE_SYMLINK = 3
fld public final static int SSH_FILEXFER_TYPE_UNKNOWN = 5
fld public final static int SSH_FXF_ACCESS_AUDIT_ALARM_INFO = 4096
fld public final static int SSH_FXF_ACCESS_BACKUP = 8192
fld public final static int SSH_FXF_ACCESS_DISPOSITION = 7
fld public final static int SSH_FXF_APPEND = 4
fld public final static int SSH_FXF_APPEND_DATA = 8
fld public final static int SSH_FXF_APPEND_DATA_ATOMIC = 16
fld public final static int SSH_FXF_BACKUP_STREAM = 16384
fld public final static int SSH_FXF_BLOCK_ADVISORY = 512
fld public final static int SSH_FXF_CREAT = 8
fld public final static int SSH_FXF_CREATE_NEW = 0
fld public final static int SSH_FXF_CREATE_TRUNCATE = 1
fld public final static int SSH_FXF_DELETE_LOCK = 256
fld public final static int SSH_FXF_DELETE_ON_CLOSE = 2048
fld public final static int SSH_FXF_EXCL = 32
fld public final static int SSH_FXF_NOFOLLOW = 1024
fld public final static int SSH_FXF_OPEN_EXISTING = 2
fld public final static int SSH_FXF_OPEN_OR_CREATE = 3
fld public final static int SSH_FXF_OVERRIDE_OWNER = 32768
fld public final static int SSH_FXF_READ = 1
fld public final static int SSH_FXF_READ_LOCK = 64
fld public final static int SSH_FXF_RENAME_ATOMIC = 2
fld public final static int SSH_FXF_RENAME_NATIVE = 4
fld public final static int SSH_FXF_RENAME_OVERWRITE = 1
fld public final static int SSH_FXF_TEXT = 64
fld public final static int SSH_FXF_TEXT_MODE = 32
fld public final static int SSH_FXF_TRUNC = 16
fld public final static int SSH_FXF_TRUNCATE_EXISTING = 4
fld public final static int SSH_FXF_WRITE = 2
fld public final static int SSH_FXF_WRITE_LOCK = 128
fld public final static int SSH_FXP_ATTRS = 105
fld public final static int SSH_FXP_BLOCK = 22
fld public final static int SSH_FXP_CLOSE = 4
fld public final static int SSH_FXP_DATA = 103
fld public final static int SSH_FXP_EXTENDED = 200
fld public final static int SSH_FXP_EXTENDED_REPLY = 201
fld public final static int SSH_FXP_FSETSTAT = 10
fld public final static int SSH_FXP_FSTAT = 8
fld public final static int SSH_FXP_HANDLE = 102
fld public final static int SSH_FXP_INIT = 1
fld public final static int SSH_FXP_LINK = 21
fld public final static int SSH_FXP_LSTAT = 7
fld public final static int SSH_FXP_MKDIR = 14
fld public final static int SSH_FXP_NAME = 104
fld public final static int SSH_FXP_OPEN = 3
fld public final static int SSH_FXP_OPENDIR = 11
fld public final static int SSH_FXP_READ = 5
fld public final static int SSH_FXP_READDIR = 12
fld public final static int SSH_FXP_READLINK = 19
fld public final static int SSH_FXP_REALPATH = 16
fld public final static int SSH_FXP_REALPATH_NO_CHECK = 1
fld public final static int SSH_FXP_REALPATH_STAT_ALWAYS = 3
fld public final static int SSH_FXP_REALPATH_STAT_IF = 2
fld public final static int SSH_FXP_REMOVE = 13
fld public final static int SSH_FXP_RENAME = 18
fld public final static int SSH_FXP_RENAME_ATOMIC = 2
fld public final static int SSH_FXP_RENAME_NATIVE = 4
fld public final static int SSH_FXP_RENAME_OVERWRITE = 1
fld public final static int SSH_FXP_RMDIR = 15
fld public final static int SSH_FXP_SETSTAT = 9
fld public final static int SSH_FXP_STAT = 17
fld public final static int SSH_FXP_STATUS = 101
fld public final static int SSH_FXP_SYMLINK = 20
fld public final static int SSH_FXP_UNBLOCK = 23
fld public final static int SSH_FXP_VERSION = 2
fld public final static int SSH_FXP_WRITE = 6
fld public final static int SSH_FX_BAD_MESSAGE = 5
fld public final static int SSH_FX_BYTE_RANGE_LOCK_CONFLICT = 25
fld public final static int SSH_FX_BYTE_RANGE_LOCK_REFUSED = 26
fld public final static int SSH_FX_CANNOT_DELETE = 22
fld public final static int SSH_FX_CONNECTION_LOST = 7
fld public final static int SSH_FX_DELETE_PENDING = 27
fld public final static int SSH_FX_DIR_NOT_EMPTY = 18
fld public final static int SSH_FX_EOF = 1
fld public final static int SSH_FX_FAILURE = 4
fld public final static int SSH_FX_FILE_ALREADY_EXISTS = 11
fld public final static int SSH_FX_FILE_CORRUPT = 28
fld public final static int SSH_FX_FILE_IS_A_DIRECTORY = 24
fld public final static int SSH_FX_GROUP_INVALID = 30
fld public final static int SSH_FX_INVALID_FILENAME = 20
fld public final static int SSH_FX_INVALID_HANDLE = 9
fld public final static int SSH_FX_INVALID_PARAMETER = 23
fld public final static int SSH_FX_LINK_LOOP = 21
fld public final static int SSH_FX_LOCK_CONFLICT = 17
fld public final static int SSH_FX_NOT_A_DIRECTORY = 19
fld public final static int SSH_FX_NO_CONNECTION = 6
fld public final static int SSH_FX_NO_MATCHING_BYTE_RANGE_LOCK = 31
fld public final static int SSH_FX_NO_MEDIA = 13
fld public final static int SSH_FX_NO_SPACE_ON_FILESYSTEM = 14
fld public final static int SSH_FX_NO_SUCH_FILE = 2
fld public final static int SSH_FX_NO_SUCH_PATH = 10
fld public final static int SSH_FX_OK = 0
fld public final static int SSH_FX_OP_UNSUPPORTED = 8
fld public final static int SSH_FX_OWNER_INVALID = 29
fld public final static int SSH_FX_PERMISSION_DENIED = 3
fld public final static int SSH_FX_QUOTA_EXCEEDED = 15
fld public final static int SSH_FX_UNKNOWN_PRINCIPAL = 16
fld public final static int SSH_FX_WRITE_PROTECT = 12
fld public final static int S_IFBLK = 24576
fld public final static int S_IFCHR = 8192
fld public final static int S_IFDIR = 16384
fld public final static int S_IFIFO = 4096
fld public final static int S_IFLNK = 40960
fld public final static int S_IFMT = 61440
fld public final static int S_IFREG = 32768
fld public final static int S_IFSOCK = 49152
fld public final static int S_IRGRP = 32
fld public final static int S_IROTH = 4
fld public final static int S_IRUSR = 256
fld public final static int S_ISGID = 1024
fld public final static int S_ISUID = 2048
fld public final static int S_ISVTX = 512
fld public final static int S_IWGRP = 16
fld public final static int S_IWOTH = 2
fld public final static int S_IWUSR = 128
fld public final static int S_IXGRP = 8
fld public final static int S_IXOTH = 1
fld public final static int S_IXUSR = 64
fld public final static java.lang.String EXT_ACL_SUPPORTED = "acl-supported"
fld public final static java.lang.String EXT_CHECK_FILE = "check-file"
fld public final static java.lang.String EXT_CHECK_FILE_HANDLE = "check-file-handle"
fld public final static java.lang.String EXT_CHECK_FILE_NAME = "check-file-name"
fld public final static java.lang.String EXT_COPY_DATA = "copy-data"
fld public final static java.lang.String EXT_COPY_FILE = "copy-file"
fld public final static java.lang.String EXT_FILENAME_CHARSET = "filename-charset"
fld public final static java.lang.String EXT_FILENAME_XLATE_CONTROL = "filename-translation-control"
fld public final static java.lang.String EXT_MD5_HASH = "md5-hash"
fld public final static java.lang.String EXT_MD5_HASH_HANDLE = "md5-hash-handle"
fld public final static java.lang.String EXT_NEWLINE = "newline"
fld public final static java.lang.String EXT_SPACE_AVAILABLE = "space-available"
fld public final static java.lang.String EXT_SUPPORTED = "supported"
fld public final static java.lang.String EXT_SUPPORTED2 = "supported2"
fld public final static java.lang.String EXT_TEXT_SEEK = "text-seek"
fld public final static java.lang.String EXT_VENDOR_ID = "vendor-id"
fld public final static java.lang.String EXT_VERSIONS = "versions"
fld public final static java.lang.String EXT_VERSION_SELECT = "version-select"
fld public final static java.lang.String SFTP_SUBSYSTEM_NAME = "sftp"
meth public static java.lang.String getCommandMessageName(int)
meth public static java.lang.String getStatusName(int)
supr java.lang.Object
hcls LazyCommandNameHolder,LazyStatusNameHolder

CLSS public org.apache.sshd.sftp.common.SftpException
cons public init(int,java.lang.String)
meth public int getStatus()
meth public java.lang.String toString()
supr java.io.IOException
hfds serialVersionUID,status

CLSS public final org.apache.sshd.sftp.common.SftpHelper
fld public final static java.util.Map<java.lang.Integer,java.lang.String> DEFAULT_SUBSTATUS_MESSAGE
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} encodeACLs({%%0},int,java.util.Collection<java.nio.file.attribute.AclEntry>)
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} writeACLs({%%0},int,java.util.Collection<java.nio.file.attribute.AclEntry>)
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} writeAclEntry({%%0},java.nio.file.attribute.AclEntry)
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} writeAttributes({%%0},org.apache.sshd.sftp.client.SftpClient$Attributes,int)
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} writeAttrs({%%0},int,java.util.Map<java.lang.String,?>)
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} writeAttrsV3({%%0},int,java.util.Map<java.lang.String,?>)
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} writeAttrsV4({%%0},int,java.util.Map<java.lang.String,?>)
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} writeExtensions({%%0},java.util.Map<?,?>)
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} writeTime({%%0},int,int,java.nio.file.attribute.FileTime)
meth public static boolean getBool(java.lang.Boolean)
meth public static int attributesToPermissions(boolean,boolean,boolean,java.util.Collection<java.nio.file.attribute.PosixFilePermission>)
meth public static int encodeAclEntryType(java.nio.file.attribute.AclEntryType)
meth public static int fileTypeFromChar(char)
meth public static int fileTypeToPermission(int)
meth public static int permissionsToFileType(int)
meth public static int resolveSubstatus(java.lang.Throwable)
meth public static java.lang.Boolean getEndOfFileIndicatorValue(org.apache.sshd.common.util.buffer.Buffer,int)
meth public static java.lang.Boolean getEndOfListIndicatorValue(org.apache.sshd.common.util.buffer.Buffer,int)
meth public static java.lang.Boolean indicateEndOfNamesList(org.apache.sshd.common.util.buffer.Buffer,int,org.apache.sshd.common.PropertyResolver)
meth public static java.lang.Boolean indicateEndOfNamesList(org.apache.sshd.common.util.buffer.Buffer,int,org.apache.sshd.common.PropertyResolver,boolean)
meth public static java.lang.String getLongName(java.lang.String,java.util.Map<java.lang.String,?>)
meth public static java.lang.String resolveStatusMessage(int)
meth public static java.nio.file.attribute.AclEntry buildAclEntry(int,int,int,java.lang.String)
meth public static java.nio.file.attribute.AclEntryType decodeAclEntryType(int)
meth public static java.nio.file.attribute.FileTime readTime(org.apache.sshd.common.util.buffer.Buffer,int,int)
meth public static java.util.List<java.nio.file.attribute.AclEntry> decodeACLs(org.apache.sshd.common.util.buffer.Buffer,int)
meth public static java.util.List<java.nio.file.attribute.AclEntry> readACLs(org.apache.sshd.common.util.buffer.Buffer,int)
meth public static java.util.NavigableMap<java.lang.String,byte[]> readExtensions(org.apache.sshd.common.util.buffer.Buffer)
meth public static java.util.NavigableMap<java.lang.String,byte[]> toBinaryExtensions(java.util.Map<java.lang.String,java.lang.String>)
meth public static java.util.NavigableMap<java.lang.String,java.lang.Object> readAttrs(org.apache.sshd.common.util.buffer.Buffer,int)
meth public static java.util.NavigableMap<java.lang.String,java.lang.String> toStringExtensions(java.util.Map<java.lang.String,?>)
meth public static java.util.Set<java.nio.file.attribute.AclEntryFlag> decodeAclFlags(int)
meth public static java.util.Set<java.nio.file.attribute.AclEntryPermission> decodeAclMask(int)
meth public static java.util.Set<java.nio.file.attribute.PosixFilePermission> permissionsToAttributes(int)
meth public static long encodeAclFlags(java.util.Collection<java.nio.file.attribute.AclEntryFlag>)
meth public static long encodeAclMask(java.util.Collection<java.nio.file.attribute.AclEntryPermission>)
meth public static org.apache.sshd.sftp.client.SftpClient$Attributes complete(org.apache.sshd.sftp.client.SftpClient$Attributes,java.lang.String)
supr java.lang.Object
hfds UNIX_PERMISSIONS_START

CLSS public final !enum org.apache.sshd.sftp.common.SftpUniversalOwnerAndGroup
fld public final static java.util.Set<org.apache.sshd.sftp.common.SftpUniversalOwnerAndGroup> VALUES
fld public final static org.apache.sshd.sftp.common.SftpUniversalOwnerAndGroup Anonymous
fld public final static org.apache.sshd.sftp.common.SftpUniversalOwnerAndGroup Authenticated
fld public final static org.apache.sshd.sftp.common.SftpUniversalOwnerAndGroup Batch
fld public final static org.apache.sshd.sftp.common.SftpUniversalOwnerAndGroup Dialup
fld public final static org.apache.sshd.sftp.common.SftpUniversalOwnerAndGroup Everyone
fld public final static org.apache.sshd.sftp.common.SftpUniversalOwnerAndGroup Group
fld public final static org.apache.sshd.sftp.common.SftpUniversalOwnerAndGroup Interactive
fld public final static org.apache.sshd.sftp.common.SftpUniversalOwnerAndGroup Network
fld public final static org.apache.sshd.sftp.common.SftpUniversalOwnerAndGroup Owner
fld public final static org.apache.sshd.sftp.common.SftpUniversalOwnerAndGroup Service
intf org.apache.sshd.common.NamedResource
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public static org.apache.sshd.sftp.common.SftpUniversalOwnerAndGroup fromName(java.lang.String)
meth public static org.apache.sshd.sftp.common.SftpUniversalOwnerAndGroup valueOf(java.lang.String)
meth public static org.apache.sshd.sftp.common.SftpUniversalOwnerAndGroup[] values()
supr java.lang.Enum<org.apache.sshd.sftp.common.SftpUniversalOwnerAndGroup>
hfds name

CLSS public abstract org.apache.sshd.sftp.common.extensions.AbstractParser<%0 extends java.lang.Object>
cons protected init(java.lang.String)
intf org.apache.sshd.sftp.common.extensions.ExtensionParser<{org.apache.sshd.sftp.common.extensions.AbstractParser%0}>
meth public final java.lang.String getName()
supr java.lang.Object
hfds name

CLSS public org.apache.sshd.sftp.common.extensions.AclSupportedParser
cons public init()
fld public final static org.apache.sshd.sftp.common.extensions.AclSupportedParser INSTANCE
innr public static AclCapabilities
meth public org.apache.sshd.sftp.common.extensions.AclSupportedParser$AclCapabilities parse(byte[],int,int)
meth public org.apache.sshd.sftp.common.extensions.AclSupportedParser$AclCapabilities parse(org.apache.sshd.common.util.buffer.Buffer)
supr org.apache.sshd.sftp.common.extensions.AbstractParser<org.apache.sshd.sftp.common.extensions.AclSupportedParser$AclCapabilities>

CLSS public static org.apache.sshd.sftp.common.extensions.AclSupportedParser$AclCapabilities
 outer org.apache.sshd.sftp.common.extensions.AclSupportedParser
cons public init()
cons public init(int)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public int getCapabilities()
meth public int hashCode()
meth public java.lang.String toString()
meth public org.apache.sshd.sftp.common.extensions.AclSupportedParser$AclCapabilities clone()
meth public static int constructAclCapabilities(java.util.Collection<java.lang.Integer>)
meth public static java.lang.Integer getAclCapabilityValue(java.lang.String)
meth public static java.lang.String getAclCapabilityName(int)
meth public static java.util.NavigableMap<java.lang.Integer,java.lang.String> getAclCapabilityValuesMap()
meth public static java.util.NavigableMap<java.lang.String,java.lang.Integer> getAclCapabilityNamesMap()
meth public static java.util.NavigableSet<java.lang.String> decodeAclCapabilities(int)
meth public static java.util.Set<java.lang.Integer> deconstructAclCapabilities(int)
meth public void setCapabilities(int)
supr java.lang.Object
hfds capabilities,serialVersionUID
hcls LazyAclCapabilityNameHolder

CLSS public abstract interface org.apache.sshd.sftp.common.extensions.ExtensionParser<%0 extends java.lang.Object>
intf java.util.function.Function<byte[],{org.apache.sshd.sftp.common.extensions.ExtensionParser%0}>
intf org.apache.sshd.common.NamedResource
meth public abstract {org.apache.sshd.sftp.common.extensions.ExtensionParser%0} parse(byte[],int,int)
meth public {org.apache.sshd.sftp.common.extensions.ExtensionParser%0} apply(byte[])
meth public {org.apache.sshd.sftp.common.extensions.ExtensionParser%0} parse(byte[])

CLSS public org.apache.sshd.sftp.common.extensions.FilenameCharsetParser
cons public init()
fld public final static org.apache.sshd.sftp.common.extensions.FilenameCharsetParser INSTANCE
innr public static FilenameCharset
meth public org.apache.sshd.sftp.common.extensions.FilenameCharsetParser$FilenameCharset parse(byte[],int,int)
meth public org.apache.sshd.sftp.common.extensions.FilenameCharsetParser$FilenameCharset parse(java.lang.String)
supr org.apache.sshd.sftp.common.extensions.AbstractParser<org.apache.sshd.sftp.common.extensions.FilenameCharsetParser$FilenameCharset>

CLSS public static org.apache.sshd.sftp.common.extensions.FilenameCharsetParser$FilenameCharset
 outer org.apache.sshd.sftp.common.extensions.FilenameCharsetParser
cons public init()
cons public init(java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getCharset()
meth public java.lang.String toString()
meth public org.apache.sshd.sftp.common.extensions.FilenameCharsetParser$FilenameCharset clone()
meth public void setCharset(java.lang.String)
supr java.lang.Object
hfds charset,serialVersionUID

CLSS public org.apache.sshd.sftp.common.extensions.NewlineParser
cons public init()
fld public final static org.apache.sshd.sftp.common.extensions.NewlineParser INSTANCE
innr public static Newline
meth public org.apache.sshd.sftp.common.extensions.NewlineParser$Newline parse(byte[],int,int)
meth public org.apache.sshd.sftp.common.extensions.NewlineParser$Newline parse(java.lang.String)
supr org.apache.sshd.sftp.common.extensions.AbstractParser<org.apache.sshd.sftp.common.extensions.NewlineParser$Newline>

CLSS public static org.apache.sshd.sftp.common.extensions.NewlineParser$Newline
 outer org.apache.sshd.sftp.common.extensions.NewlineParser
cons public init()
cons public init(java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getNewline()
meth public java.lang.String toString()
meth public org.apache.sshd.sftp.common.extensions.NewlineParser$Newline clone()
meth public void setNewline(java.lang.String)
supr java.lang.Object
hfds newline,serialVersionUID

CLSS public final org.apache.sshd.sftp.common.extensions.ParserUtils
fld public final static java.util.Collection<org.apache.sshd.sftp.common.extensions.ExtensionParser<?>> BUILT_IN_PARSERS
meth public !varargs static java.lang.Object parse(java.lang.String,byte[])
meth public static java.util.List<org.apache.sshd.sftp.common.extensions.ExtensionParser<?>> getRegisteredParsers()
meth public static java.util.Map<java.lang.String,java.lang.Object> parse(java.util.Map<java.lang.String,byte[]>)
meth public static java.util.Set<java.lang.String> getRegisteredParsersNames()
meth public static java.util.Set<java.lang.String> supportedExtensions(java.util.Map<java.lang.String,?>)
meth public static org.apache.sshd.sftp.common.extensions.ExtensionParser<?> getRegisteredParser(java.lang.String)
meth public static org.apache.sshd.sftp.common.extensions.ExtensionParser<?> registerParser(org.apache.sshd.sftp.common.extensions.ExtensionParser<?>)
meth public static org.apache.sshd.sftp.common.extensions.ExtensionParser<?> unregisterParser(java.lang.String)
supr java.lang.Object
hfds PARSERS_MAP

CLSS public org.apache.sshd.sftp.common.extensions.SpaceAvailableExtensionInfo
cons public init()
cons public init(java.nio.file.FileStore) throws java.io.IOException
cons public init(org.apache.sshd.common.util.buffer.Buffer)
fld public int bytesPerAllocationUnit
fld public long bytesAvailableToUser
fld public long bytesOnDevice
fld public long unusedBytesAvailableToUser
fld public long unusedBytesOnDevice
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.apache.sshd.sftp.common.extensions.SpaceAvailableExtensionInfo clone()
meth public static org.apache.sshd.sftp.common.extensions.SpaceAvailableExtensionInfo decode(org.apache.sshd.common.util.buffer.Buffer)
meth public static void decode(org.apache.sshd.common.util.buffer.Buffer,org.apache.sshd.sftp.common.extensions.SpaceAvailableExtensionInfo)
meth public static void encode(org.apache.sshd.common.util.buffer.Buffer,org.apache.sshd.sftp.common.extensions.SpaceAvailableExtensionInfo)
supr java.lang.Object

CLSS public org.apache.sshd.sftp.common.extensions.Supported2Parser
cons public init()
fld public final static org.apache.sshd.sftp.common.extensions.Supported2Parser INSTANCE
innr public static Supported2
meth public org.apache.sshd.sftp.common.extensions.Supported2Parser$Supported2 parse(byte[],int,int)
meth public org.apache.sshd.sftp.common.extensions.Supported2Parser$Supported2 parse(org.apache.sshd.common.util.buffer.Buffer)
supr org.apache.sshd.sftp.common.extensions.AbstractParser<org.apache.sshd.sftp.common.extensions.Supported2Parser$Supported2>

CLSS public static org.apache.sshd.sftp.common.extensions.Supported2Parser$Supported2
 outer org.apache.sshd.sftp.common.extensions.Supported2Parser
cons public init()
fld public int supportedAccessMask
fld public int supportedAttributeBits
fld public int supportedAttributeMask
fld public int supportedOpenFlags
fld public java.util.Collection<java.lang.String> attribExtensionNames
fld public java.util.Collection<java.lang.String> extensionNames
fld public long maxReadSize
fld public short supportedBlock
fld public short supportedOpenBlockVector
meth public java.lang.String toString()
supr java.lang.Object

CLSS public org.apache.sshd.sftp.common.extensions.SupportedParser
cons public init()
fld public final static org.apache.sshd.sftp.common.extensions.SupportedParser INSTANCE
innr public static Supported
meth public org.apache.sshd.sftp.common.extensions.SupportedParser$Supported parse(byte[],int,int)
meth public org.apache.sshd.sftp.common.extensions.SupportedParser$Supported parse(org.apache.sshd.common.util.buffer.Buffer)
supr org.apache.sshd.sftp.common.extensions.AbstractParser<org.apache.sshd.sftp.common.extensions.SupportedParser$Supported>

CLSS public static org.apache.sshd.sftp.common.extensions.SupportedParser$Supported
 outer org.apache.sshd.sftp.common.extensions.SupportedParser
cons public init()
fld public int supportedAccessMask
fld public int supportedAttributeBits
fld public int supportedAttributeMask
fld public int supportedOpenFlags
fld public java.util.Collection<java.lang.String> extensionNames
fld public long maxReadSize
meth public java.lang.String toString()
supr java.lang.Object

CLSS public org.apache.sshd.sftp.common.extensions.VendorIdParser
cons public init()
fld public final static org.apache.sshd.sftp.common.extensions.VendorIdParser INSTANCE
innr public static VendorId
meth public org.apache.sshd.sftp.common.extensions.VendorIdParser$VendorId parse(byte[],int,int)
meth public org.apache.sshd.sftp.common.extensions.VendorIdParser$VendorId parse(org.apache.sshd.common.util.buffer.Buffer)
supr org.apache.sshd.sftp.common.extensions.AbstractParser<org.apache.sshd.sftp.common.extensions.VendorIdParser$VendorId>

CLSS public static org.apache.sshd.sftp.common.extensions.VendorIdParser$VendorId
 outer org.apache.sshd.sftp.common.extensions.VendorIdParser
cons public init()
fld public java.lang.String productName
fld public java.lang.String productVersion
fld public java.lang.String vendorName
fld public long productBuildNumber
meth public java.lang.String toString()
supr java.lang.Object

CLSS public org.apache.sshd.sftp.common.extensions.VersionsParser
cons public init()
fld public final static org.apache.sshd.sftp.common.extensions.VersionsParser INSTANCE
innr public static Versions
meth public org.apache.sshd.sftp.common.extensions.VersionsParser$Versions parse(byte[],int,int)
meth public org.apache.sshd.sftp.common.extensions.VersionsParser$Versions parse(java.lang.String)
supr org.apache.sshd.sftp.common.extensions.AbstractParser<org.apache.sshd.sftp.common.extensions.VersionsParser$Versions>

CLSS public static org.apache.sshd.sftp.common.extensions.VersionsParser$Versions
 outer org.apache.sshd.sftp.common.extensions.VersionsParser
cons public init()
cons public init(java.util.List<java.lang.String>)
fld public final static char SEP = ','
meth public java.lang.String toString()
meth public java.util.List<java.lang.Integer> resolveAvailableVersions(int)
meth public java.util.List<java.lang.String> getVersions()
meth public void setVersions(java.util.List<java.lang.String>)
supr java.lang.Object
hfds versions

CLSS public abstract org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser
cons protected init(java.lang.String)
innr public static OpenSSHExtension
meth public org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser$OpenSSHExtension parse(byte[],int,int)
meth public org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser$OpenSSHExtension parse(java.lang.String)
supr org.apache.sshd.sftp.common.extensions.AbstractParser<org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser$OpenSSHExtension>

CLSS public static org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser$OpenSSHExtension
 outer org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.sshd.common.NamedResource
meth public boolean equals(java.lang.Object)
meth public final java.lang.String getName()
meth public int hashCode()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
meth public org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser$OpenSSHExtension clone()
meth public void setVersion(java.lang.String)
supr java.lang.Object
hfds name,serialVersionUID,version

CLSS public org.apache.sshd.sftp.common.extensions.openssh.FstatVfsExtensionParser
cons public init()
fld public final static java.lang.String NAME = "fstatvfs@openssh.com"
fld public final static org.apache.sshd.sftp.common.extensions.openssh.FstatVfsExtensionParser INSTANCE
supr org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser

CLSS public org.apache.sshd.sftp.common.extensions.openssh.FsyncExtensionParser
cons public init()
fld public final static java.lang.String NAME = "fsync@openssh.com"
fld public final static org.apache.sshd.sftp.common.extensions.openssh.FsyncExtensionParser INSTANCE
supr org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser

CLSS public org.apache.sshd.sftp.common.extensions.openssh.HardLinkExtensionParser
cons public init()
fld public final static java.lang.String NAME = "hardlink@openssh.com"
fld public final static org.apache.sshd.sftp.common.extensions.openssh.HardLinkExtensionParser INSTANCE
supr org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser

CLSS public org.apache.sshd.sftp.common.extensions.openssh.LSetStatExtensionParser
cons public init()
fld public final static java.lang.String NAME = "lsetstat@openssh.com"
fld public final static org.apache.sshd.sftp.common.extensions.openssh.LSetStatExtensionParser INSTANCE
supr org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser

CLSS public org.apache.sshd.sftp.common.extensions.openssh.LimitsExtensionParser
cons public init()
fld public final static java.lang.String NAME = "limits@openssh.com"
fld public final static org.apache.sshd.sftp.common.extensions.openssh.LimitsExtensionParser INSTANCE
supr org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser

CLSS public org.apache.sshd.sftp.common.extensions.openssh.PosixRenameExtensionParser
cons public init()
fld public final static java.lang.String NAME = "posix-rename@openssh.com"
fld public final static org.apache.sshd.sftp.common.extensions.openssh.PosixRenameExtensionParser INSTANCE
supr org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser

CLSS public org.apache.sshd.sftp.common.extensions.openssh.StatVfsExtensionParser
cons public init()
fld public final static java.lang.String NAME = "statvfs@openssh.com"
fld public final static org.apache.sshd.sftp.common.extensions.openssh.StatVfsExtensionParser INSTANCE
supr org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser

CLSS public abstract org.apache.sshd.sftp.server.AbstractSftpEventListenerAdapter
cons protected init()
intf org.apache.sshd.sftp.server.SftpEventListener
meth public void blocked(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.FileHandle,long,long,int,java.lang.Throwable) throws java.io.IOException
meth public void blocking(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.FileHandle,long,long,int) throws java.io.IOException
meth public void closed(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.Handle,java.lang.Throwable) throws java.io.IOException
meth public void closing(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.Handle) throws java.io.IOException
meth public void created(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.util.Map<java.lang.String,?>,java.lang.Throwable) throws java.io.IOException
meth public void creating(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public void destroying(org.apache.sshd.server.session.ServerSession) throws java.io.IOException
meth public void exiting(org.apache.sshd.server.session.ServerSession,org.apache.sshd.sftp.server.Handle) throws java.io.IOException
meth public void initialized(org.apache.sshd.server.session.ServerSession,int) throws java.io.IOException
meth public void linked(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.nio.file.Path,boolean,java.lang.Throwable) throws java.io.IOException
meth public void linking(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.nio.file.Path,boolean) throws java.io.IOException
meth public void modifiedAttributes(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.util.Map<java.lang.String,?>,java.lang.Throwable) throws java.io.IOException
meth public void modifyingAttributes(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public void moved(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.nio.file.Path,java.util.Collection<java.nio.file.CopyOption>,java.lang.Throwable) throws java.io.IOException
meth public void moving(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.nio.file.Path,java.util.Collection<java.nio.file.CopyOption>) throws java.io.IOException
meth public void open(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.Handle) throws java.io.IOException
meth public void openFailed(org.apache.sshd.server.session.ServerSession,java.lang.String,java.nio.file.Path,boolean,java.lang.Throwable) throws java.io.IOException
meth public void opening(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.Handle) throws java.io.IOException
meth public void read(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.FileHandle,long,byte[],int,int,int,java.lang.Throwable) throws java.io.IOException
meth public void readEntries(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.DirectoryHandle,java.util.Map<java.lang.String,java.nio.file.Path>) throws java.io.IOException
meth public void reading(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.FileHandle,long,byte[],int,int) throws java.io.IOException
meth public void readingEntries(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.DirectoryHandle) throws java.io.IOException
meth public void receivedExtension(org.apache.sshd.server.session.ServerSession,java.lang.String,int) throws java.io.IOException
meth public void removed(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,boolean,java.lang.Throwable) throws java.io.IOException
meth public void removing(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,boolean) throws java.io.IOException
meth public void unblocked(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.FileHandle,long,long,java.lang.Throwable) throws java.io.IOException
meth public void unblocking(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.FileHandle,long,long) throws java.io.IOException
meth public void writing(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.FileHandle,long,byte[],int,int) throws java.io.IOException
meth public void written(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.FileHandle,long,byte[],int,int,java.lang.Throwable) throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public abstract org.apache.sshd.sftp.server.AbstractSftpEventListenerManager
cons protected init()
intf org.apache.sshd.sftp.server.SftpEventListenerManager
meth public boolean addSftpEventListener(org.apache.sshd.sftp.server.SftpEventListener)
meth public boolean removeSftpEventListener(org.apache.sshd.sftp.server.SftpEventListener)
meth public java.util.Collection<org.apache.sshd.sftp.server.SftpEventListener> getRegisteredListeners()
meth public org.apache.sshd.sftp.server.SftpEventListener getSftpEventListenerProxy()
supr java.lang.Object
hfds sftpEventListenerProxy,sftpEventListeners

CLSS public abstract org.apache.sshd.sftp.server.AbstractSftpSubsystemHelper
cons protected init(org.apache.sshd.server.channel.ChannelSession,org.apache.sshd.sftp.server.SftpSubsystemConfigurator)
fld public final static java.util.List<java.lang.String> DEFAULT_OPEN_SSH_EXTENSIONS_NAMES
fld public final static java.util.List<org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser$OpenSSHExtension> DEFAULT_OPEN_SSH_EXTENSIONS
fld public final static java.util.NavigableMap<java.lang.String,org.apache.sshd.common.OptionalFeature> DEFAULT_SUPPORTED_CLIENT_EXTENSIONS
fld public final static java.util.Set<java.lang.Integer> DEFAULT_ACL_SUPPORTED_MASK
intf org.apache.sshd.sftp.server.SftpSubsystemProxy
meth protected !varargs java.lang.Object resolveMissingFileAttributeValue(java.nio.file.Path,java.lang.String,java.lang.Object,org.apache.sshd.common.util.io.FileInfoExtractor<?>,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs java.lang.String getLongName(java.nio.file.Path,java.lang.String,boolean,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs java.lang.String getLongName(java.nio.file.Path,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs java.util.AbstractMap$SimpleImmutableEntry<java.nio.file.Path,java.lang.Boolean> doRealPathV345(int,java.lang.String,java.nio.file.Path,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs java.util.AbstractMap$SimpleImmutableEntry<java.nio.file.Path,java.lang.Boolean> doRealPathV6(int,java.lang.String,java.util.Collection<java.lang.String>,java.nio.file.Path,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs java.util.AbstractMap$SimpleImmutableEntry<java.nio.file.Path,java.lang.Boolean> validateRealPath(int,java.lang.String,java.nio.file.Path,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs java.util.NavigableMap<java.lang.String,java.lang.Object> addMissingAttribute(java.nio.file.Path,java.util.NavigableMap<java.lang.String,java.lang.Object>,java.lang.String,org.apache.sshd.common.util.io.FileInfoExtractor<?>,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs java.util.NavigableMap<java.lang.String,java.lang.Object> getAttributes(java.nio.file.Path,int,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs java.util.NavigableMap<java.lang.String,java.lang.Object> getAttributes(java.nio.file.Path,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs java.util.NavigableMap<java.lang.String,java.lang.Object> handleUnknownStatusFileAttributes(java.nio.file.Path,int,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs java.util.NavigableMap<java.lang.String,java.lang.Object> readFileAttributes(java.nio.file.Path,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs java.util.NavigableMap<java.lang.String,java.lang.Object> resolveFileAttributes(java.nio.file.Path,int,boolean,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs java.util.NavigableMap<java.lang.String,java.lang.Object> resolveMissingFileAttributes(java.nio.file.Path,int,java.util.Map<java.lang.String,java.lang.Object>,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs java.util.NavigableMap<java.lang.String,java.lang.Object> resolveReportedFileAttributes(java.nio.file.Path,int,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs void sendStatus(org.apache.sshd.common.util.buffer.Buffer,int,java.lang.Throwable,int,java.lang.Object[]) throws java.io.IOException
meth protected !varargs void setFileAccessControl(java.nio.file.Path,java.util.List<java.nio.file.attribute.AclEntry>,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs void setFileAttribute(java.nio.file.Path,java.lang.String,java.lang.String,java.lang.Object,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs void setFileAttributes(java.nio.file.Path,java.util.Map<java.lang.String,?>,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs void setFileExtensions(java.nio.file.Path,java.util.Map<java.lang.String,byte[]>,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs void setFileOwnership(java.nio.file.Path,java.lang.String,java.security.Principal,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs void setFilePermissions(java.nio.file.Path,java.util.Set<java.nio.file.attribute.PosixFilePermission>,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs void setFileRawViewAttribute(java.nio.file.Path,java.lang.String,java.lang.String,java.lang.Object,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs void setFileTime(java.nio.file.Path,java.lang.String,java.lang.String,java.nio.file.attribute.FileTime,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected !varargs void writeDirEntry(int,org.apache.sshd.sftp.server.DirectoryHandle,java.util.Map<java.lang.String,java.nio.file.Path>,org.apache.sshd.common.util.buffer.Buffer,int,java.nio.file.Path,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected <%0 extends java.io.IOException> {%%0} signalOpenFailure(int,java.lang.String,java.nio.file.Path,boolean,{%%0}) throws java.io.IOException
meth protected <%0 extends java.io.IOException> {%%0} signalRemovalPreConditionFailure(int,java.lang.String,java.nio.file.Path,{%%0},boolean) throws java.io.IOException
meth protected <%0 extends org.apache.sshd.sftp.server.Handle> {%%0} validateHandle(java.lang.String,org.apache.sshd.sftp.server.Handle,java.lang.Class<{%%0}>) throws java.io.IOException
meth protected abstract !varargs java.lang.String doOpenDir(int,java.lang.String,java.nio.file.Path,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected abstract byte[] doMD5Hash(int,java.lang.String,java.lang.String,long,long,byte[]) throws java.lang.Exception
meth protected abstract int doRead(int,java.lang.String,long,int,byte[],int,java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>) throws java.io.IOException
meth protected abstract java.lang.String doOpen(int,java.lang.String,int,int,java.util.Map<java.lang.String,java.lang.Object>) throws java.io.IOException
meth protected abstract java.util.Map<java.lang.String,java.lang.Object> doFStat(int,java.lang.String,int) throws java.io.IOException
meth protected abstract org.apache.sshd.common.util.buffer.Buffer prepareReply(org.apache.sshd.common.util.buffer.Buffer)
meth protected abstract void createLink(int,java.lang.String,java.lang.String,boolean) throws java.io.IOException
meth protected abstract void doBlock(int,java.lang.String,long,long,int) throws java.io.IOException
meth protected abstract void doCheckFileHash(int,java.lang.String,java.lang.String,java.util.Collection<java.lang.String>,long,long,int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected abstract void doClose(int,java.lang.String) throws java.io.IOException
meth protected abstract void doCopyData(int,java.lang.String,long,long,java.lang.String,long) throws java.io.IOException
meth protected abstract void doFSetStat(int,java.lang.String,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth protected abstract void doInit(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected abstract void doOpenSSHFsync(int,java.lang.String) throws java.io.IOException
meth protected abstract void doReadDir(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected abstract void doTextSeek(int,java.lang.String,long) throws java.io.IOException
meth protected abstract void doUnblock(int,java.lang.String,long,long) throws java.io.IOException
meth protected abstract void doVersionSelect(org.apache.sshd.common.util.buffer.Buffer,int,java.lang.String) throws java.io.IOException
meth protected abstract void doWrite(int,java.lang.String,long,int,byte[],int,int) throws java.io.IOException
meth protected abstract void send(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected boolean resolvePathResolutionFollowLinks(int,java.lang.String,java.nio.file.Path) throws java.io.IOException
meth protected byte[] doMD5Hash(int,java.nio.file.Path,long,long,byte[]) throws java.lang.Exception
meth protected int appendAclSupportedExtension(org.apache.sshd.common.util.buffer.Buffer,org.apache.sshd.server.session.ServerSession)
meth protected int doReadDir(int,java.lang.String,org.apache.sshd.sftp.server.DirectoryHandle,org.apache.sshd.common.util.buffer.Buffer,int,boolean) throws java.io.IOException
meth protected java.lang.Boolean validateProposedVersion(org.apache.sshd.common.util.buffer.Buffer,int,java.lang.String) throws java.io.IOException
meth protected java.lang.String appendNewlineExtension(org.apache.sshd.common.util.buffer.Buffer,org.apache.sshd.server.session.ServerSession)
meth protected java.lang.String appendVersionsExtension(org.apache.sshd.common.util.buffer.Buffer,java.lang.String,org.apache.sshd.server.session.ServerSession)
meth protected java.lang.String getLongName(java.nio.file.Path,java.lang.String,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth protected java.lang.String getLongName(java.nio.file.Path,java.lang.String,org.apache.sshd.sftp.client.SftpClient$Attributes) throws java.io.IOException
meth protected java.lang.String getShortName(java.nio.file.Path) throws java.io.IOException
meth protected java.lang.String resolveNewlineValue(org.apache.sshd.server.session.ServerSession)
meth protected java.nio.file.LinkOption[] getPathResolutionLinkOption(int,java.lang.String,java.nio.file.Path) throws java.io.IOException
meth protected java.nio.file.Path normalize(java.nio.file.Path)
meth protected java.nio.file.Path resolveFile(java.lang.String) throws java.io.IOException
meth protected java.nio.file.Path resolveNormalizedLocation(java.lang.String) throws java.io.IOException
meth protected java.nio.file.attribute.GroupPrincipal toGroup(java.nio.file.Path,java.nio.file.attribute.GroupPrincipal) throws java.io.IOException
meth protected java.nio.file.attribute.UserPrincipal toUser(java.nio.file.Path,java.nio.file.attribute.UserPrincipal) throws java.io.IOException
meth protected java.util.AbstractMap$SimpleImmutableEntry<java.nio.file.Path,java.lang.String> doReadLink(int,java.lang.String) throws java.io.IOException
meth protected java.util.Collection<java.lang.Integer> resolveAclSupportedCapabilities(org.apache.sshd.server.session.ServerSession)
meth protected java.util.List<org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser$OpenSSHExtension> appendOpenSSHExtensions(org.apache.sshd.common.util.buffer.Buffer,org.apache.sshd.server.session.ServerSession)
meth protected java.util.List<org.apache.sshd.sftp.common.extensions.openssh.AbstractOpenSSHExtensionParser$OpenSSHExtension> resolveOpenSSHExtensions(org.apache.sshd.server.session.ServerSession)
meth protected java.util.Map$Entry<java.lang.Integer,java.lang.String> checkVersionCompatibility(org.apache.sshd.common.util.buffer.Buffer,int,int,int) throws java.io.IOException
meth protected java.util.Map<java.lang.String,?> appendVendorIdExtension(org.apache.sshd.common.util.buffer.Buffer,java.util.Map<java.lang.String,?>,org.apache.sshd.server.session.ServerSession)
meth protected java.util.Map<java.lang.String,java.lang.Object> doLStat(int,java.lang.String,int) throws java.io.IOException
meth protected java.util.Map<java.lang.String,java.lang.Object> doStat(int,java.lang.String,int) throws java.io.IOException
meth protected java.util.Map<java.lang.String,java.lang.Object> readAttrs(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected java.util.Map<java.lang.String,org.apache.sshd.common.OptionalFeature> getSupportedClientExtensions(org.apache.sshd.server.session.ServerSession)
meth protected java.util.NavigableMap<java.lang.String,java.lang.Object> handleReadFileAttributesException(java.nio.file.Path,java.lang.String,java.nio.file.LinkOption[],java.io.IOException) throws java.io.IOException
meth protected org.apache.sshd.sftp.client.extensions.openssh.OpenSSHLimitsExtensionInfo getOpenSSHLimitsExtensionInfo(int,org.apache.sshd.server.channel.ChannelSession) throws java.io.IOException
meth protected org.apache.sshd.sftp.common.extensions.SpaceAvailableExtensionInfo doSpaceAvailable(int,java.lang.String) throws java.io.IOException
meth protected void appendExtensions(org.apache.sshd.common.util.buffer.Buffer,java.lang.String)
meth protected void appendSupported2Extension(org.apache.sshd.common.util.buffer.Buffer,java.util.Collection<java.lang.String>)
meth protected void appendSupportedExtension(org.apache.sshd.common.util.buffer.Buffer,java.util.Collection<java.lang.String>)
meth protected void doBlock(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doCheckFileHash(int,java.nio.file.Path,org.apache.sshd.common.NamedFactory<? extends org.apache.sshd.common.digest.Digest>,long,long,int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void doCheckFileHash(org.apache.sshd.common.util.buffer.Buffer,int,java.lang.String) throws java.io.IOException
meth protected void doClose(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doCopyData(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doCopyFile(int,java.lang.String,java.lang.String,boolean) throws java.io.IOException
meth protected void doCopyFile(int,java.lang.String,java.lang.String,java.util.Collection<java.nio.file.CopyOption>) throws java.io.IOException
meth protected void doCopyFile(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doExtended(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doFSetStat(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doFStat(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doLStat(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doLink(int,java.lang.String,java.lang.String,boolean) throws java.io.IOException
meth protected void doLink(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doMD5Hash(org.apache.sshd.common.util.buffer.Buffer,int,java.lang.String) throws java.io.IOException
meth protected void doMakeDirectory(int,java.lang.String,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth protected void doMakeDirectory(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doOpen(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doOpenDir(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doOpenSSHFsync(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doOpenSSHHardLink(int,java.lang.String,java.lang.String) throws java.io.IOException
meth protected void doOpenSSHHardLink(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doOpenSSHLimits(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doPosixRename(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doProcess(org.apache.sshd.common.util.buffer.Buffer,int,int,int) throws java.io.IOException
meth protected void doRead(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doReadLink(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doRealPath(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doRemove(int,java.nio.file.Path,boolean) throws java.io.IOException
meth protected void doRemove(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doRemoveDirectory(int,java.lang.String) throws java.io.IOException
meth protected void doRemoveDirectory(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doRemoveFile(int,java.lang.String) throws java.io.IOException
meth protected void doRename(int,java.lang.String,java.lang.String,int) throws java.io.IOException
meth protected void doRename(int,java.lang.String,java.lang.String,java.util.Collection<java.nio.file.CopyOption>) throws java.io.IOException
meth protected void doRename(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doSetAttributes(int,java.lang.String,java.nio.file.Path,java.util.Map<java.lang.String,?>,boolean) throws java.io.IOException
meth protected void doSetStat(int,java.lang.String,int,java.lang.String,java.util.Map<java.lang.String,?>,java.lang.Boolean) throws java.io.IOException
meth protected void doSetStat(org.apache.sshd.common.util.buffer.Buffer,int,java.lang.String,int,java.lang.Boolean) throws java.io.IOException
meth protected void doSpaceAvailable(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doStat(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doSymLink(int,java.lang.String,java.lang.String) throws java.io.IOException
meth protected void doSymLink(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doTextSeek(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doUnblock(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doUnsupported(org.apache.sshd.common.util.buffer.Buffer,int,int,int) throws java.io.IOException
meth protected void doUnsupportedExtension(org.apache.sshd.common.util.buffer.Buffer,int,java.lang.String) throws java.io.IOException
meth protected void doVersionSelect(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doWrite(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void executeExtendedCommand(org.apache.sshd.common.util.buffer.Buffer,int,java.lang.String) throws java.io.IOException
meth protected void handleSetFileAttributeFailure(java.nio.file.Path,java.lang.String,java.lang.String,java.lang.Object,java.util.Collection<java.lang.String>,java.lang.Exception) throws java.io.IOException
meth protected void handleUnsupportedAttributes(java.util.Collection<java.lang.String>)
meth protected void handleUserPrincipalLookupServiceException(java.lang.Class<? extends java.security.Principal>,java.lang.String,java.io.IOException) throws java.io.IOException
meth protected void process(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected void sendAttrs(org.apache.sshd.common.util.buffer.Buffer,int,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth protected void sendHandle(org.apache.sshd.common.util.buffer.Buffer,int,java.lang.String) throws java.io.IOException
meth protected void sendLink(org.apache.sshd.common.util.buffer.Buffer,int,java.nio.file.Path,java.lang.String) throws java.io.IOException
meth protected void sendPath(org.apache.sshd.common.util.buffer.Buffer,int,java.nio.file.Path,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth protected void sendStatus(org.apache.sshd.common.util.buffer.Buffer,int,int,java.lang.String) throws java.io.IOException
meth protected void sendStatus(org.apache.sshd.common.util.buffer.Buffer,int,int,java.lang.String,java.lang.String) throws java.io.IOException
meth protected void writeAttrs(org.apache.sshd.common.util.buffer.Buffer,java.util.Map<java.lang.String,?>)
meth protected void writeDirEntry(org.apache.sshd.server.session.ServerSession,int,org.apache.sshd.common.util.buffer.Buffer,int,java.nio.file.Path,java.lang.String,org.apache.sshd.sftp.client.SftpClient$Attributes) throws java.io.IOException
meth public boolean addSftpEventListener(org.apache.sshd.sftp.server.SftpEventListener)
meth public boolean removeSftpEventListener(org.apache.sshd.sftp.server.SftpEventListener)
meth public java.lang.Boolean checkSymlinkState(java.nio.file.Path,boolean,java.nio.file.LinkOption[])
meth public java.lang.Boolean validateParentExistWithNoSymlinksIfNeverFollowSymlinks(java.nio.file.Path,boolean)
meth public org.apache.sshd.server.channel.ChannelSession getServerChannelSession()
meth public org.apache.sshd.sftp.server.SftpErrorStatusDataHandler getErrorStatusDataHandler()
meth public org.apache.sshd.sftp.server.SftpEventListener getSftpEventListenerProxy()
meth public org.apache.sshd.sftp.server.SftpFileSystemAccessor getFileSystemAccessor()
meth public org.apache.sshd.sftp.server.UnsupportedAttributePolicy getUnsupportedAttributePolicy()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds channelSession,errorStatusDataHandler,fileSystemAccessor,sftpEventListenerProxy,sftpEventListeners,unsupportedAttributePolicy

CLSS public org.apache.sshd.sftp.server.DefaultGroupPrincipal
cons public init(java.lang.String)
intf java.nio.file.attribute.GroupPrincipal
supr org.apache.sshd.sftp.server.PrincipalBase

CLSS public org.apache.sshd.sftp.server.DefaultUserPrincipal
cons public init(java.lang.String)
intf java.nio.file.attribute.UserPrincipal
supr org.apache.sshd.sftp.server.PrincipalBase

CLSS public org.apache.sshd.sftp.server.DirectoryHandle
cons public init(org.apache.sshd.sftp.server.SftpSubsystem,java.nio.file.Path,java.lang.String) throws java.io.IOException
intf java.util.Iterator<java.nio.file.Path>
meth public boolean hasNext()
meth public boolean isDone()
meth public boolean isSendDot()
meth public boolean isSendDotDot()
meth public boolean isWithDots()
meth public java.nio.file.Path next()
meth public void close() throws java.io.IOException
meth public void markDone()
meth public void markDotDotSent()
meth public void markDotSent()
meth public void remove()
supr org.apache.sshd.sftp.server.Handle
hfds done,ds,fileList,sendDot,sendDotDot,withDots

CLSS public org.apache.sshd.sftp.server.FileHandle
cons public init(org.apache.sshd.sftp.server.SftpSubsystem,java.nio.file.Path,java.lang.String,int,int,java.util.Map<java.lang.String,java.lang.Object>) throws java.io.IOException
meth public boolean isOpenAppend()
meth public final java.util.Collection<java.nio.file.attribute.FileAttribute<?>> getFileAttributes()
meth public final java.util.Set<java.nio.file.StandardOpenOption> getOpenOptions()
meth public int getAccessMask()
meth public int read(byte[],int,int,long) throws java.io.IOException
meth public int read(byte[],int,int,long,java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>) throws java.io.IOException
meth public int read(byte[],long) throws java.io.IOException
meth public java.nio.channels.SeekableByteChannel getFileChannel()
meth public static java.nio.file.attribute.FileAttribute<?> toFileAttribute(java.lang.String,java.lang.Object)
meth public static java.util.Collection<java.nio.file.attribute.FileAttribute<?>> toFileAttributes(java.util.Map<java.lang.String,?>)
meth public static java.util.Set<java.nio.file.StandardOpenOption> getOpenOptions(int,int)
meth public void append(byte[]) throws java.io.IOException
meth public void append(byte[],int,int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void lock(long,long,int) throws java.io.IOException
meth public void unlock(long,long) throws java.io.IOException
meth public void write(byte[],int,int,long) throws java.io.IOException
meth public void write(byte[],long) throws java.io.IOException
supr org.apache.sshd.sftp.server.Handle
hfds access,fileAttributes,fileChannel,locks,openOptions

CLSS public abstract org.apache.sshd.sftp.server.Handle
cons protected init(org.apache.sshd.sftp.server.SftpSubsystem,java.nio.file.Path,java.lang.String)
intf java.nio.channels.Channel
intf org.apache.sshd.common.AttributeStore
meth protected org.apache.sshd.sftp.server.SftpSubsystem getSubsystem()
meth protected static java.lang.String safe(java.lang.String)
meth protected void signalHandleOpen() throws java.io.IOException
meth protected void signalHandleOpening() throws java.io.IOException
meth public <%0 extends java.lang.Object> {%%0} computeAttributeIfAbsent(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,java.util.function.Function<? super org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,? extends {%%0}>)
meth public <%0 extends java.lang.Object> {%%0} getAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} removeAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} setAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,{%%0})
meth public boolean isOpen()
meth public int getAttributesCount()
meth public java.lang.String getFileHandle()
meth public java.lang.String toString()
meth public java.nio.file.Path getFile()
meth public java.util.Collection<org.apache.sshd.common.AttributeRepository$AttributeKey<?>> attributeKeys()
meth public void clearAttributes()
meth public void close() throws java.io.IOException
supr java.lang.Object
hfds attributes,closed,file,handle,sftpSubsystem

CLSS public org.apache.sshd.sftp.server.InvalidHandleException
cons public init(java.lang.String,org.apache.sshd.sftp.server.Handle,java.lang.Class<? extends org.apache.sshd.sftp.server.Handle>)
supr java.io.IOException
hfds serialVersionUID

CLSS public org.apache.sshd.sftp.server.PrincipalBase
cons public init(java.lang.String)
intf java.security.Principal
meth public boolean equals(java.lang.Object)
meth public final java.lang.String getName()
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds name

CLSS public abstract interface org.apache.sshd.sftp.server.SftpErrorDataChannelReceiverProvider
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.server.channel.ChannelDataReceiver getErrorChannelDataReceiver()

CLSS public abstract interface org.apache.sshd.sftp.server.SftpErrorStatusDataHandler
fld public final static org.apache.sshd.sftp.server.SftpErrorStatusDataHandler DEFAULT
meth public !varargs int resolveSubStatus(org.apache.sshd.sftp.server.SftpSubsystemEnvironment,int,java.lang.Throwable,int,java.lang.Object[])
meth public !varargs java.lang.String resolveErrorLanguage(org.apache.sshd.sftp.server.SftpSubsystemEnvironment,int,java.lang.Throwable,int,int,java.lang.Object[])
meth public !varargs java.lang.String resolveErrorMessage(org.apache.sshd.sftp.server.SftpSubsystemEnvironment,int,java.lang.Throwable,int,int,java.lang.Object[])

CLSS public abstract interface org.apache.sshd.sftp.server.SftpErrorStatusDataHandlerProvider
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.sftp.server.SftpErrorStatusDataHandler getErrorStatusDataHandler()

CLSS public abstract interface org.apache.sshd.sftp.server.SftpEventListener
intf org.apache.sshd.common.util.SshdEventListener
meth public static <%0 extends org.apache.sshd.sftp.server.SftpEventListener> {%%0} validateListener({%%0})
meth public void blocked(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.FileHandle,long,long,int,java.lang.Throwable) throws java.io.IOException
meth public void blocking(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.FileHandle,long,long,int) throws java.io.IOException
meth public void closed(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.Handle,java.lang.Throwable) throws java.io.IOException
meth public void closing(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.Handle) throws java.io.IOException
meth public void created(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.util.Map<java.lang.String,?>,java.lang.Throwable) throws java.io.IOException
meth public void creating(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public void destroying(org.apache.sshd.server.session.ServerSession) throws java.io.IOException
meth public void exiting(org.apache.sshd.server.session.ServerSession,org.apache.sshd.sftp.server.Handle) throws java.io.IOException
meth public void initialized(org.apache.sshd.server.session.ServerSession,int) throws java.io.IOException
meth public void linked(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.nio.file.Path,boolean,java.lang.Throwable) throws java.io.IOException
meth public void linking(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.nio.file.Path,boolean) throws java.io.IOException
meth public void modifiedAttributes(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.util.Map<java.lang.String,?>,java.lang.Throwable) throws java.io.IOException
meth public void modifyingAttributes(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public void moved(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.nio.file.Path,java.util.Collection<java.nio.file.CopyOption>,java.lang.Throwable) throws java.io.IOException
meth public void moving(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.nio.file.Path,java.util.Collection<java.nio.file.CopyOption>) throws java.io.IOException
meth public void open(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.Handle) throws java.io.IOException
meth public void openFailed(org.apache.sshd.server.session.ServerSession,java.lang.String,java.nio.file.Path,boolean,java.lang.Throwable) throws java.io.IOException
meth public void opening(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.Handle) throws java.io.IOException
meth public void read(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.FileHandle,long,byte[],int,int,int,java.lang.Throwable) throws java.io.IOException
meth public void readEntries(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.DirectoryHandle,java.util.Map<java.lang.String,java.nio.file.Path>) throws java.io.IOException
meth public void reading(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.FileHandle,long,byte[],int,int) throws java.io.IOException
meth public void readingEntries(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.DirectoryHandle) throws java.io.IOException
meth public void received(org.apache.sshd.server.session.ServerSession,int,int) throws java.io.IOException
meth public void receivedExtension(org.apache.sshd.server.session.ServerSession,java.lang.String,int) throws java.io.IOException
meth public void removed(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,boolean,java.lang.Throwable) throws java.io.IOException
meth public void removing(org.apache.sshd.server.session.ServerSession,java.nio.file.Path,boolean) throws java.io.IOException
meth public void unblocked(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.FileHandle,long,long,java.lang.Throwable) throws java.io.IOException
meth public void unblocking(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.FileHandle,long,long) throws java.io.IOException
meth public void writing(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.FileHandle,long,byte[],int,int) throws java.io.IOException
meth public void written(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.sftp.server.FileHandle,long,byte[],int,int,java.lang.Throwable) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.sftp.server.SftpEventListenerManager
meth public abstract boolean addSftpEventListener(org.apache.sshd.sftp.server.SftpEventListener)
meth public abstract boolean removeSftpEventListener(org.apache.sshd.sftp.server.SftpEventListener)
meth public abstract org.apache.sshd.sftp.server.SftpEventListener getSftpEventListenerProxy()

CLSS public abstract interface org.apache.sshd.sftp.server.SftpFileSystemAccessor
fld public final static boolean DEFAULT_AUTO_SYNC_FILE_ON_CLOSE = true
fld public final static java.lang.String PROP_AUTO_SYNC_FILE_ON_CLOSE = "sftp-auto-fsync-on-close"
fld public final static java.util.List<java.lang.String> DEFAULT_UNIX_VIEW
fld public final static java.util.NavigableMap<java.lang.String,org.apache.sshd.common.util.io.FileInfoExtractor<?>> FILEATTRS_RESOLVERS
fld public final static org.apache.sshd.sftp.server.SftpFileSystemAccessor DEFAULT
meth public !varargs java.nio.channels.SeekableByteChannel openFile(org.apache.sshd.sftp.server.SftpSubsystemProxy,org.apache.sshd.sftp.server.FileHandle,java.nio.file.Path,java.lang.String,java.util.Set<? extends java.nio.file.OpenOption>,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs java.nio.file.DirectoryStream<java.nio.file.Path> openDirectory(org.apache.sshd.sftp.server.SftpSubsystemProxy,org.apache.sshd.sftp.server.DirectoryHandle,java.nio.file.Path,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs java.util.Map<java.lang.String,?> readFileAttributes(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs java.util.NavigableMap<java.lang.String,java.lang.Object> resolveReportedFileAttributes(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,int,java.util.NavigableMap<java.lang.String,java.lang.Object>,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs static java.nio.channels.SeekableByteChannel seekableByteChannelNoLinkFollow(java.nio.file.Path,java.util.Set<? extends java.nio.file.OpenOption>,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs void applyExtensionFileAttributes(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,java.util.Map<java.lang.String,byte[]>,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void setFileAccessControl(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,java.util.List<java.nio.file.attribute.AclEntry>,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void setFileAttribute(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,java.lang.String,java.lang.String,java.lang.Object,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void setFileOwner(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,java.security.Principal,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void setFilePermissions(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,java.util.Set<java.nio.file.attribute.PosixFilePermission>,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void setGroupOwner(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,java.security.Principal,java.nio.file.LinkOption[]) throws java.io.IOException
meth public boolean noFollow(java.util.Collection<?>)
meth public java.lang.String resolveLinkTarget(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path) throws java.io.IOException
meth public java.nio.channels.FileLock tryLock(org.apache.sshd.sftp.server.SftpSubsystemProxy,org.apache.sshd.sftp.server.FileHandle,java.nio.file.Path,java.lang.String,java.nio.channels.Channel,long,long,boolean) throws java.io.IOException
meth public java.nio.file.LinkOption[] resolveFileAccessLinkOptions(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,int,java.lang.String,boolean) throws java.io.IOException
meth public java.nio.file.Path resolveLocalFilePath(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,java.lang.String) throws java.io.IOException
meth public java.nio.file.attribute.GroupPrincipal resolveGroupOwner(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,java.nio.file.attribute.GroupPrincipal) throws java.io.IOException
meth public java.nio.file.attribute.UserPrincipal resolveFileOwner(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,java.nio.file.attribute.UserPrincipal) throws java.io.IOException
meth public static java.nio.file.SecureDirectoryStream<java.nio.file.Path> secure(java.nio.file.DirectoryStream<java.nio.file.Path>)
meth public static java.nio.file.SecureDirectoryStream<java.nio.file.Path> secureResolveDirectoryStream(java.nio.file.Path) throws java.io.IOException
meth public void closeDirectory(org.apache.sshd.sftp.server.SftpSubsystemProxy,org.apache.sshd.sftp.server.DirectoryHandle,java.nio.file.Path,java.lang.String,java.nio.file.DirectoryStream<java.nio.file.Path>) throws java.io.IOException
meth public void closeFile(org.apache.sshd.sftp.server.SftpSubsystemProxy,org.apache.sshd.sftp.server.FileHandle,java.nio.file.Path,java.lang.String,java.nio.channels.Channel,java.util.Set<? extends java.nio.file.OpenOption>) throws java.io.IOException
meth public void copyFile(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,java.nio.file.Path,java.util.Collection<java.nio.file.CopyOption>) throws java.io.IOException
meth public void createDirectory(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path) throws java.io.IOException
meth public void createLink(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,java.nio.file.Path,boolean) throws java.io.IOException
meth public void putRemoteFileName(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,org.apache.sshd.common.util.buffer.Buffer,java.lang.String,boolean) throws java.io.IOException
meth public void removeFile(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,boolean) throws java.io.IOException
meth public void renameFile(org.apache.sshd.sftp.server.SftpSubsystemProxy,java.nio.file.Path,java.nio.file.Path,java.util.Collection<java.nio.file.CopyOption>) throws java.io.IOException
meth public void syncFileData(org.apache.sshd.sftp.server.SftpSubsystemProxy,org.apache.sshd.sftp.server.FileHandle,java.nio.file.Path,java.lang.String,java.nio.channels.Channel) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.sftp.server.SftpFileSystemAccessorManager
intf org.apache.sshd.sftp.server.SftpFileSystemAccessorProvider
meth public abstract void setFileSystemAccessor(org.apache.sshd.sftp.server.SftpFileSystemAccessor)

CLSS public abstract interface org.apache.sshd.sftp.server.SftpFileSystemAccessorProvider
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.sftp.server.SftpFileSystemAccessor getFileSystemAccessor()

CLSS public org.apache.sshd.sftp.server.SftpSubsystem
cons public init(org.apache.sshd.server.channel.ChannelSession,org.apache.sshd.sftp.server.SftpSubsystemConfigurator)
fld protected byte[] workBuf
fld protected final java.util.Map<java.lang.String,byte[]> extensions
fld protected final java.util.Map<java.lang.String,org.apache.sshd.sftp.server.Handle> handles
fld protected final java.util.concurrent.BlockingQueue<org.apache.sshd.common.util.buffer.Buffer> requests
fld protected final java.util.concurrent.atomic.AtomicBoolean closed
fld protected final java.util.concurrent.atomic.AtomicLong requestsCount
fld protected final org.apache.sshd.common.util.buffer.Buffer buffer
fld protected final static org.apache.sshd.common.util.buffer.Buffer CLOSE
fld protected int fileHandleSize
fld protected int maxFileHandleRounds
fld protected int maxHandleCount
fld protected int version
fld protected java.nio.file.FileSystem fileSystem
fld protected java.nio.file.Path defaultDir
fld protected java.util.Deque<java.lang.String> unusedHandles
fld protected java.util.concurrent.Future<?> pendingFuture
fld protected org.apache.sshd.common.io.IoOutputStream out
fld protected org.apache.sshd.common.random.Random randomizer
fld protected org.apache.sshd.common.util.threads.CloseableExecutorService executorService
fld protected org.apache.sshd.server.Environment env
fld protected org.apache.sshd.server.ExitCallback callback
intf java.lang.Runnable
intf org.apache.sshd.common.file.FileSystemAware
intf org.apache.sshd.common.util.threads.ExecutorServiceCarrier
intf org.apache.sshd.server.channel.ChannelDataReceiver
intf org.apache.sshd.server.command.AsyncCommand
meth protected !varargs java.lang.String doOpenDir(int,java.lang.String,java.nio.file.Path,java.nio.file.LinkOption[]) throws java.io.IOException
meth protected byte[] doMD5Hash(int,java.lang.String,java.lang.String,long,long,byte[]) throws java.lang.Exception
meth protected int doRead(int,java.lang.String,long,int,byte[],int,java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>) throws java.io.IOException
meth protected java.lang.String doOpen(int,java.lang.String,int,int,java.util.Map<java.lang.String,java.lang.Object>) throws java.io.IOException
meth protected java.lang.String generateFileHandle(java.nio.file.Path) throws java.io.IOException
meth protected java.util.Map<java.lang.String,java.lang.Object> doFStat(int,java.lang.String,int) throws java.io.IOException
meth protected org.apache.sshd.common.util.buffer.Buffer prepareReply(org.apache.sshd.common.util.buffer.Buffer)
meth protected org.apache.sshd.server.channel.ChannelDataReceiver resolveErrorDataChannelReceiver(org.apache.sshd.server.channel.ChannelSession,org.apache.sshd.server.channel.ChannelDataReceiver)
meth protected void closeAllHandles()
meth protected void createLink(int,java.lang.String,java.lang.String,boolean) throws java.io.IOException
meth protected void doBlock(int,java.lang.String,long,long,int) throws java.io.IOException
meth protected void doCheckFileHash(int,java.lang.String,java.lang.String,java.util.Collection<java.lang.String>,long,long,int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void doClose(int,java.lang.String) throws java.io.IOException
meth protected void doCopyData(int,java.lang.String,long,long,java.lang.String,long) throws java.io.IOException
meth protected void doFSetStat(int,java.lang.String,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth protected void doInit(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doOpenSSHFsync(int,java.lang.String) throws java.io.IOException
meth protected void doProcess(org.apache.sshd.common.util.buffer.Buffer,int,int,int) throws java.io.IOException
meth protected void doReadDir(org.apache.sshd.common.util.buffer.Buffer,int) throws java.io.IOException
meth protected void doTextSeek(int,java.lang.String,long) throws java.io.IOException
meth protected void doUnblock(int,java.lang.String,long,long) throws java.io.IOException
meth protected void doVersionSelect(org.apache.sshd.common.util.buffer.Buffer,int,java.lang.String) throws java.io.IOException
meth protected void doWrite(int,java.lang.String,long,int,byte[],int,int) throws java.io.IOException
meth protected void initializeSessionRelatedMember(org.apache.sshd.server.session.ServerSession,org.apache.sshd.server.channel.ChannelSession)
meth protected void send(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public int data(org.apache.sshd.server.channel.ChannelSession,byte[],int,int) throws java.io.IOException
meth public int getVersion()
meth public java.nio.file.Path getDefaultDirectory()
meth public org.apache.sshd.common.util.threads.CloseableExecutorService getExecutorService()
meth public org.apache.sshd.server.session.ServerSession getServerSession()
meth public void close() throws java.io.IOException
meth public void destroy(org.apache.sshd.server.channel.ChannelSession)
meth public void run()
meth public void setErrorStream(java.io.OutputStream)
meth public void setExitCallback(org.apache.sshd.server.ExitCallback)
meth public void setFileSystem(java.nio.file.FileSystem)
meth public void setInputStream(java.io.InputStream)
meth public void setIoErrorStream(org.apache.sshd.common.io.IoOutputStream)
meth public void setIoInputStream(org.apache.sshd.common.io.IoInputStream)
meth public void setIoOutputStream(org.apache.sshd.common.io.IoOutputStream)
meth public void setOutputStream(java.io.OutputStream)
meth public void start(org.apache.sshd.server.channel.ChannelSession,org.apache.sshd.server.Environment) throws java.io.IOException
supr org.apache.sshd.sftp.server.AbstractSftpSubsystemHelper
hfds serverSession

CLSS public abstract interface org.apache.sshd.sftp.server.SftpSubsystemConfigurator
intf org.apache.sshd.common.util.threads.ExecutorServiceCarrier
intf org.apache.sshd.sftp.server.SftpErrorDataChannelReceiverProvider
intf org.apache.sshd.sftp.server.SftpErrorStatusDataHandlerProvider
intf org.apache.sshd.sftp.server.SftpFileSystemAccessorProvider
intf org.apache.sshd.sftp.server.SftpUnsupportedAttributePolicyProvider

CLSS public abstract interface org.apache.sshd.sftp.server.SftpSubsystemEnvironment
fld public final static int HIGHER_SFTP_IMPL = 6
fld public final static int LOWER_SFTP_IMPL = 3
fld public final static java.lang.String ALL_SFTP_IMPL
fld public final static java.util.List<java.lang.Integer> SUPPORTED_SFTP_VERSIONS
intf org.apache.sshd.common.session.SessionHolder<org.apache.sshd.server.session.ServerSession>
intf org.apache.sshd.server.channel.ServerChannelSessionHolder
intf org.apache.sshd.server.session.ServerSessionHolder
intf org.apache.sshd.sftp.server.SftpFileSystemAccessorProvider
intf org.apache.sshd.sftp.server.SftpUnsupportedAttributePolicyProvider
meth public abstract int getVersion()
meth public abstract java.nio.file.Path getDefaultDirectory()
meth public org.apache.sshd.server.session.ServerSession getSession()

CLSS public org.apache.sshd.sftp.server.SftpSubsystemFactory
cons public init()
fld public final static java.lang.String NAME = "sftp"
fld public final static org.apache.sshd.sftp.server.UnsupportedAttributePolicy DEFAULT_POLICY
innr public static Builder
intf org.apache.sshd.common.util.threads.ManagedExecutorServiceSupplier
intf org.apache.sshd.server.subsystem.SubsystemFactory
intf org.apache.sshd.sftp.server.SftpFileSystemAccessorManager
intf org.apache.sshd.sftp.server.SftpSubsystemConfigurator
meth public java.lang.String getName()
meth public java.util.function.Supplier<? extends org.apache.sshd.common.util.threads.CloseableExecutorService> getExecutorServiceProvider()
meth public org.apache.sshd.common.util.threads.CloseableExecutorService getExecutorService()
meth public org.apache.sshd.server.channel.ChannelDataReceiver getErrorChannelDataReceiver()
meth public org.apache.sshd.server.command.Command createSubsystem(org.apache.sshd.server.channel.ChannelSession) throws java.io.IOException
meth public org.apache.sshd.sftp.server.SftpErrorStatusDataHandler getErrorStatusDataHandler()
meth public org.apache.sshd.sftp.server.SftpFileSystemAccessor getFileSystemAccessor()
meth public org.apache.sshd.sftp.server.UnsupportedAttributePolicy getUnsupportedAttributePolicy()
meth public void setErrorChannelDataReceiver(org.apache.sshd.server.channel.ChannelDataReceiver)
meth public void setErrorStatusDataHandler(org.apache.sshd.sftp.server.SftpErrorStatusDataHandler)
meth public void setExecutorServiceProvider(java.util.function.Supplier<? extends org.apache.sshd.common.util.threads.CloseableExecutorService>)
meth public void setFileSystemAccessor(org.apache.sshd.sftp.server.SftpFileSystemAccessor)
meth public void setUnsupportedAttributePolicy(org.apache.sshd.sftp.server.UnsupportedAttributePolicy)
supr org.apache.sshd.sftp.server.AbstractSftpEventListenerManager
hfds errorChannelDataReceiver,errorStatusDataHandler,executorsProvider,fileSystemAccessor,policy

CLSS public static org.apache.sshd.sftp.server.SftpSubsystemFactory$Builder
 outer org.apache.sshd.sftp.server.SftpSubsystemFactory
cons public init()
intf org.apache.sshd.common.util.ObjectBuilder<org.apache.sshd.sftp.server.SftpSubsystemFactory>
meth public org.apache.sshd.sftp.server.SftpSubsystemFactory build()
meth public org.apache.sshd.sftp.server.SftpSubsystemFactory$Builder withErrorChannelDataReceiver(org.apache.sshd.server.channel.ChannelDataReceiver)
meth public org.apache.sshd.sftp.server.SftpSubsystemFactory$Builder withExecutorServiceProvider(java.util.function.Supplier<? extends org.apache.sshd.common.util.threads.CloseableExecutorService>)
meth public org.apache.sshd.sftp.server.SftpSubsystemFactory$Builder withFileSystemAccessor(org.apache.sshd.sftp.server.SftpFileSystemAccessor)
meth public org.apache.sshd.sftp.server.SftpSubsystemFactory$Builder withSftpErrorStatusDataHandler(org.apache.sshd.sftp.server.SftpErrorStatusDataHandler)
meth public org.apache.sshd.sftp.server.SftpSubsystemFactory$Builder withUnsupportedAttributePolicy(org.apache.sshd.sftp.server.UnsupportedAttributePolicy)
supr org.apache.sshd.sftp.server.AbstractSftpEventListenerManager
hfds errorChannelDataReceiver,errorStatusDataHandler,executorsProvider,fileSystemAccessor,policy

CLSS public abstract interface org.apache.sshd.sftp.server.SftpSubsystemProxy
intf org.apache.sshd.sftp.server.SftpErrorStatusDataHandlerProvider
intf org.apache.sshd.sftp.server.SftpEventListenerManager
intf org.apache.sshd.sftp.server.SftpSubsystemEnvironment

CLSS public abstract interface org.apache.sshd.sftp.server.SftpUnsupportedAttributePolicyProvider
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.sftp.server.UnsupportedAttributePolicy getUnsupportedAttributePolicy()

CLSS public final org.apache.sshd.sftp.server.UnixDateFormat
fld public final static java.util.List<java.lang.String> MONTHS
fld public final static long SIX_MONTHS = 15811200000
meth public static java.lang.String getUnixDate(java.nio.file.attribute.FileTime)
meth public static java.lang.String getUnixDate(long)
supr java.lang.Object

CLSS public final !enum org.apache.sshd.sftp.server.UnsupportedAttributePolicy
fld public final static java.util.Set<org.apache.sshd.sftp.server.UnsupportedAttributePolicy> VALUES
fld public final static org.apache.sshd.sftp.server.UnsupportedAttributePolicy Ignore
fld public final static org.apache.sshd.sftp.server.UnsupportedAttributePolicy ThrowException
fld public final static org.apache.sshd.sftp.server.UnsupportedAttributePolicy Warn
meth public static org.apache.sshd.sftp.server.UnsupportedAttributePolicy valueOf(java.lang.String)
meth public static org.apache.sshd.sftp.server.UnsupportedAttributePolicy[] values()
supr java.lang.Enum<org.apache.sshd.sftp.server.UnsupportedAttributePolicy>

