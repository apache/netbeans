#Signature file v4.1
#Version 0.12.0

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

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
hfds MAX_SKIP_BUFFER_SIZE

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

CLSS public abstract org.apache.commons.compress.archivers.ArchiveInputStream
cons public init()
meth protected void count(int)
meth protected void count(long)
meth protected void pushedBackBytes(long)
meth public abstract org.apache.commons.compress.archivers.ArchiveEntry getNextEntry() throws java.io.IOException
meth public boolean canReadEntryData(org.apache.commons.compress.archivers.ArchiveEntry)
meth public int getCount()
 anno 0 java.lang.Deprecated()
meth public int read() throws java.io.IOException
meth public long getBytesRead()
supr java.io.InputStream
hfds BYTE_MASK,SINGLE,bytesRead

CLSS public abstract org.apache.commons.compress.archivers.ArchiveOutputStream
cons public init()
meth protected void count(int)
meth protected void count(long)
meth public abstract org.apache.commons.compress.archivers.ArchiveEntry createArchiveEntry(java.io.File,java.lang.String) throws java.io.IOException
meth public abstract void closeArchiveEntry() throws java.io.IOException
meth public abstract void finish() throws java.io.IOException
meth public abstract void putArchiveEntry(org.apache.commons.compress.archivers.ArchiveEntry) throws java.io.IOException
meth public boolean canWriteEntryData(org.apache.commons.compress.archivers.ArchiveEntry)
meth public int getCount()
 anno 0 java.lang.Deprecated()
meth public long getBytesWritten()
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream
hfds BYTE_MASK,bytesWritten,oneByte

CLSS public org.apache.commons.compress.archivers.ArchiveStreamFactory
cons public init()
fld public final static java.lang.String AR = "ar"
fld public final static java.lang.String ARJ = "arj"
fld public final static java.lang.String CPIO = "cpio"
fld public final static java.lang.String DUMP = "dump"
fld public final static java.lang.String JAR = "jar"
fld public final static java.lang.String SEVEN_Z = "7z"
fld public final static java.lang.String TAR = "tar"
fld public final static java.lang.String ZIP = "zip"
meth public java.lang.String getEntryEncoding()
meth public org.apache.commons.compress.archivers.ArchiveInputStream createArchiveInputStream(java.io.InputStream) throws org.apache.commons.compress.archivers.ArchiveException
meth public org.apache.commons.compress.archivers.ArchiveInputStream createArchiveInputStream(java.lang.String,java.io.InputStream) throws org.apache.commons.compress.archivers.ArchiveException
meth public org.apache.commons.compress.archivers.ArchiveOutputStream createArchiveOutputStream(java.lang.String,java.io.OutputStream) throws org.apache.commons.compress.archivers.ArchiveException
meth public void setEntryEncoding(java.lang.String)
supr java.lang.Object
hfds entryEncoding

CLSS public final org.apache.commons.compress.archivers.Lister
cons public init()
meth public static void main(java.lang.String[]) throws java.lang.Exception
supr java.lang.Object
hfds factory

CLSS public org.apache.commons.compress.archivers.StreamingNotSupportedException
cons public init(java.lang.String)
meth public java.lang.String getFormat()
supr org.apache.commons.compress.archivers.ArchiveException
hfds format,serialVersionUID

CLSS public org.apache.commons.compress.archivers.tar.TarArchiveEntry
cons public init(byte[])
cons public init(byte[],org.apache.commons.compress.archivers.zip.ZipEncoding) throws java.io.IOException
cons public init(java.io.File)
cons public init(java.io.File,java.lang.String)
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,byte)
cons public init(java.lang.String,byte,boolean)
fld public final static int DEFAULT_DIR_MODE = 16877
fld public final static int DEFAULT_FILE_MODE = 33188
fld public final static int MAX_NAMELEN = 31
fld public final static int MILLIS_PER_SECOND = 1000
intf org.apache.commons.compress.archivers.ArchiveEntry
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
meth public boolean isPaxHeader()
meth public boolean isSymbolicLink()
meth public int getDevMajor()
meth public int getDevMinor()
meth public int getGroupId()
meth public int getMode()
meth public int getUserId()
meth public int hashCode()
meth public java.io.File getFile()
meth public java.lang.String getGroupName()
meth public java.lang.String getLinkName()
meth public java.lang.String getName()
meth public java.lang.String getUserName()
meth public java.util.Date getLastModifiedDate()
meth public java.util.Date getModTime()
meth public long getRealSize()
meth public long getSize()
meth public org.apache.commons.compress.archivers.tar.TarArchiveEntry[] getDirectoryEntries()
meth public void parseTarHeader(byte[])
meth public void parseTarHeader(byte[],org.apache.commons.compress.archivers.zip.ZipEncoding) throws java.io.IOException
meth public void setDevMajor(int)
meth public void setDevMinor(int)
meth public void setGroupId(int)
meth public void setGroupName(java.lang.String)
meth public void setIds(int,int)
meth public void setLinkName(java.lang.String)
meth public void setModTime(java.util.Date)
meth public void setModTime(long)
meth public void setMode(int)
meth public void setName(java.lang.String)
meth public void setNames(java.lang.String,java.lang.String)
meth public void setSize(long)
meth public void setUserId(int)
meth public void setUserName(java.lang.String)
meth public void writeEntryHeader(byte[])
meth public void writeEntryHeader(byte[],org.apache.commons.compress.archivers.zip.ZipEncoding,boolean) throws java.io.IOException
supr java.lang.Object
hfds checkSumOK,devMajor,devMinor,file,groupId,groupName,isExtended,linkFlag,linkName,magic,modTime,mode,name,realSize,size,userId,userName,version

CLSS public org.apache.commons.compress.archivers.tar.TarArchiveInputStream
cons public init(java.io.InputStream)
cons public init(java.io.InputStream,int)
cons public init(java.io.InputStream,int,int)
cons public init(java.io.InputStream,int,int,java.lang.String)
cons public init(java.io.InputStream,int,java.lang.String)
cons public init(java.io.InputStream,java.lang.String)
meth protected boolean isEOFRecord(byte[])
meth protected byte[] getLongNameData() throws java.io.IOException
meth protected byte[] readRecord() throws java.io.IOException
meth protected final boolean isAtEOF()
meth protected final void setAtEOF(boolean)
meth protected final void setCurrentEntry(org.apache.commons.compress.archivers.tar.TarArchiveEntry)
meth public boolean canReadEntryData(org.apache.commons.compress.archivers.ArchiveEntry)
meth public int available() throws java.io.IOException
meth public int getRecordSize()
meth public int read(byte[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public org.apache.commons.compress.archivers.ArchiveEntry getNextEntry() throws java.io.IOException
meth public org.apache.commons.compress.archivers.tar.TarArchiveEntry getCurrentEntry()
meth public org.apache.commons.compress.archivers.tar.TarArchiveEntry getNextTarEntry() throws java.io.IOException
meth public static boolean matches(byte[],int)
meth public void close() throws java.io.IOException
meth public void reset()
supr org.apache.commons.compress.archivers.ArchiveInputStream
hfds SMALL_BUF,SMALL_BUFFER_SIZE,blockSize,currEntry,encoding,entryOffset,entrySize,hasHitEOF,is,recordSize

CLSS public org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
cons public init(java.io.OutputStream)
cons public init(java.io.OutputStream,int)
cons public init(java.io.OutputStream,int,int)
cons public init(java.io.OutputStream,int,int,java.lang.String)
cons public init(java.io.OutputStream,int,java.lang.String)
cons public init(java.io.OutputStream,java.lang.String)
fld public final static int BIGNUMBER_ERROR = 0
fld public final static int BIGNUMBER_POSIX = 2
fld public final static int BIGNUMBER_STAR = 1
fld public final static int LONGFILE_ERROR = 0
fld public final static int LONGFILE_GNU = 2
fld public final static int LONGFILE_POSIX = 3
fld public final static int LONGFILE_TRUNCATE = 1
meth public int getCount()
 anno 0 java.lang.Deprecated()
meth public int getRecordSize()
meth public long getBytesWritten()
meth public org.apache.commons.compress.archivers.ArchiveEntry createArchiveEntry(java.io.File,java.lang.String) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void closeArchiveEntry() throws java.io.IOException
meth public void finish() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void putArchiveEntry(org.apache.commons.compress.archivers.ArchiveEntry) throws java.io.IOException
meth public void setAddPaxHeadersForNonAsciiNames(boolean)
meth public void setBigNumberMode(int)
meth public void setLongFileMode(int)
meth public void write(byte[],int,int) throws java.io.IOException
supr org.apache.commons.compress.archivers.ArchiveOutputStream
hfds ASCII,addPaxHeadersForNonAsciiNames,assemBuf,assemLen,bigNumberMode,closed,currBytes,currName,currSize,encoding,finished,haveUnclosedEntry,longFileMode,out,recordBuf,recordSize,recordsPerBlock,recordsWritten

CLSS public org.apache.commons.compress.archivers.tar.TarArchiveSparseEntry
cons public init(byte[]) throws java.io.IOException
intf org.apache.commons.compress.archivers.tar.TarConstants
meth public boolean isExtended()
supr java.lang.Object
hfds isExtended

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
fld public final static byte LF_NORMAL = 48
fld public final static byte LF_OLDNORM = 0
fld public final static byte LF_PAX_EXTENDED_HEADER_LC = 120
fld public final static byte LF_PAX_EXTENDED_HEADER_UC = 88
fld public final static byte LF_PAX_GLOBAL_EXTENDED_HEADER = 103
fld public final static byte LF_SYMLINK = 50
fld public final static int ATIMELEN_GNU = 12
fld public final static int CHKSUMLEN = 8
fld public final static int CHKSUM_OFFSET = 148
fld public final static int CTIMELEN_GNU = 12
fld public final static int DEFAULT_BLKSIZE = 10240
fld public final static int DEFAULT_RCDSIZE = 512
fld public final static int DEVLEN = 8
fld public final static int FORMAT_OLDGNU = 2
fld public final static int FORMAT_POSIX = 3
fld public final static int GIDLEN = 8
fld public final static int GNAMELEN = 32
fld public final static int ISEXTENDEDLEN_GNU = 1
fld public final static int ISEXTENDEDLEN_GNU_SPARSE = 1
fld public final static int LONGNAMESLEN_GNU = 4
fld public final static int MAGICLEN = 6
fld public final static int MAGIC_OFFSET = 257
fld public final static int MODELEN = 8
fld public final static int MODTIMELEN = 12
fld public final static int NAMELEN = 100
fld public final static int OFFSETLEN_GNU = 12
fld public final static int PAD2LEN_GNU = 1
fld public final static int PREFIXLEN = 155
fld public final static int REALSIZELEN_GNU = 12
fld public final static int SIZELEN = 12
fld public final static int SPARSELEN_GNU = 96
fld public final static int SPARSELEN_GNU_SPARSE = 504
fld public final static int UIDLEN = 8
fld public final static int UNAMELEN = 32
fld public final static int VERSIONLEN = 2
fld public final static int VERSION_OFFSET = 263
fld public final static java.lang.String GNU_LONGLINK = "././@LongLink"
fld public final static java.lang.String MAGIC_ANT = "ustar\u0000"
fld public final static java.lang.String MAGIC_GNU = "ustar "
fld public final static java.lang.String MAGIC_POSIX = "ustar\u0000"
fld public final static java.lang.String VERSION_ANT = "\u0000\u0000"
fld public final static java.lang.String VERSION_GNU_SPACE = " \u0000"
fld public final static java.lang.String VERSION_GNU_ZERO = "0\u0000"
fld public final static java.lang.String VERSION_POSIX = "00"
fld public final static long MAXID = 2097151
fld public final static long MAXSIZE = 8589934591

CLSS public org.apache.commons.compress.archivers.tar.TarUtils
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
meth public static void formatUnsignedOctalString(long,byte[],int,int)
supr java.lang.Object
hfds BYTE_MASK,DEFAULT_ENCODING,FALLBACK_ENCODING

