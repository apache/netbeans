#Signature file v4.1
#Version 8.57

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract javax.annotation.processing.AbstractProcessor
cons protected init()
fld protected javax.annotation.processing.ProcessingEnvironment processingEnv
intf javax.annotation.processing.Processor
meth protected boolean isInitialized()
meth public abstract boolean process(java.util.Set<? extends javax.lang.model.element.TypeElement>,javax.annotation.processing.RoundEnvironment)
meth public java.lang.Iterable<? extends javax.annotation.processing.Completion> getCompletions(javax.lang.model.element.Element,javax.lang.model.element.AnnotationMirror,javax.lang.model.element.ExecutableElement,java.lang.String)
meth public java.util.Set<java.lang.String> getSupportedAnnotationTypes()
meth public java.util.Set<java.lang.String> getSupportedOptions()
meth public javax.lang.model.SourceVersion getSupportedSourceVersion()
meth public void init(javax.annotation.processing.ProcessingEnvironment)
supr java.lang.Object

CLSS public abstract interface javax.annotation.processing.Processor
meth public abstract boolean process(java.util.Set<? extends javax.lang.model.element.TypeElement>,javax.annotation.processing.RoundEnvironment)
meth public abstract java.lang.Iterable<? extends javax.annotation.processing.Completion> getCompletions(javax.lang.model.element.Element,javax.lang.model.element.AnnotationMirror,javax.lang.model.element.ExecutableElement,java.lang.String)
meth public abstract java.util.Set<java.lang.String> getSupportedAnnotationTypes()
meth public abstract java.util.Set<java.lang.String> getSupportedOptions()
meth public abstract javax.lang.model.SourceVersion getSupportedSourceVersion()
meth public abstract void init(javax.annotation.processing.ProcessingEnvironment)

CLSS public abstract org.openide.util.Lookup
cons public init()
fld public final static org.openide.util.Lookup EMPTY
innr public abstract interface static Provider
innr public abstract static Item
innr public abstract static Result
innr public final static Template
meth public <%0 extends java.lang.Object> java.util.Collection<? extends {%%0}> lookupAll(java.lang.Class<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookupResult(java.lang.Class<{%%0}>)
meth public abstract <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public abstract <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public static org.openide.util.Lookup getDefault()
supr java.lang.Object
hfds LOG,defaultLookup,defaultLookupProvider
hcls DefLookup,Empty

CLSS public abstract static org.openide.util.Lookup$Item<%0 extends java.lang.Object>
 outer org.openide.util.Lookup
cons public init()
meth public abstract java.lang.Class<? extends {org.openide.util.Lookup$Item%0}> getType()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getId()
meth public abstract {org.openide.util.Lookup$Item%0} getInstance()
meth public java.lang.String toString()
supr java.lang.Object

CLSS public abstract interface static org.openide.util.Lookup$Provider
 outer org.openide.util.Lookup
meth public abstract org.openide.util.Lookup getLookup()

CLSS public abstract static org.openide.util.Lookup$Result<%0 extends java.lang.Object>
 outer org.openide.util.Lookup
cons public init()
meth public abstract java.util.Collection<? extends {org.openide.util.Lookup$Result%0}> allInstances()
meth public abstract void addLookupListener(org.openide.util.LookupListener)
meth public abstract void removeLookupListener(org.openide.util.LookupListener)
meth public java.util.Collection<? extends org.openide.util.Lookup$Item<{org.openide.util.Lookup$Result%0}>> allItems()
meth public java.util.Set<java.lang.Class<? extends {org.openide.util.Lookup$Result%0}>> allClasses()
supr java.lang.Object

CLSS public final static org.openide.util.Lookup$Template<%0 extends java.lang.Object>
 outer org.openide.util.Lookup
cons public init()
 anno 0 java.lang.Deprecated()
cons public init(java.lang.Class<{org.openide.util.Lookup$Template%0}>)
cons public init(java.lang.Class<{org.openide.util.Lookup$Template%0}>,java.lang.String,{org.openide.util.Lookup$Template%0})
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.Class<{org.openide.util.Lookup$Template%0}> getType()
meth public java.lang.String getId()
meth public java.lang.String toString()
meth public {org.openide.util.Lookup$Template%0} getInstance()
supr java.lang.Object
hfds hashCode,id,instance,type

CLSS public final org.openide.util.LookupEvent
cons public init(org.openide.util.Lookup$Result)
supr java.util.EventObject

CLSS public abstract interface org.openide.util.LookupListener
intf java.util.EventListener
meth public abstract void resultChanged(org.openide.util.LookupEvent)

CLSS public org.openide.util.lookup.AbstractLookup
cons protected init()
cons public init(org.openide.util.lookup.AbstractLookup$Content)
innr public abstract static Pair
innr public static Content
intf java.io.Serializable
meth protected final void addPair(org.openide.util.lookup.AbstractLookup$Pair<?>)
meth protected final void addPair(org.openide.util.lookup.AbstractLookup$Pair<?>,java.util.concurrent.Executor)
meth protected final void removePair(org.openide.util.lookup.AbstractLookup$Pair<?>)
meth protected final void removePair(org.openide.util.lookup.AbstractLookup$Pair<?>,java.util.concurrent.Executor)
meth protected final void setPairs(java.util.Collection<? extends org.openide.util.lookup.AbstractLookup$Pair>)
meth protected final void setPairs(java.util.Collection<? extends org.openide.util.lookup.AbstractLookup$Pair>,java.util.concurrent.Executor)
meth protected void beforeLookup(org.openide.util.Lookup$Template<?>)
meth protected void initialize()
meth public final <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public final <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public final <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public java.lang.String toString()
supr org.openide.util.Lookup
hfds LOG,count,serialVersionUID,tree,treeLock
hcls CycleError,ISE,Info,NotifyListeners,R,ReferenceIterator,ReferenceToResult,Storage

CLSS public static org.openide.util.lookup.AbstractLookup$Content
 outer org.openide.util.lookup.AbstractLookup
cons public init()
cons public init(java.util.concurrent.Executor)
intf java.io.Serializable
meth public final void addPair(org.openide.util.lookup.AbstractLookup$Pair<?>)
meth public final void removePair(org.openide.util.lookup.AbstractLookup$Pair<?>)
meth public final void setPairs(java.util.Collection<? extends org.openide.util.lookup.AbstractLookup$Pair>)
supr java.lang.Object
hfds al,notifyIn,serialVersionUID

CLSS public abstract static org.openide.util.lookup.AbstractLookup$Pair<%0 extends java.lang.Object>
 outer org.openide.util.lookup.AbstractLookup
cons protected init()
intf java.io.Serializable
meth protected abstract boolean creatorOf(java.lang.Object)
meth protected abstract boolean instanceOf(java.lang.Class<?>)
supr org.openide.util.Lookup$Item<{org.openide.util.lookup.AbstractLookup$Pair%0}>
hfds index,serialVersionUID

CLSS public final org.openide.util.lookup.InstanceContent
cons public init()
cons public init(java.util.concurrent.Executor)
innr public abstract interface static Convertor
meth public final <%0 extends java.lang.Object, %1 extends java.lang.Object> void add({%%0},org.openide.util.lookup.InstanceContent$Convertor<{%%0},{%%1}>)
meth public final <%0 extends java.lang.Object, %1 extends java.lang.Object> void remove({%%0},org.openide.util.lookup.InstanceContent$Convertor<{%%0},{%%1}>)
meth public final <%0 extends java.lang.Object, %1 extends java.lang.Object> void set(java.util.Collection<{%%0}>,org.openide.util.lookup.InstanceContent$Convertor<{%%0},{%%1}>)
meth public final void add(java.lang.Object)
meth public final void remove(java.lang.Object)
supr org.openide.util.lookup.AbstractLookup$Content
hcls ConvertingItem,SimpleItem

CLSS public abstract interface static org.openide.util.lookup.InstanceContent$Convertor<%0 extends java.lang.Object, %1 extends java.lang.Object>
 outer org.openide.util.lookup.InstanceContent
meth public abstract java.lang.Class<? extends {org.openide.util.lookup.InstanceContent$Convertor%1}> type({org.openide.util.lookup.InstanceContent$Convertor%0})
meth public abstract java.lang.String displayName({org.openide.util.lookup.InstanceContent$Convertor%0})
meth public abstract java.lang.String id({org.openide.util.lookup.InstanceContent$Convertor%0})
meth public abstract {org.openide.util.lookup.InstanceContent$Convertor%1} convert({org.openide.util.lookup.InstanceContent$Convertor%0})

CLSS public org.openide.util.lookup.Lookups
meth public !varargs static org.openide.util.Lookup exclude(org.openide.util.Lookup,java.lang.Class[])
meth public !varargs static org.openide.util.Lookup fixed(java.lang.Object[])
meth public static <%0 extends java.lang.Object, %1 extends java.lang.Object> org.openide.util.Lookup fixed({%%0}[],org.openide.util.lookup.InstanceContent$Convertor<? super {%%0},{%%1}>)
meth public static <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem({%%0},java.lang.String)
meth public static org.openide.util.Lookup forPath(java.lang.String)
meth public static org.openide.util.Lookup metaInfServices(java.lang.ClassLoader)
meth public static org.openide.util.Lookup metaInfServices(java.lang.ClassLoader,java.lang.String)
meth public static org.openide.util.Lookup proxy(org.openide.util.Lookup$Provider)
meth public static org.openide.util.Lookup singleton(java.lang.Object)
meth public static void executeWith(org.openide.util.Lookup,java.lang.Runnable)
supr java.lang.Object
hcls LookupItem

CLSS public abstract interface !annotation org.openide.util.lookup.NamedServiceDefinition
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=RUNTIME)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[ANNOTATION_TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String position()
meth public abstract java.lang.Class<?>[] serviceType()
meth public abstract java.lang.String path()

CLSS public org.openide.util.lookup.ProxyLookup
cons protected init()
cons public !varargs init(org.openide.util.Lookup[])
cons public init(org.openide.util.lookup.ProxyLookup$Controller)
innr public final static Controller
meth protected !varargs final void setLookups(java.util.concurrent.Executor,org.openide.util.Lookup[])
meth protected !varargs final void setLookups(org.openide.util.Lookup[])
meth protected final org.openide.util.Lookup[] getLookups()
meth protected void beforeLookup(org.openide.util.Lookup$Template<?>)
meth public final <%0 extends java.lang.Object> org.openide.util.Lookup$Item<{%%0}> lookupItem(org.openide.util.Lookup$Template<{%%0}>)
meth public final <%0 extends java.lang.Object> org.openide.util.Lookup$Result<{%%0}> lookup(org.openide.util.Lookup$Template<{%%0}>)
meth public final <%0 extends java.lang.Object> {%%0} lookup(java.lang.Class<{%%0}>)
meth public java.lang.String toString()
supr org.openide.util.Lookup
hfds data
hcls EmptyInternalData,ImmutableInternalData,LazyCollection,LazyList,LazySet,R,RealInternalData,SingleInternalData,WeakRef,WeakResult

CLSS public final static org.openide.util.lookup.ProxyLookup$Controller
 outer org.openide.util.lookup.ProxyLookup
cons public init()
meth public !varargs void setLookups(java.util.concurrent.Executor,org.openide.util.Lookup[])
meth public !varargs void setLookups(org.openide.util.Lookup[])
supr java.lang.Object
hfds consumer

CLSS public abstract interface !annotation org.openide.util.lookup.ServiceProvider
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault int position()
meth public abstract !hasdefault java.lang.String path()
meth public abstract !hasdefault java.lang.String[] supersedes()
meth public abstract java.lang.Class<?> service()

CLSS public abstract interface !annotation org.openide.util.lookup.ServiceProviders
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=SOURCE)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[TYPE])
intf java.lang.annotation.Annotation
meth public abstract org.openide.util.lookup.ServiceProvider[] value()

CLSS public abstract org.openide.util.lookup.implspi.AbstractServiceProviderProcessor
cons protected init()
meth protected !varargs final void register(javax.lang.model.element.Element,java.lang.Class<? extends java.lang.annotation.Annotation>,javax.lang.model.type.TypeMirror,java.lang.String,int,java.lang.String[])
meth protected abstract boolean handleProcess(java.util.Set<? extends javax.lang.model.element.TypeElement>,javax.annotation.processing.RoundEnvironment)
meth protected final void register(javax.lang.model.element.Element,java.lang.String)
meth protected final void register(javax.lang.model.element.TypeElement,java.lang.Class<? extends java.lang.annotation.Annotation>,javax.lang.model.type.TypeMirror,java.lang.String,int,java.lang.String[])
 anno 0 java.lang.Deprecated()
meth public final boolean process(java.util.Set<? extends javax.lang.model.element.TypeElement>,javax.annotation.processing.RoundEnvironment)
meth public javax.lang.model.SourceVersion getSupportedSourceVersion()
supr javax.annotation.processing.AbstractProcessor
hfds originatingElementsByProcessor,outputFilesByProcessor,verifiedClasses

CLSS public final org.openide.util.lookup.implspi.ActiveQueue
meth public static java.lang.ref.ReferenceQueue<java.lang.Object> queue()
supr java.lang.Object
hfds LOGGER,activeReferenceQueue
hcls Daemon,Impl

CLSS public abstract org.openide.util.lookup.implspi.NamedServicesProvider
cons protected init()
meth protected <%0 extends java.lang.Object> {%%0} lookupObject(java.lang.String,java.lang.Class<{%%0}>)
meth protected abstract org.openide.util.Lookup create(java.lang.String)
meth protected org.openide.util.Lookup lookupFor(java.lang.Object)
meth public static <%0 extends java.lang.Object> {%%0} getConfigObject(java.lang.String,java.lang.Class<{%%0}>)
meth public static org.openide.util.Lookup createLookupFor(java.lang.Object)
meth public static org.openide.util.Lookup forPath(java.lang.String)
supr java.lang.Object
hfds IN,namedServicesProviders

CLSS public abstract org.openide.util.lookup.implspi.SharedClassObjectBridge
cons protected init()
meth protected abstract <%0 extends java.lang.Object> {%%0} findObject(java.lang.Class<{%%0}>) throws java.lang.IllegalAccessException,java.lang.InstantiationException
meth public static <%0 extends java.lang.Object> {%%0} newInstance(java.lang.Class<{%%0}>) throws java.lang.IllegalAccessException,java.lang.InstantiationException
meth public static void setInstance(org.openide.util.lookup.implspi.SharedClassObjectBridge)
supr java.lang.Object
hfds INSTANCE

CLSS abstract interface org.openide.util.lookup.implspi.package-info

