#Signature file v4.1
#Version 1.33

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

CLSS public abstract interface org.netbeans.modules.javaee.resources.api.JmsDestination
intf org.netbeans.modules.javaee.resources.api.JndiResource
meth public abstract java.lang.String getClassName()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getDestinationName()
meth public abstract java.lang.String getInterfaceName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getResourceAdapterName()
meth public abstract java.lang.String[] getProperties()

CLSS public abstract interface org.netbeans.modules.javaee.resources.api.JmsDestinations
intf org.netbeans.modules.javaee.resources.api.JndiResource
meth public abstract java.util.List<org.netbeans.modules.javaee.resources.api.JmsDestination> getJmsDestinations()

CLSS public abstract interface org.netbeans.modules.javaee.resources.api.JndiResource
innr public final static !enum Type
meth public abstract org.netbeans.modules.javaee.resources.api.model.Location getLocation()

CLSS public final static !enum org.netbeans.modules.javaee.resources.api.JndiResource$Type
 outer org.netbeans.modules.javaee.resources.api.JndiResource
fld public final static org.netbeans.modules.javaee.resources.api.JndiResource$Type ADMINISTRED_OBJECT
fld public final static org.netbeans.modules.javaee.resources.api.JndiResource$Type CONNECTOR_RESOURCE
fld public final static org.netbeans.modules.javaee.resources.api.JndiResource$Type DATA_SOURCE
fld public final static org.netbeans.modules.javaee.resources.api.JndiResource$Type JMS_CONNECTION_FACTORY
fld public final static org.netbeans.modules.javaee.resources.api.JndiResource$Type JMS_DESTINATION
fld public final static org.netbeans.modules.javaee.resources.api.JndiResource$Type MAIL_SESSION
meth public static org.netbeans.modules.javaee.resources.api.JndiResource$Type valueOf(java.lang.String)
meth public static org.netbeans.modules.javaee.resources.api.JndiResource$Type[] values()
supr java.lang.Enum<org.netbeans.modules.javaee.resources.api.JndiResource$Type>

CLSS public org.netbeans.modules.javaee.resources.api.JndiResourcesDefinition
fld public final static java.lang.String ANN_ADMINISTRED_OBJECT = "javax.resource.AdministeredObjectDefinition"
fld public final static java.lang.String ANN_CONNECTION_RESOURCE = "javax.resource.ConnectorResourceDefinition"
fld public final static java.lang.String ANN_DATA_SOURCE = "javax.annotation.sql.DataSourceDefinition"
fld public final static java.lang.String ANN_JMS_CONNECTION_FACTORY = "javax.jms.JMSConnectionFactoryDefinition"
fld public final static java.lang.String ANN_JMS_DESTINATION = "javax.jms.JMSDestinationDefinition"
fld public final static java.lang.String ANN_JMS_DESTINATIONS = "javax.jms.JMSDestinationDefinitions"
fld public final static java.lang.String ANN_JMS_DESTINATIONS_JAKARTA = "jakarta.jms.JMSDestinationDefinitions"
fld public final static java.lang.String ANN_JMS_DESTINATION_JAKARTA = "jakarta.jms.JMSDestinationDefinition"
supr java.lang.Object

CLSS public abstract org.netbeans.modules.javaee.resources.api.model.JndiResourcesAbstractModel
cons protected init(org.netbeans.modules.javaee.resources.api.model.JndiResourcesModelUnit)
meth protected org.netbeans.modules.javaee.resources.api.model.JndiResourcesModel getModel()
meth protected org.netbeans.modules.javaee.resources.spi.model.JndiResourcesModelProvider getProvider()
meth public org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper getHelper()
supr java.lang.Object
hfds helper,model,provider

CLSS public final org.netbeans.modules.javaee.resources.api.model.JndiResourcesModel
meth public java.util.List<org.netbeans.modules.javaee.resources.api.JmsDestination> getJmsDestinations()
meth public java.util.List<org.netbeans.modules.javaee.resources.api.JndiResource> getResources()
supr java.lang.Object
hfds modelImplementation

CLSS public org.netbeans.modules.javaee.resources.api.model.JndiResourcesModelFactory
meth public static org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.javaee.resources.api.model.JndiResourcesModel> createMetaModel(org.netbeans.modules.javaee.resources.api.model.JndiResourcesModelUnit)
supr java.lang.Object
hfds LOGGER

CLSS public final org.netbeans.modules.javaee.resources.api.model.JndiResourcesModelSupport
fld public final static java.util.Map<org.netbeans.api.project.Project,java.lang.ref.WeakReference<org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.javaee.resources.api.model.JndiResourcesModel>>> MODELS
meth public static org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.javaee.resources.api.model.JndiResourcesModel> getModel(org.netbeans.api.project.Project)
supr java.lang.Object
hfds LOGGER

CLSS public final org.netbeans.modules.javaee.resources.api.model.JndiResourcesModelUnit
meth public boolean equals(java.lang.Object)
meth public int hashCode()
meth public org.netbeans.api.java.classpath.ClassPath getBootPath()
meth public org.netbeans.api.java.classpath.ClassPath getCompilePath()
meth public org.netbeans.api.java.classpath.ClassPath getSourcePath()
meth public org.netbeans.api.java.source.ClasspathInfo getClassPathInfo()
meth public static org.netbeans.modules.javaee.resources.api.model.JndiResourcesModelUnit create(org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.java.classpath.ClassPath)
supr java.lang.Object
hfds bootPath,classPathInfo,compilePath,sourcePath

CLSS public abstract interface org.netbeans.modules.javaee.resources.api.model.Location
meth public abstract int getOffset()
meth public abstract org.openide.filesystems.FileObject getFile()

CLSS public abstract interface org.netbeans.modules.javaee.resources.api.model.Refreshable
meth public abstract boolean refresh(javax.lang.model.element.TypeElement)

CLSS public abstract interface org.netbeans.modules.javaee.resources.spi.model.JndiResourcesModelProvider
meth public abstract java.util.List<org.netbeans.modules.javaee.resources.api.JmsDestination> getJmsDestinations()
meth public abstract java.util.List<org.netbeans.modules.javaee.resources.api.JndiResource> getResources()

CLSS public abstract interface org.netbeans.modules.javaee.resources.spi.model.JndiResourcesModelProviderFactory
meth public abstract org.netbeans.modules.javaee.resources.spi.model.JndiResourcesModelProvider createProvider(org.netbeans.modules.javaee.resources.api.model.JndiResourcesAbstractModel)

