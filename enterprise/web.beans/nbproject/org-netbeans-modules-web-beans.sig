#Signature file v4.1
#Version 2.42

CLSS public java.beans.FeatureDescriptor
cons public init()
meth public boolean isExpert()
meth public boolean isHidden()
meth public boolean isPreferred()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String getShortDescription()
meth public java.lang.String toString()
meth public java.util.Enumeration<java.lang.String> attributeNames()
meth public void setDisplayName(java.lang.String)
meth public void setExpert(boolean)
meth public void setHidden(boolean)
meth public void setName(java.lang.String)
meth public void setPreferred(boolean)
meth public void setShortDescription(java.lang.String)
meth public void setValue(java.lang.String,java.lang.Object)
supr java.lang.Object

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract interface org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation
meth public abstract boolean isInjectionTarget(org.netbeans.api.java.source.CompilationController,javax.lang.model.element.TypeElement)
meth public abstract boolean isStaticReferenceRequired(org.netbeans.api.java.source.CompilationController,javax.lang.model.element.TypeElement)

CLSS public org.netbeans.modules.web.beans.BeansDataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader) throws java.io.IOException
meth protected int associateLookup()
supr org.openide.loaders.MultiDataObject

CLSS public org.netbeans.modules.web.beans.CarCdiUtil
cons public init(org.netbeans.api.project.Project)
meth public java.util.Collection<org.openide.filesystems.FileObject> getBeansTargetFolder(boolean)
supr org.netbeans.modules.web.beans.CdiUtil

CLSS public org.netbeans.modules.web.beans.CdiProjectOpenHook
cons public init(org.netbeans.api.project.Project)
meth protected void projectClosed()
meth protected void projectOpened()
supr org.netbeans.spi.project.ui.ProjectOpenedHook
hfds myProject

CLSS public org.netbeans.modules.web.beans.CdiUtil
cons public init(org.netbeans.api.project.Project)
fld public final static java.lang.String BEANS = "beans"
fld public final static java.lang.String BEANS_XML = "beans.xml"
fld public final static java.lang.String WEB_INF = "WEB-INF"
meth protected org.netbeans.api.project.Project getProject()
meth public boolean isCdi11OrLater()
meth public boolean isCdiEnabled()
meth public java.util.Collection<org.openide.filesystems.FileObject> getBeansTargetFolder(boolean)
meth public org.openide.filesystems.FileObject enableCdi()
 anno 0 org.netbeans.api.annotations.common.CheckForNull()
meth public static boolean isCdi11OrLater(org.netbeans.api.project.Project)
meth public static boolean isCdiEnabled(org.netbeans.api.project.Project)
meth public static java.util.Collection<org.openide.filesystems.FileObject> getBeansTargetFolder(org.netbeans.api.project.Project,boolean)
meth public void log(java.lang.String,java.lang.Class<?>,java.lang.Object[])
meth public void log(java.lang.String,java.lang.Class<?>,java.lang.Object[],boolean)
supr java.lang.Object
hfds LOG,META_INF,myMessages,myProject

CLSS public org.netbeans.modules.web.beans.EjbCdiUtil
cons public init(org.netbeans.api.project.Project)
meth public java.util.Collection<org.openide.filesystems.FileObject> getBeansTargetFolder(boolean)
supr org.netbeans.modules.web.beans.CdiUtil

CLSS public org.netbeans.modules.web.beans.MetaModelSupport
cons public init(org.netbeans.api.project.Project)
meth public org.netbeans.api.java.classpath.ClassPath getClassPath(java.lang.String)
meth public org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.web.beans.api.model.WebBeansModel> getMetaModel()
supr java.lang.Object
hfds MODELS,myProject

CLSS public org.netbeans.modules.web.beans.WebBeanInjectionTargetQueryImplementation
cons public init()
intf org.netbeans.modules.javaee.injection.spi.InjectionTargetQueryImplementation
meth public boolean isInjectionTarget(org.netbeans.api.java.source.CompilationController,javax.lang.model.element.TypeElement)
meth public boolean isStaticReferenceRequired(org.netbeans.api.java.source.CompilationController,javax.lang.model.element.TypeElement)
supr java.lang.Object

CLSS public org.netbeans.modules.web.beans.WebCdiUtil
cons public init(org.netbeans.api.project.Project)
meth public java.util.Collection<org.openide.filesystems.FileObject> getBeansTargetFolder(boolean)
supr org.netbeans.modules.web.beans.CdiUtil

CLSS public abstract org.netbeans.modules.web.beans.api.model.AbstractModelImplementation
cons protected init(org.netbeans.modules.web.beans.api.model.ModelUnit)
meth protected org.netbeans.modules.web.beans.api.model.WebBeansModel getModel()
meth protected org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider getProvider()
meth public org.netbeans.modules.web.beans.api.model.BeansModel getBeansModel()
meth public org.netbeans.modules.web.beans.api.model.ModelUnit getModelUnit()
supr java.lang.Object
hfds myModel,myProvider,myUnit

CLSS public final !enum org.netbeans.modules.web.beans.api.model.BeanArchiveType
fld public final static org.netbeans.modules.web.beans.api.model.BeanArchiveType EXPLICIT
fld public final static org.netbeans.modules.web.beans.api.model.BeanArchiveType IMPLICIT
fld public final static org.netbeans.modules.web.beans.api.model.BeanArchiveType NONE
meth public static org.netbeans.modules.web.beans.api.model.BeanArchiveType valueOf(java.lang.String)
meth public static org.netbeans.modules.web.beans.api.model.BeanArchiveType[] values()
supr java.lang.Enum<org.netbeans.modules.web.beans.api.model.BeanArchiveType>

CLSS public abstract interface org.netbeans.modules.web.beans.api.model.BeansModel
meth public abstract boolean isCdi11OrLater()
meth public abstract java.util.LinkedHashSet<java.lang.String> getDecoratorClasses()
meth public abstract java.util.LinkedHashSet<java.lang.String> getInterceptorClasses()
meth public abstract java.util.Set<java.lang.String> getAlternativeClasses()
meth public abstract java.util.Set<java.lang.String> getAlternativeStereotypes()
meth public abstract org.netbeans.modules.web.beans.api.model.BeanArchiveType getBeanArchiveType()

CLSS public final org.netbeans.modules.web.beans.api.model.BeansModelFactory
meth public static org.netbeans.modules.web.beans.api.model.BeansModel createModel(org.netbeans.modules.web.beans.api.model.ModelUnit)
meth public static org.netbeans.modules.web.beans.api.model.BeansModel getModel(org.netbeans.modules.web.beans.api.model.ModelUnit)
supr java.lang.Object
hfds MODELS

CLSS public abstract interface org.netbeans.modules.web.beans.api.model.BeansResult
meth public abstract boolean isDisabled(javax.lang.model.element.Element)

CLSS public org.netbeans.modules.web.beans.api.model.CdiException
cons public init(java.lang.String)
supr java.lang.Exception
hfds serialVersionUID

CLSS public abstract interface org.netbeans.modules.web.beans.api.model.DependencyInjectionResult
innr public abstract interface static ApplicableResult
innr public abstract interface static Error
innr public abstract interface static InjectableResult
innr public abstract interface static ResolutionResult
innr public final static !enum ResultKind
meth public abstract javax.lang.model.element.VariableElement getVariable()
meth public abstract javax.lang.model.type.TypeMirror getVariableType()
meth public abstract org.netbeans.modules.web.beans.api.model.DependencyInjectionResult$ResultKind getKind()

CLSS public abstract interface static org.netbeans.modules.web.beans.api.model.DependencyInjectionResult$ApplicableResult
 outer org.netbeans.modules.web.beans.api.model.DependencyInjectionResult
intf org.netbeans.modules.web.beans.api.model.BeansResult
intf org.netbeans.modules.web.beans.api.model.DependencyInjectionResult
meth public abstract java.util.Set<javax.lang.model.element.Element> getProductions()
meth public abstract java.util.Set<javax.lang.model.element.TypeElement> getTypeElements()

CLSS public abstract interface static org.netbeans.modules.web.beans.api.model.DependencyInjectionResult$Error
 outer org.netbeans.modules.web.beans.api.model.DependencyInjectionResult
intf org.netbeans.modules.web.beans.api.model.DependencyInjectionResult
meth public abstract java.lang.String getMessage()

CLSS public abstract interface static org.netbeans.modules.web.beans.api.model.DependencyInjectionResult$InjectableResult
 outer org.netbeans.modules.web.beans.api.model.DependencyInjectionResult
intf org.netbeans.modules.web.beans.api.model.DependencyInjectionResult
meth public abstract javax.lang.model.element.Element getElement()

CLSS public abstract interface static org.netbeans.modules.web.beans.api.model.DependencyInjectionResult$ResolutionResult
 outer org.netbeans.modules.web.beans.api.model.DependencyInjectionResult
intf org.netbeans.modules.web.beans.api.model.DependencyInjectionResult
intf org.netbeans.modules.web.beans.api.model.Result
meth public abstract boolean hasAlternative(javax.lang.model.element.Element)
meth public abstract boolean isAlternative(javax.lang.model.element.Element)

CLSS public final static !enum org.netbeans.modules.web.beans.api.model.DependencyInjectionResult$ResultKind
 outer org.netbeans.modules.web.beans.api.model.DependencyInjectionResult
fld public final static org.netbeans.modules.web.beans.api.model.DependencyInjectionResult$ResultKind DEFINITION_ERROR
fld public final static org.netbeans.modules.web.beans.api.model.DependencyInjectionResult$ResultKind INJECTABLES_RESOLVED
fld public final static org.netbeans.modules.web.beans.api.model.DependencyInjectionResult$ResultKind INJECTABLE_RESOLVED
fld public final static org.netbeans.modules.web.beans.api.model.DependencyInjectionResult$ResultKind RESOLUTION_ERROR
meth public static org.netbeans.modules.web.beans.api.model.DependencyInjectionResult$ResultKind valueOf(java.lang.String)
meth public static org.netbeans.modules.web.beans.api.model.DependencyInjectionResult$ResultKind[] values()
supr java.lang.Enum<org.netbeans.modules.web.beans.api.model.DependencyInjectionResult$ResultKind>

CLSS public org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError
cons public init(javax.lang.model.element.Element,java.lang.String)
meth public javax.lang.model.element.Element getErrorElement()
supr org.netbeans.modules.web.beans.api.model.CdiException
hfds myElement,serialVersionUID

CLSS public abstract interface org.netbeans.modules.web.beans.api.model.InterceptorsResult
intf org.netbeans.modules.web.beans.api.model.BeansResult
intf org.netbeans.modules.web.beans.api.model.Result
meth public abstract java.util.List<javax.lang.model.element.TypeElement> getAllInterceptors()
meth public abstract java.util.List<javax.lang.model.element.TypeElement> getDeclaredInterceptors()
meth public abstract java.util.List<javax.lang.model.element.TypeElement> getResolvedInterceptors()
meth public abstract javax.lang.model.element.Element getElement()

CLSS public org.netbeans.modules.web.beans.api.model.ModelUnit
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public org.netbeans.api.java.classpath.ClassPath getBootPath()
meth public org.netbeans.api.java.classpath.ClassPath getCompilePath()
meth public org.netbeans.api.java.classpath.ClassPath getSourcePath()
meth public org.netbeans.api.java.source.ClasspathInfo getClassPathInfo()
meth public org.netbeans.api.project.Project getProject()
meth public static org.netbeans.modules.web.beans.api.model.ModelUnit create(org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.project.Project)
supr java.lang.Object
hfds myBootPath,myClassPathInfo,myCompilePath,myProject,mySourcePath

CLSS public abstract interface org.netbeans.modules.web.beans.api.model.Result
meth public abstract java.util.List<javax.lang.model.element.AnnotationMirror> getAllStereotypes(javax.lang.model.element.Element)
meth public abstract java.util.List<javax.lang.model.element.AnnotationMirror> getStereotypes(javax.lang.model.element.Element)

CLSS public final org.netbeans.modules.web.beans.api.model.WebBeansModel
meth public boolean hasImplicitDefaultQualifier(javax.lang.model.element.Element)
meth public boolean isCdi11OrLater()
meth public boolean isDynamicInjectionPoint(javax.lang.model.element.VariableElement)
meth public boolean isEventInjectionPoint(javax.lang.model.element.VariableElement)
meth public boolean isInjectionPoint(javax.lang.model.element.VariableElement) throws org.netbeans.modules.web.beans.api.model.InjectionPointDefinitionError
meth public java.lang.String getName(javax.lang.model.element.Element)
meth public java.lang.String getScope(javax.lang.model.element.Element) throws org.netbeans.modules.web.beans.api.model.CdiException
meth public java.util.Collection<javax.lang.model.element.AnnotationMirror> getInterceptorBindings(javax.lang.model.element.Element)
meth public java.util.Collection<javax.lang.model.element.TypeElement> getDecorators(javax.lang.model.element.TypeElement)
meth public java.util.List<javax.lang.model.element.AnnotationMirror> getQualifiers(javax.lang.model.element.Element,boolean)
meth public java.util.List<javax.lang.model.element.Element> getNamedElements()
meth public java.util.List<javax.lang.model.element.ExecutableElement> getObservers(javax.lang.model.element.VariableElement,javax.lang.model.type.DeclaredType)
meth public java.util.List<javax.lang.model.element.VariableElement> getEventInjectionPoints(javax.lang.model.element.ExecutableElement,javax.lang.model.type.DeclaredType)
meth public javax.lang.model.element.VariableElement getObserverParameter(javax.lang.model.element.ExecutableElement)
meth public javax.lang.model.type.TypeMirror resolveType(java.lang.String)
meth public org.netbeans.api.java.source.CompilationController getCompilationController()
meth public org.netbeans.modules.web.beans.api.model.AbstractModelImplementation getModelImplementation()
meth public org.netbeans.modules.web.beans.api.model.DependencyInjectionResult lookupInjectables(javax.lang.model.element.VariableElement,javax.lang.model.type.DeclaredType,java.util.concurrent.atomic.AtomicBoolean)
meth public org.netbeans.modules.web.beans.api.model.InterceptorsResult getInterceptors(javax.lang.model.element.Element)
supr java.lang.Object
hfds myImpl

CLSS public final org.netbeans.modules.web.beans.api.model.WebBeansModelFactory
meth public static org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.web.beans.api.model.WebBeansModel> createMetaModel(org.netbeans.modules.web.beans.api.model.ModelUnit)
meth public static org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.web.beans.api.model.WebBeansModel> getMetaModel(org.netbeans.modules.web.beans.api.model.ModelUnit)
supr java.lang.Object
hfds MODELS

CLSS public abstract org.netbeans.spi.project.ui.ProjectOpenedHook
cons protected init()
meth protected abstract void projectClosed()
meth protected abstract void projectOpened()
supr java.lang.Object

CLSS public abstract org.openide.loaders.DataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.DataLoader) throws org.openide.loaders.DataObjectExistsException
fld public final static java.lang.String PROP_COOKIE = "cookie"
fld public final static java.lang.String PROP_FILES = "files"
fld public final static java.lang.String PROP_HELP = "helpCtx"
fld public final static java.lang.String PROP_MODIFIED = "modified"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_PRIMARY_FILE = "primaryFile"
fld public final static java.lang.String PROP_TEMPLATE = "template"
fld public final static java.lang.String PROP_VALID = "valid"
innr public abstract interface static !annotation Registration
innr public abstract interface static !annotation Registrations
innr public abstract interface static Container
innr public abstract interface static Factory
innr public final static Registry
intf java.io.Serializable
intf org.openide.nodes.Node$Cookie
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(org.openide.loaders.DataShadow,java.lang.Class<{%%0}>)
meth protected abstract org.openide.filesystems.FileObject handleMove(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected abstract org.openide.filesystems.FileObject handleRename(java.lang.String) throws java.io.IOException
meth protected abstract org.openide.loaders.DataObject handleCopy(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected abstract org.openide.loaders.DataObject handleCreateFromTemplate(org.openide.loaders.DataFolder,java.lang.String) throws java.io.IOException
meth protected abstract void handleDelete() throws java.io.IOException
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void fireVetoableChange(java.lang.String,java.lang.Object,java.lang.Object) throws java.beans.PropertyVetoException
meth protected final void markFiles() throws java.io.IOException
meth protected org.openide.filesystems.FileLock takePrimaryFileLock() throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCopyRename(org.openide.loaders.DataFolder,java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.openide.loaders.DataShadow handleCreateShadow(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.nodes.Node createNodeDelegate()
meth protected void dispose()
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public abstract boolean isCopyAllowed()
meth public abstract boolean isDeleteAllowed()
meth public abstract boolean isMoveAllowed()
meth public abstract boolean isRenameAllowed()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public boolean isModified()
meth public boolean isShadowAllowed()
meth public final boolean isTemplate()
meth public final boolean isValid()
meth public final org.openide.filesystems.FileObject getPrimaryFile()
meth public final org.openide.loaders.DataFolder getFolder()
meth public final org.openide.loaders.DataLoader getLoader()
meth public final org.openide.loaders.DataObject copy(org.openide.loaders.DataFolder) throws java.io.IOException
meth public final org.openide.loaders.DataObject createFromTemplate(org.openide.loaders.DataFolder) throws java.io.IOException
meth public final org.openide.loaders.DataObject createFromTemplate(org.openide.loaders.DataFolder,java.lang.String) throws java.io.IOException
meth public final org.openide.loaders.DataObject createFromTemplate(org.openide.loaders.DataFolder,java.lang.String,java.util.Map<java.lang.String,?>) throws java.io.IOException
meth public final org.openide.loaders.DataShadow createShadow(org.openide.loaders.DataFolder) throws java.io.IOException
meth public final org.openide.nodes.Node getNodeDelegate()
meth public final void delete() throws java.io.IOException
meth public final void move(org.openide.loaders.DataFolder) throws java.io.IOException
meth public final void rename(java.lang.String) throws java.io.IOException
meth public final void setTemplate(boolean) throws java.io.IOException
meth public java.lang.Object writeReplace()
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.Set<org.openide.filesystems.FileObject> files()
meth public org.openide.util.Lookup getLookup()
meth public static org.openide.loaders.DataObject find(org.openide.filesystems.FileObject) throws org.openide.loaders.DataObjectNotFoundException
meth public static org.openide.loaders.DataObject$Registry getRegistry()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeVetoableChangeListener(java.beans.VetoableChangeListener)
meth public void setModified(boolean)
meth public void setValid(boolean) throws java.beans.PropertyVetoException
supr java.lang.Object
hfds BEING_CREATED,EA_ASSIGNED_LOADER,EA_ASSIGNED_LOADER_MODULE,LOCK,LOG,OBJ_LOG,PROGRESS_INFO_TL,REGISTRY_INSTANCE,changeSupport,changeSupportUpdater,item,loader,modif,modified,nodeDelegate,serialVersionUID,syncModified,synchObject,vetoableChangeSupport,warnedClasses
hcls CreateAction,DOSavable,ModifiedRegistry,ProgressInfo,Replace

CLSS public org.openide.loaders.MultiDataObject
cons public init(org.openide.filesystems.FileObject,org.openide.loaders.MultiFileLoader) throws org.openide.loaders.DataObjectExistsException
innr public abstract Entry
meth protected final org.openide.loaders.MultiDataObject$Entry registerEntry(org.openide.filesystems.FileObject)
meth protected final org.openide.nodes.CookieSet getCookieSet()
meth protected final void addSecondaryEntry(org.openide.loaders.MultiDataObject$Entry)
meth protected final void registerEditor(java.lang.String,boolean)
meth protected final void removeSecondaryEntry(org.openide.loaders.MultiDataObject$Entry)
meth protected final void setCookieSet(org.openide.nodes.CookieSet)
 anno 0 java.lang.Deprecated()
meth protected int associateLookup()
meth protected org.openide.filesystems.FileLock takePrimaryFileLock() throws java.io.IOException
meth protected org.openide.filesystems.FileObject handleMove(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.filesystems.FileObject handleRename(java.lang.String) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCopy(org.openide.loaders.DataFolder) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCopyRename(org.openide.loaders.DataFolder,java.lang.String,java.lang.String) throws java.io.IOException
meth protected org.openide.loaders.DataObject handleCreateFromTemplate(org.openide.loaders.DataFolder,java.lang.String) throws java.io.IOException
meth protected org.openide.nodes.Node createNodeDelegate()
meth protected void handleDelete() throws java.io.IOException
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public boolean isCopyAllowed()
meth public boolean isDeleteAllowed()
meth public boolean isMoveAllowed()
meth public boolean isRenameAllowed()
meth public final java.util.Set<org.openide.loaders.MultiDataObject$Entry> secondaryEntries()
meth public final org.openide.loaders.MultiDataObject$Entry findSecondaryEntry(org.openide.filesystems.FileObject)
meth public final org.openide.loaders.MultiDataObject$Entry getPrimaryEntry()
meth public final org.openide.loaders.MultiFileLoader getMultiFileLoader()
meth public java.util.Set<org.openide.filesystems.FileObject> files()
meth public org.openide.util.HelpCtx getHelpCtx()
meth public org.openide.util.Lookup getLookup()
supr org.openide.loaders.DataObject
hfds ERR,RECOGNIZER,TEMPLATE_ATTRIBUTES,chLis,checked,cookieSet,cookieSetLock,delayProcessor,delayedPropFilesLock,delayedPropFilesTask,firingProcessor,later,primary,secondary,secondaryCreationLock,serialVersionUID
hcls ChangeAndBefore,EmptyRecognizer,EntryReplace,Pair

CLSS public abstract org.openide.nodes.Node
cons protected init(org.openide.nodes.Children)
cons protected init(org.openide.nodes.Children,org.openide.util.Lookup)
fld public final static java.lang.String PROP_COOKIE = "cookie"
fld public final static java.lang.String PROP_DISPLAY_NAME = "displayName"
fld public final static java.lang.String PROP_ICON = "icon"
fld public final static java.lang.String PROP_LEAF = "leaf"
fld public final static java.lang.String PROP_NAME = "name"
fld public final static java.lang.String PROP_OPENED_ICON = "openedIcon"
fld public final static java.lang.String PROP_PARENT_NODE = "parentNode"
fld public final static java.lang.String PROP_PROPERTY_SETS = "propertySets"
fld public final static java.lang.String PROP_SHORT_DESCRIPTION = "shortDescription"
fld public final static org.openide.nodes.Node EMPTY
innr public abstract interface static Cookie
innr public abstract interface static Handle
innr public abstract static IndexedProperty
innr public abstract static Property
innr public abstract static PropertySet
intf org.openide.util.HelpCtx$Provider
intf org.openide.util.Lookup$Provider
meth protected final boolean hasPropertyChangeListener()
meth protected final void fireCookieChange()
meth protected final void fireDisplayNameChange(java.lang.String,java.lang.String)
meth protected final void fireIconChange()
meth protected final void fireNameChange(java.lang.String,java.lang.String)
meth protected final void fireNodeDestroyed()
meth protected final void fireOpenedIconChange()
meth protected final void firePropertyChange(java.lang.String,java.lang.Object,java.lang.Object)
meth protected final void firePropertySetsChange(org.openide.nodes.Node$PropertySet[],org.openide.nodes.Node$PropertySet[])
meth protected final void fireShortDescriptionChange(java.lang.String,java.lang.String)
meth protected final void setChildren(org.openide.nodes.Children)
meth protected java.lang.Object clone() throws java.lang.CloneNotSupportedException
meth public <%0 extends org.openide.nodes.Node$Cookie> {%%0} getCookie(java.lang.Class<{%%0}>)
meth public abstract boolean canCopy()
meth public abstract boolean canCut()
meth public abstract boolean canDestroy()
meth public abstract boolean canRename()
meth public abstract boolean hasCustomizer()
meth public abstract java.awt.Component getCustomizer()
meth public abstract java.awt.Image getIcon(int)
meth public abstract java.awt.Image getOpenedIcon(int)
meth public abstract java.awt.datatransfer.Transferable clipboardCopy() throws java.io.IOException
meth public abstract java.awt.datatransfer.Transferable clipboardCut() throws java.io.IOException
meth public abstract java.awt.datatransfer.Transferable drag() throws java.io.IOException
meth public abstract org.openide.nodes.Node cloneNode()
meth public abstract org.openide.nodes.Node$Handle getHandle()
meth public abstract org.openide.nodes.Node$PropertySet[] getPropertySets()
meth public abstract org.openide.util.HelpCtx getHelpCtx()
meth public abstract org.openide.util.datatransfer.NewType[] getNewTypes()
meth public abstract org.openide.util.datatransfer.PasteType getDropType(java.awt.datatransfer.Transferable,int,int)
meth public abstract org.openide.util.datatransfer.PasteType[] getPasteTypes(java.awt.datatransfer.Transferable)
meth public boolean equals(java.lang.Object)
meth public final boolean isLeaf()
meth public final javax.swing.JPopupMenu getContextMenu()
meth public final org.openide.nodes.Children getChildren()
meth public final org.openide.nodes.Node getParentNode()
meth public final org.openide.util.Lookup getLookup()
meth public final void addNodeListener(org.openide.nodes.NodeListener)
meth public final void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public final void removeNodeListener(org.openide.nodes.NodeListener)
meth public final void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public int hashCode()
meth public java.lang.String getHtmlDisplayName()
meth public java.lang.String toString()
meth public javax.swing.Action getPreferredAction()
meth public javax.swing.Action[] getActions(boolean)
meth public org.openide.util.actions.SystemAction getDefaultAction()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getActions()
 anno 0 java.lang.Deprecated()
meth public org.openide.util.actions.SystemAction[] getContextActions()
 anno 0 java.lang.Deprecated()
meth public void destroy() throws java.io.IOException
meth public void setDisplayName(java.lang.String)
meth public void setHidden(boolean)
 anno 0 java.lang.Deprecated()
meth public void setName(java.lang.String)
meth public void setShortDescription(java.lang.String)
supr java.beans.FeatureDescriptor
hfds BLOCK_EVENTS,INIT_LOCK,LOCK,TEMPL_COOKIE,err,hierarchy,listeners,lookups,parent,warnedBadProperties
hcls LookupEventList,PropertyEditorRef

CLSS public abstract interface static org.openide.nodes.Node$Cookie
 outer org.openide.nodes.Node

CLSS public final org.openide.util.HelpCtx
cons public init(java.lang.Class<?>)
 anno 0 java.lang.Deprecated()
cons public init(java.lang.String)
cons public init(java.net.URL)
 anno 0 java.lang.Deprecated()
fld public final static org.openide.util.HelpCtx DEFAULT_HELP
innr public abstract interface static Displayer
innr public abstract interface static Provider
meth public boolean display()
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public java.lang.String getHelpID()
meth public java.lang.String toString()
meth public java.net.URL getHelp()
meth public static org.openide.util.HelpCtx findHelp(java.awt.Component)
meth public static org.openide.util.HelpCtx findHelp(java.lang.Object)
meth public static void setHelpIDString(javax.swing.JComponent,java.lang.String)
supr java.lang.Object
hfds err,helpCtx,helpID

CLSS public abstract interface static org.openide.util.HelpCtx$Provider
 outer org.openide.util.HelpCtx
meth public abstract org.openide.util.HelpCtx getHelpCtx()

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

CLSS public abstract interface static org.openide.util.Lookup$Provider
 outer org.openide.util.Lookup
meth public abstract org.openide.util.Lookup getLookup()

