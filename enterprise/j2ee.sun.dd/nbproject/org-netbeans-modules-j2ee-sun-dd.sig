#Signature file v4.1
#Version 1.56.0

CLSS public abstract interface java.io.Serializable

CLSS public abstract interface java.lang.Cloneable

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

CLSS public final org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion
fld public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion GLASSFISH_4_0
fld public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion GLASSFISH_4_1
fld public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion GLASSFISH_5_0
fld public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion GLASSFISH_5_1
fld public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion GLASSFISH_6
fld public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion GLASSFISH_7
fld public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion SUN_APPSERVER_10_0
fld public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion SUN_APPSERVER_10_1
fld public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion SUN_APPSERVER_7_0
fld public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion SUN_APPSERVER_8_0
fld public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion SUN_APPSERVER_8_1
fld public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion SUN_APPSERVER_9_0
fld public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion SUN_APPSERVER_9_1_1
fld public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion SUN_WEBSERVER_7_0
fld public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion[] asDDVersions
fld public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion[] webServerDDVersions
meth public final java.lang.String getAppClientVersionAsString()
meth public final java.lang.String getApplicationVersionAsString()
meth public final java.lang.String getCmpMappingsVersionAsString()
meth public final java.lang.String getEjbJarVersionAsString()
meth public final java.lang.String getSunAppClientPublicId()
meth public final java.lang.String getSunAppClientSystemId()
meth public final java.lang.String getSunApplicationPublicId()
meth public final java.lang.String getSunApplicationSystemId()
meth public final java.lang.String getSunCmpMappingsPublicId()
meth public final java.lang.String getSunCmpMappingsSystemId()
meth public final java.lang.String getSunEjbJarPublicId()
meth public final java.lang.String getSunEjbJarSystemId()
meth public final java.lang.String getSunWebAppPublicId()
meth public final java.lang.String getSunWebAppSystemId()
meth public final java.lang.String getWebAppVersionAsString()
meth public final java.math.BigDecimal getNumericAppClientVersion()
meth public final java.math.BigDecimal getNumericApplicationVersion()
meth public final java.math.BigDecimal getNumericCmpMappingsVersion()
meth public final java.math.BigDecimal getNumericEjbJarVersion()
meth public final java.math.BigDecimal getNumericServerVersion()
meth public final java.math.BigDecimal getNumericWebAppVersion()
meth public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getASDDVersion(java.lang.String)
meth public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getASDDVersion(java.math.BigDecimal)
meth public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getASDDVersionFromAppClientVersion(java.math.BigDecimal)
meth public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getASDDVersionFromAppVersion(java.math.BigDecimal)
meth public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getASDDVersionFromCmpMappingsVersion(java.math.BigDecimal)
meth public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getASDDVersionFromEjbVersion(java.math.BigDecimal)
meth public final static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getASDDVersionFromServletVersion(java.math.BigDecimal)
meth public int compareTo(java.lang.Object)
meth public java.lang.String toString()
supr java.lang.Object
hfds appClientPublicId,appClientSystemId,appClientVersion,appClientVersionString,appPublicId,appSystemId,appVersion,appVersionString,cmpMappingsPublicId,cmpMappingsSystemId,cmpMappingsVersion,cmpMappingsVersionString,displayName,ejbJarPublicId,ejbJarSystemId,ejbVersion,ejbVersionString,numericVersion,servletVersion,servletVersionString,version,webAppPublicId,webAppSystemId

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
fld public final static int MERGE_INTERSECT = 1
fld public final static int MERGE_UNION = 2
fld public final static int MERGE_UPDATE = 3
meth public abstract boolean isTrivial(java.lang.String)
meth public abstract int addValue(java.lang.String,java.lang.Object)
meth public abstract int removeValue(java.lang.String,java.lang.Object)
meth public abstract int size(java.lang.String)
meth public abstract java.lang.Object clone()
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract java.lang.Object getValue(java.lang.String,int)
meth public abstract java.lang.Object[] getValues(java.lang.String)
meth public abstract java.lang.String dumpBeanNode()
meth public abstract java.lang.String getAttributeValue(java.lang.String)
meth public abstract java.lang.String getAttributeValue(java.lang.String,int,java.lang.String)
meth public abstract java.lang.String getAttributeValue(java.lang.String,java.lang.String)
meth public abstract java.lang.String[] findPropertyValue(java.lang.String,java.lang.Object)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean cloneVersion(java.lang.String)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean getPropertyParent(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void merge(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,int)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void removeValue(java.lang.String,int)
meth public abstract void setAttributeValue(java.lang.String,int,java.lang.String,java.lang.String)
meth public abstract void setAttributeValue(java.lang.String,java.lang.String)
meth public abstract void setAttributeValue(java.lang.String,java.lang.String,java.lang.String)
meth public abstract void setValue(java.lang.String,int,java.lang.Object)
meth public abstract void setValue(java.lang.String,java.lang.Object)
meth public abstract void setValue(java.lang.String,java.lang.Object[])
meth public abstract void write(java.io.OutputStream) throws java.io.IOException
meth public abstract void write(java.io.Writer) throws java.io.IOException,org.netbeans.modules.j2ee.sun.dd.api.DDException

CLSS public org.netbeans.modules.j2ee.sun.dd.api.DDException
cons public init(java.lang.String)
meth public java.lang.String getMessage()
supr org.netbeans.modules.schema2beans.Schema2BeansException
hfds errorMsg

CLSS public final org.netbeans.modules.j2ee.sun.dd.api.DDProvider
meth public org.netbeans.modules.j2ee.sun.dd.api.RootInterface getDDRoot(java.io.Reader) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException,org.xml.sax.SAXException
meth public org.netbeans.modules.j2ee.sun.dd.api.RootInterface getDDRoot(org.openide.filesystems.FileObject) throws java.io.IOException
meth public org.netbeans.modules.j2ee.sun.dd.api.RootInterface getDDRoot(org.xml.sax.InputSource) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException,org.xml.sax.SAXException
meth public org.netbeans.modules.j2ee.sun.dd.api.RootInterface newGraph(java.lang.Class,java.lang.String)
meth public org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication getAppDDRoot(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient getAppClientDDRoot(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar getEjbDDRoot(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources getResourcesGraph(java.io.InputStream) throws java.io.IOException,org.xml.sax.SAXException
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources getResourcesGraph(java.lang.String)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources getResourcesRoot(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp getWebDDRoot(java.io.InputStream) throws java.io.IOException,org.netbeans.modules.j2ee.sun.dd.api.DDException,org.xml.sax.SAXException
meth public org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp getWebDDRoot(org.w3c.dom.Document) throws org.netbeans.modules.j2ee.sun.dd.api.DDException
meth public org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp getWebDDRoot(org.xml.sax.InputSource) throws java.io.IOException,org.netbeans.modules.j2ee.sun.dd.api.DDException,org.xml.sax.SAXException
meth public static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getASDDVersion(org.netbeans.modules.j2ee.sun.dd.api.RootInterface)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion getASDDVersion(org.netbeans.modules.j2ee.sun.dd.api.RootInterface,org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion)
meth public static org.netbeans.modules.j2ee.sun.dd.api.DDProvider getDefault()
meth public void merge(org.netbeans.modules.j2ee.sun.dd.api.RootInterface,java.io.Reader)
supr java.lang.Object
hfds apiToVersionMap,dObjMap,ddMap,ddProvider,publicIdToInfoMap,sunAppClientVersionMap,sunApplicationVersionMap,sunEjbJarVersionMap,sunResourcesVersionMap,sunWebAppVersionMap
hcls DDParse,DocTypeInfo,SunDDErrorHandler,SunDDFileChangeListener,SunDDResolver,VersionInfo

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.RootInterface
fld public final static int STATE_INVALID_PARSABLE = 1
fld public final static int STATE_INVALID_UNPARSABLE = 2
fld public final static int STATE_VALID = 0
fld public final static java.lang.String PROPERTY_STATUS = "dd_status"
fld public final static java.lang.String PROPERTY_VERSION = "dd_version"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract boolean isEventSource(org.netbeans.modules.j2ee.sun.dd.api.RootInterface)
meth public abstract int getStatus()
meth public abstract java.math.BigDecimal getVersion()
meth public abstract void setVersion(java.math.BigDecimal)
meth public abstract void write(org.openide.filesystems.FileObject) throws java.io.IOException

CLSS public org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getVersion()
supr java.lang.Exception
hfds exceptionMsg,version

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.app.SunApplication
fld public final static java.lang.String PASS_BY_REFERENCE = "PassByReference"
fld public final static java.lang.String REALM = "Realm"
fld public final static java.lang.String SECURITY_ROLE_MAPPING = "SecurityRoleMapping"
fld public final static java.lang.String UNIQUE_ID = "UniqueId"
fld public final static java.lang.String VERSION_1_3_0 = "1.30"
fld public final static java.lang.String VERSION_1_4_0 = "1.40"
fld public final static java.lang.String VERSION_5_0_0 = "5.00"
fld public final static java.lang.String VERSION_6_0_0 = "6.00"
fld public final static java.lang.String VERSION_6_0_1 = "6.01"
fld public final static java.lang.String WEB = "Web"
intf org.netbeans.modules.j2ee.sun.dd.api.RootInterface
meth public abstract int addSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping)
meth public abstract int addWeb(org.netbeans.modules.j2ee.sun.dd.api.app.Web)
meth public abstract int removeSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping)
meth public abstract int removeWeb(org.netbeans.modules.j2ee.sun.dd.api.app.Web)
meth public abstract int sizeSecurityRoleMapping()
meth public abstract int sizeWeb()
meth public abstract java.lang.String getPassByReference()
meth public abstract java.lang.String getRealm()
meth public abstract java.lang.String getUniqueId()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.app.Web getWeb(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.app.Web newWeb()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.app.Web[] getWeb()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping getSecurityRoleMapping(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping newSecurityRoleMapping()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[] getSecurityRoleMapping()
meth public abstract void setPassByReference(java.lang.String)
meth public abstract void setRealm(java.lang.String)
meth public abstract void setSecurityRoleMapping(int,org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping)
meth public abstract void setSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[])
meth public abstract void setUniqueId(java.lang.String)
meth public abstract void setWeb(int,org.netbeans.modules.j2ee.sun.dd.api.app.Web)
meth public abstract void setWeb(org.netbeans.modules.j2ee.sun.dd.api.app.Web[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.app.Web
fld public final static java.lang.String CONTEXT_ROOT = "ContextRoot"
fld public final static java.lang.String WEB_URI = "WebUri"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getContextRoot()
meth public abstract java.lang.String getWebUri()
meth public abstract void setContextRoot(java.lang.String)
meth public abstract void setWebUri(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.client.JavaWebStartAccess
fld public final static java.lang.String CONTEXT_ROOT = "ContextRoot"
fld public final static java.lang.String ELIGIBLE = "Eligible"
fld public final static java.lang.String VENDOR = "Vendor"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract boolean isJnlpDoc() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getContextRoot()
meth public abstract java.lang.String getEligible()
meth public abstract java.lang.String getJnlpDocHref() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getVendor()
meth public abstract void setContextRoot(java.lang.String)
meth public abstract void setEligible(java.lang.String)
meth public abstract void setJnlpDoc(boolean) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setJnlpDocHref(java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setVendor(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.client.SunApplicationClient
fld public final static java.lang.String EJB_REF = "EjbRef"
fld public final static java.lang.String JAVA_WEB_START_ACCESS = "JavaWebStartAccess"
fld public final static java.lang.String MESSAGE_DESTINATION = "MessageDestination"
fld public final static java.lang.String MESSAGE_DESTINATION_REF = "MessageDestinationRef"
fld public final static java.lang.String RESOURCE_ENV_REF = "ResourceEnvRef"
fld public final static java.lang.String RESOURCE_REF = "ResourceRef"
fld public final static java.lang.String SERVICE_REF = "ServiceRef"
fld public final static java.lang.String VERSION_1_3_0 = "1.30"
fld public final static java.lang.String VERSION_1_4_0 = "1.40"
fld public final static java.lang.String VERSION_1_4_1 = "1.41"
fld public final static java.lang.String VERSION_5_0_0 = "5.00"
fld public final static java.lang.String VERSION_6_0_0 = "6.00"
fld public final static java.lang.String VERSION_6_0_1 = "6.01"
intf org.netbeans.modules.j2ee.sun.dd.api.RootInterface
meth public abstract int addEjbRef(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef)
meth public abstract int addMessageDestination(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination)
meth public abstract int addMessageDestinationRef(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int addResourceEnvRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef)
meth public abstract int addResourceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef)
meth public abstract int addServiceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef)
meth public abstract int removeEjbRef(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef)
meth public abstract int removeMessageDestination(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination)
meth public abstract int removeMessageDestinationRef(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int removeResourceEnvRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef)
meth public abstract int removeResourceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef)
meth public abstract int removeServiceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef)
meth public abstract int sizeEjbRef()
meth public abstract int sizeMessageDestination()
meth public abstract int sizeMessageDestinationRef() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int sizeResourceEnvRef()
meth public abstract int sizeResourceRef()
meth public abstract int sizeServiceRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.client.JavaWebStartAccess getJavaWebStartAccess() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.client.JavaWebStartAccess newJavaWebStartAccess() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef getEjbRef(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef newEjbRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef[] getEjbRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination getMessageDestination(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination newMessageDestination()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef getMessageDestinationRef(int) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef newMessageDestinationRef() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef[] getMessageDestinationRef() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination[] getMessageDestination()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef getResourceEnvRef(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef newResourceEnvRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef[] getResourceEnvRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef getResourceRef(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef newResourceRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef[] getResourceRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef getServiceRef(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef newServiceRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef[] getServiceRef()
meth public abstract void setEjbRef(int,org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef)
meth public abstract void setEjbRef(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef[])
meth public abstract void setJavaWebStartAccess(org.netbeans.modules.j2ee.sun.dd.api.client.JavaWebStartAccess) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setMessageDestination(int,org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination)
meth public abstract void setMessageDestination(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination[])
meth public abstract void setMessageDestinationRef(int,org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setMessageDestinationRef(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef[]) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setResourceEnvRef(int,org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef)
meth public abstract void setResourceEnvRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef[])
meth public abstract void setResourceRef(int,org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef)
meth public abstract void setResourceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef[])
meth public abstract void setServiceRef(int,org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef)
meth public abstract void setServiceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.cmp.CheckVersionOfAccessedInstances
fld public final static java.lang.String COLUMN_NAME = "ColumnName"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addColumnName(java.lang.String)
meth public abstract int removeColumnName(java.lang.String)
meth public abstract int sizeColumnName()
meth public abstract java.lang.String getColumnName(int)
meth public abstract java.lang.String[] getColumnName()
meth public abstract void setColumnName(int,java.lang.String)
meth public abstract void setColumnName(java.lang.String[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.cmp.CmpFieldMapping
fld public final static java.lang.String COLUMN_NAME = "ColumnName"
fld public final static java.lang.String FETCHED_WITH = "FetchedWith"
fld public final static java.lang.String FIELD_NAME = "FieldName"
fld public final static java.lang.String READ_ONLY = "ReadOnly"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract boolean isReadOnly()
meth public abstract int addColumnName(java.lang.String)
meth public abstract int removeColumnName(java.lang.String)
meth public abstract int sizeColumnName()
meth public abstract java.lang.String getColumnName(int)
meth public abstract java.lang.String getFieldName()
meth public abstract java.lang.String[] getColumnName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.FetchedWith getFetchedWith()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.FetchedWith newFetchedWith()
meth public abstract void setColumnName(int,java.lang.String)
meth public abstract void setColumnName(java.lang.String[])
meth public abstract void setFetchedWith(org.netbeans.modules.j2ee.sun.dd.api.cmp.FetchedWith)
meth public abstract void setFieldName(java.lang.String)
meth public abstract void setReadOnly(boolean)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.cmp.CmrFieldMapping
fld public final static java.lang.String CMR_FIELD_NAME = "CmrFieldName"
fld public final static java.lang.String COLUMN_PAIR = "ColumnPair"
fld public final static java.lang.String FETCHED_WITH = "FetchedWith"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addColumnPair(org.netbeans.modules.j2ee.sun.dd.api.cmp.ColumnPair)
meth public abstract int removeColumnPair(org.netbeans.modules.j2ee.sun.dd.api.cmp.ColumnPair)
meth public abstract int sizeColumnPair()
meth public abstract java.lang.String getCmrFieldName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.ColumnPair getColumnPair(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.ColumnPair newColumnPair()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.ColumnPair[] getColumnPair()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.FetchedWith getFetchedWith()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.FetchedWith newFetchedWith()
meth public abstract void setCmrFieldName(java.lang.String)
meth public abstract void setColumnPair(int,org.netbeans.modules.j2ee.sun.dd.api.cmp.ColumnPair)
meth public abstract void setColumnPair(org.netbeans.modules.j2ee.sun.dd.api.cmp.ColumnPair[])
meth public abstract void setFetchedWith(org.netbeans.modules.j2ee.sun.dd.api.cmp.FetchedWith)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.cmp.ColumnPair
fld public final static java.lang.String COLUMN_NAME = "ColumnName"
fld public final static java.lang.String COLUMN_NAME2 = "ColumnName2"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getColumnName()
meth public abstract java.lang.String getColumnName2()
meth public abstract void setColumnName(java.lang.String)
meth public abstract void setColumnName2(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.cmp.Consistency
fld public final static java.lang.String CHECK_ALL_AT_COMMIT = "CheckAllAtCommit"
fld public final static java.lang.String CHECK_MODIFIED_AT_COMMIT = "CheckModifiedAtCommit"
fld public final static java.lang.String CHECK_VERSION_OF_ACCESSED_INSTANCES = "CheckVersionOfAccessedInstances"
fld public final static java.lang.String LOCK_WHEN_LOADED = "LockWhenLoaded"
fld public final static java.lang.String LOCK_WHEN_MODIFIED = "LockWhenModified"
fld public final static java.lang.String NONE = "None"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract boolean isCheckAllAtCommit()
meth public abstract boolean isCheckModifiedAtCommit()
meth public abstract boolean isLockWhenLoaded()
meth public abstract boolean isLockWhenModified()
meth public abstract boolean isNone()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.CheckVersionOfAccessedInstances getCheckVersionOfAccessedInstances() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.CheckVersionOfAccessedInstances newCheckVersionOfAccessedInstances() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setCheckAllAtCommit(boolean)
meth public abstract void setCheckModifiedAtCommit(boolean)
meth public abstract void setCheckVersionOfAccessedInstances(org.netbeans.modules.j2ee.sun.dd.api.cmp.CheckVersionOfAccessedInstances) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setLockWhenLoaded(boolean)
meth public abstract void setLockWhenModified(boolean)
meth public abstract void setNone(boolean)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.cmp.EntityMapping
fld public final static java.lang.String CMP_FIELD_MAPPING = "CmpFieldMapping"
fld public final static java.lang.String CMR_FIELD_MAPPING = "CmrFieldMapping"
fld public final static java.lang.String CONSISTENCY = "Consistency"
fld public final static java.lang.String EJB_NAME = "EjbName"
fld public final static java.lang.String SECONDARY_TABLE = "SecondaryTable"
fld public final static java.lang.String TABLE_NAME = "TableName"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addCmpFieldMapping(org.netbeans.modules.j2ee.sun.dd.api.cmp.CmpFieldMapping)
meth public abstract int addCmrFieldMapping(org.netbeans.modules.j2ee.sun.dd.api.cmp.CmrFieldMapping)
meth public abstract int addSecondaryTable(org.netbeans.modules.j2ee.sun.dd.api.cmp.SecondaryTable)
meth public abstract int removeCmpFieldMapping(org.netbeans.modules.j2ee.sun.dd.api.cmp.CmpFieldMapping)
meth public abstract int removeCmrFieldMapping(org.netbeans.modules.j2ee.sun.dd.api.cmp.CmrFieldMapping)
meth public abstract int removeSecondaryTable(org.netbeans.modules.j2ee.sun.dd.api.cmp.SecondaryTable)
meth public abstract int sizeCmpFieldMapping()
meth public abstract int sizeCmrFieldMapping()
meth public abstract int sizeSecondaryTable()
meth public abstract java.lang.String getEjbName()
meth public abstract java.lang.String getTableName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.CmpFieldMapping getCmpFieldMapping(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.CmpFieldMapping newCmpFieldMapping()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.CmpFieldMapping[] getCmpFieldMapping()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.CmrFieldMapping getCmrFieldMapping(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.CmrFieldMapping newCmrFieldMapping()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.CmrFieldMapping[] getCmrFieldMapping()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.Consistency getConsistency()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.Consistency newConsistency()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.SecondaryTable getSecondaryTable(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.SecondaryTable newSecondaryTable()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.SecondaryTable[] getSecondaryTable()
meth public abstract void setCmpFieldMapping(int,org.netbeans.modules.j2ee.sun.dd.api.cmp.CmpFieldMapping)
meth public abstract void setCmpFieldMapping(org.netbeans.modules.j2ee.sun.dd.api.cmp.CmpFieldMapping[])
meth public abstract void setCmrFieldMapping(int,org.netbeans.modules.j2ee.sun.dd.api.cmp.CmrFieldMapping)
meth public abstract void setCmrFieldMapping(org.netbeans.modules.j2ee.sun.dd.api.cmp.CmrFieldMapping[])
meth public abstract void setConsistency(org.netbeans.modules.j2ee.sun.dd.api.cmp.Consistency)
meth public abstract void setEjbName(java.lang.String)
meth public abstract void setSecondaryTable(int,org.netbeans.modules.j2ee.sun.dd.api.cmp.SecondaryTable)
meth public abstract void setSecondaryTable(org.netbeans.modules.j2ee.sun.dd.api.cmp.SecondaryTable[])
meth public abstract void setTableName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.cmp.FetchedWith
fld public final static java.lang.String DEFAULT = "Default"
fld public final static java.lang.String LEVEL = "Level"
fld public final static java.lang.String NAMED_GROUP = "NamedGroup"
fld public final static java.lang.String NONE = "None"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract boolean isDefault() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract boolean isNone()
meth public abstract java.lang.Integer getLevel()
meth public abstract java.lang.String getNamedGroup()
meth public abstract void setDefault(boolean) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setLevel(java.lang.Integer)
meth public abstract void setNamedGroup(java.lang.String)
meth public abstract void setNone(boolean)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.cmp.SecondaryTable
fld public final static java.lang.String COLUMN_PAIR = "ColumnPair"
fld public final static java.lang.String TABLE_NAME = "TableName"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addColumnPair(org.netbeans.modules.j2ee.sun.dd.api.cmp.ColumnPair)
meth public abstract int removeColumnPair(org.netbeans.modules.j2ee.sun.dd.api.cmp.ColumnPair)
meth public abstract int sizeColumnPair()
meth public abstract java.lang.String getTableName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.ColumnPair getColumnPair(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.ColumnPair newColumnPair()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.ColumnPair[] getColumnPair()
meth public abstract void setColumnPair(int,org.netbeans.modules.j2ee.sun.dd.api.cmp.ColumnPair)
meth public abstract void setColumnPair(org.netbeans.modules.j2ee.sun.dd.api.cmp.ColumnPair[])
meth public abstract void setTableName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMapping
fld public final static java.lang.String ENTITY_MAPPING = "EntityMapping"
fld public final static java.lang.String SCHEMA = "Schema"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addEntityMapping(org.netbeans.modules.j2ee.sun.dd.api.cmp.EntityMapping)
meth public abstract int removeEntityMapping(org.netbeans.modules.j2ee.sun.dd.api.cmp.EntityMapping)
meth public abstract int sizeEntityMapping()
meth public abstract java.lang.String getSchema()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.EntityMapping getEntityMapping(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.EntityMapping newEntityMapping()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.EntityMapping[] getEntityMapping()
meth public abstract void setEntityMapping(int,org.netbeans.modules.j2ee.sun.dd.api.cmp.EntityMapping)
meth public abstract void setEntityMapping(org.netbeans.modules.j2ee.sun.dd.api.cmp.EntityMapping[])
meth public abstract void setSchema(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMappings
fld public final static java.lang.String SUN_CMP_MAPPING = "SunCmpMapping"
fld public final static java.lang.String VERSION_1_0 = "1.0"
fld public final static java.lang.String VERSION_1_1 = "1.1"
fld public final static java.lang.String VERSION_1_2 = "1.2"
intf org.netbeans.modules.j2ee.sun.dd.api.RootInterface
meth public abstract int addSunCmpMapping(org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMapping)
meth public abstract int removeSunCmpMapping(org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMapping)
meth public abstract int sizeSunCmpMapping()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMapping getSunCmpMapping(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMapping newSunCmpMapping()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMapping[] getSunCmpMapping()
meth public abstract void setSunCmpMapping(int,org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMapping)
meth public abstract void setSunCmpMapping(org.netbeans.modules.j2ee.sun.dd.api.cmp.SunCmpMapping[])

CLSS abstract interface org.netbeans.modules.j2ee.sun.dd.api.cmp.package-info

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String VALUE = "Value"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()
meth public abstract void setName(java.lang.String)
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String PASSWORD = "Password"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getPassword()
meth public abstract void setName(java.lang.String)
meth public abstract void setPassword(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef
fld public final static java.lang.String EJB_REF_NAME = "EjbRefName"
fld public final static java.lang.String JNDI_NAME = "JndiName"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getEjbRefName()
meth public abstract java.lang.String getJndiName()
meth public abstract void setEjbRefName(java.lang.String)
meth public abstract void setJndiName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.JavaMethod
fld public final static java.lang.String METHOD_NAME = "MethodName"
fld public final static java.lang.String METHOD_PARAMS = "MethodParams"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getMethodName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams getMethodParams()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams newMethodParams()
meth public abstract void setMethodName(java.lang.String)
meth public abstract void setMethodParams(org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.LoginConfig
fld public final static java.lang.String AUTH_METHOD = "AuthMethod"
fld public final static java.lang.String REALM = "Realm"
fld public final static java.lang.String VERSION_SERVER_8_1 = "Server 8.1"
fld public final static java.lang.String VERSION_SERVER_9_0 = "Server 9.0"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getAuthMethod()
meth public abstract java.lang.String getRealm() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setAuthMethod(java.lang.String)
meth public abstract void setRealm(java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.Message
fld public final static java.lang.String JAVA_METHOD = "JavaMethod"
fld public final static java.lang.String OPERATION_NAME = "OperationName"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getOperationName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.JavaMethod getJavaMethod()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.JavaMethod newJavaMethod()
meth public abstract void setJavaMethod(org.netbeans.modules.j2ee.sun.dd.api.common.JavaMethod)
meth public abstract void setOperationName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination
fld public final static java.lang.String JNDI_NAME = "JndiName"
fld public final static java.lang.String MESSAGE_DESTINATION_NAME = "MessageDestinationName"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getMessageDestinationName()
meth public abstract void setJndiName(java.lang.String)
meth public abstract void setMessageDestinationName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef
fld public final static java.lang.String JNDI_NAME = "JndiName"
fld public final static java.lang.String MESSAGE_DESTINATION_REF_NAME = "MessageDestinationRefName"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getMessageDestinationRefName()
meth public abstract void setJndiName(java.lang.String)
meth public abstract void setMessageDestinationRefName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurity
fld public final static java.lang.String MESSAGE = "Message"
fld public final static java.lang.String REQUESTPROTECTIONAUTHRECIPIENT = "RequestProtectionAuthRecipient"
fld public final static java.lang.String REQUESTPROTECTIONAUTHSOURCE = "RequestProtectionAuthSource"
fld public final static java.lang.String REQUEST_PROTECTION = "RequestProtection"
fld public final static java.lang.String RESPONSEPROTECTIONAUTHRECIPIENT = "ResponseProtectionAuthRecipient"
fld public final static java.lang.String RESPONSEPROTECTIONAUTHSOURCE = "ResponseProtectionAuthSource"
fld public final static java.lang.String RESPONSE_PROTECTION = "ResponseProtection"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract boolean isRequestProtection()
meth public abstract boolean isResponseProtection()
meth public abstract int addMessage(org.netbeans.modules.j2ee.sun.dd.api.common.Message)
meth public abstract int removeMessage(org.netbeans.modules.j2ee.sun.dd.api.common.Message)
meth public abstract int sizeMessage()
meth public abstract java.lang.String getRequestProtectionAuthRecipient()
meth public abstract java.lang.String getRequestProtectionAuthSource()
meth public abstract java.lang.String getResponseProtectionAuthRecipient()
meth public abstract java.lang.String getResponseProtectionAuthSource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.Message getMessage(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.Message newMessage()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.Message[] getMessage()
meth public abstract void setMessage(int,org.netbeans.modules.j2ee.sun.dd.api.common.Message)
meth public abstract void setMessage(org.netbeans.modules.j2ee.sun.dd.api.common.Message[])
meth public abstract void setRequestProtection(boolean)
meth public abstract void setRequestProtectionAuthRecipient(java.lang.String)
meth public abstract void setRequestProtectionAuthSource(java.lang.String)
meth public abstract void setResponseProtection(boolean)
meth public abstract void setResponseProtectionAuthRecipient(java.lang.String)
meth public abstract void setResponseProtectionAuthSource(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding
fld public final static java.lang.String AUTHLAYER = "AuthLayer"
fld public final static java.lang.String MESSAGE_SECURITY = "MessageSecurity"
fld public final static java.lang.String PROVIDERID = "ProviderId"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addMessageSecurity(org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurity)
meth public abstract int removeMessageSecurity(org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurity)
meth public abstract int sizeMessageSecurity()
meth public abstract java.lang.String getAuthLayer()
meth public abstract java.lang.String getProviderId()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurity getMessageSecurity(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurity newMessageSecurity()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurity[] getMessageSecurity()
meth public abstract void setAuthLayer(java.lang.String)
meth public abstract void setMessageSecurity(int,org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurity)
meth public abstract void setMessageSecurity(org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurity[])
meth public abstract void setProviderId(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams
fld public final static java.lang.String METHOD_PARAM = "MethodParam"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addMethodParam(java.lang.String)
meth public abstract int removeMethodParam(java.lang.String)
meth public abstract int sizeMethodParam()
meth public abstract java.lang.String getMethodParam(int)
meth public abstract java.lang.String[] getMethodParam()
meth public abstract void setMethodParam(int,java.lang.String)
meth public abstract void setMethodParam(java.lang.String[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.PluginData
fld public final static java.lang.String AUTO_GENERATE_SQL = "AutoGenerateSql"
fld public final static java.lang.String CLIENT_ARGS = "ClientArgs"
fld public final static java.lang.String CLIENT_JAR_PATH = "ClientJarPath"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getAutoGenerateSql()
meth public abstract java.lang.String getClientArgs()
meth public abstract java.lang.String getClientJarPath()
meth public abstract void setAutoGenerateSql(java.lang.String)
meth public abstract void setClientArgs(java.lang.String)
meth public abstract void setClientJarPath(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo
fld public final static java.lang.String CALL_PROPERTY = "CallProperty"
fld public final static java.lang.String MESSAGE_SECURITY_BINDING = "MessageSecurityBinding"
fld public final static java.lang.String SERVICE_ENDPOINT_INTERFACE = "ServiceEndpointInterface"
fld public final static java.lang.String STUB_PROPERTY = "StubProperty"
fld public final static java.lang.String VERSION_SERVER_8_0 = "Server 8.0"
fld public final static java.lang.String WSDL_PORT = "WsdlPort"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addCallProperty(org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty)
meth public abstract int addStubProperty(org.netbeans.modules.j2ee.sun.dd.api.common.StubProperty)
meth public abstract int removeCallProperty(org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty)
meth public abstract int removeStubProperty(org.netbeans.modules.j2ee.sun.dd.api.common.StubProperty)
meth public abstract int sizeCallProperty()
meth public abstract int sizeStubProperty()
meth public abstract java.lang.String getServiceEndpointInterface()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty getCallProperty(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty newCallProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty[] getCallProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding getMessageSecurityBinding() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding newMessageSecurityBinding() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.StubProperty getStubProperty(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.StubProperty newStubProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.StubProperty[] getStubProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.WsdlPort getWsdlPort()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.WsdlPort newWsdlPort()
meth public abstract void setCallProperty(int,org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty)
meth public abstract void setCallProperty(org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty[])
meth public abstract void setMessageSecurityBinding(org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setServiceEndpointInterface(java.lang.String)
meth public abstract void setStubProperty(int,org.netbeans.modules.j2ee.sun.dd.api.common.StubProperty)
meth public abstract void setStubProperty(org.netbeans.modules.j2ee.sun.dd.api.common.StubProperty[])
meth public abstract void setWsdlPort(org.netbeans.modules.j2ee.sun.dd.api.common.WsdlPort)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.PropertyElement
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String VALUE = "Value"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()
meth public abstract void setName(java.lang.String)
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef
fld public final static java.lang.String JNDI_NAME = "JndiName"
fld public final static java.lang.String RESOURCE_ENV_REF_NAME = "ResourceEnvRefName"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getResourceEnvRefName()
meth public abstract void setJndiName(java.lang.String)
meth public abstract void setResourceEnvRefName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef
fld public final static java.lang.String DEFAULT_RESOURCE_PRINCIPAL = "DefaultResourcePrincipal"
fld public final static java.lang.String JNDI_NAME = "JndiName"
fld public final static java.lang.String RES_REF_NAME = "ResRefName"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getResRefName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal getDefaultResourcePrincipal()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal newDefaultResourcePrincipal()
meth public abstract void setDefaultResourcePrincipal(org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal)
meth public abstract void setJndiName(java.lang.String)
meth public abstract void setResRefName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping
fld public final static java.lang.String GROUP_NAME = "GroupName"
fld public final static java.lang.String PRINCIPALNAMECLASSNAME = "PrincipalNameClassName"
fld public final static java.lang.String PRINCIPAL_NAME = "PrincipalName"
fld public final static java.lang.String ROLE_NAME = "RoleName"
fld public final static java.lang.String VERSION_SERVER_8_0 = "Server 8.0"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addGroupName(java.lang.String)
meth public abstract int addPrincipalName(java.lang.String)
meth public abstract int removeGroupName(java.lang.String)
meth public abstract int removePrincipalName(java.lang.String)
meth public abstract int sizeGroupName()
meth public abstract int sizePrincipalName()
meth public abstract int sizePrincipalNameClassName() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getGroupName(int)
meth public abstract java.lang.String getPrincipalName(int)
meth public abstract java.lang.String getPrincipalNameClassName(int) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getRoleName()
meth public abstract java.lang.String[] getGroupName()
meth public abstract java.lang.String[] getPrincipalName()
meth public abstract void setGroupName(int,java.lang.String)
meth public abstract void setGroupName(java.lang.String[])
meth public abstract void setPrincipalName(int,java.lang.String)
meth public abstract void setPrincipalName(java.lang.String[])
meth public abstract void setPrincipalNameClassName(int,java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setRoleName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.ServiceQname
fld public final static java.lang.String LOCALPART = "Localpart"
fld public final static java.lang.String NAMESPACEURI = "NamespaceURI"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getLocalpart()
meth public abstract java.lang.String getNamespaceURI()
meth public abstract void setLocalpart(java.lang.String)
meth public abstract void setNamespaceURI(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef
fld public final static java.lang.String CALL_PROPERTY = "CallProperty"
fld public final static java.lang.String PORT_INFO = "PortInfo"
fld public final static java.lang.String SERVICE_IMPL_CLASS = "ServiceImplClass"
fld public final static java.lang.String SERVICE_QNAME = "ServiceQname"
fld public final static java.lang.String SERVICE_REF_NAME = "ServiceRefName"
fld public final static java.lang.String WSDL_OVERRIDE = "WsdlOverride"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addCallProperty(org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty)
meth public abstract int addPortInfo(org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo)
meth public abstract int removeCallProperty(org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty)
meth public abstract int removePortInfo(org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo)
meth public abstract int sizeCallProperty()
meth public abstract int sizePortInfo()
meth public abstract java.lang.String getServiceImplClass()
meth public abstract java.lang.String getServiceRefName()
meth public abstract java.lang.String getWsdlOverride()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty getCallProperty(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty newCallProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty[] getCallProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo getPortInfo(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo newPortInfo()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo[] getPortInfo()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ServiceQname getServiceQname()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ServiceQname newServiceQname()
meth public abstract void setCallProperty(int,org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty)
meth public abstract void setCallProperty(org.netbeans.modules.j2ee.sun.dd.api.common.CallProperty[])
meth public abstract void setPortInfo(int,org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo)
meth public abstract void setPortInfo(org.netbeans.modules.j2ee.sun.dd.api.common.PortInfo[])
meth public abstract void setServiceImplClass(java.lang.String)
meth public abstract void setServiceQname(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceQname)
meth public abstract void setServiceRefName(java.lang.String)
meth public abstract void setWsdlOverride(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.StubProperty
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String VALUE = "Value"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()
meth public abstract void setName(java.lang.String)
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription
fld public final static java.lang.String WEBSERVICE_DESCRIPTION_NAME = "WebserviceDescriptionName"
fld public final static java.lang.String WSDL_PUBLISH_LOCATION = "WsdlPublishLocation"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getWebserviceDescriptionName()
meth public abstract java.lang.String getWsdlPublishLocation()
meth public abstract void setWebserviceDescriptionName(java.lang.String)
meth public abstract void setWsdlPublishLocation(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint
fld public final static java.lang.String DEBUGGING_ENABLED = "DebuggingEnabled"
fld public final static java.lang.String ENDPOINT_ADDRESS_URI = "EndpointAddressUri"
fld public final static java.lang.String LOGIN_CONFIG = "LoginConfig"
fld public final static java.lang.String MESSAGE_SECURITY_BINDING = "MessageSecurityBinding"
fld public final static java.lang.String PORT_COMPONENT_NAME = "PortComponentName"
fld public final static java.lang.String SERVICE_QNAME = "ServiceQname"
fld public final static java.lang.String SERVLET_IMPL_CLASS = "ServletImplClass"
fld public final static java.lang.String TIE_CLASS = "TieClass"
fld public final static java.lang.String TRANSPORT_GUARANTEE = "TransportGuarantee"
fld public final static java.lang.String VERSION_SERVER_8_0 = "Server 8.0"
fld public final static java.lang.String VERSION_SERVER_8_1 = "Server 8.1"
fld public final static java.lang.String VERSION_SERVER_9_0 = "Server 9.0"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getDebuggingEnabled() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getEndpointAddressUri()
meth public abstract java.lang.String getPortComponentName()
meth public abstract java.lang.String getServletImplClass()
meth public abstract java.lang.String getTieClass()
meth public abstract java.lang.String getTransportGuarantee()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.LoginConfig getLoginConfig()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.LoginConfig newLoginConfig()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding getMessageSecurityBinding() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding newMessageSecurityBinding() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ServiceQname getServiceQname()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ServiceQname newServiceQname()
meth public abstract void setDebuggingEnabled(java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setEndpointAddressUri(java.lang.String)
meth public abstract void setLoginConfig(org.netbeans.modules.j2ee.sun.dd.api.common.LoginConfig)
meth public abstract void setMessageSecurityBinding(org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setPortComponentName(java.lang.String)
meth public abstract void setServiceQname(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceQname)
meth public abstract void setServletImplClass(java.lang.String)
meth public abstract void setTieClass(java.lang.String)
meth public abstract void setTransportGuarantee(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.common.WsdlPort
fld public final static java.lang.String LOCALPART = "Localpart"
fld public final static java.lang.String NAMESPACEURI = "NamespaceURI"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getLocalpart()
meth public abstract java.lang.String getNamespaceURI()
meth public abstract void setLocalpart(java.lang.String)
meth public abstract void setNamespaceURI(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfig
fld public final static java.lang.String ACTIVATION_CONFIG_PROPERTY = "ActivationConfigProperty"
fld public final static java.lang.String DESCRIPTION = "Description"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addActivationConfigProperty(org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfigProperty)
meth public abstract int removeActivationConfigProperty(org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfigProperty)
meth public abstract int sizeActivationConfigProperty()
meth public abstract java.lang.String getDescription()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfigProperty getActivationConfigProperty(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfigProperty newActivationConfigProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfigProperty[] getActivationConfigProperty()
meth public abstract void setActivationConfigProperty(int,org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfigProperty)
meth public abstract void setActivationConfigProperty(org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfigProperty[])
meth public abstract void setDescription(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfigProperty
fld public final static java.lang.String ACTIVATION_CONFIG_PROPERTY_NAME = "ActivationConfigPropertyName"
fld public final static java.lang.String ACTIVATION_CONFIG_PROPERTY_VALUE = "ActivationConfigPropertyValue"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getActivationConfigPropertyName()
meth public abstract java.lang.String getActivationConfigPropertyValue()
meth public abstract void setActivationConfigPropertyName(java.lang.String)
meth public abstract void setActivationConfigPropertyValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.AsContext
fld public final static java.lang.String AUTH_METHOD = "AuthMethod"
fld public final static java.lang.String REALM = "Realm"
fld public final static java.lang.String REQUIRED = "Required"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getAuthMethod()
meth public abstract java.lang.String getRealm()
meth public abstract java.lang.String getRequired()
meth public abstract void setAuthMethod(java.lang.String)
meth public abstract void setRealm(java.lang.String)
meth public abstract void setRequired(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanCache
fld public final static java.lang.String CACHE_IDLE_TIMEOUT_IN_SECONDS = "CacheIdleTimeoutInSeconds"
fld public final static java.lang.String IS_CACHE_OVERFLOW_ALLOWED = "IsCacheOverflowAllowed"
fld public final static java.lang.String MAX_CACHE_SIZE = "MaxCacheSize"
fld public final static java.lang.String REMOVAL_TIMEOUT_IN_SECONDS = "RemovalTimeoutInSeconds"
fld public final static java.lang.String RESIZE_QUANTITY = "ResizeQuantity"
fld public final static java.lang.String VICTIM_SELECTION_POLICY = "VictimSelectionPolicy"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getCacheIdleTimeoutInSeconds()
meth public abstract java.lang.String getIsCacheOverflowAllowed()
meth public abstract java.lang.String getMaxCacheSize()
meth public abstract java.lang.String getRemovalTimeoutInSeconds()
meth public abstract java.lang.String getResizeQuantity()
meth public abstract java.lang.String getVictimSelectionPolicy()
meth public abstract void setCacheIdleTimeoutInSeconds(java.lang.String)
meth public abstract void setIsCacheOverflowAllowed(java.lang.String)
meth public abstract void setMaxCacheSize(java.lang.String)
meth public abstract void setRemovalTimeoutInSeconds(java.lang.String)
meth public abstract void setResizeQuantity(java.lang.String)
meth public abstract void setVictimSelectionPolicy(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanPool
fld public final static java.lang.String MAX_POOL_SIZE = "MaxPoolSize"
fld public final static java.lang.String MAX_WAIT_TIME_IN_MILLIS = "MaxWaitTimeInMillis"
fld public final static java.lang.String POOL_IDLE_TIMEOUT_IN_SECONDS = "PoolIdleTimeoutInSeconds"
fld public final static java.lang.String RESIZE_QUANTITY = "ResizeQuantity"
fld public final static java.lang.String STEADY_POOL_SIZE = "SteadyPoolSize"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getMaxPoolSize()
meth public abstract java.lang.String getMaxWaitTimeInMillis()
meth public abstract java.lang.String getPoolIdleTimeoutInSeconds()
meth public abstract java.lang.String getResizeQuantity()
meth public abstract java.lang.String getSteadyPoolSize()
meth public abstract void setMaxPoolSize(java.lang.String)
meth public abstract void setMaxWaitTimeInMillis(java.lang.String)
meth public abstract void setPoolIdleTimeoutInSeconds(java.lang.String)
meth public abstract void setResizeQuantity(java.lang.String)
meth public abstract void setSteadyPoolSize(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.CheckpointAtEndOfMethod
fld public final static java.lang.String METHOD = "Method"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addMethod(org.netbeans.modules.j2ee.sun.dd.api.ejb.Method)
meth public abstract int removeMethod(org.netbeans.modules.j2ee.sun.dd.api.ejb.Method)
meth public abstract int sizeMethod()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Method getMethod(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Method newMethod()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Method[] getMethod()
meth public abstract void setMethod(int,org.netbeans.modules.j2ee.sun.dd.api.ejb.Method)
meth public abstract void setMethod(org.netbeans.modules.j2ee.sun.dd.api.ejb.Method[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.CheckpointedMethods
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addMethod(org.netbeans.modules.j2ee.sun.dd.api.ejb.Method)
meth public abstract int removeMethod(org.netbeans.modules.j2ee.sun.dd.api.ejb.Method)
meth public abstract int sizeMethod()
meth public abstract java.lang.String getDescription()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Method getMethod(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Method[] getMethod()
meth public abstract void setDescription(java.lang.String)
meth public abstract void setMethod(int,org.netbeans.modules.j2ee.sun.dd.api.ejb.Method)
meth public abstract void setMethod(org.netbeans.modules.j2ee.sun.dd.api.ejb.Method[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.Cmp
fld public final static java.lang.String IS_ONE_ONE_CMP = "IsOneOneCmp"
fld public final static java.lang.String MAPPING_PROPERTIES = "MappingProperties"
fld public final static java.lang.String ONE_ONE_FINDERS = "OneOneFinders"
fld public final static java.lang.String PREFETCH_DISABLED = "PrefetchDisabled"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getIsOneOneCmp()
meth public abstract java.lang.String getMappingProperties()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.OneOneFinders getOneOneFinders()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.OneOneFinders newOneOneFinders()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PrefetchDisabled getPrefetchDisabled() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PrefetchDisabled newPrefetchDisabled() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setIsOneOneCmp(java.lang.String)
meth public abstract void setMappingProperties(java.lang.String)
meth public abstract void setOneOneFinders(org.netbeans.modules.j2ee.sun.dd.api.ejb.OneOneFinders)
meth public abstract void setPrefetchDisabled(org.netbeans.modules.j2ee.sun.dd.api.ejb.PrefetchDisabled) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.CmpResource
fld public final static java.lang.String CREATE_TABLES_AT_DEPLOY = "CreateTablesAtDeploy"
fld public final static java.lang.String DATABASE_VENDOR_NAME = "DatabaseVendorName"
fld public final static java.lang.String DEFAULT_RESOURCE_PRINCIPAL = "DefaultResourcePrincipal"
fld public final static java.lang.String DROP_TABLES_AT_UNDEPLOY = "DropTablesAtUndeploy"
fld public final static java.lang.String JNDI_NAME = "JndiName"
fld public final static java.lang.String PROPERTY = "PropertyElement"
fld public final static java.lang.String SCHEMA_GENERATOR_PROPERTIES = "SchemaGeneratorProperties"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement)
meth public abstract int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement)
meth public abstract int sizePropertyElement()
meth public abstract java.lang.String getCreateTablesAtDeploy()
meth public abstract java.lang.String getDatabaseVendorName()
meth public abstract java.lang.String getDropTablesAtUndeploy()
meth public abstract java.lang.String getJndiName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal getDefaultResourcePrincipal()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal newDefaultResourcePrincipal()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement getPropertyElement(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement newPropertyElement()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement[] getPropertyElement()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.SchemaGeneratorProperties getSchemaGeneratorProperties()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.SchemaGeneratorProperties newSchemaGeneratorProperties()
meth public abstract void setCreateTablesAtDeploy(java.lang.String)
meth public abstract void setDatabaseVendorName(java.lang.String)
meth public abstract void setDefaultResourcePrincipal(org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal)
meth public abstract void setDropTablesAtUndeploy(java.lang.String)
meth public abstract void setJndiName(java.lang.String)
meth public abstract void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement)
meth public abstract void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement[])
meth public abstract void setSchemaGeneratorProperties(org.netbeans.modules.j2ee.sun.dd.api.ejb.SchemaGeneratorProperties)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb
fld public final static java.lang.String AVAILABILITYENABLED = "AvailabilityEnabled"
fld public final static java.lang.String BEAN_CACHE = "BeanCache"
fld public final static java.lang.String BEAN_POOL = "BeanPool"
fld public final static java.lang.String CHECKPOINTED_METHODS = "CheckpointedMethods"
fld public final static java.lang.String CHECKPOINT_AT_END_OF_METHOD = "CheckpointAtEndOfMethod"
fld public final static java.lang.String CMP = "Cmp"
fld public final static java.lang.String CMT_TIMEOUT_IN_SECONDS = "CmtTimeoutInSeconds"
fld public final static java.lang.String COMMIT_OPTION = "CommitOption"
fld public final static java.lang.String EJB_NAME = "EjbName"
fld public final static java.lang.String EJB_REF = "EjbRef"
fld public final static java.lang.String FLUSH_AT_END_OF_METHOD = "FlushAtEndOfMethod"
fld public final static java.lang.String GEN_CLASSES = "GenClasses"
fld public final static java.lang.String IOR_SECURITY_CONFIG = "IorSecurityConfig"
fld public final static java.lang.String IS_READ_ONLY_BEAN = "IsReadOnlyBean"
fld public final static java.lang.String JMS_DURABLE_SUBSCRIPTION_NAME = "JmsDurableSubscriptionName"
fld public final static java.lang.String JMS_MAX_MESSAGES_LOAD = "JmsMaxMessagesLoad"
fld public final static java.lang.String JNDI_NAME = "JndiName"
fld public final static java.lang.String MDB_CONNECTION_FACTORY = "MdbConnectionFactory"
fld public final static java.lang.String MDB_RESOURCE_ADAPTER = "MdbResourceAdapter"
fld public final static java.lang.String MESSAGE_DESTINATION_REF = "MessageDestinationRef"
fld public final static java.lang.String PASS_BY_REFERENCE = "PassByReference"
fld public final static java.lang.String PRINCIPAL = "Principal"
fld public final static java.lang.String REFRESH_PERIOD_IN_SECONDS = "RefreshPeriodInSeconds"
fld public final static java.lang.String RESOURCE_ENV_REF = "ResourceEnvRef"
fld public final static java.lang.String RESOURCE_REF = "ResourceRef"
fld public final static java.lang.String SERVICE_REF = "ServiceRef"
fld public final static java.lang.String WEBSERVICE_ENDPOINT = "WebserviceEndpoint"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addEjbRef(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef)
meth public abstract int addMessageDestinationRef(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int addResourceEnvRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef)
meth public abstract int addResourceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef)
meth public abstract int addServiceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef)
meth public abstract int addWebserviceEndpoint(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint)
meth public abstract int removeEjbRef(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef)
meth public abstract int removeMessageDestinationRef(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int removeResourceEnvRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef)
meth public abstract int removeResourceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef)
meth public abstract int removeServiceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef)
meth public abstract int removeWebserviceEndpoint(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint)
meth public abstract int sizeEjbRef()
meth public abstract int sizeMessageDestinationRef() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int sizeResourceEnvRef()
meth public abstract int sizeResourceRef()
meth public abstract int sizeServiceRef()
meth public abstract int sizeWebserviceEndpoint()
meth public abstract java.lang.String getAvailabilityEnabled() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getCheckpointedMethods() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getCmtTimeoutInSeconds()
meth public abstract java.lang.String getCommitOption()
meth public abstract java.lang.String getEjbName()
meth public abstract java.lang.String getIsReadOnlyBean()
meth public abstract java.lang.String getJmsDurableSubscriptionName()
meth public abstract java.lang.String getJmsMaxMessagesLoad()
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getPassByReference()
meth public abstract java.lang.String getRefreshPeriodInSeconds()
meth public abstract java.lang.String getUseThreadPoolId()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef getEjbRef(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef newEjbRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef[] getEjbRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef getMessageDestinationRef(int) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef newMessageDestinationRef() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef[] getMessageDestinationRef() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef getResourceEnvRef(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef newResourceEnvRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef[] getResourceEnvRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef getResourceRef(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef newResourceRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef[] getResourceRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef getServiceRef(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef newServiceRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef[] getServiceRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint getWebserviceEndpoint(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint newWebserviceEndpoint()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint[] getWebserviceEndpoint()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanCache getBeanCache()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanCache newBeanCache()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanPool getBeanPool()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanPool newBeanPool()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.CheckpointAtEndOfMethod getCheckpointAtEndOfMethod() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.CheckpointAtEndOfMethod newCheckpointAtEndOfMethod() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Cmp getCmp()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Cmp newCmp()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.FlushAtEndOfMethod getFlushAtEndOfMethod() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.FlushAtEndOfMethod newFlushAtEndOfMethod() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.GenClasses getGenClasses()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.GenClasses newGenClasses()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.IorSecurityConfig getIorSecurityConfig()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.IorSecurityConfig newIorSecurityConfig()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbConnectionFactory getMdbConnectionFactory()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbConnectionFactory newMdbConnectionFactory()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbResourceAdapter getMdbResourceAdapter()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbResourceAdapter newMdbResourceAdapter()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Principal getPrincipal()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Principal newPrincipal()
meth public abstract void setAvailabilityEnabled(java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setBeanCache(org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanCache)
meth public abstract void setBeanPool(org.netbeans.modules.j2ee.sun.dd.api.ejb.BeanPool)
meth public abstract void setCheckpointAtEndOfMethod(org.netbeans.modules.j2ee.sun.dd.api.ejb.CheckpointAtEndOfMethod) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setCheckpointedMethods(java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setCmp(org.netbeans.modules.j2ee.sun.dd.api.ejb.Cmp)
meth public abstract void setCmtTimeoutInSeconds(java.lang.String)
meth public abstract void setCommitOption(java.lang.String)
meth public abstract void setEjbName(java.lang.String)
meth public abstract void setEjbRef(int,org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef)
meth public abstract void setEjbRef(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef[])
meth public abstract void setFlushAtEndOfMethod(org.netbeans.modules.j2ee.sun.dd.api.ejb.FlushAtEndOfMethod) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setGenClasses(org.netbeans.modules.j2ee.sun.dd.api.ejb.GenClasses)
meth public abstract void setIorSecurityConfig(org.netbeans.modules.j2ee.sun.dd.api.ejb.IorSecurityConfig)
meth public abstract void setIsReadOnlyBean(java.lang.String)
meth public abstract void setJmsDurableSubscriptionName(java.lang.String)
meth public abstract void setJmsMaxMessagesLoad(java.lang.String)
meth public abstract void setJndiName(java.lang.String)
meth public abstract void setMdbConnectionFactory(org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbConnectionFactory)
meth public abstract void setMdbResourceAdapter(org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbResourceAdapter)
meth public abstract void setMessageDestinationRef(int,org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setMessageDestinationRef(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef[]) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setPassByReference(java.lang.String)
meth public abstract void setPrincipal(org.netbeans.modules.j2ee.sun.dd.api.ejb.Principal)
meth public abstract void setRefreshPeriodInSeconds(java.lang.String)
meth public abstract void setResourceEnvRef(int,org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef)
meth public abstract void setResourceEnvRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef[])
meth public abstract void setResourceRef(int,org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef)
meth public abstract void setResourceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef[])
meth public abstract void setServiceRef(int,org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef)
meth public abstract void setServiceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef[])
meth public abstract void setUseThreadPoolId(java.lang.String)
meth public abstract void setWebserviceEndpoint(int,org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint)
meth public abstract void setWebserviceEndpoint(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans
fld public final static java.lang.String CMP_RESOURCE = "CmpResource"
fld public final static java.lang.String EJB = "Ejb"
fld public final static java.lang.String MESSAGE_DESTINATION = "MessageDestination"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String PM_DESCRIPTORS = "PmDescriptors"
fld public final static java.lang.String UNIQUE_ID = "UniqueId"
fld public final static java.lang.String WEBSERVICE_DESCRIPTION = "WebserviceDescription"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addEjb(org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb)
meth public abstract int addMessageDestination(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination)
meth public abstract int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int addWebserviceDescription(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription)
meth public abstract int removeEjb(org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb)
meth public abstract int removeMessageDestination(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination)
meth public abstract int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int removeWebserviceDescription(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription)
meth public abstract int sizeEjb()
meth public abstract int sizeMessageDestination()
meth public abstract int sizePropertyElement() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int sizeWebserviceDescription()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getUniqueId()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination getMessageDestination(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination newMessageDestination()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination[] getMessageDestination()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription getWebserviceDescription(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription newWebserviceDescription()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription[] getWebserviceDescription()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.CmpResource getCmpResource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.CmpResource newCmpResource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb getEjb(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb newEjb()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb[] getEjb()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptors getPmDescriptors()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptors newPmDescriptors()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement getPropertyElement(int) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement newPropertyElement() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement[] getPropertyElement() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setCmpResource(org.netbeans.modules.j2ee.sun.dd.api.ejb.CmpResource)
meth public abstract void setEjb(int,org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb)
meth public abstract void setEjb(org.netbeans.modules.j2ee.sun.dd.api.ejb.Ejb[])
meth public abstract void setMessageDestination(int,org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination)
meth public abstract void setMessageDestination(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination[])
meth public abstract void setName(java.lang.String)
meth public abstract void setPmDescriptors(org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptors)
meth public abstract void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement[]) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setUniqueId(java.lang.String)
meth public abstract void setWebserviceDescription(int,org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription)
meth public abstract void setWebserviceDescription(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.Finder
fld public final static java.lang.String METHOD_NAME = "MethodName"
fld public final static java.lang.String QUERY_FILTER = "QueryFilter"
fld public final static java.lang.String QUERY_ORDERING = "QueryOrdering"
fld public final static java.lang.String QUERY_PARAMS = "QueryParams"
fld public final static java.lang.String QUERY_VARIABLES = "QueryVariables"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getMethodName()
meth public abstract java.lang.String getQueryFilter()
meth public abstract java.lang.String getQueryOrdering()
meth public abstract java.lang.String getQueryParams()
meth public abstract java.lang.String getQueryVariables()
meth public abstract void setMethodName(java.lang.String)
meth public abstract void setQueryFilter(java.lang.String)
meth public abstract void setQueryOrdering(java.lang.String)
meth public abstract void setQueryParams(java.lang.String)
meth public abstract void setQueryVariables(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.FlushAtEndOfMethod
fld public final static java.lang.String METHOD = "Method"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addMethod(org.netbeans.modules.j2ee.sun.dd.api.ejb.Method)
meth public abstract int removeMethod(org.netbeans.modules.j2ee.sun.dd.api.ejb.Method)
meth public abstract int sizeMethod()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Method getMethod(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Method newMethod()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Method[] getMethod()
meth public abstract void setMethod(int,org.netbeans.modules.j2ee.sun.dd.api.ejb.Method)
meth public abstract void setMethod(org.netbeans.modules.j2ee.sun.dd.api.ejb.Method[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.GenClasses
fld public final static java.lang.String LOCAL_HOME_IMPL = "LocalHomeImpl"
fld public final static java.lang.String LOCAL_IMPL = "LocalImpl"
fld public final static java.lang.String REMOTE_HOME_IMPL = "RemoteHomeImpl"
fld public final static java.lang.String REMOTE_IMPL = "RemoteImpl"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getLocalHomeImpl()
meth public abstract java.lang.String getLocalImpl()
meth public abstract java.lang.String getRemoteHomeImpl()
meth public abstract java.lang.String getRemoteImpl()
meth public abstract void setLocalHomeImpl(java.lang.String)
meth public abstract void setLocalImpl(java.lang.String)
meth public abstract void setRemoteHomeImpl(java.lang.String)
meth public abstract void setRemoteImpl(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.IorSecurityConfig
fld public final static java.lang.String AS_CONTEXT = "AsContext"
fld public final static java.lang.String SAS_CONTEXT = "SasContext"
fld public final static java.lang.String TRANSPORT_CONFIG = "TransportConfig"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.AsContext getAsContext()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.AsContext newAsContext()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.SasContext getSasContext()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.SasContext newSasContext()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.TransportConfig getTransportConfig()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.TransportConfig newTransportConfig()
meth public abstract void setAsContext(org.netbeans.modules.j2ee.sun.dd.api.ejb.AsContext)
meth public abstract void setSasContext(org.netbeans.modules.j2ee.sun.dd.api.ejb.SasContext)
meth public abstract void setTransportConfig(org.netbeans.modules.j2ee.sun.dd.api.ejb.TransportConfig)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbConnectionFactory
fld public final static java.lang.String DEFAULT_RESOURCE_PRINCIPAL = "DefaultResourcePrincipal"
fld public final static java.lang.String JNDI_NAME = "JndiName"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getJndiName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal getDefaultResourcePrincipal()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal newDefaultResourcePrincipal()
meth public abstract void setDefaultResourcePrincipal(org.netbeans.modules.j2ee.sun.dd.api.common.DefaultResourcePrincipal)
meth public abstract void setJndiName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.MdbResourceAdapter
fld public final static java.lang.String ACTIVATION_CONFIG = "ActivationConfig"
fld public final static java.lang.String RESOURCE_ADAPTER_MID = "ResourceAdapterMid"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getResourceAdapterMid()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfig getActivationConfig()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfig newActivationConfig()
meth public abstract void setActivationConfig(org.netbeans.modules.j2ee.sun.dd.api.ejb.ActivationConfig)
meth public abstract void setResourceAdapterMid(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.Method
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String EJB_NAME = "EjbName"
fld public final static java.lang.String METHOD_INTF = "MethodIntf"
fld public final static java.lang.String METHOD_NAME = "MethodName"
fld public final static java.lang.String METHOD_PARAMS = "MethodParams"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getEjbName()
meth public abstract java.lang.String getMethodIntf()
meth public abstract java.lang.String getMethodName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams getMethodParams()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams newMethodParams()
meth public abstract void setDescription(java.lang.String)
meth public abstract void setEjbName(java.lang.String)
meth public abstract void setMethodIntf(java.lang.String)
meth public abstract void setMethodName(java.lang.String)
meth public abstract void setMethodParams(org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.OneOneFinders
fld public final static java.lang.String FINDER = "Finder"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addFinder(org.netbeans.modules.j2ee.sun.dd.api.ejb.Finder)
meth public abstract int removeFinder(org.netbeans.modules.j2ee.sun.dd.api.ejb.Finder)
meth public abstract int sizeFinder()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Finder getFinder(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Finder newFinder()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.Finder[] getFinder()
meth public abstract void setFinder(int,org.netbeans.modules.j2ee.sun.dd.api.ejb.Finder)
meth public abstract void setFinder(org.netbeans.modules.j2ee.sun.dd.api.ejb.Finder[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptor
fld public final static java.lang.String PM_CLASS_GENERATOR = "PmClassGenerator"
fld public final static java.lang.String PM_CONFIG = "PmConfig"
fld public final static java.lang.String PM_IDENTIFIER = "PmIdentifier"
fld public final static java.lang.String PM_MAPPING_FACTORY = "PmMappingFactory"
fld public final static java.lang.String PM_VERSION = "PmVersion"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getPmClassGenerator()
meth public abstract java.lang.String getPmConfig()
meth public abstract java.lang.String getPmIdentifier()
meth public abstract java.lang.String getPmMappingFactory()
meth public abstract java.lang.String getPmVersion()
meth public abstract void setPmClassGenerator(java.lang.String)
meth public abstract void setPmConfig(java.lang.String)
meth public abstract void setPmIdentifier(java.lang.String)
meth public abstract void setPmMappingFactory(java.lang.String)
meth public abstract void setPmVersion(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptors
fld public final static java.lang.String PM_DESCRIPTOR = "PmDescriptor"
fld public final static java.lang.String PM_INUSE = "PmInuse"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addPmDescriptor(org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptor)
meth public abstract int removePmDescriptor(org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptor)
meth public abstract int sizePmDescriptor()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptor getPmDescriptor(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptor newPmDescriptor()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptor[] getPmDescriptor()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PmInuse getPmInuse()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PmInuse newPmInuse()
meth public abstract void setPmDescriptor(int,org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptor)
meth public abstract void setPmDescriptor(org.netbeans.modules.j2ee.sun.dd.api.ejb.PmDescriptor[])
meth public abstract void setPmInuse(org.netbeans.modules.j2ee.sun.dd.api.ejb.PmInuse)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.PmInuse
fld public final static java.lang.String PM_IDENTIFIER = "PmIdentifier"
fld public final static java.lang.String PM_VERSION = "PmVersion"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getPmIdentifier()
meth public abstract java.lang.String getPmVersion()
meth public abstract void setPmIdentifier(java.lang.String)
meth public abstract void setPmVersion(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.PrefetchDisabled
fld public final static java.lang.String QUERY_METHOD = "QueryMethod"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addQueryMethod(org.netbeans.modules.j2ee.sun.dd.api.ejb.QueryMethod)
meth public abstract int removeQueryMethod(org.netbeans.modules.j2ee.sun.dd.api.ejb.QueryMethod)
meth public abstract int sizeQueryMethod()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.QueryMethod getQueryMethod(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.QueryMethod newQueryMethod()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.QueryMethod[] getQueryMethod()
meth public abstract void setQueryMethod(int,org.netbeans.modules.j2ee.sun.dd.api.ejb.QueryMethod)
meth public abstract void setQueryMethod(org.netbeans.modules.j2ee.sun.dd.api.ejb.QueryMethod[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.Principal
fld public final static java.lang.String NAME = "Name"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getName()
meth public abstract void setName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String VALUE = "Value"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()
meth public abstract void setName(java.lang.String)
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.QueryMethod
fld public final static java.lang.String METHOD_NAME = "MethodName"
fld public final static java.lang.String METHOD_PARAMS = "MethodParams"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getMethodName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams getMethodParams()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams newMethodParams()
meth public abstract void setMethodName(java.lang.String)
meth public abstract void setMethodParams(org.netbeans.modules.j2ee.sun.dd.api.common.MethodParams)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.SasContext
fld public final static java.lang.String CALLER_PROPAGATION = "CallerPropagation"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getCallerPropagation()
meth public abstract void setCallerPropagation(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.SchemaGeneratorProperties
fld public final static java.lang.String PROPERTY = "PropertyElement"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement)
meth public abstract int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement)
meth public abstract int sizePropertyElement()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement getPropertyElement(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement newPropertyElement()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement[] getPropertyElement()
meth public abstract void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement)
meth public abstract void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.ejb.PropertyElement[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.Session
fld public final static java.lang.String CHECKPOINTED_METHODS = "CheckpointedMethods"
fld public final static java.lang.String CHECKPOINT_LOCATION = "CheckpointLocation"
fld public final static java.lang.String QUICK_CHECKPOINT = "QuickCheckpoint"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getCheckpointLocation()
meth public abstract java.lang.String getQuickCheckpoint()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.CheckpointedMethods getCheckpointedMethods()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.CheckpointedMethods newCheckpointedMethods()
meth public abstract void setCheckpointLocation(java.lang.String)
meth public abstract void setCheckpointedMethods(org.netbeans.modules.j2ee.sun.dd.api.ejb.CheckpointedMethods)
meth public abstract void setQuickCheckpoint(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.SunEjbJar
fld public final static java.lang.String ENTERPRISE_BEANS = "EnterpriseBeans"
fld public final static java.lang.String SECURITY_ROLE_MAPPING = "SecurityRoleMapping"
fld public final static java.lang.String VERSION_2_0_0 = "2.00"
fld public final static java.lang.String VERSION_2_1_0 = "2.10"
fld public final static java.lang.String VERSION_2_1_1 = "2.11"
fld public final static java.lang.String VERSION_3_0_0 = "3.00"
fld public final static java.lang.String VERSION_3_0_1 = "3.01"
fld public final static java.lang.String VERSION_3_1_0 = "3.10"
fld public final static java.lang.String VERSION_3_1_1 = "3.11"
intf org.netbeans.modules.j2ee.sun.dd.api.RootInterface
meth public abstract int addSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping)
meth public abstract int removeSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping)
meth public abstract int sizeSecurityRoleMapping()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping getSecurityRoleMapping(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping newSecurityRoleMapping()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[] getSecurityRoleMapping()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans getEnterpriseBeans()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans newEnterpriseBeans()
meth public abstract void setEnterpriseBeans(org.netbeans.modules.j2ee.sun.dd.api.ejb.EnterpriseBeans)
meth public abstract void setSecurityRoleMapping(int,org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping)
meth public abstract void setSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.ejb.TransportConfig
fld public final static java.lang.String CONFIDENTIALITY = "Confidentiality"
fld public final static java.lang.String ESTABLISH_TRUST_IN_CLIENT = "EstablishTrustInClient"
fld public final static java.lang.String ESTABLISH_TRUST_IN_TARGET = "EstablishTrustInTarget"
fld public final static java.lang.String INTEGRITY = "Integrity"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getConfidentiality()
meth public abstract java.lang.String getEstablishTrustInClient()
meth public abstract java.lang.String getEstablishTrustInTarget()
meth public abstract java.lang.String getIntegrity()
meth public abstract void setConfidentiality(java.lang.String)
meth public abstract void setEstablishTrustInClient(java.lang.String)
meth public abstract void setEstablishTrustInTarget(java.lang.String)
meth public abstract void setIntegrity(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String JNDINAME = "JndiName"
fld public final static java.lang.String OBJECTTYPE = "ObjectType"
fld public final static java.lang.String PROPERTY = "PropertyElement"
fld public final static java.lang.String RESADAPTER = "ResAdapter"
fld public final static java.lang.String RESTYPE = "ResType"
meth public abstract int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int sizePropertyElement()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getEnabled()
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getObjectType()
meth public abstract java.lang.String getResAdapter()
meth public abstract java.lang.String getResType()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public abstract void setDescription(java.lang.String)
meth public abstract void setEnabled(java.lang.String)
meth public abstract void setJndiName(java.lang.String)
meth public abstract void setObjectType(java.lang.String)
meth public abstract void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public abstract void setResAdapter(java.lang.String)
meth public abstract void setResType(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool
fld public final static java.lang.String CONNECTIONDEFINITIONNAME = "ConnectionDefinitionName"
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String FAILALLCONNECTIONS = "FailAllConnections"
fld public final static java.lang.String IDLETIMEOUTINSECONDS = "IdleTimeoutInSeconds"
fld public final static java.lang.String ISCONNECTIONVALIDATIONREQUIRED = "IsConnectionValidationRequired"
fld public final static java.lang.String MAXPOOLSIZE = "MaxPoolSize"
fld public final static java.lang.String MAXWAITTIMEINMILLIS = "MaxWaitTimeInMillis"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String POOLRESIZEQUANTITY = "PoolResizeQuantity"
fld public final static java.lang.String PROPERTY = "PropertyElement"
fld public final static java.lang.String RESOURCEADAPTERNAME = "ResourceAdapterName"
fld public final static java.lang.String SECURITY_MAP = "SecurityMap"
fld public final static java.lang.String STEADYPOOLSIZE = "SteadyPoolSize"
fld public final static java.lang.String TRANSACTIONSUPPORT = "TransactionSupport"
meth public abstract int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int addSecurityMap(org.netbeans.modules.j2ee.sun.dd.api.serverresources.SecurityMap)
meth public abstract int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int removeSecurityMap(org.netbeans.modules.j2ee.sun.dd.api.serverresources.SecurityMap)
meth public abstract int sizePropertyElement()
meth public abstract int sizeSecurityMap()
meth public abstract java.lang.String getConnectionDefinitionName()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getFailAllConnections()
meth public abstract java.lang.String getIdleTimeoutInSeconds()
meth public abstract java.lang.String getIsConnectionValidationRequired()
meth public abstract java.lang.String getMaxPoolSize()
meth public abstract java.lang.String getMaxWaitTimeInMillis()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getPoolResizeQuantity()
meth public abstract java.lang.String getResourceAdapterName()
meth public abstract java.lang.String getSteadyPoolSize()
meth public abstract java.lang.String getTransactionSupport()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.SecurityMap getSecurityMap(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.SecurityMap newSecurityMap()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.SecurityMap[] getSecurityMap()
meth public abstract void setConnectionDefinitionName(java.lang.String)
meth public abstract void setDescription(java.lang.String)
meth public abstract void setFailAllConnections(java.lang.String)
meth public abstract void setIdleTimeoutInSeconds(java.lang.String)
meth public abstract void setIsConnectionValidationRequired(java.lang.String)
meth public abstract void setMaxPoolSize(java.lang.String)
meth public abstract void setMaxWaitTimeInMillis(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setPoolResizeQuantity(java.lang.String)
meth public abstract void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public abstract void setResourceAdapterName(java.lang.String)
meth public abstract void setSecurityMap(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.SecurityMap)
meth public abstract void setSecurityMap(org.netbeans.modules.j2ee.sun.dd.api.serverresources.SecurityMap[])
meth public abstract void setSteadyPoolSize(java.lang.String)
meth public abstract void setTransactionSupport(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String JNDINAME = "JndiName"
fld public final static java.lang.String OBJECTTYPE = "ObjectType"
fld public final static java.lang.String POOLNAME = "PoolName"
fld public final static java.lang.String PROPERTY = "PropertyElement"
meth public abstract int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int sizePropertyElement()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getEnabled()
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getObjectType()
meth public abstract java.lang.String getPoolName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public abstract void setDescription(java.lang.String)
meth public abstract void setEnabled(java.lang.String)
meth public abstract void setJndiName(java.lang.String)
meth public abstract void setObjectType(java.lang.String)
meth public abstract void setPoolName(java.lang.String)
meth public abstract void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String FACTORYCLASS = "FactoryClass"
fld public final static java.lang.String JNDINAME = "JndiName"
fld public final static java.lang.String OBJECTTYPE = "ObjectType"
fld public final static java.lang.String PROPERTY = "PropertyElement"
fld public final static java.lang.String RESTYPE = "ResType"
meth public abstract int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int sizePropertyElement()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getEnabled()
meth public abstract java.lang.String getFactoryClass()
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getObjectType()
meth public abstract java.lang.String getResType()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public abstract void setDescription(java.lang.String)
meth public abstract void setEnabled(java.lang.String)
meth public abstract void setFactoryClass(java.lang.String)
meth public abstract void setJndiName(java.lang.String)
meth public abstract void setObjectType(java.lang.String)
meth public abstract void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public abstract void setResType(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String FACTORYCLASS = "FactoryClass"
fld public final static java.lang.String JNDILOOKUPNAME = "JndiLookupName"
fld public final static java.lang.String JNDINAME = "JndiName"
fld public final static java.lang.String OBJECTTYPE = "ObjectType"
fld public final static java.lang.String PROPERTY = "PropertyElement"
fld public final static java.lang.String RESTYPE = "ResType"
meth public abstract int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int sizePropertyElement()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getEnabled()
meth public abstract java.lang.String getFactoryClass()
meth public abstract java.lang.String getJndiLookupName()
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getObjectType()
meth public abstract java.lang.String getResType()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public abstract void setDescription(java.lang.String)
meth public abstract void setEnabled(java.lang.String)
meth public abstract void setFactoryClass(java.lang.String)
meth public abstract void setJndiLookupName(java.lang.String)
meth public abstract void setJndiName(java.lang.String)
meth public abstract void setObjectType(java.lang.String)
meth public abstract void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public abstract void setResType(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool
fld public final static java.lang.String ALLOWNONCOMPONENTCALLERS = "AllowNonComponentCallers"
fld public final static java.lang.String CONNECTIONVALIDATIONMETHOD = "ConnectionValidationMethod"
fld public final static java.lang.String DATASOURCECLASSNAME = "DatasourceClassname"
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String FAILALLCONNECTIONS = "FailAllConnections"
fld public final static java.lang.String IDLETIMEOUTINSECONDS = "IdleTimeoutInSeconds"
fld public final static java.lang.String ISCONNECTIONVALIDATIONREQUIRED = "IsConnectionValidationRequired"
fld public final static java.lang.String ISISOLATIONLEVELGUARANTEED = "IsIsolationLevelGuaranteed"
fld public final static java.lang.String MAXPOOLSIZE = "MaxPoolSize"
fld public final static java.lang.String MAXWAITTIMEINMILLIS = "MaxWaitTimeInMillis"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String NONTRANSACTIONALCONNECTIONS = "NonTransactionalConnections"
fld public final static java.lang.String POOLRESIZEQUANTITY = "PoolResizeQuantity"
fld public final static java.lang.String PROPERTY = "PropertyElement"
fld public final static java.lang.String RESTYPE = "ResType"
fld public final static java.lang.String STEADYPOOLSIZE = "SteadyPoolSize"
fld public final static java.lang.String TRANSACTIONISOLATIONLEVEL = "TransactionIsolationLevel"
fld public final static java.lang.String VALIDATIONTABLENAME = "ValidationTableName"
meth public abstract int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int sizePropertyElement()
meth public abstract java.lang.String getAllowNonComponentCallers()
meth public abstract java.lang.String getConnectionValidationMethod()
meth public abstract java.lang.String getDatasourceClassname()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getFailAllConnections()
meth public abstract java.lang.String getIdleTimeoutInSeconds()
meth public abstract java.lang.String getIsConnectionValidationRequired()
meth public abstract java.lang.String getIsIsolationLevelGuaranteed()
meth public abstract java.lang.String getMaxPoolSize()
meth public abstract java.lang.String getMaxWaitTimeInMillis()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getNonTransactionalConnections()
meth public abstract java.lang.String getPoolResizeQuantity()
meth public abstract java.lang.String getResType()
meth public abstract java.lang.String getSteadyPoolSize()
meth public abstract java.lang.String getTransactionIsolationLevel()
meth public abstract java.lang.String getValidationTableName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public abstract void setAllowNonComponentCallers(java.lang.String)
meth public abstract void setConnectionValidationMethod(java.lang.String)
meth public abstract void setDatasourceClassname(java.lang.String)
meth public abstract void setDescription(java.lang.String)
meth public abstract void setFailAllConnections(java.lang.String)
meth public abstract void setIdleTimeoutInSeconds(java.lang.String)
meth public abstract void setIsConnectionValidationRequired(java.lang.String)
meth public abstract void setIsIsolationLevelGuaranteed(java.lang.String)
meth public abstract void setMaxPoolSize(java.lang.String)
meth public abstract void setMaxWaitTimeInMillis(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setNonTransactionalConnections(java.lang.String)
meth public abstract void setPoolResizeQuantity(java.lang.String)
meth public abstract void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public abstract void setResType(java.lang.String)
meth public abstract void setSteadyPoolSize(java.lang.String)
meth public abstract void setTransactionIsolationLevel(java.lang.String)
meth public abstract void setValidationTableName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String JNDINAME = "JndiName"
fld public final static java.lang.String OBJECTTYPE = "ObjectType"
fld public final static java.lang.String POOLNAME = "PoolName"
fld public final static java.lang.String PROPERTY = "PropertyElement"
meth public abstract int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int sizePropertyElement()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getEnabled()
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getObjectType()
meth public abstract java.lang.String getPoolName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public abstract void setDescription(java.lang.String)
meth public abstract void setEnabled(java.lang.String)
meth public abstract void setJndiName(java.lang.String)
meth public abstract void setObjectType(java.lang.String)
meth public abstract void setPoolName(java.lang.String)
meth public abstract void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.serverresources.JmsResource
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String JNDINAME = "JndiName"
fld public final static java.lang.String PROPERTY = "PropertyElement"
fld public final static java.lang.String RESTYPE = "ResType"
meth public abstract int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int sizePropertyElement()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getEnabled()
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getResType()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public abstract void setDescription(java.lang.String)
meth public abstract void setEnabled(java.lang.String)
meth public abstract void setJndiName(java.lang.String)
meth public abstract void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public abstract void setResType(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource
fld public final static java.lang.String DEBUG = "Debug"
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String FROM = "From"
fld public final static java.lang.String HOST = "Host"
fld public final static java.lang.String JNDINAME = "JndiName"
fld public final static java.lang.String OBJECTTYPE = "ObjectType"
fld public final static java.lang.String PROPERTY = "PropertyElement"
fld public final static java.lang.String STOREPROTOCOL = "StoreProtocol"
fld public final static java.lang.String STOREPROTOCOLCLASS = "StoreProtocolClass"
fld public final static java.lang.String TRANSPORTPROTOCOL = "TransportProtocol"
fld public final static java.lang.String TRANSPORTPROTOCOLCLASS = "TransportProtocolClass"
fld public final static java.lang.String USER = "User"
meth public abstract int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int sizePropertyElement()
meth public abstract java.lang.String getDebug()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getEnabled()
meth public abstract java.lang.String getFrom()
meth public abstract java.lang.String getHost()
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getObjectType()
meth public abstract java.lang.String getStoreProtocol()
meth public abstract java.lang.String getStoreProtocolClass()
meth public abstract java.lang.String getTransportProtocol()
meth public abstract java.lang.String getTransportProtocolClass()
meth public abstract java.lang.String getUser()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public abstract void setDebug(java.lang.String)
meth public abstract void setDescription(java.lang.String)
meth public abstract void setEnabled(java.lang.String)
meth public abstract void setFrom(java.lang.String)
meth public abstract void setHost(java.lang.String)
meth public abstract void setJndiName(java.lang.String)
meth public abstract void setObjectType(java.lang.String)
meth public abstract void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public abstract void setStoreProtocol(java.lang.String)
meth public abstract void setStoreProtocolClass(java.lang.String)
meth public abstract void setTransportProtocol(java.lang.String)
meth public abstract void setTransportProtocolClass(java.lang.String)
meth public abstract void setUser(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String FACTORYCLASS = "FactoryClass"
fld public final static java.lang.String JDBCRESOURCEJNDINAME = "JdbcResourceJndiName"
fld public final static java.lang.String JNDINAME = "JndiName"
fld public final static java.lang.String OBJECTTYPE = "ObjectType"
fld public final static java.lang.String PROPERTY = "PropertyElement"
meth public abstract int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int sizePropertyElement()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getEnabled()
meth public abstract java.lang.String getFactoryClass()
meth public abstract java.lang.String getJdbcResourceJndiName()
meth public abstract java.lang.String getJndiName()
meth public abstract java.lang.String getObjectType()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public abstract void setDescription(java.lang.String)
meth public abstract void setEnabled(java.lang.String)
meth public abstract void setFactoryClass(java.lang.String)
meth public abstract void setJdbcResourceJndiName(java.lang.String)
meth public abstract void setJndiName(java.lang.String)
meth public abstract void setObjectType(java.lang.String)
meth public abstract void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String VALUE = "Value"
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()
meth public abstract void setDescription(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String OBJECTTYPE = "ObjectType"
fld public final static java.lang.String PROPERTY = "PropertyElement"
fld public final static java.lang.String RESOURCEADAPTERNAME = "ResourceAdapterName"
fld public final static java.lang.String THREADPOOLIDS = "ThreadPoolIds"
meth public abstract int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract int sizePropertyElement()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getObjectType()
meth public abstract java.lang.String getResourceAdapterName()
meth public abstract java.lang.String getThreadPoolIds()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public abstract void setName(java.lang.String)
meth public abstract void setObjectType(java.lang.String)
meth public abstract void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public abstract void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public abstract void setResourceAdapterName(java.lang.String)
meth public abstract void setThreadPoolIds(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources
fld public final static java.lang.String ADMIN_OBJECT_RESOURCE = "AdminObjectResource"
fld public final static java.lang.String CONNECTOR_CONNECTION_POOL = "ConnectorConnectionPool"
fld public final static java.lang.String CONNECTOR_RESOURCE = "ConnectorResource"
fld public final static java.lang.String CUSTOM_RESOURCE = "CustomResource"
fld public final static java.lang.String EXTERNAL_JNDI_RESOURCE = "ExternalJndiResource"
fld public final static java.lang.String JDBC_CONNECTION_POOL = "JdbcConnectionPool"
fld public final static java.lang.String JDBC_RESOURCE = "JdbcResource"
fld public final static java.lang.String JMS_RESOURCE = "JmsResource"
fld public final static java.lang.String MAIL_RESOURCE = "MailResource"
fld public final static java.lang.String PERSISTENCE_MANAGER_FACTORY_RESOURCE = "PersistenceManagerFactoryResource"
fld public final static java.lang.String RESOURCE_ADAPTER_CONFIG = "ResourceAdapterConfig"
fld public final static java.lang.String VERSION_1_0 = "1.00"
fld public final static java.lang.String VERSION_1_2 = "1.20"
fld public final static java.lang.String VERSION_1_3 = "1.30"
fld public final static java.lang.String VERSION_1_5 = "1.50"
intf org.netbeans.modules.j2ee.sun.dd.api.RootInterface
meth public abstract int addAdminObjectResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource)
meth public abstract int addConnectorConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool)
meth public abstract int addConnectorResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource)
meth public abstract int addCustomResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource)
meth public abstract int addExternalJndiResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource)
meth public abstract int addJdbcConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool)
meth public abstract int addJdbcResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource)
meth public abstract int addMailResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource)
meth public abstract int addPersistenceManagerFactoryResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource)
meth public abstract int addResourceAdapterConfig(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig)
meth public abstract int removeAdminObjectResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource)
meth public abstract int removeConnectorConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool)
meth public abstract int removeConnectorResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource)
meth public abstract int removeCustomResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource)
meth public abstract int removeExternalJndiResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource)
meth public abstract int removeJdbcConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool)
meth public abstract int removeJdbcResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource)
meth public abstract int removeMailResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource)
meth public abstract int removePersistenceManagerFactoryResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource)
meth public abstract int removeResourceAdapterConfig(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig)
meth public abstract int sizeAdminObjectResource()
meth public abstract int sizeConnectorConnectionPool()
meth public abstract int sizeConnectorResource()
meth public abstract int sizeCustomResource()
meth public abstract int sizeExternalJndiResource()
meth public abstract int sizeJdbcConnectionPool()
meth public abstract int sizeJdbcResource()
meth public abstract int sizeMailResource()
meth public abstract int sizePersistenceManagerFactoryResource()
meth public abstract int sizeResourceAdapterConfig()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource getAdminObjectResource(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource newAdminObjectResource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource[] getAdminObjectResource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool getConnectorConnectionPool(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool newConnectorConnectionPool()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool[] getConnectorConnectionPool()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource getConnectorResource(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource newConnectorResource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource[] getConnectorResource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource getCustomResource(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource newCustomResource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource[] getCustomResource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource getExternalJndiResource(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource newExternalJndiResource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource[] getExternalJndiResource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool getJdbcConnectionPool(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool newJdbcConnectionPool()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool[] getJdbcConnectionPool()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource getJdbcResource(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource newJdbcResource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource[] getJdbcResource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource getMailResource(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource newMailResource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource[] getMailResource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource getPersistenceManagerFactoryResource(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource newPersistenceManagerFactoryResource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource[] getPersistenceManagerFactoryResource()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig getResourceAdapterConfig(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig newResourceAdapterConfig()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig[] getResourceAdapterConfig()
meth public abstract void setAdminObjectResource(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource)
meth public abstract void setAdminObjectResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource[])
meth public abstract void setConnectorConnectionPool(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool)
meth public abstract void setConnectorConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool[])
meth public abstract void setConnectorResource(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource)
meth public abstract void setConnectorResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource[])
meth public abstract void setCustomResource(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource)
meth public abstract void setCustomResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource[])
meth public abstract void setExternalJndiResource(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource)
meth public abstract void setExternalJndiResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource[])
meth public abstract void setJdbcConnectionPool(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool)
meth public abstract void setJdbcConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool[])
meth public abstract void setJdbcResource(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource)
meth public abstract void setJdbcResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource[])
meth public abstract void setMailResource(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource)
meth public abstract void setMailResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource[])
meth public abstract void setPersistenceManagerFactoryResource(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource)
meth public abstract void setPersistenceManagerFactoryResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource[])
meth public abstract void setResourceAdapterConfig(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig)
meth public abstract void setResourceAdapterConfig(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig[])
meth public abstract void write(java.io.File) throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.serverresources.SecurityMap
fld public final static java.lang.String BACKENDPRINCIPALPASSWORD = "BackendPrincipalPassword"
fld public final static java.lang.String BACKENDPRINCIPALUSERNAME = "BackendPrincipalUserName"
fld public final static java.lang.String BACKEND_PRINCIPAL = "BackendPrincipal"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String PRINCIPAL = "Principal"
fld public final static java.lang.String USER_GROUP = "UserGroup"
meth public abstract boolean isBackendPrincipal()
meth public abstract int addPrincipal(java.lang.String)
meth public abstract int addUserGroup(java.lang.String)
meth public abstract int removePrincipal(java.lang.String)
meth public abstract int removeUserGroup(java.lang.String)
meth public abstract int sizePrincipal()
meth public abstract int sizeUserGroup()
meth public abstract java.lang.String getBackendPrincipalPassword()
meth public abstract java.lang.String getBackendPrincipalUserName()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getPrincipal(int)
meth public abstract java.lang.String getUserGroup(int)
meth public abstract java.lang.String[] getPrincipal()
meth public abstract java.lang.String[] getUserGroup()
meth public abstract void setBackendPrincipal(boolean)
meth public abstract void setBackendPrincipalPassword(java.lang.String)
meth public abstract void setBackendPrincipalUserName(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setPrincipal(int,java.lang.String)
meth public abstract void setPrincipal(java.lang.String[])
meth public abstract void setUserGroup(int,java.lang.String)
meth public abstract void setUserGroup(java.lang.String[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.services.MessageSecurityProvider
meth public abstract boolean setEndpointBinding(java.io.File,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding)
meth public abstract boolean setServiceRefBinding(java.io.File,java.lang.String,java.lang.String,java.lang.String,org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding)
meth public abstract boolean setServiceRefBinding(java.io.File,java.lang.String,org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding getEndpointBinding(java.io.File,java.lang.String,java.lang.String)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding getServiceRefBinding(java.io.File,java.lang.String)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding getServiceRefBinding(java.io.File,java.lang.String,java.lang.String,java.lang.String)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding newMessageSecurityBinding(java.io.File)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.Cache
fld public final static java.lang.String CACHE_HELPER = "CacheHelper"
fld public final static java.lang.String CACHE_MAPPING = "CacheMapping"
fld public final static java.lang.String DEFAULT_HELPER = "DefaultHelper"
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String MAXENTRIES = "MaxEntries"
fld public final static java.lang.String PROPERTY = "WebProperty"
fld public final static java.lang.String TIMEOUTINSECONDS = "TimeoutInSeconds"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addCacheHelper(org.netbeans.modules.j2ee.sun.dd.api.web.CacheHelper)
meth public abstract int addCacheMapping(org.netbeans.modules.j2ee.sun.dd.api.web.CacheMapping)
meth public abstract int addWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int removeCacheHelper(org.netbeans.modules.j2ee.sun.dd.api.web.CacheHelper)
meth public abstract int removeCacheMapping(org.netbeans.modules.j2ee.sun.dd.api.web.CacheMapping)
meth public abstract int removeWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int sizeCacheHelper()
meth public abstract int sizeCacheMapping()
meth public abstract int sizeWebProperty()
meth public abstract java.lang.String getEnabled()
meth public abstract java.lang.String getMaxEntries()
meth public abstract java.lang.String getTimeoutInSeconds()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.CacheHelper getCacheHelper(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.CacheHelper newCacheHelper()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.CacheHelper[] getCacheHelper()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.CacheMapping getCacheMapping(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.CacheMapping newCacheMapping()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.CacheMapping[] getCacheMapping()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.DefaultHelper getDefaultHelper()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.DefaultHelper newDefaultHelper()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty getWebProperty(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty newWebProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[] getWebProperty()
meth public abstract void setCacheHelper(int,org.netbeans.modules.j2ee.sun.dd.api.web.CacheHelper)
meth public abstract void setCacheHelper(org.netbeans.modules.j2ee.sun.dd.api.web.CacheHelper[])
meth public abstract void setCacheMapping(int,org.netbeans.modules.j2ee.sun.dd.api.web.CacheMapping)
meth public abstract void setCacheMapping(org.netbeans.modules.j2ee.sun.dd.api.web.CacheMapping[])
meth public abstract void setDefaultHelper(org.netbeans.modules.j2ee.sun.dd.api.web.DefaultHelper)
meth public abstract void setEnabled(java.lang.String)
meth public abstract void setMaxEntries(java.lang.String)
meth public abstract void setTimeoutInSeconds(java.lang.String)
meth public abstract void setWebProperty(int,org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract void setWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.CacheHelper
fld public final static java.lang.String CLASSNAME = "ClassName"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String PROPERTY = "WebProperty"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int removeWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int sizeWebProperty()
meth public abstract java.lang.String getClassName()
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty getWebProperty(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty newWebProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[] getWebProperty()
meth public abstract void setClassName(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setWebProperty(int,org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract void setWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.CacheMapping
fld public final static java.lang.String CACHE_HELPER_REF = "CacheHelperRef"
fld public final static java.lang.String CONSTRAINT_FIELD = "ConstraintField"
fld public final static java.lang.String DISPATCHER = "Dispatcher"
fld public final static java.lang.String HTTP_METHOD = "HttpMethod"
fld public final static java.lang.String KEYFIELDNAME = "KeyFieldName"
fld public final static java.lang.String KEYFIELDSCOPE = "KeyFieldScope"
fld public final static java.lang.String KEY_FIELD = "KeyField"
fld public final static java.lang.String REFRESHFIELDNAME = "RefreshFieldName"
fld public final static java.lang.String REFRESHFIELDSCOPE = "RefreshFieldScope"
fld public final static java.lang.String REFRESH_FIELD = "RefreshField"
fld public final static java.lang.String SERVLET_NAME = "ServletName"
fld public final static java.lang.String TIMEOUT = "Timeout"
fld public final static java.lang.String TIMEOUTNAME = "TimeoutName"
fld public final static java.lang.String TIMEOUTSCOPE = "TimeoutScope"
fld public final static java.lang.String URL_PATTERN = "UrlPattern"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract boolean isKeyField(int)
meth public abstract boolean isRefreshField()
meth public abstract boolean[] getKeyField()
meth public abstract int addConstraintField(org.netbeans.modules.j2ee.sun.dd.api.web.ConstraintField)
meth public abstract int addDispatcher(java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int addHttpMethod(java.lang.String)
meth public abstract int addKeyField(boolean)
meth public abstract int removeConstraintField(org.netbeans.modules.j2ee.sun.dd.api.web.ConstraintField)
meth public abstract int removeDispatcher(java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int removeHttpMethod(java.lang.String)
meth public abstract int removeKeyField(boolean)
meth public abstract int sizeConstraintField()
meth public abstract int sizeDispatcher() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int sizeHttpMethod()
meth public abstract int sizeKeyField()
meth public abstract int sizeKeyFieldName()
meth public abstract int sizeKeyFieldScope()
meth public abstract java.lang.String getCacheHelperRef()
meth public abstract java.lang.String getDispatcher(int) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getHttpMethod(int)
meth public abstract java.lang.String getKeyFieldName(int)
meth public abstract java.lang.String getKeyFieldScope(int)
meth public abstract java.lang.String getRefreshFieldName()
meth public abstract java.lang.String getRefreshFieldScope()
meth public abstract java.lang.String getServletName()
meth public abstract java.lang.String getTimeout()
meth public abstract java.lang.String getTimeoutName()
meth public abstract java.lang.String getTimeoutScope()
meth public abstract java.lang.String getUrlPattern()
meth public abstract java.lang.String[] getDispatcher() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String[] getHttpMethod()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.ConstraintField getConstraintField(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.ConstraintField newConstraintField()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.ConstraintField[] getConstraintField()
meth public abstract void removeKeyField(int)
meth public abstract void setCacheHelperRef(java.lang.String)
meth public abstract void setConstraintField(int,org.netbeans.modules.j2ee.sun.dd.api.web.ConstraintField)
meth public abstract void setConstraintField(org.netbeans.modules.j2ee.sun.dd.api.web.ConstraintField[])
meth public abstract void setDispatcher(int,java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setDispatcher(java.lang.String[]) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setHttpMethod(int,java.lang.String)
meth public abstract void setHttpMethod(java.lang.String[])
meth public abstract void setKeyField(boolean[])
meth public abstract void setKeyField(int,boolean)
meth public abstract void setKeyFieldName(int,java.lang.String)
meth public abstract void setKeyFieldScope(int,java.lang.String)
meth public abstract void setRefreshField(boolean)
meth public abstract void setRefreshFieldName(java.lang.String)
meth public abstract void setRefreshFieldScope(java.lang.String)
meth public abstract void setServletName(java.lang.String)
meth public abstract void setTimeout(java.lang.String)
meth public abstract void setTimeoutName(java.lang.String)
meth public abstract void setTimeoutScope(java.lang.String)
meth public abstract void setUrlPattern(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.ConstraintField
fld public final static java.lang.String CACHEONMATCH = "CacheOnMatch"
fld public final static java.lang.String CACHEONMATCHFAILURE = "CacheOnMatchFailure"
fld public final static java.lang.String CONSTRAINTFIELDVALUECACHEONMATCH = "ConstraintFieldValueCacheOnMatch"
fld public final static java.lang.String CONSTRAINTFIELDVALUECACHEONMATCHFAILURE = "ConstraintFieldValueCacheOnMatchFailure"
fld public final static java.lang.String CONSTRAINTFIELDVALUEMATCHEXPR = "ConstraintFieldValueMatchExpr"
fld public final static java.lang.String CONSTRAINT_FIELD_VALUE = "ConstraintFieldValue"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String SCOPE = "Scope"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addConstraintFieldValue(java.lang.String)
meth public abstract int removeConstraintFieldValue(java.lang.String)
meth public abstract int sizeConstraintFieldValue()
meth public abstract int sizeConstraintFieldValueCacheOnMatch()
meth public abstract int sizeConstraintFieldValueCacheOnMatchFailure()
meth public abstract int sizeConstraintFieldValueMatchExpr()
meth public abstract java.lang.String getCacheOnMatch()
meth public abstract java.lang.String getCacheOnMatchFailure()
meth public abstract java.lang.String getConstraintFieldValue(int)
meth public abstract java.lang.String getConstraintFieldValueCacheOnMatch(int)
meth public abstract java.lang.String getConstraintFieldValueCacheOnMatchFailure(int)
meth public abstract java.lang.String getConstraintFieldValueMatchExpr(int)
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getScope()
meth public abstract java.lang.String[] getConstraintFieldValue()
meth public abstract void setCacheOnMatch(java.lang.String)
meth public abstract void setCacheOnMatchFailure(java.lang.String)
meth public abstract void setConstraintFieldValue(int,java.lang.String)
meth public abstract void setConstraintFieldValue(java.lang.String[])
meth public abstract void setConstraintFieldValueCacheOnMatch(int,java.lang.String)
meth public abstract void setConstraintFieldValueCacheOnMatchFailure(int,java.lang.String)
meth public abstract void setConstraintFieldValueMatchExpr(int,java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setScope(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.CookieProperties
fld public final static java.lang.String PROPERTY = "WebProperty"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int removeWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int sizeWebProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty getWebProperty(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty newWebProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[] getWebProperty()
meth public abstract void setWebProperty(int,org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract void setWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.DefaultHelper
fld public final static java.lang.String PROPERTY = "WebProperty"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int removeWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int sizeWebProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty getWebProperty(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty newWebProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[] getWebProperty()
meth public abstract void setWebProperty(int,org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract void setWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.JspConfig
fld public final static java.lang.String PROPERTY = "WebProperty"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int removeWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int sizeWebProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty getWebProperty(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty newWebProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[] getWebProperty()
meth public abstract void setWebProperty(int,org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract void setWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetInfo
fld public final static java.lang.String DEFAULTLOCALE = "DefaultLocale"
fld public final static java.lang.String LOCALE_CHARSET_MAP = "LocaleCharsetMap"
fld public final static java.lang.String PARAMETERENCODINGDEFAULTCHARSET = "ParameterEncodingDefaultCharset"
fld public final static java.lang.String PARAMETERENCODINGFORMHINTFIELD = "ParameterEncodingFormHintField"
fld public final static java.lang.String PARAMETER_ENCODING = "ParameterEncoding"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract boolean isParameterEncoding()
meth public abstract int addLocaleCharsetMap(org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetMap)
meth public abstract int removeLocaleCharsetMap(org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetMap)
meth public abstract int sizeLocaleCharsetMap()
meth public abstract java.lang.String getDefaultLocale()
meth public abstract java.lang.String getParameterEncodingDefaultCharset()
meth public abstract java.lang.String getParameterEncodingFormHintField()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetMap getLocaleCharsetMap(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetMap newLocaleCharsetMap()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetMap[] getLocaleCharsetMap()
meth public abstract void setDefaultLocale(java.lang.String)
meth public abstract void setLocaleCharsetMap(int,org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetMap)
meth public abstract void setLocaleCharsetMap(org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetMap[])
meth public abstract void setParameterEncoding(boolean)
meth public abstract void setParameterEncodingDefaultCharset(java.lang.String)
meth public abstract void setParameterEncodingFormHintField(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetMap
fld public final static java.lang.String AGENT = "Agent"
fld public final static java.lang.String CHARSET = "Charset"
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String LOCALE = "Locale"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getAgent()
meth public abstract java.lang.String getCharset()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getLocale()
meth public abstract void setAgent(java.lang.String)
meth public abstract void setCharset(java.lang.String)
meth public abstract void setDescription(java.lang.String)
meth public abstract void setLocale(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.LoginConfigInterface
fld public final static java.lang.String AUTH_METHOD = "AuthMethod"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getAuthMethod()
meth public abstract void setAuthMethod(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.ManagerProperties
fld public final static java.lang.String PROPERTY = "WebProperty"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int removeWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int sizeWebProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty getWebProperty(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty newWebProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[] getWebProperty()
meth public abstract void setWebProperty(int,org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract void setWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.MyClassLoader
fld public final static java.lang.String DELEGATE = "Delegate"
fld public final static java.lang.String DYNAMICRELOADINTERVAL = "DynamicReloadInterval"
fld public final static java.lang.String EXTRACLASSPATH = "ExtraClassPath"
fld public final static java.lang.String PROPERTY = "WebProperty"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int removeWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int sizeWebProperty() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getDelegate()
meth public abstract java.lang.String getDynamicReloadInterval() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getExtraClassPath()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty getWebProperty(int) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty newWebProperty() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[] getWebProperty() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setDelegate(java.lang.String)
meth public abstract void setDynamicReloadInterval(java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setExtraClassPath(java.lang.String)
meth public abstract void setWebProperty(int,org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[]) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.PropertyElement
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String VALUE = "Value"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()
meth public abstract void setDescription(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.Servlet
fld public final static java.lang.String PRINCIPALNAMECLASSNAME = "PrincipalNameClassName"
fld public final static java.lang.String PRINCIPAL_NAME = "PrincipalName"
fld public final static java.lang.String SERVLET_NAME = "ServletName"
fld public final static java.lang.String WEBSERVICE_ENDPOINT = "WebserviceEndpoint"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addWebserviceEndpoint(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint)
meth public abstract int removeWebserviceEndpoint(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint)
meth public abstract int sizeWebserviceEndpoint()
meth public abstract java.lang.String getPrincipalName()
meth public abstract java.lang.String getPrincipalNameClassName() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getServletName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint getWebserviceEndpoint(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint newWebserviceEndpoint()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint[] getWebserviceEndpoint()
meth public abstract void setPrincipalName(java.lang.String)
meth public abstract void setPrincipalNameClassName(java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setServletName(java.lang.String)
meth public abstract void setWebserviceEndpoint(int,org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint)
meth public abstract void setWebserviceEndpoint(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.SessionConfig
fld public final static java.lang.String COOKIE_PROPERTIES = "CookieProperties"
fld public final static java.lang.String SESSION_MANAGER = "SessionManager"
fld public final static java.lang.String SESSION_PROPERTIES = "SessionProperties"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.CookieProperties getCookieProperties()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.CookieProperties newCookieProperties()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.SessionManager getSessionManager()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.SessionManager newSessionManager()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.SessionProperties getSessionProperties()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.SessionProperties newSessionProperties()
meth public abstract void setCookieProperties(org.netbeans.modules.j2ee.sun.dd.api.web.CookieProperties)
meth public abstract void setSessionManager(org.netbeans.modules.j2ee.sun.dd.api.web.SessionManager)
meth public abstract void setSessionProperties(org.netbeans.modules.j2ee.sun.dd.api.web.SessionProperties)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.SessionManager
fld public final static java.lang.String MANAGER_PROPERTIES = "ManagerProperties"
fld public final static java.lang.String PERSISTENCETYPE = "PersistenceType"
fld public final static java.lang.String STORE_PROPERTIES = "StoreProperties"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getPersistenceType()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.ManagerProperties getManagerProperties()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.ManagerProperties newManagerProperties()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.StoreProperties getStoreProperties()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.StoreProperties newStoreProperties()
meth public abstract void setManagerProperties(org.netbeans.modules.j2ee.sun.dd.api.web.ManagerProperties)
meth public abstract void setPersistenceType(java.lang.String)
meth public abstract void setStoreProperties(org.netbeans.modules.j2ee.sun.dd.api.web.StoreProperties)

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.SessionProperties
fld public final static java.lang.String PROPERTY = "WebProperty"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int removeWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int sizeWebProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty getWebProperty(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty newWebProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[] getWebProperty()
meth public abstract void setWebProperty(int,org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract void setWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.StoreProperties
fld public final static java.lang.String PROPERTY = "WebProperty"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int removeWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int sizeWebProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty getWebProperty(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty newWebProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[] getWebProperty()
meth public abstract void setWebProperty(int,org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract void setWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.SunWebApp
fld public final static java.lang.String CACHE = "Cache"
fld public final static java.lang.String CLASS_LOADER = "MyClassLoader"
fld public final static java.lang.String CONTEXT_ROOT = "ContextRoot"
fld public final static java.lang.String EJB_REF = "EjbRef"
fld public final static java.lang.String ERRORURL = "ErrorUrl"
fld public final static java.lang.String HTTPSERVLETSECURITYPROVIDER = "HttpservletSecurityProvider"
fld public final static java.lang.String IDEMPOTENTURLPATTERNNUMOFRETRIES = "IdempotentUrlPatternNumOfRetries"
fld public final static java.lang.String IDEMPOTENTURLPATTERNURLPATTERN = "IdempotentUrlPatternUrlPattern"
fld public final static java.lang.String IDEMPOTENT_URL_PATTERN = "IdempotentUrlPattern"
fld public final static java.lang.String JSP_CONFIG = "JspConfig"
fld public final static java.lang.String LOCALE_CHARSET_INFO = "LocaleCharsetInfo"
fld public final static java.lang.String MESSAGE_DESTINATION = "MessageDestination"
fld public final static java.lang.String MESSAGE_DESTINATION_REF = "MessageDestinationRef"
fld public final static java.lang.String PARAMETERENCODINGDEFAULTCHARSET = "ParameterEncodingDefaultCharset"
fld public final static java.lang.String PARAMETERENCODINGFORMHINTFIELD = "ParameterEncodingFormHintField"
fld public final static java.lang.String PARAMETER_ENCODING = "ParameterEncoding"
fld public final static java.lang.String PROPERTY = "WebProperty"
fld public final static java.lang.String RESOURCE_ENV_REF = "ResourceEnvRef"
fld public final static java.lang.String RESOURCE_REF = "ResourceRef"
fld public final static java.lang.String SECURITY_ROLE_MAPPING = "SecurityRoleMapping"
fld public final static java.lang.String SERVICE_REF = "ServiceRef"
fld public final static java.lang.String SERVLET = "Servlet"
fld public final static java.lang.String SESSION_CONFIG = "SessionConfig"
fld public final static java.lang.String VALVE = "Valve"
fld public final static java.lang.String VERSION_2_3_0 = "2.30"
fld public final static java.lang.String VERSION_2_4_0 = "2.40"
fld public final static java.lang.String VERSION_2_4_1 = "2.41"
fld public final static java.lang.String VERSION_2_5_0 = "2.50"
fld public final static java.lang.String VERSION_3_0_0 = "3.00"
fld public final static java.lang.String VERSION_3_0_1 = "3.01"
fld public final static java.lang.String WEBSERVICE_DESCRIPTION = "WebserviceDescription"
intf org.netbeans.modules.j2ee.sun.dd.api.RootInterface
meth public abstract boolean isIdempotentUrlPattern(int) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract boolean isMyClassLoader() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract boolean isParameterEncoding() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract boolean[] getIdempotentUrlPattern() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int addEjbRef(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef)
meth public abstract int addIdempotentUrlPattern(boolean) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int addMessageDestination(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination)
meth public abstract int addMessageDestinationRef(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int addResourceEnvRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef)
meth public abstract int addResourceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef)
meth public abstract int addSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping)
meth public abstract int addServiceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef)
meth public abstract int addServlet(org.netbeans.modules.j2ee.sun.dd.api.web.Servlet)
meth public abstract int addValve(org.netbeans.modules.j2ee.sun.dd.api.web.Valve) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int addWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int addWebserviceDescription(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription)
meth public abstract int removeEjbRef(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef)
meth public abstract int removeIdempotentUrlPattern(boolean) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int removeMessageDestination(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination)
meth public abstract int removeMessageDestinationRef(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int removeResourceEnvRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef)
meth public abstract int removeResourceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef)
meth public abstract int removeSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping)
meth public abstract int removeServiceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef)
meth public abstract int removeServlet(org.netbeans.modules.j2ee.sun.dd.api.web.Servlet)
meth public abstract int removeValve(org.netbeans.modules.j2ee.sun.dd.api.web.Valve) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int removeWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int removeWebserviceDescription(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription)
meth public abstract int sizeEjbRef()
meth public abstract int sizeIdempotentUrlPattern() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int sizeIdempotentUrlPatternNumOfRetries() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int sizeIdempotentUrlPatternUrlPattern() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int sizeMessageDestination()
meth public abstract int sizeMessageDestinationRef() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int sizeResourceEnvRef()
meth public abstract int sizeResourceRef()
meth public abstract int sizeSecurityRoleMapping()
meth public abstract int sizeServiceRef()
meth public abstract int sizeServlet()
meth public abstract int sizeValve() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract int sizeWebProperty()
meth public abstract int sizeWebserviceDescription()
meth public abstract java.lang.String getContextRoot()
meth public abstract java.lang.String getErrorUrl() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getHttpservletSecurityProvider() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getIdempotentUrlPatternNumOfRetries(int) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getIdempotentUrlPatternUrlPattern(int) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getMyClassLoaderDelegate() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getMyClassLoaderExtraClassPath() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getParameterEncodingDefaultCharset() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract java.lang.String getParameterEncodingFormHintField() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef getEjbRef(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef newEjbRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef[] getEjbRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination getMessageDestination(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination newMessageDestination()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef getMessageDestinationRef(int) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef newMessageDestinationRef() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef[] getMessageDestinationRef() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination[] getMessageDestination()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef getResourceEnvRef(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef newResourceEnvRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef[] getResourceEnvRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef getResourceRef(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef newResourceRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef[] getResourceRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping getSecurityRoleMapping(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping newSecurityRoleMapping()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[] getSecurityRoleMapping()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef getServiceRef(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef newServiceRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef[] getServiceRef()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription getWebserviceDescription(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription newWebserviceDescription()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription[] getWebserviceDescription()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.Cache getCache()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.Cache newCache()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.JspConfig getJspConfig()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.JspConfig newJspConfig()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetInfo getLocaleCharsetInfo()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetInfo newLocaleCharsetInfo()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.MyClassLoader getMyClassLoader() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.MyClassLoader newMyClassLoader() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.Servlet getServlet(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.Servlet newServlet()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.Servlet[] getServlet()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.SessionConfig getSessionConfig()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.SessionConfig newSessionConfig()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.Valve getValve(int) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.Valve newValve() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.Valve[] getValve() throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty getWebProperty(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty newWebProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[] getWebProperty()
meth public abstract void removeIdempotentUrlPattern(int) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setCache(org.netbeans.modules.j2ee.sun.dd.api.web.Cache)
meth public abstract void setContextRoot(java.lang.String)
meth public abstract void setEjbRef(int,org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef)
meth public abstract void setEjbRef(org.netbeans.modules.j2ee.sun.dd.api.common.EjbRef[])
meth public abstract void setErrorUrl(java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setHttpservletSecurityProvider(java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setIdempotentUrlPattern(boolean[]) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setIdempotentUrlPattern(int,boolean) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setIdempotentUrlPatternNumOfRetries(int,java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setIdempotentUrlPatternUrlPattern(int,java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setJspConfig(org.netbeans.modules.j2ee.sun.dd.api.web.JspConfig)
meth public abstract void setLocaleCharsetInfo(org.netbeans.modules.j2ee.sun.dd.api.web.LocaleCharsetInfo)
meth public abstract void setMessageDestination(int,org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination)
meth public abstract void setMessageDestination(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestination[])
meth public abstract void setMessageDestinationRef(int,org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setMessageDestinationRef(org.netbeans.modules.j2ee.sun.dd.api.common.MessageDestinationRef[]) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setMyClassLoader(boolean) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setMyClassLoader(org.netbeans.modules.j2ee.sun.dd.api.web.MyClassLoader) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setMyClassLoaderDelegate(java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setMyClassLoaderExtraClassPath(java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setParameterEncoding(boolean) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setParameterEncodingDefaultCharset(java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setParameterEncodingFormHintField(java.lang.String) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setResourceEnvRef(int,org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef)
meth public abstract void setResourceEnvRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceEnvRef[])
meth public abstract void setResourceRef(int,org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef)
meth public abstract void setResourceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ResourceRef[])
meth public abstract void setSecurityRoleMapping(int,org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping)
meth public abstract void setSecurityRoleMapping(org.netbeans.modules.j2ee.sun.dd.api.common.SecurityRoleMapping[])
meth public abstract void setServiceRef(int,org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef)
meth public abstract void setServiceRef(org.netbeans.modules.j2ee.sun.dd.api.common.ServiceRef[])
meth public abstract void setServlet(int,org.netbeans.modules.j2ee.sun.dd.api.web.Servlet)
meth public abstract void setServlet(org.netbeans.modules.j2ee.sun.dd.api.web.Servlet[])
meth public abstract void setSessionConfig(org.netbeans.modules.j2ee.sun.dd.api.web.SessionConfig)
meth public abstract void setValve(int,org.netbeans.modules.j2ee.sun.dd.api.web.Valve) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setValve(org.netbeans.modules.j2ee.sun.dd.api.web.Valve[]) throws org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException
meth public abstract void setWebProperty(int,org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract void setWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[])
meth public abstract void setWebserviceDescription(int,org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription)
meth public abstract void setWebserviceDescription(org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceDescription[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.Valve
fld public final static java.lang.String CLASSNAME = "ClassName"
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String PROPERTY = "WebProperty"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract int addWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int removeWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract int sizeWebProperty()
meth public abstract java.lang.String getClassName()
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getName()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty getWebProperty(int)
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty newWebProperty()
meth public abstract org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[] getWebProperty()
meth public abstract void setClassName(java.lang.String)
meth public abstract void setDescription(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setWebProperty(int,org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty)
meth public abstract void setWebProperty(org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty[])

CLSS public abstract interface org.netbeans.modules.j2ee.sun.dd.api.web.WebProperty
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String VALUE = "Value"
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public abstract java.lang.String getDescription()
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()
meth public abstract void setDescription(java.lang.String)
meth public abstract void setName(java.lang.String)
meth public abstract void setValue(java.lang.String)

CLSS public abstract org.netbeans.modules.j2ee.sun.dd.impl.common.SunBaseBean
cons public init(java.util.Vector,org.netbeans.modules.schema2beans.Version)
intf org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean
meth public boolean isTrivial(java.lang.String)
meth public org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean cloneVersion(java.lang.String)
meth public org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean getPropertyParent(java.lang.String)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void merge(org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean,int)
meth public void write(java.io.Writer) throws java.io.IOException,org.netbeans.modules.j2ee.sun.dd.api.DDException
meth public void write(org.openide.filesystems.FileObject) throws java.io.IOException
supr org.netbeans.modules.schema2beans.BaseBean
hfds commonBeanModelMap

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.AdminObjectResource
cons public init()
cons public init(int)
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String JNDINAME = "JndiName"
fld public final static java.lang.String OBJECTTYPE = "ObjectType"
fld public final static java.lang.String PROPERTY = "PropertyElement"
fld public final static java.lang.String RESADAPTER = "ResAdapter"
fld public final static java.lang.String RESTYPE = "ResType"
intf org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource
meth public int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int sizePropertyElement()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getDescription()
meth public java.lang.String getEnabled()
meth public java.lang.String getJndiName()
meth public java.lang.String getObjectType()
meth public java.lang.String getResAdapter()
meth public java.lang.String getResType()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnabled(java.lang.String)
meth public void setJndiName(java.lang.String)
meth public void setObjectType(java.lang.String)
meth public void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public void setResAdapter(java.lang.String)
meth public void setResType(java.lang.String)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.j2ee.sun.dd.impl.common.SunBaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.ConnectorConnectionPool
cons public init()
cons public init(int)
fld public final static java.lang.String ASSOCIATEWITHTHREAD = "AssociateWithThread"
fld public final static java.lang.String CONNECTIONCREATIONRETRYATTEMPTS = "ConnectionCreationRetryAttempts"
fld public final static java.lang.String CONNECTIONCREATIONRETRYINTERVALINSECONDS = "ConnectionCreationRetryIntervalInSeconds"
fld public final static java.lang.String CONNECTIONDEFINITIONNAME = "ConnectionDefinitionName"
fld public final static java.lang.String CONNECTIONLEAKRECLAIM = "ConnectionLeakReclaim"
fld public final static java.lang.String CONNECTIONLEAKTIMEOUTINSECONDS = "ConnectionLeakTimeoutInSeconds"
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String FAILALLCONNECTIONS = "FailAllConnections"
fld public final static java.lang.String IDLETIMEOUTINSECONDS = "IdleTimeoutInSeconds"
fld public final static java.lang.String ISCONNECTIONVALIDATIONREQUIRED = "IsConnectionValidationRequired"
fld public final static java.lang.String LAZYCONNECTIONASSOCIATION = "LazyConnectionAssociation"
fld public final static java.lang.String LAZYCONNECTIONENLISTMENT = "LazyConnectionEnlistment"
fld public final static java.lang.String MATCHCONNECTIONS = "MatchConnections"
fld public final static java.lang.String MAXCONNECTIONUSAGECOUNT = "MaxConnectionUsageCount"
fld public final static java.lang.String MAXPOOLSIZE = "MaxPoolSize"
fld public final static java.lang.String MAXWAITTIMEINMILLIS = "MaxWaitTimeInMillis"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String POOLRESIZEQUANTITY = "PoolResizeQuantity"
fld public final static java.lang.String PROPERTY = "PropertyElement"
fld public final static java.lang.String RESOURCEADAPTERNAME = "ResourceAdapterName"
fld public final static java.lang.String SECURITY_MAP = "SecurityMap"
fld public final static java.lang.String STEADYPOOLSIZE = "SteadyPoolSize"
fld public final static java.lang.String TRANSACTIONSUPPORT = "TransactionSupport"
fld public final static java.lang.String VALIDATEATMOSTONCEPERIODINSECONDS = "ValidateAtmostOncePeriodInSeconds"
intf org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool
meth public int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int addSecurityMap(org.netbeans.modules.j2ee.sun.dd.api.serverresources.SecurityMap)
meth public int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int removeSecurityMap(org.netbeans.modules.j2ee.sun.dd.api.serverresources.SecurityMap)
meth public int sizePropertyElement()
meth public int sizeSecurityMap()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getAssociateWithThread()
meth public java.lang.String getConnectionCreationRetryAttempts()
meth public java.lang.String getConnectionCreationRetryIntervalInSeconds()
meth public java.lang.String getConnectionDefinitionName()
meth public java.lang.String getConnectionLeakReclaim()
meth public java.lang.String getConnectionLeakTimeoutInSeconds()
meth public java.lang.String getDescription()
meth public java.lang.String getFailAllConnections()
meth public java.lang.String getIdleTimeoutInSeconds()
meth public java.lang.String getIsConnectionValidationRequired()
meth public java.lang.String getLazyConnectionAssociation()
meth public java.lang.String getLazyConnectionEnlistment()
meth public java.lang.String getMatchConnections()
meth public java.lang.String getMaxConnectionUsageCount()
meth public java.lang.String getMaxPoolSize()
meth public java.lang.String getMaxWaitTimeInMillis()
meth public java.lang.String getName()
meth public java.lang.String getPoolResizeQuantity()
meth public java.lang.String getResourceAdapterName()
meth public java.lang.String getSteadyPoolSize()
meth public java.lang.String getTransactionSupport()
meth public java.lang.String getValidateAtmostOncePeriodInSeconds()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.SecurityMap getSecurityMap(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.SecurityMap newSecurityMap()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.SecurityMap[] getSecurityMap()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setAssociateWithThread(java.lang.String)
meth public void setConnectionCreationRetryAttempts(java.lang.String)
meth public void setConnectionCreationRetryIntervalInSeconds(java.lang.String)
meth public void setConnectionDefinitionName(java.lang.String)
meth public void setConnectionLeakReclaim(java.lang.String)
meth public void setConnectionLeakTimeoutInSeconds(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setFailAllConnections(java.lang.String)
meth public void setIdleTimeoutInSeconds(java.lang.String)
meth public void setIsConnectionValidationRequired(java.lang.String)
meth public void setLazyConnectionAssociation(java.lang.String)
meth public void setLazyConnectionEnlistment(java.lang.String)
meth public void setMatchConnections(java.lang.String)
meth public void setMaxConnectionUsageCount(java.lang.String)
meth public void setMaxPoolSize(java.lang.String)
meth public void setMaxWaitTimeInMillis(java.lang.String)
meth public void setName(java.lang.String)
meth public void setPoolResizeQuantity(java.lang.String)
meth public void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public void setResourceAdapterName(java.lang.String)
meth public void setSecurityMap(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.SecurityMap)
meth public void setSecurityMap(org.netbeans.modules.j2ee.sun.dd.api.serverresources.SecurityMap[])
meth public void setSteadyPoolSize(java.lang.String)
meth public void setTransactionSupport(java.lang.String)
meth public void setValidateAtmostOncePeriodInSeconds(java.lang.String)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.j2ee.sun.dd.impl.common.SunBaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.ConnectorResource
cons public init()
cons public init(int)
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String JNDINAME = "JndiName"
fld public final static java.lang.String OBJECTTYPE = "ObjectType"
fld public final static java.lang.String POOLNAME = "PoolName"
fld public final static java.lang.String PROPERTY = "PropertyElement"
intf org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource
meth public int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int sizePropertyElement()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getDescription()
meth public java.lang.String getEnabled()
meth public java.lang.String getJndiName()
meth public java.lang.String getObjectType()
meth public java.lang.String getPoolName()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnabled(java.lang.String)
meth public void setJndiName(java.lang.String)
meth public void setObjectType(java.lang.String)
meth public void setPoolName(java.lang.String)
meth public void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.j2ee.sun.dd.impl.common.SunBaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.CustomResource
cons public init()
cons public init(int)
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String FACTORYCLASS = "FactoryClass"
fld public final static java.lang.String JNDINAME = "JndiName"
fld public final static java.lang.String OBJECTTYPE = "ObjectType"
fld public final static java.lang.String PROPERTY = "PropertyElement"
fld public final static java.lang.String RESTYPE = "ResType"
intf org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource
meth public int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int sizePropertyElement()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getDescription()
meth public java.lang.String getEnabled()
meth public java.lang.String getFactoryClass()
meth public java.lang.String getJndiName()
meth public java.lang.String getObjectType()
meth public java.lang.String getResType()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnabled(java.lang.String)
meth public void setFactoryClass(java.lang.String)
meth public void setJndiName(java.lang.String)
meth public void setObjectType(java.lang.String)
meth public void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public void setResType(java.lang.String)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.j2ee.sun.dd.impl.common.SunBaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.ExternalJndiResource
cons public init()
cons public init(int)
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String FACTORYCLASS = "FactoryClass"
fld public final static java.lang.String JNDILOOKUPNAME = "JndiLookupName"
fld public final static java.lang.String JNDINAME = "JndiName"
fld public final static java.lang.String OBJECTTYPE = "ObjectType"
fld public final static java.lang.String PROPERTY = "PropertyElement"
fld public final static java.lang.String RESTYPE = "ResType"
intf org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource
meth public int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int sizePropertyElement()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getDescription()
meth public java.lang.String getEnabled()
meth public java.lang.String getFactoryClass()
meth public java.lang.String getJndiLookupName()
meth public java.lang.String getJndiName()
meth public java.lang.String getObjectType()
meth public java.lang.String getResType()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnabled(java.lang.String)
meth public void setFactoryClass(java.lang.String)
meth public void setJndiLookupName(java.lang.String)
meth public void setJndiName(java.lang.String)
meth public void setObjectType(java.lang.String)
meth public void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public void setResType(java.lang.String)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.j2ee.sun.dd.impl.common.SunBaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.JdbcConnectionPool
cons public init()
cons public init(int)
fld public final static java.lang.String ALLOWNONCOMPONENTCALLERS = "AllowNonComponentCallers"
fld public final static java.lang.String ASSOCIATEWITHTHREAD = "AssociateWithThread"
fld public final static java.lang.String CONNECTIONCREATIONRETRYATTEMPTS = "ConnectionCreationRetryAttempts"
fld public final static java.lang.String CONNECTIONCREATIONRETRYINTERVALINSECONDS = "ConnectionCreationRetryIntervalInSeconds"
fld public final static java.lang.String CONNECTIONLEAKRECLAIM = "ConnectionLeakReclaim"
fld public final static java.lang.String CONNECTIONLEAKTIMEOUTINSECONDS = "ConnectionLeakTimeoutInSeconds"
fld public final static java.lang.String CONNECTIONVALIDATIONMETHOD = "ConnectionValidationMethod"
fld public final static java.lang.String DATASOURCECLASSNAME = "DatasourceClassname"
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String FAILALLCONNECTIONS = "FailAllConnections"
fld public final static java.lang.String IDLETIMEOUTINSECONDS = "IdleTimeoutInSeconds"
fld public final static java.lang.String ISCONNECTIONVALIDATIONREQUIRED = "IsConnectionValidationRequired"
fld public final static java.lang.String ISISOLATIONLEVELGUARANTEED = "IsIsolationLevelGuaranteed"
fld public final static java.lang.String LAZYCONNECTIONASSOCIATION = "LazyConnectionAssociation"
fld public final static java.lang.String LAZYCONNECTIONENLISTMENT = "LazyConnectionEnlistment"
fld public final static java.lang.String MATCHCONNECTIONS = "MatchConnections"
fld public final static java.lang.String MAXCONNECTIONUSAGECOUNT = "MaxConnectionUsageCount"
fld public final static java.lang.String MAXPOOLSIZE = "MaxPoolSize"
fld public final static java.lang.String MAXWAITTIMEINMILLIS = "MaxWaitTimeInMillis"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String NONTRANSACTIONALCONNECTIONS = "NonTransactionalConnections"
fld public final static java.lang.String POOLRESIZEQUANTITY = "PoolResizeQuantity"
fld public final static java.lang.String PROPERTY = "PropertyElement"
fld public final static java.lang.String RESTYPE = "ResType"
fld public final static java.lang.String STATEMENTTIMEOUTINSECONDS = "StatementTimeoutInSeconds"
fld public final static java.lang.String STEADYPOOLSIZE = "SteadyPoolSize"
fld public final static java.lang.String TRANSACTIONISOLATIONLEVEL = "TransactionIsolationLevel"
fld public final static java.lang.String VALIDATEATMOSTONCEPERIODINSECONDS = "ValidateAtmostOncePeriodInSeconds"
fld public final static java.lang.String VALIDATIONTABLENAME = "ValidationTableName"
fld public final static java.lang.String WRAPJDBCOBJECTS = "WrapJdbcObjects"
intf org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool
meth public int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int sizePropertyElement()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getAllowNonComponentCallers()
meth public java.lang.String getAssociateWithThread()
meth public java.lang.String getConnectionCreationRetryAttempts()
meth public java.lang.String getConnectionCreationRetryIntervalInSeconds()
meth public java.lang.String getConnectionLeakReclaim()
meth public java.lang.String getConnectionLeakTimeoutInSeconds()
meth public java.lang.String getConnectionValidationMethod()
meth public java.lang.String getDatasourceClassname()
meth public java.lang.String getDescription()
meth public java.lang.String getFailAllConnections()
meth public java.lang.String getIdleTimeoutInSeconds()
meth public java.lang.String getIsConnectionValidationRequired()
meth public java.lang.String getIsIsolationLevelGuaranteed()
meth public java.lang.String getLazyConnectionAssociation()
meth public java.lang.String getLazyConnectionEnlistment()
meth public java.lang.String getMatchConnections()
meth public java.lang.String getMaxConnectionUsageCount()
meth public java.lang.String getMaxPoolSize()
meth public java.lang.String getMaxWaitTimeInMillis()
meth public java.lang.String getName()
meth public java.lang.String getNonTransactionalConnections()
meth public java.lang.String getPoolResizeQuantity()
meth public java.lang.String getResType()
meth public java.lang.String getStatementTimeoutInSeconds()
meth public java.lang.String getSteadyPoolSize()
meth public java.lang.String getTransactionIsolationLevel()
meth public java.lang.String getValidateAtmostOncePeriodInSeconds()
meth public java.lang.String getValidationTableName()
meth public java.lang.String getWrapJdbcObjects()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setAllowNonComponentCallers(java.lang.String)
meth public void setAssociateWithThread(java.lang.String)
meth public void setConnectionCreationRetryAttempts(java.lang.String)
meth public void setConnectionCreationRetryIntervalInSeconds(java.lang.String)
meth public void setConnectionLeakReclaim(java.lang.String)
meth public void setConnectionLeakTimeoutInSeconds(java.lang.String)
meth public void setConnectionValidationMethod(java.lang.String)
meth public void setDatasourceClassname(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setFailAllConnections(java.lang.String)
meth public void setIdleTimeoutInSeconds(java.lang.String)
meth public void setIsConnectionValidationRequired(java.lang.String)
meth public void setIsIsolationLevelGuaranteed(java.lang.String)
meth public void setLazyConnectionAssociation(java.lang.String)
meth public void setLazyConnectionEnlistment(java.lang.String)
meth public void setMatchConnections(java.lang.String)
meth public void setMaxConnectionUsageCount(java.lang.String)
meth public void setMaxPoolSize(java.lang.String)
meth public void setMaxWaitTimeInMillis(java.lang.String)
meth public void setName(java.lang.String)
meth public void setNonTransactionalConnections(java.lang.String)
meth public void setPoolResizeQuantity(java.lang.String)
meth public void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public void setResType(java.lang.String)
meth public void setStatementTimeoutInSeconds(java.lang.String)
meth public void setSteadyPoolSize(java.lang.String)
meth public void setTransactionIsolationLevel(java.lang.String)
meth public void setValidateAtmostOncePeriodInSeconds(java.lang.String)
meth public void setValidationTableName(java.lang.String)
meth public void setWrapJdbcObjects(java.lang.String)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.j2ee.sun.dd.impl.common.SunBaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.JdbcResource
cons public init()
cons public init(int)
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String JNDINAME = "JndiName"
fld public final static java.lang.String OBJECTTYPE = "ObjectType"
fld public final static java.lang.String POOLNAME = "PoolName"
fld public final static java.lang.String PROPERTY = "PropertyElement"
intf org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource
meth public int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int sizePropertyElement()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getDescription()
meth public java.lang.String getEnabled()
meth public java.lang.String getJndiName()
meth public java.lang.String getObjectType()
meth public java.lang.String getPoolName()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnabled(java.lang.String)
meth public void setJndiName(java.lang.String)
meth public void setObjectType(java.lang.String)
meth public void setPoolName(java.lang.String)
meth public void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.j2ee.sun.dd.impl.common.SunBaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.MailResource
cons public init()
cons public init(int)
fld public final static java.lang.String DEBUG = "Debug"
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String FROM = "From"
fld public final static java.lang.String HOST = "Host"
fld public final static java.lang.String JNDINAME = "JndiName"
fld public final static java.lang.String OBJECTTYPE = "ObjectType"
fld public final static java.lang.String PROPERTY = "PropertyElement"
fld public final static java.lang.String STOREPROTOCOL = "StoreProtocol"
fld public final static java.lang.String STOREPROTOCOLCLASS = "StoreProtocolClass"
fld public final static java.lang.String TRANSPORTPROTOCOL = "TransportProtocol"
fld public final static java.lang.String TRANSPORTPROTOCOLCLASS = "TransportProtocolClass"
fld public final static java.lang.String USER = "User"
intf org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource
meth public int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int sizePropertyElement()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getDebug()
meth public java.lang.String getDescription()
meth public java.lang.String getEnabled()
meth public java.lang.String getFrom()
meth public java.lang.String getHost()
meth public java.lang.String getJndiName()
meth public java.lang.String getObjectType()
meth public java.lang.String getStoreProtocol()
meth public java.lang.String getStoreProtocolClass()
meth public java.lang.String getTransportProtocol()
meth public java.lang.String getTransportProtocolClass()
meth public java.lang.String getUser()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setDebug(java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnabled(java.lang.String)
meth public void setFrom(java.lang.String)
meth public void setHost(java.lang.String)
meth public void setJndiName(java.lang.String)
meth public void setObjectType(java.lang.String)
meth public void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public void setStoreProtocol(java.lang.String)
meth public void setStoreProtocolClass(java.lang.String)
meth public void setTransportProtocol(java.lang.String)
meth public void setTransportProtocolClass(java.lang.String)
meth public void setUser(java.lang.String)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.j2ee.sun.dd.impl.common.SunBaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.PersistenceManagerFactoryResource
cons public init()
cons public init(int)
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String ENABLED = "Enabled"
fld public final static java.lang.String FACTORYCLASS = "FactoryClass"
fld public final static java.lang.String JDBCRESOURCEJNDINAME = "JdbcResourceJndiName"
fld public final static java.lang.String JNDINAME = "JndiName"
fld public final static java.lang.String OBJECTTYPE = "ObjectType"
fld public final static java.lang.String PROPERTY = "PropertyElement"
intf org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource
meth public int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int sizePropertyElement()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getDescription()
meth public java.lang.String getEnabled()
meth public java.lang.String getFactoryClass()
meth public java.lang.String getJdbcResourceJndiName()
meth public java.lang.String getJndiName()
meth public java.lang.String getObjectType()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setEnabled(java.lang.String)
meth public void setFactoryClass(java.lang.String)
meth public void setJdbcResourceJndiName(java.lang.String)
meth public void setJndiName(java.lang.String)
meth public void setObjectType(java.lang.String)
meth public void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.j2ee.sun.dd.impl.common.SunBaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.PropertyElement
cons public init()
cons public init(int)
fld public final static java.lang.String DESCRIPTION = "Description"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String VALUE = "Value"
intf org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getDescription()
meth public java.lang.String getName()
meth public java.lang.String getValue()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setDescription(java.lang.String)
meth public void setName(java.lang.String)
meth public void setValue(java.lang.String)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.j2ee.sun.dd.impl.common.SunBaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.ResourceAdapterConfig
cons public init()
cons public init(int)
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String OBJECTTYPE = "ObjectType"
fld public final static java.lang.String PROPERTY = "PropertyElement"
fld public final static java.lang.String RESOURCEADAPTERNAME = "ResourceAdapterName"
fld public final static java.lang.String THREADPOOLIDS = "ThreadPoolIds"
intf org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig
meth public int addPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int removePropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public int sizePropertyElement()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getName()
meth public java.lang.String getObjectType()
meth public java.lang.String getResourceAdapterName()
meth public java.lang.String getThreadPoolIds()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement getPropertyElement(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement newPropertyElement()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[] getPropertyElement()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setName(java.lang.String)
meth public void setObjectType(java.lang.String)
meth public void setPropertyElement(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement)
meth public void setPropertyElement(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PropertyElement[])
meth public void setResourceAdapterName(java.lang.String)
meth public void setThreadPoolIds(java.lang.String)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.j2ee.sun.dd.impl.common.SunBaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.Resources
cons public init()
cons public init(int)
cons public init(org.w3c.dom.Node,int)
fld public final static java.lang.String ADMIN_OBJECT_RESOURCE = "AdminObjectResource"
fld public final static java.lang.String CONNECTOR_CONNECTION_POOL = "ConnectorConnectionPool"
fld public final static java.lang.String CONNECTOR_RESOURCE = "ConnectorResource"
fld public final static java.lang.String CUSTOM_RESOURCE = "CustomResource"
fld public final static java.lang.String EXTERNAL_JNDI_RESOURCE = "ExternalJndiResource"
fld public final static java.lang.String JDBC_CONNECTION_POOL = "JdbcConnectionPool"
fld public final static java.lang.String JDBC_RESOURCE = "JdbcResource"
fld public final static java.lang.String MAIL_RESOURCE = "MailResource"
fld public final static java.lang.String PERSISTENCE_MANAGER_FACTORY_RESOURCE = "PersistenceManagerFactoryResource"
fld public final static java.lang.String RESOURCE_ADAPTER_CONFIG = "ResourceAdapterConfig"
intf org.netbeans.modules.j2ee.sun.dd.api.serverresources.Resources
meth protected void initFromNode(org.w3c.dom.Node,int) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth protected void initOptions(int)
meth public boolean isEventSource(org.netbeans.modules.j2ee.sun.dd.api.RootInterface)
meth public int addAdminObjectResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource)
meth public int addConnectorConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool)
meth public int addConnectorResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource)
meth public int addCustomResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource)
meth public int addExternalJndiResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource)
meth public int addJdbcConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool)
meth public int addJdbcResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource)
meth public int addMailResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource)
meth public int addPersistenceManagerFactoryResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource)
meth public int addResourceAdapterConfig(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig)
meth public int getStatus()
meth public int removeAdminObjectResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource)
meth public int removeConnectorConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool)
meth public int removeConnectorResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource)
meth public int removeCustomResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource)
meth public int removeExternalJndiResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource)
meth public int removeJdbcConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool)
meth public int removeJdbcResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource)
meth public int removeMailResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource)
meth public int removePersistenceManagerFactoryResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource)
meth public int removeResourceAdapterConfig(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig)
meth public int sizeAdminObjectResource()
meth public int sizeConnectorConnectionPool()
meth public int sizeConnectorResource()
meth public int sizeCustomResource()
meth public int sizeExternalJndiResource()
meth public int sizeJdbcConnectionPool()
meth public int sizeJdbcResource()
meth public int sizeMailResource()
meth public int sizePersistenceManagerFactoryResource()
meth public int sizeResourceAdapterConfig()
meth public java.lang.String _getSchemaLocation()
meth public java.lang.String dumpBeanNode()
meth public java.math.BigDecimal getVersion()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource getAdminObjectResource(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource newAdminObjectResource()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource[] getAdminObjectResource()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool getConnectorConnectionPool(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool newConnectorConnectionPool()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool[] getConnectorConnectionPool()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource getConnectorResource(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource newConnectorResource()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource[] getConnectorResource()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource getCustomResource(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource newCustomResource()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource[] getCustomResource()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource getExternalJndiResource(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource newExternalJndiResource()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource[] getExternalJndiResource()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool getJdbcConnectionPool(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool newJdbcConnectionPool()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool[] getJdbcConnectionPool()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource getJdbcResource(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource newJdbcResource()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource[] getJdbcResource()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource getMailResource(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource newMailResource()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource[] getMailResource()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource getPersistenceManagerFactoryResource(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource newPersistenceManagerFactoryResource()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource[] getPersistenceManagerFactoryResource()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig getResourceAdapterConfig(int)
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig newResourceAdapterConfig()
meth public org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig[] getResourceAdapterConfig()
meth public org.xml.sax.SAXParseException getError()
meth public static org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.Resources createGraph()
meth public static org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.Resources createGraph(java.io.File) throws java.io.IOException
meth public static org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.Resources createGraph(java.io.InputStream)
meth public static org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.Resources createGraph(java.io.InputStream,boolean)
meth public static org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.Resources createGraph(org.w3c.dom.Node)
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void _setSchemaLocation(java.lang.String)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setAdminObjectResource(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource)
meth public void setAdminObjectResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.AdminObjectResource[])
meth public void setConnectorConnectionPool(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool)
meth public void setConnectorConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorConnectionPool[])
meth public void setConnectorResource(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource)
meth public void setConnectorResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ConnectorResource[])
meth public void setCustomResource(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource)
meth public void setCustomResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.CustomResource[])
meth public void setExternalJndiResource(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource)
meth public void setExternalJndiResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ExternalJndiResource[])
meth public void setJdbcConnectionPool(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool)
meth public void setJdbcConnectionPool(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcConnectionPool[])
meth public void setJdbcResource(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource)
meth public void setJdbcResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.JdbcResource[])
meth public void setMailResource(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource)
meth public void setMailResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.MailResource[])
meth public void setPersistenceManagerFactoryResource(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource)
meth public void setPersistenceManagerFactoryResource(org.netbeans.modules.j2ee.sun.dd.api.serverresources.PersistenceManagerFactoryResource[])
meth public void setResourceAdapterConfig(int,org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig)
meth public void setResourceAdapterConfig(org.netbeans.modules.j2ee.sun.dd.api.serverresources.ResourceAdapterConfig[])
meth public void setVersion(java.math.BigDecimal)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.j2ee.sun.dd.impl.common.SunBaseBean
hfds SERIALIZATION_HELPER_CHARSET,comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.SecurityMap
cons public init()
cons public init(int)
fld public final static java.lang.String BACKENDPRINCIPALPASSWORD = "BackendPrincipalPassword"
fld public final static java.lang.String BACKENDPRINCIPALUSERNAME = "BackendPrincipalUserName"
fld public final static java.lang.String BACKEND_PRINCIPAL = "BackendPrincipal"
fld public final static java.lang.String NAME = "Name"
fld public final static java.lang.String PRINCIPAL = "Principal"
fld public final static java.lang.String USER_GROUP = "UserGroup"
intf org.netbeans.modules.j2ee.sun.dd.api.serverresources.SecurityMap
meth public boolean isBackendPrincipal()
meth public int addPrincipal(java.lang.String)
meth public int addUserGroup(java.lang.String)
meth public int removePrincipal(java.lang.String)
meth public int removeUserGroup(java.lang.String)
meth public int sizePrincipal()
meth public int sizeUserGroup()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getBackendPrincipalPassword()
meth public java.lang.String getBackendPrincipalUserName()
meth public java.lang.String getName()
meth public java.lang.String getPrincipal(int)
meth public java.lang.String getUserGroup(int)
meth public java.lang.String[] getPrincipal()
meth public java.lang.String[] getUserGroup()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setBackendPrincipal(boolean)
meth public void setBackendPrincipalPassword(java.lang.String)
meth public void setBackendPrincipalUserName(java.lang.String)
meth public void setName(java.lang.String)
meth public void setPrincipal(int,java.lang.String)
meth public void setPrincipal(java.lang.String[])
meth public void setUserGroup(int,java.lang.String)
meth public void setUserGroup(java.lang.String[])
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.j2ee.sun.dd.impl.common.SunBaseBean
hfds comparators,runtimeVersion

CLSS abstract interface org.netbeans.modules.j2ee.sun.dd.impl.serverresources.model.package-info

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Appclient
cons public init()
cons public init(int)
fld public final static java.lang.String FAILED = "Failed"
fld public final static java.lang.String NOT_APPLICABLE = "NotApplicable"
fld public final static java.lang.String PASSED = "Passed"
fld public final static java.lang.String WARNING = "Warning"
meth public java.lang.String dumpBeanNode()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed getFailed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed newFailed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable getNotApplicable()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable newNotApplicable()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed getPassed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed newPassed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning getWarning()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning newWarning()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setFailed(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed)
meth public void setNotApplicable(org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable)
meth public void setPassed(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed)
meth public void setWarning(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Application
cons public init()
cons public init(int)
fld public final static java.lang.String FAILED = "Failed"
fld public final static java.lang.String NOT_APPLICABLE = "NotApplicable"
fld public final static java.lang.String PASSED = "Passed"
fld public final static java.lang.String WARNING = "Warning"
meth public java.lang.String dumpBeanNode()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed getFailed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed newFailed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable getNotApplicable()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable newNotApplicable()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed getPassed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed newPassed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning getWarning()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning newWarning()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setFailed(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed)
meth public void setNotApplicable(org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable)
meth public void setPassed(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed)
meth public void setWarning(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Connector
cons public init()
cons public init(int)
fld public final static java.lang.String FAILED = "Failed"
fld public final static java.lang.String NOT_APPLICABLE = "NotApplicable"
fld public final static java.lang.String PASSED = "Passed"
fld public final static java.lang.String WARNING = "Warning"
meth public java.lang.String dumpBeanNode()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed getFailed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed newFailed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable getNotApplicable()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable newNotApplicable()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed getPassed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed newPassed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning getWarning()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning newWarning()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setFailed(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed)
meth public void setNotApplicable(org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable)
meth public void setPassed(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed)
meth public void setWarning(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Ejb
cons public init()
cons public init(int)
fld public final static java.lang.String FAILED = "Failed"
fld public final static java.lang.String NOT_APPLICABLE = "NotApplicable"
fld public final static java.lang.String PASSED = "Passed"
fld public final static java.lang.String WARNING = "Warning"
meth public java.lang.String dumpBeanNode()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed getFailed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed newFailed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable getNotApplicable()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable newNotApplicable()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed getPassed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed newPassed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning getWarning()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning newWarning()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setFailed(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed)
meth public void setNotApplicable(org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable)
meth public void setPassed(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed)
meth public void setWarning(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Error
cons public init()
cons public init(int)
fld public final static java.lang.String ERROR_DESCRIPTION = "ErrorDescription"
fld public final static java.lang.String ERROR_NAME = "ErrorName"
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getErrorDescription()
meth public java.lang.String getErrorName()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setErrorDescription(java.lang.String)
meth public void setErrorName(java.lang.String)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed
cons public init()
cons public init(int)
fld public final static java.lang.String TEST = "Test"
meth public int addTest(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test)
meth public int removeTest(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test)
meth public int sizeTest()
meth public java.lang.String dumpBeanNode()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test getTest(int)
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test newTest()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test[] getTest()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setTest(int,org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test)
meth public void setTest(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test[])
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.verifier.FailureCount
cons public init()
cons public init(int)
fld public final static java.lang.String ERROR_NUMBER = "ErrorNumber"
fld public final static java.lang.String FAILURE_NUMBER = "FailureNumber"
fld public final static java.lang.String WARNING_NUMBER = "WarningNumber"
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getErrorNumber()
meth public java.lang.String getFailureNumber()
meth public java.lang.String getWarningNumber()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setErrorNumber(java.lang.String)
meth public void setFailureNumber(java.lang.String)
meth public void setWarningNumber(java.lang.String)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable
cons public init()
cons public init(int)
fld public final static java.lang.String TEST = "Test"
meth public int addTest(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test)
meth public int removeTest(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test)
meth public int sizeTest()
meth public java.lang.String dumpBeanNode()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test getTest(int)
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test newTest()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test[] getTest()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setTest(int,org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test)
meth public void setTest(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test[])
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Other
cons public init()
cons public init(int)
fld public final static java.lang.String FAILED = "Failed"
fld public final static java.lang.String NOT_APPLICABLE = "NotApplicable"
fld public final static java.lang.String PASSED = "Passed"
fld public final static java.lang.String WARNING = "Warning"
meth public java.lang.String dumpBeanNode()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed getFailed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed newFailed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable getNotApplicable()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable newNotApplicable()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed getPassed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed newPassed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning getWarning()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning newWarning()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setFailed(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed)
meth public void setNotApplicable(org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable)
meth public void setPassed(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed)
meth public void setWarning(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed
cons public init()
cons public init(int)
fld public final static java.lang.String TEST = "Test"
meth public int addTest(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test)
meth public int removeTest(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test)
meth public int sizeTest()
meth public java.lang.String dumpBeanNode()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test getTest(int)
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test newTest()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test[] getTest()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setTest(int,org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test)
meth public void setTest(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test[])
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.verifier.StaticVerification
cons public init()
cons public init(int)
cons public init(org.w3c.dom.Node,int)
fld public final static java.lang.String APPCLIENT = "Appclient"
fld public final static java.lang.String APPLICATION = "Application"
fld public final static java.lang.String CONNECTOR = "Connector"
fld public final static java.lang.String EJB = "Ejb"
fld public final static java.lang.String ERROR = "Error"
fld public final static java.lang.String FAILURE_COUNT = "FailureCount"
fld public final static java.lang.String OTHER = "Other"
fld public final static java.lang.String WEB = "Web"
meth protected void initFromNode(org.w3c.dom.Node,int) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth protected void initOptions(int)
meth public java.lang.String _getSchemaLocation()
meth public java.lang.String dumpBeanNode()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Appclient getAppclient()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Appclient newAppclient()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Application getApplication()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Application newApplication()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Connector getConnector()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Connector newConnector()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Ejb getEjb()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Ejb newEjb()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Error getError()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Error newError()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.FailureCount getFailureCount()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.FailureCount newFailureCount()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Other getOther()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Other newOther()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Web getWeb()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Web newWeb()
meth public static org.netbeans.modules.j2ee.sun.dd.impl.verifier.StaticVerification createGraph()
meth public static org.netbeans.modules.j2ee.sun.dd.impl.verifier.StaticVerification createGraph(java.io.File) throws java.io.IOException
meth public static org.netbeans.modules.j2ee.sun.dd.impl.verifier.StaticVerification createGraph(java.io.InputStream)
meth public static org.netbeans.modules.j2ee.sun.dd.impl.verifier.StaticVerification createGraph(java.io.InputStream,boolean)
meth public static org.netbeans.modules.j2ee.sun.dd.impl.verifier.StaticVerification createGraph(org.w3c.dom.Node)
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void _setSchemaLocation(java.lang.String)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setAppclient(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Appclient)
meth public void setApplication(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Application)
meth public void setConnector(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Connector)
meth public void setEjb(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Ejb)
meth public void setError(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Error)
meth public void setFailureCount(org.netbeans.modules.j2ee.sun.dd.impl.verifier.FailureCount)
meth public void setOther(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Other)
meth public void setWeb(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Web)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds SERIALIZATION_HELPER_CHARSET,comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test
cons public init()
cons public init(int)
fld public final static java.lang.String TEST_ASSERTION = "TestAssertion"
fld public final static java.lang.String TEST_DESCRIPTION = "TestDescription"
fld public final static java.lang.String TEST_NAME = "TestName"
meth public java.lang.String dumpBeanNode()
meth public java.lang.String getTestAssertion()
meth public java.lang.String getTestDescription()
meth public java.lang.String getTestName()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setTestAssertion(java.lang.String)
meth public void setTestDescription(java.lang.String)
meth public void setTestName(java.lang.String)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning
cons public init()
cons public init(int)
fld public final static java.lang.String TEST = "Test"
meth public int addTest(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test)
meth public int removeTest(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test)
meth public int sizeTest()
meth public java.lang.String dumpBeanNode()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test getTest(int)
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test newTest()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test[] getTest()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setTest(int,org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test)
meth public void setTest(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Test[])
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Web
cons public init()
cons public init(int)
fld public final static java.lang.String FAILED = "Failed"
fld public final static java.lang.String NOT_APPLICABLE = "NotApplicable"
fld public final static java.lang.String PASSED = "Passed"
fld public final static java.lang.String WARNING = "Warning"
meth public java.lang.String dumpBeanNode()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed getFailed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed newFailed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable getNotApplicable()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable newNotApplicable()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed getPassed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed newPassed()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning getWarning()
meth public org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning newWarning()
meth public static void addComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public static void removeComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void dump(java.lang.StringBuffer,java.lang.String)
meth public void setFailed(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Failed)
meth public void setNotApplicable(org.netbeans.modules.j2ee.sun.dd.impl.verifier.NotApplicable)
meth public void setPassed(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Passed)
meth public void setWarning(org.netbeans.modules.j2ee.sun.dd.impl.verifier.Warning)
meth public void validate() throws org.netbeans.modules.schema2beans.ValidateException
supr org.netbeans.modules.schema2beans.BaseBean
hfds comparators,runtimeVersion

CLSS abstract interface org.netbeans.modules.j2ee.sun.dd.impl.verifier.package-info

CLSS public abstract org.netbeans.modules.schema2beans.BaseBean
cons public init(java.util.Vector,org.netbeans.modules.schema2beans.Version)
fld protected org.netbeans.modules.schema2beans.DOMBinding binding
fld protected org.netbeans.modules.schema2beans.GraphManager graphManager
fld public final static int MERGE_COMPARE = 4
fld public final static int MERGE_INTERSECT = 1
fld public final static int MERGE_NONE = 0
fld public final static int MERGE_UNION = 2
fld public final static int MERGE_UPDATE = 3
innr public IterateChoiceProperties
intf java.lang.Cloneable
intf org.netbeans.modules.schema2beans.Bean
meth protected boolean hasDomNode()
meth protected int addValue(org.netbeans.modules.schema2beans.BeanProp,java.lang.Object)
meth protected int removeValue(org.netbeans.modules.schema2beans.BeanProp,java.lang.Object)
meth protected java.util.Iterator beanPropsIterator()
meth protected org.netbeans.modules.schema2beans.DOMBinding domBinding()
meth protected void addKnownValue(java.lang.String,java.lang.Object)
meth protected void buildPathName(java.lang.StringBuffer)
meth protected void copyProperty(org.netbeans.modules.schema2beans.BeanProp,org.netbeans.modules.schema2beans.BaseBean,int,java.lang.Object)
meth protected void init(java.util.Vector,org.netbeans.modules.schema2beans.Version)
meth protected void initPropertyTables(int)
meth protected void removeValue(org.netbeans.modules.schema2beans.BeanProp,int)
meth protected void setDomBinding(org.netbeans.modules.schema2beans.DOMBinding)
meth protected void setGraphManager(org.netbeans.modules.schema2beans.GraphManager)
meth protected void setValue(org.netbeans.modules.schema2beans.BeanProp,int,java.lang.Object)
meth public abstract void dump(java.lang.StringBuffer,java.lang.String)
meth public boolean hasName(java.lang.String)
meth public boolean isChoiceProperty()
meth public boolean isChoiceProperty(java.lang.String)
meth public boolean isEqualTo(java.lang.Object)
meth public boolean isNull(java.lang.String)
meth public boolean isNull(java.lang.String,int)
meth public boolean isRoot()
meth public int addValue(java.lang.String,java.lang.Object)
meth public int idToIndex(java.lang.String,int)
meth public int indexOf(java.lang.String,java.lang.Object)
meth public int indexToId(java.lang.String,int)
meth public int removeValue(java.lang.String,java.lang.Object)
meth public int size(java.lang.String)
meth public java.lang.Object clone()
meth public java.lang.Object getValue(java.lang.String)
meth public java.lang.Object getValue(java.lang.String,int)
meth public java.lang.Object getValueById(java.lang.String,int)
meth public java.lang.Object[] getValues(java.lang.String)
meth public java.lang.Object[] knownValues(java.lang.String)
meth public java.lang.String _getXPathExpr()
meth public java.lang.String _getXPathExpr(java.lang.Object)
meth public java.lang.String dtdName()
meth public java.lang.String dumpBeanNode()
meth public java.lang.String dumpDomNode()
meth public java.lang.String dumpDomNode(int)
meth public java.lang.String dumpDomNode(java.lang.String,int)
meth public java.lang.String fullName()
meth public java.lang.String getAttributeValue(java.lang.String)
meth public java.lang.String getAttributeValue(java.lang.String,int,java.lang.String)
meth public java.lang.String getAttributeValue(java.lang.String,java.lang.String)
meth public java.lang.String getDefaultNamespace()
meth public java.lang.String name()
meth public java.lang.String nameChild(java.lang.Object)
meth public java.lang.String nameChild(java.lang.Object,boolean,boolean)
meth public java.lang.String nameChild(java.lang.Object,boolean,boolean,boolean)
meth public java.lang.String nameSelf()
meth public java.lang.String toString()
meth public java.lang.String[] findAttributeValue(java.lang.String,java.lang.String)
meth public java.lang.String[] findPropertyValue(java.lang.String,java.lang.Object)
meth public java.lang.String[] findValue(java.lang.Object)
meth public java.lang.String[] getAttributeNames()
meth public java.lang.String[] getAttributeNames(java.lang.String)
meth public java.util.Iterator listChoiceProperties()
meth public org.netbeans.modules.schema2beans.BaseAttribute[] listAttributes()
meth public org.netbeans.modules.schema2beans.BaseAttribute[] listAttributes(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseBean newInstance(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseBean parent()
meth public org.netbeans.modules.schema2beans.BaseBean[] childBeans(boolean)
meth public org.netbeans.modules.schema2beans.BaseProperty getProperty()
meth public org.netbeans.modules.schema2beans.BaseProperty getProperty(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseProperty[] listChoiceProperties(java.lang.String)
meth public org.netbeans.modules.schema2beans.BaseProperty[] listProperties()
meth public org.netbeans.modules.schema2beans.Bean _getParent()
meth public org.netbeans.modules.schema2beans.Bean _getRoot()
meth public org.netbeans.modules.schema2beans.Bean propertyById(java.lang.String,int)
meth public org.netbeans.modules.schema2beans.BeanProp beanProp()
meth public org.netbeans.modules.schema2beans.BeanProp beanProp(int)
meth public org.netbeans.modules.schema2beans.BeanProp beanProp(java.lang.String)
meth public org.netbeans.modules.schema2beans.BeanProp[] beanProps()
meth public org.netbeans.modules.schema2beans.GraphManager graphManager()
meth public org.w3c.dom.Comment addComment(java.lang.String)
meth public org.w3c.dom.Comment[] comments()
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream,boolean) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream,boolean,org.xml.sax.EntityResolver) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public static org.netbeans.modules.schema2beans.BaseBean createGraph(java.lang.Class,java.io.InputStream,boolean,org.xml.sax.EntityResolver,org.xml.sax.ErrorHandler) throws org.netbeans.modules.schema2beans.Schema2BeansException
meth public void _setChanged(boolean)
meth public void addBeanComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void addPropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void changeDocType(java.lang.String,java.lang.String)
meth public void childBeans(boolean,java.util.List)
meth public void createAttribute(java.lang.String,java.lang.String,int,java.lang.String[],java.lang.String)
meth public void createAttribute(java.lang.String,java.lang.String,java.lang.String,int,java.lang.String[],java.lang.String)
meth public void createBean(org.w3c.dom.Node,org.netbeans.modules.schema2beans.GraphManager)
meth public void createProperty(java.lang.String,java.lang.String,int,java.lang.Class)
meth public void createProperty(java.lang.String,java.lang.String,java.lang.Class)
meth public void createRoot(java.lang.String,java.lang.String,int,java.lang.Class)
meth public void dumpAttributes(java.lang.String,int,java.lang.StringBuffer,java.lang.String)
meth public void dumpXml()
meth public void merge(org.netbeans.modules.schema2beans.BaseBean)
meth public void merge(org.netbeans.modules.schema2beans.BaseBean,int)
meth public void mergeUpdate(org.netbeans.modules.schema2beans.BaseBean)
meth public void reindent()
meth public void reindent(java.lang.String)
meth public void removeBeanComparator(org.netbeans.modules.schema2beans.BeanComparator)
meth public void removeComment(org.w3c.dom.Comment)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public void removePropertyChangeListener(java.lang.String,java.beans.PropertyChangeListener)
meth public void removeValue(java.lang.String,int)
meth public void setAttributeValue(java.lang.String,int,java.lang.String,java.lang.String)
meth public void setAttributeValue(java.lang.String,java.lang.String)
meth public void setAttributeValue(java.lang.String,java.lang.String,java.lang.String)
meth public void setDefaultNamespace(java.lang.String)
meth public void setValue(java.lang.String,int,java.lang.Object)
meth public void setValue(java.lang.String,java.lang.Object)
meth public void setValue(java.lang.String,java.lang.Object[])
meth public void setValueById(java.lang.String,int,java.lang.Object)
meth public void write(java.io.File) throws java.io.IOException
meth public void write(java.io.OutputStream) throws java.io.IOException
meth public void write(java.io.OutputStream,java.lang.String) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void write(java.io.Writer) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void write(java.io.Writer,java.lang.String) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void writeNoReindent(java.io.OutputStream) throws java.io.IOException,org.netbeans.modules.schema2beans.Schema2BeansException
meth public void writeNode(java.io.Writer) throws java.io.IOException
supr java.lang.Object
hfds attrCache,changeListeners,comparators,defaultNamespace,isRoot,propByName,propByOrder,propertyOrder

CLSS public abstract interface org.netbeans.modules.schema2beans.Bean
meth public abstract boolean hasName(java.lang.String)
meth public abstract boolean isRoot()
meth public abstract int addValue(java.lang.String,java.lang.Object)
meth public abstract int idToIndex(java.lang.String,int)
meth public abstract int indexToId(java.lang.String,int)
meth public abstract int removeValue(java.lang.String,java.lang.Object)
meth public abstract java.lang.Object getValue(java.lang.String)
meth public abstract java.lang.Object getValue(java.lang.String,int)
meth public abstract java.lang.Object getValueById(java.lang.String,int)
meth public abstract java.lang.Object[] getValues(java.lang.String)
meth public abstract java.lang.String dtdName()
meth public abstract java.lang.String name()
meth public abstract org.netbeans.modules.schema2beans.BaseProperty getProperty()
meth public abstract org.netbeans.modules.schema2beans.BaseProperty getProperty(java.lang.String)
meth public abstract org.netbeans.modules.schema2beans.BaseProperty[] listProperties()
meth public abstract org.netbeans.modules.schema2beans.Bean _getParent()
meth public abstract org.netbeans.modules.schema2beans.Bean _getRoot()
meth public abstract org.netbeans.modules.schema2beans.Bean propertyById(java.lang.String,int)
meth public abstract org.netbeans.modules.schema2beans.BeanProp beanProp(java.lang.String)
meth public abstract void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void childBeans(boolean,java.util.List)
meth public abstract void removePropertyChangeListener(java.beans.PropertyChangeListener)
meth public abstract void setValue(java.lang.String,int,java.lang.Object)
meth public abstract void setValue(java.lang.String,java.lang.Object)
meth public abstract void setValueById(java.lang.String,int,java.lang.Object)

CLSS public org.netbeans.modules.schema2beans.Schema2BeansException
cons public init(java.lang.String)
fld protected java.lang.String originalStackTrace
intf java.io.Serializable
meth public java.lang.String getOriginalStackTrace()
meth public void stashOriginalStackTrace()
supr java.lang.Exception

