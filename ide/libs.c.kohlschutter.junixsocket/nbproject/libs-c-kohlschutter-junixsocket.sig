#Signature file v4.1
#Version 2.20

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
hfds name,ordinal

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
hfds FIELDS_OFFSET,UNSAFE,holder,serialPersistentFields,serialVersionUID
hcls InetSocketAddressHolder

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
hfds bound,closeLock,closed,created,factory,impl,oldImpl

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
hfds bound,closeLock,closed,connected,created,factory,impl,oldImpl,shutIn,shutOut

CLSS public abstract java.net.SocketAddress
cons public init()
intf java.io.Serializable
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract javax.net.SocketFactory
cons protected init()
meth public abstract java.net.Socket createSocket(java.lang.String,int) throws java.io.IOException
meth public abstract java.net.Socket createSocket(java.lang.String,int,java.net.InetAddress,int) throws java.io.IOException
meth public abstract java.net.Socket createSocket(java.net.InetAddress,int) throws java.io.IOException
meth public abstract java.net.Socket createSocket(java.net.InetAddress,int,java.net.InetAddress,int) throws java.io.IOException
meth public java.net.Socket createSocket() throws java.io.IOException
meth public static javax.net.SocketFactory getDefault()
supr java.lang.Object
hfds theFactory

CLSS public org.newsclub.net.unix.AFUNIXServerSocket
cons protected init() throws java.io.IOException
meth public boolean isBound()
meth public boolean isClosed()
meth public java.lang.String toString()
meth public org.newsclub.net.unix.AFUNIXSocket accept() throws java.io.IOException
meth public static boolean isSupported()
meth public static org.newsclub.net.unix.AFUNIXServerSocket bindOn(org.newsclub.net.unix.AFUNIXSocketAddress) throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXServerSocket newInstance() throws java.io.IOException
meth public void bind(java.net.SocketAddress,int) throws java.io.IOException
meth public void close() throws java.io.IOException
supr java.net.ServerSocket
hfds boundEndpoint,implementation

CLSS public final org.newsclub.net.unix.AFUNIXSocket
meth public !varargs void setOutboundFileDescriptors(java.io.FileDescriptor[]) throws java.io.IOException
meth public boolean isClosed()
meth public int getAncillaryReceiveBufferSize()
meth public java.io.FileDescriptor[] getReceivedFileDescriptors() throws java.io.IOException
meth public java.lang.String toString()
meth public org.newsclub.net.unix.AFUNIXSocketCredentials getPeerCredentials() throws java.io.IOException
meth public static boolean isSupported()
meth public static boolean supports(org.newsclub.net.unix.AFUNIXSocketCapability)
meth public static java.lang.String getLoadedLibrary()
meth public static org.newsclub.net.unix.AFUNIXSocket connectTo(org.newsclub.net.unix.AFUNIXSocketAddress) throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXSocket newInstance() throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXSocket newStrictInstance() throws java.io.IOException
meth public void bind(java.net.SocketAddress) throws java.io.IOException
meth public void clearReceivedFileDescriptors()
meth public void connect(java.net.SocketAddress) throws java.io.IOException
meth public void connect(java.net.SocketAddress,int) throws java.io.IOException
meth public void setAncillaryReceiveBufferSize(int)
supr java.net.Socket
hfds addr,capabilities,impl,loadedLibrary,socketFactory

CLSS public final org.newsclub.net.unix.AFUNIXSocketAddress
cons public init(byte[]) throws java.io.IOException
cons public init(byte[],int) throws java.io.IOException
cons public init(java.io.File) throws java.io.IOException
cons public init(java.io.File,int) throws java.io.IOException
meth public java.lang.String toString()
meth public static org.newsclub.net.unix.AFUNIXSocketAddress inAbstractNamespace(java.lang.String) throws java.io.IOException
meth public static org.newsclub.net.unix.AFUNIXSocketAddress inAbstractNamespace(java.lang.String,int) throws java.io.IOException
supr java.net.InetSocketAddress
hfds bytes,serialVersionUID

CLSS public final !enum org.newsclub.net.unix.AFUNIXSocketCapability
fld public final static org.newsclub.net.unix.AFUNIXSocketCapability CAPABILITY_ANCILLARY_MESSAGES
fld public final static org.newsclub.net.unix.AFUNIXSocketCapability CAPABILITY_FILE_DESCRIPTORS
fld public final static org.newsclub.net.unix.AFUNIXSocketCapability CAPABILITY_PEER_CREDENTIALS
meth public static org.newsclub.net.unix.AFUNIXSocketCapability valueOf(java.lang.String)
meth public static org.newsclub.net.unix.AFUNIXSocketCapability[] values()
supr java.lang.Enum<org.newsclub.net.unix.AFUNIXSocketCapability>
hfds bitmask

CLSS public final org.newsclub.net.unix.AFUNIXSocketCredentials
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public java.util.UUID getUUID()
meth public long getGid()
meth public long getPid()
meth public long getUid()
meth public long[] getGids()
supr java.lang.Object
hfds gids,pid,uid,uuid

CLSS public abstract org.newsclub.net.unix.AFUNIXSocketFactory
cons public init()
innr public final static FactoryArg
innr public final static SystemProperty
innr public final static URIScheme
meth protected abstract org.newsclub.net.unix.AFUNIXSocketAddress addressFromHost(java.lang.String,int) throws java.io.IOException
meth protected boolean isHostnameSupported(java.lang.String)
meth protected boolean isInetAddressSupported(java.net.InetAddress)
meth public java.net.Socket createSocket() throws java.io.IOException
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
meth protected org.newsclub.net.unix.AFUNIXSocketAddress addressFromHost(java.lang.String,int) throws java.io.IOException
supr org.newsclub.net.unix.AFUNIXSocketFactory
hfds socketFile

CLSS public final static org.newsclub.net.unix.AFUNIXSocketFactory$SystemProperty
 outer org.newsclub.net.unix.AFUNIXSocketFactory
cons public init()
meth protected final boolean isHostnameSupported(java.lang.String)
meth protected org.newsclub.net.unix.AFUNIXSocketAddress addressFromHost(java.lang.String,int) throws java.io.IOException
supr org.newsclub.net.unix.AFUNIXSocketFactory
hfds PROP_SOCKET_DEFAULT

CLSS public final static org.newsclub.net.unix.AFUNIXSocketFactory$URIScheme
 outer org.newsclub.net.unix.AFUNIXSocketFactory
cons public init()
meth protected boolean isHostnameSupported(java.lang.String)
meth protected org.newsclub.net.unix.AFUNIXSocketAddress addressFromHost(java.lang.String,int) throws java.io.IOException
supr org.newsclub.net.unix.AFUNIXSocketFactory
hfds FILE_SCHEME_LOCALHOST,FILE_SCHEME_PREFIX,FILE_SCHEME_PREFIX_ENCODED

