#Signature file v4.1
#Version 2.32

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

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

CLSS public final org.newsclub.net.unix.AFUNIXDatagramChannel
intf org.newsclub.net.unix.AFUNIXSocketExtensions
intf org.newsclub.net.unix.AFUNIXSomeSocket
meth protected void implCloseSelectableChannel() throws java.io.IOException
meth protected void implConfigureBlocking(boolean) throws java.io.IOException
meth public !varargs void setOutboundFileDescriptors(java.io.FileDescriptor[]) throws java.io.IOException
meth public <%0 extends java.lang.Object> org.newsclub.net.unix.AFUNIXDatagramChannel setOption(java.net.SocketOption<{%%0}>,{%%0}) throws java.io.IOException
meth public <%0 extends java.lang.Object> {%%0} getOption(java.net.SocketOption<{%%0}>) throws java.io.IOException
meth public boolean hasOutboundFileDescriptors()
meth public boolean isBound()
meth public boolean isConnected()
meth public boolean isDeleteOnClose()
meth public int getAncillaryReceiveBufferSize()
meth public int read(java.nio.ByteBuffer) throws java.io.IOException
meth public int send(java.nio.ByteBuffer,java.net.SocketAddress) throws java.io.IOException
meth public int write(java.nio.ByteBuffer) throws java.io.IOException
meth public java.io.FileDescriptor getFileDescriptor() throws java.io.IOException
meth public java.io.FileDescriptor[] getReceivedFileDescriptors() throws java.io.IOException
meth public java.nio.channels.MembershipKey join(java.net.InetAddress,java.net.NetworkInterface) throws java.io.IOException
meth public java.nio.channels.MembershipKey join(java.net.InetAddress,java.net.NetworkInterface,java.net.InetAddress) throws java.io.IOException
meth public java.util.Set<java.net.SocketOption<?>> supportedOptions()
meth public long read(java.nio.ByteBuffer[],int,int) throws java.io.IOException
meth public long write(java.nio.ByteBuffer[],int,int) throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXDatagramChannel bind(java.net.SocketAddress) throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXDatagramChannel connect(java.net.SocketAddress) throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXDatagramChannel disconnect() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXDatagramSocket socket()
meth public org.newsclub.net.unix.AFUNIXSocketAddress getLocalAddress() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketAddress getRemoteAddress() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketAddress receive(java.nio.ByteBuffer) throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketCredentials getPeerCredentials() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXDatagramChannel open() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXDatagramChannel open(java.net.ProtocolFamily) throws java.io.IOException
meth public void clearReceivedFileDescriptors()
meth public void ensureAncillaryReceiveBufferSize(int)
meth public void setAncillaryReceiveBufferSize(int)
meth public void setDeleteOnClose(boolean)
supr java.nio.channels.DatagramChannel
hfds afSocket

CLSS public final org.newsclub.net.unix.AFUNIXDatagramSocket
intf org.newsclub.net.unix.AFUNIXSocketExtensions
intf org.newsclub.net.unix.AFUNIXSomeSocket
meth public !varargs void setOutboundFileDescriptors(java.io.FileDescriptor[]) throws java.io.IOException
meth public boolean hasOutboundFileDescriptors()
meth public boolean isBound()
meth public boolean isClosed()
meth public boolean isConnected()
meth public boolean isDeleteOnClose()
meth public int getAncillaryReceiveBufferSize()
meth public java.io.FileDescriptor getFileDescriptor() throws java.io.IOException
meth public java.io.FileDescriptor[] getReceivedFileDescriptors() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXDatagramChannel getChannel()
meth public org.newsclub.net.unix.AFUNIXSocketAddress getLocalSocketAddress()
meth public org.newsclub.net.unix.AFUNIXSocketAddress getRemoteSocketAddress()
meth public org.newsclub.net.unix.AFUNIXSocketCredentials getPeerCredentials() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXDatagramSocket newInstance() throws java.io.IOException
meth public void bind(java.net.SocketAddress) throws java.net.SocketException
meth public void clearReceivedFileDescriptors()
meth public void close()
meth public void connect(java.net.InetAddress,int)
meth public void connect(java.net.SocketAddress) throws java.net.SocketException
meth public void ensureAncillaryReceiveBufferSize(int)
meth public void peek(java.net.DatagramPacket) throws java.io.IOException
meth public void send(java.net.DatagramPacket) throws java.io.IOException
meth public void setAncillaryReceiveBufferSize(int)
meth public void setDeleteOnClose(boolean)
supr java.net.DatagramSocket
hfds WILDCARD_ADDRESS,ancillaryDataSupport,channel,created,deleteOnClose,impl

CLSS public final org.newsclub.net.unix.AFUNIXPipe
innr public final SinkChannel
innr public final SourceChannel
intf java.io.Closeable
meth public org.newsclub.net.unix.AFUNIXPipe$SinkChannel sink()
meth public org.newsclub.net.unix.AFUNIXPipe$SourceChannel source()
meth public static org.newsclub.net.unix.AFUNIXPipe open() throws java.io.IOException
meth public void close() throws java.io.IOException
supr java.nio.channels.Pipe
hfds options,sinkChannel,sinkCore,sourceChannel,sourceCore

CLSS public final org.newsclub.net.unix.AFUNIXPipe$SinkChannel
 outer org.newsclub.net.unix.AFUNIXPipe
intf org.newsclub.net.unix.FileDescriptorAccess
meth protected void implCloseSelectableChannel() throws java.io.IOException
meth protected void implConfigureBlocking(boolean) throws java.io.IOException
meth public int write(java.nio.ByteBuffer) throws java.io.IOException
meth public java.io.FileDescriptor getFileDescriptor() throws java.io.IOException
meth public long write(java.nio.ByteBuffer[]) throws java.io.IOException
meth public long write(java.nio.ByteBuffer[],int,int) throws java.io.IOException
supr java.nio.channels.Pipe$SinkChannel

CLSS public final org.newsclub.net.unix.AFUNIXPipe$SourceChannel
 outer org.newsclub.net.unix.AFUNIXPipe
intf org.newsclub.net.unix.FileDescriptorAccess
meth protected void implCloseSelectableChannel() throws java.io.IOException
meth protected void implConfigureBlocking(boolean) throws java.io.IOException
meth public int read(java.nio.ByteBuffer) throws java.io.IOException
meth public java.io.FileDescriptor getFileDescriptor() throws java.io.IOException
meth public long read(java.nio.ByteBuffer[]) throws java.io.IOException
meth public long read(java.nio.ByteBuffer[],int,int) throws java.io.IOException
supr java.nio.channels.Pipe$SourceChannel

CLSS public final !enum org.newsclub.net.unix.AFUNIXProtocolFamily
fld public final static org.newsclub.net.unix.AFUNIXProtocolFamily UNIX
intf java.net.ProtocolFamily
meth public static org.newsclub.net.unix.AFUNIXProtocolFamily valueOf(java.lang.String)
meth public static org.newsclub.net.unix.AFUNIXProtocolFamily[] values()
supr java.lang.Enum<org.newsclub.net.unix.AFUNIXProtocolFamily>

CLSS public final org.newsclub.net.unix.AFUNIXSelectorProvider
meth public java.nio.channels.spi.AbstractSelector openSelector() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXDatagramChannel openDatagramChannel() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXDatagramChannel openDatagramChannel(java.net.ProtocolFamily) throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXPipe openPipe() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXPipe openSelectablePipe() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXServerSocketChannel openServerSocketChannel() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXServerSocketChannel openServerSocketChannel(java.net.SocketAddress) throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketChannel openSocketChannel() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketChannel openSocketChannel(java.net.SocketAddress) throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketPair<org.newsclub.net.unix.AFUNIXDatagramChannel> openDatagramChannelPair() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketPair<org.newsclub.net.unix.AFUNIXSocketChannel> openSocketChannelPair() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXSelectorProvider getInstance()
meth public static org.newsclub.net.unix.AFUNIXSelectorProvider provider()
supr java.nio.channels.spi.SelectorProvider
hfds INSTANCE

CLSS public org.newsclub.net.unix.AFUNIXServerSocket
cons protected init() throws java.io.IOException
intf org.newsclub.net.unix.FileDescriptorAccess
meth protected org.newsclub.net.unix.AFUNIXSocket newSocketInstance() throws java.io.IOException
meth public boolean isBound()
meth public boolean isClosed()
meth public boolean isDeleteOnClose()
meth public int getLocalPort()
meth public java.io.FileDescriptor getFileDescriptor() throws java.io.IOException
meth public java.lang.String toString()
meth public org.newsclub.net.unix.AFUNIXServerSocketChannel getChannel()
meth public org.newsclub.net.unix.AFUNIXSocket accept() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketAddress getLocalSocketAddress()
meth public static boolean isSupported()
meth public static org.newsclub.net.unix.AFUNIXServerSocket bindOn(java.io.File,boolean) throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXServerSocket bindOn(java.nio.file.Path,boolean) throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXServerSocket bindOn(org.newsclub.net.unix.AFUNIXSocketAddress) throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXServerSocket bindOn(org.newsclub.net.unix.AFUNIXSocketAddress,boolean) throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXServerSocket forceBindOn(org.newsclub.net.unix.AFUNIXSocketAddress) throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXServerSocket newInstance() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXServerSocket newInstance(java.io.FileDescriptor,int,int) throws java.io.IOException
meth public void addCloseable(java.io.Closeable)
meth public void bind(java.net.SocketAddress,int) throws java.io.IOException
meth public void close() throws java.io.IOException
meth public void removeCloseable(java.io.Closeable)
meth public void setDeleteOnClose(boolean)
supr java.net.ServerSocket
hfds boundEndpoint,channel,closeables,created,deleteOnClose,implementation

CLSS public final org.newsclub.net.unix.AFUNIXServerSocketChannel
intf org.newsclub.net.unix.FileDescriptorAccess
meth protected void implCloseSelectableChannel() throws java.io.IOException
meth protected void implConfigureBlocking(boolean) throws java.io.IOException
meth public <%0 extends java.lang.Object> org.newsclub.net.unix.AFUNIXServerSocketChannel setOption(java.net.SocketOption<{%%0}>,{%%0}) throws java.io.IOException
meth public <%0 extends java.lang.Object> {%%0} getOption(java.net.SocketOption<{%%0}>) throws java.io.IOException
meth public java.io.FileDescriptor getFileDescriptor() throws java.io.IOException
meth public java.util.Set<java.net.SocketOption<?>> supportedOptions()
meth public org.newsclub.net.unix.AFUNIXServerSocket socket()
meth public org.newsclub.net.unix.AFUNIXServerSocketChannel bind(java.net.SocketAddress,int) throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketAddress getLocalAddress() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketChannel accept() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXServerSocketChannel open() throws java.io.IOException
supr java.nio.channels.ServerSocketChannel
hfds afSocket

CLSS public final org.newsclub.net.unix.AFUNIXSocket
intf org.newsclub.net.unix.AFUNIXSocketExtensions
intf org.newsclub.net.unix.AFUNIXSomeSocket
meth public !varargs void setOutboundFileDescriptors(java.io.FileDescriptor[]) throws java.io.IOException
meth public boolean hasOutboundFileDescriptors()
meth public boolean isBound()
meth public boolean isClosed()
meth public boolean isConnected()
meth public int getAncillaryReceiveBufferSize()
meth public java.io.FileDescriptor getFileDescriptor() throws java.io.IOException
meth public java.io.FileDescriptor[] getReceivedFileDescriptors() throws java.io.IOException
meth public java.lang.String toString()
meth public org.newsclub.net.unix.AFUNIXSocketAddress getLocalSocketAddress()
meth public org.newsclub.net.unix.AFUNIXSocketAddress getRemoteSocketAddress()
meth public org.newsclub.net.unix.AFUNIXSocketChannel getChannel()
meth public org.newsclub.net.unix.AFUNIXSocketCredentials getPeerCredentials() throws java.io.IOException
meth public static boolean isSupported()
meth public static boolean supports(org.newsclub.net.unix.AFUNIXSocketCapability)
meth public static java.lang.String getLoadedLibrary()
meth public static java.lang.String getVersion()
meth public static org.newsclub.net.unix.AFUNIXSocket connectTo(org.newsclub.net.unix.AFUNIXSocketAddress) throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXSocket newInstance() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXSocket newStrictInstance() throws java.io.IOException
meth public static void main(java.lang.String[])
meth public void addCloseable(java.io.Closeable)
meth public void bind(java.net.SocketAddress) throws java.io.IOException
meth public void clearReceivedFileDescriptors()
meth public void close() throws java.io.IOException
meth public void connect(java.net.SocketAddress) throws java.io.IOException
meth public void connect(java.net.SocketAddress,int) throws java.io.IOException
meth public void ensureAncillaryReceiveBufferSize(int)
meth public void removeCloseable(java.io.Closeable)
meth public void setAncillaryReceiveBufferSize(int)
supr java.net.Socket
hfds capabilities,channel,closeables,created,impl,loadedLibrary,socketFactory

CLSS public final org.newsclub.net.unix.AFUNIXSocketAddress
cons public init(byte[]) throws java.net.SocketException
cons public init(byte[],int) throws java.net.SocketException
cons public init(java.io.File) throws java.net.SocketException
cons public init(java.io.File,int) throws java.net.SocketException
meth public boolean hasFilename()
meth public boolean isInAbstractNamespace()
meth public byte[] getPathAsBytes()
meth public java.io.File getFile() throws java.io.FileNotFoundException
meth public java.lang.String getPath()
meth public java.lang.String toString()
meth public java.net.InetAddress wrapAddress()
meth public static boolean isSupportedAddress(java.net.InetAddress)
meth public static boolean isSupportedAddress(java.net.SocketAddress)
meth public static java.nio.charset.Charset addressCharset()
meth public static org.newsclub.net.unix.AFUNIXSocketAddress inAbstractNamespace(java.lang.String) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFUNIXSocketAddress inAbstractNamespace(java.lang.String,int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFUNIXSocketAddress of(byte[]) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFUNIXSocketAddress of(byte[],int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFUNIXSocketAddress of(java.io.File) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFUNIXSocketAddress of(java.io.File,int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFUNIXSocketAddress of(java.nio.file.Path) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFUNIXSocketAddress of(java.nio.file.Path,int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFUNIXSocketAddress ofNewTempFile() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXSocketAddress ofNewTempPath(int) throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXSocketAddress unwrap(java.net.InetAddress,int) throws java.net.SocketException
meth public static org.newsclub.net.unix.AFUNIXSocketAddress unwrap(java.net.SocketAddress) throws java.net.SocketException
supr java.net.InetSocketAddress
hfds ADDRESS_CACHE,ADDRESS_CHARSET,INTERNAL_DUMMY_BIND,INTERNAL_DUMMY_CONNECT,INTERNAL_DUMMY_DONT_CONNECT,SOCKADDR_UN_LENGTH,SOCKETADDRESS_BUFFER_TL,bytes,inetAddress,serialVersionUID

CLSS public final !enum org.newsclub.net.unix.AFUNIXSocketCapability
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
intf org.newsclub.net.unix.AFUNIXSomeSocket
meth protected void implCloseSelectableChannel() throws java.io.IOException
meth protected void implConfigureBlocking(boolean) throws java.io.IOException
meth public !varargs void setOutboundFileDescriptors(java.io.FileDescriptor[]) throws java.io.IOException
meth public <%0 extends java.lang.Object> org.newsclub.net.unix.AFUNIXSocketChannel setOption(java.net.SocketOption<{%%0}>,{%%0}) throws java.io.IOException
meth public <%0 extends java.lang.Object> {%%0} getOption(java.net.SocketOption<{%%0}>) throws java.io.IOException
meth public boolean connect(java.net.SocketAddress) throws java.io.IOException
meth public boolean finishConnect() throws java.io.IOException
meth public boolean hasOutboundFileDescriptors()
meth public boolean isConnected()
meth public boolean isConnectionPending()
meth public int getAncillaryReceiveBufferSize()
meth public int read(java.nio.ByteBuffer) throws java.io.IOException
meth public int write(java.nio.ByteBuffer) throws java.io.IOException
meth public java.io.FileDescriptor getFileDescriptor() throws java.io.IOException
meth public java.io.FileDescriptor[] getReceivedFileDescriptors() throws java.io.IOException
meth public java.lang.String toString()
meth public java.util.Set<java.net.SocketOption<?>> supportedOptions()
meth public long read(java.nio.ByteBuffer[],int,int) throws java.io.IOException
meth public long write(java.nio.ByteBuffer[],int,int) throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocket socket()
meth public org.newsclub.net.unix.AFUNIXSocketAddress getLocalAddress() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketAddress getRemoteAddress() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketChannel bind(java.net.SocketAddress) throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketChannel shutdownInput() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketChannel shutdownOutput() throws java.io.IOException
meth public org.newsclub.net.unix.AFUNIXSocketCredentials getPeerCredentials() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXSocketChannel open() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXSocketChannel open(java.net.SocketAddress) throws java.io.IOException
meth public void clearReceivedFileDescriptors()
meth public void ensureAncillaryReceiveBufferSize(int)
meth public void setAncillaryReceiveBufferSize(int)
supr java.nio.channels.SocketChannel
hfds afSocket,connectPending

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
meth public abstract !varargs void setOutboundFileDescriptors(java.io.FileDescriptor[]) throws java.io.IOException
meth public abstract boolean hasOutboundFileDescriptors()
meth public abstract int getAncillaryReceiveBufferSize()
meth public abstract java.io.FileDescriptor[] getReceivedFileDescriptors() throws java.io.IOException
meth public abstract org.newsclub.net.unix.AFUNIXSocketCredentials getPeerCredentials() throws java.io.IOException
meth public abstract void clearReceivedFileDescriptors()
meth public abstract void ensureAncillaryReceiveBufferSize(int)
meth public abstract void setAncillaryReceiveBufferSize(int)

CLSS public abstract org.newsclub.net.unix.AFUNIXSocketFactory
cons public init()
innr public final static FactoryArg
innr public final static SystemProperty
innr public final static URIScheme
meth protected abstract org.newsclub.net.unix.AFUNIXSocketAddress addressFromHost(java.lang.String,int) throws java.net.SocketException
meth protected boolean isHostnameSupported(java.lang.String)
meth protected boolean isInetAddressSupported(java.net.InetAddress)
meth public java.net.Socket createSocket() throws java.net.SocketException
meth public java.net.Socket createSocket(java.lang.String,int) throws java.io.IOException
meth public java.net.Socket createSocket(java.lang.String,int,java.net.InetAddress,int) throws java.io.IOException
meth public java.net.Socket createSocket(java.net.InetAddress,int) throws java.io.IOException
meth public java.net.Socket createSocket(java.net.InetAddress,int,java.net.InetAddress,int) throws java.io.IOException
supr javax.net.SocketFactory
hcls DefaultSocketHostnameSocketFactory

CLSS public final static org.newsclub.net.unix.AFUNIXSocketFactory$FactoryArg
 outer org.newsclub.net.unix.AFUNIXSocketFactory
cons public init(java.io.File)
cons public init(java.lang.String)
meth protected final boolean isHostnameSupported(java.lang.String)
meth protected org.newsclub.net.unix.AFUNIXSocketAddress addressFromHost(java.lang.String,int) throws java.net.SocketException
supr org.newsclub.net.unix.AFUNIXSocketFactory
hfds socketFile

CLSS public final static org.newsclub.net.unix.AFUNIXSocketFactory$SystemProperty
 outer org.newsclub.net.unix.AFUNIXSocketFactory
cons public init()
meth protected final boolean isHostnameSupported(java.lang.String)
meth protected org.newsclub.net.unix.AFUNIXSocketAddress addressFromHost(java.lang.String,int) throws java.net.SocketException
supr org.newsclub.net.unix.AFUNIXSocketFactory
hfds PROP_SOCKET_DEFAULT

CLSS public final static org.newsclub.net.unix.AFUNIXSocketFactory$URIScheme
 outer org.newsclub.net.unix.AFUNIXSocketFactory
cons public init()
meth protected boolean isHostnameSupported(java.lang.String)
meth protected org.newsclub.net.unix.AFUNIXSocketAddress addressFromHost(java.lang.String,int) throws java.net.SocketException
supr org.newsclub.net.unix.AFUNIXSocketFactory
hfds FILE_SCHEME_LOCALHOST,FILE_SCHEME_PREFIX,FILE_SCHEME_PREFIX_ENCODED

CLSS public final org.newsclub.net.unix.AFUNIXSocketPair<%0 extends org.newsclub.net.unix.AFUNIXSomeSocket>
meth public static org.newsclub.net.unix.AFUNIXSocketPair<org.newsclub.net.unix.AFUNIXDatagramChannel> openDatagram() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXSocketPair<org.newsclub.net.unix.AFUNIXSocketChannel> open() throws java.io.IOException
meth public {org.newsclub.net.unix.AFUNIXSocketPair%0} getSocket1()
meth public {org.newsclub.net.unix.AFUNIXSocketPair%0} getSocket2()
supr java.lang.Object
hfds socket1,socket2

CLSS public abstract interface org.newsclub.net.unix.AFUNIXSomeSocket
intf java.io.Closeable
intf org.newsclub.net.unix.FileDescriptorAccess

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
meth public <%0 extends java.lang.Object> {%%0} as(java.lang.Class<{%%0}>) throws java.io.IOException
meth public boolean isAvailable(java.lang.Class<?>) throws java.io.IOException
meth public java.io.FileDescriptor getFileDescriptor()
meth public java.util.Set<java.lang.Class<?>> availableTypes()
meth public org.newsclub.net.unix.FileDescriptorCast withLocalPort(int)
meth public org.newsclub.net.unix.FileDescriptorCast withRemotePort(int)
meth public static org.newsclub.net.unix.FileDescriptorCast using(java.io.FileDescriptor) throws java.io.IOException
supr java.lang.Object
hfds GLOBAL_PROVIDERS,GLOBAL_PROVIDERS_FINAL,PRIMARY_TYPE_PROVIDERS_MAP,cpm,fdObj,localPort,remotePort
hcls CastingProvider,CastingProviderMap

