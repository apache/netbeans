#Signature file v4.1
#Version 3.5

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

CLSS public java.net.DatagramSocket
cons protected init(java.net.DatagramSocketImpl)
cons public init() throws java.net.SocketException
cons public init(int) throws java.net.SocketException
cons public init(int,java.net.InetAddress) throws java.net.SocketException
cons public init(java.net.SocketAddress) throws java.net.SocketException
intf java.io.Closeable
meth public boolean getBroadcast() throws java.net.SocketException
meth public boolean getReuseAddress() throws java.net.SocketException
meth public boolean isBound()
meth public boolean isClosed()
meth public boolean isConnected()
meth public int getLocalPort()
meth public int getPort()
meth public int getReceiveBufferSize() throws java.net.SocketException
meth public int getSendBufferSize() throws java.net.SocketException
meth public int getSoTimeout() throws java.net.SocketException
meth public int getTrafficClass() throws java.net.SocketException
meth public java.net.InetAddress getInetAddress()
meth public java.net.InetAddress getLocalAddress()
meth public java.net.SocketAddress getLocalSocketAddress()
meth public java.net.SocketAddress getRemoteSocketAddress()
meth public java.nio.channels.DatagramChannel getChannel()
meth public static void setDatagramSocketImplFactory(java.net.DatagramSocketImplFactory) throws java.io.IOException
meth public void bind(java.net.SocketAddress) throws java.net.SocketException
meth public void close()
meth public void connect(java.net.InetAddress,int)
meth public void connect(java.net.SocketAddress) throws java.net.SocketException
meth public void disconnect()
meth public void receive(java.net.DatagramPacket) throws java.io.IOException
meth public void send(java.net.DatagramPacket) throws java.io.IOException
meth public void setBroadcast(boolean) throws java.net.SocketException
meth public void setReceiveBufferSize(int) throws java.net.SocketException
meth public void setReuseAddress(boolean) throws java.net.SocketException
meth public void setSendBufferSize(int) throws java.net.SocketException
meth public void setSoTimeout(int) throws java.net.SocketException
meth public void setTrafficClass(int) throws java.net.SocketException
supr java.lang.Object

CLSS public abstract java.net.DatagramSocketImpl
cons public init()
fld protected int localPort
fld protected java.io.FileDescriptor fd
intf java.net.SocketOptions
meth protected abstract byte getTTL() throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth protected abstract int getTimeToLive() throws java.io.IOException
meth protected abstract int peek(java.net.InetAddress) throws java.io.IOException
meth protected abstract int peekData(java.net.DatagramPacket) throws java.io.IOException
meth protected abstract void bind(int,java.net.InetAddress) throws java.net.SocketException
meth protected abstract void close()
meth protected abstract void create() throws java.net.SocketException
meth protected abstract void join(java.net.InetAddress) throws java.io.IOException
meth protected abstract void joinGroup(java.net.SocketAddress,java.net.NetworkInterface) throws java.io.IOException
meth protected abstract void leave(java.net.InetAddress) throws java.io.IOException
meth protected abstract void leaveGroup(java.net.SocketAddress,java.net.NetworkInterface) throws java.io.IOException
meth protected abstract void receive(java.net.DatagramPacket) throws java.io.IOException
meth protected abstract void send(java.net.DatagramPacket) throws java.io.IOException
meth protected abstract void setTTL(byte) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth protected abstract void setTimeToLive(int) throws java.io.IOException
meth protected int getLocalPort()
meth protected java.io.FileDescriptor getFileDescriptor()
meth protected void connect(java.net.InetAddress,int) throws java.net.SocketException
meth protected void disconnect()
supr java.lang.Object

CLSS public java.net.InetSocketAddress
cons public init(int)
cons public init(java.lang.String,int)
cons public init(java.net.InetAddress,int)
meth public final boolean equals(java.lang.Object)
meth public final boolean isUnresolved()
meth public final int getPort()
meth public final int hashCode()
meth public final java.lang.String getHostName()
meth public final java.lang.String getHostString()
meth public final java.net.InetAddress getAddress()
meth public java.lang.String toString()
meth public static java.net.InetSocketAddress createUnresolved(java.lang.String,int)
supr java.net.SocketAddress

CLSS public abstract interface java.net.ProtocolFamily
meth public abstract java.lang.String name()

CLSS public java.net.ServerSocket
cons public init() throws java.io.IOException
cons public init(int) throws java.io.IOException
cons public init(int,int) throws java.io.IOException
cons public init(int,int,java.net.InetAddress) throws java.io.IOException
intf java.io.Closeable
meth protected final void implAccept(java.net.Socket) throws java.io.IOException
meth public boolean getReuseAddress() throws java.net.SocketException
meth public boolean isBound()
meth public boolean isClosed()
meth public int getLocalPort()
meth public int getReceiveBufferSize() throws java.net.SocketException
meth public int getSoTimeout() throws java.io.IOException
meth public java.lang.String toString()
meth public java.net.InetAddress getInetAddress()
meth public java.net.Socket accept() throws java.io.IOException
meth public java.net.SocketAddress getLocalSocketAddress()
meth public java.nio.channels.ServerSocketChannel getChannel()
meth public static void setSocketFactory(java.net.SocketImplFactory) throws java.io.IOException
meth public void bind(java.net.SocketAddress) throws java.io.IOException
meth public void bind(java.net.SocketAddress,int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void setPerformancePreferences(int,int,int)
meth public void setReceiveBufferSize(int) throws java.net.SocketException
meth public void setReuseAddress(boolean) throws java.net.SocketException
meth public void setSoTimeout(int) throws java.net.SocketException
supr java.lang.Object

CLSS public java.net.Socket
cons protected init(java.net.SocketImpl) throws java.net.SocketException
cons public init()
cons public init(java.lang.String,int) throws java.io.IOException
cons public init(java.lang.String,int,boolean) throws java.io.IOException
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String,int,java.net.InetAddress,int) throws java.io.IOException
cons public init(java.net.InetAddress,int) throws java.io.IOException
cons public init(java.net.InetAddress,int,boolean) throws java.io.IOException
 anno 0 java.lang.Deprecated()
cons public init(java.net.InetAddress,int,java.net.InetAddress,int) throws java.io.IOException
cons public init(java.net.Proxy)
intf java.io.Closeable
meth public boolean getKeepAlive() throws java.net.SocketException
meth public boolean getOOBInline() throws java.net.SocketException
meth public boolean getReuseAddress() throws java.net.SocketException
meth public boolean getTcpNoDelay() throws java.net.SocketException
meth public boolean isBound()
meth public boolean isClosed()
meth public boolean isConnected()
meth public boolean isInputShutdown()
meth public boolean isOutputShutdown()
meth public int getLocalPort()
meth public int getPort()
meth public int getReceiveBufferSize() throws java.net.SocketException
meth public int getSendBufferSize() throws java.net.SocketException
meth public int getSoLinger() throws java.net.SocketException
meth public int getSoTimeout() throws java.net.SocketException
meth public int getTrafficClass() throws java.net.SocketException
meth public java.io.InputStream getInputStream() throws java.io.IOException
meth public java.io.OutputStream getOutputStream() throws java.io.IOException
meth public java.lang.String toString()
meth public java.net.InetAddress getInetAddress()
meth public java.net.InetAddress getLocalAddress()
meth public java.net.SocketAddress getLocalSocketAddress()
meth public java.net.SocketAddress getRemoteSocketAddress()
meth public java.nio.channels.SocketChannel getChannel()
meth public static void setSocketImplFactory(java.net.SocketImplFactory) throws java.io.IOException
meth public void bind(java.net.SocketAddress) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void connect(java.net.SocketAddress) throws java.io.IOException
meth public void connect(java.net.SocketAddress,int) throws java.io.IOException
meth public void sendUrgentData(int) throws java.io.IOException
meth public void setKeepAlive(boolean) throws java.net.SocketException
meth public void setOOBInline(boolean) throws java.net.SocketException
meth public void setPerformancePreferences(int,int,int)
meth public void setReceiveBufferSize(int) throws java.net.SocketException
meth public void setReuseAddress(boolean) throws java.net.SocketException
meth public void setSendBufferSize(int) throws java.net.SocketException
meth public void setSoLinger(boolean,int) throws java.net.SocketException
meth public void setSoTimeout(int) throws java.net.SocketException
meth public void setTcpNoDelay(boolean) throws java.net.SocketException
meth public void setTrafficClass(int) throws java.net.SocketException
meth public void shutdownInput() throws java.io.IOException
meth public void shutdownOutput() throws java.io.IOException
supr java.lang.Object

CLSS public abstract java.net.SocketAddress
cons public init()
intf java.io.Serializable
supr java.lang.Object

CLSS public abstract java.net.SocketImpl
cons public init()
fld protected int localport
fld protected int port
fld protected java.io.FileDescriptor fd
fld protected java.net.InetAddress address
intf java.net.SocketOptions
meth protected abstract int available() throws java.io.IOException
meth protected abstract java.io.InputStream getInputStream() throws java.io.IOException
meth protected abstract java.io.OutputStream getOutputStream() throws java.io.IOException
meth protected abstract void accept(java.net.SocketImpl) throws java.io.IOException
meth protected abstract void bind(java.net.InetAddress,int) throws java.io.IOException
meth protected abstract void close() throws java.io.IOException
meth protected abstract void connect(java.lang.String,int) throws java.io.IOException
meth protected abstract void connect(java.net.InetAddress,int) throws java.io.IOException
meth protected abstract void connect(java.net.SocketAddress,int) throws java.io.IOException
meth protected abstract void create(boolean) throws java.io.IOException
meth protected abstract void listen(int) throws java.io.IOException
meth protected abstract void sendUrgentData(int) throws java.io.IOException
meth protected boolean supportsUrgentData()
meth protected int getLocalPort()
meth protected int getPort()
meth protected java.io.FileDescriptor getFileDescriptor()
meth protected java.net.InetAddress getInetAddress()
meth protected void setPerformancePreferences(int,int,int)
meth protected void shutdownInput() throws java.io.IOException
meth protected void shutdownOutput() throws java.io.IOException
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface java.net.SocketOption<%0 extends java.lang.Object>
meth public abstract java.lang.Class<{java.net.SocketOption%0}> type()
meth public abstract java.lang.String name()

CLSS public abstract interface java.net.SocketOptions
fld public final static int IP_MULTICAST_IF = 16
fld public final static int IP_MULTICAST_IF2 = 31
fld public final static int IP_MULTICAST_LOOP = 18
fld public final static int IP_TOS = 3
fld public final static int SO_BINDADDR = 15
fld public final static int SO_BROADCAST = 32
fld public final static int SO_KEEPALIVE = 8
fld public final static int SO_LINGER = 128
fld public final static int SO_OOBINLINE = 4099
fld public final static int SO_RCVBUF = 4098
fld public final static int SO_REUSEADDR = 4
fld public final static int SO_SNDBUF = 4097
fld public final static int SO_TIMEOUT = 4102
fld public final static int TCP_NODELAY = 1
meth public abstract java.lang.Object getOption(int) throws java.net.SocketException
meth public abstract void setOption(int,java.lang.Object) throws java.net.SocketException

CLSS public abstract interface java.nio.channels.ByteChannel
intf java.nio.channels.ReadableByteChannel
intf java.nio.channels.WritableByteChannel

CLSS public abstract interface java.nio.channels.Channel
intf java.io.Closeable
meth public abstract boolean isOpen()
meth public abstract void close() throws java.io.IOException

CLSS public abstract java.nio.channels.DatagramChannel
cons protected init(java.nio.channels.spi.SelectorProvider)
intf java.nio.channels.ByteChannel
intf java.nio.channels.GatheringByteChannel
intf java.nio.channels.MulticastChannel
intf java.nio.channels.ScatteringByteChannel
meth public abstract <%0 extends java.lang.Object> java.nio.channels.DatagramChannel setOption(java.net.SocketOption<{%%0}>,{%%0}) throws java.io.IOException
meth public abstract boolean isConnected()
meth public abstract int read(java.nio.ByteBuffer) throws java.io.IOException
meth public abstract int send(java.nio.ByteBuffer,java.net.SocketAddress) throws java.io.IOException
meth public abstract int write(java.nio.ByteBuffer) throws java.io.IOException
meth public abstract java.net.DatagramSocket socket()
meth public abstract java.net.SocketAddress getLocalAddress() throws java.io.IOException
meth public abstract java.net.SocketAddress getRemoteAddress() throws java.io.IOException
meth public abstract java.net.SocketAddress receive(java.nio.ByteBuffer) throws java.io.IOException
meth public abstract java.nio.channels.DatagramChannel bind(java.net.SocketAddress) throws java.io.IOException
meth public abstract java.nio.channels.DatagramChannel connect(java.net.SocketAddress) throws java.io.IOException
meth public abstract java.nio.channels.DatagramChannel disconnect() throws java.io.IOException
meth public abstract long read(java.nio.ByteBuffer[],int,int) throws java.io.IOException
meth public abstract long write(java.nio.ByteBuffer[],int,int) throws java.io.IOException
meth public final int validOps()
meth public final long read(java.nio.ByteBuffer[]) throws java.io.IOException
meth public final long write(java.nio.ByteBuffer[]) throws java.io.IOException
meth public static java.nio.channels.DatagramChannel open() throws java.io.IOException
meth public static java.nio.channels.DatagramChannel open(java.net.ProtocolFamily) throws java.io.IOException
supr java.nio.channels.spi.AbstractSelectableChannel

CLSS public abstract interface java.nio.channels.GatheringByteChannel
intf java.nio.channels.WritableByteChannel
meth public abstract long write(java.nio.ByteBuffer[]) throws java.io.IOException
meth public abstract long write(java.nio.ByteBuffer[],int,int) throws java.io.IOException

CLSS public abstract interface java.nio.channels.InterruptibleChannel
intf java.nio.channels.Channel
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.nio.channels.MulticastChannel
intf java.nio.channels.NetworkChannel
meth public abstract java.nio.channels.MembershipKey join(java.net.InetAddress,java.net.NetworkInterface) throws java.io.IOException
meth public abstract java.nio.channels.MembershipKey join(java.net.InetAddress,java.net.NetworkInterface,java.net.InetAddress) throws java.io.IOException
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.nio.channels.NetworkChannel
intf java.nio.channels.Channel
meth public abstract <%0 extends java.lang.Object> java.nio.channels.NetworkChannel setOption(java.net.SocketOption<{%%0}>,{%%0}) throws java.io.IOException
meth public abstract <%0 extends java.lang.Object> {%%0} getOption(java.net.SocketOption<{%%0}>) throws java.io.IOException
meth public abstract java.net.SocketAddress getLocalAddress() throws java.io.IOException
meth public abstract java.nio.channels.NetworkChannel bind(java.net.SocketAddress) throws java.io.IOException
meth public abstract java.util.Set<java.net.SocketOption<?>> supportedOptions()

CLSS public abstract java.nio.channels.Pipe
cons protected init()
innr public abstract static SinkChannel
innr public abstract static SourceChannel
meth public abstract java.nio.channels.Pipe$SinkChannel sink()
meth public abstract java.nio.channels.Pipe$SourceChannel source()
meth public static java.nio.channels.Pipe open() throws java.io.IOException
supr java.lang.Object

CLSS public abstract static java.nio.channels.Pipe$SinkChannel
 outer java.nio.channels.Pipe
cons protected init(java.nio.channels.spi.SelectorProvider)
intf java.nio.channels.GatheringByteChannel
intf java.nio.channels.WritableByteChannel
meth public final int validOps()
supr java.nio.channels.spi.AbstractSelectableChannel

CLSS public abstract static java.nio.channels.Pipe$SourceChannel
 outer java.nio.channels.Pipe
cons protected init(java.nio.channels.spi.SelectorProvider)
intf java.nio.channels.ReadableByteChannel
intf java.nio.channels.ScatteringByteChannel
meth public final int validOps()
supr java.nio.channels.spi.AbstractSelectableChannel

CLSS public abstract interface java.nio.channels.ReadableByteChannel
intf java.nio.channels.Channel
meth public abstract int read(java.nio.ByteBuffer) throws java.io.IOException

CLSS public abstract interface java.nio.channels.ScatteringByteChannel
intf java.nio.channels.ReadableByteChannel
meth public abstract long read(java.nio.ByteBuffer[]) throws java.io.IOException
meth public abstract long read(java.nio.ByteBuffer[],int,int) throws java.io.IOException

CLSS public abstract java.nio.channels.SelectableChannel
cons protected init()
intf java.nio.channels.Channel
meth public abstract boolean isBlocking()
meth public abstract boolean isRegistered()
meth public abstract int validOps()
meth public abstract java.lang.Object blockingLock()
meth public abstract java.nio.channels.SelectableChannel configureBlocking(boolean) throws java.io.IOException
meth public abstract java.nio.channels.SelectionKey keyFor(java.nio.channels.Selector)
meth public abstract java.nio.channels.SelectionKey register(java.nio.channels.Selector,int,java.lang.Object) throws java.nio.channels.ClosedChannelException
meth public abstract java.nio.channels.spi.SelectorProvider provider()
meth public final java.nio.channels.SelectionKey register(java.nio.channels.Selector,int) throws java.nio.channels.ClosedChannelException
supr java.nio.channels.spi.AbstractInterruptibleChannel

CLSS public abstract java.nio.channels.ServerSocketChannel
cons protected init(java.nio.channels.spi.SelectorProvider)
intf java.nio.channels.NetworkChannel
meth public abstract <%0 extends java.lang.Object> java.nio.channels.ServerSocketChannel setOption(java.net.SocketOption<{%%0}>,{%%0}) throws java.io.IOException
meth public abstract java.net.ServerSocket socket()
meth public abstract java.net.SocketAddress getLocalAddress() throws java.io.IOException
meth public abstract java.nio.channels.ServerSocketChannel bind(java.net.SocketAddress,int) throws java.io.IOException
meth public abstract java.nio.channels.SocketChannel accept() throws java.io.IOException
meth public final int validOps()
meth public final java.nio.channels.ServerSocketChannel bind(java.net.SocketAddress) throws java.io.IOException
meth public static java.nio.channels.ServerSocketChannel open() throws java.io.IOException
supr java.nio.channels.spi.AbstractSelectableChannel

CLSS public abstract java.nio.channels.SocketChannel
cons protected init(java.nio.channels.spi.SelectorProvider)
intf java.nio.channels.ByteChannel
intf java.nio.channels.GatheringByteChannel
intf java.nio.channels.NetworkChannel
intf java.nio.channels.ScatteringByteChannel
meth public abstract <%0 extends java.lang.Object> java.nio.channels.SocketChannel setOption(java.net.SocketOption<{%%0}>,{%%0}) throws java.io.IOException
meth public abstract boolean connect(java.net.SocketAddress) throws java.io.IOException
meth public abstract boolean finishConnect() throws java.io.IOException
meth public abstract boolean isConnected()
meth public abstract boolean isConnectionPending()
meth public abstract int read(java.nio.ByteBuffer) throws java.io.IOException
meth public abstract int write(java.nio.ByteBuffer) throws java.io.IOException
meth public abstract java.net.Socket socket()
meth public abstract java.net.SocketAddress getLocalAddress() throws java.io.IOException
meth public abstract java.net.SocketAddress getRemoteAddress() throws java.io.IOException
meth public abstract java.nio.channels.SocketChannel bind(java.net.SocketAddress) throws java.io.IOException
meth public abstract java.nio.channels.SocketChannel shutdownInput() throws java.io.IOException
meth public abstract java.nio.channels.SocketChannel shutdownOutput() throws java.io.IOException
meth public abstract long read(java.nio.ByteBuffer[],int,int) throws java.io.IOException
meth public abstract long write(java.nio.ByteBuffer[],int,int) throws java.io.IOException
meth public final int validOps()
meth public final long read(java.nio.ByteBuffer[]) throws java.io.IOException
meth public final long write(java.nio.ByteBuffer[]) throws java.io.IOException
meth public static java.nio.channels.SocketChannel open() throws java.io.IOException
meth public static java.nio.channels.SocketChannel open(java.net.SocketAddress) throws java.io.IOException
supr java.nio.channels.spi.AbstractSelectableChannel

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

CLSS public abstract java.nio.channels.spi.AbstractSelectableChannel
cons protected init(java.nio.channels.spi.SelectorProvider)
meth protected abstract void implCloseSelectableChannel() throws java.io.IOException
meth protected abstract void implConfigureBlocking(boolean) throws java.io.IOException
meth protected final void implCloseChannel() throws java.io.IOException
meth public final boolean isBlocking()
meth public final boolean isRegistered()
meth public final java.lang.Object blockingLock()
meth public final java.nio.channels.SelectableChannel configureBlocking(boolean) throws java.io.IOException
meth public final java.nio.channels.SelectionKey keyFor(java.nio.channels.Selector)
meth public final java.nio.channels.SelectionKey register(java.nio.channels.Selector,int,java.lang.Object) throws java.nio.channels.ClosedChannelException
meth public final java.nio.channels.spi.SelectorProvider provider()
supr java.nio.channels.SelectableChannel

CLSS public abstract java.nio.channels.spi.SelectorProvider
cons protected init()
meth public abstract java.nio.channels.DatagramChannel openDatagramChannel() throws java.io.IOException
meth public abstract java.nio.channels.DatagramChannel openDatagramChannel(java.net.ProtocolFamily) throws java.io.IOException
meth public abstract java.nio.channels.Pipe openPipe() throws java.io.IOException
meth public abstract java.nio.channels.ServerSocketChannel openServerSocketChannel() throws java.io.IOException
meth public abstract java.nio.channels.SocketChannel openSocketChannel() throws java.io.IOException
meth public abstract java.nio.channels.spi.AbstractSelector openSelector() throws java.io.IOException
meth public java.nio.channels.Channel inheritedChannel() throws java.io.IOException
meth public static java.nio.channels.spi.SelectorProvider provider()
supr java.lang.Object

CLSS public abstract javax.net.SocketFactory
cons protected init()
meth public abstract java.net.Socket createSocket(java.lang.String,int) throws java.io.IOException
meth public abstract java.net.Socket createSocket(java.lang.String,int,java.net.InetAddress,int) throws java.io.IOException
meth public abstract java.net.Socket createSocket(java.net.InetAddress,int) throws java.io.IOException
meth public abstract java.net.Socket createSocket(java.net.InetAddress,int,java.net.InetAddress,int) throws java.io.IOException
meth public java.net.Socket createSocket() throws java.io.IOException
meth public static javax.net.SocketFactory getDefault()
supr java.lang.Object

CLSS public final org.newsclub.net.unix.AFAddressFamily
supr java.lang.Object

CLSS public abstract org.newsclub.net.unix.AFAddressFamilyConfig<%0 extends org.newsclub.net.unix.AFSocketAddress>
cons protected init()
meth protected abstract java.lang.Class<? extends org.newsclub.net.unix.AFDatagramChannel<{org.newsclub.net.unix.AFAddressFamilyConfig%0}>> datagramChannelClass()
meth protected abstract java.lang.Class<? extends org.newsclub.net.unix.AFDatagramSocket<{org.newsclub.net.unix.AFAddressFamilyConfig%0}>> datagramSocketClass()
meth protected abstract java.lang.Class<? extends org.newsclub.net.unix.AFServerSocket<{org.newsclub.net.unix.AFAddressFamilyConfig%0}>> serverSocketClass()
meth protected abstract java.lang.Class<? extends org.newsclub.net.unix.AFServerSocketChannel<{org.newsclub.net.unix.AFAddressFamilyConfig%0}>> serverSocketChannelClass()
meth protected abstract java.lang.Class<? extends org.newsclub.net.unix.AFSocket<{org.newsclub.net.unix.AFAddressFamilyConfig%0}>> socketClass()
meth protected abstract java.lang.Class<? extends org.newsclub.net.unix.AFSocketChannel<{org.newsclub.net.unix.AFAddressFamilyConfig%0}>> socketChannelClass()
meth protected abstract org.newsclub.net.unix.AFDatagramSocket$Constructor<{org.newsclub.net.unix.AFAddressFamilyConfig%0}> datagramSocketConstructor()
meth protected abstract org.newsclub.net.unix.AFServerSocket$Constructor<{org.newsclub.net.unix.AFAddressFamilyConfig%0}> serverSocketConstructor()
meth protected abstract org.newsclub.net.unix.AFSocket$Constructor<{org.newsclub.net.unix.AFAddressFamilyConfig%0}> socketConstructor()
supr java.lang.Object

CLSS public abstract org.newsclub.net.unix.AFDatagramChannel
intf org.newsclub.net.unix.AFSocketExtensions
intf org.newsclub.net.unix.AFSomeSocket
supr java.nio.channels.DatagramChannel
hfds afSocket

CLSS public abstract org.newsclub.net.unix.AFDatagramSocket
intf org.newsclub.net.unix.AFSocketExtensions
intf org.newsclub.net.unix.AFSomeSocket
supr java.net.DatagramSocket
hfds WILDCARD_ADDRESS,ancillaryDataSupport,channel,created,deleteOnClose,impl

CLSS public abstract interface static org.newsclub.net.unix.AFDatagramSocket$Constructor<%0 extends org.newsclub.net.unix.AFSocketAddress>
 outer org.newsclub.net.unix.AFDatagramSocket
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.newsclub.net.unix.AFDatagramSocket<{org.newsclub.net.unix.AFDatagramSocket$Constructor%0}> newSocket(java.io.FileDescriptor) throws java.io.IOException

CLSS public abstract org.newsclub.net.unix.AFDatagramSocketImpl
meth protected final void finalize()
supr java.net.DatagramSocketImpl

CLSS public abstract org.newsclub.net.unix.AFInputStream
intf org.newsclub.net.unix.FileDescriptorAccess
meth public long transferTo(java.io.OutputStream) throws java.io.IOException
supr java.io.InputStream
hfds DEFAULT_BUFFER_SIZE

CLSS public abstract org.newsclub.net.unix.AFOutputStream
intf org.newsclub.net.unix.FileDescriptorAccess
meth public long transferFrom(java.io.InputStream) throws java.io.IOException
supr java.io.OutputStream
hfds DEFAULT_BUFFER_SIZE

CLSS public final org.newsclub.net.unix.AFPipe
innr public final SinkChannel
innr public final SourceChannel
intf java.io.Closeable
meth public org.newsclub.net.unix.AFPipe$SinkChannel sink()
meth public org.newsclub.net.unix.AFPipe$SourceChannel source()
meth public static org.newsclub.net.unix.AFPipe open() throws java.io.IOException
meth public void close() throws java.io.IOException
supr java.nio.channels.Pipe
hfds options,sinkChannel,sinkCore,sourceChannel,sourceCore

CLSS public final org.newsclub.net.unix.AFPipe$SinkChannel
 outer org.newsclub.net.unix.AFPipe
intf org.newsclub.net.unix.FileDescriptorAccess
meth protected void implCloseSelectableChannel() throws java.io.IOException
meth protected void implConfigureBlocking(boolean) throws java.io.IOException
meth public int write(java.nio.ByteBuffer) throws java.io.IOException
meth public java.io.FileDescriptor getFileDescriptor() throws java.io.IOException
meth public long write(java.nio.ByteBuffer[]) throws java.io.IOException
meth public long write(java.nio.ByteBuffer[],int,int) throws java.io.IOException
supr java.nio.channels.Pipe$SinkChannel

CLSS public final org.newsclub.net.unix.AFPipe$SourceChannel
 outer org.newsclub.net.unix.AFPipe
intf org.newsclub.net.unix.FileDescriptorAccess
meth protected void implCloseSelectableChannel() throws java.io.IOException
meth protected void implConfigureBlocking(boolean) throws java.io.IOException
meth public int read(java.nio.ByteBuffer) throws java.io.IOException
meth public java.io.FileDescriptor getFileDescriptor() throws java.io.IOException
meth public long read(java.nio.ByteBuffer[]) throws java.io.IOException
meth public long read(java.nio.ByteBuffer[],int,int) throws java.io.IOException
supr java.nio.channels.Pipe$SourceChannel

CLSS public abstract org.newsclub.net.unix.AFSelectorProvider
supr java.nio.channels.spi.SelectorProvider

CLSS public abstract org.newsclub.net.unix.AFServerSocket
intf org.newsclub.net.unix.FileDescriptorAccess
supr java.net.ServerSocket

CLSS public abstract interface org.newsclub.net.unix.AFServerSocket$Constructor

CLSS public abstract org.newsclub.net.unix.AFServerSocketChannel
intf org.newsclub.net.unix.FileDescriptorAccess
supr java.nio.channels.ServerSocketChannel

CLSS public abstract org.newsclub.net.unix.AFSocket
intf org.newsclub.net.unix.AFSocketExtensions
intf org.newsclub.net.unix.AFSomeSocket
supr java.net.Socket

CLSS public abstract interface org.newsclub.net.unix.AFSocket$Constructor

CLSS public abstract org.newsclub.net.unix.AFSocketAddress
supr java.net.InetSocketAddress
hfds ADDRESS_CACHE,INTERNAL_DUMMY_BIND,INTERNAL_DUMMY_CONNECT,INTERNAL_DUMMY_DONT_CONNECT,SOCKADDR_MAX_LEN,SOCKADDR_NATIVE_DATA_OFFSET,SOCKADDR_NATIVE_FAMILY_OFFSET,SOCKETADDRESS_BUFFER_TL,addressFamily,bytes,inetAddress,nativeAddress,serialVersionUID

CLSS public abstract interface org.newsclub.net.unix.AFSocketAddress$AFSocketAddressConstructor

CLSS public abstract org.newsclub.net.unix.AFSocketAddressConfig<%0 extends org.newsclub.net.unix.AFSocketAddress>
cons protected init()
meth protected abstract java.lang.String selectorProviderClassname()
meth protected abstract java.util.Set<java.lang.String> uriSchemes()
meth protected abstract org.newsclub.net.unix.AFSocketAddress$AFSocketAddressConstructor<{org.newsclub.net.unix.AFSocketAddressConfig%0}> addressConstructor()
meth protected abstract {org.newsclub.net.unix.AFSocketAddressConfig%0} parseURI(java.net.URI,int) throws java.net.SocketException
supr java.lang.Object

CLSS public abstract interface org.newsclub.net.unix.AFSocketAddressFromHostname<%0 extends org.newsclub.net.unix.AFSocketAddress>
meth public abstract java.net.SocketAddress addressFromHost(java.lang.String,int) throws java.net.SocketException
meth public boolean isHostnameSupported(java.lang.String)

CLSS public final !enum org.newsclub.net.unix.AFSocketCapability
fld public final static org.newsclub.net.unix.AFSocketCapability CAPABILITY_ABSTRACT_NAMESPACE
fld public final static org.newsclub.net.unix.AFSocketCapability CAPABILITY_ANCILLARY_MESSAGES
fld public final static org.newsclub.net.unix.AFSocketCapability CAPABILITY_FD_AS_REDIRECT
fld public final static org.newsclub.net.unix.AFSocketCapability CAPABILITY_FILE_DESCRIPTORS
fld public final static org.newsclub.net.unix.AFSocketCapability CAPABILITY_NATIVE_SOCKETPAIR
fld public final static org.newsclub.net.unix.AFSocketCapability CAPABILITY_PEER_CREDENTIALS
fld public final static org.newsclub.net.unix.AFSocketCapability CAPABILITY_TIPC
fld public final static org.newsclub.net.unix.AFSocketCapability CAPABILITY_UNIX_DATAGRAMS
fld public final static org.newsclub.net.unix.AFSocketCapability CAPABILITY_UNIX_DOMAIN
meth public static org.newsclub.net.unix.AFSocketCapability valueOf(java.lang.String)
meth public static org.newsclub.net.unix.AFSocketCapability[] values()
supr java.lang.Enum<org.newsclub.net.unix.AFSocketCapability>
hfds bitmask

CLSS public abstract org.newsclub.net.unix.AFSocketChannel
intf org.newsclub.net.unix.AFSocketExtensions
intf org.newsclub.net.unix.AFSomeSocket
supr java.nio.channels.SocketChannel

CLSS protected abstract interface static org.newsclub.net.unix.AFSocketChannel$AFSocketSupplier<%0 extends org.newsclub.net.unix.AFSocketAddress>
 outer org.newsclub.net.unix.AFSocketChannel
 anno 0 java.lang.FunctionalInterface()
meth public abstract org.newsclub.net.unix.AFSocket<{org.newsclub.net.unix.AFSocketChannel$AFSocketSupplier%0}> newInstance() throws java.io.IOException

CLSS public abstract interface org.newsclub.net.unix.AFSocketExtensions
meth public abstract int getAncillaryReceiveBufferSize()
meth public abstract void ensureAncillaryReceiveBufferSize(int)
meth public abstract void setAncillaryReceiveBufferSize(int)

CLSS public abstract org.newsclub.net.unix.AFSocketFactory<%0 extends org.newsclub.net.unix.AFSocketAddress>
cons protected init()
innr public final static FixedAddressSocketFactory
intf org.newsclub.net.unix.AFSocketAddressFromHostname<{org.newsclub.net.unix.AFSocketFactory%0}>
meth protected abstract java.net.Socket connectTo({org.newsclub.net.unix.AFSocketFactory%0}) throws java.io.IOException
meth protected final boolean isInetAddressSupported(java.net.InetAddress)
meth public abstract java.net.Socket createSocket() throws java.net.SocketException
meth public final java.net.Socket createSocket(java.lang.String,int) throws java.io.IOException
meth public final java.net.Socket createSocket(java.lang.String,int,java.net.InetAddress,int) throws java.io.IOException
meth public final java.net.Socket createSocket(java.net.InetAddress,int) throws java.io.IOException
meth public final java.net.Socket createSocket(java.net.InetAddress,int,java.net.InetAddress,int) throws java.io.IOException
supr javax.net.SocketFactory

CLSS public final static org.newsclub.net.unix.AFSocketFactory$FixedAddressSocketFactory
 outer org.newsclub.net.unix.AFSocketFactory
cons public init(java.net.SocketAddress)
meth protected java.net.Socket connectTo(org.newsclub.net.unix.AFSocketAddress) throws java.io.IOException
meth public boolean isHostnameSupported(java.lang.String)
meth public java.net.Socket createSocket() throws java.net.SocketException
meth public java.net.SocketAddress addressFromHost(java.lang.String,int) throws java.net.SocketException
supr org.newsclub.net.unix.AFSocketFactory<org.newsclub.net.unix.AFSocketAddress>
hfds forceAddr

CLSS public abstract org.newsclub.net.unix.AFSocketImpl
meth protected <%0 extends java.lang.Object> void setOption(java.net.SocketOption<{%%0}>,{%%0}) throws java.io.IOException
meth protected <%0 extends java.lang.Object> {%%0} getOption(java.net.SocketOption<{%%0}>) throws java.io.IOException
meth protected final void finalize()
meth protected java.util.Set<java.net.SocketOption<?>> supportedOptions()
supr java.net.SocketImpl
hfds SHUTDOWN_RD_WR,SHUT_RD,SHUT_RD_WR,SHUT_WR,addressFamily,ancillaryDataSupport,bound,closedInputStream,closedOutputStream,connected,core,createType,implExtensions,in,out,reuseAddr,shutdownState,socketTimeout

CLSS public abstract interface org.newsclub.net.unix.AFSocketImplExtensions<%0 extends org.newsclub.net.unix.AFSocketAddress>

CLSS public final org.newsclub.net.unix.AFSocketOption<%0 extends java.lang.Object>
cons public init(java.lang.String,java.lang.Class<{org.newsclub.net.unix.AFSocketOption%0}>,int,int)
intf java.net.SocketOption<{org.newsclub.net.unix.AFSocketOption%0}>
meth public java.lang.Class<{org.newsclub.net.unix.AFSocketOption%0}> type()
meth public java.lang.String name()
meth public java.lang.String toString()
supr java.lang.Object
hfds level,name,optionName,type

CLSS public abstract org.newsclub.net.unix.AFSocketPair
supr org.newsclub.net.unix.CloseablePair

CLSS public final !enum org.newsclub.net.unix.AFSocketProtocol
fld public final static org.newsclub.net.unix.AFSocketProtocol DEFAULT
meth public static org.newsclub.net.unix.AFSocketProtocol valueOf(java.lang.String)
meth public static org.newsclub.net.unix.AFSocketProtocol[] values()
supr java.lang.Enum<org.newsclub.net.unix.AFSocketProtocol>
hfds id

CLSS public final !enum org.newsclub.net.unix.AFSocketType
fld public final static org.newsclub.net.unix.AFSocketType SOCK_DGRAM
fld public final static org.newsclub.net.unix.AFSocketType SOCK_RDM
fld public final static org.newsclub.net.unix.AFSocketType SOCK_SEQPACKET
fld public final static org.newsclub.net.unix.AFSocketType SOCK_STREAM
meth public static org.newsclub.net.unix.AFSocketType valueOf(java.lang.String)
meth public static org.newsclub.net.unix.AFSocketType[] values()
supr java.lang.Enum<org.newsclub.net.unix.AFSocketType>
hfds id

CLSS public abstract interface org.newsclub.net.unix.AFSomeSocket
intf java.io.Closeable
intf org.newsclub.net.unix.FileDescriptorAccess

CLSS public final org.newsclub.net.unix.AFTIPCSocketAddress
fld public final static int TIPC_RESERVED_TYPES = 64
fld public final static int TIPC_TOP_SRV = 1
innr public final static AddressType
innr public final static Scope
meth public boolean hasFilename()
meth public int getTIPCDomain()
meth public int getTIPCInstance()
meth public int getTIPCLower()
meth public int getTIPCNodeHash()
meth public int getTIPCRef()
meth public int getTIPCType()
meth public int getTIPCUpper()
meth public java.io.File getFile() throws java.io.FileNotFoundException
meth public java.lang.String toString()
meth public java.net.URI toURI(java.lang.String,java.net.URI) throws java.io.IOException
meth public org.newsclub.net.unix.AFTIPCSocketAddress$Scope getScope()
meth public static boolean isSupportedAddress(java.net.InetAddress)
meth public static boolean isSupportedAddress(java.net.SocketAddress)
meth public static org.newsclub.net.unix.AFAddressFamily<org.newsclub.net.unix.AFTIPCSocketAddress> addressFamily()
meth public static org.newsclub.net.unix.AFTIPCSocketAddress of(java.net.URI) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFTIPCSocketAddress of(java.net.URI,int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFTIPCSocketAddress ofService(int,int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFTIPCSocketAddress ofService(int,org.newsclub.net.unix.AFTIPCSocketAddress$Scope,int,int,int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFTIPCSocketAddress ofService(org.newsclub.net.unix.AFTIPCSocketAddress$Scope,int,int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFTIPCSocketAddress ofService(org.newsclub.net.unix.AFTIPCSocketAddress$Scope,int,int,int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFTIPCSocketAddress ofServiceRange(int,int,int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFTIPCSocketAddress ofServiceRange(int,org.newsclub.net.unix.AFTIPCSocketAddress$Scope,int,int,int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFTIPCSocketAddress ofServiceRange(org.newsclub.net.unix.AFTIPCSocketAddress$Scope,int,int,int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFTIPCSocketAddress ofSocket(int,int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFTIPCSocketAddress ofSocket(int,int,int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFTIPCSocketAddress ofTopologyService() throws java.net.SocketException
meth public static org.newsclub.net.unix.AFTIPCSocketAddress unwrap(java.lang.String,int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFTIPCSocketAddress unwrap(java.net.InetAddress,int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFTIPCSocketAddress unwrap(java.net.SocketAddress) throws java.net.SocketException
supr org.newsclub.net.unix.AFSocketAddress
hfds PAT_TIPC_URI_HOST_AND_PORT,afTipc,serialVersionUID

CLSS public final org.newsclub.net.unix.AFTIPCSocketAddress$AddressType
supr org.newsclub.net.unix.NamedInteger

CLSS public final org.newsclub.net.unix.AFTIPCSocketAddress$Scope
supr org.newsclub.net.unix.NamedInteger

CLSS public final org.newsclub.net.unix.AFTIPCSocketImplExtensions
intf org.newsclub.net.unix.AFSocketImplExtensions<org.newsclub.net.unix.AFTIPCSocketAddress>
meth public byte[] getTIPCNodeId(int) throws java.io.IOException
meth public int[] getTIPCDestName()
meth public int[] getTIPCErrInfo()
meth public java.lang.String getTIPCLinkName(int,int) throws java.io.IOException
supr java.lang.Object
hfds ancillaryDataSupport

CLSS public final org.newsclub.net.unix.AFUNIXDatagramChannel
intf org.newsclub.net.unix.AFUNIXSocketExtensions
meth public !varargs void setOutboundFileDescriptors(java.io.FileDescriptor[]) throws java.io.IOException
meth public boolean hasOutboundFileDescriptors()
meth public java.io.FileDescriptor[] getReceivedFileDescriptors() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketCredentials getPeerCredentials() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXDatagramChannel open() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXDatagramChannel open(java.net.ProtocolFamily) throws java.io.IOException
meth public void clearReceivedFileDescriptors()
supr org.newsclub.net.unix.AFDatagramChannel<org.newsclub.net.unix.AFUNIXSocketAddress>

CLSS public final org.newsclub.net.unix.AFUNIXDatagramSocket
intf org.newsclub.net.unix.AFUNIXSocketExtensions
meth protected org.newsclub.net.unix.AFUNIXDatagramChannel newChannel()
meth public !varargs void setOutboundFileDescriptors(java.io.FileDescriptor[]) throws java.io.IOException
meth public boolean hasOutboundFileDescriptors()
meth public java.io.FileDescriptor[] getReceivedFileDescriptors() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXDatagramChannel getChannel()
meth public org.newsclub.net.unix.AFUNIXSocketCredentials getPeerCredentials() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXDatagramSocket newInstance() throws java.io.IOException
meth public void clearReceivedFileDescriptors()
supr org.newsclub.net.unix.AFDatagramSocket<org.newsclub.net.unix.AFUNIXSocketAddress>

CLSS public final !enum org.newsclub.net.unix.AFUNIXProtocolFamily
fld public final static org.newsclub.net.unix.AFUNIXProtocolFamily UNIX
intf java.net.ProtocolFamily
meth public static org.newsclub.net.unix.AFUNIXProtocolFamily valueOf(java.lang.String)
meth public static org.newsclub.net.unix.AFUNIXProtocolFamily[] values()
supr java.lang.Enum<org.newsclub.net.unix.AFUNIXProtocolFamily>

CLSS public final org.newsclub.net.unix.AFUNIXSelectorProvider
supr org.newsclub.net.unix.AFSelectorProvider

CLSS public org.newsclub.net.unix.AFUNIXServerSocket
cons protected init() throws java.io.IOException
meth protected org.newsclub.net.unix.AFSocketImpl newImpl(java.io.FileDescriptor) throws java.net.SocketException
meth protected org.newsclub.net.unix.AFUNIXServerSocketChannel newChannel()
meth protected org.newsclub.net.unix.AFUNIXSocket newSocketInstance() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXServerSocketChannel getChannel()
meth public org.newsclub.net.unix.AFUNIXSocket accept() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXServerSocket bindOn(java.io.File,boolean) throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXServerSocket bindOn(java.nio.file.Path,boolean) throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXServerSocket bindOn(org.newsclub.net.unix.AFUNIXSocketAddress) throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXServerSocket bindOn(org.newsclub.net.unix.AFUNIXSocketAddress,boolean) throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXServerSocket forceBindOn(org.newsclub.net.unix.AFUNIXSocketAddress) throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXServerSocket newInstance() throws java.io.IOException
supr org.newsclub.net.unix.AFServerSocket<org.newsclub.net.unix.AFUNIXSocketAddress>

CLSS public final org.newsclub.net.unix.AFUNIXServerSocketChannel
meth public org.newsclub.net.unix.AFUNIXSocketChannel accept() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXServerSocketChannel open() throws java.io.IOException
supr org.newsclub.net.unix.AFServerSocketChannel<org.newsclub.net.unix.AFUNIXSocketAddress>

CLSS public final org.newsclub.net.unix.AFUNIXSocket
intf org.newsclub.net.unix.AFUNIXSocketExtensions
meth protected org.newsclub.net.unix.AFUNIXSocketChannel newChannel()
meth public !varargs void setOutboundFileDescriptors(java.io.FileDescriptor[]) throws java.io.IOException
meth public boolean hasOutboundFileDescriptors()
meth public java.io.FileDescriptor[] getReceivedFileDescriptors() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketChannel getChannel()
meth public org.newsclub.net.unix.AFUNIXSocketCredentials getPeerCredentials() throws java.io.IOException
meth public static boolean isSupported()
meth public static org.newsclub.net.unix.AFUNIXSocket connectTo(org.newsclub.net.unix.AFUNIXSocketAddress) throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXSocket newInstance() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXSocket newStrictInstance() throws java.io.IOException
meth public static void main(java.lang.String[])
meth public void clearReceivedFileDescriptors()
supr org.newsclub.net.unix.AFSocket<org.newsclub.net.unix.AFUNIXSocketAddress>
hfds CONSTRUCTOR_STRICT

CLSS public final org.newsclub.net.unix.AFUNIXSocketAddress
supr org.newsclub.net.unix.AFSocketAddress

CLSS public final !enum org.newsclub.net.unix.AFUNIXSocketCapability
 anno 0 java.lang.Deprecated()
fld public final static org.newsclub.net.unix.AFUNIXSocketCapability CAPABILITY_ABSTRACT_NAMESPACE
fld public final static org.newsclub.net.unix.AFUNIXSocketCapability CAPABILITY_ANCILLARY_MESSAGES
fld public final static org.newsclub.net.unix.AFUNIXSocketCapability CAPABILITY_DATAGRAMS
fld public final static org.newsclub.net.unix.AFUNIXSocketCapability CAPABILITY_FILE_DESCRIPTORS
fld public final static org.newsclub.net.unix.AFUNIXSocketCapability CAPABILITY_NATIVE_SOCKETPAIR
fld public final static org.newsclub.net.unix.AFUNIXSocketCapability CAPABILITY_PEER_CREDENTIALS
meth public static org.newsclub.net.unix.AFUNIXSocketCapability valueOf(java.lang.String)
meth public static org.newsclub.net.unix.AFUNIXSocketCapability[] values()
supr java.lang.Enum<org.newsclub.net.unix.AFUNIXSocketCapability>
hfds bitmask

CLSS public final org.newsclub.net.unix.AFUNIXSocketChannel
intf org.newsclub.net.unix.AFUNIXSocketExtensions
meth public !varargs void setOutboundFileDescriptors(java.io.FileDescriptor[]) throws java.io.IOException
meth public boolean hasOutboundFileDescriptors()
meth public java.io.FileDescriptor[] getReceivedFileDescriptors() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketCredentials getPeerCredentials() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXSocketChannel open() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXSocketChannel open(java.net.SocketAddress) throws java.io.IOException
meth public void clearReceivedFileDescriptors()
supr org.newsclub.net.unix.AFSocketChannel<org.newsclub.net.unix.AFUNIXSocketAddress>

CLSS public final org.newsclub.net.unix.AFUNIXSocketCredentials
fld public final static org.newsclub.net.unix.AFUNIXSocketCredentials SAME_PROCESS
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.UUID getUUID()
meth public long getGid()
meth public long getPid()
meth public long getUid()
meth public long[] getGids()
meth public static org.newsclub.net.unix.AFUNIXSocketCredentials remotePeerCredentials()
supr java.lang.Object
hfds gids,pid,serialVersionUID,uid,uuid

CLSS public abstract interface org.newsclub.net.unix.AFUNIXSocketExtensions
intf org.newsclub.net.unix.AFSocketExtensions
meth public abstract !varargs void setOutboundFileDescriptors(java.io.FileDescriptor[]) throws java.io.IOException
meth public abstract boolean hasOutboundFileDescriptors()
meth public abstract java.io.FileDescriptor[] getReceivedFileDescriptors() throws java.io.IOException
meth public abstract org.newsclub.net.unix.AFUNIXSocketCredentials getPeerCredentials() throws java.io.IOException
meth public abstract void clearReceivedFileDescriptors()

CLSS public abstract org.newsclub.net.unix.AFUNIXSocketFactory
cons protected init()
innr public final static FactoryArg
innr public final static SystemProperty
innr public final static URIScheme
meth protected org.newsclub.net.unix.AFUNIXSocket connectTo(org.newsclub.net.unix.AFUNIXSocketAddress) throws java.io.IOException
meth public java.net.Socket createSocket() throws java.net.SocketException
supr org.newsclub.net.unix.AFSocketFactory<org.newsclub.net.unix.AFUNIXSocketAddress>
hcls DefaultSocketHostnameSocketFactory

CLSS public final static org.newsclub.net.unix.AFUNIXSocketFactory$FactoryArg
 outer org.newsclub.net.unix.AFUNIXSocketFactory
cons public init(java.io.File)
cons public init(java.lang.String)
meth public final boolean isHostnameSupported(java.lang.String)
meth public org.newsclub.net.unix.AFUNIXSocketAddress addressFromHost(java.lang.String,int) throws java.net.SocketException
supr org.newsclub.net.unix.AFUNIXSocketFactory
hfds socketFile

CLSS public final static org.newsclub.net.unix.AFUNIXSocketFactory$SystemProperty
 outer org.newsclub.net.unix.AFUNIXSocketFactory
cons public init()
meth public final boolean isHostnameSupported(java.lang.String)
meth public org.newsclub.net.unix.AFUNIXSocketAddress addressFromHost(java.lang.String,int) throws java.net.SocketException
supr org.newsclub.net.unix.AFUNIXSocketFactory
hfds PROP_SOCKET_DEFAULT

CLSS public final static org.newsclub.net.unix.AFUNIXSocketFactory$URIScheme
 outer org.newsclub.net.unix.AFUNIXSocketFactory
cons public init()
meth public boolean isHostnameSupported(java.lang.String)
meth public org.newsclub.net.unix.AFUNIXSocketAddress addressFromHost(java.lang.String,int) throws java.net.SocketException
supr org.newsclub.net.unix.AFUNIXSocketFactory
hfds FILE_SCHEME_LOCALHOST,FILE_SCHEME_PREFIX,FILE_SCHEME_PREFIX_ENCODED

CLSS public final org.newsclub.net.unix.AFUNIXSocketPair<%0 extends org.newsclub.net.unix.AFSomeSocket>
meth public static org.newsclub.net.unix.AFUNIXSocketPair<org.newsclub.net.unix.AFUNIXDatagramChannel> openDatagram() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXSocketPair<org.newsclub.net.unix.AFUNIXSocketChannel> open() throws java.io.IOException
supr org.newsclub.net.unix.AFSocketPair<{org.newsclub.net.unix.AFUNIXSocketPair%0}>

CLSS public org.newsclub.net.unix.CloseablePair
intf java.io.Closeable
supr java.lang.Object

CLSS public final org.newsclub.net.unix.Closeables
cons public !varargs init(java.io.Closeable[])
cons public init()
intf java.io.Closeable
meth public boolean add(java.io.Closeable)
meth public boolean add(java.lang.ref.WeakReference<java.io.Closeable>)
meth public boolean remove(java.io.Closeable)
meth public void close() throws java.io.IOException
meth public void close(java.io.IOException) throws java.io.IOException
supr java.lang.Object
hfds list
hcls HardReference

CLSS public abstract interface org.newsclub.net.unix.FileDescriptorAccess
meth public abstract java.io.FileDescriptor getFileDescriptor() throws java.io.IOException

CLSS public final org.newsclub.net.unix.FileDescriptorCast
intf org.newsclub.net.unix.FileDescriptorAccess
supr java.lang.Object
hfds FD_IS_PROVIDER,GLOBAL_PROVIDERS,GLOBAL_PROVIDERS_FINAL,PRIMARY_TYPE_PROVIDERS_MAP,cpm,fdObj,localPort,remotePort

CLSS public final org.newsclub.net.unix.HostAndPort
cons public init(java.lang.String,int)
meth public boolean equals(java.lang.Object)
meth public int getPort()
meth public int hashCode()
meth public java.lang.String getHostname()
meth public java.lang.String toString()
meth public java.net.URI toURI(java.lang.String)
meth public java.net.URI toURI(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
meth public java.net.URI toURI(java.lang.String,java.net.URI)
meth public static org.newsclub.net.unix.HostAndPort parseFrom(java.net.URI) throws java.net.SocketException
supr java.lang.Object
hfds PAT_HOST_AND_PORT,hostname,port

CLSS public org.newsclub.net.unix.NamedInteger
intf java.io.Serializable
supr java.lang.Object
hfds id,name,serialVersionUID

CLSS public abstract interface static org.newsclub.net.unix.NamedInteger$HasOfValue
 outer org.newsclub.net.unix.NamedInteger

CLSS protected abstract interface static org.newsclub.net.unix.NamedInteger$UndefinedValueConstructor<%0 extends org.newsclub.net.unix.NamedInteger>
 outer org.newsclub.net.unix.NamedInteger
 anno 0 java.lang.FunctionalInterface()
meth public abstract {org.newsclub.net.unix.NamedInteger$UndefinedValueConstructor%0} newInstance(int)

CLSS public abstract org.newsclub.net.unix.NamedIntegerBitmask
intf java.io.Serializable
supr java.lang.Object
hfds flags,name,serialVersionUID

CLSS public abstract interface org.newsclub.net.unix.NamedIntegerBitmask$Constructor

CLSS public abstract interface org.newsclub.net.unix.SocketAddressFilter
 anno 0 java.lang.FunctionalInterface()
meth public abstract java.net.SocketAddress apply(java.net.SocketAddress) throws java.io.IOException

