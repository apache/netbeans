#Signature file v4.1
#Version 1.51

CLSS public abstract interface java.awt.datatransfer.Transferable
meth public abstract boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public abstract java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public abstract java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException

CLSS public java.beans.DefaultPersistenceDelegate
cons public init()
cons public init(java.lang.String[])
meth protected boolean mutatesTo(java.lang.Object,java.lang.Object)
meth protected java.beans.Expression instantiate(java.lang.Object,java.beans.Encoder)
meth protected void initialize(java.lang.Class<?>,java.lang.Object,java.lang.Object,java.beans.Encoder)
supr java.beans.PersistenceDelegate

CLSS public abstract java.beans.PersistenceDelegate
cons public init()
meth protected abstract java.beans.Expression instantiate(java.lang.Object,java.beans.Encoder)
meth protected boolean mutatesTo(java.lang.Object,java.lang.Object)
meth protected void initialize(java.lang.Class<?>,java.lang.Object,java.lang.Object,java.beans.Encoder)
meth public void writeObject(java.lang.Object,java.beans.Encoder)
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

CLSS public abstract interface java.util.EventListener

CLSS public org.netbeans.modules.websvc.manager.api.WebServiceDescriptor
cons public init()
cons public init(java.lang.String,java.lang.String,int,java.net.URL,java.io.File,org.netbeans.modules.websvc.jaxwsmodelapi.WSService)
supr org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceProxyDescriptor

CLSS public org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer
cons public init()
fld protected final static java.awt.datatransfer.DataFlavor PORT_NODE_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor METHOD_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor METHOD_NODE_FLAVOR
fld public final static java.awt.datatransfer.DataFlavor PORT_FLAVOR
innr public final static Method
innr public final static MethodTransferable
innr public final static Port
innr public final static PortTransferable
supr java.lang.Object

CLSS public final static org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer$Method
 outer org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer
cons public init(org.netbeans.modules.websvc.manager.model.WebServiceData,com.sun.tools.ws.processor.model.java.JavaMethod,java.lang.String,org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation)
meth public com.sun.tools.ws.processor.model.java.JavaMethod getMethod()
meth public java.lang.String getPortName()
meth public org.netbeans.modules.websvc.api.jaxws.wsdlmodel.WsdlOperation getOperation()
meth public org.netbeans.modules.websvc.manager.model.WebServiceData getWebServiceData()
supr java.lang.Object
hfds method,operation,portName,wsData

CLSS public final static org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer$MethodTransferable
 outer org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer
cons public init(org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer$Method)
intf java.awt.datatransfer.Transferable
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
supr java.lang.Object
hfds SUPPORTED_FLAVORS,transferData

CLSS public final static org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer$Port
 outer org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer
cons public init(org.netbeans.modules.websvc.manager.model.WebServiceData,java.lang.String)
meth public java.lang.String getPortName()
meth public org.netbeans.modules.websvc.manager.model.WebServiceData getWebServiceData()
supr java.lang.Object
hfds portName,wsData

CLSS public final static org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer$PortTransferable
 outer org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer
cons public init(org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer$Method)
cons public init(org.netbeans.modules.websvc.manager.api.WebServiceMetaDataTransfer$Port)
intf java.awt.datatransfer.Transferable
meth public boolean isDataFlavorSupported(java.awt.datatransfer.DataFlavor)
meth public java.awt.datatransfer.DataFlavor[] getTransferDataFlavors()
meth public java.lang.Object getTransferData(java.awt.datatransfer.DataFlavor) throws java.awt.datatransfer.UnsupportedFlavorException,java.io.IOException
supr java.lang.Object
hfds SUPPORTED_FLAVORS,transferData

CLSS public org.netbeans.modules.websvc.manager.model.WebServiceData
cons public init()
cons public init(java.lang.String,java.lang.String)
cons public init(java.lang.String,java.lang.String,java.lang.String)
cons public init(org.netbeans.modules.websvc.jaxwsmodelapi.WSService,java.lang.String,java.lang.String,java.lang.String)
cons public init(org.netbeans.modules.websvc.manager.model.WebServiceData)
fld public final static java.lang.String JAX_RPC = "jaxrpc"
fld public final static java.lang.String JAX_WS = "jaxws"
innr public final static !enum State
intf org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData
meth public boolean isCompiled()
 anno 0 java.lang.Deprecated()
meth public boolean isJaxRpcEnabled()
meth public boolean isJaxWsEnabled()
meth public boolean isReady()
meth public boolean isResolved()
meth public java.lang.String getCatalog()
meth public java.lang.String getEffectivePackageName()
meth public java.lang.String getGroupId()
meth public java.lang.String getId()
meth public java.lang.String getJaxRpcDescriptorPath()
meth public java.lang.String getJaxWsDescriptorPath()
meth public java.lang.String getName()
meth public java.lang.String getOriginalWsdl()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getOriginalWsdlUrl()
meth public java.lang.String getPackageName()
meth public java.lang.String getStateName()
meth public java.lang.String getURL()
 anno 0 java.lang.Deprecated()
meth public java.lang.String getWsdlFile()
meth public org.netbeans.modules.websvc.jaxwsmodelapi.WSService getWsdlService()
meth public org.netbeans.modules.websvc.manager.api.WebServiceDescriptor getJaxRpcDescriptor()
meth public org.netbeans.modules.websvc.manager.api.WebServiceDescriptor getJaxWsDescriptor()
meth public org.netbeans.modules.websvc.manager.model.WebServiceData$State getState()
meth public org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData$Status getStatus()
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addWebServiceDataListener(org.netbeans.modules.websvc.manager.model.WebServiceDataListener)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removeWebServiceDataListener(org.netbeans.modules.websvc.manager.model.WebServiceDataListener)
meth public void reset()
meth public void setCatalog(java.lang.String)
meth public void setCompiled(boolean)
 anno 0 java.lang.Deprecated()
meth public void setGroupId(java.lang.String)
meth public void setId(java.lang.String)
meth public void setJaxRpcDescriptor(org.netbeans.modules.websvc.manager.api.WebServiceDescriptor)
meth public void setJaxRpcDescriptorPath(java.lang.String)
meth public void setJaxRpcEnabled(boolean)
meth public void setJaxWsDescriptor(org.netbeans.modules.websvc.manager.api.WebServiceDescriptor)
meth public void setJaxWsDescriptorPath(java.lang.String)
meth public void setJaxWsEnabled(boolean)
meth public void setName(java.lang.String)
meth public void setOriginalWsdl(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setOriginalWsdlUrl(java.lang.String)
meth public void setPackageName(java.lang.String)
meth public void setResolved(boolean)
meth public void setState(org.netbeans.modules.websvc.manager.model.WebServiceData$State)
meth public void setStateName(java.lang.String)
meth public void setURL(java.lang.String)
 anno 0 java.lang.Deprecated()
meth public void setWsdlFile(java.lang.String)
meth public void setWsdlService(org.netbeans.modules.websvc.jaxwsmodelapi.WSService)
supr java.lang.Object
hfds catalog,compiled,groupId,jaxRpcDescriptor,jaxRpcDescriptorPath,jaxRpcEnabled,jaxWsDescriptor,jaxWsDescriptorPath,jaxWsEnabled,listeners,originalWsdlUrl,packageName,propertyListeners,resolved,websvcId,wsName,wsdlFile,wsdlService,wsdlState

CLSS public final static !enum org.netbeans.modules.websvc.manager.model.WebServiceData$State
 outer org.netbeans.modules.websvc.manager.model.WebServiceData
fld public final static org.netbeans.modules.websvc.manager.model.WebServiceData$State WSDL_RETRIEVED
fld public final static org.netbeans.modules.websvc.manager.model.WebServiceData$State WSDL_RETRIEVING
fld public final static org.netbeans.modules.websvc.manager.model.WebServiceData$State WSDL_SERVICE_COMPILED
fld public final static org.netbeans.modules.websvc.manager.model.WebServiceData$State WSDL_SERVICE_COMPILE_FAILED
fld public final static org.netbeans.modules.websvc.manager.model.WebServiceData$State WSDL_SERVICE_COMPILING
fld public final static org.netbeans.modules.websvc.manager.model.WebServiceData$State WSDL_UNRETRIEVED
meth public static org.netbeans.modules.websvc.manager.model.WebServiceData$State valueOf(java.lang.String)
meth public static org.netbeans.modules.websvc.manager.model.WebServiceData$State[] values()
supr java.lang.Enum<org.netbeans.modules.websvc.manager.model.WebServiceData$State>

CLSS public org.netbeans.modules.websvc.manager.model.WebServiceDataEvent
cons public init(org.netbeans.modules.websvc.manager.model.WebServiceData)
meth public java.lang.String getId()
supr java.lang.Object
hfds wsId

CLSS public abstract interface org.netbeans.modules.websvc.manager.model.WebServiceDataListener
intf java.util.EventListener
meth public abstract void webServiceCompiled(org.netbeans.modules.websvc.manager.model.WebServiceDataEvent)

CLSS public org.netbeans.modules.websvc.manager.model.WebServiceDataPersistenceDelegate
cons public init()
meth public void writeObject(java.lang.Object,java.beans.Encoder)
supr java.beans.DefaultPersistenceDelegate

CLSS public org.netbeans.modules.websvc.manager.model.WebServiceGroup
cons public init()
cons public init(java.lang.String)
meth public boolean equals(java.lang.Object)
meth public boolean isUserDefined()
meth public int hashCode()
meth public java.lang.String getId()
meth public java.lang.String getName()
meth public java.util.Set<java.lang.String> getWebServiceIds()
meth public void add(java.lang.String)
meth public void add(java.lang.String,boolean)
meth public void addWebServiceGroupListener(org.netbeans.modules.websvc.manager.model.WebServiceGroupListener)
meth public void modify(java.lang.String)
meth public void remove(java.lang.String)
meth public void remove(java.lang.String,boolean)
meth public void removeWebServiceGroupListener(org.netbeans.modules.websvc.manager.model.WebServiceGroupListener)
meth public void setId(java.lang.String)
meth public void setName(java.lang.String)
meth public void setUserDefined(boolean)
meth public void setWebServiceIds(java.util.Set)
supr java.lang.Object
hfds groupId,groupName,listeners,userDefined,webserviceIds

CLSS public org.netbeans.modules.websvc.manager.model.WebServiceGroupEvent
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getGroupId()
meth public java.lang.String getWebServiceId()
supr java.lang.Object
hfds groupId,websvcId

CLSS public abstract interface org.netbeans.modules.websvc.manager.model.WebServiceGroupListener
intf java.util.EventListener
meth public abstract void webServiceAdded(org.netbeans.modules.websvc.manager.model.WebServiceGroupEvent)
meth public abstract void webServiceRemoved(org.netbeans.modules.websvc.manager.model.WebServiceGroupEvent)

CLSS public org.netbeans.modules.websvc.manager.model.WebServiceListModel
fld public boolean isDirty
fld public final static java.lang.String DEFAULT_GROUP = "default"
meth public boolean isDirty()
meth public boolean isDisplayNameUnique(java.lang.String)
meth public boolean isInitialized()
meth public boolean webServiceExists(org.netbeans.modules.websvc.manager.model.WebServiceData)
meth public java.lang.String getUniqueDisplayName(java.lang.String)
meth public java.lang.String getUniqueWebServiceGroupId()
meth public java.lang.String getUniqueWebServiceId()
meth public java.util.List<java.lang.String> getPartnerServices()
meth public java.util.List<org.netbeans.modules.websvc.manager.model.WebServiceData> getWebServiceSet()
meth public java.util.List<org.netbeans.modules.websvc.manager.model.WebServiceGroup> getWebServiceGroupSet()
meth public org.netbeans.modules.websvc.manager.model.WebServiceData findWebServiceData(java.lang.String,java.lang.String)
meth public org.netbeans.modules.websvc.manager.model.WebServiceData findWebServiceData(java.lang.String,java.lang.String,boolean)
meth public org.netbeans.modules.websvc.manager.model.WebServiceData getWebService(java.lang.String)
meth public org.netbeans.modules.websvc.manager.model.WebServiceData getWebServiceData(java.lang.String,java.lang.String)
meth public org.netbeans.modules.websvc.manager.model.WebServiceData getWebServiceData(java.lang.String,java.lang.String,boolean)
meth public org.netbeans.modules.websvc.manager.model.WebServiceGroup getWebServiceGroup(java.lang.String)
meth public static org.netbeans.modules.websvc.manager.model.WebServiceListModel getInstance()
meth public static void resetInstance()
meth public void addDefaultGroupListener(org.netbeans.modules.websvc.manager.model.WebServiceGroupListener)
meth public void addWebService(org.netbeans.modules.websvc.manager.model.WebServiceData)
meth public void addWebServiceGroup(org.netbeans.modules.websvc.manager.model.WebServiceGroup)
meth public void addWebServiceListModelListener(org.netbeans.modules.websvc.manager.model.WebServiceListModelListener)
meth public void removeWebService(java.lang.String)
meth public void removeWebServiceGroup(java.lang.String)
meth public void removeWebServiceListModelListener(org.netbeans.modules.websvc.manager.model.WebServiceListModelListener)
meth public void setDirty(boolean)
supr java.lang.Object
hfds defaultGroupListeners,initialized,listeners,partnerServiceListener,partnerServices,serviceGroupRandom,serviceRandom,uniqueDisplayNames,webServiceGroups,webServices,websvcNodeModel
hcls RestFolderListener

CLSS public org.netbeans.modules.websvc.manager.model.WebServiceListModelEvent
cons public init(java.lang.String)
meth public java.lang.String getWebServiceGroupId()
meth public void setWebServiceGroupId(java.lang.String)
supr java.lang.Object
hfds websvcGroupId

CLSS public abstract interface org.netbeans.modules.websvc.manager.model.WebServiceListModelListener
intf java.util.EventListener
meth public abstract void webServiceGroupAdded(org.netbeans.modules.websvc.manager.model.WebServiceListModelEvent)
meth public abstract void webServiceGroupRemoved(org.netbeans.modules.websvc.manager.model.WebServiceListModelEvent)

CLSS public abstract interface org.netbeans.modules.websvc.manager.spi.WebServiceManagerExt
meth public abstract boolean wsServiceAddedExt(org.netbeans.modules.websvc.manager.api.WebServiceDescriptor)
meth public abstract boolean wsServiceRemovedExt(org.netbeans.modules.websvc.manager.api.WebServiceDescriptor)

CLSS public org.netbeans.modules.websvc.manager.util.ManagerUtil
cons public init()
fld public final static int BUFFER_SIZE = 4096
fld public final static java.lang.String LOCALIZING_BUNDLE = "SystemFileSystem.localizingBundle"
fld public final static java.lang.String PATH_IN_WAR_LIB = "WEB-INF/lib"
fld public final static java.lang.String PATH_LIBRARIES = "lib"
fld public final static java.lang.String WSDL_FILE_EXTENSION = "wsdl"
fld public final static java.lang.String xsdNamespace = "xsd"
meth public static boolean addLibraryReferences(org.netbeans.api.project.Project,org.netbeans.api.project.libraries.Library[]) throws java.io.IOException
meth public static boolean addLibraryReferences(org.netbeans.api.project.Project,org.netbeans.api.project.libraries.Library[],java.lang.String) throws java.io.IOException
meth public static boolean addRootReferences(org.netbeans.api.project.Project,java.net.URL[]) throws java.io.IOException
meth public static boolean hasLibraryReference(org.netbeans.api.project.Project,org.netbeans.api.project.libraries.Library,java.lang.String)
meth public static boolean hasOutput(com.sun.tools.ws.processor.model.java.JavaMethod)
meth public static boolean hasRootReference(org.netbeans.api.project.Project,java.net.URL)
meth public static boolean hasRootReference(org.netbeans.api.project.Project,java.net.URL,java.lang.String)
meth public static boolean isAcronyn(java.lang.String)
meth public static boolean isJAXRPCAvailable()
meth public static boolean isJavaEE5Project(org.netbeans.api.project.Project)
meth public static boolean isJavaPrimitive(java.lang.String)
meth public static boolean isPrimitiveType(java.lang.String)
meth public static boolean isValidIdentifier(java.lang.String)
meth public static boolean isValidPackageName(java.lang.String)
meth public static boolean removeLibraryReferences(org.netbeans.api.project.Project,org.netbeans.api.project.libraries.Library[]) throws java.io.IOException
meth public static int getOutputHolderIndex(com.sun.tools.ws.processor.model.java.JavaMethod)
meth public static java.lang.String changeString(java.lang.String,java.lang.String,java.lang.String)
meth public static java.lang.String decapitalize(java.lang.String)
meth public static java.lang.String getFileName(java.lang.String)
meth public static java.lang.String getJ2eePlatformVersion(org.netbeans.api.project.Project)
meth public static java.lang.String getLocalizedName(org.openide.filesystems.FileObject)
meth public static java.lang.String getParameterType(com.sun.tools.ws.processor.model.java.JavaParameter)
meth public static java.lang.String getProperPortName(java.lang.String)
meth public static java.lang.String getWrapperForPrimitive(java.lang.String)
meth public static java.lang.String makeValidJavaBeanName(java.lang.String)
meth public static java.lang.String removeNamespace(java.lang.String)
meth public static java.lang.String typeToString(java.lang.reflect.Type)
meth public static java.lang.String upperCaseFirstChar(java.lang.String)
meth public static java.lang.reflect.Method getPropertyGetter(java.lang.String,java.lang.String,java.lang.ClassLoader)
meth public static java.util.Collection<? extends org.netbeans.modules.websvc.manager.spi.WebServiceManagerExt> getExtensions()
meth public static java.util.List<java.net.URL> buildClasspath(java.io.File,boolean) throws java.io.IOException
meth public static org.netbeans.api.project.libraries.Library getWebServiceSupportLibDef(boolean)
meth public static org.openide.filesystems.FileObject getProjectLibraryDirectory(org.netbeans.api.project.Project) throws java.io.IOException
meth public static org.openide.filesystems.FileObject getSourceRoot(org.netbeans.api.project.Project)
supr java.lang.Object
hfds PRIMITIVE_TYPES,PRIMITIVE_WRAPPER_CLASSES,extensionsResult

CLSS public org.netbeans.modules.websvc.manager.util.WebServiceLibReferenceHelper
cons public init()
meth public static java.util.List<java.lang.String> getDefaultJaxWsClientJars(org.netbeans.modules.websvc.manager.model.WebServiceData)
meth public static void addArchiveRefsToProject(org.netbeans.api.project.Project,java.util.List<java.lang.String>)
meth public static void addDefaultJaxWsClientJar(org.netbeans.api.project.Project,org.netbeans.modules.websvc.manager.model.WebServiceData)
meth public static void addLibRefsToProject(org.netbeans.api.project.Project,java.util.List<org.netbeans.api.project.libraries.Library>)
meth public static void addLibRefsToProject(org.netbeans.api.project.Project,java.util.List<org.netbeans.api.project.libraries.Library>,java.lang.String)
supr java.lang.Object
hfds DESIGN_RUNTIME_LIBRARY_ADD,WEBSERVICE_CLIENTS_SUB_DIR
hcls AddArchiveReferences,AddLibrary,AddLibraryFromRole,AddLibraryToProject

CLSS public abstract interface org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData
fld public final static java.lang.String PROP_STATE = "state"
innr public final static !enum Status
meth public abstract boolean isReady()
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getOriginalWsdlUrl()
meth public abstract java.lang.String getWsdlFile()
meth public abstract org.netbeans.modules.websvc.jaxwsmodelapi.WSService getWsdlService()
meth public abstract org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlData$Status getStatus()
meth public abstract org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceProxyDescriptor getJaxRpcDescriptor()
meth public abstract org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceProxyDescriptor getJaxWsDescriptor()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)

CLSS public org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceProxyDescriptor
cons public init()
cons public init(java.lang.String,java.lang.String,int,java.net.URL,java.io.File,org.netbeans.modules.websvc.jaxwsmodelapi.WSService)
fld public final static int JAX_RPC_TYPE = 0
fld public final static int JAX_WS_TYPE = 1
fld public final static java.lang.String WEBSVC_HOME
innr public static JarEntry
meth public int getWsType()
meth public java.io.File getXmlDescriptorFile()
meth public java.lang.String getName()
meth public java.lang.String getPackageName()
meth public java.lang.String getWsdl()
meth public java.lang.String getXmlDescriptor()
meth public java.net.URL getWsdlUrl()
meth public java.util.List<org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceProxyDescriptor$JarEntry> getJars()
meth public java.util.Map<java.lang.String,java.lang.Object> getConsumerData()
meth public org.netbeans.modules.websvc.jaxwsmodelapi.WSService getModel()
meth public void addConsumerData(java.lang.String,java.lang.Object)
meth public void addJar(java.lang.String,java.lang.String)
meth public void removeConsumerData(java.lang.String)
meth public void removeJar(java.lang.String,java.lang.String)
meth public void setConsumerData(java.util.Map<java.lang.String,java.lang.Object>)
meth public void setJars(java.util.List<org.netbeans.modules.websvc.saas.spi.websvcmgr.WsdlServiceProxyDescriptor$JarEntry>)
meth public void setModel(org.netbeans.modules.websvc.jaxwsmodelapi.WSService)
meth public void setName(java.lang.String)
meth public void setPackageName(java.lang.String)
meth public void setWsType(int)
meth public void setWsdl(java.lang.String)
meth public void setXmlDescriptor(java.lang.String)
supr java.lang.Object
hfds consumerData,jars,model,name,packageName,wsType,wsdl,xmlDescriptor

