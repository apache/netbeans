#Signature file v4.1
#Version 9.20

CLSS public java.awt.datatransfer.Clipboard
cons public init(java.lang.String)
fld protected java.awt.datatransfer.ClipboardOwner owner
fld protected java.awt.datatransfer.Transferable contents
meth public boolean isDataFlavorAvailable(java.awt.datatransfer.DataFlavor)
meth public java.awt.datatransfer.DataFlavor[] getAvailableDataFlavors()
meth public java.awt.datatransfer.FlavorListener[] getFlavorListeners()
meth public java.awt.datatransfer.Transferable getContents(java.lang.Object)
meth public java.lang.Object getData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public java.lang.String getName()
meth public void addFlavorListener(java.awt.datatransfer.FlavorListener)
meth public void removeFlavorListener(java.awt.datatransfer.FlavorListener)
meth public void setContents(java.awt.datatransfer.Transferable,java.awt.datatransfer.ClipboardOwner)
supr java.lang.Object

CLSS public abstract interface java.awt.datatransfer.Transferable
meth public abstract boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public abstract java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public abstract java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException

CLSS public abstract interface java.awt.event.ActionListener
intf java.util.EventListener
meth public abstract void actionPerformed(java.awt.event.ActionEvent)

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.DataInput
meth public abstract boolean readBoolean() throws java.io.IOException
meth public abstract byte readByte() throws java.io.IOException
meth public abstract char readChar() throws java.io.IOException
meth public abstract double readDouble() throws java.io.IOException
meth public abstract float readFloat() throws java.io.IOException
meth public abstract int readInt() throws java.io.IOException
meth public abstract int readUnsignedByte() throws java.io.IOException
meth public abstract int readUnsignedShort() throws java.io.IOException
meth public abstract int skipBytes(int) throws java.io.IOException
meth public abstract java.lang.String readLine() throws java.io.IOException
meth public abstract java.lang.String readUTF() throws java.io.IOException
meth public abstract long readLong() throws java.io.IOException
meth public abstract short readShort() throws java.io.IOException
meth public abstract void readFully(byte[]) throws java.io.IOException
meth public abstract void readFully(byte[],int,int) throws java.io.IOException

CLSS public abstract interface java.io.DataOutput
meth public abstract void write(byte[]) throws java.io.IOException
meth public abstract void write(byte[],int,int) throws java.io.IOException
meth public abstract void write(int) throws java.io.IOException
meth public abstract void writeBoolean(boolean) throws java.io.IOException
meth public abstract void writeByte(int) throws java.io.IOException
meth public abstract void writeBytes(java.lang.String) throws java.io.IOException
meth public abstract void writeChar(int) throws java.io.IOException
meth public abstract void writeChars(java.lang.String) throws java.io.IOException
meth public abstract void writeDouble(double) throws java.io.IOException
meth public abstract void writeFloat(float) throws java.io.IOException
meth public abstract void writeInt(int) throws java.io.IOException
meth public abstract void writeLong(long) throws java.io.IOException
meth public abstract void writeShort(int) throws java.io.IOException
meth public abstract void writeUTF(java.lang.String) throws java.io.IOException

CLSS public abstract interface java.io.Externalizable
intf java.io.Serializable
meth public abstract void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeExternal(java.io.ObjectOutput) throws java.io.IOException

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

CLSS public abstract interface java.io.ObjectInput
intf java.io.DataInput
intf java.lang.AutoCloseable
meth public abstract int available() throws java.io.IOException
meth public abstract int read() throws java.io.IOException
meth public abstract int read(byte[]) throws java.io.IOException
meth public abstract int read(byte[],int,int) throws java.io.IOException
meth public abstract java.lang.Object readObject() throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract long skip(long) throws java.io.IOException
meth public abstract void close() throws java.io.IOException

CLSS public java.io.ObjectInputStream
cons protected init() throws java.io.IOException
cons public init(java.io.InputStream) throws java.io.IOException
innr public abstract static GetField
intf java.io.ObjectInput
intf java.io.ObjectStreamConstants
meth protected boolean enableResolveObject(boolean)
meth protected java.io.ObjectStreamClass readClassDescriptor() throws java.io.IOException,java.lang.ClassNotFoundException
meth protected java.lang.Class<?> resolveClass(java.io.ObjectStreamClass) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected java.lang.Class<?> resolveProxyClass(java.lang.String[]) throws java.io.IOException,java.lang.ClassNotFoundException
meth protected java.lang.Object readObjectOverride() throws java.io.IOException,java.lang.ClassNotFoundException
meth protected java.lang.Object resolveObject(java.lang.Object) throws java.io.IOException
meth protected void readStreamHeader() throws java.io.IOException
meth public boolean readBoolean() throws java.io.IOException
meth public byte readByte() throws java.io.IOException
meth public char readChar() throws java.io.IOException
meth public double readDouble() throws java.io.IOException
meth public final java.lang.Object readObject() throws java.io.IOException,java.lang.ClassNotFoundException
meth public float readFloat() throws java.io.IOException
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public int readInt() throws java.io.IOException
meth public int readUnsignedByte() throws java.io.IOException
meth public int readUnsignedShort() throws java.io.IOException
meth public int skipBytes(int) throws java.io.IOException
meth public java.io.ObjectInputStream$GetField readFields() throws java.io.IOException,java.lang.ClassNotFoundException
meth public java.lang.Object readUnshared() throws java.io.IOException,java.lang.ClassNotFoundException
meth public java.lang.String readLine() throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public java.lang.String readUTF() throws java.io.IOException
meth public long readLong() throws java.io.IOException
meth public short readShort() throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void defaultReadObject() throws java.io.IOException,java.lang.ClassNotFoundException
meth public void readFully(byte[]) throws java.io.IOException
meth public void readFully(byte[],int,int) throws java.io.IOException
meth public void registerValidation(java.io.ObjectInputValidation,int) throws java.io.InvalidObjectException,java.io.NotActiveException
supr java.io.InputStream

CLSS public abstract interface java.io.ObjectOutput
intf java.io.DataOutput
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException
meth public abstract void flush() throws java.io.IOException
meth public abstract void write(byte[]) throws java.io.IOException
meth public abstract void write(byte[],int,int) throws java.io.IOException
meth public abstract void write(int) throws java.io.IOException
meth public abstract void writeObject(java.lang.Object) throws java.io.IOException

CLSS public java.io.ObjectOutputStream
cons protected init() throws java.io.IOException
cons public init(java.io.OutputStream) throws java.io.IOException
innr public abstract static PutField
intf java.io.ObjectOutput
intf java.io.ObjectStreamConstants
meth protected boolean enableReplaceObject(boolean)
meth protected java.lang.Object replaceObject(java.lang.Object) throws java.io.IOException
meth protected void annotateClass(java.lang.Class<?>) throws java.io.IOException
meth protected void annotateProxyClass(java.lang.Class<?>) throws java.io.IOException
meth protected void drain() throws java.io.IOException
meth protected void writeClassDescriptor(java.io.ObjectStreamClass) throws java.io.IOException
meth protected void writeObjectOverride(java.lang.Object) throws java.io.IOException
meth protected void writeStreamHeader() throws java.io.IOException
meth public final void writeObject(java.lang.Object) throws java.io.IOException
meth public java.io.ObjectOutputStream$PutField putFields() throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void defaultWriteObject() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void reset() throws java.io.IOException
meth public void useProtocolVersion(int) throws java.io.IOException
meth public void write(byte[]) throws java.io.IOException
meth public void write(byte[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
meth public void writeBoolean(boolean) throws java.io.IOException
meth public void writeByte(int) throws java.io.IOException
meth public void writeBytes(java.lang.String) throws java.io.IOException
meth public void writeChar(int) throws java.io.IOException
meth public void writeChars(java.lang.String) throws java.io.IOException
meth public void writeDouble(double) throws java.io.IOException
meth public void writeFields() throws java.io.IOException
meth public void writeFloat(float) throws java.io.IOException
meth public void writeInt(int) throws java.io.IOException
meth public void writeLong(long) throws java.io.IOException
meth public void writeShort(int) throws java.io.IOException
meth public void writeUTF(java.lang.String) throws java.io.IOException
meth public void writeUnshared(java.lang.Object) throws java.io.IOException
supr java.io.OutputStream

CLSS public abstract interface java.io.ObjectStreamConstants
fld public final static byte SC_BLOCK_DATA = 8
fld public final static byte SC_ENUM = 16
fld public final static byte SC_EXTERNALIZABLE = 4
fld public final static byte SC_SERIALIZABLE = 2
fld public final static byte SC_WRITE_METHOD = 1
fld public final static byte TC_ARRAY = 117
fld public final static byte TC_BASE = 112
fld public final static byte TC_BLOCKDATA = 119
fld public final static byte TC_BLOCKDATALONG = 122
fld public final static byte TC_CLASS = 118
fld public final static byte TC_CLASSDESC = 114
fld public final static byte TC_ENDBLOCKDATA = 120
fld public final static byte TC_ENUM = 126
fld public final static byte TC_EXCEPTION = 123
fld public final static byte TC_LONGSTRING = 124
fld public final static byte TC_MAX = 126
fld public final static byte TC_NULL = 112
fld public final static byte TC_OBJECT = 115
fld public final static byte TC_PROXYCLASSDESC = 125
fld public final static byte TC_REFERENCE = 113
fld public final static byte TC_RESET = 121
fld public final static byte TC_STRING = 116
fld public final static int PROTOCOL_VERSION_1 = 1
fld public final static int PROTOCOL_VERSION_2 = 2
fld public final static int baseWireHandle = 8257536
fld public final static java.io.SerializablePermission SUBCLASS_IMPLEMENTATION_PERMISSION
fld public final static java.io.SerializablePermission SUBSTITUTION_PERMISSION
fld public final static short STREAM_MAGIC = -21267
fld public final static short STREAM_VERSION = 5

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

CLSS public abstract interface !annotation java.lang.Deprecated
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract java.text.Format
cons protected init()
innr public static Field
intf java.io.Serializable
intf java.lang.Cloneable
meth public abstract java.lang.Object parseObject(java.lang.String,java.text.ParsePosition)
meth public abstract java.lang.StringBuffer format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
meth public final java.lang.String format(java.lang.Object)
meth public java.lang.Object clone()
meth public java.lang.Object parseObject(java.lang.String) throws java.text.ParseException
meth public java.text.AttributedCharacterIterator formatToCharacterIterator(java.lang.Object)
supr java.lang.Object

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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

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

CLSS public abstract interface java.util.concurrent.ScheduledExecutorService
intf java.util.concurrent.ExecutorService
meth public abstract <%0 extends java.lang.Object> java.util.concurrent.ScheduledFuture<{%%0}> schedule(java.util.concurrent.Callable<{%%0}>,long,java.util.concurrent.TimeUnit)
meth public abstract java.util.concurrent.ScheduledFuture<?> schedule(java.lang.Runnable,long,java.util.concurrent.TimeUnit)
meth public abstract java.util.concurrent.ScheduledFuture<?> scheduleAtFixedRate(java.lang.Runnable,long,long,java.util.concurrent.TimeUnit)
meth public abstract java.util.concurrent.ScheduledFuture<?> scheduleWithFixedDelay(java.lang.Runnable,long,long,java.util.concurrent.TimeUnit)

CLSS public abstract interface javax.swing.Action
fld public final static java.lang.String ACCELERATOR_KEY = "AcceleratorKey"
fld public final static java.lang.String ACTION_COMMAND_KEY = "ActionCommandKey"
fld public final static java.lang.String DEFAULT = "Default"
fld public final static java.lang.String DISPLAYED_MNEMONIC_INDEX_KEY = "SwingDisplayedMnemonicIndexKey"
fld public final static java.lang.String LARGE_ICON_KEY = "SwingLargeIconKey"
fld public final static java.lang.String LONG_DESCRIPTION = "LongDescription"
fld public final static java.lang.String MNEMONIC_KEY = "MnemonicKey"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String SELECTED_KEY = "SwingSelectedKey"
fld public final static java.lang.String SHORT_DESCRIPTION = "ShortDescription"
fld public final static java.lang.String SMALL_ICON = "SmallIcon"
intf java.awt.event.ActionListener
meth public abstract boolean isEnabled()
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void putValue(java.lang.String,java.lang.Object)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setEnabled(boolean)

CLSS public abstract interface javax.swing.Icon
meth public abstract int getIconHeight()
meth public abstract int getIconWidth()
meth public abstract void paintIcon(java.awt.Component,java.awt.Graphics,int,int)

CLSS public abstract org.openide.ErrorManager
cons public init()
fld public final static int ERROR = 65536
fld public final static int EXCEPTION = 4096
fld public final static int INFORMATIONAL = 1
fld public final static int UNKNOWN = 0
fld public final static int USER = 256
fld public final static int WARNING = 16
innr public abstract interface static Annotation
meth public abstract java.lang.Throwable annotate(java.lang.Throwable,int,java.lang.String,java.lang.String,java.lang.Throwable,java.util.Date)
meth public abstract java.lang.Throwable attachAnnotations(java.lang.Throwable,org.openide.ErrorManager$Annotation[])
meth public abstract org.openide.ErrorManager getInstance(java.lang.String)
meth public abstract org.openide.ErrorManager$Annotation[] findAnnotations(java.lang.Throwable)
meth public abstract void log(int,java.lang.String)
meth public abstract void notify(int,java.lang.Throwable)
meth public boolean isLoggable(int)
meth public boolean isNotifiable(int)
meth public final java.lang.Throwable annotate(java.lang.Throwable,java.lang.String)
meth public final java.lang.Throwable annotate(java.lang.Throwable,java.lang.Throwable)
meth public final java.lang.Throwable copyAnnotation(java.lang.Throwable,java.lang.Throwable)
 anno 0 java.lang.Deprecated()
meth public final void log(java.lang.String)
meth public final void notify(java.lang.Throwable)
meth public static org.openide.ErrorManager getDefault()
supr java.lang.Object
hfds current
hcls AnnException,DelegatingErrorManager,OwnLevel

CLSS public abstract interface static org.openide.ErrorManager$Annotation
 outer org.openide.ErrorManager
meth public abstract int getSeverity()
meth public abstract java.lang.String getLocalizedMessage()
meth public abstract java.lang.String getMessage()
meth public abstract java.lang.Throwable getStackTrace()
meth public abstract java.util.Date getDate()

CLSS public abstract org.openide.LifecycleManager
cons protected init()
meth public abstract void exit()
meth public abstract void saveAll()
meth public static org.openide.LifecycleManager getDefault()
meth public void exit(int)
meth public void markForRestart()
supr java.lang.Object
hcls Trivial

CLSS public abstract org.openide.ServiceType
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static java.lang.String PROP_NAME = "name"
innr public abstract static Registry
innr public final static Handle
intf java.io.Serializable
intf org.openide.util.HelpCtx$Provider
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
 anno 0 java.lang.Deprecated()
meth protected java.lang.String displayName()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public final org.openide.ServiceType createClone()
 anno 0 java.lang.Deprecated()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public java.lang.String getName()
meth public void setName(java.lang.String)
supr java.lang.Object
hfds err,name,serialVersionUID,supp

CLSS public final static org.openide.ServiceType$Handle
 outer org.openide.ServiceType
 anno 0 java.lang.Deprecated()
cons public init(org.openide.ServiceType)
intf java.io.Serializable
meth public java.lang.String toString()
meth public org.openide.ServiceType getServiceType()
supr java.lang.Object
hfds className,name,serialVersionUID,serviceType

CLSS public abstract static org.openide.ServiceType$Registry
 outer org.openide.ServiceType
 anno 0 java.lang.Deprecated()
cons public init()
intf java.io.Serializable
meth public <%0 extends org.openide.ServiceType> java.util.Enumeration<{%%0}> services(java.lang.Class<{%%0}>)
meth public abstract java.util.Enumeration<org.openide.ServiceType> services()
meth public abstract java.util.List getServiceTypes()
meth public abstract void setServiceTypes(java.util.List)
 anno 0 java.lang.Deprecated()
meth public org.openide.ServiceType find(java.lang.Class)
 anno 0 java.lang.Deprecated()
meth public org.openide.ServiceType find(java.lang.String)
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract interface org.openide.util.AsyncGUIJob
meth public abstract void construct()
meth public abstract void finished()

CLSS public abstract org.openide.util.BaseUtilities
fld public final static int OS_AIX = 64
fld public final static int OS_DEC = 1024
 anno 0 java.lang.Deprecated()
fld public final static int OS_FREEBSD = 131072
fld public final static int OS_HP = 32
fld public final static int OS_IRIX = 128
fld public final static int OS_LINUX = 16
fld public final static int OS_MAC = 4096
fld public final static int OS_OPENBSD = 1048576
fld public final static int OS_OS2 = 2048
fld public final static int OS_OTHER = 65536
fld public final static int OS_SOLARIS = 8
fld public final static int OS_SUNOS = 256
fld public final static int OS_TRU64 = 512
fld public final static int OS_UNIX_MASK = 1709048
 anno 0 java.lang.Deprecated()
fld public final static int OS_UNIX_OTHER = 524288
fld public final static int OS_VMS = 16384
fld public final static int OS_WIN2000 = 8192
fld public final static int OS_WIN95 = 2
fld public final static int OS_WIN98 = 4
fld public final static int OS_WINDOWS_MASK = 303111
 anno 0 java.lang.Deprecated()
fld public final static int OS_WINNT = 1
fld public final static int OS_WINVISTA = 262144
fld public final static int OS_WIN_OTHER = 32768
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> topologicalSort(java.util.Collection<? extends {%%0}>,java.util.Map<? super {%%0},? extends java.util.Collection<? extends {%%0}>>) throws org.openide.util.TopologicalSortException
meth public static boolean compareObjects(java.lang.Object,java.lang.Object)
meth public static boolean compareObjectsImpl(java.lang.Object,java.lang.Object,int)
meth public static boolean isJavaIdentifier(java.lang.String)
meth public static boolean isMac()
meth public static boolean isUnix()
meth public static boolean isWindows()
meth public static int getOperatingSystem()
meth public static java.io.File toFile(java.net.URI)
meth public static java.lang.Class<?> getObjectType(java.lang.Class<?>)
meth public static java.lang.Class<?> getPrimitiveType(java.lang.Class<?>)
meth public static java.lang.Object toPrimitiveArray(java.lang.Object[])
meth public static java.lang.Object[] toObjectArray(java.lang.Object)
meth public static java.lang.String escapeParameters(java.lang.String[])
meth public static java.lang.String getClassName(java.lang.Class<?>)
meth public static java.lang.String getShortClassName(java.lang.Class<?>)
meth public static java.lang.String pureClassName(java.lang.String)
meth public static java.lang.String translate(java.lang.String)
meth public static java.lang.String wrapString(java.lang.String,int,java.text.BreakIterator,boolean)
meth public static java.lang.String[] parseParameters(java.lang.String)
meth public static java.lang.String[] wrapStringToArray(java.lang.String,int,java.text.BreakIterator,boolean)
meth public static java.lang.ref.ReferenceQueue<java.lang.Object> activeReferenceQueue()
meth public static java.net.URI toURI(java.io.File)
supr java.lang.Object
hfds LOG,TRANS_LOCK,operatingSystem,pathURIConsistent,transExp,transLoader
hcls RE

CLSS public abstract org.openide.util.CachedHiDPIIcon
cons protected init(int,int)
intf javax.swing.Icon
meth protected abstract java.awt.Image createAndPaintImage(java.awt.Component,java.awt.image.ColorModel,int,int,double)
meth protected final static java.awt.image.BufferedImage createBufferedImage(java.awt.image.ColorModel,int,int)
meth public final int getIconHeight()
meth public final int getIconWidth()
meth public final void paintIcon(java.awt.Component,java.awt.Graphics,int,int)
supr java.lang.Object
hfds MAX_CACHE_SIZE,cache,cacheSize,height,width
hcls CachedImageKey

CLSS public abstract interface org.openide.util.Cancellable
meth public abstract boolean cancel()

CLSS public final org.openide.util.ChangeSupport
cons public init(java.lang.Object)
meth public boolean hasListeners()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void fireChange()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds LOG,listeners,source

CLSS public final org.openide.util.CharSequences
meth public static boolean isCompact(java.lang.CharSequence)
meth public static int indexOf(java.lang.CharSequence,java.lang.CharSequence)
meth public static int indexOf(java.lang.CharSequence,java.lang.CharSequence,int)
meth public static java.lang.CharSequence create(char[],int,int)
meth public static java.lang.CharSequence create(java.lang.CharSequence)
meth public static java.lang.CharSequence empty()
meth public static java.util.Comparator<java.lang.CharSequence> comparator()
supr java.lang.Object
hfds Comparator,EMPTY,decodeTable,encodeTable
hcls ByteBasedSequence,CharBasedSequence,CharSequenceComparator,CompactCharSequence,Fixed6Bit_11_20,Fixed6Bit_1_10,Fixed6Bit_21_30,Fixed_0_7,Fixed_16_23,Fixed_8_15

CLSS public abstract interface org.openide.util.ContextAwareAction
intf javax.swing.Action
meth public abstract javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)

CLSS public abstract interface org.openide.util.ContextGlobalProvider
meth public abstract org.openide.util.Lookup createGlobalContext()

CLSS public final org.openide.util.EditableProperties
cons public init(boolean)
intf java.lang.Cloneable
meth public java.lang.Object clone()
meth public java.lang.String get(java.lang.Object)
meth public java.lang.String getProperty(java.lang.String)
meth public java.lang.String put(java.lang.String,java.lang.String)
meth public java.lang.String setProperty(java.lang.String,java.lang.String)
meth public java.lang.String setProperty(java.lang.String,java.lang.String[])
meth public java.lang.String[] getComment(java.lang.String)
meth public java.util.Set<java.util.Map$Entry<java.lang.String,java.lang.String>> entrySet()
meth public org.openide.util.EditableProperties cloneProperties()
meth public void load(java.io.InputStream) throws java.io.IOException
meth public void setComment(java.lang.String,java.lang.String[],boolean)
meth public void store(java.io.OutputStream) throws java.io.IOException
supr java.util.AbstractMap<java.lang.String,java.lang.String>
hfds INDENT,READING_KEY_VALUE,WAITING_FOR_KEY_VALUE,alphabetize,state
hcls Item,IteratorImpl,MapEntryImpl,SetImpl,State

CLSS public final org.openide.util.Enumerations
innr public abstract interface static Processor
meth public !varargs static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> array({%%0}[])
meth public final static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> empty()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Enumeration<{%%1}> convert(java.util.Enumeration<? extends {%%0}>,org.openide.util.Enumerations$Processor<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Enumeration<{%%1}> filter(java.util.Enumeration<? extends {%%0}>,org.openide.util.Enumerations$Processor<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Enumeration<{%%1}> queue(java.util.Enumeration<? extends {%%0}>,org.openide.util.Enumerations$Processor<{%%0},{%%1}>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> concat(java.util.Enumeration<? extends java.util.Enumeration<? extends {%%0}>>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> concat(java.util.Enumeration<? extends {%%0}>,java.util.Enumeration<? extends {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> removeDuplicates(java.util.Enumeration<{%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> removeNulls(java.util.Enumeration<{%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> singleton({%%0})
supr java.lang.Object
hcls AltEn,FilEn,QEn,RNulls,SeqEn

CLSS public abstract interface static org.openide.util.Enumerations$Processor<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.openide.util.Enumerations
meth public abstract {org.openide.util.Enumerations$Processor%1} process({org.openide.util.Enumerations$Processor%0},java.util.Collection<{org.openide.util.Enumerations$Processor%0}>)

CLSS public final org.openide.util.Exceptions
meth public static <%0 extends java.lang.Throwable> {%%0} attachLocalizedMessage({%%0},java.lang.String)
meth public static <%0 extends java.lang.Throwable> {%%0} attachMessage({%%0},java.lang.String)
meth public static <%0 extends java.lang.Throwable> {%%0} attachSeverity({%%0},java.util.logging.Level)
meth public static java.lang.String findLocalizedMessage(java.lang.Throwable)
meth public static void printStackTrace(java.lang.Throwable)
supr java.lang.Object
hfds LOC_MSG_PLACEHOLDER,LOG
hcls AnnException,OwnLevel

CLSS public final org.openide.util.HelpCtx
cons public init(java.lang.Class<?>)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
cons public init(java.net.URL)
 anno 0 java.lang.Deprecated()
fld public final static org.openide.util.HelpCtx DEFAULT_HELP
innr public abstract interface static Displayer
innr public abstract interface static Provider
meth public boolean display()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getHelpID()
meth public java.lang.String toString()
meth public java.net.URL getHelp()
meth public static org.openide.util.HelpCtx findHelp(java.awt.Component)
meth public static org.openide.util.HelpCtx findHelp(java.lang.Object)
meth public static void setHelpIDString(javax.swing.JComponent,java.lang.String)
supr java.lang.Object
hfds err,helpCtx,helpID

CLSS public abstract interface static org.openide.util.HelpCtx$Displayer
 outer org.openide.util.HelpCtx
meth public abstract boolean display(org.openide.util.HelpCtx)

CLSS public abstract interface static org.openide.util.HelpCtx$Provider
 outer org.openide.util.HelpCtx
meth public abstract org.openide.util.HelpCtx getHelpCtx()

CLSS public final org.openide.util.ImageUtilities
meth public final static java.awt.Image addToolTipToImage(java.awt.Image,java.lang.String)
meth public final static java.awt.Image assignToolTipToImage(java.awt.Image,java.lang.String)
meth public final static java.awt.Image icon2Image(javax.swing.Icon)
meth public final static java.awt.Image loadImage(java.lang.String)
meth public final static java.awt.Image loadImage(java.lang.String,boolean)
meth public final static java.awt.Image mergeImages(java.awt.Image,java.awt.Image,int,int)
meth public final static java.lang.String getImageToolTip(java.awt.Image)
meth public final static javax.swing.Icon image2Icon(java.awt.Image)
meth public final static javax.swing.ImageIcon loadImageIcon(java.lang.String,boolean)
meth public static java.awt.Image createDisabledImage(java.awt.Image)
meth public static javax.swing.Icon createDisabledIcon(javax.swing.Icon)
supr java.lang.Object
hfds DARK_LAF_SUFFIX,ERR,LOGGER,NO_ICON,PNG_READER,TOOLTIP_SEPAR,cache,classLoaderLoader,component,compositeCache,dummyIconComponent,extraInitialSlashes,imageIconFilter,imageToolTipCache,localizedCache,mediaTrackerID,svgLoaderLoader,tracker
hcls ActiveRef,CachedLookupLoader,CompositeImageKey,DisabledButtonFilter,IconImageIcon,MergedIcon,ToolTipImage,ToolTipImageKey

CLSS public abstract org.openide.util.Lookup
cons public init()
fld public final static org.openide.util.Lookup EMPTY
innr public abstract interface static Provider
innr public abstract static Item
innr public abstract static Result
innr public final static Template
meth public <%0 extends java.lang.Object> java.util.Collection<? extends {%%0}> lookupAll(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookupResult(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public static org.openide.util.Lookup getDefault()
supr java.lang.Object
hfds LOG,defaultLookup,defaultLookupProvider
hcls DefLookup,Empty

CLSS public abstract static org.openide.util.Lookup$Item<%0 extends java.lang.Object>
 outer org.openide.util.Lookup
cons public init()
meth public abstract java.lang.Class<? extends {org.openide.util.Lookup$Item%0}> getType()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getId()
meth public abstract {org.openide.util.Lookup$Item%0} getInstance()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface static org.openide.util.Lookup$Provider
 outer org.openide.util.Lookup
meth public abstract org.openide.util.Lookup getLookup()

CLSS public abstract static org.openide.util.Lookup$Result<%0 extends java.lang.Object>
 outer org.openide.util.Lookup
cons public init()
meth public abstract java.util.Collection<? extends {org.openide.util.Lookup$Result%0}> allInstances()
meth public abstract void addLookupListener(org.openide.util.LookupListener)
meth public abstract void removeLookupListener(org.openide.util.LookupListener)
meth public java.util.Collection<? extends org.openide.util.Lookup$Item<{org.openide.util.Lookup$Result%0}>> allItems()
meth public java.util.Set<java.lang.Class<? extends {org.openide.util.Lookup$Result%0}>> allClasses()
supr java.lang.Object

CLSS public final static org.openide.util.Lookup$Template<%0 extends java.lang.Object>
 outer org.openide.util.Lookup
cons public init()
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Class<{org.openide.util.Lookup$Template%0}>)
cons public init(java.lang.Class<{org.openide.util.Lookup$Template%0}>,java.lang.String,{org.openide.util.Lookup$Template%0})
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Class<{org.openide.util.Lookup$Template%0}> getType()
meth public java.lang.String getId()
meth public java.lang.String toString()
meth public {org.openide.util.Lookup$Template%0} getInstance()
supr java.lang.Object
hfds hashCode,id,instance,type

CLSS public final org.openide.util.LookupEvent
cons public init(org.openide.util.Lookup$Result)
supr java.util.EventObject

CLSS public abstract interface org.openide.util.LookupListener
intf java.util.EventListener
meth public abstract void resultChanged(org.openide.util.LookupEvent)

CLSS public org.openide.util.MapFormat
cons public init(java.util.Map)
meth protected java.lang.Object processKey(java.lang.String)
meth public boolean isExactMatch()
meth public boolean willThrowExceptionIfKeyWasNotFound()
meth public java.lang.Object parseObject(java.lang.String,java.text.ParsePosition)
meth public java.lang.String getLeftBrace()
meth public java.lang.String getRightBrace()
meth public java.lang.String parse(java.lang.String)
meth public java.lang.String processPattern(java.lang.String)
meth public java.lang.StringBuffer format(java.lang.Object,java.lang.StringBuffer,java.text.FieldPosition)
meth public java.util.Map getMap()
meth public static java.lang.String format(java.lang.String,java.util.Map)
meth public void setExactMatch(boolean)
meth public void setLeftBrace(java.lang.String)
meth public void setMap(java.util.Map)
meth public void setRightBrace(java.lang.String)
meth public void setThrowExceptionIfKeyWasNotFound(boolean)
supr java.text.Format
hfds BUFSIZE,argmap,arguments,exactmatch,ldel,locale,maxOffset,offsets,rdel,serialVersionUID,throwex

CLSS public final org.openide.util.Mutex
cons public init()
cons public init(java.lang.Object)
cons public init(org.openide.util.Mutex$Privileged)
cons public init(org.openide.util.Mutex$Privileged,java.util.concurrent.Executor)
cons public init(org.openide.util.spi.MutexImplementation)
fld public final static org.openide.util.Mutex EVENT
innr public abstract interface static Action
innr public abstract interface static ExceptionAction
innr public final static Privileged
meth public <%0 extends java.lang.Object> {%%0} readAccess(org.openide.util.Mutex$Action<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} readAccess(org.openide.util.Mutex$ExceptionAction<{%%0}>) throws org.openide.util.MutexException
meth public <%0 extends java.lang.Object> {%%0} writeAccess(org.openide.util.Mutex$Action<{%%0}>)
meth public <%0 extends java.lang.Object> {%%0} writeAccess(org.openide.util.Mutex$ExceptionAction<{%%0}>) throws org.openide.util.MutexException
meth public boolean isReadAccess()
meth public boolean isWriteAccess()
meth public java.lang.String toString()
meth public void postReadRequest(java.lang.Runnable)
meth public void postWriteRequest(java.lang.Runnable)
meth public void readAccess(java.lang.Runnable)
meth public void writeAccess(java.lang.Runnable)
supr java.lang.Object
hfds LOG,impl

CLSS public abstract interface static org.openide.util.Mutex$Action<%0 extends java.lang.Object>
 outer org.openide.util.Mutex
intf org.openide.util.Mutex$ExceptionAction<{org.openide.util.Mutex$Action%0}>
meth public abstract {org.openide.util.Mutex$Action%0} run()

CLSS public abstract interface static org.openide.util.Mutex$ExceptionAction<%0 extends java.lang.Object>
 outer org.openide.util.Mutex
meth public abstract {org.openide.util.Mutex$ExceptionAction%0} run() throws java.lang.Exception

CLSS public final static org.openide.util.Mutex$Privileged
 outer org.openide.util.Mutex
cons public init()
meth public boolean tryReadAccess(long)
meth public boolean tryWriteAccess(long)
meth public void enterReadAccess()
meth public void enterWriteAccess()
meth public void exitReadAccess()
meth public void exitWriteAccess()
supr java.lang.Object
hfds delegate

CLSS public org.openide.util.MutexException
cons public init(java.lang.Exception)
meth public java.lang.Exception getException()
meth public java.lang.Throwable getCause()
supr java.lang.Exception
hfds ex,serialVersionUID

CLSS public org.openide.util.NbBundle
cons public init()
 anno 0 java.lang.Deprecated()
innr public abstract interface static !annotation Messages
innr public abstract interface static ClassLoaderFinder
meth public !varargs static java.lang.String getMessage(java.lang.Class<?>,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object,java.lang.Object[])
meth public static <%0 extends java.lang.Object> {%%0} getLocalizedValue(java.util.Map<java.lang.String,{%%0}>,java.lang.String)
meth public static <%0 extends java.lang.Object> {%%0} getLocalizedValue(java.util.Map<java.lang.String,{%%0}>,java.lang.String,java.util.Locale)
meth public static java.lang.String getBranding()
meth public static java.lang.String getLocalizedValue(java.util.jar.Attributes,java.util.jar.Attributes$Name)
meth public static java.lang.String getLocalizedValue(java.util.jar.Attributes,java.util.jar.Attributes$Name,java.util.Locale)
meth public static java.lang.String getMessage(java.lang.Class<?>,java.lang.String)
meth public static java.lang.String getMessage(java.lang.Class<?>,java.lang.String,java.lang.Object)
meth public static java.lang.String getMessage(java.lang.Class<?>,java.lang.String,java.lang.Object,java.lang.Object)
meth public static java.lang.String getMessage(java.lang.Class<?>,java.lang.String,java.lang.Object,java.lang.Object,java.lang.Object)
meth public static java.lang.String getMessage(java.lang.Class<?>,java.lang.String,java.lang.Object[])
meth public static java.net.URL getLocalizedFile(java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.net.URL getLocalizedFile(java.lang.String,java.lang.String,java.util.Locale)
 anno 0 java.lang.Deprecated()
meth public static java.net.URL getLocalizedFile(java.lang.String,java.lang.String,java.util.Locale,java.lang.ClassLoader)
 anno 0 java.lang.Deprecated()
meth public static java.util.Iterator<java.lang.String> getLocalizingSuffixes()
meth public static java.util.ResourceBundle getBundle(java.lang.Class<?>)
meth public static java.util.ResourceBundle getBundle(java.lang.String)
meth public static java.util.ResourceBundle getBundle(java.lang.String,java.util.Locale)
meth public static java.util.ResourceBundle getBundle(java.lang.String,java.util.Locale,java.lang.ClassLoader)
meth public static void setBranding(java.lang.String)
meth public static void setClassLoaderFinder(org.openide.util.NbBundle$ClassLoaderFinder)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds LOG,USE_DEBUG_LOADER,brandingToken,bundleCache,localizedFileCache,utfThenIsoCharset,utfThenIsoCharsetOnlyUTF8
hcls AttributesMap,DebugLoader,LocaleIterator,MergedBundle,PBundle,UtfThenIsoCharset

CLSS public abstract interface static org.openide.util.NbBundle$ClassLoaderFinder
 outer org.openide.util.NbBundle
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.ClassLoader find()
 anno 0 java.lang.Deprecated()

CLSS public abstract interface static !annotation org.openide.util.NbBundle$Messages
 outer org.openide.util.NbBundle
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PACKAGE, TYPE, METHOD, CONSTRUCTOR, FIELD])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String[] value()

CLSS public org.openide.util.NbCollections
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> checkedMapByCopy(java.util.Map,java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,boolean)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> checkedMapByFilter(java.util.Map,java.lang.Class<{%%0}>,java.lang.Class<{%%1}>,boolean)
meth public static <%0 extends java.lang.Object> java.lang.Iterable<{%%0}> iterable(java.util.Enumeration<{%%0}>)
meth public static <%0 extends java.lang.Object> java.lang.Iterable<{%%0}> iterable(java.util.Iterator<{%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Enumeration<{%%0}> checkedEnumerationByFilter(java.util.Enumeration<?>,java.lang.Class<{%%0}>,boolean)
meth public static <%0 extends java.lang.Object> java.util.Iterator<{%%0}> checkedIteratorByFilter(java.util.Iterator,java.lang.Class<{%%0}>,boolean)
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> checkedListByCopy(java.util.List,java.lang.Class<{%%0}>,boolean)
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> checkedSetByCopy(java.util.Set,java.lang.Class<{%%0}>,boolean)
meth public static <%0 extends java.lang.Object> java.util.Set<{%%0}> checkedSetByFilter(java.util.Set,java.lang.Class<{%%0}>,boolean)
supr java.lang.Object
hfds LOG
hcls CheckedIterator,CheckedMap,CheckedSet

CLSS public final org.openide.util.NbPreferences
innr public abstract interface static Provider
meth public static java.util.prefs.Preferences forModule(java.lang.Class)
meth public static java.util.prefs.Preferences root()
supr java.lang.Object
hfds PREFS_IMPL

CLSS public abstract interface static org.openide.util.NbPreferences$Provider
 outer org.openide.util.NbPreferences
meth public abstract java.util.prefs.Preferences preferencesForModule(java.lang.Class)
meth public abstract java.util.prefs.Preferences preferencesRoot()

CLSS public final org.openide.util.NetworkSettings
cons public init()
innr public abstract static ProxyCredentialsProvider
meth public static <%0 extends java.lang.Object> {%%0} suppressAuthenticationDialog(java.util.concurrent.Callable<{%%0}>) throws java.lang.Exception
meth public static boolean isAuthenticationDialogSuppressed()
meth public static char[] getAuthenticationPassword(java.net.URI)
meth public static java.lang.String getAuthenticationUsername(java.net.URI)
meth public static java.lang.String getKeyForAuthenticationPassword(java.net.URI)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String getProxyHost(java.net.URI)
meth public static java.lang.String getProxyPort(java.net.URI)
supr java.lang.Object
hfds LOGGER,PROXY_AUTHENTICATION_PASSWORD,authenticationDialogSuppressed

CLSS public abstract static org.openide.util.NetworkSettings$ProxyCredentialsProvider
 outer org.openide.util.NetworkSettings
cons public init()
meth protected abstract boolean isProxyAuthentication(java.net.URI)
meth protected abstract char[] getProxyPassword(java.net.URI)
meth protected abstract java.lang.String getProxyHost(java.net.URI)
meth protected abstract java.lang.String getProxyPort(java.net.URI)
meth protected abstract java.lang.String getProxyUserName(java.net.URI)
supr java.lang.Object

CLSS public org.openide.util.NotImplementedException
cons public init()
cons public init(java.lang.String)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public final org.openide.util.Pair<%0 extends java.lang.Object, %1 extends java.lang.Object>
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.openide.util.Pair<{%%0},{%%1}> of({%%0},{%%1})
meth public {org.openide.util.Pair%0} first()
meth public {org.openide.util.Pair%1} second()
supr java.lang.Object
hfds first,second

CLSS public org.openide.util.Parameters
meth public static void javaIdentifier(java.lang.CharSequence,java.lang.CharSequence)
meth public static void javaIdentifierOrNull(java.lang.CharSequence,java.lang.CharSequence)
meth public static void notEmpty(java.lang.CharSequence,java.lang.CharSequence)
meth public static void notNull(java.lang.CharSequence,java.lang.Object)
meth public static void notWhitespace(java.lang.CharSequence,java.lang.CharSequence)
supr java.lang.Object

CLSS public org.openide.util.Queue<%0 extends java.lang.Object>
 anno 0 java.lang.Deprecated()
cons public init()
meth public void put({org.openide.util.Queue%0})
meth public {org.openide.util.Queue%0} get()
supr java.lang.Object
hfds queue

CLSS public final org.openide.util.RequestProcessor
cons public init()
cons public init(java.lang.Class<?>)
cons public init(java.lang.String)
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,boolean)
cons public init(java.lang.String,int,boolean,boolean)
innr public final Task
intf java.util.concurrent.ScheduledExecutorService
meth public <%0 extends java.lang.Object> java.util.List<java.util.concurrent.Future<{%%0}>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>) throws java.lang.InterruptedException
meth public <%0 extends java.lang.Object> java.util.List<java.util.concurrent.Future<{%%0}>> invokeAll(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>,long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.lang.Runnable,{%%0})
meth public <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> submit(java.util.concurrent.Callable<{%%0}>)
meth public <%0 extends java.lang.Object> java.util.concurrent.ScheduledFuture<{%%0}> schedule(java.util.concurrent.Callable<{%%0}>,long,java.util.concurrent.TimeUnit)
meth public <%0 extends java.lang.Object> {%%0} invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException
meth public <%0 extends java.lang.Object> {%%0} invokeAny(java.util.Collection<? extends java.util.concurrent.Callable<{%%0}>>,long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException,java.util.concurrent.ExecutionException,java.util.concurrent.TimeoutException
meth public boolean awaitTermination(long,java.util.concurrent.TimeUnit) throws java.lang.InterruptedException
meth public boolean isRequestProcessorThread()
meth public boolean isShutdown()
meth public boolean isTerminated()
meth public java.util.List<java.lang.Runnable> shutdownNow()
meth public java.util.concurrent.Future<?> submit(java.lang.Runnable)
meth public java.util.concurrent.ScheduledFuture<?> schedule(java.lang.Runnable,long,java.util.concurrent.TimeUnit)
meth public java.util.concurrent.ScheduledFuture<?> scheduleAtFixedRate(java.lang.Runnable,long,long,java.util.concurrent.TimeUnit)
meth public java.util.concurrent.ScheduledFuture<?> scheduleWithFixedDelay(java.lang.Runnable,long,long,java.util.concurrent.TimeUnit)
meth public org.openide.util.RequestProcessor$Task create(java.lang.Runnable)
meth public org.openide.util.RequestProcessor$Task create(java.lang.Runnable,boolean)
meth public org.openide.util.RequestProcessor$Task post(java.lang.Runnable)
meth public org.openide.util.RequestProcessor$Task post(java.lang.Runnable,int)
meth public org.openide.util.RequestProcessor$Task post(java.lang.Runnable,int,int)
meth public static org.openide.util.RequestProcessor getDefault()
meth public static org.openide.util.RequestProcessor$Task createRequest(java.lang.Runnable)
 anno 0 java.lang.Deprecated()
meth public static org.openide.util.RequestProcessor$Task postRequest(java.lang.Runnable)
 anno 0 java.lang.Deprecated()
meth public static org.openide.util.RequestProcessor$Task postRequest(java.lang.Runnable,int)
 anno 0 java.lang.Deprecated()
meth public static org.openide.util.RequestProcessor$Task postRequest(java.lang.Runnable,int,int)
 anno 0 java.lang.Deprecated()
meth public void execute(java.lang.Runnable)
meth public void shutdown()
meth public void stop()
supr java.lang.Object
hfds DEFAULT,SLOW,TOP_GROUP,UNLIMITED,counter,enableStackTraces,finishAwaitingTasks,inParallel,interruptThread,logger,name,processorLock,processors,queue,stopped,throughput,warnParallel
hcls CreatedItem,FastItem,FixedDelayTask,FixedRateTask,Item,Processor,RPFutureTask,RunnableWrapper,ScheduledRPFutureTask,SlowItem,TaskFutureWrapper,TickTac,TopLevelThreadGroup,WaitableCallable

CLSS public final org.openide.util.RequestProcessor$Task
 outer org.openide.util.RequestProcessor
intf org.openide.util.Cancellable
meth public boolean cancel()
meth public boolean waitFinished(long) throws java.lang.InterruptedException
meth public int getDelay()
meth public int getPriority()
meth public java.lang.String toString()
meth public void run()
meth public void schedule(int)
meth public void setPriority(int)
meth public void waitFinished()
supr org.openide.util.Task
hfds cancelled,item,lastThread,priority,time

CLSS public abstract org.openide.util.SharedClassObject
cons protected init()
intf java.io.Externalizable
meth protected boolean clearSharedData()
meth protected final java.lang.Object getLock()
meth protected final java.lang.Object getProperty(java.lang.Object)
meth protected final java.lang.Object putProperty(java.lang.Object,java.lang.Object)
meth protected final java.lang.Object putProperty(java.lang.String,java.lang.Object,boolean)
meth protected final void finalize() throws java.lang.Throwable
meth protected java.lang.Object writeReplace()
meth protected void addNotify()
meth protected void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected void initialize()
meth protected void removeNotify()
meth protected void reset()
meth public final boolean equals(java.lang.Object)
meth public final int hashCode()
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public static <%0 extends org.openide.util.SharedClassObject> {%%0} findObject(java.lang.Class<{%%0}>)
meth public static <%0 extends org.openide.util.SharedClassObject> {%%0} findObject(java.lang.Class<{%%0}>,boolean)
meth public void readExternal(java.io.ObjectInput) throws java.io.IOException,java.lang.ClassNotFoundException
meth public void writeExternal(java.io.ObjectOutput) throws java.io.IOException
supr java.lang.Object
hfds PROP_SUPPORT,addNotifySuper,alreadyWarnedAboutDupes,dataEntry,err,first,firstTrace,inReadExternal,initializeSuper,instancesBeingCreated,lock,prematureSystemOptionMutation,removeNotifySuper,serialVersionUID,systemOption,values,waitingOnSystemOption
hcls DataEntry,SetAccessibleAction,WriteReplace

CLSS public org.openide.util.Task
cons protected init()
cons public init(java.lang.Runnable)
fld public final static org.openide.util.Task EMPTY
intf java.lang.Runnable
meth protected final void notifyFinished()
meth protected final void notifyRunning()
meth public boolean waitFinished(long) throws java.lang.InterruptedException
meth public final boolean isFinished()
meth public java.lang.String toString()
meth public void addTaskListener(org.openide.util.TaskListener)
meth public void removeTaskListener(org.openide.util.TaskListener)
meth public void run()
meth public void waitFinished()
supr java.lang.Object
hfds LOG,RP,finished,list,overrides,run

CLSS public abstract interface org.openide.util.TaskListener
intf java.util.EventListener
meth public abstract void taskFinished(org.openide.util.Task)

CLSS public final org.openide.util.TopologicalSortException
meth public final java.util.List partialSort()
meth public final java.util.Set[] topologicalSets()
meth public final java.util.Set[] unsortableSets()
meth public final void printStackTrace(java.io.PrintStream)
meth public final void printStackTrace(java.io.PrintWriter)
meth public java.lang.String getMessage()
meth public java.lang.String toString()
supr java.lang.Exception
hfds counter,dualGraph,edges,result,vertexes
hcls Vertex

CLSS public abstract interface !annotation org.openide.util.URLStreamHandlerRegistration
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract java.lang.String[] protocol()

CLSS public abstract org.openide.util.Union2<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf java.io.Serializable
intf java.lang.Cloneable
meth public abstract boolean hasFirst()
meth public abstract boolean hasSecond()
meth public abstract org.openide.util.Union2<{org.openide.util.Union2%0},{org.openide.util.Union2%1}> clone()
meth public abstract {org.openide.util.Union2%0} first()
meth public abstract {org.openide.util.Union2%1} second()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.openide.util.Union2<{%%0},{%%1}> createFirst({%%0})
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.openide.util.Union2<{%%0},{%%1}> createSecond({%%1})
supr java.lang.Object
hfds serialVersionUID
hcls Union2First,Union2Second

CLSS public org.openide.util.UserCancelException
cons public init()
cons public init(java.lang.String)
supr java.io.IOException
hfds serialVersionUID

CLSS public abstract org.openide.util.UserQuestionException
cons public init()
cons public init(java.lang.String)
meth public abstract void confirmed() throws java.io.IOException
supr java.io.IOException
hfds serialVersionUID

CLSS public final org.openide.util.Utilities
fld public final static int OS_AIX = 64
fld public final static int OS_DEC = 1024
 anno 0 java.lang.Deprecated()
fld public final static int OS_FREEBSD = 131072
fld public final static int OS_HP = 32
fld public final static int OS_IRIX = 128
fld public final static int OS_LINUX = 16
fld public final static int OS_MAC = 4096
fld public final static int OS_OPENBSD = 1048576
fld public final static int OS_OS2 = 2048
fld public final static int OS_OTHER = 65536
fld public final static int OS_SOLARIS = 8
fld public final static int OS_SUNOS = 256
fld public final static int OS_TRU64 = 512
fld public final static int OS_UNIX_MASK = 1709048
 anno 0 java.lang.Deprecated()
fld public final static int OS_UNIX_OTHER = 524288
fld public final static int OS_VMS = 16384
fld public final static int OS_WIN2000 = 8192
fld public final static int OS_WIN95 = 2
fld public final static int OS_WIN98 = 4
fld public final static int OS_WINDOWS_MASK = 303111
 anno 0 java.lang.Deprecated()
fld public final static int OS_WINNT = 1
fld public final static int OS_WINVISTA = 262144
fld public final static int OS_WIN_OTHER = 32768
fld public final static int TYPICAL_WINDOWS_TASKBAR_HEIGHT = 27
innr public static UnorderableException
meth public static <%0 extends java.lang.Object> java.util.List<{%%0}> topologicalSort(java.util.Collection<? extends {%%0}>,java.util.Map<? super {%%0},? extends java.util.Collection<? extends {%%0}>>) throws org.openide.util.TopologicalSortException
meth public static boolean compareObjects(java.lang.Object,java.lang.Object)
meth public static boolean compareObjectsImpl(java.lang.Object,java.lang.Object,int)
meth public static boolean isJavaIdentifier(java.lang.String)
meth public static boolean isLargeFrameIcons()
 anno 0 java.lang.Deprecated()
meth public static boolean isMac()
meth public static boolean isUnix()
meth public static boolean isWindows()
meth public static int arrayHashCode(java.lang.Object[])
 anno 0 java.lang.Deprecated()
meth public static int getOperatingSystem()
meth public static int showJFileChooser(javax.swing.JFileChooser,java.awt.Component,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.awt.Component getFocusTraversableComponent(java.awt.Component)
meth public static java.awt.Cursor createCustomCursor(java.awt.Component,java.awt.Image,java.lang.String)
meth public static java.awt.Cursor createProgressCursor(java.awt.Component)
meth public static java.awt.Dimension getScreenSize()
 anno 0 java.lang.Deprecated()
meth public static java.awt.Image icon2Image(javax.swing.Icon)
 anno 0 java.lang.Deprecated()
meth public static java.awt.Image loadImage(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.awt.Image loadImage(java.lang.String,boolean)
 anno 0 java.lang.Deprecated()
meth public static java.awt.Image mergeImages(java.awt.Image,java.awt.Image,int,int)
 anno 0 java.lang.Deprecated()
meth public static java.awt.Rectangle findCenterBounds(java.awt.Dimension)
meth public static java.awt.Rectangle getUsableScreenBounds()
meth public static java.awt.Rectangle getUsableScreenBounds(java.awt.GraphicsConfiguration)
meth public static java.beans.BeanInfo getBeanInfo(java.lang.Class<?>) throws java.beans.IntrospectionException
meth public static java.beans.BeanInfo getBeanInfo(java.lang.Class<?>,java.lang.Class<?>) throws java.beans.IntrospectionException
meth public static java.io.File toFile(java.net.URI)
meth public static java.io.File toFile(java.net.URL)
 anno 0 java.lang.Deprecated()
meth public static java.lang.Class<?> getObjectType(java.lang.Class<?>)
meth public static java.lang.Class<?> getPrimitiveType(java.lang.Class<?>)
meth public static java.lang.Object toPrimitiveArray(java.lang.Object[])
meth public static java.lang.Object[] toObjectArray(java.lang.Object)
meth public static java.lang.String escapeParameters(java.lang.String[])
meth public static java.lang.String getClassName(java.lang.Class<?>)
meth public static java.lang.String getShortClassName(java.lang.Class<?>)
meth public static java.lang.String keyToString(javax.swing.KeyStroke)
meth public static java.lang.String keyToString(javax.swing.KeyStroke,boolean)
meth public static java.lang.String pureClassName(java.lang.String)
meth public static java.lang.String replaceString(java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String translate(java.lang.String)
meth public static java.lang.String wrapString(java.lang.String,int,boolean,boolean)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String wrapString(java.lang.String,int,java.text.BreakIterator,boolean)
meth public static java.lang.String[] parseParameters(java.lang.String)
meth public static java.lang.String[] wrapStringToArray(java.lang.String,int,boolean,boolean)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String[] wrapStringToArray(java.lang.String,int,java.text.BreakIterator,boolean)
meth public static java.lang.ref.ReferenceQueue<java.lang.Object> activeReferenceQueue()
meth public static java.net.URI toURI(java.io.File)
meth public static java.net.URL toURL(java.io.File) throws java.net.MalformedURLException
 anno 0 java.lang.Deprecated()
meth public static java.util.List partialSort(java.util.List,java.util.Comparator,boolean)
 anno 0 java.lang.Deprecated()
meth public static java.util.List<? extends javax.swing.Action> actionsForPath(java.lang.String)
meth public static javax.swing.JPopupMenu actionsToPopup(javax.swing.Action[],java.awt.Component)
meth public static javax.swing.JPopupMenu actionsToPopup(javax.swing.Action[],org.openide.util.Lookup)
meth public static javax.swing.KeyStroke stringToKey(java.lang.String)
meth public static javax.swing.KeyStroke[] stringToKeys(java.lang.String)
meth public static org.openide.util.Lookup actionsGlobalContext()
meth public static void attachInitJob(java.awt.Component,org.openide.util.AsyncGUIJob)
meth public static void disabledActionBeep()
supr java.lang.Object
hfds ALT_WILDCARD_MASK,CTRL_WILDCARD_MASK,LOG,TYPICAL_MACOSX_MENU_HEIGHT,clearIntrospector,doClear,global,namesAndValues,screenBoundsCache
hcls NamesAndValues

CLSS public static org.openide.util.Utilities$UnorderableException
 outer org.openide.util.Utilities
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,java.util.Collection,java.util.Map)
cons public init(java.util.Collection,java.util.Map)
meth public java.util.Collection getUnorderable()
meth public java.util.Map getDeps()
supr java.lang.RuntimeException
hfds deps,serialVersionUID,unorderable

CLSS public abstract org.openide.util.VectorIcon
cons protected init(int,int)
intf java.io.Serializable
intf javax.swing.Icon
meth protected abstract void paintIcon(java.awt.Component,java.awt.Graphics2D,int,int,double)
meth protected final static int round(double)
meth protected final static void setAntiAliasing(java.awt.Graphics2D,boolean)
meth public final int getIconHeight()
meth public final int getIconWidth()
meth public final void paintIcon(java.awt.Component,java.awt.Graphics,int,int)
supr java.lang.Object
hfds height,width

CLSS public final org.openide.util.WeakListeners
meth public static <%0 extends java.util.EventListener> {%%0} create(java.lang.Class<{%%0}>,java.lang.Class<? super {%%0}>,{%%0},java.lang.Object)
meth public static <%0 extends java.util.EventListener> {%%0} create(java.lang.Class<{%%0}>,{%%0},java.lang.Object)
meth public static java.beans.PropertyChangeListener propertyChange(java.beans.PropertyChangeListener,java.lang.Object)
meth public static java.beans.PropertyChangeListener propertyChange(java.beans.PropertyChangeListener,java.lang.String,java.lang.Object)
meth public static java.beans.VetoableChangeListener vetoableChange(java.beans.VetoableChangeListener,java.lang.Object)
meth public static java.beans.VetoableChangeListener vetoableChange(java.beans.VetoableChangeListener,java.lang.String,java.lang.Object)
meth public static javax.swing.event.ChangeListener change(javax.swing.event.ChangeListener,java.lang.Object)
meth public static javax.swing.event.DocumentListener document(javax.swing.event.DocumentListener,java.lang.Object)
supr java.lang.Object

CLSS public org.openide.util.WeakSet<%0 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(int,float)
cons public init(java.util.Collection<? extends {org.openide.util.WeakSet%0}>)
intf java.io.Serializable
intf java.lang.Cloneable
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public boolean add({org.openide.util.WeakSet%0})
meth public boolean contains(java.lang.Object)
meth public boolean containsAll(java.util.Collection<?>)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean retainAll(java.util.Collection<?>)
meth public int hashCode()
meth public int size()
meth public java.lang.Object clone()
meth public java.lang.Object[] toArray()
meth public java.lang.String toString()
meth public java.util.Iterator<{org.openide.util.WeakSet%0}> iterator()
meth public void clear()
meth public void resize(int)
meth public {org.openide.util.WeakSet%0} putIfAbsent({org.openide.util.WeakSet%0})
supr java.util.AbstractSet<{org.openide.util.WeakSet%0}>
hfds PRESENT,loadFactor,m,s,serialVersionUID
hcls SharedKeyWeakHashMap

CLSS public abstract org.openide.util.actions.ActionInvoker
cons protected init()
meth protected abstract void invokeAction(javax.swing.Action,java.awt.event.ActionEvent)
meth public static void invokeAction(javax.swing.Action,java.awt.event.ActionEvent,boolean,java.lang.Runnable)
supr java.lang.Object
hfds RP
hcls ActionRunnable

CLSS public abstract interface org.openide.util.actions.ActionPerformer
 anno 0 java.lang.Deprecated()
meth public abstract void performAction(org.openide.util.actions.SystemAction)

CLSS public abstract org.openide.util.actions.ActionPresenterProvider
cons protected init()
meth public abstract java.awt.Component createToolbarPresenter(javax.swing.Action)
meth public abstract java.awt.Component[] convertComponents(java.awt.Component)
meth public abstract javax.swing.JMenuItem createMenuPresenter(javax.swing.Action)
meth public abstract javax.swing.JMenuItem createPopupPresenter(javax.swing.Action)
meth public abstract javax.swing.JPopupMenu createEmptyPopup()
meth public static org.openide.util.actions.ActionPresenterProvider getDefault()
supr java.lang.Object
hcls Default

CLSS public abstract org.openide.util.actions.BooleanStateAction
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static java.lang.String PROP_BOOLEAN_STATE = "booleanState"
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected void initialize()
meth public boolean getBooleanState()
meth public java.awt.Component getToolbarPresenter()
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void setBooleanState(boolean)
supr org.openide.util.actions.SystemAction
hfds serialVersionUID

CLSS public abstract org.openide.util.actions.CallableSystemAction
cons public init()
intf org.openide.util.actions.Presenter$Menu
intf org.openide.util.actions.Presenter$Popup
intf org.openide.util.actions.Presenter$Toolbar
meth protected boolean asynchronous()
meth public abstract void performAction()
meth public java.awt.Component getToolbarPresenter()
meth public javax.swing.JMenuItem getMenuPresenter()
meth public javax.swing.JMenuItem getPopupPresenter()
meth public void actionPerformed(java.awt.event.ActionEvent)
supr org.openide.util.actions.SystemAction
hfds DEFAULT_ASYNCH,serialVersionUID,warnedAsynchronousActions

CLSS public abstract org.openide.util.actions.CallbackSystemAction
cons public init()
intf org.openide.util.ContextAwareAction
meth protected void initialize()
meth public boolean getSurviveFocusChange()
meth public java.lang.Object getActionMapKey()
meth public javax.swing.Action createContextAwareInstance(org.openide.util.Lookup)
meth public org.openide.util.actions.ActionPerformer getActionPerformer()
 anno 0 java.lang.Deprecated()
meth public void actionPerformed(java.awt.event.ActionEvent)
meth public void performAction()
 anno 0 java.lang.Deprecated()
meth public void setActionPerformer(org.openide.util.actions.ActionPerformer)
 anno 0 java.lang.Deprecated()
meth public void setSurviveFocusChange(boolean)
supr org.openide.util.actions.CallableSystemAction
hfds LISTENER,PROP_ACTION_PERFORMER,err,notSurviving,serialVersionUID,surviving
hcls ActionDelegateListener,DelegateAction,GlobalManager,WeakAction

CLSS public abstract interface org.openide.util.actions.Presenter
innr public abstract interface static Menu
innr public abstract interface static Popup
innr public abstract interface static Toolbar

CLSS public abstract interface static org.openide.util.actions.Presenter$Menu
 outer org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter
meth public abstract javax.swing.JMenuItem getMenuPresenter()

CLSS public abstract interface static org.openide.util.actions.Presenter$Popup
 outer org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter
meth public abstract javax.swing.JMenuItem getPopupPresenter()

CLSS public abstract interface static org.openide.util.actions.Presenter$Toolbar
 outer org.openide.util.actions.Presenter
intf org.openide.util.actions.Presenter
meth public abstract java.awt.Component getToolbarPresenter()

CLSS public abstract org.openide.util.actions.SystemAction
cons public init()
fld public final static java.lang.String PROP_ENABLED = "enabled"
fld public final static java.lang.String PROP_ICON = "icon"
intf javax.swing.Action
intf org.openide.util.HelpCtx$Provider
meth protected boolean clearSharedData()
meth protected java.lang.String iconResource()
meth protected void initialize()
meth public abstract java.lang.String getName()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract void actionPerformed(java.awt.event.ActionEvent)
meth public boolean isEnabled()
meth public final java.lang.Object getValue(java.lang.String)
meth public final javax.swing.Icon getIcon()
meth public final javax.swing.Icon getIcon(boolean)
meth public final void putValue(java.lang.String,java.lang.Object)
meth public final void setIcon(javax.swing.Icon)
meth public static <%0 extends org.openide.util.actions.SystemAction> {%%0} get(java.lang.Class<{%%0}>)
meth public static javax.swing.JPopupMenu createPopupMenu(org.openide.util.actions.SystemAction[])
 anno 0 java.lang.Deprecated()
meth public static javax.swing.JToolBar createToolbarPresenter(org.openide.util.actions.SystemAction[])
meth public static org.openide.util.actions.SystemAction[] linkActions(org.openide.util.actions.SystemAction[],org.openide.util.actions.SystemAction[])
meth public void setEnabled(boolean)
supr org.openide.util.SharedClassObject
hfds LOG,PROP_ICON_TEXTUAL,relativeIconResourceClasses,serialVersionUID
hcls ComponentIcon

CLSS public final org.openide.util.datatransfer.ClipboardEvent
meth public boolean isConsumed()
meth public org.openide.util.datatransfer.ExClipboard getClipboard()
meth public void consume()
supr java.util.EventObject
hfds consumed,serialVersionUID

CLSS public abstract interface org.openide.util.datatransfer.ClipboardListener
intf java.util.EventListener
meth public abstract void clipboardChanged(org.openide.util.datatransfer.ClipboardEvent)

CLSS public abstract org.openide.util.datatransfer.ExClipboard
cons public init(java.lang.String)
innr public abstract interface static Convertor
meth protected abstract org.openide.util.datatransfer.ExClipboard$Convertor[] getConvertors()
meth protected final void fireClipboardChange()
meth public final void addClipboardListener(org.openide.util.datatransfer.ClipboardListener)
meth public final void removeClipboardListener(org.openide.util.datatransfer.ClipboardListener)
meth public java.awt.datatransfer.Transferable convert(java.awt.datatransfer.Transferable)
meth public static void transferableAccepted(java.awt.datatransfer.Transferable,int)
meth public static void transferableOwnershipLost(java.awt.datatransfer.Transferable)
meth public static void transferableRejected(java.awt.datatransfer.Transferable)
meth public void setContents(java.awt.datatransfer.Transferable,java.awt.datatransfer.ClipboardOwner)
supr java.awt.datatransfer.Clipboard
hfds listeners

CLSS public abstract interface static org.openide.util.datatransfer.ExClipboard$Convertor
 outer org.openide.util.datatransfer.ExClipboard
meth public abstract java.awt.datatransfer.Transferable convert(java.awt.datatransfer.Transferable)

CLSS public org.openide.util.datatransfer.ExTransferable
fld public final static java.awt.datatransfer.DataFlavor multiFlavor
fld public final static java.awt.datatransfer.Transferable EMPTY
innr public abstract static Single
innr public static Multi
intf java.awt.datatransfer.Transferable
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public final void addTransferListener(org.openide.util.datatransfer.TransferListener)
meth public final void removeTransferListener(org.openide.util.datatransfer.TransferListener)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public static org.openide.util.datatransfer.ExTransferable create(java.awt.datatransfer.Transferable)
meth public void put(org.openide.util.datatransfer.ExTransferable$Single)
meth public void remove(java.awt.datatransfer.DataFlavor)
supr java.lang.Object
hfds listeners,map
hcls Empty

CLSS public static org.openide.util.datatransfer.ExTransferable$Multi
 outer org.openide.util.datatransfer.ExTransferable
cons public init(java.awt.datatransfer.Transferable[])
intf java.awt.datatransfer.Transferable
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
supr java.lang.Object
hfds flavorList,transferObject
hcls TransferObjectImpl

CLSS public abstract static org.openide.util.datatransfer.ExTransferable$Single
 outer org.openide.util.datatransfer.ExTransferable
cons public init(java.awt.datatransfer.DataFlavor)
intf java.awt.datatransfer.Transferable
meth protected abstract java.lang.Object getData() throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
supr java.lang.Object
hfds flavor

CLSS public abstract interface org.openide.util.datatransfer.MultiTransferObject
meth public abstract boolean areDataFlavorsSupported(java.awt.datatransfer.DataFlavor[])
meth public abstract boolean isDataFlavorSupported(int,java.awt.datatransfer.DataFlavor)
meth public abstract int getCount()
meth public abstract java.awt.datatransfer.DataFlavor[] getTransferDataFlavors(int)
meth public abstract java.awt.datatransfer.Transferable getTransferableAt(int)
meth public abstract java.lang.Object getTransferData(int,java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException

CLSS public abstract org.openide.util.datatransfer.NewType
cons public init()
intf org.openide.util.HelpCtx$Provider
meth public abstract void create() throws java.io.IOException
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr java.lang.Object

CLSS public abstract org.openide.util.datatransfer.PasteType
cons public init()
intf org.openide.util.HelpCtx$Provider
meth public abstract java.awt.datatransfer.Transferable paste() throws java.io.IOException
meth public java.lang.String getName()
meth public org.openide.util.HelpCtx getHelpCtx()
supr java.lang.Object

CLSS public abstract interface org.openide.util.datatransfer.TransferListener
intf java.util.EventListener
meth public abstract void accepted(int)
meth public abstract void ownershipLost()
meth public abstract void rejected()

CLSS public org.openide.util.io.FoldingIOException
cons public init(java.lang.Throwable)
 anno 0 java.lang.Deprecated()
meth public java.lang.String getLocalizedMessage()
meth public java.lang.String toString()
meth public void printStackTrace()
meth public void printStackTrace(java.io.PrintStream)
meth public void printStackTrace(java.io.PrintWriter)
supr java.io.IOException
hfds serialVersionUID,t

CLSS public final org.openide.util.io.NbMarshalledObject
cons public init(java.lang.Object) throws java.io.IOException
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object get() throws java.io.IOException,java.lang.ClassNotFoundException
supr java.lang.Object
hfds HEX,hash,objBytes,serialVersionUID

CLSS public org.openide.util.io.NbObjectInputStream
cons public init(java.io.InputStream) throws java.io.IOException
meth protected java.io.ObjectStreamClass readClassDescriptor() throws java.io.IOException,java.lang.ClassNotFoundException
meth protected java.lang.Class resolveClass(java.io.ObjectStreamClass) throws java.io.IOException,java.lang.ClassNotFoundException
meth public static java.lang.Object readSafely(java.io.ObjectInput) throws java.io.IOException
meth public static void skipSafely(java.io.ObjectInput) throws java.io.IOException
supr java.io.ObjectInputStream

CLSS public org.openide.util.io.NbObjectOutputStream
cons public init(java.io.OutputStream) throws java.io.IOException
meth protected void annotateClass(java.lang.Class) throws java.io.IOException
meth public java.lang.Object replaceObject(java.lang.Object) throws java.io.IOException
meth public static void writeSafely(java.io.ObjectOutput,java.lang.Object) throws java.io.IOException
supr java.io.ObjectOutputStream
hfds SVUID,alreadyReported,examinedClasses,serializing

CLSS public org.openide.util.io.NullInputStream
cons public init()
fld public boolean throwException
meth public int read() throws java.io.IOException
supr java.io.InputStream

CLSS public org.openide.util.io.NullOutputStream
cons public init()
fld public boolean throwException
meth public void write(int) throws java.io.IOException
supr java.io.OutputStream

CLSS public org.openide.util.io.OperationException
cons public init(java.lang.Exception)
meth public java.lang.Exception getException()
meth public java.lang.String getMessage()
meth public java.lang.Throwable getCause()
supr java.io.IOException
hfds ex,serialVersionUID

CLSS public org.openide.util.io.ReaderInputStream
cons public init(java.io.Reader) throws java.io.IOException
cons public init(java.io.Reader,java.lang.String) throws java.io.IOException
meth public int available() throws java.io.IOException
meth public int read() throws java.io.IOException
meth public int read(byte[],int,int) throws java.io.IOException
meth public void close() throws java.io.IOException
supr java.io.InputStream
hfds osw,pis,pos,reader

CLSS public org.openide.util.io.SafeException
cons public init(java.lang.Exception)
meth public java.lang.Exception getException()
meth public java.lang.Throwable getCause()
supr org.openide.util.io.FoldingIOException
hfds ex,serialVersionUID

CLSS public abstract interface !annotation org.openide.util.lookup.NamedServiceDefinition
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String position()
meth public abstract java.lang.Class<?>[] serviceType()
meth public abstract java.lang.String path()

CLSS public abstract interface org.openide.util.spi.MutexEventProvider
meth public abstract org.openide.util.spi.MutexImplementation createMutex()

CLSS public abstract interface org.openide.util.spi.MutexImplementation
meth public abstract <%0 extends java.lang.Object> {%%0} readAccess(org.openide.util.Mutex$ExceptionAction<{%%0}>) throws org.openide.util.MutexException
meth public abstract <%0 extends java.lang.Object> {%%0} writeAccess(org.openide.util.Mutex$ExceptionAction<{%%0}>) throws org.openide.util.MutexException
meth public abstract boolean isReadAccess()
meth public abstract boolean isWriteAccess()
meth public abstract void postReadRequest(java.lang.Runnable)
meth public abstract void postWriteRequest(java.lang.Runnable)
meth public abstract void readAccess(java.lang.Runnable)
meth public abstract void writeAccess(java.lang.Runnable)

CLSS public abstract interface org.openide.util.spi.SVGLoader
meth public abstract javax.swing.Icon loadIcon(java.net.URL) throws java.io.IOException

CLSS public abstract org.openide.xml.EntityCatalog
cons protected init()
fld public final static java.lang.String PUBLIC_ID = "-//NetBeans//Entity Mapping Registration 1.0//EN"
 anno 0 java.lang.Deprecated()
intf org.xml.sax.EntityResolver
meth public static org.openide.xml.EntityCatalog getDefault()
supr java.lang.Object
hfds instance
hcls Forwarder

CLSS public final org.openide.xml.XMLUtil
meth public static byte[] fromHex(char[],int,int) throws java.io.IOException
meth public static java.lang.String findText(org.w3c.dom.Node)
meth public static java.lang.String toAttributeValue(java.lang.String) throws java.io.CharConversionException
meth public static java.lang.String toElementContent(java.lang.String) throws java.io.CharConversionException
meth public static java.lang.String toHex(byte[],int,int)
meth public static java.util.List<org.w3c.dom.Element> findSubElements(org.w3c.dom.Element)
meth public static org.w3c.dom.Document createDocument(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public static org.w3c.dom.Document parse(org.xml.sax.InputSource,boolean,boolean,org.xml.sax.ErrorHandler,org.xml.sax.EntityResolver) throws java.io.IOException,org.xml.sax.SAXException
meth public static org.w3c.dom.Element findElement(org.w3c.dom.Element,java.lang.String,java.lang.String)
meth public static org.w3c.dom.Element translateXML(org.w3c.dom.Element,java.lang.String)
meth public static org.xml.sax.ErrorHandler defaultErrorHandler()
meth public static org.xml.sax.XMLReader createXMLReader() throws org.xml.sax.SAXException
meth public static org.xml.sax.XMLReader createXMLReader(boolean) throws org.xml.sax.SAXException
meth public static org.xml.sax.XMLReader createXMLReader(boolean,boolean) throws org.xml.sax.SAXException
meth public static void appendChildElement(org.w3c.dom.Element,org.w3c.dom.Element,java.lang.String[])
meth public static void copyDocument(org.w3c.dom.Element,org.w3c.dom.Element,java.lang.String)
meth public static void validate(org.w3c.dom.Element,javax.xml.validation.Schema) throws org.xml.sax.SAXException
meth public static void write(org.w3c.dom.Document,java.io.OutputStream,java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds DEC2HEX,IDENTITY_XSLT_WITH_INDENT,ORACLE_IS_STANDALONE,doms,saxes
hcls ErrHandler

CLSS public abstract interface org.xml.sax.EntityResolver
meth public abstract org.xml.sax.InputSource resolveEntity(java.lang.String,java.lang.String) throws java.io.IOException,org.xml.sax.SAXException

