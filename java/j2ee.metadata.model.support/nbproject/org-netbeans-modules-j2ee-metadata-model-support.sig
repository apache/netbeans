#Signature file v4.1
#Version 1.54

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

CLSS public abstract interface org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler
meth public abstract void handleAnnotation(javax.lang.model.element.TypeElement,javax.lang.model.element.Element,javax.lang.model.element.AnnotationMirror)

CLSS public org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHelper
cons public init(org.netbeans.api.java.source.CompilationInfo)
cons public init(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper)
meth public boolean hasAnnotation(java.util.List<? extends javax.lang.model.element.AnnotationMirror>,java.lang.String)
meth public boolean hasAnyAnnotation(java.util.List<? extends javax.lang.model.element.AnnotationMirror>,java.util.Set<java.lang.String>)
meth public boolean isSameRawType(javax.lang.model.type.TypeMirror,java.lang.String)
meth public java.lang.String getAnnotationTypeName(javax.lang.model.type.DeclaredType)
meth public java.util.List<? extends javax.lang.model.element.TypeElement> getSuperclasses(javax.lang.model.element.TypeElement)
meth public java.util.Map<java.lang.String,? extends javax.lang.model.element.AnnotationMirror> getAnnotationsByType(java.util.List<? extends javax.lang.model.element.AnnotationMirror>)
meth public javax.lang.model.element.TypeElement getSuperclass(javax.lang.model.element.TypeElement)
meth public javax.lang.model.type.TypeMirror resolveType(java.lang.String)
meth public org.netbeans.api.java.source.CompilationInfo getCompilationInfo()
meth public org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationScanner getAnnotationScanner()
supr java.lang.Object
hfds helper,info

CLSS public final org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper
meth public <%0 extends java.lang.Object> java.util.concurrent.Future<{%%0}> runJavaSourceTaskWhenScanFinished(java.util.concurrent.Callable<{%%0}>) throws java.io.IOException
meth public <%0 extends java.lang.Object> {%%0} runJavaSourceTask(java.util.concurrent.Callable<{%%0}>) throws java.io.IOException
meth public <%0 extends org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject> org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager<{%%0}> createPersistentObjectManager(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider<{%%0}>)
meth public boolean hasAnnotation(java.util.List<? extends javax.lang.model.element.AnnotationMirror>,java.lang.String)
meth public boolean hasAnyAnnotation(java.util.List<? extends javax.lang.model.element.AnnotationMirror>,java.util.Set<java.lang.String>)
meth public boolean isJavaScanInProgress()
meth public boolean isSameRawType(javax.lang.model.type.TypeMirror,java.lang.String)
meth public java.lang.String getAnnotationTypeName(javax.lang.model.type.DeclaredType)
meth public java.util.List<? extends javax.lang.model.element.TypeElement> getSuperclasses(javax.lang.model.element.TypeElement)
meth public java.util.Map<java.lang.String,? extends javax.lang.model.element.AnnotationMirror> getAnnotationsByType(java.util.List<? extends javax.lang.model.element.AnnotationMirror>)
meth public javax.lang.model.element.TypeElement getSuperclass(javax.lang.model.element.TypeElement)
meth public javax.lang.model.type.TypeMirror resolveType(java.lang.String)
meth public org.netbeans.api.java.source.ClasspathInfo getClasspathInfo()
meth public org.netbeans.api.java.source.CompilationController getCompilationController()
meth public org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHelper getHelper()
meth public org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationScanner getAnnotationScanner()
meth public static org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper create(org.netbeans.api.java.source.ClasspathInfo)
meth public void addJavaContextListener(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.JavaContextListener)
meth public void runJavaSourceTask(java.lang.Runnable) throws java.io.IOException
supr java.lang.Object
hfds annotationScanner,classIndex,controller,cpi,helper,javaContextListeners,javaSource,listener,managers,userActionTaskThread
hcls ClassIndexListenerImpl,DelegatingFuture

CLSS public org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationScanner
fld public final static java.util.Set<javax.lang.model.element.ElementKind> TYPE_KINDS
meth public void findAnnotations(java.lang.String,java.util.Set<javax.lang.model.element.ElementKind>,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler) throws java.lang.InterruptedException
meth public void findAnnotations(java.lang.String,java.util.Set<javax.lang.model.element.ElementKind>,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler,boolean) throws java.lang.InterruptedException
supr java.lang.Object
hfds LOGGER,helper

CLSS public abstract interface org.netbeans.modules.j2ee.metadata.model.api.support.annotation.JavaContextListener
meth public abstract void javaContextLeft()

CLSS public abstract interface org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider<%0 extends java.lang.Object>
meth public abstract boolean modifyObjects(javax.lang.model.element.TypeElement,java.util.List<{org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider%0}>)
meth public abstract java.util.List<{org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider%0}> createInitialObjects() throws java.lang.InterruptedException
meth public abstract java.util.List<{org.netbeans.modules.j2ee.metadata.model.api.support.annotation.ObjectProvider%0}> createObjects(javax.lang.model.element.TypeElement)

CLSS public abstract org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject
cons public init(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper,javax.lang.model.element.TypeElement)
meth protected final org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper getHelper()
meth public final javax.lang.model.element.TypeElement getTypeElement()
meth public final org.netbeans.api.java.source.ElementHandle<javax.lang.model.element.TypeElement> getTypeElementHandle()
supr java.lang.Object
hfds helper,typeElementHandle

CLSS public org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager<%0 extends org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject>
intf org.netbeans.modules.j2ee.metadata.model.api.support.annotation.JavaContextListener
meth public java.util.Collection<{org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager%0}> getObjects()
meth public void addChangeListener(javax.swing.event.ChangeListener)
meth public void javaContextLeft()
meth public void removeChangeListener(javax.swing.event.ChangeListener)
supr java.lang.Object
hfds LOGGER,NO_EVENTS,changeSupport,helper,initialized,objectList,provider,rp,temporary

CLSS public final org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser
meth public <%0 extends java.lang.Object> void expectPrimitive(java.lang.String,java.lang.Class<{%%0}>,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.DefaultProvider)
meth public org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult parse(javax.lang.model.element.AnnotationMirror)
meth public static org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser create(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHelper)
meth public static org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser create(org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper)
meth public static org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.DefaultProvider defaultValue(java.lang.Object)
meth public void expect(java.lang.String,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ValueProvider)
meth public void expectAnnotation(java.lang.String,javax.lang.model.type.TypeMirror,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationValueHandler,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.DefaultProvider)
meth public void expectAnnotationArray(java.lang.String,javax.lang.model.type.TypeMirror,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.DefaultProvider)
meth public void expectClass(java.lang.String,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.DefaultProvider)
meth public void expectClassArray(java.lang.String,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.DefaultProvider)
meth public void expectEnumConstant(java.lang.String,javax.lang.model.type.TypeMirror,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.DefaultProvider)
meth public void expectEnumConstantArray(java.lang.String,javax.lang.model.type.TypeMirror,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.DefaultProvider)
meth public void expectPrimitiveArray(java.lang.String,java.lang.Class<?>,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.DefaultProvider)
meth public void expectString(java.lang.String,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.DefaultProvider)
meth public void expectStringArray(java.lang.String,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler,org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.DefaultProvider)
supr java.lang.Object
hfds PRIMITIVE_WRAPPERS,helper,providers
hcls AnnotationArrayValueProvider,AnnotationValueProvider,ClassArrayValueProvider,ClassValueProvider,DefaultArrayValueProvider,DefaultProviderImpl,DefaultValueProvider,EnumConstantArrayValueProvider,EnumConstantValueProvider,PrimitiveArrayValueProvider,PrimitiveValueProvider,TypeCheckingArrayValueProvider,TypeCheckingValueProvider

CLSS public abstract interface org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationValueHandler
meth public abstract java.lang.Object handleAnnotation(javax.lang.model.element.AnnotationMirror)

CLSS public abstract interface org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ArrayValueHandler
meth public abstract java.lang.Object handleArray(java.util.List<javax.lang.model.element.AnnotationValue>)

CLSS public abstract interface org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.DefaultProvider
meth public abstract java.lang.Object getDefaultValue()

CLSS public final org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult
meth public <%0 extends java.lang.Object> {%%0} get(java.lang.String,java.lang.Class<{%%0}>)
supr java.lang.Object
hfds resultMap

CLSS public abstract interface org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ValueProvider
intf org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.DefaultProvider
meth public abstract java.lang.Object getValue(javax.lang.model.element.AnnotationValue)

