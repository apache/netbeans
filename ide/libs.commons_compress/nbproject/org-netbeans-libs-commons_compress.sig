#Signature file v4.1
#Version 0.29.0

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

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface java.nio.channels.ReadableByteChannel
intf java.nio.channels.Channel
meth public abstract int read(java.nio.ByteBuffer) throws java.io.IOException

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

CLSS public abstract interface java.util.Iterator<%0 extends java.lang.Object>
meth public abstract boolean hasNext()
meth public abstract {java.util.Iterator%0} next()
meth public void forEachRemaining(java.util.function.Consumer<? super {java.util.Iterator%0}>)
meth public void remove()

CLSS public java.util.zip.ZipEntry
cons public init(java.lang.String)
cons public init(java.util.zip.ZipEntry)
fld public final static int CENATT = 36
fld public final static int CENATX = 38
fld public final static int CENCOM = 32
fld public final static int CENCRC = 16
fld public final static int CENDSK = 34
fld public final static int CENEXT = 30
fld public final static int CENFLG = 8
fld public final static int CENHDR = 46
fld public final static int CENHOW = 10
fld public final static int CENLEN = 24
fld public final static int CENNAM = 28
fld public final static int CENOFF = 42
fld public final static int CENSIZ = 20
fld public final static int CENTIM = 12
fld public final static int CENVEM = 4
fld public final static int CENVER = 6
fld public final static int DEFLATED = 8
fld public final static int ENDCOM = 20
fld public final static int ENDHDR = 22
fld public final static int ENDOFF = 16
fld public final static int ENDSIZ = 12
fld public final static int ENDSUB = 8
fld public final static int ENDTOT = 10
fld public final static int EXTCRC = 4
fld public final static int EXTHDR = 16
fld public final static int EXTLEN = 12
fld public final static int EXTSIZ = 8
fld public final static int LOCCRC = 14
fld public final static int LOCEXT = 28
fld public final static int LOCFLG = 6
fld public final static int LOCHDR = 30
fld public final static int LOCHOW = 8
fld public final static int LOCLEN = 22
fld public final static int LOCNAM = 26
fld public final static int LOCSIZ = 18
fld public final static int LOCTIM = 10
fld public final static int LOCVER = 4
fld public final static int STORED = 0
fld public final static long CENSIG = 33639248
fld public final static long ENDSIG = 101010256
fld public final static long EXTSIG = 134695760
fld public final static long LOCSIG = 67324752
intf java.lang.Cloneable
meth public boolean isDirectory()
meth public byte[] getExtra()
meth public int getMethod()
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String getComment()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.nio.file.attribute.FileTime getCreationTime()
meth public java.nio.file.attribute.FileTime getLastAccessTime()
meth public java.nio.file.attribute.FileTime getLastModifiedTime()
meth public java.util.zip.ZipEntry setCreationTime(java.nio.file.attribute.FileTime)
meth public java.util.zip.ZipEntry setLastAccessTime(java.nio.file.attribute.FileTime)
meth public java.util.zip.ZipEntry setLastModifiedTime(java.nio.file.attribute.FileTime)
meth public long getCompressedSize()
meth public long getCrc()
meth public long getSize()
meth public long getTime()
meth public void setComment(java.lang.String)
meth public void setCompressedSize(long)
meth public void setCrc(long)
meth public void setExtra(byte[])
meth public void setMethod(int)
meth public void setSize(long)
meth public void setTime(long)
supr java.lang.Object

CLSS public java.util.zip.ZipException
cons public init()
cons public init(java.lang.String)
supr java.io.IOException

CLSS public abstract interface org.apache.commons.compress.archivers.ArchiveEntry
fld public final static long SIZE_UNKNOWN = -1
meth public abstract boolean isDirectory()
meth public abstract java.lang.String getName()
meth public abstract java.util.Date getLastModifiedDate()
meth public abstract long getSize()

CLSS public org.apache.commons.compress.archivers.ArchiveException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
supr java.lang.Exception
hfds serialVersionUID

CLSS public abstract org.apache.commons.compress.archivers.ArchiveInputStream<%0 extends org.apache.commons.compress.archivers.ArchiveEntry>
cons public init()
meth protected void count(int)
meth protected void count(long)
meth protected void pushedBackBytes(long)
meth public abstract {org.apache.commons.compress.archivers.ArchiveInputStream%0} getNextEntry() throws java.io.IOException
meth public boolean canReadEntryData(org.apache.commons.compress.archivers.ArchiveEntry)
meth public int getCount()
 anno 0 java.lang.Deprecated()
meth public int read() throws java.io.IOException
meth public long getBytesRead()
supr java.io.InputStream
hfds BYTE_MASK,bytesRead,single

CLSS public abstract org.apache.commons.compress.archivers.ArchiveOutputStream<%0 extends org.apache.commons.compress.archivers.ArchiveEntry>
cons public init()
meth protected void count(int)
meth protected void count(long)
meth public !varargs {org.apache.commons.compress.archivers.ArchiveOutputStream%0} createArchiveEntry(java.nio.file.Path,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth public abstract void closeArchiveEntry() throws java.io.IOException
meth public abstract void finish() throws java.io.IOException
meth public abstract void putArchiveEntry({org.apache.commons.compress.archivers.ArchiveOutputStream%0}) throws java.io.IOException
meth public abstract {org.apache.commons.compress.archivers.ArchiveOutputStream%0} createArchiveEntry(java.io.File,java.lang.String) throws java.io.IOException
meth public boolean canWriteEntryData(org.apache.commons.compress.archivers.ArchiveEntry)
meth public int getCount()
 anno 0 java.lang.Deprecated()
meth public long getBytesWritten()
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream
hfds BYTE_MASK,bytesWritten,oneByte

CLSS public org.apache.commons.compress.archivers.ArchiveStreamFactory
cons public init()
cons public init(java.lang.String)
fld public final static java.lang.String APK = "apk"
fld public final static java.lang.String APKM = "apkm"
fld public final static java.lang.String APKS = "apks"
fld public final static java.lang.String AR = "ar"
fld public final static java.lang.String ARJ = "arj"
fld public final static java.lang.String CPIO = "cpio"
fld public final static java.lang.String DUMP = "dump"
fld public final static java.lang.String JAR = "jar"
fld public final static java.lang.String SEVEN_Z = "7z"
fld public final static java.lang.String TAR = "tar"
fld public final static java.lang.String XAPK = "xapk"
fld public final static java.lang.String ZIP = "zip"
fld public final static org.apache.commons.compress.archivers.ArchiveStreamFactory DEFAULT
intf org.apache.commons.compress.archivers.ArchiveStreamProvider
meth public <%0 extends org.apache.commons.compress.archivers.ArchiveInputStream<? extends org.apache.commons.compress.archivers.ArchiveEntry>> {%%0} createArchiveInputStream(java.io.InputStream) throws org.apache.commons.compress.archivers.ArchiveException
meth public <%0 extends org.apache.commons.compress.archivers.ArchiveInputStream<? extends org.apache.commons.compress.archivers.ArchiveEntry>> {%%0} createArchiveInputStream(java.lang.String,java.io.InputStream) throws org.apache.commons.compress.archivers.ArchiveException
meth public <%0 extends org.apache.commons.compress.archivers.ArchiveInputStream<? extends org.apache.commons.compress.archivers.ArchiveEntry>> {%%0} createArchiveInputStream(java.lang.String,java.io.InputStream,java.lang.String) throws org.apache.commons.compress.archivers.ArchiveException
meth public <%0 extends org.apache.commons.compress.archivers.ArchiveOutputStream<? extends org.apache.commons.compress.archivers.ArchiveEntry>> {%%0} createArchiveOutputStream(java.lang.String,java.io.OutputStream) throws org.apache.commons.compress.archivers.ArchiveException
meth public <%0 extends org.apache.commons.compress.archivers.ArchiveOutputStream<? extends org.apache.commons.compress.archivers.ArchiveEntry>> {%%0} createArchiveOutputStream(java.lang.String,java.io.OutputStream,java.lang.String) throws org.apache.commons.compress.archivers.ArchiveException
meth public java.lang.String getEntryEncoding()
meth public java.util.Set<java.lang.String> getInputStreamArchiveNames()
meth public java.util.Set<java.lang.String> getOutputStreamArchiveNames()
meth public java.util.SortedMap<java.lang.String,org.apache.commons.compress.archivers.ArchiveStreamProvider> getArchiveInputStreamProviders()
meth public java.util.SortedMap<java.lang.String,org.apache.commons.compress.archivers.ArchiveStreamProvider> getArchiveOutputStreamProviders()
meth public static java.lang.String detect(java.io.InputStream) throws org.apache.commons.compress.archivers.ArchiveException
meth public static java.util.SortedMap<java.lang.String,org.apache.commons.compress.archivers.ArchiveStreamProvider> findAvailableArchiveInputStreamProviders()
meth public static java.util.SortedMap<java.lang.String,org.apache.commons.compress.archivers.ArchiveStreamProvider> findAvailableArchiveOutputStreamProviders()
meth public void setEntryEncoding(java.lang.String)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds DUMP_SIGNATURE_SIZE,SIGNATURE_SIZE,TAR_HEADER_SIZE,archiveInputStreamProviders,archiveOutputStreamProviders,encoding,entryEncoding

CLSS public abstract interface org.apache.commons.compress.archivers.ArchiveStreamProvider
meth public abstract <%0 extends org.apache.commons.compress.archivers.ArchiveInputStream<? extends org.apache.commons.compress.archivers.ArchiveEntry>> {%%0} createArchiveInputStream(java.lang.String,java.io.InputStream,java.lang.String) throws org.apache.commons.compress.archivers.ArchiveException
meth public abstract <%0 extends org.apache.commons.compress.archivers.ArchiveOutputStream<? extends org.apache.commons.compress.archivers.ArchiveEntry>> {%%0} createArchiveOutputStream(java.lang.String,java.io.OutputStream,java.lang.String) throws org.apache.commons.compress.archivers.ArchiveException
meth public abstract java.util.Set<java.lang.String> getInputStreamArchiveNames()
meth public abstract java.util.Set<java.lang.String> getOutputStreamArchiveNames()

CLSS public abstract interface org.apache.commons.compress.archivers.EntryStreamOffsets
fld public final static long OFFSET_UNKNOWN = -1
meth public abstract boolean isStreamContiguous()
meth public abstract long getDataOffset()

CLSS public final org.apache.commons.compress.archivers.Lister
cons public init()
meth public static void main(java.lang.String[]) throws java.io.IOException,org.apache.commons.compress.archivers.ArchiveException
supr java.lang.Object
hfds FACTORY

CLSS public org.apache.commons.compress.archivers.StreamingNotSupportedException
cons public init(java.lang.String)
meth public java.lang.String getFormat()
supr org.apache.commons.compress.archivers.ArchiveException
hfds format,serialVersionUID

CLSS abstract interface org.apache.commons.compress.archivers.package-info

CLSS public org.apache.commons.compress.archivers.tar.TarArchiveEntry
cons public !varargs init(java.nio.file.Path,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
cons public init(byte[])
cons public init(byte[],org.apache.commons.compress.archivers.zip.ZipEncoding) throws java.io.IOException
cons public init(byte[],org.apache.commons.compress.archivers.zip.ZipEncoding,boolean) throws java.io.IOException
cons public init(byte[],org.apache.commons.compress.archivers.zip.ZipEncoding,boolean,long) throws java.io.IOException
cons public init(java.io.File)
cons public init(java.io.File,java.lang.String)
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,byte)
cons public init(java.lang.String,byte,boolean)
cons public init(java.nio.file.Path) throws java.io.IOException
cons public init(java.util.Map<java.lang.String,java.lang.String>,byte[],org.apache.commons.compress.archivers.zip.ZipEncoding,boolean) throws java.io.IOException
cons public init(java.util.Map<java.lang.String,java.lang.String>,byte[],org.apache.commons.compress.archivers.zip.ZipEncoding,boolean,long) throws java.io.IOException
fld public final static int DEFAULT_DIR_MODE = 16877
fld public final static int DEFAULT_FILE_MODE = 33188
fld public final static int MAX_NAMELEN = 31
fld public final static int MILLIS_PER_SECOND = 1000
 anno 0 java.lang.Deprecated()
fld public final static long UNKNOWN = -1
intf org.apache.commons.compress.archivers.ArchiveEntry
intf org.apache.commons.compress.archivers.EntryStreamOffsets
intf org.apache.commons.compress.archivers.tar.TarConstants
meth public boolean equals(java.lang.Object)
meth public boolean equals(org.apache.commons.compress.archivers.tar.TarArchiveEntry)
meth public boolean isBlockDevice()
meth public boolean isCharacterDevice()
meth public boolean isCheckSumOK()
meth public boolean isDescendent(org.apache.commons.compress.archivers.tar.TarArchiveEntry)
meth public boolean isDirectory()
meth public boolean isExtended()
meth public boolean isFIFO()
meth public boolean isFile()
meth public boolean isGNULongLinkEntry()
meth public boolean isGNULongNameEntry()
meth public boolean isGNUSparse()
meth public boolean isGlobalPaxHeader()
meth public boolean isLink()
meth public boolean isOldGNUSparse()
meth public boolean isPaxGNU1XSparse()
meth public boolean isPaxGNUSparse()
meth public boolean isPaxHeader()
meth public boolean isSparse()
meth public boolean isStarSparse()
meth public boolean isStreamContiguous()
meth public boolean isSymbolicLink()
meth public byte getLinkFlag()
meth public int getDevMajor()
meth public int getDevMinor()
meth public int getGroupId()
 anno 0 java.lang.Deprecated()
meth public int getMode()
meth public int getUserId()
 anno 0 java.lang.Deprecated()
meth public int hashCode()
meth public java.io.File getFile()
meth public java.lang.String getExtraPaxHeader(java.lang.String)
meth public java.lang.String getGroupName()
meth public java.lang.String getLinkName()
meth public java.lang.String getName()
meth public java.lang.String getUserName()
meth public java.nio.file.Path getPath()
meth public java.nio.file.attribute.FileTime getCreationTime()
meth public java.nio.file.attribute.FileTime getLastAccessTime()
meth public java.nio.file.attribute.FileTime getLastModifiedTime()
meth public java.nio.file.attribute.FileTime getStatusChangeTime()
meth public java.util.Date getLastModifiedDate()
meth public java.util.Date getModTime()
meth public java.util.List<org.apache.commons.compress.archivers.tar.TarArchiveStructSparse> getOrderedSparseHeaders() throws java.io.IOException
meth public java.util.List<org.apache.commons.compress.archivers.tar.TarArchiveStructSparse> getSparseHeaders()
meth public java.util.Map<java.lang.String,java.lang.String> getExtraPaxHeaders()
meth public long getDataOffset()
meth public long getLongGroupId()
meth public long getLongUserId()
meth public long getRealSize()
meth public long getSize()
meth public org.apache.commons.compress.archivers.tar.TarArchiveEntry[] getDirectoryEntries()
meth public void addPaxHeader(java.lang.String,java.lang.String)
meth public void clearExtraPaxHeaders()
meth public void parseTarHeader(byte[])
meth public void parseTarHeader(byte[],org.apache.commons.compress.archivers.zip.ZipEncoding) throws java.io.IOException
meth public void setCreationTime(java.nio.file.attribute.FileTime)
meth public void setDataOffset(long)
meth public void setDevMajor(int)
meth public void setDevMinor(int)
meth public void setGroupId(int)
meth public void setGroupId(long)
meth public void setGroupName(java.lang.String)
meth public void setIds(int,int)
meth public void setLastAccessTime(java.nio.file.attribute.FileTime)
meth public void setLastModifiedTime(java.nio.file.attribute.FileTime)
meth public void setLinkName(java.lang.String)
meth public void setModTime(java.nio.file.attribute.FileTime)
meth public void setModTime(java.util.Date)
meth public void setModTime(long)
meth public void setMode(int)
meth public void setName(java.lang.String)
meth public void setNames(java.lang.String,java.lang.String)
meth public void setSize(long)
meth public void setSparseHeaders(java.util.List<org.apache.commons.compress.archivers.tar.TarArchiveStructSparse>)
meth public void setStatusChangeTime(java.nio.file.attribute.FileTime)
meth public void setUserId(int)
meth public void setUserId(long)
meth public void setUserName(java.lang.String)
meth public void writeEntryHeader(byte[])
meth public void writeEntryHeader(byte[],org.apache.commons.compress.archivers.zip.ZipEncoding,boolean) throws java.io.IOException
supr java.lang.Object
hfds EMPTY_TAR_ARCHIVE_ENTRY_ARRAY,PAX_EXTENDED_HEADER_FILE_TIMES_PATTERN,aTime,birthTime,cTime,checkSumOK,dataOffset,devMajor,devMinor,extraPaxHeaders,file,groupId,groupName,isExtended,linkFlag,linkName,linkOptions,mTime,magic,mode,name,paxGNU1XSparse,paxGNUSparse,preserveAbsolutePath,realSize,size,sparseHeaders,starSparse,userId,userName,version

CLSS public org.apache.commons.compress.archivers.tar.TarArchiveInputStream
cons public init(java.io.InputStream)
cons public init(java.io.InputStream,boolean)
cons public init(java.io.InputStream,int)
cons public init(java.io.InputStream,int,int)
cons public init(java.io.InputStream,int,int,java.lang.String)
cons public init(java.io.InputStream,int,int,java.lang.String,boolean)
cons public init(java.io.InputStream,int,java.lang.String)
cons public init(java.io.InputStream,java.lang.String)
meth protected boolean isEOFRecord(byte[])
meth protected byte[] getLongNameData() throws java.io.IOException
meth protected byte[] readRecord() throws java.io.IOException
meth protected final boolean isAtEOF()
meth protected final void setAtEOF(boolean)
meth protected final void setCurrentEntry(org.apache.commons.compress.archivers.tar.TarArchiveEntry)
meth public boolean canReadEntryData(org.apache.commons.compress.archivers.ArchiveEntry)
meth public boolean markSupported()
meth public int available() throws java.io.IOException
meth public int getRecordSize()
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public org.apache.commons.compress.archivers.tar.TarArchiveEntry getCurrentEntry()
meth public org.apache.commons.compress.archivers.tar.TarArchiveEntry getNextEntry() throws java.io.IOException
meth public org.apache.commons.compress.archivers.tar.TarArchiveEntry getNextTarEntry() throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static boolean matches(byte[],int)
meth public void close() throws java.io.IOException
meth public void mark(int)
meth public void reset()
supr org.apache.commons.compress.archivers.ArchiveInputStream<org.apache.commons.compress.archivers.tar.TarArchiveEntry>
hfds SMALL_BUFFER_SIZE,blockSize,currEntry,currentSparseInputStreamIndex,encoding,entryOffset,entrySize,globalPaxHeaders,globalSparseHeaders,hasHitEOF,inputStream,lenient,recordBuffer,recordSize,smallBuf,sparseInputStreams,zipEncoding

CLSS public org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
cons public init(java.io.OutputStream)
cons public init(java.io.OutputStream,int)
cons public init(java.io.OutputStream,int,int)
 anno 0 java.lang.Deprecated()
cons public init(java.io.OutputStream,int,int,java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(java.io.OutputStream,int,java.lang.String)
cons public init(java.io.OutputStream,java.lang.String)
fld public final static int BIGNUMBER_ERROR = 0
fld public final static int BIGNUMBER_POSIX = 2
fld public final static int BIGNUMBER_STAR = 1
fld public final static int LONGFILE_ERROR = 0
fld public final static int LONGFILE_GNU = 2
fld public final static int LONGFILE_POSIX = 3
fld public final static int LONGFILE_TRUNCATE = 1
meth public !varargs org.apache.commons.compress.archivers.tar.TarArchiveEntry createArchiveEntry(java.nio.file.Path,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth public int getCount()
 anno 0 java.lang.Deprecated()
meth public int getRecordSize()
 anno 0 java.lang.Deprecated()
meth public long getBytesWritten()
meth public org.apache.commons.compress.archivers.tar.TarArchiveEntry createArchiveEntry(java.io.File,java.lang.String) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void closeArchiveEntry() throws java.io.IOException
meth public void finish() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void putArchiveEntry(org.apache.commons.compress.archivers.tar.TarArchiveEntry) throws java.io.IOException
meth public void setAddPaxHeadersForNonAsciiNames(boolean)
meth public void setBigNumberMode(int)
meth public void setLongFileMode(int)
meth public void write(byte[],int,int) throws java.io.IOException
supr org.apache.commons.compress.archivers.ArchiveOutputStream<org.apache.commons.compress.archivers.tar.TarArchiveEntry>
hfds ASCII,BLOCK_SIZE_UNSPECIFIED,RECORD_SIZE,addPaxHeadersForNonAsciiNames,bigNumberMode,closed,countingOut,currBytes,currName,currSize,encoding,finished,haveUnclosedEntry,longFileMode,out,recordBuf,recordsPerBlock,recordsWritten,zipEncoding

CLSS public org.apache.commons.compress.archivers.tar.TarArchiveSparseEntry
cons public init(byte[]) throws java.io.IOException
intf org.apache.commons.compress.archivers.tar.TarConstants
meth public boolean isExtended()
meth public java.util.List<org.apache.commons.compress.archivers.tar.TarArchiveStructSparse> getSparseHeaders()
supr java.lang.Object
hfds isExtended,sparseHeaders

CLSS public final org.apache.commons.compress.archivers.tar.TarArchiveStructSparse
cons public init(long,long)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public long getNumbytes()
meth public long getOffset()
supr java.lang.Object
hfds numbytes,offset

CLSS public abstract interface org.apache.commons.compress.archivers.tar.TarConstants
fld public final static byte LF_BLK = 52
fld public final static byte LF_CHR = 51
fld public final static byte LF_CONTIG = 55
fld public final static byte LF_DIR = 53
fld public final static byte LF_FIFO = 54
fld public final static byte LF_GNUTYPE_LONGLINK = 75
fld public final static byte LF_GNUTYPE_LONGNAME = 76
fld public final static byte LF_GNUTYPE_SPARSE = 83
fld public final static byte LF_LINK = 49
fld public final static byte LF_MULTIVOLUME = 77
fld public final static byte LF_NORMAL = 48
fld public final static byte LF_OLDNORM = 0
fld public final static byte LF_PAX_EXTENDED_HEADER_LC = 120
fld public final static byte LF_PAX_EXTENDED_HEADER_UC = 88
fld public final static byte LF_PAX_GLOBAL_EXTENDED_HEADER = 103
fld public final static byte LF_SYMLINK = 50
fld public final static int ATIMELEN_GNU = 12
fld public final static int ATIMELEN_XSTAR = 12
fld public final static int CHKSUMLEN = 8
fld public final static int CHKSUM_OFFSET = 148
fld public final static int CTIMELEN_GNU = 12
fld public final static int CTIMELEN_XSTAR = 12
fld public final static int DEFAULT_BLKSIZE = 10240
fld public final static int DEFAULT_RCDSIZE = 512
fld public final static int DEVLEN = 8
fld public final static int FORMAT_OLDGNU = 2
fld public final static int FORMAT_POSIX = 3
fld public final static int FORMAT_XSTAR = 4
fld public final static int GIDLEN = 8
fld public final static int GNAMELEN = 32
fld public final static int ISEXTENDEDLEN_GNU = 1
fld public final static int ISEXTENDEDLEN_GNU_SPARSE = 1
fld public final static int LF_OFFSET = 156
fld public final static int LONGNAMESLEN_GNU = 4
fld public final static int MAGICLEN = 6
fld public final static int MAGIC_OFFSET = 257
fld public final static int MODELEN = 8
fld public final static int MODTIMELEN = 12
fld public final static int NAMELEN = 100
fld public final static int OFFSETLEN_GNU = 12
fld public final static int PAD2LEN_GNU = 1
fld public final static int PREFIXLEN = 155
fld public final static int PREFIXLEN_XSTAR = 131
fld public final static int REALSIZELEN_GNU = 12
fld public final static int SIZELEN = 12
fld public final static int SPARSELEN_GNU = 96
fld public final static int SPARSELEN_GNU_SPARSE = 504
fld public final static int SPARSE_HEADERS_IN_EXTENSION_HEADER = 21
fld public final static int SPARSE_HEADERS_IN_OLDGNU_HEADER = 4
fld public final static int SPARSE_NUMBYTES_LEN = 12
fld public final static int SPARSE_OFFSET_LEN = 12
fld public final static int UIDLEN = 8
fld public final static int UNAMELEN = 32
fld public final static int VERSIONLEN = 2
fld public final static int VERSION_OFFSET = 263
fld public final static int XSTAR_ATIME_OFFSET = 476
fld public final static int XSTAR_CTIME_OFFSET = 488
fld public final static int XSTAR_MAGIC_LEN = 4
fld public final static int XSTAR_MAGIC_OFFSET = 508
fld public final static int XSTAR_MULTIVOLUME_OFFSET = 464
fld public final static int XSTAR_PREFIX_OFFSET = 345
fld public final static java.lang.String GNU_LONGLINK = "././@LongLink"
fld public final static java.lang.String MAGIC_ANT = "ustar\u0000"
fld public final static java.lang.String MAGIC_GNU = "ustar "
fld public final static java.lang.String MAGIC_POSIX = "ustar\u0000"
fld public final static java.lang.String MAGIC_XSTAR = "tar\u0000"
fld public final static java.lang.String VERSION_ANT = "\u0000\u0000"
fld public final static java.lang.String VERSION_GNU_SPACE = " \u0000"
fld public final static java.lang.String VERSION_GNU_ZERO = "0\u0000"
fld public final static java.lang.String VERSION_POSIX = "00"
fld public final static long MAXID = 2097151
fld public final static long MAXSIZE = 8589934591

CLSS public org.apache.commons.compress.archivers.tar.TarFile
cons public init(byte[]) throws java.io.IOException
cons public init(byte[],boolean) throws java.io.IOException
cons public init(byte[],java.lang.String) throws java.io.IOException
cons public init(java.io.File) throws java.io.IOException
cons public init(java.io.File,boolean) throws java.io.IOException
cons public init(java.io.File,java.lang.String) throws java.io.IOException
cons public init(java.nio.channels.SeekableByteChannel) throws java.io.IOException
cons public init(java.nio.channels.SeekableByteChannel,int,int,java.lang.String,boolean) throws java.io.IOException
cons public init(java.nio.file.Path) throws java.io.IOException
cons public init(java.nio.file.Path,boolean) throws java.io.IOException
cons public init(java.nio.file.Path,java.lang.String) throws java.io.IOException
intf java.io.Closeable
meth protected final boolean isAtEOF()
meth protected final void setAtEOF(boolean)
meth public java.io.InputStream getInputStream(org.apache.commons.compress.archivers.tar.TarArchiveEntry) throws java.io.IOException
meth public java.util.List<org.apache.commons.compress.archivers.tar.TarArchiveEntry> getEntries()
meth public void close() throws java.io.IOException
supr java.lang.Object
hfds SMALL_BUFFER_SIZE,archive,blockSize,currEntry,entries,globalPaxHeaders,globalSparseHeaders,hasHitEOF,lenient,recordBuffer,recordSize,smallBuf,sparseInputStreams,zipEncoding
hcls BoundedTarEntryInputStream

CLSS public org.apache.commons.compress.archivers.tar.TarUtils
meth protected static java.util.List<org.apache.commons.compress.archivers.tar.TarArchiveStructSparse> parseFromPAX01SparseHeaders(java.lang.String) throws java.io.IOException
meth protected static java.util.List<org.apache.commons.compress.archivers.tar.TarArchiveStructSparse> parsePAX01SparseHeaders(java.lang.String)
 anno 0 java.lang.Deprecated()
meth protected static java.util.List<org.apache.commons.compress.archivers.tar.TarArchiveStructSparse> parsePAX1XSparseHeaders(java.io.InputStream,int) throws java.io.IOException
meth protected static java.util.Map<java.lang.String,java.lang.String> parsePaxHeaders(java.io.InputStream,java.util.List<org.apache.commons.compress.archivers.tar.TarArchiveStructSparse>,java.util.Map<java.lang.String,java.lang.String>) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth protected static java.util.Map<java.lang.String,java.lang.String> parsePaxHeaders(java.io.InputStream,java.util.List<org.apache.commons.compress.archivers.tar.TarArchiveStructSparse>,java.util.Map<java.lang.String,java.lang.String>,long) throws java.io.IOException
meth public static boolean parseBoolean(byte[],int)
meth public static boolean verifyCheckSum(byte[])
meth public static int formatCheckSumOctalBytes(long,byte[],int,int)
meth public static int formatLongOctalBytes(long,byte[],int,int)
meth public static int formatLongOctalOrBinaryBytes(long,byte[],int,int)
meth public static int formatNameBytes(java.lang.String,byte[],int,int)
meth public static int formatNameBytes(java.lang.String,byte[],int,int,org.apache.commons.compress.archivers.zip.ZipEncoding) throws java.io.IOException
meth public static int formatOctalBytes(long,byte[],int,int)
meth public static java.lang.String parseName(byte[],int,int)
meth public static java.lang.String parseName(byte[],int,int,org.apache.commons.compress.archivers.zip.ZipEncoding) throws java.io.IOException
meth public static long computeCheckSum(byte[])
meth public static long parseOctal(byte[],int,int)
meth public static long parseOctalOrBinary(byte[],int,int)
meth public static org.apache.commons.compress.archivers.tar.TarArchiveStructSparse parseSparse(byte[],int)
meth public static void formatUnsignedOctalString(long,byte[],int,int)
supr java.lang.Object
hfds BYTE_MASK,DEFAULT_ENCODING,FALLBACK_ENCODING

CLSS abstract interface org.apache.commons.compress.archivers.tar.package-info

CLSS public abstract org.apache.commons.compress.archivers.zip.AbstractUnicodeExtraField
cons protected init()
cons protected init(java.lang.String,byte[])
cons protected init(java.lang.String,byte[],int,int)
intf org.apache.commons.compress.archivers.zip.ZipExtraField
meth public byte[] getCentralDirectoryData()
meth public byte[] getLocalFileDataData()
meth public byte[] getUnicodeName()
meth public long getNameCRC32()
meth public org.apache.commons.compress.archivers.zip.ZipShort getCentralDirectoryLength()
meth public org.apache.commons.compress.archivers.zip.ZipShort getLocalFileDataLength()
meth public void parseFromCentralDirectoryData(byte[],int,int) throws java.util.zip.ZipException
meth public void parseFromLocalFileData(byte[],int,int) throws java.util.zip.ZipException
meth public void setNameCRC32(long)
meth public void setUnicodeName(byte[])
supr java.lang.Object
hfds data,nameCRC32,unicodeName

CLSS public org.apache.commons.compress.archivers.zip.AsiExtraField
cons public init()
intf java.lang.Cloneable
intf org.apache.commons.compress.archivers.zip.UnixStat
intf org.apache.commons.compress.archivers.zip.ZipExtraField
meth protected int getMode(int)
meth public boolean isDirectory()
meth public boolean isLink()
meth public byte[] getCentralDirectoryData()
meth public byte[] getLocalFileDataData()
meth public int getGroupId()
meth public int getMode()
meth public int getUserId()
meth public java.lang.Object clone()
meth public java.lang.String getLinkedFile()
meth public org.apache.commons.compress.archivers.zip.ZipShort getCentralDirectoryLength()
meth public org.apache.commons.compress.archivers.zip.ZipShort getHeaderId()
meth public org.apache.commons.compress.archivers.zip.ZipShort getLocalFileDataLength()
meth public void parseFromCentralDirectoryData(byte[],int,int) throws java.util.zip.ZipException
meth public void parseFromLocalFileData(byte[],int,int) throws java.util.zip.ZipException
meth public void setDirectory(boolean)
meth public void setGroupId(int)
meth public void setLinkedFile(java.lang.String)
meth public void setMode(int)
meth public void setUserId(int)
supr java.lang.Object
hfds HEADER_ID,MIN_SIZE,crc,dirFlag,gid,link,mode,uid

CLSS public abstract interface org.apache.commons.compress.archivers.zip.CharsetAccessor
meth public abstract java.nio.charset.Charset getCharset()

CLSS public org.apache.commons.compress.archivers.zip.DefaultBackingStoreSupplier
cons public init(java.nio.file.Path)
intf org.apache.commons.compress.parallel.ScatterGatherBackingStoreSupplier
meth public org.apache.commons.compress.parallel.ScatterGatherBackingStore get() throws java.io.IOException
supr java.lang.Object
hfds PREFIX,dir,storeNum

CLSS public abstract interface org.apache.commons.compress.archivers.zip.ExtraFieldParsingBehavior
intf org.apache.commons.compress.archivers.zip.UnparseableExtraFieldBehavior
meth public abstract org.apache.commons.compress.archivers.zip.ZipExtraField createExtraField(org.apache.commons.compress.archivers.zip.ZipShort) throws java.lang.IllegalAccessException,java.lang.InstantiationException,java.util.zip.ZipException
meth public abstract org.apache.commons.compress.archivers.zip.ZipExtraField fill(org.apache.commons.compress.archivers.zip.ZipExtraField,byte[],int,int,boolean) throws java.util.zip.ZipException

CLSS public org.apache.commons.compress.archivers.zip.ExtraFieldUtils
cons public init()
innr public final static UnparseableExtraField
meth public static byte[] mergeCentralDirectoryData(org.apache.commons.compress.archivers.zip.ZipExtraField[])
meth public static byte[] mergeLocalFileDataData(org.apache.commons.compress.archivers.zip.ZipExtraField[])
meth public static org.apache.commons.compress.archivers.zip.ZipExtraField createExtraField(org.apache.commons.compress.archivers.zip.ZipShort) throws java.lang.IllegalAccessException,java.lang.InstantiationException
meth public static org.apache.commons.compress.archivers.zip.ZipExtraField createExtraFieldNoDefault(org.apache.commons.compress.archivers.zip.ZipShort) throws java.lang.IllegalAccessException,java.lang.InstantiationException
meth public static org.apache.commons.compress.archivers.zip.ZipExtraField fillExtraField(org.apache.commons.compress.archivers.zip.ZipExtraField,byte[],int,int,boolean) throws java.util.zip.ZipException
meth public static org.apache.commons.compress.archivers.zip.ZipExtraField[] parse(byte[]) throws java.util.zip.ZipException
meth public static org.apache.commons.compress.archivers.zip.ZipExtraField[] parse(byte[],boolean) throws java.util.zip.ZipException
meth public static org.apache.commons.compress.archivers.zip.ZipExtraField[] parse(byte[],boolean,org.apache.commons.compress.archivers.zip.ExtraFieldParsingBehavior) throws java.util.zip.ZipException
meth public static org.apache.commons.compress.archivers.zip.ZipExtraField[] parse(byte[],boolean,org.apache.commons.compress.archivers.zip.ExtraFieldUtils$UnparseableExtraField) throws java.util.zip.ZipException
meth public static void register(java.lang.Class<?>)
supr java.lang.Object
hfds EMPTY_ZIP_EXTRA_FIELD_ARRAY,IMPLEMENTATIONS,WORD

CLSS public final static org.apache.commons.compress.archivers.zip.ExtraFieldUtils$UnparseableExtraField
 outer org.apache.commons.compress.archivers.zip.ExtraFieldUtils
fld public final static int READ_KEY = 2
fld public final static int SKIP_KEY = 1
fld public final static int THROW_KEY = 0
fld public final static org.apache.commons.compress.archivers.zip.ExtraFieldUtils$UnparseableExtraField READ
fld public final static org.apache.commons.compress.archivers.zip.ExtraFieldUtils$UnparseableExtraField SKIP
fld public final static org.apache.commons.compress.archivers.zip.ExtraFieldUtils$UnparseableExtraField THROW
intf org.apache.commons.compress.archivers.zip.UnparseableExtraFieldBehavior
meth public int getKey()
meth public org.apache.commons.compress.archivers.zip.ZipExtraField onUnparseableExtraField(byte[],int,int,boolean,int) throws java.util.zip.ZipException
supr java.lang.Object
hfds key

CLSS public final org.apache.commons.compress.archivers.zip.GeneralPurposeBit
cons public init()
fld public final static int UFT8_NAMES_FLAG = 2048
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public boolean usesDataDescriptor()
meth public boolean usesEncryption()
meth public boolean usesStrongEncryption()
meth public boolean usesUTF8ForNames()
meth public byte[] encode()
meth public int hashCode()
meth public java.lang.Object clone()
meth public static org.apache.commons.compress.archivers.zip.GeneralPurposeBit parse(byte[],int)
meth public void encode(byte[],int)
meth public void useDataDescriptor(boolean)
meth public void useEncryption(boolean)
meth public void useStrongEncryption(boolean)
meth public void useUTF8ForNames(boolean)
supr java.lang.Object
hfds DATA_DESCRIPTOR_FLAG,ENCRYPTION_FLAG,NUMBER_OF_SHANNON_FANO_TREES_FLAG,SLIDING_DICTIONARY_SIZE_FLAG,STRONG_ENCRYPTION_FLAG,dataDescriptorFlag,encryptionFlag,languageEncodingFlag,numberOfShannonFanoTrees,slidingDictionarySize,strongEncryptionFlag

CLSS public final org.apache.commons.compress.archivers.zip.JarMarker
cons public init()
intf org.apache.commons.compress.archivers.zip.ZipExtraField
meth public byte[] getCentralDirectoryData()
meth public byte[] getLocalFileDataData()
meth public org.apache.commons.compress.archivers.zip.ZipShort getCentralDirectoryLength()
meth public org.apache.commons.compress.archivers.zip.ZipShort getHeaderId()
meth public org.apache.commons.compress.archivers.zip.ZipShort getLocalFileDataLength()
meth public static org.apache.commons.compress.archivers.zip.JarMarker getInstance()
meth public void parseFromCentralDirectoryData(byte[],int,int) throws java.util.zip.ZipException
meth public void parseFromLocalFileData(byte[],int,int) throws java.util.zip.ZipException
supr java.lang.Object
hfds DEFAULT,ID,NULL

CLSS public abstract org.apache.commons.compress.archivers.zip.PKWareExtraHeader
cons protected init(org.apache.commons.compress.archivers.zip.ZipShort)
innr public final static !enum EncryptionAlgorithm
innr public final static !enum HashAlgorithm
intf org.apache.commons.compress.archivers.zip.ZipExtraField
meth protected final void assertMinimalLength(int,int) throws java.util.zip.ZipException
meth public byte[] getCentralDirectoryData()
meth public byte[] getLocalFileDataData()
meth public org.apache.commons.compress.archivers.zip.ZipShort getCentralDirectoryLength()
meth public org.apache.commons.compress.archivers.zip.ZipShort getHeaderId()
meth public org.apache.commons.compress.archivers.zip.ZipShort getLocalFileDataLength()
meth public void parseFromCentralDirectoryData(byte[],int,int) throws java.util.zip.ZipException
meth public void parseFromLocalFileData(byte[],int,int) throws java.util.zip.ZipException
meth public void setCentralDirectoryData(byte[])
meth public void setLocalFileDataData(byte[])
supr java.lang.Object
hfds centralData,headerId,localData

CLSS public final static !enum org.apache.commons.compress.archivers.zip.PKWareExtraHeader$EncryptionAlgorithm
 outer org.apache.commons.compress.archivers.zip.PKWareExtraHeader
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$EncryptionAlgorithm AES128
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$EncryptionAlgorithm AES192
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$EncryptionAlgorithm AES256
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$EncryptionAlgorithm DES
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$EncryptionAlgorithm RC2
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$EncryptionAlgorithm RC2pre52
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$EncryptionAlgorithm RC4
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$EncryptionAlgorithm TripleDES168
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$EncryptionAlgorithm TripleDES192
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$EncryptionAlgorithm UNKNOWN
meth public int getCode()
meth public static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$EncryptionAlgorithm getAlgorithmByCode(int)
meth public static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$EncryptionAlgorithm valueOf(java.lang.String)
meth public static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$EncryptionAlgorithm[] values()
supr java.lang.Enum<org.apache.commons.compress.archivers.zip.PKWareExtraHeader$EncryptionAlgorithm>
hfds code,codeToEnum

CLSS public final static !enum org.apache.commons.compress.archivers.zip.PKWareExtraHeader$HashAlgorithm
 outer org.apache.commons.compress.archivers.zip.PKWareExtraHeader
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$HashAlgorithm CRC32
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$HashAlgorithm MD5
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$HashAlgorithm NONE
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$HashAlgorithm RIPEND160
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$HashAlgorithm SHA1
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$HashAlgorithm SHA256
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$HashAlgorithm SHA384
fld public final static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$HashAlgorithm SHA512
meth public int getCode()
meth public static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$HashAlgorithm getAlgorithmByCode(int)
meth public static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$HashAlgorithm valueOf(java.lang.String)
meth public static org.apache.commons.compress.archivers.zip.PKWareExtraHeader$HashAlgorithm[] values()
supr java.lang.Enum<org.apache.commons.compress.archivers.zip.PKWareExtraHeader$HashAlgorithm>
hfds code,codeToEnum

CLSS public org.apache.commons.compress.archivers.zip.ParallelScatterZipCreator
cons public init()
cons public init(java.util.concurrent.ExecutorService)
cons public init(java.util.concurrent.ExecutorService,org.apache.commons.compress.parallel.ScatterGatherBackingStoreSupplier)
cons public init(java.util.concurrent.ExecutorService,org.apache.commons.compress.parallel.ScatterGatherBackingStoreSupplier,int)
meth public final java.util.concurrent.Callable<org.apache.commons.compress.archivers.zip.ScatterZipOutputStream> createCallable(org.apache.commons.compress.archivers.zip.ZipArchiveEntry,org.apache.commons.compress.parallel.InputStreamSupplier)
meth public final java.util.concurrent.Callable<org.apache.commons.compress.archivers.zip.ScatterZipOutputStream> createCallable(org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequestSupplier)
meth public final void submit(java.util.concurrent.Callable<?>)
meth public final void submitStreamAwareCallable(java.util.concurrent.Callable<? extends org.apache.commons.compress.archivers.zip.ScatterZipOutputStream>)
meth public org.apache.commons.compress.archivers.zip.ScatterStatistics getStatisticsMessage()
meth public void addArchiveEntry(org.apache.commons.compress.archivers.zip.ZipArchiveEntry,org.apache.commons.compress.parallel.InputStreamSupplier)
meth public void addArchiveEntry(org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequestSupplier)
meth public void writeTo(org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream) throws java.io.IOException,java.lang.InterruptedException,java.util.concurrent.ExecutionException
supr java.lang.Object
hfds backingStoreSupplier,compressionDoneAt,compressionLevel,executorService,futures,scatterDoneAt,startedAt,streams,tlScatterStreams

CLSS public org.apache.commons.compress.archivers.zip.ResourceAlignmentExtraField
cons public init()
cons public init(int)
cons public init(int,boolean)
cons public init(int,boolean,int)
fld public final static int BASE_SIZE = 2
fld public final static org.apache.commons.compress.archivers.zip.ZipShort ID
intf org.apache.commons.compress.archivers.zip.ZipExtraField
meth public boolean allowMethodChange()
meth public byte[] getCentralDirectoryData()
meth public byte[] getLocalFileDataData()
meth public org.apache.commons.compress.archivers.zip.ZipShort getCentralDirectoryLength()
meth public org.apache.commons.compress.archivers.zip.ZipShort getHeaderId()
meth public org.apache.commons.compress.archivers.zip.ZipShort getLocalFileDataLength()
meth public short getAlignment()
meth public void parseFromCentralDirectoryData(byte[],int,int) throws java.util.zip.ZipException
meth public void parseFromLocalFileData(byte[],int,int) throws java.util.zip.ZipException
supr java.lang.Object
hfds ALLOW_METHOD_MESSAGE_CHANGE_FLAG,alignment,allowMethodChange,padding

CLSS public org.apache.commons.compress.archivers.zip.ScatterStatistics
meth public java.lang.String toString()
meth public long getCompressionElapsed()
meth public long getMergingElapsed()
supr java.lang.Object
hfds compressionElapsed,mergingElapsed

CLSS public org.apache.commons.compress.archivers.zip.ScatterZipOutputStream
cons public init(org.apache.commons.compress.parallel.ScatterGatherBackingStore,org.apache.commons.compress.archivers.zip.StreamCompressor)
innr public static ZipEntryWriter
intf java.io.Closeable
meth public org.apache.commons.compress.archivers.zip.ScatterZipOutputStream$ZipEntryWriter zipEntryWriter() throws java.io.IOException
meth public static org.apache.commons.compress.archivers.zip.ScatterZipOutputStream fileBased(java.io.File) throws java.io.FileNotFoundException
meth public static org.apache.commons.compress.archivers.zip.ScatterZipOutputStream fileBased(java.io.File,int) throws java.io.FileNotFoundException
meth public static org.apache.commons.compress.archivers.zip.ScatterZipOutputStream pathBased(java.nio.file.Path) throws java.io.FileNotFoundException
meth public static org.apache.commons.compress.archivers.zip.ScatterZipOutputStream pathBased(java.nio.file.Path,int) throws java.io.FileNotFoundException
meth public void addArchiveEntry(org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequest) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void writeTo(org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream) throws java.io.IOException
supr java.lang.Object
hfds backingStore,isClosed,items,streamCompressor,zipEntryWriter
hcls CompressedEntry

CLSS public static org.apache.commons.compress.archivers.zip.ScatterZipOutputStream$ZipEntryWriter
 outer org.apache.commons.compress.archivers.zip.ScatterZipOutputStream
cons public init(org.apache.commons.compress.archivers.zip.ScatterZipOutputStream) throws java.io.IOException
intf java.io.Closeable
meth public void close() throws java.io.IOException
meth public void writeNextZipEntry(org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream) throws java.io.IOException
supr java.lang.Object
hfds itemsIterator,itemsIteratorData

CLSS public abstract org.apache.commons.compress.archivers.zip.StreamCompressor
intf java.io.Closeable
meth protected abstract void writeOut(byte[],int,int) throws java.io.IOException
meth public long getBytesRead()
meth public long getBytesWrittenForLastEntry()
meth public long getCrc32()
meth public long getTotalBytesWritten()
meth public static org.apache.commons.compress.archivers.zip.StreamCompressor create(int,org.apache.commons.compress.parallel.ScatterGatherBackingStore)
meth public static org.apache.commons.compress.archivers.zip.StreamCompressor create(org.apache.commons.compress.parallel.ScatterGatherBackingStore)
meth public void close() throws java.io.IOException
meth public void deflate(java.io.InputStream,int) throws java.io.IOException
meth public void writeCounted(byte[]) throws java.io.IOException
meth public void writeCounted(byte[],int,int) throws java.io.IOException
supr java.lang.Object
hfds BUFFER_SIZE,DEFLATER_BLOCK_SIZE,crc,def,outputBuffer,readerBuf,sourcePayloadLength,totalWrittenToOutputStream,writtenToOutputStreamForLastEntry
hcls DataOutputCompressor,OutputStreamCompressor,ScatterGatherBackingStoreCompressor,SeekableByteChannelCompressor

CLSS public org.apache.commons.compress.archivers.zip.UnicodeCommentExtraField
cons public init()
cons public init(java.lang.String,byte[])
cons public init(java.lang.String,byte[],int,int)
fld public final static org.apache.commons.compress.archivers.zip.ZipShort UCOM_ID
meth public org.apache.commons.compress.archivers.zip.ZipShort getHeaderId()
supr org.apache.commons.compress.archivers.zip.AbstractUnicodeExtraField

CLSS public org.apache.commons.compress.archivers.zip.UnicodePathExtraField
cons public init()
cons public init(java.lang.String,byte[])
cons public init(java.lang.String,byte[],int,int)
fld public final static org.apache.commons.compress.archivers.zip.ZipShort UPATH_ID
meth public org.apache.commons.compress.archivers.zip.ZipShort getHeaderId()
supr org.apache.commons.compress.archivers.zip.AbstractUnicodeExtraField

CLSS public abstract interface org.apache.commons.compress.archivers.zip.UnixStat
fld public final static int DEFAULT_DIR_PERM = 493
fld public final static int DEFAULT_FILE_PERM = 420
fld public final static int DEFAULT_LINK_PERM = 511
fld public final static int DIR_FLAG = 16384
fld public final static int FILE_FLAG = 32768
fld public final static int FILE_TYPE_FLAG = 61440
fld public final static int LINK_FLAG = 40960
fld public final static int PERM_MASK = 4095

CLSS public abstract interface org.apache.commons.compress.archivers.zip.UnparseableExtraFieldBehavior
meth public abstract org.apache.commons.compress.archivers.zip.ZipExtraField onUnparseableExtraField(byte[],int,int,boolean,int) throws java.util.zip.ZipException

CLSS public final org.apache.commons.compress.archivers.zip.UnparseableExtraFieldData
cons public init()
intf org.apache.commons.compress.archivers.zip.ZipExtraField
meth public byte[] getCentralDirectoryData()
meth public byte[] getLocalFileDataData()
meth public org.apache.commons.compress.archivers.zip.ZipShort getCentralDirectoryLength()
meth public org.apache.commons.compress.archivers.zip.ZipShort getHeaderId()
meth public org.apache.commons.compress.archivers.zip.ZipShort getLocalFileDataLength()
meth public void parseFromCentralDirectoryData(byte[],int,int)
meth public void parseFromLocalFileData(byte[],int,int)
supr java.lang.Object
hfds HEADER_ID,centralDirectoryData,localFileData

CLSS public org.apache.commons.compress.archivers.zip.UnrecognizedExtraField
cons public init()
intf org.apache.commons.compress.archivers.zip.ZipExtraField
meth public byte[] getCentralDirectoryData()
meth public byte[] getLocalFileDataData()
meth public org.apache.commons.compress.archivers.zip.ZipShort getCentralDirectoryLength()
meth public org.apache.commons.compress.archivers.zip.ZipShort getHeaderId()
meth public org.apache.commons.compress.archivers.zip.ZipShort getLocalFileDataLength()
meth public void parseFromCentralDirectoryData(byte[],int,int)
meth public void parseFromLocalFileData(byte[],int,int)
meth public void setCentralDirectoryData(byte[])
meth public void setHeaderId(org.apache.commons.compress.archivers.zip.ZipShort)
meth public void setLocalFileDataData(byte[])
supr java.lang.Object
hfds centralData,headerId,localData

CLSS public org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException
cons public init(org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException$Feature)
cons public init(org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException$Feature,org.apache.commons.compress.archivers.zip.ZipArchiveEntry)
cons public init(org.apache.commons.compress.archivers.zip.ZipMethod,org.apache.commons.compress.archivers.zip.ZipArchiveEntry)
innr public static Feature
meth public org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException$Feature getFeature()
meth public org.apache.commons.compress.archivers.zip.ZipArchiveEntry getEntry()
supr java.util.zip.ZipException
hfds entry,reason,serialVersionUID

CLSS public static org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException$Feature
 outer org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException
fld public final static org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException$Feature DATA_DESCRIPTOR
fld public final static org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException$Feature ENCRYPTION
fld public final static org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException$Feature METHOD
fld public final static org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException$Feature SPLITTING
fld public final static org.apache.commons.compress.archivers.zip.UnsupportedZipFeatureException$Feature UNKNOWN_COMPRESSED_SIZE
intf java.io.Serializable
meth public java.lang.String toString()
supr java.lang.Object
hfds name,serialVersionUID

CLSS public org.apache.commons.compress.archivers.zip.X000A_NTFS
cons public init()
fld public final static org.apache.commons.compress.archivers.zip.ZipShort HEADER_ID
intf org.apache.commons.compress.archivers.zip.ZipExtraField
meth public boolean equals(java.lang.Object)
meth public byte[] getCentralDirectoryData()
meth public byte[] getLocalFileDataData()
meth public int hashCode()
meth public java.lang.String toString()
meth public java.nio.file.attribute.FileTime getAccessFileTime()
meth public java.nio.file.attribute.FileTime getCreateFileTime()
meth public java.nio.file.attribute.FileTime getModifyFileTime()
meth public java.util.Date getAccessJavaTime()
meth public java.util.Date getCreateJavaTime()
meth public java.util.Date getModifyJavaTime()
meth public org.apache.commons.compress.archivers.zip.ZipEightByteInteger getAccessTime()
meth public org.apache.commons.compress.archivers.zip.ZipEightByteInteger getCreateTime()
meth public org.apache.commons.compress.archivers.zip.ZipEightByteInteger getModifyTime()
meth public org.apache.commons.compress.archivers.zip.ZipShort getCentralDirectoryLength()
meth public org.apache.commons.compress.archivers.zip.ZipShort getHeaderId()
meth public org.apache.commons.compress.archivers.zip.ZipShort getLocalFileDataLength()
meth public void parseFromCentralDirectoryData(byte[],int,int) throws java.util.zip.ZipException
meth public void parseFromLocalFileData(byte[],int,int) throws java.util.zip.ZipException
meth public void setAccessFileTime(java.nio.file.attribute.FileTime)
meth public void setAccessJavaTime(java.util.Date)
meth public void setAccessTime(org.apache.commons.compress.archivers.zip.ZipEightByteInteger)
meth public void setCreateFileTime(java.nio.file.attribute.FileTime)
meth public void setCreateJavaTime(java.util.Date)
meth public void setCreateTime(org.apache.commons.compress.archivers.zip.ZipEightByteInteger)
meth public void setModifyFileTime(java.nio.file.attribute.FileTime)
meth public void setModifyJavaTime(java.util.Date)
meth public void setModifyTime(org.apache.commons.compress.archivers.zip.ZipEightByteInteger)
supr java.lang.Object
hfds TIME_ATTR_SIZE,TIME_ATTR_TAG,accessTime,createTime,modifyTime

CLSS public org.apache.commons.compress.archivers.zip.X0014_X509Certificates
cons public init()
supr org.apache.commons.compress.archivers.zip.PKWareExtraHeader

CLSS public org.apache.commons.compress.archivers.zip.X0015_CertificateIdForFile
cons public init()
meth public int getRecordCount()
meth public org.apache.commons.compress.archivers.zip.PKWareExtraHeader$HashAlgorithm getHashAlgorithm()
meth public void parseFromCentralDirectoryData(byte[],int,int) throws java.util.zip.ZipException
supr org.apache.commons.compress.archivers.zip.PKWareExtraHeader
hfds hashAlg,rcount

CLSS public org.apache.commons.compress.archivers.zip.X0016_CertificateIdForCentralDirectory
cons public init()
meth public int getRecordCount()
meth public org.apache.commons.compress.archivers.zip.PKWareExtraHeader$HashAlgorithm getHashAlgorithm()
meth public void parseFromCentralDirectoryData(byte[],int,int) throws java.util.zip.ZipException
supr org.apache.commons.compress.archivers.zip.PKWareExtraHeader
hfds hashAlg,rcount

CLSS public org.apache.commons.compress.archivers.zip.X0017_StrongEncryptionHeader
cons public init()
meth public long getRecordCount()
meth public org.apache.commons.compress.archivers.zip.PKWareExtraHeader$EncryptionAlgorithm getEncryptionAlgorithm()
meth public org.apache.commons.compress.archivers.zip.PKWareExtraHeader$HashAlgorithm getHashAlgorithm()
meth public void parseCentralDirectoryFormat(byte[],int,int) throws java.util.zip.ZipException
meth public void parseFileFormat(byte[],int,int) throws java.util.zip.ZipException
meth public void parseFromCentralDirectoryData(byte[],int,int) throws java.util.zip.ZipException
meth public void parseFromLocalFileData(byte[],int,int) throws java.util.zip.ZipException
supr org.apache.commons.compress.archivers.zip.PKWareExtraHeader
hfds algId,bitlen,erdData,flags,format,hashAlg,hashSize,ivData,keyBlob,rcount,recipientKeyHash,vCRC32,vData

CLSS public org.apache.commons.compress.archivers.zip.X0019_EncryptionRecipientCertificateList
cons public init()
supr org.apache.commons.compress.archivers.zip.PKWareExtraHeader

CLSS public org.apache.commons.compress.archivers.zip.X5455_ExtendedTimestamp
cons public init()
fld public final static byte ACCESS_TIME_BIT = 2
fld public final static byte CREATE_TIME_BIT = 4
fld public final static byte MODIFY_TIME_BIT = 1
fld public final static org.apache.commons.compress.archivers.zip.ZipShort HEADER_ID
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.commons.compress.archivers.zip.ZipExtraField
meth public boolean equals(java.lang.Object)
meth public boolean isBit0_modifyTimePresent()
meth public boolean isBit1_accessTimePresent()
meth public boolean isBit2_createTimePresent()
meth public byte getFlags()
meth public byte[] getCentralDirectoryData()
meth public byte[] getLocalFileDataData()
meth public int hashCode()
meth public java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public java.lang.String toString()
meth public java.nio.file.attribute.FileTime getAccessFileTime()
meth public java.nio.file.attribute.FileTime getCreateFileTime()
meth public java.nio.file.attribute.FileTime getModifyFileTime()
meth public java.util.Date getAccessJavaTime()
meth public java.util.Date getCreateJavaTime()
meth public java.util.Date getModifyJavaTime()
meth public org.apache.commons.compress.archivers.zip.ZipLong getAccessTime()
meth public org.apache.commons.compress.archivers.zip.ZipLong getCreateTime()
meth public org.apache.commons.compress.archivers.zip.ZipLong getModifyTime()
meth public org.apache.commons.compress.archivers.zip.ZipShort getCentralDirectoryLength()
meth public org.apache.commons.compress.archivers.zip.ZipShort getHeaderId()
meth public org.apache.commons.compress.archivers.zip.ZipShort getLocalFileDataLength()
meth public void parseFromCentralDirectoryData(byte[],int,int) throws java.util.zip.ZipException
meth public void parseFromLocalFileData(byte[],int,int) throws java.util.zip.ZipException
meth public void setAccessFileTime(java.nio.file.attribute.FileTime)
meth public void setAccessJavaTime(java.util.Date)
meth public void setAccessTime(org.apache.commons.compress.archivers.zip.ZipLong)
meth public void setCreateFileTime(java.nio.file.attribute.FileTime)
meth public void setCreateJavaTime(java.util.Date)
meth public void setCreateTime(org.apache.commons.compress.archivers.zip.ZipLong)
meth public void setFlags(byte)
meth public void setModifyFileTime(java.nio.file.attribute.FileTime)
meth public void setModifyJavaTime(java.util.Date)
meth public void setModifyTime(org.apache.commons.compress.archivers.zip.ZipLong)
supr java.lang.Object
hfds accessTime,bit0_modifyTimePresent,bit1_accessTimePresent,bit2_createTimePresent,createTime,flags,modifyTime,serialVersionUID

CLSS public org.apache.commons.compress.archivers.zip.X7875_NewUnix
cons public init()
intf java.io.Serializable
intf java.lang.Cloneable
intf org.apache.commons.compress.archivers.zip.ZipExtraField
meth public boolean equals(java.lang.Object)
meth public byte[] getCentralDirectoryData()
meth public byte[] getLocalFileDataData()
meth public int hashCode()
meth public java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public java.lang.String toString()
meth public long getGID()
meth public long getUID()
meth public org.apache.commons.compress.archivers.zip.ZipShort getCentralDirectoryLength()
meth public org.apache.commons.compress.archivers.zip.ZipShort getHeaderId()
meth public org.apache.commons.compress.archivers.zip.ZipShort getLocalFileDataLength()
meth public void parseFromCentralDirectoryData(byte[],int,int) throws java.util.zip.ZipException
meth public void parseFromLocalFileData(byte[],int,int) throws java.util.zip.ZipException
meth public void setGID(long)
meth public void setUID(long)
supr java.lang.Object
hfds HEADER_ID,ONE_THOUSAND,ZERO,gid,serialVersionUID,uid,version

CLSS public org.apache.commons.compress.archivers.zip.Zip64ExtendedInformationExtraField
cons public init()
cons public init(org.apache.commons.compress.archivers.zip.ZipEightByteInteger,org.apache.commons.compress.archivers.zip.ZipEightByteInteger)
cons public init(org.apache.commons.compress.archivers.zip.ZipEightByteInteger,org.apache.commons.compress.archivers.zip.ZipEightByteInteger,org.apache.commons.compress.archivers.zip.ZipEightByteInteger,org.apache.commons.compress.archivers.zip.ZipLong)
intf org.apache.commons.compress.archivers.zip.ZipExtraField
meth public byte[] getCentralDirectoryData()
meth public byte[] getLocalFileDataData()
meth public org.apache.commons.compress.archivers.zip.ZipEightByteInteger getCompressedSize()
meth public org.apache.commons.compress.archivers.zip.ZipEightByteInteger getRelativeHeaderOffset()
meth public org.apache.commons.compress.archivers.zip.ZipEightByteInteger getSize()
meth public org.apache.commons.compress.archivers.zip.ZipLong getDiskStartNumber()
meth public org.apache.commons.compress.archivers.zip.ZipShort getCentralDirectoryLength()
meth public org.apache.commons.compress.archivers.zip.ZipShort getHeaderId()
meth public org.apache.commons.compress.archivers.zip.ZipShort getLocalFileDataLength()
meth public void parseFromCentralDirectoryData(byte[],int,int) throws java.util.zip.ZipException
meth public void parseFromLocalFileData(byte[],int,int) throws java.util.zip.ZipException
meth public void reparseCentralDirectoryData(boolean,boolean,boolean,boolean) throws java.util.zip.ZipException
meth public void setCompressedSize(org.apache.commons.compress.archivers.zip.ZipEightByteInteger)
meth public void setDiskStartNumber(org.apache.commons.compress.archivers.zip.ZipLong)
meth public void setRelativeHeaderOffset(org.apache.commons.compress.archivers.zip.ZipEightByteInteger)
meth public void setSize(org.apache.commons.compress.archivers.zip.ZipEightByteInteger)
supr java.lang.Object
hfds HEADER_ID,LFH_MUST_HAVE_BOTH_SIZES_MSG,compressedSize,diskStart,rawCentralDirectoryData,relativeHeaderOffset,size

CLSS public final !enum org.apache.commons.compress.archivers.zip.Zip64Mode
fld public final static org.apache.commons.compress.archivers.zip.Zip64Mode Always
fld public final static org.apache.commons.compress.archivers.zip.Zip64Mode AlwaysWithCompatibility
fld public final static org.apache.commons.compress.archivers.zip.Zip64Mode AsNeeded
fld public final static org.apache.commons.compress.archivers.zip.Zip64Mode Never
meth public static org.apache.commons.compress.archivers.zip.Zip64Mode valueOf(java.lang.String)
meth public static org.apache.commons.compress.archivers.zip.Zip64Mode[] values()
supr java.lang.Enum<org.apache.commons.compress.archivers.zip.Zip64Mode>

CLSS public org.apache.commons.compress.archivers.zip.Zip64RequiredException
cons public init(java.lang.String)
supr java.util.zip.ZipException
hfds ARCHIVE_TOO_BIG_MESSAGE,NUMBER_OF_THE_DISK_OF_CENTRAL_DIRECTORY_TOO_BIG_MESSAGE,NUMBER_OF_THIS_DISK_TOO_BIG_MESSAGE,SIZE_OF_CENTRAL_DIRECTORY_TOO_BIG_MESSAGE,TOO_MANY_ENTRIES_MESSAGE,TOO_MANY_ENTRIES_ON_THIS_DISK_MESSAGE,serialVersionUID

CLSS public org.apache.commons.compress.archivers.zip.ZipArchiveEntry
cons protected init()
cons public !varargs init(java.nio.file.Path,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
cons public init(java.io.File,java.lang.String)
cons public init(java.lang.String)
cons public init(java.util.zip.ZipEntry) throws java.util.zip.ZipException
cons public init(org.apache.commons.compress.archivers.zip.ZipArchiveEntry) throws java.util.zip.ZipException
fld public final static int CENATT = 36
fld public final static int CENATX = 38
fld public final static int CENCOM = 32
fld public final static int CENCRC = 16
fld public final static int CENDSK = 34
fld public final static int CENEXT = 30
fld public final static int CENFLG = 8
fld public final static int CENHDR = 46
fld public final static int CENHOW = 10
fld public final static int CENLEN = 24
fld public final static int CENNAM = 28
fld public final static int CENOFF = 42
fld public final static int CENSIZ = 20
fld public final static int CENTIM = 12
fld public final static int CENVEM = 4
fld public final static int CENVER = 6
fld public final static int CRC_UNKNOWN = -1
fld public final static int ENDCOM = 20
fld public final static int ENDHDR = 22
fld public final static int ENDOFF = 16
fld public final static int ENDSIZ = 12
fld public final static int ENDSUB = 8
fld public final static int ENDTOT = 10
fld public final static int EXTCRC = 4
fld public final static int EXTHDR = 16
fld public final static int EXTLEN = 12
fld public final static int EXTSIZ = 8
fld public final static int LOCCRC = 14
fld public final static int LOCEXT = 28
fld public final static int LOCFLG = 6
fld public final static int LOCHDR = 30
fld public final static int LOCHOW = 8
fld public final static int LOCLEN = 22
fld public final static int LOCNAM = 26
fld public final static int LOCSIZ = 18
fld public final static int LOCTIM = 10
fld public final static int LOCVER = 4
fld public final static int PLATFORM_FAT = 0
fld public final static int PLATFORM_UNIX = 3
fld public final static long CENSIG = 33639248
fld public final static long ENDSIG = 101010256
fld public final static long EXTSIG = 134695760
fld public final static long LOCSIG = 67324752
innr public final static !enum CommentSource
innr public final static !enum NameSource
innr public static !enum ExtraFieldParsingMode
intf org.apache.commons.compress.archivers.ArchiveEntry
intf org.apache.commons.compress.archivers.EntryStreamOffsets
meth protected int getAlignment()
meth protected void setDataOffset(long)
meth protected void setExtra()
meth protected void setLocalHeaderOffset(long)
meth protected void setName(java.lang.String)
meth protected void setName(java.lang.String,byte[])
meth protected void setPlatform(int)
meth protected void setStreamContiguous(boolean)
meth public boolean equals(java.lang.Object)
meth public boolean isDirectory()
meth public boolean isStreamContiguous()
meth public boolean isUnixSymlink()
meth public byte[] getCentralDirectoryExtra()
meth public byte[] getLocalFileDataExtra()
meth public byte[] getRawName()
meth public int getInternalAttributes()
meth public int getMethod()
meth public int getPlatform()
meth public int getRawFlag()
meth public int getUnixMode()
meth public int getVersionMadeBy()
meth public int getVersionRequired()
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String getName()
meth public java.util.Date getLastModifiedDate()
meth public java.util.zip.ZipEntry setCreationTime(java.nio.file.attribute.FileTime)
meth public java.util.zip.ZipEntry setLastAccessTime(java.nio.file.attribute.FileTime)
meth public java.util.zip.ZipEntry setLastModifiedTime(java.nio.file.attribute.FileTime)
meth public long getDataOffset()
meth public long getDiskNumberStart()
meth public long getExternalAttributes()
meth public long getLocalHeaderOffset()
meth public long getSize()
meth public long getTime()
meth public org.apache.commons.compress.archivers.zip.GeneralPurposeBit getGeneralPurposeBit()
meth public org.apache.commons.compress.archivers.zip.UnparseableExtraFieldData getUnparseableExtraFieldData()
meth public org.apache.commons.compress.archivers.zip.ZipArchiveEntry$CommentSource getCommentSource()
meth public org.apache.commons.compress.archivers.zip.ZipArchiveEntry$NameSource getNameSource()
meth public org.apache.commons.compress.archivers.zip.ZipExtraField getExtraField(org.apache.commons.compress.archivers.zip.ZipShort)
meth public org.apache.commons.compress.archivers.zip.ZipExtraField[] getExtraFields()
meth public org.apache.commons.compress.archivers.zip.ZipExtraField[] getExtraFields(boolean)
meth public org.apache.commons.compress.archivers.zip.ZipExtraField[] getExtraFields(org.apache.commons.compress.archivers.zip.ExtraFieldParsingBehavior) throws java.util.zip.ZipException
meth public void addAsFirstExtraField(org.apache.commons.compress.archivers.zip.ZipExtraField)
meth public void addExtraField(org.apache.commons.compress.archivers.zip.ZipExtraField)
meth public void removeExtraField(org.apache.commons.compress.archivers.zip.ZipShort)
meth public void removeUnparseableExtraFieldData()
meth public void setAlignment(int)
meth public void setCentralDirectoryExtra(byte[])
meth public void setCommentSource(org.apache.commons.compress.archivers.zip.ZipArchiveEntry$CommentSource)
meth public void setDiskNumberStart(long)
meth public void setExternalAttributes(long)
meth public void setExtra(byte[])
meth public void setExtraFields(org.apache.commons.compress.archivers.zip.ZipExtraField[])
meth public void setGeneralPurposeBit(org.apache.commons.compress.archivers.zip.GeneralPurposeBit)
meth public void setInternalAttributes(int)
meth public void setMethod(int)
meth public void setNameSource(org.apache.commons.compress.archivers.zip.ZipArchiveEntry$NameSource)
meth public void setRawFlag(int)
meth public void setSize(long)
meth public void setTime(java.nio.file.attribute.FileTime)
meth public void setTime(long)
meth public void setUnixMode(int)
meth public void setVersionMadeBy(int)
meth public void setVersionRequired(int)
supr java.util.zip.ZipEntry
hfds EMPTY_ARRAY,EMPTY_LINKED_LIST,SHORT_MASK,SHORT_SHIFT,alignment,commentSource,dataOffset,diskNumberStart,externalAttributes,extraFields,gpb,internalAttributes,isStreamContiguous,lastModifiedDateSet,localHeaderOffset,method,name,nameSource,platform,rawFlag,rawName,size,time,unparseableExtra,versionMadeBy,versionRequired

CLSS public final static !enum org.apache.commons.compress.archivers.zip.ZipArchiveEntry$CommentSource
 outer org.apache.commons.compress.archivers.zip.ZipArchiveEntry
fld public final static org.apache.commons.compress.archivers.zip.ZipArchiveEntry$CommentSource COMMENT
fld public final static org.apache.commons.compress.archivers.zip.ZipArchiveEntry$CommentSource UNICODE_EXTRA_FIELD
meth public static org.apache.commons.compress.archivers.zip.ZipArchiveEntry$CommentSource valueOf(java.lang.String)
meth public static org.apache.commons.compress.archivers.zip.ZipArchiveEntry$CommentSource[] values()
supr java.lang.Enum<org.apache.commons.compress.archivers.zip.ZipArchiveEntry$CommentSource>

CLSS public static !enum org.apache.commons.compress.archivers.zip.ZipArchiveEntry$ExtraFieldParsingMode
 outer org.apache.commons.compress.archivers.zip.ZipArchiveEntry
fld public final static org.apache.commons.compress.archivers.zip.ZipArchiveEntry$ExtraFieldParsingMode BEST_EFFORT
fld public final static org.apache.commons.compress.archivers.zip.ZipArchiveEntry$ExtraFieldParsingMode DRACONIC
fld public final static org.apache.commons.compress.archivers.zip.ZipArchiveEntry$ExtraFieldParsingMode ONLY_PARSEABLE_LENIENT
fld public final static org.apache.commons.compress.archivers.zip.ZipArchiveEntry$ExtraFieldParsingMode ONLY_PARSEABLE_STRICT
fld public final static org.apache.commons.compress.archivers.zip.ZipArchiveEntry$ExtraFieldParsingMode STRICT_FOR_KNOW_EXTRA_FIELDS
intf org.apache.commons.compress.archivers.zip.ExtraFieldParsingBehavior
meth public org.apache.commons.compress.archivers.zip.ZipExtraField createExtraField(org.apache.commons.compress.archivers.zip.ZipShort) throws java.lang.IllegalAccessException,java.lang.InstantiationException,java.util.zip.ZipException
meth public org.apache.commons.compress.archivers.zip.ZipExtraField fill(org.apache.commons.compress.archivers.zip.ZipExtraField,byte[],int,int,boolean) throws java.util.zip.ZipException
meth public org.apache.commons.compress.archivers.zip.ZipExtraField onUnparseableExtraField(byte[],int,int,boolean,int) throws java.util.zip.ZipException
meth public static org.apache.commons.compress.archivers.zip.ZipArchiveEntry$ExtraFieldParsingMode valueOf(java.lang.String)
meth public static org.apache.commons.compress.archivers.zip.ZipArchiveEntry$ExtraFieldParsingMode[] values()
supr java.lang.Enum<org.apache.commons.compress.archivers.zip.ZipArchiveEntry$ExtraFieldParsingMode>
hfds onUnparseableData

CLSS public final static !enum org.apache.commons.compress.archivers.zip.ZipArchiveEntry$NameSource
 outer org.apache.commons.compress.archivers.zip.ZipArchiveEntry
fld public final static org.apache.commons.compress.archivers.zip.ZipArchiveEntry$NameSource NAME
fld public final static org.apache.commons.compress.archivers.zip.ZipArchiveEntry$NameSource NAME_WITH_EFS_FLAG
fld public final static org.apache.commons.compress.archivers.zip.ZipArchiveEntry$NameSource UNICODE_EXTRA_FIELD
meth public static org.apache.commons.compress.archivers.zip.ZipArchiveEntry$NameSource valueOf(java.lang.String)
meth public static org.apache.commons.compress.archivers.zip.ZipArchiveEntry$NameSource[] values()
supr java.lang.Enum<org.apache.commons.compress.archivers.zip.ZipArchiveEntry$NameSource>

CLSS public abstract interface org.apache.commons.compress.archivers.zip.ZipArchiveEntryPredicate
meth public abstract boolean test(org.apache.commons.compress.archivers.zip.ZipArchiveEntry)

CLSS public org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequest
meth public int getMethod()
meth public java.io.InputStream getPayloadStream()
meth public static org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequest createZipArchiveEntryRequest(org.apache.commons.compress.archivers.zip.ZipArchiveEntry,org.apache.commons.compress.parallel.InputStreamSupplier)
supr java.lang.Object
hfds method,payloadSupplier,zipArchiveEntry

CLSS public abstract interface org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequestSupplier
meth public abstract org.apache.commons.compress.archivers.zip.ZipArchiveEntryRequest get()

CLSS public org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
cons public init(java.io.InputStream)
cons public init(java.io.InputStream,java.lang.String)
cons public init(java.io.InputStream,java.lang.String,boolean)
cons public init(java.io.InputStream,java.lang.String,boolean,boolean)
cons public init(java.io.InputStream,java.lang.String,boolean,boolean,boolean)
intf org.apache.commons.compress.utils.InputStreamStatistics
meth public boolean canReadEntryData(org.apache.commons.compress.archivers.ArchiveEntry)
meth public int read(byte[],int,int) throws java.io.IOException
meth public long getCompressedCount()
meth public long getUncompressedCount()
meth public long skip(long) throws java.io.IOException
meth public org.apache.commons.compress.archivers.zip.ZipArchiveEntry getNextEntry() throws java.io.IOException
meth public org.apache.commons.compress.archivers.zip.ZipArchiveEntry getNextZipEntry() throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public static boolean matches(byte[],int)
meth public void close() throws java.io.IOException
supr org.apache.commons.compress.archivers.ArchiveInputStream<org.apache.commons.compress.archivers.zip.ZipArchiveEntry>
hfds APK_SIGNING_BLOCK_MAGIC,CFH,CFH_LEN,DD,LFH,LFH_LEN,LONG_MAX,TWO_EXP_32,USE_ZIPFILE_INSTEAD_OF_STREAM_DISCLAIMER,allowStoredEntriesWithDataDescriptor,buf,closed,current,encoding,entriesRead,hitCentralDirectory,inf,inputStream,lastStoredEntry,lfhBuf,shortBuf,skipBuf,skipSplitSig,twoDwordBuf,uncompressedCount,useUnicodeExtraFields,wordBuf,zipEncoding
hcls BoundedInputStream,CurrentEntry

CLSS public org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
cons public !varargs init(java.nio.file.Path,java.nio.file.OpenOption[]) throws java.io.IOException
cons public init(java.io.File) throws java.io.IOException
cons public init(java.io.File,long) throws java.io.IOException
cons public init(java.io.OutputStream)
cons public init(java.nio.channels.SeekableByteChannel)
cons public init(java.nio.file.Path,long) throws java.io.IOException
fld protected boolean finished
fld protected final java.util.zip.Deflater def
fld public final static int DEFAULT_COMPRESSION = -1
fld public final static int DEFLATED = 8
fld public final static int EFS_FLAG = 2048
 anno 0 java.lang.Deprecated()
fld public final static int STORED = 0
innr public final static UnicodeExtraFieldPolicy
meth protected final void deflate() throws java.io.IOException
meth protected final void writeOut(byte[]) throws java.io.IOException
meth protected final void writeOut(byte[],int,int) throws java.io.IOException
meth protected void writeCentralDirectoryEnd() throws java.io.IOException
meth protected void writeCentralFileHeader(org.apache.commons.compress.archivers.zip.ZipArchiveEntry) throws java.io.IOException
meth protected void writeDataDescriptor(org.apache.commons.compress.archivers.zip.ZipArchiveEntry) throws java.io.IOException
meth protected void writeLocalFileHeader(org.apache.commons.compress.archivers.zip.ZipArchiveEntry) throws java.io.IOException
meth protected void writeZip64CentralDirectory() throws java.io.IOException
meth public !varargs org.apache.commons.compress.archivers.zip.ZipArchiveEntry createArchiveEntry(java.nio.file.Path,java.lang.String,java.nio.file.LinkOption[]) throws java.io.IOException
meth public boolean canWriteEntryData(org.apache.commons.compress.archivers.ArchiveEntry)
meth public boolean isSeekable()
meth public java.lang.String getEncoding()
meth public long getBytesWritten()
meth public org.apache.commons.compress.archivers.zip.ZipArchiveEntry createArchiveEntry(java.io.File,java.lang.String) throws java.io.IOException
meth public void addRawArchiveEntry(org.apache.commons.compress.archivers.zip.ZipArchiveEntry,java.io.InputStream) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void closeArchiveEntry() throws java.io.IOException
meth public void finish() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void putArchiveEntry(org.apache.commons.compress.archivers.zip.ZipArchiveEntry) throws java.io.IOException
meth public void setComment(java.lang.String)
meth public void setCreateUnicodeExtraFields(org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream$UnicodeExtraFieldPolicy)
meth public void setEncoding(java.lang.String)
meth public void setFallbackToUTF8(boolean)
meth public void setLevel(int)
meth public void setMethod(int)
meth public void setUseLanguageEncodingFlag(boolean)
meth public void setUseZip64(org.apache.commons.compress.archivers.zip.Zip64Mode)
meth public void write(byte[],int,int) throws java.io.IOException
meth public void writePreamble(byte[]) throws java.io.IOException
meth public void writePreamble(byte[],int,int) throws java.io.IOException
supr org.apache.commons.compress.archivers.ArchiveOutputStream<org.apache.commons.compress.archivers.zip.ZipArchiveEntry>
hfds BUFFER_SIZE,CFH_COMMENT_LENGTH_OFFSET,CFH_COMPRESSED_SIZE_OFFSET,CFH_CRC_OFFSET,CFH_DISK_NUMBER_OFFSET,CFH_EXTERNAL_ATTRIBUTES_OFFSET,CFH_EXTRA_LENGTH_OFFSET,CFH_FILENAME_LENGTH_OFFSET,CFH_FILENAME_OFFSET,CFH_GPB_OFFSET,CFH_INTERNAL_ATTRIBUTES_OFFSET,CFH_LFH_OFFSET,CFH_METHOD_OFFSET,CFH_ORIGINAL_SIZE_OFFSET,CFH_SIG,CFH_SIG_OFFSET,CFH_TIME_OFFSET,CFH_VERSION_MADE_BY_OFFSET,CFH_VERSION_NEEDED_OFFSET,DD_SIG,DEFAULT_ENCODING,EOCD_SIG,LFH_COMPRESSED_SIZE_OFFSET,LFH_CRC_OFFSET,LFH_EXTRA_LENGTH_OFFSET,LFH_FILENAME_LENGTH_OFFSET,LFH_FILENAME_OFFSET,LFH_GPB_OFFSET,LFH_METHOD_OFFSET,LFH_ORIGINAL_SIZE_OFFSET,LFH_SIG,LFH_SIG_OFFSET,LFH_TIME_OFFSET,LFH_VERSION_NEEDED_OFFSET,LZERO,ONE,ZERO,ZIP64_EOCD_LOC_SIG,ZIP64_EOCD_SIG,cdDiskNumberStart,cdLength,cdOffset,channel,comment,copyBuffer,createUnicodeExtraFields,encoding,entries,entry,eocdLength,fallbackToUTF8,hasCompressionLevelChanged,hasUsedZip64,isSplitZip,level,metaData,method,numberOfCDInDiskData,outputStream,streamCompressor,useUTF8Flag,zip64Mode,zipEncoding
hcls CurrentEntry,EntryMetaData

CLSS public final static org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream$UnicodeExtraFieldPolicy
 outer org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream
fld public final static org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream$UnicodeExtraFieldPolicy ALWAYS
fld public final static org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream$UnicodeExtraFieldPolicy NEVER
fld public final static org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream$UnicodeExtraFieldPolicy NOT_ENCODEABLE
meth public java.lang.String toString()
supr java.lang.Object
hfds name

CLSS public final org.apache.commons.compress.archivers.zip.ZipEightByteInteger
cons public init(byte[])
cons public init(byte[],int)
cons public init(java.math.BigInteger)
cons public init(long)
fld public final static org.apache.commons.compress.archivers.zip.ZipEightByteInteger ZERO
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public byte[] getBytes()
meth public int hashCode()
meth public java.lang.String toString()
meth public java.math.BigInteger getValue()
meth public long getLongValue()
meth public static byte[] getBytes(java.math.BigInteger)
meth public static byte[] getBytes(long)
meth public static java.math.BigInteger getValue(byte[])
meth public static java.math.BigInteger getValue(byte[],int)
meth public static long getLongValue(byte[])
meth public static long getLongValue(byte[],int)
supr java.lang.Object
hfds BYTE_1,BYTE_1_MASK,BYTE_1_SHIFT,BYTE_2,BYTE_2_MASK,BYTE_2_SHIFT,BYTE_3,BYTE_3_MASK,BYTE_3_SHIFT,BYTE_4,BYTE_4_MASK,BYTE_4_SHIFT,BYTE_5,BYTE_5_MASK,BYTE_5_SHIFT,BYTE_6,BYTE_6_MASK,BYTE_6_SHIFT,BYTE_7,BYTE_7_MASK,BYTE_7_SHIFT,LEFTMOST_BIT,LEFTMOST_BIT_SHIFT,serialVersionUID,value

CLSS public abstract interface org.apache.commons.compress.archivers.zip.ZipEncoding
meth public abstract boolean canEncode(java.lang.String)
meth public abstract java.lang.String decode(byte[]) throws java.io.IOException
meth public abstract java.nio.ByteBuffer encode(java.lang.String) throws java.io.IOException

CLSS public abstract org.apache.commons.compress.archivers.zip.ZipEncodingHelper
cons public init()
meth public static org.apache.commons.compress.archivers.zip.ZipEncoding getZipEncoding(java.lang.String)
supr java.lang.Object
hfds ZIP_ENCODING_UTF_8

CLSS public abstract interface org.apache.commons.compress.archivers.zip.ZipExtraField
fld public final static int EXTRAFIELD_HEADER_SIZE = 4
meth public abstract byte[] getCentralDirectoryData()
meth public abstract byte[] getLocalFileDataData()
meth public abstract org.apache.commons.compress.archivers.zip.ZipShort getCentralDirectoryLength()
meth public abstract org.apache.commons.compress.archivers.zip.ZipShort getHeaderId()
meth public abstract org.apache.commons.compress.archivers.zip.ZipShort getLocalFileDataLength()
meth public abstract void parseFromCentralDirectoryData(byte[],int,int) throws java.util.zip.ZipException
meth public abstract void parseFromLocalFileData(byte[],int,int) throws java.util.zip.ZipException

CLSS public org.apache.commons.compress.archivers.zip.ZipFile
cons public init(java.io.File) throws java.io.IOException
cons public init(java.io.File,java.lang.String) throws java.io.IOException
cons public init(java.io.File,java.lang.String,boolean) throws java.io.IOException
cons public init(java.io.File,java.lang.String,boolean,boolean) throws java.io.IOException
cons public init(java.lang.String) throws java.io.IOException
cons public init(java.lang.String,java.lang.String) throws java.io.IOException
cons public init(java.nio.channels.SeekableByteChannel) throws java.io.IOException
cons public init(java.nio.channels.SeekableByteChannel,java.lang.String) throws java.io.IOException
cons public init(java.nio.channels.SeekableByteChannel,java.lang.String,java.lang.String,boolean) throws java.io.IOException
cons public init(java.nio.channels.SeekableByteChannel,java.lang.String,java.lang.String,boolean,boolean) throws java.io.IOException
cons public init(java.nio.file.Path) throws java.io.IOException
cons public init(java.nio.file.Path,java.lang.String) throws java.io.IOException
cons public init(java.nio.file.Path,java.lang.String,boolean) throws java.io.IOException
cons public init(java.nio.file.Path,java.lang.String,boolean,boolean) throws java.io.IOException
intf java.io.Closeable
meth protected void finalize() throws java.lang.Throwable
meth public boolean canReadEntryData(org.apache.commons.compress.archivers.zip.ZipArchiveEntry)
meth public java.io.InputStream getContentBeforeFirstLocalFileHeader()
meth public java.io.InputStream getInputStream(org.apache.commons.compress.archivers.zip.ZipArchiveEntry) throws java.io.IOException
meth public java.io.InputStream getRawInputStream(org.apache.commons.compress.archivers.zip.ZipArchiveEntry) throws java.io.IOException
meth public java.lang.Iterable<org.apache.commons.compress.archivers.zip.ZipArchiveEntry> getEntries(java.lang.String)
meth public java.lang.Iterable<org.apache.commons.compress.archivers.zip.ZipArchiveEntry> getEntriesInPhysicalOrder(java.lang.String)
meth public java.lang.String getEncoding()
meth public java.lang.String getUnixSymlink(org.apache.commons.compress.archivers.zip.ZipArchiveEntry) throws java.io.IOException
meth public java.util.Enumeration<org.apache.commons.compress.archivers.zip.ZipArchiveEntry> getEntries()
meth public java.util.Enumeration<org.apache.commons.compress.archivers.zip.ZipArchiveEntry> getEntriesInPhysicalOrder()
meth public long getFirstLocalFileHeaderOffset()
meth public org.apache.commons.compress.archivers.zip.ZipArchiveEntry getEntry(java.lang.String)
meth public static void closeQuietly(org.apache.commons.compress.archivers.zip.ZipFile)
meth public void close() throws java.io.IOException
meth public void copyRawEntries(org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream,org.apache.commons.compress.archivers.zip.ZipArchiveEntryPredicate) throws java.io.IOException
supr java.lang.Object
hfds BYTE_SHIFT,CFD_DISK_OFFSET,CFD_LENGTH_OFFSET,CFD_LOCATOR_RELATIVE_OFFSET,CFH_LEN,CFH_SIG,HASH_SIZE,LFH_OFFSET_FOR_FILENAME_LENGTH,MAX_EOCD_SIZE,MIN_EOCD_SIZE,NIBLET_MASK,ONE_ZERO_BYTE,POS_0,POS_1,POS_2,POS_3,ZIP64_EOCDL_LENGTH,ZIP64_EOCDL_LOCATOR_OFFSET,ZIP64_EOCD_CFD_DISK_OFFSET,ZIP64_EOCD_CFD_LOCATOR_OFFSET,ZIP64_EOCD_CFD_LOCATOR_RELATIVE_OFFSET,archive,archiveName,centralDirectoryStartDiskNumber,centralDirectoryStartOffset,centralDirectoryStartRelativeOffset,cfhBbuf,cfhBuf,closed,dwordBbuf,dwordBuf,encoding,entries,firstLocalFileHeaderOffset,isSplitZipArchive,nameMap,offsetComparator,shortBbuf,shortBuf,useUnicodeExtraFields,wordBbuf,wordBuf,zipEncoding
hcls BoundedFileChannelInputStream,Entry,NameAndComment,StoredStatisticsStream

CLSS public final org.apache.commons.compress.archivers.zip.ZipLong
cons public init(byte[])
cons public init(byte[],int)
cons public init(int)
cons public init(long)
fld public final static org.apache.commons.compress.archivers.zip.ZipLong AED_SIG
fld public final static org.apache.commons.compress.archivers.zip.ZipLong CFH_SIG
fld public final static org.apache.commons.compress.archivers.zip.ZipLong DD_SIG
fld public final static org.apache.commons.compress.archivers.zip.ZipLong LFH_SIG
fld public final static org.apache.commons.compress.archivers.zip.ZipLong SINGLE_SEGMENT_SPLIT_MARKER
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public byte[] getBytes()
meth public int getIntValue()
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public long getValue()
meth public static byte[] getBytes(long)
meth public static long getValue(byte[])
meth public static long getValue(byte[],int)
meth public static void putLong(long,byte[],int)
meth public void putLong(byte[],int)
supr java.lang.Object
hfds ZIP64_MAGIC,serialVersionUID,value

CLSS public final !enum org.apache.commons.compress.archivers.zip.ZipMethod
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod AES_ENCRYPTED
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod BZIP2
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod DEFLATED
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod ENHANCED_DEFLATED
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod EXPANDING_LEVEL_1
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod EXPANDING_LEVEL_2
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod EXPANDING_LEVEL_3
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod EXPANDING_LEVEL_4
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod IMPLODING
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod JPEG
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod LZMA
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod PKWARE_IMPLODING
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod PPMD
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod STORED
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod TOKENIZATION
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod UNKNOWN
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod UNSHRINKING
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod WAVPACK
fld public final static org.apache.commons.compress.archivers.zip.ZipMethod XZ
meth public int getCode()
meth public static org.apache.commons.compress.archivers.zip.ZipMethod getMethodByCode(int)
meth public static org.apache.commons.compress.archivers.zip.ZipMethod valueOf(java.lang.String)
meth public static org.apache.commons.compress.archivers.zip.ZipMethod[] values()
supr java.lang.Enum<org.apache.commons.compress.archivers.zip.ZipMethod>
hfds UNKNOWN_CODE,code,codeToEnum

CLSS public final org.apache.commons.compress.archivers.zip.ZipShort
cons public init(byte[])
cons public init(byte[],int)
cons public init(int)
fld public final static org.apache.commons.compress.archivers.zip.ZipShort ZERO
intf java.io.Serializable
intf java.lang.Cloneable
meth public boolean equals(java.lang.Object)
meth public byte[] getBytes()
meth public int getValue()
meth public int hashCode()
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public static byte[] getBytes(int)
meth public static int getValue(byte[])
meth public static int getValue(byte[],int)
meth public static void putShort(int,byte[],int)
supr java.lang.Object
hfds serialVersionUID,value

CLSS public org.apache.commons.compress.archivers.zip.ZipSplitReadOnlySeekableByteChannel
cons public init(java.util.List<java.nio.channels.SeekableByteChannel>) throws java.io.IOException
meth public !varargs static java.nio.channels.SeekableByteChannel forFiles(java.io.File[]) throws java.io.IOException
meth public !varargs static java.nio.channels.SeekableByteChannel forOrderedSeekableByteChannels(java.nio.channels.SeekableByteChannel[]) throws java.io.IOException
meth public !varargs static java.nio.channels.SeekableByteChannel forPaths(java.nio.file.Path[]) throws java.io.IOException
meth public static java.nio.channels.SeekableByteChannel buildFromLastSplitSegment(java.io.File) throws java.io.IOException
meth public static java.nio.channels.SeekableByteChannel buildFromLastSplitSegment(java.nio.file.Path) throws java.io.IOException
meth public static java.nio.channels.SeekableByteChannel forFiles(java.io.File,java.lang.Iterable<java.io.File>) throws java.io.IOException
meth public static java.nio.channels.SeekableByteChannel forOrderedSeekableByteChannels(java.nio.channels.SeekableByteChannel,java.lang.Iterable<java.nio.channels.SeekableByteChannel>) throws java.io.IOException
meth public static java.nio.channels.SeekableByteChannel forPaths(java.nio.file.Path,java.lang.Iterable<java.nio.file.Path>) throws java.io.IOException
supr org.apache.commons.compress.utils.MultiReadOnlySeekableByteChannel
hfds EMPTY_PATH_ARRAY,ZIP_SPLIT_SIGNATURE_LENGTH,zipSplitSignatureByteBuffer
hcls ZipSplitSegmentComparator

CLSS public abstract org.apache.commons.compress.archivers.zip.ZipUtil
cons public init()
meth public static boolean isDosTime(long)
meth public static byte unsignedIntToSignedByte(int)
meth public static byte[] reverse(byte[])
meth public static byte[] toDosTime(long)
meth public static int signedByteToUnsignedInt(byte)
meth public static java.util.Date fromDosTime(org.apache.commons.compress.archivers.zip.ZipLong)
meth public static long adjustToLong(int)
meth public static long dosToJavaTime(long)
meth public static org.apache.commons.compress.archivers.zip.ZipLong toDosTime(java.util.Date)
meth public static void toDosTime(long,byte[],int)
supr java.lang.Object
hfds DOSTIME_BEFORE_1980,UPPER_DOSTIME_BOUND

CLSS abstract interface org.apache.commons.compress.archivers.zip.package-info

CLSS public abstract interface org.apache.commons.compress.parallel.ScatterGatherBackingStoreSupplier
meth public abstract org.apache.commons.compress.parallel.ScatterGatherBackingStore get() throws java.io.IOException

CLSS public org.apache.commons.compress.utils.ArchiveUtils
meth public static boolean isArrayZero(byte[],int)
meth public static boolean isEqual(byte[],byte[])
meth public static boolean isEqual(byte[],byte[],boolean)
meth public static boolean isEqual(byte[],int,int,byte[],int,int)
meth public static boolean isEqual(byte[],int,int,byte[],int,int,boolean)
meth public static boolean isEqualWithNull(byte[],int,int,byte[],int,int)
meth public static boolean matchAsciiBuffer(java.lang.String,byte[])
meth public static boolean matchAsciiBuffer(java.lang.String,byte[],int,int)
meth public static byte[] toAsciiBytes(java.lang.String)
meth public static java.lang.String sanitize(java.lang.String)
meth public static java.lang.String toAsciiString(byte[])
meth public static java.lang.String toAsciiString(byte[],int,int)
meth public static java.lang.String toString(org.apache.commons.compress.archivers.ArchiveEntry)
supr java.lang.Object
hfds MAX_SANITIZED_NAME_LENGTH

CLSS public org.apache.commons.compress.utils.BitInputStream
cons public init(java.io.InputStream,java.nio.ByteOrder)
intf java.io.Closeable
meth public int bitsCached()
meth public long bitsAvailable() throws java.io.IOException
meth public long getBytesRead()
meth public long readBits(int) throws java.io.IOException
meth public void alignWithByteBoundary()
meth public void clearBitCache()
meth public void close() throws java.io.IOException
supr java.lang.Object
hfds MASKS,MAXIMUM_CACHE_SIZE,bitsCached,bitsCachedSize,byteOrder,in

CLSS public abstract org.apache.commons.compress.utils.BoundedArchiveInputStream
cons public init(long,long)
meth protected abstract int read(long,java.nio.ByteBuffer) throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
supr java.io.InputStream
hfds end,loc,singleByteBuffer

CLSS public org.apache.commons.compress.utils.BoundedInputStream
cons public init(java.io.InputStream,long)
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long getBytesRemaining()
meth public long skip(long) throws java.io.IOException
meth public void close()
supr java.io.FilterInputStream
hfds bytesRemaining

CLSS public org.apache.commons.compress.utils.BoundedSeekableByteChannelInputStream
cons public init(long,long,java.nio.channels.SeekableByteChannel)
meth protected int read(long,java.nio.ByteBuffer) throws java.io.IOException
supr org.apache.commons.compress.utils.BoundedArchiveInputStream
hfds channel

CLSS public final org.apache.commons.compress.utils.ByteUtils
fld public final static byte[] EMPTY_BYTE_ARRAY
innr public abstract interface static ByteConsumer
innr public abstract interface static ByteSupplier
innr public static InputStreamByteSupplier
innr public static OutputStreamByteConsumer
meth public static long fromLittleEndian(byte[])
meth public static long fromLittleEndian(byte[],int,int)
meth public static long fromLittleEndian(java.io.DataInput,int) throws java.io.IOException
meth public static long fromLittleEndian(java.io.InputStream,int) throws java.io.IOException
meth public static long fromLittleEndian(org.apache.commons.compress.utils.ByteUtils$ByteSupplier,int) throws java.io.IOException
meth public static void toLittleEndian(byte[],long,int,int)
meth public static void toLittleEndian(java.io.DataOutput,long,int) throws java.io.IOException
meth public static void toLittleEndian(java.io.OutputStream,long,int) throws java.io.IOException
meth public static void toLittleEndian(org.apache.commons.compress.utils.ByteUtils$ByteConsumer,long,int) throws java.io.IOException
supr java.lang.Object

CLSS public abstract interface static org.apache.commons.compress.utils.ByteUtils$ByteConsumer
 outer org.apache.commons.compress.utils.ByteUtils
meth public abstract void accept(int) throws java.io.IOException

CLSS public abstract interface static org.apache.commons.compress.utils.ByteUtils$ByteSupplier
 outer org.apache.commons.compress.utils.ByteUtils
meth public abstract int getAsByte() throws java.io.IOException

CLSS public static org.apache.commons.compress.utils.ByteUtils$InputStreamByteSupplier
 outer org.apache.commons.compress.utils.ByteUtils
cons public init(java.io.InputStream)
intf org.apache.commons.compress.utils.ByteUtils$ByteSupplier
meth public int getAsByte() throws java.io.IOException
supr java.lang.Object
hfds is

CLSS public static org.apache.commons.compress.utils.ByteUtils$OutputStreamByteConsumer
 outer org.apache.commons.compress.utils.ByteUtils
cons public init(java.io.OutputStream)
intf org.apache.commons.compress.utils.ByteUtils$ByteConsumer
meth public void accept(int) throws java.io.IOException
supr java.lang.Object
hfds os

CLSS public org.apache.commons.compress.utils.CRC32VerifyingInputStream
cons public init(java.io.InputStream,long,int)
cons public init(java.io.InputStream,long,long)
supr org.apache.commons.compress.utils.ChecksumVerifyingInputStream

CLSS public org.apache.commons.compress.utils.CharsetNames
cons public init()
fld public final static java.lang.String ISO_8859_1 = "ISO-8859-1"
fld public final static java.lang.String US_ASCII = "US-ASCII"
fld public final static java.lang.String UTF_16 = "UTF-16"
fld public final static java.lang.String UTF_16BE = "UTF-16BE"
fld public final static java.lang.String UTF_16LE = "UTF-16LE"
fld public final static java.lang.String UTF_8 = "UTF-8"
supr java.lang.Object

CLSS public org.apache.commons.compress.utils.Charsets
cons public init()
fld public final static java.nio.charset.Charset ISO_8859_1
 anno 0 java.lang.Deprecated()
fld public final static java.nio.charset.Charset US_ASCII
 anno 0 java.lang.Deprecated()
fld public final static java.nio.charset.Charset UTF_16
 anno 0 java.lang.Deprecated()
fld public final static java.nio.charset.Charset UTF_16BE
 anno 0 java.lang.Deprecated()
fld public final static java.nio.charset.Charset UTF_16LE
 anno 0 java.lang.Deprecated()
fld public final static java.nio.charset.Charset UTF_8
 anno 0 java.lang.Deprecated()
meth public static java.nio.charset.Charset toCharset(java.lang.String)
meth public static java.nio.charset.Charset toCharset(java.nio.charset.Charset)
supr java.lang.Object

CLSS public org.apache.commons.compress.utils.ChecksumCalculatingInputStream
cons public init(java.util.zip.Checksum,java.io.InputStream)
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long getValue()
meth public long skip(long) throws java.io.IOException
supr java.io.FilterInputStream
hfds checksum

CLSS public org.apache.commons.compress.utils.ChecksumVerifyingInputStream
cons public init(java.util.zip.Checksum,java.io.InputStream,long,long)
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long getBytesRemaining()
meth public long skip(long) throws java.io.IOException
supr java.io.FilterInputStream
hfds bytesRemaining,checksum,expectedChecksum

CLSS public org.apache.commons.compress.utils.CloseShieldFilterInputStream
cons public init(java.io.InputStream)
meth public void close() throws java.io.IOException
supr java.io.FilterInputStream

CLSS public org.apache.commons.compress.utils.CountingInputStream
cons public init(java.io.InputStream)
meth protected final void count(long)
meth public int read() throws java.io.IOException
meth public int read(byte[]) throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long getBytesRead()
supr java.io.FilterInputStream
hfds bytesRead

CLSS public org.apache.commons.compress.utils.CountingOutputStream
cons public init(java.io.OutputStream)
meth protected void count(long)
meth public long getBytesWritten()
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.FilterOutputStream
hfds bytesWritten

CLSS public org.apache.commons.compress.utils.ExactMath
meth public static int add(int,long)
supr java.lang.Object

CLSS public org.apache.commons.compress.utils.FileNameUtils
cons public init()
meth public static java.lang.String getBaseName(java.lang.String)
meth public static java.lang.String getBaseName(java.nio.file.Path)
meth public static java.lang.String getExtension(java.lang.String)
meth public static java.lang.String getExtension(java.nio.file.Path)
supr java.lang.Object

CLSS public org.apache.commons.compress.utils.FixedLengthBlockOutputStream
cons public init(java.io.OutputStream,int)
cons public init(java.nio.channels.WritableByteChannel,int)
intf java.nio.channels.WritableByteChannel
meth public boolean isOpen()
meth public int write(java.nio.ByteBuffer) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void flushBlock() throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream
hfds blockSize,buffer,closed,out
hcls BufferAtATimeOutputChannel

CLSS public org.apache.commons.compress.utils.FlushShieldFilterOutputStream
cons public init(java.io.OutputStream)
meth public void flush() throws java.io.IOException
supr java.io.FilterOutputStream

CLSS public final org.apache.commons.compress.utils.IOUtils
fld public final static java.nio.file.LinkOption[] EMPTY_LINK_OPTIONS
meth public static byte[] readRange(java.io.InputStream,int) throws java.io.IOException
meth public static byte[] readRange(java.nio.channels.ReadableByteChannel,int) throws java.io.IOException
meth public static byte[] toByteArray(java.io.InputStream) throws java.io.IOException
meth public static int read(java.io.File,byte[]) throws java.io.IOException
meth public static int readFully(java.io.InputStream,byte[]) throws java.io.IOException
meth public static int readFully(java.io.InputStream,byte[],int,int) throws java.io.IOException
meth public static long copy(java.io.InputStream,java.io.OutputStream) throws java.io.IOException
meth public static long copy(java.io.InputStream,java.io.OutputStream,int) throws java.io.IOException
meth public static long copyRange(java.io.InputStream,long,java.io.OutputStream) throws java.io.IOException
meth public static long copyRange(java.io.InputStream,long,java.io.OutputStream,int) throws java.io.IOException
meth public static long skip(java.io.InputStream,long) throws java.io.IOException
meth public static void closeQuietly(java.io.Closeable)
meth public static void copy(java.io.File,java.io.OutputStream) throws java.io.IOException
meth public static void readFully(java.nio.channels.ReadableByteChannel,java.nio.ByteBuffer) throws java.io.IOException
supr java.lang.Object
hfds COPY_BUF_SIZE,SKIP_BUF,SKIP_BUF_SIZE

CLSS public abstract interface org.apache.commons.compress.utils.InputStreamStatistics
meth public abstract long getCompressedCount()
meth public abstract long getUncompressedCount()

CLSS public org.apache.commons.compress.utils.Iterators
meth public static <%0 extends java.lang.Object> boolean addAll(java.util.Collection<{%%0}>,java.util.Iterator<? extends {%%0}>)
supr java.lang.Object

CLSS public org.apache.commons.compress.utils.Lists
meth public static <%0 extends java.lang.Object> java.util.ArrayList<{%%0}> newArrayList()
meth public static <%0 extends java.lang.Object> java.util.ArrayList<{%%0}> newArrayList(java.util.Iterator<? extends {%%0}>)
supr java.lang.Object

CLSS public org.apache.commons.compress.utils.MultiReadOnlySeekableByteChannel
cons public init(java.util.List<java.nio.channels.SeekableByteChannel>)
intf java.nio.channels.SeekableByteChannel
meth public !varargs static java.nio.channels.SeekableByteChannel forFiles(java.io.File[]) throws java.io.IOException
meth public !varargs static java.nio.channels.SeekableByteChannel forPaths(java.nio.file.Path[]) throws java.io.IOException
meth public !varargs static java.nio.channels.SeekableByteChannel forSeekableByteChannels(java.nio.channels.SeekableByteChannel[])
meth public boolean isOpen()
meth public int read(java.nio.ByteBuffer) throws java.io.IOException
meth public int write(java.nio.ByteBuffer)
meth public java.nio.channels.SeekableByteChannel position(long) throws java.io.IOException
meth public java.nio.channels.SeekableByteChannel position(long,long) throws java.io.IOException
meth public java.nio.channels.SeekableByteChannel truncate(long)
meth public long position()
meth public long size() throws java.io.IOException
meth public void close() throws java.io.IOException
supr java.lang.Object
hfds EMPTY_PATH_ARRAY,channels,currentChannelIdx,globalPosition

CLSS public org.apache.commons.compress.utils.OsgiUtils
cons public init()
meth public static boolean isRunningInOsgiEnvironment()
supr java.lang.Object
hfds inOsgiEnvironment

CLSS public org.apache.commons.compress.utils.SeekableInMemoryByteChannel
cons public init()
cons public init(byte[])
cons public init(int)
intf java.nio.channels.SeekableByteChannel
meth public boolean isOpen()
meth public byte[] array()
meth public int read(java.nio.ByteBuffer) throws java.io.IOException
meth public int write(java.nio.ByteBuffer) throws java.io.IOException
meth public java.nio.channels.SeekableByteChannel position(long) throws java.io.IOException
meth public java.nio.channels.SeekableByteChannel truncate(long)
meth public long position()
meth public long size()
meth public void close()
supr java.lang.Object
hfds NAIVE_RESIZE_LIMIT,closed,data,position,size

CLSS public org.apache.commons.compress.utils.ServiceLoaderIterator<%0 extends java.lang.Object>
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Class<{org.apache.commons.compress.utils.ServiceLoaderIterator%0}>)
cons public init(java.lang.Class<{org.apache.commons.compress.utils.ServiceLoaderIterator%0}>,java.lang.ClassLoader)
intf java.util.Iterator<{org.apache.commons.compress.utils.ServiceLoaderIterator%0}>
meth public boolean hasNext()
meth public void remove()
meth public {org.apache.commons.compress.utils.ServiceLoaderIterator%0} next()
supr java.lang.Object
hfds nextServiceLoader,service,serviceLoaderIterator

CLSS public org.apache.commons.compress.utils.Sets
meth public !varargs static <%0 extends java.lang.Object> java.util.HashSet<{%%0}> newHashSet({%%0}[])
 anno 0 java.lang.SafeVarargs()
supr java.lang.Object

CLSS public org.apache.commons.compress.utils.SkipShieldingInputStream
cons public init(java.io.InputStream)
meth public long skip(long) throws java.io.IOException
supr java.io.FilterInputStream
hfds SKIP_BUFFER,SKIP_BUFFER_SIZE

CLSS public final org.apache.commons.compress.utils.TimeUtils
meth public static boolean isUnixTime(java.nio.file.attribute.FileTime)
meth public static boolean isUnixTime(long)
meth public static java.nio.file.attribute.FileTime ntfsTimeToFileTime(long)
meth public static java.nio.file.attribute.FileTime toFileTime(java.util.Date)
meth public static java.nio.file.attribute.FileTime truncateToHundredNanos(java.nio.file.attribute.FileTime)
meth public static java.nio.file.attribute.FileTime unixTimeToFileTime(long)
meth public static java.util.Date ntfsTimeToDate(long)
meth public static java.util.Date toDate(java.nio.file.attribute.FileTime)
meth public static long toNtfsTime(java.nio.file.attribute.FileTime)
meth public static long toNtfsTime(java.util.Date)
meth public static long toNtfsTime(long)
meth public static long toUnixTime(java.nio.file.attribute.FileTime)
supr java.lang.Object
hfds HUNDRED_NANOS_PER_MILLISECOND,HUNDRED_NANOS_PER_SECOND,WINDOWS_EPOCH_OFFSET

CLSS abstract interface org.apache.commons.compress.utils.package-info

