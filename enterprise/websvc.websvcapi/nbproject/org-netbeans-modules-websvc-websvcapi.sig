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

CLSS public org.netbeans.modules.websvc.api.webservices.StubDescriptor
cons public init(java.lang.String,java.lang.String)
fld public final static java.lang.String SEI_SERVICE_STUB = "sei_service"
fld public final static java.lang.String WSDL_SERVICE_STUB = "wsdl_service"
meth public java.lang.String getDisplayName()
meth public java.lang.String getName()
meth public java.lang.String toString()
supr java.lang.Object
hfds displayName,name

CLSS public final org.netbeans.modules.websvc.api.webservices.WebServicesSupport
meth public boolean isBroken(org.netbeans.api.project.Project)
meth public boolean isFromWSDL(java.lang.String)
meth public java.lang.String generateImplementationBean(java.lang.String,org.openide.filesystems.FileObject,org.netbeans.api.project.Project,java.lang.String) throws java.io.IOException
meth public java.lang.String getArchiveDDFolderName()
meth public java.lang.String getImplementationBean(java.lang.String)
meth public java.util.List<org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport$ServiceSettings> getServices()
meth public org.netbeans.spi.project.support.ant.AntProjectHelper getAntProjectHelper()
meth public org.netbeans.spi.project.support.ant.ReferenceHelper getReferenceHelper()
meth public org.openide.filesystems.FileObject getWebservicesDD()
meth public org.openide.filesystems.FileObject getWsDDFolder()
meth public static org.netbeans.modules.websvc.api.webservices.WebServicesSupport getWebServicesSupport(org.openide.filesystems.FileObject)
meth public void addInfrastructure(java.lang.String,org.openide.filesystems.FileObject)
meth public void addServiceEntriesToDD(java.lang.String,java.lang.String,java.lang.String)
meth public void addServiceImpl(java.lang.String,org.openide.filesystems.FileObject,boolean)
meth public void addServiceImpl(java.lang.String,org.openide.filesystems.FileObject,boolean,java.lang.String[])
meth public void addServiceImplLinkEntry(org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean,java.lang.String)
meth public void removeProjectEntries(java.lang.String)
meth public void removeServiceEntry(java.lang.String)
meth public void showBrokenAlert(org.netbeans.api.project.Project)
supr java.lang.Object
hfds impl,implementations

CLSS public final org.netbeans.modules.websvc.api.webservices.WebServicesView
meth public org.openide.nodes.Node createWebServicesView(org.openide.filesystems.FileObject)
meth public static org.netbeans.modules.websvc.api.webservices.WebServicesView getWebServicesView(org.openide.filesystems.FileObject)
supr java.lang.Object
hfds impl,implementations

CLSS public abstract interface org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport
fld public final static java.lang.String PROP_DEBUG_CHANGED = "debugChanged"
fld public final static java.lang.String PROP_FEATURES_CHANGED = "featuresChanged"
fld public final static java.lang.String PROP_OPTIMIZE_CHANGED = "optimizeChanged"
fld public final static java.lang.String PROP_VERBOSE_CHANGED = "verboseChanged"
innr public abstract interface static Panel
innr public final static FeatureDescriptor
innr public final static ServiceSettings
meth public abstract org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport$Panel getWsCompileSupport()

CLSS public final static org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport$FeatureDescriptor
 outer org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getFeatures()
meth public java.lang.String getServiceName()
supr java.lang.Object
hfds features,serviceName

CLSS public abstract interface static org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport$Panel
 outer org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport
meth public abstract javax.swing.JPanel getComponent()
meth public abstract void initValues(java.util.List)
meth public abstract void validatePanel() throws org.openide.WizardValidationException

CLSS public final static org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport$ServiceSettings
 outer org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport
cons public init(java.lang.String,org.netbeans.modules.websvc.api.webservices.StubDescriptor,java.lang.String,java.util.List,java.util.List)
meth public java.lang.String getCurrentFeatures()
meth public java.lang.String getNewFeatures()
meth public java.lang.String getServiceName()
meth public java.lang.String toString()
meth public java.util.List getAvailableFeatures()
meth public java.util.List getImportantFeatures()
meth public org.netbeans.modules.websvc.api.webservices.StubDescriptor getStubDescriptor()
meth public void setNewFeatures(java.lang.String)
supr java.lang.Object
hfds availableFeatures,currentFeatures,importantFeatures,name,newFeatures,stubType

CLSS public org.netbeans.modules.websvc.spi.webservices.WebServicesConstants
cons public init()
fld public final static java.lang.String CLIENT_SOURCE_URL = "client-source-url"
fld public final static java.lang.String CONFIG_PROP_SUFFIX = ".config.name"
fld public final static java.lang.String J2EE_PLATFORM_JSR109_SUPPORT = "j2ee.platform.is.jsr109"
fld public final static java.lang.String J2EE_PLATFORM_JWSDP_CLASSPATH = "j2ee.platform.jwsdp.classpath"
fld public final static java.lang.String J2EE_PLATFORM_WSCOMPILE_CLASSPATH = "j2ee.platform.wscompile.classpath"
fld public final static java.lang.String J2EE_PLATFORM_WSGEN_CLASSPATH = "j2ee.platform.wsgen.classpath"
fld public final static java.lang.String J2EE_PLATFORM_WSIMPORT_CLASSPATH = "j2ee.platform.wsimport.classpath"
fld public final static java.lang.String J2EE_PLATFORM_WSIT_CLASSPATH = "j2ee.platform.wsit.classpath"
fld public final static java.lang.String MAPPING_FILE_SUFFIX = "-mapping.xml"
fld public final static java.lang.String MAPPING_PROP_SUFFIX = ".mapping"
fld public final static java.lang.String WEBSERVICES_DD = "webservices"
fld public final static java.lang.String WEBSVC_GENERATED_DIR = "websvc.generated.dir"
fld public final static java.lang.String WEB_SERVICE = "web-service"
fld public final static java.lang.String WEB_SERVICES = "web-services"
fld public final static java.lang.String WEB_SERVICE_FROM_WSDL = "from-wsdl"
fld public final static java.lang.String WEB_SERVICE_NAME = "web-service-name"
fld public final static java.lang.String WEB_SERVICE_STUB_TYPE = "web-service-stub-type"
fld public final static java.lang.String WSCOMPILE_CLASSPATH = "wscompile.classpath"
fld public final static java.lang.String WSCOMPILE_TOOLS_CLASSPATH = "wscompile.tools.classpath"
fld public final static java.lang.String WSDL_FOLDER = "wsdl"
fld public final static java.lang.String WebServiceServlet_PREFIX = "WSServlet_"
fld public final static java.lang.String[] WSCOMPILE_JARS
supr java.lang.Object

CLSS public final org.netbeans.modules.websvc.spi.webservices.WebServicesSupportFactory
meth public static org.netbeans.modules.websvc.api.webservices.WebServicesSupport createWebServicesSupport(org.netbeans.modules.websvc.spi.webservices.WebServicesSupportImpl)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.websvc.spi.webservices.WebServicesSupportImpl
meth public abstract boolean isFromWSDL(java.lang.String)
meth public abstract java.lang.String generateImplementationBean(java.lang.String,org.openide.filesystems.FileObject,org.netbeans.api.project.Project,java.lang.String) throws java.io.IOException
meth public abstract java.lang.String getArchiveDDFolderName()
meth public abstract java.lang.String getImplementationBean(java.lang.String)
meth public abstract java.util.List<org.netbeans.modules.websvc.api.webservices.WsCompileEditorSupport$ServiceSettings> getServices()
meth public abstract org.netbeans.api.java.classpath.ClassPath getClassPath()
meth public abstract org.netbeans.spi.project.support.ant.AntProjectHelper getAntProjectHelper()
meth public abstract org.netbeans.spi.project.support.ant.ReferenceHelper getReferenceHelper()
meth public abstract org.openide.filesystems.FileObject getWebservicesDD()
meth public abstract org.openide.filesystems.FileObject getWsDDFolder()
meth public abstract void addInfrastructure(java.lang.String,org.openide.filesystems.FileObject)
meth public abstract void addServiceEntriesToDD(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void addServiceImpl(java.lang.String,org.openide.filesystems.FileObject,boolean)
meth public abstract void addServiceImpl(java.lang.String,org.openide.filesystems.FileObject,boolean,java.lang.String[])
meth public abstract void addServiceImplLinkEntry(org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean,java.lang.String)
meth public abstract void removeProjectEntries(java.lang.String)
meth public abstract void removeServiceEntry(java.lang.String)

CLSS public abstract interface org.netbeans.modules.websvc.spi.webservices.WebServicesSupportProvider
meth public abstract org.netbeans.modules.websvc.api.webservices.WebServicesSupport findWebServicesSupport(org.openide.filesystems.FileObject)

CLSS public final org.netbeans.modules.websvc.spi.webservices.WebServicesViewFactory
meth public static org.netbeans.modules.websvc.api.webservices.WebServicesView createWebServicesView(org.netbeans.modules.websvc.spi.webservices.WebServicesViewImpl)
supr java.lang.Object

CLSS public abstract interface org.netbeans.modules.websvc.spi.webservices.WebServicesViewImpl
meth public abstract org.openide.nodes.Node createWebServicesView(org.openide.filesystems.FileObject)

CLSS public abstract interface org.netbeans.modules.websvc.spi.webservices.WebServicesViewProvider
meth public abstract org.netbeans.modules.websvc.api.webservices.WebServicesView findWebServicesView(org.openide.filesystems.FileObject)

