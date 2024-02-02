#Signature file v4.1
#Version 2.45.0

CLSS public java.io.IOException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

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

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

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

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract javax.net.ServerSocketFactory
cons protected init()
meth public abstract java.net.ServerSocket createServerSocket(int) throws java.io.IOException
meth public abstract java.net.ServerSocket createServerSocket(int,int) throws java.io.IOException
meth public abstract java.net.ServerSocket createServerSocket(int,int,java.net.InetAddress) throws java.io.IOException
meth public java.net.ServerSocket createServerSocket() throws java.io.IOException
meth public static javax.net.ServerSocketFactory getDefault()
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

CLSS public abstract interface javax.net.ssl.TrustManager

CLSS public abstract interface javax.net.ssl.X509TrustManager
intf javax.net.ssl.TrustManager
meth public abstract java.security.cert.X509Certificate[] getAcceptedIssuers()
meth public abstract void checkClientTrusted(java.security.cert.X509Certificate[],java.lang.String) throws java.security.cert.CertificateException
meth public abstract void checkServerTrusted(java.security.cert.X509Certificate[],java.lang.String) throws java.security.cert.CertificateException

CLSS public abstract org.apache.commons.net.DatagramSocketClient
cons public init()
fld protected boolean _isOpen_
fld protected int _timeout_
fld protected java.net.DatagramSocket _socket_
fld protected org.apache.commons.net.DatagramSocketFactory _socketFactory_
intf java.lang.AutoCloseable
meth protected java.net.DatagramSocket checkOpen()
meth public boolean isOpen()
meth public int getDefaultTimeout()
meth public int getLocalPort()
meth public int getSoTimeout() throws java.net.SocketException
 anno 0 java.lang.Deprecated()
meth public java.lang.String getCharsetName()
 anno 0 java.lang.Deprecated()
meth public java.net.InetAddress getLocalAddress()
meth public java.nio.charset.Charset getCharset()
meth public java.time.Duration getSoTimeoutDuration() throws java.net.SocketException
meth public void close()
meth public void open() throws java.net.SocketException
meth public void open(int) throws java.net.SocketException
meth public void open(int,java.net.InetAddress) throws java.net.SocketException
meth public void setCharset(java.nio.charset.Charset)
meth public void setDatagramSocketFactory(org.apache.commons.net.DatagramSocketFactory)
meth public void setDefaultTimeout(int)
 anno 0 java.lang.Deprecated()
meth public void setDefaultTimeout(java.time.Duration)
meth public void setSoTimeout(int) throws java.net.SocketException
 anno 0 java.lang.Deprecated()
meth public void setSoTimeout(java.time.Duration) throws java.net.SocketException
supr java.lang.Object
hfds DEFAULT_SOCKET_FACTORY,charset

CLSS public abstract interface org.apache.commons.net.DatagramSocketFactory
meth public abstract java.net.DatagramSocket createDatagramSocket() throws java.net.SocketException
meth public abstract java.net.DatagramSocket createDatagramSocket(int) throws java.net.SocketException
meth public abstract java.net.DatagramSocket createDatagramSocket(int,java.net.InetAddress) throws java.net.SocketException

CLSS public org.apache.commons.net.DefaultDatagramSocketFactory
cons public init()
intf org.apache.commons.net.DatagramSocketFactory
meth public java.net.DatagramSocket createDatagramSocket() throws java.net.SocketException
meth public java.net.DatagramSocket createDatagramSocket(int) throws java.net.SocketException
meth public java.net.DatagramSocket createDatagramSocket(int,java.net.InetAddress) throws java.net.SocketException
supr java.lang.Object

CLSS public org.apache.commons.net.DefaultSocketFactory
cons public init()
cons public init(java.net.Proxy)
meth public java.net.ServerSocket createServerSocket(int) throws java.io.IOException
meth public java.net.ServerSocket createServerSocket(int,int) throws java.io.IOException
meth public java.net.ServerSocket createServerSocket(int,int,java.net.InetAddress) throws java.io.IOException
meth public java.net.Socket createSocket() throws java.io.IOException
meth public java.net.Socket createSocket(java.lang.String,int) throws java.io.IOException
meth public java.net.Socket createSocket(java.lang.String,int,java.net.InetAddress,int) throws java.io.IOException
meth public java.net.Socket createSocket(java.net.InetAddress,int) throws java.io.IOException
meth public java.net.Socket createSocket(java.net.InetAddress,int,java.net.InetAddress,int) throws java.io.IOException
supr javax.net.SocketFactory
hfds connProxy

CLSS public org.apache.commons.net.MalformedServerReplyException
cons public init()
cons public init(java.lang.String)
supr java.io.IOException
hfds serialVersionUID

CLSS public org.apache.commons.net.PrintCommandListener
cons public init(java.io.PrintStream)
cons public init(java.io.PrintStream,boolean)
cons public init(java.io.PrintStream,boolean,char)
cons public init(java.io.PrintStream,boolean,char,boolean)
cons public init(java.io.PrintWriter)
cons public init(java.io.PrintWriter,boolean)
cons public init(java.io.PrintWriter,boolean,char)
cons public init(java.io.PrintWriter,boolean,char,boolean)
intf org.apache.commons.net.ProtocolCommandListener
meth public void protocolCommandSent(org.apache.commons.net.ProtocolCommandEvent)
meth public void protocolReplyReceived(org.apache.commons.net.ProtocolCommandEvent)
supr java.lang.Object
hfds directionMarker,eolMarker,nologin,writer

CLSS public org.apache.commons.net.ProtocolCommandEvent
cons public init(java.lang.Object,int,java.lang.String)
cons public init(java.lang.Object,java.lang.String,java.lang.String)
meth public boolean isCommand()
meth public boolean isReply()
meth public int getReplyCode()
meth public java.lang.String getCommand()
meth public java.lang.String getMessage()
supr java.util.EventObject
hfds command,isCommand,message,replyCode,serialVersionUID

CLSS public abstract interface org.apache.commons.net.ProtocolCommandListener
intf java.util.EventListener
meth public abstract void protocolCommandSent(org.apache.commons.net.ProtocolCommandEvent)
meth public abstract void protocolReplyReceived(org.apache.commons.net.ProtocolCommandEvent)

CLSS public org.apache.commons.net.ProtocolCommandSupport
cons public init(java.lang.Object)
intf java.io.Serializable
meth public int getListenerCount()
meth public void addProtocolCommandListener(org.apache.commons.net.ProtocolCommandListener)
meth public void fireCommandSent(java.lang.String,java.lang.String)
meth public void fireReplyReceived(int,java.lang.String)
meth public void removeProtocolCommandListener(org.apache.commons.net.ProtocolCommandListener)
supr java.lang.Object
hfds listeners,serialVersionUID,source

CLSS public abstract org.apache.commons.net.SocketClient
cons public init()
fld protected int _defaultPort_
fld protected int _timeout_
fld protected int connectTimeout
fld protected java.io.InputStream _input_
fld protected java.io.OutputStream _output_
fld protected java.lang.String _hostname_
fld protected java.net.InetSocketAddress remoteInetSocketAddress
fld protected java.net.Socket _socket_
fld protected javax.net.ServerSocketFactory _serverSocketFactory_
fld protected javax.net.SocketFactory _socketFactory_
fld public final static java.lang.String NETASCII_EOL = "\r\n"
meth protected int getReceiveBufferSize()
meth protected int getSendBufferSize()
meth protected java.net.InetSocketAddress getRemoteInetSocketAddress()
meth protected org.apache.commons.net.ProtocolCommandSupport getCommandSupport()
meth protected void _connectAction_() throws java.io.IOException
meth protected void applySocketAttributes() throws java.net.SocketException
meth protected void createCommandSupport()
meth protected void fireCommandSent(java.lang.String,java.lang.String)
meth protected void fireReplyReceived(int,java.lang.String)
meth public boolean getKeepAlive() throws java.net.SocketException
meth public boolean getTcpNoDelay() throws java.net.SocketException
meth public boolean isAvailable()
meth public boolean isConnected()
meth public boolean verifyRemote(java.net.Socket)
meth public int getConnectTimeout()
meth public int getDefaultPort()
meth public int getDefaultTimeout()
meth public int getLocalPort()
meth public int getRemotePort()
meth public int getSoLinger() throws java.net.SocketException
meth public int getSoTimeout() throws java.net.SocketException
meth public java.lang.String getCharsetName()
 anno 0 java.lang.Deprecated()
meth public java.net.InetAddress getLocalAddress()
meth public java.net.InetAddress getRemoteAddress()
meth public java.net.Proxy getProxy()
meth public java.nio.charset.Charset getCharset()
meth public javax.net.ServerSocketFactory getServerSocketFactory()
meth public void addProtocolCommandListener(org.apache.commons.net.ProtocolCommandListener)
meth public void connect(java.lang.String) throws java.io.IOException
meth public void connect(java.lang.String,int) throws java.io.IOException
meth public void connect(java.lang.String,int,java.net.InetAddress,int) throws java.io.IOException
meth public void connect(java.net.InetAddress) throws java.io.IOException
meth public void connect(java.net.InetAddress,int) throws java.io.IOException
meth public void connect(java.net.InetAddress,int,java.net.InetAddress,int) throws java.io.IOException
meth public void disconnect() throws java.io.IOException
meth public void removeProtocolCommandListener(org.apache.commons.net.ProtocolCommandListener)
meth public void setCharset(java.nio.charset.Charset)
meth public void setConnectTimeout(int)
meth public void setDefaultPort(int)
meth public void setDefaultTimeout(int)
meth public void setKeepAlive(boolean) throws java.net.SocketException
meth public void setProxy(java.net.Proxy)
meth public void setReceiveBufferSize(int) throws java.net.SocketException
meth public void setSendBufferSize(int) throws java.net.SocketException
meth public void setServerSocketFactory(javax.net.ServerSocketFactory)
meth public void setSoLinger(boolean,int) throws java.net.SocketException
meth public void setSoTimeout(int) throws java.net.SocketException
meth public void setSocketFactory(javax.net.SocketFactory)
meth public void setTcpNoDelay(boolean) throws java.net.SocketException
supr java.lang.Object
hfds DEFAULT_CONNECT_TIMEOUT,DEFAULT_SERVER_SOCKET_FACTORY,DEFAULT_SOCKET_FACTORY,charset,commandSupport,connProxy,receiveBufferSize,sendBufferSize

CLSS public abstract interface org.apache.commons.net.ftp.Configurable
meth public abstract void configure(org.apache.commons.net.ftp.FTPClientConfig)

CLSS public org.apache.commons.net.ftp.FTP
cons public init()
fld protected boolean _newReplyString
fld protected boolean strictMultilineParsing
fld protected int _replyCode
fld protected java.io.BufferedReader _controlInput_
fld protected java.io.BufferedWriter _controlOutput_
fld protected java.lang.String _controlEncoding
fld protected java.lang.String _replyString
fld protected java.util.ArrayList<java.lang.String> _replyLines
fld protected org.apache.commons.net.ProtocolCommandSupport _commandSupport_
fld public final static int ASCII_FILE_TYPE = 0
fld public final static int BINARY_FILE_TYPE = 2
fld public final static int BLOCK_TRANSFER_MODE = 11
fld public final static int CARRIAGE_CONTROL_TEXT_FORMAT = 6
fld public final static int COMPRESSED_TRANSFER_MODE = 12
fld public final static int DEFAULT_DATA_PORT = 20
fld public final static int DEFAULT_PORT = 21
fld public final static int EBCDIC_FILE_TYPE = 1
fld public final static int FILE_STRUCTURE = 7
fld public final static int LOCAL_FILE_TYPE = 3
fld public final static int NON_PRINT_TEXT_FORMAT = 4
fld public final static int PAGE_STRUCTURE = 9
fld public final static int RECORD_STRUCTURE = 8
fld public final static int REPLY_CODE_LEN = 3
fld public final static int STREAM_TRANSFER_MODE = 10
fld public final static int TELNET_TEXT_FORMAT = 5
fld public final static java.lang.String DEFAULT_CONTROL_ENCODING = "ISO-8859-1"
meth protected org.apache.commons.net.ProtocolCommandSupport getCommandSupport()
meth protected void __getReplyNoReport() throws java.io.IOException
meth protected void __noop() throws java.io.IOException
meth protected void _connectAction_() throws java.io.IOException
meth protected void _connectAction_(java.io.Reader) throws java.io.IOException
meth public boolean isStrictMultilineParsing()
meth public boolean isStrictReplyParsing()
meth public int abor() throws java.io.IOException
meth public int acct(java.lang.String) throws java.io.IOException
meth public int allo(int) throws java.io.IOException
meth public int allo(int,int) throws java.io.IOException
meth public int allo(long) throws java.io.IOException
meth public int allo(long,int) throws java.io.IOException
meth public int appe(java.lang.String) throws java.io.IOException
meth public int cdup() throws java.io.IOException
meth public int cwd(java.lang.String) throws java.io.IOException
meth public int dele(java.lang.String) throws java.io.IOException
meth public int eprt(java.net.InetAddress,int) throws java.io.IOException
meth public int epsv() throws java.io.IOException
meth public int feat() throws java.io.IOException
meth public int getReply() throws java.io.IOException
meth public int getReplyCode()
meth public int help() throws java.io.IOException
meth public int help(java.lang.String) throws java.io.IOException
meth public int list() throws java.io.IOException
meth public int list(java.lang.String) throws java.io.IOException
meth public int mdtm(java.lang.String) throws java.io.IOException
meth public int mfmt(java.lang.String,java.lang.String) throws java.io.IOException
meth public int mkd(java.lang.String) throws java.io.IOException
meth public int mlsd() throws java.io.IOException
meth public int mlsd(java.lang.String) throws java.io.IOException
meth public int mlst() throws java.io.IOException
meth public int mlst(java.lang.String) throws java.io.IOException
meth public int mode(int) throws java.io.IOException
meth public int nlst() throws java.io.IOException
meth public int nlst(java.lang.String) throws java.io.IOException
meth public int noop() throws java.io.IOException
meth public int pass(java.lang.String) throws java.io.IOException
meth public int pasv() throws java.io.IOException
meth public int port(java.net.InetAddress,int) throws java.io.IOException
meth public int pwd() throws java.io.IOException
meth public int quit() throws java.io.IOException
meth public int rein() throws java.io.IOException
meth public int rest(java.lang.String) throws java.io.IOException
meth public int retr(java.lang.String) throws java.io.IOException
meth public int rmd(java.lang.String) throws java.io.IOException
meth public int rnfr(java.lang.String) throws java.io.IOException
meth public int rnto(java.lang.String) throws java.io.IOException
meth public int sendCommand(int) throws java.io.IOException
meth public int sendCommand(int,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public int sendCommand(java.lang.String) throws java.io.IOException
meth public int sendCommand(java.lang.String,java.lang.String) throws java.io.IOException
meth public int sendCommand(org.apache.commons.net.ftp.FTPCmd) throws java.io.IOException
meth public int sendCommand(org.apache.commons.net.ftp.FTPCmd,java.lang.String) throws java.io.IOException
meth public int site(java.lang.String) throws java.io.IOException
meth public int size(java.lang.String) throws java.io.IOException
meth public int smnt(java.lang.String) throws java.io.IOException
meth public int stat() throws java.io.IOException
meth public int stat(java.lang.String) throws java.io.IOException
meth public int stor(java.lang.String) throws java.io.IOException
meth public int stou() throws java.io.IOException
meth public int stou(java.lang.String) throws java.io.IOException
meth public int stru(int) throws java.io.IOException
meth public int syst() throws java.io.IOException
meth public int type(int) throws java.io.IOException
meth public int type(int,int) throws java.io.IOException
meth public int user(java.lang.String) throws java.io.IOException
meth public java.lang.String getControlEncoding()
meth public java.lang.String getReplyString()
meth public java.lang.String[] getReplyStrings()
meth public void disconnect() throws java.io.IOException
meth public void setControlEncoding(java.lang.String)
meth public void setStrictMultilineParsing(boolean)
meth public void setStrictReplyParsing(boolean)
supr org.apache.commons.net.SocketClient
hfds modes,strictReplyParsing

CLSS public org.apache.commons.net.ftp.FTPClient
cons public init()
fld public final static int ACTIVE_LOCAL_DATA_CONNECTION_MODE = 0
fld public final static int ACTIVE_REMOTE_DATA_CONNECTION_MODE = 1
fld public final static int PASSIVE_LOCAL_DATA_CONNECTION_MODE = 2
fld public final static int PASSIVE_REMOTE_DATA_CONNECTION_MODE = 3
fld public final static java.lang.String FTP_IP_ADDRESS_FROM_PASV_RESPONSE = "org.apache.commons.net.ftp.ipAddressFromPasvResponse"
fld public final static java.lang.String FTP_SYSTEM_TYPE = "org.apache.commons.net.ftp.systemType"
fld public final static java.lang.String FTP_SYSTEM_TYPE_DEFAULT = "org.apache.commons.net.ftp.systemType.default"
fld public final static java.lang.String SYSTEM_TYPE_PROPERTIES = "/systemType.properties"
innr public abstract interface static HostnameResolver
innr public static NatServerResolverImpl
intf org.apache.commons.net.ftp.Configurable
meth protected boolean _retrieveFile(java.lang.String,java.lang.String,java.io.OutputStream) throws java.io.IOException
meth protected boolean _storeFile(java.lang.String,java.lang.String,java.io.InputStream) throws java.io.IOException
meth protected boolean restart(long) throws java.io.IOException
meth protected java.io.InputStream _retrieveFileStream(java.lang.String,java.lang.String) throws java.io.IOException
meth protected java.io.OutputStream _storeFileStream(java.lang.String,java.lang.String) throws java.io.IOException
meth protected java.lang.String getListArguments(java.lang.String)
meth protected java.net.Socket _openDataConnection_(int,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth protected java.net.Socket _openDataConnection_(java.lang.String,java.lang.String) throws java.io.IOException
meth protected java.net.Socket _openDataConnection_(org.apache.commons.net.ftp.FTPCmd,java.lang.String) throws java.io.IOException
meth protected void _connectAction_() throws java.io.IOException
meth protected void _connectAction_(java.io.Reader) throws java.io.IOException
meth protected void _parseExtendedPassiveModeReply(java.lang.String) throws org.apache.commons.net.MalformedServerReplyException
meth protected void _parsePassiveModeReply(java.lang.String) throws org.apache.commons.net.MalformedServerReplyException
meth public boolean abort() throws java.io.IOException
meth public boolean allocate(int) throws java.io.IOException
meth public boolean allocate(int,int) throws java.io.IOException
meth public boolean allocate(long) throws java.io.IOException
meth public boolean allocate(long,int) throws java.io.IOException
meth public boolean appendFile(java.lang.String,java.io.InputStream) throws java.io.IOException
meth public boolean changeToParentDirectory() throws java.io.IOException
meth public boolean changeWorkingDirectory(java.lang.String) throws java.io.IOException
meth public boolean completePendingCommand() throws java.io.IOException
meth public boolean deleteFile(java.lang.String) throws java.io.IOException
meth public boolean doCommand(java.lang.String,java.lang.String) throws java.io.IOException
meth public boolean enterRemoteActiveMode(java.net.InetAddress,int) throws java.io.IOException
meth public boolean enterRemotePassiveMode() throws java.io.IOException
meth public boolean features() throws java.io.IOException
meth public boolean getAutodetectUTF8()
meth public boolean getListHiddenFiles()
meth public boolean hasFeature(java.lang.String) throws java.io.IOException
meth public boolean hasFeature(java.lang.String,java.lang.String) throws java.io.IOException
meth public boolean hasFeature(org.apache.commons.net.ftp.FTPCmd) throws java.io.IOException
meth public boolean isIpAddressFromPasvResponse()
meth public boolean isRemoteVerificationEnabled()
meth public boolean isUseEPSVwithIPv4()
meth public boolean login(java.lang.String,java.lang.String) throws java.io.IOException
meth public boolean login(java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException
meth public boolean logout() throws java.io.IOException
meth public boolean makeDirectory(java.lang.String) throws java.io.IOException
meth public boolean reinitialize() throws java.io.IOException
meth public boolean remoteAppend(java.lang.String) throws java.io.IOException
meth public boolean remoteRetrieve(java.lang.String) throws java.io.IOException
meth public boolean remoteStore(java.lang.String) throws java.io.IOException
meth public boolean remoteStoreUnique() throws java.io.IOException
meth public boolean remoteStoreUnique(java.lang.String) throws java.io.IOException
meth public boolean removeDirectory(java.lang.String) throws java.io.IOException
meth public boolean rename(java.lang.String,java.lang.String) throws java.io.IOException
meth public boolean retrieveFile(java.lang.String,java.io.OutputStream) throws java.io.IOException
meth public boolean sendNoOp() throws java.io.IOException
meth public boolean sendSiteCommand(java.lang.String) throws java.io.IOException
meth public boolean setFileStructure(int) throws java.io.IOException
meth public boolean setFileTransferMode(int) throws java.io.IOException
meth public boolean setFileType(int) throws java.io.IOException
meth public boolean setFileType(int,int) throws java.io.IOException
meth public boolean setModificationTime(java.lang.String,java.lang.String) throws java.io.IOException
meth public boolean storeFile(java.lang.String,java.io.InputStream) throws java.io.IOException
meth public boolean storeUniqueFile(java.io.InputStream) throws java.io.IOException
meth public boolean storeUniqueFile(java.lang.String,java.io.InputStream) throws java.io.IOException
meth public boolean structureMount(java.lang.String) throws java.io.IOException
meth public int getBufferSize()
meth public int getControlKeepAliveReplyTimeout()
 anno 0 java.lang.Deprecated()
meth public int getDataConnectionMode()
meth public int getPassivePort()
meth public int getReceiveDataSocketBufferSize()
meth public int getSendDataSocketBufferSize()
meth public int[] getCslDebug()
 anno 0 java.lang.Deprecated()
meth public java.io.InputStream retrieveFileStream(java.lang.String) throws java.io.IOException
meth public java.io.OutputStream appendFileStream(java.lang.String) throws java.io.IOException
meth public java.io.OutputStream storeFileStream(java.lang.String) throws java.io.IOException
meth public java.io.OutputStream storeUniqueFileStream() throws java.io.IOException
meth public java.io.OutputStream storeUniqueFileStream(java.lang.String) throws java.io.IOException
meth public java.lang.String featureValue(java.lang.String) throws java.io.IOException
meth public java.lang.String getModificationTime(java.lang.String) throws java.io.IOException
meth public java.lang.String getPassiveHost()
meth public java.lang.String getSize(java.lang.String) throws java.io.IOException
meth public java.lang.String getStatus() throws java.io.IOException
meth public java.lang.String getStatus(java.lang.String) throws java.io.IOException
meth public java.lang.String getSystemName() throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public java.lang.String getSystemType() throws java.io.IOException
meth public java.lang.String listHelp() throws java.io.IOException
meth public java.lang.String listHelp(java.lang.String) throws java.io.IOException
meth public java.lang.String printWorkingDirectory() throws java.io.IOException
meth public java.lang.String[] doCommandAsStrings(java.lang.String,java.lang.String) throws java.io.IOException
meth public java.lang.String[] featureValues(java.lang.String) throws java.io.IOException
meth public java.lang.String[] listNames() throws java.io.IOException
meth public java.lang.String[] listNames(java.lang.String) throws java.io.IOException
meth public java.net.InetAddress getPassiveLocalIPAddress()
meth public java.time.Duration getControlKeepAliveReplyTimeoutDuration()
meth public java.time.Duration getControlKeepAliveTimeoutDuration()
meth public java.time.Duration getDataTimeout()
meth public java.time.Instant mdtmInstant(java.lang.String) throws java.io.IOException
meth public java.util.Calendar mdtmCalendar(java.lang.String) throws java.io.IOException
meth public long getControlKeepAliveTimeout()
 anno 0 java.lang.Deprecated()
meth public long getRestartOffset()
meth public org.apache.commons.net.ftp.FTPFile mdtmFile(java.lang.String) throws java.io.IOException
meth public org.apache.commons.net.ftp.FTPFile mlistFile(java.lang.String) throws java.io.IOException
meth public org.apache.commons.net.ftp.FTPFile[] listDirectories() throws java.io.IOException
meth public org.apache.commons.net.ftp.FTPFile[] listDirectories(java.lang.String) throws java.io.IOException
meth public org.apache.commons.net.ftp.FTPFile[] listFiles() throws java.io.IOException
meth public org.apache.commons.net.ftp.FTPFile[] listFiles(java.lang.String) throws java.io.IOException
meth public org.apache.commons.net.ftp.FTPFile[] listFiles(java.lang.String,org.apache.commons.net.ftp.FTPFileFilter) throws java.io.IOException
meth public org.apache.commons.net.ftp.FTPFile[] mlistDir() throws java.io.IOException
meth public org.apache.commons.net.ftp.FTPFile[] mlistDir(java.lang.String) throws java.io.IOException
meth public org.apache.commons.net.ftp.FTPFile[] mlistDir(java.lang.String,org.apache.commons.net.ftp.FTPFileFilter) throws java.io.IOException
meth public org.apache.commons.net.ftp.FTPListParseEngine initiateListParsing() throws java.io.IOException
meth public org.apache.commons.net.ftp.FTPListParseEngine initiateListParsing(java.lang.String) throws java.io.IOException
meth public org.apache.commons.net.ftp.FTPListParseEngine initiateListParsing(java.lang.String,java.lang.String) throws java.io.IOException
meth public org.apache.commons.net.ftp.FTPListParseEngine initiateMListParsing() throws java.io.IOException
meth public org.apache.commons.net.ftp.FTPListParseEngine initiateMListParsing(java.lang.String) throws java.io.IOException
meth public org.apache.commons.net.io.CopyStreamListener getCopyStreamListener()
meth public void configure(org.apache.commons.net.ftp.FTPClientConfig)
meth public void disconnect() throws java.io.IOException
meth public void enterLocalActiveMode()
meth public void enterLocalPassiveMode()
meth public void setActiveExternalIPAddress(java.lang.String) throws java.net.UnknownHostException
meth public void setActivePortRange(int,int)
meth public void setAutodetectUTF8(boolean)
meth public void setBufferSize(int)
meth public void setControlKeepAliveReplyTimeout(int)
 anno 0 java.lang.Deprecated()
meth public void setControlKeepAliveReplyTimeout(java.time.Duration)
meth public void setControlKeepAliveTimeout(java.time.Duration)
meth public void setControlKeepAliveTimeout(long)
 anno 0 java.lang.Deprecated()
meth public void setCopyStreamListener(org.apache.commons.net.io.CopyStreamListener)
meth public void setDataTimeout(int)
 anno 0 java.lang.Deprecated()
meth public void setDataTimeout(java.time.Duration)
meth public void setIpAddressFromPasvResponse(boolean)
meth public void setListHiddenFiles(boolean)
meth public void setParserFactory(org.apache.commons.net.ftp.parser.FTPFileEntryParserFactory)
meth public void setPassiveLocalIPAddress(java.lang.String) throws java.net.UnknownHostException
meth public void setPassiveLocalIPAddress(java.net.InetAddress)
meth public void setPassiveNatWorkaround(boolean)
 anno 0 java.lang.Deprecated()
meth public void setPassiveNatWorkaroundStrategy(org.apache.commons.net.ftp.FTPClient$HostnameResolver)
meth public void setReceieveDataSocketBufferSize(int)
meth public void setRemoteVerificationEnabled(boolean)
meth public void setReportActiveExternalIPAddress(java.lang.String) throws java.net.UnknownHostException
meth public void setRestartOffset(long)
meth public void setSendDataSocketBufferSize(int)
meth public void setUseEPSVwithIPv4(boolean)
supr org.apache.commons.net.ftp.FTP
hfds PARMS_PAT,activeExternalHost,activeMaxPort,activeMinPort,autodetectEncoding,bufferSize,configuration,controlKeepAliveReplyTimeout,controlKeepAliveTimeout,copyStreamListener,cslDebug,dataConnectionMode,dataTimeout,entryParser,entryParserKey,featuresMap,fileFormat,fileStructure,fileTransferMode,fileType,ipAddressFromPasvResponse,listHiddenFiles,parserFactory,passiveHost,passiveLocalHost,passiveNatWorkaroundStrategy,passivePort,random,receiveDataSocketBufferSize,remoteVerificationEnabled,reportActiveExternalHost,restartOffset,sendDataSocketBufferSize,systemName,useEPSVwithIPv4
hcls CSL,PropertiesSingleton

CLSS public abstract interface static org.apache.commons.net.ftp.FTPClient$HostnameResolver
 outer org.apache.commons.net.ftp.FTPClient
meth public abstract java.lang.String resolve(java.lang.String) throws java.net.UnknownHostException

CLSS public static org.apache.commons.net.ftp.FTPClient$NatServerResolverImpl
 outer org.apache.commons.net.ftp.FTPClient
cons public init(org.apache.commons.net.ftp.FTPClient)
intf org.apache.commons.net.ftp.FTPClient$HostnameResolver
meth public java.lang.String resolve(java.lang.String) throws java.net.UnknownHostException
supr java.lang.Object
hfds client

CLSS public org.apache.commons.net.ftp.FTPClientConfig
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean,boolean)
cons public init(org.apache.commons.net.ftp.FTPClientConfig)
fld public final static java.lang.String SYST_AS400 = "AS/400"
fld public final static java.lang.String SYST_L8 = "TYPE: L8"
fld public final static java.lang.String SYST_MACOS_PETER = "MACOS PETER"
fld public final static java.lang.String SYST_MVS = "MVS"
fld public final static java.lang.String SYST_NETWARE = "NETWARE"
fld public final static java.lang.String SYST_NT = "WINDOWS"
fld public final static java.lang.String SYST_OS2 = "OS/2"
fld public final static java.lang.String SYST_OS400 = "OS/400"
fld public final static java.lang.String SYST_UNIX = "UNIX"
fld public final static java.lang.String SYST_UNIX_TRIM_LEADING = "UNIX_LTRIM"
fld public final static java.lang.String SYST_VMS = "VMS"
meth public boolean getUnparseableEntries()
meth public boolean isLenientFutureDates()
meth public java.lang.String getDefaultDateFormatStr()
meth public java.lang.String getRecentDateFormatStr()
meth public java.lang.String getServerLanguageCode()
meth public java.lang.String getServerSystemKey()
meth public java.lang.String getServerTimeZoneId()
meth public java.lang.String getShortMonthNames()
meth public static java.text.DateFormatSymbols getDateFormatSymbols(java.lang.String)
meth public static java.text.DateFormatSymbols lookupDateFormatSymbols(java.lang.String)
meth public static java.util.Collection<java.lang.String> getSupportedLanguageCodes()
meth public void setDefaultDateFormatStr(java.lang.String)
meth public void setLenientFutureDates(boolean)
meth public void setRecentDateFormatStr(java.lang.String)
meth public void setServerLanguageCode(java.lang.String)
meth public void setServerTimeZoneId(java.lang.String)
meth public void setShortMonthNames(java.lang.String)
meth public void setUnparseableEntries(boolean)
supr java.lang.Object
hfds LANGUAGE_CODE_MAP,defaultDateFormatStr,lenientFutureDates,recentDateFormatStr,saveUnparseableEntries,serverLanguageCode,serverSystemKey,serverTimeZoneId,shortMonthNames

CLSS public final !enum org.apache.commons.net.ftp.FTPCmd
fld public final static org.apache.commons.net.ftp.FTPCmd ABOR
fld public final static org.apache.commons.net.ftp.FTPCmd ABORT
fld public final static org.apache.commons.net.ftp.FTPCmd ACCOUNT
fld public final static org.apache.commons.net.ftp.FTPCmd ACCT
fld public final static org.apache.commons.net.ftp.FTPCmd ALLO
fld public final static org.apache.commons.net.ftp.FTPCmd ALLOCATE
fld public final static org.apache.commons.net.ftp.FTPCmd APPE
fld public final static org.apache.commons.net.ftp.FTPCmd APPEND
fld public final static org.apache.commons.net.ftp.FTPCmd CDUP
fld public final static org.apache.commons.net.ftp.FTPCmd CHANGE_TO_PARENT_DIRECTORY
fld public final static org.apache.commons.net.ftp.FTPCmd CHANGE_WORKING_DIRECTORY
fld public final static org.apache.commons.net.ftp.FTPCmd CWD
fld public final static org.apache.commons.net.ftp.FTPCmd DATA_PORT
fld public final static org.apache.commons.net.ftp.FTPCmd DELE
fld public final static org.apache.commons.net.ftp.FTPCmd DELETE
fld public final static org.apache.commons.net.ftp.FTPCmd EPRT
fld public final static org.apache.commons.net.ftp.FTPCmd EPSV
fld public final static org.apache.commons.net.ftp.FTPCmd FEAT
fld public final static org.apache.commons.net.ftp.FTPCmd FEATURES
fld public final static org.apache.commons.net.ftp.FTPCmd FILE_STRUCTURE
fld public final static org.apache.commons.net.ftp.FTPCmd GET_MOD_TIME
fld public final static org.apache.commons.net.ftp.FTPCmd HELP
fld public final static org.apache.commons.net.ftp.FTPCmd LIST
fld public final static org.apache.commons.net.ftp.FTPCmd LOGOUT
fld public final static org.apache.commons.net.ftp.FTPCmd MAKE_DIRECTORY
fld public final static org.apache.commons.net.ftp.FTPCmd MDTM
fld public final static org.apache.commons.net.ftp.FTPCmd MFMT
fld public final static org.apache.commons.net.ftp.FTPCmd MKD
fld public final static org.apache.commons.net.ftp.FTPCmd MLSD
fld public final static org.apache.commons.net.ftp.FTPCmd MLST
fld public final static org.apache.commons.net.ftp.FTPCmd MODE
fld public final static org.apache.commons.net.ftp.FTPCmd MOD_TIME
fld public final static org.apache.commons.net.ftp.FTPCmd NAME_LIST
fld public final static org.apache.commons.net.ftp.FTPCmd NLST
fld public final static org.apache.commons.net.ftp.FTPCmd NOOP
fld public final static org.apache.commons.net.ftp.FTPCmd PASS
fld public final static org.apache.commons.net.ftp.FTPCmd PASSIVE
fld public final static org.apache.commons.net.ftp.FTPCmd PASSWORD
fld public final static org.apache.commons.net.ftp.FTPCmd PASV
fld public final static org.apache.commons.net.ftp.FTPCmd PORT
fld public final static org.apache.commons.net.ftp.FTPCmd PRINT_WORKING_DIRECTORY
fld public final static org.apache.commons.net.ftp.FTPCmd PWD
fld public final static org.apache.commons.net.ftp.FTPCmd QUIT
fld public final static org.apache.commons.net.ftp.FTPCmd REIN
fld public final static org.apache.commons.net.ftp.FTPCmd REINITIALIZE
fld public final static org.apache.commons.net.ftp.FTPCmd REMOVE_DIRECTORY
fld public final static org.apache.commons.net.ftp.FTPCmd RENAME_FROM
fld public final static org.apache.commons.net.ftp.FTPCmd RENAME_TO
fld public final static org.apache.commons.net.ftp.FTPCmd REPRESENTATION_TYPE
fld public final static org.apache.commons.net.ftp.FTPCmd REST
fld public final static org.apache.commons.net.ftp.FTPCmd RESTART
fld public final static org.apache.commons.net.ftp.FTPCmd RETR
fld public final static org.apache.commons.net.ftp.FTPCmd RETRIEVE
fld public final static org.apache.commons.net.ftp.FTPCmd RMD
fld public final static org.apache.commons.net.ftp.FTPCmd RNFR
fld public final static org.apache.commons.net.ftp.FTPCmd RNTO
fld public final static org.apache.commons.net.ftp.FTPCmd SET_MOD_TIME
fld public final static org.apache.commons.net.ftp.FTPCmd SITE
fld public final static org.apache.commons.net.ftp.FTPCmd SITE_PARAMETERS
fld public final static org.apache.commons.net.ftp.FTPCmd SIZE
fld public final static org.apache.commons.net.ftp.FTPCmd SMNT
fld public final static org.apache.commons.net.ftp.FTPCmd STAT
fld public final static org.apache.commons.net.ftp.FTPCmd STATUS
fld public final static org.apache.commons.net.ftp.FTPCmd STOR
fld public final static org.apache.commons.net.ftp.FTPCmd STORE
fld public final static org.apache.commons.net.ftp.FTPCmd STORE_UNIQUE
fld public final static org.apache.commons.net.ftp.FTPCmd STOU
fld public final static org.apache.commons.net.ftp.FTPCmd STRU
fld public final static org.apache.commons.net.ftp.FTPCmd STRUCTURE_MOUNT
fld public final static org.apache.commons.net.ftp.FTPCmd SYST
fld public final static org.apache.commons.net.ftp.FTPCmd SYSTEM
fld public final static org.apache.commons.net.ftp.FTPCmd TRANSFER_MODE
fld public final static org.apache.commons.net.ftp.FTPCmd TYPE
fld public final static org.apache.commons.net.ftp.FTPCmd USER
fld public final static org.apache.commons.net.ftp.FTPCmd USERNAME
meth public final java.lang.String getCommand()
meth public static org.apache.commons.net.ftp.FTPCmd valueOf(java.lang.String)
meth public static org.apache.commons.net.ftp.FTPCmd[] values()
supr java.lang.Enum<org.apache.commons.net.ftp.FTPCmd>

CLSS public final org.apache.commons.net.ftp.FTPCommand
 anno 0 java.lang.Deprecated()
fld public final static int ABOR = 21
fld public final static int ABORT = 21
fld public final static int ACCOUNT = 2
fld public final static int ACCT = 2
fld public final static int ALLO = 17
fld public final static int ALLOCATE = 17
fld public final static int APPE = 16
fld public final static int APPEND = 16
fld public final static int CDUP = 4
fld public final static int CHANGE_TO_PARENT_DIRECTORY = 4
fld public final static int CHANGE_WORKING_DIRECTORY = 3
fld public final static int CWD = 3
fld public final static int DATA_PORT = 8
fld public final static int DELE = 22
fld public final static int DELETE = 22
fld public final static int EPRT = 37
fld public final static int EPSV = 36
fld public final static int FEAT = 34
fld public final static int FEATURES = 34
fld public final static int FILE_STRUCTURE = 11
fld public final static int GET_MOD_TIME = 33
fld public final static int HELP = 31
fld public final static int LIST = 26
fld public final static int LOGOUT = 7
fld public final static int MAKE_DIRECTORY = 24
fld public final static int MDTM = 33
fld public final static int MFMT = 35
fld public final static int MKD = 24
fld public final static int MLSD = 38
fld public final static int MLST = 39
fld public final static int MODE = 12
fld public final static int MOD_TIME = 33
fld public final static int NAME_LIST = 27
fld public final static int NLST = 27
fld public final static int NOOP = 32
fld public final static int PASS = 1
fld public final static int PASSIVE = 9
fld public final static int PASSWORD = 1
fld public final static int PASV = 9
fld public final static int PORT = 8
fld public final static int PRINT_WORKING_DIRECTORY = 25
fld public final static int PWD = 25
fld public final static int QUIT = 7
fld public final static int REIN = 6
fld public final static int REINITIALIZE = 6
fld public final static int REMOVE_DIRECTORY = 23
fld public final static int RENAME_FROM = 19
fld public final static int RENAME_TO = 20
fld public final static int REPRESENTATION_TYPE = 10
fld public final static int REST = 18
fld public final static int RESTART = 18
fld public final static int RETR = 13
fld public final static int RETRIEVE = 13
fld public final static int RMD = 23
fld public final static int RNFR = 19
fld public final static int RNTO = 20
fld public final static int SET_MOD_TIME = 35
fld public final static int SITE = 28
fld public final static int SITE_PARAMETERS = 28
fld public final static int SMNT = 5
fld public final static int STAT = 30
fld public final static int STATUS = 30
fld public final static int STOR = 14
fld public final static int STORE = 14
fld public final static int STORE_UNIQUE = 15
fld public final static int STOU = 15
fld public final static int STRU = 11
fld public final static int STRUCTURE_MOUNT = 5
fld public final static int SYST = 29
fld public final static int SYSTEM = 29
fld public final static int TRANSFER_MODE = 12
fld public final static int TYPE = 10
fld public final static int USER = 0
fld public final static int USERNAME = 0
meth public static java.lang.String getCommand(int)
supr java.lang.Object
hfds COMMANDS,LAST

CLSS public org.apache.commons.net.ftp.FTPConnectionClosedException
cons public init()
cons public init(java.lang.String)
supr java.io.IOException
hfds serialVersionUID

CLSS public org.apache.commons.net.ftp.FTPFile
cons public init()
fld public final static int DIRECTORY_TYPE = 1
fld public final static int EXECUTE_PERMISSION = 2
fld public final static int FILE_TYPE = 0
fld public final static int GROUP_ACCESS = 1
fld public final static int READ_PERMISSION = 0
fld public final static int SYMBOLIC_LINK_TYPE = 2
fld public final static int UNKNOWN_TYPE = 3
fld public final static int USER_ACCESS = 0
fld public final static int WORLD_ACCESS = 2
fld public final static int WRITE_PERMISSION = 1
intf java.io.Serializable
meth public boolean hasPermission(int,int)
meth public boolean isDirectory()
meth public boolean isFile()
meth public boolean isSymbolicLink()
meth public boolean isUnknown()
meth public boolean isValid()
meth public int getHardLinkCount()
meth public int getType()
meth public java.lang.String getGroup()
meth public java.lang.String getLink()
meth public java.lang.String getName()
meth public java.lang.String getRawListing()
meth public java.lang.String getUser()
meth public java.lang.String toFormattedString()
meth public java.lang.String toFormattedString(java.lang.String)
meth public java.lang.String toString()
meth public java.time.Instant getTimestampInstant()
meth public java.util.Calendar getTimestamp()
meth public long getSize()
meth public void setGroup(java.lang.String)
meth public void setHardLinkCount(int)
meth public void setLink(java.lang.String)
meth public void setName(java.lang.String)
meth public void setPermission(int,int,boolean)
meth public void setRawListing(java.lang.String)
meth public void setSize(long)
meth public void setTimestamp(java.util.Calendar)
meth public void setType(int)
meth public void setUser(java.lang.String)
supr java.lang.Object
hfds calendar,group,hardLinkCount,link,name,permissions,rawListing,serialVersionUID,size,type,user

CLSS public abstract interface org.apache.commons.net.ftp.FTPFileEntryParser
meth public abstract java.lang.String readNextEntry(java.io.BufferedReader) throws java.io.IOException
meth public abstract java.util.List<java.lang.String> preParse(java.util.List<java.lang.String>)
meth public abstract org.apache.commons.net.ftp.FTPFile parseFTPEntry(java.lang.String)

CLSS public abstract org.apache.commons.net.ftp.FTPFileEntryParserImpl
cons public init()
intf org.apache.commons.net.ftp.FTPFileEntryParser
meth public java.lang.String readNextEntry(java.io.BufferedReader) throws java.io.IOException
meth public java.util.List<java.lang.String> preParse(java.util.List<java.lang.String>)
supr java.lang.Object

CLSS public abstract interface org.apache.commons.net.ftp.FTPFileFilter
meth public abstract boolean accept(org.apache.commons.net.ftp.FTPFile)

CLSS public org.apache.commons.net.ftp.FTPFileFilters
cons public init()
fld public final static org.apache.commons.net.ftp.FTPFileFilter ALL
fld public final static org.apache.commons.net.ftp.FTPFileFilter DIRECTORIES
fld public final static org.apache.commons.net.ftp.FTPFileFilter NON_NULL
supr java.lang.Object

CLSS public org.apache.commons.net.ftp.FTPHTTPClient
cons public init(java.lang.String,int)
cons public init(java.lang.String,int,java.lang.String,java.lang.String)
cons public init(java.lang.String,int,java.lang.String,java.lang.String,java.nio.charset.Charset)
cons public init(java.lang.String,int,java.nio.charset.Charset)
meth protected java.net.Socket _openDataConnection_(int,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth protected java.net.Socket _openDataConnection_(java.lang.String,java.lang.String) throws java.io.IOException
meth public void connect(java.lang.String,int) throws java.io.IOException
supr org.apache.commons.net.ftp.FTPClient
hfds CRLF,charset,proxyHost,proxyPassword,proxyPort,proxyUsername,tunnelHost

CLSS public org.apache.commons.net.ftp.FTPListParseEngine
cons public init(org.apache.commons.net.ftp.FTPFileEntryParser)
meth public boolean hasNext()
meth public boolean hasPrevious()
meth public java.util.List<org.apache.commons.net.ftp.FTPFile> getFileList(org.apache.commons.net.ftp.FTPFileFilter)
meth public org.apache.commons.net.ftp.FTPFile[] getFiles() throws java.io.IOException
meth public org.apache.commons.net.ftp.FTPFile[] getFiles(org.apache.commons.net.ftp.FTPFileFilter) throws java.io.IOException
meth public org.apache.commons.net.ftp.FTPFile[] getNext(int)
meth public org.apache.commons.net.ftp.FTPFile[] getPrevious(int)
meth public void readServerList(java.io.InputStream) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public void readServerList(java.io.InputStream,java.lang.String) throws java.io.IOException
meth public void resetIterator()
supr java.lang.Object
hfds EMPTY_FTP_FILE_ARRAY,entries,internalIterator,parser,saveUnparseableEntries

CLSS public final org.apache.commons.net.ftp.FTPReply
fld public final static int ACTION_ABORTED = 451
fld public final static int BAD_COMMAND_SEQUENCE = 503
fld public final static int BAD_TLS_NEGOTIATION_OR_DATA_ENCRYPTION_REQUIRED = 522
fld public final static int CANNOT_OPEN_DATA_CONNECTION = 425
fld public final static int CLOSING_DATA_CONNECTION = 226
fld public final static int COMMAND_IS_SUPERFLUOUS = 202
fld public final static int COMMAND_NOT_IMPLEMENTED = 502
fld public final static int COMMAND_NOT_IMPLEMENTED_FOR_PARAMETER = 504
fld public final static int COMMAND_OK = 200
fld public final static int DATA_CONNECTION_ALREADY_OPEN = 125
fld public final static int DATA_CONNECTION_OPEN = 225
fld public final static int DENIED_FOR_POLICY_REASONS = 533
fld public final static int DIRECTORY_STATUS = 212
fld public final static int ENTERING_EPSV_MODE = 229
fld public final static int ENTERING_PASSIVE_MODE = 227
fld public final static int EXTENDED_PORT_FAILURE = 522
fld public final static int FAILED_SECURITY_CHECK = 535
fld public final static int FILE_ACTION_NOT_TAKEN = 450
fld public final static int FILE_ACTION_OK = 250
fld public final static int FILE_ACTION_PENDING = 350
fld public final static int FILE_NAME_NOT_ALLOWED = 553
fld public final static int FILE_STATUS = 213
fld public final static int FILE_STATUS_OK = 150
fld public final static int FILE_UNAVAILABLE = 550
fld public final static int HELP_MESSAGE = 214
fld public final static int INSUFFICIENT_STORAGE = 452
fld public final static int NAME_SYSTEM_TYPE = 215
fld public final static int NEED_ACCOUNT = 332
fld public final static int NEED_ACCOUNT_FOR_STORING_FILES = 532
fld public final static int NEED_PASSWORD = 331
fld public final static int NOT_LOGGED_IN = 530
fld public final static int PAGE_TYPE_UNKNOWN = 551
fld public final static int PATHNAME_CREATED = 257
fld public final static int REQUESTED_PROT_LEVEL_NOT_SUPPORTED = 536
fld public final static int REQUEST_DENIED = 534
fld public final static int RESTART_MARKER = 110
fld public final static int SECURITY_DATA_EXCHANGE_COMPLETE = 234
fld public final static int SECURITY_DATA_EXCHANGE_SUCCESSFULLY = 235
fld public final static int SECURITY_DATA_IS_ACCEPTABLE = 335
fld public final static int SECURITY_MECHANISM_IS_OK = 334
fld public final static int SERVICE_CLOSING_CONTROL_CONNECTION = 221
fld public final static int SERVICE_NOT_AVAILABLE = 421
fld public final static int SERVICE_NOT_READY = 120
fld public final static int SERVICE_READY = 220
fld public final static int STORAGE_ALLOCATION_EXCEEDED = 552
fld public final static int SYNTAX_ERROR_IN_ARGUMENTS = 501
fld public final static int SYSTEM_STATUS = 211
fld public final static int TRANSFER_ABORTED = 426
fld public final static int UNAVAILABLE_RESOURCE = 431
fld public final static int UNRECOGNIZED_COMMAND = 500
fld public final static int USER_LOGGED_IN = 230
meth public static boolean isNegativePermanent(int)
meth public static boolean isNegativeTransient(int)
meth public static boolean isPositiveCompletion(int)
meth public static boolean isPositiveIntermediate(int)
meth public static boolean isPositivePreliminary(int)
meth public static boolean isProtectedReplyCode(int)
supr java.lang.Object

CLSS public org.apache.commons.net.ftp.FTPSClient
cons public init()
cons public init(boolean)
cons public init(boolean,javax.net.ssl.SSLContext)
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(javax.net.ssl.SSLContext)
fld public final static int DEFAULT_FTPS_DATA_PORT = 989
fld public final static int DEFAULT_FTPS_PORT = 990
fld public static java.lang.String KEYSTORE_ALGORITHM
 anno 0 java.lang.Deprecated()
fld public static java.lang.String PROVIDER
 anno 0 java.lang.Deprecated()
fld public static java.lang.String STORE_TYPE
 anno 0 java.lang.Deprecated()
fld public static java.lang.String TRUSTSTORE_ALGORITHM
 anno 0 java.lang.Deprecated()
meth protected java.net.Socket _openDataConnection_(int,java.lang.String) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth protected java.net.Socket _openDataConnection_(java.lang.String,java.lang.String) throws java.io.IOException
meth protected void _connectAction_() throws java.io.IOException
meth protected void _prepareDataSocket_(java.net.Socket) throws java.io.IOException
meth protected void execAUTH() throws java.io.IOException
meth protected void sslNegotiation() throws java.io.IOException
meth public boolean getEnableSessionCreation()
meth public boolean getNeedClientAuth()
meth public boolean getUseClientMode()
meth public boolean getWantClientAuth()
meth public boolean isEndpointCheckingEnabled()
meth public byte[] parseADATReply(java.lang.String)
meth public int execADAT(byte[]) throws java.io.IOException
meth public int execAUTH(java.lang.String) throws java.io.IOException
meth public int execCCC() throws java.io.IOException
meth public int execCONF(byte[]) throws java.io.IOException
meth public int execENC(byte[]) throws java.io.IOException
meth public int execMIC(byte[]) throws java.io.IOException
meth public int sendCommand(java.lang.String,java.lang.String) throws java.io.IOException
meth public java.lang.String getAuthValue()
meth public java.lang.String[] getEnabledCipherSuites()
meth public java.lang.String[] getEnabledProtocols()
meth public javax.net.ssl.HostnameVerifier getHostnameVerifier()
meth public javax.net.ssl.TrustManager getTrustManager()
meth public long parsePBSZ(long) throws java.io.IOException
meth public void disconnect() throws java.io.IOException
meth public void execPBSZ(long) throws java.io.IOException
meth public void execPROT(java.lang.String) throws java.io.IOException
meth public void setAuthValue(java.lang.String)
meth public void setEnabledCipherSuites(java.lang.String[])
meth public void setEnabledProtocols(java.lang.String[])
meth public void setEnabledSessionCreation(boolean)
meth public void setEndpointCheckingEnabled(boolean)
meth public void setHostnameVerifier(javax.net.ssl.HostnameVerifier)
meth public void setKeyManager(javax.net.ssl.KeyManager)
meth public void setNeedClientAuth(boolean)
meth public void setTrustManager(javax.net.ssl.TrustManager)
meth public void setUseClientMode(boolean)
meth public void setWantClientAuth(boolean)
supr org.apache.commons.net.ftp.FTPClient
hfds CMD_ADAT,CMD_AUTH,CMD_CCC,CMD_CONF,CMD_ENC,CMD_MIC,CMD_PBSZ,CMD_PROT,DEFAULT_PROT,DEFAULT_PROTOCOL,PROT_COMMAND_VALUE,auth,context,hostnameVerifier,isClientMode,isCreation,isImplicit,isNeedClientAuth,isWantClientAuth,keyManager,plainSocket,protocol,protocols,suites,tlsEndpointChecking,trustManager

CLSS public final org.apache.commons.net.ftp.FTPSCommand
 anno 0 java.lang.Deprecated()
cons public init()
fld public final static int ADAT = 1
fld public final static int AUTH = 0
fld public final static int AUTHENTICATION_SECURITY_DATA = 1
fld public final static int AUTHENTICATION_SECURITY_MECHANISM = 0
fld public final static int CCC = 4
fld public final static int CLEAR_COMMAND_CHANNEL = 4
fld public final static int DATA_CHANNEL_PROTECTION_LEVEL = 3
fld public final static int PBSZ = 2
fld public final static int PROT = 3
fld public final static int PROTECTION_BUFFER_SIZE = 2
meth public static java.lang.String getCommand(int)
supr java.lang.Object
hfds commands

CLSS public org.apache.commons.net.ftp.FTPSServerSocketFactory
cons public init(javax.net.ssl.SSLContext)
meth public java.net.ServerSocket createServerSocket() throws java.io.IOException
meth public java.net.ServerSocket createServerSocket(int) throws java.io.IOException
meth public java.net.ServerSocket createServerSocket(int,int) throws java.io.IOException
meth public java.net.ServerSocket createServerSocket(int,int,java.net.InetAddress) throws java.io.IOException
meth public java.net.ServerSocket init(java.net.ServerSocket)
supr javax.net.ServerSocketFactory
hfds sslContext

CLSS public org.apache.commons.net.ftp.FTPSSocketFactory
cons public init(javax.net.ssl.SSLContext)
meth public java.net.ServerSocket createServerSocket(int) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public java.net.ServerSocket createServerSocket(int,int) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public java.net.ServerSocket createServerSocket(int,int,java.net.InetAddress) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public java.net.ServerSocket init(java.net.ServerSocket) throws java.io.IOException
 anno 0 java.lang.Deprecated()
meth public java.net.Socket createSocket() throws java.io.IOException
meth public java.net.Socket createSocket(java.lang.String,int) throws java.io.IOException
meth public java.net.Socket createSocket(java.lang.String,int,java.net.InetAddress,int) throws java.io.IOException
meth public java.net.Socket createSocket(java.net.InetAddress,int) throws java.io.IOException
meth public java.net.Socket createSocket(java.net.InetAddress,int,java.net.InetAddress,int) throws java.io.IOException
supr javax.net.SocketFactory
hfds context

CLSS public org.apache.commons.net.ftp.FTPSTrustManager
 anno 0 java.lang.Deprecated()
cons public init()
intf javax.net.ssl.X509TrustManager
meth public java.security.cert.X509Certificate[] getAcceptedIssuers()
meth public void checkClientTrusted(java.security.cert.X509Certificate[],java.lang.String)
meth public void checkServerTrusted(java.security.cert.X509Certificate[],java.lang.String) throws java.security.cert.CertificateException
supr java.lang.Object

CLSS abstract interface org.apache.commons.net.ftp.package-info

CLSS abstract interface org.apache.commons.net.package-info

CLSS public org.apache.commons.net.util.Base64
 anno 0 java.lang.Deprecated()
cons public init()
cons public init(boolean)
cons public init(int)
cons public init(int,byte[])
cons public init(int,byte[],boolean)
meth public boolean isUrlSafe()
meth public byte[] decode(byte[])
meth public byte[] decode(java.lang.String)
meth public byte[] encode(byte[])
meth public java.lang.String encodeToString(byte[])
meth public static boolean isArrayByteBase64(byte[])
meth public static boolean isBase64(byte)
meth public static byte[] decodeBase64(byte[])
meth public static byte[] decodeBase64(java.lang.String)
meth public static byte[] encodeBase64(byte[])
meth public static byte[] encodeBase64(byte[],boolean)
meth public static byte[] encodeBase64(byte[],boolean,boolean)
meth public static byte[] encodeBase64(byte[],boolean,boolean,int)
meth public static byte[] encodeBase64Chunked(byte[])
meth public static byte[] encodeBase64URLSafe(byte[])
meth public static byte[] encodeInteger(java.math.BigInteger)
meth public static java.lang.String encodeBase64String(byte[])
meth public static java.lang.String encodeBase64String(byte[],boolean)
meth public static java.lang.String encodeBase64StringUnChunked(byte[])
meth public static java.lang.String encodeBase64URLSafeString(byte[])
meth public static java.math.BigInteger decodeInteger(byte[])
supr java.lang.Object
hfds CHUNK_SEPARATOR,CHUNK_SIZE,DECODE_TABLE,DEFAULT_BUFFER_RESIZE_FACTOR,DEFAULT_BUFFER_SIZE,MASK_6BITS,MASK_8BITS,PAD,STANDARD_ENCODE_TABLE,URL_SAFE_ENCODE_TABLE,buffer,currentLinePos,decodeSize,encodeSize,encodeTable,eof,lineLength,lineSeparator,modulus,pos,readPos,x

CLSS public org.apache.commons.net.util.Charsets
cons public init()
meth public static java.nio.charset.Charset toCharset(java.lang.String)
meth public static java.nio.charset.Charset toCharset(java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public final org.apache.commons.net.util.KeyManagerUtils
meth public static javax.net.ssl.KeyManager createClientKeyManager(java.io.File,java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException
meth public static javax.net.ssl.KeyManager createClientKeyManager(java.io.File,java.lang.String,java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException
meth public static javax.net.ssl.KeyManager createClientKeyManager(java.lang.String,java.io.File,java.lang.String,java.lang.String,java.lang.String) throws java.io.IOException,java.security.GeneralSecurityException
meth public static javax.net.ssl.KeyManager createClientKeyManager(java.security.KeyStore,java.lang.String,java.lang.String) throws java.security.GeneralSecurityException
supr java.lang.Object
hfds DEFAULT_STORE_TYPE
hcls ClientKeyStore,X509KeyManager

CLSS public org.apache.commons.net.util.ListenerList
cons public init()
intf java.io.Serializable
intf java.lang.Iterable<java.util.EventListener>
meth public int getListenerCount()
meth public java.util.Iterator<java.util.EventListener> iterator()
meth public void addListener(java.util.EventListener)
meth public void removeListener(java.util.EventListener)
supr java.lang.Object
hfds listeners,serialVersionUID

CLSS public org.apache.commons.net.util.NetConstants
fld public final static byte[] EMPTY_BTYE_ARRAY
fld public final static int EOS = -1
fld public final static java.lang.String[] EMPTY_STRING_ARRAY
fld public final static java.security.cert.X509Certificate[] EMPTY_X509_CERTIFICATE_ARRAY
supr java.lang.Object

CLSS public org.apache.commons.net.util.SSLContextUtils
meth public static javax.net.ssl.SSLContext createSSLContext(java.lang.String,javax.net.ssl.KeyManager,javax.net.ssl.TrustManager) throws java.io.IOException
meth public static javax.net.ssl.SSLContext createSSLContext(java.lang.String,javax.net.ssl.KeyManager[],javax.net.ssl.TrustManager[]) throws java.io.IOException
supr java.lang.Object

CLSS public org.apache.commons.net.util.SSLSocketUtils
meth public static boolean enableEndpointNameVerification(javax.net.ssl.SSLSocket)
supr java.lang.Object

CLSS public org.apache.commons.net.util.SubnetUtils
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
innr public final SubnetInfo
meth public boolean isInclusiveHostCount()
meth public final org.apache.commons.net.util.SubnetUtils$SubnetInfo getInfo()
meth public org.apache.commons.net.util.SubnetUtils getNext()
meth public org.apache.commons.net.util.SubnetUtils getPrevious()
meth public void setInclusiveHostCount(boolean)
supr java.lang.Object
hfds ADDRESS_PATTERN,CIDR_PATTERN,IP_ADDRESS,NBITS,PARSE_FAIL,SLASH_FORMAT,address,broadcast,inclusiveHostCount,netmask,network

CLSS public final org.apache.commons.net.util.SubnetUtils$SubnetInfo
 outer org.apache.commons.net.util.SubnetUtils
meth public boolean isInRange(int)
meth public boolean isInRange(java.lang.String)
meth public int asInteger(java.lang.String)
meth public int getAddressCount()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getAddress()
meth public java.lang.String getBroadcastAddress()
meth public java.lang.String getCidrSignature()
meth public java.lang.String getHighAddress()
meth public java.lang.String getLowAddress()
meth public java.lang.String getNetmask()
meth public java.lang.String getNetworkAddress()
meth public java.lang.String getNextAddress()
meth public java.lang.String getPreviousAddress()
meth public java.lang.String toString()
meth public java.lang.String[] getAllAddresses()
meth public long getAddressCountLong()
supr java.lang.Object
hfds UNSIGNED_INT_MASK

CLSS public final org.apache.commons.net.util.TrustManagerUtils
cons public init()
meth public static javax.net.ssl.X509TrustManager getAcceptAllTrustManager()
meth public static javax.net.ssl.X509TrustManager getDefaultTrustManager(java.security.KeyStore) throws java.security.GeneralSecurityException
meth public static javax.net.ssl.X509TrustManager getValidateServerCertificateTrustManager()
supr java.lang.Object
hfds ACCEPT_ALL,CHECK_SERVER_VALIDITY
hcls TrustManager

CLSS abstract interface org.apache.commons.net.util.package-info

