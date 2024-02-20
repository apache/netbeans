#Signature file v4.1
#Version 1.63.0

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

CLSS public abstract interface org.netbeans.modules.spring.api.Action<%0 extends java.lang.Object>
meth public abstract void run({org.netbeans.modules.spring.api.Action%0})

CLSS public final org.netbeans.modules.spring.api.SpringUtilities
fld public final static java.lang.String SPRING_CLASS_NAME = "org.springframework.core.SpringVersion"
meth public static boolean containsSpring(org.netbeans.api.java.classpath.ClassPath)
meth public static boolean isSpringLibrary(org.netbeans.api.project.libraries.Library)
meth public static boolean isSpringWebMVCLibrary(org.netbeans.api.project.libraries.Library)
meth public static java.lang.String getImplementationVersion(org.openide.filesystems.JarFileSystem)
meth public static java.lang.String getSpringLibraryVersion(org.netbeans.api.project.libraries.Library)
meth public static org.netbeans.api.project.libraries.Library findJSTLibrary()
meth public static org.netbeans.api.project.libraries.Library findSpringLibrary()
meth public static org.netbeans.api.project.libraries.Library findSpringWebMVCLibrary()
meth public static org.netbeans.api.project.libraries.Library findSpringWebMVCLibrary(java.lang.String)
meth public static org.netbeans.api.project.libraries.Library[] getJavaLibraries()
supr java.lang.Object
hfds JSTL_CLASS_NAME,SPRING_WEBMVC_CLASS_NAME

CLSS public final org.netbeans.modules.spring.api.beans.ConfigFileGroup
meth public boolean containsFile(java.io.File)
meth public java.lang.String getName()
meth public java.lang.String toString()
meth public java.util.List<java.io.File> getFiles()
meth public static org.netbeans.modules.spring.api.beans.ConfigFileGroup create(java.lang.String,java.util.List<java.io.File>)
meth public static org.netbeans.modules.spring.api.beans.ConfigFileGroup create(java.util.List<java.io.File>)
supr java.lang.Object
hfds files,name

CLSS public final org.netbeans.modules.spring.api.beans.ConfigFileManager
meth public java.util.List<java.io.File> getConfigFiles()
meth public java.util.List<org.netbeans.modules.spring.api.beans.ConfigFileGroup> getConfigFileGroups()
meth public org.openide.util.Mutex mutex()
meth public void putConfigFilesAndGroups(java.util.List<java.io.File>,java.util.List<org.netbeans.modules.spring.api.beans.ConfigFileGroup>)
meth public void save() throws java.io.IOException
supr java.lang.Object
hfds impl

CLSS public final org.netbeans.modules.spring.api.beans.SpringAnnotations
cons public init()
fld public final static java.util.Set<java.lang.String> SPRING_COMPONENTS
supr java.lang.Object

CLSS public final org.netbeans.modules.spring.api.beans.SpringConstants
fld public final static java.lang.String CONFIG_MIME_TYPE = "text/x-springconfig+xml"
supr java.lang.Object

CLSS public final org.netbeans.modules.spring.api.beans.SpringScope
meth public java.util.List<org.netbeans.modules.spring.api.beans.model.SpringConfigModel> getAllConfigModels()
meth public org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.spring.api.beans.model.SpringModel> getSpringAnnotationModel(org.openide.filesystems.FileObject)
meth public org.netbeans.modules.spring.api.beans.ConfigFileManager getConfigFileManager()
meth public static org.netbeans.modules.spring.api.beans.SpringScope getSpringScope(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds configFileManager,fileModelManager,springAnnotationModel

CLSS public abstract org.netbeans.modules.spring.api.beans.model.AbstractModelImplementation
cons protected init(org.netbeans.modules.spring.api.beans.model.ModelUnit)
meth protected org.netbeans.modules.spring.api.beans.model.SpringModel getModel()
meth protected org.netbeans.modules.spring.spi.beans.SpringModelProvider getProvider()
meth public org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper getHelper()
meth public org.netbeans.modules.spring.api.beans.model.ModelUnit getModelUnit()
supr java.lang.Object
hfds helper,model,modelUnit,provider

CLSS public abstract interface org.netbeans.modules.spring.api.beans.model.FileSpringBeans
meth public abstract java.lang.String findAliasName(java.lang.String)
meth public abstract java.util.List<org.netbeans.modules.spring.api.beans.model.SpringBean> getBeans()
meth public abstract java.util.Set<java.lang.String> getAliases()
meth public abstract org.netbeans.modules.spring.api.beans.model.SpringBean findBean(java.lang.String)
meth public abstract org.netbeans.modules.spring.api.beans.model.SpringBean findBeanByID(java.lang.String)

CLSS public abstract interface org.netbeans.modules.spring.api.beans.model.Location
meth public abstract int getOffset()
meth public abstract org.openide.filesystems.FileObject getFile()

CLSS public org.netbeans.modules.spring.api.beans.model.ModelUnit
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public org.netbeans.api.java.classpath.ClassPath getBootPath()
meth public org.netbeans.api.java.classpath.ClassPath getCompilePath()
meth public org.netbeans.api.java.classpath.ClassPath getSourcePath()
meth public org.netbeans.api.java.source.ClasspathInfo getClassPathInfo()
meth public static org.netbeans.modules.spring.api.beans.model.ModelUnit create(org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.java.classpath.ClassPath)
supr java.lang.Object
hfds bootPath,classPathInfo,compilePath,sourcePath

CLSS public abstract interface org.netbeans.modules.spring.api.beans.model.SpringBean
meth public abstract java.lang.String getClassName()
meth public abstract java.lang.String getFactoryBean()
meth public abstract java.lang.String getFactoryMethod()
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getParent()
meth public abstract java.util.List<java.lang.String> getNames()
meth public abstract java.util.Set<org.netbeans.modules.spring.api.beans.model.SpringBeanProperty> getProperties()
meth public abstract org.netbeans.modules.spring.api.beans.model.Location getLocation()

CLSS public abstract interface org.netbeans.modules.spring.api.beans.model.SpringBeanProperty
meth public abstract java.lang.String getName()

CLSS public abstract interface org.netbeans.modules.spring.api.beans.model.SpringBeans
meth public abstract java.util.List<org.netbeans.modules.spring.api.beans.model.SpringBean> getBeans()
meth public abstract java.util.Set<java.lang.String> getAliases()
meth public abstract org.netbeans.modules.spring.api.beans.model.FileSpringBeans getFileBeans(org.openide.filesystems.FileObject)
meth public abstract org.netbeans.modules.spring.api.beans.model.SpringBean findBean(java.lang.String)

CLSS public final org.netbeans.modules.spring.api.beans.model.SpringConfigModel
innr public final static DocumentAccess
meth public static org.netbeans.modules.spring.api.beans.model.SpringConfigModel forFileObject(org.openide.filesystems.FileObject)
meth public void runDocumentAction(org.netbeans.modules.spring.api.Action<org.netbeans.modules.spring.api.beans.model.SpringConfigModel$DocumentAccess>) throws java.io.IOException
meth public void runReadAction(org.netbeans.modules.spring.api.Action<org.netbeans.modules.spring.api.beans.model.SpringBeans>) throws java.io.IOException
supr java.lang.Object
hfds controller

CLSS public final static org.netbeans.modules.spring.api.beans.model.SpringConfigModel$DocumentAccess
 outer org.netbeans.modules.spring.api.beans.model.SpringConfigModel
meth public java.io.File getFile()
meth public javax.swing.text.Document getDocument()
meth public org.netbeans.modules.spring.api.beans.model.SpringBeans getSpringBeans()
meth public org.openide.filesystems.FileObject getFileObject()
meth public org.openide.text.PositionRef createPositionRef(int,javax.swing.text.Position$Bias)
supr java.lang.Object
hfds file,lockedDoc,springBeans

CLSS public final org.netbeans.modules.spring.api.beans.model.SpringMetaModelSupport
cons public init(org.netbeans.api.project.Project)
meth public org.netbeans.api.java.classpath.ClassPath getClassPath(java.lang.String)
meth public org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.spring.api.beans.model.SpringModel> getMetaModel()
meth public org.netbeans.modules.spring.api.beans.model.ModelUnit getModelUnit()
supr java.lang.Object
hfds TYPE_WEB_INF,project

CLSS public final org.netbeans.modules.spring.api.beans.model.SpringModel
cons public init(org.netbeans.modules.spring.api.beans.model.AbstractModelImplementation)
meth public java.util.List<org.netbeans.modules.spring.api.beans.model.SpringBean> getBeans()
meth public org.netbeans.modules.spring.api.beans.model.AbstractModelImplementation getModelImplementation()
supr java.lang.Object
hfds abstractModelImplementation

CLSS public final org.netbeans.modules.spring.api.beans.model.SpringModelFactory
fld protected static java.util.HashMap<org.netbeans.modules.spring.api.beans.model.ModelUnit,java.lang.ref.WeakReference<org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.spring.api.beans.model.SpringModel>>> MODELS
meth public static org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.spring.api.beans.model.SpringModel> createMetaModel(org.netbeans.modules.spring.api.beans.model.ModelUnit)
meth public static org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.spring.api.beans.model.SpringModel> getMetaModel(org.netbeans.modules.spring.api.beans.model.ModelUnit)
supr java.lang.Object
hfds LOGGER

CLSS public abstract interface org.netbeans.modules.spring.spi.beans.SpringConfigFileLocationProvider
meth public abstract org.openide.filesystems.FileObject getLocation()

CLSS public abstract interface org.netbeans.modules.spring.spi.beans.SpringConfigFileProvider
meth public abstract java.util.Set<java.io.File> getConfigFiles()

CLSS public abstract interface org.netbeans.modules.spring.spi.beans.SpringModelProvider
meth public abstract java.util.List<org.netbeans.modules.spring.api.beans.model.SpringBean> getBeans()

CLSS public abstract interface org.netbeans.modules.spring.spi.beans.SpringModelProviderFactory
meth public abstract org.netbeans.modules.spring.spi.beans.SpringModelProvider createSpringModelProvider(org.netbeans.modules.spring.api.beans.model.AbstractModelImplementation)

