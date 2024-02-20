#Signature file v4.1
#Version 1.75

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

CLSS public abstract interface java.util.Comparator<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.function.Function<? super {java.util.Comparator%0},? extends {%%0}>)
meth public <%0 extends java.lang.Object> java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.function.Function<? super {java.util.Comparator%0},? extends {%%0}>,java.util.Comparator<? super {%%0}>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract int compare({java.util.Comparator%0},{java.util.Comparator%0})
meth public java.util.Comparator<{java.util.Comparator%0}> reversed()
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparing(java.util.Comparator<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingDouble(java.util.function.ToDoubleFunction<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingInt(java.util.function.ToIntFunction<? super {java.util.Comparator%0}>)
meth public java.util.Comparator<{java.util.Comparator%0}> thenComparingLong(java.util.function.ToLongFunction<? super {java.util.Comparator%0}>)
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{%%0}> naturalOrder()
meth public static <%0 extends java.lang.Comparable<? super {%%0}>> java.util.Comparator<{%%0}> reverseOrder()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Comparable<? super {%%1}>> java.util.Comparator<{%%0}> comparing(java.util.function.Function<? super {%%0},? extends {%%1}>)
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Comparator<{%%0}> comparing(java.util.function.Function<? super {%%0},? extends {%%1}>,java.util.Comparator<? super {%%1}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingDouble(java.util.function.ToDoubleFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingInt(java.util.function.ToIntFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> comparingLong(java.util.function.ToLongFunction<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> nullsFirst(java.util.Comparator<? super {%%0}>)
meth public static <%0 extends java.lang.Object> java.util.Comparator<{%%0}> nullsLast(java.util.Comparator<? super {%%0}>)

CLSS public org.netbeans.modules.classfile.Access
fld public final static int ABSTRACT = 1024
fld public final static int ANNOTATION = 8192
fld public final static int BRIDGE = 64
fld public final static int ENUM = 16384
fld public final static int FINAL = 16
fld public final static int INTERFACE = 512
fld public final static int MANDATED = 32768
fld public final static int MODULE = 32768
fld public final static int NATIVE = 256
fld public final static int OPEN = 32
fld public final static int PRIVATE = 2
fld public final static int PROTECTED = 4
fld public final static int PUBLIC = 1
fld public final static int STATIC = 8
fld public final static int STATIC_PHASE = 64
fld public final static int STRICT = 2048
fld public final static int SUPER = 32
fld public final static int SYNCHRONIZED = 32
fld public final static int SYNTHETIC = 4096
fld public final static int TRANSIENT = 128
fld public final static int TRANSITIVE = 32
fld public final static int VARARGS = 128
fld public final static int VOLATILE = 64
meth public final static boolean isPackagePrivate(int)
meth public final static boolean isPrivate(int)
meth public final static boolean isProtected(int)
meth public final static boolean isPublic(int)
meth public static boolean isStatic(int)
meth public static java.lang.String toString(int)
supr java.lang.Object

CLSS public org.netbeans.modules.classfile.Annotation
meth public boolean isRuntimeVisible()
meth public final org.netbeans.modules.classfile.AnnotationComponent getComponent(java.lang.String)
meth public final org.netbeans.modules.classfile.AnnotationComponent[] getComponents()
meth public final org.netbeans.modules.classfile.ClassName getType()
meth public java.lang.String toString()
supr java.lang.Object
hfds components,runtimeVisible,type

CLSS public org.netbeans.modules.classfile.AnnotationComponent
meth public final java.lang.String getName()
meth public final org.netbeans.modules.classfile.ElementValue getValue()
meth public java.lang.String toString()
supr java.lang.Object
hfds name,value

CLSS public final org.netbeans.modules.classfile.ArrayElementValue
meth public java.lang.String toString()
meth public org.netbeans.modules.classfile.ElementValue[] getValues()
supr org.netbeans.modules.classfile.ElementValue
hfds values

CLSS public final org.netbeans.modules.classfile.AttributeMap
meth public boolean containsAttribute(java.lang.String)
meth public boolean isEmpty()
meth public int size()
meth public java.util.Set<java.lang.String> keySet()
supr java.lang.Object
hfds map

CLSS public final org.netbeans.modules.classfile.BootstrapMethod
meth public int getMethodRef()
meth public int[] getArguments()
meth public java.lang.String toString()
supr java.lang.Object
hfds arguments,methodRef

CLSS public abstract interface org.netbeans.modules.classfile.ByteCodes
fld public final static int bc_aaload = 50
fld public final static int bc_aastore = 83
fld public final static int bc_aconst_null = 1
fld public final static int bc_aload = 25
fld public final static int bc_aload_0 = 42
fld public final static int bc_aload_1 = 43
fld public final static int bc_aload_2 = 44
fld public final static int bc_aload_3 = 45
fld public final static int bc_anewarray = 189
fld public final static int bc_areturn = 176
fld public final static int bc_arraylength = 190
fld public final static int bc_astore = 58
fld public final static int bc_astore_0 = 75
fld public final static int bc_astore_1 = 76
fld public final static int bc_astore_2 = 77
fld public final static int bc_astore_3 = 78
fld public final static int bc_athrow = 191
fld public final static int bc_baload = 51
fld public final static int bc_bastore = 84
fld public final static int bc_bipush = 16
fld public final static int bc_caload = 52
fld public final static int bc_castore = 85
fld public final static int bc_checkcast = 192
fld public final static int bc_d2f = 144
fld public final static int bc_d2i = 142
fld public final static int bc_d2l = 143
fld public final static int bc_dadd = 99
fld public final static int bc_daload = 49
fld public final static int bc_dastore = 82
fld public final static int bc_dcmpg = 152
fld public final static int bc_dcmpl = 151
fld public final static int bc_dconst_0 = 14
fld public final static int bc_dconst_1 = 15
fld public final static int bc_ddiv = 111
fld public final static int bc_dload = 24
fld public final static int bc_dload_0 = 38
fld public final static int bc_dload_1 = 39
fld public final static int bc_dload_2 = 40
fld public final static int bc_dload_3 = 41
fld public final static int bc_dmul = 107
fld public final static int bc_dneg = 119
fld public final static int bc_drem = 115
fld public final static int bc_dreturn = 175
fld public final static int bc_dstore = 57
fld public final static int bc_dstore_0 = 71
fld public final static int bc_dstore_1 = 72
fld public final static int bc_dstore_2 = 73
fld public final static int bc_dstore_3 = 74
fld public final static int bc_dsub = 103
fld public final static int bc_dup = 89
fld public final static int bc_dup2 = 92
fld public final static int bc_dup2_x1 = 93
fld public final static int bc_dup2_x2 = 94
fld public final static int bc_dup_x1 = 90
fld public final static int bc_dup_x2 = 91
fld public final static int bc_f2d = 141
fld public final static int bc_f2i = 139
fld public final static int bc_f2l = 140
fld public final static int bc_fadd = 98
fld public final static int bc_faload = 48
fld public final static int bc_fastore = 81
fld public final static int bc_fcmpg = 150
fld public final static int bc_fcmpl = 149
fld public final static int bc_fconst_0 = 11
fld public final static int bc_fconst_1 = 12
fld public final static int bc_fconst_2 = 13
fld public final static int bc_fdiv = 110
fld public final static int bc_fload = 23
fld public final static int bc_fload_0 = 34
fld public final static int bc_fload_1 = 35
fld public final static int bc_fload_2 = 36
fld public final static int bc_fload_3 = 37
fld public final static int bc_fmul = 106
fld public final static int bc_fneg = 118
fld public final static int bc_frem = 114
fld public final static int bc_freturn = 174
fld public final static int bc_fstore = 56
fld public final static int bc_fstore_0 = 67
fld public final static int bc_fstore_1 = 68
fld public final static int bc_fstore_2 = 69
fld public final static int bc_fstore_3 = 70
fld public final static int bc_fsub = 102
fld public final static int bc_getfield = 180
fld public final static int bc_getstatic = 178
fld public final static int bc_goto = 167
fld public final static int bc_goto_w = 200
fld public final static int bc_i2b = 145
fld public final static int bc_i2c = 146
fld public final static int bc_i2d = 135
fld public final static int bc_i2f = 134
fld public final static int bc_i2l = 133
fld public final static int bc_i2s = 147
fld public final static int bc_iadd = 96
fld public final static int bc_iaload = 46
fld public final static int bc_iand = 126
fld public final static int bc_iastore = 79
fld public final static int bc_iconst_0 = 3
fld public final static int bc_iconst_1 = 4
fld public final static int bc_iconst_2 = 5
fld public final static int bc_iconst_3 = 6
fld public final static int bc_iconst_4 = 7
fld public final static int bc_iconst_5 = 8
fld public final static int bc_iconst_m1 = 2
fld public final static int bc_idiv = 108
fld public final static int bc_if_acmpeq = 165
fld public final static int bc_if_acmpne = 166
fld public final static int bc_if_icmpeq = 159
fld public final static int bc_if_icmpge = 162
fld public final static int bc_if_icmpgt = 163
fld public final static int bc_if_icmple = 164
fld public final static int bc_if_icmplt = 161
fld public final static int bc_if_icmpne = 160
fld public final static int bc_ifeq = 153
fld public final static int bc_ifge = 156
fld public final static int bc_ifgt = 157
fld public final static int bc_ifle = 158
fld public final static int bc_iflt = 155
fld public final static int bc_ifne = 154
fld public final static int bc_ifnonnull = 199
fld public final static int bc_ifnull = 198
fld public final static int bc_iinc = 132
fld public final static int bc_iload = 21
fld public final static int bc_iload_0 = 26
fld public final static int bc_iload_1 = 27
fld public final static int bc_iload_2 = 28
fld public final static int bc_iload_3 = 29
fld public final static int bc_imul = 104
fld public final static int bc_ineg = 116
fld public final static int bc_instanceof = 193
fld public final static int bc_invokeinterface = 185
fld public final static int bc_invokespecial = 183
fld public final static int bc_invokestatic = 184
fld public final static int bc_invokevirtual = 182
fld public final static int bc_ior = 128
fld public final static int bc_irem = 112
fld public final static int bc_ireturn = 172
fld public final static int bc_ishl = 120
fld public final static int bc_ishr = 122
fld public final static int bc_istore = 54
fld public final static int bc_istore_0 = 59
fld public final static int bc_istore_1 = 60
fld public final static int bc_istore_2 = 61
fld public final static int bc_istore_3 = 62
fld public final static int bc_isub = 100
fld public final static int bc_iushr = 124
fld public final static int bc_ixor = 130
fld public final static int bc_jsr = 168
fld public final static int bc_jsr_w = 201
fld public final static int bc_l2d = 138
fld public final static int bc_l2f = 137
fld public final static int bc_l2i = 136
fld public final static int bc_ladd = 97
fld public final static int bc_laload = 47
fld public final static int bc_land = 127
fld public final static int bc_lastore = 80
fld public final static int bc_lcmp = 148
fld public final static int bc_lconst_0 = 9
fld public final static int bc_lconst_1 = 10
fld public final static int bc_ldc = 18
fld public final static int bc_ldc2_w = 20
fld public final static int bc_ldc_w = 19
fld public final static int bc_ldiv = 109
fld public final static int bc_lload = 22
fld public final static int bc_lload_0 = 30
fld public final static int bc_lload_1 = 31
fld public final static int bc_lload_2 = 32
fld public final static int bc_lload_3 = 33
fld public final static int bc_lmul = 105
fld public final static int bc_lneg = 117
fld public final static int bc_lookupswitch = 171
fld public final static int bc_lor = 129
fld public final static int bc_lrem = 113
fld public final static int bc_lreturn = 173
fld public final static int bc_lshl = 121
fld public final static int bc_lshr = 123
fld public final static int bc_lstore = 55
fld public final static int bc_lstore_0 = 63
fld public final static int bc_lstore_1 = 64
fld public final static int bc_lstore_2 = 65
fld public final static int bc_lstore_3 = 66
fld public final static int bc_lsub = 101
fld public final static int bc_lushr = 125
fld public final static int bc_lxor = 131
fld public final static int bc_monitorenter = 194
fld public final static int bc_monitorexit = 195
fld public final static int bc_multianewarray = 197
fld public final static int bc_new = 187
fld public final static int bc_newarray = 188
fld public final static int bc_nop = 0
fld public final static int bc_pop = 87
fld public final static int bc_pop2 = 88
fld public final static int bc_putfield = 181
fld public final static int bc_putstatic = 179
fld public final static int bc_ret = 169
fld public final static int bc_return = 177
fld public final static int bc_saload = 53
fld public final static int bc_sastore = 86
fld public final static int bc_sipush = 17
fld public final static int bc_swap = 95
fld public final static int bc_tableswitch = 170
fld public final static int bc_wide = 196
fld public final static int bc_xxxunusedxxx = 186

CLSS public final org.netbeans.modules.classfile.CPClassInfo
meth public final int getTag()
meth public java.lang.Object getValue()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public org.netbeans.modules.classfile.ClassName getClassName()
supr org.netbeans.modules.classfile.CPEntry

CLSS public org.netbeans.modules.classfile.CPConstantDynamicInfo
meth public int getBootstrapMethod()
meth public int getNameAndType()
meth public int getTag()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.CPEntry
hfds iBootstrapMethod,iNameAndType

CLSS public final org.netbeans.modules.classfile.CPDoubleInfo
meth public final int getTag()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.CPEntry

CLSS public abstract org.netbeans.modules.classfile.CPEntry
meth public abstract int getTag()
meth public java.lang.Object getValue()
supr java.lang.Object
hfds pool,value

CLSS public final org.netbeans.modules.classfile.CPFieldInfo
meth public final int getClassID()
meth public final int getFieldID()
meth public final int getTag()
meth public final java.lang.String getFieldName()
meth public final java.lang.String getSignature()
meth public final org.netbeans.modules.classfile.ClassName getClassName()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.CPNameAndTypeInfo

CLSS public final org.netbeans.modules.classfile.CPFloatInfo
meth public final int getTag()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.CPEntry

CLSS public final org.netbeans.modules.classfile.CPIntegerInfo
meth public final int getTag()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.CPEntry

CLSS public final org.netbeans.modules.classfile.CPInterfaceMethodInfo
meth public final int getTag()
supr org.netbeans.modules.classfile.CPMethodInfo

CLSS public org.netbeans.modules.classfile.CPInvokeDynamicInfo
meth public int getBootstrapMethod()
meth public int getNameAndType()
meth public int getTag()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.CPEntry
hfds iBootstrapMethod,iNameAndType

CLSS public final org.netbeans.modules.classfile.CPLongInfo
meth public final int getTag()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.CPEntry

CLSS public org.netbeans.modules.classfile.CPMethodHandleInfo
innr public final static !enum ReferenceKind
meth public int getReference()
meth public int getTag()
meth public java.lang.String toString()
meth public org.netbeans.modules.classfile.CPMethodHandleInfo$ReferenceKind getReferenceKind()
supr org.netbeans.modules.classfile.CPEntry
hfds iReference,referenceKind

CLSS public final static !enum org.netbeans.modules.classfile.CPMethodHandleInfo$ReferenceKind
 outer org.netbeans.modules.classfile.CPMethodHandleInfo
fld public final static org.netbeans.modules.classfile.CPMethodHandleInfo$ReferenceKind getField
fld public final static org.netbeans.modules.classfile.CPMethodHandleInfo$ReferenceKind getStatic
fld public final static org.netbeans.modules.classfile.CPMethodHandleInfo$ReferenceKind invokeInterface
fld public final static org.netbeans.modules.classfile.CPMethodHandleInfo$ReferenceKind invokeSpecial
fld public final static org.netbeans.modules.classfile.CPMethodHandleInfo$ReferenceKind invokeStatic
fld public final static org.netbeans.modules.classfile.CPMethodHandleInfo$ReferenceKind invokeVirtual
fld public final static org.netbeans.modules.classfile.CPMethodHandleInfo$ReferenceKind newInvokeSpecial
fld public final static org.netbeans.modules.classfile.CPMethodHandleInfo$ReferenceKind putField
fld public final static org.netbeans.modules.classfile.CPMethodHandleInfo$ReferenceKind putStatic
meth public static org.netbeans.modules.classfile.CPMethodHandleInfo$ReferenceKind valueOf(java.lang.String)
meth public static org.netbeans.modules.classfile.CPMethodHandleInfo$ReferenceKind[] values()
supr java.lang.Enum<org.netbeans.modules.classfile.CPMethodHandleInfo$ReferenceKind>
hfds kindInt

CLSS public org.netbeans.modules.classfile.CPMethodInfo
meth public final int getClassID()
meth public final int getFieldID()
meth public final java.lang.String getFieldName()
meth public final java.lang.String getFullMethodName()
meth public final java.lang.String getMethodName()
meth public final java.lang.String getSignature()
meth public final org.netbeans.modules.classfile.ClassName getClassName()
meth public int getTag()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.CPNameAndTypeInfo

CLSS public org.netbeans.modules.classfile.CPMethodTypeInfo
meth public int getDescriptor()
meth public int getTag()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.CPEntry
hfds iDescriptor

CLSS public final org.netbeans.modules.classfile.CPModuleInfo
meth public final int getTag()
meth public java.lang.Object getValue()
meth public java.lang.String getName()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.CPEntry

CLSS public org.netbeans.modules.classfile.CPNameAndTypeInfo
cons protected init(org.netbeans.modules.classfile.ConstantPool)
meth public final java.lang.String getDescriptor()
meth public final java.lang.String getName()
meth public int getTag()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.CPEntry
hfds iDesc,iName

CLSS public final org.netbeans.modules.classfile.CPPackageInfo
meth public final int getTag()
meth public java.lang.Object getValue()
meth public java.lang.String getName()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.CPEntry

CLSS public final org.netbeans.modules.classfile.CPStringInfo
meth public final int getTag()
meth public java.lang.Object getValue()
meth public java.lang.String getName()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.CPEntry

CLSS public final org.netbeans.modules.classfile.CPUTF8Info
meth public final int getTag()
meth public final java.lang.Object getValue()
meth public final java.lang.String getName()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.CPEntry
hfds name,utf

CLSS public final org.netbeans.modules.classfile.ClassElementValue
meth public final org.netbeans.modules.classfile.ClassName getClassName()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.ElementValue
hfds name

CLSS public org.netbeans.modules.classfile.ClassFile
cons public init(java.io.File,boolean) throws java.io.IOException
cons public init(java.io.InputStream) throws java.io.IOException
cons public init(java.io.InputStream,boolean) throws java.io.IOException
cons public init(java.lang.String) throws java.io.IOException
cons public init(java.lang.String,boolean) throws java.io.IOException
meth public final boolean isAnnotation()
meth public final boolean isAnnotationPresent(org.netbeans.modules.classfile.ClassName)
meth public final boolean isDeprecated()
meth public final boolean isEnum()
meth public final boolean isModule()
meth public final boolean isSynthetic()
meth public final int getAccess()
meth public final int getMethodCount()
meth public final int getVariableCount()
meth public final java.lang.String getSourceFileName()
meth public final java.util.Collection<org.netbeans.modules.classfile.Annotation> getAnnotations()
meth public final java.util.Collection<org.netbeans.modules.classfile.ClassName> getInterfaces()
meth public final java.util.Collection<org.netbeans.modules.classfile.InnerClass> getInnerClasses()
meth public final java.util.Collection<org.netbeans.modules.classfile.Method> getMethods()
meth public final java.util.Collection<org.netbeans.modules.classfile.Variable> getVariables()
meth public final java.util.List<java.lang.String> getModulePackages()
meth public final java.util.List<org.netbeans.modules.classfile.BootstrapMethod> getBootstrapMethods()
meth public final java.util.Set<org.netbeans.modules.classfile.ClassName> getAllClassNames()
meth public final org.netbeans.modules.classfile.Annotation getAnnotation(org.netbeans.modules.classfile.ClassName)
meth public final org.netbeans.modules.classfile.AttributeMap getAttributes()
meth public final org.netbeans.modules.classfile.ClassName getModuleMainClass()
meth public final org.netbeans.modules.classfile.ClassName getName()
meth public final org.netbeans.modules.classfile.ClassName getSuperClass()
meth public final org.netbeans.modules.classfile.ConstantPool getConstantPool()
meth public final org.netbeans.modules.classfile.Method getMethod(java.lang.String,java.lang.String)
meth public final org.netbeans.modules.classfile.Module getModule()
meth public final org.netbeans.modules.classfile.ModuleTarget getModuleTarget()
meth public final org.netbeans.modules.classfile.Variable getVariable(java.lang.String)
meth public int getMajorVersion()
meth public int getMinorVersion()
meth public java.lang.String getTypeSignature()
meth public java.lang.String toString()
meth public org.netbeans.modules.classfile.EnclosingMethod getEnclosingMethod()
supr java.lang.Object
hfds BUFFER_SIZE,LOG,annotations,attributes,badNonJavaClassNames,bootstrapMethods,classAccess,classInfo,constantPool,enclosingMethod,includeCode,innerClasses,interfaces,majorVersion,methods,minorVersion,module,moduleMainClz,modulePackages,moduleTarget,sourceFileName,superClassInfo,typeSignature,variables

CLSS public final org.netbeans.modules.classfile.ClassName
intf java.io.Serializable
intf java.lang.Comparable<org.netbeans.modules.classfile.ClassName>
intf java.util.Comparator<org.netbeans.modules.classfile.ClassName>
meth public boolean equals(java.lang.Object)
meth public int compare(org.netbeans.modules.classfile.ClassName,org.netbeans.modules.classfile.ClassName)
meth public int compareTo(org.netbeans.modules.classfile.ClassName)
meth public int hashCode()
meth public java.lang.String getExternalName()
meth public java.lang.String getExternalName(boolean)
meth public java.lang.String getInternalName()
meth public java.lang.String getPackage()
meth public java.lang.String getSimpleName()
meth public java.lang.String getType()
meth public java.lang.String toString()
meth public static org.netbeans.modules.classfile.ClassName getClassName(java.lang.String)
supr java.lang.Object
hfds cache,externalName,internalName,packageName,serialVersionUID,simpleName,type

CLSS public final org.netbeans.modules.classfile.Code
meth public final byte[] getByteCodes()
meth public final int getMaxLocals()
meth public final int getMaxStack()
meth public final int[] getLineNumberTable()
meth public final org.netbeans.modules.classfile.ExceptionTableEntry[] getExceptionTable()
meth public final org.netbeans.modules.classfile.LocalVariableTableEntry[] getLocalVariableTable()
meth public final org.netbeans.modules.classfile.LocalVariableTypeTableEntry[] getLocalVariableTypeTable()
meth public final org.netbeans.modules.classfile.StackMapFrame[] getStackMapTable()
meth public java.lang.String toString()
supr java.lang.Object
hfds byteCodes,debug,exceptionTable,lineNumberTable,localVariableTable,localVariableTypeTable,maxLocals,maxStack,stackMapTable

CLSS public final org.netbeans.modules.classfile.ConstantPool
meth public final <%0 extends org.netbeans.modules.classfile.CPEntry> java.util.Collection<? extends {%%0}> getAllConstants(java.lang.Class<{%%0}>)
meth public final java.util.Set<org.netbeans.modules.classfile.ClassName> getAllClassNames()
meth public final org.netbeans.modules.classfile.CPClassInfo getClass(int)
meth public final org.netbeans.modules.classfile.CPEntry get(int)
supr java.lang.Object
hfds CONSTANT_Class,CONSTANT_ConstantDynamic,CONSTANT_Double,CONSTANT_FieldRef,CONSTANT_Float,CONSTANT_Integer,CONSTANT_InterfaceMethodRef,CONSTANT_InvokeDynamic,CONSTANT_Long,CONSTANT_MethodHandle,CONSTANT_MethodRef,CONSTANT_MethodType,CONSTANT_Module,CONSTANT_NameAndType,CONSTANT_POOL_START,CONSTANT_Package,CONSTANT_String,CONSTANT_Utf8,constantPoolCount,cpEntries

CLSS public final org.netbeans.modules.classfile.ConstantPoolReader
cons public init(java.io.InputStream)
intf java.io.DataInput
meth public boolean readBoolean() throws java.io.IOException
meth public byte readByte() throws java.io.IOException
meth public char readChar() throws java.io.IOException
meth public double readDouble() throws java.io.IOException
meth public float readFloat() throws java.io.IOException
meth public int readInt() throws java.io.IOException
meth public int readUnsignedByte() throws java.io.IOException
meth public int readUnsignedShort() throws java.io.IOException
meth public int skipBytes(int) throws java.io.IOException
meth public java.lang.String readLine() throws java.io.IOException
meth public java.lang.String readUTF() throws java.io.IOException
meth public long readLong() throws java.io.IOException
meth public short readShort() throws java.io.IOException
meth public void readFully(byte[]) throws java.io.IOException
meth public void readFully(byte[],int,int) throws java.io.IOException
supr java.io.FilterInputStream
hfds bytearr,lineBuffer,str

CLSS public abstract org.netbeans.modules.classfile.ElementValue
supr java.lang.Object

CLSS public final org.netbeans.modules.classfile.EnclosingMethod
meth public boolean hasMethod()
meth public java.lang.String toString()
meth public org.netbeans.modules.classfile.CPClassInfo getClassInfo()
meth public org.netbeans.modules.classfile.CPNameAndTypeInfo getMethodInfo()
meth public org.netbeans.modules.classfile.ClassName getClassName()
supr java.lang.Object
hfds classInfo,methodInfo

CLSS public final org.netbeans.modules.classfile.EnumElementValue
meth public final java.lang.String getEnumName()
meth public final java.lang.String getEnumType()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.ElementValue
hfds enumName,enumType

CLSS public final org.netbeans.modules.classfile.ExceptionTableEntry
meth public final int getEndPC()
meth public final int getHandlerPC()
meth public final int getStartPC()
meth public final org.netbeans.modules.classfile.CPClassInfo getCatchType()
supr java.lang.Object
hfds catchType,endPC,handlerPC,startPC

CLSS public abstract org.netbeans.modules.classfile.Field
meth public abstract java.lang.String getDeclaration()
meth public final boolean isAnnotationPresent(org.netbeans.modules.classfile.ClassName)
meth public final boolean isDeprecated()
meth public final boolean isPackagePrivate()
meth public final boolean isPrivate()
meth public final boolean isProtected()
meth public final boolean isPublic()
meth public final boolean isStatic()
meth public final boolean isSynthetic()
meth public final int getAccess()
meth public final java.lang.String getDescriptor()
meth public final java.lang.String getName()
meth public final java.util.Collection<org.netbeans.modules.classfile.Annotation> getAnnotations()
meth public final org.netbeans.modules.classfile.Annotation getAnnotation(org.netbeans.modules.classfile.ClassName)
meth public final org.netbeans.modules.classfile.AttributeMap getAttributes()
meth public final org.netbeans.modules.classfile.ClassFile getClassFile()
meth public java.lang.String getTypeSignature()
meth public java.lang.String toString()
supr java.lang.Object
hfds _name,_type,access,annotations,attributes,classFile,iName,iType,typeSignature

CLSS public final org.netbeans.modules.classfile.InnerClass
meth public final int getAccess()
meth public final java.lang.String getSimpleName()
meth public final org.netbeans.modules.classfile.ClassName getName()
meth public final org.netbeans.modules.classfile.ClassName getOuterClassName()
meth public java.lang.String toString()
supr java.lang.Object
hfds access,name,outerClassName,simpleName

CLSS public final org.netbeans.modules.classfile.InvalidClassFileAttributeException
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public final org.netbeans.modules.classfile.InvalidClassFormatException
supr java.io.IOException
hfds serialVersionUID

CLSS public final org.netbeans.modules.classfile.LocalVariableTableEntry
meth public final int getIndex()
meth public final int getLength()
meth public final int getStartPC()
meth public final java.lang.String getDescription()
meth public final java.lang.String getName()
supr java.lang.Object
hfds description,index,length,name,startPC

CLSS public final org.netbeans.modules.classfile.LocalVariableTypeTableEntry
meth public final int getIndex()
meth public final int getLength()
meth public final int getStartPC()
meth public final java.lang.String getName()
meth public final java.lang.String getSignature()
supr java.lang.Object
hfds index,length,name,signature,startPC

CLSS public final org.netbeans.modules.classfile.Method
meth public final boolean isAbstract()
meth public final boolean isBridge()
meth public final boolean isNative()
meth public final boolean isSynchronized()
meth public final boolean isVarArgs()
meth public final java.lang.String getDeclaration()
meth public final java.lang.String getReturnSignature()
meth public final java.lang.String getReturnType()
meth public final java.util.List<org.netbeans.modules.classfile.Parameter> getParameters()
meth public final org.netbeans.modules.classfile.CPClassInfo[] getExceptionClasses()
meth public final org.netbeans.modules.classfile.Code getCode()
meth public java.lang.String toString()
meth public org.netbeans.modules.classfile.ElementValue getAnnotationDefault()
supr org.netbeans.modules.classfile.Field
hfds annotationDefault,code,exceptions,notloadedAnnotationDefault,parameters

CLSS public final org.netbeans.modules.classfile.Module
innr public final static ExportsEntry
innr public final static OpensEntry
innr public final static ProvidesEntry
innr public final static RequiresEntry
meth public int getFlags()
meth public java.lang.String getName()
meth public java.lang.String getVersion()
meth public java.util.List<org.netbeans.modules.classfile.ClassName> getUses()
meth public java.util.List<org.netbeans.modules.classfile.Module$ExportsEntry> getExportsEntries()
meth public java.util.List<org.netbeans.modules.classfile.Module$OpensEntry> getOpensEntries()
meth public java.util.List<org.netbeans.modules.classfile.Module$ProvidesEntry> getProvidesEntries()
meth public java.util.List<org.netbeans.modules.classfile.Module$RequiresEntry> getRequiresEntries()
supr java.lang.Object
hfds exports,flags,name,opens,provides,requires,uses,version

CLSS public final static org.netbeans.modules.classfile.Module$ExportsEntry
 outer org.netbeans.modules.classfile.Module
meth public int getFlags()
meth public java.lang.String getPackage()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getExportsTo()
supr java.lang.Object
hfds flags,pkg,to

CLSS public final static org.netbeans.modules.classfile.Module$OpensEntry
 outer org.netbeans.modules.classfile.Module
meth public int getFlags()
meth public java.lang.String getPackage()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getOpensTo()
supr java.lang.Object
hfds flags,pkg,to

CLSS public final static org.netbeans.modules.classfile.Module$ProvidesEntry
 outer org.netbeans.modules.classfile.Module
meth public java.lang.String toString()
meth public java.util.List<org.netbeans.modules.classfile.ClassName> getImplementations()
meth public org.netbeans.modules.classfile.ClassName getService()
supr java.lang.Object
hfds impls,service

CLSS public final static org.netbeans.modules.classfile.Module$RequiresEntry
 outer org.netbeans.modules.classfile.Module
meth public int getFlags()
meth public java.lang.String getModule()
meth public java.lang.String getVersion()
meth public java.lang.String toString()
supr java.lang.Object
hfds flags,module,version

CLSS public final org.netbeans.modules.classfile.ModuleTarget
meth public java.lang.String getOSArch()
meth public java.lang.String getOSName()
meth public java.lang.String getOSVersion()
meth public java.lang.String getPlatform()
meth public java.lang.String toString()
supr java.lang.Object
hfds platform

CLSS public final org.netbeans.modules.classfile.NestedElementValue
meth public final org.netbeans.modules.classfile.Annotation getNestedValue()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.ElementValue
hfds value

CLSS public final org.netbeans.modules.classfile.Parameter
meth public final java.lang.String getDeclaration()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.Field
hcls ParamIterator

CLSS public final org.netbeans.modules.classfile.PrimitiveElementValue
meth public final org.netbeans.modules.classfile.CPEntry getValue()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.ElementValue
hfds value

CLSS public abstract org.netbeans.modules.classfile.StackMapFrame
innr public final static AppendFrame
innr public final static ChopFrame
innr public final static FullFrame
innr public final static SameFrame
innr public final static SameFrameExtended
innr public final static SameLocals1StackItemFrame
innr public final static SameLocals1StackItemFrameExtended
meth public abstract int getOffsetDelta()
meth public final int getFrameType()
supr java.lang.Object
hfds frameType

CLSS public final static org.netbeans.modules.classfile.StackMapFrame$AppendFrame
 outer org.netbeans.modules.classfile.StackMapFrame
meth public int getOffsetDelta()
meth public org.netbeans.modules.classfile.VerificationTypeInfo[] getLocals()
supr org.netbeans.modules.classfile.StackMapFrame
hfds locals,offset

CLSS public final static org.netbeans.modules.classfile.StackMapFrame$ChopFrame
 outer org.netbeans.modules.classfile.StackMapFrame
meth public int getOffsetDelta()
supr org.netbeans.modules.classfile.StackMapFrame
hfds offset

CLSS public final static org.netbeans.modules.classfile.StackMapFrame$FullFrame
 outer org.netbeans.modules.classfile.StackMapFrame
meth public int getOffsetDelta()
meth public org.netbeans.modules.classfile.VerificationTypeInfo[] getLocals()
meth public org.netbeans.modules.classfile.VerificationTypeInfo[] getStackItems()
supr org.netbeans.modules.classfile.StackMapFrame
hfds locals,offset,stackItems

CLSS public final static org.netbeans.modules.classfile.StackMapFrame$SameFrame
 outer org.netbeans.modules.classfile.StackMapFrame
meth public int getOffsetDelta()
supr org.netbeans.modules.classfile.StackMapFrame

CLSS public final static org.netbeans.modules.classfile.StackMapFrame$SameFrameExtended
 outer org.netbeans.modules.classfile.StackMapFrame
meth public int getOffsetDelta()
supr org.netbeans.modules.classfile.StackMapFrame
hfds offset

CLSS public final static org.netbeans.modules.classfile.StackMapFrame$SameLocals1StackItemFrame
 outer org.netbeans.modules.classfile.StackMapFrame
meth public int getOffsetDelta()
meth public org.netbeans.modules.classfile.VerificationTypeInfo getVerificationTypeInfo()
supr org.netbeans.modules.classfile.StackMapFrame
hfds typeInfo

CLSS public final static org.netbeans.modules.classfile.StackMapFrame$SameLocals1StackItemFrameExtended
 outer org.netbeans.modules.classfile.StackMapFrame
meth public int getOffsetDelta()
meth public org.netbeans.modules.classfile.VerificationTypeInfo getVerificationTypeInfo()
supr org.netbeans.modules.classfile.StackMapFrame
hfds offset,typeInfo

CLSS public final org.netbeans.modules.classfile.Variable
meth public final boolean isConstant()
meth public final boolean isEnumConstant()
meth public final java.lang.Object getConstantValue()
meth public final java.lang.Object getValue()
 anno 0 java.lang.Deprecated()
meth public final java.lang.String getDeclaration()
meth public java.lang.String toString()
supr org.netbeans.modules.classfile.Field
hfds constValue,notLoadedConstValue

CLSS public abstract org.netbeans.modules.classfile.VerificationTypeInfo
fld public final static int ITEM_Double = 3
fld public final static int ITEM_Float = 2
fld public final static int ITEM_Integer = 1
fld public final static int ITEM_Long = 4
fld public final static int ITEM_Null = 5
fld public final static int ITEM_Object = 7
fld public final static int ITEM_Top = 0
fld public final static int ITEM_Uninitialized = 8
fld public final static int ITEM_UninitializedThis = 6
innr public final static DoubleVariableInfo
innr public final static FloatVariableInfo
innr public final static IntegerVariableInfo
innr public final static LongVariableInfo
innr public final static NullVariableInfo
innr public final static ObjectVariableInfo
innr public final static TopVariableInfo
innr public final static UninitializedThisVariableInfo
innr public final static UninitializedVariableInfo
meth public int getTag()
supr java.lang.Object
hfds tag

CLSS public final static org.netbeans.modules.classfile.VerificationTypeInfo$DoubleVariableInfo
 outer org.netbeans.modules.classfile.VerificationTypeInfo
supr org.netbeans.modules.classfile.VerificationTypeInfo

CLSS public final static org.netbeans.modules.classfile.VerificationTypeInfo$FloatVariableInfo
 outer org.netbeans.modules.classfile.VerificationTypeInfo
supr org.netbeans.modules.classfile.VerificationTypeInfo

CLSS public final static org.netbeans.modules.classfile.VerificationTypeInfo$IntegerVariableInfo
 outer org.netbeans.modules.classfile.VerificationTypeInfo
supr org.netbeans.modules.classfile.VerificationTypeInfo

CLSS public final static org.netbeans.modules.classfile.VerificationTypeInfo$LongVariableInfo
 outer org.netbeans.modules.classfile.VerificationTypeInfo
supr org.netbeans.modules.classfile.VerificationTypeInfo

CLSS public final static org.netbeans.modules.classfile.VerificationTypeInfo$NullVariableInfo
 outer org.netbeans.modules.classfile.VerificationTypeInfo
supr org.netbeans.modules.classfile.VerificationTypeInfo

CLSS public final static org.netbeans.modules.classfile.VerificationTypeInfo$ObjectVariableInfo
 outer org.netbeans.modules.classfile.VerificationTypeInfo
meth public org.netbeans.modules.classfile.CPEntry getConstantPoolEntry()
supr org.netbeans.modules.classfile.VerificationTypeInfo
hfds cpEntry

CLSS public final static org.netbeans.modules.classfile.VerificationTypeInfo$TopVariableInfo
 outer org.netbeans.modules.classfile.VerificationTypeInfo
supr org.netbeans.modules.classfile.VerificationTypeInfo

CLSS public final static org.netbeans.modules.classfile.VerificationTypeInfo$UninitializedThisVariableInfo
 outer org.netbeans.modules.classfile.VerificationTypeInfo
supr org.netbeans.modules.classfile.VerificationTypeInfo

CLSS public final static org.netbeans.modules.classfile.VerificationTypeInfo$UninitializedVariableInfo
 outer org.netbeans.modules.classfile.VerificationTypeInfo
meth public int getOffset()
supr org.netbeans.modules.classfile.VerificationTypeInfo
hfds offset

