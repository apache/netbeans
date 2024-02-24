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

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.lang.Object clone()
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract java.lang.String getId()
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setId(java.lang.String)
meth public abstract void write(java.io.OutputStream) throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.ComponentInterface
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.CreateCapability
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
intf org.netbeans.modules.j2ee.dd.api.common.DisplayNameInterface
intf org.netbeans.modules.j2ee.dd.api.common.FindCapability
intf org.netbeans.modules.j2ee.dd.api.common.IconInterface

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.CreateCapability
meth public abstract org.netbeans.modules.j2ee.dd.api.common.CommonDDBean addBean(java.lang.String) throws java.lang.ClassNotFoundException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.CommonDDBean addBean(java.lang.String,java.lang.String[],java.lang.Object[],java.lang.String) throws java.lang.ClassNotFoundException,org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.CommonDDBean createBean(java.lang.String) throws java.lang.ClassNotFoundException

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract java.lang.String getDefaultDescription()
meth public abstract java.lang.String getDescription(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.util.Map getAllDescriptions()
meth public abstract void removeAllDescriptions()
meth public abstract void removeDescription()
meth public abstract void removeDescriptionForLocale(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setAllDescriptions(java.util.Map) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setDescription(java.lang.String)
meth public abstract void setDescription(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.DisplayNameInterface
meth public abstract java.lang.String getDefaultDisplayName()
meth public abstract java.lang.String getDisplayName(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.util.Map getAllDisplayNames()
meth public abstract void removeAllDisplayNames()
meth public abstract void removeDisplayName()
meth public abstract void removeDisplayNameForLocale(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setAllDisplayNames(java.util.Map) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setDisplayName(java.lang.String)
meth public abstract void setDisplayName(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.FindCapability
meth public abstract org.netbeans.modules.j2ee.dd.api.common.CommonDDBean findBeanByName(java.lang.String,java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.IconInterface
meth public abstract java.lang.String getLargeIcon()
meth public abstract java.lang.String getLargeIcon(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getSmallIcon()
meth public abstract java.lang.String getSmallIcon(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.util.Map getAllIcons()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.Icon getDefaultIcon()
meth public abstract void removeAllIcons()
meth public abstract void removeIcon()
meth public abstract void removeIcon(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void removeLargeIcon()
meth public abstract void removeLargeIcon(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void removeSmallIcon()
meth public abstract void removeSmallIcon(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setAllIcons(java.lang.String[],java.lang.String[],java.lang.String[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon)
meth public abstract void setLargeIcon(java.lang.String)
meth public abstract void setLargeIcon(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setSmallIcon(java.lang.String)
meth public abstract void setSmallIcon(java.lang.String,java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.RootInterface
fld public final static int MERGE_INTERSECT = 1
fld public final static int MERGE_UNION = 2
fld public final static int MERGE_UPDATE = 3
intf org.netbeans.modules.j2ee.dd.api.common.ComponentInterface
meth public abstract void merge(org.netbeans.modules.j2ee.dd.api.common.RootInterface,int)
meth public abstract void write(org.openide.filesystems.FileObject) throws java.io.IOException

CLSS public final org.netbeans.modules.j2ee.dd.api.webservices.DDProvider
meth public org.netbeans.modules.j2ee.dd.api.webservices.Webservices getDDRoot(org.openide.filesystems.FileObject) throws java.io.IOException
meth public org.netbeans.modules.j2ee.dd.api.webservices.Webservices getDDRoot(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public org.netbeans.modules.j2ee.dd.api.webservices.Webservices getDDRootCopy(org.openide.filesystems.FileObject) throws java.io.IOException
meth public org.netbeans.modules.schema2beans.BaseBean getBaseBean(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
 anno 0 java.lang.Deprecated()
meth public org.xml.sax.SAXParseException parse(org.openide.filesystems.FileObject) throws java.io.IOException,org.xml.sax.SAXException
meth public static org.netbeans.modules.j2ee.dd.api.webservices.DDProvider getDefault()
supr java.lang.Object
hfds ddMap,ddProvider
hcls DDParse,DDResolver,ErrorHandler

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.webservices.PortComponent
fld public final static java.lang.String HANDLER = "Handler"
fld public final static java.lang.String PORTCOMPONENTNAMEID = "PortComponentNameId"
fld public final static java.lang.String PORT_COMPONENT_NAME = "PortComponentName"
fld public final static java.lang.String SERVICE_ENDPOINT_INTERFACE = "ServiceEndpointInterface"
fld public final static java.lang.String SERVICE_IMPL_BEAN = "ServiceImplBean"
fld public final static java.lang.String WSDLPORTID = "WsdlPortId"
fld public final static java.lang.String WSDL_PORT = "WsdlPort"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract int addHandler(org.netbeans.modules.j2ee.dd.api.webservices.PortComponentHandler)
meth public abstract int removeHandler(org.netbeans.modules.j2ee.dd.api.webservices.PortComponentHandler)
meth public abstract int sizeHandler()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getDescriptionId()
meth public abstract java.lang.String getDescriptionXmlLang()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getDisplayNameId()
meth public abstract java.lang.String getDisplayNameXmlLang()
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getPortComponentName()
meth public abstract java.lang.String getPortComponentNameId()
meth public abstract java.lang.String getServiceEndpointInterface()
meth public abstract java.lang.String getWsdlPortId()
meth public abstract java.lang.String getWsdlServiceId() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract javax.xml.namespace.QName getWsdlPort()
meth public abstract javax.xml.namespace.QName getWsdlService() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.Icon getIcon()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.Icon newIcon()
meth public abstract org.netbeans.modules.j2ee.dd.api.webservices.PortComponentHandler getHandler(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.webservices.PortComponentHandler newPortComponentHandler()
meth public abstract org.netbeans.modules.j2ee.dd.api.webservices.PortComponentHandler[] getHandler()
meth public abstract org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean getServiceImplBean()
meth public abstract org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean newServiceImplBean()
meth public abstract void setDescription(java.lang.String)
meth public abstract void setDescriptionId(java.lang.String)
meth public abstract void setDescriptionXmlLang(java.lang.String)
meth public abstract void setDisplayName(java.lang.String)
meth public abstract void setDisplayNameId(java.lang.String)
meth public abstract void setDisplayNameXmlLang(java.lang.String)
meth public abstract void setHandler(int,org.netbeans.modules.j2ee.dd.api.webservices.PortComponentHandler)
meth public abstract void setHandler(org.netbeans.modules.j2ee.dd.api.webservices.PortComponentHandler[])
meth public abstract void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon)
meth public abstract void setId(java.lang.String)
meth public abstract void setPortComponentName(java.lang.String)
meth public abstract void setPortComponentNameId(java.lang.String)
meth public abstract void setServiceEndpointInterface(java.lang.String)
meth public abstract void setServiceImplBean(org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean)
meth public abstract void setWsdlPort(javax.xml.namespace.QName)
meth public abstract void setWsdlPortId(java.lang.String)
meth public abstract void setWsdlService(javax.xml.namespace.QName) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setWsdlServiceId(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.webservices.PortComponentHandler
intf org.netbeans.modules.j2ee.dd.api.common.ComponentInterface
meth public abstract int addInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam)
meth public abstract int addSoapHeader(javax.xml.namespace.QName)
meth public abstract int addSoapRole(java.lang.String)
meth public abstract int removeInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam)
meth public abstract int removeSoapHeader(javax.xml.namespace.QName)
meth public abstract int removeSoapRole(java.lang.String)
meth public abstract int sizeInitParam()
meth public abstract int sizeSoapHeader()
meth public abstract int sizeSoapHeaderId()
meth public abstract int sizeSoapRole()
meth public abstract java.lang.String getHandlerClass()
meth public abstract java.lang.String getHandlerName()
meth public abstract java.lang.String getHandlerNameId()
meth public abstract java.lang.String getSoapHeaderId(int)
meth public abstract java.lang.String getSoapRole(int)
meth public abstract java.lang.String getSoapRoleId()
meth public abstract java.lang.String[] getSoapRole()
meth public abstract javax.xml.namespace.QName getSoapHeader(int)
meth public abstract javax.xml.namespace.QName[] getSoapHeader()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.Icon newIcon()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InitParam getInitParam(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InitParam newInitParam()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InitParam[] getInitParam()
meth public abstract void setHandlerClass(java.lang.String)
meth public abstract void setHandlerName(java.lang.String)
meth public abstract void setHandlerNameId(java.lang.String)
meth public abstract void setInitParam(int,org.netbeans.modules.j2ee.dd.api.common.InitParam)
meth public abstract void setInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam[])
meth public abstract void setSoapHeader(int,javax.xml.namespace.QName)
meth public abstract void setSoapHeader(javax.xml.namespace.QName[])
meth public abstract void setSoapHeaderId(int,java.lang.String)
meth public abstract void setSoapRole(int,java.lang.String)
meth public abstract void setSoapRole(java.lang.String[])
meth public abstract void setSoapRoleId(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.webservices.ServiceImplBean
fld public final static java.lang.String EJB_LINK = "EjbLink"
fld public final static java.lang.String SERVLET_LINK = "ServletLink"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.lang.String getEjbLink()
meth public abstract java.lang.String getServletLink()
meth public abstract void setEjbLink(java.lang.String)
meth public abstract void setServletLink(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription
fld public final static java.lang.String JAXRPC_MAPPING_FILE = "JaxrpcMappingFile"
fld public final static java.lang.String PORT_COMPONENT = "PortComponent"
fld public final static java.lang.String WEBSERVICEDESCRIPTIONNAMEID = "WebserviceDescriptionNameId"
fld public final static java.lang.String WEBSERVICE_DESCRIPTION_NAME = "WebserviceDescriptionName"
fld public final static java.lang.String WSDL_FILE = "WsdlFile"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract int addPortComponent(org.netbeans.modules.j2ee.dd.api.webservices.PortComponent)
meth public abstract int removePortComponent(org.netbeans.modules.j2ee.dd.api.webservices.PortComponent)
meth public abstract int sizePortComponent()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getDescriptionId()
meth public abstract java.lang.String getDescriptionXmlLang()
meth public abstract java.lang.String getDisplayName()
meth public abstract java.lang.String getDisplayNameId()
meth public abstract java.lang.String getDisplayNameXmlLang()
meth public abstract java.lang.String getId()
meth public abstract java.lang.String getJaxrpcMappingFile()
meth public abstract java.lang.String getWebserviceDescriptionName()
meth public abstract java.lang.String getWebserviceDescriptionNameId()
meth public abstract java.lang.String getWsdlFile()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.Icon getIcon()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.Icon newIcon()
meth public abstract org.netbeans.modules.j2ee.dd.api.webservices.PortComponent getPortComponent(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.webservices.PortComponent newPortComponent()
meth public abstract org.netbeans.modules.j2ee.dd.api.webservices.PortComponent[] getPortComponent()
meth public abstract void setDescription(java.lang.String)
meth public abstract void setDescriptionId(java.lang.String)
meth public abstract void setDescriptionXmlLang(java.lang.String)
meth public abstract void setDisplayName(java.lang.String)
meth public abstract void setDisplayNameId(java.lang.String)
meth public abstract void setDisplayNameXmlLang(java.lang.String)
meth public abstract void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon)
meth public abstract void setId(java.lang.String)
meth public abstract void setJaxrpcMappingFile(java.lang.String)
meth public abstract void setPortComponent(int,org.netbeans.modules.j2ee.dd.api.webservices.PortComponent)
meth public abstract void setPortComponent(org.netbeans.modules.j2ee.dd.api.webservices.PortComponent[])
meth public abstract void setWebserviceDescriptionName(java.lang.String)
meth public abstract void setWebserviceDescriptionNameId(java.lang.String)
meth public abstract void setWsdlFile(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.webservices.Webservices
fld public final static int STATE_INVALID_PARSABLE = 1
fld public final static int STATE_INVALID_UNPARSABLE = 2
fld public final static int STATE_VALID = 0
fld public final static java.lang.String PROPERTY_STATUS = "dd_status"
fld public final static java.lang.String PROPERTY_VERSION = "dd_version"
fld public final static java.lang.String VERSION = "Version"
fld public final static java.lang.String VERSION_1_1 = "1.1"
fld public final static java.lang.String VERSION_1_2 = "1.2"
fld public final static java.lang.String WEBSERVICE_DESCRIPTION = "WebserviceDescription"
intf org.netbeans.modules.j2ee.dd.api.common.RootInterface
meth public abstract int addWebserviceDescription(org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription)
meth public abstract int getStatus()
meth public abstract int removeWebserviceDescription(org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription)
meth public abstract int sizeWebserviceDescription()
meth public abstract java.math.BigDecimal getVersion()
meth public abstract org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription getWebserviceDescription(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription newWebserviceDescription()
meth public abstract org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription[] getWebserviceDescription()
meth public abstract org.xml.sax.SAXParseException getError()
meth public abstract void setWebserviceDescription(int,org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription)
meth public abstract void setWebserviceDescription(org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata
meth public abstract org.netbeans.modules.j2ee.dd.api.webservices.WebserviceDescription findWebserviceByName(java.lang.String)
meth public abstract org.netbeans.modules.j2ee.dd.api.webservices.Webservices getRoot()

CLSS public org.netbeans.modules.j2ee.dd.spi.webservices.WebservicesMetadataModelFactory
meth public static org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata> createMetadataModel(org.netbeans.modules.j2ee.dd.spi.MetadataUnit)
supr java.lang.Object

