#Signature file v4.1
#Version 1.55

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

CLSS public org.netbeans.modules.websvc.api.client.ClientStubDescriptor
cons public init(java.lang.String,java.lang.String)
fld public final static java.lang.String JAXRPC_CLIENT_STUB = "jaxrpc_static_client"
fld public final static java.lang.String JSR109_CLIENT_STUB = "jsr-109_client"
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String toString()
supr java.lang.Object
hfds displayName,name

CLSS public org.netbeans.modules.websvc.api.client.WebServicesClientConstants
cons public init()
fld public final static java.lang.String CLIENT_SOURCE_URL = "client-source-url"
fld public final static java.lang.String CONFIG_PROP_SUFFIX = ".config.name"
fld public final static java.lang.String J2EE_PLATFORM_WSCOMPILE_CLASSPATH = "j2ee.platform.wscompile.classpath"
fld public final static java.lang.String J2EE_PLATFORM_WSIMPORT_CLASSPATH = "j2ee.platform.wsimport.classpath"
fld public final static java.lang.String MAPPING_FILE_SUFFIX = "-mapping.xml"
fld public final static java.lang.String MAPPING_PROP_SUFFIX = ".mapping"
fld public final static java.lang.String WEBSERVICES_DD = "webservices"
fld public final static java.lang.String WEBSVC_GENERATED_DIR = "websvc.generated.dir"
fld public final static java.lang.String WEB_SERVICE = "web-service"
fld public final static java.lang.String WEB_SERVICES = "web-services"
fld public final static java.lang.String WEB_SERVICE_CLIENT = "web-service-client"
fld public final static java.lang.String WEB_SERVICE_CLIENTS = "web-service-clients"
fld public final static java.lang.String WEB_SERVICE_CLIENT_NAME = "web-service-client-name"
fld public final static java.lang.String WEB_SERVICE_FROM_WSDL = "from-wsdl"
fld public final static java.lang.String WEB_SERVICE_NAME = "web-service-name"
fld public final static java.lang.String WEB_SERVICE_STUB_TYPE = "web-service-stub-type"
fld public final static java.lang.String WSCOMPILE = "wscompile"
fld public final static java.lang.String WSCOMPILE_CLASSPATH = "wscompile.classpath"
fld public final static java.lang.String WSCOMPILE_TOOLS_CLASSPATH = "wscompile.tools.classpath"
fld public final static java.lang.String WSDL_FOLDER = "wsdl"
fld public final static java.lang.String WebServiceServlet_PREFIX = "WSServlet_"
fld public final static java.lang.String[] WSCOMPILE_JARS
supr java.lang.Object

CLSS public final org.netbeans.modules.websvc.api.client.WebServicesClientSupport
fld public final static java.lang.String WSCLIENTUPTODATE_CLASSPATH = "wsclientuptodate.classpath"
meth public java.lang.String getServiceRefName(java.lang.String)
meth public java.lang.String getWsdlSource(java.lang.String)
meth public java.util.List getServiceClients()
meth public java.util.List<org.netbeans.modules.websvc.api.client.ClientStubDescriptor> getStubDescriptors()
meth public org.openide.filesystems.FileObject getDeploymentDescriptor()
meth public org.openide.filesystems.FileObject getWsdlFolder()
meth public org.openide.filesystems.FileObject getWsdlFolder(boolean) throws java.io.IOException
meth public static boolean isBroken(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.websvc.api.client.WebServicesClientSupport getWebServicesClientSupport(org.openide.filesystems.FileObject)
meth public static void showBrokenAlert(org.netbeans.api.project.Project)
meth public void addServiceClient(java.lang.String,java.lang.String,java.lang.String,org.openide.filesystems.FileObject,org.netbeans.modules.websvc.api.client.ClientStubDescriptor)
meth public void addServiceClient(java.lang.String,java.lang.String,java.lang.String,org.openide.filesystems.FileObject,org.netbeans.modules.websvc.api.client.ClientStubDescriptor,java.lang.String[])
meth public void addServiceClientReference(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public void removeServiceClient(java.lang.String)
meth public void setProxyJVMOptions(java.lang.String,java.lang.String)
meth public void setWsdlSource(java.lang.String,java.lang.String)
supr java.lang.Object
hfds impl,implementations

CLSS public final org.netbeans.modules.websvc.api.client.WebServicesClientView
meth public org.openide.nodes.Node createWebServiceClientView(org.netbeans.api.project.Project)
meth public org.openide.nodes.Node createWebServiceClientView(org.netbeans.api.project.SourceGroup)
meth public org.openide.nodes.Node createWebServiceClientView(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.websvc.api.client.WebServicesClientView getWebServicesClientView(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds impl,implementations

CLSS public abstract interface org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport
fld public final static java.lang.String PROP_FEATURES_CHANGED = "featuresChanged"
fld public final static java.lang.String PROP_OPTIONS_CHANGED = "optionsChanged"
innr public abstract interface static Panel
innr public final static FeatureDescriptor
innr public final static OptionDescriptor
innr public final static ServiceSettings
meth public abstract org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport$Panel getWsCompileSupport()

CLSS public final static org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport$FeatureDescriptor
 outer org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getFeatures()
meth public java.lang.String getServiceName()
supr java.lang.Object
hfds features,serviceName

CLSS public final static org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport$OptionDescriptor
 outer org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport
cons public init(java.lang.String,boolean[])
meth public boolean[] getOptions()
meth public java.lang.String getServiceName()
supr java.lang.Object
hfds options,serviceName

CLSS public abstract interface static org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport$Panel
 outer org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport
meth public abstract javax.swing.JPanel getComponent()
meth public abstract void initValues(java.util.List)
meth public abstract void validatePanel() throws org.openide.WizardValidationException

CLSS public final static org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport$ServiceSettings
 outer org.netbeans.modules.websvc.api.client.WsCompileClientEditorSupport
cons public init(java.lang.String,org.netbeans.modules.websvc.api.client.ClientStubDescriptor,boolean[],java.lang.String,java.util.List,java.util.List)
meth public boolean[] getOptions()
meth public java.lang.String getCurrentFeatures()
meth public java.lang.String getNewFeatures()
meth public java.lang.String getServiceName()
meth public java.lang.String toString()
meth public java.util.List<java.lang.String> getAvailableFeatures()
meth public java.util.List<java.lang.String> getImportantFeatures()
meth public org.netbeans.modules.websvc.api.client.ClientStubDescriptor getClientStubDescriptor()
meth public void setNewFeatures(java.lang.String)
meth public void setOptions(boolean[])
supr java.lang.Object
hfds availableFeatures,currentFeatures,importantFeatures,name,newFeatures,options,stubType

CLSS public final org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport
meth public java.lang.String addServiceClient(java.lang.String,java.lang.String,java.lang.String,boolean)
meth public java.lang.String getServiceRefName(org.openide.nodes.Node)
meth public java.net.URL getCatalog()
meth public java.util.List getServiceClients()
meth public org.netbeans.spi.project.support.ant.AntProjectHelper getAntProjectHelper()
meth public org.openide.filesystems.FileObject getBindingsFolderForClient(java.lang.String,boolean)
meth public org.openide.filesystems.FileObject getLocalWsdlFolderForClient(java.lang.String,boolean)
meth public org.openide.filesystems.FileObject getWsdlFolder(boolean) throws java.io.IOException
meth public static org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport getJaxWsClientSupport(org.openide.filesystems.FileObject)
meth public void removeServiceClient(java.lang.String)
supr java.lang.Object
hfds impl,implementations

CLSS public final org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientView
meth public org.openide.nodes.Node createJAXWSClientView(org.netbeans.api.project.Project)
meth public static org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientView getJAXWSClientView()
supr java.lang.Object
hfds impl,implementations

CLSS public final org.netbeans.modules.websvc.spi.client.WebServicesClientSupportFactory
meth public static org.netbeans.modules.websvc.api.client.WebServicesClientSupport createWebServicesClientSupport(org.netbeans.modules.websvc.spi.client.WebServicesClientSupportImpl)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.websvc.spi.client.WebServicesClientSupportImpl
meth public abstract java.lang.String getServiceRefName(java.lang.String)
meth public abstract java.lang.String getWsdlSource(java.lang.String)
meth public abstract java.util.List getServiceClients()
meth public abstract java.util.List<org.netbeans.modules.websvc.api.client.ClientStubDescriptor> getStubDescriptors()
meth public abstract org.openide.filesystems.FileObject getDeploymentDescriptor()
meth public abstract org.openide.filesystems.FileObject getWsdlFolder(boolean) throws java.io.IOException
meth public abstract void addServiceClient(java.lang.String,java.lang.String,java.lang.String,org.openide.filesystems.FileObject,org.netbeans.modules.websvc.api.client.ClientStubDescriptor)
meth public abstract void addServiceClient(java.lang.String,java.lang.String,java.lang.String,org.openide.filesystems.FileObject,org.netbeans.modules.websvc.api.client.ClientStubDescriptor,java.lang.String[])
meth public abstract void addServiceClientReference(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String[])
meth public abstract void removeServiceClient(java.lang.String)
meth public abstract void setProxyJVMOptions(java.lang.String,java.lang.String)
meth public abstract void setWsdlSource(java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.modules.websvc.spi.client.WebServicesClientSupportProvider
meth public abstract org.netbeans.modules.websvc.api.client.WebServicesClientSupport findWebServicesClientSupport(org.openide.filesystems.FileObject)
meth public abstract org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport findJAXWSClientSupport(org.openide.filesystems.FileObject)

CLSS public final org.netbeans.modules.websvc.spi.client.WebServicesClientViewFactory
meth public static org.netbeans.modules.websvc.api.client.WebServicesClientView createWebServicesClientView(org.netbeans.modules.websvc.spi.client.WebServicesClientViewImpl)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.websvc.spi.client.WebServicesClientViewImpl
meth public abstract org.openide.nodes.Node createWebServiceClientView(org.netbeans.api.project.Project)
meth public abstract org.openide.nodes.Node createWebServiceClientView(org.netbeans.api.project.SourceGroup)
meth public abstract org.openide.nodes.Node createWebServiceClientView(org.openide.filesystems.FileObject)

CLSS public abstract interface org.netbeans.modules.websvc.spi.client.WebServicesClientViewProvider
meth public abstract org.netbeans.modules.websvc.api.client.WebServicesClientView findWebServicesClientView(org.openide.filesystems.FileObject)

CLSS public final org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportFactory
meth public static org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport createJAXWSClientSupport(org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportImpl)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportImpl
fld public final static java.lang.String CATALOG_FILE = "catalog.xml"
fld public final static java.lang.String CLIENTS_LOCAL_FOLDER = "web-service-references"
fld public final static java.lang.String XML_RESOURCES_FOLDER = "xml-resources"
meth public abstract java.lang.String addServiceClient(java.lang.String,java.lang.String,java.lang.String,boolean)
meth public abstract java.lang.String getServiceRefName(org.openide.nodes.Node)
meth public abstract java.net.URL getCatalog()
meth public abstract java.util.List<org.netbeans.modules.websvc.api.jaxws.project.config.Client> getServiceClients()
meth public abstract org.netbeans.spi.project.support.ant.AntProjectHelper getAntProjectHelper()
meth public abstract org.openide.filesystems.FileObject getBindingsFolderForClient(java.lang.String,boolean)
meth public abstract org.openide.filesystems.FileObject getLocalWsdlFolderForClient(java.lang.String,boolean)
meth public abstract org.openide.filesystems.FileObject getWsdlFolder(boolean) throws java.io.IOException
meth public abstract void removeServiceClient(java.lang.String)

CLSS public final org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientViewFactory
meth public static org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientView createJAXWSClientView(org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientViewImpl)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientViewImpl
meth public abstract org.openide.nodes.Node createJAXWSClientView(org.netbeans.api.project.Project)

CLSS public abstract interface org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientViewProvider
meth public abstract org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientView findJAXWSClientView()

CLSS public abstract org.netbeans.modules.websvc.spi.jaxws.client.ProjectJAXWSClientSupport
cons public init(org.netbeans.api.project.Project,org.netbeans.spi.project.support.ant.AntProjectHelper)
fld protected final static java.lang.String JAKARTA_EE_VERSION_10 = "jakarta-ee-version-10"
fld protected final static java.lang.String JAKARTA_EE_VERSION_8 = "jakarta-ee-version-8"
fld protected final static java.lang.String JAKARTA_EE_VERSION_9 = "jakarta-ee-version-9"
fld protected final static java.lang.String JAKARTA_EE_VERSION_91 = "jakarta-ee-version-91"
fld protected final static java.lang.String JAVA_EE_VERSION_15 = "java-ee-version-15"
fld protected final static java.lang.String JAVA_EE_VERSION_16 = "java-ee-version-16"
fld protected final static java.lang.String JAVA_EE_VERSION_17 = "java-ee-version-17"
fld protected final static java.lang.String JAVA_EE_VERSION_18 = "java-ee-version-18"
fld protected final static java.lang.String JAVA_EE_VERSION_NONE = "java-ee-version-none"
intf org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportImpl
meth protected abstract void addJaxWs20Library() throws java.lang.Exception
meth protected java.lang.String getProjectJavaEEVersion()
meth protected org.openide.filesystems.FileObject getWsdlFolderForClient(java.lang.String) throws java.io.IOException
meth protected org.openide.filesystems.FileObject getXmlArtifactsRoot()
meth public abstract org.openide.filesystems.FileObject getWsdlFolder(boolean) throws java.io.IOException
meth public java.lang.String addServiceClient(java.lang.String,java.lang.String,java.lang.String,boolean)
meth public java.lang.String getServiceRefName(org.openide.nodes.Node)
meth public java.lang.String getWsdlUrl(java.lang.String)
meth public java.net.URL getCatalog()
meth public java.util.List<org.netbeans.modules.websvc.api.jaxws.project.config.Client> getServiceClients()
meth public org.netbeans.spi.project.support.ant.AntProjectHelper getAntProjectHelper()
meth public org.openide.filesystems.FileObject getBindingsFolderForClient(java.lang.String,boolean)
meth public org.openide.filesystems.FileObject getLocalWsdlFolderForClient(java.lang.String,boolean)
meth public void removeServiceClient(java.lang.String)
supr java.lang.Object
hfds DEFAULT_WSIMPORT_OPTIONS,DEFAULT_WSIMPORT_VALUES,PACKAGE_OPTION,TARGET_OPTION,WSDL_LOCATION_OPTION,XENDORSED_OPTION,XNOCOMPILE_OPTION,clientArtifactsFolder,helper,project
hcls WsImportFailedMessage

