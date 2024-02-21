#Signature file v4.1
#Version 1.49

CLSS public abstract interface java.io.Serializable

CLSS public abstract java.lang.ClassLoader
cons protected init()
cons protected init(java.lang.ClassLoader)
meth protected final java.lang.Class<?> defineClass(byte[],int,int)
 anno 0 java.lang.Deprecated()
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int)
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int,java.security.ProtectionDomain)
meth protected final java.lang.Class<?> defineClass(java.lang.String,java.nio.ByteBuffer,java.security.ProtectionDomain)
meth protected final java.lang.Class<?> findLoadedClass(java.lang.String)
meth protected final java.lang.Class<?> findSystemClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected final void resolveClass(java.lang.Class<?>)
meth protected final void setSigners(java.lang.Class<?>,java.lang.Object[])
meth protected java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Class<?> loadClass(java.lang.String,boolean) throws java.lang.ClassNotFoundException
meth protected java.lang.Object getClassLoadingLock(java.lang.String)
meth protected java.lang.Package definePackage(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.net.URL)
meth protected java.lang.Package getPackage(java.lang.String)
meth protected java.lang.Package[] getPackages()
meth protected java.lang.String findLibrary(java.lang.String)
meth protected java.net.URL findResource(java.lang.String)
meth protected java.util.Enumeration<java.net.URL> findResources(java.lang.String) throws java.io.IOException
meth protected static boolean registerAsParallelCapable()
meth public final java.lang.ClassLoader getParent()
meth public java.io.InputStream getResourceAsStream(java.lang.String)
meth public java.lang.Class<?> loadClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.net.URL getResource(java.lang.String)
meth public java.util.Enumeration<java.net.URL> getResources(java.lang.String) throws java.io.IOException
meth public static java.io.InputStream getSystemResourceAsStream(java.lang.String)
meth public static java.lang.ClassLoader getSystemClassLoader()
meth public static java.net.URL getSystemResource(java.lang.String)
meth public static java.util.Enumeration<java.net.URL> getSystemResources(java.lang.String) throws java.io.IOException
meth public void clearAssertionStatus()
meth public void setClassAssertionStatus(java.lang.String,boolean)
meth public void setDefaultAssertionStatus(boolean)
meth public void setPackageAssertionStatus(java.lang.String,boolean)
supr java.lang.Object

CLSS public abstract interface java.lang.Cloneable

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

CLSS public abstract net.sf.cglib.beans.BeanCopier
cons public init()
innr public static Generator
meth public abstract void copy(java.lang.Object,java.lang.Object,net.sf.cglib.core.Converter)
meth public static net.sf.cglib.beans.BeanCopier create(java.lang.Class,java.lang.Class,boolean)
supr java.lang.Object
hfds BEAN_COPIER,CONVERT,CONVERTER,COPY,KEY_FACTORY,class$net$sf$cglib$beans$BeanCopier,class$net$sf$cglib$beans$BeanCopier$BeanCopierKey
hcls BeanCopierKey

CLSS public static net.sf.cglib.beans.BeanCopier$Generator
 outer net.sf.cglib.beans.BeanCopier
cons public init()
meth protected java.lang.ClassLoader getDefaultClassLoader()
meth protected java.lang.Object firstInstance(java.lang.Class)
meth protected java.lang.Object nextInstance(java.lang.Object)
meth public net.sf.cglib.beans.BeanCopier create()
meth public void generateClass(org.objectweb.asm.ClassVisitor)
meth public void setSource(java.lang.Class)
meth public void setTarget(java.lang.Class)
meth public void setUseConverter(boolean)
supr net.sf.cglib.core.AbstractClassGenerator
hfds SOURCE,source,target,useConverter

CLSS public net.sf.cglib.beans.BeanGenerator
cons public init()
meth protected java.lang.ClassLoader getDefaultClassLoader()
meth protected java.lang.Object firstInstance(java.lang.Class)
meth protected java.lang.Object nextInstance(java.lang.Object)
meth public java.lang.Object create()
meth public java.lang.Object createClass()
meth public static void addProperties(net.sf.cglib.beans.BeanGenerator,java.beans.PropertyDescriptor[])
meth public static void addProperties(net.sf.cglib.beans.BeanGenerator,java.lang.Class)
meth public static void addProperties(net.sf.cglib.beans.BeanGenerator,java.util.Map)
meth public void addProperty(java.lang.String,java.lang.Class)
meth public void generateClass(org.objectweb.asm.ClassVisitor) throws java.lang.Exception
meth public void setSuperclass(java.lang.Class)
supr net.sf.cglib.core.AbstractClassGenerator
hfds KEY_FACTORY,SOURCE,class$java$lang$Object,class$net$sf$cglib$beans$BeanGenerator,class$net$sf$cglib$beans$BeanGenerator$BeanGeneratorKey,classOnly,props,superclass
hcls BeanGeneratorKey

CLSS public abstract net.sf.cglib.beans.BeanMap
cons protected init()
cons protected init(java.lang.Object)
fld protected java.lang.Object bean
fld public final static int REQUIRE_GETTER = 1
fld public final static int REQUIRE_SETTER = 2
innr public static Generator
intf java.util.Map
meth public abstract java.lang.Class getPropertyType(java.lang.String)
meth public abstract java.lang.Object get(java.lang.Object,java.lang.Object)
meth public abstract java.lang.Object put(java.lang.Object,java.lang.Object,java.lang.Object)
meth public abstract net.sf.cglib.beans.BeanMap newInstance(java.lang.Object)
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.Object get(java.lang.Object)
meth public java.lang.Object getBean()
meth public java.lang.Object put(java.lang.Object,java.lang.Object)
meth public java.lang.Object remove(java.lang.Object)
meth public java.lang.String toString()
meth public java.util.Collection values()
meth public java.util.Set entrySet()
meth public static net.sf.cglib.beans.BeanMap create(java.lang.Object)
meth public void clear()
meth public void putAll(java.util.Map)
meth public void setBean(java.lang.Object)
supr java.lang.Object
hfds class$net$sf$cglib$beans$BeanMap,class$net$sf$cglib$beans$BeanMap$Generator$BeanMapKey

CLSS public static net.sf.cglib.beans.BeanMap$Generator
 outer net.sf.cglib.beans.BeanMap
cons public init()
meth protected java.lang.ClassLoader getDefaultClassLoader()
meth protected java.lang.Object firstInstance(java.lang.Class)
meth protected java.lang.Object nextInstance(java.lang.Object)
meth public net.sf.cglib.beans.BeanMap create()
meth public void generateClass(org.objectweb.asm.ClassVisitor) throws java.lang.Exception
meth public void setBean(java.lang.Object)
meth public void setBeanClass(java.lang.Class)
meth public void setRequire(int)
supr net.sf.cglib.core.AbstractClassGenerator
hfds KEY_FACTORY,SOURCE,bean,beanClass,require
hcls BeanMapKey

CLSS public abstract net.sf.cglib.beans.BulkBean
cons protected init()
fld protected java.lang.Class target
fld protected java.lang.Class[] types
fld protected java.lang.String[] getters
fld protected java.lang.String[] setters
innr public static Generator
meth public abstract void getPropertyValues(java.lang.Object,java.lang.Object[])
meth public abstract void setPropertyValues(java.lang.Object,java.lang.Object[])
meth public java.lang.Class[] getPropertyTypes()
meth public java.lang.Object[] getPropertyValues(java.lang.Object)
meth public java.lang.String[] getGetters()
meth public java.lang.String[] getSetters()
meth public static net.sf.cglib.beans.BulkBean create(java.lang.Class,java.lang.String[],java.lang.String[],java.lang.Class[])
supr java.lang.Object
hfds KEY_FACTORY,class$net$sf$cglib$beans$BulkBean,class$net$sf$cglib$beans$BulkBean$BulkBeanKey
hcls BulkBeanKey

CLSS public static net.sf.cglib.beans.BulkBean$Generator
 outer net.sf.cglib.beans.BulkBean
cons public init()
meth protected java.lang.ClassLoader getDefaultClassLoader()
meth protected java.lang.Object firstInstance(java.lang.Class)
meth protected java.lang.Object nextInstance(java.lang.Object)
meth public net.sf.cglib.beans.BulkBean create()
meth public void generateClass(org.objectweb.asm.ClassVisitor) throws java.lang.Exception
meth public void setGetters(java.lang.String[])
meth public void setSetters(java.lang.String[])
meth public void setTarget(java.lang.Class)
meth public void setTypes(java.lang.Class[])
supr net.sf.cglib.core.AbstractClassGenerator
hfds SOURCE,getters,setters,target,types

CLSS public net.sf.cglib.beans.BulkBeanException
cons public init(java.lang.String,int)
cons public init(java.lang.Throwable,int)
meth public int getIndex()
meth public java.lang.Throwable getCause()
supr java.lang.RuntimeException
hfds cause,index

CLSS public net.sf.cglib.beans.FixedKeySet
cons public init(java.lang.String[])
meth public int size()
meth public java.util.Iterator iterator()
supr java.util.AbstractSet
hfds set,size

CLSS public net.sf.cglib.beans.ImmutableBean
innr public static Generator
meth public static java.lang.Object create(java.lang.Object)
supr java.lang.Object
hfds CSTRUCT_OBJECT,FIELD_NAME,ILLEGAL_STATE_EXCEPTION,OBJECT_CLASSES,class$java$lang$Object,class$net$sf$cglib$beans$ImmutableBean

CLSS public static net.sf.cglib.beans.ImmutableBean$Generator
 outer net.sf.cglib.beans.ImmutableBean
cons public init()
meth protected java.lang.ClassLoader getDefaultClassLoader()
meth protected java.lang.Object firstInstance(java.lang.Class)
meth protected java.lang.Object nextInstance(java.lang.Object)
meth public java.lang.Object create()
meth public void generateClass(org.objectweb.asm.ClassVisitor)
meth public void setBean(java.lang.Object)
supr net.sf.cglib.core.AbstractClassGenerator
hfds SOURCE,bean,target

CLSS public abstract net.sf.cglib.core.AbstractClassGenerator
cons protected init(net.sf.cglib.core.AbstractClassGenerator$Source)
innr protected static Source
intf net.sf.cglib.core.ClassGenerator
meth protected abstract java.lang.ClassLoader getDefaultClassLoader()
meth protected abstract java.lang.Object firstInstance(java.lang.Class) throws java.lang.Exception
meth protected abstract java.lang.Object nextInstance(java.lang.Object) throws java.lang.Exception
meth protected final java.lang.String getClassName()
meth protected java.lang.Object create(java.lang.Object)
meth protected void setNamePrefix(java.lang.String)
meth public boolean getAttemptLoad()
meth public boolean getUseCache()
meth public java.lang.ClassLoader getClassLoader()
meth public net.sf.cglib.core.GeneratorStrategy getStrategy()
meth public net.sf.cglib.core.NamingPolicy getNamingPolicy()
meth public static net.sf.cglib.core.AbstractClassGenerator getCurrent()
meth public void setAttemptLoad(boolean)
meth public void setClassLoader(java.lang.ClassLoader)
meth public void setNamingPolicy(net.sf.cglib.core.NamingPolicy)
meth public void setStrategy(net.sf.cglib.core.GeneratorStrategy)
meth public void setUseCache(boolean)
supr java.lang.Object
hfds CURRENT,NAME_KEY,attemptLoad,classLoader,className,key,namePrefix,namingPolicy,source,strategy,useCache

CLSS protected static net.sf.cglib.core.AbstractClassGenerator$Source
 outer net.sf.cglib.core.AbstractClassGenerator
cons public init(java.lang.String)
supr java.lang.Object
hfds cache,name

CLSS public net.sf.cglib.core.Block
cons public init(net.sf.cglib.core.CodeEmitter)
meth public net.sf.cglib.core.CodeEmitter getCodeEmitter()
meth public org.objectweb.asm.Label getEnd()
meth public org.objectweb.asm.Label getStart()
meth public void end()
supr java.lang.Object
hfds e,end,start

CLSS public net.sf.cglib.core.ClassEmitter
hfds classInfo,fieldInfo,hookCounter,rawStaticInit,staticHook,staticHookSig,staticInit
hcls FieldInfo

CLSS public abstract interface net.sf.cglib.core.ClassGenerator
meth public abstract void generateClass(org.objectweb.asm.ClassVisitor) throws java.lang.Exception

CLSS public abstract net.sf.cglib.core.ClassInfo
cons protected init()
meth public abstract int getModifiers()
meth public abstract org.objectweb.asm.Type getSuperType()
meth public abstract org.objectweb.asm.Type getType()
meth public abstract org.objectweb.asm.Type[] getInterfaces()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public net.sf.cglib.core.ClassNameReader
meth public static java.lang.String getClassName(org.objectweb.asm.ClassReader)
meth public static java.lang.String[] getClassInfo(org.objectweb.asm.ClassReader)
supr java.lang.Object
hfds EARLY_EXIT
hcls EarlyExitException

CLSS public net.sf.cglib.core.ClassesKey
meth public static java.lang.Object create(java.lang.Object[])
supr java.lang.Object
hfds FACTORY,class$net$sf$cglib$core$ClassesKey$Key
hcls Key

CLSS public net.sf.cglib.core.CodeEmitter
hfds BOOLEAN_VALUE,CHAR_VALUE,CSTRUCT_NULL,CSTRUCT_STRING,DOUBLE_VALUE,FLOAT_VALUE,INT_VALUE,LONG_VALUE,ce,state
hcls State

CLSS public net.sf.cglib.core.CodeGenerationException
cons public init(java.lang.Throwable)
meth public java.lang.Throwable getCause()
supr java.lang.RuntimeException
hfds cause

CLSS public net.sf.cglib.core.CollectionUtils
meth public static java.util.Collection filter(java.util.Collection,net.sf.cglib.core.Predicate)
meth public static java.util.List transform(java.util.Collection,net.sf.cglib.core.Transformer)
meth public static java.util.Map bucket(java.util.Collection,net.sf.cglib.core.Transformer)
meth public static java.util.Map getIndexMap(java.util.List)
meth public static void reverse(java.util.Map,java.util.Map)
supr java.lang.Object

CLSS public abstract interface net.sf.cglib.core.Constants

CLSS public abstract interface net.sf.cglib.core.Converter
meth public abstract java.lang.Object convert(java.lang.Object,java.lang.Class,java.lang.Object)

CLSS public abstract interface net.sf.cglib.core.Customizer
meth public abstract void customize(net.sf.cglib.core.CodeEmitter,org.objectweb.asm.Type)

CLSS public net.sf.cglib.core.DebuggingClassWriter
hfds className,debugLocation,superName,traceEnabled

CLSS public net.sf.cglib.core.DefaultGeneratorStrategy
cons public init()
fld public final static net.sf.cglib.core.DefaultGeneratorStrategy INSTANCE
intf net.sf.cglib.core.GeneratorStrategy
meth protected byte[] transform(byte[]) throws java.lang.Exception
meth protected net.sf.cglib.core.ClassGenerator transform(net.sf.cglib.core.ClassGenerator) throws java.lang.Exception
meth protected org.objectweb.asm.ClassWriter getClassWriter() throws java.lang.Exception
meth public byte[] generate(net.sf.cglib.core.ClassGenerator) throws java.lang.Exception
supr java.lang.Object

CLSS public net.sf.cglib.core.DefaultNamingPolicy
cons public init()
fld public final static net.sf.cglib.core.DefaultNamingPolicy INSTANCE
intf net.sf.cglib.core.NamingPolicy
meth protected java.lang.String getTag()
meth public java.lang.String getClassName(java.lang.String,java.lang.String,java.lang.Object,net.sf.cglib.core.Predicate)
supr java.lang.Object

CLSS public net.sf.cglib.core.DuplicatesPredicate
cons public init()
intf net.sf.cglib.core.Predicate
meth public boolean evaluate(java.lang.Object)
supr java.lang.Object
hfds unique

CLSS public net.sf.cglib.core.EmitUtils
fld public final static net.sf.cglib.core.EmitUtils$ArrayDelimiters DEFAULT_DELIMITERS
innr public static ArrayDelimiters
meth public static net.sf.cglib.core.CodeEmitter begin_method(net.sf.cglib.core.ClassEmitter,net.sf.cglib.core.MethodInfo)
meth public static net.sf.cglib.core.CodeEmitter begin_method(net.sf.cglib.core.ClassEmitter,net.sf.cglib.core.MethodInfo,int)
meth public static void add_properties(net.sf.cglib.core.ClassEmitter,java.lang.String[],org.objectweb.asm.Type[])
meth public static void add_property(net.sf.cglib.core.ClassEmitter,java.lang.String,org.objectweb.asm.Type,java.lang.String)
meth public static void append_string(net.sf.cglib.core.CodeEmitter,org.objectweb.asm.Type,net.sf.cglib.core.EmitUtils$ArrayDelimiters,net.sf.cglib.core.Customizer)
meth public static void constructor_switch(net.sf.cglib.core.CodeEmitter,java.util.List,net.sf.cglib.core.ObjectSwitchCallback)
meth public static void factory_method(net.sf.cglib.core.ClassEmitter,net.sf.cglib.core.Signature)
meth public static void hash_code(net.sf.cglib.core.CodeEmitter,org.objectweb.asm.Type,int,net.sf.cglib.core.Customizer)
meth public static void load_class(net.sf.cglib.core.CodeEmitter,org.objectweb.asm.Type)
meth public static void load_class_this(net.sf.cglib.core.CodeEmitter)
meth public static void load_method(net.sf.cglib.core.CodeEmitter,net.sf.cglib.core.MethodInfo)
meth public static void method_switch(net.sf.cglib.core.CodeEmitter,java.util.List,net.sf.cglib.core.ObjectSwitchCallback)
meth public static void not_equals(net.sf.cglib.core.CodeEmitter,org.objectweb.asm.Type,org.objectweb.asm.Label,net.sf.cglib.core.Customizer)
meth public static void null_constructor(net.sf.cglib.core.ClassEmitter)
meth public static void process_array(net.sf.cglib.core.CodeEmitter,org.objectweb.asm.Type,net.sf.cglib.core.ProcessArrayCallback)
meth public static void process_arrays(net.sf.cglib.core.CodeEmitter,org.objectweb.asm.Type,net.sf.cglib.core.ProcessArrayCallback)
meth public static void push_array(net.sf.cglib.core.CodeEmitter,java.lang.Object[])
meth public static void push_object(net.sf.cglib.core.CodeEmitter,java.lang.Object)
meth public static void string_switch(net.sf.cglib.core.CodeEmitter,java.lang.String[],int,net.sf.cglib.core.ObjectSwitchCallback)
meth public static void wrap_throwable(net.sf.cglib.core.Block,org.objectweb.asm.Type)
meth public static void wrap_undeclared_throwable(net.sf.cglib.core.CodeEmitter,net.sf.cglib.core.Block,org.objectweb.asm.Type[],org.objectweb.asm.Type)
supr java.lang.Object
hfds APPEND_BOOLEAN,APPEND_CHAR,APPEND_DOUBLE,APPEND_FLOAT,APPEND_INT,APPEND_LONG,APPEND_STRING,CSTRUCT_NULL,CSTRUCT_THROWABLE,DOUBLE_TO_LONG_BITS,EQUALS,FLOAT_TO_INT_BITS,FOR_NAME,GET_DECLARED_METHOD,GET_NAME,HASH_CODE,LENGTH,SET_LENGTH,STRING_CHAR_AT,STRING_LENGTH,TO_STRING,class$java$lang$Class,class$org$objectweb$asm$Type
hcls ParameterTyper

CLSS public static net.sf.cglib.core.EmitUtils$ArrayDelimiters
 outer net.sf.cglib.core.EmitUtils
cons public init(java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object
hfds after,before,inside

CLSS public abstract interface net.sf.cglib.core.GeneratorStrategy
meth public abstract boolean equals(java.lang.Object)
meth public abstract byte[] generate(net.sf.cglib.core.ClassGenerator) throws java.lang.Exception

CLSS public abstract net.sf.cglib.core.KeyFactory
cons protected init()
fld public final static net.sf.cglib.core.Customizer CLASS_BY_NAME
fld public final static net.sf.cglib.core.Customizer OBJECT_BY_CLASS
innr public static Generator
meth public static net.sf.cglib.core.KeyFactory create(java.lang.Class)
meth public static net.sf.cglib.core.KeyFactory create(java.lang.Class,net.sf.cglib.core.Customizer)
meth public static net.sf.cglib.core.KeyFactory create(java.lang.ClassLoader,java.lang.Class,net.sf.cglib.core.Customizer)
supr java.lang.Object
hfds APPEND_STRING,EQUALS,GET_CLASS,GET_NAME,HASH_CODE,KEY_FACTORY,PRIMES,TO_STRING,class$java$lang$Object,class$net$sf$cglib$core$KeyFactory

CLSS public static net.sf.cglib.core.KeyFactory$Generator
 outer net.sf.cglib.core.KeyFactory
cons public init()
meth protected java.lang.ClassLoader getDefaultClassLoader()
meth protected java.lang.Object firstInstance(java.lang.Class)
meth protected java.lang.Object nextInstance(java.lang.Object)
meth public net.sf.cglib.core.KeyFactory create()
meth public void generateClass(org.objectweb.asm.ClassVisitor)
meth public void setCustomizer(net.sf.cglib.core.Customizer)
meth public void setHashConstant(int)
meth public void setHashMultiplier(int)
meth public void setInterface(java.lang.Class)
supr net.sf.cglib.core.AbstractClassGenerator
hfds SOURCE,constant,customizer,keyInterface,multiplier

CLSS public net.sf.cglib.core.Local
cons public init(int,org.objectweb.asm.Type)
meth public int getIndex()
meth public org.objectweb.asm.Type getType()
supr java.lang.Object
hfds index,type

CLSS public net.sf.cglib.core.LocalVariablesSorter
hfds state
hcls 1,State

CLSS public abstract net.sf.cglib.core.MethodInfo
cons protected init()
meth public abstract int getModifiers()
meth public abstract net.sf.cglib.core.ClassInfo getClassInfo()
meth public abstract net.sf.cglib.core.Signature getSignature()
meth public abstract org.objectweb.asm.Type[] getExceptionTypes()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public net.sf.cglib.core.MethodInfoTransformer
cons public init()
intf net.sf.cglib.core.Transformer
meth public java.lang.Object transform(java.lang.Object)
meth public static net.sf.cglib.core.MethodInfoTransformer getInstance()
supr java.lang.Object
hfds INSTANCE

CLSS public net.sf.cglib.core.MethodWrapper
innr public abstract interface static MethodWrapperKey
meth public static java.lang.Object create(java.lang.reflect.Method)
meth public static java.util.Set createSet(java.util.Collection)
supr java.lang.Object
hfds KEY_FACTORY,class$net$sf$cglib$core$MethodWrapper$MethodWrapperKey

CLSS public abstract interface static net.sf.cglib.core.MethodWrapper$MethodWrapperKey
 outer net.sf.cglib.core.MethodWrapper
meth public abstract java.lang.Object newInstance(java.lang.String,java.lang.String[],java.lang.String)

CLSS public abstract interface net.sf.cglib.core.NamingPolicy
meth public abstract boolean equals(java.lang.Object)
meth public abstract java.lang.String getClassName(java.lang.String,java.lang.String,java.lang.Object,net.sf.cglib.core.Predicate)

CLSS public abstract interface net.sf.cglib.core.ObjectSwitchCallback
meth public abstract void processCase(java.lang.Object,org.objectweb.asm.Label) throws java.lang.Exception
meth public abstract void processDefault() throws java.lang.Exception

CLSS public abstract interface net.sf.cglib.core.Predicate
meth public abstract boolean evaluate(java.lang.Object)

CLSS public abstract interface net.sf.cglib.core.ProcessArrayCallback
meth public abstract void processElement(org.objectweb.asm.Type)

CLSS public abstract interface net.sf.cglib.core.ProcessSwitchCallback
meth public abstract void processCase(int,org.objectweb.asm.Label) throws java.lang.Exception
meth public abstract void processDefault() throws java.lang.Exception

CLSS public net.sf.cglib.core.ReflectUtils
meth public static int findPackageProtected(java.lang.Class[])
meth public static java.beans.PropertyDescriptor[] getBeanGetters(java.lang.Class)
meth public static java.beans.PropertyDescriptor[] getBeanProperties(java.lang.Class)
meth public static java.beans.PropertyDescriptor[] getBeanSetters(java.lang.Class)
meth public static java.lang.Class defineClass(java.lang.String,byte[],java.lang.ClassLoader) throws java.lang.Exception
meth public static java.lang.Class[] getClasses(java.lang.Object[])
meth public static java.lang.Object newInstance(java.lang.Class)
meth public static java.lang.Object newInstance(java.lang.Class,java.lang.Class[],java.lang.Object[])
meth public static java.lang.Object newInstance(java.lang.reflect.Constructor,java.lang.Object[])
meth public static java.lang.String[] getNames(java.lang.Class[])
meth public static java.lang.reflect.Constructor findConstructor(java.lang.String)
meth public static java.lang.reflect.Constructor findConstructor(java.lang.String,java.lang.ClassLoader)
meth public static java.lang.reflect.Constructor getConstructor(java.lang.Class,java.lang.Class[])
meth public static java.lang.reflect.Method findDeclaredMethod(java.lang.Class,java.lang.String,java.lang.Class[]) throws java.lang.NoSuchMethodException
meth public static java.lang.reflect.Method findInterfaceMethod(java.lang.Class)
meth public static java.lang.reflect.Method findMethod(java.lang.String)
meth public static java.lang.reflect.Method findMethod(java.lang.String,java.lang.ClassLoader)
meth public static java.lang.reflect.Method findNewInstance(java.lang.Class)
meth public static java.lang.reflect.Method[] findMethods(java.lang.String[],java.lang.reflect.Method[])
meth public static java.lang.reflect.Method[] getPropertyMethods(java.beans.PropertyDescriptor[],boolean,boolean)
meth public static java.util.List addAllInterfaces(java.lang.Class,java.util.List)
meth public static java.util.List addAllMethods(java.lang.Class,java.util.List)
meth public static net.sf.cglib.core.ClassInfo getClassInfo(java.lang.Class)
meth public static net.sf.cglib.core.MethodInfo getMethodInfo(java.lang.reflect.Member)
meth public static net.sf.cglib.core.MethodInfo getMethodInfo(java.lang.reflect.Member,int)
meth public static net.sf.cglib.core.Signature getSignature(java.lang.reflect.Member)
meth public static org.objectweb.asm.Type[] getExceptionTypes(java.lang.reflect.Member)
supr java.lang.Object
hfds CGLIB_PACKAGES,DEFINE_CLASS,PROTECTION_DOMAIN,array$B,class$java$lang$Object,class$java$lang$String,class$java$security$ProtectionDomain,class$net$sf$cglib$core$ReflectUtils,defaultLoader,primitives,transforms

CLSS public net.sf.cglib.core.RejectModifierPredicate
cons public init(int)
intf net.sf.cglib.core.Predicate
meth public boolean evaluate(java.lang.Object)
supr java.lang.Object
hfds rejectMask

CLSS public net.sf.cglib.core.Signature
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,org.objectweb.asm.Type,org.objectweb.asm.Type[])
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getDescriptor()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public org.objectweb.asm.Type getReturnType()
meth public org.objectweb.asm.Type[] getArgumentTypes()
supr java.lang.Object
hfds desc,name

CLSS public net.sf.cglib.core.TinyBitSet
cons public init()
meth public boolean get(int)
meth public int cardinality()
meth public int length()
meth public void clear(int)
meth public void set(int)
supr java.lang.Object
hfds T,value

CLSS public abstract interface net.sf.cglib.core.Transformer
meth public abstract java.lang.Object transform(java.lang.Object)

CLSS public net.sf.cglib.core.TypeUtils
meth public static boolean isAbstract(int)
meth public static boolean isArray(org.objectweb.asm.Type)
meth public static boolean isConstructor(net.sf.cglib.core.MethodInfo)
meth public static boolean isFinal(int)
meth public static boolean isInterface(int)
meth public static boolean isPrimitive(org.objectweb.asm.Type)
meth public static boolean isPrivate(int)
meth public static boolean isProtected(int)
meth public static boolean isPublic(int)
meth public static boolean isStatic(int)
meth public static boolean isSynthetic(int)
meth public static int DCONST(double)
meth public static int FCONST(float)
meth public static int ICONST(int)
meth public static int LCONST(long)
meth public static int NEWARRAY(org.objectweb.asm.Type)
meth public static int getStackSize(org.objectweb.asm.Type[])
meth public static java.lang.String emulateClassGetName(org.objectweb.asm.Type)
meth public static java.lang.String escapeType(java.lang.String)
meth public static java.lang.String getClassName(org.objectweb.asm.Type)
meth public static java.lang.String getPackageName(java.lang.String)
meth public static java.lang.String getPackageName(org.objectweb.asm.Type)
meth public static java.lang.String upperFirst(java.lang.String)
meth public static java.lang.String[] toInternalNames(org.objectweb.asm.Type[])
meth public static net.sf.cglib.core.Signature parseConstructor(java.lang.String)
meth public static net.sf.cglib.core.Signature parseConstructor(org.objectweb.asm.Type[])
meth public static net.sf.cglib.core.Signature parseSignature(java.lang.String)
meth public static org.objectweb.asm.Type fromInternalName(java.lang.String)
meth public static org.objectweb.asm.Type getBoxedType(org.objectweb.asm.Type)
meth public static org.objectweb.asm.Type getComponentType(org.objectweb.asm.Type)
meth public static org.objectweb.asm.Type getType(java.lang.String)
meth public static org.objectweb.asm.Type getUnboxedType(org.objectweb.asm.Type)
meth public static org.objectweb.asm.Type parseType(java.lang.String)
meth public static org.objectweb.asm.Type[] add(org.objectweb.asm.Type[],org.objectweb.asm.Type)
meth public static org.objectweb.asm.Type[] add(org.objectweb.asm.Type[],org.objectweb.asm.Type[])
meth public static org.objectweb.asm.Type[] fromInternalNames(java.lang.String[])
meth public static org.objectweb.asm.Type[] getTypes(java.lang.Class[])
meth public static org.objectweb.asm.Type[] parseTypes(java.lang.String)
supr java.lang.Object
hfds rtransforms,transforms

CLSS public net.sf.cglib.core.VisibilityPredicate
cons public init(java.lang.Class,boolean)
intf net.sf.cglib.core.Predicate
meth public boolean evaluate(java.lang.Object)
supr java.lang.Object
hfds pkg,protectedOk

CLSS public abstract interface net.sf.cglib.proxy.Callback

CLSS public abstract interface net.sf.cglib.proxy.CallbackFilter
meth public abstract boolean equals(java.lang.Object)
meth public abstract int accept(java.lang.reflect.Method)

CLSS public abstract net.sf.cglib.proxy.CallbackHelper
cons public init(java.lang.Class,java.lang.Class[])
intf net.sf.cglib.proxy.CallbackFilter
meth protected abstract java.lang.Object getCallback(java.lang.reflect.Method)
meth public boolean equals(java.lang.Object)
meth public int accept(java.lang.reflect.Method)
meth public int hashCode()
meth public java.lang.Class[] getCallbackTypes()
meth public net.sf.cglib.proxy.Callback[] getCallbacks()
supr java.lang.Object
hfds callbacks,methodMap

CLSS public abstract interface net.sf.cglib.proxy.Dispatcher
intf net.sf.cglib.proxy.Callback
meth public abstract java.lang.Object loadObject() throws java.lang.Exception

CLSS public net.sf.cglib.proxy.Enhancer
cons public init()
innr public abstract interface static EnhancerKey
meth protected java.lang.ClassLoader getDefaultClassLoader()
meth protected java.lang.Object firstInstance(java.lang.Class) throws java.lang.Exception
meth protected java.lang.Object nextInstance(java.lang.Object)
meth protected void filterConstructors(java.lang.Class,java.util.List)
meth public java.lang.Class createClass()
meth public java.lang.Object create()
meth public java.lang.Object create(java.lang.Class[],java.lang.Object[])
meth public static boolean isEnhanced(java.lang.Class)
meth public static java.lang.Object create(java.lang.Class,java.lang.Class[],net.sf.cglib.proxy.Callback)
meth public static java.lang.Object create(java.lang.Class,java.lang.Class[],net.sf.cglib.proxy.CallbackFilter,net.sf.cglib.proxy.Callback[])
meth public static java.lang.Object create(java.lang.Class,net.sf.cglib.proxy.Callback)
meth public static void getMethods(java.lang.Class,java.lang.Class[],java.util.List)
meth public static void registerCallbacks(java.lang.Class,net.sf.cglib.proxy.Callback[])
meth public static void registerStaticCallbacks(java.lang.Class,net.sf.cglib.proxy.Callback[])
meth public void generateClass(org.objectweb.asm.ClassVisitor) throws java.lang.Exception
meth public void setCallback(net.sf.cglib.proxy.Callback)
meth public void setCallbackFilter(net.sf.cglib.proxy.CallbackFilter)
meth public void setCallbackType(java.lang.Class)
meth public void setCallbackTypes(java.lang.Class[])
meth public void setCallbacks(net.sf.cglib.proxy.Callback[])
meth public void setInterceptDuringConstruction(boolean)
meth public void setInterfaces(java.lang.Class[])
meth public void setSerialVersionUID(java.lang.Long)
meth public void setSuperclass(java.lang.Class)
meth public void setUseFactory(boolean)
supr net.sf.cglib.core.AbstractClassGenerator
hfds ALL_ZERO,BIND_CALLBACKS,BOUND_FIELD,CALLBACK,CALLBACK_ARRAY,CONSTRUCTED_FIELD,CSTRUCT_NULL,FACTORY,GET_CALLBACK,GET_CALLBACKS,ILLEGAL_ARGUMENT_EXCEPTION,ILLEGAL_STATE_EXCEPTION,KEY_FACTORY,MULTIARG_NEW_INSTANCE,NEW_INSTANCE,SET_CALLBACK,SET_CALLBACKS,SET_STATIC_CALLBACKS,SET_STATIC_CALLBACKS_NAME,SET_THREAD_CALLBACKS,SET_THREAD_CALLBACKS_NAME,SINGLE_NEW_INSTANCE,SOURCE,STATIC_CALLBACKS_FIELD,THREAD_CALLBACKS_FIELD,THREAD_LOCAL,THREAD_LOCAL_GET,THREAD_LOCAL_SET,argumentTypes,arguments,array$Lnet$sf$cglib$proxy$Callback,callbackTypes,callbacks,class$java$lang$Object,class$net$sf$cglib$proxy$Enhancer,class$net$sf$cglib$proxy$Enhancer$EnhancerKey,class$net$sf$cglib$proxy$Factory,classOnly,filter,interceptDuringConstruction,interfaces,serialVersionUID,superclass,useFactory

CLSS public abstract interface static net.sf.cglib.proxy.Enhancer$EnhancerKey
 outer net.sf.cglib.proxy.Enhancer
meth public abstract java.lang.Object newInstance(java.lang.String,java.lang.String[],net.sf.cglib.proxy.CallbackFilter,org.objectweb.asm.Type[],boolean,boolean,java.lang.Long)

CLSS public abstract interface net.sf.cglib.proxy.Factory
meth public abstract java.lang.Object newInstance(java.lang.Class[],java.lang.Object[],net.sf.cglib.proxy.Callback[])
meth public abstract java.lang.Object newInstance(net.sf.cglib.proxy.Callback)
meth public abstract java.lang.Object newInstance(net.sf.cglib.proxy.Callback[])
meth public abstract net.sf.cglib.proxy.Callback getCallback(int)
meth public abstract net.sf.cglib.proxy.Callback[] getCallbacks()
meth public abstract void setCallback(int,net.sf.cglib.proxy.Callback)
meth public abstract void setCallbacks(net.sf.cglib.proxy.Callback[])

CLSS public abstract interface net.sf.cglib.proxy.FixedValue
intf net.sf.cglib.proxy.Callback
meth public abstract java.lang.Object loadObject() throws java.lang.Exception

CLSS public net.sf.cglib.proxy.InterfaceMaker
cons public init()
meth protected java.lang.ClassLoader getDefaultClassLoader()
meth protected java.lang.Object firstInstance(java.lang.Class)
meth protected java.lang.Object nextInstance(java.lang.Object)
meth public java.lang.Class create()
meth public void add(java.lang.Class)
meth public void add(java.lang.reflect.Method)
meth public void add(net.sf.cglib.core.Signature,org.objectweb.asm.Type[])
meth public void generateClass(org.objectweb.asm.ClassVisitor) throws java.lang.Exception
supr net.sf.cglib.core.AbstractClassGenerator
hfds SOURCE,class$net$sf$cglib$proxy$InterfaceMaker,signatures

CLSS public abstract interface net.sf.cglib.proxy.InvocationHandler
intf net.sf.cglib.proxy.Callback
meth public abstract java.lang.Object invoke(java.lang.Object,java.lang.reflect.Method,java.lang.Object[]) throws java.lang.Throwable

CLSS public abstract interface net.sf.cglib.proxy.LazyLoader
intf net.sf.cglib.proxy.Callback
meth public abstract java.lang.Object loadObject() throws java.lang.Exception

CLSS public abstract interface net.sf.cglib.proxy.MethodInterceptor
intf net.sf.cglib.proxy.Callback
meth public abstract java.lang.Object intercept(java.lang.Object,java.lang.reflect.Method,java.lang.Object[],net.sf.cglib.proxy.MethodProxy) throws java.lang.Throwable

CLSS public net.sf.cglib.proxy.MethodProxy
meth public int getSuperIndex()
meth public java.lang.Object invoke(java.lang.Object,java.lang.Object[]) throws java.lang.Throwable
meth public java.lang.Object invokeSuper(java.lang.Object,java.lang.Object[]) throws java.lang.Throwable
meth public java.lang.String getSuperName()
meth public net.sf.cglib.core.Signature getSignature()
meth public static net.sf.cglib.proxy.MethodProxy create(java.lang.Class,java.lang.Class,java.lang.String,java.lang.String,java.lang.String)
meth public static net.sf.cglib.proxy.MethodProxy find(java.lang.Class,net.sf.cglib.core.Signature)
supr java.lang.Object
hfds createInfo,fastClassInfo,initLock,sig1,sig2
hcls 1,CreateInfo,FastClassInfo

CLSS public abstract net.sf.cglib.proxy.Mixin
cons public init()
fld public final static int STYLE_BEANS = 1
fld public final static int STYLE_EVERYTHING = 2
fld public final static int STYLE_INTERFACES = 0
innr public static Generator
meth public abstract net.sf.cglib.proxy.Mixin newInstance(java.lang.Object[])
meth public static java.lang.Class[] getClasses(java.lang.Object[])
meth public static net.sf.cglib.proxy.Mixin create(java.lang.Class[],java.lang.Object[])
meth public static net.sf.cglib.proxy.Mixin create(java.lang.Object[])
meth public static net.sf.cglib.proxy.Mixin createBean(java.lang.ClassLoader,java.lang.Object[])
meth public static net.sf.cglib.proxy.Mixin createBean(java.lang.Object[])
supr java.lang.Object
hfds KEY_FACTORY,ROUTE_CACHE,class$net$sf$cglib$proxy$Mixin,class$net$sf$cglib$proxy$Mixin$MixinKey
hcls MixinKey,Route

CLSS public static net.sf.cglib.proxy.Mixin$Generator
 outer net.sf.cglib.proxy.Mixin
cons public init()
meth protected java.lang.ClassLoader getDefaultClassLoader()
meth protected java.lang.Object firstInstance(java.lang.Class)
meth protected java.lang.Object nextInstance(java.lang.Object)
meth public net.sf.cglib.proxy.Mixin create()
meth public void generateClass(org.objectweb.asm.ClassVisitor)
meth public void setClasses(java.lang.Class[])
meth public void setDelegates(java.lang.Object[])
meth public void setStyle(int)
supr net.sf.cglib.core.AbstractClassGenerator
hfds SOURCE,classes,delegates,route,style

CLSS public abstract interface net.sf.cglib.proxy.NoOp
fld public final static net.sf.cglib.proxy.NoOp INSTANCE
intf net.sf.cglib.proxy.Callback

CLSS public net.sf.cglib.proxy.Proxy
cons protected init(net.sf.cglib.proxy.InvocationHandler)
fld protected net.sf.cglib.proxy.InvocationHandler h
intf java.io.Serializable
meth public static boolean isProxyClass(java.lang.Class)
meth public static java.lang.Class getProxyClass(java.lang.ClassLoader,java.lang.Class[])
meth public static java.lang.Object newProxyInstance(java.lang.ClassLoader,java.lang.Class[],net.sf.cglib.proxy.InvocationHandler)
meth public static net.sf.cglib.proxy.InvocationHandler getInvocationHandler(java.lang.Object)
supr java.lang.Object
hfds BAD_OBJECT_METHOD_FILTER,class$net$sf$cglib$proxy$InvocationHandler,class$net$sf$cglib$proxy$NoOp,class$net$sf$cglib$proxy$Proxy$ProxyImpl
hcls ProxyImpl

CLSS public abstract interface net.sf.cglib.proxy.ProxyRefDispatcher
intf net.sf.cglib.proxy.Callback
meth public abstract java.lang.Object loadObject(java.lang.Object) throws java.lang.Exception

CLSS public net.sf.cglib.proxy.UndeclaredThrowableException
cons public init(java.lang.Throwable)
meth public java.lang.Throwable getUndeclaredThrowable()
supr net.sf.cglib.core.CodeGenerationException

CLSS public abstract net.sf.cglib.reflect.ConstructorDelegate
cons protected init()
innr public static Generator
meth public static net.sf.cglib.reflect.ConstructorDelegate create(java.lang.Class,java.lang.Class)
supr java.lang.Object
hfds KEY_FACTORY,class$net$sf$cglib$reflect$ConstructorDelegate,class$net$sf$cglib$reflect$ConstructorDelegate$ConstructorKey
hcls ConstructorKey

CLSS public static net.sf.cglib.reflect.ConstructorDelegate$Generator
 outer net.sf.cglib.reflect.ConstructorDelegate
cons public init()
meth protected java.lang.ClassLoader getDefaultClassLoader()
meth protected java.lang.Object firstInstance(java.lang.Class)
meth protected java.lang.Object nextInstance(java.lang.Object)
meth public net.sf.cglib.reflect.ConstructorDelegate create()
meth public void generateClass(org.objectweb.asm.ClassVisitor)
meth public void setInterface(java.lang.Class)
meth public void setTargetClass(java.lang.Class)
supr net.sf.cglib.core.AbstractClassGenerator
hfds CONSTRUCTOR_DELEGATE,SOURCE,iface,targetClass

CLSS public abstract net.sf.cglib.reflect.FastClass
cons protected init()
cons protected init(java.lang.Class)
innr public static Generator
meth protected static java.lang.String getSignatureWithoutReturnType(java.lang.String,java.lang.Class[])
meth public abstract int getIndex(java.lang.Class[])
meth public abstract int getIndex(java.lang.String,java.lang.Class[])
meth public abstract int getIndex(net.sf.cglib.core.Signature)
meth public abstract int getMaxIndex()
meth public abstract java.lang.Object invoke(int,java.lang.Object,java.lang.Object[]) throws java.lang.reflect.InvocationTargetException
meth public abstract java.lang.Object newInstance(int,java.lang.Object[]) throws java.lang.reflect.InvocationTargetException
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Class getJavaClass()
meth public java.lang.Object invoke(java.lang.String,java.lang.Class[],java.lang.Object,java.lang.Object[]) throws java.lang.reflect.InvocationTargetException
meth public java.lang.Object newInstance() throws java.lang.reflect.InvocationTargetException
meth public java.lang.Object newInstance(java.lang.Class[],java.lang.Object[]) throws java.lang.reflect.InvocationTargetException
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public net.sf.cglib.reflect.FastConstructor getConstructor(java.lang.Class[])
meth public net.sf.cglib.reflect.FastConstructor getConstructor(java.lang.reflect.Constructor)
meth public net.sf.cglib.reflect.FastMethod getMethod(java.lang.String,java.lang.Class[])
meth public net.sf.cglib.reflect.FastMethod getMethod(java.lang.reflect.Method)
meth public static net.sf.cglib.reflect.FastClass create(java.lang.Class)
meth public static net.sf.cglib.reflect.FastClass create(java.lang.ClassLoader,java.lang.Class)
supr java.lang.Object
hfds class$java$lang$Class,class$net$sf$cglib$reflect$FastClass,type

CLSS public static net.sf.cglib.reflect.FastClass$Generator
 outer net.sf.cglib.reflect.FastClass
cons public init()
meth protected java.lang.ClassLoader getDefaultClassLoader()
meth protected java.lang.Object firstInstance(java.lang.Class)
meth protected java.lang.Object nextInstance(java.lang.Object)
meth public net.sf.cglib.reflect.FastClass create()
meth public void generateClass(org.objectweb.asm.ClassVisitor) throws java.lang.Exception
meth public void setType(java.lang.Class)
supr net.sf.cglib.core.AbstractClassGenerator
hfds SOURCE,type

CLSS public net.sf.cglib.reflect.FastConstructor
meth public java.lang.Class[] getExceptionTypes()
meth public java.lang.Class[] getParameterTypes()
meth public java.lang.Object newInstance() throws java.lang.reflect.InvocationTargetException
meth public java.lang.Object newInstance(java.lang.Object[]) throws java.lang.reflect.InvocationTargetException
meth public java.lang.reflect.Constructor getJavaConstructor()
supr net.sf.cglib.reflect.FastMember

CLSS public abstract net.sf.cglib.reflect.FastMember
cons protected init(net.sf.cglib.reflect.FastClass,java.lang.reflect.Member,int)
fld protected int index
fld protected java.lang.reflect.Member member
fld protected net.sf.cglib.reflect.FastClass fc
meth public abstract java.lang.Class[] getExceptionTypes()
meth public abstract java.lang.Class[] getParameterTypes()
meth public boolean equals(java.lang.Object)
meth public int getIndex()
meth public int getModifiers()
meth public int hashCode()
meth public java.lang.Class getDeclaringClass()
meth public java.lang.String getName()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public net.sf.cglib.reflect.FastMethod
meth public java.lang.Class getReturnType()
meth public java.lang.Class[] getExceptionTypes()
meth public java.lang.Class[] getParameterTypes()
meth public java.lang.Object invoke(java.lang.Object,java.lang.Object[]) throws java.lang.reflect.InvocationTargetException
meth public java.lang.reflect.Method getJavaMethod()
supr net.sf.cglib.reflect.FastMember

CLSS public abstract net.sf.cglib.reflect.MethodDelegate
cons public init()
fld protected java.lang.Object target
fld protected java.lang.String eqMethod
innr public static Generator
meth public abstract net.sf.cglib.reflect.MethodDelegate newInstance(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object getTarget()
meth public static net.sf.cglib.reflect.MethodDelegate create(java.lang.Object,java.lang.String,java.lang.Class)
meth public static net.sf.cglib.reflect.MethodDelegate createStatic(java.lang.Class,java.lang.String,java.lang.Class)
supr java.lang.Object
hfds KEY_FACTORY,class$net$sf$cglib$reflect$MethodDelegate,class$net$sf$cglib$reflect$MethodDelegate$MethodDelegateKey
hcls MethodDelegateKey

CLSS public static net.sf.cglib.reflect.MethodDelegate$Generator
 outer net.sf.cglib.reflect.MethodDelegate
cons public init()
meth protected java.lang.ClassLoader getDefaultClassLoader()
meth protected java.lang.Object firstInstance(java.lang.Class)
meth protected java.lang.Object nextInstance(java.lang.Object)
meth public net.sf.cglib.reflect.MethodDelegate create()
meth public void generateClass(org.objectweb.asm.ClassVisitor) throws java.lang.NoSuchMethodException
meth public void setInterface(java.lang.Class)
meth public void setMethodName(java.lang.String)
meth public void setTarget(java.lang.Object)
meth public void setTargetClass(java.lang.Class)
supr net.sf.cglib.core.AbstractClassGenerator
hfds METHOD_DELEGATE,NEW_INSTANCE,SOURCE,iface,methodName,target,targetClass

CLSS public abstract net.sf.cglib.reflect.MulticastDelegate
cons protected init()
fld protected java.lang.Object[] targets
innr public static Generator
intf java.lang.Cloneable
meth protected net.sf.cglib.reflect.MulticastDelegate addHelper(java.lang.Object)
meth public abstract net.sf.cglib.reflect.MulticastDelegate add(java.lang.Object)
meth public abstract net.sf.cglib.reflect.MulticastDelegate newInstance()
meth public java.util.List getTargets()
meth public net.sf.cglib.reflect.MulticastDelegate remove(java.lang.Object)
meth public static net.sf.cglib.reflect.MulticastDelegate create(java.lang.Class)
supr java.lang.Object
hfds class$net$sf$cglib$reflect$MulticastDelegate

CLSS public static net.sf.cglib.reflect.MulticastDelegate$Generator
 outer net.sf.cglib.reflect.MulticastDelegate
cons public init()
meth protected java.lang.ClassLoader getDefaultClassLoader()
meth protected java.lang.Object firstInstance(java.lang.Class)
meth protected java.lang.Object nextInstance(java.lang.Object)
meth public net.sf.cglib.reflect.MulticastDelegate create()
meth public void generateClass(org.objectweb.asm.ClassVisitor)
meth public void setInterface(java.lang.Class)
supr net.sf.cglib.core.AbstractClassGenerator
hfds ADD_DELEGATE,ADD_HELPER,MULTICAST_DELEGATE,NEW_INSTANCE,SOURCE,iface

CLSS public abstract net.sf.cglib.transform.AbstractClassFilterTransformer
hfds pass,target

CLSS public abstract net.sf.cglib.transform.AbstractClassLoader
cons protected init(java.lang.ClassLoader,java.lang.ClassLoader,net.sf.cglib.transform.ClassFilter)
meth protected int getFlags()
meth protected net.sf.cglib.core.ClassGenerator getGenerator(org.objectweb.asm.ClassReader)
meth protected org.objectweb.asm.Attribute[] attributes()
meth protected void postProcess(java.lang.Class)
meth public java.lang.Class loadClass(java.lang.String) throws java.lang.ClassNotFoundException
supr java.lang.ClassLoader
hfds DOMAIN,class$net$sf$cglib$transform$AbstractClassLoader,classPath,filter

CLSS public abstract net.sf.cglib.transform.AbstractClassTransformer

CLSS public abstract net.sf.cglib.transform.AbstractProcessTask
hfds filesets

CLSS public abstract net.sf.cglib.transform.AbstractTransformTask
hfds CLASS_MAGIC,ZIP_MAGIC,verbose

CLSS public net.sf.cglib.transform.AnnotationVisitorTee
hfds av1,av2

CLSS public abstract net.sf.cglib.transform.ClassEmitterTransformer

CLSS public abstract interface net.sf.cglib.transform.ClassFilter
meth public abstract boolean accept(java.lang.String)

CLSS public net.sf.cglib.transform.ClassFilterTransformer
hfds filter

CLSS public net.sf.cglib.transform.ClassReaderGenerator
cons public init(org.objectweb.asm.ClassReader,int)
cons public init(org.objectweb.asm.ClassReader,org.objectweb.asm.Attribute[],int)
intf net.sf.cglib.core.ClassGenerator
meth public void generateClass(org.objectweb.asm.ClassVisitor)
supr java.lang.Object
hfds attrs,flags,r

CLSS public abstract interface net.sf.cglib.transform.ClassTransformer

CLSS public net.sf.cglib.transform.ClassTransformerChain
hfds chain

CLSS public abstract interface net.sf.cglib.transform.ClassTransformerFactory
meth public abstract net.sf.cglib.transform.ClassTransformer newInstance()

CLSS public net.sf.cglib.transform.ClassTransformerTee
hfds branch

CLSS public net.sf.cglib.transform.ClassVisitorTee
hfds cv1,cv2

CLSS public net.sf.cglib.transform.FieldVisitorTee
hfds fv1,fv2

CLSS public abstract interface net.sf.cglib.transform.MethodFilter
meth public abstract boolean accept(int,java.lang.String,java.lang.String,java.lang.String,java.lang.String[])

CLSS public net.sf.cglib.transform.MethodFilterTransformer
hfds direct,filter,pass

CLSS public net.sf.cglib.transform.MethodVisitorTee
hfds mv1,mv2

CLSS public net.sf.cglib.transform.TransformingClassGenerator
cons public init(net.sf.cglib.core.ClassGenerator,net.sf.cglib.transform.ClassTransformer)
intf net.sf.cglib.core.ClassGenerator
meth public void generateClass(org.objectweb.asm.ClassVisitor) throws java.lang.Exception
supr java.lang.Object
hfds gen,t

CLSS public net.sf.cglib.transform.TransformingClassLoader
cons public init(java.lang.ClassLoader,net.sf.cglib.transform.ClassFilter,net.sf.cglib.transform.ClassTransformerFactory)
meth protected net.sf.cglib.core.ClassGenerator getGenerator(org.objectweb.asm.ClassReader)
supr net.sf.cglib.transform.AbstractClassLoader
hfds t

CLSS public net.sf.cglib.transform.impl.AbstractInterceptFieldCallback
cons public init()
intf net.sf.cglib.transform.impl.InterceptFieldCallback
meth public boolean readBoolean(java.lang.Object,java.lang.String,boolean)
meth public boolean writeBoolean(java.lang.Object,java.lang.String,boolean,boolean)
meth public byte readByte(java.lang.Object,java.lang.String,byte)
meth public byte writeByte(java.lang.Object,java.lang.String,byte,byte)
meth public char readChar(java.lang.Object,java.lang.String,char)
meth public char writeChar(java.lang.Object,java.lang.String,char,char)
meth public double readDouble(java.lang.Object,java.lang.String,double)
meth public double writeDouble(java.lang.Object,java.lang.String,double,double)
meth public float readFloat(java.lang.Object,java.lang.String,float)
meth public float writeFloat(java.lang.Object,java.lang.String,float,float)
meth public int readInt(java.lang.Object,java.lang.String,int)
meth public int writeInt(java.lang.Object,java.lang.String,int,int)
meth public java.lang.Object readObject(java.lang.Object,java.lang.String,java.lang.Object)
meth public java.lang.Object writeObject(java.lang.Object,java.lang.String,java.lang.Object,java.lang.Object)
meth public long readLong(java.lang.Object,java.lang.String,long)
meth public long writeLong(java.lang.Object,java.lang.String,long,long)
meth public short readShort(java.lang.Object,java.lang.String,short)
meth public short writeShort(java.lang.Object,java.lang.String,short,short)
supr java.lang.Object

CLSS public net.sf.cglib.transform.impl.AccessFieldTransformer
hfds callback

CLSS public abstract interface static net.sf.cglib.transform.impl.AccessFieldTransformer$Callback
 outer net.sf.cglib.transform.impl.AccessFieldTransformer
meth public abstract java.lang.String getPropertyName(org.objectweb.asm.Type,java.lang.String)

CLSS public net.sf.cglib.transform.impl.AddDelegateTransformer
hfds CSTRUCT_OBJECT,DELEGATE,class$java$lang$Object,delegateIf,delegateImpl,delegateType

CLSS public net.sf.cglib.transform.impl.AddInitTransformer
hfds info

CLSS public net.sf.cglib.transform.impl.AddPropertyTransformer
hfds names,types

CLSS public net.sf.cglib.transform.impl.AddStaticInitTransformer
hfds info

CLSS public abstract interface net.sf.cglib.transform.impl.FieldProvider
meth public abstract java.lang.Class[] getFieldTypes()
meth public abstract java.lang.Object getField(int)
meth public abstract java.lang.Object getField(java.lang.String)
meth public abstract java.lang.String[] getFieldNames()
meth public abstract void setField(int,java.lang.Object)
meth public abstract void setField(java.lang.String,java.lang.Object)

CLSS public net.sf.cglib.transform.impl.FieldProviderTransformer
hfds FIELD_NAMES,FIELD_PROVIDER,FIELD_TYPES,ILLEGAL_ARGUMENT_EXCEPTION,PROVIDER_GET,PROVIDER_GET_BY_INDEX,PROVIDER_GET_NAMES,PROVIDER_GET_TYPES,PROVIDER_SET,PROVIDER_SET_BY_INDEX,access,fields

CLSS public abstract interface net.sf.cglib.transform.impl.InterceptFieldCallback
meth public abstract boolean readBoolean(java.lang.Object,java.lang.String,boolean)
meth public abstract boolean writeBoolean(java.lang.Object,java.lang.String,boolean,boolean)
meth public abstract byte readByte(java.lang.Object,java.lang.String,byte)
meth public abstract byte writeByte(java.lang.Object,java.lang.String,byte,byte)
meth public abstract char readChar(java.lang.Object,java.lang.String,char)
meth public abstract char writeChar(java.lang.Object,java.lang.String,char,char)
meth public abstract double readDouble(java.lang.Object,java.lang.String,double)
meth public abstract double writeDouble(java.lang.Object,java.lang.String,double,double)
meth public abstract float readFloat(java.lang.Object,java.lang.String,float)
meth public abstract float writeFloat(java.lang.Object,java.lang.String,float,float)
meth public abstract int readInt(java.lang.Object,java.lang.String,int)
meth public abstract int writeInt(java.lang.Object,java.lang.String,int,int)
meth public abstract java.lang.Object readObject(java.lang.Object,java.lang.String,java.lang.Object)
meth public abstract java.lang.Object writeObject(java.lang.Object,java.lang.String,java.lang.Object,java.lang.Object)
meth public abstract long readLong(java.lang.Object,java.lang.String,long)
meth public abstract long writeLong(java.lang.Object,java.lang.String,long,long)
meth public abstract short readShort(java.lang.Object,java.lang.String,short)
meth public abstract short writeShort(java.lang.Object,java.lang.String,short,short)

CLSS public abstract interface net.sf.cglib.transform.impl.InterceptFieldEnabled
meth public abstract net.sf.cglib.transform.impl.InterceptFieldCallback getInterceptFieldCallback()
meth public abstract void setInterceptFieldCallback(net.sf.cglib.transform.impl.InterceptFieldCallback)

CLSS public abstract interface net.sf.cglib.transform.impl.InterceptFieldFilter
meth public abstract boolean acceptRead(org.objectweb.asm.Type,java.lang.String)
meth public abstract boolean acceptWrite(org.objectweb.asm.Type,java.lang.String)

CLSS public net.sf.cglib.transform.impl.InterceptFieldTransformer
hfds CALLBACK,CALLBACK_FIELD,ENABLED,ENABLED_GET,ENABLED_SET,filter

CLSS public net.sf.cglib.transform.impl.UndeclaredThrowableStrategy
cons public init(java.lang.Class)
meth protected net.sf.cglib.core.ClassGenerator transform(net.sf.cglib.core.ClassGenerator) throws java.lang.Exception
supr net.sf.cglib.core.DefaultGeneratorStrategy
hfds TRANSFORM_FILTER,t

CLSS public net.sf.cglib.transform.impl.UndeclaredThrowableTransformer
hfds class$java$lang$Throwable,wrapper

CLSS public abstract net.sf.cglib.util.ParallelSorter
cons protected init()
fld protected java.lang.Object[] a
innr public static Generator
meth protected abstract void swap(int,int)
meth protected int compare(int,int)
meth protected void mergeSort(int,int)
meth protected void quickSort(int,int)
meth public abstract net.sf.cglib.util.ParallelSorter newInstance(java.lang.Object[])
meth public static net.sf.cglib.util.ParallelSorter create(java.lang.Object[])
meth public void mergeSort(int)
meth public void mergeSort(int,int,int)
meth public void mergeSort(int,int,int,java.util.Comparator)
meth public void mergeSort(int,java.util.Comparator)
meth public void quickSort(int)
meth public void quickSort(int,int,int)
meth public void quickSort(int,int,int,java.util.Comparator)
meth public void quickSort(int,java.util.Comparator)
supr java.lang.Object
hfds class$net$sf$cglib$util$ParallelSorter,comparer
hcls ByteComparer,ComparatorComparer,Comparer,DoubleComparer,FloatComparer,IntComparer,LongComparer,ObjectComparer,ShortComparer

CLSS public static net.sf.cglib.util.ParallelSorter$Generator
 outer net.sf.cglib.util.ParallelSorter
cons public init()
meth protected java.lang.ClassLoader getDefaultClassLoader()
meth protected java.lang.Object firstInstance(java.lang.Class)
meth protected java.lang.Object nextInstance(java.lang.Object)
meth public net.sf.cglib.util.ParallelSorter create()
meth public void generateClass(org.objectweb.asm.ClassVisitor) throws java.lang.Exception
meth public void setArrays(java.lang.Object[])
supr net.sf.cglib.core.AbstractClassGenerator
hfds SOURCE,arrays

CLSS public abstract net.sf.cglib.util.StringSwitcher
cons protected init()
innr public static Generator
meth public abstract int intValue(java.lang.String)
meth public static net.sf.cglib.util.StringSwitcher create(java.lang.String[],int[],boolean)
supr java.lang.Object
hfds INT_VALUE,KEY_FACTORY,STRING_SWITCHER,class$net$sf$cglib$util$StringSwitcher,class$net$sf$cglib$util$StringSwitcher$StringSwitcherKey
hcls StringSwitcherKey

CLSS public static net.sf.cglib.util.StringSwitcher$Generator
 outer net.sf.cglib.util.StringSwitcher
cons public init()
meth protected java.lang.ClassLoader getDefaultClassLoader()
meth protected java.lang.Object firstInstance(java.lang.Class)
meth protected java.lang.Object nextInstance(java.lang.Object)
meth public net.sf.cglib.util.StringSwitcher create()
meth public void generateClass(org.objectweb.asm.ClassVisitor) throws java.lang.Exception
meth public void setFixedInput(boolean)
meth public void setInts(int[])
meth public void setStrings(java.lang.String[])
supr net.sf.cglib.core.AbstractClassGenerator
hfds SOURCE,fixedInput,ints,strings

