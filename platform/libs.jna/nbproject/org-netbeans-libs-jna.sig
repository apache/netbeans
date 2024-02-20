#Signature file v4.1
#Version 2.18

CLSS public abstract interface com.sun.jna.AltCallingConvention

CLSS public abstract interface com.sun.jna.Callback
fld public final static java.lang.String METHOD_NAME = "callback"
fld public final static java.util.List<java.lang.String> FORBIDDEN_NAMES
innr public abstract interface static UncaughtExceptionHandler

CLSS public abstract interface static com.sun.jna.Callback$UncaughtExceptionHandler
 outer com.sun.jna.Callback
meth public abstract void uncaughtException(com.sun.jna.Callback,java.lang.Throwable)

CLSS public com.sun.jna.CallbackParameterContext
meth public int getIndex()
meth public java.lang.Object[] getArguments()
meth public java.lang.reflect.Method getMethod()
supr com.sun.jna.FromNativeContext
hfds args,index,method

CLSS public abstract interface com.sun.jna.CallbackProxy
intf com.sun.jna.Callback
meth public abstract java.lang.Class<?> getReturnType()
meth public abstract java.lang.Class<?>[] getParameterTypes()
meth public abstract java.lang.Object callback(java.lang.Object[])

CLSS public com.sun.jna.CallbackReference
intf java.io.Closeable
meth protected void dispose()
 anno 0 java.lang.Deprecated()
meth public com.sun.jna.Pointer getTrampoline()
meth public static com.sun.jna.Callback getCallback(java.lang.Class<?>,com.sun.jna.Pointer)
meth public static com.sun.jna.Pointer getFunctionPointer(com.sun.jna.Callback)
meth public void close()
supr java.lang.ref.WeakReference<com.sun.jna.Callback>
hfds DLL_CALLBACK_CLASS,PROXY_CALLBACK_METHOD,allocatedMemory,allocations,callbackMap,callingConvention,cbstruct,cleanable,directCallbackMap,initializers,method,pointerCallbackMap,proxy,trampoline
hcls AttachOptions,CallbackReferenceDisposer,DefaultCallbackProxy,NativeFunctionHandler

CLSS public com.sun.jna.CallbackResultContext
meth public java.lang.reflect.Method getMethod()
supr com.sun.jna.ToNativeContext
hfds method

CLSS public com.sun.jna.CallbackThreadInitializer
cons public init()
cons public init(boolean)
cons public init(boolean,boolean)
cons public init(boolean,boolean,java.lang.String)
cons public init(boolean,boolean,java.lang.String,java.lang.ThreadGroup)
meth public boolean detach(com.sun.jna.Callback)
meth public boolean isDaemon(com.sun.jna.Callback)
meth public java.lang.String getName(com.sun.jna.Callback)
meth public java.lang.ThreadGroup getThreadGroup(com.sun.jna.Callback)
supr java.lang.Object
hfds daemon,detach,group,name

CLSS public com.sun.jna.DefaultTypeMapper
cons public init()
intf com.sun.jna.TypeMapper
meth public com.sun.jna.FromNativeConverter getFromNativeConverter(java.lang.Class<?>)
meth public com.sun.jna.ToNativeConverter getToNativeConverter(java.lang.Class<?>)
meth public void addFromNativeConverter(java.lang.Class<?>,com.sun.jna.FromNativeConverter)
meth public void addToNativeConverter(java.lang.Class<?>,com.sun.jna.ToNativeConverter)
meth public void addTypeConverter(java.lang.Class<?>,com.sun.jna.TypeConverter)
supr java.lang.Object
hfds fromNativeConverters,toNativeConverters
hcls Entry

CLSS public com.sun.jna.FromNativeContext
meth public java.lang.Class<?> getTargetType()
supr java.lang.Object
hfds type

CLSS public abstract interface com.sun.jna.FromNativeConverter
meth public abstract java.lang.Class<?> nativeType()
meth public abstract java.lang.Object fromNative(java.lang.Object,com.sun.jna.FromNativeContext)

CLSS public com.sun.jna.Function
fld public final static int ALT_CONVENTION = 63
fld public final static int C_CONVENTION = 0
fld public final static int MAX_NARGS = 256
fld public final static int THROW_LAST_ERROR = 64
fld public final static int USE_VARARGS = 384
innr public abstract interface static PostCallRead
meth public boolean equals(java.lang.Object)
meth public com.sun.jna.Pointer invokePointer(java.lang.Object[])
meth public double invokeDouble(java.lang.Object[])
meth public float invokeFloat(java.lang.Object[])
meth public int getCallingConvention()
meth public int hashCode()
meth public int invokeInt(java.lang.Object[])
meth public java.lang.Object invoke(java.lang.Class<?>,java.lang.Object[])
meth public java.lang.Object invoke(java.lang.Class<?>,java.lang.Object[],java.util.Map<java.lang.String,?>)
meth public java.lang.Object invokeObject(java.lang.Object[])
meth public java.lang.String getName()
meth public java.lang.String invokeString(java.lang.Object[],boolean)
meth public java.lang.String toString()
meth public long invokeLong(java.lang.Object[])
meth public static com.sun.jna.Function getFunction(com.sun.jna.Pointer)
meth public static com.sun.jna.Function getFunction(com.sun.jna.Pointer,int)
meth public static com.sun.jna.Function getFunction(com.sun.jna.Pointer,int,java.lang.String)
meth public static com.sun.jna.Function getFunction(java.lang.String,java.lang.String)
meth public static com.sun.jna.Function getFunction(java.lang.String,java.lang.String,int)
meth public static com.sun.jna.Function getFunction(java.lang.String,java.lang.String,int,java.lang.String)
meth public void invoke(java.lang.Object[])
meth public void invokeVoid(java.lang.Object[])
supr com.sun.jna.Pointer
hfds INTEGER_FALSE,INTEGER_TRUE,IS_VARARGS,MASK_CC,OPTION_INVOKING_METHOD,callFlags,encoding,functionName,library,options
hcls NativeMappedArray,PointerArray

CLSS public abstract interface static com.sun.jna.Function$PostCallRead
 outer com.sun.jna.Function
meth public abstract void read()

CLSS public abstract interface com.sun.jna.FunctionMapper
meth public abstract java.lang.String getFunctionName(com.sun.jna.NativeLibrary,java.lang.reflect.Method)

CLSS public com.sun.jna.FunctionParameterContext
meth public com.sun.jna.Function getFunction()
meth public int getParameterIndex()
meth public java.lang.Object[] getParameters()
supr com.sun.jna.ToNativeContext
hfds args,function,index

CLSS public com.sun.jna.FunctionResultContext
meth public com.sun.jna.Function getFunction()
meth public java.lang.Object[] getArguments()
supr com.sun.jna.FromNativeContext
hfds args,function

CLSS public abstract com.sun.jna.IntegerType
cons public init(int)
cons public init(int,boolean)
cons public init(int,long)
cons public init(int,long,boolean)
intf com.sun.jna.NativeMapped
meth public boolean equals(java.lang.Object)
meth public double doubleValue()
meth public final static int compare(long,long)
meth public float floatValue()
meth public int hashCode()
meth public int intValue()
meth public java.lang.Class<?> nativeType()
meth public java.lang.Object fromNative(java.lang.Object,com.sun.jna.FromNativeContext)
meth public java.lang.Object toNative()
meth public java.lang.String toString()
meth public long longValue()
meth public static <%0 extends com.sun.jna.IntegerType> int compare({%%0},{%%0})
meth public static int compare(com.sun.jna.IntegerType,long)
meth public void setValue(long)
supr java.lang.Number
hfds number,serialVersionUID,size,unsigned,value

CLSS public abstract interface com.sun.jna.InvocationMapper
meth public abstract java.lang.reflect.InvocationHandler getInvocationHandler(com.sun.jna.NativeLibrary,java.lang.reflect.Method)

CLSS public final com.sun.jna.JNIEnv
fld public final static com.sun.jna.JNIEnv CURRENT
supr java.lang.Object

CLSS public com.sun.jna.LastErrorException
cons protected init(int,java.lang.String)
cons public init(int)
cons public init(java.lang.String)
meth public int getErrorCode()
supr java.lang.RuntimeException
hfds errorCode,serialVersionUID

CLSS public abstract interface com.sun.jna.Library
fld public final static java.lang.String OPTION_ALLOW_OBJECTS = "allow-objects"
fld public final static java.lang.String OPTION_CALLING_CONVENTION = "calling-convention"
fld public final static java.lang.String OPTION_CLASSLOADER = "classloader"
fld public final static java.lang.String OPTION_FUNCTION_MAPPER = "function-mapper"
fld public final static java.lang.String OPTION_INVOCATION_MAPPER = "invocation-mapper"
fld public final static java.lang.String OPTION_OPEN_FLAGS = "open-flags"
fld public final static java.lang.String OPTION_STRING_ENCODING = "string-encoding"
fld public final static java.lang.String OPTION_STRUCTURE_ALIGNMENT = "structure-alignment"
fld public final static java.lang.String OPTION_TYPE_MAPPER = "type-mapper"
innr public static Handler

CLSS public static com.sun.jna.Library$Handler
 outer com.sun.jna.Library
cons public init(java.lang.String,java.lang.Class<?>,java.util.Map<java.lang.String,?>)
intf java.lang.reflect.InvocationHandler
meth public com.sun.jna.NativeLibrary getNativeLibrary()
meth public java.lang.Class<?> getInterfaceClass()
meth public java.lang.Object invoke(java.lang.Object,java.lang.reflect.Method,java.lang.Object[]) throws java.lang.Throwable
meth public java.lang.String getLibraryName()
supr java.lang.Object
hfds OBJECT_EQUALS,OBJECT_HASHCODE,OBJECT_TOSTRING,functions,interfaceClass,invocationMapper,nativeLibrary,options
hcls FunctionInfo

CLSS public com.sun.jna.Memory
cons protected init()
cons public init(long)
fld protected long size
intf java.io.Closeable
meth protected static long malloc(long)
meth protected static void free(long)
meth protected void boundsCheck(long,long)
meth protected void dispose()
 anno 0 java.lang.Deprecated()
meth public boolean valid()
meth public byte getByte(long)
meth public char getChar(long)
meth public com.sun.jna.Memory align(int)
meth public com.sun.jna.Pointer getPointer(long)
meth public com.sun.jna.Pointer share(long)
meth public com.sun.jna.Pointer share(long,long)
meth public double getDouble(long)
meth public float getFloat(long)
meth public int getInt(long)
meth public java.lang.String dump()
meth public java.lang.String getString(long,java.lang.String)
meth public java.lang.String getWideString(long)
meth public java.lang.String toString()
meth public java.nio.ByteBuffer getByteBuffer(long,long)
meth public long getLong(long)
meth public long size()
meth public short getShort(long)
meth public static void disposeAll()
meth public static void purge()
meth public void clear()
meth public void close()
meth public void read(long,byte[],int,int)
meth public void read(long,char[],int,int)
meth public void read(long,com.sun.jna.Pointer[],int,int)
meth public void read(long,double[],int,int)
meth public void read(long,float[],int,int)
meth public void read(long,int[],int,int)
meth public void read(long,long[],int,int)
meth public void read(long,short[],int,int)
meth public void setByte(long,byte)
meth public void setChar(long,char)
meth public void setDouble(long,double)
meth public void setFloat(long,float)
meth public void setInt(long,int)
meth public void setLong(long,long)
meth public void setPointer(long,com.sun.jna.Pointer)
meth public void setShort(long,short)
meth public void setString(long,java.lang.String,java.lang.String)
meth public void setWideString(long,java.lang.String)
meth public void write(long,byte[],int,int)
meth public void write(long,char[],int,int)
meth public void write(long,com.sun.jna.Pointer[],int,int)
meth public void write(long,double[],int,int)
meth public void write(long,float[],int,int)
meth public void write(long,int[],int,int)
meth public void write(long,long[],int,int)
meth public void write(long,short[],int,int)
supr com.sun.jna.Pointer
hfds allocatedMemory,buffers,cleanable
hcls MemoryDisposer,SharedMemory

CLSS public com.sun.jna.MethodParameterContext
meth public java.lang.reflect.Method getMethod()
supr com.sun.jna.FunctionParameterContext
hfds method

CLSS public com.sun.jna.MethodResultContext
meth public java.lang.reflect.Method getMethod()
supr com.sun.jna.FunctionResultContext
hfds method

CLSS public final com.sun.jna.Native
fld public final static boolean DEBUG_JNA_LOAD
fld public final static boolean DEBUG_LOAD
fld public final static int BOOL_SIZE
fld public final static int LONG_DOUBLE_SIZE
fld public final static int LONG_SIZE
fld public final static int POINTER_SIZE
fld public final static int SIZE_T_SIZE
fld public final static int WCHAR_SIZE
fld public final static java.lang.String DEFAULT_ENCODING
fld public final static java.lang.String VERSION = "5.12.1"
fld public final static java.lang.String VERSION_NATIVE = "6.1.4"
fld public final static java.nio.charset.Charset DEFAULT_CHARSET
innr public abstract interface static ffi_callback
meth public static <%0 extends com.sun.jna.Library> {%%0} load(java.lang.Class<{%%0}>)
meth public static <%0 extends com.sun.jna.Library> {%%0} load(java.lang.Class<{%%0}>,java.util.Map<java.lang.String,?>)
meth public static <%0 extends com.sun.jna.Library> {%%0} load(java.lang.String,java.lang.Class<{%%0}>)
meth public static <%0 extends com.sun.jna.Library> {%%0} load(java.lang.String,java.lang.Class<{%%0}>,java.util.Map<java.lang.String,?>)
meth public static <%0 extends java.lang.Object> {%%0} loadLibrary(java.lang.Class<{%%0}>)
 anno 0 java.lang.Deprecated()
meth public static <%0 extends java.lang.Object> {%%0} loadLibrary(java.lang.Class<{%%0}>,java.util.Map<java.lang.String,?>)
 anno 0 java.lang.Deprecated()
meth public static <%0 extends java.lang.Object> {%%0} loadLibrary(java.lang.String,java.lang.Class<{%%0}>)
 anno 0 java.lang.Deprecated()
meth public static <%0 extends java.lang.Object> {%%0} loadLibrary(java.lang.String,java.lang.Class<{%%0}>,java.util.Map<java.lang.String,?>)
 anno 0 java.lang.Deprecated()
meth public static boolean isProtected()
meth public static boolean isSupportedNativeType(java.lang.Class<?>)
meth public static boolean registered(java.lang.Class<?>)
meth public static byte[] toByteArray(java.lang.String)
meth public static byte[] toByteArray(java.lang.String,java.lang.String)
meth public static byte[] toByteArray(java.lang.String,java.nio.charset.Charset)
meth public static char[] toCharArray(java.lang.String)
meth public static com.sun.jna.Callback$UncaughtExceptionHandler getCallbackExceptionHandler()
meth public static com.sun.jna.Library synchronizedLibrary(com.sun.jna.Library)
meth public static com.sun.jna.Pointer getComponentPointer(java.awt.Component)
meth public static com.sun.jna.Pointer getDirectBufferPointer(java.nio.Buffer)
meth public static com.sun.jna.Pointer getWindowPointer(java.awt.Window)
meth public static com.sun.jna.TypeMapper getTypeMapper(java.lang.Class<?>)
meth public static int getLastError()
meth public static int getNativeSize(java.lang.Class<?>)
meth public static int getNativeSize(java.lang.Class<?>,java.lang.Object)
meth public static int getStructureAlignment(java.lang.Class<?>)
meth public static java.io.File extractFromResourcePath(java.lang.String) throws java.io.IOException
meth public static java.io.File extractFromResourcePath(java.lang.String,java.lang.ClassLoader) throws java.io.IOException
meth public static java.lang.String getDefaultStringEncoding()
meth public static java.lang.String getStringEncoding(java.lang.Class<?>)
meth public static java.lang.String getWebStartLibraryPath(java.lang.String)
meth public static java.lang.String toString(byte[])
meth public static java.lang.String toString(byte[],java.lang.String)
meth public static java.lang.String toString(byte[],java.nio.charset.Charset)
meth public static java.lang.String toString(char[])
meth public static java.util.List<java.lang.String> toStringList(char[])
meth public static java.util.List<java.lang.String> toStringList(char[],int,int)
meth public static java.util.Map<java.lang.String,java.lang.Object> getLibraryOptions(java.lang.Class<?>)
meth public static long ffi_prep_cif(int,int,long,long)
meth public static long ffi_prep_closure(long,com.sun.jna.Native$ffi_callback)
meth public static long getComponentID(java.awt.Component)
meth public static long getWindowID(java.awt.Window)
meth public static long malloc(long)
meth public static void detach(boolean)
meth public static void ffi_call(long,long,long,long)
meth public static void ffi_free_closure(long)
meth public static void free(long)
meth public static void main(java.lang.String[])
meth public static void register(com.sun.jna.NativeLibrary)
meth public static void register(java.lang.Class<?>,com.sun.jna.NativeLibrary)
meth public static void register(java.lang.Class<?>,java.lang.String)
meth public static void register(java.lang.String)
meth public static void setCallbackExceptionHandler(com.sun.jna.Callback$UncaughtExceptionHandler)
meth public static void setCallbackThreadInitializer(com.sun.jna.Callback,com.sun.jna.CallbackThreadInitializer)
meth public static void setLastError(int)
meth public static void setProtected(boolean)
meth public static void unregister()
meth public static void unregister(java.lang.Class<?>)
supr java.lang.Object
hfds CB_HAS_INITIALIZER,CB_OPTION_DIRECT,CB_OPTION_IN_DLL,CVT_ARRAY_BOOLEAN,CVT_ARRAY_BYTE,CVT_ARRAY_CHAR,CVT_ARRAY_DOUBLE,CVT_ARRAY_FLOAT,CVT_ARRAY_INT,CVT_ARRAY_LONG,CVT_ARRAY_SHORT,CVT_BOOLEAN,CVT_BUFFER,CVT_BYTE,CVT_CALLBACK,CVT_DEFAULT,CVT_FLOAT,CVT_INTEGER_TYPE,CVT_JNIENV,CVT_NATIVE_MAPPED,CVT_NATIVE_MAPPED_STRING,CVT_NATIVE_MAPPED_WSTRING,CVT_OBJECT,CVT_POINTER,CVT_POINTER_TYPE,CVT_SHORT,CVT_STRING,CVT_STRUCTURE,CVT_STRUCTURE_BYVAL,CVT_TYPE_MAPPER,CVT_TYPE_MAPPER_STRING,CVT_TYPE_MAPPER_WSTRING,CVT_UNSUPPORTED,CVT_WSTRING,DEBUG_JNA_LOAD_LEVEL,DEFAULT_HANDLER,JNA_TMPLIB_PREFIX,LOG,MAX_ALIGNMENT,MAX_PADDING,TYPE_BOOL,TYPE_LONG,TYPE_LONG_DOUBLE,TYPE_SIZE_T,TYPE_VOIDP,TYPE_WCHAR_T,_OPTION_ENCLOSING_LIBRARY,callbackExceptionHandler,finalizer,jnidispatchPath,libraries,nativeThreadTerminationFlag,nativeThreads,registeredClasses,registeredLibraries,typeOptions
hcls AWT,Buffers

CLSS public abstract interface static com.sun.jna.Native$ffi_callback
 outer com.sun.jna.Native
meth public abstract void invoke(long,long,long)

CLSS public com.sun.jna.NativeLibrary
intf java.io.Closeable
meth public com.sun.jna.Function getFunction(java.lang.String)
meth public com.sun.jna.Function getFunction(java.lang.String,int)
meth public com.sun.jna.Function getFunction(java.lang.String,int,java.lang.String)
meth public com.sun.jna.Pointer getGlobalVariableAddress(java.lang.String)
meth public final static com.sun.jna.NativeLibrary getInstance(java.lang.String)
meth public final static com.sun.jna.NativeLibrary getInstance(java.lang.String,java.lang.ClassLoader)
meth public final static com.sun.jna.NativeLibrary getInstance(java.lang.String,java.util.Map<java.lang.String,?>)
meth public final static com.sun.jna.NativeLibrary getProcess()
meth public final static com.sun.jna.NativeLibrary getProcess(java.util.Map<java.lang.String,?>)
meth public final static void addSearchPath(java.lang.String,java.lang.String)
meth public java.io.File getFile()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Map<java.lang.String,?> getOptions()
meth public void close()
meth public void dispose()
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds DEBUG_LOAD_LEVEL,DEFAULT_OPEN_OPTIONS,LOG,addSuppressedMethod,callFlags,cleanable,encoding,functions,handle,libraries,libraryName,libraryPath,librarySearchPath,options,searchPaths
hcls NativeLibraryDisposer

CLSS public com.sun.jna.NativeLong
cons public init()
cons public init(long)
cons public init(long,boolean)
fld public final static int SIZE
supr com.sun.jna.IntegerType
hfds serialVersionUID

CLSS public abstract interface com.sun.jna.NativeMapped
meth public abstract java.lang.Class<?> nativeType()
meth public abstract java.lang.Object fromNative(java.lang.Object,com.sun.jna.FromNativeContext)
meth public abstract java.lang.Object toNative()

CLSS public com.sun.jna.NativeMappedConverter
cons public init(java.lang.Class<?>)
intf com.sun.jna.TypeConverter
meth public com.sun.jna.NativeMapped defaultValue()
meth public java.lang.Class<?> nativeType()
meth public java.lang.Object fromNative(java.lang.Object,com.sun.jna.FromNativeContext)
meth public java.lang.Object toNative(java.lang.Object,com.sun.jna.ToNativeContext)
meth public static com.sun.jna.NativeMappedConverter getInstance(java.lang.Class<?>)
supr java.lang.Object
hfds converters,instance,nativeType,type

CLSS public final com.sun.jna.Platform
fld public final static boolean HAS_AWT
fld public final static boolean HAS_BUFFERS
fld public final static boolean HAS_DLL_CALLBACKS
fld public final static boolean HAS_JAWT
fld public final static boolean RO_FIELDS
fld public final static int AIX = 7
fld public final static int ANDROID = 8
fld public final static int FREEBSD = 4
fld public final static int GNU = 9
fld public final static int KFREEBSD = 10
fld public final static int LINUX = 1
fld public final static int MAC = 0
fld public final static int NETBSD = 11
fld public final static int OPENBSD = 5
fld public final static int SOLARIS = 3
fld public final static int UNSPECIFIED = -1
fld public final static int WINDOWS = 2
fld public final static int WINDOWSCE = 6
fld public final static java.lang.String ARCH
fld public final static java.lang.String C_LIBRARY_NAME
fld public final static java.lang.String MATH_LIBRARY_NAME
fld public final static java.lang.String RESOURCE_PREFIX
meth public final static boolean hasRuntimeExec()
meth public final static boolean is64Bit()
meth public final static boolean isAIX()
meth public final static boolean isARM()
meth public final static boolean isAndroid()
meth public final static boolean isFreeBSD()
meth public final static boolean isGNU()
meth public final static boolean isIntel()
meth public final static boolean isLinux()
meth public final static boolean isLoongArch()
meth public final static boolean isMIPS()
meth public final static boolean isMac()
meth public final static boolean isNetBSD()
meth public final static boolean isOpenBSD()
meth public final static boolean isPPC()
meth public final static boolean isSPARC()
meth public final static boolean isSolaris()
meth public final static boolean isWindows()
meth public final static boolean isWindowsCE()
meth public final static boolean isX11()
meth public final static boolean iskFreeBSD()
meth public final static int getOSType()
supr java.lang.Object
hfds osType

CLSS public com.sun.jna.Pointer
cons public init(long)
fld protected long peer
fld public final static com.sun.jna.Pointer NULL
meth public boolean equals(java.lang.Object)
meth public byte getByte(long)
meth public byte[] getByteArray(long,int)
meth public char getChar(long)
meth public char[] getCharArray(long,int)
meth public com.sun.jna.NativeLong getNativeLong(long)
meth public com.sun.jna.Pointer getPointer(long)
meth public com.sun.jna.Pointer share(long)
meth public com.sun.jna.Pointer share(long,long)
meth public com.sun.jna.Pointer[] getPointerArray(long)
meth public com.sun.jna.Pointer[] getPointerArray(long,int)
meth public double getDouble(long)
meth public double[] getDoubleArray(long,int)
meth public final static com.sun.jna.Pointer createConstant(int)
meth public final static com.sun.jna.Pointer createConstant(long)
meth public float getFloat(long)
meth public float[] getFloatArray(long,int)
meth public int getInt(long)
meth public int hashCode()
meth public int[] getIntArray(long,int)
meth public java.lang.String dump(long,int)
meth public java.lang.String getString(long)
meth public java.lang.String getString(long,java.lang.String)
meth public java.lang.String getWideString(long)
meth public java.lang.String toString()
meth public java.lang.String[] getStringArray(long)
meth public java.lang.String[] getStringArray(long,int)
meth public java.lang.String[] getStringArray(long,int,java.lang.String)
meth public java.lang.String[] getStringArray(long,java.lang.String)
meth public java.lang.String[] getWideStringArray(long)
meth public java.lang.String[] getWideStringArray(long,int)
meth public java.nio.ByteBuffer getByteBuffer(long,long)
meth public long getLong(long)
meth public long indexOf(long,byte)
meth public long[] getLongArray(long,int)
meth public short getShort(long)
meth public short[] getShortArray(long,int)
meth public static long nativeValue(com.sun.jna.Pointer)
meth public static void nativeValue(com.sun.jna.Pointer,long)
meth public void clear(long)
meth public void read(long,byte[],int,int)
meth public void read(long,char[],int,int)
meth public void read(long,com.sun.jna.Pointer[],int,int)
meth public void read(long,double[],int,int)
meth public void read(long,float[],int,int)
meth public void read(long,int[],int,int)
meth public void read(long,long[],int,int)
meth public void read(long,short[],int,int)
meth public void setByte(long,byte)
meth public void setChar(long,char)
meth public void setDouble(long,double)
meth public void setFloat(long,float)
meth public void setInt(long,int)
meth public void setLong(long,long)
meth public void setMemory(long,long,byte)
meth public void setNativeLong(long,com.sun.jna.NativeLong)
meth public void setPointer(long,com.sun.jna.Pointer)
meth public void setShort(long,short)
meth public void setString(long,com.sun.jna.WString)
meth public void setString(long,java.lang.String)
meth public void setString(long,java.lang.String,java.lang.String)
meth public void setWideString(long,java.lang.String)
meth public void write(long,byte[],int,int)
meth public void write(long,char[],int,int)
meth public void write(long,com.sun.jna.Pointer[],int,int)
meth public void write(long,double[],int,int)
meth public void write(long,float[],int,int)
meth public void write(long,int[],int,int)
meth public void write(long,long[],int,int)
meth public void write(long,short[],int,int)
supr java.lang.Object
hcls Opaque

CLSS public abstract com.sun.jna.PointerType
cons protected init()
cons protected init(com.sun.jna.Pointer)
intf com.sun.jna.NativeMapped
meth public boolean equals(java.lang.Object)
meth public com.sun.jna.Pointer getPointer()
meth public int hashCode()
meth public java.lang.Class<?> nativeType()
meth public java.lang.Object fromNative(java.lang.Object,com.sun.jna.FromNativeContext)
meth public java.lang.Object toNative()
meth public java.lang.String toString()
meth public void setPointer(com.sun.jna.Pointer)
supr java.lang.Object
hfds pointer

CLSS public com.sun.jna.StringArray
cons public init(com.sun.jna.WString[])
cons public init(java.lang.String[])
cons public init(java.lang.String[],boolean)
cons public init(java.lang.String[],java.lang.String)
intf com.sun.jna.Function$PostCallRead
meth public java.lang.String toString()
meth public void read()
supr com.sun.jna.Memory
hfds encoding,natives,original

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
hfds LOG,PLACEHOLDER_MEMORY,actualAlignType,alignType,array,autoRead,autoWrite,busy,encoding,fieldOrder,layoutInfo,memory,nativeStrings,readCalled,reads,size,structAlignment,structFields,typeInfo,typeMapper
hcls AutoAllocated,FFIType,LayoutInfo,NativeStringTracking,StructureSet

CLSS public abstract interface static com.sun.jna.Structure$ByReference
 outer com.sun.jna.Structure

CLSS public abstract interface static com.sun.jna.Structure$ByValue
 outer com.sun.jna.Structure

CLSS public abstract interface static !annotation com.sun.jna.Structure$FieldOrder
 outer com.sun.jna.Structure
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.String[] value()

CLSS protected static com.sun.jna.Structure$StructField
 outer com.sun.jna.Structure
cons protected init()
fld public boolean isReadOnly
fld public boolean isVolatile
fld public com.sun.jna.FromNativeContext context
fld public com.sun.jna.FromNativeConverter readConverter
fld public com.sun.jna.ToNativeConverter writeConverter
fld public int offset
fld public int size
fld public java.lang.Class<?> type
fld public java.lang.String name
fld public java.lang.reflect.Field field
meth public java.lang.String toString()
supr java.lang.Object

CLSS public com.sun.jna.StructureReadContext
meth public com.sun.jna.Structure getStructure()
meth public java.lang.reflect.Field getField()
supr com.sun.jna.FromNativeContext
hfds field,structure

CLSS public com.sun.jna.StructureWriteContext
meth public com.sun.jna.Structure getStructure()
meth public java.lang.reflect.Field getField()
supr com.sun.jna.ToNativeContext
hfds field,struct

CLSS public com.sun.jna.ToNativeContext
supr java.lang.Object

CLSS public abstract interface com.sun.jna.ToNativeConverter
meth public abstract java.lang.Class<?> nativeType()
meth public abstract java.lang.Object toNative(java.lang.Object,com.sun.jna.ToNativeContext)

CLSS public abstract interface com.sun.jna.TypeConverter
intf com.sun.jna.FromNativeConverter
intf com.sun.jna.ToNativeConverter

CLSS public abstract interface com.sun.jna.TypeMapper
meth public abstract com.sun.jna.FromNativeConverter getFromNativeConverter(java.lang.Class<?>)
meth public abstract com.sun.jna.ToNativeConverter getToNativeConverter(java.lang.Class<?>)

CLSS public abstract com.sun.jna.Union
cons protected init()
cons protected init(com.sun.jna.Pointer)
cons protected init(com.sun.jna.Pointer,int)
cons protected init(com.sun.jna.Pointer,int,com.sun.jna.TypeMapper)
cons protected init(com.sun.jna.TypeMapper)
meth protected int getNativeAlignment(java.lang.Class<?>,java.lang.Object,boolean)
meth protected java.lang.Object readField(com.sun.jna.Structure$StructField)
meth protected java.util.List<java.lang.String> getFieldOrder()
meth protected void writeField(com.sun.jna.Structure$StructField)
meth public java.lang.Object getTypedValue(java.lang.Class<?>)
meth public java.lang.Object readField(java.lang.String)
meth public java.lang.Object setTypedValue(java.lang.Object)
meth public void setType(java.lang.Class<?>)
meth public void setType(java.lang.String)
meth public void writeField(java.lang.String)
meth public void writeField(java.lang.String,java.lang.Object)
supr com.sun.jna.Structure
hfds activeField

CLSS public final com.sun.jna.WString
cons public init(java.lang.String)
intf java.lang.CharSequence
intf java.lang.Comparable
meth public boolean equals(java.lang.Object)
meth public char charAt(int)
meth public int compareTo(java.lang.Object)
meth public int hashCode()
meth public int length()
meth public java.lang.CharSequence subSequence(int,int)
meth public java.lang.String toString()
supr java.lang.Object
hfds string

CLSS public com.sun.jna.WeakMemoryHolder
cons public init()
meth public void clean()
meth public void put(java.lang.Object,com.sun.jna.Memory)
supr java.lang.Object
hfds backingMap,referenceQueue

CLSS public abstract com.sun.jna.ptr.ByReference
cons protected init(int)
meth public java.lang.String toString()
supr com.sun.jna.PointerType

CLSS public com.sun.jna.ptr.ByteByReference
cons public init()
cons public init(byte)
meth public byte getValue()
meth public java.lang.String toString()
meth public void setValue(byte)
supr com.sun.jna.ptr.ByReference

CLSS public com.sun.jna.ptr.DoubleByReference
cons public init()
cons public init(double)
meth public double getValue()
meth public java.lang.String toString()
meth public void setValue(double)
supr com.sun.jna.ptr.ByReference

CLSS public com.sun.jna.ptr.FloatByReference
cons public init()
cons public init(float)
meth public float getValue()
meth public java.lang.String toString()
meth public void setValue(float)
supr com.sun.jna.ptr.ByReference

CLSS public com.sun.jna.ptr.IntByReference
cons public init()
cons public init(int)
meth public int getValue()
meth public java.lang.String toString()
meth public void setValue(int)
supr com.sun.jna.ptr.ByReference

CLSS public com.sun.jna.ptr.LongByReference
cons public init()
cons public init(long)
meth public java.lang.String toString()
meth public long getValue()
meth public void setValue(long)
supr com.sun.jna.ptr.ByReference

CLSS public com.sun.jna.ptr.NativeLongByReference
cons public init()
cons public init(com.sun.jna.NativeLong)
meth public com.sun.jna.NativeLong getValue()
meth public java.lang.String toString()
meth public void setValue(com.sun.jna.NativeLong)
supr com.sun.jna.ptr.ByReference

CLSS public com.sun.jna.ptr.PointerByReference
cons public init()
cons public init(com.sun.jna.Pointer)
meth public com.sun.jna.Pointer getValue()
meth public void setValue(com.sun.jna.Pointer)
supr com.sun.jna.ptr.ByReference

CLSS public com.sun.jna.ptr.ShortByReference
cons public init()
cons public init(short)
meth public java.lang.String toString()
meth public short getValue()
meth public void setValue(short)
supr com.sun.jna.ptr.ByReference

CLSS public abstract interface com.sun.jna.win32.DLLCallback
fld public final static int DLL_FPTRS = 16
intf com.sun.jna.Callback

CLSS public abstract interface com.sun.jna.win32.StdCall
intf com.sun.jna.AltCallingConvention

CLSS public com.sun.jna.win32.StdCallFunctionMapper
cons public init()
intf com.sun.jna.FunctionMapper
meth protected int getArgumentNativeStackSize(java.lang.Class<?>)
meth public java.lang.String getFunctionName(com.sun.jna.NativeLibrary,java.lang.reflect.Method)
supr java.lang.Object

CLSS public abstract interface com.sun.jna.win32.StdCallLibrary
fld public final static com.sun.jna.FunctionMapper FUNCTION_MAPPER
fld public final static int STDCALL_CONVENTION = 63
innr public abstract interface static StdCallCallback
intf com.sun.jna.Library
intf com.sun.jna.win32.StdCall

CLSS public abstract interface static com.sun.jna.win32.StdCallLibrary$StdCallCallback
 outer com.sun.jna.win32.StdCallLibrary
intf com.sun.jna.Callback
intf com.sun.jna.win32.StdCall

CLSS public com.sun.jna.win32.W32APIFunctionMapper
cons protected init(boolean)
fld public final static com.sun.jna.FunctionMapper ASCII
fld public final static com.sun.jna.FunctionMapper UNICODE
intf com.sun.jna.FunctionMapper
meth public java.lang.String getFunctionName(com.sun.jna.NativeLibrary,java.lang.reflect.Method)
supr java.lang.Object
hfds suffix

CLSS public abstract interface com.sun.jna.win32.W32APIOptions
fld public final static java.util.Map<java.lang.String,java.lang.Object> ASCII_OPTIONS
fld public final static java.util.Map<java.lang.String,java.lang.Object> DEFAULT_OPTIONS
fld public final static java.util.Map<java.lang.String,java.lang.Object> UNICODE_OPTIONS
intf com.sun.jna.win32.StdCallLibrary

CLSS public com.sun.jna.win32.W32APITypeMapper
cons protected init(boolean)
fld public final static com.sun.jna.TypeMapper ASCII
fld public final static com.sun.jna.TypeMapper DEFAULT
fld public final static com.sun.jna.TypeMapper UNICODE
supr com.sun.jna.DefaultTypeMapper

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface java.lang.CharSequence
meth public abstract char charAt(int)
meth public abstract int length()
meth public abstract java.lang.CharSequence subSequence(int,int)
meth public abstract java.lang.String toString()
meth public java.util.stream.IntStream chars()
meth public java.util.stream.IntStream codePoints()

CLSS public abstract interface java.lang.Comparable<%0 extends java.lang.Object>
meth public abstract int compareTo({java.lang.Comparable%0})

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable

CLSS public abstract java.lang.Number
cons public init()
intf java.io.Serializable
meth public abstract double doubleValue()
meth public abstract float floatValue()
meth public abstract int intValue()
meth public abstract long longValue()
meth public byte byteValue()
meth public short shortValue()
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

CLSS public abstract java.lang.ref.Reference<%0 extends java.lang.Object>
meth public boolean enqueue()
meth public boolean isEnqueued()
meth public void clear()
meth public {java.lang.ref.Reference%0} get()
supr java.lang.Object

CLSS public java.lang.ref.WeakReference<%0 extends java.lang.Object>
cons public init({java.lang.ref.WeakReference%0})
cons public init({java.lang.ref.WeakReference%0},java.lang.ref.ReferenceQueue<? super {java.lang.ref.WeakReference%0}>)
supr java.lang.ref.Reference<{java.lang.ref.WeakReference%0}>

CLSS public abstract interface java.lang.reflect.InvocationHandler
meth public abstract java.lang.Object invoke(java.lang.Object,java.lang.reflect.Method,java.lang.Object[]) throws java.lang.Throwable

