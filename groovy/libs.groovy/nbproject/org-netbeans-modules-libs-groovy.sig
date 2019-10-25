#Signature file v4.1
#Version 1.12

CLSS public abstract interface groovy.lang.AdaptingMetaClass
intf groovy.lang.MetaClass
meth public abstract groovy.lang.MetaClass getAdaptee()
meth public abstract void setAdaptee(groovy.lang.MetaClass)

CLSS public groovy.lang.BenchmarkInterceptor
cons public init()
fld protected java.util.Map calls
intf groovy.lang.Interceptor
meth public boolean doInvoke()
meth public java.lang.Object afterInvoke(java.lang.Object,java.lang.String,java.lang.Object[],java.lang.Object)
meth public java.lang.Object beforeInvoke(java.lang.Object,java.lang.String,java.lang.Object[])
meth public java.util.List statistic()
meth public java.util.Map getCalls()
meth public void reset()
supr java.lang.Object

CLSS public groovy.lang.Binding
cons public init()
cons public init(java.lang.String[])
cons public init(java.util.Map)
meth public boolean hasVariable(java.lang.String)
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object getVariable(java.lang.String)
meth public java.util.Map getVariables()
meth public void setProperty(java.lang.String,java.lang.Object)
meth public void setVariable(java.lang.String,java.lang.Object)
supr groovy.lang.GroovyObjectSupport
hfds variables

CLSS public abstract interface groovy.lang.Buildable
meth public abstract void build(groovy.lang.GroovyObject)

CLSS public abstract interface !annotation groovy.lang.Category
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class value()

CLSS public abstract groovy.lang.Closure<%0 extends java.lang.Object>
cons public init(java.lang.Object)
cons public init(java.lang.Object,java.lang.Object)
fld protected int maximumNumberOfParameters
fld protected java.lang.Class[] parameterTypes
fld public final static groovy.lang.Closure IDENTITY
fld public final static int DELEGATE_FIRST = 1
fld public final static int DELEGATE_ONLY = 3
fld public final static int DONE = 1
fld public final static int OWNER_FIRST = 0
fld public final static int OWNER_ONLY = 2
fld public final static int SKIP = 2
fld public final static int TO_SELF = 4
intf groovy.lang.GroovyCallable<{groovy.lang.Closure%0}>
intf java.io.Serializable
intf java.lang.Cloneable
intf java.lang.Runnable
meth protected static java.lang.Object throwRuntimeException(java.lang.Throwable)
meth public !varargs groovy.lang.Closure<{groovy.lang.Closure%0}> curry(java.lang.Object[])
meth public !varargs groovy.lang.Closure<{groovy.lang.Closure%0}> ncurry(int,java.lang.Object[])
meth public !varargs groovy.lang.Closure<{groovy.lang.Closure%0}> rcurry(java.lang.Object[])
meth public !varargs groovy.lang.Closure<{groovy.lang.Closure%0}> trampoline(java.lang.Object[])
meth public !varargs {groovy.lang.Closure%0} call(java.lang.Object[])
meth public <%0 extends java.lang.Object> groovy.lang.Closure<{%%0}> rightShift(groovy.lang.Closure<{%%0}>)
meth public boolean isCase(java.lang.Object)
meth public groovy.lang.Closure asWritable()
meth public groovy.lang.Closure<{groovy.lang.Closure%0}> curry(java.lang.Object)
meth public groovy.lang.Closure<{groovy.lang.Closure%0}> dehydrate()
meth public groovy.lang.Closure<{groovy.lang.Closure%0}> leftShift(groovy.lang.Closure)
meth public groovy.lang.Closure<{groovy.lang.Closure%0}> memoize()
meth public groovy.lang.Closure<{groovy.lang.Closure%0}> memoizeAtLeast(int)
meth public groovy.lang.Closure<{groovy.lang.Closure%0}> memoizeAtMost(int)
meth public groovy.lang.Closure<{groovy.lang.Closure%0}> memoizeBetween(int,int)
meth public groovy.lang.Closure<{groovy.lang.Closure%0}> ncurry(int,java.lang.Object)
meth public groovy.lang.Closure<{groovy.lang.Closure%0}> rcurry(java.lang.Object)
meth public groovy.lang.Closure<{groovy.lang.Closure%0}> rehydrate(java.lang.Object,java.lang.Object,java.lang.Object)
meth public groovy.lang.Closure<{groovy.lang.Closure%0}> trampoline()
meth public int getDirective()
meth public int getMaximumNumberOfParameters()
meth public int getResolveStrategy()
meth public java.lang.Class[] getParameterTypes()
meth public java.lang.Object clone()
meth public java.lang.Object getDelegate()
meth public java.lang.Object getOwner()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object getThisObject()
meth public void run()
meth public void setDelegate(java.lang.Object)
meth public void setDirective(int)
meth public void setProperty(java.lang.String,java.lang.Object)
meth public void setResolveStrategy(int)
meth public {groovy.lang.Closure%0} call()
meth public {groovy.lang.Closure%0} call(java.lang.Object)
meth public {groovy.lang.Closure%0} leftShift(java.lang.Object)
supr groovy.lang.GroovyObjectSupport
hfds EMPTY_OBJECT_ARRAY,bcw,delegate,directive,owner,resolveStrategy,serialVersionUID,thisObject
hcls WritableClosure

CLSS public groovy.lang.ClosureException
cons public init(groovy.lang.Closure,java.lang.Throwable)
meth public groovy.lang.Closure getClosure()
supr java.lang.RuntimeException
hfds closure,serialVersionUID

CLSS public abstract interface groovy.lang.ClosureInvokingMethod
meth public abstract boolean isStatic()
meth public abstract groovy.lang.Closure getClosure()
meth public abstract java.lang.String getName()

CLSS public abstract interface !annotation groovy.lang.Delegate
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD, METHOD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean allNames()
meth public abstract !hasdefault boolean deprecated()
meth public abstract !hasdefault boolean interfaces()
meth public abstract !hasdefault boolean methodAnnotations()
meth public abstract !hasdefault boolean parameterAnnotations()
meth public abstract !hasdefault java.lang.Class[] excludeTypes()
meth public abstract !hasdefault java.lang.Class[] includeTypes()
meth public abstract !hasdefault java.lang.String[] excludes()
meth public abstract !hasdefault java.lang.String[] includes()

CLSS public abstract interface !annotation groovy.lang.DelegatesTo
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
innr public abstract interface static !annotation Target
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int genericTypeIndex()
meth public abstract !hasdefault int strategy()
meth public abstract !hasdefault java.lang.Class value()
meth public abstract !hasdefault java.lang.String target()
meth public abstract !hasdefault java.lang.String type()

CLSS public abstract interface static !annotation groovy.lang.DelegatesTo$Target
 outer groovy.lang.DelegatesTo
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[PARAMETER])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String value()

CLSS public groovy.lang.DelegatingMetaClass
cons public init(groovy.lang.MetaClass)
cons public init(java.lang.Class)
fld protected groovy.lang.MetaClass delegate
intf groovy.lang.GroovyObject
intf groovy.lang.MetaClass
intf groovy.lang.MutableMetaClass
meth public boolean equals(java.lang.Object)
meth public boolean isGroovyObject()
meth public boolean isModified()
meth public groovy.lang.MetaClass getAdaptee()
meth public groovy.lang.MetaClass getMetaClass()
meth public groovy.lang.MetaMethod getMetaMethod(java.lang.String,java.lang.Object[])
meth public groovy.lang.MetaMethod getStaticMetaMethod(java.lang.String,java.lang.Class[])
meth public groovy.lang.MetaMethod getStaticMetaMethod(java.lang.String,java.lang.Object[])
meth public groovy.lang.MetaMethod pickMethod(java.lang.String,java.lang.Class[])
 anno 0 java.lang.Deprecated()
meth public groovy.lang.MetaProperty getMetaProperty(java.lang.String)
meth public groovy.lang.MetaProperty hasProperty(java.lang.Object,java.lang.String)
meth public int hashCode()
meth public int selectConstructorAndTransformArguments(int,java.lang.Object[])
meth public java.lang.Class getTheClass()
meth public java.lang.Object getAttribute(java.lang.Class,java.lang.Object,java.lang.String,boolean)
meth public java.lang.Object getAttribute(java.lang.Object,java.lang.String)
meth public java.lang.Object getProperty(java.lang.Class,java.lang.Object,java.lang.String,boolean,boolean)
meth public java.lang.Object getProperty(java.lang.Object,java.lang.String)
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object invokeConstructor(java.lang.Object[])
meth public java.lang.Object invokeMethod(java.lang.Class,java.lang.Object,java.lang.String,java.lang.Object[],boolean,boolean)
meth public java.lang.Object invokeMethod(java.lang.Object,java.lang.String,java.lang.Object)
meth public java.lang.Object invokeMethod(java.lang.Object,java.lang.String,java.lang.Object[])
meth public java.lang.Object invokeMethod(java.lang.String,java.lang.Object)
meth public java.lang.Object invokeMissingMethod(java.lang.Object,java.lang.String,java.lang.Object[])
meth public java.lang.Object invokeMissingProperty(java.lang.Object,java.lang.String,java.lang.Object,boolean)
meth public java.lang.Object invokeStaticMethod(java.lang.Object,java.lang.String,java.lang.Object[])
meth public java.lang.String toString()
meth public java.util.List<groovy.lang.MetaMethod> getMetaMethods()
meth public java.util.List<groovy.lang.MetaMethod> getMethods()
meth public java.util.List<groovy.lang.MetaMethod> respondsTo(java.lang.Object,java.lang.String)
meth public java.util.List<groovy.lang.MetaMethod> respondsTo(java.lang.Object,java.lang.String,java.lang.Object[])
meth public java.util.List<groovy.lang.MetaProperty> getProperties()
meth public org.codehaus.groovy.ast.ClassNode getClassNode()
meth public void addMetaBeanProperty(groovy.lang.MetaBeanProperty)
meth public void addMetaMethod(groovy.lang.MetaMethod)
meth public void addNewInstanceMethod(java.lang.reflect.Method)
meth public void addNewStaticMethod(java.lang.reflect.Method)
meth public void initialize()
meth public void setAdaptee(groovy.lang.MetaClass)
meth public void setAttribute(java.lang.Class,java.lang.Object,java.lang.String,java.lang.Object,boolean,boolean)
meth public void setAttribute(java.lang.Object,java.lang.String,java.lang.Object)
meth public void setMetaClass(groovy.lang.MetaClass)
meth public void setProperty(java.lang.Class,java.lang.Object,java.lang.String,java.lang.Object,boolean,boolean)
meth public void setProperty(java.lang.Object,java.lang.String,java.lang.Object)
meth public void setProperty(java.lang.String,java.lang.Object)
supr java.lang.Object

CLSS public groovy.lang.DeprecationException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public groovy.lang.EmptyRange<%0 extends java.lang.Comparable>
cons public init({groovy.lang.EmptyRange%0})
fld protected {groovy.lang.EmptyRange%0} at
intf groovy.lang.Range<{groovy.lang.EmptyRange%0}>
meth public boolean add({groovy.lang.EmptyRange%0})
meth public boolean addAll(int,java.util.Collection<? extends {groovy.lang.EmptyRange%0}>)
meth public boolean addAll(java.util.Collection<? extends {groovy.lang.EmptyRange%0}>)
meth public boolean containsWithinBounds(java.lang.Object)
meth public boolean isReverse()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean retainAll(java.util.Collection<?>)
meth public int size()
meth public java.lang.String inspect()
meth public java.lang.String toString()
meth public java.util.List<{groovy.lang.EmptyRange%0}> step(int)
meth public void step(int,groovy.lang.Closure)
meth public {groovy.lang.EmptyRange%0} get(int)
meth public {groovy.lang.EmptyRange%0} getFrom()
meth public {groovy.lang.EmptyRange%0} getTo()
meth public {groovy.lang.EmptyRange%0} remove(int)
meth public {groovy.lang.EmptyRange%0} set(int,{groovy.lang.EmptyRange%0})
supr java.util.AbstractList<{groovy.lang.EmptyRange%0}>

CLSS public groovy.lang.ExpandoMetaClass
cons public init(groovy.lang.MetaClassRegistry,java.lang.Class,boolean,boolean,groovy.lang.MetaMethod[])
cons public init(java.lang.Class)
cons public init(java.lang.Class,boolean)
cons public init(java.lang.Class,boolean,boolean)
cons public init(java.lang.Class,boolean,boolean,groovy.lang.MetaMethod[])
cons public init(java.lang.Class,boolean,groovy.lang.MetaMethod[])
cons public init(java.lang.Class,groovy.lang.MetaMethod[])
fld public boolean inRegistry
fld public final static java.lang.String CONSTRUCTOR = "constructor"
fld public final static java.lang.String STATIC_QUALIFIER = "static"
innr protected ExpandoMetaConstructor
innr protected ExpandoMetaProperty
intf groovy.lang.GroovyObject
meth protected boolean isInitialized()
meth protected java.lang.Object getSubclassMetaMethods(java.lang.String)
meth protected void checkInitalised()
meth protected void onGetPropertyFoundInHierarchy(groovy.lang.MetaMethod)
meth protected void onInvokeMethodFoundInHierarchy(groovy.lang.MetaMethod)
meth protected void onSetPropertyFoundInHierarchy(groovy.lang.MetaMethod)
meth protected void onSuperMethodFoundInHierarchy(groovy.lang.MetaMethod)
meth protected void onSuperPropertyFoundInHierarchy(groovy.lang.MetaBeanProperty)
meth protected void performOperationOnMetaClass(groovy.lang.ExpandoMetaClass$Callable)
meth protected void registerStaticMethod(java.lang.String,groovy.lang.Closure)
meth protected void registerStaticMethod(java.lang.String,groovy.lang.Closure,java.lang.Class[])
meth protected void setInitialized(boolean)
meth public boolean hasCustomStaticInvokeMethod()
meth public boolean hasMetaMethod(java.lang.String,java.lang.Class[])
meth public boolean hasMetaProperty(java.lang.String)
meth public boolean isModified()
meth public boolean isSetter(java.lang.String,org.codehaus.groovy.reflection.CachedClass[])
meth public groovy.lang.ExpandoMetaClass define(groovy.lang.Closure)
meth public groovy.lang.MetaClass getMetaClass()
meth public groovy.lang.MetaMethod findMixinMethod(java.lang.String,java.lang.Class[])
meth public groovy.lang.MetaMethod retrieveConstructor(java.lang.Object[])
meth public groovy.lang.MetaProperty getMetaProperty(java.lang.String)
meth public java.lang.Class getJavaClass()
meth public java.lang.Object castToMixedType(java.lang.Object,java.lang.Class)
meth public java.lang.Object getProperty(java.lang.Class,java.lang.Object,java.lang.String,boolean,boolean)
meth public java.lang.Object getProperty(java.lang.Object,java.lang.String)
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object invokeConstructor(java.lang.Object[])
meth public java.lang.Object invokeMethod(java.lang.Class,java.lang.Object,java.lang.String,java.lang.Object[],boolean,boolean)
meth public java.lang.Object invokeMethod(java.lang.String,java.lang.Object)
meth public java.lang.Object invokeStaticMethod(java.lang.Object,java.lang.String,java.lang.Object[])
meth public java.lang.String getPropertyForSetter(java.lang.String)
meth public java.util.Collection getExpandoSubclassMethods()
meth public java.util.Collection<groovy.lang.MetaProperty> getExpandoProperties()
meth public java.util.List<groovy.lang.MetaMethod> getExpandoMethods()
meth public java.util.List<groovy.lang.MetaMethod> getMethods()
meth public java.util.List<groovy.lang.MetaProperty> getProperties()
meth public org.codehaus.groovy.runtime.callsite.CallSite createConstructorSite(org.codehaus.groovy.runtime.callsite.CallSite,java.lang.Object[])
meth public org.codehaus.groovy.runtime.callsite.CallSite createPogoCallCurrentSite(org.codehaus.groovy.runtime.callsite.CallSite,java.lang.Class,java.lang.String,java.lang.Object[])
meth public org.codehaus.groovy.runtime.callsite.CallSite createPogoCallSite(org.codehaus.groovy.runtime.callsite.CallSite,java.lang.Object[])
meth public org.codehaus.groovy.runtime.callsite.CallSite createPojoCallSite(org.codehaus.groovy.runtime.callsite.CallSite,java.lang.Object,java.lang.Object[])
meth public org.codehaus.groovy.runtime.callsite.CallSite createStaticSite(org.codehaus.groovy.runtime.callsite.CallSite,java.lang.Object[])
meth public static boolean isValidExpandoProperty(java.lang.String)
meth public static void disableGlobally()
meth public static void enableGlobally()
meth public void addMixinClass(org.codehaus.groovy.reflection.MixinInMetaClass)
meth public void initialize()
meth public void refreshInheritedMethods(java.util.Set)
meth public void registerBeanProperty(java.lang.String,java.lang.Object)
meth public void registerInstanceMethod(groovy.lang.MetaMethod)
meth public void registerInstanceMethod(java.lang.String,groovy.lang.Closure)
meth public void registerSubclassInstanceMethod(groovy.lang.MetaMethod)
meth public void registerSubclassInstanceMethod(java.lang.String,java.lang.Class,groovy.lang.Closure)
meth public void setMetaClass(groovy.lang.MetaClass)
meth public void setProperty(java.lang.Class,java.lang.Object,java.lang.String,java.lang.Object,boolean,boolean)
meth public void setProperty(java.lang.String,java.lang.Object)
supr groovy.lang.MetaClassImpl
hfds CLASS,CLASS_PROPERTY,EMPTY_CLASS_ARRAY,GROOVY_CONSTRUCTOR,META_CLASS,META_CLASS_PROPERTY,META_METHODS,METHODS,PROPERTIES,allowChangesAfterInit,beanPropertyCache,expandoMethods,expandoProperties,expandoSubclassMethods,inheritedMetaMethods,initCalled,initialized,invokeStaticMethodMethod,mixinClasses,modified,myMetaClass,readLock,rwl,staticBeanPropertyCache,writeLock
hcls Callable,DefiningClosure,MixedInAccessor,StaticDefiningClosure,SubClassDefiningClosure

CLSS protected groovy.lang.ExpandoMetaClass$ExpandoMetaConstructor
 outer groovy.lang.ExpandoMetaClass
cons protected init(groovy.lang.ExpandoMetaClass)
meth public java.lang.Object leftShift(groovy.lang.Closure)
supr groovy.lang.GroovyObjectSupport

CLSS protected groovy.lang.ExpandoMetaClass$ExpandoMetaProperty
 outer groovy.lang.ExpandoMetaClass
cons protected init(groovy.lang.ExpandoMetaClass,java.lang.String)
cons protected init(groovy.lang.ExpandoMetaClass,java.lang.String,boolean)
fld protected boolean isStatic
fld protected java.lang.String propertyName
meth public boolean isStatic()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object leftShift(java.lang.Object)
meth public java.lang.String getPropertyName()
meth public void setProperty(java.lang.String,java.lang.Object)
supr groovy.lang.GroovyObjectSupport

CLSS public groovy.lang.ExpandoMetaClassCreationHandle
cons public init()
fld public final static groovy.lang.ExpandoMetaClassCreationHandle instance
meth protected groovy.lang.MetaClass createNormalMetaClass(java.lang.Class,groovy.lang.MetaClassRegistry)
meth public boolean hasModifiedMetaClass(groovy.lang.ExpandoMetaClass)
meth public static void disable()
meth public static void enable()
meth public void registerModifiedMetaClass(groovy.lang.ExpandoMetaClass)
supr groovy.lang.MetaClassRegistry$MetaClassCreationHandle

CLSS public abstract groovy.lang.GString
cons public init(java.lang.Object)
cons public init(java.lang.Object[])
fld public final static groovy.lang.GString EMPTY
fld public final static java.lang.Object[] EMPTY_OBJECT_ARRAY
fld public final static java.lang.String[] EMPTY_STRING_ARRAY
intf groovy.lang.Buildable
intf groovy.lang.Writable
intf java.io.Serializable
intf java.lang.CharSequence
intf java.lang.Comparable
meth public abstract java.lang.String[] getStrings()
meth public boolean equals(groovy.lang.GString)
meth public boolean equals(java.lang.Object)
meth public byte[] getBytes()
meth public byte[] getBytes(java.lang.String) throws java.io.UnsupportedEncodingException
meth public char charAt(int)
meth public groovy.lang.GString plus(groovy.lang.GString)
meth public groovy.lang.GString plus(java.lang.String)
meth public int compareTo(java.lang.Object)
meth public int getValueCount()
meth public int hashCode()
meth public int length()
meth public java.io.Writer writeTo(java.io.Writer) throws java.io.IOException
meth public java.lang.CharSequence subSequence(int,int)
meth public java.lang.Object getValue(int)
meth public java.lang.Object invokeMethod(java.lang.String,java.lang.Object)
meth public java.lang.Object[] getValues()
meth public java.lang.String toString()
meth public java.util.regex.Pattern negate()
meth public void build(groovy.lang.GroovyObject)
supr groovy.lang.GroovyObjectSupport
hfds MKP,YIELD,serialVersionUID,values

CLSS public abstract interface groovy.lang.GeneratedGroovyProxy
meth public abstract java.lang.Object getProxyTarget()

CLSS public abstract interface !annotation groovy.lang.Grab
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PARAMETER, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean changing()
meth public abstract !hasdefault boolean force()
meth public abstract !hasdefault boolean initClass()
meth public abstract !hasdefault boolean transitive()
meth public abstract !hasdefault java.lang.String classifier()
meth public abstract !hasdefault java.lang.String conf()
meth public abstract !hasdefault java.lang.String ext()
meth public abstract !hasdefault java.lang.String group()
meth public abstract !hasdefault java.lang.String module()
meth public abstract !hasdefault java.lang.String type()
meth public abstract !hasdefault java.lang.String value()
meth public abstract !hasdefault java.lang.String version()

CLSS public abstract interface !annotation groovy.lang.GrabConfig
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PARAMETER, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean autoDownload()
meth public abstract !hasdefault boolean disableChecksums()
meth public abstract !hasdefault boolean initContextClassLoader()
meth public abstract !hasdefault boolean systemClassLoader()
meth public abstract !hasdefault java.lang.String[] systemProperties()

CLSS public abstract interface !annotation groovy.lang.GrabExclude
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PARAMETER, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String group()
meth public abstract !hasdefault java.lang.String module()
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface !annotation groovy.lang.GrabResolver
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PARAMETER, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean initClass()
meth public abstract !hasdefault boolean m2Compatible()
meth public abstract !hasdefault java.lang.String name()
meth public abstract !hasdefault java.lang.String root()
meth public abstract !hasdefault java.lang.String value()

CLSS public abstract interface !annotation groovy.lang.Grapes
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean initClass()
meth public abstract groovy.lang.Grab[] value()

CLSS public abstract interface groovy.lang.GroovyCallable<%0 extends java.lang.Object>
intf java.util.concurrent.Callable<{groovy.lang.GroovyCallable%0}>

CLSS public groovy.lang.GroovyClassLoader
cons public init()
cons public init(groovy.lang.GroovyClassLoader)
cons public init(java.lang.ClassLoader)
cons public init(java.lang.ClassLoader,org.codehaus.groovy.control.CompilerConfiguration)
cons public init(java.lang.ClassLoader,org.codehaus.groovy.control.CompilerConfiguration,boolean)
fld protected final java.util.Map<java.lang.String,java.lang.Class> classCache
fld protected final java.util.Map<java.lang.String,java.lang.Class> sourceCache
innr public static ClassCollector
innr public static InnerLoader
meth protected boolean isRecompilable(java.lang.Class)
meth protected boolean isSourceNewer(java.net.URL,java.lang.Class) throws java.io.IOException
meth protected groovy.lang.GroovyClassLoader$ClassCollector createCollector(org.codehaus.groovy.control.CompilationUnit,org.codehaus.groovy.control.SourceUnit)
meth protected java.lang.Class getClassCacheEntry(java.lang.String)
meth protected java.lang.Class loadClass(java.lang.String,boolean) throws java.lang.ClassNotFoundException
meth protected java.lang.Class recompile(java.net.URL,java.lang.String,java.lang.Class) throws java.io.IOException
meth protected java.lang.String[] getClassPath()
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
meth protected long getTimeStamp(java.lang.Class)
meth protected org.codehaus.groovy.control.CompilationUnit createCompilationUnit(org.codehaus.groovy.control.CompilerConfiguration,java.security.CodeSource)
meth protected void removeClassCacheEntry(java.lang.String)
meth protected void setClassCacheEntry(java.lang.Class)
meth public groovy.lang.GroovyResourceLoader getResourceLoader()
meth public java.lang.Boolean isShouldRecompile()
meth public java.lang.Class defineClass(java.lang.String,byte[])
meth public java.lang.Class defineClass(org.codehaus.groovy.ast.ClassNode,java.lang.String,java.lang.String)
meth public java.lang.Class loadClass(java.lang.String,boolean,boolean) throws java.lang.ClassNotFoundException
meth public java.lang.Class loadClass(java.lang.String,boolean,boolean,boolean) throws java.lang.ClassNotFoundException
meth public java.lang.Class parseClass(groovy.lang.GroovyCodeSource)
meth public java.lang.Class parseClass(groovy.lang.GroovyCodeSource,boolean)
meth public java.lang.Class parseClass(java.io.File) throws java.io.IOException
meth public java.lang.Class parseClass(java.io.InputStream,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.lang.Class parseClass(java.io.Reader,java.lang.String)
meth public java.lang.Class parseClass(java.lang.String)
meth public java.lang.Class parseClass(java.lang.String,java.lang.String)
meth public java.lang.Class<?> loadClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.lang.Class[] getLoadedClasses()
meth public java.lang.String generateScriptName()
meth public void addClasspath(java.lang.String)
meth public void addURL(java.net.URL)
meth public void clearCache()
meth public void close() throws java.io.IOException
meth public void setResourceLoader(groovy.lang.GroovyResourceLoader)
meth public void setShouldRecompile(java.lang.Boolean)
supr java.net.URLClassLoader
hfds EMPTY_CLASS_ARRAY,EMPTY_URL_ARRAY,config,recompile,resourceLoader,scriptNameCounter,sourceEncoding
hcls TimestampAdder

CLSS public static groovy.lang.GroovyClassLoader$ClassCollector
 outer groovy.lang.GroovyClassLoader
cons protected init(groovy.lang.GroovyClassLoader$InnerLoader,org.codehaus.groovy.control.CompilationUnit,org.codehaus.groovy.control.SourceUnit)
meth protected java.lang.Class createClass(byte[],org.codehaus.groovy.ast.ClassNode)
meth protected java.lang.Class onClassNode(groovyjarjarasm.asm.ClassWriter,org.codehaus.groovy.ast.ClassNode)
meth public groovy.lang.GroovyClassLoader getDefiningClassLoader()
meth public java.util.Collection getLoadedClasses()
meth public void call(groovyjarjarasm.asm.ClassVisitor,org.codehaus.groovy.ast.ClassNode)
supr org.codehaus.groovy.control.CompilationUnit$ClassgenCallback
hfds cl,generatedClass,loadedClasses,su,unit

CLSS public static groovy.lang.GroovyClassLoader$InnerLoader
 outer groovy.lang.GroovyClassLoader
cons public init(groovy.lang.GroovyClassLoader)
meth public groovy.lang.GroovyResourceLoader getResourceLoader()
meth public java.io.InputStream getResourceAsStream(java.lang.String)
meth public java.lang.Class loadClass(java.lang.String,boolean,boolean,boolean) throws java.lang.ClassNotFoundException
meth public java.lang.Class parseClass(groovy.lang.GroovyCodeSource,boolean)
meth public java.lang.Class[] getLoadedClasses()
meth public java.net.URL findResource(java.lang.String)
meth public java.net.URL getResource(java.lang.String)
meth public java.net.URL[] getURLs()
meth public java.util.Enumeration findResources(java.lang.String) throws java.io.IOException
meth public long getTimeStamp()
meth public void addClasspath(java.lang.String)
meth public void addURL(java.net.URL)
meth public void clearCache()
meth public void setResourceLoader(groovy.lang.GroovyResourceLoader)
supr groovy.lang.GroovyClassLoader
hfds delegate,timeStamp

CLSS public groovy.lang.GroovyCodeSource
cons public init(java.io.File) throws java.io.IOException
cons public init(java.io.File,java.lang.String) throws java.io.IOException
cons public init(java.io.Reader,java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(java.net.URI) throws java.io.IOException
cons public init(java.net.URL)
meth public boolean equals(java.lang.Object)
meth public boolean isCachable()
meth public int hashCode()
meth public java.io.File getFile()
meth public java.lang.String getName()
meth public java.lang.String getScriptText()
meth public java.net.URL getURL()
meth public java.security.CodeSource getCodeSource()
meth public void setCachable(boolean)
supr java.lang.Object
hfds cachable,certs,codeSource,file,name,scriptText,url

CLSS public abstract interface groovy.lang.GroovyInterceptable
intf groovy.lang.GroovyObject

CLSS public abstract interface groovy.lang.GroovyObject
meth public abstract groovy.lang.MetaClass getMetaClass()
meth public abstract java.lang.Object getProperty(java.lang.String)
meth public abstract java.lang.Object invokeMethod(java.lang.String,java.lang.Object)
meth public abstract void setMetaClass(groovy.lang.MetaClass)
meth public abstract void setProperty(java.lang.String,java.lang.Object)

CLSS public abstract groovy.lang.GroovyObjectSupport
cons public init()
intf groovy.lang.GroovyObject
meth public groovy.lang.MetaClass getMetaClass()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object invokeMethod(java.lang.String,java.lang.Object)
meth public void setMetaClass(groovy.lang.MetaClass)
meth public void setProperty(java.lang.String,java.lang.Object)
supr java.lang.Object
hfds metaClass

CLSS public abstract interface groovy.lang.GroovyResourceLoader
meth public abstract java.net.URL loadGroovySource(java.lang.String) throws java.net.MalformedURLException

CLSS public groovy.lang.GroovyRuntimeException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.String,org.codehaus.groovy.ast.ASTNode)
cons public init(java.lang.Throwable)
meth protected java.lang.String getLocationText()
meth public java.lang.String getMessage()
meth public java.lang.String getMessageWithoutLocationText()
meth public org.codehaus.groovy.ast.ASTNode getNode()
meth public org.codehaus.groovy.ast.ModuleNode getModule()
meth public void setModule(org.codehaus.groovy.ast.ModuleNode)
supr java.lang.RuntimeException
hfds module,node,serialVersionUID

CLSS public groovy.lang.GroovyShell
cons public init()
cons public init(groovy.lang.Binding)
cons public init(groovy.lang.Binding,org.codehaus.groovy.control.CompilerConfiguration)
cons public init(groovy.lang.GroovyShell)
cons public init(java.lang.ClassLoader)
cons public init(java.lang.ClassLoader,groovy.lang.Binding)
cons public init(java.lang.ClassLoader,groovy.lang.Binding,org.codehaus.groovy.control.CompilerConfiguration)
cons public init(java.lang.ClassLoader,org.codehaus.groovy.control.CompilerConfiguration)
cons public init(org.codehaus.groovy.control.CompilerConfiguration)
fld public final static java.lang.String DEFAULT_CODE_BASE = "/groovy/shell"
meth protected java.lang.String generateScriptName()
meth public groovy.lang.Binding getContext()
meth public groovy.lang.GroovyClassLoader getClassLoader()
meth public groovy.lang.Script parse(groovy.lang.GroovyCodeSource)
meth public groovy.lang.Script parse(java.io.File) throws java.io.IOException
meth public groovy.lang.Script parse(java.io.Reader)
meth public groovy.lang.Script parse(java.io.Reader,java.lang.String)
meth public groovy.lang.Script parse(java.lang.String)
meth public groovy.lang.Script parse(java.lang.String,java.lang.String)
meth public groovy.lang.Script parse(java.net.URI) throws java.io.IOException
meth public java.lang.Object evaluate(groovy.lang.GroovyCodeSource)
meth public java.lang.Object evaluate(java.io.File) throws java.io.IOException
meth public java.lang.Object evaluate(java.io.Reader)
meth public java.lang.Object evaluate(java.io.Reader,java.lang.String)
meth public java.lang.Object evaluate(java.lang.String)
meth public java.lang.Object evaluate(java.lang.String,java.lang.String)
meth public java.lang.Object evaluate(java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.Object evaluate(java.net.URI) throws java.io.IOException
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object getVariable(java.lang.String)
meth public java.lang.Object run(groovy.lang.GroovyCodeSource,java.lang.String[])
meth public java.lang.Object run(groovy.lang.GroovyCodeSource,java.util.List)
meth public java.lang.Object run(java.io.File,java.lang.String[]) throws java.io.IOException
meth public java.lang.Object run(java.io.File,java.util.List) throws java.io.IOException
meth public java.lang.Object run(java.io.Reader,java.lang.String,java.lang.String[])
meth public java.lang.Object run(java.io.Reader,java.lang.String,java.util.List)
meth public java.lang.Object run(java.lang.String,java.lang.String,java.lang.String[])
meth public java.lang.Object run(java.lang.String,java.lang.String,java.util.List)
meth public java.lang.Object run(java.net.URI,java.lang.String[]) throws java.io.IOException
meth public java.lang.Object run(java.net.URI,java.util.List) throws java.io.IOException
meth public static void main(java.lang.String[])
meth public void resetLoadedClasses()
meth public void setProperty(java.lang.String,java.lang.Object)
meth public void setVariable(java.lang.String,java.lang.Object)
supr groovy.lang.GroovyObjectSupport
hfds EMPTY_STRING_ARRAY,config,context,counter,loader

CLSS public final groovy.lang.GroovySystem
fld public final static java.util.Map<java.lang.String,org.apache.groovy.plugin.GroovyRunner> RUNNER_REGISTRY
 anno 0 java.lang.Deprecated()
meth public static boolean isKeepJavaMetaClasses()
meth public static boolean isUseReflection()
 anno 0 java.lang.Deprecated()
meth public static groovy.lang.MetaClassRegistry getMetaClassRegistry()
meth public static java.lang.String getVersion()
meth public static void setKeepJavaMetaClasses(boolean)
meth public static void stopThreadedReferenceManager()
supr java.lang.Object
hfds META_CLASS_REGISTRY,USE_REFLECTION,keepJavaMetaClasses

CLSS public groovy.lang.IllegalPropertyAccessException
cons public init(java.lang.String,java.lang.Class,int)
cons public init(java.lang.reflect.Field,java.lang.Class)
supr groovy.lang.MissingPropertyException
hfds serialVersionUID

CLSS public groovy.lang.IncorrectClosureArgumentsException
cons public init(groovy.lang.Closure,java.lang.Object,java.lang.Class[])
meth public groovy.lang.Closure getClosure()
meth public java.lang.Class[] getExpected()
meth public java.lang.Object getArguments()
supr groovy.lang.GroovyRuntimeException
hfds arguments,closure,expected,serialVersionUID

CLSS public groovy.lang.IntRange
cons protected init(int,int,boolean)
cons public init(boolean,int,int)
cons public init(int,int)
intf groovy.lang.Range<java.lang.Integer>
meth public <%0 extends java.lang.Number & java.lang.Comparable> groovy.lang.NumberRange by({%%0})
meth public boolean contains(java.lang.Object)
meth public boolean containsAll(java.util.Collection)
meth public boolean containsWithinBounds(java.lang.Object)
meth public boolean equals(groovy.lang.IntRange)
meth public boolean equals(java.lang.Object)
meth public boolean isReverse()
meth public int getFromInt()
meth public int getToInt()
meth public int hashCode()
meth public int size()
meth public java.lang.Boolean getInclusive()
meth public java.lang.Integer get(int)
meth public java.lang.Integer getFrom()
meth public java.lang.Integer getTo()
meth public java.lang.String inspect()
meth public java.lang.String toString()
meth public java.util.Iterator<java.lang.Integer> iterator()
meth public java.util.List<java.lang.Integer> step(int)
meth public java.util.List<java.lang.Integer> subList(int,int)
meth public org.codehaus.groovy.runtime.RangeInfo subListBorders(int)
meth public void step(int,groovy.lang.Closure)
supr java.util.AbstractList<java.lang.Integer>
hfds from,inclusive,reverse,to
hcls IntRangeIterator

CLSS public abstract interface groovy.lang.Interceptor
meth public abstract boolean doInvoke()
meth public abstract java.lang.Object afterInvoke(java.lang.Object,java.lang.String,java.lang.Object[],java.lang.Object)
meth public abstract java.lang.Object beforeInvoke(java.lang.Object,java.lang.String,java.lang.Object[])

CLSS public abstract interface !annotation groovy.lang.Lazy
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[FIELD])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean soft()

CLSS public final groovy.lang.ListWithDefault<%0 extends java.lang.Object>
intf java.util.List<{groovy.lang.ListWithDefault%0}>
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public boolean add({groovy.lang.ListWithDefault%0})
meth public boolean addAll(int,java.util.Collection<? extends {groovy.lang.ListWithDefault%0}>)
meth public boolean addAll(java.util.Collection<? extends {groovy.lang.ListWithDefault%0}>)
meth public boolean contains(java.lang.Object)
meth public boolean containsAll(java.util.Collection<?>)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public boolean isLazyDefaultValues()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean retainAll(java.util.Collection<?>)
meth public groovy.lang.Closure getInitClosure()
meth public groovy.lang.ListWithDefault<{groovy.lang.ListWithDefault%0}> subList(int,int)
meth public int hashCode()
meth public int indexOf(java.lang.Object)
meth public int lastIndexOf(java.lang.Object)
meth public int size()
meth public java.lang.Object[] toArray()
meth public java.util.Iterator<{groovy.lang.ListWithDefault%0}> iterator()
meth public java.util.List<{groovy.lang.ListWithDefault%0}> getDelegate()
meth public java.util.ListIterator<{groovy.lang.ListWithDefault%0}> listIterator()
meth public java.util.ListIterator<{groovy.lang.ListWithDefault%0}> listIterator(int)
meth public static <%0 extends java.lang.Object> groovy.lang.ListWithDefault<{%%0}> newInstance(java.util.List<{%%0}>,boolean,groovy.lang.Closure)
meth public void add(int,{groovy.lang.ListWithDefault%0})
meth public void clear()
meth public {groovy.lang.ListWithDefault%0} get(int)
meth public {groovy.lang.ListWithDefault%0} getAt(int)
meth public {groovy.lang.ListWithDefault%0} remove(int)
meth public {groovy.lang.ListWithDefault%0} set(int,{groovy.lang.ListWithDefault%0})
supr java.lang.Object
hfds delegate,initClosure,lazyDefaultValues

CLSS public final groovy.lang.MapWithDefault<%0 extends java.lang.Object, %1 extends java.lang.Object>
intf java.util.Map<{groovy.lang.MapWithDefault%0},{groovy.lang.MapWithDefault%1}>
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.util.Collection<{groovy.lang.MapWithDefault%1}> values()
meth public java.util.Set<java.util.Map$Entry<{groovy.lang.MapWithDefault%0},{groovy.lang.MapWithDefault%1}>> entrySet()
meth public java.util.Set<{groovy.lang.MapWithDefault%0}> keySet()
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> java.util.Map<{%%0},{%%1}> newInstance(java.util.Map<{%%0},{%%1}>,groovy.lang.Closure)
meth public void clear()
meth public void putAll(java.util.Map<? extends {groovy.lang.MapWithDefault%0},? extends {groovy.lang.MapWithDefault%1}>)
meth public {groovy.lang.MapWithDefault%1} get(java.lang.Object)
meth public {groovy.lang.MapWithDefault%1} put({groovy.lang.MapWithDefault%0},{groovy.lang.MapWithDefault%1})
meth public {groovy.lang.MapWithDefault%1} remove(java.lang.Object)
supr java.lang.Object
hfds delegate,initClosure

CLSS public groovy.lang.MetaArrayLengthProperty
cons public init()
meth public java.lang.Object getProperty(java.lang.Object)
meth public void setProperty(java.lang.Object,java.lang.Object)
supr groovy.lang.MetaProperty

CLSS public groovy.lang.MetaBeanProperty
cons public init(java.lang.String,java.lang.Class,groovy.lang.MetaMethod,groovy.lang.MetaMethod)
meth public groovy.lang.MetaMethod getGetter()
meth public groovy.lang.MetaMethod getSetter()
meth public int getModifiers()
meth public java.lang.Object getProperty(java.lang.Object)
meth public org.codehaus.groovy.reflection.CachedField getField()
meth public void setField(org.codehaus.groovy.reflection.CachedField)
meth public void setProperty(java.lang.Object,java.lang.Object)
supr groovy.lang.MetaProperty
hfds field,getter,setter

CLSS public abstract interface groovy.lang.MetaClass
intf groovy.lang.MetaObjectProtocol
meth public abstract groovy.lang.MetaMethod pickMethod(java.lang.String,java.lang.Class[])
meth public abstract int selectConstructorAndTransformArguments(int,java.lang.Object[])
meth public abstract java.lang.Object getAttribute(java.lang.Class,java.lang.Object,java.lang.String,boolean)
meth public abstract java.lang.Object getProperty(java.lang.Class,java.lang.Object,java.lang.String,boolean,boolean)
meth public abstract java.lang.Object invokeMethod(java.lang.Class,java.lang.Object,java.lang.String,java.lang.Object[],boolean,boolean)
meth public abstract java.lang.Object invokeMissingMethod(java.lang.Object,java.lang.String,java.lang.Object[])
meth public abstract java.lang.Object invokeMissingProperty(java.lang.Object,java.lang.String,java.lang.Object,boolean)
meth public abstract java.util.List<groovy.lang.MetaMethod> getMetaMethods()
meth public abstract java.util.List<groovy.lang.MetaMethod> getMethods()
meth public abstract java.util.List<groovy.lang.MetaProperty> getProperties()
meth public abstract org.codehaus.groovy.ast.ClassNode getClassNode()
meth public abstract void initialize()
meth public abstract void setAttribute(java.lang.Class,java.lang.Object,java.lang.String,java.lang.Object,boolean,boolean)
meth public abstract void setProperty(java.lang.Class,java.lang.Object,java.lang.String,java.lang.Object,boolean,boolean)

CLSS public groovy.lang.MetaClassImpl
cons public init(groovy.lang.MetaClassRegistry,java.lang.Class)
cons public init(groovy.lang.MetaClassRegistry,java.lang.Class,groovy.lang.MetaMethod[])
cons public init(java.lang.Class)
cons public init(java.lang.Class,groovy.lang.MetaMethod[])
fld protected final boolean isGroovyObject
fld protected final boolean isMap
fld protected final java.lang.Class theClass
fld protected final org.codehaus.groovy.reflection.CachedClass theCachedClass
fld protected final org.codehaus.groovy.runtime.metaclass.MetaMethodIndex metaMethodIndex
fld protected final static java.lang.String INVOKE_METHOD_METHOD = "invokeMethod"
fld protected final static java.lang.String METHOD_MISSING = "methodMissing"
fld protected final static java.lang.String PROPERTY_MISSING = "propertyMissing"
fld protected final static java.lang.String STATIC_METHOD_MISSING = "$static_methodMissing"
fld protected final static java.lang.String STATIC_PROPERTY_MISSING = "$static_propertyMissing"
fld protected groovy.lang.MetaClassRegistry registry
fld protected groovy.lang.MetaMethod getPropertyMethod
fld protected groovy.lang.MetaMethod invokeMethodMethod
fld protected groovy.lang.MetaMethod setPropertyMethod
fld public final static java.lang.Object[] EMPTY_ARGUMENTS
innr public final static MetaConstructor
innr public static Index
intf groovy.lang.MetaClass
intf groovy.lang.MutableMetaClass
meth protected boolean isInitialized()
meth protected final void checkIfGroovyObjectMethod(groovy.lang.MetaMethod)
meth protected groovy.lang.MetaBeanProperty findPropertyInClassHierarchy(java.lang.String,org.codehaus.groovy.reflection.CachedClass)
meth protected groovy.lang.MetaMethod createTransformMetaMethod(groovy.lang.MetaMethod)
meth protected groovy.lang.MetaMethod findMixinMethod(java.lang.String,java.lang.Class[])
meth protected java.lang.Object chooseMethod(java.lang.String,java.lang.Object,java.lang.Class[])
meth protected java.lang.Object getSubclassMetaMethods(java.lang.String)
meth protected java.lang.Object invokeStaticMissingProperty(java.lang.Object,java.lang.String,java.lang.Object,boolean)
meth protected java.util.LinkedList<org.codehaus.groovy.reflection.CachedClass> getSuperClasses()
meth protected static groovy.lang.MetaMethod findMethodInClassHierarchy(java.lang.Class,java.lang.String,java.lang.Class[],groovy.lang.MetaClass)
meth protected static groovy.lang.MetaMethod findOwnMethod(java.lang.Class,java.lang.String,java.lang.Class[],groovy.lang.MetaClass,groovy.lang.MetaMethod)
meth protected static java.lang.Object doChooseMostSpecificParams(java.lang.String,java.lang.String,java.util.List,java.lang.Class[],boolean)
meth protected static java.lang.String createErrorMessageForAmbiguity(java.lang.String,java.lang.String,java.lang.Class[],java.util.LinkedList)
meth protected static long handleMatches(long,java.util.LinkedList,java.lang.Object,long)
meth protected void addMetaMethodToIndex(groovy.lang.MetaMethod,org.codehaus.groovy.runtime.metaclass.MetaMethodIndex$Header)
meth protected void applyPropertyDescriptors(java.beans.PropertyDescriptor[])
meth protected void checkInitalised()
meth protected void clearInvocationCaches()
meth protected void dropMethodCache(java.lang.String)
meth protected void dropStaticMethodCache(java.lang.String)
meth protected void onGetPropertyFoundInHierarchy(groovy.lang.MetaMethod)
meth protected void onInvokeMethodFoundInHierarchy(groovy.lang.MetaMethod)
meth protected void onMixinMethodFound(groovy.lang.MetaMethod)
meth protected void onSetPropertyFoundInHierarchy(groovy.lang.MetaMethod)
meth protected void onSuperMethodFoundInHierarchy(groovy.lang.MetaMethod)
meth protected void onSuperPropertyFoundInHierarchy(groovy.lang.MetaBeanProperty)
meth public boolean hasCustomInvokeMethod()
meth public boolean hasCustomStaticInvokeMethod()
meth public boolean isGroovyObject()
meth public boolean isModified()
meth public final org.codehaus.groovy.reflection.CachedClass getTheCachedClass()
meth public groovy.lang.MetaClassRegistry getRegistry()
meth public groovy.lang.MetaMethod getMetaMethod(java.lang.String,java.lang.Object[])
meth public groovy.lang.MetaMethod getMethodWithCaching(java.lang.Class,java.lang.String,java.lang.Object[],boolean)
meth public groovy.lang.MetaMethod getMethodWithoutCaching(java.lang.Class,java.lang.String,java.lang.Class[],boolean)
meth public groovy.lang.MetaMethod getStaticMetaMethod(java.lang.String,java.lang.Object[])
meth public groovy.lang.MetaMethod pickMethod(java.lang.String,java.lang.Class[])
meth public groovy.lang.MetaMethod retrieveConstructor(java.lang.Object[])
meth public groovy.lang.MetaMethod retrieveStaticMethod(java.lang.String,java.lang.Object[])
meth public groovy.lang.MetaMethod[] getAdditionalMetaMethods()
meth public groovy.lang.MetaProperty getEffectiveGetMetaProperty(java.lang.Class,java.lang.Object,java.lang.String,boolean)
meth public groovy.lang.MetaProperty getMetaProperty(java.lang.String)
meth public groovy.lang.MetaProperty hasProperty(java.lang.Object,java.lang.String)
meth public int getVersion()
meth public int selectConstructorAndTransformArguments(int,java.lang.Object[])
meth public java.lang.Class getTheClass()
meth public java.lang.Object getAttribute(java.lang.Class,java.lang.Object,java.lang.String,boolean)
meth public java.lang.Object getAttribute(java.lang.Class,java.lang.Object,java.lang.String,boolean,boolean)
meth public java.lang.Object getAttribute(java.lang.Object,java.lang.String)
meth public java.lang.Object getProperty(java.lang.Class,java.lang.Object,java.lang.String,boolean,boolean)
meth public java.lang.Object getProperty(java.lang.Object,java.lang.String)
meth public java.lang.Object invokeConstructor(java.lang.Object[])
meth public java.lang.Object invokeMethod(java.lang.Class,java.lang.Object,java.lang.String,java.lang.Object[],boolean,boolean)
meth public java.lang.Object invokeMethod(java.lang.Object,java.lang.String,java.lang.Object)
meth public java.lang.Object invokeMethod(java.lang.Object,java.lang.String,java.lang.Object[])
meth public java.lang.Object invokeMissingMethod(java.lang.Object,java.lang.String,java.lang.Object[])
meth public java.lang.Object invokeMissingProperty(java.lang.Object,java.lang.String,java.lang.Object,boolean)
meth public java.lang.Object invokeStaticMethod(java.lang.Object,java.lang.String,java.lang.Object[])
meth public java.lang.String toString()
meth public java.lang.reflect.Constructor retrieveConstructor(java.lang.Class[])
meth public java.util.List respondsTo(java.lang.Object,java.lang.String)
meth public java.util.List respondsTo(java.lang.Object,java.lang.String,java.lang.Object[])
meth public java.util.List<groovy.lang.MetaMethod> getMetaMethods()
meth public java.util.List<groovy.lang.MetaMethod> getMethods()
meth public java.util.List<groovy.lang.MetaProperty> getProperties()
meth public org.codehaus.groovy.ast.ClassNode getClassNode()
meth public org.codehaus.groovy.reflection.ClassInfo getClassInfo()
meth public org.codehaus.groovy.runtime.callsite.CallSite createConstructorSite(org.codehaus.groovy.runtime.callsite.CallSite,java.lang.Object[])
meth public org.codehaus.groovy.runtime.callsite.CallSite createPogoCallCurrentSite(org.codehaus.groovy.runtime.callsite.CallSite,java.lang.Class,java.lang.Object[])
meth public org.codehaus.groovy.runtime.callsite.CallSite createPogoCallSite(org.codehaus.groovy.runtime.callsite.CallSite,java.lang.Object[])
meth public org.codehaus.groovy.runtime.callsite.CallSite createPojoCallSite(org.codehaus.groovy.runtime.callsite.CallSite,java.lang.Object,java.lang.Object[])
meth public org.codehaus.groovy.runtime.callsite.CallSite createStaticSite(org.codehaus.groovy.runtime.callsite.CallSite,java.lang.Object[])
meth public void addMetaBeanProperty(groovy.lang.MetaBeanProperty)
meth public void addMetaMethod(groovy.lang.MetaMethod)
meth public void addNewInstanceMethod(java.lang.reflect.Method)
meth public void addNewStaticMethod(java.lang.reflect.Method)
meth public void incVersion()
meth public void initialize()
meth public void setAttribute(java.lang.Class,java.lang.Object,java.lang.String,java.lang.Object,boolean,boolean)
meth public void setAttribute(java.lang.Object,java.lang.String,java.lang.Object)
meth public void setProperties(java.lang.Object,java.util.Map)
meth public void setProperty(java.lang.Class,java.lang.Object,java.lang.String,java.lang.Object,boolean,boolean)
meth public void setProperty(java.lang.Object,java.lang.String,java.lang.Object)
supr java.lang.Object
hfds AMBIGUOUS_LISTENER_METHOD,CACHED_CLASS_NAME_COMPARATOR,CLOSURE_CALL_METHOD,CLOSURE_DO_CALL_METHOD,EMPTY,GETTER_MISSING_ARGS,GET_PROPERTY_METHOD,METHOD_INDEX_COPIER,METHOD_MISSING_ARGS,NAME_INDEX_COPIER,PROP_NAMES,SETTER_MISSING_ARGS,SET_PROPERTY_METHOD,additionalMetaMethods,allMethods,arrayLengthProperty,classNode,classPropertyIndex,classPropertyIndexForSuper,constructors,genericGetMethod,genericSetMethod,initialized,listeners,mainClassMethodHeader,methodMissing,myNewMetaMethods,newGroovyMethodsSet,propertyMissingGet,propertyMissingSet,staticPropertyIndex
hcls DummyMetaMethod,MethodIndex,MethodIndexAction

CLSS public static groovy.lang.MetaClassImpl$Index
 outer groovy.lang.MetaClassImpl
cons public init()
cons public init(boolean)
cons public init(int)
meth public boolean checkEquals(org.codehaus.groovy.util.ComplexKeyHashMap$Entry,java.lang.Object)
meth public org.codehaus.groovy.util.SingleKeyHashMap getNotNull(org.codehaus.groovy.reflection.CachedClass)
meth public org.codehaus.groovy.util.SingleKeyHashMap getNullable(org.codehaus.groovy.reflection.CachedClass)
meth public void put(org.codehaus.groovy.reflection.CachedClass,org.codehaus.groovy.util.SingleKeyHashMap)
supr org.codehaus.groovy.util.SingleKeyHashMap

CLSS public final static groovy.lang.MetaClassImpl$MetaConstructor
 outer groovy.lang.MetaClassImpl
meth public boolean isBeanConstructor()
meth public int getModifiers()
meth public java.lang.Class getReturnType()
meth public java.lang.Object invoke(java.lang.Object,java.lang.Object[])
meth public java.lang.String getName()
meth public org.codehaus.groovy.reflection.CachedClass getDeclaringClass()
meth public org.codehaus.groovy.reflection.CachedConstructor getCachedConstrcutor()
supr groovy.lang.MetaMethod
hfds beanConstructor,cc

CLSS public abstract interface groovy.lang.MetaClassRegistry
innr public static MetaClassCreationHandle
meth public abstract groovy.lang.MetaClass getMetaClass(java.lang.Class)
meth public abstract groovy.lang.MetaClassRegistry$MetaClassCreationHandle getMetaClassCreationHandler()
meth public abstract groovy.lang.MetaClassRegistryChangeEventListener[] getMetaClassRegistryChangeEventListeners()
meth public abstract java.util.Iterator iterator()
meth public abstract void addMetaClassRegistryChangeEventListener(groovy.lang.MetaClassRegistryChangeEventListener)
meth public abstract void addNonRemovableMetaClassRegistryChangeEventListener(groovy.lang.MetaClassRegistryChangeEventListener)
meth public abstract void removeMetaClass(java.lang.Class)
meth public abstract void removeMetaClassRegistryChangeEventListener(groovy.lang.MetaClassRegistryChangeEventListener)
meth public abstract void setMetaClass(java.lang.Class,groovy.lang.MetaClass)
meth public abstract void setMetaClassCreationHandle(groovy.lang.MetaClassRegistry$MetaClassCreationHandle)

CLSS public static groovy.lang.MetaClassRegistry$MetaClassCreationHandle
 outer groovy.lang.MetaClassRegistry
cons public init()
meth protected groovy.lang.MetaClass createNormalMetaClass(java.lang.Class,groovy.lang.MetaClassRegistry)
meth public boolean isDisableCustomMetaClassLookup()
meth public final groovy.lang.MetaClass create(java.lang.Class,groovy.lang.MetaClassRegistry)
meth public void setDisableCustomMetaClassLookup(boolean)
supr java.lang.Object
hfds disableCustomMetaClassLookup

CLSS public groovy.lang.MetaClassRegistryChangeEvent
cons public init(java.lang.Object,java.lang.Object,java.lang.Class,groovy.lang.MetaClass,groovy.lang.MetaClass)
meth public boolean isPerInstanceMetaClassChange()
meth public groovy.lang.MetaClass getNewMetaClass()
meth public groovy.lang.MetaClass getOldMetaClass()
meth public groovy.lang.MetaClassRegistry getRegistry()
meth public java.lang.Class getClassToUpdate()
meth public java.lang.Object getInstance()
supr java.util.EventObject
hfds clazz,instance,metaClass,oldMetaClass,serialVersionUID

CLSS public abstract interface groovy.lang.MetaClassRegistryChangeEventListener
intf java.util.EventListener
meth public abstract void updateConstantMetaClass(groovy.lang.MetaClassRegistryChangeEvent)

CLSS public groovy.lang.MetaExpandoProperty
cons public init(java.util.Map$Entry)
meth public java.lang.Object getProperty(java.lang.Object)
meth public void setProperty(java.lang.Object,java.lang.Object)
supr groovy.lang.MetaProperty
hfds value

CLSS public abstract groovy.lang.MetaMethod
cons public init()
cons public init(java.lang.Class[])
fld public final static groovy.lang.MetaMethod[] EMPTY_ARRAY
intf java.lang.Cloneable
meth protected static boolean equal(org.codehaus.groovy.reflection.CachedClass[],java.lang.Class[])
meth protected static boolean equal(org.codehaus.groovy.reflection.CachedClass[],org.codehaus.groovy.reflection.CachedClass[])
meth public abstract int getModifiers()
meth public abstract java.lang.Class getReturnType()
meth public abstract java.lang.Object invoke(java.lang.Object,java.lang.Object[])
meth public abstract java.lang.String getName()
meth public abstract org.codehaus.groovy.reflection.CachedClass getDeclaringClass()
meth public boolean isAbstract()
meth public boolean isCacheable()
meth public boolean isMethod(groovy.lang.MetaMethod)
meth public boolean isStatic()
meth public final boolean isPrivate()
meth public final boolean isProtected()
meth public final boolean isPublic()
meth public final boolean isSame(groovy.lang.MetaMethod)
meth public final java.lang.RuntimeException processDoMethodInvokeException(java.lang.Exception,java.lang.Object,java.lang.Object[])
meth public java.lang.Object clone()
meth public java.lang.Object doMethodInvoke(java.lang.Object,java.lang.Object[])
meth public java.lang.String getDescriptor()
meth public java.lang.String getMopName()
meth public java.lang.String getSignature()
meth public java.lang.String toString()
meth public void checkParameters(java.lang.Class[])
supr org.codehaus.groovy.reflection.ParameterTypes
hfds mopName,signature

CLSS public abstract interface groovy.lang.MetaObjectProtocol
meth public abstract groovy.lang.MetaMethod getMetaMethod(java.lang.String,java.lang.Object[])
meth public abstract groovy.lang.MetaMethod getStaticMetaMethod(java.lang.String,java.lang.Object[])
meth public abstract groovy.lang.MetaProperty getMetaProperty(java.lang.String)
meth public abstract groovy.lang.MetaProperty hasProperty(java.lang.Object,java.lang.String)
meth public abstract java.lang.Class getTheClass()
meth public abstract java.lang.Object getAttribute(java.lang.Object,java.lang.String)
meth public abstract java.lang.Object getProperty(java.lang.Object,java.lang.String)
meth public abstract java.lang.Object invokeConstructor(java.lang.Object[])
meth public abstract java.lang.Object invokeMethod(java.lang.Object,java.lang.String,java.lang.Object)
meth public abstract java.lang.Object invokeMethod(java.lang.Object,java.lang.String,java.lang.Object[])
meth public abstract java.lang.Object invokeStaticMethod(java.lang.Object,java.lang.String,java.lang.Object[])
meth public abstract java.util.List<groovy.lang.MetaMethod> getMethods()
meth public abstract java.util.List<groovy.lang.MetaMethod> respondsTo(java.lang.Object,java.lang.String)
meth public abstract java.util.List<groovy.lang.MetaMethod> respondsTo(java.lang.Object,java.lang.String,java.lang.Object[])
meth public abstract java.util.List<groovy.lang.MetaProperty> getProperties()
meth public abstract void setAttribute(java.lang.Object,java.lang.String,java.lang.Object)
meth public abstract void setProperty(java.lang.Object,java.lang.String,java.lang.Object)

CLSS public abstract groovy.lang.MetaProperty
cons public init(java.lang.String,java.lang.Class)
fld protected final java.lang.String name
fld protected java.lang.Class type
fld public final static java.lang.String PROPERTY_SET_PREFIX = "set"
meth public abstract java.lang.Object getProperty(java.lang.Object)
meth public abstract void setProperty(java.lang.Object,java.lang.Object)
meth public int getModifiers()
meth public java.lang.Class getType()
meth public java.lang.String getName()
meth public static java.lang.String getGetterName(java.lang.String,java.lang.Class)
meth public static java.lang.String getSetterName(java.lang.String)
supr java.lang.Object

CLSS public groovy.lang.MissingClassException
cons public init(java.lang.String,org.codehaus.groovy.ast.ASTNode,java.lang.String)
cons public init(org.codehaus.groovy.ast.ClassNode,java.lang.String)
meth public java.lang.String getType()
supr groovy.lang.GroovyRuntimeException
hfds serialVersionUID,type

CLSS public groovy.lang.MissingFieldException
cons public init(java.lang.String,java.lang.Class)
cons public init(java.lang.String,java.lang.Class,java.lang.Throwable)
cons public init(java.lang.String,java.lang.String,java.lang.Class)
meth public java.lang.Class getType()
meth public java.lang.String getField()
supr groovy.lang.GroovyRuntimeException
hfds field,serialVersionUID,type

CLSS public groovy.lang.MissingMethodException
cons public init(java.lang.String,java.lang.Class,java.lang.Object[])
cons public init(java.lang.String,java.lang.Class,java.lang.Object[],boolean)
meth public boolean isStatic()
meth public java.lang.Class getType()
meth public java.lang.Object[] getArguments()
meth public java.lang.String getMessage()
meth public java.lang.String getMethod()
supr groovy.lang.GroovyRuntimeException
hfds EMPTY_OBJECT_ARRAY,arguments,isStatic,method,serialVersionUID,type

CLSS public groovy.lang.MissingPropertyException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Class)
cons public init(java.lang.String,java.lang.Class,java.lang.Throwable)
cons public init(java.lang.String,java.lang.String,java.lang.Class)
fld public final static java.lang.Object MPE
meth public java.lang.Class getType()
meth public java.lang.String getMessageWithoutLocationText()
meth public java.lang.String getProperty()
supr groovy.lang.GroovyRuntimeException
hfds property,serialVersionUID,type

CLSS public abstract interface !annotation groovy.lang.Mixin
 anno 0 java.lang.Deprecated()
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract java.lang.Class[] value()

CLSS public abstract interface groovy.lang.MutableMetaClass
intf groovy.lang.MetaClass
meth public abstract boolean isModified()
meth public abstract void addMetaBeanProperty(groovy.lang.MetaBeanProperty)
meth public abstract void addMetaMethod(groovy.lang.MetaMethod)
meth public abstract void addNewInstanceMethod(java.lang.reflect.Method)
meth public abstract void addNewStaticMethod(java.lang.reflect.Method)

CLSS public abstract interface !annotation groovy.lang.Newify
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, METHOD, TYPE, FIELD, LOCAL_VARIABLE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean auto()
meth public abstract !hasdefault java.lang.Class<?>[] value()
meth public abstract !hasdefault java.lang.String pattern()

CLSS public groovy.lang.NonEmptySequence
cons public init()
cons public init(java.lang.Class)
cons public init(java.lang.Class,java.util.List)
meth public int minimumSize()
supr groovy.lang.Sequence
hfds serialVersionUID

CLSS public groovy.lang.NumberRange
cons public <%0 extends java.lang.Number & java.lang.Comparable, %1 extends java.lang.Number & java.lang.Comparable, %2 extends java.lang.Number & java.lang.Comparable<? super java.lang.Number>> init({%%0},{%%1},{%%2})
cons public <%0 extends java.lang.Number & java.lang.Comparable, %1 extends java.lang.Number & java.lang.Comparable, %2 extends java.lang.Number & java.lang.Comparable> init({%%0},{%%1},{%%2},boolean)
cons public <%0 extends java.lang.Number & java.lang.Comparable, %1 extends java.lang.Number & java.lang.Comparable> init({%%0},{%%1})
cons public <%0 extends java.lang.Number & java.lang.Comparable, %1 extends java.lang.Number & java.lang.Comparable> init({%%0},{%%1},boolean)
intf groovy.lang.Range<java.lang.Comparable>
meth public <%0 extends java.lang.Number & java.lang.Comparable> groovy.lang.NumberRange by({%%0})
meth public boolean contains(java.lang.Object)
meth public boolean containsWithinBounds(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean fastEquals(groovy.lang.NumberRange)
meth public boolean isReverse()
meth public int hashCode()
meth public int size()
meth public java.lang.Comparable get(int)
meth public java.lang.Comparable getFrom()
meth public java.lang.Comparable getStepSize()
meth public java.lang.Comparable getTo()
meth public java.lang.String inspect()
meth public java.lang.String toString()
meth public java.util.Iterator<java.lang.Comparable> iterator()
meth public java.util.List<java.lang.Comparable> step(int)
meth public java.util.List<java.lang.Comparable> subList(int,int)
meth public void step(int,groovy.lang.Closure)
supr java.util.AbstractList<java.lang.Comparable>
hfds from,hashCodeCache,inclusive,reverse,size,stepSize,to
hcls StepIterator

CLSS public groovy.lang.ObjectRange
cons public init(java.lang.Comparable,java.lang.Comparable)
cons public init(java.lang.Comparable,java.lang.Comparable,boolean)
intf groovy.lang.Range<java.lang.Comparable>
meth protected int compareTo(java.lang.Comparable,java.lang.Comparable)
meth protected java.lang.Object decrement(java.lang.Object)
meth protected java.lang.Object increment(java.lang.Object)
meth protected void checkBoundaryCompatibility()
meth public boolean contains(java.lang.Object)
meth public boolean containsWithinBounds(java.lang.Object)
meth public boolean equals(groovy.lang.ObjectRange)
meth public boolean equals(java.lang.Object)
meth public boolean isReverse()
meth public int size()
meth public java.lang.Comparable get(int)
meth public java.lang.Comparable getFrom()
meth public java.lang.Comparable getTo()
meth public java.lang.String inspect()
meth public java.lang.String toString()
meth public java.util.Iterator<java.lang.Comparable> iterator()
meth public java.util.List<java.lang.Comparable> step(int)
meth public java.util.List<java.lang.Comparable> subList(int,int)
meth public void step(int,groovy.lang.Closure)
supr java.util.AbstractList<java.lang.Comparable>
hfds from,reverse,size,to
hcls StepIterator

CLSS public groovy.lang.ParameterArray
cons public init(java.lang.Object)
meth public java.lang.Object get()
meth public java.lang.String toString()
supr java.lang.Object
hfds parameters

CLSS public abstract interface groovy.lang.PropertyAccessInterceptor
intf groovy.lang.Interceptor
meth public abstract java.lang.Object beforeGet(java.lang.Object,java.lang.String)
meth public abstract void beforeSet(java.lang.Object,java.lang.String,java.lang.Object)

CLSS public groovy.lang.PropertyValue
cons public init(java.lang.Object,groovy.lang.MetaProperty)
meth public java.lang.Class getType()
meth public java.lang.Object getValue()
meth public java.lang.String getName()
meth public void setValue(java.lang.Object)
supr java.lang.Object
hfds bean,mp

CLSS public groovy.lang.ProxyMetaClass
cons public init(groovy.lang.MetaClassRegistry,java.lang.Class,groovy.lang.MetaClass)
fld protected groovy.lang.Interceptor interceptor
fld protected groovy.lang.MetaClass adaptee
intf groovy.lang.AdaptingMetaClass
meth public groovy.lang.Interceptor getInterceptor()
meth public groovy.lang.MetaClass getAdaptee()
meth public java.lang.Object getProperty(java.lang.Class,java.lang.Object,java.lang.String,boolean,boolean)
meth public java.lang.Object invokeConstructor(java.lang.Object[])
meth public java.lang.Object invokeMethod(java.lang.Class,java.lang.Object,java.lang.String,java.lang.Object[],boolean,boolean)
meth public java.lang.Object invokeMethod(java.lang.Object,java.lang.String,java.lang.Object[])
meth public java.lang.Object invokeStaticMethod(java.lang.Object,java.lang.String,java.lang.Object[])
meth public java.lang.Object use(groovy.lang.Closure)
meth public java.lang.Object use(groovy.lang.GroovyObject,groovy.lang.Closure)
meth public static groovy.lang.ProxyMetaClass getInstance(java.lang.Class)
meth public void initialize()
meth public void setAdaptee(groovy.lang.MetaClass)
meth public void setInterceptor(groovy.lang.Interceptor)
meth public void setProperty(java.lang.Class,java.lang.Object,java.lang.String,java.lang.Object,boolean,boolean)
supr groovy.lang.MetaClassImpl
hcls Callable

CLSS public abstract interface groovy.lang.Range<%0 extends java.lang.Comparable>
intf java.util.List<{groovy.lang.Range%0}>
meth public abstract boolean containsWithinBounds(java.lang.Object)
meth public abstract boolean isReverse()
meth public abstract java.lang.String inspect()
meth public abstract java.util.List<{groovy.lang.Range%0}> step(int)
meth public abstract void step(int,groovy.lang.Closure)
meth public abstract {groovy.lang.Range%0} getFrom()
meth public abstract {groovy.lang.Range%0} getTo()

CLSS public groovy.lang.ReadOnlyPropertyException
cons public init(java.lang.String,java.lang.Class)
cons public init(java.lang.String,java.lang.String)
supr groovy.lang.MissingPropertyException
hfds serialVersionUID

CLSS public groovy.lang.Reference<%0 extends java.lang.Object>
cons public init()
cons public init({groovy.lang.Reference%0})
intf java.io.Serializable
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object invokeMethod(java.lang.String,java.lang.Object)
meth public void set({groovy.lang.Reference%0})
meth public void setProperty(java.lang.String,java.lang.Object)
meth public {groovy.lang.Reference%0} get()
supr groovy.lang.GroovyObjectSupport
hfds serialVersionUID,value

CLSS public abstract groovy.lang.Script
cons protected init()
cons protected init(groovy.lang.Binding)
meth public abstract java.lang.Object run()
meth public groovy.lang.Binding getBinding()
meth public java.lang.Object evaluate(java.io.File) throws java.io.IOException
meth public java.lang.Object evaluate(java.lang.String)
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object invokeMethod(java.lang.String,java.lang.Object)
meth public void print(java.lang.Object)
meth public void printf(java.lang.String,java.lang.Object)
meth public void printf(java.lang.String,java.lang.Object[])
meth public void println()
meth public void println(java.lang.Object)
meth public void run(java.io.File,java.lang.String[]) throws java.io.IOException
meth public void setBinding(groovy.lang.Binding)
meth public void setProperty(java.lang.String,java.lang.Object)
supr groovy.lang.GroovyObjectSupport
hfds binding

CLSS public groovy.lang.Sequence
cons public init()
cons public init(java.lang.Class)
cons public init(java.lang.Class,java.util.List)
intf groovy.lang.GroovyObject
meth protected void checkCollectionType(java.util.Collection)
meth protected void checkType(java.lang.Object)
meth protected void removeRange(int,int)
meth public boolean add(java.lang.Object)
meth public boolean addAll(int,java.util.Collection)
meth public boolean addAll(java.util.Collection)
meth public boolean equals(groovy.lang.Sequence)
meth public boolean equals(java.lang.Object)
meth public groovy.lang.MetaClass getMetaClass()
meth public int hashCode()
meth public int minimumSize()
meth public java.lang.Class type()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object invokeMethod(java.lang.String,java.lang.Object)
meth public java.lang.Object remove(int)
meth public java.lang.Object set(int,java.lang.Object)
meth public void add(int,java.lang.Object)
meth public void clear()
meth public void set(java.util.Collection)
meth public void setMetaClass(groovy.lang.MetaClass)
meth public void setProperty(java.lang.String,java.lang.Object)
supr java.util.ArrayList
hfds hashCode,metaClass,serialVersionUID,type

CLSS public abstract interface !annotation groovy.lang.Singleton
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault boolean lazy()
meth public abstract !hasdefault boolean strict()
meth public abstract !hasdefault java.lang.String property()

CLSS public groovy.lang.SpreadListEvaluatingException
cons public init(java.lang.String)
supr groovy.lang.GroovyRuntimeException
hfds serialVersionUID

CLSS public groovy.lang.SpreadMap
cons public init(java.lang.Object[])
cons public init(java.util.List)
cons public init(java.util.Map)
meth public boolean equals(groovy.lang.SpreadMap)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object put(java.lang.Object,java.lang.Object)
meth public java.lang.Object remove(java.lang.Object)
meth public java.lang.String toString()
meth public void putAll(java.util.Map)
supr java.util.HashMap
hfds hashCode,serialVersionUID

CLSS public groovy.lang.SpreadMapEvaluatingException
cons public init(java.lang.String)
supr groovy.lang.GroovyRuntimeException
hfds serialVersionUID

CLSS public groovy.lang.StringWriterIOException
cons public init(java.io.IOException)
meth public java.io.IOException getIOException()
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public groovy.lang.TracingInterceptor
cons public init()
fld protected java.io.Writer writer
intf groovy.lang.Interceptor
meth protected void write(java.lang.Object,java.lang.String,java.lang.Object[],java.lang.String)
meth protected void writeInfo(java.lang.Class,java.lang.String,java.lang.Object[]) throws java.io.IOException
meth public boolean doInvoke()
meth public java.io.Writer getWriter()
meth public java.lang.Object afterInvoke(java.lang.Object,java.lang.String,java.lang.Object[],java.lang.Object)
meth public java.lang.Object beforeInvoke(java.lang.Object,java.lang.String,java.lang.Object[])
meth public void setWriter(java.io.Writer)
supr java.lang.Object
hfds indent

CLSS public groovy.lang.Tuple<%0 extends java.lang.Object>
cons public !varargs init({groovy.lang.Tuple%0}[])
 anno 0 java.lang.SafeVarargs()
intf java.io.Serializable
meth public boolean equals(java.lang.Object)
meth public groovy.lang.Tuple<{groovy.lang.Tuple%0}> subTuple(int,int)
meth public int hashCode()
meth public int size()
meth public java.util.List<{groovy.lang.Tuple%0}> subList(int,int)
meth public {groovy.lang.Tuple%0} get(int)
supr java.util.AbstractList<{groovy.lang.Tuple%0}>
hfds contents,serialVersionUID

CLSS public groovy.lang.Tuple1<%0 extends java.lang.Object>
cons public init({groovy.lang.Tuple1%0})
meth public int size()
meth public java.lang.Object get(int)
meth public {groovy.lang.Tuple1%0} getFirst()
supr groovy.lang.Tuple
hfds first,serialVersionUID

CLSS public groovy.lang.Tuple2<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init({groovy.lang.Tuple2%0},{groovy.lang.Tuple2%1})
meth public int size()
meth public java.lang.Object get(int)
meth public {groovy.lang.Tuple2%0} getFirst()
meth public {groovy.lang.Tuple2%1} getSecond()
supr groovy.lang.Tuple
hfds first,second,serialVersionUID

CLSS public groovy.lang.Tuple3<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object>
cons public init({groovy.lang.Tuple3%0},{groovy.lang.Tuple3%1},{groovy.lang.Tuple3%2})
meth public int size()
meth public java.lang.Object get(int)
meth public {groovy.lang.Tuple3%0} getFirst()
meth public {groovy.lang.Tuple3%1} getSecond()
meth public {groovy.lang.Tuple3%2} getThird()
supr groovy.lang.Tuple
hfds first,second,serialVersionUID,third

CLSS public groovy.lang.Tuple4<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object>
cons public init({groovy.lang.Tuple4%0},{groovy.lang.Tuple4%1},{groovy.lang.Tuple4%2},{groovy.lang.Tuple4%3})
meth public int size()
meth public java.lang.Object get(int)
meth public {groovy.lang.Tuple4%0} getFirst()
meth public {groovy.lang.Tuple4%1} getSecond()
meth public {groovy.lang.Tuple4%2} getThird()
meth public {groovy.lang.Tuple4%3} getFourth()
supr groovy.lang.Tuple
hfds first,fourth,second,serialVersionUID,third

CLSS public groovy.lang.Tuple5<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object, %4 extends java.lang.Object>
cons public init({groovy.lang.Tuple5%0},{groovy.lang.Tuple5%1},{groovy.lang.Tuple5%2},{groovy.lang.Tuple5%3},{groovy.lang.Tuple5%4})
meth public int size()
meth public java.lang.Object get(int)
meth public {groovy.lang.Tuple5%0} getFirst()
meth public {groovy.lang.Tuple5%1} getSecond()
meth public {groovy.lang.Tuple5%2} getThird()
meth public {groovy.lang.Tuple5%3} getFourth()
meth public {groovy.lang.Tuple5%4} getFifth()
supr groovy.lang.Tuple
hfds fifth,first,fourth,second,serialVersionUID,third

CLSS public groovy.lang.Tuple6<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object, %4 extends java.lang.Object, %5 extends java.lang.Object>
cons public init({groovy.lang.Tuple6%0},{groovy.lang.Tuple6%1},{groovy.lang.Tuple6%2},{groovy.lang.Tuple6%3},{groovy.lang.Tuple6%4},{groovy.lang.Tuple6%5})
meth public int size()
meth public java.lang.Object get(int)
meth public {groovy.lang.Tuple6%0} getFirst()
meth public {groovy.lang.Tuple6%1} getSecond()
meth public {groovy.lang.Tuple6%2} getThird()
meth public {groovy.lang.Tuple6%3} getFourth()
meth public {groovy.lang.Tuple6%4} getFifth()
meth public {groovy.lang.Tuple6%5} getSixth()
supr groovy.lang.Tuple
hfds fifth,first,fourth,second,serialVersionUID,sixth,third

CLSS public groovy.lang.Tuple7<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object, %4 extends java.lang.Object, %5 extends java.lang.Object, %6 extends java.lang.Object>
cons public init({groovy.lang.Tuple7%0},{groovy.lang.Tuple7%1},{groovy.lang.Tuple7%2},{groovy.lang.Tuple7%3},{groovy.lang.Tuple7%4},{groovy.lang.Tuple7%5},{groovy.lang.Tuple7%6})
meth public int size()
meth public java.lang.Object get(int)
meth public {groovy.lang.Tuple7%0} getFirst()
meth public {groovy.lang.Tuple7%1} getSecond()
meth public {groovy.lang.Tuple7%2} getThird()
meth public {groovy.lang.Tuple7%3} getFourth()
meth public {groovy.lang.Tuple7%4} getFifth()
meth public {groovy.lang.Tuple7%5} getSixth()
meth public {groovy.lang.Tuple7%6} getSeventh()
supr groovy.lang.Tuple
hfds fifth,first,fourth,second,serialVersionUID,seventh,sixth,third

CLSS public groovy.lang.Tuple8<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object, %4 extends java.lang.Object, %5 extends java.lang.Object, %6 extends java.lang.Object, %7 extends java.lang.Object>
cons public init({groovy.lang.Tuple8%0},{groovy.lang.Tuple8%1},{groovy.lang.Tuple8%2},{groovy.lang.Tuple8%3},{groovy.lang.Tuple8%4},{groovy.lang.Tuple8%5},{groovy.lang.Tuple8%6},{groovy.lang.Tuple8%7})
meth public int size()
meth public java.lang.Object get(int)
meth public {groovy.lang.Tuple8%0} getFirst()
meth public {groovy.lang.Tuple8%1} getSecond()
meth public {groovy.lang.Tuple8%2} getThird()
meth public {groovy.lang.Tuple8%3} getFourth()
meth public {groovy.lang.Tuple8%4} getFifth()
meth public {groovy.lang.Tuple8%5} getSixth()
meth public {groovy.lang.Tuple8%6} getSeventh()
meth public {groovy.lang.Tuple8%7} getEighth()
supr groovy.lang.Tuple
hfds eighth,fifth,first,fourth,second,serialVersionUID,seventh,sixth,third

CLSS public groovy.lang.Tuple9<%0 extends java.lang.Object, %1 extends java.lang.Object, %2 extends java.lang.Object, %3 extends java.lang.Object, %4 extends java.lang.Object, %5 extends java.lang.Object, %6 extends java.lang.Object, %7 extends java.lang.Object, %8 extends java.lang.Object>
cons public init({groovy.lang.Tuple9%0},{groovy.lang.Tuple9%1},{groovy.lang.Tuple9%2},{groovy.lang.Tuple9%3},{groovy.lang.Tuple9%4},{groovy.lang.Tuple9%5},{groovy.lang.Tuple9%6},{groovy.lang.Tuple9%7},{groovy.lang.Tuple9%8})
meth public int size()
meth public java.lang.Object get(int)
meth public {groovy.lang.Tuple9%0} getFirst()
meth public {groovy.lang.Tuple9%1} getSecond()
meth public {groovy.lang.Tuple9%2} getThird()
meth public {groovy.lang.Tuple9%3} getFourth()
meth public {groovy.lang.Tuple9%4} getFifth()
meth public {groovy.lang.Tuple9%5} getSixth()
meth public {groovy.lang.Tuple9%6} getSeventh()
meth public {groovy.lang.Tuple9%7} getEighth()
meth public {groovy.lang.Tuple9%8} getNinth()
supr groovy.lang.Tuple
hfds eighth,fifth,first,fourth,ninth,second,serialVersionUID,seventh,sixth,third

CLSS public abstract interface groovy.lang.Writable
meth public abstract java.io.Writer writeTo(java.io.Writer) throws java.io.IOException

CLSS public abstract interface groovy.transform.CompilationUnitAware
meth public abstract void setCompilationUnit(org.codehaus.groovy.control.CompilationUnit)

CLSS public abstract groovy.util.AbstractFactory
cons public init()
intf groovy.util.Factory
meth public boolean isHandlesNodeChildren()
meth public boolean isLeaf()
meth public boolean onHandleNodeAttributes(groovy.util.FactoryBuilderSupport,java.lang.Object,java.util.Map)
meth public boolean onNodeChildren(groovy.util.FactoryBuilderSupport,java.lang.Object,groovy.lang.Closure)
meth public void onFactoryRegistration(groovy.util.FactoryBuilderSupport,java.lang.String,java.lang.String)
meth public void onNodeCompleted(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object)
meth public void setChild(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object)
meth public void setParent(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object)
supr java.lang.Object

CLSS public abstract interface groovy.util.Factory
meth public abstract boolean isHandlesNodeChildren()
meth public abstract boolean isLeaf()
meth public abstract boolean onHandleNodeAttributes(groovy.util.FactoryBuilderSupport,java.lang.Object,java.util.Map)
meth public abstract boolean onNodeChildren(groovy.util.FactoryBuilderSupport,java.lang.Object,groovy.lang.Closure)
meth public abstract java.lang.Object newInstance(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object,java.util.Map) throws java.lang.IllegalAccessException,java.lang.InstantiationException
meth public abstract void onFactoryRegistration(groovy.util.FactoryBuilderSupport,java.lang.String,java.lang.String)
meth public abstract void onNodeCompleted(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object)
meth public abstract void setChild(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object)
meth public abstract void setParent(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object)

CLSS public abstract groovy.util.FactoryBuilderSupport
cons public init()
cons public init(boolean)
fld protected boolean autoRegistrationComplete
fld protected boolean autoRegistrationRunning
fld protected groovy.lang.Closure methodMissingDelegate
fld protected groovy.lang.Closure propertyMissingDelegate
fld protected java.lang.String registrationGroupName
fld protected java.util.LinkedList<groovy.lang.Closure> attributeDelegates
fld protected java.util.LinkedList<groovy.lang.Closure> postInstantiateDelegates
fld protected java.util.LinkedList<groovy.lang.Closure> postNodeCompletionDelegates
fld protected java.util.LinkedList<groovy.lang.Closure> preInstantiateDelegates
fld protected java.util.Map<java.lang.String,groovy.lang.Closure> explicitMethods
fld protected java.util.Map<java.lang.String,groovy.lang.Closure[]> explicitProperties
fld protected java.util.Map<java.lang.String,java.util.Set<java.lang.String>> registrationGroup
fld public final static java.lang.String CHILD_BUILDER = "_CHILD_BUILDER_"
fld public final static java.lang.String CURRENT_BUILDER = "_CURRENT_BUILDER_"
fld public final static java.lang.String CURRENT_FACTORY = "_CURRENT_FACTORY_"
fld public final static java.lang.String CURRENT_NAME = "_CURRENT_NAME_"
fld public final static java.lang.String CURRENT_NODE = "_CURRENT_NODE_"
fld public final static java.lang.String OWNER = "owner"
fld public final static java.lang.String PARENT_BUILDER = "_PARENT_BUILDER_"
fld public final static java.lang.String PARENT_CONTEXT = "_PARENT_CONTEXT_"
fld public final static java.lang.String PARENT_FACTORY = "_PARENT_FACTORY_"
fld public final static java.lang.String PARENT_NAME = "_PARENT_NAME_"
fld public final static java.lang.String PARENT_NODE = "_PARENT_NODE_"
fld public final static java.lang.String SCRIPT_CLASS_NAME = "_SCRIPT_CLASS_NAME_"
meth protected boolean checkExplicitMethod(java.lang.String,java.lang.Object,groovy.lang.Reference)
meth protected groovy.lang.Closure resolveExplicitMethod(java.lang.String,java.lang.Object)
meth protected groovy.lang.Closure[] resolveExplicitProperty(java.lang.String)
meth protected groovy.util.Factory resolveFactory(java.lang.Object,java.util.Map,java.lang.Object)
meth protected groovy.util.FactoryBuilderSupport getProxyBuilder()
meth protected java.lang.Object createNode(java.lang.Object,java.util.Map,java.lang.Object)
meth protected java.lang.Object dispatchNodeCall(java.lang.Object,java.lang.Object)
meth protected java.lang.Object dispathNodeCall(java.lang.Object,java.lang.Object)
 anno 0 java.lang.Deprecated()
meth protected java.lang.Object postNodeCompletion(java.lang.Object,java.lang.Object)
meth protected java.util.LinkedList<java.util.Map<java.lang.String,java.lang.Object>> getContexts()
meth protected java.util.Map<java.lang.String,java.lang.Object> getContinuationData()
meth protected java.util.Map<java.lang.String,java.lang.Object> popContext()
meth protected void handleNodeAttributes(java.lang.Object,java.util.Map)
meth protected void newContext()
meth protected void nodeCompleted(java.lang.Object,java.lang.Object)
meth protected void postInstantiate(java.lang.Object,java.util.Map,java.lang.Object)
meth protected void preInstantiate(java.lang.Object,java.util.Map,java.lang.Object)
meth protected void reset()
meth protected void restoreFromContinuationData(java.util.Map<java.lang.String,java.lang.Object>)
meth protected void setClosureDelegate(groovy.lang.Closure,java.lang.Object)
meth protected void setNodeAttributes(java.lang.Object,java.util.Map)
meth protected void setParent(java.lang.Object,java.lang.Object)
meth protected void setProxyBuilder(groovy.util.FactoryBuilderSupport)
meth public groovy.lang.Closure addAttributeDelegate(groovy.lang.Closure)
meth public groovy.lang.Closure addPostInstantiateDelegate(groovy.lang.Closure)
meth public groovy.lang.Closure addPostNodeCompletionDelegate(groovy.lang.Closure)
meth public groovy.lang.Closure addPreInstantiateDelegate(groovy.lang.Closure)
meth public groovy.lang.Closure getMethodMissingDelegate()
meth public groovy.lang.Closure getNameMappingClosure()
meth public groovy.lang.Closure getPropertyMissingDelegate()
meth public groovy.util.Factory getCurrentFactory()
meth public groovy.util.Factory getParentFactory()
meth public groovy.util.FactoryBuilderSupport getChildBuilder()
meth public groovy.util.FactoryBuilderSupport getCurrentBuilder()
meth public java.lang.Object build(groovy.lang.Script)
meth public java.lang.Object build(java.lang.Class)
meth public java.lang.Object build(java.lang.String,groovy.lang.GroovyClassLoader)
meth public java.lang.Object getContextAttribute(java.lang.String)
meth public java.lang.Object getCurrent()
meth public java.lang.Object getName(java.lang.String)
meth public java.lang.Object getParentNode()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object getVariable(java.lang.String)
meth public java.lang.Object invokeMethod(java.lang.String)
meth public java.lang.Object invokeMethod(java.lang.String,java.lang.Object)
meth public java.lang.Object withBuilder(groovy.util.FactoryBuilderSupport,groovy.lang.Closure)
meth public java.lang.Object withBuilder(groovy.util.FactoryBuilderSupport,java.lang.String,groovy.lang.Closure)
meth public java.lang.Object withBuilder(java.util.Map,groovy.util.FactoryBuilderSupport,java.lang.String,groovy.lang.Closure)
meth public java.lang.String getCurrentName()
meth public java.lang.String getParentName()
meth public java.util.List<groovy.lang.Closure> getAttributeDelegates()
meth public java.util.List<groovy.lang.Closure> getDisposalClosures()
meth public java.util.List<groovy.lang.Closure> getPostInstantiateDelegates()
meth public java.util.List<groovy.lang.Closure> getPostNodeCompletionDelegates()
meth public java.util.List<groovy.lang.Closure> getPreInstantiateDelegates()
meth public java.util.Map getParentContext()
meth public java.util.Map getVariables()
meth public java.util.Map<java.lang.String,groovy.lang.Closure> getExplicitMethods()
meth public java.util.Map<java.lang.String,groovy.lang.Closure> getLocalExplicitMethods()
meth public java.util.Map<java.lang.String,groovy.lang.Closure[]> getExplicitProperties()
meth public java.util.Map<java.lang.String,groovy.lang.Closure[]> getLocalExplicitProperties()
meth public java.util.Map<java.lang.String,groovy.util.Factory> getFactories()
meth public java.util.Map<java.lang.String,groovy.util.Factory> getLocalFactories()
meth public java.util.Map<java.lang.String,java.lang.Object> getContext()
meth public java.util.Set<java.lang.String> getRegistrationGroupItems(java.lang.String)
meth public java.util.Set<java.lang.String> getRegistrationGroups()
meth public static boolean checkValueIsType(java.lang.Object,java.lang.Object,java.lang.Class)
meth public static boolean checkValueIsTypeNotString(java.lang.Object,java.lang.Object,java.lang.Class)
meth public static void checkValueIsNull(java.lang.Object,java.lang.Object)
meth public void addDisposalClosure(groovy.lang.Closure)
meth public void autoRegisterNodes()
meth public void dispose()
meth public void registerBeanFactory(java.lang.String,java.lang.Class)
meth public void registerBeanFactory(java.lang.String,java.lang.String,java.lang.Class)
meth public void registerExplicitMethod(java.lang.String,groovy.lang.Closure)
meth public void registerExplicitMethod(java.lang.String,java.lang.String,groovy.lang.Closure)
meth public void registerExplicitProperty(java.lang.String,groovy.lang.Closure,groovy.lang.Closure)
meth public void registerExplicitProperty(java.lang.String,java.lang.String,groovy.lang.Closure,groovy.lang.Closure)
meth public void registerFactory(java.lang.String,groovy.util.Factory)
meth public void registerFactory(java.lang.String,java.lang.String,groovy.util.Factory)
meth public void removeAttributeDelegate(groovy.lang.Closure)
meth public void removePostInstantiateDelegate(groovy.lang.Closure)
meth public void removePostNodeCompletionDelegate(groovy.lang.Closure)
meth public void removePreInstantiateDelegate(groovy.lang.Closure)
meth public void setMethodMissingDelegate(groovy.lang.Closure)
meth public void setNameMappingClosure(groovy.lang.Closure)
meth public void setProperty(java.lang.String,java.lang.Object)
meth public void setPropertyMissingDelegate(groovy.lang.Closure)
meth public void setVariable(java.lang.String,java.lang.Object)
supr groovy.lang.Binding
hfds LOG,METHOD_COMPARATOR,contexts,disposalClosures,factories,globalProxyBuilder,localProxyBuilder,nameMappingClosure

CLSS public groovyjarjarantlr.ANTLRError
cons public init()
cons public init(java.lang.String)
supr java.lang.Error

CLSS public groovyjarjarantlr.ANTLRException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Exception

CLSS public abstract interface groovyjarjarantlr.ANTLRGrammarParseBehavior
meth public abstract void abortGrammar()
meth public abstract void beginAlt(boolean)
meth public abstract void beginChildList()
meth public abstract void beginExceptionGroup()
meth public abstract void beginExceptionSpec(groovyjarjarantlr.Token)
meth public abstract void beginSubRule(groovyjarjarantlr.Token,groovyjarjarantlr.Token,boolean)
meth public abstract void beginTree(groovyjarjarantlr.Token) throws groovyjarjarantlr.SemanticException
meth public abstract void defineRuleName(groovyjarjarantlr.Token,java.lang.String,boolean,java.lang.String) throws groovyjarjarantlr.SemanticException
meth public abstract void defineToken(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public abstract void endAlt()
meth public abstract void endChildList()
meth public abstract void endExceptionGroup()
meth public abstract void endExceptionSpec()
meth public abstract void endGrammar()
meth public abstract void endOptions()
meth public abstract void endRule(java.lang.String)
meth public abstract void endSubRule()
meth public abstract void endTree()
meth public abstract void hasError()
meth public abstract void noASTSubRule()
meth public abstract void oneOrMoreSubRule()
meth public abstract void optionalSubRule()
meth public abstract void refAction(groovyjarjarantlr.Token)
meth public abstract void refArgAction(groovyjarjarantlr.Token)
meth public abstract void refCharLiteral(groovyjarjarantlr.Token,groovyjarjarantlr.Token,boolean,int,boolean)
meth public abstract void refCharRange(groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,int,boolean)
meth public abstract void refElementOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public abstract void refExceptionHandler(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public abstract void refHeaderAction(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public abstract void refInitAction(groovyjarjarantlr.Token)
meth public abstract void refMemberAction(groovyjarjarantlr.Token)
meth public abstract void refPreambleAction(groovyjarjarantlr.Token)
meth public abstract void refReturnAction(groovyjarjarantlr.Token)
meth public abstract void refRule(groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,int)
meth public abstract void refSemPred(groovyjarjarantlr.Token)
meth public abstract void refStringLiteral(groovyjarjarantlr.Token,groovyjarjarantlr.Token,int,boolean)
meth public abstract void refToken(groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,boolean,int,boolean)
meth public abstract void refTokenRange(groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,int,boolean)
meth public abstract void refTokensSpecElementOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public abstract void refTreeSpecifier(groovyjarjarantlr.Token)
meth public abstract void refWildcard(groovyjarjarantlr.Token,groovyjarjarantlr.Token,int)
meth public abstract void setArgOfRuleRef(groovyjarjarantlr.Token)
meth public abstract void setCharVocabulary(groovyjarjarantlr.collections.impl.BitSet)
meth public abstract void setFileOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token,java.lang.String)
meth public abstract void setGrammarOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public abstract void setRuleOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public abstract void setSubruleOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public abstract void setUserExceptions(java.lang.String)
meth public abstract void startLexer(java.lang.String,groovyjarjarantlr.Token,java.lang.String,java.lang.String)
meth public abstract void startParser(java.lang.String,groovyjarjarantlr.Token,java.lang.String,java.lang.String)
meth public abstract void startTreeWalker(java.lang.String,groovyjarjarantlr.Token,java.lang.String,java.lang.String)
meth public abstract void synPred()
meth public abstract void zeroOrMoreSubRule()

CLSS public groovyjarjarantlr.ANTLRHashString
cons public init(char[],int,groovyjarjarantlr.CharScanner)
cons public init(groovyjarjarantlr.CharScanner)
cons public init(java.lang.String,groovyjarjarantlr.CharScanner)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public void setBuffer(char[],int)
meth public void setString(java.lang.String)
supr java.lang.Object
hfds buf,len,lexer,prime,s

CLSS public groovyjarjarantlr.ANTLRLexer
cons public init(groovyjarjarantlr.InputBuffer)
cons public init(groovyjarjarantlr.LexerSharedInputState)
cons public init(java.io.InputStream)
cons public init(java.io.Reader)
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_0
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_1
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_2
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_3
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_4
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_5
intf groovyjarjarantlr.ANTLRTokenTypes
intf groovyjarjarantlr.TokenStream
meth protected final int mINTERNAL_RULE_REF(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mDIGIT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mESC(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mML_COMMENT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mNESTED_ACTION(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mNESTED_ARG_ACTION(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mSL_COMMENT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mWS_LOOP(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mWS_OPT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mXDIGIT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mACTION(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mARG_ACTION(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mASSIGN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mBANG(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mCARET(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mCHAR_LITERAL(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mCLOSE_ELEMENT_OPTION(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mCOLON(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mCOMMA(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mCOMMENT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mIMPLIES(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mINT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mLPAREN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mNOT_OP(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mOPEN_ELEMENT_OPTION(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mOR(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mPLUS(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mQUESTION(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mRANGE(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mRCURLY(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mRPAREN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mRULE_REF(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSEMI(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSTAR(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSTRING_LITERAL(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mTOKEN_REF(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mTREE_BEGIN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mWILDCARD(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mWS(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public groovyjarjarantlr.Token nextToken() throws groovyjarjarantlr.TokenStreamException
meth public static int escapeCharValue(java.lang.String)
meth public static int tokenTypeForCharLiteral(java.lang.String)
supr groovyjarjarantlr.CharScanner

CLSS public groovyjarjarantlr.ANTLRParser
cons protected init(groovyjarjarantlr.TokenBuffer,int)
cons protected init(groovyjarjarantlr.TokenStream,int)
cons public init(groovyjarjarantlr.ParserSharedInputState)
cons public init(groovyjarjarantlr.TokenBuffer)
cons public init(groovyjarjarantlr.TokenBuffer,groovyjarjarantlr.ANTLRGrammarParseBehavior,groovyjarjarantlr.Tool)
cons public init(groovyjarjarantlr.TokenStream)
fld protected int blockNesting
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_0
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_1
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_10
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_11
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_2
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_3
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_4
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_5
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_6
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_7
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_8
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_9
fld public final static java.lang.String[] _tokenNames
intf groovyjarjarantlr.ANTLRTokenTypes
meth public final groovyjarjarantlr.Token id() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final groovyjarjarantlr.Token optionValue() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final groovyjarjarantlr.Token qualifiedID() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final groovyjarjarantlr.collections.impl.BitSet charSet() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final groovyjarjarantlr.collections.impl.BitSet setBlockElement() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final int ast_type_spec() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final java.lang.String superClass() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void alternative() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void block() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void classDef() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void ebnf(groovyjarjarantlr.Token,boolean) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void element() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void elementNoOptionSpec() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void elementOptionSpec() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void exceptionGroup() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void exceptionHandler() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void exceptionSpec() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void exceptionSpecNoLabel() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void fileOptionsSpec() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void grammar() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void lexerOptionsSpec() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void lexerSpec(java.lang.String) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void notTerminal(groovyjarjarantlr.Token) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void parserOptionsSpec() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void parserSpec(java.lang.String) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void range(groovyjarjarantlr.Token) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void rootNode() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void rule() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void ruleOptionsSpec() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void rules() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void subruleOptionsSpec() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void terminal(groovyjarjarantlr.Token) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void throwsSpec() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void tokensSpec() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void tokensSpecOptions(groovyjarjarantlr.Token) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void tree() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void treeParserOptionsSpec() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void treeParserSpec(java.lang.String) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public void reportError(groovyjarjarantlr.RecognitionException)
meth public void reportError(groovyjarjarantlr.RecognitionException,java.lang.String)
meth public void reportError(java.lang.String)
meth public void reportWarning(java.lang.String)
supr groovyjarjarantlr.LLkParser
hfds DEBUG_PARSER,antlrTool,behavior

CLSS public groovyjarjarantlr.ANTLRStringBuffer
cons public init()
cons public init(int)
fld protected char[] buffer
fld protected int length
meth public final char charAt(int)
meth public final char[] getBuffer()
meth public final int length()
meth public final java.lang.String toString()
meth public final void append(char)
meth public final void append(java.lang.String)
meth public final void setCharAt(int,char)
meth public final void setLength(int)
supr java.lang.Object

CLSS public groovyjarjarantlr.ANTLRTokdefLexer
cons public init(groovyjarjarantlr.InputBuffer)
cons public init(groovyjarjarantlr.LexerSharedInputState)
cons public init(java.io.InputStream)
cons public init(java.io.Reader)
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_0
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_1
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_2
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_3
intf groovyjarjarantlr.ANTLRTokdefParserTokenTypes
intf groovyjarjarantlr.TokenStream
meth protected final void mDIGIT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mESC(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mXDIGIT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mASSIGN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mID(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mINT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mLPAREN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mML_COMMENT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mRPAREN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSL_COMMENT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSTRING(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mWS(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public groovyjarjarantlr.Token nextToken() throws groovyjarjarantlr.TokenStreamException
supr groovyjarjarantlr.CharScanner

CLSS public groovyjarjarantlr.ANTLRTokdefParser
cons protected init(groovyjarjarantlr.TokenBuffer,int)
cons protected init(groovyjarjarantlr.TokenStream,int)
cons public init(groovyjarjarantlr.ParserSharedInputState)
cons public init(groovyjarjarantlr.TokenBuffer)
cons public init(groovyjarjarantlr.TokenStream)
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_0
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_1
fld public final static java.lang.String[] _tokenNames
intf groovyjarjarantlr.ANTLRTokdefParserTokenTypes
meth protected groovyjarjarantlr.Tool getTool()
meth public final void file(groovyjarjarantlr.ImportVocabTokenManager) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void line(groovyjarjarantlr.ImportVocabTokenManager) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public void reportError(groovyjarjarantlr.RecognitionException)
meth public void reportError(java.lang.String)
meth public void reportWarning(java.lang.String)
meth public void setTool(groovyjarjarantlr.Tool)
supr groovyjarjarantlr.LLkParser
hfds antlrTool

CLSS public abstract interface groovyjarjarantlr.ANTLRTokdefParserTokenTypes
fld public final static int ASSIGN = 6
fld public final static int DIGIT = 14
fld public final static int EOF = 1
fld public final static int ESC = 13
fld public final static int ID = 4
fld public final static int INT = 9
fld public final static int LPAREN = 7
fld public final static int ML_COMMENT = 12
fld public final static int NULL_TREE_LOOKAHEAD = 3
fld public final static int RPAREN = 8
fld public final static int SL_COMMENT = 11
fld public final static int STRING = 5
fld public final static int WS = 10
fld public final static int XDIGIT = 15

CLSS public abstract interface groovyjarjarantlr.ANTLRTokenTypes
fld public final static int ACTION = 7
fld public final static int ARG_ACTION = 34
fld public final static int ASSIGN = 15
fld public final static int BANG = 33
fld public final static int CARET = 49
fld public final static int CHAR_LITERAL = 19
fld public final static int CLOSE_ELEMENT_OPTION = 26
fld public final static int COLON = 36
fld public final static int COMMA = 38
fld public final static int COMMENT = 53
fld public final static int DIGIT = 57
fld public final static int DOC_COMMENT = 8
fld public final static int EOF = 1
fld public final static int ESC = 56
fld public final static int IMPLIES = 48
fld public final static int INT = 20
fld public final static int INTERNAL_RULE_REF = 62
fld public final static int LITERAL_Lexer = 12
fld public final static int LITERAL_Parser = 29
fld public final static int LITERAL_TreeParser = 13
fld public final static int LITERAL_catch = 40
fld public final static int LITERAL_charVocabulary = 18
fld public final static int LITERAL_class = 10
fld public final static int LITERAL_exception = 39
fld public final static int LITERAL_extends = 11
fld public final static int LITERAL_header = 5
fld public final static int LITERAL_lexclass = 9
fld public final static int LITERAL_options = 51
fld public final static int LITERAL_private = 32
fld public final static int LITERAL_protected = 30
fld public final static int LITERAL_public = 31
fld public final static int LITERAL_returns = 35
fld public final static int LITERAL_throws = 37
fld public final static int LITERAL_tokens = 4
fld public final static int LPAREN = 27
fld public final static int ML_COMMENT = 55
fld public final static int NESTED_ACTION = 60
fld public final static int NESTED_ARG_ACTION = 59
fld public final static int NOT_OP = 42
fld public final static int NULL_TREE_LOOKAHEAD = 3
fld public final static int OPEN_ELEMENT_OPTION = 25
fld public final static int OPTIONS = 14
fld public final static int OR = 21
fld public final static int PLUS = 47
fld public final static int QUESTION = 45
fld public final static int RANGE = 22
fld public final static int RCURLY = 17
fld public final static int RPAREN = 28
fld public final static int RULE_REF = 41
fld public final static int SEMI = 16
fld public final static int SEMPRED = 43
fld public final static int SL_COMMENT = 54
fld public final static int STAR = 46
fld public final static int STRING_LITERAL = 6
fld public final static int TOKENS = 23
fld public final static int TOKEN_REF = 24
fld public final static int TREE_BEGIN = 44
fld public final static int WILDCARD = 50
fld public final static int WS = 52
fld public final static int WS_LOOP = 61
fld public final static int WS_OPT = 63
fld public final static int XDIGIT = 58

CLSS public groovyjarjarantlr.ASTFactory
cons public init()
cons public init(java.util.Hashtable)
fld protected java.lang.Class theASTNodeTypeClass
fld protected java.lang.String theASTNodeType
fld protected java.util.Hashtable tokenTypeToASTClassMap
meth protected groovyjarjarantlr.collections.AST create(java.lang.Class)
meth protected groovyjarjarantlr.collections.AST createUsingCtor(groovyjarjarantlr.Token,java.lang.String)
meth public groovyjarjarantlr.collections.AST create()
meth public groovyjarjarantlr.collections.AST create(groovyjarjarantlr.Token)
meth public groovyjarjarantlr.collections.AST create(groovyjarjarantlr.Token,java.lang.String)
meth public groovyjarjarantlr.collections.AST create(groovyjarjarantlr.collections.AST)
meth public groovyjarjarantlr.collections.AST create(int)
meth public groovyjarjarantlr.collections.AST create(int,java.lang.String)
meth public groovyjarjarantlr.collections.AST create(int,java.lang.String,java.lang.String)
meth public groovyjarjarantlr.collections.AST create(java.lang.String)
meth public groovyjarjarantlr.collections.AST dup(groovyjarjarantlr.collections.AST)
meth public groovyjarjarantlr.collections.AST dupList(groovyjarjarantlr.collections.AST)
meth public groovyjarjarantlr.collections.AST dupTree(groovyjarjarantlr.collections.AST)
meth public groovyjarjarantlr.collections.AST make(groovyjarjarantlr.collections.AST[])
meth public groovyjarjarantlr.collections.AST make(groovyjarjarantlr.collections.impl.ASTArray)
meth public java.lang.Class getASTNodeType(int)
meth public java.util.Hashtable getTokenTypeToASTClassMap()
meth public void addASTChild(groovyjarjarantlr.ASTPair,groovyjarjarantlr.collections.AST)
meth public void error(java.lang.String)
meth public void makeASTRoot(groovyjarjarantlr.ASTPair,groovyjarjarantlr.collections.AST)
meth public void setASTNodeClass(java.lang.Class)
meth public void setASTNodeClass(java.lang.String)
meth public void setASTNodeType(java.lang.String)
meth public void setTokenTypeASTNodeType(int,java.lang.String)
meth public void setTokenTypeToASTClassMap(java.util.Hashtable)
supr java.lang.Object
hfds class$antlr$CommonAST,class$antlr$Token

CLSS public groovyjarjarantlr.ASTIterator
cons public init(groovyjarjarantlr.collections.AST)
fld protected groovyjarjarantlr.collections.AST cursor
fld protected groovyjarjarantlr.collections.AST original
meth public boolean isSubtree(groovyjarjarantlr.collections.AST,groovyjarjarantlr.collections.AST)
meth public groovyjarjarantlr.collections.AST next(groovyjarjarantlr.collections.AST)
supr java.lang.Object

CLSS public groovyjarjarantlr.ASTNULLType
cons public init()
intf groovyjarjarantlr.collections.AST
meth public boolean equals(groovyjarjarantlr.collections.AST)
meth public boolean equalsList(groovyjarjarantlr.collections.AST)
meth public boolean equalsListPartial(groovyjarjarantlr.collections.AST)
meth public boolean equalsTree(groovyjarjarantlr.collections.AST)
meth public boolean equalsTreePartial(groovyjarjarantlr.collections.AST)
meth public groovyjarjarantlr.collections.AST getFirstChild()
meth public groovyjarjarantlr.collections.AST getNextSibling()
meth public groovyjarjarantlr.collections.ASTEnumeration findAll(groovyjarjarantlr.collections.AST)
meth public groovyjarjarantlr.collections.ASTEnumeration findAllPartial(groovyjarjarantlr.collections.AST)
meth public int getColumn()
meth public int getLine()
meth public int getNumberOfChildren()
meth public int getType()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public java.lang.String toStringList()
meth public java.lang.String toStringTree()
meth public void addChild(groovyjarjarantlr.collections.AST)
meth public void initialize(groovyjarjarantlr.Token)
meth public void initialize(groovyjarjarantlr.collections.AST)
meth public void initialize(int,java.lang.String)
meth public void setFirstChild(groovyjarjarantlr.collections.AST)
meth public void setNextSibling(groovyjarjarantlr.collections.AST)
meth public void setText(java.lang.String)
meth public void setType(int)
supr java.lang.Object

CLSS public groovyjarjarantlr.ASTPair
cons public init()
fld public groovyjarjarantlr.collections.AST child
fld public groovyjarjarantlr.collections.AST root
meth public final void advanceChildToEnd()
meth public groovyjarjarantlr.ASTPair copy()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface groovyjarjarantlr.ASTVisitor
meth public abstract void visit(groovyjarjarantlr.collections.AST)

CLSS public abstract interface groovyjarjarantlr.ASdebug.IASDebugStream
meth public abstract groovyjarjarantlr.ASdebug.TokenOffsetInfo getOffsetInfo(groovyjarjarantlr.Token)
meth public abstract java.lang.String getEntireText()

CLSS public groovyjarjarantlr.ActionTransInfo
cons public init()
fld public boolean assignToRoot
fld public java.lang.String followSetName
fld public java.lang.String refRuleRoot
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract groovyjarjarantlr.BaseAST
cons public init()
fld protected groovyjarjarantlr.BaseAST down
fld protected groovyjarjarantlr.BaseAST right
intf groovyjarjarantlr.collections.AST
intf java.io.Serializable
meth public abstract void initialize(groovyjarjarantlr.Token)
meth public abstract void initialize(groovyjarjarantlr.collections.AST)
meth public abstract void initialize(int,java.lang.String)
meth public boolean equals(groovyjarjarantlr.collections.AST)
meth public boolean equalsList(groovyjarjarantlr.collections.AST)
meth public boolean equalsListPartial(groovyjarjarantlr.collections.AST)
meth public boolean equalsTree(groovyjarjarantlr.collections.AST)
meth public boolean equalsTreePartial(groovyjarjarantlr.collections.AST)
meth public groovyjarjarantlr.collections.AST getFirstChild()
meth public groovyjarjarantlr.collections.AST getNextSibling()
meth public groovyjarjarantlr.collections.ASTEnumeration findAll(groovyjarjarantlr.collections.AST)
meth public groovyjarjarantlr.collections.ASTEnumeration findAllPartial(groovyjarjarantlr.collections.AST)
meth public int getColumn()
meth public int getLine()
meth public int getNumberOfChildren()
meth public int getType()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public java.lang.String toStringList()
meth public java.lang.String toStringTree()
meth public static java.lang.String decode(java.lang.String)
meth public static java.lang.String encode(java.lang.String)
meth public static java.lang.String[] getTokenNames()
meth public static void setVerboseStringConversion(boolean,java.lang.String[])
meth public void addChild(groovyjarjarantlr.collections.AST)
meth public void removeChildren()
meth public void setFirstChild(groovyjarjarantlr.collections.AST)
meth public void setNextSibling(groovyjarjarantlr.collections.AST)
meth public void setText(java.lang.String)
meth public void setType(int)
meth public void xmlSerialize(java.io.Writer) throws java.io.IOException
meth public void xmlSerializeNode(java.io.Writer) throws java.io.IOException
meth public void xmlSerializeRootClose(java.io.Writer) throws java.io.IOException
meth public void xmlSerializeRootOpen(java.io.Writer) throws java.io.IOException
supr java.lang.Object
hfds tokenNames,verboseStringConversion

CLSS public groovyjarjarantlr.ByteBuffer
cons public init(java.io.InputStream)
fld public java.io.InputStream input
meth public void fill(int) throws groovyjarjarantlr.CharStreamException
supr groovyjarjarantlr.InputBuffer

CLSS public groovyjarjarantlr.CSharpCodeGenerator
cons public init()
fld protected boolean genAST
fld protected boolean saveText
fld protected final static java.lang.String NONUNIQUE
fld protected int syntacticPredLevel
fld public final static int caseSizeThreshold = 127
meth protected boolean lookaheadIsEmpty(groovyjarjarantlr.Alternative,int)
meth protected int addSemPred(java.lang.String)
meth protected java.lang.String getBitsetName(int)
meth protected java.lang.String getLookaheadTestExpression(groovyjarjarantlr.Alternative,int)
meth protected java.lang.String getLookaheadTestExpression(groovyjarjarantlr.Lookahead[],int)
meth protected java.lang.String getLookaheadTestTerm(int,groovyjarjarantlr.collections.impl.BitSet)
meth protected java.lang.String processActionForSpecialSymbols(java.lang.String,int,groovyjarjarantlr.RuleBlock,groovyjarjarantlr.ActionTransInfo)
meth protected void genASTDeclaration(groovyjarjarantlr.AlternativeElement)
meth protected void genASTDeclaration(groovyjarjarantlr.AlternativeElement,java.lang.String)
meth protected void genASTDeclaration(groovyjarjarantlr.AlternativeElement,java.lang.String,java.lang.String)
meth protected void genAlt(groovyjarjarantlr.Alternative,groovyjarjarantlr.AlternativeBlock)
meth protected void genBitsets(groovyjarjarantlr.collections.impl.Vector,int)
meth protected void genBlockInitAction(groovyjarjarantlr.AlternativeBlock)
meth protected void genBlockPreamble(groovyjarjarantlr.AlternativeBlock)
meth protected void genCases(groovyjarjarantlr.collections.impl.BitSet)
meth protected void genHeader()
meth protected void genMatch(groovyjarjarantlr.GrammarAtom)
meth protected void genMatch(groovyjarjarantlr.collections.impl.BitSet)
meth protected void genMatchUsingAtomText(groovyjarjarantlr.GrammarAtom)
meth protected void genMatchUsingAtomTokenType(groovyjarjarantlr.GrammarAtom)
meth protected void genSemPred(java.lang.String,int)
meth protected void genSemPredMap()
meth protected void genSynPred(groovyjarjarantlr.SynPredBlock,java.lang.String)
meth protected void genTokenDefinitions(groovyjarjarantlr.TokenManager) throws java.io.IOException
meth protected void genTokenTypes(groovyjarjarantlr.TokenManager) throws java.io.IOException
meth public java.lang.Object genCommonBlock(groovyjarjarantlr.AlternativeBlock,boolean)
meth public java.lang.String getASTCreateString(groovyjarjarantlr.GrammarAtom,java.lang.String)
meth public java.lang.String getASTCreateString(groovyjarjarantlr.collections.impl.Vector)
meth public java.lang.String getASTCreateString(java.lang.String)
meth public java.lang.String getRangeExpression(int,int[])
meth public java.lang.String getTokenTypesClassName()
meth public java.lang.String mapTreeId(java.lang.String,groovyjarjarantlr.ActionTransInfo)
meth public java.lang.String processStringForASTConstructor(java.lang.String)
meth public java.lang.String[] split(java.lang.String,java.lang.String)
meth public void exitIfError()
meth public void gen()
meth public void gen(groovyjarjarantlr.ActionElement)
meth public void gen(groovyjarjarantlr.AlternativeBlock)
meth public void gen(groovyjarjarantlr.BlockEndElement)
meth public void gen(groovyjarjarantlr.CharLiteralElement)
meth public void gen(groovyjarjarantlr.CharRangeElement)
meth public void gen(groovyjarjarantlr.LexerGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.OneOrMoreBlock)
meth public void gen(groovyjarjarantlr.ParserGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.RuleRefElement)
meth public void gen(groovyjarjarantlr.StringLiteralElement)
meth public void gen(groovyjarjarantlr.TokenRangeElement)
meth public void gen(groovyjarjarantlr.TokenRefElement)
meth public void gen(groovyjarjarantlr.TreeElement)
meth public void gen(groovyjarjarantlr.TreeWalkerGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.WildcardElement)
meth public void gen(groovyjarjarantlr.ZeroOrMoreBlock)
meth public void genBody(groovyjarjarantlr.LexerGrammar) throws java.io.IOException
meth public void genBody(groovyjarjarantlr.ParserGrammar) throws java.io.IOException
meth public void genBody(groovyjarjarantlr.TreeWalkerGrammar) throws java.io.IOException
meth public void genInitFactory(groovyjarjarantlr.Grammar)
meth public void genNextToken()
meth public void genRule(groovyjarjarantlr.RuleSymbol,boolean,int,groovyjarjarantlr.TokenManager)
meth public void genTokenStrings()
meth public void setupOutput(java.lang.String) throws java.io.IOException
supr groovyjarjarantlr.CodeGenerator
hfds astTypes,astVarNumber,blockNestingLevel,commonExtraArgs,commonExtraParams,commonLocalVars,currentASTResult,currentRule,declaredASTVariables,exceptionThrown,labeledElementASTType,labeledElementInit,labeledElementType,lt1Value,nameSpace,saveIndexCreateLevel,semPreds,throwNoViable,treeVariableMap,usingCustomAST

CLSS public groovyjarjarantlr.CSharpNameSpace
cons public init(java.lang.String)
supr groovyjarjarantlr.NameSpace

CLSS public groovyjarjarantlr.CharBuffer
cons public init(java.io.Reader)
fld public java.io.Reader input
meth public void fill(int) throws groovyjarjarantlr.CharStreamException
supr groovyjarjarantlr.InputBuffer

CLSS public abstract interface groovyjarjarantlr.CharFormatter
meth public abstract java.lang.String escapeChar(int,boolean)
meth public abstract java.lang.String escapeString(java.lang.String)
meth public abstract java.lang.String literalChar(int)
meth public abstract java.lang.String literalString(java.lang.String)

CLSS public groovyjarjarantlr.CharQueue
cons public init(int)
fld protected char[] buffer
fld protected int nbrEntries
meth public final char elementAt(int)
meth public final void append(char)
meth public final void removeFirst()
meth public final void reset()
meth public void init(int)
supr java.lang.Object
hfds offset,sizeLessOne

CLSS public abstract groovyjarjarantlr.CharScanner
cons public init()
cons public init(groovyjarjarantlr.InputBuffer)
cons public init(groovyjarjarantlr.LexerSharedInputState)
fld protected boolean caseSensitive
fld protected boolean caseSensitiveLiterals
fld protected boolean commitToPath
fld protected boolean saveConsumedInput
fld protected groovyjarjarantlr.ANTLRHashString hashString
fld protected groovyjarjarantlr.ANTLRStringBuffer text
fld protected groovyjarjarantlr.LexerSharedInputState inputState
fld protected groovyjarjarantlr.Token _returnToken
fld protected int tabsize
fld protected int traceDepth
fld protected java.lang.Class tokenObjectClass
fld protected java.util.Hashtable literals
fld public final static char EOF_CHAR = '\uffff'
intf groovyjarjarantlr.TokenStream
meth protected groovyjarjarantlr.Token makeToken(int)
meth public boolean getCaseSensitive()
meth public boolean getCommitToPath()
meth public char LA(int) throws groovyjarjarantlr.CharStreamException
meth public char toLower(char)
meth public final boolean getCaseSensitiveLiterals()
meth public groovyjarjarantlr.InputBuffer getInputBuffer()
meth public groovyjarjarantlr.LexerSharedInputState getInputState()
meth public groovyjarjarantlr.Token getTokenObject()
meth public int getColumn()
meth public int getLine()
meth public int getTabSize()
meth public int mark()
meth public int testLiteralsTable(int)
meth public int testLiteralsTable(java.lang.String,int)
meth public java.lang.String getFilename()
meth public java.lang.String getText()
meth public void append(char)
meth public void append(java.lang.String)
meth public void commit()
meth public void consume() throws groovyjarjarantlr.CharStreamException
meth public void consumeUntil(groovyjarjarantlr.collections.impl.BitSet) throws groovyjarjarantlr.CharStreamException
meth public void consumeUntil(int) throws groovyjarjarantlr.CharStreamException
meth public void match(char) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.MismatchedCharException
meth public void match(groovyjarjarantlr.collections.impl.BitSet) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.MismatchedCharException
meth public void match(java.lang.String) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.MismatchedCharException
meth public void matchNot(char) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.MismatchedCharException
meth public void matchRange(char,char) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.MismatchedCharException
meth public void newline()
meth public void panic()
meth public void panic(java.lang.String)
meth public void reportError(groovyjarjarantlr.RecognitionException)
meth public void reportError(java.lang.String)
meth public void reportWarning(java.lang.String)
meth public void resetText()
meth public void rewind(int)
meth public void setCaseSensitive(boolean)
meth public void setColumn(int)
meth public void setCommitToPath(boolean)
meth public void setFilename(java.lang.String)
meth public void setInputState(groovyjarjarantlr.LexerSharedInputState)
meth public void setLine(int)
meth public void setTabSize(int)
meth public void setText(java.lang.String)
meth public void setTokenObjectClass(java.lang.String)
meth public void tab()
meth public void traceIn(java.lang.String) throws groovyjarjarantlr.CharStreamException
meth public void traceIndent()
meth public void traceOut(java.lang.String) throws groovyjarjarantlr.CharStreamException
meth public void uponEOF() throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.TokenStreamException
supr java.lang.Object
hfds NO_CHAR

CLSS public groovyjarjarantlr.CharStreamException
cons public init(java.lang.String)
supr groovyjarjarantlr.ANTLRException

CLSS public groovyjarjarantlr.CharStreamIOException
cons public init(java.io.IOException)
fld public java.io.IOException io
supr groovyjarjarantlr.CharStreamException

CLSS public abstract groovyjarjarantlr.CodeGenerator
cons public init()
fld protected boolean DEBUG_CODE_GENERATOR
fld protected final static int BITSET_OPTIMIZE_INIT_THRESHOLD = 8
fld protected final static int DEFAULT_BITSET_TEST_THRESHOLD = 4
fld protected final static int DEFAULT_MAKE_SWITCH_THRESHOLD = 2
fld protected groovyjarjarantlr.CharFormatter charFormatter
fld protected groovyjarjarantlr.DefineGrammarSymbols behavior
fld protected groovyjarjarantlr.Grammar grammar
fld protected groovyjarjarantlr.LLkGrammarAnalyzer analyzer
fld protected groovyjarjarantlr.Tool antlrTool
fld protected groovyjarjarantlr.collections.impl.Vector bitsetsUsed
fld protected int bitsetTestThreshold
fld protected int makeSwitchThreshold
fld protected int tabs
fld protected java.io.PrintWriter currentOutput
fld public static java.lang.String TokenTypesFileExt
fld public static java.lang.String TokenTypesFileSuffix
meth protected abstract java.lang.String processActionForSpecialSymbols(java.lang.String,int,groovyjarjarantlr.RuleBlock,groovyjarjarantlr.ActionTransInfo)
meth protected int markBitsetForGen(groovyjarjarantlr.collections.impl.BitSet)
meth protected java.lang.String extractIdOfAction(groovyjarjarantlr.Token)
meth protected java.lang.String extractIdOfAction(java.lang.String,int,int)
meth protected java.lang.String extractTypeOfAction(groovyjarjarantlr.Token)
meth protected java.lang.String extractTypeOfAction(java.lang.String,int,int)
meth protected java.lang.String getBitsetName(int)
meth protected java.lang.String removeAssignmentFromDeclaration(java.lang.String)
meth protected void _print(java.lang.String)
meth protected void _printAction(java.lang.String)
meth protected void _println(java.lang.String)
meth protected void genTokenInterchange(groovyjarjarantlr.TokenManager) throws java.io.IOException
meth protected void print(java.lang.String)
meth protected void printAction(java.lang.String)
meth protected void printTabs()
meth protected void println(java.lang.String)
meth protected void setGrammar(groovyjarjarantlr.Grammar)
meth public abstract java.lang.String getASTCreateString(groovyjarjarantlr.GrammarAtom,java.lang.String)
meth public abstract java.lang.String getASTCreateString(groovyjarjarantlr.collections.impl.Vector)
meth public abstract java.lang.String mapTreeId(java.lang.String,groovyjarjarantlr.ActionTransInfo)
meth public abstract void gen()
meth public abstract void gen(groovyjarjarantlr.ActionElement)
meth public abstract void gen(groovyjarjarantlr.AlternativeBlock)
meth public abstract void gen(groovyjarjarantlr.BlockEndElement)
meth public abstract void gen(groovyjarjarantlr.CharLiteralElement)
meth public abstract void gen(groovyjarjarantlr.CharRangeElement)
meth public abstract void gen(groovyjarjarantlr.LexerGrammar) throws java.io.IOException
meth public abstract void gen(groovyjarjarantlr.OneOrMoreBlock)
meth public abstract void gen(groovyjarjarantlr.ParserGrammar) throws java.io.IOException
meth public abstract void gen(groovyjarjarantlr.RuleRefElement)
meth public abstract void gen(groovyjarjarantlr.StringLiteralElement)
meth public abstract void gen(groovyjarjarantlr.TokenRangeElement)
meth public abstract void gen(groovyjarjarantlr.TokenRefElement)
meth public abstract void gen(groovyjarjarantlr.TreeElement)
meth public abstract void gen(groovyjarjarantlr.TreeWalkerGrammar) throws java.io.IOException
meth public abstract void gen(groovyjarjarantlr.WildcardElement)
meth public abstract void gen(groovyjarjarantlr.ZeroOrMoreBlock)
meth public java.lang.String getFIRSTBitSet(java.lang.String,int)
meth public java.lang.String getFOLLOWBitSet(java.lang.String,int)
meth public java.lang.String processStringForASTConstructor(java.lang.String)
meth public static boolean elementsAreRange(int[])
meth public static java.lang.String decodeLexerRuleName(java.lang.String)
meth public static java.lang.String encodeLexerRuleName(java.lang.String)
meth public static java.lang.String reverseLexerRuleName(java.lang.String)
meth public void setAnalyzer(groovyjarjarantlr.LLkGrammarAnalyzer)
meth public void setBehavior(groovyjarjarantlr.DefineGrammarSymbols)
meth public void setTool(groovyjarjarantlr.Tool)
supr java.lang.Object
hfds OLD_ACTION_TRANSLATOR

CLSS public groovyjarjarantlr.CommonAST
cons public init()
cons public init(groovyjarjarantlr.Token)
meth public int getType()
meth public java.lang.String getText()
meth public void initialize(groovyjarjarantlr.Token)
meth public void initialize(groovyjarjarantlr.collections.AST)
meth public void initialize(int,java.lang.String)
meth public void setText(java.lang.String)
meth public void setType(int)
supr groovyjarjarantlr.BaseAST
hfds text,ttype

CLSS public groovyjarjarantlr.CommonASTWithHiddenTokens
cons public init()
cons public init(groovyjarjarantlr.Token)
fld protected groovyjarjarantlr.CommonHiddenStreamToken hiddenAfter
fld protected groovyjarjarantlr.CommonHiddenStreamToken hiddenBefore
meth public groovyjarjarantlr.CommonHiddenStreamToken getHiddenAfter()
meth public groovyjarjarantlr.CommonHiddenStreamToken getHiddenBefore()
meth public void initialize(groovyjarjarantlr.Token)
meth public void initialize(groovyjarjarantlr.collections.AST)
supr groovyjarjarantlr.CommonAST

CLSS public groovyjarjarantlr.CommonHiddenStreamToken
cons public init()
cons public init(int,java.lang.String)
cons public init(java.lang.String)
fld protected groovyjarjarantlr.CommonHiddenStreamToken hiddenAfter
fld protected groovyjarjarantlr.CommonHiddenStreamToken hiddenBefore
meth protected void setHiddenAfter(groovyjarjarantlr.CommonHiddenStreamToken)
meth protected void setHiddenBefore(groovyjarjarantlr.CommonHiddenStreamToken)
meth public groovyjarjarantlr.CommonHiddenStreamToken getHiddenAfter()
meth public groovyjarjarantlr.CommonHiddenStreamToken getHiddenBefore()
supr groovyjarjarantlr.CommonToken

CLSS public groovyjarjarantlr.CommonToken
cons public init()
cons public init(int,java.lang.String)
cons public init(java.lang.String)
fld protected int col
fld protected int line
fld protected java.lang.String text
meth public int getColumn()
meth public int getLine()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public void setColumn(int)
meth public void setLine(int)
meth public void setText(java.lang.String)
supr groovyjarjarantlr.Token

CLSS public groovyjarjarantlr.CppCodeGenerator
cons public init()
fld protected boolean genAST
fld protected boolean genHashLines
fld protected boolean noConstructors
fld protected boolean saveText
fld protected final static java.lang.String NONUNIQUE
fld protected int outputLine
fld protected int syntacticPredLevel
fld protected java.lang.String outputFile
fld public final static int caseSizeThreshold = 127
meth protected boolean lookaheadIsEmpty(groovyjarjarantlr.Alternative,int)
meth protected int addSemPred(java.lang.String)
meth protected int countLines(java.lang.String)
meth protected java.lang.String getLookaheadTestExpression(groovyjarjarantlr.Alternative,int)
meth protected java.lang.String getLookaheadTestExpression(groovyjarjarantlr.Lookahead[],int)
meth protected java.lang.String getLookaheadTestTerm(int,groovyjarjarantlr.collections.impl.BitSet)
meth protected java.lang.String processActionForSpecialSymbols(java.lang.String,int,groovyjarjarantlr.RuleBlock,groovyjarjarantlr.ActionTransInfo)
meth protected void _print(java.lang.String)
meth protected void _printAction(java.lang.String)
meth protected void _println(java.lang.String)
meth protected void genASTDeclaration(groovyjarjarantlr.AlternativeElement)
meth protected void genASTDeclaration(groovyjarjarantlr.AlternativeElement,java.lang.String)
meth protected void genASTDeclaration(groovyjarjarantlr.AlternativeElement,java.lang.String,java.lang.String)
meth protected void genAlt(groovyjarjarantlr.Alternative,groovyjarjarantlr.AlternativeBlock)
meth protected void genBitsets(groovyjarjarantlr.collections.impl.Vector,int,java.lang.String)
meth protected void genBitsetsHeader(groovyjarjarantlr.collections.impl.Vector,int)
meth protected void genBlockInitAction(groovyjarjarantlr.AlternativeBlock)
meth protected void genBlockPreamble(groovyjarjarantlr.AlternativeBlock)
meth protected void genCases(groovyjarjarantlr.collections.impl.BitSet)
meth protected void genHeader(java.lang.String)
meth protected void genMatch(groovyjarjarantlr.GrammarAtom)
meth protected void genMatch(groovyjarjarantlr.collections.impl.BitSet)
meth protected void genMatchUsingAtomText(groovyjarjarantlr.GrammarAtom)
meth protected void genMatchUsingAtomTokenType(groovyjarjarantlr.GrammarAtom)
meth protected void genSemPred(java.lang.String,int)
meth protected void genSemPredMap(java.lang.String)
meth protected void genSynPred(groovyjarjarantlr.SynPredBlock,java.lang.String)
meth protected void genTokenTypes(groovyjarjarantlr.TokenManager) throws java.io.IOException
meth protected void println(java.lang.String)
meth public java.lang.Object genCommonBlock(groovyjarjarantlr.AlternativeBlock,boolean)
meth public java.lang.String getASTCreateString(groovyjarjarantlr.GrammarAtom,java.lang.String)
meth public java.lang.String getASTCreateString(groovyjarjarantlr.collections.impl.Vector)
meth public java.lang.String getASTCreateString(java.lang.String)
meth public java.lang.String getRangeExpression(int,int[])
meth public java.lang.String mapTreeId(java.lang.String,groovyjarjarantlr.ActionTransInfo)
meth public java.lang.String processStringForASTConstructor(java.lang.String)
meth public void exitIfError()
meth public void gen()
meth public void gen(groovyjarjarantlr.ActionElement)
meth public void gen(groovyjarjarantlr.AlternativeBlock)
meth public void gen(groovyjarjarantlr.BlockEndElement)
meth public void gen(groovyjarjarantlr.CharLiteralElement)
meth public void gen(groovyjarjarantlr.CharRangeElement)
meth public void gen(groovyjarjarantlr.LexerGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.OneOrMoreBlock)
meth public void gen(groovyjarjarantlr.ParserGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.RuleRefElement)
meth public void gen(groovyjarjarantlr.StringLiteralElement)
meth public void gen(groovyjarjarantlr.TokenRangeElement)
meth public void gen(groovyjarjarantlr.TokenRefElement)
meth public void gen(groovyjarjarantlr.TreeElement)
meth public void gen(groovyjarjarantlr.TreeWalkerGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.WildcardElement)
meth public void gen(groovyjarjarantlr.ZeroOrMoreBlock)
meth public void genBody(groovyjarjarantlr.LexerGrammar) throws java.io.IOException
meth public void genBody(groovyjarjarantlr.ParserGrammar) throws java.io.IOException
meth public void genBody(groovyjarjarantlr.TreeWalkerGrammar) throws java.io.IOException
meth public void genInclude(groovyjarjarantlr.LexerGrammar) throws java.io.IOException
meth public void genInclude(groovyjarjarantlr.ParserGrammar) throws java.io.IOException
meth public void genInclude(groovyjarjarantlr.TreeWalkerGrammar) throws java.io.IOException
meth public void genInitFactory(groovyjarjarantlr.Grammar)
meth public void genLineNo(groovyjarjarantlr.GrammarElement)
meth public void genLineNo(groovyjarjarantlr.Token)
meth public void genLineNo(int)
meth public void genLineNo2()
meth public void genNextToken()
meth public void genRule(groovyjarjarantlr.RuleSymbol,boolean,int,java.lang.String)
meth public void genRuleHeader(groovyjarjarantlr.RuleSymbol,boolean)
meth public void genTokenStrings(java.lang.String)
meth public void printAction(groovyjarjarantlr.Token)
meth public void printHeaderAction(java.lang.String)
supr groovyjarjarantlr.CodeGenerator
hfds DEBUG_CPP_CODE_GENERATOR,astTypes,astVarNumber,commonExtraArgs,commonExtraParams,commonLocalVars,currentASTResult,currentRule,declaredASTVariables,exceptionThrown,labeledElementASTInit,labeledElementASTType,labeledElementInit,labeledElementType,lt1Value,nameSpace,namespaceAntlr,namespaceStd,postIncludeCpp,postIncludeHpp,preIncludeCpp,preIncludeHpp,semPreds,throwNoViable,treeVariableMap,usingCustomAST

CLSS public groovyjarjarantlr.DefaultFileLineFormatter
cons public init()
meth public java.lang.String getFormatString(java.lang.String,int,int)
supr groovyjarjarantlr.FileLineFormatter

CLSS public groovyjarjarantlr.DefaultJavaCodeGeneratorPrintWriterManager
cons public init()
intf groovyjarjarantlr.JavaCodeGeneratorPrintWriterManager
meth public int getCurrentOutputLine()
meth public java.io.PrintWriter setupOutput(groovyjarjarantlr.Tool,groovyjarjarantlr.Grammar) throws java.io.IOException
meth public java.io.PrintWriter setupOutput(groovyjarjarantlr.Tool,groovyjarjarantlr.Grammar,java.lang.String) throws java.io.IOException
meth public java.io.PrintWriter setupOutput(groovyjarjarantlr.Tool,java.lang.String) throws java.io.IOException
meth public java.util.Map getSourceMaps()
meth public void endMapping()
meth public void finishOutput() throws java.io.IOException
meth public void startMapping(int)
meth public void startSingleSourceLineMapping(int)
supr java.lang.Object
hfds currentFileName,currentOutput,grammar,smapOutput,sourceMaps,tool

CLSS public groovyjarjarantlr.DefineGrammarSymbols
cons public init(groovyjarjarantlr.Tool,java.lang.String[],groovyjarjarantlr.LLkAnalyzer)
fld protected groovyjarjarantlr.Grammar grammar
fld protected groovyjarjarantlr.Tool tool
fld protected int numLexers
fld protected int numParsers
fld protected int numTreeParsers
fld protected java.util.Hashtable grammars
fld protected java.util.Hashtable headerActions
fld protected java.util.Hashtable tokenManagers
intf groovyjarjarantlr.ANTLRGrammarParseBehavior
meth public int getHeaderActionLine(java.lang.String)
meth public java.lang.String getHeaderAction(java.lang.String)
meth public void _refStringLiteral(groovyjarjarantlr.Token,groovyjarjarantlr.Token,int,boolean)
meth public void _refToken(groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,boolean,int,boolean)
meth public void abortGrammar()
meth public void beginAlt(boolean)
meth public void beginChildList()
meth public void beginExceptionGroup()
meth public void beginExceptionSpec(groovyjarjarantlr.Token)
meth public void beginSubRule(groovyjarjarantlr.Token,groovyjarjarantlr.Token,boolean)
meth public void beginTree(groovyjarjarantlr.Token) throws groovyjarjarantlr.SemanticException
meth public void defineRuleName(groovyjarjarantlr.Token,java.lang.String,boolean,java.lang.String) throws groovyjarjarantlr.SemanticException
meth public void defineToken(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public void endAlt()
meth public void endChildList()
meth public void endExceptionGroup()
meth public void endExceptionSpec()
meth public void endGrammar()
meth public void endOptions()
meth public void endRule(java.lang.String)
meth public void endSubRule()
meth public void endTree()
meth public void hasError()
meth public void noASTSubRule()
meth public void oneOrMoreSubRule()
meth public void optionalSubRule()
meth public void refAction(groovyjarjarantlr.Token)
meth public void refArgAction(groovyjarjarantlr.Token)
meth public void refCharLiteral(groovyjarjarantlr.Token,groovyjarjarantlr.Token,boolean,int,boolean)
meth public void refCharRange(groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,int,boolean)
meth public void refElementOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public void refExceptionHandler(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public void refHeaderAction(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public void refInitAction(groovyjarjarantlr.Token)
meth public void refMemberAction(groovyjarjarantlr.Token)
meth public void refPreambleAction(groovyjarjarantlr.Token)
meth public void refReturnAction(groovyjarjarantlr.Token)
meth public void refRule(groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,int)
meth public void refSemPred(groovyjarjarantlr.Token)
meth public void refStringLiteral(groovyjarjarantlr.Token,groovyjarjarantlr.Token,int,boolean)
meth public void refToken(groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,boolean,int,boolean)
meth public void refTokenRange(groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,int,boolean)
meth public void refTokensSpecElementOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public void refTreeSpecifier(groovyjarjarantlr.Token)
meth public void refWildcard(groovyjarjarantlr.Token,groovyjarjarantlr.Token,int)
meth public void reset()
meth public void setArgOfRuleRef(groovyjarjarantlr.Token)
meth public void setCharVocabulary(groovyjarjarantlr.collections.impl.BitSet)
meth public void setFileOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token,java.lang.String)
meth public void setGrammarOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public void setRuleOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public void setSubruleOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public void setUserExceptions(java.lang.String)
meth public void startLexer(java.lang.String,groovyjarjarantlr.Token,java.lang.String,java.lang.String)
meth public void startParser(java.lang.String,groovyjarjarantlr.Token,java.lang.String,java.lang.String)
meth public void startTreeWalker(java.lang.String,groovyjarjarantlr.Token,java.lang.String,java.lang.String)
meth public void synPred()
meth public void zeroOrMoreSubRule()
supr java.lang.Object
hfds DEFAULT_TOKENMANAGER_NAME,analyzer,args,language,thePreambleAction

CLSS public groovyjarjarantlr.DiagnosticCodeGenerator
cons public init()
fld protected boolean doingLexRules
fld protected int syntacticPredLevel
meth protected java.lang.String processActionForSpecialSymbols(java.lang.String,int,groovyjarjarantlr.RuleBlock,groovyjarjarantlr.ActionTransInfo)
meth protected void genAlt(groovyjarjarantlr.Alternative)
meth protected void genBlockPreamble(groovyjarjarantlr.AlternativeBlock)
meth protected void genHeader()
meth protected void genLookaheadSetForAlt(groovyjarjarantlr.Alternative)
meth protected void genSynPred(groovyjarjarantlr.SynPredBlock)
meth protected void genTokenTypes(groovyjarjarantlr.TokenManager) throws java.io.IOException
meth public java.lang.String getASTCreateString(groovyjarjarantlr.GrammarAtom,java.lang.String)
meth public java.lang.String getASTCreateString(groovyjarjarantlr.collections.impl.Vector)
meth public java.lang.String mapTreeId(java.lang.String,groovyjarjarantlr.ActionTransInfo)
meth public void gen()
meth public void gen(groovyjarjarantlr.ActionElement)
meth public void gen(groovyjarjarantlr.AlternativeBlock)
meth public void gen(groovyjarjarantlr.BlockEndElement)
meth public void gen(groovyjarjarantlr.CharLiteralElement)
meth public void gen(groovyjarjarantlr.CharRangeElement)
meth public void gen(groovyjarjarantlr.LexerGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.OneOrMoreBlock)
meth public void gen(groovyjarjarantlr.ParserGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.RuleRefElement)
meth public void gen(groovyjarjarantlr.StringLiteralElement)
meth public void gen(groovyjarjarantlr.TokenRangeElement)
meth public void gen(groovyjarjarantlr.TokenRefElement)
meth public void gen(groovyjarjarantlr.TreeElement)
meth public void gen(groovyjarjarantlr.TreeWalkerGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.WildcardElement)
meth public void gen(groovyjarjarantlr.ZeroOrMoreBlock)
meth public void genCommonBlock(groovyjarjarantlr.AlternativeBlock)
meth public void genFollowSetForRuleBlock(groovyjarjarantlr.RuleBlock)
meth public void genLookaheadSetForBlock(groovyjarjarantlr.AlternativeBlock)
meth public void genNextToken()
meth public void genRule(groovyjarjarantlr.RuleSymbol)
meth public void printSet(int,int,groovyjarjarantlr.Lookahead)
supr groovyjarjarantlr.CodeGenerator

CLSS public groovyjarjarantlr.DocBookCodeGenerator
cons public init()
fld protected boolean doingLexRules
fld protected boolean firstElementInAlt
fld protected int syntacticPredLevel
fld protected java.lang.Object prevAltElem
meth protected java.lang.String processActionForSpecialSymbols(java.lang.String,int,groovyjarjarantlr.RuleBlock,groovyjarjarantlr.ActionTransInfo)
meth protected void genAlt(groovyjarjarantlr.Alternative)
meth protected void genGenericBlock(groovyjarjarantlr.AlternativeBlock,java.lang.String)
meth protected void genHeader()
meth protected void genLookaheadSetForAlt(groovyjarjarantlr.Alternative)
meth protected void genSynPred(groovyjarjarantlr.SynPredBlock)
meth protected void genTokenTypes(groovyjarjarantlr.TokenManager) throws java.io.IOException
meth public java.lang.String getASTCreateString(groovyjarjarantlr.GrammarAtom,java.lang.String)
meth public java.lang.String getASTCreateString(groovyjarjarantlr.collections.impl.Vector)
meth public java.lang.String mapTreeId(java.lang.String,groovyjarjarantlr.ActionTransInfo)
meth public void gen()
meth public void gen(groovyjarjarantlr.ActionElement)
meth public void gen(groovyjarjarantlr.AlternativeBlock)
meth public void gen(groovyjarjarantlr.BlockEndElement)
meth public void gen(groovyjarjarantlr.CharLiteralElement)
meth public void gen(groovyjarjarantlr.CharRangeElement)
meth public void gen(groovyjarjarantlr.LexerGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.OneOrMoreBlock)
meth public void gen(groovyjarjarantlr.ParserGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.RuleRefElement)
meth public void gen(groovyjarjarantlr.StringLiteralElement)
meth public void gen(groovyjarjarantlr.TokenRangeElement)
meth public void gen(groovyjarjarantlr.TokenRefElement)
meth public void gen(groovyjarjarantlr.TreeElement)
meth public void gen(groovyjarjarantlr.TreeWalkerGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.WildcardElement)
meth public void gen(groovyjarjarantlr.ZeroOrMoreBlock)
meth public void genCommonBlock(groovyjarjarantlr.AlternativeBlock)
meth public void genFollowSetForRuleBlock(groovyjarjarantlr.RuleBlock)
meth public void genLookaheadSetForBlock(groovyjarjarantlr.AlternativeBlock)
meth public void genNextToken()
meth public void genRule(groovyjarjarantlr.RuleSymbol)
meth public void genTail()
meth public void printSet(int,int,groovyjarjarantlr.Lookahead)
supr groovyjarjarantlr.CodeGenerator

CLSS public groovyjarjarantlr.DumpASTVisitor
cons public init()
fld protected int level
intf groovyjarjarantlr.ASTVisitor
meth public void visit(groovyjarjarantlr.collections.AST)
supr java.lang.Object

CLSS public abstract groovyjarjarantlr.FileLineFormatter
cons public init()
meth public abstract java.lang.String getFormatString(java.lang.String,int,int)
meth public static groovyjarjarantlr.FileLineFormatter getFormatter()
meth public static void setFormatter(groovyjarjarantlr.FileLineFormatter)
supr java.lang.Object
hfds formatter

CLSS public abstract groovyjarjarantlr.Grammar
cons public init(java.lang.String,groovyjarjarantlr.Tool,java.lang.String)
fld protected boolean analyzerDebug
fld protected boolean buildAST
fld protected boolean debuggingOutput
fld protected boolean defaultErrorHandler
fld protected boolean hasSyntacticPredicate
fld protected boolean hasUserErrorHandling
fld protected boolean interactive
fld protected boolean traceRules
fld protected groovyjarjarantlr.CodeGenerator generator
fld protected groovyjarjarantlr.LLkGrammarAnalyzer theLLkAnalyzer
fld protected groovyjarjarantlr.Token classMemberAction
fld protected groovyjarjarantlr.Token preambleAction
fld protected groovyjarjarantlr.Tool antlrTool
fld protected groovyjarjarantlr.collections.impl.Vector rules
fld protected int maxk
fld protected java.lang.Object tokenManager
fld protected java.lang.String className
fld protected java.lang.String comment
fld protected java.lang.String exportVocab
fld protected java.lang.String fileName
fld protected java.lang.String importVocab
fld protected java.lang.String superClass
fld protected java.util.Hashtable options
fld protected java.util.Hashtable symbols
meth protected abstract java.lang.String getSuperClass()
meth protected java.lang.String getClassName()
meth public abstract void generate() throws java.io.IOException
meth public abstract void processArguments(java.lang.String[])
meth public boolean getDefaultErrorHandler()
meth public boolean hasOption(java.lang.String)
meth public boolean isDefined(java.lang.String)
meth public boolean setOption(java.lang.String,groovyjarjarantlr.Token)
meth public groovyjarjarantlr.Token getOption(java.lang.String)
meth public int getIntegerOption(java.lang.String)
meth public java.lang.Object getSymbol(java.lang.String)
meth public java.lang.String getFilename()
meth public java.lang.String toString()
meth public java.util.Enumeration getSymbols()
meth public void define(groovyjarjarantlr.RuleSymbol)
meth public void setCodeGenerator(groovyjarjarantlr.CodeGenerator)
meth public void setFilename(java.lang.String)
meth public void setGrammarAnalyzer(groovyjarjarantlr.LLkGrammarAnalyzer)
meth public void setTokenManager(groovyjarjarantlr.TokenManager)
supr java.lang.Object

CLSS public abstract interface groovyjarjarantlr.GrammarAnalyzer
fld public final static int LOOKAHEAD_DEPTH_INIT = -1
fld public final static int NONDETERMINISTIC = 2147483647

CLSS public groovyjarjarantlr.HTMLCodeGenerator
cons public init()
fld protected boolean doingLexRules
fld protected boolean firstElementInAlt
fld protected int syntacticPredLevel
fld protected java.lang.Object prevAltElem
meth protected java.lang.String processActionForSpecialSymbols(java.lang.String,int,groovyjarjarantlr.RuleBlock,groovyjarjarantlr.ActionTransInfo)
meth protected void genAlt(groovyjarjarantlr.Alternative)
meth protected void genGenericBlock(groovyjarjarantlr.AlternativeBlock,java.lang.String)
meth protected void genHeader()
meth protected void genLookaheadSetForAlt(groovyjarjarantlr.Alternative)
meth protected void genSynPred(groovyjarjarantlr.SynPredBlock)
meth protected void genTokenTypes(groovyjarjarantlr.TokenManager) throws java.io.IOException
meth public java.lang.String getASTCreateString(groovyjarjarantlr.GrammarAtom,java.lang.String)
meth public java.lang.String getASTCreateString(groovyjarjarantlr.collections.impl.Vector)
meth public java.lang.String mapTreeId(java.lang.String,groovyjarjarantlr.ActionTransInfo)
meth public void gen()
meth public void gen(groovyjarjarantlr.ActionElement)
meth public void gen(groovyjarjarantlr.AlternativeBlock)
meth public void gen(groovyjarjarantlr.BlockEndElement)
meth public void gen(groovyjarjarantlr.CharLiteralElement)
meth public void gen(groovyjarjarantlr.CharRangeElement)
meth public void gen(groovyjarjarantlr.LexerGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.OneOrMoreBlock)
meth public void gen(groovyjarjarantlr.ParserGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.RuleRefElement)
meth public void gen(groovyjarjarantlr.StringLiteralElement)
meth public void gen(groovyjarjarantlr.TokenRangeElement)
meth public void gen(groovyjarjarantlr.TokenRefElement)
meth public void gen(groovyjarjarantlr.TreeElement)
meth public void gen(groovyjarjarantlr.TreeWalkerGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.WildcardElement)
meth public void gen(groovyjarjarantlr.ZeroOrMoreBlock)
meth public void genCommonBlock(groovyjarjarantlr.AlternativeBlock)
meth public void genFollowSetForRuleBlock(groovyjarjarantlr.RuleBlock)
meth public void genLookaheadSetForBlock(groovyjarjarantlr.AlternativeBlock)
meth public void genNextToken()
meth public void genRule(groovyjarjarantlr.RuleSymbol)
meth public void genTail()
meth public void printSet(int,int,groovyjarjarantlr.Lookahead)
supr groovyjarjarantlr.CodeGenerator

CLSS public abstract groovyjarjarantlr.InputBuffer
cons public init()
fld protected groovyjarjarantlr.CharQueue queue
fld protected int markerOffset
fld protected int nMarkers
fld protected int numToConsume
meth protected void syncConsume()
meth public abstract void fill(int) throws groovyjarjarantlr.CharStreamException
meth public boolean isMarked()
meth public char LA(int) throws groovyjarjarantlr.CharStreamException
meth public int mark()
meth public java.lang.String getLAChars()
meth public java.lang.String getMarkedChars()
meth public void commit()
meth public void consume()
meth public void reset()
meth public void rewind(int)
supr java.lang.Object

CLSS public groovyjarjarantlr.JavaCodeGenerator
cons public init()
fld protected boolean genAST
fld protected boolean saveText
fld protected final static java.lang.String NONUNIQUE
fld protected int syntacticPredLevel
fld public final static int CONTINUE_LAST_MAPPING = -888
fld public final static int NO_MAPPING = -999
fld public final static int caseSizeThreshold = 127
meth protected boolean lookaheadIsEmpty(groovyjarjarantlr.Alternative,int)
meth protected int addSemPred(java.lang.String)
meth protected java.lang.String getLookaheadTestExpression(groovyjarjarantlr.Alternative,int)
meth protected java.lang.String getLookaheadTestExpression(groovyjarjarantlr.Lookahead[],int)
meth protected java.lang.String getLookaheadTestTerm(int,groovyjarjarantlr.collections.impl.BitSet)
meth protected java.lang.String processActionForSpecialSymbols(java.lang.String,int,groovyjarjarantlr.RuleBlock,groovyjarjarantlr.ActionTransInfo)
meth protected void _print(java.lang.String)
meth protected void _print(java.lang.String,int)
meth protected void _println(java.lang.String)
meth protected void _println(java.lang.String,int)
meth protected void genASTDeclaration(groovyjarjarantlr.AlternativeElement)
meth protected void genASTDeclaration(groovyjarjarantlr.AlternativeElement,java.lang.String)
meth protected void genASTDeclaration(groovyjarjarantlr.AlternativeElement,java.lang.String,java.lang.String)
meth protected void genAlt(groovyjarjarantlr.Alternative,groovyjarjarantlr.AlternativeBlock)
meth protected void genBitsets(groovyjarjarantlr.collections.impl.Vector,int)
meth protected void genBlockInitAction(groovyjarjarantlr.AlternativeBlock)
meth protected void genBlockPreamble(groovyjarjarantlr.AlternativeBlock)
meth protected void genCases(groovyjarjarantlr.collections.impl.BitSet,int)
meth protected void genHeader()
meth protected void genMatch(groovyjarjarantlr.GrammarAtom)
meth protected void genMatch(groovyjarjarantlr.collections.impl.BitSet)
meth protected void genMatchUsingAtomText(groovyjarjarantlr.GrammarAtom)
meth protected void genMatchUsingAtomTokenType(groovyjarjarantlr.GrammarAtom)
meth protected void genSemPred(java.lang.String,int)
meth protected void genSemPredMap()
meth protected void genSynPred(groovyjarjarantlr.SynPredBlock,java.lang.String)
meth protected void genTokenASTNodeMap()
meth protected void genTokenTypes(groovyjarjarantlr.TokenManager) throws java.io.IOException
meth protected void print(java.lang.String)
meth protected void print(java.lang.String,int)
meth protected void printAction(java.lang.String)
meth protected void printAction(java.lang.String,int)
meth public groovyjarjarantlr.JavaCodeGeneratorPrintWriterManager getPrintWriterManager()
meth public java.lang.Object genCommonBlock(groovyjarjarantlr.AlternativeBlock,boolean)
meth public java.lang.String getASTCreateString(groovyjarjarantlr.GrammarAtom,java.lang.String)
meth public java.lang.String getASTCreateString(groovyjarjarantlr.collections.impl.Vector)
meth public java.lang.String getASTCreateString(java.lang.String)
meth public java.lang.String getRangeExpression(int,int[])
meth public java.lang.String mapTreeId(java.lang.String,groovyjarjarantlr.ActionTransInfo)
meth public void exitIfError()
meth public void gen()
meth public void gen(groovyjarjarantlr.ActionElement)
meth public void gen(groovyjarjarantlr.AlternativeBlock)
meth public void gen(groovyjarjarantlr.BlockEndElement)
meth public void gen(groovyjarjarantlr.CharLiteralElement)
meth public void gen(groovyjarjarantlr.CharRangeElement)
meth public void gen(groovyjarjarantlr.LexerGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.OneOrMoreBlock)
meth public void gen(groovyjarjarantlr.ParserGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.RuleRefElement)
meth public void gen(groovyjarjarantlr.StringLiteralElement)
meth public void gen(groovyjarjarantlr.TokenRangeElement)
meth public void gen(groovyjarjarantlr.TokenRefElement)
meth public void gen(groovyjarjarantlr.TreeElement)
meth public void gen(groovyjarjarantlr.TreeWalkerGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.WildcardElement)
meth public void gen(groovyjarjarantlr.ZeroOrMoreBlock)
meth public void genNextToken()
meth public void genRule(groovyjarjarantlr.RuleSymbol,boolean,int)
meth public void genTokenStrings()
meth public void println(java.lang.String)
meth public void println(java.lang.String,int)
meth public void setPrintWriterManager(groovyjarjarantlr.JavaCodeGeneratorPrintWriterManager)
meth public void setTool(groovyjarjarantlr.Tool)
supr groovyjarjarantlr.CodeGenerator
hfds astVarNumber,commonExtraArgs,commonExtraParams,commonLocalVars,currentASTResult,currentRule,declaredASTVariables,defaultLine,exceptionThrown,labeledElementASTType,labeledElementInit,labeledElementType,lt1Value,printWriterManager,semPreds,throwNoViable,treeVariableMap

CLSS public abstract interface groovyjarjarantlr.JavaCodeGeneratorPrintWriterManager
meth public abstract java.io.PrintWriter setupOutput(groovyjarjarantlr.Tool,groovyjarjarantlr.Grammar) throws java.io.IOException
meth public abstract java.io.PrintWriter setupOutput(groovyjarjarantlr.Tool,java.lang.String) throws java.io.IOException
meth public abstract java.util.Map getSourceMaps()
meth public abstract void endMapping()
meth public abstract void finishOutput() throws java.io.IOException
meth public abstract void startMapping(int)
meth public abstract void startSingleSourceLineMapping(int)

CLSS public groovyjarjarantlr.LLkAnalyzer
cons public init(groovyjarjarantlr.Tool)
fld protected boolean lexicalAnalysis
fld protected groovyjarjarantlr.Grammar grammar
fld protected groovyjarjarantlr.Tool tool
fld public boolean DEBUG_ANALYZER
intf groovyjarjarantlr.LLkGrammarAnalyzer
meth protected boolean altUsesWildcardDefault(groovyjarjarantlr.Alternative)
meth public boolean deterministic(groovyjarjarantlr.AlternativeBlock)
meth public boolean deterministic(groovyjarjarantlr.OneOrMoreBlock)
meth public boolean deterministic(groovyjarjarantlr.ZeroOrMoreBlock)
meth public boolean deterministicImpliedPath(groovyjarjarantlr.BlockWithImpliedExitPath)
meth public boolean subruleCanBeInverted(groovyjarjarantlr.AlternativeBlock,boolean)
meth public groovyjarjarantlr.Lookahead FOLLOW(int,groovyjarjarantlr.RuleEndElement)
meth public groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.ActionElement)
meth public groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.AlternativeBlock)
meth public groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.BlockEndElement)
meth public groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.CharLiteralElement)
meth public groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.CharRangeElement)
meth public groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.GrammarAtom)
meth public groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.OneOrMoreBlock)
meth public groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.RuleBlock)
meth public groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.RuleEndElement)
meth public groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.RuleRefElement)
meth public groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.StringLiteralElement)
meth public groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.SynPredBlock)
meth public groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.TokenRangeElement)
meth public groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.TreeElement)
meth public groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.WildcardElement)
meth public groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.ZeroOrMoreBlock)
meth public groovyjarjarantlr.Lookahead look(int,java.lang.String)
meth public static boolean lookaheadEquivForApproxAndFullAnalysis(groovyjarjarantlr.Lookahead[],int)
meth public void setGrammar(groovyjarjarantlr.Grammar)
supr java.lang.Object
hfds charFormatter,currentBlock

CLSS public abstract interface groovyjarjarantlr.LLkGrammarAnalyzer
intf groovyjarjarantlr.GrammarAnalyzer
meth public abstract boolean deterministic(groovyjarjarantlr.AlternativeBlock)
meth public abstract boolean deterministic(groovyjarjarantlr.OneOrMoreBlock)
meth public abstract boolean deterministic(groovyjarjarantlr.ZeroOrMoreBlock)
meth public abstract boolean subruleCanBeInverted(groovyjarjarantlr.AlternativeBlock,boolean)
meth public abstract groovyjarjarantlr.Lookahead FOLLOW(int,groovyjarjarantlr.RuleEndElement)
meth public abstract groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.ActionElement)
meth public abstract groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.AlternativeBlock)
meth public abstract groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.BlockEndElement)
meth public abstract groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.CharLiteralElement)
meth public abstract groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.CharRangeElement)
meth public abstract groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.GrammarAtom)
meth public abstract groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.OneOrMoreBlock)
meth public abstract groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.RuleBlock)
meth public abstract groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.RuleEndElement)
meth public abstract groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.RuleRefElement)
meth public abstract groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.StringLiteralElement)
meth public abstract groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.SynPredBlock)
meth public abstract groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.TokenRangeElement)
meth public abstract groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.TreeElement)
meth public abstract groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.WildcardElement)
meth public abstract groovyjarjarantlr.Lookahead look(int,groovyjarjarantlr.ZeroOrMoreBlock)
meth public abstract groovyjarjarantlr.Lookahead look(int,java.lang.String)
meth public abstract void setGrammar(groovyjarjarantlr.Grammar)

CLSS public groovyjarjarantlr.LLkParser
cons public init(groovyjarjarantlr.ParserSharedInputState,int)
cons public init(groovyjarjarantlr.TokenBuffer,int)
cons public init(groovyjarjarantlr.TokenStream,int)
cons public init(int)
meth public groovyjarjarantlr.Token LT(int) throws groovyjarjarantlr.TokenStreamException
meth public int LA(int) throws groovyjarjarantlr.TokenStreamException
meth public void consume() throws groovyjarjarantlr.TokenStreamException
meth public void traceIn(java.lang.String) throws groovyjarjarantlr.TokenStreamException
meth public void traceOut(java.lang.String) throws groovyjarjarantlr.TokenStreamException
supr groovyjarjarantlr.Parser
hfds k

CLSS public groovyjarjarantlr.LexerSharedInputState
cons public init(groovyjarjarantlr.InputBuffer)
cons public init(java.io.InputStream)
cons public init(java.io.Reader)
fld protected groovyjarjarantlr.InputBuffer input
fld protected int column
fld protected int line
fld protected int tokenStartColumn
fld protected int tokenStartLine
fld protected java.lang.String filename
fld public int guessing
meth public groovyjarjarantlr.InputBuffer getInput()
meth public int getColumn()
meth public int getLine()
meth public int getTokenStartColumn()
meth public int getTokenStartLine()
meth public java.lang.String getFilename()
meth public void reset()
supr java.lang.Object

CLSS public groovyjarjarantlr.Lookahead
cons public init()
cons public init(groovyjarjarantlr.collections.impl.BitSet)
cons public init(java.lang.String)
intf java.lang.Cloneable
meth public boolean containsEpsilon()
meth public boolean nil()
meth public groovyjarjarantlr.Lookahead intersection(groovyjarjarantlr.Lookahead)
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public java.lang.String toString(java.lang.String,groovyjarjarantlr.CharFormatter)
meth public java.lang.String toString(java.lang.String,groovyjarjarantlr.CharFormatter,groovyjarjarantlr.Grammar)
meth public java.lang.String toString(java.lang.String,groovyjarjarantlr.collections.impl.Vector)
meth public static groovyjarjarantlr.Lookahead of(int)
meth public void combineWith(groovyjarjarantlr.Lookahead)
meth public void resetEpsilon()
meth public void setEpsilon()
supr java.lang.Object
hfds cycle,epsilonDepth,fset,hasEpsilon

CLSS public groovyjarjarantlr.MakeGrammar
cons public init(groovyjarjarantlr.Tool,java.lang.String[],groovyjarjarantlr.LLkAnalyzer)
fld protected boolean grammarError
fld protected groovyjarjarantlr.RuleBlock ruleBlock
fld protected groovyjarjarantlr.collections.Stack blocks
fld protected int nested
fld protected java.lang.Object lastRuleRef
fld protected java.lang.Object ruleEnd
meth protected void addElementToCurrentAlt(groovyjarjarantlr.AlternativeElement)
meth public java.lang.Object context()
meth public static groovyjarjarantlr.RuleBlock createNextTokenRule(groovyjarjarantlr.Grammar,groovyjarjarantlr.collections.impl.Vector,java.lang.String)
meth public static void setBlock(groovyjarjarantlr.AlternativeBlock,groovyjarjarantlr.AlternativeBlock)
meth public void abortGrammar()
meth public void beginAlt(boolean)
meth public void beginChildList()
meth public void beginExceptionGroup()
meth public void beginExceptionSpec(groovyjarjarantlr.Token)
meth public void beginSubRule(groovyjarjarantlr.Token,groovyjarjarantlr.Token,boolean)
meth public void beginTree(groovyjarjarantlr.Token) throws groovyjarjarantlr.SemanticException
meth public void defineRuleName(groovyjarjarantlr.Token,java.lang.String,boolean,java.lang.String) throws groovyjarjarantlr.SemanticException
meth public void endAlt()
meth public void endChildList()
meth public void endExceptionGroup()
meth public void endExceptionSpec()
meth public void endGrammar()
meth public void endRule(java.lang.String)
meth public void endSubRule()
meth public void endTree()
meth public void hasError()
meth public void noAutoGenSubRule()
meth public void oneOrMoreSubRule()
meth public void optionalSubRule()
meth public void refAction(groovyjarjarantlr.Token)
meth public void refArgAction(groovyjarjarantlr.Token)
meth public void refCharLiteral(groovyjarjarantlr.Token,groovyjarjarantlr.Token,boolean,int,boolean)
meth public void refCharRange(groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,int,boolean)
meth public void refElementOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public void refExceptionHandler(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public void refInitAction(groovyjarjarantlr.Token)
meth public void refMemberAction(groovyjarjarantlr.Token)
meth public void refPreambleAction(groovyjarjarantlr.Token)
meth public void refReturnAction(groovyjarjarantlr.Token)
meth public void refRule(groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,int)
meth public void refSemPred(groovyjarjarantlr.Token)
meth public void refStringLiteral(groovyjarjarantlr.Token,groovyjarjarantlr.Token,int,boolean)
meth public void refToken(groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,boolean,int,boolean)
meth public void refTokenRange(groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token,int,boolean)
meth public void refTokensSpecElementOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public void refTreeSpecifier(groovyjarjarantlr.Token)
meth public void refWildcard(groovyjarjarantlr.Token,groovyjarjarantlr.Token,int)
meth public void reset()
meth public void setArgOfRuleRef(groovyjarjarantlr.Token)
meth public void setRuleOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public void setSubruleOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public void setUserExceptions(java.lang.String)
meth public void synPred()
meth public void zeroOrMoreSubRule()
supr groovyjarjarantlr.DefineGrammarSymbols
hfds currentExceptionSpec

CLSS public groovyjarjarantlr.MismatchedCharException
cons public init()
cons public init(char,char,boolean,groovyjarjarantlr.CharScanner)
cons public init(char,char,char,boolean,groovyjarjarantlr.CharScanner)
cons public init(char,groovyjarjarantlr.collections.impl.BitSet,boolean,groovyjarjarantlr.CharScanner)
fld public final static int CHAR = 1
fld public final static int NOT_CHAR = 2
fld public final static int NOT_RANGE = 4
fld public final static int NOT_SET = 6
fld public final static int RANGE = 3
fld public final static int SET = 5
fld public groovyjarjarantlr.CharScanner scanner
fld public groovyjarjarantlr.collections.impl.BitSet set
fld public int expecting
fld public int foundChar
fld public int mismatchType
fld public int upper
meth public java.lang.String getMessage()
supr groovyjarjarantlr.RecognitionException

CLSS public groovyjarjarantlr.MismatchedTokenException
cons public init()
cons public init(java.lang.String[],groovyjarjarantlr.Token,groovyjarjarantlr.collections.impl.BitSet,boolean,java.lang.String)
cons public init(java.lang.String[],groovyjarjarantlr.Token,int,boolean,java.lang.String)
cons public init(java.lang.String[],groovyjarjarantlr.Token,int,int,boolean,java.lang.String)
cons public init(java.lang.String[],groovyjarjarantlr.collections.AST,groovyjarjarantlr.collections.impl.BitSet,boolean)
cons public init(java.lang.String[],groovyjarjarantlr.collections.AST,int,boolean)
cons public init(java.lang.String[],groovyjarjarantlr.collections.AST,int,int,boolean)
fld public final static int NOT_RANGE = 4
fld public final static int NOT_SET = 6
fld public final static int NOT_TOKEN = 2
fld public final static int RANGE = 3
fld public final static int SET = 5
fld public final static int TOKEN = 1
fld public groovyjarjarantlr.Token token
fld public groovyjarjarantlr.collections.AST node
fld public groovyjarjarantlr.collections.impl.BitSet set
fld public int expecting
fld public int mismatchType
fld public int upper
meth public java.lang.String getMessage()
supr groovyjarjarantlr.RecognitionException
hfds tokenNames,tokenText

CLSS public groovyjarjarantlr.NameSpace
cons public init(java.lang.String)
meth protected void parse(java.lang.String)
meth public java.lang.String getName()
supr java.lang.Object
hfds _name,names

CLSS public groovyjarjarantlr.NoViableAltException
cons public init(groovyjarjarantlr.Token,java.lang.String)
cons public init(groovyjarjarantlr.collections.AST)
fld public groovyjarjarantlr.Token token
fld public groovyjarjarantlr.collections.AST node
meth public java.lang.String getMessage()
supr groovyjarjarantlr.RecognitionException

CLSS public groovyjarjarantlr.NoViableAltForCharException
cons public init(char,groovyjarjarantlr.CharScanner)
cons public init(char,java.lang.String,int)
cons public init(char,java.lang.String,int,int)
fld public char foundChar
meth public java.lang.String getMessage()
supr groovyjarjarantlr.RecognitionException

CLSS public abstract groovyjarjarantlr.ParseTree
cons public init()
meth protected abstract int getLeftmostDerivation(java.lang.StringBuffer,int)
meth public java.lang.String getLeftmostDerivation(int)
meth public java.lang.String getLeftmostDerivationStep(int)
meth public void initialize(groovyjarjarantlr.Token)
meth public void initialize(groovyjarjarantlr.collections.AST)
meth public void initialize(int,java.lang.String)
supr groovyjarjarantlr.BaseAST

CLSS public groovyjarjarantlr.ParseTreeRule
cons public init(java.lang.String)
cons public init(java.lang.String,int)
fld protected int altNumber
fld protected java.lang.String ruleName
fld public final static int INVALID_ALT = -1
meth protected int getLeftmostDerivation(java.lang.StringBuffer,int)
meth public java.lang.String getRuleName()
meth public java.lang.String toString()
supr groovyjarjarantlr.ParseTree

CLSS public groovyjarjarantlr.ParseTreeToken
cons public init(groovyjarjarantlr.Token)
fld protected groovyjarjarantlr.Token token
meth protected int getLeftmostDerivation(java.lang.StringBuffer,int)
meth public java.lang.String toString()
supr groovyjarjarantlr.ParseTree

CLSS public abstract groovyjarjarantlr.Parser
cons public init()
cons public init(groovyjarjarantlr.ParserSharedInputState)
fld protected groovyjarjarantlr.ASTFactory astFactory
fld protected groovyjarjarantlr.ParserSharedInputState inputState
fld protected groovyjarjarantlr.collections.AST returnAST
fld protected int traceDepth
fld protected java.lang.String[] tokenNames
fld protected java.util.Hashtable tokenTypeToASTClassMap
meth protected void defaultDebuggingSetup(groovyjarjarantlr.TokenStream,groovyjarjarantlr.TokenBuffer)
meth public abstract groovyjarjarantlr.Token LT(int) throws groovyjarjarantlr.TokenStreamException
meth public abstract int LA(int) throws groovyjarjarantlr.TokenStreamException
meth public abstract void consume() throws groovyjarjarantlr.TokenStreamException
meth public boolean isDebugMode()
meth public groovyjarjarantlr.ASTFactory getASTFactory()
meth public groovyjarjarantlr.ParserSharedInputState getInputState()
meth public groovyjarjarantlr.collections.AST getAST()
meth public int mark()
meth public java.lang.String getFilename()
meth public java.lang.String getTokenName(int)
meth public java.lang.String[] getTokenNames()
meth public java.util.Hashtable getTokenTypeToASTClassMap()
meth public static void panic()
meth public void addMessageListener(groovyjarjarantlr.debug.MessageListener)
meth public void addParserListener(groovyjarjarantlr.debug.ParserListener)
meth public void addParserMatchListener(groovyjarjarantlr.debug.ParserMatchListener)
meth public void addParserTokenListener(groovyjarjarantlr.debug.ParserTokenListener)
meth public void addSemanticPredicateListener(groovyjarjarantlr.debug.SemanticPredicateListener)
meth public void addSyntacticPredicateListener(groovyjarjarantlr.debug.SyntacticPredicateListener)
meth public void addTraceListener(groovyjarjarantlr.debug.TraceListener)
meth public void consumeUntil(groovyjarjarantlr.collections.impl.BitSet) throws groovyjarjarantlr.TokenStreamException
meth public void consumeUntil(int) throws groovyjarjarantlr.TokenStreamException
meth public void match(groovyjarjarantlr.collections.impl.BitSet) throws groovyjarjarantlr.MismatchedTokenException,groovyjarjarantlr.TokenStreamException
meth public void match(int) throws groovyjarjarantlr.MismatchedTokenException,groovyjarjarantlr.TokenStreamException
meth public void matchNot(int) throws groovyjarjarantlr.MismatchedTokenException,groovyjarjarantlr.TokenStreamException
meth public void recover(groovyjarjarantlr.RecognitionException,groovyjarjarantlr.collections.impl.BitSet) throws groovyjarjarantlr.TokenStreamException
meth public void removeMessageListener(groovyjarjarantlr.debug.MessageListener)
meth public void removeParserListener(groovyjarjarantlr.debug.ParserListener)
meth public void removeParserMatchListener(groovyjarjarantlr.debug.ParserMatchListener)
meth public void removeParserTokenListener(groovyjarjarantlr.debug.ParserTokenListener)
meth public void removeSemanticPredicateListener(groovyjarjarantlr.debug.SemanticPredicateListener)
meth public void removeSyntacticPredicateListener(groovyjarjarantlr.debug.SyntacticPredicateListener)
meth public void removeTraceListener(groovyjarjarantlr.debug.TraceListener)
meth public void reportError(groovyjarjarantlr.RecognitionException)
meth public void reportError(java.lang.String)
meth public void reportWarning(java.lang.String)
meth public void rewind(int)
meth public void setASTFactory(groovyjarjarantlr.ASTFactory)
meth public void setASTNodeClass(java.lang.String)
meth public void setASTNodeType(java.lang.String)
meth public void setDebugMode(boolean)
meth public void setFilename(java.lang.String)
meth public void setIgnoreInvalidDebugCalls(boolean)
meth public void setInputState(groovyjarjarantlr.ParserSharedInputState)
meth public void setTokenBuffer(groovyjarjarantlr.TokenBuffer)
meth public void traceIn(java.lang.String) throws groovyjarjarantlr.TokenStreamException
meth public void traceIndent()
meth public void traceOut(java.lang.String) throws groovyjarjarantlr.TokenStreamException
supr java.lang.Object
hfds ignoreInvalidDebugCalls

CLSS public groovyjarjarantlr.ParserSharedInputState
cons public init()
fld protected groovyjarjarantlr.TokenBuffer input
fld protected java.lang.String filename
fld public int guessing
meth public groovyjarjarantlr.TokenBuffer getInput()
meth public java.lang.String getFilename()
meth public void reset()
supr java.lang.Object

CLSS public groovyjarjarantlr.PreservingFileWriter
cons public init(java.lang.String) throws java.io.IOException
fld protected java.io.File target_file
fld protected java.io.File tmp_file
meth public void close() throws java.io.IOException
supr java.io.FileWriter

CLSS public groovyjarjarantlr.PrintWriterWithSMAP
cons public init(java.io.OutputStream)
cons public init(java.io.OutputStream,boolean)
cons public init(java.io.Writer)
cons public init(java.io.Writer,boolean)
meth protected void mapLine(boolean)
meth public int getCurrentOutputLine()
meth public java.util.Map getSourceMap()
meth public void checkChar(int)
meth public void dump(java.io.PrintWriter,java.lang.String,java.lang.String)
meth public void endMapping()
meth public void println()
meth public void startMapping(int)
meth public void startSingleSourceLineMapping(int)
meth public void write(char[],int,int)
meth public void write(int)
meth public void write(java.lang.String,int,int)
supr java.io.PrintWriter
hfds anythingWrittenSinceMapping,currentOutputLine,currentSourceLine,lastPrintCharacterWasCR,mapLines,mapSingleSourceLine,sourceMap

CLSS public groovyjarjarantlr.PythonCharFormatter
cons public init()
intf groovyjarjarantlr.CharFormatter
meth public java.lang.String _escapeChar(int,boolean)
meth public java.lang.String escapeChar(int,boolean)
meth public java.lang.String escapeString(java.lang.String)
meth public java.lang.String literalChar(int)
meth public java.lang.String literalString(java.lang.String)
supr java.lang.Object

CLSS public groovyjarjarantlr.PythonCodeGenerator
cons public init()
fld protected boolean genAST
fld protected boolean saveText
fld protected final static java.lang.String NONUNIQUE
fld protected int syntacticPredLevel
fld public final static int caseSizeThreshold = 127
fld public final static java.lang.String initHeaderAction = "__init__"
fld public final static java.lang.String mainHeaderAction = "__main__"
meth protected boolean isspace(char)
meth protected boolean lookaheadIsEmpty(groovyjarjarantlr.Alternative,int)
meth protected int addSemPred(java.lang.String)
meth protected java.lang.String extractIdOfAction(java.lang.String,int,int)
meth protected java.lang.String extractTypeOfAction(java.lang.String,int,int)
meth protected java.lang.String getLookaheadTestExpression(groovyjarjarantlr.Alternative,int)
meth protected java.lang.String getLookaheadTestExpression(groovyjarjarantlr.Lookahead[],int)
meth protected java.lang.String getLookaheadTestTerm(int,groovyjarjarantlr.collections.impl.BitSet)
meth protected java.lang.String processActionCode(java.lang.String,int)
meth protected java.lang.String processActionForSpecialSymbols(java.lang.String,int,groovyjarjarantlr.RuleBlock,groovyjarjarantlr.ActionTransInfo)
meth protected void _printAction(java.lang.String)
meth protected void _printJavadoc(java.lang.String)
meth protected void checkCurrentOutputStream()
meth protected void flushTokens()
meth protected void genASTDeclaration(groovyjarjarantlr.AlternativeElement)
meth protected void genASTDeclaration(groovyjarjarantlr.AlternativeElement,java.lang.String)
meth protected void genASTDeclaration(groovyjarjarantlr.AlternativeElement,java.lang.String,java.lang.String)
meth protected void genAlt(groovyjarjarantlr.Alternative,groovyjarjarantlr.AlternativeBlock)
meth protected void genBitsets(groovyjarjarantlr.collections.impl.Vector,int)
meth protected void genBlockInitAction(groovyjarjarantlr.AlternativeBlock)
meth protected void genBlockPreamble(groovyjarjarantlr.AlternativeBlock)
meth protected void genCases(groovyjarjarantlr.collections.impl.BitSet)
meth protected void genHeader()
meth protected void genHeaderInit(groovyjarjarantlr.Grammar)
meth protected void genHeaderMain(groovyjarjarantlr.Grammar)
meth protected void genJavadocComment(groovyjarjarantlr.Grammar)
meth protected void genJavadocComment(groovyjarjarantlr.RuleSymbol)
meth protected void genLexerTest()
meth protected void genMatch(groovyjarjarantlr.GrammarAtom)
meth protected void genMatch(groovyjarjarantlr.collections.impl.BitSet)
meth protected void genMatchUsingAtomText(groovyjarjarantlr.GrammarAtom)
meth protected void genMatchUsingAtomTokenType(groovyjarjarantlr.GrammarAtom)
meth protected void genSemPred(java.lang.String,int)
meth protected void genSemPredMap()
meth protected void genSynPred(groovyjarjarantlr.SynPredBlock,java.lang.String)
meth protected void genTokenASTNodeMap()
meth protected void genTokenTypes(groovyjarjarantlr.TokenManager) throws java.io.IOException
meth protected void od(java.lang.String,int,int,java.lang.String)
meth protected void printAction(java.lang.String)
meth protected void printActionCode(java.lang.String,int)
meth protected void printGrammarAction(groovyjarjarantlr.Grammar)
meth protected void printMainFunc(java.lang.String)
meth protected void printTabs()
meth public java.lang.Object genCommonBlock(groovyjarjarantlr.AlternativeBlock,boolean)
meth public java.lang.String getASTCreateString(groovyjarjarantlr.GrammarAtom,java.lang.String)
meth public java.lang.String getASTCreateString(groovyjarjarantlr.collections.impl.Vector)
meth public java.lang.String getASTCreateString(java.lang.String)
meth public java.lang.String getRangeExpression(int,int[])
meth public java.lang.String mapTreeId(java.lang.String,groovyjarjarantlr.ActionTransInfo)
meth public void exitIfError()
meth public void gen()
meth public void gen(groovyjarjarantlr.ActionElement)
meth public void gen(groovyjarjarantlr.AlternativeBlock)
meth public void gen(groovyjarjarantlr.BlockEndElement)
meth public void gen(groovyjarjarantlr.CharLiteralElement)
meth public void gen(groovyjarjarantlr.CharRangeElement)
meth public void gen(groovyjarjarantlr.LexerGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.OneOrMoreBlock)
meth public void gen(groovyjarjarantlr.ParserGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.RuleRefElement)
meth public void gen(groovyjarjarantlr.StringLiteralElement)
meth public void gen(groovyjarjarantlr.TokenRangeElement)
meth public void gen(groovyjarjarantlr.TokenRefElement)
meth public void gen(groovyjarjarantlr.TreeElement)
meth public void gen(groovyjarjarantlr.TreeWalkerGrammar) throws java.io.IOException
meth public void gen(groovyjarjarantlr.WildcardElement)
meth public void gen(groovyjarjarantlr.ZeroOrMoreBlock)
meth public void genNextToken()
meth public void genRule(groovyjarjarantlr.RuleSymbol,boolean,int)
meth public void genTokenStrings()
meth public void setupOutput(java.lang.String) throws java.io.IOException
supr groovyjarjarantlr.CodeGenerator
hfds astVarNumber,commonExtraArgs,commonExtraParams,commonLocalVars,currentASTResult,currentRule,declaredASTVariables,exceptionThrown,labeledElementASTType,labeledElementInit,labeledElementType,lexerClassName,lt1Value,parserClassName,semPreds,throwNoViable,treeVariableMap,treeWalkerClassName

CLSS public groovyjarjarantlr.RecognitionException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String,int)
cons public init(java.lang.String,java.lang.String,int,int)
fld public int column
fld public int line
fld public java.lang.String fileName
meth public int getColumn()
meth public int getLine()
meth public java.lang.String getErrorMessage()
meth public java.lang.String getFilename()
meth public java.lang.String toString()
supr groovyjarjarantlr.ANTLRException

CLSS public groovyjarjarantlr.RuleBlock
cons public init(groovyjarjarantlr.Grammar,java.lang.String)
cons public init(groovyjarjarantlr.Grammar,java.lang.String,int,boolean)
fld protected boolean defaultErrorHandler
fld protected boolean doAutoGen
fld protected boolean generateAmbigWarnings
fld protected boolean hasASynPred
fld protected boolean hasAnAction
fld protected boolean testLiterals
fld protected boolean warnWhenFollowAmbig
fld protected boolean[] lock
fld protected groovyjarjarantlr.Grammar grammar
fld protected groovyjarjarantlr.Lookahead[] cache
fld protected groovyjarjarantlr.collections.impl.Vector alternatives
fld protected int ID
fld protected int alti
fld protected int altj
fld protected int analysisAlt
fld protected int autoGenType
fld protected int column
fld protected int line
fld protected java.lang.Object endNode
fld protected java.lang.String argAction
fld protected java.lang.String enclosingRuleName
fld protected java.lang.String ignoreRule
fld protected java.lang.String initAction
fld protected java.lang.String label
fld protected java.lang.String returnAction
fld protected java.lang.String ruleName
fld protected java.lang.String throwsSpec
fld protected static int nblks
fld public final static int AUTO_GEN_BANG = 3
fld public final static int AUTO_GEN_CARET = 2
fld public final static int AUTO_GEN_NONE = 1
meth public boolean getAutoGen()
meth public boolean getDefaultErrorHandler()
meth public boolean getTestLiterals()
meth public boolean isLexerAutoGenRule()
meth public groovyjarjarantlr.Lookahead look(int)
meth public groovyjarjarantlr.collections.impl.Vector getAlternatives()
meth public int getAutoGenType()
meth public int getColumn()
meth public int getLine()
meth public java.lang.Object findExceptionSpec(groovyjarjarantlr.Token)
meth public java.lang.Object findExceptionSpec(java.lang.String)
meth public java.lang.Object getAlternativeAt(int)
meth public java.lang.Object getEndElement()
meth public java.lang.String getIgnoreRule()
meth public java.lang.String getInitAction()
meth public java.lang.String getLabel()
meth public java.lang.String getRuleName()
meth public java.lang.String toString()
meth public void addAlternative(groovyjarjarantlr.Alternative)
meth public void addExceptionSpec(groovyjarjarantlr.ExceptionSpec)
meth public void generate()
meth public void prepareForAnalysis()
meth public void removeTrackingOfRuleRefs(groovyjarjarantlr.Grammar)
meth public void setAlternatives(groovyjarjarantlr.collections.impl.Vector)
meth public void setAutoGen(boolean)
meth public void setAutoGenType(int)
meth public void setDefaultErrorHandler(boolean)
meth public void setEndElement(groovyjarjarantlr.RuleEndElement)
meth public void setInitAction(java.lang.String)
meth public void setLabel(java.lang.String)
meth public void setOption(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
supr java.lang.Object
hfds exceptionSpecs,labeledElements

CLSS public groovyjarjarantlr.SemanticException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String,int)
cons public init(java.lang.String,java.lang.String,int,int)
supr groovyjarjarantlr.RecognitionException

CLSS public groovyjarjarantlr.StringUtils
cons public init()
meth public static java.lang.String stripBack(java.lang.String,char)
meth public static java.lang.String stripBack(java.lang.String,java.lang.String)
meth public static java.lang.String stripFront(java.lang.String,char)
meth public static java.lang.String stripFront(java.lang.String,java.lang.String)
meth public static java.lang.String stripFrontBack(java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public groovyjarjarantlr.Token
cons public init()
cons public init(int)
cons public init(int,java.lang.String)
fld protected int type
fld public final static int EOF_TYPE = 1
fld public final static int INVALID_TYPE = 0
fld public final static int MIN_USER_TYPE = 4
fld public final static int NULL_TREE_LOOKAHEAD = 3
fld public final static int SKIP = -1
fld public static groovyjarjarantlr.Token badToken
intf java.lang.Cloneable
meth public int getColumn()
meth public int getLine()
meth public int getType()
meth public java.lang.String getFilename()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public void setColumn(int)
meth public void setFilename(java.lang.String)
meth public void setLine(int)
meth public void setText(java.lang.String)
meth public void setType(int)
supr java.lang.Object

CLSS public groovyjarjarantlr.TokenBuffer
cons public init(groovyjarjarantlr.TokenStream)
fld protected groovyjarjarantlr.TokenStream input
meth public final groovyjarjarantlr.Token LT(int) throws groovyjarjarantlr.TokenStreamException
meth public final int LA(int) throws groovyjarjarantlr.TokenStreamException
meth public final int mark()
meth public final void consume()
meth public final void reset()
meth public final void rewind(int)
meth public groovyjarjarantlr.TokenStream getInput()
supr java.lang.Object
hfds markerOffset,nMarkers,numToConsume,queue

CLSS public abstract interface groovyjarjarantlr.TokenStream
meth public abstract groovyjarjarantlr.Token nextToken() throws groovyjarjarantlr.TokenStreamException

CLSS public groovyjarjarantlr.TokenStreamBasicFilter
cons public init(groovyjarjarantlr.TokenStream)
fld protected groovyjarjarantlr.TokenStream input
fld protected groovyjarjarantlr.collections.impl.BitSet discardMask
intf groovyjarjarantlr.ASdebug.IASDebugStream
intf groovyjarjarantlr.TokenStream
meth public groovyjarjarantlr.ASdebug.TokenOffsetInfo getOffsetInfo(groovyjarjarantlr.Token)
meth public groovyjarjarantlr.Token nextToken() throws groovyjarjarantlr.TokenStreamException
meth public java.lang.String getEntireText()
meth public void discard(groovyjarjarantlr.collections.impl.BitSet)
meth public void discard(int)
supr java.lang.Object

CLSS public groovyjarjarantlr.TokenStreamException
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr groovyjarjarantlr.ANTLRException

CLSS public groovyjarjarantlr.TokenStreamHiddenTokenFilter
cons public init(groovyjarjarantlr.TokenStream)
fld protected groovyjarjarantlr.CommonHiddenStreamToken firstHidden
fld protected groovyjarjarantlr.CommonHiddenStreamToken lastHiddenToken
fld protected groovyjarjarantlr.CommonHiddenStreamToken nextMonitoredToken
fld protected groovyjarjarantlr.collections.impl.BitSet hideMask
intf groovyjarjarantlr.TokenStream
meth protected groovyjarjarantlr.CommonHiddenStreamToken LA(int)
meth protected void consume() throws groovyjarjarantlr.TokenStreamException
meth public groovyjarjarantlr.CommonHiddenStreamToken getHiddenAfter(groovyjarjarantlr.CommonHiddenStreamToken)
meth public groovyjarjarantlr.CommonHiddenStreamToken getHiddenBefore(groovyjarjarantlr.CommonHiddenStreamToken)
meth public groovyjarjarantlr.CommonHiddenStreamToken getInitialHiddenToken()
meth public groovyjarjarantlr.Token nextToken() throws groovyjarjarantlr.TokenStreamException
meth public groovyjarjarantlr.collections.impl.BitSet getDiscardMask()
meth public groovyjarjarantlr.collections.impl.BitSet getHideMask()
meth public void hide(groovyjarjarantlr.collections.impl.BitSet)
meth public void hide(int)
supr groovyjarjarantlr.TokenStreamBasicFilter

CLSS public groovyjarjarantlr.TokenStreamIOException
cons public init(java.io.IOException)
fld public java.io.IOException io
supr groovyjarjarantlr.TokenStreamException

CLSS public groovyjarjarantlr.TokenStreamRecognitionException
cons public init(groovyjarjarantlr.RecognitionException)
fld public groovyjarjarantlr.RecognitionException recog
meth public java.lang.String toString()
supr groovyjarjarantlr.TokenStreamException

CLSS public groovyjarjarantlr.TokenStreamRetryException
cons public init()
supr groovyjarjarantlr.TokenStreamException

CLSS public groovyjarjarantlr.TokenStreamRewriteEngine
cons public init(groovyjarjarantlr.TokenStream)
cons public init(groovyjarjarantlr.TokenStream,int)
fld protected groovyjarjarantlr.TokenStream stream
fld protected groovyjarjarantlr.collections.impl.BitSet discardMask
fld protected int index
fld protected java.util.List tokens
fld protected java.util.Map lastRewriteTokenIndexes
fld protected java.util.Map programs
fld public final static int MIN_TOKEN_INDEX = 0
fld public final static int PROGRAM_INIT_SIZE = 100
fld public final static java.lang.String DEFAULT_PROGRAM_NAME = "default"
intf groovyjarjarantlr.ASdebug.IASDebugStream
intf groovyjarjarantlr.TokenStream
meth protected int getLastRewriteTokenIndex(java.lang.String)
meth protected java.util.List getProgram(java.lang.String)
meth protected void addToSortedRewriteList(groovyjarjarantlr.TokenStreamRewriteEngine$RewriteOperation)
meth protected void addToSortedRewriteList(java.lang.String,groovyjarjarantlr.TokenStreamRewriteEngine$RewriteOperation)
meth protected void setLastRewriteTokenIndex(java.lang.String,int)
meth public groovyjarjarantlr.ASdebug.TokenOffsetInfo getOffsetInfo(groovyjarjarantlr.Token)
meth public groovyjarjarantlr.Token nextToken() throws groovyjarjarantlr.TokenStreamException
meth public groovyjarjarantlr.TokenWithIndex getToken(int)
meth public int getLastRewriteTokenIndex()
meth public int getTokenStreamSize()
meth public int index()
meth public int size()
meth public java.lang.String getEntireText()
meth public java.lang.String toDebugString()
meth public java.lang.String toDebugString(int,int)
meth public java.lang.String toOriginalString()
meth public java.lang.String toOriginalString(int,int)
meth public java.lang.String toString()
meth public java.lang.String toString(int,int)
meth public java.lang.String toString(java.lang.String)
meth public java.lang.String toString(java.lang.String,int,int)
meth public void delete(groovyjarjarantlr.Token)
meth public void delete(groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public void delete(int)
meth public void delete(int,int)
meth public void delete(java.lang.String,groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public void delete(java.lang.String,int,int)
meth public void deleteProgram()
meth public void deleteProgram(java.lang.String)
meth public void discard(int)
meth public void insertAfter(groovyjarjarantlr.Token,java.lang.String)
meth public void insertAfter(int,java.lang.String)
meth public void insertAfter(java.lang.String,groovyjarjarantlr.Token,java.lang.String)
meth public void insertAfter(java.lang.String,int,java.lang.String)
meth public void insertBefore(groovyjarjarantlr.Token,java.lang.String)
meth public void insertBefore(int,java.lang.String)
meth public void insertBefore(java.lang.String,groovyjarjarantlr.Token,java.lang.String)
meth public void insertBefore(java.lang.String,int,java.lang.String)
meth public void replace(groovyjarjarantlr.Token,groovyjarjarantlr.Token,java.lang.String)
meth public void replace(groovyjarjarantlr.Token,java.lang.String)
meth public void replace(int,int,java.lang.String)
meth public void replace(int,java.lang.String)
meth public void replace(java.lang.String,groovyjarjarantlr.Token,groovyjarjarantlr.Token,java.lang.String)
meth public void replace(java.lang.String,int,int,java.lang.String)
meth public void rollback(int)
meth public void rollback(java.lang.String,int)
supr java.lang.Object
hcls DeleteOp,InsertBeforeOp,ReplaceOp,RewriteOperation

CLSS public groovyjarjarantlr.TokenStreamSelector
cons public init()
fld protected groovyjarjarantlr.TokenStream input
fld protected groovyjarjarantlr.collections.Stack streamStack
fld protected java.util.Hashtable inputStreamNames
intf groovyjarjarantlr.ASdebug.IASDebugStream
intf groovyjarjarantlr.TokenStream
meth public groovyjarjarantlr.ASdebug.TokenOffsetInfo getOffsetInfo(groovyjarjarantlr.Token)
meth public groovyjarjarantlr.Token nextToken() throws groovyjarjarantlr.TokenStreamException
meth public groovyjarjarantlr.TokenStream getCurrentStream()
meth public groovyjarjarantlr.TokenStream getStream(java.lang.String)
meth public groovyjarjarantlr.TokenStream pop()
meth public java.lang.String getEntireText()
meth public void addInputStream(groovyjarjarantlr.TokenStream,java.lang.String)
meth public void push(groovyjarjarantlr.TokenStream)
meth public void push(java.lang.String)
meth public void retry() throws groovyjarjarantlr.TokenStreamRetryException
meth public void select(groovyjarjarantlr.TokenStream)
meth public void select(java.lang.String)
supr java.lang.Object

CLSS public groovyjarjarantlr.TokenWithIndex
cons public init()
cons public init(int,java.lang.String)
meth public int getIndex()
meth public java.lang.String toString()
meth public void setIndex(int)
supr groovyjarjarantlr.CommonToken
hfds index

CLSS public groovyjarjarantlr.Tool
cons public init()
fld protected boolean genHashLines
fld protected boolean hasError
fld protected boolean noConstructors
fld protected boolean upperCaseMangledLiterals
fld protected groovyjarjarantlr.NameSpace nameSpace
fld protected java.lang.String grammarFile
fld protected java.lang.String literalsPrefix
fld protected java.lang.String namespaceAntlr
fld protected java.lang.String namespaceStd
fld protected java.lang.String outputDir
fld public static java.lang.String version
meth protected void checkForInvalidArguments(java.lang.String[],groovyjarjarantlr.collections.impl.BitSet)
meth protected void processArguments(java.lang.String[])
meth public boolean getGenHashLines()
meth public boolean getUpperCaseMangledLiterals()
meth public boolean hasError()
meth public groovyjarjarantlr.NameSpace getNameSpace()
meth public int doEverything(java.lang.String[])
meth public java.io.File parent(java.io.File)
meth public java.io.PrintWriter openOutputFile(java.lang.String) throws java.io.IOException
meth public java.io.Reader getGrammarReader()
meth public java.lang.String fileMinusPath(java.lang.String)
meth public java.lang.String getGrammarFile()
meth public java.lang.String getLanguage(groovyjarjarantlr.MakeGrammar)
meth public java.lang.String getLiteralsPrefix()
meth public java.lang.String getNamespaceAntlr()
meth public java.lang.String getNamespaceStd()
meth public java.lang.String getOutputDirectory()
meth public java.lang.String pathToFile(java.lang.String)
meth public static groovyjarjarantlr.collections.impl.Vector parseSeparatedList(java.lang.String,char)
meth public static void main(java.lang.String[])
meth public void copyFile(java.lang.String,java.lang.String) throws java.io.IOException
meth public void doEverythingWrapper(java.lang.String[])
meth public void error(java.lang.String)
meth public void error(java.lang.String,java.lang.String,int,int)
meth public void fatalError(java.lang.String)
meth public void panic()
meth public void panic(java.lang.String)
meth public void reportException(java.lang.Exception,java.lang.String)
meth public void reportProgress(java.lang.String)
meth public void setArgOK(int)
meth public void setFileLineFormatter(groovyjarjarantlr.FileLineFormatter)
meth public void setNameSpace(java.lang.String)
meth public void setOutputDirectory(java.lang.String)
meth public void toolError(java.lang.String)
meth public void warning(java.lang.String)
meth public void warning(java.lang.String,java.lang.String,int,int)
meth public void warning(java.lang.String[],java.lang.String,int,int)
supr java.lang.Object
hfds cmdLineArgValid,errorHandler,f,genDiagnostics,genDocBook,genHTML

CLSS public groovyjarjarantlr.TreeParser
cons public init()
fld protected groovyjarjarantlr.ASTFactory astFactory
fld protected groovyjarjarantlr.TreeParserSharedInputState inputState
fld protected groovyjarjarantlr.collections.AST _retTree
fld protected groovyjarjarantlr.collections.AST returnAST
fld protected int traceDepth
fld protected java.lang.String[] tokenNames
fld public static groovyjarjarantlr.ASTNULLType ASTNULL
meth protected void match(groovyjarjarantlr.collections.AST,int) throws groovyjarjarantlr.MismatchedTokenException
meth protected void matchNot(groovyjarjarantlr.collections.AST,int) throws groovyjarjarantlr.MismatchedTokenException
meth public groovyjarjarantlr.ASTFactory getASTFactory()
meth public groovyjarjarantlr.collections.AST getAST()
meth public java.lang.String getTokenName(int)
meth public java.lang.String[] getTokenNames()
meth public static void panic()
meth public void match(groovyjarjarantlr.collections.AST,groovyjarjarantlr.collections.impl.BitSet) throws groovyjarjarantlr.MismatchedTokenException
meth public void reportError(groovyjarjarantlr.RecognitionException)
meth public void reportError(java.lang.String)
meth public void reportWarning(java.lang.String)
meth public void setASTFactory(groovyjarjarantlr.ASTFactory)
meth public void setASTNodeClass(java.lang.String)
meth public void setASTNodeType(java.lang.String)
meth public void traceIn(java.lang.String,groovyjarjarantlr.collections.AST)
meth public void traceIndent()
meth public void traceOut(java.lang.String,groovyjarjarantlr.collections.AST)
supr java.lang.Object

CLSS public groovyjarjarantlr.TreeParserSharedInputState
cons public init()
fld public int guessing
supr java.lang.Object

CLSS public groovyjarjarantlr.Utils
cons public init()
meth public static java.lang.Class loadClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public static java.lang.Object createInstanceOf(java.lang.String) throws java.lang.ClassNotFoundException,java.lang.IllegalAccessException,java.lang.InstantiationException
meth public static void error(java.lang.String)
meth public static void error(java.lang.String,java.lang.Throwable)
supr java.lang.Object
hfds useDirectClassLoading,useSystemExit

CLSS public groovyjarjarantlr.Version
cons public init()
fld public final static java.lang.String datestamp = "20060906"
fld public final static java.lang.String patchlevel = "7"
fld public final static java.lang.String project_version = "2.7.7 (20060906)"
fld public final static java.lang.String subversion = "7"
fld public final static java.lang.String version = "2"
supr java.lang.Object

CLSS public abstract interface groovyjarjarantlr.collections.AST
meth public abstract boolean equals(groovyjarjarantlr.collections.AST)
meth public abstract boolean equalsList(groovyjarjarantlr.collections.AST)
meth public abstract boolean equalsListPartial(groovyjarjarantlr.collections.AST)
meth public abstract boolean equalsTree(groovyjarjarantlr.collections.AST)
meth public abstract boolean equalsTreePartial(groovyjarjarantlr.collections.AST)
meth public abstract groovyjarjarantlr.collections.AST getFirstChild()
meth public abstract groovyjarjarantlr.collections.AST getNextSibling()
meth public abstract groovyjarjarantlr.collections.ASTEnumeration findAll(groovyjarjarantlr.collections.AST)
meth public abstract groovyjarjarantlr.collections.ASTEnumeration findAllPartial(groovyjarjarantlr.collections.AST)
meth public abstract int getColumn()
meth public abstract int getLine()
meth public abstract int getNumberOfChildren()
meth public abstract int getType()
meth public abstract java.lang.String getText()
meth public abstract java.lang.String toString()
meth public abstract java.lang.String toStringList()
meth public abstract java.lang.String toStringTree()
meth public abstract void addChild(groovyjarjarantlr.collections.AST)
meth public abstract void initialize(groovyjarjarantlr.Token)
meth public abstract void initialize(groovyjarjarantlr.collections.AST)
meth public abstract void initialize(int,java.lang.String)
meth public abstract void setFirstChild(groovyjarjarantlr.collections.AST)
meth public abstract void setNextSibling(groovyjarjarantlr.collections.AST)
meth public abstract void setText(java.lang.String)
meth public abstract void setType(int)

CLSS public abstract groovyjarjarasm.asm.AnnotationVisitor
cons public init(int)
cons public init(int,groovyjarjarasm.asm.AnnotationVisitor)
fld protected final int api
fld protected groovyjarjarasm.asm.AnnotationVisitor av
meth public groovyjarjarasm.asm.AnnotationVisitor visitAnnotation(java.lang.String,java.lang.String)
meth public groovyjarjarasm.asm.AnnotationVisitor visitArray(java.lang.String)
meth public void visit(java.lang.String,java.lang.Object)
meth public void visitEnd()
meth public void visitEnum(java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public groovyjarjarasm.asm.Attribute
cons protected init(java.lang.String)
fld public final java.lang.String type
meth protected groovyjarjarasm.asm.Attribute read(groovyjarjarasm.asm.ClassReader,int,int,char[],int,groovyjarjarasm.asm.Label[])
meth protected groovyjarjarasm.asm.ByteVector write(groovyjarjarasm.asm.ClassWriter,byte[],int,int,int)
meth protected groovyjarjarasm.asm.Label[] getLabels()
meth public boolean isCodeAttribute()
meth public boolean isUnknown()
supr java.lang.Object
hfds content,nextAttribute
hcls Set

CLSS public groovyjarjarasm.asm.ByteVector
cons public init()
cons public init(int)
meth public groovyjarjarasm.asm.ByteVector putByte(int)
meth public groovyjarjarasm.asm.ByteVector putByteArray(byte[],int,int)
meth public groovyjarjarasm.asm.ByteVector putInt(int)
meth public groovyjarjarasm.asm.ByteVector putLong(long)
meth public groovyjarjarasm.asm.ByteVector putShort(int)
meth public groovyjarjarasm.asm.ByteVector putUTF8(java.lang.String)
supr java.lang.Object
hfds data,length

CLSS public groovyjarjarasm.asm.ClassReader
cons public init(byte[])
cons public init(byte[],int,int)
cons public init(java.io.InputStream) throws java.io.IOException
cons public init(java.lang.String) throws java.io.IOException
fld public final byte[] b
fld public final int header
fld public final static int EXPAND_FRAMES = 8
fld public final static int SKIP_CODE = 1
fld public final static int SKIP_DEBUG = 2
fld public final static int SKIP_FRAMES = 4
meth protected groovyjarjarasm.asm.Label readLabel(int,groovyjarjarasm.asm.Label[])
meth public int getAccess()
meth public int getItem(int)
meth public int getItemCount()
meth public int getMaxStringLength()
meth public int readByte(int)
meth public int readInt(int)
meth public int readUnsignedShort(int)
meth public java.lang.Object readConst(int,char[])
meth public java.lang.String getClassName()
meth public java.lang.String getSuperName()
meth public java.lang.String readClass(int,char[])
meth public java.lang.String readModule(int,char[])
meth public java.lang.String readPackage(int,char[])
meth public java.lang.String readUTF8(int,char[])
meth public java.lang.String[] getInterfaces()
meth public long readLong(int)
meth public short readShort(int)
meth public void accept(groovyjarjarasm.asm.ClassVisitor,groovyjarjarasm.asm.Attribute[],int)
meth public void accept(groovyjarjarasm.asm.ClassVisitor,int)
supr java.lang.Object
hfds EXPAND_ASM_INSNS,INPUT_STREAM_DATA_CHUNK_SIZE,bootstrapMethodOffsets,cpInfoOffsets,cpInfoValues,maxStringLength

CLSS public abstract groovyjarjarasm.asm.ClassVisitor
cons public init(int)
cons public init(int,groovyjarjarasm.asm.ClassVisitor)
fld protected final int api
fld protected groovyjarjarasm.asm.ClassVisitor cv
meth public groovyjarjarasm.asm.AnnotationVisitor visitAnnotation(java.lang.String,boolean)
meth public groovyjarjarasm.asm.AnnotationVisitor visitTypeAnnotation(int,groovyjarjarasm.asm.TypePath,java.lang.String,boolean)
meth public groovyjarjarasm.asm.FieldVisitor visitField(int,java.lang.String,java.lang.String,java.lang.String,java.lang.Object)
meth public groovyjarjarasm.asm.MethodVisitor visitMethod(int,java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public groovyjarjarasm.asm.ModuleVisitor visitModule(java.lang.String,int,java.lang.String)
meth public void visit(int,int,java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public void visitAttribute(groovyjarjarasm.asm.Attribute)
meth public void visitEnd()
meth public void visitInnerClass(java.lang.String,java.lang.String,java.lang.String,int)
meth public void visitNestHostExperimental(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void visitNestMemberExperimental(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void visitOuterClass(java.lang.String,java.lang.String,java.lang.String)
meth public void visitSource(java.lang.String,java.lang.String)
supr java.lang.Object

CLSS public groovyjarjarasm.asm.ClassWriter
cons public init(groovyjarjarasm.asm.ClassReader,int)
cons public init(int)
fld public final static int COMPUTE_FRAMES = 2
fld public final static int COMPUTE_MAXS = 1
meth protected java.lang.String getCommonSuperClass(java.lang.String,java.lang.String)
meth public !varargs int newConstantDynamic(java.lang.String,java.lang.String,groovyjarjarasm.asm.Handle,java.lang.Object[])
meth public !varargs int newInvokeDynamic(java.lang.String,java.lang.String,groovyjarjarasm.asm.Handle,java.lang.Object[])
meth public byte[] toByteArray()
meth public final groovyjarjarasm.asm.AnnotationVisitor visitAnnotation(java.lang.String,boolean)
meth public final groovyjarjarasm.asm.AnnotationVisitor visitTypeAnnotation(int,groovyjarjarasm.asm.TypePath,java.lang.String,boolean)
meth public final groovyjarjarasm.asm.FieldVisitor visitField(int,java.lang.String,java.lang.String,java.lang.String,java.lang.Object)
meth public final groovyjarjarasm.asm.MethodVisitor visitMethod(int,java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public final groovyjarjarasm.asm.ModuleVisitor visitModule(java.lang.String,int,java.lang.String)
meth public final void visit(int,int,java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public final void visitAttribute(groovyjarjarasm.asm.Attribute)
meth public final void visitEnd()
meth public final void visitInnerClass(java.lang.String,java.lang.String,java.lang.String,int)
meth public final void visitOuterClass(java.lang.String,java.lang.String,java.lang.String)
meth public final void visitSource(java.lang.String,java.lang.String)
meth public int newClass(java.lang.String)
meth public int newConst(java.lang.Object)
meth public int newField(java.lang.String,java.lang.String,java.lang.String)
meth public int newHandle(int,java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public int newHandle(int,java.lang.String,java.lang.String,java.lang.String,boolean)
meth public int newMethod(java.lang.String,java.lang.String,java.lang.String,boolean)
meth public int newMethodType(java.lang.String)
meth public int newModule(java.lang.String)
meth public int newNameType(java.lang.String,java.lang.String)
meth public int newPackage(java.lang.String)
meth public int newUTF8(java.lang.String)
meth public void visitNestHostExperimental(java.lang.String)
meth public void visitNestMemberExperimental(java.lang.String)
supr groovyjarjarasm.asm.ClassVisitor
hfds accessFlags,compute,debugExtension,enclosingClassIndex,enclosingMethodIndex,firstAttribute,firstField,firstMethod,innerClasses,interfaceCount,interfaces,lastField,lastMethod,lastRuntimeInvisibleAnnotation,lastRuntimeInvisibleTypeAnnotation,lastRuntimeVisibleAnnotation,lastRuntimeVisibleTypeAnnotation,moduleWriter,nestHostClassIndex,nestMemberClasses,numberOfInnerClasses,numberOfNestMemberClasses,signatureIndex,sourceFileIndex,superClass,symbolTable,thisClass,version

CLSS public final groovyjarjarasm.asm.ConstantDynamic
 anno 0 java.lang.Deprecated()
cons public !varargs init(java.lang.String,java.lang.String,groovyjarjarasm.asm.Handle,java.lang.Object[])
meth public boolean equals(java.lang.Object)
meth public groovyjarjarasm.asm.Handle getBootstrapMethod()
meth public int hashCode()
meth public java.lang.Object[] getBootstrapMethodArguments()
meth public java.lang.String getDescriptor()
meth public java.lang.String getName()
meth public java.lang.String toString()
supr java.lang.Object
hfds bootstrapMethod,bootstrapMethodArguments,descriptor,name

CLSS public abstract groovyjarjarasm.asm.FieldVisitor
cons public init(int)
cons public init(int,groovyjarjarasm.asm.FieldVisitor)
fld protected final int api
fld protected groovyjarjarasm.asm.FieldVisitor fv
meth public groovyjarjarasm.asm.AnnotationVisitor visitAnnotation(java.lang.String,boolean)
meth public groovyjarjarasm.asm.AnnotationVisitor visitTypeAnnotation(int,groovyjarjarasm.asm.TypePath,java.lang.String,boolean)
meth public void visitAttribute(groovyjarjarasm.asm.Attribute)
meth public void visitEnd()
supr java.lang.Object

CLSS public final groovyjarjarasm.asm.Handle
cons public init(int,java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
cons public init(int,java.lang.String,java.lang.String,java.lang.String,boolean)
meth public boolean equals(java.lang.Object)
meth public boolean isInterface()
meth public int getTag()
meth public int hashCode()
meth public java.lang.String getDesc()
meth public java.lang.String getName()
meth public java.lang.String getOwner()
meth public java.lang.String toString()
supr java.lang.Object
hfds descriptor,isInterface,name,owner,tag

CLSS public groovyjarjarasm.asm.Label
cons public init()
fld public java.lang.Object info
meth public int getOffset()
meth public java.lang.String toString()
supr java.lang.Object
hfds EMPTY_LIST,FLAG_DEBUG_ONLY,FLAG_JUMP_TARGET,FLAG_REACHABLE,FLAG_RESOLVED,FLAG_SUBROUTINE_CALLER,FLAG_SUBROUTINE_END,FLAG_SUBROUTINE_START,FORWARD_REFERENCES_CAPACITY_INCREMENT,FORWARD_REFERENCE_HANDLE_MASK,FORWARD_REFERENCE_TYPE_MASK,FORWARD_REFERENCE_TYPE_SHORT,FORWARD_REFERENCE_TYPE_WIDE,LINE_NUMBERS_CAPACITY_INCREMENT,bytecodeOffset,flags,forwardReferences,frame,inputStackSize,lineNumber,nextBasicBlock,nextListElement,otherLineNumbers,outgoingEdges,outputStackMax,outputStackSize,subroutineId

CLSS public abstract groovyjarjarasm.asm.MethodVisitor
cons public init(int)
cons public init(int,groovyjarjarasm.asm.MethodVisitor)
fld protected final int api
fld protected groovyjarjarasm.asm.MethodVisitor mv
meth public !varargs void visitInvokeDynamicInsn(java.lang.String,java.lang.String,groovyjarjarasm.asm.Handle,java.lang.Object[])
meth public !varargs void visitTableSwitchInsn(int,int,groovyjarjarasm.asm.Label,groovyjarjarasm.asm.Label[])
meth public groovyjarjarasm.asm.AnnotationVisitor visitAnnotation(java.lang.String,boolean)
meth public groovyjarjarasm.asm.AnnotationVisitor visitAnnotationDefault()
meth public groovyjarjarasm.asm.AnnotationVisitor visitInsnAnnotation(int,groovyjarjarasm.asm.TypePath,java.lang.String,boolean)
meth public groovyjarjarasm.asm.AnnotationVisitor visitLocalVariableAnnotation(int,groovyjarjarasm.asm.TypePath,groovyjarjarasm.asm.Label[],groovyjarjarasm.asm.Label[],int[],java.lang.String,boolean)
meth public groovyjarjarasm.asm.AnnotationVisitor visitParameterAnnotation(int,java.lang.String,boolean)
meth public groovyjarjarasm.asm.AnnotationVisitor visitTryCatchAnnotation(int,groovyjarjarasm.asm.TypePath,java.lang.String,boolean)
meth public groovyjarjarasm.asm.AnnotationVisitor visitTypeAnnotation(int,groovyjarjarasm.asm.TypePath,java.lang.String,boolean)
meth public void visitAnnotableParameterCount(int,boolean)
meth public void visitAttribute(groovyjarjarasm.asm.Attribute)
meth public void visitCode()
meth public void visitEnd()
meth public void visitFieldInsn(int,java.lang.String,java.lang.String,java.lang.String)
meth public void visitFrame(int,int,java.lang.Object[],int,java.lang.Object[])
meth public void visitIincInsn(int,int)
meth public void visitInsn(int)
meth public void visitIntInsn(int,int)
meth public void visitJumpInsn(int,groovyjarjarasm.asm.Label)
meth public void visitLabel(groovyjarjarasm.asm.Label)
meth public void visitLdcInsn(java.lang.Object)
meth public void visitLineNumber(int,groovyjarjarasm.asm.Label)
meth public void visitLocalVariable(java.lang.String,java.lang.String,java.lang.String,groovyjarjarasm.asm.Label,groovyjarjarasm.asm.Label,int)
meth public void visitLookupSwitchInsn(groovyjarjarasm.asm.Label,int[],groovyjarjarasm.asm.Label[])
meth public void visitMaxs(int,int)
meth public void visitMethodInsn(int,java.lang.String,java.lang.String,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void visitMethodInsn(int,java.lang.String,java.lang.String,java.lang.String,boolean)
meth public void visitMultiANewArrayInsn(java.lang.String,int)
meth public void visitParameter(java.lang.String,int)
meth public void visitTryCatchBlock(groovyjarjarasm.asm.Label,groovyjarjarasm.asm.Label,groovyjarjarasm.asm.Label,java.lang.String)
meth public void visitTypeInsn(int,java.lang.String)
meth public void visitVarInsn(int,int)
supr java.lang.Object
hfds REQUIRES_ASM5

CLSS public abstract groovyjarjarasm.asm.ModuleVisitor
cons public init(int)
cons public init(int,groovyjarjarasm.asm.ModuleVisitor)
fld protected final int api
fld protected groovyjarjarasm.asm.ModuleVisitor mv
meth public !varargs void visitExport(java.lang.String,int,java.lang.String[])
meth public !varargs void visitOpen(java.lang.String,int,java.lang.String[])
meth public !varargs void visitProvide(java.lang.String,java.lang.String[])
meth public void visitEnd()
meth public void visitMainClass(java.lang.String)
meth public void visitPackage(java.lang.String)
meth public void visitRequire(java.lang.String,int,java.lang.String)
meth public void visitUse(java.lang.String)
supr java.lang.Object

CLSS public abstract interface groovyjarjarasm.asm.Opcodes
fld public final static int AALOAD = 50
fld public final static int AASTORE = 83
fld public final static int ACC_ABSTRACT = 1024
fld public final static int ACC_ANNOTATION = 8192
fld public final static int ACC_BRIDGE = 64
fld public final static int ACC_DEPRECATED = 131072
fld public final static int ACC_ENUM = 16384
fld public final static int ACC_FINAL = 16
fld public final static int ACC_INTERFACE = 512
fld public final static int ACC_MANDATED = 32768
fld public final static int ACC_MODULE = 32768
fld public final static int ACC_NATIVE = 256
fld public final static int ACC_OPEN = 32
fld public final static int ACC_PRIVATE = 2
fld public final static int ACC_PROTECTED = 4
fld public final static int ACC_PUBLIC = 1
fld public final static int ACC_STATIC = 8
fld public final static int ACC_STATIC_PHASE = 64
fld public final static int ACC_STRICT = 2048
fld public final static int ACC_SUPER = 32
fld public final static int ACC_SYNCHRONIZED = 32
fld public final static int ACC_SYNTHETIC = 4096
fld public final static int ACC_TRANSIENT = 128
fld public final static int ACC_TRANSITIVE = 32
fld public final static int ACC_VARARGS = 128
fld public final static int ACC_VOLATILE = 64
fld public final static int ACONST_NULL = 1
fld public final static int ALOAD = 25
fld public final static int ANEWARRAY = 189
fld public final static int ARETURN = 176
fld public final static int ARRAYLENGTH = 190
fld public final static int ASM4 = 262144
fld public final static int ASM5 = 327680
fld public final static int ASM6 = 393216
fld public final static int ASM7_EXPERIMENTAL = 17235968
 anno 0 java.lang.Deprecated()
fld public final static int ASTORE = 58
fld public final static int ATHROW = 191
fld public final static int BALOAD = 51
fld public final static int BASTORE = 84
fld public final static int BIPUSH = 16
fld public final static int CALOAD = 52
fld public final static int CASTORE = 85
fld public final static int CHECKCAST = 192
fld public final static int D2F = 144
fld public final static int D2I = 142
fld public final static int D2L = 143
fld public final static int DADD = 99
fld public final static int DALOAD = 49
fld public final static int DASTORE = 82
fld public final static int DCMPG = 152
fld public final static int DCMPL = 151
fld public final static int DCONST_0 = 14
fld public final static int DCONST_1 = 15
fld public final static int DDIV = 111
fld public final static int DLOAD = 24
fld public final static int DMUL = 107
fld public final static int DNEG = 119
fld public final static int DREM = 115
fld public final static int DRETURN = 175
fld public final static int DSTORE = 57
fld public final static int DSUB = 103
fld public final static int DUP = 89
fld public final static int DUP2 = 92
fld public final static int DUP2_X1 = 93
fld public final static int DUP2_X2 = 94
fld public final static int DUP_X1 = 90
fld public final static int DUP_X2 = 91
fld public final static int F2D = 141
fld public final static int F2I = 139
fld public final static int F2L = 140
fld public final static int FADD = 98
fld public final static int FALOAD = 48
fld public final static int FASTORE = 81
fld public final static int FCMPG = 150
fld public final static int FCMPL = 149
fld public final static int FCONST_0 = 11
fld public final static int FCONST_1 = 12
fld public final static int FCONST_2 = 13
fld public final static int FDIV = 110
fld public final static int FLOAD = 23
fld public final static int FMUL = 106
fld public final static int FNEG = 118
fld public final static int FREM = 114
fld public final static int FRETURN = 174
fld public final static int FSTORE = 56
fld public final static int FSUB = 102
fld public final static int F_APPEND = 1
fld public final static int F_CHOP = 2
fld public final static int F_FULL = 0
fld public final static int F_NEW = -1
fld public final static int F_SAME = 3
fld public final static int F_SAME1 = 4
fld public final static int GETFIELD = 180
fld public final static int GETSTATIC = 178
fld public final static int GOTO = 167
fld public final static int H_GETFIELD = 1
fld public final static int H_GETSTATIC = 2
fld public final static int H_INVOKEINTERFACE = 9
fld public final static int H_INVOKESPECIAL = 7
fld public final static int H_INVOKESTATIC = 6
fld public final static int H_INVOKEVIRTUAL = 5
fld public final static int H_NEWINVOKESPECIAL = 8
fld public final static int H_PUTFIELD = 3
fld public final static int H_PUTSTATIC = 4
fld public final static int I2B = 145
fld public final static int I2C = 146
fld public final static int I2D = 135
fld public final static int I2F = 134
fld public final static int I2L = 133
fld public final static int I2S = 147
fld public final static int IADD = 96
fld public final static int IALOAD = 46
fld public final static int IAND = 126
fld public final static int IASTORE = 79
fld public final static int ICONST_0 = 3
fld public final static int ICONST_1 = 4
fld public final static int ICONST_2 = 5
fld public final static int ICONST_3 = 6
fld public final static int ICONST_4 = 7
fld public final static int ICONST_5 = 8
fld public final static int ICONST_M1 = 2
fld public final static int IDIV = 108
fld public final static int IFEQ = 153
fld public final static int IFGE = 156
fld public final static int IFGT = 157
fld public final static int IFLE = 158
fld public final static int IFLT = 155
fld public final static int IFNE = 154
fld public final static int IFNONNULL = 199
fld public final static int IFNULL = 198
fld public final static int IF_ACMPEQ = 165
fld public final static int IF_ACMPNE = 166
fld public final static int IF_ICMPEQ = 159
fld public final static int IF_ICMPGE = 162
fld public final static int IF_ICMPGT = 163
fld public final static int IF_ICMPLE = 164
fld public final static int IF_ICMPLT = 161
fld public final static int IF_ICMPNE = 160
fld public final static int IINC = 132
fld public final static int ILOAD = 21
fld public final static int IMUL = 104
fld public final static int INEG = 116
fld public final static int INSTANCEOF = 193
fld public final static int INVOKEDYNAMIC = 186
fld public final static int INVOKEINTERFACE = 185
fld public final static int INVOKESPECIAL = 183
fld public final static int INVOKESTATIC = 184
fld public final static int INVOKEVIRTUAL = 182
fld public final static int IOR = 128
fld public final static int IREM = 112
fld public final static int IRETURN = 172
fld public final static int ISHL = 120
fld public final static int ISHR = 122
fld public final static int ISTORE = 54
fld public final static int ISUB = 100
fld public final static int IUSHR = 124
fld public final static int IXOR = 130
fld public final static int JSR = 168
fld public final static int L2D = 138
fld public final static int L2F = 137
fld public final static int L2I = 136
fld public final static int LADD = 97
fld public final static int LALOAD = 47
fld public final static int LAND = 127
fld public final static int LASTORE = 80
fld public final static int LCMP = 148
fld public final static int LCONST_0 = 9
fld public final static int LCONST_1 = 10
fld public final static int LDC = 18
fld public final static int LDIV = 109
fld public final static int LLOAD = 22
fld public final static int LMUL = 105
fld public final static int LNEG = 117
fld public final static int LOOKUPSWITCH = 171
fld public final static int LOR = 129
fld public final static int LREM = 113
fld public final static int LRETURN = 173
fld public final static int LSHL = 121
fld public final static int LSHR = 123
fld public final static int LSTORE = 55
fld public final static int LSUB = 101
fld public final static int LUSHR = 125
fld public final static int LXOR = 131
fld public final static int MONITORENTER = 194
fld public final static int MONITOREXIT = 195
fld public final static int MULTIANEWARRAY = 197
fld public final static int NEW = 187
fld public final static int NEWARRAY = 188
fld public final static int NOP = 0
fld public final static int POP = 87
fld public final static int POP2 = 88
fld public final static int PUTFIELD = 181
fld public final static int PUTSTATIC = 179
fld public final static int RET = 169
fld public final static int RETURN = 177
fld public final static int SALOAD = 53
fld public final static int SASTORE = 86
fld public final static int SIPUSH = 17
fld public final static int SWAP = 95
fld public final static int TABLESWITCH = 170
fld public final static int T_BOOLEAN = 4
fld public final static int T_BYTE = 8
fld public final static int T_CHAR = 5
fld public final static int T_DOUBLE = 7
fld public final static int T_FLOAT = 6
fld public final static int T_INT = 10
fld public final static int T_LONG = 11
fld public final static int T_SHORT = 9
fld public final static int V10 = 54
fld public final static int V11 = 55
fld public final static int V1_1 = 196653
fld public final static int V1_2 = 46
fld public final static int V1_3 = 47
fld public final static int V1_4 = 48
fld public final static int V1_5 = 49
fld public final static int V1_6 = 50
fld public final static int V1_7 = 51
fld public final static int V1_8 = 52
fld public final static int V9 = 53
fld public final static int V_PREVIEW_EXPERIMENTAL = -65536
 anno 0 java.lang.Deprecated()
fld public final static java.lang.Integer DOUBLE
fld public final static java.lang.Integer FLOAT
fld public final static java.lang.Integer INTEGER
fld public final static java.lang.Integer LONG
fld public final static java.lang.Integer NULL
fld public final static java.lang.Integer TOP
fld public final static java.lang.Integer UNINITIALIZED_THIS

CLSS public groovyjarjarasm.asm.Type
fld public final static groovyjarjarasm.asm.Type BOOLEAN_TYPE
fld public final static groovyjarjarasm.asm.Type BYTE_TYPE
fld public final static groovyjarjarasm.asm.Type CHAR_TYPE
fld public final static groovyjarjarasm.asm.Type DOUBLE_TYPE
fld public final static groovyjarjarasm.asm.Type FLOAT_TYPE
fld public final static groovyjarjarasm.asm.Type INT_TYPE
fld public final static groovyjarjarasm.asm.Type LONG_TYPE
fld public final static groovyjarjarasm.asm.Type SHORT_TYPE
fld public final static groovyjarjarasm.asm.Type VOID_TYPE
fld public final static int ARRAY = 9
fld public final static int BOOLEAN = 1
fld public final static int BYTE = 3
fld public final static int CHAR = 2
fld public final static int DOUBLE = 8
fld public final static int FLOAT = 6
fld public final static int INT = 5
fld public final static int LONG = 7
fld public final static int METHOD = 11
fld public final static int OBJECT = 10
fld public final static int SHORT = 4
fld public final static int VOID = 0
meth public !varargs static groovyjarjarasm.asm.Type getMethodType(groovyjarjarasm.asm.Type,groovyjarjarasm.asm.Type[])
meth public !varargs static java.lang.String getMethodDescriptor(groovyjarjarasm.asm.Type,groovyjarjarasm.asm.Type[])
meth public boolean equals(java.lang.Object)
meth public groovyjarjarasm.asm.Type getElementType()
meth public groovyjarjarasm.asm.Type getReturnType()
meth public groovyjarjarasm.asm.Type[] getArgumentTypes()
meth public int getArgumentsAndReturnSizes()
meth public int getDimensions()
meth public int getOpcode(int)
meth public int getSize()
meth public int getSort()
meth public int hashCode()
meth public java.lang.String getClassName()
meth public java.lang.String getDescriptor()
meth public java.lang.String getInternalName()
meth public java.lang.String toString()
meth public static groovyjarjarasm.asm.Type getMethodType(java.lang.String)
meth public static groovyjarjarasm.asm.Type getObjectType(java.lang.String)
meth public static groovyjarjarasm.asm.Type getReturnType(java.lang.String)
meth public static groovyjarjarasm.asm.Type getReturnType(java.lang.reflect.Method)
meth public static groovyjarjarasm.asm.Type getType(java.lang.Class<?>)
meth public static groovyjarjarasm.asm.Type getType(java.lang.String)
meth public static groovyjarjarasm.asm.Type getType(java.lang.reflect.Constructor<?>)
meth public static groovyjarjarasm.asm.Type getType(java.lang.reflect.Method)
meth public static groovyjarjarasm.asm.Type[] getArgumentTypes(java.lang.String)
meth public static groovyjarjarasm.asm.Type[] getArgumentTypes(java.lang.reflect.Method)
meth public static int getArgumentsAndReturnSizes(java.lang.String)
meth public static java.lang.String getConstructorDescriptor(java.lang.reflect.Constructor<?>)
meth public static java.lang.String getDescriptor(java.lang.Class<?>)
meth public static java.lang.String getInternalName(java.lang.Class<?>)
meth public static java.lang.String getMethodDescriptor(java.lang.reflect.Method)
supr java.lang.Object
hfds INTERNAL,PRIMITIVE_DESCRIPTORS,sort,valueBegin,valueBuffer,valueEnd

CLSS public groovyjarjarasm.asm.TypePath
fld public final static int ARRAY_ELEMENT = 0
fld public final static int INNER_TYPE = 1
fld public final static int TYPE_ARGUMENT = 3
fld public final static int WILDCARD_BOUND = 2
meth public int getLength()
meth public int getStep(int)
meth public int getStepArgument(int)
meth public java.lang.String toString()
meth public static groovyjarjarasm.asm.TypePath fromString(java.lang.String)
supr java.lang.Object
hfds typePathContainer,typePathOffset

CLSS public groovyjarjarasm.asm.TypeReference
cons public init(int)
fld public final static int CAST = 71
fld public final static int CLASS_EXTENDS = 16
fld public final static int CLASS_TYPE_PARAMETER = 0
fld public final static int CLASS_TYPE_PARAMETER_BOUND = 17
fld public final static int CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT = 72
fld public final static int CONSTRUCTOR_REFERENCE = 69
fld public final static int CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT = 74
fld public final static int EXCEPTION_PARAMETER = 66
fld public final static int FIELD = 19
fld public final static int INSTANCEOF = 67
fld public final static int LOCAL_VARIABLE = 64
fld public final static int METHOD_FORMAL_PARAMETER = 22
fld public final static int METHOD_INVOCATION_TYPE_ARGUMENT = 73
fld public final static int METHOD_RECEIVER = 21
fld public final static int METHOD_REFERENCE = 70
fld public final static int METHOD_REFERENCE_TYPE_ARGUMENT = 75
fld public final static int METHOD_RETURN = 20
fld public final static int METHOD_TYPE_PARAMETER = 1
fld public final static int METHOD_TYPE_PARAMETER_BOUND = 18
fld public final static int NEW = 68
fld public final static int RESOURCE_VARIABLE = 65
fld public final static int THROWS = 23
meth public int getExceptionIndex()
meth public int getFormalParameterIndex()
meth public int getSort()
meth public int getSuperTypeIndex()
meth public int getTryCatchBlockIndex()
meth public int getTypeArgumentIndex()
meth public int getTypeParameterBoundIndex()
meth public int getTypeParameterIndex()
meth public int getValue()
meth public static groovyjarjarasm.asm.TypeReference newExceptionReference(int)
meth public static groovyjarjarasm.asm.TypeReference newFormalParameterReference(int)
meth public static groovyjarjarasm.asm.TypeReference newSuperTypeReference(int)
meth public static groovyjarjarasm.asm.TypeReference newTryCatchReference(int)
meth public static groovyjarjarasm.asm.TypeReference newTypeArgumentReference(int,int)
meth public static groovyjarjarasm.asm.TypeReference newTypeParameterBoundReference(int,int,int)
meth public static groovyjarjarasm.asm.TypeReference newTypeParameterReference(int,int)
meth public static groovyjarjarasm.asm.TypeReference newTypeReference(int)
supr java.lang.Object
hfds targetTypeAndInfo

CLSS public abstract interface java.io.Closeable
intf java.lang.AutoCloseable
meth public abstract void close() throws java.io.IOException

CLSS public java.io.FileWriter
cons public init(java.io.File) throws java.io.IOException
cons public init(java.io.File,boolean) throws java.io.IOException
cons public init(java.io.FileDescriptor)
cons public init(java.lang.String) throws java.io.IOException
cons public init(java.lang.String,boolean) throws java.io.IOException
supr java.io.OutputStreamWriter

CLSS public abstract interface java.io.Flushable
meth public abstract void flush() throws java.io.IOException

CLSS public java.io.OutputStreamWriter
cons public init(java.io.OutputStream)
cons public init(java.io.OutputStream,java.lang.String) throws java.io.UnsupportedEncodingException
cons public init(java.io.OutputStream,java.nio.charset.Charset)
cons public init(java.io.OutputStream,java.nio.charset.CharsetEncoder)
meth public java.lang.String getEncoding()
meth public void close() throws java.io.IOException
meth public void flush() throws java.io.IOException
meth public void write(char[],int,int) throws java.io.IOException
meth public void write(int) throws java.io.IOException
meth public void write(java.lang.String,int,int) throws java.io.IOException
supr java.io.Writer
hfds se

CLSS public java.io.PrintWriter
cons public init(java.io.File) throws java.io.FileNotFoundException
cons public init(java.io.File,java.lang.String) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
cons public init(java.io.OutputStream)
cons public init(java.io.OutputStream,boolean)
cons public init(java.io.Writer)
cons public init(java.io.Writer,boolean)
cons public init(java.lang.String) throws java.io.FileNotFoundException
cons public init(java.lang.String,java.lang.String) throws java.io.FileNotFoundException,java.io.UnsupportedEncodingException
fld protected java.io.Writer out
meth protected void clearError()
meth protected void setError()
meth public !varargs java.io.PrintWriter format(java.lang.String,java.lang.Object[])
meth public !varargs java.io.PrintWriter format(java.util.Locale,java.lang.String,java.lang.Object[])
meth public !varargs java.io.PrintWriter printf(java.lang.String,java.lang.Object[])
meth public !varargs java.io.PrintWriter printf(java.util.Locale,java.lang.String,java.lang.Object[])
meth public boolean checkError()
meth public java.io.PrintWriter append(char)
meth public java.io.PrintWriter append(java.lang.CharSequence)
meth public java.io.PrintWriter append(java.lang.CharSequence,int,int)
meth public void close()
meth public void flush()
meth public void print(boolean)
meth public void print(char)
meth public void print(char[])
meth public void print(double)
meth public void print(float)
meth public void print(int)
meth public void print(java.lang.Object)
meth public void print(java.lang.String)
meth public void print(long)
meth public void println()
meth public void println(boolean)
meth public void println(char)
meth public void println(char[])
meth public void println(double)
meth public void println(float)
meth public void println(int)
meth public void println(java.lang.Object)
meth public void println(java.lang.String)
meth public void println(long)
meth public void write(char[])
meth public void write(char[],int,int)
meth public void write(int)
meth public void write(java.lang.String)
meth public void write(java.lang.String,int,int)
supr java.io.Writer
hfds autoFlush,formatter,lineSeparator,psOut,trouble

CLSS public abstract interface java.io.Serializable

CLSS public abstract java.io.Writer
cons protected init()
cons protected init(java.lang.Object)
fld protected java.lang.Object lock
intf java.io.Closeable
intf java.io.Flushable
intf java.lang.Appendable
meth public abstract void close() throws java.io.IOException
meth public abstract void flush() throws java.io.IOException
meth public abstract void write(char[],int,int) throws java.io.IOException
meth public java.io.Writer append(char) throws java.io.IOException
meth public java.io.Writer append(java.lang.CharSequence) throws java.io.IOException
meth public java.io.Writer append(java.lang.CharSequence,int,int) throws java.io.IOException
meth public void write(char[]) throws java.io.IOException
meth public void write(int) throws java.io.IOException
meth public void write(java.lang.String) throws java.io.IOException
meth public void write(java.lang.String,int,int) throws java.io.IOException
supr java.lang.Object
hfds WRITE_BUFFER_SIZE,writeBuffer

CLSS public abstract interface java.lang.Appendable
meth public abstract java.lang.Appendable append(char) throws java.io.IOException
meth public abstract java.lang.Appendable append(java.lang.CharSequence) throws java.io.IOException
meth public abstract java.lang.Appendable append(java.lang.CharSequence,int,int) throws java.io.IOException

CLSS public java.lang.AssertionError
cons public init()
cons public init(boolean)
cons public init(char)
cons public init(double)
cons public init(float)
cons public init(int)
cons public init(java.lang.Object)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(long)
supr java.lang.Error
hfds serialVersionUID

CLSS public abstract interface java.lang.AutoCloseable
meth public abstract void close() throws java.lang.Exception

CLSS public abstract interface java.lang.CharSequence
meth public abstract char charAt(int)
meth public abstract int length()
meth public abstract java.lang.CharSequence subSequence(int,int)
meth public abstract java.lang.String toString()
meth public java.util.stream.IntStream chars()
meth public java.util.stream.IntStream codePoints()

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
hfds assertionLock,classAssertionStatus,classes,defaultAssertionStatus,defaultDomain,domains,loadedLibraryNames,nativeLibraries,nativeLibraryContext,nocerts,package2certs,packageAssertionStatus,packages,parallelLockMap,parent,scl,sclSet,sys_paths,systemNativeLibraries,usr_paths
hcls NativeLibrary,ParallelLoaders

CLSS public abstract interface java.lang.Cloneable

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
hfds name,ordinal

CLSS public java.lang.Error
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

CLSS public java.lang.Exception
cons protected init(java.lang.String,java.lang.Throwable,boolean,boolean)
cons public init()
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
cons public init(java.lang.Throwable)
supr java.lang.Throwable
hfds serialVersionUID

CLSS public abstract interface !annotation java.lang.FunctionalInterface
 anno 0 java.lang.annotation.Documented()
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation

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

CLSS public abstract interface java.lang.Runnable
 anno 0 java.lang.FunctionalInterface()
meth public abstract void run()

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

CLSS public java.net.URLClassLoader
cons public init(java.net.URL[])
cons public init(java.net.URL[],java.lang.ClassLoader)
cons public init(java.net.URL[],java.lang.ClassLoader,java.net.URLStreamHandlerFactory)
intf java.io.Closeable
meth protected java.lang.Class<?> findClass(java.lang.String) throws java.lang.ClassNotFoundException
meth protected java.lang.Package definePackage(java.lang.String,java.util.jar.Manifest,java.net.URL)
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
meth protected void addURL(java.net.URL)
meth public java.io.InputStream getResourceAsStream(java.lang.String)
meth public java.net.URL findResource(java.lang.String)
meth public java.net.URL[] getURLs()
meth public java.util.Enumeration<java.net.URL> findResources(java.lang.String) throws java.io.IOException
meth public static java.net.URLClassLoader newInstance(java.net.URL[])
meth public static java.net.URLClassLoader newInstance(java.net.URL[],java.lang.ClassLoader)
meth public void close() throws java.io.IOException
supr java.security.SecureClassLoader
hfds acc,closeables,ucp

CLSS public java.security.SecureClassLoader
cons protected init()
cons protected init(java.lang.ClassLoader)
meth protected final java.lang.Class<?> defineClass(java.lang.String,byte[],int,int,java.security.CodeSource)
meth protected final java.lang.Class<?> defineClass(java.lang.String,java.nio.ByteBuffer,java.security.CodeSource)
meth protected java.security.PermissionCollection getPermissions(java.security.CodeSource)
supr java.lang.ClassLoader
hfds debug,initialized,pdcache

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
hfds MAX_ARRAY_SIZE

CLSS public abstract java.util.AbstractList<%0 extends java.lang.Object>
cons protected init()
fld protected int modCount
intf java.util.List<{java.util.AbstractList%0}>
meth protected void removeRange(int,int)
meth public abstract {java.util.AbstractList%0} get(int)
meth public boolean add({java.util.AbstractList%0})
meth public boolean addAll(int,java.util.Collection<? extends {java.util.AbstractList%0}>)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public int indexOf(java.lang.Object)
meth public int lastIndexOf(java.lang.Object)
meth public java.util.Iterator<{java.util.AbstractList%0}> iterator()
meth public java.util.List<{java.util.AbstractList%0}> subList(int,int)
meth public java.util.ListIterator<{java.util.AbstractList%0}> listIterator()
meth public java.util.ListIterator<{java.util.AbstractList%0}> listIterator(int)
meth public void add(int,{java.util.AbstractList%0})
meth public void clear()
meth public {java.util.AbstractList%0} remove(int)
meth public {java.util.AbstractList%0} set(int,{java.util.AbstractList%0})
supr java.util.AbstractCollection<{java.util.AbstractList%0}>
hcls Itr,ListItr

CLSS public abstract java.util.AbstractMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons protected init()
innr public static SimpleEntry
innr public static SimpleImmutableEntry
intf java.util.Map<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public abstract java.util.Set<java.util.Map$Entry<{java.util.AbstractMap%0},{java.util.AbstractMap%1}>> entrySet()
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.String toString()
meth public java.util.Collection<{java.util.AbstractMap%1}> values()
meth public java.util.Set<{java.util.AbstractMap%0}> keySet()
meth public void clear()
meth public void putAll(java.util.Map<? extends {java.util.AbstractMap%0},? extends {java.util.AbstractMap%1}>)
meth public {java.util.AbstractMap%1} get(java.lang.Object)
meth public {java.util.AbstractMap%1} put({java.util.AbstractMap%0},{java.util.AbstractMap%1})
meth public {java.util.AbstractMap%1} remove(java.lang.Object)
supr java.lang.Object
hfds keySet,values

CLSS public java.util.ArrayList<%0 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(java.util.Collection<? extends {java.util.ArrayList%0}>)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.List<{java.util.ArrayList%0}>
intf java.util.RandomAccess
meth protected void removeRange(int,int)
meth public <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public boolean add({java.util.ArrayList%0})
meth public boolean addAll(int,java.util.Collection<? extends {java.util.ArrayList%0}>)
meth public boolean addAll(java.util.Collection<? extends {java.util.ArrayList%0}>)
meth public boolean contains(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object)
meth public boolean removeAll(java.util.Collection<?>)
meth public boolean removeIf(java.util.function.Predicate<? super {java.util.ArrayList%0}>)
meth public boolean retainAll(java.util.Collection<?>)
meth public int indexOf(java.lang.Object)
meth public int lastIndexOf(java.lang.Object)
meth public int size()
meth public java.lang.Object clone()
meth public java.lang.Object[] toArray()
meth public java.util.Iterator<{java.util.ArrayList%0}> iterator()
meth public java.util.List<{java.util.ArrayList%0}> subList(int,int)
meth public java.util.ListIterator<{java.util.ArrayList%0}> listIterator()
meth public java.util.ListIterator<{java.util.ArrayList%0}> listIterator(int)
meth public java.util.Spliterator<{java.util.ArrayList%0}> spliterator()
meth public void add(int,{java.util.ArrayList%0})
meth public void clear()
meth public void ensureCapacity(int)
meth public void forEach(java.util.function.Consumer<? super {java.util.ArrayList%0}>)
meth public void replaceAll(java.util.function.UnaryOperator<{java.util.ArrayList%0}>)
meth public void sort(java.util.Comparator<? super {java.util.ArrayList%0}>)
meth public void trimToSize()
meth public {java.util.ArrayList%0} get(int)
meth public {java.util.ArrayList%0} remove(int)
meth public {java.util.ArrayList%0} set(int,{java.util.ArrayList%0})
supr java.util.AbstractList<{java.util.ArrayList%0}>
hfds DEFAULTCAPACITY_EMPTY_ELEMENTDATA,DEFAULT_CAPACITY,EMPTY_ELEMENTDATA,MAX_ARRAY_SIZE,elementData,serialVersionUID,size
hcls ArrayListSpliterator,Itr,ListItr,SubList

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

CLSS public abstract java.util.Dictionary<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
meth public abstract boolean isEmpty()
meth public abstract int size()
meth public abstract java.util.Enumeration<{java.util.Dictionary%0}> keys()
meth public abstract java.util.Enumeration<{java.util.Dictionary%1}> elements()
meth public abstract {java.util.Dictionary%1} get(java.lang.Object)
meth public abstract {java.util.Dictionary%1} put({java.util.Dictionary%0},{java.util.Dictionary%1})
meth public abstract {java.util.Dictionary%1} remove(java.lang.Object)
supr java.lang.Object

CLSS public abstract interface java.util.EventListener

CLSS public java.util.EventObject
cons public init(java.lang.Object)
fld protected java.lang.Object source
intf java.io.Serializable
meth public java.lang.Object getSource()
meth public java.lang.String toString()
supr java.lang.Object
hfds serialVersionUID

CLSS public java.util.HashMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(int,float)
cons public init(java.util.Map<? extends {java.util.HashMap%0},? extends {java.util.HashMap%1}>)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.Map<{java.util.HashMap%0},{java.util.HashMap%1}>
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object,java.lang.Object)
meth public boolean replace({java.util.HashMap%0},{java.util.HashMap%1},{java.util.HashMap%1})
meth public int size()
meth public java.lang.Object clone()
meth public java.util.Collection<{java.util.HashMap%1}> values()
meth public java.util.Set<java.util.Map$Entry<{java.util.HashMap%0},{java.util.HashMap%1}>> entrySet()
meth public java.util.Set<{java.util.HashMap%0}> keySet()
meth public void clear()
meth public void forEach(java.util.function.BiConsumer<? super {java.util.HashMap%0},? super {java.util.HashMap%1}>)
meth public void putAll(java.util.Map<? extends {java.util.HashMap%0},? extends {java.util.HashMap%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.HashMap%0},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} compute({java.util.HashMap%0},java.util.function.BiFunction<? super {java.util.HashMap%0},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} computeIfAbsent({java.util.HashMap%0},java.util.function.Function<? super {java.util.HashMap%0},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} computeIfPresent({java.util.HashMap%0},java.util.function.BiFunction<? super {java.util.HashMap%0},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} get(java.lang.Object)
meth public {java.util.HashMap%1} getOrDefault(java.lang.Object,{java.util.HashMap%1})
meth public {java.util.HashMap%1} merge({java.util.HashMap%0},{java.util.HashMap%1},java.util.function.BiFunction<? super {java.util.HashMap%1},? super {java.util.HashMap%1},? extends {java.util.HashMap%1}>)
meth public {java.util.HashMap%1} put({java.util.HashMap%0},{java.util.HashMap%1})
meth public {java.util.HashMap%1} putIfAbsent({java.util.HashMap%0},{java.util.HashMap%1})
meth public {java.util.HashMap%1} remove(java.lang.Object)
meth public {java.util.HashMap%1} replace({java.util.HashMap%0},{java.util.HashMap%1})
supr java.util.AbstractMap<{java.util.HashMap%0},{java.util.HashMap%1}>
hfds DEFAULT_INITIAL_CAPACITY,DEFAULT_LOAD_FACTOR,MAXIMUM_CAPACITY,MIN_TREEIFY_CAPACITY,TREEIFY_THRESHOLD,UNTREEIFY_THRESHOLD,entrySet,loadFactor,modCount,serialVersionUID,size,table,threshold
hcls EntryIterator,EntrySet,EntrySpliterator,HashIterator,HashMapSpliterator,KeyIterator,KeySet,KeySpliterator,Node,TreeNode,ValueIterator,ValueSpliterator,Values

CLSS public java.util.Hashtable<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init()
cons public init(int)
cons public init(int,float)
cons public init(java.util.Map<? extends {java.util.Hashtable%0},? extends {java.util.Hashtable%1}>)
intf java.io.Serializable
intf java.lang.Cloneable
intf java.util.Map<{java.util.Hashtable%0},{java.util.Hashtable%1}>
meth protected void rehash()
meth public boolean contains(java.lang.Object)
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public boolean remove(java.lang.Object,java.lang.Object)
meth public boolean replace({java.util.Hashtable%0},{java.util.Hashtable%1},{java.util.Hashtable%1})
meth public int hashCode()
meth public int size()
meth public java.lang.Object clone()
meth public java.lang.String toString()
meth public java.util.Collection<{java.util.Hashtable%1}> values()
meth public java.util.Enumeration<{java.util.Hashtable%0}> keys()
meth public java.util.Enumeration<{java.util.Hashtable%1}> elements()
meth public java.util.Set<java.util.Map$Entry<{java.util.Hashtable%0},{java.util.Hashtable%1}>> entrySet()
meth public java.util.Set<{java.util.Hashtable%0}> keySet()
meth public void clear()
meth public void forEach(java.util.function.BiConsumer<? super {java.util.Hashtable%0},? super {java.util.Hashtable%1}>)
meth public void putAll(java.util.Map<? extends {java.util.Hashtable%0},? extends {java.util.Hashtable%1}>)
meth public void replaceAll(java.util.function.BiFunction<? super {java.util.Hashtable%0},? super {java.util.Hashtable%1},? extends {java.util.Hashtable%1}>)
meth public {java.util.Hashtable%1} compute({java.util.Hashtable%0},java.util.function.BiFunction<? super {java.util.Hashtable%0},? super {java.util.Hashtable%1},? extends {java.util.Hashtable%1}>)
meth public {java.util.Hashtable%1} computeIfAbsent({java.util.Hashtable%0},java.util.function.Function<? super {java.util.Hashtable%0},? extends {java.util.Hashtable%1}>)
meth public {java.util.Hashtable%1} computeIfPresent({java.util.Hashtable%0},java.util.function.BiFunction<? super {java.util.Hashtable%0},? super {java.util.Hashtable%1},? extends {java.util.Hashtable%1}>)
meth public {java.util.Hashtable%1} get(java.lang.Object)
meth public {java.util.Hashtable%1} getOrDefault(java.lang.Object,{java.util.Hashtable%1})
meth public {java.util.Hashtable%1} merge({java.util.Hashtable%0},{java.util.Hashtable%1},java.util.function.BiFunction<? super {java.util.Hashtable%1},? super {java.util.Hashtable%1},? extends {java.util.Hashtable%1}>)
meth public {java.util.Hashtable%1} put({java.util.Hashtable%0},{java.util.Hashtable%1})
meth public {java.util.Hashtable%1} putIfAbsent({java.util.Hashtable%0},{java.util.Hashtable%1})
meth public {java.util.Hashtable%1} remove(java.lang.Object)
meth public {java.util.Hashtable%1} replace({java.util.Hashtable%0},{java.util.Hashtable%1})
supr java.util.Dictionary<{java.util.Hashtable%0},{java.util.Hashtable%1}>
hfds ENTRIES,KEYS,MAX_ARRAY_SIZE,VALUES,count,entrySet,keySet,loadFactor,modCount,serialVersionUID,table,threshold,values
hcls Entry,EntrySet,Enumerator,KeySet,ValueCollection

CLSS public abstract interface java.util.Iterator<%0 extends java.lang.Object>
meth public abstract boolean hasNext()
meth public abstract {java.util.Iterator%0} next()
meth public void forEachRemaining(java.util.function.Consumer<? super {java.util.Iterator%0}>)
meth public void remove()

CLSS public abstract interface java.util.List<%0 extends java.lang.Object>
intf java.util.Collection<{java.util.List%0}>
meth public abstract <%0 extends java.lang.Object> {%%0}[] toArray({%%0}[])
meth public abstract boolean add({java.util.List%0})
meth public abstract boolean addAll(int,java.util.Collection<? extends {java.util.List%0}>)
meth public abstract boolean addAll(java.util.Collection<? extends {java.util.List%0}>)
meth public abstract boolean contains(java.lang.Object)
meth public abstract boolean containsAll(java.util.Collection<?>)
meth public abstract boolean equals(java.lang.Object)
meth public abstract boolean isEmpty()
meth public abstract boolean remove(java.lang.Object)
meth public abstract boolean removeAll(java.util.Collection<?>)
meth public abstract boolean retainAll(java.util.Collection<?>)
meth public abstract int hashCode()
meth public abstract int indexOf(java.lang.Object)
meth public abstract int lastIndexOf(java.lang.Object)
meth public abstract int size()
meth public abstract java.lang.Object[] toArray()
meth public abstract java.util.Iterator<{java.util.List%0}> iterator()
meth public abstract java.util.List<{java.util.List%0}> subList(int,int)
meth public abstract java.util.ListIterator<{java.util.List%0}> listIterator()
meth public abstract java.util.ListIterator<{java.util.List%0}> listIterator(int)
meth public abstract void add(int,{java.util.List%0})
meth public abstract void clear()
meth public abstract {java.util.List%0} get(int)
meth public abstract {java.util.List%0} remove(int)
meth public abstract {java.util.List%0} set(int,{java.util.List%0})
meth public java.util.Spliterator<{java.util.List%0}> spliterator()
meth public void replaceAll(java.util.function.UnaryOperator<{java.util.List%0}>)
meth public void sort(java.util.Comparator<? super {java.util.List%0}>)

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

CLSS public abstract interface java.util.RandomAccess

CLSS public abstract interface java.util.concurrent.Callable<%0 extends java.lang.Object>
 anno 0 java.lang.FunctionalInterface()
meth public abstract {java.util.concurrent.Callable%0} call() throws java.lang.Exception

CLSS public org.codehaus.groovy.GroovyBugError
cons public init(java.lang.Exception)
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Exception)
meth public java.lang.String getBugText()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
meth public java.lang.Throwable getCause()
meth public void setBugText(java.lang.String)
supr java.lang.AssertionError
hfds exception,message,serialVersionUID

CLSS public org.codehaus.groovy.GroovyException
cons public init()
cons public init(boolean)
cons public init(java.lang.String)
cons public init(java.lang.String,boolean)
cons public init(java.lang.String,java.lang.Throwable)
intf org.codehaus.groovy.GroovyExceptionInterface
meth public boolean isFatal()
meth public void setFatal(boolean)
supr java.lang.Exception
hfds fatal,serialVersionUID

CLSS public abstract interface org.codehaus.groovy.GroovyExceptionInterface
meth public abstract boolean isFatal()
meth public abstract void setFatal(boolean)

CLSS public org.codehaus.groovy.ant.AntProjectPropertiesDelegate
cons public init(java.util.Map)
cons public init(org.apache.tools.ant.Project)
meth public boolean contains(java.lang.Object)
meth public boolean containsKey(java.lang.Object)
meth public boolean containsValue(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public boolean isEmpty()
meth public int hashCode()
meth public int size()
meth public java.lang.Object clone()
meth public java.lang.Object get(java.lang.Object)
meth public java.lang.Object put(java.lang.Object,java.lang.Object)
meth public java.lang.Object remove(java.lang.Object)
meth public java.lang.String toString()
meth public java.util.Collection values()
meth public java.util.Enumeration elements()
meth public java.util.Enumeration keys()
meth public java.util.Set entrySet()
meth public java.util.Set keySet()
meth public void clear()
meth public void putAll(java.util.Map)
supr java.util.Hashtable
hfds project

CLSS public abstract org.codehaus.groovy.ant.CompileTaskSupport

CLSS public org.codehaus.groovy.ant.FileIterator
cons public init(org.apache.tools.ant.Project,java.util.Iterator<org.apache.tools.ant.types.FileSet>)
cons public init(org.apache.tools.ant.Project,java.util.Iterator<org.apache.tools.ant.types.FileSet>,boolean)
intf java.util.Iterator
meth public boolean hasNext()
meth public java.lang.Object next()
meth public void remove()
supr java.lang.Object
hfds ds,fileIndex,fileSetIterator,files,iterateDirectories,nextFile,nextObjectSet,project

CLSS public org.codehaus.groovy.ant.FileScanner
hfds filesets

CLSS public org.codehaus.groovy.ant.FileSystemCompilerFacade
cons public init()
meth public static void main(java.lang.String[])
supr java.lang.Object

CLSS public org.codehaus.groovy.ant.GenerateStubsTask

CLSS public org.codehaus.groovy.ant.Groovy
hfds EMPTY_OBJECT_ARRAY,PREFIX,SUFFIX,append,classpath,cmdline,command,configscript,configuration,contextClassLoader,filesets,fork,includeAntRuntime,indy,log,output,scriptBaseClass,srcFile,useGroovyShell

CLSS public org.codehaus.groovy.ant.Groovyc
hfds EMPTY_URL_ARRAY,compileClasspath,compileSourcepath,configscript,destDir,encoding,errorProperty,forceLookupUnnamedFiles,fork,forkJavaHome,forkedExecutable,includeAntRuntime,includeDestClasses,includeJavaRuntime,javac,jointCompilation,keepStubs,log,memoryInitialSize,memoryMaximumSize,parameters,scriptBaseClass,scriptExtension,scriptExtensions,src,stacktrace,stubDir,targetBytecode,taskSuccess,temporaryFiles,updatedProperty,useIndy,verbose

CLSS public org.codehaus.groovy.ant.GroovycTask

CLSS public org.codehaus.groovy.ant.Groovydoc
hfds author,charset,destDir,docTitle,excludePackageNames,extensions,fileEncoding,footer,header,includeMainForScripts,includeNoSourcePackages,links,log,noTimestamp,noVersionStamp,overviewFile,packageNames,packageScope,packageSets,privateScope,processScripts,protectedScope,publicScope,sourceFilesToDoc,sourcePath,styleSheetFile,useDefaultExcludes,windowTitle

CLSS public org.codehaus.groovy.ant.LoggingHelper
cons public init(org.apache.tools.ant.Task)
meth public void debug(java.lang.String)
meth public void error(java.lang.String)
meth public void error(java.lang.String,java.lang.Throwable)
meth public void info(java.lang.String)
meth public void verbose(java.lang.String)
meth public void warn(java.lang.String)
supr java.lang.Object
hfds owner

CLSS public org.codehaus.groovy.ant.RootLoaderRef
hfds name,taskClasspath

CLSS public org.codehaus.groovy.ant.UberCompileTask
hfds classpath,destdir,genStubsTask,groovycTask,javacTask,src
hcls GenStubsAdapter,GroovycAdapter,JavacAdapter

CLSS public org.codehaus.groovy.ant.VerifyClass
hfds topDir,verbose

CLSS public org.codehaus.groovy.antlr.parser.GroovyLexer
cons public init(groovyjarjarantlr.InputBuffer)
cons public init(groovyjarjarantlr.LexerSharedInputState)
cons public init(java.io.InputStream)
cons public init(java.io.Reader)
fld protected final static int SCS_DRE_TYPE = 3
fld protected final static int SCS_LIMIT = 16
fld protected final static int SCS_LIT = 8
fld protected final static int SCS_RE_TYPE = 2
fld protected final static int SCS_SQ_TYPE = 0
fld protected final static int SCS_TQ_TYPE = 1
fld protected final static int SCS_TYPE = 3
fld protected final static int SCS_VAL = 4
fld protected int lastSigTokenType
fld protected int parenLevel
fld protected int stringCtorState
fld protected int suppressNewline
fld protected java.util.ArrayList parenLevelStack
fld protected org.codehaus.groovy.antlr.parser.GroovyRecognizer parser
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_0
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_1
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_10
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_11
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_12
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_13
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_2
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_3
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_4
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_5
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_6
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_7
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_8
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_9
fld public static boolean tracing
intf groovyjarjarantlr.TokenStream
intf org.codehaus.groovy.antlr.parser.GroovyTokenTypes
meth protected boolean allowRegexpLiteral()
meth protected boolean atDollarDollarEscape() throws groovyjarjarantlr.CharStreamException
meth protected boolean atDollarSlashEscape() throws groovyjarjarantlr.CharStreamException
meth protected boolean atMultiCommentStart() throws groovyjarjarantlr.CharStreamException
meth protected boolean atValidDollarEscape() throws groovyjarjarantlr.CharStreamException
meth protected final int mDOLLAR_REGEXP_CTOR_END(boolean,boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final int mREGEXP_CTOR_END(boolean,boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final int mSTRING_CTOR_END(boolean,boolean,boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mBIG_SUFFIX(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mDIGIT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mDIGITS_WITH_UNDERSCORE(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mDIGITS_WITH_UNDERSCORE_OPT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mDIV(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mDIV_ASSIGN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mDOLLAR(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mDOLLAR_REGEXP_SYMBOL(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mESC(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mESCAPED_DOLLAR(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mESCAPED_SLASH(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mEXPONENT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mFLOAT_SUFFIX(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mHEX_DIGIT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mLETTER(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mONE_NL(boolean,boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mREGEXP_SYMBOL(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mSTRING_CH(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mSTRING_NL(boolean,boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void mVOCAB(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected groovyjarjarantlr.Token makeToken(int)
meth protected static boolean isExpressionEndingToken(int)
meth protected void newlineCheck(boolean) throws groovyjarjarantlr.RecognitionException
meth protected void popParenLevel()
meth protected void pushParenLevel()
meth protected void restartStringCtor(boolean)
meth public boolean isAssertEnabled()
meth public boolean isEnumEnabled()
meth public boolean isWhitespaceIncluded()
meth public final void mASSIGN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mAT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mBAND(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mBAND_ASSIGN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mBNOT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mBOR(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mBOR_ASSIGN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mBSR(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mBSR_ASSIGN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mBXOR(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mBXOR_ASSIGN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mCLOSABLE_BLOCK_OP(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mCOLON(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mCOMMA(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mCOMPARE_TO(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mDEC(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mDOLLAR_REGEXP_LITERAL(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mDOT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mELVIS_OPERATOR(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mEQUAL(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mGE(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mGT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mIDENT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mIDENTICAL(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mINC(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mLAND(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mLBRACK(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mLCURLY(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mLE(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mLNOT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mLOR(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mLPAREN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mLT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mMEMBER_POINTER(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mMINUS(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mMINUS_ASSIGN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mML_COMMENT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mMOD(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mMOD_ASSIGN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mNLS(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mNOT_EQUAL(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mNOT_IDENTICAL(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mNUM_INT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mOPTIONAL_DOT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mPLUS(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mPLUS_ASSIGN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mQUESTION(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mRANGE_EXCLUSIVE(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mRANGE_INCLUSIVE(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mRBRACK(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mRCURLY(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mREGEXP_LITERAL(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mREGEX_FIND(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mREGEX_MATCH(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mRPAREN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSEMI(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSH_COMMENT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSL(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSL_ASSIGN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSL_COMMENT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSPREAD_DOT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSR(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSR_ASSIGN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSTAR(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSTAR_ASSIGN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSTAR_STAR(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSTAR_STAR_ASSIGN(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mSTRING_LITERAL(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mTRIPLE_DOT(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void mWS(boolean) throws groovyjarjarantlr.CharStreamException,groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public groovyjarjarantlr.Token nextToken() throws groovyjarjarantlr.TokenStreamException
meth public groovyjarjarantlr.TokenStream plumb()
meth public void enableAssert(boolean)
meth public void enableEnum(boolean)
meth public void setTokenObjectClass(java.lang.String)
meth public void setWhitespaceIncluded(boolean)
meth public void traceIn(java.lang.String) throws groovyjarjarantlr.CharStreamException
meth public void traceOut(java.lang.String) throws groovyjarjarantlr.CharStreamException
supr groovyjarjarantlr.CharScanner
hfds assertEnabled,enumEnabled,ttypes,whitespaceIncluded

CLSS public org.codehaus.groovy.antlr.parser.GroovyRecognizer
cons protected init(groovyjarjarantlr.TokenBuffer,int)
cons protected init(groovyjarjarantlr.TokenStream,int)
cons public init(groovyjarjarantlr.ParserSharedInputState)
cons public init(groovyjarjarantlr.TokenBuffer)
cons public init(groovyjarjarantlr.TokenStream)
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_0
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_1
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_10
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_100
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_101
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_102
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_103
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_104
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_105
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_106
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_107
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_108
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_109
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_11
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_110
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_111
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_12
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_13
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_14
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_15
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_16
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_17
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_18
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_19
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_2
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_20
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_21
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_22
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_23
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_24
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_25
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_26
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_27
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_28
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_29
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_3
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_30
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_31
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_32
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_33
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_34
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_35
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_36
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_37
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_38
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_39
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_4
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_40
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_41
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_42
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_43
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_44
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_45
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_46
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_47
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_48
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_49
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_5
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_50
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_51
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_52
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_53
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_54
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_55
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_56
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_57
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_58
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_59
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_6
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_60
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_61
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_62
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_63
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_64
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_65
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_66
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_67
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_68
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_69
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_7
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_70
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_71
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_72
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_73
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_74
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_75
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_76
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_77
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_78
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_79
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_8
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_80
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_81
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_82
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_83
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_84
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_85
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_86
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_87
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_88
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_89
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_9
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_90
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_91
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_92
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_93
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_94
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_95
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_96
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_97
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_98
fld public final static groovyjarjarantlr.collections.impl.BitSet _tokenSet_99
fld public final static java.lang.String[] _tokenNames
fld public static boolean tracing
intf org.codehaus.groovy.antlr.parser.GroovyTokenTypes
meth protected final void enumConstantFieldInternal(groovyjarjarantlr.collections.AST,groovyjarjarantlr.collections.AST,groovyjarjarantlr.collections.AST,groovyjarjarantlr.Token) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void typeArgumentsOrParametersEnd() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected final void typeDefinitionInternal(groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth protected void buildTokenTypeASTClassMap()
meth public final boolean strictContextExpression(boolean) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final byte argument() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void aCase() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void additiveExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void andExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void annotation() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void annotationArguments() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void annotationBlock() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void annotationDefinition(groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void annotationField() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void annotationIdent() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void annotationMemberValueInitializer() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void annotationMemberValuePair() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void annotationMemberValuePairs() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void annotationsInternal() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void annotationsOpt() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void appendedBlock(groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void argList() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void argumentLabel() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void argumentLabelStart() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void assignmentExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void assignmentLessExpression() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void balancedBrackets() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void balancedTokens() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void blockBody(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void branchStatement() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void builtInType() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void builtInTypeArraySpec(boolean) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void builtInTypeSpec(boolean) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void caseSList() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void casesGroup() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void checkSuspiciousExpressionStatement(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void classBlock() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void classDefinition(groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void classField() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void classOrInterfaceType(boolean) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void classTypeSpec(boolean) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void closableBlock() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void closableBlockConstructorExpression() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void closableBlockParam() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void closableBlockParamsOpt(boolean) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void closableBlockParamsStart() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void closureList() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void commandArgument() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void commandArguments(groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void commandArgumentsGreedy(groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void compatibleBodyStatement() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void compilationUnit() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void compoundStatement() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void conditionalExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void constant() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void constantNumber() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void constructorBody() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void constructorDefinition(groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void constructorStart() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void controlExpressionList() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void declaration() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void declarationStart() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void declaratorBrackets(groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void dynamicMemberName() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void enumBlock() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void enumConstant() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void enumConstantBlock() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void enumConstantField() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void enumConstants() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void enumConstantsStart() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void enumDefinition(groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void equalityExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void exclusiveOrExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void explicitConstructorInvocation() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void expression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void expressionStatement(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void expressionStatementNoCheck() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void finallyClause() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void forCond() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void forInClause() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void forInit() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void forIter() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void forStatement() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void genericMethod() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void genericMethodStart() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void handler() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void identifier() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void identifierStar() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void implementsClause() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void implicitParameters() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void importStatement() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void inclusiveOrExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void indexPropertyArgs(groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void interfaceBlock() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void interfaceDefinition(groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void interfaceExtends() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void interfaceField() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void keywordPropertyNames() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void listOfVariables(groovyjarjarantlr.collections.AST,groovyjarjarantlr.collections.AST,groovyjarjarantlr.Token) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void listOrMapConstructorExpression() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void logicalAndExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void logicalOrExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void methodCallArgs(groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void modifier() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void modifiers() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void modifiersInternal() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void modifiersOpt() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void multicatch() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void multicatch_types() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void multipleAssignment(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void multipleAssignmentDeclaration() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void multipleAssignmentDeclarationStart() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void multiplicativeExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void namePart() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void newArrayDeclarator() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void newExpression() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void nls() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void nlsWarn() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void openBlock() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void openOrClosableBlock() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void packageDefinition() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void parameterDeclaration() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void parameterDeclarationList() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void parameterModifiersOpt() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void parenthesizedExpression() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void pathChain(int,groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void pathElement(groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void pathElementStart() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void pathExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void postfixExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void powerExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void powerExpressionNotPlusMinus(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void primaryExpression() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void qualifiedTypeName() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void regexExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void relationalExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void sep() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void shiftExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void singleDeclaration() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void singleDeclarationNoInit() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void singleVariable(groovyjarjarantlr.collections.AST,groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void snippetUnit() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void statement(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void statementLabelPrefix() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void stringConstructorExpression() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void stringConstructorValuePart() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void superClassClause() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void suspiciousExpressionStatementStart() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void throwsClause() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void traitDefinition(groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void tryBlock() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void type() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void typeArgument() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void typeArgumentBounds() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void typeArgumentSpec() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void typeArguments() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void typeArgumentsDiamond() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void typeDefinitionStart() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void typeNamePairs(groovyjarjarantlr.collections.AST,groovyjarjarantlr.Token) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void typeParameter() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void typeParameterBounds() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void typeParameters() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void typeSpec(boolean) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void unaryExpression(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void unaryExpressionNotPlusMinus(int) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void upperCaseIdent() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void varInitializer() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void variableDeclarator(groovyjarjarantlr.collections.AST,groovyjarjarantlr.collections.AST,groovyjarjarantlr.Token) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void variableDefinitions(groovyjarjarantlr.collections.AST,groovyjarjarantlr.collections.AST) throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void variableName() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public final void wildcardType() throws groovyjarjarantlr.RecognitionException,groovyjarjarantlr.TokenStreamException
meth public groovyjarjarantlr.Token cloneToken(groovyjarjarantlr.Token)
meth public groovyjarjarantlr.collections.AST create(int,java.lang.String,groovyjarjarantlr.Token,groovyjarjarantlr.Token)
meth public groovyjarjarantlr.collections.AST create(int,java.lang.String,groovyjarjarantlr.collections.AST)
meth public groovyjarjarantlr.collections.AST create(int,java.lang.String,groovyjarjarantlr.collections.AST,groovyjarjarantlr.Token)
meth public groovyjarjarantlr.collections.AST create(int,java.lang.String,groovyjarjarantlr.collections.AST,groovyjarjarantlr.collections.AST)
meth public java.util.List getWarningList()
meth public org.codehaus.groovy.antlr.parser.GroovyLexer getLexer()
meth public static org.codehaus.groovy.antlr.parser.GroovyRecognizer make(groovyjarjarantlr.InputBuffer)
meth public static org.codehaus.groovy.antlr.parser.GroovyRecognizer make(groovyjarjarantlr.LexerSharedInputState)
meth public static org.codehaus.groovy.antlr.parser.GroovyRecognizer make(java.io.InputStream)
meth public static org.codehaus.groovy.antlr.parser.GroovyRecognizer make(java.io.Reader)
meth public static org.codehaus.groovy.antlr.parser.GroovyRecognizer make(org.codehaus.groovy.antlr.parser.GroovyLexer)
meth public void addWarning(java.lang.String,java.lang.String)
meth public void matchGenericTypeBracketsFailed(java.lang.String,java.lang.String) throws groovyjarjarantlr.SemanticException
meth public void requireFailed(java.lang.String,java.lang.String) throws groovyjarjarantlr.SemanticException
meth public void setFilename(java.lang.String)
meth public void setSourceBuffer(org.codehaus.groovy.antlr.SourceBuffer)
meth public void traceIn(java.lang.String) throws groovyjarjarantlr.TokenStreamException
meth public void traceOut(java.lang.String) throws groovyjarjarantlr.TokenStreamException
supr groovyjarjarantlr.LLkParser
hfds ANTLR_LOOP_EXIT,LC_INIT,LC_STMT,argListHasLabels,currentClass,dummyVariableToforceClassLoaderToFindASTClass,lastPathExpression,lexer,ltCounter,sepToken,sourceBuffer,warningList

CLSS public abstract interface org.codehaus.groovy.antlr.parser.GroovyTokenTypes
fld public final static int ABSTRACT = 39
fld public final static int ANNOTATION = 66
fld public final static int ANNOTATIONS = 65
fld public final static int ANNOTATION_ARRAY_INIT = 69
fld public final static int ANNOTATION_DEF = 64
fld public final static int ANNOTATION_FIELD_DEF = 68
fld public final static int ANNOTATION_MEMBER_VALUE_PAIR = 67
fld public final static int ARRAY_DECLARATOR = 17
fld public final static int ASSIGN = 124
fld public final static int AT = 96
fld public final static int BAND = 125
fld public final static int BAND_ASSIGN = 170
fld public final static int BIG_SUFFIX = 230
fld public final static int BLOCK = 4
fld public final static int BNOT = 195
fld public final static int BOR = 134
fld public final static int BOR_ASSIGN = 172
fld public final static int BSR = 103
fld public final static int BSR_ASSIGN = 168
fld public final static int BXOR = 177
fld public final static int BXOR_ASSIGN = 171
fld public final static int CASE_GROUP = 32
fld public final static int CLASS_DEF = 13
fld public final static int CLOSABLE_BLOCK = 50
fld public final static int CLOSABLE_BLOCK_OP = 135
fld public final static int CLOSURE_LIST = 77
fld public final static int COLON = 136
fld public final static int COMMA = 101
fld public final static int COMPARE_TO = 184
fld public final static int CTOR_CALL = 45
fld public final static int CTOR_IDENT = 46
fld public final static int DEC = 193
fld public final static int DIGIT = 225
fld public final static int DIGITS_WITH_UNDERSCORE = 226
fld public final static int DIGITS_WITH_UNDERSCORE_OPT = 227
fld public final static int DIV = 191
fld public final static int DIV_ASSIGN = 165
fld public final static int DOLLAR = 206
fld public final static int DOLLAR_REGEXP_CTOR_END = 215
fld public final static int DOLLAR_REGEXP_LITERAL = 213
fld public final static int DOLLAR_REGEXP_SYMBOL = 219
fld public final static int DOT = 90
fld public final static int DYNAMIC_MEMBER = 53
fld public final static int ELIST = 33
fld public final static int ELVIS_OPERATOR = 174
fld public final static int EMPTY_STAT = 37
fld public final static int ENUM_CONSTANT_DEF = 62
fld public final static int ENUM_DEF = 61
fld public final static int EOF = 1
fld public final static int EQUAL = 181
fld public final static int ESC = 220
fld public final static int ESCAPED_DOLLAR = 217
fld public final static int ESCAPED_SLASH = 216
fld public final static int EXPONENT = 228
fld public final static int EXPR = 28
fld public final static int EXTENDS_CLAUSE = 18
fld public final static int FINAL = 38
fld public final static int FLOAT_SUFFIX = 229
fld public final static int FOR_CONDITION = 35
fld public final static int FOR_EACH_CLAUSE = 63
fld public final static int FOR_INIT = 34
fld public final static int FOR_IN_ITERABLE = 59
fld public final static int FOR_ITERATOR = 36
fld public final static int GE = 186
fld public final static int GT = 100
fld public final static int HEX_DIGIT = 222
fld public final static int IDENT = 87
fld public final static int IDENTICAL = 182
fld public final static int IMPLEMENTS_CLAUSE = 19
fld public final static int IMPLICIT_PARAMETERS = 51
fld public final static int IMPORT = 29
fld public final static int INC = 190
fld public final static int INDEX_OP = 24
fld public final static int INSTANCE_INIT = 10
fld public final static int INTERFACE_DEF = 14
fld public final static int LABELED_ARG = 54
fld public final static int LABELED_STAT = 22
fld public final static int LAND = 176
fld public final static int LBRACK = 85
fld public final static int LCURLY = 126
fld public final static int LE = 185
fld public final static int LETTER = 224
fld public final static int LIST_CONSTRUCTOR = 57
fld public final static int LITERAL_as = 114
fld public final static int LITERAL_assert = 147
fld public final static int LITERAL_boolean = 105
fld public final static int LITERAL_break = 144
fld public final static int LITERAL_byte = 106
fld public final static int LITERAL_case = 150
fld public final static int LITERAL_catch = 153
fld public final static int LITERAL_char = 107
fld public final static int LITERAL_class = 92
fld public final static int LITERAL_continue = 145
fld public final static int LITERAL_def = 84
fld public final static int LITERAL_default = 129
fld public final static int LITERAL_double = 112
fld public final static int LITERAL_else = 138
fld public final static int LITERAL_enum = 94
fld public final static int LITERAL_extends = 98
fld public final static int LITERAL_false = 157
fld public final static int LITERAL_finally = 152
fld public final static int LITERAL_float = 110
fld public final static int LITERAL_for = 141
fld public final static int LITERAL_if = 137
fld public final static int LITERAL_implements = 131
fld public final static int LITERAL_import = 82
fld public final static int LITERAL_in = 142
fld public final static int LITERAL_instanceof = 158
fld public final static int LITERAL_int = 109
fld public final static int LITERAL_interface = 93
fld public final static int LITERAL_long = 111
fld public final static int LITERAL_native = 119
fld public final static int LITERAL_new = 159
fld public final static int LITERAL_null = 160
fld public final static int LITERAL_package = 81
fld public final static int LITERAL_private = 115
fld public final static int LITERAL_protected = 117
fld public final static int LITERAL_public = 116
fld public final static int LITERAL_return = 143
fld public final static int LITERAL_short = 108
fld public final static int LITERAL_static = 83
fld public final static int LITERAL_super = 99
fld public final static int LITERAL_switch = 140
fld public final static int LITERAL_synchronized = 121
fld public final static int LITERAL_this = 132
fld public final static int LITERAL_threadsafe = 120
fld public final static int LITERAL_throw = 146
fld public final static int LITERAL_throws = 130
fld public final static int LITERAL_trait = 95
fld public final static int LITERAL_transient = 118
fld public final static int LITERAL_true = 161
fld public final static int LITERAL_try = 151
fld public final static int LITERAL_void = 104
fld public final static int LITERAL_volatile = 122
fld public final static int LITERAL_while = 139
fld public final static int LNOT = 196
fld public final static int LOR = 175
fld public final static int LPAREN = 91
fld public final static int LT = 89
fld public final static int MAP_CONSTRUCTOR = 58
fld public final static int MEMBER_POINTER = 156
fld public final static int METHOD_CALL = 27
fld public final static int METHOD_DEF = 8
fld public final static int MINUS = 149
fld public final static int MINUS_ASSIGN = 163
fld public final static int ML_COMMENT = 210
fld public final static int MOD = 192
fld public final static int MODIFIERS = 5
fld public final static int MOD_ASSIGN = 166
fld public final static int MULTICATCH = 78
fld public final static int MULTICATCH_TYPES = 79
fld public final static int NLS = 205
fld public final static int NOT_EQUAL = 180
fld public final static int NOT_IDENTICAL = 183
fld public final static int NULL_TREE_LOOKAHEAD = 3
fld public final static int NUM_BIG_DECIMAL = 204
fld public final static int NUM_BIG_INT = 203
fld public final static int NUM_DOUBLE = 202
fld public final static int NUM_FLOAT = 200
fld public final static int NUM_INT = 199
fld public final static int NUM_LONG = 201
fld public final static int OBJBLOCK = 6
fld public final static int ONE_NL = 208
fld public final static int OPTIONAL_DOT = 155
fld public final static int PACKAGE_DEF = 16
fld public final static int PARAMETERS = 20
fld public final static int PARAMETER_DEF = 21
fld public final static int PLUS = 148
fld public final static int PLUS_ASSIGN = 162
fld public final static int POST_DEC = 26
fld public final static int POST_INC = 25
fld public final static int QUESTION = 97
fld public final static int RANGE_EXCLUSIVE = 189
fld public final static int RANGE_INCLUSIVE = 188
fld public final static int RBRACK = 86
fld public final static int RCURLY = 127
fld public final static int REGEXP_CTOR_END = 214
fld public final static int REGEXP_LITERAL = 212
fld public final static int REGEXP_SYMBOL = 218
fld public final static int REGEX_FIND = 178
fld public final static int REGEX_MATCH = 179
fld public final static int RPAREN = 123
fld public final static int SELECT_SLOT = 52
fld public final static int SEMI = 128
fld public final static int SH_COMMENT = 80
fld public final static int SL = 187
fld public final static int SLIST = 7
fld public final static int SL_ASSIGN = 169
fld public final static int SL_COMMENT = 209
fld public final static int SPREAD_ARG = 55
fld public final static int SPREAD_DOT = 154
fld public final static int SPREAD_MAP_ARG = 56
fld public final static int SR = 102
fld public final static int SR_ASSIGN = 167
fld public final static int STAR = 113
fld public final static int STAR_ASSIGN = 164
fld public final static int STAR_STAR = 194
fld public final static int STAR_STAR_ASSIGN = 173
fld public final static int STATIC_IMPORT = 60
fld public final static int STATIC_INIT = 11
fld public final static int STRICTFP = 43
fld public final static int STRING_CH = 211
fld public final static int STRING_CONSTRUCTOR = 48
fld public final static int STRING_CTOR_END = 198
fld public final static int STRING_CTOR_MIDDLE = 49
fld public final static int STRING_CTOR_START = 197
fld public final static int STRING_LITERAL = 88
fld public final static int STRING_NL = 221
fld public final static int SUPER_CTOR_CALL = 44
fld public final static int TRAIT_DEF = 15
fld public final static int TRIPLE_DOT = 133
fld public final static int TYPE = 12
fld public final static int TYPECAST = 23
fld public final static int TYPE_ARGUMENT = 71
fld public final static int TYPE_ARGUMENTS = 70
fld public final static int TYPE_LOWER_BOUNDS = 76
fld public final static int TYPE_PARAMETER = 73
fld public final static int TYPE_PARAMETERS = 72
fld public final static int TYPE_UPPER_BOUNDS = 75
fld public final static int UNARY_MINUS = 30
fld public final static int UNARY_PLUS = 31
fld public final static int UNUSED_CONST = 41
fld public final static int UNUSED_DO = 42
fld public final static int UNUSED_GOTO = 40
fld public final static int VARIABLE_DEF = 9
fld public final static int VARIABLE_PARAMETER_DEF = 47
fld public final static int VOCAB = 223
fld public final static int WILDCARD_TYPE = 74
fld public final static int WS = 207

CLSS public org.codehaus.groovy.ast.ASTNode
cons public init()
meth public <%0 extends java.lang.Object> {%%0} getNodeMetaData(java.lang.Object)
meth public boolean equals(java.lang.Object)
meth public int getColumnNumber()
meth public int getLastColumnNumber()
meth public int getLastLineNumber()
meth public int getLineNumber()
meth public int hashCode()
meth public java.lang.Object putNodeMetaData(java.lang.Object,java.lang.Object)
meth public java.lang.String getText()
meth public java.util.Map<?,?> getNodeMetaData()
meth public org.codehaus.groovy.util.ListHashMap getMetaDataMap()
meth public void copyNodeMetaData(org.codehaus.groovy.ast.ASTNode)
meth public void removeNodeMetaData(java.lang.Object)
meth public void setColumnNumber(int)
meth public void setLastColumnNumber(int)
meth public void setLastLineNumber(int)
meth public void setLineNumber(int)
meth public void setNodeMetaData(java.lang.Object,java.lang.Object)
meth public void setSourcePosition(org.codehaus.groovy.ast.ASTNode)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr java.lang.Object
hfds columnNumber,lastColumnNumber,lastLineNumber,lineNumber,metaDataMap

CLSS public org.codehaus.groovy.ast.AnnotatedNode
cons public init()
meth public boolean hasNoRealSourcePosition()
meth public boolean isSynthetic()
meth public java.util.List<org.codehaus.groovy.ast.AnnotationNode> getAnnotations()
meth public java.util.List<org.codehaus.groovy.ast.AnnotationNode> getAnnotations(org.codehaus.groovy.ast.ClassNode)
meth public org.codehaus.groovy.ast.ClassNode getDeclaringClass()
meth public void addAnnotation(org.codehaus.groovy.ast.AnnotationNode)
meth public void addAnnotations(java.util.List<org.codehaus.groovy.ast.AnnotationNode>)
meth public void setDeclaringClass(org.codehaus.groovy.ast.ClassNode)
meth public void setHasNoRealSourcePosition(boolean)
meth public void setSynthetic(boolean)
supr org.codehaus.groovy.ast.ASTNode
hfds annotations,declaringClass,hasNoRealSourcePositionFlag,synthetic

CLSS public org.codehaus.groovy.ast.AnnotationNode
cons public init(org.codehaus.groovy.ast.ClassNode)
fld public final static int ANNOTATION_TARGET = 64
fld public final static int CONSTRUCTOR_TARGET = 2
fld public final static int FIELD_TARGET = 8
fld public final static int LOCAL_VARIABLE_TARGET = 32
fld public final static int METHOD_TARGET = 4
fld public final static int PACKAGE_TARGET = 128
fld public final static int PARAMETER_TARGET = 16
fld public final static int TYPE_PARAMETER_TARGET = 256
fld public final static int TYPE_TARGET = 65
fld public final static int TYPE_USE_TARGET = 512
meth public boolean hasClassRetention()
meth public boolean hasRuntimeRetention()
meth public boolean hasSourceRetention()
meth public boolean isBuiltIn()
meth public boolean isTargetAllowed(int)
meth public java.util.Map<java.lang.String,org.codehaus.groovy.ast.expr.Expression> getMembers()
meth public org.codehaus.groovy.ast.ClassNode getClassNode()
meth public org.codehaus.groovy.ast.expr.Expression getMember(java.lang.String)
meth public static java.lang.String targetToName(int)
meth public void addMember(java.lang.String,org.codehaus.groovy.ast.expr.Expression)
meth public void setAllowedTargets(int)
meth public void setClassRetention(boolean)
meth public void setMember(java.lang.String,org.codehaus.groovy.ast.expr.Expression)
meth public void setRuntimeRetention(boolean)
meth public void setSourceRetention(boolean)
supr org.codehaus.groovy.ast.ASTNode
hfds ALL_TARGETS,allowedTargets,classNode,classRetention,members,runtimeRetention,sourceRetention

CLSS public org.codehaus.groovy.ast.AstToTextHelper
cons public init()
meth public static java.lang.String getClassText(org.codehaus.groovy.ast.ClassNode)
meth public static java.lang.String getModifiersText(int)
meth public static java.lang.String getParameterText(org.codehaus.groovy.ast.Parameter)
meth public static java.lang.String getParametersText(org.codehaus.groovy.ast.Parameter[])
meth public static java.lang.String getThrowsClauseText(org.codehaus.groovy.ast.ClassNode[])
supr java.lang.Object

CLSS public abstract org.codehaus.groovy.ast.ClassCodeExpressionTransformer
cons public init()
intf org.codehaus.groovy.ast.expr.ExpressionTransformer
meth protected void visitConstructorOrMethod(org.codehaus.groovy.ast.MethodNode,boolean)
meth public org.codehaus.groovy.ast.expr.Expression transform(org.codehaus.groovy.ast.expr.Expression)
meth public void visitAnnotations(org.codehaus.groovy.ast.AnnotatedNode)
meth public void visitAssertStatement(org.codehaus.groovy.ast.stmt.AssertStatement)
meth public void visitCaseStatement(org.codehaus.groovy.ast.stmt.CaseStatement)
meth public void visitDoWhileLoop(org.codehaus.groovy.ast.stmt.DoWhileStatement)
meth public void visitExpressionStatement(org.codehaus.groovy.ast.stmt.ExpressionStatement)
meth public void visitField(org.codehaus.groovy.ast.FieldNode)
meth public void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement)
meth public void visitIfElse(org.codehaus.groovy.ast.stmt.IfStatement)
meth public void visitProperty(org.codehaus.groovy.ast.PropertyNode)
meth public void visitReturnStatement(org.codehaus.groovy.ast.stmt.ReturnStatement)
meth public void visitSwitch(org.codehaus.groovy.ast.stmt.SwitchStatement)
meth public void visitSynchronizedStatement(org.codehaus.groovy.ast.stmt.SynchronizedStatement)
meth public void visitThrowStatement(org.codehaus.groovy.ast.stmt.ThrowStatement)
meth public void visitWhileLoop(org.codehaus.groovy.ast.stmt.WhileStatement)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport

CLSS public abstract org.codehaus.groovy.ast.ClassCodeVisitorSupport
cons public init()
intf org.codehaus.groovy.ast.GroovyClassVisitor
intf org.codehaus.groovy.transform.ErrorCollecting
meth protected abstract org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth protected void visitClassCodeContainer(org.codehaus.groovy.ast.stmt.Statement)
meth protected void visitConstructorOrMethod(org.codehaus.groovy.ast.MethodNode,boolean)
meth protected void visitObjectInitializerStatements(org.codehaus.groovy.ast.ClassNode)
meth protected void visitStatement(org.codehaus.groovy.ast.stmt.Statement)
meth public void addError(java.lang.String,org.codehaus.groovy.ast.ASTNode)
meth public void visitAnnotations(org.codehaus.groovy.ast.AnnotatedNode)
meth public void visitAssertStatement(org.codehaus.groovy.ast.stmt.AssertStatement)
meth public void visitBlockStatement(org.codehaus.groovy.ast.stmt.BlockStatement)
meth public void visitBreakStatement(org.codehaus.groovy.ast.stmt.BreakStatement)
meth public void visitCaseStatement(org.codehaus.groovy.ast.stmt.CaseStatement)
meth public void visitCatchStatement(org.codehaus.groovy.ast.stmt.CatchStatement)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public void visitConstructor(org.codehaus.groovy.ast.ConstructorNode)
meth public void visitContinueStatement(org.codehaus.groovy.ast.stmt.ContinueStatement)
meth public void visitDeclarationExpression(org.codehaus.groovy.ast.expr.DeclarationExpression)
meth public void visitDoWhileLoop(org.codehaus.groovy.ast.stmt.DoWhileStatement)
meth public void visitExpressionStatement(org.codehaus.groovy.ast.stmt.ExpressionStatement)
meth public void visitField(org.codehaus.groovy.ast.FieldNode)
meth public void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement)
meth public void visitIfElse(org.codehaus.groovy.ast.stmt.IfStatement)
meth public void visitImports(org.codehaus.groovy.ast.ModuleNode)
meth public void visitMethod(org.codehaus.groovy.ast.MethodNode)
meth public void visitPackage(org.codehaus.groovy.ast.PackageNode)
meth public void visitProperty(org.codehaus.groovy.ast.PropertyNode)
meth public void visitReturnStatement(org.codehaus.groovy.ast.stmt.ReturnStatement)
meth public void visitSwitch(org.codehaus.groovy.ast.stmt.SwitchStatement)
meth public void visitSynchronizedStatement(org.codehaus.groovy.ast.stmt.SynchronizedStatement)
meth public void visitThrowStatement(org.codehaus.groovy.ast.stmt.ThrowStatement)
meth public void visitTryCatchFinally(org.codehaus.groovy.ast.stmt.TryCatchStatement)
meth public void visitWhileLoop(org.codehaus.groovy.ast.stmt.WhileStatement)
supr org.codehaus.groovy.ast.CodeVisitorSupport

CLSS public org.codehaus.groovy.ast.ClassHelper
cons public init()
fld protected final static org.codehaus.groovy.ast.ClassNode[] EMPTY_TYPE_ARRAY
fld public final static java.lang.String OBJECT = "java.lang.Object"
fld public final static org.codehaus.groovy.ast.ClassNode Annotation_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode BINDING_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode BigDecimal_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode BigInteger_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode Boolean_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode Byte_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode CLASS_Type
fld public final static org.codehaus.groovy.ast.ClassNode CLOSURE_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode COMPARABLE_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode Character_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode DYNAMIC_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode Double_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode ELEMENT_TYPE_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode Enum_Type
fld public final static org.codehaus.groovy.ast.ClassNode Float_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode GENERATED_CLOSURE_Type
fld public final static org.codehaus.groovy.ast.ClassNode GROOVY_INTERCEPTABLE_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode GROOVY_OBJECT_SUPPORT_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode GROOVY_OBJECT_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode GSTRING_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode Integer_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode Iterator_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode LIST_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode Long_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode MAP_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode METACLASS_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode Number_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode OBJECT_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode PATTERN_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode RANGE_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode REFERENCE_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode SCRIPT_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode STRING_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode Short_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode VOID_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode boolean_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode byte_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode char_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode double_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode float_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode int_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode long_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode short_TYPE
fld public final static org.codehaus.groovy.ast.ClassNode void_WRAPPER_TYPE
meth public static boolean isCachedType(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isNumberType(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isPrimitiveType(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isSAMType(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isStaticConstantInitializerType(org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode getNextSuperClass(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode getUnwrapper(org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode getWrapper(org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode make(java.lang.Class)
meth public static org.codehaus.groovy.ast.ClassNode make(java.lang.Class,boolean)
meth public static org.codehaus.groovy.ast.ClassNode make(java.lang.String)
meth public static org.codehaus.groovy.ast.ClassNode makeCached(java.lang.Class)
meth public static org.codehaus.groovy.ast.ClassNode makeReference()
meth public static org.codehaus.groovy.ast.ClassNode makeWithoutCaching(java.lang.Class)
meth public static org.codehaus.groovy.ast.ClassNode makeWithoutCaching(java.lang.Class,boolean)
meth public static org.codehaus.groovy.ast.ClassNode makeWithoutCaching(java.lang.String)
meth public static org.codehaus.groovy.ast.ClassNode[] make(java.lang.Class[])
meth public static org.codehaus.groovy.ast.MethodNode findSAM(org.codehaus.groovy.ast.ClassNode)
supr java.lang.Object
hfds ABSTRACT_STATIC_PRIVATE,PRIMITIVE_TYPE_TO_WRAPPER_TYPE_MAP,VISIBILITY,WRAPPER_TYPE_TO_PRIMITIVE_TYPE_MAP,classes,primitiveClassNames,types
hcls ClassHelperCache

CLSS public org.codehaus.groovy.ast.ClassNode
cons public init(java.lang.Class)
cons public init(java.lang.String,int,org.codehaus.groovy.ast.ClassNode)
cons public init(java.lang.String,int,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.MixinNode[])
fld protected boolean isPrimaryNode
fld protected final java.lang.Object lazyInitLock
fld protected java.lang.Class clazz
fld protected java.util.List<org.codehaus.groovy.ast.InnerClassNode> innerClasses
fld public final static org.codehaus.groovy.ast.ClassNode SUPER
fld public final static org.codehaus.groovy.ast.ClassNode THIS
fld public final static org.codehaus.groovy.ast.ClassNode[] EMPTY_ARRAY
intf groovyjarjarasm.asm.Opcodes
meth protected boolean parametersEqual(org.codehaus.groovy.ast.Parameter[],org.codehaus.groovy.ast.Parameter[])
meth protected void setCompileUnit(org.codehaus.groovy.ast.CompileUnit)
meth public boolean declaresInterface(org.codehaus.groovy.ast.ClassNode)
meth public boolean equals(java.lang.Object)
meth public boolean hasDeclaredMethod(java.lang.String,org.codehaus.groovy.ast.Parameter[])
meth public boolean hasMethod(java.lang.String,org.codehaus.groovy.ast.Parameter[])
meth public boolean hasPackageName()
meth public boolean hasPossibleMethod(java.lang.String,org.codehaus.groovy.ast.expr.Expression)
meth public boolean hasPossibleStaticMethod(java.lang.String,org.codehaus.groovy.ast.expr.Expression)
meth public boolean hasProperty(java.lang.String)
meth public boolean implementsInterface(org.codehaus.groovy.ast.ClassNode)
meth public boolean isAbstract()
meth public boolean isAnnotated()
meth public boolean isAnnotationDefinition()
meth public boolean isArray()
meth public boolean isDerivedFrom(org.codehaus.groovy.ast.ClassNode)
meth public boolean isDerivedFromGroovyObject()
meth public boolean isEnum()
meth public boolean isGenericsPlaceHolder()
meth public boolean isInterface()
meth public boolean isPrimaryClassNode()
meth public boolean isRedirectNode()
meth public boolean isResolved()
meth public boolean isScript()
meth public boolean isScriptBody()
meth public boolean isStaticClass()
meth public boolean isSyntheticPublic()
meth public boolean isUsingGenerics()
meth public int getModifiers()
meth public int hashCode()
meth public java.lang.Class getTypeClass()
meth public java.lang.String getName()
meth public java.lang.String getNameWithoutPackage()
meth public java.lang.String getPackageName()
meth public java.lang.String getText()
meth public java.lang.String getUnresolvedName()
meth public java.lang.String setName(java.lang.String)
meth public java.lang.String toString()
meth public java.lang.String toString(boolean)
meth public java.util.Iterator<org.codehaus.groovy.ast.InnerClassNode> getInnerClasses()
meth public java.util.List<org.codehaus.groovy.ast.AnnotationNode> getAnnotations()
meth public java.util.List<org.codehaus.groovy.ast.AnnotationNode> getAnnotations(org.codehaus.groovy.ast.ClassNode)
meth public java.util.List<org.codehaus.groovy.ast.ClassNode> getOuterClasses()
meth public java.util.List<org.codehaus.groovy.ast.ConstructorNode> getDeclaredConstructors()
meth public java.util.List<org.codehaus.groovy.ast.FieldNode> getFields()
meth public java.util.List<org.codehaus.groovy.ast.MethodNode> getAbstractMethods()
meth public java.util.List<org.codehaus.groovy.ast.MethodNode> getAllDeclaredMethods()
meth public java.util.List<org.codehaus.groovy.ast.MethodNode> getDeclaredMethods(java.lang.String)
meth public java.util.List<org.codehaus.groovy.ast.MethodNode> getMethods()
meth public java.util.List<org.codehaus.groovy.ast.MethodNode> getMethods(java.lang.String)
meth public java.util.List<org.codehaus.groovy.ast.PropertyNode> getProperties()
meth public java.util.List<org.codehaus.groovy.ast.stmt.Statement> getObjectInitializerStatements()
meth public java.util.Map<java.lang.Class<? extends org.codehaus.groovy.transform.ASTTransformation>,java.util.Set<org.codehaus.groovy.ast.ASTNode>> getTransforms(org.codehaus.groovy.control.CompilePhase)
meth public java.util.Map<java.lang.String,org.codehaus.groovy.ast.FieldNode> getFieldIndex()
meth public java.util.Map<java.lang.String,org.codehaus.groovy.ast.MethodNode> getDeclaredMethodsMap()
meth public java.util.Set<org.codehaus.groovy.ast.ClassNode> getAllInterfaces()
meth public org.codehaus.groovy.ast.ClassNode getComponentType()
meth public org.codehaus.groovy.ast.ClassNode getOuterClass()
meth public org.codehaus.groovy.ast.ClassNode getPlainNodeReference()
meth public org.codehaus.groovy.ast.ClassNode getSuperClass()
meth public org.codehaus.groovy.ast.ClassNode getUnresolvedSuperClass()
meth public org.codehaus.groovy.ast.ClassNode getUnresolvedSuperClass(boolean)
meth public org.codehaus.groovy.ast.ClassNode makeArray()
meth public org.codehaus.groovy.ast.ClassNode redirect()
meth public org.codehaus.groovy.ast.ClassNode[] getInterfaces()
meth public org.codehaus.groovy.ast.ClassNode[] getUnresolvedInterfaces()
meth public org.codehaus.groovy.ast.ClassNode[] getUnresolvedInterfaces(boolean)
meth public org.codehaus.groovy.ast.CompileUnit getCompileUnit()
meth public org.codehaus.groovy.ast.ConstructorNode addConstructor(int,org.codehaus.groovy.ast.Parameter[],org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.stmt.Statement)
meth public org.codehaus.groovy.ast.ConstructorNode getDeclaredConstructor(org.codehaus.groovy.ast.Parameter[])
meth public org.codehaus.groovy.ast.FieldNode addField(java.lang.String,int,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
meth public org.codehaus.groovy.ast.FieldNode addFieldFirst(java.lang.String,int,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
meth public org.codehaus.groovy.ast.FieldNode getDeclaredField(java.lang.String)
meth public org.codehaus.groovy.ast.FieldNode getField(java.lang.String)
meth public org.codehaus.groovy.ast.FieldNode getOuterField(java.lang.String)
meth public org.codehaus.groovy.ast.GenericsType[] getGenericsTypes()
meth public org.codehaus.groovy.ast.MethodNode addMethod(java.lang.String,int,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.Parameter[],org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.stmt.Statement)
meth public org.codehaus.groovy.ast.MethodNode addSyntheticMethod(java.lang.String,int,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.Parameter[],org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.stmt.Statement)
meth public org.codehaus.groovy.ast.MethodNode getDeclaredMethod(java.lang.String,org.codehaus.groovy.ast.Parameter[])
meth public org.codehaus.groovy.ast.MethodNode getEnclosingMethod()
meth public org.codehaus.groovy.ast.MethodNode getGetterMethod(java.lang.String)
meth public org.codehaus.groovy.ast.MethodNode getGetterMethod(java.lang.String,boolean)
meth public org.codehaus.groovy.ast.MethodNode getMethod(java.lang.String,org.codehaus.groovy.ast.Parameter[])
meth public org.codehaus.groovy.ast.MethodNode getSetterMethod(java.lang.String)
meth public org.codehaus.groovy.ast.MethodNode getSetterMethod(java.lang.String,boolean)
meth public org.codehaus.groovy.ast.MethodNode tryFindPossibleMethod(java.lang.String,org.codehaus.groovy.ast.expr.Expression)
meth public org.codehaus.groovy.ast.MixinNode[] getMixins()
meth public org.codehaus.groovy.ast.ModuleNode getModule()
meth public org.codehaus.groovy.ast.PackageNode getPackage()
meth public org.codehaus.groovy.ast.PropertyNode addProperty(java.lang.String,int,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.stmt.Statement,org.codehaus.groovy.ast.stmt.Statement)
meth public org.codehaus.groovy.ast.PropertyNode getProperty(java.lang.String)
meth public void addConstructor(org.codehaus.groovy.ast.ConstructorNode)
meth public void addField(org.codehaus.groovy.ast.FieldNode)
meth public void addFieldFirst(org.codehaus.groovy.ast.FieldNode)
meth public void addInterface(org.codehaus.groovy.ast.ClassNode)
meth public void addMethod(org.codehaus.groovy.ast.MethodNode)
meth public void addMixin(org.codehaus.groovy.ast.MixinNode)
meth public void addObjectInitializerStatements(org.codehaus.groovy.ast.stmt.Statement)
meth public void addProperty(org.codehaus.groovy.ast.PropertyNode)
meth public void addStaticInitializerStatements(java.util.List<org.codehaus.groovy.ast.stmt.Statement>,boolean)
meth public void addTransform(java.lang.Class<? extends org.codehaus.groovy.transform.ASTTransformation>,org.codehaus.groovy.ast.ASTNode)
meth public void positionStmtsAfterEnumInitStmts(java.util.List<org.codehaus.groovy.ast.stmt.Statement>)
meth public void removeConstructor(org.codehaus.groovy.ast.ConstructorNode)
meth public void removeField(java.lang.String)
meth public void removeMethod(org.codehaus.groovy.ast.MethodNode)
meth public void renameField(java.lang.String,java.lang.String)
meth public void setAnnotated(boolean)
meth public void setEnclosingMethod(org.codehaus.groovy.ast.MethodNode)
meth public void setGenericsPlaceHolder(boolean)
meth public void setGenericsTypes(org.codehaus.groovy.ast.GenericsType[])
meth public void setInterfaces(org.codehaus.groovy.ast.ClassNode[])
meth public void setModifiers(int)
meth public void setModule(org.codehaus.groovy.ast.ModuleNode)
meth public void setRedirect(org.codehaus.groovy.ast.ClassNode)
meth public void setScript(boolean)
meth public void setScriptBody(boolean)
meth public void setStaticClass(boolean)
meth public void setSuperClass(org.codehaus.groovy.ast.ClassNode)
meth public void setSyntheticPublic(boolean)
meth public void setUnresolvedSuperClass(org.codehaus.groovy.ast.ClassNode)
meth public void setUsingGenerics(boolean)
meth public void visitContents(org.codehaus.groovy.ast.GroovyClassVisitor)
supr org.codehaus.groovy.ast.AnnotatedNode
hfds annotated,compileUnit,componentType,constructors,enclosingMethod,fieldIndex,fields,genericsTypes,interfaces,lazyInitDone,methods,methodsList,mixins,modifiers,module,name,objectInitializers,placeholder,properties,redirect,script,scriptBody,staticClass,superClass,syntheticPublic,transformInstances,usesGenerics
hcls MapOfLists

CLSS public abstract org.codehaus.groovy.ast.CodeVisitorSupport
cons public init()
intf org.codehaus.groovy.ast.GroovyCodeVisitor
meth protected void visitEmptyStatement(org.codehaus.groovy.ast.stmt.EmptyStatement)
meth protected void visitListOfExpressions(java.util.List<? extends org.codehaus.groovy.ast.expr.Expression>)
meth public void visitArgumentlistExpression(org.codehaus.groovy.ast.expr.ArgumentListExpression)
meth public void visitArrayExpression(org.codehaus.groovy.ast.expr.ArrayExpression)
meth public void visitAssertStatement(org.codehaus.groovy.ast.stmt.AssertStatement)
meth public void visitAttributeExpression(org.codehaus.groovy.ast.expr.AttributeExpression)
meth public void visitBinaryExpression(org.codehaus.groovy.ast.expr.BinaryExpression)
meth public void visitBitwiseNegationExpression(org.codehaus.groovy.ast.expr.BitwiseNegationExpression)
meth public void visitBlockStatement(org.codehaus.groovy.ast.stmt.BlockStatement)
meth public void visitBooleanExpression(org.codehaus.groovy.ast.expr.BooleanExpression)
meth public void visitBreakStatement(org.codehaus.groovy.ast.stmt.BreakStatement)
meth public void visitBytecodeExpression(org.codehaus.groovy.classgen.BytecodeExpression)
meth public void visitCaseStatement(org.codehaus.groovy.ast.stmt.CaseStatement)
meth public void visitCastExpression(org.codehaus.groovy.ast.expr.CastExpression)
meth public void visitCatchStatement(org.codehaus.groovy.ast.stmt.CatchStatement)
meth public void visitClassExpression(org.codehaus.groovy.ast.expr.ClassExpression)
meth public void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public void visitClosureListExpression(org.codehaus.groovy.ast.expr.ClosureListExpression)
meth public void visitConstantExpression(org.codehaus.groovy.ast.expr.ConstantExpression)
meth public void visitConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth public void visitContinueStatement(org.codehaus.groovy.ast.stmt.ContinueStatement)
meth public void visitDeclarationExpression(org.codehaus.groovy.ast.expr.DeclarationExpression)
meth public void visitDoWhileLoop(org.codehaus.groovy.ast.stmt.DoWhileStatement)
meth public void visitExpressionStatement(org.codehaus.groovy.ast.stmt.ExpressionStatement)
meth public void visitFieldExpression(org.codehaus.groovy.ast.expr.FieldExpression)
meth public void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement)
meth public void visitGStringExpression(org.codehaus.groovy.ast.expr.GStringExpression)
meth public void visitIfElse(org.codehaus.groovy.ast.stmt.IfStatement)
meth public void visitListExpression(org.codehaus.groovy.ast.expr.ListExpression)
meth public void visitMapEntryExpression(org.codehaus.groovy.ast.expr.MapEntryExpression)
meth public void visitMapExpression(org.codehaus.groovy.ast.expr.MapExpression)
meth public void visitMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression)
meth public void visitMethodPointerExpression(org.codehaus.groovy.ast.expr.MethodPointerExpression)
meth public void visitNotExpression(org.codehaus.groovy.ast.expr.NotExpression)
meth public void visitPostfixExpression(org.codehaus.groovy.ast.expr.PostfixExpression)
meth public void visitPrefixExpression(org.codehaus.groovy.ast.expr.PrefixExpression)
meth public void visitPropertyExpression(org.codehaus.groovy.ast.expr.PropertyExpression)
meth public void visitRangeExpression(org.codehaus.groovy.ast.expr.RangeExpression)
meth public void visitReturnStatement(org.codehaus.groovy.ast.stmt.ReturnStatement)
meth public void visitShortTernaryExpression(org.codehaus.groovy.ast.expr.ElvisOperatorExpression)
meth public void visitSpreadExpression(org.codehaus.groovy.ast.expr.SpreadExpression)
meth public void visitSpreadMapExpression(org.codehaus.groovy.ast.expr.SpreadMapExpression)
meth public void visitStaticMethodCallExpression(org.codehaus.groovy.ast.expr.StaticMethodCallExpression)
meth public void visitSwitch(org.codehaus.groovy.ast.stmt.SwitchStatement)
meth public void visitSynchronizedStatement(org.codehaus.groovy.ast.stmt.SynchronizedStatement)
meth public void visitTernaryExpression(org.codehaus.groovy.ast.expr.TernaryExpression)
meth public void visitThrowStatement(org.codehaus.groovy.ast.stmt.ThrowStatement)
meth public void visitTryCatchFinally(org.codehaus.groovy.ast.stmt.TryCatchStatement)
meth public void visitTupleExpression(org.codehaus.groovy.ast.expr.TupleExpression)
meth public void visitUnaryMinusExpression(org.codehaus.groovy.ast.expr.UnaryMinusExpression)
meth public void visitUnaryPlusExpression(org.codehaus.groovy.ast.expr.UnaryPlusExpression)
meth public void visitVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
meth public void visitWhileLoop(org.codehaus.groovy.ast.stmt.WhileStatement)
supr java.lang.Object

CLSS public org.codehaus.groovy.ast.CompileUnit
cons public init(groovy.lang.GroovyClassLoader,java.security.CodeSource,org.codehaus.groovy.control.CompilerConfiguration)
cons public init(groovy.lang.GroovyClassLoader,org.codehaus.groovy.control.CompilerConfiguration)
meth public boolean hasClassNodeToCompile()
meth public groovy.lang.GroovyClassLoader getClassLoader()
meth public java.security.CodeSource getCodeSource()
meth public java.util.Iterator<java.lang.String> iterateClassNodeToCompile()
meth public java.util.List getClasses()
meth public java.util.List<org.codehaus.groovy.ast.ModuleNode> getModules()
meth public java.util.Map<java.lang.String,org.codehaus.groovy.ast.InnerClassNode> getGeneratedInnerClasses()
meth public org.codehaus.groovy.ast.ClassNode getClass(java.lang.String)
meth public org.codehaus.groovy.ast.InnerClassNode getGeneratedInnerClass(java.lang.String)
meth public org.codehaus.groovy.control.CompilerConfiguration getConfig()
meth public org.codehaus.groovy.control.SourceUnit getScriptSourceLocation(java.lang.String)
meth public void addClass(org.codehaus.groovy.ast.ClassNode)
meth public void addClassNodeToCompile(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.control.SourceUnit)
meth public void addGeneratedInnerClass(org.codehaus.groovy.ast.InnerClassNode)
meth public void addModule(org.codehaus.groovy.ast.ModuleNode)
supr java.lang.Object
hfds classLoader,classNameToSource,classes,classesToCompile,codeSource,config,generatedInnerClasses,modules

CLSS public org.codehaus.groovy.ast.ConstructorNode
cons public init(int,org.codehaus.groovy.ast.Parameter[],org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.stmt.Statement)
cons public init(int,org.codehaus.groovy.ast.stmt.Statement)
meth public boolean firstStatementIsSpecialConstructorCall()
supr org.codehaus.groovy.ast.MethodNode

CLSS public org.codehaus.groovy.ast.DynamicVariable
cons public init(java.lang.String,boolean)
intf org.codehaus.groovy.ast.Variable
meth public boolean hasInitialExpression()
meth public boolean isClosureSharedVariable()
meth public boolean isDynamicTyped()
meth public boolean isInStaticContext()
meth public int getModifiers()
meth public java.lang.String getName()
meth public org.codehaus.groovy.ast.ClassNode getOriginType()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public org.codehaus.groovy.ast.expr.Expression getInitialExpression()
meth public void setClosureSharedVariable(boolean)
supr java.lang.Object
hfds closureShare,name,staticContext

CLSS public org.codehaus.groovy.ast.EnumConstantClassNode
cons public init(org.codehaus.groovy.ast.ClassNode,java.lang.String,int,org.codehaus.groovy.ast.ClassNode)
supr org.codehaus.groovy.ast.InnerClassNode

CLSS public org.codehaus.groovy.ast.FieldNode
cons public init(java.lang.String,int,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
intf groovyjarjarasm.asm.Opcodes
intf org.codehaus.groovy.ast.Variable
meth public boolean hasInitialExpression()
meth public boolean isClosureSharedVariable()
 anno 0 java.lang.Deprecated()
meth public boolean isDynamicTyped()
meth public boolean isEnum()
meth public boolean isFinal()
meth public boolean isHolder()
meth public boolean isInStaticContext()
meth public boolean isPrivate()
meth public boolean isProtected()
meth public boolean isPublic()
meth public boolean isStatic()
meth public boolean isVolatile()
meth public int getModifiers()
meth public java.lang.String getName()
meth public org.codehaus.groovy.ast.ClassNode getOriginType()
meth public org.codehaus.groovy.ast.ClassNode getOwner()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public org.codehaus.groovy.ast.expr.Expression getInitialExpression()
meth public org.codehaus.groovy.ast.expr.Expression getInitialValueExpression()
meth public static org.codehaus.groovy.ast.FieldNode newStatic(java.lang.Class,java.lang.String) throws java.lang.NoSuchFieldException
meth public void rename(java.lang.String)
meth public void setClosureSharedVariable(boolean)
 anno 0 java.lang.Deprecated()
meth public void setHolder(boolean)
meth public void setInitialValueExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void setModifiers(int)
meth public void setOriginType(org.codehaus.groovy.ast.ClassNode)
meth public void setOwner(org.codehaus.groovy.ast.ClassNode)
meth public void setType(org.codehaus.groovy.ast.ClassNode)
supr org.codehaus.groovy.ast.AnnotatedNode
hfds dynamicTyped,holder,initialValueExpression,modifiers,name,originType,owner,type

CLSS public org.codehaus.groovy.ast.GenericsType
cons public init(org.codehaus.groovy.ast.ClassNode)
cons public init(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.ClassNode)
fld public final static org.codehaus.groovy.ast.GenericsType[] EMPTY_ARRAY
meth public boolean isCompatibleWith(org.codehaus.groovy.ast.ClassNode)
meth public boolean isPlaceholder()
meth public boolean isResolved()
meth public boolean isWildcard()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.ClassNode getLowerBound()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public org.codehaus.groovy.ast.ClassNode[] getUpperBounds()
meth public void setName(java.lang.String)
meth public void setPlaceholder(boolean)
meth public void setResolved(boolean)
meth public void setType(org.codehaus.groovy.ast.ClassNode)
meth public void setWildcard(boolean)
supr org.codehaus.groovy.ast.ASTNode
hfds lowerBound,name,placeholder,resolved,type,upperBounds,wildcard
hcls GenericsTypeMatcher

CLSS public abstract interface org.codehaus.groovy.ast.GroovyClassVisitor
meth public abstract void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public abstract void visitConstructor(org.codehaus.groovy.ast.ConstructorNode)
meth public abstract void visitField(org.codehaus.groovy.ast.FieldNode)
meth public abstract void visitMethod(org.codehaus.groovy.ast.MethodNode)
meth public abstract void visitProperty(org.codehaus.groovy.ast.PropertyNode)

CLSS public abstract interface org.codehaus.groovy.ast.GroovyCodeVisitor
meth public abstract void visitArgumentlistExpression(org.codehaus.groovy.ast.expr.ArgumentListExpression)
meth public abstract void visitArrayExpression(org.codehaus.groovy.ast.expr.ArrayExpression)
meth public abstract void visitAssertStatement(org.codehaus.groovy.ast.stmt.AssertStatement)
meth public abstract void visitAttributeExpression(org.codehaus.groovy.ast.expr.AttributeExpression)
meth public abstract void visitBinaryExpression(org.codehaus.groovy.ast.expr.BinaryExpression)
meth public abstract void visitBitwiseNegationExpression(org.codehaus.groovy.ast.expr.BitwiseNegationExpression)
meth public abstract void visitBlockStatement(org.codehaus.groovy.ast.stmt.BlockStatement)
meth public abstract void visitBooleanExpression(org.codehaus.groovy.ast.expr.BooleanExpression)
meth public abstract void visitBreakStatement(org.codehaus.groovy.ast.stmt.BreakStatement)
meth public abstract void visitBytecodeExpression(org.codehaus.groovy.classgen.BytecodeExpression)
meth public abstract void visitCaseStatement(org.codehaus.groovy.ast.stmt.CaseStatement)
meth public abstract void visitCastExpression(org.codehaus.groovy.ast.expr.CastExpression)
meth public abstract void visitCatchStatement(org.codehaus.groovy.ast.stmt.CatchStatement)
meth public abstract void visitClassExpression(org.codehaus.groovy.ast.expr.ClassExpression)
meth public abstract void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public abstract void visitClosureListExpression(org.codehaus.groovy.ast.expr.ClosureListExpression)
meth public abstract void visitConstantExpression(org.codehaus.groovy.ast.expr.ConstantExpression)
meth public abstract void visitConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth public abstract void visitContinueStatement(org.codehaus.groovy.ast.stmt.ContinueStatement)
meth public abstract void visitDeclarationExpression(org.codehaus.groovy.ast.expr.DeclarationExpression)
meth public abstract void visitDoWhileLoop(org.codehaus.groovy.ast.stmt.DoWhileStatement)
meth public abstract void visitExpressionStatement(org.codehaus.groovy.ast.stmt.ExpressionStatement)
meth public abstract void visitFieldExpression(org.codehaus.groovy.ast.expr.FieldExpression)
meth public abstract void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement)
meth public abstract void visitGStringExpression(org.codehaus.groovy.ast.expr.GStringExpression)
meth public abstract void visitIfElse(org.codehaus.groovy.ast.stmt.IfStatement)
meth public abstract void visitListExpression(org.codehaus.groovy.ast.expr.ListExpression)
meth public abstract void visitMapEntryExpression(org.codehaus.groovy.ast.expr.MapEntryExpression)
meth public abstract void visitMapExpression(org.codehaus.groovy.ast.expr.MapExpression)
meth public abstract void visitMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression)
meth public abstract void visitMethodPointerExpression(org.codehaus.groovy.ast.expr.MethodPointerExpression)
meth public abstract void visitNotExpression(org.codehaus.groovy.ast.expr.NotExpression)
meth public abstract void visitPostfixExpression(org.codehaus.groovy.ast.expr.PostfixExpression)
meth public abstract void visitPrefixExpression(org.codehaus.groovy.ast.expr.PrefixExpression)
meth public abstract void visitPropertyExpression(org.codehaus.groovy.ast.expr.PropertyExpression)
meth public abstract void visitRangeExpression(org.codehaus.groovy.ast.expr.RangeExpression)
meth public abstract void visitReturnStatement(org.codehaus.groovy.ast.stmt.ReturnStatement)
meth public abstract void visitShortTernaryExpression(org.codehaus.groovy.ast.expr.ElvisOperatorExpression)
meth public abstract void visitSpreadExpression(org.codehaus.groovy.ast.expr.SpreadExpression)
meth public abstract void visitSpreadMapExpression(org.codehaus.groovy.ast.expr.SpreadMapExpression)
meth public abstract void visitStaticMethodCallExpression(org.codehaus.groovy.ast.expr.StaticMethodCallExpression)
meth public abstract void visitSwitch(org.codehaus.groovy.ast.stmt.SwitchStatement)
meth public abstract void visitSynchronizedStatement(org.codehaus.groovy.ast.stmt.SynchronizedStatement)
meth public abstract void visitTernaryExpression(org.codehaus.groovy.ast.expr.TernaryExpression)
meth public abstract void visitThrowStatement(org.codehaus.groovy.ast.stmt.ThrowStatement)
meth public abstract void visitTryCatchFinally(org.codehaus.groovy.ast.stmt.TryCatchStatement)
meth public abstract void visitTupleExpression(org.codehaus.groovy.ast.expr.TupleExpression)
meth public abstract void visitUnaryMinusExpression(org.codehaus.groovy.ast.expr.UnaryMinusExpression)
meth public abstract void visitUnaryPlusExpression(org.codehaus.groovy.ast.expr.UnaryPlusExpression)
meth public abstract void visitVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
meth public abstract void visitWhileLoop(org.codehaus.groovy.ast.stmt.WhileStatement)

CLSS public org.codehaus.groovy.ast.ImportNode
cons public init(java.lang.String)
cons public init(org.codehaus.groovy.ast.ClassNode)
cons public init(org.codehaus.groovy.ast.ClassNode,java.lang.String)
cons public init(org.codehaus.groovy.ast.ClassNode,java.lang.String,java.lang.String)
intf groovyjarjarasm.asm.Opcodes
meth public boolean isStar()
meth public boolean isStatic()
meth public java.lang.String getAlias()
meth public java.lang.String getClassName()
meth public java.lang.String getFieldName()
meth public java.lang.String getPackageName()
meth public java.lang.String getText()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.AnnotatedNode
hfds alias,fieldName,isStar,isStatic,packageName,type

CLSS public org.codehaus.groovy.ast.InnerClassNode
cons public init(org.codehaus.groovy.ast.ClassNode,java.lang.String,int,org.codehaus.groovy.ast.ClassNode)
cons public init(org.codehaus.groovy.ast.ClassNode,java.lang.String,int,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.MixinNode[])
meth public boolean isAnonymous()
meth public org.codehaus.groovy.ast.ClassNode getOuterClass()
meth public org.codehaus.groovy.ast.ClassNode getOuterMostClass()
meth public org.codehaus.groovy.ast.ConstructorNode addConstructor(int,org.codehaus.groovy.ast.Parameter[],org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.stmt.Statement)
meth public org.codehaus.groovy.ast.FieldNode getOuterField(java.lang.String)
meth public org.codehaus.groovy.ast.VariableScope getVariableScope()
meth public void addConstructor(org.codehaus.groovy.ast.ConstructorNode)
meth public void setAnonymous(boolean)
meth public void setVariableScope(org.codehaus.groovy.ast.VariableScope)
supr org.codehaus.groovy.ast.ClassNode
hfds anonymous,outerClass,scope

CLSS public org.codehaus.groovy.ast.InterfaceHelperClassNode
cons public init(org.codehaus.groovy.ast.ClassNode,java.lang.String,int,org.codehaus.groovy.ast.ClassNode,java.util.List<java.lang.String>)
meth public java.util.List<java.lang.String> getCallSites()
meth public void setCallSites(java.util.List<java.lang.String>)
supr org.codehaus.groovy.ast.InnerClassNode
hfds callSites

CLSS public abstract org.codehaus.groovy.ast.MethodCallTransformation
cons public init()
intf org.codehaus.groovy.transform.ASTTransformation
meth protected abstract org.codehaus.groovy.ast.GroovyCodeVisitor getTransformer(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr java.lang.Object

CLSS public abstract org.codehaus.groovy.ast.MethodInvocationTrap
cons public init(org.codehaus.groovy.control.io.ReaderSource,org.codehaus.groovy.control.SourceUnit)
fld protected final org.codehaus.groovy.control.SourceUnit sourceUnit
fld protected final org.codehaus.groovy.control.io.ReaderSource source
meth protected abstract boolean handleTargetMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression)
meth protected abstract boolean isBuildInvocation(org.codehaus.groovy.ast.expr.MethodCallExpression)
meth protected java.lang.String convertClosureToSource(org.codehaus.groovy.ast.expr.ClosureExpression)
meth protected void addError(java.lang.String,org.codehaus.groovy.ast.ASTNode)
meth public void visitMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression)
supr org.codehaus.groovy.ast.CodeVisitorSupport

CLSS public org.codehaus.groovy.ast.MethodNode
cons public init(java.lang.String,int,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.Parameter[],org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.stmt.Statement)
fld public final static java.lang.String SCRIPT_BODY_METHOD_KEY = "org.codehaus.groovy.ast.MethodNode.isScriptBody"
intf groovyjarjarasm.asm.Opcodes
meth public boolean hasAnnotationDefault()
meth public boolean hasDefaultValue()
meth public boolean isAbstract()
meth public boolean isDynamicReturnType()
meth public boolean isFinal()
meth public boolean isPackageScope()
meth public boolean isPrivate()
meth public boolean isProtected()
meth public boolean isPublic()
meth public boolean isScriptBody()
meth public boolean isStatic()
meth public boolean isStaticConstructor()
meth public boolean isSyntheticPublic()
meth public boolean isVoidMethod()
meth public int getModifiers()
meth public java.lang.String getName()
meth public java.lang.String getText()
meth public java.lang.String getTypeDescriptor()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.ClassNode getReturnType()
meth public org.codehaus.groovy.ast.ClassNode[] getExceptions()
meth public org.codehaus.groovy.ast.GenericsType[] getGenericsTypes()
meth public org.codehaus.groovy.ast.Parameter[] getParameters()
meth public org.codehaus.groovy.ast.VariableScope getVariableScope()
meth public org.codehaus.groovy.ast.stmt.Statement getCode()
meth public org.codehaus.groovy.ast.stmt.Statement getFirstStatement()
meth public void setAnnotationDefault(boolean)
meth public void setCode(org.codehaus.groovy.ast.stmt.Statement)
meth public void setGenericsTypes(org.codehaus.groovy.ast.GenericsType[])
meth public void setIsScriptBody()
meth public void setModifiers(int)
meth public void setParameters(org.codehaus.groovy.ast.Parameter[])
meth public void setReturnType(org.codehaus.groovy.ast.ClassNode)
meth public void setSyntheticPublic(boolean)
meth public void setVariableScope(org.codehaus.groovy.ast.VariableScope)
supr org.codehaus.groovy.ast.AnnotatedNode
hfds code,dynamicReturnType,exceptions,genericsTypes,hasDefault,hasDefaultValue,modifiers,name,parameters,returnType,staticConstructor,syntheticPublic,typeDescriptor,variableScope

CLSS public org.codehaus.groovy.ast.MixinASTTransformation
 anno 0 java.lang.Deprecated()
cons public init()
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds MY_TYPE

CLSS public org.codehaus.groovy.ast.MixinNode
cons public init(java.lang.String,int,org.codehaus.groovy.ast.ClassNode)
cons public init(java.lang.String,int,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode[])
fld public final static org.codehaus.groovy.ast.MixinNode[] EMPTY_ARRAY
supr org.codehaus.groovy.ast.ClassNode

CLSS public org.codehaus.groovy.ast.ModuleNode
cons public init(org.codehaus.groovy.ast.CompileUnit)
cons public init(org.codehaus.groovy.control.SourceUnit)
intf groovyjarjarasm.asm.Opcodes
meth protected java.lang.String extractClassFromFileDescription()
meth protected org.codehaus.groovy.ast.ClassNode createStatementsClass()
meth public boolean hasImportsResolved()
meth public boolean hasPackage()
meth public boolean hasPackageName()
meth public boolean isEmpty()
meth public java.lang.String getDescription()
meth public java.lang.String getMainClassName()
meth public java.lang.String getPackageName()
meth public java.util.List<org.codehaus.groovy.ast.ClassNode> getClasses()
meth public java.util.List<org.codehaus.groovy.ast.ImportNode> getImports()
meth public java.util.List<org.codehaus.groovy.ast.ImportNode> getStarImports()
meth public java.util.List<org.codehaus.groovy.ast.MethodNode> getMethods()
meth public java.util.Map<java.lang.String,org.codehaus.groovy.ast.ImportNode> getStaticImports()
meth public java.util.Map<java.lang.String,org.codehaus.groovy.ast.ImportNode> getStaticStarImports()
meth public org.codehaus.groovy.ast.ClassNode getImportType(java.lang.String)
meth public org.codehaus.groovy.ast.ClassNode getScriptClassDummy()
meth public org.codehaus.groovy.ast.CompileUnit getUnit()
meth public org.codehaus.groovy.ast.ImportNode getImport(java.lang.String)
meth public org.codehaus.groovy.ast.PackageNode getPackage()
meth public org.codehaus.groovy.ast.stmt.BlockStatement getStatementBlock()
meth public org.codehaus.groovy.control.SourceUnit getContext()
meth public void addClass(org.codehaus.groovy.ast.ClassNode)
meth public void addImport(java.lang.String,org.codehaus.groovy.ast.ClassNode)
meth public void addImport(java.lang.String,org.codehaus.groovy.ast.ClassNode,java.util.List<org.codehaus.groovy.ast.AnnotationNode>)
meth public void addMethod(org.codehaus.groovy.ast.MethodNode)
meth public void addStarImport(java.lang.String)
meth public void addStarImport(java.lang.String,java.util.List<org.codehaus.groovy.ast.AnnotationNode>)
meth public void addStatement(org.codehaus.groovy.ast.stmt.Statement)
meth public void addStaticImport(org.codehaus.groovy.ast.ClassNode,java.lang.String,java.lang.String)
meth public void addStaticImport(org.codehaus.groovy.ast.ClassNode,java.lang.String,java.lang.String,java.util.List<org.codehaus.groovy.ast.AnnotationNode>)
meth public void addStaticStarImport(java.lang.String,org.codehaus.groovy.ast.ClassNode)
meth public void addStaticStarImport(java.lang.String,org.codehaus.groovy.ast.ClassNode,java.util.List<org.codehaus.groovy.ast.AnnotationNode>)
meth public void setDescription(java.lang.String)
meth public void setImportsResolved(boolean)
meth public void setPackage(org.codehaus.groovy.ast.PackageNode)
meth public void setPackageName(java.lang.String)
meth public void sortClasses()
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.ASTNode
hfds SCRIPT_CONTEXT_CTOR,classes,context,createClassForStatements,description,imports,importsResolved,mainClassName,methods,packageNode,scriptDummy,starImports,statementBlock,staticImports,staticStarImports,unit

CLSS public org.codehaus.groovy.ast.PackageNode
cons public init(java.lang.String)
meth public java.lang.String getName()
meth public java.lang.String getText()
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.AnnotatedNode
hfds name

CLSS public org.codehaus.groovy.ast.Parameter
cons public init(org.codehaus.groovy.ast.ClassNode,java.lang.String)
cons public init(org.codehaus.groovy.ast.ClassNode,java.lang.String,org.codehaus.groovy.ast.expr.Expression)
fld public final static org.codehaus.groovy.ast.Parameter[] EMPTY_ARRAY
intf org.codehaus.groovy.ast.Variable
meth public boolean hasInitialExpression()
meth public boolean isClosureSharedVariable()
meth public boolean isDynamicTyped()
meth public boolean isInStaticContext()
meth public int getModifiers()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.ClassNode getOriginType()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public org.codehaus.groovy.ast.expr.Expression getInitialExpression()
meth public void setClosureSharedVariable(boolean)
meth public void setInStaticContext(boolean)
meth public void setInitialExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void setModifiers(int)
meth public void setOriginType(org.codehaus.groovy.ast.ClassNode)
meth public void setType(org.codehaus.groovy.ast.ClassNode)
supr org.codehaus.groovy.ast.AnnotatedNode
hfds closureShare,defaultValue,dynamicTyped,hasDefaultValue,inStaticContext,modifiers,name,originType,type

CLSS public org.codehaus.groovy.ast.PropertyNode
cons public init(java.lang.String,int,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.stmt.Statement,org.codehaus.groovy.ast.stmt.Statement)
cons public init(org.codehaus.groovy.ast.FieldNode,int,org.codehaus.groovy.ast.stmt.Statement,org.codehaus.groovy.ast.stmt.Statement)
intf groovyjarjarasm.asm.Opcodes
intf org.codehaus.groovy.ast.Variable
meth public boolean hasInitialExpression()
meth public boolean isClosureSharedVariable()
meth public boolean isDynamicTyped()
meth public boolean isInStaticContext()
meth public boolean isPrivate()
meth public boolean isPublic()
meth public boolean isStatic()
meth public int getModifiers()
meth public java.lang.String getName()
meth public org.codehaus.groovy.ast.ClassNode getOriginType()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public org.codehaus.groovy.ast.FieldNode getField()
meth public org.codehaus.groovy.ast.expr.Expression getInitialExpression()
meth public org.codehaus.groovy.ast.stmt.Statement getGetterBlock()
meth public org.codehaus.groovy.ast.stmt.Statement getSetterBlock()
meth public void setClosureSharedVariable(boolean)
 anno 0 java.lang.Deprecated()
meth public void setField(org.codehaus.groovy.ast.FieldNode)
meth public void setGetterBlock(org.codehaus.groovy.ast.stmt.Statement)
meth public void setSetterBlock(org.codehaus.groovy.ast.stmt.Statement)
meth public void setType(org.codehaus.groovy.ast.ClassNode)
supr org.codehaus.groovy.ast.AnnotatedNode
hfds field,getterBlock,modifiers,setterBlock

CLSS public org.codehaus.groovy.ast.TransformingCodeVisitor
cons public init(org.codehaus.groovy.ast.ClassCodeExpressionTransformer)
meth public void visitArgumentlistExpression(org.codehaus.groovy.ast.expr.ArgumentListExpression)
meth public void visitArrayExpression(org.codehaus.groovy.ast.expr.ArrayExpression)
meth public void visitAssertStatement(org.codehaus.groovy.ast.stmt.AssertStatement)
meth public void visitAttributeExpression(org.codehaus.groovy.ast.expr.AttributeExpression)
meth public void visitBinaryExpression(org.codehaus.groovy.ast.expr.BinaryExpression)
meth public void visitBitwiseNegationExpression(org.codehaus.groovy.ast.expr.BitwiseNegationExpression)
meth public void visitBlockStatement(org.codehaus.groovy.ast.stmt.BlockStatement)
meth public void visitBooleanExpression(org.codehaus.groovy.ast.expr.BooleanExpression)
meth public void visitBreakStatement(org.codehaus.groovy.ast.stmt.BreakStatement)
meth public void visitBytecodeExpression(org.codehaus.groovy.classgen.BytecodeExpression)
meth public void visitCaseStatement(org.codehaus.groovy.ast.stmt.CaseStatement)
meth public void visitCastExpression(org.codehaus.groovy.ast.expr.CastExpression)
meth public void visitCatchStatement(org.codehaus.groovy.ast.stmt.CatchStatement)
meth public void visitClassExpression(org.codehaus.groovy.ast.expr.ClassExpression)
meth public void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public void visitClosureListExpression(org.codehaus.groovy.ast.expr.ClosureListExpression)
meth public void visitConstantExpression(org.codehaus.groovy.ast.expr.ConstantExpression)
meth public void visitContinueStatement(org.codehaus.groovy.ast.stmt.ContinueStatement)
meth public void visitDeclarationExpression(org.codehaus.groovy.ast.expr.DeclarationExpression)
meth public void visitDoWhileLoop(org.codehaus.groovy.ast.stmt.DoWhileStatement)
meth public void visitExpressionStatement(org.codehaus.groovy.ast.stmt.ExpressionStatement)
meth public void visitFieldExpression(org.codehaus.groovy.ast.expr.FieldExpression)
meth public void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement)
meth public void visitGStringExpression(org.codehaus.groovy.ast.expr.GStringExpression)
meth public void visitIfElse(org.codehaus.groovy.ast.stmt.IfStatement)
meth public void visitListExpression(org.codehaus.groovy.ast.expr.ListExpression)
meth public void visitMapEntryExpression(org.codehaus.groovy.ast.expr.MapEntryExpression)
meth public void visitMapExpression(org.codehaus.groovy.ast.expr.MapExpression)
meth public void visitMethodPointerExpression(org.codehaus.groovy.ast.expr.MethodPointerExpression)
meth public void visitNotExpression(org.codehaus.groovy.ast.expr.NotExpression)
meth public void visitPostfixExpression(org.codehaus.groovy.ast.expr.PostfixExpression)
meth public void visitPrefixExpression(org.codehaus.groovy.ast.expr.PrefixExpression)
meth public void visitPropertyExpression(org.codehaus.groovy.ast.expr.PropertyExpression)
meth public void visitRangeExpression(org.codehaus.groovy.ast.expr.RangeExpression)
meth public void visitReturnStatement(org.codehaus.groovy.ast.stmt.ReturnStatement)
meth public void visitShortTernaryExpression(org.codehaus.groovy.ast.expr.ElvisOperatorExpression)
meth public void visitSpreadExpression(org.codehaus.groovy.ast.expr.SpreadExpression)
meth public void visitSpreadMapExpression(org.codehaus.groovy.ast.expr.SpreadMapExpression)
meth public void visitStaticMethodCallExpression(org.codehaus.groovy.ast.expr.StaticMethodCallExpression)
meth public void visitSwitch(org.codehaus.groovy.ast.stmt.SwitchStatement)
meth public void visitSynchronizedStatement(org.codehaus.groovy.ast.stmt.SynchronizedStatement)
meth public void visitTernaryExpression(org.codehaus.groovy.ast.expr.TernaryExpression)
meth public void visitThrowStatement(org.codehaus.groovy.ast.stmt.ThrowStatement)
meth public void visitTryCatchFinally(org.codehaus.groovy.ast.stmt.TryCatchStatement)
meth public void visitTupleExpression(org.codehaus.groovy.ast.expr.TupleExpression)
meth public void visitUnaryMinusExpression(org.codehaus.groovy.ast.expr.UnaryMinusExpression)
meth public void visitUnaryPlusExpression(org.codehaus.groovy.ast.expr.UnaryPlusExpression)
meth public void visitVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
meth public void visitWhileLoop(org.codehaus.groovy.ast.stmt.WhileStatement)
supr org.codehaus.groovy.ast.CodeVisitorSupport
hfds trn

CLSS public abstract interface org.codehaus.groovy.ast.Variable
meth public abstract boolean hasInitialExpression()
meth public abstract boolean isClosureSharedVariable()
meth public abstract boolean isDynamicTyped()
meth public abstract boolean isInStaticContext()
meth public abstract int getModifiers()
meth public abstract java.lang.String getName()
meth public abstract org.codehaus.groovy.ast.ClassNode getOriginType()
meth public abstract org.codehaus.groovy.ast.ClassNode getType()
meth public abstract org.codehaus.groovy.ast.expr.Expression getInitialExpression()
meth public abstract void setClosureSharedVariable(boolean)

CLSS public org.codehaus.groovy.ast.VariableScope
cons public init()
cons public init(org.codehaus.groovy.ast.VariableScope)
meth public boolean isClassScope()
meth public boolean isInStaticContext()
meth public boolean isReferencedClassVariable(java.lang.String)
meth public boolean isReferencedLocalVariable(java.lang.String)
meth public boolean isRoot()
meth public int getReferencedLocalVariablesCount()
meth public java.lang.Object removeReferencedClassVariable(java.lang.String)
meth public java.util.Iterator<org.codehaus.groovy.ast.Variable> getDeclaredVariablesIterator()
meth public java.util.Iterator<org.codehaus.groovy.ast.Variable> getReferencedClassVariablesIterator()
meth public java.util.Iterator<org.codehaus.groovy.ast.Variable> getReferencedLocalVariablesIterator()
meth public java.util.Map<java.lang.String,org.codehaus.groovy.ast.Variable> getDeclaredVariables()
meth public java.util.Map<java.lang.String,org.codehaus.groovy.ast.Variable> getReferencedClassVariables()
meth public org.codehaus.groovy.ast.ClassNode getClassScope()
meth public org.codehaus.groovy.ast.Variable getDeclaredVariable(java.lang.String)
meth public org.codehaus.groovy.ast.Variable getReferencedClassVariable(java.lang.String)
meth public org.codehaus.groovy.ast.Variable getReferencedLocalVariable(java.lang.String)
meth public org.codehaus.groovy.ast.VariableScope copy()
meth public org.codehaus.groovy.ast.VariableScope getParent()
meth public void putDeclaredVariable(org.codehaus.groovy.ast.Variable)
meth public void putReferencedClassVariable(org.codehaus.groovy.ast.Variable)
meth public void putReferencedLocalVariable(org.codehaus.groovy.ast.Variable)
meth public void setClassScope(org.codehaus.groovy.ast.ClassNode)
meth public void setInStaticContext(boolean)
supr java.lang.Object
hfds clazzScope,declaredVariables,inStaticContext,parent,referencedClassVariables,referencedLocalVariables,resolvesDynamic

CLSS public org.codehaus.groovy.ast.expr.AnnotationConstantExpression
cons public init(org.codehaus.groovy.ast.AnnotationNode)
meth public java.lang.String toString()
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.ConstantExpression

CLSS public org.codehaus.groovy.ast.expr.ArgumentListExpression
cons public init()
cons public init(java.util.List<org.codehaus.groovy.ast.expr.Expression>)
cons public init(org.codehaus.groovy.ast.Parameter[])
cons public init(org.codehaus.groovy.ast.expr.Expression)
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
cons public init(org.codehaus.groovy.ast.expr.Expression[])
fld public final static java.lang.Object[] EMPTY_ARRAY
fld public final static org.codehaus.groovy.ast.expr.ArgumentListExpression EMPTY_ARGUMENTS
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.TupleExpression

CLSS public org.codehaus.groovy.ast.expr.ArrayExpression
cons public init(org.codehaus.groovy.ast.ClassNode,java.util.List<org.codehaus.groovy.ast.expr.Expression>)
cons public init(org.codehaus.groovy.ast.ClassNode,java.util.List<org.codehaus.groovy.ast.expr.Expression>,java.util.List<org.codehaus.groovy.ast.expr.Expression>)
meth public boolean isDynamic()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public java.util.List<org.codehaus.groovy.ast.expr.Expression> getExpressions()
meth public java.util.List<org.codehaus.groovy.ast.expr.Expression> getSizeExpression()
meth public org.codehaus.groovy.ast.ClassNode getElementType()
meth public org.codehaus.groovy.ast.expr.Expression getExpression(int)
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void addExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds elementType,expressions,sizeExpression

CLSS public org.codehaus.groovy.ast.expr.AttributeExpression
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression,boolean)
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.PropertyExpression

CLSS public org.codehaus.groovy.ast.expr.BinaryExpression
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.syntax.Token,org.codehaus.groovy.ast.expr.Expression)
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.expr.Expression getLeftExpression()
meth public org.codehaus.groovy.ast.expr.Expression getRightExpression()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public org.codehaus.groovy.syntax.Token getOperation()
meth public static org.codehaus.groovy.ast.expr.BinaryExpression newAssignmentExpression(org.codehaus.groovy.ast.Variable,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BinaryExpression newInitializationExpression(java.lang.String,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
meth public void setLeftExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void setRightExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds leftExpression,operation,rightExpression

CLSS public org.codehaus.groovy.ast.expr.BitwiseNegationExpression
cons public init(org.codehaus.groovy.ast.expr.Expression)
meth public java.lang.String getText()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public org.codehaus.groovy.ast.expr.Expression getExpression()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds expression

CLSS public org.codehaus.groovy.ast.expr.BooleanExpression
cons public init(org.codehaus.groovy.ast.expr.Expression)
meth public java.lang.String getText()
meth public org.codehaus.groovy.ast.expr.Expression getExpression()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds expression

CLSS public org.codehaus.groovy.ast.expr.CastExpression
cons public init(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
cons public init(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression,boolean)
meth public boolean isCoerce()
meth public boolean isIgnoringAutoboxing()
meth public boolean isStrict()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.expr.Expression getExpression()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public static org.codehaus.groovy.ast.expr.CastExpression asExpression(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
meth public void setCoerce(boolean)
meth public void setStrict(boolean)
meth public void setType(org.codehaus.groovy.ast.ClassNode)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds coerce,expression,ignoreAutoboxing,strict

CLSS public org.codehaus.groovy.ast.expr.ClassExpression
cons public init(org.codehaus.groovy.ast.ClassNode)
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression

CLSS public org.codehaus.groovy.ast.expr.ClosureExpression
cons public init(org.codehaus.groovy.ast.Parameter[],org.codehaus.groovy.ast.stmt.Statement)
meth public boolean isParameterSpecified()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.Parameter[] getParameters()
meth public org.codehaus.groovy.ast.VariableScope getVariableScope()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public org.codehaus.groovy.ast.stmt.Statement getCode()
meth public void setCode(org.codehaus.groovy.ast.stmt.Statement)
meth public void setVariableScope(org.codehaus.groovy.ast.VariableScope)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds code,parameters,variableScope

CLSS public org.codehaus.groovy.ast.expr.ClosureListExpression
cons public init()
cons public init(java.util.List<org.codehaus.groovy.ast.expr.Expression>)
meth public java.lang.String getText()
meth public org.codehaus.groovy.ast.VariableScope getVariableScope()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void setVariableScope(org.codehaus.groovy.ast.VariableScope)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.ListExpression
hfds scope

CLSS public org.codehaus.groovy.ast.expr.ConstantExpression
cons public init(java.lang.Object)
cons public init(java.lang.Object,boolean)
fld public final static org.codehaus.groovy.ast.expr.ConstantExpression EMPTY_EXPRESSION
fld public final static org.codehaus.groovy.ast.expr.ConstantExpression EMPTY_STRING
fld public final static org.codehaus.groovy.ast.expr.ConstantExpression FALSE
fld public final static org.codehaus.groovy.ast.expr.ConstantExpression NULL
fld public final static org.codehaus.groovy.ast.expr.ConstantExpression PRIM_FALSE
fld public final static org.codehaus.groovy.ast.expr.ConstantExpression PRIM_TRUE
fld public final static org.codehaus.groovy.ast.expr.ConstantExpression TRUE
fld public final static org.codehaus.groovy.ast.expr.ConstantExpression VOID
meth public boolean isEmptyStringExpression()
meth public boolean isFalseExpression()
meth public boolean isNullExpression()
meth public boolean isTrueExpression()
meth public java.lang.Object getValue()
meth public java.lang.String getConstantName()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void setConstantName(java.lang.String)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds constantName,value

CLSS public org.codehaus.groovy.ast.expr.ConstructorCallExpression
cons public init(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
intf org.codehaus.groovy.ast.expr.MethodCall
meth public boolean isSpecialCall()
meth public boolean isSuperCall()
meth public boolean isThisCall()
meth public boolean isUsingAnonymousInnerClass()
meth public java.lang.String getMethodAsString()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.ASTNode getReceiver()
meth public org.codehaus.groovy.ast.expr.Expression getArguments()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void setUsingAnonymousInnerClass(boolean)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds arguments,usesAnonymousInnerClass

CLSS public org.codehaus.groovy.ast.expr.DeclarationExpression
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.syntax.Token,org.codehaus.groovy.ast.expr.Expression)
cons public init(org.codehaus.groovy.ast.expr.VariableExpression,org.codehaus.groovy.syntax.Token,org.codehaus.groovy.ast.expr.Expression)
meth public boolean isMultipleAssignmentDeclaration()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public org.codehaus.groovy.ast.expr.TupleExpression getTupleExpression()
meth public org.codehaus.groovy.ast.expr.VariableExpression getVariableExpression()
meth public void setLeftExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void setRightExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.BinaryExpression

CLSS public org.codehaus.groovy.ast.expr.ElvisOperatorExpression
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.TernaryExpression

CLSS public org.codehaus.groovy.ast.expr.EmptyExpression
cons public init()
fld public final static org.codehaus.groovy.ast.expr.EmptyExpression INSTANCE
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression

CLSS public abstract org.codehaus.groovy.ast.expr.Expression
cons public init()
meth protected <%0 extends org.codehaus.groovy.ast.expr.Expression> java.util.List<{%%0}> transformExpressions(java.util.List<? extends org.codehaus.groovy.ast.expr.Expression>,org.codehaus.groovy.ast.expr.ExpressionTransformer,java.lang.Class<{%%0}>)
meth protected java.util.List<org.codehaus.groovy.ast.expr.Expression> transformExpressions(java.util.List<? extends org.codehaus.groovy.ast.expr.Expression>,org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public abstract org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public void setType(org.codehaus.groovy.ast.ClassNode)
supr org.codehaus.groovy.ast.AnnotatedNode
hfds type

CLSS public abstract interface org.codehaus.groovy.ast.expr.ExpressionTransformer
meth public abstract org.codehaus.groovy.ast.expr.Expression transform(org.codehaus.groovy.ast.expr.Expression)

CLSS public org.codehaus.groovy.ast.expr.FieldExpression
cons public init(org.codehaus.groovy.ast.FieldNode)
meth public boolean isDynamicTyped()
meth public boolean isUseReferenceDirectly()
meth public java.lang.String getFieldName()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public org.codehaus.groovy.ast.FieldNode getField()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void setType(org.codehaus.groovy.ast.ClassNode)
meth public void setUseReferenceDirectly(boolean)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds field,useRef

CLSS public org.codehaus.groovy.ast.expr.GStringExpression
cons public init(java.lang.String)
cons public init(java.lang.String,java.util.List<org.codehaus.groovy.ast.expr.ConstantExpression>,java.util.List<org.codehaus.groovy.ast.expr.Expression>)
meth public boolean isConstantString()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public java.util.List<org.codehaus.groovy.ast.expr.ConstantExpression> getStrings()
meth public java.util.List<org.codehaus.groovy.ast.expr.Expression> getValues()
meth public org.codehaus.groovy.ast.expr.Expression asConstantString()
meth public org.codehaus.groovy.ast.expr.Expression getValue(int)
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void addString(org.codehaus.groovy.ast.expr.ConstantExpression)
meth public void addValue(org.codehaus.groovy.ast.expr.Expression)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds strings,values,verbatimText

CLSS public org.codehaus.groovy.ast.expr.ListExpression
cons public init()
cons public init(java.util.List<org.codehaus.groovy.ast.expr.Expression>)
meth public boolean isWrapped()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public java.util.List<org.codehaus.groovy.ast.expr.Expression> getExpressions()
meth public org.codehaus.groovy.ast.expr.Expression getExpression(int)
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void addExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void setWrapped(boolean)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds expressions,wrapped

CLSS public org.codehaus.groovy.ast.expr.MapEntryExpression
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.expr.Expression getKeyExpression()
meth public org.codehaus.groovy.ast.expr.Expression getValueExpression()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void setKeyExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void setValueExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds keyExpression,valueExpression

CLSS public org.codehaus.groovy.ast.expr.MapExpression
cons public init()
cons public init(java.util.List<org.codehaus.groovy.ast.expr.MapEntryExpression>)
meth public boolean isDynamic()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public java.util.List<org.codehaus.groovy.ast.expr.MapEntryExpression> getMapEntryExpressions()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void addMapEntryExpression(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public void addMapEntryExpression(org.codehaus.groovy.ast.expr.MapEntryExpression)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds mapEntryExpressions

CLSS public abstract interface org.codehaus.groovy.ast.expr.MethodCall
meth public abstract java.lang.String getMethodAsString()
meth public abstract java.lang.String getText()
meth public abstract org.codehaus.groovy.ast.ASTNode getReceiver()
meth public abstract org.codehaus.groovy.ast.expr.Expression getArguments()

CLSS public org.codehaus.groovy.ast.expr.MethodCallExpression
cons public init(org.codehaus.groovy.ast.expr.Expression,java.lang.String,org.codehaus.groovy.ast.expr.Expression)
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
fld public final static org.codehaus.groovy.ast.expr.Expression NO_ARGUMENTS
intf org.codehaus.groovy.ast.expr.MethodCall
meth public boolean isImplicitThis()
meth public boolean isSafe()
meth public boolean isSpreadSafe()
meth public boolean isUsingGenerics()
meth public java.lang.String getMethodAsString()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.ASTNode getReceiver()
meth public org.codehaus.groovy.ast.GenericsType[] getGenericsTypes()
meth public org.codehaus.groovy.ast.MethodNode getMethodTarget()
meth public org.codehaus.groovy.ast.expr.Expression getArguments()
meth public org.codehaus.groovy.ast.expr.Expression getMethod()
meth public org.codehaus.groovy.ast.expr.Expression getObjectExpression()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void setArguments(org.codehaus.groovy.ast.expr.Expression)
meth public void setGenericsTypes(org.codehaus.groovy.ast.GenericsType[])
meth public void setImplicitThis(boolean)
meth public void setMethod(org.codehaus.groovy.ast.expr.Expression)
meth public void setMethodTarget(org.codehaus.groovy.ast.MethodNode)
meth public void setObjectExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void setSafe(boolean)
meth public void setSpreadSafe(boolean)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds arguments,genericsTypes,implicitThis,method,objectExpression,safe,spreadSafe,target,usesGenerics

CLSS public org.codehaus.groovy.ast.expr.MethodPointerExpression
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public boolean isDynamic()
meth public java.lang.Class getTypeClass()
meth public java.lang.String getText()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public org.codehaus.groovy.ast.expr.Expression getExpression()
meth public org.codehaus.groovy.ast.expr.Expression getMethodName()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds expression,methodName

CLSS public org.codehaus.groovy.ast.expr.NamedArgumentListExpression
cons public init()
cons public init(java.util.List<org.codehaus.groovy.ast.expr.MapEntryExpression>)
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
supr org.codehaus.groovy.ast.expr.MapExpression

CLSS public org.codehaus.groovy.ast.expr.NotExpression
cons public init(org.codehaus.groovy.ast.expr.Expression)
meth public boolean isDynamic()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.BooleanExpression

CLSS public org.codehaus.groovy.ast.expr.PostfixExpression
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.syntax.Token)
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public org.codehaus.groovy.ast.expr.Expression getExpression()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public org.codehaus.groovy.syntax.Token getOperation()
meth public void setExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds expression,operation

CLSS public org.codehaus.groovy.ast.expr.PrefixExpression
cons public init(org.codehaus.groovy.syntax.Token,org.codehaus.groovy.ast.expr.Expression)
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public org.codehaus.groovy.ast.expr.Expression getExpression()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public org.codehaus.groovy.syntax.Token getOperation()
meth public void setExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds expression,operation

CLSS public org.codehaus.groovy.ast.expr.PropertyExpression
cons public init(org.codehaus.groovy.ast.expr.Expression,java.lang.String)
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression,boolean)
meth public boolean isDynamic()
meth public boolean isImplicitThis()
meth public boolean isSafe()
meth public boolean isSpreadSafe()
meth public boolean isStatic()
meth public java.lang.String getPropertyAsString()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.expr.Expression getObjectExpression()
meth public org.codehaus.groovy.ast.expr.Expression getProperty()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void setImplicitThis(boolean)
meth public void setObjectExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void setSpreadSafe(boolean)
meth public void setStatic(boolean)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds implicitThis,isStatic,objectExpression,property,safe,spreadSafe

CLSS public org.codehaus.groovy.ast.expr.RangeExpression
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression,boolean)
meth public boolean isInclusive()
meth public java.lang.String getText()
meth public org.codehaus.groovy.ast.expr.Expression getFrom()
meth public org.codehaus.groovy.ast.expr.Expression getTo()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds from,inclusive,to

CLSS public org.codehaus.groovy.ast.expr.SpreadExpression
cons public init(org.codehaus.groovy.ast.expr.Expression)
meth public java.lang.String getText()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public org.codehaus.groovy.ast.expr.Expression getExpression()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds expression

CLSS public org.codehaus.groovy.ast.expr.SpreadMapExpression
cons public init(org.codehaus.groovy.ast.expr.Expression)
meth public java.lang.String getText()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public org.codehaus.groovy.ast.expr.Expression getExpression()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds expression

CLSS public org.codehaus.groovy.ast.expr.StaticMethodCallExpression
cons public init(org.codehaus.groovy.ast.ClassNode,java.lang.String,org.codehaus.groovy.ast.expr.Expression)
intf org.codehaus.groovy.ast.expr.MethodCall
meth public groovy.lang.MetaMethod getMetaMethod()
meth public java.lang.String getMethod()
meth public java.lang.String getMethodAsString()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.ASTNode getReceiver()
meth public org.codehaus.groovy.ast.ClassNode getOwnerType()
meth public org.codehaus.groovy.ast.expr.Expression getArguments()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void setMetaMethod(groovy.lang.MetaMethod)
meth public void setOwnerType(org.codehaus.groovy.ast.ClassNode)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds arguments,metaMethod,method,ownerType

CLSS public org.codehaus.groovy.ast.expr.TernaryExpression
cons public init(org.codehaus.groovy.ast.expr.BooleanExpression,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public org.codehaus.groovy.ast.expr.BooleanExpression getBooleanExpression()
meth public org.codehaus.groovy.ast.expr.Expression getFalseExpression()
meth public org.codehaus.groovy.ast.expr.Expression getTrueExpression()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds booleanExpression,falseExpression,trueExpression

CLSS public org.codehaus.groovy.ast.expr.TupleExpression
cons public init()
cons public init(int)
cons public init(java.util.List<org.codehaus.groovy.ast.expr.Expression>)
cons public init(org.codehaus.groovy.ast.expr.Expression)
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
cons public init(org.codehaus.groovy.ast.expr.Expression[])
intf java.lang.Iterable<org.codehaus.groovy.ast.expr.Expression>
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public java.util.Iterator<org.codehaus.groovy.ast.expr.Expression> iterator()
meth public java.util.List<org.codehaus.groovy.ast.expr.Expression> getExpressions()
meth public org.codehaus.groovy.ast.expr.Expression getExpression(int)
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public org.codehaus.groovy.ast.expr.TupleExpression addExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds expressions

CLSS public org.codehaus.groovy.ast.expr.UnaryMinusExpression
cons public init(org.codehaus.groovy.ast.expr.Expression)
meth public boolean isDynamic()
meth public java.lang.String getText()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public org.codehaus.groovy.ast.expr.Expression getExpression()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds expression

CLSS public org.codehaus.groovy.ast.expr.UnaryPlusExpression
cons public init(org.codehaus.groovy.ast.expr.Expression)
meth public boolean isDynamic()
meth public java.lang.String getText()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public org.codehaus.groovy.ast.expr.Expression getExpression()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds expression

CLSS public org.codehaus.groovy.ast.expr.VariableExpression
cons public init(java.lang.String)
cons public init(java.lang.String,org.codehaus.groovy.ast.ClassNode)
cons public init(org.codehaus.groovy.ast.Variable)
fld public final static org.codehaus.groovy.ast.expr.VariableExpression SUPER_EXPRESSION
fld public final static org.codehaus.groovy.ast.expr.VariableExpression THIS_EXPRESSION
intf org.codehaus.groovy.ast.Variable
meth public boolean hasInitialExpression()
meth public boolean isClosureSharedVariable()
meth public boolean isDynamicTyped()
meth public boolean isInStaticContext()
meth public boolean isSuperExpression()
meth public boolean isThisExpression()
meth public boolean isUseReferenceDirectly()
meth public int getModifiers()
meth public java.lang.String getName()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.ClassNode getOriginType()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public org.codehaus.groovy.ast.Variable getAccessedVariable()
meth public org.codehaus.groovy.ast.expr.Expression getInitialExpression()
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void setAccessedVariable(org.codehaus.groovy.ast.Variable)
meth public void setClosureSharedVariable(boolean)
meth public void setInStaticContext(boolean)
meth public void setModifiers(int)
meth public void setType(org.codehaus.groovy.ast.ClassNode)
meth public void setUseReferenceDirectly(boolean)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression
hfds accessedVariable,closureShare,inStaticContext,isDynamicTyped,modifiers,originType,useRef,variable

CLSS public org.codehaus.groovy.ast.stmt.AssertStatement
cons public init(org.codehaus.groovy.ast.expr.BooleanExpression)
cons public init(org.codehaus.groovy.ast.expr.BooleanExpression,org.codehaus.groovy.ast.expr.Expression)
meth public org.codehaus.groovy.ast.expr.BooleanExpression getBooleanExpression()
meth public org.codehaus.groovy.ast.expr.Expression getMessageExpression()
meth public void setBooleanExpression(org.codehaus.groovy.ast.expr.BooleanExpression)
meth public void setMessageExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds booleanExpression,messageExpression

CLSS public org.codehaus.groovy.ast.stmt.BlockStatement
cons public init()
cons public init(java.util.List<org.codehaus.groovy.ast.stmt.Statement>,org.codehaus.groovy.ast.VariableScope)
cons public init(org.codehaus.groovy.ast.stmt.Statement[],org.codehaus.groovy.ast.VariableScope)
meth public boolean isEmpty()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public java.util.List<org.codehaus.groovy.ast.stmt.Statement> getStatements()
meth public org.codehaus.groovy.ast.VariableScope getVariableScope()
meth public void addStatement(org.codehaus.groovy.ast.stmt.Statement)
meth public void addStatements(java.util.List<org.codehaus.groovy.ast.stmt.Statement>)
meth public void setVariableScope(org.codehaus.groovy.ast.VariableScope)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds scope,statements

CLSS public org.codehaus.groovy.ast.stmt.BreakStatement
cons public init()
cons public init(java.lang.String)
meth public java.lang.String getLabel()
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds label

CLSS public org.codehaus.groovy.ast.stmt.CaseStatement
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.stmt.Statement)
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.expr.Expression getExpression()
meth public org.codehaus.groovy.ast.stmt.Statement getCode()
meth public void setCode(org.codehaus.groovy.ast.stmt.Statement)
meth public void setExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds code,expression

CLSS public org.codehaus.groovy.ast.stmt.CatchStatement
cons public init(org.codehaus.groovy.ast.Parameter,org.codehaus.groovy.ast.stmt.Statement)
meth public org.codehaus.groovy.ast.ClassNode getExceptionType()
meth public org.codehaus.groovy.ast.Parameter getVariable()
meth public org.codehaus.groovy.ast.stmt.Statement getCode()
meth public void setCode(org.codehaus.groovy.ast.stmt.Statement)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds code,variable

CLSS public org.codehaus.groovy.ast.stmt.ContinueStatement
cons public init()
cons public init(java.lang.String)
meth public java.lang.String getLabel()
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds label

CLSS public org.codehaus.groovy.ast.stmt.DoWhileStatement
cons public init(org.codehaus.groovy.ast.expr.BooleanExpression,org.codehaus.groovy.ast.stmt.Statement)
intf org.codehaus.groovy.ast.stmt.LoopingStatement
meth public org.codehaus.groovy.ast.expr.BooleanExpression getBooleanExpression()
meth public org.codehaus.groovy.ast.stmt.Statement getLoopBlock()
meth public void setBooleanExpression(org.codehaus.groovy.ast.expr.BooleanExpression)
meth public void setLoopBlock(org.codehaus.groovy.ast.stmt.Statement)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds booleanExpression,loopBlock

CLSS public org.codehaus.groovy.ast.stmt.EmptyStatement
cons public init()
fld public final static org.codehaus.groovy.ast.stmt.EmptyStatement INSTANCE
meth public boolean isEmpty()
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement

CLSS public org.codehaus.groovy.ast.stmt.ExpressionStatement
cons public init(org.codehaus.groovy.ast.expr.Expression)
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.expr.Expression getExpression()
meth public void setExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds expression

CLSS public org.codehaus.groovy.ast.stmt.ForStatement
cons public init(org.codehaus.groovy.ast.Parameter,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.stmt.Statement)
fld public final static org.codehaus.groovy.ast.Parameter FOR_LOOP_DUMMY
intf org.codehaus.groovy.ast.stmt.LoopingStatement
meth public org.codehaus.groovy.ast.ClassNode getVariableType()
meth public org.codehaus.groovy.ast.Parameter getVariable()
meth public org.codehaus.groovy.ast.VariableScope getVariableScope()
meth public org.codehaus.groovy.ast.expr.Expression getCollectionExpression()
meth public org.codehaus.groovy.ast.stmt.Statement getLoopBlock()
meth public void setCollectionExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void setLoopBlock(org.codehaus.groovy.ast.stmt.Statement)
meth public void setVariableScope(org.codehaus.groovy.ast.VariableScope)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds collectionExpression,loopBlock,scope,variable

CLSS public org.codehaus.groovy.ast.stmt.IfStatement
cons public init(org.codehaus.groovy.ast.expr.BooleanExpression,org.codehaus.groovy.ast.stmt.Statement,org.codehaus.groovy.ast.stmt.Statement)
meth public org.codehaus.groovy.ast.expr.BooleanExpression getBooleanExpression()
meth public org.codehaus.groovy.ast.stmt.Statement getElseBlock()
meth public org.codehaus.groovy.ast.stmt.Statement getIfBlock()
meth public void setBooleanExpression(org.codehaus.groovy.ast.expr.BooleanExpression)
meth public void setElseBlock(org.codehaus.groovy.ast.stmt.Statement)
meth public void setIfBlock(org.codehaus.groovy.ast.stmt.Statement)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds booleanExpression,elseBlock,ifBlock

CLSS public abstract interface org.codehaus.groovy.ast.stmt.LoopingStatement
meth public abstract org.codehaus.groovy.ast.stmt.Statement getLoopBlock()
meth public abstract void setLoopBlock(org.codehaus.groovy.ast.stmt.Statement)

CLSS public org.codehaus.groovy.ast.stmt.ReturnStatement
cons public init(org.codehaus.groovy.ast.expr.Expression)
cons public init(org.codehaus.groovy.ast.stmt.ExpressionStatement)
fld public final static org.codehaus.groovy.ast.stmt.ReturnStatement RETURN_NULL_OR_VOID
meth public boolean isReturningNullOrVoid()
meth public java.lang.String getText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.expr.Expression getExpression()
meth public void setExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds expression

CLSS public org.codehaus.groovy.ast.stmt.Statement
cons public init()
meth public boolean isEmpty()
meth public java.lang.String getStatementLabel()
meth public java.util.List<java.lang.String> getStatementLabels()
meth public void addStatementLabel(java.lang.String)
meth public void setStatementLabel(java.lang.String)
supr org.codehaus.groovy.ast.ASTNode
hfds statementLabels

CLSS public org.codehaus.groovy.ast.stmt.SwitchStatement
cons public init(org.codehaus.groovy.ast.expr.Expression)
cons public init(org.codehaus.groovy.ast.expr.Expression,java.util.List<org.codehaus.groovy.ast.stmt.CaseStatement>,org.codehaus.groovy.ast.stmt.Statement)
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.stmt.Statement)
meth public java.util.List<org.codehaus.groovy.ast.stmt.CaseStatement> getCaseStatements()
meth public org.codehaus.groovy.ast.expr.Expression getExpression()
meth public org.codehaus.groovy.ast.stmt.CaseStatement getCaseStatement(int)
meth public org.codehaus.groovy.ast.stmt.Statement getDefaultStatement()
meth public void addCase(org.codehaus.groovy.ast.stmt.CaseStatement)
meth public void setDefaultStatement(org.codehaus.groovy.ast.stmt.Statement)
meth public void setExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds caseStatements,defaultStatement,expression

CLSS public org.codehaus.groovy.ast.stmt.SynchronizedStatement
cons public init(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.stmt.Statement)
meth public org.codehaus.groovy.ast.expr.Expression getExpression()
meth public org.codehaus.groovy.ast.stmt.Statement getCode()
meth public void setCode(org.codehaus.groovy.ast.stmt.Statement)
meth public void setExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds code,expression

CLSS public org.codehaus.groovy.ast.stmt.ThrowStatement
cons public init(org.codehaus.groovy.ast.expr.Expression)
meth public java.lang.String getText()
meth public org.codehaus.groovy.ast.expr.Expression getExpression()
meth public void setExpression(org.codehaus.groovy.ast.expr.Expression)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds expression

CLSS public org.codehaus.groovy.ast.stmt.TryCatchStatement
cons public init(org.codehaus.groovy.ast.stmt.Statement,org.codehaus.groovy.ast.stmt.Statement)
meth public java.util.List<org.codehaus.groovy.ast.stmt.CatchStatement> getCatchStatements()
meth public org.codehaus.groovy.ast.stmt.CatchStatement getCatchStatement(int)
meth public org.codehaus.groovy.ast.stmt.Statement getFinallyStatement()
meth public org.codehaus.groovy.ast.stmt.Statement getTryStatement()
meth public void addCatch(org.codehaus.groovy.ast.stmt.CatchStatement)
meth public void setCatchStatement(int,org.codehaus.groovy.ast.stmt.CatchStatement)
meth public void setFinallyStatement(org.codehaus.groovy.ast.stmt.Statement)
meth public void setTryStatement(org.codehaus.groovy.ast.stmt.Statement)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds catchStatements,finallyStatement,tryStatement

CLSS public org.codehaus.groovy.ast.stmt.WhileStatement
cons public init(org.codehaus.groovy.ast.expr.BooleanExpression,org.codehaus.groovy.ast.stmt.Statement)
intf org.codehaus.groovy.ast.stmt.LoopingStatement
meth public org.codehaus.groovy.ast.expr.BooleanExpression getBooleanExpression()
meth public org.codehaus.groovy.ast.stmt.Statement getLoopBlock()
meth public void setBooleanExpression(org.codehaus.groovy.ast.expr.BooleanExpression)
meth public void setLoopBlock(org.codehaus.groovy.ast.stmt.Statement)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds booleanExpression,loopBlock

CLSS public org.codehaus.groovy.ast.tools.BeanUtils
cons public init()
meth public static java.util.List<org.codehaus.groovy.ast.PropertyNode> getAllProperties(org.codehaus.groovy.ast.ClassNode,boolean,boolean,boolean)
meth public static java.util.List<org.codehaus.groovy.ast.PropertyNode> getAllProperties(org.codehaus.groovy.ast.ClassNode,boolean,boolean,boolean,boolean,boolean)
meth public static void addPseudoProperties(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,java.util.List<org.codehaus.groovy.ast.PropertyNode>,java.util.Set<java.lang.String>,boolean,boolean,boolean)
supr java.lang.Object
hfds GENERATED_TYPE,GET_PREFIX,IS_PREFIX,SET_PREFIX

CLSS public org.codehaus.groovy.ast.tools.ClassNodeUtils
 anno 0 java.lang.Deprecated()
cons public init()
meth public static boolean hasPossibleStaticMethod(org.codehaus.groovy.ast.ClassNode,java.lang.String,org.codehaus.groovy.ast.expr.Expression,boolean)
 anno 0 java.lang.Deprecated()
meth public static boolean hasPossibleStaticProperty(org.codehaus.groovy.ast.ClassNode,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static boolean hasStaticProperty(org.codehaus.groovy.ast.ClassNode,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static boolean isInnerClass(org.codehaus.groovy.ast.ClassNode)
 anno 0 java.lang.Deprecated()
meth public static boolean isValidAccessorName(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.lang.String getPropNameForAccessor(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.util.Map<java.lang.String,org.codehaus.groovy.ast.MethodNode> getDeclaredMethodMapsFromInterfaces(org.codehaus.groovy.ast.ClassNode)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.PropertyNode getStaticProperty(org.codehaus.groovy.ast.ClassNode,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static void addDeclaredMethodMapsFromSuperInterfaces(org.codehaus.groovy.ast.ClassNode,java.util.Map<java.lang.String,org.codehaus.groovy.ast.MethodNode>)
 anno 0 java.lang.Deprecated()
meth public static void addInterfaceMethods(org.codehaus.groovy.ast.ClassNode,java.util.Map<java.lang.String,org.codehaus.groovy.ast.MethodNode>)
 anno 0 java.lang.Deprecated()
supr java.lang.Object

CLSS public org.codehaus.groovy.ast.tools.ClosureUtils
cons public init()
meth public static boolean hasSingleCharacterArg(groovy.lang.Closure)
meth public static boolean hasSingleStringArg(groovy.lang.Closure)
meth public static java.lang.String convertClosureToSource(org.codehaus.groovy.control.io.ReaderSource,org.codehaus.groovy.ast.expr.ClosureExpression) throws java.lang.Exception
supr java.lang.Object

CLSS public org.codehaus.groovy.ast.tools.GeneralUtils
cons public init()
fld public final static org.codehaus.groovy.syntax.Token AND
fld public final static org.codehaus.groovy.syntax.Token ASSIGN
fld public final static org.codehaus.groovy.syntax.Token CMP
fld public final static org.codehaus.groovy.syntax.Token EQ
fld public final static org.codehaus.groovy.syntax.Token LT
fld public final static org.codehaus.groovy.syntax.Token NE
fld public final static org.codehaus.groovy.syntax.Token OR
meth public !varargs static org.codehaus.groovy.ast.Parameter[] params(org.codehaus.groovy.ast.Parameter[])
meth public !varargs static org.codehaus.groovy.ast.expr.ArgumentListExpression args(java.lang.String[])
meth public !varargs static org.codehaus.groovy.ast.expr.ArgumentListExpression args(org.codehaus.groovy.ast.expr.Expression[])
meth public !varargs static org.codehaus.groovy.ast.stmt.BlockStatement block(org.codehaus.groovy.ast.VariableScope,org.codehaus.groovy.ast.stmt.Statement[])
meth public !varargs static org.codehaus.groovy.ast.stmt.BlockStatement block(org.codehaus.groovy.ast.stmt.Statement[])
meth public static boolean copyStatementsWithSuperAdjustment(org.codehaus.groovy.ast.expr.ClosureExpression,org.codehaus.groovy.ast.stmt.BlockStatement)
meth public static boolean hasDeclaredMethod(org.codehaus.groovy.ast.ClassNode,java.lang.String,int)
meth public static boolean inSamePackage(java.lang.Class,java.lang.Class)
meth public static boolean inSamePackage(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
meth public static boolean isDefaultVisibility(int)
meth public static boolean isOrImplements(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
meth public static java.lang.String convertASTToSource(org.codehaus.groovy.control.io.ReaderSource,org.codehaus.groovy.ast.ASTNode) throws java.lang.Exception
meth public static java.lang.String getGetterName(org.codehaus.groovy.ast.PropertyNode)
meth public static java.lang.String getSetterName(java.lang.String)
meth public static java.lang.String makeDescriptorWithoutReturnType(org.codehaus.groovy.ast.MethodNode)
 anno 0 java.lang.Deprecated()
meth public static java.util.List<java.lang.String> getInstanceNonPropertyFieldNames(org.codehaus.groovy.ast.ClassNode)
meth public static java.util.List<java.lang.String> getInstancePropertyNames(org.codehaus.groovy.ast.ClassNode)
meth public static java.util.List<org.codehaus.groovy.ast.FieldNode> getInstanceNonPropertyFields(org.codehaus.groovy.ast.ClassNode)
meth public static java.util.List<org.codehaus.groovy.ast.FieldNode> getInstancePropertyFields(org.codehaus.groovy.ast.ClassNode)
meth public static java.util.List<org.codehaus.groovy.ast.FieldNode> getSuperNonPropertyFields(org.codehaus.groovy.ast.ClassNode)
meth public static java.util.List<org.codehaus.groovy.ast.FieldNode> getSuperPropertyFields(org.codehaus.groovy.ast.ClassNode)
meth public static java.util.List<org.codehaus.groovy.ast.MethodNode> getAllMethods(org.codehaus.groovy.ast.ClassNode)
meth public static java.util.List<org.codehaus.groovy.ast.PropertyNode> getAllProperties(java.util.Set<java.lang.String>,org.codehaus.groovy.ast.ClassNode,boolean,boolean,boolean,boolean,boolean,boolean)
meth public static java.util.List<org.codehaus.groovy.ast.PropertyNode> getAllProperties(java.util.Set<java.lang.String>,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,boolean,boolean,boolean,boolean,boolean,boolean)
meth public static java.util.List<org.codehaus.groovy.ast.PropertyNode> getAllProperties(java.util.Set<java.lang.String>,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,boolean,boolean,boolean,boolean,boolean,boolean,boolean,boolean,boolean)
meth public static java.util.List<org.codehaus.groovy.ast.PropertyNode> getAllProperties(org.codehaus.groovy.ast.ClassNode)
meth public static java.util.List<org.codehaus.groovy.ast.PropertyNode> getInstanceProperties(org.codehaus.groovy.ast.ClassNode)
meth public static java.util.Set<org.codehaus.groovy.ast.ClassNode> getInterfacesAndSuperInterfaces(org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.Parameter param(org.codehaus.groovy.ast.ClassNode,java.lang.String)
meth public static org.codehaus.groovy.ast.Parameter param(org.codehaus.groovy.ast.ClassNode,java.lang.String,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.Parameter[] cloneParams(org.codehaus.groovy.ast.Parameter[])
meth public static org.codehaus.groovy.ast.expr.ArgumentListExpression args(java.util.List<org.codehaus.groovy.ast.expr.Expression>)
meth public static org.codehaus.groovy.ast.expr.ArgumentListExpression args(org.codehaus.groovy.ast.Parameter[])
meth public static org.codehaus.groovy.ast.expr.BinaryExpression andX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BinaryExpression binX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.syntax.Token,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BinaryExpression cmpX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BinaryExpression eqX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BinaryExpression hasClassX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.expr.BinaryExpression hasEqualFieldX(org.codehaus.groovy.ast.FieldNode,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BinaryExpression hasEqualPropertyX(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.PropertyNode,org.codehaus.groovy.ast.expr.VariableExpression)
meth public static org.codehaus.groovy.ast.expr.BinaryExpression hasEqualPropertyX(org.codehaus.groovy.ast.PropertyNode,org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.expr.BinaryExpression ltX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BinaryExpression neX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BinaryExpression orX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BinaryExpression plusX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BooleanExpression boolX(org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BooleanExpression equalsNullX(org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BooleanExpression hasSameFieldX(org.codehaus.groovy.ast.FieldNode,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BooleanExpression hasSamePropertyX(org.codehaus.groovy.ast.PropertyNode,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BooleanExpression isInstanceOfX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.expr.BooleanExpression isOneX(org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BooleanExpression isTrueX(org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BooleanExpression isZeroX(org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BooleanExpression notNullX(org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.BooleanExpression sameX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.CastExpression castX(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.CastExpression castX(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression,boolean)
meth public static org.codehaus.groovy.ast.expr.ClassExpression classX(java.lang.Class)
meth public static org.codehaus.groovy.ast.expr.ClassExpression classX(org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.expr.ClosureExpression closureX(org.codehaus.groovy.ast.Parameter[],org.codehaus.groovy.ast.stmt.Statement)
meth public static org.codehaus.groovy.ast.expr.ClosureExpression closureX(org.codehaus.groovy.ast.stmt.Statement)
meth public static org.codehaus.groovy.ast.expr.ConstantExpression constX(java.lang.Object)
meth public static org.codehaus.groovy.ast.expr.ConstantExpression constX(java.lang.Object,boolean)
meth public static org.codehaus.groovy.ast.expr.ConstructorCallExpression ctorX(org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.expr.ConstructorCallExpression ctorX(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.Expression assignX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.Expression attrX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.Expression findArg(java.lang.String)
meth public static org.codehaus.groovy.ast.expr.Expression getterThisX(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.PropertyNode)
meth public static org.codehaus.groovy.ast.expr.Expression getterX(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.PropertyNode)
meth public static org.codehaus.groovy.ast.expr.Expression indexX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.Expression propX(org.codehaus.groovy.ast.expr.Expression,java.lang.String)
meth public static org.codehaus.groovy.ast.expr.Expression propX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.FieldExpression fieldX(org.codehaus.groovy.ast.ClassNode,java.lang.String)
meth public static org.codehaus.groovy.ast.expr.FieldExpression fieldX(org.codehaus.groovy.ast.FieldNode)
meth public static org.codehaus.groovy.ast.expr.ListExpression classList2args(java.util.List<java.lang.String>)
meth public static org.codehaus.groovy.ast.expr.ListExpression list2args(java.util.List)
meth public static org.codehaus.groovy.ast.expr.MethodCallExpression callSuperX(java.lang.String)
meth public static org.codehaus.groovy.ast.expr.MethodCallExpression callSuperX(java.lang.String,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.MethodCallExpression callThisX(java.lang.String)
meth public static org.codehaus.groovy.ast.expr.MethodCallExpression callThisX(java.lang.String,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.MethodCallExpression callX(org.codehaus.groovy.ast.expr.Expression,java.lang.String)
meth public static org.codehaus.groovy.ast.expr.MethodCallExpression callX(org.codehaus.groovy.ast.expr.Expression,java.lang.String,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.MethodCallExpression callX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.NotExpression notX(org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.StaticMethodCallExpression callX(org.codehaus.groovy.ast.ClassNode,java.lang.String)
meth public static org.codehaus.groovy.ast.expr.StaticMethodCallExpression callX(org.codehaus.groovy.ast.ClassNode,java.lang.String,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.TernaryExpression ternaryX(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.expr.VariableExpression varX(java.lang.String)
meth public static org.codehaus.groovy.ast.expr.VariableExpression varX(java.lang.String,org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.expr.VariableExpression varX(org.codehaus.groovy.ast.Variable)
meth public static org.codehaus.groovy.ast.stmt.BlockStatement block(org.codehaus.groovy.ast.VariableScope,java.util.List<org.codehaus.groovy.ast.stmt.Statement>)
meth public static org.codehaus.groovy.ast.stmt.CatchStatement catchS(org.codehaus.groovy.ast.Parameter,org.codehaus.groovy.ast.stmt.Statement)
meth public static org.codehaus.groovy.ast.stmt.Statement assignS(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.stmt.Statement createConstructorStatementDefault(org.codehaus.groovy.ast.FieldNode)
meth public static org.codehaus.groovy.ast.stmt.Statement ctorSuperS()
meth public static org.codehaus.groovy.ast.stmt.Statement ctorSuperS(org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.stmt.Statement ctorThisS()
meth public static org.codehaus.groovy.ast.stmt.Statement ctorThisS(org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.stmt.Statement declS(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.stmt.Statement ifElseS(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.stmt.Statement,org.codehaus.groovy.ast.stmt.Statement)
meth public static org.codehaus.groovy.ast.stmt.Statement ifS(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.stmt.Statement ifS(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.stmt.Statement)
meth public static org.codehaus.groovy.ast.stmt.Statement returnS(org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.stmt.Statement safeExpression(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.stmt.Statement stmt(org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.stmt.ThrowStatement throwS(org.codehaus.groovy.ast.expr.Expression)
meth public static void copyAnnotatedNodeAnnotations(org.codehaus.groovy.ast.AnnotatedNode,java.util.List<org.codehaus.groovy.ast.AnnotationNode>,java.util.List<org.codehaus.groovy.ast.AnnotationNode>)
supr java.lang.Object
hfds INDEX,INSTANCEOF,PLUS

CLSS public org.codehaus.groovy.ast.tools.GenericsUtils
cons public init()
fld public final static java.lang.String JAVA_LANG_OBJECT = "java.lang.Object"
fld public final static org.codehaus.groovy.ast.GenericsType[] EMPTY_GENERICS_ARRAY
meth public !varargs static org.codehaus.groovy.ast.ClassNode makeClassSafe0(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.GenericsType[])
meth public !varargs static org.codehaus.groovy.ast.ClassNode makeClassSafeWithGenerics(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.GenericsType[])
meth public !varargs static org.codehaus.groovy.ast.GenericsType buildWildcardType(org.codehaus.groovy.ast.ClassNode[])
meth public static java.lang.String toGenericTypesString(org.codehaus.groovy.ast.GenericsType[])
meth public static java.util.Map<java.lang.String,org.codehaus.groovy.ast.ClassNode> addMethodGenerics(org.codehaus.groovy.ast.MethodNode,java.util.Map<java.lang.String,org.codehaus.groovy.ast.ClassNode>)
meth public static java.util.Map<java.lang.String,org.codehaus.groovy.ast.ClassNode> createGenericsSpec(org.codehaus.groovy.ast.ClassNode)
meth public static java.util.Map<java.lang.String,org.codehaus.groovy.ast.ClassNode> createGenericsSpec(org.codehaus.groovy.ast.ClassNode,java.util.Map<java.lang.String,org.codehaus.groovy.ast.ClassNode>)
meth public static java.util.Map<java.lang.String,org.codehaus.groovy.ast.GenericsType> extractPlaceholders(org.codehaus.groovy.ast.ClassNode)
meth public static java.util.Map<org.codehaus.groovy.ast.GenericsType,org.codehaus.groovy.ast.GenericsType> makeDeclaringAndActualGenericsTypeMap(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode correctToGenericsSpec(java.util.Map<java.lang.String,org.codehaus.groovy.ast.ClassNode>,org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode correctToGenericsSpec(java.util.Map<java.lang.String,org.codehaus.groovy.ast.ClassNode>,org.codehaus.groovy.ast.GenericsType)
meth public static org.codehaus.groovy.ast.ClassNode correctToGenericsSpecRecurse(java.util.Map<java.lang.String,org.codehaus.groovy.ast.ClassNode>,org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode correctToGenericsSpecRecurse(java.util.Map<java.lang.String,org.codehaus.groovy.ast.ClassNode>,org.codehaus.groovy.ast.ClassNode,java.util.List<java.lang.String>)
meth public static org.codehaus.groovy.ast.ClassNode findActualTypeByGenericsPlaceholderName(java.lang.String,java.util.Map<org.codehaus.groovy.ast.GenericsType,org.codehaus.groovy.ast.GenericsType>)
meth public static org.codehaus.groovy.ast.ClassNode findParameterizedType(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode findParameterizedTypeFromCache(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode getSuperClass(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode makeClassSafe(java.lang.Class)
meth public static org.codehaus.groovy.ast.ClassNode makeClassSafeWithGenerics(java.lang.Class,org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode newClass(org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode nonGeneric(org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode parameterizeInterfaceGenerics(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.ClassNode parameterizeType(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode[] correctToGenericsSpecRecurse(java.util.Map<java.lang.String,org.codehaus.groovy.ast.ClassNode>,org.codehaus.groovy.ast.ClassNode[])
meth public static org.codehaus.groovy.ast.ClassNode[] parseClassNodesFromString(java.lang.String,org.codehaus.groovy.control.SourceUnit,org.codehaus.groovy.control.CompilationUnit,org.codehaus.groovy.ast.MethodNode,org.codehaus.groovy.ast.ASTNode)
meth public static org.codehaus.groovy.ast.GenericsType[] alignGenericTypes(org.codehaus.groovy.ast.GenericsType[],org.codehaus.groovy.ast.GenericsType[],org.codehaus.groovy.ast.GenericsType[])
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.GenericsType[] applyGenericsContextToPlaceHolders(java.util.Map<java.lang.String,org.codehaus.groovy.ast.ClassNode>,org.codehaus.groovy.ast.GenericsType[])
meth public static org.codehaus.groovy.ast.MethodNode correctToGenericsSpec(java.util.Map<java.lang.String,org.codehaus.groovy.ast.ClassNode>,org.codehaus.groovy.ast.MethodNode)
meth public static void clearParameterizedTypeCache()
meth public static void extractPlaceholders(org.codehaus.groovy.ast.ClassNode,java.util.Map<java.lang.String,org.codehaus.groovy.ast.GenericsType>)
meth public static void extractSuperClassGenerics(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,java.util.Map<java.lang.String,org.codehaus.groovy.ast.ClassNode>)
supr java.lang.Object
hfds PARAMETERIZED_TYPE_CACHE,PARAMETERIZED_TYPE_CACHE_ENABLED,TRUE_STR
hcls ParameterizedTypeCacheKey

CLSS public org.codehaus.groovy.ast.tools.ParameterUtils
cons public init()
meth public static boolean parametersEqual(org.codehaus.groovy.ast.Parameter[],org.codehaus.groovy.ast.Parameter[])
supr java.lang.Object

CLSS public org.codehaus.groovy.ast.tools.PropertyNodeUtils
cons public init()
meth public static int adjustPropertyModifiersForMethod(org.codehaus.groovy.ast.PropertyNode)
supr java.lang.Object

CLSS public org.codehaus.groovy.ast.tools.WideningCategories
cons public init()
innr public static LowestUpperBoundClassNode
meth public static boolean implementsInterfaceOrSubclassOf(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
meth public static boolean isBigDecCategory(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isBigIntCategory(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isDouble(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isDoubleCategory(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isFloat(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isFloatingCategory(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isInt(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isIntCategory(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isLongCategory(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isNumberCategory(org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode lowestUpperBound(java.util.List<org.codehaus.groovy.ast.ClassNode>)
meth public static org.codehaus.groovy.ast.ClassNode lowestUpperBound(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
supr java.lang.Object
hfds EMPTY_CLASSNODE_LIST,INTERFACE_CLASSNODE_COMPARATOR,NUMBER_TYPES_PRECEDENCE

CLSS public static org.codehaus.groovy.ast.tools.WideningCategories$LowestUpperBoundClassNode
 outer org.codehaus.groovy.ast.tools.WideningCategories
cons public !varargs init(java.lang.String,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode[])
meth public int hashCode()
meth public java.lang.Class getTypeClass()
meth public java.lang.String getLubName()
meth public java.lang.String getName()
meth public java.lang.String getText()
meth public org.codehaus.groovy.ast.ClassNode getPlainNodeReference()
supr org.codehaus.groovy.ast.ClassNode
hfds CLASS_NODE_COMPARATOR,compileTimeClassNode,interfaces,name,text,upper

CLSS public org.codehaus.groovy.classgen.AnnotationVisitor
cons public init(org.codehaus.groovy.control.SourceUnit,org.codehaus.groovy.control.ErrorCollector)
meth protected void addError(java.lang.String)
meth protected void addError(java.lang.String,org.codehaus.groovy.ast.ASTNode)
meth protected void visitAnnotationExpression(java.lang.String,org.codehaus.groovy.ast.expr.AnnotationConstantExpression,org.codehaus.groovy.ast.ClassNode)
meth protected void visitConstantExpression(java.lang.String,org.codehaus.groovy.ast.expr.ConstantExpression,org.codehaus.groovy.ast.ClassNode)
meth protected void visitEnumExpression(java.lang.String,org.codehaus.groovy.ast.expr.PropertyExpression,org.codehaus.groovy.ast.ClassNode)
meth protected void visitExpression(java.lang.String,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.ClassNode)
meth protected void visitListExpression(java.lang.String,org.codehaus.groovy.ast.expr.ListExpression,org.codehaus.groovy.ast.ClassNode)
meth public org.codehaus.groovy.ast.AnnotationNode visit(org.codehaus.groovy.ast.AnnotationNode)
meth public void checkCircularReference(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
meth public void checkReturnType(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ASTNode)
meth public void setReportClass(org.codehaus.groovy.ast.ClassNode)
supr java.lang.Object
hfds annotation,errorCollector,reportClass,source

CLSS public org.codehaus.groovy.classgen.AsmClassGenerator
cons public init(org.codehaus.groovy.control.SourceUnit,org.codehaus.groovy.classgen.GeneratorContext,groovyjarjarasm.asm.ClassVisitor,java.lang.String)
fld public final static boolean ASM_DEBUG = false
fld public final static boolean CREATE_DEBUG_INFO = true
fld public final static boolean CREATE_LINE_NUMBER_INFO = true
fld public final static java.lang.String MINIMUM_BYTECODE_VERSION = "_MINIMUM_BYTECODE_VERSION"
fld public final static org.codehaus.groovy.classgen.asm.MethodCallerMultiAdapter getField
fld public final static org.codehaus.groovy.classgen.asm.MethodCallerMultiAdapter getGroovyObjectField
fld public final static org.codehaus.groovy.classgen.asm.MethodCallerMultiAdapter setField
fld public final static org.codehaus.groovy.classgen.asm.MethodCallerMultiAdapter setGroovyObjectField
fld public final static org.codehaus.groovy.classgen.asm.MethodCallerMultiAdapter setProperty
meth protected boolean emptyArguments(org.codehaus.groovy.ast.expr.Expression)
meth protected org.codehaus.groovy.ast.CompileUnit getCompileUnit()
meth protected void createInterfaceSyntheticStaticFields()
meth protected void createSyntheticStaticFields()
meth protected void loadThisOrOwner()
meth protected void visitConstructorOrMethod(org.codehaus.groovy.ast.MethodNode,boolean)
meth protected void visitStatement(org.codehaus.groovy.ast.stmt.Statement)
meth public boolean addInnerClass(org.codehaus.groovy.ast.ClassNode)
meth public org.codehaus.groovy.classgen.asm.WriterController getController()
meth public org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth public static boolean containsSpreadExpression(org.codehaus.groovy.ast.expr.Expression)
meth public static boolean isNullConstant(org.codehaus.groovy.ast.expr.Expression)
meth public static boolean isThisExpression(org.codehaus.groovy.ast.expr.Expression)
meth public static boolean samePackages(java.lang.String,java.lang.String)
meth public static int argumentSize(org.codehaus.groovy.ast.expr.Expression)
meth public static org.codehaus.groovy.ast.FieldNode getDeclaredFieldOfCurrentClassOrAccessibleFieldOfSuper(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,java.lang.String,boolean)
meth public void despreadList(java.util.List,boolean)
meth public void loadInstanceField(org.codehaus.groovy.ast.expr.FieldExpression)
meth public void loadStaticField(org.codehaus.groovy.ast.expr.FieldExpression)
meth public void loadWrapper(org.codehaus.groovy.ast.expr.Expression)
meth public void onLineNumber(org.codehaus.groovy.ast.ASTNode,java.lang.String)
meth public void throwException(java.lang.String)
meth public void visitAnnotations(org.codehaus.groovy.ast.AnnotatedNode)
meth public void visitArgumentlistExpression(org.codehaus.groovy.ast.expr.ArgumentListExpression)
meth public void visitArrayExpression(org.codehaus.groovy.ast.expr.ArrayExpression)
meth public void visitAssertStatement(org.codehaus.groovy.ast.stmt.AssertStatement)
meth public void visitAttributeExpression(org.codehaus.groovy.ast.expr.AttributeExpression)
meth public void visitBinaryExpression(org.codehaus.groovy.ast.expr.BinaryExpression)
meth public void visitBitwiseNegationExpression(org.codehaus.groovy.ast.expr.BitwiseNegationExpression)
meth public void visitBlockStatement(org.codehaus.groovy.ast.stmt.BlockStatement)
meth public void visitBooleanExpression(org.codehaus.groovy.ast.expr.BooleanExpression)
meth public void visitBreakStatement(org.codehaus.groovy.ast.stmt.BreakStatement)
meth public void visitBytecodeExpression(org.codehaus.groovy.classgen.BytecodeExpression)
meth public void visitBytecodeSequence(org.codehaus.groovy.classgen.BytecodeSequence)
meth public void visitCaseStatement(org.codehaus.groovy.ast.stmt.CaseStatement)
meth public void visitCastExpression(org.codehaus.groovy.ast.expr.CastExpression)
meth public void visitCatchStatement(org.codehaus.groovy.ast.stmt.CatchStatement)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public void visitClassExpression(org.codehaus.groovy.ast.expr.ClassExpression)
meth public void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public void visitClosureListExpression(org.codehaus.groovy.ast.expr.ClosureListExpression)
meth public void visitConstantExpression(org.codehaus.groovy.ast.expr.ConstantExpression)
meth public void visitConstructor(org.codehaus.groovy.ast.ConstructorNode)
meth public void visitConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth public void visitContinueStatement(org.codehaus.groovy.ast.stmt.ContinueStatement)
meth public void visitDeclarationExpression(org.codehaus.groovy.ast.expr.DeclarationExpression)
meth public void visitDoWhileLoop(org.codehaus.groovy.ast.stmt.DoWhileStatement)
meth public void visitExpressionStatement(org.codehaus.groovy.ast.stmt.ExpressionStatement)
meth public void visitField(org.codehaus.groovy.ast.FieldNode)
meth public void visitFieldExpression(org.codehaus.groovy.ast.expr.FieldExpression)
meth public void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement)
meth public void visitGStringExpression(org.codehaus.groovy.ast.expr.GStringExpression)
meth public void visitGenericType(org.codehaus.groovy.ast.GenericsType)
meth public void visitIfElse(org.codehaus.groovy.ast.stmt.IfStatement)
meth public void visitListExpression(org.codehaus.groovy.ast.expr.ListExpression)
meth public void visitMapEntryExpression(org.codehaus.groovy.ast.expr.MapEntryExpression)
meth public void visitMapExpression(org.codehaus.groovy.ast.expr.MapExpression)
meth public void visitMethod(org.codehaus.groovy.ast.MethodNode)
meth public void visitMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression)
meth public void visitMethodPointerExpression(org.codehaus.groovy.ast.expr.MethodPointerExpression)
meth public void visitNotExpression(org.codehaus.groovy.ast.expr.NotExpression)
meth public void visitPostfixExpression(org.codehaus.groovy.ast.expr.PostfixExpression)
meth public void visitPrefixExpression(org.codehaus.groovy.ast.expr.PrefixExpression)
meth public void visitProperty(org.codehaus.groovy.ast.PropertyNode)
meth public void visitPropertyExpression(org.codehaus.groovy.ast.expr.PropertyExpression)
meth public void visitRangeExpression(org.codehaus.groovy.ast.expr.RangeExpression)
meth public void visitReturnStatement(org.codehaus.groovy.ast.stmt.ReturnStatement)
meth public void visitSpreadExpression(org.codehaus.groovy.ast.expr.SpreadExpression)
meth public void visitSpreadMapExpression(org.codehaus.groovy.ast.expr.SpreadMapExpression)
meth public void visitStaticMethodCallExpression(org.codehaus.groovy.ast.expr.StaticMethodCallExpression)
meth public void visitSwitch(org.codehaus.groovy.ast.stmt.SwitchStatement)
meth public void visitSynchronizedStatement(org.codehaus.groovy.ast.stmt.SynchronizedStatement)
meth public void visitTernaryExpression(org.codehaus.groovy.ast.expr.TernaryExpression)
meth public void visitThrowStatement(org.codehaus.groovy.ast.stmt.ThrowStatement)
meth public void visitTryCatchFinally(org.codehaus.groovy.ast.stmt.TryCatchStatement)
meth public void visitTupleExpression(org.codehaus.groovy.ast.expr.TupleExpression)
meth public void visitUnaryMinusExpression(org.codehaus.groovy.ast.expr.UnaryMinusExpression)
meth public void visitUnaryPlusExpression(org.codehaus.groovy.ast.expr.UnaryPlusExpression)
meth public void visitVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
meth public void visitWhileLoop(org.codehaus.groovy.ast.stmt.WhileStatement)
supr org.codehaus.groovy.classgen.ClassGenerator
hfds context,controller,createGroovyObjectWrapperMethod,createListMethod,createMapMethod,createPojoWrapperMethod,createRangeMethod,currentASTNode,cv,despreadList,genericParameterNames,getFieldOnSuper,getGroovyObjectProperty,getMethodPointer,getProperty,getPropertyOnSuper,passingParams,referencedClasses,setFieldOnSuper,setGroovyObjectProperty,setPropertyOnSuper,source,sourceFile,spreadMap

CLSS public abstract org.codehaus.groovy.classgen.BytecodeExpression
cons public init()
cons public init(org.codehaus.groovy.ast.ClassNode)
fld public final static org.codehaus.groovy.classgen.BytecodeExpression NOP
meth public abstract void visit(groovyjarjarasm.asm.MethodVisitor)
meth public org.codehaus.groovy.ast.expr.Expression transformExpression(org.codehaus.groovy.ast.expr.ExpressionTransformer)
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.expr.Expression

CLSS public abstract org.codehaus.groovy.classgen.BytecodeInstruction
cons public init()
meth public abstract void visit(groovyjarjarasm.asm.MethodVisitor)
supr java.lang.Object

CLSS public org.codehaus.groovy.classgen.BytecodeSequence
cons public init(java.util.List)
cons public init(org.codehaus.groovy.classgen.BytecodeInstruction)
meth public java.util.List getInstructions()
meth public void visit(org.codehaus.groovy.ast.GroovyCodeVisitor)
supr org.codehaus.groovy.ast.stmt.Statement
hfds instructions

CLSS public org.codehaus.groovy.classgen.ClassCompletionVerifier
cons public init(org.codehaus.groovy.control.SourceUnit)
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth public org.codehaus.groovy.ast.ClassNode getClassNode()
meth public void visitBinaryExpression(org.codehaus.groovy.ast.expr.BinaryExpression)
meth public void visitCatchStatement(org.codehaus.groovy.ast.stmt.CatchStatement)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public void visitConstantExpression(org.codehaus.groovy.ast.expr.ConstantExpression)
meth public void visitConstructor(org.codehaus.groovy.ast.ConstructorNode)
meth public void visitDeclarationExpression(org.codehaus.groovy.ast.expr.DeclarationExpression)
meth public void visitField(org.codehaus.groovy.ast.FieldNode)
meth public void visitGStringExpression(org.codehaus.groovy.ast.expr.GStringExpression)
meth public void visitMethod(org.codehaus.groovy.ast.MethodNode)
meth public void visitMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression)
meth public void visitProperty(org.codehaus.groovy.ast.PropertyNode)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds INVALID_NAME_CHARS,currentClass,inConstructor,inStaticConstructor,source,strictNames

CLSS public abstract org.codehaus.groovy.classgen.ClassGenerator
cons public init()
fld protected java.util.LinkedList<org.codehaus.groovy.ast.ClassNode> innerClasses
intf groovyjarjarasm.asm.Opcodes
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth public java.util.LinkedList<org.codehaus.groovy.ast.ClassNode> getInnerClasses()
meth public void visitBytecodeSequence(org.codehaus.groovy.classgen.BytecodeSequence)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport

CLSS public org.codehaus.groovy.classgen.ClassGeneratorException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.Throwable)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public org.codehaus.groovy.classgen.DummyClassGenerator
cons public init(org.codehaus.groovy.classgen.GeneratorContext,groovyjarjarasm.asm.ClassVisitor,java.lang.ClassLoader,java.lang.String)
meth protected org.codehaus.groovy.ast.CompileUnit getCompileUnit()
meth protected void visitParameter(org.codehaus.groovy.ast.ASTNode,org.codehaus.groovy.ast.Parameter)
meth protected void visitParameters(org.codehaus.groovy.ast.ASTNode,org.codehaus.groovy.ast.Parameter[])
meth public void visitAnnotations(org.codehaus.groovy.ast.AnnotatedNode)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public void visitConstructor(org.codehaus.groovy.ast.ConstructorNode)
meth public void visitField(org.codehaus.groovy.ast.FieldNode)
meth public void visitMethod(org.codehaus.groovy.ast.MethodNode)
meth public void visitProperty(org.codehaus.groovy.ast.PropertyNode)
supr org.codehaus.groovy.classgen.ClassGenerator
hfds classNode,context,cv,internalBaseClassName,internalClassName,mv

CLSS public org.codehaus.groovy.classgen.EnumCompletionVisitor
cons public init(org.codehaus.groovy.control.CompilationUnit,org.codehaus.groovy.control.SourceUnit)
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds sourceUnit

CLSS public org.codehaus.groovy.classgen.EnumVisitor
cons public init(org.codehaus.groovy.control.CompilationUnit,org.codehaus.groovy.control.SourceUnit)
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds FS,PRIVATE_FS,PS,PUBLIC_FS,sourceUnit

CLSS public org.codehaus.groovy.classgen.ExtendedVerifier
cons public init(org.codehaus.groovy.control.SourceUnit)
fld public final static java.lang.String JVM_ERROR_MESSAGE = "Please make sure you are running on a JVM >= 1.5"
meth protected boolean isAnnotationCompatible()
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth protected void visitAnnotations(org.codehaus.groovy.ast.AnnotatedNode,int)
meth public void addError(java.lang.String,org.codehaus.groovy.ast.ASTNode)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public void visitConstructor(org.codehaus.groovy.ast.ConstructorNode)
meth public void visitDeclarationExpression(org.codehaus.groovy.ast.expr.DeclarationExpression)
meth public void visitField(org.codehaus.groovy.ast.FieldNode)
meth public void visitGenericType(org.codehaus.groovy.ast.GenericsType)
meth public void visitMethod(org.codehaus.groovy.ast.MethodNode)
meth public void visitProperty(org.codehaus.groovy.ast.PropertyNode)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds currentClass,source

CLSS public org.codehaus.groovy.classgen.FinalVariableAnalyzer
cons public init(org.codehaus.groovy.control.SourceUnit)
cons public init(org.codehaus.groovy.control.SourceUnit,org.codehaus.groovy.classgen.FinalVariableAnalyzer$VariableNotFinalCallback)
innr public abstract interface static VariableNotFinalCallback
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth public boolean isEffectivelyFinal(org.codehaus.groovy.ast.Variable)
meth public void visitArgumentlistExpression(org.codehaus.groovy.ast.expr.ArgumentListExpression)
meth public void visitBinaryExpression(org.codehaus.groovy.ast.expr.BinaryExpression)
meth public void visitBlockStatement(org.codehaus.groovy.ast.stmt.BlockStatement)
meth public void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public void visitIfElse(org.codehaus.groovy.ast.stmt.IfStatement)
meth public void visitPostfixExpression(org.codehaus.groovy.ast.expr.PostfixExpression)
meth public void visitPrefixExpression(org.codehaus.groovy.ast.expr.PrefixExpression)
meth public void visitTryCatchFinally(org.codehaus.groovy.ast.stmt.TryCatchStatement)
meth public void visitVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds assignmentTracker,callback,declaredFinalVariables,inArgumentList,inAssignmentRHS,sourceUnit
hcls StateMap,VariableState

CLSS public abstract interface static org.codehaus.groovy.classgen.FinalVariableAnalyzer$VariableNotFinalCallback
 outer org.codehaus.groovy.classgen.FinalVariableAnalyzer
meth public abstract void variableNotAlwaysInitialized(org.codehaus.groovy.ast.expr.VariableExpression)
meth public abstract void variableNotFinal(org.codehaus.groovy.ast.Variable,org.codehaus.groovy.ast.expr.Expression)

CLSS public org.codehaus.groovy.classgen.GeneratorContext
cons public init(org.codehaus.groovy.ast.CompileUnit)
cons public init(org.codehaus.groovy.ast.CompileUnit,int)
meth public int getNextInnerClassIdx()
meth public java.lang.String getNextClosureInnerName(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.MethodNode)
meth public org.codehaus.groovy.ast.CompileUnit getCompileUnit()
meth public static java.lang.String encodeAsValidClassName(java.lang.String)
supr java.lang.Object
hfds CHARACTERS_TO_ENCODE,MAX_ENCODING,MIN_ENCODING,closureClassIdx,compileUnit,innerClassIdx

CLSS public org.codehaus.groovy.classgen.InnerClassCompletionVisitor
cons public init(org.codehaus.groovy.control.CompilationUnit,org.codehaus.groovy.control.SourceUnit)
intf groovyjarjarasm.asm.Opcodes
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public void visitConstructor(org.codehaus.groovy.ast.ConstructorNode)
supr org.codehaus.groovy.classgen.InnerClassVisitorHelper
hfds CLOSURE_DESCRIPTOR,CLOSURE_INTERNAL_NAME,classNode,sourceUnit,thisField

CLSS public org.codehaus.groovy.classgen.InnerClassVisitor
cons public init(org.codehaus.groovy.control.CompilationUnit,org.codehaus.groovy.control.SourceUnit)
intf groovyjarjarasm.asm.Opcodes
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth protected void visitConstructorOrMethod(org.codehaus.groovy.ast.MethodNode,boolean)
meth protected void visitObjectInitializerStatements(org.codehaus.groovy.ast.ClassNode)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public void visitConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth public void visitField(org.codehaus.groovy.ast.FieldNode)
meth public void visitProperty(org.codehaus.groovy.ast.PropertyNode)
supr org.codehaus.groovy.classgen.InnerClassVisitorHelper
hfds PUBLIC_SYNTHETIC,classNode,currentField,currentMethod,inClosure,processingObjInitStatements,sourceUnit,thisField

CLSS public abstract org.codehaus.groovy.classgen.InnerClassVisitorHelper
cons public init()
meth protected static boolean isStatic(org.codehaus.groovy.ast.InnerClassNode)
meth protected static boolean shouldHandleImplicitThisForInnerClass(org.codehaus.groovy.ast.ClassNode)
meth protected static int getObjectDistance(org.codehaus.groovy.ast.ClassNode)
meth protected static org.codehaus.groovy.ast.ClassNode getClassNode(org.codehaus.groovy.ast.ClassNode,boolean)
meth protected static void addFieldInit(org.codehaus.groovy.ast.Parameter,org.codehaus.groovy.ast.FieldNode,org.codehaus.groovy.ast.stmt.BlockStatement)
meth protected static void setMethodDispatcherCode(org.codehaus.groovy.ast.stmt.BlockStatement,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.Parameter[])
meth protected static void setPropertyGetterDispatcher(org.codehaus.groovy.ast.stmt.BlockStatement,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.Parameter[])
meth protected static void setPropertySetterDispatcher(org.codehaus.groovy.ast.stmt.BlockStatement,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.Parameter[])
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport

CLSS public org.codehaus.groovy.classgen.ReturnAdder
cons public init()
cons public init(org.codehaus.groovy.classgen.ReturnAdder$ReturnStatementListener)
innr public abstract interface static ReturnStatementListener
meth public static void addReturnIfNeeded(org.codehaus.groovy.ast.MethodNode)
 anno 0 java.lang.Deprecated()
meth public void visitMethod(org.codehaus.groovy.ast.MethodNode)
supr java.lang.Object
hfds DEFAULT_LISTENER,doAdd,listener

CLSS public abstract interface static org.codehaus.groovy.classgen.ReturnAdder$ReturnStatementListener
 outer org.codehaus.groovy.classgen.ReturnAdder
meth public abstract void returnStatementAdded(org.codehaus.groovy.ast.stmt.ReturnStatement)

CLSS public org.codehaus.groovy.classgen.VariableScopeVisitor
cons public init(org.codehaus.groovy.control.SourceUnit)
cons public init(org.codehaus.groovy.control.SourceUnit,boolean)
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth protected void visitConstructorOrMethod(org.codehaus.groovy.ast.MethodNode,boolean)
meth public void prepareVisit(org.codehaus.groovy.ast.ClassNode)
meth public void visitAnnotations(org.codehaus.groovy.ast.AnnotatedNode)
meth public void visitBinaryExpression(org.codehaus.groovy.ast.expr.BinaryExpression)
meth public void visitBlockStatement(org.codehaus.groovy.ast.stmt.BlockStatement)
meth public void visitCatchStatement(org.codehaus.groovy.ast.stmt.CatchStatement)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public void visitConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth public void visitDeclarationExpression(org.codehaus.groovy.ast.expr.DeclarationExpression)
meth public void visitField(org.codehaus.groovy.ast.FieldNode)
meth public void visitFieldExpression(org.codehaus.groovy.ast.expr.FieldExpression)
meth public void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement)
meth public void visitIfElse(org.codehaus.groovy.ast.stmt.IfStatement)
meth public void visitMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression)
meth public void visitProperty(org.codehaus.groovy.ast.PropertyNode)
meth public void visitPropertyExpression(org.codehaus.groovy.ast.expr.PropertyExpression)
meth public void visitVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds currentClass,currentScope,headScope,inConstructor,isSpecialConstructorCall,recurseInnerClasses,source,stateStack
hcls StateStackElement

CLSS public org.codehaus.groovy.classgen.Verifier
cons public init()
fld public final static java.lang.String DEFAULT_PARAMETER_GENERATED = "DEFAULT_PARAMETER_GENERATED"
fld public final static java.lang.String INITIAL_EXPRESSION = "INITIAL_EXPRESSION"
fld public final static java.lang.String STATIC_METACLASS_BOOL = "__$stMC"
fld public final static java.lang.String SWAP_INIT = "__$swapInit"
fld public final static java.lang.String __TIMESTAMP = "__timeStamp"
fld public final static java.lang.String __TIMESTAMP__ = "__timeStamp__239_neverHappen"
innr public abstract interface static DefaultArgsAction
intf groovyjarjarasm.asm.Opcodes
intf org.codehaus.groovy.ast.GroovyClassVisitor
meth protected org.codehaus.groovy.ast.MethodNode addMethod(org.codehaus.groovy.ast.ClassNode,boolean,java.lang.String,int,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.Parameter[],org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.stmt.Statement)
meth protected org.codehaus.groovy.ast.stmt.Statement createGetterBlock(org.codehaus.groovy.ast.PropertyNode,org.codehaus.groovy.ast.FieldNode)
meth protected org.codehaus.groovy.ast.stmt.Statement createSetterBlock(org.codehaus.groovy.ast.PropertyNode,org.codehaus.groovy.ast.FieldNode)
meth protected org.codehaus.groovy.classgen.FinalVariableAnalyzer$VariableNotFinalCallback getFinalVariablesCallback()
meth protected void addClosureCode(org.codehaus.groovy.ast.InnerClassNode)
meth protected void addConstructor(org.codehaus.groovy.ast.Parameter[],org.codehaus.groovy.ast.ConstructorNode,org.codehaus.groovy.ast.stmt.Statement,org.codehaus.groovy.ast.ClassNode)
meth protected void addCovariantMethods(org.codehaus.groovy.ast.ClassNode)
meth protected void addDefaultConstructor(org.codehaus.groovy.ast.ClassNode)
meth protected void addDefaultParameterConstructors(org.codehaus.groovy.ast.ClassNode)
meth protected void addDefaultParameterMethods(org.codehaus.groovy.ast.ClassNode)
meth protected void addDefaultParameters(java.util.List,org.codehaus.groovy.classgen.Verifier$DefaultArgsAction)
meth protected void addDefaultParameters(org.codehaus.groovy.classgen.Verifier$DefaultArgsAction,org.codehaus.groovy.ast.MethodNode)
meth protected void addFieldInitialization(java.util.List,java.util.List,org.codehaus.groovy.ast.FieldNode,boolean,java.util.List,java.util.Set)
meth protected void addGroovyObjectInterfaceAndMethods(org.codehaus.groovy.ast.ClassNode,java.lang.String)
meth protected void addInitialization(org.codehaus.groovy.ast.ClassNode)
meth protected void addInitialization(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ConstructorNode)
meth protected void addPropertyMethod(org.codehaus.groovy.ast.MethodNode)
meth protected void addReturnIfNeeded(org.codehaus.groovy.ast.MethodNode)
meth protected void addTimeStamp(org.codehaus.groovy.ast.ClassNode)
 anno 0 java.lang.Deprecated()
meth protected void setClassNode(org.codehaus.groovy.ast.ClassNode)
meth public org.codehaus.groovy.ast.ClassNode getClassNode()
meth public org.codehaus.groovy.ast.MethodNode getMethodNode()
meth public static java.lang.Long getTimestampFromFieldName(java.lang.String)
meth public static java.lang.String capitalize(java.lang.String)
meth public static long getTimestamp(java.lang.Class)
meth public static org.codehaus.groovy.ast.expr.ConstantExpression transformToPrimitiveConstantIfPossible(org.codehaus.groovy.ast.expr.ConstantExpression)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public void visitConstructor(org.codehaus.groovy.ast.ConstructorNode)
meth public void visitField(org.codehaus.groovy.ast.FieldNode)
meth public void visitGenericType(org.codehaus.groovy.ast.GenericsType)
meth public void visitMethod(org.codehaus.groovy.ast.MethodNode)
meth public void visitProperty(org.codehaus.groovy.ast.PropertyNode)
supr java.lang.Object
hfds GENERATED_ANNOTATION,GET_PROPERTY_PARAMS,INVOKE_METHOD_PARAMS,SET_METACLASS_PARAMS,SET_PROPERTY_PARAMS,classNode,methodNode
hcls SwapInitStatement

CLSS public abstract interface static org.codehaus.groovy.classgen.Verifier$DefaultArgsAction
 outer org.codehaus.groovy.classgen.Verifier
meth public abstract void call(org.codehaus.groovy.ast.expr.ArgumentListExpression,org.codehaus.groovy.ast.Parameter[],org.codehaus.groovy.ast.MethodNode)

CLSS public org.codehaus.groovy.classgen.VerifierCodeVisitor
intf groovyjarjarasm.asm.Opcodes
meth public static void assertValidIdentifier(java.lang.String,java.lang.String,org.codehaus.groovy.ast.ASTNode)
meth public void visitConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth public void visitFieldExpression(org.codehaus.groovy.ast.expr.FieldExpression)
meth public void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement)
meth public void visitListExpression(org.codehaus.groovy.ast.expr.ListExpression)
meth public void visitVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
supr org.codehaus.groovy.ast.CodeVisitorSupport
hfds verifier

CLSS public org.codehaus.groovy.classgen.genArrayAccess
cons public init()
cons public init(groovy.lang.Binding)
meth public !varargs static void main(java.lang.String[])
meth public java.lang.Object genInners()
meth public java.lang.Object run()
supr groovy.lang.Script

CLSS public final org.codehaus.groovy.classgen.genArrayAccess$_genInners_closure1
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object call(java.lang.Object,java.lang.Object)
meth public java.lang.Object doCall(java.lang.Object,java.lang.Object)
meth public java.lang.Object getRes()
supr groovy.lang.Closure

CLSS public org.codehaus.groovy.classgen.genArrays
cons public init()
cons public init(groovy.lang.Binding)
meth public !varargs static void main(java.lang.String[])
meth public java.lang.Object genMethod(int)
meth public java.lang.Object genMethods()
meth public java.lang.Object run()
supr groovy.lang.Script

CLSS public org.codehaus.groovy.classgen.genDgmMath
cons public init()
cons public init(groovy.lang.Binding)
meth public !varargs static void main(java.lang.String[])
meth public java.lang.Object getMath(java.lang.Object,java.lang.Object)
meth public java.lang.Object run()
supr groovy.lang.Script

CLSS public final org.codehaus.groovy.classgen.genDgmMath$_run_closure1
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object doCall(java.lang.Object)
meth public java.lang.Object getTypes()
supr groovy.lang.Closure

CLSS public final org.codehaus.groovy.classgen.genDgmMath$_run_closure1$_closure2
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object doCall(java.lang.Object)
meth public java.lang.Object getA()
supr groovy.lang.Closure

CLSS public org.codehaus.groovy.classgen.genMathModification
cons public init()
cons public init(groovy.lang.Binding)
meth public !varargs static void main(java.lang.String[])
meth public java.lang.Object getMath(java.lang.Object,java.lang.Object)
meth public java.lang.Object isFloatingPoint(java.lang.Object)
meth public java.lang.Object isLong(java.lang.Object)
meth public java.lang.Object run()
supr groovy.lang.Script

CLSS public final org.codehaus.groovy.classgen.genMathModification$_run_closure1
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object doCall(java.lang.Object)
meth public java.lang.Object getNumbers()
supr groovy.lang.Closure

CLSS public final org.codehaus.groovy.classgen.genMathModification$_run_closure1$_closure4
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object call(java.lang.Object,java.lang.Object)
meth public java.lang.Object doCall(java.lang.Object,java.lang.Object)
meth public java.lang.Object getOp()
supr groovy.lang.Closure

CLSS public final org.codehaus.groovy.classgen.genMathModification$_run_closure2
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object doCall(java.lang.Object)
meth public java.lang.Object getNumbers()
supr groovy.lang.Closure

CLSS public final org.codehaus.groovy.classgen.genMathModification$_run_closure2$_closure5
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object call(java.lang.Object,java.lang.Object)
meth public java.lang.Object doCall(java.lang.Object,java.lang.Object)
meth public java.lang.Object getOp()
supr groovy.lang.Closure

CLSS public final org.codehaus.groovy.classgen.genMathModification$_run_closure2$_closure6
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object call(java.lang.Object,java.lang.Object)
meth public java.lang.Object doCall(java.lang.Object,java.lang.Object)
meth public java.lang.Object getOp()
supr groovy.lang.Closure

CLSS public final org.codehaus.groovy.classgen.genMathModification$_run_closure3
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object doCall(java.lang.Object)
meth public java.lang.Object getNumbers()
supr groovy.lang.Closure

CLSS public final org.codehaus.groovy.classgen.genMathModification$_run_closure3$_closure7
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object call(java.lang.Object,java.lang.Object)
meth public java.lang.Object doCall(java.lang.Object,java.lang.Object)
meth public java.lang.Object getNumbers()
meth public java.lang.Object getOp()
supr groovy.lang.Closure

CLSS public final org.codehaus.groovy.classgen.genMathModification$_run_closure3$_closure7$_closure8
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference,groovy.lang.Reference,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object call(java.lang.Object,java.lang.Object)
meth public java.lang.Object doCall(java.lang.Object,java.lang.Object)
meth public java.lang.Object getOp()
meth public java.lang.Object getType1()
meth public java.lang.Object getWrappedType1()
supr groovy.lang.Closure

CLSS public org.codehaus.groovy.control.ASTTransformationsContext
cons public init(org.codehaus.groovy.control.CompilationUnit,groovy.lang.GroovyClassLoader)
fld protected final groovy.lang.GroovyClassLoader transformLoader
fld protected final java.util.Set<java.lang.String> globalTransformNames
fld protected final org.codehaus.groovy.control.CompilationUnit compilationUnit
meth public groovy.lang.GroovyClassLoader getTransformLoader()
meth public java.util.Set<java.lang.String> getGlobalTransformNames()
meth public org.codehaus.groovy.control.CompilationUnit getCompilationUnit()
supr java.lang.Object

CLSS public org.codehaus.groovy.control.AnnotationConstantsVisitor
cons public init()
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth protected void visitConstructorOrMethod(org.codehaus.groovy.ast.MethodNode,boolean)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds inAnnotationDef,source

CLSS public abstract interface org.codehaus.groovy.control.BytecodeProcessor
meth public abstract byte[] processBytecode(java.lang.String,byte[])

CLSS public org.codehaus.groovy.control.ClassNodeResolver
cons public init()
fld protected final static org.codehaus.groovy.ast.ClassNode NO_CLASS
innr public static LookupResult
meth public org.codehaus.groovy.ast.ClassNode getFromClassCache(java.lang.String)
meth public org.codehaus.groovy.control.ClassNodeResolver$LookupResult findClassNode(java.lang.String,org.codehaus.groovy.control.CompilationUnit)
meth public org.codehaus.groovy.control.ClassNodeResolver$LookupResult resolveName(java.lang.String,org.codehaus.groovy.control.CompilationUnit)
meth public void cacheClass(java.lang.String,org.codehaus.groovy.ast.ClassNode)
supr java.lang.Object
hfds cachedClasses

CLSS public static org.codehaus.groovy.control.ClassNodeResolver$LookupResult
 outer org.codehaus.groovy.control.ClassNodeResolver
cons public init(org.codehaus.groovy.control.SourceUnit,org.codehaus.groovy.ast.ClassNode)
meth public boolean isClassNode()
meth public boolean isSourceUnit()
meth public org.codehaus.groovy.ast.ClassNode getClassNode()
meth public org.codehaus.groovy.control.SourceUnit getSourceUnit()
supr java.lang.Object
hfds cn,su

CLSS public org.codehaus.groovy.control.CompilationFailedException
cons public init(int,org.codehaus.groovy.control.ProcessingUnit)
cons public init(int,org.codehaus.groovy.control.ProcessingUnit,java.lang.Throwable)
fld protected int phase
fld protected org.codehaus.groovy.control.ProcessingUnit unit
meth public org.codehaus.groovy.control.ProcessingUnit getUnit()
supr groovy.lang.GroovyRuntimeException
hfds serialVersionUID

CLSS public org.codehaus.groovy.control.CompilationUnit
cons public init()
cons public init(groovy.lang.GroovyClassLoader)
cons public init(org.codehaus.groovy.control.CompilerConfiguration)
cons public init(org.codehaus.groovy.control.CompilerConfiguration,java.security.CodeSource,groovy.lang.GroovyClassLoader)
cons public init(org.codehaus.groovy.control.CompilerConfiguration,java.security.CodeSource,groovy.lang.GroovyClassLoader,groovy.lang.GroovyClassLoader)
fld protected boolean configured
fld protected boolean debug
fld protected java.util.LinkedList<org.codehaus.groovy.control.SourceUnit> queuedSources
fld protected java.util.List<java.lang.String> names
fld protected java.util.List<org.codehaus.groovy.tools.GroovyClass> generatedClasses
fld protected java.util.Map classSourcesByPublicClassName
fld protected java.util.Map summariesByPublicClassName
fld protected java.util.Map summariesBySourceName
fld protected java.util.Map<java.lang.String,org.codehaus.groovy.control.SourceUnit> sources
fld protected org.codehaus.groovy.ast.CompileUnit ast
fld protected org.codehaus.groovy.classgen.Verifier verifier
fld protected org.codehaus.groovy.control.ASTTransformationsContext astTransformationsContext
fld protected org.codehaus.groovy.control.ClassNodeResolver classNodeResolver
fld protected org.codehaus.groovy.control.CompilationUnit$ClassgenCallback classgenCallback
fld protected org.codehaus.groovy.control.CompilationUnit$ProgressCallback progressCallback
fld protected org.codehaus.groovy.control.OptimizerVisitor optimizer
fld protected org.codehaus.groovy.control.ResolveVisitor resolveVisitor
fld protected org.codehaus.groovy.control.StaticImportVisitor staticImportVisitor
innr public abstract static ClassgenCallback
innr public abstract static GroovyClassOperation
innr public abstract static PrimaryClassNodeOperation
innr public abstract static ProgressCallback
innr public abstract static SourceUnitOperation
meth protected boolean dequeued()
meth protected groovyjarjarasm.asm.ClassVisitor createClassVisitor()
meth protected void mark()
meth public boolean isPublicClass(java.lang.String)
meth public groovy.lang.GroovyClassLoader getTransformLoader()
meth public java.util.Iterator<org.codehaus.groovy.control.SourceUnit> iterator()
meth public java.util.List getClasses()
meth public java.util.Map getClassSourcesByPublicClassName()
meth public java.util.Map getSummariesByPublicClassName()
meth public java.util.Map getSummariesBySourceName()
meth public org.codehaus.groovy.ast.ClassNode getClassNode(java.lang.String)
meth public org.codehaus.groovy.ast.ClassNode getFirstClassNode()
meth public org.codehaus.groovy.ast.CompileUnit getAST()
meth public org.codehaus.groovy.control.ASTTransformationsContext getASTTransformationsContext()
meth public org.codehaus.groovy.control.ClassNodeResolver getClassNodeResolver()
meth public org.codehaus.groovy.control.CompilationUnit$ClassgenCallback getClassgenCallback()
meth public org.codehaus.groovy.control.CompilationUnit$ProgressCallback getProgressCallback()
meth public org.codehaus.groovy.control.SourceUnit addSource(java.io.File)
meth public org.codehaus.groovy.control.SourceUnit addSource(java.lang.String,java.io.InputStream)
meth public org.codehaus.groovy.control.SourceUnit addSource(java.lang.String,java.lang.String)
meth public org.codehaus.groovy.control.SourceUnit addSource(java.net.URL)
meth public org.codehaus.groovy.control.SourceUnit addSource(org.codehaus.groovy.control.SourceUnit)
meth public void addClassNode(org.codehaus.groovy.ast.ClassNode)
meth public void addFirstPhaseOperation(org.codehaus.groovy.control.CompilationUnit$PrimaryClassNodeOperation,int)
meth public void addNewPhaseOperation(org.codehaus.groovy.control.CompilationUnit$SourceUnitOperation,int)
meth public void addPhaseOperation(org.codehaus.groovy.control.CompilationUnit$GroovyClassOperation)
meth public void addPhaseOperation(org.codehaus.groovy.control.CompilationUnit$PrimaryClassNodeOperation,int)
meth public void addPhaseOperation(org.codehaus.groovy.control.CompilationUnit$SourceUnitOperation,int)
meth public void addSources(java.io.File[])
meth public void addSources(java.lang.String[])
meth public void applyToGeneratedGroovyClasses(org.codehaus.groovy.control.CompilationUnit$GroovyClassOperation)
meth public void applyToPrimaryClassNodes(org.codehaus.groovy.control.CompilationUnit$PrimaryClassNodeOperation)
meth public void applyToSourceUnits(org.codehaus.groovy.control.CompilationUnit$SourceUnitOperation)
meth public void compile()
meth public void compile(int)
meth public void configure(org.codehaus.groovy.control.CompilerConfiguration)
meth public void setClassNodeResolver(org.codehaus.groovy.control.ClassNodeResolver)
meth public void setClassgenCallback(org.codehaus.groovy.control.CompilationUnit$ClassgenCallback)
meth public void setProgressCallback(org.codehaus.groovy.control.CompilationUnit$ProgressCallback)
supr org.codehaus.groovy.control.ProcessingUnit
hfds classgen,compileCompleteCheck,convert,mark,newPhaseOperations,output,phaseOperations,resolve,staticImport

CLSS public abstract static org.codehaus.groovy.control.CompilationUnit$ClassgenCallback
 outer org.codehaus.groovy.control.CompilationUnit
cons public init()
meth public abstract void call(groovyjarjarasm.asm.ClassVisitor,org.codehaus.groovy.ast.ClassNode)
supr java.lang.Object

CLSS public abstract static org.codehaus.groovy.control.CompilationUnit$GroovyClassOperation
 outer org.codehaus.groovy.control.CompilationUnit
cons public init()
meth public abstract void call(org.codehaus.groovy.tools.GroovyClass)
supr java.lang.Object

CLSS public abstract static org.codehaus.groovy.control.CompilationUnit$PrimaryClassNodeOperation
 outer org.codehaus.groovy.control.CompilationUnit
cons public init()
meth public abstract void call(org.codehaus.groovy.control.SourceUnit,org.codehaus.groovy.classgen.GeneratorContext,org.codehaus.groovy.ast.ClassNode)
meth public boolean needSortedInput()
supr java.lang.Object

CLSS public abstract static org.codehaus.groovy.control.CompilationUnit$ProgressCallback
 outer org.codehaus.groovy.control.CompilationUnit
cons public init()
meth public abstract void call(org.codehaus.groovy.control.ProcessingUnit,int)
supr java.lang.Object

CLSS public abstract static org.codehaus.groovy.control.CompilationUnit$SourceUnitOperation
 outer org.codehaus.groovy.control.CompilationUnit
cons public init()
meth public abstract void call(org.codehaus.groovy.control.SourceUnit)
supr java.lang.Object

CLSS public final !enum org.codehaus.groovy.control.CompilePhase
fld public final static org.codehaus.groovy.control.CompilePhase CANONICALIZATION
fld public final static org.codehaus.groovy.control.CompilePhase CLASS_GENERATION
fld public final static org.codehaus.groovy.control.CompilePhase CONVERSION
fld public final static org.codehaus.groovy.control.CompilePhase FINALIZATION
fld public final static org.codehaus.groovy.control.CompilePhase INITIALIZATION
fld public final static org.codehaus.groovy.control.CompilePhase INSTRUCTION_SELECTION
fld public final static org.codehaus.groovy.control.CompilePhase OUTPUT
fld public final static org.codehaus.groovy.control.CompilePhase PARSING
fld public final static org.codehaus.groovy.control.CompilePhase SEMANTIC_ANALYSIS
fld public static org.codehaus.groovy.control.CompilePhase[] phases
meth public int getPhaseNumber()
meth public static org.codehaus.groovy.control.CompilePhase fromPhaseNumber(int)
meth public static org.codehaus.groovy.control.CompilePhase valueOf(java.lang.String)
meth public static org.codehaus.groovy.control.CompilePhase[] values()
supr java.lang.Enum<org.codehaus.groovy.control.CompilePhase>
hfds phaseNumber

CLSS public org.codehaus.groovy.control.CompilerConfiguration
cons public init()
cons public init(java.util.Properties)
cons public init(org.codehaus.groovy.control.CompilerConfiguration)
fld public final static int ASM_API_VERSION = 393216
fld public final static java.lang.String CURRENT_JVM_VERSION
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String DEFAULT_SOURCE_ENCODING = "UTF-8"
fld public final static java.lang.String INVOKEDYNAMIC = "indy"
fld public final static java.lang.String JDK10 = "10"
fld public final static java.lang.String JDK11 = "11"
fld public final static java.lang.String JDK4 = "1.4"
fld public final static java.lang.String JDK5 = "1.5"
fld public final static java.lang.String JDK6 = "1.6"
fld public final static java.lang.String JDK7 = "1.7"
fld public final static java.lang.String JDK8 = "1.8"
fld public final static java.lang.String JDK9 = "9"
fld public final static java.lang.String POST_JDK5 = "1.5"
fld public final static java.lang.String PRE_JDK5 = "1.4"
fld public final static java.lang.String[] ALLOWED_JDKS
fld public final static org.codehaus.groovy.control.CompilerConfiguration DEFAULT
meth public !varargs org.codehaus.groovy.control.CompilerConfiguration addCompilationCustomizers(org.codehaus.groovy.control.customizers.CompilationCustomizer[])
meth public boolean getDebug()
meth public boolean getParameters()
meth public boolean getRecompileGroovySource()
meth public boolean getVerbose()
meth public boolean isIndyEnabled()
meth public int getMinimumRecompilationInterval()
meth public int getTolerance()
meth public int getWarningLevel()
meth public java.io.File getTargetDirectory()
meth public java.io.PrintWriter getOutput()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getDefaultScriptExtension()
meth public java.lang.String getScriptBaseClass()
meth public java.lang.String getSourceEncoding()
meth public java.lang.String getTargetBytecode()
meth public java.util.List<java.lang.String> getClasspath()
meth public java.util.List<org.codehaus.groovy.control.customizers.CompilationCustomizer> getCompilationCustomizers()
meth public java.util.Map<java.lang.String,java.lang.Boolean> getOptimizationOptions()
meth public java.util.Map<java.lang.String,java.lang.Object> getJointCompilationOptions()
meth public java.util.Set<java.lang.String> getDisabledGlobalASTTransformations()
meth public java.util.Set<java.lang.String> getScriptExtensions()
meth public org.codehaus.groovy.control.BytecodeProcessor getBytecodePostprocessor()
meth public org.codehaus.groovy.control.ParserPluginFactory getPluginFactory()
meth public static boolean isPostJDK5(java.lang.String)
meth public static boolean isPostJDK7(java.lang.String)
meth public static boolean isPostJDK8(java.lang.String)
meth public static boolean isPostJDK9(java.lang.String)
meth public void configure(java.util.Properties)
meth public void setBytecodePostprocessor(org.codehaus.groovy.control.BytecodeProcessor)
meth public void setClasspath(java.lang.String)
meth public void setClasspathList(java.util.List<java.lang.String>)
meth public void setDebug(boolean)
meth public void setDefaultScriptExtension(java.lang.String)
meth public void setDisabledGlobalASTTransformations(java.util.Set<java.lang.String>)
meth public void setJointCompilationOptions(java.util.Map<java.lang.String,java.lang.Object>)
meth public void setMinimumRecompilationInterval(int)
meth public void setOptimizationOptions(java.util.Map<java.lang.String,java.lang.Boolean>)
meth public void setOutput(java.io.PrintWriter)
 anno 0 java.lang.Deprecated()
meth public void setParameters(boolean)
meth public void setPluginFactory(org.codehaus.groovy.control.ParserPluginFactory)
meth public void setRecompileGroovySource(boolean)
meth public void setScriptBaseClass(java.lang.String)
meth public void setScriptExtensions(java.util.Set<java.lang.String>)
meth public void setSourceEncoding(java.lang.String)
meth public void setTargetBytecode(java.lang.String)
meth public void setTargetDirectory(java.io.File)
meth public void setTargetDirectory(java.lang.String)
meth public void setTolerance(int)
meth public void setVerbose(boolean)
meth public void setWarningLevel(int)
supr java.lang.Object
hfds bytecodePostprocessor,classpath,compilationCustomizers,debug,defaultScriptExtension,disabledGlobalASTTransformations,jointCompilationOptions,minimumRecompilationInterval,optimizationOptions,output,parameters,pluginFactory,recompileGroovySource,scriptBaseClass,scriptExtensions,sourceEncoding,targetBytecode,targetDirectory,tolerance,verbose,warningLevel

CLSS public org.codehaus.groovy.control.ConfigurationException
cons public init(java.lang.Exception)
cons public init(java.lang.String)
fld protected java.lang.Exception cause
intf org.codehaus.groovy.GroovyExceptionInterface
meth public boolean isFatal()
meth public java.lang.Throwable getCause()
meth public void setFatal(boolean)
supr java.lang.RuntimeException
hfds serialVersionUID

CLSS public org.codehaus.groovy.control.ErrorCollector
cons public init(org.codehaus.groovy.control.CompilerConfiguration)
fld protected java.util.LinkedList errors
fld protected java.util.LinkedList warnings
fld protected org.codehaus.groovy.control.CompilerConfiguration configuration
intf java.io.Serializable
meth protected void failIfErrors()
meth public boolean hasErrors()
meth public boolean hasWarnings()
meth public int getErrorCount()
meth public int getWarningCount()
meth public java.lang.Exception getException(int)
meth public java.util.List getErrors()
meth public java.util.List getWarnings()
meth public org.codehaus.groovy.control.CompilerConfiguration getConfiguration()
meth public org.codehaus.groovy.control.messages.Message getError(int)
meth public org.codehaus.groovy.control.messages.Message getLastError()
meth public org.codehaus.groovy.control.messages.WarningMessage getWarning(int)
meth public org.codehaus.groovy.syntax.SyntaxException getSyntaxError(int)
meth public void addCollectorContents(org.codehaus.groovy.control.ErrorCollector)
meth public void addError(java.lang.String,org.codehaus.groovy.syntax.CSTNode,org.codehaus.groovy.control.SourceUnit)
meth public void addError(org.codehaus.groovy.control.messages.Message)
meth public void addError(org.codehaus.groovy.control.messages.Message,boolean)
meth public void addError(org.codehaus.groovy.syntax.SyntaxException,org.codehaus.groovy.control.SourceUnit)
meth public void addErrorAndContinue(org.codehaus.groovy.control.messages.Message)
meth public void addErrorAndContinue(org.codehaus.groovy.syntax.SyntaxException,org.codehaus.groovy.control.SourceUnit)
meth public void addException(java.lang.Exception,org.codehaus.groovy.control.SourceUnit)
meth public void addFatalError(org.codehaus.groovy.control.messages.Message)
meth public void addWarning(int,java.lang.String,java.lang.Object,org.codehaus.groovy.syntax.CSTNode,org.codehaus.groovy.control.SourceUnit)
meth public void addWarning(int,java.lang.String,org.codehaus.groovy.syntax.CSTNode,org.codehaus.groovy.control.SourceUnit)
meth public void addWarning(org.codehaus.groovy.control.messages.WarningMessage)
meth public void write(java.io.PrintWriter,org.codehaus.groovy.control.Janitor)
supr java.lang.Object
hfds serialVersionUID

CLSS public org.codehaus.groovy.control.GenericsVisitor
cons public init(org.codehaus.groovy.control.SourceUnit)
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public void visitConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth public void visitDeclarationExpression(org.codehaus.groovy.ast.expr.DeclarationExpression)
meth public void visitField(org.codehaus.groovy.ast.FieldNode)
meth public void visitMethod(org.codehaus.groovy.ast.MethodNode)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds source

CLSS public abstract interface org.codehaus.groovy.control.HasCleanup
meth public abstract void cleanup()

CLSS public org.codehaus.groovy.control.Janitor
cons public init()
intf org.codehaus.groovy.control.HasCleanup
meth public void cleanup()
meth public void register(org.codehaus.groovy.control.HasCleanup)
supr java.lang.Object
hfds pending

CLSS public org.codehaus.groovy.control.LabelVerifier
cons public init(org.codehaus.groovy.control.SourceUnit)
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth protected void assertNoLabelsMissed()
meth protected void visitClassCodeContainer(org.codehaus.groovy.ast.stmt.Statement)
meth public void visitBreakStatement(org.codehaus.groovy.ast.stmt.BreakStatement)
meth public void visitContinueStatement(org.codehaus.groovy.ast.stmt.ContinueStatement)
meth public void visitDoWhileLoop(org.codehaus.groovy.ast.stmt.DoWhileStatement)
meth public void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement)
meth public void visitStatement(org.codehaus.groovy.ast.stmt.Statement)
meth public void visitSwitch(org.codehaus.groovy.ast.stmt.SwitchStatement)
meth public void visitWhileLoop(org.codehaus.groovy.ast.stmt.WhileStatement)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds breakLabels,continueLabels,inLoop,inSwitch,source,visitedLabels

CLSS public org.codehaus.groovy.control.MultipleCompilationErrorsException
cons public init(org.codehaus.groovy.control.ErrorCollector)
fld protected org.codehaus.groovy.control.ErrorCollector collector
meth public java.lang.String getMessage()
meth public org.codehaus.groovy.control.ErrorCollector getErrorCollector()
supr org.codehaus.groovy.control.CompilationFailedException
hfds serialVersionUID

CLSS public org.codehaus.groovy.control.OptimizerVisitor
cons public init(org.codehaus.groovy.control.CompilationUnit)
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth public org.codehaus.groovy.ast.expr.Expression transform(org.codehaus.groovy.ast.expr.Expression)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.control.SourceUnit)
meth public void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
supr org.codehaus.groovy.ast.ClassCodeExpressionTransformer
hfds const2Objects,const2Prims,currentClass,index,missingFields,source

CLSS public abstract interface org.codehaus.groovy.control.ParserPlugin
meth public abstract org.codehaus.groovy.ast.ModuleNode buildAST(org.codehaus.groovy.control.SourceUnit,java.lang.ClassLoader,org.codehaus.groovy.syntax.Reduction) throws org.codehaus.groovy.syntax.ParserException
meth public abstract org.codehaus.groovy.syntax.Reduction parseCST(org.codehaus.groovy.control.SourceUnit,java.io.Reader)

CLSS public abstract org.codehaus.groovy.control.ParserPluginFactory
cons public init()
meth public abstract org.codehaus.groovy.control.ParserPlugin createParserPlugin()
meth public static org.codehaus.groovy.control.ParserPluginFactory newInstance()
meth public static org.codehaus.groovy.control.ParserPluginFactory newInstance(boolean)
 anno 0 java.lang.Deprecated()
supr java.lang.Object

CLSS public org.codehaus.groovy.control.Phases
cons public init()
fld public final static int ALL = 9
fld public final static int CANONICALIZATION = 5
fld public final static int CLASS_GENERATION = 7
fld public final static int CONVERSION = 3
fld public final static int FINALIZATION = 9
fld public final static int INITIALIZATION = 1
fld public final static int INSTRUCTION_SELECTION = 6
fld public final static int OUTPUT = 8
fld public final static int PARSING = 2
fld public final static int SEMANTIC_ANALYSIS = 4
fld public final static java.lang.String[] descriptions
meth public static java.lang.String getDescription(int)
supr java.lang.Object

CLSS public abstract org.codehaus.groovy.control.ProcessingUnit
cons public init(org.codehaus.groovy.control.CompilerConfiguration,groovy.lang.GroovyClassLoader,org.codehaus.groovy.control.ErrorCollector)
fld protected boolean phaseComplete
fld protected groovy.lang.GroovyClassLoader classLoader
fld protected int phase
fld protected org.codehaus.groovy.control.CompilerConfiguration configuration
fld protected org.codehaus.groovy.control.ErrorCollector errorCollector
meth public groovy.lang.GroovyClassLoader getClassLoader()
meth public int getPhase()
meth public java.lang.String getPhaseDescription()
meth public org.codehaus.groovy.control.CompilerConfiguration getConfiguration()
meth public org.codehaus.groovy.control.ErrorCollector getErrorCollector()
meth public void completePhase()
meth public void configure(org.codehaus.groovy.control.CompilerConfiguration)
meth public void gotoPhase(int)
meth public void nextPhase()
meth public void setClassLoader(groovy.lang.GroovyClassLoader)
meth public void setConfiguration(org.codehaus.groovy.control.CompilerConfiguration)
supr java.lang.Object

CLSS public org.codehaus.groovy.control.ResolveVisitor
cons public init(org.codehaus.groovy.control.CompilationUnit)
fld public final static java.lang.String QUESTION_MARK = "?"
fld public final static java.lang.String[] DEFAULT_IMPORTS
meth protected org.codehaus.groovy.ast.expr.Expression transformAnnotationConstantExpression(org.codehaus.groovy.ast.expr.AnnotationConstantExpression)
meth protected org.codehaus.groovy.ast.expr.Expression transformBinaryExpression(org.codehaus.groovy.ast.expr.BinaryExpression)
meth protected org.codehaus.groovy.ast.expr.Expression transformClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth protected org.codehaus.groovy.ast.expr.Expression transformConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth protected org.codehaus.groovy.ast.expr.Expression transformDeclarationExpression(org.codehaus.groovy.ast.expr.DeclarationExpression)
meth protected org.codehaus.groovy.ast.expr.Expression transformMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression)
meth protected org.codehaus.groovy.ast.expr.Expression transformPropertyExpression(org.codehaus.groovy.ast.expr.PropertyExpression)
meth protected org.codehaus.groovy.ast.expr.Expression transformVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth protected void visitConstructorOrMethod(org.codehaus.groovy.ast.MethodNode,boolean)
meth public org.codehaus.groovy.ast.expr.Expression transform(org.codehaus.groovy.ast.expr.Expression)
meth public void setClassNodeResolver(org.codehaus.groovy.control.ClassNodeResolver)
meth public void startResolving(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.control.SourceUnit)
meth public void visitAnnotations(org.codehaus.groovy.ast.AnnotatedNode)
meth public void visitBlockStatement(org.codehaus.groovy.ast.stmt.BlockStatement)
meth public void visitCatchStatement(org.codehaus.groovy.ast.stmt.CatchStatement)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public void visitField(org.codehaus.groovy.ast.FieldNode)
meth public void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement)
meth public void visitProperty(org.codehaus.groovy.ast.PropertyNode)
supr org.codehaus.groovy.ast.ClassCodeExpressionTransformer
hfds BIGDECIMAL_STR,BIGINTEGER_STR,checkingVariableTypeInDeclaration,classNodeResolver,compilationUnit,currImportNode,currentClass,currentMethod,currentScope,fieldTypesChecked,genericParameterNames,inClosure,inPropertyExpression,isTopLevelProperty,source
hcls ConstructedClassWithPackage,ConstructedNestedClass,LowerCaseClass

CLSS public org.codehaus.groovy.control.SourceExtensionHandler
cons public init()
meth public static java.util.Set<java.lang.String> getRegisteredExtensions(java.lang.ClassLoader)
supr java.lang.Object

CLSS public org.codehaus.groovy.control.SourceUnit
cons public init(java.io.File,org.codehaus.groovy.control.CompilerConfiguration,groovy.lang.GroovyClassLoader,org.codehaus.groovy.control.ErrorCollector)
cons public init(java.lang.String,java.lang.String,org.codehaus.groovy.control.CompilerConfiguration,groovy.lang.GroovyClassLoader,org.codehaus.groovy.control.ErrorCollector)
cons public init(java.lang.String,org.codehaus.groovy.control.io.ReaderSource,org.codehaus.groovy.control.CompilerConfiguration,groovy.lang.GroovyClassLoader,org.codehaus.groovy.control.ErrorCollector)
cons public init(java.net.URL,org.codehaus.groovy.control.CompilerConfiguration,groovy.lang.GroovyClassLoader,org.codehaus.groovy.control.ErrorCollector)
fld protected java.lang.String name
fld protected org.codehaus.groovy.ast.ModuleNode ast
fld protected org.codehaus.groovy.control.io.ReaderSource source
fld protected org.codehaus.groovy.syntax.Reduction cst
meth protected boolean isEofToken(groovyjarjarantlr.Token)
meth public boolean failedWithUnexpectedEOF()
meth public java.lang.String getName()
meth public java.lang.String getSample(int,int,org.codehaus.groovy.control.Janitor)
meth public org.codehaus.groovy.ast.ModuleNode getAST()
meth public org.codehaus.groovy.control.io.ReaderSource getSource()
meth public org.codehaus.groovy.syntax.Reduction getCST()
meth public static org.codehaus.groovy.control.SourceUnit create(java.lang.String,java.lang.String)
meth public static org.codehaus.groovy.control.SourceUnit create(java.lang.String,java.lang.String,int)
meth public void addError(org.codehaus.groovy.syntax.SyntaxException)
meth public void addErrorAndContinue(org.codehaus.groovy.syntax.SyntaxException)
meth public void addException(java.lang.Exception)
meth public void convert()
meth public void parse()
supr org.codehaus.groovy.control.ProcessingUnit
hfds parserPlugin

CLSS public org.codehaus.groovy.control.StaticImportVisitor
cons public init()
meth protected org.codehaus.groovy.ast.expr.Expression transformBinaryExpression(org.codehaus.groovy.ast.expr.BinaryExpression)
meth protected org.codehaus.groovy.ast.expr.Expression transformClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth protected org.codehaus.groovy.ast.expr.Expression transformConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth protected org.codehaus.groovy.ast.expr.Expression transformMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression)
meth protected org.codehaus.groovy.ast.expr.Expression transformPropertyExpression(org.codehaus.groovy.ast.expr.PropertyExpression)
meth protected org.codehaus.groovy.ast.expr.Expression transformVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth protected void visitConstructorOrMethod(org.codehaus.groovy.ast.MethodNode,boolean)
meth public org.codehaus.groovy.ast.expr.Expression transform(org.codehaus.groovy.ast.expr.Expression)
meth public void visitAnnotations(org.codehaus.groovy.ast.AnnotatedNode)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.ast.ClassCodeExpressionTransformer
hfds currentClass,currentMethod,foundArgs,foundConstant,inAnnotation,inClosure,inLeftExpression,inPropertyExpression,inSpecialConstructorCall,source

CLSS public org.codehaus.groovy.control.StaticVerifier
cons public init()
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth public void visitClass(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.control.SourceUnit)
meth public void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public void visitConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth public void visitConstructorOrMethod(org.codehaus.groovy.ast.MethodNode,boolean)
meth public void visitMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression)
meth public void visitPropertyExpression(org.codehaus.groovy.ast.expr.PropertyExpression)
meth public void visitVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds currentMethod,inClosure,inPropertyExpression,inSpecialConstructorCall,source

CLSS public abstract org.codehaus.groovy.control.XStreamUtils
cons public init()
meth public static void serialize(java.lang.String,java.lang.Object)
supr java.lang.Object

CLSS public org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
cons public init(java.lang.Class<? extends java.lang.annotation.Annotation>)
cons public init(java.lang.Class<? extends java.lang.annotation.Annotation>,java.lang.ClassLoader)
cons public init(java.lang.Class<? extends java.lang.annotation.Annotation>,java.lang.String)
cons public init(java.lang.Class<? extends java.lang.annotation.Annotation>,java.lang.String,java.lang.ClassLoader)
cons public init(java.util.Map,java.lang.Class<? extends java.lang.annotation.Annotation>)
cons public init(java.util.Map,java.lang.Class<? extends java.lang.annotation.Annotation>,java.lang.ClassLoader)
cons public init(java.util.Map,java.lang.Class<? extends java.lang.annotation.Annotation>,java.lang.String)
cons public init(java.util.Map,java.lang.Class<? extends java.lang.annotation.Annotation>,java.lang.String,java.lang.ClassLoader)
cons public init(java.util.Map,org.codehaus.groovy.transform.ASTTransformation)
cons public init(org.codehaus.groovy.transform.ASTTransformation)
fld protected org.codehaus.groovy.control.CompilationUnit compilationUnit
intf groovy.lang.GroovyObject
intf groovy.transform.CompilationUnitAware
meth public final org.codehaus.groovy.transform.ASTTransformation getTransformation()
meth public void call(org.codehaus.groovy.control.SourceUnit,org.codehaus.groovy.classgen.GeneratorContext,org.codehaus.groovy.ast.ClassNode)
meth public void setAnnotationParameters(java.util.Map<java.lang.String,java.lang.Object>)
meth public void setCompilationUnit(org.codehaus.groovy.control.CompilationUnit)
supr org.codehaus.groovy.control.customizers.CompilationCustomizer
hfds annotationNode,applied,transformation

CLSS public final org.codehaus.groovy.control.customizers.ASTTransformationCustomizer$_setAnnotationParameters_closure1
cons public init(java.lang.Object,java.lang.Object)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object call(java.lang.Object,java.lang.Object)
meth public java.lang.Object doCall(java.lang.Object,java.lang.Object)
supr groovy.lang.Closure

CLSS public final org.codehaus.groovy.control.customizers.ASTTransformationCustomizer$_setAnnotationParameters_closure1$_closure2
cons public init(java.lang.Object,java.lang.Object)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object doCall()
meth public java.lang.Object doCall(java.lang.Object)
supr groovy.lang.Closure

CLSS public abstract org.codehaus.groovy.control.customizers.CompilationCustomizer
cons public init(org.codehaus.groovy.control.CompilePhase)
meth public org.codehaus.groovy.control.CompilePhase getPhase()
supr org.codehaus.groovy.control.CompilationUnit$PrimaryClassNodeOperation
hfds phase

CLSS public abstract org.codehaus.groovy.control.customizers.DelegatingCustomizer
cons public init(org.codehaus.groovy.control.customizers.CompilationCustomizer)
fld protected final org.codehaus.groovy.control.customizers.CompilationCustomizer delegate
meth public void call(org.codehaus.groovy.control.SourceUnit,org.codehaus.groovy.classgen.GeneratorContext,org.codehaus.groovy.ast.ClassNode)
supr org.codehaus.groovy.control.customizers.CompilationCustomizer

CLSS public org.codehaus.groovy.control.customizers.ImportCustomizer
cons public init()
meth public !varargs org.codehaus.groovy.control.customizers.ImportCustomizer addImports(java.lang.String[])
meth public !varargs org.codehaus.groovy.control.customizers.ImportCustomizer addStarImports(java.lang.String[])
meth public !varargs org.codehaus.groovy.control.customizers.ImportCustomizer addStaticStars(java.lang.String[])
meth public org.codehaus.groovy.control.customizers.ImportCustomizer addImport(java.lang.String,java.lang.String)
meth public org.codehaus.groovy.control.customizers.ImportCustomizer addStaticImport(java.lang.String,java.lang.String)
meth public org.codehaus.groovy.control.customizers.ImportCustomizer addStaticImport(java.lang.String,java.lang.String,java.lang.String)
meth public void call(org.codehaus.groovy.control.SourceUnit,org.codehaus.groovy.classgen.GeneratorContext,org.codehaus.groovy.ast.ClassNode)
supr org.codehaus.groovy.control.customizers.CompilationCustomizer
hfds imports
hcls Import,ImportType

CLSS public org.codehaus.groovy.control.customizers.SecureASTCustomizer
cons public init()
innr public abstract interface static ExpressionChecker
innr public abstract interface static StatementChecker
meth public !varargs void addExpressionCheckers(org.codehaus.groovy.control.customizers.SecureASTCustomizer$ExpressionChecker[])
meth public !varargs void addStatementCheckers(org.codehaus.groovy.control.customizers.SecureASTCustomizer$StatementChecker[])
meth public boolean isClosuresAllowed()
meth public boolean isIndirectImportCheckEnabled()
meth public boolean isMethodDefinitionAllowed()
meth public boolean isPackageAllowed()
meth public java.util.List<java.lang.Class<? extends org.codehaus.groovy.ast.expr.Expression>> getExpressionsBlacklist()
meth public java.util.List<java.lang.Class<? extends org.codehaus.groovy.ast.expr.Expression>> getExpressionsWhitelist()
meth public java.util.List<java.lang.Class<? extends org.codehaus.groovy.ast.stmt.Statement>> getStatementsBlacklist()
meth public java.util.List<java.lang.Class<? extends org.codehaus.groovy.ast.stmt.Statement>> getStatementsWhitelist()
meth public java.util.List<java.lang.Integer> getTokensBlacklist()
meth public java.util.List<java.lang.Integer> getTokensWhitelist()
meth public java.util.List<java.lang.String> getConstantTypesBlackList()
meth public java.util.List<java.lang.String> getConstantTypesWhiteList()
meth public java.util.List<java.lang.String> getImportsBlacklist()
meth public java.util.List<java.lang.String> getImportsWhitelist()
meth public java.util.List<java.lang.String> getReceiversBlackList()
meth public java.util.List<java.lang.String> getReceiversWhiteList()
meth public java.util.List<java.lang.String> getStarImportsBlacklist()
meth public java.util.List<java.lang.String> getStarImportsWhitelist()
meth public java.util.List<java.lang.String> getStaticImportsBlacklist()
meth public java.util.List<java.lang.String> getStaticImportsWhitelist()
meth public java.util.List<java.lang.String> getStaticStarImportsBlacklist()
meth public java.util.List<java.lang.String> getStaticStarImportsWhitelist()
meth public void call(org.codehaus.groovy.control.SourceUnit,org.codehaus.groovy.classgen.GeneratorContext,org.codehaus.groovy.ast.ClassNode)
meth public void setClosuresAllowed(boolean)
meth public void setConstantTypesBlackList(java.util.List<java.lang.String>)
meth public void setConstantTypesClassesBlackList(java.util.List<java.lang.Class>)
meth public void setConstantTypesClassesWhiteList(java.util.List<java.lang.Class>)
meth public void setConstantTypesWhiteList(java.util.List<java.lang.String>)
meth public void setExpressionsBlacklist(java.util.List<java.lang.Class<? extends org.codehaus.groovy.ast.expr.Expression>>)
meth public void setExpressionsWhitelist(java.util.List<java.lang.Class<? extends org.codehaus.groovy.ast.expr.Expression>>)
meth public void setImportsBlacklist(java.util.List<java.lang.String>)
meth public void setImportsWhitelist(java.util.List<java.lang.String>)
meth public void setIndirectImportCheckEnabled(boolean)
meth public void setMethodDefinitionAllowed(boolean)
meth public void setPackageAllowed(boolean)
meth public void setReceiversBlackList(java.util.List<java.lang.String>)
meth public void setReceiversClassesBlackList(java.util.List<java.lang.Class>)
meth public void setReceiversClassesWhiteList(java.util.List<java.lang.Class>)
meth public void setReceiversWhiteList(java.util.List<java.lang.String>)
meth public void setStarImportsBlacklist(java.util.List<java.lang.String>)
meth public void setStarImportsWhitelist(java.util.List<java.lang.String>)
meth public void setStatementsBlacklist(java.util.List<java.lang.Class<? extends org.codehaus.groovy.ast.stmt.Statement>>)
meth public void setStatementsWhitelist(java.util.List<java.lang.Class<? extends org.codehaus.groovy.ast.stmt.Statement>>)
meth public void setStaticImportsBlacklist(java.util.List<java.lang.String>)
meth public void setStaticImportsWhitelist(java.util.List<java.lang.String>)
meth public void setStaticStarImportsBlacklist(java.util.List<java.lang.String>)
meth public void setStaticStarImportsWhitelist(java.util.List<java.lang.String>)
meth public void setTokensBlacklist(java.util.List<java.lang.Integer>)
meth public void setTokensWhitelist(java.util.List<java.lang.Integer>)
supr org.codehaus.groovy.control.customizers.CompilationCustomizer
hfds constantTypesBlackList,constantTypesWhiteList,expressionCheckers,expressionsBlacklist,expressionsWhitelist,importsBlacklist,importsWhitelist,isClosuresAllowed,isIndirectImportCheckEnabled,isMethodDefinitionAllowed,isPackageAllowed,receiversBlackList,receiversWhiteList,starImportsBlacklist,starImportsWhitelist,statementCheckers,statementsBlacklist,statementsWhitelist,staticImportsBlacklist,staticImportsWhitelist,staticStarImportsBlacklist,staticStarImportsWhitelist,tokensBlacklist,tokensWhitelist
hcls SecuringCodeVisitor

CLSS public abstract interface static org.codehaus.groovy.control.customizers.SecureASTCustomizer$ExpressionChecker
 outer org.codehaus.groovy.control.customizers.SecureASTCustomizer
meth public abstract boolean isAuthorized(org.codehaus.groovy.ast.expr.Expression)

CLSS public abstract interface static org.codehaus.groovy.control.customizers.SecureASTCustomizer$StatementChecker
 outer org.codehaus.groovy.control.customizers.SecureASTCustomizer
meth public abstract boolean isAuthorized(org.codehaus.groovy.ast.stmt.Statement)

CLSS public org.codehaus.groovy.control.customizers.SourceAwareCustomizer
cons public init(org.codehaus.groovy.control.customizers.CompilationCustomizer)
meth public boolean accept(java.lang.String)
meth public boolean acceptBaseName(java.lang.String)
meth public boolean acceptClass(org.codehaus.groovy.ast.ClassNode)
meth public boolean acceptExtension(java.lang.String)
meth public boolean acceptSource(org.codehaus.groovy.control.SourceUnit)
meth public void call(org.codehaus.groovy.control.SourceUnit,org.codehaus.groovy.classgen.GeneratorContext,org.codehaus.groovy.ast.ClassNode)
meth public void setBaseNameValidator(groovy.lang.Closure<java.lang.Boolean>)
meth public void setClassValidator(groovy.lang.Closure<java.lang.Boolean>)
meth public void setExtensionValidator(groovy.lang.Closure<java.lang.Boolean>)
meth public void setSourceUnitValidator(groovy.lang.Closure<java.lang.Boolean>)
supr org.codehaus.groovy.control.customizers.DelegatingCustomizer
hfds baseNameValidator,classValidator,extensionValidator,sourceUnitValidator

CLSS public org.codehaus.groovy.control.customizers.builder.ASTTransformationCustomizerFactory
cons public init()
intf groovy.lang.GroovyObject
meth public boolean isLeaf()
meth public boolean onHandleNodeAttributes(groovy.util.FactoryBuilderSupport,java.lang.Object,java.util.Map)
meth public java.lang.Object newInstance(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object,java.util.Map) throws java.lang.IllegalAccessException,java.lang.InstantiationException
supr groovy.util.AbstractFactory

CLSS public org.codehaus.groovy.control.customizers.builder.CompilerCustomizationBuilder
cons public init()
meth protected java.lang.Object postNodeCompletion(java.lang.Object,java.lang.Object)
meth public static org.codehaus.groovy.control.CompilerConfiguration withConfig(org.codehaus.groovy.control.CompilerConfiguration,groovy.lang.Closure)
supr groovy.util.FactoryBuilderSupport

CLSS public org.codehaus.groovy.control.customizers.builder.CustomizersFactory
cons public init()
intf org.codehaus.groovy.control.customizers.builder.PostCompletionFactory
meth public java.lang.Object newInstance(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object,java.util.Map) throws java.lang.IllegalAccessException,java.lang.InstantiationException
meth public java.lang.Object postCompleteNode(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object)
meth public void setChild(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object)
supr groovy.util.AbstractFactory

CLSS public org.codehaus.groovy.control.customizers.builder.ImportCustomizerFactory
cons public init()
meth public boolean isHandlesNodeChildren()
meth public boolean onNodeChildren(groovy.util.FactoryBuilderSupport,java.lang.Object,groovy.lang.Closure)
meth public java.lang.Object newInstance(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object,java.util.Map) throws java.lang.IllegalAccessException,java.lang.InstantiationException
supr groovy.util.AbstractFactory
hcls ImportHelper

CLSS public org.codehaus.groovy.control.customizers.builder.InlinedASTCustomizerFactory
cons public init()
intf org.codehaus.groovy.control.customizers.builder.PostCompletionFactory
meth public boolean isHandlesNodeChildren()
meth public boolean onNodeChildren(groovy.util.FactoryBuilderSupport,java.lang.Object,groovy.lang.Closure)
meth public java.lang.Object newInstance(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object,java.util.Map) throws java.lang.IllegalAccessException,java.lang.InstantiationException
meth public java.lang.Object postCompleteNode(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object)
supr groovy.util.AbstractFactory

CLSS public abstract interface org.codehaus.groovy.control.customizers.builder.PostCompletionFactory
meth public abstract java.lang.Object postCompleteNode(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object)

CLSS public org.codehaus.groovy.control.customizers.builder.SecureASTCustomizerFactory
cons public init()
meth public boolean isHandlesNodeChildren()
meth public boolean onNodeChildren(groovy.util.FactoryBuilderSupport,java.lang.Object,groovy.lang.Closure)
meth public java.lang.Object newInstance(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object,java.util.Map) throws java.lang.IllegalAccessException,java.lang.InstantiationException
supr groovy.util.AbstractFactory

CLSS public org.codehaus.groovy.control.customizers.builder.SourceAwareCustomizerFactory
cons public init()
innr public static SourceOptions
intf org.codehaus.groovy.control.customizers.builder.PostCompletionFactory
meth public java.lang.Object newInstance(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object,java.util.Map) throws java.lang.IllegalAccessException,java.lang.InstantiationException
meth public java.lang.Object postCompleteNode(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object)
meth public void setChild(groovy.util.FactoryBuilderSupport,java.lang.Object,java.lang.Object)
supr groovy.util.AbstractFactory

CLSS public static org.codehaus.groovy.control.customizers.builder.SourceAwareCustomizerFactory$SourceOptions
 outer org.codehaus.groovy.control.customizers.builder.SourceAwareCustomizerFactory
cons public init()
fld public groovy.lang.Closure<java.lang.Boolean> basenameValidator
fld public groovy.lang.Closure<java.lang.Boolean> classValidator
fld public groovy.lang.Closure<java.lang.Boolean> extensionValidator
fld public groovy.lang.Closure<java.lang.Boolean> unitValidator
fld public java.lang.String basename
fld public java.lang.String extension
fld public java.util.List<java.lang.String> basenames
fld public java.util.List<java.lang.String> extensions
fld public org.codehaus.groovy.control.customizers.CompilationCustomizer delegate
supr java.lang.Object

CLSS public org.codehaus.groovy.control.messages.ExceptionMessage
cons public init(java.lang.Exception,boolean,org.codehaus.groovy.control.ProcessingUnit)
fld protected boolean verbose
meth public java.lang.Exception getCause()
meth public void write(java.io.PrintWriter,org.codehaus.groovy.control.Janitor)
supr org.codehaus.groovy.control.messages.Message
hfds cause,owner

CLSS public org.codehaus.groovy.control.messages.LocatedMessage
cons public init(java.lang.String,java.lang.Object,org.codehaus.groovy.syntax.CSTNode,org.codehaus.groovy.control.SourceUnit)
cons public init(java.lang.String,org.codehaus.groovy.syntax.CSTNode,org.codehaus.groovy.control.SourceUnit)
fld protected org.codehaus.groovy.syntax.CSTNode context
meth public void write(java.io.PrintWriter,org.codehaus.groovy.control.Janitor)
supr org.codehaus.groovy.control.messages.SimpleMessage

CLSS public abstract org.codehaus.groovy.control.messages.Message
cons public init()
meth public abstract void write(java.io.PrintWriter,org.codehaus.groovy.control.Janitor)
meth public final void write(java.io.PrintWriter)
meth public static org.codehaus.groovy.control.messages.Message create(java.lang.String,java.lang.Object,org.codehaus.groovy.control.ProcessingUnit)
meth public static org.codehaus.groovy.control.messages.Message create(java.lang.String,org.codehaus.groovy.control.ProcessingUnit)
meth public static org.codehaus.groovy.control.messages.Message create(org.codehaus.groovy.syntax.SyntaxException,org.codehaus.groovy.control.SourceUnit)
supr java.lang.Object

CLSS public org.codehaus.groovy.control.messages.SimpleMessage
cons public init(java.lang.String,java.lang.Object,org.codehaus.groovy.control.ProcessingUnit)
cons public init(java.lang.String,org.codehaus.groovy.control.ProcessingUnit)
fld protected java.lang.Object data
fld protected java.lang.String message
fld protected org.codehaus.groovy.control.ProcessingUnit owner
meth public java.lang.String getMessage()
meth public void write(java.io.PrintWriter,org.codehaus.groovy.control.Janitor)
supr org.codehaus.groovy.control.messages.Message

CLSS public org.codehaus.groovy.control.messages.SyntaxErrorMessage
cons public init(org.codehaus.groovy.syntax.SyntaxException,org.codehaus.groovy.control.SourceUnit)
fld protected org.codehaus.groovy.control.SourceUnit source
fld protected org.codehaus.groovy.syntax.SyntaxException cause
meth public org.codehaus.groovy.syntax.SyntaxException getCause()
meth public void write(java.io.PrintWriter,org.codehaus.groovy.control.Janitor)
supr org.codehaus.groovy.control.messages.Message

CLSS public org.codehaus.groovy.control.messages.WarningMessage
cons public init(int,java.lang.String,java.lang.Object,org.codehaus.groovy.syntax.CSTNode,org.codehaus.groovy.control.SourceUnit)
cons public init(int,java.lang.String,org.codehaus.groovy.syntax.CSTNode,org.codehaus.groovy.control.SourceUnit)
fld public final static int LIKELY_ERRORS = 1
fld public final static int NONE = 0
fld public final static int PARANOIA = 3
fld public final static int POSSIBLE_ERRORS = 2
meth public boolean isRelevant(int)
meth public static boolean isRelevant(int,int)
meth public void write(java.io.PrintWriter,org.codehaus.groovy.control.Janitor)
supr org.codehaus.groovy.control.messages.LocatedMessage
hfds importance

CLSS public org.codehaus.groovy.reflection.CacheAccessControlException
cons public init(java.lang.String,java.lang.Throwable)
supr groovy.lang.GroovyRuntimeException
hfds serialVersionUID

CLSS public org.codehaus.groovy.reflection.CachedClass
cons public init(java.lang.Class,org.codehaus.groovy.reflection.ClassInfo)
fld public final boolean isArray
fld public final boolean isInterface
fld public final boolean isNumber
fld public final boolean isPrimitive
fld public final int modifiers
fld public final static org.codehaus.groovy.reflection.CachedClass[] EMPTY_ARRAY
fld public org.codehaus.groovy.reflection.CachedMethod[] mopMethods
fld public org.codehaus.groovy.reflection.ClassInfo classInfo
innr public static CachedMethodComparatorByName
innr public static CachedMethodComparatorWithString
meth public boolean isAssignableFrom(java.lang.Class)
meth public boolean isDirectlyAssignable(java.lang.Object)
meth public boolean isInterface()
meth public boolean isPrimitive()
meth public boolean isVoid()
meth public final java.lang.Class getTheClass()
meth public groovy.lang.MetaMethod[] getNewMetaMethods()
meth public int getModifiers()
meth public int getSuperClassDistance()
meth public int hashCode()
meth public java.lang.Object coerceArgument(java.lang.Object)
meth public java.lang.String getName()
meth public java.lang.String getTypeDescription()
meth public java.lang.String toString()
meth public java.util.Collection<org.codehaus.groovy.reflection.ClassInfo> getHierarchy()
meth public java.util.Set<org.codehaus.groovy.reflection.CachedClass> getDeclaredInterfaces()
meth public java.util.Set<org.codehaus.groovy.reflection.CachedClass> getInterfaces()
meth public org.codehaus.groovy.reflection.CachedClass getCachedClass()
meth public org.codehaus.groovy.reflection.CachedClass getCachedSuperClass()
meth public org.codehaus.groovy.reflection.CachedConstructor[] getConstructors()
meth public org.codehaus.groovy.reflection.CachedField[] getFields()
meth public org.codehaus.groovy.reflection.CachedMethod searchMethods(java.lang.String,org.codehaus.groovy.reflection.CachedClass[])
meth public org.codehaus.groovy.reflection.CachedMethod[] getMethods()
meth public org.codehaus.groovy.runtime.callsite.CallSiteClassLoader getCallSiteLoader()
meth public void addNewMopMethods(java.util.List<groovy.lang.MetaMethod>)
meth public void setNewMopMethods(java.util.List<groovy.lang.MetaMethod>)
supr java.lang.Object
hfds EMPTY,EMPTY_METHOD_ARRAY,cachedClass,cachedSuperClass,callSiteClassLoader,constructors,declaredInterfaces,distance,fields,hashCode,hierarchy,interfaces,methods,softBundle

CLSS public static org.codehaus.groovy.reflection.CachedClass$CachedMethodComparatorByName
 outer org.codehaus.groovy.reflection.CachedClass
cons public init()
fld public final static java.util.Comparator INSTANCE
intf java.util.Comparator
meth public int compare(java.lang.Object,java.lang.Object)
supr java.lang.Object

CLSS public static org.codehaus.groovy.reflection.CachedClass$CachedMethodComparatorWithString
 outer org.codehaus.groovy.reflection.CachedClass
cons public init()
fld public final static java.util.Comparator INSTANCE
intf java.util.Comparator
meth public int compare(java.lang.Object,java.lang.Object)
supr java.lang.Object

CLSS public org.codehaus.groovy.reflection.CachedConstructor
cons public init(java.lang.reflect.Constructor)
cons public init(org.codehaus.groovy.reflection.CachedClass,java.lang.reflect.Constructor)
fld public final java.lang.reflect.Constructor cachedConstructor
meth protected java.lang.Class[] getPT()
meth public int getModifiers()
meth public java.lang.Object doConstructorInvoke(java.lang.Object[])
meth public java.lang.Object invoke(java.lang.Object[])
meth public org.codehaus.groovy.reflection.CachedClass getCachedClass()
meth public static org.codehaus.groovy.reflection.CachedConstructor find(java.lang.reflect.Constructor)
supr org.codehaus.groovy.reflection.ParameterTypes
hfds clazz

CLSS public org.codehaus.groovy.reflection.CachedField
cons public init(java.lang.reflect.Field)
fld public final java.lang.reflect.Field field
meth public boolean isFinal()
meth public boolean isStatic()
meth public int getModifiers()
meth public java.lang.Object getProperty(java.lang.Object)
meth public void setProperty(java.lang.Object,java.lang.Object)
supr groovy.lang.MetaProperty

CLSS public org.codehaus.groovy.reflection.CachedMethod
cons public init(java.lang.reflect.Method)
cons public init(org.codehaus.groovy.reflection.CachedClass,java.lang.reflect.Method)
fld public final org.codehaus.groovy.reflection.CachedClass cachedClass
fld public final static org.codehaus.groovy.reflection.CachedMethod[] EMPTY_ARRAY
intf java.lang.Comparable
meth protected java.lang.Class[] getPT()
meth public boolean equals(java.lang.Object)
meth public boolean isStatic()
meth public final java.lang.Object invoke(java.lang.Object,java.lang.Object[])
meth public final java.lang.reflect.Method setAccessible()
meth public int compareTo(java.lang.Object)
meth public int getModifiers()
meth public int getParamsCount()
meth public int hashCode()
meth public java.lang.Class getReturnType()
meth public java.lang.String getDescriptor()
meth public java.lang.String getName()
meth public java.lang.String getSignature()
meth public java.lang.String toString()
meth public java.lang.reflect.Method getCachedMethod()
meth public org.codehaus.groovy.reflection.CachedClass getDeclaringClass()
meth public org.codehaus.groovy.reflection.ParameterTypes getParamTypes()
meth public org.codehaus.groovy.runtime.callsite.CallSite createPogoMetaMethodSite(org.codehaus.groovy.runtime.callsite.CallSite,groovy.lang.MetaClassImpl,java.lang.Class[])
meth public org.codehaus.groovy.runtime.callsite.CallSite createPojoMetaMethodSite(org.codehaus.groovy.runtime.callsite.CallSite,groovy.lang.MetaClassImpl,java.lang.Class[])
meth public org.codehaus.groovy.runtime.callsite.CallSite createStaticMetaMethodSite(org.codehaus.groovy.runtime.callsite.CallSite,groovy.lang.MetaClassImpl,java.lang.Class[])
meth public static org.codehaus.groovy.reflection.CachedMethod find(java.lang.reflect.Method)
supr groovy.lang.MetaMethod
hfds COMPARATOR,cachedMethod,hashCode,pogoCallSiteConstructor,pojoCallSiteConstructor,skipCompiled,staticCallSiteConstructor
hcls MyComparator

CLSS public org.codehaus.groovy.reflection.ClassInfo
fld public final int hash = -1
innr public abstract interface static ClassInfoAction
intf org.codehaus.groovy.util.Finalizable
meth public boolean hasPerInstanceMetaClasses()
meth public final groovy.lang.MetaClass getMetaClass()
meth public final java.lang.Class<?> getTheClass()
meth public groovy.lang.ExpandoMetaClass getModifiedExpando()
meth public groovy.lang.MetaClass getMetaClass(java.lang.Object)
meth public groovy.lang.MetaClass getMetaClassForClass()
meth public groovy.lang.MetaClass getPerInstanceMetaClass(java.lang.Object)
meth public groovy.lang.MetaClass getStrongMetaClass()
meth public groovy.lang.MetaClass getWeakMetaClass()
meth public int getVersion()
meth public org.codehaus.groovy.reflection.CachedClass getCachedClass()
meth public org.codehaus.groovy.reflection.ClassLoaderForClassArtifacts getArtifactClassLoader()
meth public static int fullSize()
meth public static int size()
meth public static java.util.Collection<org.codehaus.groovy.reflection.ClassInfo> getAllClassInfo()
meth public static org.codehaus.groovy.reflection.ClassInfo getClassInfo(java.lang.Class)
meth public static void clearModifiedExpandos()
meth public static void onAllClassInfo(org.codehaus.groovy.reflection.ClassInfo$ClassInfoAction)
meth public static void remove(java.lang.Class<?>)
meth public void finalizeReference()
meth public void incVersion()
meth public void lock()
meth public void setPerInstanceMetaClass(java.lang.Object,groovy.lang.MetaClass)
meth public void setStrongMetaClass(groovy.lang.MetaClass)
meth public void setWeakMetaClass(groovy.lang.MetaClass)
meth public void unlock()
supr java.lang.Object
hfds artifactClassLoader,cachedClassRef,classRef,dgmMetaMethods,globalClassSet,globalClassValue,klazz,lock,modifiedExpandos,newMetaMethods,perInstanceMetaClassMap,softBundle,strongMetaClass,version,weakBundle,weakMetaClass
hcls GlobalClassSet,LazyCachedClassRef,LazyClassLoaderRef,Sentinel

CLSS public abstract interface static org.codehaus.groovy.reflection.ClassInfo$ClassInfoAction
 outer org.codehaus.groovy.reflection.ClassInfo
meth public abstract void onClassInfo(org.codehaus.groovy.reflection.ClassInfo)

CLSS public org.codehaus.groovy.reflection.ClassLoaderForClassArtifacts
cons public init(java.lang.Class)
fld public final java.lang.ref.SoftReference<java.lang.Class> klazz
meth public java.lang.Class define(java.lang.String,byte[])
meth public java.lang.Class loadClass(java.lang.String) throws java.lang.ClassNotFoundException
meth public java.lang.String createClassName(java.lang.reflect.Method)
meth public java.lang.reflect.Constructor defineClassAndGetConstructor(java.lang.String,byte[])
supr java.lang.ClassLoader
hfds classNamesCounter

CLSS public abstract org.codehaus.groovy.reflection.GeneratedMetaMethod
cons public init(java.lang.String,org.codehaus.groovy.reflection.CachedClass,java.lang.Class,java.lang.Class[])
innr public static DgmMethodRecord
innr public static Proxy
meth public int getModifiers()
meth public java.lang.Class getReturnType()
meth public java.lang.String getName()
meth public org.codehaus.groovy.reflection.CachedClass getDeclaringClass()
supr groovy.lang.MetaMethod
hfds declaringClass,name,returnType

CLSS public static org.codehaus.groovy.reflection.GeneratedMetaMethod$DgmMethodRecord
 outer org.codehaus.groovy.reflection.GeneratedMetaMethod
cons public init()
fld public java.lang.Class returnType
fld public java.lang.Class[] parameters
fld public java.lang.String className
fld public java.lang.String methodName
intf java.io.Serializable
meth public static java.util.List<org.codehaus.groovy.reflection.GeneratedMetaMethod$DgmMethodRecord> loadDgmInfo() throws java.io.IOException
meth public static void saveDgmInfo(java.util.List<org.codehaus.groovy.reflection.GeneratedMetaMethod$DgmMethodRecord>,java.lang.String) throws java.io.IOException
supr java.lang.Object
hfds PRIMITIVE_CLASSES,serialVersionUID

CLSS public static org.codehaus.groovy.reflection.GeneratedMetaMethod$Proxy
 outer org.codehaus.groovy.reflection.GeneratedMetaMethod
cons public init(java.lang.String,java.lang.String,org.codehaus.groovy.reflection.CachedClass,java.lang.Class,java.lang.Class[])
meth public boolean isValidMethod(java.lang.Class[])
meth public final groovy.lang.MetaMethod proxy()
meth public java.lang.Object doMethodInvoke(java.lang.Object,java.lang.Object[])
meth public java.lang.Object invoke(java.lang.Object,java.lang.Object[])
supr org.codehaus.groovy.reflection.GeneratedMetaMethod
hfds className,proxy

CLSS public abstract interface org.codehaus.groovy.reflection.GroovyClassValue<%0 extends java.lang.Object>
innr public abstract interface static ComputeValue
meth public abstract void remove(java.lang.Class<?>)
meth public abstract {org.codehaus.groovy.reflection.GroovyClassValue%0} get(java.lang.Class<?>)

CLSS public abstract interface static org.codehaus.groovy.reflection.GroovyClassValue$ComputeValue<%0 extends java.lang.Object>
 outer org.codehaus.groovy.reflection.GroovyClassValue
meth public abstract {org.codehaus.groovy.reflection.GroovyClassValue$ComputeValue%0} computeValue(java.lang.Class<?>)

CLSS public org.codehaus.groovy.reflection.MixinInMetaClass
cons public init(groovy.lang.ExpandoMetaClass,org.codehaus.groovy.reflection.CachedClass)
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Object getMixinInstance(java.lang.Object)
meth public org.codehaus.groovy.reflection.CachedClass getInstanceClass()
meth public org.codehaus.groovy.reflection.CachedClass getMixinClass()
meth public static void mixinClassesToMetaClass(groovy.lang.MetaClass,java.util.List<java.lang.Class>)
meth public void setMixinInstance(java.lang.Object,java.lang.Object)
supr org.codehaus.groovy.util.ManagedConcurrentMap
hfds constructor,emc,mixinClass,softBundle

CLSS public org.codehaus.groovy.reflection.ParameterTypes
cons public init()
cons public init(java.lang.Class[])
cons public init(java.lang.String[])
cons public init(org.codehaus.groovy.reflection.CachedClass[])
fld protected boolean isVargsMethod
fld protected volatile java.lang.Class[] nativeParamTypes
fld protected volatile org.codehaus.groovy.reflection.CachedClass[] parameterTypes
meth protected final void setParametersTypes(org.codehaus.groovy.reflection.CachedClass[])
meth protected java.lang.Class[] getPT()
meth public boolean isValidExactMethod(java.lang.Class[])
meth public boolean isValidExactMethod(java.lang.Object[])
meth public boolean isValidMethod(java.lang.Class[])
meth public boolean isValidMethod(java.lang.Object[])
meth public boolean isVargsMethod()
meth public boolean isVargsMethod(java.lang.Object[])
meth public final java.lang.Object[] coerceArgumentsToClasses(java.lang.Object[])
meth public java.lang.Class[] getNativeParameterTypes()
meth public java.lang.Object[] correctArguments(java.lang.Object[])
meth public org.codehaus.groovy.reflection.CachedClass[] getParameterTypes()
supr java.lang.Object
hfds NO_PARAMETERS

CLSS public org.codehaus.groovy.reflection.ReflectionCache
cons public init()
fld public final static org.codehaus.groovy.reflection.CachedClass OBJECT_ARRAY_CLASS
fld public final static org.codehaus.groovy.reflection.CachedClass OBJECT_CLASS
meth public static boolean isArray(java.lang.Class)
meth public static boolean isAssignableFrom(java.lang.Class,java.lang.Class)
meth public static java.lang.Class autoboxType(java.lang.Class)
meth public static java.lang.String getMOPMethodName(org.codehaus.groovy.reflection.CachedClass,java.lang.String,boolean)
meth public static org.codehaus.groovy.reflection.CachedClass getCachedClass(java.lang.Class)
supr java.lang.Object
hfds STRING_CLASS,mopNames

CLSS public org.codehaus.groovy.reflection.ReflectionUtils
cons public init()
meth public static boolean isCallingClassReflectionAvailable()
meth public static java.lang.Class getCallingClass()
meth public static java.lang.Class getCallingClass(int)
meth public static java.lang.Class getCallingClass(int,java.util.Collection<java.lang.String>)
supr java.lang.Object
hfds HELPER,IGNORED_PACKAGES
hcls ClassContextHelper

CLSS public org.codehaus.groovy.reflection.SunClassLoader
cons protected init() throws java.lang.Throwable
fld protected final java.util.Map<java.lang.String,java.lang.Class> knownClasses
fld protected final static org.codehaus.groovy.reflection.SunClassLoader sunVM
intf groovyjarjarasm.asm.Opcodes
meth protected java.lang.Class loadClass(java.lang.String,boolean) throws java.lang.ClassNotFoundException
meth protected static java.lang.String resName(java.lang.String)
meth protected void define(byte[],java.lang.String)
meth protected void loadFromRes(java.lang.String) throws java.io.IOException
meth public java.lang.Class doesKnow(java.lang.String)
supr java.lang.ClassLoader

CLSS public abstract interface org.codehaus.groovy.runtime.GeneratedClosure

CLSS public org.codehaus.groovy.syntax.ASTHelper
cons public init()
cons public init(org.codehaus.groovy.control.SourceUnit,java.lang.ClassLoader)
fld protected org.codehaus.groovy.ast.ModuleNode output
fld protected static java.util.Map resolutions
meth protected java.lang.String dot(java.lang.String)
meth protected void addImport(org.codehaus.groovy.ast.ClassNode,java.lang.String,java.lang.String)
meth protected void addImport(org.codehaus.groovy.ast.ClassNode,java.lang.String,java.lang.String,java.util.List<org.codehaus.groovy.ast.AnnotationNode>)
meth protected void addStarImport(java.lang.String)
meth protected void addStarImport(java.lang.String,java.util.List<org.codehaus.groovy.ast.AnnotationNode>)
meth protected void addStaticImport(org.codehaus.groovy.ast.ClassNode,java.lang.String,java.lang.String)
meth protected void addStaticImport(org.codehaus.groovy.ast.ClassNode,java.lang.String,java.lang.String,java.util.List<org.codehaus.groovy.ast.AnnotationNode>)
meth protected void addStaticStarImport(org.codehaus.groovy.ast.ClassNode,java.lang.String)
meth protected void addStaticStarImport(org.codehaus.groovy.ast.ClassNode,java.lang.String,java.util.List<org.codehaus.groovy.ast.AnnotationNode>)
meth protected void makeModule()
meth public java.lang.ClassLoader getClassLoader()
meth public java.lang.String getPackageName()
meth public org.codehaus.groovy.ast.PackageNode setPackage(java.lang.String,java.util.List<org.codehaus.groovy.ast.AnnotationNode>)
meth public org.codehaus.groovy.control.SourceUnit getController()
meth public static java.lang.String dot(java.lang.String,java.lang.String)
meth public void setClassLoader(java.lang.ClassLoader)
meth public void setController(org.codehaus.groovy.control.SourceUnit)
meth public void setPackageName(java.lang.String)
supr java.lang.Object
hfds classLoader,controller,packageName

CLSS public abstract org.codehaus.groovy.syntax.CSTNode
cons public init()
meth protected void write(java.io.PrintWriter,java.lang.String)
meth public abstract int size()
meth public abstract org.codehaus.groovy.syntax.CSTNode get(int)
meth public abstract org.codehaus.groovy.syntax.Reduction asReduction()
meth public abstract org.codehaus.groovy.syntax.Token getRoot()
meth public boolean canMean(int)
meth public boolean hasChildren()
meth public boolean isA(int)
meth public boolean isAllOf(int[])
meth public boolean isAnExpression()
meth public boolean isEmpty()
meth public boolean isOneOf(int[])
meth public int children()
meth public int getMeaning()
meth public int getMeaningAs(int[])
meth public int getStartColumn()
meth public int getStartLine()
meth public int getType()
meth public java.lang.String getDescription()
meth public java.lang.String getRootText()
meth public java.lang.String toString()
meth public org.codehaus.groovy.syntax.CSTNode add(org.codehaus.groovy.syntax.CSTNode)
meth public org.codehaus.groovy.syntax.CSTNode get(int,boolean)
meth public org.codehaus.groovy.syntax.CSTNode set(int,org.codehaus.groovy.syntax.CSTNode)
meth public org.codehaus.groovy.syntax.CSTNode setMeaning(int)
meth public org.codehaus.groovy.syntax.Token getRoot(boolean)
meth public void addChildrenOf(org.codehaus.groovy.syntax.CSTNode)
meth public void markAsExpression()
meth public void write(java.io.PrintWriter)
supr java.lang.Object

CLSS public org.codehaus.groovy.syntax.Numbers
cons public init()
meth public static boolean isDigit(char)
meth public static boolean isHexDigit(char)
meth public static boolean isNumericTypeSpecifier(char,boolean)
meth public static boolean isOctalDigit(char)
meth public static java.lang.Number parseDecimal(java.lang.String)
meth public static java.lang.Number parseInteger(groovyjarjarantlr.collections.AST,java.lang.String)
meth public static java.lang.Number parseInteger(java.lang.String)
 anno 0 java.lang.Deprecated()
supr java.lang.Object
hfds MAX_DOUBLE,MAX_FLOAT,MAX_INTEGER,MAX_LONG,MIN_DOUBLE,MIN_FLOAT,MIN_INTEGER,MIN_LONG

CLSS public org.codehaus.groovy.syntax.ParserException
cons public init(java.lang.String,java.lang.Throwable,int,int)
cons public init(java.lang.String,java.lang.Throwable,int,int,int,int)
cons public init(java.lang.String,org.codehaus.groovy.syntax.Token)
supr org.codehaus.groovy.syntax.TokenException
hfds serialVersionUID

CLSS public org.codehaus.groovy.syntax.ReadException
cons public init(java.io.IOException)
cons public init(java.lang.String,java.io.IOException)
meth public java.io.IOException getIOCause()
meth public java.lang.String getMessage()
meth public java.lang.String toString()
supr org.codehaus.groovy.GroovyException
hfds cause,serialVersionUID

CLSS public org.codehaus.groovy.syntax.Reduction
cons public init(org.codehaus.groovy.syntax.Token)
fld public final static org.codehaus.groovy.syntax.Reduction EMPTY
meth public boolean isAnExpression()
meth public boolean isEmpty()
meth public int size()
meth public org.codehaus.groovy.syntax.CSTNode add(org.codehaus.groovy.syntax.CSTNode)
meth public org.codehaus.groovy.syntax.CSTNode get(int)
meth public org.codehaus.groovy.syntax.CSTNode remove(int)
meth public org.codehaus.groovy.syntax.CSTNode set(int,org.codehaus.groovy.syntax.CSTNode)
meth public org.codehaus.groovy.syntax.Reduction asReduction()
meth public org.codehaus.groovy.syntax.Token getRoot()
meth public static org.codehaus.groovy.syntax.Reduction newContainer()
meth public void markAsExpression()
supr org.codehaus.groovy.syntax.CSTNode
hfds elements,marked

CLSS public org.codehaus.groovy.syntax.RuntimeParserException
cons public init(java.lang.String,org.codehaus.groovy.ast.ASTNode)
meth public void throwParserException() throws org.codehaus.groovy.syntax.SyntaxException
supr groovy.lang.GroovyRuntimeException
hfds serialVersionUID

CLSS public org.codehaus.groovy.syntax.SyntaxException
cons public init(java.lang.String,int,int)
cons public init(java.lang.String,int,int,int,int)
cons public init(java.lang.String,java.lang.Throwable,int,int)
cons public init(java.lang.String,java.lang.Throwable,int,int,int,int)
cons public init(java.lang.String,org.codehaus.groovy.ast.ASTNode)
meth public int getEndColumn()
meth public int getEndLine()
meth public int getLine()
meth public int getStartColumn()
meth public int getStartLine()
meth public java.lang.String getMessage()
meth public java.lang.String getOriginalMessage()
meth public java.lang.String getSourceLocator()
meth public void setSourceLocator(java.lang.String)
supr org.codehaus.groovy.GroovyException
hfds endColumn,endLine,serialVersionUID,sourceLocator,startColumn,startLine

CLSS public org.codehaus.groovy.syntax.Token
cons public init(int,java.lang.String,int,int)
fld public final static org.codehaus.groovy.syntax.Token EOF
fld public final static org.codehaus.groovy.syntax.Token NULL
meth public int getMeaning()
meth public int getStartColumn()
meth public int getStartLine()
meth public int getType()
meth public int size()
meth public java.lang.String getRootText()
meth public java.lang.String getText()
meth public org.codehaus.groovy.syntax.CSTNode get(int)
meth public org.codehaus.groovy.syntax.CSTNode setMeaning(int)
meth public org.codehaus.groovy.syntax.Reduction asReduction()
meth public org.codehaus.groovy.syntax.Reduction asReduction(org.codehaus.groovy.syntax.CSTNode)
meth public org.codehaus.groovy.syntax.Reduction asReduction(org.codehaus.groovy.syntax.CSTNode,org.codehaus.groovy.syntax.CSTNode)
meth public org.codehaus.groovy.syntax.Reduction asReduction(org.codehaus.groovy.syntax.CSTNode,org.codehaus.groovy.syntax.CSTNode,org.codehaus.groovy.syntax.CSTNode)
meth public org.codehaus.groovy.syntax.Token dup()
meth public org.codehaus.groovy.syntax.Token getRoot()
meth public static org.codehaus.groovy.syntax.Token newDecimal(java.lang.String,int,int)
meth public static org.codehaus.groovy.syntax.Token newIdentifier(java.lang.String,int,int)
meth public static org.codehaus.groovy.syntax.Token newInteger(java.lang.String,int,int)
meth public static org.codehaus.groovy.syntax.Token newKeyword(java.lang.String,int,int)
meth public static org.codehaus.groovy.syntax.Token newPlaceholder(int)
meth public static org.codehaus.groovy.syntax.Token newString(java.lang.String,int,int)
meth public static org.codehaus.groovy.syntax.Token newSymbol(int,int,int)
meth public static org.codehaus.groovy.syntax.Token newSymbol(java.lang.String,int,int)
meth public void setText(java.lang.String)
supr org.codehaus.groovy.syntax.CSTNode
hfds meaning,startColumn,startLine,text,type

CLSS public org.codehaus.groovy.syntax.TokenException
cons public init(java.lang.String,java.lang.Throwable,int,int)
cons public init(java.lang.String,java.lang.Throwable,int,int,int,int)
cons public init(java.lang.String,org.codehaus.groovy.syntax.Token)
supr org.codehaus.groovy.syntax.SyntaxException
hfds serialVersionUID

CLSS public org.codehaus.groovy.syntax.TokenMismatchException
cons public init(org.codehaus.groovy.syntax.Token,int)
meth public int getExpectedType()
meth public org.codehaus.groovy.syntax.Token getUnexpectedToken()
supr org.codehaus.groovy.syntax.TokenException
hfds expectedType,serialVersionUID,unexpectedToken

CLSS public org.codehaus.groovy.syntax.TokenUtil
meth public static int removeAssignment(int)
supr java.lang.Object

CLSS public org.codehaus.groovy.syntax.Types
cons public init()
fld public final static int ANY = 1000
fld public final static int ANY_END_OF_STATEMENT = 1003
fld public final static int ARRAY_EXPRESSION = 1905
fld public final static int ARRAY_ITEM_TERMINATORS = 2001
fld public final static int ASSIGN = 100
fld public final static int ASSIGNMENT_OPERATOR = 1100
fld public final static int BITWISE_AND = 341
fld public final static int BITWISE_AND_EQUAL = 351
fld public final static int BITWISE_NEGATION = 97
fld public final static int BITWISE_OPERATOR = 1107
fld public final static int BITWISE_OR = 340
fld public final static int BITWISE_OR_EQUAL = 350
fld public final static int BITWISE_XOR = 342
fld public final static int BITWISE_XOR_EQUAL = 352
fld public final static int COLON = 310
fld public final static int COMMA = 300
fld public final static int COMPARE_EQUAL = 123
fld public final static int COMPARE_GREATER_THAN = 126
fld public final static int COMPARE_GREATER_THAN_EQUAL = 127
fld public final static int COMPARE_IDENTICAL = 121
fld public final static int COMPARE_LESS_THAN = 124
fld public final static int COMPARE_LESS_THAN_EQUAL = 125
fld public final static int COMPARE_NOT_EQUAL = 120
fld public final static int COMPARE_NOT_IDENTICAL = 122
fld public final static int COMPARE_TO = 128
fld public final static int COMPARISON_OPERATOR = 1101
fld public final static int COMPLEX_EXPRESSION = 1911
fld public final static int CREATABLE_PRIMITIVE_TYPE = 1341
fld public final static int CREATABLE_TYPE_NAME = 1430
fld public final static int DECIMAL_NUMBER = 451
fld public final static int DECLARATION_MODIFIER = 1410
fld public final static int DEREFERENCE_OPERATOR = 1106
fld public final static int DIVIDE = 203
fld public final static int DIVIDE_EQUAL = 213
fld public final static int DOT = 70
fld public final static int DOT_DOT = 75
fld public final static int DOT_DOT_DOT = 77
fld public final static int DOUBLE_PIPE = 162
fld public final static int EOF = -1
fld public final static int EQUAL = 100
fld public final static int EQUALS = 100
fld public final static int EXPRESSION = 1900
fld public final static int FIND_REGEX = 90
fld public final static int GENERAL_END_OF_STATEMENT = 1002
fld public final static int GSTRING_END = 902
fld public final static int GSTRING_EXPRESSION_END = 904
fld public final static int GSTRING_EXPRESSION_START = 903
fld public final static int GSTRING_START = 901
fld public final static int IDENTIFIER = 440
fld public final static int INFIX_OPERATOR = 1220
fld public final static int INTDIV = 204
fld public final static int INTDIV_EQUAL = 214
fld public final static int INTEGER_NUMBER = 450
fld public final static int KEYWORD = 1300
fld public final static int KEYWORD_ABSTRACT = 510
fld public final static int KEYWORD_AS = 552
fld public final static int KEYWORD_ASSERT = 585
fld public final static int KEYWORD_BOOLEAN = 601
fld public final static int KEYWORD_BREAK = 574
fld public final static int KEYWORD_BYTE = 602
fld public final static int KEYWORD_CASE = 577
fld public final static int KEYWORD_CATCH = 581
fld public final static int KEYWORD_CHAR = 608
fld public final static int KEYWORD_CLASS = 531
fld public final static int KEYWORD_CONST = 700
fld public final static int KEYWORD_CONTINUE = 575
fld public final static int KEYWORD_DEF = 530
fld public final static int KEYWORD_DEFAULT = 578
fld public final static int KEYWORD_DEFMACRO = 539
fld public final static int KEYWORD_DO = 570
fld public final static int KEYWORD_DOUBLE = 607
fld public final static int KEYWORD_ELSE = 562
fld public final static int KEYWORD_EXPRESSION = 1903
fld public final static int KEYWORD_EXTENDS = 541
fld public final static int KEYWORD_FALSE = 611
fld public final static int KEYWORD_FINAL = 511
fld public final static int KEYWORD_FINALLY = 582
fld public final static int KEYWORD_FLOAT = 606
fld public final static int KEYWORD_FOR = 572
fld public final static int KEYWORD_GOTO = 701
fld public final static int KEYWORD_IDENTIFIER = 1361
fld public final static int KEYWORD_IF = 561
fld public final static int KEYWORD_IMPLEMENTS = 540
fld public final static int KEYWORD_IMPORT = 551
fld public final static int KEYWORD_IN = 573
fld public final static int KEYWORD_INSTANCEOF = 544
fld public final static int KEYWORD_INT = 604
fld public final static int KEYWORD_INTERFACE = 532
fld public final static int KEYWORD_LONG = 605
fld public final static int KEYWORD_MIXIN = 533
fld public final static int KEYWORD_NATIVE = 512
fld public final static int KEYWORD_NEW = 546
fld public final static int KEYWORD_NULL = 612
fld public final static int KEYWORD_PACKAGE = 550
fld public final static int KEYWORD_PRIVATE = 500
fld public final static int KEYWORD_PROPERTY = 545
fld public final static int KEYWORD_PROTECTED = 501
fld public final static int KEYWORD_PUBLIC = 502
fld public final static int KEYWORD_RETURN = 560
fld public final static int KEYWORD_SHORT = 603
fld public final static int KEYWORD_STATIC = 521
fld public final static int KEYWORD_SUPER = 543
fld public final static int KEYWORD_SWITCH = 576
fld public final static int KEYWORD_SYNCHRONIZED = 520
fld public final static int KEYWORD_THIS = 542
fld public final static int KEYWORD_THROW = 583
fld public final static int KEYWORD_THROWS = 584
fld public final static int KEYWORD_TRANSIENT = 513
fld public final static int KEYWORD_TRUE = 610
fld public final static int KEYWORD_TRY = 580
fld public final static int KEYWORD_VOID = 600
fld public final static int KEYWORD_VOLATILE = 514
fld public final static int KEYWORD_WHILE = 571
fld public final static int LEFT_CURLY_BRACE = 10
fld public final static int LEFT_OF_MATCHED_CONTAINER = 1501
fld public final static int LEFT_PARENTHESIS = 50
fld public final static int LEFT_SHIFT = 280
fld public final static int LEFT_SHIFT_EQUAL = 285
fld public final static int LEFT_SQUARE_BRACKET = 30
fld public final static int LITERAL = 1310
fld public final static int LITERAL_EXPRESSION = 1904
fld public final static int LOGICAL_AND = 164
fld public final static int LOGICAL_AND_EQUAL = 168
fld public final static int LOGICAL_OPERATOR = 1103
fld public final static int LOGICAL_OR = 162
fld public final static int LOGICAL_OR_EQUAL = 166
fld public final static int LOOP = 1350
fld public final static int MATCHED_CONTAINER = 1500
fld public final static int MATCH_REGEX = 94
fld public final static int MATH_OPERATOR = 1102
fld public final static int METHOD_CALL_STARTERS = 2006
fld public final static int MINUS = 201
fld public final static int MINUS_EQUAL = 211
fld public final static int MINUS_MINUS = 260
fld public final static int MOD = 205
fld public final static int MOD_EQUAL = 215
fld public final static int MULTIPLY = 202
fld public final static int MULTIPLY_EQUAL = 212
fld public final static int NAMED_VALUE = 1330
fld public final static int NAVIGATE = 80
fld public final static int NEWLINE = 5
fld public final static int NOT = 160
fld public final static int NOT_EOF = 1001
fld public final static int NUMBER = 1320
fld public final static int OPERATOR_EXPRESSION = 1901
fld public final static int OPTIONAL_DATATYPE_FOLLOWERS = 2003
fld public final static int PARAMETER_TERMINATORS = 2000
fld public final static int PIPE = 340
fld public final static int PLUS = 200
fld public final static int PLUS_EQUAL = 210
fld public final static int PLUS_PLUS = 250
fld public final static int POSTFIX_MINUS_MINUS = 262
fld public final static int POSTFIX_OPERATOR = 1210
fld public final static int POSTFIX_PLUS_PLUS = 252
fld public final static int POWER = 206
fld public final static int POWER_EQUAL = 216
fld public final static int PRECLUDES_CAST_OPERATOR = 2008
fld public final static int PREFIX_MINUS = 263
fld public final static int PREFIX_MINUS_MINUS = 261
fld public final static int PREFIX_OPERATOR = 1200
fld public final static int PREFIX_OR_INFIX_OPERATOR = 1230
fld public final static int PREFIX_PLUS = 253
fld public final static int PREFIX_PLUS_PLUS = 251
fld public final static int PRIMITIVE_TYPE = 1340
fld public final static int PURE_PREFIX_OPERATOR = 1235
fld public final static int QUESTION = 330
fld public final static int RANGE_OPERATOR = 1104
fld public final static int REGEX_COMPARISON_OPERATOR = 1105
fld public final static int REGEX_PATTERN = 97
fld public final static int RESERVED_KEYWORD = 1360
fld public final static int RIGHT_CURLY_BRACE = 20
fld public final static int RIGHT_OF_MATCHED_CONTAINER = 1502
fld public final static int RIGHT_PARENTHESIS = 60
fld public final static int RIGHT_SHIFT = 281
fld public final static int RIGHT_SHIFT_EQUAL = 286
fld public final static int RIGHT_SHIFT_UNSIGNED = 282
fld public final static int RIGHT_SHIFT_UNSIGNED_EQUAL = 287
fld public final static int RIGHT_SQUARE_BRACKET = 40
fld public final static int SEMICOLON = 320
fld public final static int SIGN = 1325
fld public final static int SIMPLE_EXPRESSION = 1910
fld public final static int STAR = 202
fld public final static int STAR_STAR = 206
fld public final static int STRING = 400
fld public final static int SWITCH_BLOCK_TERMINATORS = 2004
fld public final static int SWITCH_ENTRIES = 2005
fld public final static int SYMBOL = 1301
fld public final static int SYNTHETIC = 1370
fld public final static int SYNTH_BLOCK = 816
fld public final static int SYNTH_CAST = 815
fld public final static int SYNTH_CLASS = 801
fld public final static int SYNTH_CLOSURE = 817
fld public final static int SYNTH_COMPILATION_UNIT = 800
fld public final static int SYNTH_EXPRESSION = 1902
fld public final static int SYNTH_GSTRING = 812
fld public final static int SYNTH_INTERFACE = 802
fld public final static int SYNTH_LABEL = 818
fld public final static int SYNTH_LIST = 810
fld public final static int SYNTH_MAP = 811
fld public final static int SYNTH_METHOD = 804
fld public final static int SYNTH_METHOD_CALL = 814
fld public final static int SYNTH_MIXIN = 803
fld public final static int SYNTH_PARAMETER_DECLARATION = 806
fld public final static int SYNTH_PROPERTY = 805
fld public final static int SYNTH_TERNARY = 819
fld public final static int SYNTH_TUPLE = 820
fld public final static int SYNTH_VARIABLE_DECLARATION = 830
fld public final static int TRUTH_VALUE = 1331
fld public final static int TYPE_DECLARATION = 1400
fld public final static int TYPE_LIST_TERMINATORS = 2002
fld public final static int TYPE_NAME = 1420
fld public final static int UNKNOWN = 0
fld public final static int UNSAFE_OVER_NEWLINES = 2007
meth public static boolean canMean(int,int)
meth public static boolean isKeyword(java.lang.String)
meth public static boolean ofType(int,int)
meth public static int getPrecedence(int,boolean)
meth public static int lookup(java.lang.String,int)
meth public static int lookupKeyword(java.lang.String)
meth public static int lookupSymbol(java.lang.String)
meth public static java.lang.String getDescription(int)
meth public static java.lang.String getText(int)
meth public static java.util.Collection<java.lang.String> getKeywords()
meth public static void makePostfix(org.codehaus.groovy.syntax.CSTNode,boolean)
meth public static void makePrefix(org.codehaus.groovy.syntax.CSTNode,boolean)
supr java.lang.Object
hfds DESCRIPTIONS,KEYWORDS,LOOKUP,TEXTS

CLSS public org.codehaus.groovy.transform.ASTTestTransformation
cons public init()
innr public static LabelFinder
intf groovy.lang.GroovyObject
intf groovy.transform.CompilationUnitAware
meth public void setCompilationUnit(org.codehaus.groovy.control.CompilationUnit)
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds compilationUnit
hcls AssertionSourceDelegatingSourceUnit,ProgressCallbackChain

CLSS public org.codehaus.groovy.transform.ASTTestTransformation$1
innr public final _closure1
intf groovy.lang.GroovyObject
meth public groovy.lang.Binding getBinding()
meth public void call(org.codehaus.groovy.control.ProcessingUnit,int)
meth public void setBinding(groovy.lang.Binding)
supr org.codehaus.groovy.control.CompilationUnit$ProgressCallback
hfds binding

CLSS public final org.codehaus.groovy.transform.ASTTestTransformation$1$_call_closure2
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object doCall()
meth public java.lang.Object doCall(java.lang.Object)
meth public java.lang.Object getCustomizer()
supr groovy.lang.Closure

CLSS public final org.codehaus.groovy.transform.ASTTestTransformation$1$_call_closure3
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object doCall()
meth public java.lang.Object doCall(java.lang.Object)
meth public java.lang.Object getCustomizer()
supr groovy.lang.Closure

CLSS public final org.codehaus.groovy.transform.ASTTestTransformation$1$_call_closure4
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object doCall()
meth public java.lang.Object doCall(java.lang.Object)
meth public java.lang.Object getCustomizer()
supr groovy.lang.Closure

CLSS public final org.codehaus.groovy.transform.ASTTestTransformation$1$_call_closure5
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object doCall()
meth public java.lang.Object doCall(java.lang.Object)
meth public java.lang.Object getCustomizer()
supr groovy.lang.Closure

CLSS public final org.codehaus.groovy.transform.ASTTestTransformation$1$_closure1
 outer org.codehaus.groovy.transform.ASTTestTransformation$1
cons public init(java.lang.Object,java.lang.Object)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object doCall()
meth public java.lang.Object doCall(java.lang.Object)
supr groovy.lang.Closure

CLSS public static org.codehaus.groovy.transform.ASTTestTransformation$LabelFinder
 outer org.codehaus.groovy.transform.ASTTestTransformation
cons public init(java.lang.String,org.codehaus.groovy.control.SourceUnit)
intf groovy.lang.GroovyObject
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth protected void visitStatement(org.codehaus.groovy.ast.stmt.Statement)
meth public java.util.List<org.codehaus.groovy.ast.stmt.Statement> getTargets()
meth public static java.util.List<org.codehaus.groovy.ast.stmt.Statement> lookup(org.codehaus.groovy.ast.ClassNode,java.lang.String)
meth public static java.util.List<org.codehaus.groovy.ast.stmt.Statement> lookup(org.codehaus.groovy.ast.MethodNode,java.lang.String)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds label,targets,unit

CLSS public abstract interface org.codehaus.groovy.transform.ASTTransformation
meth public abstract void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)

CLSS public org.codehaus.groovy.transform.ASTTransformationCollectorCodeVisitor
cons public init(org.codehaus.groovy.control.SourceUnit,groovy.lang.GroovyClassLoader)
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth public void visitAnnotations(org.codehaus.groovy.ast.AnnotatedNode)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds classNode,source,transformLoader

CLSS public final org.codehaus.groovy.transform.ASTTransformationVisitor
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth public static void addGlobalTransforms(org.codehaus.groovy.control.ASTTransformationsContext)
meth public static void addGlobalTransformsAfterGrab(org.codehaus.groovy.control.ASTTransformationsContext)
meth public static void addPhaseOperations(org.codehaus.groovy.control.CompilationUnit)
meth public void visitAnnotations(org.codehaus.groovy.ast.AnnotatedNode)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds context,phase,source,targetNodes,transforms

CLSS public abstract org.codehaus.groovy.transform.AbstractASTTransformUtil
 anno 0 java.lang.Deprecated()
cons public init()
intf groovyjarjarasm.asm.Opcodes
meth public static boolean hasDeclaredMethod(org.codehaus.groovy.ast.ClassNode,java.lang.String,int)
 anno 0 java.lang.Deprecated()
meth public static boolean isOrImplements(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
 anno 0 java.lang.Deprecated()
meth public static java.util.List<org.codehaus.groovy.ast.FieldNode> getInstanceNonPropertyFields(org.codehaus.groovy.ast.ClassNode)
 anno 0 java.lang.Deprecated()
meth public static java.util.List<org.codehaus.groovy.ast.FieldNode> getInstancePropertyFields(org.codehaus.groovy.ast.ClassNode)
 anno 0 java.lang.Deprecated()
meth public static java.util.List<org.codehaus.groovy.ast.FieldNode> getSuperNonPropertyFields(org.codehaus.groovy.ast.ClassNode)
 anno 0 java.lang.Deprecated()
meth public static java.util.List<org.codehaus.groovy.ast.FieldNode> getSuperPropertyFields(org.codehaus.groovy.ast.ClassNode)
 anno 0 java.lang.Deprecated()
meth public static java.util.List<org.codehaus.groovy.ast.PropertyNode> getInstanceProperties(org.codehaus.groovy.ast.ClassNode)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.expr.BooleanExpression differentExpr(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.expr.BooleanExpression differentFieldExpr(org.codehaus.groovy.ast.FieldNode,org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.expr.BooleanExpression differentPropertyExpr(org.codehaus.groovy.ast.PropertyNode,org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.expr.BooleanExpression equalsNullExpr(org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.expr.BooleanExpression identicalExpr(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.expr.BooleanExpression isInstanceOf(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.ClassNode)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.expr.BooleanExpression isInstanceof(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.expr.BooleanExpression isOneExpr(org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.expr.BooleanExpression isTrueExpr(org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.expr.BooleanExpression isZeroExpr(org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.expr.BooleanExpression notNullExpr(org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.expr.Expression findArg(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.stmt.ExpressionStatement declStatement(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.stmt.IfStatement returnFalseIfNull(org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.stmt.IfStatement returnTrueIfIdentical(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.stmt.Statement assignStatement(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.stmt.Statement createConstructorStatementDefault(org.codehaus.groovy.ast.FieldNode)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.stmt.Statement returnFalseIfFieldNotEqual(org.codehaus.groovy.ast.FieldNode,org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.stmt.Statement returnFalseIfNotInstanceof(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.stmt.Statement returnFalseIfPropertyNotEqual(org.codehaus.groovy.ast.PropertyNode,org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.stmt.Statement returnFalseIfWrongType(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
meth public static org.codehaus.groovy.ast.stmt.Statement safeExpression(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
 anno 0 java.lang.Deprecated()
supr java.lang.Object

CLSS public abstract org.codehaus.groovy.transform.AbstractASTTransformation
cons public init()
fld protected org.codehaus.groovy.control.SourceUnit sourceUnit
fld public final static org.codehaus.groovy.ast.ClassNode RETENTION_CLASSNODE
intf groovyjarjarasm.asm.Opcodes
intf org.codehaus.groovy.transform.ASTTransformation
intf org.codehaus.groovy.transform.ErrorCollecting
meth protected boolean checkIncludeExclude(org.codehaus.groovy.ast.AnnotationNode,java.util.List<java.lang.String>,java.util.List<java.lang.String>,java.lang.String)
 anno 0 java.lang.Deprecated()
meth protected boolean checkIncludeExcludeUndefinedAware(org.codehaus.groovy.ast.AnnotationNode,java.util.List<java.lang.String>,java.util.List<java.lang.String>,java.lang.String)
meth protected boolean checkNotInterface(org.codehaus.groovy.ast.ClassNode,java.lang.String)
meth protected java.util.List<org.codehaus.groovy.ast.AnnotationNode> copyAnnotatedNodeAnnotations(org.codehaus.groovy.ast.AnnotatedNode,java.lang.String)
meth protected void checkIncludeExclude(org.codehaus.groovy.ast.AnnotationNode,java.util.List<java.lang.String>,java.util.List<java.lang.String>,java.util.List<org.codehaus.groovy.ast.ClassNode>,java.util.List<org.codehaus.groovy.ast.ClassNode>,java.lang.String)
 anno 0 java.lang.Deprecated()
meth protected void checkIncludeExcludeUndefinedAware(org.codehaus.groovy.ast.AnnotationNode,java.util.List<java.lang.String>,java.util.List<java.lang.String>,java.util.List<org.codehaus.groovy.ast.ClassNode>,java.util.List<org.codehaus.groovy.ast.ClassNode>,java.lang.String)
meth protected void init(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
meth public boolean checkPropertyList(org.codehaus.groovy.ast.ClassNode,java.util.List<java.lang.String>,java.lang.String,org.codehaus.groovy.ast.AnnotationNode,java.lang.String,boolean)
meth public boolean checkPropertyList(org.codehaus.groovy.ast.ClassNode,java.util.List<java.lang.String>,java.lang.String,org.codehaus.groovy.ast.AnnotationNode,java.lang.String,boolean,boolean,boolean)
meth public boolean checkPropertyList(org.codehaus.groovy.ast.ClassNode,java.util.List<java.lang.String>,java.lang.String,org.codehaus.groovy.ast.AnnotationNode,java.lang.String,boolean,boolean,boolean,boolean,boolean)
meth public boolean hasAnnotation(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
meth public boolean memberHasValue(org.codehaus.groovy.ast.AnnotationNode,java.lang.String,java.lang.Object)
meth public int getMemberIntValue(org.codehaus.groovy.ast.AnnotationNode,java.lang.String)
meth public java.lang.Object getMemberValue(org.codehaus.groovy.ast.AnnotationNode,java.lang.String)
meth public java.lang.String getAnnotationName()
meth public java.util.List<org.codehaus.groovy.ast.ClassNode> getClassList(org.codehaus.groovy.ast.AnnotationNode,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public java.util.List<org.codehaus.groovy.ast.ClassNode> getMemberClassList(org.codehaus.groovy.ast.AnnotationNode,java.lang.String)
meth public org.codehaus.groovy.ast.ClassNode getMemberClassValue(org.codehaus.groovy.ast.AnnotationNode,java.lang.String)
meth public org.codehaus.groovy.ast.ClassNode getMemberClassValue(org.codehaus.groovy.ast.AnnotationNode,java.lang.String,org.codehaus.groovy.ast.ClassNode)
meth public static boolean deemedInternalName(java.lang.String)
meth public static boolean shouldSkip(java.lang.String,java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth public static boolean shouldSkip(java.lang.String,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean)
meth public static boolean shouldSkipOnDescriptor(boolean,java.util.Map,org.codehaus.groovy.ast.MethodNode,java.util.List<org.codehaus.groovy.ast.ClassNode>,java.util.List<org.codehaus.groovy.ast.ClassNode>)
 anno 0 java.lang.Deprecated()
meth public static boolean shouldSkipOnDescriptorUndefinedAware(boolean,java.util.Map,org.codehaus.groovy.ast.MethodNode,java.util.List<org.codehaus.groovy.ast.ClassNode>,java.util.List<org.codehaus.groovy.ast.ClassNode>)
meth public static boolean shouldSkipUndefinedAware(java.lang.String,java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth public static boolean shouldSkipUndefinedAware(java.lang.String,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean)
meth public static java.lang.String getMemberStringValue(org.codehaus.groovy.ast.AnnotationNode,java.lang.String)
meth public static java.lang.String getMemberStringValue(org.codehaus.groovy.ast.AnnotationNode,java.lang.String,java.lang.String)
meth public static java.util.List<java.lang.String> getMemberList(org.codehaus.groovy.ast.AnnotationNode,java.lang.String)
 anno 0 java.lang.Deprecated()
meth public static java.util.List<java.lang.String> getMemberStringList(org.codehaus.groovy.ast.AnnotationNode,java.lang.String)
meth public static java.util.List<java.lang.String> tokenize(java.lang.String)
meth public static org.codehaus.groovy.ast.ClassNode nonGeneric(org.codehaus.groovy.ast.ClassNode)
 anno 0 java.lang.Deprecated()
meth public void addError(java.lang.String,org.codehaus.groovy.ast.ASTNode)
supr java.lang.Object

CLSS public abstract org.codehaus.groovy.transform.AbstractInterruptibleASTTransformation
cons public init()
fld protected boolean applyToAllClasses
fld protected boolean applyToAllMembers
fld protected boolean checkOnMethodStart
fld protected final static java.lang.String CHECK_METHOD_START_MEMBER = "checkOnMethodStart"
fld protected final static java.lang.String THROWN_EXCEPTION_TYPE = "thrown"
fld protected org.codehaus.groovy.ast.ClassNode thrownExceptionType
fld protected org.codehaus.groovy.control.SourceUnit source
intf groovyjarjarasm.asm.Opcodes
intf org.codehaus.groovy.transform.ASTTransformation
meth protected abstract java.lang.String getErrorMessage()
meth protected abstract org.codehaus.groovy.ast.ClassNode type()
meth protected abstract org.codehaus.groovy.ast.expr.Expression createCondition()
meth protected final org.codehaus.groovy.ast.stmt.Statement wrapBlock(org.codehaus.groovy.ast.stmt.Statement)
meth protected org.codehaus.groovy.ast.stmt.Statement createInterruptStatement()
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth protected static boolean getBooleanAnnotationParameter(org.codehaus.groovy.ast.AnnotationNode,java.lang.String,boolean)
meth protected static org.codehaus.groovy.ast.ClassNode getClassAnnotationParameter(org.codehaus.groovy.ast.AnnotationNode,java.lang.String,org.codehaus.groovy.ast.ClassNode)
meth protected static void internalError(java.lang.String)
meth protected void setupTransform(org.codehaus.groovy.ast.AnnotationNode)
meth public final void visitDoWhileLoop(org.codehaus.groovy.ast.stmt.DoWhileStatement)
meth public final void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement)
meth public final void visitWhileLoop(org.codehaus.groovy.ast.stmt.WhileStatement)
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds APPLY_TO_ALL_CLASSES,APPLY_TO_ALL_MEMBERS

CLSS public org.codehaus.groovy.transform.AnnotationCollectorTransform
cons public init()
innr public static ClassChanger
meth protected java.util.List<org.codehaus.groovy.ast.AnnotationNode> getTargetAnnotationList(org.codehaus.groovy.ast.AnnotationNode,org.codehaus.groovy.ast.AnnotationNode,org.codehaus.groovy.control.SourceUnit)
meth protected void addError(java.lang.String,org.codehaus.groovy.ast.ASTNode,org.codehaus.groovy.control.SourceUnit)
meth public java.util.List<org.codehaus.groovy.ast.AnnotationNode> visit(org.codehaus.groovy.ast.AnnotationNode,org.codehaus.groovy.ast.AnnotationNode,org.codehaus.groovy.ast.AnnotatedNode,org.codehaus.groovy.control.SourceUnit)
supr java.lang.Object

CLSS public static org.codehaus.groovy.transform.AnnotationCollectorTransform$ClassChanger
 outer org.codehaus.groovy.transform.AnnotationCollectorTransform
cons public init()
meth public void transformClass(org.codehaus.groovy.ast.ClassNode)
supr java.lang.Object

CLSS public org.codehaus.groovy.transform.AutoCloneASTTransformation
cons public init()
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds BAIS_TYPE,BAOS_TYPE,CLONEABLE_TYPE,INVOKER_TYPE,MY_CLASS,MY_TYPE,MY_TYPE_NAME,OIS_TYPE,OOS_TYPE

CLSS public org.codehaus.groovy.transform.AutoFinalASTTransformation
cons public init()
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds MY_CLASS,MY_TYPE,MY_TYPE_NAME,candidate

CLSS public org.codehaus.groovy.transform.AutoImplementASTTransformation
cons public init()
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds MY_CLASS,MY_TYPE,MY_TYPE_NAME

CLSS public org.codehaus.groovy.transform.BaseScriptASTTransformation
cons public init()
fld public final static org.codehaus.groovy.ast.ClassNode MY_TYPE
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds CONTEXT_CTOR_PARAMETERS,MY_CLASS,MY_TYPE_NAME

CLSS public org.codehaus.groovy.transform.BuilderASTTransformation
cons public init()
fld public final static java.lang.String MY_TYPE_NAME
fld public final static org.codehaus.groovy.ast.ClassNode[] NO_EXCEPTIONS
fld public final static org.codehaus.groovy.ast.Parameter[] NO_PARAMS
innr public abstract interface static BuilderStrategy
innr public abstract static AbstractBuilderStrategy
intf groovy.transform.CompilationUnitAware
meth public void setCompilationUnit(org.codehaus.groovy.control.CompilationUnit)
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds MY_CLASS,MY_TYPE,compilationUnit

CLSS public abstract static org.codehaus.groovy.transform.BuilderASTTransformation$AbstractBuilderStrategy
 outer org.codehaus.groovy.transform.BuilderASTTransformation
cons public init()
innr protected static PropertyInfo
intf org.codehaus.groovy.transform.BuilderASTTransformation$BuilderStrategy
meth protected boolean getIncludeExclude(org.codehaus.groovy.transform.BuilderASTTransformation,org.codehaus.groovy.ast.AnnotationNode,org.codehaus.groovy.ast.ClassNode,java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth protected boolean unsupportedAttribute(org.codehaus.groovy.transform.BuilderASTTransformation,org.codehaus.groovy.ast.AnnotationNode,java.lang.String)
meth protected boolean unsupportedAttribute(org.codehaus.groovy.transform.BuilderASTTransformation,org.codehaus.groovy.ast.AnnotationNode,java.lang.String,java.lang.String)
meth protected java.lang.String getSetterName(java.lang.String,java.lang.String)
meth protected java.util.List<org.codehaus.groovy.ast.FieldNode> getFields(org.codehaus.groovy.transform.BuilderASTTransformation,org.codehaus.groovy.ast.AnnotationNode,org.codehaus.groovy.ast.ClassNode)
meth protected java.util.List<org.codehaus.groovy.transform.BuilderASTTransformation$AbstractBuilderStrategy$PropertyInfo> getPropertyInfoFromClassNode(org.codehaus.groovy.transform.BuilderASTTransformation,org.codehaus.groovy.ast.AnnotationNode,org.codehaus.groovy.ast.ClassNode,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean,boolean)
meth protected java.util.List<org.codehaus.groovy.transform.BuilderASTTransformation$AbstractBuilderStrategy$PropertyInfo> getPropertyInfos(org.codehaus.groovy.transform.BuilderASTTransformation,org.codehaus.groovy.ast.AnnotationNode,org.codehaus.groovy.ast.ClassNode,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean,boolean)
meth protected static java.util.List<org.codehaus.groovy.transform.BuilderASTTransformation$AbstractBuilderStrategy$PropertyInfo> getPropertyInfoFromBeanInfo(org.codehaus.groovy.ast.ClassNode,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean)
meth protected static java.util.List<org.codehaus.groovy.transform.BuilderASTTransformation$AbstractBuilderStrategy$PropertyInfo> getPropertyInfoFromClassNode(org.codehaus.groovy.ast.ClassNode,java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth protected static java.util.List<org.codehaus.groovy.transform.BuilderASTTransformation$AbstractBuilderStrategy$PropertyInfo> getPropertyInfoFromClassNode(org.codehaus.groovy.ast.ClassNode,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean)
meth protected void checkKnownField(org.codehaus.groovy.transform.BuilderASTTransformation,org.codehaus.groovy.ast.AnnotationNode,java.lang.String,java.util.List<org.codehaus.groovy.ast.FieldNode>)
meth protected void checkKnownProperty(org.codehaus.groovy.transform.BuilderASTTransformation,org.codehaus.groovy.ast.AnnotationNode,java.lang.String,java.util.List<org.codehaus.groovy.transform.BuilderASTTransformation$AbstractBuilderStrategy$PropertyInfo>)
supr java.lang.Object

CLSS protected static org.codehaus.groovy.transform.BuilderASTTransformation$AbstractBuilderStrategy$PropertyInfo
 outer org.codehaus.groovy.transform.BuilderASTTransformation$AbstractBuilderStrategy
cons public init(java.lang.String,org.codehaus.groovy.ast.ClassNode)
meth public java.lang.String getName()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public void setName(java.lang.String)
meth public void setType(org.codehaus.groovy.ast.ClassNode)
supr java.lang.Object
hfds name,type

CLSS public abstract interface static org.codehaus.groovy.transform.BuilderASTTransformation$BuilderStrategy
 outer org.codehaus.groovy.transform.BuilderASTTransformation
meth public abstract void build(org.codehaus.groovy.transform.BuilderASTTransformation,org.codehaus.groovy.ast.AnnotatedNode,org.codehaus.groovy.ast.AnnotationNode)

CLSS public org.codehaus.groovy.transform.CategoryASTTransformation
cons public init()
intf groovyjarjarasm.asm.Opcodes
intf org.codehaus.groovy.transform.ASTTransformation
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr java.lang.Object
hfds thisExpression

CLSS public org.codehaus.groovy.transform.CompileDynamicProcessor
cons public init()
meth public java.util.List<org.codehaus.groovy.ast.AnnotationNode> visit(org.codehaus.groovy.ast.AnnotationNode,org.codehaus.groovy.ast.AnnotationNode,org.codehaus.groovy.ast.AnnotatedNode,org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AnnotationCollectorTransform
hfds COMPILESTATIC_NODE,TYPECHECKINGMODE_NODE

CLSS public org.codehaus.groovy.transform.ConditionalInterruptibleASTTransformation
cons public init()
intf groovy.lang.GroovyObject
meth protected java.lang.String getErrorMessage()
meth protected org.codehaus.groovy.ast.ClassNode type()
meth protected org.codehaus.groovy.ast.expr.Expression createCondition()
meth protected void setupTransform(org.codehaus.groovy.ast.AnnotationNode)
meth public void visitAnnotations(org.codehaus.groovy.ast.AnnotatedNode)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public void visitField(org.codehaus.groovy.ast.FieldNode)
meth public void visitMethod(org.codehaus.groovy.ast.MethodNode)
meth public void visitProperty(org.codehaus.groovy.ast.PropertyNode)
supr org.codehaus.groovy.transform.AbstractInterruptibleASTTransformation
hfds MY_TYPE,conditionCallExpression,conditionMethod,conditionNode,currentClass

CLSS public org.codehaus.groovy.transform.DelegateASTTransformation
cons public init()
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds DEPRECATED_TYPE,GROOVYOBJECT_TYPE,LAZY_TYPE,MEMBER_ALL_NAMES,MEMBER_DEPRECATED,MEMBER_EXCLUDES,MEMBER_EXCLUDE_TYPES,MEMBER_INCLUDES,MEMBER_INCLUDE_TYPES,MEMBER_INTERFACES,MEMBER_METHOD_ANNOTATIONS,MEMBER_PARAMETER_ANNOTATIONS,MY_CLASS,MY_TYPE,MY_TYPE_NAME
hcls DelegateDescription

CLSS public org.codehaus.groovy.transform.EqualsAndHashCodeASTTransformation
cons public init()
meth public static void createEquals(org.codehaus.groovy.ast.ClassNode,boolean,boolean,boolean,java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth public static void createEquals(org.codehaus.groovy.ast.ClassNode,boolean,boolean,boolean,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean)
meth public static void createEquals(org.codehaus.groovy.ast.ClassNode,boolean,boolean,boolean,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean,boolean)
meth public static void createHashCode(org.codehaus.groovy.ast.ClassNode,boolean,boolean,boolean,java.util.List<java.lang.String>,java.util.List<java.lang.String>)
meth public static void createHashCode(org.codehaus.groovy.ast.ClassNode,boolean,boolean,boolean,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean)
meth public static void createHashCode(org.codehaus.groovy.ast.ClassNode,boolean,boolean,boolean,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean,boolean)
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds HASHUTIL_TYPE,MY_CLASS,MY_TYPE,MY_TYPE_NAME,OBJECT_TYPE

CLSS public abstract interface org.codehaus.groovy.transform.ErrorCollecting
meth public abstract void addError(java.lang.String,org.codehaus.groovy.ast.ASTNode)

CLSS public org.codehaus.groovy.transform.ExternalizeMethodsASTTransformation
cons public init()
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds EXTERNALIZABLE_TYPE,MY_CLASS,MY_TYPE,MY_TYPE_NAME,OBJECTINPUT_TYPE,OBJECTOUTPUT_TYPE

CLSS public org.codehaus.groovy.transform.ExternalizeVerifierASTTransformation
cons public init()
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds EXTERNALIZABLE_TYPE,MY_CLASS,MY_TYPE,MY_TYPE_NAME,SERIALIZABLE_TYPE

CLSS public org.codehaus.groovy.transform.FieldASTTransformation
cons public init()
intf groovyjarjarasm.asm.Opcodes
intf org.codehaus.groovy.transform.ASTTransformation
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth public org.codehaus.groovy.ast.expr.Expression transform(org.codehaus.groovy.ast.expr.Expression)
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
meth public void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public void visitConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth public void visitExpressionStatement(org.codehaus.groovy.ast.stmt.ExpressionStatement)
meth public void visitMethod(org.codehaus.groovy.ast.MethodNode)
supr org.codehaus.groovy.ast.ClassCodeExpressionTransformer
hfds ASTTRANSFORMCLASS_TYPE,LAZY_TYPE,MY_CLASS,MY_TYPE,MY_TYPE_NAME,OPTION_TYPE,candidate,currentAIC,currentClosure,fieldNode,insideScriptBody,sourceUnit,variableName

CLSS public abstract interface !annotation org.codehaus.groovy.transform.GroovyASTTransformation
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault org.codehaus.groovy.control.CompilePhase phase()

CLSS public abstract interface !annotation org.codehaus.groovy.transform.GroovyASTTransformationClass
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.Class[] classes()
meth public abstract !hasdefault java.lang.String[] value()

CLSS public org.codehaus.groovy.transform.ImmutableASTTransformation
cons public init()
fld public final static org.codehaus.groovy.ast.ClassNode MY_TYPE
intf groovy.transform.CompilationUnitAware
meth public java.lang.String getAnnotationName()
meth public static java.lang.Object checkImmutable(java.lang.Class<?>,java.lang.String,java.lang.Object)
meth public static java.lang.Object checkImmutable(java.lang.Class<?>,java.lang.String,java.lang.Object,java.util.List<java.lang.String>,java.util.List<java.lang.Class>)
meth public static java.lang.Object checkImmutable(java.lang.String,java.lang.String,java.lang.Object)
meth public static void checkPropNames(java.lang.Object,java.util.Map<java.lang.String,java.lang.Object>)
meth public void setCompilationUnit(org.codehaus.groovy.control.CompilationUnit)
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds COPY_WITH_METHOD,HMAP_TYPE,MEMBER_ADD_COPY_WITH,MY_CLASS,MY_TYPE_NAME,compilationUnit

CLSS public org.codehaus.groovy.transform.IndexedPropertyASTTransformation
cons public init()
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds LIST_TYPE,MY_CLASS,MY_TYPE,MY_TYPE_NAME

CLSS public org.codehaus.groovy.transform.InheritConstructorsASTTransformation
cons public init()
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds MY_CLASS,MY_TYPE,MY_TYPE_NAME

CLSS public org.codehaus.groovy.transform.LazyASTTransformation
cons public init()
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds NULL_EXPR,SOFT_REF

CLSS public org.codehaus.groovy.transform.LogASTTransformation
cons public init()
fld public final static java.lang.String DEFAULT_CATEGORY_NAME = "##default-category-name##"
innr public abstract interface static LoggingStrategy
innr public abstract static AbstractLoggingStrategy
intf groovy.transform.CompilationUnitAware
meth public void setCompilationUnit(org.codehaus.groovy.control.CompilationUnit)
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds compilationUnit

CLSS public abstract static org.codehaus.groovy.transform.LogASTTransformation$AbstractLoggingStrategy
 outer org.codehaus.groovy.transform.LogASTTransformation
cons protected init()
cons protected init(groovy.lang.GroovyClassLoader)
fld protected final groovy.lang.GroovyClassLoader loader
intf org.codehaus.groovy.transform.LogASTTransformation$LoggingStrategy
meth protected org.codehaus.groovy.ast.ClassNode classNode(java.lang.String)
meth public java.lang.String getCategoryName(org.codehaus.groovy.ast.ClassNode,java.lang.String)
supr java.lang.Object

CLSS public abstract interface static org.codehaus.groovy.transform.LogASTTransformation$LoggingStrategy
 outer org.codehaus.groovy.transform.LogASTTransformation
meth public abstract boolean isLoggingMethod(java.lang.String)
meth public abstract java.lang.String getCategoryName(org.codehaus.groovy.ast.ClassNode,java.lang.String)
meth public abstract org.codehaus.groovy.ast.FieldNode addLoggerFieldToClass(org.codehaus.groovy.ast.ClassNode,java.lang.String,java.lang.String)
meth public abstract org.codehaus.groovy.ast.expr.Expression wrapLoggingMethodCall(org.codehaus.groovy.ast.expr.Expression,java.lang.String,org.codehaus.groovy.ast.expr.Expression)

CLSS public org.codehaus.groovy.transform.MapConstructorASTTransformation
cons public init()
intf groovy.transform.CompilationUnitAware
meth public java.lang.String getAnnotationName()
meth public void setCompilationUnit(org.codehaus.groovy.control.CompilationUnit)
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds LHMAP_TYPE,MAP_TYPE,MY_CLASS,MY_TYPE,MY_TYPE_NAME,compilationUnit

CLSS public org.codehaus.groovy.transform.MemoizedASTTransformation
cons public init()
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds CLOSURE_CALL_METHOD_NAME,CLOSURE_LABEL,MAX_CACHE_SIZE_NAME,MEMOIZE_AT_LEAST_METHOD_NAME,MEMOIZE_AT_MOST_METHOD_NAME,MEMOIZE_BETWEEN_METHOD_NAME,MEMOIZE_METHOD_NAME,METHOD_LABEL,MY_CLASS,MY_TYPE,MY_TYPE_NAME,OVERRIDE_CLASSNODE,PROTECTED_CACHE_SIZE_NAME

CLSS public org.codehaus.groovy.transform.NamedVariantASTTransformation
cons public init()
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds MY_CLASS,MY_TYPE,MY_TYPE_NAME,NAMED_DELEGATE_TYPE,NAMED_PARAM_TYPE

CLSS public org.codehaus.groovy.transform.NewifyASTTransformation
cons public init()
intf org.codehaus.groovy.transform.ASTTransformation
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth public org.codehaus.groovy.ast.expr.Expression transform(org.codehaus.groovy.ast.expr.Expression)
meth public static java.lang.String extractName(java.lang.String)
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.ast.ClassCodeExpressionTransformer
hfds BASE_BAD_PARAM_ERROR,MY_NAME,MY_TYPE,auto,candidate,classNamePattern,classesToNewify,extractNamePattern,globalClasses,nameToGlobalClassesNodesMap,nameToInnerClassesNodesMap,source
hcls NewifyClassData

CLSS public org.codehaus.groovy.transform.PackageScopeASTTransformation
cons public init()
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds LEGACY_TYPE_NAME,MY_CLASS,MY_TYPE,MY_TYPE_NAME,TARGET_CLASS,TARGET_CLASS_NAME

CLSS public org.codehaus.groovy.transform.ReadWriteLockASTTransformation
cons public init()
fld public final static java.lang.String DEFAULT_INSTANCE_LOCKNAME = "$reentrantlock"
fld public final static java.lang.String DEFAULT_STATIC_LOCKNAME = "$REENTRANTLOCK"
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds LOCK_TYPE,READ_LOCK_TYPE,WRITE_LOCK_TYPE

CLSS public org.codehaus.groovy.transform.SingletonASTTransformation
cons public init()
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation

CLSS public org.codehaus.groovy.transform.SortableASTTransformation
cons public init()
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds ARG0,ARG1,COMPARABLE_TYPE,COMPARATOR_TYPE,MY_TYPE,MY_TYPE_NAME,OTHER,OTHER_HASH,THIS_HASH,VALUE

CLSS public org.codehaus.groovy.transform.SourceURIASTTransformation
cons public init()
meth protected java.net.URI getSourceURI(org.codehaus.groovy.ast.AnnotationNode)
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds MY_CLASS,MY_TYPE,MY_TYPE_NAME,URI_TYPE

CLSS public org.codehaus.groovy.transform.StaticTypesTransformation
cons public init()
fld protected org.codehaus.groovy.control.CompilationUnit compilationUnit
fld public final static java.lang.String STATIC_ERROR_PREFIX = "[Static type checking] - "
intf groovy.transform.CompilationUnitAware
intf org.codehaus.groovy.transform.ASTTransformation
meth protected org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor newVisitor(org.codehaus.groovy.control.SourceUnit,org.codehaus.groovy.ast.ClassNode)
meth protected void addTypeCheckingExtensions(org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor,org.codehaus.groovy.ast.expr.Expression)
meth public void setCompilationUnit(org.codehaus.groovy.control.CompilationUnit)
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr java.lang.Object

CLSS public org.codehaus.groovy.transform.SynchronizedASTTransformation
cons public init()
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds MY_CLASS,MY_TYPE,MY_TYPE_NAME

CLSS public org.codehaus.groovy.transform.ThreadInterruptibleASTTransformation
cons public init()
intf groovy.lang.GroovyObject
meth protected java.lang.String getErrorMessage()
meth protected org.codehaus.groovy.ast.ClassNode type()
meth protected org.codehaus.groovy.ast.expr.Expression createCondition()
meth public void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public void visitMethod(org.codehaus.groovy.ast.MethodNode)
supr org.codehaus.groovy.transform.AbstractInterruptibleASTTransformation
hfds CURRENTTHREAD_METHOD,ISINTERRUPTED_METHOD,MY_TYPE,THREAD_TYPE

CLSS public org.codehaus.groovy.transform.TimedInterruptibleASTTransformation
cons public init()
intf groovy.lang.GroovyObject
meth public static java.lang.Object getConstantAnnotationParameter(org.codehaus.groovy.ast.AnnotationNode,java.lang.String,java.lang.Class,java.lang.Object)
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds APPLY_TO_ALL_CLASSES,APPLY_TO_ALL_MEMBERS,CHECK_METHOD_START_MEMBER,MY_TYPE,THROWN_EXCEPTION_TYPE
hcls TimedInterruptionVisitor

CLSS public final org.codehaus.groovy.transform.TimedInterruptibleASTTransformation$_visit_closure1
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference,groovy.lang.Reference,groovy.lang.Reference,groovy.lang.Reference,groovy.lang.Reference,groovy.lang.Reference,groovy.lang.Reference,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object call(org.codehaus.groovy.ast.ClassNode)
meth public java.lang.Object doCall(org.codehaus.groovy.ast.ClassNode)
meth public java.lang.Object getApplyToAllClasses()
meth public java.lang.Object getApplyToAllMembers()
meth public java.lang.Object getCheckOnMethodStart()
meth public java.lang.Object getMaximum()
meth public java.lang.Object getThrown()
meth public org.codehaus.groovy.ast.AnnotationNode getNode()
meth public org.codehaus.groovy.ast.expr.Expression getUnit()
meth public org.codehaus.groovy.control.SourceUnit getSource()
supr groovy.lang.Closure

CLSS public final org.codehaus.groovy.transform.TimedInterruptibleASTTransformation$_visit_closure2
cons public init(java.lang.Object,java.lang.Object,groovy.lang.Reference,groovy.lang.Reference,groovy.lang.Reference,groovy.lang.Reference,groovy.lang.Reference,groovy.lang.Reference,groovy.lang.Reference,groovy.lang.Reference)
intf org.codehaus.groovy.runtime.GeneratedClosure
meth public java.lang.Object call(org.codehaus.groovy.ast.ClassNode)
meth public java.lang.Object doCall(org.codehaus.groovy.ast.ClassNode)
meth public java.lang.Object getApplyToAllClasses()
meth public java.lang.Object getApplyToAllMembers()
meth public java.lang.Object getCheckOnMethodStart()
meth public java.lang.Object getMaximum()
meth public java.lang.Object getThrown()
meth public org.codehaus.groovy.ast.AnnotationNode getNode()
meth public org.codehaus.groovy.ast.expr.Expression getUnit()
meth public org.codehaus.groovy.control.SourceUnit getSource()
supr groovy.lang.Closure

CLSS public org.codehaus.groovy.transform.ToStringASTTransformation
cons public init()
meth public static void createToString(org.codehaus.groovy.ast.ClassNode,boolean,boolean,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean)
meth public static void createToString(org.codehaus.groovy.ast.ClassNode,boolean,boolean,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean,boolean)
meth public static void createToString(org.codehaus.groovy.ast.ClassNode,boolean,boolean,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean,boolean,boolean)
meth public static void createToString(org.codehaus.groovy.ast.ClassNode,boolean,boolean,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean,boolean,boolean,boolean)
meth public static void createToString(org.codehaus.groovy.ast.ClassNode,boolean,boolean,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean,boolean,boolean,boolean,boolean)
meth public static void createToString(org.codehaus.groovy.ast.ClassNode,boolean,boolean,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean,boolean,boolean,boolean,boolean,boolean)
meth public static void createToString(org.codehaus.groovy.ast.ClassNode,boolean,boolean,java.util.List<java.lang.String>,java.util.List<java.lang.String>,boolean,boolean,boolean,boolean,boolean,boolean,boolean,boolean)
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds INVOKER_TYPE,MY_CLASS,MY_TYPE,MY_TYPE_NAME,STRINGBUILDER_TYPE
hcls ToStringElement

CLSS public org.codehaus.groovy.transform.TupleConstructorASTTransformation
cons public init()
intf groovy.transform.CompilationUnitAware
meth public java.lang.String getAnnotationName()
meth public static void addSpecialMapConstructors(int,org.codehaus.groovy.ast.ClassNode,java.lang.String,boolean)
meth public void setCompilationUnit(org.codehaus.groovy.control.CompilationUnit)
meth public void visit(org.codehaus.groovy.ast.ASTNode[],org.codehaus.groovy.control.SourceUnit)
supr org.codehaus.groovy.transform.AbstractASTTransformation
hfds CHECK_METHOD_TYPE,LHMAP_TYPE,MAP_CONSTRUCTOR_CLASS,MY_CLASS,MY_TYPE,MY_TYPE_NAME,compilationUnit,primitivesInitialValues

CLSS public org.codehaus.groovy.transform.stc.AbstractTypeCheckingExtension
cons public init(org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor)
fld protected boolean debug
fld protected boolean handled
fld protected final org.codehaus.groovy.transform.stc.TypeCheckingContext context
meth protected !varargs java.lang.Object safeCall(groovy.lang.Closure,java.lang.Object[])
meth public !varargs boolean argTypesMatches(org.codehaus.groovy.ast.ClassNode[],java.lang.Class[])
meth public !varargs boolean argTypesMatches(org.codehaus.groovy.ast.expr.MethodCall,java.lang.Class[])
meth public !varargs boolean firstArgTypesMatches(org.codehaus.groovy.ast.ClassNode[],java.lang.Class[])
meth public !varargs boolean firstArgTypesMatches(org.codehaus.groovy.ast.expr.MethodCall,java.lang.Class[])
meth public <%0 extends java.lang.Object> {%%0} withTypeChecker(groovy.lang.Closure<{%%0}>)
meth public boolean argTypeMatches(org.codehaus.groovy.ast.ClassNode[],int,java.lang.Class)
meth public boolean argTypeMatches(org.codehaus.groovy.ast.expr.MethodCall,int,java.lang.Class)
meth public boolean isAnnotatedBy(org.codehaus.groovy.ast.ASTNode,java.lang.Class)
meth public boolean isAnnotatedBy(org.codehaus.groovy.ast.ASTNode,org.codehaus.groovy.ast.ClassNode)
meth public boolean isDynamic(org.codehaus.groovy.ast.expr.VariableExpression)
meth public boolean isExtensionMethod(org.codehaus.groovy.ast.MethodNode)
meth public boolean isGenerated(org.codehaus.groovy.ast.MethodNode)
meth public boolean isMethodCall(java.lang.Object)
meth public java.util.LinkedHashMap getCurrentScope()
meth public java.util.LinkedHashMap newScope()
meth public java.util.LinkedHashMap newScope(groovy.lang.Closure)
meth public java.util.LinkedHashMap scopeExit()
meth public java.util.LinkedHashMap scopeExit(groovy.lang.Closure)
meth public java.util.List<org.codehaus.groovy.ast.ClassNode> getEnclosingClassNodes()
meth public java.util.List<org.codehaus.groovy.ast.MethodNode> getEnclosingMethods()
meth public java.util.List<org.codehaus.groovy.ast.MethodNode> unique(org.codehaus.groovy.ast.MethodNode)
meth public java.util.List<org.codehaus.groovy.ast.expr.BinaryExpression> getEnclosingBinaryExpressionStack()
meth public java.util.List<org.codehaus.groovy.ast.expr.Expression> getEnclosingMethodCalls()
meth public java.util.List<org.codehaus.groovy.transform.stc.TypeCheckingContext$EnclosingClosure> getEnclosingClosureStack()
meth public java.util.Set<org.codehaus.groovy.ast.MethodNode> getGeneratedMethods()
meth public org.codehaus.groovy.ast.ClassNode getEnclosingClassNode()
meth public org.codehaus.groovy.ast.ClassNode popEnclosingClassNode()
meth public org.codehaus.groovy.ast.MethodNode getEnclosingMethod()
meth public org.codehaus.groovy.ast.MethodNode makeDynamic(org.codehaus.groovy.ast.expr.MethodCall)
meth public org.codehaus.groovy.ast.MethodNode makeDynamic(org.codehaus.groovy.ast.expr.MethodCall,org.codehaus.groovy.ast.ClassNode)
meth public org.codehaus.groovy.ast.MethodNode newMethod(java.lang.String,java.lang.Class)
meth public org.codehaus.groovy.ast.MethodNode newMethod(java.lang.String,java.util.concurrent.Callable<org.codehaus.groovy.ast.ClassNode>)
meth public org.codehaus.groovy.ast.MethodNode newMethod(java.lang.String,org.codehaus.groovy.ast.ClassNode)
meth public org.codehaus.groovy.ast.MethodNode popEnclosingMethod()
meth public org.codehaus.groovy.ast.expr.ArgumentListExpression getArguments(org.codehaus.groovy.ast.expr.MethodCall)
meth public org.codehaus.groovy.ast.expr.BinaryExpression getEnclosingBinaryExpression()
meth public org.codehaus.groovy.ast.expr.BinaryExpression popEnclosingBinaryExpression()
meth public org.codehaus.groovy.ast.expr.Expression getEnclosingMethodCall()
meth public org.codehaus.groovy.ast.expr.Expression popEnclosingMethodCall()
meth public org.codehaus.groovy.transform.stc.TypeCheckingContext$EnclosingClosure getEnclosingClosure()
meth public org.codehaus.groovy.transform.stc.TypeCheckingContext$EnclosingClosure popEnclosingClosure()
meth public void delegatesTo(org.codehaus.groovy.ast.ClassNode)
meth public void delegatesTo(org.codehaus.groovy.ast.ClassNode,int)
meth public void delegatesTo(org.codehaus.groovy.ast.ClassNode,int,org.codehaus.groovy.transform.stc.DelegationMetadata)
meth public void log(java.lang.String)
meth public void makeDynamic(org.codehaus.groovy.ast.expr.PropertyExpression)
meth public void makeDynamic(org.codehaus.groovy.ast.expr.PropertyExpression,org.codehaus.groovy.ast.ClassNode)
meth public void makeDynamic(org.codehaus.groovy.ast.expr.VariableExpression)
meth public void makeDynamic(org.codehaus.groovy.ast.expr.VariableExpression,org.codehaus.groovy.ast.ClassNode)
meth public void popTemporaryTypeInfo()
meth public void pushEnclosingBinaryExpression(org.codehaus.groovy.ast.expr.BinaryExpression)
meth public void pushEnclosingClassNode(org.codehaus.groovy.ast.ClassNode)
meth public void pushEnclosingClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public void pushEnclosingMethod(org.codehaus.groovy.ast.MethodNode)
meth public void pushEnclosingMethodCall(org.codehaus.groovy.ast.expr.Expression)
meth public void pushTemporaryTypeInfo()
meth public void setHandled(boolean)
supr org.codehaus.groovy.transform.stc.TypeCheckingExtension
hfds LOG,generatedMethods,scopeData
hcls TypeCheckingScope

CLSS public org.codehaus.groovy.transform.stc.DefaultTypeCheckingExtension
cons public init(org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor)
fld protected final java.util.List<org.codehaus.groovy.transform.stc.TypeCheckingExtension> handlers
meth public boolean beforeMethodCall(org.codehaus.groovy.ast.expr.MethodCall)
meth public boolean beforeVisitClass(org.codehaus.groovy.ast.ClassNode)
meth public boolean beforeVisitMethod(org.codehaus.groovy.ast.MethodNode)
meth public boolean handleIncompatibleAssignment(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
meth public boolean handleIncompatibleReturnType(org.codehaus.groovy.ast.stmt.ReturnStatement,org.codehaus.groovy.ast.ClassNode)
meth public boolean handleUnresolvedAttribute(org.codehaus.groovy.ast.expr.AttributeExpression)
meth public boolean handleUnresolvedProperty(org.codehaus.groovy.ast.expr.PropertyExpression)
meth public boolean handleUnresolvedVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
meth public java.util.List<org.codehaus.groovy.ast.MethodNode> handleAmbiguousMethods(java.util.List<org.codehaus.groovy.ast.MethodNode>,org.codehaus.groovy.ast.expr.Expression)
meth public java.util.List<org.codehaus.groovy.ast.MethodNode> handleMissingMethod(org.codehaus.groovy.ast.ClassNode,java.lang.String,org.codehaus.groovy.ast.expr.ArgumentListExpression,org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.expr.MethodCall)
meth public void addHandler(org.codehaus.groovy.transform.stc.TypeCheckingExtension)
meth public void afterMethodCall(org.codehaus.groovy.ast.expr.MethodCall)
meth public void afterVisitClass(org.codehaus.groovy.ast.ClassNode)
meth public void afterVisitMethod(org.codehaus.groovy.ast.MethodNode)
meth public void finish()
meth public void onMethodSelection(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.MethodNode)
meth public void removeHandler(org.codehaus.groovy.transform.stc.TypeCheckingExtension)
meth public void setup()
supr org.codehaus.groovy.transform.stc.TypeCheckingExtension

CLSS public org.codehaus.groovy.transform.stc.ExtensionMethodNode
cons public init(org.codehaus.groovy.ast.MethodNode,java.lang.String,int,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.Parameter[],org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.stmt.Statement)
cons public init(org.codehaus.groovy.ast.MethodNode,java.lang.String,int,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.Parameter[],org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.stmt.Statement,boolean)
meth public boolean isStaticExtension()
meth public org.codehaus.groovy.ast.MethodNode getExtensionMethodNode()
supr org.codehaus.groovy.ast.MethodNode
hfds extensionMethodNode,isStaticExtension

CLSS public org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport
cons public init(org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor,java.lang.String,org.codehaus.groovy.control.CompilationUnit)
innr public abstract static TypeCheckingDSL
meth public boolean beforeMethodCall(org.codehaus.groovy.ast.expr.MethodCall)
meth public boolean beforeVisitClass(org.codehaus.groovy.ast.ClassNode)
meth public boolean beforeVisitMethod(org.codehaus.groovy.ast.MethodNode)
meth public boolean handleIncompatibleAssignment(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
meth public boolean handleIncompatibleReturnType(org.codehaus.groovy.ast.stmt.ReturnStatement,org.codehaus.groovy.ast.ClassNode)
meth public boolean handleUnresolvedAttribute(org.codehaus.groovy.ast.expr.AttributeExpression)
meth public boolean handleUnresolvedProperty(org.codehaus.groovy.ast.expr.PropertyExpression)
meth public boolean handleUnresolvedVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
meth public java.util.List<org.codehaus.groovy.ast.MethodNode> handleAmbiguousMethods(java.util.List<org.codehaus.groovy.ast.MethodNode>,org.codehaus.groovy.ast.expr.Expression)
meth public java.util.List<org.codehaus.groovy.ast.MethodNode> handleMissingMethod(org.codehaus.groovy.ast.ClassNode,java.lang.String,org.codehaus.groovy.ast.expr.ArgumentListExpression,org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.expr.MethodCall)
meth public void afterMethodCall(org.codehaus.groovy.ast.expr.MethodCall)
meth public void afterVisitClass(org.codehaus.groovy.ast.ClassNode)
meth public void afterVisitMethod(org.codehaus.groovy.ast.MethodNode)
meth public void finish()
meth public void onMethodSelection(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.MethodNode)
meth public void setDebug(boolean)
meth public void setup()
supr org.codehaus.groovy.transform.stc.AbstractTypeCheckingExtension
hfds METHOD_ALIASES,compilationUnit,eventHandlers,scriptPath

CLSS public abstract static org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport$TypeCheckingDSL
 outer org.codehaus.groovy.transform.stc.GroovyTypeCheckingExtensionSupport
cons public init()
meth public java.lang.Object getProperty(java.lang.String)
meth public java.lang.Object invokeMethod(java.lang.String,java.lang.Object)
meth public void setProperty(java.lang.String,java.lang.Object)
supr groovy.lang.Script
hfds extension

CLSS public org.codehaus.groovy.transform.stc.Receiver<%0 extends java.lang.Object>
cons public init(org.codehaus.groovy.ast.ClassNode)
cons public init(org.codehaus.groovy.ast.ClassNode,{org.codehaus.groovy.transform.stc.Receiver%0})
meth public java.lang.String toString()
meth public org.codehaus.groovy.ast.ClassNode getType()
meth public static <%0 extends java.lang.Object> org.codehaus.groovy.transform.stc.Receiver<{%%0}> make(org.codehaus.groovy.ast.ClassNode)
meth public {org.codehaus.groovy.transform.stc.Receiver%0} getData()
supr java.lang.Object
hfds data,type

CLSS public org.codehaus.groovy.transform.stc.SharedVariableCollector
cons public init(org.codehaus.groovy.control.SourceUnit)
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth public java.util.Set<org.codehaus.groovy.ast.expr.VariableExpression> getClosureSharedExpressions()
meth public void visitVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds closureSharedExpressions,unit,visited

CLSS public abstract interface org.codehaus.groovy.transform.stc.SignatureCodec
meth public abstract java.lang.String encode(org.codehaus.groovy.ast.ClassNode)
meth public abstract org.codehaus.groovy.ast.ClassNode decode(java.lang.String)

CLSS public org.codehaus.groovy.transform.stc.SignatureCodecVersion1
cons public init(java.lang.ClassLoader)
intf org.codehaus.groovy.transform.stc.SignatureCodec
meth public java.lang.String encode(org.codehaus.groovy.ast.ClassNode)
meth public org.codehaus.groovy.ast.ClassNode decode(java.lang.String)
supr java.lang.Object
hfds classLoader

CLSS public abstract org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport
cons public init()
fld protected final static java.lang.Object EXTENSION_METHOD_CACHE
fld protected final static java.util.Comparator<org.codehaus.groovy.ast.MethodNode> DGM_METHOD_NODE_COMPARATOR
fld protected final static java.util.Map<java.lang.String,java.lang.Integer> NUMBER_OPS
fld protected final static java.util.Map<org.codehaus.groovy.ast.ClassNode,java.lang.Integer> NUMBER_TYPES
fld protected final static org.codehaus.groovy.ast.ClassNode ArrayList_TYPE
fld protected final static org.codehaus.groovy.ast.ClassNode Collection_TYPE
fld protected final static org.codehaus.groovy.ast.ClassNode Deprecated_TYPE
fld protected final static org.codehaus.groovy.ast.ClassNode GSTRING_STRING_CLASSNODE
fld protected final static org.codehaus.groovy.ast.ClassNode Matcher_TYPE
fld protected final static org.codehaus.groovy.ast.ClassNode UNKNOWN_PARAMETER_TYPE
meth protected static boolean isArrayAccessExpression(org.codehaus.groovy.ast.expr.Expression)
meth protected static boolean typeCheckMethodArgumentWithGenerics(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,boolean)
meth protected static boolean typeCheckMethodsWithGenerics(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.MethodNode)
meth protected static java.util.Set<org.codehaus.groovy.ast.MethodNode> findDGMMethodsForClassNode(java.lang.ClassLoader,org.codehaus.groovy.ast.ClassNode,java.lang.String)
meth protected static java.util.Set<org.codehaus.groovy.ast.MethodNode> findDGMMethodsForClassNode(org.codehaus.groovy.ast.ClassNode,java.lang.String)
 anno 0 java.lang.Deprecated()
meth protected static org.codehaus.groovy.ast.ClassNode fullyResolveType(org.codehaus.groovy.ast.ClassNode,java.util.Map<java.lang.String,org.codehaus.groovy.ast.GenericsType>)
meth protected static org.codehaus.groovy.ast.GenericsType fullyResolve(org.codehaus.groovy.ast.GenericsType,java.util.Map<java.lang.String,org.codehaus.groovy.ast.GenericsType>)
meth protected static org.codehaus.groovy.ast.Variable findTargetVariable(org.codehaus.groovy.ast.expr.VariableExpression)
meth protected static void findDGMMethodsForClassNode(java.lang.ClassLoader,org.codehaus.groovy.ast.ClassNode,java.lang.String,java.util.TreeSet<org.codehaus.groovy.ast.MethodNode>)
meth protected static void findDGMMethodsForClassNode(org.codehaus.groovy.ast.ClassNode,java.lang.String,java.util.TreeSet<org.codehaus.groovy.ast.MethodNode>)
 anno 0 java.lang.Deprecated()
meth public !varargs static java.util.List<org.codehaus.groovy.ast.MethodNode> chooseBestMethod(org.codehaus.groovy.ast.ClassNode,java.util.Collection<org.codehaus.groovy.ast.MethodNode>,org.codehaus.groovy.ast.ClassNode[])
meth public static boolean checkCompatibleAssignmentTypes(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
meth public static boolean checkCompatibleAssignmentTypes(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
meth public static boolean checkCompatibleAssignmentTypes(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression,boolean)
meth public static boolean implementsInterfaceOrIsSubclassOf(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
meth public static boolean isAssignment(int)
meth public static boolean isBeingCompiled(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isClassClassNodeWrappingConcreteType(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isCompareToBoolean(int)
meth public static boolean isGStringOrGStringStringLUB(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isParameterizedWithGStringOrGStringString(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isParameterizedWithString(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isUsingGenericsOrIsArrayUsingGenerics(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isUsingUncheckedGenerics(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isWildcardLeftHandSide(org.codehaus.groovy.ast.ClassNode)
meth public static boolean isWithCall(java.lang.String,org.codehaus.groovy.ast.expr.Expression)
meth public static boolean missesGenericsTypes(org.codehaus.groovy.ast.ClassNode)
meth public static int allParametersAndArgumentsMatch(org.codehaus.groovy.ast.Parameter[],org.codehaus.groovy.ast.ClassNode[])
meth public static java.lang.Object evaluateExpression(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.control.CompilerConfiguration)
meth public static java.util.List<org.codehaus.groovy.ast.MethodNode> findDGMMethodsByNameAndArguments(java.lang.ClassLoader,org.codehaus.groovy.ast.ClassNode,java.lang.String,org.codehaus.groovy.ast.ClassNode[])
meth public static java.util.List<org.codehaus.groovy.ast.MethodNode> findDGMMethodsByNameAndArguments(java.lang.ClassLoader,org.codehaus.groovy.ast.ClassNode,java.lang.String,org.codehaus.groovy.ast.ClassNode[],java.util.List<org.codehaus.groovy.ast.MethodNode>)
meth public static java.util.List<org.codehaus.groovy.ast.MethodNode> findDGMMethodsByNameAndArguments(org.codehaus.groovy.ast.ClassNode,java.lang.String,org.codehaus.groovy.ast.ClassNode[])
 anno 0 java.lang.Deprecated()
meth public static java.util.List<org.codehaus.groovy.ast.MethodNode> findDGMMethodsByNameAndArguments(org.codehaus.groovy.ast.ClassNode,java.lang.String,org.codehaus.groovy.ast.ClassNode[],java.util.List<org.codehaus.groovy.ast.MethodNode>)
 anno 0 java.lang.Deprecated()
meth public static java.util.List<org.codehaus.groovy.ast.MethodNode> findSetters(org.codehaus.groovy.ast.ClassNode,java.lang.String,boolean)
meth public static java.util.Set<org.codehaus.groovy.ast.ClassNode> collectAllInterfaces(org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.ClassNode getCorrectedClassNode(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,boolean)
meth public static org.codehaus.groovy.ast.ClassNode isTraitSelf(org.codehaus.groovy.ast.expr.VariableExpression)
meth public static org.codehaus.groovy.ast.ClassNode resolveClassNodeGenerics(java.util.Map<java.lang.String,org.codehaus.groovy.ast.GenericsType>,java.util.Map<java.lang.String,org.codehaus.groovy.ast.GenericsType>,org.codehaus.groovy.ast.ClassNode)
meth public static org.codehaus.groovy.ast.Parameter[] parameterizeArguments(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.MethodNode)
supr java.lang.Object
hcls ExtensionMethodCache,ObjectArrayStaticTypesHelper

CLSS public org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor
cons public init(org.codehaus.groovy.control.SourceUnit,org.codehaus.groovy.ast.ClassNode)
fld protected final org.codehaus.groovy.classgen.ReturnAdder returnAdder
fld protected final org.codehaus.groovy.classgen.ReturnAdder$ReturnStatementListener returnListener
fld protected final static int CURRENT_SIGNATURE_PROTOCOL_VERSION = 1
fld protected final static java.lang.Object ERROR_COLLECTOR
fld protected final static java.util.List<org.codehaus.groovy.ast.MethodNode> EMPTY_METHODNODE_LIST
fld protected final static org.codehaus.groovy.ast.ClassNode CLOSUREPARAMS_CLASSNODE
fld protected final static org.codehaus.groovy.ast.ClassNode DELEGATES_TO
fld protected final static org.codehaus.groovy.ast.ClassNode DELEGATES_TO_TARGET
fld protected final static org.codehaus.groovy.ast.ClassNode DGM_CLASSNODE
fld protected final static org.codehaus.groovy.ast.ClassNode ENUMERATION_TYPE
fld protected final static org.codehaus.groovy.ast.ClassNode ITERABLE_TYPE
fld protected final static org.codehaus.groovy.ast.ClassNode LINKEDHASHMAP_CLASSNODE
fld protected final static org.codehaus.groovy.ast.ClassNode MAP_ENTRY_TYPE
fld protected final static org.codehaus.groovy.ast.ClassNode TYPECHECKED_CLASSNODE
fld protected final static org.codehaus.groovy.ast.ClassNode TYPECHECKING_INFO_NODE
fld protected final static org.codehaus.groovy.ast.ClassNode[] TYPECHECKING_ANNOTATIONS
fld protected final static org.codehaus.groovy.ast.MethodNode GET_DELEGATE
fld protected final static org.codehaus.groovy.ast.MethodNode GET_OWNER
fld protected final static org.codehaus.groovy.ast.MethodNode GET_THISOBJECT
fld protected final static org.codehaus.groovy.ast.expr.Expression CURRENT_SIGNATURE_PROTOCOL
fld protected org.codehaus.groovy.ast.FieldNode currentField
fld protected org.codehaus.groovy.ast.PropertyNode currentProperty
fld protected org.codehaus.groovy.transform.stc.DefaultTypeCheckingExtension extension
fld protected org.codehaus.groovy.transform.stc.TypeCheckingContext typeCheckingContext
fld public final static org.codehaus.groovy.ast.MethodNode CLOSURE_CALL_NO_ARG
fld public final static org.codehaus.groovy.ast.MethodNode CLOSURE_CALL_ONE_ARG
fld public final static org.codehaus.groovy.ast.MethodNode CLOSURE_CALL_VARGS
fld public final static org.codehaus.groovy.ast.stmt.Statement GENERATED_EMPTY_STATEMENT
innr protected VariableExpressionTypeMemoizer
innr public static SignatureCodecFactory
meth protected !varargs java.util.List<org.codehaus.groovy.ast.MethodNode> findMethod(org.codehaus.groovy.ast.ClassNode,java.lang.String,org.codehaus.groovy.ast.ClassNode[])
meth protected !varargs org.codehaus.groovy.ast.MethodNode findMethodOrFail(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.ClassNode,java.lang.String,org.codehaus.groovy.ast.ClassNode[])
meth protected boolean areCategoryMethodCalls(java.util.List<org.codehaus.groovy.ast.MethodNode>,java.lang.String,org.codehaus.groovy.ast.ClassNode[])
meth protected boolean checkCast(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
meth protected boolean existsProperty(org.codehaus.groovy.ast.expr.PropertyExpression,boolean)
meth protected boolean existsProperty(org.codehaus.groovy.ast.expr.PropertyExpression,boolean,org.codehaus.groovy.ast.ClassCodeVisitorSupport)
meth protected boolean isClosureCall(java.lang.String,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth protected boolean isSecondPassNeededForControlStructure(java.util.Map<org.codehaus.groovy.ast.expr.VariableExpression,org.codehaus.groovy.ast.ClassNode>,java.util.Map<org.codehaus.groovy.ast.expr.VariableExpression,java.util.List<org.codehaus.groovy.ast.ClassNode>>)
meth protected boolean isSkippedInnerClass(org.codehaus.groovy.ast.AnnotatedNode)
meth protected boolean shouldSkipClassNode(org.codehaus.groovy.ast.ClassNode)
meth protected boolean shouldSkipMethodNode(org.codehaus.groovy.ast.MethodNode)
meth protected boolean typeCheckMethodsWithGenericsOrFail(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.MethodNode,org.codehaus.groovy.ast.expr.Expression)
meth protected java.lang.Object extractTemporaryTypeInfoKey(org.codehaus.groovy.ast.expr.Expression)
meth protected java.lang.Object getDelegationMetadata(org.codehaus.groovy.ast.expr.ClosureExpression)
meth protected java.lang.Object hasSetter(org.codehaus.groovy.ast.expr.PropertyExpression)
 anno 0 java.lang.Deprecated()
meth protected java.util.List<org.codehaus.groovy.ast.ClassNode> getTemporaryTypesForExpression(org.codehaus.groovy.ast.expr.Expression)
meth protected java.util.List<org.codehaus.groovy.ast.MethodNode> findMethodsWithGenerated(org.codehaus.groovy.ast.ClassNode,java.lang.String)
meth protected java.util.List<org.codehaus.groovy.transform.stc.Receiver<java.lang.String>> makeOwnerList(org.codehaus.groovy.ast.expr.Expression)
meth protected java.util.Map<org.codehaus.groovy.ast.expr.VariableExpression,java.util.List<org.codehaus.groovy.ast.ClassNode>> pushAssignmentTracking()
meth protected java.util.Map<org.codehaus.groovy.ast.expr.VariableExpression,org.codehaus.groovy.ast.ClassNode> popAssignmentTracking(java.util.Map<org.codehaus.groovy.ast.expr.VariableExpression,java.util.List<org.codehaus.groovy.ast.ClassNode>>)
meth protected org.codehaus.groovy.ast.ClassNode checkReturnType(org.codehaus.groovy.ast.stmt.ReturnStatement)
meth protected org.codehaus.groovy.ast.ClassNode findCurrentInstanceOfClass(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.ClassNode)
meth protected org.codehaus.groovy.ast.ClassNode getInferredReturnType(org.codehaus.groovy.ast.ASTNode)
meth protected org.codehaus.groovy.ast.ClassNode getInferredReturnTypeFromWithClosureArgument(org.codehaus.groovy.ast.expr.Expression)
meth protected org.codehaus.groovy.ast.ClassNode getOriginalDeclarationType(org.codehaus.groovy.ast.expr.Expression)
meth protected org.codehaus.groovy.ast.ClassNode getResultType(org.codehaus.groovy.ast.ClassNode,int,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.BinaryExpression)
meth protected org.codehaus.groovy.ast.ClassNode getType(org.codehaus.groovy.ast.ASTNode)
meth protected org.codehaus.groovy.ast.ClassNode inferComponentType(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
meth protected org.codehaus.groovy.ast.ClassNode inferListExpressionType(org.codehaus.groovy.ast.expr.ListExpression)
meth protected org.codehaus.groovy.ast.ClassNode inferMapExpressionType(org.codehaus.groovy.ast.expr.MapExpression)
meth protected org.codehaus.groovy.ast.ClassNode inferReturnTypeGenerics(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.MethodNode,org.codehaus.groovy.ast.expr.Expression)
meth protected org.codehaus.groovy.ast.ClassNode inferReturnTypeGenerics(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.MethodNode,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.GenericsType[])
meth protected org.codehaus.groovy.ast.ClassNode storeInferredReturnType(org.codehaus.groovy.ast.ASTNode,org.codehaus.groovy.ast.ClassNode)
meth protected org.codehaus.groovy.ast.ClassNode[] getArgumentTypes(org.codehaus.groovy.ast.expr.ArgumentListExpression)
meth protected org.codehaus.groovy.ast.ClassNode[] getTypeCheckingAnnotations()
meth protected org.codehaus.groovy.ast.MethodNode checkGroovyStyleConstructor(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.ASTNode)
meth protected org.codehaus.groovy.ast.MethodNode typeCheckMapConstructor(org.codehaus.groovy.ast.expr.ConstructorCallExpression,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth protected static boolean hasRHSIncompleteGenericTypeInfo(org.codehaus.groovy.ast.ClassNode)
meth protected static boolean isClassInnerClassOrEqualTo(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
meth protected static boolean isNullConstant(org.codehaus.groovy.ast.expr.Expression)
meth protected static java.lang.String formatArgumentList(org.codehaus.groovy.ast.ClassNode[])
meth protected static java.lang.String prettyPrintMethodList(java.util.List<org.codehaus.groovy.ast.MethodNode>)
meth protected static org.codehaus.groovy.ast.ClassNode getGroupOperationResultType(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
meth protected static org.codehaus.groovy.ast.ClassNode wrapTypeIfNecessary(org.codehaus.groovy.ast.ClassNode)
meth protected static org.codehaus.groovy.ast.ClassNode[] extractTypesFromParameters(org.codehaus.groovy.ast.Parameter[])
meth protected void addAmbiguousErrorMessage(java.util.List<org.codehaus.groovy.ast.MethodNode>,java.lang.String,org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.expr.Expression)
meth protected void addAssignmentError(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
meth protected void addCategoryMethodCallError(org.codehaus.groovy.ast.expr.Expression)
meth protected void addClosureReturnType(org.codehaus.groovy.ast.ClassNode)
meth protected void addNoMatchingMethodError(org.codehaus.groovy.ast.ClassNode,java.lang.String,org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.expr.Expression)
meth protected void addReceivers(java.util.List<org.codehaus.groovy.transform.stc.Receiver<java.lang.String>>,java.util.Collection<org.codehaus.groovy.transform.stc.Receiver<java.lang.String>>,boolean)
meth protected void addStaticTypeError(java.lang.String,org.codehaus.groovy.ast.ASTNode)
meth protected void addTypeCheckingInfoAnnotation(org.codehaus.groovy.ast.MethodNode)
meth protected void addUnsupportedPreOrPostfixExpressionError(org.codehaus.groovy.ast.expr.Expression)
meth protected void checkClosureParameters(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.ClassNode)
 anno 0 java.lang.Deprecated()
meth protected void checkForbiddenSpreadArgument(org.codehaus.groovy.ast.expr.ArgumentListExpression)
meth protected void checkGroovyConstructorMap(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.MapExpression)
meth protected void checkGroovyStyleConstructor(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode[])
 anno 0 java.lang.Deprecated()
meth protected void collectAllInterfaceMethodsByName(org.codehaus.groovy.ast.ClassNode,java.lang.String,java.util.List<org.codehaus.groovy.ast.MethodNode>)
meth protected void inferClosureParameterTypes(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.ClosureExpression,org.codehaus.groovy.ast.Parameter,org.codehaus.groovy.ast.MethodNode)
meth protected void inferDiamondType(org.codehaus.groovy.ast.expr.ConstructorCallExpression,org.codehaus.groovy.ast.ClassNode)
meth protected void pushInstanceOfTypeInfo(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.expr.Expression)
meth protected void restoreVariableExpressionMetadata(java.util.Map<org.codehaus.groovy.ast.expr.VariableExpression,org.codehaus.groovy.util.ListHashMap>)
meth protected void saveVariableExpressionMetadata(java.util.Set<org.codehaus.groovy.ast.expr.VariableExpression>,java.util.Map<org.codehaus.groovy.ast.expr.VariableExpression,org.codehaus.groovy.util.ListHashMap>)
meth protected void silentlyVisitMethodNode(org.codehaus.groovy.ast.MethodNode)
meth protected void startMethodInference(org.codehaus.groovy.ast.MethodNode,org.codehaus.groovy.control.ErrorCollector)
meth protected void storeInferredTypeForPropertyExpression(org.codehaus.groovy.ast.expr.PropertyExpression,org.codehaus.groovy.ast.ClassNode)
meth protected void storeTargetMethod(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.MethodNode)
meth protected void storeType(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.ClassNode)
meth protected void typeCheckAssignment(org.codehaus.groovy.ast.expr.BinaryExpression,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.ClassNode)
meth protected void typeCheckClosureCall(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.Parameter[])
meth protected void visitConstructorOrMethod(org.codehaus.groovy.ast.MethodNode,boolean)
meth protected void visitMethodCallArguments(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.ArgumentListExpression,boolean,org.codehaus.groovy.ast.MethodNode)
meth public boolean isSkipMode(org.codehaus.groovy.ast.AnnotatedNode)
meth public org.codehaus.groovy.ast.expr.BinaryExpression findInstanceOfNotReturnExpression(org.codehaus.groovy.ast.stmt.IfStatement)
meth public org.codehaus.groovy.transform.stc.TypeCheckingContext getTypeCheckingContext()
meth public static java.lang.String extractPropertyNameFromMethodName(java.lang.String,java.lang.String)
meth public static org.codehaus.groovy.ast.ClassNode inferLoopElementType(org.codehaus.groovy.ast.ClassNode)
meth public void addError(java.lang.String,org.codehaus.groovy.ast.ASTNode)
meth public void addTypeCheckingExtension(org.codehaus.groovy.transform.stc.TypeCheckingExtension)
meth public void initialize()
meth public void performSecondPass()
meth public void setCompilationUnit(org.codehaus.groovy.control.CompilationUnit)
meth public void setMethodsToBeVisited(java.util.Set<org.codehaus.groovy.ast.MethodNode>)
meth public void visitAttributeExpression(org.codehaus.groovy.ast.expr.AttributeExpression)
meth public void visitBinaryExpression(org.codehaus.groovy.ast.expr.BinaryExpression)
meth public void visitBitwiseNegationExpression(org.codehaus.groovy.ast.expr.BitwiseNegationExpression)
meth public void visitBlockStatement(org.codehaus.groovy.ast.stmt.BlockStatement)
meth public void visitCaseStatement(org.codehaus.groovy.ast.stmt.CaseStatement)
meth public void visitCastExpression(org.codehaus.groovy.ast.expr.CastExpression)
meth public void visitClass(org.codehaus.groovy.ast.ClassNode)
meth public void visitClassExpression(org.codehaus.groovy.ast.expr.ClassExpression)
meth public void visitClosingBlock(org.codehaus.groovy.ast.stmt.BlockStatement)
meth public void visitClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public void visitConstructor(org.codehaus.groovy.ast.ConstructorNode)
meth public void visitConstructorCallExpression(org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth public void visitField(org.codehaus.groovy.ast.FieldNode)
meth public void visitForLoop(org.codehaus.groovy.ast.stmt.ForStatement)
meth public void visitIfElse(org.codehaus.groovy.ast.stmt.IfStatement)
meth public void visitInstanceofNot(org.codehaus.groovy.ast.expr.BinaryExpression)
meth public void visitMethod(org.codehaus.groovy.ast.MethodNode)
meth public void visitMethodCallExpression(org.codehaus.groovy.ast.expr.MethodCallExpression)
meth public void visitPostfixExpression(org.codehaus.groovy.ast.expr.PostfixExpression)
meth public void visitPrefixExpression(org.codehaus.groovy.ast.expr.PrefixExpression)
meth public void visitProperty(org.codehaus.groovy.ast.PropertyNode)
meth public void visitPropertyExpression(org.codehaus.groovy.ast.expr.PropertyExpression)
meth public void visitRangeExpression(org.codehaus.groovy.ast.expr.RangeExpression)
meth public void visitReturnStatement(org.codehaus.groovy.ast.stmt.ReturnStatement)
meth public void visitStaticMethodCallExpression(org.codehaus.groovy.ast.expr.StaticMethodCallExpression)
meth public void visitSwitch(org.codehaus.groovy.ast.stmt.SwitchStatement)
meth public void visitTernaryExpression(org.codehaus.groovy.ast.expr.TernaryExpression)
meth public void visitTryCatchFinally(org.codehaus.groovy.ast.stmt.TryCatchStatement)
meth public void visitUnaryMinusExpression(org.codehaus.groovy.ast.expr.UnaryMinusExpression)
meth public void visitUnaryPlusExpression(org.codehaus.groovy.ast.expr.UnaryPlusExpression)
meth public void visitVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
meth public void visitWhileLoop(org.codehaus.groovy.ast.stmt.WhileStatement)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds DEBUG_GENERATED_CODE,EMPTY_STRING_ARRAY,UNIQUE_LONG
hcls ExtensionMethodDeclaringClass,ParameterVariableExpression,SetterInfo

CLSS public static org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor$SignatureCodecFactory
 outer org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor
cons public init()
meth public static org.codehaus.groovy.transform.stc.SignatureCodec getCodec(int,java.lang.ClassLoader)
supr java.lang.Object

CLSS protected org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor$VariableExpressionTypeMemoizer
 outer org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor
cons public init(java.util.Map<org.codehaus.groovy.ast.expr.VariableExpression,org.codehaus.groovy.ast.ClassNode>)
meth protected org.codehaus.groovy.control.SourceUnit getSourceUnit()
meth public void visitVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
supr org.codehaus.groovy.ast.ClassCodeVisitorSupport
hfds varOrigType

CLSS public final !enum org.codehaus.groovy.transform.stc.StaticTypesMarker
fld public final static org.codehaus.groovy.transform.stc.StaticTypesMarker CLOSURE_ARGUMENTS
fld public final static org.codehaus.groovy.transform.stc.StaticTypesMarker DECLARATION_INFERRED_TYPE
fld public final static org.codehaus.groovy.transform.stc.StaticTypesMarker DELEGATION_METADATA
fld public final static org.codehaus.groovy.transform.stc.StaticTypesMarker DIRECT_METHOD_CALL_TARGET
fld public final static org.codehaus.groovy.transform.stc.StaticTypesMarker DYNAMIC_RESOLUTION
fld public final static org.codehaus.groovy.transform.stc.StaticTypesMarker IMPLICIT_RECEIVER
fld public final static org.codehaus.groovy.transform.stc.StaticTypesMarker INFERRED_RETURN_TYPE
fld public final static org.codehaus.groovy.transform.stc.StaticTypesMarker INFERRED_TYPE
fld public final static org.codehaus.groovy.transform.stc.StaticTypesMarker INITIAL_EXPRESSION
fld public final static org.codehaus.groovy.transform.stc.StaticTypesMarker PV_FIELDS_ACCESS
fld public final static org.codehaus.groovy.transform.stc.StaticTypesMarker PV_FIELDS_MUTATION
fld public final static org.codehaus.groovy.transform.stc.StaticTypesMarker PV_METHODS_ACCESS
fld public final static org.codehaus.groovy.transform.stc.StaticTypesMarker READONLY_PROPERTY
fld public final static org.codehaus.groovy.transform.stc.StaticTypesMarker SUPER_MOP_METHOD_REQUIRED
meth public static org.codehaus.groovy.transform.stc.StaticTypesMarker valueOf(java.lang.String)
meth public static org.codehaus.groovy.transform.stc.StaticTypesMarker[] values()
supr java.lang.Enum<org.codehaus.groovy.transform.stc.StaticTypesMarker>

CLSS public org.codehaus.groovy.transform.stc.TraitTypeCheckingExtension
cons public init(org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor)
meth public java.util.List<org.codehaus.groovy.ast.MethodNode> handleMissingMethod(org.codehaus.groovy.ast.ClassNode,java.lang.String,org.codehaus.groovy.ast.expr.ArgumentListExpression,org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.expr.MethodCall)
meth public void setup()
supr org.codehaus.groovy.transform.stc.AbstractTypeCheckingExtension
hfds NOTFOUND

CLSS public org.codehaus.groovy.transform.stc.TypeCheckingContext
cons public init(org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor)
fld protected boolean isInStaticContext
fld protected final java.util.IdentityHashMap<org.codehaus.groovy.ast.stmt.BlockStatement,java.util.Map<org.codehaus.groovy.ast.expr.VariableExpression,java.util.List<org.codehaus.groovy.ast.ClassNode>>> blockStatements2Types
fld protected final java.util.LinkedHashSet<org.codehaus.groovy.transform.stc.SecondPassExpression> secondPassExpressions
fld protected final java.util.LinkedList<org.codehaus.groovy.ast.ClassNode> enclosingClassNodes
fld protected final java.util.LinkedList<org.codehaus.groovy.ast.MethodNode> enclosingMethods
fld protected final java.util.LinkedList<org.codehaus.groovy.ast.expr.BinaryExpression> enclosingBinaryExpressions
fld protected final java.util.LinkedList<org.codehaus.groovy.ast.expr.ConstructorCallExpression> enclosingConstructorCalls
fld protected final java.util.LinkedList<org.codehaus.groovy.ast.expr.Expression> enclosingMethodCalls
fld protected final java.util.LinkedList<org.codehaus.groovy.ast.expr.PropertyExpression> enclosingPropertyExpressions
fld protected final java.util.LinkedList<org.codehaus.groovy.ast.stmt.BlockStatement> enclosingBlocks
fld protected final java.util.LinkedList<org.codehaus.groovy.ast.stmt.ReturnStatement> enclosingReturnStatements
fld protected final java.util.LinkedList<org.codehaus.groovy.control.ErrorCollector> errorCollectors
fld protected final java.util.LinkedList<org.codehaus.groovy.transform.stc.TypeCheckingContext$EnclosingClosure> enclosingClosures
fld protected final java.util.Map<org.codehaus.groovy.ast.expr.VariableExpression,java.util.List<org.codehaus.groovy.ast.ClassNode>> closureSharedVariablesAssignmentTypes
fld protected final java.util.Set<java.lang.Long> reportedErrors
fld protected final org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor visitor
fld protected java.lang.Object delegationMetadata
fld protected java.util.Map<org.codehaus.groovy.ast.Parameter,org.codehaus.groovy.ast.ClassNode> controlStructureVariables
fld protected java.util.Map<org.codehaus.groovy.ast.expr.VariableExpression,java.util.List<org.codehaus.groovy.ast.ClassNode>> ifElseForWhileAssignmentTracker
fld protected java.util.Set<org.codehaus.groovy.ast.MethodNode> alreadyVisitedMethods
fld protected java.util.Set<org.codehaus.groovy.ast.MethodNode> methodsToBeVisited
fld protected java.util.Stack<java.util.Map<java.lang.Object,java.util.List<org.codehaus.groovy.ast.ClassNode>>> temporaryIfBranchTypeInformation
fld protected org.codehaus.groovy.ast.ClassNode lastImplicitItType
fld protected org.codehaus.groovy.control.CompilationUnit compilationUnit
fld protected org.codehaus.groovy.control.SourceUnit source
innr public static EnclosingClosure
meth public java.util.List<org.codehaus.groovy.ast.ClassNode> getEnclosingClassNodes()
meth public java.util.List<org.codehaus.groovy.ast.MethodNode> getEnclosingMethods()
meth public java.util.List<org.codehaus.groovy.ast.expr.BinaryExpression> getEnclosingBinaryExpressionStack()
meth public java.util.List<org.codehaus.groovy.ast.expr.ConstructorCallExpression> getEnclosingConstructorCalls()
meth public java.util.List<org.codehaus.groovy.ast.expr.Expression> getEnclosingMethodCalls()
meth public java.util.List<org.codehaus.groovy.ast.expr.PropertyExpression> getEnclosingPropertyExpressions()
meth public java.util.List<org.codehaus.groovy.control.ErrorCollector> getErrorCollectors()
meth public java.util.List<org.codehaus.groovy.transform.stc.TypeCheckingContext$EnclosingClosure> getEnclosingClosureStack()
meth public org.codehaus.groovy.ast.ClassNode getEnclosingClassNode()
meth public org.codehaus.groovy.ast.ClassNode popEnclosingClassNode()
meth public org.codehaus.groovy.ast.MethodNode getEnclosingMethod()
meth public org.codehaus.groovy.ast.MethodNode popEnclosingMethod()
meth public org.codehaus.groovy.ast.expr.BinaryExpression getEnclosingBinaryExpression()
meth public org.codehaus.groovy.ast.expr.BinaryExpression popEnclosingBinaryExpression()
meth public org.codehaus.groovy.ast.expr.ConstructorCallExpression getEnclosingConstructorCall()
meth public org.codehaus.groovy.ast.expr.ConstructorCallExpression popEnclosingConstructorCall()
meth public org.codehaus.groovy.ast.expr.Expression getEnclosingMethodCall()
meth public org.codehaus.groovy.ast.expr.Expression getEnclosingPropertyExpression()
meth public org.codehaus.groovy.ast.expr.Expression popEnclosingMethodCall()
meth public org.codehaus.groovy.ast.expr.Expression popEnclosingPropertyExpression()
meth public org.codehaus.groovy.ast.stmt.ReturnStatement getEnclosingReturnStatement()
meth public org.codehaus.groovy.ast.stmt.ReturnStatement popEnclosingReturnStatement()
meth public org.codehaus.groovy.control.CompilationUnit getCompilationUnit()
meth public org.codehaus.groovy.control.ErrorCollector getErrorCollector()
meth public org.codehaus.groovy.control.ErrorCollector popErrorCollector()
meth public org.codehaus.groovy.control.ErrorCollector pushErrorCollector()
meth public org.codehaus.groovy.control.SourceUnit getSource()
meth public org.codehaus.groovy.transform.stc.TypeCheckingContext$EnclosingClosure getEnclosingClosure()
meth public org.codehaus.groovy.transform.stc.TypeCheckingContext$EnclosingClosure popEnclosingClosure()
meth public void popTemporaryTypeInfo()
meth public void pushEnclosingBinaryExpression(org.codehaus.groovy.ast.expr.BinaryExpression)
meth public void pushEnclosingClassNode(org.codehaus.groovy.ast.ClassNode)
meth public void pushEnclosingClosureExpression(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public void pushEnclosingConstructorCall(org.codehaus.groovy.ast.expr.ConstructorCallExpression)
meth public void pushEnclosingMethod(org.codehaus.groovy.ast.MethodNode)
meth public void pushEnclosingMethodCall(org.codehaus.groovy.ast.expr.Expression)
meth public void pushEnclosingPropertyExpression(org.codehaus.groovy.ast.expr.PropertyExpression)
meth public void pushEnclosingReturnStatement(org.codehaus.groovy.ast.stmt.ReturnStatement)
meth public void pushErrorCollector(org.codehaus.groovy.control.ErrorCollector)
meth public void pushTemporaryTypeInfo()
meth public void setCompilationUnit(org.codehaus.groovy.control.CompilationUnit)
supr java.lang.Object

CLSS public static org.codehaus.groovy.transform.stc.TypeCheckingContext$EnclosingClosure
 outer org.codehaus.groovy.transform.stc.TypeCheckingContext
cons public init(org.codehaus.groovy.ast.expr.ClosureExpression)
meth public java.lang.String toString()
meth public java.util.List<org.codehaus.groovy.ast.ClassNode> getReturnTypes()
meth public org.codehaus.groovy.ast.expr.ClosureExpression getClosureExpression()
meth public void addReturnType(org.codehaus.groovy.ast.ClassNode)
supr java.lang.Object
hfds closureExpression,returnTypes

CLSS public org.codehaus.groovy.transform.stc.TypeCheckingExtension
cons public init(org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor)
fld protected final org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor typeCheckingVisitor
meth public !varargs org.codehaus.groovy.ast.ClassNode parameterizedType(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode[])
meth public boolean beforeMethodCall(org.codehaus.groovy.ast.expr.MethodCall)
meth public boolean beforeVisitClass(org.codehaus.groovy.ast.ClassNode)
meth public boolean beforeVisitMethod(org.codehaus.groovy.ast.MethodNode)
meth public boolean existsProperty(org.codehaus.groovy.ast.expr.PropertyExpression,boolean)
meth public boolean existsProperty(org.codehaus.groovy.ast.expr.PropertyExpression,boolean,org.codehaus.groovy.ast.ClassCodeVisitorSupport)
meth public boolean handleIncompatibleAssignment(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.expr.Expression)
meth public boolean handleIncompatibleReturnType(org.codehaus.groovy.ast.stmt.ReturnStatement,org.codehaus.groovy.ast.ClassNode)
meth public boolean handleUnresolvedAttribute(org.codehaus.groovy.ast.expr.AttributeExpression)
meth public boolean handleUnresolvedProperty(org.codehaus.groovy.ast.expr.PropertyExpression)
meth public boolean handleUnresolvedVariableExpression(org.codehaus.groovy.ast.expr.VariableExpression)
meth public boolean isStaticMethodCallOnClass(org.codehaus.groovy.ast.expr.MethodCall,org.codehaus.groovy.ast.ClassNode)
meth public java.util.List<org.codehaus.groovy.ast.MethodNode> handleAmbiguousMethods(java.util.List<org.codehaus.groovy.ast.MethodNode>,org.codehaus.groovy.ast.expr.Expression)
meth public java.util.List<org.codehaus.groovy.ast.MethodNode> handleMissingMethod(org.codehaus.groovy.ast.ClassNode,java.lang.String,org.codehaus.groovy.ast.expr.ArgumentListExpression,org.codehaus.groovy.ast.ClassNode[],org.codehaus.groovy.ast.expr.MethodCall)
meth public org.codehaus.groovy.ast.ClassNode buildListType(org.codehaus.groovy.ast.ClassNode)
meth public org.codehaus.groovy.ast.ClassNode buildMapType(org.codehaus.groovy.ast.ClassNode,org.codehaus.groovy.ast.ClassNode)
meth public org.codehaus.groovy.ast.ClassNode classNodeFor(java.lang.Class)
meth public org.codehaus.groovy.ast.ClassNode classNodeFor(java.lang.String)
meth public org.codehaus.groovy.ast.ClassNode extractStaticReceiver(org.codehaus.groovy.ast.expr.MethodCall)
meth public org.codehaus.groovy.ast.ClassNode getType(org.codehaus.groovy.ast.ASTNode)
meth public org.codehaus.groovy.ast.ClassNode lookupClassNodeFor(java.lang.String)
meth public org.codehaus.groovy.ast.ClassNode[] getArgumentTypes(org.codehaus.groovy.ast.expr.ArgumentListExpression)
meth public org.codehaus.groovy.ast.MethodNode getTargetMethod(org.codehaus.groovy.ast.expr.Expression)
meth public void addStaticTypeError(java.lang.String,org.codehaus.groovy.ast.ASTNode)
meth public void afterMethodCall(org.codehaus.groovy.ast.expr.MethodCall)
meth public void afterVisitClass(org.codehaus.groovy.ast.ClassNode)
meth public void afterVisitMethod(org.codehaus.groovy.ast.MethodNode)
meth public void finish()
meth public void onMethodSelection(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.MethodNode)
meth public void setup()
meth public void storeType(org.codehaus.groovy.ast.expr.Expression,org.codehaus.groovy.ast.ClassNode)
supr java.lang.Object

CLSS public abstract org.codehaus.groovy.util.AbstractConcurrentMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(java.lang.Object)
innr public abstract interface static Entry
innr public abstract static Segment
meth public org.codehaus.groovy.util.AbstractConcurrentMap$Entry<{org.codehaus.groovy.util.AbstractConcurrentMap%0},{org.codehaus.groovy.util.AbstractConcurrentMap%1}> getOrPut({org.codehaus.groovy.util.AbstractConcurrentMap%0},{org.codehaus.groovy.util.AbstractConcurrentMap%1})
meth public org.codehaus.groovy.util.AbstractConcurrentMap$Segment segmentFor(int)
meth public void put({org.codehaus.groovy.util.AbstractConcurrentMap%0},{org.codehaus.groovy.util.AbstractConcurrentMap%1})
meth public void remove({org.codehaus.groovy.util.AbstractConcurrentMap%0})
meth public {org.codehaus.groovy.util.AbstractConcurrentMap%1} get({org.codehaus.groovy.util.AbstractConcurrentMap%0})
supr org.codehaus.groovy.util.AbstractConcurrentMapBase

CLSS public abstract org.codehaus.groovy.util.AbstractConcurrentMapBase
cons public init(java.lang.Object)
fld protected final org.codehaus.groovy.util.AbstractConcurrentMapBase$Segment[] segments
fld protected final static int MAXIMUM_CAPACITY = 1073741824
innr public abstract interface static Entry
innr public static Segment
meth protected abstract org.codehaus.groovy.util.AbstractConcurrentMapBase$Segment createSegment(java.lang.Object,int)
meth protected static <%0 extends java.lang.Object> int hash({%%0})
meth public int fullSize()
meth public int size()
meth public java.util.Collection values()
meth public org.codehaus.groovy.util.AbstractConcurrentMapBase$Segment segmentFor(int)
supr java.lang.Object
hfds MAX_SEGMENTS,RETRIES_BEFORE_LOCK,segmentMask,segmentShift

CLSS public org.codehaus.groovy.util.ComplexKeyHashMap
cons public init()
cons public init(boolean)
cons public init(int)
fld protected final static int DEFAULT_CAPACITY = 32
fld protected final static int MAXIMUM_CAPACITY = 268435456
fld protected final static int MINIMUM_CAPACITY = 4
fld protected int size
fld protected int threshold
fld protected org.codehaus.groovy.util.ComplexKeyHashMap$Entry[] table
innr public abstract interface static EntryIterator
innr public static Entry
meth public boolean isEmpty()
meth public int size()
meth public org.codehaus.groovy.util.ComplexKeyHashMap$EntryIterator getEntrySetIterator()
meth public org.codehaus.groovy.util.ComplexKeyHashMap$Entry[] getTable()
meth public static int hash(int)
meth public void clear()
meth public void init(int)
meth public void resize(int)
supr java.lang.Object

CLSS public abstract interface org.codehaus.groovy.util.Finalizable
meth public abstract void finalizeReference()

CLSS public org.codehaus.groovy.util.ManagedConcurrentMap<%0 extends java.lang.Object, %1 extends java.lang.Object>
cons public init(org.codehaus.groovy.util.ReferenceBundle)
fld protected org.codehaus.groovy.util.ReferenceBundle bundle
innr public static Entry
innr public static EntryWithValue
innr public static Segment
meth protected org.codehaus.groovy.util.ManagedConcurrentMap$Segment<{org.codehaus.groovy.util.ManagedConcurrentMap%0},{org.codehaus.groovy.util.ManagedConcurrentMap%1}> createSegment(java.lang.Object,int)
supr org.codehaus.groovy.util.AbstractConcurrentMap<{org.codehaus.groovy.util.ManagedConcurrentMap%0},{org.codehaus.groovy.util.ManagedConcurrentMap%1}>

CLSS public org.codehaus.groovy.util.SingleKeyHashMap
cons public init()
cons public init(boolean)
innr public abstract interface static Copier
innr public static Entry
meth public boolean containsKey(java.lang.String)
meth public final java.lang.Object get(java.lang.Object)
meth public final org.codehaus.groovy.util.ComplexKeyHashMap$Entry remove(java.lang.Object)
meth public org.codehaus.groovy.util.SingleKeyHashMap$Entry getOrPut(java.lang.Object)
meth public org.codehaus.groovy.util.SingleKeyHashMap$Entry getOrPutEntry(org.codehaus.groovy.util.SingleKeyHashMap$Entry)
meth public org.codehaus.groovy.util.SingleKeyHashMap$Entry putCopyOfUnexisting(org.codehaus.groovy.util.SingleKeyHashMap$Entry)
meth public static org.codehaus.groovy.util.SingleKeyHashMap copy(org.codehaus.groovy.util.SingleKeyHashMap,org.codehaus.groovy.util.SingleKeyHashMap,org.codehaus.groovy.util.SingleKeyHashMap$Copier)
meth public void put(java.lang.Object,java.lang.Object)
supr org.codehaus.groovy.util.ComplexKeyHashMap

