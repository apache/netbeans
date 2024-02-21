#Signature file v4.1
#Version 1.63.0

CLSS public abstract interface java.io.Serializable

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

CLSS public abstract interface !annotation org.netbeans.api.annotations.common.SuppressWarnings
 anno 0 java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy value=CLASS)
 anno 0 java.lang.annotation.Target(java.lang.annotation.ElementType[] value=[CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE])
intf java.lang.annotation.Annotation
meth public abstract !hasdefault java.lang.String justification()
meth public abstract !hasdefault java.lang.String[] value()

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.application.Application
fld public final static int STATE_INVALID_PARSABLE = 1
fld public final static int STATE_INVALID_UNPARSABLE = 2
fld public final static int STATE_VALID = 0
fld public final static java.lang.String MODULE = "Module"
fld public final static java.lang.String PROPERTY_STATUS = "dd_status"
fld public final static java.lang.String PROPERTY_VERSION = "dd_version"
fld public final static java.lang.String SECURITY_ROLE = "SecurityRole"
fld public final static java.lang.String VERSION_10 = "10"
fld public final static java.lang.String VERSION_1_4 = "1.4"
fld public final static java.lang.String VERSION_5 = "5"
fld public final static java.lang.String VERSION_6 = "6"
fld public final static java.lang.String VERSION_7 = "7"
fld public final static java.lang.String VERSION_8 = "8"
fld public final static java.lang.String VERSION_9 = "9"
intf org.netbeans.modules.j2ee.dd.api.common.RootInterface
meth public abstract int addIcon(org.netbeans.modules.j2ee.dd.api.common.Icon) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addModule(org.netbeans.modules.j2ee.dd.api.application.Module)
meth public abstract int addSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole)
meth public abstract int getStatus()
meth public abstract int removeIcon(org.netbeans.modules.j2ee.dd.api.common.Icon) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeModule(org.netbeans.modules.j2ee.dd.api.application.Module)
meth public abstract int removeSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole)
meth public abstract int sizeIcon() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeModule()
meth public abstract int sizeSecurityRole()
meth public abstract java.math.BigDecimal getVersion()
meth public abstract org.netbeans.modules.j2ee.dd.api.application.Module getModule(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.application.Module newModule()
meth public abstract org.netbeans.modules.j2ee.dd.api.application.Module[] getModule()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.Icon getIcon(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.Icon newIcon()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.SecurityRole getSecurityRole(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.SecurityRole newSecurityRole()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] getSecurityRole()
meth public abstract org.xml.sax.SAXParseException getError()
meth public abstract void setIcon(int,org.netbeans.modules.j2ee.dd.api.common.Icon) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setModule(int,org.netbeans.modules.j2ee.dd.api.application.Module)
meth public abstract void setModule(org.netbeans.modules.j2ee.dd.api.application.Module[])
meth public abstract void setSecurityRole(int,org.netbeans.modules.j2ee.dd.api.common.SecurityRole)
meth public abstract void setSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.application.ApplicationMetadata
meth public abstract org.netbeans.modules.j2ee.dd.api.application.Application getRoot()

CLSS public final org.netbeans.modules.j2ee.dd.api.application.DDProvider
meth public org.netbeans.modules.j2ee.dd.api.application.Application getDDRoot(org.openide.filesystems.FileObject) throws java.io.IOException
meth public org.netbeans.modules.j2ee.dd.api.application.Application getDDRoot(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public org.netbeans.modules.j2ee.dd.api.application.Application getDDRootCopy(org.openide.filesystems.FileObject) throws java.io.IOException
meth public org.netbeans.modules.schema2beans.BaseBean getBaseBean(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public org.xml.sax.SAXParseException parse(org.openide.filesystems.FileObject) throws java.io.IOException,org.xml.sax.SAXException
meth public static org.netbeans.modules.j2ee.dd.api.application.DDProvider getDefault()
supr java.lang.Object
hfds APP_13_DOCTYPE,LOGGER,bundle,ddMap,ddProvider
hcls DDParse,DDResolver,ErrorHandler

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.application.Module
fld public final static java.lang.String ALT_DD = "AltDd"
fld public final static java.lang.String CONNECTOR = "Connector"
fld public final static java.lang.String EJB = "Ejb"
fld public final static java.lang.String JAVA = "Java"
fld public final static java.lang.String WEB = "Web"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.lang.String getAltDd()
meth public abstract java.lang.String getConnector()
meth public abstract java.lang.String getEjb()
meth public abstract java.lang.String getJava()
meth public abstract org.netbeans.modules.j2ee.dd.api.application.Web getWeb()
meth public abstract org.netbeans.modules.j2ee.dd.api.application.Web newWeb()
meth public abstract void setAltDd(java.lang.String)
meth public abstract void setConnector(java.lang.String)
meth public abstract void setEjb(java.lang.String)
meth public abstract void setJava(java.lang.String)
meth public abstract void setWeb(org.netbeans.modules.j2ee.dd.api.application.Web)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.application.Web
fld public final static java.lang.String CONTEXTROOTID = "ContextRootId"
fld public final static java.lang.String CONTEXT_ROOT = "ContextRoot"
fld public final static java.lang.String WEB_URI = "WebUri"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.lang.String getContextRoot()
meth public abstract java.lang.String getContextRootId()
meth public abstract java.lang.String getWebUri()
meth public abstract java.lang.String getWebUriId() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setContextRoot(java.lang.String)
meth public abstract void setContextRootId(java.lang.String)
meth public abstract void setWebUri(java.lang.String)
meth public abstract void setWebUriId(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.client.AppClient
fld public final static int STATE_INVALID_PARSABLE = 1
fld public final static int STATE_INVALID_UNPARSABLE = 2
fld public final static int STATE_VALID = 0
fld public final static java.lang.String PROPERTY_STATUS = "dd_status"
fld public final static java.lang.String PROPERTY_VERSION = "dd_version"
fld public final static java.lang.String VERSION_10_0 = "10"
fld public final static java.lang.String VERSION_1_4 = "1.4"
fld public final static java.lang.String VERSION_5_0 = "5"
fld public final static java.lang.String VERSION_6_0 = "6"
fld public final static java.lang.String VERSION_7_0 = "7"
fld public final static java.lang.String VERSION_8_0 = "8"
fld public final static java.lang.String VERSION_9_0 = "9"
intf org.netbeans.modules.j2ee.dd.api.common.RootInterface
meth public abstract int addEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef)
meth public abstract int addEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry)
meth public abstract int addIcon(org.netbeans.modules.j2ee.dd.api.common.Icon) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef)
meth public abstract int addResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef)
meth public abstract int addServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int getStatus()
meth public abstract int removeEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef)
meth public abstract int removeEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry)
meth public abstract int removeIcon(org.netbeans.modules.j2ee.dd.api.common.Icon) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef)
meth public abstract int removeResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef)
meth public abstract int removeServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeEjbRef()
meth public abstract int sizeEnvEntry()
meth public abstract int sizeIcon() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeMessageDestination() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeMessageDestinationRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeResourceEnvRef()
meth public abstract int sizeResourceRef()
meth public abstract int sizeServiceRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getCallbackHandler()
meth public abstract java.math.BigDecimal getVersion()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbRef getEjbRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbRef newEjbRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbRef[] getEjbRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EnvEntry getEnvEntry(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EnvEntry newEnvEntry()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EnvEntry[] getEnvEntry()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.Icon getIcon(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.Icon newIcon() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestination getMessageDestination(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestination newMessageDestination() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef getMessageDestinationRef(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef newMessageDestinationRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[] getMessageDestinationRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestination[] getMessageDestination() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef getResourceEnvRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef newResourceEnvRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[] getResourceEnvRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceRef getResourceRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceRef newResourceRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceRef[] getResourceRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRef getServiceRef(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRef newServiceRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRef[] getServiceRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.xml.sax.SAXParseException getError()
meth public abstract void setCallbackHandler(java.lang.String)
meth public abstract void setEjbRef(int,org.netbeans.modules.j2ee.dd.api.common.EjbRef)
meth public abstract void setEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef[])
meth public abstract void setEnvEntry(int,org.netbeans.modules.j2ee.dd.api.common.EnvEntry)
meth public abstract void setEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry[])
meth public abstract void setIcon(int,org.netbeans.modules.j2ee.dd.api.common.Icon) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setIcon(org.netbeans.modules.j2ee.dd.api.common.Icon[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMessageDestination(int,org.netbeans.modules.j2ee.dd.api.common.MessageDestination) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMessageDestinationRef(int,org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setResourceEnvRef(int,org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef)
meth public abstract void setResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[])
meth public abstract void setResourceRef(int,org.netbeans.modules.j2ee.dd.api.common.ResourceRef)
meth public abstract void setResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef[])
meth public abstract void setServiceRef(int,org.netbeans.modules.j2ee.dd.api.common.ServiceRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setVersion(java.math.BigDecimal)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata
meth public abstract org.netbeans.modules.j2ee.dd.api.client.AppClient getRoot()

CLSS public final org.netbeans.modules.j2ee.dd.api.client.DDProvider
meth public org.netbeans.modules.j2ee.dd.api.client.AppClient getDDRoot(java.io.File) throws java.io.IOException,org.xml.sax.SAXException
meth public org.netbeans.modules.j2ee.dd.api.client.AppClient getDDRoot(org.openide.filesystems.FileObject) throws java.io.IOException
meth public org.netbeans.modules.j2ee.dd.api.client.AppClient getDDRootCopy(org.openide.filesystems.FileObject) throws java.io.IOException
meth public org.netbeans.modules.schema2beans.BaseBean getBaseBean(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
 anno 0 java.lang.Deprecated()
meth public static org.netbeans.modules.j2ee.dd.api.client.DDProvider getDefault()
supr java.lang.Object
hfds baseBeanMap,ddMap,ddProvider,errorMap,fileChangeListener
hcls FCA

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

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef
fld public final static java.lang.String EJB_LINK = "EjbLink"
fld public final static java.lang.String EJB_REF_NAME = "EjbRefName"
fld public final static java.lang.String EJB_REF_TYPE = "EjbRefType"
fld public final static java.lang.String EJB_REF_TYPE_ENTITY = "Entity"
fld public final static java.lang.String EJB_REF_TYPE_SESSION = "Session"
fld public final static java.lang.String LOCAL = "Local"
fld public final static java.lang.String LOCAL_HOME = "LocalHome"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract int addInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getEjbLink()
meth public abstract java.lang.String getEjbRefName()
meth public abstract java.lang.String getEjbRefType()
meth public abstract java.lang.String getLocal()
meth public abstract java.lang.String getLocalHome()
meth public abstract java.lang.String getMappedName() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget getInjectionTarget(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget newInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget[] getInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setEjbLink(java.lang.String)
meth public abstract void setEjbRefName(java.lang.String)
meth public abstract void setEjbRefType(java.lang.String)
meth public abstract void setInjectionTarget(int,org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setLocal(java.lang.String)
meth public abstract void setLocalHome(java.lang.String)
meth public abstract void setMappedName(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.EjbRef
fld public final static java.lang.String EJB_LINK = "EjbLink"
fld public final static java.lang.String EJB_REF_NAME = "EjbRefName"
fld public final static java.lang.String EJB_REF_TYPE = "EjbRefType"
fld public final static java.lang.String EJB_REF_TYPE_ENTITY = "Entity"
fld public final static java.lang.String EJB_REF_TYPE_SESSION = "Session"
fld public final static java.lang.String HOME = "Home"
fld public final static java.lang.String REMOTE = "Remote"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract int addInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getEjbLink()
meth public abstract java.lang.String getEjbRefName()
meth public abstract java.lang.String getEjbRefType()
meth public abstract java.lang.String getHome()
meth public abstract java.lang.String getMappedName() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getRemote()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget getInjectionTarget(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget newInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget[] getInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setEjbLink(java.lang.String)
meth public abstract void setEjbRefName(java.lang.String)
meth public abstract void setEjbRefType(java.lang.String)
meth public abstract void setHome(java.lang.String)
meth public abstract void setInjectionTarget(int,org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMappedName(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setRemote(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.EnvEntry
fld public final static java.lang.String ENV_ENTRY_NAME = "EnvEntryName"
fld public final static java.lang.String ENV_ENTRY_TYPE = "EnvEntryType"
fld public final static java.lang.String ENV_ENTRY_VALUE = "EnvEntryValue"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract int addInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getEnvEntryName()
meth public abstract java.lang.String getEnvEntryType()
meth public abstract java.lang.String getEnvEntryValue()
meth public abstract java.lang.String getMappedName() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget getInjectionTarget(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget newInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget[] getInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setEnvEntryName(java.lang.String)
meth public abstract void setEnvEntryType(java.lang.String)
meth public abstract void setEnvEntryValue(java.lang.String)
meth public abstract void setInjectionTarget(int,org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMappedName(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.FindCapability
meth public abstract org.netbeans.modules.j2ee.dd.api.common.CommonDDBean findBeanByName(java.lang.String,java.lang.String,java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.Icon
fld public final static java.lang.String LARGE_ICON = "LargeIcon"
fld public final static java.lang.String SMALL_ICON = "SmallIcon"
fld public final static java.lang.String XMLLANG = "XmlLang"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.lang.String getLargeIcon()
meth public abstract java.lang.String getSmallIcon()
meth public abstract java.lang.String getXmlLang() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setLargeIcon(java.lang.String)
meth public abstract void setSmallIcon(java.lang.String)
meth public abstract void setXmlLang(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException

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

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.InitParam
fld public final static java.lang.String PARAM_NAME = "ParamName"
fld public final static java.lang.String PARAM_VALUE = "ParamValue"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract java.lang.String getParamName()
meth public abstract java.lang.String getParamValue()
meth public abstract void setParamName(java.lang.String)
meth public abstract void setParamValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.InjectionComplete
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.lang.String getInjectionCompleteClass()
meth public abstract java.lang.String getInjectionCompleteMethod()
meth public abstract void setInjectionCompleteClass(java.lang.String)
meth public abstract void setInjectionCompleteMethod(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.InjectionTarget
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.lang.String getInjectionTargetClass()
meth public abstract java.lang.String getInjectionTargetName()
meth public abstract void setInjectionTargetClass(java.lang.String)
meth public abstract void setInjectionTargetName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.MessageDestination
fld public final static java.lang.String MESSAGE_DESTINATION_NAME = "MessageDestinationName"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
intf org.netbeans.modules.j2ee.dd.api.common.DisplayNameInterface
intf org.netbeans.modules.j2ee.dd.api.common.IconInterface
meth public abstract java.lang.String getMappedName() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getMessageDestinationName()
meth public abstract void setMappedName(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMessageDestinationName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef
fld public final static java.lang.String MESSAGE_DESTINATION_LINK = "MessageDestinationLink"
fld public final static java.lang.String MESSAGE_DESTINATION_REF_NAME = "MessageDestinationRefName"
fld public final static java.lang.String MESSAGE_DESTINATION_TYPE = "MessageDestinationType"
fld public final static java.lang.String MESSAGE_DESTINATION_USAGE = "MessageDestinationUsage"
fld public final static java.lang.String MESSAGE_DESTINATION_USAGE_CONSUMES = "Consumes"
fld public final static java.lang.String MESSAGE_DESTINATION_USAGE_CONSUMESPRODUCES = "ConsumesProduces"
fld public final static java.lang.String MESSAGE_DESTINATION_USAGE_PRODUCES = "Produces"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract int addInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getMappedName() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getMessageDestinationLink()
meth public abstract java.lang.String getMessageDestinationRefName()
meth public abstract java.lang.String getMessageDestinationType()
meth public abstract java.lang.String getMessageDestinationUsage()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget getInjectionTarget(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget newInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget[] getInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setInjectionTarget(int,org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMappedName(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMessageDestinationLink(java.lang.String)
meth public abstract void setMessageDestinationRefName(java.lang.String)
meth public abstract void setMessageDestinationType(java.lang.String)
meth public abstract void setMessageDestinationUsage(java.lang.String)

CLSS public org.netbeans.modules.j2ee.dd.api.common.NameAlreadyUsedException
cons public init(java.lang.String,java.lang.String,java.lang.String)
meth public java.lang.String getMessage()
supr java.lang.Exception
hfds beanName,keyProperty,keyValue

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.PortComponentRef
fld public final static java.lang.String PORT_COMPONENT_LINK = "PortComponentLink"
fld public final static java.lang.String SERVICE_ENDPOINT_INTERFACE = "ServiceEndpointInterface"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.lang.String getPortComponentLink()
meth public abstract java.lang.String getServiceEndpointInterface()
meth public abstract void setPortComponentLink(java.lang.String)
meth public abstract void setServiceEndpointInterface(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef
fld public final static java.lang.String RESOURCE_ENV_REF_NAME = "ResourceEnvRefName"
fld public final static java.lang.String RESOURCE_ENV_REF_TYPE = "ResourceEnvRefType"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract int addInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getMappedName() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getResourceEnvRefName()
meth public abstract java.lang.String getResourceEnvRefType()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget getInjectionTarget(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget newInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget[] getInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setInjectionTarget(int,org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMappedName(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setResourceEnvRefName(java.lang.String)
meth public abstract void setResourceEnvRefType(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.ResourceRef
fld public final static java.lang.String RES_AUTH = "ResAuth"
fld public final static java.lang.String RES_AUTH_APPLICATION = "Application"
fld public final static java.lang.String RES_AUTH_CONTAINER = "Container"
fld public final static java.lang.String RES_REF_NAME = "ResRefName"
fld public final static java.lang.String RES_SHARING_SCOPE = "ResSharingScope"
fld public final static java.lang.String RES_SHARING_SCOPE_SHAREABLE = "Shareable"
fld public final static java.lang.String RES_SHARING_SCOPE_UNSHAREABLE = "Unshareable"
fld public final static java.lang.String RES_TYPE = "ResType"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract int addInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getMappedName() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getResAuth()
meth public abstract java.lang.String getResRefName()
meth public abstract java.lang.String getResSharingScope()
meth public abstract java.lang.String getResType()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget getInjectionTarget(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget newInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget[] getInjectionTarget() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setInjectionTarget(int,org.netbeans.modules.j2ee.dd.api.common.InjectionTarget) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMappedName(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setResAuth(java.lang.String)
meth public abstract void setResRefName(java.lang.String)
meth public abstract void setResSharingScope(java.lang.String)
meth public abstract void setResType(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.RootInterface
fld public final static int MERGE_INTERSECT = 1
fld public final static int MERGE_UNION = 2
fld public final static int MERGE_UPDATE = 3
intf org.netbeans.modules.j2ee.dd.api.common.ComponentInterface
meth public abstract void merge(org.netbeans.modules.j2ee.dd.api.common.RootInterface,int)
meth public abstract void write(org.openide.filesystems.FileObject) throws java.io.IOException

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.RunAs
fld public final static java.lang.String ROLE_NAME = "RoleName"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract java.lang.String getRoleName()
meth public abstract void setRoleName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.SecurityRole
fld public final static java.lang.String ROLE_NAME = "RoleName"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract java.lang.String getRoleName()
meth public abstract void setRoleName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef
fld public final static java.lang.String ROLE_LINK = "RoleLink"
fld public final static java.lang.String ROLE_NAME = "RoleName"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract java.lang.String getRoleLink()
meth public abstract java.lang.String getRoleName()
meth public abstract void setRoleLink(java.lang.String)
meth public abstract void setRoleName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.ServiceRef
fld public final static java.lang.String HANDLER = "Handler"
fld public final static java.lang.String JAXRPC_MAPPING_FILE = "JaxrpcMappingFile"
fld public final static java.lang.String PORT_COMPONENT_REF = "PortComponentRef"
fld public final static java.lang.String SERVICE_INTERFACE = "ServiceInterface"
fld public final static java.lang.String SERVICE_QNAME = "ServiceQname"
fld public final static java.lang.String SERVICE_REF_NAME = "ServiceRefName"
fld public final static java.lang.String WSDL_FILE = "WsdlFile"
intf org.netbeans.modules.j2ee.dd.api.common.ComponentInterface
meth public abstract int addHandler(org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler)
meth public abstract int addPortComponentRef(org.netbeans.modules.j2ee.dd.api.common.PortComponentRef)
meth public abstract int removeHandler(org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler)
meth public abstract int removePortComponentRef(org.netbeans.modules.j2ee.dd.api.common.PortComponentRef)
meth public abstract int sizeHandler()
meth public abstract int sizePortComponentRef()
meth public abstract java.lang.String getJaxrpcMappingFile()
meth public abstract java.lang.String getMappedName() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getServiceInterface()
meth public abstract java.lang.String getServiceQname()
meth public abstract java.lang.String getServiceRefName()
meth public abstract java.net.URI getWsdlFile()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.PortComponentRef getPortComponentRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.PortComponentRef newPortComponentRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.PortComponentRef[] getPortComponentRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler getHandler(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler newServiceRefHandler() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandlerChains getHandlerChains() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandlerChains newServiceRefHandlerChains() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler[] getHandler()
meth public abstract void setHandler(int,org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler)
meth public abstract void setHandler(org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler[])
meth public abstract void setHandlerChains(org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandlerChains) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setJaxrpcMappingFile(java.lang.String)
meth public abstract void setMappedName(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPortComponentRef(int,org.netbeans.modules.j2ee.dd.api.common.PortComponentRef)
meth public abstract void setPortComponentRef(org.netbeans.modules.j2ee.dd.api.common.PortComponentRef[])
meth public abstract void setServiceInterface(java.lang.String)
meth public abstract void setServiceQname(java.lang.String)
meth public abstract void setServiceRefName(java.lang.String)
meth public abstract void setWsdlFile(java.net.URI)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler
fld public final static java.lang.String HANDLER_CLASS = "HandlerClass"
fld public final static java.lang.String HANDLER_NAME = "HandlerName"
fld public final static java.lang.String INIT_PARAM = "InitParam"
fld public final static java.lang.String PORT_NAME = "PortName"
fld public final static java.lang.String SOAP_HEADER = "SoapHeader"
fld public final static java.lang.String SOAP_ROLE = "SoapRole"
intf org.netbeans.modules.j2ee.dd.api.common.ComponentInterface
meth public abstract int addInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam)
meth public abstract int addPortName(java.lang.String)
meth public abstract int addSoapHeader(java.lang.String)
meth public abstract int addSoapRole(java.lang.String)
meth public abstract int removeInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam)
meth public abstract int removePortName(java.lang.String)
meth public abstract int removeSoapHeader(java.lang.String)
meth public abstract int removeSoapRole(java.lang.String)
meth public abstract int sizeInitParam()
meth public abstract int sizePortName()
meth public abstract int sizeSoapHeader()
meth public abstract int sizeSoapRole()
meth public abstract java.lang.String getHandlerClass()
meth public abstract java.lang.String getHandlerName()
meth public abstract java.lang.String getPortName(int)
meth public abstract java.lang.String getSoapHeader(int)
meth public abstract java.lang.String getSoapRole(int)
meth public abstract java.lang.String[] getPortName()
meth public abstract java.lang.String[] getSoapHeader()
meth public abstract java.lang.String[] getSoapRole()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InitParam getInitParam(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InitParam[] getInitParam()
meth public abstract void setHandlerClass(java.lang.String)
meth public abstract void setHandlerName(java.lang.String)
meth public abstract void setInitParam(int,org.netbeans.modules.j2ee.dd.api.common.InitParam)
meth public abstract void setInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam[])
meth public abstract void setPortName(int,java.lang.String)
meth public abstract void setPortName(java.lang.String[])
meth public abstract void setSoapHeader(int,java.lang.String)
meth public abstract void setSoapHeader(java.lang.String[])
meth public abstract void setSoapRole(int,java.lang.String)
meth public abstract void setSoapRole(java.lang.String[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandlerChain
meth public abstract int addHandler(org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler)
meth public abstract int removeHandler(org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler)
meth public abstract int sizeHandler()
meth public abstract java.lang.String getPortNamePattern()
meth public abstract java.lang.String getProtocolBindings()
meth public abstract java.lang.String getServiceNamePattern()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler getHandler(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler newServiceRefHandler()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler[] getHandler()
meth public abstract void setHandler(int,org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler)
meth public abstract void setHandler(org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandler[])
meth public abstract void setPortNamePattern(java.lang.String)
meth public abstract void setProtocolBindings(java.lang.String)
meth public abstract void setServiceNamePattern(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandlerChains
meth public abstract int addHandlerChain(org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandlerChain)
meth public abstract int removeHandlerChain(org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandlerChain)
meth public abstract int sizeHandlerChain()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandlerChain getHandlerChain(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandlerChain newServiceRefHandlerChain()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandlerChain[] getHandlerChain()
meth public abstract void setHandlerChain(int,org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandlerChain)
meth public abstract void setHandlerChain(org.netbeans.modules.j2ee.dd.api.common.ServiceRefHandlerChain[])

CLSS public org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
cons public init(java.lang.String)
cons public init(java.lang.String,java.lang.String)
meth public java.lang.String getVersion()
supr java.lang.Exception
hfds version

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfig
fld public final static java.lang.String ACTIVATION_CONFIG_PROPERTY = "ActivationConfigProperty"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract int addActivationConfigProperty(org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty)
meth public abstract int removeActivationConfigProperty(org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty)
meth public abstract int sizeActivationConfigProperty()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty getActivationConfigProperty(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty newActivationConfigProperty()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty[] getActivationConfigProperty()
meth public abstract void setActivationConfigProperty(int,org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty)
meth public abstract void setActivationConfigProperty(org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfigProperty
fld public final static java.lang.String ACTIVATIONCONFIGPROPERTYNAMEID = "ActivationConfigPropertyNameId"
fld public final static java.lang.String ACTIVATIONCONFIGPROPERTYVALUEID = "ActivationConfigPropertyValueId"
fld public final static java.lang.String ACTIVATION_CONFIG_PROPERTY_NAME = "ActivationConfigPropertyName"
fld public final static java.lang.String ACTIVATION_CONFIG_PROPERTY_VALUE = "ActivationConfigPropertyValue"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.lang.String getActivationConfigPropertyName()
meth public abstract java.lang.String getActivationConfigPropertyNameId()
meth public abstract java.lang.String getActivationConfigPropertyValue()
meth public abstract java.lang.String getActivationConfigPropertyValueId()
meth public abstract void setActivationConfigPropertyName(java.lang.String)
meth public abstract void setActivationConfigPropertyNameId(java.lang.String)
meth public abstract void setActivationConfigPropertyValue(java.lang.String)
meth public abstract void setActivationConfigPropertyValueId(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.ApplicationException
meth public abstract boolean isRollback()
meth public abstract java.lang.String getExceptionClass()
meth public abstract void setExceptionClass(java.lang.String)
meth public abstract void setRollback(boolean)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke
meth public abstract java.lang.String getClass2()
meth public abstract java.lang.String getMethodName()
meth public abstract void setClass2(java.lang.String)
meth public abstract void setMethodName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor
fld public final static java.lang.String CONTAINER_TRANSACTION = "ContainerTransaction"
fld public final static java.lang.String EXCLUDE_LIST = "ExcludeList"
fld public final static java.lang.String MESSAGE_DESTINATION = "MessageDestination"
fld public final static java.lang.String METHOD_PERMISSION = "MethodPermission"
fld public final static java.lang.String SECURITY_ROLE = "SecurityRole"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract int addContainerTransaction(org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction)
meth public abstract int addMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addMethodPermission(org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission)
meth public abstract int addSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole)
meth public abstract int removeContainerTransaction(org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction)
meth public abstract int removeMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeMethodPermission(org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission)
meth public abstract int removeSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole)
meth public abstract int sizeContainerTransaction()
meth public abstract int sizeMessageDestination() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeMethodPermission()
meth public abstract int sizeSecurityRole()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestination getMessageDestination(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestination newMessageDestination() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestination[] getMessageDestination() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.SecurityRole getSecurityRole(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.SecurityRole newSecurityRole()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] getSecurityRole()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction getContainerTransaction(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction newContainerTransaction()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction[] getContainerTransaction()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.ExcludeList getExcludeList()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.ExcludeList newExcludeList()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission getMethodPermission(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission newMethodPermission()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission[] getMethodPermission()
meth public abstract void setContainerTransaction(int,org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction)
meth public abstract void setContainerTransaction(org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction[])
meth public abstract void setExcludeList(org.netbeans.modules.j2ee.dd.api.ejb.ExcludeList)
meth public abstract void setMessageDestination(int,org.netbeans.modules.j2ee.dd.api.common.MessageDestination) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMethodPermission(int,org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission)
meth public abstract void setMethodPermission(org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission[])
meth public abstract void setSecurityRole(int,org.netbeans.modules.j2ee.dd.api.common.SecurityRole)
meth public abstract void setSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.CmpField
fld public final static java.lang.String FIELD_NAME = "FieldName"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract java.lang.String getFieldName()
meth public abstract void setFieldName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.CmrField
fld public final static java.lang.String CMRFIELDNAMEID = "CmrFieldNameId"
fld public final static java.lang.String CMR_FIELD_NAME = "CmrFieldName"
fld public final static java.lang.String CMR_FIELD_TYPE = "CmrFieldType"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract java.lang.String getCmrFieldName()
meth public abstract java.lang.String getCmrFieldNameId()
meth public abstract java.lang.String getCmrFieldType()
meth public abstract void setCmrFieldName(java.lang.String)
meth public abstract void setCmrFieldNameId(java.lang.String)
meth public abstract void setCmrFieldType(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.ContainerTransaction
fld public final static java.lang.String METHOD = "Method"
fld public final static java.lang.String TRANS_ATTRIBUTE = "TransAttribute"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract int addMethod(org.netbeans.modules.j2ee.dd.api.ejb.Method)
meth public abstract int removeMethod(org.netbeans.modules.j2ee.dd.api.ejb.Method)
meth public abstract int sizeMethod()
meth public abstract java.lang.String getTransAttribute()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Method getMethod(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Method newMethod()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Method[] getMethod()
meth public abstract void setMethod(int,org.netbeans.modules.j2ee.dd.api.ejb.Method)
meth public abstract void setMethod(org.netbeans.modules.j2ee.dd.api.ejb.Method[])
meth public abstract void setTransAttribute(java.lang.String)

CLSS public final org.netbeans.modules.j2ee.dd.api.ejb.DDProvider
meth public org.netbeans.modules.j2ee.dd.api.ejb.EjbJar getDDRoot(org.openide.filesystems.FileObject) throws java.io.IOException
meth public org.netbeans.modules.j2ee.dd.api.ejb.EjbJar getDDRoot(org.xml.sax.InputSource) throws java.io.IOException,org.xml.sax.SAXException
meth public org.netbeans.modules.j2ee.dd.api.ejb.EjbJar getDDRootCopy(org.openide.filesystems.FileObject) throws java.io.IOException
meth public org.netbeans.modules.schema2beans.BaseBean getBaseBean(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
meth public static org.netbeans.modules.j2ee.dd.api.ejb.DDProvider getDefault()
supr java.lang.Object
hfds EJB_21_DOCTYPE,EJB_30_DOCTYPE,EJB_31_DOCTYPE,EJB_32_DOCTYPE,EJB_40_DOCTYPE,ddMap,ddProvider
hcls DDFileChangeListener,DDResolver,ErrorHandler

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.Ejb
fld public final static java.lang.String EJB_CLASS = "EjbClass"
fld public final static java.lang.String EJB_LOCAL_REF = "EjbLocalRef"
fld public final static java.lang.String EJB_NAME = "EjbName"
fld public final static java.lang.String EJB_REF = "EjbRef"
fld public final static java.lang.String ENV_ENTRY = "EnvEntry"
fld public final static java.lang.String MESSAGE_DESTINATION_REF = "MessageDestinationRef"
fld public final static java.lang.String RESOURCE_ENV_REF = "ResourceEnvRef"
fld public final static java.lang.String RESOURCE_REF = "ResourceRef"
fld public final static java.lang.String SECURITY_IDENTITY = "SecurityIdentity"
fld public final static java.lang.String SERVICE_REF = "ServiceRef"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.ComponentInterface
meth public abstract int addEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef)
meth public abstract int addEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef)
meth public abstract int addEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry)
meth public abstract int addMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef)
meth public abstract int addResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef)
meth public abstract int addServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef)
meth public abstract int removeEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef)
meth public abstract int removeEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry)
meth public abstract int removeMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef)
meth public abstract int removeResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef)
meth public abstract int removeServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeEjbLocalRef()
meth public abstract int sizeEjbRef()
meth public abstract int sizeEnvEntry()
meth public abstract int sizeMessageDestinationRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeResourceEnvRef()
meth public abstract int sizeResourceRef()
meth public abstract int sizeServiceRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getEjbClass()
meth public abstract java.lang.String getEjbName()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef getEjbLocalRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef newEjbLocalRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef[] getEjbLocalRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbRef getEjbRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbRef newEjbRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbRef[] getEjbRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EnvEntry getEnvEntry(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EnvEntry newEnvEntry()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EnvEntry[] getEnvEntry()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef getMessageDestinationRef(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef newMessageDestinationRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[] getMessageDestinationRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef getResourceEnvRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef newResourceEnvRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[] getResourceEnvRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceRef getResourceRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceRef newResourceRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceRef[] getResourceRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRef getServiceRef(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRef newServiceRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRef[] getServiceRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.EjbJar getRoot()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.SecurityIdentity getSecurityIdentity()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.SecurityIdentity newSecurityIdentity()
meth public abstract void setEjbClass(java.lang.String)
meth public abstract void setEjbLocalRef(int,org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef)
meth public abstract void setEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef[])
meth public abstract void setEjbName(java.lang.String)
meth public abstract void setEjbRef(int,org.netbeans.modules.j2ee.dd.api.common.EjbRef)
meth public abstract void setEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef[])
meth public abstract void setEnvEntry(int,org.netbeans.modules.j2ee.dd.api.common.EnvEntry)
meth public abstract void setEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry[])
meth public abstract void setMessageDestinationRef(int,org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setResourceEnvRef(int,org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef)
meth public abstract void setResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[])
meth public abstract void setResourceRef(int,org.netbeans.modules.j2ee.dd.api.common.ResourceRef)
meth public abstract void setResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef[])
meth public abstract void setSecurityIdentity(org.netbeans.modules.j2ee.dd.api.ejb.SecurityIdentity)
meth public abstract void setServiceRef(int,org.netbeans.modules.j2ee.dd.api.common.ServiceRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.EjbJar
fld public final static int STATE_INVALID_PARSABLE = 1
fld public final static int STATE_INVALID_UNPARSABLE = 2
fld public final static int STATE_VALID = 0
fld public final static java.lang.String ASSEMBLY_DESCRIPTOR = "AssemblyDescriptor"
fld public final static java.lang.String EJB_CLIENT_JAR = "EjbClientJar"
fld public final static java.lang.String ENTERPRISE_BEANS = "EnterpriseBeans"
fld public final static java.lang.String PROPERTY_STATUS = "dd_status"
fld public final static java.lang.String PROPERTY_VERSION = "dd_version"
fld public final static java.lang.String RELATIONSHIPS = "Relationships"
fld public final static java.lang.String VERSION_2_1 = "2.1"
fld public final static java.lang.String VERSION_3_0 = "3.0"
fld public final static java.lang.String VERSION_3_1 = "3.1"
fld public final static java.lang.String VERSION_3_2 = "3.2"
fld public final static java.lang.String VERSION_4_0 = "4.0"
intf org.netbeans.modules.j2ee.dd.api.common.RootInterface
meth public abstract int getStatus()
meth public abstract java.lang.String getSingleEjbClientJar()
meth public abstract java.math.BigDecimal getVersion()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor getSingleAssemblyDescriptor()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor newAssemblyDescriptor()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans getEnterpriseBeans()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans newEnterpriseBeans()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Interceptors getInterceptors() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Interceptors newInterceptors() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Relationships getSingleRelationships()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Relationships newRelationships()
meth public abstract org.xml.sax.SAXParseException getError()
meth public abstract void setAssemblyDescriptor(org.netbeans.modules.j2ee.dd.api.ejb.AssemblyDescriptor)
meth public abstract void setEjbClientJar(java.lang.String)
meth public abstract void setEnterpriseBeans(org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans)
meth public abstract void setInterceptors(org.netbeans.modules.j2ee.dd.api.ejb.Interceptors) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setRelationships(org.netbeans.modules.j2ee.dd.api.ejb.Relationships)
meth public abstract void setVersion(java.math.BigDecimal)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Ejb findByEjbClass(java.lang.String)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.EjbJar getRoot()
meth public abstract org.openide.filesystems.FileObject findResource(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation
fld public final static java.lang.String EJBRELATIONNAMEID = "EjbRelationNameId"
fld public final static java.lang.String EJB_RELATIONSHIP_ROLE = "EjbRelationshipRole"
fld public final static java.lang.String EJB_RELATIONSHIP_ROLE2 = "EjbRelationshipRole2"
fld public final static java.lang.String EJB_RELATION_NAME = "EjbRelationName"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract java.lang.String getEjbRelationName()
meth public abstract java.lang.String getEjbRelationNameId()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole getEjbRelationshipRole()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole getEjbRelationshipRole2()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole newEjbRelationshipRole()
meth public abstract void setEjbRelationName(java.lang.String)
meth public abstract void setEjbRelationNameId(java.lang.String)
meth public abstract void setEjbRelationshipRole(org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole)
meth public abstract void setEjbRelationshipRole2(org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.EjbRelationshipRole
fld public final static java.lang.String CASCADEDELETEID = "CascadeDeleteId"
fld public final static java.lang.String CASCADE_DELETE = "CascadeDelete"
fld public final static java.lang.String CMR_FIELD = "CmrField"
fld public final static java.lang.String EJBRELATIONSHIPROLENAMEID = "EjbRelationshipRoleNameId"
fld public final static java.lang.String EJB_RELATIONSHIP_ROLE_NAME = "EjbRelationshipRoleName"
fld public final static java.lang.String MULTIPLICITY = "Multiplicity"
fld public final static java.lang.String MULTIPLICITY_MANY = "Many"
fld public final static java.lang.String MULTIPLICITY_ONE = "One"
fld public final static java.lang.String RELATIONSHIP_ROLE_SOURCE = "RelationshipRoleSource"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract boolean isCascadeDelete()
meth public abstract java.lang.String getEjbRelationshipRoleName()
meth public abstract java.lang.String getEjbRelationshipRoleNameId()
meth public abstract java.lang.String getMultiplicity()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.CmrField getCmrField()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.CmrField newCmrField()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.RelationshipRoleSource getRelationshipRoleSource()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.RelationshipRoleSource newRelationshipRoleSource()
meth public abstract void setCascadeDelete(boolean)
meth public abstract void setCmrField(org.netbeans.modules.j2ee.dd.api.ejb.CmrField)
meth public abstract void setEjbRelationshipRoleName(java.lang.String)
meth public abstract void setEjbRelationshipRoleNameId(java.lang.String)
meth public abstract void setMultiplicity(java.lang.String)
meth public abstract void setRelationshipRoleSource(org.netbeans.modules.j2ee.dd.api.ejb.RelationshipRoleSource)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans
fld public final static java.lang.String ENTITY = "Entity"
fld public final static java.lang.String MESSAGE_DRIVEN = "MessageDriven"
fld public final static java.lang.String SESSION = "Session"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.FindCapability
meth public abstract int addEntity(org.netbeans.modules.j2ee.dd.api.ejb.Entity)
meth public abstract int addMessageDriven(org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven)
meth public abstract int addSession(org.netbeans.modules.j2ee.dd.api.ejb.Session)
meth public abstract int removeEntity(org.netbeans.modules.j2ee.dd.api.ejb.Entity)
meth public abstract int removeMessageDriven(org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven)
meth public abstract int removeSession(org.netbeans.modules.j2ee.dd.api.ejb.Session)
meth public abstract int sizeEntity()
meth public abstract int sizeMessageDriven()
meth public abstract int sizeSession()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Ejb[] getEjbs()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Entity getEntity(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Entity newEntity()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Entity[] getEntity()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven getMessageDriven(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven newMessageDriven()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven[] getMessageDriven()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Session getSession(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Session newSession()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Session[] getSession()
meth public abstract void removeEjb(org.netbeans.modules.j2ee.dd.api.ejb.Ejb)
meth public abstract void setEntity(int,org.netbeans.modules.j2ee.dd.api.ejb.Entity)
meth public abstract void setEntity(org.netbeans.modules.j2ee.dd.api.ejb.Entity[])
meth public abstract void setMessageDriven(int,org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven)
meth public abstract void setMessageDriven(org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven[])
meth public abstract void setSession(int,org.netbeans.modules.j2ee.dd.api.ejb.Session)
meth public abstract void setSession(org.netbeans.modules.j2ee.dd.api.ejb.Session[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.Entity
fld public final static java.lang.String ABSTRACT_SCHEMA_NAME = "AbstractSchemaName"
fld public final static java.lang.String CMP_FIELD = "CmpField"
fld public final static java.lang.String CMP_VERSION = "CmpVersion"
fld public final static java.lang.String CMP_VERSION_ONE = "1.x"
fld public final static java.lang.String CMP_VERSION_TWO = "2.x"
fld public final static java.lang.String PERSISTENCE_TYPE = "PersistenceType"
fld public final static java.lang.String PERSISTENCE_TYPE_BEAN = "Bean"
fld public final static java.lang.String PERSISTENCE_TYPE_CONTAINER = "Container"
fld public final static java.lang.String PRIMKEYFIELDID = "PrimkeyFieldId"
fld public final static java.lang.String PRIMKEY_FIELD = "PrimkeyField"
fld public final static java.lang.String PRIM_KEY_CLASS = "PrimKeyClass"
fld public final static java.lang.String QUERY = "Query"
fld public final static java.lang.String REENTRANT = "Reentrant"
intf org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession
meth public abstract boolean isReentrant()
meth public abstract int addCmpField(org.netbeans.modules.j2ee.dd.api.ejb.CmpField)
meth public abstract int addQuery(org.netbeans.modules.j2ee.dd.api.ejb.Query)
meth public abstract int removeCmpField(org.netbeans.modules.j2ee.dd.api.ejb.CmpField)
meth public abstract int removeQuery(org.netbeans.modules.j2ee.dd.api.ejb.Query)
meth public abstract int sizeCmpField()
meth public abstract int sizeQuery()
meth public abstract java.lang.String getAbstractSchemaName()
meth public abstract java.lang.String getCmpVersion()
meth public abstract java.lang.String getPersistenceType()
meth public abstract java.lang.String getPrimKeyClass()
meth public abstract java.lang.String getPrimkeyField()
meth public abstract java.lang.String getPrimkeyFieldId()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.CmpField getCmpField(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.CmpField newCmpField()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.CmpField[] getCmpField()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Query getQuery(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Query newQuery()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Query[] getQuery()
meth public abstract void setAbstractSchemaName(java.lang.String)
meth public abstract void setCmpField(int,org.netbeans.modules.j2ee.dd.api.ejb.CmpField)
meth public abstract void setCmpField(org.netbeans.modules.j2ee.dd.api.ejb.CmpField[])
meth public abstract void setCmpVersion(java.lang.String)
meth public abstract void setPersistenceType(java.lang.String)
meth public abstract void setPrimKeyClass(java.lang.String)
meth public abstract void setPrimkeyField(java.lang.String)
meth public abstract void setPrimkeyFieldId(java.lang.String)
meth public abstract void setQuery(int,org.netbeans.modules.j2ee.dd.api.ejb.Query)
meth public abstract void setQuery(org.netbeans.modules.j2ee.dd.api.ejb.Query[])
meth public abstract void setReentrant(boolean)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession
fld public final static java.lang.String HOME = "Home"
fld public final static java.lang.String LOCAL = "Local"
fld public final static java.lang.String LOCAL_HOME = "LocalHome"
fld public final static java.lang.String REMOTE = "Remote"
fld public final static java.lang.String SECURITY_ROLE_REF = "SecurityRoleRef"
intf org.netbeans.modules.j2ee.dd.api.ejb.Ejb
meth public abstract int addSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef)
meth public abstract int removeSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef)
meth public abstract int sizeSecurityRoleRef()
meth public abstract java.lang.String getHome()
meth public abstract java.lang.String getLocal()
meth public abstract java.lang.String getLocalHome()
meth public abstract java.lang.String getRemote()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef getSecurityRoleRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef newSecurityRoleRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef[] getSecurityRoleRef()
meth public abstract void setHome(java.lang.String)
meth public abstract void setLocal(java.lang.String)
meth public abstract void setLocalHome(java.lang.String)
meth public abstract void setRemote(java.lang.String)
meth public abstract void setSecurityRoleRef(int,org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef)
meth public abstract void setSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.ExcludeList
fld public final static java.lang.String METHOD = "Method"
meth public abstract int addMethod(org.netbeans.modules.j2ee.dd.api.ejb.Method)
meth public abstract int removeMethod(org.netbeans.modules.j2ee.dd.api.ejb.Method)
meth public abstract int sizeMethod()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Method getMethod(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Method newMethod()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Method[] getMethod()
meth public abstract void setMethod(int,org.netbeans.modules.j2ee.dd.api.ejb.Method)
meth public abstract void setMethod(org.netbeans.modules.j2ee.dd.api.ejb.Method[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.InitMethod
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod getBeanMethod()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod getCreateMethod()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod newNamedMethod()
meth public abstract void setBeanMethod(org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod)
meth public abstract void setCreateMethod(org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.Interceptor
meth public abstract int addAroundInvoke(org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke)
meth public abstract int addDescription(java.lang.String)
meth public abstract int addEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef)
meth public abstract int addEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef)
meth public abstract int addEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry)
meth public abstract int addMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef)
meth public abstract int addPersistenceContextRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef)
meth public abstract int addPersistenceUnitRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef)
meth public abstract int addPostActivate(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback)
meth public abstract int addPostConstruct(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback)
meth public abstract int addPreDestroy(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback)
meth public abstract int addPrePassivate(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback)
meth public abstract int addResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef)
meth public abstract int addResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef)
meth public abstract int addServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef)
meth public abstract int removeAroundInvoke(org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke)
meth public abstract int removeDescription(java.lang.String)
meth public abstract int removeEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef)
meth public abstract int removeEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef)
meth public abstract int removeEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry)
meth public abstract int removeMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef)
meth public abstract int removePersistenceContextRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef)
meth public abstract int removePersistenceUnitRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef)
meth public abstract int removePostActivate(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback)
meth public abstract int removePostConstruct(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback)
meth public abstract int removePreDestroy(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback)
meth public abstract int removePrePassivate(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback)
meth public abstract int removeResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef)
meth public abstract int removeResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef)
meth public abstract int removeServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef)
meth public abstract int sizeAroundInvoke()
meth public abstract int sizeDescription()
meth public abstract int sizeEjbLocalRef()
meth public abstract int sizeEjbRef()
meth public abstract int sizeEnvEntry()
meth public abstract int sizeMessageDestinationRef()
meth public abstract int sizePersistenceContextRef()
meth public abstract int sizePersistenceUnitRef()
meth public abstract int sizePostActivate()
meth public abstract int sizePostConstruct()
meth public abstract int sizePreDestroy()
meth public abstract int sizePrePassivate()
meth public abstract int sizeResourceEnvRef()
meth public abstract int sizeResourceRef()
meth public abstract int sizeServiceRef()
meth public abstract java.lang.String getDescription(int)
meth public abstract java.lang.String getInterceptorClass()
meth public abstract java.lang.String[] getDescription()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef getEjbLocalRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef newEjbLocalRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef[] getEjbLocalRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbRef getEjbRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbRef newEjbRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbRef[] getEjbRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EnvEntry getEnvEntry(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EnvEntry newEnvEntry()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EnvEntry[] getEnvEntry()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef getMessageDestinationRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef newMessageDestinationRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[] getMessageDestinationRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef getResourceEnvRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef newResourceEnvRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[] getResourceEnvRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceRef getResourceRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceRef newResourceRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceRef[] getResourceRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRef getServiceRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRef newServiceRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRef[] getServiceRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke getAroundInvoke(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke newAroundInvoke()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke[] getAroundInvoke()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback getPostActivate(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback getPostConstruct(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback getPreDestroy(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback getPrePassivate(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback newLifecycleCallback()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[] getPostActivate()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[] getPostConstruct()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[] getPreDestroy()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[] getPrePassivate()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef getPersistenceContextRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef newPersistenceContextRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef[] getPersistenceContextRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef getPersistenceUnitRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef newPersistenceUnitRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef[] getPersistenceUnitRef()
meth public abstract void setAroundInvoke(int,org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke)
meth public abstract void setAroundInvoke(org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke[])
meth public abstract void setDescription(int,java.lang.String)
meth public abstract void setDescription(java.lang.String[])
meth public abstract void setEjbLocalRef(int,org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef)
meth public abstract void setEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef[])
meth public abstract void setEjbRef(int,org.netbeans.modules.j2ee.dd.api.common.EjbRef)
meth public abstract void setEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef[])
meth public abstract void setEnvEntry(int,org.netbeans.modules.j2ee.dd.api.common.EnvEntry)
meth public abstract void setEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry[])
meth public abstract void setInterceptorClass(java.lang.String)
meth public abstract void setMessageDestinationRef(int,org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef)
meth public abstract void setMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[])
meth public abstract void setPersistenceContextRef(int,org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef)
meth public abstract void setPersistenceContextRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef[])
meth public abstract void setPersistenceUnitRef(int,org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef)
meth public abstract void setPersistenceUnitRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef[])
meth public abstract void setPostActivate(int,org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback)
meth public abstract void setPostActivate(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[])
meth public abstract void setPostConstruct(int,org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback)
meth public abstract void setPostConstruct(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[])
meth public abstract void setPreDestroy(int,org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback)
meth public abstract void setPreDestroy(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[])
meth public abstract void setPrePassivate(int,org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback)
meth public abstract void setPrePassivate(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[])
meth public abstract void setResourceEnvRef(int,org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef)
meth public abstract void setResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[])
meth public abstract void setResourceRef(int,org.netbeans.modules.j2ee.dd.api.common.ResourceRef)
meth public abstract void setResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef[])
meth public abstract void setServiceRef(int,org.netbeans.modules.j2ee.dd.api.common.ServiceRef)
meth public abstract void setServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.InterceptorBinding
meth public abstract boolean isExcludeClassInterceptors()
meth public abstract boolean isExcludeDefaultInterceptors()
meth public abstract int addDescription(java.lang.String)
meth public abstract int addInterceptorClass(java.lang.String)
meth public abstract int removeDescription(java.lang.String)
meth public abstract int removeInterceptorClass(java.lang.String)
meth public abstract int sizeDescription()
meth public abstract int sizeInterceptorClass()
meth public abstract java.lang.String getDescription(int)
meth public abstract java.lang.String getEjbName()
meth public abstract java.lang.String getInterceptorClass(int)
meth public abstract java.lang.String[] getDescription()
meth public abstract java.lang.String[] getInterceptorClass()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.InterceptorOrder getInterceptorOrder()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.InterceptorOrder newInterceptorOrder()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod getMethod()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod newNamedMethod()
meth public abstract void setDescription(int,java.lang.String)
meth public abstract void setDescription(java.lang.String[])
meth public abstract void setEjbName(java.lang.String)
meth public abstract void setExcludeClassInterceptors(boolean)
meth public abstract void setExcludeDefaultInterceptors(boolean)
meth public abstract void setInterceptorClass(int,java.lang.String)
meth public abstract void setInterceptorClass(java.lang.String[])
meth public abstract void setInterceptorOrder(org.netbeans.modules.j2ee.dd.api.ejb.InterceptorOrder)
meth public abstract void setMethod(org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.InterceptorOrder
meth public abstract int addInterceptorClass(java.lang.String)
meth public abstract int removeInterceptorClass(java.lang.String)
meth public abstract int sizeInterceptorClass()
meth public abstract java.lang.String getInterceptorClass(int)
meth public abstract java.lang.String[] getInterceptorClass()
meth public abstract void setInterceptorClass(int,java.lang.String)
meth public abstract void setInterceptorClass(java.lang.String[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.Interceptors
meth public abstract int addDescription(java.lang.String)
meth public abstract int addInterceptor(org.netbeans.modules.j2ee.dd.api.ejb.Interceptor)
meth public abstract int removeDescription(java.lang.String)
meth public abstract int removeInterceptor(org.netbeans.modules.j2ee.dd.api.ejb.Interceptor)
meth public abstract int sizeDescription()
meth public abstract int sizeInterceptor()
meth public abstract java.lang.String getDescription(int)
meth public abstract java.lang.String[] getDescription()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Interceptor getInterceptor(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Interceptor newInterceptor()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Interceptor[] getInterceptor()
meth public abstract void setDescription(int,java.lang.String)
meth public abstract void setDescription(java.lang.String[])
meth public abstract void setInterceptor(int,org.netbeans.modules.j2ee.dd.api.ejb.Interceptor)
meth public abstract void setInterceptor(org.netbeans.modules.j2ee.dd.api.ejb.Interceptor[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback
meth public abstract java.lang.String getLifecycleCallbackClass()
meth public abstract java.lang.String getLifecycleCallbackMethod()
meth public abstract void setLifecycleCallbackClass(java.lang.String)
meth public abstract void setLifecycleCallbackMethod(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.MessageDriven
fld public final static java.lang.String ACTIVATION_CONFIG = "ActivationConfig"
fld public final static java.lang.String MESSAGE_DESTINATION_LINK = "MessageDestinationLink"
fld public final static java.lang.String MESSAGE_DESTINATION_TYPE = "MessageDestinationType"
fld public final static java.lang.String MESSAGING_TYPE = "MessagingType"
fld public final static java.lang.String TRANSACTION_TYPE = "TransactionType"
fld public final static java.lang.String TRANSACTION_TYPE_BEAN = "Bean"
fld public final static java.lang.String TRANSACTION_TYPE_CONTAINER = "Container"
intf org.netbeans.modules.j2ee.dd.api.ejb.Ejb
meth public abstract int addAroundInvoke(org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addPersistenceContextRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addPersistenceUnitRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addPostConstruct(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addPreDestroy(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeAroundInvoke(org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removePersistenceContextRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removePersistenceUnitRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removePostConstruct(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removePreDestroy(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeAroundInvoke() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizePersistenceContextRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizePersistenceUnitRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizePostConstruct() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizePreDestroy() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getMappedName() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getMessageDestinationLink() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getMessageDestinationType() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getMessagingType() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getTransactionType()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfig getActivationConfig() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfig newActivationConfig() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke getAroundInvoke(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke newAroundInvoke() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke[] getAroundInvoke() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback getPostConstruct(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback getPreDestroy(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback newLifecycleCallback() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[] getPostConstruct() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[] getPreDestroy() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod getTimeoutMethod() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod newNamedMethod() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef getPersistenceContextRef(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef newPersistenceContextRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef[] getPersistenceContextRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef getPersistenceUnitRef(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef newPersistenceUnitRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef[] getPersistenceUnitRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setActivationConfig(org.netbeans.modules.j2ee.dd.api.ejb.ActivationConfig) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setAroundInvoke(int,org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setAroundInvoke(org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMappedName(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMessageDestinationLink(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMessageDestinationType(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMessagingType(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPersistenceContextRef(int,org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPersistenceContextRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPersistenceUnitRef(int,org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPersistenceUnitRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPostConstruct(int,org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPostConstruct(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPreDestroy(int,org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPreDestroy(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setTimeoutMethod(org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setTransactionType(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.Method
fld public final static java.lang.String EJB_NAME = "EjbName"
fld public final static java.lang.String METHOD_INTF = "MethodIntf"
fld public final static java.lang.String METHOD_NAME = "MethodName"
fld public final static java.lang.String METHOD_PARAMS = "MethodParams"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract java.lang.String getEjbName()
meth public abstract java.lang.String getMethodIntf()
meth public abstract java.lang.String getMethodName()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.MethodParams getMethodParams()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.MethodParams newMethodParams()
meth public abstract void setEjbName(java.lang.String)
meth public abstract void setMethodIntf(java.lang.String)
meth public abstract void setMethodName(java.lang.String)
meth public abstract void setMethodParams(org.netbeans.modules.j2ee.dd.api.ejb.MethodParams)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.MethodParams
fld public final static java.lang.String METHOD_PARAM = "MethodParam"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract int addMethodParam(java.lang.String)
meth public abstract int removeMethodParam(java.lang.String)
meth public abstract int sizeMethodParam()
meth public abstract java.lang.String getMethodParam(int)
meth public abstract java.lang.String[] getMethodParam()
meth public abstract void setMethodParam(int,java.lang.String)
meth public abstract void setMethodParam(java.lang.String[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.MethodPermission
fld public final static java.lang.String METHOD = "Method"
fld public final static java.lang.String ROLE_NAME = "RoleName"
fld public final static java.lang.String UNCHECKED = "Unchecked"
fld public final static java.lang.String UNCHECKEDID = "UncheckedId"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract boolean isUnchecked() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addMethod(org.netbeans.modules.j2ee.dd.api.ejb.Method)
meth public abstract int addRoleName(java.lang.String)
meth public abstract int removeMethod(org.netbeans.modules.j2ee.dd.api.ejb.Method)
meth public abstract int removeRoleName(java.lang.String)
meth public abstract int sizeMethod()
meth public abstract int sizeRoleName()
meth public abstract java.lang.String getRoleName(int)
meth public abstract java.lang.String[] getRoleName()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Method getMethod(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Method newMethod()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Method[] getMethod()
meth public abstract void setMethod(int,org.netbeans.modules.j2ee.dd.api.ejb.Method)
meth public abstract void setMethod(org.netbeans.modules.j2ee.dd.api.ejb.Method[])
meth public abstract void setRoleName(int,java.lang.String)
meth public abstract void setRoleName(java.lang.String[])
meth public abstract void setUnchecked(boolean) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod
meth public abstract java.lang.String getMethodName()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.MethodParams getMethodParams()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.MethodParams newMethodParams()
meth public abstract void setMethodName(java.lang.String)
meth public abstract void setMethodParams(org.netbeans.modules.j2ee.dd.api.ejb.MethodParams)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef
meth public abstract int addDescription(java.lang.String)
meth public abstract int addInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget)
meth public abstract int addPersistenceProperty(org.netbeans.modules.j2ee.dd.api.ejb.Property)
meth public abstract int removeDescription(java.lang.String)
meth public abstract int removeInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget)
meth public abstract int removePersistenceProperty(org.netbeans.modules.j2ee.dd.api.ejb.Property)
meth public abstract int sizeDescription()
meth public abstract int sizeInjectionTarget()
meth public abstract int sizePersistenceProperty()
meth public abstract java.lang.String getDescription(int)
meth public abstract java.lang.String getMappedName()
meth public abstract java.lang.String getPersistenceContextRefName()
meth public abstract java.lang.String getPersistenceContextType()
meth public abstract java.lang.String getPersistenceUnitName()
meth public abstract java.lang.String[] getDescription()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget getInjectionTarget(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget newInjectionTarget()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget[] getInjectionTarget()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Property getPersistenceProperty(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Property newProperty()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.Property[] getPersistenceProperty()
meth public abstract void setDescription(int,java.lang.String)
meth public abstract void setDescription(java.lang.String[])
meth public abstract void setInjectionTarget(int,org.netbeans.modules.j2ee.dd.api.common.InjectionTarget)
meth public abstract void setInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget[])
meth public abstract void setMappedName(java.lang.String)
meth public abstract void setPersistenceContextRefName(java.lang.String)
meth public abstract void setPersistenceContextType(java.lang.String)
meth public abstract void setPersistenceProperty(int,org.netbeans.modules.j2ee.dd.api.ejb.Property)
meth public abstract void setPersistenceProperty(org.netbeans.modules.j2ee.dd.api.ejb.Property[])
meth public abstract void setPersistenceUnitName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef
meth public abstract int addDescription(java.lang.String)
meth public abstract int addInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget)
meth public abstract int removeDescription(java.lang.String)
meth public abstract int removeInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget)
meth public abstract int sizeDescription()
meth public abstract int sizeInjectionTarget()
meth public abstract java.lang.String getDescription(int)
meth public abstract java.lang.String getMappedName()
meth public abstract java.lang.String getPersistenceUnitName()
meth public abstract java.lang.String getPersistenceUnitRefName()
meth public abstract java.lang.String[] getDescription()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget getInjectionTarget(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget newInjectionTarget()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InjectionTarget[] getInjectionTarget()
meth public abstract void setDescription(int,java.lang.String)
meth public abstract void setDescription(java.lang.String[])
meth public abstract void setInjectionTarget(int,org.netbeans.modules.j2ee.dd.api.common.InjectionTarget)
meth public abstract void setInjectionTarget(org.netbeans.modules.j2ee.dd.api.common.InjectionTarget[])
meth public abstract void setMappedName(java.lang.String)
meth public abstract void setPersistenceUnitName(java.lang.String)
meth public abstract void setPersistenceUnitRefName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.Property
meth public abstract java.lang.String getName()
meth public abstract java.lang.String getValue()
meth public abstract void setName(java.lang.String)
meth public abstract void setValue(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.Query
fld public final static java.lang.String EJBQLID = "EjbQlId"
fld public final static java.lang.String EJB_QL = "EjbQl"
fld public final static java.lang.String QUERY_METHOD = "QueryMethod"
fld public final static java.lang.String RESULT_TYPE_MAPPING = "ResultTypeMapping"
fld public final static java.lang.String RESULT_TYPE_MAPPING_LOCAL = "Local"
fld public final static java.lang.String RESULT_TYPE_MAPPING_REMOTE = "Remote"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract java.lang.String getEjbQl()
meth public abstract java.lang.String getResultTypeMapping()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.QueryMethod getQueryMethod()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.QueryMethod newQueryMethod()
meth public abstract void setEjbQl(java.lang.String)
meth public abstract void setQueryMethod(org.netbeans.modules.j2ee.dd.api.ejb.QueryMethod)
meth public abstract void setResultTypeMapping(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.QueryMethod
fld public final static java.lang.String METHOD_NAME = "MethodName"
fld public final static java.lang.String METHOD_PARAMS = "MethodParams"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.lang.String getMethodName()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.MethodParams getMethodParams()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.MethodParams newMethodParams()
meth public abstract void setMethodName(java.lang.String)
meth public abstract void setMethodParams(org.netbeans.modules.j2ee.dd.api.ejb.MethodParams)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.RelationshipRoleSource
fld public final static java.lang.String EJB_NAME = "EjbName"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract java.lang.String getEjbName()
meth public abstract void setEjbName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.Relationships
fld public final static java.lang.String EJB_RELATION = "EjbRelation"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract int addEjbRelation(org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation)
meth public abstract int removeEjbRelation(org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation)
meth public abstract int sizeEjbRelation()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation getEjbRelation(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation newEjbRelation()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation[] getEjbRelation()
meth public abstract void setEjbRelation(int,org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation)
meth public abstract void setEjbRelation(org.netbeans.modules.j2ee.dd.api.ejb.EjbRelation[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.RemoveMethod
meth public abstract boolean isRetainIfException()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod getBeanMethod()
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod newNamedMethod()
meth public abstract void setBeanMethod(org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod)
meth public abstract void setRetainIfException(boolean)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.SecurityIdentity
fld public final static java.lang.String RUN_AS = "RunAs"
fld public final static java.lang.String USE_CALLER_IDENTITY = "UseCallerIdentity"
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract boolean isUseCallerIdentity()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.RunAs getRunAs()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.RunAs newRunAs()
meth public abstract void setRunAs(org.netbeans.modules.j2ee.dd.api.common.RunAs)
meth public abstract void setUseCallerIdentity(boolean)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.ejb.Session
fld public final static java.lang.String BUSINESS_LOCAL = "BusinessLocal"
fld public final static java.lang.String BUSINESS_REMOTE = "BusinessRemote"
fld public final static java.lang.String SERVICE_ENDPOINT = "ServiceEndpoint"
fld public final static java.lang.String SESSION_TYPE = "SessionType"
fld public final static java.lang.String SESSION_TYPE_SINGLETON = "Singleton"
fld public final static java.lang.String SESSION_TYPE_STATEFUL = "Stateful"
fld public final static java.lang.String SESSION_TYPE_STATELESS = "Stateless"
fld public final static java.lang.String TRANSACTION_TYPE = "TransactionType"
fld public final static java.lang.String TRANSACTION_TYPE_BEAN = "Bean"
fld public final static java.lang.String TRANSACTION_TYPE_CONTAINER = "Container"
intf org.netbeans.modules.j2ee.dd.api.ejb.EntityAndSession
meth public abstract boolean isLocalBean()
meth public abstract int addAroundInvoke(org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addBusinessLocal(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addBusinessRemote(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addInitMethod(org.netbeans.modules.j2ee.dd.api.ejb.InitMethod) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addPersistenceContextRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addPersistenceUnitRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addPostActivate(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addPostConstruct(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addPreDestroy(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addPrePassivate(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addRemoveMethod(org.netbeans.modules.j2ee.dd.api.ejb.RemoveMethod) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeAroundInvoke(org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeBusinessLocal(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeBusinessRemote(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeInitMethod(org.netbeans.modules.j2ee.dd.api.ejb.InitMethod) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removePersistenceContextRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removePersistenceUnitRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removePostActivate(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removePostConstruct(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removePreDestroy(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removePrePassivate(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeRemoveMethod(org.netbeans.modules.j2ee.dd.api.ejb.RemoveMethod) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeAroundInvoke() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeBusinessLocal() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeBusinessRemote() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeInitMethod() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizePersistenceContextRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizePersistenceUnitRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizePostActivate() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizePostConstruct() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizePreDestroy() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizePrePassivate() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeRemoveMethod() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getBusinessLocal(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getBusinessRemote(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getMappedName() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getServiceEndpoint() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getSessionType()
meth public abstract java.lang.String getTransactionType()
meth public abstract java.lang.String[] getBusinessLocal() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String[] getBusinessRemote() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke getAroundInvoke(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke newAroundInvoke() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke[] getAroundInvoke() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.InitMethod getInitMethod(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.InitMethod newInitMethod() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.InitMethod[] getInitMethod() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback getPostActivate(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback getPostConstruct(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback getPreDestroy(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback getPrePassivate(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback newLifecycleCallback() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[] getPostActivate() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[] getPostConstruct() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[] getPreDestroy() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[] getPrePassivate() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod getTimeoutMethod() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod newNamedMethod() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef getPersistenceContextRef(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef newPersistenceContextRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef[] getPersistenceContextRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef getPersistenceUnitRef(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef newPersistenceUnitRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef[] getPersistenceUnitRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.RemoveMethod getRemoveMethod(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.RemoveMethod newRemoveMethod() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.ejb.RemoveMethod[] getRemoveMethod() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setAroundInvoke(int,org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setAroundInvoke(org.netbeans.modules.j2ee.dd.api.ejb.AroundInvoke[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setBusinessLocal(int,java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setBusinessLocal(java.lang.String[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setBusinessRemote(int,java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setBusinessRemote(java.lang.String[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setInitMethod(int,org.netbeans.modules.j2ee.dd.api.ejb.InitMethod) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setInitMethod(org.netbeans.modules.j2ee.dd.api.ejb.InitMethod[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMappedName(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPersistenceContextRef(int,org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPersistenceContextRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceContextRef[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPersistenceUnitRef(int,org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPersistenceUnitRef(org.netbeans.modules.j2ee.dd.api.ejb.PersistenceUnitRef[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPostActivate(int,org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPostActivate(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPostConstruct(int,org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPostConstruct(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPreDestroy(int,org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPreDestroy(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPrePassivate(int,org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setPrePassivate(org.netbeans.modules.j2ee.dd.api.ejb.LifecycleCallback[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setRemoveMethod(int,org.netbeans.modules.j2ee.dd.api.ejb.RemoveMethod) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setRemoveMethod(org.netbeans.modules.j2ee.dd.api.ejb.RemoveMethod[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setServiceEndpoint(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setSessionType(java.lang.String)
meth public abstract void setTimeoutMethod(org.netbeans.modules.j2ee.dd.api.ejb.NamedMethod) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setTransactionType(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.AbsoluteOrdering
meth public abstract java.lang.String[] getName()
meth public abstract void setName(java.lang.String[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.AuthConstraint
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract int addRoleName(java.lang.String)
meth public abstract int removeRoleName(java.lang.String)
meth public abstract int sizeRoleName()
meth public abstract java.lang.String getRoleName(int)
meth public abstract java.lang.String[] getRoleName()
meth public abstract void setRoleName(int,java.lang.String)
meth public abstract void setRoleName(java.lang.String[])

CLSS public final org.netbeans.modules.j2ee.dd.api.web.DDProvider
meth public org.netbeans.modules.j2ee.dd.api.web.WebApp getDDRoot(java.io.File) throws java.io.IOException,org.xml.sax.SAXException
meth public org.netbeans.modules.j2ee.dd.api.web.WebApp getDDRoot(org.openide.filesystems.FileObject) throws java.io.IOException
meth public org.netbeans.modules.j2ee.dd.api.web.WebApp getDDRoot(org.openide.filesystems.FileObject,boolean) throws java.io.IOException
meth public org.netbeans.modules.j2ee.dd.api.web.WebApp getDDRootCopy(org.openide.filesystems.FileObject) throws java.io.IOException
meth public org.netbeans.modules.schema2beans.BaseBean getBaseBean(org.netbeans.modules.j2ee.dd.api.common.CommonDDBean)
 anno 0 java.lang.Deprecated()
meth public org.xml.sax.SAXParseException parse(org.openide.filesystems.FileObject) throws java.io.IOException,org.xml.sax.SAXException
meth public static org.netbeans.modules.j2ee.dd.api.web.DDProvider getDefault()
supr java.lang.Object
hfds LOGGER,baseBeanMap,ddMap,ddProvider,errorMap,fileChangeListener
hcls FCA

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.ErrorPage
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.lang.Integer getErrorCode()
meth public abstract java.lang.String getExceptionType()
meth public abstract java.lang.String getLocation()
meth public abstract void setErrorCode(java.lang.Integer)
meth public abstract void setExceptionType(java.lang.String)
meth public abstract void setLocation(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.Filter
intf org.netbeans.modules.j2ee.dd.api.common.ComponentInterface
meth public abstract int addInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam)
meth public abstract int removeInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam)
meth public abstract int sizeInitParam()
meth public abstract java.lang.String getFilterClass()
meth public abstract java.lang.String getFilterName()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InitParam getInitParam(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InitParam[] getInitParam()
meth public abstract void setFilterClass(java.lang.String)
meth public abstract void setFilterName(java.lang.String)
meth public abstract void setInitParam(int,org.netbeans.modules.j2ee.dd.api.common.InitParam)
meth public abstract void setInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.FilterMapping
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract int addDispatcher(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeDispatcher(java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeDispatcher() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getDispatcher(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String getFilterName()
meth public abstract java.lang.String getServletName()
meth public abstract java.lang.String getUrlPattern()
meth public abstract java.lang.String[] getDispatcher() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String[] getServletNames() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract java.lang.String[] getUrlPatterns() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setDispatcher(int,java.lang.String) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setDispatcher(java.lang.String[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setFilterName(java.lang.String)
meth public abstract void setServletName(java.lang.String)
meth public abstract void setServletNames(java.lang.String[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setUrlPattern(java.lang.String)
meth public abstract void setUrlPatterns(java.lang.String[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.FormLoginConfig
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.lang.String getFormErrorPage()
meth public abstract java.lang.String getFormLoginPage()
meth public abstract void setFormErrorPage(java.lang.String)
meth public abstract void setFormLoginPage(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.JspConfig
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.CreateCapability
intf org.netbeans.modules.j2ee.dd.api.common.FindCapability
meth public abstract int addJspPropertyGroup(org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup)
meth public abstract int addTaglib(org.netbeans.modules.j2ee.dd.api.web.Taglib)
meth public abstract int removeJspPropertyGroup(org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup)
meth public abstract int removeTaglib(org.netbeans.modules.j2ee.dd.api.web.Taglib)
meth public abstract int sizeJspPropertyGroup()
meth public abstract int sizeTaglib()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup getJspPropertyGroup(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup[] getJspPropertyGroup()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.Taglib getTaglib(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.web.Taglib[] getTaglib()
meth public abstract void setJspPropertyGroup(int,org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup)
meth public abstract void setJspPropertyGroup(org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup[])
meth public abstract void setTaglib(int,org.netbeans.modules.j2ee.dd.api.web.Taglib)
meth public abstract void setTaglib(org.netbeans.modules.j2ee.dd.api.web.Taglib[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.ComponentInterface
meth public abstract boolean isElIgnored()
meth public abstract boolean isIsXml()
meth public abstract boolean isScriptingInvalid()
meth public abstract int addIncludeCoda(java.lang.String)
meth public abstract int addIncludePrelude(java.lang.String)
meth public abstract int addUrlPattern(java.lang.String)
meth public abstract int removeIncludeCoda(java.lang.String)
meth public abstract int removeIncludePrelude(java.lang.String)
meth public abstract int removeUrlPattern(java.lang.String)
meth public abstract int sizeIncludeCoda()
meth public abstract int sizeIncludePrelude()
meth public abstract int sizeUrlPattern()
meth public abstract java.lang.String getIncludeCoda(int)
meth public abstract java.lang.String getIncludePrelude(int)
meth public abstract java.lang.String getPageEncoding()
meth public abstract java.lang.String getUrlPattern(int)
meth public abstract java.lang.String[] getIncludeCoda()
meth public abstract java.lang.String[] getIncludePrelude()
meth public abstract java.lang.String[] getUrlPattern()
meth public abstract void setElIgnored(boolean)
meth public abstract void setIncludeCoda(int,java.lang.String)
meth public abstract void setIncludeCoda(java.lang.String[])
meth public abstract void setIncludePrelude(int,java.lang.String)
meth public abstract void setIncludePrelude(java.lang.String[])
meth public abstract void setIsXml(boolean)
meth public abstract void setPageEncoding(java.lang.String)
meth public abstract void setScriptingInvalid(boolean)
meth public abstract void setUrlPattern(int,java.lang.String)
meth public abstract void setUrlPattern(java.lang.String[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.Listener
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
intf org.netbeans.modules.j2ee.dd.api.common.DisplayNameInterface
intf org.netbeans.modules.j2ee.dd.api.common.IconInterface
meth public abstract java.lang.String getListenerClass()
meth public abstract void setListenerClass(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.lang.String getEncoding()
meth public abstract java.lang.String getLocale()
meth public abstract void setEncoding(java.lang.String)
meth public abstract void setLocale(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMappingList
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.CreateCapability
intf org.netbeans.modules.j2ee.dd.api.common.FindCapability
meth public abstract int addLocaleEncodingMapping(org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping)
meth public abstract int removeLocaleEncodingMapping(org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping)
meth public abstract int sizeLocaleEncodingMapping()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping getLocaleEncodingMapping(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping[] getLocaleEncodingMapping()
meth public abstract void setLocaleEncodingMapping(int,org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping)
meth public abstract void setLocaleEncodingMapping(org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMapping[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.LoginConfig
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.CreateCapability
meth public abstract java.lang.String getAuthMethod()
meth public abstract java.lang.String getRealmName()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.FormLoginConfig getFormLoginConfig()
meth public abstract void setAuthMethod(java.lang.String)
meth public abstract void setFormLoginConfig(org.netbeans.modules.j2ee.dd.api.web.FormLoginConfig)
meth public abstract void setRealmName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.MimeMapping
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.lang.String getExtension()
meth public abstract java.lang.String getMimeType()
meth public abstract void setExtension(java.lang.String)
meth public abstract void setMimeType(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.RelativeOrdering
meth public abstract org.netbeans.modules.j2ee.dd.api.web.RelativeOrderingItems getAfter()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.RelativeOrderingItems getBefore()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.RelativeOrderingItems newRelativeOrderingItems()
meth public abstract void setAfter(org.netbeans.modules.j2ee.dd.api.web.RelativeOrderingItems)
meth public abstract void setBefore(org.netbeans.modules.j2ee.dd.api.web.RelativeOrderingItems)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.RelativeOrderingItems
meth public abstract int addName(java.lang.String)
meth public abstract int removeName(java.lang.String)
meth public abstract int sizeName()
meth public abstract java.lang.String getName(int)
meth public abstract java.lang.String[] getName()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.RelativeOrderingOthersItem getOthers()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.RelativeOrderingOthersItem newRelativeOrderingOthersItem()
meth public abstract void setName(int,java.lang.String)
meth public abstract void setName(java.lang.String[])
meth public abstract void setOthers(org.netbeans.modules.j2ee.dd.api.web.RelativeOrderingOthersItem)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.RelativeOrderingOthersItem
meth public abstract java.lang.String getId()
meth public abstract void setId(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.CreateCapability
intf org.netbeans.modules.j2ee.dd.api.common.DisplayNameInterface
intf org.netbeans.modules.j2ee.dd.api.common.FindCapability
meth public abstract int addWebResourceCollection(org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection)
meth public abstract int removeWebResourceCollection(org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection)
meth public abstract int sizeWebResourceCollection()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.AuthConstraint getAuthConstraint()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.UserDataConstraint getUserDataConstraint()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection getWebResourceCollection(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection[] getWebResourceCollection()
meth public abstract void setAuthConstraint(org.netbeans.modules.j2ee.dd.api.web.AuthConstraint)
meth public abstract void setUserDataConstraint(org.netbeans.modules.j2ee.dd.api.web.UserDataConstraint)
meth public abstract void setWebResourceCollection(int,org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection)
meth public abstract void setWebResourceCollection(org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.Servlet
intf org.netbeans.modules.j2ee.dd.api.common.ComponentInterface
meth public abstract int addInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam)
meth public abstract int addSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef)
meth public abstract int removeInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam)
meth public abstract int removeSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef)
meth public abstract int sizeInitParam()
meth public abstract int sizeSecurityRoleRef()
meth public abstract java.lang.String getJspFile()
meth public abstract java.lang.String getServletClass()
meth public abstract java.lang.String getServletName()
meth public abstract java.math.BigInteger getLoadOnStartup()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InitParam getInitParam(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InitParam[] getInitParam()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.RunAs getRunAs()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef getSecurityRoleRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef[] getSecurityRoleRef()
meth public abstract void setInitParam(int,org.netbeans.modules.j2ee.dd.api.common.InitParam)
meth public abstract void setInitParam(org.netbeans.modules.j2ee.dd.api.common.InitParam[])
meth public abstract void setJspFile(java.lang.String)
meth public abstract void setLoadOnStartup(java.math.BigInteger)
meth public abstract void setRunAs(org.netbeans.modules.j2ee.dd.api.common.RunAs)
meth public abstract void setSecurityRoleRef(int,org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef)
meth public abstract void setSecurityRoleRef(org.netbeans.modules.j2ee.dd.api.common.SecurityRoleRef[])
meth public abstract void setServletClass(java.lang.String)
meth public abstract void setServletName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.ServletMapping
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.lang.String getServletName()
meth public abstract java.lang.String getUrlPattern()
 anno 0 java.lang.Deprecated()
meth public abstract void setServletName(java.lang.String)
meth public abstract void setUrlPattern(java.lang.String)
 anno 0 java.lang.Deprecated()

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.ServletMapping25
intf org.netbeans.modules.j2ee.dd.api.web.ServletMapping
meth public abstract int addUrlPattern(java.lang.String)
meth public abstract int removeUrlPattern(java.lang.String)
meth public abstract int sizeUrlPattern()
meth public abstract java.lang.String getUrlPattern(int)
meth public abstract java.lang.String[] getUrlPatterns()
meth public abstract void setUrlPattern(int,java.lang.String)
meth public abstract void setUrlPatterns(java.lang.String[])

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.SessionConfig
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.math.BigInteger getSessionTimeout()
meth public abstract void setSessionTimeout(java.math.BigInteger)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.Taglib
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract java.lang.String getTaglibLocation()
meth public abstract java.lang.String getTaglibUri()
meth public abstract void setTaglibLocation(java.lang.String)
meth public abstract void setTaglibUri(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.UserDataConstraint
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract java.lang.String getTransportGuarantee()
meth public abstract void setTransportGuarantee(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.WebApp
fld public final static int STATE_INVALID_OLD_VERSION = 3
fld public final static int STATE_INVALID_PARSABLE = 1
fld public final static int STATE_INVALID_UNPARSABLE = 2
fld public final static int STATE_VALID = 0
fld public final static java.lang.String PROPERTY_STATUS = "dd_status"
fld public final static java.lang.String PROPERTY_VERSION = "dd_version"
fld public final static java.lang.String VERSION_2_4 = "2.4"
fld public final static java.lang.String VERSION_2_5 = "2.5"
fld public final static java.lang.String VERSION_3_0 = "3.0"
fld public final static java.lang.String VERSION_3_1 = "3.1"
fld public final static java.lang.String VERSION_4_0 = "4.0"
fld public final static java.lang.String VERSION_5_0 = "5.0"
fld public final static java.lang.String VERSION_6_0 = "6.0"
intf org.netbeans.modules.j2ee.dd.api.common.RootInterface
meth public abstract boolean isDistributable()
meth public abstract boolean isMetadataComplete() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addContextParam(org.netbeans.modules.j2ee.dd.api.common.InitParam)
meth public abstract int addEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef)
meth public abstract int addEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef)
meth public abstract int addEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry)
meth public abstract int addErrorPage(org.netbeans.modules.j2ee.dd.api.web.ErrorPage)
meth public abstract int addFilter(org.netbeans.modules.j2ee.dd.api.web.Filter)
meth public abstract int addFilterMapping(org.netbeans.modules.j2ee.dd.api.web.FilterMapping)
meth public abstract int addJspConfig(org.netbeans.modules.j2ee.dd.api.web.JspConfig) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addListener(org.netbeans.modules.j2ee.dd.api.web.Listener)
meth public abstract int addMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addMimeMapping(org.netbeans.modules.j2ee.dd.api.web.MimeMapping)
meth public abstract int addResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef)
meth public abstract int addResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef)
meth public abstract int addSecurityConstraint(org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint)
meth public abstract int addSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole)
meth public abstract int addServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int addServlet(org.netbeans.modules.j2ee.dd.api.web.Servlet)
meth public abstract int addServletMapping(org.netbeans.modules.j2ee.dd.api.web.ServletMapping)
meth public abstract int getStatus()
meth public abstract int removeContextParam(org.netbeans.modules.j2ee.dd.api.common.InitParam)
meth public abstract int removeEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef)
meth public abstract int removeEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef)
meth public abstract int removeEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry)
meth public abstract int removeErrorPage(org.netbeans.modules.j2ee.dd.api.web.ErrorPage)
meth public abstract int removeFilter(org.netbeans.modules.j2ee.dd.api.web.Filter)
meth public abstract int removeFilterMapping(org.netbeans.modules.j2ee.dd.api.web.FilterMapping)
meth public abstract int removeJspConfig(org.netbeans.modules.j2ee.dd.api.web.JspConfig) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeListener(org.netbeans.modules.j2ee.dd.api.web.Listener)
meth public abstract int removeMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeMimeMapping(org.netbeans.modules.j2ee.dd.api.web.MimeMapping)
meth public abstract int removeResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef)
meth public abstract int removeResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef)
meth public abstract int removeSecurityConstraint(org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint)
meth public abstract int removeSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole)
meth public abstract int removeServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int removeServlet(org.netbeans.modules.j2ee.dd.api.web.Servlet)
meth public abstract int removeServletMapping(org.netbeans.modules.j2ee.dd.api.web.ServletMapping)
meth public abstract int sizeContextParam()
meth public abstract int sizeEjbLocalRef()
meth public abstract int sizeEjbRef()
meth public abstract int sizeEnvEntry()
meth public abstract int sizeErrorPage()
meth public abstract int sizeFilter()
meth public abstract int sizeFilterMapping()
meth public abstract int sizeListener()
meth public abstract int sizeMessageDestination() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeMessageDestinationRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeMimeMapping()
meth public abstract int sizeResourceEnvRef()
meth public abstract int sizeResourceRef()
meth public abstract int sizeSecurityConstraint()
meth public abstract int sizeSecurityRole()
meth public abstract int sizeServiceRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract int sizeServlet()
meth public abstract int sizeServletMapping()
meth public abstract java.lang.String getVersion()
meth public abstract java.lang.String[] getName() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef getEjbLocalRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef[] getEjbLocalRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbRef getEjbRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EjbRef[] getEjbRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EnvEntry getEnvEntry(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.EnvEntry[] getEnvEntry()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InitParam getContextParam(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.InitParam[] getContextParam()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestination getMessageDestination(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef getMessageDestinationRef(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[] getMessageDestinationRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.MessageDestination[] getMessageDestination() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef getResourceEnvRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[] getResourceEnvRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceRef getResourceRef(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ResourceRef[] getResourceRef()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.SecurityRole getSecurityRole(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.common.SecurityRole[] getSecurityRole()
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRef getServiceRef(int) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.common.ServiceRef[] getServiceRef() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.web.AbsoluteOrdering newAbsoluteOrdering() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.web.AbsoluteOrdering[] getAbsoluteOrdering() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.web.ErrorPage getErrorPage(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.web.ErrorPage[] getErrorPage()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.Filter getFilter(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.web.FilterMapping getFilterMapping(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.web.FilterMapping[] getFilterMapping()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.Filter[] getFilter()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.JspConfig getSingleJspConfig() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.web.Listener getListener(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.web.Listener[] getListener()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMappingList getSingleLocaleEncodingMappingList() throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract org.netbeans.modules.j2ee.dd.api.web.LoginConfig getSingleLoginConfig()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.MimeMapping getMimeMapping(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.web.MimeMapping[] getMimeMapping()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint getSecurityConstraint(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint[] getSecurityConstraint()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.Servlet getServlet(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.web.ServletMapping getServletMapping(int)
meth public abstract org.netbeans.modules.j2ee.dd.api.web.ServletMapping[] getServletMapping()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.Servlet[] getServlet()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.SessionConfig getSingleSessionConfig()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList getSingleWelcomeFileList()
meth public abstract org.xml.sax.SAXParseException getError()
meth public abstract void setAbsoluteOrdering(org.netbeans.modules.j2ee.dd.api.web.AbsoluteOrdering[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setContextParam(int,org.netbeans.modules.j2ee.dd.api.common.InitParam)
meth public abstract void setContextParam(org.netbeans.modules.j2ee.dd.api.common.InitParam[])
meth public abstract void setDistributable(boolean)
meth public abstract void setEjbLocalRef(int,org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef)
meth public abstract void setEjbLocalRef(org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef[])
meth public abstract void setEjbRef(int,org.netbeans.modules.j2ee.dd.api.common.EjbRef)
meth public abstract void setEjbRef(org.netbeans.modules.j2ee.dd.api.common.EjbRef[])
meth public abstract void setEnvEntry(int,org.netbeans.modules.j2ee.dd.api.common.EnvEntry)
meth public abstract void setEnvEntry(org.netbeans.modules.j2ee.dd.api.common.EnvEntry[])
meth public abstract void setErrorPage(int,org.netbeans.modules.j2ee.dd.api.web.ErrorPage)
meth public abstract void setErrorPage(org.netbeans.modules.j2ee.dd.api.web.ErrorPage[])
meth public abstract void setFilter(int,org.netbeans.modules.j2ee.dd.api.web.Filter)
meth public abstract void setFilter(org.netbeans.modules.j2ee.dd.api.web.Filter[])
meth public abstract void setFilterMapping(int,org.netbeans.modules.j2ee.dd.api.web.FilterMapping)
meth public abstract void setFilterMapping(org.netbeans.modules.j2ee.dd.api.web.FilterMapping[])
meth public abstract void setJspConfig(org.netbeans.modules.j2ee.dd.api.web.JspConfig) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setListener(int,org.netbeans.modules.j2ee.dd.api.web.Listener)
meth public abstract void setListener(org.netbeans.modules.j2ee.dd.api.web.Listener[])
meth public abstract void setLocaleEncodingMappingList(org.netbeans.modules.j2ee.dd.api.web.LocaleEncodingMappingList) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setLoginConfig(org.netbeans.modules.j2ee.dd.api.web.LoginConfig)
meth public abstract void setMessageDestination(int,org.netbeans.modules.j2ee.dd.api.common.MessageDestination) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMessageDestination(org.netbeans.modules.j2ee.dd.api.common.MessageDestination[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMessageDestinationRef(int,org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMessageDestinationRef(org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMetadataComplete(boolean) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setMimeMapping(int,org.netbeans.modules.j2ee.dd.api.web.MimeMapping)
meth public abstract void setMimeMapping(org.netbeans.modules.j2ee.dd.api.web.MimeMapping[])
meth public abstract void setName(java.lang.String[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setResourceEnvRef(int,org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef)
meth public abstract void setResourceEnvRef(org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef[])
meth public abstract void setResourceRef(int,org.netbeans.modules.j2ee.dd.api.common.ResourceRef)
meth public abstract void setResourceRef(org.netbeans.modules.j2ee.dd.api.common.ResourceRef[])
meth public abstract void setSecurityConstraint(int,org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint)
meth public abstract void setSecurityConstraint(org.netbeans.modules.j2ee.dd.api.web.SecurityConstraint[])
meth public abstract void setSecurityRole(int,org.netbeans.modules.j2ee.dd.api.common.SecurityRole)
meth public abstract void setSecurityRole(org.netbeans.modules.j2ee.dd.api.common.SecurityRole[])
meth public abstract void setServiceRef(int,org.netbeans.modules.j2ee.dd.api.common.ServiceRef) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setServiceRef(org.netbeans.modules.j2ee.dd.api.common.ServiceRef[]) throws org.netbeans.modules.j2ee.dd.api.common.VersionNotSupportedException
meth public abstract void setServlet(int,org.netbeans.modules.j2ee.dd.api.web.Servlet)
meth public abstract void setServlet(org.netbeans.modules.j2ee.dd.api.web.Servlet[])
meth public abstract void setServletMapping(int,org.netbeans.modules.j2ee.dd.api.web.ServletMapping)
meth public abstract void setServletMapping(org.netbeans.modules.j2ee.dd.api.web.ServletMapping[])
meth public abstract void setSessionConfig(org.netbeans.modules.j2ee.dd.api.web.SessionConfig)
meth public abstract void setWelcomeFileList(org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata
meth public abstract java.util.List<java.lang.String> getSecurityRoles()
meth public abstract java.util.List<org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef> getEjbLocalRefs()
meth public abstract java.util.List<org.netbeans.modules.j2ee.dd.api.common.EjbRef> getEjbRefs()
meth public abstract java.util.List<org.netbeans.modules.j2ee.dd.api.common.EnvEntry> getEnvEntries()
meth public abstract java.util.List<org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef> getMessageDestinationRefs()
meth public abstract java.util.List<org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef> getResourceEnvRefs()
meth public abstract java.util.List<org.netbeans.modules.j2ee.dd.api.common.ResourceRef> getResourceRefs()
meth public abstract java.util.List<org.netbeans.modules.j2ee.dd.api.common.ServiceRef> getServiceRefs()
meth public abstract java.util.List<org.netbeans.modules.j2ee.dd.api.web.WebFragment> getFragments()
meth public abstract java.util.List<org.netbeans.modules.j2ee.dd.api.web.model.FilterInfo> getFilters()
meth public abstract java.util.List<org.netbeans.modules.j2ee.dd.api.web.model.ServletInfo> getServlets()
meth public abstract java.util.List<org.openide.filesystems.FileObject> getFragmentFiles()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.WebApp getRoot()

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.WebFragment
intf org.netbeans.modules.j2ee.dd.api.common.RootInterface
intf org.netbeans.modules.j2ee.dd.api.web.WebApp
meth public abstract org.netbeans.modules.j2ee.dd.api.web.RelativeOrdering newRelativeOrdering()
meth public abstract org.netbeans.modules.j2ee.dd.api.web.RelativeOrdering[] getOrdering()
meth public abstract void setOrdering(org.netbeans.modules.j2ee.dd.api.web.RelativeOrdering[])

CLSS public final org.netbeans.modules.j2ee.dd.api.web.WebFragmentProvider
meth public org.netbeans.modules.j2ee.dd.api.web.WebFragment getWebFragmentRoot(org.openide.filesystems.FileObject) throws java.io.IOException
meth public static org.netbeans.modules.j2ee.dd.api.web.WebFragmentProvider getDefault()
supr java.lang.Object
hfds LOG,instance

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.WebResourceCollection
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
intf org.netbeans.modules.j2ee.dd.api.common.DescriptionInterface
meth public abstract int addHttpMethod(java.lang.String)
meth public abstract int addUrlPattern(java.lang.String)
meth public abstract int removeHttpMethod(java.lang.String)
meth public abstract int removeUrlPattern(java.lang.String)
meth public abstract int sizeHttpMethod()
meth public abstract int sizeUrlPattern()
meth public abstract java.lang.String getHttpMethod(int)
meth public abstract java.lang.String getUrlPattern(int)
meth public abstract java.lang.String getWebResourceName()
meth public abstract java.lang.String[] getHttpMethod()
meth public abstract java.lang.String[] getUrlPattern()
meth public abstract void setHttpMethod(int,java.lang.String)
meth public abstract void setHttpMethod(java.lang.String[])
meth public abstract void setUrlPattern(int,java.lang.String)
meth public abstract void setUrlPattern(java.lang.String[])
meth public abstract void setWebResourceName(java.lang.String)

CLSS public abstract interface org.netbeans.modules.j2ee.dd.api.web.WelcomeFileList
intf org.netbeans.modules.j2ee.dd.api.common.CommonDDBean
meth public abstract int addWelcomeFile(java.lang.String)
meth public abstract int removeWelcomeFile(java.lang.String)
meth public abstract int sizeWelcomeFile()
meth public abstract java.lang.String getWelcomeFile(int)
meth public abstract java.lang.String[] getWelcomeFile()
meth public abstract void setWelcomeFile(int,java.lang.String)
meth public abstract void setWelcomeFile(java.lang.String[])

CLSS public org.netbeans.modules.j2ee.dd.api.web.model.FilterInfo
meth public java.lang.String getFilterClass()
meth public java.lang.String getName()
meth public java.util.List<java.lang.String> getUrlPatterns()
supr java.lang.Object
hfds filterClass,name,urlPatterns

CLSS public final org.netbeans.modules.j2ee.dd.api.web.model.ServletInfo
meth public java.lang.String getName()
meth public java.lang.String getServletClass()
meth public java.util.List<java.lang.String> getUrlPatterns()
supr java.lang.Object
hfds name,servletClass,urlPatterns

CLSS public org.netbeans.modules.j2ee.dd.spi.MetadataUnit
fld public final static java.lang.String PROP_DEPLOYMENT_DESCRIPTOR = "deploymentDescriptor"
meth public org.netbeans.api.java.classpath.ClassPath getBootPath()
meth public org.netbeans.api.java.classpath.ClassPath getCompilePath()
meth public org.netbeans.api.java.classpath.ClassPath getSourcePath()
meth public org.openide.filesystems.FileObject getDeploymentDescriptor()
meth public static org.netbeans.modules.j2ee.dd.spi.MetadataUnit create(org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.java.classpath.ClassPath,org.netbeans.api.java.classpath.ClassPath,java.io.File)
meth public void addPropertyChangeListener(java.beans.PropertyChangeListener)
meth public void changeDeploymentDescriptor(java.io.File)
meth public void removePropertyChangeListener(java.beans.PropertyChangeListener)
supr java.lang.Object
hfds bootPath,compilePath,deploymentDescriptor,propChangeSupport,sourcePath

CLSS public org.netbeans.modules.j2ee.dd.spi.client.AppClientMetadataModelFactory
meth public static org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata> createMetadataModel(org.netbeans.modules.j2ee.dd.spi.MetadataUnit)
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.dd.spi.ejb.EjbJarMetadataModelFactory
meth public static org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata> createMetadataModel(org.netbeans.modules.j2ee.dd.spi.MetadataUnit)
supr java.lang.Object

CLSS public org.netbeans.modules.j2ee.dd.spi.web.WebAppMetadataModelFactory
meth public static org.netbeans.modules.j2ee.metadata.model.api.MetadataModel<org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata> createMetadataModel(org.netbeans.modules.j2ee.dd.spi.MetadataUnit,boolean)
supr java.lang.Object

