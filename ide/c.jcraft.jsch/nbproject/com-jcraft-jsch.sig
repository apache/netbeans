#Signature file v4.1
#Version ${mf.OpenIDE-Module-Specification-Version}

CLSS public abstract interface com.jcraft.jsch.AgentConnector
meth public abstract boolean isAvailable()
meth public abstract java.lang.String getName()
meth public abstract void query(com.jcraft.jsch.Buffer) throws com.jcraft.jsch.AgentProxyException

CLSS public com.jcraft.jsch.AgentIdentityRepository
cons public init(com.jcraft.jsch.AgentConnector)
intf com.jcraft.jsch.IdentityRepository
meth public boolean add(byte[])
meth public boolean remove(byte[])
meth public int getStatus()
meth public java.lang.String getName()
meth public java.util.Vector<com.jcraft.jsch.Identity> getIdentities()
meth public void removeAll()
supr java.lang.Object
hfds agent

CLSS public com.jcraft.jsch.AgentProxyException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public com.jcraft.jsch.Buffer
cons public init()
cons public init(byte[])
cons public init(int)
meth public byte[] getMPInt()
meth public byte[] getMPIntBits()
meth public byte[] getString()
meth public int getByte()
meth public int getByte(int)
meth public int getInt()
meth public int getLength()
meth public int getOffSet()
meth public long getLong()
meth public long getUInt()
meth public void getByte(byte[])
meth public void putByte(byte)
meth public void putByte(byte[])
meth public void putByte(byte[],int,int)
meth public void putInt(int)
meth public void putLong(long)
meth public void putMPInt(byte[])
meth public void putString(byte[])
meth public void putString(byte[],int,int)
meth public void reset()
meth public void setOffSet(int)
meth public void shift()
supr java.lang.Object
hfds buffer,index,s,tmp

CLSS public abstract com.jcraft.jsch.Channel
fld protected byte[] type
intf java.lang.Runnable
meth protected com.jcraft.jsch.Packet genChannelOpenPacket()
meth protected void sendChannelOpen() throws java.lang.Exception
meth protected void sendOpenConfirmation() throws java.lang.Exception
meth protected void sendOpenFailure(int)
meth public boolean isClosed()
meth public boolean isConnected()
meth public boolean isEOF()
meth public com.jcraft.jsch.Session getSession() throws com.jcraft.jsch.JSchException
meth public int getExitStatus()
meth public int getId()
meth public java.io.InputStream getExtInputStream() throws java.io.IOException
meth public java.io.InputStream getInputStream() throws java.io.IOException
meth public java.io.OutputStream getOutputStream() throws java.io.IOException
meth public void connect() throws com.jcraft.jsch.JSchException
meth public void connect(int) throws com.jcraft.jsch.JSchException
meth public void disconnect()
meth public void run()
meth public void sendSignal(java.lang.String) throws java.lang.Exception
meth public void setExtOutputStream(java.io.OutputStream)
meth public void setExtOutputStream(java.io.OutputStream,boolean)
meth public void setInputStream(java.io.InputStream)
meth public void setInputStream(java.io.InputStream,boolean)
meth public void setOutputStream(java.io.OutputStream)
meth public void setOutputStream(java.io.OutputStream,boolean)
meth public void setXForwarding(boolean)
meth public void start() throws com.jcraft.jsch.JSchException
supr java.lang.Object
hfds SSH_MSG_CHANNEL_OPEN_CONFIRMATION,SSH_MSG_CHANNEL_OPEN_FAILURE,SSH_MSG_CHANNEL_WINDOW_ADJUST,SSH_OPEN_ADMINISTRATIVELY_PROHIBITED,SSH_OPEN_CONNECT_FAILED,SSH_OPEN_RESOURCE_SHORTAGE,SSH_OPEN_UNKNOWN_CHANNEL_TYPE,close,connectTimeout,connected,eof_local,eof_remote,exitstatus,id,index,io,lmpsize,lwsize,lwsize_max,notifyme,open_confirmation,pool,recipient,reply,rmpsize,rwsize,session,thread
hcls MyPipedInputStream,PassiveInputStream,PassiveOutputStream

CLSS public com.jcraft.jsch.ChannelDirectStreamLocal
meth protected com.jcraft.jsch.Packet genChannelOpenPacket()
meth public java.lang.String getSocketPath()
meth public void setSocketPath(java.lang.String)
supr com.jcraft.jsch.ChannelDirectTCPIP
hfds LOCAL_MAXIMUM_PACKET_SIZE,LOCAL_WINDOW_SIZE_MAX,_type,socketPath

CLSS public com.jcraft.jsch.ChannelDirectTCPIP
meth protected com.jcraft.jsch.Packet genChannelOpenPacket()
meth public void connect(int) throws com.jcraft.jsch.JSchException
meth public void run()
meth public void setHost(java.lang.String)
meth public void setInputStream(java.io.InputStream)
meth public void setOrgIPAddress(java.lang.String)
meth public void setOrgPort(int)
meth public void setOutputStream(java.io.OutputStream)
meth public void setPort(int)
supr com.jcraft.jsch.Channel
hfds LOCAL_MAXIMUM_PACKET_SIZE,LOCAL_WINDOW_SIZE_MAX,_type,host,originator_IP_address,originator_port,port

CLSS public com.jcraft.jsch.ChannelExec
cons public init()
fld protected boolean agent_forwarding
fld protected boolean pty
fld protected boolean xforwading
fld protected byte[] terminal_mode
fld protected int tcol
fld protected int thp
fld protected int trow
fld protected int twp
fld protected java.lang.String ttype
fld protected java.util.Hashtable<byte[],byte[]> env
meth protected void sendRequests() throws java.lang.Exception
meth public java.io.InputStream getErrStream() throws java.io.IOException
meth public void run()
meth public void setAgentForwarding(boolean)
meth public void setCommand(byte[])
meth public void setCommand(java.lang.String)
meth public void setEnv(byte[],byte[])
meth public void setEnv(java.lang.String,java.lang.String)
meth public void setEnv(java.util.Hashtable<byte[],byte[]>)
 anno 0 java.lang.Deprecated()
meth public void setErrStream(java.io.OutputStream)
meth public void setErrStream(java.io.OutputStream,boolean)
meth public void setPty(boolean)
meth public void setPtySize(int,int,int,int)
meth public void setPtyType(java.lang.String)
meth public void setPtyType(java.lang.String,int,int,int,int)
meth public void setTerminalMode(byte[])
meth public void setXForwarding(boolean)
meth public void start() throws com.jcraft.jsch.JSchException
supr com.jcraft.jsch.Channel
hfds command

CLSS public com.jcraft.jsch.ChannelForwardedTCPIP
meth public int getRemotePort()
meth public void run()
supr com.jcraft.jsch.Channel
hfds LOCAL_MAXIMUM_PACKET_SIZE,LOCAL_WINDOW_SIZE_MAX,TIMEOUT,config,daemon,pool,socket
hcls Config,ConfigDaemon,ConfigLHost

CLSS public com.jcraft.jsch.ChannelSftp
cons public init()
fld protected boolean agent_forwarding
fld protected boolean pty
fld protected boolean xforwading
fld protected byte[] terminal_mode
fld protected int tcol
fld protected int thp
fld protected int trow
fld protected int twp
fld protected java.lang.String ttype
fld protected java.util.Hashtable<byte[],byte[]> env
fld public final static int APPEND = 2
fld public final static int OVERWRITE = 0
fld public final static int RESUME = 1
fld public final static int SSH_FX_BAD_MESSAGE = 5
fld public final static int SSH_FX_CONNECTION_LOST = 7
fld public final static int SSH_FX_EOF = 1
fld public final static int SSH_FX_FAILURE = 4
fld public final static int SSH_FX_NO_CONNECTION = 6
fld public final static int SSH_FX_NO_SUCH_FILE = 2
fld public final static int SSH_FX_OK = 0
fld public final static int SSH_FX_OP_UNSUPPORTED = 8
fld public final static int SSH_FX_PERMISSION_DENIED = 3
innr public abstract interface static LsEntrySelector
innr public static LsEntry
meth protected void sendRequests() throws java.lang.Exception
meth public com.jcraft.jsch.SftpATTRS lstat(java.lang.String) throws com.jcraft.jsch.SftpException
meth public com.jcraft.jsch.SftpATTRS stat(java.lang.String) throws com.jcraft.jsch.SftpException
meth public com.jcraft.jsch.SftpStatVFS statVFS(java.lang.String) throws com.jcraft.jsch.SftpException
meth public int getBulkRequests()
meth public int getServerVersion() throws com.jcraft.jsch.SftpException
meth public java.io.InputStream get(java.lang.String) throws com.jcraft.jsch.SftpException
meth public java.io.InputStream get(java.lang.String,com.jcraft.jsch.SftpProgressMonitor) throws com.jcraft.jsch.SftpException
meth public java.io.InputStream get(java.lang.String,com.jcraft.jsch.SftpProgressMonitor,int) throws com.jcraft.jsch.SftpException
 anno 0 java.lang.Deprecated()
meth public java.io.InputStream get(java.lang.String,com.jcraft.jsch.SftpProgressMonitor,long) throws com.jcraft.jsch.SftpException
meth public java.io.InputStream get(java.lang.String,int) throws com.jcraft.jsch.SftpException
 anno 0 java.lang.Deprecated()
meth public java.io.OutputStream put(java.lang.String) throws com.jcraft.jsch.SftpException
meth public java.io.OutputStream put(java.lang.String,com.jcraft.jsch.SftpProgressMonitor,int) throws com.jcraft.jsch.SftpException
meth public java.io.OutputStream put(java.lang.String,com.jcraft.jsch.SftpProgressMonitor,int,long) throws com.jcraft.jsch.SftpException
meth public java.io.OutputStream put(java.lang.String,int) throws com.jcraft.jsch.SftpException
meth public java.lang.String getExtension(java.lang.String)
meth public java.lang.String getHome() throws com.jcraft.jsch.SftpException
meth public java.lang.String lpwd()
meth public java.lang.String pwd() throws com.jcraft.jsch.SftpException
meth public java.lang.String readlink(java.lang.String) throws com.jcraft.jsch.SftpException
meth public java.lang.String realpath(java.lang.String) throws com.jcraft.jsch.SftpException
meth public java.lang.String version()
meth public java.util.Vector<com.jcraft.jsch.ChannelSftp$LsEntry> ls(java.lang.String) throws com.jcraft.jsch.SftpException
meth public void _put(java.io.InputStream,java.lang.String,com.jcraft.jsch.SftpProgressMonitor,int) throws com.jcraft.jsch.SftpException
meth public void cd(java.lang.String) throws com.jcraft.jsch.SftpException
meth public void chgrp(int,java.lang.String) throws com.jcraft.jsch.SftpException
meth public void chmod(int,java.lang.String) throws com.jcraft.jsch.SftpException
meth public void chown(int,java.lang.String) throws com.jcraft.jsch.SftpException
meth public void disconnect()
meth public void exit()
meth public void get(java.lang.String,java.io.OutputStream) throws com.jcraft.jsch.SftpException
meth public void get(java.lang.String,java.io.OutputStream,com.jcraft.jsch.SftpProgressMonitor) throws com.jcraft.jsch.SftpException
meth public void get(java.lang.String,java.io.OutputStream,com.jcraft.jsch.SftpProgressMonitor,int,long) throws com.jcraft.jsch.SftpException
meth public void get(java.lang.String,java.lang.String) throws com.jcraft.jsch.SftpException
meth public void get(java.lang.String,java.lang.String,com.jcraft.jsch.SftpProgressMonitor) throws com.jcraft.jsch.SftpException
meth public void get(java.lang.String,java.lang.String,com.jcraft.jsch.SftpProgressMonitor,int) throws com.jcraft.jsch.SftpException
meth public void hardlink(java.lang.String,java.lang.String) throws com.jcraft.jsch.SftpException
meth public void lcd(java.lang.String) throws com.jcraft.jsch.SftpException
meth public void ls(java.lang.String,com.jcraft.jsch.ChannelSftp$LsEntrySelector) throws com.jcraft.jsch.SftpException
meth public void mkdir(java.lang.String) throws com.jcraft.jsch.SftpException
meth public void put(java.io.InputStream,java.lang.String) throws com.jcraft.jsch.SftpException
meth public void put(java.io.InputStream,java.lang.String,com.jcraft.jsch.SftpProgressMonitor) throws com.jcraft.jsch.SftpException
meth public void put(java.io.InputStream,java.lang.String,com.jcraft.jsch.SftpProgressMonitor,int) throws com.jcraft.jsch.SftpException
meth public void put(java.io.InputStream,java.lang.String,int) throws com.jcraft.jsch.SftpException
meth public void put(java.lang.String,java.lang.String) throws com.jcraft.jsch.SftpException
meth public void put(java.lang.String,java.lang.String,com.jcraft.jsch.SftpProgressMonitor) throws com.jcraft.jsch.SftpException
meth public void put(java.lang.String,java.lang.String,com.jcraft.jsch.SftpProgressMonitor,int) throws com.jcraft.jsch.SftpException
meth public void put(java.lang.String,java.lang.String,int) throws com.jcraft.jsch.SftpException
meth public void quit()
meth public void rename(java.lang.String,java.lang.String) throws com.jcraft.jsch.SftpException
meth public void rm(java.lang.String) throws com.jcraft.jsch.SftpException
meth public void rmdir(java.lang.String) throws com.jcraft.jsch.SftpException
meth public void run()
meth public void setAgentForwarding(boolean)
meth public void setBulkRequests(int) throws com.jcraft.jsch.JSchException
meth public void setEnv(byte[],byte[])
meth public void setEnv(java.lang.String,java.lang.String)
meth public void setEnv(java.util.Hashtable<byte[],byte[]>)
 anno 0 java.lang.Deprecated()
meth public void setFilenameEncoding(java.lang.String) throws com.jcraft.jsch.SftpException
 anno 0 java.lang.Deprecated()
meth public void setFilenameEncoding(java.nio.charset.Charset) throws com.jcraft.jsch.SftpException
meth public void setMtime(java.lang.String,int) throws com.jcraft.jsch.SftpException
meth public void setPty(boolean)
meth public void setPtySize(int,int,int,int)
meth public void setPtyType(java.lang.String)
meth public void setPtyType(java.lang.String,int,int,int,int)
meth public void setStat(java.lang.String,com.jcraft.jsch.SftpATTRS) throws com.jcraft.jsch.SftpException
meth public void setTerminalMode(byte[])
meth public void setXForwarding(boolean)
meth public void start() throws com.jcraft.jsch.JSchException
meth public void symlink(java.lang.String,java.lang.String) throws com.jcraft.jsch.SftpException
supr com.jcraft.jsch.Channel
hfds LOCAL_MAXIMUM_PACKET_SIZE,LOCAL_WINDOW_SIZE_MAX,MAX_MSG_LENGTH,SSH_FILEXFER_ATTR_ACMODTIME,SSH_FILEXFER_ATTR_EXTENDED,SSH_FILEXFER_ATTR_PERMISSIONS,SSH_FILEXFER_ATTR_SIZE,SSH_FILEXFER_ATTR_UIDGID,SSH_FXF_APPEND,SSH_FXF_CREAT,SSH_FXF_EXCL,SSH_FXF_READ,SSH_FXF_TRUNC,SSH_FXF_WRITE,SSH_FXP_ATTRS,SSH_FXP_CLOSE,SSH_FXP_DATA,SSH_FXP_EXTENDED,SSH_FXP_EXTENDED_REPLY,SSH_FXP_FSETSTAT,SSH_FXP_FSTAT,SSH_FXP_HANDLE,SSH_FXP_INIT,SSH_FXP_LSTAT,SSH_FXP_MKDIR,SSH_FXP_NAME,SSH_FXP_OPEN,SSH_FXP_OPENDIR,SSH_FXP_READ,SSH_FXP_READDIR,SSH_FXP_READLINK,SSH_FXP_REALPATH,SSH_FXP_REMOVE,SSH_FXP_RENAME,SSH_FXP_RMDIR,SSH_FXP_SETSTAT,SSH_FXP_STAT,SSH_FXP_STATUS,SSH_FXP_SYMLINK,SSH_FXP_VERSION,SSH_FXP_WRITE,ackid,buf,client_version,cwd,extension_hardlink,extension_posix_rename,extension_statvfs,extensions,fEncoding,fEncoding_is_utf8,file_separator,file_separatorc,fs_is_bs,home,interactive,io_in,lcwd,obuf,opacket,packet,rq,seq,server_version,version
hcls Header,RequestQueue

CLSS public static com.jcraft.jsch.ChannelSftp$LsEntry
 outer com.jcraft.jsch.ChannelSftp
intf java.lang.Comparable<com.jcraft.jsch.ChannelSftp$LsEntry>
meth public com.jcraft.jsch.SftpATTRS getAttrs()
meth public int compareTo(com.jcraft.jsch.ChannelSftp$LsEntry)
meth public java.lang.String getFilename()
meth public java.lang.String getLongname()
meth public java.lang.String toString()
supr java.lang.Object
hfds attrs,filename,longname

CLSS public abstract interface static com.jcraft.jsch.ChannelSftp$LsEntrySelector
 outer com.jcraft.jsch.ChannelSftp
fld public final static int BREAK = 1
fld public final static int CONTINUE = 0
meth public abstract int select(com.jcraft.jsch.ChannelSftp$LsEntry)

CLSS public com.jcraft.jsch.ChannelShell
fld protected boolean agent_forwarding
fld protected boolean pty
fld protected boolean xforwading
fld protected byte[] terminal_mode
fld protected int tcol
fld protected int thp
fld protected int trow
fld protected int twp
fld protected java.lang.String ttype
fld protected java.util.Hashtable<byte[],byte[]> env
meth protected void sendRequests() throws java.lang.Exception
meth public void run()
meth public void setAgentForwarding(boolean)
meth public void setEnv(byte[],byte[])
meth public void setEnv(java.lang.String,java.lang.String)
meth public void setEnv(java.util.Hashtable<byte[],byte[]>)
 anno 0 java.lang.Deprecated()
meth public void setPty(boolean)
meth public void setPtySize(int,int,int,int)
meth public void setPtyType(java.lang.String)
meth public void setPtyType(java.lang.String,int,int,int,int)
meth public void setTerminalMode(byte[])
meth public void setXForwarding(boolean)
meth public void start() throws com.jcraft.jsch.JSchException
supr com.jcraft.jsch.Channel

CLSS public com.jcraft.jsch.ChannelSubsystem
cons public init()
fld protected boolean agent_forwarding
fld protected boolean pty
fld protected boolean xforwading
fld protected byte[] terminal_mode
fld protected int tcol
fld protected int thp
fld protected int trow
fld protected int twp
fld protected java.lang.String ttype
fld protected java.util.Hashtable<byte[],byte[]> env
meth protected void sendRequests() throws java.lang.Exception
meth public java.io.InputStream getErrStream() throws java.io.IOException
meth public void run()
meth public void setAgentForwarding(boolean)
meth public void setEnv(byte[],byte[])
meth public void setEnv(java.lang.String,java.lang.String)
meth public void setEnv(java.util.Hashtable<byte[],byte[]>)
 anno 0 java.lang.Deprecated()
meth public void setErrStream(java.io.OutputStream)
meth public void setPty(boolean)
meth public void setPtySize(int,int,int,int)
meth public void setPtyType(java.lang.String)
meth public void setPtyType(java.lang.String,int,int,int,int)
meth public void setSubsystem(java.lang.String)
meth public void setTerminalMode(byte[])
meth public void setWantReply(boolean)
meth public void setXForwarding(boolean)
meth public void start() throws com.jcraft.jsch.JSchException
supr com.jcraft.jsch.Channel
hfds subsystem,want_reply

CLSS public abstract interface com.jcraft.jsch.Cipher
fld public final static int DECRYPT_MODE = 1
fld public final static int ENCRYPT_MODE = 0
meth public abstract boolean isAEAD()
meth public abstract boolean isCBC()
meth public abstract boolean isChaCha20()
meth public abstract int getBlockSize()
meth public abstract int getIVSize()
meth public abstract int getTagSize()
meth public abstract void doFinal(byte[],int,int,byte[],int) throws java.lang.Exception
meth public abstract void init(int,byte[],byte[]) throws java.lang.Exception
meth public abstract void update(byte[],int,int,byte[],int) throws java.lang.Exception
meth public abstract void update(int) throws java.lang.Exception
meth public abstract void updateAAD(byte[],int,int) throws java.lang.Exception

CLSS public com.jcraft.jsch.CipherNone
cons public init()
intf com.jcraft.jsch.Cipher
meth public boolean isAEAD()
meth public boolean isCBC()
meth public boolean isChaCha20()
meth public int getBlockSize()
meth public int getIVSize()
meth public int getTagSize()
meth public void doFinal(byte[],int,int,byte[],int) throws java.lang.Exception
meth public void init(int,byte[],byte[]) throws java.lang.Exception
meth public void update(byte[],int,int,byte[],int) throws java.lang.Exception
meth public void update(int) throws java.lang.Exception
meth public void updateAAD(byte[],int,int) throws java.lang.Exception
supr java.lang.Object
hfds bsize,ivsize

CLSS public abstract interface com.jcraft.jsch.Compression
fld public final static int DEFLATER = 1
fld public final static int INFLATER = 0
meth public abstract byte[] compress(byte[],int,int[])
meth public abstract byte[] uncompress(byte[],int,int[])
meth public abstract void init(int,int) throws java.lang.Exception

CLSS public abstract interface com.jcraft.jsch.ConfigRepository
fld public final static com.jcraft.jsch.ConfigRepository nullConfig
fld public final static com.jcraft.jsch.ConfigRepository$Config defaultConfig
innr public abstract interface static Config
meth public abstract com.jcraft.jsch.ConfigRepository$Config getConfig(java.lang.String)

CLSS public abstract interface static com.jcraft.jsch.ConfigRepository$Config
 outer com.jcraft.jsch.ConfigRepository
meth public abstract int getPort()
meth public abstract java.lang.String getHostname()
meth public abstract java.lang.String getUser()
meth public abstract java.lang.String getValue(java.lang.String)
meth public abstract java.lang.String[] getValues(java.lang.String)

CLSS public abstract interface com.jcraft.jsch.DH
meth public abstract byte[] getE() throws java.lang.Exception
meth public abstract byte[] getK() throws java.lang.Exception
meth public abstract void checkRange() throws java.lang.Exception
meth public abstract void init() throws java.lang.Exception
meth public abstract void setF(byte[])
meth public abstract void setG(byte[])
meth public abstract void setP(byte[])

CLSS public com.jcraft.jsch.DH25519
cons public init()
supr com.jcraft.jsch.DHXEC

CLSS public com.jcraft.jsch.DH448
cons public init()
supr com.jcraft.jsch.DHXEC

CLSS public com.jcraft.jsch.DHEC256
cons public init()
supr com.jcraft.jsch.DHECN

CLSS public com.jcraft.jsch.DHEC384
cons public init()
supr com.jcraft.jsch.DHECN

CLSS public com.jcraft.jsch.DHEC521
cons public init()
supr com.jcraft.jsch.DHECN

CLSS public abstract com.jcraft.jsch.DHECN
cons public init()
fld protected int key_size
fld protected java.lang.String sha_name
meth public boolean next(com.jcraft.jsch.Buffer) throws java.lang.Exception
meth public int getState()
meth public void init(com.jcraft.jsch.Session,byte[],byte[],byte[],byte[]) throws java.lang.Exception
supr com.jcraft.jsch.KeyExchange
hfds I_C,I_S,Q_C,SSH_MSG_KEX_ECDH_INIT,SSH_MSG_KEX_ECDH_REPLY,V_C,V_S,buf,e,ecdh,packet,state

CLSS public com.jcraft.jsch.DHG1
cons public init()
supr com.jcraft.jsch.DHGN
hfds g,p

CLSS public com.jcraft.jsch.DHG14
cons public init()
supr com.jcraft.jsch.DHG14N

CLSS public com.jcraft.jsch.DHG14224
cons public init()
supr com.jcraft.jsch.DHG14N

CLSS public com.jcraft.jsch.DHG14256
cons public init()
supr com.jcraft.jsch.DHG14N

CLSS public abstract com.jcraft.jsch.DHG14N
cons public init()
supr com.jcraft.jsch.DHGN
hfds g,p

CLSS public com.jcraft.jsch.DHG15
cons public init()
supr com.jcraft.jsch.DHG15N

CLSS public com.jcraft.jsch.DHG15256
cons public init()
supr com.jcraft.jsch.DHG15N

CLSS public com.jcraft.jsch.DHG15384
cons public init()
supr com.jcraft.jsch.DHG15N

CLSS public abstract com.jcraft.jsch.DHG15N
cons public init()
supr com.jcraft.jsch.DHGN
hfds g,p

CLSS public com.jcraft.jsch.DHG16
cons public init()
supr com.jcraft.jsch.DHG16N

CLSS public com.jcraft.jsch.DHG16384
cons public init()
supr com.jcraft.jsch.DHG16N

CLSS public abstract com.jcraft.jsch.DHG16N
cons public init()
supr com.jcraft.jsch.DHGN
hfds g,p

CLSS public com.jcraft.jsch.DHG17
cons public init()
supr com.jcraft.jsch.DHGN
hfds g,p

CLSS public com.jcraft.jsch.DHG18
cons public init()
supr com.jcraft.jsch.DHGN
hfds g,p

CLSS public com.jcraft.jsch.DHGEX
cons public init()
fld protected java.lang.String hash
meth public boolean next(com.jcraft.jsch.Buffer) throws java.lang.Exception
meth public int getState()
meth public void init(com.jcraft.jsch.Session,byte[],byte[],byte[],byte[]) throws java.lang.Exception
supr com.jcraft.jsch.KeyExchange
hfds I_C,I_S,SSH_MSG_KEX_DH_GEX_GROUP,SSH_MSG_KEX_DH_GEX_INIT,SSH_MSG_KEX_DH_GEX_REPLY,SSH_MSG_KEX_DH_GEX_REQUEST,V_C,V_S,buf,dh,e,g,max,min,p,packet,preferred,state

CLSS public com.jcraft.jsch.DHGEX224
supr com.jcraft.jsch.DHGEX

CLSS public com.jcraft.jsch.DHGEX256
supr com.jcraft.jsch.DHGEX

CLSS public com.jcraft.jsch.DHGEX384
supr com.jcraft.jsch.DHGEX

CLSS public com.jcraft.jsch.DHGEX512
supr com.jcraft.jsch.DHGEX

CLSS public abstract com.jcraft.jsch.DHGN
cons public init()
meth public boolean next(com.jcraft.jsch.Buffer) throws java.lang.Exception
meth public int getState()
meth public void init(com.jcraft.jsch.Session,byte[],byte[],byte[],byte[]) throws java.lang.Exception
supr com.jcraft.jsch.KeyExchange
hfds I_C,I_S,SSH_MSG_KEXDH_INIT,SSH_MSG_KEXDH_REPLY,V_C,V_S,buf,dh,e,packet,state

CLSS public abstract com.jcraft.jsch.DHXEC
cons public init()
fld protected int key_len
fld protected java.lang.String curve_name
fld protected java.lang.String sha_name
meth public boolean next(com.jcraft.jsch.Buffer) throws java.lang.Exception
meth public int getState()
meth public void init(com.jcraft.jsch.Session,byte[],byte[],byte[],byte[]) throws java.lang.Exception
supr com.jcraft.jsch.KeyExchange
hfds I_C,I_S,Q_C,SSH_MSG_KEX_ECDH_INIT,SSH_MSG_KEX_ECDH_REPLY,V_C,V_S,buf,e,packet,state,xdh

CLSS public abstract interface com.jcraft.jsch.ECDH
meth public abstract boolean validate(byte[],byte[]) throws java.lang.Exception
meth public abstract byte[] getQ() throws java.lang.Exception
meth public abstract byte[] getSecret(byte[],byte[]) throws java.lang.Exception
meth public abstract void init(int) throws java.lang.Exception

CLSS public abstract interface com.jcraft.jsch.ForwardedTCPIPDaemon
intf java.lang.Runnable
meth public abstract void setArg(java.lang.Object[])
meth public abstract void setChannel(com.jcraft.jsch.ChannelForwardedTCPIP,java.io.InputStream,java.io.OutputStream)

CLSS public abstract interface com.jcraft.jsch.GSSContext
meth public abstract boolean isEstablished()
meth public abstract byte[] getMIC(byte[],int,int)
meth public abstract byte[] init(byte[],int,int) throws com.jcraft.jsch.JSchException
meth public abstract void create(java.lang.String,java.lang.String) throws com.jcraft.jsch.JSchException
meth public abstract void dispose()

CLSS public abstract interface com.jcraft.jsch.HASH
meth public abstract byte[] digest() throws java.lang.Exception
meth public abstract int getBlockSize()
meth public abstract java.lang.String name()
meth public abstract void init() throws java.lang.Exception
meth public abstract void update(byte[],int,int) throws java.lang.Exception

CLSS public com.jcraft.jsch.HostKey
cons public init(java.lang.String,byte[]) throws com.jcraft.jsch.JSchException
cons public init(java.lang.String,int,byte[]) throws com.jcraft.jsch.JSchException
cons public init(java.lang.String,int,byte[],java.lang.String) throws com.jcraft.jsch.JSchException
cons public init(java.lang.String,java.lang.String,int,byte[],java.lang.String) throws com.jcraft.jsch.JSchException
fld protected byte[] key
fld protected int type
fld protected java.lang.String comment
fld protected java.lang.String host
fld protected java.lang.String marker
fld public final static int ECDSA256 = 3
fld public final static int ECDSA384 = 4
fld public final static int ECDSA521 = 5
fld public final static int ED25519 = 6
fld public final static int ED448 = 7
fld public final static int GUESS = 0
fld public final static int SSHDSS = 1
fld public final static int SSHRSA = 2
fld public final static int UNKNOWN = -1
meth protected static int name2type(java.lang.String)
meth public java.lang.String getComment()
meth public java.lang.String getFingerPrint(com.jcraft.jsch.JSch)
meth public java.lang.String getHost()
meth public java.lang.String getKey()
meth public java.lang.String getMarker()
meth public java.lang.String getType()
supr java.lang.Object
hfds names

CLSS public abstract interface com.jcraft.jsch.HostKeyRepository
fld public final static int CHANGED = 2
fld public final static int NOT_INCLUDED = 1
fld public final static int OK = 0
meth public abstract com.jcraft.jsch.HostKey[] getHostKey()
meth public abstract com.jcraft.jsch.HostKey[] getHostKey(java.lang.String,java.lang.String)
meth public abstract int check(java.lang.String,byte[])
meth public abstract java.lang.String getKnownHostsRepositoryID()
meth public abstract void add(com.jcraft.jsch.HostKey,com.jcraft.jsch.UserInfo)
meth public abstract void remove(java.lang.String,java.lang.String)
meth public abstract void remove(java.lang.String,java.lang.String,byte[])

CLSS public com.jcraft.jsch.IO
cons public init()
meth public void close()
meth public void put(com.jcraft.jsch.Packet) throws java.io.IOException
supr java.lang.Object
hfds in,in_dontclose,out,out_dontclose,out_ext,out_ext_dontclose

CLSS public abstract interface com.jcraft.jsch.Identity
meth public abstract boolean decrypt()
 anno 0 java.lang.Deprecated()
meth public abstract boolean isEncrypted()
meth public abstract boolean setPassphrase(byte[]) throws com.jcraft.jsch.JSchException
meth public abstract byte[] getPublicKeyBlob()
meth public abstract byte[] getSignature(byte[])
meth public abstract byte[] getSignature(byte[],java.lang.String)
meth public abstract java.lang.String getAlgName()
meth public abstract java.lang.String getName()
meth public abstract void clear()

CLSS public abstract interface com.jcraft.jsch.IdentityRepository
fld public final static int NOTRUNNING = 1
fld public final static int RUNNING = 2
fld public final static int UNAVAILABLE = 0
innr public static Wrapper
meth public abstract boolean add(byte[])
meth public abstract boolean remove(byte[])
meth public abstract int getStatus()
meth public abstract java.lang.String getName()
meth public abstract java.util.Vector<com.jcraft.jsch.Identity> getIdentities()
meth public abstract void removeAll()

CLSS public static com.jcraft.jsch.IdentityRepository$Wrapper
 outer com.jcraft.jsch.IdentityRepository
intf com.jcraft.jsch.IdentityRepository
meth public boolean add(byte[])
meth public boolean remove(byte[])
meth public int getStatus()
meth public java.lang.String getName()
meth public java.util.Vector<com.jcraft.jsch.Identity> getIdentities()
meth public void removeAll()
supr java.lang.Object
hfds cache,ir,keep_in_cache

CLSS public com.jcraft.jsch.JSch
cons public init()
fld public final static java.lang.String VERSION
meth protected boolean removeSession(com.jcraft.jsch.Session)
meth protected void addSession(com.jcraft.jsch.Session)
meth public com.jcraft.jsch.ConfigRepository getConfigRepository()
meth public com.jcraft.jsch.HostKeyRepository getHostKeyRepository()
meth public com.jcraft.jsch.IdentityRepository getIdentityRepository()
meth public com.jcraft.jsch.Session getSession(java.lang.String) throws com.jcraft.jsch.JSchException
meth public com.jcraft.jsch.Session getSession(java.lang.String,java.lang.String) throws com.jcraft.jsch.JSchException
meth public com.jcraft.jsch.Session getSession(java.lang.String,java.lang.String,int) throws com.jcraft.jsch.JSchException
meth public java.util.Vector<java.lang.String> getIdentityNames() throws com.jcraft.jsch.JSchException
meth public static java.lang.String getConfig(java.lang.String)
meth public static void setConfig(java.lang.String,java.lang.String)
meth public static void setConfig(java.util.Hashtable<java.lang.String,java.lang.String>)
meth public static void setLogger(com.jcraft.jsch.Logger)
meth public void addIdentity(com.jcraft.jsch.Identity,byte[]) throws com.jcraft.jsch.JSchException
meth public void addIdentity(java.lang.String) throws com.jcraft.jsch.JSchException
meth public void addIdentity(java.lang.String,byte[]) throws com.jcraft.jsch.JSchException
meth public void addIdentity(java.lang.String,byte[],byte[],byte[]) throws com.jcraft.jsch.JSchException
meth public void addIdentity(java.lang.String,java.lang.String) throws com.jcraft.jsch.JSchException
meth public void addIdentity(java.lang.String,java.lang.String,byte[]) throws com.jcraft.jsch.JSchException
meth public void removeAllIdentity() throws com.jcraft.jsch.JSchException
meth public void removeIdentity(com.jcraft.jsch.Identity) throws com.jcraft.jsch.JSchException
meth public void removeIdentity(java.lang.String) throws com.jcraft.jsch.JSchException
 anno 0 java.lang.Deprecated()
meth public void setConfigRepository(com.jcraft.jsch.ConfigRepository)
meth public void setHostKeyRepository(com.jcraft.jsch.HostKeyRepository)
meth public void setIdentityRepository(com.jcraft.jsch.IdentityRepository)
meth public void setKnownHosts(java.io.InputStream) throws com.jcraft.jsch.JSchException
meth public void setKnownHosts(java.lang.String) throws com.jcraft.jsch.JSchException
supr java.lang.Object
hfds DEVNULL,config,configRepository,defaultIdentityRepository,identityRepository,known_hosts,logger,sessionPool

CLSS public com.jcraft.jsch.JSchException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

CLSS public com.jcraft.jsch.JUnixSocketFactory
cons public init() throws com.jcraft.jsch.AgentProxyException
intf com.jcraft.jsch.USocketFactory
meth public java.nio.channels.ServerSocketChannel bind(java.nio.file.Path) throws java.io.IOException
meth public java.nio.channels.SocketChannel connect(java.nio.file.Path) throws java.io.IOException
supr java.lang.Object

CLSS public abstract com.jcraft.jsch.KeyExchange
cons public init()
fld protected byte[] H
fld protected byte[] K
fld protected byte[] K_S
fld protected com.jcraft.jsch.HASH sha
fld protected com.jcraft.jsch.Session session
fld protected final int DSS = 1
fld protected final int ECDSA = 2
fld protected final int EDDSA = 3
fld protected final int RSA = 0
fld public final static int STATE_END = 0
meth protected boolean verify(java.lang.String,byte[],int,byte[]) throws java.lang.Exception
meth protected byte[] normalize(byte[])
meth protected static java.lang.String[] guess(com.jcraft.jsch.Session,byte[],byte[]) throws java.lang.Exception
meth public abstract boolean next(com.jcraft.jsch.Buffer) throws java.lang.Exception
meth public abstract int getState()
meth public abstract void init(com.jcraft.jsch.Session,byte[],byte[],byte[],byte[]) throws java.lang.Exception
meth public java.lang.String getFingerPrint()
meth public java.lang.String getKeyAlgorithName()
meth public java.lang.String getKeyType()
supr java.lang.Object
hfds PROPOSAL_COMP_ALGS_CTOS,PROPOSAL_COMP_ALGS_STOC,PROPOSAL_ENC_ALGS_CTOS,PROPOSAL_ENC_ALGS_STOC,PROPOSAL_KEX_ALGS,PROPOSAL_LANG_CTOS,PROPOSAL_LANG_STOC,PROPOSAL_MAC_ALGS_CTOS,PROPOSAL_MAC_ALGS_STOC,PROPOSAL_MAX,PROPOSAL_SERVER_HOST_KEY_ALGS,enc_c2s,enc_s2c,kex,key_alg_name,lang_c2s,lang_s2c,mac_c2s,mac_s2c,server_host_key,type

CLSS public abstract com.jcraft.jsch.KeyPair
cons public init(com.jcraft.jsch.JSch)
fld protected boolean encrypted
fld protected byte[] data
fld protected byte[] kdfOptions
fld protected com.jcraft.jsch.Cipher cipher
fld protected java.lang.String kdfName
fld protected java.lang.String publicKeyComment
fld public final static int DEFERRED = -1
fld public final static int DSA = 1
fld public final static int ECDSA = 3
fld public final static int ED25519 = 5
fld public final static int ED448 = 6
fld public final static int ERROR = 0
fld public final static int RSA = 2
fld public final static int UNKNOWN = 4
meth public abstract byte[] forSSHAgent() throws com.jcraft.jsch.JSchException
meth public abstract byte[] getSignature(byte[])
meth public abstract byte[] getSignature(byte[],java.lang.String)
meth public abstract com.jcraft.jsch.Signature getVerifier()
meth public abstract com.jcraft.jsch.Signature getVerifier(java.lang.String)
meth public abstract int getKeyType()
meth public boolean decrypt(byte[])
meth public boolean decrypt(java.lang.String)
meth public boolean isEncrypted()
meth public byte[] getPublicKeyBlob()
meth public java.lang.String getFingerPrint()
meth public java.lang.String getPublicKeyComment()
meth public static com.jcraft.jsch.KeyPair genKeyPair(com.jcraft.jsch.JSch,int) throws com.jcraft.jsch.JSchException
meth public static com.jcraft.jsch.KeyPair genKeyPair(com.jcraft.jsch.JSch,int,int) throws com.jcraft.jsch.JSchException
meth public static com.jcraft.jsch.KeyPair load(com.jcraft.jsch.JSch,byte[],byte[]) throws com.jcraft.jsch.JSchException
meth public static com.jcraft.jsch.KeyPair load(com.jcraft.jsch.JSch,java.lang.String) throws com.jcraft.jsch.JSchException
meth public static com.jcraft.jsch.KeyPair load(com.jcraft.jsch.JSch,java.lang.String,java.lang.String) throws com.jcraft.jsch.JSchException
meth public void dispose()
meth public void finalize()
meth public void setPassphrase(byte[])
 anno 0 java.lang.Deprecated()
meth public void setPassphrase(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setPublicKeyComment(java.lang.String)
meth public void writePrivateKey(java.io.OutputStream)
meth public void writePrivateKey(java.io.OutputStream,byte[])
meth public void writePrivateKey(java.lang.String) throws java.io.IOException
meth public void writePrivateKey(java.lang.String,byte[]) throws java.io.IOException
meth public void writePublicKey(java.io.OutputStream,java.lang.String)
meth public void writePublicKey(java.lang.String,java.lang.String) throws java.io.IOException
meth public void writeSECSHPublicKey(java.io.OutputStream,java.lang.String)
meth public void writeSECSHPublicKey(java.lang.String,java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds AUTH_MAGIC,VENDOR_FSECURE,VENDOR_OPENSSH,VENDOR_OPENSSH_V1,VENDOR_PKCS8,VENDOR_PUTTY,cr,hash,header,header1,header2,header3,iv,jsch,passphrase,publickeyblob,random,space,vendor
hcls ASN1,ASN1Exception

CLSS public com.jcraft.jsch.KeyPairDSA
cons public init(com.jcraft.jsch.JSch)
cons public init(com.jcraft.jsch.JSch,byte[],byte[],byte[],byte[],byte[])
meth public byte[] forSSHAgent() throws com.jcraft.jsch.JSchException
meth public byte[] getPublicKeyBlob()
meth public byte[] getSignature(byte[])
meth public byte[] getSignature(byte[],java.lang.String)
meth public com.jcraft.jsch.Signature getVerifier()
meth public com.jcraft.jsch.Signature getVerifier(java.lang.String)
meth public int getKeySize()
meth public int getKeyType()
meth public void dispose()
supr com.jcraft.jsch.KeyPair
hfds G_array,P_array,Q_array,begin,end,key_size,prv_array,pub_array,sshdss

CLSS public com.jcraft.jsch.KeyPairDeferred
meth public boolean decrypt(byte[])
meth public boolean decrypt(java.lang.String)
meth public boolean isEncrypted()
meth public byte[] forSSHAgent() throws com.jcraft.jsch.JSchException
meth public byte[] getPublicKeyBlob()
meth public byte[] getSignature(byte[])
meth public byte[] getSignature(byte[],java.lang.String)
meth public com.jcraft.jsch.Signature getVerifier()
meth public com.jcraft.jsch.Signature getVerifier(java.lang.String)
meth public int getKeyType()
meth public java.lang.String getFingerPrint()
meth public java.lang.String getPublicKeyComment()
supr com.jcraft.jsch.KeyPair
hfds delegate

CLSS public com.jcraft.jsch.KeyPairECDSA
cons public init(com.jcraft.jsch.JSch)
cons public init(com.jcraft.jsch.JSch,byte[])
cons public init(com.jcraft.jsch.JSch,byte[],byte[],byte[],byte[])
meth public byte[] forSSHAgent() throws com.jcraft.jsch.JSchException
meth public byte[] getPublicKeyBlob()
meth public byte[] getSignature(byte[])
meth public byte[] getSignature(byte[],java.lang.String)
meth public com.jcraft.jsch.Signature getVerifier()
meth public com.jcraft.jsch.Signature getVerifier(java.lang.String)
meth public int getKeySize()
meth public int getKeyType()
meth public void dispose()
supr com.jcraft.jsch.KeyPair
hfds begin,end,key_size,name,names,oids,prv_array,r_array,s_array

CLSS public com.jcraft.jsch.KeyPairEd25519
cons public init(com.jcraft.jsch.JSch)
cons public init(com.jcraft.jsch.JSch,byte[],byte[])
meth public int getKeySize()
meth public int getKeyType()
supr com.jcraft.jsch.KeyPairEdDSA
hfds keySize

CLSS public com.jcraft.jsch.KeyPairEd448
cons public init(com.jcraft.jsch.JSch)
cons public init(com.jcraft.jsch.JSch,byte[],byte[])
meth public int getKeySize()
meth public int getKeyType()
supr com.jcraft.jsch.KeyPairEdDSA
hfds keySize

CLSS public abstract com.jcraft.jsch.KeyPairEdDSA
cons public init(com.jcraft.jsch.JSch,byte[],byte[])
meth public byte[] forSSHAgent() throws com.jcraft.jsch.JSchException
meth public byte[] getPublicKeyBlob()
meth public byte[] getSignature(byte[])
meth public byte[] getSignature(byte[],java.lang.String)
meth public com.jcraft.jsch.Signature getVerifier()
meth public com.jcraft.jsch.Signature getVerifier(java.lang.String)
meth public void dispose()
supr com.jcraft.jsch.KeyPair
hfds prv_array,pub_array

CLSS public abstract interface com.jcraft.jsch.KeyPairGenDSA
meth public abstract byte[] getG()
meth public abstract byte[] getP()
meth public abstract byte[] getQ()
meth public abstract byte[] getX()
meth public abstract byte[] getY()
meth public abstract void init(int) throws java.lang.Exception

CLSS public abstract interface com.jcraft.jsch.KeyPairGenECDSA
meth public abstract byte[] getD()
meth public abstract byte[] getR()
meth public abstract byte[] getS()
meth public abstract void init(int) throws java.lang.Exception

CLSS public abstract interface com.jcraft.jsch.KeyPairGenEdDSA
meth public abstract byte[] getPrv()
meth public abstract byte[] getPub()
meth public abstract void init(java.lang.String,int) throws java.lang.Exception

CLSS public abstract interface com.jcraft.jsch.KeyPairGenRSA
meth public abstract byte[] getC()
meth public abstract byte[] getD()
meth public abstract byte[] getE()
meth public abstract byte[] getEP()
meth public abstract byte[] getEQ()
meth public abstract byte[] getN()
meth public abstract byte[] getP()
meth public abstract byte[] getQ()
meth public abstract void init(int) throws java.lang.Exception

CLSS public abstract interface com.jcraft.jsch.KeyPairGenXEC
meth public abstract void init(java.lang.String) throws java.lang.Exception

CLSS public com.jcraft.jsch.KeyPairPKCS8
cons public init(com.jcraft.jsch.JSch)
meth public boolean decrypt(byte[])
meth public byte[] forSSHAgent() throws com.jcraft.jsch.JSchException
meth public byte[] getPublicKeyBlob()
meth public byte[] getSignature(byte[])
meth public byte[] getSignature(byte[],java.lang.String)
meth public com.jcraft.jsch.Signature getVerifier()
meth public com.jcraft.jsch.Signature getVerifier(java.lang.String)
meth public int getKeySize()
meth public int getKeyType()
supr com.jcraft.jsch.KeyPair
hfds aes128cbc,aes192cbc,aes256cbc,begin,dsaEncryption,end,kpair,pbeWithMD5AndDESCBC,pbes2,pbkdf2,rsaEncryption

CLSS public com.jcraft.jsch.KeyPairRSA
cons public init(com.jcraft.jsch.JSch)
cons public init(com.jcraft.jsch.JSch,byte[],byte[],byte[])
meth public byte[] forSSHAgent() throws com.jcraft.jsch.JSchException
meth public byte[] getPublicKeyBlob()
meth public byte[] getSignature(byte[])
meth public byte[] getSignature(byte[],java.lang.String)
meth public com.jcraft.jsch.Signature getVerifier()
meth public com.jcraft.jsch.Signature getVerifier(java.lang.String)
meth public int getKeySize()
meth public int getKeyType()
meth public void dispose()
supr com.jcraft.jsch.KeyPair
hfds begin,c_array,end,ep_array,eq_array,key_size,n_array,p_array,prv_array,pub_array,q_array,sshrsa

CLSS public com.jcraft.jsch.KnownHosts
intf com.jcraft.jsch.HostKeyRepository
meth protected void sync() throws java.io.IOException
meth protected void sync(java.lang.String) throws java.io.IOException
meth public com.jcraft.jsch.HostKey[] getHostKey()
meth public com.jcraft.jsch.HostKey[] getHostKey(java.lang.String,java.lang.String)
meth public int check(java.lang.String,byte[])
meth public java.lang.String getKnownHostsRepositoryID()
meth public void add(com.jcraft.jsch.HostKey,com.jcraft.jsch.UserInfo)
meth public void remove(java.lang.String,java.lang.String)
meth public void remove(java.lang.String,java.lang.String,byte[])
supr java.lang.Object
hfds _known_hosts,cr,hmacsha1,jsch,known_hosts,pool,space
hcls HashedHostKey

CLSS public abstract interface com.jcraft.jsch.Logger
fld public final static int DEBUG = 0
fld public final static int ERROR = 3
fld public final static int FATAL = 4
fld public final static int INFO = 1
fld public final static int WARN = 2
meth public abstract boolean isEnabled(int)
meth public abstract void log(int,java.lang.String)

CLSS public abstract interface com.jcraft.jsch.MAC
meth public abstract boolean isEtM()
meth public abstract int getBlockSize()
meth public abstract java.lang.String getName()
meth public abstract void doFinal(byte[],int)
meth public abstract void init(byte[]) throws java.lang.Exception
meth public abstract void update(byte[],int,int)
meth public abstract void update(int)

CLSS public com.jcraft.jsch.OpenSSHConfig
intf com.jcraft.jsch.ConfigRepository
meth public com.jcraft.jsch.ConfigRepository$Config getConfig(java.lang.String)
meth public static com.jcraft.jsch.OpenSSHConfig parse(java.lang.String) throws java.io.IOException
meth public static com.jcraft.jsch.OpenSSHConfig parseFile(java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds config,hosts,keymap,keysWithListAdoption
hcls MyConfig

CLSS public abstract interface com.jcraft.jsch.PBKDF
meth public abstract byte[] getKey(byte[],byte[],int,int)

CLSS public com.jcraft.jsch.Packet
cons public init(com.jcraft.jsch.Buffer)
meth public void reset()
supr java.lang.Object
hfds ba4,buffer,random

CLSS public com.jcraft.jsch.PageantConnector
cons public init() throws com.jcraft.jsch.AgentProxyException
innr public static COPYDATASTRUCT32
innr public static COPYDATASTRUCT64
intf com.jcraft.jsch.AgentConnector
meth public boolean isAvailable()
meth public java.lang.String getName()
meth public void query(com.jcraft.jsch.Buffer) throws com.jcraft.jsch.AgentProxyException
supr java.lang.Object
hfds AGENT_COPYDATA_ID,AGENT_MAX_MSGLEN,libK,libU
hcls User32

CLSS public static com.jcraft.jsch.PageantConnector$COPYDATASTRUCT32
 outer com.jcraft.jsch.PageantConnector
cons public init()
fld public com.sun.jna.Pointer lpData
fld public int cbData
fld public int dwData
meth protected java.util.List<java.lang.String> getFieldOrder()
supr com.sun.jna.Structure

CLSS public static com.jcraft.jsch.PageantConnector$COPYDATASTRUCT64
 outer com.jcraft.jsch.PageantConnector
cons public init()
fld public com.sun.jna.Pointer lpData
fld public int dwData
fld public long cbData
meth protected java.util.List<java.lang.String> getFieldOrder()
supr com.sun.jna.Structure

CLSS public abstract interface com.jcraft.jsch.Proxy
meth public abstract java.io.InputStream getInputStream()
meth public abstract java.io.OutputStream getOutputStream()
meth public abstract java.net.Socket getSocket()
meth public abstract void close()
meth public abstract void connect(com.jcraft.jsch.SocketFactory,java.lang.String,int,int) throws java.lang.Exception

CLSS public com.jcraft.jsch.ProxyHTTP
cons public init(java.lang.String)
cons public init(java.lang.String,int)
intf com.jcraft.jsch.Proxy
meth public java.io.InputStream getInputStream()
meth public java.io.OutputStream getOutputStream()
meth public java.net.Socket getSocket()
meth public static int getDefaultPort()
meth public void close()
meth public void connect(com.jcraft.jsch.SocketFactory,java.lang.String,int,int) throws com.jcraft.jsch.JSchException
meth public void setUserPasswd(java.lang.String,java.lang.String)
supr java.lang.Object
hfds DEFAULTPORT,in,out,passwd,proxy_host,proxy_port,socket,user

CLSS public com.jcraft.jsch.ProxySOCKS4
cons public init(java.lang.String)
cons public init(java.lang.String,int)
intf com.jcraft.jsch.Proxy
meth public java.io.InputStream getInputStream()
meth public java.io.OutputStream getOutputStream()
meth public java.net.Socket getSocket()
meth public static int getDefaultPort()
meth public void close()
meth public void connect(com.jcraft.jsch.SocketFactory,java.lang.String,int,int) throws com.jcraft.jsch.JSchException
meth public void setUserPasswd(java.lang.String,java.lang.String)
supr java.lang.Object
hfds DEFAULTPORT,in,out,passwd,proxy_host,proxy_port,socket,user

CLSS public com.jcraft.jsch.ProxySOCKS5
cons public init(java.lang.String)
cons public init(java.lang.String,int)
intf com.jcraft.jsch.Proxy
meth public java.io.InputStream getInputStream()
meth public java.io.OutputStream getOutputStream()
meth public java.net.Socket getSocket()
meth public static int getDefaultPort()
meth public void close()
meth public void connect(com.jcraft.jsch.SocketFactory,java.lang.String,int,int) throws com.jcraft.jsch.JSchException
meth public void setUserPasswd(java.lang.String,java.lang.String)
supr java.lang.Object
hfds DEFAULTPORT,in,out,passwd,proxy_host,proxy_port,socket,user

CLSS public abstract interface com.jcraft.jsch.Random
meth public abstract void fill(byte[],int,int)

CLSS public com.jcraft.jsch.SSHAgentConnector
cons public init() throws com.jcraft.jsch.AgentProxyException
cons public init(com.jcraft.jsch.USocketFactory) throws com.jcraft.jsch.AgentProxyException
cons public init(com.jcraft.jsch.USocketFactory,java.nio.file.Path)
cons public init(java.nio.file.Path) throws com.jcraft.jsch.AgentProxyException
intf com.jcraft.jsch.AgentConnector
meth public boolean isAvailable()
meth public java.lang.String getName()
meth public void query(com.jcraft.jsch.Buffer) throws com.jcraft.jsch.AgentProxyException
supr java.lang.Object
hfds MAX_AGENT_REPLY_LEN,factory,usocketPath

CLSS public abstract interface com.jcraft.jsch.ServerSocketFactory
meth public abstract java.net.ServerSocket createServerSocket(int,int,java.net.InetAddress) throws java.io.IOException

CLSS public com.jcraft.jsch.Session
fld protected boolean daemon_thread
intf java.lang.Runnable
meth public boolean isConnected()
meth public com.jcraft.jsch.Buffer read(com.jcraft.jsch.Buffer) throws java.lang.Exception
meth public com.jcraft.jsch.Channel getStreamForwarder(java.lang.String,int) throws com.jcraft.jsch.JSchException
meth public com.jcraft.jsch.Channel openChannel(java.lang.String) throws com.jcraft.jsch.JSchException
meth public com.jcraft.jsch.HostKey getHostKey()
meth public com.jcraft.jsch.HostKeyRepository getHostKeyRepository()
meth public com.jcraft.jsch.UserInfo getUserInfo()
meth public int getPort()
meth public int getServerAliveCountMax()
meth public int getServerAliveInterval()
meth public int getTimeout()
meth public int setPortForwardingL(int,java.lang.String,int) throws com.jcraft.jsch.JSchException
meth public int setPortForwardingL(java.lang.String) throws com.jcraft.jsch.JSchException
meth public int setPortForwardingL(java.lang.String,int,java.lang.String,int) throws com.jcraft.jsch.JSchException
meth public int setPortForwardingL(java.lang.String,int,java.lang.String,int,com.jcraft.jsch.ServerSocketFactory) throws com.jcraft.jsch.JSchException
meth public int setPortForwardingL(java.lang.String,int,java.lang.String,int,com.jcraft.jsch.ServerSocketFactory,int) throws com.jcraft.jsch.JSchException
meth public int setPortForwardingR(java.lang.String) throws com.jcraft.jsch.JSchException
meth public int setSocketForwardingL(java.lang.String,int,java.lang.String,com.jcraft.jsch.ServerSocketFactory,int) throws com.jcraft.jsch.JSchException
meth public java.lang.String getClientVersion()
meth public java.lang.String getConfig(java.lang.String)
meth public java.lang.String getHost()
meth public java.lang.String getHostKeyAlias()
meth public java.lang.String getServerVersion()
meth public java.lang.String getUserName()
meth public java.lang.String[] getPortForwardingL() throws com.jcraft.jsch.JSchException
meth public java.lang.String[] getPortForwardingR() throws com.jcraft.jsch.JSchException
meth public java.lang.String[] getUnavailableSignatures()
meth public void connect() throws com.jcraft.jsch.JSchException
meth public void connect(int) throws com.jcraft.jsch.JSchException
meth public void delPortForwardingL(int) throws com.jcraft.jsch.JSchException
meth public void delPortForwardingL(java.lang.String,int) throws com.jcraft.jsch.JSchException
meth public void delPortForwardingR(int) throws com.jcraft.jsch.JSchException
meth public void delPortForwardingR(java.lang.String,int) throws com.jcraft.jsch.JSchException
meth public void disconnect()
meth public void encode(com.jcraft.jsch.Packet) throws java.lang.Exception
meth public void noMoreSessionChannels() throws java.lang.Exception
meth public void rekey() throws java.lang.Exception
meth public void run()
meth public void sendIgnore() throws java.lang.Exception
meth public void sendKeepAliveMsg() throws java.lang.Exception
meth public void setClientVersion(java.lang.String)
meth public void setConfig(java.lang.String,java.lang.String)
meth public void setConfig(java.util.Hashtable<java.lang.String,java.lang.String>)
meth public void setConfig(java.util.Properties)
meth public void setDaemonThread(boolean)
meth public void setHost(java.lang.String)
meth public void setHostKeyAlias(java.lang.String)
meth public void setHostKeyRepository(com.jcraft.jsch.HostKeyRepository)
meth public void setIdentityRepository(com.jcraft.jsch.IdentityRepository)
meth public void setInputStream(java.io.InputStream)
meth public void setOutputStream(java.io.OutputStream)
meth public void setPassword(byte[])
meth public void setPassword(java.lang.String)
meth public void setPort(int)
meth public void setPortForwardingR(int,java.lang.String) throws com.jcraft.jsch.JSchException
meth public void setPortForwardingR(int,java.lang.String,int) throws com.jcraft.jsch.JSchException
meth public void setPortForwardingR(int,java.lang.String,int,com.jcraft.jsch.SocketFactory) throws com.jcraft.jsch.JSchException
meth public void setPortForwardingR(int,java.lang.String,java.lang.Object[]) throws com.jcraft.jsch.JSchException
meth public void setPortForwardingR(java.lang.String,int,java.lang.String,int) throws com.jcraft.jsch.JSchException
meth public void setPortForwardingR(java.lang.String,int,java.lang.String,int,com.jcraft.jsch.SocketFactory) throws com.jcraft.jsch.JSchException
meth public void setPortForwardingR(java.lang.String,int,java.lang.String,java.lang.Object[]) throws com.jcraft.jsch.JSchException
meth public void setProxy(com.jcraft.jsch.Proxy)
meth public void setServerAliveCountMax(int)
meth public void setServerAliveInterval(int) throws com.jcraft.jsch.JSchException
meth public void setSocketFactory(com.jcraft.jsch.SocketFactory)
meth public void setTimeout(int) throws com.jcraft.jsch.JSchException
meth public void setUserInfo(com.jcraft.jsch.UserInfo)
meth public void setX11Cookie(java.lang.String)
meth public void setX11Host(java.lang.String)
meth public void setX11Port(int)
meth public void write(com.jcraft.jsch.Packet) throws java.lang.Exception
supr java.lang.Object
hfds Ec2s,Es2c,IVc2s,IVs2c,I_C,I_S,K_S,MACc2s,MACs2c,PACKET_MAX_SIZE,SSH_MSG_CHANNEL_CLOSE,SSH_MSG_CHANNEL_DATA,SSH_MSG_CHANNEL_EOF,SSH_MSG_CHANNEL_EXTENDED_DATA,SSH_MSG_CHANNEL_FAILURE,SSH_MSG_CHANNEL_OPEN,SSH_MSG_CHANNEL_OPEN_CONFIRMATION,SSH_MSG_CHANNEL_OPEN_FAILURE,SSH_MSG_CHANNEL_REQUEST,SSH_MSG_CHANNEL_SUCCESS,SSH_MSG_CHANNEL_WINDOW_ADJUST,SSH_MSG_DEBUG,SSH_MSG_DISCONNECT,SSH_MSG_EXT_INFO,SSH_MSG_GLOBAL_REQUEST,SSH_MSG_IGNORE,SSH_MSG_KEXDH_INIT,SSH_MSG_KEXDH_REPLY,SSH_MSG_KEXINIT,SSH_MSG_KEX_DH_GEX_GROUP,SSH_MSG_KEX_DH_GEX_INIT,SSH_MSG_KEX_DH_GEX_REPLY,SSH_MSG_KEX_DH_GEX_REQUEST,SSH_MSG_NEWKEYS,SSH_MSG_REQUEST_FAILURE,SSH_MSG_REQUEST_SUCCESS,SSH_MSG_SERVICE_ACCEPT,SSH_MSG_SERVICE_REQUEST,SSH_MSG_UNIMPLEMENTED,V_C,V_S,agent_forwarding,auth_failures,buf,buffer_margin,c2scipher,c2scipher_size,c2smac,compress_len,config,connectThread,deflater,grr,guess,host,hostKeyAlias,hostkey,hostkeyRepository,identityRepository,in,in_kex,in_prompt,inflater,io,isAuthed,isConnected,jsch,keepalivemsg,kex_start_time,lock,max_auth_tries,nomoresessions,not_available_shks,org_host,out,packet,password,port,proxy,random,s2ccipher,s2ccipher_size,s2cmac,s2cmac_result1,s2cmac_result2,seqi,seqo,serverAliveCountMax,serverAliveInterval,serverSigAlgs,session_id,socket,socket_factory,sshBugSigType74,thread,timeout,uncompress_len,userinfo,username,x11_forwarding
hcls Forwarding,GlobalRequestReply

CLSS public com.jcraft.jsch.SftpATTRS
fld public final static int SSH_FILEXFER_ATTR_ACMODTIME = 8
fld public final static int SSH_FILEXFER_ATTR_EXTENDED = -2147483648
fld public final static int SSH_FILEXFER_ATTR_PERMISSIONS = 4
fld public final static int SSH_FILEXFER_ATTR_SIZE = 1
fld public final static int SSH_FILEXFER_ATTR_UIDGID = 2
meth public boolean isBlk()
meth public boolean isChr()
meth public boolean isDir()
meth public boolean isFifo()
meth public boolean isLink()
meth public boolean isReg()
meth public boolean isSock()
meth public int getATime()
meth public int getFlags()
meth public int getGId()
meth public int getMTime()
meth public int getPermissions()
meth public int getUId()
meth public java.lang.String getAtimeString()
meth public java.lang.String getMtimeString()
meth public java.lang.String getPermissionsString()
meth public java.lang.String toString()
meth public java.lang.String[] getExtended()
meth public long getSize()
meth public void setACMODTIME(int,int)
meth public void setPERMISSIONS(int)
meth public void setSIZE(long)
meth public void setUIDGID(int,int)
supr java.lang.Object
hfds S_IEXEC,S_IFBLK,S_IFCHR,S_IFDIR,S_IFIFO,S_IFLNK,S_IFMT,S_IFREG,S_IFSOCK,S_IREAD,S_IRGRP,S_IROTH,S_IRUSR,S_ISGID,S_ISUID,S_ISVTX,S_IWGRP,S_IWOTH,S_IWRITE,S_IWUSR,S_IXGRP,S_IXOTH,S_IXUSR,atime,extended,flags,gid,mtime,permissions,pmask,size,uid

CLSS public com.jcraft.jsch.SftpException
cons public init(int,java.lang.String)
cons public init(int,java.lang.String,java.lang.Throwable)
fld public int id
meth public java.lang.String toString()
supr java.lang.Exception
hfds serialVersionUID

CLSS public abstract interface com.jcraft.jsch.SftpProgressMonitor
fld public final static int GET = 1
fld public final static int PUT = 0
fld public final static long UNKNOWN_SIZE = -1
meth public abstract boolean count(long)
meth public abstract void end()
meth public abstract void init(int,java.lang.String,java.lang.String,long)

CLSS public com.jcraft.jsch.SftpStatVFS
meth public int getCapacity()
meth public long getAvail()
meth public long getAvailBlocks()
meth public long getAvailForNonRoot()
meth public long getAvailINodes()
meth public long getBlockSize()
meth public long getBlocks()
meth public long getFileSystemID()
meth public long getFragmentSize()
meth public long getFreeBlocks()
meth public long getFreeINodes()
meth public long getINodes()
meth public long getMaximumFilenameLength()
meth public long getMountFlag()
meth public long getSize()
meth public long getUsed()
supr java.lang.Object
hfds atime,bavail,bfree,blocks,bsize,extended,favail,ffree,files,flag,flags,frsize,fsid,gid,mtime,namemax,permissions,size,uid

CLSS public abstract interface com.jcraft.jsch.Signature
meth public abstract boolean verify(byte[]) throws java.lang.Exception
meth public abstract byte[] sign() throws java.lang.Exception
meth public abstract void init() throws java.lang.Exception
meth public abstract void update(byte[]) throws java.lang.Exception

CLSS public abstract interface com.jcraft.jsch.SignatureDSA
intf com.jcraft.jsch.Signature
meth public abstract void setPrvKey(byte[],byte[],byte[],byte[]) throws java.lang.Exception
meth public abstract void setPubKey(byte[],byte[],byte[],byte[]) throws java.lang.Exception

CLSS public abstract interface com.jcraft.jsch.SignatureECDSA
intf com.jcraft.jsch.Signature
meth public abstract void setPrvKey(byte[]) throws java.lang.Exception
meth public abstract void setPubKey(byte[],byte[]) throws java.lang.Exception

CLSS public abstract interface com.jcraft.jsch.SignatureEdDSA
intf com.jcraft.jsch.Signature
meth public abstract void setPrvKey(byte[]) throws java.lang.Exception
meth public abstract void setPubKey(byte[]) throws java.lang.Exception

CLSS public abstract interface com.jcraft.jsch.SignatureRSA
intf com.jcraft.jsch.Signature
meth public abstract void setPrvKey(byte[],byte[]) throws java.lang.Exception
meth public abstract void setPubKey(byte[],byte[]) throws java.lang.Exception

CLSS public abstract interface com.jcraft.jsch.SocketFactory
meth public abstract java.io.InputStream getInputStream(java.net.Socket) throws java.io.IOException
meth public abstract java.io.OutputStream getOutputStream(java.net.Socket) throws java.io.IOException
meth public abstract java.net.Socket createSocket(java.lang.String,int) throws java.io.IOException

CLSS public abstract interface com.jcraft.jsch.UIKeyboardInteractive
meth public abstract java.lang.String[] promptKeyboardInteractive(java.lang.String,java.lang.String,java.lang.String,java.lang.String[],boolean[])

CLSS public abstract interface com.jcraft.jsch.USocketFactory
meth public abstract java.nio.channels.ServerSocketChannel bind(java.nio.file.Path) throws java.io.IOException
meth public abstract java.nio.channels.SocketChannel connect(java.nio.file.Path) throws java.io.IOException

CLSS public com.jcraft.jsch.UnixDomainSocketFactory
cons public init() throws com.jcraft.jsch.AgentProxyException
intf com.jcraft.jsch.USocketFactory
meth public java.nio.channels.ServerSocketChannel bind(java.nio.file.Path) throws java.io.IOException
meth public java.nio.channels.SocketChannel connect(java.nio.file.Path) throws java.io.IOException
supr java.lang.Object

CLSS public abstract com.jcraft.jsch.UserAuth
cons public init()
fld protected com.jcraft.jsch.Buffer buf
fld protected com.jcraft.jsch.Packet packet
fld protected com.jcraft.jsch.UserInfo userinfo
fld protected final static int SSH_MSG_USERAUTH_BANNER = 53
fld protected final static int SSH_MSG_USERAUTH_FAILURE = 51
fld protected final static int SSH_MSG_USERAUTH_INFO_REQUEST = 60
fld protected final static int SSH_MSG_USERAUTH_INFO_RESPONSE = 61
fld protected final static int SSH_MSG_USERAUTH_PK_OK = 60
fld protected final static int SSH_MSG_USERAUTH_REQUEST = 50
fld protected final static int SSH_MSG_USERAUTH_SUCCESS = 52
fld protected java.lang.String username
meth public boolean start(com.jcraft.jsch.Session) throws java.lang.Exception
supr java.lang.Object

CLSS public abstract interface com.jcraft.jsch.UserInfo
meth public abstract boolean promptPassphrase(java.lang.String)
meth public abstract boolean promptPassword(java.lang.String)
meth public abstract boolean promptYesNo(java.lang.String)
meth public abstract java.lang.String getPassphrase()
meth public abstract java.lang.String getPassword()
meth public abstract void showMessage(java.lang.String)

CLSS public abstract interface com.jcraft.jsch.XDH
meth public abstract boolean validate(byte[]) throws java.lang.Exception
meth public abstract byte[] getQ() throws java.lang.Exception
meth public abstract byte[] getSecret(byte[]) throws java.lang.Exception
meth public abstract void init(java.lang.String,int) throws java.lang.Exception

CLSS public abstract com.sun.jna.Structure
cons protected init()
cons protected init(com.sun.jna.Pointer)
cons protected init(com.sun.jna.Pointer,int)
cons protected init(com.sun.jna.Pointer,int,com.sun.jna.TypeMapper)
cons protected init(com.sun.jna.TypeMapper)
cons protected init(int)
cons protected init(int,com.sun.jna.TypeMapper)
fld protected final static int CALCULATE_SIZE = -1
fld public final static int ALIGN_DEFAULT = 0
fld public final static int ALIGN_GNUC = 2
fld public final static int ALIGN_MSVC = 3
fld public final static int ALIGN_NONE = 1
innr protected static StructField
innr public abstract interface static !annotation FieldOrder
innr public abstract interface static ByReference
innr public abstract interface static ByValue
meth protected com.sun.jna.Memory autoAllocate(int)
meth protected int calculateSize(boolean)
meth protected int fieldOffset(java.lang.String)
meth protected int getNativeAlignment(java.lang.Class<?>,java.lang.Object,boolean)
meth protected int getNativeSize(java.lang.Class<?>)
meth protected int getNativeSize(java.lang.Class<?>,java.lang.Object)
meth protected int getStructAlignment()
meth protected java.lang.Object readField(com.sun.jna.Structure$StructField)
meth protected java.lang.String getStringEncoding()
meth protected java.util.List<java.lang.String> getFieldOrder()
meth protected java.util.List<java.lang.reflect.Field> getFieldList()
meth protected java.util.List<java.lang.reflect.Field> getFields(boolean)
meth protected void allocateMemory()
meth protected void allocateMemory(int)
meth protected void cacheTypeInfo(com.sun.jna.Pointer)
meth protected void ensureAllocated()
meth protected void setAlignType(int)
meth protected void setStringEncoding(java.lang.String)
meth protected void sortFields(java.util.List<java.lang.reflect.Field>,java.util.List<java.lang.String>)
meth protected void useMemory(com.sun.jna.Pointer)
meth protected void useMemory(com.sun.jna.Pointer,int)
meth protected void writeField(com.sun.jna.Structure$StructField)
meth public !varargs static java.util.List<java.lang.String> createFieldsOrder(java.lang.String[])
meth public !varargs static java.util.List<java.lang.String> createFieldsOrder(java.util.List<java.lang.String>,java.lang.String[])
meth public boolean dataEquals(com.sun.jna.Structure)
meth public boolean dataEquals(com.sun.jna.Structure,boolean)
meth public boolean equals(java.lang.Object)
meth public boolean getAutoRead()
meth public boolean getAutoWrite()
meth public com.sun.jna.Pointer getPointer()
meth public com.sun.jna.Structure[] toArray(com.sun.jna.Structure[])
meth public com.sun.jna.Structure[] toArray(int)
meth public int hashCode()
meth public int size()
meth public java.lang.Object readField(java.lang.String)
meth public java.lang.String toString()
meth public java.lang.String toString(boolean)
meth public static <%0 extends com.sun.jna.Structure> {%%0} newInstance(java.lang.Class<{%%0}>)
meth public static <%0 extends com.sun.jna.Structure> {%%0} newInstance(java.lang.Class<{%%0}>,com.sun.jna.Pointer)
meth public static java.util.List<java.lang.String> createFieldsOrder(java.lang.String)
meth public static java.util.List<java.lang.String> createFieldsOrder(java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth public static void autoRead(com.sun.jna.Structure[])
meth public static void autoWrite(com.sun.jna.Structure[])
meth public void autoRead()
meth public void autoWrite()
meth public void clear()
meth public void read()
meth public void setAutoRead(boolean)
meth public void setAutoSynch(boolean)
meth public void setAutoWrite(boolean)
meth public void write()
meth public void writeField(java.lang.String)
meth public void writeField(java.lang.String,java.lang.Object)
supr java.lang.Object
hfds LOG,PLACEHOLDER_MEMORY,actualAlignType,alignType,array,autoRead,autoWrite,busy,encoding,fieldList,fieldListLock,fieldOrder,fieldOrderLock,layoutInfo,layoutInfoLock,memory,nativeStrings,readCalled,reads,size,structAlignment,structFields,typeInfo,typeMapper,validationLock,validationMap
hcls AutoAllocated,FFIType,LayoutInfo,NativeStringTracking,StructureSet

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

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

