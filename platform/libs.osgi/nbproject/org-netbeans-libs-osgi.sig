#Signature file v4.1
#Version 1.24.1

CLSS public final info.dmtree.Acl
cons public init(java.lang.String)
cons public init(java.lang.String[],int[])
fld public final static int ADD = 2
fld public final static int ALL_PERMISSION = 31
fld public final static int DELETE = 8
fld public final static int EXEC = 16
fld public final static int GET = 1
fld public final static int REPLACE = 4
meth public boolean equals(java.lang.Object)
meth public boolean isPermitted(java.lang.String,int)
meth public info.dmtree.Acl addPermission(java.lang.String,int)
meth public info.dmtree.Acl deletePermission(java.lang.String,int)
meth public info.dmtree.Acl setPermission(java.lang.String,int)
meth public int getPermissions(java.lang.String)
meth public int hashcode()
meth public java.lang.String toString()
meth public java.lang.String[] getPrincipals()
supr java.lang.Object
hfds ALL_PRINCIPALS,PERMISSION_CODES,PERMISSION_NAMES,globalPermissions,principalPermissions

CLSS public abstract interface info.dmtree.DmtAdmin
meth public abstract info.dmtree.DmtSession getSession(java.lang.String) throws info.dmtree.DmtException
meth public abstract info.dmtree.DmtSession getSession(java.lang.String,int) throws info.dmtree.DmtException
meth public abstract info.dmtree.DmtSession getSession(java.lang.String,java.lang.String,int) throws info.dmtree.DmtException
meth public abstract void addEventListener(int,java.lang.String,info.dmtree.DmtEventListener)
meth public abstract void addEventListener(java.lang.String,int,java.lang.String,info.dmtree.DmtEventListener)
meth public abstract void removeEventListener(info.dmtree.DmtEventListener)

CLSS public final info.dmtree.DmtData
cons public init(boolean)
cons public init(byte[])
cons public init(byte[],boolean)
cons public init(float)
cons public init(int)
cons public init(java.lang.Object)
cons public init(java.lang.String)
cons public init(java.lang.String,byte[])
cons public init(java.lang.String,int)
cons public init(java.lang.String,java.lang.String)
fld public final static info.dmtree.DmtData NULL_VALUE
fld public final static int FORMAT_BASE64 = 128
fld public final static int FORMAT_BINARY = 64
fld public final static int FORMAT_BOOLEAN = 8
fld public final static int FORMAT_DATE = 16
fld public final static int FORMAT_FLOAT = 2
fld public final static int FORMAT_INTEGER = 1
fld public final static int FORMAT_NODE = 1024
fld public final static int FORMAT_NULL = 512
fld public final static int FORMAT_RAW_BINARY = 4096
fld public final static int FORMAT_RAW_STRING = 2048
fld public final static int FORMAT_STRING = 4
fld public final static int FORMAT_TIME = 32
fld public final static int FORMAT_XML = 256
meth public boolean equals(java.lang.Object)
meth public boolean getBoolean()
meth public byte[] getBase64()
meth public byte[] getBinary()
meth public byte[] getRawBinary()
meth public float getFloat()
meth public int getFormat()
meth public int getInt()
meth public int getSize()
meth public int hashCode()
meth public java.lang.Object getNode()
meth public java.lang.String getDate()
meth public java.lang.String getFormatName()
meth public java.lang.String getRawString()
meth public java.lang.String getString()
meth public java.lang.String getTime()
meth public java.lang.String getXml()
meth public java.lang.String toString()
supr java.lang.Object
hfds FORMAT_NAMES,bool,bytes,complex,flt,format,formatName,hex,integer,str

CLSS public abstract interface info.dmtree.DmtEvent
fld public final static int ADDED = 1
fld public final static int COPIED = 2
fld public final static int DELETED = 4
fld public final static int RENAMED = 8
fld public final static int REPLACED = 16
fld public final static int SESSION_CLOSED = 64
fld public final static int SESSION_OPENED = 32
meth public abstract int getSessionId()
meth public abstract int getType()
meth public abstract java.lang.String[] getNewNodes()
meth public abstract java.lang.String[] getNodes()

CLSS public abstract interface info.dmtree.DmtEventListener
meth public abstract void changeOccurred(info.dmtree.DmtEvent)

CLSS public info.dmtree.DmtException
cons public init(java.lang.String,int,java.lang.String)
cons public init(java.lang.String,int,java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,int,java.lang.String,java.util.Vector,boolean)
cons public init(java.lang.String[],int,java.lang.String)
cons public init(java.lang.String[],int,java.lang.String,java.lang.Throwable)
cons public init(java.lang.String[],int,java.lang.String,java.util.Vector,boolean)
fld public final static int ALERT_NOT_ROUTED = 5
fld public final static int COMMAND_FAILED = 500
fld public final static int COMMAND_NOT_ALLOWED = 405
fld public final static int CONCURRENT_ACCESS = 4
fld public final static int DATA_STORE_FAILURE = 510
fld public final static int FEATURE_NOT_SUPPORTED = 406
fld public final static int INVALID_URI = 3
fld public final static int METADATA_MISMATCH = 2
fld public final static int NODE_ALREADY_EXISTS = 418
fld public final static int NODE_NOT_FOUND = 404
fld public final static int PERMISSION_DENIED = 425
fld public final static int REMOTE_ERROR = 1
fld public final static int ROLLBACK_FAILED = 516
fld public final static int SESSION_CREATION_TIMEOUT = 7
fld public final static int TRANSACTION_ERROR = 6
fld public final static int UNAUTHORIZED = 401
fld public final static int URI_TOO_LONG = 414
meth public boolean isFatal()
meth public int getCode()
meth public java.lang.String getMessage()
meth public java.lang.String getURI()
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable[] getCauses()
meth public void printStackTrace(java.io.PrintStream)
supr java.lang.Exception
hfds causes,code,fatal,message,serialVersionUID,uri

CLSS public info.dmtree.DmtIllegalStateException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
meth public java.lang.Throwable getCause()
meth public java.lang.Throwable initCause(java.lang.Throwable)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public abstract interface info.dmtree.DmtSession
fld public final static int LOCK_TYPE_ATOMIC = 2
fld public final static int LOCK_TYPE_EXCLUSIVE = 1
fld public final static int LOCK_TYPE_SHARED = 0
fld public final static int STATE_CLOSED = 1
fld public final static int STATE_INVALID = 2
fld public final static int STATE_OPEN = 0
meth public abstract boolean isLeafNode(java.lang.String) throws info.dmtree.DmtException
meth public abstract boolean isNodeUri(java.lang.String)
meth public abstract info.dmtree.Acl getEffectiveNodeAcl(java.lang.String) throws info.dmtree.DmtException
meth public abstract info.dmtree.Acl getNodeAcl(java.lang.String) throws info.dmtree.DmtException
meth public abstract info.dmtree.DmtData getNodeValue(java.lang.String) throws info.dmtree.DmtException
meth public abstract info.dmtree.MetaNode getMetaNode(java.lang.String) throws info.dmtree.DmtException
meth public abstract int getLockType()
meth public abstract int getNodeSize(java.lang.String) throws info.dmtree.DmtException
meth public abstract int getNodeVersion(java.lang.String) throws info.dmtree.DmtException
meth public abstract int getSessionId()
meth public abstract int getState()
meth public abstract java.lang.String getNodeTitle(java.lang.String) throws info.dmtree.DmtException
meth public abstract java.lang.String getNodeType(java.lang.String) throws info.dmtree.DmtException
meth public abstract java.lang.String getPrincipal()
meth public abstract java.lang.String getRootUri()
meth public abstract java.lang.String[] getChildNodeNames(java.lang.String) throws info.dmtree.DmtException
meth public abstract java.util.Date getNodeTimestamp(java.lang.String) throws info.dmtree.DmtException
meth public abstract void close() throws info.dmtree.DmtException
meth public abstract void commit() throws info.dmtree.DmtException
meth public abstract void copy(java.lang.String,java.lang.String,boolean) throws info.dmtree.DmtException
meth public abstract void createInteriorNode(java.lang.String) throws info.dmtree.DmtException
meth public abstract void createInteriorNode(java.lang.String,java.lang.String) throws info.dmtree.DmtException
meth public abstract void createLeafNode(java.lang.String) throws info.dmtree.DmtException
meth public abstract void createLeafNode(java.lang.String,info.dmtree.DmtData) throws info.dmtree.DmtException
meth public abstract void createLeafNode(java.lang.String,info.dmtree.DmtData,java.lang.String) throws info.dmtree.DmtException
meth public abstract void deleteNode(java.lang.String) throws info.dmtree.DmtException
meth public abstract void execute(java.lang.String,java.lang.String) throws info.dmtree.DmtException
meth public abstract void execute(java.lang.String,java.lang.String,java.lang.String) throws info.dmtree.DmtException
meth public abstract void renameNode(java.lang.String,java.lang.String) throws info.dmtree.DmtException
meth public abstract void rollback() throws info.dmtree.DmtException
meth public abstract void setDefaultNodeValue(java.lang.String) throws info.dmtree.DmtException
meth public abstract void setNodeAcl(java.lang.String,info.dmtree.Acl) throws info.dmtree.DmtException
meth public abstract void setNodeTitle(java.lang.String,java.lang.String) throws info.dmtree.DmtException
meth public abstract void setNodeType(java.lang.String,java.lang.String) throws info.dmtree.DmtException
meth public abstract void setNodeValue(java.lang.String,info.dmtree.DmtData) throws info.dmtree.DmtException

CLSS public abstract interface info.dmtree.MetaNode
fld public final static int AUTOMATIC = 2
fld public final static int CMD_ADD = 0
fld public final static int CMD_DELETE = 1
fld public final static int CMD_EXECUTE = 2
fld public final static int CMD_GET = 4
fld public final static int CMD_REPLACE = 3
fld public final static int DYNAMIC = 1
fld public final static int PERMANENT = 0
meth public abstract boolean can(int)
meth public abstract boolean isLeaf()
meth public abstract boolean isValidName(java.lang.String)
meth public abstract boolean isValidValue(info.dmtree.DmtData)
meth public abstract boolean isZeroOccurrenceAllowed()
meth public abstract double getMax()
meth public abstract double getMin()
meth public abstract info.dmtree.DmtData getDefault()
meth public abstract info.dmtree.DmtData[] getValidValues()
meth public abstract int getFormat()
meth public abstract int getMaxOccurrence()
meth public abstract int getScope()
meth public abstract java.lang.Object getExtensionProperty(java.lang.String)
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String[] getExtensionPropertyKeys()
meth public abstract java.lang.String[] getMimeTypes()
meth public abstract java.lang.String[] getRawFormatNames()
meth public abstract java.lang.String[] getValidNames()

CLSS public final info.dmtree.Uri
meth public static boolean isAbsoluteUri(java.lang.String)
meth public static boolean isValidUri(java.lang.String)
meth public static int getMaxSegmentNameLength()
meth public static int getMaxUriLength()
meth public static int getMaxUriSegments()
meth public static java.lang.String mangle(java.lang.String)
meth public static java.lang.String toUri(java.lang.String[])
meth public static java.lang.String[] toPath(java.lang.String)
supr java.lang.Object
hfds BASE_64_TABLE,MINIMAL_SEGMENT_LENGTH_LIMIT,SEGMENT_LENGTH_LIMIT_PROPERTY,array$B,segmentLengthLimit
hcls ImplHolder

CLSS public abstract interface java.io.Serializable

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

CLSS public java.lang.RuntimeException
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception
hfds serialVersionUID

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

