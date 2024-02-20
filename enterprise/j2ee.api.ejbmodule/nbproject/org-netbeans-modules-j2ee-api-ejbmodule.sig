#Signature file v4.1
#Version 1.60

CLSS public abstract interface java.io.Serializable

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

CLSS public final org.netbeans.modules.j2ee.api.ejbjar.Car
meth public java.lang.String getJ2eePlatformVersion()
 anno 0 java.lang.Deprecated()
meth public org.netbeans.api.j2ee.core.Profile getJ2eeProfile()
meth public org.openide.filesystems.FileObject getDeploymentDescriptor()
meth public org.openide.filesystems.FileObject getMetaInf()
meth public org.openide.filesystems.FileObject[] getJavaSources()
meth public static org.netbeans.modules.j2ee.api.ejbjar.Car getCar(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.j2ee.api.ejbjar.Car[] getCars(org.netbeans.api.project.Project)
supr java.lang.Object
hfds impl,impl2,implementations

CLSS public final org.netbeans.modules.j2ee.api.ejbjar.Ear
meth public java.lang.String getJ2eePlatformVersion()
 anno 0 java.lang.Deprecated()
meth public org.netbeans.api.j2ee.core.Profile getJ2eeProfile()
meth public org.openide.filesystems.FileObject getDeploymentDescriptor()
meth public static org.netbeans.modules.j2ee.api.ejbjar.Ear getEar(org.openide.filesystems.FileObject)
meth public void addCarModule(org.netbeans.modules.j2ee.api.ejbjar.Car)
meth public void addEjbJarModule(org.netbeans.modules.j2ee.api.ejbjar.EjbJar)
meth public void addWebModule(org.netbeans.modules.web.api.webmodule.WebModule)
supr java.lang.Object
hfds impl,impl2,implementations

CLSS public final org.netbeans.modules.j2ee.api.ejbjar.EjbJar
meth public java.lang.String getJ2eePlatformVersion()
 anno 0 java.lang.Deprecated()
meth public org.netbeans.api.j2ee.core.Profile getJ2eeProfile()
meth public org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata> getMetadataModel()
meth public org.openide.filesystems.FileObject getDeploymentDescriptor()
meth public org.openide.filesystems.FileObject getMetaInf()
meth public org.openide.filesystems.FileObject[] getJavaSources()
meth public static org.netbeans.modules.j2ee.api.ejbjar.EjbJar getEjbJar(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.j2ee.api.ejbjar.EjbJar[] getEjbJars(org.netbeans.api.project.Project)
supr java.lang.Object
hfds impl,impl2,implementations

CLSS public final org.netbeans.modules.j2ee.api.ejbjar.EjbProjectConstants
fld public final static java.lang.String ARTIFACT_TYPE_EJBJAR = "j2ee_archive"
fld public final static java.lang.String ARTIFACT_TYPE_J2EE_MODULE_IN_EAR_ARCHIVE = "j2ee_ear_archive"
fld public final static java.lang.String COMMAND_REDEPLOY = "redeploy"
fld public final static java.lang.String J2EE_13_LEVEL = "1.3"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String J2EE_14_LEVEL = "1.4"
 anno 0 java.lang.Deprecated()
fld public final static java.lang.String JAVA_EE_5_LEVEL = "1.5"
 anno 0 java.lang.Deprecated()
supr java.lang.Object

CLSS public final org.netbeans.modules.j2ee.api.ejbjar.EjbReference
innr public final static !enum EjbRefIType
meth public java.lang.String getComponentName(org.netbeans.modules.j2ee.api.ejbjar.EjbReference$EjbRefIType)
meth public java.lang.String getEjbClass()
meth public java.lang.String getEjbRefType()
meth public java.lang.String getHomeName(org.netbeans.modules.j2ee.api.ejbjar.EjbReference$EjbRefIType)
meth public java.lang.String getLocal()
meth public java.lang.String getLocalHome()
meth public java.lang.String getRemote()
meth public java.lang.String getRemoteHome()
meth public org.netbeans.api.java.source.ClasspathInfo getClasspathInfo()
meth public org.netbeans.modules.j2ee.api.ejbjar.EjbJar getEjbModule()
meth public org.openide.filesystems.FileObject getComponentFO(org.netbeans.modules.j2ee.api.ejbjar.EjbReference$EjbRefIType)
meth public static org.netbeans.modules.j2ee.api.ejbjar.EjbReference create(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.api.ejbjar.EjbJar)
supr java.lang.Object
hfds cpInfo,ejbClass,ejbModule,ejbRefType,local,localHome,remote,remoteHome

CLSS public final static !enum org.netbeans.modules.j2ee.api.ejbjar.EjbReference$EjbRefIType
 outer org.netbeans.modules.j2ee.api.ejbjar.EjbReference
fld public final static org.netbeans.modules.j2ee.api.ejbjar.EjbReference$EjbRefIType LOCAL
fld public final static org.netbeans.modules.j2ee.api.ejbjar.EjbReference$EjbRefIType NO_INTERFACE
fld public final static org.netbeans.modules.j2ee.api.ejbjar.EjbReference$EjbRefIType REMOTE
meth public java.lang.String toString()
meth public static org.netbeans.modules.j2ee.api.ejbjar.EjbReference$EjbRefIType valueOf(java.lang.String)
meth public static org.netbeans.modules.j2ee.api.ejbjar.EjbReference$EjbRefIType[] values()
supr java.lang.Enum<org.netbeans.modules.j2ee.api.ejbjar.EjbReference$EjbRefIType>
hfds name

CLSS public abstract interface org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer
meth public abstract java.lang.String addDestinationRef(org.netbeans.modules.j2ee.api.ejbjar.MessageDestinationReference,org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public abstract java.lang.String addEjbLocalReference(org.netbeans.modules.j2ee.api.ejbjar.EjbReference,org.netbeans.modules.j2ee.api.ejbjar.EjbReference$EjbRefIType,java.lang.String,org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public abstract java.lang.String addEjbReference(org.netbeans.modules.j2ee.api.ejbjar.EjbReference,org.netbeans.modules.j2ee.api.ejbjar.EjbReference$EjbRefIType,java.lang.String,org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public abstract java.lang.String addResourceRef(org.netbeans.modules.j2ee.api.ejbjar.ResourceReference,org.openide.filesystems.FileObject,java.lang.String) throws java.io.IOException
meth public abstract java.lang.String getServiceLocatorName()
meth public abstract void setServiceLocatorName(java.lang.String) throws java.io.IOException

CLSS public org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceSupport
meth public static void populate(org.netbeans.modules.j2ee.api.ejbjar.MessageDestinationReference,java.lang.String,org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef)
meth public static void populate(org.netbeans.modules.j2ee.api.ejbjar.ResourceReference,java.lang.String,org.netbeans.modules.j2ee.dd.api.common.ResourceRef)
supr java.lang.Object

CLSS public final org.netbeans.modules.j2ee.api.ejbjar.MessageDestinationReference
meth public java.lang.String getMessageDestinationLink()
meth public java.lang.String getMessageDestinationRefName()
meth public java.lang.String getMessageDestinationType()
meth public java.lang.String getMessageDestinationUsage()
meth public static org.netbeans.modules.j2ee.api.ejbjar.MessageDestinationReference create(java.lang.String,java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object
hfds messageDestinationLink,messageDestinationRefName,messageDestinationType,messageDestinationUsage

CLSS public final org.netbeans.modules.j2ee.api.ejbjar.ResourceReference
meth public java.lang.String getDefaultDescription()
meth public java.lang.String getResAuth()
meth public java.lang.String getResRefName()
meth public java.lang.String getResSharingScope()
meth public java.lang.String getResType()
meth public static org.netbeans.modules.j2ee.api.ejbjar.ResourceReference create(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)
supr java.lang.Object
hfds defaultDescription,resAuth,resRefName,resSharingScope,resType

CLSS public final org.netbeans.modules.j2ee.spi.ejbjar.CarFactory
meth public static org.netbeans.modules.j2ee.api.ejbjar.Car createCar(org.netbeans.modules.j2ee.spi.ejbjar.CarImplementation)
meth public static org.netbeans.modules.j2ee.api.ejbjar.Car createCar(org.netbeans.modules.j2ee.spi.ejbjar.CarImplementation2)
meth public static org.netbeans.modules.j2ee.api.ejbjar.Ear createEar(org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.j2ee.spi.ejbjar.CarImplementation
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.String getJ2eePlatformVersion()
meth public abstract org.openide.filesystems.FileObject getDeploymentDescriptor()
meth public abstract org.openide.filesystems.FileObject getMetaInf()
meth public abstract org.openide.filesystems.FileObject[] getJavaSources()

CLSS public abstract interface org.netbeans.modules.j2ee.spi.ejbjar.CarImplementation2
meth public abstract org.netbeans.api.j2ee.core.Profile getJ2eeProfile()
meth public abstract org.openide.filesystems.FileObject getDeploymentDescriptor()
meth public abstract org.openide.filesystems.FileObject getMetaInf()
meth public abstract org.openide.filesystems.FileObject[] getJavaSources()

CLSS public abstract interface org.netbeans.modules.j2ee.spi.ejbjar.CarProvider
meth public abstract org.netbeans.modules.j2ee.api.ejbjar.Car findCar(org.openide.filesystems.FileObject)

CLSS public abstract interface org.netbeans.modules.j2ee.spi.ejbjar.CarsInProject
meth public abstract org.netbeans.modules.j2ee.api.ejbjar.Car[] getCars()

CLSS public abstract interface org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.String getJ2eePlatformVersion()
meth public abstract org.openide.filesystems.FileObject getDeploymentDescriptor()
meth public abstract org.openide.filesystems.FileObject getMetaInf()
meth public abstract void addCarModule(org.netbeans.modules.j2ee.api.ejbjar.Car)
meth public abstract void addEjbJarModule(org.netbeans.modules.j2ee.api.ejbjar.EjbJar)
meth public abstract void addWebModule(org.netbeans.modules.web.api.webmodule.WebModule)

CLSS public abstract interface org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation2
meth public abstract org.netbeans.api.j2ee.core.Profile getJ2eeProfile()
meth public abstract org.openide.filesystems.FileObject getDeploymentDescriptor()
meth public abstract org.openide.filesystems.FileObject getMetaInf()
meth public abstract void addCarModule(org.netbeans.modules.j2ee.api.ejbjar.Car)
meth public abstract void addEjbJarModule(org.netbeans.modules.j2ee.api.ejbjar.EjbJar)
meth public abstract void addWebModule(org.netbeans.modules.web.api.webmodule.WebModule)

CLSS public abstract interface org.netbeans.modules.j2ee.spi.ejbjar.EarProvider
meth public abstract org.netbeans.modules.j2ee.api.ejbjar.Ear findEar(org.openide.filesystems.FileObject)

CLSS public final org.netbeans.modules.j2ee.spi.ejbjar.EjbJarFactory
meth public static org.netbeans.modules.j2ee.api.ejbjar.Ear createEar(org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation)
meth public static org.netbeans.modules.j2ee.api.ejbjar.Ear createEar(org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation2)
meth public static org.netbeans.modules.j2ee.api.ejbjar.EjbJar createEjbJar(org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.modules.j2ee.api.ejbjar.EjbJar createEjbJar(org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation2)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation
 anno 0 java.lang.Deprecated()
meth public abstract java.lang.String getJ2eePlatformVersion()
meth public abstract org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata> getMetadataModel()
meth public abstract org.openide.filesystems.FileObject getDeploymentDescriptor()
meth public abstract org.openide.filesystems.FileObject getMetaInf()
meth public abstract org.openide.filesystems.FileObject[] getJavaSources()

CLSS public abstract interface org.netbeans.modules.j2ee.spi.ejbjar.EjbJarImplementation2
meth public abstract org.netbeans.api.j2ee.core.Profile getJ2eeProfile()
meth public abstract org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata> getMetadataModel()
meth public abstract org.openide.filesystems.FileObject getDeploymentDescriptor()
meth public abstract org.openide.filesystems.FileObject getMetaInf()
meth public abstract org.openide.filesystems.FileObject[] getJavaSources()

CLSS public abstract interface org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider
meth public abstract org.netbeans.modules.j2ee.api.ejbjar.EjbJar findEjbJar(org.openide.filesystems.FileObject)

CLSS public abstract interface org.netbeans.modules.j2ee.spi.ejbjar.EjbJarsInProject
meth public abstract org.netbeans.modules.j2ee.api.ejbjar.EjbJar[] getEjbJars()

CLSS public abstract interface org.netbeans.modules.j2ee.spi.ejbjar.EjbNodesFactory
fld public final static java.lang.String CONTAINER_NODE_NAME = "EJBS"
meth public abstract org.openide.nodes.Node createEntityNode(java.lang.String,org.netbeans.modules.j2ee.api.ejbjar.EjbJar,org.netbeans.api.project.Project)
meth public abstract org.openide.nodes.Node createMessageNode(java.lang.String,org.netbeans.modules.j2ee.api.ejbjar.EjbJar,org.netbeans.api.project.Project)
meth public abstract org.openide.nodes.Node createSessionNode(java.lang.String,org.netbeans.modules.j2ee.api.ejbjar.EjbJar,org.netbeans.api.project.Project)

CLSS public final org.netbeans.modules.j2ee.spi.ejbjar.support.EjbEnterpriseReferenceContainerSupport
meth public static org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer createEnterpriseReferenceContainer(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper)
supr java.lang.Object
hcls ERC

CLSS public org.netbeans.modules.j2ee.spi.ejbjar.support.EjbJarSupport
cons public init()
meth public static org.netbeans.modules.j2ee.spi.ejbjar.EjbJarProvider createEjbJarProvider(org.netbeans.api.project.Project,org.netbeans.modules.j2ee.api.ejbjar.EjbJar)
meth public static org.netbeans.modules.j2ee.spi.ejbjar.EjbJarsInProject createEjbJarsInProject(org.netbeans.modules.j2ee.api.ejbjar.EjbJar)
supr java.lang.Object
hcls EjbJarProviderImpl,EjbJarsInProjectImpl

CLSS public final org.netbeans.modules.j2ee.spi.ejbjar.support.EjbReferenceSupport
cons public init()
meth public static org.netbeans.modules.j2ee.api.ejbjar.EjbReference createEjbReference(org.netbeans.modules.j2ee.api.ejbjar.EjbJar,java.lang.String) throws java.io.IOException
supr java.lang.Object

CLSS public final org.netbeans.modules.j2ee.spi.ejbjar.support.J2eeProjectView
fld public final static java.lang.String CONFIG_FILES_VIEW_NAME = "configurationFiles"
meth public static org.netbeans.modules.j2ee.spi.ejbjar.EjbNodesFactory getEjbNodesFactory()
meth public static org.openide.nodes.Node createConfigFilesView(org.openide.filesystems.FileObject)
meth public static org.openide.nodes.Node createEjbsView(org.netbeans.modules.j2ee.api.ejbjar.EjbJar,org.netbeans.api.project.Project)
meth public static org.openide.nodes.Node createServerResourcesNode(org.netbeans.api.project.Project)
supr java.lang.Object
hfds LOGGER,factoryInstance
hcls DocBaseNode,VisibilityQueryDataFilter

