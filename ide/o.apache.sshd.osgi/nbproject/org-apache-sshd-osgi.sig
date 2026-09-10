#Signature file v4.1
#Version 2.17.1

CLSS public java.io.ByteArrayOutputStream
cons public init()
cons public init(int)
fld protected byte[] buf
fld protected int count
meth public byte[] toByteArray()
meth public int size()
meth public java.lang.String toString()
meth public java.lang.String toString(int)
 anno 0 java.lang.Deprecated()
meth public java.lang.String toString(java.lang.String) throws java.io.UnsupportedEncodingException
meth public void close() throws java.io.IOException
meth public void reset()
meth public void write(byte[],int,int)
meth public void write(int)
meth public void writeTo(java.io.OutputStream) throws java.io.IOException
supr java.io.OutputStream

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public java.io.FilterInputStream
cons protected init(java.io.InputStream)
fld protected volatile java.io.InputStream in
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.io.InputStream

CLSS public java.io.FilterOutputStream
cons public init(java.io.OutputStream)
fld protected java.io.OutputStream out
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream

CLSS public abstract java.io.FilterReader
cons protected init(java.io.Reader)
fld protected java.io.Reader in
meth public boolean markSupported()
meth public boolean ready() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(char[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void mark(int) throws java.io.IOException
meth public void reset() throws java.io.IOException
supr java.io.Reader

CLSS public abstract java.io.FilterWriter
cons protected init(java.io.Writer)
fld protected java.io.Writer out
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(char[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
meth public void write(java.lang.String,int,int) throws java.io.IOException
supr java.io.Writer

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

CLSS public java.io.PrintStream
cons public init(java.io.File) throws java.io.FileNotFoundException
cons public init(java.io.File,java.lang.String) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
cons public init(java.io.OutputStream)
cons public init(java.io.OutputStream,boolean)
cons public init(java.io.OutputStream,boolean,java.lang.String) throws java.io.UnsupportedEncodingException
cons public init(java.lang.String) throws java.io.FileNotFoundException
cons public init(java.lang.String,java.lang.String) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
intf java.io.Closeable
intf java.lang.Appendable
meth protected void clearError()
meth protected void setError()
meth public !varargs java.io.PrintStream format(java.lang.String,java.lang.Object[])
meth public !varargs java.io.PrintStream format(java.util.Locale,java.lang.String,java.lang.Object[])
meth public !varargs java.io.PrintStream printf(java.lang.String,java.lang.Object[])
meth public !varargs java.io.PrintStream printf(java.util.Locale,java.lang.String,java.lang.Object[])
meth public boolean checkError()
meth public java.io.PrintStream append(char)
meth public java.io.PrintStream append(java.lang.CharSequence)
meth public java.io.PrintStream append(java.lang.CharSequence,int,int)
meth public void close()
meth public void flush()
meth public void print(boolean)
meth public void print(char)
meth public void print(char[])
meth public void print(double)
meth public void print(float)
meth public void print(int)
meth public void print(java.lang.Object)
meth public void print(java.lang.String)
meth public void print(long)
meth public void println()
meth public void println(boolean)
meth public void println(char)
meth public void println(char[])
meth public void println(double)
meth public void println(float)
meth public void println(int)
meth public void println(java.lang.Object)
meth public void println(java.lang.String)
meth public void println(long)
meth public void write(byte[],int,int)
meth public void write(int)
supr java.io.FilterOutputStream

CLSS public abstract java.io.Reader
cons protected init()
cons protected init(java.lang.Object)
fld protected java.lang.Object lock
intf java.io.Closeable
intf java.lang.Readable
meth public abstract int read(char[],int,int) throws java.io.IOException
meth public abstract void close() throws java.io.IOException
meth public boolean markSupported()
meth public boolean ready() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(char[]) throws java.io.IOException
meth public int read(java.nio.CharBuffer) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void mark(int) throws java.io.IOException
meth public void reset() throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

CLSS public abstract java.io.Writer
cons protected init()
cons protected init(java.lang.Object)
fld protected java.lang.Object lock
intf java.io.Closeable
intf java.io.Flushable
intf java.lang.Appendable
meth public abstract void close() throws java.io.IOException
meth public abstract void flush() throws java.io.IOException
meth public abstract void write(char[],int,int) throws java.io.IOException
meth public java.io.Writer append(char) throws java.io.IOException
meth public java.io.Writer append(java.lang.CharSequence) throws java.io.IOException
meth public java.io.Writer append(java.lang.CharSequence,int,int) throws java.io.IOException
meth public void write(char[]) throws java.io.IOException
meth public void write(int) throws java.io.IOException
meth public void write(java.lang.String) throws java.io.IOException
meth public void write(java.lang.String,int,int) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface java.lang.Appendable
meth public abstract java.lang.Appendable append(char) throws java.io.IOException
meth public abstract java.lang.Appendable append(java.lang.CharSequence) throws java.io.IOException
meth public abstract java.lang.Appendable append(java.lang.CharSequence,int,int) throws java.io.IOException

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

CLSS public java.lang.IllegalStateException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException

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

CLSS public abstract interface java.lang.Readable
meth public abstract int read(java.nio.CharBuffer) throws java.io.IOException

CLSS public abstract interface java.lang.Runnable
 anno 0 java.lang.FunctionalInterface()
meth public abstract void run()

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

CLSS public abstract interface java.lang.reflect.InvocationHandler
meth public abstract java.lang.Object invoke(java.lang.Object,java.lang.reflect.Method,java.lang.Object[]) throws java.lang.Throwable

CLSS public abstract java.net.SocketAddress
cons public init()
intf java.io.Serializable
supr java.lang.Object

CLSS public abstract interface java.nio.channels.Channel
intf java.io.Closeable
meth public abstract boolean isOpen()
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.nio.channels.CompletionHandler<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract void completed({java.nio.channels.CompletionHandler%0},{java.nio.channels.CompletionHandler%1})
meth public abstract void failed(java.lang.Throwable,{java.nio.channels.CompletionHandler%1})

CLSS public abstract interface java.nio.file.DirectoryStream<%0 extends java.lang.Object>
innr public abstract interface static Filter
intf java.io.Closeable
intf java.lang.Iterable<{java.nio.file.DirectoryStream%0}>
meth public abstract java.util.Iterator<{java.nio.file.DirectoryStream%0}> iterator()

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

CLSS public abstract interface java.nio.file.SecureDirectoryStream<%0 extends java.lang.Object>
intf java.nio.file.DirectoryStream<{java.nio.file.SecureDirectoryStream%0}>
meth public abstract !varargs <%0 extends java.nio.file.attribute.FileAttributeView> {%%0} getFileAttributeView({java.nio.file.SecureDirectoryStream%0},java.lang.Class<{%%0}>,java.nio.file.LinkOption[])
meth public abstract !varargs java.nio.channels.SeekableByteChannel newByteChannel({java.nio.file.SecureDirectoryStream%0},java.util.Set<? extends java.nio.file.OpenOption>,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public abstract !varargs java.nio.file.SecureDirectoryStream<{java.nio.file.SecureDirectoryStream%0}> newDirectoryStream({java.nio.file.SecureDirectoryStream%0},java.nio.file.LinkOption[]) throws java.io.IOException
meth public abstract <%0 extends java.nio.file.attribute.FileAttributeView> {%%0} getFileAttributeView(java.lang.Class<{%%0}>)
meth public abstract void deleteDirectory({java.nio.file.SecureDirectoryStream%0}) throws java.io.IOException
meth public abstract void deleteFile({java.nio.file.SecureDirectoryStream%0}) throws java.io.IOException
meth public abstract void move({java.nio.file.SecureDirectoryStream%0},java.nio.file.SecureDirectoryStream<{java.nio.file.SecureDirectoryStream%0}>,{java.nio.file.SecureDirectoryStream%0}) throws java.io.IOException

CLSS public abstract interface java.nio.file.Watchable
meth public abstract !varargs java.nio.file.WatchKey register(java.nio.file.WatchService,java.nio.file.WatchEvent$Kind<?>[]) throws java.io.IOException
meth public abstract !varargs java.nio.file.WatchKey register(java.nio.file.WatchService,java.nio.file.WatchEvent$Kind<?>[],java.nio.file.WatchEvent$Modifier[]) throws java.io.IOException

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

CLSS public abstract interface java.security.Key
fld public final static long serialVersionUID = 6603384152749567654
intf java.io.Serializable
meth public abstract byte[] getEncoded()
meth public abstract java.lang.String getAlgorithm()
meth public abstract java.lang.String getFormat()

CLSS public abstract interface java.security.PrivateKey
fld public final static long serialVersionUID = 6034044314589513430
intf java.security.Key
intf javax.security.auth.Destroyable

CLSS public abstract interface java.security.PublicKey
fld public final static long serialVersionUID = 7187392471159151072
intf java.security.Key

CLSS public abstract interface java.security.spec.AlgorithmParameterSpec

CLSS public abstract interface java.util.EventListener

CLSS public abstract interface java.util.Iterator<%0 extends java.lang.Object>
meth public abstract boolean hasNext()
meth public abstract {java.util.Iterator%0} next()
meth public void forEachRemaining(java.util.function.Consumer<? super {java.util.Iterator%0}>)
meth public void remove()

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

CLSS public abstract java.util.concurrent.AbstractExecutorService
cons public init()
intf java.util.concurrent.ExecutorService
meth protected <%0 extends java.lang.Object> java.util.concurrent.RunnableFuture<{%%0}> newTaskFor(java.lang.Runnable,{%%0})
meth protected <%0 extends java.lang.Object> java.util.concurrent.RunnableFuture<{%%0}> newTaskFor(java.util.concurrent.Callable<{%%0}>)
meth public <%0 extends java.lang.Object> java.util.List<java.util.concurrent.Future<{%%0}>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>) throws java.lang.InterruptedException
meth public <%0 extends java.lang.Object> java.util.List<java.util.concurrent.Future<{%%0}>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>,long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.lang.Runnable,{%%0})
meth public <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.util.concurrent.Callable<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException
meth public <%0 extends java.lang.Object> {%%0} invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>,long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException,java.util.concurrent.TimeoutException
meth public java.util.concurrent.Future<?> submit(java.lang.Runnable)
supr java.lang.Object

CLSS public abstract interface java.util.concurrent.Executor
meth public abstract void execute(java.lang.Runnable)

CLSS public abstract interface java.util.concurrent.ExecutorService
intf java.util.concurrent.Executor
meth public abstract <%0 extends java.lang.Object> java.util.List<java.util.concurrent.Future<{%%0}>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>) throws java.lang.InterruptedException
meth public abstract <%0 extends java.lang.Object> java.util.List<java.util.concurrent.Future<{%%0}>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>,long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.lang.Runnable,{%%0})
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.util.concurrent.Callable<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException
meth public abstract <%0 extends java.lang.Object> {%%0} invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>,long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException,java.util.concurrent.TimeoutException
meth public abstract boolean awaitTermination(long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public abstract boolean isShutdown()
meth public abstract boolean isTerminated()
meth public abstract java.util.List<java.lang.Runnable> shutdownNow()
meth public abstract java.util.concurrent.Future<?> submit(java.lang.Runnable)
meth public abstract void shutdown()

CLSS public abstract interface java.util.concurrent.ThreadFactory
meth public abstract java.lang.Thread newThread(java.lang.Runnable)

CLSS public java.util.concurrent.ThreadPoolExecutor
cons public init(int,int,long,java.util.concurrent.TimeUnit,java.util.concurrent.BlockingQueue<java.lang.Runnable>)
cons public init(int,int,long,java.util.concurrent.TimeUnit,java.util.concurrent.BlockingQueue<java.lang.Runnable>,java.util.concurrent.RejectedExecutionHandler)
cons public init(int,int,long,java.util.concurrent.TimeUnit,java.util.concurrent.BlockingQueue<java.lang.Runnable>,java.util.concurrent.ThreadFactory)
cons public init(int,int,long,java.util.concurrent.TimeUnit,java.util.concurrent.BlockingQueue<java.lang.Runnable>,java.util.concurrent.ThreadFactory,java.util.concurrent.RejectedExecutionHandler)
innr public static AbortPolicy
innr public static CallerRunsPolicy
innr public static DiscardOldestPolicy
innr public static DiscardPolicy
meth protected void afterExecute(java.lang.Runnable,java.lang.Throwable)
meth protected void beforeExecute(java.lang.Thread,java.lang.Runnable)
meth protected void finalize()
meth protected void terminated()
meth public boolean allowsCoreThreadTimeOut()
meth public boolean awaitTermination(long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public boolean isShutdown()
meth public boolean isTerminated()
meth public boolean isTerminating()
meth public boolean prestartCoreThread()
meth public boolean remove(java.lang.Runnable)
meth public int getActiveCount()
meth public int getCorePoolSize()
meth public int getLargestPoolSize()
meth public int getMaximumPoolSize()
meth public int getPoolSize()
meth public int prestartAllCoreThreads()
meth public java.lang.String toString()
meth public java.util.List<java.lang.Runnable> shutdownNow()
meth public java.util.concurrent.BlockingQueue<java.lang.Runnable> getQueue()
meth public java.util.concurrent.RejectedExecutionHandler getRejectedExecutionHandler()
meth public java.util.concurrent.ThreadFactory getThreadFactory()
meth public long getCompletedTaskCount()
meth public long getKeepAliveTime(java.util.concurrent.TimeUnit)
meth public long getTaskCount()
meth public void allowCoreThreadTimeOut(boolean)
meth public void execute(java.lang.Runnable)
meth public void purge()
meth public void setCorePoolSize(int)
meth public void setKeepAliveTime(long,java.util.concurrent.TimeUnit)
meth public void setMaximumPoolSize(int)
meth public void setRejectedExecutionHandler(java.util.concurrent.RejectedExecutionHandler)
meth public void setThreadFactory(java.util.concurrent.ThreadFactory)
meth public void shutdown()
supr java.util.concurrent.AbstractExecutorService

CLSS public abstract interface java.util.function.BiPredicate<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract boolean test({java.util.function.BiPredicate%0},{java.util.function.BiPredicate%1})
meth public java.util.function.BiPredicate<{java.util.function.BiPredicate%0},{java.util.function.BiPredicate%1}> and(java.util.function.BiPredicate<? super {java.util.function.BiPredicate%0},? super {java.util.function.BiPredicate%1}>)
meth public java.util.function.BiPredicate<{java.util.function.BiPredicate%0},{java.util.function.BiPredicate%1}> negate()
meth public java.util.function.BiPredicate<{java.util.function.BiPredicate%0},{java.util.function.BiPredicate%1}> or(java.util.function.BiPredicate<? super {java.util.function.BiPredicate%0},? super {java.util.function.BiPredicate%1}>)

CLSS public abstract interface java.util.function.Supplier<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract {java.util.function.Supplier%0} get()

CLSS public javax.crypto.spec.GCMParameterSpec
cons public init(int,byte[])
cons public init(int,byte[],int,int)
intf java.security.spec.AlgorithmParameterSpec
meth public byte[] getIV()
meth public int getTLen()
supr java.lang.Object

CLSS public abstract interface javax.security.auth.Destroyable
meth public boolean isDestroyed()
meth public void destroy() throws javax.security.auth.DestroyFailedException

CLSS public abstract interface org.apache.sshd.agent.SshAgent
fld public final static java.lang.String SSH_AUTHSOCKET_ENV_NAME = "SSH_AUTH_SOCK"
intf java.nio.channels.Channel
meth public abstract !varargs void addIdentity(java.security.KeyPair,java.lang.String,org.apache.sshd.agent.SshAgentKeyConstraint[]) throws java.io.IOException
meth public abstract java.lang.Iterable<? extends java.util.Map$Entry<java.security.PublicKey,java.lang.String>> getIdentities() throws java.io.IOException
meth public abstract java.util.Map$Entry<java.lang.String,byte[]> sign(org.apache.sshd.common.session.SessionContext,java.security.PublicKey,java.lang.String,byte[]) throws java.io.IOException
meth public abstract void removeAllIdentities() throws java.io.IOException
meth public abstract void removeIdentity(java.security.PublicKey) throws java.io.IOException
meth public java.security.KeyPair resolveLocalIdentity(java.security.PublicKey)

CLSS public final org.apache.sshd.agent.SshAgentConstants
fld public final static byte SSH2_AGENTC_ADD_IDENTITY = 17
fld public final static byte SSH2_AGENTC_ADD_ID_CONSTRAINED = 25
fld public final static byte SSH2_AGENTC_REMOVE_ALL_IDENTITIES = 19
fld public final static byte SSH2_AGENTC_REMOVE_IDENTITY = 18
fld public final static byte SSH2_AGENTC_REQUEST_IDENTITIES = 11
fld public final static byte SSH2_AGENTC_SIGN_REQUEST = 13
fld public final static byte SSH2_AGENT_FAILURE = 30
fld public final static byte SSH2_AGENT_IDENTITIES_ANSWER = 12
fld public final static byte SSH2_AGENT_SIGN_RESPONSE = 14
fld public final static byte SSH_AGENTC_ADD_RSA_IDENTITY = 7
fld public final static byte SSH_AGENTC_ADD_RSA_ID_CONSTRAINED = 24
fld public final static byte SSH_AGENTC_ADD_SMARTCARD_KEY = 20
fld public final static byte SSH_AGENTC_ADD_SMARTCARD_KEY_CONSTRAINED = 26
fld public final static byte SSH_AGENTC_LOCK = 22
fld public final static byte SSH_AGENTC_REMOVE_ALL_RSA_IDENTITIES = 9
fld public final static byte SSH_AGENTC_REMOVE_RSA_IDENTITY = 8
fld public final static byte SSH_AGENTC_REMOVE_SMARTCARD_KEY = 21
fld public final static byte SSH_AGENTC_REQUEST_RSA_IDENTITIES = 1
fld public final static byte SSH_AGENTC_RSA_CHALLENGE = 3
fld public final static byte SSH_AGENTC_UNLOCK = 23
fld public final static byte SSH_AGENT_CONSTRAIN_CONFIRM = 2
fld public final static byte SSH_AGENT_CONSTRAIN_EXTENSION = -1
fld public final static byte SSH_AGENT_CONSTRAIN_LIFETIME = 1
fld public final static byte SSH_AGENT_FAILURE = 5
fld public final static byte SSH_AGENT_KEY_LIST = 104
fld public final static byte SSH_AGENT_OPERATION_COMPLETE = 105
fld public final static byte SSH_AGENT_RSA_IDENTITIES_ANSWER = 2
fld public final static byte SSH_AGENT_RSA_RESPONSE = 4
fld public final static byte SSH_AGENT_SUCCESS = 6
fld public final static int SSH_AGENT_LIST_KEYS = 204
fld public final static int SSH_AGENT_PRIVATE_KEY_OP = 205
meth public static java.lang.String getCommandMessageName(int)
supr java.lang.Object
hcls LazyMessagesMapHolder

CLSS public abstract interface org.apache.sshd.agent.SshAgentFactory
meth public abstract java.util.List<org.apache.sshd.common.channel.ChannelFactory> getChannelForwardingFactories(org.apache.sshd.common.FactoryManager)
meth public abstract org.apache.sshd.agent.SshAgent createClient(org.apache.sshd.common.session.Session,org.apache.sshd.common.FactoryManager) throws java.io.IOException
meth public abstract org.apache.sshd.agent.SshAgentServer createServer(org.apache.sshd.common.session.ConnectionService) throws java.io.IOException

CLSS public abstract org.apache.sshd.agent.SshAgentKeyConstraint
cons protected init(byte)
fld public final static org.apache.sshd.agent.SshAgentKeyConstraint CONFIRM
innr public abstract static Extension
innr public static FidoProviderExtension
innr public static LifeTime
meth public byte getId()
meth public void put(org.apache.sshd.common.util.buffer.Buffer)
supr java.lang.Object
hfds id

CLSS public abstract static org.apache.sshd.agent.SshAgentKeyConstraint$Extension
 outer org.apache.sshd.agent.SshAgentKeyConstraint
cons protected init(java.lang.String)
meth public void put(org.apache.sshd.common.util.buffer.Buffer)
supr org.apache.sshd.agent.SshAgentKeyConstraint
hfds name

CLSS public static org.apache.sshd.agent.SshAgentKeyConstraint$FidoProviderExtension
 outer org.apache.sshd.agent.SshAgentKeyConstraint
cons public init(java.lang.String)
meth public void put(org.apache.sshd.common.util.buffer.Buffer)
supr org.apache.sshd.agent.SshAgentKeyConstraint$Extension
hfds provider

CLSS public static org.apache.sshd.agent.SshAgentKeyConstraint$LifeTime
 outer org.apache.sshd.agent.SshAgentKeyConstraint
cons public init(int)
meth public void put(org.apache.sshd.common.util.buffer.Buffer)
supr org.apache.sshd.agent.SshAgentKeyConstraint
hfds secondsToLive

CLSS public abstract interface org.apache.sshd.agent.SshAgentServer
intf java.nio.channels.Channel
meth public abstract java.lang.String getId()

CLSS public abstract org.apache.sshd.agent.common.AbstractAgentClient
cons protected init(org.apache.sshd.agent.SshAgent)
meth protected abstract void reply(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.util.buffer.Buffer prepare(org.apache.sshd.common.util.buffer.Buffer)
meth protected void process(int,org.apache.sshd.common.util.buffer.Buffer,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void messageReceived(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds agent,buffer

CLSS public abstract org.apache.sshd.agent.common.AbstractAgentProxy
cons protected init(org.apache.sshd.common.util.threads.CloseableExecutorService)
intf org.apache.sshd.agent.SshAgent
intf org.apache.sshd.common.util.threads.ExecutorServiceCarrier
meth protected abstract org.apache.sshd.common.util.buffer.Buffer request(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.util.buffer.Buffer createBuffer(byte)
meth protected org.apache.sshd.common.util.buffer.Buffer createBuffer(byte,int)
meth protected org.apache.sshd.common.util.buffer.Buffer prepare(org.apache.sshd.common.util.buffer.Buffer)
meth public !varargs void addIdentity(java.security.KeyPair,java.lang.String,org.apache.sshd.agent.SshAgentKeyConstraint[]) throws java.io.IOException
meth public java.lang.Iterable<? extends java.util.Map$Entry<java.security.PublicKey,java.lang.String>> getIdentities() throws java.io.IOException
meth public java.lang.String getChannelType()
meth public java.util.Map$Entry<java.lang.String,byte[]> sign(org.apache.sshd.common.session.SessionContext,java.security.PublicKey,java.lang.String,byte[]) throws java.io.IOException
meth public org.apache.sshd.common.util.threads.CloseableExecutorService getExecutorService()
meth public void close() throws java.io.IOException
meth public void removeAllIdentities() throws java.io.IOException
meth public void removeIdentity(java.security.PublicKey) throws java.io.IOException
meth public void setChannelType(java.lang.String)
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds channelType,executor

CLSS public org.apache.sshd.agent.common.AgentDelegate
cons public init(org.apache.sshd.agent.SshAgent)
intf org.apache.sshd.agent.SshAgent
meth public !varargs void addIdentity(java.security.KeyPair,java.lang.String,org.apache.sshd.agent.SshAgentKeyConstraint[]) throws java.io.IOException
meth public boolean isOpen()
meth public java.lang.Iterable<? extends java.util.Map$Entry<java.security.PublicKey,java.lang.String>> getIdentities() throws java.io.IOException
meth public java.util.Map$Entry<java.lang.String,byte[]> sign(org.apache.sshd.common.session.SessionContext,java.security.PublicKey,java.lang.String,byte[]) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void removeAllIdentities() throws java.io.IOException
meth public void removeIdentity(java.security.PublicKey) throws java.io.IOException
supr java.lang.Object
hfds agent

CLSS public abstract interface org.apache.sshd.agent.common.AgentForwardSupport
intf org.apache.sshd.common.Closeable
meth public abstract java.lang.String initialize() throws java.io.IOException

CLSS public org.apache.sshd.agent.common.DefaultAgentForwardSupport
cons public init(org.apache.sshd.common.session.ConnectionService)
intf org.apache.sshd.agent.common.AgentForwardSupport
meth protected org.apache.sshd.agent.SshAgentServer createSshAgentServer(org.apache.sshd.common.session.ConnectionService,org.apache.sshd.common.session.Session) throws java.lang.Throwable
meth protected void doCloseImmediately()
meth public java.lang.String initialize() throws java.io.IOException
meth public java.lang.String toString()
meth public void close() throws java.io.IOException
supr org.apache.sshd.common.util.closeable.AbstractCloseable
hfds agentServerHolder,serviceInstance

CLSS public org.apache.sshd.agent.local.AgentForwardedChannel
cons public init(java.lang.String)
fld public final static java.lang.String MESSAGE_POLL_FREQUENCY = "agent-fwd-channel-message-poll-time"
fld public final static long DEFAULT_MESSAGE_POLL_FREQUENCY
meth protected org.apache.sshd.common.util.buffer.Buffer request(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.util.buffer.Buffer waitForMessageBuffer() throws java.io.IOException
meth protected void doOpen() throws java.io.IOException
meth protected void doWriteData(byte[],int,long) throws java.io.IOException
meth public org.apache.sshd.agent.SshAgent getAgent()
supr org.apache.sshd.client.channel.AbstractClientChannel
hfds messages,receiveBuffer

CLSS public org.apache.sshd.agent.local.AgentImpl
cons public init()
intf org.apache.sshd.agent.SshAgent
meth protected static java.util.Map$Entry<java.security.KeyPair,java.lang.String> getKeyPair(java.util.Collection<? extends java.util.Map$Entry<java.security.KeyPair,java.lang.String>>,java.security.PublicKey)
meth public !varargs void addIdentity(java.security.KeyPair,java.lang.String,org.apache.sshd.agent.SshAgentKeyConstraint[]) throws java.io.IOException
meth public boolean isOpen()
meth public java.lang.Iterable<? extends java.util.Map$Entry<java.security.PublicKey,java.lang.String>> getIdentities() throws java.io.IOException
meth public java.security.KeyPair resolveLocalIdentity(java.security.PublicKey)
meth public java.util.Map$Entry<java.lang.String,byte[]> sign(org.apache.sshd.common.session.SessionContext,java.security.PublicKey,java.lang.String,byte[]) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void removeAllIdentities() throws java.io.IOException
meth public void removeIdentity(java.security.PublicKey) throws java.io.IOException
supr java.lang.Object
hfds LOG,keys,open

CLSS public org.apache.sshd.agent.local.AgentServerProxy
cons public init(org.apache.sshd.common.session.ConnectionService) throws java.io.IOException
intf org.apache.sshd.agent.SshAgentServer
meth public boolean isOpen()
meth public java.lang.String getId()
meth public org.apache.sshd.agent.SshAgent createClient() throws java.io.IOException
meth public void close() throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds id,open,service

CLSS public org.apache.sshd.agent.local.ChannelAgentForwarding
cons public init(org.apache.sshd.common.util.threads.CloseableExecutorService)
innr protected AgentClient
meth protected org.apache.sshd.client.future.OpenFuture doInit(org.apache.sshd.common.util.buffer.Buffer)
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected void doWriteData(byte[],int,long) throws java.io.IOException
meth protected void doWriteExtendedData(byte[],int,long) throws java.io.IOException
meth public void handleEof() throws java.io.IOException
supr org.apache.sshd.server.channel.AbstractServerChannel
hfds agent,client,out

CLSS protected org.apache.sshd.agent.local.ChannelAgentForwarding$AgentClient
 outer org.apache.sshd.agent.local.ChannelAgentForwarding
cons public init(org.apache.sshd.agent.local.ChannelAgentForwarding)
meth protected void reply(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr org.apache.sshd.agent.common.AbstractAgentClient

CLSS public org.apache.sshd.agent.local.ChannelAgentForwardingFactory
cons public init(java.lang.String)
fld public final static org.apache.sshd.agent.local.ChannelAgentForwardingFactory IETF
fld public final static org.apache.sshd.agent.local.ChannelAgentForwardingFactory OPENSSH
intf org.apache.sshd.common.channel.ChannelFactory
meth public java.lang.String getName()
meth public org.apache.sshd.common.channel.Channel createChannel(org.apache.sshd.common.session.Session) throws java.io.IOException
supr java.lang.Object
hfds name

CLSS public org.apache.sshd.agent.local.LocalAgentFactory
cons public init()
cons public init(org.apache.sshd.agent.SshAgent)
fld public final static java.util.List<org.apache.sshd.common.channel.ChannelFactory> DEFAULT_FORWARDING_CHANNELS
intf org.apache.sshd.agent.SshAgentFactory
meth public java.util.List<org.apache.sshd.common.channel.ChannelFactory> getChannelForwardingFactories(org.apache.sshd.common.FactoryManager)
meth public org.apache.sshd.agent.SshAgent createClient(org.apache.sshd.common.session.Session,org.apache.sshd.common.FactoryManager) throws java.io.IOException
meth public org.apache.sshd.agent.SshAgent getAgent()
meth public org.apache.sshd.agent.SshAgentServer createServer(org.apache.sshd.common.session.ConnectionService) throws java.io.IOException
supr java.lang.Object
hfds agent

CLSS public org.apache.sshd.agent.local.ProxyAgentFactory
cons public init()
intf org.apache.sshd.agent.SshAgentFactory
meth public java.util.List<org.apache.sshd.common.channel.ChannelFactory> getChannelForwardingFactories(org.apache.sshd.common.FactoryManager)
meth public org.apache.sshd.agent.SshAgent createClient(org.apache.sshd.common.session.Session,org.apache.sshd.common.FactoryManager) throws java.io.IOException
meth public org.apache.sshd.agent.SshAgentServer createServer(org.apache.sshd.common.session.ConnectionService) throws java.io.IOException
meth public static boolean isPreferredUnixAgent(org.apache.sshd.common.PropertyResolver)
supr java.lang.Object
hfds proxies

CLSS public org.apache.sshd.agent.unix.AgentClient
cons public init(org.apache.sshd.common.FactoryManager,java.lang.String) throws java.io.IOException
cons public init(org.apache.sshd.common.FactoryManager,java.lang.String,org.apache.sshd.common.util.threads.CloseableExecutorService) throws java.io.IOException
fld public final static java.lang.String MESSAGE_POLL_FREQUENCY = "agent-client-message-poll-time"
fld public final static long DEFAULT_MESSAGE_POLL_FREQUENCY
intf java.lang.Runnable
intf org.apache.sshd.common.FactoryManagerHolder
meth protected org.apache.sshd.common.util.buffer.Buffer request(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.util.buffer.Buffer waitForMessageBuffer() throws java.io.IOException
meth protected void messageReceived(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void throwException(int) throws java.io.IOException
meth public boolean isOpen()
meth public java.lang.String getAuthSocket()
meth public java.lang.String toString()
meth public org.apache.sshd.common.FactoryManager getFactoryManager()
meth public void close() throws java.io.IOException
meth public void run()
supr org.apache.sshd.agent.common.AbstractAgentProxy
hfds authSocket,handle,manager,messages,open,pool,pumper,receiveBuffer

CLSS public org.apache.sshd.agent.unix.AgentForwardedChannel
cons public init(long,java.lang.String)
intf java.lang.Runnable
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected void doOpen() throws java.io.IOException
meth protected void doWriteData(byte[],int,long) throws java.io.IOException
meth public void run()
supr org.apache.sshd.client.channel.AbstractClientChannel
hfds socket

CLSS public org.apache.sshd.agent.unix.AgentServer
cons public init()
cons public init(org.apache.sshd.agent.SshAgent,org.apache.sshd.common.util.threads.CloseableExecutorService)
cons public init(org.apache.sshd.common.util.threads.CloseableExecutorService)
innr protected static SshAgentSession
intf java.io.Closeable
intf org.apache.sshd.common.util.threads.ExecutorServiceCarrier
meth public java.lang.String start() throws java.lang.Exception
meth public org.apache.sshd.agent.SshAgent getAgent()
meth public org.apache.sshd.common.util.threads.CloseableExecutorService getExecutorService()
meth public void close() throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds agent,agentThread,authSocket,handle,pool,service

CLSS protected static org.apache.sshd.agent.unix.AgentServer$SshAgentSession
 outer org.apache.sshd.agent.unix.AgentServer
cons public init(long,org.apache.sshd.agent.SshAgent)
intf java.lang.Runnable
meth protected void reply(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void run()
supr org.apache.sshd.agent.common.AbstractAgentClient
hfds socket

CLSS public org.apache.sshd.agent.unix.AgentServerProxy
cons public init(org.apache.sshd.common.session.ConnectionService) throws java.io.IOException
cons public init(org.apache.sshd.common.session.ConnectionService,org.apache.sshd.common.util.threads.CloseableExecutorService) throws java.io.IOException
intf org.apache.sshd.agent.SshAgentServer
meth protected boolean deleteFile(java.io.File,java.lang.String,boolean)
meth protected java.io.File removeSocketFile(java.lang.String,boolean) throws java.lang.Exception
meth protected void signalEOS(org.apache.sshd.agent.unix.AprLibrary,boolean) throws java.lang.Exception
meth public boolean isOpen()
meth public java.lang.String getId()
meth public org.apache.sshd.common.util.threads.CloseableExecutorService getExecutorService()
meth public static java.io.IOException toIOException(int)
meth public void close() throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds END_OF_STREAM_MESSAGE,authSocket,handle,innerFinished,open,pipeService,piper,pool,service

CLSS public final org.apache.sshd.agent.unix.AprLibrary
meth protected void finalize() throws java.lang.Throwable
meth public static boolean isInitialized()
meth public static org.apache.sshd.agent.unix.AprLibrary getInstance()
supr java.lang.Object
hfds library,pool

CLSS public org.apache.sshd.agent.unix.ChannelAgentForwarding
cons public init(org.apache.sshd.common.util.threads.CloseableExecutorService)
meth protected org.apache.sshd.client.future.OpenFuture doInit(org.apache.sshd.common.util.buffer.Buffer)
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected void doWriteData(byte[],int,long) throws java.io.IOException
meth protected void doWriteExtendedData(byte[],int,long) throws java.io.IOException
supr org.apache.sshd.server.channel.AbstractServerChannel
hfds authSocket,forwardService,forwarder,handle,out,pool

CLSS public org.apache.sshd.agent.unix.ChannelAgentForwardingFactory
cons public init(java.lang.String)
cons public init(java.lang.String,org.apache.sshd.common.Factory<org.apache.sshd.common.util.threads.CloseableExecutorService>)
fld public final static org.apache.sshd.agent.unix.ChannelAgentForwardingFactory IETF
fld public final static org.apache.sshd.agent.unix.ChannelAgentForwardingFactory OPENSSH
intf org.apache.sshd.common.channel.ChannelFactory
meth public java.lang.String getName()
meth public org.apache.sshd.common.channel.Channel createChannel(org.apache.sshd.common.session.Session) throws java.io.IOException
supr java.lang.Object
hfds executorServiceFactory,name

CLSS public org.apache.sshd.agent.unix.UnixAgentFactory
cons public init()
cons public init(org.apache.sshd.common.Factory<org.apache.sshd.common.util.threads.CloseableExecutorService>)
fld public final static java.util.List<org.apache.sshd.common.channel.ChannelFactory> DEFAULT_FORWARDING_CHANNELS
intf org.apache.sshd.agent.SshAgentFactory
meth protected org.apache.sshd.common.util.threads.CloseableExecutorService newExecutor()
meth public java.util.List<org.apache.sshd.common.channel.ChannelFactory> getChannelForwardingFactories(org.apache.sshd.common.FactoryManager)
meth public org.apache.sshd.agent.SshAgent createClient(org.apache.sshd.common.session.Session,org.apache.sshd.common.FactoryManager) throws java.io.IOException
meth public org.apache.sshd.agent.SshAgentServer createServer(org.apache.sshd.common.session.ConnectionService) throws java.io.IOException
supr java.lang.Object
hfds executorServiceFactory

CLSS public org.apache.sshd.certificate.OpenSshCertificateBuilder
cons protected init(org.apache.sshd.common.config.keys.OpenSshCertificate$Type)
fld protected byte[] nonce
fld protected final java.util.Map<java.lang.String,java.lang.String> criticalOptions
fld protected final java.util.Map<java.lang.String,java.lang.String> extensions
fld protected final org.apache.sshd.common.config.keys.OpenSshCertificate$Type type
fld protected final static java.util.Map<java.lang.String,java.lang.String> SIGNATURE_ALGORITHM_MAP
fld protected java.lang.String id
fld protected java.security.PublicKey publicKey
fld protected java.util.Collection<java.lang.String> principals
fld protected long serial
fld protected long validAfter
fld protected long validBefore
meth protected void validate()
meth public org.apache.sshd.certificate.OpenSshCertificateBuilder criticalOption(java.lang.String,java.lang.String)
meth public org.apache.sshd.certificate.OpenSshCertificateBuilder criticalOptions(java.util.List<org.apache.sshd.common.config.keys.OpenSshCertificate$CertificateOption>)
meth public org.apache.sshd.certificate.OpenSshCertificateBuilder criticalOptions(java.util.Map<java.lang.String,java.lang.String>)
meth public org.apache.sshd.certificate.OpenSshCertificateBuilder extension(java.lang.String,java.lang.String)
meth public org.apache.sshd.certificate.OpenSshCertificateBuilder extensions(java.util.List<org.apache.sshd.common.config.keys.OpenSshCertificate$CertificateOption>)
meth public org.apache.sshd.certificate.OpenSshCertificateBuilder extensions(java.util.Map<java.lang.String,java.lang.String>)
meth public org.apache.sshd.certificate.OpenSshCertificateBuilder id(java.lang.String)
meth public org.apache.sshd.certificate.OpenSshCertificateBuilder nonce(byte[])
meth public org.apache.sshd.certificate.OpenSshCertificateBuilder principals(java.util.Collection<java.lang.String>)
meth public org.apache.sshd.certificate.OpenSshCertificateBuilder publicKey(java.security.PublicKey)
meth public org.apache.sshd.certificate.OpenSshCertificateBuilder serial(long)
meth public org.apache.sshd.certificate.OpenSshCertificateBuilder validAfter(java.time.Instant)
meth public org.apache.sshd.certificate.OpenSshCertificateBuilder validAfter(long)
meth public org.apache.sshd.certificate.OpenSshCertificateBuilder validBefore(java.time.Instant)
meth public org.apache.sshd.certificate.OpenSshCertificateBuilder validBefore(long)
meth public org.apache.sshd.common.config.keys.OpenSshCertificate sign(java.security.KeyPair) throws java.lang.Exception
meth public org.apache.sshd.common.config.keys.OpenSshCertificate sign(java.security.KeyPair,java.lang.String) throws java.lang.Exception
meth public static org.apache.sshd.certificate.OpenSshCertificateBuilder hostCertificate()
meth public static org.apache.sshd.certificate.OpenSshCertificateBuilder userCertificate()
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.client.ClientAuthenticationManager
intf org.apache.sshd.common.auth.UserAuthFactoriesManager<org.apache.sshd.client.session.ClientSession,org.apache.sshd.client.auth.UserAuth,org.apache.sshd.client.auth.UserAuthFactory>
intf org.apache.sshd.common.keyprovider.KeyIdentityProviderHolder
meth public abstract java.lang.String removePasswordIdentity(java.lang.String)
meth public abstract java.security.KeyPair removePublicKeyIdentity(java.security.KeyPair)
meth public abstract org.apache.sshd.client.auth.AuthenticationIdentitiesProvider getRegisteredIdentities()
meth public abstract org.apache.sshd.client.auth.hostbased.HostBasedAuthenticationReporter getHostBasedAuthenticationReporter()
meth public abstract org.apache.sshd.client.auth.keyboard.UserInteraction getUserInteraction()
meth public abstract org.apache.sshd.client.auth.password.PasswordAuthenticationReporter getPasswordAuthenticationReporter()
meth public abstract org.apache.sshd.client.auth.password.PasswordIdentityProvider getPasswordIdentityProvider()
meth public abstract org.apache.sshd.client.auth.pubkey.PublicKeyAuthenticationReporter getPublicKeyAuthenticationReporter()
meth public abstract org.apache.sshd.client.keyverifier.ServerKeyVerifier getServerKeyVerifier()
meth public abstract void addPasswordIdentity(java.lang.String)
meth public abstract void addPublicKeyIdentity(java.security.KeyPair)
meth public abstract void setHostBasedAuthenticationReporter(org.apache.sshd.client.auth.hostbased.HostBasedAuthenticationReporter)
meth public abstract void setPasswordAuthenticationReporter(org.apache.sshd.client.auth.password.PasswordAuthenticationReporter)
meth public abstract void setPasswordIdentityProvider(org.apache.sshd.client.auth.password.PasswordIdentityProvider)
meth public abstract void setPublicKeyAuthenticationReporter(org.apache.sshd.client.auth.pubkey.PublicKeyAuthenticationReporter)
meth public abstract void setServerKeyVerifier(org.apache.sshd.client.keyverifier.ServerKeyVerifier)
meth public abstract void setUserInteraction(org.apache.sshd.client.auth.keyboard.UserInteraction)
meth public void setUserAuthFactoriesNames(java.util.Collection<java.lang.String>)

CLSS public org.apache.sshd.client.ClientBuilder
cons public init()
fld protected org.apache.sshd.client.config.hosts.HostConfigEntryResolver hostConfigEntryResolver
fld protected org.apache.sshd.client.config.keys.ClientIdentityLoader clientIdentityLoader
fld protected org.apache.sshd.client.keyverifier.ServerKeyVerifier serverKeyVerifier
fld protected org.apache.sshd.common.config.keys.FilePasswordProvider filePasswordProvider
fld public final static java.util.List<org.apache.sshd.common.channel.ChannelFactory> DEFAULT_CHANNEL_FACTORIES
fld public final static java.util.List<org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.session.ConnectionService>> DEFAULT_GLOBAL_REQUEST_HANDLERS
fld public final static java.util.List<org.apache.sshd.common.compression.CompressionFactory> DEFAULT_COMPRESSION_FACTORIES
fld public final static java.util.function.Function<org.apache.sshd.common.kex.DHFactory,org.apache.sshd.common.kex.KeyExchangeFactory> DH2KEX
fld public final static org.apache.sshd.client.config.hosts.HostConfigEntryResolver DEFAULT_HOST_CONFIG_ENTRY_RESOLVER
fld public final static org.apache.sshd.client.config.keys.ClientIdentityLoader DEFAULT_CLIENT_IDENTITY_LOADER
fld public final static org.apache.sshd.client.keyverifier.ServerKeyVerifier DEFAULT_SERVER_KEY_VERIFIER
fld public final static org.apache.sshd.common.config.keys.FilePasswordProvider DEFAULT_FILE_PASSWORD_PROVIDER
fld public final static org.apache.sshd.common.kex.extension.KexExtensionHandler DEFAULT_KEX_EXTENSION_HANDLER
meth protected org.apache.sshd.client.ClientBuilder fillWithDefaultValues()
meth public org.apache.sshd.client.ClientBuilder clientIdentityLoader(org.apache.sshd.client.config.keys.ClientIdentityLoader)
meth public org.apache.sshd.client.ClientBuilder filePasswordProvider(org.apache.sshd.common.config.keys.FilePasswordProvider)
meth public org.apache.sshd.client.ClientBuilder hostConfigEntryResolver(org.apache.sshd.client.config.hosts.HostConfigEntryResolver)
meth public org.apache.sshd.client.ClientBuilder serverKeyVerifier(org.apache.sshd.client.keyverifier.ServerKeyVerifier)
meth public org.apache.sshd.client.SshClient build(boolean)
meth public static java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.compression.Compression>> setUpDefaultCompressionFactories(boolean)
meth public static java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> setUpDefaultSignatureFactories(boolean)
meth public static java.util.List<org.apache.sshd.common.kex.KeyExchangeFactory> setUpDefaultKeyExchanges(boolean)
meth public static org.apache.sshd.client.ClientBuilder builder()
supr org.apache.sshd.common.BaseBuilder<org.apache.sshd.client.SshClient,org.apache.sshd.client.ClientBuilder>

CLSS public abstract interface org.apache.sshd.client.ClientFactoryManager
intf org.apache.sshd.client.ClientAuthenticationManager
intf org.apache.sshd.client.config.keys.ClientIdentityLoaderManager
intf org.apache.sshd.client.session.ClientProxyConnectorHolder
intf org.apache.sshd.client.session.ClientSessionCreator
intf org.apache.sshd.common.FactoryManager
intf org.apache.sshd.common.config.keys.FilePasswordProviderManager
meth public abstract org.apache.sshd.client.config.hosts.HostConfigEntryResolver getHostConfigEntryResolver()
meth public abstract void setHostConfigEntryResolver(org.apache.sshd.client.config.hosts.HostConfigEntryResolver)

CLSS public org.apache.sshd.client.SshClient
cons public init()
fld protected java.util.List<org.apache.sshd.client.auth.UserAuthFactory> userAuthFactories
fld protected org.apache.sshd.client.session.SessionFactory sessionFactory
fld protected org.apache.sshd.common.io.IoConnector connector
fld public final static java.util.List<org.apache.sshd.client.auth.UserAuthFactory> DEFAULT_USER_AUTH_FACTORIES
fld public final static java.util.List<org.apache.sshd.common.ServiceFactory> DEFAULT_SERVICE_FACTORIES
fld public final static org.apache.sshd.common.Factory<org.apache.sshd.client.SshClient> DEFAULT_SSH_CLIENT_FACTORY
intf org.apache.sshd.client.ClientFactoryManager
meth protected java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> parseProxyJumps(java.lang.String,org.apache.sshd.common.AttributeRepository) throws java.io.IOException
meth protected java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> parseProxyJumps(org.apache.sshd.client.config.hosts.HostConfigEntry,org.apache.sshd.common.AttributeRepository) throws java.io.IOException
meth protected org.apache.sshd.client.config.hosts.HostConfigEntry resolveHost(java.lang.String,java.lang.String,int,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth protected org.apache.sshd.client.future.ConnectFuture doConnect(java.lang.String,java.net.SocketAddress,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress,org.apache.sshd.common.keyprovider.KeyIdentityProvider,org.apache.sshd.client.config.hosts.HostConfigEntry) throws java.io.IOException
meth protected org.apache.sshd.client.future.ConnectFuture doConnect(org.apache.sshd.client.config.hosts.HostConfigEntry,java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry>,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth protected org.apache.sshd.client.session.SessionFactory createSessionFactory()
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.io.IoConnectFuture> createConnectCompletionListener(org.apache.sshd.client.future.ConnectFuture,java.lang.String,java.net.SocketAddress,org.apache.sshd.common.keyprovider.KeyIdentityProvider,org.apache.sshd.client.config.hosts.HostConfigEntry)
meth protected org.apache.sshd.common.io.IoConnector createConnector()
meth protected org.apache.sshd.common.keyprovider.KeyIdentityProvider ensureFilePasswordProvider(org.apache.sshd.common.keyprovider.KeyIdentityProvider)
meth protected org.apache.sshd.common.keyprovider.KeyIdentityProvider preloadClientIdentities(java.util.Collection<? extends org.apache.sshd.common.NamedResource>) throws java.io.IOException
meth protected void checkConfig()
meth protected void onConnectOperationComplete(org.apache.sshd.common.io.IoSession,org.apache.sshd.client.future.ConnectFuture,java.lang.String,java.net.SocketAddress,org.apache.sshd.common.keyprovider.KeyIdentityProvider,org.apache.sshd.client.config.hosts.HostConfigEntry) throws java.io.IOException,java.security.GeneralSecurityException
meth protected void setupDefaultSessionIdentities(org.apache.sshd.client.session.ClientSession,org.apache.sshd.common.keyprovider.KeyIdentityProvider) throws java.io.IOException,java.security.GeneralSecurityException
meth public !varargs static <%0 extends org.apache.sshd.client.SshClient> {%%0} setKeyPairProvider({%%0},boolean,boolean,org.apache.sshd.common.config.keys.FilePasswordProvider,java.nio.file.LinkOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public !varargs static <%0 extends org.apache.sshd.client.SshClient> {%%0} setKeyPairProvider({%%0},java.nio.file.Path,boolean,boolean,org.apache.sshd.common.config.keys.FilePasswordProvider,java.nio.file.LinkOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public boolean isStarted()
meth public java.lang.String removePasswordIdentity(java.lang.String)
meth public java.lang.String toString()
meth public java.security.KeyPair removePublicKeyIdentity(java.security.KeyPair)
meth public java.util.List<org.apache.sshd.client.auth.UserAuthFactory> getUserAuthFactories()
meth public org.apache.sshd.client.auth.AuthenticationIdentitiesProvider getRegisteredIdentities()
meth public org.apache.sshd.client.auth.hostbased.HostBasedAuthenticationReporter getHostBasedAuthenticationReporter()
meth public org.apache.sshd.client.auth.keyboard.UserInteraction getUserInteraction()
meth public org.apache.sshd.client.auth.password.PasswordAuthenticationReporter getPasswordAuthenticationReporter()
meth public org.apache.sshd.client.auth.password.PasswordIdentityProvider getPasswordIdentityProvider()
meth public org.apache.sshd.client.auth.pubkey.PublicKeyAuthenticationReporter getPublicKeyAuthenticationReporter()
meth public org.apache.sshd.client.config.hosts.HostConfigEntryResolver getHostConfigEntryResolver()
meth public org.apache.sshd.client.config.keys.ClientIdentityLoader getClientIdentityLoader()
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.lang.String,int,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.net.SocketAddress,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(org.apache.sshd.client.config.hosts.HostConfigEntry,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.keyverifier.ServerKeyVerifier getServerKeyVerifier()
meth public org.apache.sshd.client.session.ClientProxyConnector getClientProxyConnector()
meth public org.apache.sshd.client.session.SessionFactory getSessionFactory()
meth public org.apache.sshd.common.config.keys.FilePasswordProvider getFilePasswordProvider()
meth public org.apache.sshd.common.keyprovider.KeyIdentityProvider getKeyIdentityProvider()
meth public static org.apache.sshd.client.SshClient setUpDefaultClient()
meth public static org.apache.sshd.client.simple.SimpleClient setUpDefaultSimpleClient()
meth public static org.apache.sshd.client.simple.SimpleClient wrapAsSimpleClient(org.apache.sshd.client.SshClient)
meth public void addPasswordIdentity(java.lang.String)
meth public void addPublicKeyIdentity(java.security.KeyPair)
meth public void open() throws java.io.IOException
meth public void setClientIdentityLoader(org.apache.sshd.client.config.keys.ClientIdentityLoader)
meth public void setClientProxyConnector(org.apache.sshd.client.session.ClientProxyConnector)
meth public void setFilePasswordProvider(org.apache.sshd.common.config.keys.FilePasswordProvider)
meth public void setHostBasedAuthenticationReporter(org.apache.sshd.client.auth.hostbased.HostBasedAuthenticationReporter)
meth public void setHostConfigEntryResolver(org.apache.sshd.client.config.hosts.HostConfigEntryResolver)
meth public void setKeyIdentityProvider(org.apache.sshd.common.keyprovider.KeyIdentityProvider)
meth public void setPasswordAuthenticationReporter(org.apache.sshd.client.auth.password.PasswordAuthenticationReporter)
meth public void setPasswordIdentityProvider(org.apache.sshd.client.auth.password.PasswordIdentityProvider)
meth public void setPublicKeyAuthenticationReporter(org.apache.sshd.client.auth.pubkey.PublicKeyAuthenticationReporter)
meth public void setServerKeyVerifier(org.apache.sshd.client.keyverifier.ServerKeyVerifier)
meth public void setSessionFactory(org.apache.sshd.client.session.SessionFactory)
meth public void setUserAuthFactories(java.util.List<org.apache.sshd.client.auth.UserAuthFactory>)
meth public void setUserInteraction(org.apache.sshd.client.auth.keyboard.UserInteraction)
meth public void start()
meth public void stop()
supr org.apache.sshd.common.helpers.AbstractFactoryManager
hfds clientIdentityLoader,filePasswordProvider,hostBasedAuthenticationReporter,hostConfigEntryResolver,identities,identitiesProvider,keyIdentityProvider,passwordAuthenticationReporter,passwordIdentityProvider,proxyConnector,publicKeyAuthenticationReporter,serverKeyVerifier,started,userInteraction

CLSS public abstract org.apache.sshd.client.auth.AbstractUserAuth
cons protected init(java.lang.String)
intf org.apache.sshd.client.auth.UserAuth
meth protected abstract boolean processAuthDataRequest(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected abstract boolean sendAuthDataRequest(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth protected void setCancellable(boolean)
meth public boolean isCancellable()
meth public boolean process(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public final java.lang.String getName()
meth public java.lang.String getService()
meth public java.lang.String toString()
meth public org.apache.sshd.client.session.ClientSession getClientSession()
meth public org.apache.sshd.client.session.ClientSession getSession()
meth public void destroy()
meth public void init(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds cancellable,clientSession,name,service

CLSS public abstract org.apache.sshd.client.auth.AbstractUserAuthFactory
cons protected init(java.lang.String)
intf org.apache.sshd.client.auth.UserAuthFactory
supr org.apache.sshd.common.auth.AbstractUserAuthMethodFactory<org.apache.sshd.client.session.ClientSession,org.apache.sshd.client.auth.UserAuth>

CLSS public abstract interface org.apache.sshd.client.auth.AuthenticationIdentitiesProvider
fld public final static java.util.Comparator<java.lang.Object> KEYPAIR_IDENTITY_COMPARATOR
fld public final static java.util.Comparator<java.lang.Object> PASSWORD_IDENTITY_COMPARATOR
intf org.apache.sshd.client.auth.password.PasswordIdentityProvider
intf org.apache.sshd.common.keyprovider.KeyIdentityProvider
meth public abstract java.lang.Iterable<?> loadIdentities(org.apache.sshd.common.session.SessionContext) throws java.io.IOException,java.security.GeneralSecurityException
meth public static int findIdentityIndex(java.util.List<?>,java.util.Comparator<? super java.lang.Object>,java.lang.Object)
meth public static org.apache.sshd.client.auth.AuthenticationIdentitiesProvider wrapIdentities(java.lang.Iterable<?>)

CLSS public final !enum org.apache.sshd.client.auth.BuiltinUserAuthFactories
fld public final static java.util.Set<org.apache.sshd.client.auth.BuiltinUserAuthFactories> VALUES
fld public final static org.apache.sshd.client.auth.BuiltinUserAuthFactories HOSTBASED
fld public final static org.apache.sshd.client.auth.BuiltinUserAuthFactories KBINTERACTIVE
fld public final static org.apache.sshd.client.auth.BuiltinUserAuthFactories PASSWORD
fld public final static org.apache.sshd.client.auth.BuiltinUserAuthFactories PUBLICKEY
innr public static ParseResult
intf org.apache.sshd.common.NamedFactory<org.apache.sshd.client.auth.UserAuthFactory>
meth public !varargs static org.apache.sshd.client.auth.BuiltinUserAuthFactories$ParseResult parseFactoriesList(java.lang.String[])
meth public java.lang.String getName()
meth public org.apache.sshd.client.auth.UserAuthFactory create()
meth public static org.apache.sshd.client.auth.BuiltinUserAuthFactories valueOf(java.lang.String)
meth public static org.apache.sshd.client.auth.BuiltinUserAuthFactories$ParseResult parseFactoriesList(java.lang.String)
meth public static org.apache.sshd.client.auth.BuiltinUserAuthFactories$ParseResult parseFactoriesList(java.util.Collection<java.lang.String>)
meth public static org.apache.sshd.client.auth.BuiltinUserAuthFactories[] values()
meth public static org.apache.sshd.client.auth.UserAuthFactory fromFactoryName(java.lang.String)
meth public static org.apache.sshd.client.auth.UserAuthFactory resolveFactory(java.lang.String)
supr java.lang.Enum<org.apache.sshd.client.auth.BuiltinUserAuthFactories>
hfds factory

CLSS public static org.apache.sshd.client.auth.BuiltinUserAuthFactories$ParseResult
 outer org.apache.sshd.client.auth.BuiltinUserAuthFactories
cons public init(java.util.List<org.apache.sshd.client.auth.UserAuthFactory>,java.util.List<java.lang.String>)
fld public final static org.apache.sshd.client.auth.BuiltinUserAuthFactories$ParseResult EMPTY
supr org.apache.sshd.common.config.NamedFactoriesListParseResult<org.apache.sshd.client.auth.UserAuth,org.apache.sshd.client.auth.UserAuthFactory>

CLSS public abstract interface org.apache.sshd.client.auth.UserAuth
intf org.apache.sshd.client.session.ClientSessionHolder
intf org.apache.sshd.common.auth.UserAuthInstance<org.apache.sshd.client.session.ClientSession>
meth public abstract boolean process(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public abstract void destroy()
meth public abstract void init(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth public boolean isCancellable()
meth public void signalAuthMethodFailure(org.apache.sshd.client.session.ClientSession,java.lang.String,boolean,java.util.List<java.lang.String>,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void signalAuthMethodSuccess(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception

CLSS public abstract interface org.apache.sshd.client.auth.UserAuthFactory
intf org.apache.sshd.common.auth.UserAuthMethodFactory<org.apache.sshd.client.session.ClientSession,org.apache.sshd.client.auth.UserAuth>

CLSS public abstract interface org.apache.sshd.client.auth.hostbased.HostBasedAuthenticationReporter
meth public void signalAuthenticationAttempt(org.apache.sshd.client.session.ClientSession,java.lang.String,java.security.KeyPair,java.lang.String,java.lang.String,byte[]) throws java.lang.Exception
meth public void signalAuthenticationExhausted(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String,java.lang.String) throws java.lang.Exception
meth public void signalAuthenticationFailure(org.apache.sshd.client.session.ClientSession,java.lang.String,java.security.KeyPair,java.lang.String,java.lang.String,boolean,java.util.List<java.lang.String>) throws java.lang.Exception
meth public void signalAuthenticationSuccess(org.apache.sshd.client.session.ClientSession,java.lang.String,java.security.KeyPair,java.lang.String,java.lang.String) throws java.lang.Exception

CLSS public abstract interface org.apache.sshd.client.auth.hostbased.HostKeyIdentityProvider
 anno 0 java.lang.FunctionalInterface()
meth public !varargs static org.apache.sshd.client.auth.hostbased.HostKeyIdentityProvider wrap(java.security.KeyPair[])
meth public abstract java.lang.Iterable<? extends java.util.Map$Entry<java.security.KeyPair,java.util.List<java.security.cert.X509Certificate>>> loadHostKeys(org.apache.sshd.common.session.SessionContext) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.util.Iterator<? extends java.util.Map$Entry<java.security.KeyPair,java.util.List<java.security.cert.X509Certificate>>> iteratorOf(org.apache.sshd.common.session.SessionContext,org.apache.sshd.client.auth.hostbased.HostKeyIdentityProvider) throws java.io.IOException,java.security.GeneralSecurityException
meth public static org.apache.sshd.client.auth.hostbased.HostKeyIdentityProvider wrap(java.lang.Iterable<java.security.KeyPair>)

CLSS public org.apache.sshd.client.auth.hostbased.UserAuthHostBased
cons public init(org.apache.sshd.client.auth.hostbased.HostKeyIdentityProvider)
fld protected final org.apache.sshd.client.auth.hostbased.HostKeyIdentityProvider clientHostKeys
fld protected java.util.Iterator<? extends java.util.Map$Entry<java.security.KeyPair,? extends java.util.Collection<java.security.cert.X509Certificate>>> keys
fld protected java.util.Map$Entry<java.security.KeyPair,? extends java.util.Collection<java.security.cert.X509Certificate>> keyInfo
fld public final static java.lang.String NAME = "hostbased"
intf org.apache.sshd.common.signature.SignatureFactoriesManager
meth protected boolean processAuthDataRequest(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected boolean sendAuthDataRequest(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth protected byte[] appendSignature(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String,java.security.PublicKey,byte[],java.lang.String,java.lang.String,org.apache.sshd.common.signature.Signature,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected java.lang.String resolveClientHostname(org.apache.sshd.client.session.ClientSession)
meth protected java.lang.String resolveClientUsername(org.apache.sshd.client.session.ClientSession)
meth public java.lang.String getClientHostname()
meth public java.lang.String getClientUsername()
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories()
meth public void init(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth public void setClientHostname(java.lang.String)
meth public void setClientUsername(java.lang.String)
meth public void setSignatureFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
meth public void signalAuthMethodFailure(org.apache.sshd.client.session.ClientSession,java.lang.String,boolean,java.util.List<java.lang.String>,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void signalAuthMethodSuccess(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
supr org.apache.sshd.client.auth.AbstractUserAuth
hfds clientHostname,clientUsername,factories

CLSS public org.apache.sshd.client.auth.hostbased.UserAuthHostBasedFactory
cons public init()
fld public final static java.lang.String NAME = "hostbased"
fld public final static org.apache.sshd.client.auth.hostbased.UserAuthHostBasedFactory INSTANCE
intf org.apache.sshd.common.signature.SignatureFactoriesManager
meth public java.lang.String getClientHostname()
meth public java.lang.String getClientUsername()
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories()
meth public org.apache.sshd.client.auth.hostbased.HostKeyIdentityProvider getClientHostKeys()
meth public org.apache.sshd.client.auth.hostbased.UserAuthHostBased createUserAuth(org.apache.sshd.client.session.ClientSession) throws java.io.IOException
meth public void setClientHostKeys(org.apache.sshd.client.auth.hostbased.HostKeyIdentityProvider)
meth public void setClientHostname(java.lang.String)
meth public void setClientUsername(java.lang.String)
meth public void setSignatureFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
supr org.apache.sshd.client.auth.AbstractUserAuthFactory
hfds clientHostKeys,clientHostname,clientUsername,factories

CLSS public org.apache.sshd.client.auth.keyboard.UserAuthKeyboardInteractive
cons public init()
fld public final static java.lang.String NAME = "keyboard-interactive"
meth protected boolean processAuthDataRequest(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected boolean sendAuthDataRequest(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth protected boolean useCurrentPassword(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String[],boolean[])
meth protected boolean verifyTrialsCount(org.apache.sshd.client.session.ClientSession,java.lang.String,int,int,int)
meth protected java.lang.String getCurrentPasswordCandidate()
meth protected java.lang.String getExchangeLanguageTag(org.apache.sshd.client.session.ClientSession)
meth protected java.lang.String getExchangeSubMethods(org.apache.sshd.client.session.ClientSession)
meth protected java.lang.String[] getUserResponses(java.lang.String,java.lang.String,java.lang.String,java.lang.String[],boolean[])
meth public static java.lang.String getAuthCommandName(int)
meth public void init(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
supr org.apache.sshd.client.auth.AbstractUserAuth
hfds maxAttempts,nOfAttempts,passwords,requestPending,wasChallenged,withUserInteraction

CLSS public org.apache.sshd.client.auth.keyboard.UserAuthKeyboardInteractiveFactory
cons public init()
fld public final static java.lang.String NAME = "keyboard-interactive"
fld public final static org.apache.sshd.client.auth.keyboard.UserAuthKeyboardInteractiveFactory INSTANCE
meth public org.apache.sshd.client.auth.keyboard.UserAuthKeyboardInteractive createUserAuth(org.apache.sshd.client.session.ClientSession) throws java.io.IOException
supr org.apache.sshd.client.auth.AbstractUserAuthFactory

CLSS public abstract interface org.apache.sshd.client.auth.keyboard.UserInteraction
fld public final static boolean DEFAULT_AUTO_DETECT_PASSWORD_PROMPT = true
fld public final static java.lang.String AUTO_DETECT_PASSWORD_PROMPT = "user-interaction-auto-detect-password-prompt"
fld public final static java.lang.String CHECK_INTERACTIVE_PASSWORD_DELIM = "user-interaction-check-password-delimiter"
fld public final static java.lang.String DEFAULT_CHECK_INTERACTIVE_PASSWORD_DELIM = ":"
fld public final static java.lang.String DEFAULT_INTERACTIVE_PASSWORD_PROMPT = "password"
fld public final static java.lang.String INTERACTIVE_PASSWORD_PROMPT = "user-interaction-password-prompt"
fld public final static org.apache.sshd.client.auth.keyboard.UserInteraction NONE
meth public abstract java.lang.String getUpdatedPassword(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String)
meth public abstract java.lang.String[] interactive(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String,java.lang.String,java.lang.String[],boolean[])
meth public boolean isInteractionAllowed(org.apache.sshd.client.session.ClientSession)
meth public java.lang.String resolveAuthPasswordAttempt(org.apache.sshd.client.session.ClientSession) throws java.lang.Exception
meth public java.security.KeyPair resolveAuthPublicKeyIdentityAttempt(org.apache.sshd.client.session.ClientSession) throws java.lang.Exception
meth public static int findPromptComponentLastPosition(java.lang.String,java.lang.String)
meth public void serverVersionInfo(org.apache.sshd.client.session.ClientSession,java.util.List<java.lang.String>)
meth public void welcome(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String)

CLSS public abstract interface org.apache.sshd.client.auth.password.PasswordAuthenticationReporter
meth public void signalAuthenticationAttempt(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String,boolean,java.lang.String) throws java.lang.Exception
meth public void signalAuthenticationExhausted(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth public void signalAuthenticationFailure(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String,boolean,java.util.List<java.lang.String>) throws java.lang.Exception
meth public void signalAuthenticationSuccess(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String) throws java.lang.Exception

CLSS public abstract interface org.apache.sshd.client.auth.password.PasswordIdentityProvider
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.client.auth.password.PasswordIdentityProvider EMPTY_PASSWORDS_PROVIDER
meth public !varargs static org.apache.sshd.client.auth.password.PasswordIdentityProvider multiProvider(org.apache.sshd.common.session.SessionContext,org.apache.sshd.client.auth.password.PasswordIdentityProvider[])
meth public !varargs static org.apache.sshd.client.auth.password.PasswordIdentityProvider wrapPasswords(java.lang.String[])
meth public abstract java.lang.Iterable<java.lang.String> loadPasswords(org.apache.sshd.common.session.SessionContext) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.lang.Iterable<java.lang.String> iterableOf(org.apache.sshd.common.session.SessionContext,java.util.Collection<? extends org.apache.sshd.client.auth.password.PasswordIdentityProvider>)
meth public static java.util.Iterator<java.lang.String> iteratorOf(org.apache.sshd.common.session.SessionContext,org.apache.sshd.client.auth.password.PasswordIdentityProvider) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.util.Iterator<java.lang.String> iteratorOf(org.apache.sshd.common.session.SessionContext,org.apache.sshd.client.auth.password.PasswordIdentityProvider,org.apache.sshd.client.auth.password.PasswordIdentityProvider) throws java.io.IOException,java.security.GeneralSecurityException
meth public static org.apache.sshd.client.auth.password.PasswordIdentityProvider multiProvider(org.apache.sshd.common.session.SessionContext,java.util.Collection<? extends org.apache.sshd.client.auth.password.PasswordIdentityProvider>)
meth public static org.apache.sshd.client.auth.password.PasswordIdentityProvider resolvePasswordIdentityProvider(org.apache.sshd.common.session.SessionContext,org.apache.sshd.client.auth.password.PasswordIdentityProvider,org.apache.sshd.client.auth.password.PasswordIdentityProvider)
meth public static org.apache.sshd.client.auth.password.PasswordIdentityProvider wrapPasswords(java.lang.Iterable<java.lang.String>)

CLSS public org.apache.sshd.client.auth.password.UserAuthPassword
cons public init()
fld public final static java.lang.String NAME = "password"
meth protected boolean processAuthDataRequest(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected boolean sendAuthDataRequest(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth protected java.lang.String resolveAttemptedPassword(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth protected org.apache.sshd.common.io.IoWriteFuture sendPassword(org.apache.sshd.common.util.buffer.Buffer,org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String) throws java.lang.Exception
meth public void init(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth public void signalAuthMethodFailure(org.apache.sshd.client.session.ClientSession,java.lang.String,boolean,java.util.List<java.lang.String>,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void signalAuthMethodSuccess(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
supr org.apache.sshd.client.auth.AbstractUserAuth
hfds current,maxAttempts,nOfAttempts,passwords

CLSS public org.apache.sshd.client.auth.password.UserAuthPasswordFactory
cons public init()
fld public final static java.lang.String NAME = "password"
fld public final static org.apache.sshd.client.auth.password.UserAuthPasswordFactory INSTANCE
meth public org.apache.sshd.client.auth.password.UserAuthPassword createUserAuth(org.apache.sshd.client.session.ClientSession) throws java.io.IOException
supr org.apache.sshd.client.auth.AbstractUserAuthFactory

CLSS public abstract org.apache.sshd.client.auth.pubkey.AbstractKeyPairIterator<%0 extends org.apache.sshd.client.auth.pubkey.PublicKeyIdentity>
cons protected init(org.apache.sshd.client.session.ClientSession)
intf java.util.Iterator<{org.apache.sshd.client.auth.pubkey.AbstractKeyPairIterator%0}>
intf org.apache.sshd.client.session.ClientSessionHolder
intf org.apache.sshd.common.session.SessionHolder<org.apache.sshd.client.session.ClientSession>
meth public final org.apache.sshd.client.session.ClientSession getClientSession()
meth public final org.apache.sshd.client.session.ClientSession getSession()
meth public java.lang.String toString()
meth public void remove()
supr java.lang.Object
hfds session

CLSS public org.apache.sshd.client.auth.pubkey.KeyAgentIdentity
cons public init(org.apache.sshd.agent.SshAgent,java.security.PublicKey,java.lang.String)
intf org.apache.sshd.client.auth.pubkey.PublicKeyIdentity
meth public java.lang.String getComment()
meth public java.lang.String toString()
meth public java.security.KeyPair getKeyIdentity()
meth public java.util.Map$Entry<java.lang.String,byte[]> sign(org.apache.sshd.common.session.SessionContext,java.lang.String,byte[]) throws java.lang.Exception
supr java.lang.Object
hfds agent,comment,keyPair,resolvedPair

CLSS public org.apache.sshd.client.auth.pubkey.KeyPairIdentity
cons public init(org.apache.sshd.common.signature.SignatureFactoriesManager,org.apache.sshd.common.signature.SignatureFactoriesManager,java.security.KeyPair)
intf org.apache.sshd.client.auth.pubkey.PublicKeyIdentity
intf org.apache.sshd.common.signature.SignatureFactoriesHolder
meth public java.lang.String toString()
meth public java.security.KeyPair getKeyIdentity()
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories()
meth public java.util.Map$Entry<java.lang.String,byte[]> sign(org.apache.sshd.common.session.SessionContext,java.lang.String,byte[]) throws java.lang.Exception
supr java.lang.Object
hfds pair,signatureFactories

CLSS public abstract interface org.apache.sshd.client.auth.pubkey.PublicKeyAuthenticationReporter
meth public void signalAuthenticationAttempt(org.apache.sshd.client.session.ClientSession,java.lang.String,java.security.KeyPair,java.lang.String) throws java.lang.Exception
meth public void signalAuthenticationExhausted(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth public void signalAuthenticationFailure(org.apache.sshd.client.session.ClientSession,java.lang.String,java.security.KeyPair,boolean,java.util.List<java.lang.String>) throws java.lang.Exception
meth public void signalAuthenticationSuccess(org.apache.sshd.client.session.ClientSession,java.lang.String,java.security.KeyPair) throws java.lang.Exception
meth public void signalIdentitySkipped(org.apache.sshd.client.session.ClientSession,java.lang.String,java.security.KeyPair) throws java.lang.Exception
meth public void signalSignatureAttempt(org.apache.sshd.client.session.ClientSession,java.lang.String,java.security.KeyPair,java.lang.String,byte[]) throws java.lang.Exception

CLSS public abstract interface org.apache.sshd.client.auth.pubkey.PublicKeyIdentity
meth public abstract java.security.KeyPair getKeyIdentity()
meth public abstract java.util.Map$Entry<java.lang.String,byte[]> sign(org.apache.sshd.common.session.SessionContext,java.lang.String,byte[]) throws java.lang.Exception

CLSS public org.apache.sshd.client.auth.pubkey.SessionKeyPairIterator
cons public init(org.apache.sshd.client.session.ClientSession,org.apache.sshd.common.signature.SignatureFactoriesManager,java.util.Iterator<java.security.KeyPair>)
meth public boolean hasNext()
meth public org.apache.sshd.client.auth.pubkey.KeyPairIdentity next()
supr org.apache.sshd.client.auth.pubkey.AbstractKeyPairIterator<org.apache.sshd.client.auth.pubkey.KeyPairIdentity>
hfds keys,signatureFactories

CLSS public org.apache.sshd.client.auth.pubkey.SshAgentPublicKeyIterator
cons public init(org.apache.sshd.client.session.ClientSession,org.apache.sshd.agent.SshAgent) throws java.io.IOException
meth public boolean hasNext()
meth public org.apache.sshd.client.auth.pubkey.KeyAgentIdentity next()
supr org.apache.sshd.client.auth.pubkey.AbstractKeyPairIterator<org.apache.sshd.client.auth.pubkey.KeyAgentIdentity>
hfds agent,keys

CLSS public org.apache.sshd.client.auth.pubkey.UserAuthPublicKey
cons public init()
cons public init(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
fld protected final java.util.Deque<java.lang.String> currentAlgorithms
fld protected java.lang.String chosenAlgorithm
fld protected java.util.Iterator<org.apache.sshd.client.auth.pubkey.PublicKeyIdentity> keys
fld protected java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> factories
fld protected org.apache.sshd.client.auth.pubkey.PublicKeyIdentity current
fld public final static java.lang.String NAME = "publickey"
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<java.lang.Boolean> USE_DEFAULT_IDENTITIES
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<java.lang.String> IDENTITY_AGENT
intf org.apache.sshd.common.signature.SignatureFactoriesManager
meth protected boolean processAuthDataRequest(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected boolean sendAuthDataRequest(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth protected byte[] appendSignature(org.apache.sshd.client.session.ClientSession,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.security.PublicKey,java.security.PublicKey,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected java.lang.String getDefaultSignatureAlgorithm(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.client.auth.pubkey.PublicKeyIdentity,java.security.KeyPair,java.lang.String) throws java.lang.Exception
meth protected java.util.Iterator<org.apache.sshd.client.auth.pubkey.PublicKeyIdentity> createPublicKeyIterator(org.apache.sshd.client.session.ClientSession,org.apache.sshd.common.signature.SignatureFactoriesManager) throws java.lang.Exception
meth protected org.apache.sshd.client.auth.pubkey.PublicKeyIdentity resolveAttemptedPublicKeyIdentity(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth protected org.apache.sshd.client.auth.pubkey.PublicKeyIdentity resolveAttemptedPublicKeyIdentity(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.client.auth.pubkey.PublicKeyAuthenticationReporter) throws java.lang.Exception
meth protected void releaseKeys() throws java.io.IOException
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories()
meth public void destroy()
meth public void init(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.lang.Exception
meth public void setSignatureFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
meth public void signalAuthMethodFailure(org.apache.sshd.client.session.ClientSession,java.lang.String,boolean,java.util.List<java.lang.String>,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void signalAuthMethodSuccess(org.apache.sshd.client.session.ClientSession,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
supr org.apache.sshd.client.auth.AbstractUserAuth

CLSS public org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyFactory
cons public init()
cons public init(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
fld public final static java.lang.String NAME = "publickey"
fld public final static org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyFactory INSTANCE
intf org.apache.sshd.common.signature.SignatureFactoriesManager
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories()
meth public org.apache.sshd.client.auth.pubkey.UserAuthPublicKey createUserAuth(org.apache.sshd.client.session.ClientSession) throws java.io.IOException
meth public void setSignatureFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
supr org.apache.sshd.client.auth.AbstractUserAuthFactory
hfds factories

CLSS public org.apache.sshd.client.auth.pubkey.UserAuthPublicKeyIterator
cons public init(org.apache.sshd.client.session.ClientSession,org.apache.sshd.common.signature.SignatureFactoriesManager) throws java.lang.Exception
intf java.nio.channels.Channel
meth protected java.lang.Iterable<org.apache.sshd.client.auth.pubkey.KeyAgentIdentity> initializeAgentIdentities(org.apache.sshd.client.session.ClientSession) throws java.io.IOException
meth protected java.lang.Iterable<org.apache.sshd.client.auth.pubkey.KeyPairIdentity> initializeSessionIdentities(org.apache.sshd.client.session.ClientSession,org.apache.sshd.common.signature.SignatureFactoriesManager)
meth protected void closeAgent() throws java.io.IOException
meth public boolean hasNext()
meth public boolean isOpen()
meth public org.apache.sshd.client.auth.pubkey.PublicKeyIdentity next()
meth public void close() throws java.io.IOException
supr org.apache.sshd.client.auth.pubkey.AbstractKeyPairIterator<org.apache.sshd.client.auth.pubkey.PublicKeyIdentity>
hfds agent,current,open

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

CLSS public org.apache.sshd.client.channel.ChannelDirectTcpip
cons public init(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress)
meth public org.apache.sshd.client.future.OpenFuture open() throws java.io.IOException
meth public org.apache.sshd.common.util.net.SshdSocketAddress getLocalSocketAddress()
meth public org.apache.sshd.common.util.net.SshdSocketAddress getRemoteSocketAddress()
supr org.apache.sshd.client.channel.AsyncCapableClientChannel
hfds local,remote

CLSS public org.apache.sshd.client.channel.ChannelExec
cons public init(byte[],org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>)
cons public init(java.lang.String,java.nio.charset.Charset,org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>)
cons public init(java.lang.String,org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>)
meth protected void doOpen() throws java.io.IOException
meth public void handleFailure() throws java.io.IOException
meth public void handleSuccess() throws java.io.IOException
supr org.apache.sshd.client.channel.PtyCapableChannelSession
hfds cmdBytes,command

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

CLSS public org.apache.sshd.client.channel.ChannelShell
cons public init(org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>)
meth protected void doOpen() throws java.io.IOException
meth public void handleFailure() throws java.io.IOException
meth public void handleSuccess() throws java.io.IOException
supr org.apache.sshd.client.channel.PtyCapableChannelSession

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

CLSS public final !enum org.apache.sshd.client.channel.ClientChannelEvent
fld public final static java.util.Set<org.apache.sshd.client.channel.ClientChannelEvent> VALUES
fld public final static org.apache.sshd.client.channel.ClientChannelEvent CLOSED
fld public final static org.apache.sshd.client.channel.ClientChannelEvent EOF
fld public final static org.apache.sshd.client.channel.ClientChannelEvent EXIT_SIGNAL
fld public final static org.apache.sshd.client.channel.ClientChannelEvent EXIT_STATUS
fld public final static org.apache.sshd.client.channel.ClientChannelEvent OPENED
fld public final static org.apache.sshd.client.channel.ClientChannelEvent STDERR_DATA
fld public final static org.apache.sshd.client.channel.ClientChannelEvent STDOUT_DATA
fld public final static org.apache.sshd.client.channel.ClientChannelEvent TIMEOUT
meth public static org.apache.sshd.client.channel.ClientChannelEvent valueOf(java.lang.String)
meth public static org.apache.sshd.client.channel.ClientChannelEvent[] values()
supr java.lang.Enum<org.apache.sshd.client.channel.ClientChannelEvent>

CLSS public abstract interface org.apache.sshd.client.channel.ClientChannelHolder
 anno 0 java.lang.FunctionalInterface()
intf org.apache.sshd.common.channel.ChannelHolder
meth public abstract org.apache.sshd.client.channel.ClientChannel getClientChannel()
meth public org.apache.sshd.common.channel.Channel getChannel()

CLSS public org.apache.sshd.client.channel.PtyCapableChannelSession
cons public init(boolean,org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>)
intf org.apache.sshd.common.channel.PtyChannelConfigurationMutator
meth protected java.lang.String resolvePtyType(org.apache.sshd.common.channel.PtyChannelConfigurationHolder)
meth protected void doOpenPty() throws java.io.IOException
meth public boolean isAgentForwarding()
meth public boolean isUsePty()
meth public int getPtyColumns()
meth public int getPtyHeight()
meth public int getPtyLines()
meth public int getPtyWidth()
meth public java.lang.String getPtyType()
meth public java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer> getPtyModes()
meth public void sendWindowChange(int,int) throws java.io.IOException
meth public void sendWindowChange(int,int,int,int) throws java.io.IOException
meth public void setAgentForwarding(boolean)
meth public void setPtyColumns(int)
meth public void setPtyHeight(int)
meth public void setPtyLines(int)
meth public void setPtyModes(java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer>)
meth public void setPtyType(java.lang.String)
meth public void setPtyWidth(int)
meth public void setUsePty(boolean)
meth public void setupSensibleDefaultPty()
supr org.apache.sshd.client.channel.ChannelSession
hfds agentForwarding,config,usePty

CLSS public abstract org.apache.sshd.client.channel.exit.AbstractChannelExitRequestHandler<%0 extends java.lang.Object>
cons protected init(java.util.concurrent.atomic.AtomicReference<{org.apache.sshd.client.channel.exit.AbstractChannelExitRequestHandler%0}>,org.apache.sshd.common.util.EventNotifier<? super java.lang.String>)
fld protected final java.util.concurrent.atomic.AtomicReference<{org.apache.sshd.client.channel.exit.AbstractChannelExitRequestHandler%0}> holder
fld protected final org.apache.sshd.common.util.EventNotifier<? super java.lang.String> notifier
intf org.apache.sshd.common.NamedResource
meth protected abstract {org.apache.sshd.client.channel.exit.AbstractChannelExitRequestHandler%0} processRequestValue(org.apache.sshd.common.channel.Channel,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected java.lang.String getEvent(org.apache.sshd.common.channel.Channel,java.lang.String,{org.apache.sshd.client.channel.exit.AbstractChannelExitRequestHandler%0})
meth protected void notifyStateChanged(org.apache.sshd.common.channel.Channel,java.lang.String,{org.apache.sshd.client.channel.exit.AbstractChannelExitRequestHandler%0})
meth public org.apache.sshd.common.channel.RequestHandler$Result process(org.apache.sshd.common.channel.Channel,java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
supr org.apache.sshd.common.channel.AbstractChannelRequestHandler

CLSS public org.apache.sshd.client.channel.exit.ExitSignalChannelRequestHandler
cons public init(java.util.concurrent.atomic.AtomicReference<java.lang.String>,org.apache.sshd.common.util.EventNotifier<? super java.lang.String>)
fld public final static java.lang.String NAME = "exit-signal"
meth protected java.lang.String processRequestValue(org.apache.sshd.common.channel.Channel,java.lang.String,boolean,java.lang.String,java.lang.String) throws java.lang.Exception
meth protected java.lang.String processRequestValue(org.apache.sshd.common.channel.Channel,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public final java.lang.String getName()
supr org.apache.sshd.client.channel.exit.AbstractChannelExitRequestHandler<java.lang.String>

CLSS public org.apache.sshd.client.channel.exit.ExitStatusChannelRequestHandler
cons public init(java.util.concurrent.atomic.AtomicReference<java.lang.Integer>,org.apache.sshd.common.util.EventNotifier<? super java.lang.String>)
fld public final static java.lang.String NAME = "exit-status"
meth protected java.lang.Integer processRequestValue(org.apache.sshd.common.channel.Channel,int) throws java.lang.Exception
meth protected java.lang.Integer processRequestValue(org.apache.sshd.common.channel.Channel,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public final java.lang.String getName()
supr org.apache.sshd.client.channel.exit.AbstractChannelExitRequestHandler<java.lang.Integer>

CLSS public final org.apache.sshd.client.config.SshClientConfigFileReader
fld public final static java.lang.String REQUEST_TTY_OPTION = "RequestTTY"
fld public final static java.lang.String SENDENV_PROP = "SendEnv"
fld public final static java.lang.String SETENV_PROP = "SetEnv"
fld public final static long DEFAULT_LIVECHECK_REPLY_WAIT = 0
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> CLIENT_LIVECHECK_USE_NULLS
fld public final static org.apache.sshd.common.Property<java.time.Duration> CLIENT_LIVECHECK_INTERVAL_PROP
fld public final static org.apache.sshd.common.Property<java.time.Duration> CLIENT_LIVECHECK_REPLIES_WAIT
meth public static <%0 extends org.apache.sshd.client.SshClient> {%%0} configure({%%0},org.apache.sshd.common.PropertyResolver,boolean,boolean)
meth public static <%0 extends org.apache.sshd.client.SshClient> {%%0} setupClientHeartbeat({%%0},java.util.Map<java.lang.String,?>)
meth public static <%0 extends org.apache.sshd.client.SshClient> {%%0} setupClientHeartbeat({%%0},org.apache.sshd.common.PropertyResolver)
supr java.lang.Object

CLSS public org.apache.sshd.client.config.hosts.ConfigFileHostEntryResolver
cons public !varargs init(java.nio.file.Path,java.nio.file.LinkOption[])
cons public init(java.nio.file.Path)
intf org.apache.sshd.client.config.hosts.HostConfigEntryResolver
meth protected java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> reloadHostConfigEntries(java.nio.file.Path,java.lang.String,int,java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.apache.sshd.client.config.hosts.HostConfigEntryResolver resolveEffectiveResolver(java.lang.String,int,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.config.hosts.HostConfigEntry resolveEffectiveHost(java.lang.String,int,java.net.SocketAddress,java.lang.String,java.lang.String,org.apache.sshd.common.AttributeRepository) throws java.io.IOException
supr org.apache.sshd.common.util.io.ModifiableFileWatcher
hfds delegateHolder

CLSS public org.apache.sshd.client.config.hosts.DefaultConfigFileHostEntryResolver
cons public !varargs init(java.nio.file.Path,boolean,java.nio.file.LinkOption[])
cons public init(boolean)
fld public final static org.apache.sshd.client.config.hosts.DefaultConfigFileHostEntryResolver INSTANCE
meth protected java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> reloadHostConfigEntries(java.nio.file.Path,java.lang.String,int,java.lang.String,java.lang.String) throws java.io.IOException
meth public final boolean isStrict()
supr org.apache.sshd.client.config.hosts.ConfigFileHostEntryResolver
hfds strict

CLSS public org.apache.sshd.client.config.hosts.HostConfigEntry
cons public init()
cons public init(java.lang.String,java.lang.String,int,java.lang.String)
cons public init(java.lang.String,java.lang.String,int,java.lang.String,java.lang.String)
fld protected final java.util.Collection<java.lang.String> identities
fld protected final java.util.Map<java.lang.String,java.lang.String> properties
fld protected int port
fld protected java.lang.Boolean exclusiveIdentites
fld protected java.lang.String host
fld protected java.lang.String hostName
fld protected java.lang.String proxyJump
fld protected java.lang.String username
fld public final static boolean DEFAULT_EXCLUSIVE_IDENTITIES = false
fld public final static char LOCAL_HOME_MACRO = 'd'
fld public final static char LOCAL_HOST_MACRO = 'l'
fld public final static char LOCAL_USER_MACRO = 'u'
fld public final static char PATH_MACRO_CHAR = '%'
fld public final static char REMOTE_HOST_MACRO = 'h'
fld public final static char REMOTE_PORT_MACRO = 'p'
fld public final static char REMOTE_USER_MACRO = 'r'
fld public final static java.lang.String CERTIFICATE_FILE_CONFIG_PROP = "CertificateFile"
fld public final static java.lang.String EXCLUSIVE_IDENTITIES_CONFIG_PROP = "IdentitiesOnly"
fld public final static java.lang.String HOST_CONFIG_PROP = "Host"
fld public final static java.lang.String HOST_NAME_CONFIG_PROP = "HostName"
fld public final static java.lang.String IDENTITY_AGENT = "IdentityAgent"
fld public final static java.lang.String IDENTITY_FILE_CONFIG_PROP = "IdentityFile"
fld public final static java.lang.String MATCH_CONFIG_PROP = "Match"
fld public final static java.lang.String MULTI_VALUE_SEPARATORS = " ,"
fld public final static java.lang.String PORT_CONFIG_PROP = "Port"
fld public final static java.lang.String PROXY_JUMP_CONFIG_PROP = "ProxyJump"
fld public final static java.lang.String STD_CONFIG_FILENAME = "config"
fld public final static java.lang.String USER_CONFIG_PROP = "User"
fld public final static java.util.NavigableSet<java.lang.String> EXPLICIT_PROPERTIES
intf org.apache.sshd.common.auth.MutableUserHolder
meth public !varargs static <%0 extends java.lang.Appendable> {%%0} appendNonEmptyValues({%%0},java.lang.String,java.lang.Object[]) throws java.io.IOException
meth public !varargs static java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> findMatchingEntries(java.lang.String,org.apache.sshd.client.config.hosts.HostConfigEntry[])
meth public !varargs static java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> readHostConfigEntries(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs static void writeHostConfigEntries(java.nio.file.Path,java.util.Collection<? extends org.apache.sshd.client.config.hosts.HostConfigEntry>,java.nio.file.OpenOption[]) throws java.io.IOException
meth public <%0 extends java.lang.Appendable> {%%0} append({%%0}) throws java.io.IOException
meth public boolean isIdentitiesOnly()
meth public int getPort()
meth public java.lang.String appendPropertyValue(java.lang.String,java.lang.String)
meth public java.lang.String getHost()
meth public java.lang.String getHostName()
meth public java.lang.String getProperty(java.lang.String)
meth public java.lang.String getProperty(java.lang.String,java.lang.String)
meth public java.lang.String getProxyJump()
meth public java.lang.String getUsername()
meth public java.lang.String removeProperty(java.lang.String)
meth public java.lang.String setProperty(java.lang.String,java.lang.String)
meth public java.lang.String toString()
meth public java.util.Collection<java.lang.String> getIdentities()
meth public java.util.Map<java.lang.String,java.lang.String> getProperties()
meth public static <%0 extends java.lang.Appendable> {%%0} appendHostConfigEntries({%%0},java.util.Collection<? extends org.apache.sshd.client.config.hosts.HostConfigEntry>) throws java.io.IOException
meth public static <%0 extends java.lang.Appendable> {%%0} appendNonEmptyPort({%%0},java.lang.String,int) throws java.io.IOException
meth public static <%0 extends java.lang.Appendable> {%%0} appendNonEmptyProperties({%%0},java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public static <%0 extends java.lang.Appendable> {%%0} appendNonEmptyProperty({%%0},java.lang.String,java.lang.Object) throws java.io.IOException
meth public static <%0 extends java.lang.Appendable> {%%0} appendNonEmptyValues({%%0},java.lang.String,java.util.Collection<?>) throws java.io.IOException
meth public static java.lang.String resolveIdentityFilePath(java.lang.String,java.lang.String,int,java.lang.String) throws java.io.IOException
meth public static java.nio.file.Path getDefaultHostConfigFile()
meth public static java.util.List<java.lang.String> parseConfigValue(java.lang.String)
meth public static java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> findMatchingEntries(java.lang.String,java.util.Collection<? extends org.apache.sshd.client.config.hosts.HostConfigEntry>)
meth public static java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> readHostConfigEntries(java.io.BufferedReader) throws java.io.IOException
meth public static java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> readHostConfigEntries(java.io.InputStream,boolean) throws java.io.IOException
meth public static java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> readHostConfigEntries(java.io.Reader,boolean) throws java.io.IOException
meth public static java.util.List<org.apache.sshd.client.config.hosts.HostConfigEntry> readHostConfigEntries(java.net.URL) throws java.io.IOException
meth public static org.apache.sshd.client.config.hosts.HostConfigEntryResolver toHostConfigEntryResolver(java.util.Collection<? extends org.apache.sshd.client.config.hosts.HostConfigEntry>)
meth public static void writeHostConfigEntries(java.io.OutputStream,boolean,java.util.Collection<? extends org.apache.sshd.client.config.hosts.HostConfigEntry>) throws java.io.IOException
meth public void addIdentity(java.lang.String)
meth public void addIdentity(java.nio.file.Path)
meth public void collate(org.apache.sshd.client.config.hosts.HostConfigEntry)
meth public void processProperty(java.lang.String,java.util.Collection<java.lang.String>)
meth public void setHost(java.lang.String)
meth public void setHost(java.util.Collection<java.lang.String>)
meth public void setHostName(java.lang.String)
meth public void setIdentities(java.util.Collection<java.lang.String>)
meth public void setIdentitiesOnly(boolean)
meth public void setPort(int)
meth public void setProperties(java.util.Map<java.lang.String,java.lang.String>)
meth public void setProxyJump(java.lang.String)
meth public void setUsername(java.lang.String)
supr org.apache.sshd.client.config.hosts.HostPatternsHolder
hcls LazyDefaultConfigFileHolder

CLSS public abstract interface org.apache.sshd.client.config.hosts.HostConfigEntryResolver
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.client.config.hosts.HostConfigEntryResolver EMPTY
meth public abstract org.apache.sshd.client.config.hosts.HostConfigEntry resolveEffectiveHost(java.lang.String,int,java.net.SocketAddress,java.lang.String,java.lang.String,org.apache.sshd.common.AttributeRepository) throws java.io.IOException

CLSS public org.apache.sshd.client.config.hosts.HostPatternValue
cons public init()
cons public init(java.util.regex.Pattern,boolean)
cons public init(java.util.regex.Pattern,int,boolean)
meth public boolean isNegated()
meth public int getPort()
meth public java.lang.String toString()
meth public java.util.regex.Pattern getPattern()
meth public void setNegated(boolean)
meth public void setPattern(java.util.regex.Pattern)
meth public void setPort(int)
supr java.lang.Object
hfds negated,pattern,port

CLSS public abstract org.apache.sshd.client.config.hosts.HostPatternsHolder
cons protected init()
fld public final static char NEGATION_CHAR_PATTERN = '!'
fld public final static char NON_STANDARD_PORT_PATTERN_ENCLOSURE_END_DELIM = ']'
fld public final static char NON_STANDARD_PORT_PATTERN_ENCLOSURE_START_DELIM = '['
fld public final static char PORT_VALUE_DELIMITER = ':'
fld public final static char SINGLE_CHAR_PATTERN = '?'
fld public final static char WILDCARD_PATTERN = '*'
fld public final static java.lang.String ALL_HOSTS_PATTERN
fld public final static java.lang.String PATTERN_CHARS
meth public !varargs static java.util.List<org.apache.sshd.client.config.hosts.HostPatternValue> parsePatterns(java.lang.CharSequence[])
meth public boolean isHostMatch(java.lang.String,int)
meth public java.util.Collection<org.apache.sshd.client.config.hosts.HostPatternValue> getPatterns()
meth public static boolean isHostMatch(java.lang.String,int,java.util.Collection<org.apache.sshd.client.config.hosts.HostPatternValue>)
meth public static boolean isHostMatch(java.lang.String,java.util.regex.Pattern)
meth public static boolean isPortMatch(int,int)
meth public static boolean isSpecificHostPattern(java.lang.String)
meth public static boolean isValidPatternChar(char)
meth public static java.util.List<org.apache.sshd.client.config.hosts.HostPatternValue> parsePatterns(java.util.Collection<? extends java.lang.CharSequence>)
meth public static org.apache.sshd.client.config.hosts.HostPatternValue toPattern(java.lang.CharSequence)
meth public void setPatterns(java.util.Collection<org.apache.sshd.client.config.hosts.HostPatternValue>)
supr java.lang.Object
hfds patterns

CLSS public final !enum org.apache.sshd.client.config.hosts.KnownHostDigest
fld public final static java.util.Set<org.apache.sshd.client.config.hosts.KnownHostDigest> VALUES
fld public final static org.apache.sshd.client.config.hosts.KnownHostDigest SHA1
intf org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac>
meth public java.lang.String getName()
meth public org.apache.sshd.common.mac.Mac create()
meth public static org.apache.sshd.client.config.hosts.KnownHostDigest fromName(java.lang.String)
meth public static org.apache.sshd.client.config.hosts.KnownHostDigest valueOf(java.lang.String)
meth public static org.apache.sshd.client.config.hosts.KnownHostDigest[] values()
supr java.lang.Enum<org.apache.sshd.client.config.hosts.KnownHostDigest>
hfds factory,name

CLSS public org.apache.sshd.client.config.hosts.KnownHostEntry
cons public init()
cons public init(java.lang.String)
fld public final static char MARKER_INDICATOR = '@'
fld public final static java.lang.String STD_HOSTS_FILENAME = "known_hosts"
meth public !varargs static java.util.List<org.apache.sshd.client.config.hosts.KnownHostEntry> readKnownHostEntries(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
meth public boolean isHostMatch(java.lang.String,int)
meth public java.lang.String getConfigLine()
meth public java.lang.String getMarker()
meth public java.lang.String toString()
meth public org.apache.sshd.client.config.hosts.KnownHostHashValue getHashedEntry()
meth public org.apache.sshd.common.config.keys.AuthorizedKeyEntry getKeyEntry()
meth public static <%0 extends org.apache.sshd.client.config.hosts.KnownHostEntry> {%%0} parseKnownHostEntry({%%0},java.lang.String)
meth public static java.nio.file.Path getDefaultKnownHostsFile()
meth public static java.util.List<org.apache.sshd.client.config.hosts.KnownHostEntry> readKnownHostEntries(java.io.BufferedReader) throws java.io.IOException
meth public static java.util.List<org.apache.sshd.client.config.hosts.KnownHostEntry> readKnownHostEntries(java.io.InputStream,boolean) throws java.io.IOException
meth public static java.util.List<org.apache.sshd.client.config.hosts.KnownHostEntry> readKnownHostEntries(java.io.Reader,boolean) throws java.io.IOException
meth public static java.util.List<org.apache.sshd.client.config.hosts.KnownHostEntry> readKnownHostEntries(java.net.URL) throws java.io.IOException
meth public static org.apache.sshd.client.config.hosts.KnownHostEntry parseKnownHostEntry(java.lang.String)
meth public void setConfigLine(java.lang.String)
meth public void setHashedEntry(org.apache.sshd.client.config.hosts.KnownHostHashValue)
meth public void setKeyEntry(org.apache.sshd.common.config.keys.AuthorizedKeyEntry)
meth public void setMarker(java.lang.String)
supr org.apache.sshd.client.config.hosts.HostPatternsHolder
hfds LOG,hashedEntry,keyEntry,line,marker
hcls LazyDefaultConfigFileHolder

CLSS public org.apache.sshd.client.config.hosts.KnownHostHashValue
cons public init()
fld public final static char HASHED_HOST_DELIMITER = '|'
fld public final static org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac> DEFAULT_DIGEST
meth public boolean isHostMatch(java.lang.String,int)
meth public byte[] getDigestValue()
meth public byte[] getSaltValue()
meth public java.lang.String toString()
meth public org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac> getDigester()
meth public static <%0 extends java.lang.Appendable> {%%0} append({%%0},org.apache.sshd.client.config.hosts.KnownHostHashValue) throws java.io.IOException
meth public static <%0 extends java.lang.Appendable> {%%0} append({%%0},org.apache.sshd.common.NamedResource,byte[],byte[]) throws java.io.IOException
meth public static <%0 extends java.lang.Appendable> {%%0} appendHostPattern({%%0},java.lang.String,int) throws java.io.IOException
meth public static <%0 extends org.apache.sshd.client.config.hosts.KnownHostHashValue> {%%0} parse(java.lang.String,{%%0})
meth public static byte[] calculateHashValue(java.lang.String,int,org.apache.sshd.common.Factory<? extends org.apache.sshd.common.mac.Mac>,byte[]) throws java.lang.Exception
meth public static byte[] calculateHashValue(java.lang.String,int,org.apache.sshd.common.mac.Mac,byte[]) throws java.lang.Exception
meth public static java.lang.String createHostPattern(java.lang.String,int)
meth public static org.apache.sshd.client.config.hosts.KnownHostHashValue parse(java.lang.String)
meth public void setDigestValue(byte[])
meth public void setDigester(org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac>)
meth public void setSaltValue(byte[])
supr java.lang.Object
hfds digestValue,digester,saltValue

CLSS public org.apache.sshd.client.config.keys.BuiltinClientIdentitiesWatcher
cons public init(java.nio.file.Path,boolean,org.apache.sshd.client.config.keys.ClientIdentityLoader,org.apache.sshd.common.config.keys.FilePasswordProvider,boolean)
cons public init(java.nio.file.Path,boolean,org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder,org.apache.sshd.common.config.keys.FilePasswordProviderHolder,boolean)
cons public init(java.nio.file.Path,java.util.Collection<java.lang.String>,boolean,org.apache.sshd.client.config.keys.ClientIdentityLoader,org.apache.sshd.common.config.keys.FilePasswordProvider,boolean)
cons public init(java.nio.file.Path,java.util.Collection<java.lang.String>,boolean,org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder,org.apache.sshd.common.config.keys.FilePasswordProviderHolder,boolean)
meth protected boolean isSupported(org.apache.sshd.common.session.SessionContext,java.security.KeyPair)
meth public final boolean isSupportedOnly()
meth public java.lang.Iterable<java.security.KeyPair> loadKeys(org.apache.sshd.common.session.SessionContext)
meth public static java.util.List<java.nio.file.Path> getBuiltinIdentitiesPaths(java.nio.file.Path,java.util.Collection<java.lang.String>)
meth public static java.util.List<java.nio.file.Path> getDefaultBuiltinIdentitiesPaths(java.nio.file.Path)
supr org.apache.sshd.client.config.keys.ClientIdentitiesWatcher
hfds supportedOnly

CLSS public org.apache.sshd.client.config.keys.ClientIdentitiesWatcher
cons public init(java.util.Collection<? extends java.nio.file.Path>,org.apache.sshd.client.config.keys.ClientIdentityLoader,org.apache.sshd.common.config.keys.FilePasswordProvider)
cons public init(java.util.Collection<? extends java.nio.file.Path>,org.apache.sshd.client.config.keys.ClientIdentityLoader,org.apache.sshd.common.config.keys.FilePasswordProvider,boolean)
cons public init(java.util.Collection<? extends java.nio.file.Path>,org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder,org.apache.sshd.common.config.keys.FilePasswordProviderHolder)
cons public init(java.util.Collection<? extends java.nio.file.Path>,org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder,org.apache.sshd.common.config.keys.FilePasswordProviderHolder,boolean)
cons public init(java.util.Collection<org.apache.sshd.client.config.keys.ClientIdentityProvider>)
meth protected java.lang.Iterable<java.security.KeyPair> doGetKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.client.config.keys.ClientIdentityProvider)
meth protected java.lang.Iterable<java.security.KeyPair> loadKeys(org.apache.sshd.common.session.SessionContext,java.util.function.Predicate<java.security.KeyPair>)
meth public java.lang.Iterable<java.security.KeyPair> loadKeys(org.apache.sshd.common.session.SessionContext)
meth public static java.util.List<org.apache.sshd.client.config.keys.ClientIdentityProvider> buildProviders(java.util.Collection<? extends java.nio.file.Path>,org.apache.sshd.client.config.keys.ClientIdentityLoader,org.apache.sshd.common.config.keys.FilePasswordProvider,boolean)
meth public static java.util.List<org.apache.sshd.client.config.keys.ClientIdentityProvider> buildProviders(java.util.Collection<? extends java.nio.file.Path>,org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder,org.apache.sshd.common.config.keys.FilePasswordProviderHolder,boolean)
supr org.apache.sshd.common.keyprovider.AbstractKeyPairProvider
hfds providers

CLSS public final org.apache.sshd.client.config.keys.ClientIdentity
fld public final static java.lang.String ID_FILE_PREFIX = "id_"
fld public final static java.lang.String ID_FILE_SUFFIX = ""
fld public final static java.util.function.Function<java.lang.String,java.lang.String> ID_GENERATOR
meth public !varargs static java.util.Map<java.lang.String,java.nio.file.Path> scanIdentitiesFolder(java.nio.file.Path,boolean,java.util.Collection<java.lang.String>,java.util.function.Function<? super java.lang.String,java.lang.String>,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs static java.util.Map<java.lang.String,java.security.KeyPair> loadDefaultIdentities(boolean,org.apache.sshd.common.config.keys.FilePasswordProvider,java.nio.file.LinkOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public !varargs static java.util.Map<java.lang.String,java.security.KeyPair> loadDefaultIdentities(java.nio.file.Path,boolean,org.apache.sshd.common.config.keys.FilePasswordProvider,java.nio.file.LinkOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public !varargs static java.util.Map<java.lang.String,java.security.KeyPair> loadIdentities(org.apache.sshd.common.session.SessionContext,java.nio.file.Path,boolean,java.util.Collection<java.lang.String>,java.util.function.Function<? super java.lang.String,java.lang.String>,org.apache.sshd.common.config.keys.FilePasswordProvider,java.nio.file.LinkOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public !varargs static org.apache.sshd.common.keyprovider.KeyPairProvider loadDefaultKeyPairProvider(boolean,boolean,org.apache.sshd.common.config.keys.FilePasswordProvider,java.nio.file.LinkOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public !varargs static org.apache.sshd.common.keyprovider.KeyPairProvider loadDefaultKeyPairProvider(java.nio.file.Path,boolean,boolean,org.apache.sshd.common.config.keys.FilePasswordProvider,java.nio.file.LinkOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.lang.String getIdentityFileName(java.lang.String)
meth public static java.lang.String getIdentityFileName(org.apache.sshd.common.NamedResource)
meth public static java.lang.String getIdentityType(java.lang.String)
supr java.lang.Object

CLSS public org.apache.sshd.client.config.keys.ClientIdentityFileWatcher
cons public init(java.nio.file.Path,org.apache.sshd.client.config.keys.ClientIdentityLoader,org.apache.sshd.common.config.keys.FilePasswordProvider)
cons public init(java.nio.file.Path,org.apache.sshd.client.config.keys.ClientIdentityLoader,org.apache.sshd.common.config.keys.FilePasswordProvider,boolean)
cons public init(java.nio.file.Path,org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder,org.apache.sshd.common.config.keys.FilePasswordProviderHolder)
cons public init(java.nio.file.Path,org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder,org.apache.sshd.common.config.keys.FilePasswordProviderHolder,boolean)
intf org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder
intf org.apache.sshd.client.config.keys.ClientIdentityProvider
intf org.apache.sshd.common.config.keys.FilePasswordProviderHolder
meth protected java.lang.Iterable<java.security.KeyPair> reloadClientIdentities(org.apache.sshd.common.session.SessionContext,java.nio.file.Path) throws java.io.IOException,java.security.GeneralSecurityException
meth public boolean isStrict()
meth public java.lang.Iterable<java.security.KeyPair> getClientIdentities(org.apache.sshd.common.session.SessionContext) throws java.io.IOException,java.security.GeneralSecurityException
meth public org.apache.sshd.client.config.keys.ClientIdentityLoader getClientIdentityLoader()
meth public org.apache.sshd.common.config.keys.FilePasswordProvider getFilePasswordProvider()
supr org.apache.sshd.common.util.io.ModifiableFileWatcher
hfds identitiesHolder,loaderHolder,providerHolder,strict

CLSS public abstract interface org.apache.sshd.client.config.keys.ClientIdentityLoader
fld public final static org.apache.sshd.client.config.keys.ClientIdentityLoader DEFAULT
meth public abstract boolean isValidLocation(org.apache.sshd.common.NamedResource) throws java.io.IOException
meth public abstract java.lang.Iterable<java.security.KeyPair> loadClientIdentities(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.config.keys.FilePasswordProvider) throws java.io.IOException,java.security.GeneralSecurityException
meth public static org.apache.sshd.common.keyprovider.KeyIdentityProvider asKeyIdentityProvider(org.apache.sshd.client.config.keys.ClientIdentityLoader,java.util.Collection<? extends org.apache.sshd.common.NamedResource>,org.apache.sshd.common.config.keys.FilePasswordProvider,boolean)

CLSS public abstract interface org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.client.config.keys.ClientIdentityLoader getClientIdentityLoader()
meth public static org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder loaderHolderOf(org.apache.sshd.client.config.keys.ClientIdentityLoader)

CLSS public abstract interface org.apache.sshd.client.config.keys.ClientIdentityLoaderManager
intf org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder
meth public abstract void setClientIdentityLoader(org.apache.sshd.client.config.keys.ClientIdentityLoader)

CLSS public abstract interface org.apache.sshd.client.config.keys.ClientIdentityProvider
 anno 0 java.lang.FunctionalInterface()
meth public abstract java.lang.Iterable<java.security.KeyPair> getClientIdentities(org.apache.sshd.common.session.SessionContext) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.lang.Iterable<java.security.KeyPair> lazyKeysLoader(java.lang.Iterable<? extends org.apache.sshd.client.config.keys.ClientIdentityProvider>,java.util.function.Function<? super org.apache.sshd.client.config.keys.ClientIdentityProvider,? extends java.lang.Iterable<java.security.KeyPair>>,java.util.function.Predicate<java.security.KeyPair>)
meth public static java.util.Iterator<java.security.KeyPair> lazyKeysIterator(java.util.Iterator<? extends org.apache.sshd.client.config.keys.ClientIdentityProvider>,java.util.function.Function<? super org.apache.sshd.client.config.keys.ClientIdentityProvider,? extends java.lang.Iterable<java.security.KeyPair>>,java.util.function.Predicate<java.security.KeyPair>)
meth public static org.apache.sshd.client.config.keys.ClientIdentityProvider of(java.security.KeyPair)

CLSS public org.apache.sshd.client.config.keys.DefaultClientIdentitiesWatcher
cons public init(boolean,org.apache.sshd.client.config.keys.ClientIdentityLoader,org.apache.sshd.common.config.keys.FilePasswordProvider,boolean)
cons public init(boolean,org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder,org.apache.sshd.common.config.keys.FilePasswordProviderHolder,boolean)
cons public init(org.apache.sshd.client.config.keys.ClientIdentityLoader,org.apache.sshd.common.config.keys.FilePasswordProvider)
cons public init(org.apache.sshd.client.config.keys.ClientIdentityLoader,org.apache.sshd.common.config.keys.FilePasswordProvider,boolean)
cons public init(org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder,org.apache.sshd.common.config.keys.FilePasswordProviderHolder)
cons public init(org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder,org.apache.sshd.common.config.keys.FilePasswordProviderHolder,boolean)
meth public static java.util.List<java.nio.file.Path> getDefaultBuiltinIdentitiesPaths()
supr org.apache.sshd.client.config.keys.BuiltinClientIdentitiesWatcher

CLSS public org.apache.sshd.client.config.keys.LazyClientIdentityIterator
cons public init(java.util.Iterator<? extends org.apache.sshd.client.config.keys.ClientIdentityProvider>,java.util.function.Function<? super org.apache.sshd.client.config.keys.ClientIdentityProvider,? extends java.lang.Iterable<java.security.KeyPair>>,java.util.function.Predicate<java.security.KeyPair>)
fld protected boolean finished
fld protected java.security.KeyPair currentPair
fld protected java.util.Iterator<java.security.KeyPair> currentIdentities
intf java.util.Iterator<java.security.KeyPair>
meth public boolean hasNext()
meth public java.lang.String toString()
meth public java.security.KeyPair next()
meth public java.util.Iterator<? extends org.apache.sshd.client.config.keys.ClientIdentityProvider> getProviders()
meth public java.util.function.Function<? super org.apache.sshd.client.config.keys.ClientIdentityProvider,? extends java.lang.Iterable<java.security.KeyPair>> getIdentitiesExtractor()
meth public java.util.function.Predicate<java.security.KeyPair> getFilter()
supr java.lang.Object
hfds filter,kpExtractor,providers

CLSS public org.apache.sshd.client.config.keys.LazyClientKeyIdentityProvider
cons public init(org.apache.sshd.client.config.keys.ClientIdentityLoader,java.util.Collection<? extends org.apache.sshd.common.NamedResource>,org.apache.sshd.common.config.keys.FilePasswordProvider,boolean)
intf org.apache.sshd.client.config.keys.ClientIdentityLoaderHolder
intf org.apache.sshd.common.config.keys.FilePasswordProviderHolder
intf org.apache.sshd.common.keyprovider.KeyIdentityProvider
meth protected java.lang.Iterable<java.security.KeyPair> loadClientIdentities(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource) throws java.io.IOException,java.security.GeneralSecurityException
meth public boolean isIgnoreNonExisting()
meth public java.lang.Iterable<java.security.KeyPair> loadKeys(org.apache.sshd.common.session.SessionContext) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<? extends org.apache.sshd.common.NamedResource> getLocations()
meth public org.apache.sshd.client.config.keys.ClientIdentityLoader getClientIdentityLoader()
meth public org.apache.sshd.common.config.keys.FilePasswordProvider getFilePasswordProvider()
supr java.lang.Object
hfds clientIdentityLoader,ignoreNonExisting,locations,passwordProvider

CLSS public abstract interface org.apache.sshd.client.future.AuthFuture
intf org.apache.sshd.common.future.Cancellable
intf org.apache.sshd.common.future.SshFuture<org.apache.sshd.client.future.AuthFuture>
intf org.apache.sshd.common.future.VerifiableFuture<org.apache.sshd.client.future.AuthFuture>
meth public abstract boolean isFailure()
meth public abstract boolean isSuccess()
meth public abstract boolean wasCanceled()
meth public abstract void setAuthed(boolean)
meth public abstract void setCancellable(boolean)

CLSS public abstract interface org.apache.sshd.client.future.ConnectFuture
intf org.apache.sshd.client.session.ClientSessionHolder
intf org.apache.sshd.common.future.Cancellable
intf org.apache.sshd.common.future.SshFuture<org.apache.sshd.client.future.ConnectFuture>
intf org.apache.sshd.common.future.VerifiableFuture<org.apache.sshd.client.future.ConnectFuture>
intf org.apache.sshd.common.session.SessionHolder<org.apache.sshd.client.session.ClientSession>
meth public abstract boolean isConnected()
meth public abstract void setSession(org.apache.sshd.client.session.ClientSession)
meth public org.apache.sshd.client.session.ClientSession getClientSession()

CLSS public org.apache.sshd.client.future.DefaultAuthFuture
cons public init(java.lang.Object,java.lang.Object)
intf org.apache.sshd.client.future.AuthFuture
meth public !varargs org.apache.sshd.client.future.AuthFuture verify(long,org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public boolean isFailure()
meth public boolean isSuccess()
meth public boolean wasCanceled()
meth public org.apache.sshd.common.future.CancelFuture cancel()
meth public org.apache.sshd.common.future.CancelFuture getCancellation()
meth public void setAuthed(boolean)
meth public void setCancellable(boolean)
supr org.apache.sshd.common.future.DefaultCancellableSshFuture<org.apache.sshd.client.future.AuthFuture>
hfds cancellable,cancellation

CLSS public org.apache.sshd.client.future.DefaultConnectFuture
cons public init(java.lang.Object,java.lang.Object)
intf org.apache.sshd.client.future.ConnectFuture
meth public !varargs org.apache.sshd.client.future.ConnectFuture verify(long,org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public boolean isConnected()
meth public org.apache.sshd.client.session.ClientSession getSession()
meth public void setSession(org.apache.sshd.client.session.ClientSession)
supr org.apache.sshd.common.future.DefaultCancellableSshFuture<org.apache.sshd.client.future.ConnectFuture>

CLSS public org.apache.sshd.client.future.DefaultOpenFuture
cons public init(java.lang.Object,java.lang.Object)
intf org.apache.sshd.client.future.OpenFuture
meth public !varargs org.apache.sshd.client.future.OpenFuture verify(long,org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public boolean isOpened()
meth public void setOpened()
supr org.apache.sshd.common.future.DefaultCancellableSshFuture<org.apache.sshd.client.future.OpenFuture>

CLSS public abstract interface org.apache.sshd.client.future.OpenFuture
intf org.apache.sshd.common.future.Cancellable
intf org.apache.sshd.common.future.SshFuture<org.apache.sshd.client.future.OpenFuture>
intf org.apache.sshd.common.future.VerifiableFuture<org.apache.sshd.client.future.OpenFuture>
meth public abstract boolean isOpened()
meth public abstract void setOpened()

CLSS public org.apache.sshd.client.global.OpenSshHostKeysHandler
cons public init()
cons public init(org.apache.sshd.common.util.buffer.keys.BufferPublicKeyParser<? extends java.security.PublicKey>)
fld public final static java.lang.String REQUEST = "hostkeys-00@openssh.com"
fld public final static org.apache.sshd.client.global.OpenSshHostKeysHandler INSTANCE
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleHostKeys(org.apache.sshd.common.session.Session,java.util.Collection<? extends java.security.PublicKey>,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
supr org.apache.sshd.common.global.AbstractOpenSshHostKeysHandler

CLSS public abstract org.apache.sshd.client.kex.AbstractDHClientKeyExchange
cons protected init(org.apache.sshd.common.session.Session)
intf org.apache.sshd.client.session.ClientSessionHolder
meth public final org.apache.sshd.client.session.AbstractClientSession getClientSession()
supr org.apache.sshd.common.kex.dh.AbstractDHKeyExchange

CLSS public org.apache.sshd.client.kex.DHGClient
cons protected init(org.apache.sshd.common.kex.DHFactory,org.apache.sshd.common.session.Session)
fld protected final org.apache.sshd.common.kex.DHFactory factory
fld protected org.apache.sshd.common.kex.AbstractDH dh
meth protected org.apache.sshd.common.kex.AbstractDH getDH() throws java.lang.Exception
meth protected void verifyCertificate(org.apache.sshd.common.session.Session,org.apache.sshd.common.config.keys.OpenSshCertificate) throws java.lang.Exception
meth public boolean next(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public final java.lang.String getName()
meth public static org.apache.sshd.common.kex.KeyExchangeFactory newFactory(org.apache.sshd.common.kex.DHFactory)
meth public void init(byte[],byte[],byte[],byte[]) throws java.lang.Exception
supr org.apache.sshd.client.kex.AbstractDHClientKeyExchange
hfds kemClient

CLSS public org.apache.sshd.client.kex.DHGEXClient
cons protected init(org.apache.sshd.common.kex.DHFactory,org.apache.sshd.common.session.Session)
fld protected byte expected
fld protected byte[] g
fld protected final org.apache.sshd.common.kex.DHFactory factory
fld protected int max
fld protected int min
fld protected int prf
fld protected org.apache.sshd.common.kex.AbstractDH dh
meth protected byte[] getP()
meth protected java.math.BigInteger getPValue()
meth protected org.apache.sshd.common.kex.AbstractDH getDH(java.math.BigInteger,java.math.BigInteger) throws java.lang.Exception
meth protected void setP(byte[])
meth protected void validateEValue() throws java.lang.Exception
meth protected void validateFValue() throws java.lang.Exception
meth public boolean next(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public final java.lang.String getName()
meth public static org.apache.sshd.common.kex.KeyExchangeFactory newFactory(org.apache.sshd.common.kex.DHFactory)
meth public void init(byte[],byte[],byte[],byte[]) throws java.lang.Exception
supr org.apache.sshd.client.kex.AbstractDHClientKeyExchange
hfds p,pValue

CLSS public final org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier
fld public final static org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier INSTANCE
supr org.apache.sshd.client.keyverifier.StaticServerKeyVerifier

CLSS public org.apache.sshd.client.keyverifier.DefaultKnownHostsServerKeyVerifier
cons public !varargs init(org.apache.sshd.client.keyverifier.ServerKeyVerifier,boolean,java.nio.file.Path,java.nio.file.LinkOption[])
cons public init(org.apache.sshd.client.keyverifier.ServerKeyVerifier)
cons public init(org.apache.sshd.client.keyverifier.ServerKeyVerifier,boolean)
cons public init(org.apache.sshd.client.keyverifier.ServerKeyVerifier,boolean,java.io.File)
meth protected java.util.List<org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier$HostEntryPair> reloadKnownHosts(org.apache.sshd.client.session.ClientSession,java.nio.file.Path) throws java.io.IOException,java.security.GeneralSecurityException
meth public final boolean isStrict()
supr org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier
hfds strict

CLSS public org.apache.sshd.client.keyverifier.DelegatingServerKeyVerifier
cons public init()
intf org.apache.sshd.client.keyverifier.ServerKeyVerifier
meth public boolean verifyServerKey(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey)
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier
cons public !varargs init(org.apache.sshd.client.keyverifier.ServerKeyVerifier,java.nio.file.Path,java.nio.file.LinkOption[])
cons public init(org.apache.sshd.client.keyverifier.ServerKeyVerifier,java.nio.file.Path)
fld protected final java.lang.Object updateLock
fld public final static java.lang.String KNOWN_HOSTS_FILE_OPTION = "UserKnownHostsFile"
fld public final static java.lang.String STRICT_CHECKING_OPTION = "StrictHostKeyChecking"
innr public static HostEntryPair
intf org.apache.sshd.client.keyverifier.ModifiedServerKeyAcceptor
intf org.apache.sshd.client.keyverifier.ServerKeyVerifier
meth protected boolean acceptIncompleteHostKeys(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey,java.lang.Throwable)
meth protected boolean acceptKnownHostEntries(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey,java.util.Collection<org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier$HostEntryPair>)
meth protected boolean acceptUnknownHostKey(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey)
meth protected java.lang.String prepareModifiedServerKeyLine(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,org.apache.sshd.client.config.hosts.KnownHostEntry,java.lang.String,java.security.PublicKey,java.security.PublicKey) throws java.lang.Exception
meth protected java.security.PublicKey resolveHostKey(org.apache.sshd.client.session.ClientSession,org.apache.sshd.client.config.hosts.KnownHostEntry,org.apache.sshd.common.config.keys.PublicKeyEntryResolver) throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.util.Collection<org.apache.sshd.common.util.net.SshdSocketAddress> resolveHostNetworkIdentities(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress)
meth protected java.util.List<org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier$HostEntryPair> findKnownHostEntries(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.util.Collection<org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier$HostEntryPair>)
meth protected java.util.List<org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier$HostEntryPair> reloadKnownHosts(org.apache.sshd.client.session.ClientSession,java.nio.file.Path) throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.util.function.Supplier<java.util.Collection<org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier$HostEntryPair>> getKnownHostSupplier(org.apache.sshd.client.session.ClientSession,java.nio.file.Path)
meth protected org.apache.sshd.client.config.hosts.KnownHostEntry prepareKnownHostEntry(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey) throws java.lang.Exception
meth protected org.apache.sshd.client.config.hosts.KnownHostEntry updateKnownHostsFile(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey,java.nio.file.Path,java.util.Collection<org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier$HostEntryPair>) throws java.lang.Exception
meth protected org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac> getHostValueDigester(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress)
meth protected org.apache.sshd.common.config.keys.PublicKeyEntryResolver getFallbackPublicKeyEntryResolver()
meth protected void handleKnownHostsFileUpdateFailure(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey,java.nio.file.Path,java.util.Collection<org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier$HostEntryPair>,java.lang.Throwable)
meth protected void handleModifiedServerKeyUpdateFailure(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier$HostEntryPair,java.security.PublicKey,java.nio.file.Path,java.util.Collection<org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier$HostEntryPair>,java.lang.Throwable)
meth protected void handleRevokedKey(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey)
meth protected void setLoadedHostsEntries(java.util.Collection<org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier$HostEntryPair>)
meth protected void updateModifiedServerKey(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey,java.util.Collection<org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier$HostEntryPair>,org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier$HostEntryPair)
meth protected void updateModifiedServerKey(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier$HostEntryPair,java.security.PublicKey,java.nio.file.Path,java.util.Collection<org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier$HostEntryPair>) throws java.lang.Exception
meth public boolean acceptModifiedServerKey(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,org.apache.sshd.client.config.hosts.KnownHostEntry,java.security.PublicKey,java.security.PublicKey) throws java.lang.Exception
meth public boolean verifyServerKey(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey)
meth public org.apache.sshd.client.keyverifier.ModifiedServerKeyAcceptor getModifiedServerKeyAcceptor()
meth public org.apache.sshd.client.keyverifier.ServerKeyVerifier getDelegateVerifier()
meth public void setModifiedServerKeyAcceptor(org.apache.sshd.client.keyverifier.ModifiedServerKeyAcceptor)
supr org.apache.sshd.common.util.io.ModifiableFileWatcher
hfds delegate,keysSupplier,modKeyAcceptor

CLSS public static org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier$HostEntryPair
 outer org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier
cons public init()
cons public init(org.apache.sshd.client.config.hosts.KnownHostEntry,java.security.PublicKey)
meth public java.lang.String toString()
meth public java.security.PublicKey getServerKey()
meth public org.apache.sshd.client.config.hosts.KnownHostEntry getHostEntry()
meth public void setHostEntry(org.apache.sshd.client.config.hosts.KnownHostEntry)
meth public void setServerKey(java.security.PublicKey)
supr java.lang.Object
hfds hostEntry,serverKey

CLSS public abstract interface org.apache.sshd.client.keyverifier.ModifiedServerKeyAcceptor
 anno 0 java.lang.FunctionalInterface()
meth public abstract boolean acceptModifiedServerKey(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,org.apache.sshd.client.config.hosts.KnownHostEntry,java.security.PublicKey,java.security.PublicKey) throws java.lang.Exception

CLSS public final org.apache.sshd.client.keyverifier.RejectAllServerKeyVerifier
fld public final static org.apache.sshd.client.keyverifier.RejectAllServerKeyVerifier INSTANCE
supr org.apache.sshd.client.keyverifier.StaticServerKeyVerifier

CLSS public org.apache.sshd.client.keyverifier.RequiredServerKeyVerifier
cons public init(java.security.PublicKey)
intf org.apache.sshd.client.keyverifier.ServerKeyVerifier
meth public boolean verifyServerKey(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey)
meth public final java.security.PublicKey getRequiredKey()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds requiredKey

CLSS public abstract interface org.apache.sshd.client.keyverifier.ServerKeyVerifier
 anno 0 java.lang.FunctionalInterface()
meth public abstract boolean verifyServerKey(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey)

CLSS public abstract org.apache.sshd.client.keyverifier.StaticServerKeyVerifier
cons protected init(boolean)
intf org.apache.sshd.client.keyverifier.ServerKeyVerifier
meth protected void handleAcceptance(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey)
meth protected void handleRejection(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey)
meth public final boolean isAccepted()
meth public final boolean verifyServerKey(org.apache.sshd.client.session.ClientSession,java.net.SocketAddress,java.security.PublicKey)
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds acceptance

CLSS public abstract org.apache.sshd.client.session.AbstractClientSession
cons protected init(org.apache.sshd.client.ClientFactoryManager,org.apache.sshd.common.io.IoSession)
fld protected final boolean sendImmediateClientIdentification
fld protected final boolean sendImmediateKexInit
intf org.apache.sshd.client.session.ClientSession
meth protected !varargs void setKexSeed(byte[])
meth protected boolean readIdentification(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected byte[] receiveKexInit(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected byte[] sendKexInit(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>) throws java.lang.Exception
meth protected java.lang.String resolveAvailableSignaturesProposal(org.apache.sshd.common.FactoryManager)
meth protected org.apache.sshd.client.session.ClientUserAuthService getUserAuthService()
meth protected org.apache.sshd.common.forward.Forwarder getForwarder()
meth protected org.apache.sshd.common.io.IoWriteFuture sendClientIdentification() throws java.lang.Exception
meth protected org.apache.sshd.common.session.ConnectionService getConnectionService()
meth protected void checkKeys() throws java.io.IOException
meth protected void initializeKeyExchangePhase() throws java.lang.Exception
meth protected void initializeProxyConnector() throws java.lang.Exception
meth protected void receiveKexInit(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,byte[]) throws java.io.IOException
meth protected void signalExtraServerVersionInfo(java.lang.String,java.util.List<java.lang.String>) throws java.lang.Exception
meth public java.lang.String removePasswordIdentity(java.lang.String)
meth public java.net.SocketAddress getConnectAddress()
meth public java.security.KeyPair removePublicKeyIdentity(java.security.KeyPair)
meth public java.security.PublicKey getServerKey()
meth public java.util.List<org.apache.sshd.client.auth.UserAuthFactory> getUserAuthFactories()
meth public org.apache.sshd.client.ClientFactoryManager getFactoryManager()
meth public org.apache.sshd.client.auth.AuthenticationIdentitiesProvider getRegisteredIdentities()
meth public org.apache.sshd.client.auth.hostbased.HostBasedAuthenticationReporter getHostBasedAuthenticationReporter()
meth public org.apache.sshd.client.auth.keyboard.UserInteraction getUserInteraction()
meth public org.apache.sshd.client.auth.password.PasswordAuthenticationReporter getPasswordAuthenticationReporter()
meth public org.apache.sshd.client.auth.password.PasswordIdentityProvider getPasswordIdentityProvider()
meth public org.apache.sshd.client.auth.pubkey.PublicKeyAuthenticationReporter getPublicKeyAuthenticationReporter()
meth public org.apache.sshd.client.channel.ChannelDirectTcpip createDirectTcpipChannel(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.channel.ChannelExec createExecChannel(byte[],org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public org.apache.sshd.client.channel.ChannelExec createExecChannel(java.lang.String,java.nio.charset.Charset,org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public org.apache.sshd.client.channel.ChannelShell createShellChannel(org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public org.apache.sshd.client.channel.ChannelSubsystem createSubsystemChannel(java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.channel.ClientChannel createChannel(java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.channel.ClientChannel createChannel(java.lang.String,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.keyverifier.ServerKeyVerifier getServerKeyVerifier()
meth public org.apache.sshd.client.session.ClientProxyConnector getClientProxyConnector()
meth public org.apache.sshd.common.AttributeRepository getConnectionContext()
meth public org.apache.sshd.common.future.KeyExchangeFuture switchToNoneCipher() throws java.io.IOException
meth public org.apache.sshd.common.keyprovider.KeyIdentityProvider getKeyIdentityProvider()
meth public org.apache.sshd.common.util.net.SshdSocketAddress startDynamicPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.common.util.net.SshdSocketAddress startLocalPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.common.util.net.SshdSocketAddress startRemotePortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public void addPasswordIdentity(java.lang.String)
meth public void addPublicKeyIdentity(java.security.KeyPair)
meth public void setClientProxyConnector(org.apache.sshd.client.session.ClientProxyConnector)
meth public void setConnectAddress(java.net.SocketAddress)
meth public void setHostBasedAuthenticationReporter(org.apache.sshd.client.auth.hostbased.HostBasedAuthenticationReporter)
meth public void setKeyIdentityProvider(org.apache.sshd.common.keyprovider.KeyIdentityProvider)
meth public void setPasswordAuthenticationReporter(org.apache.sshd.client.auth.password.PasswordAuthenticationReporter)
meth public void setPasswordIdentityProvider(org.apache.sshd.client.auth.password.PasswordIdentityProvider)
meth public void setPublicKeyAuthenticationReporter(org.apache.sshd.client.auth.pubkey.PublicKeyAuthenticationReporter)
meth public void setServerKey(java.security.PublicKey)
meth public void setServerKeyVerifier(org.apache.sshd.client.keyverifier.ServerKeyVerifier)
meth public void setUserAuthFactories(java.util.List<org.apache.sshd.client.auth.UserAuthFactory>)
meth public void setUserInteraction(org.apache.sshd.client.auth.keyboard.UserInteraction)
meth public void startService(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void stopDynamicPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public void stopLocalPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public void stopRemotePortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
supr org.apache.sshd.common.session.helpers.AbstractSession
hfds connectAddress,connectionContext,hostBasedAuthenticationReporter,identities,identitiesProvider,keyIdentityProvider,passwordAuthenticationReporter,passwordIdentityProvider,proxyConnector,publicKeyAuthenticationReporter,serverKey,serverKeyVerifier,userAuthFactories,userInteraction

CLSS public org.apache.sshd.client.session.ClientConnectionService
cons public init(org.apache.sshd.client.session.AbstractClientSession) throws org.apache.sshd.common.SshException
fld protected final int heartbeatMaxNoReply
fld protected final java.lang.String heartbeatRequest
fld protected final java.time.Duration heartbeatInterval
fld protected final java.util.concurrent.atomic.AtomicInteger outstandingHeartbeats
fld protected java.util.concurrent.ScheduledFuture<?> clientHeartbeat
intf org.apache.sshd.client.session.ClientSessionHolder
meth protected boolean sendHeartBeat()
meth protected int configureMaxNoReply()
meth protected java.util.concurrent.ScheduledFuture<?> startHeartBeat()
meth protected void stopHeartBeat()
meth public final org.apache.sshd.client.session.ClientSession getClientSession()
meth public org.apache.sshd.agent.common.AgentForwardSupport getAgentForwardSupport()
meth public org.apache.sshd.client.session.AbstractClientSession getSession()
meth public org.apache.sshd.server.x11.X11ForwardSupport getX11ForwardSupport()
meth public void start()
supr org.apache.sshd.common.session.helpers.AbstractConnectionService

CLSS public org.apache.sshd.client.session.ClientConnectionServiceFactory
cons public init()
fld public final static org.apache.sshd.client.session.ClientConnectionServiceFactory INSTANCE
intf org.apache.sshd.common.ServiceFactory
meth public java.lang.String getName()
meth public org.apache.sshd.common.Service create(org.apache.sshd.common.session.Session) throws java.io.IOException
supr org.apache.sshd.common.session.AbstractConnectionServiceFactory

CLSS public abstract interface org.apache.sshd.client.session.ClientProxyConnector
 anno 0 java.lang.FunctionalInterface()
meth public abstract void sendClientProxyMetadata(org.apache.sshd.client.session.ClientSession) throws java.lang.Exception

CLSS public abstract interface org.apache.sshd.client.session.ClientProxyConnectorHolder
meth public abstract org.apache.sshd.client.session.ClientProxyConnector getClientProxyConnector()
meth public abstract void setClientProxyConnector(org.apache.sshd.client.session.ClientProxyConnector)

CLSS public abstract interface org.apache.sshd.client.session.ClientSession
fld public final static java.util.Set<org.apache.sshd.client.channel.ClientChannelEvent> REMOTE_COMMAND_WAIT_EVENTS
innr public final static !enum ClientSessionEvent
intf org.apache.sshd.client.ClientAuthenticationManager
intf org.apache.sshd.client.session.ClientProxyConnectorHolder
intf org.apache.sshd.common.forward.PortForwardingManager
intf org.apache.sshd.common.session.Session
meth public abstract java.net.SocketAddress getConnectAddress()
meth public abstract java.security.PublicKey getServerKey()
meth public abstract java.util.Map<java.lang.Object,java.lang.Object> getMetadataMap()
meth public abstract java.util.Set<org.apache.sshd.client.session.ClientSession$ClientSessionEvent> getSessionState()
meth public abstract java.util.Set<org.apache.sshd.client.session.ClientSession$ClientSessionEvent> waitFor(java.util.Collection<org.apache.sshd.client.session.ClientSession$ClientSessionEvent>,long)
meth public abstract org.apache.sshd.client.ClientFactoryManager getFactoryManager()
meth public abstract org.apache.sshd.client.channel.ChannelDirectTcpip createDirectTcpipChannel(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public abstract org.apache.sshd.client.channel.ChannelExec createExecChannel(byte[],org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public abstract org.apache.sshd.client.channel.ChannelExec createExecChannel(java.lang.String,java.nio.charset.Charset,org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public abstract org.apache.sshd.client.channel.ChannelShell createShellChannel(org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public abstract org.apache.sshd.client.channel.ChannelSubsystem createSubsystemChannel(java.lang.String) throws java.io.IOException
meth public abstract org.apache.sshd.client.channel.ClientChannel createChannel(java.lang.String) throws java.io.IOException
meth public abstract org.apache.sshd.client.channel.ClientChannel createChannel(java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract org.apache.sshd.client.future.AuthFuture auth() throws java.io.IOException
meth public abstract org.apache.sshd.common.AttributeRepository getConnectionContext()
meth public abstract org.apache.sshd.common.future.KeyExchangeFuture switchToNoneCipher() throws java.io.IOException
meth public java.lang.String executeRemoteCommand(java.lang.String) throws java.io.IOException
meth public java.lang.String executeRemoteCommand(java.lang.String,java.io.OutputStream,java.nio.charset.Charset) throws java.io.IOException
meth public java.lang.String executeRemoteCommand(java.lang.String,java.io.OutputStream,java.nio.charset.Charset,java.time.Duration) throws java.io.IOException
meth public java.lang.String executeRemoteCommand(java.lang.String,java.time.Duration) throws java.io.IOException
meth public java.util.Set<org.apache.sshd.client.session.ClientSession$ClientSessionEvent> waitFor(java.util.Collection<org.apache.sshd.client.session.ClientSession$ClientSessionEvent>,java.time.Duration)
meth public org.apache.sshd.client.channel.ChannelExec createExecChannel(java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.channel.ChannelExec createExecChannel(java.lang.String,org.apache.sshd.common.channel.PtyChannelConfigurationHolder,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public org.apache.sshd.client.channel.ChannelShell createShellChannel() throws java.io.IOException
meth public org.apache.sshd.client.session.forward.DynamicPortForwardingTracker createDynamicPortForwardingTracker(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.session.forward.ExplicitPortForwardingTracker createLocalPortForwardingTracker(int,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.session.forward.ExplicitPortForwardingTracker createLocalPortForwardingTracker(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.session.forward.ExplicitPortForwardingTracker createRemotePortForwardingTracker(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public static java.util.Iterator<java.lang.String> passwordIteratorOf(org.apache.sshd.client.session.ClientSession) throws java.io.IOException,java.security.GeneralSecurityException
meth public static org.apache.sshd.common.keyprovider.KeyIdentityProvider providerOf(org.apache.sshd.client.session.ClientSession)
meth public void executeRemoteCommand(java.lang.String,java.io.OutputStream,java.io.OutputStream,java.nio.charset.Charset) throws java.io.IOException
meth public void executeRemoteCommand(java.lang.String,java.io.OutputStream,java.io.OutputStream,java.nio.charset.Charset,java.time.Duration) throws java.io.IOException

CLSS public final static !enum org.apache.sshd.client.session.ClientSession$ClientSessionEvent
 outer org.apache.sshd.client.session.ClientSession
fld public final static org.apache.sshd.client.session.ClientSession$ClientSessionEvent AUTHED
fld public final static org.apache.sshd.client.session.ClientSession$ClientSessionEvent CLOSED
fld public final static org.apache.sshd.client.session.ClientSession$ClientSessionEvent TIMEOUT
fld public final static org.apache.sshd.client.session.ClientSession$ClientSessionEvent WAIT_AUTH
meth public static org.apache.sshd.client.session.ClientSession$ClientSessionEvent valueOf(java.lang.String)
meth public static org.apache.sshd.client.session.ClientSession$ClientSessionEvent[] values()
supr java.lang.Enum<org.apache.sshd.client.session.ClientSession$ClientSessionEvent>

CLSS public abstract interface org.apache.sshd.client.session.ClientSessionCreator
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<org.apache.sshd.common.util.net.SshdSocketAddress> TARGET_SERVER
meth public abstract org.apache.sshd.client.future.ConnectFuture connect(java.lang.String) throws java.io.IOException
meth public abstract org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.lang.String,int,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth public abstract org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.net.SocketAddress,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth public abstract org.apache.sshd.client.future.ConnectFuture connect(org.apache.sshd.client.config.hosts.HostConfigEntry,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.lang.String,int) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.lang.String,int,java.net.SocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.lang.String,int,org.apache.sshd.common.AttributeRepository) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.net.SocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.net.SocketAddress,java.net.SocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(java.lang.String,java.net.SocketAddress,org.apache.sshd.common.AttributeRepository) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(org.apache.sshd.client.config.hosts.HostConfigEntry) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(org.apache.sshd.client.config.hosts.HostConfigEntry,java.net.SocketAddress) throws java.io.IOException
meth public org.apache.sshd.client.future.ConnectFuture connect(org.apache.sshd.client.config.hosts.HostConfigEntry,org.apache.sshd.common.AttributeRepository) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.client.session.ClientSessionHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.client.session.ClientSession getClientSession()

CLSS public org.apache.sshd.client.session.ClientSessionImpl
cons public init(org.apache.sshd.client.ClientFactoryManager,org.apache.sshd.common.io.IoSession) throws java.lang.Exception
meth protected <%0 extends java.util.Collection<org.apache.sshd.client.session.ClientSession$ClientSessionEvent>> {%%0} updateCurrentSessionState({%%0})
meth protected java.lang.String nextServiceName()
meth protected java.util.List<org.apache.sshd.common.Service> getServices()
meth protected org.apache.sshd.common.session.helpers.CurrentService initializeCurrentService()
meth protected void handleDisconnect(int,java.lang.String,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void preClose()
meth protected void sendInitialServiceRequest() throws java.io.IOException
meth protected void signalAuthFailure(java.lang.Throwable)
meth protected void signalSessionEvent(org.apache.sshd.common.session.SessionListener$Event) throws java.lang.Exception
meth public java.util.Map<java.lang.Object,java.lang.Object> getMetadataMap()
meth public java.util.Set<org.apache.sshd.client.session.ClientSession$ClientSessionEvent> getSessionState()
meth public java.util.Set<org.apache.sshd.client.session.ClientSession$ClientSessionEvent> waitFor(java.util.Collection<org.apache.sshd.client.session.ClientSession$ClientSessionEvent>,long)
meth public org.apache.sshd.client.future.AuthFuture auth() throws java.io.IOException
meth public void exceptionCaught(java.lang.Throwable)
meth public void start() throws java.lang.Exception
meth public void switchToNextService() throws java.io.IOException
supr org.apache.sshd.client.session.AbstractClientSession
hfds authErrorHolder,authFuture,beforeAuthErrorHolder,initialServiceRequestSent,metadataMap
hcls Services

CLSS public org.apache.sshd.client.session.ClientUserAuthService
cons public init(org.apache.sshd.common.session.Session)
fld protected final java.util.List<java.lang.String> clientMethods
fld protected final java.util.List<org.apache.sshd.client.auth.UserAuthFactory> authFactories
fld protected final java.util.concurrent.atomic.AtomicReference<org.apache.sshd.client.future.AuthFuture> authFutureHolder
fld protected final org.apache.sshd.client.session.ClientSessionImpl clientSession
fld protected java.util.List<java.lang.String> serverMethods
intf org.apache.sshd.client.session.ClientSessionHolder
intf org.apache.sshd.common.Service
meth protected org.apache.sshd.client.future.AuthFuture createAuthFuture(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.io.IOException
meth protected org.apache.sshd.client.future.AuthFuture updateCurrentAuthFuture(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.io.IOException
meth protected org.apache.sshd.common.io.IoWriteFuture sendInitialAuthRequest(org.apache.sshd.client.session.ClientSession,java.lang.String) throws java.io.IOException
meth protected void preClose()
meth protected void processUserAuth(int,org.apache.sshd.common.util.buffer.Buffer,org.apache.sshd.client.future.AuthFuture) throws java.lang.Exception
meth protected void tryNext(int,org.apache.sshd.client.future.AuthFuture) throws java.lang.Exception
meth public java.lang.String getCurrentServiceName()
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public org.apache.sshd.client.future.AuthFuture auth(java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.session.ClientSession getClientSession()
meth public org.apache.sshd.client.session.ClientSession getSession()
meth public void process(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void start()
supr org.apache.sshd.common.util.closeable.AbstractCloseable
hfds currentMethod,currentUserAuth,initLock,initialRequestSender,properties,pubkeyAuth,service,started

CLSS public org.apache.sshd.client.session.ClientUserAuthServiceFactory
cons public init()
fld public final static org.apache.sshd.client.session.ClientUserAuthServiceFactory INSTANCE
meth public org.apache.sshd.common.Service create(org.apache.sshd.common.session.Session) throws java.io.IOException
supr org.apache.sshd.common.auth.AbstractUserAuthServiceFactory

CLSS public org.apache.sshd.client.session.SessionFactory
cons public init(org.apache.sshd.client.ClientFactoryManager)
meth protected org.apache.sshd.client.session.ClientSessionImpl doCreateSession(org.apache.sshd.common.io.IoSession) throws java.lang.Exception
meth public final org.apache.sshd.client.ClientFactoryManager getClient()
supr org.apache.sshd.common.session.helpers.AbstractSessionFactory<org.apache.sshd.client.ClientFactoryManager,org.apache.sshd.client.session.ClientSessionImpl>

CLSS public org.apache.sshd.client.session.forward.DynamicPortForwardingTracker
cons public init(org.apache.sshd.client.session.ClientSession,org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress)
meth public void close() throws java.io.IOException
supr org.apache.sshd.client.session.forward.PortForwardingTracker

CLSS public org.apache.sshd.client.session.forward.ExplicitPortForwardingTracker
cons public init(org.apache.sshd.client.session.ClientSession,boolean,org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress)
intf org.apache.sshd.common.util.net.ConnectionEndpointsIndicator
meth public boolean isLocalForwarding()
meth public java.lang.String toString()
meth public org.apache.sshd.common.util.net.SshdSocketAddress getRemoteAddress()
meth public void close() throws java.io.IOException
supr org.apache.sshd.client.session.forward.PortForwardingTracker
hfds localForwarding,remoteAddress

CLSS public abstract org.apache.sshd.client.session.forward.PortForwardingTracker
cons protected init(org.apache.sshd.client.session.ClientSession,org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress)
fld protected final java.util.concurrent.atomic.AtomicBoolean open
intf java.nio.channels.Channel
intf org.apache.sshd.client.session.ClientSessionHolder
intf org.apache.sshd.common.session.SessionHolder<org.apache.sshd.client.session.ClientSession>
meth public boolean isOpen()
meth public java.lang.String toString()
meth public org.apache.sshd.client.session.ClientSession getClientSession()
meth public org.apache.sshd.client.session.ClientSession getSession()
meth public org.apache.sshd.common.util.net.SshdSocketAddress getBoundAddress()
meth public org.apache.sshd.common.util.net.SshdSocketAddress getLocalAddress()
supr java.lang.Object
hfds boundAddress,localAddress,session

CLSS public abstract org.apache.sshd.client.simple.AbstractSimpleClient
cons protected init()
intf org.apache.sshd.client.simple.SimpleClient
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public abstract org.apache.sshd.client.simple.AbstractSimpleClientSessionCreator
cons protected init()
cons protected init(long,long)
intf org.apache.sshd.client.session.ClientSessionCreator
meth protected org.apache.sshd.client.session.ClientSession authSession(org.apache.sshd.client.future.ConnectFuture,java.lang.String) throws java.io.IOException
meth protected org.apache.sshd.client.session.ClientSession authSession(org.apache.sshd.client.future.ConnectFuture,java.security.KeyPair) throws java.io.IOException
meth protected org.apache.sshd.client.session.ClientSession authSession(org.apache.sshd.client.session.ClientSession) throws java.io.IOException
meth protected org.apache.sshd.client.session.ClientSession loginSession(org.apache.sshd.client.future.ConnectFuture,java.lang.String) throws java.io.IOException
meth protected org.apache.sshd.client.session.ClientSession loginSession(org.apache.sshd.client.future.ConnectFuture,java.security.KeyPair) throws java.io.IOException
meth public long getAuthenticationTimeout()
meth public long getConnectTimeout()
meth public org.apache.sshd.client.session.ClientSession sessionLogin(java.lang.String,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.session.ClientSession sessionLogin(java.lang.String,java.security.KeyPair) throws java.io.IOException
meth public org.apache.sshd.client.session.ClientSession sessionLogin(java.net.SocketAddress,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.session.ClientSession sessionLogin(java.net.SocketAddress,java.lang.String,java.security.KeyPair) throws java.io.IOException
meth public static org.apache.sshd.client.simple.SimpleClient wrap(org.apache.sshd.client.session.ClientSessionCreator,java.nio.channels.Channel)
meth public void setAuthenticationTimeout(long)
meth public void setConnectTimeout(long)
supr org.apache.sshd.client.simple.AbstractSimpleClient
hfds authenticateTimeout,connectTimeout

CLSS public abstract interface org.apache.sshd.client.simple.SimpleClient
intf org.apache.sshd.client.simple.SimpleSessionClient

CLSS public abstract interface org.apache.sshd.client.simple.SimpleClientConfigurator
fld public final static int DEFAULT_PORT = 22
fld public final static long DEFAULT_AUTHENTICATION_TIMEOUT = 9223372036854775807
fld public final static long DEFAULT_CONNECT_TIMEOUT = 9223372036854775807
meth public abstract long getAuthenticationTimeout()
meth public abstract long getConnectTimeout()
meth public abstract void setAuthenticationTimeout(long)
meth public abstract void setConnectTimeout(long)

CLSS public abstract interface org.apache.sshd.client.simple.SimpleSessionClient
intf java.nio.channels.Channel
intf org.apache.sshd.client.simple.SimpleClientConfigurator
meth public abstract org.apache.sshd.client.session.ClientSession sessionLogin(java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract org.apache.sshd.client.session.ClientSession sessionLogin(java.lang.String,java.security.KeyPair) throws java.io.IOException
meth public abstract org.apache.sshd.client.session.ClientSession sessionLogin(java.net.SocketAddress,java.lang.String,java.lang.String) throws java.io.IOException
meth public abstract org.apache.sshd.client.session.ClientSession sessionLogin(java.net.SocketAddress,java.lang.String,java.security.KeyPair) throws java.io.IOException
meth public org.apache.sshd.client.session.ClientSession sessionLogin(java.lang.String,int,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.session.ClientSession sessionLogin(java.lang.String,int,java.lang.String,java.security.KeyPair) throws java.io.IOException
meth public org.apache.sshd.client.session.ClientSession sessionLogin(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.session.ClientSession sessionLogin(java.lang.String,java.lang.String,java.security.KeyPair) throws java.io.IOException
meth public org.apache.sshd.client.session.ClientSession sessionLogin(java.net.InetAddress,int,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.session.ClientSession sessionLogin(java.net.InetAddress,int,java.lang.String,java.security.KeyPair) throws java.io.IOException
meth public org.apache.sshd.client.session.ClientSession sessionLogin(java.net.InetAddress,java.lang.String,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.client.session.ClientSession sessionLogin(java.net.InetAddress,java.lang.String,java.security.KeyPair) throws java.io.IOException

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

CLSS public abstract interface org.apache.sshd.common.AlgorithmNameProvider
meth public abstract java.lang.String getAlgorithm()

CLSS public abstract interface org.apache.sshd.common.AttributeRepository
innr public static AttributeKey
meth public <%0 extends java.lang.Object> {%%0} resolveAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} getAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public abstract int getAttributesCount()
meth public abstract java.util.Collection<org.apache.sshd.common.AttributeRepository$AttributeKey<?>> attributeKeys()
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.AttributeRepository ofKeyValuePair(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,{%%0})
meth public static org.apache.sshd.common.AttributeRepository ofAttributesMap(java.util.Map<org.apache.sshd.common.AttributeRepository$AttributeKey<?>,?>)

CLSS public static org.apache.sshd.common.AttributeRepository$AttributeKey<%0 extends java.lang.Object>
 outer org.apache.sshd.common.AttributeRepository
cons public init()
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.common.AttributeStore
intf org.apache.sshd.common.AttributeRepository
meth public <%0 extends java.lang.Object> {%%0} computeAttributeIfAbsent(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,java.util.function.Function<? super org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,? extends {%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} removeAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} setAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,{%%0})
meth public abstract void clearAttributes()

CLSS public org.apache.sshd.common.BaseBuilder<%0 extends org.apache.sshd.common.helpers.AbstractFactoryManager, %1 extends org.apache.sshd.common.BaseBuilder<{org.apache.sshd.common.BaseBuilder%0},{org.apache.sshd.common.BaseBuilder%1}>>
cons public init()
fld protected java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.cipher.Cipher>> cipherFactories
fld protected java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.compression.Compression>> compressionFactories
fld protected java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac>> macFactories
fld protected java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> signatureFactories
fld protected java.util.List<org.apache.sshd.common.channel.ChannelFactory> channelFactories
fld protected java.util.List<org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.session.ConnectionService>> globalRequestHandlers
fld protected java.util.List<org.apache.sshd.common.kex.KeyExchangeFactory> keyExchangeFactories
fld protected org.apache.sshd.common.Factory<org.apache.sshd.common.random.Random> randomFactory
fld protected org.apache.sshd.common.Factory<{org.apache.sshd.common.BaseBuilder%0}> factory
fld protected org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver channelStreamPacketWriterResolver
fld protected org.apache.sshd.common.file.FileSystemFactory fileSystemFactory
fld protected org.apache.sshd.common.forward.ForwarderFactory forwarderFactory
fld protected org.apache.sshd.common.kex.extension.KexExtensionHandler kexExtensionHandler
fld protected org.apache.sshd.common.session.UnknownChannelReferenceHandler unknownChannelReferenceHandler
fld protected org.apache.sshd.server.forward.ForwardingFilter forwardingFilter
fld public final static java.util.List<org.apache.sshd.common.cipher.BuiltinCiphers> DEFAULT_CIPHERS_PREFERENCE
fld public final static java.util.List<org.apache.sshd.common.kex.BuiltinDHFactories> DEFAULT_KEX_PREFERENCE
fld public final static java.util.List<org.apache.sshd.common.mac.BuiltinMacs> DEFAULT_MAC_PREFERENCE
fld public final static java.util.List<org.apache.sshd.common.signature.BuiltinSignatures> DEFAULT_SIGNATURE_PREFERENCE
fld public final static org.apache.sshd.common.file.FileSystemFactory DEFAULT_FILE_SYSTEM_FACTORY
fld public final static org.apache.sshd.common.forward.ForwarderFactory DEFAULT_FORWARDER_FACTORY
fld public final static org.apache.sshd.common.session.UnknownChannelReferenceHandler DEFAULT_UNKNOWN_CHANNEL_REFERENCE_HANDLER
fld public final static org.apache.sshd.server.forward.ForwardingFilter DEFAULT_FORWARDING_FILTER
intf org.apache.sshd.common.util.ObjectBuilder<{org.apache.sshd.common.BaseBuilder%0}>
meth protected {org.apache.sshd.common.BaseBuilder%1} fillWithDefaultValues()
meth protected {org.apache.sshd.common.BaseBuilder%1} me()
meth public static java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.cipher.Cipher>> setUpDefaultCiphers(boolean)
meth public static java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac>> setUpDefaultMacs(boolean)
meth public {org.apache.sshd.common.BaseBuilder%0} build()
meth public {org.apache.sshd.common.BaseBuilder%0} build(boolean)
meth public {org.apache.sshd.common.BaseBuilder%1} channelFactories(java.util.List<org.apache.sshd.common.channel.ChannelFactory>)
meth public {org.apache.sshd.common.BaseBuilder%1} channelStreamPacketWriterResolver(org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver)
meth public {org.apache.sshd.common.BaseBuilder%1} cipherFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.cipher.Cipher>>)
meth public {org.apache.sshd.common.BaseBuilder%1} compressionFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.compression.Compression>>)
meth public {org.apache.sshd.common.BaseBuilder%1} factory(org.apache.sshd.common.Factory<{org.apache.sshd.common.BaseBuilder%0}>)
meth public {org.apache.sshd.common.BaseBuilder%1} fileSystemFactory(org.apache.sshd.common.file.FileSystemFactory)
meth public {org.apache.sshd.common.BaseBuilder%1} forwarderFactory(org.apache.sshd.common.forward.ForwarderFactory)
meth public {org.apache.sshd.common.BaseBuilder%1} forwardingFilter(org.apache.sshd.server.forward.ForwardingFilter)
meth public {org.apache.sshd.common.BaseBuilder%1} globalRequestHandlers(java.util.List<org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.session.ConnectionService>>)
meth public {org.apache.sshd.common.BaseBuilder%1} kexExtensionHandler(org.apache.sshd.common.kex.extension.KexExtensionHandler)
meth public {org.apache.sshd.common.BaseBuilder%1} keyExchangeFactories(java.util.List<org.apache.sshd.common.kex.KeyExchangeFactory>)
meth public {org.apache.sshd.common.BaseBuilder%1} macFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac>>)
meth public {org.apache.sshd.common.BaseBuilder%1} randomFactory(org.apache.sshd.common.Factory<org.apache.sshd.common.random.Random>)
meth public {org.apache.sshd.common.BaseBuilder%1} signatureFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
meth public {org.apache.sshd.common.BaseBuilder%1} unknownChannelReferenceHandler(org.apache.sshd.common.session.UnknownChannelReferenceHandler)
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.common.BuiltinFactory<%0 extends java.lang.Object>
intf org.apache.sshd.common.NamedFactory<{org.apache.sshd.common.BuiltinFactory%0}>
intf org.apache.sshd.common.OptionalFeature
meth public static <%0 extends java.lang.Object, %1 extends org.apache.sshd.common.BuiltinFactory<{%%0}>> java.util.List<org.apache.sshd.common.NamedFactory<{%%0}>> setUpFactories(boolean,java.util.Collection<? extends {%%1}>)

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

CLSS public final org.apache.sshd.common.CommonModuleProperties
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> ALLOW_INSECURE_AUTH
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> ALLOW_NON_INTEGRITY_AUTH
fld public final static org.apache.sshd.common.Property<java.lang.Integer> HEXDUMP_CHUNK_SIZE
fld public final static org.apache.sshd.common.Property<java.time.Duration> CLOSE_WAIT_TIMEOUT
fld public final static org.apache.sshd.common.Property<java.time.Duration> SESSION_HEARTBEAT_INTERVAL
fld public final static org.apache.sshd.common.Property<org.apache.sshd.common.session.SessionHeartbeatController$HeartbeatType> SESSION_HEARTBEAT_TYPE
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.common.Factory<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
intf java.util.function.Supplier<{org.apache.sshd.common.Factory%0}>
meth public abstract {org.apache.sshd.common.Factory%0} create()
meth public {org.apache.sshd.common.Factory%0} get()

CLSS public abstract interface org.apache.sshd.common.FactoryManager
fld public final static java.lang.String DEFAULT_VERSION = "SSHD-UNKNOWN"
intf org.apache.sshd.common.AttributeStore
intf org.apache.sshd.common.channel.ChannelListenerManager
intf org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolverManager
intf org.apache.sshd.common.forward.PortForwardingEventListenerManager
intf org.apache.sshd.common.io.IoServiceEventListenerManager
intf org.apache.sshd.common.kex.KexFactoryManager
intf org.apache.sshd.common.session.ReservedSessionMessagesManager
intf org.apache.sshd.common.session.SessionDisconnectHandlerManager
intf org.apache.sshd.common.session.SessionHeartbeatController
intf org.apache.sshd.common.session.SessionListenerManager
intf org.apache.sshd.common.session.UnknownChannelReferenceHandlerManager
meth public <%0 extends java.lang.Object> {%%0} resolveAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public abstract java.lang.String getVersion()
meth public abstract java.util.List<? extends org.apache.sshd.common.ServiceFactory> getServiceFactories()
meth public abstract java.util.List<? extends org.apache.sshd.common.channel.ChannelFactory> getChannelFactories()
meth public abstract java.util.List<org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.session.ConnectionService>> getGlobalRequestHandlers()
meth public abstract java.util.concurrent.ScheduledExecutorService getScheduledExecutorService()
meth public abstract org.apache.sshd.agent.SshAgentFactory getAgentFactory()
meth public abstract org.apache.sshd.common.Factory<? extends org.apache.sshd.common.random.Random> getRandomFactory()
meth public abstract org.apache.sshd.common.file.FileSystemFactory getFileSystemFactory()
meth public abstract org.apache.sshd.common.forward.ForwarderFactory getForwarderFactory()
meth public abstract org.apache.sshd.common.io.IoServiceFactory getIoServiceFactory()
meth public abstract org.apache.sshd.server.forward.ForwardingFilter getForwardingFilter()
meth public org.apache.sshd.server.forward.AgentForwardingFilter getAgentForwardingFilter()
meth public org.apache.sshd.server.forward.TcpForwardingFilter getTcpForwardingFilter()
meth public org.apache.sshd.server.forward.X11ForwardingFilter getX11ForwardingFilter()
meth public static <%0 extends java.lang.Object> {%%0} resolveAttribute(org.apache.sshd.common.FactoryManager,org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)

CLSS public abstract interface org.apache.sshd.common.FactoryManagerHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.common.FactoryManager getFactoryManager()

CLSS public abstract interface org.apache.sshd.common.NamedFactory<%0 extends java.lang.Object>
intf org.apache.sshd.common.Factory<{org.apache.sshd.common.NamedFactory%0}>
intf org.apache.sshd.common.NamedResource
meth public static <%0 extends java.lang.Object> {%%0} create(java.util.Collection<? extends org.apache.sshd.common.NamedFactory<? extends {%%0}>>,java.lang.String)
meth public static <%0 extends org.apache.sshd.common.NamedResource & org.apache.sshd.common.OptionalFeature> java.util.List<{%%0}> setUpBuiltinFactories(boolean,java.util.Collection<? extends {%%0}>)
meth public static <%0 extends org.apache.sshd.common.OptionalFeature, %1 extends org.apache.sshd.common.NamedResource> java.util.List<{%%1}> setUpTransformedFactories(boolean,java.util.Collection<? extends {%%0}>,java.util.function.Function<? super {%%0},? extends {%%1}>)

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

CLSS public abstract interface org.apache.sshd.common.Property<%0 extends java.lang.Object>
innr public abstract static BaseProperty
innr public static BooleanProperty
innr public static CharsetProperty
innr public static DurationInSecondsProperty
innr public static DurationProperty
innr public static EnumProperty
innr public static IntegerProperty
innr public static LongProperty
innr public static ObjectProperty
innr public static StringProperty
innr public static Validating
intf org.apache.sshd.common.NamedResource
meth public abstract java.lang.Class<{org.apache.sshd.common.Property%0}> getType()
meth public abstract java.util.Optional<{org.apache.sshd.common.Property%0}> get(org.apache.sshd.common.PropertyResolver)
meth public abstract java.util.Optional<{org.apache.sshd.common.Property%0}> getDefault()
meth public abstract void set(org.apache.sshd.common.PropertyResolver,{org.apache.sshd.common.Property%0})
meth public abstract {org.apache.sshd.common.Property%0} getOrCustomDefault(org.apache.sshd.common.PropertyResolver,{org.apache.sshd.common.Property%0})
meth public static <%0 extends java.lang.Enum<{%%0}>> org.apache.sshd.common.Property<{%%0}> enum_(java.lang.String,java.lang.Class<{%%0}>)
meth public static <%0 extends java.lang.Enum<{%%0}>> org.apache.sshd.common.Property<{%%0}> enum_(java.lang.String,java.lang.Class<{%%0}>,{%%0})
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.Property<{%%0}> validating(org.apache.sshd.common.Property<{%%0}>,java.util.function.Consumer<? super {%%0}>)
meth public static org.apache.sshd.common.Property<java.lang.Boolean> bool(java.lang.String)
meth public static org.apache.sshd.common.Property<java.lang.Boolean> bool(java.lang.String,boolean)
meth public static org.apache.sshd.common.Property<java.lang.Integer> integer(java.lang.String)
meth public static org.apache.sshd.common.Property<java.lang.Integer> integer(java.lang.String,int)
meth public static org.apache.sshd.common.Property<java.lang.Long> long_(java.lang.String)
meth public static org.apache.sshd.common.Property<java.lang.Long> long_(java.lang.String,long)
meth public static org.apache.sshd.common.Property<java.lang.Object> object(java.lang.String)
meth public static org.apache.sshd.common.Property<java.lang.Object> object(java.lang.String,java.lang.Object)
meth public static org.apache.sshd.common.Property<java.lang.String> string(java.lang.String)
meth public static org.apache.sshd.common.Property<java.lang.String> string(java.lang.String,java.lang.String)
meth public static org.apache.sshd.common.Property<java.nio.charset.Charset> charset(java.lang.String)
meth public static org.apache.sshd.common.Property<java.nio.charset.Charset> charset(java.lang.String,java.nio.charset.Charset)
meth public static org.apache.sshd.common.Property<java.time.Duration> duration(java.lang.String)
meth public static org.apache.sshd.common.Property<java.time.Duration> duration(java.lang.String,java.time.Duration)
meth public static org.apache.sshd.common.Property<java.time.Duration> duration(java.lang.String,java.time.Duration,java.time.Duration)
meth public static org.apache.sshd.common.Property<java.time.Duration> durationSec(java.lang.String)
meth public static org.apache.sshd.common.Property<java.time.Duration> durationSec(java.lang.String,java.time.Duration)
meth public static org.apache.sshd.common.Property<java.time.Duration> durationSec(java.lang.String,java.time.Duration,java.time.Duration)
meth public void remove(org.apache.sshd.common.PropertyResolver)
meth public {org.apache.sshd.common.Property%0} getOrNull(org.apache.sshd.common.PropertyResolver)
meth public {org.apache.sshd.common.Property%0} getRequired(org.apache.sshd.common.PropertyResolver)
meth public {org.apache.sshd.common.Property%0} getRequiredDefault()

CLSS public abstract static org.apache.sshd.common.Property$BaseProperty<%0 extends java.lang.Object>
 outer org.apache.sshd.common.Property
cons protected init(java.lang.String,java.lang.Class<{org.apache.sshd.common.Property$BaseProperty%0}>)
cons protected init(java.lang.String,java.lang.Class<{org.apache.sshd.common.Property$BaseProperty%0}>,{org.apache.sshd.common.Property$BaseProperty%0})
intf org.apache.sshd.common.Property<{org.apache.sshd.common.Property$BaseProperty%0}>
meth protected abstract {org.apache.sshd.common.Property$BaseProperty%0} fromStorage(java.lang.Object)
meth protected java.lang.Object toStorage({org.apache.sshd.common.Property$BaseProperty%0})
meth public java.lang.Class<{org.apache.sshd.common.Property$BaseProperty%0}> getType()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Optional<{org.apache.sshd.common.Property$BaseProperty%0}> get(org.apache.sshd.common.PropertyResolver)
meth public java.util.Optional<{org.apache.sshd.common.Property$BaseProperty%0}> getDefault()
meth public void set(org.apache.sshd.common.PropertyResolver,{org.apache.sshd.common.Property$BaseProperty%0})
meth public {org.apache.sshd.common.Property$BaseProperty%0} getOrCustomDefault(org.apache.sshd.common.PropertyResolver,{org.apache.sshd.common.Property$BaseProperty%0})
supr java.lang.Object
hfds defaultValue,name,type

CLSS public static org.apache.sshd.common.Property$BooleanProperty
 outer org.apache.sshd.common.Property
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Boolean)
meth protected java.lang.Boolean fromStorage(java.lang.Object)
supr org.apache.sshd.common.Property$BaseProperty<java.lang.Boolean>

CLSS public static org.apache.sshd.common.Property$CharsetProperty
 outer org.apache.sshd.common.Property
cons public init(java.lang.String)
cons public init(java.lang.String,java.nio.charset.Charset)
meth protected java.nio.charset.Charset fromStorage(java.lang.Object)
supr org.apache.sshd.common.Property$BaseProperty<java.nio.charset.Charset>

CLSS public static org.apache.sshd.common.Property$DurationInSecondsProperty
 outer org.apache.sshd.common.Property
cons public init(java.lang.String)
cons public init(java.lang.String,java.time.Duration)
cons public init(java.lang.String,java.time.Duration,java.time.Duration)
meth protected java.lang.Object toStorage(java.time.Duration)
meth protected java.time.Duration fromStorage(java.lang.Object)
supr org.apache.sshd.common.Property$DurationProperty

CLSS public static org.apache.sshd.common.Property$DurationProperty
 outer org.apache.sshd.common.Property
cons public init(java.lang.String)
cons public init(java.lang.String,java.time.Duration)
cons public init(java.lang.String,java.time.Duration,java.time.Duration)
fld protected final java.time.Duration min
meth protected java.lang.Object toStorage(java.time.Duration)
meth protected java.time.Duration fromStorage(java.lang.Object)
meth protected static java.time.Duration atLeast(java.lang.String,java.time.Duration,java.time.Duration)
supr org.apache.sshd.common.Property$BaseProperty<java.time.Duration>

CLSS public static org.apache.sshd.common.Property$EnumProperty<%0 extends java.lang.Enum<{org.apache.sshd.common.Property$EnumProperty%0}>>
 outer org.apache.sshd.common.Property
cons public init(java.lang.String,java.lang.Class<{org.apache.sshd.common.Property$EnumProperty%0}>)
cons public init(java.lang.String,java.lang.Class<{org.apache.sshd.common.Property$EnumProperty%0}>,{org.apache.sshd.common.Property$EnumProperty%0})
fld protected final java.util.Collection<{org.apache.sshd.common.Property$EnumProperty%0}> values
meth protected {org.apache.sshd.common.Property$EnumProperty%0} fromStorage(java.lang.Object)
supr org.apache.sshd.common.Property$BaseProperty<{org.apache.sshd.common.Property$EnumProperty%0}>

CLSS public static org.apache.sshd.common.Property$IntegerProperty
 outer org.apache.sshd.common.Property
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Integer)
meth protected java.lang.Integer fromStorage(java.lang.Object)
supr org.apache.sshd.common.Property$BaseProperty<java.lang.Integer>

CLSS public static org.apache.sshd.common.Property$LongProperty
 outer org.apache.sshd.common.Property
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Long)
meth protected java.lang.Long fromStorage(java.lang.Object)
supr org.apache.sshd.common.Property$BaseProperty<java.lang.Long>

CLSS public static org.apache.sshd.common.Property$ObjectProperty
 outer org.apache.sshd.common.Property
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Object)
meth protected java.lang.Object fromStorage(java.lang.Object)
supr org.apache.sshd.common.Property$BaseProperty<java.lang.Object>

CLSS public static org.apache.sshd.common.Property$StringProperty
 outer org.apache.sshd.common.Property
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
meth protected java.lang.String fromStorage(java.lang.Object)
supr org.apache.sshd.common.Property$BaseProperty<java.lang.String>

CLSS public static org.apache.sshd.common.Property$Validating<%0 extends java.lang.Object>
 outer org.apache.sshd.common.Property
cons public init(org.apache.sshd.common.Property<{org.apache.sshd.common.Property$Validating%0}>,java.util.function.Consumer<? super {org.apache.sshd.common.Property$Validating%0}>)
fld protected final java.util.function.Consumer<? super {org.apache.sshd.common.Property$Validating%0}> validator
fld protected final org.apache.sshd.common.Property<{org.apache.sshd.common.Property$Validating%0}> delegate
intf org.apache.sshd.common.Property<{org.apache.sshd.common.Property$Validating%0}>
meth public java.lang.Class<{org.apache.sshd.common.Property$Validating%0}> getType()
meth public java.lang.String getName()
meth public java.util.Optional<{org.apache.sshd.common.Property$Validating%0}> get(org.apache.sshd.common.PropertyResolver)
meth public java.util.Optional<{org.apache.sshd.common.Property$Validating%0}> getDefault()
meth public void remove(org.apache.sshd.common.PropertyResolver)
meth public void set(org.apache.sshd.common.PropertyResolver,{org.apache.sshd.common.Property$Validating%0})
meth public {org.apache.sshd.common.Property$Validating%0} getOrCustomDefault(org.apache.sshd.common.PropertyResolver,{org.apache.sshd.common.Property$Validating%0})
meth public {org.apache.sshd.common.Property$Validating%0} getRequiredDefault()
supr java.lang.Object

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

CLSS public final org.apache.sshd.common.PropertyResolverUtils
fld public final static java.lang.String NONE_VALUE = "none"
fld public final static java.util.NavigableSet<java.lang.String> FALSE_VALUES
fld public final static java.util.NavigableSet<java.lang.String> TRUE_VALUES
meth public static <%0 extends java.lang.Enum<{%%0}>> {%%0} toEnum(java.lang.Class<{%%0}>,java.lang.Object,boolean,java.util.Collection<{%%0}>)
meth public static boolean getBooleanProperty(java.util.Map<java.lang.String,?>,java.lang.String,boolean)
meth public static boolean getBooleanProperty(org.apache.sshd.common.PropertyResolver,java.lang.String,boolean)
meth public static boolean isNoneValue(java.lang.String)
meth public static boolean toBoolean(java.lang.Object,boolean)
meth public static int getIntProperty(java.util.Map<java.lang.String,?>,java.lang.String,int)
meth public static int getIntProperty(org.apache.sshd.common.PropertyResolver,java.lang.String,int)
meth public static int toInteger(java.lang.Object,int)
meth public static java.lang.Boolean getBoolean(java.util.Map<java.lang.String,?>,java.lang.String)
meth public static java.lang.Boolean getBoolean(org.apache.sshd.common.PropertyResolver,java.lang.String)
meth public static java.lang.Boolean parseBoolean(java.lang.String)
meth public static java.lang.Boolean toBoolean(java.lang.Object)
meth public static java.lang.Integer getInteger(java.util.Map<java.lang.String,?>,java.lang.String)
meth public static java.lang.Integer getInteger(org.apache.sshd.common.PropertyResolver,java.lang.String)
meth public static java.lang.Integer toInteger(java.lang.Object)
meth public static java.lang.Long getLong(java.util.Map<java.lang.String,?>,java.lang.String)
meth public static java.lang.Long getLong(org.apache.sshd.common.PropertyResolver,java.lang.String)
meth public static java.lang.Long toLong(java.lang.Object)
meth public static java.lang.Object getObject(java.util.Map<java.lang.String,?>,java.lang.String)
meth public static java.lang.Object getObject(org.apache.sshd.common.PropertyResolver,java.lang.String)
meth public static java.lang.Object getObject(org.apache.sshd.common.PropertyResolver,java.lang.String,java.lang.Object)
meth public static java.lang.Object resolvePropertyValue(java.util.Map<java.lang.String,?>,java.lang.String)
meth public static java.lang.Object resolvePropertyValue(org.apache.sshd.common.PropertyResolver,java.lang.String)
meth public static java.lang.Object updateProperty(java.util.Map<java.lang.String,java.lang.Object>,java.lang.String,boolean)
meth public static java.lang.Object updateProperty(java.util.Map<java.lang.String,java.lang.Object>,java.lang.String,int)
meth public static java.lang.Object updateProperty(java.util.Map<java.lang.String,java.lang.Object>,java.lang.String,java.lang.Object)
meth public static java.lang.Object updateProperty(java.util.Map<java.lang.String,java.lang.Object>,java.lang.String,long)
meth public static java.lang.Object updateProperty(org.apache.sshd.common.PropertyResolver,java.lang.String,boolean)
meth public static java.lang.Object updateProperty(org.apache.sshd.common.PropertyResolver,java.lang.String,int)
meth public static java.lang.Object updateProperty(org.apache.sshd.common.PropertyResolver,java.lang.String,java.lang.Object)
meth public static java.lang.Object updateProperty(org.apache.sshd.common.PropertyResolver,java.lang.String,long)
meth public static java.lang.String getString(java.util.Map<java.lang.String,?>,java.lang.String)
meth public static java.lang.String getString(org.apache.sshd.common.PropertyResolver,java.lang.String)
meth public static java.lang.String getStringProperty(java.util.Map<java.lang.String,?>,java.lang.String,java.lang.String)
meth public static java.lang.String getStringProperty(org.apache.sshd.common.PropertyResolver,java.lang.String,java.lang.String)
meth public static java.nio.charset.Charset getCharset(java.util.Map<java.lang.String,?>,java.lang.String,java.nio.charset.Charset)
meth public static java.nio.charset.Charset getCharset(org.apache.sshd.common.PropertyResolver,java.lang.String,java.nio.charset.Charset)
meth public static java.nio.charset.Charset toCharset(java.lang.Object)
meth public static java.util.Map<java.lang.String,java.lang.Object> resolvePropertiesSource(org.apache.sshd.common.PropertyResolver,java.lang.String)
meth public static long getLongProperty(java.util.Map<java.lang.String,?>,java.lang.String,long)
meth public static long getLongProperty(org.apache.sshd.common.PropertyResolver,java.lang.String,long)
meth public static long toLong(java.lang.Object,long)
meth public static org.apache.sshd.common.PropertyResolver toPropertyResolver(java.util.Map<java.lang.String,?>)
meth public static org.apache.sshd.common.PropertyResolver toPropertyResolver(java.util.Map<java.lang.String,?>,org.apache.sshd.common.PropertyResolver)
meth public static org.apache.sshd.common.PropertyResolver toPropertyResolver(java.util.Properties)
supr java.lang.Object

CLSS public org.apache.sshd.common.RuntimeSshException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public abstract interface org.apache.sshd.common.Service
intf org.apache.sshd.common.Closeable
intf org.apache.sshd.common.PropertyResolver
intf org.apache.sshd.common.session.SessionHolder<org.apache.sshd.common.session.Session>
meth public abstract void process(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public abstract void start()
meth public org.apache.sshd.common.PropertyResolver getParentPropertyResolver()

CLSS public abstract interface org.apache.sshd.common.ServiceFactory
intf org.apache.sshd.common.NamedResource
meth public abstract org.apache.sshd.common.Service create(org.apache.sshd.common.session.Session) throws java.io.IOException
meth public static org.apache.sshd.common.Service create(java.util.Collection<? extends org.apache.sshd.common.ServiceFactory>,java.lang.String,org.apache.sshd.common.session.Session) throws java.io.IOException

CLSS public final org.apache.sshd.common.SshConstants
fld public final static byte SSH_MSG_CHANNEL_CLOSE = 97
fld public final static byte SSH_MSG_CHANNEL_DATA = 94
fld public final static byte SSH_MSG_CHANNEL_EOF = 96
fld public final static byte SSH_MSG_CHANNEL_EXTENDED_DATA = 95
fld public final static byte SSH_MSG_CHANNEL_FAILURE = 100
fld public final static byte SSH_MSG_CHANNEL_OPEN = 90
fld public final static byte SSH_MSG_CHANNEL_OPEN_CONFIRMATION = 91
fld public final static byte SSH_MSG_CHANNEL_OPEN_FAILURE = 92
fld public final static byte SSH_MSG_CHANNEL_REQUEST = 98
fld public final static byte SSH_MSG_CHANNEL_SUCCESS = 99
fld public final static byte SSH_MSG_CHANNEL_WINDOW_ADJUST = 93
fld public final static byte SSH_MSG_DEBUG = 4
fld public final static byte SSH_MSG_DISCONNECT = 1
fld public final static byte SSH_MSG_GLOBAL_REQUEST = 80
fld public final static byte SSH_MSG_IGNORE = 2
fld public final static byte SSH_MSG_KEXDH_INIT = 30
fld public final static byte SSH_MSG_KEXDH_REPLY = 31
fld public final static byte SSH_MSG_KEXINIT = 20
fld public final static byte SSH_MSG_KEX_DH_GEX_GROUP = 31
fld public final static byte SSH_MSG_KEX_DH_GEX_INIT = 32
fld public final static byte SSH_MSG_KEX_DH_GEX_REPLY = 33
fld public final static byte SSH_MSG_KEX_DH_GEX_REQUEST = 34
fld public final static byte SSH_MSG_KEX_DH_GEX_REQUEST_OLD = 30
fld public final static byte SSH_MSG_KEX_FIRST = 30
fld public final static byte SSH_MSG_KEX_LAST = 49
fld public final static byte SSH_MSG_NEWKEYS = 21
fld public final static byte SSH_MSG_REQUEST_FAILURE = 82
fld public final static byte SSH_MSG_REQUEST_SUCCESS = 81
fld public final static byte SSH_MSG_SERVICE_ACCEPT = 6
fld public final static byte SSH_MSG_SERVICE_REQUEST = 5
fld public final static byte SSH_MSG_UNIMPLEMENTED = 3
fld public final static byte SSH_MSG_USERAUTH_BANNER = 53
fld public final static byte SSH_MSG_USERAUTH_FAILURE = 51
fld public final static byte SSH_MSG_USERAUTH_GSSAPI_MIC = 66
fld public final static byte SSH_MSG_USERAUTH_INFO_REQUEST = 60
fld public final static byte SSH_MSG_USERAUTH_INFO_RESPONSE = 61
fld public final static byte SSH_MSG_USERAUTH_PASSWD_CHANGEREQ = 60
fld public final static byte SSH_MSG_USERAUTH_PK_OK = 60
fld public final static byte SSH_MSG_USERAUTH_REQUEST = 50
fld public final static byte SSH_MSG_USERAUTH_SUCCESS = 52
fld public final static int DEFAULT_PORT = 22
fld public final static int MSG_KEX_COOKIE_SIZE = 16
fld public final static int SSH2_DISCONNECT_AUTH_CANCELLED_BY_USER = 13
fld public final static int SSH2_DISCONNECT_BY_APPLICATION = 11
fld public final static int SSH2_DISCONNECT_COMPRESSION_ERROR = 6
fld public final static int SSH2_DISCONNECT_CONNECTION_LOST = 10
fld public final static int SSH2_DISCONNECT_HOST_AUTHENTICATION_FAILED = 4
fld public final static int SSH2_DISCONNECT_HOST_KEY_NOT_VERIFIABLE = 9
fld public final static int SSH2_DISCONNECT_HOST_NOT_ALLOWED_TO_CONNECT = 1
fld public final static int SSH2_DISCONNECT_ILLEGAL_USER_NAME = 15
fld public final static int SSH2_DISCONNECT_KEY_EXCHANGE_FAILED = 3
fld public final static int SSH2_DISCONNECT_MAC_ERROR = 5
fld public final static int SSH2_DISCONNECT_NO_MORE_AUTH_METHODS_AVAILABLE = 14
fld public final static int SSH2_DISCONNECT_PROTOCOL_ERROR = 2
fld public final static int SSH2_DISCONNECT_PROTOCOL_VERSION_NOT_SUPPORTED = 8
fld public final static int SSH2_DISCONNECT_RESERVED = 4
fld public final static int SSH2_DISCONNECT_SERVICE_NOT_AVAILABLE = 7
fld public final static int SSH2_DISCONNECT_TOO_MANY_CONNECTIONS = 12
fld public final static int SSH_EXTENDED_DATA_STDERR = 1
fld public final static int SSH_OPEN_ADMINISTRATIVELY_PROHIBITED = 1
fld public final static int SSH_OPEN_CONNECT_FAILED = 2
fld public final static int SSH_OPEN_RESOURCE_SHORTAGE = 4
fld public final static int SSH_OPEN_UNKNOWN_CHANNEL_TYPE = 3
fld public final static int SSH_PACKET_HEADER_LEN = 5
fld public final static int SSH_REQUIRED_PAYLOAD_PACKET_LENGTH_SUPPORT = 32768
fld public final static int SSH_REQUIRED_TOTAL_PACKET_LENGTH_SUPPORT = 35000
fld public final static java.util.function.IntUnaryOperator TO_EFFECTIVE_PORT
meth public static boolean isAmbiguousOpcode(int)
meth public static java.lang.String getCommandMessageName(int)
meth public static java.lang.String getDisconnectReasonName(int)
meth public static java.lang.String getOpenErrorCodeName(int)
meth public static java.util.Set<java.lang.Integer> getAmbiguousOpcodes()
supr java.lang.Object
hcls LazyAmbiguousOpcodesHolder,LazyMessagesMapHolder,LazyOpenCodesMapHolder,LazyReasonsMapHolder

CLSS public org.apache.sshd.common.SshException
cons public init(int)
cons public init(int,java.lang.String)
cons public init(int,java.lang.String,java.lang.Throwable)
cons public init(int,java.lang.Throwable)
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public int getDisconnectCode()
supr java.io.IOException
hfds disconnectCode,serialVersionUID

CLSS public final org.apache.sshd.common.SyspropsMapWrapper
fld public final static java.lang.String SYSPROPS_MAPPED_PREFIX = "org.apache.sshd.config"
fld public final static org.apache.sshd.common.PropertyResolver RAW_PROPS_RESOLVER
fld public final static org.apache.sshd.common.PropertyResolver SYSPROPS_RESOLVER
fld public final static org.apache.sshd.common.SyspropsMapWrapper INSTANCE
intf java.util.Map<java.lang.String,java.lang.Object>
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean isEmpty()
meth public int size()
meth public java.lang.Object get(java.lang.Object)
meth public java.lang.Object put(java.lang.String,java.lang.Object)
meth public java.lang.Object remove(java.lang.Object)
meth public java.lang.String toString()
meth public java.util.Collection<java.lang.Object> values()
meth public java.util.Set<java.lang.String> keySet()
meth public java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.Object>> entrySet()
meth public static boolean isMappedSyspropKey(java.lang.String)
meth public static java.lang.String getMappedSyspropKey(java.lang.Object)
meth public static java.lang.String getUnmappedSyspropKey(java.lang.Object)
meth public void clear()
meth public void putAll(java.util.Map<? extends java.lang.String,?>)
supr java.lang.Object

CLSS public abstract org.apache.sshd.common.auth.AbstractUserAuthMethodFactory<%0 extends org.apache.sshd.common.session.SessionContext, %1 extends org.apache.sshd.common.auth.UserAuthInstance<{org.apache.sshd.common.auth.AbstractUserAuthMethodFactory%0}>>
cons protected init(java.lang.String)
intf org.apache.sshd.common.auth.UserAuthMethodFactory<{org.apache.sshd.common.auth.AbstractUserAuthMethodFactory%0},{org.apache.sshd.common.auth.AbstractUserAuthMethodFactory%1}>
meth public final java.lang.String getName()
meth public java.lang.String toString()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds name

CLSS public abstract org.apache.sshd.common.auth.AbstractUserAuthServiceFactory
cons protected init()
cons protected init(java.lang.String)
fld public final static java.lang.String DEFAULT_NAME = "ssh-userauth"
intf org.apache.sshd.common.ServiceFactory
meth public final java.lang.String getName()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds name

CLSS public org.apache.sshd.common.auth.BasicCredentialsImpl
cons public init()
cons public init(java.lang.String,java.lang.String)
intf java.lang.Cloneable
intf org.apache.sshd.common.auth.MutableBasicCredentials
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getPassword()
meth public java.lang.String getUsername()
meth public org.apache.sshd.common.auth.BasicCredentialsImpl clone()
meth public void setPassword(java.lang.String)
meth public void setUsername(java.lang.String)
supr java.lang.Object
hfds password,username

CLSS public abstract interface org.apache.sshd.common.auth.BasicCredentialsProvider
intf org.apache.sshd.common.auth.PasswordHolder
intf org.apache.sshd.common.auth.UsernameHolder

CLSS public abstract interface org.apache.sshd.common.auth.MutableBasicCredentials
intf org.apache.sshd.common.auth.BasicCredentialsProvider
intf org.apache.sshd.common.auth.MutablePassword
intf org.apache.sshd.common.auth.MutableUserHolder

CLSS public abstract interface org.apache.sshd.common.auth.MutablePassword
intf org.apache.sshd.common.auth.PasswordHolder
meth public abstract void setPassword(java.lang.String)

CLSS public abstract interface org.apache.sshd.common.auth.MutableUserHolder
intf org.apache.sshd.common.auth.UsernameHolder
meth public abstract void setUsername(java.lang.String)

CLSS public abstract interface org.apache.sshd.common.auth.PasswordHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract java.lang.String getPassword()

CLSS public abstract interface org.apache.sshd.common.auth.UserAuthFactoriesManager<%0 extends org.apache.sshd.common.session.SessionContext, %1 extends org.apache.sshd.common.auth.UserAuthInstance<{org.apache.sshd.common.auth.UserAuthFactoriesManager%0}>, %2 extends org.apache.sshd.common.auth.UserAuthMethodFactory<{org.apache.sshd.common.auth.UserAuthFactoriesManager%0},{org.apache.sshd.common.auth.UserAuthFactoriesManager%1}>>
meth public !varargs void setUserAuthFactoriesNames(java.lang.String[])
meth public abstract java.util.List<{org.apache.sshd.common.auth.UserAuthFactoriesManager%2}> getUserAuthFactories()
meth public abstract void setUserAuthFactories(java.util.List<{org.apache.sshd.common.auth.UserAuthFactoriesManager%2}>)
meth public abstract void setUserAuthFactoriesNames(java.util.Collection<java.lang.String>)
meth public java.lang.String getUserAuthFactoriesNameList()
meth public java.util.List<java.lang.String> getUserAuthFactoriesNames()
meth public void setUserAuthFactoriesNameList(java.lang.String)

CLSS public abstract interface org.apache.sshd.common.auth.UserAuthInstance<%0 extends org.apache.sshd.common.session.SessionContext>
intf org.apache.sshd.common.NamedResource
meth public abstract {org.apache.sshd.common.auth.UserAuthInstance%0} getSession()

CLSS public abstract interface org.apache.sshd.common.auth.UserAuthMethodFactory<%0 extends org.apache.sshd.common.session.SessionContext, %1 extends org.apache.sshd.common.auth.UserAuthInstance<{org.apache.sshd.common.auth.UserAuthMethodFactory%0}>>
fld public final static java.lang.String HOST_BASED = "hostbased"
fld public final static java.lang.String KB_INTERACTIVE = "keyboard-interactive"
fld public final static java.lang.String PASSWORD = "password"
fld public final static java.lang.String PUBLIC_KEY = "publickey"
intf org.apache.sshd.common.NamedResource
meth public abstract {org.apache.sshd.common.auth.UserAuthMethodFactory%1} createUserAuth({org.apache.sshd.common.auth.UserAuthMethodFactory%0}) throws java.io.IOException
meth public static <%0 extends org.apache.sshd.common.session.SessionContext, %1 extends org.apache.sshd.common.auth.UserAuthInstance<{%%0}>> {%%1} createUserAuth({%%0},java.util.Collection<? extends org.apache.sshd.common.auth.UserAuthMethodFactory<{%%0},{%%1}>>,java.lang.String) throws java.io.IOException
meth public static boolean isDataIntegrityAuthenticationTransport(org.apache.sshd.common.session.SessionContext)
meth public static boolean isSecureAuthenticationTransport(org.apache.sshd.common.session.SessionContext)

CLSS public abstract interface org.apache.sshd.common.auth.UsernameHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract java.lang.String getUsername()

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

CLSS public org.apache.sshd.common.channel.AbstractChannel$GracefulChannelCloseable
 outer org.apache.sshd.common.channel.AbstractChannel
cons public init(org.apache.sshd.common.channel.AbstractChannel)
meth protected void handleClosePacketWriteFailure(org.apache.sshd.common.channel.Channel,boolean,java.lang.Throwable)
meth protected void handleClosePacketWritten(org.apache.sshd.common.channel.Channel,boolean)
meth public boolean isClosed()
meth public boolean isClosing()
meth public java.lang.String toString()
meth public org.apache.sshd.common.future.CloseFuture close(boolean)
meth public void addCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
meth public void removeCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
meth public void setClosing(boolean)
supr org.apache.sshd.common.util.closeable.IoBaseCloseable
hfds closing

CLSS protected final static !enum org.apache.sshd.common.channel.AbstractChannel$GracefulState
 outer org.apache.sshd.common.channel.AbstractChannel
fld public final static org.apache.sshd.common.channel.AbstractChannel$GracefulState CloseReceived
fld public final static org.apache.sshd.common.channel.AbstractChannel$GracefulState CloseSent
fld public final static org.apache.sshd.common.channel.AbstractChannel$GracefulState Closed
fld public final static org.apache.sshd.common.channel.AbstractChannel$GracefulState Opened
meth public static org.apache.sshd.common.channel.AbstractChannel$GracefulState valueOf(java.lang.String)
meth public static org.apache.sshd.common.channel.AbstractChannel$GracefulState[] values()
supr java.lang.Enum<org.apache.sshd.common.channel.AbstractChannel$GracefulState>

CLSS public abstract interface static org.apache.sshd.common.channel.AbstractChannel$PacketValidator
 outer org.apache.sshd.common.channel.AbstractChannel
 anno 0 java.lang.FunctionalInterface()
meth public abstract boolean isValid(long,long,boolean)

CLSS public abstract org.apache.sshd.common.channel.AbstractChannelRequestHandler
cons protected init()
intf org.apache.sshd.common.channel.ChannelRequestHandler
supr org.apache.sshd.common.channel.AbstractRequestHandler<org.apache.sshd.common.channel.Channel>

CLSS public abstract org.apache.sshd.common.channel.AbstractRequestHandler<%0 extends java.lang.Object>
cons protected init()
intf org.apache.sshd.common.channel.RequestHandler<{org.apache.sshd.common.channel.AbstractRequestHandler%0}>
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public org.apache.sshd.common.channel.BufferedIoOutputStream
cons public init(java.lang.Object,long,org.apache.sshd.common.io.IoOutputStream,int,java.time.Duration)
cons public init(java.lang.Object,long,org.apache.sshd.common.io.IoOutputStream,org.apache.sshd.common.PropertyResolver)
fld protected final int maxPendingBytesCount
fld protected final java.lang.Object id
fld protected final java.time.Duration maxWaitForPendingWrites
fld protected final java.util.Queue<org.apache.sshd.common.channel.IoWriteFutureImpl> writes
fld protected final java.util.concurrent.atomic.AtomicInteger pendingBytesCount
fld protected final java.util.concurrent.atomic.AtomicLong writtenBytesCount
fld protected final java.util.concurrent.atomic.AtomicReference<org.apache.sshd.common.channel.IoWriteFutureImpl> currentWrite
fld protected final java.util.concurrent.atomic.AtomicReference<org.apache.sshd.common.channel.exception.SshChannelBufferedOutputException> pendingException
fld protected final long channelId
fld protected final org.apache.sshd.common.io.IoOutputStream out
intf org.apache.sshd.common.channel.ChannelIdentifier
intf org.apache.sshd.common.io.IoOutputStream
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected void finishWrite(org.apache.sshd.common.channel.IoWriteFutureImpl,int)
meth protected void startWriting() throws java.io.IOException
meth protected void waitForAvailableWriteSpace(int) throws java.io.IOException
meth public java.lang.Object getId()
meth public java.lang.String toString()
meth public long getChannelId()
meth public org.apache.sshd.common.io.IoWriteFuture writeBuffer(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr org.apache.sshd.common.util.closeable.AbstractInnerCloseable

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

CLSS public org.apache.sshd.common.channel.ChannelAsyncInputStream
cons public init(org.apache.sshd.common.channel.Channel)
innr public static IoReadFutureImpl
intf org.apache.sshd.common.channel.ChannelHolder
intf org.apache.sshd.common.io.IoInputStream
meth protected org.apache.sshd.common.future.CloseFuture doCloseGracefully()
meth protected void preClose()
meth public java.lang.String toString()
meth public org.apache.sshd.common.channel.Channel getChannel()
meth public org.apache.sshd.common.io.IoReadFuture read(org.apache.sshd.common.util.buffer.Buffer)
meth public void write(org.apache.sshd.common.util.Readable) throws java.io.IOException
supr org.apache.sshd.common.util.closeable.AbstractCloseable
hfds buffer,channelInstance,pending,readFutureId

CLSS public static org.apache.sshd.common.channel.ChannelAsyncInputStream$IoReadFutureImpl
 outer org.apache.sshd.common.channel.ChannelAsyncInputStream
cons public init(java.lang.Object,org.apache.sshd.common.util.buffer.Buffer)
intf org.apache.sshd.common.io.IoReadFuture
meth public !varargs org.apache.sshd.common.io.IoReadFuture verify(long,org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public int getRead()
meth public java.lang.Throwable getException()
meth public org.apache.sshd.common.util.buffer.Buffer getBuffer()
supr org.apache.sshd.common.future.DefaultVerifiableSshFuture<org.apache.sshd.common.io.IoReadFuture>
hfds buffer

CLSS public org.apache.sshd.common.channel.ChannelAsyncOutputStream
cons public init(org.apache.sshd.common.channel.Channel,byte)
fld protected final java.util.concurrent.atomic.AtomicReference<org.apache.sshd.common.io.IoWriteFuture> lastWrite
fld protected final org.apache.sshd.common.channel.ChannelAsyncOutputStream$WriteState writeState
innr protected static BufferedFuture
innr protected static WriteState
intf org.apache.sshd.common.channel.ChannelHolder
intf org.apache.sshd.common.io.IoOutputStream
meth protected org.apache.sshd.common.channel.IoWriteFutureImpl writePacket(org.apache.sshd.common.channel.IoWriteFutureImpl,boolean)
meth protected org.apache.sshd.common.future.CloseFuture doCloseGracefully()
meth protected org.apache.sshd.common.util.buffer.Buffer createSendBuffer(org.apache.sshd.common.util.buffer.Buffer,org.apache.sshd.common.channel.Channel,int)
meth protected void doCloseImmediately()
meth protected void doWriteIfPossible(boolean)
meth protected void onWritten(org.apache.sshd.common.channel.IoWriteFutureImpl,int,int,org.apache.sshd.common.io.IoWriteFuture)
meth protected void preClose()
meth protected void shutdown()
meth protected void terminateFuture(org.apache.sshd.common.channel.IoWriteFutureImpl)
meth public byte getCommandType()
meth public java.lang.String toString()
meth public org.apache.sshd.common.channel.Channel getChannel()
meth public org.apache.sshd.common.io.IoWriteFuture writeBuffer(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void onWindowExpanded() throws java.io.IOException
supr org.apache.sshd.common.util.closeable.AbstractCloseable
hfds channelInstance,cmd,packetWriteId,packetWriter

CLSS protected static org.apache.sshd.common.channel.ChannelAsyncOutputStream$BufferedFuture
 outer org.apache.sshd.common.channel.ChannelAsyncOutputStream
fld protected boolean waitOnWindow
supr org.apache.sshd.common.channel.IoWriteFutureImpl

CLSS protected static org.apache.sshd.common.channel.ChannelAsyncOutputStream$WriteState
 outer org.apache.sshd.common.channel.ChannelAsyncOutputStream
cons protected init()
fld protected boolean windowExpanded
fld protected boolean writeInProgress
fld protected int toSend
fld protected int totalLength
fld protected org.apache.sshd.common.channel.IoWriteFutureImpl pendingWrite
fld protected org.apache.sshd.common.util.closeable.AbstractCloseable$State openState
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.common.channel.ChannelFactory
intf org.apache.sshd.common.NamedResource
meth public abstract org.apache.sshd.common.channel.Channel createChannel(org.apache.sshd.common.session.Session) throws java.io.IOException
meth public static org.apache.sshd.common.channel.Channel createChannel(org.apache.sshd.common.session.Session,java.util.Collection<? extends org.apache.sshd.common.channel.ChannelFactory>,java.lang.String) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.common.channel.ChannelHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.common.channel.Channel getChannel()

CLSS public abstract interface org.apache.sshd.common.channel.ChannelIdentifier
 anno 0 java.lang.FunctionalInterface()
meth public abstract long getChannelId()

CLSS public abstract interface org.apache.sshd.common.channel.ChannelListener
fld public final static org.apache.sshd.common.channel.ChannelListener EMPTY
intf org.apache.sshd.common.util.SshdEventListener
meth public static <%0 extends org.apache.sshd.common.channel.ChannelListener> {%%0} validateListener({%%0})
meth public void channelClosed(org.apache.sshd.common.channel.Channel,java.lang.Throwable)
meth public void channelInitialized(org.apache.sshd.common.channel.Channel)
meth public void channelOpenFailure(org.apache.sshd.common.channel.Channel,java.lang.Throwable)
meth public void channelOpenSuccess(org.apache.sshd.common.channel.Channel)
meth public void channelStateChanged(org.apache.sshd.common.channel.Channel,java.lang.String)

CLSS public abstract interface org.apache.sshd.common.channel.ChannelListenerManager
meth public abstract org.apache.sshd.common.channel.ChannelListener getChannelListenerProxy()
meth public abstract void addChannelListener(org.apache.sshd.common.channel.ChannelListener)
meth public abstract void removeChannelListener(org.apache.sshd.common.channel.ChannelListener)

CLSS public org.apache.sshd.common.channel.ChannelOutputStream
cons public init(org.apache.sshd.common.channel.AbstractChannel,org.apache.sshd.common.channel.RemoteWindow,java.time.Duration,org.slf4j.Logger,byte,boolean)
cons public init(org.apache.sshd.common.channel.AbstractChannel,org.apache.sshd.common.channel.RemoteWindow,long,org.slf4j.Logger,byte,boolean)
cons public init(org.apache.sshd.common.channel.AbstractChannel,org.apache.sshd.common.channel.RemoteWindow,org.slf4j.Logger,byte,boolean)
fld protected final java.util.concurrent.atomic.AtomicReference<org.apache.sshd.common.channel.ChannelOutputStream$OpenState> openState
fld protected final org.slf4j.Logger log
innr protected final static !enum OpenState
innr protected final static !enum WriteState
intf java.nio.channels.Channel
intf org.apache.sshd.common.channel.ChannelHolder
meth protected org.apache.sshd.common.util.buffer.Buffer newBuffer(int)
meth public boolean isEofOnClose()
meth public boolean isNoDelay()
meth public boolean isOpen()
meth public byte getCommandType()
meth public java.lang.String toString()
meth public org.apache.sshd.common.channel.AbstractChannel getChannel()
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void setNoDelay(boolean)
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream
hfds buffer,bufferLength,bufferLock,channelInstance,cmd,eofOnClose,isFlushing,lastSize,maxWaitTimeout,noDelay,packetWriter,remoteWindow

CLSS protected final static !enum org.apache.sshd.common.channel.ChannelOutputStream$OpenState
 outer org.apache.sshd.common.channel.ChannelOutputStream
fld public final static org.apache.sshd.common.channel.ChannelOutputStream$OpenState CLOSED
fld public final static org.apache.sshd.common.channel.ChannelOutputStream$OpenState CLOSING
fld public final static org.apache.sshd.common.channel.ChannelOutputStream$OpenState OPEN
meth public static org.apache.sshd.common.channel.ChannelOutputStream$OpenState valueOf(java.lang.String)
meth public static org.apache.sshd.common.channel.ChannelOutputStream$OpenState[] values()
supr java.lang.Enum<org.apache.sshd.common.channel.ChannelOutputStream$OpenState>

CLSS protected final static !enum org.apache.sshd.common.channel.ChannelOutputStream$WriteState
 outer org.apache.sshd.common.channel.ChannelOutputStream
fld public final static org.apache.sshd.common.channel.ChannelOutputStream$WriteState BUFFERED
fld public final static org.apache.sshd.common.channel.ChannelOutputStream$WriteState NEED_FLUSH
fld public final static org.apache.sshd.common.channel.ChannelOutputStream$WriteState NEED_SPACE
meth public static org.apache.sshd.common.channel.ChannelOutputStream$WriteState valueOf(java.lang.String)
meth public static org.apache.sshd.common.channel.ChannelOutputStream$WriteState[] values()
supr java.lang.Enum<org.apache.sshd.common.channel.ChannelOutputStream$WriteState>

CLSS public org.apache.sshd.common.channel.ChannelPipedInputStream
cons public init(org.apache.sshd.common.PropertyResolver,org.apache.sshd.common.channel.LocalWindow)
cons public init(org.apache.sshd.common.channel.LocalWindow,java.time.Duration)
cons public init(org.apache.sshd.common.channel.LocalWindow,long)
intf org.apache.sshd.common.channel.ChannelPipedSink
meth public boolean isOpen()
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long getTimeout()
meth public void close() throws java.io.IOException
meth public void eof()
meth public void receive(byte[],int,int) throws java.io.IOException
meth public void setTimeout(long)
supr java.io.InputStream
hfds b,buffer,dataAvailable,localWindow,lock,open,timeout,writerClosed

CLSS public org.apache.sshd.common.channel.ChannelPipedOutputStream
cons public init(org.apache.sshd.common.channel.ChannelPipedSink)
intf java.nio.channels.Channel
meth public boolean isOpen()
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream
hfds b,closed,sink

CLSS public abstract interface org.apache.sshd.common.channel.ChannelPipedSink
intf java.nio.channels.Channel
meth public abstract void eof()
meth public abstract void receive(byte[],int,int) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.common.channel.ChannelRequestHandler
fld public final static java.util.function.Function<org.apache.sshd.common.channel.ChannelRequestHandler,org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.channel.Channel>> CHANN2HNDLR
intf org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.channel.Channel>
meth public abstract org.apache.sshd.common.channel.RequestHandler$Result process(org.apache.sshd.common.channel.Channel,java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception

CLSS public org.apache.sshd.common.channel.IoWriteFutureImpl
cons public init(java.lang.Object,org.apache.sshd.common.util.buffer.Buffer)
meth public org.apache.sshd.common.util.buffer.Buffer getBuffer()
supr org.apache.sshd.common.io.AbstractIoWriteFuture
hfds buffer

CLSS public org.apache.sshd.common.channel.LocalWindow
cons public init(org.apache.sshd.common.channel.AbstractChannel,boolean)
meth public org.apache.sshd.common.channel.AbstractChannel getChannel()
meth public void consume(long) throws java.io.IOException
meth public void init(org.apache.sshd.common.PropertyResolver)
meth public void release(long) throws java.io.IOException
supr org.apache.sshd.common.channel.Window
hfds adjustment,channel,released

CLSS public org.apache.sshd.common.channel.PtyChannelConfiguration
cons public init()
intf org.apache.sshd.common.channel.PtyChannelConfigurationMutator
meth public int getPtyColumns()
meth public int getPtyHeight()
meth public int getPtyLines()
meth public int getPtyWidth()
meth public java.lang.String getPtyType()
meth public java.lang.String toString()
meth public java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer> getPtyModes()
meth public void setPtyColumns(int)
meth public void setPtyHeight(int)
meth public void setPtyLines(int)
meth public void setPtyModes(java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer>)
meth public void setPtyType(java.lang.String)
meth public void setPtyWidth(int)
supr java.lang.Object
hfds ptyColumns,ptyHeight,ptyLines,ptyModes,ptyType,ptyWidth

CLSS public abstract interface org.apache.sshd.common.channel.PtyChannelConfigurationHolder
fld public final static int DEFAULT_COLUMNS_COUNT = 80
fld public final static int DEFAULT_HEIGHT = 480
fld public final static int DEFAULT_ROWS_COUNT = 24
fld public final static int DEFAULT_WIDTH = 640
fld public final static java.lang.String DUMMY_PTY_TYPE = "dummy"
fld public final static java.lang.String WINDOWS_PTY_TYPE = "windows"
fld public final static java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer> DEFAULT_PTY_MODES
meth public abstract int getPtyColumns()
meth public abstract int getPtyHeight()
meth public abstract int getPtyLines()
meth public abstract int getPtyWidth()
meth public abstract java.lang.String getPtyType()
meth public abstract java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer> getPtyModes()

CLSS public abstract interface org.apache.sshd.common.channel.PtyChannelConfigurationMutator
intf org.apache.sshd.common.channel.PtyChannelConfigurationHolder
meth public abstract void setPtyColumns(int)
meth public abstract void setPtyHeight(int)
meth public abstract void setPtyLines(int)
meth public abstract void setPtyModes(java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer>)
meth public abstract void setPtyType(java.lang.String)
meth public abstract void setPtyWidth(int)
meth public static <%0 extends org.apache.sshd.common.channel.PtyChannelConfigurationMutator> {%%0} copyConfiguration(org.apache.sshd.common.channel.PtyChannelConfigurationHolder,{%%0})
meth public static <%0 extends org.apache.sshd.common.channel.PtyChannelConfigurationMutator> {%%0} setupSensitiveDefaultPtyConfiguration({%%0}) throws java.io.IOException,java.lang.InterruptedException

CLSS public final !enum org.apache.sshd.common.channel.PtyMode
fld public final static byte TTY_OP_END = 0
fld public final static java.lang.Integer FALSE_SETTING
fld public final static java.lang.Integer TRUE_SETTING
fld public final static java.util.Comparator<org.apache.sshd.common.channel.PtyMode> BY_OPCODE
fld public final static java.util.NavigableMap<java.lang.Integer,org.apache.sshd.common.channel.PtyMode> COMMANDS
fld public final static java.util.Set<org.apache.sshd.common.channel.PtyMode> MODES
fld public final static java.util.function.ToIntFunction<org.apache.sshd.common.channel.PtyMode> OPCODE_EXTRACTOR
fld public final static org.apache.sshd.common.channel.PtyMode CS7
fld public final static org.apache.sshd.common.channel.PtyMode CS8
fld public final static org.apache.sshd.common.channel.PtyMode ECHO
fld public final static org.apache.sshd.common.channel.PtyMode ECHOCTL
fld public final static org.apache.sshd.common.channel.PtyMode ECHOE
fld public final static org.apache.sshd.common.channel.PtyMode ECHOK
fld public final static org.apache.sshd.common.channel.PtyMode ECHOKE
fld public final static org.apache.sshd.common.channel.PtyMode ECHONL
fld public final static org.apache.sshd.common.channel.PtyMode ICANON
fld public final static org.apache.sshd.common.channel.PtyMode ICRNL
fld public final static org.apache.sshd.common.channel.PtyMode IEXTEN
fld public final static org.apache.sshd.common.channel.PtyMode IGNCR
fld public final static org.apache.sshd.common.channel.PtyMode IGNPAR
fld public final static org.apache.sshd.common.channel.PtyMode IMAXBEL
fld public final static org.apache.sshd.common.channel.PtyMode INLCR
fld public final static org.apache.sshd.common.channel.PtyMode INPCK
fld public final static org.apache.sshd.common.channel.PtyMode ISIG
fld public final static org.apache.sshd.common.channel.PtyMode ISTRIP
fld public final static org.apache.sshd.common.channel.PtyMode IUCLC
fld public final static org.apache.sshd.common.channel.PtyMode IUTF8
fld public final static org.apache.sshd.common.channel.PtyMode IXANY
fld public final static org.apache.sshd.common.channel.PtyMode IXOFF
fld public final static org.apache.sshd.common.channel.PtyMode IXON
fld public final static org.apache.sshd.common.channel.PtyMode NOFLSH
fld public final static org.apache.sshd.common.channel.PtyMode OCRNL
fld public final static org.apache.sshd.common.channel.PtyMode OLCUC
fld public final static org.apache.sshd.common.channel.PtyMode ONLCR
fld public final static org.apache.sshd.common.channel.PtyMode ONLRET
fld public final static org.apache.sshd.common.channel.PtyMode ONOCR
fld public final static org.apache.sshd.common.channel.PtyMode OPOST
fld public final static org.apache.sshd.common.channel.PtyMode PARENB
fld public final static org.apache.sshd.common.channel.PtyMode PARMRK
fld public final static org.apache.sshd.common.channel.PtyMode PARODD
fld public final static org.apache.sshd.common.channel.PtyMode PENDIN
fld public final static org.apache.sshd.common.channel.PtyMode TOSTOP
fld public final static org.apache.sshd.common.channel.PtyMode TTY_OP_ISPEED
fld public final static org.apache.sshd.common.channel.PtyMode TTY_OP_OSPEED
fld public final static org.apache.sshd.common.channel.PtyMode VDISCARD
fld public final static org.apache.sshd.common.channel.PtyMode VDSUSP
fld public final static org.apache.sshd.common.channel.PtyMode VEOF
fld public final static org.apache.sshd.common.channel.PtyMode VEOL
fld public final static org.apache.sshd.common.channel.PtyMode VEOL2
fld public final static org.apache.sshd.common.channel.PtyMode VERASE
fld public final static org.apache.sshd.common.channel.PtyMode VFLUSH
fld public final static org.apache.sshd.common.channel.PtyMode VINTR
fld public final static org.apache.sshd.common.channel.PtyMode VKILL
fld public final static org.apache.sshd.common.channel.PtyMode VLNEXT
fld public final static org.apache.sshd.common.channel.PtyMode VQUIT
fld public final static org.apache.sshd.common.channel.PtyMode VREPRINT
fld public final static org.apache.sshd.common.channel.PtyMode VSTART
fld public final static org.apache.sshd.common.channel.PtyMode VSTATUS
fld public final static org.apache.sshd.common.channel.PtyMode VSTOP
fld public final static org.apache.sshd.common.channel.PtyMode VSUSP
fld public final static org.apache.sshd.common.channel.PtyMode VSWTCH
fld public final static org.apache.sshd.common.channel.PtyMode VWERASE
fld public final static org.apache.sshd.common.channel.PtyMode XCASE
meth public !varargs static java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer> createEnabledOptions(org.apache.sshd.common.channel.PtyMode[])
meth public !varargs static java.util.Set<org.apache.sshd.common.channel.PtyMode> resolveEnabledOptions(java.util.Map<org.apache.sshd.common.channel.PtyMode,?>,org.apache.sshd.common.channel.PtyMode[])
meth public int toInt()
meth public static boolean getBooleanSettingValue(int)
meth public static boolean getBooleanSettingValue(java.lang.Object)
meth public static boolean getBooleanSettingValue(java.util.Map<org.apache.sshd.common.channel.PtyMode,?>,java.util.Collection<org.apache.sshd.common.channel.PtyMode>,boolean)
meth public static boolean getBooleanSettingValue(java.util.Map<org.apache.sshd.common.channel.PtyMode,?>,org.apache.sshd.common.channel.PtyMode)
meth public static boolean isCharSetting(org.apache.sshd.common.channel.PtyMode)
meth public static java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer> createEnabledOptions(java.util.Collection<org.apache.sshd.common.channel.PtyMode>)
meth public static java.util.Set<org.apache.sshd.common.channel.PtyMode> resolveEnabledOptions(java.util.Map<org.apache.sshd.common.channel.PtyMode,?>,java.util.Collection<org.apache.sshd.common.channel.PtyMode>)
meth public static org.apache.sshd.common.channel.PtyMode fromInt(int)
meth public static org.apache.sshd.common.channel.PtyMode fromName(java.lang.String)
meth public static org.apache.sshd.common.channel.PtyMode valueOf(java.lang.String)
meth public static org.apache.sshd.common.channel.PtyMode[] values()
supr java.lang.Enum<org.apache.sshd.common.channel.PtyMode>
hfds v

CLSS public org.apache.sshd.common.channel.RemoteWindow
cons public init(org.apache.sshd.common.channel.Channel,boolean)
meth protected void waitForCondition(java.util.function.Predicate<? super org.apache.sshd.common.channel.Window>,java.time.Duration) throws java.lang.InterruptedException,java.net.SocketTimeoutException,org.apache.sshd.common.channel.WindowClosedException
meth public long waitForSpace(java.time.Duration) throws java.lang.InterruptedException,java.net.SocketTimeoutException,org.apache.sshd.common.channel.WindowClosedException
meth public long waitForSpace(long) throws java.lang.InterruptedException,java.net.SocketTimeoutException,org.apache.sshd.common.channel.WindowClosedException
meth public void consume(long)
meth public void expand(long)
meth public void init(long,long,org.apache.sshd.common.PropertyResolver)
meth public void waitAndConsume(long,java.time.Duration) throws java.lang.InterruptedException,java.net.SocketTimeoutException,org.apache.sshd.common.channel.WindowClosedException
meth public void waitAndConsume(long,long) throws java.lang.InterruptedException,java.net.SocketTimeoutException,org.apache.sshd.common.channel.WindowClosedException
supr org.apache.sshd.common.channel.Window
hfds SPACE_AVAILABLE_PREDICATE

CLSS public abstract interface org.apache.sshd.common.channel.RequestHandler<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
innr public final static !enum Result
meth public abstract org.apache.sshd.common.channel.RequestHandler$Result process({org.apache.sshd.common.channel.RequestHandler%0},java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception

CLSS public final static !enum org.apache.sshd.common.channel.RequestHandler$Result
 outer org.apache.sshd.common.channel.RequestHandler
fld public final static java.util.Set<org.apache.sshd.common.channel.RequestHandler$Result> VALUES
fld public final static org.apache.sshd.common.channel.RequestHandler$Result Replied
fld public final static org.apache.sshd.common.channel.RequestHandler$Result ReplyFailure
fld public final static org.apache.sshd.common.channel.RequestHandler$Result ReplySuccess
fld public final static org.apache.sshd.common.channel.RequestHandler$Result Unsupported
meth public static org.apache.sshd.common.channel.RequestHandler$Result fromName(java.lang.String)
meth public static org.apache.sshd.common.channel.RequestHandler$Result valueOf(java.lang.String)
meth public static org.apache.sshd.common.channel.RequestHandler$Result[] values()
supr java.lang.Enum<org.apache.sshd.common.channel.RequestHandler$Result>

CLSS public org.apache.sshd.common.channel.SimpleIoOutputStream
cons public init(org.apache.sshd.common.channel.ChannelOutputStream)
fld protected final org.apache.sshd.common.channel.ChannelOutputStream os
innr protected static DefaultIoWriteFuture
intf org.apache.sshd.common.io.IoOutputStream
meth protected void doCloseImmediately()
meth public org.apache.sshd.common.io.IoWriteFuture writeBuffer(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr org.apache.sshd.common.util.closeable.AbstractCloseable

CLSS protected static org.apache.sshd.common.channel.SimpleIoOutputStream$DefaultIoWriteFuture
 outer org.apache.sshd.common.channel.SimpleIoOutputStream
cons public init(java.lang.Object,java.lang.Object)
supr org.apache.sshd.common.io.AbstractIoWriteFuture

CLSS public abstract interface org.apache.sshd.common.channel.StreamingChannel
innr public final static !enum Streaming
meth public abstract org.apache.sshd.common.channel.StreamingChannel$Streaming getStreaming()
meth public abstract void setStreaming(org.apache.sshd.common.channel.StreamingChannel$Streaming)

CLSS public final static !enum org.apache.sshd.common.channel.StreamingChannel$Streaming
 outer org.apache.sshd.common.channel.StreamingChannel
fld public final static org.apache.sshd.common.channel.StreamingChannel$Streaming Async
fld public final static org.apache.sshd.common.channel.StreamingChannel$Streaming Sync
meth public static org.apache.sshd.common.channel.StreamingChannel$Streaming valueOf(java.lang.String)
meth public static org.apache.sshd.common.channel.StreamingChannel$Streaming[] values()
supr java.lang.Enum<org.apache.sshd.common.channel.StreamingChannel$Streaming>

CLSS public final org.apache.sshd.common.channel.SttySupport
fld public final static int DEFAULT_TERMINAL_HEIGHT = 24
fld public final static int DEFAULT_TERMINAL_WIDTH = 80
fld public final static java.lang.String DEFAULT_SSHD_STTY_COMMAND = "stty"
fld public final static java.lang.String SSHD_STTY_COMMAND_PROP = "sshd.sttyCommand"
meth public static int getTerminalHeight()
meth public static int getTerminalProperty(java.lang.String) throws java.io.IOException,java.lang.InterruptedException
meth public static int getTerminalWidth()
meth public static java.lang.String exec(java.lang.String) throws java.io.IOException,java.lang.InterruptedException
meth public static java.lang.String getSttyCommand()
meth public static java.lang.String getTtyProps() throws java.io.IOException,java.lang.InterruptedException
meth public static java.lang.String stty(java.lang.String) throws java.io.IOException,java.lang.InterruptedException
meth public static java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer> getUnixPtyModes() throws java.io.IOException,java.lang.InterruptedException
meth public static java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer> parsePtyModes(java.lang.String)
meth public static void setSttyCommand(java.lang.String)
supr java.lang.Object
hfds STTY_COMMAND_HOLDER,TTY_PROPS_HOLDER,TTY_PROPS_LAST_FETCHED_HOLDER

CLSS public abstract org.apache.sshd.common.channel.Window
cons protected init(org.apache.sshd.common.channel.Channel,boolean)
fld protected final java.lang.Object lock
intf java.io.Closeable
intf org.apache.sshd.common.channel.ChannelHolder
meth protected static java.util.function.Predicate<org.apache.sshd.common.channel.Window> largerThan(long)
meth protected void checkInitialized(java.lang.String)
meth protected void init(long,long,org.apache.sshd.common.PropertyResolver)
meth protected void updateSize(long)
meth public abstract void consume(long) throws java.io.IOException
meth public boolean isOpen()
meth public java.lang.String toString()
meth public long getMaxSize()
meth public long getPacketSize()
meth public long getSize()
meth public org.apache.sshd.common.channel.Channel getChannel()
meth public void close() throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds channelInstance,closed,initialized,maxSize,packetSize,size,suffix

CLSS public org.apache.sshd.common.channel.WindowClosedException
cons public init(java.lang.String)
supr org.apache.sshd.common.SshException
hfds serialVersionUID

CLSS public org.apache.sshd.common.channel.exception.SshChannelBufferedOutputException
cons public init(long,java.lang.String)
cons public init(long,java.lang.String,java.lang.Throwable)
cons public init(long,java.lang.Throwable)
supr org.apache.sshd.common.channel.exception.SshChannelException
hfds serialVersionUID

CLSS public org.apache.sshd.common.channel.exception.SshChannelClosedException
cons public init(long,java.lang.String)
cons public init(long,java.lang.String,java.lang.Throwable)
cons public init(long,java.lang.Throwable)
supr org.apache.sshd.common.channel.exception.SshChannelException
hfds serialVersionUID

CLSS public abstract org.apache.sshd.common.channel.exception.SshChannelException
cons protected init(long,java.lang.String)
cons protected init(long,java.lang.String,java.lang.Throwable)
cons protected init(long,java.lang.Throwable)
intf org.apache.sshd.common.channel.ChannelIdentifier
meth public long getChannelId()
supr java.io.IOException
hfds channelId,serialVersionUID

CLSS public org.apache.sshd.common.channel.exception.SshChannelInvalidPacketException
cons public init(long,java.lang.String)
cons public init(long,java.lang.String,java.lang.Throwable)
cons public init(long,java.lang.Throwable)
supr org.apache.sshd.common.channel.exception.SshChannelException
hfds serialVersionUID

CLSS public org.apache.sshd.common.channel.exception.SshChannelNotFoundException
cons public init(long,java.lang.String)
cons public init(long,java.lang.String,java.lang.Throwable)
cons public init(long,java.lang.Throwable)
supr org.apache.sshd.common.channel.exception.SshChannelException
hfds serialVersionUID

CLSS public org.apache.sshd.common.channel.exception.SshChannelOpenException
cons public init(long,int,java.lang.String)
cons public init(long,int,java.lang.String,java.lang.Throwable)
meth public int getReasonCode()
supr org.apache.sshd.common.channel.exception.SshChannelException
hfds code,serialVersionUID

CLSS public abstract interface org.apache.sshd.common.channel.throttle.ChannelStreamWriter
intf java.nio.channels.Channel
meth public abstract org.apache.sshd.common.io.IoWriteFuture writeData(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException

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

CLSS public org.apache.sshd.common.channel.throttle.DefaultChannelStreamWriter
cons public init(org.apache.sshd.common.channel.Channel)
fld protected final org.apache.sshd.common.channel.Channel channel
fld protected volatile boolean closed
intf org.apache.sshd.common.channel.throttle.ChannelStreamWriter
meth public boolean isOpen()
meth public org.apache.sshd.common.io.IoWriteFuture writeData(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void close() throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.sshd.common.cipher.BaseCBCCipher
cons public init(int,int,int,java.lang.String,int,java.lang.String,int)
meth protected java.security.spec.AlgorithmParameterSpec determineNewParameters(byte[],int,int)
meth public void update(byte[],int,int) throws java.lang.Exception
supr org.apache.sshd.common.cipher.BaseCipher
hfds lastEncryptedBlock

CLSS public org.apache.sshd.common.cipher.BaseCTRCipher
cons public init(int,int,int,java.lang.String,int,java.lang.String,int)
meth protected java.security.spec.AlgorithmParameterSpec determineNewParameters(byte[],int,int)
meth protected void reInit(byte[],int,int) throws java.security.InvalidAlgorithmParameterException,java.security.InvalidKeyException
meth public void update(byte[],int,int) throws java.lang.Exception
supr org.apache.sshd.common.cipher.BaseCipher
hfds blocksProcessed

CLSS public org.apache.sshd.common.cipher.BaseCipher
cons public init(int,int,int,java.lang.String,int,java.lang.String,int)
fld protected org.apache.sshd.common.cipher.Cipher$Mode mode
intf org.apache.sshd.common.cipher.Cipher
meth protected byte[] initializeIVData(org.apache.sshd.common.cipher.Cipher$Mode,byte[],int)
meth protected byte[] initializeKeyData(org.apache.sshd.common.cipher.Cipher$Mode,byte[],int)
meth protected java.security.spec.AlgorithmParameterSpec determineNewParameters(byte[],int,int)
meth protected javax.crypto.Cipher createCipherInstance(org.apache.sshd.common.cipher.Cipher$Mode,byte[],byte[]) throws java.lang.Exception
meth protected javax.crypto.Cipher getCipherInstance()
meth protected static byte[] resize(byte[],int)
meth protected void reInit(byte[],int,int) throws java.security.InvalidAlgorithmParameterException,java.security.InvalidKeyException
meth public int getAuthenticationTagSize()
meth public int getCipherBlockSize()
meth public int getIVSize()
meth public int getKdfSize()
meth public int getKeySize()
meth public java.lang.String getAlgorithm()
meth public java.lang.String getTransformation()
meth public java.lang.String toString()
meth public void init(org.apache.sshd.common.cipher.Cipher$Mode,byte[],byte[]) throws java.lang.Exception
meth public void update(byte[],int,int) throws java.lang.Exception
meth public void updateAAD(byte[],int,int) throws java.lang.Exception
supr java.lang.Object
hfds algorithm,alwaysReInit,authSize,blkSize,cipher,factory,ivsize,kdfSize,keySize,s,secretKey,transformation
hcls CipherFactory

CLSS public org.apache.sshd.common.cipher.BaseGCMCipher
cons public init(int,int,int,java.lang.String,int,java.lang.String,int)
fld protected boolean initialized
fld protected javax.crypto.SecretKey secretKey
fld protected org.apache.sshd.common.cipher.BaseGCMCipher$CounterGCMParameterSpec parameters
fld protected org.apache.sshd.common.cipher.Cipher$Mode mode
innr protected static CounterGCMParameterSpec
meth protected javax.crypto.Cipher createCipherInstance(org.apache.sshd.common.cipher.Cipher$Mode,byte[],byte[]) throws java.lang.Exception
meth protected javax.crypto.Cipher getInitializedCipherInstance() throws java.lang.Exception
meth public void update(byte[],int,int) throws java.lang.Exception
meth public void updateAAD(byte[],int,int) throws java.lang.Exception
supr org.apache.sshd.common.cipher.BaseCipher

CLSS protected static org.apache.sshd.common.cipher.BaseGCMCipher$CounterGCMParameterSpec
 outer org.apache.sshd.common.cipher.BaseGCMCipher
cons protected init(int,byte[])
fld protected final byte[] iv
fld protected final long initialCounter
meth protected void incrementCounter()
meth public byte[] getIV()
supr javax.crypto.spec.GCMParameterSpec

CLSS public org.apache.sshd.common.cipher.BaseRC4Cipher
cons public init(int,int,int,int)
fld public final static int SKIP_SIZE = 1536
meth protected byte[] initializeIVData(org.apache.sshd.common.cipher.Cipher$Mode,byte[],int)
meth protected javax.crypto.Cipher createCipherInstance(org.apache.sshd.common.cipher.Cipher$Mode,byte[],byte[]) throws java.lang.Exception
meth public void update(byte[],int,int) throws java.lang.Exception
supr org.apache.sshd.common.cipher.BaseCipher

CLSS public !enum org.apache.sshd.common.cipher.BuiltinCiphers
fld public final static java.util.Set<org.apache.sshd.common.cipher.BuiltinCiphers> VALUES
fld public final static org.apache.sshd.common.cipher.BuiltinCiphers aes128cbc
fld public final static org.apache.sshd.common.cipher.BuiltinCiphers aes128ctr
fld public final static org.apache.sshd.common.cipher.BuiltinCiphers aes128gcm
fld public final static org.apache.sshd.common.cipher.BuiltinCiphers aes192cbc
fld public final static org.apache.sshd.common.cipher.BuiltinCiphers aes192ctr
fld public final static org.apache.sshd.common.cipher.BuiltinCiphers aes256cbc
fld public final static org.apache.sshd.common.cipher.BuiltinCiphers aes256ctr
fld public final static org.apache.sshd.common.cipher.BuiltinCiphers aes256gcm
fld public final static org.apache.sshd.common.cipher.BuiltinCiphers arcfour128
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.cipher.BuiltinCiphers arcfour256
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.cipher.BuiltinCiphers blowfishcbc
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.cipher.BuiltinCiphers cc20p1305_openssh
fld public final static org.apache.sshd.common.cipher.BuiltinCiphers none
fld public final static org.apache.sshd.common.cipher.BuiltinCiphers tripledescbc
 anno 0 java.lang.Deprecated()
innr public final static Constants
innr public static ParseResult
intf org.apache.sshd.common.cipher.CipherFactory
meth public !varargs static org.apache.sshd.common.cipher.BuiltinCiphers$ParseResult parseCiphersList(java.lang.String[])
meth public boolean isSupported()
meth public final java.lang.String getName()
meth public final java.lang.String toString()
meth public int getAuthenticationTagSize()
meth public int getCipherBlockSize()
meth public int getIVSize()
meth public int getKdfSize()
meth public int getKeySize()
meth public java.lang.String getAlgorithm()
meth public java.lang.String getTransformation()
meth public org.apache.sshd.common.cipher.Cipher create()
meth public static java.util.NavigableSet<org.apache.sshd.common.cipher.CipherFactory> getRegisteredExtensions()
meth public static org.apache.sshd.common.NamedFactory<org.apache.sshd.common.cipher.Cipher> unregisterExtension(java.lang.String)
meth public static org.apache.sshd.common.cipher.BuiltinCiphers fromFactory(org.apache.sshd.common.NamedFactory<org.apache.sshd.common.cipher.Cipher>)
meth public static org.apache.sshd.common.cipher.BuiltinCiphers fromFactoryName(java.lang.String)
meth public static org.apache.sshd.common.cipher.BuiltinCiphers fromString(java.lang.String)
meth public static org.apache.sshd.common.cipher.BuiltinCiphers valueOf(java.lang.String)
meth public static org.apache.sshd.common.cipher.BuiltinCiphers$ParseResult parseCiphersList(java.lang.String)
meth public static org.apache.sshd.common.cipher.BuiltinCiphers$ParseResult parseCiphersList(java.util.Collection<java.lang.String>)
meth public static org.apache.sshd.common.cipher.BuiltinCiphers[] values()
meth public static org.apache.sshd.common.cipher.CipherFactory resolveFactory(java.lang.String)
meth public static void registerExtension(org.apache.sshd.common.cipher.CipherFactory)
supr java.lang.Enum<org.apache.sshd.common.cipher.BuiltinCiphers>
hfds EXTENSIONS,algorithm,authSize,blkSize,factoryName,ivsize,keySize,supported,transformation

CLSS public final static org.apache.sshd.common.cipher.BuiltinCiphers$Constants
 outer org.apache.sshd.common.cipher.BuiltinCiphers
fld public final static java.lang.String AES128_CBC = "aes128-cbc"
fld public final static java.lang.String AES128_CTR = "aes128-ctr"
fld public final static java.lang.String AES128_GCM = "aes128-gcm@openssh.com"
fld public final static java.lang.String AES192_CBC = "aes192-cbc"
fld public final static java.lang.String AES192_CTR = "aes192-ctr"
fld public final static java.lang.String AES256_CBC = "aes256-cbc"
fld public final static java.lang.String AES256_CTR = "aes256-ctr"
fld public final static java.lang.String AES256_GCM = "aes256-gcm@openssh.com"
fld public final static java.lang.String ARCFOUR128 = "arcfour128"
fld public final static java.lang.String ARCFOUR256 = "arcfour256"
fld public final static java.lang.String BLOWFISH_CBC = "blowfish-cbc"
fld public final static java.lang.String CC20P1305_OPENSSH = "chacha20-poly1305@openssh.com"
fld public final static java.lang.String NONE = "none"
fld public final static java.lang.String TRIPLE_DES_CBC = "3des-cbc"
fld public final static java.util.regex.Pattern NONE_CIPHER_PATTERN
meth public static boolean isNoneCipherIncluded(java.lang.String)
supr java.lang.Object

CLSS public static org.apache.sshd.common.cipher.BuiltinCiphers$ParseResult
 outer org.apache.sshd.common.cipher.BuiltinCiphers
cons public init(java.util.List<org.apache.sshd.common.cipher.CipherFactory>,java.util.List<java.lang.String>)
fld public final static org.apache.sshd.common.cipher.BuiltinCiphers$ParseResult EMPTY
supr org.apache.sshd.common.config.NamedFactoriesListParseResult<org.apache.sshd.common.cipher.Cipher,org.apache.sshd.common.cipher.CipherFactory>

CLSS public org.apache.sshd.common.cipher.ChaCha20Cipher
cons public init()
fld protected final org.apache.sshd.common.cipher.ChaCha20Cipher$ChaChaEngine bodyEngine
fld protected final org.apache.sshd.common.cipher.ChaCha20Cipher$ChaChaEngine headerEngine
fld protected final org.apache.sshd.common.mac.Mac mac
fld protected org.apache.sshd.common.cipher.Cipher$Mode mode
innr protected static ChaChaEngine
intf org.apache.sshd.common.cipher.Cipher
meth public int getAuthenticationTagSize()
meth public int getCipherBlockSize()
meth public int getIVSize()
meth public int getKdfSize()
meth public int getKeySize()
meth public java.lang.String getAlgorithm()
meth public java.lang.String getTransformation()
meth public java.lang.String toString()
meth public void init(org.apache.sshd.common.cipher.Cipher$Mode,byte[],byte[]) throws java.lang.Exception
meth public void update(byte[],int,int) throws java.lang.Exception
meth public void updateAAD(byte[],int,int) throws java.lang.Exception
supr java.lang.Object

CLSS protected static org.apache.sshd.common.cipher.ChaCha20Cipher$ChaChaEngine
 outer org.apache.sshd.common.cipher.ChaCha20Cipher
cons protected init()
fld protected final byte[] keyStream
fld protected final byte[] nonce
fld protected final int[] engineState
fld protected long initialNonce
fld protected long nonceVal
meth protected byte[] polyKey()
meth protected void advanceNonce()
meth protected void crypt(byte[],int,int,byte[],int)
meth protected void initCounter(long)
meth protected void initKey(byte[])
meth protected void initNonce(byte[])
meth protected void setKeyStream(int[])
supr java.lang.Object
hfds BLOCK_BYTES,BLOCK_INTS,COUNTER_OFFSET,ENGINE_STATE_HEADER,KEY_BYTES,KEY_INTS,KEY_OFFSET,NONCE_OFFSET

CLSS public abstract interface org.apache.sshd.common.cipher.Cipher
innr public final static !enum Mode
intf org.apache.sshd.common.cipher.CipherInformation
meth public abstract void init(org.apache.sshd.common.cipher.Cipher$Mode,byte[],byte[]) throws java.lang.Exception
meth public abstract void update(byte[],int,int) throws java.lang.Exception
meth public abstract void updateAAD(byte[],int,int) throws java.lang.Exception
meth public static boolean checkSupported(java.lang.String,int)
meth public void update(byte[]) throws java.lang.Exception
meth public void updateAAD(byte[]) throws java.lang.Exception
meth public void updateWithAAD(byte[],int,int,int) throws java.lang.Exception

CLSS public final static !enum org.apache.sshd.common.cipher.Cipher$Mode
 outer org.apache.sshd.common.cipher.Cipher
fld public final static org.apache.sshd.common.cipher.Cipher$Mode Decrypt
fld public final static org.apache.sshd.common.cipher.Cipher$Mode Encrypt
meth public static org.apache.sshd.common.cipher.Cipher$Mode valueOf(java.lang.String)
meth public static org.apache.sshd.common.cipher.Cipher$Mode[] values()
supr java.lang.Enum<org.apache.sshd.common.cipher.Cipher$Mode>

CLSS public abstract interface org.apache.sshd.common.cipher.CipherFactory
intf org.apache.sshd.common.BuiltinFactory<org.apache.sshd.common.cipher.Cipher>
intf org.apache.sshd.common.cipher.CipherInformation

CLSS public abstract interface org.apache.sshd.common.cipher.CipherInformation
intf org.apache.sshd.common.AlgorithmNameProvider
intf org.apache.sshd.common.keyprovider.KeySizeIndicator
meth public abstract int getAuthenticationTagSize()
meth public abstract int getCipherBlockSize()
meth public abstract int getIVSize()
meth public abstract int getKdfSize()
meth public abstract java.lang.String getTransformation()

CLSS public org.apache.sshd.common.cipher.CipherNone
cons public init()
intf org.apache.sshd.common.cipher.Cipher
meth public int getAuthenticationTagSize()
meth public int getCipherBlockSize()
meth public int getIVSize()
meth public int getKdfSize()
meth public int getKeySize()
meth public java.lang.String getAlgorithm()
meth public java.lang.String getTransformation()
meth public void init(org.apache.sshd.common.cipher.Cipher$Mode,byte[],byte[]) throws java.lang.Exception
meth public void update(byte[],int,int) throws java.lang.Exception
meth public void updateAAD(byte[],int,int) throws java.lang.Exception
supr java.lang.Object

CLSS public final !enum org.apache.sshd.common.cipher.ECCurves
fld public final static java.util.Comparator<org.apache.sshd.common.cipher.ECCurves> BY_KEY_SIZE
fld public final static java.util.List<org.apache.sshd.common.cipher.ECCurves> SORTED_KEY_SIZE
fld public final static java.util.NavigableSet<java.lang.String> KEY_TYPES
fld public final static java.util.NavigableSet<java.lang.String> NAMES
fld public final static java.util.Set<org.apache.sshd.common.cipher.ECCurves> VALUES
fld public final static org.apache.sshd.common.cipher.ECCurves nistp256
fld public final static org.apache.sshd.common.cipher.ECCurves nistp384
fld public final static org.apache.sshd.common.cipher.ECCurves nistp521
innr public abstract static !enum ECPointCompression
innr public final static Constants
intf org.apache.sshd.common.NamedResource
intf org.apache.sshd.common.OptionalFeature
intf org.apache.sshd.common.keyprovider.KeySizeIndicator
intf org.apache.sshd.common.keyprovider.KeyTypeIndicator
meth public !varargs static java.math.BigInteger octetStringToInteger(byte[])
meth public !varargs static java.security.spec.ECPoint octetStringToEcPoint(byte[])
meth public final boolean isSupported()
meth public final int getKeySize()
meth public final int getNumPointOctets()
meth public final java.lang.String getKeyType()
meth public final java.lang.String getName()
meth public final java.lang.String getOID()
meth public final java.security.spec.ECParameterSpec getParameters()
meth public final java.util.List<java.lang.Integer> getOIDValue()
meth public final org.apache.sshd.common.digest.Digest getDigestForParams()
meth public static byte[] encodeECPoint(java.security.spec.ECPoint,java.security.spec.ECParameterSpec)
meth public static byte[] encodeECPoint(java.security.spec.ECPoint,java.security.spec.EllipticCurve)
meth public static int getCurveSize(java.security.spec.ECParameterSpec)
meth public static org.apache.sshd.common.cipher.ECCurves fromCurveName(java.lang.String)
meth public static org.apache.sshd.common.cipher.ECCurves fromCurveParameters(java.security.spec.ECParameterSpec)
meth public static org.apache.sshd.common.cipher.ECCurves fromCurveSize(int)
meth public static org.apache.sshd.common.cipher.ECCurves fromECKey(java.security.interfaces.ECKey)
meth public static org.apache.sshd.common.cipher.ECCurves fromKeyType(java.lang.String)
meth public static org.apache.sshd.common.cipher.ECCurves fromOID(java.lang.String)
meth public static org.apache.sshd.common.cipher.ECCurves fromOIDValue(java.util.List<? extends java.lang.Number>)
meth public static org.apache.sshd.common.cipher.ECCurves valueOf(java.lang.String)
meth public static org.apache.sshd.common.cipher.ECCurves[] values()
supr java.lang.Enum<org.apache.sshd.common.cipher.ECCurves>
hfds digestFactory,keySize,keyType,name,numOctets,oidString,oidValue,params,secName

CLSS public final static org.apache.sshd.common.cipher.ECCurves$Constants
 outer org.apache.sshd.common.cipher.ECCurves
fld public final static java.lang.String ECDSA_SHA2_PREFIX = "ecdsa-sha2-"
fld public final static java.lang.String NISTP256 = "nistp256"
fld public final static java.lang.String NISTP384 = "nistp384"
fld public final static java.lang.String NISTP521 = "nistp521"
supr java.lang.Object

CLSS public abstract static !enum org.apache.sshd.common.cipher.ECCurves$ECPointCompression
 outer org.apache.sshd.common.cipher.ECCurves
fld public final static java.util.Set<org.apache.sshd.common.cipher.ECCurves$ECPointCompression> VALUES
fld public final static org.apache.sshd.common.cipher.ECCurves$ECPointCompression UNCOMPRESSED
fld public final static org.apache.sshd.common.cipher.ECCurves$ECPointCompression VARIANT2
fld public final static org.apache.sshd.common.cipher.ECCurves$ECPointCompression VARIANT3
meth protected void writeCoordinate(java.io.OutputStream,java.lang.String,java.math.BigInteger,int) throws java.io.IOException
meth public abstract java.security.spec.ECPoint octetStringToEcPoint(byte[],int,int)
meth public byte[] ecPointToOctetString(java.lang.String,java.security.spec.ECPoint)
meth public final byte getIndicatorValue()
meth public static org.apache.sshd.common.cipher.ECCurves$ECPointCompression fromIndicatorValue(int)
meth public static org.apache.sshd.common.cipher.ECCurves$ECPointCompression valueOf(java.lang.String)
meth public static org.apache.sshd.common.cipher.ECCurves$ECPointCompression[] values()
meth public void writeECPoint(java.io.OutputStream,java.lang.String,java.security.spec.ECPoint) throws java.io.IOException
supr java.lang.Enum<org.apache.sshd.common.cipher.ECCurves$ECPointCompression>
hfds indicatorValue

CLSS abstract interface org.apache.sshd.common.cipher.package-info

CLSS public abstract org.apache.sshd.common.compression.BaseCompression
cons protected init(java.lang.String)
intf org.apache.sshd.common.compression.Compression
meth public boolean isCompressionExecuted()
meth public final java.lang.String getName()
meth public java.lang.String toString()
supr java.lang.Object
hfds name

CLSS public abstract !enum org.apache.sshd.common.compression.BuiltinCompressions
fld public final static java.util.Set<org.apache.sshd.common.compression.BuiltinCompressions> VALUES
fld public final static org.apache.sshd.common.compression.BuiltinCompressions delayedZlib
fld public final static org.apache.sshd.common.compression.BuiltinCompressions none
fld public final static org.apache.sshd.common.compression.BuiltinCompressions zlib
innr public final static Constants
innr public static ParseResult
intf org.apache.sshd.common.compression.CompressionFactory
meth public !varargs static org.apache.sshd.common.compression.BuiltinCompressions$ParseResult parseCompressionsList(java.lang.String[])
meth public boolean isCompressionExecuted()
meth public boolean isDelayed()
meth public final boolean isSupported()
meth public final java.lang.String getName()
meth public final java.lang.String toString()
meth public static java.util.NavigableSet<org.apache.sshd.common.compression.CompressionFactory> getRegisteredExtensions()
meth public static org.apache.sshd.common.compression.BuiltinCompressions fromFactoryName(java.lang.String)
meth public static org.apache.sshd.common.compression.BuiltinCompressions valueOf(java.lang.String)
meth public static org.apache.sshd.common.compression.BuiltinCompressions$ParseResult parseCompressionsList(java.lang.String)
meth public static org.apache.sshd.common.compression.BuiltinCompressions$ParseResult parseCompressionsList(java.util.Collection<java.lang.String>)
meth public static org.apache.sshd.common.compression.BuiltinCompressions[] values()
meth public static org.apache.sshd.common.compression.CompressionFactory resolveFactory(java.lang.String)
meth public static org.apache.sshd.common.compression.CompressionFactory unregisterExtension(java.lang.String)
meth public static void registerExtension(org.apache.sshd.common.compression.CompressionFactory)
supr java.lang.Enum<org.apache.sshd.common.compression.BuiltinCompressions>
hfds EXTENSIONS,name

CLSS public final static org.apache.sshd.common.compression.BuiltinCompressions$Constants
 outer org.apache.sshd.common.compression.BuiltinCompressions
fld public final static java.lang.String DELAYED_ZLIB = "zlib@openssh.com"
fld public final static java.lang.String NONE = "none"
fld public final static java.lang.String ZLIB = "zlib"
supr java.lang.Object

CLSS public static org.apache.sshd.common.compression.BuiltinCompressions$ParseResult
 outer org.apache.sshd.common.compression.BuiltinCompressions
cons public init(java.util.List<org.apache.sshd.common.compression.CompressionFactory>,java.util.List<java.lang.String>)
fld public final static org.apache.sshd.common.compression.BuiltinCompressions$ParseResult EMPTY
supr org.apache.sshd.common.config.NamedFactoriesListParseResult<org.apache.sshd.common.compression.Compression,org.apache.sshd.common.compression.CompressionFactory>

CLSS public abstract interface org.apache.sshd.common.compression.Compression
innr public final static !enum Type
intf org.apache.sshd.common.compression.CompressionInformation
meth public abstract void compress(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public abstract void init(org.apache.sshd.common.compression.Compression$Type,int)
meth public abstract void uncompress(org.apache.sshd.common.util.buffer.Buffer,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException

CLSS public final static !enum org.apache.sshd.common.compression.Compression$Type
 outer org.apache.sshd.common.compression.Compression
fld public final static org.apache.sshd.common.compression.Compression$Type Deflater
fld public final static org.apache.sshd.common.compression.Compression$Type Inflater
meth public static org.apache.sshd.common.compression.Compression$Type valueOf(java.lang.String)
meth public static org.apache.sshd.common.compression.Compression$Type[] values()
supr java.lang.Enum<org.apache.sshd.common.compression.Compression$Type>

CLSS public org.apache.sshd.common.compression.CompressionDelayedZlib
cons public init()
meth public boolean isDelayed()
supr org.apache.sshd.common.compression.CompressionZlib

CLSS public abstract interface org.apache.sshd.common.compression.CompressionFactory
intf org.apache.sshd.common.BuiltinFactory<org.apache.sshd.common.compression.Compression>
intf org.apache.sshd.common.compression.CompressionInformation

CLSS public abstract interface org.apache.sshd.common.compression.CompressionInformation
intf org.apache.sshd.common.NamedResource
meth public abstract boolean isCompressionExecuted()
meth public abstract boolean isDelayed()

CLSS public org.apache.sshd.common.compression.CompressionNone
cons public init()
meth public boolean isCompressionExecuted()
meth public boolean isDelayed()
meth public java.lang.String toString()
meth public void compress(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void init(org.apache.sshd.common.compression.Compression$Type,int)
meth public void uncompress(org.apache.sshd.common.util.buffer.Buffer,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr org.apache.sshd.common.compression.BaseCompression
hfds level,type

CLSS public org.apache.sshd.common.compression.CompressionZlib
cons protected init(java.lang.String)
cons public init()
meth public boolean isDelayed()
meth public void compress(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void init(org.apache.sshd.common.compression.Compression$Type,int)
meth public void uncompress(org.apache.sshd.common.util.buffer.Buffer,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr org.apache.sshd.common.compression.BaseCompression
hfds BUF_SIZE,compresser,decompresser,tmpbuf

CLSS abstract interface org.apache.sshd.common.compression.package-info

CLSS public final !enum org.apache.sshd.common.config.CompressionConfigValue
fld public final static java.util.Set<org.apache.sshd.common.config.CompressionConfigValue> VALUES
fld public final static org.apache.sshd.common.config.CompressionConfigValue DELAYED
fld public final static org.apache.sshd.common.config.CompressionConfigValue NO
fld public final static org.apache.sshd.common.config.CompressionConfigValue YES
intf org.apache.sshd.common.compression.CompressionFactory
meth public boolean isCompressionExecuted()
meth public boolean isDelayed()
meth public boolean isSupported()
meth public final java.lang.String getName()
meth public final java.lang.String toString()
meth public final org.apache.sshd.common.compression.Compression create()
meth public static org.apache.sshd.common.config.CompressionConfigValue fromName(java.lang.String)
meth public static org.apache.sshd.common.config.CompressionConfigValue valueOf(java.lang.String)
meth public static org.apache.sshd.common.config.CompressionConfigValue[] values()
supr java.lang.Enum<org.apache.sshd.common.config.CompressionConfigValue>
hfds factory

CLSS public final org.apache.sshd.common.config.ConfigFileReaderSupport
fld public final static boolean DEFAULT_KBD_INTERACTIVE_AUTH_VALUE
fld public final static boolean DEFAULT_KEEP_ALIVE = true
fld public final static boolean DEFAULT_PASSWORD_AUTH_VALUE
fld public final static boolean DEFAULT_PUBKEY_AUTH_VALUE
fld public final static boolean DEFAULT_USE_DNS = true
fld public final static char COMMENT_CHAR = '#'
fld public final static int DEFAULT_MAX_AUTH_TRIES = 6
fld public final static int DEFAULT_MAX_SESSIONS = 10
fld public final static int DEFAULT_MAX_STARTUPS = 10
fld public final static java.lang.String AUTH_KEYS_FILE_CONFIG_PROP = "AuthorizedKeysFile"
fld public final static java.lang.String CIPHERS_CONFIG_PROP = "Ciphers"
fld public final static java.lang.String COMPRESSION_PROP = "Compression"
fld public final static java.lang.String DEFAULT_BIND_ADDRESS = "0.0.0.0"
fld public final static java.lang.String DEFAULT_COMPRESSION
fld public final static java.lang.String DEFAULT_KBD_INTERACTIVE_AUTH = "yes"
fld public final static java.lang.String DEFAULT_PASSWORD_AUTH = "yes"
fld public final static java.lang.String DEFAULT_PUBKEY_AUTH = "yes"
fld public final static java.lang.String HOST_KEY_ALGORITHMS_CONFIG_PROP = "HostKeyAlgorithms"
fld public final static java.lang.String KBD_INTERACTIVE_CONFIG_PROP = "KbdInteractiveAuthentication"
fld public final static java.lang.String KEEP_ALIVE_CONFIG_PROP = "TCPKeepAlive"
fld public final static java.lang.String KEX_ALGORITHMS_CONFIG_PROP = "KexAlgorithms"
fld public final static java.lang.String KEY_REGENERATE_INTERVAL_CONFIG_PROP = "KeyRegenerationInterval"
fld public final static java.lang.String LISTEN_ADDRESS_CONFIG_PROP = "ListenAddress"
fld public final static java.lang.String LOGIN_GRACE_TIME_CONFIG_PROP = "LoginGraceTime"
fld public final static java.lang.String LOG_LEVEL_CONFIG_PROP = "LogLevel"
fld public final static java.lang.String MACS_CONFIG_PROP = "MACs"
fld public final static java.lang.String MAX_AUTH_TRIES_CONFIG_PROP = "MaxAuthTries"
fld public final static java.lang.String MAX_SESSIONS_CONFIG_PROP = "MaxSessions"
fld public final static java.lang.String MAX_STARTUPS_CONFIG_PROP = "MaxStartups"
fld public final static java.lang.String PASSWORD_AUTH_CONFIG_PROP = "PasswordAuthentication"
fld public final static java.lang.String PORT_CONFIG_PROP = "Port"
fld public final static java.lang.String PREFERRED_AUTHS_CONFIG_PROP = "PreferredAuthentications"
fld public final static java.lang.String PUBKEY_AUTH_CONFIG_PROP = "PubkeyAuthentication"
fld public final static java.lang.String SUBSYSTEM_CONFIG_PROP = "Subsystem"
fld public final static java.lang.String SYSLOG_FACILITY_CONFIG_PROP = "SyslogFacility"
fld public final static java.lang.String USE_DNS_CONFIG_PROP = "UseDNS"
fld public final static long DEFAULT_LOGIN_GRACE_TIME
fld public final static long DEFAULT_REKEY_TIME_LIMIT
fld public final static org.apache.sshd.common.config.LogLevelValue DEFAULT_LOG_LEVEL
fld public final static org.apache.sshd.common.config.SyslogFacilityValue DEFAULT_SYSLOG_FACILITY
meth public !varargs static java.util.Properties readConfigFile(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
meth public static boolean parseBooleanValue(java.lang.String)
meth public static java.lang.String yesNoValueOf(boolean)
meth public static java.util.Properties readConfigFile(java.io.BufferedReader) throws java.io.IOException
meth public static java.util.Properties readConfigFile(java.io.InputStream,boolean) throws java.io.IOException
meth public static java.util.Properties readConfigFile(java.io.Reader,boolean) throws java.io.IOException
meth public static java.util.Properties readConfigFile(java.net.URL) throws java.io.IOException
supr java.lang.Object

CLSS public abstract org.apache.sshd.common.config.FactoriesListParseResult<%0 extends java.lang.Object, %1 extends org.apache.sshd.common.NamedResource>
cons protected init(java.util.List<{org.apache.sshd.common.config.FactoriesListParseResult%1}>,java.util.List<java.lang.String>)
meth public final java.util.List<{org.apache.sshd.common.config.FactoriesListParseResult%1}> getParsedFactories()
meth public java.util.List<java.lang.String> getUnsupportedFactories()
supr org.apache.sshd.common.config.ListParseResult<{org.apache.sshd.common.config.FactoriesListParseResult%1}>

CLSS public abstract org.apache.sshd.common.config.ListParseResult<%0 extends java.lang.Object>
cons protected init(java.util.List<{org.apache.sshd.common.config.ListParseResult%0}>,java.util.List<java.lang.String>)
meth public final java.util.List<{org.apache.sshd.common.config.ListParseResult%0}> getParsedValues()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getUnsupportedValues()
supr java.lang.Object
hfds parsed,unsupported

CLSS public final !enum org.apache.sshd.common.config.LogLevelValue
fld public final static java.util.Set<org.apache.sshd.common.config.LogLevelValue> VALUES
fld public final static org.apache.sshd.common.config.LogLevelValue DEBUG
fld public final static org.apache.sshd.common.config.LogLevelValue DEBUG1
fld public final static org.apache.sshd.common.config.LogLevelValue DEBUG2
fld public final static org.apache.sshd.common.config.LogLevelValue DEBUG3
fld public final static org.apache.sshd.common.config.LogLevelValue ERROR
fld public final static org.apache.sshd.common.config.LogLevelValue FATAL
fld public final static org.apache.sshd.common.config.LogLevelValue INFO
fld public final static org.apache.sshd.common.config.LogLevelValue QUIET
fld public final static org.apache.sshd.common.config.LogLevelValue VERBOSE
meth public java.util.logging.Level getLoggingLevel()
meth public static org.apache.sshd.common.config.LogLevelValue fromName(java.lang.String)
meth public static org.apache.sshd.common.config.LogLevelValue valueOf(java.lang.String)
meth public static org.apache.sshd.common.config.LogLevelValue[] values()
supr java.lang.Enum<org.apache.sshd.common.config.LogLevelValue>
hfds level

CLSS public abstract org.apache.sshd.common.config.NamedFactoriesListParseResult<%0 extends java.lang.Object, %1 extends org.apache.sshd.common.NamedResource>
cons protected init(java.util.List<{org.apache.sshd.common.config.NamedFactoriesListParseResult%1}>,java.util.List<java.lang.String>)
meth public java.lang.String toString()
supr org.apache.sshd.common.config.FactoriesListParseResult<{org.apache.sshd.common.config.NamedFactoriesListParseResult%0},{org.apache.sshd.common.config.NamedFactoriesListParseResult%1}>

CLSS public abstract org.apache.sshd.common.config.NamedResourceListParseResult<%0 extends org.apache.sshd.common.NamedResource>
cons protected init(java.util.List<{org.apache.sshd.common.config.NamedResourceListParseResult%0}>,java.util.List<java.lang.String>)
meth public final java.util.List<{org.apache.sshd.common.config.NamedResourceListParseResult%0}> getParsedResources()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getUnsupportedResources()
supr org.apache.sshd.common.config.ListParseResult<{org.apache.sshd.common.config.NamedResourceListParseResult%0}>

CLSS public final org.apache.sshd.common.config.SshConfigFileReader
meth public static <%0 extends org.apache.sshd.common.helpers.AbstractFactoryManager> {%%0} configure({%%0},org.apache.sshd.common.PropertyResolver,boolean,boolean)
meth public static <%0 extends org.apache.sshd.common.helpers.AbstractFactoryManager> {%%0} configureCiphers({%%0},java.lang.String,boolean,boolean)
meth public static <%0 extends org.apache.sshd.common.helpers.AbstractFactoryManager> {%%0} configureCiphers({%%0},org.apache.sshd.common.PropertyResolver,boolean,boolean)
meth public static <%0 extends org.apache.sshd.common.helpers.AbstractFactoryManager> {%%0} configureCompression({%%0},java.lang.String,boolean,boolean)
meth public static <%0 extends org.apache.sshd.common.helpers.AbstractFactoryManager> {%%0} configureCompression({%%0},org.apache.sshd.common.PropertyResolver,boolean,boolean)
meth public static <%0 extends org.apache.sshd.common.helpers.AbstractFactoryManager> {%%0} configureKeyExchanges({%%0},java.lang.String,boolean,java.util.function.Function<? super org.apache.sshd.common.kex.DHFactory,? extends org.apache.sshd.common.kex.KeyExchangeFactory>,boolean)
meth public static <%0 extends org.apache.sshd.common.helpers.AbstractFactoryManager> {%%0} configureKeyExchanges({%%0},org.apache.sshd.common.PropertyResolver,boolean,java.util.function.Function<? super org.apache.sshd.common.kex.DHFactory,? extends org.apache.sshd.common.kex.KeyExchangeFactory>,boolean)
meth public static <%0 extends org.apache.sshd.common.helpers.AbstractFactoryManager> {%%0} configureMacs({%%0},java.lang.String,boolean,boolean)
meth public static <%0 extends org.apache.sshd.common.helpers.AbstractFactoryManager> {%%0} configureMacs({%%0},org.apache.sshd.common.PropertyResolver,boolean,boolean)
meth public static <%0 extends org.apache.sshd.common.helpers.AbstractFactoryManager> {%%0} configureSignatures({%%0},java.lang.String,boolean,boolean)
meth public static <%0 extends org.apache.sshd.common.helpers.AbstractFactoryManager> {%%0} configureSignatures({%%0},org.apache.sshd.common.PropertyResolver,boolean,boolean)
meth public static org.apache.sshd.common.cipher.BuiltinCiphers$ParseResult getCiphers(org.apache.sshd.common.PropertyResolver)
meth public static org.apache.sshd.common.compression.CompressionFactory getCompression(org.apache.sshd.common.PropertyResolver)
meth public static org.apache.sshd.common.kex.BuiltinDHFactories$ParseResult getKexFactories(org.apache.sshd.common.PropertyResolver)
meth public static org.apache.sshd.common.mac.BuiltinMacs$ParseResult getMacs(org.apache.sshd.common.PropertyResolver)
meth public static org.apache.sshd.common.signature.BuiltinSignatures$ParseResult getSignatures(org.apache.sshd.common.PropertyResolver)
supr java.lang.Object

CLSS public final !enum org.apache.sshd.common.config.SyslogFacilityValue
fld public final static java.util.Set<org.apache.sshd.common.config.SyslogFacilityValue> VALUES
fld public final static org.apache.sshd.common.config.SyslogFacilityValue AUTH
fld public final static org.apache.sshd.common.config.SyslogFacilityValue DAEMON
fld public final static org.apache.sshd.common.config.SyslogFacilityValue LOCAL0
fld public final static org.apache.sshd.common.config.SyslogFacilityValue LOCAL1
fld public final static org.apache.sshd.common.config.SyslogFacilityValue LOCAL2
fld public final static org.apache.sshd.common.config.SyslogFacilityValue LOCAL3
fld public final static org.apache.sshd.common.config.SyslogFacilityValue LOCAL4
fld public final static org.apache.sshd.common.config.SyslogFacilityValue LOCAL5
fld public final static org.apache.sshd.common.config.SyslogFacilityValue LOCAL6
fld public final static org.apache.sshd.common.config.SyslogFacilityValue LOCAL7
fld public final static org.apache.sshd.common.config.SyslogFacilityValue USER
meth public static org.apache.sshd.common.config.SyslogFacilityValue fromName(java.lang.String)
meth public static org.apache.sshd.common.config.SyslogFacilityValue valueOf(java.lang.String)
meth public static org.apache.sshd.common.config.SyslogFacilityValue[] values()
supr java.lang.Enum<org.apache.sshd.common.config.SyslogFacilityValue>

CLSS public final !enum org.apache.sshd.common.config.TimeValueConfig
fld public final static java.util.Set<org.apache.sshd.common.config.TimeValueConfig> VALUES
fld public final static org.apache.sshd.common.config.TimeValueConfig DAYS
fld public final static org.apache.sshd.common.config.TimeValueConfig HOURS
fld public final static org.apache.sshd.common.config.TimeValueConfig MINUTES
fld public final static org.apache.sshd.common.config.TimeValueConfig SECONDS
fld public final static org.apache.sshd.common.config.TimeValueConfig WEEKS
meth public final char getLowerCaseValue()
meth public final char getUpperCaseValue()
meth public final long getInterval()
meth public static java.util.Map<org.apache.sshd.common.config.TimeValueConfig,java.lang.Long> parse(java.lang.String)
meth public static long durationOf(java.lang.String)
meth public static long durationOf(java.util.Map<org.apache.sshd.common.config.TimeValueConfig,? extends java.lang.Number>)
meth public static org.apache.sshd.common.config.TimeValueConfig fromValueChar(char)
meth public static org.apache.sshd.common.config.TimeValueConfig valueOf(java.lang.String)
meth public static org.apache.sshd.common.config.TimeValueConfig[] values()
supr java.lang.Enum<org.apache.sshd.common.config.TimeValueConfig>
hfds hiChar,interval,loChar

CLSS public final org.apache.sshd.common.config.VersionProperties
fld public final static java.lang.String REPORTED_VERSION = "sshd-version"
meth public static java.util.NavigableMap<java.lang.String,java.lang.String> getVersionProperties()
supr java.lang.Object
hcls LazyVersionPropertiesHolder

CLSS public org.apache.sshd.common.config.keys.AuthorizedKeyEntry
cons public init()
fld public final static char BOOLEAN_OPTION_NEGATION_INDICATOR = '!'
meth public !varargs static java.util.List<org.apache.sshd.common.config.keys.AuthorizedKeyEntry> readAuthorizedKeys(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getComment()
meth public java.lang.String toString()
meth public java.security.PublicKey appendPublicKey(org.apache.sshd.common.session.SessionContext,java.lang.Appendable,org.apache.sshd.common.config.keys.PublicKeyEntryResolver) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.security.PublicKey resolvePublicKey(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.config.keys.PublicKeyEntryResolver) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Map<java.lang.String,java.lang.String> getLoginOptions()
meth public static java.util.AbstractMap$SimpleImmutableEntry<java.lang.String,java.lang.String> addLoginOption(java.util.Map<java.lang.String,java.lang.String>,java.lang.String)
meth public static java.util.AbstractMap$SimpleImmutableEntry<java.lang.String,java.lang.String> resolveEntryComponents(java.lang.String)
meth public static java.util.List<org.apache.sshd.common.config.keys.AuthorizedKeyEntry> readAuthorizedKeys(java.io.BufferedReader) throws java.io.IOException
meth public static java.util.List<org.apache.sshd.common.config.keys.AuthorizedKeyEntry> readAuthorizedKeys(java.io.InputStream,boolean) throws java.io.IOException
meth public static java.util.List<org.apache.sshd.common.config.keys.AuthorizedKeyEntry> readAuthorizedKeys(java.io.Reader,boolean) throws java.io.IOException
meth public static java.util.List<org.apache.sshd.common.config.keys.AuthorizedKeyEntry> readAuthorizedKeys(java.net.URL) throws java.io.IOException
meth public static java.util.NavigableMap<java.lang.String,java.lang.String> parseLoginOptions(java.lang.String)
meth public static org.apache.sshd.common.config.keys.AuthorizedKeyEntry parseAuthorizedKeyEntry(java.lang.String)
meth public static org.apache.sshd.common.config.keys.AuthorizedKeyEntry parseAuthorizedKeyEntry(java.lang.String,org.apache.sshd.common.config.keys.PublicKeyEntryDataResolver)
meth public void setComment(java.lang.String)
meth public void setLoginOptions(java.util.Map<java.lang.String,java.lang.String>)
supr org.apache.sshd.common.config.keys.PublicKeyEntry
hfds comment,loginOptions,serialVersionUID

CLSS public !enum org.apache.sshd.common.config.keys.BuiltinIdentities
fld public final static java.util.NavigableSet<java.lang.String> NAMES
fld public final static java.util.Set<org.apache.sshd.common.config.keys.BuiltinIdentities> VALUES
fld public final static org.apache.sshd.common.config.keys.BuiltinIdentities DSA
fld public final static org.apache.sshd.common.config.keys.BuiltinIdentities ECDSA
fld public final static org.apache.sshd.common.config.keys.BuiltinIdentities ED25119
fld public final static org.apache.sshd.common.config.keys.BuiltinIdentities RSA
innr public final static Constants
intf org.apache.sshd.common.config.keys.Identity
meth public boolean isSupported()
meth public final java.lang.Class<? extends java.security.PrivateKey> getPrivateKeyType()
meth public final java.lang.Class<? extends java.security.PublicKey> getPublicKeyType()
meth public final java.lang.String getName()
meth public java.lang.String getAlgorithm()
meth public java.util.NavigableSet<java.lang.String> getSupportedKeyTypes()
meth public static org.apache.sshd.common.config.keys.BuiltinIdentities fromAlgorithm(java.lang.String)
meth public static org.apache.sshd.common.config.keys.BuiltinIdentities fromKey(java.security.Key)
meth public static org.apache.sshd.common.config.keys.BuiltinIdentities fromKeyPair(java.security.KeyPair)
meth public static org.apache.sshd.common.config.keys.BuiltinIdentities fromKeyType(java.lang.Class<?>)
meth public static org.apache.sshd.common.config.keys.BuiltinIdentities fromKeyTypeName(java.lang.String)
meth public static org.apache.sshd.common.config.keys.BuiltinIdentities fromName(java.lang.String)
meth public static org.apache.sshd.common.config.keys.BuiltinIdentities valueOf(java.lang.String)
meth public static org.apache.sshd.common.config.keys.BuiltinIdentities[] values()
supr java.lang.Enum<org.apache.sshd.common.config.keys.BuiltinIdentities>
hfds algorithm,name,prvType,pubType,types

CLSS public final static org.apache.sshd.common.config.keys.BuiltinIdentities$Constants
 outer org.apache.sshd.common.config.keys.BuiltinIdentities
fld public final static java.lang.String DSA = "DSA"
fld public final static java.lang.String ECDSA = "ECDSA"
fld public final static java.lang.String ED25519 = "ED25519"
fld public final static java.lang.String RSA = "RSA"
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.common.config.keys.FilePasswordProvider
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.common.config.keys.FilePasswordProvider EMPTY
innr public abstract interface static Decoder
innr public final static !enum ResourceDecodeResult
meth public <%0 extends java.lang.Object> {%%0} decode(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.config.keys.FilePasswordProvider$Decoder<? extends {%%0}>) throws java.io.IOException,java.security.GeneralSecurityException
meth public abstract java.lang.String getPassword(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,int) throws java.io.IOException
meth public org.apache.sshd.common.config.keys.FilePasswordProvider$ResourceDecodeResult handleDecodeAttemptResult(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,int,java.lang.String,java.lang.Exception) throws java.io.IOException,java.security.GeneralSecurityException
meth public static org.apache.sshd.common.config.keys.FilePasswordProvider of(java.lang.String)

CLSS public abstract interface static org.apache.sshd.common.config.keys.FilePasswordProvider$Decoder<%0 extends java.lang.Object>
 outer org.apache.sshd.common.config.keys.FilePasswordProvider
meth public abstract {org.apache.sshd.common.config.keys.FilePasswordProvider$Decoder%0} decode(java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException

CLSS public final static !enum org.apache.sshd.common.config.keys.FilePasswordProvider$ResourceDecodeResult
 outer org.apache.sshd.common.config.keys.FilePasswordProvider
fld public final static java.util.Set<org.apache.sshd.common.config.keys.FilePasswordProvider$ResourceDecodeResult> VALUES
fld public final static org.apache.sshd.common.config.keys.FilePasswordProvider$ResourceDecodeResult IGNORE
fld public final static org.apache.sshd.common.config.keys.FilePasswordProvider$ResourceDecodeResult RETRY
fld public final static org.apache.sshd.common.config.keys.FilePasswordProvider$ResourceDecodeResult TERMINATE
meth public static org.apache.sshd.common.config.keys.FilePasswordProvider$ResourceDecodeResult valueOf(java.lang.String)
meth public static org.apache.sshd.common.config.keys.FilePasswordProvider$ResourceDecodeResult[] values()
supr java.lang.Enum<org.apache.sshd.common.config.keys.FilePasswordProvider$ResourceDecodeResult>

CLSS public abstract interface org.apache.sshd.common.config.keys.FilePasswordProviderHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.common.config.keys.FilePasswordProvider getFilePasswordProvider()
meth public static org.apache.sshd.common.config.keys.FilePasswordProviderHolder providerHolderOf(org.apache.sshd.common.config.keys.FilePasswordProvider)

CLSS public abstract interface org.apache.sshd.common.config.keys.FilePasswordProviderManager
intf org.apache.sshd.common.config.keys.FilePasswordProviderHolder
meth public abstract void setFilePasswordProvider(org.apache.sshd.common.config.keys.FilePasswordProvider)

CLSS public abstract interface org.apache.sshd.common.config.keys.Identity
intf org.apache.sshd.common.AlgorithmNameProvider
intf org.apache.sshd.common.NamedResource
intf org.apache.sshd.common.OptionalFeature
intf org.apache.sshd.common.config.keys.KeyTypeNamesSupport
meth public abstract java.lang.Class<? extends java.security.PrivateKey> getPrivateKeyType()
meth public abstract java.lang.Class<? extends java.security.PublicKey> getPublicKeyType()

CLSS public abstract interface org.apache.sshd.common.config.keys.IdentityResourceLoader<%0 extends java.security.PublicKey, %1 extends java.security.PrivateKey>
fld public final static int MAX_BIGINT_OCTETS_COUNT = 32767
intf org.apache.sshd.common.config.keys.KeyTypeNamesSupport
meth public abstract java.lang.Class<{org.apache.sshd.common.config.keys.IdentityResourceLoader%0}> getPublicKeyType()
meth public abstract java.lang.Class<{org.apache.sshd.common.config.keys.IdentityResourceLoader%1}> getPrivateKeyType()

CLSS public final org.apache.sshd.common.config.keys.IdentityUtils
meth public !varargs static java.util.NavigableMap<java.lang.String,java.security.KeyPair> loadIdentities(org.apache.sshd.common.session.SessionContext,java.util.Map<java.lang.String,? extends java.nio.file.Path>,org.apache.sshd.common.config.keys.FilePasswordProvider,java.nio.file.OpenOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.lang.String getIdentityFileName(java.lang.String,java.lang.String,java.lang.String)
meth public static org.apache.sshd.common.keyprovider.KeyPairProvider createKeyPairProvider(java.util.Map<java.lang.String,java.security.KeyPair>,boolean)
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.common.config.keys.KeyEntryResolver<%0 extends java.security.PublicKey, %1 extends java.security.PrivateKey>
intf org.apache.sshd.common.config.keys.IdentityResourceLoader<{org.apache.sshd.common.config.keys.KeyEntryResolver%0},{org.apache.sshd.common.config.keys.KeyEntryResolver%1}>
meth public !varargs static int writeRLEBytes(java.io.OutputStream,byte[]) throws java.io.IOException
meth public abstract java.security.KeyFactory getKeyFactoryInstance() throws java.security.GeneralSecurityException
meth public abstract java.security.KeyPairGenerator getKeyPairGenerator() throws java.security.GeneralSecurityException
meth public abstract {org.apache.sshd.common.config.keys.KeyEntryResolver%0} clonePublicKey({org.apache.sshd.common.config.keys.KeyEntryResolver%0}) throws java.security.GeneralSecurityException
meth public abstract {org.apache.sshd.common.config.keys.KeyEntryResolver%1} clonePrivateKey({org.apache.sshd.common.config.keys.KeyEntryResolver%1}) throws java.security.GeneralSecurityException
meth public java.security.KeyPair cloneKeyPair(java.security.KeyPair) throws java.security.GeneralSecurityException
meth public java.security.KeyPair generateKeyPair(int) throws java.security.GeneralSecurityException
meth public static byte[] encodeInt(java.io.OutputStream,int) throws java.io.IOException
meth public static byte[] readRLEBytes(java.io.InputStream,int) throws java.io.IOException
meth public static int decodeInt(byte[])
meth public static int decodeInt(byte[],int,int)
meth public static int decodeInt(java.io.InputStream) throws java.io.IOException
meth public static int encodeBigInt(java.io.OutputStream,java.math.BigInteger) throws java.io.IOException
meth public static int encodeString(java.io.OutputStream,java.lang.String) throws java.io.IOException
meth public static int encodeString(java.io.OutputStream,java.lang.String,java.lang.String) throws java.io.IOException
meth public static int encodeString(java.io.OutputStream,java.lang.String,java.nio.charset.Charset) throws java.io.IOException
meth public static int writeRLEBytes(java.io.OutputStream,byte[],int,int) throws java.io.IOException
meth public static java.lang.String decodeString(java.io.InputStream,int) throws java.io.IOException
meth public static java.lang.String decodeString(java.io.InputStream,java.lang.String,int) throws java.io.IOException
meth public static java.lang.String decodeString(java.io.InputStream,java.nio.charset.Charset,int) throws java.io.IOException
meth public static java.math.BigInteger decodeBigInt(java.io.InputStream) throws java.io.IOException
meth public static java.util.Map$Entry<byte[],java.lang.Integer> readRLEBytes(byte[],int)
meth public static java.util.Map$Entry<byte[],java.lang.Integer> readRLEBytes(byte[],int,int,int)
meth public static java.util.Map$Entry<java.lang.String,java.lang.Integer> decodeString(byte[],int)
meth public static java.util.Map$Entry<java.lang.String,java.lang.Integer> decodeString(byte[],int,int,int)
meth public static java.util.Map$Entry<java.lang.String,java.lang.Integer> decodeString(byte[],int,int,java.nio.charset.Charset,int)
meth public static java.util.Map$Entry<java.lang.String,java.lang.Integer> decodeString(byte[],java.nio.charset.Charset,int)

CLSS public org.apache.sshd.common.config.keys.KeyRandomArt
cons public init(java.lang.String,int,byte[])
cons public init(java.security.PublicKey) throws java.lang.Exception
cons public init(java.security.PublicKey,org.apache.sshd.common.Factory<? extends org.apache.sshd.common.digest.Digest>) throws java.lang.Exception
cons public init(java.security.PublicKey,org.apache.sshd.common.digest.Digest) throws java.lang.Exception
fld public final static int FLDBASE = 8
fld public final static int FLDSIZE_X = 17
fld public final static int FLDSIZE_Y = 9
fld public final static java.lang.String AUGMENTATION_STRING = " .o+=*BOX@%&#/^SE"
intf org.apache.sshd.common.AlgorithmNameProvider
intf org.apache.sshd.common.keyprovider.KeySizeIndicator
meth public <%0 extends java.lang.Appendable> {%%0} append({%%0}) throws java.io.IOException
meth public int getKeySize()
meth public java.lang.String getAlgorithm()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Appendable> {%%0} combine(org.apache.sshd.common.session.SessionContext,{%%0},char,org.apache.sshd.common.keyprovider.KeyIdentityProvider) throws java.lang.Exception
meth public static <%0 extends java.lang.Appendable> {%%0} combine({%%0},char,java.util.Collection<? extends org.apache.sshd.common.config.keys.KeyRandomArt>) throws java.io.IOException
meth public static java.lang.String combine(char,java.util.Collection<? extends org.apache.sshd.common.config.keys.KeyRandomArt>)
meth public static java.lang.String combine(org.apache.sshd.common.session.SessionContext,char,org.apache.sshd.common.keyprovider.KeyIdentityProvider) throws java.lang.Exception
meth public static java.util.Collection<org.apache.sshd.common.config.keys.KeyRandomArt> generate(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.keyprovider.KeyIdentityProvider) throws java.lang.Exception
supr java.lang.Object
hfds algorithm,field,keySize

CLSS public abstract interface org.apache.sshd.common.config.keys.KeyTypeNamesSupport
 anno 0 java.lang.FunctionalInterface()
meth public abstract java.util.NavigableSet<java.lang.String> getSupportedKeyTypes()
meth public static <%0 extends org.apache.sshd.common.config.keys.KeyTypeNamesSupport> {%%0} findSupporterByKeyTypeName(java.lang.String,java.util.Collection<? extends {%%0}>)

CLSS public final org.apache.sshd.common.config.keys.KeyUtils
fld public final static java.lang.String DSS_ALGORITHM = "DSA"
fld public final static java.lang.String EC_ALGORITHM = "EC"
fld public final static java.lang.String KEY_FINGERPRINT_FACTORY_PROP = "org.apache.sshd.keyFingerprintFactory"
fld public final static java.lang.String RSA_ALGORITHM = "RSA"
fld public final static java.lang.String RSA_SHA256_CERT_TYPE_ALIAS = "rsa-sha2-256-cert-v01@openssh.com"
fld public final static java.lang.String RSA_SHA256_KEY_TYPE_ALIAS = "rsa-sha2-256"
fld public final static java.lang.String RSA_SHA512_CERT_TYPE_ALIAS = "rsa-sha2-512-cert-v01@openssh.com"
fld public final static java.lang.String RSA_SHA512_KEY_TYPE_ALIAS = "rsa-sha2-512"
fld public final static java.math.BigInteger DEFAULT_RSA_PUBLIC_EXPONENT
fld public final static java.util.Set<java.nio.file.attribute.PosixFilePermission> STRICTLY_PROHIBITED_FILE_PERMISSION
fld public final static org.apache.sshd.common.digest.DigestFactory DEFAULT_FINGERPRINT_DIGEST_FACTORY
meth public !varargs static java.security.PublicKey findMatchingKey(java.security.PublicKey,java.security.PublicKey[])
meth public !varargs static java.util.AbstractMap$SimpleImmutableEntry<java.lang.String,java.lang.Object> validateStrictKeyFilePermissions(java.nio.file.Path,java.nio.file.LinkOption[]) throws java.io.IOException
meth public static boolean compareDSAKeys(java.security.interfaces.DSAPrivateKey,java.security.interfaces.DSAPrivateKey)
meth public static boolean compareDSAKeys(java.security.interfaces.DSAPublicKey,java.security.interfaces.DSAPublicKey)
meth public static boolean compareDSAParams(java.security.interfaces.DSAParams,java.security.interfaces.DSAParams)
meth public static boolean compareECKeys(java.security.interfaces.ECPrivateKey,java.security.interfaces.ECPrivateKey)
meth public static boolean compareECKeys(java.security.interfaces.ECPublicKey,java.security.interfaces.ECPublicKey)
meth public static boolean compareECParams(java.security.spec.ECParameterSpec,java.security.spec.ECParameterSpec)
meth public static boolean compareKeyPairs(java.security.KeyPair,java.security.KeyPair)
meth public static boolean compareKeys(java.security.PrivateKey,java.security.PrivateKey)
meth public static boolean compareKeys(java.security.PublicKey,java.security.PublicKey)
meth public static boolean compareOpenSSHCertificateKeys(org.apache.sshd.common.config.keys.OpenSshCertificate,org.apache.sshd.common.config.keys.OpenSshCertificate)
meth public static boolean compareRSAKeys(java.security.interfaces.RSAPrivateKey,java.security.interfaces.RSAPrivateKey)
meth public static boolean compareRSAKeys(java.security.interfaces.RSAPublicKey,java.security.interfaces.RSAPublicKey)
meth public static boolean compareSkEcdsaKeys(org.apache.sshd.common.config.keys.u2f.SkEcdsaPublicKey,org.apache.sshd.common.config.keys.u2f.SkEcdsaPublicKey)
meth public static boolean compareSkEd25519Keys(org.apache.sshd.common.config.keys.u2f.SkED25519PublicKey,org.apache.sshd.common.config.keys.u2f.SkED25519PublicKey)
meth public static boolean isCertificateAlgorithm(java.lang.String)
meth public static byte[] getRawFingerprint(java.security.PublicKey) throws java.lang.Exception
meth public static byte[] getRawFingerprint(org.apache.sshd.common.Factory<? extends org.apache.sshd.common.digest.Digest>,java.security.PublicKey) throws java.lang.Exception
meth public static byte[] getRawFingerprint(org.apache.sshd.common.digest.Digest,java.security.PublicKey) throws java.lang.Exception
meth public static int getKeySize(java.security.Key)
meth public static java.lang.String getCanonicalKeyType(java.lang.String)
meth public static java.lang.String getFingerPrint(java.lang.String)
meth public static java.lang.String getFingerPrint(java.lang.String,java.nio.charset.Charset)
meth public static java.lang.String getFingerPrint(java.security.PublicKey)
meth public static java.lang.String getFingerPrint(org.apache.sshd.common.Factory<? extends org.apache.sshd.common.digest.Digest>,java.lang.String)
meth public static java.lang.String getFingerPrint(org.apache.sshd.common.Factory<? extends org.apache.sshd.common.digest.Digest>,java.lang.String,java.nio.charset.Charset)
meth public static java.lang.String getFingerPrint(org.apache.sshd.common.Factory<? extends org.apache.sshd.common.digest.Digest>,java.security.PublicKey)
meth public static java.lang.String getFingerPrint(org.apache.sshd.common.digest.Digest,java.lang.String)
meth public static java.lang.String getFingerPrint(org.apache.sshd.common.digest.Digest,java.lang.String,java.nio.charset.Charset)
meth public static java.lang.String getFingerPrint(org.apache.sshd.common.digest.Digest,java.security.PublicKey)
meth public static java.lang.String getKeyType(java.security.Key)
meth public static java.lang.String getKeyType(java.security.KeyPair)
meth public static java.lang.String getSignatureAlgorithm(java.lang.String,java.security.PublicKey)
meth public static java.lang.String unregisterCanonicalKeyTypeAlias(java.lang.String)
meth public static java.security.KeyPair cloneKeyPair(java.lang.String,java.security.KeyPair) throws java.security.GeneralSecurityException
meth public static java.security.KeyPair generateKeyPair(java.lang.String,int) throws java.security.GeneralSecurityException
meth public static java.security.PublicKey findMatchingKey(java.security.PublicKey,java.util.Collection<? extends java.security.PublicKey>)
meth public static java.security.PublicKey loadPublicKey(java.nio.file.Path) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.security.PublicKey recoverPublicKey(java.security.PrivateKey) throws java.security.GeneralSecurityException
meth public static java.security.interfaces.DSAPublicKey recoverDSAPublicKey(java.security.interfaces.DSAPrivateKey) throws java.security.GeneralSecurityException
meth public static java.security.interfaces.RSAPublicKey recoverFromRSAPrivateCrtKey(java.security.interfaces.RSAPrivateCrtKey) throws java.security.GeneralSecurityException
meth public static java.security.interfaces.RSAPublicKey recoverRSAPublicKey(java.math.BigInteger,java.math.BigInteger) throws java.security.GeneralSecurityException
meth public static java.security.interfaces.RSAPublicKey recoverRSAPublicKey(java.math.BigInteger,java.math.BigInteger,java.math.BigInteger) throws java.security.GeneralSecurityException
meth public static java.security.interfaces.RSAPublicKey recoverRSAPublicKey(java.security.interfaces.RSAPrivateKey) throws java.security.GeneralSecurityException
meth public static java.util.AbstractMap$SimpleImmutableEntry<java.lang.Boolean,java.lang.String> checkFingerPrint(java.lang.String,java.security.PublicKey)
meth public static java.util.AbstractMap$SimpleImmutableEntry<java.lang.Boolean,java.lang.String> checkFingerPrint(java.lang.String,org.apache.sshd.common.Factory<? extends org.apache.sshd.common.digest.Digest>,java.security.PublicKey)
meth public static java.util.AbstractMap$SimpleImmutableEntry<java.lang.Boolean,java.lang.String> checkFingerPrint(java.lang.String,org.apache.sshd.common.digest.Digest,java.security.PublicKey)
meth public static java.util.List<java.lang.String> getAllEquivalentKeyTypes(java.lang.String)
meth public static java.util.List<java.lang.String> registerCanonicalKeyTypes(java.lang.String,java.util.Collection<java.lang.String>)
meth public static java.util.NavigableSet<java.lang.String> getRegisteredKeyTypeAliases()
meth public static java.util.NavigableSet<java.lang.String> unregisterPublicKeyEntryDecoder(org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<?,?>)
meth public static java.util.NavigableSet<java.lang.String> unregisterPublicKeyEntryDecoderKeyTypes(org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<?,?>)
meth public static org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<?,?> getPublicKeyEntryDecoder(java.lang.Class<?>)
meth public static org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<?,?> getPublicKeyEntryDecoder(java.lang.String)
meth public static org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<?,?> getPublicKeyEntryDecoder(java.security.Key)
meth public static org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<?,?> getPublicKeyEntryDecoder(java.security.KeyPair)
meth public static org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<?,?> registerPublicKeyEntryDecoderForKeyType(java.lang.String,org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<?,?>)
meth public static org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<?,?> unregisterPublicKeyEntryDecoderForKeyType(java.lang.String)
meth public static org.apache.sshd.common.digest.DigestFactory getDefaultFingerPrintFactory()
meth public static void registerPublicKeyEntryDecoder(org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<?,?>)
meth public static void registerPublicKeyEntryDecoderKeyTypes(org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<?,?>)
meth public static void setDefaultFingerPrintFactory(org.apache.sshd.common.digest.DigestFactory)
supr java.lang.Object
hfds BY_KEY_CLASS_DECODERS_MAP,BY_KEY_TYPE_DECODERS_MAP,DEFAULT_DIGEST_HOLDER,KEY_TYPE_ALIASES,SIGNATURE_ALGORITHM_MAP

CLSS public abstract interface org.apache.sshd.common.config.keys.OpenSshCertificate
fld public final static long INFINITY = -1
fld public final static long MIN_EPOCH = 0
innr public final static !enum Type
innr public static CertificateOption
intf java.security.PrivateKey
intf org.apache.sshd.common.config.keys.SshPublicKey
meth public abstract byte[] getMessage()
meth public abstract byte[] getNonce()
meth public abstract byte[] getRawSignature()
meth public abstract byte[] getSignature()
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getRawKeyType()
meth public abstract java.lang.String getReserved()
meth public abstract java.lang.String getSignatureAlgorithm()
meth public abstract java.security.PublicKey getCaPubKey()
meth public abstract java.security.PublicKey getCertPubKey()
meth public abstract java.util.Collection<java.lang.String> getPrincipals()
meth public abstract java.util.List<org.apache.sshd.common.config.keys.OpenSshCertificate$CertificateOption> getCriticalOptions()
meth public abstract java.util.List<org.apache.sshd.common.config.keys.OpenSshCertificate$CertificateOption> getExtensions()
meth public abstract java.util.SortedMap<java.lang.String,java.lang.String> getCriticalOptionsMap()
meth public abstract java.util.SortedMap<java.lang.String,java.lang.String> getExtensionsMap()
meth public abstract long getSerial()
meth public abstract long getValidAfter()
meth public abstract long getValidBefore()
meth public abstract org.apache.sshd.common.config.keys.OpenSshCertificate$Type getType()
meth public static boolean isValidAt(org.apache.sshd.common.config.keys.OpenSshCertificate,java.time.Instant)
meth public static boolean isValidNow(org.apache.sshd.common.config.keys.OpenSshCertificate)

CLSS public static org.apache.sshd.common.config.keys.OpenSshCertificate$CertificateOption
 outer org.apache.sshd.common.config.keys.OpenSshCertificate
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
meth public boolean equals(java.lang.Object)
meth public final java.lang.String getData()
meth public final java.lang.String getName()
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds data,name

CLSS public final static !enum org.apache.sshd.common.config.keys.OpenSshCertificate$Type
 outer org.apache.sshd.common.config.keys.OpenSshCertificate
fld public final static java.util.List<org.apache.sshd.common.config.keys.OpenSshCertificate$Type> VALUES
fld public final static org.apache.sshd.common.config.keys.OpenSshCertificate$Type HOST
fld public final static org.apache.sshd.common.config.keys.OpenSshCertificate$Type USER
meth public int getCode()
meth public static org.apache.sshd.common.config.keys.OpenSshCertificate$Type fromCode(int)
meth public static org.apache.sshd.common.config.keys.OpenSshCertificate$Type valueOf(java.lang.String)
meth public static org.apache.sshd.common.config.keys.OpenSshCertificate$Type[] values()
supr java.lang.Enum<org.apache.sshd.common.config.keys.OpenSshCertificate$Type>

CLSS public org.apache.sshd.common.config.keys.OpenSshCertificateImpl
cons public init()
intf org.apache.sshd.common.config.keys.OpenSshCertificate
meth public boolean addCriticalOption(java.lang.String,java.lang.String)
meth public boolean addExtension(java.lang.String,java.lang.String)
meth public byte[] getEncoded()
meth public byte[] getMessage()
meth public byte[] getNonce()
meth public byte[] getRawSignature()
meth public byte[] getSignature()
meth public java.lang.String getAlgorithm()
meth public java.lang.String getFormat()
meth public java.lang.String getId()
meth public java.lang.String getKeyType()
meth public java.lang.String getRawKeyType()
meth public java.lang.String getReserved()
meth public java.lang.String getSignatureAlgorithm()
meth public java.lang.String toString()
meth public java.security.PublicKey getCaPubKey()
meth public java.security.PublicKey getCertPubKey()
meth public java.util.Collection<java.lang.String> getPrincipals()
meth public java.util.List<org.apache.sshd.common.config.keys.OpenSshCertificate$CertificateOption> getCriticalOptions()
meth public java.util.List<org.apache.sshd.common.config.keys.OpenSshCertificate$CertificateOption> getExtensions()
meth public java.util.SortedMap<java.lang.String,java.lang.String> getCriticalOptionsMap()
meth public java.util.SortedMap<java.lang.String,java.lang.String> getExtensionsMap()
meth public long getSerial()
meth public long getValidAfter()
meth public long getValidBefore()
meth public org.apache.sshd.common.config.keys.OpenSshCertificate$Type getType()
meth public void setCaPubKey(java.security.PublicKey)
meth public void setCertPubKey(java.security.PublicKey)
meth public void setCriticalOptions(java.util.List<org.apache.sshd.common.config.keys.OpenSshCertificate$CertificateOption>)
meth public void setCriticalOptions(java.util.Map<java.lang.String,java.lang.String>)
meth public void setExtensions(java.util.List<org.apache.sshd.common.config.keys.OpenSshCertificate$CertificateOption>)
meth public void setExtensions(java.util.Map<java.lang.String,java.lang.String>)
meth public void setId(java.lang.String)
meth public void setKeyType(java.lang.String)
meth public void setMessage(byte[])
meth public void setNonce(byte[])
meth public void setPrincipals(java.util.Collection<java.lang.String>)
meth public void setReserved(java.lang.String)
meth public void setSerial(long)
meth public void setSignature(byte[])
meth public void setType(org.apache.sshd.common.config.keys.OpenSshCertificate$Type)
meth public void setValidAfter(java.time.Instant)
meth public void setValidAfter(long)
meth public void setValidBefore(java.time.Instant)
meth public void setValidBefore(long)
supr java.lang.Object
hfds caPubKey,certificatePublicKey,criticalOptions,extensions,id,keyType,message,nonce,principals,reserved,serial,serialVersionUID,signature,type,validAfter,validBefore

CLSS public abstract interface org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder<%0 extends java.security.PublicKey, %1 extends java.security.PrivateKey>
intf org.apache.sshd.common.config.keys.KeyEntryResolver<{org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder%0},{org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder%1}>
intf org.apache.sshd.common.config.keys.PrivateKeyEntryResolver
meth public !varargs {org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder%1} decodePrivateKey(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.config.keys.FilePasswordProvider,byte[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public abstract {org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder%1} decodePrivateKey(org.apache.sshd.common.session.SessionContext,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream) throws java.io.IOException,java.security.GeneralSecurityException
meth public boolean isPublicKeyRecoverySupported()
meth public java.lang.String encodePrivateKey(org.apache.sshd.common.util.io.output.SecureByteArrayOutputStream,{org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder%1},{org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder%0}) throws java.io.IOException
meth public java.security.PrivateKey resolve(org.apache.sshd.common.session.SessionContext,java.lang.String,byte[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public {org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder%0} recoverPublicKey({org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder%1}) throws java.security.GeneralSecurityException
meth public {org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder%1} decodePrivateKey(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.config.keys.FilePasswordProvider,byte[],int,int) throws java.io.IOException,java.security.GeneralSecurityException
meth public {org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder%1} decodePrivateKey(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream) throws java.io.IOException,java.security.GeneralSecurityException

CLSS public abstract interface org.apache.sshd.common.config.keys.PrivateKeyEntryResolver
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.common.config.keys.PrivateKeyEntryResolver FAILING
fld public final static org.apache.sshd.common.config.keys.PrivateKeyEntryResolver IGNORING
meth public abstract java.security.PrivateKey resolve(org.apache.sshd.common.session.SessionContext,java.lang.String,byte[]) throws java.io.IOException,java.security.GeneralSecurityException

CLSS public org.apache.sshd.common.config.keys.PublicKeyEntry
cons public !varargs init(java.lang.String,byte[])
cons public init()
fld public final static char COMMENT_CHAR = '#'
fld public final static java.lang.String PUBKEY_FILE_SUFFIX = ".pub"
fld public final static java.lang.String STD_KEYFILE_FOLDER_NAME = ".ssh"
intf java.io.Serializable
intf org.apache.sshd.common.keyprovider.KeyTypeIndicator
meth protected boolean isEquivalent(org.apache.sshd.common.config.keys.PublicKeyEntry)
meth public boolean equals(java.lang.Object)
meth public byte[] getKeyData()
meth public int hashCode()
meth public java.lang.String getKeyType()
meth public java.lang.String toString()
meth public java.security.PublicKey appendPublicKey(org.apache.sshd.common.session.SessionContext,java.lang.Appendable,org.apache.sshd.common.config.keys.PublicKeyEntryResolver) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.security.PublicKey resolvePublicKey(org.apache.sshd.common.session.SessionContext,java.util.Map<java.lang.String,java.lang.String>,org.apache.sshd.common.config.keys.PublicKeyEntryResolver) throws java.io.IOException,java.security.GeneralSecurityException
meth public org.apache.sshd.common.config.keys.PublicKeyEntryDataResolver getKeyDataResolver()
meth public org.apache.sshd.common.config.keys.PublicKeyEntryDataResolver resolvePublicKeyEntryDataResolver()
meth public static <%0 extends java.lang.Appendable> {%%0} appendPublicKeyEntry({%%0},java.security.PublicKey) throws java.io.IOException
meth public static <%0 extends java.lang.Appendable> {%%0} appendPublicKeyEntry({%%0},java.security.PublicKey,org.apache.sshd.common.config.keys.PublicKeyEntryDataResolver) throws java.io.IOException
meth public static <%0 extends org.apache.sshd.common.config.keys.PublicKeyEntry> {%%0} parsePublicKeyEntry({%%0},java.lang.String)
meth public static <%0 extends org.apache.sshd.common.config.keys.PublicKeyEntry> {%%0} parsePublicKeyEntry({%%0},java.lang.String,org.apache.sshd.common.config.keys.PublicKeyEntryDataResolver)
meth public static java.lang.String toString(java.security.PublicKey)
meth public static java.lang.String toString(java.security.PublicKey,org.apache.sshd.common.config.keys.PublicKeyEntryDataResolver)
meth public static java.nio.file.Path getDefaultKeysFolderPath()
meth public static java.util.List<java.security.PublicKey> resolvePublicKeyEntries(org.apache.sshd.common.session.SessionContext,java.util.Collection<? extends org.apache.sshd.common.config.keys.PublicKeyEntry>,org.apache.sshd.common.config.keys.PublicKeyEntryResolver) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.util.NavigableMap<java.lang.String,org.apache.sshd.common.config.keys.PublicKeyEntryDataResolver> getRegisteredKeyDataEntryResolvers()
meth public static org.apache.sshd.common.config.keys.PublicKeyEntry parsePublicKeyEntry(java.lang.String)
meth public static org.apache.sshd.common.config.keys.PublicKeyEntry parsePublicKeyEntry(java.lang.String,org.apache.sshd.common.config.keys.PublicKeyEntryDataResolver)
meth public static org.apache.sshd.common.config.keys.PublicKeyEntryDataResolver getKeyDataEntryResolver(java.lang.String)
meth public static org.apache.sshd.common.config.keys.PublicKeyEntryDataResolver resolveKeyDataEntryResolver(java.lang.String)
meth public static org.apache.sshd.common.config.keys.PublicKeyEntryDataResolver unregisterKeyDataEntryResolver(java.lang.String)
meth public static void registerKeyDataEntryResolver(java.lang.String,org.apache.sshd.common.config.keys.PublicKeyEntryDataResolver)
meth public void setKeyData(byte[])
meth public void setKeyDataResolver(org.apache.sshd.common.config.keys.PublicKeyEntryDataResolver)
meth public void setKeyType(java.lang.String)
supr java.lang.Object
hfds KEY_DATA_RESOLVERS,keyData,keyDataResolver,keyType,serialVersionUID
hcls LazyDefaultKeysFolderHolder

CLSS public abstract interface org.apache.sshd.common.config.keys.PublicKeyEntryDataResolver
fld public final static org.apache.sshd.common.config.keys.PublicKeyEntryDataResolver DEFAULT
meth public byte[] decodeEntryKeyData(java.lang.String)
meth public java.lang.String encodeEntryKeyData(byte[])

CLSS public abstract interface org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<%0 extends java.security.PublicKey, %1 extends java.security.PrivateKey>
intf org.apache.sshd.common.config.keys.KeyEntryResolver<{org.apache.sshd.common.config.keys.PublicKeyEntryDecoder%0},{org.apache.sshd.common.config.keys.PublicKeyEntryDecoder%1}>
intf org.apache.sshd.common.config.keys.PublicKeyEntryResolver
intf org.apache.sshd.common.config.keys.PublicKeyRawDataDecoder<{org.apache.sshd.common.config.keys.PublicKeyEntryDecoder%0}>
meth public abstract java.lang.String encodePublicKey(java.io.OutputStream,{org.apache.sshd.common.config.keys.PublicKeyEntryDecoder%0}) throws java.io.IOException
meth public java.security.PublicKey resolve(org.apache.sshd.common.session.SessionContext,java.lang.String,byte[],java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public {org.apache.sshd.common.config.keys.PublicKeyEntryDecoder%0} decodePublicKeyByType(org.apache.sshd.common.session.SessionContext,java.lang.String,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException

CLSS public abstract interface org.apache.sshd.common.config.keys.PublicKeyEntryResolver
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.common.config.keys.PublicKeyEntryResolver FAILING
fld public final static org.apache.sshd.common.config.keys.PublicKeyEntryResolver IGNORING
fld public final static org.apache.sshd.common.config.keys.PublicKeyEntryResolver UNSUPPORTED
meth public abstract java.security.PublicKey resolve(org.apache.sshd.common.session.SessionContext,java.lang.String,byte[],java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException

CLSS public abstract interface org.apache.sshd.common.config.keys.PublicKeyRawDataDecoder<%0 extends java.security.PublicKey>
meth public abstract {org.apache.sshd.common.config.keys.PublicKeyRawDataDecoder%0} decodePublicKey(org.apache.sshd.common.session.SessionContext,java.lang.String,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public abstract {org.apache.sshd.common.config.keys.PublicKeyRawDataDecoder%0} decodePublicKeyByType(org.apache.sshd.common.session.SessionContext,java.lang.String,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public {org.apache.sshd.common.config.keys.PublicKeyRawDataDecoder%0} decodePublicKey(org.apache.sshd.common.session.SessionContext,java.lang.String,byte[],int,int,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public {org.apache.sshd.common.config.keys.PublicKeyRawDataDecoder%0} decodePublicKey(org.apache.sshd.common.session.SessionContext,java.lang.String,byte[],java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException

CLSS public abstract interface org.apache.sshd.common.config.keys.PublicKeyRawDataReader<%0 extends java.security.PublicKey>
meth public !varargs {org.apache.sshd.common.config.keys.PublicKeyRawDataReader%0} readPublicKey(org.apache.sshd.common.session.SessionContext,java.nio.file.Path,java.nio.charset.Charset,java.nio.file.OpenOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public !varargs {org.apache.sshd.common.config.keys.PublicKeyRawDataReader%0} readPublicKey(org.apache.sshd.common.session.SessionContext,java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public abstract {org.apache.sshd.common.config.keys.PublicKeyRawDataReader%0} readPublicKey(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.util.List<java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public {org.apache.sshd.common.config.keys.PublicKeyRawDataReader%0} readPublicKey(org.apache.sshd.common.session.SessionContext,java.net.URL) throws java.io.IOException,java.security.GeneralSecurityException
meth public {org.apache.sshd.common.config.keys.PublicKeyRawDataReader%0} readPublicKey(org.apache.sshd.common.session.SessionContext,java.net.URL,java.nio.charset.Charset) throws java.io.IOException,java.security.GeneralSecurityException
meth public {org.apache.sshd.common.config.keys.PublicKeyRawDataReader%0} readPublicKey(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.io.BufferedReader) throws java.io.IOException,java.security.GeneralSecurityException
meth public {org.apache.sshd.common.config.keys.PublicKeyRawDataReader%0} readPublicKey(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.io.InputStream) throws java.io.IOException,java.security.GeneralSecurityException
meth public {org.apache.sshd.common.config.keys.PublicKeyRawDataReader%0} readPublicKey(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.io.InputStream,java.nio.charset.Charset) throws java.io.IOException,java.security.GeneralSecurityException
meth public {org.apache.sshd.common.config.keys.PublicKeyRawDataReader%0} readPublicKey(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.io.Reader) throws java.io.IOException,java.security.GeneralSecurityException
meth public {org.apache.sshd.common.config.keys.PublicKeyRawDataReader%0} readPublicKey(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.util.io.resource.IoResource<?>) throws java.io.IOException,java.security.GeneralSecurityException
meth public {org.apache.sshd.common.config.keys.PublicKeyRawDataReader%0} readPublicKey(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.util.io.resource.IoResource<?>,java.nio.charset.Charset) throws java.io.IOException,java.security.GeneralSecurityException

CLSS public abstract interface org.apache.sshd.common.config.keys.SshPublicKey
intf java.security.PublicKey
meth public abstract java.lang.String getKeyType()

CLSS public org.apache.sshd.common.config.keys.UnsupportedSshPublicKey
cons public init(java.lang.String,byte[])
intf org.apache.sshd.common.config.keys.SshPublicKey
meth public boolean equals(java.lang.Object)
meth public byte[] getEncoded()
meth public byte[] getKeyData()
meth public int hashCode()
meth public java.lang.String getAlgorithm()
meth public java.lang.String getFormat()
meth public java.lang.String getKeyType()
supr java.lang.Object
hfds keyData,keyType,serialVersionUID

CLSS public abstract org.apache.sshd.common.config.keys.impl.AbstractIdentityResourceLoader<%0 extends java.security.PublicKey, %1 extends java.security.PrivateKey>
cons protected init(java.lang.Class<{org.apache.sshd.common.config.keys.impl.AbstractIdentityResourceLoader%0}>,java.lang.Class<{org.apache.sshd.common.config.keys.impl.AbstractIdentityResourceLoader%1}>,java.util.Collection<java.lang.String>)
intf org.apache.sshd.common.config.keys.IdentityResourceLoader<{org.apache.sshd.common.config.keys.impl.AbstractIdentityResourceLoader%0},{org.apache.sshd.common.config.keys.impl.AbstractIdentityResourceLoader%1}>
meth public final java.lang.Class<{org.apache.sshd.common.config.keys.impl.AbstractIdentityResourceLoader%0}> getPublicKeyType()
meth public final java.lang.Class<{org.apache.sshd.common.config.keys.impl.AbstractIdentityResourceLoader%1}> getPrivateKeyType()
meth public java.util.NavigableSet<java.lang.String> getSupportedKeyTypes()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds prvType,pubType,types

CLSS public abstract org.apache.sshd.common.config.keys.impl.AbstractKeyEntryResolver<%0 extends java.security.PublicKey, %1 extends java.security.PrivateKey>
cons protected init(java.lang.Class<{org.apache.sshd.common.config.keys.impl.AbstractKeyEntryResolver%0}>,java.lang.Class<{org.apache.sshd.common.config.keys.impl.AbstractKeyEntryResolver%1}>,java.util.Collection<java.lang.String>)
intf org.apache.sshd.common.config.keys.KeyEntryResolver<{org.apache.sshd.common.config.keys.impl.AbstractKeyEntryResolver%0},{org.apache.sshd.common.config.keys.impl.AbstractKeyEntryResolver%1}>
meth public java.lang.String toString()
meth public {org.apache.sshd.common.config.keys.impl.AbstractKeyEntryResolver%0} generatePublicKey(java.security.spec.KeySpec) throws java.security.GeneralSecurityException
meth public {org.apache.sshd.common.config.keys.impl.AbstractKeyEntryResolver%1} generatePrivateKey(java.security.spec.KeySpec) throws java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.impl.AbstractIdentityResourceLoader<{org.apache.sshd.common.config.keys.impl.AbstractKeyEntryResolver%0},{org.apache.sshd.common.config.keys.impl.AbstractKeyEntryResolver%1}>

CLSS public abstract org.apache.sshd.common.config.keys.impl.AbstractPrivateKeyEntryDecoder<%0 extends java.security.PublicKey, %1 extends java.security.PrivateKey>
cons protected init(java.lang.Class<{org.apache.sshd.common.config.keys.impl.AbstractPrivateKeyEntryDecoder%0}>,java.lang.Class<{org.apache.sshd.common.config.keys.impl.AbstractPrivateKeyEntryDecoder%1}>,java.util.Collection<java.lang.String>)
intf org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder<{org.apache.sshd.common.config.keys.impl.AbstractPrivateKeyEntryDecoder%0},{org.apache.sshd.common.config.keys.impl.AbstractPrivateKeyEntryDecoder%1}>
supr org.apache.sshd.common.config.keys.impl.AbstractKeyEntryResolver<{org.apache.sshd.common.config.keys.impl.AbstractPrivateKeyEntryDecoder%0},{org.apache.sshd.common.config.keys.impl.AbstractPrivateKeyEntryDecoder%1}>

CLSS public abstract org.apache.sshd.common.config.keys.impl.AbstractPublicKeyEntryDecoder<%0 extends java.security.PublicKey, %1 extends java.security.PrivateKey>
cons protected init(java.lang.Class<{org.apache.sshd.common.config.keys.impl.AbstractPublicKeyEntryDecoder%0}>,java.lang.Class<{org.apache.sshd.common.config.keys.impl.AbstractPublicKeyEntryDecoder%1}>,java.util.Collection<java.lang.String>)
intf org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<{org.apache.sshd.common.config.keys.impl.AbstractPublicKeyEntryDecoder%0},{org.apache.sshd.common.config.keys.impl.AbstractPublicKeyEntryDecoder%1}>
meth protected final boolean parseBooleanHeader(java.util.Map<java.lang.String,java.lang.String>,java.lang.String,boolean)
supr org.apache.sshd.common.config.keys.impl.AbstractKeyEntryResolver<{org.apache.sshd.common.config.keys.impl.AbstractPublicKeyEntryDecoder%0},{org.apache.sshd.common.config.keys.impl.AbstractPublicKeyEntryDecoder%1}>

CLSS public org.apache.sshd.common.config.keys.impl.DSSPublicKeyEntryDecoder
cons public init()
fld public final static org.apache.sshd.common.config.keys.impl.DSSPublicKeyEntryDecoder INSTANCE
meth public java.lang.String encodePublicKey(java.io.OutputStream,java.security.interfaces.DSAPublicKey) throws java.io.IOException
meth public java.security.KeyFactory getKeyFactoryInstance() throws java.security.GeneralSecurityException
meth public java.security.KeyPairGenerator getKeyPairGenerator() throws java.security.GeneralSecurityException
meth public java.security.interfaces.DSAPrivateKey clonePrivateKey(java.security.interfaces.DSAPrivateKey) throws java.security.GeneralSecurityException
meth public java.security.interfaces.DSAPublicKey clonePublicKey(java.security.interfaces.DSAPublicKey) throws java.security.GeneralSecurityException
meth public java.security.interfaces.DSAPublicKey decodePublicKey(org.apache.sshd.common.session.SessionContext,java.lang.String,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.impl.AbstractPublicKeyEntryDecoder<java.security.interfaces.DSAPublicKey,java.security.interfaces.DSAPrivateKey>

CLSS public org.apache.sshd.common.config.keys.impl.ECDSAPublicKeyEntryDecoder
cons public init()
fld public final static byte ECPOINT_COMPRESSED_VARIANT_2 = 2
fld public final static byte ECPOINT_COMPRESSED_VARIANT_3 = 2
fld public final static byte ECPOINT_UNCOMPRESSED_FORM_INDICATOR = 4
fld public final static int MAX_ALLOWED_POINT_SIZE = 32767
fld public final static int MAX_CURVE_NAME_LENGTH = 1024
fld public final static org.apache.sshd.common.config.keys.impl.ECDSAPublicKeyEntryDecoder INSTANCE
meth public java.lang.String encodePublicKey(java.io.OutputStream,java.security.interfaces.ECPublicKey) throws java.io.IOException
meth public java.security.KeyFactory getKeyFactoryInstance() throws java.security.GeneralSecurityException
meth public java.security.KeyPair generateKeyPair(int) throws java.security.GeneralSecurityException
meth public java.security.KeyPairGenerator getKeyPairGenerator() throws java.security.GeneralSecurityException
meth public java.security.interfaces.ECPrivateKey clonePrivateKey(java.security.interfaces.ECPrivateKey) throws java.security.GeneralSecurityException
meth public java.security.interfaces.ECPublicKey clonePublicKey(java.security.interfaces.ECPublicKey) throws java.security.GeneralSecurityException
meth public java.security.interfaces.ECPublicKey decodePublicKey(org.apache.sshd.common.session.SessionContext,java.lang.String,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.impl.AbstractPublicKeyEntryDecoder<java.security.interfaces.ECPublicKey,java.security.interfaces.ECPrivateKey>

CLSS public org.apache.sshd.common.config.keys.impl.OpenSSHCertificateDecoder
cons public init()
fld public final static org.apache.sshd.common.config.keys.impl.OpenSSHCertificateDecoder INSTANCE
meth public java.lang.String encodePublicKey(java.io.OutputStream,org.apache.sshd.common.config.keys.OpenSshCertificate) throws java.io.IOException
meth public java.security.KeyFactory getKeyFactoryInstance() throws java.security.GeneralSecurityException
meth public java.security.KeyPairGenerator getKeyPairGenerator() throws java.security.GeneralSecurityException
meth public org.apache.sshd.common.config.keys.OpenSshCertificate clonePrivateKey(org.apache.sshd.common.config.keys.OpenSshCertificate) throws java.security.GeneralSecurityException
meth public org.apache.sshd.common.config.keys.OpenSshCertificate clonePublicKey(org.apache.sshd.common.config.keys.OpenSshCertificate) throws java.security.GeneralSecurityException
meth public org.apache.sshd.common.config.keys.OpenSshCertificate decodePublicKey(org.apache.sshd.common.session.SessionContext,java.lang.String,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.impl.AbstractPublicKeyEntryDecoder<org.apache.sshd.common.config.keys.OpenSshCertificate,org.apache.sshd.common.config.keys.OpenSshCertificate>

CLSS public org.apache.sshd.common.config.keys.impl.RSAPublicKeyDecoder
cons public init()
fld public final static org.apache.sshd.common.config.keys.impl.RSAPublicKeyDecoder INSTANCE
meth public java.lang.String encodePublicKey(java.io.OutputStream,java.security.interfaces.RSAPublicKey) throws java.io.IOException
meth public java.security.KeyFactory getKeyFactoryInstance() throws java.security.GeneralSecurityException
meth public java.security.KeyPairGenerator getKeyPairGenerator() throws java.security.GeneralSecurityException
meth public java.security.interfaces.RSAPrivateKey clonePrivateKey(java.security.interfaces.RSAPrivateKey) throws java.security.GeneralSecurityException
meth public java.security.interfaces.RSAPublicKey clonePublicKey(java.security.interfaces.RSAPublicKey) throws java.security.GeneralSecurityException
meth public java.security.interfaces.RSAPublicKey decodePublicKey(org.apache.sshd.common.session.SessionContext,java.lang.String,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.impl.AbstractPublicKeyEntryDecoder<java.security.interfaces.RSAPublicKey,java.security.interfaces.RSAPrivateKey>

CLSS public org.apache.sshd.common.config.keys.impl.SkECDSAPublicKeyEntryDecoder
cons public init()
fld public final static int MAX_APP_NAME_LENGTH = 1024
fld public final static java.lang.String KEY_TYPE = "sk-ecdsa-sha2-nistp256@openssh.com"
fld public final static org.apache.sshd.common.config.keys.impl.SkECDSAPublicKeyEntryDecoder INSTANCE
meth public java.lang.String encodePublicKey(java.io.OutputStream,org.apache.sshd.common.config.keys.u2f.SkEcdsaPublicKey) throws java.io.IOException
meth public java.security.KeyFactory getKeyFactoryInstance()
meth public java.security.KeyPair generateKeyPair(int)
meth public java.security.KeyPairGenerator getKeyPairGenerator()
meth public java.security.PrivateKey clonePrivateKey(java.security.PrivateKey)
meth public org.apache.sshd.common.config.keys.u2f.SkEcdsaPublicKey clonePublicKey(org.apache.sshd.common.config.keys.u2f.SkEcdsaPublicKey) throws java.security.GeneralSecurityException
meth public org.apache.sshd.common.config.keys.u2f.SkEcdsaPublicKey decodePublicKey(org.apache.sshd.common.session.SessionContext,java.lang.String,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.impl.AbstractPublicKeyEntryDecoder<org.apache.sshd.common.config.keys.u2f.SkEcdsaPublicKey,java.security.PrivateKey>
hfds NO_TOUCH_REQUIRED_HEADER,VERIFY_REQUIRED_HEADER

CLSS public org.apache.sshd.common.config.keys.impl.SkED25519PublicKeyEntryDecoder
cons public init()
fld public final static int MAX_APP_NAME_LENGTH = 1024
fld public final static java.lang.String KEY_TYPE = "sk-ssh-ed25519@openssh.com"
fld public final static org.apache.sshd.common.config.keys.impl.SkED25519PublicKeyEntryDecoder INSTANCE
meth public java.lang.String encodePublicKey(java.io.OutputStream,org.apache.sshd.common.config.keys.u2f.SkED25519PublicKey) throws java.io.IOException
meth public java.security.KeyFactory getKeyFactoryInstance()
meth public java.security.KeyPair generateKeyPair(int)
meth public java.security.KeyPairGenerator getKeyPairGenerator()
meth public java.security.PrivateKey clonePrivateKey(java.security.PrivateKey)
meth public org.apache.sshd.common.config.keys.u2f.SkED25519PublicKey clonePublicKey(org.apache.sshd.common.config.keys.u2f.SkED25519PublicKey)
meth public org.apache.sshd.common.config.keys.u2f.SkED25519PublicKey decodePublicKey(org.apache.sshd.common.session.SessionContext,java.lang.String,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.impl.AbstractPublicKeyEntryDecoder<org.apache.sshd.common.config.keys.u2f.SkED25519PublicKey,java.security.PrivateKey>
hfds NO_TOUCH_REQUIRED_HEADER,VERIFY_REQUIRED_HEADER

CLSS public org.apache.sshd.common.config.keys.loader.AESPrivateKeyObfuscator
cons public init()
fld public final static java.lang.String CIPHER_NAME = "AES"
fld public final static org.apache.sshd.common.config.keys.loader.AESPrivateKeyObfuscator INSTANCE
meth protected int resolveInitializationVectorLength(org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext) throws java.security.GeneralSecurityException
meth protected int resolveKeyLength(org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext) throws java.security.GeneralSecurityException
meth protected org.apache.sshd.common.cipher.CipherInformation resolveCipherInformation(int,java.lang.String)
meth public byte[] applyPrivateKeyCipher(byte[],org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext,boolean) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.List<java.lang.Integer> getSupportedKeySizes()
meth public static java.util.List<java.lang.Integer> getAvailableKeyLengths()
meth public static java.util.function.Predicate<org.apache.sshd.common.cipher.CipherInformation> createCipherSelector(int,java.lang.String)
supr org.apache.sshd.common.config.keys.loader.AbstractPrivateKeyObfuscator
hcls LazyKeyLengthsHolder

CLSS public abstract org.apache.sshd.common.config.keys.loader.AbstractKeyPairResourceParser
cons protected init(java.util.List<java.lang.String>,java.util.List<java.lang.String>)
intf org.apache.sshd.common.config.keys.loader.KeyPairResourceParser
meth protected java.util.Map$Entry<java.util.Map<java.lang.String,java.lang.String>,java.util.List<java.lang.String>> separateDataLinesFromHeaders(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,java.util.List<java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public abstract java.util.Collection<java.security.KeyPair> extractKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public boolean canExtractKeyPairs(org.apache.sshd.common.NamedResource,java.util.List<java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> extractKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,byte[],java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> extractKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.util.List<java.lang.String>,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> loadKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.config.keys.FilePasswordProvider,java.util.List<java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.List<java.lang.String> getBeginners()
meth public java.util.List<java.lang.String> getEnders()
meth public java.util.List<java.util.List<java.lang.String>> getEndingMarkers()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds beginners,enders,endingMarkers

CLSS public abstract org.apache.sshd.common.config.keys.loader.AbstractPrivateKeyObfuscator
cons protected init(java.lang.String)
intf org.apache.sshd.common.config.keys.loader.PrivateKeyObfuscator
meth protected abstract int resolveInitializationVectorLength(org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext) throws java.security.GeneralSecurityException
meth protected abstract int resolveKeyLength(org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext) throws java.security.GeneralSecurityException
meth protected byte[] applyPrivateKeyCipher(byte[],org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext,int,byte[],boolean) throws java.io.IOException,java.security.GeneralSecurityException
meth protected byte[] deriveEncryptionKey(org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext,int) throws java.io.IOException,java.security.GeneralSecurityException
meth public <%0 extends java.lang.Appendable> {%%0} appendPrivateKeyEncryptionContext({%%0},org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext) throws java.io.IOException
meth public byte[] generateInitializationVector(org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext) throws java.security.GeneralSecurityException
meth public final java.lang.String getCipherName()
supr java.lang.Object
hfds algName

CLSS public org.apache.sshd.common.config.keys.loader.DESPrivateKeyObfuscator
cons public init()
fld public final static int DEFAULT_KEY_LENGTH = 24
fld public final static java.util.List<java.lang.Integer> AVAILABLE_KEY_LENGTHS
fld public final static org.apache.sshd.common.config.keys.loader.DESPrivateKeyObfuscator INSTANCE
meth protected int resolveInitializationVectorLength(org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext) throws java.security.GeneralSecurityException
meth protected int resolveKeyLength(org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext) throws java.security.GeneralSecurityException
meth public byte[] applyPrivateKeyCipher(byte[],org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext,boolean) throws java.io.IOException,java.security.GeneralSecurityException
meth public final static org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext resolveEffectiveContext(org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext)
meth public java.util.List<java.lang.Integer> getSupportedKeySizes()
supr org.apache.sshd.common.config.keys.loader.AbstractPrivateKeyObfuscator

CLSS public org.apache.sshd.common.config.keys.loader.FileWatcherKeyPairResourceLoader
cons public !varargs init(java.nio.file.Path,org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader,java.nio.file.LinkOption[])
cons public init(java.nio.file.Path,org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader)
fld protected final java.util.concurrent.atomic.AtomicReference<java.util.Collection<java.security.KeyPair>> keysHolder
intf org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader
meth protected java.util.Collection<java.security.KeyPair> reloadKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.config.keys.FilePasswordProvider,java.util.List<java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> loadKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.config.keys.FilePasswordProvider,java.util.List<java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader getKeyPairResourceLoader()
meth public void setKeyPairResourceLoader(org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader)
supr org.apache.sshd.common.util.io.ModifiableFileWatcher
hfds delegateLoader

CLSS public abstract interface org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader
 anno 0 java.lang.FunctionalInterface()
fld public final static int MAX_CIPHER_NAME_LENGTH = 256
fld public final static int MAX_KEY_COMMENT_LENGTH = 1024
fld public final static int MAX_KEY_TYPE_NAME_LENGTH = 256
fld public final static int MAX_PRIVATE_KEY_DATA_SIZE = 262136
fld public final static int MAX_PUBLIC_KEY_DATA_SIZE = 65534
fld public final static org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader EMPTY
meth public !varargs java.util.Collection<java.security.KeyPair> loadKeyPairs(org.apache.sshd.common.session.SessionContext,java.nio.file.Path,org.apache.sshd.common.config.keys.FilePasswordProvider,java.nio.charset.Charset,java.nio.file.OpenOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public !varargs java.util.Collection<java.security.KeyPair> loadKeyPairs(org.apache.sshd.common.session.SessionContext,java.nio.file.Path,org.apache.sshd.common.config.keys.FilePasswordProvider,java.nio.file.OpenOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public abstract java.util.Collection<java.security.KeyPair> loadKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.config.keys.FilePasswordProvider,java.util.List<java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> loadKeyPairs(org.apache.sshd.common.session.SessionContext,java.net.URL,org.apache.sshd.common.config.keys.FilePasswordProvider) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> loadKeyPairs(org.apache.sshd.common.session.SessionContext,java.net.URL,org.apache.sshd.common.config.keys.FilePasswordProvider,java.nio.charset.Charset) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> loadKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.BufferedReader) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> loadKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> loadKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream,java.nio.charset.Charset) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> loadKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.Reader) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> loadKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.config.keys.FilePasswordProvider,java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> loadKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.util.io.resource.IoResource<?>,org.apache.sshd.common.config.keys.FilePasswordProvider) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> loadKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.util.io.resource.IoResource<?>,org.apache.sshd.common.config.keys.FilePasswordProvider,java.nio.charset.Charset) throws java.io.IOException,java.security.GeneralSecurityException

CLSS public abstract interface org.apache.sshd.common.config.keys.loader.KeyPairResourceParser
fld public final static org.apache.sshd.common.config.keys.loader.KeyPairResourceParser EMPTY
intf org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader
meth public !varargs static org.apache.sshd.common.config.keys.loader.KeyPairResourceParser aggregate(org.apache.sshd.common.config.keys.loader.KeyPairResourceParser[])
meth public abstract boolean canExtractKeyPairs(org.apache.sshd.common.NamedResource,java.util.List<java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public static boolean containsMarkerLine(java.util.List<java.lang.String>,java.lang.String)
meth public static boolean containsMarkerLine(java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth public static byte[] extractDataBytes(java.util.Collection<java.lang.String>)
meth public static java.lang.String joinDataLines(java.util.Collection<java.lang.String>)
meth public static java.util.AbstractMap$SimpleImmutableEntry<java.lang.Integer,java.lang.Integer> findMarkerLine(java.util.List<java.lang.String>,int,java.util.List<java.lang.String>)
meth public static java.util.AbstractMap$SimpleImmutableEntry<java.lang.Integer,java.lang.Integer> findMarkerLine(java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth public static org.apache.sshd.common.config.keys.loader.KeyPairResourceParser aggregate(java.util.Collection<? extends org.apache.sshd.common.config.keys.loader.KeyPairResourceParser>)

CLSS public org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext
cons public init()
cons public init(java.lang.String)
fld public final static java.lang.String DEFAULT_CIPHER_MODE = "CBC"
intf java.lang.Cloneable
intf org.apache.sshd.common.auth.MutablePassword
meth public !varargs void setInitVector(byte[])
meth public boolean equals(java.lang.Object)
meth public byte[] getInitVector()
meth public final static <%0 extends org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext> {%%0} initializeObfuscator({%%0},org.apache.sshd.common.config.keys.loader.PrivateKeyObfuscator,java.lang.String)
meth public final static <%0 extends org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext> {%%0} parseAlgorithmInfo({%%0},java.lang.String)
meth public final static java.util.List<org.apache.sshd.common.config.keys.loader.PrivateKeyObfuscator> getRegisteredPrivateKeyObfuscators()
meth public final static java.util.NavigableSet<java.lang.String> getRegisteredPrivateKeyObfuscatorCiphers()
meth public final static org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext newPrivateKeyEncryptionContext(org.apache.sshd.common.config.keys.loader.PrivateKeyObfuscator,java.lang.String)
meth public final static org.apache.sshd.common.config.keys.loader.PrivateKeyObfuscator getRegisteredPrivateKeyObfuscator(java.lang.String)
meth public int hashCode()
meth public java.lang.String getCipherMode()
meth public java.lang.String getCipherName()
meth public java.lang.String getCipherType()
meth public java.lang.String getPassword()
meth public java.lang.String toString()
meth public org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext clone()
meth public org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext parseAlgorithmInfo(java.lang.String)
meth public org.apache.sshd.common.config.keys.loader.PrivateKeyObfuscator getPrivateKeyObfuscator()
meth public org.apache.sshd.common.config.keys.loader.PrivateKeyObfuscator resolvePrivateKeyObfuscator()
meth public static boolean unregisterPrivateKeyObfuscator(org.apache.sshd.common.config.keys.loader.PrivateKeyObfuscator)
meth public static org.apache.sshd.common.config.keys.loader.PrivateKeyObfuscator registerPrivateKeyObfuscator(java.lang.String,org.apache.sshd.common.config.keys.loader.PrivateKeyObfuscator)
meth public static org.apache.sshd.common.config.keys.loader.PrivateKeyObfuscator registerPrivateKeyObfuscator(org.apache.sshd.common.config.keys.loader.PrivateKeyObfuscator)
meth public static org.apache.sshd.common.config.keys.loader.PrivateKeyObfuscator unregisterPrivateKeyObfuscator(java.lang.String)
meth public void setCipherMode(java.lang.String)
meth public void setCipherName(java.lang.String)
meth public void setCipherType(java.lang.String)
meth public void setPassword(java.lang.String)
meth public void setPrivateKeyObfuscator(org.apache.sshd.common.config.keys.loader.PrivateKeyObfuscator)
supr java.lang.Object
hfds OBFUSCATORS,cipherMode,cipherName,cipherType,initVector,obfuscator,password

CLSS public abstract interface org.apache.sshd.common.config.keys.loader.PrivateKeyObfuscator
meth public abstract <%0 extends java.lang.Appendable> {%%0} appendPrivateKeyEncryptionContext({%%0},org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext) throws java.io.IOException
meth public abstract byte[] applyPrivateKeyCipher(byte[],org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext,boolean) throws java.io.IOException,java.security.GeneralSecurityException
meth public abstract byte[] generateInitializationVector(org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext) throws java.security.GeneralSecurityException
meth public abstract java.lang.String getCipherName()
meth public abstract java.util.List<java.lang.Integer> getSupportedKeySizes()

CLSS public org.apache.sshd.common.config.keys.loader.openssh.OpenSSHDSSPrivateKeyEntryDecoder
cons public init()
fld public final static org.apache.sshd.common.config.keys.loader.openssh.OpenSSHDSSPrivateKeyEntryDecoder INSTANCE
meth public boolean isPublicKeyRecoverySupported()
meth public java.lang.String encodePrivateKey(org.apache.sshd.common.util.io.output.SecureByteArrayOutputStream,java.security.interfaces.DSAPrivateKey,java.security.interfaces.DSAPublicKey) throws java.io.IOException
meth public java.security.KeyFactory getKeyFactoryInstance() throws java.security.GeneralSecurityException
meth public java.security.KeyPairGenerator getKeyPairGenerator() throws java.security.GeneralSecurityException
meth public java.security.interfaces.DSAPrivateKey clonePrivateKey(java.security.interfaces.DSAPrivateKey) throws java.security.GeneralSecurityException
meth public java.security.interfaces.DSAPrivateKey decodePrivateKey(org.apache.sshd.common.session.SessionContext,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.security.interfaces.DSAPublicKey clonePublicKey(java.security.interfaces.DSAPublicKey) throws java.security.GeneralSecurityException
meth public java.security.interfaces.DSAPublicKey recoverPublicKey(java.security.interfaces.DSAPrivateKey) throws java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.impl.AbstractPrivateKeyEntryDecoder<java.security.interfaces.DSAPublicKey,java.security.interfaces.DSAPrivateKey>

CLSS public org.apache.sshd.common.config.keys.loader.openssh.OpenSSHECDSAPrivateKeyEntryDecoder
cons public init()
fld public final static org.apache.sshd.common.config.keys.loader.openssh.OpenSSHECDSAPrivateKeyEntryDecoder INSTANCE
meth public java.lang.String encodePrivateKey(org.apache.sshd.common.util.io.output.SecureByteArrayOutputStream,java.security.interfaces.ECPrivateKey,java.security.interfaces.ECPublicKey) throws java.io.IOException
meth public java.security.KeyFactory getKeyFactoryInstance() throws java.security.GeneralSecurityException
meth public java.security.KeyPair generateKeyPair(int) throws java.security.GeneralSecurityException
meth public java.security.KeyPairGenerator getKeyPairGenerator() throws java.security.GeneralSecurityException
meth public java.security.interfaces.ECPrivateKey clonePrivateKey(java.security.interfaces.ECPrivateKey) throws java.security.GeneralSecurityException
meth public java.security.interfaces.ECPrivateKey decodePrivateKey(org.apache.sshd.common.session.SessionContext,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.security.interfaces.ECPublicKey clonePublicKey(java.security.interfaces.ECPublicKey) throws java.security.GeneralSecurityException
meth public java.security.interfaces.ECPublicKey recoverPublicKey(java.security.interfaces.ECPrivateKey) throws java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.impl.AbstractPrivateKeyEntryDecoder<java.security.interfaces.ECPublicKey,java.security.interfaces.ECPrivateKey>

CLSS public abstract interface org.apache.sshd.common.config.keys.loader.openssh.OpenSSHKdfOptions
fld public final static int MAX_KDF_NAME_LENGTH = 1024
fld public final static int MAX_KDF_OPTIONS_SIZE = 32767
fld public final static java.lang.String NONE_KDF = "none"
fld public final static java.util.function.Predicate<java.lang.String> IS_NONE_KDF
intf org.apache.sshd.common.NamedResource
intf org.apache.sshd.common.config.keys.loader.openssh.OpenSSHKeyDecryptor
meth public abstract void initialize(java.lang.String,byte[]) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.common.config.keys.loader.openssh.OpenSSHKeyDecryptor
meth public abstract boolean isEncrypted()
meth public abstract byte[] decodePrivateKeyBytes(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.cipher.CipherFactory,byte[],java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException

CLSS public org.apache.sshd.common.config.keys.loader.openssh.OpenSSHKeyPairResourceParser
cons public init()
fld public final static java.lang.String AUTH_MAGIC = "openssh-key-v1"
fld public final static java.lang.String BEGIN_MARKER = "BEGIN OPENSSH PRIVATE KEY"
fld public final static java.lang.String END_MARKER = "END OPENSSH PRIVATE KEY"
fld public final static java.util.List<java.lang.String> BEGINNERS
fld public final static java.util.List<java.lang.String> ENDERS
fld public final static org.apache.sshd.common.config.keys.loader.openssh.OpenSSHKeyPairResourceParser INSTANCE
meth protected <%0 extends java.io.InputStream> {%%0} validateStreamMagicMarker(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,{%%0}) throws java.io.IOException
meth protected java.security.PublicKey readPublicKey(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.config.keys.loader.openssh.OpenSSHParserContext,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.util.List<java.security.KeyPair> readPrivateKeys(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.config.keys.loader.openssh.OpenSSHParserContext,java.util.Collection<? extends java.security.PublicKey>,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream) throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.util.Map$Entry<java.security.PrivateKey,java.lang.String> readPrivateKey(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.config.keys.loader.openssh.OpenSSHParserContext,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream) throws java.io.IOException,java.security.GeneralSecurityException
meth protected org.apache.sshd.common.config.keys.loader.openssh.OpenSSHKdfOptions resolveKdfOptions(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> extractKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public static org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder<?,?> getPrivateKeyEntryDecoder(java.lang.Class<?>)
meth public static org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder<?,?> getPrivateKeyEntryDecoder(java.lang.String)
meth public static org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder<?,?> getPrivateKeyEntryDecoder(java.security.Key)
meth public static org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder<?,?> getPrivateKeyEntryDecoder(java.security.KeyPair)
meth public static void registerPrivateKeyEntryDecoder(org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder<?,?>)
supr org.apache.sshd.common.config.keys.loader.AbstractKeyPairResourceParser
hfds AUTH_MAGIC_BYTES,BY_KEY_CLASS_DECODERS_MAP,BY_KEY_TYPE_DECODERS_MAP

CLSS public org.apache.sshd.common.config.keys.loader.openssh.OpenSSHParserContext
cons public init()
cons public init(java.lang.String,org.apache.sshd.common.config.keys.loader.openssh.OpenSSHKdfOptions)
fld public final static java.lang.String NONE_CIPHER = "none"
fld public final static java.util.function.Predicate<java.lang.String> IS_NONE_CIPHER
meth public boolean isEncrypted()
meth public java.lang.String getCipherName()
meth public java.lang.String toString()
meth public org.apache.sshd.common.config.keys.loader.openssh.OpenSSHKdfOptions getKdfOptions()
meth public void setCipherName(java.lang.String)
meth public void setKdfOptions(org.apache.sshd.common.config.keys.loader.openssh.OpenSSHKdfOptions)
supr java.lang.Object
hfds cipherName,kdfOptions

CLSS public org.apache.sshd.common.config.keys.loader.openssh.OpenSSHRSAPrivateKeyDecoder
cons public init()
fld public final static java.math.BigInteger DEFAULT_PUBLIC_EXPONENT
fld public final static org.apache.sshd.common.config.keys.loader.openssh.OpenSSHRSAPrivateKeyDecoder INSTANCE
meth public boolean isPublicKeyRecoverySupported()
meth public java.lang.String encodePrivateKey(org.apache.sshd.common.util.io.output.SecureByteArrayOutputStream,java.security.interfaces.RSAPrivateKey,java.security.interfaces.RSAPublicKey) throws java.io.IOException
meth public java.security.KeyFactory getKeyFactoryInstance() throws java.security.GeneralSecurityException
meth public java.security.KeyPairGenerator getKeyPairGenerator() throws java.security.GeneralSecurityException
meth public java.security.interfaces.RSAPrivateKey clonePrivateKey(java.security.interfaces.RSAPrivateKey) throws java.security.GeneralSecurityException
meth public java.security.interfaces.RSAPrivateKey decodePrivateKey(org.apache.sshd.common.session.SessionContext,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.security.interfaces.RSAPublicKey clonePublicKey(java.security.interfaces.RSAPublicKey) throws java.security.GeneralSecurityException
meth public java.security.interfaces.RSAPublicKey recoverPublicKey(java.security.interfaces.RSAPrivateKey) throws java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.impl.AbstractPrivateKeyEntryDecoder<java.security.interfaces.RSAPublicKey,java.security.interfaces.RSAPrivateKey>

CLSS public org.apache.sshd.common.config.keys.loader.openssh.kdf.BCrypt
cons public init()
meth public byte[] crypt_raw(byte[],byte[],int,int[])
meth public static boolean checkpw(java.lang.String,java.lang.String)
meth public static java.lang.String gensalt()
meth public static java.lang.String gensalt(int)
meth public static java.lang.String gensalt(int,java.security.SecureRandom)
meth public static java.lang.String hashpw(java.lang.String,java.lang.String)
meth public void hash(byte[],byte[],byte[])
meth public void pbkdf(byte[],byte[],int,byte[])
supr java.lang.Object
hfds BCRYPT_SALT_LEN,BLOWFISH_NUM_ROUNDS,GENSALT_DEFAULT_LOG2_ROUNDS,P,P_orig,S,S_orig,base64_code,bf_crypt_ciphertext,index_64,openbsd_iv

CLSS public org.apache.sshd.common.config.keys.loader.openssh.kdf.BCryptKdfOptions
cons public init()
fld public final static int DEFAULT_MAX_ROUNDS = 255
fld public final static java.lang.String NAME = "bcrypt"
innr public static BCryptBadRoundsException
intf org.apache.sshd.common.config.keys.loader.openssh.OpenSSHKdfOptions
meth protected void bcryptKdf(byte[],byte[]) throws java.io.IOException,java.security.GeneralSecurityException
meth protected void initialize(java.io.InputStream,int) throws java.io.IOException
meth public boolean equals(java.lang.Object)
meth public boolean isEncrypted()
meth public byte[] decodePrivateKeyBytes(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.cipher.CipherFactory,byte[],java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException
meth public byte[] getSalt()
meth public final java.lang.String getName()
meth public int getNumRounds()
meth public int hashCode()
meth public java.lang.String toString()
meth public static int getMaxAllowedRounds()
meth public static void setMaxAllowedRounds(int)
meth public void initialize(java.lang.String,byte[]) throws java.io.IOException
meth public void setNumRounds(int)
meth public void setSalt(byte[])
supr java.lang.Object
hfds MAX_ROUNDS_HOLDER,numRounds,salt

CLSS public static org.apache.sshd.common.config.keys.loader.openssh.kdf.BCryptKdfOptions$BCryptBadRoundsException
 outer org.apache.sshd.common.config.keys.loader.openssh.kdf.BCryptKdfOptions
cons public init(int)
cons public init(int,java.lang.String)
cons public init(int,java.lang.String,java.lang.Throwable)
meth public int getRounds()
supr org.apache.sshd.common.RuntimeSshException
hfds rounds,serialVersionUID

CLSS public org.apache.sshd.common.config.keys.loader.openssh.kdf.RawKdfOptions
cons public init()
intf org.apache.sshd.common.config.keys.loader.openssh.OpenSSHKdfOptions
meth public boolean equals(java.lang.Object)
meth public boolean isEncrypted()
meth public byte[] decodePrivateKeyBytes(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.cipher.CipherFactory,byte[],java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException
meth public byte[] getOptions()
meth public int hashCode()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public void initialize(java.lang.String,byte[]) throws java.io.IOException
meth public void setName(java.lang.String)
meth public void setOptions(byte[])
supr java.lang.Object
hfds name,options

CLSS public abstract org.apache.sshd.common.config.keys.loader.pem.AbstractPEMResourceKeyPairParser
cons protected init(java.lang.String,java.lang.String,java.util.List<java.lang.String>,java.util.List<java.lang.String>)
intf org.apache.sshd.common.config.keys.loader.pem.KeyPairPEMResourceParser
meth protected byte[] applyPrivateKeyCipher(byte[],org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext,boolean) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.lang.String getAlgorithm()
meth public java.lang.String getAlgorithmIdentifier()
meth public java.util.Collection<java.security.KeyPair> extractKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.util.List<java.lang.String>,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.loader.AbstractKeyPairResourceParser
hfds algId,algo

CLSS public org.apache.sshd.common.config.keys.loader.pem.DSSPEMResourceKeyPairParser
cons public init()
fld public final static java.lang.String BEGIN_MARKER = "BEGIN DSA PRIVATE KEY"
fld public final static java.lang.String DSS_OID = "1.2.840.10040.4.1"
fld public final static java.lang.String END_MARKER = "END DSA PRIVATE KEY"
fld public final static java.util.List<java.lang.String> BEGINNERS
fld public final static java.util.List<java.lang.String> ENDERS
fld public final static org.apache.sshd.common.config.keys.loader.pem.DSSPEMResourceKeyPairParser INSTANCE
meth public java.util.Collection<java.security.KeyPair> extractKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.security.KeyPair decodeDSSKeyPair(java.security.KeyFactory,java.io.InputStream,boolean) throws java.io.IOException,java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.loader.pem.AbstractPEMResourceKeyPairParser

CLSS public org.apache.sshd.common.config.keys.loader.pem.ECDSAPEMResourceKeyPairParser
cons public init()
fld public final static java.lang.String BEGIN_MARKER = "BEGIN EC PRIVATE KEY"
fld public final static java.lang.String ECDSA_OID = "1.2.840.10045.2.1"
fld public final static java.lang.String END_MARKER = "END EC PRIVATE KEY"
fld public final static java.util.List<java.lang.String> BEGINNERS
fld public final static java.util.List<java.lang.String> ENDERS
fld public final static org.apache.sshd.common.config.keys.loader.pem.ECDSAPEMResourceKeyPairParser INSTANCE
meth public final static java.security.spec.ECPoint decodeECPointData(org.apache.sshd.common.util.io.der.ASN1Object) throws java.io.IOException
meth public final static java.security.spec.ECPoint decodeECPublicKeyValue(org.apache.sshd.common.util.io.der.ASN1Object) throws java.io.IOException
meth public final static java.security.spec.ECPoint decodeECPublicKeyValue(org.apache.sshd.common.util.io.der.DERParser) throws java.io.IOException
meth public java.util.Collection<java.security.KeyPair> extractKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.security.KeyPair parseECKeyPair(java.io.InputStream,boolean) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.security.KeyPair parseECKeyPair(org.apache.sshd.common.cipher.ECCurves,org.apache.sshd.common.util.io.der.DERParser) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.util.Map$Entry<java.security.spec.ECPrivateKeySpec,org.apache.sshd.common.util.io.der.ASN1Object> decodeECPrivateKeySpec(org.apache.sshd.common.cipher.ECCurves,org.apache.sshd.common.util.io.der.DERParser) throws java.io.IOException
meth public static java.util.Map$Entry<java.security.spec.ECPublicKeySpec,java.security.spec.ECPrivateKeySpec> decodeECPrivateKeySpec(org.apache.sshd.common.cipher.ECCurves,org.apache.sshd.common.util.io.der.ASN1Object) throws java.io.IOException
meth public static java.util.Map$Entry<org.apache.sshd.common.cipher.ECCurves,org.apache.sshd.common.util.io.der.ASN1Object> parseCurveParameter(org.apache.sshd.common.util.io.der.ASN1Object) throws java.io.IOException
meth public static java.util.Map$Entry<org.apache.sshd.common.cipher.ECCurves,org.apache.sshd.common.util.io.der.ASN1Object> parseCurveParameter(org.apache.sshd.common.util.io.der.DERParser) throws java.io.IOException
supr org.apache.sshd.common.config.keys.loader.pem.AbstractPEMResourceKeyPairParser

CLSS public abstract interface org.apache.sshd.common.config.keys.loader.pem.KeyPairPEMResourceParser
intf org.apache.sshd.common.AlgorithmNameProvider
intf org.apache.sshd.common.config.keys.loader.KeyPairResourceParser
meth public abstract java.lang.String getAlgorithmIdentifier()

CLSS public final org.apache.sshd.common.config.keys.loader.pem.PEMResourceParserUtils
fld public final static org.apache.sshd.common.config.keys.loader.KeyPairResourceParser PROXY
meth public static org.apache.sshd.common.config.keys.loader.pem.KeyPairPEMResourceParser getPEMResourceParserByAlgorithm(java.lang.String)
meth public static org.apache.sshd.common.config.keys.loader.pem.KeyPairPEMResourceParser getPEMResourceParserByOid(java.lang.String)
meth public static org.apache.sshd.common.config.keys.loader.pem.KeyPairPEMResourceParser getPEMResourceParserByOidValues(java.util.Collection<? extends java.lang.Number>)
meth public static void registerPEMResourceParser(org.apache.sshd.common.config.keys.loader.pem.KeyPairPEMResourceParser)
supr java.lang.Object
hfds BY_ALGORITHM_MAP,BY_OID_MAP,PROXY_HOLDER

CLSS public org.apache.sshd.common.config.keys.loader.pem.PKCS8PEMResourceKeyPairParser
cons public init()
fld public final static java.lang.String BEGIN_ENCRYPTED_MARKER = "BEGIN ENCRYPTED PRIVATE KEY"
fld public final static java.lang.String BEGIN_MARKER = "BEGIN PRIVATE KEY"
fld public final static java.lang.String END_ENCRYPTED_MARKER = "END ENCRYPTED PRIVATE KEY"
fld public final static java.lang.String END_MARKER = "END PRIVATE KEY"
fld public final static java.lang.String PKCS8_FORMAT = "PKCS#8"
fld public final static java.util.List<java.lang.String> BEGINNERS
fld public final static java.util.List<java.lang.String> ENDERS
fld public final static org.apache.sshd.common.config.keys.loader.pem.PKCS8PEMResourceKeyPairParser INSTANCE
meth public java.util.Collection<java.security.KeyPair> decryptKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,org.apache.sshd.common.config.keys.FilePasswordProvider,byte[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> extractKeyPairs(byte[],org.apache.sshd.common.config.keys.loader.pem.PKCS8PrivateKeyInfo) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> extractKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.security.PrivateKey decodePEMPrivateKeyPKCS8(java.lang.String,byte[]) throws java.security.GeneralSecurityException
meth public static java.security.PrivateKey decodePEMPrivateKeyPKCS8(java.util.List<java.lang.Integer>,byte[]) throws java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.loader.pem.AbstractPEMResourceKeyPairParser

CLSS public org.apache.sshd.common.config.keys.loader.pem.PKCS8PrivateKeyInfo
cons public init()
cons public init(byte[]) throws java.io.IOException
cons public init(org.apache.sshd.common.util.io.der.ASN1Object) throws java.io.IOException
cons public init(org.apache.sshd.common.util.io.der.DERParser) throws java.io.IOException
meth public java.lang.String toString()
meth public java.math.BigInteger getVersion()
meth public java.util.List<java.lang.Integer> getAlgorithmIdentifier()
meth public org.apache.sshd.common.util.io.der.ASN1Object getAlgorithmParameter()
meth public org.apache.sshd.common.util.io.der.ASN1Object getPrivateKeyBytes()
meth public void clear()
meth public void decode(byte[]) throws java.io.IOException
meth public void decode(org.apache.sshd.common.util.io.der.ASN1Object) throws java.io.IOException
meth public void decode(org.apache.sshd.common.util.io.der.DERParser) throws java.io.IOException
meth public void setAlgorithmIdentifier(java.util.List<java.lang.Integer>)
meth public void setAlgorithmParameter(org.apache.sshd.common.util.io.der.ASN1Object)
meth public void setPrivateKeyBytes(org.apache.sshd.common.util.io.der.ASN1Object)
meth public void setVersion(java.math.BigInteger)
supr java.lang.Object
hfds algorithmIdentifier,algorithmParameter,privateKeyBytes,version

CLSS public org.apache.sshd.common.config.keys.loader.pem.RSAPEMResourceKeyPairParser
cons public init()
fld public final static java.lang.String BEGIN_MARKER = "BEGIN RSA PRIVATE KEY"
fld public final static java.lang.String END_MARKER = "END RSA PRIVATE KEY"
fld public final static java.lang.String RSA_OID = "1.2.840.113549.1.1.1"
fld public final static java.util.List<java.lang.String> BEGINNERS
fld public final static java.util.List<java.lang.String> ENDERS
fld public final static org.apache.sshd.common.config.keys.loader.pem.RSAPEMResourceKeyPairParser INSTANCE
meth public java.util.Collection<java.security.KeyPair> extractKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.security.KeyPair decodeRSAKeyPair(java.security.KeyFactory,java.io.InputStream,boolean) throws java.io.IOException,java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.loader.pem.AbstractPEMResourceKeyPairParser

CLSS public org.apache.sshd.common.config.keys.loader.ssh2.Ssh2PublicKeyEntryDecoder
cons public init()
fld public final static char HEADER_CONTINUATION_INDICATOR = '\u005c'
fld public final static java.lang.String BEGIN_MARKER = "BEGIN SSH2 PUBLIC KEY"
fld public final static java.lang.String END_MARKER = "END SSH2 PUBLIC KEY"
fld public final static java.util.List<java.lang.String> START_MARKERS
fld public final static java.util.List<java.lang.String> STOP_MARKERS
fld public final static java.util.NavigableSet<java.lang.String> SUPPORTED_KEY_TYPES
fld public final static org.apache.sshd.common.config.keys.loader.ssh2.Ssh2PublicKeyEntryDecoder INSTANCE
intf org.apache.sshd.common.config.keys.KeyTypeNamesSupport
intf org.apache.sshd.common.config.keys.PublicKeyEntryResolver
intf org.apache.sshd.common.config.keys.PublicKeyRawDataDecoder<java.security.PublicKey>
intf org.apache.sshd.common.config.keys.PublicKeyRawDataReader<java.security.PublicKey>
meth protected java.util.Map$Entry<java.util.Map<java.lang.String,java.lang.String>,java.util.List<java.lang.String>> separateDataLinesFromHeaders(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,java.util.List<java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.security.PublicKey decodePublicKey(org.apache.sshd.common.session.SessionContext,java.lang.String,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.security.PublicKey decodePublicKeyByType(org.apache.sshd.common.session.SessionContext,java.lang.String,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.security.PublicKey readPublicKey(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,byte[],java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.security.PublicKey readPublicKey(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,java.util.List<java.lang.String>,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.security.PublicKey readPublicKey(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.util.List<java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.security.PublicKey resolve(org.apache.sshd.common.session.SessionContext,java.lang.String,byte[],java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.NavigableSet<java.lang.String> getSupportedKeyTypes()
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.common.config.keys.u2f.SecurityKeyPublicKey<%0 extends java.security.PublicKey>
intf org.apache.sshd.common.config.keys.SshPublicKey
meth public abstract boolean isNoTouchRequired()
meth public abstract boolean isVerifyRequired()
meth public abstract java.lang.String getAppName()
meth public abstract {org.apache.sshd.common.config.keys.u2f.SecurityKeyPublicKey%0} getDelegatePublicKey()

CLSS public org.apache.sshd.common.config.keys.u2f.SkED25519PublicKey
cons public init(java.lang.String,boolean,boolean,java.security.PublicKey)
cons public init(java.lang.String,boolean,java.security.PublicKey)
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String ALGORITHM = "ED25519-SK"
intf org.apache.sshd.common.config.keys.u2f.SecurityKeyPublicKey<java.security.PublicKey>
meth public boolean equals(java.lang.Object)
meth public boolean isNoTouchRequired()
meth public boolean isVerifyRequired()
meth public byte[] getEncoded()
meth public int hashCode()
meth public java.lang.String getAlgorithm()
meth public java.lang.String getAppName()
meth public java.lang.String getFormat()
meth public java.lang.String getKeyType()
meth public java.lang.String toString()
meth public java.security.PublicKey getDelegatePublicKey()
supr java.lang.Object
hfds appName,delegatePublicKey,noTouchRequired,serialVersionUID,verifyRequired

CLSS public org.apache.sshd.common.config.keys.u2f.SkEcdsaPublicKey
cons public init(java.lang.String,boolean,boolean,java.security.interfaces.ECPublicKey)
cons public init(java.lang.String,boolean,java.security.interfaces.ECPublicKey)
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String ALGORITHM = "ECDSA-SK"
intf org.apache.sshd.common.config.keys.u2f.SecurityKeyPublicKey<java.security.interfaces.ECPublicKey>
meth public boolean equals(java.lang.Object)
meth public boolean isNoTouchRequired()
meth public boolean isVerifyRequired()
meth public byte[] getEncoded()
meth public int hashCode()
meth public java.lang.String getAlgorithm()
meth public java.lang.String getAppName()
meth public java.lang.String getFormat()
meth public java.lang.String getKeyType()
meth public java.lang.String toString()
meth public java.security.interfaces.ECPublicKey getDelegatePublicKey()
supr java.lang.Object
hfds appName,delegatePublicKey,noTouchRequired,serialVersionUID,verifyRequired

CLSS public abstract interface org.apache.sshd.common.config.keys.writer.KeyPairResourceWriter<%0 extends org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext>
meth public abstract void writePrivateKey(java.security.KeyPair,java.lang.String,{org.apache.sshd.common.config.keys.writer.KeyPairResourceWriter%0},java.io.OutputStream) throws java.io.IOException,java.security.GeneralSecurityException
meth public abstract void writePublicKey(java.security.PublicKey,java.lang.String,java.io.OutputStream) throws java.io.IOException,java.security.GeneralSecurityException
meth public void writePublicKey(java.security.KeyPair,java.lang.String,java.io.OutputStream) throws java.io.IOException,java.security.GeneralSecurityException

CLSS public org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyEncryptionContext
cons public init()
fld public final static int DEFAULT_KDF_ROUNDS = 16
fld public final static java.lang.String AES = "AES"
meth protected java.lang.String getCipherFactoryName()
meth public int getKdfRounds()
meth public java.lang.String getCipherName()
meth public void setCipherName(java.lang.String)
meth public void setKdfRounds(int)
supr org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext
hfds kdfRounds

CLSS public org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter
cons public init()
fld public final static int LINE_LENGTH = 70
fld public final static java.lang.String DASHES = "-----"
fld public final static org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter INSTANCE
innr public static KeyEncryptor
intf org.apache.sshd.common.config.keys.writer.KeyPairResourceWriter<org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyEncryptionContext>
meth public static byte[] encodePrivateKey(java.security.KeyPair,java.lang.String,int,java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException
meth public static byte[] encodePublicKey(java.security.PublicKey,java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.lang.String firstLine(java.lang.String)
meth public static org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyEncryptionContext determineEncryption(org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyEncryptionContext)
meth public static void write(java.io.OutputStream,byte[],int) throws java.io.IOException
meth public static void write(java.io.OutputStream,java.lang.String) throws java.io.IOException
meth public void writePrivateKey(java.security.KeyPair,java.lang.String,org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyEncryptionContext,java.io.OutputStream) throws java.io.IOException,java.security.GeneralSecurityException
meth public void writePublicKey(java.security.PublicKey,java.lang.String,java.io.OutputStream) throws java.io.IOException,java.security.GeneralSecurityException
supr java.lang.Object
hfds VERTICALSPACE

CLSS public static org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter$KeyEncryptor
 outer org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter
cons public init(org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyEncryptionContext)
fld protected final org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyEncryptionContext options
fld public final static int BCRYPT_SALT_LENGTH = 16
meth protected byte[] convert(java.lang.String)
meth protected byte[] deriveEncryptionKey(org.apache.sshd.common.config.keys.loader.PrivateKeyEncryptionContext,int) throws java.io.IOException,java.security.GeneralSecurityException
meth public byte[] getKdfOptions()
supr org.apache.sshd.common.config.keys.loader.AESPrivateKeyObfuscator
hfds kdfOptions

CLSS public org.apache.sshd.common.digest.BaseDigest
cons public init(java.lang.String,int)
intf org.apache.sshd.common.digest.Digest
meth protected java.security.MessageDigest getMessageDigest()
meth public boolean equals(java.lang.Object)
meth public byte[] digest() throws java.lang.Exception
meth public final java.lang.String getAlgorithm()
meth public int compareTo(org.apache.sshd.common.digest.Digest)
meth public int getBlockSize()
meth public int hashCode()
meth public java.lang.String toString()
meth public void init() throws java.lang.Exception
meth public void update(byte[]) throws java.lang.Exception
meth public void update(byte[],int,int) throws java.lang.Exception
supr java.lang.Object
hfds algorithm,bsize,h,md,s

CLSS public final !enum org.apache.sshd.common.digest.BuiltinDigests
fld public final static java.util.Set<org.apache.sshd.common.digest.BuiltinDigests> VALUES
fld public final static org.apache.sshd.common.digest.BuiltinDigests md5
fld public final static org.apache.sshd.common.digest.BuiltinDigests sha1
fld public final static org.apache.sshd.common.digest.BuiltinDigests sha224
fld public final static org.apache.sshd.common.digest.BuiltinDigests sha256
fld public final static org.apache.sshd.common.digest.BuiltinDigests sha384
fld public final static org.apache.sshd.common.digest.BuiltinDigests sha512
innr public final static Constants
intf org.apache.sshd.common.digest.DigestFactory
meth public final boolean isSupported()
meth public final int getBlockSize()
meth public final java.lang.String getAlgorithm()
meth public final java.lang.String getName()
meth public final java.lang.String toString()
meth public final org.apache.sshd.common.digest.Digest create()
meth public static org.apache.sshd.common.digest.BuiltinDigests fromAlgorithm(java.lang.String)
meth public static org.apache.sshd.common.digest.BuiltinDigests fromDigest(org.apache.sshd.common.digest.Digest)
meth public static org.apache.sshd.common.digest.BuiltinDigests fromFactory(org.apache.sshd.common.NamedFactory<? extends org.apache.sshd.common.digest.Digest>)
meth public static org.apache.sshd.common.digest.BuiltinDigests fromFactoryName(java.lang.String)
meth public static org.apache.sshd.common.digest.BuiltinDigests fromString(java.lang.String)
meth public static org.apache.sshd.common.digest.BuiltinDigests valueOf(java.lang.String)
meth public static org.apache.sshd.common.digest.BuiltinDigests[] values()
supr java.lang.Enum<org.apache.sshd.common.digest.BuiltinDigests>
hfds algorithm,blockSize,factoryName,supported

CLSS public final static org.apache.sshd.common.digest.BuiltinDigests$Constants
 outer org.apache.sshd.common.digest.BuiltinDigests
fld public final static java.lang.String MD5 = "md5"
fld public final static java.lang.String SHA1 = "sha1"
fld public final static java.lang.String SHA224 = "sha224"
fld public final static java.lang.String SHA256 = "sha256"
fld public final static java.lang.String SHA384 = "sha384"
fld public final static java.lang.String SHA512 = "sha512"
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.common.digest.Digest
intf java.lang.Comparable<org.apache.sshd.common.digest.Digest>
intf org.apache.sshd.common.digest.DigestInformation
meth public abstract byte[] digest() throws java.lang.Exception
meth public abstract void init() throws java.lang.Exception
meth public abstract void update(byte[]) throws java.lang.Exception
meth public abstract void update(byte[],int,int) throws java.lang.Exception

CLSS public abstract interface org.apache.sshd.common.digest.DigestFactory
intf org.apache.sshd.common.NamedFactory<org.apache.sshd.common.digest.Digest>
intf org.apache.sshd.common.OptionalFeature
intf org.apache.sshd.common.digest.DigestInformation

CLSS public abstract interface org.apache.sshd.common.digest.DigestInformation
intf org.apache.sshd.common.AlgorithmNameProvider
meth public abstract int getBlockSize()

CLSS public final org.apache.sshd.common.digest.DigestUtils
meth public !varargs static byte[] getRawFingerprint(org.apache.sshd.common.digest.Digest,byte[]) throws java.lang.Exception
meth public !varargs static java.lang.String getFingerPrint(org.apache.sshd.common.Factory<? extends org.apache.sshd.common.digest.Digest>,byte[]) throws java.lang.Exception
meth public !varargs static java.lang.String getFingerPrint(org.apache.sshd.common.digest.Digest,byte[]) throws java.lang.Exception
meth public static <%0 extends org.apache.sshd.common.digest.Digest> {%%0} findDigestByAlgorithm(java.lang.String,java.util.Comparator<? super java.lang.String>,java.util.Collection<? extends {%%0}>)
meth public static <%0 extends org.apache.sshd.common.digest.DigestFactory> {%%0} findFactoryByAlgorithm(java.lang.String,java.util.Comparator<? super java.lang.String>,java.util.Collection<? extends {%%0}>)
meth public static boolean checkSupported(java.lang.String)
meth public static byte[] getRawFingerprint(org.apache.sshd.common.digest.Digest,byte[],int,int) throws java.lang.Exception
meth public static java.lang.String getFingerPrint(org.apache.sshd.common.Factory<? extends org.apache.sshd.common.digest.Digest>,byte[],int,int) throws java.lang.Exception
meth public static java.lang.String getFingerPrint(org.apache.sshd.common.Factory<? extends org.apache.sshd.common.digest.Digest>,java.lang.String) throws java.lang.Exception
meth public static java.lang.String getFingerPrint(org.apache.sshd.common.Factory<? extends org.apache.sshd.common.digest.Digest>,java.lang.String,java.nio.charset.Charset) throws java.lang.Exception
meth public static java.lang.String getFingerPrint(org.apache.sshd.common.digest.Digest,byte[],int,int) throws java.lang.Exception
meth public static java.lang.String getFingerPrint(org.apache.sshd.common.digest.Digest,java.lang.String) throws java.lang.Exception
meth public static java.lang.String getFingerPrint(org.apache.sshd.common.digest.Digest,java.lang.String,java.nio.charset.Charset) throws java.lang.Exception
supr java.lang.Object

CLSS abstract interface org.apache.sshd.common.digest.package-info

CLSS public abstract interface org.apache.sshd.common.file.FileSystemAware
 anno 0 java.lang.FunctionalInterface()
meth public abstract void setFileSystem(java.nio.file.FileSystem)
meth public void setFileSystemFactory(org.apache.sshd.common.file.FileSystemFactory,org.apache.sshd.common.session.SessionContext) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.common.file.FileSystemFactory
meth public abstract java.nio.file.FileSystem createFileSystem(org.apache.sshd.common.session.SessionContext) throws java.io.IOException
meth public abstract java.nio.file.Path getUserHomeDir(org.apache.sshd.common.session.SessionContext) throws java.io.IOException

CLSS public org.apache.sshd.common.file.nativefs.NativeFileSystemFactory
cons public init()
cons public init(boolean)
fld public final static java.lang.String DEFAULT_USERS_HOME
fld public final static org.apache.sshd.common.file.nativefs.NativeFileSystemFactory INSTANCE
intf org.apache.sshd.common.file.FileSystemFactory
meth public boolean isCreateHome()
meth public java.lang.String getUsersHomeDir()
meth public java.nio.file.FileSystem createFileSystem(org.apache.sshd.common.session.SessionContext) throws java.io.IOException
meth public java.nio.file.Path getUserHomeDir(org.apache.sshd.common.session.SessionContext) throws java.io.IOException
meth public void setCreateHome(boolean)
meth public void setUsersHomeDir(java.lang.String)
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds createHome,usersHomeDir

CLSS public org.apache.sshd.common.file.nonefs.NoneFileSystem
cons public init()
fld public final static org.apache.sshd.common.file.nonefs.NoneFileSystem INSTANCE
meth public !varargs java.nio.file.Path getPath(java.lang.String,java.lang.String[])
meth public boolean isOpen()
meth public boolean isReadOnly()
meth public java.lang.Iterable<java.nio.file.FileStore> getFileStores()
meth public java.lang.Iterable<java.nio.file.Path> getRootDirectories()
meth public java.lang.String getSeparator()
meth public java.nio.file.PathMatcher getPathMatcher(java.lang.String)
meth public java.nio.file.WatchService newWatchService() throws java.io.IOException
meth public java.nio.file.attribute.UserPrincipalLookupService getUserPrincipalLookupService()
meth public java.nio.file.spi.FileSystemProvider provider()
meth public java.util.Set<java.lang.String> supportedFileAttributeViews()
meth public void close() throws java.io.IOException
supr java.nio.file.FileSystem

CLSS public org.apache.sshd.common.file.nonefs.NoneFileSystemFactory
cons public init()
fld public final static org.apache.sshd.common.file.nonefs.NoneFileSystemFactory INSTANCE
intf org.apache.sshd.common.file.FileSystemFactory
meth public java.nio.file.FileSystem createFileSystem(org.apache.sshd.common.session.SessionContext) throws java.io.IOException
meth public java.nio.file.Path getUserHomeDir(org.apache.sshd.common.session.SessionContext) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.sshd.common.file.nonefs.NoneFileSystemProvider
cons public init()
fld public final static java.lang.String SCHEME = "none"
fld public final static org.apache.sshd.common.file.nonefs.NoneFileSystemProvider INSTANCE
meth public !varargs <%0 extends java.nio.file.attribute.BasicFileAttributes> {%%0} readAttributes(java.nio.file.Path,java.lang.Class<{%%0}>,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs <%0 extends java.nio.file.attribute.FileAttributeView> {%%0} getFileAttributeView(java.nio.file.Path,java.lang.Class<{%%0}>,java.nio.file.LinkOption[])
meth public !varargs java.nio.channels.SeekableByteChannel newByteChannel(java.nio.file.Path,java.util.Set<? extends java.nio.file.OpenOption>,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs java.util.Map<java.lang.String,java.lang.Object> readAttributes(java.nio.file.Path,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void checkAccess(java.nio.file.Path,java.nio.file.AccessMode[]) throws java.io.IOException
meth public !varargs void copy(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption[]) throws java.io.IOException
meth public !varargs void createDirectory(java.nio.file.Path,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs void move(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption[]) throws java.io.IOException
meth public !varargs void setAttribute(java.nio.file.Path,java.lang.String,java.lang.Object,java.nio.file.LinkOption[]) throws java.io.IOException
meth public boolean isHidden(java.nio.file.Path) throws java.io.IOException
meth public boolean isSameFile(java.nio.file.Path,java.nio.file.Path) throws java.io.IOException
meth public java.lang.String getScheme()
meth public java.nio.file.DirectoryStream<java.nio.file.Path> newDirectoryStream(java.nio.file.Path,java.nio.file.DirectoryStream$Filter<? super java.nio.file.Path>) throws java.io.IOException
meth public java.nio.file.FileStore getFileStore(java.nio.file.Path) throws java.io.IOException
meth public java.nio.file.FileSystem getFileSystem(java.net.URI)
meth public java.nio.file.FileSystem newFileSystem(java.net.URI,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public java.nio.file.Path getPath(java.net.URI)
meth public void delete(java.nio.file.Path) throws java.io.IOException
supr java.nio.file.spi.FileSystemProvider

CLSS public org.apache.sshd.common.file.root.RootedDirectoryStream
cons public init(org.apache.sshd.common.file.root.RootedFileSystem,java.nio.file.DirectoryStream<java.nio.file.Path>)
fld protected final java.nio.file.DirectoryStream<java.nio.file.Path> delegate
fld protected final org.apache.sshd.common.file.root.RootedFileSystem rfs
intf java.nio.file.DirectoryStream<java.nio.file.Path>
meth protected java.util.Iterator<java.nio.file.Path> root(org.apache.sshd.common.file.root.RootedFileSystem,java.util.Iterator<java.nio.file.Path>)
meth public java.util.Iterator<java.nio.file.Path> iterator()
meth public void close() throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.sshd.common.file.root.RootedFileSystem
cons public init(org.apache.sshd.common.file.root.RootedFileSystemProvider,java.nio.file.Path,java.util.Map<java.lang.String,?>)
meth protected boolean hostFsHasWindowsSeparator()
meth protected org.apache.sshd.common.file.root.RootedPath create(java.lang.String,java.util.List<java.lang.String>)
meth public boolean isOpen()
meth public boolean isReadOnly()
meth public java.lang.Iterable<java.nio.file.FileStore> getFileStores()
meth public java.lang.String toString()
meth public java.nio.file.FileSystem getRootFileSystem()
meth public java.nio.file.Path getRoot()
meth public java.nio.file.attribute.UserPrincipalLookupService getUserPrincipalLookupService()
meth public java.util.Set<java.lang.String> supportedFileAttributeViews()
meth public org.apache.sshd.common.file.root.RootedFileSystemProvider provider()
meth public void close() throws java.io.IOException
supr org.apache.sshd.common.file.util.BaseFileSystem<org.apache.sshd.common.file.root.RootedPath>
hfds rootFs,rootPath

CLSS public org.apache.sshd.common.file.root.RootedFileSystemProvider
cons public init()
fld protected final org.slf4j.Logger log
meth protected java.nio.file.DirectoryStream<java.nio.file.Path> root(org.apache.sshd.common.file.root.RootedFileSystem,java.nio.file.DirectoryStream<java.nio.file.Path>)
meth protected java.nio.file.FileSystem newFileSystem(java.lang.Object,java.nio.file.Path,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth protected java.nio.file.Path resolveLocalPath(org.apache.sshd.common.file.root.RootedPath)
meth protected java.nio.file.Path root(org.apache.sshd.common.file.root.RootedFileSystem,java.nio.file.Path)
meth protected java.nio.file.Path unroot(java.nio.file.Path)
meth protected java.nio.file.Path uriToPath(java.net.URI)
meth protected java.nio.file.spi.FileSystemProvider provider(java.nio.file.Path)
meth protected org.apache.sshd.common.file.root.RootedFileSystem getFileSystem(java.nio.file.Path)
meth public !varargs <%0 extends java.nio.file.attribute.BasicFileAttributes> {%%0} readAttributes(java.nio.file.Path,java.lang.Class<{%%0}>,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs <%0 extends java.nio.file.attribute.FileAttributeView> {%%0} getFileAttributeView(java.nio.file.Path,java.lang.Class<{%%0}>,java.nio.file.LinkOption[])
meth public !varargs java.io.InputStream newInputStream(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs java.io.OutputStream newOutputStream(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
meth public !varargs java.nio.channels.AsynchronousFileChannel newAsynchronousFileChannel(java.nio.file.Path,java.util.Set<? extends java.nio.file.OpenOption>,java.util.concurrent.ExecutorService,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs java.nio.channels.FileChannel newFileChannel(java.nio.file.Path,java.util.Set<? extends java.nio.file.OpenOption>,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs java.nio.channels.SeekableByteChannel newByteChannel(java.nio.file.Path,java.util.Set<? extends java.nio.file.OpenOption>,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs java.util.Map<java.lang.String,java.lang.Object> readAttributes(java.nio.file.Path,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs void checkAccess(java.nio.file.Path,java.nio.file.AccessMode[]) throws java.io.IOException
meth public !varargs void copy(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption[]) throws java.io.IOException
meth public !varargs void createDirectory(java.nio.file.Path,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs void createSymbolicLink(java.nio.file.Path,java.nio.file.Path,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs void move(java.nio.file.Path,java.nio.file.Path,java.nio.file.CopyOption[]) throws java.io.IOException
meth public !varargs void setAttribute(java.nio.file.Path,java.lang.String,java.lang.Object,java.nio.file.LinkOption[]) throws java.io.IOException
meth public boolean deleteIfExists(java.nio.file.Path) throws java.io.IOException
meth public boolean isHidden(java.nio.file.Path) throws java.io.IOException
meth public boolean isSameFile(java.nio.file.Path,java.nio.file.Path) throws java.io.IOException
meth public java.lang.String getScheme()
meth public java.nio.file.DirectoryStream<java.nio.file.Path> newDirectoryStream(java.nio.file.Path,java.nio.file.DirectoryStream$Filter<? super java.nio.file.Path>) throws java.io.IOException
meth public java.nio.file.FileStore getFileStore(java.nio.file.Path) throws java.io.IOException
meth public java.nio.file.FileSystem getFileSystem(java.net.URI)
meth public java.nio.file.FileSystem newFileSystem(java.net.URI,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public java.nio.file.FileSystem newFileSystem(java.nio.file.Path,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public java.nio.file.Path getPath(java.net.URI)
meth public java.nio.file.Path readSymbolicLink(java.nio.file.Path) throws java.io.IOException
meth public void createLink(java.nio.file.Path,java.nio.file.Path) throws java.io.IOException
meth public void delete(java.nio.file.Path) throws java.io.IOException
supr java.nio.file.spi.FileSystemProvider
hfds fileSystems

CLSS public final org.apache.sshd.common.file.root.RootedFileSystemUtils
meth public static void validateSafeRelativeSymlink(java.nio.file.Path)
supr java.lang.Object

CLSS public org.apache.sshd.common.file.root.RootedPath
cons public init(org.apache.sshd.common.file.root.RootedFileSystem,java.lang.String,java.util.List<java.lang.String>)
meth public !varargs org.apache.sshd.common.file.root.RootedPath toRealPath(java.nio.file.LinkOption[]) throws java.io.IOException
meth public java.io.File toFile()
supr org.apache.sshd.common.file.util.BasePath<org.apache.sshd.common.file.root.RootedPath,org.apache.sshd.common.file.root.RootedFileSystem>

CLSS public org.apache.sshd.common.file.root.RootedSecureDirectoryStream
cons public init(org.apache.sshd.common.file.root.RootedFileSystem,java.nio.file.SecureDirectoryStream<java.nio.file.Path>)
intf java.nio.file.SecureDirectoryStream<java.nio.file.Path>
meth protected java.nio.file.Path fixPath(java.nio.file.Path)
meth public !varargs <%0 extends java.nio.file.attribute.FileAttributeView> {%%0} getFileAttributeView(java.nio.file.Path,java.lang.Class<{%%0}>,java.nio.file.LinkOption[])
meth public !varargs java.nio.channels.SeekableByteChannel newByteChannel(java.nio.file.Path,java.util.Set<? extends java.nio.file.OpenOption>,java.nio.file.attribute.FileAttribute<?>[]) throws java.io.IOException
meth public !varargs java.nio.file.SecureDirectoryStream<java.nio.file.Path> newDirectoryStream(java.nio.file.Path,java.nio.file.LinkOption[]) throws java.io.IOException
meth public <%0 extends java.nio.file.attribute.FileAttributeView> {%%0} getFileAttributeView(java.lang.Class<{%%0}>)
meth public void deleteDirectory(java.nio.file.Path) throws java.io.IOException
meth public void deleteFile(java.nio.file.Path) throws java.io.IOException
meth public void move(java.nio.file.Path,java.nio.file.SecureDirectoryStream<java.nio.file.Path>,java.nio.file.Path) throws java.io.IOException
supr org.apache.sshd.common.file.root.RootedDirectoryStream

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

CLSS public org.apache.sshd.common.file.util.MockFileSystem
cons public init(java.lang.String)
meth public !varargs java.nio.file.Path getPath(java.lang.String,java.lang.String[])
meth public boolean isOpen()
meth public boolean isReadOnly()
meth public java.lang.Iterable<java.nio.file.FileStore> getFileStores()
meth public java.lang.Iterable<java.nio.file.Path> getRootDirectories()
meth public java.lang.String getSeparator()
meth public java.lang.String toString()
meth public java.nio.file.PathMatcher getPathMatcher(java.lang.String)
meth public java.nio.file.WatchService newWatchService() throws java.io.IOException
meth public java.nio.file.attribute.UserPrincipalLookupService getUserPrincipalLookupService()
meth public java.nio.file.spi.FileSystemProvider provider()
meth public java.util.Set<java.lang.String> supportedFileAttributeViews()
meth public void close() throws java.io.IOException
supr java.nio.file.FileSystem
hfds name,open

CLSS public org.apache.sshd.common.file.util.MockPath
cons public init(java.lang.String)
intf java.nio.file.Path
meth public !varargs java.nio.file.Path toRealPath(java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs java.nio.file.WatchKey register(java.nio.file.WatchService,java.nio.file.WatchEvent$Kind<?>[]) throws java.io.IOException
meth public !varargs java.nio.file.WatchKey register(java.nio.file.WatchService,java.nio.file.WatchEvent$Kind<?>[],java.nio.file.WatchEvent$Modifier[]) throws java.io.IOException
meth public boolean endsWith(java.lang.String)
meth public boolean endsWith(java.nio.file.Path)
meth public boolean isAbsolute()
meth public boolean startsWith(java.lang.String)
meth public boolean startsWith(java.nio.file.Path)
meth public int compareTo(java.nio.file.Path)
meth public int getNameCount()
meth public java.io.File toFile()
meth public java.lang.String toString()
meth public java.net.URI toUri()
meth public java.nio.file.FileSystem getFileSystem()
meth public java.nio.file.Path getFileName()
meth public java.nio.file.Path getName(int)
meth public java.nio.file.Path getParent()
meth public java.nio.file.Path getRoot()
meth public java.nio.file.Path normalize()
meth public java.nio.file.Path relativize(java.nio.file.Path)
meth public java.nio.file.Path resolve(java.lang.String)
meth public java.nio.file.Path resolve(java.nio.file.Path)
meth public java.nio.file.Path resolveSibling(java.lang.String)
meth public java.nio.file.Path resolveSibling(java.nio.file.Path)
meth public java.nio.file.Path subpath(int,int)
meth public java.nio.file.Path toAbsolutePath()
meth public java.util.Iterator<java.nio.file.Path> iterator()
supr java.lang.Object
hfds fs,path

CLSS public org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory
cons public init()
cons public init(java.nio.file.Path)
intf org.apache.sshd.common.file.FileSystemFactory
meth public java.nio.file.FileSystem createFileSystem(org.apache.sshd.common.session.SessionContext) throws java.io.IOException
meth public java.nio.file.Path getDefaultHomeDir()
meth public java.nio.file.Path getUserHomeDir(java.lang.String)
meth public java.nio.file.Path getUserHomeDir(org.apache.sshd.common.session.SessionContext) throws java.io.IOException
meth public void setDefaultHomeDir(java.nio.file.Path)
meth public void setUserHomeDir(java.lang.String,java.nio.file.Path)
supr java.lang.Object
hfds defaultHomeDir,homeDirs

CLSS public org.apache.sshd.common.forward.ChannelToPortHandler
cons public init(org.apache.sshd.common.io.IoSession,org.apache.sshd.common.channel.Channel)
meth protected void handleWriteDataFailure(byte,byte[],int,int,java.lang.Throwable)
meth protected void handleWriteDataSuccess(byte,byte[],int,int)
meth public org.apache.sshd.common.io.IoSession getPortSession()
meth public org.apache.sshd.common.io.IoWriteFuture sendToPort(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void handleEof() throws java.io.IOException
meth public void sendToPort(byte,byte[],int,long) throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds channel,port

CLSS public org.apache.sshd.common.forward.DefaultForwarder
cons public init(org.apache.sshd.common.session.ConnectionService)
fld public final static java.util.Set<org.apache.sshd.client.channel.ClientChannelEvent> STATIC_IO_MSG_RECEIVED_EVENTS
intf org.apache.sshd.common.forward.Forwarder
intf org.apache.sshd.common.session.SessionHolder<org.apache.sshd.common.session.Session>
meth protected java.net.InetSocketAddress doBind(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.io.IoAcceptor) throws java.io.IOException
meth protected java.util.Collection<org.apache.sshd.common.forward.PortForwardingEventListener> getDefaultListeners()
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected org.apache.sshd.common.io.IoAcceptor createIoAcceptor(org.apache.sshd.common.Factory<? extends org.apache.sshd.common.io.IoHandler>)
meth protected org.apache.sshd.common.io.IoAcceptor getDynamicIoAcceptor()
meth protected org.apache.sshd.common.io.IoAcceptor getLocalIoAcceptor()
meth protected void invokePortEventListenerSignaller(org.apache.sshd.common.util.io.functors.Invoker<org.apache.sshd.common.forward.PortForwardingEventListener,java.lang.Void>) throws java.lang.Throwable
meth protected void invokePortEventListenerSignallerHolders(java.util.Collection<? extends org.apache.sshd.common.forward.PortForwardingEventListenerManager>,org.apache.sshd.common.util.io.functors.Invoker<org.apache.sshd.common.forward.PortForwardingEventListener,java.lang.Void>) throws java.lang.Throwable
meth protected void invokePortEventListenerSignallerListeners(java.util.Collection<? extends org.apache.sshd.common.forward.PortForwardingEventListener>,org.apache.sshd.common.util.io.functors.Invoker<org.apache.sshd.common.forward.PortForwardingEventListener,java.lang.Void>) throws java.lang.Throwable
meth protected void preClose()
meth protected void signalEstablishedDynamicTunnel(org.apache.sshd.common.forward.PortForwardingEventListener,org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress,java.lang.Throwable) throws java.io.IOException
meth protected void signalEstablishedDynamicTunnel(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress,java.lang.Throwable) throws java.io.IOException
meth protected void signalEstablishedExplicitTunnel(org.apache.sshd.common.forward.PortForwardingEventListener,org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress,boolean,org.apache.sshd.common.util.net.SshdSocketAddress,java.lang.Throwable) throws java.io.IOException
meth protected void signalEstablishedExplicitTunnel(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress,boolean,org.apache.sshd.common.util.net.SshdSocketAddress,java.lang.Throwable) throws java.io.IOException
meth protected void signalEstablishingDynamicTunnel(org.apache.sshd.common.forward.PortForwardingEventListener,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth protected void signalEstablishingDynamicTunnel(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth protected void signalEstablishingExplicitTunnel(org.apache.sshd.common.forward.PortForwardingEventListener,org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress,boolean) throws java.io.IOException
meth protected void signalEstablishingExplicitTunnel(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress,boolean) throws java.io.IOException
meth protected void signalTearingDownDynamicTunnel(org.apache.sshd.common.forward.PortForwardingEventListener,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth protected void signalTearingDownDynamicTunnel(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth protected void signalTearingDownExplicitTunnel(org.apache.sshd.common.forward.PortForwardingEventListener,org.apache.sshd.common.util.net.SshdSocketAddress,boolean,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth protected void signalTearingDownExplicitTunnel(org.apache.sshd.common.util.net.SshdSocketAddress,boolean,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth protected void signalTornDownDynamicTunnel(org.apache.sshd.common.forward.PortForwardingEventListener,org.apache.sshd.common.util.net.SshdSocketAddress,java.lang.Throwable) throws java.io.IOException
meth protected void signalTornDownDynamicTunnel(org.apache.sshd.common.util.net.SshdSocketAddress,java.lang.Throwable) throws java.io.IOException
meth protected void signalTornDownExplicitTunnel(org.apache.sshd.common.forward.PortForwardingEventListener,org.apache.sshd.common.util.net.SshdSocketAddress,boolean,org.apache.sshd.common.util.net.SshdSocketAddress,java.lang.Throwable) throws java.io.IOException
meth protected void signalTornDownExplicitTunnel(org.apache.sshd.common.util.net.SshdSocketAddress,boolean,org.apache.sshd.common.util.net.SshdSocketAddress,java.lang.Throwable) throws java.io.IOException
meth protected void unbindDynamicForwarding(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.forward.SocksProxy,java.net.InetSocketAddress) throws java.io.IOException
meth protected void unbindLocalForwarding(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress,java.net.InetSocketAddress) throws java.io.IOException
meth public boolean addPortForwardingEventListenerManager(org.apache.sshd.common.forward.PortForwardingEventListenerManager)
meth public boolean isLocalPortForwardingStartedForPort(int)
meth public boolean removePortForwardingEventListenerManager(org.apache.sshd.common.forward.PortForwardingEventListenerManager)
meth public final org.apache.sshd.common.session.ConnectionService getConnectionService()
meth public java.lang.String toString()
meth public java.util.Collection<org.apache.sshd.common.forward.PortForwardingEventListenerManager> getRegisteredManagers()
meth public java.util.List<java.util.Map$Entry<java.lang.Integer,org.apache.sshd.common.util.net.SshdSocketAddress>> getRemoteForwardsBindings()
meth public java.util.List<java.util.Map$Entry<org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress>> getLocalForwardsBindings()
meth public java.util.List<org.apache.sshd.common.util.net.SshdSocketAddress> getBoundLocalPortForwards(int)
meth public java.util.List<org.apache.sshd.common.util.net.SshdSocketAddress> getStartedLocalPortForwards()
meth public java.util.NavigableSet<java.lang.Integer> getStartedRemotePortForwards()
meth public org.apache.sshd.common.forward.PortForwardingEventListener getPortForwardingEventListenerProxy()
meth public org.apache.sshd.common.session.Session getSession()
meth public org.apache.sshd.common.util.net.SshdSocketAddress getBoundRemotePortForward(int)
meth public org.apache.sshd.common.util.net.SshdSocketAddress getForwardedPort(int)
meth public org.apache.sshd.common.util.net.SshdSocketAddress localPortForwardingRequested(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.common.util.net.SshdSocketAddress startDynamicPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.common.util.net.SshdSocketAddress startLocalPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.common.util.net.SshdSocketAddress startRemotePortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public void addPortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
meth public void localPortForwardingCancelled(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public void removePortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
meth public void stopDynamicPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public void stopLocalPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public void stopRemotePortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
supr org.apache.sshd.common.util.closeable.AbstractInnerCloseable
hfds boundDynamic,boundLocals,dynamicAcceptor,dynamicLocal,dynamicLock,listenerProxy,listeners,localAcceptor,localForwards,localLock,localToRemote,managersHolder,remoteToLocal,service,sessionInstance,socksProxyIoHandlerFactory,staticIoHandlerFactory
hcls StaticIoHandler

CLSS public org.apache.sshd.common.forward.DefaultForwarderFactory
cons public init()
fld public final static org.apache.sshd.common.forward.DefaultForwarderFactory INSTANCE
intf org.apache.sshd.common.forward.ForwarderFactory
intf org.apache.sshd.common.forward.PortForwardingEventListenerManager
meth public org.apache.sshd.common.forward.Forwarder create(org.apache.sshd.common.session.ConnectionService)
meth public org.apache.sshd.common.forward.PortForwardingEventListener getPortForwardingEventListenerProxy()
meth public void addPortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
meth public void removePortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
supr java.lang.Object
hfds listenerProxy,listeners

CLSS public abstract interface org.apache.sshd.common.forward.Forwarder
intf org.apache.sshd.common.Closeable
intf org.apache.sshd.common.forward.PortForwardingEventListenerManager
intf org.apache.sshd.common.forward.PortForwardingEventListenerManagerHolder
intf org.apache.sshd.common.forward.PortForwardingManager
meth public abstract org.apache.sshd.common.util.net.SshdSocketAddress getForwardedPort(int)
meth public abstract org.apache.sshd.common.util.net.SshdSocketAddress localPortForwardingRequested(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public abstract void localPortForwardingCancelled(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.common.forward.ForwarderFactory
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.common.forward.Forwarder create(org.apache.sshd.common.session.ConnectionService)

CLSS public abstract interface org.apache.sshd.common.forward.ForwardingTunnelEndpointsProvider
meth public abstract org.apache.sshd.common.util.net.SshdSocketAddress getTunnelEntrance()
meth public abstract org.apache.sshd.common.util.net.SshdSocketAddress getTunnelExit()

CLSS public org.apache.sshd.common.forward.LocalForwardingEntry
cons public init(org.apache.sshd.common.util.net.SshdSocketAddress,java.net.InetSocketAddress)
cons public init(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.apache.sshd.common.util.net.SshdSocketAddress getBoundAddress()
meth public org.apache.sshd.common.util.net.SshdSocketAddress getCombinedBoundAddress()
meth public org.apache.sshd.common.util.net.SshdSocketAddress getLocalAddress()
meth public static org.apache.sshd.common.forward.LocalForwardingEntry findMatchingEntry(java.lang.String,boolean,int,java.util.Collection<? extends org.apache.sshd.common.forward.LocalForwardingEntry>)
meth public static org.apache.sshd.common.forward.LocalForwardingEntry findMatchingEntry(java.lang.String,int,java.util.Collection<? extends org.apache.sshd.common.forward.LocalForwardingEntry>)
meth public static org.apache.sshd.common.util.net.SshdSocketAddress resolveCombinedBoundAddress(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress)
supr java.lang.Object
hfds bound,combined,local

CLSS public abstract interface org.apache.sshd.common.forward.PortForwardingEventListener
fld public final static org.apache.sshd.common.forward.PortForwardingEventListener EMPTY
intf org.apache.sshd.common.util.SshdEventListener
meth public static <%0 extends org.apache.sshd.common.forward.PortForwardingEventListener> {%%0} validateListener({%%0})
meth public void establishedDynamicTunnel(org.apache.sshd.common.session.Session,org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress,java.lang.Throwable) throws java.io.IOException
meth public void establishedExplicitTunnel(org.apache.sshd.common.session.Session,org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress,boolean,org.apache.sshd.common.util.net.SshdSocketAddress,java.lang.Throwable) throws java.io.IOException
meth public void establishingDynamicTunnel(org.apache.sshd.common.session.Session,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public void establishingExplicitTunnel(org.apache.sshd.common.session.Session,org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress,boolean) throws java.io.IOException
meth public void tearingDownDynamicTunnel(org.apache.sshd.common.session.Session,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public void tearingDownExplicitTunnel(org.apache.sshd.common.session.Session,org.apache.sshd.common.util.net.SshdSocketAddress,boolean,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public void tornDownDynamicTunnel(org.apache.sshd.common.session.Session,org.apache.sshd.common.util.net.SshdSocketAddress,java.lang.Throwable) throws java.io.IOException
meth public void tornDownExplicitTunnel(org.apache.sshd.common.session.Session,org.apache.sshd.common.util.net.SshdSocketAddress,boolean,org.apache.sshd.common.util.net.SshdSocketAddress,java.lang.Throwable) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.common.forward.PortForwardingEventListenerManager
meth public abstract org.apache.sshd.common.forward.PortForwardingEventListener getPortForwardingEventListenerProxy()
meth public abstract void addPortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
meth public abstract void removePortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)

CLSS public abstract interface org.apache.sshd.common.forward.PortForwardingEventListenerManagerHolder
meth public abstract boolean addPortForwardingEventListenerManager(org.apache.sshd.common.forward.PortForwardingEventListenerManager)
meth public abstract boolean removePortForwardingEventListenerManager(org.apache.sshd.common.forward.PortForwardingEventListenerManager)
meth public abstract java.util.Collection<org.apache.sshd.common.forward.PortForwardingEventListenerManager> getRegisteredManagers()

CLSS public abstract interface org.apache.sshd.common.forward.PortForwardingInformationProvider
meth public abstract java.util.List<java.util.Map$Entry<java.lang.Integer,org.apache.sshd.common.util.net.SshdSocketAddress>> getRemoteForwardsBindings()
meth public abstract java.util.List<java.util.Map$Entry<org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress>> getLocalForwardsBindings()
meth public abstract java.util.List<org.apache.sshd.common.util.net.SshdSocketAddress> getBoundLocalPortForwards(int)
meth public abstract java.util.List<org.apache.sshd.common.util.net.SshdSocketAddress> getStartedLocalPortForwards()
meth public abstract java.util.NavigableSet<java.lang.Integer> getStartedRemotePortForwards()
meth public abstract org.apache.sshd.common.util.net.SshdSocketAddress getBoundRemotePortForward(int)
meth public boolean isLocalPortForwardingStartedForPort(int)
meth public boolean isRemotePortForwardingStartedForPort(int)

CLSS public abstract interface org.apache.sshd.common.forward.PortForwardingManager
intf org.apache.sshd.common.forward.PortForwardingInformationProvider
meth public abstract org.apache.sshd.common.util.net.SshdSocketAddress startDynamicPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public abstract org.apache.sshd.common.util.net.SshdSocketAddress startLocalPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public abstract org.apache.sshd.common.util.net.SshdSocketAddress startRemotePortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public abstract void stopDynamicPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public abstract void stopLocalPortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public abstract void stopRemotePortForwarding(org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException
meth public org.apache.sshd.common.util.net.SshdSocketAddress startLocalPortForwarding(int,org.apache.sshd.common.util.net.SshdSocketAddress) throws java.io.IOException

CLSS public org.apache.sshd.common.forward.SocksProxy
cons public init(org.apache.sshd.common.session.ConnectionService)
innr public Socks4
innr public Socks5
innr public abstract static Proxy
intf org.apache.sshd.common.io.IoHandler
meth public void exceptionCaught(org.apache.sshd.common.io.IoSession,java.lang.Throwable) throws java.lang.Exception
meth public void messageReceived(org.apache.sshd.common.io.IoSession,org.apache.sshd.common.util.Readable) throws java.lang.Exception
meth public void sessionClosed(org.apache.sshd.common.io.IoSession) throws java.lang.Exception
meth public void sessionCreated(org.apache.sshd.common.io.IoSession) throws java.lang.Exception
supr org.apache.sshd.common.util.closeable.AbstractCloseable
hfds proxies,service

CLSS public abstract static org.apache.sshd.common.forward.SocksProxy$Proxy
 outer org.apache.sshd.common.forward.SocksProxy
cons protected init(org.apache.sshd.common.io.IoSession)
fld protected org.apache.sshd.common.forward.TcpipClientChannel channel
fld protected org.apache.sshd.common.io.IoSession session
intf java.io.Closeable
meth protected int getUByte(org.apache.sshd.common.util.buffer.Buffer)
meth protected int getUShort(org.apache.sshd.common.util.buffer.Buffer)
meth protected void onMessage(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void close() throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.sshd.common.forward.SocksProxy$Socks4
 outer org.apache.sshd.common.forward.SocksProxy
cons public init(org.apache.sshd.common.forward.SocksProxy,org.apache.sshd.common.io.IoSession)
meth protected java.lang.String getNTString(org.apache.sshd.common.util.buffer.Buffer)
meth protected void onChannelOpened(org.apache.sshd.client.future.OpenFuture)
meth protected void onMessage(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr org.apache.sshd.common.forward.SocksProxy$Proxy

CLSS public org.apache.sshd.common.forward.SocksProxy$Socks5
 outer org.apache.sshd.common.forward.SocksProxy
cons public init(org.apache.sshd.common.forward.SocksProxy,org.apache.sshd.common.io.IoSession)
meth protected java.lang.String getBLString(org.apache.sshd.common.util.buffer.Buffer)
meth protected void onChannelOpened(org.apache.sshd.client.future.OpenFuture)
meth protected void onMessage(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr org.apache.sshd.common.forward.SocksProxy$Proxy
hfds authMethods,response

CLSS public org.apache.sshd.common.forward.TcpipClientChannel
cons public init(org.apache.sshd.common.forward.TcpipClientChannel$Type,org.apache.sshd.common.io.IoSession,org.apache.sshd.common.util.net.SshdSocketAddress)
fld protected final org.apache.sshd.common.forward.ChannelToPortHandler port
fld protected final org.apache.sshd.common.util.net.SshdSocketAddress remote
fld protected org.apache.sshd.common.util.net.SshdSocketAddress localEntry
innr public final static !enum Type
intf org.apache.sshd.common.forward.ForwardingTunnelEndpointsProvider
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected org.apache.sshd.common.forward.ChannelToPortHandler createChannelToPortHandler(org.apache.sshd.common.io.IoSession)
meth protected void doOpen() throws java.io.IOException
meth protected void doWriteData(byte[],int,long) throws java.io.IOException
meth protected void doWriteExtendedData(byte[],int,long) throws java.io.IOException
meth public org.apache.sshd.client.future.OpenFuture open() throws java.io.IOException
meth public org.apache.sshd.common.forward.TcpipClientChannel$Type getTcpipChannelType()
meth public org.apache.sshd.common.util.net.SshdSocketAddress getTunnelEntrance()
meth public org.apache.sshd.common.util.net.SshdSocketAddress getTunnelExit()
meth public void handleEof() throws java.io.IOException
meth public void updateLocalForwardingEntry(org.apache.sshd.common.forward.LocalForwardingEntry)
supr org.apache.sshd.client.channel.AbstractClientChannel
hfds tunnelEntrance,tunnelExit,typeEnum

CLSS public final static !enum org.apache.sshd.common.forward.TcpipClientChannel$Type
 outer org.apache.sshd.common.forward.TcpipClientChannel
fld public final static java.util.Set<org.apache.sshd.common.forward.TcpipClientChannel$Type> VALUES
fld public final static org.apache.sshd.common.forward.TcpipClientChannel$Type Direct
fld public final static org.apache.sshd.common.forward.TcpipClientChannel$Type Forwarded
intf org.apache.sshd.common.NamedResource
meth public java.lang.String getName()
meth public static org.apache.sshd.common.forward.TcpipClientChannel$Type valueOf(java.lang.String)
meth public static org.apache.sshd.common.forward.TcpipClientChannel$Type[] values()
supr java.lang.Enum<org.apache.sshd.common.forward.TcpipClientChannel$Type>
hfds channelType

CLSS public abstract interface org.apache.sshd.common.forward.TcpipForwardingExceptionMarker

CLSS public abstract org.apache.sshd.common.future.AbstractSshFuture<%0 extends org.apache.sshd.common.future.SshFuture<{org.apache.sshd.common.future.AbstractSshFuture%0}>>
cons protected init(java.lang.Object)
intf org.apache.sshd.common.future.SshFuture<{org.apache.sshd.common.future.AbstractSshFuture%0}>
meth protected !varargs <%0 extends java.lang.Object> {%%0} verifyResult(java.lang.Class<? extends {%%0}>,long,org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth protected !varargs <%0 extends java.lang.Throwable> {%%0} formatExceptionMessage(java.util.function.Function<? super java.lang.String,? extends {%%0}>,java.lang.String,java.lang.Object[])
meth protected abstract !varargs java.lang.Object await0(long,boolean,org.apache.sshd.common.future.CancelOption[]) throws java.io.InterruptedIOException
meth protected org.apache.sshd.common.future.SshFutureListener<{org.apache.sshd.common.future.AbstractSshFuture%0}> asListener(java.lang.Object)
meth protected void notifyListener(org.apache.sshd.common.future.SshFutureListener<{org.apache.sshd.common.future.AbstractSshFuture%0}>)
meth protected {org.apache.sshd.common.future.AbstractSshFuture%0} asT()
meth public !varargs boolean await(long,org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public !varargs boolean awaitUninterruptibly(long,org.apache.sshd.common.future.CancelOption[])
meth public java.lang.Object getId()
meth public java.lang.String toString()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds id

CLSS public abstract interface org.apache.sshd.common.future.CancelFuture
intf org.apache.sshd.common.future.SshFuture<org.apache.sshd.common.future.CancelFuture>
intf org.apache.sshd.common.future.VerifiableFuture<java.lang.Boolean>
meth public abstract boolean isCanceled()
meth public abstract java.util.concurrent.CancellationException getBackTrace()
meth public abstract void setBackTrace(java.util.concurrent.CancellationException)
meth public abstract void setCanceled()
meth public abstract void setCanceled(java.lang.Throwable)
meth public abstract void setNotCanceled()

CLSS public final !enum org.apache.sshd.common.future.CancelOption
fld public final static org.apache.sshd.common.future.CancelOption CANCEL_ON_INTERRUPT
fld public final static org.apache.sshd.common.future.CancelOption CANCEL_ON_TIMEOUT
fld public final static org.apache.sshd.common.future.CancelOption NO_CANCELLATION
meth public static org.apache.sshd.common.future.CancelOption valueOf(java.lang.String)
meth public static org.apache.sshd.common.future.CancelOption[] values()
supr java.lang.Enum<org.apache.sshd.common.future.CancelOption>

CLSS public abstract interface org.apache.sshd.common.future.Cancellable
intf org.apache.sshd.common.future.WithException
meth public abstract boolean isCanceled()
meth public abstract org.apache.sshd.common.future.CancelFuture cancel()
meth public abstract org.apache.sshd.common.future.CancelFuture getCancellation()

CLSS public abstract interface org.apache.sshd.common.future.CloseFuture
intf org.apache.sshd.common.future.SshFuture<org.apache.sshd.common.future.CloseFuture>
meth public abstract boolean isClosed()
meth public abstract void setClosed()

CLSS public org.apache.sshd.common.future.DefaultCancelFuture
cons protected init(java.lang.Object)
intf org.apache.sshd.common.future.CancelFuture
meth public !varargs java.lang.Boolean verify(long,org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public boolean isCanceled()
meth public java.util.concurrent.CancellationException getBackTrace()
meth public void setBackTrace(java.util.concurrent.CancellationException)
meth public void setCanceled()
meth public void setCanceled(java.lang.Throwable)
meth public void setNotCanceled()
supr org.apache.sshd.common.future.DefaultSshFuture<org.apache.sshd.common.future.CancelFuture>
hfds backTrace

CLSS public abstract org.apache.sshd.common.future.DefaultCancellableSshFuture<%0 extends org.apache.sshd.common.future.SshFuture<{org.apache.sshd.common.future.DefaultCancellableSshFuture%0}>>
cons protected init(java.lang.Object,java.lang.Object)
intf org.apache.sshd.common.future.Cancellable
meth protected org.apache.sshd.common.future.CancelFuture createCancellation()
meth public boolean isCanceled()
meth public java.lang.Throwable getException()
meth public org.apache.sshd.common.future.CancelFuture cancel()
meth public org.apache.sshd.common.future.CancelFuture getCancellation()
meth public void setException(java.lang.Throwable)
supr org.apache.sshd.common.future.DefaultVerifiableSshFuture<{org.apache.sshd.common.future.DefaultCancellableSshFuture%0}>

CLSS public org.apache.sshd.common.future.DefaultCloseFuture
cons public init(java.lang.Object,java.lang.Object)
intf org.apache.sshd.common.future.CloseFuture
meth public boolean isClosed()
meth public void setClosed()
supr org.apache.sshd.common.future.DefaultSshFuture<org.apache.sshd.common.future.CloseFuture>

CLSS public org.apache.sshd.common.future.DefaultKeyExchangeFuture
cons public init(java.lang.Object,java.lang.Object)
intf org.apache.sshd.common.future.KeyExchangeFuture
meth public !varargs org.apache.sshd.common.future.KeyExchangeFuture verify(long,org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public java.lang.Throwable getException()
supr org.apache.sshd.common.future.DefaultVerifiableSshFuture<org.apache.sshd.common.future.KeyExchangeFuture>

CLSS public org.apache.sshd.common.future.DefaultSshFuture<%0 extends org.apache.sshd.common.future.SshFuture<{org.apache.sshd.common.future.DefaultSshFuture%0}>>
cons public init(java.lang.Object,java.lang.Object)
meth protected !varargs java.lang.Object await0(long,boolean,org.apache.sshd.common.future.CancelOption[]) throws java.io.InterruptedIOException
meth protected org.apache.sshd.common.future.CancelFuture createCancellation()
meth protected void notifyListeners()
meth protected void onValueSet(java.lang.Object)
meth public boolean isDone()
meth public int getNumRegisteredListeners()
meth public java.lang.Object getValue()
meth public java.lang.String toString()
meth public void setValue(java.lang.Object)
meth public {org.apache.sshd.common.future.DefaultSshFuture%0} addListener(org.apache.sshd.common.future.SshFutureListener<{org.apache.sshd.common.future.DefaultSshFuture%0}>)
meth public {org.apache.sshd.common.future.DefaultSshFuture%0} removeListener(org.apache.sshd.common.future.SshFutureListener<{org.apache.sshd.common.future.DefaultSshFuture%0}>)
supr org.apache.sshd.common.future.AbstractSshFuture<{org.apache.sshd.common.future.DefaultSshFuture%0}>
hfds listeners,lock,result

CLSS public abstract org.apache.sshd.common.future.DefaultVerifiableSshFuture<%0 extends org.apache.sshd.common.future.SshFuture<{org.apache.sshd.common.future.DefaultVerifiableSshFuture%0}>>
cons protected init(java.lang.Object,java.lang.Object)
intf org.apache.sshd.common.future.VerifiableFuture<{org.apache.sshd.common.future.DefaultVerifiableSshFuture%0}>
supr org.apache.sshd.common.future.DefaultSshFuture<{org.apache.sshd.common.future.DefaultVerifiableSshFuture%0}>

CLSS public org.apache.sshd.common.future.GlobalRequestFuture
cons public init(java.lang.String)
cons public init(java.lang.String,org.apache.sshd.common.future.GlobalRequestFuture$ReplyHandler)
innr public abstract interface static ReplyHandler
intf org.apache.sshd.common.future.HasException
intf org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.io.IoWriteFuture>
meth public java.lang.String getId()
meth public java.lang.String toString()
meth public java.lang.Throwable getException()
meth public long getSequenceNumber()
meth public org.apache.sshd.common.future.GlobalRequestFuture$ReplyHandler getHandler()
meth public org.apache.sshd.common.util.buffer.Buffer getBuffer()
meth public void operationComplete(org.apache.sshd.common.io.IoWriteFuture)
meth public void setSequenceNumber(long)
supr org.apache.sshd.common.future.DefaultSshFuture<org.apache.sshd.common.future.GlobalRequestFuture>
hfds handler,sequenceNumber

CLSS public abstract interface static org.apache.sshd.common.future.GlobalRequestFuture$ReplyHandler
 outer org.apache.sshd.common.future.GlobalRequestFuture
 anno 0 java.lang.FunctionalInterface()
meth public abstract void accept(int,org.apache.sshd.common.util.buffer.Buffer)

CLSS public abstract interface org.apache.sshd.common.future.HasException
meth public abstract java.lang.Throwable getException()

CLSS public abstract interface org.apache.sshd.common.future.KeyExchangeFuture
intf org.apache.sshd.common.future.SshFuture<org.apache.sshd.common.future.KeyExchangeFuture>
intf org.apache.sshd.common.future.VerifiableFuture<org.apache.sshd.common.future.KeyExchangeFuture>
meth public abstract java.lang.Throwable getException()

CLSS public abstract interface org.apache.sshd.common.future.SshFuture<%0 extends org.apache.sshd.common.future.SshFuture<{org.apache.sshd.common.future.SshFuture%0}>>
intf org.apache.sshd.common.future.WaitableFuture
meth public abstract {org.apache.sshd.common.future.SshFuture%0} addListener(org.apache.sshd.common.future.SshFutureListener<{org.apache.sshd.common.future.SshFuture%0}>)
meth public abstract {org.apache.sshd.common.future.SshFuture%0} removeListener(org.apache.sshd.common.future.SshFutureListener<{org.apache.sshd.common.future.SshFuture%0}>)

CLSS public abstract interface org.apache.sshd.common.future.SshFutureListener<%0 extends org.apache.sshd.common.future.SshFuture>
 anno 0 java.lang.FunctionalInterface()
intf org.apache.sshd.common.util.SshdEventListener
meth public abstract void operationComplete({org.apache.sshd.common.future.SshFutureListener%0})

CLSS public abstract interface org.apache.sshd.common.future.VerifiableFuture<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public !varargs {org.apache.sshd.common.future.VerifiableFuture%0} verify(java.time.Duration,org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public !varargs {org.apache.sshd.common.future.VerifiableFuture%0} verify(long,java.util.concurrent.TimeUnit,org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public !varargs {org.apache.sshd.common.future.VerifiableFuture%0} verify(org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public abstract !varargs {org.apache.sshd.common.future.VerifiableFuture%0} verify(long,org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public {org.apache.sshd.common.future.VerifiableFuture%0} verify() throws java.io.IOException
meth public {org.apache.sshd.common.future.VerifiableFuture%0} verify(java.time.Duration) throws java.io.IOException
meth public {org.apache.sshd.common.future.VerifiableFuture%0} verify(long) throws java.io.IOException
meth public {org.apache.sshd.common.future.VerifiableFuture%0} verify(long,java.util.concurrent.TimeUnit) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.common.future.WaitableFuture
meth public !varargs boolean await(java.time.Duration,org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public !varargs boolean await(long,java.util.concurrent.TimeUnit,org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public !varargs boolean await(org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public !varargs boolean awaitUninterruptibly(java.time.Duration,org.apache.sshd.common.future.CancelOption[])
meth public !varargs boolean awaitUninterruptibly(long,java.util.concurrent.TimeUnit,org.apache.sshd.common.future.CancelOption[])
meth public !varargs boolean awaitUninterruptibly(org.apache.sshd.common.future.CancelOption[])
meth public abstract !varargs boolean await(long,org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public abstract !varargs boolean awaitUninterruptibly(long,org.apache.sshd.common.future.CancelOption[])
meth public abstract boolean isDone()
meth public abstract java.lang.Object getId()
meth public boolean await() throws java.io.IOException
meth public boolean await(java.time.Duration) throws java.io.IOException
meth public boolean await(long) throws java.io.IOException
meth public boolean await(long,java.util.concurrent.TimeUnit) throws java.io.IOException
meth public boolean awaitUninterruptibly()
meth public boolean awaitUninterruptibly(java.time.Duration)
meth public boolean awaitUninterruptibly(long)
meth public boolean awaitUninterruptibly(long,java.util.concurrent.TimeUnit)

CLSS public abstract interface org.apache.sshd.common.future.WithException
intf org.apache.sshd.common.future.HasException
meth public abstract void setException(java.lang.Throwable)

CLSS public abstract org.apache.sshd.common.global.AbstractOpenSshHostKeysHandler
cons protected init(java.lang.String)
cons protected init(java.lang.String,org.apache.sshd.common.util.buffer.keys.BufferPublicKeyParser<? extends java.security.PublicKey>)
meth protected abstract org.apache.sshd.common.channel.RequestHandler$Result handleHostKeys(org.apache.sshd.common.session.Session,java.util.Collection<? extends java.security.PublicKey>,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected boolean isIgnoreInvalidKeys()
meth protected void setIgnoreInvalidKeys(boolean)
meth public final java.lang.String getRequestName()
meth public java.lang.String toString()
meth public org.apache.sshd.common.channel.RequestHandler$Result process(org.apache.sshd.common.session.ConnectionService,java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public org.apache.sshd.common.util.buffer.keys.BufferPublicKeyParser<? extends java.security.PublicKey> getPublicKeysParser()
supr org.apache.sshd.common.session.helpers.AbstractConnectionServiceRequestHandler
hfds ignoreInvalidKeys,parser,request

CLSS public org.apache.sshd.common.global.GlobalRequestException
cons public init(int)
meth public int getCode()
supr java.lang.Exception
hfds code,serialVersionUID

CLSS public org.apache.sshd.common.global.KeepAliveHandler
cons public init()
fld public final static org.apache.sshd.common.global.KeepAliveHandler INSTANCE
meth public org.apache.sshd.common.channel.RequestHandler$Result process(org.apache.sshd.common.session.ConnectionService,java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
supr org.apache.sshd.common.session.helpers.AbstractConnectionServiceRequestHandler

CLSS public abstract org.apache.sshd.common.helpers.AbstractFactoryManager
cons protected init()
fld protected boolean shutdownExecutor
fld protected final java.util.Collection<org.apache.sshd.common.channel.ChannelListener> channelListeners
fld protected final java.util.Collection<org.apache.sshd.common.forward.PortForwardingEventListener> tunnelListeners
fld protected final java.util.Collection<org.apache.sshd.common.session.SessionListener> sessionListeners
fld protected final java.util.concurrent.atomic.AtomicReference<java.util.concurrent.ScheduledFuture<?>> timeoutListenerFuture
fld protected final java.util.concurrent.atomic.AtomicReference<org.apache.sshd.common.session.helpers.SessionTimeoutListener> sessionTimeoutListener
fld protected final org.apache.sshd.common.channel.ChannelListener channelListenerProxy
fld protected final org.apache.sshd.common.forward.PortForwardingEventListener tunnelListenerProxy
fld protected final org.apache.sshd.common.session.SessionListener sessionListenerProxy
fld protected java.util.List<? extends org.apache.sshd.common.ServiceFactory> serviceFactories
fld protected java.util.List<? extends org.apache.sshd.common.channel.ChannelFactory> channelFactories
fld protected java.util.List<org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.session.ConnectionService>> globalRequestHandlers
fld protected java.util.concurrent.ScheduledExecutorService executor
fld protected org.apache.sshd.agent.SshAgentFactory agentFactory
fld protected org.apache.sshd.common.Factory<? extends org.apache.sshd.common.random.Random> randomFactory
fld protected org.apache.sshd.common.file.FileSystemFactory fileSystemFactory
fld protected org.apache.sshd.common.forward.ForwarderFactory forwarderFactory
fld protected org.apache.sshd.common.io.IoServiceFactory ioServiceFactory
fld protected org.apache.sshd.common.io.IoServiceFactoryFactory ioServiceFactoryFactory
fld protected org.apache.sshd.server.forward.ForwardingFilter forwardingFilter
intf org.apache.sshd.common.FactoryManager
meth protected org.apache.sshd.common.session.helpers.SessionTimeoutListener createSessionTimeoutListener()
meth protected void checkConfig()
meth protected void removeSessionTimeout(org.apache.sshd.common.session.helpers.AbstractSessionFactory<?,?>)
meth protected void setupSessionTimeout(org.apache.sshd.common.session.helpers.AbstractSessionFactory<?,?>)
meth protected void stopSessionTimeoutListener(org.apache.sshd.common.session.helpers.AbstractSessionFactory<?,?>)
meth public <%0 extends java.lang.Object> {%%0} computeAttributeIfAbsent(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,java.util.function.Function<? super org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,? extends {%%0}>)
meth public <%0 extends java.lang.Object> {%%0} getAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} removeAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} setAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,{%%0})
meth public int getAttributesCount()
meth public int getNioWorkers()
meth public java.lang.String getVersion()
meth public java.util.Collection<org.apache.sshd.common.AttributeRepository$AttributeKey<?>> attributeKeys()
meth public java.util.List<? extends org.apache.sshd.common.ServiceFactory> getServiceFactories()
meth public java.util.List<? extends org.apache.sshd.common.channel.ChannelFactory> getChannelFactories()
meth public java.util.List<org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.session.ConnectionService>> getGlobalRequestHandlers()
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public java.util.concurrent.ScheduledExecutorService getScheduledExecutorService()
meth public org.apache.sshd.agent.SshAgentFactory getAgentFactory()
meth public org.apache.sshd.common.Factory<? extends org.apache.sshd.common.random.Random> getRandomFactory()
meth public org.apache.sshd.common.PropertyResolver getParentPropertyResolver()
meth public org.apache.sshd.common.channel.ChannelListener getChannelListenerProxy()
meth public org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver getChannelStreamWriterResolver()
meth public org.apache.sshd.common.file.FileSystemFactory getFileSystemFactory()
meth public org.apache.sshd.common.forward.ForwarderFactory getForwarderFactory()
meth public org.apache.sshd.common.forward.PortForwardingEventListener getPortForwardingEventListenerProxy()
meth public org.apache.sshd.common.io.IoServiceEventListener getIoServiceEventListener()
meth public org.apache.sshd.common.io.IoServiceFactory getIoServiceFactory()
meth public org.apache.sshd.common.io.IoServiceFactoryFactory getIoServiceFactoryFactory()
meth public org.apache.sshd.common.session.ReservedSessionMessagesHandler getReservedSessionMessagesHandler()
meth public org.apache.sshd.common.session.SessionDisconnectHandler getSessionDisconnectHandler()
meth public org.apache.sshd.common.session.SessionListener getSessionListenerProxy()
meth public org.apache.sshd.common.session.UnknownChannelReferenceHandler getUnknownChannelReferenceHandler()
meth public org.apache.sshd.common.session.UnknownChannelReferenceHandler resolveUnknownChannelReferenceHandler()
meth public org.apache.sshd.server.forward.ForwardingFilter getForwardingFilter()
meth public void addChannelListener(org.apache.sshd.common.channel.ChannelListener)
meth public void addPortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
meth public void addSessionListener(org.apache.sshd.common.session.SessionListener)
meth public void clearAttributes()
meth public void removeChannelListener(org.apache.sshd.common.channel.ChannelListener)
meth public void removePortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
meth public void removeSessionListener(org.apache.sshd.common.session.SessionListener)
meth public void setAgentFactory(org.apache.sshd.agent.SshAgentFactory)
meth public void setChannelFactories(java.util.List<? extends org.apache.sshd.common.channel.ChannelFactory>)
meth public void setChannelStreamWriterResolver(org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver)
meth public void setFileSystemFactory(org.apache.sshd.common.file.FileSystemFactory)
meth public void setForwarderFactory(org.apache.sshd.common.forward.ForwarderFactory)
meth public void setForwardingFilter(org.apache.sshd.server.forward.ForwardingFilter)
meth public void setGlobalRequestHandlers(java.util.List<org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.session.ConnectionService>>)
meth public void setIoServiceEventListener(org.apache.sshd.common.io.IoServiceEventListener)
meth public void setIoServiceFactoryFactory(org.apache.sshd.common.io.IoServiceFactoryFactory)
meth public void setNioWorkers(int)
meth public void setParentPropertyResolver(org.apache.sshd.common.PropertyResolver)
meth public void setRandomFactory(org.apache.sshd.common.Factory<? extends org.apache.sshd.common.random.Random>)
meth public void setReservedSessionMessagesHandler(org.apache.sshd.common.session.ReservedSessionMessagesHandler)
meth public void setScheduledExecutorService(java.util.concurrent.ScheduledExecutorService)
meth public void setScheduledExecutorService(java.util.concurrent.ScheduledExecutorService,boolean)
meth public void setServiceFactories(java.util.List<? extends org.apache.sshd.common.ServiceFactory>)
meth public void setSessionDisconnectHandler(org.apache.sshd.common.session.SessionDisconnectHandler)
meth public void setUnknownChannelReferenceHandler(org.apache.sshd.common.session.UnknownChannelReferenceHandler)
supr org.apache.sshd.common.kex.AbstractKexFactoryManager
hfds attributes,channelStreamWriterResolver,eventListener,parentResolver,properties,reservedSessionMessagesHandler,sessionDisconnectHandler,unknownChannelReferenceHandler

CLSS public abstract org.apache.sshd.common.io.AbstractIoServiceFactory
cons protected init(org.apache.sshd.common.FactoryManager,org.apache.sshd.common.util.threads.CloseableExecutorService)
intf org.apache.sshd.common.FactoryManagerHolder
intf org.apache.sshd.common.io.IoServiceFactory
intf org.apache.sshd.common.util.threads.ExecutorServiceCarrier
meth protected <%0 extends org.apache.sshd.common.io.IoService> {%%0} autowireCreatedService({%%0})
meth protected void doCloseImmediately()
meth public final org.apache.sshd.common.FactoryManager getFactoryManager()
meth public final org.apache.sshd.common.util.threads.CloseableExecutorService getExecutorService()
meth public org.apache.sshd.common.io.IoServiceEventListener getIoServiceEventListener()
meth public static int getNioWorkers(org.apache.sshd.common.FactoryManager)
meth public void setIoServiceEventListener(org.apache.sshd.common.io.IoServiceEventListener)
supr org.apache.sshd.common.util.closeable.AbstractCloseable
hfds eventListener,executor,manager

CLSS public abstract org.apache.sshd.common.io.AbstractIoServiceFactoryFactory
cons protected init(org.apache.sshd.common.Factory<org.apache.sshd.common.util.threads.CloseableExecutorService>)
intf org.apache.sshd.common.io.IoServiceFactoryFactory
meth protected org.apache.sshd.common.util.threads.CloseableExecutorService newExecutor()
meth public org.apache.sshd.common.Factory<org.apache.sshd.common.util.threads.CloseableExecutorService> getExecutorServiceFactory()
meth public void setExecutorServiceFactory(org.apache.sshd.common.Factory<org.apache.sshd.common.util.threads.CloseableExecutorService>)
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds executorServiceFactory

CLSS public abstract org.apache.sshd.common.io.AbstractIoWriteFuture
cons protected init(java.lang.Object,java.lang.Object)
intf org.apache.sshd.common.io.IoWriteFuture
meth public !varargs org.apache.sshd.common.io.IoWriteFuture verify(long,org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public boolean isWritten()
meth public java.lang.Throwable getException()
meth public static org.apache.sshd.common.io.IoWriteFuture fulfilled(java.lang.Object,java.lang.Object)
supr org.apache.sshd.common.future.DefaultVerifiableSshFuture<org.apache.sshd.common.io.IoWriteFuture>

CLSS public final !enum org.apache.sshd.common.io.BuiltinIoServiceFactoryFactories
fld public final static java.util.Set<org.apache.sshd.common.io.BuiltinIoServiceFactoryFactories> VALUES
fld public final static org.apache.sshd.common.io.BuiltinIoServiceFactoryFactories MINA
fld public final static org.apache.sshd.common.io.BuiltinIoServiceFactoryFactories NETTY
fld public final static org.apache.sshd.common.io.BuiltinIoServiceFactoryFactories NIO2
intf org.apache.sshd.common.NamedFactory<org.apache.sshd.common.io.IoServiceFactoryFactory>
intf org.apache.sshd.common.OptionalFeature
meth public boolean isSupported()
meth public final java.lang.Class<? extends org.apache.sshd.common.io.IoServiceFactoryFactory> getFactoryClass()
meth public final java.lang.String getFactoryClassName()
meth public final java.lang.String getName()
meth public final org.apache.sshd.common.io.IoServiceFactoryFactory create()
meth public static org.apache.sshd.common.io.BuiltinIoServiceFactoryFactories fromFactoryClass(java.lang.Class<?>)
meth public static org.apache.sshd.common.io.BuiltinIoServiceFactoryFactories fromFactoryName(java.lang.String)
meth public static org.apache.sshd.common.io.BuiltinIoServiceFactoryFactories valueOf(java.lang.String)
meth public static org.apache.sshd.common.io.BuiltinIoServiceFactoryFactories[] values()
supr java.lang.Enum<org.apache.sshd.common.io.BuiltinIoServiceFactoryFactories>
hfds factoryClass,factoryClassName

CLSS public org.apache.sshd.common.io.DefaultIoConnectFuture
cons public init(java.lang.Object,java.lang.Object)
intf org.apache.sshd.common.io.IoConnectFuture
meth public !varargs org.apache.sshd.common.io.IoConnectFuture verify(long,org.apache.sshd.common.future.CancelOption[]) throws java.io.IOException
meth public boolean isConnected()
meth public org.apache.sshd.common.io.IoSession getSession()
meth public void setSession(org.apache.sshd.common.io.IoSession)
supr org.apache.sshd.common.future.DefaultCancellableSshFuture<org.apache.sshd.common.io.IoConnectFuture>

CLSS public org.apache.sshd.common.io.DefaultIoServiceFactoryFactory
cons protected init()
cons protected init(org.apache.sshd.common.Factory<org.apache.sshd.common.util.threads.CloseableExecutorService>)
meth public org.apache.sshd.common.io.IoServiceFactory create(org.apache.sshd.common.FactoryManager)
meth public org.apache.sshd.common.io.IoServiceFactoryFactory getIoServiceProvider()
meth public static <%0 extends org.apache.sshd.common.io.IoServiceFactoryFactory> {%%0} newInstance(java.lang.Class<? extends {%%0}>,java.lang.String)
meth public static <%0 extends org.apache.sshd.common.io.IoServiceFactoryFactory> {%%0} newInstance(java.lang.Class<{%%0}>)
meth public static <%0 extends org.apache.sshd.common.io.IoServiceFactoryFactory> {%%0} tryLoad(java.lang.String,java.util.ServiceLoader<{%%0}>)
meth public static org.apache.sshd.common.io.DefaultIoServiceFactoryFactory getDefaultIoServiceFactoryFactoryInstance()
supr org.apache.sshd.common.io.AbstractIoServiceFactoryFactory
hfds LOGGER,factory
hcls LazyDefaultIoServiceFactoryFactoryHolder

CLSS public abstract interface org.apache.sshd.common.io.IoAcceptor
intf org.apache.sshd.common.io.IoService
meth public abstract java.util.Set<java.net.SocketAddress> getBoundAddresses()
meth public abstract void bind(java.net.SocketAddress) throws java.io.IOException
meth public abstract void bind(java.util.Collection<? extends java.net.SocketAddress>) throws java.io.IOException
meth public abstract void unbind()
meth public abstract void unbind(java.net.SocketAddress)
meth public abstract void unbind(java.util.Collection<? extends java.net.SocketAddress>)

CLSS public abstract interface org.apache.sshd.common.io.IoConnectFuture
intf org.apache.sshd.common.future.Cancellable
intf org.apache.sshd.common.future.SshFuture<org.apache.sshd.common.io.IoConnectFuture>
meth public abstract boolean isConnected()
meth public abstract org.apache.sshd.common.io.IoSession getSession()
meth public abstract void setSession(org.apache.sshd.common.io.IoSession)

CLSS public abstract interface org.apache.sshd.common.io.IoConnector
intf org.apache.sshd.common.io.IoService
meth public abstract org.apache.sshd.common.io.IoConnectFuture connect(java.net.SocketAddress,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress)

CLSS public abstract interface org.apache.sshd.common.io.IoHandler
meth public abstract void exceptionCaught(org.apache.sshd.common.io.IoSession,java.lang.Throwable) throws java.lang.Exception
meth public abstract void messageReceived(org.apache.sshd.common.io.IoSession,org.apache.sshd.common.util.Readable) throws java.lang.Exception
meth public abstract void sessionClosed(org.apache.sshd.common.io.IoSession) throws java.lang.Exception
meth public abstract void sessionCreated(org.apache.sshd.common.io.IoSession) throws java.lang.Exception

CLSS public abstract interface org.apache.sshd.common.io.IoHandlerFactory
intf org.apache.sshd.common.Factory<org.apache.sshd.common.io.IoHandler>

CLSS public abstract interface org.apache.sshd.common.io.IoInputStream
intf org.apache.sshd.common.Closeable
meth public abstract org.apache.sshd.common.io.IoReadFuture read(org.apache.sshd.common.util.buffer.Buffer)

CLSS public abstract interface org.apache.sshd.common.io.IoOutputStream
intf org.apache.sshd.common.Closeable
meth public abstract org.apache.sshd.common.io.IoWriteFuture writeBuffer(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.common.io.IoReadFuture
intf org.apache.sshd.common.future.SshFuture<org.apache.sshd.common.io.IoReadFuture>
intf org.apache.sshd.common.future.VerifiableFuture<org.apache.sshd.common.io.IoReadFuture>
meth public abstract int getRead()
meth public abstract java.lang.Throwable getException()
meth public abstract org.apache.sshd.common.util.buffer.Buffer getBuffer()

CLSS public abstract interface org.apache.sshd.common.io.IoService
fld public final static boolean DEFAULT_REUSE_ADDRESS = true
intf org.apache.sshd.common.Closeable
intf org.apache.sshd.common.io.IoServiceEventListenerManager
meth public abstract java.util.Map<java.lang.Long,org.apache.sshd.common.io.IoSession> getManagedSessions()

CLSS public abstract interface org.apache.sshd.common.io.IoServiceEventListener
intf org.apache.sshd.common.util.SshdEventListener
meth public void abortAcceptedConnection(org.apache.sshd.common.io.IoAcceptor,java.net.SocketAddress,java.net.SocketAddress,java.net.SocketAddress,java.lang.Throwable) throws java.io.IOException
meth public void abortEstablishedConnection(org.apache.sshd.common.io.IoConnector,java.net.SocketAddress,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress,java.lang.Throwable) throws java.io.IOException
meth public void connectionAccepted(org.apache.sshd.common.io.IoAcceptor,java.net.SocketAddress,java.net.SocketAddress,java.net.SocketAddress) throws java.io.IOException
meth public void connectionEstablished(org.apache.sshd.common.io.IoConnector,java.net.SocketAddress,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.common.io.IoServiceEventListenerManager
meth public abstract org.apache.sshd.common.io.IoServiceEventListener getIoServiceEventListener()
meth public abstract void setIoServiceEventListener(org.apache.sshd.common.io.IoServiceEventListener)

CLSS public abstract interface org.apache.sshd.common.io.IoServiceFactory
intf org.apache.sshd.common.Closeable
intf org.apache.sshd.common.io.IoServiceEventListenerManager
meth public abstract org.apache.sshd.common.io.IoAcceptor createAcceptor(org.apache.sshd.common.io.IoHandler)
meth public abstract org.apache.sshd.common.io.IoConnector createConnector(org.apache.sshd.common.io.IoHandler)

CLSS public abstract interface org.apache.sshd.common.io.IoServiceFactoryFactory
meth public abstract org.apache.sshd.common.io.IoServiceFactory create(org.apache.sshd.common.FactoryManager)
meth public abstract void setExecutorServiceFactory(org.apache.sshd.common.Factory<org.apache.sshd.common.util.threads.CloseableExecutorService>)

CLSS public abstract interface org.apache.sshd.common.io.IoSession
intf org.apache.sshd.common.Closeable
intf org.apache.sshd.common.util.net.ConnectionEndpointsIndicator
meth public abstract java.lang.Object getAttribute(java.lang.Object)
meth public abstract java.lang.Object removeAttribute(java.lang.Object)
meth public abstract java.lang.Object setAttribute(java.lang.Object,java.lang.Object)
meth public abstract java.lang.Object setAttributeIfAbsent(java.lang.Object,java.lang.Object)
meth public abstract java.net.SocketAddress getAcceptanceAddress()
meth public abstract long getId()
meth public abstract org.apache.sshd.common.future.CloseFuture close(boolean)
meth public abstract org.apache.sshd.common.io.IoService getService()
meth public abstract org.apache.sshd.common.io.IoWriteFuture writeBuffer(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public abstract void resumeRead()
meth public abstract void shutdownOutputStream() throws java.io.IOException
meth public abstract void suspendRead()

CLSS public abstract interface org.apache.sshd.common.io.IoWriteFuture
intf org.apache.sshd.common.future.HasException
intf org.apache.sshd.common.future.SshFuture<org.apache.sshd.common.io.IoWriteFuture>
intf org.apache.sshd.common.future.VerifiableFuture<org.apache.sshd.common.io.IoWriteFuture>
meth public abstract boolean isWritten()

CLSS public org.apache.sshd.common.io.ReadPendingException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.IllegalStateException
hfds serialVersionUID

CLSS public org.apache.sshd.common.io.WritePendingException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.IllegalStateException
hfds serialVersionUID

CLSS public org.apache.sshd.common.io.nio2.Nio2Acceptor
cons public init(org.apache.sshd.common.io.nio2.Nio2ServiceFactory,org.apache.sshd.common.PropertyResolver,org.apache.sshd.common.io.IoHandler,java.nio.channels.AsynchronousChannelGroup,java.util.concurrent.ExecutorService)
fld protected final java.util.Map<java.net.SocketAddress,java.nio.channels.AsynchronousServerSocketChannel> channels
innr protected AcceptCompletionHandler
intf org.apache.sshd.common.io.IoAcceptor
meth protected java.io.Closeable protectInProgressBinding(java.net.SocketAddress,java.nio.channels.AsynchronousServerSocketChannel)
meth protected java.nio.channels.AsynchronousServerSocketChannel openAsynchronousServerSocketChannel(java.net.SocketAddress,java.nio.channels.AsynchronousChannelGroup) throws java.io.IOException
meth protected java.nio.channels.CompletionHandler<java.nio.channels.AsynchronousSocketChannel,? super java.net.SocketAddress> createSocketCompletionHandler(java.util.Map<java.net.SocketAddress,java.nio.channels.AsynchronousServerSocketChannel>,java.nio.channels.AsynchronousServerSocketChannel) throws java.io.IOException
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected void closeImmediately0()
meth protected void preClose()
meth public java.lang.String toString()
meth public java.util.Set<java.net.SocketAddress> getBoundAddresses()
meth public void bind(java.net.SocketAddress) throws java.io.IOException
meth public void bind(java.util.Collection<? extends java.net.SocketAddress>) throws java.io.IOException
meth public void unbind()
meth public void unbind(java.net.SocketAddress)
meth public void unbind(java.util.Collection<? extends java.net.SocketAddress>)
supr org.apache.sshd.common.io.nio2.Nio2Service
hfds backlog,nio2ServiceFactory

CLSS protected org.apache.sshd.common.io.nio2.Nio2Acceptor$AcceptCompletionHandler
 outer org.apache.sshd.common.io.nio2.Nio2Acceptor
fld protected final java.nio.channels.AsynchronousServerSocketChannel socket
meth protected boolean okToReaccept(java.lang.Throwable,java.net.SocketAddress)
meth protected org.apache.sshd.common.io.nio2.Nio2Session createSession(org.apache.sshd.common.io.nio2.Nio2Acceptor,java.net.SocketAddress,java.nio.channels.AsynchronousSocketChannel,org.apache.sshd.common.io.IoHandler) throws java.lang.Throwable
meth protected void onCompleted(java.nio.channels.AsynchronousSocketChannel,java.net.SocketAddress)
meth protected void onFailed(java.lang.Throwable,java.net.SocketAddress)
supr org.apache.sshd.common.io.nio2.Nio2CompletionHandler<java.nio.channels.AsynchronousSocketChannel,java.net.SocketAddress>

CLSS public abstract org.apache.sshd.common.io.nio2.Nio2CompletionHandler<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init()
intf java.nio.channels.CompletionHandler<{org.apache.sshd.common.io.nio2.Nio2CompletionHandler%0},{org.apache.sshd.common.io.nio2.Nio2CompletionHandler%1}>
meth protected abstract void onCompleted({org.apache.sshd.common.io.nio2.Nio2CompletionHandler%0},{org.apache.sshd.common.io.nio2.Nio2CompletionHandler%1})
meth protected abstract void onFailed(java.lang.Throwable,{org.apache.sshd.common.io.nio2.Nio2CompletionHandler%1})
meth public void completed({org.apache.sshd.common.io.nio2.Nio2CompletionHandler%0},{org.apache.sshd.common.io.nio2.Nio2CompletionHandler%1})
meth public void failed(java.lang.Throwable,{org.apache.sshd.common.io.nio2.Nio2CompletionHandler%1})
supr java.lang.Object

CLSS public org.apache.sshd.common.io.nio2.Nio2Connector
cons public init(org.apache.sshd.common.io.nio2.Nio2ServiceFactory,org.apache.sshd.common.PropertyResolver,org.apache.sshd.common.io.IoHandler,java.nio.channels.AsynchronousChannelGroup,java.util.concurrent.ExecutorService)
innr protected ConnectionCompletionHandler
intf org.apache.sshd.common.io.IoConnector
meth protected java.nio.channels.AsynchronousSocketChannel openAsynchronousSocketChannel(java.net.SocketAddress,java.nio.channels.AsynchronousChannelGroup) throws java.io.IOException
meth protected org.apache.sshd.common.io.nio2.Nio2CompletionHandler<java.lang.Void,java.lang.Object> createConnectionCompletionHandler(org.apache.sshd.common.io.IoConnectFuture,java.nio.channels.AsynchronousSocketChannel,org.apache.sshd.common.AttributeRepository,org.apache.sshd.common.PropertyResolver,org.apache.sshd.common.io.IoHandler)
meth protected org.apache.sshd.common.io.nio2.Nio2Session createSession(org.apache.sshd.common.PropertyResolver,org.apache.sshd.common.io.IoHandler,java.nio.channels.AsynchronousSocketChannel) throws java.lang.Throwable
meth public org.apache.sshd.common.io.IoConnectFuture connect(java.net.SocketAddress,org.apache.sshd.common.AttributeRepository,java.net.SocketAddress)
supr org.apache.sshd.common.io.nio2.Nio2Service
hfds nio2ServiceFactory

CLSS protected org.apache.sshd.common.io.nio2.Nio2Connector$ConnectionCompletionHandler
 outer org.apache.sshd.common.io.nio2.Nio2Connector
cons protected init(org.apache.sshd.common.io.nio2.Nio2Connector,org.apache.sshd.common.io.IoConnectFuture,java.nio.channels.AsynchronousSocketChannel,org.apache.sshd.common.AttributeRepository,org.apache.sshd.common.PropertyResolver,org.apache.sshd.common.io.IoHandler)
fld protected final java.nio.channels.AsynchronousSocketChannel socket
fld protected final org.apache.sshd.common.AttributeRepository context
fld protected final org.apache.sshd.common.PropertyResolver propertyResolver
fld protected final org.apache.sshd.common.io.IoConnectFuture future
fld protected final org.apache.sshd.common.io.IoHandler handler
meth protected void onCompleted(java.lang.Void,java.lang.Object)
meth protected void onFailed(java.lang.Throwable,java.lang.Object)
supr org.apache.sshd.common.io.nio2.Nio2CompletionHandler<java.lang.Void,java.lang.Object>

CLSS public org.apache.sshd.common.io.nio2.Nio2DefaultIoWriteFuture
cons public init(java.lang.Object,java.lang.Object,java.nio.ByteBuffer)
meth public java.nio.ByteBuffer getBuffer()
meth public void setException(java.lang.Throwable)
meth public void setWritten()
supr org.apache.sshd.common.io.AbstractIoWriteFuture
hfds buffer

CLSS public abstract org.apache.sshd.common.io.nio2.Nio2Service
cons protected init(org.apache.sshd.common.PropertyResolver,org.apache.sshd.common.io.IoHandler,java.nio.channels.AsynchronousChannelGroup,java.util.concurrent.ExecutorService)
fld protected final java.util.Map<java.lang.Long,org.apache.sshd.common.io.IoSession> sessions
fld protected final java.util.concurrent.atomic.AtomicBoolean disposing
fld protected final org.apache.sshd.common.PropertyResolver propertyResolver
fld public final static java.util.Map<org.apache.sshd.common.Property<?>,java.util.AbstractMap$SimpleImmutableEntry<java.net.SocketOption<?>,java.lang.Object>> CONFIGURABLE_OPTIONS
intf org.apache.sshd.common.io.IoService
meth protected <%0 extends java.lang.Object> boolean setOption(java.nio.channels.NetworkChannel,org.apache.sshd.common.Property<?>,java.net.SocketOption<{%%0}>,{%%0}) throws java.io.IOException
meth protected <%0 extends java.nio.channels.NetworkChannel> {%%0} setSocketOptions({%%0}) throws java.io.IOException
meth protected java.nio.channels.AsynchronousChannelGroup getChannelGroup()
meth protected java.util.concurrent.ExecutorService getExecutorService()
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected org.apache.sshd.common.io.IoSession mapSession(org.apache.sshd.common.io.IoSession)
meth protected void unmapSession(java.lang.Long)
meth public java.util.Map<java.lang.Long,org.apache.sshd.common.io.IoSession> getManagedSessions()
meth public org.apache.sshd.common.io.IoHandler getIoHandler()
meth public org.apache.sshd.common.io.IoServiceEventListener getIoServiceEventListener()
meth public void dispose()
meth public void sessionClosed(org.apache.sshd.common.io.nio2.Nio2Session)
meth public void setIoServiceEventListener(org.apache.sshd.common.io.IoServiceEventListener)
supr org.apache.sshd.common.util.closeable.AbstractInnerCloseable
hfds eventListener,executor,group,handler,noMoreSessions

CLSS public org.apache.sshd.common.io.nio2.Nio2ServiceFactory
cons public init(org.apache.sshd.common.FactoryManager,org.apache.sshd.common.util.threads.CloseableExecutorService,org.apache.sshd.common.util.threads.CloseableExecutorService)
meth protected void doCloseImmediately()
meth public org.apache.sshd.common.io.IoAcceptor createAcceptor(org.apache.sshd.common.io.IoHandler)
meth public org.apache.sshd.common.io.IoConnector createConnector(org.apache.sshd.common.io.IoHandler)
meth public org.apache.sshd.common.io.nio2.Nio2Session createSession(org.apache.sshd.common.io.nio2.Nio2Service,org.apache.sshd.common.io.IoHandler,java.nio.channels.AsynchronousSocketChannel,java.net.SocketAddress) throws java.lang.Throwable
supr org.apache.sshd.common.io.AbstractIoServiceFactory
hfds group,resuming

CLSS public org.apache.sshd.common.io.nio2.Nio2ServiceFactoryFactory
cons public init()
cons public init(org.apache.sshd.common.Factory<org.apache.sshd.common.util.threads.CloseableExecutorService>)
meth public org.apache.sshd.common.io.IoServiceFactory create(org.apache.sshd.common.FactoryManager)
supr org.apache.sshd.common.io.AbstractIoServiceFactoryFactory

CLSS public org.apache.sshd.common.io.nio2.Nio2Session
cons public init(org.apache.sshd.common.io.nio2.Nio2Service,org.apache.sshd.common.PropertyResolver,org.apache.sshd.common.io.IoHandler,java.nio.channels.AsynchronousSocketChannel,java.net.SocketAddress) throws java.io.IOException
fld public final static int DEFAULT_READBUF_SIZE = 32768
intf org.apache.sshd.common.io.IoSession
meth protected org.apache.sshd.common.future.CloseFuture doCloseGracefully()
meth protected org.apache.sshd.common.io.nio2.Nio2CompletionHandler<java.lang.Integer,java.lang.Object> createReadCycleCompletionHandler(java.nio.ByteBuffer,org.apache.sshd.common.util.Readable)
meth protected org.apache.sshd.common.io.nio2.Nio2CompletionHandler<java.lang.Integer,java.lang.Object> createWriteCycleCompletionHandler(org.apache.sshd.common.io.nio2.Nio2DefaultIoWriteFuture,java.nio.channels.AsynchronousSocketChannel,java.nio.ByteBuffer)
meth protected void doCloseImmediately()
meth protected void doReadCycle(java.nio.ByteBuffer,org.apache.sshd.common.io.nio2.Nio2CompletionHandler<java.lang.Integer,java.lang.Object>)
meth protected void doReadCycle(java.nio.ByteBuffer,org.apache.sshd.common.util.Readable)
meth protected void doShutdownOutputStream(org.apache.sshd.common.io.nio2.Nio2DefaultIoWriteFuture,java.nio.channels.AsynchronousSocketChannel) throws java.io.IOException
meth protected void doWriteCycle(java.nio.ByteBuffer,org.apache.sshd.common.io.nio2.Nio2CompletionHandler<java.lang.Integer,java.lang.Object>)
meth protected void exceptionCaught(java.lang.Throwable)
meth protected void finishWrite(org.apache.sshd.common.io.nio2.Nio2DefaultIoWriteFuture)
meth protected void handleCompletedWriteCycle(org.apache.sshd.common.io.nio2.Nio2DefaultIoWriteFuture,java.nio.channels.AsynchronousSocketChannel,java.nio.ByteBuffer,int,org.apache.sshd.common.io.nio2.Nio2CompletionHandler<java.lang.Integer,java.lang.Object>,java.lang.Integer,java.lang.Object)
meth protected void handleReadCycleCompletion(java.nio.ByteBuffer,org.apache.sshd.common.util.Readable,org.apache.sshd.common.io.nio2.Nio2CompletionHandler<java.lang.Integer,java.lang.Object>,java.lang.Integer,java.lang.Object)
meth protected void handleReadCycleFailure(java.nio.ByteBuffer,org.apache.sshd.common.util.Readable,java.lang.Throwable,java.lang.Object)
meth protected void handleWriteCycleFailure(org.apache.sshd.common.io.nio2.Nio2DefaultIoWriteFuture,java.nio.channels.AsynchronousSocketChannel,java.nio.ByteBuffer,int,java.lang.Throwable,java.lang.Object)
meth protected void startWriting()
meth public java.lang.Object getAttribute(java.lang.Object)
meth public java.lang.Object removeAttribute(java.lang.Object)
meth public java.lang.Object setAttribute(java.lang.Object,java.lang.Object)
meth public java.lang.Object setAttributeIfAbsent(java.lang.Object,java.lang.Object)
meth public java.lang.String toString()
meth public java.net.SocketAddress getAcceptanceAddress()
meth public java.net.SocketAddress getLocalAddress()
meth public java.net.SocketAddress getRemoteAddress()
meth public java.nio.channels.AsynchronousSocketChannel getSocket()
meth public long getId()
meth public org.apache.sshd.common.io.IoHandler getIoHandler()
meth public org.apache.sshd.common.io.IoWriteFuture writeBuffer(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public org.apache.sshd.common.io.nio2.Nio2Service getService()
meth public void resumeRead()
meth public void shutdownOutputStream() throws java.io.IOException
meth public void startReading()
meth public void startReading(byte[])
meth public void startReading(byte[],int,int)
meth public void startReading(int)
meth public void startReading(java.nio.ByteBuffer)
meth public void suspend()
meth public void suspendRead()
supr org.apache.sshd.common.util.closeable.AbstractCloseable
hfds SESSION_ID_GENERATOR,acceptanceAddress,attributes,currentWrite,id,ioHandler,lastReadCycleStart,lastWriteCycleStart,localAddress,outputShutDown,propertyResolver,readCyclesCounter,readRunnable,readerThread,remoteAddress,service,socketChannel,suspend,suspendLock,writeCyclesCounter,writes

CLSS public abstract org.apache.sshd.common.kex.AbstractDH
cons protected init()
fld protected javax.crypto.KeyAgreement myKeyAgree
meth protected abstract byte[] calculateE() throws java.lang.Exception
meth protected abstract byte[] calculateK() throws java.lang.Exception
meth protected void checkKeyAgreementNecessity()
meth public abstract org.apache.sshd.common.digest.Digest getHash() throws java.lang.Exception
meth public abstract void setF(byte[])
meth public boolean isPublicDataAvailable()
meth public boolean isSharedSecretAvailable()
meth public byte[] getE() throws java.lang.Exception
meth public byte[] getK() throws java.lang.Exception
meth public java.lang.String toString()
meth public org.apache.sshd.common.kex.KeyEncapsulationMethod getKeyEncapsulation()
meth public static byte[] stripLeadingZeroes(byte[])
meth public void putE(org.apache.sshd.common.util.buffer.Buffer,byte[])
meth public void putF(org.apache.sshd.common.util.buffer.Buffer,byte[])
supr java.lang.Object
hfds e_array,k_array

CLSS public abstract org.apache.sshd.common.kex.AbstractKexFactoryManager
cons protected init()
cons protected init(org.apache.sshd.common.kex.KexFactoryManager)
intf org.apache.sshd.common.kex.KexFactoryManager
meth protected <%0 extends java.lang.Object, %1 extends java.util.Collection<{%%0}>> {%%1} resolveEffectiveFactories({%%1},{%%1})
meth protected <%0 extends java.lang.Object> {%%0} resolveEffectiveProvider(java.lang.Class<{%%0}>,{%%0},{%%0})
meth protected org.apache.sshd.common.kex.KexFactoryManager getDelegate()
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.cipher.Cipher>> getCipherFactories()
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.compression.Compression>> getCompressionFactories()
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac>> getMacFactories()
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories()
meth public java.util.List<org.apache.sshd.common.kex.KeyExchangeFactory> getKeyExchangeFactories()
meth public org.apache.sshd.common.kex.extension.KexExtensionHandler getKexExtensionHandler()
meth public void setCipherFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.cipher.Cipher>>)
meth public void setCompressionFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.compression.Compression>>)
meth public void setKexExtensionHandler(org.apache.sshd.common.kex.extension.KexExtensionHandler)
meth public void setKeyExchangeFactories(java.util.List<org.apache.sshd.common.kex.KeyExchangeFactory>)
meth public void setMacFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac>>)
meth public void setSignatureFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
supr org.apache.sshd.common.util.closeable.AbstractInnerCloseable
hfds cipherFactories,compressionFactories,delegate,kexExtensionHandler,keyExchangeFactories,macFactories,signatureFactories

CLSS public abstract !enum org.apache.sshd.common.kex.BuiltinDHFactories
fld public final static java.util.Set<org.apache.sshd.common.kex.BuiltinDHFactories> VALUES
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories curve25519
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories curve25519_libssh
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories curve448
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories dhg1
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories dhg14
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories dhg14_256
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories dhg15_512
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories dhg16_512
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories dhg17_512
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories dhg18_512
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories dhgex
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories dhgex256
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories ecdhp256
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories ecdhp384
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories ecdhp521
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories mlkem1024nistp384
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories mlkem768nistp256
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories mlkem768x25519
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories sntrup761x25519
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories sntrup761x25519_openssh
innr public final static Constants
innr public final static ParseResult
intf org.apache.sshd.common.kex.DHFactory
meth public !varargs static org.apache.sshd.common.kex.BuiltinDHFactories$ParseResult parseDHFactoriesList(java.lang.String[])
meth public boolean isGroupExchange()
meth public boolean isSupported()
meth public final java.lang.String getName()
meth public final java.lang.String toString()
meth public static java.util.NavigableSet<org.apache.sshd.common.kex.DHFactory> getRegisteredExtensions()
meth public static org.apache.sshd.common.kex.BuiltinDHFactories fromFactoryName(java.lang.String)
meth public static org.apache.sshd.common.kex.BuiltinDHFactories valueOf(java.lang.String)
meth public static org.apache.sshd.common.kex.BuiltinDHFactories$ParseResult parseDHFactoriesList(java.lang.String)
meth public static org.apache.sshd.common.kex.BuiltinDHFactories$ParseResult parseDHFactoriesList(java.util.Collection<java.lang.String>)
meth public static org.apache.sshd.common.kex.BuiltinDHFactories[] values()
meth public static org.apache.sshd.common.kex.DHFactory resolveFactory(java.lang.String)
meth public static org.apache.sshd.common.kex.DHFactory unregisterExtension(java.lang.String)
meth public static void registerExtension(org.apache.sshd.common.kex.DHFactory)
supr java.lang.Enum<org.apache.sshd.common.kex.BuiltinDHFactories>
hfds EXTENSIONS,factoryName

CLSS public final static org.apache.sshd.common.kex.BuiltinDHFactories$Constants
 outer org.apache.sshd.common.kex.BuiltinDHFactories
fld public final static java.lang.String CURVE25519_SHA256 = "curve25519-sha256"
fld public final static java.lang.String CURVE25519_SHA256_LIBSSH = "curve25519-sha256@libssh.org"
fld public final static java.lang.String CURVE448_SHA512 = "curve448-sha512"
fld public final static java.lang.String DIFFIE_HELLMAN_GROUP14_SHA1 = "diffie-hellman-group14-sha1"
fld public final static java.lang.String DIFFIE_HELLMAN_GROUP14_SHA256 = "diffie-hellman-group14-sha256"
fld public final static java.lang.String DIFFIE_HELLMAN_GROUP15_SHA512 = "diffie-hellman-group15-sha512"
fld public final static java.lang.String DIFFIE_HELLMAN_GROUP16_SHA512 = "diffie-hellman-group16-sha512"
fld public final static java.lang.String DIFFIE_HELLMAN_GROUP17_SHA512 = "diffie-hellman-group17-sha512"
fld public final static java.lang.String DIFFIE_HELLMAN_GROUP18_SHA512 = "diffie-hellman-group18-sha512"
fld public final static java.lang.String DIFFIE_HELLMAN_GROUP1_SHA1 = "diffie-hellman-group1-sha1"
fld public final static java.lang.String DIFFIE_HELLMAN_GROUP_EXCHANGE_SHA1 = "diffie-hellman-group-exchange-sha1"
fld public final static java.lang.String DIFFIE_HELLMAN_GROUP_EXCHANGE_SHA256 = "diffie-hellman-group-exchange-sha256"
fld public final static java.lang.String ECDH_SHA2_NISTP256 = "ecdh-sha2-nistp256"
fld public final static java.lang.String ECDH_SHA2_NISTP384 = "ecdh-sha2-nistp384"
fld public final static java.lang.String ECDH_SHA2_NISTP521 = "ecdh-sha2-nistp521"
fld public final static java.lang.String MLKEM1024_NISTP384_SHA384 = "mlkem1024nistp384-sha384"
fld public final static java.lang.String MLKEM768_25519_SHA256 = "mlkem768x25519-sha256"
fld public final static java.lang.String MLKEM768_NISTP256_SHA256 = "mlkem768nistp256-sha256"
fld public final static java.lang.String SNTRUP761_25519_SHA512 = "sntrup761x25519-sha512"
fld public final static java.lang.String SNTRUP761_25519_SHA512_OPENSSH = "sntrup761x25519-sha512@openssh.com"
supr java.lang.Object

CLSS public final static org.apache.sshd.common.kex.BuiltinDHFactories$ParseResult
 outer org.apache.sshd.common.kex.BuiltinDHFactories
cons public init(java.util.List<org.apache.sshd.common.kex.DHFactory>,java.util.List<java.lang.String>)
fld public final static org.apache.sshd.common.kex.BuiltinDHFactories$ParseResult EMPTY
meth public java.util.List<java.lang.String> getUnsupportedFactories()
meth public java.util.List<org.apache.sshd.common.kex.DHFactory> getParsedFactories()
supr org.apache.sshd.common.config.NamedResourceListParseResult<org.apache.sshd.common.kex.DHFactory>

CLSS public abstract !enum org.apache.sshd.common.kex.BuiltinKEM
fld public final static org.apache.sshd.common.kex.BuiltinKEM mlkem1024
fld public final static org.apache.sshd.common.kex.BuiltinKEM mlkem768
fld public final static org.apache.sshd.common.kex.BuiltinKEM sntrup761
intf org.apache.sshd.common.NamedResource
intf org.apache.sshd.common.OptionalFeature
intf org.apache.sshd.common.kex.KeyEncapsulationMethod
meth public java.lang.String getName()
meth public static org.apache.sshd.common.kex.BuiltinKEM valueOf(java.lang.String)
meth public static org.apache.sshd.common.kex.BuiltinKEM[] values()
supr java.lang.Enum<org.apache.sshd.common.kex.BuiltinKEM>
hfds name

CLSS public abstract interface org.apache.sshd.common.kex.CurveSizeIndicator
meth public abstract int getByteLength()

CLSS public abstract interface org.apache.sshd.common.kex.DHFactory
intf org.apache.sshd.common.NamedResource
intf org.apache.sshd.common.OptionalFeature
meth public abstract !varargs org.apache.sshd.common.kex.AbstractDH create(java.lang.Object[]) throws java.lang.Exception
meth public abstract boolean isGroupExchange()

CLSS public org.apache.sshd.common.kex.DHG
cons public init(org.apache.sshd.common.Factory<? extends org.apache.sshd.common.digest.Digest>) throws java.lang.Exception
cons public init(org.apache.sshd.common.Factory<? extends org.apache.sshd.common.digest.Digest>,java.math.BigInteger,java.math.BigInteger) throws java.lang.Exception
fld public final static java.lang.String KEX_TYPE = "DH"
meth protected byte[] calculateE() throws java.lang.Exception
meth protected byte[] calculateK() throws java.lang.Exception
meth public java.lang.String toString()
meth public java.math.BigInteger getG()
meth public java.math.BigInteger getP()
meth public org.apache.sshd.common.digest.Digest getHash() throws java.lang.Exception
meth public void setF(byte[])
meth public void setF(java.math.BigInteger)
meth public void setG(byte[])
meth public void setG(java.math.BigInteger)
meth public void setP(byte[])
meth public void setP(java.math.BigInteger)
supr org.apache.sshd.common.kex.AbstractDH
hfds f,factory,g,p

CLSS public final org.apache.sshd.common.kex.DHGroupData
meth public static byte[] getG()
meth public static byte[] getOakleyGroupPrimeValue(java.lang.String)
meth public static byte[] getP1()
meth public static byte[] getP14()
meth public static byte[] getP15()
meth public static byte[] getP16()
meth public static byte[] getP17()
meth public static byte[] getP18()
meth public static byte[] parseOakleyGroupPrimeValue(java.lang.String)
meth public static byte[] readOakleyGroupPrimeValue(java.io.BufferedReader) throws java.io.IOException
meth public static byte[] readOakleyGroupPrimeValue(java.io.InputStream) throws java.io.IOException
meth public static byte[] readOakleyGroupPrimeValue(java.io.Reader) throws java.io.IOException
meth public static byte[] readOakleyGroupPrimeValue(java.lang.String)
meth public static byte[] readOakleyGroupPrimeValue(java.util.stream.Stream<java.lang.String>)
supr java.lang.Object
hfds OAKLEY_GROUPS

CLSS public org.apache.sshd.common.kex.ECDH
cons public init(java.lang.String) throws java.lang.Exception
cons public init(java.lang.String,boolean) throws java.lang.Exception
cons public init(java.security.spec.ECParameterSpec) throws java.lang.Exception
cons public init(java.security.spec.ECParameterSpec,boolean) throws java.lang.Exception
cons public init(org.apache.sshd.common.cipher.ECCurves) throws java.lang.Exception
cons public init(org.apache.sshd.common.cipher.ECCurves,boolean) throws java.lang.Exception
fld public final static java.lang.String KEX_TYPE = "ECDH"
meth protected byte[] calculateE() throws java.lang.Exception
meth protected byte[] calculateK() throws java.lang.Exception
meth public java.lang.String toString()
meth public org.apache.sshd.common.digest.Digest getHash() throws java.lang.Exception
meth public void putE(org.apache.sshd.common.util.buffer.Buffer,byte[])
meth public void putF(org.apache.sshd.common.util.buffer.Buffer,byte[])
meth public void setF(byte[])
supr org.apache.sshd.common.kex.AbstractDH
hfds curve,f,params,raw

CLSS public abstract interface org.apache.sshd.common.kex.KexFactoryManager
intf org.apache.sshd.common.kex.extension.KexExtensionHandlerManager
intf org.apache.sshd.common.signature.SignatureFactoriesManager
meth public !varargs void setCipherFactoriesNames(java.lang.String[])
meth public !varargs void setCompressionFactoriesNames(java.lang.String[])
meth public !varargs void setMacFactoriesNames(java.lang.String[])
meth public abstract java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.cipher.Cipher>> getCipherFactories()
meth public abstract java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.compression.Compression>> getCompressionFactories()
meth public abstract java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac>> getMacFactories()
meth public abstract java.util.List<org.apache.sshd.common.kex.KeyExchangeFactory> getKeyExchangeFactories()
meth public abstract void setCipherFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.cipher.Cipher>>)
meth public abstract void setCompressionFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.compression.Compression>>)
meth public abstract void setKeyExchangeFactories(java.util.List<org.apache.sshd.common.kex.KeyExchangeFactory>)
meth public abstract void setMacFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac>>)
meth public java.lang.String getCipherFactoriesNameList()
meth public java.lang.String getCompressionFactoriesNameList()
meth public java.lang.String getMacFactoriesNameList()
meth public java.util.List<java.lang.String> getCipherFactoriesNames()
meth public java.util.List<java.lang.String> getCompressionFactoriesNames()
meth public java.util.List<java.lang.String> getMacFactoriesNames()
meth public void setCipherFactoriesNameList(java.lang.String)
meth public void setCipherFactoriesNames(java.util.Collection<java.lang.String>)
meth public void setCompressionFactoriesNameList(java.lang.String)
meth public void setCompressionFactoriesNames(java.util.Collection<java.lang.String>)
meth public void setMacFactoriesNameList(java.lang.String)
meth public void setMacFactoriesNames(java.util.Collection<java.lang.String>)

CLSS public final !enum org.apache.sshd.common.kex.KexProposalOption
fld public final static int PROPOSAL_MAX
fld public final static java.util.Comparator<org.apache.sshd.common.kex.KexProposalOption> BY_PROPOSAL_INDEX
fld public final static java.util.List<org.apache.sshd.common.kex.KexProposalOption> VALUES
fld public final static java.util.Set<org.apache.sshd.common.kex.KexProposalOption> CIPHER_PROPOSALS
fld public final static java.util.Set<org.apache.sshd.common.kex.KexProposalOption> COMPRESSION_PROPOSALS
fld public final static java.util.Set<org.apache.sshd.common.kex.KexProposalOption> FIRST_KEX_PACKET_GUESS_MATCHES
fld public final static java.util.Set<org.apache.sshd.common.kex.KexProposalOption> LANGUAGE_PROPOSALS
fld public final static java.util.Set<org.apache.sshd.common.kex.KexProposalOption> MAC_PROPOSALS
fld public final static org.apache.sshd.common.kex.KexProposalOption ALGORITHMS
fld public final static org.apache.sshd.common.kex.KexProposalOption C2SCOMP
fld public final static org.apache.sshd.common.kex.KexProposalOption C2SENC
fld public final static org.apache.sshd.common.kex.KexProposalOption C2SLANG
fld public final static org.apache.sshd.common.kex.KexProposalOption C2SMAC
fld public final static org.apache.sshd.common.kex.KexProposalOption S2CCOMP
fld public final static org.apache.sshd.common.kex.KexProposalOption S2CENC
fld public final static org.apache.sshd.common.kex.KexProposalOption S2CLANG
fld public final static org.apache.sshd.common.kex.KexProposalOption S2CMAC
fld public final static org.apache.sshd.common.kex.KexProposalOption SERVERKEYS
innr public final static Constants
meth public final int getProposalIndex()
meth public final java.lang.String getDescription()
meth public static org.apache.sshd.common.kex.KexProposalOption fromName(java.lang.String)
meth public static org.apache.sshd.common.kex.KexProposalOption fromProposalIndex(int)
meth public static org.apache.sshd.common.kex.KexProposalOption valueOf(java.lang.String)
meth public static org.apache.sshd.common.kex.KexProposalOption[] values()
supr java.lang.Enum<org.apache.sshd.common.kex.KexProposalOption>
hfds description,proposalIndex

CLSS public final static org.apache.sshd.common.kex.KexProposalOption$Constants
 outer org.apache.sshd.common.kex.KexProposalOption
fld public final static int PROPOSAL_COMP_ALGS_CTOS = 6
fld public final static int PROPOSAL_COMP_ALGS_STOC = 7
fld public final static int PROPOSAL_ENC_ALGS_CTOS = 2
fld public final static int PROPOSAL_ENC_ALGS_STOC = 3
fld public final static int PROPOSAL_KEX_ALGS = 0
fld public final static int PROPOSAL_LANG_CTOS = 8
fld public final static int PROPOSAL_LANG_STOC = 9
fld public final static int PROPOSAL_MAC_ALGS_CTOS = 4
fld public final static int PROPOSAL_MAC_ALGS_STOC = 5
fld public final static int PROPOSAL_SERVER_HOST_KEY_ALGS = 1
supr java.lang.Object

CLSS public final !enum org.apache.sshd.common.kex.KexState
fld public final static java.util.Set<org.apache.sshd.common.kex.KexState> VALUES
fld public final static org.apache.sshd.common.kex.KexState DONE
fld public final static org.apache.sshd.common.kex.KexState INIT
fld public final static org.apache.sshd.common.kex.KexState KEYS
fld public final static org.apache.sshd.common.kex.KexState RUN
fld public final static org.apache.sshd.common.kex.KexState UNKNOWN
meth public static org.apache.sshd.common.kex.KexState valueOf(java.lang.String)
meth public static org.apache.sshd.common.kex.KexState[] values()
supr java.lang.Enum<org.apache.sshd.common.kex.KexState>

CLSS public abstract interface org.apache.sshd.common.kex.KeyEncapsulationMethod
innr public abstract interface static Client
innr public abstract interface static Server
meth public abstract org.apache.sshd.common.kex.KeyEncapsulationMethod$Client getClient()
meth public abstract org.apache.sshd.common.kex.KeyEncapsulationMethod$Server getServer()

CLSS public abstract interface static org.apache.sshd.common.kex.KeyEncapsulationMethod$Client
 outer org.apache.sshd.common.kex.KeyEncapsulationMethod
meth public abstract byte[] extractSecret(byte[])
meth public abstract byte[] getPublicKey()
meth public abstract int getEncapsulationLength()
meth public abstract void init()

CLSS public abstract interface static org.apache.sshd.common.kex.KeyEncapsulationMethod$Server
 outer org.apache.sshd.common.kex.KeyEncapsulationMethod
meth public abstract byte[] getEncapsulation()
meth public abstract byte[] getSecret()
meth public abstract byte[] init(byte[])
meth public abstract int getPublicKeyLength()

CLSS public abstract interface org.apache.sshd.common.kex.KeyExchange
fld public final static java.util.NavigableMap<java.lang.Integer,java.lang.String> GROUP_KEX_OPCODES_MAP
fld public final static java.util.NavigableMap<java.lang.Integer,java.lang.String> SIMPLE_KEX_OPCODES_MAP
intf org.apache.sshd.common.NamedResource
intf org.apache.sshd.common.session.SessionHolder<org.apache.sshd.common.session.Session>
meth public abstract boolean next(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public abstract byte[] getH()
meth public abstract byte[] getK()
meth public abstract org.apache.sshd.common.digest.Digest getHash()
meth public abstract void init(byte[],byte[],byte[],byte[]) throws java.lang.Exception
meth public static boolean isValidDHValue(java.math.BigInteger,java.math.BigInteger)
meth public static java.lang.String getGroupKexOpcodeName(int)
meth public static java.lang.String getSimpleKexOpcodeName(int)

CLSS public abstract interface org.apache.sshd.common.kex.KeyExchangeFactory
intf org.apache.sshd.common.NamedResource
meth public abstract org.apache.sshd.common.kex.KeyExchange createKeyExchange(org.apache.sshd.common.session.Session) throws java.lang.Exception

CLSS public final !enum org.apache.sshd.common.kex.MontgomeryCurve
fld public final static org.apache.sshd.common.kex.MontgomeryCurve x25519
fld public final static org.apache.sshd.common.kex.MontgomeryCurve x448
intf org.apache.sshd.common.OptionalFeature
intf org.apache.sshd.common.kex.CurveSizeIndicator
intf org.apache.sshd.common.keyprovider.KeySizeIndicator
meth public boolean isSupported()
meth public byte[] encode(java.security.PublicKey) throws java.security.InvalidKeyException
meth public int getByteLength()
meth public int getKeySize()
meth public java.lang.String getAlgorithm()
meth public java.security.KeyPair generateKeyPair()
meth public java.security.PublicKey decode(byte[]) throws java.security.spec.InvalidKeySpecException
meth public javax.crypto.KeyAgreement createKeyAgreement() throws java.security.GeneralSecurityException
meth public static org.apache.sshd.common.kex.MontgomeryCurve valueOf(java.lang.String)
meth public static org.apache.sshd.common.kex.MontgomeryCurve[] values()
supr java.lang.Enum<org.apache.sshd.common.kex.MontgomeryCurve>
hfds algorithm,encodedPublicKeyPrefix,keyFactory,keyPairGenerator,keySize,supported

CLSS public abstract org.apache.sshd.common.kex.XDH
cons public init(org.apache.sshd.common.kex.MontgomeryCurve,boolean) throws java.lang.Exception
fld protected byte[] f
fld protected final boolean raw
fld protected final org.apache.sshd.common.kex.MontgomeryCurve curve
intf org.apache.sshd.common.kex.CurveSizeIndicator
meth protected byte[] calculateE() throws java.lang.Exception
meth protected byte[] calculateK() throws java.lang.Exception
meth public int getByteLength()
meth public void putE(org.apache.sshd.common.util.buffer.Buffer,byte[])
meth public void putF(org.apache.sshd.common.util.buffer.Buffer,byte[])
meth public void setF(byte[])
supr org.apache.sshd.common.kex.AbstractDH

CLSS public abstract org.apache.sshd.common.kex.dh.AbstractDHKeyExchange
cons protected init(org.apache.sshd.common.session.Session)
fld protected byte[] h
fld protected byte[] i_c
fld protected byte[] i_s
fld protected byte[] k
fld protected byte[] v_c
fld protected byte[] v_s
fld protected org.apache.sshd.common.digest.Digest hash
intf org.apache.sshd.common.kex.KeyExchange
meth protected byte[] getE()
meth protected byte[] getF()
meth protected byte[] normalize(byte[])
meth protected byte[] updateE(byte[])
meth protected byte[] updateE(org.apache.sshd.common.util.buffer.Buffer)
meth protected byte[] updateF(byte[])
meth protected byte[] updateF(org.apache.sshd.common.util.buffer.Buffer)
meth protected java.math.BigInteger getEValue()
meth protected java.math.BigInteger getFValue()
meth protected void setE(byte[])
meth protected void setF(byte[])
meth protected void validateEValue(java.math.BigInteger) throws org.apache.sshd.common.SshException
meth protected void validateFValue(java.math.BigInteger) throws org.apache.sshd.common.SshException
meth public byte[] getH()
meth public byte[] getK()
meth public java.lang.String toString()
meth public org.apache.sshd.common.digest.Digest getHash()
meth public org.apache.sshd.common.session.Session getSession()
meth public void init(byte[],byte[],byte[],byte[]) throws java.lang.Exception
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds e,eValue,f,fValue,session

CLSS public org.apache.sshd.common.kex.extension.DefaultClientKexExtensionHandler
cons public init()
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<java.lang.Integer> HOSTBOUND_AUTHENTICATION
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<java.util.Set<java.lang.String>> SERVER_ALGORITHMS
fld public final static org.apache.sshd.common.kex.extension.DefaultClientKexExtensionHandler INSTANCE
intf org.apache.sshd.common.kex.extension.KexExtensionHandler
meth protected void handleServerSignatureAlgorithms(org.apache.sshd.common.session.Session,java.util.Collection<java.lang.String>)
meth public boolean handleKexExtensionRequest(org.apache.sshd.common.session.Session,int,int,java.lang.String,byte[]) throws java.io.IOException
meth public boolean isKexExtensionsAvailable(org.apache.sshd.common.session.Session,org.apache.sshd.common.kex.extension.KexExtensionHandler$AvailabilityPhase) throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public org.apache.sshd.common.kex.extension.DefaultServerKexExtensionHandler
cons public init()
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<java.lang.Boolean> CLIENT_REQUESTED_EXT_INFO
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<java.lang.Boolean> EXT_INFO_SENT_AT_NEWKEYS
fld public final static org.apache.sshd.common.kex.extension.DefaultServerKexExtensionHandler INSTANCE
intf org.apache.sshd.common.kex.extension.KexExtensionHandler
meth public void collectExtensions(org.apache.sshd.common.session.Session,org.apache.sshd.common.kex.extension.KexExtensionHandler$KexPhase,java.util.function.BiConsumer<java.lang.String,java.lang.Object>)
meth public void handleKexInitProposal(org.apache.sshd.common.session.Session,boolean,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>) throws java.lang.Exception
meth public void sendKexExtensions(org.apache.sshd.common.session.Session,org.apache.sshd.common.kex.extension.KexExtensionHandler$KexPhase) throws java.lang.Exception
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public abstract interface org.apache.sshd.common.kex.extension.KexExtensionHandler
innr public final static !enum AvailabilityPhase
innr public final static !enum KexPhase
meth public boolean handleKexCompressionMessage(org.apache.sshd.common.session.Session,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public boolean handleKexExtensionRequest(org.apache.sshd.common.session.Session,int,int,java.lang.String,byte[]) throws java.lang.Exception
meth public boolean handleKexExtensionsMessage(org.apache.sshd.common.session.Session,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public boolean isKexExtensionsAvailable(org.apache.sshd.common.session.Session,org.apache.sshd.common.kex.extension.KexExtensionHandler$AvailabilityPhase) throws java.io.IOException
meth public void handleKexExtensionNegotiation(org.apache.sshd.common.session.Session,org.apache.sshd.common.kex.KexProposalOption,java.lang.String,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.lang.String,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.lang.String) throws java.lang.Exception
meth public void handleKexInitProposal(org.apache.sshd.common.session.Session,boolean,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>) throws java.lang.Exception
meth public void sendKexExtensions(org.apache.sshd.common.session.Session,org.apache.sshd.common.kex.extension.KexExtensionHandler$KexPhase) throws java.lang.Exception

CLSS public final static !enum org.apache.sshd.common.kex.extension.KexExtensionHandler$AvailabilityPhase
 outer org.apache.sshd.common.kex.extension.KexExtensionHandler
fld public final static org.apache.sshd.common.kex.extension.KexExtensionHandler$AvailabilityPhase AUTHOK
fld public final static org.apache.sshd.common.kex.extension.KexExtensionHandler$AvailabilityPhase NEWKEYS
fld public final static org.apache.sshd.common.kex.extension.KexExtensionHandler$AvailabilityPhase PREKEX
fld public final static org.apache.sshd.common.kex.extension.KexExtensionHandler$AvailabilityPhase PROPOSAL
meth public static org.apache.sshd.common.kex.extension.KexExtensionHandler$AvailabilityPhase valueOf(java.lang.String)
meth public static org.apache.sshd.common.kex.extension.KexExtensionHandler$AvailabilityPhase[] values()
supr java.lang.Enum<org.apache.sshd.common.kex.extension.KexExtensionHandler$AvailabilityPhase>

CLSS public final static !enum org.apache.sshd.common.kex.extension.KexExtensionHandler$KexPhase
 outer org.apache.sshd.common.kex.extension.KexExtensionHandler
fld public final static java.util.Set<org.apache.sshd.common.kex.extension.KexExtensionHandler$KexPhase> VALUES
fld public final static org.apache.sshd.common.kex.extension.KexExtensionHandler$KexPhase AUTHOK
fld public final static org.apache.sshd.common.kex.extension.KexExtensionHandler$KexPhase NEWKEYS
meth public static org.apache.sshd.common.kex.extension.KexExtensionHandler$KexPhase valueOf(java.lang.String)
meth public static org.apache.sshd.common.kex.extension.KexExtensionHandler$KexPhase[] values()
supr java.lang.Enum<org.apache.sshd.common.kex.extension.KexExtensionHandler$KexPhase>

CLSS public abstract interface org.apache.sshd.common.kex.extension.KexExtensionHandlerManager
meth public abstract org.apache.sshd.common.kex.extension.KexExtensionHandler getKexExtensionHandler()
meth public abstract void setKexExtensionHandler(org.apache.sshd.common.kex.extension.KexExtensionHandler)

CLSS public abstract interface org.apache.sshd.common.kex.extension.KexExtensionParser<%0 extends java.lang.Object>
intf org.apache.sshd.common.NamedResource
meth public abstract void putExtension({org.apache.sshd.common.kex.extension.KexExtensionParser%0},org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public abstract {org.apache.sshd.common.kex.extension.KexExtensionParser%0} parseExtension(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public {org.apache.sshd.common.kex.extension.KexExtensionParser%0} parseExtension(byte[]) throws java.io.IOException
meth public {org.apache.sshd.common.kex.extension.KexExtensionParser%0} parseExtension(byte[],int,int) throws java.io.IOException

CLSS public final org.apache.sshd.common.kex.extension.KexExtensions
fld public final static byte SSH_MSG_EXT_INFO = 7
fld public final static byte SSH_MSG_NEWCOMPRESS = 8
fld public final static java.lang.String CLIENT_KEX_EXTENSION = "ext-info-c"
fld public final static java.lang.String SERVER_KEX_EXTENSION = "ext-info-s"
fld public final static java.lang.String STRICT_KEX_CLIENT_EXTENSION = "kex-strict-c-v00@openssh.com"
fld public final static java.lang.String STRICT_KEX_SERVER_EXTENSION = "kex-strict-s-v00@openssh.com"
fld public final static java.util.function.Predicate<java.lang.String> IS_KEX_EXTENSION_SIGNAL
meth public static java.util.List<java.util.Map$Entry<java.lang.String,?>> parseExtensions(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public static java.util.NavigableSet<java.lang.String> getRegisteredExtensionParserNames()
meth public static org.apache.sshd.common.kex.extension.KexExtensionParser<?> getRegisteredExtensionParser(java.lang.String)
meth public static org.apache.sshd.common.kex.extension.KexExtensionParser<?> registerExtensionParser(org.apache.sshd.common.kex.extension.KexExtensionParser<?>)
meth public static org.apache.sshd.common.kex.extension.KexExtensionParser<?> unregisterExtensionParser(java.lang.String)
meth public static void putExtensions(java.util.Collection<? extends java.util.Map$Entry<java.lang.String,?>>,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr java.lang.Object
hfds EXTENSION_PARSERS

CLSS public abstract org.apache.sshd.common.kex.extension.parser.AbstractKexExtensionParser<%0 extends java.lang.Object>
cons protected init(java.lang.String)
intf org.apache.sshd.common.kex.extension.KexExtensionParser<{org.apache.sshd.common.kex.extension.parser.AbstractKexExtensionParser%0}>
meth protected abstract void encode({org.apache.sshd.common.kex.extension.parser.AbstractKexExtensionParser%0},org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public java.lang.String getName()
meth public void putExtension({org.apache.sshd.common.kex.extension.parser.AbstractKexExtensionParser%0},org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr java.lang.Object
hfds name

CLSS public org.apache.sshd.common.kex.extension.parser.DelayCompression
cons public init()
fld public final static java.lang.String NAME = "delay-compression"
fld public final static org.apache.sshd.common.kex.extension.parser.DelayCompression INSTANCE
meth protected void encode(org.apache.sshd.common.kex.extension.parser.DelayedCompressionAlgorithms,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public org.apache.sshd.common.kex.extension.parser.DelayedCompressionAlgorithms parseExtension(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr org.apache.sshd.common.kex.extension.parser.AbstractKexExtensionParser<org.apache.sshd.common.kex.extension.parser.DelayedCompressionAlgorithms>

CLSS public org.apache.sshd.common.kex.extension.parser.DelayedCompressionAlgorithms
cons public init()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getClient2Server()
meth public java.util.List<java.lang.String> getServer2Client()
meth public org.apache.sshd.common.kex.extension.parser.DelayedCompressionAlgorithms withClient2Server(java.util.List<java.lang.String>)
meth public org.apache.sshd.common.kex.extension.parser.DelayedCompressionAlgorithms withServer2Client(java.util.List<java.lang.String>)
meth public void setClient2Server(java.util.List<java.lang.String>)
meth public void setServer2Client(java.util.List<java.lang.String>)
supr java.lang.Object
hfds client2server,server2client

CLSS public org.apache.sshd.common.kex.extension.parser.Elevation
cons public init()
fld public final static java.lang.String NAME = "elevation"
fld public final static org.apache.sshd.common.kex.extension.parser.Elevation INSTANCE
meth protected void encode(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public java.lang.String parseExtension(byte[],int,int) throws java.io.IOException
meth public java.lang.String parseExtension(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr org.apache.sshd.common.kex.extension.parser.AbstractKexExtensionParser<java.lang.String>

CLSS public org.apache.sshd.common.kex.extension.parser.HostBoundPubkeyAuthentication
cons public init()
fld public final static java.lang.String AUTH_NAME = "publickey-hostbound-v00@openssh.com"
fld public final static java.lang.String NAME = "publickey-hostbound@openssh.com"
fld public final static org.apache.sshd.common.kex.extension.parser.HostBoundPubkeyAuthentication INSTANCE
meth protected void encode(java.lang.Integer,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public java.lang.Integer parseExtension(byte[],int,int) throws java.io.IOException
meth public java.lang.Integer parseExtension(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr org.apache.sshd.common.kex.extension.parser.AbstractKexExtensionParser<java.lang.Integer>
hfds LOG

CLSS public org.apache.sshd.common.kex.extension.parser.NoFlowControl
cons public init()
fld public final static java.lang.String NAME = "no-flow-control"
fld public final static org.apache.sshd.common.kex.extension.parser.NoFlowControl INSTANCE
meth protected void encode(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public java.lang.String parseExtension(byte[],int,int) throws java.io.IOException
meth public java.lang.String parseExtension(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr org.apache.sshd.common.kex.extension.parser.AbstractKexExtensionParser<java.lang.String>

CLSS public org.apache.sshd.common.kex.extension.parser.ServerSignatureAlgorithms
cons public init()
fld public final static java.lang.String NAME = "server-sig-algs"
fld public final static org.apache.sshd.common.kex.extension.parser.ServerSignatureAlgorithms INSTANCE
meth protected void encode(java.util.List<java.lang.String>,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public java.util.List<java.lang.String> parseExtension(byte[],int,int) throws java.io.IOException
meth public java.util.List<java.lang.String> parseExtension(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr org.apache.sshd.common.kex.extension.parser.AbstractKexExtensionParser<java.util.List<java.lang.String>>

CLSS public abstract org.apache.sshd.common.keyprovider.AbstractKeyPairProvider
cons protected init()
intf org.apache.sshd.common.keyprovider.KeyPairProvider
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public abstract org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider<%0 extends java.lang.Object>
cons protected init()
innr protected KeyPairIterator
meth protected java.io.InputStream openKeyPairResource(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,{org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider%0}) throws java.io.IOException
meth protected java.lang.Iterable<java.security.KeyPair> doLoadKeys(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.io.InputStream,org.apache.sshd.common.config.keys.FilePasswordProvider) throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.lang.Iterable<java.security.KeyPair> doLoadKeys(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,{org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider%0},org.apache.sshd.common.config.keys.FilePasswordProvider) throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.lang.Iterable<java.security.KeyPair> doLoadKeys(org.apache.sshd.common.session.SessionContext,{org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider%0}) throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.lang.Iterable<java.security.KeyPair> loadKeys(org.apache.sshd.common.session.SessionContext,java.util.Collection<? extends {org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider%0}>)
meth protected org.apache.sshd.common.util.io.resource.IoResource<?> getIoResource(org.apache.sshd.common.session.SessionContext,{org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider%0})
meth protected void resetCacheMap(java.util.Collection<?>)
meth public org.apache.sshd.common.config.keys.FilePasswordProvider getPasswordFinder()
meth public void setPasswordFinder(org.apache.sshd.common.config.keys.FilePasswordProvider)
supr org.apache.sshd.common.keyprovider.AbstractKeyPairProvider
hfds cacheMap,passwordFinder

CLSS protected org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider$KeyPairIterator
 outer org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider
cons protected init(org.apache.sshd.common.session.SessionContext,java.util.Collection<? extends {org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider%0}>)
fld protected final org.apache.sshd.common.session.SessionContext session
intf java.util.Iterator<java.security.KeyPair>
meth public boolean hasNext()
meth public java.security.KeyPair next()
meth public void remove()
supr java.lang.Object
hfds currentIdentities,iterator,nextKeyPair,nextKeyPairSet

CLSS public org.apache.sshd.common.keyprovider.ClassLoadableResourceKeyPairProvider
cons public init()
cons public init(java.lang.ClassLoader)
cons public init(java.lang.ClassLoader,java.lang.String)
cons public init(java.lang.ClassLoader,java.util.Collection<java.lang.String>)
cons public init(java.lang.String)
cons public init(java.util.Collection<java.lang.String>)
meth protected java.lang.ClassLoader resolveClassLoader()
meth protected org.apache.sshd.common.util.io.resource.IoResource<?> getIoResource(org.apache.sshd.common.session.SessionContext,java.lang.String)
meth public java.lang.ClassLoader getResourceLoader()
meth public java.lang.Iterable<java.security.KeyPair> loadKeys(org.apache.sshd.common.session.SessionContext)
meth public java.util.Collection<java.lang.String> getResources()
meth public void setResourceLoader(java.lang.ClassLoader)
meth public void setResources(java.util.Collection<java.lang.String>)
supr org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider<java.lang.String>
hfds classLoader,resources

CLSS public org.apache.sshd.common.keyprovider.FileHostKeyCertificateProvider
cons public !varargs init(java.nio.file.Path[])
cons public init(java.nio.file.Path)
cons public init(java.util.Collection<? extends java.nio.file.Path>)
intf org.apache.sshd.common.keyprovider.HostKeyCertificateProvider
meth public java.lang.Iterable<org.apache.sshd.common.config.keys.OpenSshCertificate> loadCertificates(org.apache.sshd.common.session.SessionContext) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<? extends java.nio.file.Path> getPaths()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds files

CLSS public org.apache.sshd.common.keyprovider.FileKeyPairProvider
cons public !varargs init(java.nio.file.Path[])
cons public init()
cons public init(java.nio.file.Path)
cons public init(java.util.Collection<? extends java.nio.file.Path>)
meth protected java.lang.Iterable<java.security.KeyPair> doLoadKeys(org.apache.sshd.common.session.SessionContext,java.nio.file.Path) throws java.io.IOException,java.security.GeneralSecurityException
meth protected org.apache.sshd.common.util.io.resource.IoResource<java.nio.file.Path> getIoResource(org.apache.sshd.common.session.SessionContext,java.nio.file.Path)
meth public java.lang.Iterable<java.security.KeyPair> loadKeys(org.apache.sshd.common.session.SessionContext)
meth public java.util.Collection<? extends java.nio.file.Path> getPaths()
meth public void setPaths(java.util.Collection<? extends java.nio.file.Path>)
supr org.apache.sshd.common.keyprovider.AbstractResourceKeyPairProvider<java.nio.file.Path>
hfds files

CLSS public abstract interface org.apache.sshd.common.keyprovider.HostKeyCertificateProvider
 anno 0 java.lang.FunctionalInterface()
meth public abstract java.lang.Iterable<org.apache.sshd.common.config.keys.OpenSshCertificate> loadCertificates(org.apache.sshd.common.session.SessionContext) throws java.io.IOException,java.security.GeneralSecurityException
meth public org.apache.sshd.common.config.keys.OpenSshCertificate loadCertificate(org.apache.sshd.common.session.SessionContext,java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException

CLSS public abstract interface org.apache.sshd.common.keyprovider.KeyIdentityProvider
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.common.keyprovider.KeyIdentityProvider EMPTY_KEYS_PROVIDER
meth public !varargs static org.apache.sshd.common.keyprovider.KeyIdentityProvider multiProvider(org.apache.sshd.common.keyprovider.KeyIdentityProvider[])
meth public !varargs static org.apache.sshd.common.keyprovider.KeyIdentityProvider wrapKeyPairs(java.security.KeyPair[])
meth public abstract java.lang.Iterable<java.security.KeyPair> loadKeys(org.apache.sshd.common.session.SessionContext) throws java.io.IOException,java.security.GeneralSecurityException
meth public static boolean isEmpty(org.apache.sshd.common.keyprovider.KeyIdentityProvider)
meth public static java.lang.Iterable<java.security.KeyPair> iterableOf(org.apache.sshd.common.session.SessionContext,java.util.Collection<? extends org.apache.sshd.common.keyprovider.KeyIdentityProvider>)
meth public static java.security.KeyPair exhaustCurrentIdentities(java.util.Iterator<java.security.KeyPair>)
meth public static org.apache.sshd.common.keyprovider.KeyIdentityProvider multiProvider(java.util.Collection<? extends org.apache.sshd.common.keyprovider.KeyIdentityProvider>)
meth public static org.apache.sshd.common.keyprovider.KeyIdentityProvider resolveKeyIdentityProvider(org.apache.sshd.common.keyprovider.KeyIdentityProvider,org.apache.sshd.common.keyprovider.KeyIdentityProvider)
meth public static org.apache.sshd.common.keyprovider.KeyIdentityProvider wrapKeyPairs(java.lang.Iterable<java.security.KeyPair>)

CLSS public abstract interface org.apache.sshd.common.keyprovider.KeyIdentityProviderHolder
meth public abstract org.apache.sshd.common.keyprovider.KeyIdentityProvider getKeyIdentityProvider()
meth public abstract void setKeyIdentityProvider(org.apache.sshd.common.keyprovider.KeyIdentityProvider)

CLSS public abstract interface org.apache.sshd.common.keyprovider.KeyPairProvider
fld public final static java.lang.String ECDSA_SHA2_NISTP256
fld public final static java.lang.String ECDSA_SHA2_NISTP384
fld public final static java.lang.String ECDSA_SHA2_NISTP521
fld public final static java.lang.String SSH_DSS = "ssh-dss"
fld public final static java.lang.String SSH_DSS_CERT = "ssh-dss-cert-v01@openssh.com"
fld public final static java.lang.String SSH_ECDSA_SHA2_NISTP256_CERT = "ecdsa-sha2-nistp256-cert-v01@openssh.com"
fld public final static java.lang.String SSH_ECDSA_SHA2_NISTP384_CERT = "ecdsa-sha2-nistp384-cert-v01@openssh.com"
fld public final static java.lang.String SSH_ECDSA_SHA2_NISTP521_CERT = "ecdsa-sha2-nistp521-cert-v01@openssh.com"
fld public final static java.lang.String SSH_ED25519 = "ssh-ed25519"
fld public final static java.lang.String SSH_ED25519_CERT = "ssh-ed25519-cert-v01@openssh.com"
fld public final static java.lang.String SSH_RSA = "ssh-rsa"
fld public final static java.lang.String SSH_RSA_CERT = "ssh-rsa-cert-v01@openssh.com"
fld public final static org.apache.sshd.common.keyprovider.KeyPairProvider EMPTY_KEYPAIR_PROVIDER
intf org.apache.sshd.common.keyprovider.KeyIdentityProvider
meth public !varargs static org.apache.sshd.common.keyprovider.KeyPairProvider wrap(java.security.KeyPair[])
meth public java.lang.Iterable<java.lang.String> getKeyTypes(org.apache.sshd.common.session.SessionContext) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.security.KeyPair loadKey(org.apache.sshd.common.session.SessionContext,java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException
meth public static org.apache.sshd.common.keyprovider.KeyPairProvider wrap(java.lang.Iterable<java.security.KeyPair>)

CLSS public abstract interface org.apache.sshd.common.keyprovider.KeyPairProviderHolder
meth public abstract org.apache.sshd.common.keyprovider.KeyPairProvider getKeyPairProvider()
meth public abstract void setKeyPairProvider(org.apache.sshd.common.keyprovider.KeyPairProvider)

CLSS public abstract interface org.apache.sshd.common.keyprovider.KeySizeIndicator
meth public abstract int getKeySize()

CLSS public abstract interface org.apache.sshd.common.keyprovider.KeyTypeIndicator
 anno 0 java.lang.FunctionalInterface()
meth public abstract java.lang.String getKeyType()
meth public static <%0 extends org.apache.sshd.common.keyprovider.KeyTypeIndicator> java.util.NavigableMap<java.lang.String,java.util.List<{%%0}>> groupByKeyType(java.util.Collection<? extends {%%0}>)

CLSS public org.apache.sshd.common.keyprovider.MappedKeyPairProvider
cons public !varargs init(java.security.KeyPair[])
cons public init(java.util.Collection<java.security.KeyPair>)
cons public init(java.util.Map<java.lang.String,java.security.KeyPair>)
fld public final static java.util.function.Function<java.util.Map<java.lang.String,java.security.KeyPair>,org.apache.sshd.common.keyprovider.KeyPairProvider> MAP_TO_KEY_PAIR_PROVIDER
intf org.apache.sshd.common.keyprovider.KeyPairProvider
meth public java.lang.Iterable<java.lang.String> getKeyTypes(org.apache.sshd.common.session.SessionContext)
meth public java.lang.Iterable<java.security.KeyPair> loadKeys(org.apache.sshd.common.session.SessionContext)
meth public java.lang.String toString()
meth public java.security.KeyPair loadKey(org.apache.sshd.common.session.SessionContext,java.lang.String)
meth public static java.util.Map<java.lang.String,java.security.KeyPair> mapUniquePairs(java.util.Collection<java.security.KeyPair>)
supr java.lang.Object
hfds pairsMap

CLSS public org.apache.sshd.common.keyprovider.MultiKeyIdentityIterator
cons public init(org.apache.sshd.common.session.SessionContext,java.lang.Iterable<? extends org.apache.sshd.common.keyprovider.KeyIdentityProvider>)
fld protected boolean finished
fld protected java.util.Iterator<java.security.KeyPair> currentProvider
intf java.util.Iterator<java.security.KeyPair>
intf org.apache.sshd.common.session.SessionContextHolder
meth public boolean hasNext()
meth public java.security.KeyPair next()
meth public java.util.Iterator<? extends org.apache.sshd.common.keyprovider.KeyIdentityProvider> getProviders()
meth public org.apache.sshd.common.session.SessionContext getSessionContext()
supr java.lang.Object
hfds providers,sessionContext

CLSS public org.apache.sshd.common.keyprovider.MultiKeyIdentityProvider
cons public init(java.lang.Iterable<? extends org.apache.sshd.common.keyprovider.KeyIdentityProvider>)
fld protected final java.lang.Iterable<? extends org.apache.sshd.common.keyprovider.KeyIdentityProvider> providers
intf org.apache.sshd.common.keyprovider.KeyIdentityProvider
meth public java.lang.Iterable<? extends org.apache.sshd.common.keyprovider.KeyIdentityProvider> getProviders()
meth public java.lang.Iterable<java.security.KeyPair> loadKeys(org.apache.sshd.common.session.SessionContext)
supr java.lang.Object

CLSS public org.apache.sshd.common.mac.BaseMac
cons public init(java.lang.String,int,int,boolean)
intf org.apache.sshd.common.mac.Mac
meth public boolean isEncryptThenMac()
meth public int getBlockSize()
meth public int getDefaultBlockSize()
meth public java.lang.String getAlgorithm()
meth public java.lang.String toString()
meth public void doFinal(byte[],int) throws java.lang.Exception
meth public void init(byte[]) throws java.lang.Exception
meth public void update(byte[],int,int)
meth public void updateUInt(long)
supr java.lang.Object
hfds algorithm,bsize,defbsize,etmMode,mac,s,tmp

CLSS public !enum org.apache.sshd.common.mac.BuiltinMacs
fld public final static java.util.Set<org.apache.sshd.common.mac.BuiltinMacs> VALUES
fld public final static org.apache.sshd.common.mac.BuiltinMacs hmacmd5
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.mac.BuiltinMacs hmacmd596
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.mac.BuiltinMacs hmacsha1
fld public final static org.apache.sshd.common.mac.BuiltinMacs hmacsha196
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.mac.BuiltinMacs hmacsha1etm
fld public final static org.apache.sshd.common.mac.BuiltinMacs hmacsha256
fld public final static org.apache.sshd.common.mac.BuiltinMacs hmacsha256etm
fld public final static org.apache.sshd.common.mac.BuiltinMacs hmacsha512
fld public final static org.apache.sshd.common.mac.BuiltinMacs hmacsha512etm
innr public final static Constants
innr public final static ParseResult
intf org.apache.sshd.common.mac.MacFactory
meth public !varargs static org.apache.sshd.common.mac.BuiltinMacs$ParseResult parseMacsList(java.lang.String[])
meth public final boolean isSupported()
meth public final int getBlockSize()
meth public final int getDefaultBlockSize()
meth public final java.lang.String getAlgorithm()
meth public final java.lang.String getName()
meth public final java.lang.String toString()
meth public org.apache.sshd.common.mac.Mac create()
meth public static java.util.NavigableSet<org.apache.sshd.common.mac.MacFactory> getRegisteredExtensions()
meth public static org.apache.sshd.common.mac.BuiltinMacs fromFactory(org.apache.sshd.common.NamedFactory<org.apache.sshd.common.mac.Mac>)
meth public static org.apache.sshd.common.mac.BuiltinMacs fromFactoryName(java.lang.String)
meth public static org.apache.sshd.common.mac.BuiltinMacs fromString(java.lang.String)
meth public static org.apache.sshd.common.mac.BuiltinMacs valueOf(java.lang.String)
meth public static org.apache.sshd.common.mac.BuiltinMacs$ParseResult parseMacsList(java.lang.String)
meth public static org.apache.sshd.common.mac.BuiltinMacs$ParseResult parseMacsList(java.util.Collection<java.lang.String>)
meth public static org.apache.sshd.common.mac.BuiltinMacs[] values()
meth public static org.apache.sshd.common.mac.MacFactory resolveFactory(java.lang.String)
meth public static org.apache.sshd.common.mac.MacFactory unregisterExtension(java.lang.String)
meth public static void registerExtension(org.apache.sshd.common.mac.MacFactory)
supr java.lang.Enum<org.apache.sshd.common.mac.BuiltinMacs>
hfds EXTENSIONS,algorithm,bsize,defbsize,factoryName

CLSS public final static org.apache.sshd.common.mac.BuiltinMacs$Constants
 outer org.apache.sshd.common.mac.BuiltinMacs
fld public final static java.lang.String ETM_HMAC_SHA1 = "hmac-sha1-etm@openssh.com"
fld public final static java.lang.String ETM_HMAC_SHA2_256 = "hmac-sha2-256-etm@openssh.com"
fld public final static java.lang.String ETM_HMAC_SHA2_512 = "hmac-sha2-512-etm@openssh.com"
fld public final static java.lang.String HMAC_MD5 = "hmac-md5"
fld public final static java.lang.String HMAC_MD5_96 = "hmac-md5-96"
fld public final static java.lang.String HMAC_SHA1 = "hmac-sha1"
fld public final static java.lang.String HMAC_SHA1_96 = "hmac-sha1-96"
fld public final static java.lang.String HMAC_SHA2_256 = "hmac-sha2-256"
fld public final static java.lang.String HMAC_SHA2_512 = "hmac-sha2-512"
supr java.lang.Object

CLSS public final static org.apache.sshd.common.mac.BuiltinMacs$ParseResult
 outer org.apache.sshd.common.mac.BuiltinMacs
cons public init(java.util.List<org.apache.sshd.common.mac.MacFactory>,java.util.List<java.lang.String>)
fld public final static org.apache.sshd.common.mac.BuiltinMacs$ParseResult EMPTY
supr org.apache.sshd.common.config.NamedFactoriesListParseResult<org.apache.sshd.common.mac.Mac,org.apache.sshd.common.mac.MacFactory>

CLSS public abstract interface org.apache.sshd.common.mac.Mac
intf org.apache.sshd.common.mac.MacInformation
meth public abstract void doFinal(byte[],int) throws java.lang.Exception
meth public abstract void init(byte[]) throws java.lang.Exception
meth public abstract void update(byte[],int,int)
meth public abstract void updateUInt(long)
meth public byte[] doFinal() throws java.lang.Exception
meth public static boolean equals(byte[],int,byte[],int,int)
meth public void doFinal(byte[]) throws java.lang.Exception
meth public void update(byte[])

CLSS public abstract interface org.apache.sshd.common.mac.MacFactory
intf org.apache.sshd.common.BuiltinFactory<org.apache.sshd.common.mac.Mac>
intf org.apache.sshd.common.mac.MacInformation

CLSS public abstract interface org.apache.sshd.common.mac.MacInformation
intf org.apache.sshd.common.AlgorithmNameProvider
meth public abstract int getBlockSize()
meth public abstract int getDefaultBlockSize()
meth public boolean isEncryptThenMac()

CLSS public org.apache.sshd.common.mac.Poly1305Mac
cons public init()
fld public final static int KEY_BYTES = 32
intf org.apache.sshd.common.mac.Mac
meth public int getBlockSize()
meth public int getDefaultBlockSize()
meth public java.lang.String getAlgorithm()
meth public static int unpackIntLE(byte[],int)
meth public static void packIntLE(int,byte[],int)
meth public void doFinal(byte[],int) throws java.lang.Exception
meth public void init(byte[]) throws java.lang.Exception
meth public void update(byte[],int,int)
meth public void updateUInt(long)
supr java.lang.Object
hfds BLOCK_SIZE,currentBlock,currentBlockOffset,h0,h1,h2,h3,h4,k0,k1,k2,k3,r0,r1,r2,r3,r4,s1,s2,s3,s4

CLSS abstract interface org.apache.sshd.common.mac.package-info

CLSS public final org.apache.sshd.common.net.InetAddressRange
meth public boolean contains(byte[])
meth public boolean contains(java.net.InetAddress)
meth public boolean contains(org.apache.sshd.common.net.InetAddressRange)
meth public boolean equals(java.lang.Object)
meth public boolean isIpV4()
meth public boolean isIpV6()
meth public boolean overlaps(org.apache.sshd.common.net.InetAddressRange)
meth public byte[] broadcastAddress()
meth public byte[] first(boolean)
meth public byte[] last(boolean)
meth public int hashCode()
meth public int networkZoneBits()
meth public int subnetBits()
meth public java.lang.String toString()
meth public long numberOfAddresses(boolean)
meth public static boolean isCIDR(java.lang.String)
meth public static org.apache.sshd.common.net.InetAddressRange fromCIDR(java.lang.String)
supr java.lang.Object
hfds BITS,IP4_BYTE,IP4_CIDR,IP4_DOT_BYTE,IP4_PREFIX,IP6_CIDR,IP6_PART,IP6_WORD,base,broadcast,mask,networkZoneBits
hcls Builder

CLSS public abstract org.apache.sshd.common.random.AbstractRandom
cons protected init()
intf org.apache.sshd.common.random.Random
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract org.apache.sshd.common.random.AbstractRandomFactory
cons protected init(java.lang.String)
intf org.apache.sshd.common.random.RandomFactory
meth public final java.lang.String getName()
meth public java.lang.String toString()
supr java.lang.Object
hfds name

CLSS public org.apache.sshd.common.random.JceRandom
cons public init()
fld public final static java.lang.String NAME = "JCE"
meth public int random(int)
meth public java.lang.String getName()
meth public static java.security.SecureRandom getGlobalInstance()
meth public void fill(byte[],int,int)
supr org.apache.sshd.common.random.AbstractRandom
hfds random,tmp
hcls Cache

CLSS public org.apache.sshd.common.random.JceRandomFactory
cons public init()
fld public final static java.lang.String NAME = "default"
fld public final static org.apache.sshd.common.random.JceRandomFactory INSTANCE
meth public boolean isSupported()
meth public org.apache.sshd.common.random.Random create()
supr org.apache.sshd.common.random.AbstractRandomFactory

CLSS public abstract interface org.apache.sshd.common.random.Random
intf org.apache.sshd.common.NamedResource
meth public abstract int random(int)
meth public abstract void fill(byte[],int,int)
meth public void fill(byte[])

CLSS public abstract interface org.apache.sshd.common.random.RandomFactory
intf org.apache.sshd.common.BuiltinFactory<org.apache.sshd.common.random.Random>

CLSS public org.apache.sshd.common.random.SingletonRandomFactory
cons public init(org.apache.sshd.common.NamedFactory<org.apache.sshd.common.random.Random>)
intf org.apache.sshd.common.random.RandomFactory
meth public boolean isSupported()
meth public int random(int)
meth public java.lang.String getName()
meth public org.apache.sshd.common.random.Random create()
meth public void fill(byte[],int,int)
supr org.apache.sshd.common.random.AbstractRandom
hfds factory,random

CLSS abstract interface org.apache.sshd.common.random.package-info

CLSS public abstract org.apache.sshd.common.session.AbstractConnectionServiceFactory
cons protected init()
intf org.apache.sshd.common.forward.PortForwardingEventListenerManager
meth public org.apache.sshd.common.forward.PortForwardingEventListener getPortForwardingEventListenerProxy()
meth public void addPortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
meth public void removePortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds listenerProxy,listeners

CLSS public abstract interface org.apache.sshd.common.session.ConnectionService
intf org.apache.sshd.common.Service
intf org.apache.sshd.common.forward.PortForwardingEventListenerManager
intf org.apache.sshd.common.forward.PortForwardingEventListenerManagerHolder
intf org.apache.sshd.common.session.SessionHeartbeatController
intf org.apache.sshd.common.session.UnknownChannelReferenceHandlerManager
meth public abstract boolean isAllowMoreSessions()
meth public abstract long registerChannel(org.apache.sshd.common.channel.Channel) throws java.io.IOException
meth public abstract org.apache.sshd.agent.common.AgentForwardSupport getAgentForwardSupport()
meth public abstract org.apache.sshd.common.forward.Forwarder getForwarder()
meth public abstract org.apache.sshd.server.x11.X11ForwardSupport getX11ForwardSupport()
meth public abstract void setAllowMoreSessions(boolean)
meth public abstract void unregisterChannel(org.apache.sshd.common.channel.Channel)

CLSS public abstract interface org.apache.sshd.common.session.ConnectionServiceRequestHandler
fld public final static java.util.function.Function<org.apache.sshd.common.session.ConnectionServiceRequestHandler,org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.session.ConnectionService>> SVC2HNDLR
intf org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.session.ConnectionService>
meth public abstract org.apache.sshd.common.channel.RequestHandler$Result process(org.apache.sshd.common.session.ConnectionService,java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception

CLSS public abstract interface org.apache.sshd.common.session.ReservedSessionMessagesHandler
intf org.apache.sshd.common.util.SshdEventListener
meth public boolean handleUnimplementedMessage(org.apache.sshd.common.session.Session,int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public boolean sendReservedHeartbeat(org.apache.sshd.common.session.ConnectionService) throws java.lang.Exception
meth public org.apache.sshd.common.io.IoWriteFuture sendIdentification(org.apache.sshd.common.session.Session,java.lang.String,java.util.List<java.lang.String>) throws java.lang.Exception
meth public org.apache.sshd.common.io.IoWriteFuture sendKexInitRequest(org.apache.sshd.common.session.Session,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void handleDebugMessage(org.apache.sshd.common.session.Session,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void handleIgnoreMessage(org.apache.sshd.common.session.Session,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception

CLSS public abstract interface org.apache.sshd.common.session.ReservedSessionMessagesManager
meth public abstract org.apache.sshd.common.session.ReservedSessionMessagesHandler getReservedSessionMessagesHandler()
meth public abstract void setReservedSessionMessagesHandler(org.apache.sshd.common.session.ReservedSessionMessagesHandler)

CLSS public abstract interface org.apache.sshd.common.session.Session
intf org.apache.sshd.common.FactoryManagerHolder
intf org.apache.sshd.common.auth.MutableUserHolder
intf org.apache.sshd.common.channel.ChannelListenerManager
intf org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolverManager
intf org.apache.sshd.common.forward.PortForwardingEventListenerManager
intf org.apache.sshd.common.forward.PortForwardingInformationProvider
intf org.apache.sshd.common.kex.KexFactoryManager
intf org.apache.sshd.common.session.ReservedSessionMessagesManager
intf org.apache.sshd.common.session.SessionContext
intf org.apache.sshd.common.session.SessionDisconnectHandlerManager
intf org.apache.sshd.common.session.SessionListenerManager
intf org.apache.sshd.common.session.UnknownChannelReferenceHandlerManager
meth public <%0 extends java.lang.Object> {%%0} resolveAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public abstract !varargs org.apache.sshd.common.io.IoWriteFuture sendIgnoreMessage(byte[]) throws java.io.IOException
meth public abstract <%0 extends org.apache.sshd.common.Service> {%%0} getService(java.lang.Class<{%%0}>)
meth public abstract java.time.Duration getAuthTimeout()
meth public abstract java.time.Duration getIdleTimeout()
meth public abstract java.time.Instant getAuthTimeoutStart()
meth public abstract java.time.Instant getIdleTimeoutStart()
meth public abstract java.time.Instant resetAuthTimeout()
meth public abstract java.time.Instant resetIdleTimeout()
meth public abstract org.apache.sshd.common.future.GlobalRequestFuture request(org.apache.sshd.common.util.buffer.Buffer,java.lang.String,org.apache.sshd.common.future.GlobalRequestFuture$ReplyHandler) throws java.io.IOException
meth public abstract org.apache.sshd.common.future.KeyExchangeFuture reExchangeKeys() throws java.io.IOException
meth public abstract org.apache.sshd.common.io.IoSession getIoSession()
meth public abstract org.apache.sshd.common.io.IoWriteFuture sendDebugMessage(boolean,java.lang.Object,java.lang.String) throws java.io.IOException
meth public abstract org.apache.sshd.common.io.IoWriteFuture writePacket(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public abstract org.apache.sshd.common.io.IoWriteFuture writePacket(org.apache.sshd.common.util.buffer.Buffer,long,java.util.concurrent.TimeUnit) throws java.io.IOException
meth public abstract org.apache.sshd.common.kex.KeyExchange getKex()
meth public abstract org.apache.sshd.common.session.helpers.TimeoutIndicator getTimeoutStatus()
meth public abstract org.apache.sshd.common.util.buffer.Buffer createBuffer(byte,int)
meth public abstract org.apache.sshd.common.util.buffer.Buffer prepareBuffer(byte,org.apache.sshd.common.util.buffer.Buffer)
meth public abstract org.apache.sshd.common.util.buffer.Buffer request(java.lang.String,org.apache.sshd.common.util.buffer.Buffer,long) throws java.io.IOException
meth public abstract void disconnect(int,java.lang.String) throws java.io.IOException
meth public abstract void exceptionCaught(java.lang.Throwable)
meth public abstract void setAuthenticated() throws java.io.IOException
meth public abstract void startService(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public java.net.SocketAddress getLocalAddress()
meth public java.net.SocketAddress getRemoteAddress()
meth public org.apache.sshd.common.io.IoWriteFuture writePacket(org.apache.sshd.common.util.buffer.Buffer,java.time.Duration) throws java.io.IOException
meth public org.apache.sshd.common.io.IoWriteFuture writePacket(org.apache.sshd.common.util.buffer.Buffer,long) throws java.io.IOException
meth public org.apache.sshd.common.util.buffer.Buffer createBuffer(byte)
meth public org.apache.sshd.common.util.buffer.Buffer request(java.lang.String,org.apache.sshd.common.util.buffer.Buffer,java.time.Duration) throws java.io.IOException
meth public org.apache.sshd.common.util.buffer.Buffer request(java.lang.String,org.apache.sshd.common.util.buffer.Buffer,long,java.util.concurrent.TimeUnit) throws java.io.IOException
meth public static <%0 extends java.lang.Object> {%%0} resolveAttribute(org.apache.sshd.common.session.Session,org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)

CLSS public abstract interface org.apache.sshd.common.session.SessionContext
fld public final static int MAX_VERSION_LINE_LENGTH = 256
fld public final static java.lang.String DEFAULT_SSH_VERSION_PREFIX = "SSH-2.0-"
fld public final static java.lang.String FALLBACK_SSH_VERSION_PREFIX = "SSH-1.99-"
intf org.apache.sshd.common.AttributeStore
intf org.apache.sshd.common.Closeable
intf org.apache.sshd.common.auth.UsernameHolder
intf org.apache.sshd.common.session.SessionHeartbeatController
intf org.apache.sshd.common.util.net.ConnectionEndpointsIndicator
meth public abstract boolean isAuthenticated()
meth public abstract boolean isServerSession()
meth public abstract byte[] getSessionId()
meth public abstract java.lang.String getClientVersion()
meth public abstract java.lang.String getNegotiatedKexParameter(org.apache.sshd.common.kex.KexProposalOption)
meth public abstract java.lang.String getServerVersion()
meth public abstract java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> getClientKexProposals()
meth public abstract java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> getKexNegotiationResult()
meth public abstract java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> getServerKexProposals()
meth public abstract org.apache.sshd.common.cipher.CipherInformation getCipherInformation(boolean)
meth public abstract org.apache.sshd.common.compression.CompressionInformation getCompressionInformation(boolean)
meth public abstract org.apache.sshd.common.kex.KexState getKexState()
meth public abstract org.apache.sshd.common.mac.MacInformation getMacInformation(boolean)
meth public static boolean isDataIntegrityTransport(org.apache.sshd.common.session.SessionContext)
meth public static boolean isSecureSessionTransport(org.apache.sshd.common.session.SessionContext)
meth public static boolean isValidSessionPayloadSize(long)
meth public static boolean isValidVersionPrefix(java.lang.String)
meth public static long validateSessionPayloadSize(long,java.lang.String)

CLSS public abstract interface org.apache.sshd.common.session.SessionContextHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.common.session.SessionContext getSessionContext()

CLSS public abstract interface org.apache.sshd.common.session.SessionDisconnectHandler
meth public boolean handleAuthCountDisconnectReason(org.apache.sshd.common.session.Session,org.apache.sshd.common.Service,java.lang.String,java.lang.String,java.lang.String,int,int) throws java.io.IOException
meth public boolean handleAuthParamsDisconnectReason(org.apache.sshd.common.session.Session,org.apache.sshd.common.Service,java.lang.String,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public boolean handleKexDisconnectReason(org.apache.sshd.common.session.Session,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,org.apache.sshd.common.kex.KexProposalOption) throws java.io.IOException
meth public boolean handleSessionsCountDisconnectReason(org.apache.sshd.common.session.Session,org.apache.sshd.common.Service,java.lang.String,int,int) throws java.io.IOException
meth public boolean handleTimeoutDisconnectReason(org.apache.sshd.common.session.Session,org.apache.sshd.common.session.helpers.TimeoutIndicator) throws java.io.IOException
meth public boolean handleUnsupportedServiceDisconnectReason(org.apache.sshd.common.session.Session,int,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.common.session.SessionDisconnectHandlerManager
meth public abstract org.apache.sshd.common.session.SessionDisconnectHandler getSessionDisconnectHandler()
meth public abstract void setSessionDisconnectHandler(org.apache.sshd.common.session.SessionDisconnectHandler)

CLSS public abstract interface org.apache.sshd.common.session.SessionHeartbeatController
innr public final static !enum HeartbeatType
intf org.apache.sshd.common.PropertyResolver
meth public java.time.Duration getSessionHeartbeatInterval()
meth public org.apache.sshd.common.session.SessionHeartbeatController$HeartbeatType getSessionHeartbeatType()
meth public void disableSessionHeartbeat()
meth public void setSessionHeartbeat(org.apache.sshd.common.session.SessionHeartbeatController$HeartbeatType,java.time.Duration)
meth public void setSessionHeartbeat(org.apache.sshd.common.session.SessionHeartbeatController$HeartbeatType,java.util.concurrent.TimeUnit,long)

CLSS public final static !enum org.apache.sshd.common.session.SessionHeartbeatController$HeartbeatType
 outer org.apache.sshd.common.session.SessionHeartbeatController
fld public final static java.util.Set<org.apache.sshd.common.session.SessionHeartbeatController$HeartbeatType> VALUES
fld public final static org.apache.sshd.common.session.SessionHeartbeatController$HeartbeatType IGNORE
fld public final static org.apache.sshd.common.session.SessionHeartbeatController$HeartbeatType NONE
fld public final static org.apache.sshd.common.session.SessionHeartbeatController$HeartbeatType RESERVED
meth public static org.apache.sshd.common.session.SessionHeartbeatController$HeartbeatType fromName(java.lang.String)
meth public static org.apache.sshd.common.session.SessionHeartbeatController$HeartbeatType valueOf(java.lang.String)
meth public static org.apache.sshd.common.session.SessionHeartbeatController$HeartbeatType[] values()
supr java.lang.Enum<org.apache.sshd.common.session.SessionHeartbeatController$HeartbeatType>

CLSS public abstract interface org.apache.sshd.common.session.SessionHolder<%0 extends org.apache.sshd.common.session.Session>
 anno 0 java.lang.FunctionalInterface()
intf org.apache.sshd.common.session.SessionContextHolder
meth public abstract {org.apache.sshd.common.session.SessionHolder%0} getSession()
meth public org.apache.sshd.common.session.SessionContext getSessionContext()

CLSS public abstract interface org.apache.sshd.common.session.SessionListener
innr public final static !enum Event
intf org.apache.sshd.common.util.SshdEventListener
meth public static <%0 extends org.apache.sshd.common.session.SessionListener> {%%0} validateListener({%%0})
meth public void sessionClosed(org.apache.sshd.common.session.Session)
meth public void sessionCreated(org.apache.sshd.common.session.Session)
meth public void sessionDisconnect(org.apache.sshd.common.session.Session,int,java.lang.String,java.lang.String,boolean)
meth public void sessionEstablished(org.apache.sshd.common.session.Session)
meth public void sessionEvent(org.apache.sshd.common.session.Session,org.apache.sshd.common.session.SessionListener$Event)
meth public void sessionException(org.apache.sshd.common.session.Session,java.lang.Throwable)
meth public void sessionNegotiationEnd(org.apache.sshd.common.session.Session,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.lang.Throwable)
meth public void sessionNegotiationOptionsCreated(org.apache.sshd.common.session.Session,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>)
meth public void sessionNegotiationStart(org.apache.sshd.common.session.Session,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>)
meth public void sessionPeerIdentificationLine(org.apache.sshd.common.session.Session,java.lang.String,java.util.List<java.lang.String>)
meth public void sessionPeerIdentificationReceived(org.apache.sshd.common.session.Session,java.lang.String,java.util.List<java.lang.String>)
meth public void sessionPeerIdentificationSend(org.apache.sshd.common.session.Session,java.lang.String,java.util.List<java.lang.String>)

CLSS public final static !enum org.apache.sshd.common.session.SessionListener$Event
 outer org.apache.sshd.common.session.SessionListener
fld public final static org.apache.sshd.common.session.SessionListener$Event Authenticated
fld public final static org.apache.sshd.common.session.SessionListener$Event KexCompleted
fld public final static org.apache.sshd.common.session.SessionListener$Event KeyEstablished
meth public static org.apache.sshd.common.session.SessionListener$Event valueOf(java.lang.String)
meth public static org.apache.sshd.common.session.SessionListener$Event[] values()
supr java.lang.Enum<org.apache.sshd.common.session.SessionListener$Event>

CLSS public abstract interface org.apache.sshd.common.session.SessionListenerManager
meth public abstract org.apache.sshd.common.session.SessionListener getSessionListenerProxy()
meth public abstract void addSessionListener(org.apache.sshd.common.session.SessionListener)
meth public abstract void removeSessionListener(org.apache.sshd.common.session.SessionListener)

CLSS public org.apache.sshd.common.session.SessionWorkBuffer
cons public init(org.apache.sshd.common.session.Session)
intf org.apache.sshd.common.session.SessionHolder<org.apache.sshd.common.session.Session>
meth public org.apache.sshd.common.session.Session getSession()
meth public org.apache.sshd.common.util.buffer.Buffer clear(boolean)
meth public void forceClear(boolean)
supr org.apache.sshd.common.util.buffer.ByteArrayBuffer
hfds session

CLSS public abstract interface org.apache.sshd.common.session.UnknownChannelReferenceHandler
meth public abstract org.apache.sshd.common.channel.Channel handleUnknownChannelCommand(org.apache.sshd.common.session.ConnectionService,byte,long,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.common.session.UnknownChannelReferenceHandlerManager
meth public abstract org.apache.sshd.common.session.UnknownChannelReferenceHandler getUnknownChannelReferenceHandler()
meth public abstract org.apache.sshd.common.session.UnknownChannelReferenceHandler resolveUnknownChannelReferenceHandler()
meth public abstract void setUnknownChannelReferenceHandler(org.apache.sshd.common.session.UnknownChannelReferenceHandler)

CLSS public abstract org.apache.sshd.common.session.helpers.AbstractConnectionService
cons protected init(org.apache.sshd.common.session.helpers.AbstractSession)
fld protected final java.util.Map<java.lang.Long,org.apache.sshd.common.channel.Channel> channels
fld protected final java.util.concurrent.atomic.AtomicLong heartbeatCount
fld protected final java.util.concurrent.atomic.AtomicLong nextChannelId
fld public final static java.lang.String DEFAULT_SESSION_IGNORE_HEARTBEAT_STRING = "ignore@sshd.apache.org"
fld public final static java.util.function.IntUnaryOperator RESPONSE_BUFFER_GROWTH_FACTOR
intf org.apache.sshd.common.session.ConnectionService
meth protected boolean sendHeartBeat()
meth protected java.util.concurrent.ScheduledFuture<?> startHeartBeat()
meth protected long getNextChannelId()
meth protected org.apache.sshd.agent.common.AgentForwardSupport createAgentForwardSupport(org.apache.sshd.common.session.Session)
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected org.apache.sshd.common.channel.Channel getChannel(byte,long,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.channel.Channel getChannel(byte,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.forward.Forwarder createForwardingFilter(org.apache.sshd.common.session.Session)
meth protected org.apache.sshd.common.io.IoWriteFuture globalRequest(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected org.apache.sshd.common.io.IoWriteFuture handleUnknownRequest(org.apache.sshd.common.util.buffer.Buffer,java.lang.String,boolean) throws java.io.IOException
meth protected org.apache.sshd.common.io.IoWriteFuture sendChannelOpenFailure(org.apache.sshd.common.util.buffer.Buffer,long,int,java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.apache.sshd.common.io.IoWriteFuture sendGlobalResponse(org.apache.sshd.common.util.buffer.Buffer,java.lang.String,org.apache.sshd.common.channel.RequestHandler$Result,boolean) throws java.io.IOException
meth protected org.apache.sshd.server.x11.X11ForwardSupport createX11ForwardSupport(org.apache.sshd.common.session.Session)
meth protected void channelOpen(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void futureDone(org.apache.sshd.common.future.HasException)
meth protected void preClose()
meth protected void requestFailure(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void requestSuccess(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void stopHeartBeat()
meth public boolean addPortForwardingEventListenerManager(org.apache.sshd.common.forward.PortForwardingEventListenerManager)
meth public boolean isAllowMoreSessions()
meth public boolean removePortForwardingEventListenerManager(org.apache.sshd.common.forward.PortForwardingEventListenerManager)
meth public java.lang.String toString()
meth public java.util.Collection<org.apache.sshd.common.channel.Channel> getChannels()
meth public java.util.Collection<org.apache.sshd.common.forward.PortForwardingEventListenerManager> getRegisteredManagers()
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public long registerChannel(org.apache.sshd.common.channel.Channel) throws java.io.IOException
meth public org.apache.sshd.agent.common.AgentForwardSupport getAgentForwardSupport()
meth public org.apache.sshd.common.forward.Forwarder getForwarder()
meth public org.apache.sshd.common.forward.PortForwardingEventListener getPortForwardingEventListenerProxy()
meth public org.apache.sshd.common.session.UnknownChannelReferenceHandler getUnknownChannelReferenceHandler()
meth public org.apache.sshd.common.session.UnknownChannelReferenceHandler resolveUnknownChannelReferenceHandler()
meth public org.apache.sshd.common.session.helpers.AbstractSession getSession()
meth public org.apache.sshd.server.x11.X11ForwardSupport getX11ForwardSupport()
meth public void addPortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
meth public void channelClose(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void channelData(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void channelEof(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void channelExtendedData(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void channelFailure(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void channelOpenConfirmation(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void channelOpenFailure(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void channelRequest(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void channelSuccess(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void channelWindowAdjust(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void process(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void removePortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
meth public void setAllowMoreSessions(boolean)
meth public void setUnknownChannelReferenceHandler(org.apache.sshd.common.session.UnknownChannelReferenceHandler)
meth public void start()
meth public void unregisterChannel(org.apache.sshd.common.channel.Channel)
supr org.apache.sshd.common.util.closeable.AbstractInnerCloseable
hfds agentForwardHolder,allowMoreSessions,forwarderHolder,heartBeat,listenerProxy,listeners,managersHolder,properties,sessionInstance,unknownChannelReferenceHandler,x11ForwardHolder

CLSS public abstract org.apache.sshd.common.session.helpers.AbstractConnectionServiceRequestHandler
cons protected init()
intf org.apache.sshd.common.session.ConnectionServiceRequestHandler
meth public org.apache.sshd.common.channel.RequestHandler$Result process(org.apache.sshd.common.session.ConnectionService,java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public abstract org.apache.sshd.common.session.helpers.AbstractSession
cons protected init(boolean,org.apache.sshd.common.FactoryManager,org.apache.sshd.common.io.IoSession)
fld protected boolean initialKexDone
fld protected boolean strictKex
fld protected byte[] inMacResult
fld protected byte[] sessionId
fld protected final java.lang.Object requestLock
fld protected final java.util.Collection<org.apache.sshd.common.channel.ChannelListener> channelListeners
fld protected final java.util.Collection<org.apache.sshd.common.forward.PortForwardingEventListener> tunnelListeners
fld protected final java.util.Collection<org.apache.sshd.common.session.SessionListener> sessionListeners
fld protected final java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> clientProposal
fld protected final java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> negotiationResult
fld protected final java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> serverProposal
fld protected final java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> unmodClientProposal
fld protected final java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> unmodNegotiationResult
fld protected final java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> unmodServerProposal
fld protected final java.util.concurrent.atomic.AtomicLong ignorePacketsCount
fld protected final java.util.concurrent.atomic.AtomicLong inBlocksCount
fld protected final java.util.concurrent.atomic.AtomicLong inBytesCount
fld protected final java.util.concurrent.atomic.AtomicLong inPacketsCount
fld protected final java.util.concurrent.atomic.AtomicLong maxRekeyBlocks
fld protected final java.util.concurrent.atomic.AtomicLong outBlocksCount
fld protected final java.util.concurrent.atomic.AtomicLong outBytesCount
fld protected final java.util.concurrent.atomic.AtomicLong outPacketsCount
fld protected final java.util.concurrent.atomic.AtomicReference<java.time.Instant> lastKeyTimeValue
fld protected final java.util.concurrent.atomic.AtomicReference<org.apache.sshd.common.future.DefaultKeyExchangeFuture> kexFutureHolder
fld protected final java.util.concurrent.atomic.AtomicReference<org.apache.sshd.common.kex.KexState> kexState
fld protected final java.util.concurrent.locks.ReentrantLock decodeLock
fld protected final java.util.concurrent.locks.ReentrantLock encodeLock
fld protected final org.apache.sshd.common.channel.ChannelListener channelListenerProxy
fld protected final org.apache.sshd.common.forward.PortForwardingEventListener tunnelListenerProxy
fld protected final org.apache.sshd.common.random.Random random
fld protected final org.apache.sshd.common.session.SessionListener sessionListenerProxy
fld protected final org.apache.sshd.common.session.SessionWorkBuffer decoderBuffer
fld protected final org.apache.sshd.common.session.helpers.CurrentService currentService
fld protected final org.apache.sshd.common.session.helpers.KeyExchangeMessageHandler kexHandler
fld protected int decoderLength
fld protected int decoderState
fld protected int ignorePacketDataLength
fld protected int ignorePacketsVariance
fld protected int inCipherSize
fld protected int inMacSize
fld protected int outCipherSize
fld protected int outMacSize
fld protected java.lang.Boolean firstKexPacketFollows
fld protected java.lang.String clientVersion
fld protected java.lang.String serverVersion
fld protected java.time.Duration maxRekeyInterval
fld protected long ignorePacketsFrequency
fld protected long initialKexInitSequenceNumber
fld protected long maxRekeyBytes
fld protected long maxRekyPackets
fld protected long seqi
fld protected long seqo
fld protected org.apache.sshd.common.SshException discarding
fld protected org.apache.sshd.common.cipher.Cipher inCipher
fld protected org.apache.sshd.common.cipher.Cipher outCipher
fld protected org.apache.sshd.common.compression.Compression inCompression
fld protected org.apache.sshd.common.compression.Compression outCompression
fld protected org.apache.sshd.common.future.DefaultKeyExchangeFuture kexInitializedFuture
fld protected org.apache.sshd.common.kex.KeyExchange kex
fld protected org.apache.sshd.common.mac.Mac inMac
fld protected org.apache.sshd.common.mac.Mac outMac
fld protected org.apache.sshd.common.session.SessionWorkBuffer uncompressBuffer
fld protected org.apache.sshd.common.session.helpers.AbstractSession$MessageCodingSettings inSettings
fld protected org.apache.sshd.common.session.helpers.AbstractSession$MessageCodingSettings outSettings
fld public final static java.lang.String SESSION = "org.apache.sshd.session"
innr protected static MessageCodingSettings
meth protected <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} validateTargetBuffer(int,{%%0})
meth protected abstract !varargs void setKexSeed(byte[])
meth protected abstract boolean readIdentification(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected abstract void checkKeys() throws java.io.IOException
meth protected abstract void receiveKexInit(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,byte[]) throws java.io.IOException
meth protected abstract void start() throws java.lang.Exception
meth protected boolean doInvokeUnimplementedMessageHandler(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected boolean handleFirstKexPacketFollows(int,org.apache.sshd.common.util.buffer.Buffer,boolean)
meth protected boolean handleServiceRequest(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected boolean isRekeyBlocksCountExceeded()
meth protected boolean isRekeyDataSizeExceeded()
meth protected boolean isRekeyPacketCountsExceeded()
meth protected boolean isRekeyRequired()
meth protected boolean isRekeyTimeIntervalExceeded()
meth protected boolean removeValue(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,org.apache.sshd.common.kex.KexProposalOption,java.lang.String)
meth protected boolean validateServiceKexState(org.apache.sshd.common.kex.KexState)
meth protected byte[] getClientKexData()
meth protected byte[] getServerKexData()
meth protected byte[] receiveKexInit(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected byte[] receiveKexInit(org.apache.sshd.common.util.buffer.Buffer,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>) throws java.lang.Exception
meth protected byte[] sendKexInit() throws java.lang.Exception
meth protected byte[] sendKexInit(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>) throws java.lang.Exception
meth protected int resolveIgnoreBufferDataLength()
meth protected java.lang.String resolveSessionKexProposal(java.lang.String) throws java.io.IOException
meth protected java.util.List<org.apache.sshd.common.Service> getServices()
meth protected java.util.Map$Entry<java.lang.String,java.lang.String> comparePreferredKexProposalOption(org.apache.sshd.common.kex.KexProposalOption)
meth protected java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> doStrictKexProposal(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>)
meth protected java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> negotiate() throws java.lang.Exception
meth protected java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> setNegotiationResult(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>)
meth protected long determineRekeyBlockLimit(int,int)
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected org.apache.sshd.common.future.KeyExchangeFuture checkRekey() throws java.lang.Exception
meth protected org.apache.sshd.common.future.KeyExchangeFuture requestNewKeysExchange() throws java.lang.Exception
meth protected org.apache.sshd.common.io.IoWriteFuture doWritePacket(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.io.IoWriteFuture notImplemented(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected org.apache.sshd.common.io.IoWriteFuture sendNewKeys() throws java.lang.Exception
meth protected org.apache.sshd.common.session.helpers.CurrentService initializeCurrentService()
meth protected org.apache.sshd.common.session.helpers.KeyExchangeMessageHandler initializeKeyExchangeMessageHandler()
meth protected org.apache.sshd.common.util.buffer.Buffer encode(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.util.buffer.Buffer preProcessEncodeBuffer(int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.util.buffer.Buffer resolveOutputPacket(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected void aeadOutgoingBuffer(org.apache.sshd.common.util.buffer.Buffer,int,int) throws java.lang.Exception
meth protected void appendOutgoingMac(org.apache.sshd.common.util.buffer.Buffer,int,int) throws java.lang.Exception
meth protected void decode() throws java.lang.Exception
meth protected void doHandleMessage(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void doKexNegotiation() throws java.lang.Exception
meth protected void encryptOutgoingBuffer(org.apache.sshd.common.util.buffer.Buffer,int,int) throws java.lang.Exception
meth protected void failStrictKex(int) throws org.apache.sshd.common.SshException
meth protected void handleKexExtension(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleKexInit(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleKexMessage(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleMessage(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleNewCompression(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleNewKeys(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleServiceAccept(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleServiceAccept(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleServiceRequest(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void performKexNegotiation() throws java.lang.Exception
meth protected void preClose()
meth protected void prepareNewKeys() throws java.lang.Exception
meth protected void refreshConfiguration()
meth protected void requestFailure(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void requestSuccess(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void setClientKexData(byte[])
meth protected void setInputEncoding() throws java.lang.Exception
meth protected void setOutputEncoding() throws java.lang.Exception
meth protected void setServerKexData(byte[])
meth protected void validateIncomingMac(byte[],int,int) throws java.lang.Exception
meth protected void validateKexState(int,org.apache.sshd.common.kex.KexState)
meth public <%0 extends org.apache.sshd.common.Service> {%%0} getService(java.lang.Class<{%%0}>)
meth public byte[] getSessionId()
meth public java.lang.String getClientVersion()
meth public java.lang.String getNegotiatedKexParameter(org.apache.sshd.common.kex.KexProposalOption)
meth public java.lang.String getServerVersion()
meth public java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> getClientKexProposals()
meth public java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> getKexNegotiationResult()
meth public java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> getServerKexProposals()
meth public org.apache.sshd.common.channel.ChannelListener getChannelListenerProxy()
meth public org.apache.sshd.common.cipher.CipherInformation getCipherInformation(boolean)
meth public org.apache.sshd.common.compression.CompressionInformation getCompressionInformation(boolean)
meth public org.apache.sshd.common.forward.PortForwardingEventListener getPortForwardingEventListenerProxy()
meth public org.apache.sshd.common.future.GlobalRequestFuture request(org.apache.sshd.common.util.buffer.Buffer,java.lang.String,org.apache.sshd.common.future.GlobalRequestFuture$ReplyHandler) throws java.io.IOException
meth public org.apache.sshd.common.future.KeyExchangeFuture reExchangeKeys() throws java.io.IOException
meth public org.apache.sshd.common.io.IoWriteFuture writePacket(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public org.apache.sshd.common.io.IoWriteFuture writePacket(org.apache.sshd.common.util.buffer.Buffer,long,java.util.concurrent.TimeUnit) throws java.io.IOException
meth public org.apache.sshd.common.kex.KexState getKexState()
meth public org.apache.sshd.common.kex.KeyExchange getKex()
meth public org.apache.sshd.common.mac.MacInformation getMacInformation(boolean)
meth public org.apache.sshd.common.session.SessionListener getSessionListenerProxy()
meth public org.apache.sshd.common.util.buffer.Buffer createBuffer(byte,int)
meth public org.apache.sshd.common.util.buffer.Buffer prepareBuffer(byte,org.apache.sshd.common.util.buffer.Buffer)
meth public org.apache.sshd.common.util.buffer.Buffer request(java.lang.String,org.apache.sshd.common.util.buffer.Buffer,long) throws java.io.IOException
meth public static int calculatePadLength(int,int,boolean)
meth public static org.apache.sshd.common.session.helpers.AbstractSession getSession(org.apache.sshd.common.io.IoSession)
meth public static org.apache.sshd.common.session.helpers.AbstractSession getSession(org.apache.sshd.common.io.IoSession,boolean)
meth public static void attachSession(org.apache.sshd.common.io.IoSession,org.apache.sshd.common.session.helpers.AbstractSession)
meth public void addChannelListener(org.apache.sshd.common.channel.ChannelListener)
meth public void addPortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
meth public void addSessionListener(org.apache.sshd.common.session.SessionListener)
meth public void messageReceived(org.apache.sshd.common.util.Readable) throws java.lang.Exception
meth public void removeChannelListener(org.apache.sshd.common.channel.ChannelListener)
meth public void removePortForwardingEventListener(org.apache.sshd.common.forward.PortForwardingEventListener)
meth public void removeSessionListener(org.apache.sshd.common.session.SessionListener)
supr org.apache.sshd.common.session.helpers.SessionHelper
hfds clientKexData,globalSequenceNumbers,pendingGlobalRequests,serverKexData
hcls KexStart

CLSS protected static org.apache.sshd.common.session.helpers.AbstractSession$MessageCodingSettings
 outer org.apache.sshd.common.session.helpers.AbstractSession
cons public init(org.apache.sshd.common.cipher.Cipher,org.apache.sshd.common.mac.Mac,org.apache.sshd.common.compression.Compression,org.apache.sshd.common.cipher.Cipher$Mode,byte[],byte[])
meth public org.apache.sshd.common.cipher.Cipher getCipher(long) throws java.lang.Exception
meth public org.apache.sshd.common.compression.Compression getCompression()
meth public org.apache.sshd.common.mac.Mac getMac()
supr java.lang.Object
hfds cipher,compression,iv,key,mac,mode

CLSS public abstract org.apache.sshd.common.session.helpers.AbstractSessionFactory<%0 extends org.apache.sshd.common.FactoryManager, %1 extends org.apache.sshd.common.session.helpers.AbstractSession>
cons protected init({org.apache.sshd.common.session.helpers.AbstractSessionFactory%0})
meth protected abstract {org.apache.sshd.common.session.helpers.AbstractSessionFactory%1} doCreateSession(org.apache.sshd.common.io.IoSession) throws java.lang.Exception
meth protected {org.apache.sshd.common.session.helpers.AbstractSessionFactory%1} createSession(org.apache.sshd.common.io.IoSession) throws java.lang.Exception
meth protected {org.apache.sshd.common.session.helpers.AbstractSessionFactory%1} setupSession({org.apache.sshd.common.session.helpers.AbstractSessionFactory%1}) throws java.lang.Exception
meth public {org.apache.sshd.common.session.helpers.AbstractSessionFactory%0} getFactoryManager()
supr org.apache.sshd.common.session.helpers.AbstractSessionIoHandler
hfds manager

CLSS public abstract org.apache.sshd.common.session.helpers.AbstractSessionIoHandler
cons protected init()
intf org.apache.sshd.common.io.IoHandler
meth protected abstract org.apache.sshd.common.session.helpers.AbstractSession createSession(org.apache.sshd.common.io.IoSession) throws java.lang.Exception
meth public void exceptionCaught(org.apache.sshd.common.io.IoSession,java.lang.Throwable) throws java.lang.Exception
meth public void messageReceived(org.apache.sshd.common.io.IoSession,org.apache.sshd.common.util.Readable) throws java.lang.Exception
meth public void sessionClosed(org.apache.sshd.common.io.IoSession) throws java.lang.Exception
meth public void sessionCreated(org.apache.sshd.common.io.IoSession) throws java.lang.Exception
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public org.apache.sshd.common.session.helpers.CurrentService
cons protected init(org.apache.sshd.common.session.Session)
fld protected final org.apache.sshd.common.session.Session session
meth public boolean process(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public java.lang.String getName()
meth public org.apache.sshd.common.Service getService()
meth public void set(org.apache.sshd.common.Service,java.lang.String,boolean)
meth public void start()
supr java.lang.Object
hfds currentName,currentService

CLSS public org.apache.sshd.common.session.helpers.DefaultUnknownChannelReferenceHandler
cons public init()
fld public final static org.apache.sshd.common.session.helpers.DefaultUnknownChannelReferenceHandler INSTANCE
intf org.apache.sshd.common.session.UnknownChannelReferenceHandler
meth protected org.apache.sshd.common.io.IoWriteFuture sendFailureResponse(org.apache.sshd.common.session.ConnectionService,byte,long) throws java.io.IOException
meth public org.apache.sshd.common.channel.Channel handleUnknownChannelCommand(org.apache.sshd.common.session.ConnectionService,byte,long,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public org.apache.sshd.common.session.helpers.KeyExchangeMessageHandler
cons public init(org.apache.sshd.common.session.helpers.AbstractSession,org.slf4j.Logger)
fld protected final java.util.Queue<org.apache.sshd.common.session.helpers.PendingWriteFuture> pendingPackets
fld protected final java.util.concurrent.atomic.AtomicBoolean kexFlushed
fld protected final java.util.concurrent.atomic.AtomicBoolean shutDown
fld protected final java.util.concurrent.atomic.AtomicReference<org.apache.sshd.common.future.DefaultKeyExchangeFuture> kexFlushedFuture
fld protected final java.util.concurrent.locks.ReentrantReadWriteLock lock
fld protected final org.apache.sshd.common.session.helpers.AbstractSession session
fld protected final org.slf4j.Logger log
fld protected static java.util.concurrent.ExecutorService flushRunner
meth protected boolean isBlockAllowed(int)
meth protected org.apache.sshd.common.io.IoWriteFuture writeOrEnqueue(int,org.apache.sshd.common.util.buffer.Buffer,long,java.util.concurrent.TimeUnit) throws java.io.IOException
meth protected org.apache.sshd.common.session.helpers.PendingWriteFuture enqueuePendingPacket(int,org.apache.sshd.common.util.buffer.Buffer)
meth protected void flushQueue(org.apache.sshd.common.future.DefaultKeyExchangeFuture)
meth public <%0 extends java.lang.Object> {%%0} updateState(java.util.function.Supplier<{%%0}>)
meth public java.util.AbstractMap$SimpleImmutableEntry<java.lang.Integer,org.apache.sshd.common.future.DefaultKeyExchangeFuture> terminateKeyExchange()
meth public org.apache.sshd.common.future.DefaultKeyExchangeFuture initNewKeyExchange()
meth public org.apache.sshd.common.io.IoWriteFuture writePacket(org.apache.sshd.common.util.buffer.Buffer,long,java.util.concurrent.TimeUnit) throws java.io.IOException
meth public void shutdown()
meth public void updateState(java.lang.Runnable)
supr java.lang.Object

CLSS public org.apache.sshd.common.session.helpers.MissingAttachedSessionException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.IllegalStateException
hfds serialVersionUID

CLSS public org.apache.sshd.common.session.helpers.MultipleAttachedSessionException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.IllegalStateException
hfds serialVersionUID

CLSS public org.apache.sshd.common.session.helpers.PacketBuffer
cons public init()
cons public init(byte[],boolean)
supr org.apache.sshd.common.util.buffer.ByteArrayBuffer

CLSS public org.apache.sshd.common.session.helpers.PendingWriteFuture
cons public init(java.lang.Object,org.apache.sshd.common.util.buffer.Buffer)
intf org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.io.IoWriteFuture>
meth public org.apache.sshd.common.util.buffer.Buffer getBuffer()
meth public void operationComplete(org.apache.sshd.common.io.IoWriteFuture)
meth public void setException(java.lang.Throwable)
meth public void setWritten()
supr org.apache.sshd.common.io.AbstractIoWriteFuture
hfds buffer

CLSS public org.apache.sshd.common.session.helpers.ReservedSessionMessagesHandlerAdapter
cons public init()
fld public final static org.apache.sshd.common.session.helpers.ReservedSessionMessagesHandlerAdapter DEFAULT
intf org.apache.sshd.common.session.ReservedSessionMessagesHandler
meth public boolean handleUnimplementedMessage(org.apache.sshd.common.session.Session,int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public org.apache.sshd.common.io.IoWriteFuture sendIdentification(org.apache.sshd.common.session.Session,java.lang.String,java.util.List<java.lang.String>) throws java.lang.Exception
meth public void handleDebugMessage(org.apache.sshd.common.session.Session,boolean,java.lang.String,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void handleDebugMessage(org.apache.sshd.common.session.Session,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void handleIgnoreMessage(org.apache.sshd.common.session.Session,byte[],org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void handleIgnoreMessage(org.apache.sshd.common.session.Session,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public abstract org.apache.sshd.common.session.helpers.SessionHelper
cons protected init(boolean,org.apache.sshd.common.FactoryManager,org.apache.sshd.common.io.IoSession)
fld protected java.time.Instant authStart
fld protected java.time.Instant idleStart
fld protected java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> initialKexProposal
intf org.apache.sshd.common.session.Session
meth protected abstract java.lang.String resolveAvailableSignaturesProposal(org.apache.sshd.common.FactoryManager) throws java.io.IOException,java.security.GeneralSecurityException
meth protected abstract org.apache.sshd.common.session.ConnectionService getConnectionService()
meth protected boolean doInvokeUnimplementedMessageHandler(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected byte[] resizeKey(byte[],int,org.apache.sshd.common.digest.Digest,byte[],byte[]) throws java.lang.Exception
meth protected java.lang.String resolveAvailableSignaturesProposal() throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.lang.String resolveIdentificationString(java.lang.String)
meth protected java.lang.String resolveSessionKexProposal(java.lang.String) throws java.io.IOException
meth protected java.net.SocketAddress resolvePeerAddress(java.net.SocketAddress)
meth protected java.util.List<java.lang.String> doReadIdentification(org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.lang.Exception
meth protected java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> createProposal(java.lang.String) throws java.io.IOException
meth protected java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> getKexProposal() throws java.lang.Exception
meth protected java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String> mergeProposals(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>)
meth protected long calculateNextIgnorePacketCount(org.apache.sshd.common.random.Random,long,int)
meth protected org.apache.sshd.common.forward.Forwarder getForwarder()
meth protected org.apache.sshd.common.io.IoWriteFuture sendIdentification(java.lang.String,java.util.List<java.lang.String>) throws java.lang.Exception
meth protected org.apache.sshd.common.io.IoWriteFuture sendNotImplemented(long) throws java.io.IOException
meth protected org.apache.sshd.common.session.ReservedSessionMessagesHandler resolveReservedSessionMessagesHandler()
meth protected org.apache.sshd.common.session.helpers.TimeoutIndicator checkAuthenticationTimeout(java.time.Instant,java.time.Duration)
meth protected org.apache.sshd.common.session.helpers.TimeoutIndicator checkForTimeouts() throws java.io.IOException
meth protected org.apache.sshd.common.session.helpers.TimeoutIndicator checkIdleTimeout(java.time.Instant,java.time.Duration)
meth protected org.apache.sshd.common.util.buffer.Buffer preProcessEncodeBuffer(int,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected void doInvokeDebugMessageHandler(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void doInvokeIgnoreMessageHandler(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleDebug(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleDisconnect(int,java.lang.String,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleDisconnect(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleIgnore(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleUnimplemented(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void invokeSessionSignaller(org.apache.sshd.common.util.io.functors.Invoker<org.apache.sshd.common.session.SessionListener,java.lang.Void>) throws java.lang.Throwable
meth protected void signalDisconnect(int,java.lang.String,java.lang.String,boolean)
meth protected void signalDisconnect(org.apache.sshd.common.session.SessionListener,int,java.lang.String,java.lang.String,boolean)
meth protected void signalExceptionCaught(java.lang.Throwable)
meth protected void signalExceptionCaught(org.apache.sshd.common.session.SessionListener,java.lang.Throwable)
meth protected void signalNegotiationEnd(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.lang.Throwable)
meth protected void signalNegotiationEnd(org.apache.sshd.common.session.SessionListener,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.lang.Throwable)
meth protected void signalNegotiationOptionsCreated(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>)
meth protected void signalNegotiationOptionsCreated(org.apache.sshd.common.session.SessionListener,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>)
meth protected void signalNegotiationStart(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>)
meth protected void signalNegotiationStart(org.apache.sshd.common.session.SessionListener,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>)
meth protected void signalPeerIdentificationReceived(java.lang.String,java.util.List<java.lang.String>) throws java.lang.Exception
meth protected void signalPeerIdentificationReceived(org.apache.sshd.common.session.SessionListener,java.lang.String,java.util.List<java.lang.String>)
meth protected void signalReadPeerIdentificationLine(java.lang.String,java.util.List<java.lang.String>) throws java.lang.Exception
meth protected void signalReadPeerIdentificationLine(org.apache.sshd.common.session.SessionListener,java.lang.String,java.util.List<java.lang.String>)
meth protected void signalSendIdentification(java.lang.String,java.util.List<java.lang.String>) throws java.lang.Exception
meth protected void signalSendIdentification(org.apache.sshd.common.session.SessionListener,java.lang.String,java.util.List<java.lang.String>)
meth protected void signalSessionClosed()
meth protected void signalSessionClosed(org.apache.sshd.common.session.SessionListener)
meth protected void signalSessionCreated(org.apache.sshd.common.io.IoSession) throws java.lang.Exception
meth protected void signalSessionCreated(org.apache.sshd.common.session.SessionListener)
meth protected void signalSessionEstablished(org.apache.sshd.common.io.IoSession) throws java.lang.Exception
meth protected void signalSessionEstablished(org.apache.sshd.common.session.SessionListener)
meth protected void signalSessionEvent(org.apache.sshd.common.session.SessionListener$Event) throws java.lang.Exception
meth protected void signalSessionEvent(org.apache.sshd.common.session.SessionListener,org.apache.sshd.common.session.SessionListener$Event) throws java.io.IOException
meth public !varargs org.apache.sshd.common.io.IoWriteFuture sendIgnoreMessage(byte[]) throws java.io.IOException
meth public <%0 extends java.lang.Object> {%%0} computeAttributeIfAbsent(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,java.util.function.Function<? super org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,? extends {%%0}>)
meth public <%0 extends java.lang.Object> {%%0} getAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} removeAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} setAttribute(org.apache.sshd.common.AttributeRepository$AttributeKey<{%%0}>,{%%0})
meth public boolean isAuthenticated()
meth public boolean isLocalPortForwardingStartedForPort(int)
meth public boolean isRemotePortForwardingStartedForPort(int)
meth public boolean isServerSession()
meth public int getAttributesCount()
meth public java.lang.String getUsername()
meth public java.lang.String toString()
meth public java.time.Duration getAuthTimeout()
meth public java.time.Duration getIdleTimeout()
meth public java.time.Instant getAuthTimeoutStart()
meth public java.time.Instant getIdleTimeoutStart()
meth public java.time.Instant resetAuthTimeout()
meth public java.time.Instant resetIdleTimeout()
meth public java.util.Collection<org.apache.sshd.common.AttributeRepository$AttributeKey<?>> attributeKeys()
meth public java.util.List<java.util.Map$Entry<java.lang.Integer,org.apache.sshd.common.util.net.SshdSocketAddress>> getRemoteForwardsBindings()
meth public java.util.List<java.util.Map$Entry<org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.util.net.SshdSocketAddress>> getLocalForwardsBindings()
meth public java.util.List<org.apache.sshd.common.util.net.SshdSocketAddress> getBoundLocalPortForwards(int)
meth public java.util.List<org.apache.sshd.common.util.net.SshdSocketAddress> getStartedLocalPortForwards()
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public java.util.NavigableSet<java.lang.Integer> getStartedRemotePortForwards()
meth public org.apache.sshd.common.FactoryManager getFactoryManager()
meth public org.apache.sshd.common.PropertyResolver getParentPropertyResolver()
meth public org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver getChannelStreamWriterResolver()
meth public org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver resolveChannelStreamWriterResolver()
meth public org.apache.sshd.common.io.IoSession getIoSession()
meth public org.apache.sshd.common.io.IoWriteFuture sendDebugMessage(boolean,java.lang.Object,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.common.session.ReservedSessionMessagesHandler getReservedSessionMessagesHandler()
meth public org.apache.sshd.common.session.SessionDisconnectHandler getSessionDisconnectHandler()
meth public org.apache.sshd.common.session.UnknownChannelReferenceHandler getUnknownChannelReferenceHandler()
meth public org.apache.sshd.common.session.UnknownChannelReferenceHandler resolveUnknownChannelReferenceHandler()
meth public org.apache.sshd.common.session.helpers.TimeoutIndicator getTimeoutStatus()
meth public org.apache.sshd.common.util.net.SshdSocketAddress getBoundRemotePortForward(int)
meth public void clearAttributes()
meth public void disconnect(int,java.lang.String) throws java.io.IOException
meth public void exceptionCaught(java.lang.Throwable)
meth public void setAuthenticated() throws java.io.IOException
meth public void setChannelStreamWriterResolver(org.apache.sshd.common.channel.throttle.ChannelStreamWriterResolver)
meth public void setReservedSessionMessagesHandler(org.apache.sshd.common.session.ReservedSessionMessagesHandler)
meth public void setSessionDisconnectHandler(org.apache.sshd.common.session.SessionDisconnectHandler)
meth public void setUnknownChannelReferenceHandler(org.apache.sshd.common.session.UnknownChannelReferenceHandler)
meth public void setUsername(java.lang.String)
supr org.apache.sshd.common.kex.AbstractKexFactoryManager
hfds attributes,authed,channelStreamPacketWriterResolver,ioSession,properties,reservedSessionMessagesHandler,serverSession,sessionDisconnectHandler,timeoutStatus,unknownChannelReferenceHandler,username

CLSS public org.apache.sshd.common.session.helpers.SessionTimeoutListener
cons public init()
fld protected final java.util.Set<org.apache.sshd.common.session.helpers.SessionHelper> sessions
intf java.lang.Runnable
intf org.apache.sshd.common.session.SessionListener
meth public void run()
meth public void sessionClosed(org.apache.sshd.common.session.Session)
meth public void sessionCreated(org.apache.sshd.common.session.Session)
meth public void sessionException(org.apache.sshd.common.session.Session,java.lang.Throwable)
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public org.apache.sshd.common.session.helpers.TimeoutIndicator
cons public init(org.apache.sshd.common.session.helpers.TimeoutIndicator$TimeoutStatus,java.time.Duration,java.time.Duration)
fld public final static org.apache.sshd.common.session.helpers.TimeoutIndicator NONE
innr public final static !enum TimeoutStatus
meth public java.lang.String toString()
meth public java.time.Duration getExpiredValue()
meth public java.time.Duration getThresholdValue()
meth public org.apache.sshd.common.session.helpers.TimeoutIndicator$TimeoutStatus getStatus()
meth public static java.lang.String toDisplayDurationValue(java.time.Duration)
supr java.lang.Object
hfds expiredValue,status,thresholdValue

CLSS public final static !enum org.apache.sshd.common.session.helpers.TimeoutIndicator$TimeoutStatus
 outer org.apache.sshd.common.session.helpers.TimeoutIndicator
fld public final static org.apache.sshd.common.session.helpers.TimeoutIndicator$TimeoutStatus AuthTimeout
fld public final static org.apache.sshd.common.session.helpers.TimeoutIndicator$TimeoutStatus IdleTimeout
fld public final static org.apache.sshd.common.session.helpers.TimeoutIndicator$TimeoutStatus NoTimeout
meth public static org.apache.sshd.common.session.helpers.TimeoutIndicator$TimeoutStatus valueOf(java.lang.String)
meth public static org.apache.sshd.common.session.helpers.TimeoutIndicator$TimeoutStatus[] values()
supr java.lang.Enum<org.apache.sshd.common.session.helpers.TimeoutIndicator$TimeoutStatus>

CLSS public abstract org.apache.sshd.common.signature.AbstractSecurityKeySignature
cons protected init(java.lang.String)
intf org.apache.sshd.common.signature.Signature
meth protected abstract java.lang.String getSignatureKeyType()
meth protected abstract org.apache.sshd.common.signature.Signature getDelegateSignature()
meth public boolean verify(org.apache.sshd.common.session.SessionContext,byte[]) throws java.lang.Exception
meth public byte[] sign(org.apache.sshd.common.session.SessionContext)
meth public void initSigner(org.apache.sshd.common.session.SessionContext,java.security.PrivateKey)
meth public void initVerifier(org.apache.sshd.common.session.SessionContext,java.security.PublicKey) throws java.security.GeneralSecurityException
meth public void update(org.apache.sshd.common.session.SessionContext,byte[],int,int)
supr java.lang.Object
hfds FLAG_USER_PRESENCE,FLAG_VERIFIED,challengeDigest,keyType,publicKey

CLSS public abstract org.apache.sshd.common.signature.AbstractSignature
cons protected init(java.lang.String,java.lang.String)
intf org.apache.sshd.common.signature.Signature
meth protected boolean doVerify(byte[]) throws java.security.SignatureException
meth protected java.security.Signature doInitSignature(org.apache.sshd.common.session.SessionContext,java.lang.String,java.security.Key,boolean) throws java.security.GeneralSecurityException
meth protected java.security.Signature getSignature()
meth protected java.util.Map$Entry<java.lang.String,byte[]> extractEncodedSignature(byte[],java.util.Collection<java.lang.String>)
meth protected java.util.Map$Entry<java.lang.String,byte[]> extractEncodedSignature(byte[],java.util.function.Predicate<? super java.lang.String>)
meth public byte[] sign(org.apache.sshd.common.session.SessionContext) throws java.lang.Exception
meth public final java.lang.String getAlgorithm()
meth public java.lang.String getSshAlgorithmName(java.lang.String)
meth public java.lang.String toString()
meth public void initSigner(org.apache.sshd.common.session.SessionContext,java.security.PrivateKey) throws java.lang.Exception
meth public void initVerifier(org.apache.sshd.common.session.SessionContext,java.security.PublicKey) throws java.lang.Exception
meth public void update(org.apache.sshd.common.session.SessionContext,byte[],int,int) throws java.lang.Exception
supr java.lang.Object
hfds algorithm,signatureInstance,sshAlgorithmName

CLSS public abstract !enum org.apache.sshd.common.signature.BuiltinSignatures
fld public final static java.util.Set<org.apache.sshd.common.signature.BuiltinSignatures> VALUES
fld public final static org.apache.sshd.common.signature.BuiltinSignatures dsa
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.signature.BuiltinSignatures dsa_cert
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.signature.BuiltinSignatures ed25519
fld public final static org.apache.sshd.common.signature.BuiltinSignatures ed25519_cert
fld public final static org.apache.sshd.common.signature.BuiltinSignatures nistp256
fld public final static org.apache.sshd.common.signature.BuiltinSignatures nistp256_cert
fld public final static org.apache.sshd.common.signature.BuiltinSignatures nistp384
fld public final static org.apache.sshd.common.signature.BuiltinSignatures nistp384_cert
fld public final static org.apache.sshd.common.signature.BuiltinSignatures nistp521
fld public final static org.apache.sshd.common.signature.BuiltinSignatures nistp521_cert
fld public final static org.apache.sshd.common.signature.BuiltinSignatures rsa
fld public final static org.apache.sshd.common.signature.BuiltinSignatures rsaSHA256
fld public final static org.apache.sshd.common.signature.BuiltinSignatures rsaSHA256_cert
fld public final static org.apache.sshd.common.signature.BuiltinSignatures rsaSHA512
fld public final static org.apache.sshd.common.signature.BuiltinSignatures rsaSHA512_cert
fld public final static org.apache.sshd.common.signature.BuiltinSignatures rsa_cert
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.signature.BuiltinSignatures sk_ecdsa_sha2_nistp256
fld public final static org.apache.sshd.common.signature.BuiltinSignatures sk_ssh_ed25519
innr public final static ParseResult
intf org.apache.sshd.common.signature.SignatureFactory
meth public !varargs static org.apache.sshd.common.signature.BuiltinSignatures$ParseResult parseSignatureList(java.lang.String[])
meth public boolean isSupported()
meth public final java.lang.String getName()
meth public final java.lang.String toString()
meth public static java.util.NavigableSet<org.apache.sshd.common.signature.SignatureFactory> getRegisteredExtensions()
meth public static org.apache.sshd.common.signature.BuiltinSignatures fromFactory(org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>)
meth public static org.apache.sshd.common.signature.BuiltinSignatures fromFactoryName(java.lang.String)
meth public static org.apache.sshd.common.signature.BuiltinSignatures fromString(java.lang.String)
meth public static org.apache.sshd.common.signature.BuiltinSignatures getFactoryByCurveSize(java.security.spec.ECParameterSpec)
meth public static org.apache.sshd.common.signature.BuiltinSignatures valueOf(java.lang.String)
meth public static org.apache.sshd.common.signature.BuiltinSignatures$ParseResult parseSignatureList(java.lang.String)
meth public static org.apache.sshd.common.signature.BuiltinSignatures$ParseResult parseSignatureList(java.util.Collection<java.lang.String>)
meth public static org.apache.sshd.common.signature.BuiltinSignatures[] values()
meth public static org.apache.sshd.common.signature.Signature getSignerByCurveSize(java.security.spec.ECParameterSpec)
meth public static org.apache.sshd.common.signature.SignatureFactory resolveFactory(java.lang.String)
meth public static org.apache.sshd.common.signature.SignatureFactory unregisterExtension(java.lang.String)
meth public static void registerExtension(org.apache.sshd.common.signature.SignatureFactory)
supr java.lang.Enum<org.apache.sshd.common.signature.BuiltinSignatures>
hfds EXTENSIONS,factoryName

CLSS public final static org.apache.sshd.common.signature.BuiltinSignatures$ParseResult
 outer org.apache.sshd.common.signature.BuiltinSignatures
cons public init(java.util.List<org.apache.sshd.common.signature.SignatureFactory>,java.util.List<java.lang.String>)
fld public final static org.apache.sshd.common.signature.BuiltinSignatures$ParseResult EMPTY
supr org.apache.sshd.common.config.NamedFactoriesListParseResult<org.apache.sshd.common.signature.Signature,org.apache.sshd.common.signature.SignatureFactory>

CLSS public abstract interface org.apache.sshd.common.signature.Signature
intf org.apache.sshd.common.AlgorithmNameProvider
meth public abstract boolean verify(org.apache.sshd.common.session.SessionContext,byte[]) throws java.lang.Exception
meth public abstract byte[] sign(org.apache.sshd.common.session.SessionContext) throws java.lang.Exception
meth public abstract void initSigner(org.apache.sshd.common.session.SessionContext,java.security.PrivateKey) throws java.lang.Exception
meth public abstract void initVerifier(org.apache.sshd.common.session.SessionContext,java.security.PublicKey) throws java.lang.Exception
meth public abstract void update(org.apache.sshd.common.session.SessionContext,byte[],int,int) throws java.lang.Exception
meth public java.lang.String getSshAlgorithmName(java.lang.String)
meth public void update(org.apache.sshd.common.session.SessionContext,byte[]) throws java.lang.Exception

CLSS public org.apache.sshd.common.signature.SignatureDSA
cons protected init(java.lang.String)
cons public init()
fld public final static int DSA_SIGNATURE_LENGTH = 40
fld public final static int MAX_SIGNATURE_VALUE_LENGTH = 20
fld public final static java.lang.String DEFAULT_ALGORITHM = "SHA1withDSA"
meth public boolean verify(org.apache.sshd.common.session.SessionContext,byte[]) throws java.lang.Exception
meth public byte[] sign(org.apache.sshd.common.session.SessionContext) throws java.lang.Exception
meth public static void putBigInteger(java.math.BigInteger,byte[],int)
supr org.apache.sshd.common.signature.AbstractSignature

CLSS public org.apache.sshd.common.signature.SignatureECDSA
cons protected init(java.lang.String,java.lang.String)
innr public static SignatureECDSA256
innr public static SignatureECDSA384
innr public static SignatureECDSA521
meth public boolean verify(org.apache.sshd.common.session.SessionContext,byte[]) throws java.lang.Exception
meth public byte[] sign(org.apache.sshd.common.session.SessionContext) throws java.lang.Exception
supr org.apache.sshd.common.signature.AbstractSignature

CLSS public static org.apache.sshd.common.signature.SignatureECDSA$SignatureECDSA256
 outer org.apache.sshd.common.signature.SignatureECDSA
cons public init()
fld public final static java.lang.String DEFAULT_ALGORITHM = "SHA256withECDSA"
supr org.apache.sshd.common.signature.SignatureECDSA

CLSS public static org.apache.sshd.common.signature.SignatureECDSA$SignatureECDSA384
 outer org.apache.sshd.common.signature.SignatureECDSA
cons public init()
fld public final static java.lang.String DEFAULT_ALGORITHM = "SHA384withECDSA"
supr org.apache.sshd.common.signature.SignatureECDSA

CLSS public static org.apache.sshd.common.signature.SignatureECDSA$SignatureECDSA521
 outer org.apache.sshd.common.signature.SignatureECDSA
cons public init()
fld public final static java.lang.String DEFAULT_ALGORITHM = "SHA512withECDSA"
supr org.apache.sshd.common.signature.SignatureECDSA

CLSS public abstract interface org.apache.sshd.common.signature.SignatureFactoriesHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories()
meth public java.lang.String getSignatureFactoriesNameList()
meth public java.util.List<java.lang.String> getSignatureFactoriesNames()

CLSS public abstract interface org.apache.sshd.common.signature.SignatureFactoriesManager
intf org.apache.sshd.common.signature.SignatureFactoriesHolder
meth public !varargs void setSignatureFactoriesNames(java.lang.String[])
meth public abstract void setSignatureFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
meth public static java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories(org.apache.sshd.common.signature.SignatureFactoriesManager)
meth public static java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> resolveSignatureFactories(org.apache.sshd.common.signature.SignatureFactoriesManager,org.apache.sshd.common.signature.SignatureFactoriesManager)
meth public void setSignatureFactoriesNameList(java.lang.String)
meth public void setSignatureFactoriesNames(java.util.Collection<java.lang.String>)

CLSS public abstract interface org.apache.sshd.common.signature.SignatureFactory
fld public final static java.util.List<java.lang.String> ECC_SIGNATURE_TYPE_PREFERENCES
fld public final static java.util.List<java.lang.String> RSA_SIGNATURE_TYPE_PREFERENCES
intf org.apache.sshd.common.BuiltinFactory<org.apache.sshd.common.signature.Signature>
meth public static int resolvePreferredSignaturePosition(java.util.List<? extends org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>,org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>)
meth public static int resolvePreferredSignaturePosition(java.util.List<java.lang.String>,int,java.util.Map<java.lang.String,java.lang.Integer>)
meth public static java.util.List<java.lang.String> resolveSignatureFactoriesProposal(java.lang.Iterable<java.lang.String>,java.util.Collection<? extends org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
meth public static java.util.List<java.lang.String> resolveSignatureFactoryNamesProposal(java.lang.Iterable<java.lang.String>,java.util.Collection<java.lang.String>)
meth public static org.apache.sshd.common.NamedFactory<? extends org.apache.sshd.common.signature.Signature> resolveSignatureFactory(java.lang.String,java.util.Collection<? extends org.apache.sshd.common.NamedFactory<? extends org.apache.sshd.common.signature.Signature>>)
meth public static org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature> resolveSignatureFactoryByPublicKey(java.security.PublicKey,java.lang.String) throws java.security.spec.InvalidKeySpecException

CLSS public abstract org.apache.sshd.common.signature.SignatureRSA
cons protected init(java.lang.String,java.lang.String)
fld public final static java.util.NavigableSet<java.lang.String> SUPPORTED_KEY_TYPES
meth protected int getVerifierSignatureSize()
meth public boolean verify(org.apache.sshd.common.session.SessionContext,byte[]) throws java.lang.Exception
meth public static int getVerifierSignatureSize(java.security.interfaces.RSAKey)
meth public void initVerifier(org.apache.sshd.common.session.SessionContext,java.security.PublicKey) throws java.lang.Exception
supr org.apache.sshd.common.signature.AbstractSignature
hfds verifierSignatureSize

CLSS public org.apache.sshd.common.signature.SignatureRSASHA1
cons public init()
fld public final static java.lang.String ALGORITHM = "SHA1withRSA"
supr org.apache.sshd.common.signature.SignatureRSA

CLSS public org.apache.sshd.common.signature.SignatureRSASHA256
cons public init()
fld public final static java.lang.String ALGORITHM = "SHA256withRSA"
supr org.apache.sshd.common.signature.SignatureRSA

CLSS public org.apache.sshd.common.signature.SignatureRSASHA512
cons public init()
fld public final static java.lang.String ALGORITHM = "SHA512withRSA"
supr org.apache.sshd.common.signature.SignatureRSA

CLSS public org.apache.sshd.common.signature.SignatureSkECDSA
cons public init()
fld public final static java.lang.String ALGORITHM = "ECDSA-SK"
meth protected java.lang.String getSignatureKeyType()
meth protected org.apache.sshd.common.signature.Signature getDelegateSignature()
meth public java.lang.String getAlgorithm()
supr org.apache.sshd.common.signature.AbstractSecurityKeySignature

CLSS public org.apache.sshd.common.signature.SignatureSkED25519
cons public init()
fld public final static java.lang.String ALGORITHM = "ED25519-SK"
meth protected java.lang.String getSignatureKeyType()
meth protected org.apache.sshd.common.signature.Signature getDelegateSignature()
meth public java.lang.String getAlgorithm()
supr org.apache.sshd.common.signature.AbstractSecurityKeySignature

CLSS abstract interface org.apache.sshd.common.signature.package-info

CLSS public final org.apache.sshd.common.util.EventListenerUtils
fld public final static java.util.Comparator<java.util.EventListener> LISTENER_INSTANCE_COMPARATOR
meth public static <%0 extends org.apache.sshd.common.util.SshdEventListener> java.util.Set<{%%0}> synchronizedListenersSet()
meth public static <%0 extends org.apache.sshd.common.util.SshdEventListener> java.util.Set<{%%0}> synchronizedListenersSet(java.util.Collection<? extends {%%0}>)
meth public static <%0 extends org.apache.sshd.common.util.SshdEventListener> {%%0} proxyWrapper(java.lang.Class<{%%0}>,java.lang.ClassLoader,java.lang.Iterable<? extends {%%0}>)
meth public static <%0 extends org.apache.sshd.common.util.SshdEventListener> {%%0} proxyWrapper(java.lang.Class<{%%0}>,java.lang.Iterable<? extends {%%0}>)
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.common.util.EventNotifier<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract void notifyEvent({org.apache.sshd.common.util.EventNotifier%0}) throws java.lang.Exception

CLSS public final org.apache.sshd.common.util.ExceptionUtils
meth public static <%0 extends java.lang.Throwable> {%%0} accumulateException({%%0},{%%0})
meth public static java.lang.RuntimeException toRuntimeException(java.lang.Throwable)
meth public static java.lang.RuntimeException toRuntimeException(java.lang.Throwable,boolean)
meth public static java.lang.Throwable peelException(java.lang.Throwable)
meth public static java.lang.Throwable resolveExceptionCause(java.lang.Throwable)
meth public static void rethrowAsIoException(java.lang.Throwable) throws java.io.IOException
supr java.lang.Object

CLSS public final org.apache.sshd.common.util.GenericUtils
fld public final static boolean[] EMPTY_BOOLEAN_ARRAY
fld public final static byte[] EMPTY_BYTE_ARRAY
fld public final static char[] EMPTY_CHAR_ARRAY
fld public final static java.lang.Object NULL
fld public final static java.lang.Object[] EMPTY_OBJECT_ARRAY
fld public final static java.lang.String QUOTES = "\u0022'"
fld public final static java.lang.String[] EMPTY_STRING_ARRAY
fld public final static java.util.Comparator<java.lang.String> CASE_SENSITIVE_ORDER
meth public !varargs static <%0 extends java.lang.Comparable<{%%0}>> java.util.NavigableSet<{%%0}> asSortedSet({%%0}[])
 anno 0 java.lang.SafeVarargs()
meth public !varargs static <%0 extends java.lang.Enum<{%%0}>> java.util.Set<{%%0}> of({%%0}[])
 anno 0 java.lang.SafeVarargs()
meth public !varargs static <%0 extends java.lang.Object> int length({%%0}[])
 anno 0 java.lang.SafeVarargs()
meth public !varargs static <%0 extends java.lang.Object> java.util.List<{%%0}> asList({%%0}[])
 anno 0 java.lang.SafeVarargs()
meth public !varargs static <%0 extends java.lang.Object> java.util.List<{%%0}> selectMatchingMembers(java.util.function.Predicate<? super {%%0}>,{%%0}[])
 anno 0 java.lang.SafeVarargs()
meth public !varargs static <%0 extends java.lang.Object> java.util.List<{%%0}> unmodifiableList({%%0}[])
 anno 0 java.lang.SafeVarargs()
meth public !varargs static <%0 extends java.lang.Object> java.util.NavigableSet<{%%0}> asSortedSet(java.util.Comparator<? super {%%0}>,{%%0}[])
 anno 0 java.lang.SafeVarargs()
meth public !varargs static <%0 extends java.lang.Object> java.util.Set<{%%0}> asSet({%%0}[])
 anno 0 java.lang.SafeVarargs()
meth public !varargs static <%0 extends java.lang.Object> {%%0} findFirstMatchingMember(java.util.function.Predicate<? super {%%0}>,{%%0}[])
 anno 0 java.lang.SafeVarargs()
meth public static <%0 extends java.lang.Comparable<{%%0}>> java.util.NavigableSet<{%%0}> asSortedSet(java.util.Collection<? extends {%%0}>)
meth public static <%0 extends java.lang.Enum<{%%0}>> java.util.Set<{%%0}> of(java.util.Collection<? extends {%%0}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.lang.Iterable<{%%1}> wrapIterable(java.lang.Iterable<? extends {%%0}>,java.util.function.Function<? super {%%0},? extends {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Iterator<{%%1}> wrapIterator(java.lang.Iterable<? extends {%%0}>,java.util.function.Function<? super {%%0},? extends {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Iterator<{%%1}> wrapIterator(java.util.Iterator<? extends {%%0}>,java.util.function.Function<? super {%%0},? extends {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.List<{%%1}> map(java.util.Collection<? extends {%%0}>,java.util.function.Function<? super {%%0},? extends {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.NavigableSet<{%%1}> mapSort(java.util.Collection<? extends {%%0}>,java.util.function.Function<? super {%%0},? extends {%%1}>,java.util.Comparator<? super {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends {%%0}> java.util.function.Function<{%%1},{%%0}> downcast()
meth public static <%0 extends java.lang.Object> boolean containsAny(java.util.Collection<? extends {%%0}>,java.lang.Iterable<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> boolean equals(java.util.Collection<{%%0}>,java.util.Collection<{%%0}>)
meth public static <%0 extends java.lang.Object> boolean isEmpty(java.lang.Iterable<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> boolean isEmpty(java.util.Iterator<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> boolean isEmpty({%%0}[])
meth public static <%0 extends java.lang.Object> boolean isNotEmpty(java.lang.Iterable<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> boolean isNotEmpty(java.util.Iterator<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> int findFirstDifferentValueIndex(java.lang.Iterable<? extends {%%0}>,java.lang.Iterable<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> int findFirstDifferentValueIndex(java.lang.Iterable<? extends {%%0}>,java.lang.Iterable<? extends {%%0}>,org.apache.sshd.common.util.functors.UnaryEquator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> int findFirstDifferentValueIndex(java.util.Iterator<? extends {%%0}>,java.util.Iterator<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> int findFirstDifferentValueIndex(java.util.Iterator<? extends {%%0}>,java.util.Iterator<? extends {%%0}>,org.apache.sshd.common.util.functors.UnaryEquator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> int findFirstDifferentValueIndex(java.util.List<? extends {%%0}>,java.util.List<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> int findFirstDifferentValueIndex(java.util.List<? extends {%%0}>,java.util.List<? extends {%%0}>,org.apache.sshd.common.util.functors.UnaryEquator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.lang.Iterable<{%%0}> multiIterableSuppliers(java.lang.Iterable<? extends java.util.function.Supplier<? extends java.lang.Iterable<? extends {%%0}>>>)
meth public static <%0 extends java.lang.Object> java.lang.String join({%%0}[],char)
meth public static <%0 extends java.lang.Object> java.lang.String join({%%0}[],java.lang.CharSequence)
meth public static <%0 extends java.lang.Object> java.util.Iterator<{%%0}> iteratorOf(java.lang.Iterable<{%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Iterator<{%%0}> iteratorOf(java.util.Iterator<{%%0}>)
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> selectMatchingMembers(java.util.function.Predicate<? super {%%0}>,java.util.Collection<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> unmodifiableList(java.util.Collection<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> unmodifiableList(java.util.stream.Stream<{%%0}>)
meth public static <%0 extends java.lang.Object> java.util.NavigableSet<{%%0}> asSortedSet(java.util.Comparator<? super {%%0}>,java.util.Collection<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.function.Supplier<{%%0}> memoizeLock(java.util.function.Supplier<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.function.Supplier<{%%0}> supplierOf({%%0})
meth public static <%0 extends java.lang.Object> java.util.stream.Collector<{%%0},?,java.util.NavigableSet<{%%0}>> toSortedSet(java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.stream.Stream<{%%0}> stream(java.lang.Iterable<{%%0}>)
meth public static <%0 extends java.lang.Object> void forEach(java.lang.Iterable<? extends {%%0}>,java.util.function.Consumer<? super {%%0}>)
meth public static <%0 extends java.lang.Object> {%%0} findFirstMatchingMember(java.util.function.Predicate<? super {%%0}>,java.util.Collection<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> {%%0} head(java.lang.Iterable<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> {%%0} selectNextMatchingValue(java.util.Iterator<?>,java.lang.Class<{%%0}>)
meth public static boolean isBlank(java.lang.CharSequence)
meth public static boolean isEmpty(char[])
meth public static boolean isEmpty(java.lang.CharSequence)
meth public static boolean isEmpty(java.util.Collection<?>)
meth public static boolean isNegativeOrNull(java.time.Duration)
meth public static boolean isNotBlank(java.lang.CharSequence)
meth public static boolean isNotEmpty(java.lang.CharSequence)
meth public static boolean isNotEmpty(java.util.Collection<?>)
meth public static boolean isPositive(java.time.Duration)
meth public static int compare(char[],char[])
meth public static int hashCode(java.lang.String)
meth public static int hashCode(java.lang.String,java.lang.Boolean)
meth public static int indexOf(java.lang.CharSequence,char)
meth public static int lastIndexOf(java.lang.CharSequence,char)
meth public static int length(char[])
meth public static int length(java.lang.CharSequence)
meth public static int safeCompare(java.lang.String,java.lang.String,boolean)
meth public static int size(java.util.Collection<?>)
meth public static java.lang.CharSequence stripDelimiters(java.lang.CharSequence,char)
meth public static java.lang.CharSequence stripQuotes(java.lang.CharSequence)
meth public static java.lang.String join(java.lang.Iterable<?>,char)
meth public static java.lang.String join(java.lang.Iterable<?>,java.lang.CharSequence)
meth public static java.lang.String join(java.util.Iterator<?>,char)
meth public static java.lang.String join(java.util.Iterator<?>,java.lang.CharSequence)
meth public static java.lang.String replace(java.lang.String,java.lang.String,java.lang.String,int)
meth public static java.lang.String replaceWhitespaceAndTrim(java.lang.String)
meth public static java.lang.String trimToEmpty(java.lang.String)
meth public static java.lang.String[] split(java.lang.String,char)
meth public static java.util.List<java.lang.String> filterToNotBlank(java.util.List<java.lang.String>)
supr java.lang.Object

CLSS public org.apache.sshd.common.util.IgnoringEmptyMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
intf java.util.Map<{org.apache.sshd.common.util.IgnoringEmptyMap%0},{org.apache.sshd.common.util.IgnoringEmptyMap%1}>
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.String toString()
meth public java.util.Collection<{org.apache.sshd.common.util.IgnoringEmptyMap%1}> values()
meth public java.util.Set<java.util.Map$Entry<{org.apache.sshd.common.util.IgnoringEmptyMap%0},{org.apache.sshd.common.util.IgnoringEmptyMap%1}>> entrySet()
meth public java.util.Set<{org.apache.sshd.common.util.IgnoringEmptyMap%0}> keySet()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.apache.sshd.common.util.IgnoringEmptyMap<{%%0},{%%1}> getInstance()
meth public void clear()
meth public void putAll(java.util.Map<? extends {org.apache.sshd.common.util.IgnoringEmptyMap%0},? extends {org.apache.sshd.common.util.IgnoringEmptyMap%1}>)
meth public {org.apache.sshd.common.util.IgnoringEmptyMap%1} get(java.lang.Object)
meth public {org.apache.sshd.common.util.IgnoringEmptyMap%1} put({org.apache.sshd.common.util.IgnoringEmptyMap%0},{org.apache.sshd.common.util.IgnoringEmptyMap%1})
meth public {org.apache.sshd.common.util.IgnoringEmptyMap%1} remove(java.lang.Object)
supr java.lang.Object
hfds INSTANCE

CLSS public final org.apache.sshd.common.util.MapEntryUtils
innr public static EnumMapBuilder
innr public static GenericMapPopulator
innr public static MapBuilder
innr public static NavigableMapBuilder
meth public !varargs static <%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.util.Map<{%%0},{%%1}>> {%%2} mapValues(java.util.function.Function<? super {%%1},? extends {%%0}>,java.util.function.Supplier<? extends {%%2}>,{%%1}[])
 anno 0 java.lang.SafeVarargs()
meth public static <%0 extends java.lang.Comparable<{%%0}>, %1 extends java.lang.Object> java.util.Comparator<java.util.Map$Entry<{%%0},{%%1}>> byKeyEntryComparator()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object> java.util.NavigableMap<{%%1},{%%2}> toSortedMap(java.lang.Iterable<? extends {%%0}>,java.util.function.Function<? super {%%0},? extends {%%1}>,java.util.function.Function<? super {%%0},? extends {%%2}>,java.util.Comparator<? super {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object> java.util.stream.Collector<{%%0},?,java.util.NavigableMap<{%%1},{%%2}>> toSortedMap(java.util.function.Function<? super {%%0},? extends {%%1}>,java.util.function.Function<? super {%%0},? extends {%%2}>,java.util.Comparator<? super {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.util.Map<{%%0},{%%1}>> {%%2} mapValues(java.util.function.Function<? super {%%1},? extends {%%0}>,java.util.function.Supplier<? extends {%%2}>,java.util.Collection<? extends {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.util.Map<{%%1},{%%0}>> {%%2} flipMap(java.util.Map<? extends {%%0},? extends {%%1}>,java.util.function.Supplier<? extends {%%2}>,boolean)
meth public static <%0 extends java.lang.Object> java.util.function.BinaryOperator<{%%0}> throwingMerger()
meth public static <%0 extends java.lang.Object> java.util.function.Supplier<java.util.NavigableMap<java.lang.String,{%%0}>> caseInsensitiveMap()
meth public static boolean isEmpty(java.util.Map<?,?>)
meth public static boolean isNotEmpty(java.util.Map<?,?>)
meth public static int size(java.util.Map<?,?>)
supr java.lang.Object
hfds BY_KEY_COMPARATOR,CASE_INSENSITIVE_MAP_FACTORY

CLSS public static org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder<%0 extends java.lang.Enum<{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%0}>, %1 extends java.lang.Object>
 outer org.apache.sshd.common.util.MapEntryUtils
cons public init(java.lang.Class<{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%0}>)
meth public java.util.Map<{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%1}> build()
meth public java.util.Map<{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%1}> immutable()
meth public org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder<{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%1}> clear()
meth public org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder<{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%1}> put({org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%1})
meth public org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder<{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%1}> putAll(java.util.Map<? extends {org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%0},? extends {org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%1}>)
meth public org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder<{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%1}> remove({org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%0})
meth public static <%0 extends java.lang.Enum<{%%0}>, %1 extends java.lang.Object> org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder<{%%0},{%%1}> builder(java.lang.Class<{%%0}>)
supr org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator<{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%1},java.util.Map<{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$EnumMapBuilder%1}>>

CLSS public static org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.util.Map<{org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%0},{org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%1}>>
 outer org.apache.sshd.common.util.MapEntryUtils
cons public init({org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%2})
intf java.util.function.Supplier<{org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%2}>
meth public org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator<{org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%0},{org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%1},{org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%2}> clear()
meth public org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator<{org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%0},{org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%1},{org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%2}> put({org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%0},{org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%1})
meth public org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator<{org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%0},{org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%1},{org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%2}> putAll(java.util.Map<? extends {org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%0},? extends {org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%1}>)
meth public org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator<{org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%0},{org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%1},{org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%2}> remove({org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%0})
meth public {org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator%2} get()
supr java.lang.Object
hfds map

CLSS public static org.apache.sshd.common.util.MapEntryUtils$MapBuilder<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.apache.sshd.common.util.MapEntryUtils
cons public init()
meth public java.util.Map<{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%1}> build()
meth public java.util.Map<{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%1}> immutable()
meth public org.apache.sshd.common.util.MapEntryUtils$MapBuilder<{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%1}> clear()
meth public org.apache.sshd.common.util.MapEntryUtils$MapBuilder<{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%1}> put({org.apache.sshd.common.util.MapEntryUtils$MapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%1})
meth public org.apache.sshd.common.util.MapEntryUtils$MapBuilder<{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%1}> putAll(java.util.Map<? extends {org.apache.sshd.common.util.MapEntryUtils$MapBuilder%0},? extends {org.apache.sshd.common.util.MapEntryUtils$MapBuilder%1}>)
meth public org.apache.sshd.common.util.MapEntryUtils$MapBuilder<{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%1}> remove({org.apache.sshd.common.util.MapEntryUtils$MapBuilder%0})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.apache.sshd.common.util.MapEntryUtils$MapBuilder<{%%0},{%%1}> builder()
supr org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator<{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%1},java.util.Map<{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$MapBuilder%1}>>

CLSS public static org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.apache.sshd.common.util.MapEntryUtils
cons public init(java.util.Comparator<? super {org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%0}>)
meth public java.util.NavigableMap<{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%1}> build()
meth public java.util.NavigableMap<{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%1}> immutable()
meth public org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder<{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%1}> clear()
meth public org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder<{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%1}> put({org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%1})
meth public org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder<{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%1}> putAll(java.util.Map<? extends {org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%0},? extends {org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%1}>)
meth public org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder<{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%1}> remove({org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%0})
meth public static <%0 extends java.lang.Comparable<? super {%%0}>, %1 extends java.lang.Object> org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder<{%%0},{%%1}> builder()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder<{%%0},{%%1}> builder(java.util.Comparator<? super {%%0}>)
supr org.apache.sshd.common.util.MapEntryUtils$GenericMapPopulator<{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%1},java.util.NavigableMap<{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%0},{org.apache.sshd.common.util.MapEntryUtils$NavigableMapBuilder%1}>>

CLSS public final org.apache.sshd.common.util.NumberUtils
fld public final static java.util.List<java.lang.Class<?>> NUMERIC_PRIMITIVE_CLASSES
meth public !varargs static int hashCode(byte[])
meth public !varargs static int hashCode(int[])
meth public !varargs static int hashCode(long[])
meth public !varargs static int length(byte[])
meth public !varargs static int length(int[])
meth public !varargs static int length(long[])
meth public !varargs static java.lang.String join(char,boolean,byte[])
meth public !varargs static java.lang.String join(char,int[])
meth public !varargs static java.lang.String join(char,long[])
meth public !varargs static java.lang.String join(java.lang.CharSequence,boolean,byte[])
meth public !varargs static java.lang.String join(java.lang.CharSequence,int[])
meth public !varargs static java.lang.String join(java.lang.CharSequence,long[])
meth public !varargs static java.util.List<java.lang.Integer> asList(int[])
meth public static boolean isEmpty(byte[])
meth public static boolean isEmpty(int[])
meth public static boolean isEmpty(long[])
meth public static boolean isIntegerNumber(java.lang.CharSequence)
meth public static boolean isNumericClass(java.lang.Class<?>)
meth public static byte[] emptyIfNull(byte[])
meth public static int diffOffset(byte[],int,byte[],int,int)
meth public static int getNextPowerOf2(int)
meth public static int hashCode(byte[],int,int)
meth public static java.lang.Integer toInteger(java.lang.Number)
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.common.util.ObjectBuilder<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
intf java.util.function.Supplier<{org.apache.sshd.common.util.ObjectBuilder%0}>
meth public abstract {org.apache.sshd.common.util.ObjectBuilder%0} build()
meth public {org.apache.sshd.common.util.ObjectBuilder%0} get()

CLSS public final org.apache.sshd.common.util.OsUtils
fld public final static java.lang.String ANDROID_MODE_OVERRIDE_PROP = "org.apache.sshd.androidMode"
fld public final static java.lang.String CURRENT_USER_OVERRIDE_PROP = "org.apache.sshd.currentUser"
fld public final static java.lang.String DALVIK_MACHINE_OVERRIDE_PROP = "org.apache.sshd.dalvikMachine"
fld public final static java.lang.String JAVA_VERSION_OVERRIDE_PROP = "org.apache.sshd.javaVersion"
fld public final static java.lang.String LINUX_SHELL_COMMAND_NAME = "/bin/sh"
fld public final static java.lang.String OS_TYPE_OVERRIDE_PROP = "org.apache.sshd.osType"
fld public final static java.lang.String ROOT_USER = "root"
fld public final static java.lang.String WINDOWS_SHELL_COMMAND_NAME = "cmd.exe"
fld public final static java.util.List<java.lang.String> ANDROID_DETECTION_PROPERTIES
fld public final static java.util.List<java.lang.String> DALVIK_DETECTION_PROPERTIES
fld public final static java.util.List<java.lang.String> LINUX_COMMAND
fld public final static java.util.List<java.lang.String> WINDOWS_COMMAND
fld public final static java.util.function.Predicate<java.lang.String> ANDROID_PROPERTY_VALUE_MATCHER
fld public final static java.util.function.Predicate<java.lang.String> DALVIK_PROPERTY_VALUE_MATCHER
meth public static boolean isAndroid()
meth public static boolean isDalvikMachine()
meth public static boolean isOSX()
meth public static boolean isUNIX()
meth public static boolean isWin32()
meth public static java.lang.String getCanonicalUser(java.lang.String)
meth public static java.lang.String getComparablePath(java.lang.String)
meth public static java.lang.String getCurrentUser()
meth public static java.lang.String resolveCanonicalGroup(java.lang.String,java.lang.String)
meth public static java.lang.String resolveDefaultInteractiveShellCommand()
meth public static java.lang.String resolveDefaultInteractiveShellCommand(boolean)
meth public static java.nio.file.Path getCurrentWorkingDirectory()
meth public static java.util.List<java.lang.String> resolveDefaultInteractiveCommandElements()
meth public static java.util.List<java.lang.String> resolveDefaultInteractiveCommandElements(boolean)
meth public static org.apache.sshd.common.util.VersionInfo getJavaVersion()
meth public static void setAndroid(java.lang.Boolean)
meth public static void setCurrentUser(java.lang.String)
meth public static void setCurrentWorkingDirectoryResolver(java.util.function.Supplier<? extends java.nio.file.Path>)
meth public static void setDalvikMachine(java.lang.Boolean)
meth public static void setJavaVersion(org.apache.sshd.common.util.VersionInfo)
meth public static void setOS(java.lang.String)
supr java.lang.Object
hfds ANDROID_HOLDER,CURRENT_USER_HOLDER,CWD_PROVIDER_HOLDER,DALVIK_HOLDER,JAVA_VERSION_HOLDER,OS_TYPE_HOLDER

CLSS public final org.apache.sshd.common.util.ProxyUtils
meth public static <%0 extends java.lang.Object> {%%0} newProxyInstance(java.lang.Class<{%%0}>,java.lang.reflect.InvocationHandler)
meth public static <%0 extends java.lang.Object> {%%0} newProxyInstance(java.lang.ClassLoader,java.lang.Class<{%%0}>,java.lang.reflect.InvocationHandler)
meth public static java.lang.Throwable unwrapInvocationThrowable(java.lang.Throwable)
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.common.util.Readable
meth public abstract int available()
meth public abstract void getRawBytes(byte[],int,int)
meth public static org.apache.sshd.common.util.Readable readable(java.nio.ByteBuffer)

CLSS public final org.apache.sshd.common.util.ReflectionUtils
fld public final static java.util.function.Function<java.lang.reflect.Field,java.lang.String> FIELD_NAME_EXTRACTOR
meth public static <%0 extends java.lang.Object> {%%0} newInstance(java.lang.Class<?>,java.lang.Class<? extends {%%0}>) throws java.lang.ReflectiveOperationException
meth public static boolean isClassAvailable(java.lang.ClassLoader,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.lang.Object newInstance(java.lang.Class<?>) throws java.lang.ReflectiveOperationException
meth public static java.util.Collection<java.lang.reflect.Field> getMatchingDeclaredFields(java.lang.Class<?>,java.util.function.Predicate<? super java.lang.reflect.Field>)
meth public static java.util.Collection<java.lang.reflect.Field> getMatchingFields(java.lang.Class<?>,java.util.function.Predicate<? super java.lang.reflect.Field>)
supr java.lang.Object

CLSS public final org.apache.sshd.common.util.SelectorUtils
fld public final static java.lang.String ANT_HANDLER_PREFIX = "%ant["
fld public final static java.lang.String PATTERN_HANDLER_PREFIX = "["
fld public final static java.lang.String PATTERN_HANDLER_SUFFIX = "]"
fld public final static java.lang.String REGEX_HANDLER_PREFIX = "%regex["
meth public static boolean equals(char,char,boolean)
meth public static boolean isWindowsDriveSpecified(java.lang.CharSequence)
meth public static boolean isWindowsDriveSpecified(java.lang.CharSequence,int,int)
meth public static boolean match(java.lang.String,java.lang.String)
meth public static boolean match(java.lang.String,java.lang.String,boolean)
meth public static boolean matchAntPathPattern(java.lang.String,java.lang.String,boolean)
meth public static boolean matchAntPathPattern(java.lang.String,java.lang.String,java.lang.String,boolean)
meth public static boolean matchAntPathPatternStart(java.lang.String,java.lang.String,java.lang.String,boolean)
meth public static boolean matchPath(java.lang.String,java.lang.String)
meth public static boolean matchPath(java.lang.String,java.lang.String,boolean)
meth public static boolean matchPath(java.lang.String,java.lang.String,java.lang.String,boolean)
meth public static boolean matchPatternStart(java.lang.String,java.lang.String)
meth public static boolean matchPatternStart(java.lang.String,java.lang.String,boolean)
meth public static boolean matchPatternStart(java.lang.String,java.lang.String,java.lang.String,boolean)
meth public static java.lang.String applySlashifyRules(java.lang.String,char)
meth public static java.lang.String concatPaths(java.lang.String,java.lang.String,char)
meth public static java.lang.String removeWhitespace(java.lang.String)
meth public static java.lang.String translateToFileSystemPath(java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String translateToFileSystemPath(java.lang.String,java.lang.String,java.nio.file.FileSystem)
meth public static java.lang.String translateToLocalFileSystemPath(java.lang.String,char,java.lang.String)
meth public static java.lang.String translateToLocalFileSystemPath(java.lang.String,char,java.nio.file.FileSystem)
meth public static java.lang.String translateToLocalPath(java.lang.String)
meth public static java.util.List<java.lang.String> tokenizePath(java.lang.String)
meth public static java.util.List<java.lang.String> tokenizePath(java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.common.util.SshdEventListener
intf java.util.EventListener
meth public static <%0 extends org.apache.sshd.common.util.SshdEventListener> {%%0} validateListener({%%0},java.lang.String)

CLSS public final org.apache.sshd.common.util.ValidateUtils
meth public !varargs static <%0 extends java.lang.Object, %1 extends java.lang.Iterable<{%%0}>> {%%1} checkNotNullAndNotEmpty({%%1},java.lang.String,java.lang.Object[])
meth public !varargs static <%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.util.Map<{%%0},{%%1}>> {%%2} checkNotNullAndNotEmpty({%%2},java.lang.String,java.lang.Object[])
meth public !varargs static <%0 extends java.lang.Object, %1 extends java.util.Collection<{%%0}>> {%%1} checkNotNullAndNotEmpty({%%1},java.lang.String,java.lang.Object[])
meth public !varargs static <%0 extends java.lang.Object> {%%0} checkInstanceOf(java.lang.Object,java.lang.Class<{%%0}>,java.lang.String,java.lang.Object[])
meth public !varargs static <%0 extends java.lang.Object> {%%0} checkNotNull({%%0},java.lang.String,java.lang.Object[])
meth public !varargs static <%0 extends java.lang.Object> {%%0}[] checkNotNullAndNotEmpty({%%0}[],java.lang.String,java.lang.Object[])
meth public !varargs static <%0 extends java.lang.Throwable> {%%0} createFormattedException(java.util.function.Function<? super java.lang.String,? extends {%%0}>,java.lang.String,java.lang.Object[])
meth public !varargs static byte[] checkNotNullAndNotEmpty(byte[],java.lang.String,java.lang.Object[])
meth public !varargs static char[] checkNotNullAndNotEmpty(char[],java.lang.String,java.lang.Object[])
meth public !varargs static int[] checkNotNullAndNotEmpty(int[],java.lang.String,java.lang.Object[])
meth public !varargs static java.lang.String checkNotNullAndNotEmpty(java.lang.String,java.lang.String,java.lang.Object[])
meth public !varargs static java.lang.String hasContent(java.lang.String,java.lang.String,java.lang.Object[])
meth public !varargs static void checkState(boolean,java.lang.String,java.lang.Object[])
meth public !varargs static void checkTrue(boolean,java.lang.String,java.lang.Object[])
meth public !varargs static void throwIllegalArgumentException(java.lang.String,java.lang.Object[])
meth public !varargs static void throwIllegalStateException(java.lang.String,java.lang.Object[])
meth public static <%0 extends java.lang.Object> {%%0} checkInstanceOf(java.lang.Object,java.lang.Class<{%%0}>,java.lang.String)
meth public static <%0 extends java.lang.Object> {%%0} checkInstanceOf(java.lang.Object,java.lang.Class<{%%0}>,java.lang.String,java.lang.Object)
meth public static <%0 extends java.lang.Object> {%%0} checkInstanceOf(java.lang.Object,java.lang.Class<{%%0}>,java.lang.String,long)
meth public static <%0 extends java.lang.Object> {%%0} checkNotNull({%%0},java.lang.String)
meth public static <%0 extends java.lang.Object> {%%0} checkNotNull({%%0},java.lang.String,java.lang.Object)
meth public static <%0 extends java.lang.Object> {%%0} checkNotNull({%%0},java.lang.String,long)
meth public static <%0 extends java.lang.Throwable> {%%0} initializeExceptionCause({%%0},java.lang.Throwable)
meth public static byte[] checkNotNullAndNotEmpty(byte[],java.lang.String)
meth public static char[] checkNotNullAndNotEmpty(char[],java.lang.String)
meth public static int[] checkNotNullAndNotEmpty(int[],java.lang.String)
meth public static java.lang.String checkNotNullAndNotEmpty(java.lang.String,java.lang.String)
meth public static java.lang.String checkNotNullAndNotEmpty(java.lang.String,java.lang.String,java.lang.Object)
meth public static java.lang.String hasContent(java.lang.String,java.lang.String)
meth public static java.lang.String hasContent(java.lang.String,java.lang.String,java.lang.Object)
meth public static void checkState(boolean,java.lang.String)
meth public static void checkState(boolean,java.lang.String,java.lang.Object)
meth public static void checkState(boolean,java.lang.String,long)
meth public static void checkTrue(boolean,java.lang.String)
meth public static void checkTrue(boolean,java.lang.String,java.lang.Object)
meth public static void checkTrue(boolean,java.lang.String,long)
supr java.lang.Object

CLSS public org.apache.sshd.common.util.VersionInfo
cons public init(int,int)
cons public init(int,int,int,int)
intf java.io.Serializable
intf java.lang.Comparable<org.apache.sshd.common.util.VersionInfo>
meth public boolean equals(java.lang.Object)
meth public final int getBuildNumber()
meth public final int getMajorVersion()
meth public final int getMinorVersion()
meth public final int getRelease()
meth public int compareTo(org.apache.sshd.common.util.VersionInfo)
meth public int hashCode()
meth public java.lang.String toString()
meth public static org.apache.sshd.common.util.VersionInfo parse(java.lang.String)
supr java.lang.Object
hfds buildNumber,majorVersion,minorVersion,release,serialVersionUID

CLSS public abstract org.apache.sshd.common.util.buffer.Buffer
cons protected init()
fld protected final byte[] workBuf
intf org.apache.sshd.common.util.Readable
meth protected abstract int size()
meth protected abstract void copyRawBytes(int,byte[],int,int)
meth protected java.security.KeyPair extractEC(java.lang.String,java.security.spec.ECParameterSpec) throws java.security.GeneralSecurityException
meth public !varargs boolean isValidMessageStructure(java.lang.Class<?>[])
meth public abstract byte[] array()
meth public abstract byte[] getBytesConsumed()
meth public abstract byte[] getBytesConsumed(int)
meth public abstract int capacity()
meth public abstract int putBuffer(org.apache.sshd.common.util.Readable,boolean)
meth public abstract int rpos()
meth public abstract int wpos()
meth public abstract java.lang.String getString(java.nio.charset.Charset)
meth public abstract org.apache.sshd.common.util.buffer.Buffer clear(boolean)
meth public abstract org.apache.sshd.common.util.buffer.Buffer ensureCapacity(int,java.util.function.IntUnaryOperator)
meth public abstract void compact()
meth public abstract void putBuffer(java.nio.ByteBuffer)
meth public abstract void putRawBytes(byte[],int,int)
meth public abstract void rpos(int)
meth public abstract void wpos(int)
meth public boolean getBoolean()
meth public boolean isValidMessageStructure(java.util.Collection<java.lang.Class<?>>)
meth public byte getByte()
meth public byte rawByte(int)
meth public byte[] getBytes()
meth public byte[] getCompactData()
meth public byte[] getMPIntAsBytes()
meth public int ensureAvailable(int)
meth public int getInt()
meth public int getUByte()
meth public int getUShort()
meth public java.lang.String getString()
meth public java.lang.String toHex()
meth public java.lang.String toString()
meth public java.math.BigInteger getMPInt()
meth public java.security.KeyPair getKeyPair() throws org.apache.sshd.common.SshException
meth public java.security.PublicKey getPublicKey() throws org.apache.sshd.common.SshException
meth public java.security.PublicKey getPublicKey(org.apache.sshd.common.util.buffer.keys.BufferPublicKeyParser<? extends java.security.PublicKey>) throws org.apache.sshd.common.SshException
meth public java.security.PublicKey getRawPublicKey() throws org.apache.sshd.common.SshException
meth public java.security.PublicKey getRawPublicKey(org.apache.sshd.common.util.buffer.keys.BufferPublicKeyParser<? extends java.security.PublicKey>) throws org.apache.sshd.common.SshException
meth public java.util.Collection<java.lang.String> getAvailableStrings()
meth public java.util.Collection<java.lang.String> getAvailableStrings(java.nio.charset.Charset)
meth public java.util.Collection<java.lang.String> getStringList(boolean)
meth public java.util.Collection<java.lang.String> getStringList(boolean,java.nio.charset.Charset)
meth public java.util.List<java.lang.String> getNameList()
meth public java.util.List<java.lang.String> getNameList(char)
meth public java.util.List<java.lang.String> getNameList(java.nio.charset.Charset)
meth public java.util.List<java.lang.String> getNameList(java.nio.charset.Charset,char)
meth public java.util.List<java.lang.String> getStringList(int)
meth public java.util.List<java.lang.String> getStringList(int,java.nio.charset.Charset)
meth public java.util.List<org.apache.sshd.common.config.keys.OpenSshCertificate$CertificateOption> getCertificateOptions()
meth public java.util.List<org.apache.sshd.common.config.keys.OpenSshCertificate$CertificateOption> getCertificateOptions(java.nio.charset.Charset)
meth public long getLong()
meth public long getUInt()
meth public long rawUInt(int)
meth public org.apache.sshd.common.util.buffer.Buffer clear()
meth public org.apache.sshd.common.util.buffer.Buffer ensureCapacity(int)
meth public short getShort()
meth public void dumpHex(org.apache.sshd.common.util.logging.SimplifiedLog,java.lang.String,org.apache.sshd.common.PropertyResolver)
meth public void dumpHex(org.apache.sshd.common.util.logging.SimplifiedLog,java.util.logging.Level,java.lang.String,org.apache.sshd.common.PropertyResolver)
meth public void getRawBytes(byte[])
meth public void putAndWipeBytes(byte[])
meth public void putAndWipeBytes(byte[],int,int)
meth public void putAndWipeChars(char[])
meth public void putAndWipeChars(char[],int,int)
meth public void putAndWipeChars(char[],int,int,java.nio.charset.Charset)
meth public void putAndWipeChars(char[],java.nio.charset.Charset)
meth public void putBoolean(boolean)
meth public void putBuffer(org.apache.sshd.common.util.Readable)
meth public void putBufferedData(java.lang.Object)
meth public void putByte(byte)
meth public void putBytes(byte[])
meth public void putBytes(byte[],int,int)
meth public void putCertificateOptions(java.util.List<org.apache.sshd.common.config.keys.OpenSshCertificate$CertificateOption>)
meth public void putCertificateOptions(java.util.List<org.apache.sshd.common.config.keys.OpenSshCertificate$CertificateOption>,java.nio.charset.Charset)
meth public void putChars(char[])
meth public void putChars(char[],int,int)
meth public void putChars(char[],int,int,java.nio.charset.Charset)
meth public void putChars(char[],java.nio.charset.Charset)
meth public void putInt(long)
meth public void putKeyPair(java.security.KeyPair)
meth public void putLong(long)
meth public void putMPInt(byte[])
meth public void putMPInt(java.math.BigInteger)
meth public void putNameList(java.util.Collection<java.lang.String>)
meth public void putNameList(java.util.Collection<java.lang.String>,char)
meth public void putNameList(java.util.Collection<java.lang.String>,java.nio.charset.Charset)
meth public void putNameList(java.util.Collection<java.lang.String>,java.nio.charset.Charset,char)
meth public void putOptionalBufferedData(java.lang.Object)
meth public void putPublicKey(java.security.PublicKey)
meth public void putRawBytes(byte[])
meth public void putRawPublicKey(java.security.PublicKey)
meth public void putRawPublicKeyBytes(java.security.PublicKey)
meth public void putShort(int)
meth public void putString(java.lang.String)
meth public void putString(java.lang.String,java.nio.charset.Charset)
meth public void putStringList(java.util.Collection<?>,boolean)
meth public void putStringList(java.util.Collection<?>,java.nio.charset.Charset,boolean)
meth public void putUInt(long)
supr java.lang.Object

CLSS public org.apache.sshd.common.util.buffer.BufferException
cons public init(java.lang.String)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public final org.apache.sshd.common.util.buffer.BufferUtils
fld public final static char DEFAULT_HEX_SEPARATOR = ' '
fld public final static char EMPTY_HEX_SEPARATOR = '\u0000'
fld public final static int MAX_UINT8_VALUE = 255
fld public final static java.lang.String HEX_DIGITS = "0123456789abcdef"
fld public final static java.util.function.IntUnaryOperator DEFAULT_BUFFER_GROWTH_FACTOR
fld public final static java.util.logging.Level DEFAULT_HEXDUMP_LEVEL
fld public final static long MAX_UINT32_VALUE = 4294967295
meth public !varargs static <%0 extends java.lang.Appendable> {%%0} appendHex({%%0},char,byte[]) throws java.io.IOException
meth public !varargs static java.lang.String toHex(byte[])
meth public !varargs static java.lang.String toHex(char,byte[])
meth public !varargs static long getUInt(byte[])
meth public !varargs static long validateInt32Value(long,java.lang.String,java.lang.Object[])
meth public !varargs static long validateUint32Value(long,java.lang.String,java.lang.Object[])
meth public !varargs static void dumpHex(org.apache.sshd.common.util.logging.SimplifiedLog,java.util.logging.Level,java.lang.String,char,int,byte[])
meth public !varargs static void dumpHex(org.apache.sshd.common.util.logging.SimplifiedLog,java.util.logging.Level,java.lang.String,org.apache.sshd.common.PropertyResolver,char,byte[])
meth public static <%0 extends java.io.OutputStream> int decodeHex({%%0},char,java.lang.CharSequence) throws java.io.IOException
meth public static <%0 extends java.io.OutputStream> int decodeHex({%%0},char,java.lang.CharSequence,int,int) throws java.io.IOException
meth public static <%0 extends java.lang.Appendable> {%%0} appendHex({%%0},byte[],int,int,char) throws java.io.IOException
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} clear({%%0})
meth public static boolean equals(byte[],byte[])
meth public static boolean equals(byte[],int,byte[],int,int)
meth public static boolean isValidInt32Value(long)
meth public static boolean isValidUint32Value(long)
meth public static byte fromHex(char,char)
meth public static byte[] decodeHex(char,java.lang.CharSequence)
meth public static byte[] decodeHex(char,java.lang.CharSequence,int,int)
meth public static int getInt(byte[],int,int)
meth public static int getNextPowerOf2(int)
meth public static int indexOf(byte[],byte,int,int)
meth public static int putLong(long,byte[],int,int)
meth public static int putUInt(long,byte[])
meth public static int putUInt(long,byte[],int,int)
meth public static int readInt(java.io.InputStream,byte[]) throws java.io.IOException
meth public static int readInt(java.io.InputStream,byte[],int,int) throws java.io.IOException
meth public static int updateLengthPlaceholder(org.apache.sshd.common.util.buffer.Buffer,int)
meth public static java.lang.String toHex(byte[],int,int)
meth public static java.lang.String toHex(byte[],int,int,char)
meth public static java.math.BigInteger fromMPIntBytes(byte[])
meth public static long getLong(byte[],int,int)
meth public static long getUInt(byte[],int,int)
meth public static long readUInt(java.io.InputStream,byte[]) throws java.io.IOException
meth public static long readUInt(java.io.InputStream,byte[],int,int) throws java.io.IOException
meth public static long validateInt32Value(long,java.lang.String)
meth public static long validateInt32Value(long,java.lang.String,java.lang.Object)
meth public static long validateUint32Value(long,java.lang.String)
meth public static long validateUint32Value(long,java.lang.String,java.lang.Object)
meth public static void dumpHex(org.apache.sshd.common.util.logging.SimplifiedLog,java.util.logging.Level,java.lang.String,char,int,byte[],int,int)
meth public static void dumpHex(org.apache.sshd.common.util.logging.SimplifiedLog,java.util.logging.Level,java.lang.String,org.apache.sshd.common.PropertyResolver,char,byte[],int,int)
meth public static void updateLengthPlaceholder(org.apache.sshd.common.util.buffer.Buffer,int,long)
meth public static void writeInt(java.io.OutputStream,int,byte[]) throws java.io.IOException
meth public static void writeInt(java.io.OutputStream,int,byte[],int,int) throws java.io.IOException
meth public static void writeUInt(java.io.OutputStream,long,byte[]) throws java.io.IOException
meth public static void writeUInt(java.io.OutputStream,long,byte[],int,int) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.sshd.common.util.buffer.ByteArrayBuffer
cons public init()
cons public init(byte[])
cons public init(byte[],boolean)
cons public init(byte[],int,int)
cons public init(byte[],int,int,boolean)
cons public init(int)
cons public init(int,boolean)
fld public final static int DEFAULT_SIZE = 256
meth protected int size()
meth protected void copyRawBytes(int,byte[],int,int)
meth public byte getByte()
meth public byte rawByte(int)
meth public byte[] array()
meth public byte[] getBytesConsumed()
meth public byte[] getBytesConsumed(int)
meth public int available()
meth public int capacity()
meth public int getInt()
meth public int putBuffer(org.apache.sshd.common.util.Readable,boolean)
meth public int rpos()
meth public int wpos()
meth public java.lang.String getString(java.nio.charset.Charset)
meth public long getUInt()
meth public long rawUInt(int)
meth public org.apache.sshd.common.util.buffer.Buffer clear(boolean)
meth public org.apache.sshd.common.util.buffer.Buffer ensureCapacity(int,java.util.function.IntUnaryOperator)
meth public static org.apache.sshd.common.util.buffer.ByteArrayBuffer getCompactClone(byte[])
meth public static org.apache.sshd.common.util.buffer.ByteArrayBuffer getCompactClone(byte[],int,int)
meth public void compact()
meth public void getRawBytes(byte[],int,int)
meth public void putBuffer(java.nio.ByteBuffer)
meth public void putByte(byte)
meth public void putBytes(byte[],int,int)
meth public void putInt(long)
meth public void putRawBytes(byte[],int,int)
meth public void putUInt(long)
meth public void rpos(int)
meth public void wpos(int)
supr org.apache.sshd.common.util.buffer.Buffer
hfds data,rpos,wpos

CLSS public abstract org.apache.sshd.common.util.buffer.keys.AbstractBufferPublicKeyParser<%0 extends java.security.PublicKey>
cons protected !varargs init(java.lang.Class<{org.apache.sshd.common.util.buffer.keys.AbstractBufferPublicKeyParser%0}>,java.lang.String[])
cons protected init(java.lang.Class<{org.apache.sshd.common.util.buffer.keys.AbstractBufferPublicKeyParser%0}>,java.util.Collection<java.lang.String>)
intf org.apache.sshd.common.util.buffer.keys.BufferPublicKeyParser<{org.apache.sshd.common.util.buffer.keys.AbstractBufferPublicKeyParser%0}>
meth protected <%0 extends java.security.spec.KeySpec> {org.apache.sshd.common.util.buffer.keys.AbstractBufferPublicKeyParser%0} generatePublicKey(java.lang.String,{%%0}) throws java.security.GeneralSecurityException
meth protected java.security.KeyFactory getKeyFactory(java.lang.String) throws java.security.GeneralSecurityException
meth public boolean isKeyTypeSupported(java.lang.String)
meth public final java.lang.Class<{org.apache.sshd.common.util.buffer.keys.AbstractBufferPublicKeyParser%0}> getKeyClass()
meth public java.lang.String toString()
meth public java.util.Collection<java.lang.String> getSupportedKeyTypes()
supr java.lang.Object
hfds keyClass,supported

CLSS public abstract interface org.apache.sshd.common.util.buffer.keys.BufferPublicKeyParser<%0 extends java.security.PublicKey>
fld public final static org.apache.sshd.common.util.buffer.keys.BufferPublicKeyParser<java.security.PublicKey> DEFAULT
fld public final static org.apache.sshd.common.util.buffer.keys.BufferPublicKeyParser<java.security.PublicKey> EMPTY
meth public abstract boolean isKeyTypeSupported(java.lang.String)
meth public abstract {org.apache.sshd.common.util.buffer.keys.BufferPublicKeyParser%0} getRawPublicKey(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.security.GeneralSecurityException
meth public static org.apache.sshd.common.util.buffer.keys.BufferPublicKeyParser<java.security.PublicKey> aggregate(java.util.Collection<? extends org.apache.sshd.common.util.buffer.keys.BufferPublicKeyParser<? extends java.security.PublicKey>>)

CLSS public org.apache.sshd.common.util.buffer.keys.DSSBufferPublicKeyParser
cons public init()
fld public final static org.apache.sshd.common.util.buffer.keys.DSSBufferPublicKeyParser INSTANCE
meth public java.security.interfaces.DSAPublicKey getRawPublicKey(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.security.GeneralSecurityException
supr org.apache.sshd.common.util.buffer.keys.AbstractBufferPublicKeyParser<java.security.interfaces.DSAPublicKey>

CLSS public org.apache.sshd.common.util.buffer.keys.ECBufferPublicKeyParser
cons public init()
fld public final static org.apache.sshd.common.util.buffer.keys.ECBufferPublicKeyParser INSTANCE
meth protected java.security.interfaces.ECPublicKey getRawECKey(java.lang.String,java.security.spec.ECParameterSpec,org.apache.sshd.common.util.buffer.Buffer) throws java.security.GeneralSecurityException
meth public java.security.interfaces.ECPublicKey getRawPublicKey(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.security.GeneralSecurityException
supr org.apache.sshd.common.util.buffer.keys.AbstractBufferPublicKeyParser<java.security.interfaces.ECPublicKey>

CLSS public org.apache.sshd.common.util.buffer.keys.ED25519BufferPublicKeyParser
cons public init()
fld public final static org.apache.sshd.common.util.buffer.keys.ED25519BufferPublicKeyParser INSTANCE
meth public java.security.PublicKey getRawPublicKey(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.security.GeneralSecurityException
supr org.apache.sshd.common.util.buffer.keys.AbstractBufferPublicKeyParser<java.security.PublicKey>

CLSS public org.apache.sshd.common.util.buffer.keys.OpenSSHCertPublicKeyParser
cons public init()
fld public final static java.util.List<java.lang.String> KEY_TYPES
fld public final static org.apache.sshd.common.util.buffer.keys.OpenSSHCertPublicKeyParser INSTANCE
meth public org.apache.sshd.common.config.keys.OpenSshCertificate getRawPublicKey(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.security.GeneralSecurityException
supr org.apache.sshd.common.util.buffer.keys.AbstractBufferPublicKeyParser<org.apache.sshd.common.config.keys.OpenSshCertificate>

CLSS public org.apache.sshd.common.util.buffer.keys.RSABufferPublicKeyParser
cons public init()
fld public final static org.apache.sshd.common.util.buffer.keys.RSABufferPublicKeyParser INSTANCE
meth public java.security.interfaces.RSAPublicKey getRawPublicKey(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.security.GeneralSecurityException
supr org.apache.sshd.common.util.buffer.keys.AbstractBufferPublicKeyParser<java.security.interfaces.RSAPublicKey>

CLSS public org.apache.sshd.common.util.buffer.keys.SkECBufferPublicKeyParser
cons public init()
fld public final static org.apache.sshd.common.util.buffer.keys.SkECBufferPublicKeyParser INSTANCE
meth public org.apache.sshd.common.config.keys.u2f.SkEcdsaPublicKey getRawPublicKey(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.security.GeneralSecurityException
supr org.apache.sshd.common.util.buffer.keys.AbstractBufferPublicKeyParser<org.apache.sshd.common.config.keys.u2f.SkEcdsaPublicKey>

CLSS public org.apache.sshd.common.util.buffer.keys.SkED25519BufferPublicKeyParser
cons public init()
fld public final static org.apache.sshd.common.util.buffer.keys.SkED25519BufferPublicKeyParser INSTANCE
meth public org.apache.sshd.common.config.keys.u2f.SkED25519PublicKey getRawPublicKey(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.security.GeneralSecurityException
supr org.apache.sshd.common.util.buffer.keys.AbstractBufferPublicKeyParser<org.apache.sshd.common.config.keys.u2f.SkED25519PublicKey>

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

CLSS public final static !enum org.apache.sshd.common.util.closeable.AbstractCloseable$State
 outer org.apache.sshd.common.util.closeable.AbstractCloseable
fld public final static org.apache.sshd.common.util.closeable.AbstractCloseable$State Closed
fld public final static org.apache.sshd.common.util.closeable.AbstractCloseable$State Graceful
fld public final static org.apache.sshd.common.util.closeable.AbstractCloseable$State Immediate
fld public final static org.apache.sshd.common.util.closeable.AbstractCloseable$State Opened
meth public static org.apache.sshd.common.util.closeable.AbstractCloseable$State valueOf(java.lang.String)
meth public static org.apache.sshd.common.util.closeable.AbstractCloseable$State[] values()
supr java.lang.Enum<org.apache.sshd.common.util.closeable.AbstractCloseable$State>

CLSS public abstract org.apache.sshd.common.util.closeable.AbstractInnerCloseable
cons protected init()
cons protected init(java.lang.String)
meth protected abstract org.apache.sshd.common.Closeable getInnerCloseable()
meth protected final org.apache.sshd.common.future.CloseFuture doCloseGracefully()
meth protected final void doCloseImmediately()
supr org.apache.sshd.common.util.closeable.AbstractCloseable

CLSS public org.apache.sshd.common.util.closeable.AutoCloseableDelegateInvocationHandler
cons public init(java.lang.Object,java.lang.AutoCloseable)
intf java.lang.reflect.InvocationHandler
meth public java.lang.AutoCloseable getAutoCloseableDelegate()
meth public java.lang.Object getProxyTarget()
meth public java.lang.Object invoke(java.lang.Object,java.lang.reflect.Method,java.lang.Object[]) throws java.lang.Throwable
meth public static <%0 extends java.lang.AutoCloseable> {%%0} wrapDelegateCloseable(java.lang.Object,java.lang.Class<{%%0}>,java.lang.AutoCloseable)
meth public static boolean isCloseMethod(java.lang.reflect.Method)
meth public static boolean isCloseMethodInvocation(java.lang.reflect.Method,java.lang.Object[])
supr java.lang.Object
hfds closers,delegate,proxyTarget

CLSS public final org.apache.sshd.common.util.closeable.Builder
cons public init(java.lang.Object)
intf org.apache.sshd.common.util.ObjectBuilder<org.apache.sshd.common.Closeable>
meth public !varargs final <%0 extends org.apache.sshd.common.future.SshFuture<{%%0}>> org.apache.sshd.common.util.closeable.Builder when(org.apache.sshd.common.future.SshFuture<{%%0}>[])
 anno 0 java.lang.SafeVarargs()
meth public !varargs org.apache.sshd.common.util.closeable.Builder parallel(org.apache.sshd.common.Closeable[])
meth public !varargs org.apache.sshd.common.util.closeable.Builder sequential(org.apache.sshd.common.Closeable[])
meth public <%0 extends org.apache.sshd.common.future.SshFuture<{%%0}>> org.apache.sshd.common.util.closeable.Builder when(java.lang.Object,java.lang.Iterable<? extends org.apache.sshd.common.future.SshFuture<{%%0}>>)
meth public <%0 extends org.apache.sshd.common.future.SshFuture<{%%0}>> org.apache.sshd.common.util.closeable.Builder when(org.apache.sshd.common.future.SshFuture<{%%0}>)
meth public org.apache.sshd.common.Closeable build()
meth public org.apache.sshd.common.util.closeable.Builder close(org.apache.sshd.common.Closeable)
meth public org.apache.sshd.common.util.closeable.Builder parallel(java.lang.Object,java.lang.Iterable<? extends org.apache.sshd.common.Closeable>)
meth public org.apache.sshd.common.util.closeable.Builder run(java.lang.Object,java.lang.Runnable)
meth public org.apache.sshd.common.util.closeable.Builder sequential(java.lang.Object,java.lang.Iterable<org.apache.sshd.common.Closeable>)
supr java.lang.Object
hfds closeables,lock

CLSS public org.apache.sshd.common.util.closeable.FuturesCloseable<%0 extends org.apache.sshd.common.future.SshFuture<{org.apache.sshd.common.util.closeable.FuturesCloseable%0}>>
cons public init(java.lang.Object,java.lang.Object,java.lang.Iterable<? extends org.apache.sshd.common.future.SshFuture<{org.apache.sshd.common.util.closeable.FuturesCloseable%0}>>)
meth protected void doClose(boolean)
supr org.apache.sshd.common.util.closeable.SimpleCloseable
hfds futures

CLSS public abstract org.apache.sshd.common.util.closeable.IoBaseCloseable
cons protected init()
cons protected init(java.lang.String)
intf org.apache.sshd.common.Closeable
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public org.apache.sshd.common.util.closeable.NioChannelDelegateInvocationHandler
cons public init(java.lang.Object,java.nio.channels.Channel)
meth public java.lang.Object invoke(java.lang.Object,java.lang.reflect.Method,java.lang.Object[]) throws java.lang.Throwable
meth public java.nio.channels.Channel getChannelDelegate()
meth public static <%0 extends java.nio.channels.Channel> {%%0} wrapDelegateChannel(java.lang.Object,java.lang.Class<{%%0}>,java.nio.channels.Channel)
meth public static boolean isQueryOpenMethodInvocation(java.lang.reflect.Method)
meth public static boolean isQueryOpenMethodInvocation(java.lang.reflect.Method,java.lang.Object[])
supr org.apache.sshd.common.util.closeable.AutoCloseableDelegateInvocationHandler

CLSS public org.apache.sshd.common.util.closeable.ParallelCloseable
cons public init(java.lang.Object,java.lang.Object,java.lang.Iterable<? extends org.apache.sshd.common.Closeable>)
meth protected void doClose(boolean)
supr org.apache.sshd.common.util.closeable.SimpleCloseable
hfds closeables

CLSS public org.apache.sshd.common.util.closeable.SequentialCloseable
cons public init(java.lang.Object,java.lang.Object,java.lang.Iterable<? extends org.apache.sshd.common.Closeable>)
meth protected void doClose(boolean)
supr org.apache.sshd.common.util.closeable.SimpleCloseable
hfds closeables

CLSS public org.apache.sshd.common.util.closeable.SimpleCloseable
cons public init(java.lang.Object,java.lang.Object)
fld protected final java.util.concurrent.atomic.AtomicBoolean closing
fld protected final org.apache.sshd.common.future.DefaultCloseFuture future
meth protected void doClose(boolean)
meth public boolean isClosed()
meth public boolean isClosing()
meth public java.lang.String toString()
meth public org.apache.sshd.common.future.CloseFuture close(boolean)
meth public void addCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
meth public void removeCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
supr org.apache.sshd.common.util.closeable.IoBaseCloseable

CLSS public final org.apache.sshd.common.util.functors.Int2IntFunction
meth public static java.util.function.IntUnaryOperator add(int)
meth public static java.util.function.IntUnaryOperator constant(int)
meth public static java.util.function.IntUnaryOperator div(int)
meth public static java.util.function.IntUnaryOperator mul(int)
meth public static java.util.function.IntUnaryOperator sub(int)
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.common.util.functors.UnaryEquator<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
intf java.util.function.BiPredicate<{org.apache.sshd.common.util.functors.UnaryEquator%0},{org.apache.sshd.common.util.functors.UnaryEquator%0}>
meth public org.apache.sshd.common.util.functors.UnaryEquator<{org.apache.sshd.common.util.functors.UnaryEquator%0}> and(org.apache.sshd.common.util.functors.UnaryEquator<? super {org.apache.sshd.common.util.functors.UnaryEquator%0}>)
meth public org.apache.sshd.common.util.functors.UnaryEquator<{org.apache.sshd.common.util.functors.UnaryEquator%0}> negate()
meth public org.apache.sshd.common.util.functors.UnaryEquator<{org.apache.sshd.common.util.functors.UnaryEquator%0}> or(org.apache.sshd.common.util.functors.UnaryEquator<? super {org.apache.sshd.common.util.functors.UnaryEquator%0}>)
meth public static <%0 extends java.lang.Object> boolean isSameReference({%%0},{%%0})
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.util.functors.UnaryEquator<{%%0}> comparing(java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.util.functors.UnaryEquator<{%%0}> defaultEquality()
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.util.functors.UnaryEquator<{%%0}> falsum()
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.util.functors.UnaryEquator<{%%0}> referenceEquality()
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.util.functors.UnaryEquator<{%%0}> verum()

CLSS public org.apache.sshd.common.util.helper.LazyIterablesConcatenator<%0 extends java.lang.Object>
cons public init(java.lang.Iterable<? extends java.lang.Iterable<? extends {org.apache.sshd.common.util.helper.LazyIterablesConcatenator%0}>>)
intf java.lang.Iterable<{org.apache.sshd.common.util.helper.LazyIterablesConcatenator%0}>
meth public java.lang.Iterable<? extends java.lang.Iterable<? extends {org.apache.sshd.common.util.helper.LazyIterablesConcatenator%0}>> getIterables()
meth public java.lang.String toString()
meth public java.util.Iterator<{org.apache.sshd.common.util.helper.LazyIterablesConcatenator%0}> iterator()
meth public static <%0 extends java.lang.Object> java.lang.Iterable<{%%0}> lazyConcatenateIterables(java.lang.Iterable<? extends java.lang.Iterable<? extends {%%0}>>)
supr java.lang.Object
hfds iterables

CLSS public org.apache.sshd.common.util.helper.LazyMatchingTypeIterable<%0 extends java.lang.Object>
cons public init(java.lang.Iterable<?>,java.lang.Class<{org.apache.sshd.common.util.helper.LazyMatchingTypeIterable%0}>)
intf java.lang.Iterable<{org.apache.sshd.common.util.helper.LazyMatchingTypeIterable%0}>
meth public java.lang.Class<{org.apache.sshd.common.util.helper.LazyMatchingTypeIterable%0}> getType()
meth public java.lang.Iterable<?> getValues()
meth public java.lang.String toString()
meth public java.util.Iterator<{org.apache.sshd.common.util.helper.LazyMatchingTypeIterable%0}> iterator()
meth public static <%0 extends java.lang.Object> java.lang.Iterable<{%%0}> lazySelectMatchingTypes(java.lang.Iterable<?>,java.lang.Class<{%%0}>)
supr java.lang.Object
hfds type,values

CLSS public org.apache.sshd.common.util.helper.LazyMatchingTypeIterator<%0 extends java.lang.Object>
cons public init(java.util.Iterator<?>,java.lang.Class<{org.apache.sshd.common.util.helper.LazyMatchingTypeIterator%0}>)
fld protected boolean finished
fld protected {org.apache.sshd.common.util.helper.LazyMatchingTypeIterator%0} nextValue
intf java.util.Iterator<{org.apache.sshd.common.util.helper.LazyMatchingTypeIterator%0}>
meth public boolean hasNext()
meth public java.lang.Class<{org.apache.sshd.common.util.helper.LazyMatchingTypeIterator%0}> getType()
meth public java.lang.String toString()
meth public java.util.Iterator<?> getValues()
meth public static <%0 extends java.lang.Object> java.util.Iterator<{%%0}> lazySelectMatchingTypes(java.util.Iterator<?>,java.lang.Class<{%%0}>)
meth public {org.apache.sshd.common.util.helper.LazyMatchingTypeIterator%0} next()
supr java.lang.Object
hfds type,values

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

CLSS public abstract interface org.apache.sshd.common.util.io.FileInfoExtractor<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.common.util.io.FileInfoExtractor<java.lang.Boolean> EXISTS
fld public final static org.apache.sshd.common.util.io.FileInfoExtractor<java.lang.Boolean> ISDIR
fld public final static org.apache.sshd.common.util.io.FileInfoExtractor<java.lang.Boolean> ISREG
fld public final static org.apache.sshd.common.util.io.FileInfoExtractor<java.lang.Boolean> ISSYMLINK
fld public final static org.apache.sshd.common.util.io.FileInfoExtractor<java.lang.Long> SIZE
fld public final static org.apache.sshd.common.util.io.FileInfoExtractor<java.nio.file.attribute.FileTime> LASTMODIFIED
fld public final static org.apache.sshd.common.util.io.FileInfoExtractor<java.util.Set<java.nio.file.attribute.PosixFilePermission>> PERMISSIONS
meth public abstract !varargs {org.apache.sshd.common.util.io.FileInfoExtractor%0} infoOf(java.nio.file.Path,java.nio.file.LinkOption[]) throws java.io.IOException

CLSS public org.apache.sshd.common.util.io.FileSnapshot
cons protected init(java.time.Instant,java.nio.file.attribute.FileTime,long,java.lang.Object)
fld public final static long UNKNOWN_SIZE = -1
fld public final static org.apache.sshd.common.util.io.FileSnapshot NO_FILE
meth protected boolean mayBeRacilyClean()
meth protected java.lang.Object getFileKey()
meth protected java.nio.file.attribute.FileTime getLastModified()
meth protected java.time.Instant getTime()
meth protected long getSize()
meth public !varargs org.apache.sshd.common.util.io.FileSnapshot reload(java.nio.file.Path,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs static org.apache.sshd.common.util.io.FileSnapshot save(java.nio.file.Path,java.nio.file.LinkOption[]) throws java.io.IOException
meth public boolean same(org.apache.sshd.common.util.io.FileSnapshot)
supr java.lang.Object
hfds WORST_CASE_TIMESTAMP_RESOLUTION,fileKey,lastModified,size,snapTime

CLSS public final org.apache.sshd.common.util.io.IoUtils
fld public final static int DEFAULT_COPY_SIZE = 8192
fld public final static java.lang.String ACL_VIEW_ATTR = "acl"
fld public final static java.lang.String CREATE_TIME_VIEW_ATTR = "creationTime"
fld public final static java.lang.String DIRECTORY_VIEW_ATTR = "isDirectory"
fld public final static java.lang.String EOL
fld public final static java.lang.String EXECUTABLE_VIEW_ATTR = "isExecutable"
fld public final static java.lang.String EXTENDED_VIEW_ATTR = "extended"
fld public final static java.lang.String FILEKEY_VIEW_ATTR = "fileKey"
fld public final static java.lang.String GROUPID_VIEW_ATTR = "gid"
fld public final static java.lang.String GROUP_VIEW_ATTR = "group"
fld public final static java.lang.String LASTACC_TIME_VIEW_ATTR = "lastAccessTime"
fld public final static java.lang.String LASTMOD_TIME_VIEW_ATTR = "lastModifiedTime"
fld public final static java.lang.String NUMLINKS_VIEW_ATTR = "nlink"
fld public final static java.lang.String OTHERFILE_VIEW_ATTR = "isOther"
fld public final static java.lang.String OWNER_VIEW_ATTR = "owner"
fld public final static java.lang.String PERMISSIONS_VIEW_ATTR = "permissions"
fld public final static java.lang.String REGFILE_VIEW_ATTR = "isRegularFile"
fld public final static java.lang.String SIZE_VIEW_ATTR = "size"
fld public final static java.lang.String SYMLINK_VIEW_ATTR = "isSymbolicLink"
fld public final static java.lang.String USERID_VIEW_ATTR = "uid"
fld public final static java.nio.file.CopyOption[] EMPTY_COPY_OPTIONS
fld public final static java.nio.file.LinkOption[] EMPTY_LINK_OPTIONS
fld public final static java.nio.file.OpenOption[] EMPTY_OPEN_OPTIONS
fld public final static java.nio.file.attribute.FileAttribute<?>[] EMPTY_FILE_ATTRIBUTES
fld public final static java.util.List<java.lang.String> WINDOWS_EXECUTABLE_EXTENSIONS
fld public final static java.util.Set<java.nio.file.StandardOpenOption> WRITEABLE_OPEN_OPTIONS
meth public !varargs static boolean followLinks(java.nio.file.LinkOption[])
meth public !varargs static java.io.IOException closeQuietly(java.io.Closeable[])
meth public !varargs static java.lang.Boolean checkFileExists(java.nio.file.Path,java.nio.file.LinkOption[])
meth public !varargs static java.lang.String getFileOwner(java.nio.file.Path,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs static java.nio.file.Path ensureDirectory(java.nio.file.Path,java.nio.file.LinkOption[])
meth public !varargs static java.util.Set<java.nio.file.attribute.PosixFilePermission> getPermissions(java.nio.file.Path,java.nio.file.LinkOption[]) throws java.io.IOException
meth public static boolean isExecutable(java.io.File)
meth public static boolean isWindowsExecutable(java.lang.String)
meth public static byte[] getEOLBytes()
meth public static byte[] toByteArray(java.io.InputStream) throws java.io.IOException
meth public static int read(java.io.InputStream,byte[]) throws java.io.IOException
meth public static int read(java.io.InputStream,byte[],int,int) throws java.io.IOException
meth public static java.io.IOException closeQuietly(java.io.Closeable)
meth public static java.io.IOException closeQuietly(java.util.Collection<? extends java.io.Closeable>)
meth public static java.lang.Boolean checkFileExistsAnySymlinks(java.nio.file.Path,boolean)
meth public static java.lang.String appendPathComponent(java.lang.String,java.lang.String)
meth public static java.nio.file.LinkOption[] getLinkOptions(boolean)
meth public static java.nio.file.Path buildPath(java.nio.file.Path,java.nio.file.FileSystem,java.util.List<java.lang.String>)
meth public static java.nio.file.Path buildRelativePath(java.nio.file.FileSystem,java.util.List<java.lang.String>)
meth public static java.nio.file.Path chroot(java.nio.file.Path,java.nio.file.Path)
meth public static java.nio.file.Path getFirstPartsOfPath(java.nio.file.Path,int)
meth public static java.nio.file.Path removeCdUpAboveRoot(java.nio.file.Path)
meth public static java.nio.file.attribute.PosixFilePermission validateExcludedPermissions(java.util.Collection<java.nio.file.attribute.PosixFilePermission>,java.util.Collection<java.nio.file.attribute.PosixFilePermission>)
meth public static java.util.List<java.lang.String> readAllLines(java.io.BufferedReader) throws java.io.IOException
meth public static java.util.List<java.lang.String> readAllLines(java.io.BufferedReader,int) throws java.io.IOException
meth public static java.util.List<java.lang.String> readAllLines(java.io.InputStream) throws java.io.IOException
meth public static java.util.List<java.lang.String> readAllLines(java.io.Reader) throws java.io.IOException
meth public static java.util.List<java.lang.String> readAllLines(java.net.URL) throws java.io.IOException
meth public static java.util.Set<java.nio.file.attribute.PosixFilePermission> getPermissionsFromFile(java.io.File)
meth public static long copy(java.io.InputStream,java.io.OutputStream) throws java.io.IOException
meth public static long copy(java.io.InputStream,java.io.OutputStream,int) throws java.io.IOException
meth public static void readFully(java.io.InputStream,byte[]) throws java.io.IOException
meth public static void readFully(java.io.InputStream,byte[],int,int) throws java.io.IOException
meth public static void setPermissions(java.nio.file.Path,java.util.Set<java.nio.file.attribute.PosixFilePermission>) throws java.io.IOException
meth public static void setPermissionsToFile(java.io.File,java.util.Collection<java.nio.file.attribute.PosixFilePermission>)
supr java.lang.Object
hfds EOL_BYTES,NO_FOLLOW_OPTIONS

CLSS public abstract interface org.apache.sshd.common.util.io.LineDataConsumer
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.common.util.io.LineDataConsumer FAIL
fld public final static org.apache.sshd.common.util.io.LineDataConsumer IGNORE
meth public abstract void consume(java.lang.CharSequence) throws java.io.IOException

CLSS public org.apache.sshd.common.util.io.ModifiableFileWatcher
cons public !varargs init(java.nio.file.Path,java.nio.file.LinkOption[])
cons public init(java.nio.file.Path)
fld protected final java.nio.file.LinkOption[] options
fld public final static java.util.Set<java.nio.file.attribute.PosixFilePermission> STRICTLY_PROHIBITED_FILE_PERMISSION
meth public !varargs org.apache.sshd.common.util.io.resource.PathResource toPathResource(java.nio.file.OpenOption[])
meth public !varargs static java.util.AbstractMap$SimpleImmutableEntry<java.lang.String,java.lang.Object> validateStrictConfigFilePermissions(java.nio.file.Path,java.nio.file.LinkOption[]) throws java.io.IOException
meth public boolean checkReloadRequired() throws java.io.IOException
meth public final boolean exists() throws java.io.IOException
meth public final java.nio.file.Path getPath()
meth public final java.nio.file.attribute.FileTime lastModified() throws java.io.IOException
meth public final long size() throws java.io.IOException
meth public java.lang.String toString()
meth public org.apache.sshd.common.util.io.resource.PathResource toPathResource()
meth public void resetReloadAttributes()
meth public void updateReloadAttributes() throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds file,metadata

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

CLSS public final org.apache.sshd.common.util.io.PathUtils
fld public final static char HOME_TILDE_CHAR = '~'
fld public final static java.util.Comparator<java.nio.file.Path> BY_CASE_INSENSITIVE_FILENAME
fld public final static java.util.Comparator<java.nio.file.Path> BY_CASE_SENSITIVE_FILENAME
fld public final static org.apache.sshd.common.util.functors.UnaryEquator<java.nio.file.Path> EQ_CASE_INSENSITIVE_FILENAME
fld public final static org.apache.sshd.common.util.functors.UnaryEquator<java.nio.file.Path> EQ_CASE_SENSITIVE_FILENAME
meth public static int safeCompareFilename(java.nio.file.Path,java.nio.file.Path,boolean)
meth public static java.lang.String normalizePath(java.lang.String)
meth public static java.lang.StringBuilder appendUserHome(java.lang.StringBuilder)
meth public static java.lang.StringBuilder appendUserHome(java.lang.StringBuilder,java.lang.String)
meth public static java.lang.StringBuilder appendUserHome(java.lang.StringBuilder,java.nio.file.Path)
meth public static java.nio.file.Path getUserHomeFolder()
meth public static void setUserHomeFolderResolver(java.util.function.Supplier<? extends java.nio.file.Path>)
supr java.lang.Object
hfds USER_HOME_RESOLVER_HOLDER
hcls LazyDefaultUserHomeFolderHolder

CLSS public final !enum org.apache.sshd.common.util.io.der.ASN1Class
fld public final static java.util.List<org.apache.sshd.common.util.io.der.ASN1Class> VALUES
fld public final static org.apache.sshd.common.util.io.der.ASN1Class APPLICATION
fld public final static org.apache.sshd.common.util.io.der.ASN1Class CONTEXT
fld public final static org.apache.sshd.common.util.io.der.ASN1Class PRIVATE
fld public final static org.apache.sshd.common.util.io.der.ASN1Class UNIVERSAL
meth public byte getClassValue()
meth public static org.apache.sshd.common.util.io.der.ASN1Class fromDERValue(int)
meth public static org.apache.sshd.common.util.io.der.ASN1Class fromName(java.lang.String)
meth public static org.apache.sshd.common.util.io.der.ASN1Class fromTypeValue(int)
meth public static org.apache.sshd.common.util.io.der.ASN1Class valueOf(java.lang.String)
meth public static org.apache.sshd.common.util.io.der.ASN1Class[] values()
supr java.lang.Enum<org.apache.sshd.common.util.io.der.ASN1Class>
hfds byteValue

CLSS public org.apache.sshd.common.util.io.der.ASN1Object
cons public !varargs init(byte,int,byte[])
cons public !varargs init(org.apache.sshd.common.util.io.der.ASN1Class,org.apache.sshd.common.util.io.der.ASN1Type,boolean,int,byte[])
cons public init()
fld public final static byte CONSTRUCTED = 32
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public boolean isConstructed()
meth public byte[] getPureValueBytes()
meth public byte[] getValue()
meth public int getLength()
meth public int hashCode()
meth public java.lang.Object asObject() throws java.io.IOException
meth public java.lang.String asString() throws java.io.IOException
meth public java.lang.String toString()
meth public java.math.BigInteger asInteger() throws java.io.IOException
meth public java.math.BigInteger toInteger()
meth public java.util.List<java.lang.Integer> asOID() throws java.io.IOException
meth public java.util.List<java.lang.Integer> toOID() throws java.io.IOException
meth public org.apache.sshd.common.util.io.der.ASN1Class getObjClass()
meth public org.apache.sshd.common.util.io.der.ASN1Object clone()
meth public org.apache.sshd.common.util.io.der.ASN1Type getObjType()
meth public org.apache.sshd.common.util.io.der.DERParser createParser()
meth public void setConstructed(boolean)
meth public void setLength(int)
meth public void setObjClass(org.apache.sshd.common.util.io.der.ASN1Class)
meth public void setObjType(org.apache.sshd.common.util.io.der.ASN1Type)
meth public void setValue(byte[])
supr java.lang.Object
hfds constructed,length,objClass,objType,serialVersionUID,value

CLSS public final !enum org.apache.sshd.common.util.io.der.ASN1Type
fld public final static java.util.Set<org.apache.sshd.common.util.io.der.ASN1Type> VALUES
fld public final static org.apache.sshd.common.util.io.der.ASN1Type ANY
fld public final static org.apache.sshd.common.util.io.der.ASN1Type BIT_STRING
fld public final static org.apache.sshd.common.util.io.der.ASN1Type BMP_STRING
fld public final static org.apache.sshd.common.util.io.der.ASN1Type BOOLEAN
fld public final static org.apache.sshd.common.util.io.der.ASN1Type ENUMERATED
fld public final static org.apache.sshd.common.util.io.der.ASN1Type GENERALIZED_TIME
fld public final static org.apache.sshd.common.util.io.der.ASN1Type GENERAL_STRING
fld public final static org.apache.sshd.common.util.io.der.ASN1Type GRAPHIC_STRING
fld public final static org.apache.sshd.common.util.io.der.ASN1Type IA5_STRING
fld public final static org.apache.sshd.common.util.io.der.ASN1Type INTEGER
fld public final static org.apache.sshd.common.util.io.der.ASN1Type ISO646_STRING
fld public final static org.apache.sshd.common.util.io.der.ASN1Type NULL
fld public final static org.apache.sshd.common.util.io.der.ASN1Type NUMERIC_STRING
fld public final static org.apache.sshd.common.util.io.der.ASN1Type OBJECT_IDENTIFIER
fld public final static org.apache.sshd.common.util.io.der.ASN1Type OCTET_STRING
fld public final static org.apache.sshd.common.util.io.der.ASN1Type PRINTABLE_STRING
fld public final static org.apache.sshd.common.util.io.der.ASN1Type REAL
fld public final static org.apache.sshd.common.util.io.der.ASN1Type RELATIVE_OID
fld public final static org.apache.sshd.common.util.io.der.ASN1Type SEQUENCE
fld public final static org.apache.sshd.common.util.io.der.ASN1Type SET
fld public final static org.apache.sshd.common.util.io.der.ASN1Type T61_STRING
fld public final static org.apache.sshd.common.util.io.der.ASN1Type UNIVERSAL_STRING
fld public final static org.apache.sshd.common.util.io.der.ASN1Type UTC_TIME
fld public final static org.apache.sshd.common.util.io.der.ASN1Type UTF8_STRING
fld public final static org.apache.sshd.common.util.io.der.ASN1Type VIDEOTEX_STRING
meth public byte getTypeValue()
meth public static org.apache.sshd.common.util.io.der.ASN1Type fromDERValue(int)
meth public static org.apache.sshd.common.util.io.der.ASN1Type fromName(java.lang.String)
meth public static org.apache.sshd.common.util.io.der.ASN1Type fromTypeValue(int)
meth public static org.apache.sshd.common.util.io.der.ASN1Type valueOf(java.lang.String)
meth public static org.apache.sshd.common.util.io.der.ASN1Type[] values()
supr java.lang.Enum<org.apache.sshd.common.util.io.der.ASN1Type>
hfds typeValue

CLSS public org.apache.sshd.common.util.io.der.DERParser
cons public !varargs init(byte[])
cons public init(byte[],int,int)
cons public init(java.io.InputStream)
fld public final static int MAX_DER_VALUE_LENGTH = 65534
meth public int readLength() throws java.io.IOException
meth public java.math.BigInteger readBigInteger() throws java.io.IOException
meth public org.apache.sshd.common.util.io.der.ASN1Object readObject() throws java.io.IOException
supr java.io.FilterInputStream
hfds lenBytes

CLSS public org.apache.sshd.common.util.io.der.DERWriter
cons public init()
cons public init(int)
cons public init(java.io.OutputStream)
meth public !varargs void writeBigInteger(byte[]) throws java.io.IOException
meth public !varargs void writeObject(byte,int,byte[]) throws java.io.IOException
meth public byte[] toByteArray() throws java.io.IOException
meth public org.apache.sshd.common.util.io.der.DERWriter startSequence()
meth public void write(byte[],int,int) throws java.io.IOException
meth public void writeBigInteger(byte[],int,int) throws java.io.IOException
meth public void writeBigInteger(java.math.BigInteger) throws java.io.IOException
meth public void writeLength(int) throws java.io.IOException
meth public void writeObject(org.apache.sshd.common.util.io.der.ASN1Object) throws java.io.IOException
supr java.io.FilterOutputStream
hfds lenBytes

CLSS public abstract interface org.apache.sshd.common.util.io.functors.IOFunction<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public <%0 extends java.lang.Object> org.apache.sshd.common.util.io.functors.IOFunction<{%%0},{org.apache.sshd.common.util.io.functors.IOFunction%1}> compose(org.apache.sshd.common.util.io.functors.IOFunction<? super {%%0},? extends {org.apache.sshd.common.util.io.functors.IOFunction%0}>)
meth public <%0 extends java.lang.Object> org.apache.sshd.common.util.io.functors.IOFunction<{org.apache.sshd.common.util.io.functors.IOFunction%0},{%%0}> andThen(org.apache.sshd.common.util.io.functors.IOFunction<? super {org.apache.sshd.common.util.io.functors.IOFunction%1},? extends {%%0}>)
meth public abstract {org.apache.sshd.common.util.io.functors.IOFunction%1} apply({org.apache.sshd.common.util.io.functors.IOFunction%0}) throws java.io.IOException
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.util.io.functors.IOFunction<{%%0},{%%0}> identity()

CLSS public abstract interface org.apache.sshd.common.util.io.functors.Invoker<%0 extends java.lang.Object, %1 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract {org.apache.sshd.common.util.io.functors.Invoker%1} invoke({org.apache.sshd.common.util.io.functors.Invoker%0}) throws java.lang.Throwable
meth public static <%0 extends java.lang.Object> java.util.AbstractMap$SimpleImmutableEntry<org.apache.sshd.common.util.io.functors.Invoker<? super {%%0},?>,java.lang.Throwable> invokeTillFirstFailure({%%0},java.util.Collection<? extends org.apache.sshd.common.util.io.functors.Invoker<? super {%%0},?>>)
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.util.io.functors.Invoker<{%%0},java.lang.Void> wrapAll(java.util.Collection<? extends org.apache.sshd.common.util.io.functors.Invoker<? super {%%0},?>>)
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.util.io.functors.Invoker<{%%0},java.lang.Void> wrapFirst(java.util.Collection<? extends org.apache.sshd.common.util.io.functors.Invoker<? super {%%0},?>>)
meth public static <%0 extends java.lang.Object> void invokeAll({%%0},java.util.Collection<? extends org.apache.sshd.common.util.io.functors.Invoker<? super {%%0},?>>) throws java.lang.Throwable

CLSS public org.apache.sshd.common.util.io.input.CloseableEmptyInputStream
cons public init()
intf java.nio.channels.Channel
meth public boolean isOpen()
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void reset() throws java.io.IOException
supr org.apache.sshd.common.util.io.input.EmptyInputStream
hfds open

CLSS public org.apache.sshd.common.util.io.input.EmptyInputStream
cons public init()
fld public final static org.apache.sshd.common.util.io.input.EmptyInputStream DEV_NULL
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void mark(int)
meth public void reset() throws java.io.IOException
supr java.io.InputStream

CLSS public abstract org.apache.sshd.common.util.io.input.InputStreamWithChannel
cons protected init()
intf java.nio.channels.Channel
supr java.io.InputStream

CLSS public org.apache.sshd.common.util.io.input.LimitInputStream
cons public init(java.io.InputStream,long)
intf java.nio.channels.Channel
meth public boolean isOpen()
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
supr java.io.FilterInputStream
hfds open,remaining

CLSS public org.apache.sshd.common.util.io.input.NoCloseInputStream
cons public init(java.io.InputStream)
meth public static java.io.InputStream resolveInputStream(java.io.InputStream,boolean)
meth public void close() throws java.io.IOException
supr java.io.FilterInputStream

CLSS public org.apache.sshd.common.util.io.input.NoCloseReader
cons public init(java.io.Reader)
meth public static java.io.Reader resolveReader(java.io.Reader,boolean)
meth public void close() throws java.io.IOException
supr java.io.FilterReader

CLSS public org.apache.sshd.common.util.io.input.NullInputStream
cons public init()
intf java.nio.channels.Channel
meth public boolean isOpen()
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void reset() throws java.io.IOException
supr java.io.InputStream
hfds open

CLSS public abstract interface org.apache.sshd.common.util.io.output.LineLevelAppender
fld public final static int TYPICAL_LINE_LENGTH = 80
fld public final static org.apache.sshd.common.util.io.output.LineLevelAppender EMPTY
intf java.io.Closeable
intf org.apache.sshd.common.util.io.LineDataConsumer
meth public abstract boolean isWriteEnabled()
meth public abstract void writeLineData(java.lang.CharSequence) throws java.io.IOException
meth public static org.apache.sshd.common.util.io.output.LineLevelAppender wrap(java.lang.Appendable)
meth public static org.apache.sshd.common.util.io.output.LineLevelAppender wrap(java.lang.Appendable,java.util.function.BooleanSupplier)
meth public void consume(java.lang.CharSequence) throws java.io.IOException

CLSS public org.apache.sshd.common.util.io.output.LineLevelAppenderStream
cons public init(java.lang.String,org.apache.sshd.common.util.io.output.LineLevelAppender)
cons public init(java.nio.charset.Charset,org.apache.sshd.common.util.io.output.LineLevelAppender)
cons public init(java.nio.charset.CharsetDecoder,org.apache.sshd.common.util.io.output.LineLevelAppender)
cons public init(org.apache.sshd.common.util.io.output.LineLevelAppender)
fld protected char[] lineBuf
fld protected final java.nio.charset.CharsetDecoder csDecoder
fld protected final org.apache.sshd.common.util.io.output.LineLevelAppender appenderInstance
meth protected char[] ensureCharDataCapacity(int)
meth protected void handleLine(byte[],int,int) throws java.io.IOException
meth public final org.apache.sshd.common.util.io.output.LineLevelAppender getLineLevelAppender()
supr org.apache.sshd.common.util.io.output.LineOutputStream

CLSS public abstract org.apache.sshd.common.util.io.output.LineOutputStream
cons protected init()
fld protected byte[] lineBuf
fld protected final byte[] oneByte
fld protected int usedLen
meth protected abstract void handleLine(byte[],int,int) throws java.io.IOException
meth protected void accumulateLineData(byte[],int,int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream

CLSS public org.apache.sshd.common.util.io.output.LoggingFilterOutputStream
cons public init(java.io.OutputStream,java.lang.String,org.slf4j.Logger,int)
cons public init(java.io.OutputStream,java.lang.String,org.slf4j.Logger,org.apache.sshd.common.PropertyResolver)
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.FilterOutputStream
hfds chunkSize,log,msg,writeCount

CLSS public org.apache.sshd.common.util.io.output.NoCloseOutputStream
cons public init(java.io.OutputStream)
meth public static java.io.OutputStream resolveOutputStream(java.io.OutputStream,boolean)
meth public void close() throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
supr java.io.FilterOutputStream

CLSS public org.apache.sshd.common.util.io.output.NoCloseWriter
cons public init(java.io.Writer)
meth public static java.io.Writer resolveWriter(java.io.Writer,boolean)
meth public void close() throws java.io.IOException
supr java.io.FilterWriter

CLSS public org.apache.sshd.common.util.io.output.NullOutputStream
cons public init()
intf java.nio.channels.Channel
meth public boolean isOpen()
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream
hfds open

CLSS public org.apache.sshd.common.util.io.output.NullPrintStream
cons public init()
meth public !varargs java.io.PrintStream format(java.lang.String,java.lang.Object[])
meth public !varargs java.io.PrintStream format(java.util.Locale,java.lang.String,java.lang.Object[])
meth public !varargs java.io.PrintStream printf(java.lang.String,java.lang.Object[])
meth public !varargs java.io.PrintStream printf(java.util.Locale,java.lang.String,java.lang.Object[])
meth public java.io.PrintStream append(char)
meth public java.io.PrintStream append(java.lang.CharSequence)
meth public java.io.PrintStream append(java.lang.CharSequence,int,int)
meth public void print(boolean)
meth public void print(char)
meth public void print(char[])
meth public void print(double)
meth public void print(float)
meth public void print(int)
meth public void print(java.lang.Object)
meth public void print(java.lang.String)
meth public void print(long)
meth public void println()
meth public void println(boolean)
meth public void println(char)
meth public void println(char[])
meth public void println(double)
meth public void println(float)
meth public void println(int)
meth public void println(java.lang.Object)
meth public void println(java.lang.String)
meth public void println(long)
meth public void write(byte[],int,int)
meth public void write(int)
supr java.io.PrintStream

CLSS public abstract org.apache.sshd.common.util.io.output.OutputStreamWithChannel
cons protected init()
intf java.nio.channels.Channel
supr java.io.OutputStream

CLSS public final org.apache.sshd.common.util.io.output.SecureByteArrayOutputStream
cons public init()
cons public init(int)
meth public void close()
meth public void write(byte[],int,int)
meth public void write(int)
supr java.io.ByteArrayOutputStream

CLSS public abstract org.apache.sshd.common.util.io.resource.AbstractIoResource<%0 extends java.lang.Object>
cons protected init(java.lang.Class<{org.apache.sshd.common.util.io.resource.AbstractIoResource%0}>,{org.apache.sshd.common.util.io.resource.AbstractIoResource%0})
intf org.apache.sshd.common.util.io.resource.IoResource<{org.apache.sshd.common.util.io.resource.AbstractIoResource%0}>
meth public java.lang.Class<{org.apache.sshd.common.util.io.resource.AbstractIoResource%0}> getResourceType()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public {org.apache.sshd.common.util.io.resource.AbstractIoResource%0} getResourceValue()
supr java.lang.Object
hfds resourceType,resourceValue

CLSS public org.apache.sshd.common.util.io.resource.ClassLoaderResource
cons public init(java.lang.ClassLoader,java.lang.String)
meth public java.io.InputStream openInputStream() throws java.io.IOException
meth public java.lang.ClassLoader getResourceLoader()
meth public java.lang.String getName()
supr org.apache.sshd.common.util.io.resource.AbstractIoResource<java.lang.ClassLoader>
hfds resourceName

CLSS public abstract interface org.apache.sshd.common.util.io.resource.IoResource<%0 extends java.lang.Object>
intf org.apache.sshd.common.NamedResource
intf org.apache.sshd.common.util.io.resource.ResourceStreamProvider
meth public abstract java.lang.Class<{org.apache.sshd.common.util.io.resource.IoResource%0}> getResourceType()
meth public abstract {org.apache.sshd.common.util.io.resource.IoResource%0} getResourceValue()
meth public static org.apache.sshd.common.util.io.resource.IoResource<?> forResource(java.lang.Object)

CLSS public org.apache.sshd.common.util.io.resource.PathResource
cons public !varargs init(java.nio.file.Path,java.nio.file.OpenOption[])
cons public init(java.nio.file.Path)
meth public java.io.InputStream openInputStream() throws java.io.IOException
meth public java.nio.file.OpenOption[] getOpenOptions()
meth public java.nio.file.Path getPath()
supr org.apache.sshd.common.util.io.resource.AbstractIoResource<java.nio.file.Path>
hfds openOptions

CLSS public abstract interface org.apache.sshd.common.util.io.resource.ResourceStreamProvider
 anno 0 java.lang.FunctionalInterface()
meth public abstract java.io.InputStream openInputStream() throws java.io.IOException

CLSS public org.apache.sshd.common.util.io.resource.URIResource
cons public init(java.net.URI)
meth public java.io.InputStream openInputStream() throws java.io.IOException
meth public java.net.URI getURI()
supr org.apache.sshd.common.util.io.resource.AbstractIoResource<java.net.URI>

CLSS public org.apache.sshd.common.util.io.resource.URLResource
cons public init(java.net.URL)
meth public java.io.InputStream openInputStream() throws java.io.IOException
meth public java.lang.String getName()
meth public java.net.URL getURL()
supr org.apache.sshd.common.util.io.resource.AbstractIoResource<java.net.URL>

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

CLSS public abstract org.apache.sshd.common.util.logging.LoggerSkeleton
cons protected init(java.lang.String)
meth public !varargs void debug(java.lang.String,java.lang.Object[])
meth public !varargs void error(java.lang.String,java.lang.Object[])
meth public !varargs void info(java.lang.String,java.lang.Object[])
meth public !varargs void trace(java.lang.String,java.lang.Object[])
meth public !varargs void warn(java.lang.String,java.lang.Object[])
meth public void debug(java.lang.String)
meth public void debug(java.lang.String,java.lang.Object)
meth public void debug(java.lang.String,java.lang.Object,java.lang.Object)
meth public void error(java.lang.String)
meth public void error(java.lang.String,java.lang.Object)
meth public void error(java.lang.String,java.lang.Object,java.lang.Object)
meth public void info(java.lang.String)
meth public void info(java.lang.String,java.lang.Object)
meth public void info(java.lang.String,java.lang.Object,java.lang.Object)
meth public void trace(java.lang.String)
meth public void trace(java.lang.String,java.lang.Object)
meth public void trace(java.lang.String,java.lang.Object,java.lang.Object)
meth public void warn(java.lang.String)
meth public void warn(java.lang.String,java.lang.Object)
meth public void warn(java.lang.String,java.lang.Object,java.lang.Object)
supr org.slf4j.helpers.MarkerIgnoringBase
hfds serialVersionUID

CLSS public final org.apache.sshd.common.util.logging.LoggingUtils
fld public final static java.util.Set<org.slf4j.event.Level> SLF4J_LEVELS
meth public !varargs static java.lang.String formatMessage(java.lang.String,java.lang.Object[])
meth public static <%0 extends java.lang.Object> java.util.function.Consumer<{%%0}> debugClosure(org.slf4j.Logger)
meth public static <%0 extends java.lang.Object> java.util.function.Consumer<{%%0}> debugClosure(org.slf4j.Logger,java.lang.Throwable)
meth public static <%0 extends java.lang.Object> java.util.function.Consumer<{%%0}> errorClosure(org.slf4j.Logger)
meth public static <%0 extends java.lang.Object> java.util.function.Consumer<{%%0}> errorClosure(org.slf4j.Logger,java.lang.Throwable)
meth public static <%0 extends java.lang.Object> java.util.function.Consumer<{%%0}> infoClosure(org.slf4j.Logger)
meth public static <%0 extends java.lang.Object> java.util.function.Consumer<{%%0}> infoClosure(org.slf4j.Logger,java.lang.Throwable)
meth public static <%0 extends java.lang.Object> java.util.function.Consumer<{%%0}> loggingClosure(org.slf4j.Logger,java.util.logging.Level)
meth public static <%0 extends java.lang.Object> java.util.function.Consumer<{%%0}> loggingClosure(org.slf4j.Logger,java.util.logging.Level,java.lang.Throwable)
meth public static <%0 extends java.lang.Object> java.util.function.Consumer<{%%0}> nologClosure(org.slf4j.Logger)
meth public static <%0 extends java.lang.Object> java.util.function.Consumer<{%%0}> traceClosure(org.slf4j.Logger)
meth public static <%0 extends java.lang.Object> java.util.function.Consumer<{%%0}> traceClosure(org.slf4j.Logger,java.lang.Throwable)
meth public static <%0 extends java.lang.Object> java.util.function.Consumer<{%%0}> warnClosure(org.slf4j.Logger)
meth public static <%0 extends java.lang.Object> java.util.function.Consumer<{%%0}> warnClosure(org.slf4j.Logger,java.lang.Throwable)
meth public static boolean isLoggable(org.slf4j.Logger,java.util.logging.Level)
meth public static java.util.Collection<java.lang.reflect.Field> getMnemonicFields(java.lang.Class<?>,java.util.function.Predicate<? super java.lang.reflect.Field>)
meth public static java.util.Map<java.lang.String,java.lang.Integer> getAmbiguousMenmonics(java.lang.Class<?>,java.lang.String)
meth public static java.util.Map<java.lang.String,java.lang.Integer> getAmbiguousMenmonics(java.lang.Class<?>,java.util.function.Predicate<? super java.lang.reflect.Field>)
meth public static java.util.NavigableMap<java.lang.Integer,java.lang.String> generateMnemonicMap(java.lang.Class<?>,java.lang.String)
meth public static java.util.NavigableMap<java.lang.Integer,java.lang.String> generateMnemonicMap(java.lang.Class<?>,java.util.function.Predicate<? super java.lang.reflect.Field>)
meth public static org.apache.sshd.common.util.logging.SimplifiedLog wrap(org.slf4j.Logger)
meth public static org.slf4j.event.Level slf4jLevelFromName(java.lang.String)
meth public static void debug(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void debug(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void debug(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void debug(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void debug(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void error(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void error(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void error(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void error(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void error(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void info(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void info(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void logMessage(org.slf4j.Logger,java.util.logging.Level,java.lang.Object,java.lang.Throwable)
meth public static void warn(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void warn(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void warn(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void warn(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void warn(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void warn(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void warn(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Throwable)
meth public static void warn(org.slf4j.Logger,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Throwable)
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.common.util.logging.SimplifiedLog
fld public final static org.apache.sshd.common.util.logging.SimplifiedLog EMPTY
meth public abstract boolean isEnabledLevel(java.util.logging.Level)
meth public abstract void log(java.util.logging.Level,java.lang.Object,java.lang.Throwable)
meth public boolean isDebugEnabled()
meth public boolean isErrorEnabled()
meth public boolean isInfoEnabled()
meth public boolean isTraceEnabled()
meth public boolean isWarnEnabled()
meth public static boolean isDebugEnabled(java.util.logging.Level)
meth public static boolean isErrorEnabled(java.util.logging.Level)
meth public static boolean isInfoEnabled(java.util.logging.Level)
meth public static boolean isLoggable(java.util.logging.Level,java.util.logging.Level)
meth public static boolean isTraceEnabled(java.util.logging.Level)
meth public static boolean isWarnEnabled(java.util.logging.Level)
meth public void debug(java.lang.String)
meth public void debug(java.lang.String,java.lang.Throwable)
meth public void error(java.lang.String)
meth public void error(java.lang.String,java.lang.Throwable)
meth public void info(java.lang.String)
meth public void info(java.lang.String,java.lang.Throwable)
meth public void log(java.util.logging.Level,java.lang.Object)
meth public void trace(java.lang.String)
meth public void trace(java.lang.String,java.lang.Throwable)
meth public void warn(java.lang.String)
meth public void warn(java.lang.String,java.lang.Throwable)

CLSS public abstract org.apache.sshd.common.util.logging.SimplifiedLoggerSkeleton
cons protected init(java.lang.String)
fld public final static org.apache.sshd.common.util.logging.SimplifiedLoggerSkeleton EMPTY
intf org.apache.sshd.common.util.logging.SimplifiedLog
meth public boolean isDebugEnabled()
meth public boolean isErrorEnabled()
meth public boolean isInfoEnabled()
meth public boolean isTraceEnabled()
meth public boolean isWarnEnabled()
meth public void debug(java.lang.String,java.lang.Throwable)
meth public void error(java.lang.String,java.lang.Throwable)
meth public void info(java.lang.String,java.lang.Throwable)
meth public void trace(java.lang.String,java.lang.Throwable)
meth public void warn(java.lang.String,java.lang.Throwable)
supr org.apache.sshd.common.util.logging.LoggerSkeleton
hfds serialVersionUID

CLSS public abstract interface org.apache.sshd.common.util.net.ConnectionEndpointsIndicator
meth public abstract java.net.SocketAddress getLocalAddress()
meth public abstract java.net.SocketAddress getRemoteAddress()

CLSS public org.apache.sshd.common.util.net.NetworkConnector
cons public init()
fld public final static java.lang.String DEFAULT_HOST = "127.0.0.1"
fld public final static long DEFAULT_CONNECT_TIMEOUT
fld public final static long DEFAULT_READ_TIMEOUT
meth public int getPort()
meth public java.lang.String getHost()
meth public java.lang.String getProtocol()
meth public java.lang.String toString()
meth public long getConnectTimeout()
meth public long getReadTimeout()
meth public void setConnectTimeout(long)
meth public void setHost(java.lang.String)
meth public void setPort(int)
meth public void setProtocol(java.lang.String)
meth public void setReadTimeout(long)
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds connectTimeout,host,port,protocol,readTimeout

CLSS public org.apache.sshd.common.util.net.SshdSocketAddress
cons public init(int)
cons public init(java.lang.String,int)
cons public init(java.net.InetSocketAddress)
fld public final static int IPV6_MAX_HEX_DIGITS_PER_GROUP = 4
fld public final static int IPV6_MAX_HEX_GROUPS = 8
fld public final static java.lang.String BROADCAST_ADDRESS = "255.255.255.255"
fld public final static java.lang.String CARRIER_GRADE_NAT_PREFIX = "100."
fld public final static java.lang.String IPV4_ANYADDR = "0.0.0.0"
fld public final static java.lang.String IPV6_LONG_ANY_ADDRESS = "0:0:0:0:0:0:0:0"
fld public final static java.lang.String IPV6_LONG_LOCALHOST = "0:0:0:0:0:0:0:1"
fld public final static java.lang.String IPV6_SHORT_ANY_ADDRESS = "::"
fld public final static java.lang.String IPV6_SHORT_LOCALHOST = "::1"
fld public final static java.lang.String LOCALHOST_IPV4 = "127.0.0.1"
fld public final static java.lang.String LOCALHOST_NAME = "localhost"
fld public final static java.lang.String PRIVATE_CLASS_A_PREFIX = "10."
fld public final static java.lang.String PRIVATE_CLASS_B_PREFIX = "172."
fld public final static java.lang.String PRIVATE_CLASS_C_PREFIX = "192.168."
fld public final static java.util.Comparator<java.net.InetAddress> BY_HOST_ADDRESS
fld public final static java.util.Comparator<java.net.SocketAddress> BY_HOST_AND_PORT
fld public final static java.util.Set<java.lang.String> WELL_KNOWN_IPV4_ADDRESSES
fld public final static java.util.Set<java.lang.String> WELL_KNOWN_IPV6_ADDRESSES
fld public final static org.apache.sshd.common.util.net.SshdSocketAddress LOCALHOST_ADDRESS
meth protected boolean isEquivalent(org.apache.sshd.common.util.net.SshdSocketAddress)
meth public boolean equals(java.lang.Object)
meth public int getPort()
meth public int hashCode()
meth public java.lang.String getHostName()
meth public java.lang.String toString()
meth public java.net.InetSocketAddress toInetSocketAddress()
meth public static <%0 extends java.lang.Object> java.util.Map$Entry<org.apache.sshd.common.util.net.SshdSocketAddress,? extends {%%0}> findMatchingOptionalWildcardEntry(java.util.Map<org.apache.sshd.common.util.net.SshdSocketAddress,? extends {%%0}>,org.apache.sshd.common.util.net.SshdSocketAddress)
meth public static <%0 extends java.lang.Object> {%%0} findByOptionalWildcardAddress(java.util.Map<org.apache.sshd.common.util.net.SshdSocketAddress,? extends {%%0}>,org.apache.sshd.common.util.net.SshdSocketAddress)
meth public static <%0 extends java.lang.Object> {%%0} removeByOptionalWildcardAddress(java.util.Map<org.apache.sshd.common.util.net.SshdSocketAddress,? extends {%%0}>,org.apache.sshd.common.util.net.SshdSocketAddress)
meth public static boolean isCarrierGradeNatIPv4Address(java.lang.String)
meth public static boolean isEquivalentHostName(java.lang.String,java.lang.String,boolean)
meth public static boolean isIPv4Address(java.lang.String)
meth public static boolean isIPv4LoopbackAddress(java.lang.String)
meth public static boolean isIPv6Address(java.lang.String)
meth public static boolean isIPv6LoopbackAddress(java.lang.String)
meth public static boolean isLoopback(java.lang.String)
meth public static boolean isLoopback(java.net.InetAddress)
meth public static boolean isLoopbackAlias(java.lang.String,java.lang.String)
meth public static boolean isPrivateIPv4Address(java.lang.String)
meth public static boolean isValidHostAddress(java.net.InetAddress)
meth public static boolean isValidIPv4AddressComponent(java.lang.CharSequence)
meth public static boolean isWildcardAddress(java.lang.String)
meth public static int toAddressPort(java.net.SocketAddress)
meth public static java.lang.String toAddressString(java.net.InetAddress)
meth public static java.lang.String toAddressString(java.net.SocketAddress)
meth public static java.net.InetAddress getFirstExternalNetwork4Address()
meth public static java.net.InetSocketAddress toInetSocketAddress(java.net.SocketAddress)
meth public static java.util.List<java.net.InetAddress> getExternalNetwork4Addresses()
meth public static org.apache.sshd.common.util.net.SshdSocketAddress toSshdSocketAddress(java.net.SocketAddress)
supr java.net.SocketAddress
hfds hostName,port,serialVersionUID

CLSS public abstract org.apache.sshd.common.util.security.AbstractSecurityProviderRegistrar
cons protected init(java.lang.String)
fld protected final java.util.Map<java.lang.Class<?>,java.util.Map<java.lang.String,java.lang.Boolean>> supportedEntities
fld protected final java.util.Map<java.lang.String,java.lang.Object> props
fld protected final java.util.concurrent.atomic.AtomicReference<java.security.Provider> providerHolder
intf org.apache.sshd.common.util.security.SecurityProviderRegistrar
meth protected java.security.Provider createProviderInstance(java.lang.String) throws java.lang.ReflectiveOperationException
meth protected java.security.Provider getOrCreateProvider(java.lang.String) throws java.lang.ReflectiveOperationException
meth public boolean isSecurityEntitySupported(java.lang.Class<?>,java.lang.String)
meth public final java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds name

CLSS public abstract interface org.apache.sshd.common.util.security.Decryptor
meth public abstract byte[] decrypt(byte[],char[]) throws java.security.GeneralSecurityException

CLSS public abstract interface org.apache.sshd.common.util.security.SecurityEntityFactory<%0 extends java.lang.Object>
meth public abstract java.lang.Class<{org.apache.sshd.common.util.security.SecurityEntityFactory%0}> getEntityType()
meth public abstract {org.apache.sshd.common.util.security.SecurityEntityFactory%0} getInstance(java.lang.String) throws java.security.GeneralSecurityException
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.util.security.SecurityEntityFactory<{%%0}> toDefaultFactory(java.lang.Class<{%%0}>) throws java.lang.ReflectiveOperationException
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.util.security.SecurityEntityFactory<{%%0}> toFactory(java.lang.Class<{%%0}>,org.apache.sshd.common.util.security.SecurityProviderChoice,org.apache.sshd.common.util.security.SecurityProviderChoice) throws java.lang.ReflectiveOperationException
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.util.security.SecurityEntityFactory<{%%0}> toNamedProviderFactory(java.lang.Class<{%%0}>,java.lang.String) throws java.lang.ReflectiveOperationException
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.util.security.SecurityEntityFactory<{%%0}> toProviderInstanceFactory(java.lang.Class<{%%0}>,java.security.Provider) throws java.lang.ReflectiveOperationException

CLSS public abstract interface org.apache.sshd.common.util.security.SecurityProviderChoice
fld public final static org.apache.sshd.common.util.security.SecurityProviderChoice EMPTY
intf org.apache.sshd.common.NamedResource
meth public abstract java.security.Provider getSecurityProvider()
meth public boolean isNamedProviderUsed()
meth public java.lang.String getProviderName()
meth public static java.security.Provider createProviderInstance(java.lang.Class<?>,java.lang.String) throws java.lang.ReflectiveOperationException
meth public static org.apache.sshd.common.util.security.SecurityProviderChoice toSecurityProviderChoice(java.lang.String)
meth public static org.apache.sshd.common.util.security.SecurityProviderChoice toSecurityProviderChoice(java.security.Provider)

CLSS public abstract interface org.apache.sshd.common.util.security.SecurityProviderRegistrar
fld public final static java.lang.String ALL_OPTIONS_VALUE = "all"
fld public final static java.lang.String ALL_OPTIONS_WILDCARD = "*"
fld public final static java.lang.String CONFIG_PROP_BASE = "org.apache.sshd.security.provider"
fld public final static java.lang.String ENABLED_PROPERTY = "enabled"
fld public final static java.lang.String NAMED_PROVIDER_PROPERTY = "useNamed"
fld public final static java.lang.String NO_OPTIONS_VALUE = "none"
fld public final static java.util.List<java.lang.Class<?>> SECURITY_ENTITIES
intf org.apache.sshd.common.OptionalFeature
intf org.apache.sshd.common.PropertyResolver
intf org.apache.sshd.common.util.security.SecurityProviderChoice
meth public boolean isCertificateFactorySupported(java.lang.String)
meth public boolean isCipherSupported(java.lang.String)
meth public boolean isEnabled()
meth public boolean isKeyAgreementSupported(java.lang.String)
meth public boolean isKeyFactorySupported(java.lang.String)
meth public boolean isKeyPairGeneratorSupported(java.lang.String)
meth public boolean isMacSupported(java.lang.String)
meth public boolean isMessageDigestSupported(java.lang.String)
meth public boolean isNamedProviderUsed()
meth public boolean isSecurityEntitySupported(java.lang.Class<?>,java.lang.String)
meth public boolean isSignatureSupported(java.lang.String)
meth public java.lang.String getBasePropertyName()
meth public java.lang.String getConfigurationPropertyName(java.lang.String)
meth public java.lang.String getDefaultSecurityEntitySupportValue(java.lang.Class<?>)
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public java.util.Optional<org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport<?,?>> getEdDSASupport()
meth public org.apache.sshd.common.PropertyResolver getParentPropertyResolver()
meth public static boolean isAllOptionsValue(java.lang.String)
meth public static boolean isSecurityEntitySupported(org.apache.sshd.common.PropertyResolver,java.lang.String,java.lang.Class<?>,java.lang.String,java.lang.String)
meth public static boolean isSecurityEntitySupported(org.apache.sshd.common.util.security.SecurityProviderRegistrar,java.lang.Class<?>,java.lang.String,java.lang.String)
meth public static boolean registerSecurityProvider(org.apache.sshd.common.util.security.SecurityProviderRegistrar)
meth public static java.lang.String getEffectiveSecurityEntityName(java.lang.Class<?>,java.lang.String)
meth public static org.apache.sshd.common.util.security.SecurityProviderRegistrar findSecurityProviderRegistrarBySecurityEntity(java.util.function.Predicate<? super org.apache.sshd.common.util.security.SecurityProviderRegistrar>,java.util.Collection<? extends org.apache.sshd.common.util.security.SecurityProviderRegistrar>)

CLSS public final org.apache.sshd.common.util.security.SecurityUtils
fld public final static int MAX_DHGEX_KEY_SIZE = 8192
fld public final static int MIN_DHGEX_KEY_SIZE = 2048
fld public final static int PREFERRED_DHGEX_KEY_SIZE = 4096
fld public final static java.lang.String BOUNCY_CASTLE = "BC"
fld public final static java.lang.String CURVE_ED25519_SHA512 = "NONEwithEdDSA"
fld public final static java.lang.String ECC_SUPPORTED_PROP = "org.apache.sshd.eccSupport"
fld public final static java.lang.String ED25519 = "Ed25519"
fld public final static java.lang.String EDDSA = "EdDSA"
fld public final static java.lang.String EDDSA_SUPPORTED_PROP = "org.apache.sshd.eddsaSupport"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String FIPS_ENABLED = "org.apache.sshd.security.fipsEnabled"
fld public final static java.lang.String MAX_DHGEX_KEY_SIZE_PROP = "org.apache.sshd.maxDHGexKeySize"
fld public final static java.lang.String MIN_DHGEX_KEY_SIZE_PROP = "org.apache.sshd.minDHGexKeySize"
fld public final static java.lang.String PROP_DEFAULT_SECURITY_PROVIDER = "org.apache.sshd.security.defaultProvider"
fld public final static java.lang.String REGISTER_BOUNCY_CASTLE_PROP = "org.apache.sshd.registerBouncyCastle"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String SECURITY_PROVIDER_REGISTRARS = "org.apache.sshd.security.registrars"
fld public final static java.util.List<java.lang.String> DEFAULT_SECURITY_PROVIDER_REGISTRARS
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.util.security.SecurityEntityFactory<{%%0}> createSecurityEntityFactory(java.lang.Class<{%%0}>,java.util.function.Predicate<? super org.apache.sshd.common.util.security.SecurityProviderRegistrar>)
meth public static <%0 extends java.lang.Object> org.apache.sshd.common.util.security.SecurityEntityFactory<{%%0}> resolveSecurityEntityFactory(java.lang.Class<{%%0}>,java.lang.String,java.util.function.Predicate<? super org.apache.sshd.common.util.security.SecurityProviderRegistrar>)
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} putEDDSAKeyPair({%%0},java.security.KeyPair)
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} putEDDSAKeyPair({%%0},java.security.PublicKey,java.security.PrivateKey)
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} putRawEDDSAPublicKey({%%0},java.security.PublicKey)
meth public static boolean compareEDDSAPPublicKeys(java.security.PublicKey,java.security.PublicKey)
meth public static boolean compareEDDSAPrivateKeys(java.security.PrivateKey,java.security.PrivateKey)
meth public static boolean isAPrioriDisabledProvider(java.lang.String)
meth public static boolean isBouncyCastleRegistered()
meth public static boolean isDHGroupExchangeSupported()
meth public static boolean isDHGroupExchangeSupported(int)
meth public static boolean isDHOakelyGroupSupported(int)
meth public static boolean isECCSupported()
meth public static boolean isEDDSACurveSupported()
meth public static boolean isFipsMode()
meth public static boolean isNetI2pCryptoEdDSARegistered()
meth public static boolean isProviderRegistered(java.lang.String)
meth public static boolean isRegistrationCompleted()
meth public static int getEDDSAKeySize(java.security.Key)
meth public static int getMaxDHGroupExchangeKeySize()
meth public static int getMinDHGroupExchangeKeySize()
meth public static java.lang.Class<? extends java.security.PrivateKey> getEDDSAPrivateKeyType()
meth public static java.lang.Class<? extends java.security.PublicKey> getEDDSAPublicKeyType()
meth public static java.lang.Iterable<java.security.KeyPair> loadKeyPairIdentities(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.io.InputStream,org.apache.sshd.common.config.keys.FilePasswordProvider) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.security.AlgorithmParameters getAlgorithmParameters(java.lang.String) throws java.security.GeneralSecurityException
meth public static java.security.KeyFactory getKeyFactory(java.lang.String) throws java.security.GeneralSecurityException
meth public static java.security.KeyPair extractEDDSAKeyPair(org.apache.sshd.common.util.buffer.Buffer,java.lang.String) throws java.security.GeneralSecurityException
meth public static java.security.KeyPairGenerator getKeyPairGenerator(java.lang.String) throws java.security.GeneralSecurityException
meth public static java.security.MessageDigest getMessageDigest(java.lang.String) throws java.security.GeneralSecurityException
meth public static java.security.PrivateKey generateEDDSAPrivateKey(java.lang.String,byte[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.security.PublicKey generateEDDSAPublicKey(java.lang.String,byte[]) throws java.security.GeneralSecurityException
meth public static java.security.PublicKey recoverEDDSAPublicKey(java.security.PrivateKey) throws java.security.GeneralSecurityException
meth public static java.security.Signature getSignature(java.lang.String) throws java.security.GeneralSecurityException
meth public static java.security.cert.CertificateFactory getCertificateFactory(java.lang.String) throws java.security.GeneralSecurityException
meth public static java.util.Optional<org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport<?,?>> getEdDSASupport()
meth public static java.util.Set<java.lang.String> getAPrioriDisabledProviders()
meth public static java.util.Set<java.lang.String> getRegisteredProviders()
meth public static javax.crypto.Cipher getCipher(java.lang.String) throws java.security.GeneralSecurityException
meth public static javax.crypto.KeyAgreement getKeyAgreement(java.lang.String) throws java.security.GeneralSecurityException
meth public static javax.crypto.Mac getMac(java.lang.String) throws java.security.GeneralSecurityException
meth public static org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder<? extends java.security.PublicKey,? extends java.security.PrivateKey> getOpenSSHEDDSAPrivateKeyEntryDecoder()
meth public static org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<? extends java.security.PublicKey,? extends java.security.PrivateKey> getEDDSAPublicKeyEntryDecoder()
meth public static org.apache.sshd.common.config.keys.loader.KeyPairResourceParser getBouncycastleKeyPairResourceParser()
meth public static org.apache.sshd.common.config.keys.loader.KeyPairResourceParser getKeyPairResourceParser()
meth public static org.apache.sshd.common.random.RandomFactory getRandomFactory()
meth public static org.apache.sshd.common.signature.Signature getEDDSASigner()
meth public static org.apache.sshd.common.util.security.Decryptor getBouncycastleEncryptedPrivateKeyInfoDecryptor()
meth public static org.apache.sshd.common.util.security.SecurityProviderChoice getDefaultProviderChoice()
meth public static org.apache.sshd.common.util.security.SecurityProviderRegistrar getRegisteredProvider(java.lang.String)
meth public static org.apache.sshd.common.util.security.SecurityProviderRegistrar registerSecurityProvider(org.apache.sshd.common.util.security.SecurityProviderRegistrar)
meth public static org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider createGeneratorHostKeyProvider(java.nio.file.Path)
meth public static void setAPrioriDisabledProvider(java.lang.String,boolean)
meth public static void setDefaultProviderChoice(org.apache.sshd.common.util.security.SecurityProviderChoice)
meth public static void setFipsMode()
meth public static void setKeyPairResourceParser(org.apache.sshd.common.config.keys.loader.KeyPairResourceParser)
meth public static void setMaxDHGroupExchangeKeySize(int)
meth public static void setMinDHGroupExchangeKeySize(int)
supr java.lang.Object
hfds APRIORI_DISABLED_PROVIDERS,DEFAULT_PROVIDER_HOLDER,FIPS_MODE,KEYPAIRS_PARSER_HODLER,MAX_DHG_KEY_SIZE_HOLDER,MIN_DHG_KEY_SIZE_HOLDER,REGISTERED_PROVIDERS,REGISTRATION_STATE_HOLDER,SECURITY_ENTITY_FACTORIES,hasEcc

CLSS public org.apache.sshd.common.util.security.SunJCESecurityProviderRegistrar
cons public init()
meth public boolean isEnabled()
meth public boolean isNamedProviderUsed()
meth public boolean isSupported()
meth public java.lang.String getDefaultSecurityEntitySupportValue(java.lang.Class<?>)
meth public java.lang.String getProviderName()
meth public java.lang.String getString(java.lang.String)
meth public java.security.Provider getSecurityProvider()
supr org.apache.sshd.common.util.security.AbstractSecurityProviderRegistrar
hfds defaultProperties

CLSS public final !enum org.apache.sshd.common.util.security.bouncycastle.BouncyCastleEncryptedPrivateKeyInfoDecryptor
fld public final static org.apache.sshd.common.util.security.bouncycastle.BouncyCastleEncryptedPrivateKeyInfoDecryptor INSTANCE
intf org.apache.sshd.common.util.security.Decryptor
meth public byte[] decrypt(byte[],char[]) throws java.security.GeneralSecurityException
meth public static org.apache.sshd.common.util.security.bouncycastle.BouncyCastleEncryptedPrivateKeyInfoDecryptor valueOf(java.lang.String)
meth public static org.apache.sshd.common.util.security.bouncycastle.BouncyCastleEncryptedPrivateKeyInfoDecryptor[] values()
supr java.lang.Enum<org.apache.sshd.common.util.security.bouncycastle.BouncyCastleEncryptedPrivateKeyInfoDecryptor>

CLSS public org.apache.sshd.common.util.security.bouncycastle.BouncyCastleGeneratorHostKeyProvider
cons public init(java.nio.file.Path)
meth protected void doWriteKeyPair(org.apache.sshd.common.NamedResource,java.security.KeyPair,java.io.OutputStream) throws java.io.IOException,java.security.GeneralSecurityException
meth public !varargs static void writePEMKeyPair(java.security.KeyPair,java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
meth public static void writePEMKeyPair(java.security.KeyPair,java.io.OutputStream) throws java.io.IOException
meth public static void writePEMKeyPair(java.security.KeyPair,java.nio.file.Path) throws java.io.IOException
supr org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider

CLSS public org.apache.sshd.common.util.security.bouncycastle.BouncyCastleKeyPairResourceParser
cons public init()
fld public final static java.util.List<java.lang.String> BEGINNERS
fld public final static java.util.List<java.lang.String> ENDERS
fld public final static org.apache.sshd.common.util.security.bouncycastle.BouncyCastleKeyPairResourceParser INSTANCE
meth public java.util.Collection<java.security.KeyPair> extractKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public java.util.Collection<java.security.KeyPair> extractKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.util.List<java.lang.String>,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.security.KeyPair loadKeyPair(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.io.InputStream,org.apache.sshd.common.config.keys.FilePasswordProvider) throws java.io.IOException,java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.loader.AbstractKeyPairResourceParser

CLSS public final org.apache.sshd.common.util.security.bouncycastle.BouncyCastleRandom
cons public init()
fld public final static java.lang.String NAME = "BC"
meth public int random(int)
meth public java.lang.String getName()
meth public void fill(byte[],int,int)
supr org.apache.sshd.common.random.AbstractRandom
hfds random

CLSS public final org.apache.sshd.common.util.security.bouncycastle.BouncyCastleRandomFactory
cons public init()
fld public final static java.lang.String NAME = "bouncycastle"
fld public final static org.apache.sshd.common.util.security.bouncycastle.BouncyCastleRandomFactory INSTANCE
meth public boolean isSupported()
meth public org.apache.sshd.common.random.Random create()
supr org.apache.sshd.common.random.AbstractRandomFactory

CLSS public org.apache.sshd.common.util.security.bouncycastle.BouncyCastleSecurityProviderRegistrar
cons public init()
fld public final static java.lang.String FIPS_PROVIDER_CLASS = "org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider"
fld public final static java.lang.String PROVIDER_CLASS = "org.bouncycastle.jce.provider.BouncyCastleProvider"
meth public boolean isEnabled()
meth public boolean isSecurityEntitySupported(java.lang.Class<?>,java.lang.String)
meth public boolean isSupported()
meth public java.lang.String getDefaultSecurityEntitySupportValue(java.lang.Class<?>)
meth public java.lang.String getProviderName()
meth public java.security.Provider getSecurityProvider()
meth public java.util.Optional<org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport<?,?>> getEdDSASupport()
supr org.apache.sshd.common.util.security.AbstractSecurityProviderRegistrar
hfds BCFIPS_PROVIDER_NAME,BC_PROVIDER_NAME,EDDSA_KEY_CLASS_NAME,NAME_FIELD,allSupportHolder,edDSASupportHolder,providerClass,providerName,supportHolder

CLSS public org.apache.sshd.common.util.security.eddsa.Ed25519PEMResourceKeyParser
cons public init()
fld public final static org.apache.sshd.common.util.security.eddsa.Ed25519PEMResourceKeyParser INSTANCE
meth public static net.i2p.crypto.eddsa.EdDSAPrivateKey decodeEdDSAPrivateKey(byte[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public static net.i2p.crypto.eddsa.EdDSAPrivateKey generateEdDSAPrivateKey(byte[]) throws java.security.GeneralSecurityException
supr org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PEMResourceKeyParser

CLSS public final org.apache.sshd.common.util.security.eddsa.Ed25519PublicKeyDecoder
fld public final static org.apache.sshd.common.util.security.eddsa.Ed25519PublicKeyDecoder INSTANCE
meth public static byte[] getSeedValue(net.i2p.crypto.eddsa.EdDSAPublicKey)
supr org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PublicKeyDecoder<net.i2p.crypto.eddsa.EdDSAPublicKey,net.i2p.crypto.eddsa.EdDSAPrivateKey>

CLSS public org.apache.sshd.common.util.security.eddsa.EdDSASecurityProviderRegistrar
cons public init()
fld public final static java.lang.String PROVIDER_CLASS = "net.i2p.crypto.eddsa.EdDSASecurityProvider"
meth public boolean isEnabled()
meth public boolean isSecurityEntitySupported(java.lang.Class<?>,java.lang.String)
meth public boolean isSupported()
meth public java.security.Provider getSecurityProvider()
meth public java.util.Optional<org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport<?,?>> getEdDSASupport()
supr org.apache.sshd.common.util.security.AbstractSecurityProviderRegistrar
hfds supportHolder

CLSS public final org.apache.sshd.common.util.security.eddsa.EdDSASecurityProviderUtils
fld public final static int KEY_SIZE = 256
fld public final static java.lang.String CURVE_ED25519_SHA512 = "Ed25519"
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} putEDDSAKeyPair({%%0},java.security.PublicKey,java.security.PrivateKey)
meth public static <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} putRawEDDSAPublicKey({%%0},java.security.PublicKey)
meth public static boolean compareEDDSAKeyParams(net.i2p.crypto.eddsa.spec.EdDSAParameterSpec,net.i2p.crypto.eddsa.spec.EdDSAParameterSpec)
meth public static boolean compareEDDSAPPublicKeys(java.security.PublicKey,java.security.PublicKey)
meth public static boolean compareEDDSAPrivateKeys(java.security.PrivateKey,java.security.PrivateKey)
meth public static boolean isEDDSAKey(java.security.Key)
meth public static boolean isEDDSAKeyFactoryAlgorithm(java.lang.String)
meth public static boolean isEDDSAKeyPairGeneratorAlgorithm(java.lang.String)
meth public static boolean isEDDSASignatureAlgorithm(java.lang.String)
meth public static int getEDDSAKeySize(java.security.Key)
meth public static java.security.PrivateKey generateEDDSAPrivateKey(byte[]) throws java.security.GeneralSecurityException
meth public static java.security.PublicKey generateEDDSAPublicKey(byte[]) throws java.security.GeneralSecurityException
meth public static net.i2p.crypto.eddsa.EdDSAPublicKey recoverEDDSAPublicKey(java.security.PrivateKey) throws java.security.GeneralSecurityException
meth public static org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder<? extends java.security.PublicKey,? extends java.security.PrivateKey> getOpenSSHEDDSAPrivateKeyEntryDecoder()
meth public static org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<? extends java.security.PublicKey,? extends java.security.PrivateKey> getEDDSAPublicKeyEntryDecoder()
meth public static org.apache.sshd.common.signature.Signature getEDDSASignature()
supr java.lang.Object

CLSS public org.apache.sshd.common.util.security.eddsa.NetI2pCryptoEdDSASupport
cons public init()
intf org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport<net.i2p.crypto.eddsa.EdDSAPublicKey,net.i2p.crypto.eddsa.EdDSAPrivateKey>
meth public <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} putEDDSAKeyPair({%%0},java.security.PublicKey,java.security.PrivateKey)
meth public <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} putRawEDDSAPublicKey({%%0},java.security.PublicKey)
meth public boolean compareEDDSAPPublicKeys(java.security.PublicKey,java.security.PublicKey)
meth public boolean compareEDDSAPrivateKeys(java.security.PrivateKey,java.security.PrivateKey)
meth public byte[] getPrivateKeyData(net.i2p.crypto.eddsa.EdDSAPrivateKey) throws java.io.IOException
meth public byte[] getPublicKeyData(net.i2p.crypto.eddsa.EdDSAPublicKey)
meth public int getEDDSAKeySize(java.security.Key)
meth public java.lang.Class<net.i2p.crypto.eddsa.EdDSAPrivateKey> getEDDSAPrivateKeyType()
meth public java.lang.Class<net.i2p.crypto.eddsa.EdDSAPublicKey> getEDDSAPublicKeyType()
meth public java.lang.String getKeyFactoryAlgorithm()
meth public java.security.spec.KeySpec createPrivateKeySpec(net.i2p.crypto.eddsa.EdDSAPrivateKey)
meth public java.security.spec.KeySpec createPublicKeySpec(net.i2p.crypto.eddsa.EdDSAPublicKey)
meth public net.i2p.crypto.eddsa.EdDSAPrivateKey generateEDDSAPrivateKey(byte[]) throws java.security.GeneralSecurityException
meth public net.i2p.crypto.eddsa.EdDSAPublicKey generateEDDSAPublicKey(byte[]) throws java.security.GeneralSecurityException
meth public net.i2p.crypto.eddsa.EdDSAPublicKey recoverEDDSAPublicKey(java.security.PrivateKey) throws java.security.GeneralSecurityException
meth public org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder<net.i2p.crypto.eddsa.EdDSAPublicKey,net.i2p.crypto.eddsa.EdDSAPrivateKey> getOpenSSHEDDSAPrivateKeyEntryDecoder()
meth public org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<net.i2p.crypto.eddsa.EdDSAPublicKey,net.i2p.crypto.eddsa.EdDSAPrivateKey> getEDDSAPublicKeyEntryDecoder()
meth public org.apache.sshd.common.signature.Signature getEDDSASigner()
supr java.lang.Object

CLSS public org.apache.sshd.common.util.security.eddsa.OpenSSHEd25519PrivateKeyEntryDecoder
cons public init()
fld public final static org.apache.sshd.common.util.security.eddsa.OpenSSHEd25519PrivateKeyEntryDecoder INSTANCE
supr org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder<net.i2p.crypto.eddsa.EdDSAPublicKey,net.i2p.crypto.eddsa.EdDSAPrivateKey>

CLSS public org.apache.sshd.common.util.security.eddsa.SignatureEd25519
cons public init()
meth protected boolean doVerify(byte[]) throws java.security.SignatureException
supr org.apache.sshd.common.util.security.eddsa.generic.GenericSignatureEd25519
hfds ED25519_ORDER

CLSS public org.apache.sshd.common.util.security.eddsa.bouncycastle.BouncyCastleEdDSASupport
cons public init()
intf org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport<org.bouncycastle.jcajce.interfaces.EdDSAPublicKey,org.bouncycastle.jcajce.interfaces.EdDSAPrivateKey>
meth public <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} putEDDSAKeyPair({%%0},java.security.PublicKey,java.security.PrivateKey)
meth public <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} putRawEDDSAPublicKey({%%0},java.security.PublicKey)
meth public boolean compareEDDSAPPublicKeys(java.security.PublicKey,java.security.PublicKey)
meth public boolean compareEDDSAPrivateKeys(java.security.PrivateKey,java.security.PrivateKey)
meth public byte[] getPrivateKeyData(org.bouncycastle.jcajce.interfaces.EdDSAPrivateKey) throws java.io.IOException
meth public byte[] getPublicKeyData(org.bouncycastle.jcajce.interfaces.EdDSAPublicKey)
meth public int getEDDSAKeySize(java.security.Key)
meth public java.lang.Class<org.bouncycastle.jcajce.interfaces.EdDSAPrivateKey> getEDDSAPrivateKeyType()
meth public java.lang.Class<org.bouncycastle.jcajce.interfaces.EdDSAPublicKey> getEDDSAPublicKeyType()
meth public java.lang.String getKeyFactoryAlgorithm()
meth public java.security.spec.KeySpec createPrivateKeySpec(org.bouncycastle.jcajce.interfaces.EdDSAPrivateKey)
meth public java.security.spec.KeySpec createPublicKeySpec(org.bouncycastle.jcajce.interfaces.EdDSAPublicKey)
meth public org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder<org.bouncycastle.jcajce.interfaces.EdDSAPublicKey,org.bouncycastle.jcajce.interfaces.EdDSAPrivateKey> getOpenSSHEDDSAPrivateKeyEntryDecoder()
meth public org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<org.bouncycastle.jcajce.interfaces.EdDSAPublicKey,org.bouncycastle.jcajce.interfaces.EdDSAPrivateKey> getEDDSAPublicKeyEntryDecoder()
meth public org.apache.sshd.common.signature.Signature getEDDSASigner()
meth public org.bouncycastle.jcajce.interfaces.EdDSAPrivateKey generateEDDSAPrivateKey(byte[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public org.bouncycastle.jcajce.interfaces.EdDSAPublicKey generateEDDSAPublicKey(byte[]) throws java.security.GeneralSecurityException
meth public org.bouncycastle.jcajce.interfaces.EdDSAPublicKey recoverEDDSAPublicKey(java.security.PrivateKey) throws java.security.GeneralSecurityException
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport<%0 extends java.security.PublicKey, %1 extends java.security.PrivateKey>
fld public final static int KEY_SIZE = 256
fld public final static java.lang.String ED25519_OID = "1.3.101.112"
meth public abstract <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} putEDDSAKeyPair({%%0},java.security.PublicKey,java.security.PrivateKey)
meth public abstract <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} putRawEDDSAPublicKey({%%0},java.security.PublicKey)
meth public abstract boolean compareEDDSAPPublicKeys(java.security.PublicKey,java.security.PublicKey)
meth public abstract boolean compareEDDSAPrivateKeys(java.security.PrivateKey,java.security.PrivateKey)
meth public abstract byte[] getPrivateKeyData({org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport%1}) throws java.io.IOException
meth public abstract byte[] getPublicKeyData({org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport%0})
meth public abstract int getEDDSAKeySize(java.security.Key)
meth public abstract java.lang.Class<{org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport%0}> getEDDSAPublicKeyType()
meth public abstract java.lang.Class<{org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport%1}> getEDDSAPrivateKeyType()
meth public abstract java.lang.String getKeyFactoryAlgorithm()
meth public abstract java.security.spec.KeySpec createPrivateKeySpec({org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport%1})
meth public abstract java.security.spec.KeySpec createPublicKeySpec({org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport%0})
meth public abstract org.apache.sshd.common.config.keys.PrivateKeyEntryDecoder<{org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport%0},{org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport%1}> getOpenSSHEDDSAPrivateKeyEntryDecoder()
meth public abstract org.apache.sshd.common.config.keys.PublicKeyEntryDecoder<{org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport%0},{org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport%1}> getEDDSAPublicKeyEntryDecoder()
meth public abstract org.apache.sshd.common.signature.Signature getEDDSASigner()
meth public abstract {org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport%0} generateEDDSAPublicKey(byte[]) throws java.security.GeneralSecurityException
meth public abstract {org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport%0} recoverEDDSAPublicKey(java.security.PrivateKey) throws java.security.GeneralSecurityException
meth public abstract {org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport%1} generateEDDSAPrivateKey(byte[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.security.KeyPair decodeEd25519KeyPair(byte[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.security.PrivateKey decodeEdDSAPrivateKey(byte[]) throws java.io.IOException,java.security.GeneralSecurityException

CLSS public final org.apache.sshd.common.util.security.eddsa.generic.EdDSAUtils
meth public static byte[] getBytes(java.security.PrivateKey) throws java.security.InvalidKeyException
meth public static byte[] getBytes(java.security.PublicKey) throws java.security.InvalidKeyException
meth public static java.security.spec.KeySpec createPrivateKeySpec(byte[]) throws java.security.InvalidKeyException
meth public static java.security.spec.KeySpec createPublicKeySpec(byte[]) throws java.security.InvalidKeyException
supr java.lang.Object
hfds ED25519_LENGTH,ED25519_OID,ED25519_PKCS8_PREFIX,ED25519_X509_PREFIX,ED448_LENGTH,ED448_OID,ED448_PKCS8_PREFIX,ED448_X509_PREFIX

CLSS public org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PEMResourceKeyParser
cons public init()
fld public final static java.lang.String BEGIN_ED25519_MARKER = "BEGIN ED25519 PRIVATE KEY"
fld public final static java.lang.String BEGIN_MARKER = "BEGIN EDDSA PRIVATE KEY"
fld public final static java.lang.String ED25519_OID = "1.3.101.112"
fld public final static java.lang.String END_ED25519_MARKER = "END ED25519 PRIVATE KEY"
fld public final static java.lang.String END_MARKER = "END EDDSA PRIVATE KEY"
fld public final static java.util.List<java.lang.String> BEGINNERS
fld public final static java.util.List<java.lang.String> ENDERS
fld public final static org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PEMResourceKeyParser INSTANCE
meth public java.util.Collection<java.security.KeyPair> extractKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.lang.String,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.security.KeyPair decodeEd25519KeyPair(byte[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.security.KeyPair parseEd25519KeyPair(java.io.InputStream,boolean) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.security.KeyPair parseEd25519KeyPair(org.apache.sshd.common.util.io.der.DERParser) throws java.io.IOException,java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.loader.pem.AbstractPEMResourceKeyPairParser

CLSS public org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PublicKeyDecoder<%0 extends java.security.PublicKey, %1 extends java.security.PrivateKey>
cons public init(java.lang.Class<{org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PublicKeyDecoder%0}>,java.lang.Class<{org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PublicKeyDecoder%1}>,org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport<{org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PublicKeyDecoder%0},{org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PublicKeyDecoder%1}>)
fld protected final org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport<{org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PublicKeyDecoder%0},{org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PublicKeyDecoder%1}> edDSASupport
fld public final static int MAX_ALLOWED_SEED_LEN = 1024
meth public java.lang.String encodePublicKey(java.io.OutputStream,{org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PublicKeyDecoder%0}) throws java.io.IOException
meth public java.security.KeyFactory getKeyFactoryInstance() throws java.security.GeneralSecurityException
meth public java.security.KeyPairGenerator getKeyPairGenerator() throws java.security.GeneralSecurityException
meth public {org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PublicKeyDecoder%0} clonePublicKey({org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PublicKeyDecoder%0}) throws java.security.GeneralSecurityException
meth public {org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PublicKeyDecoder%0} decodePublicKey(org.apache.sshd.common.session.SessionContext,java.lang.String,java.io.InputStream,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException,java.security.GeneralSecurityException
meth public {org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PublicKeyDecoder%1} clonePrivateKey({org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PublicKeyDecoder%1}) throws java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.impl.AbstractPublicKeyEntryDecoder<{org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PublicKeyDecoder%0},{org.apache.sshd.common.util.security.eddsa.generic.GenericEd25519PublicKeyDecoder%1}>

CLSS public org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder<%0 extends java.security.PublicKey, %1 extends java.security.PrivateKey>
cons public init(java.lang.Class<{org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%0}>,java.lang.Class<{org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%1}>,org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport<{org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%0},{org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%1}>)
fld protected final org.apache.sshd.common.util.security.eddsa.generic.EdDSASupport<{org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%0},{org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%1}> edDSASupport
meth public boolean isPublicKeyRecoverySupported()
meth public java.lang.String encodePrivateKey(org.apache.sshd.common.util.io.output.SecureByteArrayOutputStream,{org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%1},{org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%0}) throws java.io.IOException
meth public java.security.KeyFactory getKeyFactoryInstance() throws java.security.GeneralSecurityException
meth public java.security.KeyPairGenerator getKeyPairGenerator() throws java.security.GeneralSecurityException
meth public {org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%0} clonePublicKey({org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%0}) throws java.security.GeneralSecurityException
meth public {org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%0} recoverPublicKey({org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%1}) throws java.security.GeneralSecurityException
meth public {org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%1} clonePrivateKey({org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%1}) throws java.security.GeneralSecurityException
meth public {org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%1} decodePrivateKey(org.apache.sshd.common.session.SessionContext,java.lang.String,org.apache.sshd.common.config.keys.FilePasswordProvider,java.io.InputStream) throws java.io.IOException,java.security.GeneralSecurityException
supr org.apache.sshd.common.config.keys.impl.AbstractPrivateKeyEntryDecoder<{org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%0},{org.apache.sshd.common.util.security.eddsa.generic.GenericOpenSSHEd25519PrivateKeyEntryDecoder%1}>
hfds KEYPAIR_SIZE,PK_SIZE,SK_SIZE

CLSS public org.apache.sshd.common.util.security.eddsa.generic.GenericSignatureEd25519
cons public init(java.lang.String)
meth public boolean verify(org.apache.sshd.common.session.SessionContext,byte[]) throws java.lang.Exception
supr org.apache.sshd.common.signature.AbstractSignature

CLSS public abstract interface org.apache.sshd.common.util.threads.CloseableExecutorService
intf java.util.concurrent.ExecutorService
intf org.apache.sshd.common.Closeable
meth public boolean awaitTermination(java.time.Duration) throws java.lang.InterruptedException
meth public void close()

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

CLSS public org.apache.sshd.common.util.threads.NoCloseExecutor
cons public init(java.util.concurrent.ExecutorService)
fld protected final java.util.concurrent.ExecutorService executor
fld protected final org.apache.sshd.common.future.CloseFuture closeFuture
intf org.apache.sshd.common.util.threads.CloseableExecutorService
meth public <%0 extends java.lang.Object> java.util.List<java.util.concurrent.Future<{%%0}>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>) throws java.lang.InterruptedException
meth public <%0 extends java.lang.Object> java.util.List<java.util.concurrent.Future<{%%0}>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>,long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.lang.Runnable,{%%0})
meth public <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.util.concurrent.Callable<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException
meth public <%0 extends java.lang.Object> {%%0} invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>,long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException,java.util.concurrent.TimeoutException
meth public boolean awaitTermination(long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public boolean isClosed()
meth public boolean isClosing()
meth public boolean isShutdown()
meth public boolean isTerminated()
meth public java.util.List<java.lang.Runnable> shutdownNow()
meth public java.util.concurrent.Future<?> submit(java.lang.Runnable)
meth public org.apache.sshd.common.future.CloseFuture close(boolean)
meth public void addCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
meth public void execute(java.lang.Runnable)
meth public void removeCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
meth public void shutdown()
supr java.lang.Object

CLSS public org.apache.sshd.common.util.threads.SshThreadPoolExecutor
cons public init(int,int,long,java.util.concurrent.TimeUnit,java.util.concurrent.BlockingQueue<java.lang.Runnable>)
cons public init(int,int,long,java.util.concurrent.TimeUnit,java.util.concurrent.BlockingQueue<java.lang.Runnable>,java.util.concurrent.RejectedExecutionHandler)
cons public init(int,int,long,java.util.concurrent.TimeUnit,java.util.concurrent.BlockingQueue<java.lang.Runnable>,java.util.concurrent.ThreadFactory)
cons public init(int,int,long,java.util.concurrent.TimeUnit,java.util.concurrent.BlockingQueue<java.lang.Runnable>,java.util.concurrent.ThreadFactory,java.util.concurrent.RejectedExecutionHandler)
fld protected final org.apache.sshd.common.util.threads.SshThreadPoolExecutor$DelegateCloseable closeable
innr protected DelegateCloseable
intf org.apache.sshd.common.util.threads.CloseableExecutorService
meth protected void terminated()
meth public boolean awaitTermination(long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public boolean isClosed()
meth public boolean isClosing()
meth public boolean isShutdown()
meth public boolean isTerminated()
meth public boolean isTerminating()
meth public java.util.List<java.lang.Runnable> shutdownNow()
meth public org.apache.sshd.common.future.CloseFuture close(boolean)
meth public void addCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
meth public void removeCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
meth public void shutdown()
supr java.util.concurrent.ThreadPoolExecutor

CLSS protected org.apache.sshd.common.util.threads.SshThreadPoolExecutor$DelegateCloseable
 outer org.apache.sshd.common.util.threads.SshThreadPoolExecutor
cons protected init(org.apache.sshd.common.util.threads.SshThreadPoolExecutor)
meth protected org.apache.sshd.common.future.CloseFuture doCloseGracefully()
meth protected void doCloseImmediately()
meth protected void setClosed()
supr org.apache.sshd.common.util.closeable.AbstractCloseable

CLSS public org.apache.sshd.common.util.threads.SshdThreadFactory
cons public init(java.lang.String)
intf java.util.concurrent.ThreadFactory
meth public java.lang.Thread newThread(java.lang.Runnable)
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds group,namePrefix,threadNumber

CLSS public final org.apache.sshd.common.util.threads.ThreadUtils
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> {%%1} runAsInternal({%%0},org.apache.sshd.common.util.io.functors.IOFunction<? super {%%0},{%%1}>) throws java.io.IOException
meth public static <%0 extends java.lang.Object> {%%0} createDefaultInstance(java.lang.Class<?>,java.lang.Class<? extends {%%0}>,java.lang.String) throws java.lang.ReflectiveOperationException
meth public static <%0 extends java.lang.Object> {%%0} createDefaultInstance(java.lang.ClassLoader,java.lang.Class<? extends {%%0}>,java.lang.String) throws java.lang.ReflectiveOperationException
meth public static <%0 extends java.lang.Object> {%%0} createDefaultInstance(java.lang.Iterable<? extends java.lang.ClassLoader>,java.lang.Class<? extends {%%0}>,java.lang.String) throws java.lang.ReflectiveOperationException
meth public static <%0 extends java.lang.Object> {%%0} runAsInternal(java.util.concurrent.Callable<{%%0}>) throws java.lang.Exception
meth public static boolean isInternalThread()
meth public static java.lang.Class<?> resolveDefaultClass(java.lang.Class<?>,java.lang.String)
meth public static java.lang.Class<?> resolveDefaultClass(java.lang.Iterable<? extends java.lang.ClassLoader>,java.lang.String)
meth public static java.lang.ClassLoader resolveDefaultClassLoader(java.lang.Class<?>)
meth public static java.lang.ClassLoader resolveDefaultClassLoader(java.lang.Object)
meth public static java.lang.Iterable<java.lang.ClassLoader> resolveDefaultClassLoaders(java.lang.Class<?>)
meth public static java.lang.Iterable<java.lang.ClassLoader> resolveDefaultClassLoaders(java.lang.Object)
meth public static java.util.Iterator<java.lang.ClassLoader> iterateDefaultClassLoaders(java.lang.Class<?>)
meth public static java.util.concurrent.ScheduledExecutorService newSingleThreadScheduledExecutor(java.lang.String)
meth public static org.apache.sshd.common.util.threads.CloseableExecutorService newCachedThreadPool(java.lang.String)
meth public static org.apache.sshd.common.util.threads.CloseableExecutorService newCachedThreadPoolIf(org.apache.sshd.common.util.threads.CloseableExecutorService,java.lang.String)
meth public static org.apache.sshd.common.util.threads.CloseableExecutorService newFixedThreadPool(java.lang.String,int)
meth public static org.apache.sshd.common.util.threads.CloseableExecutorService newFixedThreadPoolIf(org.apache.sshd.common.util.threads.CloseableExecutorService,java.lang.String,int)
meth public static org.apache.sshd.common.util.threads.CloseableExecutorService newSingleThreadExecutor(java.lang.String)
meth public static org.apache.sshd.common.util.threads.CloseableExecutorService noClose(org.apache.sshd.common.util.threads.CloseableExecutorService)
meth public static org.apache.sshd.common.util.threads.CloseableExecutorService protectExecutorServiceShutdown(org.apache.sshd.common.util.threads.CloseableExecutorService,boolean)
supr java.lang.Object
hfds IS_INTERNAL_THREAD

CLSS public final org.apache.sshd.core.CoreModuleProperties
fld public final static char SERVER_EXTRA_IDENT_LINES_SEPARATOR = '|'
fld public final static int DEFAULT_FORWARDER_BUF_SIZE = 1024
fld public final static int MAX_FORWARDER_BUF_SIZE = 32767
fld public final static int MIN_FORWARDER_BUF_SIZE = 127
fld public final static java.lang.String AGENT_FORWARDING_TYPE = "agent-fw-auth-type"
fld public final static java.lang.String AGENT_FORWARDING_TYPE_IETF = "auth-agent-req"
fld public final static java.lang.String AGENT_FORWARDING_TYPE_OPENSSH = "auth-agent-req@openssh.com"
fld public final static java.lang.String AUTO_WELCOME_BANNER_VALUE = "#auto-welcome-banner"
fld public final static long DEFAULT_LIMIT_PACKET_SIZE = 536870911
fld public final static long DEFAULT_MAX_PACKET_SIZE = 32768
fld public final static long DEFAULT_WINDOW_SIZE = 2097152
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> ABORT_ON_INVALID_CERTIFICATE
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> ALLOW_DHG1_KEX_FALLBACK
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> IGNORE_INVALID_IDENTITIES
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> KB_SERVER_INTERACTIVE_ECHO_PROMPT
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> PREFER_UNIX_AGENT
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> REQUEST_EXEC_REPLY
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> REQUEST_SHELL_REPLY
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> REQUEST_SUBSYSTEM_REPLY
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> SEND_IMMEDIATE_IDENTIFICATION
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> SEND_IMMEDIATE_KEXINIT
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> SEND_REPLY_FOR_CHANNEL_DATA
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> SOCKET_KEEPALIVE
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> SOCKET_REUSEADDR
fld public final static org.apache.sshd.common.Property<java.lang.Boolean> TCP_NODELAY
fld public final static org.apache.sshd.common.Property<java.lang.Integer> BUFFERED_IO_OUTPUT_MAX_PENDING_WRITE_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.Integer> BUFFER_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.Integer> FORWARDER_BUFFER_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.Integer> HEARTBEAT_NO_REPLY_MAX
fld public final static org.apache.sshd.common.Property<java.lang.Integer> IGNORE_MESSAGE_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.Integer> IGNORE_MESSAGE_VARIANCE
fld public final static org.apache.sshd.common.Property<java.lang.Integer> INPUT_STREAM_PUMP_CHUNK_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.Integer> MAX_AUTH_REQUESTS
fld public final static org.apache.sshd.common.Property<java.lang.Integer> MAX_CONCURRENT_CHANNELS
fld public final static org.apache.sshd.common.Property<java.lang.Integer> MAX_CONCURRENT_SESSIONS
fld public final static org.apache.sshd.common.Property<java.lang.Integer> MAX_EXTDATA_BUFSIZE
fld public final static org.apache.sshd.common.Property<java.lang.Integer> MAX_IDENTIFICATION_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.Integer> MAX_PROXY_JUMPS
fld public final static org.apache.sshd.common.Property<java.lang.Integer> MIN_READ_BUFFER_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.Integer> NIO2_READ_BUFFER_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.Integer> NIO_WORKERS
fld public final static org.apache.sshd.common.Property<java.lang.Integer> PASSWORD_PROMPTS
fld public final static org.apache.sshd.common.Property<java.lang.Integer> PROP_DHGEX_CLIENT_MAX_KEY
fld public final static org.apache.sshd.common.Property<java.lang.Integer> PROP_DHGEX_CLIENT_MIN_KEY
fld public final static org.apache.sshd.common.Property<java.lang.Integer> PROP_DHGEX_CLIENT_PRF_KEY
fld public final static org.apache.sshd.common.Property<java.lang.Integer> PROP_DHGEX_SERVER_MAX_KEY
fld public final static org.apache.sshd.common.Property<java.lang.Integer> PROP_DHGEX_SERVER_MIN_KEY
fld public final static org.apache.sshd.common.Property<java.lang.Integer> SOCKET_BACKLOG
fld public final static org.apache.sshd.common.Property<java.lang.Integer> SOCKET_LINGER
fld public final static org.apache.sshd.common.Property<java.lang.Integer> SOCKET_RCVBUF
fld public final static org.apache.sshd.common.Property<java.lang.Integer> SOCKET_SNDBUF
fld public final static org.apache.sshd.common.Property<java.lang.Integer> X11_BASE_PORT
fld public final static org.apache.sshd.common.Property<java.lang.Integer> X11_DISPLAY_OFFSET
fld public final static org.apache.sshd.common.Property<java.lang.Integer> X11_MAX_DISPLAYS
fld public final static org.apache.sshd.common.Property<java.lang.Long> IGNORE_MESSAGE_FREQUENCY
fld public final static org.apache.sshd.common.Property<java.lang.Long> LIMIT_PACKET_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.Long> MAX_PACKET_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.Long> REKEY_BLOCKS_LIMIT
fld public final static org.apache.sshd.common.Property<java.lang.Long> REKEY_BYTES_LIMIT
fld public final static org.apache.sshd.common.Property<java.lang.Long> REKEY_PACKETS_LIMIT
fld public final static org.apache.sshd.common.Property<java.lang.Long> WINDOW_SIZE
fld public final static org.apache.sshd.common.Property<java.lang.Object> WELCOME_BANNER
fld public final static org.apache.sshd.common.Property<java.lang.String> AUTH_METHODS
fld public final static org.apache.sshd.common.Property<java.lang.String> CLIENT_IDENTIFICATION
fld public final static org.apache.sshd.common.Property<java.lang.String> HEARTBEAT_REQUEST
fld public final static org.apache.sshd.common.Property<java.lang.String> INTERACTIVE_LANGUAGE_TAG
fld public final static org.apache.sshd.common.Property<java.lang.String> INTERACTIVE_SUBMETHODS
fld public final static org.apache.sshd.common.Property<java.lang.String> KB_SERVER_INTERACTIVE_INSTRUCTION
fld public final static org.apache.sshd.common.Property<java.lang.String> KB_SERVER_INTERACTIVE_LANG
fld public final static org.apache.sshd.common.Property<java.lang.String> KB_SERVER_INTERACTIVE_NAME
fld public final static org.apache.sshd.common.Property<java.lang.String> KB_SERVER_INTERACTIVE_PROMPT
fld public final static org.apache.sshd.common.Property<java.lang.String> MODULI_URL
fld public final static org.apache.sshd.common.Property<java.lang.String> PREFERRED_AUTHS
fld public final static org.apache.sshd.common.Property<java.lang.String> PROXY_AUTH_CHANNEL_TYPE
fld public final static org.apache.sshd.common.Property<java.lang.String> PROXY_CHANNEL_TYPE
fld public final static org.apache.sshd.common.Property<java.lang.String> SERVER_EXTRA_IDENTIFICATION_LINES
fld public final static org.apache.sshd.common.Property<java.lang.String> SERVER_IDENTIFICATION
fld public final static org.apache.sshd.common.Property<java.lang.String> WELCOME_BANNER_LANGUAGE
fld public final static org.apache.sshd.common.Property<java.lang.String> X11_BIND_HOST
fld public final static org.apache.sshd.common.Property<java.nio.charset.Charset> WELCOME_BANNER_CHARSET
fld public final static org.apache.sshd.common.Property<java.time.Duration> AUTH_SOCKET_TIMEOUT
fld public final static org.apache.sshd.common.Property<java.time.Duration> AUTH_TIMEOUT
fld public final static org.apache.sshd.common.Property<java.time.Duration> BUFFERED_IO_OUTPUT_MAX_PENDING_WRITE_WAIT
fld public final static org.apache.sshd.common.Property<java.time.Duration> CHANNEL_CLOSE_TIMEOUT
fld public final static org.apache.sshd.common.Property<java.time.Duration> CHANNEL_OPEN_TIMEOUT
fld public final static org.apache.sshd.common.Property<java.time.Duration> COMMAND_EXIT_TIMEOUT
fld public final static org.apache.sshd.common.Property<java.time.Duration> DISCONNECT_TIMEOUT
fld public final static org.apache.sshd.common.Property<java.time.Duration> FORWARD_REQUEST_TIMEOUT
fld public final static org.apache.sshd.common.Property<java.time.Duration> HEARTBEAT_INTERVAL
fld public final static org.apache.sshd.common.Property<java.time.Duration> HEARTBEAT_REPLY_WAIT
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.Property<java.time.Duration> IDLE_TIMEOUT
fld public final static org.apache.sshd.common.Property<java.time.Duration> IO_CONNECT_TIMEOUT
fld public final static org.apache.sshd.common.Property<java.time.Duration> KEX_PROPOSAL_SETUP_TIMEOUT
 anno 0 java.lang.Deprecated()
fld public final static org.apache.sshd.common.Property<java.time.Duration> NIO2_MIN_WRITE_TIMEOUT
fld public final static org.apache.sshd.common.Property<java.time.Duration> NIO2_READ_TIMEOUT
fld public final static org.apache.sshd.common.Property<java.time.Duration> PUMP_SLEEP_TIME
fld public final static org.apache.sshd.common.Property<java.time.Duration> REKEY_TIME_LIMIT
fld public final static org.apache.sshd.common.Property<java.time.Duration> STOP_WAIT_TIME
fld public final static org.apache.sshd.common.Property<java.time.Duration> WAIT_FOR_SPACE_TIMEOUT
fld public final static org.apache.sshd.common.Property<java.time.Duration> WINDOW_TIMEOUT
fld public final static org.apache.sshd.common.Property<java.time.Duration> X11_OPEN_TIMEOUT
fld public final static org.apache.sshd.common.Property<org.apache.sshd.server.auth.WelcomeBannerPhase> WELCOME_BANNER_PHASE
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.server.Environment
fld public final static java.lang.String ENV_COLUMNS = "COLUMNS"
fld public final static java.lang.String ENV_LINES = "LINES"
fld public final static java.lang.String ENV_TERM = "TERM"
fld public final static java.lang.String ENV_USER = "USER"
meth public !varargs void addSignalListener(org.apache.sshd.server.SignalListener,org.apache.sshd.server.Signal[])
meth public abstract java.util.Map<java.lang.String,java.lang.String> getEnv()
meth public abstract java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer> getPtyModes()
meth public abstract void addSignalListener(org.apache.sshd.server.SignalListener,java.util.Collection<org.apache.sshd.server.Signal>)
meth public abstract void removeSignalListener(org.apache.sshd.server.SignalListener)
meth public void addSignalListener(org.apache.sshd.server.SignalListener)

CLSS public abstract interface org.apache.sshd.server.ExitCallback
 anno 0 java.lang.FunctionalInterface()
meth public abstract void onExit(int,java.lang.String,boolean)
meth public void onExit(int)
meth public void onExit(int,boolean)
meth public void onExit(int,java.lang.String)

CLSS public abstract interface org.apache.sshd.server.ServerAuthenticationManager
fld public final static org.apache.sshd.server.auth.gss.UserAuthGSSFactory DEFAULT_USER_AUTH_GSS_FACTORY
fld public final static org.apache.sshd.server.auth.keyboard.UserAuthKeyboardInteractiveFactory DEFAULT_USER_AUTH_KB_INTERACTIVE_FACTORY
fld public final static org.apache.sshd.server.auth.password.UserAuthPasswordFactory DEFAULT_USER_AUTH_PASSWORD_FACTORY
fld public final static org.apache.sshd.server.auth.pubkey.UserAuthPublicKeyFactory DEFAULT_USER_AUTH_PUBLIC_KEY_FACTORY
intf org.apache.sshd.common.auth.UserAuthFactoriesManager<org.apache.sshd.server.session.ServerSession,org.apache.sshd.server.auth.UserAuth,org.apache.sshd.server.auth.UserAuthFactory>
intf org.apache.sshd.common.keyprovider.KeyPairProviderHolder
meth public abstract org.apache.sshd.common.keyprovider.HostKeyCertificateProvider getHostKeyCertificateProvider()
meth public abstract org.apache.sshd.server.auth.gss.GSSAuthenticator getGSSAuthenticator()
meth public abstract org.apache.sshd.server.auth.hostbased.HostBasedAuthenticator getHostBasedAuthenticator()
meth public abstract org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator getKeyboardInteractiveAuthenticator()
meth public abstract org.apache.sshd.server.auth.password.PasswordAuthenticator getPasswordAuthenticator()
meth public abstract org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator getPublickeyAuthenticator()
meth public abstract void setGSSAuthenticator(org.apache.sshd.server.auth.gss.GSSAuthenticator)
meth public abstract void setHostBasedAuthenticator(org.apache.sshd.server.auth.hostbased.HostBasedAuthenticator)
meth public abstract void setHostKeyCertificateProvider(org.apache.sshd.common.keyprovider.HostKeyCertificateProvider)
meth public abstract void setKeyboardInteractiveAuthenticator(org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator)
meth public abstract void setPasswordAuthenticator(org.apache.sshd.server.auth.password.PasswordAuthenticator)
meth public abstract void setPublickeyAuthenticator(org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator)
meth public static java.util.List<org.apache.sshd.server.auth.UserAuthFactory> resolveUserAuthFactories(org.apache.sshd.server.ServerAuthenticationManager)
meth public static java.util.List<org.apache.sshd.server.auth.UserAuthFactory> resolveUserAuthFactories(org.apache.sshd.server.ServerAuthenticationManager,java.util.List<org.apache.sshd.server.auth.UserAuthFactory>)
meth public void setUserAuthFactoriesNames(java.util.Collection<java.lang.String>)

CLSS public org.apache.sshd.server.ServerBuilder
cons public init()
fld protected org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator interactiveAuthenticator
fld protected org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator pubkeyAuthenticator
fld public final static java.util.List<org.apache.sshd.common.channel.ChannelFactory> DEFAULT_CHANNEL_FACTORIES
fld public final static java.util.List<org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.session.ConnectionService>> DEFAULT_GLOBAL_REQUEST_HANDLERS
fld public final static java.util.List<org.apache.sshd.common.cipher.BuiltinCiphers> DEFAULT_SERVER_CIPHERS_PREFERENCE
fld public final static java.util.List<org.apache.sshd.common.compression.CompressionFactory> DEFAULT_COMPRESSION_FACTORIES
fld public final static java.util.function.Function<org.apache.sshd.common.kex.DHFactory,org.apache.sshd.common.kex.KeyExchangeFactory> DH2KEX
fld public final static org.apache.sshd.common.kex.extension.KexExtensionHandler DEFAULT_KEX_EXTENSION_HANDLER
fld public final static org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator DEFAULT_INTERACTIVE_AUTHENTICATOR
fld public final static org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator DEFAULT_PUBLIC_KEY_AUTHENTICATOR
meth protected org.apache.sshd.server.ServerBuilder fillWithDefaultValues()
meth public org.apache.sshd.server.ServerBuilder interactiveAuthenticator(org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator)
meth public org.apache.sshd.server.ServerBuilder publickeyAuthenticator(org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator)
meth public org.apache.sshd.server.SshServer build(boolean)
meth public static java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.compression.Compression>> setUpDefaultCompressionFactories(boolean)
meth public static java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> setUpDefaultSignatureFactories(boolean)
meth public static java.util.List<org.apache.sshd.common.kex.KeyExchangeFactory> setUpDefaultKeyExchanges(boolean)
meth public static org.apache.sshd.server.ServerBuilder builder()
supr org.apache.sshd.common.BaseBuilder<org.apache.sshd.server.SshServer,org.apache.sshd.server.ServerBuilder>

CLSS public abstract interface org.apache.sshd.server.ServerFactoryManager
intf org.apache.sshd.common.FactoryManager
intf org.apache.sshd.server.ServerAuthenticationManager
intf org.apache.sshd.server.session.ServerProxyAcceptorHolder
meth public abstract java.util.List<? extends org.apache.sshd.server.subsystem.SubsystemFactory> getSubsystemFactories()
meth public abstract org.apache.sshd.server.command.CommandFactory getCommandFactory()
meth public abstract org.apache.sshd.server.shell.ShellFactory getShellFactory()

CLSS public final !enum org.apache.sshd.server.Signal
fld public final static java.util.NavigableMap<java.lang.Integer,org.apache.sshd.server.Signal> NUMERIC_LOOKUP_TABLE
fld public final static java.util.NavigableMap<java.lang.String,org.apache.sshd.server.Signal> NAME_LOOKUP_TABLE
fld public final static java.util.Set<org.apache.sshd.server.Signal> SIGNALS
fld public final static org.apache.sshd.server.Signal ALRM
fld public final static org.apache.sshd.server.Signal BUS
fld public final static org.apache.sshd.server.Signal CHLD
fld public final static org.apache.sshd.server.Signal CONT
fld public final static org.apache.sshd.server.Signal FPE
fld public final static org.apache.sshd.server.Signal HUP
fld public final static org.apache.sshd.server.Signal ILL
fld public final static org.apache.sshd.server.Signal INT
fld public final static org.apache.sshd.server.Signal IO
fld public final static org.apache.sshd.server.Signal IOT
fld public final static org.apache.sshd.server.Signal KILL
fld public final static org.apache.sshd.server.Signal PIPE
fld public final static org.apache.sshd.server.Signal PROF
fld public final static org.apache.sshd.server.Signal PWR
fld public final static org.apache.sshd.server.Signal QUIT
fld public final static org.apache.sshd.server.Signal SEGV
fld public final static org.apache.sshd.server.Signal STKFLT
fld public final static org.apache.sshd.server.Signal STOP
fld public final static org.apache.sshd.server.Signal TERM
fld public final static org.apache.sshd.server.Signal TRAP
fld public final static org.apache.sshd.server.Signal TSTP
fld public final static org.apache.sshd.server.Signal TTIN
fld public final static org.apache.sshd.server.Signal TTOU
fld public final static org.apache.sshd.server.Signal URG
fld public final static org.apache.sshd.server.Signal USR1
fld public final static org.apache.sshd.server.Signal USR2
fld public final static org.apache.sshd.server.Signal VTALRM
fld public final static org.apache.sshd.server.Signal WINCH
fld public final static org.apache.sshd.server.Signal XCPU
fld public final static org.apache.sshd.server.Signal XFSZ
meth public int getNumeric()
meth public static org.apache.sshd.server.Signal get(int)
meth public static org.apache.sshd.server.Signal get(java.lang.String)
meth public static org.apache.sshd.server.Signal valueOf(java.lang.String)
meth public static org.apache.sshd.server.Signal[] values()
supr java.lang.Enum<org.apache.sshd.server.Signal>
hfds numeric

CLSS public abstract interface org.apache.sshd.server.SignalListener
 anno 0 java.lang.FunctionalInterface()
intf org.apache.sshd.common.util.SshdEventListener
meth public abstract void signal(org.apache.sshd.common.channel.Channel,org.apache.sshd.server.Signal)
meth public static <%0 extends org.apache.sshd.server.SignalListener> {%%0} validateListener({%%0})

CLSS public org.apache.sshd.server.SshServer
cons public init()
fld protected int port
fld protected java.lang.String host
fld protected org.apache.sshd.common.io.IoAcceptor acceptor
fld public final static java.util.List<org.apache.sshd.common.ServiceFactory> DEFAULT_SERVICE_FACTORIES
fld public final static org.apache.sshd.common.Factory<org.apache.sshd.server.SshServer> DEFAULT_SSH_SERVER_FACTORY
intf org.apache.sshd.server.ServerFactoryManager
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected org.apache.sshd.common.io.IoAcceptor createAcceptor()
meth protected org.apache.sshd.server.session.SessionFactory createSessionFactory()
meth protected void checkConfig()
meth public boolean isStarted()
meth public int getPort()
meth public java.lang.String getHost()
meth public java.lang.String toString()
meth public java.util.List<? extends org.apache.sshd.server.subsystem.SubsystemFactory> getSubsystemFactories()
meth public java.util.List<org.apache.sshd.common.session.helpers.AbstractSession> getActiveSessions()
meth public java.util.List<org.apache.sshd.server.auth.UserAuthFactory> getUserAuthFactories()
meth public java.util.Set<java.net.SocketAddress> getBoundAddresses()
meth public org.apache.sshd.common.keyprovider.HostKeyCertificateProvider getHostKeyCertificateProvider()
meth public org.apache.sshd.common.keyprovider.KeyPairProvider getKeyPairProvider()
meth public org.apache.sshd.server.auth.gss.GSSAuthenticator getGSSAuthenticator()
meth public org.apache.sshd.server.auth.hostbased.HostBasedAuthenticator getHostBasedAuthenticator()
meth public org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator getKeyboardInteractiveAuthenticator()
meth public org.apache.sshd.server.auth.password.PasswordAuthenticator getPasswordAuthenticator()
meth public org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator getPublickeyAuthenticator()
meth public org.apache.sshd.server.command.CommandFactory getCommandFactory()
meth public org.apache.sshd.server.session.ServerProxyAcceptor getServerProxyAcceptor()
meth public org.apache.sshd.server.session.SessionFactory getSessionFactory()
meth public org.apache.sshd.server.shell.ShellFactory getShellFactory()
meth public static org.apache.sshd.server.SshServer setUpDefaultServer()
meth public void open() throws java.io.IOException
meth public void setCommandFactory(org.apache.sshd.server.command.CommandFactory)
meth public void setGSSAuthenticator(org.apache.sshd.server.auth.gss.GSSAuthenticator)
meth public void setHost(java.lang.String)
meth public void setHostBasedAuthenticator(org.apache.sshd.server.auth.hostbased.HostBasedAuthenticator)
meth public void setHostKeyCertificateProvider(org.apache.sshd.common.keyprovider.HostKeyCertificateProvider)
meth public void setKeyPairProvider(org.apache.sshd.common.keyprovider.KeyPairProvider)
meth public void setKeyboardInteractiveAuthenticator(org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator)
meth public void setPasswordAuthenticator(org.apache.sshd.server.auth.password.PasswordAuthenticator)
meth public void setPort(int)
meth public void setPublickeyAuthenticator(org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator)
meth public void setServerProxyAcceptor(org.apache.sshd.server.session.ServerProxyAcceptor)
meth public void setSessionFactory(org.apache.sshd.server.session.SessionFactory)
meth public void setShellFactory(org.apache.sshd.server.shell.ShellFactory)
meth public void setSubsystemFactories(java.util.List<? extends org.apache.sshd.server.subsystem.SubsystemFactory>)
meth public void setUserAuthFactories(java.util.List<org.apache.sshd.server.auth.UserAuthFactory>)
meth public void start() throws java.io.IOException
meth public void stop() throws java.io.IOException
meth public void stop(boolean) throws java.io.IOException
supr org.apache.sshd.common.helpers.AbstractFactoryManager
hfds commandFactory,gssAuthenticator,hostBasedAuthenticator,hostKeyCertificateProvider,interactiveAuthenticator,keyPairProvider,passwordAuthenticator,proxyAcceptor,publickeyAuthenticator,sessionFactory,shellFactory,started,subsystemFactories,userAuthFactories

CLSS public org.apache.sshd.server.StandardEnvironment
cons public init()
intf org.apache.sshd.server.Environment
meth protected java.util.Collection<org.apache.sshd.server.SignalListener> getSignalListeners(org.apache.sshd.server.Signal,boolean)
meth public java.lang.String toString()
meth public java.util.Map<java.lang.String,java.lang.String> getEnv()
meth public java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer> getPtyModes()
meth public void addSignalListener(org.apache.sshd.server.SignalListener,java.util.Collection<org.apache.sshd.server.Signal>)
meth public void removeSignalListener(org.apache.sshd.server.SignalListener)
meth public void set(java.lang.String,java.lang.String)
meth public void signal(org.apache.sshd.common.channel.Channel,org.apache.sshd.server.Signal)
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds env,listeners,ptyModes

CLSS public abstract org.apache.sshd.server.auth.AbstractUserAuth
cons protected init(java.lang.String)
intf org.apache.sshd.server.auth.UserAuth
meth protected abstract java.lang.Boolean doAuth(org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.lang.Exception
meth public final java.lang.String getName()
meth public java.lang.Boolean auth(org.apache.sshd.server.session.ServerSession,java.lang.String,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public java.lang.Boolean next(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public java.lang.String getService()
meth public java.lang.String getUsername()
meth public java.lang.String toString()
meth public org.apache.sshd.server.session.ServerSession getServerSession()
meth public org.apache.sshd.server.session.ServerSession getSession()
meth public void destroy()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds name,service,session,username

CLSS public abstract org.apache.sshd.server.auth.AbstractUserAuthFactory
cons protected init(java.lang.String)
intf org.apache.sshd.server.auth.UserAuthFactory
supr org.apache.sshd.common.auth.AbstractUserAuthMethodFactory<org.apache.sshd.server.session.ServerSession,org.apache.sshd.server.auth.UserAuth>

CLSS public org.apache.sshd.server.auth.AsyncAuthException
cons public init()
fld protected java.lang.Boolean authed
fld protected java.lang.Object listener
meth protected java.util.function.Consumer<? super java.lang.Boolean> asListener(java.lang.Object)
meth public void addListener(java.util.function.Consumer<? super java.lang.Boolean>)
meth public void setAuthed(boolean)
supr org.apache.sshd.common.RuntimeSshException
hfds serialVersionUID

CLSS public final !enum org.apache.sshd.server.auth.BuiltinUserAuthFactories
fld public final static java.util.Set<org.apache.sshd.server.auth.BuiltinUserAuthFactories> VALUES
fld public final static org.apache.sshd.server.auth.BuiltinUserAuthFactories GSS
fld public final static org.apache.sshd.server.auth.BuiltinUserAuthFactories HOSTBASED
fld public final static org.apache.sshd.server.auth.BuiltinUserAuthFactories KBINTERACTIVE
fld public final static org.apache.sshd.server.auth.BuiltinUserAuthFactories PASSWORD
fld public final static org.apache.sshd.server.auth.BuiltinUserAuthFactories PUBLICKEY
innr public static ParseResult
intf org.apache.sshd.common.NamedFactory<org.apache.sshd.server.auth.UserAuthFactory>
meth public !varargs static org.apache.sshd.server.auth.BuiltinUserAuthFactories$ParseResult parseFactoriesList(java.lang.String[])
meth public java.lang.String getName()
meth public org.apache.sshd.server.auth.UserAuthFactory create()
meth public static org.apache.sshd.server.auth.BuiltinUserAuthFactories valueOf(java.lang.String)
meth public static org.apache.sshd.server.auth.BuiltinUserAuthFactories$ParseResult parseFactoriesList(java.lang.String)
meth public static org.apache.sshd.server.auth.BuiltinUserAuthFactories$ParseResult parseFactoriesList(java.util.Collection<java.lang.String>)
meth public static org.apache.sshd.server.auth.BuiltinUserAuthFactories[] values()
meth public static org.apache.sshd.server.auth.UserAuthFactory fromFactoryName(java.lang.String)
meth public static org.apache.sshd.server.auth.UserAuthFactory resolveFactory(java.lang.String)
supr java.lang.Enum<org.apache.sshd.server.auth.BuiltinUserAuthFactories>
hfds factory

CLSS public static org.apache.sshd.server.auth.BuiltinUserAuthFactories$ParseResult
 outer org.apache.sshd.server.auth.BuiltinUserAuthFactories
cons public init(java.util.List<org.apache.sshd.server.auth.UserAuthFactory>,java.util.List<java.lang.String>)
fld public final static org.apache.sshd.server.auth.BuiltinUserAuthFactories$ParseResult EMPTY
supr org.apache.sshd.common.config.NamedFactoriesListParseResult<org.apache.sshd.server.auth.UserAuth,org.apache.sshd.server.auth.UserAuthFactory>

CLSS public abstract interface org.apache.sshd.server.auth.UserAuth
intf org.apache.sshd.common.auth.UserAuthInstance<org.apache.sshd.server.session.ServerSession>
intf org.apache.sshd.common.auth.UsernameHolder
intf org.apache.sshd.server.session.ServerSessionHolder
meth public abstract java.lang.Boolean auth(org.apache.sshd.server.session.ServerSession,java.lang.String,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public abstract java.lang.Boolean next(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public abstract void destroy()

CLSS public abstract interface org.apache.sshd.server.auth.UserAuthFactory
intf org.apache.sshd.common.auth.UserAuthMethodFactory<org.apache.sshd.server.session.ServerSession,org.apache.sshd.server.auth.UserAuth>

CLSS public org.apache.sshd.server.auth.UserAuthNone
cons public init()
fld public final static java.lang.String NAME = "none"
meth public java.lang.Boolean doAuth(org.apache.sshd.common.util.buffer.Buffer,boolean)
supr org.apache.sshd.server.auth.AbstractUserAuth

CLSS public org.apache.sshd.server.auth.UserAuthNoneFactory
cons public init()
fld public final static java.lang.String NAME = "none"
fld public final static org.apache.sshd.server.auth.UserAuthNoneFactory INSTANCE
meth public org.apache.sshd.server.auth.UserAuthNone createUserAuth(org.apache.sshd.server.session.ServerSession) throws java.io.IOException
supr org.apache.sshd.server.auth.AbstractUserAuthFactory

CLSS public final !enum org.apache.sshd.server.auth.WelcomeBannerPhase
fld public final static java.util.Set<org.apache.sshd.server.auth.WelcomeBannerPhase> VALUES
fld public final static org.apache.sshd.server.auth.WelcomeBannerPhase FIRST_AUTHCMD
fld public final static org.apache.sshd.server.auth.WelcomeBannerPhase FIRST_FAILURE
fld public final static org.apache.sshd.server.auth.WelcomeBannerPhase FIRST_REQUEST
fld public final static org.apache.sshd.server.auth.WelcomeBannerPhase IMMEDIATE
fld public final static org.apache.sshd.server.auth.WelcomeBannerPhase NEVER
fld public final static org.apache.sshd.server.auth.WelcomeBannerPhase POST_SUCCESS
meth public static org.apache.sshd.server.auth.WelcomeBannerPhase valueOf(java.lang.String)
meth public static org.apache.sshd.server.auth.WelcomeBannerPhase[] values()
supr java.lang.Enum<org.apache.sshd.server.auth.WelcomeBannerPhase>

CLSS public final org.apache.sshd.server.auth.gss.CredentialHelper
meth public static org.ietf.jgss.GSSCredential creds(org.ietf.jgss.GSSManager,java.lang.String,java.lang.String) throws javax.security.auth.login.LoginException,org.ietf.jgss.GSSException
supr java.lang.Object
hcls FixedLoginConfiguration,G

CLSS public org.apache.sshd.server.auth.gss.GSSAuthenticator
cons public init()
meth public boolean validateIdentity(org.apache.sshd.server.session.ServerSession,java.lang.String)
meth public boolean validateInitialUser(org.apache.sshd.server.session.ServerSession,java.lang.String)
meth public org.ietf.jgss.GSSCredential getGSSCredential(org.ietf.jgss.GSSManager) throws java.net.UnknownHostException,javax.security.auth.login.LoginException,org.ietf.jgss.GSSException
meth public org.ietf.jgss.GSSManager getGSSManager()
meth public void setKeytabFile(java.lang.String)
meth public void setServicePrincipalName(java.lang.String)
supr java.lang.Object
hfds keytabFile,servicePrincipalName

CLSS public org.apache.sshd.server.auth.gss.UserAuthGSS
cons public init()
fld public final static java.lang.String NAME = "gssapi-with-mic"
fld public final static org.ietf.jgss.Oid KRB5_MECH
fld public final static org.ietf.jgss.Oid KRB5_NT_PRINCIPAL
meth protected java.lang.Boolean doAuth(org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.lang.Exception
meth public static org.ietf.jgss.Oid createOID(java.lang.String)
meth public void destroy()
supr org.apache.sshd.server.auth.AbstractUserAuth
hfds context,identity

CLSS public org.apache.sshd.server.auth.gss.UserAuthGSSFactory
cons public init()
fld public final static java.lang.String NAME = "gssapi-with-mic"
fld public final static org.apache.sshd.server.auth.gss.UserAuthGSSFactory INSTANCE
intf org.apache.sshd.server.auth.UserAuthFactory
meth public final java.lang.String getName()
meth public org.apache.sshd.server.auth.UserAuth createUserAuth(org.apache.sshd.server.session.ServerSession) throws java.io.IOException
supr java.lang.Object

CLSS public final org.apache.sshd.server.auth.hostbased.AcceptAllHostBasedAuthenticator
fld public final static org.apache.sshd.server.auth.hostbased.AcceptAllHostBasedAuthenticator INSTANCE
supr org.apache.sshd.server.auth.hostbased.StaticHostBasedAuthenticator

CLSS public abstract interface org.apache.sshd.server.auth.hostbased.HostBasedAuthenticator
 anno 0 java.lang.FunctionalInterface()
meth public abstract boolean authenticate(org.apache.sshd.server.session.ServerSession,java.lang.String,java.security.PublicKey,java.lang.String,java.lang.String,java.util.List<java.security.cert.X509Certificate>)

CLSS public final org.apache.sshd.server.auth.hostbased.RejectAllHostBasedAuthenticator
fld public final static org.apache.sshd.server.auth.hostbased.RejectAllHostBasedAuthenticator INSTANCE
supr org.apache.sshd.server.auth.hostbased.StaticHostBasedAuthenticator

CLSS public org.apache.sshd.server.auth.hostbased.StaticHostBasedAuthenticator
cons public init(boolean)
intf org.apache.sshd.server.auth.hostbased.HostBasedAuthenticator
meth protected void handleAcceptance(org.apache.sshd.server.session.ServerSession,java.lang.String,java.security.PublicKey,java.lang.String,java.lang.String,java.util.List<java.security.cert.X509Certificate>)
meth protected void handleRejection(org.apache.sshd.server.session.ServerSession,java.lang.String,java.security.PublicKey,java.lang.String,java.lang.String,java.util.List<java.security.cert.X509Certificate>)
meth public final boolean authenticate(org.apache.sshd.server.session.ServerSession,java.lang.String,java.security.PublicKey,java.lang.String,java.lang.String,java.util.List<java.security.cert.X509Certificate>)
meth public final boolean isAccepted()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds acceptance

CLSS public org.apache.sshd.server.auth.hostbased.UserAuthHostBased
cons public init()
cons public init(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
fld public final static java.lang.String NAME = "hostbased"
intf org.apache.sshd.common.signature.SignatureFactoriesManager
meth protected java.lang.Boolean doAuth(org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.lang.Exception
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories()
meth public void setSignatureFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
supr org.apache.sshd.server.auth.AbstractUserAuth
hfds factories

CLSS public org.apache.sshd.server.auth.hostbased.UserAuthHostBasedFactory
cons public init()
cons public init(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
fld public final static java.lang.String NAME = "hostbased"
fld public final static org.apache.sshd.server.auth.hostbased.UserAuthHostBasedFactory INSTANCE
intf org.apache.sshd.common.signature.SignatureFactoriesManager
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories()
meth public org.apache.sshd.server.auth.hostbased.UserAuthHostBased createUserAuth(org.apache.sshd.server.session.ServerSession) throws java.io.IOException
meth public void setSignatureFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
supr org.apache.sshd.server.auth.AbstractUserAuthFactory
hfds factories

CLSS public org.apache.sshd.server.auth.keyboard.DefaultKeyboardInteractiveAuthenticator
cons public init()
fld public final static org.apache.sshd.server.auth.keyboard.DefaultKeyboardInteractiveAuthenticator INSTANCE
intf org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator
meth protected boolean isInteractionPromptEchoEnabled(org.apache.sshd.server.session.ServerSession)
meth protected java.lang.String getInteractionInstruction(org.apache.sshd.server.session.ServerSession)
meth protected java.lang.String getInteractionLanguage(org.apache.sshd.server.session.ServerSession)
meth protected java.lang.String getInteractionName(org.apache.sshd.server.session.ServerSession)
meth protected java.lang.String getInteractionPrompt(org.apache.sshd.server.session.ServerSession)
meth public boolean authenticate(org.apache.sshd.server.session.ServerSession,java.lang.String,java.util.List<java.lang.String>) throws java.lang.Exception
meth public org.apache.sshd.server.auth.keyboard.InteractiveChallenge generateChallenge(org.apache.sshd.server.session.ServerSession,java.lang.String,java.lang.String,java.lang.String) throws java.lang.Exception
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public org.apache.sshd.server.auth.keyboard.InteractiveChallenge
cons public init()
intf java.lang.Cloneable
meth public <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} append({%%0})
meth public java.lang.String getInteractionInstruction()
meth public java.lang.String getInteractionName()
meth public java.lang.String getLanguageTag()
meth public java.lang.String toString()
meth public java.util.List<org.apache.sshd.server.auth.keyboard.PromptEntry> getPrompts()
meth public org.apache.sshd.server.auth.keyboard.InteractiveChallenge clone()
meth public void addPrompt(java.lang.String,boolean)
meth public void addPrompt(org.apache.sshd.server.auth.keyboard.PromptEntry)
meth public void clearPrompts()
meth public void setInteractionInstruction(java.lang.String)
meth public void setInteractionName(java.lang.String)
meth public void setLanguageTag(java.lang.String)
meth public void setPrompts(java.util.Collection<? extends org.apache.sshd.server.auth.keyboard.PromptEntry>)
supr java.lang.Object
hfds interactionInstruction,interactionName,languageTag,prompts

CLSS public abstract interface org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator
fld public final static org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator NONE
meth public abstract boolean authenticate(org.apache.sshd.server.session.ServerSession,java.lang.String,java.util.List<java.lang.String>) throws java.lang.Exception
meth public abstract org.apache.sshd.server.auth.keyboard.InteractiveChallenge generateChallenge(org.apache.sshd.server.session.ServerSession,java.lang.String,java.lang.String,java.lang.String) throws java.lang.Exception

CLSS public org.apache.sshd.server.auth.keyboard.PromptEntry
cons public init()
cons public init(java.lang.String,boolean)
intf java.io.Serializable
intf java.lang.Cloneable
meth public <%0 extends org.apache.sshd.common.util.buffer.Buffer> {%%0} append({%%0})
meth public boolean equals(java.lang.Object)
meth public boolean isEcho()
meth public int hashCode()
meth public java.lang.String getPrompt()
meth public java.lang.String toString()
meth public org.apache.sshd.server.auth.keyboard.PromptEntry clone()
meth public void setEcho(boolean)
meth public void setPrompt(java.lang.String)
supr java.lang.Object
hfds echo,prompt,serialVersionUID

CLSS public org.apache.sshd.server.auth.keyboard.UserAuthKeyboardInteractive
cons public init()
fld public final static java.lang.String NAME = "keyboard-interactive"
meth protected java.lang.Boolean doAuth(org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.lang.Exception
meth protected java.lang.Boolean doInitialAuth(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected java.lang.Boolean doValidateAuthResponse(org.apache.sshd.server.session.ServerSession,java.lang.String,org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
supr org.apache.sshd.server.auth.AbstractUserAuth

CLSS public org.apache.sshd.server.auth.keyboard.UserAuthKeyboardInteractiveFactory
cons public init()
fld public final static java.lang.String NAME = "keyboard-interactive"
fld public final static org.apache.sshd.server.auth.keyboard.UserAuthKeyboardInteractiveFactory INSTANCE
meth public org.apache.sshd.server.auth.keyboard.UserAuthKeyboardInteractive createUserAuth(org.apache.sshd.server.session.ServerSession) throws java.io.IOException
supr org.apache.sshd.server.auth.AbstractUserAuthFactory

CLSS public final org.apache.sshd.server.auth.password.AcceptAllPasswordAuthenticator
fld public final static org.apache.sshd.server.auth.password.AcceptAllPasswordAuthenticator INSTANCE
supr org.apache.sshd.server.auth.password.StaticPasswordAuthenticator

CLSS public abstract interface org.apache.sshd.server.auth.password.PasswordAuthenticator
 anno 0 java.lang.FunctionalInterface()
meth public abstract boolean authenticate(java.lang.String,java.lang.String,org.apache.sshd.server.session.ServerSession)
meth public boolean handleClientPasswordChangeRequest(org.apache.sshd.server.session.ServerSession,java.lang.String,java.lang.String,java.lang.String)

CLSS public org.apache.sshd.server.auth.password.PasswordChangeRequiredException
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable,java.lang.String,java.lang.String)
meth public final java.lang.String getLanguage()
meth public final java.lang.String getPrompt()
supr java.lang.RuntimeException
hfds lang,prompt,serialVersionUID

CLSS public final org.apache.sshd.server.auth.password.RejectAllPasswordAuthenticator
fld public final static org.apache.sshd.server.auth.password.RejectAllPasswordAuthenticator INSTANCE
supr org.apache.sshd.server.auth.password.StaticPasswordAuthenticator

CLSS public org.apache.sshd.server.auth.password.StaticPasswordAuthenticator
cons public init(boolean)
intf org.apache.sshd.server.auth.password.PasswordAuthenticator
meth protected void handleAcceptance(java.lang.String,java.lang.String,org.apache.sshd.server.session.ServerSession)
meth protected void handleRejection(java.lang.String,java.lang.String,org.apache.sshd.server.session.ServerSession)
meth public final boolean authenticate(java.lang.String,java.lang.String,org.apache.sshd.server.session.ServerSession)
meth public final boolean isAccepted()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds acceptance

CLSS public org.apache.sshd.server.auth.password.UserAuthPassword
cons public init()
fld public final static java.lang.String NAME = "password"
meth protected java.lang.Boolean checkPassword(org.apache.sshd.common.util.buffer.Buffer,org.apache.sshd.server.session.ServerSession,java.lang.String,java.lang.String) throws java.lang.Exception
meth protected java.lang.Boolean handleClientPasswordChangeRequest(org.apache.sshd.common.util.buffer.Buffer,org.apache.sshd.server.session.ServerSession,java.lang.String,java.lang.String,java.lang.String) throws java.lang.Exception
meth protected java.lang.Boolean handleServerPasswordChangeRequest(org.apache.sshd.common.util.buffer.Buffer,org.apache.sshd.server.session.ServerSession,java.lang.String,java.lang.String,org.apache.sshd.server.auth.password.PasswordChangeRequiredException) throws java.lang.Exception
meth public java.lang.Boolean doAuth(org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.lang.Exception
supr org.apache.sshd.server.auth.AbstractUserAuth

CLSS public org.apache.sshd.server.auth.password.UserAuthPasswordFactory
cons public init()
fld public final static java.lang.String NAME = "password"
fld public final static org.apache.sshd.server.auth.password.UserAuthPasswordFactory INSTANCE
meth public org.apache.sshd.server.auth.password.UserAuthPassword createUserAuth(org.apache.sshd.server.session.ServerSession) throws java.io.IOException
supr org.apache.sshd.server.auth.AbstractUserAuthFactory

CLSS public final org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator
fld public final static org.apache.sshd.server.auth.pubkey.AcceptAllPublickeyAuthenticator INSTANCE
supr org.apache.sshd.server.auth.pubkey.StaticPublickeyAuthenticator

CLSS public org.apache.sshd.server.auth.pubkey.AuthorizedKeyEntriesPublickeyAuthenticator
cons public init(java.lang.Object,org.apache.sshd.server.session.ServerSession,java.util.Collection<? extends org.apache.sshd.common.config.keys.AuthorizedKeyEntry>,org.apache.sshd.common.config.keys.PublicKeyEntryResolver) throws java.io.IOException,java.security.GeneralSecurityException
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<org.apache.sshd.common.config.keys.AuthorizedKeyEntry> AUTHORIZED_KEY
intf org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator
meth protected boolean matchesPrincipals(org.apache.sshd.common.config.keys.AuthorizedKeyEntry,java.lang.String,org.apache.sshd.common.config.keys.OpenSshCertificate,org.apache.sshd.server.session.ServerSession)
meth public boolean authenticate(java.lang.String,java.security.PublicKey,org.apache.sshd.server.session.ServerSession)
meth public java.lang.Object getId()
meth public java.lang.String toString()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds id,resolvedKeys

CLSS public org.apache.sshd.server.auth.pubkey.CachingPublicKeyAuthenticator
cons public init(org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator)
fld protected final org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator authenticator
fld public final static org.apache.sshd.common.AttributeRepository$AttributeKey<java.util.Map<java.security.PublicKey,java.lang.Boolean>> CACHE_ATTRIBUTE
intf org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator
meth protected java.util.Map<java.security.PublicKey,java.lang.Boolean> resolveCachedResults(java.lang.String,java.security.PublicKey,org.apache.sshd.server.session.ServerSession)
meth public boolean authenticate(java.lang.String,java.security.PublicKey,org.apache.sshd.server.session.ServerSession)
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public org.apache.sshd.server.auth.pubkey.KeySetPublickeyAuthenticator
cons public init(java.lang.Object,java.util.Collection<? extends java.security.PublicKey>)
intf org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator
meth public boolean authenticate(java.lang.String,java.security.PublicKey,org.apache.sshd.server.session.ServerSession)
meth public boolean authenticate(java.lang.String,java.security.PublicKey,org.apache.sshd.server.session.ServerSession,java.util.Collection<? extends java.security.PublicKey>)
meth public final java.util.Collection<? extends java.security.PublicKey> getKeySet()
meth public java.lang.Object getId()
meth public java.lang.String toString()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds id,keySet

CLSS public abstract interface org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator
 anno 0 java.lang.FunctionalInterface()
meth public abstract boolean authenticate(java.lang.String,java.security.PublicKey,org.apache.sshd.server.session.ServerSession)
meth public static org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator fromAuthorizedEntries(java.lang.Object,org.apache.sshd.server.session.ServerSession,java.util.Collection<? extends org.apache.sshd.common.config.keys.AuthorizedKeyEntry>,org.apache.sshd.common.config.keys.PublicKeyEntryResolver) throws java.io.IOException,java.security.GeneralSecurityException

CLSS public final org.apache.sshd.server.auth.pubkey.RejectAllPublickeyAuthenticator
fld public final static org.apache.sshd.server.auth.pubkey.RejectAllPublickeyAuthenticator INSTANCE
supr org.apache.sshd.server.auth.pubkey.StaticPublickeyAuthenticator

CLSS public abstract org.apache.sshd.server.auth.pubkey.StaticPublickeyAuthenticator
cons protected init(boolean)
intf org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator
meth protected void handleAcceptance(java.lang.String,java.security.PublicKey,org.apache.sshd.server.session.ServerSession)
meth protected void handleRejection(java.lang.String,java.security.PublicKey,org.apache.sshd.server.session.ServerSession)
meth public final boolean authenticate(java.lang.String,java.security.PublicKey,org.apache.sshd.server.session.ServerSession)
meth public final boolean isAccepted()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds acceptance

CLSS public org.apache.sshd.server.auth.pubkey.UserAuthPublicKey
cons public init()
cons public init(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
fld public final static java.lang.String NAME = "publickey"
intf org.apache.sshd.common.signature.SignatureFactoriesManager
meth protected boolean verifySignature(org.apache.sshd.server.session.ServerSession,java.lang.String,java.lang.String,java.security.PublicKey,org.apache.sshd.common.util.buffer.Buffer,org.apache.sshd.common.signature.Signature,byte[]) throws java.lang.Exception
meth protected void sendPublicKeyResponse(org.apache.sshd.server.session.ServerSession,java.lang.String,java.lang.String,java.security.PublicKey,byte[],int,int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void verifyCertificateSignature(org.apache.sshd.server.session.ServerSession,org.apache.sshd.common.config.keys.OpenSshCertificate) throws java.lang.Exception
meth protected void verifyCertificateSources(org.apache.sshd.server.session.ServerSession,org.apache.sshd.common.config.keys.OpenSshCertificate) throws java.security.cert.CertificateException
meth public java.lang.Boolean doAuth(org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.lang.Exception
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories()
meth public void setSignatureFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
supr org.apache.sshd.server.auth.AbstractUserAuth
hfds factories

CLSS public org.apache.sshd.server.auth.pubkey.UserAuthPublicKeyFactory
cons public init()
cons public init(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
fld public final static java.lang.String NAME = "publickey"
fld public final static org.apache.sshd.server.auth.pubkey.UserAuthPublicKeyFactory INSTANCE
intf org.apache.sshd.common.signature.SignatureFactoriesManager
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories()
meth public org.apache.sshd.server.auth.pubkey.UserAuthPublicKey createUserAuth(org.apache.sshd.server.session.ServerSession) throws java.io.IOException
meth public void setSignatureFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
supr org.apache.sshd.server.auth.AbstractUserAuthFactory
hfds factories

CLSS public abstract org.apache.sshd.server.channel.AbstractServerChannel
cons protected init(java.lang.String,java.util.Collection<? extends org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.channel.Channel>>,org.apache.sshd.common.util.threads.CloseableExecutorService)
cons protected init(org.apache.sshd.common.util.threads.CloseableExecutorService)
fld protected final java.util.concurrent.atomic.AtomicBoolean exitStatusSent
intf org.apache.sshd.server.channel.ServerChannel
meth protected org.apache.sshd.client.future.OpenFuture doInit(org.apache.sshd.common.util.buffer.Buffer)
meth protected void sendExitStatus(int) throws java.io.IOException
meth public org.apache.sshd.client.future.OpenFuture open(long,long,long,org.apache.sshd.common.util.buffer.Buffer)
meth public void handleOpenFailure(org.apache.sshd.common.util.buffer.Buffer)
meth public void handleOpenSuccess(long,long,long,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
supr org.apache.sshd.common.channel.AbstractChannel

CLSS public org.apache.sshd.server.channel.AsyncDataReceiver
cons public init(org.apache.sshd.common.channel.Channel)
intf org.apache.sshd.server.channel.ChannelDataReceiver
meth public int data(org.apache.sshd.server.channel.ChannelSession,byte[],int,int) throws java.io.IOException
meth public org.apache.sshd.common.io.IoInputStream getIn()
meth public void close() throws java.io.IOException
supr java.lang.Object
hfds in

CLSS public abstract interface org.apache.sshd.server.channel.ChannelDataReceiver
intf java.io.Closeable
meth public abstract int data(org.apache.sshd.server.channel.ChannelSession,byte[],int,int) throws java.io.IOException

CLSS public org.apache.sshd.server.channel.ChannelSession
cons public init()
cons public init(java.util.Collection<? extends org.apache.sshd.common.channel.RequestHandler<org.apache.sshd.common.channel.Channel>>)
fld protected final java.util.concurrent.atomic.AtomicBoolean commandStarted
fld protected final org.apache.sshd.common.future.CloseFuture commandExitFuture
fld protected final org.apache.sshd.server.StandardEnvironment env
fld protected java.io.OutputStream err
fld protected java.io.OutputStream out
fld protected java.lang.String type
fld protected org.apache.sshd.common.channel.ChannelAsyncOutputStream asyncErr
fld protected org.apache.sshd.common.channel.ChannelAsyncOutputStream asyncOut
fld protected org.apache.sshd.common.util.buffer.Buffer extendedDataBuffer
fld protected org.apache.sshd.common.util.buffer.Buffer receiverBuffer
fld protected org.apache.sshd.server.channel.ChannelDataReceiver extendedDataWriter
fld protected org.apache.sshd.server.channel.ChannelDataReceiver receiver
fld protected org.apache.sshd.server.command.Command commandInstance
fld public final static java.util.List<org.apache.sshd.common.channel.ChannelRequestHandler> DEFAULT_HANDLERS
innr public CommandCloseable
meth protected boolean isValidXauth(java.lang.String)
meth protected boolean mayWrite()
meth protected int getPtyModeValue(org.apache.sshd.common.channel.PtyMode)
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleAgentForwarding(java.lang.String,org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleAgentForwardingParsed(java.lang.String) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleBreak(org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleBreakParsed(long) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleEnv(org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleEnvParsed(java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleExec(java.lang.String,org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleExecParsed(java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleInternalRequest(java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handlePtyReq(org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handlePtyReqParsed(java.lang.String,int,int,int,int,java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer>) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleShell(java.lang.String,org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleShellParsed(java.lang.String) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleSignal(org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleSignalParsed(java.lang.String) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleSubsystem(java.lang.String,org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleSubsystemParsed(java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleWindowChange(org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleWindowChangeParsed(int,int,int,int) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleX11Forwarding(java.lang.String,org.apache.sshd.common.util.buffer.Buffer,boolean) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleX11ForwardingParsed(java.lang.String,org.apache.sshd.server.session.ServerSession,boolean,java.lang.String,java.lang.String,int) throws java.io.IOException
meth protected org.apache.sshd.common.channel.RequestHandler$Result prepareChannelCommand(java.lang.String,org.apache.sshd.server.command.Command) throws java.io.IOException
meth protected org.apache.sshd.common.io.IoWriteFuture sendResponse(org.apache.sshd.common.util.buffer.Buffer,java.lang.String,org.apache.sshd.common.channel.RequestHandler$Result,boolean) throws java.io.IOException
meth protected org.apache.sshd.server.command.Command prepareCommand(java.lang.String,org.apache.sshd.server.command.Command) throws java.io.IOException
meth protected void addEnvVariable(java.lang.String,java.lang.String)
meth protected void closeImmediately0()
meth protected void closeShell(int,boolean) throws java.io.IOException
meth protected void doWriteData(byte[],int,long) throws java.io.IOException
meth protected void doWriteExtendedData(byte[],int,long) throws java.io.IOException
meth public org.apache.sshd.server.StandardEnvironment getEnvironment()
meth public org.apache.sshd.server.session.ServerSession getSession()
meth public void handleEof() throws java.io.IOException
meth public void handleWindowAdjust(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void setDataReceiver(org.apache.sshd.server.channel.ChannelDataReceiver)
meth public void setExtendedDataWriter(org.apache.sshd.server.channel.ChannelDataReceiver)
supr org.apache.sshd.server.channel.AbstractServerChannel

CLSS public org.apache.sshd.server.channel.ChannelSession$CommandCloseable
 outer org.apache.sshd.server.channel.ChannelSession
cons public init(org.apache.sshd.server.channel.ChannelSession)
meth public boolean isClosed()
meth public boolean isClosing()
meth public org.apache.sshd.common.future.CloseFuture close(boolean)
meth public void addCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
meth public void removeCloseFutureListener(org.apache.sshd.common.future.SshFutureListener<org.apache.sshd.common.future.CloseFuture>)
supr org.apache.sshd.common.util.closeable.IoBaseCloseable

CLSS public abstract interface org.apache.sshd.server.channel.ChannelSessionAware
 anno 0 java.lang.FunctionalInterface()
meth public abstract void setChannelSession(org.apache.sshd.server.channel.ChannelSession)

CLSS public org.apache.sshd.server.channel.ChannelSessionFactory
cons public init()
fld public final static org.apache.sshd.server.channel.ChannelSessionFactory INSTANCE
intf org.apache.sshd.common.channel.ChannelFactory
meth public java.lang.String getName()
meth public org.apache.sshd.common.channel.Channel createChannel(org.apache.sshd.common.session.Session) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.sshd.server.channel.PipeDataReceiver
cons public init(org.apache.sshd.common.PropertyResolver,org.apache.sshd.common.channel.LocalWindow)
intf org.apache.sshd.server.channel.ChannelDataReceiver
meth public int data(org.apache.sshd.server.channel.ChannelSession,byte[],int,int) throws java.io.IOException
meth public java.io.InputStream getIn()
meth public void close() throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds in,out

CLSS public org.apache.sshd.server.channel.PuttyRequestHandler
cons public init()
fld public final static java.lang.String REQUEST_SUFFIX = "@putty.projects.tartarus.org"
fld public final static java.util.Set<org.apache.sshd.common.channel.PtyMode> PUTTY_OPTIONS
fld public final static org.apache.sshd.server.channel.PuttyRequestHandler INSTANCE
meth protected org.apache.sshd.common.channel.RequestHandler$Result processPuttyOpcode(org.apache.sshd.common.channel.Channel,java.lang.String,java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public org.apache.sshd.common.channel.RequestHandler$Result process(org.apache.sshd.common.channel.Channel,java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public static boolean isPuttyClient(java.lang.String)
meth public static boolean isPuttyClient(org.apache.sshd.common.session.Session)
meth public static boolean isPuttyRequest(java.lang.String)
meth public static java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer> resolveShellTtyOptions(java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer>)
supr org.apache.sshd.common.channel.AbstractChannelRequestHandler

CLSS public abstract interface org.apache.sshd.server.channel.ServerChannel
intf org.apache.sshd.common.channel.Channel
intf org.apache.sshd.server.session.ServerSessionHolder
meth public org.apache.sshd.server.session.ServerSession getServerSession()

CLSS public abstract interface org.apache.sshd.server.channel.ServerChannelSessionHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.server.channel.ChannelSession getServerChannelSession()

CLSS public abstract org.apache.sshd.server.command.AbstractCommandSupport
cons protected init(java.lang.String,org.apache.sshd.common.util.threads.CloseableExecutorService)
fld protected boolean cbCalled
fld protected org.apache.sshd.common.util.threads.CloseableExecutorService executorService
fld protected volatile java.lang.Thread cmdRunner
intf java.lang.Runnable
intf org.apache.sshd.common.session.SessionHolder<org.apache.sshd.server.session.ServerSession>
intf org.apache.sshd.common.util.threads.ExecutorServiceCarrier
intf org.apache.sshd.server.command.Command
intf org.apache.sshd.server.session.ServerSessionAware
intf org.apache.sshd.server.session.ServerSessionHolder
meth protected java.util.concurrent.Future<?> getStartedCommandFuture()
meth protected void onExit(int)
meth protected void onExit(int,java.lang.String)
meth public java.io.InputStream getInputStream()
meth public java.io.OutputStream getErrorStream()
meth public java.io.OutputStream getOutputStream()
meth public java.lang.String getCommand()
meth public java.lang.String toString()
meth public org.apache.sshd.common.util.threads.CloseableExecutorService getExecutorService()
meth public org.apache.sshd.server.Environment getEnvironment()
meth public org.apache.sshd.server.ExitCallback getExitCallback()
meth public org.apache.sshd.server.session.ServerSession getServerSession()
meth public org.apache.sshd.server.session.ServerSession getSession()
meth public void destroy(org.apache.sshd.server.channel.ChannelSession) throws java.lang.Exception
meth public void setErrorStream(java.io.OutputStream)
meth public void setExitCallback(org.apache.sshd.server.ExitCallback)
meth public void setInputStream(java.io.InputStream)
meth public void setOutputStream(java.io.OutputStream)
meth public void setSession(org.apache.sshd.server.session.ServerSession)
meth public void start(org.apache.sshd.server.channel.ChannelSession,org.apache.sshd.server.Environment) throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds callback,cmdFuture,command,environment,err,in,out,serverSession

CLSS public abstract org.apache.sshd.server.command.AbstractDelegatingCommandFactory
cons protected init(java.lang.String)
intf org.apache.sshd.server.command.CommandFactory
meth protected abstract org.apache.sshd.server.command.Command executeSupportedCommand(org.apache.sshd.server.channel.ChannelSession,java.lang.String)
meth protected org.apache.sshd.server.command.Command createUnsupportedCommand(org.apache.sshd.server.channel.ChannelSession,java.lang.String)
meth public abstract boolean isSupportedCommand(org.apache.sshd.server.channel.ChannelSession,java.lang.String)
meth public java.lang.String toString()
meth public org.apache.sshd.server.command.Command createCommand(org.apache.sshd.server.channel.ChannelSession,java.lang.String) throws java.io.IOException
meth public org.apache.sshd.server.command.CommandFactory getDelegateCommandFactory()
meth public void setDelegateCommandFactory(org.apache.sshd.server.command.CommandFactory)
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds delegate,name

CLSS public abstract org.apache.sshd.server.command.AbstractFileSystemCommand
cons protected init(java.lang.String,org.apache.sshd.common.util.threads.CloseableExecutorService)
fld protected java.nio.file.FileSystem fileSystem
intf org.apache.sshd.common.file.FileSystemAware
meth public java.nio.file.FileSystem getFileSystem()
meth public void destroy(org.apache.sshd.server.channel.ChannelSession) throws java.lang.Exception
meth public void setFileSystem(java.nio.file.FileSystem)
supr org.apache.sshd.server.command.AbstractCommandSupport

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

CLSS public abstract interface org.apache.sshd.server.command.CommandFactory
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.server.command.Command createCommand(org.apache.sshd.server.channel.ChannelSession,java.lang.String) throws java.io.IOException
meth public static java.util.List<java.lang.String> split(java.lang.String)

CLSS public abstract interface org.apache.sshd.server.command.CommandLifecycle
meth public abstract void destroy(org.apache.sshd.server.channel.ChannelSession) throws java.lang.Exception
meth public abstract void start(org.apache.sshd.server.channel.ChannelSession,org.apache.sshd.server.Environment) throws java.io.IOException

CLSS public abstract !enum org.apache.sshd.server.config.AllowTcpForwardingValue
fld public final static java.util.Set<org.apache.sshd.server.config.AllowTcpForwardingValue> VALUES
fld public final static org.apache.sshd.server.config.AllowTcpForwardingValue ALL
fld public final static org.apache.sshd.server.config.AllowTcpForwardingValue LOCAL
fld public final static org.apache.sshd.server.config.AllowTcpForwardingValue NONE
fld public final static org.apache.sshd.server.config.AllowTcpForwardingValue REMOTE
intf org.apache.sshd.server.forward.TcpForwardingFilter
meth public static org.apache.sshd.server.config.AllowTcpForwardingValue fromString(java.lang.String)
meth public static org.apache.sshd.server.config.AllowTcpForwardingValue valueOf(java.lang.String)
meth public static org.apache.sshd.server.config.AllowTcpForwardingValue[] values()
supr java.lang.Enum<org.apache.sshd.server.config.AllowTcpForwardingValue>

CLSS public final org.apache.sshd.server.config.SshServerConfigFileReader
fld public final static org.apache.sshd.common.Property<java.lang.Integer> SFTP_FORCED_VERSION_PROP
fld public final static org.apache.sshd.common.Property<java.lang.String> ALLOW_AGENT_FORWARDING_CONFIG_PROP
fld public final static org.apache.sshd.common.Property<java.lang.String> ALLOW_TCP_FORWARDING_CONFIG_PROP
fld public final static org.apache.sshd.common.Property<java.lang.String> ALLOW_X11_FORWARDING_CONFIG_PROP
fld public final static org.apache.sshd.common.Property<java.lang.String> BANNER_CONFIG_PROP
fld public final static org.apache.sshd.common.Property<java.lang.String> VISUAL_HOST_KEY
fld public final static org.apache.sshd.common.Property<java.time.Duration> SERVER_ALIVE_INTERVAL_PROP
meth public static <%0 extends org.apache.sshd.server.ServerFactoryManager> {%%0} setupServerHeartbeat({%%0},java.util.Map<java.lang.String,?>)
meth public static <%0 extends org.apache.sshd.server.ServerFactoryManager> {%%0} setupServerHeartbeat({%%0},org.apache.sshd.common.PropertyResolver)
meth public static <%0 extends org.apache.sshd.server.ServerFactoryManager> {%%0} setupSftpSubsystem({%%0},org.apache.sshd.common.PropertyResolver)
meth public static <%0 extends org.apache.sshd.server.SshServer> {%%0} configure({%%0},org.apache.sshd.common.PropertyResolver,boolean,boolean)
meth public static java.lang.Object resolveBanner(org.apache.sshd.common.PropertyResolver)
meth public static org.apache.sshd.server.forward.AgentForwardingFilter resolveAgentForwardingFilter(org.apache.sshd.common.PropertyResolver)
meth public static org.apache.sshd.server.forward.ForwardingFilter resolveServerForwarding(org.apache.sshd.common.PropertyResolver)
meth public static org.apache.sshd.server.forward.TcpForwardingFilter resolveTcpForwardingFilter(org.apache.sshd.common.PropertyResolver)
meth public static org.apache.sshd.server.forward.X11ForwardingFilter resolveX11ForwardingFilter(org.apache.sshd.common.PropertyResolver)
supr java.lang.Object

CLSS public org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator
cons public !varargs init(java.nio.file.Path,java.nio.file.LinkOption[])
cons public init(java.nio.file.Path)
fld public final static java.lang.String STD_AUTHORIZED_KEYS_FILENAME = "authorized_keys"
intf org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator
meth protected boolean isValidUsername(java.lang.String,org.apache.sshd.server.session.ServerSession)
meth protected java.util.Collection<org.apache.sshd.common.config.keys.AuthorizedKeyEntry> reloadAuthorizedKeys(java.nio.file.Path,java.lang.String,org.apache.sshd.server.session.ServerSession) throws java.io.IOException,java.security.GeneralSecurityException
meth protected org.apache.sshd.common.config.keys.PublicKeyEntryResolver getFallbackPublicKeyEntryResolver()
meth protected org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator createDelegateAuthenticator(java.lang.String,org.apache.sshd.server.session.ServerSession,java.nio.file.Path,java.util.Collection<org.apache.sshd.common.config.keys.AuthorizedKeyEntry>,org.apache.sshd.common.config.keys.PublicKeyEntryResolver) throws java.io.IOException,java.security.GeneralSecurityException
meth protected org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator resolvePublickeyAuthenticator(java.lang.String,org.apache.sshd.server.session.ServerSession) throws java.io.IOException,java.security.GeneralSecurityException
meth public !varargs static java.util.List<org.apache.sshd.common.config.keys.AuthorizedKeyEntry> readDefaultAuthorizedKeys(java.nio.file.OpenOption[]) throws java.io.IOException
meth public boolean authenticate(java.lang.String,java.security.PublicKey,org.apache.sshd.server.session.ServerSession)
meth public static java.nio.file.Path getDefaultAuthorizedKeysFile()
supr org.apache.sshd.common.util.io.ModifiableFileWatcher
hfds delegateHolder
hcls LazyDefaultAuthorizedKeysFileHolder

CLSS public org.apache.sshd.server.config.keys.DefaultAuthorizedKeysAuthenticator
cons public !varargs init(java.lang.String,java.nio.file.Path,boolean,java.nio.file.LinkOption[])
cons public !varargs init(java.nio.file.Path,boolean,java.nio.file.LinkOption[])
cons public init(boolean)
cons public init(java.lang.String,boolean)
fld public final static org.apache.sshd.server.config.keys.DefaultAuthorizedKeysAuthenticator INSTANCE
intf org.apache.sshd.common.auth.UsernameHolder
meth protected boolean isValidUsername(java.lang.String,org.apache.sshd.server.session.ServerSession)
meth protected java.nio.file.Path validateFilePath(java.nio.file.Path,java.util.Collection<java.nio.file.attribute.PosixFilePermission>,java.util.Collection<java.nio.file.attribute.PosixFilePermission>) throws java.io.IOException
meth protected java.util.Collection<org.apache.sshd.common.config.keys.AuthorizedKeyEntry> reloadAuthorizedKeys(java.nio.file.Path,java.lang.String,org.apache.sshd.server.session.ServerSession) throws java.io.IOException,java.security.GeneralSecurityException
meth public final boolean isStrict()
meth public final java.lang.String getUsername()
supr org.apache.sshd.server.config.keys.AuthorizedKeysAuthenticator
hfds strict,user

CLSS public final org.apache.sshd.server.config.keys.ServerIdentity
fld public final static java.lang.String HOST_CERT_CONFIG_PROP = "HostCertificate"
fld public final static java.lang.String HOST_KEY_CONFIG_PROP = "HostKey"
fld public final static java.lang.String ID_FILE_PREFIX = "ssh_host_"
fld public final static java.lang.String ID_FILE_SUFFIX = "_key"
fld public final static java.util.function.Function<java.lang.String,java.lang.String> ID_GENERATOR
meth public !varargs static java.util.Map<java.lang.String,java.nio.file.Path> findCertificates(java.util.Properties,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs static java.util.Map<java.lang.String,java.nio.file.Path> findIdentities(java.util.Properties,java.nio.file.LinkOption[]) throws java.io.IOException
meth public !varargs static java.util.Map<java.lang.String,java.security.KeyPair> loadIdentities(java.util.Properties,java.nio.file.LinkOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public !varargs static org.apache.sshd.common.keyprovider.KeyPairProvider loadKeyPairProvider(java.util.Properties,boolean,java.nio.file.LinkOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth public static <%0 extends org.apache.sshd.server.SshServer> {%%0} setKeyPairProvider({%%0},java.util.Properties,boolean) throws java.io.IOException,java.security.GeneralSecurityException
meth public static java.lang.String getIdentityFileName(java.lang.String)
meth public static java.lang.String getIdentityFileName(org.apache.sshd.common.NamedResource)
meth public static java.lang.String getIdentityType(java.lang.String)
supr java.lang.Object

CLSS public org.apache.sshd.server.forward.AcceptAllForwardingFilter
cons public init()
fld public final static org.apache.sshd.server.forward.AcceptAllForwardingFilter INSTANCE
supr org.apache.sshd.server.forward.StaticDecisionForwardingFilter

CLSS public abstract interface org.apache.sshd.server.forward.AgentForwardingFilter
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.server.forward.AgentForwardingFilter DEFAULT
meth public abstract boolean canForwardAgent(org.apache.sshd.common.session.Session,java.lang.String)
meth public static org.apache.sshd.server.forward.AgentForwardingFilter of(boolean)

CLSS public org.apache.sshd.server.forward.DirectTcpipFactory
cons public init()
fld public final static org.apache.sshd.server.forward.DirectTcpipFactory INSTANCE
supr org.apache.sshd.server.forward.TcpipServerChannel$TcpipFactory

CLSS public org.apache.sshd.server.forward.ForwardedTcpipFactory
cons public init()
fld public final static org.apache.sshd.server.forward.ForwardedTcpipFactory INSTANCE
supr org.apache.sshd.server.forward.TcpipServerChannel$TcpipFactory

CLSS public abstract interface org.apache.sshd.server.forward.ForwardingFilter
intf org.apache.sshd.server.forward.AgentForwardingFilter
intf org.apache.sshd.server.forward.TcpForwardingFilter
intf org.apache.sshd.server.forward.X11ForwardingFilter
meth public static org.apache.sshd.server.forward.ForwardingFilter asForwardingFilter(org.apache.sshd.server.forward.AgentForwardingFilter,org.apache.sshd.server.forward.X11ForwardingFilter,org.apache.sshd.server.forward.TcpForwardingFilter)

CLSS public org.apache.sshd.server.forward.RejectAllForwardingFilter
cons public init()
fld public final static org.apache.sshd.server.forward.RejectAllForwardingFilter INSTANCE
supr org.apache.sshd.server.forward.StaticDecisionForwardingFilter

CLSS public org.apache.sshd.server.forward.StaticDecisionForwardingFilter
cons public init(boolean)
intf org.apache.sshd.server.forward.ForwardingFilter
meth protected boolean checkAcceptance(java.lang.String,org.apache.sshd.common.session.Session,org.apache.sshd.common.util.net.SshdSocketAddress)
meth public boolean canConnect(org.apache.sshd.server.forward.TcpForwardingFilter$Type,org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.session.Session)
meth public boolean canForwardAgent(org.apache.sshd.common.session.Session,java.lang.String)
meth public boolean canForwardX11(org.apache.sshd.common.session.Session,java.lang.String)
meth public boolean canListen(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.session.Session)
meth public final boolean isAccepted()
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds acceptance

CLSS public abstract interface org.apache.sshd.server.forward.TcpForwardingFilter
fld public final static org.apache.sshd.server.forward.TcpForwardingFilter DEFAULT
innr public final static !enum Type
meth public abstract boolean canConnect(org.apache.sshd.server.forward.TcpForwardingFilter$Type,org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.session.Session)
meth public abstract boolean canListen(org.apache.sshd.common.util.net.SshdSocketAddress,org.apache.sshd.common.session.Session)

CLSS public final static !enum org.apache.sshd.server.forward.TcpForwardingFilter$Type
 outer org.apache.sshd.server.forward.TcpForwardingFilter
fld public final static java.util.Set<org.apache.sshd.server.forward.TcpForwardingFilter$Type> VALUES
fld public final static org.apache.sshd.server.forward.TcpForwardingFilter$Type Direct
fld public final static org.apache.sshd.server.forward.TcpForwardingFilter$Type Forwarded
intf org.apache.sshd.common.NamedResource
meth public final java.lang.String getName()
meth public static org.apache.sshd.server.forward.TcpForwardingFilter$Type fromEnumName(java.lang.String)
meth public static org.apache.sshd.server.forward.TcpForwardingFilter$Type fromName(java.lang.String)
meth public static org.apache.sshd.server.forward.TcpForwardingFilter$Type fromString(java.lang.String)
meth public static org.apache.sshd.server.forward.TcpForwardingFilter$Type valueOf(java.lang.String)
meth public static org.apache.sshd.server.forward.TcpForwardingFilter$Type[] values()
supr java.lang.Enum<org.apache.sshd.server.forward.TcpForwardingFilter$Type>
hfds name

CLSS public org.apache.sshd.server.forward.TcpipServerChannel
cons public init(org.apache.sshd.server.forward.TcpForwardingFilter$Type,org.apache.sshd.common.util.threads.CloseableExecutorService)
innr public abstract static TcpipFactory
intf org.apache.sshd.common.forward.ForwardingTunnelEndpointsProvider
meth protected boolean mayWrite()
meth protected org.apache.sshd.client.future.OpenFuture doInit(org.apache.sshd.common.util.buffer.Buffer)
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected org.apache.sshd.common.forward.ChannelToPortHandler createChannelToPortHandler(org.apache.sshd.common.io.IoSession)
meth protected void doWriteData(byte[],int,long) throws java.io.IOException
meth protected void doWriteExtendedData(byte[],int,long) throws java.io.IOException
meth protected void handleChannelConnectResult(org.apache.sshd.client.future.OpenFuture,org.apache.sshd.common.io.IoConnectFuture)
meth protected void handleChannelOpenFailure(org.apache.sshd.client.future.OpenFuture,java.lang.Throwable)
meth protected void handleChannelOpenSuccess(org.apache.sshd.client.future.OpenFuture,org.apache.sshd.common.io.IoSession)
meth public java.net.SocketAddress getLocalAddress()
meth public org.apache.sshd.common.forward.ChannelToPortHandler getPort()
meth public org.apache.sshd.common.util.net.SshdSocketAddress getOriginatorAddress()
meth public org.apache.sshd.common.util.net.SshdSocketAddress getTunnelEntrance()
meth public org.apache.sshd.common.util.net.SshdSocketAddress getTunnelExit()
meth public org.apache.sshd.server.forward.TcpForwardingFilter$Type getTcpipChannelType()
meth public void handleEof() throws java.io.IOException
meth public void handleWindowAdjust(org.apache.sshd.common.util.buffer.Buffer) throws java.io.IOException
meth public void setLocalAddress(java.net.SocketAddress)
supr org.apache.sshd.server.channel.AbstractServerChannel
hfds connector,localAddress,originatorAddress,out,port,tunnelEntrance,tunnelExit,type
hcls PortIoHandler

CLSS public abstract static org.apache.sshd.server.forward.TcpipServerChannel$TcpipFactory
 outer org.apache.sshd.server.forward.TcpipServerChannel
cons protected init(org.apache.sshd.server.forward.TcpForwardingFilter$Type)
intf org.apache.sshd.common.channel.ChannelFactory
intf org.apache.sshd.common.util.threads.ExecutorServiceCarrier
meth public final java.lang.String getName()
meth public final org.apache.sshd.server.forward.TcpForwardingFilter$Type getType()
meth public org.apache.sshd.common.channel.Channel createChannel(org.apache.sshd.common.session.Session) throws java.io.IOException
meth public org.apache.sshd.common.util.threads.CloseableExecutorService getExecutorService()
supr java.lang.Object
hfds type

CLSS public abstract interface org.apache.sshd.server.forward.X11ForwardingFilter
 anno 0 java.lang.FunctionalInterface()
fld public final static org.apache.sshd.server.forward.X11ForwardingFilter DEFAULT
meth public abstract boolean canForwardX11(org.apache.sshd.common.session.Session,java.lang.String)
meth public static org.apache.sshd.server.forward.X11ForwardingFilter of(boolean)

CLSS public org.apache.sshd.server.global.CancelTcpipForwardHandler
cons public init()
fld public final static java.lang.String REQUEST = "cancel-tcpip-forward"
fld public final static java.util.function.IntUnaryOperator RESPONSE_BUFFER_GROWTH_FACTOR
fld public final static org.apache.sshd.server.global.CancelTcpipForwardHandler INSTANCE
meth public org.apache.sshd.common.channel.RequestHandler$Result process(org.apache.sshd.common.session.ConnectionService,java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
supr org.apache.sshd.common.session.helpers.AbstractConnectionServiceRequestHandler

CLSS public org.apache.sshd.server.global.NoMoreSessionsHandler
cons public init()
fld public final static org.apache.sshd.server.global.NoMoreSessionsHandler INSTANCE
meth public org.apache.sshd.common.channel.RequestHandler$Result process(org.apache.sshd.common.session.ConnectionService,java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
supr org.apache.sshd.common.session.helpers.AbstractConnectionServiceRequestHandler

CLSS public org.apache.sshd.server.global.OpenSshHostKeysHandler
cons public init()
cons public init(org.apache.sshd.common.util.buffer.keys.BufferPublicKeyParser<? extends java.security.PublicKey>)
fld public final static java.lang.String REQUEST = "hostkeys-prove-00@openssh.com"
fld public final static org.apache.sshd.server.global.OpenSshHostKeysHandler INSTANCE
intf org.apache.sshd.common.signature.SignatureFactoriesManager
meth protected org.apache.sshd.common.channel.RequestHandler$Result handleHostKeys(org.apache.sshd.common.session.Session,java.util.Collection<? extends java.security.PublicKey>,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>> getSignatureFactories()
meth public void setSignatureFactories(java.util.List<org.apache.sshd.common.NamedFactory<org.apache.sshd.common.signature.Signature>>)
supr org.apache.sshd.common.global.AbstractOpenSshHostKeysHandler
hfds factories

CLSS public org.apache.sshd.server.global.TcpipForwardHandler
cons public init()
fld public final static java.lang.String REQUEST = "tcpip-forward"
fld public final static java.util.function.IntUnaryOperator RESPONSE_BUFFER_GROWTH_FACTOR
fld public final static org.apache.sshd.server.global.TcpipForwardHandler INSTANCE
meth public org.apache.sshd.common.channel.RequestHandler$Result process(org.apache.sshd.common.session.ConnectionService,java.lang.String,boolean,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
supr org.apache.sshd.common.session.helpers.AbstractConnectionServiceRequestHandler

CLSS public org.apache.sshd.server.jaas.JaasPasswordAuthenticator
cons public init()
cons public init(java.lang.String)
intf org.apache.sshd.server.auth.password.PasswordAuthenticator
meth public boolean authenticate(java.lang.String,java.lang.String,org.apache.sshd.server.session.ServerSession)
meth public java.lang.String getDomain()
meth public void setDomain(java.lang.String)
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds domain

CLSS public abstract org.apache.sshd.server.kex.AbstractDHServerKeyExchange
cons protected init(org.apache.sshd.common.session.Session)
intf org.apache.sshd.server.session.ServerSessionHolder
meth public final org.apache.sshd.server.session.ServerSession getServerSession()
supr org.apache.sshd.common.kex.dh.AbstractDHKeyExchange

CLSS public org.apache.sshd.server.kex.DHGEXServer
cons protected init(org.apache.sshd.common.kex.DHFactory,org.apache.sshd.common.session.Session)
fld protected boolean oldRequest
fld protected byte expected
fld protected final org.apache.sshd.common.kex.DHFactory factory
fld protected int max
fld protected int min
fld protected int prf
fld protected org.apache.sshd.common.kex.DHG dh
meth protected java.util.List<org.apache.sshd.server.kex.Moduli$DhGroup> loadModuliGroups(org.apache.sshd.server.session.ServerSession) throws java.io.IOException
meth protected java.util.List<org.apache.sshd.server.kex.Moduli$DhGroup> selectModuliGroups(org.apache.sshd.server.session.ServerSession,int,int,int,java.util.List<org.apache.sshd.server.kex.Moduli$DhGroup>) throws java.lang.Exception
meth protected org.apache.sshd.common.kex.DHG chooseDH(int,int,int) throws java.lang.Exception
meth protected org.apache.sshd.common.kex.DHG getDH(java.math.BigInteger,java.math.BigInteger) throws java.lang.Exception
meth public boolean next(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public final java.lang.String getName()
meth public static org.apache.sshd.common.kex.KeyExchangeFactory newFactory(org.apache.sshd.common.kex.DHFactory)
meth public void init(byte[],byte[],byte[],byte[]) throws java.lang.Exception
supr org.apache.sshd.server.kex.AbstractDHServerKeyExchange

CLSS public org.apache.sshd.server.kex.DHGServer
cons protected init(org.apache.sshd.common.kex.DHFactory,org.apache.sshd.common.session.Session)
fld protected final org.apache.sshd.common.kex.DHFactory factory
fld protected org.apache.sshd.common.kex.AbstractDH dh
meth public boolean next(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public final java.lang.String getName()
meth public static org.apache.sshd.common.kex.KeyExchangeFactory newFactory(org.apache.sshd.common.kex.DHFactory)
meth public void init(byte[],byte[],byte[],byte[]) throws java.lang.Exception
supr org.apache.sshd.server.kex.AbstractDHServerKeyExchange

CLSS public final org.apache.sshd.server.kex.Moduli
fld public final static int MODULI_TESTS_COMPOSITE = 1
fld public final static int MODULI_TYPE_SAFE = 2
fld public final static java.lang.String INTERNAL_MODULI_RESPATH = "/org/apache/sshd/moduli"
innr public static DhGroup
meth public static java.util.List<org.apache.sshd.server.kex.Moduli$DhGroup> loadInternalModuli(java.net.URL) throws java.io.IOException
meth public static java.util.List<org.apache.sshd.server.kex.Moduli$DhGroup> parseModuli(java.io.BufferedReader) throws java.io.IOException
meth public static java.util.List<org.apache.sshd.server.kex.Moduli$DhGroup> parseModuli(java.io.InputStream) throws java.io.IOException
meth public static java.util.List<org.apache.sshd.server.kex.Moduli$DhGroup> parseModuli(java.io.Reader) throws java.io.IOException
meth public static java.util.List<org.apache.sshd.server.kex.Moduli$DhGroup> parseModuli(java.net.URL) throws java.io.IOException
meth public static java.util.Map$Entry<java.lang.String,java.util.List<org.apache.sshd.server.kex.Moduli$DhGroup>> clearInternalModuliCache()
supr java.lang.Object
hfds INTERNAL_MODULI_HOLDER

CLSS public static org.apache.sshd.server.kex.Moduli$DhGroup
 outer org.apache.sshd.server.kex.Moduli
cons public init(int,java.math.BigInteger,java.math.BigInteger)
meth public int getSize()
meth public java.lang.String toString()
meth public java.math.BigInteger getG()
meth public java.math.BigInteger getP()
supr java.lang.Object
hfds g,p,size

CLSS public abstract org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider
cons protected init()
fld public final static boolean DEFAULT_ALLOWED_TO_OVERWRITE = true
fld public final static java.lang.String DEFAULT_ALGORITHM = "EC"
intf org.apache.sshd.common.AlgorithmNameProvider
intf org.apache.sshd.common.keyprovider.KeySizeIndicator
meth protected !varargs java.lang.Iterable<java.security.KeyPair> readKeyPairs(org.apache.sshd.common.session.SessionContext,java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException,java.security.GeneralSecurityException
meth protected abstract void doWriteKeyPair(org.apache.sshd.common.NamedResource,java.security.KeyPair,java.io.OutputStream) throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.lang.Iterable<java.security.KeyPair> doReadKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.io.InputStream) throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.lang.Iterable<java.security.KeyPair> loadFromFile(org.apache.sshd.common.session.SessionContext,java.lang.String,java.nio.file.Path) throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.lang.Iterable<java.security.KeyPair> resolveKeyPairs(org.apache.sshd.common.session.SessionContext,java.nio.file.Path) throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.security.KeyPair generateKeyPair(java.lang.String) throws java.security.GeneralSecurityException
meth protected void setFilePermissions(java.nio.file.Path) throws java.io.IOException
meth protected void writeKeyPair(java.security.KeyPair,java.nio.file.Path) throws java.io.IOException,java.security.GeneralSecurityException
meth public boolean hasStrictFilePermissions()
meth public boolean isOverwriteAllowed()
meth public int getKeySize()
meth public java.lang.String getAlgorithm()
meth public java.nio.file.Path getPath()
meth public java.security.spec.AlgorithmParameterSpec getKeySpec()
meth public java.util.List<java.security.KeyPair> loadKeys(org.apache.sshd.common.session.SessionContext)
meth public void clearLoadedKeys()
meth public void setAlgorithm(java.lang.String)
meth public void setKeySize(int)
meth public void setKeySpec(java.security.spec.AlgorithmParameterSpec)
meth public void setOverwriteAllowed(boolean)
meth public void setPath(java.nio.file.Path)
meth public void setStrictFilePermissions(boolean)
supr org.apache.sshd.common.keyprovider.AbstractKeyPairProvider
hfds algorithm,enforceFilePermissions,keyPairHolder,keySize,keySpec,overwriteAllowed,path

CLSS public org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
cons public init()
cons public init(java.nio.file.Path)
meth protected java.lang.Iterable<java.security.KeyPair> doReadKeyPairs(org.apache.sshd.common.session.SessionContext,org.apache.sshd.common.NamedResource,java.io.InputStream) throws java.io.IOException,java.security.GeneralSecurityException
meth protected void doWriteKeyPair(org.apache.sshd.common.NamedResource,java.security.KeyPair,java.io.OutputStream) throws java.io.IOException,java.security.GeneralSecurityException
supr org.apache.sshd.server.keyprovider.AbstractGeneratorHostKeyProvider
hcls ValidatingObjectInputStream

CLSS public abstract org.apache.sshd.server.session.AbstractServerSession
cons protected init(org.apache.sshd.server.ServerFactoryManager,org.apache.sshd.common.io.IoSession)
intf org.apache.sshd.server.session.ServerSession
meth protected !varargs void setKexSeed(byte[])
meth protected boolean handleServiceRequest(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected boolean readIdentification(org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected byte[] sendKexInit(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>) throws java.lang.Exception
meth protected java.lang.String resolveAvailableSignaturesProposal(org.apache.sshd.common.FactoryManager) throws java.io.IOException,java.security.GeneralSecurityException
meth protected java.lang.String resolveEmptySignaturesProposal(java.lang.Iterable<java.lang.String>,java.lang.Iterable<java.lang.String>)
meth protected org.apache.sshd.common.io.IoWriteFuture sendServerIdentification(java.util.List<java.lang.String>) throws java.lang.Exception
meth protected org.apache.sshd.common.session.ConnectionService getConnectionService()
meth protected void checkKeys()
meth protected void handleServiceAccept(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void receiveKexInit(java.util.Map<org.apache.sshd.common.kex.KexProposalOption,java.lang.String>,byte[]) throws java.io.IOException
meth public int getActiveSessionCountForUser(java.lang.String)
meth public java.net.SocketAddress getClientAddress()
meth public java.security.KeyPair getHostKey()
meth public java.util.List<org.apache.sshd.server.auth.UserAuthFactory> getUserAuthFactories()
meth public long getId()
meth public org.apache.sshd.common.io.IoWriteFuture signalAuthenticationSuccess(java.lang.String,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public org.apache.sshd.common.keyprovider.HostKeyCertificateProvider getHostKeyCertificateProvider()
meth public org.apache.sshd.common.keyprovider.KeyPairProvider getKeyPairProvider()
meth public org.apache.sshd.server.ServerFactoryManager getFactoryManager()
meth public org.apache.sshd.server.auth.gss.GSSAuthenticator getGSSAuthenticator()
meth public org.apache.sshd.server.auth.hostbased.HostBasedAuthenticator getHostBasedAuthenticator()
meth public org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator getKeyboardInteractiveAuthenticator()
meth public org.apache.sshd.server.auth.password.PasswordAuthenticator getPasswordAuthenticator()
meth public org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator getPublickeyAuthenticator()
meth public org.apache.sshd.server.session.ServerProxyAcceptor getServerProxyAcceptor()
meth public void setClientAddress(java.net.SocketAddress)
meth public void setGSSAuthenticator(org.apache.sshd.server.auth.gss.GSSAuthenticator)
meth public void setHostBasedAuthenticator(org.apache.sshd.server.auth.hostbased.HostBasedAuthenticator)
meth public void setHostKeyCertificateProvider(org.apache.sshd.common.keyprovider.HostKeyCertificateProvider)
meth public void setKeyPairProvider(org.apache.sshd.common.keyprovider.KeyPairProvider)
meth public void setKeyboardInteractiveAuthenticator(org.apache.sshd.server.auth.keyboard.KeyboardInteractiveAuthenticator)
meth public void setPasswordAuthenticator(org.apache.sshd.server.auth.password.PasswordAuthenticator)
meth public void setPublickeyAuthenticator(org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator)
meth public void setServerProxyAcceptor(org.apache.sshd.server.session.ServerProxyAcceptor)
meth public void setUserAuthFactories(java.util.List<org.apache.sshd.server.auth.UserAuthFactory>)
meth public void startService(java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
supr org.apache.sshd.common.session.helpers.AbstractSession
hfds clientAddress,gssAuthenticator,hostBasedAuthenticator,hostKeyCertificateProvider,interactiveAuthenticator,keyPairProvider,passwordAuthenticator,proxyAcceptor,publickeyAuthenticator,userAuthFactories

CLSS public org.apache.sshd.server.session.ServerConnectionService
cons protected init(org.apache.sshd.server.session.AbstractServerSession) throws org.apache.sshd.common.SshException
intf org.apache.sshd.server.session.ServerSessionHolder
meth public final org.apache.sshd.server.session.ServerSession getServerSession()
meth public org.apache.sshd.server.session.AbstractServerSession getSession()
supr org.apache.sshd.common.session.helpers.AbstractConnectionService

CLSS public org.apache.sshd.server.session.ServerConnectionServiceFactory
cons public init()
fld public final static org.apache.sshd.server.session.ServerConnectionServiceFactory INSTANCE
intf org.apache.sshd.common.ServiceFactory
meth public java.lang.String getName()
meth public org.apache.sshd.common.Service create(org.apache.sshd.common.session.Session) throws java.io.IOException
supr org.apache.sshd.common.session.AbstractConnectionServiceFactory

CLSS public abstract interface org.apache.sshd.server.session.ServerProxyAcceptor
 anno 0 java.lang.FunctionalInterface()
meth public abstract boolean acceptServerProxyMetadata(org.apache.sshd.server.session.ServerSession,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception

CLSS public abstract interface org.apache.sshd.server.session.ServerProxyAcceptorHolder
meth public abstract org.apache.sshd.server.session.ServerProxyAcceptor getServerProxyAcceptor()
meth public abstract void setServerProxyAcceptor(org.apache.sshd.server.session.ServerProxyAcceptor)

CLSS public abstract interface org.apache.sshd.server.session.ServerSession
intf org.apache.sshd.common.session.Session
intf org.apache.sshd.server.ServerAuthenticationManager
intf org.apache.sshd.server.session.ServerProxyAcceptorHolder
meth public abstract int getActiveSessionCountForUser(java.lang.String)
meth public abstract java.net.SocketAddress getClientAddress()
meth public abstract java.security.KeyPair getHostKey()
meth public abstract org.apache.sshd.common.io.IoWriteFuture signalAuthenticationSuccess(java.lang.String,java.lang.String,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public abstract org.apache.sshd.server.ServerFactoryManager getFactoryManager()

CLSS public abstract interface org.apache.sshd.server.session.ServerSessionAware
 anno 0 java.lang.FunctionalInterface()
meth public abstract void setSession(org.apache.sshd.server.session.ServerSession)

CLSS public abstract interface org.apache.sshd.server.session.ServerSessionHolder
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.server.session.ServerSession getServerSession()

CLSS public org.apache.sshd.server.session.ServerSessionImpl
cons public init(org.apache.sshd.server.ServerFactoryManager,org.apache.sshd.common.io.IoSession) throws java.lang.Exception
meth public void start() throws java.lang.Exception
supr org.apache.sshd.server.session.AbstractServerSession

CLSS public org.apache.sshd.server.session.ServerUserAuthService
cons public init(org.apache.sshd.common.session.Session) throws java.io.IOException
intf org.apache.sshd.common.Service
intf org.apache.sshd.server.session.ServerSessionHolder
meth protected boolean handleUserAuthRequestMessage(org.apache.sshd.server.session.ServerSession,org.apache.sshd.common.util.buffer.Buffer,java.util.concurrent.atomic.AtomicReference<java.lang.Boolean>) throws java.lang.Exception
meth protected java.lang.String loadWelcomeBanner(org.apache.sshd.server.session.ServerSession,java.net.URL,java.nio.charset.Charset) throws java.io.IOException
meth protected java.lang.String resolveWelcomeBanner(org.apache.sshd.server.session.ServerSession) throws java.io.IOException
meth protected void asyncAuth(int,org.apache.sshd.common.util.buffer.Buffer,boolean)
meth protected void handleAuthenticationFailure(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleAuthenticationInProgress(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth protected void handleAuthenticationSuccess(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public java.util.Map<java.lang.String,java.lang.Object> getProperties()
meth public org.apache.sshd.common.io.IoWriteFuture sendWelcomeBanner(org.apache.sshd.server.session.ServerSession) throws java.io.IOException
meth public org.apache.sshd.server.ServerFactoryManager getFactoryManager()
meth public org.apache.sshd.server.auth.WelcomeBannerPhase getWelcomePhase()
meth public org.apache.sshd.server.session.ServerSession getServerSession()
meth public org.apache.sshd.server.session.ServerSession getSession()
meth public void process(int,org.apache.sshd.common.util.buffer.Buffer) throws java.lang.Exception
meth public void start()
supr org.apache.sshd.common.util.closeable.AbstractCloseable
hfds authMethod,authMethods,authService,authUserName,currentAuth,maxAuthRequests,nbAuthRequests,properties,serverSession,userAuthFactories,welcomePhase,welcomeSent

CLSS public org.apache.sshd.server.session.ServerUserAuthServiceFactory
cons public init()
fld public final static org.apache.sshd.server.session.ServerUserAuthServiceFactory INSTANCE
meth public org.apache.sshd.common.Service create(org.apache.sshd.common.session.Session) throws java.io.IOException
supr org.apache.sshd.common.auth.AbstractUserAuthServiceFactory

CLSS public org.apache.sshd.server.session.SessionFactory
cons public init(org.apache.sshd.server.ServerFactoryManager)
meth protected org.apache.sshd.server.session.ServerSessionImpl doCreateSession(org.apache.sshd.common.io.IoSession) throws java.lang.Exception
meth public final org.apache.sshd.server.ServerFactoryManager getServer()
supr org.apache.sshd.common.session.helpers.AbstractSessionFactory<org.apache.sshd.server.ServerFactoryManager,org.apache.sshd.server.session.ServerSessionImpl>

CLSS public org.apache.sshd.server.shell.AggregateShellFactory
cons public init(java.util.Collection<? extends org.apache.sshd.server.shell.ShellFactorySelector>)
cons public init(java.util.Collection<? extends org.apache.sshd.server.shell.ShellFactorySelector>,org.apache.sshd.server.shell.ShellFactory)
fld protected final java.util.Collection<? extends org.apache.sshd.server.shell.ShellFactorySelector> selectors
fld protected final org.apache.sshd.server.shell.ShellFactory defaultFactory
intf org.apache.sshd.server.shell.ShellFactory
intf org.apache.sshd.server.shell.ShellFactorySelector
meth public org.apache.sshd.server.command.Command createShell(org.apache.sshd.server.channel.ChannelSession) throws java.io.IOException
meth public org.apache.sshd.server.shell.ShellFactory selectShellFactory(org.apache.sshd.server.channel.ChannelSession) throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean

CLSS public org.apache.sshd.server.shell.InteractiveProcessShellFactory
cons public init()
fld public final static org.apache.sshd.server.shell.InteractiveProcessShellFactory INSTANCE
meth protected java.util.List<java.lang.String> resolveEffectiveCommand(org.apache.sshd.server.channel.ChannelSession,java.lang.String,java.util.List<java.lang.String>)
supr org.apache.sshd.server.shell.ProcessShellFactory

CLSS public abstract interface org.apache.sshd.server.shell.InvertedShell
intf org.apache.sshd.common.session.SessionHolder<org.apache.sshd.server.session.ServerSession>
intf org.apache.sshd.server.channel.ServerChannelSessionHolder
intf org.apache.sshd.server.command.CommandLifecycle
intf org.apache.sshd.server.session.ServerSessionAware
intf org.apache.sshd.server.session.ServerSessionHolder
meth public abstract boolean isAlive()
meth public abstract int exitValue()
meth public abstract java.io.InputStream getErrorStream()
meth public abstract java.io.InputStream getOutputStream()
meth public abstract java.io.OutputStream getInputStream()
meth public org.apache.sshd.server.session.ServerSession getSession()

CLSS public org.apache.sshd.server.shell.InvertedShellWrapper
cons public init(org.apache.sshd.server.shell.InvertedShell)
cons public init(org.apache.sshd.server.shell.InvertedShell,int)
cons public init(org.apache.sshd.server.shell.InvertedShell,java.util.concurrent.Executor,boolean,int)
intf org.apache.sshd.server.command.Command
intf org.apache.sshd.server.session.ServerSessionAware
meth protected boolean pumpStream(java.io.InputStream,java.io.OutputStream,byte[]) throws java.io.IOException
meth protected void pumpStreams()
meth public java.lang.String toString()
meth public void destroy(org.apache.sshd.server.channel.ChannelSession) throws java.lang.Exception
meth public void setErrorStream(java.io.OutputStream)
meth public void setExitCallback(org.apache.sshd.server.ExitCallback)
meth public void setInputStream(java.io.InputStream)
meth public void setOutputStream(java.io.OutputStream)
meth public void setSession(org.apache.sshd.server.session.ServerSession)
meth public void start(org.apache.sshd.server.channel.ChannelSession,org.apache.sshd.server.Environment) throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds bufferSize,callback,err,executor,in,out,pumpSleepTime,shell,shellErr,shellIn,shellOut,shutdownExecutor

CLSS public org.apache.sshd.server.shell.ProcessShell
cons public !varargs init(java.lang.String[])
cons public init(java.util.Collection<java.lang.String>)
intf org.apache.sshd.server.shell.InvertedShell
meth protected java.util.Map<java.lang.String,java.lang.String> resolveShellEnvironment(java.util.Map<java.lang.String,java.lang.String>)
meth protected java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer> resolveShellTtyOptions(java.util.Map<org.apache.sshd.common.channel.PtyMode,java.lang.Integer>)
meth public boolean isAlive()
meth public int exitValue()
meth public java.io.InputStream getErrorStream()
meth public java.io.InputStream getOutputStream()
meth public java.io.OutputStream getInputStream()
meth public java.lang.String toString()
meth public org.apache.sshd.server.channel.ChannelSession getServerChannelSession()
meth public org.apache.sshd.server.session.ServerSession getServerSession()
meth public void destroy(org.apache.sshd.server.channel.ChannelSession)
meth public void setSession(org.apache.sshd.server.session.ServerSession)
meth public void start(org.apache.sshd.server.channel.ChannelSession,org.apache.sshd.server.Environment) throws java.io.IOException
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds channelSession,cmdValue,command,err,in,out,process,session

CLSS public org.apache.sshd.server.shell.ProcessShellCommandFactory
cons public init()
fld public final static java.lang.String FACTORY_NAME = "shell-command"
fld public final static org.apache.sshd.server.shell.ProcessShellCommandFactory INSTANCE
intf org.apache.sshd.server.command.CommandFactory
meth public java.lang.String toString()
meth public org.apache.sshd.server.command.Command createCommand(org.apache.sshd.server.channel.ChannelSession,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.sshd.server.shell.ProcessShellFactory
cons public !varargs init(java.lang.String,java.lang.String[])
cons public init()
cons public init(java.lang.String,java.util.List<java.lang.String>)
intf org.apache.sshd.server.shell.ShellFactory
meth protected java.util.List<java.lang.String> resolveEffectiveCommand(org.apache.sshd.server.channel.ChannelSession,java.lang.String,java.util.List<java.lang.String>)
meth protected org.apache.sshd.server.shell.InvertedShell createInvertedShell(org.apache.sshd.server.channel.ChannelSession)
meth public !varargs void setCommand(java.lang.String,java.lang.String[])
meth public java.lang.String getCommand()
meth public java.util.List<java.lang.String> getElements()
meth public org.apache.sshd.server.command.Command createShell(org.apache.sshd.server.channel.ChannelSession)
meth public void setCommand(java.lang.String,java.util.List<java.lang.String>)
supr org.apache.sshd.common.util.logging.AbstractLoggingBean
hfds command,elements

CLSS public abstract interface org.apache.sshd.server.shell.ShellFactory
meth public abstract org.apache.sshd.server.command.Command createShell(org.apache.sshd.server.channel.ChannelSession) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.server.shell.ShellFactorySelector
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.apache.sshd.server.shell.ShellFactory selectShellFactory(org.apache.sshd.server.channel.ChannelSession) throws java.io.IOException
meth public static org.apache.sshd.server.shell.ShellFactory selectShellFactory(java.util.Collection<? extends org.apache.sshd.server.shell.ShellFactorySelector>,org.apache.sshd.server.channel.ChannelSession) throws java.io.IOException

CLSS public org.apache.sshd.server.shell.TtyFilterInputStream
cons public init(java.io.InputStream,java.util.Collection<org.apache.sshd.common.channel.PtyMode>)
cons public init(java.io.InputStream,java.util.Map<org.apache.sshd.common.channel.PtyMode,?>)
fld public final static java.util.Set<org.apache.sshd.common.channel.PtyMode> INPUT_OPTIONS
meth protected int handleCR() throws java.io.IOException
meth protected int handleLF() throws java.io.IOException
meth protected int readRawInput() throws java.io.IOException
meth protected org.apache.sshd.common.util.buffer.Buffer insertCharacter(org.apache.sshd.common.util.buffer.Buffer,int)
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public void write(byte[],int,int)
meth public void write(int)
supr java.io.FilterInputStream
hfds buffer,lastChar,ttyOptions

CLSS public org.apache.sshd.server.shell.TtyFilterOutputStream
cons public init(java.io.OutputStream,java.util.Collection<org.apache.sshd.common.channel.PtyMode>)
cons public init(java.io.OutputStream,java.util.Map<org.apache.sshd.common.channel.PtyMode,?>)
fld public final static java.util.Set<org.apache.sshd.common.channel.PtyMode> OUTPUT_OPTIONS
meth protected void handleCR() throws java.io.IOException
meth protected void handleLF() throws java.io.IOException
meth protected void writeRawOutput(byte[],int,int) throws java.io.IOException
meth protected void writeRawOutput(int) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.FilterOutputStream
hfds ttyOptions

CLSS public org.apache.sshd.server.shell.UnknownCommand
cons public init(java.lang.String)
intf java.lang.Runnable
intf org.apache.sshd.server.command.Command
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getCommand()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public void destroy(org.apache.sshd.server.channel.ChannelSession)
meth public void run()
meth public void setErrorStream(java.io.OutputStream)
meth public void setExitCallback(org.apache.sshd.server.ExitCallback)
meth public void setInputStream(java.io.InputStream)
meth public void setOutputStream(java.io.OutputStream)
meth public void start(org.apache.sshd.server.channel.ChannelSession,org.apache.sshd.server.Environment) throws java.io.IOException
supr java.lang.Object
hfds callback,command,err,in,message,out

CLSS public org.apache.sshd.server.shell.UnknownCommandFactory
cons public init()
fld public final static java.lang.String FACTORY_NAME = "unknown"
fld public final static org.apache.sshd.server.shell.UnknownCommandFactory INSTANCE
intf org.apache.sshd.server.command.CommandFactory
meth public java.lang.String toString()
meth public org.apache.sshd.server.command.Command createCommand(org.apache.sshd.server.channel.ChannelSession,java.lang.String)
supr java.lang.Object

CLSS public abstract interface org.apache.sshd.server.subsystem.SubsystemFactory
intf org.apache.sshd.common.NamedResource
meth public abstract org.apache.sshd.server.command.Command createSubsystem(org.apache.sshd.server.channel.ChannelSession) throws java.io.IOException
meth public static org.apache.sshd.server.command.Command createSubsystem(org.apache.sshd.server.channel.ChannelSession,java.util.Collection<? extends org.apache.sshd.server.subsystem.SubsystemFactory>,java.lang.String) throws java.io.IOException

CLSS public org.apache.sshd.server.x11.ChannelForwardedX11
cons public init(org.apache.sshd.common.io.IoSession)
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth protected void doOpen() throws java.io.IOException
meth protected void doWriteData(byte[],int,long) throws java.io.IOException
meth public org.apache.sshd.client.future.OpenFuture open() throws java.io.IOException
meth public void handleEof() throws java.io.IOException
supr org.apache.sshd.client.channel.AbstractClientChannel
hfds serverSession

CLSS public org.apache.sshd.server.x11.DefaultX11ForwardSupport
cons public init(org.apache.sshd.common.session.ConnectionService)
intf org.apache.sshd.server.x11.X11ForwardSupport
meth protected org.apache.sshd.common.Closeable getInnerCloseable()
meth public java.lang.String createDisplay(boolean,java.lang.String,java.lang.String,int) throws java.io.IOException
meth public java.lang.String toString()
meth public void close() throws java.io.IOException
meth public void exceptionCaught(org.apache.sshd.common.io.IoSession,java.lang.Throwable) throws java.lang.Exception
meth public void messageReceived(org.apache.sshd.common.io.IoSession,org.apache.sshd.common.util.Readable) throws java.lang.Exception
meth public void sessionClosed(org.apache.sshd.common.io.IoSession) throws java.lang.Exception
meth public void sessionCreated(org.apache.sshd.common.io.IoSession) throws java.lang.Exception
supr org.apache.sshd.common.util.closeable.AbstractInnerCloseable
hfds acceptor,service

CLSS public abstract interface org.apache.sshd.server.x11.X11ForwardSupport
fld public final static java.lang.String ENV_DISPLAY = "DISPLAY"
fld public final static java.lang.String XAUTH_COMMAND
intf org.apache.sshd.common.Closeable
intf org.apache.sshd.common.io.IoHandler
meth public abstract java.lang.String createDisplay(boolean,java.lang.String,java.lang.String,int) throws java.io.IOException

CLSS public abstract interface org.apache.sshd.server.x11.X11ForwardingExceptionMarker

CLSS public abstract interface org.slf4j.Logger
fld public final static java.lang.String ROOT_LOGGER_NAME = "ROOT"
meth public abstract !varargs void debug(java.lang.String,java.lang.Object[])
meth public abstract !varargs void debug(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public abstract !varargs void error(java.lang.String,java.lang.Object[])
meth public abstract !varargs void error(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public abstract !varargs void info(java.lang.String,java.lang.Object[])
meth public abstract !varargs void info(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public abstract !varargs void trace(java.lang.String,java.lang.Object[])
meth public abstract !varargs void trace(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public abstract !varargs void warn(java.lang.String,java.lang.Object[])
meth public abstract !varargs void warn(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public abstract boolean isDebugEnabled()
meth public abstract boolean isDebugEnabled(org.slf4j.Marker)
meth public abstract boolean isErrorEnabled()
meth public abstract boolean isErrorEnabled(org.slf4j.Marker)
meth public abstract boolean isInfoEnabled()
meth public abstract boolean isInfoEnabled(org.slf4j.Marker)
meth public abstract boolean isTraceEnabled()
meth public abstract boolean isTraceEnabled(org.slf4j.Marker)
meth public abstract boolean isWarnEnabled()
meth public abstract boolean isWarnEnabled(org.slf4j.Marker)
meth public abstract java.lang.String getName()
meth public abstract void debug(java.lang.String)
meth public abstract void debug(java.lang.String,java.lang.Object)
meth public abstract void debug(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void debug(java.lang.String,java.lang.Throwable)
meth public abstract void debug(org.slf4j.Marker,java.lang.String)
meth public abstract void debug(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public abstract void debug(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void debug(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
meth public abstract void error(java.lang.String)
meth public abstract void error(java.lang.String,java.lang.Object)
meth public abstract void error(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void error(java.lang.String,java.lang.Throwable)
meth public abstract void error(org.slf4j.Marker,java.lang.String)
meth public abstract void error(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public abstract void error(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void error(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
meth public abstract void info(java.lang.String)
meth public abstract void info(java.lang.String,java.lang.Object)
meth public abstract void info(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void info(java.lang.String,java.lang.Throwable)
meth public abstract void info(org.slf4j.Marker,java.lang.String)
meth public abstract void info(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public abstract void info(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void info(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
meth public abstract void trace(java.lang.String)
meth public abstract void trace(java.lang.String,java.lang.Object)
meth public abstract void trace(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void trace(java.lang.String,java.lang.Throwable)
meth public abstract void trace(org.slf4j.Marker,java.lang.String)
meth public abstract void trace(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public abstract void trace(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void trace(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
meth public abstract void warn(java.lang.String)
meth public abstract void warn(java.lang.String,java.lang.Object)
meth public abstract void warn(java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void warn(java.lang.String,java.lang.Throwable)
meth public abstract void warn(org.slf4j.Marker,java.lang.String)
meth public abstract void warn(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public abstract void warn(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract void warn(org.slf4j.Marker,java.lang.String,java.lang.Throwable)

CLSS public abstract org.slf4j.helpers.MarkerIgnoringBase
cons public init()
fld protected java.lang.String name
intf java.io.Serializable
intf org.slf4j.Logger
meth protected java.lang.Object readResolve() throws java.io.ObjectStreamException
meth public !varargs void debug(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public !varargs void error(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public !varargs void info(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public !varargs void trace(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public !varargs void warn(org.slf4j.Marker,java.lang.String,java.lang.Object[])
meth public boolean isDebugEnabled(org.slf4j.Marker)
meth public boolean isErrorEnabled(org.slf4j.Marker)
meth public boolean isInfoEnabled(org.slf4j.Marker)
meth public boolean isTraceEnabled(org.slf4j.Marker)
meth public boolean isWarnEnabled(org.slf4j.Marker)
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public void debug(org.slf4j.Marker,java.lang.String)
meth public void debug(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public void debug(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public void debug(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
meth public void error(org.slf4j.Marker,java.lang.String)
meth public void error(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public void error(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public void error(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
meth public void info(org.slf4j.Marker,java.lang.String)
meth public void info(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public void info(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public void info(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
meth public void trace(org.slf4j.Marker,java.lang.String)
meth public void trace(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public void trace(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public void trace(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
meth public void warn(org.slf4j.Marker,java.lang.String)
meth public void warn(org.slf4j.Marker,java.lang.String,java.lang.Object)
meth public void warn(org.slf4j.Marker,java.lang.String,java.lang.Object,java.lang.Object)
meth public void warn(org.slf4j.Marker,java.lang.String,java.lang.Throwable)
supr java.lang.Object
hfds serialVersionUID

