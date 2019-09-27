#Signature file v4.1
#Version 3.31

CLSS public final com.jcraft.jzlib.Deflate
supr java.lang.Object
hfds BL_CODES,BUSY_STATE,BlockDone,Buf_size,DEF_MEM_LEVEL,DYN_TREES,D_CODES,END_BLOCK,FAST,FINISH_STATE,FinishDone,FinishStarted,HEAP_SIZE,INIT_STATE,LENGTH_CODES,LITERALS,L_CODES,MAX_BITS,MAX_MATCH,MAX_MEM_LEVEL,MAX_WBITS,MIN_LOOKAHEAD,MIN_MATCH,NeedMore,PRESET_DICT,REPZ_11_138,REPZ_3_10,REP_3_6,SLOW,STATIC_TREES,STORED,STORED_BLOCK,Z_ASCII,Z_BINARY,Z_BUF_ERROR,Z_DATA_ERROR,Z_DEFAULT_COMPRESSION,Z_DEFAULT_STRATEGY,Z_DEFLATED,Z_ERRNO,Z_FILTERED,Z_FINISH,Z_FULL_FLUSH,Z_HUFFMAN_ONLY,Z_MEM_ERROR,Z_NEED_DICT,Z_NO_FLUSH,Z_OK,Z_PARTIAL_FLUSH,Z_STREAM_END,Z_STREAM_ERROR,Z_SYNC_FLUSH,Z_UNKNOWN,Z_VERSION_ERROR,bi_buf,bi_valid,bl_count,bl_desc,bl_tree,block_start,config_table,d_buf,d_desc,data_type,depth,dyn_dtree,dyn_ltree,good_match,hash_bits,hash_mask,hash_shift,hash_size,head,heap,heap_len,heap_max,ins_h,l_buf,l_desc,last_eob_len,last_flush,last_lit,level,lit_bufsize,lookahead,match_available,match_length,match_start,matches,max_chain_length,max_lazy_match,method,nice_match,noheader,opt_len,pending,pending_buf,pending_buf_size,pending_out,prev,prev_length,prev_match,static_len,status,strategy,strm,strstart,w_bits,w_mask,w_size,window,window_size,z_errmsg
hcls Config

CLSS public final com.jcraft.jzlib.JZlib
cons public init()
fld public final static int Z_BEST_COMPRESSION = 9
fld public final static int Z_BEST_SPEED = 1
fld public final static int Z_BUF_ERROR = -5
fld public final static int Z_DATA_ERROR = -3
fld public final static int Z_DEFAULT_COMPRESSION = -1
fld public final static int Z_DEFAULT_STRATEGY = 0
fld public final static int Z_ERRNO = -1
fld public final static int Z_FILTERED = 1
fld public final static int Z_FINISH = 4
fld public final static int Z_FULL_FLUSH = 3
fld public final static int Z_HUFFMAN_ONLY = 2
fld public final static int Z_MEM_ERROR = -4
fld public final static int Z_NEED_DICT = 2
fld public final static int Z_NO_COMPRESSION = 0
fld public final static int Z_NO_FLUSH = 0
fld public final static int Z_OK = 0
fld public final static int Z_PARTIAL_FLUSH = 1
fld public final static int Z_STREAM_END = 1
fld public final static int Z_STREAM_ERROR = -2
fld public final static int Z_SYNC_FLUSH = 2
fld public final static int Z_VERSION_ERROR = -6
meth public static java.lang.String version()
supr java.lang.Object
hfds version

CLSS public com.jcraft.jzlib.ZInputStream
cons public init(java.io.InputStream)
cons public init(java.io.InputStream,boolean)
cons public init(java.io.InputStream,int)
fld protected boolean compress
fld protected byte[] buf
fld protected byte[] buf1
fld protected com.jcraft.jzlib.ZStream z
fld protected int bufsize
fld protected int flush
fld protected java.io.InputStream in
meth public int getFlushMode()
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public long getTotalIn()
meth public long getTotalOut()
meth public long skip(long) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void setFlushMode(int)
supr java.io.FilterInputStream
hfds nomoreinput

CLSS public com.jcraft.jzlib.ZOutputStream
cons public init(java.io.OutputStream)
cons public init(java.io.OutputStream,int)
cons public init(java.io.OutputStream,int,boolean)
fld protected boolean compress
fld protected byte[] buf
fld protected byte[] buf1
fld protected com.jcraft.jzlib.ZStream z
fld protected int bufsize
fld protected int flush
fld protected java.io.OutputStream out
meth public int getFlushMode()
meth public long getTotalIn()
meth public long getTotalOut()
meth public void close() throws java.io.IOException
meth public void end()
meth public void finish() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void setFlushMode(int)
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream

CLSS public final com.jcraft.jzlib.ZStream
cons public init()
fld public byte[] next_in
fld public byte[] next_out
fld public int avail_in
fld public int avail_out
fld public int next_in_index
fld public int next_out_index
fld public java.lang.String msg
fld public long adler
fld public long total_in
fld public long total_out
meth public int deflate(int)
meth public int deflateEnd()
meth public int deflateInit(int)
meth public int deflateInit(int,boolean)
meth public int deflateInit(int,int)
meth public int deflateInit(int,int,boolean)
meth public int deflateParams(int,int)
meth public int deflateSetDictionary(byte[],int)
meth public int inflate(int)
meth public int inflateEnd()
meth public int inflateInit()
meth public int inflateInit(boolean)
meth public int inflateInit(int)
meth public int inflateInit(int,boolean)
meth public int inflateSetDictionary(byte[],int)
meth public int inflateSync()
meth public void free()
supr java.lang.Object
hfds DEF_WBITS,MAX_MEM_LEVEL,MAX_WBITS,Z_BUF_ERROR,Z_DATA_ERROR,Z_ERRNO,Z_FINISH,Z_FULL_FLUSH,Z_MEM_ERROR,Z_NEED_DICT,Z_NO_FLUSH,Z_OK,Z_PARTIAL_FLUSH,Z_STREAM_END,Z_STREAM_ERROR,Z_SYNC_FLUSH,Z_VERSION_ERROR,_adler,data_type,dstate,istate

CLSS public com.jcraft.jzlib.ZStreamException
cons public init()
cons public init(java.lang.String)
supr java.io.IOException

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
hfds serialVersionUID

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
hfds maxSkipBufferSize,skipBuffer

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
hfds WRITE_BUFFER_SIZE,writeBuffer

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
hfds name,ordinal

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

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

CLSS public abstract interface java.lang.Readable
meth public abstract int read(java.nio.CharBuffer) throws java.io.IOException

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

CLSS public abstract java.util.AbstractCollection<%0 extends java.lang.Object>
cons protected init()
intf java.util.Collection<{java.util.AbstractCollection%0}>
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract int size()
meth public abstract java.util.Iterator<{java.util.AbstractCollection%0}> iterator()
meth public boolean add({java.util.AbstractCollection%0})
meth public boolean addAll(java.util.Collection<? extends {java.util.AbstractCollection%0}>)
meth public boolean contains(java.lang.Object)
meth public boolean containsAll(java.util.Collection<?>)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean retainAll(java.util.Collection<?>)
meth public java.lang.Object[] toArray()
meth public java.lang.String toString()
meth public void clear()
supr java.lang.Object
hfds MAX_ARRAY_SIZE

CLSS public abstract java.util.AbstractMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init()
innr public static SimpleEntry
innr public static SimpleImmutableEntry
intf java.util.Map<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>> entrySet()
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.String toString()
meth public java.util.Collection<{java.util.AbstractMap%1}> values()
meth public java.util.Set<{java.util.AbstractMap%0}> keySet()
meth public void clear()
meth public void putAll(java.util.Map<? extends {java.util.AbstractMap%0},? extends {java.util.AbstractMap%1}>)
meth public {java.util.AbstractMap%1} get(java.lang.Object)
meth public {java.util.AbstractMap%1} put({java.util.AbstractMap%0},{java.util.AbstractMap%1})
meth public {java.util.AbstractMap%1} remove(java.lang.Object)
supr java.lang.Object
hfds keySet,values

CLSS public abstract java.util.AbstractSet<%0 extends java.lang.Object>
cons protected init()
intf java.util.Set<{java.util.AbstractSet%0}>
meth public boolean equals(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public int hashCode()
supr java.util.AbstractCollection<{java.util.AbstractSet%0}>

CLSS public abstract interface java.util.Collection<%0 extends java.lang.Object>
intf java.lang.Iterable<{java.util.Collection%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Collection%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Collection%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Collection%0}> iterator()
meth public abstract void clear()
meth public boolean removeIf(java.util.function.Predicate<? super {java.util.Collection%0}>)
meth public java.util.Spliterator<{java.util.Collection%0}> spliterator()
meth public java.util.stream.Stream<{java.util.Collection%0}> parallelStream()
meth public java.util.stream.Stream<{java.util.Collection%0}> stream()

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

CLSS public abstract interface static java.util.Map$Entry<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer java.util.Map
meth public abstract boolean equals(java.lang.Object)
meth public abstract int hashCode()
meth public abstract {java.util.Map$Entry%0} getKey()
meth public abstract {java.util.Map$Entry%1} getValue()
meth public abstract {java.util.Map$Entry%1} setValue({java.util.Map$Entry%1})
meth public static <%0 extends java.lang.Comparable<? super {%%0}>, %1 extends java.lang.Object> java.util.Comparator<java.util.Map$Entry<{%%0},{%%1}>> comparingByKey()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Comparable<? super {%%1}>> java.util.Comparator<java.util.Map$Entry<{%%0},{%%1}>> comparingByValue()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Comparator<java.util.Map$Entry<{%%0},{%%1}>> comparingByKey(java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Comparator<java.util.Map$Entry<{%%0},{%%1}>> comparingByValue(java.util.Comparator<? super {%%1}>)

CLSS public abstract interface java.util.Set<%0 extends java.lang.Object>
intf java.util.Collection<{java.util.Set%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.Set%0})
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.Set%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.Set%0}> iterator()
meth public abstract void clear()
meth public java.util.Spliterator<{java.util.Set%0}> spliterator()

CLSS public abstract interface javax.security.auth.callback.CallbackHandler
meth public abstract void handle(javax.security.auth.callback.Callback[]) throws java.io.IOException,javax.security.auth.callback.UnsupportedCallbackException

CLSS public org.jivesoftware.smack.AccountManager
cons public init(org.jivesoftware.smack.XMPPConnection)
meth public boolean supportsAccountCreation()
meth public java.lang.String getAccountAttribute(java.lang.String)
meth public java.lang.String getAccountInstructions()
meth public java.util.Collection<java.lang.String> getAccountAttributes()
meth public void changePassword(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void createAccount(java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void createAccount(java.lang.String,java.lang.String,java.util.Map<java.lang.String,java.lang.String>) throws org.jivesoftware.smack.XMPPException
meth public void deleteAccount() throws org.jivesoftware.smack.XMPPException
supr java.lang.Object
hfds accountCreationSupported,connection,info

CLSS public org.jivesoftware.smack.Chat
meth public boolean equals(java.lang.Object)
meth public java.lang.String getParticipant()
meth public java.lang.String getThreadID()
meth public java.util.Collection<org.jivesoftware.smack.MessageListener> getListeners()
meth public org.jivesoftware.smack.PacketCollector createCollector()
meth public void addMessageListener(org.jivesoftware.smack.MessageListener)
meth public void removeMessageListener(org.jivesoftware.smack.MessageListener)
meth public void sendMessage(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void sendMessage(org.jivesoftware.smack.packet.Message) throws org.jivesoftware.smack.XMPPException
supr java.lang.Object
hfds chatManager,listeners,participant,threadID

CLSS public org.jivesoftware.smack.ChatManager
meth public java.util.Collection<org.jivesoftware.smack.ChatManagerListener> getChatListeners()
meth public org.jivesoftware.smack.Chat createChat(java.lang.String,java.lang.String,org.jivesoftware.smack.MessageListener)
meth public org.jivesoftware.smack.Chat createChat(java.lang.String,org.jivesoftware.smack.MessageListener)
meth public org.jivesoftware.smack.Chat getThreadChat(java.lang.String)
meth public void addChatListener(org.jivesoftware.smack.ChatManagerListener)
meth public void addOutgoingMessageInterceptor(org.jivesoftware.smack.PacketInterceptor)
meth public void addOutgoingMessageInterceptor(org.jivesoftware.smack.PacketInterceptor,org.jivesoftware.smack.filter.PacketFilter)
meth public void removeChatListener(org.jivesoftware.smack.ChatManagerListener)
supr java.lang.Object
hfds chatManagerListeners,connection,id,interceptors,jidChats,prefix,threadChats

CLSS public abstract interface org.jivesoftware.smack.ChatManagerListener
meth public abstract void chatCreated(org.jivesoftware.smack.Chat,boolean)

CLSS public org.jivesoftware.smack.ConnectionConfiguration
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,java.lang.String)
cons public init(java.lang.String,int,java.lang.String,org.jivesoftware.smack.proxy.ProxyInfo)
cons public init(java.lang.String,int,org.jivesoftware.smack.proxy.ProxyInfo)
cons public init(java.lang.String,org.jivesoftware.smack.proxy.ProxyInfo)
innr public final static !enum SecurityMode
intf java.lang.Cloneable
meth public boolean isCompressionEnabled()
meth public boolean isDebuggerEnabled()
meth public boolean isExpiredCertificatesCheckEnabled()
meth public boolean isNotMatchingDomainCheckEnabled()
meth public boolean isReconnectionAllowed()
meth public boolean isRosterLoadedAtLogin()
meth public boolean isSASLAuthenticationEnabled()
meth public boolean isSelfSignedCertificateEnabled()
meth public boolean isVerifyChainEnabled()
meth public boolean isVerifyRootCAEnabled()
meth public int getPort()
meth public java.lang.String getHost()
meth public java.lang.String getKeystorePath()
meth public java.lang.String getKeystoreType()
meth public java.lang.String getPKCS11Library()
meth public java.lang.String getServiceName()
meth public java.lang.String getTruststorePassword()
meth public java.lang.String getTruststorePath()
meth public java.lang.String getTruststoreType()
meth public javax.net.SocketFactory getSocketFactory()
meth public javax.security.auth.callback.CallbackHandler getCallbackHandler()
meth public org.jivesoftware.smack.ConnectionConfiguration$SecurityMode getSecurityMode()
meth public void setCallbackHandler(javax.security.auth.callback.CallbackHandler)
meth public void setCompressionEnabled(boolean)
meth public void setDebuggerEnabled(boolean)
meth public void setExpiredCertificatesCheckEnabled(boolean)
meth public void setKeystorePath(java.lang.String)
meth public void setKeystoreType(java.lang.String)
meth public void setNotMatchingDomainCheckEnabled(boolean)
meth public void setPKCS11Library(java.lang.String)
meth public void setReconnectionAllowed(boolean)
meth public void setRosterLoadedAtLogin(boolean)
meth public void setSASLAuthenticationEnabled(boolean)
meth public void setSecurityMode(org.jivesoftware.smack.ConnectionConfiguration$SecurityMode)
meth public void setSelfSignedCertificateEnabled(boolean)
meth public void setSendPresence(boolean)
meth public void setSocketFactory(javax.net.SocketFactory)
meth public void setTruststorePassword(java.lang.String)
meth public void setTruststorePath(java.lang.String)
meth public void setTruststoreType(java.lang.String)
meth public void setVerifyChainEnabled(boolean)
meth public void setVerifyRootCAEnabled(boolean)
supr java.lang.Object
hfds callbackHandler,compressionEnabled,debuggerEnabled,expiredCertificatesCheckEnabled,host,keystorePath,keystoreType,notMatchingDomainCheckEnabled,password,pkcs11Library,port,proxy,reconnectionAllowed,resource,rosterLoadedAtLogin,saslAuthenticationEnabled,securityMode,selfSignedCertificateEnabled,sendPresence,serviceName,socketFactory,truststorePassword,truststorePath,truststoreType,username,verifyChainEnabled,verifyRootCAEnabled

CLSS public final static !enum org.jivesoftware.smack.ConnectionConfiguration$SecurityMode
 outer org.jivesoftware.smack.ConnectionConfiguration
fld public final static org.jivesoftware.smack.ConnectionConfiguration$SecurityMode disabled
fld public final static org.jivesoftware.smack.ConnectionConfiguration$SecurityMode enabled
fld public final static org.jivesoftware.smack.ConnectionConfiguration$SecurityMode required
meth public final static org.jivesoftware.smack.ConnectionConfiguration$SecurityMode[] values()
meth public static org.jivesoftware.smack.ConnectionConfiguration$SecurityMode valueOf(java.lang.String)
supr java.lang.Enum<org.jivesoftware.smack.ConnectionConfiguration$SecurityMode>

CLSS public abstract interface org.jivesoftware.smack.ConnectionCreationListener
meth public abstract void connectionCreated(org.jivesoftware.smack.XMPPConnection)

CLSS public abstract interface org.jivesoftware.smack.ConnectionListener
meth public abstract void connectionClosed()
meth public abstract void connectionClosedOnError(java.lang.Exception)
meth public abstract void reconnectingIn(int)
meth public abstract void reconnectionFailed(java.lang.Exception)
meth public abstract void reconnectionSuccessful()

CLSS public abstract interface org.jivesoftware.smack.MessageListener
meth public abstract void processMessage(org.jivesoftware.smack.Chat,org.jivesoftware.smack.packet.Message)

CLSS public org.jivesoftware.smack.PacketCollector
cons protected init(org.jivesoftware.smack.PacketReader,org.jivesoftware.smack.filter.PacketFilter)
meth protected void processPacket(org.jivesoftware.smack.packet.Packet)
meth public org.jivesoftware.smack.filter.PacketFilter getPacketFilter()
meth public org.jivesoftware.smack.packet.Packet nextResult()
meth public org.jivesoftware.smack.packet.Packet nextResult(long)
meth public org.jivesoftware.smack.packet.Packet pollResult()
meth public void cancel()
supr java.lang.Object
hfds MAX_PACKETS,cancelled,packetFilter,packetReader,resultQueue

CLSS public abstract interface org.jivesoftware.smack.PacketInterceptor
meth public abstract void interceptPacket(org.jivesoftware.smack.packet.Packet)

CLSS public abstract interface org.jivesoftware.smack.PacketListener
meth public abstract void processPacket(org.jivesoftware.smack.packet.Packet)

CLSS public org.jivesoftware.smack.PrivacyList
cons protected init(boolean,boolean,java.lang.String,java.util.List<org.jivesoftware.smack.packet.PrivacyItem>)
meth public boolean isActiveList()
meth public boolean isDefaultList()
meth public java.lang.String toString()
meth public java.util.List<org.jivesoftware.smack.packet.PrivacyItem> getItems()
supr java.lang.Object
hfds isActiveList,isDefaultList,items,listName

CLSS public abstract interface org.jivesoftware.smack.PrivacyListListener
meth public abstract void setPrivacyList(java.lang.String,java.util.List<org.jivesoftware.smack.packet.PrivacyItem>)
meth public abstract void updatedPrivacyList(java.lang.String)

CLSS public org.jivesoftware.smack.PrivacyListManager
meth public org.jivesoftware.smack.PrivacyList getActiveList() throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smack.PrivacyList getDefaultList() throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smack.PrivacyList getPrivacyList(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smack.PrivacyList[] getPrivacyLists() throws org.jivesoftware.smack.XMPPException
meth public static org.jivesoftware.smack.PrivacyListManager getInstanceFor(org.jivesoftware.smack.XMPPConnection)
meth public void addListener(org.jivesoftware.smack.PrivacyListListener)
meth public void createPrivacyList(java.lang.String,java.util.List<org.jivesoftware.smack.packet.PrivacyItem>) throws org.jivesoftware.smack.XMPPException
meth public void declineActiveList() throws org.jivesoftware.smack.XMPPException
meth public void declineDefaultList() throws org.jivesoftware.smack.XMPPException
meth public void deletePrivacyList(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void setActiveListName(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void setDefaultListName(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void updatePrivacyList(java.lang.String,java.util.List<org.jivesoftware.smack.packet.PrivacyItem>) throws org.jivesoftware.smack.XMPPException
supr java.lang.Object
hfds connection,instances,listeners,packetFilter

CLSS public org.jivesoftware.smack.ReconnectionManager
intf org.jivesoftware.smack.ConnectionListener
meth protected void notifyAttemptToReconnectIn(int)
meth protected void notifyReconnectionFailed(java.lang.Exception)
meth protected void reconnect()
meth public void connectionClosed()
meth public void connectionClosedOnError(java.lang.Exception)
meth public void reconnectingIn(int)
meth public void reconnectionFailed(java.lang.Exception)
meth public void reconnectionSuccessful()
supr java.lang.Object
hfds connection,done

CLSS public org.jivesoftware.smack.Roster
innr public final static !enum SubscriptionMode
meth public boolean contains(java.lang.String)
meth public int getEntryCount()
meth public int getGroupCount()
meth public int getUnfiledEntryCount()
meth public java.util.Collection<org.jivesoftware.smack.RosterEntry> getEntries()
meth public java.util.Collection<org.jivesoftware.smack.RosterEntry> getUnfiledEntries()
meth public java.util.Collection<org.jivesoftware.smack.RosterGroup> getGroups()
meth public java.util.Iterator<org.jivesoftware.smack.packet.Presence> getPresences(java.lang.String)
meth public org.jivesoftware.smack.Roster$SubscriptionMode getSubscriptionMode()
meth public org.jivesoftware.smack.RosterEntry getEntry(java.lang.String)
meth public org.jivesoftware.smack.RosterGroup createGroup(java.lang.String)
meth public org.jivesoftware.smack.RosterGroup getGroup(java.lang.String)
meth public org.jivesoftware.smack.packet.Presence getPresence(java.lang.String)
meth public org.jivesoftware.smack.packet.Presence getPresenceResource(java.lang.String)
meth public static org.jivesoftware.smack.Roster$SubscriptionMode getDefaultSubscriptionMode()
meth public static void setDefaultSubscriptionMode(org.jivesoftware.smack.Roster$SubscriptionMode)
meth public void addRosterListener(org.jivesoftware.smack.RosterListener)
meth public void createEntry(java.lang.String,java.lang.String,java.lang.String[]) throws org.jivesoftware.smack.XMPPException
meth public void reload()
meth public void removeEntry(org.jivesoftware.smack.RosterEntry) throws org.jivesoftware.smack.XMPPException
meth public void removeRosterListener(org.jivesoftware.smack.RosterListener)
meth public void setSubscriptionMode(org.jivesoftware.smack.Roster$SubscriptionMode)
supr java.lang.Object
hfds connection,defaultSubscriptionMode,entries,groups,presenceMap,presencePacketListener,rosterInitialized,rosterListeners,subscriptionMode,unfiledEntries
hcls PresencePacketListener,RosterPacketListener

CLSS public final static !enum org.jivesoftware.smack.Roster$SubscriptionMode
 outer org.jivesoftware.smack.Roster
fld public final static org.jivesoftware.smack.Roster$SubscriptionMode accept_all
fld public final static org.jivesoftware.smack.Roster$SubscriptionMode manual
fld public final static org.jivesoftware.smack.Roster$SubscriptionMode reject_all
meth public final static org.jivesoftware.smack.Roster$SubscriptionMode[] values()
meth public static org.jivesoftware.smack.Roster$SubscriptionMode valueOf(java.lang.String)
supr java.lang.Enum<org.jivesoftware.smack.Roster$SubscriptionMode>

CLSS public org.jivesoftware.smack.RosterEntry
meth public boolean equals(java.lang.Object)
meth public java.lang.String getName()
meth public java.lang.String getUser()
meth public java.lang.String toString()
meth public java.util.Collection<org.jivesoftware.smack.RosterGroup> getGroups()
meth public org.jivesoftware.smack.packet.RosterPacket$ItemStatus getStatus()
meth public org.jivesoftware.smack.packet.RosterPacket$ItemType getType()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds connection,name,status,type,user

CLSS public org.jivesoftware.smack.RosterGroup
meth public boolean contains(java.lang.String)
meth public boolean contains(org.jivesoftware.smack.RosterEntry)
meth public int getEntryCount()
meth public java.lang.String getName()
meth public java.util.Collection<org.jivesoftware.smack.RosterEntry> getEntries()
meth public org.jivesoftware.smack.RosterEntry getEntry(java.lang.String)
meth public void addEntry(org.jivesoftware.smack.RosterEntry) throws org.jivesoftware.smack.XMPPException
meth public void removeEntry(org.jivesoftware.smack.RosterEntry) throws org.jivesoftware.smack.XMPPException
meth public void setName(java.lang.String)
supr java.lang.Object
hfds connection,entries,name

CLSS public abstract interface org.jivesoftware.smack.RosterListener
meth public abstract void entriesAdded(java.util.Collection<java.lang.String>)
meth public abstract void entriesDeleted(java.util.Collection<java.lang.String>)
meth public abstract void entriesUpdated(java.util.Collection<java.lang.String>)
meth public abstract void presenceChanged(org.jivesoftware.smack.packet.Presence)

CLSS public org.jivesoftware.smack.SASLAuthentication
meth protected void init()
meth public boolean hasAnonymousAuthentication()
meth public boolean hasNonAnonymousAuthentication()
meth public boolean isAuthenticated()
meth public java.lang.String authenticate(java.lang.String,java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public java.lang.String authenticate(java.lang.String,java.lang.String,javax.security.auth.callback.CallbackHandler) throws org.jivesoftware.smack.XMPPException
meth public java.lang.String authenticateAnonymously() throws org.jivesoftware.smack.XMPPException
meth public static java.util.List<java.lang.Class> getRegisterSASLMechanisms()
meth public static void registerSASLMechanism(java.lang.String,java.lang.Class)
meth public static void supportSASLMechanism(java.lang.String)
meth public static void supportSASLMechanism(java.lang.String,int)
meth public static void unregisterSASLMechanism(java.lang.String)
meth public static void unsupportSASLMechanism(java.lang.String)
meth public void send(java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds connection,currentMechanism,implementedMechanisms,mechanismsPreferences,resourceBinded,saslFailed,saslNegotiated,serverMechanisms,sessionSupported

CLSS public final org.jivesoftware.smack.SmackConfiguration
meth public static int getKeepAliveInterval()
meth public static int getPacketReplyTimeout()
meth public static java.lang.String getVersion()
meth public static java.util.List<java.lang.String> getSaslMechs()
meth public static void addSaslMech(java.lang.String)
meth public static void addSaslMechs(java.util.Collection<java.lang.String>)
meth public static void removeSaslMech(java.lang.String)
meth public static void removeSaslMechs(java.util.Collection<java.lang.String>)
meth public static void setKeepAliveInterval(int)
meth public static void setPacketReplyTimeout(int)
supr java.lang.Object
hfds SMACK_VERSION,defaultMechs,keepAliveInterval,packetReplyTimeout

CLSS public org.jivesoftware.smack.XMPPConnection
cons public init(java.lang.String)
cons public init(java.lang.String,javax.security.auth.callback.CallbackHandler)
cons public init(org.jivesoftware.smack.ConnectionConfiguration)
cons public init(org.jivesoftware.smack.ConnectionConfiguration,javax.security.auth.callback.CallbackHandler)
fld public static boolean DEBUG_ENABLED
meth protected org.jivesoftware.smack.ConnectionConfiguration getConfiguration()
meth protected void shutdown(org.jivesoftware.smack.packet.Presence)
meth public boolean isAnonymous()
meth public boolean isAuthenticated()
meth public boolean isConnected()
meth public boolean isSecureConnection()
meth public boolean isUsingCompression()
meth public boolean isUsingTLS()
meth public int getPort()
meth public java.lang.String getConnectionID()
meth public java.lang.String getHost()
meth public java.lang.String getServiceName()
meth public java.lang.String getUser()
meth public org.jivesoftware.smack.AccountManager getAccountManager()
meth public org.jivesoftware.smack.ChatManager getChatManager()
meth public org.jivesoftware.smack.PacketCollector createPacketCollector(org.jivesoftware.smack.filter.PacketFilter)
meth public org.jivesoftware.smack.Roster getRoster()
meth public org.jivesoftware.smack.SASLAuthentication getSASLAuthentication()
meth public static void addConnectionCreationListener(org.jivesoftware.smack.ConnectionCreationListener)
meth public static void removeConnectionCreationListener(org.jivesoftware.smack.ConnectionCreationListener)
meth public void addConnectionListener(org.jivesoftware.smack.ConnectionListener)
meth public void addPacketListener(org.jivesoftware.smack.PacketListener,org.jivesoftware.smack.filter.PacketFilter)
meth public void addPacketWriterInterceptor(org.jivesoftware.smack.PacketInterceptor,org.jivesoftware.smack.filter.PacketFilter)
meth public void addPacketWriterListener(org.jivesoftware.smack.PacketListener,org.jivesoftware.smack.filter.PacketFilter)
meth public void connect() throws org.jivesoftware.smack.XMPPException
meth public void disconnect()
meth public void disconnect(org.jivesoftware.smack.packet.Presence)
meth public void login(java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void login(java.lang.String,java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void loginAnonymously() throws org.jivesoftware.smack.XMPPException
meth public void removeConnectionListener(org.jivesoftware.smack.ConnectionListener)
meth public void removePacketListener(org.jivesoftware.smack.PacketListener)
meth public void removePacketWriterInterceptor(org.jivesoftware.smack.PacketInterceptor)
meth public void removePacketWriterListener(org.jivesoftware.smack.PacketListener)
meth public void sendPacket(org.jivesoftware.smack.packet.Packet)
supr java.lang.Object
hfds accountManager,anonymous,authenticated,callbackHandler,chatManager,compressionMethods,configuration,connected,connectionCounter,connectionCounterValue,connectionEstablishedListeners,connectionID,debugger,host,packetReader,packetWriter,port,reader,roster,saslAuthentication,serviceName,socket,user,usingCompression,usingTLS,wasAuthenticated,writer

CLSS public org.jivesoftware.smack.XMPPException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,org.jivesoftware.smack.packet.XMPPError)
cons public init(java.lang.String,org.jivesoftware.smack.packet.XMPPError,java.lang.Throwable)
cons public init(java.lang.Throwable)
cons public init(org.jivesoftware.smack.packet.StreamError)
cons public init(org.jivesoftware.smack.packet.XMPPError)
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable getWrappedThrowable()
meth public org.jivesoftware.smack.packet.StreamError getStreamError()
meth public org.jivesoftware.smack.packet.XMPPError getXMPPError()
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
supr java.lang.Exception
hfds error,streamError,wrappedThrowable

CLSS public org.jivesoftware.smack.debugger.ConsoleDebugger
cons public init(org.jivesoftware.smack.XMPPConnection,java.io.Writer,java.io.Reader)
fld public static boolean printInterpreted
intf org.jivesoftware.smack.debugger.SmackDebugger
meth public java.io.Reader getReader()
meth public java.io.Reader newConnectionReader(java.io.Reader)
meth public java.io.Writer getWriter()
meth public java.io.Writer newConnectionWriter(java.io.Writer)
meth public org.jivesoftware.smack.PacketListener getReaderListener()
meth public org.jivesoftware.smack.PacketListener getWriterListener()
meth public void userHasLogged(java.lang.String)
supr java.lang.Object
hfds connListener,connection,dateFormatter,listener,reader,readerListener,writer,writerListener

CLSS public org.jivesoftware.smack.debugger.LiteDebugger
cons public init(org.jivesoftware.smack.XMPPConnection,java.io.Writer,java.io.Reader)
intf org.jivesoftware.smack.debugger.SmackDebugger
meth public java.io.Reader getReader()
meth public java.io.Reader newConnectionReader(java.io.Reader)
meth public java.io.Writer getWriter()
meth public java.io.Writer newConnectionWriter(java.io.Writer)
meth public org.jivesoftware.smack.PacketListener getReaderListener()
meth public org.jivesoftware.smack.PacketListener getWriterListener()
meth public void rootWindowClosing(java.awt.event.WindowEvent)
meth public void userHasLogged(java.lang.String)
supr java.lang.Object
hfds NEWLINE,connection,frame,listener,reader,readerListener,writer,writerListener
hcls PopupListener

CLSS public abstract interface org.jivesoftware.smack.debugger.SmackDebugger
meth public abstract java.io.Reader getReader()
meth public abstract java.io.Reader newConnectionReader(java.io.Reader)
meth public abstract java.io.Writer getWriter()
meth public abstract java.io.Writer newConnectionWriter(java.io.Writer)
meth public abstract org.jivesoftware.smack.PacketListener getReaderListener()
meth public abstract org.jivesoftware.smack.PacketListener getWriterListener()
meth public abstract void userHasLogged(java.lang.String)

CLSS public org.jivesoftware.smack.filter.AndFilter
cons public !varargs init(org.jivesoftware.smack.filter.PacketFilter[])
cons public init()
intf org.jivesoftware.smack.filter.PacketFilter
meth public boolean accept(org.jivesoftware.smack.packet.Packet)
meth public java.lang.String toString()
meth public void addFilter(org.jivesoftware.smack.filter.PacketFilter)
supr java.lang.Object
hfds filters

CLSS public org.jivesoftware.smack.filter.FromContainsFilter
cons public init(java.lang.String)
intf org.jivesoftware.smack.filter.PacketFilter
meth public boolean accept(org.jivesoftware.smack.packet.Packet)
supr java.lang.Object
hfds from

CLSS public org.jivesoftware.smack.filter.FromMatchesFilter
cons public init(java.lang.String)
intf org.jivesoftware.smack.filter.PacketFilter
meth public boolean accept(org.jivesoftware.smack.packet.Packet)
meth public java.lang.String toString()
supr java.lang.Object
hfds address,matchBareJID

CLSS public org.jivesoftware.smack.filter.IQTypeFilter
cons public init(org.jivesoftware.smack.packet.IQ$Type)
intf org.jivesoftware.smack.filter.PacketFilter
meth public boolean accept(org.jivesoftware.smack.packet.Packet)
supr java.lang.Object
hfds type

CLSS public org.jivesoftware.smack.filter.MessageTypeFilter
cons public init(org.jivesoftware.smack.packet.Message$Type)
intf org.jivesoftware.smack.filter.PacketFilter
meth public boolean accept(org.jivesoftware.smack.packet.Packet)
supr java.lang.Object
hfds type

CLSS public org.jivesoftware.smack.filter.NotFilter
cons public init(org.jivesoftware.smack.filter.PacketFilter)
intf org.jivesoftware.smack.filter.PacketFilter
meth public boolean accept(org.jivesoftware.smack.packet.Packet)
supr java.lang.Object
hfds filter

CLSS public org.jivesoftware.smack.filter.OrFilter
cons public init()
cons public init(org.jivesoftware.smack.filter.PacketFilter,org.jivesoftware.smack.filter.PacketFilter)
intf org.jivesoftware.smack.filter.PacketFilter
meth public boolean accept(org.jivesoftware.smack.packet.Packet)
meth public java.lang.String toString()
meth public void addFilter(org.jivesoftware.smack.filter.PacketFilter)
supr java.lang.Object
hfds filters,size

CLSS public org.jivesoftware.smack.filter.PacketExtensionFilter
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
intf org.jivesoftware.smack.filter.PacketFilter
meth public boolean accept(org.jivesoftware.smack.packet.Packet)
supr java.lang.Object
hfds elementName,namespace

CLSS public abstract interface org.jivesoftware.smack.filter.PacketFilter
meth public abstract boolean accept(org.jivesoftware.smack.packet.Packet)

CLSS public org.jivesoftware.smack.filter.PacketIDFilter
cons public init(java.lang.String)
intf org.jivesoftware.smack.filter.PacketFilter
meth public boolean accept(org.jivesoftware.smack.packet.Packet)
meth public java.lang.String toString()
supr java.lang.Object
hfds packetID

CLSS public org.jivesoftware.smack.filter.PacketTypeFilter
cons public init(java.lang.Class)
intf org.jivesoftware.smack.filter.PacketFilter
meth public boolean accept(org.jivesoftware.smack.packet.Packet)
meth public java.lang.String toString()
supr java.lang.Object
hfds packetType

CLSS public org.jivesoftware.smack.filter.ThreadFilter
cons public init(java.lang.String)
intf org.jivesoftware.smack.filter.PacketFilter
meth public boolean accept(org.jivesoftware.smack.packet.Packet)
supr java.lang.Object
hfds thread

CLSS public org.jivesoftware.smack.filter.ToContainsFilter
cons public init(java.lang.String)
intf org.jivesoftware.smack.filter.PacketFilter
meth public boolean accept(org.jivesoftware.smack.packet.Packet)
supr java.lang.Object
hfds to

CLSS public org.jivesoftware.smack.packet.Authentication
cons public init()
meth public java.lang.String getChildElementXML()
meth public java.lang.String getDigest()
meth public java.lang.String getPassword()
meth public java.lang.String getResource()
meth public java.lang.String getUsername()
meth public void setDigest(java.lang.String)
meth public void setDigest(java.lang.String,java.lang.String)
meth public void setPassword(java.lang.String)
meth public void setResource(java.lang.String)
meth public void setUsername(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds digest,password,resource,username

CLSS public org.jivesoftware.smack.packet.Bind
cons public init()
meth public java.lang.String getChildElementXML()
meth public java.lang.String getJid()
meth public java.lang.String getResource()
meth public void setJid(java.lang.String)
meth public void setResource(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds jid,resource

CLSS public org.jivesoftware.smack.packet.DefaultPacketExtension
cons public init(java.lang.String,java.lang.String)
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getValue(java.lang.String)
meth public java.lang.String toXML()
meth public java.util.Collection<java.lang.String> getNames()
meth public void setValue(java.lang.String,java.lang.String)
supr java.lang.Object
hfds elementName,map,namespace

CLSS public abstract org.jivesoftware.smack.packet.IQ
cons public init()
innr public static Type
meth public abstract java.lang.String getChildElementXML()
meth public java.lang.String toXML()
meth public org.jivesoftware.smack.packet.IQ$Type getType()
meth public void setType(org.jivesoftware.smack.packet.IQ$Type)
supr org.jivesoftware.smack.packet.Packet
hfds type

CLSS public static org.jivesoftware.smack.packet.IQ$Type
 outer org.jivesoftware.smack.packet.IQ
fld public final static org.jivesoftware.smack.packet.IQ$Type ERROR
fld public final static org.jivesoftware.smack.packet.IQ$Type GET
fld public final static org.jivesoftware.smack.packet.IQ$Type RESULT
fld public final static org.jivesoftware.smack.packet.IQ$Type SET
meth public java.lang.String toString()
meth public static org.jivesoftware.smack.packet.IQ$Type fromString(java.lang.String)
supr java.lang.Object
hfds value

CLSS public org.jivesoftware.smack.packet.Message
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,org.jivesoftware.smack.packet.Message$Type)
innr public final static !enum Type
innr public static Body
meth public boolean equals(java.lang.Object)
meth public boolean removeBody(java.lang.String)
meth public boolean removeBody(org.jivesoftware.smack.packet.Message$Body)
meth public int hashCode()
meth public java.lang.String getBody()
meth public java.lang.String getBody(java.lang.String)
meth public java.lang.String getSubject()
meth public java.lang.String getThread()
meth public java.lang.String toXML()
meth public java.util.Collection<java.lang.String> getBodyLanguages()
meth public java.util.Collection<org.jivesoftware.smack.packet.Message$Body> getBodies()
meth public org.jivesoftware.smack.packet.Message$Body addBody(java.lang.String,java.lang.String)
meth public org.jivesoftware.smack.packet.Message$Type getType()
meth public void setBody(java.lang.String)
meth public void setLanguage(java.lang.String)
meth public void setSubject(java.lang.String)
meth public void setThread(java.lang.String)
meth public void setType(org.jivesoftware.smack.packet.Message$Type)
supr org.jivesoftware.smack.packet.Packet
hfds bodies,language,subject,thread,type

CLSS public static org.jivesoftware.smack.packet.Message$Body
 outer org.jivesoftware.smack.packet.Message
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getLanguage()
meth public java.lang.String getMessage()
supr java.lang.Object
hfds langauge,message

CLSS public final static !enum org.jivesoftware.smack.packet.Message$Type
 outer org.jivesoftware.smack.packet.Message
fld public final static org.jivesoftware.smack.packet.Message$Type chat
fld public final static org.jivesoftware.smack.packet.Message$Type error
fld public final static org.jivesoftware.smack.packet.Message$Type groupchat
fld public final static org.jivesoftware.smack.packet.Message$Type headline
fld public final static org.jivesoftware.smack.packet.Message$Type normal
meth public final static org.jivesoftware.smack.packet.Message$Type[] values()
meth public static org.jivesoftware.smack.packet.Message$Type fromString(java.lang.String)
meth public static org.jivesoftware.smack.packet.Message$Type valueOf(java.lang.String)
supr java.lang.Enum<org.jivesoftware.smack.packet.Message$Type>

CLSS public abstract org.jivesoftware.smack.packet.Packet
cons public init()
fld protected final static java.lang.String DEFAULT_LANGUAGE
fld public final static java.lang.String ID_NOT_AVAILABLE = "ID_NOT_AVAILABLE"
meth protected java.lang.String getExtensionsXML()
meth protected static java.lang.String getDefaultLanguage()
meth protected static java.lang.String parseXMLLang(java.lang.String)
meth public abstract java.lang.String toXML()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.String getFrom()
meth public java.lang.String getPacketID()
meth public java.lang.String getTo()
meth public java.lang.String getXmlns()
meth public java.util.Collection<java.lang.String> getPropertyNames()
meth public java.util.Collection<org.jivesoftware.smack.packet.PacketExtension> getExtensions()
meth public org.jivesoftware.smack.packet.PacketExtension getExtension(java.lang.String)
meth public org.jivesoftware.smack.packet.PacketExtension getExtension(java.lang.String,java.lang.String)
meth public org.jivesoftware.smack.packet.XMPPError getError()
meth public static java.lang.String nextID()
meth public static void setDefaultXmlns(java.lang.String)
meth public void addExtension(org.jivesoftware.smack.packet.PacketExtension)
meth public void deleteProperty(java.lang.String)
meth public void removeExtension(org.jivesoftware.smack.packet.PacketExtension)
meth public void setError(org.jivesoftware.smack.packet.XMPPError)
meth public void setFrom(java.lang.String)
meth public void setPacketID(java.lang.String)
meth public void setProperty(java.lang.String,java.lang.Object)
meth public void setTo(java.lang.String)
supr java.lang.Object
hfds DEFAULT_XML_NS,error,from,id,packetExtensions,packetID,prefix,properties,to,xmlns

CLSS public abstract interface org.jivesoftware.smack.packet.PacketExtension
meth public abstract java.lang.String getElementName()
meth public abstract java.lang.String getNamespace()
meth public abstract java.lang.String toXML()

CLSS public org.jivesoftware.smack.packet.Presence
cons public init(org.jivesoftware.smack.packet.Presence$Type)
cons public init(org.jivesoftware.smack.packet.Presence$Type,java.lang.String,int,org.jivesoftware.smack.packet.Presence$Mode)
innr public final static !enum Mode
innr public final static !enum Type
meth public boolean isAvailable()
meth public boolean isAway()
meth public int getPriority()
meth public java.lang.String getStatus()
meth public java.lang.String toString()
meth public java.lang.String toXML()
meth public org.jivesoftware.smack.packet.Presence$Mode getMode()
meth public org.jivesoftware.smack.packet.Presence$Type getType()
meth public void setLanguage(java.lang.String)
meth public void setMode(org.jivesoftware.smack.packet.Presence$Mode)
meth public void setPriority(int)
meth public void setStatus(java.lang.String)
meth public void setType(org.jivesoftware.smack.packet.Presence$Type)
supr org.jivesoftware.smack.packet.Packet
hfds language,mode,priority,status,type

CLSS public final static !enum org.jivesoftware.smack.packet.Presence$Mode
 outer org.jivesoftware.smack.packet.Presence
fld public final static org.jivesoftware.smack.packet.Presence$Mode available
fld public final static org.jivesoftware.smack.packet.Presence$Mode away
fld public final static org.jivesoftware.smack.packet.Presence$Mode chat
fld public final static org.jivesoftware.smack.packet.Presence$Mode dnd
fld public final static org.jivesoftware.smack.packet.Presence$Mode xa
meth public final static org.jivesoftware.smack.packet.Presence$Mode[] values()
meth public static org.jivesoftware.smack.packet.Presence$Mode valueOf(java.lang.String)
supr java.lang.Enum<org.jivesoftware.smack.packet.Presence$Mode>

CLSS public final static !enum org.jivesoftware.smack.packet.Presence$Type
 outer org.jivesoftware.smack.packet.Presence
fld public final static org.jivesoftware.smack.packet.Presence$Type available
fld public final static org.jivesoftware.smack.packet.Presence$Type error
fld public final static org.jivesoftware.smack.packet.Presence$Type subscribe
fld public final static org.jivesoftware.smack.packet.Presence$Type subscribed
fld public final static org.jivesoftware.smack.packet.Presence$Type unavailable
fld public final static org.jivesoftware.smack.packet.Presence$Type unsubscribe
fld public final static org.jivesoftware.smack.packet.Presence$Type unsubscribed
meth public final static org.jivesoftware.smack.packet.Presence$Type[] values()
meth public static org.jivesoftware.smack.packet.Presence$Type valueOf(java.lang.String)
supr java.lang.Enum<org.jivesoftware.smack.packet.Presence$Type>

CLSS public org.jivesoftware.smack.packet.Privacy
cons public init()
meth public boolean changeDefaultList(java.lang.String)
meth public boolean isDeclineActiveList()
meth public boolean isDeclineDefaultList()
meth public java.lang.String getActiveName()
meth public java.lang.String getChildElementXML()
meth public java.lang.String getDefaultName()
meth public java.util.List setPrivacyList(java.lang.String,java.util.List<org.jivesoftware.smack.packet.PrivacyItem>)
meth public java.util.List<org.jivesoftware.smack.packet.PrivacyItem> getActivePrivacyList()
meth public java.util.List<org.jivesoftware.smack.packet.PrivacyItem> getDefaultPrivacyList()
meth public java.util.List<org.jivesoftware.smack.packet.PrivacyItem> getPrivacyList(java.lang.String)
meth public java.util.List<org.jivesoftware.smack.packet.PrivacyItem> setActivePrivacyList()
meth public java.util.Map<java.lang.String,java.util.List<org.jivesoftware.smack.packet.PrivacyItem>> getItemLists()
meth public java.util.Set<java.lang.String> getPrivacyListNames()
meth public org.jivesoftware.smack.packet.PrivacyItem getItem(java.lang.String,int)
meth public void deleteList(java.lang.String)
meth public void deletePrivacyList(java.lang.String)
meth public void setActiveName(java.lang.String)
meth public void setDeclineActiveList(boolean)
meth public void setDeclineDefaultList(boolean)
meth public void setDefaultName(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds activeName,declineActiveList,declineDefaultList,defaultName,itemLists

CLSS public org.jivesoftware.smack.packet.PrivacyItem
cons public init(java.lang.String,boolean,int)
innr public final static !enum Type
innr public static PrivacyRule
meth public boolean isAllow()
meth public boolean isFilterEverything()
meth public boolean isFilterIQ()
meth public boolean isFilterMessage()
meth public boolean isFilterPresence_in()
meth public boolean isFilterPresence_out()
meth public int getOrder()
meth public java.lang.String getValue()
meth public java.lang.String toXML()
meth public org.jivesoftware.smack.packet.PrivacyItem$Type getType()
meth public void setFilterIQ(boolean)
meth public void setFilterMessage(boolean)
meth public void setFilterPresence_in(boolean)
meth public void setFilterPresence_out(boolean)
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds allow,filterIQ,filterMessage,filterPresence_in,filterPresence_out,order,rule

CLSS public static org.jivesoftware.smack.packet.PrivacyItem$PrivacyRule
 outer org.jivesoftware.smack.packet.PrivacyItem
cons public init()
fld public final static java.lang.String SUBSCRIPTION_BOTH = "both"
fld public final static java.lang.String SUBSCRIPTION_FROM = "from"
fld public final static java.lang.String SUBSCRIPTION_NONE = "none"
fld public final static java.lang.String SUBSCRIPTION_TO = "to"
meth protected static org.jivesoftware.smack.packet.PrivacyItem$PrivacyRule fromString(java.lang.String)
meth protected void setValue(java.lang.String)
meth public boolean isSuscription()
meth public java.lang.String getValue()
meth public org.jivesoftware.smack.packet.PrivacyItem$Type getType()
supr java.lang.Object
hfds type,value

CLSS public final static !enum org.jivesoftware.smack.packet.PrivacyItem$Type
 outer org.jivesoftware.smack.packet.PrivacyItem
fld public final static org.jivesoftware.smack.packet.PrivacyItem$Type group
fld public final static org.jivesoftware.smack.packet.PrivacyItem$Type jid
fld public final static org.jivesoftware.smack.packet.PrivacyItem$Type subscription
meth public final static org.jivesoftware.smack.packet.PrivacyItem$Type[] values()
meth public static org.jivesoftware.smack.packet.PrivacyItem$Type valueOf(java.lang.String)
supr java.lang.Enum<org.jivesoftware.smack.packet.PrivacyItem$Type>

CLSS public org.jivesoftware.smack.packet.Registration
cons public init()
meth public java.lang.String getChildElementXML()
meth public java.lang.String getInstructions()
meth public java.util.Map<java.lang.String,java.lang.String> getAttributes()
meth public void setAttributes(java.util.Map<java.lang.String,java.lang.String>)
meth public void setInstructions(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds attributes,instructions

CLSS public org.jivesoftware.smack.packet.RosterPacket
cons public init()
innr public final static !enum ItemType
innr public static Item
innr public static ItemStatus
meth public int getRosterItemCount()
meth public java.lang.String getChildElementXML()
meth public java.util.Collection<org.jivesoftware.smack.packet.RosterPacket$Item> getRosterItems()
meth public void addRosterItem(org.jivesoftware.smack.packet.RosterPacket$Item)
supr org.jivesoftware.smack.packet.IQ
hfds rosterItems

CLSS public static org.jivesoftware.smack.packet.RosterPacket$Item
 outer org.jivesoftware.smack.packet.RosterPacket
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String getUser()
meth public java.lang.String toXML()
meth public java.util.Set<java.lang.String> getGroupNames()
meth public org.jivesoftware.smack.packet.RosterPacket$ItemStatus getItemStatus()
meth public org.jivesoftware.smack.packet.RosterPacket$ItemType getItemType()
meth public void addGroupName(java.lang.String)
meth public void removeGroupName(java.lang.String)
meth public void setItemStatus(org.jivesoftware.smack.packet.RosterPacket$ItemStatus)
meth public void setItemType(org.jivesoftware.smack.packet.RosterPacket$ItemType)
meth public void setName(java.lang.String)
supr java.lang.Object
hfds groupNames,itemStatus,itemType,name,user

CLSS public static org.jivesoftware.smack.packet.RosterPacket$ItemStatus
 outer org.jivesoftware.smack.packet.RosterPacket
fld public final static org.jivesoftware.smack.packet.RosterPacket$ItemStatus SUBSCRIPTION_PENDING
fld public final static org.jivesoftware.smack.packet.RosterPacket$ItemStatus UNSUBSCRIPTION_PENDING
meth public java.lang.String toString()
meth public static org.jivesoftware.smack.packet.RosterPacket$ItemStatus fromString(java.lang.String)
supr java.lang.Object
hfds value

CLSS public final static !enum org.jivesoftware.smack.packet.RosterPacket$ItemType
 outer org.jivesoftware.smack.packet.RosterPacket
fld public final static org.jivesoftware.smack.packet.RosterPacket$ItemType both
fld public final static org.jivesoftware.smack.packet.RosterPacket$ItemType from
fld public final static org.jivesoftware.smack.packet.RosterPacket$ItemType none
fld public final static org.jivesoftware.smack.packet.RosterPacket$ItemType remove
fld public final static org.jivesoftware.smack.packet.RosterPacket$ItemType to
meth public final static org.jivesoftware.smack.packet.RosterPacket$ItemType[] values()
meth public static org.jivesoftware.smack.packet.RosterPacket$ItemType valueOf(java.lang.String)
supr java.lang.Enum<org.jivesoftware.smack.packet.RosterPacket$ItemType>

CLSS public org.jivesoftware.smack.packet.Session
cons public init()
meth public java.lang.String getChildElementXML()
supr org.jivesoftware.smack.packet.IQ

CLSS public org.jivesoftware.smack.packet.StreamError
cons public init(java.lang.String)
meth public java.lang.String getCode()
meth public java.lang.String toString()
supr java.lang.Object
hfds code

CLSS public org.jivesoftware.smack.packet.XMPPError
cons public init(int)
cons public init(int,java.lang.String)
cons public init(int,org.jivesoftware.smack.packet.XMPPError$Type,java.lang.String,java.lang.String,java.util.List<org.jivesoftware.smack.packet.PacketExtension>)
cons public init(org.jivesoftware.smack.packet.XMPPError$Condition)
cons public init(org.jivesoftware.smack.packet.XMPPError$Condition,java.lang.String)
innr public final static !enum Type
innr public static Condition
meth public int getCode()
meth public java.lang.String getCondition()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.String toXML()
meth public java.util.List<org.jivesoftware.smack.packet.PacketExtension> getExtensions()
meth public org.jivesoftware.smack.packet.PacketExtension getExtension(java.lang.String,java.lang.String)
meth public org.jivesoftware.smack.packet.XMPPError$Type getType()
meth public void addExtension(org.jivesoftware.smack.packet.PacketExtension)
meth public void setExtension(java.util.List<org.jivesoftware.smack.packet.PacketExtension>)
supr java.lang.Object
hfds applicationExtensions,code,condition,message,type
hcls ErrorSpecification

CLSS public static org.jivesoftware.smack.packet.XMPPError$Condition
 outer org.jivesoftware.smack.packet.XMPPError
cons public init(java.lang.String)
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition bad_request
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition conflict
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition feature_not_implemented
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition forbidden
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition gone
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition interna_server_error
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition item_not_found
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition jid_malformed
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition no_acceptable
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition not_allowed
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition not_authorized
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition payment_required
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition recipient_unavailable
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition redirect
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition registration_required
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition remote_server_error
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition remote_server_not_found
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition remote_server_timeout
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition request_timeout
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition resource_constraint
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition service_unavailable
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition subscription_required
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition undefined_condition
fld public final static org.jivesoftware.smack.packet.XMPPError$Condition unexpected_request
meth public java.lang.String toString()
supr java.lang.Object
hfds value

CLSS public final static !enum org.jivesoftware.smack.packet.XMPPError$Type
 outer org.jivesoftware.smack.packet.XMPPError
fld public final static org.jivesoftware.smack.packet.XMPPError$Type AUTH
fld public final static org.jivesoftware.smack.packet.XMPPError$Type CANCEL
fld public final static org.jivesoftware.smack.packet.XMPPError$Type CONTINUE
fld public final static org.jivesoftware.smack.packet.XMPPError$Type MODIFY
fld public final static org.jivesoftware.smack.packet.XMPPError$Type WAIT
meth public final static org.jivesoftware.smack.packet.XMPPError$Type[] values()
meth public static org.jivesoftware.smack.packet.XMPPError$Type valueOf(java.lang.String)
supr java.lang.Enum<org.jivesoftware.smack.packet.XMPPError$Type>

CLSS public abstract interface org.jivesoftware.smack.provider.IQProvider
meth public abstract org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception

CLSS public abstract interface org.jivesoftware.smack.provider.PacketExtensionProvider
meth public abstract org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception

CLSS public org.jivesoftware.smack.provider.PrivacyProvider
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
meth public org.jivesoftware.smack.packet.PrivacyItem parseItem(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
meth public void parseList(org.xmlpull.v1.XmlPullParser,org.jivesoftware.smack.packet.Privacy) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smack.provider.ProviderManager
meth protected void initialize()
meth public java.lang.Object getExtensionProvider(java.lang.String,java.lang.String)
meth public java.lang.Object getIQProvider(java.lang.String,java.lang.String)
meth public java.util.Collection<java.lang.Object> getExtensionProviders()
meth public java.util.Collection<java.lang.Object> getIQProviders()
meth public static org.jivesoftware.smack.provider.ProviderManager getInstance()
meth public static void setInstance(org.jivesoftware.smack.provider.ProviderManager)
meth public void addExtensionProvider(java.lang.String,java.lang.String,java.lang.Object)
meth public void addIQProvider(java.lang.String,java.lang.String,java.lang.Object)
meth public void removeExtensionProvider(java.lang.String,java.lang.String)
meth public void removeIQProvider(java.lang.String,java.lang.String)
supr java.lang.Object
hfds extensionProviders,instance,iqProviders

CLSS public org.jivesoftware.smack.sasl.SASLAnonymous
cons public init(org.jivesoftware.smack.SASLAuthentication)
meth protected java.lang.String getName()
meth protected void authenticate() throws java.io.IOException
meth public void authenticate(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public void authenticate(java.lang.String,java.lang.String,javax.security.auth.callback.CallbackHandler) throws java.io.IOException
meth public void challengeReceived(java.lang.String) throws java.io.IOException
supr org.jivesoftware.smack.sasl.SASLMechanism

CLSS public org.jivesoftware.smack.sasl.SASLCramMD5Mechanism
cons public init(org.jivesoftware.smack.SASLAuthentication)
meth protected java.lang.String getName()
supr org.jivesoftware.smack.sasl.SASLMechanism

CLSS public org.jivesoftware.smack.sasl.SASLDigestMD5Mechanism
cons public init(org.jivesoftware.smack.SASLAuthentication)
meth protected java.lang.String getName()
supr org.jivesoftware.smack.sasl.SASLMechanism

CLSS public org.jivesoftware.smack.sasl.SASLExternalMechanism
cons public init(org.jivesoftware.smack.SASLAuthentication)
meth protected java.lang.String getName()
supr org.jivesoftware.smack.sasl.SASLMechanism

CLSS public org.jivesoftware.smack.sasl.SASLGSSAPIMechanism
cons public init(org.jivesoftware.smack.SASLAuthentication)
meth protected java.lang.String getName()
meth public void authenticate(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException,org.jivesoftware.smack.XMPPException
meth public void authenticate(java.lang.String,java.lang.String,javax.security.auth.callback.CallbackHandler) throws java.io.IOException,org.jivesoftware.smack.XMPPException
supr org.jivesoftware.smack.sasl.SASLMechanism

CLSS public abstract org.jivesoftware.smack.sasl.SASLMechanism
cons public init(org.jivesoftware.smack.SASLAuthentication)
fld protected java.lang.String authenticationId
fld protected java.lang.String hostname
fld protected java.lang.String password
fld protected javax.security.sasl.SaslClient sc
intf javax.security.auth.callback.CallbackHandler
meth protected abstract java.lang.String getName()
meth protected org.jivesoftware.smack.SASLAuthentication getSASLAuthentication()
meth protected void authenticate() throws java.io.IOException,org.jivesoftware.smack.XMPPException
meth public void authenticate(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException,org.jivesoftware.smack.XMPPException
meth public void authenticate(java.lang.String,java.lang.String,javax.security.auth.callback.CallbackHandler) throws java.io.IOException,org.jivesoftware.smack.XMPPException
meth public void challengeReceived(java.lang.String) throws java.io.IOException
meth public void handle(javax.security.auth.callback.Callback[]) throws java.io.IOException,javax.security.auth.callback.UnsupportedCallbackException
supr java.lang.Object
hfds saslAuthentication

CLSS public org.jivesoftware.smack.sasl.SASLPlainMechanism
cons public init(org.jivesoftware.smack.SASLAuthentication)
meth protected java.lang.String getName()
supr org.jivesoftware.smack.sasl.SASLMechanism

CLSS public org.jivesoftware.smack.util.Base64
fld public final static int DECODE = 0
fld public final static int DONT_BREAK_LINES = 8
fld public final static int ENCODE = 1
fld public final static int GZIP = 2
fld public final static int NO_OPTIONS = 0
fld public final static int ORDERED = 32
fld public final static int URL_SAFE = 16
innr public static InputStream
innr public static OutputStream
meth public final static void main(java.lang.String[])
meth public static boolean decodeToFile(java.lang.String,java.lang.String)
meth public static boolean encodeToFile(byte[],java.lang.String)
meth public static byte[] decode(byte[],int,int,int)
meth public static byte[] decode(java.lang.String)
meth public static byte[] decode(java.lang.String,int)
meth public static byte[] decodeFromFile(java.lang.String)
meth public static java.lang.Object decodeToObject(java.lang.String)
meth public static java.lang.String encodeBytes(byte[])
meth public static java.lang.String encodeBytes(byte[],int)
meth public static java.lang.String encodeBytes(byte[],int,int)
meth public static java.lang.String encodeBytes(byte[],int,int,int)
meth public static java.lang.String encodeFromFile(java.lang.String)
meth public static java.lang.String encodeObject(java.io.Serializable)
meth public static java.lang.String encodeObject(java.io.Serializable,int)
meth public static void decodeFileToFile(java.lang.String,java.lang.String)
meth public static void encodeFileToFile(java.lang.String,java.lang.String)
supr java.lang.Object
hfds EQUALS_SIGN,EQUALS_SIGN_ENC,MAX_LINE_LENGTH,NEW_LINE,PREFERRED_ENCODING,WHITE_SPACE_ENC,_ORDERED_ALPHABET,_ORDERED_DECODABET,_STANDARD_ALPHABET,_STANDARD_DECODABET,_URL_SAFE_ALPHABET,_URL_SAFE_DECODABET

CLSS public static org.jivesoftware.smack.util.Base64$InputStream
 outer org.jivesoftware.smack.util.Base64
cons public init(java.io.InputStream)
cons public init(java.io.InputStream,int)
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
supr java.io.FilterInputStream
hfds alphabet,breakLines,buffer,bufferLength,decodabet,encode,lineLength,numSigBytes,options,position

CLSS public static org.jivesoftware.smack.util.Base64$OutputStream
 outer org.jivesoftware.smack.util.Base64
cons public init(java.io.OutputStream)
cons public init(java.io.OutputStream,int)
meth public void close() throws java.io.IOException
meth public void flushBase64() throws java.io.IOException
meth public void resumeEncoding()
meth public void suspendEncoding() throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
supr java.io.FilterOutputStream
hfds alphabet,b4,breakLines,buffer,bufferLength,decodabet,encode,lineLength,options,position,suspendEncoding

CLSS public org.jivesoftware.smack.util.Cache<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(int,long)
fld protected int maxCacheSize
fld protected java.lang.Object ageList
fld protected java.lang.Object lastAccessedList
fld protected java.util.Map<{org.jivesoftware.smack.util.Cache%0},org.jivesoftware.smack.util.Cache$CacheObject<{org.jivesoftware.smack.util.Cache%1}>> map
fld protected long cacheHits
fld protected long cacheMisses
fld protected long maxLifetime
intf java.util.Map<{org.jivesoftware.smack.util.Cache%0},{org.jivesoftware.smack.util.Cache%1}>
meth protected void cullCache()
meth protected void deleteExpiredEntries()
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean isEmpty()
meth public int getMaxCacheSize()
meth public int size()
meth public java.util.Collection<{org.jivesoftware.smack.util.Cache%1}> values()
meth public java.util.Set<java.util.Map$Entry<{org.jivesoftware.smack.util.Cache%0},{org.jivesoftware.smack.util.Cache%1}>> entrySet()
meth public java.util.Set<{org.jivesoftware.smack.util.Cache%0}> keySet()
meth public long getCacheHits()
meth public long getCacheMisses()
meth public long getMaxLifetime()
meth public void clear()
meth public void putAll(java.util.Map<? extends {org.jivesoftware.smack.util.Cache%0},? extends {org.jivesoftware.smack.util.Cache%1}>)
meth public void setMaxCacheSize(int)
meth public void setMaxLifetime(long)
meth public {org.jivesoftware.smack.util.Cache%1} get(java.lang.Object)
meth public {org.jivesoftware.smack.util.Cache%1} put({org.jivesoftware.smack.util.Cache%0},{org.jivesoftware.smack.util.Cache%1})
meth public {org.jivesoftware.smack.util.Cache%1} remove(java.lang.Object)
meth public {org.jivesoftware.smack.util.Cache%1} remove(java.lang.Object,boolean)
supr java.lang.Object
hcls CacheObject,LinkedList,LinkedListNode

CLSS public org.jivesoftware.smack.util.DNSUtil
cons public init()
innr public static HostAddress
meth public static org.jivesoftware.smack.util.DNSUtil$HostAddress resolveXMPPDomain(java.lang.String)
meth public static org.jivesoftware.smack.util.DNSUtil$HostAddress resolveXMPPServerDomain(java.lang.String)
supr java.lang.Object
hfds cache,context

CLSS public static org.jivesoftware.smack.util.DNSUtil$HostAddress
 outer org.jivesoftware.smack.util.DNSUtil
meth public boolean equals(java.lang.Object)
meth public int getPort()
meth public java.lang.String getHost()
meth public java.lang.String toString()
supr java.lang.Object
hfds host,port

CLSS public org.jivesoftware.smack.util.ObservableReader
cons public init(java.io.Reader)
meth public boolean markSupported()
meth public boolean ready() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(char[]) throws java.io.IOException
meth public int read(char[],int,int) throws java.io.IOException
meth public long skip(long) throws java.io.IOException
meth public void addReaderListener(org.jivesoftware.smack.util.ReaderListener)
meth public void close() throws java.io.IOException
meth public void mark(int) throws java.io.IOException
meth public void removeReaderListener(org.jivesoftware.smack.util.ReaderListener)
meth public void reset() throws java.io.IOException
supr java.io.Reader
hfds listeners,wrappedReader

CLSS public org.jivesoftware.smack.util.ObservableWriter
cons public init(java.io.Writer)
meth public void addWriterListener(org.jivesoftware.smack.util.WriterListener)
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void removeWriterListener(org.jivesoftware.smack.util.WriterListener)
meth public void write(char[]) throws java.io.IOException
meth public void write(char[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
meth public void write(java.lang.String) throws java.io.IOException
meth public void write(java.lang.String,int,int) throws java.io.IOException
supr java.io.Writer
hfds listeners,wrappedWriter

CLSS public org.jivesoftware.smack.util.PacketParserUtils
cons public init()
meth public static java.lang.Object parseWithIntrospection(java.lang.String,java.lang.Class,org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
meth public static java.util.Map<java.lang.String,java.lang.Object> parseProperties(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
meth public static org.jivesoftware.smack.packet.Packet parseMessage(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
meth public static org.jivesoftware.smack.packet.PacketExtension parsePacketExtension(java.lang.String,java.lang.String,org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
meth public static org.jivesoftware.smack.packet.Presence parsePresence(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
meth public static org.jivesoftware.smack.packet.XMPPError parseError(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object
hfds PROPERTIES_NAMESPACE

CLSS public abstract interface org.jivesoftware.smack.util.ReaderListener
meth public abstract void read(java.lang.String)

CLSS public org.jivesoftware.smack.util.StringUtils
meth public static byte[] decodeBase64(java.lang.String)
meth public static java.lang.String encodeBase64(byte[])
meth public static java.lang.String encodeBase64(byte[],boolean)
meth public static java.lang.String encodeBase64(byte[],int,int,boolean)
meth public static java.lang.String encodeBase64(java.lang.String)
meth public static java.lang.String encodeHex(byte[])
meth public static java.lang.String escapeForXML(java.lang.String)
meth public static java.lang.String escapeNode(java.lang.String)
meth public static java.lang.String hash(java.lang.String)
meth public static java.lang.String parseBareAddress(java.lang.String)
meth public static java.lang.String parseName(java.lang.String)
meth public static java.lang.String parseResource(java.lang.String)
meth public static java.lang.String parseServer(java.lang.String)
meth public static java.lang.String randomString(int)
meth public static java.lang.String unescapeNode(java.lang.String)
supr java.lang.Object
hfds AMP_ENCODE,GT_ENCODE,LT_ENCODE,QUOTE_ENCODE,digest,numbersAndLetters,randGen

CLSS public abstract interface org.jivesoftware.smack.util.WriterListener
meth public abstract void write(java.lang.String)

CLSS public org.jivesoftware.smack.util.collections.AbstractHashedMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init()
cons protected init(int)
cons protected init(int,float)
cons protected init(int,float,int)
cons protected init(java.util.Map<? extends {org.jivesoftware.smack.util.collections.AbstractHashedMap%0},? extends {org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>)
fld protected final static float DEFAULT_LOAD_FACTOR = 0.75
fld protected final static int DEFAULT_CAPACITY = 16
fld protected final static int DEFAULT_THRESHOLD = 12
fld protected final static int MAXIMUM_CAPACITY = 1073741824
fld protected final static java.lang.Object NULL
fld protected final static java.lang.String GETKEY_INVALID = "getKey() can only be called after next() and before remove()"
fld protected final static java.lang.String GETVALUE_INVALID = "getValue() can only be called after next() and before remove()"
fld protected final static java.lang.String NO_NEXT_ENTRY = "No next() entry in the iteration"
fld protected final static java.lang.String NO_PREVIOUS_ENTRY = "No previous() entry in the iteration"
fld protected final static java.lang.String REMOVE_INVALID = "remove() can only be called once after next()"
fld protected final static java.lang.String SETVALUE_INVALID = "setValue() can only be called after next() and before remove()"
fld protected float loadFactor
fld protected int modCount
fld protected int size
fld protected int threshold
fld protected org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySet<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}> entrySet
fld protected org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>[] data
fld protected org.jivesoftware.smack.util.collections.AbstractHashedMap$KeySet<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}> keySet
fld protected org.jivesoftware.smack.util.collections.AbstractHashedMap$Values<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}> values
innr protected abstract static HashIterator
innr protected static EntrySet
innr protected static EntrySetIterator
innr protected static HashEntry
innr protected static HashMapIterator
innr protected static KeySet
innr protected static KeySetIterator
innr protected static Values
innr protected static ValuesIterator
intf org.jivesoftware.smack.util.collections.IterableMap<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>
meth protected boolean isEqualKey(java.lang.Object,java.lang.Object)
meth protected boolean isEqualValue(java.lang.Object,java.lang.Object)
meth protected int calculateNewCapacity(int)
meth protected int calculateThreshold(int,float)
meth protected int entryHashCode(org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>)
meth protected int hash(java.lang.Object)
meth protected int hashIndex(int,int)
meth protected java.lang.Object clone()
meth protected java.util.Iterator<java.util.Map$Entry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>> createEntrySetIterator()
meth protected java.util.Iterator<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0}> createKeySetIterator()
meth protected java.util.Iterator<{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}> createValuesIterator()
meth protected org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}> createEntry(org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>,int,{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1})
meth protected org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}> entryNext(org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>)
meth protected org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}> getEntry(java.lang.Object)
meth protected void addEntry(org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>,int)
meth protected void addMapping(int,int,{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1})
meth protected void checkCapacity()
meth protected void destroyEntry(org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>)
meth protected void doReadObject(java.io.ObjectInputStream) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected void doWriteObject(java.io.ObjectOutputStream) throws java.io.IOException
meth protected void ensureCapacity(int)
meth protected void init()
meth protected void removeEntry(org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>,int,org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>)
meth protected void removeMapping(org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>,int,org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>)
meth protected void reuseEntry(org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>,int,int,{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1})
meth protected void updateEntry(org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>,{org.jivesoftware.smack.util.collections.AbstractHashedMap%1})
meth protected {org.jivesoftware.smack.util.collections.AbstractHashedMap%0} entryKey(org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>)
meth protected {org.jivesoftware.smack.util.collections.AbstractHashedMap%1} entryValue(org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>)
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.String toString()
meth public java.util.Collection<{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}> values()
meth public java.util.Set<java.util.Map$Entry<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>> entrySet()
meth public java.util.Set<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0}> keySet()
meth public org.jivesoftware.smack.util.collections.MapIterator<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}> mapIterator()
meth public void clear()
meth public void putAll(java.util.Map<? extends {org.jivesoftware.smack.util.collections.AbstractHashedMap%0},? extends {org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>)
meth public {org.jivesoftware.smack.util.collections.AbstractHashedMap%1} get(java.lang.Object)
meth public {org.jivesoftware.smack.util.collections.AbstractHashedMap%1} put({org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1})
meth public {org.jivesoftware.smack.util.collections.AbstractHashedMap%1} remove(java.lang.Object)
supr java.util.AbstractMap<{org.jivesoftware.smack.util.collections.AbstractHashedMap%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap%1}>

CLSS protected static org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySet<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.jivesoftware.smack.util.collections.AbstractHashedMap
cons protected init(org.jivesoftware.smack.util.collections.AbstractHashedMap<{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySet%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySet%1}>)
fld protected final org.jivesoftware.smack.util.collections.AbstractHashedMap<{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySet%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySet%1}> parent
meth public boolean contains(java.util.Map$Entry<{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySet%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySet%1}>)
meth public boolean remove(java.lang.Object)
meth public int size()
meth public java.util.Iterator<java.util.Map$Entry<{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySet%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySet%1}>> iterator()
meth public void clear()
supr java.util.AbstractSet<java.util.Map$Entry<{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySet%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySet%1}>>

CLSS protected static org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySetIterator<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.jivesoftware.smack.util.collections.AbstractHashedMap
cons protected init(org.jivesoftware.smack.util.collections.AbstractHashedMap<{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySetIterator%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySetIterator%1}>)
intf java.util.Iterator<java.util.Map$Entry<{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySetIterator%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySetIterator%1}>>
meth public org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySetIterator%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySetIterator%1}> next()
supr org.jivesoftware.smack.util.collections.AbstractHashedMap$HashIterator<{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySetIterator%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$EntrySetIterator%1}>

CLSS protected static org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.jivesoftware.smack.util.collections.AbstractHashedMap
cons protected init(org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry%1}>,int,{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry%1})
fld protected int hashCode
fld protected org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry%1}> next
intf java.util.Map$Entry<{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry%1}>
intf org.jivesoftware.smack.util.collections.KeyValue<{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry%1}>
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public void setKey({org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry%0})
meth public {org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry%0} getKey()
meth public {org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry%1} getValue()
meth public {org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry%1} setValue({org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry%1})
supr java.lang.Object
hfds key,value

CLSS protected abstract static org.jivesoftware.smack.util.collections.AbstractHashedMap$HashIterator<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.jivesoftware.smack.util.collections.AbstractHashedMap
cons protected init(org.jivesoftware.smack.util.collections.AbstractHashedMap<{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashIterator%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashIterator%1}>)
fld protected final org.jivesoftware.smack.util.collections.AbstractHashedMap parent
fld protected int expectedModCount
fld protected int hashIndex
fld protected org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashIterator%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashIterator%1}> last
fld protected org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashIterator%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashIterator%1}> next
meth protected org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashIterator%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashIterator%1}> currentEntry()
meth protected org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashIterator%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashIterator%1}> nextEntry()
meth public boolean hasNext()
meth public java.lang.String toString()
meth public void remove()
supr java.lang.Object

CLSS protected static org.jivesoftware.smack.util.collections.AbstractHashedMap$HashMapIterator<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.jivesoftware.smack.util.collections.AbstractHashedMap
cons protected init(org.jivesoftware.smack.util.collections.AbstractHashedMap<{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashMapIterator%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashMapIterator%1}>)
intf org.jivesoftware.smack.util.collections.MapIterator<{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashMapIterator%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashMapIterator%1}>
meth public {org.jivesoftware.smack.util.collections.AbstractHashedMap$HashMapIterator%0} getKey()
meth public {org.jivesoftware.smack.util.collections.AbstractHashedMap$HashMapIterator%0} next()
meth public {org.jivesoftware.smack.util.collections.AbstractHashedMap$HashMapIterator%1} getValue()
meth public {org.jivesoftware.smack.util.collections.AbstractHashedMap$HashMapIterator%1} setValue({org.jivesoftware.smack.util.collections.AbstractHashedMap$HashMapIterator%1})
supr org.jivesoftware.smack.util.collections.AbstractHashedMap$HashIterator<{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashMapIterator%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$HashMapIterator%1}>

CLSS protected static org.jivesoftware.smack.util.collections.AbstractHashedMap$KeySet<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.jivesoftware.smack.util.collections.AbstractHashedMap
cons protected init(org.jivesoftware.smack.util.collections.AbstractHashedMap<{org.jivesoftware.smack.util.collections.AbstractHashedMap$KeySet%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$KeySet%1}>)
fld protected final org.jivesoftware.smack.util.collections.AbstractHashedMap<{org.jivesoftware.smack.util.collections.AbstractHashedMap$KeySet%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$KeySet%1}> parent
meth public boolean contains(java.lang.Object)
meth public boolean remove(java.lang.Object)
meth public int size()
meth public java.util.Iterator<{org.jivesoftware.smack.util.collections.AbstractHashedMap$KeySet%0}> iterator()
meth public void clear()
supr java.util.AbstractSet<{org.jivesoftware.smack.util.collections.AbstractHashedMap$KeySet%0}>

CLSS protected static org.jivesoftware.smack.util.collections.AbstractHashedMap$KeySetIterator<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.jivesoftware.smack.util.collections.AbstractHashedMap
cons protected init(org.jivesoftware.smack.util.collections.AbstractHashedMap<{org.jivesoftware.smack.util.collections.AbstractHashedMap$KeySetIterator%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$KeySetIterator%1}>)
intf java.util.Iterator<{org.jivesoftware.smack.util.collections.AbstractHashedMap$KeySetIterator%0}>
meth public {org.jivesoftware.smack.util.collections.AbstractHashedMap$KeySetIterator%0} next()
supr org.jivesoftware.smack.util.collections.AbstractHashedMap$HashIterator<{org.jivesoftware.smack.util.collections.AbstractHashedMap$KeySetIterator%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$KeySetIterator%1}>

CLSS protected static org.jivesoftware.smack.util.collections.AbstractHashedMap$Values<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.jivesoftware.smack.util.collections.AbstractHashedMap
cons protected init(org.jivesoftware.smack.util.collections.AbstractHashedMap<{org.jivesoftware.smack.util.collections.AbstractHashedMap$Values%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$Values%1}>)
fld protected final org.jivesoftware.smack.util.collections.AbstractHashedMap<{org.jivesoftware.smack.util.collections.AbstractHashedMap$Values%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$Values%1}> parent
meth public boolean contains(java.lang.Object)
meth public int size()
meth public java.util.Iterator<{org.jivesoftware.smack.util.collections.AbstractHashedMap$Values%1}> iterator()
meth public void clear()
supr java.util.AbstractCollection<{org.jivesoftware.smack.util.collections.AbstractHashedMap$Values%1}>

CLSS protected static org.jivesoftware.smack.util.collections.AbstractHashedMap$ValuesIterator<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.jivesoftware.smack.util.collections.AbstractHashedMap
cons protected init(org.jivesoftware.smack.util.collections.AbstractHashedMap<{org.jivesoftware.smack.util.collections.AbstractHashedMap$ValuesIterator%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$ValuesIterator%1}>)
intf java.util.Iterator<{org.jivesoftware.smack.util.collections.AbstractHashedMap$ValuesIterator%1}>
meth public {org.jivesoftware.smack.util.collections.AbstractHashedMap$ValuesIterator%1} next()
supr org.jivesoftware.smack.util.collections.AbstractHashedMap$HashIterator<{org.jivesoftware.smack.util.collections.AbstractHashedMap$ValuesIterator%0},{org.jivesoftware.smack.util.collections.AbstractHashedMap$ValuesIterator%1}>

CLSS public abstract org.jivesoftware.smack.util.collections.AbstractKeyValue<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init({org.jivesoftware.smack.util.collections.AbstractKeyValue%0},{org.jivesoftware.smack.util.collections.AbstractKeyValue%1})
fld protected {org.jivesoftware.smack.util.collections.AbstractKeyValue%0} key
fld protected {org.jivesoftware.smack.util.collections.AbstractKeyValue%1} value
intf org.jivesoftware.smack.util.collections.KeyValue<{org.jivesoftware.smack.util.collections.AbstractKeyValue%0},{org.jivesoftware.smack.util.collections.AbstractKeyValue%1}>
meth public java.lang.String toString()
meth public {org.jivesoftware.smack.util.collections.AbstractKeyValue%0} getKey()
meth public {org.jivesoftware.smack.util.collections.AbstractKeyValue%1} getValue()
supr java.lang.Object

CLSS public abstract org.jivesoftware.smack.util.collections.AbstractMapEntry<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init({org.jivesoftware.smack.util.collections.AbstractMapEntry%0},{org.jivesoftware.smack.util.collections.AbstractMapEntry%1})
intf java.util.Map$Entry<{org.jivesoftware.smack.util.collections.AbstractMapEntry%0},{org.jivesoftware.smack.util.collections.AbstractMapEntry%1}>
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public {org.jivesoftware.smack.util.collections.AbstractMapEntry%1} setValue({org.jivesoftware.smack.util.collections.AbstractMapEntry%1})
supr org.jivesoftware.smack.util.collections.AbstractKeyValue<{org.jivesoftware.smack.util.collections.AbstractMapEntry%0},{org.jivesoftware.smack.util.collections.AbstractMapEntry%1}>

CLSS public abstract org.jivesoftware.smack.util.collections.AbstractReferenceMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init()
cons protected init(int,int,int,float,boolean)
fld protected boolean purgeValues
fld protected int keyType
fld protected int valueType
fld public final static int HARD = 0
fld public final static int SOFT = 1
fld public final static int WEAK = 2
innr protected static ReferenceEntry
meth protected boolean isEqualKey(java.lang.Object,java.lang.Object)
meth protected int hashEntry(java.lang.Object,java.lang.Object)
meth protected java.util.Iterator<java.util.Map$Entry<{org.jivesoftware.smack.util.collections.AbstractReferenceMap%0},{org.jivesoftware.smack.util.collections.AbstractReferenceMap%1}>> createEntrySetIterator()
meth protected java.util.Iterator<{org.jivesoftware.smack.util.collections.AbstractReferenceMap%0}> createKeySetIterator()
meth protected java.util.Iterator<{org.jivesoftware.smack.util.collections.AbstractReferenceMap%1}> createValuesIterator()
meth protected org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractReferenceMap%0},{org.jivesoftware.smack.util.collections.AbstractReferenceMap%1}> getEntry(java.lang.Object)
meth protected void doReadObject(java.io.ObjectInputStream) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected void doWriteObject(java.io.ObjectOutputStream) throws java.io.IOException
meth protected void init()
meth protected void purge()
meth protected void purge(java.lang.ref.Reference)
meth protected void purgeBeforeRead()
meth protected void purgeBeforeWrite()
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean isEmpty()
meth public int size()
meth public java.util.Collection<{org.jivesoftware.smack.util.collections.AbstractReferenceMap%1}> values()
meth public java.util.Set<java.util.Map$Entry<{org.jivesoftware.smack.util.collections.AbstractReferenceMap%0},{org.jivesoftware.smack.util.collections.AbstractReferenceMap%1}>> entrySet()
meth public java.util.Set<{org.jivesoftware.smack.util.collections.AbstractReferenceMap%0}> keySet()
meth public org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractReferenceMap%0},{org.jivesoftware.smack.util.collections.AbstractReferenceMap%1}> createEntry(org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractReferenceMap%0},{org.jivesoftware.smack.util.collections.AbstractReferenceMap%1}>,int,{org.jivesoftware.smack.util.collections.AbstractReferenceMap%0},{org.jivesoftware.smack.util.collections.AbstractReferenceMap%1})
meth public org.jivesoftware.smack.util.collections.MapIterator<{org.jivesoftware.smack.util.collections.AbstractReferenceMap%0},{org.jivesoftware.smack.util.collections.AbstractReferenceMap%1}> mapIterator()
meth public void clear()
meth public {org.jivesoftware.smack.util.collections.AbstractReferenceMap%1} get(java.lang.Object)
meth public {org.jivesoftware.smack.util.collections.AbstractReferenceMap%1} put({org.jivesoftware.smack.util.collections.AbstractReferenceMap%0},{org.jivesoftware.smack.util.collections.AbstractReferenceMap%1})
meth public {org.jivesoftware.smack.util.collections.AbstractReferenceMap%1} remove(java.lang.Object)
supr org.jivesoftware.smack.util.collections.AbstractHashedMap<{org.jivesoftware.smack.util.collections.AbstractReferenceMap%0},{org.jivesoftware.smack.util.collections.AbstractReferenceMap%1}>
hfds queue
hcls ReferenceEntrySet,ReferenceEntrySetIterator,ReferenceIteratorBase,ReferenceKeySet,ReferenceKeySetIterator,ReferenceMapIterator,ReferenceValues,ReferenceValuesIterator,SoftRef,WeakRef

CLSS protected static org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.jivesoftware.smack.util.collections.AbstractReferenceMap
cons public init(org.jivesoftware.smack.util.collections.AbstractReferenceMap<{org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%0},{org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%1}>,org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry<{org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%0},{org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%1}>,int,{org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%0},{org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%1})
fld protected final org.jivesoftware.smack.util.collections.AbstractReferenceMap<{org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%0},{org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%1}> parent
fld protected java.lang.ref.Reference<{org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%0}> refKey
fld protected java.lang.ref.Reference<{org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%1}> refValue
meth protected <%0 extends java.lang.Object> java.lang.ref.Reference<{%%0}> toReference(int,{%%0},int)
meth protected org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry<{org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%0},{org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%1}> next()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public {org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%0} getKey()
meth public {org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%1} getValue()
meth public {org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%1} setValue({org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%1})
supr org.jivesoftware.smack.util.collections.AbstractHashedMap$HashEntry<{org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%0},{org.jivesoftware.smack.util.collections.AbstractReferenceMap$ReferenceEntry%1}>

CLSS public final org.jivesoftware.smack.util.collections.DefaultMapEntry<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(java.util.Map$Entry<{org.jivesoftware.smack.util.collections.DefaultMapEntry%0},{org.jivesoftware.smack.util.collections.DefaultMapEntry%1}>)
cons public init(org.jivesoftware.smack.util.collections.KeyValue<{org.jivesoftware.smack.util.collections.DefaultMapEntry%0},{org.jivesoftware.smack.util.collections.DefaultMapEntry%1}>)
cons public init({org.jivesoftware.smack.util.collections.DefaultMapEntry%0},{org.jivesoftware.smack.util.collections.DefaultMapEntry%1})
supr org.jivesoftware.smack.util.collections.AbstractMapEntry<{org.jivesoftware.smack.util.collections.DefaultMapEntry%0},{org.jivesoftware.smack.util.collections.DefaultMapEntry%1}>

CLSS public org.jivesoftware.smack.util.collections.EmptyIterator<%0 extends java.lang.Object>
cons protected init()
fld public final static java.util.Iterator INSTANCE
fld public final static org.jivesoftware.smack.util.collections.ResettableIterator RESETTABLE_INSTANCE
intf org.jivesoftware.smack.util.collections.ResettableIterator<{org.jivesoftware.smack.util.collections.EmptyIterator%0}>
meth public boolean hasNext()
meth public boolean hasPrevious()
meth public int nextIndex()
meth public int previousIndex()
meth public static <%0 extends java.lang.Object> java.util.Iterator<{%%0}> getInstance()
meth public void add({org.jivesoftware.smack.util.collections.EmptyIterator%0})
meth public void remove()
meth public void reset()
meth public void set({org.jivesoftware.smack.util.collections.EmptyIterator%0})
meth public {org.jivesoftware.smack.util.collections.EmptyIterator%0} getKey()
meth public {org.jivesoftware.smack.util.collections.EmptyIterator%0} getValue()
meth public {org.jivesoftware.smack.util.collections.EmptyIterator%0} next()
meth public {org.jivesoftware.smack.util.collections.EmptyIterator%0} previous()
meth public {org.jivesoftware.smack.util.collections.EmptyIterator%0} setValue({org.jivesoftware.smack.util.collections.EmptyIterator%0})
supr java.lang.Object<{org.jivesoftware.smack.util.collections.EmptyIterator%0}>

CLSS public org.jivesoftware.smack.util.collections.EmptyMapIterator
cons protected init()
fld public final static org.jivesoftware.smack.util.collections.MapIterator INSTANCE
intf org.jivesoftware.smack.util.collections.MapIterator
intf org.jivesoftware.smack.util.collections.ResettableIterator
meth public boolean hasNext()
meth public boolean hasPrevious()
meth public int nextIndex()
meth public int previousIndex()
meth public java.lang.Object getKey()
meth public java.lang.Object getValue()
meth public java.lang.Object next()
meth public java.lang.Object previous()
meth public java.lang.Object setValue(java.lang.Object)
meth public void add(java.lang.Object)
meth public void remove()
meth public void reset()
meth public void set(java.lang.Object)
supr java.lang.Object

CLSS public abstract interface org.jivesoftware.smack.util.collections.IterableMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf java.util.Map<{org.jivesoftware.smack.util.collections.IterableMap%0},{org.jivesoftware.smack.util.collections.IterableMap%1}>
meth public abstract org.jivesoftware.smack.util.collections.MapIterator<{org.jivesoftware.smack.util.collections.IterableMap%0},{org.jivesoftware.smack.util.collections.IterableMap%1}> mapIterator()

CLSS public abstract interface org.jivesoftware.smack.util.collections.KeyValue<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public abstract {org.jivesoftware.smack.util.collections.KeyValue%0} getKey()
meth public abstract {org.jivesoftware.smack.util.collections.KeyValue%1} getValue()

CLSS public abstract interface org.jivesoftware.smack.util.collections.MapIterator<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf java.util.Iterator<{org.jivesoftware.smack.util.collections.MapIterator%0}>
meth public abstract boolean hasNext()
meth public abstract void remove()
meth public abstract {org.jivesoftware.smack.util.collections.MapIterator%0} getKey()
meth public abstract {org.jivesoftware.smack.util.collections.MapIterator%0} next()
meth public abstract {org.jivesoftware.smack.util.collections.MapIterator%1} getValue()
meth public abstract {org.jivesoftware.smack.util.collections.MapIterator%1} setValue({org.jivesoftware.smack.util.collections.MapIterator%1})

CLSS public org.jivesoftware.smack.util.collections.ReferenceMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(int,int)
cons public init(int,int,boolean)
cons public init(int,int,int,float)
cons public init(int,int,int,float,boolean)
intf java.io.Serializable
supr org.jivesoftware.smack.util.collections.AbstractReferenceMap<{org.jivesoftware.smack.util.collections.ReferenceMap%0},{org.jivesoftware.smack.util.collections.ReferenceMap%1}>
hfds serialVersionUID

CLSS public abstract interface org.jivesoftware.smack.util.collections.ResettableIterator<%0 extends java.lang.Object>
intf java.util.Iterator<{org.jivesoftware.smack.util.collections.ResettableIterator%0}>
meth public abstract void reset()

CLSS public final !enum org.jivesoftware.smackx.ChatState
fld public final static org.jivesoftware.smackx.ChatState active
fld public final static org.jivesoftware.smackx.ChatState composing
fld public final static org.jivesoftware.smackx.ChatState gone
fld public final static org.jivesoftware.smackx.ChatState inactive
fld public final static org.jivesoftware.smackx.ChatState paused
meth public final static org.jivesoftware.smackx.ChatState[] values()
meth public static org.jivesoftware.smackx.ChatState valueOf(java.lang.String)
supr java.lang.Enum<org.jivesoftware.smackx.ChatState>

CLSS public abstract interface org.jivesoftware.smackx.ChatStateListener
intf org.jivesoftware.smack.MessageListener
meth public abstract void stateChanged(org.jivesoftware.smack.Chat,org.jivesoftware.smackx.ChatState)

CLSS public org.jivesoftware.smackx.ChatStateManager
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public static org.jivesoftware.smackx.ChatStateManager getInstance(org.jivesoftware.smack.XMPPConnection)
meth public void setCurrentState(org.jivesoftware.smackx.ChatState,org.jivesoftware.smack.Chat) throws org.jivesoftware.smack.XMPPException
supr java.lang.Object
hfds chatStates,connection,filter,incomingInterceptor,managers,outgoingInterceptor
hcls IncomingMessageInterceptor,OutgoingMessageInterceptor

CLSS public org.jivesoftware.smackx.DefaultMessageEventRequestListener
cons public init()
intf org.jivesoftware.smackx.MessageEventRequestListener
meth public void composingNotificationRequested(java.lang.String,java.lang.String,org.jivesoftware.smackx.MessageEventManager)
meth public void deliveredNotificationRequested(java.lang.String,java.lang.String,org.jivesoftware.smackx.MessageEventManager)
meth public void displayedNotificationRequested(java.lang.String,java.lang.String,org.jivesoftware.smackx.MessageEventManager)
meth public void offlineNotificationRequested(java.lang.String,java.lang.String,org.jivesoftware.smackx.MessageEventManager)
supr java.lang.Object

CLSS public org.jivesoftware.smackx.Form
cons public init(java.lang.String)
cons public init(org.jivesoftware.smackx.packet.DataForm)
fld public final static java.lang.String TYPE_CANCEL = "cancel"
fld public final static java.lang.String TYPE_FORM = "form"
fld public final static java.lang.String TYPE_RESULT = "result"
fld public final static java.lang.String TYPE_SUBMIT = "submit"
meth public java.lang.String getInstructions()
meth public java.lang.String getTitle()
meth public java.lang.String getType()
meth public java.util.Iterator<org.jivesoftware.smackx.FormField> getFields()
meth public org.jivesoftware.smackx.Form createAnswerForm()
meth public org.jivesoftware.smackx.FormField getField(java.lang.String)
meth public org.jivesoftware.smackx.packet.DataForm getDataFormToSend()
meth public static org.jivesoftware.smackx.Form getFormFrom(org.jivesoftware.smack.packet.Packet)
meth public void addField(org.jivesoftware.smackx.FormField)
meth public void setAnswer(java.lang.String,boolean)
meth public void setAnswer(java.lang.String,double)
meth public void setAnswer(java.lang.String,float)
meth public void setAnswer(java.lang.String,int)
meth public void setAnswer(java.lang.String,java.lang.String)
meth public void setAnswer(java.lang.String,java.util.List<java.lang.String>)
meth public void setAnswer(java.lang.String,long)
meth public void setDefaultAnswer(java.lang.String)
meth public void setInstructions(java.lang.String)
meth public void setTitle(java.lang.String)
supr java.lang.Object
hfds dataForm

CLSS public org.jivesoftware.smackx.FormField
cons public init()
cons public init(java.lang.String)
fld public final static java.lang.String TYPE_BOOLEAN = "boolean"
fld public final static java.lang.String TYPE_FIXED = "fixed"
fld public final static java.lang.String TYPE_HIDDEN = "hidden"
fld public final static java.lang.String TYPE_JID_MULTI = "jid-multi"
fld public final static java.lang.String TYPE_JID_SINGLE = "jid-single"
fld public final static java.lang.String TYPE_LIST_MULTI = "list-multi"
fld public final static java.lang.String TYPE_LIST_SINGLE = "list-single"
fld public final static java.lang.String TYPE_TEXT_MULTI = "text-multi"
fld public final static java.lang.String TYPE_TEXT_PRIVATE = "text-private"
fld public final static java.lang.String TYPE_TEXT_SINGLE = "text-single"
innr public static Option
meth protected void resetValues()
meth public boolean isRequired()
meth public java.lang.String getDescription()
meth public java.lang.String getLabel()
meth public java.lang.String getType()
meth public java.lang.String getVariable()
meth public java.lang.String toXML()
meth public java.util.Iterator<java.lang.String> getValues()
meth public java.util.Iterator<org.jivesoftware.smackx.FormField$Option> getOptions()
meth public void addOption(org.jivesoftware.smackx.FormField$Option)
meth public void addValue(java.lang.String)
meth public void addValues(java.util.List<java.lang.String>)
meth public void setDescription(java.lang.String)
meth public void setLabel(java.lang.String)
meth public void setRequired(boolean)
meth public void setType(java.lang.String)
supr java.lang.Object
hfds description,label,options,required,type,values,variable

CLSS public static org.jivesoftware.smackx.FormField$Option
 outer org.jivesoftware.smackx.FormField
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getLabel()
meth public java.lang.String getValue()
meth public java.lang.String toString()
meth public java.lang.String toXML()
supr java.lang.Object
hfds label,value

CLSS public org.jivesoftware.smackx.GroupChatInvitation
cons public init(java.lang.String)
fld public final static java.lang.String ELEMENT_NAME = "x"
fld public final static java.lang.String NAMESPACE = "jabber:x:conference"
innr public static Provider
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getRoomAddress()
meth public java.lang.String toXML()
supr java.lang.Object
hfds roomAddress

CLSS public static org.jivesoftware.smackx.GroupChatInvitation$Provider
 outer org.jivesoftware.smackx.GroupChatInvitation
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.LastActivityManager
meth public static org.jivesoftware.smackx.packet.LastActivity getLastActivity(org.jivesoftware.smack.XMPPConnection,java.lang.String) throws org.jivesoftware.smack.XMPPException
supr java.lang.Object
hfds connection,lastMessageSent

CLSS public org.jivesoftware.smackx.MessageEventManager
cons public init(org.jivesoftware.smack.XMPPConnection)
meth public static void addNotificationsRequests(org.jivesoftware.smack.packet.Message,boolean,boolean,boolean,boolean)
meth public void addMessageEventNotificationListener(org.jivesoftware.smackx.MessageEventNotificationListener)
meth public void addMessageEventRequestListener(org.jivesoftware.smackx.MessageEventRequestListener)
meth public void destroy()
meth public void finalize()
meth public void removeMessageEventNotificationListener(org.jivesoftware.smackx.MessageEventNotificationListener)
meth public void removeMessageEventRequestListener(org.jivesoftware.smackx.MessageEventRequestListener)
meth public void sendCancelledNotification(java.lang.String,java.lang.String)
meth public void sendComposingNotification(java.lang.String,java.lang.String)
meth public void sendDeliveredNotification(java.lang.String,java.lang.String)
meth public void sendDisplayedNotification(java.lang.String,java.lang.String)
supr java.lang.Object
hfds con,messageEventNotificationListeners,messageEventRequestListeners,packetFilter,packetListener

CLSS public abstract interface org.jivesoftware.smackx.MessageEventNotificationListener
meth public abstract void cancelledNotification(java.lang.String,java.lang.String)
meth public abstract void composingNotification(java.lang.String,java.lang.String)
meth public abstract void deliveredNotification(java.lang.String,java.lang.String)
meth public abstract void displayedNotification(java.lang.String,java.lang.String)
meth public abstract void offlineNotification(java.lang.String,java.lang.String)

CLSS public abstract interface org.jivesoftware.smackx.MessageEventRequestListener
meth public abstract void composingNotificationRequested(java.lang.String,java.lang.String,org.jivesoftware.smackx.MessageEventManager)
meth public abstract void deliveredNotificationRequested(java.lang.String,java.lang.String,org.jivesoftware.smackx.MessageEventManager)
meth public abstract void displayedNotificationRequested(java.lang.String,java.lang.String,org.jivesoftware.smackx.MessageEventManager)
meth public abstract void offlineNotificationRequested(java.lang.String,java.lang.String,org.jivesoftware.smackx.MessageEventManager)

CLSS public org.jivesoftware.smackx.MultipleRecipientInfo
meth public boolean shouldNotReply()
meth public java.lang.String getReplyRoom()
meth public java.util.List getCCAddresses()
meth public java.util.List getTOAddresses()
meth public org.jivesoftware.smackx.packet.MultipleAddresses$Address getReplyAddress()
supr java.lang.Object
hfds extension

CLSS public org.jivesoftware.smackx.MultipleRecipientManager
cons public init()
meth public static org.jivesoftware.smackx.MultipleRecipientInfo getMultipleRecipientInfo(org.jivesoftware.smack.packet.Packet)
meth public static void reply(org.jivesoftware.smack.XMPPConnection,org.jivesoftware.smack.packet.Message,org.jivesoftware.smack.packet.Message) throws org.jivesoftware.smack.XMPPException
meth public static void send(org.jivesoftware.smack.XMPPConnection,org.jivesoftware.smack.packet.Packet,java.util.List,java.util.List,java.util.List) throws org.jivesoftware.smack.XMPPException
meth public static void send(org.jivesoftware.smack.XMPPConnection,org.jivesoftware.smack.packet.Packet,java.util.List,java.util.List,java.util.List,java.lang.String,java.lang.String,boolean) throws org.jivesoftware.smack.XMPPException
supr java.lang.Object
hfds services
hcls PacketCopy

CLSS public abstract interface org.jivesoftware.smackx.NodeInformationProvider
meth public abstract java.util.List<java.lang.String> getNodeFeatures()
meth public abstract java.util.List<org.jivesoftware.smackx.packet.DiscoverInfo$Identity> getNodeIdentities()
meth public abstract java.util.List<org.jivesoftware.smackx.packet.DiscoverItems$Item> getNodeItems()

CLSS public org.jivesoftware.smackx.OfflineMessageHeader
cons public init(org.jivesoftware.smackx.packet.DiscoverItems$Item)
meth public java.lang.String getJid()
meth public java.lang.String getStamp()
meth public java.lang.String getUser()
supr java.lang.Object
hfds jid,stamp,user

CLSS public org.jivesoftware.smackx.OfflineMessageManager
cons public init(org.jivesoftware.smack.XMPPConnection)
meth public boolean supportsFlexibleRetrieval() throws org.jivesoftware.smack.XMPPException
meth public int getMessageCount() throws org.jivesoftware.smack.XMPPException
meth public java.util.Iterator<org.jivesoftware.smack.packet.Message> getMessages() throws org.jivesoftware.smack.XMPPException
meth public java.util.Iterator<org.jivesoftware.smack.packet.Message> getMessages(java.util.List<java.lang.String>) throws org.jivesoftware.smack.XMPPException
meth public java.util.Iterator<org.jivesoftware.smackx.OfflineMessageHeader> getHeaders() throws org.jivesoftware.smack.XMPPException
meth public void deleteMessages() throws org.jivesoftware.smack.XMPPException
meth public void deleteMessages(java.util.List<java.lang.String>) throws org.jivesoftware.smack.XMPPException
supr java.lang.Object
hfds connection,namespace,packetFilter

CLSS public abstract interface org.jivesoftware.smackx.PEPListener
meth public abstract void eventReceived(java.lang.String,org.jivesoftware.smackx.packet.PEPEvent)

CLSS public org.jivesoftware.smackx.PEPManager
cons public init(org.jivesoftware.smack.XMPPConnection)
meth public void addPEPListener(org.jivesoftware.smackx.PEPListener)
meth public void destroy()
meth public void finalize()
meth public void publish(org.jivesoftware.smackx.packet.PEPItem)
meth public void removePEPListener(org.jivesoftware.smackx.PEPListener)
supr java.lang.Object
hfds connection,packetFilter,packetListener,pepListeners

CLSS public org.jivesoftware.smackx.PrivateDataManager
cons public init(org.jivesoftware.smack.XMPPConnection)
cons public init(org.jivesoftware.smack.XMPPConnection,java.lang.String)
innr public static PrivateDataIQProvider
meth public org.jivesoftware.smackx.packet.PrivateData getPrivateData(java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public static org.jivesoftware.smackx.provider.PrivateDataProvider getPrivateDataProvider(java.lang.String,java.lang.String)
meth public static void addPrivateDataProvider(java.lang.String,java.lang.String,org.jivesoftware.smackx.provider.PrivateDataProvider)
meth public static void removePrivateDataProvider(java.lang.String,java.lang.String)
meth public void setPrivateData(org.jivesoftware.smackx.packet.PrivateData) throws org.jivesoftware.smack.XMPPException
supr java.lang.Object
hfds connection,privateDataProviders,user
hcls PrivateDataResult

CLSS public static org.jivesoftware.smackx.PrivateDataManager$PrivateDataIQProvider
 outer org.jivesoftware.smackx.PrivateDataManager
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.RemoteRosterEntry
cons public init(java.lang.String,java.lang.String,java.lang.String[])
meth public java.lang.String getName()
meth public java.lang.String getUser()
meth public java.lang.String toXML()
meth public java.lang.String[] getGroupArrayNames()
meth public java.util.Iterator getGroupNames()
supr java.lang.Object
hfds groupNames,name,user

CLSS public org.jivesoftware.smackx.ReportedData
cons public init()
innr public static Column
innr public static Field
innr public static Row
meth public java.lang.String getTitle()
meth public java.util.Iterator<org.jivesoftware.smackx.ReportedData$Column> getColumns()
meth public java.util.Iterator<org.jivesoftware.smackx.ReportedData$Row> getRows()
meth public static org.jivesoftware.smackx.ReportedData getReportedDataFrom(org.jivesoftware.smack.packet.Packet)
meth public void addColumn(org.jivesoftware.smackx.ReportedData$Column)
meth public void addRow(org.jivesoftware.smackx.ReportedData$Row)
supr java.lang.Object
hfds columns,rows,title

CLSS public static org.jivesoftware.smackx.ReportedData$Column
 outer org.jivesoftware.smackx.ReportedData
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getLabel()
meth public java.lang.String getType()
meth public java.lang.String getVariable()
supr java.lang.Object
hfds label,type,variable

CLSS public static org.jivesoftware.smackx.ReportedData$Field
 outer org.jivesoftware.smackx.ReportedData
cons public init(java.lang.String,java.util.List<java.lang.String>)
meth public java.lang.String getVariable()
meth public java.util.Iterator<java.lang.String> getValues()
supr java.lang.Object
hfds values,variable

CLSS public static org.jivesoftware.smackx.ReportedData$Row
 outer org.jivesoftware.smackx.ReportedData
cons public init(java.util.List<org.jivesoftware.smackx.ReportedData$Field>)
meth public java.util.Iterator getValues(java.lang.String)
supr java.lang.Object
hfds fields

CLSS public abstract interface org.jivesoftware.smackx.RosterExchangeListener
meth public abstract void entriesReceived(java.lang.String,java.util.Iterator)

CLSS public org.jivesoftware.smackx.RosterExchangeManager
cons public init(org.jivesoftware.smack.XMPPConnection)
meth public void addRosterListener(org.jivesoftware.smackx.RosterExchangeListener)
meth public void destroy()
meth public void finalize()
meth public void removeRosterListener(org.jivesoftware.smackx.RosterExchangeListener)
meth public void send(org.jivesoftware.smack.Roster,java.lang.String)
meth public void send(org.jivesoftware.smack.RosterEntry,java.lang.String)
meth public void send(org.jivesoftware.smack.RosterGroup,java.lang.String)
supr java.lang.Object
hfds con,packetFilter,packetListener,rosterExchangeListeners

CLSS public org.jivesoftware.smackx.ServiceDiscoveryManager
cons public init(org.jivesoftware.smack.XMPPConnection)
meth public boolean canPublishItems(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public boolean includesFeature(java.lang.String)
meth public java.util.Iterator<java.lang.String> getFeatures()
meth public org.jivesoftware.smackx.packet.DiscoverInfo discoverInfo(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.packet.DiscoverInfo discoverInfo(java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.packet.DiscoverItems discoverItems(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.packet.DiscoverItems discoverItems(java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public static java.lang.String getIdentityName()
meth public static java.lang.String getIdentityType()
meth public static org.jivesoftware.smackx.ServiceDiscoveryManager getInstanceFor(org.jivesoftware.smack.XMPPConnection)
meth public static void setIdentityName(java.lang.String)
meth public static void setIdentityType(java.lang.String)
meth public void addFeature(java.lang.String)
meth public void publishItems(java.lang.String,java.lang.String,org.jivesoftware.smackx.packet.DiscoverItems) throws org.jivesoftware.smack.XMPPException
meth public void publishItems(java.lang.String,org.jivesoftware.smackx.packet.DiscoverItems) throws org.jivesoftware.smack.XMPPException
meth public void removeExtendedInfo()
meth public void removeFeature(java.lang.String)
meth public void removeNodeInformationProvider(java.lang.String)
meth public void setExtendedInfo(org.jivesoftware.smackx.packet.DataForm)
meth public void setNodeInformationProvider(java.lang.String,org.jivesoftware.smackx.NodeInformationProvider)
supr java.lang.Object
hfds connection,extendedInfo,features,identityName,identityType,instances,nodeInformationProviders

CLSS public org.jivesoftware.smackx.SharedGroupManager
cons public init()
meth public static java.util.List getSharedGroups(org.jivesoftware.smack.XMPPConnection) throws org.jivesoftware.smack.XMPPException
supr java.lang.Object

CLSS public org.jivesoftware.smackx.XHTMLManager
cons public init()
meth public static boolean isServiceEnabled(org.jivesoftware.smack.XMPPConnection)
meth public static boolean isServiceEnabled(org.jivesoftware.smack.XMPPConnection,java.lang.String)
meth public static boolean isXHTMLMessage(org.jivesoftware.smack.packet.Message)
meth public static java.util.Iterator getBodies(org.jivesoftware.smack.packet.Message)
meth public static void addBody(org.jivesoftware.smack.packet.Message,java.lang.String)
meth public static void setServiceEnabled(org.jivesoftware.smack.XMPPConnection,boolean)
supr java.lang.Object
hfds namespace

CLSS public org.jivesoftware.smackx.XHTMLText
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String toString()
meth public void append(java.lang.String)
meth public void appendBrTag()
meth public void appendCloseAnchorTag()
meth public void appendCloseBlockQuoteTag()
meth public void appendCloseCodeTag()
meth public void appendCloseEmTag()
meth public void appendCloseHeaderTag(int)
meth public void appendCloseInlinedQuoteTag()
meth public void appendCloseOrderedListTag()
meth public void appendCloseParagraphTag()
meth public void appendCloseSpanTag()
meth public void appendCloseStrongTag()
meth public void appendCloseUnorderedListTag()
meth public void appendImageTag(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public void appendLineItemTag(java.lang.String)
meth public void appendOpenAnchorTag(java.lang.String,java.lang.String)
meth public void appendOpenBlockQuoteTag(java.lang.String)
meth public void appendOpenCiteTag()
meth public void appendOpenCodeTag()
meth public void appendOpenEmTag()
meth public void appendOpenHeaderTag(int,java.lang.String)
meth public void appendOpenInlinedQuoteTag(java.lang.String)
meth public void appendOpenOrderedListTag(java.lang.String)
meth public void appendOpenParagraphTag(java.lang.String)
meth public void appendOpenSpanTag(java.lang.String)
meth public void appendOpenStrongTag()
meth public void appendOpenUnorderedListTag(java.lang.String)
supr java.lang.Object
hfds text

CLSS public org.jivesoftware.smackx.bookmark.BookmarkManager
meth public java.util.Collection getBookmarkedConferences() throws org.jivesoftware.smack.XMPPException
meth public java.util.Collection getBookmarkedURLs() throws org.jivesoftware.smack.XMPPException
meth public static org.jivesoftware.smackx.bookmark.BookmarkManager getBookmarkManager(org.jivesoftware.smack.XMPPConnection) throws org.jivesoftware.smack.XMPPException
meth public void addBookmarkedConference(java.lang.String,java.lang.String,boolean,java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void addBookmarkedURL(java.lang.String,java.lang.String,boolean) throws org.jivesoftware.smack.XMPPException
meth public void removeBookmarkedConference(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void removeBookmarkedURL(java.lang.String) throws org.jivesoftware.smack.XMPPException
supr java.lang.Object
hfds bookmarkLock,bookmarkManagerMap,bookmarks,privateDataManager

CLSS public org.jivesoftware.smackx.bookmark.BookmarkedConference
cons protected init(java.lang.String)
cons protected init(java.lang.String,java.lang.String,boolean,java.lang.String,java.lang.String)
intf org.jivesoftware.smackx.bookmark.SharedBookmark
meth protected void setAutoJoin(boolean)
meth protected void setName(java.lang.String)
meth protected void setNickname(java.lang.String)
meth protected void setPassword(java.lang.String)
meth protected void setShared(boolean)
meth public boolean equals(java.lang.Object)
meth public boolean isAutoJoin()
meth public boolean isShared()
meth public java.lang.String getJid()
meth public java.lang.String getName()
meth public java.lang.String getNickname()
meth public java.lang.String getPassword()
supr java.lang.Object
hfds autoJoin,isShared,jid,name,nickname,password

CLSS public org.jivesoftware.smackx.bookmark.BookmarkedURL
cons protected init(java.lang.String)
cons protected init(java.lang.String,java.lang.String,boolean)
intf org.jivesoftware.smackx.bookmark.SharedBookmark
meth protected void setName(java.lang.String)
meth protected void setRss(boolean)
meth protected void setShared(boolean)
meth public boolean equals(java.lang.Object)
meth public boolean isRss()
meth public boolean isShared()
meth public java.lang.String getName()
meth public java.lang.String getURL()
supr java.lang.Object
hfds URL,isRss,isShared,name

CLSS public org.jivesoftware.smackx.bookmark.Bookmarks
cons public init()
innr public static Provider
intf org.jivesoftware.smackx.packet.PrivateData
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String toXML()
meth public java.util.List getBookmarkedConferences()
meth public java.util.List getBookmarkedURLS()
meth public void addBookmarkedConference(org.jivesoftware.smackx.bookmark.BookmarkedConference)
meth public void addBookmarkedURL(org.jivesoftware.smackx.bookmark.BookmarkedURL)
meth public void clearBookmarkedConferences()
meth public void clearBookmarkedURLS()
meth public void removeBookmarkedConference(org.jivesoftware.smackx.bookmark.BookmarkedConference)
meth public void removeBookmarkedURL(org.jivesoftware.smackx.bookmark.BookmarkedURL)
supr java.lang.Object
hfds bookmarkedConferences,bookmarkedURLS

CLSS public static org.jivesoftware.smackx.bookmark.Bookmarks$Provider
 outer org.jivesoftware.smackx.bookmark.Bookmarks
cons public init()
intf org.jivesoftware.smackx.provider.PrivateDataProvider
meth public org.jivesoftware.smackx.packet.PrivateData parsePrivateData(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public abstract interface org.jivesoftware.smackx.bookmark.SharedBookmark
meth public abstract boolean isShared()

CLSS public org.jivesoftware.smackx.filetransfer.FaultTolerantNegotiator
cons public init(org.jivesoftware.smack.XMPPConnection,org.jivesoftware.smackx.filetransfer.StreamNegotiator,org.jivesoftware.smackx.filetransfer.StreamNegotiator)
meth public java.io.InputStream createIncomingStream(org.jivesoftware.smackx.packet.StreamInitiation) throws org.jivesoftware.smack.XMPPException
meth public java.io.OutputStream createOutgoingStream(java.lang.String,java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public java.lang.String[] getNamespaces()
meth public org.jivesoftware.smack.filter.PacketFilter getInitiationPacketFilter(java.lang.String,java.lang.String)
meth public void cleanup()
supr org.jivesoftware.smackx.filetransfer.StreamNegotiator
hfds connection,primaryFilter,primaryNegotiator,secondaryFilter,secondaryNegotiator
hcls NegotiatorService

CLSS public abstract org.jivesoftware.smackx.filetransfer.FileTransfer
cons protected init(java.lang.String,java.lang.String,org.jivesoftware.smackx.filetransfer.FileTransferNegotiator)
fld protected java.lang.String streamID
fld protected long amountWritten
fld protected org.jivesoftware.smackx.filetransfer.FileTransferNegotiator negotiator
innr public final static !enum Error
innr public final static !enum Status
meth protected boolean updateStatus(org.jivesoftware.smackx.filetransfer.FileTransfer$Status,org.jivesoftware.smackx.filetransfer.FileTransfer$Status)
meth protected void setError(org.jivesoftware.smackx.filetransfer.FileTransfer$Error)
meth protected void setException(java.lang.Exception)
meth protected void setFileInfo(java.lang.String,java.lang.String,long)
meth protected void setFileInfo(java.lang.String,long)
meth protected void setStatus(org.jivesoftware.smackx.filetransfer.FileTransfer$Status)
meth protected void writeToStream(java.io.InputStream,java.io.OutputStream) throws org.jivesoftware.smack.XMPPException
meth public abstract void cancel()
meth public boolean isDone()
meth public double getProgress()
meth public java.lang.Exception getException()
meth public java.lang.String getFileName()
meth public java.lang.String getFilePath()
meth public java.lang.String getPeer()
meth public java.lang.String getStreamID()
meth public long getAmountWritten()
meth public long getFileSize()
meth public org.jivesoftware.smackx.filetransfer.FileTransfer$Error getError()
meth public org.jivesoftware.smackx.filetransfer.FileTransfer$Status getStatus()
supr java.lang.Object
hfds BUFFER_SIZE,error,exception,fileName,filePath,fileSize,peer,status,statusMonitor

CLSS public final static !enum org.jivesoftware.smackx.filetransfer.FileTransfer$Error
 outer org.jivesoftware.smackx.filetransfer.FileTransfer
fld public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Error bad_file
fld public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Error connection
fld public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Error no_response
fld public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Error none
fld public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Error not_acceptable
fld public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Error stream
meth public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Error[] values()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public static org.jivesoftware.smackx.filetransfer.FileTransfer$Error valueOf(java.lang.String)
supr java.lang.Enum<org.jivesoftware.smackx.filetransfer.FileTransfer$Error>
hfds msg

CLSS public final static !enum org.jivesoftware.smackx.filetransfer.FileTransfer$Status
 outer org.jivesoftware.smackx.filetransfer.FileTransfer
fld public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Status cancelled
fld public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Status complete
fld public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Status error
fld public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Status in_progress
fld public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Status initial
fld public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Status negotiated
fld public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Status negotiating_stream
fld public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Status negotiating_transfer
fld public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Status refused
meth public final static org.jivesoftware.smackx.filetransfer.FileTransfer$Status[] values()
meth public java.lang.String toString()
meth public static org.jivesoftware.smackx.filetransfer.FileTransfer$Status valueOf(java.lang.String)
supr java.lang.Enum<org.jivesoftware.smackx.filetransfer.FileTransfer$Status>
hfds status

CLSS public abstract interface org.jivesoftware.smackx.filetransfer.FileTransferListener
meth public abstract void fileTransferRequest(org.jivesoftware.smackx.filetransfer.FileTransferRequest)

CLSS public org.jivesoftware.smackx.filetransfer.FileTransferManager
cons public init(org.jivesoftware.smack.XMPPConnection)
meth protected org.jivesoftware.smackx.filetransfer.IncomingFileTransfer createIncomingFileTransfer(org.jivesoftware.smackx.filetransfer.FileTransferRequest)
meth protected void fireNewRequest(org.jivesoftware.smackx.packet.StreamInitiation)
meth protected void rejectIncomingFileTransfer(org.jivesoftware.smackx.filetransfer.FileTransferRequest)
meth public org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer createOutgoingFileTransfer(java.lang.String)
meth public void addFileTransferListener(org.jivesoftware.smackx.filetransfer.FileTransferListener)
meth public void removeFileTransferListener(org.jivesoftware.smackx.filetransfer.FileTransferListener)
supr java.lang.Object
hfds connection,fileTransferNegotiator,listeners

CLSS public org.jivesoftware.smackx.filetransfer.FileTransferNegotiator
fld protected final static java.lang.String STREAM_DATA_FIELD_NAME = "stream-method"
fld public final static java.lang.String BYTE_STREAM = "http://jabber.org/protocol/bytestreams"
fld public final static java.lang.String INBAND_BYTE_STREAM = "http://jabber.org/protocol/ibb"
fld public static boolean IBB_ONLY
meth public java.lang.String getNextStreamID()
meth public org.jivesoftware.smackx.filetransfer.StreamNegotiator negotiateOutgoingTransfer(java.lang.String,java.lang.String,java.lang.String,long,java.lang.String,int) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.filetransfer.StreamNegotiator selectStreamNegotiator(org.jivesoftware.smackx.filetransfer.FileTransferRequest) throws org.jivesoftware.smack.XMPPException
meth public static boolean isServiceEnabled(org.jivesoftware.smack.XMPPConnection)
meth public static java.util.Collection getSupportedProtocols()
meth public static org.jivesoftware.smack.packet.IQ createIQ(java.lang.String,java.lang.String,java.lang.String,org.jivesoftware.smack.packet.IQ$Type)
meth public static org.jivesoftware.smackx.filetransfer.FileTransferNegotiator getInstanceFor(org.jivesoftware.smack.XMPPConnection)
meth public static void setServiceEnabled(org.jivesoftware.smack.XMPPConnection,boolean)
meth public void rejectStream(org.jivesoftware.smackx.packet.StreamInitiation)
supr java.lang.Object
hfds NAMESPACE,PROTOCOLS,STREAM_INIT_PREFIX,byteStreamTransferManager,connection,inbandTransferManager,randomGenerator,transferObject

CLSS public abstract interface org.jivesoftware.smackx.filetransfer.FileTransferNegotiatorManager
meth public abstract org.jivesoftware.smackx.filetransfer.StreamNegotiator createNegotiator()

CLSS public org.jivesoftware.smackx.filetransfer.FileTransferRequest
cons public init(org.jivesoftware.smackx.filetransfer.FileTransferManager,org.jivesoftware.smackx.packet.StreamInitiation)
meth protected org.jivesoftware.smackx.packet.StreamInitiation getStreamInitiation()
meth public java.lang.String getDescription()
meth public java.lang.String getFileName()
meth public java.lang.String getMimeType()
meth public java.lang.String getRequestor()
meth public java.lang.String getStreamID()
meth public long getFileSize()
meth public org.jivesoftware.smackx.filetransfer.IncomingFileTransfer accept()
meth public void reject()
supr java.lang.Object
hfds manager,streamInitiation

CLSS public org.jivesoftware.smackx.filetransfer.IBBTransferNegotiator
cons protected init(org.jivesoftware.smack.XMPPConnection)
fld protected final static java.lang.String NAMESPACE = "http://jabber.org/protocol/ibb"
fld public final static int DEFAULT_BLOCK_SIZE = 4096
meth public java.io.InputStream createIncomingStream(org.jivesoftware.smackx.packet.StreamInitiation) throws org.jivesoftware.smack.XMPPException
meth public java.io.OutputStream createOutgoingStream(java.lang.String,java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public java.lang.String[] getNamespaces()
meth public org.jivesoftware.smack.filter.PacketFilter getInitiationPacketFilter(java.lang.String,java.lang.String)
meth public void cleanup()
supr org.jivesoftware.smackx.filetransfer.StreamNegotiator
hfds connection
hcls IBBInputStream,IBBMessageSidFilter,IBBOpenSidFilter,IBBOutputStream

CLSS public org.jivesoftware.smackx.filetransfer.IncomingFileTransfer
cons protected init(org.jivesoftware.smackx.filetransfer.FileTransferRequest,org.jivesoftware.smackx.filetransfer.FileTransferNegotiator)
meth public java.io.InputStream recieveFile() throws org.jivesoftware.smack.XMPPException
meth public void cancel()
meth public void recieveFile(java.io.File) throws org.jivesoftware.smack.XMPPException
supr org.jivesoftware.smackx.filetransfer.FileTransfer
hfds inputStream,recieveRequest

CLSS public org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer
cons protected init(java.lang.String,java.lang.String,java.lang.String,org.jivesoftware.smackx.filetransfer.FileTransferNegotiator)
innr public abstract interface static NegotiationProgress
meth protected boolean updateStatus(org.jivesoftware.smackx.filetransfer.FileTransfer$Status,org.jivesoftware.smackx.filetransfer.FileTransfer$Status)
meth protected java.io.OutputStream getOutputStream()
meth protected void setException(java.lang.Exception)
meth protected void setOutputStream(java.io.OutputStream)
meth protected void setStatus(org.jivesoftware.smackx.filetransfer.FileTransfer$Status)
meth public java.io.OutputStream sendFile(java.lang.String,long,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public long getBytesSent()
meth public static int getResponseTimeout()
meth public static void setResponseTimeout(int)
meth public void cancel()
meth public void sendFile(java.io.File,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void sendFile(java.lang.String,long,java.lang.String,org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer$NegotiationProgress)
meth public void sendStream(java.io.InputStream,java.lang.String,long,java.lang.String)
supr org.jivesoftware.smackx.filetransfer.FileTransfer
hfds RESPONSE_TIMEOUT,callback,initiator,outputStream,transferThread

CLSS public abstract interface static org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer$NegotiationProgress
 outer org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer
meth public abstract void errorEstablishingStream(java.lang.Exception)
meth public abstract void outputStreamEstablished(java.io.OutputStream)
meth public abstract void statusUpdated(org.jivesoftware.smackx.filetransfer.FileTransfer$Status,org.jivesoftware.smackx.filetransfer.FileTransfer$Status)

CLSS public org.jivesoftware.smackx.filetransfer.Socks5TransferNegotiator
cons public init(org.jivesoftware.smackx.filetransfer.Socks5TransferNegotiatorManager,org.jivesoftware.smack.XMPPConnection)
fld protected final static java.lang.String NAMESPACE = "http://jabber.org/protocol/bytestreams"
fld public static boolean isAllowLocalProxyHost
meth public java.io.InputStream createIncomingStream(org.jivesoftware.smackx.packet.StreamInitiation) throws org.jivesoftware.smack.XMPPException
meth public java.io.OutputStream createOutgoingStream(java.lang.String,java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public java.lang.String[] getNamespaces()
meth public org.jivesoftware.smack.filter.PacketFilter getInitiationPacketFilter(java.lang.String,java.lang.String)
meth public void cleanup()
supr org.jivesoftware.smackx.filetransfer.StreamNegotiator
hfds CONNECT_FAILURE_THRESHOLD,connection,transferNegotiatorManager
hcls BytestreamSIDFilter,SelectedHostInfo

CLSS public org.jivesoftware.smackx.filetransfer.Socks5TransferNegotiatorManager
cons public init(org.jivesoftware.smack.XMPPConnection)
intf org.jivesoftware.smackx.filetransfer.FileTransferNegotiatorManager
meth public int getConnectionFailures(java.lang.String)
meth public java.lang.Runnable addTransfer() throws java.io.IOException
meth public java.util.Collection<org.jivesoftware.smackx.packet.Bytestream$StreamHost> getStreamHosts()
meth public org.jivesoftware.smackx.filetransfer.StreamNegotiator createNegotiator()
meth public void cleanup()
meth public void incrementConnectionFailures(java.lang.String)
meth public void removeTransfer()
supr java.lang.Object
hfds BLACKLIST_LIFETIME,addressBlacklist,connection,processLock,proxies,proxyLock,proxyProcess,streamHosts
hcls ProxyProcess

CLSS public abstract org.jivesoftware.smackx.filetransfer.StreamNegotiator
cons public init()
meth public abstract java.io.InputStream createIncomingStream(org.jivesoftware.smackx.packet.StreamInitiation) throws org.jivesoftware.smack.XMPPException
meth public abstract java.io.OutputStream createOutgoingStream(java.lang.String,java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public abstract java.lang.String[] getNamespaces()
meth public abstract org.jivesoftware.smack.filter.PacketFilter getInitiationPacketFilter(java.lang.String,java.lang.String)
meth public abstract void cleanup()
meth public org.jivesoftware.smack.packet.IQ createError(java.lang.String,java.lang.String,java.lang.String,org.jivesoftware.smack.packet.XMPPError)
meth public org.jivesoftware.smackx.packet.StreamInitiation createInitiationAccept(org.jivesoftware.smackx.packet.StreamInitiation,java.lang.String[])
supr java.lang.Object

CLSS public org.jivesoftware.smackx.muc.Affiliate
meth public java.lang.String getAffiliation()
meth public java.lang.String getJid()
meth public java.lang.String getNick()
meth public java.lang.String getRole()
supr java.lang.Object
hfds affiliation,jid,nick,role

CLSS public org.jivesoftware.smackx.muc.DeafOccupantInterceptor
cons public init()
intf org.jivesoftware.smack.PacketInterceptor
meth public void interceptPacket(org.jivesoftware.smack.packet.Packet)
supr java.lang.Object
hcls DeafExtension

CLSS public org.jivesoftware.smackx.muc.DefaultParticipantStatusListener
cons public init()
intf org.jivesoftware.smackx.muc.ParticipantStatusListener
meth public void adminGranted(java.lang.String)
meth public void adminRevoked(java.lang.String)
meth public void banned(java.lang.String,java.lang.String,java.lang.String)
meth public void joined(java.lang.String)
meth public void kicked(java.lang.String,java.lang.String,java.lang.String)
meth public void left(java.lang.String)
meth public void membershipGranted(java.lang.String)
meth public void membershipRevoked(java.lang.String)
meth public void moderatorGranted(java.lang.String)
meth public void moderatorRevoked(java.lang.String)
meth public void nicknameChanged(java.lang.String,java.lang.String)
meth public void ownershipGranted(java.lang.String)
meth public void ownershipRevoked(java.lang.String)
meth public void voiceGranted(java.lang.String)
meth public void voiceRevoked(java.lang.String)
supr java.lang.Object

CLSS public org.jivesoftware.smackx.muc.DefaultUserStatusListener
cons public init()
intf org.jivesoftware.smackx.muc.UserStatusListener
meth public void adminGranted()
meth public void adminRevoked()
meth public void banned(java.lang.String,java.lang.String)
meth public void kicked(java.lang.String,java.lang.String)
meth public void membershipGranted()
meth public void membershipRevoked()
meth public void moderatorGranted()
meth public void moderatorRevoked()
meth public void ownershipGranted()
meth public void ownershipRevoked()
meth public void voiceGranted()
meth public void voiceRevoked()
supr java.lang.Object

CLSS public org.jivesoftware.smackx.muc.DiscussionHistory
cons public init()
meth public int getMaxChars()
meth public int getMaxStanzas()
meth public int getSeconds()
meth public java.util.Date getSince()
meth public void setMaxChars(int)
meth public void setMaxStanzas(int)
meth public void setSeconds(int)
meth public void setSince(java.util.Date)
supr java.lang.Object
hfds maxChars,maxStanzas,seconds,since

CLSS public org.jivesoftware.smackx.muc.HostedRoom
cons public init(org.jivesoftware.smackx.packet.DiscoverItems$Item)
meth public java.lang.String getJid()
meth public java.lang.String getName()
supr java.lang.Object
hfds jid,name

CLSS public abstract interface org.jivesoftware.smackx.muc.InvitationListener
meth public abstract void invitationReceived(org.jivesoftware.smack.XMPPConnection,java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.jivesoftware.smack.packet.Message)

CLSS public abstract interface org.jivesoftware.smackx.muc.InvitationRejectionListener
meth public abstract void invitationDeclined(java.lang.String,java.lang.String)

CLSS public org.jivesoftware.smackx.muc.MultiUserChat
cons public init(org.jivesoftware.smack.XMPPConnection,java.lang.String)
meth public boolean isJoined()
meth public int getOccupantsCount()
meth public java.lang.String getNickname()
meth public java.lang.String getReservedNickname()
meth public java.lang.String getRoom()
meth public java.lang.String getSubject()
meth public java.util.Collection<org.jivesoftware.smackx.muc.Affiliate> getAdmins() throws org.jivesoftware.smack.XMPPException
meth public java.util.Collection<org.jivesoftware.smackx.muc.Affiliate> getMembers() throws org.jivesoftware.smack.XMPPException
meth public java.util.Collection<org.jivesoftware.smackx.muc.Affiliate> getOutcasts() throws org.jivesoftware.smack.XMPPException
meth public java.util.Collection<org.jivesoftware.smackx.muc.Affiliate> getOwners() throws org.jivesoftware.smack.XMPPException
meth public java.util.Collection<org.jivesoftware.smackx.muc.Occupant> getModerators() throws org.jivesoftware.smack.XMPPException
meth public java.util.Collection<org.jivesoftware.smackx.muc.Occupant> getParticipants() throws org.jivesoftware.smack.XMPPException
meth public java.util.Iterator<java.lang.String> getOccupants()
meth public org.jivesoftware.smack.Chat createPrivateChat(java.lang.String,org.jivesoftware.smack.MessageListener)
meth public org.jivesoftware.smack.packet.Message createMessage()
meth public org.jivesoftware.smack.packet.Message nextMessage()
meth public org.jivesoftware.smack.packet.Message nextMessage(long)
meth public org.jivesoftware.smack.packet.Message pollMessage()
meth public org.jivesoftware.smack.packet.Presence getOccupantPresence(java.lang.String)
meth public org.jivesoftware.smackx.Form getConfigurationForm() throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.Form getRegistrationForm() throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.muc.Occupant getOccupant(java.lang.String)
meth public static boolean isServiceEnabled(org.jivesoftware.smack.XMPPConnection,java.lang.String)
meth public static java.util.Collection<java.lang.String> getServiceNames(org.jivesoftware.smack.XMPPConnection) throws org.jivesoftware.smack.XMPPException
meth public static java.util.Collection<org.jivesoftware.smackx.muc.HostedRoom> getHostedRooms(org.jivesoftware.smack.XMPPConnection,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public static java.util.Iterator<java.lang.String> getJoinedRooms(org.jivesoftware.smack.XMPPConnection,java.lang.String)
meth public static org.jivesoftware.smackx.muc.RoomInfo getRoomInfo(org.jivesoftware.smack.XMPPConnection,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public static void addInvitationListener(org.jivesoftware.smack.XMPPConnection,org.jivesoftware.smackx.muc.InvitationListener)
meth public static void decline(org.jivesoftware.smack.XMPPConnection,java.lang.String,java.lang.String,java.lang.String)
meth public static void removeInvitationListener(org.jivesoftware.smack.XMPPConnection,org.jivesoftware.smackx.muc.InvitationListener)
meth public void addInvitationRejectionListener(org.jivesoftware.smackx.muc.InvitationRejectionListener)
meth public void addMessageListener(org.jivesoftware.smack.PacketListener)
meth public void addParticipantListener(org.jivesoftware.smack.PacketListener)
meth public void addParticipantStatusListener(org.jivesoftware.smackx.muc.ParticipantStatusListener)
meth public void addPresenceInterceptor(org.jivesoftware.smack.PacketInterceptor)
meth public void addSubjectUpdatedListener(org.jivesoftware.smackx.muc.SubjectUpdatedListener)
meth public void addUserStatusListener(org.jivesoftware.smackx.muc.UserStatusListener)
meth public void banUser(java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void banUsers(java.util.Collection<java.lang.String>) throws org.jivesoftware.smack.XMPPException
meth public void changeAvailabilityStatus(java.lang.String,org.jivesoftware.smack.packet.Presence$Mode)
meth public void changeNickname(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void changeSubject(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void create(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void destroy(java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void finalize() throws java.lang.Throwable
meth public void grantAdmin(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void grantAdmin(java.util.Collection<java.lang.String>) throws org.jivesoftware.smack.XMPPException
meth public void grantMembership(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void grantMembership(java.util.Collection<java.lang.String>) throws org.jivesoftware.smack.XMPPException
meth public void grantModerator(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void grantModerator(java.util.Collection<java.lang.String>) throws org.jivesoftware.smack.XMPPException
meth public void grantOwnership(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void grantOwnership(java.util.Collection<java.lang.String>) throws org.jivesoftware.smack.XMPPException
meth public void grantVoice(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void grantVoice(java.util.Collection<java.lang.String>) throws org.jivesoftware.smack.XMPPException
meth public void invite(java.lang.String,java.lang.String)
meth public void invite(org.jivesoftware.smack.packet.Message,java.lang.String,java.lang.String)
meth public void join(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void join(java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void join(java.lang.String,java.lang.String,org.jivesoftware.smackx.muc.DiscussionHistory,long) throws org.jivesoftware.smack.XMPPException
meth public void kickParticipant(java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void leave()
meth public void removeInvitationRejectionListener(org.jivesoftware.smackx.muc.InvitationRejectionListener)
meth public void removeMessageListener(org.jivesoftware.smack.PacketListener)
meth public void removeParticipantListener(org.jivesoftware.smack.PacketListener)
meth public void removeParticipantStatusListener(org.jivesoftware.smackx.muc.ParticipantStatusListener)
meth public void removePresenceInterceptor(org.jivesoftware.smack.PacketInterceptor)
meth public void removeSubjectUpdatedListener(org.jivesoftware.smackx.muc.SubjectUpdatedListener)
meth public void removeUserStatusListener(org.jivesoftware.smackx.muc.UserStatusListener)
meth public void revokeAdmin(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void revokeAdmin(java.util.Collection<java.lang.String>) throws org.jivesoftware.smack.XMPPException
meth public void revokeMembership(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void revokeMembership(java.util.Collection<java.lang.String>) throws org.jivesoftware.smack.XMPPException
meth public void revokeModerator(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void revokeModerator(java.util.Collection<java.lang.String>) throws org.jivesoftware.smack.XMPPException
meth public void revokeOwnership(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void revokeOwnership(java.util.Collection<java.lang.String>) throws org.jivesoftware.smack.XMPPException
meth public void revokeVoice(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void revokeVoice(java.util.Collection<java.lang.String>) throws org.jivesoftware.smack.XMPPException
meth public void sendConfigurationForm(org.jivesoftware.smackx.Form) throws org.jivesoftware.smack.XMPPException
meth public void sendMessage(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void sendMessage(org.jivesoftware.smack.packet.Message) throws org.jivesoftware.smack.XMPPException
meth public void sendRegistrationForm(org.jivesoftware.smackx.Form) throws org.jivesoftware.smack.XMPPException
supr java.lang.Object
hfds connection,connectionListeners,discoNamespace,discoNode,invitationRejectionListeners,joined,joinedRooms,messageCollector,messageFilter,nickname,occupantsMap,participantStatusListeners,presenceFilter,presenceInterceptors,room,roomListenerMultiplexor,subject,subjectUpdatedListeners,userStatusListeners
hcls InvitationsMonitor

CLSS public org.jivesoftware.smackx.muc.Occupant
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getAffiliation()
meth public java.lang.String getJid()
meth public java.lang.String getNick()
meth public java.lang.String getRole()
supr java.lang.Object
hfds affiliation,jid,nick,role

CLSS public abstract interface org.jivesoftware.smackx.muc.ParticipantStatusListener
meth public abstract void adminGranted(java.lang.String)
meth public abstract void adminRevoked(java.lang.String)
meth public abstract void banned(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void joined(java.lang.String)
meth public abstract void kicked(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void left(java.lang.String)
meth public abstract void membershipGranted(java.lang.String)
meth public abstract void membershipRevoked(java.lang.String)
meth public abstract void moderatorGranted(java.lang.String)
meth public abstract void moderatorRevoked(java.lang.String)
meth public abstract void nicknameChanged(java.lang.String,java.lang.String)
meth public abstract void ownershipGranted(java.lang.String)
meth public abstract void ownershipRevoked(java.lang.String)
meth public abstract void voiceGranted(java.lang.String)
meth public abstract void voiceRevoked(java.lang.String)

CLSS public org.jivesoftware.smackx.muc.RoomInfo
meth public boolean isMembersOnly()
meth public boolean isModerated()
meth public boolean isNonanonymous()
meth public boolean isPasswordProtected()
meth public boolean isPersistent()
meth public int getOccupantsCount()
meth public java.lang.String getDescription()
meth public java.lang.String getRoom()
meth public java.lang.String getSubject()
supr java.lang.Object
hfds description,membersOnly,moderated,nonanonymous,occupantsCount,passwordProtected,persistent,room,subject

CLSS public abstract interface org.jivesoftware.smackx.muc.SubjectUpdatedListener
meth public abstract void subjectUpdated(java.lang.String,java.lang.String)

CLSS public abstract interface org.jivesoftware.smackx.muc.UserStatusListener
meth public abstract void adminGranted()
meth public abstract void adminRevoked()
meth public abstract void banned(java.lang.String,java.lang.String)
meth public abstract void kicked(java.lang.String,java.lang.String)
meth public abstract void membershipGranted()
meth public abstract void membershipRevoked()
meth public abstract void moderatorGranted()
meth public abstract void moderatorRevoked()
meth public abstract void ownershipGranted()
meth public abstract void ownershipRevoked()
meth public abstract void voiceGranted()
meth public abstract void voiceRevoked()

CLSS public org.jivesoftware.smackx.packet.AdHocCommandData
cons public init()
innr public static SpecificError
meth public java.lang.String getChildElementXML()
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public java.lang.String getNode()
meth public java.lang.String getSessionID()
meth public java.util.List<org.jivesoftware.smackx.commands.AdHocCommand$Action> getActions()
meth public java.util.List<org.jivesoftware.smackx.commands.AdHocCommandNote> getNotes()
meth public org.jivesoftware.smackx.commands.AdHocCommand$Action getAction()
meth public org.jivesoftware.smackx.commands.AdHocCommand$Action getExecuteAction()
meth public org.jivesoftware.smackx.commands.AdHocCommand$Status getStatus()
meth public org.jivesoftware.smackx.packet.DataForm getForm()
meth public void addAction(org.jivesoftware.smackx.commands.AdHocCommand$Action)
meth public void addNote(org.jivesoftware.smackx.commands.AdHocCommandNote)
meth public void remveNote(org.jivesoftware.smackx.commands.AdHocCommandNote)
meth public void setAction(org.jivesoftware.smackx.commands.AdHocCommand$Action)
meth public void setExecuteAction(org.jivesoftware.smackx.commands.AdHocCommand$Action)
meth public void setForm(org.jivesoftware.smackx.packet.DataForm)
meth public void setId(java.lang.String)
meth public void setName(java.lang.String)
meth public void setNode(java.lang.String)
meth public void setSessionID(java.lang.String)
meth public void setStatus(org.jivesoftware.smackx.commands.AdHocCommand$Status)
supr org.jivesoftware.smack.packet.IQ
hfds action,actions,executeAction,form,id,lang,name,node,notes,sessionID,status

CLSS public static org.jivesoftware.smackx.packet.AdHocCommandData$SpecificError
 outer org.jivesoftware.smackx.packet.AdHocCommandData
cons public init(org.jivesoftware.smackx.commands.AdHocCommand$SpecificErrorCondition)
fld public final static java.lang.String namespace = "http://jabber.org/protocol/commands"
fld public org.jivesoftware.smackx.commands.AdHocCommand$SpecificErrorCondition condition
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String toXML()
meth public org.jivesoftware.smackx.commands.AdHocCommand$SpecificErrorCondition getCondition()
supr java.lang.Object

CLSS public org.jivesoftware.smackx.packet.Bytestream
cons public init()
cons public init(java.lang.String)
innr public final static !enum Mode
innr public static Activate
innr public static StreamHost
innr public static StreamHostUsed
meth public int countStreamHosts()
meth public java.lang.String getChildElementXML()
meth public java.lang.String getSessionID()
meth public java.util.Collection<org.jivesoftware.smackx.packet.Bytestream$StreamHost> getStreamHosts()
meth public org.jivesoftware.smackx.packet.Bytestream$Activate getToActivate()
meth public org.jivesoftware.smackx.packet.Bytestream$Mode getMode()
meth public org.jivesoftware.smackx.packet.Bytestream$StreamHost addStreamHost(java.lang.String,java.lang.String)
meth public org.jivesoftware.smackx.packet.Bytestream$StreamHost addStreamHost(java.lang.String,java.lang.String,int)
meth public org.jivesoftware.smackx.packet.Bytestream$StreamHost getStreamHost(java.lang.String)
meth public org.jivesoftware.smackx.packet.Bytestream$StreamHostUsed getUsedHost()
meth public void addStreamHost(org.jivesoftware.smackx.packet.Bytestream$StreamHost)
meth public void setMode(org.jivesoftware.smackx.packet.Bytestream$Mode)
meth public void setSessionID(java.lang.String)
meth public void setToActivate(java.lang.String)
meth public void setUsedHost(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds mode,sessionID,streamHosts,toActivate,usedHost

CLSS public static org.jivesoftware.smackx.packet.Bytestream$Activate
 outer org.jivesoftware.smackx.packet.Bytestream
cons public init(java.lang.String)
fld public java.lang.String NAMESPACE
fld public static java.lang.String ELEMENTNAME
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getTarget()
meth public java.lang.String toXML()
supr java.lang.Object
hfds target

CLSS public final static !enum org.jivesoftware.smackx.packet.Bytestream$Mode
 outer org.jivesoftware.smackx.packet.Bytestream
fld public final static org.jivesoftware.smackx.packet.Bytestream$Mode tcp
fld public final static org.jivesoftware.smackx.packet.Bytestream$Mode udp
meth public final static org.jivesoftware.smackx.packet.Bytestream$Mode[] values()
meth public static org.jivesoftware.smackx.packet.Bytestream$Mode fromName(java.lang.String)
meth public static org.jivesoftware.smackx.packet.Bytestream$Mode valueOf(java.lang.String)
supr java.lang.Enum<org.jivesoftware.smackx.packet.Bytestream$Mode>

CLSS public static org.jivesoftware.smackx.packet.Bytestream$StreamHost
 outer org.jivesoftware.smackx.packet.Bytestream
cons public init(java.lang.String,java.lang.String)
fld public static java.lang.String ELEMENTNAME
fld public static java.lang.String NAMESPACE
intf org.jivesoftware.smack.packet.PacketExtension
meth public int getPort()
meth public java.lang.String getAddress()
meth public java.lang.String getElementName()
meth public java.lang.String getJID()
meth public java.lang.String getNamespace()
meth public java.lang.String toXML()
meth public void setPort(int)
supr java.lang.Object
hfds JID,addy,port

CLSS public static org.jivesoftware.smackx.packet.Bytestream$StreamHostUsed
 outer org.jivesoftware.smackx.packet.Bytestream
cons public init(java.lang.String)
fld public java.lang.String NAMESPACE
fld public static java.lang.String ELEMENTNAME
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getJID()
meth public java.lang.String getNamespace()
meth public java.lang.String toXML()
supr java.lang.Object
hfds JID

CLSS public org.jivesoftware.smackx.packet.ChatStateExtension
cons public init(org.jivesoftware.smackx.ChatState)
innr public static Provider
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String toXML()
supr java.lang.Object
hfds state

CLSS public static org.jivesoftware.smackx.packet.ChatStateExtension$Provider
 outer org.jivesoftware.smackx.packet.ChatStateExtension
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.packet.DataForm
cons public init(java.lang.String)
innr public static Item
innr public static ReportedData
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getTitle()
meth public java.lang.String getType()
meth public java.lang.String toXML()
meth public java.util.Iterator<java.lang.String> getInstructions()
meth public java.util.Iterator<org.jivesoftware.smackx.FormField> getFields()
meth public java.util.Iterator<org.jivesoftware.smackx.packet.DataForm$Item> getItems()
meth public org.jivesoftware.smackx.packet.DataForm$ReportedData getReportedData()
meth public void addField(org.jivesoftware.smackx.FormField)
meth public void addInstruction(java.lang.String)
meth public void addItem(org.jivesoftware.smackx.packet.DataForm$Item)
meth public void setInstructions(java.util.List<java.lang.String>)
meth public void setReportedData(org.jivesoftware.smackx.packet.DataForm$ReportedData)
meth public void setTitle(java.lang.String)
supr java.lang.Object
hfds fields,instructions,items,reportedData,title,type

CLSS public static org.jivesoftware.smackx.packet.DataForm$Item
 outer org.jivesoftware.smackx.packet.DataForm
cons public init(java.util.List<org.jivesoftware.smackx.FormField>)
meth public java.lang.String toXML()
meth public java.util.Iterator<org.jivesoftware.smackx.FormField> getFields()
supr java.lang.Object
hfds fields

CLSS public static org.jivesoftware.smackx.packet.DataForm$ReportedData
 outer org.jivesoftware.smackx.packet.DataForm
cons public init(java.util.List<org.jivesoftware.smackx.FormField>)
meth public java.lang.String toXML()
meth public java.util.Iterator<org.jivesoftware.smackx.FormField> getFields()
supr java.lang.Object
hfds fields

CLSS public org.jivesoftware.smackx.packet.DefaultPrivateData
cons public init(java.lang.String,java.lang.String)
intf org.jivesoftware.smackx.packet.PrivateData
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getValue(java.lang.String)
meth public java.lang.String toXML()
meth public java.util.Iterator getNames()
meth public void setValue(java.lang.String,java.lang.String)
supr java.lang.Object
hfds elementName,map,namespace

CLSS public org.jivesoftware.smackx.packet.DelayInformation
cons public init(java.util.Date)
fld public static java.text.SimpleDateFormat NEW_UTC_FORMAT
fld public static java.text.SimpleDateFormat UTC_FORMAT
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getFrom()
meth public java.lang.String getNamespace()
meth public java.lang.String getReason()
meth public java.lang.String toXML()
meth public java.util.Date getStamp()
meth public void setFrom(java.lang.String)
meth public void setReason(java.lang.String)
supr java.lang.Object
hfds from,reason,stamp

CLSS public org.jivesoftware.smackx.packet.DiscoverInfo
cons public init()
innr public static Feature
innr public static Identity
meth public boolean containsFeature(java.lang.String)
meth public java.lang.String getChildElementXML()
meth public java.lang.String getNode()
meth public java.util.Iterator<org.jivesoftware.smackx.packet.DiscoverInfo$Identity> getIdentities()
meth public void addFeature(java.lang.String)
meth public void addIdentity(org.jivesoftware.smackx.packet.DiscoverInfo$Identity)
meth public void setNode(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds features,identities,node

CLSS public static org.jivesoftware.smackx.packet.DiscoverInfo$Feature
 outer org.jivesoftware.smackx.packet.DiscoverInfo
cons public init(java.lang.String)
meth public java.lang.String getVar()
meth public java.lang.String toXML()
supr java.lang.Object
hfds variable

CLSS public static org.jivesoftware.smackx.packet.DiscoverInfo$Identity
 outer org.jivesoftware.smackx.packet.DiscoverInfo
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getCategory()
meth public java.lang.String getName()
meth public java.lang.String getType()
meth public java.lang.String toXML()
meth public void setType(java.lang.String)
supr java.lang.Object
hfds category,name,type

CLSS public org.jivesoftware.smackx.packet.DiscoverItems
cons public init()
innr public static Item
meth public java.lang.String getChildElementXML()
meth public java.lang.String getNode()
meth public java.util.Iterator<org.jivesoftware.smackx.packet.DiscoverItems$Item> getItems()
meth public void addItem(org.jivesoftware.smackx.packet.DiscoverItems$Item)
meth public void setNode(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds items,node

CLSS public static org.jivesoftware.smackx.packet.DiscoverItems$Item
 outer org.jivesoftware.smackx.packet.DiscoverItems
cons public init(java.lang.String)
fld public final static java.lang.String REMOVE_ACTION = "remove"
fld public final static java.lang.String UPDATE_ACTION = "update"
meth public java.lang.String getAction()
meth public java.lang.String getEntityID()
meth public java.lang.String getName()
meth public java.lang.String getNode()
meth public java.lang.String toXML()
meth public void setAction(java.lang.String)
meth public void setName(java.lang.String)
meth public void setNode(java.lang.String)
supr java.lang.Object
hfds action,entityID,name,node

CLSS public org.jivesoftware.smackx.packet.IBBExtensions
cons public init()
fld public final static java.lang.String NAMESPACE = "http://jabber.org/protocol/ibb"
innr public static Close
innr public static Data
innr public static Open
supr java.lang.Object
hcls IBB

CLSS public static org.jivesoftware.smackx.packet.IBBExtensions$Close
 outer org.jivesoftware.smackx.packet.IBBExtensions
cons public init(java.lang.String)
fld public final static java.lang.String ELEMENT_NAME = "close"
meth public java.lang.String getChildElementXML()
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getSessionID()
supr org.jivesoftware.smack.packet.IQ

CLSS public static org.jivesoftware.smackx.packet.IBBExtensions$Data
 outer org.jivesoftware.smackx.packet.IBBExtensions
cons public init(java.lang.String)
cons public init(java.lang.String,long,java.lang.String)
fld public final static java.lang.String ELEMENT_NAME = "data"
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getData()
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getSessionID()
meth public java.lang.String toXML()
meth public long getSeq()
meth public void setData(java.lang.String)
meth public void setSeq(long)
supr java.lang.Object
hfds data,seq,sid

CLSS public static org.jivesoftware.smackx.packet.IBBExtensions$Open
 outer org.jivesoftware.smackx.packet.IBBExtensions
cons public init(java.lang.String,int)
fld public final static java.lang.String ELEMENT_NAME = "open"
meth public int getBlockSize()
meth public java.lang.String getChildElementXML()
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getSessionID()
supr org.jivesoftware.smack.packet.IQ
hfds blockSize

CLSS public org.jivesoftware.smackx.packet.LastActivity
cons public init()
fld public java.lang.String message
fld public long lastActivity
innr public static Provider
meth public java.lang.String getChildElementXML()
meth public java.lang.String getStatusMessage()
meth public long getIdleTime()
meth public static org.jivesoftware.smackx.packet.LastActivity getLastActivity(org.jivesoftware.smack.XMPPConnection,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void setLastActivity(long)
supr org.jivesoftware.smack.packet.IQ

CLSS public static org.jivesoftware.smackx.packet.LastActivity$Provider
 outer org.jivesoftware.smackx.packet.LastActivity
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.packet.MUCAdmin
cons public init()
innr public static Item
meth public java.lang.String getChildElementXML()
meth public java.util.Iterator getItems()
meth public void addItem(org.jivesoftware.smackx.packet.MUCAdmin$Item)
supr org.jivesoftware.smack.packet.IQ
hfds items

CLSS public static org.jivesoftware.smackx.packet.MUCAdmin$Item
 outer org.jivesoftware.smackx.packet.MUCAdmin
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getActor()
meth public java.lang.String getAffiliation()
meth public java.lang.String getJid()
meth public java.lang.String getNick()
meth public java.lang.String getReason()
meth public java.lang.String getRole()
meth public java.lang.String toXML()
meth public void setActor(java.lang.String)
meth public void setJid(java.lang.String)
meth public void setNick(java.lang.String)
meth public void setReason(java.lang.String)
supr java.lang.Object
hfds actor,affiliation,jid,nick,reason,role

CLSS public org.jivesoftware.smackx.packet.MUCInitialPresence
cons public init()
innr public static History
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getPassword()
meth public java.lang.String toXML()
meth public org.jivesoftware.smackx.packet.MUCInitialPresence$History getHistory()
meth public void setHistory(org.jivesoftware.smackx.packet.MUCInitialPresence$History)
meth public void setPassword(java.lang.String)
supr java.lang.Object
hfds history,password

CLSS public static org.jivesoftware.smackx.packet.MUCInitialPresence$History
 outer org.jivesoftware.smackx.packet.MUCInitialPresence
cons public init()
meth public int getMaxChars()
meth public int getMaxStanzas()
meth public int getSeconds()
meth public java.lang.String toXML()
meth public java.util.Date getSince()
meth public void setMaxChars(int)
meth public void setMaxStanzas(int)
meth public void setSeconds(int)
meth public void setSince(java.util.Date)
supr java.lang.Object
hfds maxChars,maxStanzas,seconds,since

CLSS public org.jivesoftware.smackx.packet.MUCOwner
cons public init()
innr public static Destroy
innr public static Item
meth public java.lang.String getChildElementXML()
meth public java.util.Iterator getItems()
meth public org.jivesoftware.smackx.packet.MUCOwner$Destroy getDestroy()
meth public void addItem(org.jivesoftware.smackx.packet.MUCOwner$Item)
meth public void setDestroy(org.jivesoftware.smackx.packet.MUCOwner$Destroy)
supr org.jivesoftware.smack.packet.IQ
hfds destroy,items

CLSS public static org.jivesoftware.smackx.packet.MUCOwner$Destroy
 outer org.jivesoftware.smackx.packet.MUCOwner
cons public init()
meth public java.lang.String getJid()
meth public java.lang.String getReason()
meth public java.lang.String toXML()
meth public void setJid(java.lang.String)
meth public void setReason(java.lang.String)
supr java.lang.Object
hfds jid,reason

CLSS public static org.jivesoftware.smackx.packet.MUCOwner$Item
 outer org.jivesoftware.smackx.packet.MUCOwner
cons public init(java.lang.String)
meth public java.lang.String getActor()
meth public java.lang.String getAffiliation()
meth public java.lang.String getJid()
meth public java.lang.String getNick()
meth public java.lang.String getReason()
meth public java.lang.String getRole()
meth public java.lang.String toXML()
meth public void setActor(java.lang.String)
meth public void setJid(java.lang.String)
meth public void setNick(java.lang.String)
meth public void setReason(java.lang.String)
meth public void setRole(java.lang.String)
supr java.lang.Object
hfds actor,affiliation,jid,nick,reason,role

CLSS public org.jivesoftware.smackx.packet.MUCUser
cons public init()
innr public static Decline
innr public static Destroy
innr public static Invite
innr public static Item
innr public static Status
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getPassword()
meth public java.lang.String toXML()
meth public org.jivesoftware.smackx.packet.MUCUser$Decline getDecline()
meth public org.jivesoftware.smackx.packet.MUCUser$Destroy getDestroy()
meth public org.jivesoftware.smackx.packet.MUCUser$Invite getInvite()
meth public org.jivesoftware.smackx.packet.MUCUser$Item getItem()
meth public org.jivesoftware.smackx.packet.MUCUser$Status getStatus()
meth public void setDecline(org.jivesoftware.smackx.packet.MUCUser$Decline)
meth public void setDestroy(org.jivesoftware.smackx.packet.MUCUser$Destroy)
meth public void setInvite(org.jivesoftware.smackx.packet.MUCUser$Invite)
meth public void setItem(org.jivesoftware.smackx.packet.MUCUser$Item)
meth public void setPassword(java.lang.String)
meth public void setStatus(org.jivesoftware.smackx.packet.MUCUser$Status)
supr java.lang.Object
hfds decline,destroy,invite,item,password,status

CLSS public static org.jivesoftware.smackx.packet.MUCUser$Decline
 outer org.jivesoftware.smackx.packet.MUCUser
cons public init()
meth public java.lang.String getFrom()
meth public java.lang.String getReason()
meth public java.lang.String getTo()
meth public java.lang.String toXML()
meth public void setFrom(java.lang.String)
meth public void setReason(java.lang.String)
meth public void setTo(java.lang.String)
supr java.lang.Object
hfds from,reason,to

CLSS public static org.jivesoftware.smackx.packet.MUCUser$Destroy
 outer org.jivesoftware.smackx.packet.MUCUser
cons public init()
meth public java.lang.String getJid()
meth public java.lang.String getReason()
meth public java.lang.String toXML()
meth public void setJid(java.lang.String)
meth public void setReason(java.lang.String)
supr java.lang.Object
hfds jid,reason

CLSS public static org.jivesoftware.smackx.packet.MUCUser$Invite
 outer org.jivesoftware.smackx.packet.MUCUser
cons public init()
meth public java.lang.String getFrom()
meth public java.lang.String getReason()
meth public java.lang.String getTo()
meth public java.lang.String toXML()
meth public void setFrom(java.lang.String)
meth public void setReason(java.lang.String)
meth public void setTo(java.lang.String)
supr java.lang.Object
hfds from,reason,to

CLSS public static org.jivesoftware.smackx.packet.MUCUser$Item
 outer org.jivesoftware.smackx.packet.MUCUser
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getActor()
meth public java.lang.String getAffiliation()
meth public java.lang.String getJid()
meth public java.lang.String getNick()
meth public java.lang.String getReason()
meth public java.lang.String getRole()
meth public java.lang.String toXML()
meth public void setActor(java.lang.String)
meth public void setJid(java.lang.String)
meth public void setNick(java.lang.String)
meth public void setReason(java.lang.String)
supr java.lang.Object
hfds actor,affiliation,jid,nick,reason,role

CLSS public static org.jivesoftware.smackx.packet.MUCUser$Status
 outer org.jivesoftware.smackx.packet.MUCUser
cons public init(java.lang.String)
meth public java.lang.String getCode()
meth public java.lang.String toXML()
supr java.lang.Object
hfds code

CLSS public org.jivesoftware.smackx.packet.MessageEvent
cons public init()
fld public final static java.lang.String CANCELLED = "cancelled"
fld public final static java.lang.String COMPOSING = "composing"
fld public final static java.lang.String DELIVERED = "delivered"
fld public final static java.lang.String DISPLAYED = "displayed"
fld public final static java.lang.String OFFLINE = "offline"
intf org.jivesoftware.smack.packet.PacketExtension
meth public boolean isCancelled()
meth public boolean isComposing()
meth public boolean isDelivered()
meth public boolean isDisplayed()
meth public boolean isMessageEventRequest()
meth public boolean isOffline()
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getPacketID()
meth public java.lang.String toXML()
meth public java.util.Iterator getEventTypes()
meth public void setCancelled(boolean)
meth public void setComposing(boolean)
meth public void setDelivered(boolean)
meth public void setDisplayed(boolean)
meth public void setOffline(boolean)
meth public void setPacketID(java.lang.String)
supr java.lang.Object
hfds cancelled,composing,delivered,displayed,offline,packetID

CLSS public org.jivesoftware.smackx.packet.MultipleAddresses
cons public init()
fld public final static java.lang.String BCC = "bcc"
fld public final static java.lang.String CC = "cc"
fld public final static java.lang.String NO_REPLY = "noreply"
fld public final static java.lang.String REPLY_ROOM = "replyroom"
fld public final static java.lang.String REPLY_TO = "replyto"
fld public final static java.lang.String TO = "to"
innr public static Address
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String toXML()
meth public java.util.List getAddressesOfType(java.lang.String)
meth public void addAddress(java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean,java.lang.String)
meth public void setNoReply()
supr java.lang.Object
hfds addresses

CLSS public static org.jivesoftware.smackx.packet.MultipleAddresses$Address
 outer org.jivesoftware.smackx.packet.MultipleAddresses
meth public boolean isDelivered()
meth public java.lang.String getDescription()
meth public java.lang.String getJid()
meth public java.lang.String getNode()
meth public java.lang.String getType()
meth public java.lang.String getUri()
supr java.lang.Object
hfds delivered,description,jid,node,type,uri

CLSS public org.jivesoftware.smackx.packet.OfflineMessageInfo
cons public init()
innr public static Provider
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getNode()
meth public java.lang.String toXML()
meth public void setNode(java.lang.String)
supr java.lang.Object
hfds node

CLSS public static org.jivesoftware.smackx.packet.OfflineMessageInfo$Provider
 outer org.jivesoftware.smackx.packet.OfflineMessageInfo
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.packet.OfflineMessageRequest
cons public init()
innr public static Item
innr public static Provider
meth public boolean isFetch()
meth public boolean isPurge()
meth public java.lang.String getChildElementXML()
meth public java.util.Iterator getItems()
meth public void addItem(org.jivesoftware.smackx.packet.OfflineMessageRequest$Item)
meth public void setFetch(boolean)
meth public void setPurge(boolean)
supr org.jivesoftware.smack.packet.IQ
hfds fetch,items,purge

CLSS public static org.jivesoftware.smackx.packet.OfflineMessageRequest$Item
 outer org.jivesoftware.smackx.packet.OfflineMessageRequest
cons public init(java.lang.String)
meth public java.lang.String getAction()
meth public java.lang.String getJid()
meth public java.lang.String getNode()
meth public java.lang.String toXML()
meth public void setAction(java.lang.String)
meth public void setJid(java.lang.String)
supr java.lang.Object
hfds action,jid,node

CLSS public static org.jivesoftware.smackx.packet.OfflineMessageRequest$Provider
 outer org.jivesoftware.smackx.packet.OfflineMessageRequest
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.packet.PEPEvent
cons public init()
cons public init(org.jivesoftware.smackx.packet.PEPItem)
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String toXML()
meth public void addPEPItem(org.jivesoftware.smackx.packet.PEPItem)
supr java.lang.Object
hfds item

CLSS public abstract org.jivesoftware.smackx.packet.PEPItem
cons public init(java.lang.String)
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String toXML()
supr java.lang.Object
hfds id

CLSS public org.jivesoftware.smackx.packet.PEPPubSub
cons public init(org.jivesoftware.smackx.packet.PEPItem)
meth public java.lang.String getChildElementXML()
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
supr org.jivesoftware.smack.packet.IQ
hfds item

CLSS public abstract interface org.jivesoftware.smackx.packet.PrivateData
meth public abstract java.lang.String getElementName()
meth public abstract java.lang.String getNamespace()
meth public abstract java.lang.String toXML()

CLSS public org.jivesoftware.smackx.packet.RosterExchange
cons public init()
cons public init(org.jivesoftware.smack.Roster)
intf org.jivesoftware.smack.packet.PacketExtension
meth public int getEntryCount()
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String toXML()
meth public java.util.Iterator getRosterEntries()
meth public void addRosterEntry(org.jivesoftware.smack.RosterEntry)
meth public void addRosterEntry(org.jivesoftware.smackx.RemoteRosterEntry)
supr java.lang.Object
hfds remoteRosterEntries

CLSS public org.jivesoftware.smackx.packet.SharedGroupsInfo
cons public init()
innr public static Provider
meth public java.lang.String getChildElementXML()
meth public java.util.List getGroups()
supr org.jivesoftware.smack.packet.IQ
hfds groups

CLSS public static org.jivesoftware.smackx.packet.SharedGroupsInfo$Provider
 outer org.jivesoftware.smackx.packet.SharedGroupsInfo
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.packet.StreamInitiation
cons public init()
innr public Feature
innr public static File
meth public java.lang.String getChildElementXML()
meth public java.lang.String getMimeType()
meth public java.lang.String getSessionID()
meth public org.jivesoftware.smackx.packet.DataForm getFeatureNegotiationForm()
meth public org.jivesoftware.smackx.packet.StreamInitiation$File getFile()
meth public void setFeatureNegotiationForm(org.jivesoftware.smackx.packet.DataForm)
meth public void setFile(org.jivesoftware.smackx.packet.StreamInitiation$File)
meth public void setMimeType(java.lang.String)
meth public void setSesssionID(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds featureNegotiation,file,id,mimeType

CLSS public org.jivesoftware.smackx.packet.StreamInitiation$Feature
 outer org.jivesoftware.smackx.packet.StreamInitiation
cons public init(org.jivesoftware.smackx.packet.StreamInitiation,org.jivesoftware.smackx.packet.DataForm)
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String toXML()
meth public org.jivesoftware.smackx.packet.DataForm getData()
supr java.lang.Object
hfds data

CLSS public static org.jivesoftware.smackx.packet.StreamInitiation$File
 outer org.jivesoftware.smackx.packet.StreamInitiation
cons public init(java.lang.String,long)
intf org.jivesoftware.smack.packet.PacketExtension
meth public boolean isRanged()
meth public java.lang.String getDesc()
meth public java.lang.String getElementName()
meth public java.lang.String getHash()
meth public java.lang.String getName()
meth public java.lang.String getNamespace()
meth public java.lang.String toXML()
meth public java.util.Date getDate()
meth public long getSize()
meth public void setDate(java.util.Date)
meth public void setDesc(java.lang.String)
meth public void setHash(java.lang.String)
meth public void setRanged(boolean)
supr java.lang.Object
hfds date,desc,hash,isRanged,name,size

CLSS public org.jivesoftware.smackx.packet.Time
cons public init()
cons public init(java.util.Calendar)
meth public java.lang.String getChildElementXML()
meth public java.lang.String getDisplay()
meth public java.lang.String getTz()
meth public java.lang.String getUtc()
meth public java.util.Date getTime()
meth public void setDisplay(java.lang.String)
meth public void setTime(java.util.Date)
meth public void setTz(java.lang.String)
meth public void setUtc(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds display,displayFormat,tz,utc,utcFormat

CLSS public org.jivesoftware.smackx.packet.VCard
cons public init()
meth public boolean equals(java.lang.Object)
meth public byte[] getAvatar()
meth public int hashCode()
meth public java.lang.String getAddressFieldHome(java.lang.String)
meth public java.lang.String getAddressFieldWork(java.lang.String)
meth public java.lang.String getAvatarHash()
meth public java.lang.String getChildElementXML()
meth public java.lang.String getEmailHome()
meth public java.lang.String getEmailWork()
meth public java.lang.String getField(java.lang.String)
meth public java.lang.String getFirstName()
meth public java.lang.String getJabberId()
meth public java.lang.String getLastName()
meth public java.lang.String getMiddleName()
meth public java.lang.String getNickName()
meth public java.lang.String getOrganization()
meth public java.lang.String getOrganizationUnit()
meth public java.lang.String getPhoneHome(java.lang.String)
meth public java.lang.String getPhoneWork(java.lang.String)
meth public java.lang.String toString()
meth public static byte[] getBytes(java.net.URL) throws java.io.IOException
meth public void load(org.jivesoftware.smack.XMPPConnection) throws org.jivesoftware.smack.XMPPException
meth public void load(org.jivesoftware.smack.XMPPConnection,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void save(org.jivesoftware.smack.XMPPConnection) throws org.jivesoftware.smack.XMPPException
meth public void setAddressFieldHome(java.lang.String,java.lang.String)
meth public void setAddressFieldWork(java.lang.String,java.lang.String)
meth public void setAvatar(byte[])
meth public void setAvatar(byte[],java.lang.String)
meth public void setAvatar(java.net.URL)
meth public void setEmailHome(java.lang.String)
meth public void setEmailWork(java.lang.String)
meth public void setEncodedImage(java.lang.String)
meth public void setField(java.lang.String,java.lang.String)
meth public void setField(java.lang.String,java.lang.String,boolean)
meth public void setFirstName(java.lang.String)
meth public void setJabberId(java.lang.String)
meth public void setLastName(java.lang.String)
meth public void setMiddleName(java.lang.String)
meth public void setNickName(java.lang.String)
meth public void setOrganization(java.lang.String)
meth public void setOrganizationUnit(java.lang.String)
meth public void setPhoneHome(java.lang.String,java.lang.String)
meth public void setPhoneWork(java.lang.String,java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds avatar,emailHome,emailWork,firstName,homeAddr,homePhones,lastName,middleName,organization,organizationUnit,otherSimpleFields,otherUnescapableFields,workAddr,workPhones
hcls ContentBuilder,VCardWriter

CLSS public org.jivesoftware.smackx.packet.Version
cons public init()
meth public java.lang.String getChildElementXML()
meth public java.lang.String getName()
meth public java.lang.String getOs()
meth public java.lang.String getVersion()
meth public void setName(java.lang.String)
meth public void setOs(java.lang.String)
meth public void setVersion(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds name,os,version

CLSS public org.jivesoftware.smackx.packet.XHTMLExtension
cons public init()
intf org.jivesoftware.smack.packet.PacketExtension
meth public int getBodiesCount()
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String toXML()
meth public java.util.Iterator getBodies()
meth public void addBody(java.lang.String)
supr java.lang.Object
hfds bodies

CLSS public org.jivesoftware.smackx.provider.AdHocCommandDataProvider
cons public init()
innr public static BadActionError
innr public static BadLocaleError
innr public static BadPayloadError
innr public static BadSessionIDError
innr public static MalformedActionError
innr public static SessionExpiredError
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public static org.jivesoftware.smackx.provider.AdHocCommandDataProvider$BadActionError
 outer org.jivesoftware.smackx.provider.AdHocCommandDataProvider
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public static org.jivesoftware.smackx.provider.AdHocCommandDataProvider$BadLocaleError
 outer org.jivesoftware.smackx.provider.AdHocCommandDataProvider
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public static org.jivesoftware.smackx.provider.AdHocCommandDataProvider$BadPayloadError
 outer org.jivesoftware.smackx.provider.AdHocCommandDataProvider
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public static org.jivesoftware.smackx.provider.AdHocCommandDataProvider$BadSessionIDError
 outer org.jivesoftware.smackx.provider.AdHocCommandDataProvider
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public static org.jivesoftware.smackx.provider.AdHocCommandDataProvider$MalformedActionError
 outer org.jivesoftware.smackx.provider.AdHocCommandDataProvider
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public static org.jivesoftware.smackx.provider.AdHocCommandDataProvider$SessionExpiredError
 outer org.jivesoftware.smackx.provider.AdHocCommandDataProvider
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.provider.BytestreamsProvider
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.provider.DataFormProvider
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.provider.DelayInformationProvider
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.provider.DiscoverInfoProvider
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.provider.DiscoverItemsProvider
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.provider.IBBProviders
cons public init()
innr public static Close
innr public static Data
innr public static Open
supr java.lang.Object

CLSS public static org.jivesoftware.smackx.provider.IBBProviders$Close
 outer org.jivesoftware.smackx.provider.IBBProviders
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public static org.jivesoftware.smackx.provider.IBBProviders$Data
 outer org.jivesoftware.smackx.provider.IBBProviders
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public static org.jivesoftware.smackx.provider.IBBProviders$Open
 outer org.jivesoftware.smackx.provider.IBBProviders
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.provider.MUCAdminProvider
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.provider.MUCOwnerProvider
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.provider.MUCUserProvider
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.provider.MessageEventProvider
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.provider.MultipleAddressesProvider
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.provider.PEPProvider
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
meth public void registerPEPParserExtension(java.lang.String,org.jivesoftware.smack.provider.PacketExtensionProvider)
supr java.lang.Object
hfds nodeParsers,pepItem

CLSS public abstract interface org.jivesoftware.smackx.provider.PrivateDataProvider
meth public abstract org.jivesoftware.smackx.packet.PrivateData parsePrivateData(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception

CLSS public org.jivesoftware.smackx.provider.RosterExchangeProvider
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.provider.StreamInitiationProvider
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.provider.VCardProvider
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
meth public static org.jivesoftware.smackx.packet.VCard createVCardFromXML(java.lang.String) throws java.lang.Exception
supr java.lang.Object
hfds PREFERRED_ENCODING
hcls VCardReader

CLSS public org.jivesoftware.smackx.provider.XHTMLExtensionProvider
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.search.UserSearch
cons public init()
innr public static Provider
meth public java.lang.String getChildElementXML()
meth public org.jivesoftware.smackx.Form getSearchForm(org.jivesoftware.smack.XMPPConnection,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.ReportedData sendSearchForm(org.jivesoftware.smack.XMPPConnection,org.jivesoftware.smackx.Form,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.ReportedData sendSimpleSearchForm(org.jivesoftware.smack.XMPPConnection,org.jivesoftware.smackx.Form,java.lang.String) throws org.jivesoftware.smack.XMPPException
supr org.jivesoftware.smack.packet.IQ

CLSS public static org.jivesoftware.smackx.search.UserSearch$Provider
 outer org.jivesoftware.smackx.search.UserSearch
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.search.UserSearchManager
cons public init(org.jivesoftware.smack.XMPPConnection)
meth public java.util.Collection getSearchServices() throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.Form getSearchForm(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.ReportedData getSearchResults(org.jivesoftware.smackx.Form,java.lang.String) throws org.jivesoftware.smack.XMPPException
supr java.lang.Object
hfds con,userSearch

CLSS public org.jivesoftware.smackx.workgroup.MetaData
cons public init(java.util.Map)
fld public final static java.lang.String ELEMENT_NAME = "metadata"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String toXML()
meth public java.util.Map getMetaData()
supr java.lang.Object
hfds metaData

CLSS public org.jivesoftware.smackx.workgroup.QueueUser
cons public init(java.lang.String,int,int,java.util.Date)
meth public int getEstimatedRemainingTime()
meth public int getQueuePosition()
meth public java.lang.String getUserID()
meth public java.util.Date getQueueJoinTimestamp()
supr java.lang.Object
hfds estimatedTime,joinDate,queuePosition,userID

CLSS public org.jivesoftware.smackx.workgroup.WorkgroupInvitation
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.util.Map)
fld protected java.lang.String groupChatName
fld protected java.lang.String invitationSender
fld protected java.lang.String issuingWorkgroupName
fld protected java.lang.String messageBody
fld protected java.lang.String sessionID
fld protected java.lang.String uniqueID
fld protected java.util.Map metaData
meth public java.lang.String getGroupChatName()
meth public java.lang.String getInvitationSender()
meth public java.lang.String getMessageBody()
meth public java.lang.String getSessionID()
meth public java.lang.String getUniqueID()
meth public java.lang.String getWorkgroupName()
meth public java.util.Map getMetaData()
supr java.lang.Object

CLSS public abstract interface org.jivesoftware.smackx.workgroup.WorkgroupInvitationListener
meth public abstract void invitationReceived(org.jivesoftware.smackx.workgroup.WorkgroupInvitation)

CLSS public org.jivesoftware.smackx.workgroup.agent.Agent
meth public java.lang.String getName() throws org.jivesoftware.smack.XMPPException
meth public java.lang.String getUser()
meth public static java.util.Collection getWorkgroups(java.lang.String,java.lang.String,org.jivesoftware.smack.XMPPConnection) throws org.jivesoftware.smack.XMPPException
meth public void setName(java.lang.String) throws org.jivesoftware.smack.XMPPException
supr java.lang.Object
hfds connection,workgroupJID

CLSS public org.jivesoftware.smackx.workgroup.agent.AgentRoster
meth public boolean contains(java.lang.String)
meth public int getAgentCount()
meth public java.util.Set getAgents()
meth public org.jivesoftware.smack.packet.Presence getPresence(java.lang.String)
meth public void addListener(org.jivesoftware.smackx.workgroup.agent.AgentRosterListener)
meth public void reload()
meth public void removeListener(org.jivesoftware.smackx.workgroup.agent.AgentRosterListener)
supr java.lang.Object
hfds EVENT_AGENT_ADDED,EVENT_AGENT_REMOVED,EVENT_PRESENCE_CHANGED,connection,entries,listeners,presenceMap,rosterInitialized,workgroupJID
hcls AgentStatusListener,PresencePacketListener

CLSS public abstract interface org.jivesoftware.smackx.workgroup.agent.AgentRosterListener
meth public abstract void agentAdded(java.lang.String)
meth public abstract void agentRemoved(java.lang.String)
meth public abstract void presenceChanged(org.jivesoftware.smack.packet.Presence)

CLSS public org.jivesoftware.smackx.workgroup.agent.AgentSession
cons public init(java.lang.String,org.jivesoftware.smack.XMPPConnection)
meth public boolean hasMonitorPrivileges(org.jivesoftware.smack.XMPPConnection) throws org.jivesoftware.smack.XMPPException
meth public boolean isOnline()
meth public int getMaxChats()
meth public java.lang.String getMetaData(java.lang.String)
meth public java.lang.String getWorkgroupJID()
meth public java.util.Iterator<org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue> getQueues()
meth public java.util.Map getChatMetadata(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smack.packet.Presence$Mode getPresenceMode()
meth public org.jivesoftware.smackx.Form getTranscriptSearchForm() throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.ReportedData searchTranscripts(org.jivesoftware.smackx.Form) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.workgroup.agent.Agent getAgent()
meth public org.jivesoftware.smackx.workgroup.agent.AgentRoster getAgentRoster()
meth public org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue getQueue(java.lang.String)
meth public org.jivesoftware.smackx.workgroup.ext.history.AgentChatHistory getAgentHistory(java.lang.String,int,java.util.Date) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.workgroup.ext.macros.MacroGroup getMacros(boolean) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.workgroup.ext.notes.ChatNotes getNote(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.workgroup.packet.OccupantsInfo getOccupantsInfo(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.workgroup.packet.Transcript getTranscript(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.workgroup.packet.Transcripts getTranscripts(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.workgroup.settings.GenericSettings getGenericSettings(org.jivesoftware.smack.XMPPConnection,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.workgroup.settings.SearchSettings getSearchSettings() throws org.jivesoftware.smack.XMPPException
meth public void addInvitationListener(org.jivesoftware.smackx.workgroup.WorkgroupInvitationListener)
meth public void addOfferListener(org.jivesoftware.smackx.workgroup.agent.OfferListener)
meth public void addQueueUsersListener(org.jivesoftware.smackx.workgroup.agent.QueueUsersListener)
meth public void close()
meth public void dequeueUser(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void makeRoomOwner(org.jivesoftware.smack.XMPPConnection,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void removeInvitationListener(org.jivesoftware.smackx.workgroup.WorkgroupInvitationListener)
meth public void removeMetaData(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void removeOfferListener(org.jivesoftware.smackx.workgroup.agent.OfferListener)
meth public void removeQueueUsersListener(org.jivesoftware.smackx.workgroup.agent.QueueUsersListener)
meth public void saveMacros(org.jivesoftware.smackx.workgroup.ext.macros.MacroGroup) throws org.jivesoftware.smack.XMPPException
meth public void sendRoomInvitation(org.jivesoftware.smackx.workgroup.packet.RoomInvitation$Type,java.lang.String,java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void sendRoomTransfer(org.jivesoftware.smackx.workgroup.packet.RoomTransfer$Type,java.lang.String,java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void setMetaData(java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void setNote(java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void setOnline(boolean) throws org.jivesoftware.smack.XMPPException
meth public void setStatus(org.jivesoftware.smack.packet.Presence$Mode,int) throws org.jivesoftware.smack.XMPPException
meth public void setStatus(org.jivesoftware.smack.packet.Presence$Mode,int,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void setStatus(org.jivesoftware.smack.packet.Presence$Mode,java.lang.String) throws org.jivesoftware.smack.XMPPException
supr java.lang.Object
hfds agent,agentRoster,connection,invitationListeners,maxChats,metaData,offerListeners,online,packetListener,presenceMode,queueUsersListeners,queues,transcriptManager,transcriptSearchManager,workgroupJID

CLSS public org.jivesoftware.smackx.workgroup.agent.InvitationRequest
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getInviter()
meth public java.lang.String getReason()
meth public java.lang.String getRoom()
supr org.jivesoftware.smackx.workgroup.agent.OfferContent
hfds inviter,reason,room

CLSS public org.jivesoftware.smackx.workgroup.agent.Offer
meth public boolean isAccepted()
meth public boolean isRejected()
meth public java.lang.String getSessionID()
meth public java.lang.String getUserID()
meth public java.lang.String getUserJID()
meth public java.lang.String getWorkgroupName()
meth public java.util.Date getExpiresDate()
meth public java.util.Map getMetaData()
meth public org.jivesoftware.smackx.workgroup.agent.OfferContent getContent()
meth public void accept()
meth public void reject()
supr java.lang.Object
hfds accepted,connection,content,expiresDate,metaData,rejected,session,sessionID,userID,userJID,workgroupName
hcls AcceptPacket,RejectPacket

CLSS public org.jivesoftware.smackx.workgroup.agent.OfferConfirmation
cons public init()
innr public static Provider
meth public java.lang.String getChildElementXML()
meth public java.lang.String getUserJID()
meth public long getSessionID()
meth public void notifyService(org.jivesoftware.smack.XMPPConnection,java.lang.String,java.lang.String)
meth public void setSessionID(long)
meth public void setUserJID(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds sessionID,userJID
hcls NotifyServicePacket

CLSS public static org.jivesoftware.smackx.workgroup.agent.OfferConfirmation$Provider
 outer org.jivesoftware.smackx.workgroup.agent.OfferConfirmation
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public abstract interface org.jivesoftware.smackx.workgroup.agent.OfferConfirmationListener
meth public abstract void offerConfirmed(org.jivesoftware.smackx.workgroup.agent.OfferConfirmation)

CLSS public abstract org.jivesoftware.smackx.workgroup.agent.OfferContent
cons public init()
supr java.lang.Object

CLSS public abstract interface org.jivesoftware.smackx.workgroup.agent.OfferListener
meth public abstract void offerReceived(org.jivesoftware.smackx.workgroup.agent.Offer)
meth public abstract void offerRevoked(org.jivesoftware.smackx.workgroup.agent.RevokedOffer)

CLSS public abstract interface org.jivesoftware.smackx.workgroup.agent.QueueUsersListener
meth public abstract void averageWaitTimeUpdated(org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue,int)
meth public abstract void oldestEntryUpdated(org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue,java.util.Date)
meth public abstract void statusUpdated(org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue,org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue$Status)
meth public abstract void usersUpdated(org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue,java.util.Set)

CLSS public org.jivesoftware.smackx.workgroup.agent.RevokedOffer
meth public java.lang.String getReason()
meth public java.lang.String getSessionID()
meth public java.lang.String getUserID()
meth public java.lang.String getUserJID()
meth public java.lang.String getWorkgroupName()
meth public java.util.Date getTimestamp()
supr java.lang.Object
hfds reason,sessionID,timestamp,userID,userJID,workgroupName

CLSS public org.jivesoftware.smackx.workgroup.agent.TranscriptManager
cons public init(org.jivesoftware.smack.XMPPConnection)
meth public org.jivesoftware.smackx.workgroup.packet.Transcript getTranscript(java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.workgroup.packet.Transcripts getTranscripts(java.lang.String,java.lang.String) throws org.jivesoftware.smack.XMPPException
supr java.lang.Object
hfds connection

CLSS public org.jivesoftware.smackx.workgroup.agent.TranscriptSearchManager
cons public init(org.jivesoftware.smack.XMPPConnection)
meth public org.jivesoftware.smackx.Form getSearchForm(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.ReportedData submitSearch(java.lang.String,org.jivesoftware.smackx.Form) throws org.jivesoftware.smack.XMPPException
supr java.lang.Object
hfds connection

CLSS public org.jivesoftware.smackx.workgroup.agent.TransferRequest
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getInviter()
meth public java.lang.String getReason()
meth public java.lang.String getRoom()
supr org.jivesoftware.smackx.workgroup.agent.OfferContent
hfds inviter,reason,room

CLSS public org.jivesoftware.smackx.workgroup.agent.UserRequest
cons public init()
meth public static org.jivesoftware.smackx.workgroup.agent.OfferContent getInstance()
supr org.jivesoftware.smackx.workgroup.agent.OfferContent
hfds instance

CLSS public org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue
innr public static Status
meth public int getAverageWaitTime()
meth public int getCurrentChats()
meth public int getMaxChats()
meth public int getUserCount()
meth public java.lang.String getName()
meth public java.util.Date getOldestEntry()
meth public java.util.Iterator getUsers()
meth public org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue$Status getStatus()
supr java.lang.Object
hfds averageWaitTime,currentChats,maxChats,name,oldestEntry,status,users

CLSS public static org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue$Status
 outer org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue
fld public final static org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue$Status ACTIVE
fld public final static org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue$Status CLOSED
fld public final static org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue$Status OPEN
meth public java.lang.String toString()
meth public static org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue$Status fromString(java.lang.String)
supr java.lang.Object
hfds value

CLSS public org.jivesoftware.smackx.workgroup.ext.forms.WorkgroupForm
cons public init()
fld public final static java.lang.String ELEMENT_NAME = "workgroup-form"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static InternalProvider
meth public java.lang.String getChildElementXML()
supr org.jivesoftware.smack.packet.IQ

CLSS public static org.jivesoftware.smackx.workgroup.ext.forms.WorkgroupForm$InternalProvider
 outer org.jivesoftware.smackx.workgroup.ext.forms.WorkgroupForm
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.ext.history.AgentChatHistory
cons public init()
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,java.util.Date)
fld public final static java.lang.String ELEMENT_NAME = "chat-sessions"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static InternalProvider
meth public java.lang.String getChildElementXML()
meth public java.util.Collection getAgentChatSessions()
meth public void addChatSession(org.jivesoftware.smackx.workgroup.ext.history.AgentChatSession)
supr org.jivesoftware.smack.packet.IQ
hfds agentChatSessions,agentJID,maxSessions,startDate

CLSS public static org.jivesoftware.smackx.workgroup.ext.history.AgentChatHistory$InternalProvider
 outer org.jivesoftware.smackx.workgroup.ext.history.AgentChatHistory
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.ext.history.AgentChatSession
cons public init(java.util.Date,long,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
fld public java.lang.String question
fld public java.lang.String sessionID
fld public java.lang.String visitorsEmail
fld public java.lang.String visitorsName
fld public java.util.Date startDate
fld public long duration
meth public java.lang.String getQuestion()
meth public java.lang.String getSessionID()
meth public java.lang.String getVisitorsEmail()
meth public java.lang.String getVisitorsName()
meth public java.util.Date getStartDate()
meth public long getDuration()
meth public void setDuration(long)
meth public void setQuestion(java.lang.String)
meth public void setSessionID(java.lang.String)
meth public void setStartDate(java.util.Date)
meth public void setVisitorsEmail(java.lang.String)
meth public void setVisitorsName(java.lang.String)
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.ext.history.ChatMetadata
cons public init()
fld public final static java.lang.String ELEMENT_NAME = "chat-metadata"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static Provider
meth public java.lang.String getChildElementXML()
meth public java.lang.String getSessionID()
meth public java.util.Map getMetadata()
meth public void setMetadata(java.util.Map)
meth public void setSessionID(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds map,sessionID

CLSS public static org.jivesoftware.smackx.workgroup.ext.history.ChatMetadata$Provider
 outer org.jivesoftware.smackx.workgroup.ext.history.ChatMetadata
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.ext.macros.Macro
cons public init()
fld public final static int IMAGE = 2
fld public final static int TEXT = 0
fld public final static int URL = 1
meth public int getType()
meth public java.lang.String getDescription()
meth public java.lang.String getResponse()
meth public java.lang.String getTitle()
meth public void setDescription(java.lang.String)
meth public void setResponse(java.lang.String)
meth public void setTitle(java.lang.String)
meth public void setType(int)
supr java.lang.Object
hfds description,response,title,type

CLSS public org.jivesoftware.smackx.workgroup.ext.macros.MacroGroup
cons public init()
meth public java.lang.String getTitle()
meth public java.util.List getMacroGroups()
meth public java.util.List getMacros()
meth public org.jivesoftware.smackx.workgroup.ext.macros.Macro getMacro(int)
meth public org.jivesoftware.smackx.workgroup.ext.macros.Macro getMacroByTitle(java.lang.String)
meth public org.jivesoftware.smackx.workgroup.ext.macros.MacroGroup getMacroGroup(int)
meth public org.jivesoftware.smackx.workgroup.ext.macros.MacroGroup getMacroGroupByTitle(java.lang.String)
meth public void addMacro(org.jivesoftware.smackx.workgroup.ext.macros.Macro)
meth public void addMacroGroup(org.jivesoftware.smackx.workgroup.ext.macros.MacroGroup)
meth public void removeMacro(org.jivesoftware.smackx.workgroup.ext.macros.Macro)
meth public void removeMacroGroup(org.jivesoftware.smackx.workgroup.ext.macros.MacroGroup)
meth public void setMacroGroups(java.util.List)
meth public void setMacros(java.util.List)
meth public void setTitle(java.lang.String)
supr java.lang.Object
hfds macroGroups,macros,title

CLSS public org.jivesoftware.smackx.workgroup.ext.macros.Macros
cons public init()
fld public final static java.lang.String ELEMENT_NAME = "macros"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static InternalProvider
meth public boolean isPersonal()
meth public java.lang.String getChildElementXML()
meth public org.jivesoftware.smackx.workgroup.ext.macros.MacroGroup getPersonalMacroGroup()
meth public org.jivesoftware.smackx.workgroup.ext.macros.MacroGroup getRootGroup()
meth public static void setClassLoader(java.lang.ClassLoader)
meth public void setPersonal(boolean)
meth public void setPersonalMacroGroup(org.jivesoftware.smackx.workgroup.ext.macros.MacroGroup)
meth public void setRootGroup(org.jivesoftware.smackx.workgroup.ext.macros.MacroGroup)
supr org.jivesoftware.smack.packet.IQ
hfds cl,personal,personalMacroGroup,rootGroup

CLSS public static org.jivesoftware.smackx.workgroup.ext.macros.Macros$InternalProvider
 outer org.jivesoftware.smackx.workgroup.ext.macros.Macros
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.ext.notes.ChatNotes
cons public init()
fld public final static java.lang.String ELEMENT_NAME = "chat-notes"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static Provider
meth public final static java.lang.String replace(java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getChildElementXML()
meth public java.lang.String getNotes()
meth public java.lang.String getSessionID()
meth public void setNotes(java.lang.String)
meth public void setSessionID(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds notes,sessionID

CLSS public static org.jivesoftware.smackx.workgroup.ext.notes.ChatNotes$Provider
 outer org.jivesoftware.smackx.workgroup.ext.notes.ChatNotes
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.packet.AgentInfo
cons public init()
fld public final static java.lang.String ELEMENT_NAME = "agent-info"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static Provider
meth public java.lang.String getChildElementXML()
meth public java.lang.String getJid()
meth public java.lang.String getName()
meth public void setJid(java.lang.String)
meth public void setName(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds jid,name

CLSS public static org.jivesoftware.smackx.workgroup.packet.AgentInfo$Provider
 outer org.jivesoftware.smackx.workgroup.packet.AgentInfo
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.packet.AgentStatus
fld public final static java.lang.String ELEMENT_NAME = "agent-status"
fld public final static java.lang.String NAMESPACE = "http://jabber.org/protocol/workgroup"
innr public static ChatInfo
innr public static Provider
intf org.jivesoftware.smack.packet.PacketExtension
meth public int getMaxChats()
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getWorkgroupJID()
meth public java.lang.String toXML()
meth public java.util.List getCurrentChats()
supr java.lang.Object
hfds UTC_FORMAT,currentChats,maxChats,workgroupJID

CLSS public static org.jivesoftware.smackx.workgroup.packet.AgentStatus$ChatInfo
 outer org.jivesoftware.smackx.workgroup.packet.AgentStatus
cons public init(java.lang.String,java.lang.String,java.util.Date,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getEmail()
meth public java.lang.String getQuestion()
meth public java.lang.String getSessionID()
meth public java.lang.String getUserID()
meth public java.lang.String getUsername()
meth public java.lang.String toXML()
meth public java.util.Date getDate()
supr java.lang.Object
hfds date,email,question,sessionID,userID,username

CLSS public static org.jivesoftware.smackx.workgroup.packet.AgentStatus$Provider
 outer org.jivesoftware.smackx.workgroup.packet.AgentStatus
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.packet.AgentStatusRequest
cons public init()
fld public final static java.lang.String ELEMENT_NAME = "agent-status-request"
fld public final static java.lang.String NAMESPACE = "http://jabber.org/protocol/workgroup"
innr public static Item
innr public static Provider
meth public int getAgentCount()
meth public java.lang.String getChildElementXML()
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.util.Set getAgents()
supr org.jivesoftware.smack.packet.IQ
hfds agents

CLSS public static org.jivesoftware.smackx.workgroup.packet.AgentStatusRequest$Item
 outer org.jivesoftware.smackx.workgroup.packet.AgentStatusRequest
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getJID()
meth public java.lang.String getName()
meth public java.lang.String getType()
supr java.lang.Object
hfds jid,name,type

CLSS public static org.jivesoftware.smackx.workgroup.packet.AgentStatusRequest$Provider
 outer org.jivesoftware.smackx.workgroup.packet.AgentStatusRequest
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.packet.AgentWorkgroups
cons public init(java.lang.String)
cons public init(java.lang.String,java.util.List)
innr public static Provider
meth public java.lang.String getAgentJID()
meth public java.lang.String getChildElementXML()
meth public java.util.List getWorkgroups()
supr org.jivesoftware.smack.packet.IQ
hfds agentJID,workgroups

CLSS public static org.jivesoftware.smackx.workgroup.packet.AgentWorkgroups$Provider
 outer org.jivesoftware.smackx.workgroup.packet.AgentWorkgroups
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.packet.DepartQueuePacket
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getChildElementXML()
supr org.jivesoftware.smack.packet.IQ
hfds user

CLSS public org.jivesoftware.smackx.workgroup.packet.MetaDataProvider
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.packet.MonitorPacket
cons public init()
fld public final static java.lang.String ELEMENT_NAME = "monitor"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static InternalProvider
meth public boolean isMonitor()
meth public java.lang.String getChildElementXML()
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getSessionID()
meth public void setMonitor(boolean)
meth public void setSessionID(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds isMonitor,sessionID

CLSS public static org.jivesoftware.smackx.workgroup.packet.MonitorPacket$InternalProvider
 outer org.jivesoftware.smackx.workgroup.packet.MonitorPacket
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.packet.OccupantsInfo
cons public init(java.lang.String)
fld public final static java.lang.String ELEMENT_NAME = "occupants-info"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static OccupantInfo
innr public static Provider
meth public int getOccupantsCount()
meth public java.lang.String getChildElementXML()
meth public java.lang.String getRoomID()
meth public java.util.Set<org.jivesoftware.smackx.workgroup.packet.OccupantsInfo$OccupantInfo> getOccupants()
supr org.jivesoftware.smack.packet.IQ
hfds UTC_FORMAT,occupants,roomID

CLSS public static org.jivesoftware.smackx.workgroup.packet.OccupantsInfo$OccupantInfo
 outer org.jivesoftware.smackx.workgroup.packet.OccupantsInfo
cons public init(java.lang.String,java.lang.String,java.util.Date)
meth public java.lang.String getJID()
meth public java.lang.String getNickname()
meth public java.util.Date getJoined()
supr java.lang.Object
hfds jid,joined,nickname

CLSS public static org.jivesoftware.smackx.workgroup.packet.OccupantsInfo$Provider
 outer org.jivesoftware.smackx.workgroup.packet.OccupantsInfo
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.packet.OfferRequestProvider
cons public init()
innr public static OfferRequestPacket
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public static org.jivesoftware.smackx.workgroup.packet.OfferRequestProvider$OfferRequestPacket
 outer org.jivesoftware.smackx.workgroup.packet.OfferRequestProvider
cons public init(java.lang.String,java.lang.String,int,java.util.Map,java.lang.String,org.jivesoftware.smackx.workgroup.agent.OfferContent)
meth public int getTimeout()
meth public java.lang.String getChildElementXML()
meth public java.lang.String getSessionID()
meth public java.lang.String getUserID()
meth public java.lang.String getUserJID()
meth public java.util.Map getMetaData()
meth public org.jivesoftware.smackx.workgroup.agent.OfferContent getContent()
supr org.jivesoftware.smack.packet.IQ
hfds content,metaData,sessionID,timeout,userID,userJID

CLSS public org.jivesoftware.smackx.workgroup.packet.OfferRevokeProvider
cons public init()
innr public OfferRevokePacket
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.packet.OfferRevokeProvider$OfferRevokePacket
 outer org.jivesoftware.smackx.workgroup.packet.OfferRevokeProvider
cons public init(org.jivesoftware.smackx.workgroup.packet.OfferRevokeProvider,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getChildElementXML()
meth public java.lang.String getReason()
meth public java.lang.String getSessionID()
meth public java.lang.String getUserID()
meth public java.lang.String getUserJID()
supr org.jivesoftware.smack.packet.IQ
hfds reason,sessionID,userID,userJID

CLSS public org.jivesoftware.smackx.workgroup.packet.QueueDetails
fld public final static java.lang.String ELEMENT_NAME = "notify-queue-details"
fld public final static java.lang.String NAMESPACE = "http://jabber.org/protocol/workgroup"
innr public static Provider
intf org.jivesoftware.smack.packet.PacketExtension
meth public int getUserCount()
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String toXML()
meth public java.util.Set getUsers()
supr java.lang.Object
hfds DATE_FORMATTER,users

CLSS public static org.jivesoftware.smackx.workgroup.packet.QueueDetails$Provider
 outer org.jivesoftware.smackx.workgroup.packet.QueueDetails
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.packet.QueueOverview
fld public static java.lang.String ELEMENT_NAME
fld public static java.lang.String NAMESPACE
innr public static Provider
intf org.jivesoftware.smack.packet.PacketExtension
meth public int getAverageWaitTime()
meth public int getUserCount()
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String toXML()
meth public java.util.Date getOldestEntry()
meth public org.jivesoftware.smackx.workgroup.agent.WorkgroupQueue$Status getStatus()
supr java.lang.Object
hfds DATE_FORMATTER,averageWaitTime,oldestEntry,status,userCount

CLSS public static org.jivesoftware.smackx.workgroup.packet.QueueOverview$Provider
 outer org.jivesoftware.smackx.workgroup.packet.QueueOverview
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.packet.QueueUpdate
cons public init(int,int)
fld public final static java.lang.String ELEMENT_NAME = "queue-status"
fld public final static java.lang.String NAMESPACE = "http://jabber.org/protocol/workgroup"
innr public static Provider
intf org.jivesoftware.smack.packet.PacketExtension
meth public int getPosition()
meth public int getRemaingTime()
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String toXML()
supr java.lang.Object
hfds position,remainingTime

CLSS public static org.jivesoftware.smackx.workgroup.packet.QueueUpdate$Provider
 outer org.jivesoftware.smackx.workgroup.packet.QueueUpdate
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.packet.RoomInvitation
cons public init(org.jivesoftware.smackx.workgroup.packet.RoomInvitation$Type,java.lang.String,java.lang.String,java.lang.String)
fld public final static java.lang.String ELEMENT_NAME = "invite"
fld public final static java.lang.String NAMESPACE = "http://jabber.org/protocol/workgroup"
innr public final static !enum Type
innr public static Provider
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getInviter()
meth public java.lang.String getNamespace()
meth public java.lang.String getReason()
meth public java.lang.String getRoom()
meth public java.lang.String getSessionID()
meth public java.lang.String toXML()
supr java.lang.Object
hfds invitee,inviter,reason,room,sessionID,type

CLSS public static org.jivesoftware.smackx.workgroup.packet.RoomInvitation$Provider
 outer org.jivesoftware.smackx.workgroup.packet.RoomInvitation
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public final static !enum org.jivesoftware.smackx.workgroup.packet.RoomInvitation$Type
 outer org.jivesoftware.smackx.workgroup.packet.RoomInvitation
fld public final static org.jivesoftware.smackx.workgroup.packet.RoomInvitation$Type queue
fld public final static org.jivesoftware.smackx.workgroup.packet.RoomInvitation$Type user
fld public final static org.jivesoftware.smackx.workgroup.packet.RoomInvitation$Type workgroup
meth public final static org.jivesoftware.smackx.workgroup.packet.RoomInvitation$Type[] values()
meth public static org.jivesoftware.smackx.workgroup.packet.RoomInvitation$Type valueOf(java.lang.String)
supr java.lang.Enum<org.jivesoftware.smackx.workgroup.packet.RoomInvitation$Type>

CLSS public org.jivesoftware.smackx.workgroup.packet.RoomTransfer
cons public init(org.jivesoftware.smackx.workgroup.packet.RoomTransfer$Type,java.lang.String,java.lang.String,java.lang.String)
fld public final static java.lang.String ELEMENT_NAME = "transfer"
fld public final static java.lang.String NAMESPACE = "http://jabber.org/protocol/workgroup"
innr public final static !enum Type
innr public static Provider
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getInviter()
meth public java.lang.String getNamespace()
meth public java.lang.String getReason()
meth public java.lang.String getRoom()
meth public java.lang.String getSessionID()
meth public java.lang.String toXML()
supr java.lang.Object
hfds invitee,inviter,reason,room,sessionID,type

CLSS public static org.jivesoftware.smackx.workgroup.packet.RoomTransfer$Provider
 outer org.jivesoftware.smackx.workgroup.packet.RoomTransfer
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public final static !enum org.jivesoftware.smackx.workgroup.packet.RoomTransfer$Type
 outer org.jivesoftware.smackx.workgroup.packet.RoomTransfer
fld public final static org.jivesoftware.smackx.workgroup.packet.RoomTransfer$Type queue
fld public final static org.jivesoftware.smackx.workgroup.packet.RoomTransfer$Type user
fld public final static org.jivesoftware.smackx.workgroup.packet.RoomTransfer$Type workgroup
meth public final static org.jivesoftware.smackx.workgroup.packet.RoomTransfer$Type[] values()
meth public static org.jivesoftware.smackx.workgroup.packet.RoomTransfer$Type valueOf(java.lang.String)
supr java.lang.Enum<org.jivesoftware.smackx.workgroup.packet.RoomTransfer$Type>

CLSS public org.jivesoftware.smackx.workgroup.packet.SessionID
cons public init(java.lang.String)
fld public final static java.lang.String ELEMENT_NAME = "session"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static Provider
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getSessionID()
meth public java.lang.String toXML()
supr java.lang.Object
hfds sessionID

CLSS public static org.jivesoftware.smackx.workgroup.packet.SessionID$Provider
 outer org.jivesoftware.smackx.workgroup.packet.SessionID
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.packet.Transcript
cons public init(java.lang.String)
cons public init(java.lang.String,java.util.List)
meth public java.lang.String getChildElementXML()
meth public java.lang.String getSessionID()
meth public java.util.List getPackets()
supr org.jivesoftware.smack.packet.IQ
hfds packets,sessionID

CLSS public org.jivesoftware.smackx.workgroup.packet.TranscriptProvider
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.packet.TranscriptSearch
cons public init()
fld public final static java.lang.String ELEMENT_NAME = "transcript-search"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static Provider
meth public java.lang.String getChildElementXML()
supr org.jivesoftware.smack.packet.IQ

CLSS public static org.jivesoftware.smackx.workgroup.packet.TranscriptSearch$Provider
 outer org.jivesoftware.smackx.workgroup.packet.TranscriptSearch
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.packet.Transcripts
cons public init(java.lang.String)
cons public init(java.lang.String,java.util.List<org.jivesoftware.smackx.workgroup.packet.Transcripts$TranscriptSummary>)
innr public static AgentDetail
innr public static TranscriptSummary
meth public java.lang.String getChildElementXML()
meth public java.lang.String getUserID()
meth public java.util.List<org.jivesoftware.smackx.workgroup.packet.Transcripts$TranscriptSummary> getSummaries()
supr org.jivesoftware.smack.packet.IQ
hfds UTC_FORMAT,summaries,userID

CLSS public static org.jivesoftware.smackx.workgroup.packet.Transcripts$AgentDetail
 outer org.jivesoftware.smackx.workgroup.packet.Transcripts
cons public init(java.lang.String,java.util.Date,java.util.Date)
meth public java.lang.String getAgentJID()
meth public java.lang.String toXML()
meth public java.util.Date getJoinTime()
meth public java.util.Date getLeftTime()
supr java.lang.Object
hfds agentJID,joinTime,leftTime

CLSS public static org.jivesoftware.smackx.workgroup.packet.Transcripts$TranscriptSummary
 outer org.jivesoftware.smackx.workgroup.packet.Transcripts
cons public init(java.lang.String,java.util.Date,java.util.Date,java.util.List<org.jivesoftware.smackx.workgroup.packet.Transcripts$AgentDetail>)
meth public java.lang.String getSessionID()
meth public java.lang.String toXML()
meth public java.util.Date getJoinTime()
meth public java.util.Date getLeftTime()
meth public java.util.List<org.jivesoftware.smackx.workgroup.packet.Transcripts$AgentDetail> getAgentDetails()
supr java.lang.Object
hfds agentDetails,joinTime,leftTime,sessionID

CLSS public org.jivesoftware.smackx.workgroup.packet.TranscriptsProvider
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object
hfds UTC_FORMAT

CLSS public org.jivesoftware.smackx.workgroup.packet.UserID
cons public init(java.lang.String)
fld public final static java.lang.String ELEMENT_NAME = "user"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static Provider
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getUserID()
meth public java.lang.String toXML()
supr java.lang.Object
hfds userID

CLSS public static org.jivesoftware.smackx.workgroup.packet.UserID$Provider
 outer org.jivesoftware.smackx.workgroup.packet.UserID
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.packet.WorkgroupInformation
cons public init(java.lang.String)
fld public final static java.lang.String ELEMENT_NAME = "workgroup"
fld public final static java.lang.String NAMESPACE = "http://jabber.org/protocol/workgroup"
innr public static Provider
intf org.jivesoftware.smack.packet.PacketExtension
meth public java.lang.String getElementName()
meth public java.lang.String getNamespace()
meth public java.lang.String getWorkgroupJID()
meth public java.lang.String toXML()
supr java.lang.Object
hfds workgroupJID

CLSS public static org.jivesoftware.smackx.workgroup.packet.WorkgroupInformation$Provider
 outer org.jivesoftware.smackx.workgroup.packet.WorkgroupInformation
cons public init()
intf org.jivesoftware.smack.provider.PacketExtensionProvider
meth public org.jivesoftware.smack.packet.PacketExtension parseExtension(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.settings.ChatSetting
cons public init(java.lang.String,java.lang.String,int)
meth public int getType()
meth public java.lang.String getKey()
meth public java.lang.String getValue()
meth public void setKey(java.lang.String)
meth public void setType(int)
meth public void setValue(java.lang.String)
supr java.lang.Object
hfds key,type,value

CLSS public org.jivesoftware.smackx.workgroup.settings.ChatSettings
cons public init()
cons public init(java.lang.String)
fld public final static int BOT_SETTINGS = 2
fld public final static int IMAGE_SETTINGS = 0
fld public final static int TEXT_SETTINGS = 1
fld public final static java.lang.String ELEMENT_NAME = "chat-settings"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static InternalProvider
meth public java.lang.String getChildElementXML()
meth public java.util.Collection getSettings()
meth public org.jivesoftware.smackx.workgroup.settings.ChatSetting getChatSetting(java.lang.String)
meth public org.jivesoftware.smackx.workgroup.settings.ChatSetting getFirstEntry()
meth public void addSetting(org.jivesoftware.smackx.workgroup.settings.ChatSetting)
meth public void setKey(java.lang.String)
meth public void setType(int)
supr org.jivesoftware.smack.packet.IQ
hfds key,settings,type

CLSS public static org.jivesoftware.smackx.workgroup.settings.ChatSettings$InternalProvider
 outer org.jivesoftware.smackx.workgroup.settings.ChatSettings
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.settings.GenericSettings
cons public init()
fld public final static java.lang.String ELEMENT_NAME = "generic-metadata"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static InternalProvider
meth public java.lang.String getChildElementXML()
meth public java.lang.String getQuery()
meth public java.util.Map getMap()
meth public void setMap(java.util.Map)
meth public void setQuery(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds map,query

CLSS public static org.jivesoftware.smackx.workgroup.settings.GenericSettings$InternalProvider
 outer org.jivesoftware.smackx.workgroup.settings.GenericSettings
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.settings.OfflineSettings
cons public init()
fld public final static java.lang.String ELEMENT_NAME = "offline-settings"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static InternalProvider
meth public boolean isConfigured()
meth public boolean redirects()
meth public java.lang.String getChildElementXML()
meth public java.lang.String getEmailAddress()
meth public java.lang.String getOfflineText()
meth public java.lang.String getRedirectURL()
meth public java.lang.String getSubject()
meth public void setEmailAddress(java.lang.String)
meth public void setOfflineText(java.lang.String)
meth public void setRedirectURL(java.lang.String)
meth public void setSubject(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds emailAddress,offlineText,redirectURL,subject

CLSS public static org.jivesoftware.smackx.workgroup.settings.OfflineSettings$InternalProvider
 outer org.jivesoftware.smackx.workgroup.settings.OfflineSettings
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.settings.SearchSettings
cons public init()
fld public final static java.lang.String ELEMENT_NAME = "search-settings"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static InternalProvider
meth public boolean hasForums()
meth public boolean hasKB()
meth public boolean isSearchEnabled()
meth public java.lang.String getChildElementXML()
meth public java.lang.String getForumsLocation()
meth public java.lang.String getKbLocation()
meth public void setForumsLocation(java.lang.String)
meth public void setKbLocation(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds forumsLocation,kbLocation

CLSS public static org.jivesoftware.smackx.workgroup.settings.SearchSettings$InternalProvider
 outer org.jivesoftware.smackx.workgroup.settings.SearchSettings
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.settings.SoundSettings
cons public init()
fld public final static java.lang.String ELEMENT_NAME = "sound-settings"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static InternalProvider
meth public byte[] getIncomingSoundBytes()
meth public byte[] getOutgoingSoundBytes()
meth public java.lang.String getChildElementXML()
meth public void setIncomingSound(java.lang.String)
meth public void setOutgoingSound(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds incomingSound,outgoingSound

CLSS public static org.jivesoftware.smackx.workgroup.settings.SoundSettings$InternalProvider
 outer org.jivesoftware.smackx.workgroup.settings.SoundSettings
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.settings.WorkgroupProperties
cons public init()
fld public final static java.lang.String ELEMENT_NAME = "workgroup-properties"
fld public final static java.lang.String NAMESPACE = "http://jivesoftware.com/protocol/workgroup"
innr public static InternalProvider
meth public boolean isAuthRequired()
meth public java.lang.String getChildElementXML()
meth public java.lang.String getEmail()
meth public java.lang.String getFullName()
meth public java.lang.String getJid()
meth public void setAuthRequired(boolean)
meth public void setEmail(java.lang.String)
meth public void setFullName(java.lang.String)
meth public void setJid(java.lang.String)
supr org.jivesoftware.smack.packet.IQ
hfds authRequired,email,fullName,jid

CLSS public static org.jivesoftware.smackx.workgroup.settings.WorkgroupProperties$InternalProvider
 outer org.jivesoftware.smackx.workgroup.settings.WorkgroupProperties
cons public init()
intf org.jivesoftware.smack.provider.IQProvider
meth public org.jivesoftware.smack.packet.IQ parseIQ(org.xmlpull.v1.XmlPullParser) throws java.lang.Exception
supr java.lang.Object

CLSS public abstract interface org.jivesoftware.smackx.workgroup.user.QueueListener
meth public abstract void departedQueue()
meth public abstract void joinedQueue()
meth public abstract void queuePositionUpdated(int)
meth public abstract void queueWaitTimeUpdated(int)

CLSS public org.jivesoftware.smackx.workgroup.user.Workgroup
cons public init(java.lang.String,org.jivesoftware.smack.XMPPConnection)
meth public boolean isAvailable()
meth public boolean isEmailAvailable()
meth public boolean isInQueue()
meth public int getQueuePosition()
meth public int getQueueRemainingTime()
meth public java.lang.String getWorkgroupJID()
meth public org.jivesoftware.smackx.Form getWorkgroupForm() throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.workgroup.settings.ChatSetting getChatSetting(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.workgroup.settings.ChatSettings getChatSettings() throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.workgroup.settings.ChatSettings getChatSettings(int) throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.workgroup.settings.OfflineSettings getOfflineSettings() throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.workgroup.settings.SoundSettings getSoundSettings() throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.workgroup.settings.WorkgroupProperties getWorkgroupProperties() throws org.jivesoftware.smack.XMPPException
meth public org.jivesoftware.smackx.workgroup.settings.WorkgroupProperties getWorkgroupProperties(java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void addInvitationListener(org.jivesoftware.smackx.workgroup.WorkgroupInvitationListener)
meth public void addQueueListener(org.jivesoftware.smackx.workgroup.user.QueueListener)
meth public void departQueue() throws org.jivesoftware.smack.XMPPException
meth public void joinQueue() throws org.jivesoftware.smack.XMPPException
meth public void joinQueue(java.util.Map,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void joinQueue(org.jivesoftware.smackx.Form) throws org.jivesoftware.smack.XMPPException
meth public void joinQueue(org.jivesoftware.smackx.Form,java.lang.String) throws org.jivesoftware.smack.XMPPException
meth public void removeQueueListener(org.jivesoftware.smackx.workgroup.WorkgroupInvitationListener)
meth public void removeQueueListener(org.jivesoftware.smackx.workgroup.user.QueueListener)
supr java.lang.Object
hfds connection,inQueue,invitationListeners,queueListeners,queuePosition,queueRemainingTime,siteInviteListeners,workgroupJID
hcls JoinQueuePacket

CLSS public org.jivesoftware.smackx.workgroup.util.ListenerEventDispatcher
cons public init()
fld protected boolean hasFinishedDispatching
fld protected boolean isRunning
fld protected java.util.ArrayList triplets
innr protected TripletContainer
intf java.lang.Runnable
meth public boolean hasFinished()
meth public void addListenerTriplet(java.lang.Object,java.lang.reflect.Method,java.lang.Object[])
meth public void run()
supr java.lang.Object

CLSS protected org.jivesoftware.smackx.workgroup.util.ListenerEventDispatcher$TripletContainer
 outer org.jivesoftware.smackx.workgroup.util.ListenerEventDispatcher
cons protected init(org.jivesoftware.smackx.workgroup.util.ListenerEventDispatcher,java.lang.Object,java.lang.reflect.Method,java.lang.Object[])
fld protected java.lang.Object listenerInstance
fld protected java.lang.Object[] methodArguments
fld protected java.lang.reflect.Method listenerMethod
meth protected java.lang.Object getListenerInstance()
meth protected java.lang.Object[] getMethodArguments()
meth protected java.lang.reflect.Method getListenerMethod()
supr java.lang.Object

CLSS public org.jivesoftware.smackx.workgroup.util.MetaDataUtils
cons public init()
meth public static java.lang.String serializeMetaData(java.util.Map)
meth public static java.util.Map parseMetaData(org.xmlpull.v1.XmlPullParser) throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
supr java.lang.Object

CLSS public final org.jivesoftware.smackx.workgroup.util.ModelUtil
meth public final static boolean areBooleansDifferent(java.lang.Boolean,java.lang.Boolean)
meth public final static boolean areBooleansEqual(java.lang.Boolean,java.lang.Boolean)
meth public final static boolean areDifferent(java.lang.Object,java.lang.Object)
meth public final static boolean areEqual(java.lang.Object,java.lang.Object)
meth public final static boolean hasLength(java.lang.String)
meth public final static boolean hasNonNullElement(java.lang.Object[])
meth public final static java.lang.String concat(java.lang.String[])
meth public final static java.lang.String concat(java.lang.String[],java.lang.String)
meth public final static java.lang.String nullifyIfEmpty(java.lang.String)
meth public final static java.lang.String nullifyingToString(java.lang.Object)
meth public static boolean hasStringChanged(java.lang.String,java.lang.String)
meth public static java.lang.String getTimeFromLong(long)
meth public static java.util.Iterator reverseListIterator(java.util.ListIterator)
meth public static java.util.List iteratorAsList(java.util.Iterator)
supr java.lang.Object

CLSS public org.xmlpull.mxp1.MXParser
cons public init()
fld protected boolean allStringsInterned
fld protected boolean emptyElementTag
fld protected boolean pastEndTag
fld protected boolean preventBufferCompaction
fld protected boolean processNamespaces
fld protected boolean reachedEnd
fld protected boolean roundtripSupported
fld protected boolean seenAmpersand
fld protected boolean seenDocdecl
fld protected boolean seenEndTag
fld protected boolean seenMarkup
fld protected boolean seenRoot
fld protected boolean seenStartTag
fld protected boolean tokenize
fld protected boolean usePC
fld protected char[] buf
fld protected char[] charRefOneCharBuf
fld protected char[] pc
fld protected char[][] elRawName
fld protected char[][] entityNameBuf
fld protected char[][] entityReplacementBuf
fld protected final static char LOOKUP_MAX_CHAR = '\u0400'
fld protected final static char[] NCODING
fld protected final static char[] NO
fld protected final static char[] TANDALONE
fld protected final static char[] VERSION
fld protected final static char[] YES
fld protected final static int LOOKUP_MAX = 1024
fld protected final static int READ_CHUNK_SIZE = 8192
fld protected final static java.lang.String FEATURE_NAMES_INTERNED = "http://xmlpull.org/v1/doc/features.html#names-interned"
fld protected final static java.lang.String FEATURE_XML_ROUNDTRIP = "http://xmlpull.org/v1/doc/features.html#xml-roundtrip"
fld protected final static java.lang.String PROPERTY_LOCATION = "http://xmlpull.org/v1/doc/properties.html#location"
fld protected final static java.lang.String PROPERTY_XMLDECL_CONTENT = "http://xmlpull.org/v1/doc/properties.html#xmldecl-content"
fld protected final static java.lang.String PROPERTY_XMLDECL_STANDALONE = "http://xmlpull.org/v1/doc/properties.html#xmldecl-standalone"
fld protected final static java.lang.String PROPERTY_XMLDECL_VERSION = "http://xmlpull.org/v1/doc/properties.html#xmldecl-version"
fld protected final static java.lang.String XMLNS_URI = "http://www.w3.org/2000/xmlns/"
fld protected final static java.lang.String XML_URI = "http://www.w3.org/XML/1998/namespace"
fld protected int attributeCount
fld protected int bufAbsoluteStart
fld protected int bufEnd
fld protected int bufLoadFactor
fld protected int bufSoftLimit
fld protected int bufStart
fld protected int columnNumber
fld protected int depth
fld protected int entityEnd
fld protected int eventType
fld protected int lineNumber
fld protected int namespaceEnd
fld protected int pcEnd
fld protected int pcStart
fld protected int pos
fld protected int posEnd
fld protected int posStart
fld protected int[] attributeNameHash
fld protected int[] elNamespaceCount
fld protected int[] elRawNameEnd
fld protected int[] elRawNameLine
fld protected int[] entityNameHash
fld protected int[] namespacePrefixHash
fld protected java.io.InputStream inputStream
fld protected java.io.Reader reader
fld protected java.lang.Boolean xmlDeclStandalone
fld protected java.lang.String entityRefName
fld protected java.lang.String inputEncoding
fld protected java.lang.String location
fld protected java.lang.String text
fld protected java.lang.String xmlDeclContent
fld protected java.lang.String xmlDeclVersion
fld protected java.lang.String[] attributeName
fld protected java.lang.String[] attributePrefix
fld protected java.lang.String[] attributeUri
fld protected java.lang.String[] attributeValue
fld protected java.lang.String[] elName
fld protected java.lang.String[] elPrefix
fld protected java.lang.String[] elUri
fld protected java.lang.String[] entityName
fld protected java.lang.String[] entityReplacement
fld protected java.lang.String[] namespacePrefix
fld protected java.lang.String[] namespaceUri
fld protected static boolean[] lookupNameChar
fld protected static boolean[] lookupNameStartChar
intf org.xmlpull.v1.XmlPullParser
meth protected boolean isNameChar(char)
meth protected boolean isNameStartChar(char)
meth protected boolean isS(char)
meth protected boolean parsePI() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected char more() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected char parseAttribute() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected char requireInput(char,char[]) throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected char requireNextS() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected char skipS(char) throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected char[] lookuEntityReplacement(int) throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected char[] parseEntityRef() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected final static int fastHash(char[],int,int)
meth protected int nextImpl() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected int parseEpilog() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected int parseProlog() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected java.lang.String newString(char[],int,int)
meth protected java.lang.String newStringIntern(char[],int,int)
meth protected java.lang.String printable(char)
meth protected java.lang.String printable(java.lang.String)
meth protected void ensureAttributesCapacity(int)
meth protected void ensureElementsCapacity()
meth protected void ensureEntityCapacity()
meth protected void ensureNamespacesCapacity(int)
meth protected void ensurePC(int)
meth protected void fillBuf() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected void joinPC()
meth protected void parseCDSect(boolean) throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected void parseComment() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected void parseDocdecl() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected void parseXmlDecl(char) throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected void parseXmlDeclWithVersion(int,int) throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth protected void reset()
meth protected void resetStringCache()
meth public boolean getFeature(java.lang.String)
meth public boolean isAttributeDefault(int)
meth public boolean isEmptyElementTag() throws org.xmlpull.v1.XmlPullParserException
meth public boolean isWhitespace() throws org.xmlpull.v1.XmlPullParserException
meth public char[] getTextCharacters(int[])
meth public int getAttributeCount()
meth public int getColumnNumber()
meth public int getDepth()
meth public int getEventType() throws org.xmlpull.v1.XmlPullParserException
meth public int getLineNumber()
meth public int getNamespaceCount(int) throws org.xmlpull.v1.XmlPullParserException
meth public int next() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth public int nextTag() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth public int nextToken() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth public int parseEndTag() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth public int parseStartTag() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.String getAttributeName(int)
meth public java.lang.String getAttributeNamespace(int)
meth public java.lang.String getAttributePrefix(int)
meth public java.lang.String getAttributeType(int)
meth public java.lang.String getAttributeValue(int)
meth public java.lang.String getAttributeValue(java.lang.String,java.lang.String)
meth public java.lang.String getInputEncoding()
meth public java.lang.String getName()
meth public java.lang.String getNamespace()
meth public java.lang.String getNamespace(java.lang.String)
meth public java.lang.String getNamespacePrefix(int) throws org.xmlpull.v1.XmlPullParserException
meth public java.lang.String getNamespaceUri(int) throws org.xmlpull.v1.XmlPullParserException
meth public java.lang.String getPositionDescription()
meth public java.lang.String getPrefix()
meth public java.lang.String getText()
meth public java.lang.String nextText() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth public void defineEntityReplacementText(java.lang.String,java.lang.String) throws org.xmlpull.v1.XmlPullParserException
meth public void require(int,java.lang.String,java.lang.String) throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth public void setFeature(java.lang.String,boolean) throws org.xmlpull.v1.XmlPullParserException
meth public void setInput(java.io.InputStream,java.lang.String) throws org.xmlpull.v1.XmlPullParserException
meth public void setInput(java.io.Reader) throws org.xmlpull.v1.XmlPullParserException
meth public void setProperty(java.lang.String,java.lang.Object) throws org.xmlpull.v1.XmlPullParserException
meth public void skipSubTree() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
supr java.lang.Object
hfds TRACE_SIZING

CLSS public abstract interface org.xmlpull.v1.XmlPullParser
fld public final static int CDSECT = 5
fld public final static int COMMENT = 9
fld public final static int DOCDECL = 10
fld public final static int END_DOCUMENT = 1
fld public final static int END_TAG = 3
fld public final static int ENTITY_REF = 6
fld public final static int IGNORABLE_WHITESPACE = 7
fld public final static int PROCESSING_INSTRUCTION = 8
fld public final static int START_DOCUMENT = 0
fld public final static int START_TAG = 2
fld public final static int TEXT = 4
fld public final static java.lang.String FEATURE_PROCESS_DOCDECL = "http://xmlpull.org/v1/doc/features.html#process-docdecl"
fld public final static java.lang.String FEATURE_PROCESS_NAMESPACES = "http://xmlpull.org/v1/doc/features.html#process-namespaces"
fld public final static java.lang.String FEATURE_REPORT_NAMESPACE_ATTRIBUTES = "http://xmlpull.org/v1/doc/features.html#report-namespace-prefixes"
fld public final static java.lang.String FEATURE_VALIDATION = "http://xmlpull.org/v1/doc/features.html#validation"
fld public final static java.lang.String NO_NAMESPACE = ""
fld public final static java.lang.String[] TYPES
meth public abstract boolean getFeature(java.lang.String)
meth public abstract boolean isAttributeDefault(int)
meth public abstract boolean isEmptyElementTag() throws org.xmlpull.v1.XmlPullParserException
meth public abstract boolean isWhitespace() throws org.xmlpull.v1.XmlPullParserException
meth public abstract char[] getTextCharacters(int[])
meth public abstract int getAttributeCount()
meth public abstract int getColumnNumber()
meth public abstract int getDepth()
meth public abstract int getEventType() throws org.xmlpull.v1.XmlPullParserException
meth public abstract int getLineNumber()
meth public abstract int getNamespaceCount(int) throws org.xmlpull.v1.XmlPullParserException
meth public abstract int next() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth public abstract int nextTag() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth public abstract int nextToken() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth public abstract java.lang.Object getProperty(java.lang.String)
meth public abstract java.lang.String getAttributeName(int)
meth public abstract java.lang.String getAttributeNamespace(int)
meth public abstract java.lang.String getAttributePrefix(int)
meth public abstract java.lang.String getAttributeType(int)
meth public abstract java.lang.String getAttributeValue(int)
meth public abstract java.lang.String getAttributeValue(java.lang.String,java.lang.String)
meth public abstract java.lang.String getInputEncoding()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getNamespace()
meth public abstract java.lang.String getNamespace(java.lang.String)
meth public abstract java.lang.String getNamespacePrefix(int) throws org.xmlpull.v1.XmlPullParserException
meth public abstract java.lang.String getNamespaceUri(int) throws org.xmlpull.v1.XmlPullParserException
meth public abstract java.lang.String getPositionDescription()
meth public abstract java.lang.String getPrefix()
meth public abstract java.lang.String getText()
meth public abstract java.lang.String nextText() throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth public abstract void defineEntityReplacementText(java.lang.String,java.lang.String) throws org.xmlpull.v1.XmlPullParserException
meth public abstract void require(int,java.lang.String,java.lang.String) throws java.io.IOException,org.xmlpull.v1.XmlPullParserException
meth public abstract void setFeature(java.lang.String,boolean) throws org.xmlpull.v1.XmlPullParserException
meth public abstract void setInput(java.io.InputStream,java.lang.String) throws org.xmlpull.v1.XmlPullParserException
meth public abstract void setInput(java.io.Reader) throws org.xmlpull.v1.XmlPullParserException
meth public abstract void setProperty(java.lang.String,java.lang.Object) throws org.xmlpull.v1.XmlPullParserException

CLSS public org.xmlpull.v1.XmlPullParserException
cons public init(java.lang.String)
cons public init(java.lang.String,org.xmlpull.v1.XmlPullParser,java.lang.Throwable)
fld protected int column
fld protected int row
fld protected java.lang.Throwable detail
meth public int getColumnNumber()
meth public int getLineNumber()
meth public java.lang.Throwable getDetail()
meth public void printStackTrace()
supr java.lang.Exception

