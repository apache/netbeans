#Signature file v4.1
#Version 1.18

CLSS public com.sun.corba.ee.org.omg.CORBA.GetPropertyAction
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
intf java.security.PrivilegedAction
meth public java.lang.Object run()
supr java.lang.Object
hfds defaultVal,theProp

CLSS public abstract com.sun.corba.ee.org.omg.CORBA.ORB
cons public init()
meth public void register_initial_reference(java.lang.String,org.omg.CORBA.Object) throws org.omg.CORBA.ORBPackage.InvalidName
supr org.omg.CORBA_2_3.ORB

CLSS public abstract interface com.sun.corba.ee.org.omg.CORBA.SUNVMCID
fld public final static int value = 1398079488

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

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

CLSS public abstract interface java.rmi.Remote

CLSS public java.rmi.RemoteException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
fld public java.lang.Throwable detail
meth public java.lang.String getMessage()
meth public java.lang.Throwable getCause()
supr java.io.IOException

CLSS public abstract interface java.security.PrivilegedAction<%0 extends java.lang.Object>
meth public abstract {java.security.PrivilegedAction%0} run()

CLSS public javax.activity.ActivityCompletedException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.rmi.RemoteException

CLSS public javax.activity.ActivityRequiredException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.rmi.RemoteException

CLSS public javax.activity.InvalidActivityException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.rmi.RemoteException

CLSS public javax.rmi.CORBA.ClassDesc
cons public init()
intf java.io.Serializable
supr java.lang.Object
hfds codebase,repid

CLSS public javax.rmi.CORBA.EnumDesc
cons public init()
fld public java.lang.String className
fld public java.lang.String value
intf java.io.Serializable
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract interface javax.rmi.CORBA.PortableRemoteObjectDelegate
meth public abstract java.lang.Object narrow(java.lang.Object,java.lang.Class)
meth public abstract java.rmi.Remote toStub(java.rmi.Remote) throws java.rmi.NoSuchObjectException
meth public abstract void connect(java.rmi.Remote,java.rmi.Remote) throws java.rmi.RemoteException
meth public abstract void exportObject(java.rmi.Remote) throws java.rmi.RemoteException
meth public abstract void unexportObject(java.rmi.Remote) throws java.rmi.NoSuchObjectException

CLSS public javax.rmi.CORBA.ProxyDesc
cons public init()
fld public java.lang.String codebase
fld public java.lang.String[] interfaces
fld public java.lang.reflect.InvocationHandler handler
intf java.io.Serializable
supr java.lang.Object
hfds serialVersionUID

CLSS public abstract javax.rmi.CORBA.Stub
cons public init()
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
meth public void connect(org.omg.CORBA.ORB) throws java.rmi.RemoteException
supr org.omg.CORBA_2_3.portable.ObjectImpl
hfds StubClassKey,defaultStubImplName,serialVersionUID,stubDelegate,stubDelegateClass

CLSS public abstract interface javax.rmi.CORBA.StubDelegate
meth public abstract boolean equals(javax.rmi.CORBA.Stub,java.lang.Object)
meth public abstract int hashCode(javax.rmi.CORBA.Stub)
meth public abstract java.lang.String toString(javax.rmi.CORBA.Stub)
meth public abstract void connect(javax.rmi.CORBA.Stub,org.omg.CORBA.ORB) throws java.rmi.RemoteException
meth public abstract void readObject(javax.rmi.CORBA.Stub,java.io.ObjectInputStream) throws java.io.IOException,java.lang.ClassNotFoundException
meth public abstract void writeObject(javax.rmi.CORBA.Stub,java.io.ObjectOutputStream) throws java.io.IOException

CLSS public abstract interface javax.rmi.CORBA.Tie
intf org.omg.CORBA.portable.InvokeHandler
meth public abstract java.rmi.Remote getTarget()
meth public abstract org.omg.CORBA.ORB orb()
meth public abstract org.omg.CORBA.Object thisObject()
meth public abstract void deactivate() throws java.rmi.NoSuchObjectException
meth public abstract void orb(org.omg.CORBA.ORB)
meth public abstract void setTarget(java.rmi.Remote)

CLSS public javax.rmi.CORBA.Util
meth public static boolean isLocal(javax.rmi.CORBA.Stub) throws java.rmi.RemoteException
meth public static java.lang.Class loadClass(java.lang.String,java.lang.String,java.lang.ClassLoader) throws java.lang.ClassNotFoundException
meth public static java.lang.Object copyObject(java.lang.Object,org.omg.CORBA.ORB) throws java.rmi.RemoteException
meth public static java.lang.Object readAny(org.omg.CORBA.portable.InputStream)
meth public static java.lang.Object[] copyObjects(java.lang.Object[],org.omg.CORBA.ORB) throws java.rmi.RemoteException
meth public static java.lang.String getCodebase(java.lang.Class)
meth public static java.rmi.RemoteException mapSystemException(org.omg.CORBA.SystemException)
meth public static java.rmi.RemoteException wrapException(java.lang.Throwable)
meth public static javax.rmi.CORBA.Tie getTie(java.rmi.Remote)
meth public static javax.rmi.CORBA.ValueHandler createValueHandler()
meth public static void registerTarget(javax.rmi.CORBA.Tie,java.rmi.Remote)
meth public static void unexportObject(java.rmi.Remote) throws java.rmi.NoSuchObjectException
meth public static void writeAbstractObject(org.omg.CORBA.portable.OutputStream,java.lang.Object)
meth public static void writeAny(org.omg.CORBA.portable.OutputStream,java.lang.Object)
meth public static void writeRemoteObject(org.omg.CORBA.portable.OutputStream,java.lang.Object)
supr java.lang.Object
hfds UtilClassKey,defaultUtilImplName,utilDelegate

CLSS public abstract interface javax.rmi.CORBA.UtilDelegate
meth public abstract boolean isLocal(javax.rmi.CORBA.Stub) throws java.rmi.RemoteException
meth public abstract java.lang.Class loadClass(java.lang.String,java.lang.String,java.lang.ClassLoader) throws java.lang.ClassNotFoundException
meth public abstract java.lang.Object copyObject(java.lang.Object,org.omg.CORBA.ORB) throws java.rmi.RemoteException
meth public abstract java.lang.Object readAny(org.omg.CORBA.portable.InputStream)
meth public abstract java.lang.Object[] copyObjects(java.lang.Object[],org.omg.CORBA.ORB) throws java.rmi.RemoteException
meth public abstract java.lang.String getCodebase(java.lang.Class)
meth public abstract java.rmi.RemoteException mapSystemException(org.omg.CORBA.SystemException)
meth public abstract java.rmi.RemoteException wrapException(java.lang.Throwable)
meth public abstract javax.rmi.CORBA.Tie getTie(java.rmi.Remote)
meth public abstract javax.rmi.CORBA.ValueHandler createValueHandler()
meth public abstract void registerTarget(javax.rmi.CORBA.Tie,java.rmi.Remote)
meth public abstract void unexportObject(java.rmi.Remote) throws java.rmi.NoSuchObjectException
meth public abstract void writeAbstractObject(org.omg.CORBA.portable.OutputStream,java.lang.Object)
meth public abstract void writeAny(org.omg.CORBA.portable.OutputStream,java.lang.Object)
meth public abstract void writeRemoteObject(org.omg.CORBA.portable.OutputStream,java.lang.Object)

CLSS public abstract interface javax.rmi.CORBA.ValueHandler
meth public abstract boolean isCustomMarshaled(java.lang.Class)
meth public abstract java.io.Serializable readValue(org.omg.CORBA.portable.InputStream,int,java.lang.Class,java.lang.String,org.omg.SendingContext.RunTime)
meth public abstract java.io.Serializable writeReplace(java.io.Serializable)
meth public abstract java.lang.String getRMIRepositoryID(java.lang.Class)
meth public abstract org.omg.SendingContext.RunTime getRunTimeCodeBase()
meth public abstract void writeValue(org.omg.CORBA.portable.OutputStream,java.io.Serializable)

CLSS public abstract interface javax.rmi.CORBA.ValueHandlerMultiFormat
intf javax.rmi.CORBA.ValueHandler
meth public abstract byte getMaximumStreamFormatVersion()
meth public abstract void writeValue(org.omg.CORBA.portable.OutputStream,java.io.Serializable,byte)

CLSS public javax.rmi.PortableRemoteObject
cons protected init() throws java.rmi.RemoteException
meth public static java.lang.Object narrow(java.lang.Object,java.lang.Class)
meth public static java.rmi.Remote toStub(java.rmi.Remote) throws java.rmi.NoSuchObjectException
meth public static void connect(java.rmi.Remote,java.rmi.Remote) throws java.rmi.RemoteException
meth public static void exportObject(java.rmi.Remote) throws java.rmi.RemoteException
meth public static void unexportObject(java.rmi.Remote) throws java.rmi.NoSuchObjectException
supr java.lang.Object
hfds PortableRemoteObjectClassKey,defaultPortableRemoteObjectImplName,proDelegate

CLSS public final org.omg.CORBA.ACTIVITY_COMPLETED
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.ACTIVITY_REQUIRED
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public abstract interface org.omg.CORBA.ARG_IN
fld public final static int value = 1

CLSS public abstract interface org.omg.CORBA.ARG_INOUT
fld public final static int value = 3

CLSS public abstract interface org.omg.CORBA.ARG_OUT
fld public final static int value = 2

CLSS public abstract interface org.omg.CORBA.AliasDef
intf org.omg.CORBA.AliasDefOperations
intf org.omg.CORBA.TypedefDef
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.AliasDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.AliasDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.AliasDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.AliasDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.AliasDef unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.AliasDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.AliasDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.AliasDefHolder
cons public init()
cons public init(org.omg.CORBA.AliasDef)
fld public org.omg.CORBA.AliasDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.AliasDefOperations
intf org.omg.CORBA.TypedefDefOperations
meth public abstract org.omg.CORBA.IDLType original_type_def()
meth public abstract void original_type_def(org.omg.CORBA.IDLType)

CLSS public abstract org.omg.CORBA.AliasDefPOA
cons public init()
intf org.omg.CORBA.AliasDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.AliasDef _this()
meth public org.omg.CORBA.AliasDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.AliasDefPOATie
cons public init(org.omg.CORBA.AliasDefOperations)
cons public init(org.omg.CORBA.AliasDefOperations,org.omg.PortableServer.POA)
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public org.omg.CORBA.AliasDefOperations _delegate()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.IDLType original_type_def()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.AliasDefOperations)
meth public void destroy()
meth public void id(java.lang.String)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void original_type_def(org.omg.CORBA.IDLType)
meth public void version(java.lang.String)
supr org.omg.CORBA.AliasDefPOA
hfds _impl,_poa

CLSS public abstract org.omg.CORBA.Any
cons public init()
intf org.omg.CORBA.portable.IDLEntity
meth public abstract boolean equal(org.omg.CORBA.Any)
meth public abstract boolean extract_boolean()
meth public abstract byte extract_octet()
meth public abstract char extract_char()
meth public abstract char extract_wchar()
meth public abstract double extract_double()
meth public abstract float extract_float()
meth public abstract int extract_long()
meth public abstract int extract_ulong()
meth public abstract java.io.Serializable extract_Value()
meth public abstract java.lang.String extract_string()
meth public abstract java.lang.String extract_wstring()
meth public abstract long extract_longlong()
meth public abstract long extract_ulonglong()
meth public abstract org.omg.CORBA.Any extract_any()
meth public abstract org.omg.CORBA.Object extract_Object()
meth public abstract org.omg.CORBA.TypeCode extract_TypeCode()
meth public abstract org.omg.CORBA.TypeCode type()
meth public abstract org.omg.CORBA.portable.InputStream create_input_stream()
meth public abstract org.omg.CORBA.portable.OutputStream create_output_stream()
meth public abstract short extract_short()
meth public abstract short extract_ushort()
meth public abstract void insert_Object(org.omg.CORBA.Object)
meth public abstract void insert_Object(org.omg.CORBA.Object,org.omg.CORBA.TypeCode)
meth public abstract void insert_TypeCode(org.omg.CORBA.TypeCode)
meth public abstract void insert_Value(java.io.Serializable)
meth public abstract void insert_Value(java.io.Serializable,org.omg.CORBA.TypeCode)
meth public abstract void insert_any(org.omg.CORBA.Any)
meth public abstract void insert_boolean(boolean)
meth public abstract void insert_char(char)
meth public abstract void insert_double(double)
meth public abstract void insert_float(float)
meth public abstract void insert_long(int)
meth public abstract void insert_longlong(long)
meth public abstract void insert_octet(byte)
meth public abstract void insert_short(short)
meth public abstract void insert_string(java.lang.String)
meth public abstract void insert_ulong(int)
meth public abstract void insert_ulonglong(long)
meth public abstract void insert_ushort(short)
meth public abstract void insert_wchar(char)
meth public abstract void insert_wstring(java.lang.String)
meth public abstract void read_value(org.omg.CORBA.portable.InputStream,org.omg.CORBA.TypeCode)
meth public abstract void type(org.omg.CORBA.TypeCode)
meth public abstract void write_value(org.omg.CORBA.portable.OutputStream)
meth public java.math.BigDecimal extract_fixed()
meth public org.omg.CORBA.Principal extract_Principal()
meth public org.omg.CORBA.portable.Streamable extract_Streamable()
meth public void insert_Principal(org.omg.CORBA.Principal)
meth public void insert_Streamable(org.omg.CORBA.portable.Streamable)
meth public void insert_fixed(java.math.BigDecimal)
meth public void insert_fixed(java.math.BigDecimal,org.omg.CORBA.TypeCode)
supr java.lang.Object

CLSS public final org.omg.CORBA.AnyHolder
cons public init()
cons public init(org.omg.CORBA.Any)
fld public org.omg.CORBA.Any value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.AnySeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.Any[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.Any[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.Any[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.Any[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.AnySeqHolder
cons public init()
cons public init(org.omg.CORBA.Any[])
fld public org.omg.CORBA.Any[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.ArrayDef
intf org.omg.CORBA.ArrayDefOperations
intf org.omg.CORBA.IDLType
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.ArrayDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ArrayDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ArrayDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.ArrayDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.ArrayDef unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ArrayDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ArrayDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ArrayDefHolder
cons public init()
cons public init(org.omg.CORBA.ArrayDef)
fld public org.omg.CORBA.ArrayDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.ArrayDefOperations
intf org.omg.CORBA.IDLTypeOperations
meth public abstract int length()
meth public abstract org.omg.CORBA.IDLType element_type_def()
meth public abstract org.omg.CORBA.TypeCode element_type()
meth public abstract void element_type_def(org.omg.CORBA.IDLType)
meth public abstract void length(int)

CLSS public abstract org.omg.CORBA.ArrayDefPOA
cons public init()
intf org.omg.CORBA.ArrayDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.ArrayDef _this()
meth public org.omg.CORBA.ArrayDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.ArrayDefPOATie
cons public init(org.omg.CORBA.ArrayDefOperations)
cons public init(org.omg.CORBA.ArrayDefOperations,org.omg.PortableServer.POA)
meth public int length()
meth public org.omg.CORBA.ArrayDefOperations _delegate()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.IDLType element_type_def()
meth public org.omg.CORBA.TypeCode element_type()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.ArrayDefOperations)
meth public void destroy()
meth public void element_type_def(org.omg.CORBA.IDLType)
meth public void length(int)
supr org.omg.CORBA.ArrayDefPOA
hfds _impl,_poa

CLSS public abstract org.omg.CORBA.AttrDescriptionSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.AttributeDescription[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.AttributeDescription[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.AttributeDescription[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.AttributeDescription[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.AttrDescriptionSeqHolder
cons public init()
cons public init(org.omg.CORBA.AttributeDescription[])
fld public org.omg.CORBA.AttributeDescription[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.AttributeDef
intf org.omg.CORBA.AttributeDefOperations
intf org.omg.CORBA.Contained
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.AttributeDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.AttributeDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.AttributeDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.AttributeDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.AttributeDef unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.AttributeDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.AttributeDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.AttributeDefHolder
cons public init()
cons public init(org.omg.CORBA.AttributeDef)
fld public org.omg.CORBA.AttributeDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.AttributeDefOperations
intf org.omg.CORBA.ContainedOperations
meth public abstract org.omg.CORBA.AttributeMode mode()
meth public abstract org.omg.CORBA.IDLType type_def()
meth public abstract org.omg.CORBA.TypeCode type()
meth public abstract void mode(org.omg.CORBA.AttributeMode)
meth public abstract void type_def(org.omg.CORBA.IDLType)

CLSS public abstract org.omg.CORBA.AttributeDefPOA
cons public init()
intf org.omg.CORBA.AttributeDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.AttributeDef _this()
meth public org.omg.CORBA.AttributeDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.AttributeDefPOATie
cons public init(org.omg.CORBA.AttributeDefOperations)
cons public init(org.omg.CORBA.AttributeDefOperations,org.omg.PortableServer.POA)
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public org.omg.CORBA.AttributeDefOperations _delegate()
meth public org.omg.CORBA.AttributeMode mode()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.IDLType type_def()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.AttributeDefOperations)
meth public void destroy()
meth public void id(java.lang.String)
meth public void mode(org.omg.CORBA.AttributeMode)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void type_def(org.omg.CORBA.IDLType)
meth public void version(java.lang.String)
supr org.omg.CORBA.AttributeDefPOA
hfds _impl,_poa

CLSS public final org.omg.CORBA.AttributeDescription
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.TypeCode,org.omg.CORBA.AttributeMode)
fld public java.lang.String defined_in
fld public java.lang.String id
fld public java.lang.String name
fld public java.lang.String version
fld public org.omg.CORBA.AttributeMode mode
fld public org.omg.CORBA.TypeCode type
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.AttributeDescriptionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.AttributeDescription extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.AttributeDescription read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.AttributeDescription)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.AttributeDescription)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.AttributeDescriptionHolder
cons public init()
cons public init(org.omg.CORBA.AttributeDescription)
fld public org.omg.CORBA.AttributeDescription value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.CORBA.AttributeMode
cons protected init(int)
fld public final static int _ATTR_NORMAL = 0
fld public final static int _ATTR_READONLY = 1
fld public final static org.omg.CORBA.AttributeMode ATTR_NORMAL
fld public final static org.omg.CORBA.AttributeMode ATTR_READONLY
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.CORBA.AttributeMode from_int(int)
supr java.lang.Object
hfds __array,__size,__value

CLSS public abstract org.omg.CORBA.AttributeModeHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.AttributeMode extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.AttributeMode read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.AttributeMode)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.AttributeMode)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.AttributeModeHolder
cons public init()
cons public init(org.omg.CORBA.AttributeMode)
fld public org.omg.CORBA.AttributeMode value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.BAD_CONTEXT
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.BAD_INV_ORDER
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.BAD_OPERATION
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.BAD_PARAM
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public abstract interface org.omg.CORBA.BAD_POLICY
fld public final static short value = 0

CLSS public abstract interface org.omg.CORBA.BAD_POLICY_TYPE
fld public final static short value = 2

CLSS public abstract interface org.omg.CORBA.BAD_POLICY_VALUE
fld public final static short value = 3

CLSS public final org.omg.CORBA.BAD_QOS
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.BAD_TYPECODE
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.BooleanHolder
cons public init()
cons public init(boolean)
fld public boolean value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.BooleanSeqHelper
cons public init()
meth public static boolean[] extract(org.omg.CORBA.Any)
meth public static boolean[] read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,boolean[])
meth public static void write(org.omg.CORBA.portable.OutputStream,boolean[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.BooleanSeqHolder
cons public init()
cons public init(boolean[])
fld public boolean[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.Bounds
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public final org.omg.CORBA.ByteHolder
cons public init()
cons public init(byte)
fld public byte value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.CODESET_INCOMPATIBLE
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.COMM_FAILURE
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public abstract interface org.omg.CORBA.CTX_RESTRICT_SCOPE
fld public final static int value = 15

CLSS public final org.omg.CORBA.CharHolder
cons public init()
cons public init(char)
fld public char value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.CharSeqHelper
cons public init()
meth public static char[] extract(org.omg.CORBA.Any)
meth public static char[] read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,char[])
meth public static void write(org.omg.CORBA.portable.OutputStream,char[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.CharSeqHolder
cons public init()
cons public init(char[])
fld public char[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.CompletionStatus
fld public final static int _COMPLETED_MAYBE = 2
fld public final static int _COMPLETED_NO = 1
fld public final static int _COMPLETED_YES = 0
fld public final static org.omg.CORBA.CompletionStatus COMPLETED_MAYBE
fld public final static org.omg.CORBA.CompletionStatus COMPLETED_NO
fld public final static org.omg.CORBA.CompletionStatus COMPLETED_YES
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.CORBA.CompletionStatus from_int(int)
supr java.lang.Object
hfds _value

CLSS public abstract org.omg.CORBA.CompletionStatusHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.CompletionStatus extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.CompletionStatus read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.CompletionStatus)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.CompletionStatus)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.CORBA.ConstantDef
intf org.omg.CORBA.ConstantDefOperations
intf org.omg.CORBA.Contained
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.ConstantDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ConstantDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ConstantDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.ConstantDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.ConstantDef unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ConstantDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ConstantDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ConstantDefHolder
cons public init()
cons public init(org.omg.CORBA.ConstantDef)
fld public org.omg.CORBA.ConstantDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.ConstantDefOperations
intf org.omg.CORBA.ContainedOperations
meth public abstract org.omg.CORBA.Any value()
meth public abstract org.omg.CORBA.IDLType type_def()
meth public abstract org.omg.CORBA.TypeCode type()
meth public abstract void type_def(org.omg.CORBA.IDLType)
meth public abstract void value(org.omg.CORBA.Any)

CLSS public abstract org.omg.CORBA.ConstantDefPOA
cons public init()
intf org.omg.CORBA.ConstantDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.ConstantDef _this()
meth public org.omg.CORBA.ConstantDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.ConstantDefPOATie
cons public init(org.omg.CORBA.ConstantDefOperations)
cons public init(org.omg.CORBA.ConstantDefOperations,org.omg.PortableServer.POA)
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public org.omg.CORBA.Any value()
meth public org.omg.CORBA.ConstantDefOperations _delegate()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.IDLType type_def()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.ConstantDefOperations)
meth public void destroy()
meth public void id(java.lang.String)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void type_def(org.omg.CORBA.IDLType)
meth public void value(org.omg.CORBA.Any)
meth public void version(java.lang.String)
supr org.omg.CORBA.ConstantDefPOA
hfds _impl,_poa

CLSS public final org.omg.CORBA.ConstantDescription
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.TypeCode,org.omg.CORBA.Any)
fld public java.lang.String defined_in
fld public java.lang.String id
fld public java.lang.String name
fld public java.lang.String version
fld public org.omg.CORBA.Any value
fld public org.omg.CORBA.TypeCode type
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ConstantDescriptionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ConstantDescription extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ConstantDescription read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ConstantDescription)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ConstantDescription)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.ConstantDescriptionHolder
cons public init()
cons public init(org.omg.CORBA.ConstantDescription)
fld public org.omg.CORBA.ConstantDescription value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.Contained
intf org.omg.CORBA.ContainedOperations
intf org.omg.CORBA.IRObject
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.ContainedHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.Contained extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.Contained narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.Contained read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.Contained unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.Contained)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.Contained)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ContainedHolder
cons public init()
cons public init(org.omg.CORBA.Contained)
fld public org.omg.CORBA.Contained value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.ContainedOperations
intf org.omg.CORBA.IRObjectOperations
meth public abstract java.lang.String absolute_name()
meth public abstract java.lang.String id()
meth public abstract java.lang.String name()
meth public abstract java.lang.String version()
meth public abstract org.omg.CORBA.ContainedPackage.Description describe()
meth public abstract org.omg.CORBA.Container defined_in()
meth public abstract org.omg.CORBA.Repository containing_repository()
meth public abstract void id(java.lang.String)
meth public abstract void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public abstract void name(java.lang.String)
meth public abstract void version(java.lang.String)

CLSS public abstract org.omg.CORBA.ContainedPOA
cons public init()
intf org.omg.CORBA.ContainedOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.Contained _this()
meth public org.omg.CORBA.Contained _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.ContainedPOATie
cons public init(org.omg.CORBA.ContainedOperations)
cons public init(org.omg.CORBA.ContainedOperations,org.omg.PortableServer.POA)
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public org.omg.CORBA.ContainedOperations _delegate()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.ContainedOperations)
meth public void destroy()
meth public void id(java.lang.String)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.ContainedPOA
hfds _impl,_poa

CLSS public final org.omg.CORBA.ContainedPackage.Description
cons public init()
cons public init(org.omg.CORBA.DefinitionKind,org.omg.CORBA.Any)
fld public org.omg.CORBA.Any value
fld public org.omg.CORBA.DefinitionKind kind
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ContainedPackage.DescriptionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ContainedPackage.Description extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ContainedPackage.Description read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ContainedPackage.Description)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ContainedPackage.Description)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.ContainedPackage.DescriptionHolder
cons public init()
cons public init(org.omg.CORBA.ContainedPackage.Description)
fld public org.omg.CORBA.ContainedPackage.Description value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ContainedSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.Contained[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.Contained[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.Contained[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.Contained[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ContainedSeqHolder
cons public init()
cons public init(org.omg.CORBA.Contained[])
fld public org.omg.CORBA.Contained[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.Container
intf org.omg.CORBA.ContainerOperations
intf org.omg.CORBA.IRObject
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.ContainerHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.Container extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.Container narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.Container read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.Container unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.Container)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.Container)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ContainerHolder
cons public init()
cons public init(org.omg.CORBA.Container)
fld public org.omg.CORBA.Container value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.ContainerOperations
intf org.omg.CORBA.IRObjectOperations
meth public abstract org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public abstract org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public abstract org.omg.CORBA.Contained lookup(java.lang.String)
meth public abstract org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public abstract org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public abstract org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public abstract org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public abstract org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public abstract org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public abstract org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public abstract org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public abstract org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public abstract org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])

CLSS public abstract org.omg.CORBA.ContainerPOA
cons public init()
intf org.omg.CORBA.ContainerOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.Container _this()
meth public org.omg.CORBA.Container _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.ContainerPOATie
cons public init(org.omg.CORBA.ContainerOperations)
cons public init(org.omg.CORBA.ContainerOperations,org.omg.PortableServer.POA)
meth public org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public org.omg.CORBA.Contained lookup(java.lang.String)
meth public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.ContainerOperations _delegate()
meth public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.ContainerOperations)
meth public void destroy()
supr org.omg.CORBA.ContainerPOA
hfds _impl,_poa

CLSS public final org.omg.CORBA.ContainerPackage.Description
cons public init()
cons public init(org.omg.CORBA.Contained,org.omg.CORBA.DefinitionKind,org.omg.CORBA.Any)
fld public org.omg.CORBA.Any value
fld public org.omg.CORBA.Contained contained_object
fld public org.omg.CORBA.DefinitionKind kind
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ContainerPackage.DescriptionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ContainerPackage.Description extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ContainerPackage.Description read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ContainerPackage.Description)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ContainerPackage.Description)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.ContainerPackage.DescriptionHolder
cons public init()
cons public init(org.omg.CORBA.ContainerPackage.Description)
fld public org.omg.CORBA.ContainerPackage.Description value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ContainerPackage.DescriptionSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ContainerPackage.Description[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ContainerPackage.Description[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ContainerPackage.Description[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ContainerPackage.Description[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ContainerPackage.DescriptionSeqHolder
cons public init()
cons public init(org.omg.CORBA.ContainerPackage.Description[])
fld public org.omg.CORBA.ContainerPackage.Description[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.Context
cons public init()
meth public abstract java.lang.String context_name()
meth public abstract org.omg.CORBA.Context create_child(java.lang.String)
meth public abstract org.omg.CORBA.Context parent()
meth public abstract org.omg.CORBA.NVList get_values(java.lang.String,int,java.lang.String)
meth public abstract void delete_values(java.lang.String)
meth public abstract void set_one_value(java.lang.String,org.omg.CORBA.Any)
meth public abstract void set_values(org.omg.CORBA.NVList)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ContextIdSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static java.lang.String[] extract(org.omg.CORBA.Any)
meth public static java.lang.String[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String[])
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ContextIdSeqHolder
cons public init()
cons public init(java.lang.String[])
fld public java.lang.String[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ContextIdentifierHelper
cons public init()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract org.omg.CORBA.ContextList
cons public init()
meth public abstract int count()
meth public abstract java.lang.String item(int) throws org.omg.CORBA.Bounds
meth public abstract void add(java.lang.String)
meth public abstract void remove(int) throws org.omg.CORBA.Bounds
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.Current
intf org.omg.CORBA.CurrentOperations
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.CurrentHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.Current extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.Current narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.Current read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.Current)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.Current)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.CurrentHolder
cons public init()
cons public init(org.omg.CORBA.Current)
fld public org.omg.CORBA.Current value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.CurrentOperations

CLSS public abstract interface org.omg.CORBA.CustomMarshal
meth public abstract void marshal(org.omg.CORBA.DataOutputStream)
meth public abstract void unmarshal(org.omg.CORBA.DataInputStream)

CLSS public final org.omg.CORBA.DATA_CONVERSION
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public abstract interface org.omg.CORBA.DataInputStream
intf org.omg.CORBA.portable.ValueBase
meth public abstract boolean read_boolean()
meth public abstract byte read_octet()
meth public abstract char read_char()
meth public abstract char read_wchar()
meth public abstract double read_double()
meth public abstract float read_float()
meth public abstract int read_long()
meth public abstract int read_ulong()
meth public abstract java.io.Serializable read_Value()
meth public abstract java.lang.Object read_Abstract()
meth public abstract java.lang.String read_string()
meth public abstract java.lang.String read_wstring()
meth public abstract long read_longlong()
meth public abstract long read_ulonglong()
meth public abstract org.omg.CORBA.Any read_any()
meth public abstract org.omg.CORBA.Object read_Object()
meth public abstract org.omg.CORBA.TypeCode read_TypeCode()
meth public abstract short read_short()
meth public abstract short read_ushort()
meth public abstract void read_any_array(org.omg.CORBA.AnySeqHolder,int,int)
meth public abstract void read_boolean_array(org.omg.CORBA.BooleanSeqHolder,int,int)
meth public abstract void read_char_array(org.omg.CORBA.CharSeqHolder,int,int)
meth public abstract void read_double_array(org.omg.CORBA.DoubleSeqHolder,int,int)
meth public abstract void read_float_array(org.omg.CORBA.FloatSeqHolder,int,int)
meth public abstract void read_long_array(org.omg.CORBA.LongSeqHolder,int,int)
meth public abstract void read_longlong_array(org.omg.CORBA.LongLongSeqHolder,int,int)
meth public abstract void read_octet_array(org.omg.CORBA.OctetSeqHolder,int,int)
meth public abstract void read_short_array(org.omg.CORBA.ShortSeqHolder,int,int)
meth public abstract void read_ulong_array(org.omg.CORBA.ULongSeqHolder,int,int)
meth public abstract void read_ulonglong_array(org.omg.CORBA.ULongLongSeqHolder,int,int)
meth public abstract void read_ushort_array(org.omg.CORBA.UShortSeqHolder,int,int)
meth public abstract void read_wchar_array(org.omg.CORBA.WCharSeqHolder,int,int)

CLSS public abstract interface org.omg.CORBA.DataOutputStream
intf org.omg.CORBA.portable.ValueBase
meth public abstract void write_Abstract(java.lang.Object)
meth public abstract void write_Object(org.omg.CORBA.Object)
meth public abstract void write_TypeCode(org.omg.CORBA.TypeCode)
meth public abstract void write_Value(java.io.Serializable)
meth public abstract void write_any(org.omg.CORBA.Any)
meth public abstract void write_any_array(org.omg.CORBA.Any[],int,int)
meth public abstract void write_boolean(boolean)
meth public abstract void write_boolean_array(boolean[],int,int)
meth public abstract void write_char(char)
meth public abstract void write_char_array(char[],int,int)
meth public abstract void write_double(double)
meth public abstract void write_double_array(double[],int,int)
meth public abstract void write_float(float)
meth public abstract void write_float_array(float[],int,int)
meth public abstract void write_long(int)
meth public abstract void write_long_array(int[],int,int)
meth public abstract void write_longlong(long)
meth public abstract void write_longlong_array(long[],int,int)
meth public abstract void write_octet(byte)
meth public abstract void write_octet_array(byte[],int,int)
meth public abstract void write_short(short)
meth public abstract void write_short_array(short[],int,int)
meth public abstract void write_string(java.lang.String)
meth public abstract void write_ulong(int)
meth public abstract void write_ulong_array(int[],int,int)
meth public abstract void write_ulonglong(long)
meth public abstract void write_ulonglong_array(long[],int,int)
meth public abstract void write_ushort(short)
meth public abstract void write_ushort_array(short[],int,int)
meth public abstract void write_wchar(char)
meth public abstract void write_wchar_array(char[],int,int)
meth public abstract void write_wstring(java.lang.String)

CLSS public org.omg.CORBA.DefinitionKind
cons protected init(int)
fld public final static int _dk_AbstractInterface = 24
fld public final static int _dk_Alias = 9
fld public final static int _dk_Array = 16
fld public final static int _dk_Attribute = 2
fld public final static int _dk_Constant = 3
fld public final static int _dk_Enum = 12
fld public final static int _dk_Exception = 4
fld public final static int _dk_Fixed = 19
fld public final static int _dk_Interface = 5
fld public final static int _dk_Module = 6
fld public final static int _dk_Native = 23
fld public final static int _dk_Operation = 7
fld public final static int _dk_Primitive = 13
fld public final static int _dk_Repository = 17
fld public final static int _dk_Sequence = 15
fld public final static int _dk_String = 14
fld public final static int _dk_Struct = 10
fld public final static int _dk_Typedef = 8
fld public final static int _dk_Union = 11
fld public final static int _dk_Value = 20
fld public final static int _dk_ValueBox = 21
fld public final static int _dk_ValueMember = 22
fld public final static int _dk_Wstring = 18
fld public final static int _dk_all = 1
fld public final static int _dk_none = 0
fld public final static org.omg.CORBA.DefinitionKind dk_AbstractInterface
fld public final static org.omg.CORBA.DefinitionKind dk_Alias
fld public final static org.omg.CORBA.DefinitionKind dk_Array
fld public final static org.omg.CORBA.DefinitionKind dk_Attribute
fld public final static org.omg.CORBA.DefinitionKind dk_Constant
fld public final static org.omg.CORBA.DefinitionKind dk_Enum
fld public final static org.omg.CORBA.DefinitionKind dk_Exception
fld public final static org.omg.CORBA.DefinitionKind dk_Fixed
fld public final static org.omg.CORBA.DefinitionKind dk_Interface
fld public final static org.omg.CORBA.DefinitionKind dk_Module
fld public final static org.omg.CORBA.DefinitionKind dk_Native
fld public final static org.omg.CORBA.DefinitionKind dk_Operation
fld public final static org.omg.CORBA.DefinitionKind dk_Primitive
fld public final static org.omg.CORBA.DefinitionKind dk_Repository
fld public final static org.omg.CORBA.DefinitionKind dk_Sequence
fld public final static org.omg.CORBA.DefinitionKind dk_String
fld public final static org.omg.CORBA.DefinitionKind dk_Struct
fld public final static org.omg.CORBA.DefinitionKind dk_Typedef
fld public final static org.omg.CORBA.DefinitionKind dk_Union
fld public final static org.omg.CORBA.DefinitionKind dk_Value
fld public final static org.omg.CORBA.DefinitionKind dk_ValueBox
fld public final static org.omg.CORBA.DefinitionKind dk_ValueMember
fld public final static org.omg.CORBA.DefinitionKind dk_Wstring
fld public final static org.omg.CORBA.DefinitionKind dk_all
fld public final static org.omg.CORBA.DefinitionKind dk_none
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.CORBA.DefinitionKind from_int(int)
supr java.lang.Object
hfds _value

CLSS public abstract org.omg.CORBA.DefinitionKindHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.DefinitionKind extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.DefinitionKind read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.DefinitionKind)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.DefinitionKind)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.CORBA.DomainManager
intf org.omg.CORBA.DomainManagerOperations
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract interface org.omg.CORBA.DomainManagerOperations
meth public abstract org.omg.CORBA.Policy get_domain_policy(int)

CLSS public final org.omg.CORBA.DoubleHolder
cons public init()
cons public init(double)
fld public double value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.DoubleSeqHelper
cons public init()
meth public static double[] extract(org.omg.CORBA.Any)
meth public static double[] read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,double[])
meth public static void write(org.omg.CORBA.portable.OutputStream,double[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.DoubleSeqHolder
cons public init()
cons public init(double[])
fld public double[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.DynAny
intf org.omg.CORBA.Object
meth public abstract boolean get_boolean() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract boolean next()
meth public abstract boolean seek(int)
meth public abstract byte get_octet() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract char get_char() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract char get_wchar() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract double get_double() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract float get_float() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract int get_long() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract int get_ulong() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract java.io.Serializable get_val() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract java.lang.String get_string() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract java.lang.String get_wstring() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract long get_longlong() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract long get_ulonglong() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract org.omg.CORBA.Any get_any() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract org.omg.CORBA.Any to_any() throws org.omg.CORBA.DynAnyPackage.Invalid
meth public abstract org.omg.CORBA.DynAny copy()
meth public abstract org.omg.CORBA.DynAny current_component()
meth public abstract org.omg.CORBA.Object get_reference() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract org.omg.CORBA.TypeCode get_typecode() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract org.omg.CORBA.TypeCode type()
meth public abstract short get_short() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract short get_ushort() throws org.omg.CORBA.DynAnyPackage.TypeMismatch
meth public abstract void assign(org.omg.CORBA.DynAny) throws org.omg.CORBA.DynAnyPackage.Invalid
meth public abstract void destroy()
meth public abstract void from_any(org.omg.CORBA.Any) throws org.omg.CORBA.DynAnyPackage.Invalid
meth public abstract void insert_any(org.omg.CORBA.Any) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_boolean(boolean) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_char(char) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_double(double) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_float(float) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_long(int) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_longlong(long) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_octet(byte) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_reference(org.omg.CORBA.Object) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_short(short) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_string(java.lang.String) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_ulong(int) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_ulonglong(long) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_ushort(short) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_val(java.io.Serializable) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_wchar(char) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void insert_wstring(java.lang.String) throws org.omg.CORBA.DynAnyPackage.InvalidValue
meth public abstract void rewind()

CLSS public final org.omg.CORBA.DynAnyPackage.Invalid
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public final org.omg.CORBA.DynAnyPackage.InvalidSeq
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public final org.omg.CORBA.DynAnyPackage.InvalidValue
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public final org.omg.CORBA.DynAnyPackage.TypeMismatch
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract interface org.omg.CORBA.DynArray
intf org.omg.CORBA.DynAny
intf org.omg.CORBA.Object
meth public abstract org.omg.CORBA.Any[] get_elements()
meth public abstract void set_elements(org.omg.CORBA.Any[]) throws org.omg.CORBA.DynAnyPackage.InvalidSeq

CLSS public abstract interface org.omg.CORBA.DynEnum
intf org.omg.CORBA.DynAny
intf org.omg.CORBA.Object
meth public abstract int value_as_ulong()
meth public abstract java.lang.String value_as_string()
meth public abstract void value_as_string(java.lang.String)
meth public abstract void value_as_ulong(int)

CLSS public abstract interface org.omg.CORBA.DynFixed
intf org.omg.CORBA.DynAny
intf org.omg.CORBA.Object
meth public abstract byte[] get_value()
meth public abstract void set_value(byte[]) throws org.omg.CORBA.DynAnyPackage.InvalidValue

CLSS public abstract interface org.omg.CORBA.DynSequence
intf org.omg.CORBA.DynAny
intf org.omg.CORBA.Object
meth public abstract int length()
meth public abstract org.omg.CORBA.Any[] get_elements()
meth public abstract void length(int)
meth public abstract void set_elements(org.omg.CORBA.Any[]) throws org.omg.CORBA.DynAnyPackage.InvalidSeq

CLSS public abstract interface org.omg.CORBA.DynStruct
intf org.omg.CORBA.DynAny
intf org.omg.CORBA.Object
meth public abstract java.lang.String current_member_name()
meth public abstract org.omg.CORBA.NameValuePair[] get_members()
meth public abstract org.omg.CORBA.TCKind current_member_kind()
meth public abstract void set_members(org.omg.CORBA.NameValuePair[]) throws org.omg.CORBA.DynAnyPackage.InvalidSeq

CLSS public abstract interface org.omg.CORBA.DynUnion
intf org.omg.CORBA.DynAny
intf org.omg.CORBA.Object
meth public abstract boolean set_as_default()
meth public abstract java.lang.String member_name()
meth public abstract org.omg.CORBA.DynAny discriminator()
meth public abstract org.omg.CORBA.DynAny member()
meth public abstract org.omg.CORBA.TCKind discriminator_kind()
meth public abstract org.omg.CORBA.TCKind member_kind()
meth public abstract void member_name(java.lang.String)
meth public abstract void set_as_default(boolean)

CLSS public abstract interface org.omg.CORBA.DynValue
intf org.omg.CORBA.DynAny
intf org.omg.CORBA.Object
meth public abstract java.lang.String current_member_name()
meth public abstract org.omg.CORBA.NameValuePair[] get_members()
meth public abstract org.omg.CORBA.TCKind current_member_kind()
meth public abstract void set_members(org.omg.CORBA.NameValuePair[]) throws org.omg.CORBA.DynAnyPackage.InvalidSeq

CLSS public org.omg.CORBA.DynamicImplementation
cons public init()
meth public java.lang.String[] _ids()
meth public void invoke(org.omg.CORBA.ServerRequest)
supr org.omg.CORBA.portable.ObjectImpl

CLSS public abstract interface org.omg.CORBA.EnumDef
intf org.omg.CORBA.EnumDefOperations
intf org.omg.CORBA.TypedefDef
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.EnumDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.EnumDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.EnumDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.EnumDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.EnumDef unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.EnumDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.EnumDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.EnumDefHolder
cons public init()
cons public init(org.omg.CORBA.EnumDef)
fld public org.omg.CORBA.EnumDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.EnumDefOperations
intf org.omg.CORBA.TypedefDefOperations
meth public abstract java.lang.String[] members()
meth public abstract void members(java.lang.String[])

CLSS public abstract org.omg.CORBA.EnumDefPOA
cons public init()
intf org.omg.CORBA.EnumDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.EnumDef _this()
meth public org.omg.CORBA.EnumDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.EnumDefPOATie
cons public init(org.omg.CORBA.EnumDefOperations)
cons public init(org.omg.CORBA.EnumDefOperations,org.omg.PortableServer.POA)
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] members()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDefOperations _delegate()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.EnumDefOperations)
meth public void destroy()
meth public void id(java.lang.String)
meth public void members(java.lang.String[])
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.EnumDefPOA
hfds _impl,_poa

CLSS public abstract org.omg.CORBA.EnumMemberSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static java.lang.String[] extract(org.omg.CORBA.Any)
meth public static java.lang.String[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String[])
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.EnumMemberSeqHolder
cons public init()
cons public init(java.lang.String[])
fld public java.lang.String[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.Environment
cons public init()
meth public abstract java.lang.Exception exception()
meth public abstract void clear()
meth public abstract void exception(java.lang.Exception)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ExcDescriptionSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ExceptionDescription[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ExceptionDescription[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ExceptionDescription[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ExceptionDescription[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ExcDescriptionSeqHolder
cons public init()
cons public init(org.omg.CORBA.ExceptionDescription[])
fld public org.omg.CORBA.ExceptionDescription[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.ExceptionDef
intf org.omg.CORBA.Contained
intf org.omg.CORBA.Container
intf org.omg.CORBA.ExceptionDefOperations

CLSS public abstract org.omg.CORBA.ExceptionDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ExceptionDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ExceptionDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.ExceptionDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.ExceptionDef unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ExceptionDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ExceptionDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ExceptionDefHolder
cons public init()
cons public init(org.omg.CORBA.ExceptionDef)
fld public org.omg.CORBA.ExceptionDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.ExceptionDefOperations
intf org.omg.CORBA.ContainedOperations
intf org.omg.CORBA.ContainerOperations
meth public abstract org.omg.CORBA.StructMember[] members()
meth public abstract org.omg.CORBA.TypeCode type()
meth public abstract void members(org.omg.CORBA.StructMember[])

CLSS public abstract org.omg.CORBA.ExceptionDefPOA
cons public init()
intf org.omg.CORBA.ExceptionDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.ExceptionDef _this()
meth public org.omg.CORBA.ExceptionDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.ExceptionDefPOATie
cons public init(org.omg.CORBA.ExceptionDefOperations)
cons public init(org.omg.CORBA.ExceptionDefOperations,org.omg.PortableServer.POA)
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public org.omg.CORBA.Contained lookup(java.lang.String)
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.ExceptionDefOperations _delegate()
meth public org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.StructMember[] members()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.ExceptionDefOperations)
meth public void destroy()
meth public void id(java.lang.String)
meth public void members(org.omg.CORBA.StructMember[])
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.ExceptionDefPOA
hfds _impl,_poa

CLSS public abstract org.omg.CORBA.ExceptionDefSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ExceptionDef[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ExceptionDef[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ExceptionDef[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ExceptionDef[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ExceptionDefSeqHolder
cons public init()
cons public init(org.omg.CORBA.ExceptionDef[])
fld public org.omg.CORBA.ExceptionDef[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.ExceptionDescription
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.TypeCode)
fld public java.lang.String defined_in
fld public java.lang.String id
fld public java.lang.String name
fld public java.lang.String version
fld public org.omg.CORBA.TypeCode type
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ExceptionDescriptionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ExceptionDescription extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ExceptionDescription read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ExceptionDescription)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ExceptionDescription)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.ExceptionDescriptionHolder
cons public init()
cons public init(org.omg.CORBA.ExceptionDescription)
fld public org.omg.CORBA.ExceptionDescription value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ExceptionList
cons public init()
meth public abstract int count()
meth public abstract org.omg.CORBA.TypeCode item(int) throws org.omg.CORBA.Bounds
meth public abstract void add(org.omg.CORBA.TypeCode)
meth public abstract void remove(int) throws org.omg.CORBA.Bounds
supr java.lang.Object

CLSS public final org.omg.CORBA.FREE_MEM
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public abstract org.omg.CORBA.FieldNameHelper
cons public init()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.FixedHolder
cons public init()
cons public init(java.math.BigDecimal)
fld public java.math.BigDecimal value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.FloatHolder
cons public init()
cons public init(float)
fld public float value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.FloatSeqHelper
cons public init()
meth public static float[] extract(org.omg.CORBA.Any)
meth public static float[] read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,float[])
meth public static void write(org.omg.CORBA.portable.OutputStream,float[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.FloatSeqHolder
cons public init()
cons public init(float[])
fld public float[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.IDLType
intf org.omg.CORBA.IDLTypeOperations
intf org.omg.CORBA.IRObject
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.IDLTypeHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.IDLType extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.IDLType narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.IDLType read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.IDLType)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.IDLType)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.CORBA.IDLTypeOperations
intf org.omg.CORBA.IRObjectOperations
meth public abstract org.omg.CORBA.TypeCode type()

CLSS public final org.omg.CORBA.IMP_LIMIT
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.INITIALIZE
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.INTERNAL
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.INTF_REPOS
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.INVALID_ACTIVITY
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.INVALID_TRANSACTION
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.INV_FLAG
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.INV_IDENT
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.INV_OBJREF
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.INV_POLICY
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public abstract interface org.omg.CORBA.IRObject
intf org.omg.CORBA.IRObjectOperations
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract interface org.omg.CORBA.IRObjectOperations
meth public abstract org.omg.CORBA.DefinitionKind def_kind()
meth public abstract void destroy()

CLSS public abstract org.omg.CORBA.IdentifierHelper
cons public init()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.Initializer
cons public init()
cons public init(org.omg.CORBA.StructMember[])
fld public org.omg.CORBA.StructMember[] members
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.InitializerHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.Initializer extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.Initializer read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.Initializer)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.Initializer)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.InitializerHolder
cons public init()
cons public init(org.omg.CORBA.Initializer)
fld public org.omg.CORBA.Initializer value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.InitializerSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.Initializer[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.Initializer[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.Initializer[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.Initializer[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.InitializerSeqHolder
cons public init()
cons public init(org.omg.CORBA.Initializer[])
fld public org.omg.CORBA.Initializer[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.IntHolder
cons public init()
cons public init(int)
fld public int value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.InterfaceDef
intf org.omg.CORBA.Contained
intf org.omg.CORBA.Container
intf org.omg.CORBA.IDLType
intf org.omg.CORBA.InterfaceDefOperations

CLSS public abstract org.omg.CORBA.InterfaceDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.InterfaceDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.InterfaceDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.InterfaceDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.InterfaceDef unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.InterfaceDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.InterfaceDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.InterfaceDefHolder
cons public init()
cons public init(org.omg.CORBA.InterfaceDef)
fld public org.omg.CORBA.InterfaceDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.InterfaceDefOperations
intf org.omg.CORBA.ContainedOperations
intf org.omg.CORBA.ContainerOperations
intf org.omg.CORBA.IDLTypeOperations
meth public abstract boolean is_a(java.lang.String)
meth public abstract boolean is_abstract()
meth public abstract org.omg.CORBA.AttributeDef create_attribute(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.AttributeMode)
meth public abstract org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription describe_interface()
meth public abstract org.omg.CORBA.InterfaceDef[] base_interfaces()
meth public abstract org.omg.CORBA.OperationDef create_operation(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.OperationMode,org.omg.CORBA.ParameterDescription[],org.omg.CORBA.ExceptionDef[],java.lang.String[])
meth public abstract void base_interfaces(org.omg.CORBA.InterfaceDef[])
meth public abstract void is_abstract(boolean)

CLSS public abstract org.omg.CORBA.InterfaceDefPOA
cons public init()
intf org.omg.CORBA.InterfaceDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.InterfaceDef _this()
meth public org.omg.CORBA.InterfaceDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.InterfaceDefPOATie
cons public init(org.omg.CORBA.InterfaceDefOperations)
cons public init(org.omg.CORBA.InterfaceDefOperations,org.omg.PortableServer.POA)
meth public boolean is_a(java.lang.String)
meth public boolean is_abstract()
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.AttributeDef create_attribute(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.AttributeMode)
meth public org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public org.omg.CORBA.Contained lookup(java.lang.String)
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public org.omg.CORBA.InterfaceDefOperations _delegate()
meth public org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription describe_interface()
meth public org.omg.CORBA.InterfaceDef[] base_interfaces()
meth public org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.OperationDef create_operation(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.OperationMode,org.omg.CORBA.ParameterDescription[],org.omg.CORBA.ExceptionDef[],java.lang.String[])
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.InterfaceDefOperations)
meth public void base_interfaces(org.omg.CORBA.InterfaceDef[])
meth public void destroy()
meth public void id(java.lang.String)
meth public void is_abstract(boolean)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.InterfaceDefPOA
hfds _impl,_poa

CLSS public final org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.OperationDescription[],org.omg.CORBA.AttributeDescription[],java.lang.String[],org.omg.CORBA.TypeCode)
fld public boolean is_abstract
fld public java.lang.String defined_in
fld public java.lang.String id
fld public java.lang.String name
fld public java.lang.String version
fld public java.lang.String[] base_interfaces
fld public org.omg.CORBA.AttributeDescription[] attributes
fld public org.omg.CORBA.OperationDescription[] operations
fld public org.omg.CORBA.TypeCode type
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescriptionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescriptionHolder
cons public init()
cons public init(org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription)
fld public org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.InterfaceDefSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.InterfaceDef[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.InterfaceDef[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.InterfaceDef[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.InterfaceDef[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.InterfaceDefSeqHolder
cons public init()
cons public init(org.omg.CORBA.InterfaceDef[])
fld public org.omg.CORBA.InterfaceDef[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.InterfaceDescription
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
fld public java.lang.String defined_in
fld public java.lang.String id
fld public java.lang.String name
fld public java.lang.String version
fld public java.lang.String[] base_interfaces
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.InterfaceDescriptionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.InterfaceDescription extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.InterfaceDescription read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.InterfaceDescription)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.InterfaceDescription)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.InterfaceDescriptionHolder
cons public init()
cons public init(org.omg.CORBA.InterfaceDescription)
fld public org.omg.CORBA.InterfaceDescription value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.CORBA.LocalObject
cons public init()
intf org.omg.CORBA.Object
meth public boolean _is_a(java.lang.String)
meth public boolean _is_equivalent(org.omg.CORBA.Object)
meth public boolean _is_local()
meth public boolean _non_existent()
meth public boolean validate_connection()
meth public int _hash(int)
meth public org.omg.CORBA.DomainManager[] _get_domain_managers()
meth public org.omg.CORBA.ORB _orb()
meth public org.omg.CORBA.Object _duplicate()
meth public org.omg.CORBA.Object _get_interface()
meth public org.omg.CORBA.Object _get_interface_def()
meth public org.omg.CORBA.Object _set_policy_override(org.omg.CORBA.Policy[],org.omg.CORBA.SetOverrideType)
meth public org.omg.CORBA.Policy _get_policy(int)
meth public org.omg.CORBA.Request _create_request(org.omg.CORBA.Context,java.lang.String,org.omg.CORBA.NVList,org.omg.CORBA.NamedValue)
meth public org.omg.CORBA.Request _create_request(org.omg.CORBA.Context,java.lang.String,org.omg.CORBA.NVList,org.omg.CORBA.NamedValue,org.omg.CORBA.ExceptionList,org.omg.CORBA.ContextList)
meth public org.omg.CORBA.Request _request(java.lang.String)
meth public org.omg.CORBA.portable.InputStream _invoke(org.omg.CORBA.portable.OutputStream) throws org.omg.CORBA.portable.ApplicationException,org.omg.CORBA.portable.RemarshalException
meth public org.omg.CORBA.portable.OutputStream _request(java.lang.String,boolean)
meth public org.omg.CORBA.portable.ServantObject _servant_preinvoke(java.lang.String,java.lang.Class)
meth public void _release()
meth public void _releaseReply(org.omg.CORBA.portable.InputStream)
meth public void _servant_postinvoke(org.omg.CORBA.portable.ServantObject)
supr java.lang.Object
hfds reason

CLSS public final org.omg.CORBA.LongHolder
cons public init()
cons public init(long)
fld public long value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.LongLongSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static long[] extract(org.omg.CORBA.Any)
meth public static long[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,long[])
meth public static void write(org.omg.CORBA.portable.OutputStream,long[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.LongLongSeqHolder
cons public init()
cons public init(long[])
fld public long[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.LongSeqHelper
cons public init()
meth public static int[] extract(org.omg.CORBA.Any)
meth public static int[] read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,int[])
meth public static void write(org.omg.CORBA.portable.OutputStream,int[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.LongSeqHolder
cons public init()
cons public init(int[])
fld public int[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.MARSHAL
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public abstract interface org.omg.CORBA.ModuleDef
intf org.omg.CORBA.Contained
intf org.omg.CORBA.Container
intf org.omg.CORBA.ModuleDefOperations

CLSS public abstract org.omg.CORBA.ModuleDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ModuleDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ModuleDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.ModuleDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.ModuleDef unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ModuleDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ModuleDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ModuleDefHolder
cons public init()
cons public init(org.omg.CORBA.ModuleDef)
fld public org.omg.CORBA.ModuleDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.ModuleDefOperations
intf org.omg.CORBA.ContainedOperations
intf org.omg.CORBA.ContainerOperations

CLSS public abstract org.omg.CORBA.ModuleDefPOA
cons public init()
intf org.omg.CORBA.ModuleDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.ModuleDef _this()
meth public org.omg.CORBA.ModuleDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.ModuleDefPOATie
cons public init(org.omg.CORBA.ModuleDefOperations)
cons public init(org.omg.CORBA.ModuleDefOperations,org.omg.PortableServer.POA)
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public org.omg.CORBA.Contained lookup(java.lang.String)
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.ModuleDefOperations _delegate()
meth public org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.ModuleDefOperations)
meth public void destroy()
meth public void id(java.lang.String)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.ModuleDefPOA
hfds _impl,_poa

CLSS public final org.omg.CORBA.ModuleDescription
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
fld public java.lang.String defined_in
fld public java.lang.String id
fld public java.lang.String name
fld public java.lang.String version
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ModuleDescriptionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ModuleDescription extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ModuleDescription read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ModuleDescription)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ModuleDescription)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.ModuleDescriptionHolder
cons public init()
cons public init(org.omg.CORBA.ModuleDescription)
fld public org.omg.CORBA.ModuleDescription value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.NO_IMPLEMENT
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.NO_MEMORY
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.NO_PERMISSION
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.NO_RESOURCES
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.NO_RESPONSE
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public abstract org.omg.CORBA.NVList
cons public init()
meth public abstract int count()
meth public abstract org.omg.CORBA.NamedValue add(int)
meth public abstract org.omg.CORBA.NamedValue add_item(java.lang.String,int)
meth public abstract org.omg.CORBA.NamedValue add_value(java.lang.String,org.omg.CORBA.Any,int)
meth public abstract org.omg.CORBA.NamedValue item(int) throws org.omg.CORBA.Bounds
meth public abstract void remove(int) throws org.omg.CORBA.Bounds
supr java.lang.Object

CLSS public final org.omg.CORBA.NameValuePair
cons public init()
cons public init(java.lang.String,org.omg.CORBA.Any)
fld public java.lang.String id
fld public org.omg.CORBA.Any value
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.NameValuePairHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.NameValuePair extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.NameValuePair read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.NameValuePair)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.NameValuePair)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public abstract org.omg.CORBA.NamedValue
cons public init()
meth public abstract int flags()
meth public abstract java.lang.String name()
meth public abstract org.omg.CORBA.Any value()
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.NativeDef
intf org.omg.CORBA.NativeDefOperations
intf org.omg.CORBA.TypedefDef
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.NativeDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.NativeDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.NativeDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.NativeDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.NativeDef unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.NativeDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.NativeDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.NativeDefHolder
cons public init()
cons public init(org.omg.CORBA.NativeDef)
fld public org.omg.CORBA.NativeDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.NativeDefOperations
intf org.omg.CORBA.TypedefDefOperations

CLSS public abstract org.omg.CORBA.NativeDefPOA
cons public init()
intf org.omg.CORBA.NativeDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.NativeDef _this()
meth public org.omg.CORBA.NativeDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.NativeDefPOATie
cons public init(org.omg.CORBA.NativeDefOperations)
cons public init(org.omg.CORBA.NativeDefOperations,org.omg.PortableServer.POA)
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.NativeDefOperations _delegate()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.NativeDefOperations)
meth public void destroy()
meth public void id(java.lang.String)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.NativeDefPOA
hfds _impl,_poa

CLSS public final org.omg.CORBA.OBJECT_NOT_EXIST
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.OBJ_ADAPTER
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public abstract interface org.omg.CORBA.OMGVMCID
fld public final static int value = 1330446336

CLSS public abstract org.omg.CORBA.ORB
cons public init()
meth protected abstract void set_parameters(java.applet.Applet,java.util.Properties)
meth protected abstract void set_parameters(java.lang.String[],java.util.Properties)
meth public abstract boolean poll_next_response()
meth public abstract java.lang.String object_to_string(org.omg.CORBA.Object)
meth public abstract java.lang.String[] list_initial_services()
meth public abstract org.omg.CORBA.Any create_any()
meth public abstract org.omg.CORBA.Context get_default_context()
meth public abstract org.omg.CORBA.ContextList create_context_list()
meth public abstract org.omg.CORBA.Environment create_environment()
meth public abstract org.omg.CORBA.ExceptionList create_exception_list()
meth public abstract org.omg.CORBA.NVList create_list(int)
meth public abstract org.omg.CORBA.NamedValue create_named_value(java.lang.String,org.omg.CORBA.Any,int)
meth public abstract org.omg.CORBA.Object resolve_initial_references(java.lang.String) throws org.omg.CORBA.ORBPackage.InvalidName
meth public abstract org.omg.CORBA.Object string_to_object(java.lang.String)
meth public abstract org.omg.CORBA.Request get_next_response() throws org.omg.CORBA.WrongTransaction
meth public abstract org.omg.CORBA.TypeCode create_alias_tc(java.lang.String,java.lang.String,org.omg.CORBA.TypeCode)
meth public abstract org.omg.CORBA.TypeCode create_array_tc(int,org.omg.CORBA.TypeCode)
meth public abstract org.omg.CORBA.TypeCode create_enum_tc(java.lang.String,java.lang.String,java.lang.String[])
meth public abstract org.omg.CORBA.TypeCode create_exception_tc(java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public abstract org.omg.CORBA.TypeCode create_interface_tc(java.lang.String,java.lang.String)
meth public abstract org.omg.CORBA.TypeCode create_recursive_sequence_tc(int,int)
meth public abstract org.omg.CORBA.TypeCode create_sequence_tc(int,org.omg.CORBA.TypeCode)
meth public abstract org.omg.CORBA.TypeCode create_string_tc(int)
meth public abstract org.omg.CORBA.TypeCode create_struct_tc(java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public abstract org.omg.CORBA.TypeCode create_union_tc(java.lang.String,java.lang.String,org.omg.CORBA.TypeCode,org.omg.CORBA.UnionMember[])
meth public abstract org.omg.CORBA.TypeCode create_wstring_tc(int)
meth public abstract org.omg.CORBA.TypeCode get_primitive_tc(org.omg.CORBA.TCKind)
meth public abstract org.omg.CORBA.portable.OutputStream create_output_stream()
meth public abstract void send_multiple_requests_deferred(org.omg.CORBA.Request[])
meth public abstract void send_multiple_requests_oneway(org.omg.CORBA.Request[])
meth public boolean get_service_information(short,org.omg.CORBA.ServiceInformationHolder)
meth public boolean work_pending()
meth public org.omg.CORBA.Current get_current()
meth public org.omg.CORBA.DynAny create_basic_dyn_any(org.omg.CORBA.TypeCode) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode
meth public org.omg.CORBA.DynAny create_dyn_any(org.omg.CORBA.Any)
meth public org.omg.CORBA.DynArray create_dyn_array(org.omg.CORBA.TypeCode) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode
meth public org.omg.CORBA.DynEnum create_dyn_enum(org.omg.CORBA.TypeCode) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode
meth public org.omg.CORBA.DynSequence create_dyn_sequence(org.omg.CORBA.TypeCode) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode
meth public org.omg.CORBA.DynStruct create_dyn_struct(org.omg.CORBA.TypeCode) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode
meth public org.omg.CORBA.DynUnion create_dyn_union(org.omg.CORBA.TypeCode) throws org.omg.CORBA.ORBPackage.InconsistentTypeCode
meth public org.omg.CORBA.NVList create_operation_list(org.omg.CORBA.Object)
meth public org.omg.CORBA.Policy create_policy(int,org.omg.CORBA.Any) throws org.omg.CORBA.PolicyError
meth public org.omg.CORBA.TypeCode create_abstract_interface_tc(java.lang.String,java.lang.String)
meth public org.omg.CORBA.TypeCode create_fixed_tc(short,short)
meth public org.omg.CORBA.TypeCode create_native_tc(java.lang.String,java.lang.String)
meth public org.omg.CORBA.TypeCode create_recursive_tc(java.lang.String)
meth public org.omg.CORBA.TypeCode create_value_box_tc(java.lang.String,java.lang.String,org.omg.CORBA.TypeCode)
meth public org.omg.CORBA.TypeCode create_value_tc(java.lang.String,java.lang.String,short,org.omg.CORBA.TypeCode,org.omg.CORBA.ValueMember[])
meth public static org.omg.CORBA.ORB init()
meth public static org.omg.CORBA.ORB init(java.applet.Applet,java.util.Properties)
meth public static org.omg.CORBA.ORB init(java.lang.String[],java.util.Properties)
meth public void connect(org.omg.CORBA.Object)
meth public void destroy()
meth public void disconnect(org.omg.CORBA.Object)
meth public void perform_work()
meth public void run()
meth public void shutdown(boolean)
supr java.lang.Object
hfds ORBClassKey,ORBSingletonClassKey,defaultORB,defaultORBSingleton,singleton

CLSS public final org.omg.CORBA.ORBPackage.InconsistentTypeCode
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public final org.omg.CORBA.ORBPackage.InvalidName
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract interface org.omg.CORBA.Object
meth public abstract boolean _is_a(java.lang.String)
meth public abstract boolean _is_equivalent(org.omg.CORBA.Object)
meth public abstract boolean _non_existent()
meth public abstract int _hash(int)
meth public abstract org.omg.CORBA.DomainManager[] _get_domain_managers()
meth public abstract org.omg.CORBA.Object _duplicate()
meth public abstract org.omg.CORBA.Object _get_interface_def()
meth public abstract org.omg.CORBA.Object _set_policy_override(org.omg.CORBA.Policy[],org.omg.CORBA.SetOverrideType)
meth public abstract org.omg.CORBA.Policy _get_policy(int)
meth public abstract org.omg.CORBA.Request _create_request(org.omg.CORBA.Context,java.lang.String,org.omg.CORBA.NVList,org.omg.CORBA.NamedValue)
meth public abstract org.omg.CORBA.Request _create_request(org.omg.CORBA.Context,java.lang.String,org.omg.CORBA.NVList,org.omg.CORBA.NamedValue,org.omg.CORBA.ExceptionList,org.omg.CORBA.ContextList)
meth public abstract org.omg.CORBA.Request _request(java.lang.String)
meth public abstract void _release()

CLSS public abstract org.omg.CORBA.ObjectHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.Object extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.Object read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.Object)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.Object)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ObjectHolder
cons public init()
cons public init(org.omg.CORBA.Object)
fld public org.omg.CORBA.Object value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.OctetSeqHelper
cons public init()
meth public static byte[] extract(org.omg.CORBA.Any)
meth public static byte[] read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,byte[])
meth public static void write(org.omg.CORBA.portable.OutputStream,byte[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.OctetSeqHolder
cons public init()
cons public init(byte[])
fld public byte[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.OpDescriptionSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.OperationDescription[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.OperationDescription[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.OperationDescription[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.OperationDescription[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.OpDescriptionSeqHolder
cons public init()
cons public init(org.omg.CORBA.OperationDescription[])
fld public org.omg.CORBA.OperationDescription[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.OperationDef
intf org.omg.CORBA.Contained
intf org.omg.CORBA.OperationDefOperations
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.OperationDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.OperationDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.OperationDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.OperationDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.OperationDef unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.OperationDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.OperationDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.OperationDefHolder
cons public init()
cons public init(org.omg.CORBA.OperationDef)
fld public org.omg.CORBA.OperationDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.OperationDefOperations
intf org.omg.CORBA.ContainedOperations
meth public abstract java.lang.String[] contexts()
meth public abstract org.omg.CORBA.ExceptionDef[] exceptions()
meth public abstract org.omg.CORBA.IDLType result_def()
meth public abstract org.omg.CORBA.OperationMode mode()
meth public abstract org.omg.CORBA.ParameterDescription[] params()
meth public abstract org.omg.CORBA.TypeCode result()
meth public abstract void contexts(java.lang.String[])
meth public abstract void exceptions(org.omg.CORBA.ExceptionDef[])
meth public abstract void mode(org.omg.CORBA.OperationMode)
meth public abstract void params(org.omg.CORBA.ParameterDescription[])
meth public abstract void result_def(org.omg.CORBA.IDLType)

CLSS public abstract org.omg.CORBA.OperationDefPOA
cons public init()
intf org.omg.CORBA.OperationDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.OperationDef _this()
meth public org.omg.CORBA.OperationDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.OperationDefPOATie
cons public init(org.omg.CORBA.OperationDefOperations)
cons public init(org.omg.CORBA.OperationDefOperations,org.omg.PortableServer.POA)
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] contexts()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.ExceptionDef[] exceptions()
meth public org.omg.CORBA.IDLType result_def()
meth public org.omg.CORBA.OperationDefOperations _delegate()
meth public org.omg.CORBA.OperationMode mode()
meth public org.omg.CORBA.ParameterDescription[] params()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.TypeCode result()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.OperationDefOperations)
meth public void contexts(java.lang.String[])
meth public void destroy()
meth public void exceptions(org.omg.CORBA.ExceptionDef[])
meth public void id(java.lang.String)
meth public void mode(org.omg.CORBA.OperationMode)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void params(org.omg.CORBA.ParameterDescription[])
meth public void result_def(org.omg.CORBA.IDLType)
meth public void version(java.lang.String)
supr org.omg.CORBA.OperationDefPOA
hfds _impl,_poa

CLSS public final org.omg.CORBA.OperationDescription
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.TypeCode,org.omg.CORBA.OperationMode,java.lang.String[],org.omg.CORBA.ParameterDescription[],org.omg.CORBA.ExceptionDescription[])
fld public java.lang.String defined_in
fld public java.lang.String id
fld public java.lang.String name
fld public java.lang.String version
fld public java.lang.String[] contexts
fld public org.omg.CORBA.ExceptionDescription[] exceptions
fld public org.omg.CORBA.OperationMode mode
fld public org.omg.CORBA.ParameterDescription[] parameters
fld public org.omg.CORBA.TypeCode result
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.OperationDescriptionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.OperationDescription extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.OperationDescription read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.OperationDescription)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.OperationDescription)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.OperationDescriptionHolder
cons public init()
cons public init(org.omg.CORBA.OperationDescription)
fld public org.omg.CORBA.OperationDescription value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.CORBA.OperationMode
cons protected init(int)
fld public final static int _OP_NORMAL = 0
fld public final static int _OP_ONEWAY = 1
fld public final static org.omg.CORBA.OperationMode OP_NORMAL
fld public final static org.omg.CORBA.OperationMode OP_ONEWAY
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.CORBA.OperationMode from_int(int)
supr java.lang.Object
hfds __array,__size,__value

CLSS public abstract org.omg.CORBA.OperationModeHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.OperationMode extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.OperationMode read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.OperationMode)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.OperationMode)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.OperationModeHolder
cons public init()
cons public init(org.omg.CORBA.OperationMode)
fld public org.omg.CORBA.OperationMode value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.PERSIST_STORE
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public abstract interface org.omg.CORBA.PRIVATE_MEMBER
fld public final static short value = 0

CLSS public abstract interface org.omg.CORBA.PUBLIC_MEMBER
fld public final static short value = 1

CLSS public abstract org.omg.CORBA.ParDescriptionSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ParameterDescription[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ParameterDescription[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ParameterDescription[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ParameterDescription[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ParDescriptionSeqHolder
cons public init()
cons public init(org.omg.CORBA.ParameterDescription[])
fld public org.omg.CORBA.ParameterDescription[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.ParameterDescription
cons public init()
cons public init(java.lang.String,org.omg.CORBA.TypeCode,org.omg.CORBA.IDLType,org.omg.CORBA.ParameterMode)
fld public java.lang.String name
fld public org.omg.CORBA.IDLType type_def
fld public org.omg.CORBA.ParameterMode mode
fld public org.omg.CORBA.TypeCode type
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ParameterDescriptionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ParameterDescription extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ParameterDescription read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ParameterDescription)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ParameterDescription)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.ParameterDescriptionHolder
cons public init()
cons public init(org.omg.CORBA.ParameterDescription)
fld public org.omg.CORBA.ParameterDescription value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.CORBA.ParameterMode
cons protected init(int)
fld public final static int _PARAM_IN = 0
fld public final static int _PARAM_INOUT = 2
fld public final static int _PARAM_OUT = 1
fld public final static org.omg.CORBA.ParameterMode PARAM_IN
fld public final static org.omg.CORBA.ParameterMode PARAM_INOUT
fld public final static org.omg.CORBA.ParameterMode PARAM_OUT
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.CORBA.ParameterMode from_int(int)
supr java.lang.Object
hfds __array,__size,__value

CLSS public abstract org.omg.CORBA.ParameterModeHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ParameterMode extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ParameterMode read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ParameterMode)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ParameterMode)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ParameterModeHolder
cons public init()
cons public init(org.omg.CORBA.ParameterMode)
fld public org.omg.CORBA.ParameterMode value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.Policy
intf org.omg.CORBA.Object
intf org.omg.CORBA.PolicyOperations
intf org.omg.CORBA.portable.IDLEntity

CLSS public final org.omg.CORBA.PolicyError
cons public init()
cons public init(java.lang.String,short)
cons public init(short)
fld public short reason
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CORBA.PolicyErrorCodeHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static short extract(org.omg.CORBA.Any)
meth public static short read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,short)
meth public static void write(org.omg.CORBA.portable.OutputStream,short)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract org.omg.CORBA.PolicyErrorHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.PolicyError extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.PolicyError read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.PolicyError)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.PolicyError)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.PolicyErrorHolder
cons public init()
cons public init(org.omg.CORBA.PolicyError)
fld public org.omg.CORBA.PolicyError value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.PolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.Policy extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.Policy narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.Policy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.Policy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.Policy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.PolicyHolder
cons public init()
cons public init(org.omg.CORBA.Policy)
fld public org.omg.CORBA.Policy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.PolicyListHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.Policy[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.Policy[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.Policy[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.Policy[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.PolicyListHolder
cons public init()
cons public init(org.omg.CORBA.Policy[])
fld public org.omg.CORBA.Policy[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.PolicyOperations
meth public abstract int policy_type()
meth public abstract org.omg.CORBA.Policy copy()
meth public abstract void destroy()

CLSS public abstract org.omg.CORBA.PolicyTypeHelper
cons public init()
meth public static int extract(org.omg.CORBA.Any)
meth public static int read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,int)
meth public static void write(org.omg.CORBA.portable.OutputStream,int)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.CORBA.PrimitiveDef
intf org.omg.CORBA.IDLType
intf org.omg.CORBA.PrimitiveDefOperations
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.PrimitiveDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.PrimitiveDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.PrimitiveDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.PrimitiveDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.PrimitiveDef unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.PrimitiveDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.PrimitiveDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.PrimitiveDefHolder
cons public init()
cons public init(org.omg.CORBA.PrimitiveDef)
fld public org.omg.CORBA.PrimitiveDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.PrimitiveDefOperations
intf org.omg.CORBA.IDLTypeOperations
meth public abstract org.omg.CORBA.PrimitiveKind kind()

CLSS public abstract org.omg.CORBA.PrimitiveDefPOA
cons public init()
intf org.omg.CORBA.PrimitiveDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.PrimitiveDef _this()
meth public org.omg.CORBA.PrimitiveDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.PrimitiveDefPOATie
cons public init(org.omg.CORBA.PrimitiveDefOperations)
cons public init(org.omg.CORBA.PrimitiveDefOperations,org.omg.PortableServer.POA)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.PrimitiveDefOperations _delegate()
meth public org.omg.CORBA.PrimitiveKind kind()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.PrimitiveDefOperations)
meth public void destroy()
supr org.omg.CORBA.PrimitiveDefPOA
hfds _impl,_poa

CLSS public org.omg.CORBA.PrimitiveKind
cons protected init(int)
fld public final static int _pk_Principal = 13
fld public final static int _pk_TypeCode = 12
fld public final static int _pk_any = 11
fld public final static int _pk_boolean = 8
fld public final static int _pk_char = 9
fld public final static int _pk_double = 7
fld public final static int _pk_float = 6
fld public final static int _pk_long = 3
fld public final static int _pk_null = 0
fld public final static int _pk_objref = 15
fld public final static int _pk_octet = 10
fld public final static int _pk_short = 2
fld public final static int _pk_string = 14
fld public final static int _pk_ulong = 5
fld public final static int _pk_ushort = 4
fld public final static int _pk_void = 1
fld public final static org.omg.CORBA.PrimitiveKind pk_Principal
fld public final static org.omg.CORBA.PrimitiveKind pk_TypeCode
fld public final static org.omg.CORBA.PrimitiveKind pk_any
fld public final static org.omg.CORBA.PrimitiveKind pk_boolean
fld public final static org.omg.CORBA.PrimitiveKind pk_char
fld public final static org.omg.CORBA.PrimitiveKind pk_double
fld public final static org.omg.CORBA.PrimitiveKind pk_float
fld public final static org.omg.CORBA.PrimitiveKind pk_long
fld public final static org.omg.CORBA.PrimitiveKind pk_null
fld public final static org.omg.CORBA.PrimitiveKind pk_objref
fld public final static org.omg.CORBA.PrimitiveKind pk_octet
fld public final static org.omg.CORBA.PrimitiveKind pk_short
fld public final static org.omg.CORBA.PrimitiveKind pk_string
fld public final static org.omg.CORBA.PrimitiveKind pk_ulong
fld public final static org.omg.CORBA.PrimitiveKind pk_ushort
fld public final static org.omg.CORBA.PrimitiveKind pk_void
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.CORBA.PrimitiveKind from_int(int)
supr java.lang.Object
hfds __array,__size,__value

CLSS public abstract org.omg.CORBA.PrimitiveKindHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.PrimitiveKind extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.PrimitiveKind read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.PrimitiveKind)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.PrimitiveKind)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.PrimitiveKindHolder
cons public init()
cons public init(org.omg.CORBA.PrimitiveKind)
fld public org.omg.CORBA.PrimitiveKind value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.CORBA.Principal
cons public init()
meth public byte[] name()
meth public void name(byte[])
supr java.lang.Object

CLSS public final org.omg.CORBA.PrincipalHolder
cons public init()
cons public init(org.omg.CORBA.Principal)
fld public org.omg.CORBA.Principal value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.REBIND
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public abstract interface org.omg.CORBA.Repository
intf org.omg.CORBA.Container
intf org.omg.CORBA.RepositoryOperations
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.RepositoryHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.Repository extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.Repository narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.Repository read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.Repository unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.Repository)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.Repository)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.RepositoryHolder
cons public init()
cons public init(org.omg.CORBA.Repository)
fld public org.omg.CORBA.Repository value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.RepositoryIdHelper
cons public init()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract org.omg.CORBA.RepositoryIdSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static java.lang.String[] extract(org.omg.CORBA.Any)
meth public static java.lang.String[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String[])
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.RepositoryIdSeqHolder
cons public init()
cons public init(java.lang.String[])
fld public java.lang.String[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.RepositoryOperations
intf org.omg.CORBA.ContainerOperations
meth public abstract org.omg.CORBA.ArrayDef create_array(int,org.omg.CORBA.IDLType)
meth public abstract org.omg.CORBA.Contained lookup_id(java.lang.String)
meth public abstract org.omg.CORBA.PrimitiveDef get_primitive(org.omg.CORBA.PrimitiveKind)
meth public abstract org.omg.CORBA.SequenceDef create_sequence(int,org.omg.CORBA.IDLType)
meth public abstract org.omg.CORBA.StringDef create_string(int)

CLSS public abstract org.omg.CORBA.RepositoryPOA
cons public init()
intf org.omg.CORBA.RepositoryOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.Repository _this()
meth public org.omg.CORBA.Repository _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.RepositoryPOATie
cons public init(org.omg.CORBA.RepositoryOperations)
cons public init(org.omg.CORBA.RepositoryOperations,org.omg.PortableServer.POA)
meth public org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ArrayDef create_array(int,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public org.omg.CORBA.Contained lookup(java.lang.String)
meth public org.omg.CORBA.Contained lookup_id(java.lang.String)
meth public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.PrimitiveDef get_primitive(org.omg.CORBA.PrimitiveKind)
meth public org.omg.CORBA.RepositoryOperations _delegate()
meth public org.omg.CORBA.SequenceDef create_sequence(int,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.StringDef create_string(int)
meth public org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.RepositoryOperations)
meth public void destroy()
supr org.omg.CORBA.RepositoryPOA
hfds _impl,_poa

CLSS public abstract org.omg.CORBA.Request
cons public init()
meth public abstract boolean poll_response()
meth public abstract java.lang.String operation()
meth public abstract org.omg.CORBA.Any add_in_arg()
meth public abstract org.omg.CORBA.Any add_inout_arg()
meth public abstract org.omg.CORBA.Any add_named_in_arg(java.lang.String)
meth public abstract org.omg.CORBA.Any add_named_inout_arg(java.lang.String)
meth public abstract org.omg.CORBA.Any add_named_out_arg(java.lang.String)
meth public abstract org.omg.CORBA.Any add_out_arg()
meth public abstract org.omg.CORBA.Any return_value()
meth public abstract org.omg.CORBA.Context ctx()
meth public abstract org.omg.CORBA.ContextList contexts()
meth public abstract org.omg.CORBA.Environment env()
meth public abstract org.omg.CORBA.ExceptionList exceptions()
meth public abstract org.omg.CORBA.NVList arguments()
meth public abstract org.omg.CORBA.NamedValue result()
meth public abstract org.omg.CORBA.Object target()
meth public abstract void ctx(org.omg.CORBA.Context)
meth public abstract void get_response() throws org.omg.CORBA.WrongTransaction
meth public abstract void invoke()
meth public abstract void send_deferred()
meth public abstract void send_oneway()
meth public abstract void set_return_type(org.omg.CORBA.TypeCode)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ScopedNameHelper
cons public init()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.CORBA.SequenceDef
intf org.omg.CORBA.IDLType
intf org.omg.CORBA.SequenceDefOperations
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.SequenceDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.SequenceDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.SequenceDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.SequenceDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.SequenceDef unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.SequenceDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.SequenceDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.SequenceDefHolder
cons public init()
cons public init(org.omg.CORBA.SequenceDef)
fld public org.omg.CORBA.SequenceDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.SequenceDefOperations
intf org.omg.CORBA.IDLTypeOperations
meth public abstract int bound()
meth public abstract org.omg.CORBA.IDLType element_type_def()
meth public abstract org.omg.CORBA.TypeCode element_type()
meth public abstract void bound(int)
meth public abstract void element_type_def(org.omg.CORBA.IDLType)

CLSS public abstract org.omg.CORBA.SequenceDefPOA
cons public init()
intf org.omg.CORBA.SequenceDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.SequenceDef _this()
meth public org.omg.CORBA.SequenceDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.SequenceDefPOATie
cons public init(org.omg.CORBA.SequenceDefOperations)
cons public init(org.omg.CORBA.SequenceDefOperations,org.omg.PortableServer.POA)
meth public int bound()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.IDLType element_type_def()
meth public org.omg.CORBA.SequenceDefOperations _delegate()
meth public org.omg.CORBA.TypeCode element_type()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.SequenceDefOperations)
meth public void bound(int)
meth public void destroy()
meth public void element_type_def(org.omg.CORBA.IDLType)
supr org.omg.CORBA.SequenceDefPOA
hfds _impl,_poa

CLSS public abstract org.omg.CORBA.ServerRequest
cons public init()
meth public abstract org.omg.CORBA.Context ctx()
meth public java.lang.String op_name()
meth public java.lang.String operation()
meth public void arguments(org.omg.CORBA.NVList)
meth public void except(org.omg.CORBA.Any)
meth public void params(org.omg.CORBA.NVList)
meth public void result(org.omg.CORBA.Any)
meth public void set_exception(org.omg.CORBA.Any)
meth public void set_result(org.omg.CORBA.Any)
supr java.lang.Object

CLSS public final org.omg.CORBA.ServiceDetail
cons public init()
cons public init(int,byte[])
fld public byte[] service_detail
fld public int service_detail_type
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ServiceDetailHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ServiceDetail extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ServiceDetail read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ServiceDetail)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ServiceDetail)
supr java.lang.Object
hfds _tc

CLSS public final org.omg.CORBA.ServiceInformation
cons public init()
cons public init(int[],org.omg.CORBA.ServiceDetail[])
fld public int[] service_options
fld public org.omg.CORBA.ServiceDetail[] service_details
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ServiceInformationHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.ServiceInformation extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ServiceInformation read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ServiceInformation)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ServiceInformation)
supr java.lang.Object
hfds _tc

CLSS public final org.omg.CORBA.ServiceInformationHolder
cons public init()
cons public init(org.omg.CORBA.ServiceInformation)
fld public org.omg.CORBA.ServiceInformation value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.CORBA.SetOverrideType
cons protected init(int)
fld public final static int _ADD_OVERRIDE = 1
fld public final static int _SET_OVERRIDE = 0
fld public final static org.omg.CORBA.SetOverrideType ADD_OVERRIDE
fld public final static org.omg.CORBA.SetOverrideType SET_OVERRIDE
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.CORBA.SetOverrideType from_int(int)
supr java.lang.Object
hfds _value

CLSS public abstract org.omg.CORBA.SetOverrideTypeHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.SetOverrideType extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.SetOverrideType read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.SetOverrideType)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.SetOverrideType)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ShortHolder
cons public init()
cons public init(short)
fld public short value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ShortSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static short[] extract(org.omg.CORBA.Any)
meth public static short[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,short[])
meth public static void write(org.omg.CORBA.portable.OutputStream,short[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ShortSeqHolder
cons public init()
cons public init(short[])
fld public short[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.StringDef
intf org.omg.CORBA.IDLType
intf org.omg.CORBA.StringDefOperations
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.StringDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.StringDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.StringDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.StringDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.StringDef unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.StringDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.StringDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.StringDefHolder
cons public init()
cons public init(org.omg.CORBA.StringDef)
fld public org.omg.CORBA.StringDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.StringDefOperations
intf org.omg.CORBA.IDLTypeOperations
meth public abstract int bound()
meth public abstract void bound(int)

CLSS public abstract org.omg.CORBA.StringDefPOA
cons public init()
intf org.omg.CORBA.StringDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.StringDef _this()
meth public org.omg.CORBA.StringDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.StringDefPOATie
cons public init(org.omg.CORBA.StringDefOperations)
cons public init(org.omg.CORBA.StringDefOperations,org.omg.PortableServer.POA)
meth public int bound()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.StringDefOperations _delegate()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.StringDefOperations)
meth public void bound(int)
meth public void destroy()
supr org.omg.CORBA.StringDefPOA
hfds _impl,_poa

CLSS public final org.omg.CORBA.StringHolder
cons public init()
cons public init(java.lang.String)
fld public java.lang.String value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.StringSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static java.lang.String[] extract(org.omg.CORBA.Any)
meth public static java.lang.String[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String[])
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.StringSeqHolder
cons public init()
cons public init(java.lang.String[])
fld public java.lang.String[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.CORBA.StringValueHelper
cons public init()
intf org.omg.CORBA.portable.BoxedValueHelper
meth public java.io.Serializable read_value(org.omg.CORBA.portable.InputStream)
meth public java.lang.String get_id()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
meth public void write_value(org.omg.CORBA.portable.OutputStream,java.io.Serializable)
supr java.lang.Object
hfds __active,__typeCode,_id,_instance

CLSS public abstract interface org.omg.CORBA.StructDef
intf org.omg.CORBA.Container
intf org.omg.CORBA.StructDefOperations
intf org.omg.CORBA.TypedefDef

CLSS public abstract org.omg.CORBA.StructDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.StructDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.StructDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.StructDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.StructDef unchecked_narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.StructDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.StructDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.StructDefHolder
cons public init()
cons public init(org.omg.CORBA.StructDef)
fld public org.omg.CORBA.StructDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.StructDefOperations
intf org.omg.CORBA.ContainerOperations
intf org.omg.CORBA.TypedefDefOperations
meth public abstract org.omg.CORBA.StructMember[] members()
meth public abstract void members(org.omg.CORBA.StructMember[])

CLSS public abstract org.omg.CORBA.StructDefPOA
cons public init()
intf org.omg.CORBA.StructDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.StructDef _this()
meth public org.omg.CORBA.StructDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.StructDefPOATie
cons public init(org.omg.CORBA.StructDefOperations)
cons public init(org.omg.CORBA.StructDefOperations,org.omg.PortableServer.POA)
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public org.omg.CORBA.Contained lookup(java.lang.String)
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.StructDefOperations _delegate()
meth public org.omg.CORBA.StructMember[] members()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.StructDefOperations)
meth public void destroy()
meth public void id(java.lang.String)
meth public void members(org.omg.CORBA.StructMember[])
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.StructDefPOA
hfds _impl,_poa

CLSS public final org.omg.CORBA.StructMember
cons public init()
cons public init(java.lang.String,org.omg.CORBA.TypeCode,org.omg.CORBA.IDLType)
fld public java.lang.String name
fld public org.omg.CORBA.IDLType type_def
fld public org.omg.CORBA.TypeCode type
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.StructMemberHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.StructMember extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.StructMember read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.StructMember)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.StructMember)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public abstract org.omg.CORBA.StructMemberSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.StructMember[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.StructMember[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.StructMember[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.StructMember[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.StructMemberSeqHolder
cons public init()
cons public init(org.omg.CORBA.StructMember[])
fld public org.omg.CORBA.StructMember[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.SystemException
cons protected init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
fld public int minor
fld public org.omg.CORBA.CompletionStatus completed
meth public java.lang.String toString()
supr java.lang.RuntimeException

CLSS public org.omg.CORBA.TCKind
cons protected init(int)
fld public final static int _tk_Principal = 13
fld public final static int _tk_TypeCode = 12
fld public final static int _tk_abstract_interface = 32
fld public final static int _tk_alias = 21
fld public final static int _tk_any = 11
fld public final static int _tk_array = 20
fld public final static int _tk_boolean = 8
fld public final static int _tk_char = 9
fld public final static int _tk_double = 7
fld public final static int _tk_enum = 17
fld public final static int _tk_except = 22
fld public final static int _tk_fixed = 28
fld public final static int _tk_float = 6
fld public final static int _tk_long = 3
fld public final static int _tk_longdouble = 25
fld public final static int _tk_longlong = 23
fld public final static int _tk_native = 31
fld public final static int _tk_null = 0
fld public final static int _tk_objref = 14
fld public final static int _tk_octet = 10
fld public final static int _tk_sequence = 19
fld public final static int _tk_short = 2
fld public final static int _tk_string = 18
fld public final static int _tk_struct = 15
fld public final static int _tk_ulong = 5
fld public final static int _tk_ulonglong = 24
fld public final static int _tk_union = 16
fld public final static int _tk_ushort = 4
fld public final static int _tk_value = 29
fld public final static int _tk_value_box = 30
fld public final static int _tk_void = 1
fld public final static int _tk_wchar = 26
fld public final static int _tk_wstring = 27
fld public final static org.omg.CORBA.TCKind tk_Principal
fld public final static org.omg.CORBA.TCKind tk_TypeCode
fld public final static org.omg.CORBA.TCKind tk_abstract_interface
fld public final static org.omg.CORBA.TCKind tk_alias
fld public final static org.omg.CORBA.TCKind tk_any
fld public final static org.omg.CORBA.TCKind tk_array
fld public final static org.omg.CORBA.TCKind tk_boolean
fld public final static org.omg.CORBA.TCKind tk_char
fld public final static org.omg.CORBA.TCKind tk_double
fld public final static org.omg.CORBA.TCKind tk_enum
fld public final static org.omg.CORBA.TCKind tk_except
fld public final static org.omg.CORBA.TCKind tk_fixed
fld public final static org.omg.CORBA.TCKind tk_float
fld public final static org.omg.CORBA.TCKind tk_long
fld public final static org.omg.CORBA.TCKind tk_longdouble
fld public final static org.omg.CORBA.TCKind tk_longlong
fld public final static org.omg.CORBA.TCKind tk_native
fld public final static org.omg.CORBA.TCKind tk_null
fld public final static org.omg.CORBA.TCKind tk_objref
fld public final static org.omg.CORBA.TCKind tk_octet
fld public final static org.omg.CORBA.TCKind tk_sequence
fld public final static org.omg.CORBA.TCKind tk_short
fld public final static org.omg.CORBA.TCKind tk_string
fld public final static org.omg.CORBA.TCKind tk_struct
fld public final static org.omg.CORBA.TCKind tk_ulong
fld public final static org.omg.CORBA.TCKind tk_ulonglong
fld public final static org.omg.CORBA.TCKind tk_union
fld public final static org.omg.CORBA.TCKind tk_ushort
fld public final static org.omg.CORBA.TCKind tk_value
fld public final static org.omg.CORBA.TCKind tk_value_box
fld public final static org.omg.CORBA.TCKind tk_void
fld public final static org.omg.CORBA.TCKind tk_wchar
fld public final static org.omg.CORBA.TCKind tk_wstring
meth public int value()
meth public static org.omg.CORBA.TCKind from_int(int)
supr java.lang.Object
hfds _value

CLSS public final org.omg.CORBA.TIMEOUT
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.TRANSACTION_MODE
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.TRANSACTION_REQUIRED
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.TRANSACTION_ROLLEDBACK
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.TRANSACTION_UNAVAILABLE
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public final org.omg.CORBA.TRANSIENT
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public abstract interface org.omg.CORBA.TSIdentification
meth public abstract void identify_receiver(org.omg.CosTSPortability.Receiver) throws org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified,org.omg.CORBA.TSIdentificationPackage.NotAvailable
meth public abstract void identify_sender(org.omg.CosTSPortability.Sender) throws org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified,org.omg.CORBA.TSIdentificationPackage.NotAvailable

CLSS public final org.omg.CORBA.TSIdentificationPackage.AlreadyIdentified
cons public init()
supr org.omg.CORBA.UserException

CLSS public final org.omg.CORBA.TSIdentificationPackage.NotAvailable
cons public init()
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CORBA.TypeCode
cons public init()
intf org.omg.CORBA.portable.IDLEntity
meth public abstract boolean equal(org.omg.CORBA.TypeCode)
meth public abstract boolean equivalent(org.omg.CORBA.TypeCode)
meth public abstract int default_index() throws org.omg.CORBA.TypeCodePackage.BadKind
meth public abstract int length() throws org.omg.CORBA.TypeCodePackage.BadKind
meth public abstract int member_count() throws org.omg.CORBA.TypeCodePackage.BadKind
meth public abstract java.lang.String id() throws org.omg.CORBA.TypeCodePackage.BadKind
meth public abstract java.lang.String member_name(int) throws org.omg.CORBA.TypeCodePackage.BadKind,org.omg.CORBA.TypeCodePackage.Bounds
meth public abstract java.lang.String name() throws org.omg.CORBA.TypeCodePackage.BadKind
meth public abstract org.omg.CORBA.Any member_label(int) throws org.omg.CORBA.TypeCodePackage.BadKind,org.omg.CORBA.TypeCodePackage.Bounds
meth public abstract org.omg.CORBA.TCKind kind()
meth public abstract org.omg.CORBA.TypeCode concrete_base_type() throws org.omg.CORBA.TypeCodePackage.BadKind
meth public abstract org.omg.CORBA.TypeCode content_type() throws org.omg.CORBA.TypeCodePackage.BadKind
meth public abstract org.omg.CORBA.TypeCode discriminator_type() throws org.omg.CORBA.TypeCodePackage.BadKind
meth public abstract org.omg.CORBA.TypeCode get_compact_typecode()
meth public abstract org.omg.CORBA.TypeCode member_type(int) throws org.omg.CORBA.TypeCodePackage.BadKind,org.omg.CORBA.TypeCodePackage.Bounds
meth public abstract short fixed_digits() throws org.omg.CORBA.TypeCodePackage.BadKind
meth public abstract short fixed_scale() throws org.omg.CORBA.TypeCodePackage.BadKind
meth public abstract short member_visibility(int) throws org.omg.CORBA.TypeCodePackage.BadKind,org.omg.CORBA.TypeCodePackage.Bounds
meth public abstract short type_modifier() throws org.omg.CORBA.TypeCodePackage.BadKind
supr java.lang.Object

CLSS public final org.omg.CORBA.TypeCodeHolder
cons public init()
cons public init(org.omg.CORBA.TypeCode)
fld public org.omg.CORBA.TypeCode value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.TypeCodePackage.BadKind
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public final org.omg.CORBA.TypeCodePackage.Bounds
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public final org.omg.CORBA.TypeDescription
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.TypeCode)
fld public java.lang.String defined_in
fld public java.lang.String id
fld public java.lang.String name
fld public java.lang.String version
fld public org.omg.CORBA.TypeCode type
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.TypeDescriptionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.TypeDescription extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.TypeDescription read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.TypeDescription)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.TypeDescription)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.TypeDescriptionHolder
cons public init()
cons public init(org.omg.CORBA.TypeDescription)
fld public org.omg.CORBA.TypeDescription value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.TypedefDef
intf org.omg.CORBA.Contained
intf org.omg.CORBA.IDLType
intf org.omg.CORBA.TypedefDefOperations

CLSS public abstract org.omg.CORBA.TypedefDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.TypedefDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.TypedefDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.TypedefDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypedefDef unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.TypedefDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.TypedefDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.TypedefDefHolder
cons public init()
cons public init(org.omg.CORBA.TypedefDef)
fld public org.omg.CORBA.TypedefDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.TypedefDefOperations
intf org.omg.CORBA.ContainedOperations
intf org.omg.CORBA.IDLTypeOperations

CLSS public abstract org.omg.CORBA.TypedefDefPOA
cons public init()
intf org.omg.CORBA.TypedefDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.TypedefDef _this()
meth public org.omg.CORBA.TypedefDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.TypedefDefPOATie
cons public init(org.omg.CORBA.TypedefDefOperations)
cons public init(org.omg.CORBA.TypedefDefOperations,org.omg.PortableServer.POA)
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.CORBA.TypedefDefOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.TypedefDefOperations)
meth public void destroy()
meth public void id(java.lang.String)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.TypedefDefPOA
hfds _impl,_poa

CLSS public abstract org.omg.CORBA.ULongLongSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static long[] extract(org.omg.CORBA.Any)
meth public static long[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,long[])
meth public static void write(org.omg.CORBA.portable.OutputStream,long[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ULongLongSeqHolder
cons public init()
cons public init(long[])
fld public long[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ULongSeqHelper
cons public init()
meth public static int[] extract(org.omg.CORBA.Any)
meth public static int[] read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,int[])
meth public static void write(org.omg.CORBA.portable.OutputStream,int[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ULongSeqHolder
cons public init()
cons public init(int[])
fld public int[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.UNKNOWN
cons public init()
cons public init(int,org.omg.CORBA.CompletionStatus)
cons public init(java.lang.String)
cons public init(java.lang.String,int,org.omg.CORBA.CompletionStatus)
supr org.omg.CORBA.SystemException

CLSS public abstract interface org.omg.CORBA.UNSUPPORTED_POLICY
fld public final static short value = 1

CLSS public abstract interface org.omg.CORBA.UNSUPPORTED_POLICY_VALUE
fld public final static short value = 4

CLSS public abstract org.omg.CORBA.UShortSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static short[] extract(org.omg.CORBA.Any)
meth public static short[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,short[])
meth public static void write(org.omg.CORBA.portable.OutputStream,short[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.UShortSeqHolder
cons public init()
cons public init(short[])
fld public short[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.UnionDef
intf org.omg.CORBA.Container
intf org.omg.CORBA.TypedefDef
intf org.omg.CORBA.UnionDefOperations

CLSS public abstract org.omg.CORBA.UnionDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.UnionDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.UnionDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.UnionDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.UnionDef unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.UnionDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.UnionDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.UnionDefHolder
cons public init()
cons public init(org.omg.CORBA.UnionDef)
fld public org.omg.CORBA.UnionDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.UnionDefOperations
intf org.omg.CORBA.ContainerOperations
intf org.omg.CORBA.TypedefDefOperations
meth public abstract org.omg.CORBA.IDLType discriminator_type_def()
meth public abstract org.omg.CORBA.TypeCode discriminator_type()
meth public abstract org.omg.CORBA.UnionMember[] members()
meth public abstract void discriminator_type_def(org.omg.CORBA.IDLType)
meth public abstract void members(org.omg.CORBA.UnionMember[])

CLSS public abstract org.omg.CORBA.UnionDefPOA
cons public init()
intf org.omg.CORBA.UnionDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.UnionDef _this()
meth public org.omg.CORBA.UnionDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.UnionDefPOATie
cons public init(org.omg.CORBA.UnionDefOperations)
cons public init(org.omg.CORBA.UnionDefOperations,org.omg.PortableServer.POA)
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public org.omg.CORBA.Contained lookup(java.lang.String)
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.IDLType discriminator_type_def()
meth public org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.TypeCode discriminator_type()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public org.omg.CORBA.UnionDefOperations _delegate()
meth public org.omg.CORBA.UnionMember[] members()
meth public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.UnionDefOperations)
meth public void destroy()
meth public void discriminator_type_def(org.omg.CORBA.IDLType)
meth public void id(java.lang.String)
meth public void members(org.omg.CORBA.UnionMember[])
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.UnionDefPOA
hfds _impl,_poa

CLSS public final org.omg.CORBA.UnionMember
cons public init()
cons public init(java.lang.String,org.omg.CORBA.Any,org.omg.CORBA.TypeCode,org.omg.CORBA.IDLType)
fld public java.lang.String name
fld public org.omg.CORBA.Any label
fld public org.omg.CORBA.IDLType type_def
fld public org.omg.CORBA.TypeCode type
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.UnionMemberHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.UnionMember extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.UnionMember read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.UnionMember)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.UnionMember)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public abstract org.omg.CORBA.UnionMemberSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.UnionMember[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.UnionMember[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.UnionMember[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.UnionMember[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.UnionMemberSeqHolder
cons public init()
cons public init(org.omg.CORBA.UnionMember[])
fld public org.omg.CORBA.UnionMember[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.UnknownUserException
cons public init()
cons public init(org.omg.CORBA.Any)
fld public org.omg.CORBA.Any except
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CORBA.UnknownUserExceptionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.UnknownUserException extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.UnknownUserException read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.UnknownUserException)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.UnknownUserException)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.UnknownUserExceptionHolder
cons public init()
cons public init(org.omg.CORBA.UnknownUserException)
fld public org.omg.CORBA.UnknownUserException value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.UserException
cons protected init()
cons protected init(java.lang.String)
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Exception

CLSS public abstract interface org.omg.CORBA.VM_ABSTRACT
fld public final static short value = 2

CLSS public abstract interface org.omg.CORBA.VM_CUSTOM
fld public final static short value = 1

CLSS public abstract interface org.omg.CORBA.VM_NONE
fld public final static short value = 0

CLSS public abstract interface org.omg.CORBA.VM_TRUNCATABLE
fld public final static short value = 3

CLSS public abstract org.omg.CORBA.ValueBaseHelper
cons public init()
meth public static java.io.Serializable extract(org.omg.CORBA.Any)
meth public static java.io.Serializable read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.io.Serializable)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.io.Serializable)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ValueBaseHolder
cons public init()
cons public init(java.io.Serializable)
fld public java.io.Serializable value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.ValueBoxDef
intf org.omg.CORBA.IDLType
intf org.omg.CORBA.ValueBoxDefOperations
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.ValueBoxDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.ValueBoxDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ValueBoxDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.ValueBoxDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.ValueBoxDef unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ValueBoxDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ValueBoxDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ValueBoxDefHolder
cons public init()
cons public init(org.omg.CORBA.ValueBoxDef)
fld public org.omg.CORBA.ValueBoxDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.ValueBoxDefOperations
intf org.omg.CORBA.IDLTypeOperations
meth public abstract org.omg.CORBA.IDLType original_type_def()
meth public abstract void original_type_def(org.omg.CORBA.IDLType)

CLSS public abstract org.omg.CORBA.ValueBoxDefPOA
cons public init()
intf org.omg.CORBA.ValueBoxDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.ValueBoxDef _this()
meth public org.omg.CORBA.ValueBoxDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.ValueBoxDefPOATie
cons public init(org.omg.CORBA.ValueBoxDefOperations)
cons public init(org.omg.CORBA.ValueBoxDefOperations,org.omg.PortableServer.POA)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.IDLType original_type_def()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.CORBA.ValueBoxDefOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.ValueBoxDefOperations)
meth public void destroy()
meth public void original_type_def(org.omg.CORBA.IDLType)
supr org.omg.CORBA.ValueBoxDefPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.CORBA.ValueDef
intf org.omg.CORBA.Contained
intf org.omg.CORBA.Container
intf org.omg.CORBA.IDLType
intf org.omg.CORBA.ValueDefOperations

CLSS public abstract org.omg.CORBA.ValueDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.ValueDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ValueDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.ValueDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.ValueDef unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ValueDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ValueDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ValueDefHolder
cons public init()
cons public init(org.omg.CORBA.ValueDef)
fld public org.omg.CORBA.ValueDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.ValueDefOperations
intf org.omg.CORBA.ContainedOperations
intf org.omg.CORBA.ContainerOperations
intf org.omg.CORBA.IDLTypeOperations
meth public abstract boolean has_safe_base()
meth public abstract boolean is_a(java.lang.String)
meth public abstract boolean is_abstract()
meth public abstract boolean is_custom()
meth public abstract byte flags()
meth public abstract org.omg.CORBA.AttributeDef create_attribute(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.AttributeMode)
meth public abstract org.omg.CORBA.Initializer[] initializers()
meth public abstract org.omg.CORBA.InterfaceDef[] supported_interfaces()
meth public abstract org.omg.CORBA.OperationDef create_operation(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.OperationMode,org.omg.CORBA.ParameterDescription[],org.omg.CORBA.ExceptionDef[],java.lang.String[])
meth public abstract org.omg.CORBA.ValueDef base_value()
meth public abstract org.omg.CORBA.ValueDefPackage.FullValueDescription describe_value()
meth public abstract org.omg.CORBA.ValueDef[] abstract_base_values()
meth public abstract org.omg.CORBA.ValueMemberDef create_value_member(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,short)
meth public abstract void abstract_base_values(org.omg.CORBA.ValueDef[])
meth public abstract void base_value(org.omg.CORBA.ValueDef)
meth public abstract void flags(byte)
meth public abstract void has_safe_base(boolean)
meth public abstract void initializers(org.omg.CORBA.Initializer[])
meth public abstract void is_abstract(boolean)
meth public abstract void is_custom(boolean)
meth public abstract void supported_interfaces(org.omg.CORBA.InterfaceDef[])

CLSS public abstract org.omg.CORBA.ValueDefPOA
cons public init()
intf org.omg.CORBA.ValueDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.ValueDef _this()
meth public org.omg.CORBA.ValueDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.ValueDefPOATie
cons public init(org.omg.CORBA.ValueDefOperations)
cons public init(org.omg.CORBA.ValueDefOperations,org.omg.PortableServer.POA)
meth public boolean has_safe_base()
meth public boolean is_a(java.lang.String)
meth public boolean is_abstract()
meth public boolean is_custom()
meth public byte flags()
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.AttributeDef create_attribute(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.AttributeMode)
meth public org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public org.omg.CORBA.Contained lookup(java.lang.String)
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.Initializer[] initializers()
meth public org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public org.omg.CORBA.InterfaceDef[] supported_interfaces()
meth public org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.OperationDef create_operation(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.OperationMode,org.omg.CORBA.ParameterDescription[],org.omg.CORBA.ExceptionDef[],java.lang.String[])
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ValueDef base_value()
meth public org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])
meth public org.omg.CORBA.ValueDefOperations _delegate()
meth public org.omg.CORBA.ValueDefPackage.FullValueDescription describe_value()
meth public org.omg.CORBA.ValueDef[] abstract_base_values()
meth public org.omg.CORBA.ValueMemberDef create_value_member(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,short)
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CORBA.ValueDefOperations)
meth public void abstract_base_values(org.omg.CORBA.ValueDef[])
meth public void base_value(org.omg.CORBA.ValueDef)
meth public void destroy()
meth public void flags(byte)
meth public void has_safe_base(boolean)
meth public void id(java.lang.String)
meth public void initializers(org.omg.CORBA.Initializer[])
meth public void is_abstract(boolean)
meth public void is_custom(boolean)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void supported_interfaces(org.omg.CORBA.InterfaceDef[])
meth public void version(java.lang.String)
supr org.omg.CORBA.ValueDefPOA
hfds _impl,_poa

CLSS public final org.omg.CORBA.ValueDefPackage.FullValueDescription
cons public init()
cons public init(java.lang.String,java.lang.String,boolean,boolean,java.lang.String,java.lang.String,org.omg.CORBA.OperationDescription[],org.omg.CORBA.AttributeDescription[],org.omg.CORBA.ValueMember[],org.omg.CORBA.Initializer[],java.lang.String[],java.lang.String[],boolean,java.lang.String,org.omg.CORBA.TypeCode)
fld public boolean is_abstract
fld public boolean is_custom
fld public boolean is_truncatable
fld public java.lang.String base_value
fld public java.lang.String defined_in
fld public java.lang.String id
fld public java.lang.String name
fld public java.lang.String version
fld public java.lang.String[] abstract_base_values
fld public java.lang.String[] supported_interfaces
fld public org.omg.CORBA.AttributeDescription[] attributes
fld public org.omg.CORBA.Initializer[] initializers
fld public org.omg.CORBA.OperationDescription[] operations
fld public org.omg.CORBA.TypeCode type
fld public org.omg.CORBA.ValueMember[] members
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ValueDefPackage.FullValueDescriptionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.ValueDefPackage.FullValueDescription extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ValueDefPackage.FullValueDescription read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ValueDefPackage.FullValueDescription)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ValueDefPackage.FullValueDescription)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.ValueDefPackage.FullValueDescriptionHolder
cons public init()
cons public init(org.omg.CORBA.ValueDefPackage.FullValueDescription)
fld public org.omg.CORBA.ValueDefPackage.FullValueDescription value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ValueDefSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.ValueDef[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ValueDef[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ValueDef[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ValueDef[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ValueDefSeqHolder
cons public init()
cons public init(org.omg.CORBA.ValueDef[])
fld public org.omg.CORBA.ValueDef[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.ValueDescription
cons public init()
cons public init(java.lang.String,java.lang.String,boolean,boolean,byte,java.lang.String,java.lang.String,java.lang.String[],java.lang.String[],boolean,java.lang.String)
fld public boolean has_safe_base
fld public boolean is_abstract
fld public boolean is_custom
fld public byte flags
fld public java.lang.String base_value
fld public java.lang.String defined_in
fld public java.lang.String id
fld public java.lang.String name
fld public java.lang.String version
fld public java.lang.String[] abstract_base_values
fld public java.lang.String[] supported_interfaces
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CORBA.ValueDescriptionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.ValueDescription extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ValueDescription read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ValueDescription)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ValueDescription)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.ValueDescriptionHolder
cons public init()
cons public init(org.omg.CORBA.ValueDescription)
fld public org.omg.CORBA.ValueDescription value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CORBA.ValueMember
cons public init()
cons public init(java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.TypeCode,org.omg.CORBA.IDLType,short)
fld public java.lang.String defined_in
fld public java.lang.String id
fld public java.lang.String name
fld public java.lang.String version
fld public org.omg.CORBA.IDLType type_def
fld public org.omg.CORBA.TypeCode type
fld public short access
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.ValueMemberDef
intf org.omg.CORBA.Contained
intf org.omg.CORBA.ValueMemberDefOperations
intf org.omg.CORBA.portable.IDLEntity

CLSS public abstract org.omg.CORBA.ValueMemberDefHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.ValueMemberDef extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ValueMemberDef narrow(org.omg.CORBA.Object)
meth public static org.omg.CORBA.ValueMemberDef read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.ValueMemberDef unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ValueMemberDef)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ValueMemberDef)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ValueMemberDefHolder
cons public init()
cons public init(org.omg.CORBA.ValueMemberDef)
fld public org.omg.CORBA.ValueMemberDef value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.ValueMemberDefOperations
intf org.omg.CORBA.ContainedOperations
meth public abstract org.omg.CORBA.IDLType type_def()
meth public abstract org.omg.CORBA.TypeCode type()
meth public abstract short access()
meth public abstract void access(short)
meth public abstract void type_def(org.omg.CORBA.IDLType)

CLSS public abstract org.omg.CORBA.ValueMemberDefPOA
cons public init()
intf org.omg.CORBA.ValueMemberDefOperations
intf org.omg.CORBA.portable.InvokeHandler
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.ValueMemberDef _this()
meth public org.omg.CORBA.ValueMemberDef _this(org.omg.CORBA.ORB)
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CORBA.ValueMemberDefPOATie
cons public init(org.omg.CORBA.ValueMemberDefOperations)
cons public init(org.omg.CORBA.ValueMemberDefOperations,org.omg.PortableServer.POA)
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.IDLType type_def()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.CORBA.ValueMemberDefOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public short access()
meth public void _delegate(org.omg.CORBA.ValueMemberDefOperations)
meth public void access(short)
meth public void destroy()
meth public void id(java.lang.String)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void type_def(org.omg.CORBA.IDLType)
meth public void version(java.lang.String)
supr org.omg.CORBA.ValueMemberDefPOA
hfds _impl,_poa

CLSS public abstract org.omg.CORBA.ValueMemberHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.ValueMember extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ValueMember read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ValueMember)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ValueMember)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public abstract org.omg.CORBA.ValueMemberSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.ValueMember[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ValueMember[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ValueMember[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ValueMember[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.ValueMemberSeqHolder
cons public init()
cons public init(org.omg.CORBA.ValueMember[])
fld public org.omg.CORBA.ValueMember[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.VersionSpecHelper
cons public init()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract org.omg.CORBA.VisibilityHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static short extract(org.omg.CORBA.Any)
meth public static short read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,short)
meth public static void write(org.omg.CORBA.portable.OutputStream,short)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract org.omg.CORBA.WCharSeqHelper
cons public init()
meth public static char[] extract(org.omg.CORBA.Any)
meth public static char[] read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,char[])
meth public static void write(org.omg.CORBA.portable.OutputStream,char[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.WCharSeqHolder
cons public init()
cons public init(char[])
fld public char[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CORBA.WStringSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static java.lang.String[] extract(org.omg.CORBA.Any)
meth public static java.lang.String[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String[])
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CORBA.WStringSeqHolder
cons public init()
cons public init(java.lang.String[])
fld public java.lang.String[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.CORBA.WStringValueHelper
cons public init()
intf org.omg.CORBA.portable.BoxedValueHelper
meth public java.io.Serializable read_value(org.omg.CORBA.portable.InputStream)
meth public java.lang.String get_id()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
meth public void write_value(org.omg.CORBA.portable.OutputStream,java.io.Serializable)
supr java.lang.Object
hfds __active,__typeCode,_id,_instance

CLSS public final org.omg.CORBA.WrongTransaction
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CORBA.WrongTransactionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.WrongTransaction extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.WrongTransaction read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.WrongTransaction)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.WrongTransaction)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CORBA.WrongTransactionHolder
cons public init()
cons public init(org.omg.CORBA.WrongTransaction)
fld public org.omg.CORBA.WrongTransaction value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.CORBA._AliasDefStub
cons public init()
intf org.omg.CORBA.AliasDef
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.IDLType original_type_def()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.TypeCode type()
meth public void destroy()
meth public void id(java.lang.String)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void original_type_def(org.omg.CORBA.IDLType)
meth public void version(java.lang.String)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._ArrayDefStub
cons public init()
intf org.omg.CORBA.ArrayDef
meth public int length()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.IDLType element_type_def()
meth public org.omg.CORBA.TypeCode element_type()
meth public org.omg.CORBA.TypeCode type()
meth public void destroy()
meth public void element_type_def(org.omg.CORBA.IDLType)
meth public void length(int)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._AttributeDefStub
cons public init()
intf org.omg.CORBA.AttributeDef
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.AttributeMode mode()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.IDLType type_def()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.TypeCode type()
meth public void destroy()
meth public void id(java.lang.String)
meth public void mode(org.omg.CORBA.AttributeMode)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void type_def(org.omg.CORBA.IDLType)
meth public void version(java.lang.String)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._ConstantDefStub
cons public init()
intf org.omg.CORBA.ConstantDef
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Any value()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.IDLType type_def()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.TypeCode type()
meth public void destroy()
meth public void id(java.lang.String)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void type_def(org.omg.CORBA.IDLType)
meth public void value(org.omg.CORBA.Any)
meth public void version(java.lang.String)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._ContainedStub
cons public init()
intf org.omg.CORBA.Contained
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.Repository containing_repository()
meth public void destroy()
meth public void id(java.lang.String)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._ContainerStub
cons public init()
intf org.omg.CORBA.Container
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public org.omg.CORBA.Contained lookup(java.lang.String)
meth public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._EnumDefStub
cons public init()
intf org.omg.CORBA.EnumDef
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] _ids()
meth public java.lang.String[] members()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.TypeCode type()
meth public void destroy()
meth public void id(java.lang.String)
meth public void members(java.lang.String[])
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._ExceptionDefStub
cons public init()
intf org.omg.CORBA.ExceptionDef
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public org.omg.CORBA.Contained lookup(java.lang.String)
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.StructMember[] members()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])
meth public void destroy()
meth public void id(java.lang.String)
meth public void members(org.omg.CORBA.StructMember[])
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._IDLTypeStub
cons public init()
cons public init(org.omg.CORBA.portable.Delegate)
intf org.omg.CORBA.IDLType
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.TypeCode type()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._InterfaceDefStub
cons public init()
intf org.omg.CORBA.InterfaceDef
meth public boolean is_a(java.lang.String)
meth public boolean is_abstract()
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.AttributeDef create_attribute(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.AttributeMode)
meth public org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public org.omg.CORBA.Contained lookup(java.lang.String)
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public org.omg.CORBA.InterfaceDefPackage.FullInterfaceDescription describe_interface()
meth public org.omg.CORBA.InterfaceDef[] base_interfaces()
meth public org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.OperationDef create_operation(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.OperationMode,org.omg.CORBA.ParameterDescription[],org.omg.CORBA.ExceptionDef[],java.lang.String[])
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])
meth public void base_interfaces(org.omg.CORBA.InterfaceDef[])
meth public void destroy()
meth public void id(java.lang.String)
meth public void is_abstract(boolean)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._ModuleDefStub
cons public init()
intf org.omg.CORBA.ModuleDef
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public org.omg.CORBA.Contained lookup(java.lang.String)
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])
meth public void destroy()
meth public void id(java.lang.String)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._NativeDefStub
cons public init()
intf org.omg.CORBA.NativeDef
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.TypeCode type()
meth public void destroy()
meth public void id(java.lang.String)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._OperationDefStub
cons public init()
intf org.omg.CORBA.OperationDef
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] _ids()
meth public java.lang.String[] contexts()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.ExceptionDef[] exceptions()
meth public org.omg.CORBA.IDLType result_def()
meth public org.omg.CORBA.OperationMode mode()
meth public org.omg.CORBA.ParameterDescription[] params()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.TypeCode result()
meth public void contexts(java.lang.String[])
meth public void destroy()
meth public void exceptions(org.omg.CORBA.ExceptionDef[])
meth public void id(java.lang.String)
meth public void mode(org.omg.CORBA.OperationMode)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void params(org.omg.CORBA.ParameterDescription[])
meth public void result_def(org.omg.CORBA.IDLType)
meth public void version(java.lang.String)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._PolicyStub
cons public init()
cons public init(org.omg.CORBA.portable.Delegate)
intf org.omg.CORBA.Policy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Policy copy()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._PrimitiveDefStub
cons public init()
intf org.omg.CORBA.PrimitiveDef
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.PrimitiveKind kind()
meth public org.omg.CORBA.TypeCode type()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._RepositoryStub
cons public init()
intf org.omg.CORBA.Repository
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ArrayDef create_array(int,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public org.omg.CORBA.Contained lookup(java.lang.String)
meth public org.omg.CORBA.Contained lookup_id(java.lang.String)
meth public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.PrimitiveDef get_primitive(org.omg.CORBA.PrimitiveKind)
meth public org.omg.CORBA.SequenceDef create_sequence(int,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.StringDef create_string(int)
meth public org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._SequenceDefStub
cons public init()
intf org.omg.CORBA.SequenceDef
meth public int bound()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.IDLType element_type_def()
meth public org.omg.CORBA.TypeCode element_type()
meth public org.omg.CORBA.TypeCode type()
meth public void bound(int)
meth public void destroy()
meth public void element_type_def(org.omg.CORBA.IDLType)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._StringDefStub
cons public init()
intf org.omg.CORBA.StringDef
meth public int bound()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.TypeCode type()
meth public void bound(int)
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._StructDefStub
cons public init()
intf org.omg.CORBA.StructDef
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public org.omg.CORBA.Contained lookup(java.lang.String)
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.StructMember[] members()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])
meth public void destroy()
meth public void id(java.lang.String)
meth public void members(org.omg.CORBA.StructMember[])
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._TypedefDefStub
cons public init()
intf org.omg.CORBA.TypedefDef
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.TypeCode type()
meth public void destroy()
meth public void id(java.lang.String)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._UnionDefStub
cons public init()
intf org.omg.CORBA.UnionDef
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public org.omg.CORBA.Contained lookup(java.lang.String)
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.IDLType discriminator_type_def()
meth public org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.TypeCode discriminator_type()
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public org.omg.CORBA.UnionMember[] members()
meth public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])
meth public void destroy()
meth public void discriminator_type_def(org.omg.CORBA.IDLType)
meth public void id(java.lang.String)
meth public void members(org.omg.CORBA.UnionMember[])
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void version(java.lang.String)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._ValueBoxDefStub
cons public init()
intf org.omg.CORBA.ValueBoxDef
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.IDLType original_type_def()
meth public org.omg.CORBA.TypeCode type()
meth public void destroy()
meth public void original_type_def(org.omg.CORBA.IDLType)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._ValueDefStub
cons public init()
intf org.omg.CORBA.ValueDef
meth public boolean has_safe_base()
meth public boolean is_a(java.lang.String)
meth public boolean is_abstract()
meth public boolean is_custom()
meth public byte flags()
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.AliasDef create_alias(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.AttributeDef create_attribute(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.AttributeMode)
meth public org.omg.CORBA.ConstantDef create_constant(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.Any)
meth public org.omg.CORBA.Contained lookup(java.lang.String)
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Contained[] contents(org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Contained[] lookup_name(java.lang.String,int,org.omg.CORBA.DefinitionKind,boolean)
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.ContainerPackage.Description[] describe_contents(org.omg.CORBA.DefinitionKind,boolean,int)
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.EnumDef create_enum(java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public org.omg.CORBA.ExceptionDef create_exception(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.Initializer[] initializers()
meth public org.omg.CORBA.InterfaceDef create_interface(java.lang.String,java.lang.String,java.lang.String,boolean,org.omg.CORBA.InterfaceDef[])
meth public org.omg.CORBA.InterfaceDef[] supported_interfaces()
meth public org.omg.CORBA.ModuleDef create_module(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.NativeDef create_native(java.lang.String,java.lang.String,java.lang.String)
meth public org.omg.CORBA.OperationDef create_operation(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.OperationMode,org.omg.CORBA.ParameterDescription[],org.omg.CORBA.ExceptionDef[],java.lang.String[])
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.StructDef create_struct(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.StructMember[])
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.CORBA.UnionDef create_union(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,org.omg.CORBA.UnionMember[])
meth public org.omg.CORBA.ValueBoxDef create_value_box(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType)
meth public org.omg.CORBA.ValueDef base_value()
meth public org.omg.CORBA.ValueDef create_value(java.lang.String,java.lang.String,java.lang.String,boolean,boolean,byte,org.omg.CORBA.ValueDef,boolean,org.omg.CORBA.ValueDef[],org.omg.CORBA.InterfaceDef[],org.omg.CORBA.Initializer[])
meth public org.omg.CORBA.ValueDefPackage.FullValueDescription describe_value()
meth public org.omg.CORBA.ValueDef[] abstract_base_values()
meth public org.omg.CORBA.ValueMemberDef create_value_member(java.lang.String,java.lang.String,java.lang.String,org.omg.CORBA.IDLType,short)
meth public void abstract_base_values(org.omg.CORBA.ValueDef[])
meth public void base_value(org.omg.CORBA.ValueDef)
meth public void destroy()
meth public void flags(byte)
meth public void has_safe_base(boolean)
meth public void id(java.lang.String)
meth public void initializers(org.omg.CORBA.Initializer[])
meth public void is_abstract(boolean)
meth public void is_custom(boolean)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void supported_interfaces(org.omg.CORBA.InterfaceDef[])
meth public void version(java.lang.String)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA._ValueMemberDefStub
cons public init()
intf org.omg.CORBA.ValueMemberDef
meth public java.lang.String absolute_name()
meth public java.lang.String id()
meth public java.lang.String name()
meth public java.lang.String version()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.ContainedPackage.Description describe()
meth public org.omg.CORBA.Container defined_in()
meth public org.omg.CORBA.DefinitionKind def_kind()
meth public org.omg.CORBA.IDLType type_def()
meth public org.omg.CORBA.Repository containing_repository()
meth public org.omg.CORBA.TypeCode type()
meth public short access()
meth public void access(short)
meth public void destroy()
meth public void id(java.lang.String)
meth public void move(org.omg.CORBA.Container,java.lang.String,java.lang.String)
meth public void name(java.lang.String)
meth public void type_def(org.omg.CORBA.IDLType)
meth public void version(java.lang.String)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CORBA.portable.ApplicationException
cons public init(java.lang.String,org.omg.CORBA.portable.InputStream)
meth public java.lang.String getId()
meth public org.omg.CORBA.portable.InputStream getInputStream()
supr java.lang.Exception
hfds id,ins

CLSS public abstract interface org.omg.CORBA.portable.BoxedValueHelper
meth public abstract java.io.Serializable read_value(org.omg.CORBA.portable.InputStream)
meth public abstract java.lang.String get_id()
meth public abstract void write_value(org.omg.CORBA.portable.OutputStream,java.io.Serializable)

CLSS public abstract interface org.omg.CORBA.portable.CustomValue
intf org.omg.CORBA.CustomMarshal
intf org.omg.CORBA.portable.ValueBase

CLSS public abstract org.omg.CORBA.portable.Delegate
cons public init()
meth public abstract boolean is_a(org.omg.CORBA.Object,java.lang.String)
meth public abstract boolean is_equivalent(org.omg.CORBA.Object,org.omg.CORBA.Object)
meth public abstract boolean non_existent(org.omg.CORBA.Object)
meth public abstract int hash(org.omg.CORBA.Object,int)
meth public abstract org.omg.CORBA.Object duplicate(org.omg.CORBA.Object)
meth public abstract org.omg.CORBA.Object get_interface_def(org.omg.CORBA.Object)
meth public abstract org.omg.CORBA.Request create_request(org.omg.CORBA.Object,org.omg.CORBA.Context,java.lang.String,org.omg.CORBA.NVList,org.omg.CORBA.NamedValue)
meth public abstract org.omg.CORBA.Request create_request(org.omg.CORBA.Object,org.omg.CORBA.Context,java.lang.String,org.omg.CORBA.NVList,org.omg.CORBA.NamedValue,org.omg.CORBA.ExceptionList,org.omg.CORBA.ContextList)
meth public abstract org.omg.CORBA.Request request(org.omg.CORBA.Object,java.lang.String)
meth public abstract void release(org.omg.CORBA.Object)
meth public boolean equals(org.omg.CORBA.Object,java.lang.Object)
meth public boolean is_local(org.omg.CORBA.Object)
meth public int hashCode(org.omg.CORBA.Object)
meth public java.lang.String toString(org.omg.CORBA.Object)
meth public org.omg.CORBA.DomainManager[] get_domain_managers(org.omg.CORBA.Object)
meth public org.omg.CORBA.ORB orb(org.omg.CORBA.Object)
meth public org.omg.CORBA.Object set_policy_override(org.omg.CORBA.Object,org.omg.CORBA.Policy[],org.omg.CORBA.SetOverrideType)
meth public org.omg.CORBA.Policy get_policy(org.omg.CORBA.Object,int)
meth public org.omg.CORBA.portable.InputStream invoke(org.omg.CORBA.Object,org.omg.CORBA.portable.OutputStream) throws org.omg.CORBA.portable.ApplicationException,org.omg.CORBA.portable.RemarshalException
meth public org.omg.CORBA.portable.OutputStream request(org.omg.CORBA.Object,java.lang.String,boolean)
meth public org.omg.CORBA.portable.ServantObject servant_preinvoke(org.omg.CORBA.Object,java.lang.String,java.lang.Class)
meth public void releaseReply(org.omg.CORBA.Object,org.omg.CORBA.portable.InputStream)
meth public void servant_postinvoke(org.omg.CORBA.Object,org.omg.CORBA.portable.ServantObject)
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.portable.IDLEntity
intf java.io.Serializable

CLSS public org.omg.CORBA.portable.IndirectionException
cons public init(int)
fld public int offset
supr org.omg.CORBA.SystemException

CLSS public abstract org.omg.CORBA.portable.InputStream
cons public init()
meth public abstract boolean read_boolean()
meth public abstract byte read_octet()
meth public abstract char read_char()
meth public abstract char read_wchar()
meth public abstract double read_double()
meth public abstract float read_float()
meth public abstract int read_long()
meth public abstract int read_ulong()
meth public abstract java.lang.String read_string()
meth public abstract java.lang.String read_wstring()
meth public abstract long read_longlong()
meth public abstract long read_ulonglong()
meth public abstract org.omg.CORBA.Any read_any()
meth public abstract org.omg.CORBA.Object read_Object()
meth public abstract org.omg.CORBA.TypeCode read_TypeCode()
meth public abstract short read_short()
meth public abstract short read_ushort()
meth public abstract void read_boolean_array(boolean[],int,int)
meth public abstract void read_char_array(char[],int,int)
meth public abstract void read_double_array(double[],int,int)
meth public abstract void read_float_array(float[],int,int)
meth public abstract void read_long_array(int[],int,int)
meth public abstract void read_longlong_array(long[],int,int)
meth public abstract void read_octet_array(byte[],int,int)
meth public abstract void read_short_array(short[],int,int)
meth public abstract void read_ulong_array(int[],int,int)
meth public abstract void read_ulonglong_array(long[],int,int)
meth public abstract void read_ushort_array(short[],int,int)
meth public abstract void read_wchar_array(char[],int,int)
meth public int read() throws java.io.IOException
meth public java.math.BigDecimal read_fixed()
meth public org.omg.CORBA.Context read_Context()
meth public org.omg.CORBA.ORB orb()
meth public org.omg.CORBA.Object read_Object(java.lang.Class)
meth public org.omg.CORBA.Principal read_Principal()
supr java.io.InputStream

CLSS public abstract interface org.omg.CORBA.portable.InvokeHandler
meth public abstract org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)

CLSS public abstract org.omg.CORBA.portable.ObjectImpl
cons public init()
intf org.omg.CORBA.Object
meth public abstract java.lang.String[] _ids()
meth public boolean _is_a(java.lang.String)
meth public boolean _is_equivalent(org.omg.CORBA.Object)
meth public boolean _is_local()
meth public boolean _non_existent()
meth public boolean equals(java.lang.Object)
meth public int _hash(int)
meth public int hashCode()
meth public java.lang.String toString()
meth public org.omg.CORBA.DomainManager[] _get_domain_managers()
meth public org.omg.CORBA.ORB _orb()
meth public org.omg.CORBA.Object _duplicate()
meth public org.omg.CORBA.Object _get_interface_def()
meth public org.omg.CORBA.Object _set_policy_override(org.omg.CORBA.Policy[],org.omg.CORBA.SetOverrideType)
meth public org.omg.CORBA.Policy _get_policy(int)
meth public org.omg.CORBA.Request _create_request(org.omg.CORBA.Context,java.lang.String,org.omg.CORBA.NVList,org.omg.CORBA.NamedValue)
meth public org.omg.CORBA.Request _create_request(org.omg.CORBA.Context,java.lang.String,org.omg.CORBA.NVList,org.omg.CORBA.NamedValue,org.omg.CORBA.ExceptionList,org.omg.CORBA.ContextList)
meth public org.omg.CORBA.Request _request(java.lang.String)
meth public org.omg.CORBA.portable.Delegate _get_delegate()
meth public org.omg.CORBA.portable.InputStream _invoke(org.omg.CORBA.portable.OutputStream) throws org.omg.CORBA.portable.ApplicationException,org.omg.CORBA.portable.RemarshalException
meth public org.omg.CORBA.portable.OutputStream _request(java.lang.String,boolean)
meth public org.omg.CORBA.portable.ServantObject _servant_preinvoke(java.lang.String,java.lang.Class)
meth public void _release()
meth public void _releaseReply(org.omg.CORBA.portable.InputStream)
meth public void _servant_postinvoke(org.omg.CORBA.portable.ServantObject)
meth public void _set_delegate(org.omg.CORBA.portable.Delegate)
supr java.lang.Object
hfds __delegate

CLSS public abstract org.omg.CORBA.portable.OutputStream
cons public init()
meth public abstract org.omg.CORBA.portable.InputStream create_input_stream()
meth public abstract void write_Object(org.omg.CORBA.Object)
meth public abstract void write_TypeCode(org.omg.CORBA.TypeCode)
meth public abstract void write_any(org.omg.CORBA.Any)
meth public abstract void write_boolean(boolean)
meth public abstract void write_boolean_array(boolean[],int,int)
meth public abstract void write_char(char)
meth public abstract void write_char_array(char[],int,int)
meth public abstract void write_double(double)
meth public abstract void write_double_array(double[],int,int)
meth public abstract void write_float(float)
meth public abstract void write_float_array(float[],int,int)
meth public abstract void write_long(int)
meth public abstract void write_long_array(int[],int,int)
meth public abstract void write_longlong(long)
meth public abstract void write_longlong_array(long[],int,int)
meth public abstract void write_octet(byte)
meth public abstract void write_octet_array(byte[],int,int)
meth public abstract void write_short(short)
meth public abstract void write_short_array(short[],int,int)
meth public abstract void write_string(java.lang.String)
meth public abstract void write_ulong(int)
meth public abstract void write_ulong_array(int[],int,int)
meth public abstract void write_ulonglong(long)
meth public abstract void write_ulonglong_array(long[],int,int)
meth public abstract void write_ushort(short)
meth public abstract void write_ushort_array(short[],int,int)
meth public abstract void write_wchar(char)
meth public abstract void write_wchar_array(char[],int,int)
meth public abstract void write_wstring(java.lang.String)
meth public org.omg.CORBA.ORB orb()
meth public void write(int) throws java.io.IOException
meth public void write_Context(org.omg.CORBA.Context,org.omg.CORBA.ContextList)
meth public void write_Principal(org.omg.CORBA.Principal)
meth public void write_fixed(java.math.BigDecimal)
supr java.io.OutputStream

CLSS public final org.omg.CORBA.portable.RemarshalException
cons public init()
supr java.lang.Exception

CLSS public abstract interface org.omg.CORBA.portable.ResponseHandler
meth public abstract org.omg.CORBA.portable.OutputStream createExceptionReply()
meth public abstract org.omg.CORBA.portable.OutputStream createReply()

CLSS public org.omg.CORBA.portable.ServantObject
cons public init()
fld public java.lang.Object servant
supr java.lang.Object

CLSS public abstract interface org.omg.CORBA.portable.Streamable
meth public abstract org.omg.CORBA.TypeCode _type()
meth public abstract void _read(org.omg.CORBA.portable.InputStream)
meth public abstract void _write(org.omg.CORBA.portable.OutputStream)

CLSS public abstract interface org.omg.CORBA.portable.StreamableValue
intf org.omg.CORBA.portable.Streamable
intf org.omg.CORBA.portable.ValueBase

CLSS public org.omg.CORBA.portable.UnknownException
cons public init(java.lang.Throwable)
fld public java.lang.Throwable originalEx
supr org.omg.CORBA.SystemException

CLSS public abstract interface org.omg.CORBA.portable.ValueBase
intf org.omg.CORBA.portable.IDLEntity
meth public abstract java.lang.String[] _truncatable_ids()

CLSS public abstract interface org.omg.CORBA.portable.ValueFactory
meth public abstract java.io.Serializable read_value(org.omg.CORBA_2_3.portable.InputStream)

CLSS public abstract interface org.omg.CORBA.portable.ValueInputStream
meth public abstract void end_value()
meth public abstract void start_value()

CLSS public abstract interface org.omg.CORBA.portable.ValueOutputStream
meth public abstract void end_value()
meth public abstract void start_value(java.lang.String)

CLSS public abstract org.omg.CORBA_2_3.ORB
cons public init()
meth public org.omg.CORBA.Object get_value_def(java.lang.String)
meth public org.omg.CORBA.portable.ValueFactory lookup_value_factory(java.lang.String)
meth public org.omg.CORBA.portable.ValueFactory register_value_factory(java.lang.String,org.omg.CORBA.portable.ValueFactory)
meth public void set_delegate(java.lang.Object)
meth public void unregister_value_factory(java.lang.String)
supr org.omg.CORBA.ORB

CLSS public abstract org.omg.CORBA_2_3.portable.Delegate
cons public init()
meth public java.lang.String get_codebase(org.omg.CORBA.Object)
supr org.omg.CORBA.portable.Delegate

CLSS public abstract org.omg.CORBA_2_3.portable.InputStream
cons public init()
meth public java.io.Serializable read_value()
meth public java.io.Serializable read_value(java.io.Serializable)
meth public java.io.Serializable read_value(java.lang.Class)
meth public java.io.Serializable read_value(java.lang.String)
meth public java.io.Serializable read_value(org.omg.CORBA.portable.BoxedValueHelper)
meth public java.lang.Object read_abstract_interface()
meth public java.lang.Object read_abstract_interface(java.lang.Class)
supr org.omg.CORBA.portable.InputStream
hfds ALLOW_SUBCLASS_PROP,allowSubclass

CLSS public abstract org.omg.CORBA_2_3.portable.ObjectImpl
cons public init()
meth public java.lang.String _get_codebase()
supr org.omg.CORBA.portable.ObjectImpl

CLSS public abstract org.omg.CORBA_2_3.portable.OutputStream
cons public init()
meth public void write_abstract_interface(java.lang.Object)
meth public void write_value(java.io.Serializable)
meth public void write_value(java.io.Serializable,java.lang.Class)
meth public void write_value(java.io.Serializable,java.lang.String)
meth public void write_value(java.io.Serializable,org.omg.CORBA.portable.BoxedValueHelper)
supr org.omg.CORBA.portable.OutputStream

CLSS public final org.omg.CosNaming.Binding
cons public init()
cons public init(org.omg.CosNaming.NameComponent[],org.omg.CosNaming.BindingType)
fld public org.omg.CosNaming.BindingType binding_type
fld public org.omg.CosNaming.NameComponent[] binding_name
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CosNaming.BindingHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosNaming.Binding extract(org.omg.CORBA.Any)
meth public static org.omg.CosNaming.Binding read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosNaming.Binding)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosNaming.Binding)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosNaming.BindingHolder
cons public init()
cons public init(org.omg.CosNaming.Binding)
fld public org.omg.CosNaming.Binding value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosNaming.BindingIterator
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.CosNaming.BindingIteratorOperations

CLSS public abstract org.omg.CosNaming.BindingIteratorHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosNaming.BindingIterator extract(org.omg.CORBA.Any)
meth public static org.omg.CosNaming.BindingIterator narrow(org.omg.CORBA.Object)
meth public static org.omg.CosNaming.BindingIterator read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CosNaming.BindingIterator unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosNaming.BindingIterator)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosNaming.BindingIterator)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosNaming.BindingIteratorHolder
cons public init()
cons public init(org.omg.CosNaming.BindingIterator)
fld public org.omg.CosNaming.BindingIterator value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosNaming.BindingIteratorOperations
meth public abstract boolean next_n(int,org.omg.CosNaming.BindingListHolder)
meth public abstract boolean next_one(org.omg.CosNaming.BindingHolder)
meth public abstract void destroy()

CLSS public abstract org.omg.CosNaming.BindingIteratorPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.CosNaming.BindingIteratorOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.CosNaming.BindingIterator _this()
meth public org.omg.CosNaming.BindingIterator _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CosNaming.BindingIteratorPOATie
cons public init(org.omg.CosNaming.BindingIteratorOperations)
cons public init(org.omg.CosNaming.BindingIteratorOperations,org.omg.PortableServer.POA)
meth public boolean next_n(int,org.omg.CosNaming.BindingListHolder)
meth public boolean next_one(org.omg.CosNaming.BindingHolder)
meth public org.omg.CosNaming.BindingIteratorOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CosNaming.BindingIteratorOperations)
meth public void destroy()
supr org.omg.CosNaming.BindingIteratorPOA
hfds _impl,_poa

CLSS public abstract org.omg.CosNaming.BindingListHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosNaming.Binding[] extract(org.omg.CORBA.Any)
meth public static org.omg.CosNaming.Binding[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosNaming.Binding[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosNaming.Binding[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosNaming.BindingListHolder
cons public init()
cons public init(org.omg.CosNaming.Binding[])
fld public org.omg.CosNaming.Binding[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.CosNaming.BindingType
cons protected init(int)
fld public final static int _ncontext = 1
fld public final static int _nobject = 0
fld public final static org.omg.CosNaming.BindingType ncontext
fld public final static org.omg.CosNaming.BindingType nobject
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.CosNaming.BindingType from_int(int)
supr java.lang.Object
hfds __array,__size,__value

CLSS public abstract org.omg.CosNaming.BindingTypeHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosNaming.BindingType extract(org.omg.CORBA.Any)
meth public static org.omg.CosNaming.BindingType read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosNaming.BindingType)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosNaming.BindingType)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosNaming.BindingTypeHolder
cons public init()
cons public init(org.omg.CosNaming.BindingType)
fld public org.omg.CosNaming.BindingType value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CosNaming.IstringHelper
cons public init()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosNaming.NameComponent
cons public init()
cons public init(java.lang.String,java.lang.String)
fld public java.lang.String id
fld public java.lang.String kind
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CosNaming.NameComponentHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosNaming.NameComponent extract(org.omg.CORBA.Any)
meth public static org.omg.CosNaming.NameComponent read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosNaming.NameComponent)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosNaming.NameComponent)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosNaming.NameComponentHolder
cons public init()
cons public init(org.omg.CosNaming.NameComponent)
fld public org.omg.CosNaming.NameComponent value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CosNaming.NameHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosNaming.NameComponent[] extract(org.omg.CORBA.Any)
meth public static org.omg.CosNaming.NameComponent[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosNaming.NameComponent[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosNaming.NameComponent[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosNaming.NameHolder
cons public init()
cons public init(org.omg.CosNaming.NameComponent[])
fld public org.omg.CosNaming.NameComponent[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosNaming.NamingContext
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.CosNaming.NamingContextOperations

CLSS public abstract interface org.omg.CosNaming.NamingContextExt
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.CosNaming.NamingContext
intf org.omg.CosNaming.NamingContextExtOperations

CLSS public abstract org.omg.CosNaming.NamingContextExtHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosNaming.NamingContextExt extract(org.omg.CORBA.Any)
meth public static org.omg.CosNaming.NamingContextExt narrow(org.omg.CORBA.Object)
meth public static org.omg.CosNaming.NamingContextExt read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CosNaming.NamingContextExt unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosNaming.NamingContextExt)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosNaming.NamingContextExt)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosNaming.NamingContextExtHolder
cons public init()
cons public init(org.omg.CosNaming.NamingContextExt)
fld public org.omg.CosNaming.NamingContextExt value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosNaming.NamingContextExtOperations
intf org.omg.CosNaming.NamingContextOperations
meth public abstract java.lang.String to_string(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.InvalidName
meth public abstract java.lang.String to_url(java.lang.String,java.lang.String) throws org.omg.CosNaming.NamingContextExtPackage.InvalidAddress,org.omg.CosNaming.NamingContextPackage.InvalidName
meth public abstract org.omg.CORBA.Object resolve_str(java.lang.String) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public abstract org.omg.CosNaming.NameComponent[] to_name(java.lang.String) throws org.omg.CosNaming.NamingContextPackage.InvalidName

CLSS public abstract org.omg.CosNaming.NamingContextExtPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.CosNaming.NamingContextExtOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.CosNaming.NamingContextExt _this()
meth public org.omg.CosNaming.NamingContextExt _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CosNaming.NamingContextExtPOATie
cons public init(org.omg.CosNaming.NamingContextExtOperations)
cons public init(org.omg.CosNaming.NamingContextExtOperations,org.omg.PortableServer.POA)
meth public java.lang.String to_string(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.InvalidName
meth public java.lang.String to_url(java.lang.String,java.lang.String) throws org.omg.CosNaming.NamingContextExtPackage.InvalidAddress,org.omg.CosNaming.NamingContextPackage.InvalidName
meth public org.omg.CORBA.Object resolve(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public org.omg.CORBA.Object resolve_str(java.lang.String) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public org.omg.CosNaming.NameComponent[] to_name(java.lang.String) throws org.omg.CosNaming.NamingContextPackage.InvalidName
meth public org.omg.CosNaming.NamingContext bind_new_context(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.AlreadyBound,org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public org.omg.CosNaming.NamingContext new_context()
meth public org.omg.CosNaming.NamingContextExtOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CosNaming.NamingContextExtOperations)
meth public void bind(org.omg.CosNaming.NameComponent[],org.omg.CORBA.Object) throws org.omg.CosNaming.NamingContextPackage.AlreadyBound,org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public void bind_context(org.omg.CosNaming.NameComponent[],org.omg.CosNaming.NamingContext) throws org.omg.CosNaming.NamingContextPackage.AlreadyBound,org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public void destroy() throws org.omg.CosNaming.NamingContextPackage.NotEmpty
meth public void list(int,org.omg.CosNaming.BindingListHolder,org.omg.CosNaming.BindingIteratorHolder)
meth public void rebind(org.omg.CosNaming.NameComponent[],org.omg.CORBA.Object) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public void rebind_context(org.omg.CosNaming.NameComponent[],org.omg.CosNaming.NamingContext) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public void unbind(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
supr org.omg.CosNaming.NamingContextExtPOA
hfds _impl,_poa

CLSS public abstract org.omg.CosNaming.NamingContextExtPackage.AddressHelper
cons public init()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosNaming.NamingContextExtPackage.InvalidAddress
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosNaming.NamingContextExtPackage.InvalidAddress extract(org.omg.CORBA.Any)
meth public static org.omg.CosNaming.NamingContextExtPackage.InvalidAddress read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosNaming.NamingContextExtPackage.InvalidAddress)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosNaming.NamingContextExtPackage.InvalidAddress)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosNaming.NamingContextExtPackage.InvalidAddressHolder
cons public init()
cons public init(org.omg.CosNaming.NamingContextExtPackage.InvalidAddress)
fld public org.omg.CosNaming.NamingContextExtPackage.InvalidAddress value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.CosNaming.NamingContextExtPackage.StringNameHelper
cons public init()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract org.omg.CosNaming.NamingContextExtPackage.URLStringHelper
cons public init()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract org.omg.CosNaming.NamingContextHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosNaming.NamingContext extract(org.omg.CORBA.Any)
meth public static org.omg.CosNaming.NamingContext narrow(org.omg.CORBA.Object)
meth public static org.omg.CosNaming.NamingContext read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CosNaming.NamingContext unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosNaming.NamingContext)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosNaming.NamingContext)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosNaming.NamingContextHolder
cons public init()
cons public init(org.omg.CosNaming.NamingContext)
fld public org.omg.CosNaming.NamingContext value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosNaming.NamingContextOperations
meth public abstract org.omg.CORBA.Object resolve(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public abstract org.omg.CosNaming.NamingContext bind_new_context(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.AlreadyBound,org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public abstract org.omg.CosNaming.NamingContext new_context()
meth public abstract void bind(org.omg.CosNaming.NameComponent[],org.omg.CORBA.Object) throws org.omg.CosNaming.NamingContextPackage.AlreadyBound,org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public abstract void bind_context(org.omg.CosNaming.NameComponent[],org.omg.CosNaming.NamingContext) throws org.omg.CosNaming.NamingContextPackage.AlreadyBound,org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public abstract void destroy() throws org.omg.CosNaming.NamingContextPackage.NotEmpty
meth public abstract void list(int,org.omg.CosNaming.BindingListHolder,org.omg.CosNaming.BindingIteratorHolder)
meth public abstract void rebind(org.omg.CosNaming.NameComponent[],org.omg.CORBA.Object) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public abstract void rebind_context(org.omg.CosNaming.NameComponent[],org.omg.CosNaming.NamingContext) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public abstract void unbind(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound

CLSS public abstract org.omg.CosNaming.NamingContextPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.CosNaming.NamingContextOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.CosNaming.NamingContext _this()
meth public org.omg.CosNaming.NamingContext _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CosNaming.NamingContextPOATie
cons public init(org.omg.CosNaming.NamingContextOperations)
cons public init(org.omg.CosNaming.NamingContextOperations,org.omg.PortableServer.POA)
meth public org.omg.CORBA.Object resolve(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public org.omg.CosNaming.NamingContext bind_new_context(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.AlreadyBound,org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public org.omg.CosNaming.NamingContext new_context()
meth public org.omg.CosNaming.NamingContextOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CosNaming.NamingContextOperations)
meth public void bind(org.omg.CosNaming.NameComponent[],org.omg.CORBA.Object) throws org.omg.CosNaming.NamingContextPackage.AlreadyBound,org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public void bind_context(org.omg.CosNaming.NameComponent[],org.omg.CosNaming.NamingContext) throws org.omg.CosNaming.NamingContextPackage.AlreadyBound,org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public void destroy() throws org.omg.CosNaming.NamingContextPackage.NotEmpty
meth public void list(int,org.omg.CosNaming.BindingListHolder,org.omg.CosNaming.BindingIteratorHolder)
meth public void rebind(org.omg.CosNaming.NameComponent[],org.omg.CORBA.Object) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public void rebind_context(org.omg.CosNaming.NameComponent[],org.omg.CosNaming.NamingContext) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public void unbind(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
supr org.omg.CosNaming.NamingContextPOA
hfds _impl,_poa

CLSS public final org.omg.CosNaming.NamingContextPackage.AlreadyBound
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosNaming.NamingContextPackage.AlreadyBoundHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosNaming.NamingContextPackage.AlreadyBound extract(org.omg.CORBA.Any)
meth public static org.omg.CosNaming.NamingContextPackage.AlreadyBound read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosNaming.NamingContextPackage.AlreadyBound)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosNaming.NamingContextPackage.AlreadyBound)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosNaming.NamingContextPackage.AlreadyBoundHolder
cons public init()
cons public init(org.omg.CosNaming.NamingContextPackage.AlreadyBound)
fld public org.omg.CosNaming.NamingContextPackage.AlreadyBound value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CosNaming.NamingContextPackage.CannotProceed
cons public init()
cons public init(java.lang.String,org.omg.CosNaming.NamingContext,org.omg.CosNaming.NameComponent[])
cons public init(org.omg.CosNaming.NamingContext,org.omg.CosNaming.NameComponent[])
fld public org.omg.CosNaming.NameComponent[] rest_of_name
fld public org.omg.CosNaming.NamingContext cxt
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosNaming.NamingContextPackage.CannotProceedHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosNaming.NamingContextPackage.CannotProceed extract(org.omg.CORBA.Any)
meth public static org.omg.CosNaming.NamingContextPackage.CannotProceed read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosNaming.NamingContextPackage.CannotProceed)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosNaming.NamingContextPackage.CannotProceed)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosNaming.NamingContextPackage.CannotProceedHolder
cons public init()
cons public init(org.omg.CosNaming.NamingContextPackage.CannotProceed)
fld public org.omg.CosNaming.NamingContextPackage.CannotProceed value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CosNaming.NamingContextPackage.InvalidName
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosNaming.NamingContextPackage.InvalidNameHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosNaming.NamingContextPackage.InvalidName extract(org.omg.CORBA.Any)
meth public static org.omg.CosNaming.NamingContextPackage.InvalidName read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosNaming.NamingContextPackage.InvalidName)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosNaming.NamingContextPackage.InvalidName)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosNaming.NamingContextPackage.InvalidNameHolder
cons public init()
cons public init(org.omg.CosNaming.NamingContextPackage.InvalidName)
fld public org.omg.CosNaming.NamingContextPackage.InvalidName value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CosNaming.NamingContextPackage.NotEmpty
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosNaming.NamingContextPackage.NotEmptyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosNaming.NamingContextPackage.NotEmpty extract(org.omg.CORBA.Any)
meth public static org.omg.CosNaming.NamingContextPackage.NotEmpty read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosNaming.NamingContextPackage.NotEmpty)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosNaming.NamingContextPackage.NotEmpty)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosNaming.NamingContextPackage.NotEmptyHolder
cons public init()
cons public init(org.omg.CosNaming.NamingContextPackage.NotEmpty)
fld public org.omg.CosNaming.NamingContextPackage.NotEmpty value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CosNaming.NamingContextPackage.NotFound
cons public init()
cons public init(java.lang.String,org.omg.CosNaming.NamingContextPackage.NotFoundReason,org.omg.CosNaming.NameComponent[])
cons public init(org.omg.CosNaming.NamingContextPackage.NotFoundReason,org.omg.CosNaming.NameComponent[])
fld public org.omg.CosNaming.NameComponent[] rest_of_name
fld public org.omg.CosNaming.NamingContextPackage.NotFoundReason why
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosNaming.NamingContextPackage.NotFoundHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosNaming.NamingContextPackage.NotFound extract(org.omg.CORBA.Any)
meth public static org.omg.CosNaming.NamingContextPackage.NotFound read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosNaming.NamingContextPackage.NotFound)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosNaming.NamingContextPackage.NotFound)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosNaming.NamingContextPackage.NotFoundHolder
cons public init()
cons public init(org.omg.CosNaming.NamingContextPackage.NotFound)
fld public org.omg.CosNaming.NamingContextPackage.NotFound value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.CosNaming.NamingContextPackage.NotFoundReason
cons protected init(int)
fld public final static int _missing_node = 0
fld public final static int _not_context = 1
fld public final static int _not_object = 2
fld public final static org.omg.CosNaming.NamingContextPackage.NotFoundReason missing_node
fld public final static org.omg.CosNaming.NamingContextPackage.NotFoundReason not_context
fld public final static org.omg.CosNaming.NamingContextPackage.NotFoundReason not_object
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.CosNaming.NamingContextPackage.NotFoundReason from_int(int)
supr java.lang.Object
hfds __array,__size,__value

CLSS public abstract org.omg.CosNaming.NamingContextPackage.NotFoundReasonHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosNaming.NamingContextPackage.NotFoundReason extract(org.omg.CORBA.Any)
meth public static org.omg.CosNaming.NamingContextPackage.NotFoundReason read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosNaming.NamingContextPackage.NotFoundReason)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosNaming.NamingContextPackage.NotFoundReason)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosNaming.NamingContextPackage.NotFoundReasonHolder
cons public init()
cons public init(org.omg.CosNaming.NamingContextPackage.NotFoundReason)
fld public org.omg.CosNaming.NamingContextPackage.NotFoundReason value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.CosNaming._BindingIteratorStub
cons public init()
intf org.omg.CosNaming.BindingIterator
meth public boolean next_n(int,org.omg.CosNaming.BindingListHolder)
meth public boolean next_one(org.omg.CosNaming.BindingHolder)
meth public java.lang.String[] _ids()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CosNaming._NamingContextExtStub
cons public init()
intf org.omg.CosNaming.NamingContextExt
meth public java.lang.String to_string(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.InvalidName
meth public java.lang.String to_url(java.lang.String,java.lang.String) throws org.omg.CosNaming.NamingContextExtPackage.InvalidAddress,org.omg.CosNaming.NamingContextPackage.InvalidName
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Object resolve(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public org.omg.CORBA.Object resolve_str(java.lang.String) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public org.omg.CosNaming.NameComponent[] to_name(java.lang.String) throws org.omg.CosNaming.NamingContextPackage.InvalidName
meth public org.omg.CosNaming.NamingContext bind_new_context(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.AlreadyBound,org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public org.omg.CosNaming.NamingContext new_context()
meth public void bind(org.omg.CosNaming.NameComponent[],org.omg.CORBA.Object) throws org.omg.CosNaming.NamingContextPackage.AlreadyBound,org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public void bind_context(org.omg.CosNaming.NameComponent[],org.omg.CosNaming.NamingContext) throws org.omg.CosNaming.NamingContextPackage.AlreadyBound,org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public void destroy() throws org.omg.CosNaming.NamingContextPackage.NotEmpty
meth public void list(int,org.omg.CosNaming.BindingListHolder,org.omg.CosNaming.BindingIteratorHolder)
meth public void rebind(org.omg.CosNaming.NameComponent[],org.omg.CORBA.Object) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public void rebind_context(org.omg.CosNaming.NameComponent[],org.omg.CosNaming.NamingContext) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public void unbind(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CosNaming._NamingContextStub
cons public init()
intf org.omg.CosNaming.NamingContext
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Object resolve(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public org.omg.CosNaming.NamingContext bind_new_context(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.AlreadyBound,org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public org.omg.CosNaming.NamingContext new_context()
meth public void bind(org.omg.CosNaming.NameComponent[],org.omg.CORBA.Object) throws org.omg.CosNaming.NamingContextPackage.AlreadyBound,org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public void bind_context(org.omg.CosNaming.NameComponent[],org.omg.CosNaming.NamingContext) throws org.omg.CosNaming.NamingContextPackage.AlreadyBound,org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public void destroy() throws org.omg.CosNaming.NamingContextPackage.NotEmpty
meth public void list(int,org.omg.CosNaming.BindingListHolder,org.omg.CosNaming.BindingIteratorHolder)
meth public void rebind(org.omg.CosNaming.NameComponent[],org.omg.CORBA.Object) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public void rebind_context(org.omg.CosNaming.NameComponent[],org.omg.CosNaming.NamingContext) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
meth public void unbind(org.omg.CosNaming.NameComponent[]) throws org.omg.CosNaming.NamingContextPackage.CannotProceed,org.omg.CosNaming.NamingContextPackage.InvalidName,org.omg.CosNaming.NamingContextPackage.NotFound
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public abstract interface org.omg.CosTSInteroperation.TAG_INV_POLICY
fld public final static int value = 32

CLSS public abstract interface org.omg.CosTSInteroperation.TAG_OTS_POLICY
fld public final static int value = 31

CLSS public abstract interface org.omg.CosTSPortability.Receiver
meth public abstract void received_request(int,org.omg.CosTransactions.PropagationContext)
meth public abstract void sending_reply(int,org.omg.CosTransactions.PropagationContextHolder)

CLSS public org.omg.CosTSPortability.ReceiverHolder
cons public init()
cons public init(org.omg.CosTSPortability.Receiver)
fld public org.omg.CosTSPortability.Receiver value
supr java.lang.Object

CLSS public abstract interface org.omg.CosTSPortability.Sender
meth public abstract void received_reply(int,org.omg.CosTransactions.PropagationContext,org.omg.CORBA.Environment) throws org.omg.CORBA.WrongTransaction
meth public abstract void sending_request(int,org.omg.CosTransactions.PropagationContextHolder)

CLSS public org.omg.CosTSPortability.SenderHolder
cons public init()
cons public init(org.omg.CosTSPortability.Sender)
fld public org.omg.CosTSPortability.Sender value
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.ADAPTS
fld public final static short value = 3

CLSS public abstract interface org.omg.CosTransactions.Control
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.CosTransactions.ControlOperations

CLSS public abstract org.omg.CosTransactions.ControlHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.Control extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.Control narrow(org.omg.CORBA.Object)
meth public static org.omg.CosTransactions.Control read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CosTransactions.Control unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.Control)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.Control)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosTransactions.ControlHolder
cons public init()
cons public init(org.omg.CosTransactions.Control)
fld public org.omg.CosTransactions.Control value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.ControlOperations
meth public abstract org.omg.CosTransactions.Coordinator get_coordinator() throws org.omg.CosTransactions.Unavailable
meth public abstract org.omg.CosTransactions.Terminator get_terminator() throws org.omg.CosTransactions.Unavailable

CLSS public abstract org.omg.CosTransactions.ControlPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.CosTransactions.ControlOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.CosTransactions.Control _this()
meth public org.omg.CosTransactions.Control _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CosTransactions.ControlPOATie
cons public init(org.omg.CosTransactions.ControlOperations)
cons public init(org.omg.CosTransactions.ControlOperations,org.omg.PortableServer.POA)
meth public org.omg.CosTransactions.ControlOperations _delegate()
meth public org.omg.CosTransactions.Coordinator get_coordinator() throws org.omg.CosTransactions.Unavailable
meth public org.omg.CosTransactions.Terminator get_terminator() throws org.omg.CosTransactions.Unavailable
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CosTransactions.ControlOperations)
supr org.omg.CosTransactions.ControlPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.CosTransactions.Coordinator
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.CosTransactions.CoordinatorOperations

CLSS public abstract org.omg.CosTransactions.CoordinatorHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.Coordinator extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.Coordinator narrow(org.omg.CORBA.Object)
meth public static org.omg.CosTransactions.Coordinator read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CosTransactions.Coordinator unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.Coordinator)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.Coordinator)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosTransactions.CoordinatorHolder
cons public init()
cons public init(org.omg.CosTransactions.Coordinator)
fld public org.omg.CosTransactions.Coordinator value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.CoordinatorOperations
meth public abstract boolean is_ancestor_transaction(org.omg.CosTransactions.Coordinator)
meth public abstract boolean is_descendant_transaction(org.omg.CosTransactions.Coordinator)
meth public abstract boolean is_related_transaction(org.omg.CosTransactions.Coordinator)
meth public abstract boolean is_same_transaction(org.omg.CosTransactions.Coordinator)
meth public abstract boolean is_top_level_transaction()
meth public abstract int hash_top_level_tran()
meth public abstract int hash_transaction()
meth public abstract java.lang.String get_transaction_name()
meth public abstract org.omg.CosTransactions.Control create_subtransaction() throws org.omg.CosTransactions.Inactive,org.omg.CosTransactions.SubtransactionsUnavailable
meth public abstract org.omg.CosTransactions.PropagationContext get_txcontext() throws org.omg.CosTransactions.Unavailable
meth public abstract org.omg.CosTransactions.RecoveryCoordinator register_resource(org.omg.CosTransactions.Resource) throws org.omg.CosTransactions.Inactive
meth public abstract org.omg.CosTransactions.Status get_parent_status()
meth public abstract org.omg.CosTransactions.Status get_status()
meth public abstract org.omg.CosTransactions.Status get_top_level_status()
meth public abstract void register_subtran_aware(org.omg.CosTransactions.SubtransactionAwareResource) throws org.omg.CosTransactions.Inactive,org.omg.CosTransactions.NotSubtransaction
meth public abstract void register_synchronization(org.omg.CosTransactions.Synchronization) throws org.omg.CosTransactions.Inactive,org.omg.CosTransactions.SynchronizationUnavailable
meth public abstract void rollback_only() throws org.omg.CosTransactions.Inactive

CLSS public abstract org.omg.CosTransactions.CoordinatorPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.CosTransactions.CoordinatorOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.CosTransactions.Coordinator _this()
meth public org.omg.CosTransactions.Coordinator _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CosTransactions.CoordinatorPOATie
cons public init(org.omg.CosTransactions.CoordinatorOperations)
cons public init(org.omg.CosTransactions.CoordinatorOperations,org.omg.PortableServer.POA)
meth public boolean is_ancestor_transaction(org.omg.CosTransactions.Coordinator)
meth public boolean is_descendant_transaction(org.omg.CosTransactions.Coordinator)
meth public boolean is_related_transaction(org.omg.CosTransactions.Coordinator)
meth public boolean is_same_transaction(org.omg.CosTransactions.Coordinator)
meth public boolean is_top_level_transaction()
meth public int hash_top_level_tran()
meth public int hash_transaction()
meth public java.lang.String get_transaction_name()
meth public org.omg.CosTransactions.Control create_subtransaction() throws org.omg.CosTransactions.Inactive,org.omg.CosTransactions.SubtransactionsUnavailable
meth public org.omg.CosTransactions.CoordinatorOperations _delegate()
meth public org.omg.CosTransactions.PropagationContext get_txcontext() throws org.omg.CosTransactions.Unavailable
meth public org.omg.CosTransactions.RecoveryCoordinator register_resource(org.omg.CosTransactions.Resource) throws org.omg.CosTransactions.Inactive
meth public org.omg.CosTransactions.Status get_parent_status()
meth public org.omg.CosTransactions.Status get_status()
meth public org.omg.CosTransactions.Status get_top_level_status()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CosTransactions.CoordinatorOperations)
meth public void register_subtran_aware(org.omg.CosTransactions.SubtransactionAwareResource) throws org.omg.CosTransactions.Inactive,org.omg.CosTransactions.NotSubtransaction
meth public void register_synchronization(org.omg.CosTransactions.Synchronization) throws org.omg.CosTransactions.Inactive,org.omg.CosTransactions.SynchronizationUnavailable
meth public void rollback_only() throws org.omg.CosTransactions.Inactive
supr org.omg.CosTransactions.CoordinatorPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.CosTransactions.Current
intf org.omg.CORBA.Current
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.CosTransactions.CurrentOperations

CLSS public abstract org.omg.CosTransactions.CurrentHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.Current extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.Current narrow(org.omg.CORBA.Object)
meth public static org.omg.CosTransactions.Current read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CosTransactions.Current unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.Current)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.Current)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosTransactions.CurrentHolder
cons public init()
cons public init(org.omg.CosTransactions.Current)
fld public org.omg.CosTransactions.Current value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.CurrentOperations
intf org.omg.CORBA.CurrentOperations
meth public abstract int get_timeout()
meth public abstract java.lang.String get_transaction_name()
meth public abstract org.omg.CosTransactions.Control get_control()
meth public abstract org.omg.CosTransactions.Control suspend()
meth public abstract org.omg.CosTransactions.Status get_status()
meth public abstract void begin() throws org.omg.CosTransactions.SubtransactionsUnavailable
meth public abstract void commit(boolean) throws org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed,org.omg.CosTransactions.NoTransaction
meth public abstract void resume(org.omg.CosTransactions.Control) throws org.omg.CosTransactions.InvalidControl
meth public abstract void rollback() throws org.omg.CosTransactions.NoTransaction
meth public abstract void rollback_only() throws org.omg.CosTransactions.NoTransaction
meth public abstract void set_timeout(int)

CLSS public abstract org.omg.CosTransactions.CurrentPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.CosTransactions.CurrentOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.CosTransactions.Current _this()
meth public org.omg.CosTransactions.Current _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CosTransactions.CurrentPOATie
cons public init(org.omg.CosTransactions.CurrentOperations)
cons public init(org.omg.CosTransactions.CurrentOperations,org.omg.PortableServer.POA)
meth public int get_timeout()
meth public java.lang.String get_transaction_name()
meth public org.omg.CosTransactions.Control get_control()
meth public org.omg.CosTransactions.Control suspend()
meth public org.omg.CosTransactions.CurrentOperations _delegate()
meth public org.omg.CosTransactions.Status get_status()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CosTransactions.CurrentOperations)
meth public void begin() throws org.omg.CosTransactions.SubtransactionsUnavailable
meth public void commit(boolean) throws org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed,org.omg.CosTransactions.NoTransaction
meth public void resume(org.omg.CosTransactions.Control) throws org.omg.CosTransactions.InvalidControl
meth public void rollback() throws org.omg.CosTransactions.NoTransaction
meth public void rollback_only() throws org.omg.CosTransactions.NoTransaction
meth public void set_timeout(int)
supr org.omg.CosTransactions.CurrentPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.CosTransactions.EITHER
fld public final static short value = 0

CLSS public abstract interface org.omg.CosTransactions.FORBIDS
fld public final static short value = 2

CLSS public final org.omg.CosTransactions.HeuristicCommit
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosTransactions.HeuristicCommitHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.HeuristicCommit extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.HeuristicCommit read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.HeuristicCommit)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.HeuristicCommit)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosTransactions.HeuristicCommitHolder
cons public init()
cons public init(org.omg.CosTransactions.HeuristicCommit)
fld public org.omg.CosTransactions.HeuristicCommit value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CosTransactions.HeuristicHazard
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosTransactions.HeuristicHazardHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.HeuristicHazard extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.HeuristicHazard read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.HeuristicHazard)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.HeuristicHazard)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosTransactions.HeuristicHazardHolder
cons public init()
cons public init(org.omg.CosTransactions.HeuristicHazard)
fld public org.omg.CosTransactions.HeuristicHazard value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CosTransactions.HeuristicMixed
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosTransactions.HeuristicMixedHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.HeuristicMixed extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.HeuristicMixed read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.HeuristicMixed)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.HeuristicMixed)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosTransactions.HeuristicMixedHolder
cons public init()
cons public init(org.omg.CosTransactions.HeuristicMixed)
fld public org.omg.CosTransactions.HeuristicMixed value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CosTransactions.HeuristicRollback
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosTransactions.HeuristicRollbackHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.HeuristicRollback extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.HeuristicRollback read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.HeuristicRollback)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.HeuristicRollback)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosTransactions.HeuristicRollbackHolder
cons public init()
cons public init(org.omg.CosTransactions.HeuristicRollback)
fld public org.omg.CosTransactions.HeuristicRollback value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.INVOCATION_POLICY_TYPE
fld public final static int value = 55

CLSS public final org.omg.CosTransactions.Inactive
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosTransactions.InactiveHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.Inactive extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.Inactive read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.Inactive)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.Inactive)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosTransactions.InactiveHolder
cons public init()
cons public init(org.omg.CosTransactions.Inactive)
fld public org.omg.CosTransactions.Inactive value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CosTransactions.InvalidControl
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosTransactions.InvalidControlHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.InvalidControl extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.InvalidControl read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.InvalidControl)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.InvalidControl)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosTransactions.InvalidControlHolder
cons public init()
cons public init(org.omg.CosTransactions.InvalidControl)
fld public org.omg.CosTransactions.InvalidControl value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.InvocationPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.CosTransactions.InvocationPolicyOperations

CLSS public abstract org.omg.CosTransactions.InvocationPolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.InvocationPolicy extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.InvocationPolicy narrow(org.omg.CORBA.Object)
meth public static org.omg.CosTransactions.InvocationPolicy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CosTransactions.InvocationPolicy unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.InvocationPolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.InvocationPolicy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosTransactions.InvocationPolicyHolder
cons public init()
cons public init(org.omg.CosTransactions.InvocationPolicy)
fld public org.omg.CosTransactions.InvocationPolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.InvocationPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract short value()

CLSS public abstract org.omg.CosTransactions.InvocationPolicyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.CosTransactions.InvocationPolicyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.CosTransactions.InvocationPolicy _this()
meth public org.omg.CosTransactions.InvocationPolicy _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CosTransactions.InvocationPolicyPOATie
cons public init(org.omg.CosTransactions.InvocationPolicyOperations)
cons public init(org.omg.CosTransactions.InvocationPolicyOperations,org.omg.PortableServer.POA)
meth public int policy_type()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.CosTransactions.InvocationPolicyOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public short value()
meth public void _delegate(org.omg.CosTransactions.InvocationPolicyOperations)
meth public void destroy()
supr org.omg.CosTransactions.InvocationPolicyPOA
hfds _impl,_poa

CLSS public abstract org.omg.CosTransactions.InvocationPolicyValueHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static short extract(org.omg.CORBA.Any)
meth public static short read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,short)
meth public static void write(org.omg.CORBA.portable.OutputStream,short)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.CosTransactions.NON_TX_TARGET_POLICY_TYPE
fld public final static int value = 57

CLSS public final org.omg.CosTransactions.NoTransaction
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosTransactions.NoTransactionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.NoTransaction extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.NoTransaction read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.NoTransaction)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.NoTransaction)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosTransactions.NoTransactionHolder
cons public init()
cons public init(org.omg.CosTransactions.NoTransaction)
fld public org.omg.CosTransactions.NoTransaction value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.NonTxTargetPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.CosTransactions.NonTxTargetPolicyOperations

CLSS public abstract org.omg.CosTransactions.NonTxTargetPolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.NonTxTargetPolicy extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.NonTxTargetPolicy narrow(org.omg.CORBA.Object)
meth public static org.omg.CosTransactions.NonTxTargetPolicy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CosTransactions.NonTxTargetPolicy unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.NonTxTargetPolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.NonTxTargetPolicy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosTransactions.NonTxTargetPolicyHolder
cons public init()
cons public init(org.omg.CosTransactions.NonTxTargetPolicy)
fld public org.omg.CosTransactions.NonTxTargetPolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.NonTxTargetPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract short value()

CLSS public abstract org.omg.CosTransactions.NonTxTargetPolicyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.CosTransactions.NonTxTargetPolicyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.CosTransactions.NonTxTargetPolicy _this()
meth public org.omg.CosTransactions.NonTxTargetPolicy _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CosTransactions.NonTxTargetPolicyPOATie
cons public init(org.omg.CosTransactions.NonTxTargetPolicyOperations)
cons public init(org.omg.CosTransactions.NonTxTargetPolicyOperations,org.omg.PortableServer.POA)
meth public int policy_type()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.CosTransactions.NonTxTargetPolicyOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public short value()
meth public void _delegate(org.omg.CosTransactions.NonTxTargetPolicyOperations)
meth public void destroy()
supr org.omg.CosTransactions.NonTxTargetPolicyPOA
hfds _impl,_poa

CLSS public abstract org.omg.CosTransactions.NonTxTargetPolicyValueHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static short extract(org.omg.CORBA.Any)
meth public static short read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,short)
meth public static void write(org.omg.CORBA.portable.OutputStream,short)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosTransactions.NotPrepared
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosTransactions.NotPreparedHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.NotPrepared extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.NotPrepared read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.NotPrepared)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.NotPrepared)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosTransactions.NotPreparedHolder
cons public init()
cons public init(org.omg.CosTransactions.NotPrepared)
fld public org.omg.CosTransactions.NotPrepared value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.CosTransactions.NotSubtransaction
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosTransactions.NotSubtransactionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.NotSubtransaction extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.NotSubtransaction read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.NotSubtransaction)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.NotSubtransaction)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosTransactions.NotSubtransactionHolder
cons public init()
cons public init(org.omg.CosTransactions.NotSubtransaction)
fld public org.omg.CosTransactions.NotSubtransaction value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.OTSPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.CosTransactions.OTSPolicyOperations

CLSS public abstract org.omg.CosTransactions.OTSPolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.OTSPolicy extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.OTSPolicy narrow(org.omg.CORBA.Object)
meth public static org.omg.CosTransactions.OTSPolicy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CosTransactions.OTSPolicy unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.OTSPolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.OTSPolicy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosTransactions.OTSPolicyHolder
cons public init()
cons public init(org.omg.CosTransactions.OTSPolicy)
fld public org.omg.CosTransactions.OTSPolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.OTSPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract short value()

CLSS public abstract org.omg.CosTransactions.OTSPolicyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.CosTransactions.OTSPolicyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.CosTransactions.OTSPolicy _this()
meth public org.omg.CosTransactions.OTSPolicy _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CosTransactions.OTSPolicyPOATie
cons public init(org.omg.CosTransactions.OTSPolicyOperations)
cons public init(org.omg.CosTransactions.OTSPolicyOperations,org.omg.PortableServer.POA)
meth public int policy_type()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.CosTransactions.OTSPolicyOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public short value()
meth public void _delegate(org.omg.CosTransactions.OTSPolicyOperations)
meth public void destroy()
supr org.omg.CosTransactions.OTSPolicyPOA
hfds _impl,_poa

CLSS public abstract org.omg.CosTransactions.OTSPolicyValueHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static short extract(org.omg.CORBA.Any)
meth public static short read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,short)
meth public static void write(org.omg.CORBA.portable.OutputStream,short)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.CosTransactions.OTS_POLICY_TYPE
fld public final static int value = 56

CLSS public abstract interface org.omg.CosTransactions.PERMIT
fld public final static short value = 1

CLSS public abstract interface org.omg.CosTransactions.PREVENT
fld public final static short value = 0

CLSS public final org.omg.CosTransactions.PropagationContext
cons public init()
cons public init(int,org.omg.CosTransactions.TransIdentity,org.omg.CosTransactions.TransIdentity[],org.omg.CORBA.Any)
fld public int timeout
fld public org.omg.CORBA.Any implementation_specific_data
fld public org.omg.CosTransactions.TransIdentity current
fld public org.omg.CosTransactions.TransIdentity[] parents
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CosTransactions.PropagationContextHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.PropagationContext extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.PropagationContext read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.PropagationContext)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.PropagationContext)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosTransactions.PropagationContextHolder
cons public init()
cons public init(org.omg.CosTransactions.PropagationContext)
fld public org.omg.CosTransactions.PropagationContext value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.REQUIRES
fld public final static short value = 1

CLSS public abstract interface org.omg.CosTransactions.RecoveryCoordinator
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.CosTransactions.RecoveryCoordinatorOperations

CLSS public abstract org.omg.CosTransactions.RecoveryCoordinatorHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.RecoveryCoordinator extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.RecoveryCoordinator narrow(org.omg.CORBA.Object)
meth public static org.omg.CosTransactions.RecoveryCoordinator read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CosTransactions.RecoveryCoordinator unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.RecoveryCoordinator)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.RecoveryCoordinator)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosTransactions.RecoveryCoordinatorHolder
cons public init()
cons public init(org.omg.CosTransactions.RecoveryCoordinator)
fld public org.omg.CosTransactions.RecoveryCoordinator value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.RecoveryCoordinatorOperations
meth public abstract org.omg.CosTransactions.Status replay_completion(org.omg.CosTransactions.Resource) throws org.omg.CosTransactions.NotPrepared

CLSS public abstract org.omg.CosTransactions.RecoveryCoordinatorPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.CosTransactions.RecoveryCoordinatorOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.CosTransactions.RecoveryCoordinator _this()
meth public org.omg.CosTransactions.RecoveryCoordinator _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CosTransactions.RecoveryCoordinatorPOATie
cons public init(org.omg.CosTransactions.RecoveryCoordinatorOperations)
cons public init(org.omg.CosTransactions.RecoveryCoordinatorOperations,org.omg.PortableServer.POA)
meth public org.omg.CosTransactions.RecoveryCoordinatorOperations _delegate()
meth public org.omg.CosTransactions.Status replay_completion(org.omg.CosTransactions.Resource) throws org.omg.CosTransactions.NotPrepared
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CosTransactions.RecoveryCoordinatorOperations)
supr org.omg.CosTransactions.RecoveryCoordinatorPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.CosTransactions.Resource
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.CosTransactions.ResourceOperations

CLSS public abstract org.omg.CosTransactions.ResourceHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.Resource extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.Resource narrow(org.omg.CORBA.Object)
meth public static org.omg.CosTransactions.Resource read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CosTransactions.Resource unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.Resource)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.Resource)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosTransactions.ResourceHolder
cons public init()
cons public init(org.omg.CosTransactions.Resource)
fld public org.omg.CosTransactions.Resource value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.ResourceOperations
meth public abstract org.omg.CosTransactions.Vote prepare() throws org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed
meth public abstract void commit() throws org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed,org.omg.CosTransactions.HeuristicRollback,org.omg.CosTransactions.NotPrepared
meth public abstract void commit_one_phase() throws org.omg.CosTransactions.HeuristicHazard
meth public abstract void forget()
meth public abstract void rollback() throws org.omg.CosTransactions.HeuristicCommit,org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed

CLSS public abstract org.omg.CosTransactions.ResourcePOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.CosTransactions.ResourceOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.CosTransactions.Resource _this()
meth public org.omg.CosTransactions.Resource _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CosTransactions.ResourcePOATie
cons public init(org.omg.CosTransactions.ResourceOperations)
cons public init(org.omg.CosTransactions.ResourceOperations,org.omg.PortableServer.POA)
meth public org.omg.CosTransactions.ResourceOperations _delegate()
meth public org.omg.CosTransactions.Vote prepare() throws org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CosTransactions.ResourceOperations)
meth public void commit() throws org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed,org.omg.CosTransactions.HeuristicRollback,org.omg.CosTransactions.NotPrepared
meth public void commit_one_phase() throws org.omg.CosTransactions.HeuristicHazard
meth public void forget()
meth public void rollback() throws org.omg.CosTransactions.HeuristicCommit,org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed
supr org.omg.CosTransactions.ResourcePOA
hfds _impl,_poa

CLSS public abstract interface org.omg.CosTransactions.SHARED
fld public final static short value = 1

CLSS public org.omg.CosTransactions.Status
cons protected init(int)
fld public final static int _StatusActive = 0
fld public final static int _StatusCommitted = 3
fld public final static int _StatusCommitting = 8
fld public final static int _StatusMarkedRollback = 1
fld public final static int _StatusNoTransaction = 6
fld public final static int _StatusPrepared = 2
fld public final static int _StatusPreparing = 7
fld public final static int _StatusRolledBack = 4
fld public final static int _StatusRollingBack = 9
fld public final static int _StatusUnknown = 5
fld public final static org.omg.CosTransactions.Status StatusActive
fld public final static org.omg.CosTransactions.Status StatusCommitted
fld public final static org.omg.CosTransactions.Status StatusCommitting
fld public final static org.omg.CosTransactions.Status StatusMarkedRollback
fld public final static org.omg.CosTransactions.Status StatusNoTransaction
fld public final static org.omg.CosTransactions.Status StatusPrepared
fld public final static org.omg.CosTransactions.Status StatusPreparing
fld public final static org.omg.CosTransactions.Status StatusRolledBack
fld public final static org.omg.CosTransactions.Status StatusRollingBack
fld public final static org.omg.CosTransactions.Status StatusUnknown
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.CosTransactions.Status from_int(int)
supr java.lang.Object
hfds __array,__size,__value

CLSS public abstract org.omg.CosTransactions.StatusHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.Status extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.Status read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.Status)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.Status)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosTransactions.StatusHolder
cons public init()
cons public init(org.omg.CosTransactions.Status)
fld public org.omg.CosTransactions.Status value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.SubtransactionAwareResource
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.CosTransactions.Resource
intf org.omg.CosTransactions.SubtransactionAwareResourceOperations

CLSS public abstract org.omg.CosTransactions.SubtransactionAwareResourceHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.SubtransactionAwareResource extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.SubtransactionAwareResource narrow(org.omg.CORBA.Object)
meth public static org.omg.CosTransactions.SubtransactionAwareResource read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CosTransactions.SubtransactionAwareResource unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.SubtransactionAwareResource)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.SubtransactionAwareResource)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosTransactions.SubtransactionAwareResourceHolder
cons public init()
cons public init(org.omg.CosTransactions.SubtransactionAwareResource)
fld public org.omg.CosTransactions.SubtransactionAwareResource value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.SubtransactionAwareResourceOperations
intf org.omg.CosTransactions.ResourceOperations
meth public abstract void commit_subtransaction(org.omg.CosTransactions.Coordinator)
meth public abstract void rollback_subtransaction()

CLSS public abstract org.omg.CosTransactions.SubtransactionAwareResourcePOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.CosTransactions.SubtransactionAwareResourceOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.CosTransactions.SubtransactionAwareResource _this()
meth public org.omg.CosTransactions.SubtransactionAwareResource _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CosTransactions.SubtransactionAwareResourcePOATie
cons public init(org.omg.CosTransactions.SubtransactionAwareResourceOperations)
cons public init(org.omg.CosTransactions.SubtransactionAwareResourceOperations,org.omg.PortableServer.POA)
meth public org.omg.CosTransactions.SubtransactionAwareResourceOperations _delegate()
meth public org.omg.CosTransactions.Vote prepare() throws org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CosTransactions.SubtransactionAwareResourceOperations)
meth public void commit() throws org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed,org.omg.CosTransactions.HeuristicRollback,org.omg.CosTransactions.NotPrepared
meth public void commit_one_phase() throws org.omg.CosTransactions.HeuristicHazard
meth public void commit_subtransaction(org.omg.CosTransactions.Coordinator)
meth public void forget()
meth public void rollback() throws org.omg.CosTransactions.HeuristicCommit,org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed
meth public void rollback_subtransaction()
supr org.omg.CosTransactions.SubtransactionAwareResourcePOA
hfds _impl,_poa

CLSS public final org.omg.CosTransactions.SubtransactionsUnavailable
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosTransactions.SubtransactionsUnavailableHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.SubtransactionsUnavailable extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.SubtransactionsUnavailable read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.SubtransactionsUnavailable)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.SubtransactionsUnavailable)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosTransactions.SubtransactionsUnavailableHolder
cons public init()
cons public init(org.omg.CosTransactions.SubtransactionsUnavailable)
fld public org.omg.CosTransactions.SubtransactionsUnavailable value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.Synchronization
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.CosTransactions.SynchronizationOperations

CLSS public abstract org.omg.CosTransactions.SynchronizationHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.Synchronization extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.Synchronization narrow(org.omg.CORBA.Object)
meth public static org.omg.CosTransactions.Synchronization read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CosTransactions.Synchronization unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.Synchronization)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.Synchronization)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosTransactions.SynchronizationHolder
cons public init()
cons public init(org.omg.CosTransactions.Synchronization)
fld public org.omg.CosTransactions.Synchronization value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.SynchronizationOperations
meth public abstract void after_completion(org.omg.CosTransactions.Status)
meth public abstract void before_completion()

CLSS public abstract org.omg.CosTransactions.SynchronizationPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.CosTransactions.SynchronizationOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.CosTransactions.Synchronization _this()
meth public org.omg.CosTransactions.Synchronization _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CosTransactions.SynchronizationPOATie
cons public init(org.omg.CosTransactions.SynchronizationOperations)
cons public init(org.omg.CosTransactions.SynchronizationOperations,org.omg.PortableServer.POA)
meth public org.omg.CosTransactions.SynchronizationOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CosTransactions.SynchronizationOperations)
meth public void after_completion(org.omg.CosTransactions.Status)
meth public void before_completion()
supr org.omg.CosTransactions.SynchronizationPOA
hfds _impl,_poa

CLSS public final org.omg.CosTransactions.SynchronizationUnavailable
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosTransactions.SynchronizationUnavailableHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.SynchronizationUnavailable extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.SynchronizationUnavailable read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.SynchronizationUnavailable)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.SynchronizationUnavailable)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosTransactions.SynchronizationUnavailableHolder
cons public init()
cons public init(org.omg.CosTransactions.SynchronizationUnavailable)
fld public org.omg.CosTransactions.SynchronizationUnavailable value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.Terminator
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.CosTransactions.TerminatorOperations

CLSS public abstract org.omg.CosTransactions.TerminatorHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.Terminator extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.Terminator narrow(org.omg.CORBA.Object)
meth public static org.omg.CosTransactions.Terminator read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CosTransactions.Terminator unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.Terminator)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.Terminator)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosTransactions.TerminatorHolder
cons public init()
cons public init(org.omg.CosTransactions.Terminator)
fld public org.omg.CosTransactions.Terminator value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.TerminatorOperations
meth public abstract void commit(boolean) throws org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed
meth public abstract void rollback()

CLSS public abstract org.omg.CosTransactions.TerminatorPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.CosTransactions.TerminatorOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.CosTransactions.Terminator _this()
meth public org.omg.CosTransactions.Terminator _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CosTransactions.TerminatorPOATie
cons public init(org.omg.CosTransactions.TerminatorOperations)
cons public init(org.omg.CosTransactions.TerminatorOperations,org.omg.PortableServer.POA)
meth public org.omg.CosTransactions.TerminatorOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CosTransactions.TerminatorOperations)
meth public void commit(boolean) throws org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed
meth public void rollback()
supr org.omg.CosTransactions.TerminatorPOA
hfds _impl,_poa

CLSS public final org.omg.CosTransactions.TransIdentity
cons public init()
cons public init(org.omg.CosTransactions.Coordinator,org.omg.CosTransactions.Terminator,org.omg.CosTransactions.otid_t)
fld public org.omg.CosTransactions.Coordinator coord
fld public org.omg.CosTransactions.Terminator term
fld public org.omg.CosTransactions.otid_t otid
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CosTransactions.TransIdentityHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.TransIdentity extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.TransIdentity read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.TransIdentity)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.TransIdentity)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosTransactions.TransIdentityHolder
cons public init()
cons public init(org.omg.CosTransactions.TransIdentity)
fld public org.omg.CosTransactions.TransIdentity value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.TransactionFactory
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.CosTransactions.TransactionFactoryOperations

CLSS public abstract org.omg.CosTransactions.TransactionFactoryHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.TransactionFactory extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.TransactionFactory narrow(org.omg.CORBA.Object)
meth public static org.omg.CosTransactions.TransactionFactory read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CosTransactions.TransactionFactory unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.TransactionFactory)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.TransactionFactory)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosTransactions.TransactionFactoryHolder
cons public init()
cons public init(org.omg.CosTransactions.TransactionFactory)
fld public org.omg.CosTransactions.TransactionFactory value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.TransactionFactoryOperations
meth public abstract org.omg.CosTransactions.Control create(int)
meth public abstract org.omg.CosTransactions.Control recreate(org.omg.CosTransactions.PropagationContext)

CLSS public abstract org.omg.CosTransactions.TransactionFactoryPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.CosTransactions.TransactionFactoryOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.CosTransactions.TransactionFactory _this()
meth public org.omg.CosTransactions.TransactionFactory _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CosTransactions.TransactionFactoryPOATie
cons public init(org.omg.CosTransactions.TransactionFactoryOperations)
cons public init(org.omg.CosTransactions.TransactionFactoryOperations,org.omg.PortableServer.POA)
meth public org.omg.CosTransactions.Control create(int)
meth public org.omg.CosTransactions.Control recreate(org.omg.CosTransactions.PropagationContext)
meth public org.omg.CosTransactions.TransactionFactoryOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CosTransactions.TransactionFactoryOperations)
supr org.omg.CosTransactions.TransactionFactoryPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.CosTransactions.TransactionalObject
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.CosTransactions.TransactionalObjectOperations

CLSS public abstract org.omg.CosTransactions.TransactionalObjectHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.TransactionalObject extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.TransactionalObject narrow(org.omg.CORBA.Object)
meth public static org.omg.CosTransactions.TransactionalObject read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CosTransactions.TransactionalObject unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.TransactionalObject)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.TransactionalObject)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosTransactions.TransactionalObjectHolder
cons public init()
cons public init(org.omg.CosTransactions.TransactionalObject)
fld public org.omg.CosTransactions.TransactionalObject value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.CosTransactions.TransactionalObjectOperations

CLSS public abstract org.omg.CosTransactions.TransactionalObjectPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.CosTransactions.TransactionalObjectOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.CosTransactions.TransactionalObject _this()
meth public org.omg.CosTransactions.TransactionalObject _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.CosTransactions.TransactionalObjectPOATie
cons public init(org.omg.CosTransactions.TransactionalObjectOperations)
cons public init(org.omg.CosTransactions.TransactionalObjectOperations,org.omg.PortableServer.POA)
meth public org.omg.CosTransactions.TransactionalObjectOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.CosTransactions.TransactionalObjectOperations)
supr org.omg.CosTransactions.TransactionalObjectPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.CosTransactions.UNSHARED
fld public final static short value = 2

CLSS public final org.omg.CosTransactions.Unavailable
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.CosTransactions.UnavailableHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.Unavailable extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.Unavailable read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.Unavailable)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.Unavailable)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosTransactions.UnavailableHolder
cons public init()
cons public init(org.omg.CosTransactions.Unavailable)
fld public org.omg.CosTransactions.Unavailable value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.CosTransactions.Vote
cons protected init(int)
fld public final static int _VoteCommit = 0
fld public final static int _VoteReadOnly = 2
fld public final static int _VoteRollback = 1
fld public final static org.omg.CosTransactions.Vote VoteCommit
fld public final static org.omg.CosTransactions.Vote VoteReadOnly
fld public final static org.omg.CosTransactions.Vote VoteRollback
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.CosTransactions.Vote from_int(int)
supr java.lang.Object
hfds __array,__size,__value

CLSS public abstract org.omg.CosTransactions.VoteHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.Vote extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.Vote read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.Vote)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.Vote)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.CosTransactions.VoteHolder
cons public init()
cons public init(org.omg.CosTransactions.Vote)
fld public org.omg.CosTransactions.Vote value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.CosTransactions._ControlStub
cons public init()
intf org.omg.CosTransactions.Control
meth public java.lang.String[] _ids()
meth public org.omg.CosTransactions.Coordinator get_coordinator() throws org.omg.CosTransactions.Unavailable
meth public org.omg.CosTransactions.Terminator get_terminator() throws org.omg.CosTransactions.Unavailable
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CosTransactions._CoordinatorStub
cons public init()
intf org.omg.CosTransactions.Coordinator
meth public boolean is_ancestor_transaction(org.omg.CosTransactions.Coordinator)
meth public boolean is_descendant_transaction(org.omg.CosTransactions.Coordinator)
meth public boolean is_related_transaction(org.omg.CosTransactions.Coordinator)
meth public boolean is_same_transaction(org.omg.CosTransactions.Coordinator)
meth public boolean is_top_level_transaction()
meth public int hash_top_level_tran()
meth public int hash_transaction()
meth public java.lang.String get_transaction_name()
meth public java.lang.String[] _ids()
meth public org.omg.CosTransactions.Control create_subtransaction() throws org.omg.CosTransactions.Inactive,org.omg.CosTransactions.SubtransactionsUnavailable
meth public org.omg.CosTransactions.PropagationContext get_txcontext() throws org.omg.CosTransactions.Unavailable
meth public org.omg.CosTransactions.RecoveryCoordinator register_resource(org.omg.CosTransactions.Resource) throws org.omg.CosTransactions.Inactive
meth public org.omg.CosTransactions.Status get_parent_status()
meth public org.omg.CosTransactions.Status get_status()
meth public org.omg.CosTransactions.Status get_top_level_status()
meth public void register_subtran_aware(org.omg.CosTransactions.SubtransactionAwareResource) throws org.omg.CosTransactions.Inactive,org.omg.CosTransactions.NotSubtransaction
meth public void register_synchronization(org.omg.CosTransactions.Synchronization) throws org.omg.CosTransactions.Inactive,org.omg.CosTransactions.SynchronizationUnavailable
meth public void rollback_only() throws org.omg.CosTransactions.Inactive
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CosTransactions._CurrentStub
cons public init()
intf org.omg.CosTransactions.Current
meth public int get_timeout()
meth public java.lang.String get_transaction_name()
meth public java.lang.String[] _ids()
meth public org.omg.CosTransactions.Control get_control()
meth public org.omg.CosTransactions.Control suspend()
meth public org.omg.CosTransactions.Status get_status()
meth public void begin() throws org.omg.CosTransactions.SubtransactionsUnavailable
meth public void commit(boolean) throws org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed,org.omg.CosTransactions.NoTransaction
meth public void resume(org.omg.CosTransactions.Control) throws org.omg.CosTransactions.InvalidControl
meth public void rollback() throws org.omg.CosTransactions.NoTransaction
meth public void rollback_only() throws org.omg.CosTransactions.NoTransaction
meth public void set_timeout(int)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CosTransactions._InvocationPolicyStub
cons public init()
intf org.omg.CosTransactions.InvocationPolicy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Policy copy()
meth public short value()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CosTransactions._NonTxTargetPolicyStub
cons public init()
intf org.omg.CosTransactions.NonTxTargetPolicy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Policy copy()
meth public short value()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CosTransactions._OTSPolicyStub
cons public init()
intf org.omg.CosTransactions.OTSPolicy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Policy copy()
meth public short value()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CosTransactions._RecoveryCoordinatorStub
cons public init()
intf org.omg.CosTransactions.RecoveryCoordinator
meth public java.lang.String[] _ids()
meth public org.omg.CosTransactions.Status replay_completion(org.omg.CosTransactions.Resource) throws org.omg.CosTransactions.NotPrepared
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CosTransactions._ResourceStub
cons public init()
intf org.omg.CosTransactions.Resource
meth public java.lang.String[] _ids()
meth public org.omg.CosTransactions.Vote prepare() throws org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed
meth public void commit() throws org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed,org.omg.CosTransactions.HeuristicRollback,org.omg.CosTransactions.NotPrepared
meth public void commit_one_phase() throws org.omg.CosTransactions.HeuristicHazard
meth public void forget()
meth public void rollback() throws org.omg.CosTransactions.HeuristicCommit,org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CosTransactions._SubtransactionAwareResourceStub
cons public init()
intf org.omg.CosTransactions.SubtransactionAwareResource
meth public java.lang.String[] _ids()
meth public org.omg.CosTransactions.Vote prepare() throws org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed
meth public void commit() throws org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed,org.omg.CosTransactions.HeuristicRollback,org.omg.CosTransactions.NotPrepared
meth public void commit_one_phase() throws org.omg.CosTransactions.HeuristicHazard
meth public void commit_subtransaction(org.omg.CosTransactions.Coordinator)
meth public void forget()
meth public void rollback() throws org.omg.CosTransactions.HeuristicCommit,org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed
meth public void rollback_subtransaction()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CosTransactions._SynchronizationStub
cons public init()
intf org.omg.CosTransactions.Synchronization
meth public java.lang.String[] _ids()
meth public void after_completion(org.omg.CosTransactions.Status)
meth public void before_completion()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CosTransactions._TerminatorStub
cons public init()
intf org.omg.CosTransactions.Terminator
meth public java.lang.String[] _ids()
meth public void commit(boolean) throws org.omg.CosTransactions.HeuristicHazard,org.omg.CosTransactions.HeuristicMixed
meth public void rollback()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CosTransactions._TransactionFactoryStub
cons public init()
intf org.omg.CosTransactions.TransactionFactory
meth public java.lang.String[] _ids()
meth public org.omg.CosTransactions.Control create(int)
meth public org.omg.CosTransactions.Control recreate(org.omg.CosTransactions.PropagationContext)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.CosTransactions._TransactionalObjectStub
cons public init()
intf org.omg.CosTransactions.TransactionalObject
meth public java.lang.String[] _ids()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public final org.omg.CosTransactions.otid_t
cons public init()
cons public init(int,int,byte[])
fld public byte[] tid
fld public int bqual_length
fld public int formatID
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.CosTransactions.otid_tHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CosTransactions.otid_t extract(org.omg.CORBA.Any)
meth public static org.omg.CosTransactions.otid_t read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CosTransactions.otid_t)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CosTransactions.otid_t)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.CosTransactions.otid_tHolder
cons public init()
cons public init(org.omg.CosTransactions.otid_t)
fld public org.omg.CosTransactions.otid_t value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.Dynamic.ContextListHelper
cons public init()
meth public static java.lang.String id()
meth public static java.lang.String[] extract(org.omg.CORBA.Any)
meth public static java.lang.String[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String[])
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract org.omg.Dynamic.ExceptionListHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.TypeCode[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.TypeCode[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.TypeCode[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.TypeCode[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Dynamic.ExceptionListHolder
cons public init()
cons public init(org.omg.CORBA.TypeCode[])
fld public org.omg.CORBA.TypeCode[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.Dynamic.Parameter
cons public init()
cons public init(org.omg.CORBA.Any,org.omg.CORBA.ParameterMode)
fld public org.omg.CORBA.Any argument
fld public org.omg.CORBA.ParameterMode mode
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.Dynamic.ParameterHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Dynamic.Parameter extract(org.omg.CORBA.Any)
meth public static org.omg.Dynamic.Parameter read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.Dynamic.Parameter)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Dynamic.Parameter)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.Dynamic.ParameterHolder
cons public init()
cons public init(org.omg.Dynamic.Parameter)
fld public org.omg.Dynamic.Parameter value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.Dynamic.ParameterListHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Dynamic.Parameter[] extract(org.omg.CORBA.Any)
meth public static org.omg.Dynamic.Parameter[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.Dynamic.Parameter[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Dynamic.Parameter[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Dynamic.ParameterListHolder
cons public init()
cons public init(org.omg.Dynamic.Parameter[])
fld public org.omg.Dynamic.Parameter[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.Dynamic.RequestContextHelper
cons public init()
meth public static java.lang.String id()
meth public static java.lang.String[] extract(org.omg.CORBA.Any)
meth public static java.lang.String[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String[])
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract org.omg.DynamicAny.AnySeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.Any[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.Any[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.Any[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.Any[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.DynamicAny.AnySeqHolder
cons public init()
cons public init(org.omg.CORBA.Any[])
fld public org.omg.CORBA.Any[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.DynamicAny.DynAny
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.DynamicAny.DynAnyOperations

CLSS public abstract interface org.omg.DynamicAny.DynAnyFactory
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.DynamicAny.DynAnyFactoryOperations

CLSS public abstract org.omg.DynamicAny.DynAnyFactoryHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.DynAnyFactory extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.DynAnyFactory narrow(org.omg.CORBA.Object)
meth public static org.omg.DynamicAny.DynAnyFactory read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.DynamicAny.DynAnyFactory unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.DynAnyFactory)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.DynAnyFactory)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.DynamicAny.DynAnyFactoryHolder
cons public init()
cons public init(org.omg.DynamicAny.DynAnyFactory)
fld public org.omg.DynamicAny.DynAnyFactory value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.DynamicAny.DynAnyFactoryOperations
meth public abstract org.omg.DynamicAny.DynAny create_dyn_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
meth public abstract org.omg.DynamicAny.DynAny create_dyn_any_from_type_code(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode

CLSS public abstract org.omg.DynamicAny.DynAnyFactoryPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.DynamicAny.DynAnyFactoryOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.DynamicAny.DynAnyFactory _this()
meth public org.omg.DynamicAny.DynAnyFactory _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.DynamicAny.DynAnyFactoryPOATie
cons public init(org.omg.DynamicAny.DynAnyFactoryOperations)
cons public init(org.omg.DynamicAny.DynAnyFactoryOperations,org.omg.PortableServer.POA)
meth public org.omg.DynamicAny.DynAny create_dyn_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
meth public org.omg.DynamicAny.DynAny create_dyn_any_from_type_code(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
meth public org.omg.DynamicAny.DynAnyFactoryOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.DynamicAny.DynAnyFactoryOperations)
supr org.omg.DynamicAny.DynAnyFactoryPOA
hfds _impl,_poa

CLSS public final org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCodeHolder
cons public init()
cons public init(org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode)
fld public org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.DynamicAny.DynAnyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.DynAny extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.DynAny narrow(org.omg.CORBA.Object)
meth public static org.omg.DynamicAny.DynAny read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.DynamicAny.DynAny unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.DynAny)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.DynAny)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.DynamicAny.DynAnyHolder
cons public init()
cons public init(org.omg.DynamicAny.DynAny)
fld public org.omg.DynamicAny.DynAny value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.DynamicAny.DynAnyOperations
meth public abstract boolean equal(org.omg.DynamicAny.DynAny)
meth public abstract boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract boolean next()
meth public abstract boolean seek(int)
meth public abstract byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract int component_count()
meth public abstract int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract org.omg.CORBA.Any to_any()
meth public abstract org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract org.omg.CORBA.TypeCode type()
meth public abstract org.omg.DynamicAny.DynAny copy()
meth public abstract org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void destroy()
meth public abstract void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void rewind()

CLSS public abstract org.omg.DynamicAny.DynAnyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.DynamicAny.DynAnyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.DynamicAny.DynAny _this()
meth public org.omg.DynamicAny.DynAny _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.DynamicAny.DynAnyPOATie
cons public init(org.omg.DynamicAny.DynAnyOperations)
cons public init(org.omg.DynamicAny.DynAnyOperations,org.omg.PortableServer.POA)
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAnyOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void _delegate(org.omg.DynamicAny.DynAnyOperations)
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
supr org.omg.DynamicAny.DynAnyPOA
hfds _impl,_poa

CLSS public final org.omg.DynamicAny.DynAnyPackage.InvalidValue
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.DynamicAny.DynAnyPackage.InvalidValueHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.DynAnyPackage.InvalidValue extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.DynAnyPackage.InvalidValue read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.DynAnyPackage.InvalidValue)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.DynAnyPackage.InvalidValue)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.DynamicAny.DynAnyPackage.InvalidValueHolder
cons public init()
cons public init(org.omg.DynamicAny.DynAnyPackage.InvalidValue)
fld public org.omg.DynamicAny.DynAnyPackage.InvalidValue value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.DynamicAny.DynAnyPackage.TypeMismatch
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.DynamicAny.DynAnyPackage.TypeMismatchHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.DynAnyPackage.TypeMismatch extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.DynAnyPackage.TypeMismatch read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.DynAnyPackage.TypeMismatch)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.DynAnyPackage.TypeMismatch)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.DynamicAny.DynAnyPackage.TypeMismatchHolder
cons public init()
cons public init(org.omg.DynamicAny.DynAnyPackage.TypeMismatch)
fld public org.omg.DynamicAny.DynAnyPackage.TypeMismatch value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.DynamicAny.DynAnySeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.DynAny[] extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.DynAny[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.DynAny[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.DynAny[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.DynamicAny.DynAnySeqHolder
cons public init()
cons public init(org.omg.DynamicAny.DynAny[])
fld public org.omg.DynamicAny.DynAny[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.DynamicAny.DynArray
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.DynamicAny.DynAny
intf org.omg.DynamicAny.DynArrayOperations

CLSS public abstract org.omg.DynamicAny.DynArrayHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.DynArray extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.DynArray narrow(org.omg.CORBA.Object)
meth public static org.omg.DynamicAny.DynArray read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.DynamicAny.DynArray unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.DynArray)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.DynArray)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.DynamicAny.DynArrayHolder
cons public init()
cons public init(org.omg.DynamicAny.DynArray)
fld public org.omg.DynamicAny.DynArray value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.DynamicAny.DynArrayOperations
intf org.omg.DynamicAny.DynAnyOperations
meth public abstract org.omg.CORBA.Any[] get_elements()
meth public abstract org.omg.DynamicAny.DynAny[] get_elements_as_dyn_any()
meth public abstract void set_elements(org.omg.CORBA.Any[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void set_elements_as_dyn_any(org.omg.DynamicAny.DynAny[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch

CLSS public abstract org.omg.DynamicAny.DynArrayPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.DynamicAny.DynArrayOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.DynamicAny.DynArray _this()
meth public org.omg.DynamicAny.DynArray _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.DynamicAny.DynArrayPOATie
cons public init(org.omg.DynamicAny.DynArrayOperations)
cons public init(org.omg.DynamicAny.DynArrayOperations,org.omg.PortableServer.POA)
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Any[] get_elements()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny[] get_elements_as_dyn_any()
meth public org.omg.DynamicAny.DynArrayOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void _delegate(org.omg.DynamicAny.DynArrayOperations)
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
meth public void set_elements(org.omg.CORBA.Any[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_elements_as_dyn_any(org.omg.DynamicAny.DynAny[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
supr org.omg.DynamicAny.DynArrayPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.DynamicAny.DynEnum
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.DynamicAny.DynAny
intf org.omg.DynamicAny.DynEnumOperations

CLSS public abstract org.omg.DynamicAny.DynEnumHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.DynEnum extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.DynEnum narrow(org.omg.CORBA.Object)
meth public static org.omg.DynamicAny.DynEnum read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.DynamicAny.DynEnum unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.DynEnum)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.DynEnum)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.DynamicAny.DynEnumHolder
cons public init()
cons public init(org.omg.DynamicAny.DynEnum)
fld public org.omg.DynamicAny.DynEnum value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.DynamicAny.DynEnumOperations
intf org.omg.DynamicAny.DynAnyOperations
meth public abstract int get_as_ulong()
meth public abstract java.lang.String get_as_string()
meth public abstract void set_as_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public abstract void set_as_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue

CLSS public abstract org.omg.DynamicAny.DynEnumPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.DynamicAny.DynEnumOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.DynamicAny.DynEnum _this()
meth public org.omg.DynamicAny.DynEnum _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.DynamicAny.DynEnumPOATie
cons public init(org.omg.DynamicAny.DynEnumOperations)
cons public init(org.omg.DynamicAny.DynEnumOperations,org.omg.PortableServer.POA)
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_as_ulong()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_as_string()
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynEnumOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void _delegate(org.omg.DynamicAny.DynEnumOperations)
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
meth public void set_as_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public void set_as_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
supr org.omg.DynamicAny.DynEnumPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.DynamicAny.DynFixed
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.DynamicAny.DynAny
intf org.omg.DynamicAny.DynFixedOperations

CLSS public abstract org.omg.DynamicAny.DynFixedHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.DynFixed extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.DynFixed narrow(org.omg.CORBA.Object)
meth public static org.omg.DynamicAny.DynFixed read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.DynamicAny.DynFixed unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.DynFixed)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.DynFixed)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.DynamicAny.DynFixedHolder
cons public init()
cons public init(org.omg.DynamicAny.DynFixed)
fld public org.omg.DynamicAny.DynFixed value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.DynamicAny.DynFixedOperations
intf org.omg.DynamicAny.DynAnyOperations
meth public abstract boolean set_value(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract java.lang.String get_value()

CLSS public abstract org.omg.DynamicAny.DynFixedPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.DynamicAny.DynFixedOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.DynamicAny.DynFixed _this()
meth public org.omg.DynamicAny.DynFixed _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.DynamicAny.DynFixedPOATie
cons public init(org.omg.DynamicAny.DynFixedOperations)
cons public init(org.omg.DynamicAny.DynFixedOperations,org.omg.PortableServer.POA)
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean next()
meth public boolean seek(int)
meth public boolean set_value(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_value()
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynFixedOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void _delegate(org.omg.DynamicAny.DynFixedOperations)
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
supr org.omg.DynamicAny.DynFixedPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.DynamicAny.DynSequence
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.DynamicAny.DynAny
intf org.omg.DynamicAny.DynSequenceOperations

CLSS public abstract org.omg.DynamicAny.DynSequenceHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.DynSequence extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.DynSequence narrow(org.omg.CORBA.Object)
meth public static org.omg.DynamicAny.DynSequence read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.DynamicAny.DynSequence unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.DynSequence)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.DynSequence)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.DynamicAny.DynSequenceHolder
cons public init()
cons public init(org.omg.DynamicAny.DynSequence)
fld public org.omg.DynamicAny.DynSequence value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.DynamicAny.DynSequenceOperations
intf org.omg.DynamicAny.DynAnyOperations
meth public abstract int get_length()
meth public abstract org.omg.CORBA.Any[] get_elements()
meth public abstract org.omg.DynamicAny.DynAny[] get_elements_as_dyn_any()
meth public abstract void set_elements(org.omg.CORBA.Any[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void set_elements_as_dyn_any(org.omg.DynamicAny.DynAny[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void set_length(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue

CLSS public abstract org.omg.DynamicAny.DynSequencePOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.DynamicAny.DynSequenceOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.DynamicAny.DynSequence _this()
meth public org.omg.DynamicAny.DynSequence _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.DynamicAny.DynSequencePOATie
cons public init(org.omg.DynamicAny.DynSequenceOperations)
cons public init(org.omg.DynamicAny.DynSequenceOperations,org.omg.PortableServer.POA)
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_length()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Any[] get_elements()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny[] get_elements_as_dyn_any()
meth public org.omg.DynamicAny.DynSequenceOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void _delegate(org.omg.DynamicAny.DynSequenceOperations)
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
meth public void set_elements(org.omg.CORBA.Any[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_elements_as_dyn_any(org.omg.DynamicAny.DynAny[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_length(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
supr org.omg.DynamicAny.DynSequencePOA
hfds _impl,_poa

CLSS public abstract interface org.omg.DynamicAny.DynStruct
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.DynamicAny.DynAny
intf org.omg.DynamicAny.DynStructOperations

CLSS public abstract org.omg.DynamicAny.DynStructHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.DynStruct extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.DynStruct narrow(org.omg.CORBA.Object)
meth public static org.omg.DynamicAny.DynStruct read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.DynamicAny.DynStruct unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.DynStruct)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.DynStruct)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.DynamicAny.DynStructHolder
cons public init()
cons public init(org.omg.DynamicAny.DynStruct)
fld public org.omg.DynamicAny.DynStruct value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.DynamicAny.DynStructOperations
intf org.omg.DynamicAny.DynAnyOperations
meth public abstract java.lang.String current_member_name() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract org.omg.CORBA.TCKind current_member_kind() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract org.omg.DynamicAny.NameDynAnyPair[] get_members_as_dyn_any()
meth public abstract org.omg.DynamicAny.NameValuePair[] get_members()
meth public abstract void set_members(org.omg.DynamicAny.NameValuePair[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void set_members_as_dyn_any(org.omg.DynamicAny.NameDynAnyPair[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch

CLSS public abstract org.omg.DynamicAny.DynStructPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.DynamicAny.DynStructOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.DynamicAny.DynStruct _this()
meth public org.omg.DynamicAny.DynStruct _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.DynamicAny.DynStructPOATie
cons public init(org.omg.DynamicAny.DynStructOperations)
cons public init(org.omg.DynamicAny.DynStructOperations,org.omg.PortableServer.POA)
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String current_member_name() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TCKind current_member_kind() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynStructOperations _delegate()
meth public org.omg.DynamicAny.NameDynAnyPair[] get_members_as_dyn_any()
meth public org.omg.DynamicAny.NameValuePair[] get_members()
meth public org.omg.PortableServer.POA _default_POA()
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void _delegate(org.omg.DynamicAny.DynStructOperations)
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
meth public void set_members(org.omg.DynamicAny.NameValuePair[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_members_as_dyn_any(org.omg.DynamicAny.NameDynAnyPair[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
supr org.omg.DynamicAny.DynStructPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.DynamicAny.DynUnion
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.DynamicAny.DynAny
intf org.omg.DynamicAny.DynUnionOperations

CLSS public abstract org.omg.DynamicAny.DynUnionHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.DynUnion extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.DynUnion narrow(org.omg.CORBA.Object)
meth public static org.omg.DynamicAny.DynUnion read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.DynamicAny.DynUnion unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.DynUnion)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.DynUnion)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.DynamicAny.DynUnionHolder
cons public init()
cons public init(org.omg.DynamicAny.DynUnion)
fld public org.omg.DynamicAny.DynUnion value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.DynamicAny.DynUnionOperations
intf org.omg.DynamicAny.DynAnyOperations
meth public abstract boolean has_no_active_member()
meth public abstract java.lang.String member_name() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public abstract org.omg.CORBA.TCKind discriminator_kind()
meth public abstract org.omg.CORBA.TCKind member_kind() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public abstract org.omg.DynamicAny.DynAny get_discriminator()
meth public abstract org.omg.DynamicAny.DynAny member() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public abstract void set_discriminator(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void set_to_default_member() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void set_to_no_active_member() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch

CLSS public abstract org.omg.DynamicAny.DynUnionPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.DynamicAny.DynUnionOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.DynamicAny.DynUnion _this()
meth public org.omg.DynamicAny.DynUnion _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.DynamicAny.DynUnionPOATie
cons public init(org.omg.DynamicAny.DynUnionOperations)
cons public init(org.omg.DynamicAny.DynUnionOperations,org.omg.PortableServer.POA)
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean has_no_active_member()
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String member_name() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TCKind discriminator_kind()
meth public org.omg.CORBA.TCKind member_kind() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_discriminator()
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny member() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public org.omg.DynamicAny.DynUnionOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void _delegate(org.omg.DynamicAny.DynUnionOperations)
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
meth public void set_discriminator(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_to_default_member() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_to_no_active_member() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
supr org.omg.DynamicAny.DynUnionPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.DynamicAny.DynValue
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.DynamicAny.DynValueCommon
intf org.omg.DynamicAny.DynValueOperations

CLSS public abstract interface org.omg.DynamicAny.DynValueBox
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.DynamicAny.DynValueBoxOperations
intf org.omg.DynamicAny.DynValueCommon

CLSS public abstract org.omg.DynamicAny.DynValueBoxHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.DynValueBox extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.DynValueBox narrow(org.omg.CORBA.Object)
meth public static org.omg.DynamicAny.DynValueBox read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.DynamicAny.DynValueBox unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.DynValueBox)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.DynValueBox)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.DynamicAny.DynValueBoxHolder
cons public init()
cons public init(org.omg.DynamicAny.DynValueBox)
fld public org.omg.DynamicAny.DynValueBox value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.DynamicAny.DynValueBoxOperations
intf org.omg.DynamicAny.DynValueCommonOperations
meth public abstract org.omg.CORBA.Any get_boxed_value() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public abstract org.omg.DynamicAny.DynAny get_boxed_value_as_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public abstract void set_boxed_value(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void set_boxed_value_as_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch

CLSS public abstract org.omg.DynamicAny.DynValueBoxPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.DynamicAny.DynValueBoxOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.DynamicAny.DynValueBox _this()
meth public org.omg.DynamicAny.DynValueBox _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.DynamicAny.DynValueBoxPOATie
cons public init(org.omg.DynamicAny.DynValueBoxOperations)
cons public init(org.omg.DynamicAny.DynValueBoxOperations,org.omg.PortableServer.POA)
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean is_null()
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_boxed_value() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_boxed_value_as_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynValueBoxOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void _delegate(org.omg.DynamicAny.DynValueBoxOperations)
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
meth public void set_boxed_value(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_boxed_value_as_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_to_null()
meth public void set_to_value()
supr org.omg.DynamicAny.DynValueBoxPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.DynamicAny.DynValueCommon
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.DynamicAny.DynAny
intf org.omg.DynamicAny.DynValueCommonOperations

CLSS public abstract org.omg.DynamicAny.DynValueCommonHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.DynValueCommon extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.DynValueCommon narrow(org.omg.CORBA.Object)
meth public static org.omg.DynamicAny.DynValueCommon read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.DynamicAny.DynValueCommon unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.DynValueCommon)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.DynValueCommon)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.DynamicAny.DynValueCommonHolder
cons public init()
cons public init(org.omg.DynamicAny.DynValueCommon)
fld public org.omg.DynamicAny.DynValueCommon value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.DynamicAny.DynValueCommonOperations
intf org.omg.DynamicAny.DynAnyOperations
meth public abstract boolean is_null()
meth public abstract void set_to_null()
meth public abstract void set_to_value()

CLSS public abstract org.omg.DynamicAny.DynValueCommonPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.DynamicAny.DynValueCommonOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.DynamicAny.DynValueCommon _this()
meth public org.omg.DynamicAny.DynValueCommon _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.DynamicAny.DynValueCommonPOATie
cons public init(org.omg.DynamicAny.DynValueCommonOperations)
cons public init(org.omg.DynamicAny.DynValueCommonOperations,org.omg.PortableServer.POA)
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean is_null()
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynValueCommonOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void _delegate(org.omg.DynamicAny.DynValueCommonOperations)
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
meth public void set_to_null()
meth public void set_to_value()
supr org.omg.DynamicAny.DynValueCommonPOA
hfds _impl,_poa

CLSS public abstract org.omg.DynamicAny.DynValueHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.DynValue extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.DynValue narrow(org.omg.CORBA.Object)
meth public static org.omg.DynamicAny.DynValue read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.DynamicAny.DynValue unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.DynValue)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.DynValue)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.DynamicAny.DynValueHolder
cons public init()
cons public init(org.omg.DynamicAny.DynValue)
fld public org.omg.DynamicAny.DynValue value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.DynamicAny.DynValueOperations
intf org.omg.DynamicAny.DynValueCommonOperations
meth public abstract java.lang.String current_member_name() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract org.omg.CORBA.TCKind current_member_kind() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract org.omg.DynamicAny.NameDynAnyPair[] get_members_as_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public abstract org.omg.DynamicAny.NameValuePair[] get_members() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public abstract void set_members(org.omg.DynamicAny.NameValuePair[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public abstract void set_members_as_dyn_any(org.omg.DynamicAny.NameDynAnyPair[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch

CLSS public abstract org.omg.DynamicAny.DynValuePOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.DynamicAny.DynValueOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.DynamicAny.DynValue _this()
meth public org.omg.DynamicAny.DynValue _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.DynamicAny.DynValuePOATie
cons public init(org.omg.DynamicAny.DynValueOperations)
cons public init(org.omg.DynamicAny.DynValueOperations,org.omg.PortableServer.POA)
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean is_null()
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String current_member_name() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TCKind current_member_kind() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynValueOperations _delegate()
meth public org.omg.DynamicAny.NameDynAnyPair[] get_members_as_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public org.omg.DynamicAny.NameValuePair[] get_members() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public org.omg.PortableServer.POA _default_POA()
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void _delegate(org.omg.DynamicAny.DynValueOperations)
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
meth public void set_members(org.omg.DynamicAny.NameValuePair[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_members_as_dyn_any(org.omg.DynamicAny.NameDynAnyPair[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_to_null()
meth public void set_to_value()
supr org.omg.DynamicAny.DynValuePOA
hfds _impl,_poa

CLSS public abstract org.omg.DynamicAny.FieldNameHelper
cons public init()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.DynamicAny.NameDynAnyPair
cons public init()
cons public init(java.lang.String,org.omg.DynamicAny.DynAny)
fld public java.lang.String id
fld public org.omg.DynamicAny.DynAny value
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.DynamicAny.NameDynAnyPairHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.NameDynAnyPair extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.NameDynAnyPair read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.NameDynAnyPair)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.NameDynAnyPair)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.DynamicAny.NameDynAnyPairHolder
cons public init()
cons public init(org.omg.DynamicAny.NameDynAnyPair)
fld public org.omg.DynamicAny.NameDynAnyPair value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.DynamicAny.NameDynAnyPairSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.NameDynAnyPair[] extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.NameDynAnyPair[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.NameDynAnyPair[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.NameDynAnyPair[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.DynamicAny.NameDynAnyPairSeqHolder
cons public init()
cons public init(org.omg.DynamicAny.NameDynAnyPair[])
fld public org.omg.DynamicAny.NameDynAnyPair[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.DynamicAny.NameValuePair
cons public init()
cons public init(java.lang.String,org.omg.CORBA.Any)
fld public java.lang.String id
fld public org.omg.CORBA.Any value
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.DynamicAny.NameValuePairHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.NameValuePair extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.NameValuePair read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.NameValuePair)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.NameValuePair)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.DynamicAny.NameValuePairHolder
cons public init()
cons public init(org.omg.DynamicAny.NameValuePair)
fld public org.omg.DynamicAny.NameValuePair value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.DynamicAny.NameValuePairSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.DynamicAny.NameValuePair[] extract(org.omg.CORBA.Any)
meth public static org.omg.DynamicAny.NameValuePair[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.DynamicAny.NameValuePair[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.DynamicAny.NameValuePair[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.DynamicAny.NameValuePairSeqHolder
cons public init()
cons public init(org.omg.DynamicAny.NameValuePair[])
fld public org.omg.DynamicAny.NameValuePair[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.DynamicAny._DynAnyFactoryStub
cons public init()
fld public final static java.lang.Class _opsClass
intf org.omg.DynamicAny.DynAnyFactory
meth public java.lang.String[] _ids()
meth public org.omg.DynamicAny.DynAny create_dyn_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
meth public org.omg.DynamicAny.DynAny create_dyn_any_from_type_code(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyFactoryPackage.InconsistentTypeCode
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.DynamicAny._DynAnyStub
cons public init()
fld public final static java.lang.Class _opsClass
intf org.omg.DynamicAny.DynAny
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String[] _ids()
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.DynamicAny._DynArrayStub
cons public init()
fld public final static java.lang.Class _opsClass
intf org.omg.DynamicAny.DynArray
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String[] _ids()
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Any[] get_elements()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny[] get_elements_as_dyn_any()
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
meth public void set_elements(org.omg.CORBA.Any[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_elements_as_dyn_any(org.omg.DynamicAny.DynAny[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.DynamicAny._DynEnumStub
cons public init()
fld public final static java.lang.Class _opsClass
intf org.omg.DynamicAny.DynEnum
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_as_ulong()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_as_string()
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String[] _ids()
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
meth public void set_as_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public void set_as_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.DynamicAny._DynFixedStub
cons public init()
fld public final static java.lang.Class _opsClass
intf org.omg.DynamicAny.DynFixed
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean next()
meth public boolean seek(int)
meth public boolean set_value(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_value()
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String[] _ids()
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.DynamicAny._DynSequenceStub
cons public init()
fld public final static java.lang.Class _opsClass
intf org.omg.DynamicAny.DynSequence
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_length()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String[] _ids()
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Any[] get_elements()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny[] get_elements_as_dyn_any()
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
meth public void set_elements(org.omg.CORBA.Any[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_elements_as_dyn_any(org.omg.DynamicAny.DynAny[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_length(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.DynamicAny._DynStructStub
cons public init()
fld public final static java.lang.Class _opsClass
intf org.omg.DynamicAny.DynStruct
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String current_member_name() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String[] _ids()
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TCKind current_member_kind() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.NameDynAnyPair[] get_members_as_dyn_any()
meth public org.omg.DynamicAny.NameValuePair[] get_members()
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
meth public void set_members(org.omg.DynamicAny.NameValuePair[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_members_as_dyn_any(org.omg.DynamicAny.NameDynAnyPair[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.DynamicAny._DynUnionStub
cons public init()
fld public final static java.lang.Class _opsClass
intf org.omg.DynamicAny.DynUnion
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean has_no_active_member()
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String member_name() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public java.lang.String[] _ids()
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TCKind discriminator_kind()
meth public org.omg.CORBA.TCKind member_kind() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_discriminator()
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny member() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
meth public void set_discriminator(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_to_default_member() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_to_no_active_member() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.DynamicAny._DynValueBoxStub
cons public init()
intf org.omg.DynamicAny.DynValueBox
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean is_null()
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String[] _ids()
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_boxed_value() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_boxed_value_as_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
meth public void set_boxed_value(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_boxed_value_as_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_to_null()
meth public void set_to_value()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.DynamicAny._DynValueCommonStub
cons public init()
intf org.omg.DynamicAny.DynValueCommon
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean is_null()
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String[] _ids()
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
meth public void set_to_null()
meth public void set_to_value()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.DynamicAny._DynValueStub
cons public init()
fld public final static java.lang.Class _opsClass
intf org.omg.DynamicAny.DynValue
meth public boolean equal(org.omg.DynamicAny.DynAny)
meth public boolean get_boolean() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public boolean is_null()
meth public boolean next()
meth public boolean seek(int)
meth public byte get_octet() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_char() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public char get_wchar() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public double get_double() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public float get_float() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int component_count()
meth public int get_long() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public int get_ulong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.io.Serializable get_val() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String current_member_name() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_string() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String get_wstring() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public java.lang.String[] _ids()
meth public long get_longlong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public long get_ulonglong() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any get_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.Any to_any()
meth public org.omg.CORBA.Object get_reference() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TCKind current_member_kind() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode get_typecode() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.CORBA.TypeCode type()
meth public org.omg.DynamicAny.DynAny copy()
meth public org.omg.DynamicAny.DynAny current_component() throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.DynAny get_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public org.omg.DynamicAny.NameDynAnyPair[] get_members_as_dyn_any() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public org.omg.DynamicAny.NameValuePair[] get_members() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue
meth public short get_short() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public short get_ushort() throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void assign(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void destroy()
meth public void from_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_any(org.omg.CORBA.Any) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_boolean(boolean) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_char(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_double(double) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_dyn_any(org.omg.DynamicAny.DynAny) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_float(float) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_long(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_longlong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_octet(byte) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_reference(org.omg.CORBA.Object) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_short(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_string(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_typecode(org.omg.CORBA.TypeCode) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulong(int) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ulonglong(long) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_ushort(short) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_val(java.io.Serializable) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wchar(char) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void insert_wstring(java.lang.String) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void rewind()
meth public void set_members(org.omg.DynamicAny.NameValuePair[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_members_as_dyn_any(org.omg.DynamicAny.NameDynAnyPair[]) throws org.omg.DynamicAny.DynAnyPackage.InvalidValue,org.omg.DynamicAny.DynAnyPackage.TypeMismatch
meth public void set_to_null()
meth public void set_to_value()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public abstract interface org.omg.IOP.BI_DIR_IIOP
fld public final static int value = 5

CLSS public abstract interface org.omg.IOP.ChainBypassCheck
fld public final static int value = 2

CLSS public abstract interface org.omg.IOP.ChainBypassInfo
fld public final static int value = 3

CLSS public abstract interface org.omg.IOP.CodeSets
fld public final static int value = 1

CLSS public abstract interface org.omg.IOP.Codec
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.IOP.CodecOperations

CLSS public abstract interface org.omg.IOP.CodecFactory
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.IOP.CodecFactoryOperations

CLSS public abstract org.omg.IOP.CodecFactoryHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.IOP.CodecFactory extract(org.omg.CORBA.Any)
meth public static org.omg.IOP.CodecFactory narrow(org.omg.CORBA.Object)
meth public static org.omg.IOP.CodecFactory read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.IOP.CodecFactory unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.IOP.CodecFactory)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.IOP.CodecFactory)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.IOP.CodecFactoryHolder
cons public init()
cons public init(org.omg.IOP.CodecFactory)
fld public org.omg.IOP.CodecFactory value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.IOP.CodecFactoryOperations
meth public abstract org.omg.IOP.Codec create_codec(org.omg.IOP.Encoding) throws org.omg.IOP.CodecFactoryPackage.UnknownEncoding

CLSS public final org.omg.IOP.CodecFactoryPackage.UnknownEncoding
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.IOP.CodecFactoryPackage.UnknownEncodingHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.IOP.CodecFactoryPackage.UnknownEncoding extract(org.omg.CORBA.Any)
meth public static org.omg.IOP.CodecFactoryPackage.UnknownEncoding read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.IOP.CodecFactoryPackage.UnknownEncoding)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.IOP.CodecFactoryPackage.UnknownEncoding)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.IOP.CodecFactoryPackage.UnknownEncodingHolder
cons public init()
cons public init(org.omg.IOP.CodecFactoryPackage.UnknownEncoding)
fld public org.omg.IOP.CodecFactoryPackage.UnknownEncoding value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.IOP.CodecHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.IOP.Codec extract(org.omg.CORBA.Any)
meth public static org.omg.IOP.Codec narrow(org.omg.CORBA.Object)
meth public static org.omg.IOP.Codec read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.IOP.Codec unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.IOP.Codec)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.IOP.Codec)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.IOP.CodecHolder
cons public init()
cons public init(org.omg.IOP.Codec)
fld public org.omg.IOP.Codec value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.IOP.CodecOperations
meth public abstract byte[] encode(org.omg.CORBA.Any) throws org.omg.IOP.CodecPackage.InvalidTypeForEncoding
meth public abstract byte[] encode_value(org.omg.CORBA.Any) throws org.omg.IOP.CodecPackage.InvalidTypeForEncoding
meth public abstract org.omg.CORBA.Any decode(byte[]) throws org.omg.IOP.CodecPackage.FormatMismatch
meth public abstract org.omg.CORBA.Any decode_value(byte[],org.omg.CORBA.TypeCode) throws org.omg.IOP.CodecPackage.FormatMismatch,org.omg.IOP.CodecPackage.TypeMismatch

CLSS public final org.omg.IOP.CodecPackage.FormatMismatch
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.IOP.CodecPackage.FormatMismatchHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.IOP.CodecPackage.FormatMismatch extract(org.omg.CORBA.Any)
meth public static org.omg.IOP.CodecPackage.FormatMismatch read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.IOP.CodecPackage.FormatMismatch)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.IOP.CodecPackage.FormatMismatch)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.IOP.CodecPackage.FormatMismatchHolder
cons public init()
cons public init(org.omg.IOP.CodecPackage.FormatMismatch)
fld public org.omg.IOP.CodecPackage.FormatMismatch value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.IOP.CodecPackage.InvalidTypeForEncoding
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.IOP.CodecPackage.InvalidTypeForEncodingHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.IOP.CodecPackage.InvalidTypeForEncoding extract(org.omg.CORBA.Any)
meth public static org.omg.IOP.CodecPackage.InvalidTypeForEncoding read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.IOP.CodecPackage.InvalidTypeForEncoding)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.IOP.CodecPackage.InvalidTypeForEncoding)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.IOP.CodecPackage.InvalidTypeForEncodingHolder
cons public init()
cons public init(org.omg.IOP.CodecPackage.InvalidTypeForEncoding)
fld public org.omg.IOP.CodecPackage.InvalidTypeForEncoding value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.IOP.CodecPackage.TypeMismatch
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.IOP.CodecPackage.TypeMismatchHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.IOP.CodecPackage.TypeMismatch extract(org.omg.CORBA.Any)
meth public static org.omg.IOP.CodecPackage.TypeMismatch read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.IOP.CodecPackage.TypeMismatch)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.IOP.CodecPackage.TypeMismatch)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.IOP.CodecPackage.TypeMismatchHolder
cons public init()
cons public init(org.omg.IOP.CodecPackage.TypeMismatch)
fld public org.omg.IOP.CodecPackage.TypeMismatch value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.IOP.ComponentIdHelper
cons public init()
meth public static int extract(org.omg.CORBA.Any)
meth public static int read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,int)
meth public static void write(org.omg.CORBA.portable.OutputStream,int)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.IOP.ENCODING_CDR_ENCAPS
fld public final static short value = 0

CLSS public final org.omg.IOP.Encoding
cons public init()
cons public init(short,byte,byte)
fld public byte major_version
fld public byte minor_version
fld public short format
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.IOP.EncodingFormatHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static short extract(org.omg.CORBA.Any)
meth public static short read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,short)
meth public static void write(org.omg.CORBA.portable.OutputStream,short)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract org.omg.IOP.EncodingHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.IOP.Encoding extract(org.omg.CORBA.Any)
meth public static org.omg.IOP.Encoding read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.IOP.Encoding)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.IOP.Encoding)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.IOP.EncodingHolder
cons public init()
cons public init(org.omg.IOP.Encoding)
fld public org.omg.IOP.Encoding value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.IOP.ExceptionDetailMessage
fld public final static int value = 14

CLSS public abstract interface org.omg.IOP.FORWARDED_IDENTITY
fld public final static int value = 8

CLSS public abstract interface org.omg.IOP.INVOCATION_POLICIES
fld public final static int value = 7

CLSS public final org.omg.IOP.IOR
cons public init()
cons public init(java.lang.String,org.omg.IOP.TaggedProfile[])
fld public java.lang.String type_id
fld public org.omg.IOP.TaggedProfile[] profiles
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.IOP.IORHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.IOP.IOR extract(org.omg.CORBA.Any)
meth public static org.omg.IOP.IOR read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.IOP.IOR)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.IOP.IOR)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.IOP.IORHolder
cons public init()
cons public init(org.omg.IOP.IOR)
fld public org.omg.IOP.IOR value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.IOP.LogicalThreadId
fld public final static int value = 4

CLSS public abstract org.omg.IOP.MultipleComponentProfileHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.IOP.TaggedComponent[] extract(org.omg.CORBA.Any)
meth public static org.omg.IOP.TaggedComponent[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.IOP.TaggedComponent[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.IOP.TaggedComponent[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.IOP.MultipleComponentProfileHolder
cons public init()
cons public init(org.omg.IOP.TaggedComponent[])
fld public org.omg.IOP.TaggedComponent[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.IOP.ProfileIdHelper
cons public init()
meth public static int extract(org.omg.CORBA.Any)
meth public static int read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,int)
meth public static void write(org.omg.CORBA.portable.OutputStream,int)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.IOP.RMICustomMaxStreamFormat
fld public final static int value = 17

CLSS public abstract interface org.omg.IOP.SendingContextRunTime
fld public final static int value = 6

CLSS public final org.omg.IOP.ServiceContext
cons public init()
cons public init(int,byte[])
fld public byte[] context_data
fld public int context_id
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.IOP.ServiceContextHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.IOP.ServiceContext extract(org.omg.CORBA.Any)
meth public static org.omg.IOP.ServiceContext read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.IOP.ServiceContext)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.IOP.ServiceContext)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.IOP.ServiceContextHolder
cons public init()
cons public init(org.omg.IOP.ServiceContext)
fld public org.omg.IOP.ServiceContext value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.IOP.ServiceContextListHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.IOP.ServiceContext[] extract(org.omg.CORBA.Any)
meth public static org.omg.IOP.ServiceContext[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.IOP.ServiceContext[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.IOP.ServiceContext[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.IOP.ServiceContextListHolder
cons public init()
cons public init(org.omg.IOP.ServiceContext[])
fld public org.omg.IOP.ServiceContext[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.IOP.ServiceIdHelper
cons public init()
meth public static int extract(org.omg.CORBA.Any)
meth public static int read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,int)
meth public static void write(org.omg.CORBA.portable.OutputStream,int)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.IOP.TAG_ALTERNATE_IIOP_ADDRESS
fld public final static int value = 3

CLSS public abstract interface org.omg.IOP.TAG_CODE_SETS
fld public final static int value = 1

CLSS public abstract interface org.omg.IOP.TAG_INTERNET_IOP
fld public final static int value = 0

CLSS public abstract interface org.omg.IOP.TAG_JAVA_CODEBASE
fld public final static int value = 25

CLSS public abstract interface org.omg.IOP.TAG_MULTIPLE_COMPONENTS
fld public final static int value = 1

CLSS public abstract interface org.omg.IOP.TAG_ORB_TYPE
fld public final static int value = 0

CLSS public abstract interface org.omg.IOP.TAG_POLICIES
fld public final static int value = 2

CLSS public abstract interface org.omg.IOP.TAG_RMI_CUSTOM_MAX_STREAM_FORMAT
fld public final static int value = 38

CLSS public final org.omg.IOP.TaggedComponent
cons public init()
cons public init(int,byte[])
fld public byte[] component_data
fld public int tag
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.IOP.TaggedComponentHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.IOP.TaggedComponent extract(org.omg.CORBA.Any)
meth public static org.omg.IOP.TaggedComponent read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.IOP.TaggedComponent)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.IOP.TaggedComponent)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.IOP.TaggedComponentHolder
cons public init()
cons public init(org.omg.IOP.TaggedComponent)
fld public org.omg.IOP.TaggedComponent value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.IOP.TaggedComponentSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.IOP.TaggedComponent[] extract(org.omg.CORBA.Any)
meth public static org.omg.IOP.TaggedComponent[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.IOP.TaggedComponent[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.IOP.TaggedComponent[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.IOP.TaggedComponentSeqHolder
cons public init()
cons public init(org.omg.IOP.TaggedComponent[])
fld public org.omg.IOP.TaggedComponent[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.IOP.TaggedProfile
cons public init()
cons public init(int,byte[])
fld public byte[] profile_data
fld public int tag
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.IOP.TaggedProfileHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.IOP.TaggedProfile extract(org.omg.CORBA.Any)
meth public static org.omg.IOP.TaggedProfile read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.IOP.TaggedProfile)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.IOP.TaggedProfile)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.IOP.TaggedProfileHolder
cons public init()
cons public init(org.omg.IOP.TaggedProfile)
fld public org.omg.IOP.TaggedProfile value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.IOP.TransactionService
fld public final static int value = 0

CLSS public abstract interface org.omg.IOP.UnknownExceptionInfo
fld public final static int value = 9

CLSS public abstract interface org.omg.Messaging.MAX_HOPS_POLICY_TYPE
fld public final static int value = 34

CLSS public abstract interface org.omg.Messaging.MaxHopsPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.Messaging.MaxHopsPolicyOperations

CLSS public abstract org.omg.Messaging.MaxHopsPolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.MaxHopsPolicy extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.MaxHopsPolicy narrow(org.omg.CORBA.Object)
meth public static org.omg.Messaging.MaxHopsPolicy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.Messaging.MaxHopsPolicy unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.MaxHopsPolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.MaxHopsPolicy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.MaxHopsPolicyHolder
cons public init()
cons public init(org.omg.Messaging.MaxHopsPolicy)
fld public org.omg.Messaging.MaxHopsPolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.Messaging.MaxHopsPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract short max_hops()

CLSS public abstract org.omg.Messaging.MaxHopsPolicyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.Messaging.MaxHopsPolicyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.Messaging.MaxHopsPolicy _this()
meth public org.omg.Messaging.MaxHopsPolicy _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.Messaging.MaxHopsPolicyPOATie
cons public init(org.omg.Messaging.MaxHopsPolicyOperations)
cons public init(org.omg.Messaging.MaxHopsPolicyOperations,org.omg.PortableServer.POA)
meth public int policy_type()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.Messaging.MaxHopsPolicyOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public short max_hops()
meth public void _delegate(org.omg.Messaging.MaxHopsPolicyOperations)
meth public void destroy()
supr org.omg.Messaging.MaxHopsPolicyPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.Messaging.NO_REBIND
fld public final static short value = 1

CLSS public abstract interface org.omg.Messaging.NO_RECONNECT
fld public final static short value = 2

CLSS public abstract interface org.omg.Messaging.ORDER_ANY
fld public final static short value = 1

CLSS public abstract interface org.omg.Messaging.ORDER_DEADLINE
fld public final static short value = 8

CLSS public abstract interface org.omg.Messaging.ORDER_PRIORITY
fld public final static short value = 4

CLSS public abstract interface org.omg.Messaging.ORDER_TEMPORAL
fld public final static short value = 2

CLSS public abstract org.omg.Messaging.OrderingHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static short extract(org.omg.CORBA.Any)
meth public static short read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,short)
meth public static void write(org.omg.CORBA.portable.OutputStream,short)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.PolicyValue
cons public init()
cons public init(int,byte[])
fld public byte[] pvalue
fld public int ptype
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.Messaging.PolicyValueHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.PolicyValue extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.PolicyValue read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.PolicyValue)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.PolicyValue)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.Messaging.PolicyValueHolder
cons public init()
cons public init(org.omg.Messaging.PolicyValue)
fld public org.omg.Messaging.PolicyValue value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.Messaging.PolicyValueSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.PolicyValue[] extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.PolicyValue[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.PolicyValue[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.PolicyValue[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.PolicyValueSeqHolder
cons public init()
cons public init(org.omg.Messaging.PolicyValue[])
fld public org.omg.Messaging.PolicyValue[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.Messaging.PriorityHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static short extract(org.omg.CORBA.Any)
meth public static short read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,short)
meth public static void write(org.omg.CORBA.portable.OutputStream,short)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.PriorityRange
cons public init()
cons public init(short,short)
fld public short max
fld public short min
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.Messaging.PriorityRangeHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.PriorityRange extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.PriorityRange read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.PriorityRange)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.PriorityRange)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.Messaging.PriorityRangeHolder
cons public init()
cons public init(org.omg.Messaging.PriorityRange)
fld public org.omg.Messaging.PriorityRange value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.Messaging.QUEUE_ORDER_POLICY_TYPE
fld public final static int value = 35

CLSS public abstract interface org.omg.Messaging.QueueOrderPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.Messaging.QueueOrderPolicyOperations

CLSS public abstract org.omg.Messaging.QueueOrderPolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.QueueOrderPolicy extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.QueueOrderPolicy narrow(org.omg.CORBA.Object)
meth public static org.omg.Messaging.QueueOrderPolicy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.Messaging.QueueOrderPolicy unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.QueueOrderPolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.QueueOrderPolicy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.QueueOrderPolicyHolder
cons public init()
cons public init(org.omg.Messaging.QueueOrderPolicy)
fld public org.omg.Messaging.QueueOrderPolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.Messaging.QueueOrderPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract short allowed_orders()

CLSS public abstract org.omg.Messaging.QueueOrderPolicyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.Messaging.QueueOrderPolicyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.Messaging.QueueOrderPolicy _this()
meth public org.omg.Messaging.QueueOrderPolicy _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.Messaging.QueueOrderPolicyPOATie
cons public init(org.omg.Messaging.QueueOrderPolicyOperations)
cons public init(org.omg.Messaging.QueueOrderPolicyOperations,org.omg.PortableServer.POA)
meth public int policy_type()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.Messaging.QueueOrderPolicyOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public short allowed_orders()
meth public void _delegate(org.omg.Messaging.QueueOrderPolicyOperations)
meth public void destroy()
supr org.omg.Messaging.QueueOrderPolicyPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.Messaging.REBIND_POLICY_TYPE
fld public final static int value = 23

CLSS public abstract interface org.omg.Messaging.RELATIVE_REQ_TIMEOUT_POLICY_TYPE
fld public final static int value = 31

CLSS public abstract interface org.omg.Messaging.RELATIVE_RT_TIMEOUT_POLICY_TYPE
fld public final static int value = 32

CLSS public abstract interface org.omg.Messaging.REPLY_END_TIME_POLICY_TYPE
fld public final static int value = 30

CLSS public abstract interface org.omg.Messaging.REPLY_PRIORITY_POLICY_TYPE
fld public final static int value = 26

CLSS public abstract interface org.omg.Messaging.REPLY_START_TIME_POLICY_TYPE
fld public final static int value = 29

CLSS public abstract interface org.omg.Messaging.REQUEST_END_TIME_POLICY_TYPE
fld public final static int value = 28

CLSS public abstract interface org.omg.Messaging.REQUEST_PRIORITY_POLICY_TYPE
fld public final static int value = 25

CLSS public abstract interface org.omg.Messaging.REQUEST_START_TIME_POLICY_TYPE
fld public final static int value = 27

CLSS public abstract interface org.omg.Messaging.ROUTE_FORWARD
fld public final static short value = 1

CLSS public abstract interface org.omg.Messaging.ROUTE_NONE
fld public final static short value = 0

CLSS public abstract interface org.omg.Messaging.ROUTE_STORE_AND_FORWARD
fld public final static short value = 2

CLSS public abstract interface org.omg.Messaging.ROUTING_POLICY_TYPE
fld public final static int value = 33

CLSS public abstract org.omg.Messaging.RebindModeHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static short extract(org.omg.CORBA.Any)
meth public static short read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,short)
meth public static void write(org.omg.CORBA.portable.OutputStream,short)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.Messaging.RebindPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.Messaging.RebindPolicyOperations

CLSS public abstract org.omg.Messaging.RebindPolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.RebindPolicy extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.RebindPolicy narrow(org.omg.CORBA.Object)
meth public static org.omg.Messaging.RebindPolicy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.Messaging.RebindPolicy unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.RebindPolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.RebindPolicy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.RebindPolicyHolder
cons public init()
cons public init(org.omg.Messaging.RebindPolicy)
fld public org.omg.Messaging.RebindPolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.Messaging.RebindPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract short rebind_mode()

CLSS public abstract org.omg.Messaging.RebindPolicyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.Messaging.RebindPolicyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.Messaging.RebindPolicy _this()
meth public org.omg.Messaging.RebindPolicy _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.Messaging.RebindPolicyPOATie
cons public init(org.omg.Messaging.RebindPolicyOperations)
cons public init(org.omg.Messaging.RebindPolicyOperations,org.omg.PortableServer.POA)
meth public int policy_type()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.Messaging.RebindPolicyOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public short rebind_mode()
meth public void _delegate(org.omg.Messaging.RebindPolicyOperations)
meth public void destroy()
supr org.omg.Messaging.RebindPolicyPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.Messaging.RelativeRequestTimeoutPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.Messaging.RelativeRequestTimeoutPolicyOperations

CLSS public abstract org.omg.Messaging.RelativeRequestTimeoutPolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.RelativeRequestTimeoutPolicy extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.RelativeRequestTimeoutPolicy narrow(org.omg.CORBA.Object)
meth public static org.omg.Messaging.RelativeRequestTimeoutPolicy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.Messaging.RelativeRequestTimeoutPolicy unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.RelativeRequestTimeoutPolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.RelativeRequestTimeoutPolicy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.RelativeRequestTimeoutPolicyHolder
cons public init()
cons public init(org.omg.Messaging.RelativeRequestTimeoutPolicy)
fld public org.omg.Messaging.RelativeRequestTimeoutPolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.Messaging.RelativeRequestTimeoutPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract long relative_expiry()

CLSS public abstract org.omg.Messaging.RelativeRequestTimeoutPolicyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.Messaging.RelativeRequestTimeoutPolicyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.Messaging.RelativeRequestTimeoutPolicy _this()
meth public org.omg.Messaging.RelativeRequestTimeoutPolicy _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.Messaging.RelativeRequestTimeoutPolicyPOATie
cons public init(org.omg.Messaging.RelativeRequestTimeoutPolicyOperations)
cons public init(org.omg.Messaging.RelativeRequestTimeoutPolicyOperations,org.omg.PortableServer.POA)
meth public int policy_type()
meth public long relative_expiry()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.Messaging.RelativeRequestTimeoutPolicyOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.Messaging.RelativeRequestTimeoutPolicyOperations)
meth public void destroy()
supr org.omg.Messaging.RelativeRequestTimeoutPolicyPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.Messaging.RelativeRoundtripTimeoutPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.Messaging.RelativeRoundtripTimeoutPolicyOperations

CLSS public abstract org.omg.Messaging.RelativeRoundtripTimeoutPolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.RelativeRoundtripTimeoutPolicy extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.RelativeRoundtripTimeoutPolicy narrow(org.omg.CORBA.Object)
meth public static org.omg.Messaging.RelativeRoundtripTimeoutPolicy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.Messaging.RelativeRoundtripTimeoutPolicy unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.RelativeRoundtripTimeoutPolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.RelativeRoundtripTimeoutPolicy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.RelativeRoundtripTimeoutPolicyHolder
cons public init()
cons public init(org.omg.Messaging.RelativeRoundtripTimeoutPolicy)
fld public org.omg.Messaging.RelativeRoundtripTimeoutPolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.Messaging.RelativeRoundtripTimeoutPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract long relative_expiry()

CLSS public abstract org.omg.Messaging.RelativeRoundtripTimeoutPolicyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.Messaging.RelativeRoundtripTimeoutPolicyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.Messaging.RelativeRoundtripTimeoutPolicy _this()
meth public org.omg.Messaging.RelativeRoundtripTimeoutPolicy _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.Messaging.RelativeRoundtripTimeoutPolicyPOATie
cons public init(org.omg.Messaging.RelativeRoundtripTimeoutPolicyOperations)
cons public init(org.omg.Messaging.RelativeRoundtripTimeoutPolicyOperations,org.omg.PortableServer.POA)
meth public int policy_type()
meth public long relative_expiry()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.Messaging.RelativeRoundtripTimeoutPolicyOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.Messaging.RelativeRoundtripTimeoutPolicyOperations)
meth public void destroy()
supr org.omg.Messaging.RelativeRoundtripTimeoutPolicyPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.Messaging.ReplyEndTimePolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.Messaging.ReplyEndTimePolicyOperations

CLSS public abstract org.omg.Messaging.ReplyEndTimePolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.ReplyEndTimePolicy extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.ReplyEndTimePolicy narrow(org.omg.CORBA.Object)
meth public static org.omg.Messaging.ReplyEndTimePolicy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.Messaging.ReplyEndTimePolicy unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.ReplyEndTimePolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.ReplyEndTimePolicy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.ReplyEndTimePolicyHolder
cons public init()
cons public init(org.omg.Messaging.ReplyEndTimePolicy)
fld public org.omg.Messaging.ReplyEndTimePolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.Messaging.ReplyEndTimePolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract org.omg.TimeBase.UtcT end_time()

CLSS public abstract org.omg.Messaging.ReplyEndTimePolicyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.Messaging.ReplyEndTimePolicyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.Messaging.ReplyEndTimePolicy _this()
meth public org.omg.Messaging.ReplyEndTimePolicy _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.Messaging.ReplyEndTimePolicyPOATie
cons public init(org.omg.Messaging.ReplyEndTimePolicyOperations)
cons public init(org.omg.Messaging.ReplyEndTimePolicyOperations,org.omg.PortableServer.POA)
meth public int policy_type()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.Messaging.ReplyEndTimePolicyOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public org.omg.TimeBase.UtcT end_time()
meth public void _delegate(org.omg.Messaging.ReplyEndTimePolicyOperations)
meth public void destroy()
supr org.omg.Messaging.ReplyEndTimePolicyPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.Messaging.ReplyPriorityPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.Messaging.ReplyPriorityPolicyOperations

CLSS public abstract org.omg.Messaging.ReplyPriorityPolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.ReplyPriorityPolicy extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.ReplyPriorityPolicy narrow(org.omg.CORBA.Object)
meth public static org.omg.Messaging.ReplyPriorityPolicy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.Messaging.ReplyPriorityPolicy unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.ReplyPriorityPolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.ReplyPriorityPolicy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.ReplyPriorityPolicyHolder
cons public init()
cons public init(org.omg.Messaging.ReplyPriorityPolicy)
fld public org.omg.Messaging.ReplyPriorityPolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.Messaging.ReplyPriorityPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract org.omg.Messaging.PriorityRange priority_range()

CLSS public abstract org.omg.Messaging.ReplyPriorityPolicyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.Messaging.ReplyPriorityPolicyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.Messaging.ReplyPriorityPolicy _this()
meth public org.omg.Messaging.ReplyPriorityPolicy _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.Messaging.ReplyPriorityPolicyPOATie
cons public init(org.omg.Messaging.ReplyPriorityPolicyOperations)
cons public init(org.omg.Messaging.ReplyPriorityPolicyOperations,org.omg.PortableServer.POA)
meth public int policy_type()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.Messaging.PriorityRange priority_range()
meth public org.omg.Messaging.ReplyPriorityPolicyOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.Messaging.ReplyPriorityPolicyOperations)
meth public void destroy()
supr org.omg.Messaging.ReplyPriorityPolicyPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.Messaging.ReplyStartTimePolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.Messaging.ReplyStartTimePolicyOperations

CLSS public abstract org.omg.Messaging.ReplyStartTimePolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.ReplyStartTimePolicy extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.ReplyStartTimePolicy narrow(org.omg.CORBA.Object)
meth public static org.omg.Messaging.ReplyStartTimePolicy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.Messaging.ReplyStartTimePolicy unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.ReplyStartTimePolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.ReplyStartTimePolicy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.ReplyStartTimePolicyHolder
cons public init()
cons public init(org.omg.Messaging.ReplyStartTimePolicy)
fld public org.omg.Messaging.ReplyStartTimePolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.Messaging.ReplyStartTimePolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract org.omg.TimeBase.UtcT start_time()

CLSS public abstract org.omg.Messaging.ReplyStartTimePolicyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.Messaging.ReplyStartTimePolicyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.Messaging.ReplyStartTimePolicy _this()
meth public org.omg.Messaging.ReplyStartTimePolicy _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.Messaging.ReplyStartTimePolicyPOATie
cons public init(org.omg.Messaging.ReplyStartTimePolicyOperations)
cons public init(org.omg.Messaging.ReplyStartTimePolicyOperations,org.omg.PortableServer.POA)
meth public int policy_type()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.Messaging.ReplyStartTimePolicyOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public org.omg.TimeBase.UtcT start_time()
meth public void _delegate(org.omg.Messaging.ReplyStartTimePolicyOperations)
meth public void destroy()
supr org.omg.Messaging.ReplyStartTimePolicyPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.Messaging.RequestEndTimePolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.Messaging.RequestEndTimePolicyOperations

CLSS public abstract org.omg.Messaging.RequestEndTimePolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.RequestEndTimePolicy extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.RequestEndTimePolicy narrow(org.omg.CORBA.Object)
meth public static org.omg.Messaging.RequestEndTimePolicy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.Messaging.RequestEndTimePolicy unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.RequestEndTimePolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.RequestEndTimePolicy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.RequestEndTimePolicyHolder
cons public init()
cons public init(org.omg.Messaging.RequestEndTimePolicy)
fld public org.omg.Messaging.RequestEndTimePolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.Messaging.RequestEndTimePolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract org.omg.TimeBase.UtcT end_time()

CLSS public abstract org.omg.Messaging.RequestEndTimePolicyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.Messaging.RequestEndTimePolicyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.Messaging.RequestEndTimePolicy _this()
meth public org.omg.Messaging.RequestEndTimePolicy _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.Messaging.RequestEndTimePolicyPOATie
cons public init(org.omg.Messaging.RequestEndTimePolicyOperations)
cons public init(org.omg.Messaging.RequestEndTimePolicyOperations,org.omg.PortableServer.POA)
meth public int policy_type()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.Messaging.RequestEndTimePolicyOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public org.omg.TimeBase.UtcT end_time()
meth public void _delegate(org.omg.Messaging.RequestEndTimePolicyOperations)
meth public void destroy()
supr org.omg.Messaging.RequestEndTimePolicyPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.Messaging.RequestPriorityPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.Messaging.RequestPriorityPolicyOperations

CLSS public abstract org.omg.Messaging.RequestPriorityPolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.RequestPriorityPolicy extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.RequestPriorityPolicy narrow(org.omg.CORBA.Object)
meth public static org.omg.Messaging.RequestPriorityPolicy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.Messaging.RequestPriorityPolicy unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.RequestPriorityPolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.RequestPriorityPolicy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.RequestPriorityPolicyHolder
cons public init()
cons public init(org.omg.Messaging.RequestPriorityPolicy)
fld public org.omg.Messaging.RequestPriorityPolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.Messaging.RequestPriorityPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract org.omg.Messaging.PriorityRange priority_range()

CLSS public abstract org.omg.Messaging.RequestPriorityPolicyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.Messaging.RequestPriorityPolicyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.Messaging.RequestPriorityPolicy _this()
meth public org.omg.Messaging.RequestPriorityPolicy _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.Messaging.RequestPriorityPolicyPOATie
cons public init(org.omg.Messaging.RequestPriorityPolicyOperations)
cons public init(org.omg.Messaging.RequestPriorityPolicyOperations,org.omg.PortableServer.POA)
meth public int policy_type()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.Messaging.PriorityRange priority_range()
meth public org.omg.Messaging.RequestPriorityPolicyOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.Messaging.RequestPriorityPolicyOperations)
meth public void destroy()
supr org.omg.Messaging.RequestPriorityPolicyPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.Messaging.RequestStartTimePolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.Messaging.RequestStartTimePolicyOperations

CLSS public abstract org.omg.Messaging.RequestStartTimePolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.RequestStartTimePolicy extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.RequestStartTimePolicy narrow(org.omg.CORBA.Object)
meth public static org.omg.Messaging.RequestStartTimePolicy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.Messaging.RequestStartTimePolicy unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.RequestStartTimePolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.RequestStartTimePolicy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.RequestStartTimePolicyHolder
cons public init()
cons public init(org.omg.Messaging.RequestStartTimePolicy)
fld public org.omg.Messaging.RequestStartTimePolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.Messaging.RequestStartTimePolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract org.omg.TimeBase.UtcT start_time()

CLSS public abstract org.omg.Messaging.RequestStartTimePolicyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.Messaging.RequestStartTimePolicyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.Messaging.RequestStartTimePolicy _this()
meth public org.omg.Messaging.RequestStartTimePolicy _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.Messaging.RequestStartTimePolicyPOATie
cons public init(org.omg.Messaging.RequestStartTimePolicyOperations)
cons public init(org.omg.Messaging.RequestStartTimePolicyOperations,org.omg.PortableServer.POA)
meth public int policy_type()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.Messaging.RequestStartTimePolicyOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public org.omg.TimeBase.UtcT start_time()
meth public void _delegate(org.omg.Messaging.RequestStartTimePolicyOperations)
meth public void destroy()
supr org.omg.Messaging.RequestStartTimePolicyPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.Messaging.RoutingPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.Messaging.RoutingPolicyOperations

CLSS public abstract org.omg.Messaging.RoutingPolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.RoutingPolicy extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.RoutingPolicy narrow(org.omg.CORBA.Object)
meth public static org.omg.Messaging.RoutingPolicy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.Messaging.RoutingPolicy unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.RoutingPolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.RoutingPolicy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.RoutingPolicyHolder
cons public init()
cons public init(org.omg.Messaging.RoutingPolicy)
fld public org.omg.Messaging.RoutingPolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.Messaging.RoutingPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract org.omg.Messaging.RoutingTypeRange routing_range()

CLSS public abstract org.omg.Messaging.RoutingPolicyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.Messaging.RoutingPolicyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.Messaging.RoutingPolicy _this()
meth public org.omg.Messaging.RoutingPolicy _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.Messaging.RoutingPolicyPOATie
cons public init(org.omg.Messaging.RoutingPolicyOperations)
cons public init(org.omg.Messaging.RoutingPolicyOperations,org.omg.PortableServer.POA)
meth public int policy_type()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.Messaging.RoutingPolicyOperations _delegate()
meth public org.omg.Messaging.RoutingTypeRange routing_range()
meth public org.omg.PortableServer.POA _default_POA()
meth public void _delegate(org.omg.Messaging.RoutingPolicyOperations)
meth public void destroy()
supr org.omg.Messaging.RoutingPolicyPOA
hfds _impl,_poa

CLSS public abstract org.omg.Messaging.RoutingTypeHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static short extract(org.omg.CORBA.Any)
meth public static short read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,short)
meth public static void write(org.omg.CORBA.portable.OutputStream,short)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.RoutingTypeRange
cons public init()
cons public init(short,short)
fld public short max
fld public short min
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.Messaging.RoutingTypeRangeHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.RoutingTypeRange extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.RoutingTypeRange read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.RoutingTypeRange)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.RoutingTypeRange)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.Messaging.RoutingTypeRangeHolder
cons public init()
cons public init(org.omg.Messaging.RoutingTypeRange)
fld public org.omg.Messaging.RoutingTypeRange value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.Messaging.SYNC_NONE
fld public final static short value = 0

CLSS public abstract interface org.omg.Messaging.SYNC_SCOPE_POLICY_TYPE
fld public final static int value = 24

CLSS public abstract interface org.omg.Messaging.SYNC_WITH_SERVER
fld public final static short value = 2

CLSS public abstract interface org.omg.Messaging.SYNC_WITH_TARGET
fld public final static short value = 3

CLSS public abstract interface org.omg.Messaging.SYNC_WITH_TRANSPORT
fld public final static short value = 1

CLSS public abstract org.omg.Messaging.SyncScopeHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static short extract(org.omg.CORBA.Any)
meth public static short read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,short)
meth public static void write(org.omg.CORBA.portable.OutputStream,short)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.Messaging.SyncScopePolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.Messaging.SyncScopePolicyOperations

CLSS public abstract org.omg.Messaging.SyncScopePolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.Messaging.SyncScopePolicy extract(org.omg.CORBA.Any)
meth public static org.omg.Messaging.SyncScopePolicy narrow(org.omg.CORBA.Object)
meth public static org.omg.Messaging.SyncScopePolicy read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.Messaging.SyncScopePolicy unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.Messaging.SyncScopePolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.Messaging.SyncScopePolicy)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.Messaging.SyncScopePolicyHolder
cons public init()
cons public init(org.omg.Messaging.SyncScopePolicy)
fld public org.omg.Messaging.SyncScopePolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.Messaging.SyncScopePolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract short synchronization()

CLSS public abstract org.omg.Messaging.SyncScopePolicyPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.Messaging.SyncScopePolicyOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.Messaging.SyncScopePolicy _this()
meth public org.omg.Messaging.SyncScopePolicy _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.Messaging.SyncScopePolicyPOATie
cons public init(org.omg.Messaging.SyncScopePolicyOperations)
cons public init(org.omg.Messaging.SyncScopePolicyOperations,org.omg.PortableServer.POA)
meth public int policy_type()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.Messaging.SyncScopePolicyOperations _delegate()
meth public org.omg.PortableServer.POA _default_POA()
meth public short synchronization()
meth public void _delegate(org.omg.Messaging.SyncScopePolicyOperations)
meth public void destroy()
supr org.omg.Messaging.SyncScopePolicyPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.Messaging.TRANSPARENT
fld public final static short value = 0

CLSS public org.omg.Messaging._MaxHopsPolicyStub
cons public init()
intf org.omg.Messaging.MaxHopsPolicy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Policy copy()
meth public short max_hops()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.Messaging._QueueOrderPolicyStub
cons public init()
intf org.omg.Messaging.QueueOrderPolicy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Policy copy()
meth public short allowed_orders()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.Messaging._RebindPolicyStub
cons public init()
intf org.omg.Messaging.RebindPolicy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Policy copy()
meth public short rebind_mode()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.Messaging._RelativeRequestTimeoutPolicyStub
cons public init()
intf org.omg.Messaging.RelativeRequestTimeoutPolicy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public long relative_expiry()
meth public org.omg.CORBA.Policy copy()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.Messaging._RelativeRoundtripTimeoutPolicyStub
cons public init()
intf org.omg.Messaging.RelativeRoundtripTimeoutPolicy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public long relative_expiry()
meth public org.omg.CORBA.Policy copy()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.Messaging._ReplyEndTimePolicyStub
cons public init()
intf org.omg.Messaging.ReplyEndTimePolicy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.TimeBase.UtcT end_time()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.Messaging._ReplyPriorityPolicyStub
cons public init()
intf org.omg.Messaging.ReplyPriorityPolicy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.Messaging.PriorityRange priority_range()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.Messaging._ReplyStartTimePolicyStub
cons public init()
intf org.omg.Messaging.ReplyStartTimePolicy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.TimeBase.UtcT start_time()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.Messaging._RequestEndTimePolicyStub
cons public init()
intf org.omg.Messaging.RequestEndTimePolicy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.TimeBase.UtcT end_time()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.Messaging._RequestPriorityPolicyStub
cons public init()
intf org.omg.Messaging.RequestPriorityPolicy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.Messaging.PriorityRange priority_range()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.Messaging._RequestStartTimePolicyStub
cons public init()
intf org.omg.Messaging.RequestStartTimePolicy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.TimeBase.UtcT start_time()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.Messaging._RoutingPolicyStub
cons public init()
intf org.omg.Messaging.RoutingPolicy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Policy copy()
meth public org.omg.Messaging.RoutingTypeRange routing_range()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.Messaging._SyncScopePolicyStub
cons public init()
intf org.omg.Messaging.SyncScopePolicy
meth public int policy_type()
meth public java.lang.String[] _ids()
meth public org.omg.CORBA.Policy copy()
meth public short synchronization()
meth public void destroy()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public abstract interface org.omg.PortableInterceptor.ACTIVE
fld public final static short value = 1

CLSS public abstract org.omg.PortableInterceptor.AdapterManagerIdHelper
cons public init()
meth public static int extract(org.omg.CORBA.Any)
meth public static int read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,int)
meth public static void write(org.omg.CORBA.portable.OutputStream,int)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract org.omg.PortableInterceptor.AdapterNameHelper
cons public init()
meth public static java.lang.String id()
meth public static java.lang.String[] extract(org.omg.CORBA.Any)
meth public static java.lang.String[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String[])
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract org.omg.PortableInterceptor.AdapterStateHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static short extract(org.omg.CORBA.Any)
meth public static short read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,short)
meth public static void write(org.omg.CORBA.portable.OutputStream,short)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.PortableInterceptor.ClientRequestInfo
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableInterceptor.ClientRequestInfoOperations
intf org.omg.PortableInterceptor.RequestInfo

CLSS public abstract org.omg.PortableInterceptor.ClientRequestInfoHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.ClientRequestInfo extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.ClientRequestInfo narrow(org.omg.CORBA.Object)
meth public static org.omg.PortableInterceptor.ClientRequestInfo read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.PortableInterceptor.ClientRequestInfo unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.ClientRequestInfo)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.ClientRequestInfo)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableInterceptor.ClientRequestInfoHolder
cons public init()
cons public init(org.omg.PortableInterceptor.ClientRequestInfo)
fld public org.omg.PortableInterceptor.ClientRequestInfo value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.ClientRequestInfoOperations
intf org.omg.PortableInterceptor.RequestInfoOperations
meth public abstract java.lang.String received_exception_id()
meth public abstract org.omg.CORBA.Any received_exception()
meth public abstract org.omg.CORBA.Object effective_target()
meth public abstract org.omg.CORBA.Object target()
meth public abstract org.omg.CORBA.Policy get_request_policy(int)
meth public abstract org.omg.IOP.TaggedComponent get_effective_component(int)
meth public abstract org.omg.IOP.TaggedComponent[] get_effective_components(int)
meth public abstract org.omg.IOP.TaggedProfile effective_profile()
meth public abstract void add_request_service_context(org.omg.IOP.ServiceContext,boolean)

CLSS public abstract interface org.omg.PortableInterceptor.ClientRequestInterceptor
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableInterceptor.ClientRequestInterceptorOperations
intf org.omg.PortableInterceptor.Interceptor

CLSS public abstract org.omg.PortableInterceptor.ClientRequestInterceptorHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.ClientRequestInterceptor extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.ClientRequestInterceptor narrow(org.omg.CORBA.Object)
meth public static org.omg.PortableInterceptor.ClientRequestInterceptor read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.PortableInterceptor.ClientRequestInterceptor unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.ClientRequestInterceptor)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.ClientRequestInterceptor)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableInterceptor.ClientRequestInterceptorHolder
cons public init()
cons public init(org.omg.PortableInterceptor.ClientRequestInterceptor)
fld public org.omg.PortableInterceptor.ClientRequestInterceptor value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.ClientRequestInterceptorOperations
intf org.omg.PortableInterceptor.InterceptorOperations
meth public abstract void receive_exception(org.omg.PortableInterceptor.ClientRequestInfo) throws org.omg.PortableInterceptor.ForwardRequest
meth public abstract void receive_other(org.omg.PortableInterceptor.ClientRequestInfo) throws org.omg.PortableInterceptor.ForwardRequest
meth public abstract void receive_reply(org.omg.PortableInterceptor.ClientRequestInfo)
meth public abstract void send_poll(org.omg.PortableInterceptor.ClientRequestInfo)
meth public abstract void send_request(org.omg.PortableInterceptor.ClientRequestInfo) throws org.omg.PortableInterceptor.ForwardRequest

CLSS public abstract interface org.omg.PortableInterceptor.Current
intf org.omg.CORBA.Current
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableInterceptor.CurrentOperations

CLSS public abstract org.omg.PortableInterceptor.CurrentHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.Current extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.Current narrow(org.omg.CORBA.Object)
meth public static org.omg.PortableInterceptor.Current read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.PortableInterceptor.Current unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.Current)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.Current)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableInterceptor.CurrentHolder
cons public init()
cons public init(org.omg.PortableInterceptor.Current)
fld public org.omg.PortableInterceptor.Current value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.CurrentOperations
intf org.omg.CORBA.CurrentOperations
meth public abstract org.omg.CORBA.Any get_slot(int) throws org.omg.PortableInterceptor.InvalidSlot
meth public abstract void set_slot(int,org.omg.CORBA.Any) throws org.omg.PortableInterceptor.InvalidSlot

CLSS public abstract interface org.omg.PortableInterceptor.DISCARDING
fld public final static short value = 2

CLSS public final org.omg.PortableInterceptor.ForwardRequest
cons public init()
cons public init(java.lang.String,org.omg.CORBA.Object)
cons public init(org.omg.CORBA.Object)
fld public org.omg.CORBA.Object forward
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableInterceptor.ForwardRequestHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.ForwardRequest extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.ForwardRequest read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.ForwardRequest)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.ForwardRequest)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableInterceptor.ForwardRequestHolder
cons public init()
cons public init(org.omg.PortableInterceptor.ForwardRequest)
fld public org.omg.PortableInterceptor.ForwardRequest value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.HOLDING
fld public final static short value = 0

CLSS public abstract interface org.omg.PortableInterceptor.INACTIVE
fld public final static short value = 3

CLSS public abstract interface org.omg.PortableInterceptor.IORInfo
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableInterceptor.IORInfoOperations

CLSS public abstract org.omg.PortableInterceptor.IORInfoHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.IORInfo extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.IORInfo narrow(org.omg.CORBA.Object)
meth public static org.omg.PortableInterceptor.IORInfo read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.PortableInterceptor.IORInfo unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.IORInfo)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.IORInfo)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableInterceptor.IORInfoHolder
cons public init()
cons public init(org.omg.PortableInterceptor.IORInfo)
fld public org.omg.PortableInterceptor.IORInfo value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.IORInfoOperations
meth public abstract int manager_id()
meth public abstract org.omg.CORBA.Policy get_effective_policy(int)
meth public abstract org.omg.PortableInterceptor.ObjectReferenceFactory current_factory()
meth public abstract org.omg.PortableInterceptor.ObjectReferenceTemplate adapter_template()
meth public abstract short state()
meth public abstract void add_ior_component(org.omg.IOP.TaggedComponent)
meth public abstract void add_ior_component_to_profile(org.omg.IOP.TaggedComponent,int)
meth public abstract void current_factory(org.omg.PortableInterceptor.ObjectReferenceFactory)

CLSS public abstract interface org.omg.PortableInterceptor.IORInterceptor
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableInterceptor.IORInterceptorOperations
intf org.omg.PortableInterceptor.Interceptor

CLSS public abstract org.omg.PortableInterceptor.IORInterceptorHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.IORInterceptor extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.IORInterceptor narrow(org.omg.CORBA.Object)
meth public static org.omg.PortableInterceptor.IORInterceptor read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.PortableInterceptor.IORInterceptor unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.IORInterceptor)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.IORInterceptor)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableInterceptor.IORInterceptorHolder
cons public init()
cons public init(org.omg.PortableInterceptor.IORInterceptor)
fld public org.omg.PortableInterceptor.IORInterceptor value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.IORInterceptorOperations
intf org.omg.PortableInterceptor.InterceptorOperations
meth public abstract void establish_components(org.omg.PortableInterceptor.IORInfo)

CLSS public abstract interface org.omg.PortableInterceptor.IORInterceptor_3_0
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableInterceptor.IORInterceptor
intf org.omg.PortableInterceptor.IORInterceptor_3_0Operations

CLSS public abstract org.omg.PortableInterceptor.IORInterceptor_3_0Helper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.IORInterceptor_3_0 extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.IORInterceptor_3_0 narrow(org.omg.CORBA.Object)
meth public static org.omg.PortableInterceptor.IORInterceptor_3_0 read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.PortableInterceptor.IORInterceptor_3_0 unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.IORInterceptor_3_0)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.IORInterceptor_3_0)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableInterceptor.IORInterceptor_3_0Holder
cons public init()
cons public init(org.omg.PortableInterceptor.IORInterceptor_3_0)
fld public org.omg.PortableInterceptor.IORInterceptor_3_0 value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.IORInterceptor_3_0Operations
intf org.omg.PortableInterceptor.IORInterceptorOperations
meth public abstract void adapter_manager_state_changed(int,short)
meth public abstract void adapter_state_changed(org.omg.PortableInterceptor.ObjectReferenceTemplate[],short)
meth public abstract void components_established(org.omg.PortableInterceptor.IORInfo)

CLSS public abstract interface org.omg.PortableInterceptor.Interceptor
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableInterceptor.InterceptorOperations

CLSS public abstract org.omg.PortableInterceptor.InterceptorHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.Interceptor extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.Interceptor narrow(org.omg.CORBA.Object)
meth public static org.omg.PortableInterceptor.Interceptor read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.PortableInterceptor.Interceptor unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.Interceptor)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.Interceptor)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableInterceptor.InterceptorHolder
cons public init()
cons public init(org.omg.PortableInterceptor.Interceptor)
fld public org.omg.PortableInterceptor.Interceptor value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.InterceptorOperations
meth public abstract java.lang.String name()
meth public abstract void destroy()

CLSS public final org.omg.PortableInterceptor.InvalidSlot
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableInterceptor.InvalidSlotHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.InvalidSlot extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.InvalidSlot read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.InvalidSlot)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.InvalidSlot)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableInterceptor.InvalidSlotHolder
cons public init()
cons public init(org.omg.PortableInterceptor.InvalidSlot)
fld public org.omg.PortableInterceptor.InvalidSlot value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.LOCATION_FORWARD
fld public final static short value = 3

CLSS public abstract interface org.omg.PortableInterceptor.NON_EXISTENT
fld public final static short value = 4

CLSS public abstract org.omg.PortableInterceptor.ORBIdHelper
cons public init()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.PortableInterceptor.ORBInitInfo
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableInterceptor.ORBInitInfoOperations

CLSS public abstract org.omg.PortableInterceptor.ORBInitInfoHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.ORBInitInfo extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.ORBInitInfo narrow(org.omg.CORBA.Object)
meth public static org.omg.PortableInterceptor.ORBInitInfo read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.PortableInterceptor.ORBInitInfo unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.ORBInitInfo)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.ORBInitInfo)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableInterceptor.ORBInitInfoHolder
cons public init()
cons public init(org.omg.PortableInterceptor.ORBInitInfo)
fld public org.omg.PortableInterceptor.ORBInitInfo value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.ORBInitInfoOperations
meth public abstract int allocate_slot_id()
meth public abstract java.lang.String orb_id()
meth public abstract java.lang.String[] arguments()
meth public abstract org.omg.CORBA.Object resolve_initial_references(java.lang.String) throws org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName
meth public abstract org.omg.IOP.CodecFactory codec_factory()
meth public abstract void add_client_request_interceptor(org.omg.PortableInterceptor.ClientRequestInterceptor) throws org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName
meth public abstract void add_ior_interceptor(org.omg.PortableInterceptor.IORInterceptor) throws org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName
meth public abstract void add_server_request_interceptor(org.omg.PortableInterceptor.ServerRequestInterceptor) throws org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName
meth public abstract void register_initial_reference(java.lang.String,org.omg.CORBA.Object) throws org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName
meth public abstract void register_policy_factory(int,org.omg.PortableInterceptor.PolicyFactory)

CLSS public final org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
fld public java.lang.String name
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateNameHolder
cons public init()
cons public init(org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName)
fld public org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidNameHolder
cons public init()
cons public init(org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName)
fld public org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.PortableInterceptor.ORBInitInfoPackage.ObjectIdHelper
cons public init()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.PortableInterceptor.ORBInitializer
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableInterceptor.ORBInitializerOperations

CLSS public abstract org.omg.PortableInterceptor.ORBInitializerHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.ORBInitializer extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.ORBInitializer narrow(org.omg.CORBA.Object)
meth public static org.omg.PortableInterceptor.ORBInitializer read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.PortableInterceptor.ORBInitializer unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.ORBInitializer)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.ORBInitializer)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableInterceptor.ORBInitializerHolder
cons public init()
cons public init(org.omg.PortableInterceptor.ORBInitializer)
fld public org.omg.PortableInterceptor.ORBInitializer value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.ORBInitializerOperations
meth public abstract void post_init(org.omg.PortableInterceptor.ORBInitInfo)
meth public abstract void pre_init(org.omg.PortableInterceptor.ORBInitInfo)

CLSS public abstract org.omg.PortableInterceptor.ObjectIdHelper
cons public init()
meth public static byte[] extract(org.omg.CORBA.Any)
meth public static byte[] read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,byte[])
meth public static void write(org.omg.CORBA.portable.OutputStream,byte[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.PortableInterceptor.ObjectReferenceFactory
intf org.omg.CORBA.portable.ValueBase
meth public abstract org.omg.CORBA.Object make_object(java.lang.String,byte[])

CLSS public abstract org.omg.PortableInterceptor.ObjectReferenceFactoryHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.ObjectReferenceFactory extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.ObjectReferenceFactory read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.ObjectReferenceFactory)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.ObjectReferenceFactory)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableInterceptor.ObjectReferenceFactoryHolder
cons public init()
cons public init(org.omg.PortableInterceptor.ObjectReferenceFactory)
fld public org.omg.PortableInterceptor.ObjectReferenceFactory value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.ObjectReferenceTemplate
intf org.omg.PortableInterceptor.ObjectReferenceFactory
meth public abstract java.lang.String orb_id()
meth public abstract java.lang.String server_id()
meth public abstract java.lang.String[] adapter_name()

CLSS public abstract org.omg.PortableInterceptor.ObjectReferenceTemplateHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.ObjectReferenceTemplate extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.ObjectReferenceTemplate read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.ObjectReferenceTemplate)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.ObjectReferenceTemplate)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableInterceptor.ObjectReferenceTemplateHolder
cons public init()
cons public init(org.omg.PortableInterceptor.ObjectReferenceTemplate)
fld public org.omg.PortableInterceptor.ObjectReferenceTemplate value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.ObjectReferenceTemplate[] extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.ObjectReferenceTemplate[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.ObjectReferenceTemplate[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.ObjectReferenceTemplate[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableInterceptor.ObjectReferenceTemplateSeqHolder
cons public init()
cons public init(org.omg.PortableInterceptor.ObjectReferenceTemplate[])
fld public org.omg.PortableInterceptor.ObjectReferenceTemplate[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.PolicyFactory
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableInterceptor.PolicyFactoryOperations

CLSS public abstract org.omg.PortableInterceptor.PolicyFactoryHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.PolicyFactory extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.PolicyFactory narrow(org.omg.CORBA.Object)
meth public static org.omg.PortableInterceptor.PolicyFactory read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.PortableInterceptor.PolicyFactory unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.PolicyFactory)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.PolicyFactory)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableInterceptor.PolicyFactoryHolder
cons public init()
cons public init(org.omg.PortableInterceptor.PolicyFactory)
fld public org.omg.PortableInterceptor.PolicyFactory value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.PolicyFactoryOperations
meth public abstract org.omg.CORBA.Policy create_policy(int,org.omg.CORBA.Any) throws org.omg.CORBA.PolicyError

CLSS public abstract org.omg.PortableInterceptor.ReplyStatusHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static short extract(org.omg.CORBA.Any)
meth public static short read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,short)
meth public static void write(org.omg.CORBA.portable.OutputStream,short)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.PortableInterceptor.RequestInfo
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableInterceptor.RequestInfoOperations

CLSS public abstract org.omg.PortableInterceptor.RequestInfoHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.RequestInfo extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.RequestInfo narrow(org.omg.CORBA.Object)
meth public static org.omg.PortableInterceptor.RequestInfo read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.PortableInterceptor.RequestInfo unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.RequestInfo)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.RequestInfo)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableInterceptor.RequestInfoHolder
cons public init()
cons public init(org.omg.PortableInterceptor.RequestInfo)
fld public org.omg.PortableInterceptor.RequestInfo value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.RequestInfoOperations
meth public abstract boolean response_expected()
meth public abstract int request_id()
meth public abstract java.lang.String operation()
meth public abstract java.lang.String[] contexts()
meth public abstract java.lang.String[] operation_context()
meth public abstract org.omg.CORBA.Any get_slot(int) throws org.omg.PortableInterceptor.InvalidSlot
meth public abstract org.omg.CORBA.Any result()
meth public abstract org.omg.CORBA.Object forward_reference()
meth public abstract org.omg.CORBA.TypeCode[] exceptions()
meth public abstract org.omg.Dynamic.Parameter[] arguments()
meth public abstract org.omg.IOP.ServiceContext get_reply_service_context(int)
meth public abstract org.omg.IOP.ServiceContext get_request_service_context(int)
meth public abstract short reply_status()
meth public abstract short sync_scope()

CLSS public abstract interface org.omg.PortableInterceptor.SUCCESSFUL
fld public final static short value = 0

CLSS public abstract interface org.omg.PortableInterceptor.SYSTEM_EXCEPTION
fld public final static short value = 1

CLSS public abstract org.omg.PortableInterceptor.ServerIdHelper
cons public init()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.PortableInterceptor.ServerRequestInfo
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableInterceptor.RequestInfo
intf org.omg.PortableInterceptor.ServerRequestInfoOperations

CLSS public abstract org.omg.PortableInterceptor.ServerRequestInfoHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.ServerRequestInfo extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.ServerRequestInfo narrow(org.omg.CORBA.Object)
meth public static org.omg.PortableInterceptor.ServerRequestInfo read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.PortableInterceptor.ServerRequestInfo unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.ServerRequestInfo)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.ServerRequestInfo)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableInterceptor.ServerRequestInfoHolder
cons public init()
cons public init(org.omg.PortableInterceptor.ServerRequestInfo)
fld public org.omg.PortableInterceptor.ServerRequestInfo value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.ServerRequestInfoOperations
intf org.omg.PortableInterceptor.RequestInfoOperations
meth public abstract boolean target_is_a(java.lang.String)
meth public abstract byte[] adapter_id()
meth public abstract byte[] object_id()
meth public abstract java.lang.String orb_id()
meth public abstract java.lang.String server_id()
meth public abstract java.lang.String target_most_derived_interface()
meth public abstract java.lang.String[] adapter_name()
meth public abstract org.omg.CORBA.Any sending_exception()
meth public abstract org.omg.CORBA.Policy get_server_policy(int)
meth public abstract void add_reply_service_context(org.omg.IOP.ServiceContext,boolean)
meth public abstract void set_slot(int,org.omg.CORBA.Any) throws org.omg.PortableInterceptor.InvalidSlot

CLSS public abstract interface org.omg.PortableInterceptor.ServerRequestInterceptor
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableInterceptor.Interceptor
intf org.omg.PortableInterceptor.ServerRequestInterceptorOperations

CLSS public abstract org.omg.PortableInterceptor.ServerRequestInterceptorHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableInterceptor.ServerRequestInterceptor extract(org.omg.CORBA.Any)
meth public static org.omg.PortableInterceptor.ServerRequestInterceptor narrow(org.omg.CORBA.Object)
meth public static org.omg.PortableInterceptor.ServerRequestInterceptor read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.PortableInterceptor.ServerRequestInterceptor unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableInterceptor.ServerRequestInterceptor)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableInterceptor.ServerRequestInterceptor)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableInterceptor.ServerRequestInterceptorHolder
cons public init()
cons public init(org.omg.PortableInterceptor.ServerRequestInterceptor)
fld public org.omg.PortableInterceptor.ServerRequestInterceptor value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableInterceptor.ServerRequestInterceptorOperations
intf org.omg.PortableInterceptor.InterceptorOperations
meth public abstract void receive_request(org.omg.PortableInterceptor.ServerRequestInfo) throws org.omg.PortableInterceptor.ForwardRequest
meth public abstract void receive_request_service_contexts(org.omg.PortableInterceptor.ServerRequestInfo) throws org.omg.PortableInterceptor.ForwardRequest
meth public abstract void send_exception(org.omg.PortableInterceptor.ServerRequestInfo) throws org.omg.PortableInterceptor.ForwardRequest
meth public abstract void send_other(org.omg.PortableInterceptor.ServerRequestInfo) throws org.omg.PortableInterceptor.ForwardRequest
meth public abstract void send_reply(org.omg.PortableInterceptor.ServerRequestInfo)

CLSS public abstract org.omg.PortableInterceptor.SlotIdHelper
cons public init()
meth public static int extract(org.omg.CORBA.Any)
meth public static int read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,int)
meth public static void write(org.omg.CORBA.portable.OutputStream,int)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract interface org.omg.PortableInterceptor.TRANSPORT_RETRY
fld public final static short value = 4

CLSS public abstract interface org.omg.PortableInterceptor.USER_EXCEPTION
fld public final static short value = 2

CLSS public abstract interface org.omg.PortableServer.AdapterActivator
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableServer.AdapterActivatorOperations

CLSS public abstract interface org.omg.PortableServer.AdapterActivatorOperations
meth public abstract boolean unknown_adapter(org.omg.PortableServer.POA,java.lang.String)

CLSS public abstract interface org.omg.PortableServer.Current
intf org.omg.CORBA.Current
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableServer.CurrentOperations

CLSS public abstract interface org.omg.PortableServer.CurrentOperations
intf org.omg.CORBA.CurrentOperations
meth public abstract byte[] get_object_id() throws org.omg.PortableServer.CurrentPackage.NoContext
meth public abstract org.omg.PortableServer.POA get_POA() throws org.omg.PortableServer.CurrentPackage.NoContext

CLSS public final org.omg.PortableServer.CurrentPackage.NoContext
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableServer.CurrentPackage.NoContextHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.CurrentPackage.NoContext extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.CurrentPackage.NoContext read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.CurrentPackage.NoContext)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.CurrentPackage.NoContext)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableServer.CurrentPackage.NoContextHolder
cons public init()
cons public init(org.omg.PortableServer.CurrentPackage.NoContext)
fld public org.omg.PortableServer.CurrentPackage.NoContext value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.PortableServer.DynamicImplementation
cons public init()
meth public abstract void invoke(org.omg.CORBA.ServerRequest)
supr org.omg.PortableServer.Servant

CLSS public final org.omg.PortableServer.ForwardRequest
cons public init()
cons public init(java.lang.String,org.omg.CORBA.Object)
cons public init(org.omg.CORBA.Object)
fld public org.omg.CORBA.Object forward_reference
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableServer.ForwardRequestHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.ForwardRequest extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.ForwardRequest read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.ForwardRequest)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.ForwardRequest)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableServer.ForwardRequestHolder
cons public init()
cons public init(org.omg.PortableServer.ForwardRequest)
fld public org.omg.PortableServer.ForwardRequest value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableServer.ID_ASSIGNMENT_POLICY_ID
fld public final static int value = 19

CLSS public abstract interface org.omg.PortableServer.ID_UNIQUENESS_POLICY_ID
fld public final static int value = 18

CLSS public abstract interface org.omg.PortableServer.IMPLICIT_ACTIVATION_POLICY_ID
fld public final static int value = 20

CLSS public abstract interface org.omg.PortableServer.IdAssignmentPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableServer.IdAssignmentPolicyOperations

CLSS public abstract interface org.omg.PortableServer.IdAssignmentPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract org.omg.PortableServer.IdAssignmentPolicyValue value()

CLSS public org.omg.PortableServer.IdAssignmentPolicyValue
cons protected init(int)
fld public final static int _SYSTEM_ID = 1
fld public final static int _USER_ID = 0
fld public final static org.omg.PortableServer.IdAssignmentPolicyValue SYSTEM_ID
fld public final static org.omg.PortableServer.IdAssignmentPolicyValue USER_ID
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.PortableServer.IdAssignmentPolicyValue from_int(int)
supr java.lang.Object
hfds __array,__size,__value

CLSS public abstract org.omg.PortableServer.IdAssignmentPolicyValueHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.IdAssignmentPolicyValue extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.IdAssignmentPolicyValue read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.IdAssignmentPolicyValue)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.IdAssignmentPolicyValue)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableServer.IdAssignmentPolicyValueHolder
cons public init()
cons public init(org.omg.PortableServer.IdAssignmentPolicyValue)
fld public org.omg.PortableServer.IdAssignmentPolicyValue value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableServer.IdUniquenessPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableServer.IdUniquenessPolicyOperations

CLSS public abstract interface org.omg.PortableServer.IdUniquenessPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract org.omg.PortableServer.IdUniquenessPolicyValue value()

CLSS public org.omg.PortableServer.IdUniquenessPolicyValue
cons protected init(int)
fld public final static int _MULTIPLE_ID = 1
fld public final static int _UNIQUE_ID = 0
fld public final static org.omg.PortableServer.IdUniquenessPolicyValue MULTIPLE_ID
fld public final static org.omg.PortableServer.IdUniquenessPolicyValue UNIQUE_ID
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.PortableServer.IdUniquenessPolicyValue from_int(int)
supr java.lang.Object
hfds __array,__size,__value

CLSS public abstract org.omg.PortableServer.IdUniquenessPolicyValueHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.IdUniquenessPolicyValue extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.IdUniquenessPolicyValue read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.IdUniquenessPolicyValue)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.IdUniquenessPolicyValue)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableServer.IdUniquenessPolicyValueHolder
cons public init()
cons public init(org.omg.PortableServer.IdUniquenessPolicyValue)
fld public org.omg.PortableServer.IdUniquenessPolicyValue value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableServer.ImplicitActivationPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableServer.ImplicitActivationPolicyOperations

CLSS public abstract interface org.omg.PortableServer.ImplicitActivationPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract org.omg.PortableServer.ImplicitActivationPolicyValue value()

CLSS public org.omg.PortableServer.ImplicitActivationPolicyValue
cons protected init(int)
fld public final static int _IMPLICIT_ACTIVATION = 0
fld public final static int _NO_IMPLICIT_ACTIVATION = 1
fld public final static org.omg.PortableServer.ImplicitActivationPolicyValue IMPLICIT_ACTIVATION
fld public final static org.omg.PortableServer.ImplicitActivationPolicyValue NO_IMPLICIT_ACTIVATION
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.PortableServer.ImplicitActivationPolicyValue from_int(int)
supr java.lang.Object
hfds __array,__size,__value

CLSS public abstract org.omg.PortableServer.ImplicitActivationPolicyValueHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.ImplicitActivationPolicyValue extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.ImplicitActivationPolicyValue read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.ImplicitActivationPolicyValue)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.ImplicitActivationPolicyValue)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableServer.ImplicitActivationPolicyValueHolder
cons public init()
cons public init(org.omg.PortableServer.ImplicitActivationPolicyValue)
fld public org.omg.PortableServer.ImplicitActivationPolicyValue value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableServer.LIFESPAN_POLICY_ID
fld public final static int value = 17

CLSS public abstract interface org.omg.PortableServer.LifespanPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableServer.LifespanPolicyOperations

CLSS public abstract interface org.omg.PortableServer.LifespanPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract org.omg.PortableServer.LifespanPolicyValue value()

CLSS public org.omg.PortableServer.LifespanPolicyValue
cons protected init(int)
fld public final static int _PERSISTENT = 1
fld public final static int _TRANSIENT = 0
fld public final static org.omg.PortableServer.LifespanPolicyValue PERSISTENT
fld public final static org.omg.PortableServer.LifespanPolicyValue TRANSIENT
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.PortableServer.LifespanPolicyValue from_int(int)
supr java.lang.Object
hfds __array,__size,__value

CLSS public abstract org.omg.PortableServer.LifespanPolicyValueHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.LifespanPolicyValue extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.LifespanPolicyValue read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.LifespanPolicyValue)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.LifespanPolicyValue)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableServer.LifespanPolicyValueHolder
cons public init()
cons public init(org.omg.PortableServer.LifespanPolicyValue)
fld public org.omg.PortableServer.LifespanPolicyValue value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.PortableServer.ObjectIdHelper
cons public init()
meth public static byte[] extract(org.omg.CORBA.Any)
meth public static byte[] read(org.omg.CORBA.portable.InputStream)
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,byte[])
meth public static void write(org.omg.CORBA.portable.OutputStream,byte[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableServer.ObjectIdHolder
cons public init()
cons public init(byte[])
fld public byte[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableServer.POA
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableServer.POAOperations

CLSS public abstract org.omg.PortableServer.POAHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.POA extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.POA narrow(org.omg.CORBA.Object)
meth public static org.omg.PortableServer.POA read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.POA)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.POA)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract org.omg.PortableServer.POAListHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.POA[] extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.POA[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.POA[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.POA[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableServer.POAListHolder
cons public init()
cons public init(org.omg.PortableServer.POA[])
fld public org.omg.PortableServer.POA[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableServer.POAManager
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableServer.POAManagerOperations

CLSS public abstract interface org.omg.PortableServer.POAManagerOperations
meth public abstract org.omg.PortableServer.POAManagerPackage.State get_state()
meth public abstract void activate() throws org.omg.PortableServer.POAManagerPackage.AdapterInactive
meth public abstract void deactivate(boolean,boolean) throws org.omg.PortableServer.POAManagerPackage.AdapterInactive
meth public abstract void discard_requests(boolean) throws org.omg.PortableServer.POAManagerPackage.AdapterInactive
meth public abstract void hold_requests(boolean) throws org.omg.PortableServer.POAManagerPackage.AdapterInactive

CLSS public final org.omg.PortableServer.POAManagerPackage.AdapterInactive
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableServer.POAManagerPackage.AdapterInactiveHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.POAManagerPackage.AdapterInactive extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.POAManagerPackage.AdapterInactive read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.POAManagerPackage.AdapterInactive)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.POAManagerPackage.AdapterInactive)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableServer.POAManagerPackage.AdapterInactiveHolder
cons public init()
cons public init(org.omg.PortableServer.POAManagerPackage.AdapterInactive)
fld public org.omg.PortableServer.POAManagerPackage.AdapterInactive value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.PortableServer.POAManagerPackage.State
cons protected init(int)
fld public final static int _ACTIVE = 1
fld public final static int _DISCARDING = 2
fld public final static int _HOLDING = 0
fld public final static int _INACTIVE = 3
fld public final static org.omg.PortableServer.POAManagerPackage.State ACTIVE
fld public final static org.omg.PortableServer.POAManagerPackage.State DISCARDING
fld public final static org.omg.PortableServer.POAManagerPackage.State HOLDING
fld public final static org.omg.PortableServer.POAManagerPackage.State INACTIVE
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.PortableServer.POAManagerPackage.State from_int(int)
supr java.lang.Object
hfds __array,__size,__value

CLSS public abstract org.omg.PortableServer.POAManagerPackage.StateHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.POAManagerPackage.State extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.POAManagerPackage.State read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.POAManagerPackage.State)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.POAManagerPackage.State)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableServer.POAManagerPackage.StateHolder
cons public init()
cons public init(org.omg.PortableServer.POAManagerPackage.State)
fld public org.omg.PortableServer.POAManagerPackage.State value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableServer.POAOperations
meth public abstract byte[] activate_object(org.omg.PortableServer.Servant) throws org.omg.PortableServer.POAPackage.ServantAlreadyActive,org.omg.PortableServer.POAPackage.WrongPolicy
meth public abstract byte[] id()
meth public abstract byte[] reference_to_id(org.omg.CORBA.Object) throws org.omg.PortableServer.POAPackage.WrongAdapter,org.omg.PortableServer.POAPackage.WrongPolicy
meth public abstract byte[] servant_to_id(org.omg.PortableServer.Servant) throws org.omg.PortableServer.POAPackage.ServantNotActive,org.omg.PortableServer.POAPackage.WrongPolicy
meth public abstract java.lang.String the_name()
meth public abstract org.omg.CORBA.Object create_reference(java.lang.String) throws org.omg.PortableServer.POAPackage.WrongPolicy
meth public abstract org.omg.CORBA.Object create_reference_with_id(byte[],java.lang.String)
meth public abstract org.omg.CORBA.Object id_to_reference(byte[]) throws org.omg.PortableServer.POAPackage.ObjectNotActive,org.omg.PortableServer.POAPackage.WrongPolicy
meth public abstract org.omg.CORBA.Object servant_to_reference(org.omg.PortableServer.Servant) throws org.omg.PortableServer.POAPackage.ServantNotActive,org.omg.PortableServer.POAPackage.WrongPolicy
meth public abstract org.omg.PortableServer.AdapterActivator the_activator()
meth public abstract org.omg.PortableServer.IdAssignmentPolicy create_id_assignment_policy(org.omg.PortableServer.IdAssignmentPolicyValue)
meth public abstract org.omg.PortableServer.IdUniquenessPolicy create_id_uniqueness_policy(org.omg.PortableServer.IdUniquenessPolicyValue)
meth public abstract org.omg.PortableServer.ImplicitActivationPolicy create_implicit_activation_policy(org.omg.PortableServer.ImplicitActivationPolicyValue)
meth public abstract org.omg.PortableServer.LifespanPolicy create_lifespan_policy(org.omg.PortableServer.LifespanPolicyValue)
meth public abstract org.omg.PortableServer.POA create_POA(java.lang.String,org.omg.PortableServer.POAManager,org.omg.CORBA.Policy[]) throws org.omg.PortableServer.POAPackage.AdapterAlreadyExists,org.omg.PortableServer.POAPackage.InvalidPolicy
meth public abstract org.omg.PortableServer.POA find_POA(java.lang.String,boolean) throws org.omg.PortableServer.POAPackage.AdapterNonExistent
meth public abstract org.omg.PortableServer.POA the_parent()
meth public abstract org.omg.PortableServer.POAManager the_POAManager()
meth public abstract org.omg.PortableServer.POA[] the_children()
meth public abstract org.omg.PortableServer.RequestProcessingPolicy create_request_processing_policy(org.omg.PortableServer.RequestProcessingPolicyValue)
meth public abstract org.omg.PortableServer.Servant get_servant() throws org.omg.PortableServer.POAPackage.NoServant,org.omg.PortableServer.POAPackage.WrongPolicy
meth public abstract org.omg.PortableServer.Servant id_to_servant(byte[]) throws org.omg.PortableServer.POAPackage.ObjectNotActive,org.omg.PortableServer.POAPackage.WrongPolicy
meth public abstract org.omg.PortableServer.Servant reference_to_servant(org.omg.CORBA.Object) throws org.omg.PortableServer.POAPackage.ObjectNotActive,org.omg.PortableServer.POAPackage.WrongAdapter,org.omg.PortableServer.POAPackage.WrongPolicy
meth public abstract org.omg.PortableServer.ServantManager get_servant_manager() throws org.omg.PortableServer.POAPackage.WrongPolicy
meth public abstract org.omg.PortableServer.ServantRetentionPolicy create_servant_retention_policy(org.omg.PortableServer.ServantRetentionPolicyValue)
meth public abstract org.omg.PortableServer.ThreadPolicy create_thread_policy(org.omg.PortableServer.ThreadPolicyValue)
meth public abstract void activate_object_with_id(byte[],org.omg.PortableServer.Servant) throws org.omg.PortableServer.POAPackage.ObjectAlreadyActive,org.omg.PortableServer.POAPackage.ServantAlreadyActive,org.omg.PortableServer.POAPackage.WrongPolicy
meth public abstract void deactivate_object(byte[]) throws org.omg.PortableServer.POAPackage.ObjectNotActive,org.omg.PortableServer.POAPackage.WrongPolicy
meth public abstract void destroy(boolean,boolean)
meth public abstract void set_servant(org.omg.PortableServer.Servant) throws org.omg.PortableServer.POAPackage.WrongPolicy
meth public abstract void set_servant_manager(org.omg.PortableServer.ServantManager) throws org.omg.PortableServer.POAPackage.WrongPolicy
meth public abstract void the_activator(org.omg.PortableServer.AdapterActivator)

CLSS public final org.omg.PortableServer.POAPackage.AdapterAlreadyExists
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableServer.POAPackage.AdapterAlreadyExistsHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.POAPackage.AdapterAlreadyExists extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.POAPackage.AdapterAlreadyExists read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.POAPackage.AdapterAlreadyExists)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.POAPackage.AdapterAlreadyExists)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableServer.POAPackage.AdapterAlreadyExistsHolder
cons public init()
cons public init(org.omg.PortableServer.POAPackage.AdapterAlreadyExists)
fld public org.omg.PortableServer.POAPackage.AdapterAlreadyExists value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.PortableServer.POAPackage.AdapterNonExistent
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableServer.POAPackage.AdapterNonExistentHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.POAPackage.AdapterNonExistent extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.POAPackage.AdapterNonExistent read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.POAPackage.AdapterNonExistent)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.POAPackage.AdapterNonExistent)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableServer.POAPackage.AdapterNonExistentHolder
cons public init()
cons public init(org.omg.PortableServer.POAPackage.AdapterNonExistent)
fld public org.omg.PortableServer.POAPackage.AdapterNonExistent value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.PortableServer.POAPackage.InvalidPolicy
cons public init()
cons public init(java.lang.String,short)
cons public init(short)
fld public short index
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableServer.POAPackage.InvalidPolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.POAPackage.InvalidPolicy extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.POAPackage.InvalidPolicy read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.POAPackage.InvalidPolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.POAPackage.InvalidPolicy)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableServer.POAPackage.InvalidPolicyHolder
cons public init()
cons public init(org.omg.PortableServer.POAPackage.InvalidPolicy)
fld public org.omg.PortableServer.POAPackage.InvalidPolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.PortableServer.POAPackage.NoServant
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableServer.POAPackage.NoServantHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.POAPackage.NoServant extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.POAPackage.NoServant read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.POAPackage.NoServant)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.POAPackage.NoServant)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableServer.POAPackage.NoServantHolder
cons public init()
cons public init(org.omg.PortableServer.POAPackage.NoServant)
fld public org.omg.PortableServer.POAPackage.NoServant value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.PortableServer.POAPackage.ObjectAlreadyActive
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableServer.POAPackage.ObjectAlreadyActiveHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.POAPackage.ObjectAlreadyActive extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.POAPackage.ObjectAlreadyActive read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.POAPackage.ObjectAlreadyActive)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.POAPackage.ObjectAlreadyActive)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableServer.POAPackage.ObjectAlreadyActiveHolder
cons public init()
cons public init(org.omg.PortableServer.POAPackage.ObjectAlreadyActive)
fld public org.omg.PortableServer.POAPackage.ObjectAlreadyActive value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.PortableServer.POAPackage.ObjectNotActive
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableServer.POAPackage.ObjectNotActiveHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.POAPackage.ObjectNotActive extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.POAPackage.ObjectNotActive read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.POAPackage.ObjectNotActive)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.POAPackage.ObjectNotActive)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableServer.POAPackage.ObjectNotActiveHolder
cons public init()
cons public init(org.omg.PortableServer.POAPackage.ObjectNotActive)
fld public org.omg.PortableServer.POAPackage.ObjectNotActive value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.PortableServer.POAPackage.ServantAlreadyActive
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableServer.POAPackage.ServantAlreadyActiveHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.POAPackage.ServantAlreadyActive extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.POAPackage.ServantAlreadyActive read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.POAPackage.ServantAlreadyActive)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.POAPackage.ServantAlreadyActive)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableServer.POAPackage.ServantAlreadyActiveHolder
cons public init()
cons public init(org.omg.PortableServer.POAPackage.ServantAlreadyActive)
fld public org.omg.PortableServer.POAPackage.ServantAlreadyActive value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.PortableServer.POAPackage.ServantNotActive
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableServer.POAPackage.ServantNotActiveHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.POAPackage.ServantNotActive extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.POAPackage.ServantNotActive read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.POAPackage.ServantNotActive)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.POAPackage.ServantNotActive)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableServer.POAPackage.ServantNotActiveHolder
cons public init()
cons public init(org.omg.PortableServer.POAPackage.ServantNotActive)
fld public org.omg.PortableServer.POAPackage.ServantNotActive value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.PortableServer.POAPackage.WrongAdapter
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableServer.POAPackage.WrongAdapterHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.POAPackage.WrongAdapter extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.POAPackage.WrongAdapter read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.POAPackage.WrongAdapter)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.POAPackage.WrongAdapter)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableServer.POAPackage.WrongAdapterHolder
cons public init()
cons public init(org.omg.PortableServer.POAPackage.WrongAdapter)
fld public org.omg.PortableServer.POAPackage.WrongAdapter value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.PortableServer.POAPackage.WrongPolicy
cons public init()
cons public init(java.lang.String)
supr org.omg.CORBA.UserException

CLSS public abstract org.omg.PortableServer.POAPackage.WrongPolicyHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.POAPackage.WrongPolicy extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.POAPackage.WrongPolicy read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.POAPackage.WrongPolicy)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.POAPackage.WrongPolicy)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.PortableServer.POAPackage.WrongPolicyHolder
cons public init()
cons public init(org.omg.PortableServer.POAPackage.WrongPolicy)
fld public org.omg.PortableServer.POAPackage.WrongPolicy value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableServer.REQUEST_PROCESSING_POLICY_ID
fld public final static int value = 22

CLSS public abstract interface org.omg.PortableServer.RequestProcessingPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableServer.RequestProcessingPolicyOperations

CLSS public abstract interface org.omg.PortableServer.RequestProcessingPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract org.omg.PortableServer.RequestProcessingPolicyValue value()

CLSS public org.omg.PortableServer.RequestProcessingPolicyValue
cons protected init(int)
fld public final static int _USE_ACTIVE_OBJECT_MAP_ONLY = 0
fld public final static int _USE_DEFAULT_SERVANT = 1
fld public final static int _USE_SERVANT_MANAGER = 2
fld public final static org.omg.PortableServer.RequestProcessingPolicyValue USE_ACTIVE_OBJECT_MAP_ONLY
fld public final static org.omg.PortableServer.RequestProcessingPolicyValue USE_DEFAULT_SERVANT
fld public final static org.omg.PortableServer.RequestProcessingPolicyValue USE_SERVANT_MANAGER
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.PortableServer.RequestProcessingPolicyValue from_int(int)
supr java.lang.Object
hfds __array,__size,__value

CLSS public abstract org.omg.PortableServer.RequestProcessingPolicyValueHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.RequestProcessingPolicyValue extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.RequestProcessingPolicyValue read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.RequestProcessingPolicyValue)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.RequestProcessingPolicyValue)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableServer.RequestProcessingPolicyValueHolder
cons public init()
cons public init(org.omg.PortableServer.RequestProcessingPolicyValue)
fld public org.omg.PortableServer.RequestProcessingPolicyValue value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableServer.SERVANT_RETENTION_POLICY_ID
fld public final static int value = 21

CLSS public abstract org.omg.PortableServer.Servant
cons public init()
meth public abstract java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public boolean _is_a(java.lang.String)
meth public boolean _non_existent()
meth public final byte[] _object_id()
meth public final org.omg.CORBA.ORB _orb()
meth public final org.omg.CORBA.Object _this_object()
meth public final org.omg.CORBA.Object _this_object(org.omg.CORBA.ORB)
meth public final org.omg.PortableServer.POA _poa()
meth public final org.omg.PortableServer.portable.Delegate _get_delegate()
meth public final void _set_delegate(org.omg.PortableServer.portable.Delegate)
meth public org.omg.CORBA.Object _get_interface_def()
meth public org.omg.PortableServer.POA _default_POA()
supr java.lang.Object
hfds _delegate

CLSS public abstract interface org.omg.PortableServer.ServantActivator
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableServer.ServantActivatorOperations
intf org.omg.PortableServer.ServantManager

CLSS public abstract org.omg.PortableServer.ServantActivatorHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.ServantActivator extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.ServantActivator narrow(org.omg.CORBA.Object)
meth public static org.omg.PortableServer.ServantActivator read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.PortableServer.ServantActivator unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.ServantActivator)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.ServantActivator)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableServer.ServantActivatorHolder
cons public init()
cons public init(org.omg.PortableServer.ServantActivator)
fld public org.omg.PortableServer.ServantActivator value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableServer.ServantActivatorOperations
intf org.omg.PortableServer.ServantManagerOperations
meth public abstract org.omg.PortableServer.Servant incarnate(byte[],org.omg.PortableServer.POA) throws org.omg.PortableServer.ForwardRequest
meth public abstract void etherealize(byte[],org.omg.PortableServer.POA,org.omg.PortableServer.Servant,boolean,boolean)

CLSS public abstract org.omg.PortableServer.ServantActivatorPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.PortableServer.ServantActivatorOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.PortableServer.ServantActivator _this()
meth public org.omg.PortableServer.ServantActivator _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.PortableServer.ServantActivatorPOATie
cons public init(org.omg.PortableServer.ServantActivatorOperations)
cons public init(org.omg.PortableServer.ServantActivatorOperations,org.omg.PortableServer.POA)
meth public org.omg.PortableServer.POA _default_POA()
meth public org.omg.PortableServer.Servant incarnate(byte[],org.omg.PortableServer.POA) throws org.omg.PortableServer.ForwardRequest
meth public org.omg.PortableServer.ServantActivatorOperations _delegate()
meth public void _delegate(org.omg.PortableServer.ServantActivatorOperations)
meth public void etherealize(byte[],org.omg.PortableServer.POA,org.omg.PortableServer.Servant,boolean,boolean)
supr org.omg.PortableServer.ServantActivatorPOA
hfds _impl,_poa

CLSS public abstract interface org.omg.PortableServer.ServantLocator
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableServer.ServantLocatorOperations
intf org.omg.PortableServer.ServantManager

CLSS public abstract org.omg.PortableServer.ServantLocatorHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.ServantLocator extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.ServantLocator narrow(org.omg.CORBA.Object)
meth public static org.omg.PortableServer.ServantLocator read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.PortableServer.ServantLocator unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.ServantLocator)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.ServantLocator)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableServer.ServantLocatorHolder
cons public init()
cons public init(org.omg.PortableServer.ServantLocator)
fld public org.omg.PortableServer.ServantLocator value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableServer.ServantLocatorOperations
intf org.omg.PortableServer.ServantManagerOperations
meth public abstract org.omg.PortableServer.Servant preinvoke(byte[],org.omg.PortableServer.POA,java.lang.String,org.omg.PortableServer.ServantLocatorPackage.CookieHolder) throws org.omg.PortableServer.ForwardRequest
meth public abstract void postinvoke(byte[],org.omg.PortableServer.POA,java.lang.String,java.lang.Object,org.omg.PortableServer.Servant)

CLSS public abstract org.omg.PortableServer.ServantLocatorPOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.PortableServer.ServantLocatorOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.PortableServer.ServantLocator _this()
meth public org.omg.PortableServer.ServantLocator _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.PortableServer.ServantLocatorPOATie
cons public init(org.omg.PortableServer.ServantLocatorOperations)
cons public init(org.omg.PortableServer.ServantLocatorOperations,org.omg.PortableServer.POA)
meth public org.omg.PortableServer.POA _default_POA()
meth public org.omg.PortableServer.Servant preinvoke(byte[],org.omg.PortableServer.POA,java.lang.String,org.omg.PortableServer.ServantLocatorPackage.CookieHolder) throws org.omg.PortableServer.ForwardRequest
meth public org.omg.PortableServer.ServantLocatorOperations _delegate()
meth public void _delegate(org.omg.PortableServer.ServantLocatorOperations)
meth public void postinvoke(byte[],org.omg.PortableServer.POA,java.lang.String,java.lang.Object,org.omg.PortableServer.Servant)
supr org.omg.PortableServer.ServantLocatorPOA
hfds _impl,_poa

CLSS public final org.omg.PortableServer.ServantLocatorPackage.CookieHolder
cons public init()
cons public init(java.lang.Object)
fld public java.lang.Object value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableServer.ServantManager
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableServer.ServantManagerOperations

CLSS public abstract interface org.omg.PortableServer.ServantManagerOperations

CLSS public abstract interface org.omg.PortableServer.ServantRetentionPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableServer.ServantRetentionPolicyOperations

CLSS public abstract interface org.omg.PortableServer.ServantRetentionPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract org.omg.PortableServer.ServantRetentionPolicyValue value()

CLSS public org.omg.PortableServer.ServantRetentionPolicyValue
cons protected init(int)
fld public final static int _NON_RETAIN = 1
fld public final static int _RETAIN = 0
fld public final static org.omg.PortableServer.ServantRetentionPolicyValue NON_RETAIN
fld public final static org.omg.PortableServer.ServantRetentionPolicyValue RETAIN
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.PortableServer.ServantRetentionPolicyValue from_int(int)
supr java.lang.Object
hfds __array,__size,__value

CLSS public abstract org.omg.PortableServer.ServantRetentionPolicyValueHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.ServantRetentionPolicyValue extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.ServantRetentionPolicyValue read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.ServantRetentionPolicyValue)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.ServantRetentionPolicyValue)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableServer.ServantRetentionPolicyValueHolder
cons public init()
cons public init(org.omg.PortableServer.ServantRetentionPolicyValue)
fld public org.omg.PortableServer.ServantRetentionPolicyValue value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.PortableServer.THREAD_POLICY_ID
fld public final static int value = 16

CLSS public abstract interface org.omg.PortableServer.ThreadPolicy
intf org.omg.CORBA.Policy
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.PortableServer.ThreadPolicyOperations

CLSS public abstract interface org.omg.PortableServer.ThreadPolicyOperations
intf org.omg.CORBA.PolicyOperations
meth public abstract org.omg.PortableServer.ThreadPolicyValue value()

CLSS public org.omg.PortableServer.ThreadPolicyValue
cons protected init(int)
fld public final static int _ORB_CTRL_MODEL = 0
fld public final static int _SINGLE_THREAD_MODEL = 1
fld public final static org.omg.PortableServer.ThreadPolicyValue ORB_CTRL_MODEL
fld public final static org.omg.PortableServer.ThreadPolicyValue SINGLE_THREAD_MODEL
intf org.omg.CORBA.portable.IDLEntity
meth public int value()
meth public static org.omg.PortableServer.ThreadPolicyValue from_int(int)
supr java.lang.Object
hfds __array,__size,__value

CLSS public abstract org.omg.PortableServer.ThreadPolicyValueHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.PortableServer.ThreadPolicyValue extract(org.omg.CORBA.Any)
meth public static org.omg.PortableServer.ThreadPolicyValue read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.PortableServer.ThreadPolicyValue)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.PortableServer.ThreadPolicyValue)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.PortableServer.ThreadPolicyValueHolder
cons public init()
cons public init(org.omg.PortableServer.ThreadPolicyValue)
fld public org.omg.PortableServer.ThreadPolicyValue value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public org.omg.PortableServer._ServantActivatorStub
cons public init()
fld public final static java.lang.Class _opsClass
intf org.omg.PortableServer.ServantActivator
meth public java.lang.String[] _ids()
meth public org.omg.PortableServer.Servant incarnate(byte[],org.omg.PortableServer.POA) throws org.omg.PortableServer.ForwardRequest
meth public void etherealize(byte[],org.omg.PortableServer.POA,org.omg.PortableServer.Servant,boolean,boolean)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.PortableServer._ServantLocatorStub
cons public init()
fld public final static java.lang.Class _opsClass
intf org.omg.PortableServer.ServantLocator
meth public java.lang.String[] _ids()
meth public org.omg.PortableServer.Servant preinvoke(byte[],org.omg.PortableServer.POA,java.lang.String,org.omg.PortableServer.ServantLocatorPackage.CookieHolder) throws org.omg.PortableServer.ForwardRequest
meth public void postinvoke(byte[],org.omg.PortableServer.POA,java.lang.String,java.lang.Object,org.omg.PortableServer.Servant)
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public abstract interface org.omg.PortableServer.portable.Delegate
meth public abstract boolean is_a(org.omg.PortableServer.Servant,java.lang.String)
meth public abstract boolean non_existent(org.omg.PortableServer.Servant)
meth public abstract byte[] object_id(org.omg.PortableServer.Servant)
meth public abstract org.omg.CORBA.ORB orb(org.omg.PortableServer.Servant)
meth public abstract org.omg.CORBA.Object get_interface_def(org.omg.PortableServer.Servant)
meth public abstract org.omg.CORBA.Object this_object(org.omg.PortableServer.Servant)
meth public abstract org.omg.PortableServer.POA default_POA(org.omg.PortableServer.Servant)
meth public abstract org.omg.PortableServer.POA poa(org.omg.PortableServer.Servant)

CLSS public abstract interface org.omg.SendingContext.CodeBase
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.SendingContext.CodeBaseOperations
intf org.omg.SendingContext.RunTime

CLSS public abstract org.omg.SendingContext.CodeBaseHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.SendingContext.CodeBase extract(org.omg.CORBA.Any)
meth public static org.omg.SendingContext.CodeBase narrow(org.omg.CORBA.Object)
meth public static org.omg.SendingContext.CodeBase read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.SendingContext.CodeBase unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.SendingContext.CodeBase)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.SendingContext.CodeBase)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.SendingContext.CodeBaseHolder
cons public init()
cons public init(org.omg.SendingContext.CodeBase)
fld public org.omg.SendingContext.CodeBase value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.SendingContext.CodeBaseOperations
intf org.omg.SendingContext.RunTimeOperations
meth public abstract java.lang.String implementation(java.lang.String)
meth public abstract java.lang.String implementationx(java.lang.String)
meth public abstract java.lang.String[] bases(java.lang.String)
meth public abstract java.lang.String[] implementations(java.lang.String[])
meth public abstract org.omg.CORBA.Repository get_ir()
meth public abstract org.omg.CORBA.ValueDefPackage.FullValueDescription meta(java.lang.String)
meth public abstract org.omg.CORBA.ValueDefPackage.FullValueDescription[] metas(java.lang.String[])

CLSS public abstract org.omg.SendingContext.CodeBasePOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.SendingContext.CodeBaseOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.SendingContext.CodeBase _this()
meth public org.omg.SendingContext.CodeBase _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.SendingContext.CodeBasePOATie
cons public init(org.omg.SendingContext.CodeBaseOperations)
cons public init(org.omg.SendingContext.CodeBaseOperations,org.omg.PortableServer.POA)
meth public java.lang.String implementation(java.lang.String)
meth public java.lang.String implementationx(java.lang.String)
meth public java.lang.String[] bases(java.lang.String)
meth public java.lang.String[] implementations(java.lang.String[])
meth public org.omg.CORBA.Repository get_ir()
meth public org.omg.CORBA.ValueDefPackage.FullValueDescription meta(java.lang.String)
meth public org.omg.CORBA.ValueDefPackage.FullValueDescription[] metas(java.lang.String[])
meth public org.omg.PortableServer.POA _default_POA()
meth public org.omg.SendingContext.CodeBaseOperations _delegate()
meth public void _delegate(org.omg.SendingContext.CodeBaseOperations)
supr org.omg.SendingContext.CodeBasePOA
hfds _impl,_poa

CLSS public abstract org.omg.SendingContext.CodeBasePackage.URLHelper
cons public init()
meth public static java.lang.String extract(org.omg.CORBA.Any)
meth public static java.lang.String id()
meth public static java.lang.String read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String)
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract org.omg.SendingContext.CodeBasePackage.URLSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static java.lang.String[] extract(org.omg.CORBA.Any)
meth public static java.lang.String[] read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,java.lang.String[])
meth public static void write(org.omg.CORBA.portable.OutputStream,java.lang.String[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.SendingContext.CodeBasePackage.URLSeqHolder
cons public init()
cons public init(java.lang.String[])
fld public java.lang.String[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.SendingContext.CodeBasePackage.ValueDescSeqHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.CORBA.ValueDefPackage.FullValueDescription[] extract(org.omg.CORBA.Any)
meth public static org.omg.CORBA.ValueDefPackage.FullValueDescription[] read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.CORBA.ValueDefPackage.FullValueDescription[])
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.CORBA.ValueDefPackage.FullValueDescription[])
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.SendingContext.CodeBasePackage.ValueDescSeqHolder
cons public init()
cons public init(org.omg.CORBA.ValueDefPackage.FullValueDescription[])
fld public org.omg.CORBA.ValueDefPackage.FullValueDescription[] value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.SendingContext.RunTime
intf org.omg.CORBA.Object
intf org.omg.CORBA.portable.IDLEntity
intf org.omg.SendingContext.RunTimeOperations

CLSS public abstract org.omg.SendingContext.RunTimeHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.SendingContext.RunTime extract(org.omg.CORBA.Any)
meth public static org.omg.SendingContext.RunTime narrow(org.omg.CORBA.Object)
meth public static org.omg.SendingContext.RunTime read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.SendingContext.RunTime unchecked_narrow(org.omg.CORBA.Object)
meth public static void insert(org.omg.CORBA.Any,org.omg.SendingContext.RunTime)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.SendingContext.RunTime)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.SendingContext.RunTimeHolder
cons public init()
cons public init(org.omg.SendingContext.RunTime)
fld public org.omg.SendingContext.RunTime value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract interface org.omg.SendingContext.RunTimeOperations

CLSS public abstract org.omg.SendingContext.RunTimePOA
cons public init()
intf org.omg.CORBA.portable.InvokeHandler
intf org.omg.SendingContext.RunTimeOperations
meth public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA,byte[])
meth public org.omg.CORBA.portable.OutputStream _invoke(java.lang.String,org.omg.CORBA.portable.InputStream,org.omg.CORBA.portable.ResponseHandler)
meth public org.omg.SendingContext.RunTime _this()
meth public org.omg.SendingContext.RunTime _this(org.omg.CORBA.ORB)
supr org.omg.PortableServer.Servant
hfds __ids,_methods

CLSS public org.omg.SendingContext.RunTimePOATie
cons public init(org.omg.SendingContext.RunTimeOperations)
cons public init(org.omg.SendingContext.RunTimeOperations,org.omg.PortableServer.POA)
meth public org.omg.PortableServer.POA _default_POA()
meth public org.omg.SendingContext.RunTimeOperations _delegate()
meth public void _delegate(org.omg.SendingContext.RunTimeOperations)
supr org.omg.SendingContext.RunTimePOA
hfds _impl,_poa

CLSS public org.omg.SendingContext._CodeBaseStub
cons public init()
intf org.omg.SendingContext.CodeBase
meth public java.lang.String implementation(java.lang.String)
meth public java.lang.String implementationx(java.lang.String)
meth public java.lang.String[] _ids()
meth public java.lang.String[] bases(java.lang.String)
meth public java.lang.String[] implementations(java.lang.String[])
meth public org.omg.CORBA.Repository get_ir()
meth public org.omg.CORBA.ValueDefPackage.FullValueDescription meta(java.lang.String)
meth public org.omg.CORBA.ValueDefPackage.FullValueDescription[] metas(java.lang.String[])
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public org.omg.SendingContext._RunTimeStub
cons public init()
intf org.omg.SendingContext.RunTime
meth public java.lang.String[] _ids()
supr org.omg.CORBA.portable.ObjectImpl
hfds __ids

CLSS public abstract org.omg.TimeBase.InaccuracyTHelper
cons public init()
meth public static java.lang.String id()
meth public static long extract(org.omg.CORBA.Any)
meth public static long read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,long)
meth public static void write(org.omg.CORBA.portable.OutputStream,long)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.TimeBase.IntervalT
cons public init()
cons public init(long,long)
fld public long lower_bound
fld public long upper_bound
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.TimeBase.IntervalTHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.TimeBase.IntervalT extract(org.omg.CORBA.Any)
meth public static org.omg.TimeBase.IntervalT read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.TimeBase.IntervalT)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.TimeBase.IntervalT)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.TimeBase.IntervalTHolder
cons public init()
cons public init(org.omg.TimeBase.IntervalT)
fld public org.omg.TimeBase.IntervalT value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public abstract org.omg.TimeBase.TdfTHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static short extract(org.omg.CORBA.Any)
meth public static short read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,short)
meth public static void write(org.omg.CORBA.portable.OutputStream,short)
supr java.lang.Object
hfds __typeCode,_id

CLSS public abstract org.omg.TimeBase.TimeTHelper
cons public init()
meth public static java.lang.String id()
meth public static long extract(org.omg.CORBA.Any)
meth public static long read(org.omg.CORBA.portable.InputStream)
meth public static org.omg.CORBA.TypeCode type()
meth public static void insert(org.omg.CORBA.Any,long)
meth public static void write(org.omg.CORBA.portable.OutputStream,long)
supr java.lang.Object
hfds __typeCode,_id

CLSS public final org.omg.TimeBase.UtcT
cons public init()
cons public init(long,int,short,short)
fld public int inacclo
fld public long time
fld public short inacchi
fld public short tdf
intf org.omg.CORBA.portable.IDLEntity
supr java.lang.Object

CLSS public abstract org.omg.TimeBase.UtcTHelper
cons public init()
meth public static java.lang.String id()
meth public static org.omg.CORBA.TypeCode type()
meth public static org.omg.TimeBase.UtcT extract(org.omg.CORBA.Any)
meth public static org.omg.TimeBase.UtcT read(org.omg.CORBA.portable.InputStream)
meth public static void insert(org.omg.CORBA.Any,org.omg.TimeBase.UtcT)
meth public static void write(org.omg.CORBA.portable.OutputStream,org.omg.TimeBase.UtcT)
supr java.lang.Object
hfds __active,__typeCode,_id

CLSS public final org.omg.TimeBase.UtcTHolder
cons public init()
cons public init(org.omg.TimeBase.UtcT)
fld public org.omg.TimeBase.UtcT value
intf org.omg.CORBA.portable.Streamable
meth public org.omg.CORBA.TypeCode _type()
meth public void _read(org.omg.CORBA.portable.InputStream)
meth public void _write(org.omg.CORBA.portable.OutputStream)
supr java.lang.Object

CLSS public final org.omg.stub.java.rmi._Remote_Stub
cons public init()
intf java.rmi.Remote
meth public java.lang.String[] _ids()
supr javax.rmi.CORBA.Stub
hfds _type_ids

